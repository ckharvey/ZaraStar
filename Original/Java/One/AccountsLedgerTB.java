// =======================================================================================================================================================================================================
// System: ZaraStar Admin: Ledger against TB
// Module: AccountsLedgerTB.java
// Author: C.K.Harvey
// Copyright (c) 2006-09 Christopher Harvey. All Rights Reserved.
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
import java.util.*;
import java.io.*;

public class AccountsLedgerTB extends HttpServlet
{
  GeneralUtils  generalUtils  = new GeneralUtils();
  MessagePage  messagePage  = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  AdminUtils  adminUtils  = new AdminUtils();
  AccountsUtils  accountsUtils  = new AccountsUtils();
  MiscDefinitions  miscDefinitions  = new MiscDefinitions();
  DocumentUtils  documentUtils  = new DocumentUtils();
  DefinitionTables  definitionTables  = new DefinitionTables();
  Inventory  inventory  = new Inventory();
  AccountsGenerateTBReport accountsGenerateTBReport = new AccountsGenerateTBReport();
  AccountsGeneratePLReportLive accountsGeneratePLReportLive = new AccountsGeneratePLReportLive();
  Customer customer = new Customer();
  Supplier supplier = new Supplier();

  AccountsGLLiabilitiesAccruals accountsGLLiabilitiesAccruals = new AccountsGLLiabilitiesAccruals();
  AnalyticsStock analyticsStock = new AnalyticsStock();
  AccountsGLGSTOutput accountsGLGSTOutput = new AccountsGLGSTOutput();
  AccountsGLGSTInput accountsGLGSTInput = new AccountsGLGSTInput();
  AccountsGLFixedAssetsEquity accountsGLFixedAssetsEquity = new AccountsGLFixedAssetsEquity();
  AccountsGLCurrentAssetsContra accountsGLCurrentAssetsContra = new AccountsGLCurrentAssetsContra();
  AccountsGLLiabilities accountsGLLiabilities = new AccountsGLLiabilities();
  AccountsGLEquityEarnings accountsGLEquityEarnings = new AccountsGLEquityEarnings();
  AccountsGLRecoverableExpenses accountsGLRecoverableExpenses = new AccountsGLRecoverableExpenses();
  AccountsGLCurrentAssetsPrePayments accountsGLCurrentAssetsPrePayments = new AccountsGLCurrentAssetsPrePayments();
  AccountsGLLiabilitiesPrePayments accountsGLLiabilitiesPrePayments = new AccountsGLLiabilitiesPrePayments();
  AccountsGLGSTClearing accountsGLGSTClearing = new AccountsGLGSTClearing();
  AccountsGLOtherDebtors accountsGLOtherDebtors = new AccountsGLOtherDebtors();
  AccountsGLOtherCreditors accountsGLOtherCreditors = new AccountsGLOtherCreditors();
  AccountsGLEquityCurrencyExchange accountsGLEquityCurrencyExchange = new AccountsGLEquityCurrencyExchange();
  AccountsGLBankTransactions  accountsGLBankTransactions  = new AccountsGLBankTransactions();
  AnalyticsWIP analyticsWIP = new AnalyticsWIP();

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

      Enumeration en = req.getParameterNames();
      while(en.hasMoreElements())
      {
        String name = (String)en.nextElement();
        String[] value = req.getParameterValues(name);
        if(name.equals("unm"))
          unm = value[0];
        else
        if(name.equals("sid"))
          sid = value[0];
        else
        if(name.equals("uty"))
          uty = value[0];
        else
        if(name.equals("men"))
          men = value[0];
        else
        if(name.equals("den"))
          den = value[0];
        else
        if(name.equals("dnm"))
          dnm = value[0];
        else
        if(name.equals("bnm"))
          bnm = value[0];
      }

      doIt(out, req, unm, uty, sid, men, den, dnm, bnm, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "AccountsLedgerTB", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6052, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String uty, String sid, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String workingDir    = directoryUtils.getUserDir('W', dnm, unm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null, stmt2 = null, stmt3 = null;
    ResultSet rs   = null, rs2   = null, rs3   = null;

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 6052, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
      {
        messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "6052", imagesDir, localDefnsDir, defnsDir, bytesOut);
        serverUtils.etotalBytes(req, unm, dnm, 6052, bytesOut[0], 0, "ACC:");
        if(con != null) con.close();
        if(out != null) out.flush();
        return;
      }

      String year = generalUtils.getFromDefnFile(unm, "accounts.dfn", workingDir, "");

      verify(con, stmt, stmt2, stmt3, rs, rs2, rs3, out, req, generalUtils.intToStr(generalUtils.strToInt(year) - 1), year, unm, sid, uty, men, den, dnm, bnm, workingDir, localDefnsDir, defnsDir, bytesOut);
    }
    else messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "6052", imagesDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 6052, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void verify(Connection ofsaCon, Statement ofsaStmt, Statement ofsaStmt2, Statement ofsaStmt3, ResultSet ofsaRs, ResultSet ofsaRs2, ResultSet ofsaRs3, PrintWriter out, HttpServletRequest req, String lastYear, String year, String unm,
                      String sid, String uty, String men, String den, String dnm, String bnm, String workingDir, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Ledger Against Trial Balance Verification</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    scoutln(out, bytesOut, "function gl(account,dateFrom,dateTo){");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/AccountsViewGLAccountFromTB?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+account+\"&p2=\"+dateFrom+\"&p3=\"+dateTo;}");

