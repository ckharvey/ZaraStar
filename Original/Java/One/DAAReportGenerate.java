// =======================================================================================================================================================================================================
// System: ZaraStar Analytic: Generate DAA report  
// Module: DAAReportGenerate.java
// Author: C.K.Harvey
// Copyright (c) 1999-2008 Christopher Harvey. All Rights Reserved.
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

public class DAAReportGenerate extends HttpServlet
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

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="", p3="", p4="", p5="", p6="", p7="", p8="", p9="", p10="";

    try
    {
      res.setContentType("text/html");
      out = res.getWriter();

      unm = req.getParameter("unm");
      sid = req.getParameter("sid");
      uty = req.getParameter("uty");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");
      p1  = req.getParameter("p1"); // type (Summary or Detail)
      p2  = req.getParameter("p2"); // dateFrom
      p3  = req.getParameter("p3"); // dateTo
      p4  = req.getParameter("p4"); // ignore zero balances
      p5  = req.getParameter("p5"); // range1
      p6  = req.getParameter("p6"); // range2
      p7  = req.getParameter("p7"); // range3
      p8  = req.getParameter("p8"); // range4
      p9  = req.getParameter("p9"); // customer
      p10 = req.getParameter("p10"); // orderedBy

      if(p2 == null) p2 = "";
      if(p3 == null) p3 = "";
      
      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "MainPageUtils4b", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1012, bytesOut[0], 0, "ERR:" + p3);
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                    String p1, String p2, String p3, String p4, String p5, String p6, String p7, String p8, String p9, String p10, int[] bytesOut)
                    throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');
    String reportsDir    = directoryUtils.getUserDir('R', dnm, unm);
    String workingDir    = directoryUtils.getUserDir('W', dnm, unm);

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

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 1004, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "1004b", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1004, bytesOut[0], 0, "ACC:" + p3);
      if(con  != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "1004b", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1004, bytesOut[0], 0, "SID:" + p3);
     if(con  != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    char oneOrAll;
    if(p9.length() == 0 || p9.equalsIgnoreCase("null"))
      oneOrAll = 'A';
    else oneOrAll = 'O';

    switch(r065(con, stmt, stmt2, stmt3, stmt4, rs, rs2, rs3, rs4, oneOrAll, p1.charAt(0), p10.charAt(0), p9, p2, p3, p4, p5, p6, p7, p8, dnm, unm, reportsDir, localDefnsDir, defnsDir, workingDir))
    {
      case -1 : // Definition File Not Found
                messagePage.msgScreen(false, out, req, 17, unm, sid, uty, men, den, dnm, bnm, "1004", "", "", "", imagesDir, localDefnsDir, defnsDir, bytesOut);
                break;
      case -2 : // cannot create report output file
                messagePage.msgScreen(false, out, req, 18, unm, sid, uty, men, den, dnm, bnm, "1004", "", "", "", imagesDir, localDefnsDir, defnsDir, bytesOut);
                break;
      default : // generated ok
                messagePage.msgScreen(false, out, req, 16, unm, sid, uty, men, den, dnm, bnm, "1004", "", "", "", imagesDir, localDefnsDir, defnsDir, bytesOut);
                break;
    }

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 1004, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p3);
    if(con  != null) con.close();
    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public int r065(Connection con, Statement stmt, Statement stmt2, Statement stmt3, Statement stmt4, ResultSet rs, ResultSet rs2, ResultSet rs3, ResultSet rs4,
                  char whichReportVariant, char summaryOrDetailed, char orderedBy, String customerCode, String dateFrom, String dateTo,
                  String ignoreZero, String range1, String range2, String range3, String range4, String dnm, String unm, String reportsDir,
                  String localDefnsDir, String defnsDir, String workingDir) throws Exception
  {
    reportGeneration.currFont = 1;
    reportGeneration.currPage = 1;
    reportGeneration.currDown = reportGeneration.currAcross = 0.0;

    reportGeneration.oBufLen = 30000;
    reportGeneration.oBuf = new byte[30000];
    reportGeneration.oPtr = 0;

    String[] newName = new String[1];
    if((reportGeneration.fhO = reportGeneration.createNewFile((short)65, workingDir, localDefnsDir, defnsDir, reportsDir, newName)) == null)
      return -2;

    if((reportGeneration.fhPPR = generalUtils.fileOpenD("065.ppr", localDefnsDir)) == null)
    {
      if((reportGeneration.fhPPR = generalUtils.fileOpenD("065.ppr", defnsDir)) == null)
        return -1;
    }

    reportGeneration.lastOperationPF = false;

    String dateFromText;
    if(dateFrom.length() == 0)
    {
      dateFromText = "Earliest";
      dateFrom = "1970-01-01";
    }
    else
    {
      dateFromText = dateFrom;
      dateFrom = generalUtils.convertDateToSQLFormat(dateFrom);
    }

    String dateToText;
    if(dateTo.length() == 0)
    {
      dateToText = "Latest";
      dateTo = generalUtils.todaySQLFormat(localDefnsDir, defnsDir);
    }
    else
    {
      dateToText = dateTo;
      dateTo = generalUtils.convertDateToSQLFormat(dateTo);
    }

    int dateToEncoded = generalUtils.encodeFromYYYYMMDD(dateTo);
    
    int dateRange1 = dateToEncoded - generalUtils.strToInt(range1);
    int dateRange2 = dateToEncoded - generalUtils.strToInt(range2);
    int dateRange3 = dateToEncoded - generalUtils.strToInt(range3);
    int dateRange4 = dateToEncoded - generalUtils.strToInt(range4);    

    byte[] fldNames = new byte[10000];
    byte[] fldData  = new byte[10000];

    generalUtils.repAlpha(fldNames, 10000, (short)0, "DateCreated.DateFrom");
    generalUtils.repAlpha(fldNames, 10000, (short)1, "DateCreated.DateTo");
    generalUtils.repAlpha(fldNames, 10000, (short)2, "Date.Effective");
    generalUtils.repAlpha(fldNames, 10000, (short)3, "Account.Company");
    generalUtils.repAlpha(fldNames, 10000, (short)4, "Upto.Range1");
    generalUtils.repAlpha(fldNames, 10000, (short)5, "Upto.Range2");
    generalUtils.repAlpha(fldNames, 10000, (short)6, "Upto.Range3");
    generalUtils.repAlpha(fldNames, 10000, (short)7, "Upto.Range4");
    generalUtils.repAlpha(fldNames, 10000, (short)8, "Upto.OutOfRange");
    generalUtils.repAlpha(fldNames, 10000, (short)9, "Invoice.Code");
    generalUtils.repAlpha(fldNames, 10000, (short)10,"Invoice.CompanyCode");
    generalUtils.repAlpha(fldNames, 10000, (short)11,"Invoice.CompanyName");
    generalUtils.repAlpha(fldNames, 10000, (short)12,"Invoice.DateIssued");
    generalUtils.repAlpha(fldNames, 10000, (short)13,"Invoice.AccountCompany");
    generalUtils.repAlpha(fldNames, 10000, (short)14,"Heading.CompanyCode");
    generalUtils.repAlpha(fldNames, 10000, (short)15,"Heading.CompanyName");
    generalUtils.repAlpha(fldNames, 10000, (short)16,"Heading.Currency");
    generalUtils.repAlpha(fldNames, 10000, (short)17,"Data.Range1Base");
    generalUtils.repAlpha(fldNames, 10000, (short)18,"Data.Range2Base");
    generalUtils.repAlpha(fldNames, 10000, (short)19,"Data.Range3Base");
    generalUtils.repAlpha(fldNames, 10000, (short)20,"Data.Range4Base");
    generalUtils.repAlpha(fldNames, 10000, (short)21,"Data.OutOfRangeBase");
    generalUtils.repAlpha(fldNames, 10000, (short)22,"Data.TotalBase");
    generalUtils.repAlpha(fldNames, 10000, (short)23,"Company.AccumulatedBase");
    generalUtils.repAlpha(fldNames, 10000, (short)24,"Data.Range1NonBase");
    generalUtils.repAlpha(fldNames, 10000, (short)25,"Data.Range2NonBase");
    generalUtils.repAlpha(fldNames, 10000, (short)26,"Data.Range3NonBase");
    generalUtils.repAlpha(fldNames, 10000, (short)27,"Data.Range4NonBase");
    generalUtils.repAlpha(fldNames, 10000, (short)28,"Data.OutOfRangeNonBase");
    generalUtils.repAlpha(fldNames, 10000, (short)29,"Data.TotalNonBase");
    generalUtils.repAlpha(fldNames, 10000, (short)30,"Company.AccumulatedNonBase");
    generalUtils.repAlpha(fldNames, 10000, (short)31,"SubTotal.Range1Base");
    generalUtils.repAlpha(fldNames, 10000, (short)32,"SubTotal.Range2Base");
    generalUtils.repAlpha(fldNames, 10000, (short)33,"SubTotal.Range3Base");
    generalUtils.repAlpha(fldNames, 10000, (short)34,"SubTotal.Range4Base");
    generalUtils.repAlpha(fldNames, 10000, (short)35,"SubTotal.OutOfRangeBase");
    generalUtils.repAlpha(fldNames, 10000, (short)36,"SubTotal.TotalBase");
    generalUtils.repAlpha(fldNames, 10000, (short)37,"SubTotal.Range1NonBase");
    generalUtils.repAlpha(fldNames, 10000, (short)38,"SubTotal.Range2NonBase");
    generalUtils.repAlpha(fldNames, 10000, (short)39,"SubTotal.Range3NonBase");
    generalUtils.repAlpha(fldNames, 10000, (short)40,"SubTotal.Range4NonBase");
    generalUtils.repAlpha(fldNames, 10000, (short)41,"SubTotal.OutOfRangeNonBase");
    generalUtils.repAlpha(fldNames, 10000, (short)42,"SubTotal.TotalNonBase");
    generalUtils.repAlpha(fldNames, 10000, (short)43,"Base.Total");
    generalUtils.repAlpha(fldNames, 10000, (short)44,"Total.Count");
    generalUtils.repAlpha(fldNames, 10000, (short)45,"Total.Range1");
    generalUtils.repAlpha(fldNames, 10000, (short)46,"Total.Range2");
    generalUtils.repAlpha(fldNames, 10000, (short)47,"Total.Range3");
    generalUtils.repAlpha(fldNames, 10000, (short)48,"Total.Range4");
    generalUtils.repAlpha(fldNames, 10000, (short)49,"Total.OutOfRange");
    generalUtils.repAlpha(fldNames, 10000, (short)50,"Total.Total");
    generalUtils.repAlpha(fldNames, 10000, (short)51,"Document.Code");
    generalUtils.repAlpha(fldNames, 10000, (short)52,"Document.DateIssued");
    generalUtils.repAlpha(fldNames, 10000, (short)53,"Document.Amount");

    reportGeneration.processControl(dnm, unm, localDefnsDir, defnsDir);
    reportGeneration.processSection("RH", null, null, (short)0, dnm, unm, localDefnsDir, defnsDir);

    generalUtils.repAlpha(fldData, 10000, (short)0, dateFromText);
    generalUtils.repAlpha(fldData, 10000, (short)1, dateToText);
    generalUtils.repAlpha(fldData, 10000, (short)2, dateToText);

    generalUtils.repAlpha(fldData, 10000, (short)4, range1);
    generalUtils.repAlpha(fldData, 10000, (short)5, range2);
    generalUtils.repAlpha(fldData, 10000, (short)6, range3);
    generalUtils.repAlpha(fldData, 10000, (short)7, range4);
    generalUtils.repAlpha(fldData, 10000, (short)8, range4);

    String pageHeaderSection, pageFooterSection;

    if(summaryOrDetailed == 'D')
    {  
      pageHeaderSection = "PH";
      pageFooterSection = "PF";
    }
    else
    {  
      pageHeaderSection = "PH2";
      pageFooterSection = "PF2";
    }

    reportGeneration.processSection(pageHeaderSection, fldData, fldNames, (short)9, dnm, unm, localDefnsDir, defnsDir);

    double[] grandTotals = new double[6];
    int[] oCount = new int[1];  oCount[0] = 0;
    int x;
    for(x=0;x<6;++x)
      grandTotals[x] = 0.0;

    if(whichReportVariant == 'O')
    {
      customerCode = customerCode.toUpperCase();
      
      String customerName = customer.getACompanyFieldGivenCode(con, stmt, rs, "Name", customerCode);
      String currency     = customer.getACompanyFieldGivenCode(con, stmt, rs, "Currency", customerCode);
 
      generate(con, stmt, stmt2, stmt3, rs, rs2, rs3, summaryOrDetailed, customerCode, customerName, currency, dateFrom, dateTo, ignoreZero,
               dateRange1, dateRange2, dateRange3, dateRange4, grandTotals, fldNames, fldData, oCount, dnm, unm, pageHeaderSection, pageFooterSection,
               localDefnsDir, defnsDir);
    }
    else
    {
      // for all customers
      stmt = con.createStatement();

      if(orderedBy == 'N')
        rs = stmt.executeQuery("SELECT CompanyCode, Name, Currency FROM company WHERE NoStatements != 'Y' ORDER BY Name");
      else rs = stmt.executeQuery("SELECT CompanyCode, Name, Currency FROM company WHERE NoStatements != 'Y' ORDER BY CompanyCode");

      String companyCode, companyName, currency;

      while(rs.next())
      {    
        companyCode = rs.getString(1);
        companyName = rs.getString(2);
        currency    = rs.getString(3);

        generate(con, stmt2, stmt3, stmt4, rs2, rs3, rs4, summaryOrDetailed, companyCode, companyName, currency, dateFrom, dateTo, ignoreZero, dateRange1, dateRange2, dateRange3, dateRange4, grandTotals, fldNames, fldData, oCount, dnm, unm,
                 pageHeaderSection, pageFooterSection,  localDefnsDir, defnsDir);
      }

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }

    // output grandtotals
    generalUtils.repAlpha(fldData, 10000, (short)44, generalUtils.intToStr(oCount[0]));
    for(x=0;x<5;++x)
      generalUtils.repAlpha(fldData, 10000, (short)(45 + x), generalUtils.doubleToStr(grandTotals[x]));

    generalUtils.repAlpha(fldData, 10000, (short)50, generalUtils.doubleToStr(grandTotals[5]));

    reportGeneration.processSection("BL5", fldData, fldNames, (short)51, dnm, unm, localDefnsDir, defnsDir);

    reportGeneration.processSection(pageFooterSection, null, null, (short)0, dnm, unm, localDefnsDir, defnsDir);
    reportGeneration.processSection("RF", null, null, (short)0, dnm, unm, localDefnsDir, defnsDir);

    reportGeneration.fhO.write(reportGeneration.oBuf, 0, reportGeneration.oPtr);

    reportGeneration.fhO.close();
    reportGeneration.fhPPR.close();

    return 0;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void generate(Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, ResultSet rs3,
                        char summaryOrDetailed, String customerCode, String customerName, String currency, String dateFrom, String dateTo,
                        String ignoreZero, int dateRange1, int dateRange2, int dateRange3, int dateRange4, double[] grandTotals,
                        byte[] fldNames, byte[] fldData, int[] oCount, String dnm, String unm, String pageHeaderSection, String pageFooterSection,
                        String localDefnsDir, String defnsDir) throws Exception
  {
    System.out.print(customerCode + " ");
    
    double[] amountsBase    = new double[5];
    double[] amountsNonBase = new double[5];

    double[] runningTotalsBase    = new double[6];
    double[] runningTotalsNonBase = new double[6];
    int x;
    
    for(x=0;x<6;++x)
      runningTotalsBase[x] = runningTotalsNonBase[x] = 0.0;

    int[] latestReceiptDateEncoded = new int[1];

    boolean first = true;

    double d;

    double[] amtReceivedBase    = new double[1];
    double[] amtReceivedNonBase = new double[1];
    double[] amtRepaidBase      = new double[1];
    double[] amtRepaidNonBase   = new double[1];

    int range = 0, dateEncoded;

    int earliestDate = generalUtils.encodeFromYYYYMMDD("2099-12-31"); 

    generalUtils.repAlpha(fldData, 10000, (short)14, customerCode);
    generalUtils.repAlpha(fldData, 10000, (short)15, customerName);
    generalUtils.repAlpha(fldData, 10000, (short)16, currency);


    byte[] actualDate = new byte[20];
    double currentRateForCurrency = accountsUtils.getApplicableRate(con, stmt, rs, currency, generalUtils.convertFromYYYYMMDD(dateTo), actualDate, dnm, localDefnsDir, defnsDir);

    // do invoices
  
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT InvoiceCode, CompanyCode, CompanyName, Date, TotalTotal, BaseTotalTotal FROM invoice "
                          + "WHERE CompanyCode = '" + customerCode + "' AND Status != 'C' AND Settled != 'Y' AND Date >= {d '"
                          + dateFrom + "'} AND Date <= {d '" + dateTo + "'}");

    double totalAmountBase    = 0.0;
    double totalAmountNonBase = 0.0;

    String invoiceCode, invoiceCompanyCode, invoiceCompanyName, date;
    while(rs.next())
    {    
      invoiceCode        = rs.getString(1);
      invoiceCompanyCode = rs.getString(2);
      invoiceCompanyName = rs.getString(3);
      date               = rs.getString(4);
      totalAmountNonBase = generalUtils.doubleFromStr(generalUtils.doubleDPs(rs.getString(5), '2'));
      totalAmountBase    = generalUtils.doubleFromStr(generalUtils.doubleDPs(rs.getString(6), '2'));

      for(x=0;x<5;++x)
        amountsBase[x] = amountsNonBase[x] = 0.0;

      dateEncoded = generalUtils.encodeFromYYYYMMDD(date);
      
      if(dateEncoded > dateRange1)
        range = 0;
      else
      if(dateEncoded > dateRange2)
        range = 1;
      else
      if(dateEncoded > dateRange3)
        range = 2;
      else
      if(dateEncoded > dateRange4)
        range = 3;
      else range = 4;

      if(dateEncoded < earliestDate)
        earliestDate = dateEncoded;      

      generalUtils.repAlpha(fldData, 10000, (short)9,  invoiceCode);
      generalUtils.repAlpha(fldData, 10000, (short)10, invoiceCompanyCode);
      generalUtils.repAlpha(fldData, 10000, (short)11, invoiceCompanyName);
      generalUtils.repAlpha(fldData, 10000, (short)12, generalUtils.convertFromYYYYMMDD(date));

      latestReceiptDateEncoded[0] = 0;
      amtReceivedBase[0] = amtReceivedNonBase[0] = 0.0;

      getReceipts(con, stmt2, rs2, invoiceCode, dateTo, amtReceivedBase, amtReceivedNonBase, latestReceiptDateEncoded);
      
      amtRepaidBase[0] = amtRepaidNonBase[0] = 0.0;

      // Now, chk against the CNs file
      getSalesCreditNotes(con, stmt2, stmt3, rs2, rs3, invoiceCode, dateTo, amtRepaidBase, amtRepaidNonBase, dnm, localDefnsDir, defnsDir);
      
      if(generalUtils.doubleDPs((amtReceivedNonBase[0] + amtRepaidNonBase[0]),'2') != totalAmountNonBase) // >=
      {
        totalAmountNonBase -= (amtReceivedNonBase[0] + amtRepaidNonBase[0]);
        totalAmountBase    -= (amtReceivedBase[0]    + amtRepaidBase[0]);
  
        amountsBase[range]           = totalAmountBase;
        runningTotalsBase[range]    += totalAmountBase;
        runningTotalsBase[5]        += totalAmountBase;

        grandTotals[range]          += totalAmountBase;
        
        grandTotals[5] += (currentRateForCurrency * totalAmountNonBase);
        
        amountsNonBase[range]        = totalAmountNonBase;
        runningTotalsNonBase[range] += totalAmountNonBase;
        runningTotalsNonBase[5]     += totalAmountNonBase;

        ++oCount[0];

        d=0.0;
        for(x=0;x<5;++x)
        {
          generalUtils.repAlpha(fldData, 10000, (short)(17 + x), generalUtils.doubleToStr(amountsBase[x]));
          d += amountsBase[x];
        }
        generalUtils.repAlpha(fldData, 10000, (short)22, generalUtils.doubleToStr(d));

        d=0.0;
        for(x=0;x<5;++x)
        {
          generalUtils.repAlpha(fldData, 10000, (short)(24 + x), generalUtils.doubleToStr(amountsNonBase[x]));
          d += amountsNonBase[x];
        }
        generalUtils.repAlpha(fldData, 10000, (short)29, generalUtils.doubleToStr(d));

        generalUtils.repAlpha(fldData, 10000, (short)23, generalUtils.doubleToStr(runningTotalsBase[5]));

        generalUtils.repAlpha(fldData, 10000, (short)30, generalUtils.doubleToStr(runningTotalsNonBase[5]));

        if(summaryOrDetailed == 'D')
        {
          if(first)
          {
            first = false;
            reportGeneration.processSection("BL1", pageHeaderSection, pageFooterSection, fldData, fldNames, (short)17, dnm, unm, localDefnsDir, defnsDir);
          }

          reportGeneration.processSection("BL2", pageHeaderSection, pageFooterSection, fldData, fldNames, (short)31, dnm, unm, localDefnsDir, defnsDir);
        }
      }
    }

    if(stmt != null) stmt.close();

    // do debit notes
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT DNCode, CompanyCode, CompanyName, Date, TotalTotal, BaseTotalTotal FROM debit "
                          + "WHERE CompanyCode = '" + customerCode + "' AND Status != 'C' AND Settled != 'Y' AND Date >= {d '"
                          + dateFrom + "'} AND Date <= {d '" + dateTo + "'}");

    totalAmountBase    = 0.0;
    totalAmountNonBase = 0.0;

    String dnCode, dnCompanyCode, dnCompanyName;
    while(rs.next())
    {    
      dnCode             = rs.getString(1);
      dnCompanyCode      = rs.getString(2);
      dnCompanyName      = rs.getString(3);
      date               = rs.getString(4);
      totalAmountNonBase = generalUtils.doubleFromStr(generalUtils.doubleDPs(rs.getString(5), '2'));
      totalAmountBase    = generalUtils.doubleFromStr(generalUtils.doubleDPs(rs.getString(6), '2'));

      for(x=0;x<5;++x)
        amountsBase[x] = amountsNonBase[x] = 0.0;

      dateEncoded = generalUtils.encodeFromYYYYMMDD(date);
      
      if(dateEncoded > dateRange1)
        range = 0;
      else
      if(dateEncoded > dateRange2)
        range = 1;
      else
      if(dateEncoded > dateRange3)
        range = 2;
      else
      if(dateEncoded > dateRange4)
        range = 3;
      else range = 4;

      if(dateEncoded < earliestDate)
        earliestDate = dateEncoded;      

      generalUtils.repAlpha(fldData, 10000, (short)9,  dnCode);
      generalUtils.repAlpha(fldData, 10000, (short)10, dnCompanyCode);
      generalUtils.repAlpha(fldData, 10000, (short)11, dnCompanyName);
      generalUtils.repAlpha(fldData, 10000, (short)12, generalUtils.convertFromYYYYMMDD(date));

      latestReceiptDateEncoded[0] = 0;
      amtReceivedBase[0] = amtReceivedNonBase[0] = 0.0;

      getReceipts(con, stmt2, rs2, dnCode, dateTo, amtReceivedBase, amtReceivedNonBase, latestReceiptDateEncoded);
      
      amtRepaidBase[0] = amtRepaidNonBase[0] = 0.0;

      // Now, chk against the CNs file
      getSalesCreditNotes(con, stmt2, stmt3, rs2, rs3, dnCode, dateTo, amtRepaidBase, amtRepaidNonBase, dnm, localDefnsDir, defnsDir);
      
      if(generalUtils.doubleDPs((amtReceivedNonBase[0] + amtRepaidNonBase[0]),'2') != totalAmountNonBase) // >=
      {
        totalAmountNonBase -= (amtReceivedNonBase[0] + amtRepaidNonBase[0]);
        totalAmountBase    -= (amtReceivedBase[0]    + amtRepaidBase[0]);
  
        amountsBase[range]           = totalAmountBase;
        runningTotalsBase[range]    += totalAmountBase;
        runningTotalsBase[5]        += totalAmountBase;

        grandTotals[range]          += totalAmountBase;
        grandTotals[5] += (currentRateForCurrency * totalAmountNonBase);

        amountsNonBase[range]        = totalAmountNonBase;
        runningTotalsNonBase[range] += totalAmountNonBase;
        runningTotalsNonBase[5]     += totalAmountNonBase;

        ++oCount[0];

        d=0.0;
        for(x=0;x<5;++x)
        {
          generalUtils.repAlpha(fldData, 10000, (short)(17 + x), generalUtils.doubleToStr(amountsBase[x]));
          d += amountsBase[x];
        }
        generalUtils.repAlpha(fldData, 10000, (short)22, generalUtils.doubleToStr(d));

        d=0.0;
        for(x=0;x<5;++x)
        {
          generalUtils.repAlpha(fldData, 10000, (short)(24 + x), generalUtils.doubleToStr(amountsNonBase[x]));
          d += amountsNonBase[x];
        }
        generalUtils.repAlpha(fldData, 10000, (short)29, generalUtils.doubleToStr(d));

        generalUtils.repAlpha(fldData, 10000, (short)23, generalUtils.doubleToStr(runningTotalsBase[5]));

        generalUtils.repAlpha(fldData, 10000, (short)30, generalUtils.doubleToStr(runningTotalsNonBase[5]));

        if(summaryOrDetailed == 'D')
        {
          if(first)
          {
            first = false;
            reportGeneration.processSection("BL1", pageHeaderSection, pageFooterSection, fldData, fldNames, (short)17, dnm, unm, localDefnsDir, defnsDir);
          }

          reportGeneration.processSection("BL2", pageHeaderSection, pageFooterSection, fldData, fldNames, (short)31, dnm, unm, localDefnsDir, defnsDir);
        }
      }
    }

    if(stmt != null) stmt.close();

    boolean wanted;
    if(runningTotalsBase[5] == 0.0)
    {
      if(ignoreZero.equals("N"))
        wanted = true;
      else wanted = false;
    }
    else wanted = true;

    if(wanted)
    {
      for(x=0;x<6;++x)
        generalUtils.repAlpha(fldData, 10000, (short)(31 + x), generalUtils.doubleToStr(runningTotalsBase[x]));

      for(x=0;x<6;++x)
        generalUtils.repAlpha(fldData, 10000, (short)(37 + x), generalUtils.doubleToStr(runningTotalsNonBase[x]));

      for(x=0;x<6;++x)
        runningTotalsBase[x] = runningTotalsNonBase[x] = 0.0;

      if(summaryOrDetailed == 'D')
        reportGeneration.processSection("BL3", pageHeaderSection, pageFooterSection, fldData, fldNames, (short)43, dnm, unm, localDefnsDir, defnsDir);
      else reportGeneration.processSection("BL7", pageHeaderSection, pageFooterSection, fldData, fldNames, (short)43, dnm, unm, localDefnsDir, defnsDir);
    }

    customer.updatedDateInvoicePaid(con, stmt, rs, generalUtils.decodeToYYYYMMDD(earliestDate), customerCode, dnm, localDefnsDir, defnsDir);
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // puts date of latest receipt latestReceiptDate
  private void getReceipts(Connection con, Statement stmt, ResultSet rs, String invoiceCode, String dateTo, double[] amtReceivedBase,
                           double[] amtReceivedNonBase, int[] latestReceiptDateEncoded) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t1.DateReceived, t2.AmountReceived, t2.BaseAmountReceived "
                         + "FROM receiptl AS t2 INNER JOIN receipt AS t1 ON t1.ReceiptCode = t2.ReceiptCode " 
                         + "WHERE t2.InvoiceCode = '" + invoiceCode + "' AND t1.Status != 'C' AND DateReceived <= {d '" + dateTo + "'}");

    String dateReceived;

    while(rs.next())
    {    
      dateReceived           = rs.getString(1);
      amtReceivedNonBase[0] += generalUtils.doubleFromStr(generalUtils.doubleDPs(rs.getString(2), '2'));
      amtReceivedBase[0]    += generalUtils.doubleFromStr(generalUtils.doubleDPs(rs.getString(3), '2'));

      if(generalUtils.encodeFromYYYYMMDD(dateReceived) > latestReceiptDateEncoded[0])
        latestReceiptDateEncoded[0] = generalUtils.encodeFromYYYYMMDD(dateReceived);
    }

    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void getSalesCreditNotes(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String invoiceCode, String dateTo,
                              double[] amtRepaidBase, double[] amtRepaidNonBase, String dnm, String localDefnsDir,
                              String defnsDir) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.Amount2, t2.Amount, t2.GSTRate FROM creditl AS t2 INNER JOIN credit AS t1 ON t1.CNCode = t2.CNCode "
                         + "WHERE t2.InvoiceCode = '" + invoiceCode + "' AND t1.Status != 'C' AND t1.Date <= {d '" + dateTo + "'}");

    String gstRate;
    double amount, amount2, gst, rate;

    while(rs.next())
    {    
      amount2 = generalUtils.doubleFromStr(rs.getString(1));
      amount  = generalUtils.doubleFromStr(rs.getString(2));
      gstRate = rs.getString(3);

      rate = accountsUtils.getGSTRate(con, stmt2, rs2, gstRate);

      gst = amount2 * rate;
      amount2 += gst;
      amtRepaidNonBase[0] += generalUtils.doubleDPs(amount2, '2');

      gst = amount * rate;
      amount += gst;
      amtRepaidBase[0]    += generalUtils.doubleDPs(amount,  '2');
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

}
