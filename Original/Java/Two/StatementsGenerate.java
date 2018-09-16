// =======================================================================================================================================================================================================
// System: ZaraStar Analytic: Generate statements report  
// Module: StatementsGenerate.java
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
import java.sql.*;

public class StatementsGenerate extends HttpServlet
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

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="", p3="", p4="", p5="";
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
      p3  = req.getParameter("p3");  // customer
      p4  = req.getParameter("p4");  // option (30, 60, 90, ...)
      p5  = req.getParameter("p5");  // order: N or C (Name or Code)

      if(p1  == null) p1  = "";
      if(p2  == null) p2  = "";
      if(p3  == null) p3  = "";
      if(p4  == null) p4  = "1";

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, p3, p4, p5, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "StatementsGenerate", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1012, bytesOut[0], 0, "ERR:" + p3);
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm,
                    String bnm, String p1, String p2, String p3, String p4, String p5, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');
    String reportsDir       = directoryUtils.getUserDir('R', dnm, unm);
    String workingDir       = directoryUtils.getUserDir('W', dnm, unm);

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
    Statement stmt4 = null;
    ResultSet rs4 = null;
    
    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 1012, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "1012b", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1012, bytesOut[0], 0, "ACC:" + p3);
      if(con  != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "1012b", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1012, bytesOut[0], 0, "SID:" + p3);
      if(con  != null) con.close();
      if(out != null) out.flush(); 
      return;
    }
    
    String liveDate = generalUtils.convertDateToSQLFormat(definitionTables.getAppConfigApplicationStartDate(con, stmt, rs, dnm));
       
    if(p1.length() == 0 || p2.length() == 0)
      messagePage.msgScreen(false, out, req, 18, unm, sid, uty, men, den, dnm, bnm, "1004", "", "", "", imagesDir, localDefnsDir, defnsDir, bytesOut);
    else
    {
      short rtn=0;
      if(p3.length() > 0) // one customer
        rtn = r088(con, stmt, stmt2, stmt3, stmt4, rs, rs2, rs3, rs4, true, p3, p1, p2, liveDate, unm, dnm, bnm, p4, p5, reportsDir, workingDir, localDefnsDir, defnsDir);
      else rtn = r088(con, stmt, stmt2, stmt3, stmt4, rs, rs3, rs2, rs4, false, p3, p1, p2, liveDate, unm, dnm, bnm, p4, p5, reportsDir, workingDir, localDefnsDir, defnsDir);

      switch(rtn)
      {
        case -1 : // Definition File Not Found
                  messagePage.msgScreen(false, out, req, 17, unm, sid, uty, men, den, dnm, bnm, "1012", "", "", "", imagesDir, localDefnsDir, defnsDir, bytesOut);
                  break;
        case -2 : // cannot create report output file
                  messagePage.msgScreen(false, out, req, 18, unm, sid, uty, men, den, dnm, bnm, "1012", "", "", "", imagesDir, localDefnsDir, defnsDir, bytesOut);
                  break;
        default : // generated ok
                  messagePage.msgScreen(false, out, req, 16, unm, sid, uty, men, den, dnm, bnm, "1012", "", "", "", imagesDir, localDefnsDir, defnsDir, bytesOut);
                  break;
      }
    }

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 1012, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p3);
    if(con  != null) con.close();
    if(out != null) out.flush(); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private short r088(Connection con, Statement stmt, Statement stmt2, Statement stmt3, Statement stmt4, ResultSet rs, ResultSet rs2, ResultSet rs3, ResultSet rs4, boolean oneCustomer, String customerCode, String dateFrom, String dateTo,
                     String liveDate, String unm, String dnm, String bnm, String option, String order, String reportsDir, String workingDir, String localDefnsDir, String defnsDir) throws Exception
  {
    reportGeneration.currFont = 1;
    reportGeneration.currPage = 0;
    reportGeneration.currDown = reportGeneration.currAcross = 0.0;

    reportGeneration.oBufLen = 30000;
    reportGeneration.oBuf = new byte[30000];
    reportGeneration.oPtr = 0;

    String[] newName = new String[1];
    if((reportGeneration.fhO = reportGeneration.createNewFile((short)88, workingDir, localDefnsDir, defnsDir, reportsDir, newName)) == null)
      return -2;

    if((reportGeneration.fhPPR = generalUtils.fileOpenD("088.ppr", localDefnsDir)) == null)
    {
      if((reportGeneration.fhPPR = generalUtils.fileOpenD("088.ppr", defnsDir)) == null)
        return -1;
    }

    reportGeneration.lastOperationPF = false;

    byte[] msg = new byte[30];
    generalUtils.strToBytes(msg, "T:" + order);
    reportGeneration.outputLine('T', "", "", msg, null, null, (short)0, ' ', dnm, unm, localDefnsDir, defnsDir);

    reportGeneration.processControl(dnm, unm, localDefnsDir, defnsDir);

    String dateFromText;
    if(dateFrom.length() == 0)
    {
      dateFromText = "Start";
      dateFrom = "1970-01-01";
    }
    else
    {
      dateFromText = dateFrom;
      dateFrom = generalUtils.convertDateToSQLFormat(dateFrom);
    }

    String dateToText, today = generalUtils.todaySQLFormat(localDefnsDir, defnsDir);
    if(dateTo.length() == 0) // take today
    {
      dateToText = generalUtils.today(localDefnsDir, defnsDir);
      dateTo = today;
    }   
    else 
    {
      dateToText = dateTo;
      dateTo = generalUtils.convertDateToSQLFormat(dateTo);
    }

    byte[] fldNames = new byte[10000];
    byte[] fldData  = new byte[10000];

    generalUtils.putAlpha(fldNames, 10000, (short)0,  "Head.StatementDate");
    generalUtils.putAlpha(fldNames, 10000, (short)1,  "Head.DateFrom");
    generalUtils.putAlpha(fldNames, 10000, (short)2,  "Head.DateTo");
    generalUtils.putAlpha(fldNames, 10000, (short)3,  "Head.CompanyCode");
    generalUtils.putAlpha(fldNames, 10000, (short)4,  "Head.CompanyName");
    generalUtils.putAlpha(fldNames, 10000, (short)5,  "Head.Address1");
    generalUtils.putAlpha(fldNames, 10000, (short)6,  "Head.Address2");
    generalUtils.putAlpha(fldNames, 10000, (short)7,  "Head.Address3");
    generalUtils.putAlpha(fldNames, 10000, (short)8,  "Head.Address4");
    generalUtils.putAlpha(fldNames, 10000, (short)9,  "Head.Address5");
    generalUtils.putAlpha(fldNames, 10000, (short)10, "Head.PostCode");
    generalUtils.putAlpha(fldNames, 10000, (short)11, "Line.Date");
    generalUtils.putAlpha(fldNames, 10000, (short)12, "Line.Type");
    generalUtils.putAlpha(fldNames, 10000, (short)13, "Line.Code");
    generalUtils.putAlpha(fldNames, 10000, (short)14, "Line.Reference");
    generalUtils.putAlpha(fldNames, 10000, (short)15, "Line.DrAmount");
    generalUtils.putAlpha(fldNames, 10000, (short)16, "Line.CrAmount");
    generalUtils.putAlpha(fldNames, 10000, (short)17, "Line.Balance");
    generalUtils.putAlpha(fldNames, 10000, (short)18, "Head.Phone1");
    generalUtils.putAlpha(fldNames, 10000, (short)19, "Head.Fax");
    generalUtils.putAlpha(fldNames, 10000, (short)20, "Head.Currency");
    generalUtils.putAlpha(fldNames, 10000, (short)21, "Summary.1");
    generalUtils.putAlpha(fldNames, 10000, (short)22, "Summary.2");
    generalUtils.putAlpha(fldNames, 10000, (short)23, "Summary.3");
    generalUtils.putAlpha(fldNames, 10000, (short)24, "Summary.4");
    generalUtils.putAlpha(fldNames, 10000, (short)25, "Summary.5");
    generalUtils.putAlpha(fldNames, 10000, (short)26, "Summary.6");
    generalUtils.putAlpha(fldNames, 10000, (short)27, "Summary.7");
    generalUtils.putAlpha(fldNames, 10000, (short)28, "Summary.8");
    generalUtils.putAlpha(fldNames, 10000, (short)29, "Summary.9");
    generalUtils.putAlpha(fldNames, 10000, (short)30, "Summary.10");
    generalUtils.putAlpha(fldNames, 10000, (short)31, "Summary.11");
    generalUtils.putAlpha(fldNames, 10000, (short)32, "Summary.12");
    generalUtils.putAlpha(fldNames, 10000, (short)33, "Summary.Over");
    generalUtils.putAlpha(fldNames, 10000, (short)34, "Head.SalesPerson");
    generalUtils.putAlpha(fldNames, 10000, (short)35, "Head.CreditDays");

    generalUtils.repAlpha(fldData, 10000, (short)0, generalUtils.convertFromYYYYMMDD(today));
    generalUtils.repAlpha(fldData, 10000, (short)1, dateFromText);
    generalUtils.repAlpha(fldData, 10000, (short)2, dateToText);

    String s;
    if(order.equals("C"))
      s = "CompanyCode";
    else s = "Name";
      
    if(oneCustomer)
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT Name FROM company WHERE CompanyCode = '" + customerCode + "' ORDER BY " + s);

      String companyName;

      if(rs.next())
      {    
        companyName = rs.getString(1);
      
        generate(con, stmt2, stmt3, stmt4, rs2, rs3, rs4, option, order, customerCode, companyName, dateFrom, dateTo, liveDate, fldData, fldNames, unm, dnm, bnm, localDefnsDir, defnsDir);
      }

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
    else
    {
      // for all customers
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT CompanyCode, Name FROM company WHERE NoStatements != 'Y' ORDER BY " + s);

      String companyCode, companyName;

      while(rs.next())
      {    
        companyCode = rs.getString(1);
        companyName = rs.getString(2);

        generate(con, stmt2, stmt3, stmt4, rs2, rs3, rs4, option, order, companyCode, companyName, dateFrom, dateTo, liveDate, fldData, fldNames, unm, dnm, bnm, localDefnsDir, defnsDir);

        reportGeneration.outputLine('F', null, fldData, fldNames, (short)36, ' ', dnm, unm, localDefnsDir, defnsDir);
      }

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }

    reportGeneration.fhO.write(reportGeneration.oBuf, 0, reportGeneration.oPtr);

    reportGeneration.fhO.close();
    reportGeneration.fhPPR.close();

    return 0;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void generate(Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, ResultSet rs3, String option, String order, String customerCode, String customerName, String dateFrom, String dateTo,
                        String liveDate, byte[] fldData, byte[] fldNames, String unm, String dnm, String bnm, String localDefnsDir, String defnsDir) throws Exception
  {
    System.out.print(" " + customerCode);

    byte[] msg = new byte[100];
    if(order.equals("C"))
      generalUtils.strToBytes(msg, "M:" + customerCode);
    else generalUtils.strToBytes(msg, "M:" + customerName);
    reportGeneration.outputLine('M', "", "", msg, fldData, fldNames, (short)36, ' ', dnm, unm, localDefnsDir, defnsDir);

    int dateLess30  = generalUtils.encodeFromYYYYMMDD(dateTo) - 30;  
    int dateLess60  = dateLess30 - 30;  
    int dateLess90  = dateLess30 - 60;  
    int dateLess120 = dateLess30 - 90;  
    int dateLess150 = dateLess30 - 120;  
    int dateLess180 = dateLess30 - 150;  
    int dateLess210 = dateLess30 - 180;  
    int dateLess240 = dateLess30 - 210;  
    int dateLess270 = dateLess30 - 240;  
    int dateLess300 = dateLess30 - 270;  
    int dateLess330 = dateLess30 - 300;  
    int dateLess360 = dateLess30 - 330;  
    
    byte[] data = new byte[3000];
    byte[] b    = new byte[100];

    generalUtils.repAlpha(fldData, 10000, (short)3, customerCode);
    String customerCurrency;

    if(customer.getCompanyRecGivenCode(con, stmt, rs, customerCode, '\000', dnm, data, localDefnsDir, defnsDir) != -1) // just-in-case
    {
      generalUtils.dfs(data, (short)1, b);           // name
      generalUtils.repAlpha(fldData, 10000, (short)4, b);
      generalUtils.dfs(data, (short)5, b);           // addr1
      generalUtils.repAlpha(fldData, 10000, (short)5, b);
      generalUtils.dfs(data, (short)6, b);           // addr2
      generalUtils.repAlpha(fldData, 10000, (short)6, b);
      generalUtils.dfs(data, (short)7, b);           // addr3
      generalUtils.repAlpha(fldData, 10000, (short)7, b);
      generalUtils.dfs(data, (short)8, b);           // addr4
      generalUtils.repAlpha(fldData, 10000, (short)8, b);
      generalUtils.dfs(data, (short)9, b);           // addr5
      generalUtils.repAlpha(fldData, 10000, (short)9, b);
      generalUtils.dfs(data, (short)10, b);          // pc
      generalUtils.repAlpha(fldData, 10000, (short)10, b);
      generalUtils.dfs(data, (short)13, b);          // phone1
      generalUtils.repAlpha(fldData, 10000, (short)18, b);
      generalUtils.dfs(data, (short)15, b);          // fax
      generalUtils.repAlpha(fldData, 10000, (short)19, b);
      customerCurrency = generalUtils.dfsAsStr(data, (short)38);
      generalUtils.repAlpha(fldData, 10000, (short)20, customerCurrency);
      generalUtils.dfs(data, (short)17, b);          // salesperson
      generalUtils.repAlpha(fldData, 10000, (short)34, b);
      generalUtils.dfs(data, (short)37, b);          // creditdays
      generalUtils.repAlpha(fldData, 10000, (short)35, b);
    }
    else return;

    reportGeneration.currPage = 1;

    reportGeneration.processSection("PH", fldData, fldNames, (short)36, dnm, unm, localDefnsDir, defnsDir);

    reportGeneration.processSection("BL2", null, null, (short)0, dnm, unm, localDefnsDir, defnsDir); // posn the lines cursor
    
    boolean[] currencyMismatch = new boolean[1]; currencyMismatch[0] = false;

    int[] lineCount = new int[1]; lineCount[0] = 1;
    int[] transactionsCount = new int[1]; transactionsCount[0] = 0;

    double[] upto30  = new double[1]; upto30[0]  = 0.0;
    double[] upto60  = new double[1]; upto60[0]  = 0.0;
    double[] upto90  = new double[1]; upto90[0]  = 0.0;
    double[] upto120 = new double[1]; upto120[0] = 0.0;
    double[] upto150 = new double[1]; upto150[0] = 0.0;
    double[] upto180 = new double[1]; upto180[0] = 0.0;
    double[] upto210 = new double[1]; upto210[0] = 0.0;
    double[] upto240 = new double[1]; upto240[0] = 0.0;
    double[] upto270 = new double[1]; upto270[0] = 0.0;
    double[] upto300 = new double[1]; upto300[0] = 0.0;
    double[] upto330 = new double[1]; upto330[0] = 0.0;
    double[] upto360 = new double[1]; upto360[0] = 0.0;
    double[] over    = new double[1]; over[0]    = 0.0;

    int[] earliestDate = new int[1];  earliestDate[0] = generalUtils.encodeFromYYYYMMDD("2099-12-31"); 
    double[] balance = new double[1];
    balance[0] = openBalance(con, stmt, stmt2, stmt3, rs, rs2, rs3, customerCode, currencyMismatch, dateFrom, dateTo, liveDate, customerCurrency, dateLess30, dateLess60, dateLess90, dateLess120, dateLess150, dateLess180, dateLess210, dateLess240,
                             dateLess270, dateLess300, dateLess330, dateLess360, upto30, upto60, upto90, upto120, upto150, upto180, upto210, upto240, upto270, upto300, upto330, upto360, over, earliestDate, dnm, localDefnsDir, defnsDir);
    
    writeLine(fldData, "O", "", "", "", "", balance);
    reportGeneration.processSection("BL1", fldData, fldNames, (short)36, dnm, unm, localDefnsDir, defnsDir);

    String tmpTable = unm + "_tmp";
      
    directoryUtils.createTmpTable(true, con, stmt, "DocumentDate date, DocumentCode char(20), DocumentType char(1), Reference char(20), Amount decimal(19,8), EffectiveDate date", "", tmpTable);

    stmt = con.createStatement();

    String currency;
    
    rs = stmt.executeQuery("SELECT InvoiceCode, Date, TotalTotal, Currency FROM invoice WHERE CompanyCode = '" + customerCode + "' AND Status != 'C' AND Date >= {d '" + dateFrom + "'} AND Date <= {d '" + dateTo + "'}");

    while(rs.next())
    {    
      currency = rs.getString(4);
      
      addToTmpTable(con, stmt, rs.getString(2), rs.getString(1), "I", "", rs.getString(3), rs.getString(2), tmpTable);

      if(currency.length() > 0 && ! currency.equals(customerCurrency))
        currencyMismatch[0] = true;
    }

    if(stmt != null) stmt.close();
    
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT DNCode, Date, TotalTotal, Currency FROM debit WHERE CompanyCode = '" + customerCode + "' AND Status != 'C' AND Date >= {d '" + dateFrom + "'} AND Date <= {d '" + dateTo + "'}");

    while(rs.next())
    {    
      currency = rs.getString(4);

      addToTmpTable(con, stmt, rs.getString(2), rs.getString(1), "D", "", rs.getString(3), rs.getString(2), tmpTable);

      if(currency.length() > 0 && ! currency.equals(customerCurrency))
        currencyMismatch[0] = true;
    }

    if(stmt != null) stmt.close();

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t1.DateReceived, t2.ReceiptCode, t2.InvoiceCode, t2.AmountReceived, t2.InvoiceDate, t1.Currency FROM receiptl AS t2 INNER JOIN receipt AS t1 ON t1.ReceiptCode = t2.ReceiptCode WHERE t1.CompanyCode = '"
                         + customerCode + "' AND t1.Status != 'C' AND t1.DateReceived >= {d '" + dateFrom + "'} AND DateReceived <= {d '" + dateTo + "'}");

    while(rs.next())
    {    
      currency = rs.getString(6);

      addToTmpTable(con, stmt, rs.getString(1), rs.getString(2), "R", rs.getString(3), rs.getString(4), rs.getString(5), tmpTable);

      if(currency.length() > 0 && ! currency.equals(customerCurrency))
        currencyMismatch[0] = true;
    }

    if(stmt != null) stmt.close();

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.Amount2, t2.GSTRate, t2.CNCode, t2.InvoiceCode, t1.Date, t1.Currency FROM creditl AS t2 INNER JOIN credit AS t1 ON t1.CNCode = t2.CNCode WHERE CompanyCode = '" + customerCode
                         + "' AND t1.Status != 'C' AND t1.Date >= {d '" + dateFrom + "'} AND t1.Date <= {d '" + dateTo + "'}");

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

      addToTmpTable(con, stmt, rs.getString(5), rs.getString(3), "C", rs.getString(4), generalUtils.doubleToStr(amount), invoiceDate, tmpTable);
      
      if(currency.length() > 0 && ! currency.equals(customerCurrency))
        currencyMismatch[0] = true;
    }
    
    if(stmt != null) stmt.close();
    
    int count = processTmpTable(con, stmt, stmt2, rs, rs2, tmpTable, order, customerCode, customerName, fldData, fldNames, balance, dateLess30, dateLess60, dateLess90, dateLess120, dateLess150, dateLess180, dateLess210, dateLess240, dateLess270,
                                dateLess300, dateLess330, dateLess360, upto30, upto60, upto90, upto120, upto150, upto180, upto210, upto240, upto270, upto300, upto330, upto360, over, earliestDate, unm, dnm, localDefnsDir, defnsDir);

    if(count == 0)
    {
      generalUtils.strToBytes(msg, "M:NOTRANS");    
      reportGeneration.outputLine('M', "", "", msg, fldData, fldNames, (short)36, ' ', dnm, unm, localDefnsDir, defnsDir);
    }
    
    if(currencyMismatch[0])
    {
      reportGeneration.processSection("RF3", fldData, fldNames, (short)36, dnm, unm, localDefnsDir, defnsDir);
      chkForPageThrow(con, stmt, rs, order, customerCode, customerName, fldNames, fldData, lineCount, dnm, unm, localDefnsDir, defnsDir);
    }
    
    writeSpread(option, upto30[0], upto60[0], upto90[0], upto120[0], upto150[0], upto180[0], upto210[0], upto240[0], upto270[0], upto300[0], upto330[0], upto360[0], over[0], fldNames, fldData, unm, dnm, localDefnsDir, defnsDir);
    
    if(balance[0] + upto30[0] + upto60[0] + upto90[0] + upto120[0] + upto150[0] + upto180[0] + upto210[0] + upto240[0] + upto270[0] + upto300[0] + upto330[0] + upto360[0] + over[0] == 0.0)
    {
      generalUtils.strToBytes(msg, "M:0BALANCE");    
      reportGeneration.outputLine('M', "", "", msg, fldData, fldNames, (short)36, ' ', dnm, unm, localDefnsDir, defnsDir);
    }
    
    customer.updatedDateInvoicePaid(con, stmt, rs, generalUtils.decodeToYYYYMMDD(earliestDate[0]), customerCode, dnm, localDefnsDir, defnsDir);
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
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
  private int processTmpTable(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String tmpTable, String order, String customerCode, String customerName, byte[] fldData, byte[] fldNames, double[] balance,
                              int dateLess30, int dateLess60, int dateLess90, int dateLess120, int dateLess150, int dateLess180, int dateLess210, int dateLess240, int dateLess270, int dateLess300, int dateLess330, int dateLess360,
                              double[] upto30, double[] upto60, double[] upto90, double[] upto120, double[] upto150, double[] upto180, double[] upto210, double[] upto240, double[] upto270, double[] upto300, double[] upto330, double[] upto360,
                              double[] over, int[] earliestDate, String unm, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT DocumentDate, DocumentCode, DocumentType, Reference, Amount, EffectiveDate FROM " + tmpTable + " ORDER BY DocumentDate, DocumentCode");

    String docDate, docCode, docType, reference, amount, effectiveDate, lastDocCode = "";
    int thisDate, count=0;
    double thisAmount;
    boolean first = true;
    int[] lineCount = new int[1];  lineCount[0] = 0;

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
        thisAmount = writeLine(fldData, docType, docCode, docDate, amount, reference, balance);
        lastDocCode = docCode;
      }
      else thisAmount = writeLine(fldData, docType, "", "", amount, reference, balance);
      
      reportGeneration.processSection("BL1", fldData, fldNames, (short)25, dnm, unm, localDefnsDir, defnsDir);

      chkForPageThrow(con, stmt2, rs2, order, customerCode, customerName, fldNames, fldData, lineCount, dnm, unm, localDefnsDir, defnsDir);

      if(thisDate > dateLess30)
        upto30[0] += thisAmount;
      else
      if(thisDate > dateLess60)
        upto60[0] += thisAmount;
      else
      if(thisDate > dateLess90)
        upto90[0] += thisAmount;
      else
      if(thisDate > dateLess120)
       upto120[0] += thisAmount;
       else
      if(thisDate > dateLess150)
        upto150[0] += thisAmount;
      else
      if(thisDate > dateLess180)
        upto180[0] += thisAmount;
      else
      if(thisDate > dateLess210)
        upto210[0] += thisAmount;
      else
      if(thisDate > dateLess240)
        upto240[0] += thisAmount;
      else
      if(thisDate > dateLess270)
        upto270[0] += thisAmount;
      else
      if(thisDate > dateLess300)
        upto300[0] += thisAmount;
      else
      if(thisDate > dateLess330)
        upto330[0] += thisAmount;
      else
      if(thisDate > dateLess360)
        upto360[0] += thisAmount;
      else over[0] += thisAmount;
      
      ++count;
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return count;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private double writeLine(byte[] fldData, String type, String code, String date, String amount, String reference, double[] balance) throws Exception
  {
    char drCr = ' ';
    switch(type.charAt(0))
    {
      case 'I' : drCr = 'D'; type = "Invoice";     break;
      case 'D' : drCr = 'D'; type = "Debit Note";  break;
      case 'R' : drCr = 'C'; type = "Receipt";     break;
      case 'C' : drCr = 'C'; type = "Credit Note"; break;
      case 'O' : drCr = ' '; type = "Balance b/f"; break;
    }
    
    if(! type.equals("Balance b/f") && code.length() == 0) // a 'multiple' line
      type = "";
    
    generalUtils.repAlpha(fldData, 10000, (short)11, generalUtils.convertFromYYYYMMDD(date));
    generalUtils.repAlpha(fldData, 10000, (short)12, type);
    generalUtils.repAlpha(fldData, 10000, (short)13, code);
    generalUtils.repAlpha(fldData, 10000, (short)14, reference);

    switch(drCr)
    {
      case 'D' : generalUtils.repAlpha(fldData, 10000, (short)15, generalUtils.doubleDPs(amount, '2'));
                 generalUtils.repAlpha(fldData, 10000, (short)16, " ");
                 balance[0] += generalUtils.doubleFromStr(generalUtils.doubleDPs(amount, '2'));
                 break;
      case 'C' : generalUtils.repAlpha(fldData, 10000, (short)15, " ");
                 generalUtils.repAlpha(fldData, 10000, (short)16, generalUtils.doubleDPs(amount, '2'));
                 balance[0] -= generalUtils.doubleFromStr(generalUtils.doubleDPs(amount, '2'));
                 break;
      default  : generalUtils.repAlpha(fldData, 10000, (short)15, " ");
                 generalUtils.repAlpha(fldData, 10000, (short)16, " ");
                 break;
    }

    byte[] b = new byte[20];
    generalUtils.doubleToBytesCharFormat(balance[0], b, 0);
    generalUtils.formatNumeric(b, '2');
    generalUtils.repAlpha(fldData, 10000, (short)17, b);
    
    double amt = generalUtils.doubleFromStr(generalUtils.doubleDPs(amount, '2'));
    if(drCr == 'C')
     amt *= -1;
    return generalUtils.doubleDPs(amt, '2');
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void chkForPageThrow(Connection con, Statement stmt, ResultSet rs, String order, String customerCode, String customerName, byte[] fldNames, byte[] fldData, int[] lineCount, String dnm, String unm, String localDefnsDir, String defnsDir)
                               throws Exception
  {
    ++lineCount[0];

    if(lineCount[0] >= miscDefinitions.docSizeMax(con, stmt, rs, "soa"))
    {
      reportGeneration.outputLine('E', null, fldData, fldNames, (short)36, ' ', dnm, unm, localDefnsDir, defnsDir);

      byte[] msg = new byte[100];
      
    if(order.equals("C"))
      generalUtils.strToBytes(msg, "M:" + customerCode);
    else generalUtils.strToBytes(msg, "M:" + customerName);
      
      reportGeneration.outputLine('M', "", "", msg, fldData, fldNames, (short)36, ' ', dnm, unm, localDefnsDir, defnsDir);
      
      lineCount[0] = 1;
      reportGeneration.processSection("PH", fldData, fldNames, (short)36, dnm, unm, localDefnsDir, defnsDir);

      // posn the lines cursor
      reportGeneration.processSection("BL2", null, null, (short)0, dnm, unm, localDefnsDir, defnsDir);
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void writeSpread(String option, double upto1, double upto2, double upto3, double upto4, double upto5, double upto6, double upto7, double upto8, double upto9, double upto10, double upto11, double upto12, double over, byte[] fldNames,
                           byte[] fldData, String unm, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    byte[] b = new byte[20];

    if(option.charAt(0) == '1') // 30-60-90
      over += upto4 + upto5 + upto6 + upto7 + upto8 + upto9 + upto10 + upto11 + upto12;

    generalUtils.doubleToBytesCharFormat(upto1, b, 0);
    generalUtils.formatNumeric(b, '2');
    generalUtils.repAlpha(fldData, 10000, (short)21, b);

    generalUtils.doubleToBytesCharFormat(upto2, b, 0);
    generalUtils.formatNumeric(b, '2');
    generalUtils.repAlpha(fldData, 10000, (short)22, b);

    generalUtils.doubleToBytesCharFormat(upto3, b, 0);
    generalUtils.formatNumeric(b, '2');
    generalUtils.repAlpha(fldData, 10000, (short)23, b);

    generalUtils.doubleToBytesCharFormat(upto4, b, 0);
    generalUtils.formatNumeric(b, '2');
    generalUtils.repAlpha(fldData, 10000, (short)24, b);

    generalUtils.doubleToBytesCharFormat(upto5, b, 0);
    generalUtils.formatNumeric(b, '2');
    generalUtils.repAlpha(fldData, 10000, (short)25, b);

    generalUtils.doubleToBytesCharFormat(upto6, b, 0);
    generalUtils.formatNumeric(b, '2');
    generalUtils.repAlpha(fldData, 10000, (short)26, b);

    generalUtils.doubleToBytesCharFormat(upto7, b, 0);
    generalUtils.formatNumeric(b, '2');
    generalUtils.repAlpha(fldData, 10000, (short)27, b);

    generalUtils.doubleToBytesCharFormat(upto8, b, 0);
    generalUtils.formatNumeric(b, '2');
    generalUtils.repAlpha(fldData, 10000, (short)28, b);

    generalUtils.doubleToBytesCharFormat(upto9, b, 0);
    generalUtils.formatNumeric(b, '2');
    generalUtils.repAlpha(fldData, 10000, (short)29, b);

    generalUtils.doubleToBytesCharFormat(upto10, b, 0);
    generalUtils.formatNumeric(b, '2');
    generalUtils.repAlpha(fldData, 10000, (short)30, b);

    generalUtils.doubleToBytesCharFormat(upto11, b, 0);
    generalUtils.formatNumeric(b, '2');
    generalUtils.repAlpha(fldData, 10000, (short)31, b);

    generalUtils.doubleToBytesCharFormat(upto12, b, 0);
    generalUtils.formatNumeric(b, '2');
    generalUtils.repAlpha(fldData, 10000, (short)32, b);

    generalUtils.doubleToBytesCharFormat(over, b, 0);
    generalUtils.formatNumeric(b, '2');
    generalUtils.repAlpha(fldData, 10000, (short)33, b);

    if(option.charAt(0) == '1') // 30-60-90
      reportGeneration.processSection("RF1", fldData, fldNames, (short)36, dnm, unm, localDefnsDir, defnsDir);
    else reportGeneration.processSection("RF2", fldData, fldNames, (short)36, dnm, unm, localDefnsDir, defnsDir);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private double openBalance(Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, ResultSet rs3, String customerCode, boolean[] currencyMismatch, String dateFrom, String dateTo, String liveDate,
                             String customerCurrency, int dateLess30, int dateLess60, int dateLess90, int dateLess120, int dateLess150, int dateLess180, int dateLess210, int dateLess240, int dateLess270, int dateLess300, int dateLess330,
                             int dateLess360, double[] upto30, double[] upto60, double[] upto90, double[] upto120, double[] upto150, double[] upto180, double[] upto210, double[] upto240, double[] upto270, double[] upto300, double[] upto330,
                             double[] upto360, double[] over, int[] earliestDate, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    double total = 0.0;
    double[] outstanding = new double[1];  outstanding[0] = 0.0;
    String invoiceCode, thisCurrency, dnCode;
    int thisDate;
    
    stmt = con.createStatement();

    // invoices

    rs = stmt.executeQuery("SELECT InvoiceCode, Date, TotalTotal, Currency FROM invoice WHERE CompanyCode = '" + customerCode
            + "' AND Status != 'C' AND Settled != 'Y' AND Date >= {d '" + liveDate + "'} AND Date < {d '" + dateFrom + "'}");

    while(rs.next())
    {
      invoiceCode    = rs.getString(1);
      thisDate       = generalUtils.encodeFromYYYYMMDD(rs.getString(2));
      outstanding[0] = generalUtils.doubleFromStr(generalUtils.doubleDPs(rs.getString(3), '2'));
      thisCurrency   = rs.getString(4);

      detReceiptLines(con, stmt2, rs2, invoiceCode, dateFrom, outstanding);
      detSalesCreditNoteLines(con, stmt2, stmt3, rs2, rs3, invoiceCode, dateFrom, dnm, localDefnsDir, defnsDir, outstanding);
      
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
        else
        if(thisDate > dateLess120)
          upto120[0] += outstanding[0];
        else
        if(thisDate > dateLess150)
          upto150[0] += outstanding[0];
        else
        if(thisDate > dateLess180)
          upto180[0] += outstanding[0];
        else
        if(thisDate > dateLess210)
          upto210[0] += outstanding[0];
        else
        if(thisDate > dateLess240)
          upto240[0] += outstanding[0];
        else
        if(thisDate > dateLess270)
          upto270[0] += outstanding[0];
        else
        if(thisDate > dateLess300)
          upto300[0] += outstanding[0];
        else
        if(thisDate > dateLess330)
          upto330[0] += outstanding[0];
        else
        if(thisDate > dateLess360)
          upto360[0] += outstanding[0];
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
      detSalesCreditNoteLines(con, stmt2, stmt3, rs2, rs3, dnCode, dateFrom, dnm, localDefnsDir, defnsDir, outstanding);
      
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
        else
        if(thisDate > dateLess120)
          upto120[0] += outstanding[0];
        else
        if(thisDate > dateLess150)
          upto150[0] += outstanding[0];
        else
        if(thisDate > dateLess180)
          upto180[0] += outstanding[0];
        else
        if(thisDate > dateLess210)
          upto210[0] += outstanding[0];
        else
        if(thisDate > dateLess240)
          upto240[0] += outstanding[0];
        else
        if(thisDate > dateLess270)
          upto270[0] += outstanding[0];
        else
        if(thisDate > dateLess300)
          upto300[0] += outstanding[0];
        else
        if(thisDate > dateLess330)
          upto330[0] += outstanding[0];
        else
        if(thisDate > dateLess360)
          upto360[0] += outstanding[0];
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

    rs = stmt.executeQuery("SELECT t2.AmountReceived FROM receiptl AS t2 INNER JOIN receipt AS t1 ON t1.ReceiptCode = t2.ReceiptCode WHERE t2.InvoiceCode = '"
            + invoiceCode + "' AND t1.Status != 'C' AND t1.DateReceived < {d '" + dateFrom + "'}");

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
  private void detSalesCreditNoteLines(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String invoiceCode, String dateFrom, String dnm, String localDefnsDir, String defnsDir, double[] outstanding) throws Exception
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
   
    String q = "INSERT INTO " + tmpTable + " ( DocumentDate, DocumentCode, DocumentType, Reference, Amount, EffectiveDate ) "
             + "VALUES ({d '" + docDate + "'}, '" + docCode + "', '" + docType + "', '" + reference + "', '" + amount + "', {d '" + effectiveDate + "'} )";

    stmt.executeUpdate(q);
  
    if(stmt != null) stmt.close();
  }

}
