// =======================================================================================================================================================================================================
// System: ZaraStar Analytic: Generate 30, 60, 90, 120 for a cust  
// Module: CustomerPeriods.java
// Author: C.K.Harvey
// Copyright (c) 1999-2007 Christopher Harvey. All Rights Reserved.
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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class CustomerPeriods extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  ReportGeneration reportGeneration = new ReportGeneration();
  Customer customer = new Customer();
  AccountsUtils accountsUtils = new AccountsUtils();
  MiscDefinitions miscDefinitions = new MiscDefinitions();
  DefinitionTables definitionTables = new DefinitionTables();
  
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="";
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
      p1  = req.getParameter("p1");  // customer
      p2  = req.getParameter("p2");  // dateTo

      if(p1 == null) p1 = "";

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "CustomerPeriods", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1012, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                    String p1, String p2, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');

    String userName = directoryUtils.getMySQLUserName();
    String passWord = directoryUtils.getMySQLPassWord();
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + userName + "&password=" + passWord);
    Statement stmt = null;
    ResultSet rs = null;
    Statement stmt2 = null;
    ResultSet rs2 = null;
    Statement stmt3 = null;
    ResultSet rs3 = null;

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "1012e", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1012, bytesOut[0], 0, "SID:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }
    
    String dateFrom = generalUtils.convertDateToSQLFormat(definitionTables.getAppConfigApplicationStartDate(con, stmt, rs, dnm));

    out.println(calc(con, stmt, stmt2, stmt3, rs, rs2, rs3, p1, dateFrom, p2, dnm, localDefnsDir, defnsDir));

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 1012, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private String calc(Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, ResultSet rs3,
                      String customerCode, String dateFrom, String dateTo, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    String today = generalUtils.todaySQLFormat(localDefnsDir, defnsDir);
    int dateLess30  = generalUtils.encodeFromYYYYMMDD(today) - 30;  
    int dateLess60  = dateLess30 - 30;  
    int dateLess90  = dateLess30 - 60;  
    
    byte[] data = new byte[3000];

    String customerCurrency;

    if(customer.getCompanyRecGivenCode(con, stmt, rs, customerCode, '\000', dnm, data, localDefnsDir, defnsDir) != -1) // just-in-case
      customerCurrency = generalUtils.dfsAsStr(data, (short)38);
    else return "";

    boolean[] currencyMismatch = new boolean[1]; currencyMismatch[0] = false;

    double[] upto30  = new double[1]; upto30[0]  = 0.0;
    double[] upto60  = new double[1]; upto60[0]  = 0.0;
    double[] upto90  = new double[1]; upto90[0]  = 0.0;
    double[] over    = new double[1]; over[0]    = 0.0;

    int[] earliestDate = new int[1];  earliestDate[0] = generalUtils.encodeFromYYYYMMDD("2099-12-31"); 

    double balance = calcSpread(con, stmt, stmt2, stmt3, rs, rs2, rs3, customerCode, currencyMismatch, dateFrom, dateTo, customerCurrency, dateLess30,
                                dateLess60, dateLess90, upto30, upto60, upto90, over, earliestDate, dnm, localDefnsDir, defnsDir);
    
    customer.updatedDateInvoicePaid(con, stmt, rs, generalUtils.decodeToYYYYMMDD(earliestDate[0]), customerCode, dnm, localDefnsDir, defnsDir);

    if(currencyMismatch[0])
      customerCurrency = "???";
    else
    {
      int x = customerCurrency.length();
      while(x < 3)
      {
        customerCurrency += " ";
        ++x;
      }
    }
    
    return writeSpread(customerCurrency, balance, upto30[0], upto60[0], upto90[0], over[0], localDefnsDir, defnsDir);
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private String writeSpread(String customerCurrency, double total, double upto1, double upto2, double upto3, double over, String localDefnsDir,
                             String defnsDir) throws Exception
  {
    byte[] b = new byte[20];

    generalUtils.doubleToBytesCharFormat(total, b, 0);
    generalUtils.formatNumeric(b, '2');
    String rtnStr = customerCurrency + generalUtils.stringFromBytes(b, 0L);
    
    generalUtils.doubleToBytesCharFormat(upto1, b, 0);
    generalUtils.formatNumeric(b, '2');
    rtnStr += (" " + generalUtils.stringFromBytes(b, 0L));

    generalUtils.doubleToBytesCharFormat(upto2, b, 0);
    generalUtils.formatNumeric(b, '2');
    rtnStr += (" " + generalUtils.stringFromBytes(b, 0L));

    generalUtils.doubleToBytesCharFormat(upto3, b, 0);
    generalUtils.formatNumeric(b, '2');
    rtnStr += (" " + generalUtils.stringFromBytes(b, 0L));

    generalUtils.doubleToBytesCharFormat(over, b, 0);
    generalUtils.formatNumeric(b, '2');
    rtnStr += (" " + generalUtils.stringFromBytes(b, 0L));
    
    return rtnStr;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private double calcSpread(Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, ResultSet rs3,
                            String customerCode, boolean[] currencyMismatch, String dateFrom, String dateTo, String customerCurrency, int dateLess30,
                            int dateLess60, int dateLess90, double[] upto30, double[] upto60, double[] upto90, double[] over, int[] earliestDate,
                            String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    double total = 0.0;
    double[] outstanding = new double[1];  outstanding[0] = 0.0;
    String invoiceCode, thisCurrency, dnCode;
    int thisDate;
    
    stmt = con.createStatement();

    // invoices

    rs = stmt.executeQuery("SELECT InvoiceCode, Date, TotalTotal, Currency FROM invoice WHERE CompanyCode = '" + customerCode
                         + "' AND Status != 'C' AND Settled != 'Y' AND Date >= {d '" + dateFrom + "'} AND Date <= {d '" + dateTo + "'}");

    while(rs.next())
    {
      invoiceCode    = rs.getString(1);
      thisDate       = generalUtils.encodeFromYYYYMMDD(rs.getString(2));
      outstanding[0] = generalUtils.doubleFromStr(generalUtils.doubleDPs(rs.getString(3), '2'));
      thisCurrency   = rs.getString(4);
   
      detReceiptLines(con, stmt2, rs2, invoiceCode, dateTo, outstanding);
      detSalesCreditNoteLines(con, stmt2, stmt3, rs2, rs3, invoiceCode, dateTo, dnm, localDefnsDir, defnsDir, outstanding);
      
      if(generalUtils.doubleDPs(outstanding[0], '2') != 0.0)
      {
        if(thisDate < earliestDate[0])
          earliestDate[0] = thisDate;

        total += outstanding[0];

        if(thisDate > dateLess30)
          upto30[0] += outstanding[0];
        else
        if(thisDate > dateLess60)
          upto60[0] += outstanding[0];
        else
        if(thisDate > dateLess90)
          upto90[0] += outstanding[0];
        else over[0] += outstanding[0];

        if(thisCurrency.length() > 0 && ! thisCurrency.equals(customerCurrency))
          currencyMismatch[0] = true;
      }
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
 
    // debit notes

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT DNCode, Date, TotalTotal, Currency FROM debit WHERE CompanyCode = '" + customerCode
                         + "' AND Status != 'C' AND Settled != 'Y' AND Date >= {d '" + dateFrom + "'} AND Date <= {d '" + dateTo + "'}");

    while(rs.next())
    {
      dnCode         = rs.getString(1);
      thisDate       = generalUtils.encodeFromYYYYMMDD(rs.getString(2));
      outstanding[0] = generalUtils.doubleFromStr(generalUtils.doubleDPs(rs.getString(3), '2'));
      thisCurrency   = rs.getString(4);

      detReceiptLines(con, stmt2, rs2, dnCode, dateTo, outstanding);
      detSalesCreditNoteLines(con, stmt2, stmt3, rs2, rs3, dnCode, dateTo, dnm, localDefnsDir, defnsDir, outstanding);
      
      if(generalUtils.doubleDPs(outstanding[0], '2') != 0.0)
      {
        if(thisDate < earliestDate[0])
          earliestDate[0] = thisDate;
      
        total += outstanding[0];

        if(thisDate > dateLess30)
          upto30[0] += outstanding[0];
        else
        if(thisDate > dateLess60)
          upto60[0] += outstanding[0];
        else
        if(thisDate > dateLess90)
          upto90[0] += outstanding[0];
        else over[0] += outstanding[0];

        if(thisCurrency.length() > 0 && ! thisCurrency.equals(customerCurrency))
          currencyMismatch[0] = true;
      }
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    return total; 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void detReceiptLines(Connection con, Statement stmt, ResultSet rs, String invoiceCode, String dateTo, double[] outstanding) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.AmountReceived FROM receiptl AS t2 INNER JOIN receipt AS t1 ON t1.ReceiptCode = t2.ReceiptCode "
                         + "WHERE t2.InvoiceCode = '" + invoiceCode + "' AND t1.Status != 'C' AND t1.DateReceived <= {d '" + dateTo + "'}");

    double amount;

    while(rs.next())
    {    
      amount = generalUtils.doubleFromStr(rs.getString(1));
      outstanding[0] -= generalUtils.doubleDPs(amount, '2');
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void detSalesCreditNoteLines(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String invoiceCode, String dateTo,
                                  String dnm, String localDefnsDir, String defnsDir, double[] outstanding) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.Amount2, t2.GSTRate FROM creditl AS t2 INNER JOIN credit AS t1 ON t1.CNCode = t2.CNCode "
                         + "WHERE t2.InvoiceCode = '" + invoiceCode + "' AND t1.Status != 'C' AND t1.Date <= {d '" + dateTo + "'}");

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
