// =======================================================================================================================================================================================================
// System: ZaraStar Analytic: Generate Ledger Listing
// Module: AnalyticsLedgerListingGenerate.java
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

public class AnalyticsLedgerListingGenerate extends HttpServlet
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
      p1  = req.getParameter("p1"); // format
      p2  = req.getParameter("p2"); // dateFrom
      p3  = req.getParameter("p3"); // dateTo
      
      if(p1 == null) p1 = "1"; 
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "AnalyticsLedgerListingGenerate", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6031, bytesOut[0], 0, "ERR:" + p3);
      if(out != null) out.flush();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                    String p1, String p2, String p3, int[] bytesOut) throws Exception
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

    Connection accCon = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_accounts_" + year + "?user=" + uName + "&password="
                                                    + pWord);

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 1004, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "6031b", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6031, bytesOut[0], 0, "ACC:" + p3);
      if(con != null) con.close();
      if(accCon != null) accCon.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "6031b", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6031, bytesOut[0], 0, "SID:" + p3);
      if(con != null) con.close();
      if(accCon != null) accCon.close();
      if(out != null) out.flush();
      return;
    }

    String dateFrom = generalUtils.convertDateToSQLFormat(p2);
    String dateTo   = generalUtils.convertDateToSQLFormat(p3);
            
    RandomAccessFile[] fhPPR = new RandomAccessFile[1];
    RandomAccessFile[] fhO   = new RandomAccessFile[1];

    switch(setup(p1, reportsDir, localDefnsDir, defnsDir, workingDir, fhPPR, fhO))
    {
      case -1 : // Definition File Not Found
                messagePage.msgScreen(false, out, req, 17, unm, sid, uty, men, den, dnm, bnm, "6031", imagesDir, localDefnsDir, defnsDir, bytesOut);
                break;
      case -2 : // cannot create report output file
                messagePage.msgScreen(false, out, req, 18, unm, sid, uty, men, den, dnm, bnm, "6031", imagesDir, localDefnsDir, defnsDir, bytesOut);
                break;
      default : // submitted
                messagePage.msgScreen(false, out, req, 41, unm, sid, uty, men, den, dnm, bnm, "6031", imagesDir, localDefnsDir, defnsDir, bytesOut);
                if(out != null) out.flush();
                r024(con, accCon, stmt, stmt2, stmt3, rs, rs2, rs3, year, dateFrom, dateTo, unm, dnm, reportsDir, workingDir, localDefnsDir, defnsDir, fhPPR, fhO);
                break;
    }

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 6031, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p2 + ":" + p3);
    if(con    != null) con.close();
    if(accCon != null) accCon.close();
    if(out != null) out.flush();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private int setup(String format, String reportsDir, String localDefnsDir, String defnsDir, String workingDir, RandomAccessFile[] fhPPR,
                    RandomAccessFile[] fhO) throws Exception
  {
    String defnsFile="";
    switch(format.charAt(0))
    {
      case '1' : // by entry (A4)
                 defnsFile = "024a.ppr"; break;
      case '2' : // by entry (wide)
                 defnsFile = "024b.ppr"; break;
//      case '3' : // by date (A4)
//                 defnsFile = "024c.ppr"; break;
//      case '4' : // by date (wide)
//                 defnsFile = "024d.ppr"; break;
    }
    
    String[] newName = new String[1];
    if((fhO[0] = reportGenDetails.createNewFile((short)24, workingDir, localDefnsDir, defnsDir, reportsDir, newName)) == null)
      return -2;

    if((fhPPR[0] = generalUtils.fileOpenD(defnsFile, localDefnsDir)) == null)
    {
      if((fhPPR[0] = generalUtils.fileOpenD(defnsFile, defnsDir)) == null)
        return -1;
    }
    
    return 0;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private int r024(Connection con, Connection accCon, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, ResultSet rs3,
                   String year, String dateFrom, String dateTo, String unm, String dnm, String reportsDir, String workingDir, String localDefnsDir, String defnsDir,
                   RandomAccessFile[] fhPPR, RandomAccessFile[] fhO) throws Exception
  {
    char[] negStyle  = new char[1];
    char[] zeroStyle = new char[1];
    double[] tm             = new double[1];
    double[] bm             = new double[1];
    double[] lm             = new double[1];
    double[] rm             = new double[1];
    double[] pageSizeWidth  = new double[1];
    double[] pageSizeLength = new double[1];
    double[] currDown       = new double[1];
    double[] currAcross     = new double[1];
    short[] dp       = new short[1];
    short[] currFont = new short[1];
    short[] currPage = new short[1];
    short[] oPtr     = new short[1];
    short oBufLen;
    boolean[] lastOperationPF = new boolean[1];
    String[] userFontName   = new String[1];
    String[] userFontStyle  = new String[1];
    String[] userFontItalic = new String[1];
    String[] userFontSize   = new String[1];
    char[] fontDefType   = new char[21];
    char[] fontDefStyle  = new char[21];
    char[] fontDefItalic = new char[21];
    int[]  fontDefSize   = new int[21];
    double[] down2Coord   = new double[1];
    double[] across2Coord = new double[1];
    byte[] oBuf;

    currFont[0] = 1;
    currPage[0] = 1;
    currDown[0] = currAcross[0] = 0.0;

    oBufLen = 30000;
    oBuf = new byte[30000];
    oPtr[0] = 0;

    // title
    byte[] b = new byte[80];
    generalUtils.strToBytes(b, "GL: " + dateFrom + " to " + dateTo);
    reportGenDetails.outputLine('A', b, null, null, (short)0, ' ', dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0], pageSizeWidth[0],
                     pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName, userFontStyle,
                     userFontItalic, userFontSize, fhPPR[0], fhO[0], fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord, across2Coord,
                     oBuf, null);

    
    lastOperationPF[0] = false;

    byte[] fldNames = new byte[10000];
    byte[] data     = new byte[2000];

    generalUtils.putAlpha(fldNames, 10000, (short)0,  "Account.Name");
    generalUtils.putAlpha(fldNames, 10000, (short)1,  "Account.DocumentCode");
    generalUtils.putAlpha(fldNames, 10000, (short)2,  "Account.DocumentType");
    generalUtils.putAlpha(fldNames, 10000, (short)3,  "Account.Date");
    generalUtils.putAlpha(fldNames, 10000, (short)4,  "Account.Reference");
    generalUtils.putAlpha(fldNames, 10000, (short)5,  "Account.DrCr");
    generalUtils.putAlpha(fldNames, 10000, (short)6,  "Account.Currency");
    generalUtils.putAlpha(fldNames, 10000, (short)7,  "Account.Amount");
    generalUtils.putAlpha(fldNames, 10000, (short)8,  "Account.BaseAmount");
    generalUtils.putAlpha(fldNames, 10000, (short)9,  "Account.MonthBalance");
    generalUtils.putAlpha(fldNames, 10000, (short)10, "Account.BaseBalance");
    generalUtils.putAlpha(fldNames, 10000, (short)11, "Account.Description");
    generalUtils.putAlpha(fldNames, 10000, (short)12, "Account.DebitTotal");
    generalUtils.putAlpha(fldNames, 10000, (short)13, "Account.CreditTotal");

    reportGenDetails.processControl(dnm, unm, localDefnsDir, defnsDir, dp, negStyle, zeroStyle, tm, bm, lm, rm, pageSizeWidth, pageSizeLength, currDown,
                         currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName, userFontStyle, userFontItalic, userFontSize,
                         fhPPR[0], fhO[0], fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord, across2Coord, oBuf, null);

    reportGenDetails.processSection("RH", null, null, (short)0, dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0], pageSizeWidth[0],
                         pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName, userFontStyle,
                         userFontItalic, userFontSize, fhPPR[0], fhO[0], fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord,
                         across2Coord, oBuf, null);
    
    reportGenDetails.processSection("PH", data, fldNames, (short)14, dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0], pageSizeWidth[0],
                         pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName, userFontStyle,
                         userFontItalic, userFontSize, fhPPR[0], fhO[0], fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord,
                         across2Coord, oBuf, null);

    processAccounts(con, accCon, stmt, stmt2, stmt3, rs, rs2, rs3, year, dateFrom, dateTo, unm, dnm, workingDir, localDefnsDir, defnsDir, data, fldNames, tm,
                    bm, lm, rm, pageSizeWidth, pageSizeLength, currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName,
                    userFontStyle, userFontItalic, userFontSize, fhPPR, fhO, fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord,
                    across2Coord, oBuf);
      
    reportGenDetails.processSection("PF", data, fldNames, (short)14, dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0], pageSizeWidth[0],
                         pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName, userFontStyle,
                         userFontItalic, userFontSize, fhPPR[0], fhO[0], fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord, across2Coord,
                         oBuf, null);

    reportGenDetails.processSection("RF", data, fldNames, (short)14, dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0], pageSizeWidth[0],
                         pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName, userFontStyle,
                         userFontItalic, userFontSize, fhPPR[0], fhO[0], fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord, across2Coord,
                         oBuf, null);
      
    // EOReport marker
    b[0] = '\000';
    reportGenDetails.outputLine('Z', b, null, null, (short)0, ' ', dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0], pageSizeWidth[0],
                     pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName, userFontStyle,
                     userFontItalic, userFontSize, fhPPR[0], fhO[0], fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord, across2Coord, oBuf, null);

    fhO[0].write(oBuf, 0, oPtr[0]);

    generalUtils.fileClose(fhO[0]);
    generalUtils.fileClose(fhPPR[0]);

    PrintingLayout printingLayout = new PrintingLayout();
    printingLayout.updateNumPages("0.000", reportsDir);

    return 0;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void processAccounts(Connection con, Connection accCon, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2,
                               ResultSet rs3, String year, String dateFrom, String dateTo, String unm, String dnm, String workingDir, String localDefnsDir,
                               String defnsDir, byte[] data, byte[] fldNames, double[] tm, double[] bm, double[] lm, double[] rm,
                               double[] pageSizeWidth, double[] pageSizeLength, double[] currDown, double[] currAcross, short[] currFont,
                               short[] currPage, short[] oPtr, short oBufLen, boolean[] lastOperationPF, String[] userFontName,
                               String[] userFontStyle, String[] userFontItalic, String[] userFontSize, RandomAccessFile[] fhPPR,
                               RandomAccessFile[] fhO, char[] fontDefType, char[] fontDefStyle, char[] fontDefItalic, int[] fontDefSize,
                               double[] down2Coord, double[] across2Coord, byte[] oBuf) throws Exception
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
        category = rs.getString(2);
        drCr     = rs.getString(5);

        directoryUtils.clearTmpTable(con, stmt, tmpTable);

        if(drCr.equals("D"))
          drCr = "Debit";
        else drCr = "Credit";
        
        generalUtils.repAlpha(data, 2000, (short)0, "GL Account: " + accCode + " - " + rs.getString(3) + " (" + rs.getString(4) + " " + drCr + ")");

        reportGenDetails.processSection("BL1", data, fldNames, (short)14, dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0], pageSizeWidth[0],
                         pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName, userFontStyle,
                         userFontItalic, userFontSize, fhPPR[0], fhO[0], fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord,
                         across2Coord, oBuf, null);

        listAccount(con, accCon, stmt2, stmt3, rs2, rs3, category, accCode, year, dateFrom, dateTo, unm, dnm, baseCurrency, tradeDebtorsAccount,
                    tradeCreditorsAccount, otherDebtorsAccount, otherCreditorsAccount, workingDir, localDefnsDir, defnsDir, tmpTable, data, fldNames, tm,
                    bm, lm, rm, pageSizeWidth, pageSizeLength, currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName,
                    userFontStyle, userFontItalic, userFontSize, fhPPR, fhO, fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord,
                    across2Coord, oBuf);
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
  private void listAccount(Connection con, Connection accCon, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String category,
                           String account, String year, String dateFrom, String dateTo, String unm, String dnm, String baseCurrency,
                           String tradeDebtorsAccount, String tradeCreditorsAccount, String otherDebtorsAccount, String otherCreditorsAccount,
                           String workingDir, String localDefnsDir, String defnsDir, String tmpTable, byte[] data, byte[] fldNames, double[] tm, double[] bm,
                           double[] lm, double[] rm, double[] pageSizeWidth, double[] pageSizeLength, double[] currDown, double[] currAcross,
                           short[] currFont, short[] currPage, short[] oPtr, short oBufLen, boolean[] lastOperationPF, String[] userFontName,
                           String[] userFontStyle, String[] userFontItalic, String[] userFontSize, RandomAccessFile[] fhPPR, RandomAccessFile[] fhO,
                           char[] fontDefType, char[] fontDefStyle, char[] fontDefItalic, int[] fontDefSize, double[] down2Coord,
                           double[] across2Coord, byte[] oBuf) throws Exception
  {
    if(category.equals("Income - Sales"))
    {
      accountsGLSales.calc(con, stmt, stmt2, rs, account, year, dateFrom, dateTo, "S", dnm, localDefnsDir, defnsDir, tmpTable);
      accountsGLSales.processTmpTableForListing(con, stmt, rs, tmpTable, unm, dnm, data, fldNames, tm, bm, lm, rm, pageSizeWidth, pageSizeLength,
                                       currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName, userFontStyle,
                                       userFontItalic, userFontSize, fhPPR, fhO, fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord,
                                       across2Coord, oBuf, localDefnsDir, defnsDir);
    }
    else
    if(category.startsWith("Income (Operating)"))
    {
      accountsGLSales.calc(con, stmt, stmt2, rs, account, year, dateFrom, dateTo, "C", dnm, localDefnsDir, defnsDir, tmpTable);
      accountsGLSales.processTmpTableForListing(con, stmt, rs, tmpTable, unm, dnm, data, fldNames, tm, bm, lm, rm, pageSizeWidth, pageSizeLength,
                                       currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName, userFontStyle,
                                       userFontItalic, userFontSize, fhPPR, fhO, fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord,
                                       across2Coord, oBuf, localDefnsDir, defnsDir);
    }
    else
    if(category.equals("Income - Sales Category"))
    {
      accountsGLSales.calc(con, stmt, stmt2, rs, account, year, dateFrom, dateTo, "N", dnm, localDefnsDir, defnsDir, tmpTable);
      accountsGLSales.processTmpTableForListing(con, stmt, rs, tmpTable, unm, dnm, data, fldNames, tm, bm, lm, rm, pageSizeWidth, pageSizeLength,
                                       currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName, userFontStyle,
                                       userFontItalic, userFontSize, fhPPR, fhO, fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord,
                                       across2Coord, oBuf, localDefnsDir, defnsDir);
    }
    else
    if(category.equals("Cost of Sales - Purchases"))
    {
      analyticsPurchases.calc(con, stmt, stmt2, rs, rs2, account, year, dateFrom, dateTo, "P", unm, dnm, workingDir, localDefnsDir, defnsDir, tmpTable);
      analyticsPurchases.processTmpTableForListing(con, stmt, rs, tmpTable, unm, dnm, data, fldNames, tm, bm, lm, rm, pageSizeWidth, pageSizeLength,
                                       currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName, userFontStyle,
                                       userFontItalic, userFontSize, fhPPR, fhO, fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord,
                                       across2Coord, oBuf, localDefnsDir, defnsDir);
    }
    else
    if(   category.equals("Cost of Sales - Purchases Category") || category.equals("Cost of Sales - Carriage Outward")
       || category.equals("Cost of Sales - Carriage Inward") || category.equals("Cost of Sales - Delivery and Distribution"))
    {
      analyticsPurchases.calc(con, stmt, stmt2, rs, rs2, account, year, dateFrom, dateTo, "N", unm, dnm, workingDir, localDefnsDir, defnsDir, tmpTable);
      analyticsPurchases.processTmpTableForListing(con, stmt, rs, tmpTable, unm, dnm, data, fldNames, tm, bm, lm, rm, pageSizeWidth, pageSizeLength,
                                       currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName, userFontStyle,
                                       userFontItalic, userFontSize, fhPPR, fhO, fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord,
                                       across2Coord, oBuf, localDefnsDir, defnsDir);
    }
    else
    if(category.equals("Income - Exchange Gain"))
    {
      gLExchangeGainLoss.calc(con, stmt, stmt2, rs, account, year, dateFrom, dateTo, dnm, localDefnsDir, defnsDir, tmpTable);
      gLExchangeGainLoss.processTmpTableForListing(con, stmt, rs, "G", tmpTable, unm, dnm, data, fldNames, tm, bm, lm, rm, pageSizeWidth, pageSizeLength,
                                       currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName, userFontStyle,
                                       userFontItalic, userFontSize, fhPPR, fhO, fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord,
                                       across2Coord, oBuf, localDefnsDir, defnsDir);
    }
    else
    if(category.equals("Cost of Sales"))
    {
      accountsGLCostOfSales.calc(con, stmt, stmt2, rs, account, year, dateFrom, dateTo, dnm, localDefnsDir, defnsDir, tmpTable);
      accountsGLCostOfSales.processTmpTableForListing(con, stmt, rs, tmpTable, unm, dnm, data, fldNames, tm, bm, lm, rm, pageSizeWidth, pageSizeLength,
                                       currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName, userFontStyle,
                                       userFontItalic, userFontSize, fhPPR, fhO, fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord,
                                       across2Coord, oBuf, localDefnsDir, defnsDir);
    }
    else
    if(category.equals("Cost of Sales - Discount Received"))
    {
      accountsGLDiscounts.calc(con, stmt, stmt2, rs, account, year, dateFrom, dateTo, "R", baseCurrency, dnm, localDefnsDir, defnsDir, tmpTable);
      accountsGLDiscounts.processTmpTableForListing(con, stmt, rs, tmpTable, unm, dnm, data, fldNames, tm, bm, lm, rm, pageSizeWidth, pageSizeLength,
                                       currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName, userFontStyle,
                                       userFontItalic, userFontSize, fhPPR, fhO, fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord,
                                       across2Coord, oBuf, localDefnsDir, defnsDir);
    }
    else
    if(category.equals("Cost of Sales - Exchange Loss"))
    {
      gLExchangeGainLoss.calc(con, stmt, stmt2, rs, account, year, dateFrom, dateTo, dnm, localDefnsDir, defnsDir, tmpTable);
      gLExchangeGainLoss.processTmpTableForListing(con, stmt, rs, "L", tmpTable, unm, dnm, data, fldNames, tm, bm, lm, rm, pageSizeWidth, pageSizeLength,
                                       currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName, userFontStyle,
                                       userFontItalic, userFontSize, fhPPR, fhO, fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord,
                                       across2Coord, oBuf, localDefnsDir, defnsDir);
    }
    else
    if(category.equals("Current Assets - GST Clearing"))
    {
      accountsGLGSTClearing.calc(con, stmt, stmt2, rs, account, year, dateFrom, dateTo, dnm, localDefnsDir, defnsDir, tmpTable);
      accountsGLGSTClearing.processTmpTableForListing(con, stmt, rs, tmpTable, unm, dnm, data, fldNames, tm, bm, lm, rm, pageSizeWidth, pageSizeLength,
                                       currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName, userFontStyle,
                                       userFontItalic, userFontSize, fhPPR, fhO, fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord,
                                       across2Coord, oBuf, localDefnsDir, defnsDir);
    }
    else
    if(category.equals("Liabilities - GST Input"))
    {
      accountsGLGSTInput.calc(con, stmt, stmt2, rs, account, year, dateFrom, dateTo, dnm, localDefnsDir, defnsDir, tmpTable);
      accountsGLGSTInput.processTmpTableForListing(con, stmt, rs, tmpTable, unm, dnm, data, fldNames, tm, bm, lm, rm, pageSizeWidth, pageSizeLength,
                                       currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName, userFontStyle,
                                       userFontItalic, userFontSize, fhPPR, fhO, fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord,
                                       across2Coord, oBuf, localDefnsDir, defnsDir);
    }
    else
    if(category.equals("Liabilities - GST Output"))
    {
      accountsGLGSTOutput.calc(con, stmt, stmt2, rs, account, year, dateFrom, dateTo, dnm, localDefnsDir, defnsDir, tmpTable);
      accountsGLGSTOutput.processTmpTableForListing(con, stmt, rs, tmpTable, unm, dnm, data, fldNames, tm, bm, lm, rm, pageSizeWidth, pageSizeLength,
                                       currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName, userFontStyle,
                                       userFontItalic, userFontSize, fhPPR, fhO, fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord,
                                       across2Coord, oBuf, localDefnsDir, defnsDir);
    }
    else
    if(category.equals("Current Assets - Trade Debtors"))
    {
      String[][] accCurrencies = new String[1][];
      int numCurrencies = accountsUtils.getCurrencyNames(con, stmt, rs, accCurrencies, dnm, localDefnsDir, defnsDir);
      
      for(int x=0;x<numCurrencies;++x)
      {
        accountsGLTradeDebtors.calc(con, stmt, stmt2, rs, rs2, accCurrencies[0][x], baseCurrency, year, tradeDebtorsAccount, dateFrom, dateTo, dnm, localDefnsDir, defnsDir,
                    tmpTable);
        accountsGLTradeDebtors.processTmpTableForListing(con, stmt, rs, tmpTable, unm, dnm, data, fldNames, tm, bm, lm, rm, pageSizeWidth, pageSizeLength,
                                         currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName, userFontStyle,
                                         userFontItalic, userFontSize, fhPPR, fhO, fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord,
                                         across2Coord, oBuf, localDefnsDir, defnsDir);
      }
    }
    else
    if(category.equals("Current Assets - Other Debtors"))
    {
      accountsGLOtherDebtors.calc(con, stmt, stmt2, rs, rs2, account, year, otherDebtorsAccount, dateFrom, dateTo, dnm, localDefnsDir, defnsDir, tmpTable);
      accountsGLOtherDebtors.processTmpTableForListing(con, stmt, rs, tmpTable, unm, dnm, data, fldNames, tm, bm, lm, rm, pageSizeWidth, pageSizeLength,
                                       currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName, userFontStyle,
                                       userFontItalic, userFontSize, fhPPR, fhO, fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord,
                                       across2Coord, oBuf, localDefnsDir, defnsDir);
    }
    else
    if(category.equals("Liabilities - Trade Creditors"))
    {
      String[][] accCurrencies = new String[1][];
      int numCurrencies = accountsUtils.getCurrencyNames(con, stmt, rs, accCurrencies, dnm, localDefnsDir, defnsDir);
      
      for(int x=0;x<numCurrencies;++x)
      {
        accountsGLTradeCreditors.calc(con, stmt, stmt2, rs, rs2, accCurrencies[0][x], baseCurrency, year, tradeCreditorsAccount, dateFrom, dateTo, dnm, localDefnsDir, defnsDir,
                    tmpTable);
        accountsGLTradeCreditors.processTmpTableForListing(con, stmt, rs, tmpTable, unm, dnm, data, fldNames, tm, bm, lm, rm, pageSizeWidth, pageSizeLength,
                                       currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName, userFontStyle,
                                       userFontItalic, userFontSize, fhPPR, fhO, fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord,
                                       across2Coord, oBuf, localDefnsDir, defnsDir);
      }
    }
    else
    if(category.equals("Liabilities - Other Creditors"))
    {
      accountsGLOtherCreditors.calc(con, stmt, stmt2, rs, rs2, account, year, otherCreditorsAccount, dateFrom, dateTo, dnm, localDefnsDir, defnsDir, tmpTable);
      accountsGLOtherCreditors.processTmpTableForListing(con, stmt, rs, tmpTable, unm, dnm, data, fldNames, tm, bm, lm, rm, pageSizeWidth, pageSizeLength,
                                       currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName, userFontStyle,
                                       userFontItalic, userFontSize, fhPPR, fhO, fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord,
                                       across2Coord, oBuf, localDefnsDir, defnsDir);
    }
    else
    if(category.equals("Income - Sales Returned"))
    {
      accountsGLSalesReturned.calc(con, stmt, stmt2, rs, account, year, dateFrom, dateTo, dnm, localDefnsDir, defnsDir, tmpTable);
      accountsGLSalesReturned.processTmpTableForListing(con, stmt, rs, tmpTable, unm, dnm, data, fldNames, tm, bm, lm, rm, pageSizeWidth, pageSizeLength,
                                       currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName, userFontStyle,
                                       userFontItalic, userFontSize, fhPPR, fhO, fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord,
                                       across2Coord, oBuf, localDefnsDir, defnsDir);
    }
    else
    if(category.equals("Cost of Sales - Purchases Returned"))
    {
      accountsGLPurchasesReturned.calc(con, stmt, stmt2, rs, account, year, dateFrom, dateTo, dnm, localDefnsDir, defnsDir, tmpTable);
      accountsGLPurchasesReturned.processTmpTableForListing(con, stmt, rs, tmpTable, unm, dnm, data, fldNames, tm, bm, lm, rm, pageSizeWidth, pageSizeLength,
                                       currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName, userFontStyle,
                                       userFontItalic, userFontSize, fhPPR, fhO, fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord,
                                       across2Coord, oBuf, localDefnsDir, defnsDir);
    }
    else
    if(category.equals("Current Assets - Cash"))
    { 
      accountsGLBankTransactions.calc(con, stmt, stmt2, rs, account, year, dateFrom, dateTo, baseCurrency, dnm, localDefnsDir, defnsDir, tmpTable);
      String accountCurrency = accountsUtils.getAccountCurrencyGivenAccCode(account, year, dnm, localDefnsDir, defnsDir);
      accountsGLBankTransactions.processTmpTableForListing(con, stmt, rs, tmpTable, unm, dnm, accountCurrency, baseCurrency, data, fldNames, tm, bm, lm, rm, pageSizeWidth,
                                      pageSizeLength, currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName,
                                      userFontStyle, userFontItalic, userFontSize, fhPPR, fhO, fontDefType, fontDefStyle, fontDefItalic, fontDefSize,
                                      down2Coord, across2Coord, oBuf, localDefnsDir, defnsDir);
    }
    else
    if(category.equals("Current Assets - Bank"))
    {
      accountsGLBankTransactions.calc(con, stmt, stmt2, rs, account, year, dateFrom, dateTo, baseCurrency, dnm, localDefnsDir, defnsDir, tmpTable);
      String accountCurrency = accountsUtils.getAccountCurrencyGivenAccCode(account, year, dnm, localDefnsDir, defnsDir);
      accountsGLBankTransactions.processTmpTableForListing(con, stmt, rs, tmpTable, unm, dnm, accountCurrency, baseCurrency, data, fldNames, tm, bm, lm, rm, pageSizeWidth,
                                      pageSizeLength, currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName,
                                      userFontStyle, userFontItalic, userFontSize, fhPPR, fhO, fontDefType, fontDefStyle, fontDefItalic, fontDefSize,
                                      down2Coord, across2Coord, oBuf, localDefnsDir, defnsDir);
    }
    else
    if(category.equals("Current Assets - Recoverable Expenses"))
    {
      accountsGLRecoverableExpenses.calc(con, stmt, stmt2, rs, rs2, account, year, dateFrom, dateTo, dnm, localDefnsDir, defnsDir, tmpTable);
      accountsGLRecoverableExpenses.processTmpTableForListing(con, stmt, rs, tmpTable, unm, dnm, data, fldNames, tm, bm, lm, rm, pageSizeWidth, pageSizeLength,
                                       currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName, userFontStyle,
                                       userFontItalic, userFontSize, fhPPR, fhO, fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord,
                                       across2Coord, oBuf, localDefnsDir, defnsDir);
    }
    else
    if(category.equals("Current Assets - Contra"))
    {
      accountsGLCurrentAssetsContra.calc(con, stmt, stmt2, rs, account, year, dateFrom, dateTo, dnm, localDefnsDir, defnsDir, tmpTable);
      accountsGLCurrentAssetsContra.processTmpTableForListing(con, stmt, rs, tmpTable, unm, dnm, data, fldNames, tm, bm, lm, rm, pageSizeWidth, pageSizeLength,
                                       currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName, userFontStyle,
                                       userFontItalic, userFontSize, fhPPR, fhO, fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord,
                                       across2Coord, oBuf, localDefnsDir, defnsDir);
    }
    else
    if(category.equals("Current Assets - PrePayments"))
    {
      accountsGLCurrentAssetsPrePayments.calc(con, stmt, stmt2, rs, rs2, account, year, dateFrom, dateTo, dnm, localDefnsDir, defnsDir, tmpTable);
      accountsGLCurrentAssetsPrePayments.processTmpTableForListing(con, stmt, rs, tmpTable, unm, dnm, data, fldNames, tm, bm, lm, rm, pageSizeWidth, pageSizeLength,
                                       currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName, userFontStyle,
                                       userFontItalic, userFontSize, fhPPR, fhO, fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord,
                                       across2Coord, oBuf, localDefnsDir, defnsDir);
    }
    else
    if(category.equals("Liabilities - PrePayments"))
    {
      accountsGLLiabilitiesPrePayments.calc(con, stmt, stmt2, rs, rs2, account, year, dateFrom, dateTo, dnm, localDefnsDir, defnsDir, tmpTable);
      accountsGLLiabilitiesPrePayments.processTmpTableForListing(con, stmt, rs, tmpTable, unm, dnm, data, fldNames, tm, bm, lm, rm, pageSizeWidth, pageSizeLength,
                                       currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName, userFontStyle,
                                       userFontItalic, userFontSize, fhPPR, fhO, fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord,
                                       across2Coord, oBuf, localDefnsDir, defnsDir);
    }
    else
    if(  category.equals("Liabilities") || category.equals("Liabilities - Suspense") || category.equals("Liabilities - Provision for Income Tax")
       || category.equals("Liabilities - Proposed Dividend"))
    {
      accountsGLLiabilities.calc(con, stmt, stmt2, rs, account, year, dateFrom, dateTo, dnm, localDefnsDir, defnsDir, tmpTable);
      accountsGLLiabilities.processTmpTableForListing(con, stmt, rs, tmpTable, unm, dnm, data, fldNames, tm, bm, lm, rm, pageSizeWidth, pageSizeLength,
                                       currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName, userFontStyle,
                                       userFontItalic, userFontSize, fhPPR, fhO, fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord,
                                       across2Coord, oBuf, localDefnsDir, defnsDir);
    }
    else
    if(category.equals("Income"))
    {
      accountsGLIncome.calc(con, stmt, stmt2, rs, account, year, dateFrom, dateTo, dnm, localDefnsDir, defnsDir, tmpTable);
      accountsGLIncome.processTmpTableForListing(con, stmt, rs, tmpTable, unm, dnm, data, fldNames, tm, bm, lm, rm, pageSizeWidth, pageSizeLength,
                                       currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName, userFontStyle,
                                       userFontItalic, userFontSize, fhPPR, fhO, fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord,
                                       across2Coord, oBuf, localDefnsDir, defnsDir);
    }
    else
    if(category.equals("Income - Discount Given"))
    {
      accountsGLDiscounts.calc(con, stmt, stmt2, rs, account, year, dateFrom, dateTo, "G", baseCurrency, dnm, localDefnsDir, defnsDir, tmpTable);
      accountsGLDiscounts.processTmpTableForListing(con, stmt, rs, tmpTable, unm, dnm, data, fldNames, tm, bm, lm, rm, pageSizeWidth, pageSizeLength,
                                       currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName, userFontStyle,
                                       userFontItalic, userFontSize, fhPPR, fhO, fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord,
                                       across2Coord, oBuf, localDefnsDir, defnsDir);
    }
    else
    if(category.equals("Liabilities - Accruals"))
    {
      accountsGLLiabilitiesAccruals.calc(con, stmt, stmt2, rs, account, year, dateFrom, dateTo, dnm, localDefnsDir, defnsDir, tmpTable);
      accountsGLLiabilitiesAccruals.processTmpTableForListing(con, stmt, rs, tmpTable, unm, dnm, data, fldNames, tm, bm, lm, rm, pageSizeWidth, pageSizeLength,
                                       currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName, userFontStyle,
                                       userFontItalic, userFontSize, fhPPR, fhO, fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord,
                                       across2Coord, oBuf, localDefnsDir, defnsDir);
    }
    else
    if(category.equals("Equity - Current Earnings") || category.equals("Equity - Retained Earnings") || category.equals("Fixed Assets - Accumulated Depreciation"))
    {
      accountsGLEquityEarnings.calc(con, stmt, stmt2, rs, account, year, dateFrom, dateTo, dnm, localDefnsDir, defnsDir, tmpTable);
      accountsGLEquityEarnings.processTmpTableForListing(con, stmt, rs, tmpTable, unm, dnm, data, fldNames, tm, bm, lm, rm, pageSizeWidth, pageSizeLength,
                                       currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName, userFontStyle,
                                       userFontItalic, userFontSize, fhPPR, fhO, fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord,
                                       across2Coord, oBuf, localDefnsDir, defnsDir);
    }
    else
    if(category.equals("Current Assets - Currency Exchange"))
    {
      accountsGLEquityCurrencyExchange.calc(con, stmt, stmt2, rs, account, year, dateFrom, dateTo, dnm, localDefnsDir, defnsDir, tmpTable);
      String accountCurrency = accountsUtils.getAccountCurrencyGivenAccCode(account, year, dnm, localDefnsDir, defnsDir);
      accountsGLEquityCurrencyExchange.processTmpTableForListing(con, stmt, rs, tmpTable, unm, dnm, accountCurrency, baseCurrency, data, fldNames, tm, bm, lm, rm,
                                       pageSizeWidth, pageSizeLength, currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF,
                                       userFontName, userFontStyle, userFontItalic, userFontSize, fhPPR, fhO, fontDefType, fontDefStyle,
                                       fontDefItalic, fontDefSize, down2Coord, across2Coord, oBuf, localDefnsDir, defnsDir);
    }
    else
    if(category.equals("Current Assets - Stock"))
    {
      analyticsStock.calc(con, stmt, stmt2, rs, rs2, account, year, dateFrom, dateTo, unm, dnm, workingDir, localDefnsDir, defnsDir, tmpTable);
      analyticsStock.processTmpTableForListing(con, stmt, rs, tmpTable, unm, dnm, data, fldNames, tm, bm, lm, rm, pageSizeWidth, pageSizeLength,
                                       currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName, userFontStyle,
                                       userFontItalic, userFontSize, fhPPR, fhO, fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord,
                                       across2Coord, oBuf, localDefnsDir, defnsDir);
    }
    else
    if(category.equals("Current Assets - WIP"))
    {
      analyticsWIP.calc(con, stmt, stmt2, rs, rs2, account, year, dateFrom, dateTo, unm, dnm, workingDir, localDefnsDir, defnsDir, tmpTable);
      analyticsWIP.processTmpTableForListing(con, stmt, rs, tmpTable, unm, dnm, data, fldNames, tm, bm, lm, rm, pageSizeWidth, pageSizeLength,
                                       currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName, userFontStyle,
                                       userFontItalic, userFontSize, fhPPR, fhO, fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord,
                                       across2Coord, oBuf, localDefnsDir, defnsDir);
    }
    else
    if(category.equals("Expenses - Bank Charges"))
    {
      accountsGLBankCharges.calc(con, accCon, stmt, stmt2, rs, account, year, dateFrom, dateTo, dnm, localDefnsDir, defnsDir, tmpTable);
      accountsGLBankCharges.processTmpTableForListing(con, stmt, rs, tmpTable, unm, dnm, data, fldNames, tm, bm, lm, rm, pageSizeWidth, pageSizeLength,
                                       currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName, userFontStyle,
                                       userFontItalic, userFontSize, fhPPR, fhO, fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord,
                                       across2Coord, oBuf, localDefnsDir, defnsDir);
    }
    else
    if(category.startsWith("Expenses - "))
    {
      accountsGLExpenses.calc(con, stmt, stmt2, rs, account, year, dateFrom, dateTo, dnm, localDefnsDir, defnsDir, tmpTable);
      accountsGLExpenses.processTmpTableForListing(con, stmt, rs, tmpTable, unm, dnm, data, fldNames, tm, bm, lm, rm, pageSizeWidth, pageSizeLength,
                                       currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName, userFontStyle,
                                       userFontItalic, userFontSize, fhPPR, fhO, fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord,
                                       across2Coord, oBuf, localDefnsDir, defnsDir);
    }
    else
    if(   category.equals("Fixed Assets") || category.equals("Fixed Assets - Investment") || category.equals("Equity")
       || category.equals("Current Assets") || category.equals("Current Assets - OverPayments") || category.startsWith("PL Provision"))
    {
      accountsGLFixedAssetsEquity.calc(con, stmt, stmt2, rs, rs2, account, year, dateFrom, dateTo, dnm, localDefnsDir, defnsDir, tmpTable);
      accountsGLFixedAssetsEquity.processTmpTableForListing(con, stmt, rs, tmpTable, unm, dnm, data, fldNames, tm, bm, lm, rm, pageSizeWidth, pageSizeLength,
                                       currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName, userFontStyle,
                                       userFontItalic, userFontSize, fhPPR, fhO, fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord,
                                       across2Coord, oBuf, localDefnsDir, defnsDir);
    }
  }

}
