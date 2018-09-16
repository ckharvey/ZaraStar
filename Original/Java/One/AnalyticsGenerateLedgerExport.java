// =======================================================================================================================================================================================================
// System: ZaraStar Analytic: Generate Ledger Export
// Module: AnalyticsGenerateLedgerExport.java
// Author: C.K.Harvey
// Copyright (c) 1999-2008 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.sql.*;

public class AnalyticsGenerateLedgerExport extends HttpServlet
{
  AccountsGLLiabilitiesAccruals accountsGLLiabilitiesAccruals = new AccountsGLLiabilitiesAccruals();
  AnalyticsStock analyticsStock = new AnalyticsStock();
  AccountsGLSales accountsGLSales = new AccountsGLSales();
  AnalyticsPurchases analyticsPurchases = new AnalyticsPurchases();
  GLExchangeGainLoss gLExchangeGainLoss = new GLExchangeGainLoss();
  AccountsGLTradeDebtors accountsGLTradeDebtors = new AccountsGLTradeDebtors();
  AccountsGLExpenses accountsGLExpenses = new AccountsGLExpenses();
  AccountsGLTradeCreditors accountsGLTradeCreditors = new AccountsGLTradeCreditors();
  AccountsGLGSTOutput accountsGLGSTOutput = new AccountsGLGSTOutput();
  AccountsGLGSTInput accountsGLGSTInput = new AccountsGLGSTInput();
  AccountsGLSalesReturned accountsGLSalesReturned = new AccountsGLSalesReturned();
  AccountsGLPurchasesReturned accountsGLPurchasesReturned = new AccountsGLPurchasesReturned();
  AccountsGLFixedAssetsEquity accountsGLFixedAssetsEquity = new AccountsGLFixedAssetsEquity();
  AccountsGLCurrentAssetsContra accountsGLCurrentAssetsContra = new AccountsGLCurrentAssetsContra();
  AccountsGLLiabilities accountsGLLiabilities = new AccountsGLLiabilities();
  AccountsGLIncome accountsGLIncome = new AccountsGLIncome();
  AccountsGLEquityEarnings accountsGLEquityEarnings = new AccountsGLEquityEarnings();
  AccountsGLCostOfSales accountsGLCostOfSales = new AccountsGLCostOfSales();
  AccountsGLRecoverableExpenses accountsGLRecoverableExpenses = new AccountsGLRecoverableExpenses();
  AccountsGLCurrentAssetsPrePayments accountsGLCurrentAssetsPrePayments = new AccountsGLCurrentAssetsPrePayments();
  AccountsGLLiabilitiesPrePayments accountsGLLiabilitiesPrePayments = new AccountsGLLiabilitiesPrePayments();
  AccountsGLGSTClearing accountsGLGSTClearing = new AccountsGLGSTClearing();
  AccountsGLOtherDebtors accountsGLOtherDebtors = new AccountsGLOtherDebtors();
  AccountsGLOtherCreditors accountsGLOtherCreditors = new AccountsGLOtherCreditors();
  AccountsGLBankCharges accountsGLBankCharges = new AccountsGLBankCharges();
  AccountsGLEquityCurrencyExchange accountsGLEquityCurrencyExchange = new AccountsGLEquityCurrencyExchange();
  AccountsGLBankTransactions  accountsGLBankTransactions  = new AccountsGLBankTransactions();
  AnalyticsWIP analyticsWIP = new AnalyticsWIP();
  AccountsGLDiscounts accountsGLDiscounts = new AccountsGLDiscounts();
  
  GeneralUtils  generalUtils = new GeneralUtils();
  MessagePage  messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils  pageFrameUtils = new PageFrameUtils();
  DirectoryUtils  directoryUtils = new DirectoryUtils();
  ReportGenDetails  reportGenDetails = new ReportGenDetails();
  Customer customer = new Customer();
  AccountsUtils  accountsUtils = new AccountsUtils();
  MiscDefinitions  miscDefinitions = new MiscDefinitions();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="", p3="";

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
      p1  = req.getParameter("p1"); // account
      p2  = req.getParameter("p2"); // dateFrom
      p3  = req.getParameter("p3"); // dateTo
      
