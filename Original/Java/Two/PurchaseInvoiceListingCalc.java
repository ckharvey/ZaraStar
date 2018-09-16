// =======================================================================================================================================================================================================
// System: ZaraStar Analytic: purchase invoice listing enquiry by month - calculate bottomline
// Module: PurchaseInvoiceListingCalc.java
// Author: C.K.Harvey
// Copyright (c) 2002-07 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
//
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.sql.*;

public class PurchaseInvoiceListingCalc extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  AccountsUtils accountsUtils = new AccountsUtils();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", dateFrom="", dateTo="", cashOrAccount="";

    try
    {
      directoryUtils.setContentHeaders(res);
      out = res.getWriter();

      unm = req.getParameter("unm");
      sid = req.getParameter("sid");
      uty = req.getParameter("uty");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");
      dateFrom      = req.getParameter("dateFrom");
      dateTo        = req.getParameter("dateTo");
      cashOrAccount = req.getParameter("cashOrAccount");

      if(cashOrAccount == null) cashOrAccount = "B";
      
      doIt(out, req, dateFrom, dateTo, cashOrAccount, unm, sid, uty, men, den, dnm, bnm, bytesOut);
    }
    catch(Exception e)
    {
      StringBuffer url = req.getRequestURL();
      int x=0;
      if(url.charAt(x) == 'h' || url.charAt(x) == 'H')
        x += 7;
      String urlBit="";
      while(url.charAt(x) != '\000' && url.charAt(x) != ',' && url.charAt(x) != '/')
        urlBit += url.charAt(x++);
 
      System.out.println("1034b: " +e);

      serverUtils.etotalBytes(req, unm, dnm, 1034, bytesOut[0], 0, "ERR:" + dateFrom + " " + dateTo);
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String dateFrom, String dateTo, String cashOrAccount, String unm,
                    String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut)
                    throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    String uName    = directoryUtils.getMySQLUserName();
    String passWord = directoryUtils.getMySQLPassWord();

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + uName + "&password=" + passWord);
    Statement stmt = null;
    ResultSet rs = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 1034, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      String s = "ERR:Access Denied";
      out.println(s);
      bytesOut[0] += s.length(); 
      serverUtils.etotalBytes(req, unm, dnm, 1034, bytesOut[0], 0, "ACC:" + dateFrom + " " + dateTo);
    if(con  != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      String s = "ERR:Access Denied";
      out.println(s);
      bytesOut[0] += s.length(); 
      serverUtils.etotalBytes(req, unm, dnm, 1034, bytesOut[0], 0, "SID:" + dateFrom + " " + dateTo);
    if(con  != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    generate(con, stmt, rs, out, cashOrAccount.charAt(0), unm, dnm, dateFrom, dateTo, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 1034, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), dateFrom + " " + dateTo);
    if(con  != null) con.close();
    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void generate(Connection con, Statement stmt, ResultSet rs, PrintWriter out, char type, String unm, String dnm, String dateFrom, String dateTo,
                        String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    String[][] currencies = new String[1][];  
    int x, numCurrencies = accountsUtils.getCurrencyNames(con, stmt, rs, currencies, dnm, localDefnsDir, defnsDir);
    double[] currencyTotals = new double[numCurrencies];
    for(x=0;x<numCurrencies;++x)
      currencyTotals[0] = 0.0;
    
    stmt = con.createStatement();
  
    String cashOrAccountStr = "";
    if(type == 'C')
      cashOrAccountStr = " CashOrAccount = 'C' AND ";
    else
    if(type == 'A')
      cashOrAccountStr = " CashOrAccount != 'C' AND ";
    
    rs = stmt.executeQuery("SELECT TotalTotal, Currency, CashOrAccount FROM pinvoice WHERE " + cashOrAccountStr
                                   + "Status != 'C' && Date >= {d '" + dateFrom + "'} AND Date <= {d '" + dateTo
                                   + "'} ORDER BY CompanyCode, Date");

    String totalTotal, currency, cashOrAccount;

    double amt;

    while(rs.next())                  
    {
      totalTotal    = rs.getString(1);       
      currency      = rs.getString(2);
      cashOrAccount = rs.getString(3);

      if(type == 'B' || (type == 'C' && cashOrAccount.equals("C")) || (type == 'A' && cashOrAccount.equals("A")))
      {
        for(x=0;x<numCurrencies;++x)
        {
          if(currency.equals(currencies[0][x]))
          {
            amt = generalUtils.doubleFromStr(totalTotal);          
            currencyTotals[x] += amt;
            x = numCurrencies;
          }
        }
      }  
    }
  
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    String retStr = "";
    
    for(x=0;x<numCurrencies;++x)
    {
      if(generalUtils.doubleDPs(currencyTotals[x], '2') != 0.0)
      {
        retStr += (currencies[0][x] + ": " +  generalUtils.formatNumeric(currencyTotals[x], '2') + "\n");
      }
    }    

    out.println(retStr);
    bytesOut[0] += retStr.length(); 
  }

}
