// =======================================================================================================================================================================================================
// System: ZaraStar Analytic: generate consolidated debtors - calculate bottomline  
// Module: ConsolidatedDebtorsCalc.java
// Author: C.K.Harvey
// Copyright (c) 2003-07 Christopher Harvey. All Rights Reserved.
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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.io.*;

public class ConsolidatedDebtorsCalc extends HttpServlet
{
  GeneralUtils  generalUtils = new GeneralUtils();
  MessagePage  messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils  pageFrameUtils = new PageFrameUtils();
  DirectoryUtils  directoryUtils = new DirectoryUtils();
  Customer customer = new Customer();
  AccountsUtils  accountsUtils = new AccountsUtils();
  DocumentUtils  documentUtils = new DocumentUtils();
  
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="";

    try
    {
      directoryUtils.setContentHeaders2(res);
      out = res.getWriter();

      unm = req.getParameter("unm");
      sid = req.getParameter("sid");
      uty = req.getParameter("uty");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, bytesOut);
    }    
    catch(Exception e)
    {
      StringBuffer url = req.getRequestURL();
      String urlBit="";
      int x=0;
      if(url.charAt(x) == 'h' || url.charAt(x) == 'H')
        x += 7;
      while(url.charAt(x) != '\000' && url.charAt(x) != ',' && url.charAt(x) != '/')
        urlBit += url.charAt(x++);

      System.out.println(e);
      serverUtils.etotalBytes(req, unm, dnm, 1029, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den,
                    String dnm, String bnm, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String localDefnsDir  = directoryUtils.getLocalOverrideDir(dnm);
    String defnsDir          = directoryUtils.getSupportDirs('D');
    
    String userName = directoryUtils.getMySQLUserName();
    String passWord = directoryUtils.getMySQLPassWord();
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + userName + "&password="
                                                 + passWord);
      
    Statement stmt = null, stmt2 = null, stmt3 = null;
    ResultSet rs   = null, rs2 = null, rs3 = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 1029, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      String s = "ERR:Access Denied";
      out.println(s);
      bytesOut[0] += s.length(); 
      serverUtils.etotalBytes(req, unm, dnm, 1029, bytesOut[0], 0, "ACC:");
    if(con  != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      String s = "ERR:Session Timeout";
      out.println(s);
      bytesOut[0] += s.length(); 
      serverUtils.etotalBytes(req, unm, dnm, 1029, bytesOut[0], 0, "SID:");
    if(con  != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    process(con, stmt, stmt2, stmt3, rs, rs2, rs3, out, dnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 1029, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con  != null) con.close();
    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void process(Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, ResultSet rs3, PrintWriter out, String dnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    int x;
    
    String[][] currencies = new String[1][];  
    int numCurrencies = accountsUtils.getCurrencyNames(con, stmt, rs, currencies, dnm, localDefnsDir, defnsDir);
    double[] currencyTotals = new double[numCurrencies];
    for(x=0;x<numCurrencies;++x)
      currencyTotals[x] = 0.0;
    
    calculate(con, stmt, stmt2, stmt3, rs, rs2, rs3, currencyTotals, currencies, numCurrencies, dnm, localDefnsDir, defnsDir);
    
    if(con != null) con.close();

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

  // -------------------------------------------------------------------------------------------------------------------------------
  private void calculate(Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, ResultSet rs3, double[] currencyTotals,
                         String[][] currencies, int numCurrencies, String dnm, String localDefnsDir, String defnsDir)
                         throws Exception
  {
    double amt;
    double[] outstanding = new double[1];  outstanding[0] = 0.0;
    String invoiceCode, currency, dnCode;
    int x;
    
    stmt = con.createStatement();

    // invoices

    rs = stmt.executeQuery("SELECT InvoiceCode, TotalTotal, Currency FROM invoice WHERE Status != 'C' AND Settled != 'Y'");

    while(rs.next())
    {
      invoiceCode    = rs.getString(1);
      outstanding[0] = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(2)), '2');
      currency       = rs.getString(3);
   
      detReceiptLines(con, stmt2, rs2, invoiceCode, outstanding);
      detSalesCreditNoteLines(con, stmt2, stmt3, rs2, rs3, invoiceCode, dnm, localDefnsDir, defnsDir, outstanding);
      
      amt = generalUtils.doubleDPs(outstanding[0], '2');
      if(amt != 0.0)
      {
        for(x=0;x<numCurrencies;++x)
        {
          if(currency.equals(currencies[0][x]))
          {
            currencyTotals[x] += amt;
            x = numCurrencies;
          }
        }
      }
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
 
    // debit notes

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT DNCode, TotalTotal, Currency FROM debit WHERE Status != 'C' AND Settled != 'Y'");

    while(rs.next())
    {
      dnCode         = rs.getString(1);
      outstanding[0] = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(2)), '2');
      currency       = rs.getString(3);

      detReceiptLines(con, stmt2, rs2, dnCode, outstanding);
      detSalesCreditNoteLines(con, stmt2, stmt3, rs2, rs3, dnCode, dnm, localDefnsDir, defnsDir, outstanding);
      
      amt = generalUtils.doubleDPs(outstanding[0], '2');
      if(amt != 0.0)
      {
        for(x=0;x<numCurrencies;++x)
        {
          if(currency.equals(currencies[0][x]))
          {
            currencyTotals[x] += amt;
            x = numCurrencies;
          }
        }
      }
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void detReceiptLines(Connection con, Statement stmt, ResultSet rs, String invoiceCode, double[] outstanding)
                               throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.AmountReceived FROM receiptl AS t2 INNER JOIN receipt AS t1 ON t1.ReceiptCode = t2.ReceiptCode "
                         + "WHERE t2.InvoiceCode = '" + invoiceCode + "' AND t1.Status != 'C'");

    double amount;

    while(rs.next())
    {    
      amount = generalUtils.doubleFromStr(rs.getString(1));

      outstanding[0] -= generalUtils.doubleDPs(amount, '2');
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void detSalesCreditNoteLines(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String invoiceCode, String dnm,
                                  String localDefnsDir, String defnsDir, double[] outstanding) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.Amount2, t2.GSTRate FROM creditl AS t2 INNER JOIN credit AS t1 ON t1.CNCode = t2.CNCode "
                         + "WHERE t2.InvoiceCode = '" + invoiceCode + "' AND t1.Status != 'C'");

    String gstRate;
    double amount, gst;

    while(rs.next())
    {    
      amount  = generalUtils.doubleFromStr(rs.getString(1));
      gstRate = rs.getString(2);

      if(gstRate.length() > 0)
      {
        gst = amount * accountsUtils.getGSTRate(con, stmt2, rs2, gstRate);
        amount += gst;
      }

      outstanding[0] -= generalUtils.doubleDPs(amount, '2');
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

}