      if(p1 == null) p1 = "___ALL";
      if(p2 == null) p2 = "01.01.2007";
      if(p3 == null) p3 = "31.12.2007";
      
      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, p3, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "AnalyticsGenerateLedgerExport", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6041, bytesOut[0], 0, "ERR:" + p3);
      if(out != null) out.flush();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1, String p2, String p3, int[] bytesOut) throws Exception
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
    Connection con    = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + userName + "&password=" + passWord);
    Statement stmt = null, stmt2 = null, stmt3 = null;
    ResultSet rs   = null, rs2   = null, rs3   = null;

    String year = generalUtils.getFromDefnFile(unm, "accounts.dfn", workingDir, "");

    String uName = directoryUtils.getMySQLUserName();
    String pWord = directoryUtils.getMySQLPassWord();

    Connection accCon = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_accounts_" + year + "?user=" + uName + "&password=" + pWord);

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 6031, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "6041b", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6041, bytesOut[0], 0, "ACC:" + p3);
      if(con != null) con.close();
      if(accCon != null) accCon.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "6041b", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6041, bytesOut[0], 0, "SID:" + p3);
      if(con != null) con.close();
      if(accCon != null) accCon.close();
      if(out != null) out.flush();
      return;
    }

    String dateFrom = generalUtils.convertDateToSQLFormat(p2);
    String dateTo   = generalUtils.convertDateToSQLFormat(p3);
            
    RandomAccessFile[] fh = new RandomAccessFile[1];

    switch(setup(reportsDir, localDefnsDir, defnsDir, workingDir, fh))
    {
      case -2 : // cannot create report output file
                messagePage.msgScreen(false, out, req, 18, unm, sid, uty, men, den, dnm, bnm, "6041", imagesDir, localDefnsDir, defnsDir, bytesOut);
                break;
      default : // submitted
                messagePage.msgScreen(false, out, req, 41, unm, sid, uty, men, den, dnm, bnm, "6041", imagesDir, localDefnsDir, defnsDir, bytesOut);
                if(out != null) out.flush();
                r824(con, accCon, stmt, stmt2, stmt3, rs, rs2, rs3, year, dateFrom, dateTo, p1, unm, dnm, workingDir, localDefnsDir, defnsDir, fh[0]);
                break;
    }

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 6041, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p2 + ":" + p3);
    if(con    != null) con.close();
    if(accCon != null) accCon.close();
    if(out != null) out.flush();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private int setup(String reportsDir, String localDefnsDir, String defnsDir, String workingDir, RandomAccessFile[] fh) throws Exception
  {
    String[] newName = new String[1];
    if((fh[0] = reportGenDetails.createNewFile((short)824, workingDir, localDefnsDir, defnsDir, reportsDir, newName)) == null)
      return -2;
    
    return 0;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void r824(Connection con, Connection accCon, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, ResultSet rs3, String year, String dateFrom, String dateTo, String account, String unm, String dnm,
                    String workingDir, String localDefnsDir, String defnsDir, RandomAccessFile fh) throws Exception
  {
    byte[] b = new byte[80];

    if(account.equals("___ALL")) generalUtils.strToBytes(b, "GL: " + dateFrom + " to " + dateTo + " (All Accounts)");
    else generalUtils.strToBytes(b, "GL: " + dateFrom + " to " + dateTo + " for Account: " + account);

    writeEntry(fh, "Account",      true);
    writeEntry(fh, "DocumentCode", true);
    writeEntry(fh, "DocumentType", true);
    writeEntry(fh, "Date", true);
    writeEntry(fh, "Reference", true);
    writeEntry(fh, "DrCr", true);
    writeEntry(fh, "Currency", true);
    writeEntry(fh, "Amount", true);
    writeEntry(fh, "BaseAmount", true);
    writeEntry(fh, "MonthBalance", true);
    writeEntry(fh, "BaseBalance", true);
    writeEntry(fh, "Description", true);
    writeEntry(fh, "DebitTotal", true);
    writeEntry(fh, "CreditTotal", false);

    processAccounts(con, accCon, stmt, stmt2, stmt3, rs, rs2, rs3, year, account, dateFrom, dateTo, unm, dnm, workingDir, localDefnsDir, defnsDir, fh);
      
    writeEntry(fh, "---END---", false);

    generalUtils.fileClose(fh);
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void processAccounts(Connection con, Connection accCon, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, ResultSet rs3, String year, String account, String dateFrom, String dateTo, String unm, String dnm,
                               String workingDir, String localDefnsDir, String defnsDir, RandomAccessFile fh) throws Exception
  {

    String baseCurrency = accountsUtils.getBaseCurrency(con, stmt, rs, dnm, localDefnsDir, defnsDir);

    String tmpTable = unm + "_tmp";
    
    String tradeDebtorsAccount   = accountsUtils.getTradeDebtorsAccCode(year, dnm, localDefnsDir, defnsDir);
    String tradeCreditorsAccount = accountsUtils.getTradeDebtorsAccCode(year, dnm, localDefnsDir, defnsDir);
    String otherDebtorsAccount   = accountsUtils.getOtherDebtorsAccCode(year, dnm, localDefnsDir, defnsDir);
    String otherCreditorsAccount = accountsUtils.getOtherDebtorsAccCode(year, dnm, localDefnsDir, defnsDir);
    
    directoryUtils.createTmpTable(true, con, stmt, "Type char(1), Code char(20), Reference char(20), Description char(100), Total decimal(19,8), BaseTotal decimal(19,8), Date date, Currency char(3)", "", tmpTable);

    try
    {
      stmt = accCon.createStatement();

      rs = stmt.executeQuery("SELECT AccCode, Category, Description, Currency, DrCr FROM acctdefn WHERE Active = 'Y' ORDER BY AccCode"); 

      String accCode, category, drCr;

      while(rs.next())                  
      {
        accCode  = rs.getString(1);

        if(account.equals("___ALL") || accCode.equals(account))
        {
          category = rs.getString(2);
          drCr     = rs.getString(5);

          directoryUtils.clearTmpTable(con, stmt, tmpTable);

          listAccount(con, accCon, stmt2, stmt3, rs2, rs3, category, accCode, year, dateFrom, dateTo, unm, dnm, baseCurrency, tradeDebtorsAccount, tradeCreditorsAccount, otherDebtorsAccount, otherCreditorsAccount, fh, workingDir, localDefnsDir,
                      defnsDir, tmpTable);
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
  }  

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void listAccount(Connection con, Connection accCon, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String category, String account, String year, String dateFrom, String dateTo, String unm, String dnm, String baseCurrency,
                           String tradeDebtorsAccount, String tradeCreditorsAccount, String otherDebtorsAccount, String otherCreditorsAccount, RandomAccessFile fh, String workingDir, String localDefnsDir, String defnsDir, String tmpTable)
                           throws Exception
  {
    if(category.equals("Income - Sales"))
    {
      accountsGLSales.calc(con, stmt, stmt2, rs, account, year, dateFrom, dateTo, "S", dnm, localDefnsDir, defnsDir, tmpTable);
      accountsGLSales.processTmpTableForExport(con, stmt, rs, tmpTable, account, fh);
    }
    else
    if(category.startsWith("Income (Operating)"))
    {
      accountsGLSales.calc(con, stmt, stmt2, rs, account, year, dateFrom, dateTo, "C", dnm, localDefnsDir, defnsDir, tmpTable);
      accountsGLSales.processTmpTableForExport(con, stmt, rs, tmpTable, account, fh);
    }
    else
    if(category.equals("Income - Sales Category"))
    {
      accountsGLSales.calc(con, stmt, stmt2, rs, account, year, dateFrom, dateTo, "N", dnm, localDefnsDir, defnsDir, tmpTable);
      accountsGLSales.processTmpTableForExport(con, stmt, rs, tmpTable, account, fh);
    }
    else
    if(category.equals("Cost of Sales - Purchases"))
    {
      analyticsPurchases.calc(con, stmt, stmt2, rs, rs2, account, year, dateFrom, dateTo, "P", unm, dnm, workingDir, localDefnsDir, defnsDir, tmpTable);
      analyticsPurchases.processTmpTableForExport(con, stmt, rs, tmpTable, account, fh);
    }
    else
    if(   category.equals("Cost of Sales - Purchases Category") || category.equals("Cost of Sales - Carriage Outward")
       || category.equals("Cost of Sales - Carriage Inward") || category.equals("Cost of Sales - Delivery and Distribution"))
    {
      analyticsPurchases.calc(con, stmt, stmt2, rs, rs2, account, year, dateFrom, dateTo, "N", unm, dnm, workingDir, localDefnsDir, defnsDir, tmpTable);
      analyticsPurchases.processTmpTableForExport(con, stmt, rs, tmpTable, account, fh);
    }
    else
    if(category.equals("Income - Exchange Gain"))
    {
      gLExchangeGainLoss.calc(con, stmt, stmt2, rs, account, year, dateFrom, dateTo, dnm, localDefnsDir, defnsDir, tmpTable);
      gLExchangeGainLoss.processTmpTableForExport(con, stmt, rs, "G", tmpTable, account, fh);
    }
    else
    if(category.equals("Cost of Sales"))
    {
      accountsGLCostOfSales.calc(con, stmt, stmt2, rs, account, year, dateFrom, dateTo, dnm, localDefnsDir, defnsDir, tmpTable);
      accountsGLCostOfSales.processTmpTableForExport(con, stmt, rs, tmpTable, account, fh);
    }
    else
    if(category.equals("Cost of Sales - Discount Received"))
    {
      accountsGLDiscounts.calc(con, stmt, stmt2, rs, account, year, dateFrom, dateTo, "R", baseCurrency, dnm, localDefnsDir, defnsDir, tmpTable);
      accountsGLDiscounts.processTmpTableForExport(con, stmt, rs, tmpTable, account, fh);
    }
    else
    if(category.equals("Cost of Sales - Exchange Loss"))
    {
      gLExchangeGainLoss.calc(con, stmt, stmt2, rs, account, year, dateFrom, dateTo, dnm, localDefnsDir, defnsDir, tmpTable);
      gLExchangeGainLoss.processTmpTableForExport(con, stmt, rs, "L", tmpTable, account, fh);
    }
    else
    if(category.equals("Current Assets - GST Clearing"))
    {
      accountsGLGSTClearing.calc(con, stmt, stmt2, rs, account, year, dateFrom, dateTo, dnm, localDefnsDir, defnsDir, tmpTable);
      accountsGLGSTClearing.processTmpTableForExport(con, stmt, rs, tmpTable, account, fh);
    }
    else
    if(category.equals("Liabilities - GST Input"))
    {
      accountsGLGSTInput.calc(con, stmt, stmt2, rs, account, year, dateFrom, dateTo, dnm, localDefnsDir, defnsDir, tmpTable);
      accountsGLGSTInput.processTmpTableForExport(con, stmt, rs, tmpTable, account, fh);
    }
    else
    if(category.equals("Liabilities - GST Output"))
    {
      accountsGLGSTOutput.calc(con, stmt, stmt2, rs, account, year, dateFrom, dateTo, dnm, localDefnsDir, defnsDir, tmpTable);
      accountsGLGSTOutput.processTmpTableForExport(con, stmt, rs, tmpTable, account, fh);
    }
    else
    if(category.equals("Current Assets - Trade Debtors"))
    {
      String[][] accCurrencies = new String[1][];
      int numCurrencies = accountsUtils.getCurrencyNames(con, stmt, rs, accCurrencies, dnm, localDefnsDir, defnsDir);
      
      for(int x=0;x<numCurrencies;++x)
      {
        accountsGLTradeDebtors.calc(con, stmt, stmt2, rs, rs2, accCurrencies[0][x], baseCurrency, year, tradeDebtorsAccount, dateFrom, dateTo, dnm, localDefnsDir, defnsDir, tmpTable);
        accountsGLTradeDebtors.processTmpTableForExport(con, stmt, rs, tmpTable, account, fh);
      }
    }
    else
    if(category.equals("Current Assets - Other Debtors"))
    {
      accountsGLOtherDebtors.calc(con, stmt, stmt2, rs, rs2, account, year, otherDebtorsAccount, dateFrom, dateTo, dnm, localDefnsDir, defnsDir, tmpTable);
      accountsGLOtherDebtors.processTmpTableForExport(con, stmt, rs, tmpTable, account, fh);
    }
    else
    if(category.equals("Liabilities - Trade Creditors"))
    {
      String[][] accCurrencies = new String[1][];
      int numCurrencies = accountsUtils.getCurrencyNames(con, stmt, rs, accCurrencies, dnm, localDefnsDir, defnsDir);
      
      for(int x=0;x<numCurrencies;++x)
      {
        accountsGLTradeCreditors.calc(con, stmt, stmt2, rs, rs2, accCurrencies[0][x], baseCurrency, year, tradeCreditorsAccount, dateFrom, dateTo, dnm, localDefnsDir, defnsDir, tmpTable);
        accountsGLTradeCreditors.processTmpTableForExport(con, stmt, rs, tmpTable, account, fh);
      }
    }
    else
    if(category.equals("Liabilities - Other Creditors"))
    {
      accountsGLOtherCreditors.calc(con, stmt, stmt2, rs, rs2, account, year, otherCreditorsAccount, dateFrom, dateTo, dnm, localDefnsDir, defnsDir, tmpTable);
      accountsGLOtherCreditors.processTmpTableForExport(con, stmt, rs, tmpTable, account, fh);
    }
    else
    if(category.equals("Income - Sales Returned"))
    {
      accountsGLSalesReturned.calc(con, stmt, stmt2, rs, account, year, dateFrom, dateTo, dnm, localDefnsDir, defnsDir, tmpTable);
      accountsGLSalesReturned.processTmpTableForExport(con, stmt, rs, tmpTable, account, fh);
    }
    else
    if(category.equals("Cost of Sales - Purchases Returned"))
    {
      accountsGLPurchasesReturned.calc(con, stmt, stmt2, rs, account, year, dateFrom, dateTo, dnm, localDefnsDir, defnsDir, tmpTable);
      accountsGLPurchasesReturned.processTmpTableForExport(con, stmt, rs, tmpTable, account, fh);
    }
    else
    if(category.equals("Current Assets - Cash"))
    { 
      accountsGLBankTransactions.calc(con, stmt, stmt2, rs, account, year, dateFrom, dateTo, baseCurrency, dnm, localDefnsDir, defnsDir, tmpTable);
      String accountCurrency = accountsUtils.getAccountCurrencyGivenAccCode(account, year, dnm, localDefnsDir, defnsDir);
      accountsGLBankTransactions.processTmpTableForExport(con, stmt, rs, tmpTable, accountCurrency, baseCurrency, account, fh);
    }
    else
    if(category.equals("Current Assets - Bank"))
    {
      accountsGLBankTransactions.calc(con, stmt, stmt2, rs, account, year, dateFrom, dateTo, baseCurrency, dnm, localDefnsDir, defnsDir, tmpTable);
      String accountCurrency = accountsUtils.getAccountCurrencyGivenAccCode(account, year, dnm, localDefnsDir, defnsDir);
      accountsGLBankTransactions.processTmpTableForExport(con, stmt, rs, tmpTable, accountCurrency, baseCurrency, account, fh);
    }
    else
    if(category.equals("Current Assets - Recoverable Expenses"))
    {
      accountsGLRecoverableExpenses.calc(con, stmt, stmt2, rs, rs2, account, year, dateFrom, dateTo, dnm, localDefnsDir, defnsDir, tmpTable);
      accountsGLRecoverableExpenses.processTmpTableForExport(con, stmt, rs, tmpTable, account, fh);
    }
    else
    if(category.equals("Current Assets - Contra"))
    {
      accountsGLCurrentAssetsContra.calc(con, stmt, stmt2, rs, account, year, dateFrom, dateTo, dnm, localDefnsDir, defnsDir, tmpTable);
      accountsGLCurrentAssetsContra.processTmpTableForExport(con, stmt, rs, tmpTable, account, fh);
    }
    else
    if(category.equals("Current Assets - PrePayments"))
    {
      accountsGLCurrentAssetsPrePayments.calc(con, stmt, stmt2, rs, rs2, account, year, dateFrom, dateTo, dnm, localDefnsDir, defnsDir, tmpTable);
      accountsGLCurrentAssetsPrePayments.processTmpTableForExport(con, stmt, rs, tmpTable, account, fh);
    }
    else
    if(category.equals("Liabilities - PrePayments"))
    {
      accountsGLLiabilitiesPrePayments.calc(con, stmt, stmt2, rs, rs2, account, year, dateFrom, dateTo, dnm, localDefnsDir, defnsDir, tmpTable);
      accountsGLLiabilitiesPrePayments.processTmpTableForExport(con, stmt, rs, tmpTable, account, fh);
    }
    else
    if(  category.equals("Liabilities") || category.equals("Liabilities - Suspense") || category.equals("Liabilities - Provision for Income Tax")
       || category.equals("Liabilities - Proposed Dividend"))
    {
      accountsGLLiabilities.calc(con, stmt, stmt2, rs, account, year, dateFrom, dateTo, dnm, localDefnsDir, defnsDir, tmpTable);
      accountsGLLiabilities.processTmpTableForExport(con, stmt, rs, tmpTable, account, fh);
    }
    else
    if(category.equals("Income"))
    {
      accountsGLIncome.calc(con, stmt, stmt2, rs, account, year, dateFrom, dateTo, dnm, localDefnsDir, defnsDir, tmpTable);
      accountsGLIncome.processTmpTableForExport(con, stmt, rs, tmpTable, account, fh);
    }
    else
    if(category.equals("Income - Discount Given"))
    {
      accountsGLDiscounts.calc(con, stmt, stmt2, rs, account, year, dateFrom, dateTo, "G", baseCurrency, dnm, localDefnsDir, defnsDir, tmpTable);
      accountsGLDiscounts.processTmpTableForExport(con, stmt, rs, tmpTable, account, fh);
    }
    else
    if(category.equals("Liabilities - Accruals"))
    {
      accountsGLLiabilitiesAccruals.calc(con, stmt, stmt2, rs, account, year, dateFrom, dateTo, dnm, localDefnsDir, defnsDir, tmpTable);
      accountsGLLiabilitiesAccruals.processTmpTableForExport(con, stmt, rs, tmpTable, account, fh);
    }
    else
    if(category.equals("Equity - Current Earnings") || category.equals("Equity - Retained Earnings") || category.equals("Fixed Assets - Accumulated Depreciation"))
    {
      accountsGLEquityEarnings.calc(con, stmt, stmt2, rs, account, year, dateFrom, dateTo, dnm, localDefnsDir, defnsDir, tmpTable);
      accountsGLEquityEarnings.processTmpTableForExport(con, stmt, rs, tmpTable, account, fh);
    }
    else
    if(category.equals("Current Assets - Currency Exchange"))
    {
      accountsGLEquityCurrencyExchange.calc(con, stmt, stmt2, rs, account, year, dateFrom, dateTo, dnm, localDefnsDir, defnsDir, tmpTable);
      String accountCurrency = accountsUtils.getAccountCurrencyGivenAccCode(account, year, dnm, localDefnsDir, defnsDir);
      accountsGLEquityCurrencyExchange.processTmpTableForExport(con, stmt, rs, tmpTable, accountCurrency, baseCurrency, account, fh);
    }
    else
    if(category.equals("Current Assets - Stock"))
    {
      analyticsStock.calc(con, stmt, stmt2, rs, rs2, account, year, dateFrom, dateTo, unm, dnm, workingDir, localDefnsDir, defnsDir, tmpTable);
      analyticsStock.processTmpTableForExport(con, stmt, rs, tmpTable, account, fh);
    }
    else
    if(category.equals("Current Assets - WIP"))
    {
      analyticsWIP.calc(con, stmt, stmt2, rs, rs2, account, year, dateFrom, dateTo, unm, dnm, workingDir, localDefnsDir, defnsDir, tmpTable);
      analyticsWIP.processTmpTableForExport(con, stmt, rs, tmpTable, account, fh);
    }
    else
    if(category.equals("Expenses - Bank Charges"))
    {
      accountsGLBankCharges.calc(con, accCon, stmt, stmt2, rs, account, year, dateFrom, dateTo, dnm, localDefnsDir, defnsDir, tmpTable);
      accountsGLBankCharges.processTmpTableForExport(con, stmt, rs, tmpTable, account, fh);
    }
    else
    if(category.startsWith("Expenses - "))
    {
      accountsGLExpenses.calc(con, stmt, stmt2, rs, account, year, dateFrom, dateTo, dnm, localDefnsDir, defnsDir, tmpTable);
      accountsGLExpenses.processTmpTableForExport(con, stmt, rs, tmpTable, account, fh);
    }
    else
    if(   category.equals("Fixed Assets") || category.equals("Fixed Assets - Investment") || category.equals("Equity")
       || category.equals("Current Assets") || category.equals("Current Assets - OverPayments") || category.startsWith("PL Provision"))
    {
      accountsGLFixedAssetsEquity.calc(con, stmt, stmt2, rs, rs2, account, year, dateFrom, dateTo, dnm, localDefnsDir, defnsDir, tmpTable);
      accountsGLFixedAssetsEquity.processTmpTableForExport(con, stmt, rs, tmpTable, account, fh);
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void writeEntry(RandomAccessFile fh, String s, boolean comma) throws Exception
  {
    fh.writeBytes("\"" + generalUtils.sanitise3(s) + "\"");
    if(comma)
      fh.writeBytes(",");
    else fh.writeBytes("\n");
  }


}
