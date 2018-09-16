// =============================================================================================================================================================
// System: ZaraStar Analytic: Debtor & Creditor Analysis
// Module: AnalyticsDebtorCreditorAnalysis.java
// Author: C.K.Harvey
// Copyright (c) 2003-08 Christopher Harvey. All Rights Reserved.
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

public class AnalyticsDebtorCreditorAnalysis extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  AccountsUtils accountsUtils = new AccountsUtils();
  DefinitionTables definitionTables = new DefinitionTables();
  
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="";

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
      p1  = req.getParameter("p1"); // monthEnd

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, bytesOut);
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
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "AnalyticsDebtorCreditorAnalysis", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6038, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1,
                    int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String imagesDir     = directoryUtils.getSupportDirs('I');
    String workingDir    = directoryUtils.getUserDir('W', dnm, unm);

    String uName = directoryUtils.getMySQLUserName();
    String pWord = directoryUtils.getMySQLPassWord();
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);

    Statement stmt = null, stmt2 = null;
    ResultSet rs   = null, rs2   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 6038, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "AccountsDebtorCreditorAnalysis", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6038, bytesOut[0], 0, "ACC:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "AccountsDebtorCreditorAnalysis", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6038, bytesOut[0], 0, "SID:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    int[] month = new int[1];
    int[] year  = new int[1];

    generalUtils.monthYearStrToYearAndMonth2(p1, month, year);

    // All months for the year are considered for data (but NOT those before an effective start date, if applicable).

    // need to know financial year start date for WAC start-from
    String yyyy = generalUtils.getFromDefnFile(unm, "accounts.dfn", workingDir, "");
    
    String[] financialYearStartMonth = new String[1];
    String[] financialYearEndMonth   = new String[1];
    definitionTables.getAppConfigFinancialYearMonths(con, stmt, rs, dnm, financialYearStartMonth, financialYearEndMonth);
    
    String financialYearEndDate = generalUtils.lastDayOfMonthYYYYMMDD(yyyy + "-" + generalUtils.detMonthNumFromMonthName(financialYearEndMonth[0]) + "-01");
    String financialYearStartDate = yyyy + "-" + generalUtils.detMonthNumFromMonthName(financialYearStartMonth[0]) + "-01";
    
    String yearStartDate = financialYearStartDate;
    String yearEndDate   = financialYearEndDate;

    int effectiveStartDate = generalUtils.encode(definitionTables.getAppConfigEffectiveStartDate(con, stmt, rs, dnm), localDefnsDir, defnsDir);

    if(effectiveStartDate > generalUtils.encodeFromYYYYMMDD(yearStartDate))
      yearStartDate = generalUtils.decodeToYYYYMMDD(effectiveStartDate);

    Connection accCon = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_accounts_" + yyyy + "?user=" + uName + "&password=" + pWord);

    process(accCon, con, stmt, stmt2, rs, rs2, out, req, yearStartDate, yearEndDate, yyyy, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 6038, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(accCon != null) accCon.close();
    if(con    != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void process(Connection accCon, Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, HttpServletRequest req,
                       String dateFrom, String dateTo, String year, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                       String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Debtor and Creditor Analysis</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\""
                           + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\"></head>");
  
    int[] hmenuCount = new int[1];
 
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "6038", "", "AccountsDebtorCreditorAnalysis", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
                                
    scoutln(out, bytesOut, "<form>");
  
    String[] cssFormat = new String[1];  cssFormat[0] = "";
    
    String baseCurrency = accountsUtils.getBaseCurrency(con, stmt, rs, dnm, localDefnsDir, defnsDir);      

    String debtorsAccount   = accountsUtils.getTradeDebtorsAccCode(year, dnm, localDefnsDir, defnsDir);
    String creditorsAccount = accountsUtils.getTradeCreditorsAccCode(year, dnm, localDefnsDir, defnsDir);

    String[][] accCurrencies = new String[1][];
    int numCurrencies = accountsUtils.getCurrencyNames(con, stmt, rs, accCurrencies, dnm, localDefnsDir, defnsDir);
    double[] accValsDrDebtors   = new double[numCurrencies];
    double[] accValsCrDebtors   = new double[numCurrencies];
    double[] accValsDrCreditors = new double[numCurrencies];
    double[] accValsCrCreditors = new double[numCurrencies];
    double[] accValsDebitsTotal  = new double[numCurrencies];
    double[] accValsCreditsTotal = new double[numCurrencies];
    int x;
    for(x=0;x<numCurrencies;++x) { accValsDebitsTotal[x] = 0.0; accValsCreditsTotal[x] = 0.0; }
    
    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Debtor and Creditor Analysis", "6038", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<table id='page' width='100%'>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    writeHeader(out, "Debtor", accCurrencies[0], numCurrencies, bytesOut);
    
    calculateInvoices(con, stmt, stmt2, rs, rs2, dateFrom, dateTo, accCurrencies[0], accValsDrDebtors, numCurrencies);
    writeLine(out, "Sales Invoices", accValsDrDebtors, accValsCrDebtors, numCurrencies, cssFormat, bytesOut);
    for(x=0;x<numCurrencies;++x) { accValsDebitsTotal[x] += accValsDrDebtors[x]; accValsCreditsTotal[x] += accValsCrDebtors[x]; }
    for(x=0;x<numCurrencies;++x) { accValsDrDebtors[x] = 0.0; accValsCrDebtors[x] = 0.0; }
      
    calculateDebitNotes(con, stmt, stmt2, rs, rs2, dateFrom, dateTo, accCurrencies[0], accValsDrDebtors, numCurrencies);
    writeLine(out, "Sales Debit Notes", accValsDrDebtors, accValsCrDebtors, numCurrencies, cssFormat, bytesOut);
    for(x=0;x<numCurrencies;++x) { accValsDebitsTotal[x] += accValsDrDebtors[x]; accValsCreditsTotal[x] += accValsCrDebtors[x]; }
    for(x=0;x<numCurrencies;++x) { accValsDrDebtors[x] = 0.0; accValsCrDebtors[x] = 0.0; }
      
    calculateSalesCreditNotes(con, stmt, stmt2, rs, rs2, dateFrom, dateTo, accCurrencies[0], accValsCrDebtors, numCurrencies);
    writeLine(out, "Sales Credit Notes", accValsDrDebtors, accValsCrDebtors, numCurrencies, cssFormat, bytesOut);
    for(x=0;x<numCurrencies;++x) { accValsDebitsTotal[x] += accValsDrDebtors[x]; accValsCreditsTotal[x] += accValsCrDebtors[x]; }
    for(x=0;x<numCurrencies;++x) { accValsDrDebtors[x] = 0.0; accValsCrDebtors[x] = 0.0; }
      
    calculateReceipts(con, stmt, stmt2, rs, rs2, dateFrom, dateTo, accCurrencies[0], accValsCrDebtors, numCurrencies);
    writeLine(out, "Receipts", accValsDrDebtors, accValsCrDebtors, numCurrencies, cssFormat, bytesOut);
    for(x=0;x<numCurrencies;++x) { accValsDebitsTotal[x] += accValsDrDebtors[x]; accValsCreditsTotal[x] += accValsCrDebtors[x]; }
    for(x=0;x<numCurrencies;++x) { accValsDrDebtors[x] = 0.0; accValsCrDebtors[x] = 0.0; }
      
    calculateReceiptVouchers(con, stmt, rs, dateFrom, dateTo, debtorsAccount, accCurrencies[0], accValsDrDebtors, numCurrencies);
    calculateReceiptVoucherLines(con, stmt, rs, dateFrom, dateTo, debtorsAccount, accCurrencies[0], accValsCrDebtors, numCurrencies);
    writeLine(out, "Receipt Vouchers", accValsDrDebtors, accValsCrDebtors, numCurrencies, cssFormat, bytesOut);
    for(x=0;x<numCurrencies;++x) { accValsDebitsTotal[x] += accValsDrDebtors[x]; accValsCreditsTotal[x] += accValsCrDebtors[x]; }
    for(x=0;x<numCurrencies;++x) { accValsDrDebtors[x] = 0.0; accValsCrDebtors[x] = 0.0; }

    try
    {
      stmt = accCon.createStatement();
      
      rs = stmt.executeQuery("SELECT Code, Type FROM joubatch WHERE OpeningBalances = 'Y' ORDER BY Code"); 

      String code, type;

      while(rs.next())
      {
        code = rs.getString(1);
        type = rs.getString(2);

        if(type.equals("D"))
          forBatchDebtorsOrCreditors(accCon, stmt, rs, code, dateFrom, dateTo, accCurrencies[0], accValsDrDebtors, accValsCrDebtors, numCurrencies);
        else
        if(type.equals("C"))
          ;
        else forBatchControl(accCon, stmt, rs, code, dateFrom, dateTo, debtorsAccount, accCurrencies[0], accValsDrDebtors, accValsCrDebtors, numCurrencies);
      }
     
      if(rs   != null) rs.close();                                 
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(rs   != null) rs.close();                                 
      if(stmt != null) stmt.close();
    }
    writeLine(out, "Journals (OB)", accValsDrDebtors, accValsCrDebtors, numCurrencies, cssFormat, bytesOut);
    for(x=0;x<numCurrencies;++x) { accValsDebitsTotal[x] += accValsDrDebtors[x]; accValsCreditsTotal[x] += accValsCrDebtors[x]; }
    for(x=0;x<numCurrencies;++x) { accValsDrDebtors[x] = 0.0; accValsCrDebtors[x] = 0.0; }

    try
    {
      stmt = accCon.createStatement();
      
      rs = stmt.executeQuery("SELECT Code, Type FROM joubatch WHERE OpeningBalances != 'Y' ORDER BY Code"); 

      String code, type;

      while(rs.next())
      {
        code = rs.getString(1);
        type = rs.getString(2);

        if(type.equals("D"))
          forBatchDebtorsOrCreditors(accCon, stmt, rs, code, dateFrom, dateTo, accCurrencies[0], accValsDrDebtors, accValsCrDebtors, numCurrencies);
        else
        if(type.equals("C"))
          ;
        else forBatchControl(accCon, stmt, rs, code, dateFrom, dateTo, debtorsAccount, accCurrencies[0], accValsDrDebtors, accValsCrDebtors, numCurrencies);
      }
     
      if(rs   != null) rs.close();                                 
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(rs   != null) rs.close();                                 
      if(stmt != null) stmt.close();
    }
    writeLine(out, "Journals", accValsDrDebtors, accValsCrDebtors, numCurrencies, cssFormat, bytesOut);
    for(x=0;x<numCurrencies;++x) { accValsDebitsTotal[x] += accValsDrDebtors[x]; accValsCreditsTotal[x] += accValsCrDebtors[x]; }

    writeValuationLine(con, stmt, rs, out, dateTo, baseCurrency, accValsDebitsTotal, accValsCreditsTotal, accCurrencies[0], numCurrencies, dnm, localDefnsDir,
                       defnsDir, cssFormat, bytesOut);

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    for(x=0;x<numCurrencies;++x) { accValsDebitsTotal[x] = 0.0; accValsCreditsTotal[x] = 0.0; }

    writeHeader(out, "Creditor", accCurrencies[0], numCurrencies, bytesOut);

    calculatePurchaseInvoices(con, stmt, stmt2, rs, rs2, dateFrom, dateTo, accCurrencies[0], accValsCrCreditors, numCurrencies);
    writeLine(out, "Purchase Invoices", accValsDrCreditors, accValsCrCreditors, numCurrencies, cssFormat, bytesOut);
    for(x=0;x<numCurrencies;++x) { accValsDebitsTotal[x] += accValsDrCreditors[x]; accValsCreditsTotal[x] += accValsCrCreditors[x]; }
    for(x=0;x<numCurrencies;++x) { accValsDrCreditors[x] = 0.0; accValsCrCreditors[x] = 0.0; }
      
    calculatePurchaseDebitNotes(con, stmt, stmt2, rs, rs2, dateFrom, dateTo, accCurrencies[0], accValsCrCreditors, numCurrencies);
    writeLine(out, "Purchase Debit Notes", accValsDrCreditors, accValsCrCreditors, numCurrencies, cssFormat, bytesOut);
    for(x=0;x<numCurrencies;++x) { accValsDebitsTotal[x] += accValsDrCreditors[x]; accValsCreditsTotal[x] += accValsCrCreditors[x]; }
    for(x=0;x<numCurrencies;++x) { accValsDrCreditors[x] = 0.0; accValsCrCreditors[x] = 0.0; }
      
    calculatePurchaseCreditNotes(con, stmt, stmt2, rs, rs2, dateFrom, dateTo, accCurrencies[0], accValsDrCreditors, numCurrencies);
    writeLine(out, "Purchase Credit Notes", accValsDrCreditors, accValsCrCreditors, numCurrencies, cssFormat, bytesOut);
    for(x=0;x<numCurrencies;++x) { accValsDebitsTotal[x] += accValsDrCreditors[x]; accValsCreditsTotal[x] += accValsCrCreditors[x]; }
    for(x=0;x<numCurrencies;++x) { accValsDrCreditors[x] = 0.0; accValsCrCreditors[x] = 0.0; }
      
    calculatePayments(con, stmt, stmt2, rs, rs2, dateFrom, dateTo, accCurrencies[0], accValsDrCreditors, numCurrencies);
    writeLine(out, "Payments", accValsDrCreditors, accValsCrCreditors, numCurrencies, cssFormat, bytesOut);
    for(x=0;x<numCurrencies;++x) { accValsDebitsTotal[x] += accValsDrCreditors[x]; accValsCreditsTotal[x] += accValsCrCreditors[x]; }
    for(x=0;x<numCurrencies;++x) { accValsDrCreditors[x] = 0.0; accValsCrCreditors[x] = 0.0; }
      
    calculatePaymentVouchers(con, stmt, rs, dateFrom, dateTo, creditorsAccount, accCurrencies[0], accValsCrCreditors, numCurrencies);
    calculatePaymentVoucherLines(con, stmt, rs, dateFrom, dateTo, creditorsAccount, accCurrencies[0], accValsDrCreditors, numCurrencies);
    writeLine(out, "Payment Vouchers", accValsDrCreditors, accValsCrCreditors, numCurrencies, cssFormat, bytesOut);
    for(x=0;x<numCurrencies;++x) { accValsDebitsTotal[x] += accValsDrCreditors[x]; accValsCreditsTotal[x] += accValsCrCreditors[x]; }
    for(x=0;x<numCurrencies;++x) { accValsDrCreditors[x] = 0.0; accValsCrCreditors[x] = 0.0; }
        
    try
    {
      stmt = accCon.createStatement();
      
      rs = stmt.executeQuery("SELECT Code, Type FROM joubatch WHERE OpeningBalances = 'Y' ORDER BY Code"); 

      String code, type;

      while(rs.next())
      {
        code = rs.getString(1);
        type = rs.getString(2);

        if(type.equals("C"))
          forBatchDebtorsOrCreditors(accCon, stmt, rs, code, dateFrom, dateTo, accCurrencies[0], accValsDrCreditors, accValsCrCreditors, numCurrencies);
        else
        if(type.equals("D"))
          ;
        else forBatchControl(accCon, stmt, rs, code, dateFrom, dateTo, debtorsAccount, accCurrencies[0], accValsDrCreditors, accValsCrCreditors, numCurrencies);
      }
     
      if(rs   != null) rs.close();                                 
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(rs   != null) rs.close();                                 
      if(stmt != null) stmt.close();
    }
    writeLine(out, "Journals (OB)", accValsDrCreditors, accValsCrCreditors, numCurrencies, cssFormat, bytesOut);
    for(x=0;x<numCurrencies;++x) { accValsDebitsTotal[x] += accValsDrCreditors[x]; accValsCreditsTotal[x] += accValsCrCreditors[x]; }
    for(x=0;x<numCurrencies;++x) { accValsDrCreditors[x] = 0.0; accValsCrCreditors[x] = 0.0; }

    try
    {
      stmt = accCon.createStatement();
      
      rs = stmt.executeQuery("SELECT Code, Type FROM joubatch WHERE OpeningBalances != 'Y' ORDER BY Code"); 

      String code, type;

      while(rs.next())
      {
        code = rs.getString(1);
        type = rs.getString(2);

        if(type.equals("C"))
          forBatchDebtorsOrCreditors(accCon, stmt, rs, code, dateFrom, dateTo, accCurrencies[0], accValsDrCreditors, accValsCrCreditors, numCurrencies);
        else
        if(type.equals("D"))
          ;
        else
        {
          forBatchControl(accCon, stmt, rs, code, dateFrom, dateTo, creditorsAccount, accCurrencies[0], accValsDrCreditors, accValsCrCreditors,
                          numCurrencies);
        }
      }
     
      if(rs   != null) rs.close();                                 
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(rs   != null) rs.close();                                 
      if(stmt != null) stmt.close();
    }
    writeLine(out, "Journals", accValsDrDebtors, accValsCrDebtors, numCurrencies, cssFormat, bytesOut);
    for(x=0;x<numCurrencies;++x) { accValsDebitsTotal[x] += accValsDrCreditors[x]; accValsCreditsTotal[x] += accValsCrCreditors[x]; }

    writeValuationLine(con, stmt, rs, out, dateTo, baseCurrency, accValsDebitsTotal, accValsCreditsTotal, accCurrencies[0], numCurrencies, dnm, localDefnsDir,
                       defnsDir, cssFormat, bytesOut);
    
    scoutln(out, bytesOut, "</table><table id='page' width='100%'>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    outputAnySalesCNsUnattached(con, stmt, stmt2, rs, rs2,  out, dateFrom, dateTo, baseCurrency, cssFormat, dnm, localDefnsDir, defnsDir,bytesOut);
    outputAnySalesCNForUnknownCompanies(con, stmt, stmt2, rs, rs2,  out, dateFrom, dateTo, baseCurrency, cssFormat, dnm, localDefnsDir, defnsDir,bytesOut);
    
    outputAnyPurchaseCNsUnattached(con, stmt, stmt2, rs, rs2,  out, dateFrom, dateTo, baseCurrency, cssFormat, dnm, localDefnsDir, defnsDir,bytesOut);
    outputAnyPurchaseCNForUnknownCompanies(con, stmt, stmt2, rs, rs2,  out, dateFrom, dateTo, baseCurrency, cssFormat, bytesOut);
 
    outputAnyReceiptsUnattached(con, stmt, stmt2, rs, rs2, out, dateFrom, dateTo, baseCurrency, cssFormat, dnm, localDefnsDir, defnsDir, bytesOut);
    outputAnyReceiptsForUnknownCompanies(con, stmt, stmt2, rs, rs2,  out, dateFrom, dateTo, baseCurrency, cssFormat, bytesOut);

    outputAnyPaymentsUnattached(con, stmt, stmt2, rs, rs2, out, dateFrom, dateTo, baseCurrency, cssFormat, dnm, localDefnsDir, defnsDir, bytesOut);
    outputAnyPaymentsForUnknownCompanies(con, stmt, stmt2, rs, rs2,  out, dateFrom, dateTo, baseCurrency, cssFormat, bytesOut);
    
    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void calculateInvoices(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String dateFrom, String dateTo,
                                 String[] accCurrencies, double[] accVals, int numCurrencies) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT CompanyCode, TotalTotal, Currency FROM invoice WHERE Date >= {d '" + dateFrom + "'} AND Date <= {d '" + dateTo
                         + "'} AND Status != 'C' AND Settled != 'Y'");

    double totalTotal;
    
    while(rs.next())
    {
      if(existsCustomer(con, stmt2, rs2, rs.getString(1)))
      {
        totalTotal = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(2)), '2');
      
        updateValue(totalTotal, rs.getString(3), accCurrencies, accVals, numCurrencies);
      }
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void calculateDebitNotes(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String dateFrom, String dateTo,
                                   String[] accCurrencies, double[] accVals, int numCurrencies) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT CompanyCode, TotalTotal, Currency FROM debit WHERE Date >= {d '" + dateFrom + "'} AND Date <= {d '" + dateTo
                         + "'} AND Status != 'C' AND Settled != 'Y'");

    double totalTotal;
    
    while(rs.next())
    {
      if(existsCustomer(con, stmt2, rs2, rs.getString(1)))
      {
        totalTotal = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(2)), '2');
      
        updateValue(totalTotal, rs.getString(3), accCurrencies, accVals, numCurrencies);
      }
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void calculateSalesCreditNotes(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String dateFrom, String dateTo,
                                    String[] accCurrencies, double[] accVals, int numCurrencies) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT CompanyCode, TotalTotal, Currency FROM credit WHERE Date >= {d '" + dateFrom + "'} AND Date <= {d '" + dateTo
                         + "'} AND Status != 'C'");

    double totalTotal;
    
    while(rs.next())
    {
      if(existsCustomer(con, stmt2, rs2, rs.getString(1)))
      {
        totalTotal = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(2)), '2');
      
        updateValue(totalTotal, rs.getString(3), accCurrencies, accVals, numCurrencies);
      }
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void calculateReceiptVouchers(Connection con, Statement stmt, ResultSet rs, String dateFrom, String dateTo, String debtorsAccount,
                                        String[] accCurrencies, double[] accVals, int numCurrencies) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT TotalTotal, Currency FROM rvoucher "
                         + "WHERE Status != 'C' AND Date >= {d '" + dateFrom + "'} AND Date <= {d '" + dateTo + "'} AND AccountDr = '" + debtorsAccount + "'");

    double totalTotal;
    
    while(rs.next())
    {
      totalTotal = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(1)), '2');
      
      updateValue(totalTotal, rs.getString(2), accCurrencies, accVals, numCurrencies);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void calculateReceiptVoucherLines(Connection con, Statement stmt, ResultSet rs, String dateFrom, String dateTo, String debtorsAccount,
                                            String[] accCurrencies, double[] accVals, int numCurrencies) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.Amount2, t1.Currency FROM rvoucherl AS t2 INNER JOIN rvoucher AS t1 "
                         + "ON t2.VoucherCode = t1.VoucherCode WHERE t1.Date >= {d '" + dateFrom + "'} AND t1.Date <= {d '"
                         + dateTo + "'} AND t1.Status != 'C' AND t2.AccountCr = '" + debtorsAccount + "'");

    double amount2;
    
    while(rs.next())
    {
      amount2 = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(1)), '2');
      
      updateValue(amount2, rs.getString(2), accCurrencies, accVals, numCurrencies);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void calculatePurchaseInvoices(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String dateFrom, String dateTo,
                                         String[] accCurrencies, double[] accVals, int numCurrencies) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT CompanyCode, TotalTotal, Currency FROM pinvoice WHERE Date >= {d '" + dateFrom + "'} AND Date <= {d '" + dateTo
                         + "'} AND Status != 'C' AND Settled != 'Y'");

    double totalTotal;
    
    while(rs.next())
    {
      if(existsSupplier(con, stmt2, rs2, rs.getString(1)))
      {
        totalTotal = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(2)), '2');
      
        updateValue(totalTotal, rs.getString(3), accCurrencies, accVals, numCurrencies);
      }
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void calculatePurchaseDebitNotes(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String dateFrom, String dateTo,
                                           String[] accCurrencies, double[] accVals, int numCurrencies) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT CompanyCode, TotalTotal, Currency FROM pdebit WHERE Date >= {d '" + dateFrom + "'} AND Date <= {d '" + dateTo
                         + "'} AND Status != 'C' AND Settled != 'Y'");

    double totalTotal;
    
    while(rs.next())
    {
      if(existsSupplier(con, stmt2, rs2, rs.getString(1)))
      {
        totalTotal = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(2)), '2');
      
        updateValue(totalTotal, rs.getString(3), accCurrencies, accVals, numCurrencies);
      }
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void calculatePurchaseCreditNotes(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String dateFrom, String dateTo,
                                            String[] accCurrencies, double[] accVals, int numCurrencies) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT CompanyCode, TotalTotal, Currency FROM pcredit WHERE Date >= {d '" + dateFrom + "'} AND Date <= {d '" + dateTo
                         + "'} AND Status != 'C'");

    double totalTotal;
    
    while(rs.next())
    {
      if(existsCustomer(con, stmt2, rs2, rs.getString(1)))
      {
        totalTotal = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(2)), '2');
      
        updateValue(totalTotal, rs.getString(3), accCurrencies, accVals, numCurrencies);
      }
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void calculatePaymentVouchers(Connection con, Statement stmt, ResultSet rs, String dateFrom, String dateTo, String creditorsAccount,
                                        String[] accCurrencies, double[] accVals, int numCurrencies) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT TotalTotal, Currency FROM voucher "
                         + "WHERE Status != 'C' AND Date >= {d '" + dateFrom + "'} AND Date <= {d '" + dateTo + "'} AND AccountCr = '" + creditorsAccount + "'");

    double totalTotal;
    
    while(rs.next())
    {
      totalTotal = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(1)), '2');
      
      updateValue(totalTotal, rs.getString(2), accCurrencies, accVals, numCurrencies);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void calculatePaymentVoucherLines(Connection con, Statement stmt, ResultSet rs, String dateFrom, String dateTo, String creditorsAccount,
                                            String[] accCurrencies, double[] accVals, int numCurrencies) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.Amount2, t1.Currency FROM voucherl AS t2 INNER JOIN voucher AS t1 "
                         + "ON t2.VoucherCode = t1.VoucherCode WHERE t1.Date >= {d '" + dateFrom + "'} AND t1.Date <= {d '"
                         + dateTo + "'} AND t1.Status != 'C' AND t2.AccountDr = '" + creditorsAccount + "'");

    double amount2;
    
    while(rs.next())
    {
      amount2 = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(1)), '2');
      
      updateValue(amount2, rs.getString(2), accCurrencies, accVals, numCurrencies);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void calculateReceipts(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String dateFrom, String dateTo,
                                 String[] accCurrencies, double[] accVals, int numCurrencies) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT CompanyCode, TotalAmount, Currency FROM receipt "
                         + "WHERE Status != 'C' AND DateReceived >= {d '" + dateFrom + "'} AND DateReceived <= {d '" + dateTo + "'}");

    double totalAmount;
    
    while(rs.next())
    {
      if(existsCustomer(con, stmt2, rs2, rs.getString(1)))
      {
        totalAmount = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(2)), '2');
      
        updateValue(totalAmount, rs.getString(3), accCurrencies, accVals, numCurrencies);
      }
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void calculatePayments(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String dateFrom, String dateTo,
                                 String[] accCurrencies, double[] accVals, int numCurrencies) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT CompanyCode, TotalAmount, Currency FROM payment "
                         + "WHERE Status != 'C' AND DatePaid >= {d '" + dateFrom + "'} AND DatePaid <= {d '" + dateTo + "'}");

    double totalAmount;
    
    while(rs.next())
    {
      if(existsCustomer(con, stmt2, rs2, rs.getString(1)))
      {
        totalAmount = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(2)), '2');
      
        updateValue(totalAmount, rs.getString(3), accCurrencies, accVals, numCurrencies);
      }
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void forBatchDebtorsOrCreditors(Connection con, Statement stmt, ResultSet rs, String code, String dateFrom, String dateTo, String[] accCurrencies,
                                          double[] accValsDr, double[] accValsCr, int numCurrencies) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT Amount, DrCr, Currency FROM joubatchl WHERE Code = '" + code + "' AND TransactionDate >= {d '" + dateFrom
                         + "'} AND TransactionDate <= {d '" + dateTo + "'}");
  
    double amount;
    
    while(rs.next())
    {
      amount = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(1)), '2');

      if(rs.getString(2).equals("D")) // Dr
        updateValue(amount, rs.getString(3), accCurrencies, accValsDr, numCurrencies);
      else updateValue(amount, rs.getString(3), accCurrencies, accValsCr, numCurrencies);
    }

    if(rs   != null) rs.close();                                 
    if(stmt != null) stmt.close();
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void forBatchControl(Connection con, Statement stmt, ResultSet rs, String code, String dateFrom, String dateTo, String debtorOrCreditorAccount,
                               String[] accCurrencies, double[] accValsDr, double[] accValsCr, int numCurrencies) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT Amount, DrCr, Currency FROM joubatchl WHERE Code = '" + code + "' AND TransactionDate >= {d '" + dateFrom
                         + "'} AND TransactionDate <= {d '" + dateTo + "'} AND AccCode = '" + debtorOrCreditorAccount + "'");
  
    double amount;
    
    while(rs.next())
    {
      amount = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(1)), '2');

      if(rs.getString(2).equals("D")) // Dr
        updateValue(amount, rs.getString(3), accCurrencies, accValsDr, numCurrencies);
      else updateValue(amount, rs.getString(3), accCurrencies, accValsCr, numCurrencies);
    }

    if(rs   != null) rs.close();                                 
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void writeLine(PrintWriter out, String document, double[] drAmounts, double[] crAmounts, int numCurrencies, String[] cssFormat, int[] bytesOut)
                         throws Exception
  {
    if(cssFormat[0].equals("line1")) cssFormat[0] = "line2"; else cssFormat[0] = "line1";

    scoutln(out, bytesOut, "<tr id='" + cssFormat[0] + "'><td><p>" + document + "</td>");
    
    for(int x=0;x<numCurrencies;++x)
    {
      if(drAmounts[x] != 0.0)
        scout(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(drAmounts[x], '2') + "</td>");
      else scout(out, bytesOut, "<td></td>");

      if(crAmounts[x] != 0.0)
        scout(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(crAmounts[x], '2') + "</td>");
      else scout(out, bytesOut, "<td></td>");
    }

    scoutln(out, bytesOut, "</tr>");    
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void writeValuationLine(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String dateTo, String baseCurrency, double[] drAmounts,
                                  double[] crAmounts, String[] accCurrencies, int numCurrencies, String dnm, String localDefnsDir, String defnsDir,
                                  String[] cssFormat, int[] bytesOut) throws Exception
  {
    if(cssFormat[0].equals("line1")) cssFormat[0] = "line2"; else cssFormat[0] = "line1";

    scoutln(out, bytesOut, "<tr id='" + cssFormat[0] + "'><td><p>Totals</td>");

    for(int x=0;x<numCurrencies;++x)
    {
      if(drAmounts[x] != 0.0)
        scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(drAmounts[x], '2') + "</td>");
      else scout(out, bytesOut, "<td></td>");

      if(crAmounts[x] != 0.0)
        scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(crAmounts[x], '2') + "</td>");
      else scout(out, bytesOut, "<td></td>");
    }

    scoutln(out, bytesOut, "</tr>");    

    if(cssFormat[0].equals("line1")) cssFormat[0] = "line2"; else cssFormat[0] = "line1";

    scoutln(out, bytesOut, "<tr id='" + cssFormat[0] + "'><td></td>");

    double d;
    
    for(int x=0;x<numCurrencies;++x)
    {
      d = drAmounts[x] - crAmounts[x];
      if(d != 0.0)
        scoutln(out, bytesOut, "<td align=center colspan=2><p>" + generalUtils.formatNumeric(d, '2') + "</td>");
      else scout(out, bytesOut, "<td colspan=2></td>");
    }

    scoutln(out, bytesOut, "</tr>");    

    if(cssFormat[0].equals("line1")) cssFormat[0] = "line2"; else cssFormat[0] = "line1";

    scoutln(out, bytesOut, "<tr id='" + cssFormat[0] + "'><td><p>" + baseCurrency + " Valuation as at " + dateTo + "</td>");

    double rate;
    byte[] actualDate = new byte[20];
    
    for(int x=0;x<numCurrencies;++x)
    {
      rate = accountsUtils.getApplicableRate(con, stmt, rs, accCurrencies[x], generalUtils.convertFromYYYYMMDD(dateTo), actualDate, dnm, localDefnsDir, defnsDir);

      d = drAmounts[x] - crAmounts[x];
      if(d != 0.0)
      {
        scoutln(out, bytesOut, "<td align=center colspan=2><p>" + generalUtils.formatNumeric((d * rate), '2') + " (using rate of " + rate + " as at "
                             + generalUtils.stringFromBytes(actualDate, 0) + ")</td>");
      }
      else scout(out, bytesOut, "<td colspan=2></td>");
    }

    scoutln(out, bytesOut, "</tr>");    
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void writeHeader(PrintWriter out, String debtorOrCreditor, String[] accCurrencies, int numCurrencies, int[] bytesOut) throws Exception
  {
    int x;
      
    scoutln(out, bytesOut, "<tr id='pageColumn'><td></td>");
    
    for(x=0;x<numCurrencies;++x)
      scout(out, bytesOut, "<td colspan=2 align=center><p>" + accCurrencies[x] + "</td>");

    scoutln(out, bytesOut, "</tr><tr id='pageColumn'><td></td>");

    for(x=0;x<numCurrencies;++x)
      scout(out, bytesOut, "<td colspan=2 align=center><p>" + debtorOrCreditor + "</td>");
    
    scoutln(out, bytesOut, "</tr><tr id='pageColumn'><td></td>");

    for(x=0;x<numCurrencies;++x)
      scout(out, bytesOut, "<td width=50% align=center><p>Dr</td><td width=50% align=center><p>Cr</td>");
    
    scoutln(out, bytesOut, "</tr>");
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean existsCustomer(Connection con, Statement stmt, ResultSet rs, String companyCode) throws Exception
  {
    int numRecs = 0;
        
    try
    {
      if(companyCode.length() == 0)
        return false;

      if(companyCode.equals("-"))
        return false;

      stmt = con.createStatement();
    
      rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM company WHERE CompanyCode = '" + companyCode + "'");
      rs.next();
      numRecs = rs.getInt("rowcount") ;
    }
    catch(Exception e) { }
      
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    if(numRecs == 1)
      return true;
    
    return false;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean existsSupplier(Connection con, Statement stmt, ResultSet rs, String companyCode) throws Exception
  {
    int numRecs = 0;
        
    try
    {
      if(companyCode.length() == 0)
        return false;

      if(companyCode.equals("-"))
        return false;

      stmt = con.createStatement();
    
      rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM supplier WHERE SupplierCode = '" + companyCode + "'");
      rs.next();
      numRecs = rs.getInt("rowcount") ;
    }
    catch(Exception e) { }
      
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    if(numRecs == 1)
      return true;
    
    return false;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void updateValue(double val, String thisCurrency, String[] accCurrencies, double[] accVals, int numCurrencies) throws Exception
  {
    int x=0;

    while(x < numCurrencies && ! accCurrencies[x].equals(thisCurrency)) // just-in-case
      ++x;
    
    if(x == numCurrencies) // not found!
      System.out.println(" Currency Not Found: >" + thisCurrency + "< " + val);
    else
    {
      val = generalUtils.doubleDPs(val, '2');
      accVals[x] += val;
    }
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean outputAnySalesCNsUnattached(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out,
                                         String dateFrom, String dateTo, String baseCurrency, 
                                         String[] cssFormat, String dnm, String localDefnsDir, String defnsDir, int[] bytesOut)
                                         throws Exception
  {
    stmt = con.createStatement();
 
    rs = stmt.executeQuery("SELECT COUNT(*) as rowcount FROM credit");
    
    int rowCount = 0;
    
    if(rs.next())
      rowCount = rs.getInt("rowcount");
    
    if(rs  != null) rs.close();
    if(stmt != null) stmt.close();

    if(rowCount == 0)
      return false;

    stmt = con.createStatement();

    // chk CNs for entries without valid invoice codes (blank entries or non-existant invoices)
    rs = stmt.executeQuery("SELECT t2.CNCode, t2.Amount2, t2.Amount, t2.GSTRate, t1.Date, t1.Currency "
                                   + "FROM credit AS t1 INNER JOIN creditl AS t2 ON t1.CNCode = t2.CNCode WHERE t1.Date >= {d '" + dateFrom + "'} AND t1.Date <= {d '" + dateTo + "'} AND t1.Status != 'C' AND ( t2.InvoiceCode NOT IN (SELECT InvoiceCode "
                                   + "FROM invoice) AND t2.InvoiceCode NOT IN (SELECT DNCode FROM debit) ) ORDER BY t1.Date, t2.CNCode");

    String cnCode, amount, baseAmount, gstRate, dateIssued, currency;
    double thisBaseAmt, thisNonBaseAmt, gst;
    
    boolean oneFound = false;

    while(rs.next())
    {    
      cnCode      = rs.getString(1);
      amount      = rs.getString(2);
      baseAmount  = rs.getString(3);
      gstRate     = rs.getString(4);
      dateIssued  = rs.getString(5);
      currency    = rs.getString(6);
  
      thisBaseAmt    = generalUtils.doubleFromStr(baseAmount);
      thisNonBaseAmt = generalUtils.doubleFromStr(amount);

      gst = accountsUtils.getGSTRate(con, stmt2, rs2, gstRate);
      
      thisBaseAmt    += (thisBaseAmt * gst);
      thisNonBaseAmt += (thisNonBaseAmt * gst);

      writeBodyLineSalesSalesCreditNote(out, "Unattached ", cnCode, dateIssued, generalUtils.doubleToStr('2', thisNonBaseAmt), generalUtils.doubleToStr('2', thisBaseAmt),
                              currency, baseCurrency, cssFormat, bytesOut);
      oneFound = true;
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    return oneFound;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean outputAnyReceiptsUnattached(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out,
                                              String dateFrom, String dateTo, String baseCurrency, 
                                              String[] cssFormat, String dnm, String localDefnsDir, String defnsDir, int[] bytesOut)
                                              throws Exception
  {
    stmt = con.createStatement();
 
    rs = stmt.executeQuery("SELECT COUNT(*) as rowcount FROM receipt");
    
    int rowCount = 0;
    
    if(rs.next())
      rowCount = rs.getInt("rowcount");
    
    if(rs  != null) rs.close();
    if(stmt != null) stmt.close();

    if(rowCount == 0)
      return false;

    stmt = con.createStatement();

    // chk CNs for entries without valid invoice codes (blank entries or non-existant invoices)
    rs = stmt.executeQuery("SELECT t2.ReceiptCode, t2.BaseAmountReceived, t2.AmountReceived, t1.DateReceived, t1.Currency "
                         + "FROM receipt AS t1 INNER JOIN receiptl AS t2 ON t1.ReceiptCode = t2.ReceiptCode WHERE t1.DateReceived >= {d '" + dateFrom
                         + "'} AND t1.DateReceived <= {d '" + dateTo + "'} AND t1.Status != 'C' AND ( t2.InvoiceCode NOT IN (SELECT InvoiceCode "
                         + "FROM invoice) AND t2.InvoiceCode NOT IN (SELECT DNCode FROM debit) ) ORDER BY t1.DateReceived, t2.ReceiptCode");

    String receiptCode, amount, baseAmount, dateIssued, currency;
    double thisBaseAmt, thisNonBaseAmt;
    
    boolean oneFound = false;

    while(rs.next())
    {    
      receiptCode      = rs.getString(1);
      amount      = rs.getString(2);
      baseAmount  = rs.getString(3);
      dateIssued  = rs.getString(4);
      currency    = rs.getString(5);
  
      thisBaseAmt    = generalUtils.doubleFromStr(baseAmount);
      thisNonBaseAmt = generalUtils.doubleFromStr(amount);

      writeBodyLineReceipt(out, "Unattached", receiptCode, dateIssued, generalUtils.doubleToStr('2', thisNonBaseAmt), generalUtils.doubleToStr('2', thisBaseAmt),
                              currency, baseCurrency, cssFormat, bytesOut);
      oneFound = true;
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    return oneFound;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean outputAnyPaymentsUnattached(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out,
                                              String dateFrom, String dateTo, String baseCurrency, 
                                              String[] cssFormat, String dnm, String localDefnsDir, String defnsDir, int[] bytesOut)
                                              throws Exception
  {
    stmt = con.createStatement();
 
    rs = stmt.executeQuery("SELECT COUNT(*) as rowcount FROM payment");
    
    int rowCount = 0;
    
    if(rs.next())
      rowCount = rs.getInt("rowcount");
    
    if(rs  != null) rs.close();
    if(stmt != null) stmt.close();

    if(rowCount == 0)
      return false;

    stmt = con.createStatement();

    // chk CNs for entries without valid invoice codes (blank entries or non-existant invoices)
    rs = stmt.executeQuery("SELECT t2.PaymentCode, t2.BaseAmountPaid, t2.AmountPaid, t1.DatePaid, t1.Currency "
                         + "FROM payment AS t1 INNER JOIN paymentl AS t2 ON t1.PaymentCode = t2.PaymentCode WHERE t1.DatePaid >= {d '" + dateFrom
                         + "'} AND t1.DatePaid <= {d '" + dateTo + "'} AND t1.Status != 'C' AND ( t2.InvoiceCode NOT IN (SELECT InvoiceCode "
                         + "FROM pinvoice) AND t2.InvoiceCode NOT IN (SELECT PDNCode FROM pdebit) ) ORDER BY t1.DatePaid, t2.PaymentCode");

    String paymentCode, amount, baseAmount, dateIssued, currency;
    double thisBaseAmt, thisNonBaseAmt;
    
    boolean oneFound = false;

    while(rs.next())
    {    
      paymentCode = rs.getString(1);
      amount      = rs.getString(2);
      baseAmount  = rs.getString(3);
      dateIssued  = rs.getString(4);
      currency    = rs.getString(5);
  
      thisBaseAmt    = generalUtils.doubleFromStr(baseAmount);
      thisNonBaseAmt = generalUtils.doubleFromStr(amount);

      writeBodyLinePayment(out, "Unknown", paymentCode, dateIssued, generalUtils.doubleToStr('2', thisNonBaseAmt), generalUtils.doubleToStr('2', thisBaseAmt), currency,
                           baseCurrency, cssFormat, bytesOut);
      oneFound = true;
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    return oneFound;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean outputAnyPurchaseCNsUnattached(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out,
                                         String dateFrom, String dateTo, String baseCurrency, 
                                         String[] cssFormat, String dnm, String localDefnsDir, String defnsDir, int[] bytesOut)
                                         throws Exception
  {
    stmt = con.createStatement();
 
    rs = stmt.executeQuery("SELECT COUNT(*) as rowcount FROM pcredit");
    
    int rowCount = 0;
    
    if(rs.next())
      rowCount = rs.getInt("rowcount");
    
    if(rs  != null) rs.close();
    if(stmt != null) stmt.close();

    if(rowCount == 0)
      return false;

    stmt = con.createStatement();

    // chk CNs for entries without valid invoice codes (blank entries or non-existant invoices)
    rs = stmt.executeQuery("SELECT t2.PCNCode, t2.Amount2, t2.Amount, t2.GSTRate, t1.Date, t1.Currency "
                                   + "FROM pcredit AS t1 INNER JOIN pcreditl AS t2 ON t1.PCNCode = t2.PCNCode WHERE t1.Date >= {d '" + dateFrom
                                   + "'} AND t1.Date <= {d '" + dateTo + "'} AND t1.Status != 'C' AND ( t2.InvoiceCode NOT IN (SELECT InvoiceCode "
                                   + "FROM pinvoice) AND t2.InvoiceCode NOT IN (SELECT PDNCode FROM pdebit) ) ORDER BY t1.Date, t2.PCNCode");

    String cnCode, amount, baseAmount, gstRate, dateIssued, currency;
    double thisBaseAmt, thisNonBaseAmt, gst;
    
    boolean oneFound = false;

    while(rs.next())
    {    
      cnCode      = rs.getString(1);
      amount      = rs.getString(2);
      baseAmount  = rs.getString(3);
      gstRate     = rs.getString(4);
      dateIssued  = rs.getString(5);
      currency    = rs.getString(6);
  
      thisBaseAmt    = generalUtils.doubleFromStr(baseAmount);
      thisNonBaseAmt = generalUtils.doubleFromStr(amount);

      gst = accountsUtils.getGSTRate(con, stmt2, rs2, gstRate);
      
      thisBaseAmt    += (thisBaseAmt * gst);
      thisNonBaseAmt += (thisNonBaseAmt * gst);

      writeBodyLinePurchaseCreditNote(out, "Unattached", cnCode, dateIssued, generalUtils.doubleToStr('2', thisNonBaseAmt), generalUtils.doubleToStr('2', thisBaseAmt),
                              currency, baseCurrency, cssFormat, bytesOut);
      oneFound = true;
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    return oneFound;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void writeBodyLineSalesSalesCreditNote(PrintWriter out, String msg, String code, String date, String amount, String baseAmount, String currency,
                                       String baseCurrency, String[] cssFormat,  int[] bytesOut) throws Exception
  {
    if(! cssFormat[0].equals("line1")) cssFormat[0] = "line1"; else cssFormat[0] = "line2";

    scoutln(out, bytesOut, "<tr id=\"" + cssFormat[0] + "\"><td><span id=\"textRedHighlighting\">" + msg + " Sales Credit Note&nbsp;</span></td>");
    scoutln(out, bytesOut, "<td nowrap><a href=\"javascript:credit('" + code + "')\">" + code + "</a></td>");
    scoutln(out, bytesOut, "<td nowrap align=right><span id=\"textRedHighlighting\"> &nbsp; " + currency + " " 
                  + generalUtils.doubleDPs(amount, '2') + "</span></td>");
    scoutln(out, bytesOut, "<td nowrap><span id=\"textRedHighlighting\"> &nbsp; Issued on " + generalUtils.convertFromYYYYMMDD(date) + "</span></td>");
    scoutln(out, bytesOut, "<td nowrap align=right><span id=\"textRedHighlighting\">(" + baseCurrency + " " 
                  + generalUtils.doubleDPs(baseAmount, '2') + ")</span></td>");
    scoutln(out, bytesOut, "<td></td></tr>");
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void writeBodyLineReceipt(PrintWriter out, String msg, String code, String date, String amount, String baseAmount, String currency,
                                    String baseCurrency, String[] cssFormat,  int[] bytesOut) throws Exception
  {
    if(! cssFormat[0].equals("line1"))
      cssFormat[0] = "line1";
    else cssFormat[0] = "line2";

    scoutln(out, bytesOut, "<tr id=\"" + cssFormat[0] + "\"><td><span id=\"textRedHighlighting\">" + msg + " Receipt&nbsp;</span></td>");
    scoutln(out, bytesOut, "<td nowrap><a href=\"javascript:receipt('" + code + "')\">" + code + "</a></td>");
    scoutln(out, bytesOut, "<td nowrap align=right><span id=\"textRedHighlighting\"> &nbsp; " + currency + " " 
                  + generalUtils.doubleDPs(amount, '2') + "</span></td>");
    scoutln(out, bytesOut, "<td nowrap><span id=\"textRedHighlighting\"> &nbsp; Issued on " + generalUtils.convertFromYYYYMMDD(date) + "</span></td>");
    scoutln(out, bytesOut, "<td nowrap align=right><span id=\"textRedHighlighting\">(" + baseCurrency + " " 
                  + generalUtils.doubleDPs(baseAmount, '2') + ")</span></td>");
    scoutln(out, bytesOut, "<td></td></tr>");
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void writeBodyLinePayment(PrintWriter out, String msg, String code, String date, String amount, String baseAmount, String currency,
                                    String baseCurrency, String[] cssFormat,  int[] bytesOut) throws Exception
  {
    if(! cssFormat[0].equals("line1")) cssFormat[0] = "line1"; else cssFormat[0] = "line2";

    scoutln(out, bytesOut, "<tr id=\"" + cssFormat[0] + "\"><td><span id=\"textRedHighlighting\">" + msg + " Payment&nbsp;</span></td>");
    scoutln(out, bytesOut, "<td nowrap><a href=\"javascript:payment('" + code + "')\">" + code + "</a></td>");
    scoutln(out, bytesOut, "<td nowrap align=right><span id=\"textRedHighlighting\"> &nbsp; " + currency + " " 
                  + generalUtils.doubleDPs(amount, '2') + "</span></td>");
    scoutln(out, bytesOut, "<td nowrap><span id=\"textRedHighlighting\"> &nbsp; Issued on " + generalUtils.convertFromYYYYMMDD(date) + "</span></td>");
    scoutln(out, bytesOut, "<td nowrap align=right><span id=\"textRedHighlighting\">(" + baseCurrency + " " 
                  + generalUtils.doubleDPs(baseAmount, '2') + ")</span></td>");
    scoutln(out, bytesOut, "<td></td></tr>");
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void writeBodyLinePurchaseCreditNote(PrintWriter out, String msg, String code, String date, String amount, String baseAmount, String currency,
                                               String baseCurrency, String[] cssFormat,  int[] bytesOut) throws Exception
  {
    if(! cssFormat[0].equals("line1")) cssFormat[0] = "line1"; else cssFormat[0] = "line2";

    scoutln(out, bytesOut, "<tr id=\"" + cssFormat[0] + "\"><td><span id=\"textRedHighlighting\">" + msg + " Purchase Credit Note&nbsp;</span></td>");
    scoutln(out, bytesOut, "<td nowrap><a href=\"javascript:credit('" + code + "')\">" + code + "</a></td>");
    scoutln(out, bytesOut, "<td nowrap align=right><span id=\"textRedHighlighting\"> &nbsp; " + currency + " " 
                  + generalUtils.doubleDPs(amount, '2') + "</span></td>");
    scoutln(out, bytesOut, "<td nowrap><span id=\"textRedHighlighting\"> &nbsp; Issued on " + generalUtils.convertFromYYYYMMDD(date) + "</span></td>");
    scoutln(out, bytesOut, "<td nowrap align=right><span id=\"textRedHighlighting\">(" + baseCurrency + " " 
                  + generalUtils.doubleDPs(baseAmount, '2') + ")</span></td>");
    scoutln(out, bytesOut, "<td></td></tr>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean outputAnySalesCNForUnknownCompanies(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out,
                                                      String dateFrom, String dateTo, String baseCurrency, String[] cssFormat, String dnm, String localDefnsDir,
                                                      String defnsDir, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    // chk CNs for entries without valid invoice codes (blank entries or non-existant invoices)
    rs = stmt.executeQuery("SELECT CNCode, TotalTotal, BaseTotalTotal, Date, Currency, CompanyCode "
                         + "FROM credit WHERE Date >= {d '" + dateFrom + "'} AND Date <= {d '" + dateTo + "'} AND Status != 'C' ORDER BY Date, CNCode");

    String cnCode, amount, baseAmount, gstRate, dateIssued, currency;
    double thisBaseAmt, thisNonBaseAmt, gst;
    
    boolean oneFound = false;

    while(rs.next())
    {    
      if(! existsCustomer(con, stmt2, rs2, rs.getString(6)))
      {
        cnCode      = rs.getString(1);
        amount      = rs.getString(2);
        baseAmount  = rs.getString(3);
        dateIssued  = rs.getString(4);
        currency    = rs.getString(5);
  
        thisBaseAmt    = generalUtils.doubleFromStr(baseAmount);
        thisNonBaseAmt = generalUtils.doubleFromStr(amount);

        writeBodyLineSalesSalesCreditNote(out, "Unknown Company", cnCode, dateIssued, generalUtils.doubleToStr('2', thisNonBaseAmt), generalUtils.doubleToStr('2', thisBaseAmt),
                                     currency, baseCurrency, cssFormat, bytesOut);
        
        oneFound = true;
      }
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    return oneFound;
  }

 // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean outputAnyPurchaseCNForUnknownCompanies(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out,
                                                         String dateFrom, String dateTo, String baseCurrency, String[] cssFormat, int[] bytesOut)
                                                         throws Exception
  {
    stmt = con.createStatement();

    // chk CNs for entries without valid invoice codes (blank entries or non-existant invoices)
    rs = stmt.executeQuery("SELECT PCNCode, TotalTotal, BaseTotalTotal, Date, Currency, CompanyCode "
                         + "FROM pcredit WHERE Date >= {d '" + dateFrom + "'} AND Date <= {d '" + dateTo + "'} AND Status != 'C' ORDER BY Date, PCNCode");

    String cnCode, amount, baseAmount, gstRate, dateIssued, currency;
    double thisBaseAmt, thisNonBaseAmt, gst;
    
    boolean oneFound = false;

    while(rs.next())
    {    
      if(! existsSupplier(con, stmt2, rs2, rs.getString(6)))
      {
        cnCode      = rs.getString(1);
        amount      = rs.getString(2);
        baseAmount  = rs.getString(3);
        dateIssued  = rs.getString(4);
        currency    = rs.getString(5);
  
        thisBaseAmt    = generalUtils.doubleFromStr(baseAmount);
        thisNonBaseAmt = generalUtils.doubleFromStr(amount);

        writeBodyLinePurchaseCreditNote(out, "Unknown Company", cnCode, dateIssued, generalUtils.doubleToStr('2', thisNonBaseAmt), generalUtils.doubleToStr('2', thisBaseAmt),
                                        currency, baseCurrency, cssFormat, bytesOut);
        oneFound = true;
      }
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    return oneFound;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean outputAnyReceiptsForUnknownCompanies(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out,
                                                       String dateFrom, String dateTo, String baseCurrency, String[] cssFormat, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT ReceiptCode, BaseTotalAmount, TotalAmount, DateReceived, Currency, CompanyCode "
                         + "FROM receipt WHERE DateReceived >= {d '" + dateFrom + "'} AND DateReceived <= {d '" + dateTo
                         + "'} AND Status != 'C' ORDER BY DateReceived, ReceiptCode");

    String receiptCode, amount, baseAmount, dateIssued, currency;
    double thisBaseAmt, thisNonBaseAmt;
    
    boolean oneFound = false;

    while(rs.next())
    {    
      if(! existsCustomer(con, stmt2, rs2, rs.getString(6)))
      {
        receiptCode = rs.getString(1);
        amount      = rs.getString(2);
        baseAmount  = rs.getString(3);
        dateIssued  = rs.getString(4);
        currency    = rs.getString(5);
  
        thisBaseAmt    = generalUtils.doubleFromStr(baseAmount);
        thisNonBaseAmt = generalUtils.doubleFromStr(amount);

        writeBodyLineReceipt(out, "Unknown Company", receiptCode, dateIssued, generalUtils.doubleToStr('2', thisNonBaseAmt), generalUtils.doubleToStr('2', thisBaseAmt),
                             currency, baseCurrency, cssFormat, bytesOut);
        oneFound = true;
      }
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    return oneFound;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean outputAnyPaymentsForUnknownCompanies(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out,
                                                       String dateFrom, String dateTo, String baseCurrency, String[] cssFormat, int[] bytesOut)
                                                       throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT PaymentCode, BaseTotalAmount, TotalAmount, DatePaid, Currency, CompanyCode "
                         + "FROM payment WHERE DatePaid >= {d '" + dateFrom + "'} AND DatePaid <= {d '" + dateTo
                         + "'} AND Status != 'C' ORDER BY DatePaid, PaymentCode");

    String paymentCode, amount, baseAmount, dateIssued, currency;
    double thisBaseAmt, thisNonBaseAmt;
    
    boolean oneFound = false;

    while(rs.next())
    {    
      if(! existsSupplier(con, stmt2, rs2, rs.getString(6)))
      {
        paymentCode = rs.getString(1);
        amount      = rs.getString(2);
        baseAmount  = rs.getString(3);
        dateIssued  = rs.getString(4);
        currency    = rs.getString(5);
  
        thisBaseAmt    = generalUtils.doubleFromStr(baseAmount);
        thisNonBaseAmt = generalUtils.doubleFromStr(amount);

        writeBodyLinePayment(out, "Unknown Company", paymentCode, dateIssued, generalUtils.doubleToStr('2', thisNonBaseAmt), generalUtils.doubleToStr('2', thisBaseAmt),
                             currency, baseCurrency, cssFormat, bytesOut);
        oneFound = true;
      }
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    return oneFound;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scout(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.print(str);
    bytesOut[0] += str.length();    
  }
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