    scoutln(out, bytesOut, "function sanitise(code){");
    scoutln(out, bytesOut, "var code2='';var x;var len=code.length;");
    scoutln(out, bytesOut, "for(x=0;x<len;++x)");
    scoutln(out, bytesOut, "if(code.charAt(x)=='#')code2+='%23';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='\"')code2+='%22';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='&')code2+='%26';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='%')code2+='%25';");
    scoutln(out, bytesOut, "else if(code.charAt(x)==' ')code2+='%20';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='?')code+='%3F';");
    scoutln(out, bytesOut, "else code2+=code.charAt(x);");
    scoutln(out, bytesOut, "return code2};");

    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(ofsaCon, ofsaStmt, ofsaRs, unm, dnm, defnsDir) + "general.css\">");

    int[] hmenuCount = new int[1];

    pageFrameUtils.outputPageFrame(ofsaCon, ofsaStmt, ofsaRs, out, req, "6052", "", "AccountsLedgerTB", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Ledger Against Trial Balance Verification", "6052", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<form><table id='page' width=100% border=0>");
    scoutln(out, bytesOut, "<tr id='pageColumn'><td><p>Type</td><td><p>Code</td><td><p>Name</td><td align=right><p>GL Amount</td><td align=right><p>Trial Balance</td></tr>");

    String userName = directoryUtils.getMySQLUserName();
    String passWord = directoryUtils.getMySQLPassWord();

    Class.forName("com.mysql.jdbc.Driver").newInstance();

    Connection conThisYear = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_accounts_" + year + "?user=" + userName + "&password=" + passWord);
    Statement stmtNewYear = null;
    ResultSet rsNewYear   = null;

    String[] financialYearStartMonth = new String[1];
    String[] financialYearEndMonth   = new String[1];
    definitionTables.getAppConfigFinancialYearMonths(ofsaCon, ofsaStmt, ofsaRs, dnm, financialYearStartMonth, financialYearEndMonth);

    String financialYearEndDate = generalUtils.lastDayOfMonthYYYYMMDD(year + "-" + generalUtils.detMonthNumFromMonthName(financialYearEndMonth[0]) + "-01");
    String financialYearStartDate = year + "-" + generalUtils.detMonthNumFromMonthName(financialYearStartMonth[0]) + "-01";

    String lastFinancialYearEndDate = generalUtils.lastDayOfMonthYYYYMMDD(lastYear + "-" + generalUtils.detMonthNumFromMonthName(financialYearEndMonth[0]) + "-01");
    String lastFinancialYearStartDate = lastYear + "-" + generalUtils.detMonthNumFromMonthName(financialYearStartMonth[0]) + "-01";

    int effectiveStartDate = generalUtils.encode(definitionTables.getAppConfigEffectiveStartDate(ofsaCon, ofsaStmt, ofsaRs, dnm), localDefnsDir, defnsDir);

    String theEffectiveStartDate;
    if(effectiveStartDate > generalUtils.encodeFromYYYYMMDD(financialYearStartDate))
      theEffectiveStartDate = generalUtils.decodeToYYYYMMDD(effectiveStartDate);
    else theEffectiveStartDate = financialYearStartDate;

    String baseCurrency = accountsUtils.getBaseCurrency(ofsaCon, ofsaStmt, ofsaRs, dnm, localDefnsDir, defnsDir);

    int numCustomers = customer.countCustomers(ofsaCon, ofsaStmt, ofsaRs);
    int numSuppliers = supplier.countSuppliers(ofsaCon, ofsaStmt, ofsaRs);

    String[]   debtorAccCodes  = new String[numCustomers];

    String[]   creditorAccCodes  = new String[numSuppliers];

    accountsGenerateTBReport.primeCustomers(ofsaCon, ofsaStmt, ofsaRs, debtorAccCodes);
    accountsGenerateTBReport.primeSuppliers(ofsaCon, ofsaStmt, ofsaRs, creditorAccCodes);


    String[][] accCodes = new String[1][];
    double[][][] accValsDr = new double[1][][];
    double[][][] accValsCr = new double[1][][];
    int[] numAccs = new int[1];
    char[][] accTypes = new char[1][];

    int[] monthEnds = new int[13];
    int[] numMonths = new int[1];

    double[][] debtorAccValsDr = new double[14][numCustomers];
    double[][] debtorAccValsCr = new double[14][numCustomers];

    double[][] creditorAccValsDr = new double[14][numSuppliers];
    double[][] creditorAccValsCr = new double[14][numSuppliers];

    allAccounts(conThisYear, ofsaCon, stmtNewYear, ofsaStmt, rsNewYear, ofsaRs, "G", baseCurrency, theEffectiveStartDate, financialYearEndDate, 0, financialYearStartDate, lastFinancialYearStartDate, accCodes, accValsDr, accValsCr, numAccs,
                accTypes, numCustomers, numSuppliers, debtorAccCodes, creditorAccCodes, debtorAccValsDr, debtorAccValsCr, creditorAccValsDr, creditorAccValsCr, monthEnds, numMonths, dnm);

