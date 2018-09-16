// =======================================================================================================================================================================================================
// System: ZaraStar: Analytic: Create statements page for registered user
// Module: StatementsRegisteredUser.java
// Author: C.K.Harvey
// Copyright (c) 2000-07 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;
import java.io.*;

public class StatementsRegisteredUser extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  Customer customer = new Customer();
  AccountsUtils accountsUtils = new AccountsUtils();
  DefinitionTables definitionTables = new DefinitionTables();
  Wiki wiki = new Wiki();
  Profile profile = new Profile();
  AdminControlUtils adminControlUtils = new AdminControlUtils();
  
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
      p1  = req.getParameter("p1");  // dateFrom
      p2  = req.getParameter("p2");  // dateTo

      if(p1  == null) p1  = "";
      if(p2  == null) p2  = "";

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
 
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "StatementsRegisteredUser", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1016, bytesOut[0], 0, "ERR:" + p1 + " " + p2);
      if(out != null) out.flush(); 
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1, String p2, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

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

    if(! adminControlUtils.notDisabled(con, stmt, rs, 904))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "StatementsRegisteredUserInput", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1016, bytesOut[0], 0, "ACC:" + p1 + " " + p2);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "StatementsRegisteredUserInput", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1016, bytesOut[0], 0, "SID:" + p1 + " " + p2);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    int i = unm.indexOf("_");
        
    String[] customerCode = new String[1];

    if(! profile.getExternalAccessNameCustomerCode(con, stmt, rs, unm.substring(0, i), dnm, localDefnsDir, defnsDir, customerCode))
      customerCode[0] = "";
    
    if(customerCode[0].length() == 0)
    {
      // FIXME output screen saying none found...
      serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 1016, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1 + " " + p2);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;      
    }  

    go(con, stmt, stmt2, stmt3, rs, rs2, rs3, out, req, customerCode[0], p1, p2, unm, sid, uty, men, den, dnm, bnm, imagesDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 1016, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1 + " " + p2);
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void go(Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, ResultSet rs3, PrintWriter out, HttpServletRequest req, String customerCode, String dateFrom, String dateTo, String unm, String sid,
                  String uty, String men, String den, String dnm, String bnm, String imagesDir, String localDefnsDir, String defnsDir,
                  int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Statement of Account Report</title></head>");

    String liveDate = generalUtils.convertDateToSQLFormat(definitionTables.getAppConfigApplicationStartDate(con, stmt, rs, dnm));
        
    String dateFromText = dateFrom;
    dateFrom = generalUtils.convertDateToSQLFormat(dateFrom);

    String dateToText = dateTo;
    dateTo = generalUtils.convertDateToSQLFormat(dateTo);

    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"print\" href=\""  + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "generalPlain.css" + "\">");

    outputPageFrame(con, stmt, rs, out, req, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);
     
    String customerCurrency = customer.getCompanyCurrencyGivenCode(con, stmt, rs, customerCode);
    String companyName      = customer.getCompanyNameGivenCode(con, stmt, rs, customerCode);

    scoutln(out, bytesOut, "<form><table id=\"page\" width=100% border=0>");

    scoutln(out, bytesOut, "<tr><td colspan=3 nowrap><p><b>Statement of Account</b>: ");
    scoutln(out, bytesOut, customerCode + " " + companyName + "</td></tr>");

    scoutln(out, bytesOut, "<tr><td><img src=\""+imagesDir+"z402.gif\" border=0></td></tr>");

    out.print("<tr><td colspan=3 nowrap><p>Statement dated: " + generalUtils.today(localDefnsDir, defnsDir)); 
    out.print(", for period: " + dateFromText + " to " + dateToText);
    scoutln(out, bytesOut, ", in currency: " + customerCurrency + "</td></tr>");

    scoutln(out, bytesOut, "<tr><td><img src=\""+imagesDir+"z402.gif\" border=0></td></tr>");

    scoutln(out, bytesOut, "<tr><td colspan=5><img src=\""+imagesDir+"blm2.gif\" width=100% height=3></td></tr>");
    scoutln(out, bytesOut, "<tr><td><img src=\""+imagesDir+"z402.gif\" border=0></td></tr></table>");

    scoutln(out, bytesOut, "<table id=\"page\" cellspacing=3 border=0 width=100%>");

    scoutln(out, bytesOut, "<tr bgcolor=\"#F0F0F0\"><td><p><b>Date</td>");
    scoutln(out, bytesOut, "<td><p><b>Document Type</td>");
    scoutln(out, bytesOut, "<td><p><b>Document Code</td>");
    scoutln(out, bytesOut, "<td><p><b>Reference</td>");
    scoutln(out, bytesOut, "<td align=right><p><b>Dr Amount</td>");
    scoutln(out, bytesOut, "<td align=right><p><b>Cr Amount</td>");
    scoutln(out, bytesOut, "<td align=right><p><b>Balance</td></tr>");

    generate(con, stmt, stmt2, stmt3, rs, rs2, rs3, out, customerCurrency, customerCode, dateFrom, dateTo, liveDate, unm, dnm, localDefnsDir, defnsDir, bytesOut);
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void generate(Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, ResultSet rs3, PrintWriter out, String customerCurrency, String customerCode, String dateFrom, String dateTo, String liveDate,
                        String unm, String dnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    int dateLess30 = generalUtils.encodeFromYYYYMMDD(dateTo) - 30;  
    int dateLess60 = dateLess30 - 30;
    int dateLess90 = dateLess30 - 60;  
    
    boolean[] currencyMismatch = new boolean[1]; currencyMismatch[0] = false;
    
    boolean[] isGreen = new boolean[1];  isGreen[0] = false;

    double[] upto30  = new double[1]; upto30[0]  = 0.0;
    double[] upto60  = new double[1]; upto60[0]  = 0.0;
    double[] upto90  = new double[1]; upto90[0]  = 0.0;
    double[] over    = new double[1]; over[0]    = 0.0;

    int[] earliestDate = new int[1];  earliestDate[0] = generalUtils.encodeFromYYYYMMDD("2099-12-31"); 
    double[] balance = new double[1];
    balance[0] = openBalance(con, stmt, stmt2, stmt3, rs, rs2, rs3, customerCode, currencyMismatch, dateFrom, liveDate, customerCurrency, dateLess30, dateLess60, dateLess90, upto30, upto60, upto90, over, earliestDate);
    
    writeBL1(out, "", "O", "", "", "", balance, isGreen, bytesOut);

    int i = unm.indexOf("_");
    String tmpTable = unm.substring(0, i) + "_tmp";
      
    directoryUtils.createTmpTable(true, con, stmt, "DocumentDate date, DocumentCode char(50), DocumentType char(1), Reference char(20), Amount decimal(18,9), EffectiveDate date", "", tmpTable);

    stmt = con.createStatement();

    String currency;
    
    rs = stmt.executeQuery("SELECT InvoiceCode, Date, TotalTotal, Currency FROM invoice WHERE CompanyCode = '" + customerCode + "' AND Status != 'C' AND Date >= {d '" + dateFrom + "'} AND Date <= {d '" + dateTo + "'}");

    while(rs.next())
    {    
      currency = rs.getString(4);
      
      addToTmpTable(con, stmt2, rs.getString(2), rs.getString(1), "I", "", rs.getString(3), rs.getString(2), tmpTable);

      if(currency.length() > 0 && ! currency.equals(customerCurrency))
        currencyMismatch[0] = true;
    }

    if(stmt != null) stmt.close();
    
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT DNCode, Date, TotalTotal, Currency FROM debit WHERE CompanyCode = '" + customerCode + "' AND Status != 'C' AND Date >= {d '" + dateFrom + "'} AND Date <= {d '" + dateTo + "'}");

    while(rs.next())
    {    
      currency = rs.getString(4);

      addToTmpTable(con, stmt2, rs.getString(2), rs.getString(1), "D", "", rs.getString(3), rs.getString(2), tmpTable);

      if(currency.length() > 0 && ! currency.equals(customerCurrency))
        currencyMismatch[0] = true;
    }

    if(stmt != null) stmt.close();

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t1.DateReceived, t2.ReceiptCode, t2.InvoiceCode, t2.AmountReceived, t2.InvoiceDate, t1.Currency, t1.ChequeNumber FROM receiptl AS t2 INNER JOIN receipt AS t1 ON t1.ReceiptCode = t2.ReceiptCode " 
                         + "WHERE t1.CompanyCode = '" + customerCode + "' AND t1.Status != 'C' AND t1.DateReceived >= {d '" + dateFrom + "'} AND DateReceived <= {d '" + dateTo + "'}");

    while(rs.next())
    {    
      currency = rs.getString(6);

      addToTmpTable(con, stmt2, rs.getString(1), (rs.getString(2) + " (" + rs.getString(7) + ")"), "R", rs.getString(3), rs.getString(4), rs.getString(5), tmpTable);

      if(currency.length() > 0 && ! currency.equals(customerCurrency))
        currencyMismatch[0] = true;
    }

    if(stmt != null) stmt.close();

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.Amount2, t2.GSTRate, t2.CNCode, t2.InvoiceCode, t1.Date, t1.Currency FROM creditl AS t2 INNER JOIN credit AS t1 ON t1.CNCode = t2.CNCode WHERE CompanyCode = '" + customerCode
                         + "' AND t1.Status != 'C' AND " + "t1.Date >= {d '" + dateFrom + "'} AND t1.Date <= {d '" + dateTo + "'}");

    String gstRate, invoiceDate;
    double amount, gst;

    while(rs.next())
    {    
      amount   = generalUtils.doubleFromStr(generalUtils.doubleDPs(rs.getString(1), '2'));
      gstRate  = rs.getString(2);
      currency = rs.getString(6);

      if(gstRate.length() > 0)
      {
        gst = amount * accountsUtils.getGSTRate(con, stmt2, rs2, gstRate);
        amount += gst;
      }

      invoiceDate = getInvoiceDateForSalesCreditNote(con, stmt2, rs2, rs.getString(4)); 

      addToTmpTable(con, stmt2, rs.getString(5), rs.getString(3), "C", rs.getString(4), generalUtils.doubleToStr(amount), invoiceDate, tmpTable);
      
      if(currency.length() > 0 && ! currency.equals(customerCurrency))
        currencyMismatch[0] = true;
    }
    
    if(stmt != null) stmt.close();
    
    processTmpTable(con, stmt, rs, out, tmpTable, balance, dateLess30, dateLess60, dateLess90, upto30, upto60, upto90, over, earliestDate, isGreen, bytesOut);

    if(currencyMismatch[0])
      writeRF2(out, customerCurrency, bytesOut);
    
    writeRF(out, upto30[0], upto60[0], upto90[0], over[0], bytesOut);
    
    customer.updatedDateInvoicePaid(con, stmt, rs, generalUtils.decodeToYYYYMMDD(earliestDate[0]), customerCode, dnm, localDefnsDir, defnsDir);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String getInvoiceDateForSalesCreditNote(Connection con, Statement stmt, ResultSet rs, String invoiceCode) throws Exception 
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT Date FROM invoice WHERE InvoiceCode = '" + invoiceCode + "'");

    String date = "1970-01-01";
    
    if(rs.next())
      date = rs.getString(1);

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return date;
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int processTmpTable(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String tmpTable, double[] balance, int dateLess30, int dateLess60, int dateLess90, double[] upto30, double[] upto60, double[] upto90, double[] over,
                              int[] earliestDate, boolean[] isGreen, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT DocumentDate, DocumentCode, DocumentType, Reference, Amount, EffectiveDate FROM " + tmpTable + " ORDER BY DocumentDate, DocumentCode");

    String docDate, docCode, docType, reference, amount, effectiveDate, lastDocCode = "";
    int thisDate, count=0;
    double thisAmount;
    boolean first = true;
  
    while(rs.next())
    {    
      docDate       = rs.getString(1);
      docCode       = rs.getString(2);
      docType       = rs.getString(3);
      reference     = rs.getString(4);
      amount        = rs.getString(5);
      effectiveDate = rs.getString(6);
      
      thisDate = generalUtils.encodeFromYYYYMMDD(effectiveDate);

      if(first)
      {
        if(thisDate < earliestDate[0])
          earliestDate[0] = thisDate;      
        first = false;
      }  

      if(! docCode.equals(lastDocCode))
      {
        thisAmount = writeBL1(out, docDate, docType, docCode, reference, amount, balance, isGreen, bytesOut);
        lastDocCode = docCode;
      }
      else
      {
        if(! docType.equals("R"))
          reference = "";
        thisAmount = writeBL1(out, docDate, docType, "", reference, amount, balance, isGreen, bytesOut);
      }
      
      if(thisDate > dateLess30)
        upto30[0] += thisAmount;
      else
      if(thisDate > dateLess60)
        upto60[0] += thisAmount;
      else
      if(thisDate > dateLess90)
        upto90[0] += thisAmount;
      else over[0] += thisAmount;
      
      ++count;
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return count;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private double openBalance(Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, ResultSet rs3, String customerCode, boolean[] currencyMismatch, String dateFrom, String liveDate, String customerCurrency,
                             int dateLess30, int dateLess60, int dateLess90, double[] upto30, double[] upto60, double[] upto90, double[] over, int[] earliestDate) throws Exception
  {
    double total = 0.0;      
    double[] outstanding = new double[1];  outstanding[0] = 0.0;
    String invoiceCode, thisCurrency, dnCode;
    int thisDate;
    
    stmt = con.createStatement();

    // invoices

    rs = stmt.executeQuery("SELECT InvoiceCode, Date, TotalTotal, Currency FROM invoice WHERE CompanyCode = '" + customerCode + "' AND Status != 'C' AND Settled != 'Y' AND Date >= {d '" + liveDate + "'} AND Date < {d '" + dateFrom + "'}");

    while(rs.next())
    {
      invoiceCode    = rs.getString(1);
      thisDate       = generalUtils.encodeFromYYYYMMDD(rs.getString(2));
      outstanding[0] = generalUtils.doubleFromStr(generalUtils.doubleDPs(rs.getString(3), '2'));
      thisCurrency   = rs.getString(4);
   
      detReceiptLines(con, stmt2, rs2, invoiceCode, dateFrom, outstanding);
      detSalesCreditNoteLines(con, stmt2, stmt3, rs2, rs3, invoiceCode, dateFrom, outstanding);
      
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

    rs = stmt.executeQuery("SELECT DNCode, Date, TotalTotal, Currency FROM debit WHERE CompanyCode = '" + customerCode + "' AND Status != 'C' AND Settled != 'Y' AND Date >= {d '" + liveDate + "'} AND Date < {d '" + dateFrom + "'}");

    while(rs.next())
    {
      dnCode         = rs.getString(1);
      thisDate       = generalUtils.encodeFromYYYYMMDD(rs.getString(2));
      outstanding[0] = generalUtils.doubleFromStr(generalUtils.doubleDPs(rs.getString(3), '2'));
      thisCurrency   = rs.getString(4);

      detReceiptLines(con, stmt2, rs2, dnCode, dateFrom, outstanding);
      detSalesCreditNoteLines(con, stmt2, stmt3, rs2, rs3, dnCode, dateFrom, outstanding);
      
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

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void detReceiptLines(Connection con, Statement stmt, ResultSet rs, String invoiceCode, String dateFrom, double[] outstanding) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.AmountReceived FROM receiptl AS t2 INNER JOIN receipt AS t1 ON t1.ReceiptCode = t2.ReceiptCode WHERE t2.InvoiceCode = '" + invoiceCode + "' AND t1.Status != 'C' AND t1.DateReceived < {d '" + dateFrom + "'}");

    double amount;

    while(rs.next())
    {    
      amount = generalUtils.doubleFromStr(rs.getString(1));

      outstanding[0] -= generalUtils.doubleDPs(amount, '2');
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void detSalesCreditNoteLines(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String invoiceCode, String dateFrom, double[] outstanding) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.Amount2, t2.GSTRate FROM creditl AS t2 INNER JOIN credit AS t1 ON t1.CNCode = t2.CNCode WHERE t2.InvoiceCode = '" + invoiceCode + "' AND t1.Status != 'C' AND t1.Date < {d '" + dateFrom + "'}");

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

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void addToTmpTable(Connection con, Statement stmt, String docDate, String docCode, String docType, String reference, String amount, String effectiveDate, String tmpTable) throws Exception
  {
    stmt = con.createStatement();
   
    String q = "INSERT INTO " + tmpTable + " ( DocumentDate, DocumentCode, DocumentType, Reference, Amount, EffectiveDate ) VALUES ({d '" + docDate + "'}, '" + docCode + "', '" + docType + "', '" + reference + "', '" + amount + "', {d '"
             + effectiveDate + "'} )";

    stmt.executeUpdate(q);
  
    if(stmt != null) stmt.close();
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private double writeBL1(PrintWriter out, String date, String type, String code, String reference, String amount, double[] balance, boolean[] isGreen, int[] bytesOut) throws Exception
  {
    char drCr = ' ';
    switch(type.charAt(0))
    {
      case 'I' : drCr = 'D'; type = "Invoice";         break;
      case 'D' : drCr = 'D'; type = "Debit Note";      break;
      case 'R' : drCr = 'C'; type = "Receipt";         break;
      case 'C' : drCr = 'C'; type = "Credit Note";     break;
      case 'O' : drCr = ' '; type = "Opening Balance"; break;
    }
    
    if(! type.equals("Balance b/f") && code.length() == 0) // a 'multiple' line
      type = "";
    
    String drAmount, crAmount;
    
    switch(drCr)
    {
      case 'D' : drAmount = generalUtils.doubleDPs(amount, '2');
                 crAmount = "";
                 balance[0] += generalUtils.doubleFromStr(generalUtils.doubleDPs(amount, '2'));
                 break;
      case 'C' : drAmount = "";
                 crAmount = generalUtils.doubleDPs(amount, '2');
                 balance[0] -= generalUtils.doubleFromStr(generalUtils.doubleDPs(amount, '2'));
                 break;
      default  : drAmount = crAmount = "";
                 break;
    }
    
    String bgColor;
    if(isGreen[0]) { bgColor = "#FFFFFF"; isGreen[0] = false; } else { bgColor = "#DDF0DD"; isGreen[0] = true; }

    scoutln(out, bytesOut, "<tr bgcolor=" + bgColor + "><td bgcolor=" + bgColor + "><p>" + generalUtils.convertFromYYYYMMDD(date) +"</td>");
    scoutln(out, bytesOut, "<td><p>" + type + "</td>");
    scoutln(out, bytesOut, "<td><p>" + code + "</td>");
    scoutln(out, bytesOut, "<td><p>" + reference + "</td>");
    scoutln(out, bytesOut, "<td align=right><p>" + drAmount + "</td>");
    scoutln(out, bytesOut, "<td align=right><p>" + crAmount + "</td>");
    scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.doubleDPs('2', balance[0]) + "</td></tr>");

    double amt = generalUtils.doubleFromStr(generalUtils.doubleDPs(amount, '2'));
    if(drCr == 'C')
     amt *= -1;
    return generalUtils.doubleDPs(amt, '2');
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void writeRF(PrintWriter out, double upto30, double upto60, double upto90, double over, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr></table><table id=\"page\" border=0 cellpadding=2>");

    scoutln(out, bytesOut, "<tr bgcolor=\"#F0F0F0\"><td><p>Current: " + generalUtils.doubleDPs('2', upto30) + "&nbsp;&nbsp;&nbsp;</td>");
    scoutln(out, bytesOut, "<td><p>Upto 60 Days: " + generalUtils.doubleDPs('2', upto60) + "&nbsp;&nbsp;&nbsp;</td>");
    scoutln(out, bytesOut, "<td><p>Upto 90 Days: " + generalUtils.doubleDPs('2', upto90) + "&nbsp;&nbsp;&nbsp;</td>");
    scoutln(out, bytesOut, "<td bgcolor=\"#F0F0F0\"><p>Over 90 Days: " + generalUtils.doubleDPs('2', over) + "</td></tr>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void writeRF2(PrintWriter out, String customerCurrency, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<tr><td><p>");
    scoutln(out, bytesOut, "This statement contains documents not of currency " + customerCurrency + "</td></tr>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void outputPageFrame(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String mainDNM, String dnm, String bnm, String localDefnsDir, String defnsDir,
                              int[] bytesOut) throws Exception
  {
    String[] headerLogo       = new String[1];
    String[] headerLogoRepeat = new String[1];
    String[] usesFlash        = new String[1];
    String[] footerText       = new String[1];
    String[] pageHeaderImage1 = new String[1];
    String[] pageHeaderImage2 = new String[1];
    String[] pageHeaderImage3 = new String[1];
    String[] pageHeaderImage4 = new String[1];
    String[] pageHeaderImage5 = new String[1];
    String[] watermark        = new String[1];
    
    wiki.getStyling(dnm, headerLogo, headerLogoRepeat, usesFlash, footerText, pageHeaderImage1, pageHeaderImage2, pageHeaderImage3, pageHeaderImage4, pageHeaderImage5, watermark);

    String imageLibraryDir  = directoryUtils.getImagesDir(dnm);

    scoutln(out, bytesOut, "</head><body id=\"generalPageFrame\">");
    scoutln(out, bytesOut, "<p><img src=\"" + imageLibraryDir + headerLogo[0] + "\" class=\"logo\" alt=\"logo\" /><p>\n");
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }
  
}