//    double currentEarnings = getCurrentEarnings(lastYear, lastEffectiveStartDate, lastFinancialYearEndDate, lastFinancialYearStartDate, previousLastFinancialYearStartDate, unm, dnm, workingDir, localDefnsDir, defnsDir);

    stmtNewYear = conThisYear.createStatement();

    rsNewYear = stmtNewYear.executeQuery("SELECT AccCode, Type, Category, Currency, Description FROM acctdefn WHERE Active = 'Y' ORDER BY AccCode");

    String category, accCode, desc;
    double value, ob, tbValue;

    String tmpTable = unm + "_tmp";

    directoryUtils.createTmpTable(true, ofsaCon, ofsaStmt, "Type char(1), Code char(20), Reference char(20), Description char(100), Total decimal(19,8), BaseTotal decimal(19,8), Date date, Currency char(3)", "", tmpTable);

    String otherDebtorsAccount   = accountsUtils.getOtherDebtorsAccCode(year, dnm, localDefnsDir, defnsDir);
    String otherCreditorsAccount = accountsUtils.getOtherCreditorsAccCode(year, dnm, localDefnsDir, defnsDir);
    double[] totalDRs = new double[1];
    double[] totalCRs = new double[1];
    double[] balance     = new double[1];
    String[] batchCode = new String[1];
    String[] cssFormat = new String[1]; cssFormat[0] = "";

    while(rsNewYear.next())
    {
      {
        category = rsNewYear.getString(3);
        desc     = rsNewYear.getString(5);

        {
          accCode  = rsNewYear.getString(1);
          desc     = rsNewYear.getString(5);

          getOBIssueValue(conThisYear, ofsaCon, ofsaStmt, ofsaStmt2, ofsaRs, ofsaRs2, category, accCode, year, theEffectiveStartDate, financialYearEndDate, financialYearStartDate, lastFinancialYearStartDate, unm, dnm, baseCurrency,
                          otherDebtorsAccount, otherCreditorsAccount, workingDir, localDefnsDir, defnsDir, tmpTable, totalDRs, totalCRs);

          totalDRs[0] = generalUtils.doubleDPs(totalDRs[0], '2');
          totalCRs[0] = generalUtils.doubleDPs(totalCRs[0], '2');

          value = generalUtils.doubleDPs((totalDRs[0] - totalCRs[0]), '2');

          tbValue = getTBBalance(accCode, accCodes[0], accValsDr[0][13], accValsCr[0][13], numAccs[0]);

          check(out, "General", value, tbValue, accCode, desc, theEffectiveStartDate, financialYearEndDate, cssFormat, bytesOut);

          directoryUtils.clearTmpTable(ofsaCon, ofsaStmt, tmpTable);
        }
      }
    }

    if(rsNewYear   != null) rsNewYear.close();
    if(stmtNewYear != null) stmtNewYear.close();

    if(conThisYear   != null) conThisYear.close();

    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(ofsaCon, ofsaStmt, ofsaRs, unm, dnm, bnm, localDefnsDir, defnsDir));
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private double getTBBalance(String accCode, String[] accCodes, double[] accValsDr, double[] accValsCr, int numAccs) throws Exception
  {
    int x=0;

    while(x < numAccs && ! accCodes[x].equals(accCode))
      ++x;

    if(x == numAccs) // accCode not found!
      return 0.0;

    double dr = generalUtils.doubleDPs(accValsDr[x], '2');
    double cr = generalUtils.doubleDPs(accValsCr[x], '2');

    return generalUtils.doubleDPs((dr - cr), '2');
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void getOBIssueValue(Connection accCon, Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String category, String account, String year, String dateFrom, String dateTo, String financialYearStartDate,
                               String lastFinancialYearStartDate, String unm, String dnm, String baseCurrency, String otherDebtorsAccount, String otherCreditorsAccount, String workingDir, String localDefnsDir, String defnsDir,
                               String tmpTable, double[] totalDRs, double[] totalCRs) throws Exception
  {
    if(category.equals("Current Assets - GST Clearing"))
    {
      accountsGLGSTClearing.calc(con, stmt, stmt2, rs, account, year, dateFrom, dateTo, dnm, localDefnsDir, defnsDir, tmpTable);
      accountsGLGSTClearing.processTmpTableForOBs(con, stmt, rs, tmpTable, totalDRs, totalCRs);
    }
    else
    if(category.equals("Liabilities - GST Input"))
    {
      accountsGLGSTInput.calc(con, stmt, stmt2, rs, account, year, dateFrom, dateTo, dnm, localDefnsDir, defnsDir, tmpTable);
      accountsGLGSTInput.processTmpTableForOBs(con, stmt, rs, tmpTable, totalDRs, totalCRs);
    }
    else
    if(category.equals("Liabilities - GST Output"))
    {
      accountsGLGSTOutput.calc(con, stmt, stmt2, rs, account, year, dateFrom, dateTo, dnm, localDefnsDir, defnsDir, tmpTable);
      accountsGLGSTOutput.processTmpTableForOBs(con, stmt, rs, tmpTable, totalDRs, totalCRs);
    }
    else
    if(category.equals("Current Assets - Other Debtors"))
    {
      accountsGLOtherDebtors.calc(con, stmt, stmt2, rs, rs2, account, year, otherDebtorsAccount, dateFrom, dateTo, dnm, localDefnsDir, defnsDir, tmpTable);
      accountsGLOtherDebtors.processTmpTableForOBs(con, stmt, rs, tmpTable, totalDRs, totalCRs);
    }
    else
    if(category.equals("Liabilities - Other Creditors"))
    {
      accountsGLOtherCreditors.calc(con, stmt, stmt2, rs, rs2, account, year, otherCreditorsAccount, dateFrom, dateTo, dnm, localDefnsDir, defnsDir, tmpTable);
      accountsGLOtherCreditors.processTmpTableForOBs(con, stmt, rs, tmpTable, totalDRs, totalCRs);
    }
    else
    if(category.equals("Current Assets - Cash"))
    {
      accountsGLBankTransactions.calc(con, stmt, stmt2, rs, account, year, dateFrom, dateTo, baseCurrency, dnm, localDefnsDir, defnsDir, tmpTable);
      String accountCurrency = accountsUtils.getAccountCurrencyGivenAccCode(account, year, dnm, localDefnsDir, defnsDir);
      accountsGLBankTransactions.processTmpTableForOBs(con, stmt, rs, tmpTable, accountCurrency, baseCurrency, totalDRs, totalCRs);
    }
    else
    if(category.equals("Current Assets - Bank"))
    {
      accountsGLBankTransactions.calc(con, stmt, stmt2, rs, account, year, dateFrom, dateTo, baseCurrency, dnm, localDefnsDir, defnsDir, tmpTable);
      String accountCurrency = accountsUtils.getAccountCurrencyGivenAccCode(account, year, dnm, localDefnsDir, defnsDir);
      accountsGLBankTransactions.processTmpTableForOBs(con, stmt, rs, tmpTable, accountCurrency, baseCurrency, totalDRs, totalCRs);
    }
    else
    if(category.equals("Current Assets - Recoverable Expenses"))
    {
      accountsGLRecoverableExpenses.calc(con, stmt, stmt2, rs, rs2, account, year, dateFrom, dateTo, dnm, localDefnsDir, defnsDir, tmpTable);
      accountsGLRecoverableExpenses.processTmpTableForOBs(con, stmt, rs, tmpTable, totalDRs, totalCRs);
    }
    else
    if(category.equals("Current Assets - Contra"))
    {
      accountsGLCurrentAssetsContra.calc(con, stmt, stmt2, rs, account, year, dateFrom, dateTo, dnm, localDefnsDir, defnsDir, tmpTable);
      accountsGLCurrentAssetsContra.processTmpTableForOBs(con, stmt, rs, tmpTable, totalDRs, totalCRs);
    }
    else
    if(category.equals("Current Assets - PrePayments"))
    {
      accountsGLCurrentAssetsPrePayments.calc(con, stmt, stmt2, rs, rs2, account, year, dateFrom, dateTo, dnm, localDefnsDir, defnsDir, tmpTable);
      accountsGLCurrentAssetsPrePayments.processTmpTableForOBs(con, stmt, rs, tmpTable, totalDRs, totalCRs);
    }
    else
    if(category.equals("Liabilities - PrePayments"))
    {
      accountsGLLiabilitiesPrePayments.calc(con, stmt, stmt2, rs, rs2, account, year, dateFrom, dateTo, dnm, localDefnsDir, defnsDir, tmpTable);
      accountsGLLiabilitiesPrePayments.processTmpTableForOBs(con, stmt, rs, tmpTable, totalDRs, totalCRs);
    }
    else
    if(  category.equals("Liabilities") || category.equals("Liabilities - Suspense") || category.equals("Liabilities - Provision for Income Tax") || category.equals("Liabilities - Proposed Dividend"))
    {
      accountsGLLiabilities.calc(con, stmt, stmt2, rs, account, year, dateFrom, dateTo, dnm, localDefnsDir, defnsDir, tmpTable);
      accountsGLLiabilities.processTmpTableForOBs(con, stmt, rs, tmpTable, totalDRs, totalCRs);
    }
    else
    if(category.equals("Liabilities - Accruals"))
    {
      accountsGLLiabilitiesAccruals.calc(con, stmt, stmt2, rs, account, year, dateFrom, dateTo, dnm, localDefnsDir, defnsDir, tmpTable);
      String accountCurrency = accountsUtils.getAccountCurrencyGivenAccCode(account, year, dnm, localDefnsDir, defnsDir);
      accountsGLLiabilitiesAccruals.processTmpTableForOBs(con, stmt, rs, tmpTable, accountCurrency, baseCurrency, totalDRs, totalCRs);
    }
    else
    if(category.equals("Equity - Current Earnings") || category.equals("Equity - Retained Earnings") || category.equals("Fixed Assets - Accumulated Depreciation"))
    {
      accountsGLEquityEarnings.calc(con, stmt, stmt2, rs, account, year, dateFrom, dateTo, dnm, localDefnsDir, defnsDir, tmpTable);
      accountsGLEquityEarnings.processTmpTableForOBs(con, stmt, rs, tmpTable, totalDRs, totalCRs);
    }
    else
    if(category.equals("Current Assets - Currency Exchange"))
    {
      accountsGLEquityCurrencyExchange.calc(con, stmt, stmt2, rs, account, year, dateFrom, dateTo, dnm, localDefnsDir, defnsDir, tmpTable);
      String accountCurrency = accountsUtils.getAccountCurrencyGivenAccCode(account, year, dnm, localDefnsDir, defnsDir);
      accountsGLEquityCurrencyExchange.processTmpTableForOBs(con, stmt, rs, tmpTable, accountCurrency, baseCurrency, totalDRs, totalCRs);
    }
    else
    if(category.equals("Current Assets - Stock"))
    {
      analyticsStock.calc(con, accCon, stmt, stmt2, rs, rs2, account, year, dateFrom, dateTo, financialYearStartDate, lastFinancialYearStartDate, unm, dnm, workingDir, localDefnsDir, defnsDir, tmpTable);
      analyticsStock.processTmpTableForOBs(con, stmt, rs, tmpTable, totalDRs, totalCRs);
    }
    else
    if(category.equals("Current Assets - WIP"))
    {
      analyticsWIP.calc(con, accCon, stmt, stmt2, rs, rs2, account, year, dateFrom, dateTo, financialYearStartDate, lastFinancialYearStartDate, unm, dnm, workingDir, localDefnsDir, defnsDir, tmpTable);
      analyticsWIP.processTmpTableForOBs(con, stmt, rs, tmpTable, totalDRs, totalCRs);
    }
    else
    if(category.equals("Fixed Assets") || category.equals("Fixed Assets - Investment") || category.equals("Equity") || category.equals("Current Assets") || category.equals("Current Assets - OverPayments") || category.startsWith("PL Provision"))
    {
      accountsGLFixedAssetsEquity.calc(con, stmt, stmt2, rs, rs2, account, year, dateFrom, dateTo, dnm, localDefnsDir, defnsDir, tmpTable);
      accountsGLFixedAssetsEquity.processTmpTableForOBs(con, stmt, rs, tmpTable, totalDRs, totalCRs);
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void calcDebtors(Connection accCon, Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, ResultSet rs3, String companyCode, String dateFrom, String dateTo, double[] outstanding) throws Exception
  {
    outstanding[0] = 0.0;

    onBatch(accCon, stmt, rs, "D", companyCode, dateFrom, dateTo, outstanding);

    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT InvoiceCode, TotalTotal FROM invoice WHERE CompanyCode = '" + companyCode + "' AND Status != 'C' AND Settled != 'Y' AND Date <= {d '" + dateTo + "'}");

      String code, amount;
      double amountF, amountReceived, diff;

      while(rs.next())
      {
        code   = rs.getString(1);
        amount = rs.getString(2);

        amountF = generalUtils.doubleDPs(generalUtils.doubleFromStr(amount), '2');

        amountReceived = calcReceiptLines(con, stmt2, rs2, code, dateTo);

        amountReceived += calcSalesCreditNoteLines(con, stmt2, stmt3, rs2, rs3, code, dateTo);

        diff = generalUtils.doubleDPs((amountF - amountReceived), '2');

        if(diff != 0.0) // underpayment or overpayment
          outstanding[0] += diff;
      }

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();

      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT DNCode, TotalTotal FROM debit WHERE CompanyCode = '" + companyCode + "' AND Status != 'C' AND Settled != 'Y' AND Date <= {d '" + dateTo + "'}");

      while(rs.next())
      {
        code   = rs.getString(1);
        amount = rs.getString(2);

        amountF = generalUtils.doubleDPs(generalUtils.doubleFromStr(amount), '2');

        amountReceived = calcReceiptLines(con, stmt2, rs2, code, dateTo);

        amountReceived += calcSalesCreditNoteLines(con, stmt2, stmt3, rs2, rs3, code, dateTo);

        diff = generalUtils.doubleDPs((amountF - amountReceived), '2');

        if(diff != 0.0) // underpayment or overpayment
          outstanding[0] += diff;
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

    calcAnyCNsUnattached(con, stmt, stmt2, rs, rs2, companyCode, dateTo, outstanding);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private double calcReceiptLines(Connection con, Statement stmt, ResultSet rs, String invoiceCode, String dateTo) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.AmountReceived FROM receiptl AS t2 INNER JOIN receipt AS t1 ON t1.ReceiptCode = t2.ReceiptCode WHERE t2.InvoiceCode = '" + invoiceCode + "' AND t1.Status != 'C' AND t1.DateReceived <= {d '" + dateTo + "'}");

    double amountF = 0.0;

    while(rs.next())
      amountF += generalUtils.doubleFromStr(rs.getString(1));

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    return generalUtils.doubleDPs(amountF, '2');
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private double calcSalesCreditNoteLines(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String invoiceCode, String dateTo) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.Amount2, t2.GSTRate FROM creditl AS t2 INNER JOIN credit AS t1 ON t1.CNCode = t2.CNCode WHERE t2.InvoiceCode = '" + invoiceCode + "' AND t1.Status != 'C' AND t1.Date <= {d '" + dateTo + "'}");

    String gstRate;
    double amtF, gstF, amountF = 0.0, gstRateF;

    while(rs.next())
    {
      amtF    = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(1)), '2');
      gstRate = rs.getString(2);

      if(gstRate.length() > 0)
      {
        gstRateF = accountsUtils.getGSTRate(con, stmt2, rs2, gstRate);

        gstF = amtF * gstRateF;
        amtF += gstF;
      }

      amountF += amtF;
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    return generalUtils.doubleDPs(amountF, '2');
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void calcAnyCNsUnattached(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String companyCode, String dateTo, double[] outstanding) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT COUNT(*) as rowcount FROM credit");

    int rowCount = 0;

    if(rs.next())
      rowCount = rs.getInt("rowcount");

    if(rs  != null) rs.close();
    if(stmt != null) stmt.close();

    if(rowCount == 0)
      return;

    stmt = con.createStatement();

    // chk CNs for entries without valid invoice codes (blank entries or non-existant invoices)
    rs = stmt.executeQuery("SELECT t2.Amount2, t2.GSTRate FROM credit AS t1 INNER JOIN creditl AS t2 ON t1.CNCode = t2.CNCode WHERE t1.CompanyCode = '" + companyCode + "' AND t1.Date <= {d '" + dateTo
                         + "'} AND t1.Status != 'C' AND ( t2.InvoiceCode NOT IN (SELECT InvoiceCode FROM invoice) AND t2.InvoiceCode NOT IN (SELECT DNCode FROM debit) )");

    String amount, gstRate;
    double gstF, amtF;

    while(rs.next())
    {
      amount  = rs.getString(1);
      gstRate = rs.getString(2);

      if(gstRate.length() > 0)
      {
        amtF = generalUtils.doubleDPs(generalUtils.doubleFromStr(amount), '2');
        gstF = amtF * accountsUtils.getGSTRate(con, stmt2, rs2, gstRate);
        amtF += gstF;
        amount = generalUtils.doubleToStr(amtF);
      }

      outstanding[0] -= generalUtils.doubleDPs(generalUtils.doubleFromStr(amount), '2');
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void calcCreditors(Connection accCon, Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, ResultSet rs3, String companyCode, String dateFrom, String dateTo, double[] outstanding)
                             throws Exception
  {
    outstanding[0] = 0.0;

    onBatch(accCon, stmt, rs, "C", companyCode, dateFrom, dateTo, outstanding);

    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT InvoiceCode, TotalTotal FROM pinvoice WHERE CompanyCode = '" + companyCode + "' AND Status != 'C' AND Settled != 'Y' AND Date <= {d '" + dateTo + "'}");

      String code, amount;
      double amountF, amountPaid, diff;
      double[] baseAmountPaid = new double[1];

      while(rs.next())
      {
        code   = rs.getString(1);
        amount = rs.getString(2);

        amountF = generalUtils.doubleDPs(generalUtils.doubleFromStr(amount), '2');

        baseAmountPaid[0] = 0.0;

        amountPaid = calcPaymentLines(con, stmt2, rs2, code, dateTo);

        amountPaid += calcPurchaseCreditNoteLines(con, stmt2, stmt3, rs2, rs3, code, dateTo);

        diff = generalUtils.doubleDPs((amountF - amountPaid), '2');

        if(diff != 0.0) // underpayment or overpayment
          outstanding[0] += diff;
      }

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();

      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT PDNCode, TotalTotal FROM pdebit WHERE CompanyCode = '" + companyCode + "' AND Status != 'C' AND Settled != 'Y' AND Date <= {d '" + dateTo + "'}");

      while(rs.next())
      {
        code   = rs.getString(1);
        amount = rs.getString(2);

        amountF = generalUtils.doubleDPs(generalUtils.doubleFromStr(amount), '2');

        amountPaid = calcPaymentLines(con, stmt2, rs2, code, dateTo);

        amountPaid += calcPurchaseCreditNoteLines(con, stmt2, stmt3, rs2, rs3, code, dateTo);

        diff = generalUtils.doubleDPs((amountF - amountPaid), '2');

        if(diff != 0.0) // underpayment or overpayment
          outstanding[0] += diff;
      }

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }

    calcAnyPCNsUnattached(con, stmt, stmt2, rs, rs2, companyCode, dateTo, outstanding);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private double calcPaymentLines(Connection con, Statement stmt, ResultSet rs, String invoiceCode, String dateTo) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.AmountPaid FROM paymentl AS t2 INNER JOIN payment AS t1 ON t1.PaymentCode = t2.PaymentCode WHERE t2.InvoiceCode = '" + invoiceCode + "' AND t1.Status != 'C' AND t1.DatePaid <= {d '" + dateTo + "'}");

    double amountF = 0.0;

    while(rs.next())
      amountF += generalUtils.doubleFromStr(rs.getString(1));

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    return generalUtils.doubleDPs(amountF, '2');
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private double calcPurchaseCreditNoteLines(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String invoiceCode, String dateTo) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.Amount2, t2.GSTRate FROM pcreditl AS t2 INNER JOIN pcredit AS t1 ON t1.PCNCode = t2.PCNCode WHERE t2.InvoiceCode = '" + invoiceCode + "' AND t1.Status != 'C' AND t1.Date <= {d '" + dateTo + "'}");

    String gstRate;
    double amtF, gstF, amountF = 0.0, gstRateF;

    while(rs.next())
    {
      amtF    = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(1)), '2');
      gstRate = rs.getString(2);

      if(gstRate.length() > 0)
      {
        gstRateF = accountsUtils.getGSTRate(con, stmt2, rs2, gstRate);

        gstF = amtF * gstRateF;
        amtF += gstF;
      }

      amountF += amtF;
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    return generalUtils.doubleDPs(amountF, '2');
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void calcAnyPCNsUnattached(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String companyCode, String dateTo, double[] outstanding) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT COUNT(*) as rowcount FROM pcredit");

    int rowCount = 0;

    if(rs.next())
      rowCount = rs.getInt("rowcount");

    if(rs  != null) rs.close();
    if(stmt != null) stmt.close();

    if(rowCount == 0)
      return;

    stmt = con.createStatement();

    // chk CNs for entries without valid invoice codes (blank entries or non-existant invoices)
    rs = stmt.executeQuery("SELECT t2.Amount2, t2.GSTRate FROM pcredit AS t1 INNER JOIN pcreditl AS t2 ON t1.PCNCode = t2.PCNCode WHERE t1.CompanyCode = '" + companyCode + "' AND t1.Date <= {d '" + dateTo
                         + "'} AND t1.Status != 'C' AND ( t2.InvoiceCode NOT IN (SELECT InvoiceCode FROM pinvoice) AND t2.InvoiceCode NOT IN (SELECT PDNCode FROM pdebit) )");

    String amount, gstRate;
    double gstF, amtF;

    while(rs.next())
    {
      amount     = rs.getString(1);
      gstRate    = rs.getString(3);

      if(gstRate.length() > 0)
      {
        amtF = generalUtils.doubleDPs(generalUtils.doubleFromStr(amount), '2');
        gstF = amtF * accountsUtils.getGSTRate(con, stmt2, rs2, gstRate);
        amtF += gstF;
        amount = generalUtils.doubleToStr(amtF);
      }

      outstanding[0] -= generalUtils.doubleDPs(generalUtils.doubleFromStr(amount), '2');
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void onBatch(Connection accCon, Statement accStmt, ResultSet accRs, String type, String companyCode, String dateFrom, String dateTo, double[] balance) throws Exception
  {
    try
    {
      accStmt = accCon.createStatement();

      accRs = accStmt.executeQuery("SELECT t2.DrCr, t2.Amount FROM joubatchl AS t2 INNER JOIN joubatch AS t1 ON t1.Code = t2.Code WHERE t1.Type = '" + type + "' AND t2.AccCode = '" + companyCode + "' AND t2.TransactionDate >= {d '" + dateFrom
                                 + "'} AND t2.TransactionDate <= {d '" + dateTo + "'}                   AND t1.OpeningBalances != 'Y'    ");
      double amount;

      while(accRs.next())
      {
        amount = generalUtils.doubleDPs(generalUtils.doubleFromStr(accRs.getString(2)), '2');

        if(accRs.getString(1).equals("D"))
          balance[0] += amount;
        else balance[0] -= amount;
      }

      if(accRs   != null) accRs.close();
      if(accStmt != null) accStmt.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(accRs   != null) accRs.close();
      if(accStmt != null) accStmt.close();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private double onDebtorsOrCreditorsBatch(Connection accCon, Statement accStmt, ResultSet accRs, String accCode, String dateFrom, String dateTo) throws Exception
  {
    double value = 0.0;

    try
    {
      accStmt = accCon.createStatement();

      accRs = accStmt.executeQuery("SELECT t2.DrCr, t2.Amount FROM joubatchl AS t2 INNER JOIN joubatch AS t1 ON t1.Code = t2.Code WHERE t2.AccCode = '" + accCode + "' AND t2.TransactionDate >= {d '" + dateFrom
                                 + "'} AND t2.TransactionDate <= {d '" + dateTo + "'}");
      double amount;

      while(accRs.next())
      {
        amount = generalUtils.doubleDPs(generalUtils.doubleFromStr(accRs.getString(2)), '2');

        if(accRs.getString(1).equals("D"))
          value += amount;
        else value -= amount;
      }

      if(accRs   != null) accRs.close();
      if(accStmt != null) accStmt.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(accRs   != null) accRs.close();
      if(accStmt != null) accStmt.close();
    }

    return value;
  }


  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private double getOpeningBalance(Connection accCon, Statement accStmt, ResultSet accRs, String type, String accCode, String[] jbCode) throws Exception
  {
    double total = 0.0;

    try
    {
      accStmt = accCon.createStatement();

      accRs = accStmt.executeQuery("SELECT t2.Amount, t2.DrCr, t2.Code FROM joubatchl AS t2 INNER JOIN joubatch AS t1 ON t1.Code = t2.Code WHERE t1.Type = '" + type + "' AND t2.AccCode = '" + accCode + "' AND t1.OpeningBalances = 'Y'");

      String drCr;
      double amount;

      while(accRs.next())
      {
        amount    = generalUtils.doubleDPs(generalUtils.doubleFromStr(accRs.getString(1)), '2');
        drCr      = accRs.getString(2);
        jbCode[0] = accRs.getString(3);

        if(drCr.equals("D"))
          total += amount;
        else total -= amount;
      }

      if(accRs   != null) accRs.close();
      if(accStmt != null) accStmt.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(accRs   != null) accRs.close();
      if(accStmt != null) accStmt.close();
    }

    return total;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void check(PrintWriter out, String which, double value, double tbValue, String accCode, String desc, String dateFrom, String dateTo, String[] cssFormat, int[] bytesOut) throws Exception
  {
      if(cssFormat[0].equals("line1")) cssFormat[0] = "line2"; else cssFormat[0] = "line1";

      scoutln(out, bytesOut, "<tr id='" + cssFormat[0] + "'><td><p>" + which + "</td><td><p>" + accCode + "</td><td><p>" + desc + "</td><td align=right><p><a href=\"javascript:gl('" + accCode + "','" + dateFrom + "','" + dateTo + "')\">" + generalUtils.formatNumeric(value, '2')
                           + "</a></td><td align=right><p>" + generalUtils.formatNumeric(tbValue, '2') + "</td></tr>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {
    out.println(str);
    bytesOut[0] += (str.length() + 2);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void allAccounts(Connection accCon, Connection ofsaCon, Statement accStmt, Statement ofsaStmt, ResultSet accRs, ResultSet ofsaRs, String which, String baseCurrency, String scanDateFrom, String scanDateTo, int firstReqdMonthInList,
                           String financialYearStartDate, String previousFinancialYearStartDate, String[][] accCodes, double[][][] accValsDr, double[][][] accValsCr, int[] numAccs, char[][] accTypes, int numCustomers, int numSuppliers,
                           String[] debtorAccCodes, String[] creditorAccCodes, double[][] debtorAccValsDr, double[][] debtorAccValsCr, double[][] creditorAccValsDr, double[][] creditorAccValsCr, int[] monthEnds, int[] numMonths, String dnm) throws Exception
  {
    numAccs[0] = 0;

    try
    {
      accStmt = accCon.createStatement();

      accRs = accStmt.executeQuery("SELECT COUNT(*) AS rowcount FROM acctdefn WHERE Active = 'Y'");

      accRs.next();
      numAccs[0] = accRs.getInt("rowcount");

      if(accRs   != null) accRs.close();
      if(accStmt != null) accStmt.close();

      accCodes[0] = new String[numAccs[0]];
      accTypes[0] = new char[numAccs[0]];

      accValsDr[0] = new double[14][numAccs[0]];
      accValsCr[0] = new double[14][numAccs[0]];

      String debtorsAccount = "", creditorsAccount = "", otherDebtorsAccount = "", otherCreditorsAccount = "", salesAccount = "", purchasesAccount = "", gstOutputAccount = "", gstInputAccount = "", exchangeGainAccount = "",
             exchangeLossAccount = "", salesReturnedAccount = "", purchasesReturnedAccount = "", stockAccount = "", discountReceivedAccount = "", discountGivenAccount = "";

      accStmt = accCon.createStatement();

      accRs = accStmt.executeQuery("SELECT AccCode, Category, Type FROM acctdefn WHERE Active = 'Y' ORDER BY AccCode");

      int x, y, count = 0;
      String category;

      while(accRs.next())
      {
        accCodes[0][count] = accRs.getString(1);
        category           = accRs.getString(2);
        accTypes[0][count] = accRs.getString(3).charAt(0);

        if(category.equals("Current Assets - Trade Debtors"))
          debtorsAccount = accRs.getString(1);
        else
        if(category.equals("Current Assets - Other Debtors"))
          otherDebtorsAccount = accRs.getString(1);
        else
        if(category.equals("Liabilities - Trade Creditors"))
          creditorsAccount = accRs.getString(1);
        else
        if(category.equals("Liabilities - Other Creditors"))
          otherCreditorsAccount = accRs.getString(1);
        else
        if(category.equals("Income - Sales"))
          salesAccount = accRs.getString(1);
        else
        if(category.equals("Income - Sales Returned"))
          salesReturnedAccount = accRs.getString(1);
        else
        if(category.equals("Cost of Sales - Purchases"))
          purchasesAccount = accRs.getString(1);
        else
        if(category.equals("Cost of Sales - Purchases Returned"))
          purchasesReturnedAccount = accRs.getString(1);
        else
        if(category.equals("Liabilities - GST Output"))
          gstOutputAccount = accRs.getString(1);
        else
        if(category.equals("Liabilities - GST Input"))
          gstInputAccount = accRs.getString(1);
        else
        if(category.equals("Income - Exchange Gain"))
          exchangeGainAccount = accRs.getString(1);
        else
        if(category.equals("Cost of Sales - Exchange Loss"))
          exchangeLossAccount = accRs.getString(1);
        else
        if(category.equals("Current Assets - Stock"))
          stockAccount = accRs.getString(1);
        else
        if(category.equals("Cost of Sales - Discount Received"))
          discountReceivedAccount = accRs.getString(1);
        else
        if(category.equals("Income - Discount Given"))
          discountGivenAccount = accRs.getString(1);

        ++count;
      }

      if(accRs   != null) accRs.close();
      if(accStmt != null) accStmt.close();

      accountsGenerateTBReport.scanDocuments(accCon, ofsaCon, accStmt, ofsaStmt, accRs, ofsaRs, true, scanDateFrom, scanDateTo, financialYearStartDate, previousFinancialYearStartDate, accCodes[0], accValsDr[0], accValsCr[0], numAccs[0], monthEnds, numMonths,
                           debtorsAccount, creditorsAccount, otherDebtorsAccount, otherCreditorsAccount, salesAccount, purchasesAccount, gstOutputAccount, gstInputAccount, exchangeGainAccount, exchangeLossAccount, salesReturnedAccount,
                           purchasesReturnedAccount, stockAccount, discountReceivedAccount, discountGivenAccount, baseCurrency, null, null, null, null, null, null, 0, 0, ""    ,"", dnm);

      // accValsDr and accValsCr have data for all months of the year (from the effectiveStartDate if appropriate)
      // [12] has the OB values from the OB batch(es)
      // Now, months prior (to the required first month to be shown) must be aggregated into [12], then, on display, those months may be ignored

      // the Closing colunm values are derived by totalling all months

      for(y=0;y<numAccs[0];++y)
      {
        accValsDr[0][13][y] = accValsDr[0][12][y];
        accValsCr[0][13][y] = accValsCr[0][12][y];
      }

      for(x=0;x<numMonths[0];++x)
      {
        for(y=0;y<numAccs[0];++y)
        {
          accValsDr[0][13][y] += generalUtils.doubleDPs(accValsDr[0][x][y], '2');
          accValsCr[0][13][y] += generalUtils.doubleDPs(accValsCr[0][x][y], '2');
        }
      }

      if(! which.equals("G")) // if debtors or creditor TB
      {
        for(x=0;x<firstReqdMonthInList;++x)
        {
          for(y=0;y<numCustomers;++y)
          {
            debtorAccValsDr[12][y] += generalUtils.doubleDPs(debtorAccValsDr[x][y], '2');
            debtorAccValsCr[12][y] += generalUtils.doubleDPs(debtorAccValsCr[x][y], '2');
          }

          for(y=0;y<numSuppliers;++y)
          {
            creditorAccValsDr[12][y] += generalUtils.doubleDPs(creditorAccValsDr[x][y], '2');
            creditorAccValsCr[12][y] += generalUtils.doubleDPs(creditorAccValsCr[x][y], '2');
          }
        }

        // the Closing colunm values are derived by totalling all months

        for(y=0;y<numCustomers;++y)
        {
          debtorAccValsDr[13][y] = debtorAccValsDr[12][y];
          debtorAccValsCr[13][y] = debtorAccValsCr[12][y];
        }

        for(y=0;y<numSuppliers;++y)
        {
          creditorAccValsDr[13][y] = creditorAccValsDr[12][y];
          creditorAccValsCr[13][y] = creditorAccValsCr[12][y];
        }

        for(x=0;x<numMonths[0];++x)
        {
          for(y=0;y<numCustomers;++y)
          {
            debtorAccValsDr[13][y] += generalUtils.doubleDPs(debtorAccValsDr[x][y], '2');
            debtorAccValsCr[13][y] += generalUtils.doubleDPs(debtorAccValsCr[x][y], '2');
          }

          for(y=0;y<numSuppliers;++y)
          {
            creditorAccValsDr[13][y] += generalUtils.doubleDPs(creditorAccValsDr[x][y], '2');
            creditorAccValsCr[13][y] += generalUtils.doubleDPs(creditorAccValsCr[x][y], '2');
          }
        }
      }
    }
    catch(Exception e)
    {
      System.out.println(e);
    }
  }

}
