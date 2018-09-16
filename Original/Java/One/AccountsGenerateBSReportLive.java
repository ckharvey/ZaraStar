// =======================================================================================================================================================================================================
// System: ZaraStar: Accounts: Generate BS report - live
// Module: AccountsGenerateBSReportLive.java
// Author: C.K.Harvey
// Copyright (c) 1998-2009 Christopher Harvey. All Rights Reserved.
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

public class AccountsGenerateBSReportLive extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  AccountsUtils accountsUtils = new AccountsUtils();
  DefinitionTables definitionTables = new DefinitionTables();
  DashboardUtils dashboardUtils = new DashboardUtils();
  AccountsGenerateTBReport accountsGenerateTBReport = new AccountsGenerateTBReport();
  
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
      p1  = req.getParameter("p1"); // monthEnd
      p2  = req.getParameter("p2"); // plain

      if(p2 == null) p2 = "N";
      
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "AccountsGenerateBSReportLive", bytesOut);
      System.out.println(e);
      serverUtils.etotalBytes(req, unm, dnm, 6054, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1, String p2, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');
    String workingDir    = directoryUtils.getUserDir('W', dnm, unm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 6054, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "6054e", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6054, bytesOut[0], 0, "ACC:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "6054e", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6054, bytesOut[0], 0, "SID:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    scoutln(out, bytesOut, "<html><head><title>Balance Sheet</title></head>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    // keepAlive

    RandomAccessFile fhData  = generalUtils.create(workingDir + "6054.data");
    RandomAccessFile fhState = generalUtils.create(workingDir + "6054.state");
    generalUtils.fileClose(fhState);
    String stateFileName = workingDir + "6054.state";
    keepChecking(out, "6054", unm, sid, uty, men, den, dnm, bnm, bytesOut);

    boolean plain;
    if(p2.equals("P"))
      plain = true;
    else plain = false;

    if(plain)
      scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"print\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "generalPlain.css\">");
    else scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    outputPageFrame(con, stmt, rs, out, req, plain, "", p1, "Balance Sheet as at " + p1, unm, sid, uty, men, den, dnm, bnm, " chkTimer(); ", localDefnsDir, defnsDir, bytesOut);

    scoutln(out, bytesOut, "<span id='stuff'></span>");
    if(out != null) out.flush();
    out.close();
    
    scoutln(fhData, "<table id=\"page\" border=0 width=100%>");

    if(plain)
    {
      scoutln(fhData, "<tr><td>&nbsp;</td></tr>");
      scoutln(fhData, "<tr><td colspan=4><p><b>Balance Sheet: " + p1 + "</td></tr>");
      scoutln(fhData, "<tr><td>&nbsp;</td></tr>");
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

        String previousFinancialYearStartDate = (generalUtils.intFromStr(yyyy) - 1) + "-" + generalUtils.detMonthNumFromMonthName(financialYearStartMonth[0]) + "-01";

    String yearStartDate = financialYearStartDate;
    String yearEndDate   = financialYearEndDate;

    int effectiveStartDate = generalUtils.encode(definitionTables.getAppConfigEffectiveStartDate(con, stmt, rs, dnm), localDefnsDir, defnsDir);

    if(effectiveStartDate > generalUtils.encodeFromYYYYMMDD(yearStartDate))
      yearStartDate = generalUtils.decodeToYYYYMMDD(effectiveStartDate);

    // given stated reqd month num, determine the posn of that month in the range of months
    // e.g., if yearStart is April, and reqd month is June, then reqd month is 3

    int reqdMonth = 0, x = generalUtils.getMonth(generalUtils.encodeFromYYYYMMDD(yearStartDate));
    while(x < month[0])
    {
      ++reqdMonth;
      ++x;
    }       
    
    yearEndDate = generalUtils.lastDayOfMonthYYYYMMDD(year[0] + "-" + month[0] + "-" + "1");

    generate(fhData, stateFileName, yyyy, yearStartDate, yearEndDate, financialYearStartDate, previousFinancialYearStartDate, dnm, localDefnsDir, defnsDir, bytesOut);
    
    scoutln(fhData, "<tr><td>&nbsp;</td></tr>");
    scoutln(fhData, "<tr><td>&nbsp;</td></tr>");
    scoutln(fhData, "</table></form>");
    scoutln(fhData, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 

    generalUtils.fileClose(fhData);
    directoryUtils.updateState(stateFileName, "100");
    
    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 6054, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void generate(RandomAccessFile fhData, String stateFileName, String yyyy, String dateFrom, String dateTo, String financialYearStartDate,
          String previousFinancialYearStartDate, String dnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    String[][] accCodes = new String[1][];
    double[][][] accValsDr = new double[1][][];
    double[][][] accValsCr = new double[1][][];
    int[] numAccs = new int[1];
    char[][] accTypes = new char[1][];
    
    int[] monthEnds = new int[13];
    int[] numMonths = new int[1];

    Connection accCon = null, ofsaCon  = null;
    Statement stmt = null, stmt2 = null, stmt3 = null, stmt4 = null;
    ResultSet rs   = null, rs2   = null, rs3   = null, rs4   = null;
    
    numAccs[0] = 0;

    try
    {
      String uName = directoryUtils.getMySQLUserName();
      String pWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();
    
      accCon = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_accounts_" + yyyy + "?user=" + uName + "&password=" + pWord);

      ofsaCon = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + uName + "&password=" + pWord);

      directoryUtils.updateState(stateFileName, "10");

      allAccounts(accCon, ofsaCon, stmt, stmt2, rs, rs2, yyyy, dateFrom, dateTo, financialYearStartDate, previousFinancialYearStartDate, dnm, localDefnsDir, defnsDir, accCodes, accValsDr, accValsCr, numAccs, accTypes, monthEnds, numMonths, stateFileName);
      directoryUtils.updateState(stateFileName, "90");

      String baseCurrency = accountsUtils.getBaseCurrency(ofsaCon, stmt, rs, dnm, localDefnsDir, defnsDir);
    
      String[][] accCurrencies = new String[1][];
      int numCurrencies = accountsUtils.getCurrencyNames(ofsaCon, stmt, rs, accCurrencies, dnm, localDefnsDir, defnsDir);

      double[] va = new double[1];  va[0] = 0.0;
              
      scoutln(fhData, "<tr><td></td><td align=right><p><b>Year (" + dateTo + ")</b></td>");

      // Current Assets
      
      double[] currentAssetsTotal = new double[1];
      
      double currentAssetsYTDTotal;

      scoutln(fhData, "<tr><td><p><b>Current Assets</b></td></tr>");

      currentAssetsTotal[0] = 0.0;

      showCurrentAssets(accCon, ofsaCon, stmt, stmt2, stmt3, stmt4, rs, rs2, rs3, rs4, fhData, accCodes[0], accValsDr[0], accValsCr[0], numAccs[0], accTypes[0], baseCurrency, dateFrom, dateTo, accCurrencies[0], numCurrencies, dnm, localDefnsDir,
                        defnsDir, bytesOut, currentAssetsTotal, va);
      
      currentAssetsYTDTotal   = currentAssetsTotal[0];

      scoutln(fhData, "<tr bgcolor=\"#D0D0D0\"><td><p>Total Current Assets</td>");
      scoutln(fhData, "<td align=right><p>" + generalUtils.formatNumeric(currentAssetsYTDTotal, '2') + "</td></tr>");

      directoryUtils.updateState(stateFileName, "91");

      // Fixed Assets
      
      double[] fixedAssetsTotal = new double[1];
      
      double fixedAssetsYTDTotal;

      scoutln(fhData, "<tr><td><p><b>Fixed Assets</b></td></tr>");

      fixedAssetsTotal[0] = 0.0;

      showFixedAssets(accCon, ofsaCon, stmt, stmt2, stmt3, rs, rs2, rs3, fhData, accCodes[0], accValsDr[0], accValsCr[0], numAccs[0], accTypes[0], baseCurrency, dateFrom, dateTo, accCurrencies[0], numCurrencies, dnm, localDefnsDir, defnsDir,
                      bytesOut, fixedAssetsTotal, va);
      
      fixedAssetsYTDTotal = fixedAssetsTotal[0];
      directoryUtils.updateState(stateFileName, "92");

      // Accumulated Depreciation
      
      double[] accumulatedDepreciationTotal = new double[1];
      
      accumulatedDepreciationTotal[0] = 0.0;

      showFixedAssetsAccumulatedDepreciation(accCon, ofsaCon, stmt, stmt2, stmt3, rs, rs2, rs3, fhData, accCodes[0], accValsDr[0], accValsCr[0], numAccs[0], accTypes[0], baseCurrency, dateFrom, dateTo, accCurrencies[0], numCurrencies, dnm,
                                             localDefnsDir, defnsDir, bytesOut, accumulatedDepreciationTotal, va);
      
      fixedAssetsYTDTotal = fixedAssetsTotal[0] + accumulatedDepreciationTotal[0];

      scoutln(fhData, "<tr bgcolor=\"#D0D0D0\"><td><p>Total Fixed Assets</td>");
      scoutln(fhData, "<td align=right><p>" + generalUtils.formatNumeric(fixedAssetsYTDTotal, '2') + "</td></tr>");
      directoryUtils.updateState(stateFileName, "93");

      // Investment
            
      double[] investmentYTDTotal = new double[1];
      
      scoutln(fhData, "<tr><td><p><b>Investment</b></td></tr>");

      showInvestment(accCon, stmt, rs, accCodes[0], accValsDr[0], accValsCr[0], numAccs[0], accTypes[0], investmentYTDTotal);
      
      scoutln(fhData, "<tr bgcolor=\"#D0D0D0\"><td><p>Investment</td>");
      scoutln(fhData, "<td align=right><p>" + generalUtils.formatNumeric(investmentYTDTotal[0], '2') + "</td></tr>");
      directoryUtils.updateState(stateFileName, "94");

      // Total Assets
      
      double totalAssets = currentAssetsYTDTotal + fixedAssetsYTDTotal + investmentYTDTotal[0];
      
      scoutln(fhData, "<tr bgcolor=\"#B0B0B0\"><td><p>Total Assets</td>");
      scoutln(fhData, "<td align=right><p>" + generalUtils.formatNumeric(totalAssets, '2') + "</td></tr>");
      
      // Liabilities
      
      double[] liabilitiesTotal = new double[1];
      
      double liabilitiesYTDTotal;

      scoutln(fhData, "<tr><td><p><b>Liabilities</b></td></tr>");

      liabilitiesTotal[0] = 0.0;

      showLiabilities(accCon, ofsaCon, stmt, stmt2, stmt3, stmt4, rs, rs2, rs3, rs4, fhData, accCodes[0], accValsDr[0], accValsCr[0], numAccs[0], accTypes[0], baseCurrency, dateFrom, dateTo, accCurrencies[0], numCurrencies, dnm, localDefnsDir,
                      defnsDir, bytesOut, liabilitiesTotal, va);
      
      liabilitiesYTDTotal = liabilitiesTotal[0];

      liabilitiesYTDTotal *= -1;
      scoutln(fhData, "<tr bgcolor=\"#D0D0D0\"><td><p>Total Liabilities</td>");
      scoutln(fhData, "<td align=right><p>" + generalUtils.formatNumeric(liabilitiesYTDTotal, '2') + "</td></tr>");
      directoryUtils.updateState(stateFileName, "95");

      // Suspense
            
      double[] suspenseTotal = new double[1];
      
      double suspenseYTDTotal;
      
      scoutln(fhData, "<tr><td><p><b>Suspense</b></td></tr>");

      showSuspense(accCon, ofsaCon, stmt, stmt2, stmt3, stmt4, rs, rs2, rs3, rs4, fhData, accCodes[0], accValsDr[0], accValsCr[0], numAccs[0], accTypes[0], baseCurrency, dateFrom, dateTo, accCurrencies[0], numCurrencies, dnm, localDefnsDir,
                   defnsDir, bytesOut, suspenseTotal, va);
      
      suspenseYTDTotal = suspenseTotal[0];

      scoutln(fhData, "<tr bgcolor=\"#D0D0D0\"><td><p>Total Suspense</td><td align=right><p>" + generalUtils.formatNumeric(suspenseYTDTotal, '2') + "</td></tr>");

      directoryUtils.updateState(stateFileName, "96");

      // Net Assets
      
      double netAssets = totalAssets + (-liabilitiesYTDTotal + suspenseYTDTotal);
      
      scoutln(fhData, "<tr bgcolor=\"#B0B0B0\"><td><p>Net Assets</td><td align=right><p>" + generalUtils.formatNumeric(netAssets, '2') + "</td></tr>");
      
      // Equity
            
      double[] equityTotal = new double[1];
      
      double equityYTDTotal;
      
      scoutln(fhData, "<tr><td><p><b>Equity</b></td></tr>");

      showEquity(accCon, ofsaCon, stmt, stmt2, stmt3, stmt4, rs, rs2, rs3, rs4, fhData, accCodes[0], accValsDr[0], accValsCr[0], numAccs[0], accTypes[0], baseCurrency, dateFrom, dateTo, accCurrencies[0], numCurrencies, dnm, localDefnsDir,
                 defnsDir, bytesOut, equityTotal, va);
      
      equityYTDTotal = equityTotal[0];

      double assumedCurrentEarnings = netAssets + equityYTDTotal;

      directoryUtils.updateState(stateFileName, "97");
   
      // output valuation adjustment

      va[0] *= -1;

      scoutln(fhData, "<tr bgcolor='#F0F0F0'><td><p>Valuation Adjustment</td><td align=right><p>" + generalUtils.formatNumeric(va[0], '2') + "</td></tr>");
      
      assumedCurrentEarnings += va[0];

      showCurrentEarnings(accCon, stmt, rs, fhData, assumedCurrentEarnings, accCodes[0], accValsDr[0], accValsCr[0], numAccs[0], accTypes[0]);
      
      scoutln(fhData, "<tr bgcolor='#D0D0D0'><td><p>Total Equity</td><td align=right><p>" + generalUtils.formatNumeric(netAssets, '2') + "</td></tr>");

      directoryUtils.updateState(stateFileName, "98");
      
      if(ofsaCon != null) ofsaCon.close();
      if(accCon != null) accCon.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(ofsaCon != null) ofsaCon.close();
      if(accCon != null) accCon.close();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void showCurrentAssets(Connection accCon, Connection ofsaCon, Statement stmt, Statement stmt2, Statement stmt3, Statement stmt4, ResultSet rs, ResultSet rs2, ResultSet rs3, ResultSet rs4, RandomAccessFile fhData, String[] accCodes,
                                 double[][] accValsDr, double[][] accValsCr, int numAccs, char[] accTypes, String baseCurrency, String dateFrom, String dateTo, String[] accCurrencies, int numCurrencies, String dnm, String localDefnsDir,
                                 String defnsDir, int[] bytesOut, double[] currentAssetsYTDTotal, double[] va) throws Exception
  {
    stmt = accCon.createStatement();

    rs = stmt.executeQuery("SELECT DISTINCT Category FROM acctdefn WHERE Category LIKE 'Current Assets%' AND Active = 'Y' ORDER BY Category, Description"); 

    String category;
      
    while(rs.next())
    {
      category = rs.getString(1);

      showCategory(accCon, ofsaCon, stmt2, stmt3, stmt4, rs2, rs3, rs4, fhData, category, false, accCodes, accValsDr, accValsCr, numAccs, accTypes, baseCurrency, accCurrencies, numCurrencies, dateFrom, dateTo, dnm, localDefnsDir, defnsDir,
                   bytesOut, currentAssetsYTDTotal, va);
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();    
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void showFixedAssets(Connection accCon, Connection ofsaCon, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, ResultSet rs3, RandomAccessFile fhData, String[] accCodes, double[][] accValsDr,
                               double[][] accValsCr, int numAccs, char[] accTypes, String baseCurrency, String dateFrom, String dateTo, String[] accCurrencies, int numCurrencies, String dnm, String localDefnsDir, String defnsDir, int[] bytesOut,
                               double[] fixedAssetsYTDTotal, double[] va) throws Exception
  {
    showCategory(accCon, ofsaCon, stmt, stmt2, stmt3, rs, rs2, rs3, fhData, "Fixed Assets", false, accCodes, accValsDr, accValsCr, numAccs, accTypes, baseCurrency, accCurrencies, numCurrencies, dateFrom, dateTo, dnm, localDefnsDir, defnsDir,
                 bytesOut, fixedAssetsYTDTotal, va);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void showFixedAssetsAccumulatedDepreciation(Connection accCon, Connection ofsaCon, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, ResultSet rs3, RandomAccessFile fhData, String[] accCodes,
                                                      double[][] accValsDr, double[][] accValsCr, int numAccs, char[] accTypes, String baseCurrency, String dateFrom, String dateTo, String[] accCurrencies, int numCurrencies, String dnm,
                                                      String localDefnsDir, String defnsDir, int[] bytesOut, double[] accumulatedDepreciationYTDTotal, double[] va) throws Exception
  {
    showCategory(accCon, ofsaCon, stmt, stmt2, stmt3, rs, rs2, rs3, fhData, "Fixed Assets - Accumulated Depreciation", true, accCodes, accValsDr, accValsCr, numAccs, accTypes, baseCurrency, accCurrencies, numCurrencies, dateFrom, dateTo, dnm,
                 localDefnsDir, defnsDir, bytesOut, accumulatedDepreciationYTDTotal, va);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void showLiabilities(Connection accCon, Connection ofsaCon, Statement stmt, Statement stmt2, Statement stmt3, Statement stmt4, ResultSet rs, ResultSet rs2, ResultSet rs3, ResultSet rs4, RandomAccessFile fhData, String[] accCodes,
                               double[][] accValsDr, double[][] accValsCr, int numAccs, char[] accTypes, String baseCurrency, String dateFrom, String dateTo, String[] accCurrencies, int numCurrencies, String dnm, String localDefnsDir,
                               String defnsDir, int[] bytesOut, double[] liabilitiesYTDTotal, double[] va) throws Exception
  {
    stmt = accCon.createStatement();

    rs = stmt.executeQuery("SELECT DISTINCT Category FROM acctdefn WHERE Category != 'Liabilities - Suspense' AND Category LIKE 'Liabilities%' "
                         + "AND Active = 'Y' ORDER BY Category, Description"); 

    String category;
      
    while(rs.next())
    {
      category = rs.getString(1);
 
      showCategory(accCon, ofsaCon, stmt2, stmt3, stmt4, rs2, rs3, rs4, fhData, category, true, accCodes, accValsDr, accValsCr, numAccs, accTypes, baseCurrency, accCurrencies, numCurrencies, dateFrom, dateTo, dnm, localDefnsDir, defnsDir,
                   bytesOut, liabilitiesYTDTotal, va);
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();    
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void showInvestment(Connection accCon, Statement stmt, ResultSet rs, String[] accCodes, double[][] accValsDr, double[][] accValsCr, int numAccs, char[] accTypes, double[] ytdTotal) throws Exception
  {
    // Fixed Assets - Investment
    
    stmt = accCon.createStatement();

    rs = stmt.executeQuery("SELECT AccCode FROM acctdefn WHERE Category = 'Fixed Assets - Investment' AND Active = 'Y'"); 

    double ytdAmount = 0.0;
    String accCode;
      
    while(rs.next())
    {
      accCode = rs.getString(1);
 
      ytdAmount += getAccAmt(accCode, accCodes, accValsDr[13],    accValsCr[13],    numAccs, accTypes);
    }
    
    ytdTotal[0] += ytdAmount;
        
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();    
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void showSuspense(Connection accCon, Connection ofsaCon, Statement stmt, Statement stmt2, Statement stmt3, Statement stmt4, ResultSet rs, ResultSet rs2, ResultSet rs3, ResultSet rs4, RandomAccessFile fhData, String[] accCodes,
                            double[][] accValsDr, double[][] accValsCr, int numAccs, char[] accTypes, String baseCurrency, String dateFrom, String dateTo, String[] accCurrencies, int numCurrencies, String dnm, String localDefnsDir,
                            String defnsDir, int[] bytesOut, double[] suspenseYTDTotal, double[] va) throws Exception
  {
    stmt = accCon.createStatement();
 
    rs = stmt.executeQuery("SELECT Category FROM acctdefn WHERE Category = 'Liabilities - Suspense' AND Active = 'Y'"); 

    String category;
      
    while(rs.next())
    {
      category = rs.getString(1);
 
      showCategory(accCon, ofsaCon, stmt2, stmt3, stmt4, rs2, rs3, rs4, fhData, category, false, accCodes, accValsDr, accValsCr, numAccs, accTypes, baseCurrency, accCurrencies, numCurrencies, dateFrom, dateTo, dnm, localDefnsDir, defnsDir,
                   bytesOut, suspenseYTDTotal, va);
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();    
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void showEquity(Connection accCon, Connection ofsaCon, Statement stmt, Statement stmt2, Statement stmt3, Statement stmt4, ResultSet rs, ResultSet rs2, ResultSet rs3, ResultSet rs4, RandomAccessFile fhData, String[] accCodes,
                          double[][] accValsDr, double[][] accValsCr, int numAccs, char[] accTypes, String baseCurrency, String dateFrom, String dateTo, String[] accCurrencies, int numCurrencies, String dnm, String localDefnsDir, String defnsDir,
                          int[] bytesOut, double[] equityYTDTotal, double[] va) throws Exception
  {
    stmt = accCon.createStatement();

    rs = stmt.executeQuery("SELECT DISTINCT Category FROM acctdefn WHERE Category LIKE 'Equity%' AND Category != 'Equity - Current Earnings' AND Active = 'Y' ORDER BY Category, Description"); 

    String category;
      
    while(rs.next())
    {
      category = rs.getString(1);

      showCategory(accCon, ofsaCon, stmt2, stmt3, stmt4, rs2, rs3, rs4, fhData, category, true, accCodes, accValsDr, accValsCr, numAccs, accTypes, baseCurrency, accCurrencies, numCurrencies, dateFrom, dateTo, dnm, localDefnsDir, defnsDir,
                   bytesOut, equityYTDTotal, va);
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();    
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void showCurrentEarnings(Connection accCon, Statement stmt, ResultSet rs, RandomAccessFile fhData, double assumedCurrentEarnings, String[] accCodes, double[][] accValsDr, double[][] accValsCr, int numAccs, char[] accTypes)
                                   throws Exception
  {
    stmt = accCon.createStatement();

    rs = stmt.executeQuery("SELECT AccCode, Description FROM acctdefn WHERE Category = 'Equity - Current Earnings' AND Active = 'Y'"); 

    double ytdAmount;
    String accCode, desc;
      
    while(rs.next())
    {
      accCode = rs.getString(1);
      desc    = rs.getString(2);
 
      ytdAmount = getAccAmt(accCode, accCodes, accValsDr[13], accValsCr[13], numAccs, accTypes);
      
      ytdAmount += assumedCurrentEarnings;
      
      scoutln(fhData, "<tr bgcolor=\"#F0F0F0\"><td><p>" + desc + "</td><td align=right><p>" + generalUtils.formatNumeric(ytdAmount, '2') + "</td></tr>");
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();    
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void showCategory(Connection accCon, Connection ofsaCon, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, ResultSet rs3, RandomAccessFile fhData, String category, boolean isLiability, String[] accCodes,
                            double[][] accValsDr, double[][] accValsCr, int numAccs, char[] accTypes, String baseCurrency, String[] accCurrencies, int numCurrencies, String dateFrom, String dateTo, String dnm, String localDefnsDir,
                            String defnsDir, int[] bytesOut, double[] ytdTotal, double[] va) throws Exception
  {
    stmt = accCon.createStatement();

    rs = stmt.executeQuery("SELECT AccCode, Description, Currency, DrCr FROM acctdefn WHERE Category = '" + category + "' AND Active = 'Y'"); 

    double ytdAmount;
    String accCode, desc, currency;
      
    while(rs.next())
    {
      accCode  = rs.getString(1);
      desc     = rs.getString(2);
      currency = rs.getString(3);

      ytdAmount = getAccAmt(accCode, accCodes, accValsDr[13], accValsCr[13], numAccs, accTypes);

      ytdTotal[0] += ytdAmount;
 
      if(isLiability)
        ytdAmount *= -1;

      scoutln(fhData, "<tr bgcolor=\"#F0F0F0\"><td><p>" + desc + "</td>");
      scoutln(fhData, "<td align=right><p>" + generalUtils.formatNumeric(ytdAmount, '2') + "</td></tr>");

      if(category.equals("Current Assets - Trade Debtors"))
        forDebtors(accCon, ofsaCon, stmt, stmt2, rs, rs2, fhData, dateFrom, dateTo, accCode, accCurrencies, numCurrencies, ytdAmount, ytdTotal, va, "#F0F0F0", dnm, localDefnsDir, defnsDir);
      else
      if(category.equals("Liabilities - Trade Creditors"))
        forCreditors(accCon, ofsaCon, stmt, stmt2, rs, rs2, fhData, dateFrom, dateTo, accCode, accCurrencies, numCurrencies, ytdAmount, ytdTotal, va, "#F0F0F0", dnm, localDefnsDir, defnsDir, bytesOut);
      else checkForValuationAdjustment(accCon, ofsaCon, stmt2, stmt3, rs2, rs3, fhData, accCode, desc, currency, baseCurrency, dateFrom, dateTo, ytdAmount, ytdTotal, va, "#F0F0F0", dnm, localDefnsDir, defnsDir);
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();    
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private double getAccAmt(String reqdAccCode, String[] accCodes, double[] accValsDr, double[] accValsCr, int numAccs, char[] accTypes) throws Exception
  {
    double value = 0.0;
    int x = 0;
    while(x < numAccs && ! accCodes[x].equals(reqdAccCode))
      ++x;
    
    if(x < numAccs)
    {
      value = generalUtils.doubleDPs(accValsDr[x], '2') + generalUtils.doubleDPs((accValsCr[x] * -1), '2');
    }
    
    return generalUtils.doubleDPs(value, '2');
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void allAccounts(Connection accCon, Connection ofsaCon, Statement accStmt, Statement ofsaStmt, ResultSet accRs, ResultSet ofsaRs, String year, String scanDateFrom, String scanDateTo, String financialYearStartDate,
                           String previousFinancialYearStartDate, String dnm, String localDefnsDir, String defnsDir, String[][] accCodes, double[][][] accValsDr, double[][][] accValsCr, int[] numAccs, char[][] accTypes, int[] monthEnds,
                           int[] numMonths, String stateFileName) throws Exception
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

      String[] accCurrencies = new String[numAccs[0]];

      accValsDr[0] = new double[14][numAccs[0]];
      accValsCr[0] = new double[14][numAccs[0]];

      String debtorsAccount = "", creditorsAccount = "", otherDebtorsAccount = "", otherCreditorsAccount = "", salesAccount = "", purchasesAccount = "", gstOutputAccount = "", gstInputAccount = "", exchangeGainAccount = "",
             exchangeLossAccount = "", salesReturnedAccount = "", purchasesReturnedAccount = "", stockAccount = "", discountReceivedAccount = "", discountGivenAccount = "";
      
      accStmt = accCon.createStatement();

      accRs = accStmt.executeQuery("SELECT AccCode, Category, DrCr, Currency FROM acctdefn WHERE Active = 'Y' ORDER BY AccCode");

      int x, y, count = 0;
      String category;
      
      while(accRs.next())
      {
        accCodes[0][count] = accRs.getString(1);
        category           = accRs.getString(2);
        accTypes[0][count] = accRs.getString(3).charAt(0);
        accCurrencies[count] = accRs.getString(4);

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

      String baseCurrency = accountsUtils.getBaseCurrency(ofsaCon, ofsaStmt, ofsaRs, dnm, localDefnsDir, defnsDir);      

      accountsGenerateTBReport.scanDocuments(accCon, ofsaCon, accStmt, ofsaStmt, accRs, ofsaRs, true, scanDateFrom, scanDateTo, financialYearStartDate, previousFinancialYearStartDate, accCodes[0], accValsDr[0], accValsCr[0], numAccs[0], monthEnds, numMonths,
                           debtorsAccount, creditorsAccount, otherDebtorsAccount, otherCreditorsAccount, salesAccount, purchasesAccount, gstOutputAccount, gstInputAccount, exchangeGainAccount, exchangeLossAccount, salesReturnedAccount,
                           purchasesReturnedAccount, stockAccount, discountReceivedAccount, discountGivenAccount, baseCurrency, null, null, null, null, null, null, 0, 0, stateFileName,    "", dnm);

      // accValsDr and accValsCr have data for all months of the year (from the effectiveStartDate if appropriate), upto the stated monthEnd
      // [12] has the OB values from the OB batch(es)
      
      // the YTD colunm [13] values are derived by totalling all months

      for(y=0;y<numAccs[0];++y)
      {
        accValsDr[0][13][y] = accValsDr[0][12][y];// * rate;
        accValsCr[0][13][y] = accValsCr[0][12][y];// * rate;
      }
   
      for(x=0;x<numMonths[0];++x)
      {
        for(y=0;y<numAccs[0];++y)
        {
          accValsDr[0][13][y] += generalUtils.doubleDPs(accValsDr[0][x][y], '2');
          accValsCr[0][13][y] += generalUtils.doubleDPs(accValsCr[0][x][y], '2');
        }
      }
    }
    catch(Exception e)
    {
      System.out.println(e);
    }
  }  

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(RandomAccessFile fh, String str) throws Exception
  {      
    fh.writeBytes(str + "\n");
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void outputPageFrame(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, boolean plain, String bodyStr, String monthTo, String title, String unm, String sid, String uty, String men, String den,
                               String dnm, String bnm, String otherSetup, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    int[] hmenuCount = new int[1];

    String subMenuText = dashboardUtils.buildSubMenuText(con, stmt, rs, req, "6054", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount);

    subMenuText += "<table id='title' width=100%><tr><td colspan=2 nowrap><p>" + title + directoryUtils.buildHelp(6054) + "</td></tr></table>";

    subMenuText += buildSubMenuText(plain, monthTo, unm, sid, uty, men, den, dnm, bnm, hmenuCount);

    if(plain)
      scoutln(out, bytesOut, authenticationUtils.buildPlainScreen(con, stmt, rs, req, unm, sid, uty, men, den, dnm, bnm, "AccountsGenerateBSReportLive", subMenuText, hmenuCount[0], bodyStr, otherSetup, localDefnsDir, defnsDir));
    else
    {
      scoutln(out, bytesOut, authenticationUtils.buildScreen(con, stmt, rs, req, true, unm, sid, uty, men, den, dnm, bnm, "AccountsGenerateBSReportLive", subMenuText, hmenuCount[0], bodyStr, otherSetup, localDefnsDir, defnsDir));

      scoutln(out, bytesOut, "<table border=0><tr><td valign=top><div id='second' style=\"{margin-top:0px;}\"></div></td>");
      scoutln(out, bytesOut, "<td valign=top><div id='third' style=\"{margin-top:0px;margin-left:0px;}\"></div></td></tr></table>");
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String buildSubMenuText(boolean plain, String monthTo, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] hmenuCount) throws Exception
  {
    String s = "<div id='submenu'>";

    ++hmenuCount[0];

    if(! plain)
    {
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\">";
      s += "<a href=\"/central/servlet/AccountsGenerateBSReportLive?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + monthTo + "&p2=P\">Friendly</a></dt></dl>";
    }
    
    s += "</div>";
    
    --hmenuCount[0];
    
    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void checkForValuationAdjustment(Connection accCon, Connection ofsaCon, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, RandomAccessFile fhData, String account, String desc, String currency, String baseCurrency,
                                           String dateFrom, String dateTo, double ytdAmount, double[] ytdTotal, double[] va, String cssFormat, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    double nonBase, rate, diff, d;
    byte[] actualDate = new byte[20];
      
    if(! currency.equals(baseCurrency))
    {
      nonBase = nonBaseAmountForAnAccount(accCon, ofsaCon, stmt, stmt2, rs, rs2, dateFrom, dateTo, account);
      rate = accountsUtils.getApplicableRate(ofsaCon, stmt, rs, currency, generalUtils.convertFromYYYYMMDD(dateTo), actualDate, dnm, localDefnsDir, defnsDir);
        
      d = nonBase * rate;
              
      if(d < 0)
        d *= -1;
      
      diff = ytdAmount - d;
  
      diff *= -1;
      
      ytdTotal[0] += diff;
      va[0]       += diff;

      scoutln(fhData, "<tr bgcolor='" + cssFormat + "'><td><p>" + desc + " (Valuation Adjustment)</td>");
      scoutln(fhData, "<td align=right><p>" + generalUtils.formatNumeric(diff, '2') + "</td></tr>");
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private double nonBaseAmountForAnAccount(Connection accCon, Connection ofsaCon, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String dateFrom, String dateTo, String account) throws Exception
  {
    double d = 0.0;

    String currency = currencyForAccount(accCon, account);

    d += forReceipts(ofsaCon, stmt, rs, dateFrom, dateTo, account, currency);
    d -= forPayments(ofsaCon, stmt, rs, dateFrom, dateTo, account, currency);
    
    d += forReceiptVouchers(ofsaCon, stmt, rs, dateFrom, dateTo, account);
    d -= forReceiptVoucherLines(ofsaCon, stmt, rs, dateFrom, dateTo, account);
    
    d -= forPaymentVouchers(ofsaCon, stmt, rs, dateFrom, dateTo, account);
    d += forPaymentVoucherLines(ofsaCon, stmt, rs, dateFrom, dateTo, account);
  
    d += forIATDr(ofsaCon, stmt, rs, dateFrom, dateTo, account);
    d -= forIATCr(ofsaCon, stmt, rs, dateFrom, dateTo, account);

    double dd = 0.0;
    try
    {
      stmt = accCon.createStatement();
      
      rs = stmt.executeQuery("SELECT Code FROM joubatch WHERE Type != 'D' AND Type != 'C' ORDER BY Code"); 

      while(rs.next())
        dd += forBatchAnAccount(accCon, stmt2, rs2, rs.getString(1), dateFrom, dateTo, account);
     
      if(rs   != null) rs.close();                                 
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(rs   != null) rs.close();                                 
      if(stmt != null) stmt.close();
    }

    d += dd;

    return d;  
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void forDebtors(Connection accCon, Connection ofsaCon, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, RandomAccessFile fhData, String dateFrom, String dateTo, String debtorsAccount, String[] accCurrencies,
                          int numCurrencies, double ytdAmount, double[] ytdTotal, double[] va, String cssFormat, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    double[] accValsDr = new double[numCurrencies];
    double[] accValsCr = new double[numCurrencies];
    int x;
    for(x=0;x<numCurrencies;++x) { accValsDr[x] = 0.0; accValsCr[x] = 0.0; }

    forInvoices(ofsaCon, stmt, stmt2, rs, rs2, dateFrom, dateTo, accCurrencies, accValsDr, numCurrencies);
    
    forDebitNotes(ofsaCon, stmt, stmt2, rs, rs2, dateFrom, dateTo, accCurrencies, accValsDr, numCurrencies);
    
    forSalesCreditNotes(ofsaCon, stmt, stmt2, rs, rs2, dateFrom, dateTo, accCurrencies, accValsCr, numCurrencies);
    
    forReceiptsDebtors(ofsaCon, stmt, stmt2, rs, rs2, dateFrom, dateTo, accCurrencies, accValsCr, numCurrencies);

    double d = 0.0;

    d -= forReceiptVoucherLines(ofsaCon, stmt, rs, dateFrom, dateTo, debtorsAccount);
    d += forPaymentVoucherLines(ofsaCon, stmt, rs, dateFrom, dateTo, debtorsAccount);

    d += forIATDr(ofsaCon, stmt, rs, dateFrom, dateTo, debtorsAccount);
    d -= forIATCr(ofsaCon, stmt, rs, dateFrom, dateTo, debtorsAccount);

    try
    {
      stmt = accCon.createStatement();
      
      rs = stmt.executeQuery("SELECT Code FROM joubatch WHERE Type != 'D' AND Type != 'C' ORDER BY Code"); 

      while(rs.next())
        d += forBatchAnAccount(accCon, stmt2, rs2, rs.getString(1), dateFrom, dateTo, debtorsAccount);
     
      if(rs   != null) rs.close();                                 
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(rs   != null) rs.close();                                 
      if(stmt != null) stmt.close();
    }

    try
    {
      stmt = accCon.createStatement();
      
      rs = stmt.executeQuery("SELECT Code FROM joubatch WHERE Type = 'D' ORDER BY Code"); 

      while(rs.next())
        forBatchDebtorsOrCreditors(accCon, stmt, rs, rs.getString(1), dateFrom, dateTo, accCurrencies, accValsDr, accValsCr, numCurrencies);
     
      if(rs   != null) rs.close();                                 
      if(stmt != null) stmt.close();
    }
    catch(Exception e2)
    {
      System.out.println(e2);
      if(rs   != null) rs.close();                                 
      if(stmt != null) stmt.close();
    }

    double rate;
    byte[] actualDate = new byte[20];
    
    for(x=0;x<numCurrencies;++x)
    { 
      rate = accountsUtils.getApplicableRate(ofsaCon, stmt, rs, accCurrencies[x], generalUtils.convertFromYYYYMMDD(dateTo), actualDate, dnm, localDefnsDir, defnsDir);
      d += ((accValsDr[x] - accValsCr[x]) * rate);
    }

    double diff = ytdAmount - d;
  
    diff *= -1;
    
    ytdTotal[0] += diff;
    va[0]       += diff;

    scoutln(fhData, "<tr bgcolor='" + cssFormat + "'><td><p>Trade Debtors (Valuation Adjustment)</td>");
    scoutln(fhData, "<td align=right><p>" + generalUtils.formatNumeric(diff, '2') + "</td></tr>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void forCreditors(Connection accCon, Connection ofsaCon, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, RandomAccessFile fhData,
                            String dateFrom, String dateTo, String creditorsAccount, String[] accCurrencies, int numCurrencies, double ytdAmount,
                            double[] ytdTotal, double[] va, String cssFormat, String dnm, String localDefnsDir, String defnsDir, int[] bytesOut)
                            throws Exception
  {
    double[] accValsDr = new double[numCurrencies];
    double[] accValsCr = new double[numCurrencies];
    int x;
    for(x=0;x<numCurrencies;++x) { accValsDr[x] = 0.0; accValsCr[x] = 0.0; }

    forPurchaseInvoices(ofsaCon, stmt, stmt2, rs, rs2, dateFrom, dateTo, accCurrencies, accValsCr, numCurrencies);
    
    forPurchaseDebitNotes(ofsaCon, stmt, stmt2, rs, rs2, dateFrom, dateTo, accCurrencies, accValsCr, numCurrencies);
    
    forPurchaseCreditNotes(ofsaCon, stmt, stmt2, rs, rs2, dateFrom, dateTo, accCurrencies, accValsDr, numCurrencies);
    
    forPaymentsCreditors(ofsaCon, stmt, stmt2, rs, rs2, dateFrom, dateTo, accCurrencies, accValsDr, numCurrencies);
    
    double d = 0.0;

    d -= forReceiptVoucherLines(ofsaCon, stmt, rs, dateFrom, dateTo, creditorsAccount);

    d += forPaymentVoucherLines(ofsaCon, stmt, rs, dateFrom, dateTo, creditorsAccount);

    d += forIATDr(ofsaCon, stmt, rs, dateFrom, dateTo, creditorsAccount);
    d -= forIATCr(ofsaCon, stmt, rs, dateFrom, dateTo, creditorsAccount);
    
    try
    {
      stmt = accCon.createStatement();
      
      rs = stmt.executeQuery("SELECT Code FROM joubatch WHERE Type != 'D' AND Type != 'C' ORDER BY Code"); 

      while(rs.next())
        d += forBatchAnAccount(accCon, stmt2, rs2, rs.getString(1), dateFrom, dateTo, creditorsAccount);
     
      if(rs   != null) rs.close();                                 
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(rs   != null) rs.close();                                 
      if(stmt != null) stmt.close();
    }

    try
    {
      stmt = accCon.createStatement();
      
      rs = stmt.executeQuery("SELECT Code FROM joubatch WHERE Type = 'C' ORDER BY Code"); 

      while(rs.next())
        forBatchDebtorsOrCreditors(accCon, stmt, rs, rs.getString(1), dateFrom, dateTo, accCurrencies, accValsDr, accValsCr, numCurrencies);
     
      if(rs   != null) rs.close();                                 
      if(stmt != null) stmt.close();
    }
    catch(Exception e2)
    {
      System.out.println(e2);
      if(rs   != null) rs.close();                                 
      if(stmt != null) stmt.close();
    }

    double rate;
    byte[] actualDate = new byte[20];
    
    for(x=0;x<numCurrencies;++x)
    { 
      rate = accountsUtils.getApplicableRate(ofsaCon, stmt, rs, accCurrencies[x], generalUtils.convertFromYYYYMMDD(dateTo), actualDate, dnm, localDefnsDir, defnsDir);

      d += ((accValsDr[x] - accValsCr[x]) * rate);
    }
    
    double diff = ytdAmount + d;
  
    diff *= -1;
    
    ytdTotal[0] += diff;
    va[0]       += diff;

    scoutln(fhData, "<tr bgcolor='" + cssFormat + "'><td><p>Trade Creditors (Valuation Adjustment)</td>");
    scoutln(fhData, "<td align=right><p>" + generalUtils.formatNumeric(diff, '2') + "</td></tr>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void forInvoices(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String dateFrom, String dateTo, String[] accCurrencies,
                           double[] accVals, int numCurrencies) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT CompanyCode, TotalTotal, Currency FROM invoice WHERE Date >= {d '" + dateFrom + "'} AND Date <= {d '" + dateTo + "'} AND Status != 'C' AND Settled != 'Y'");

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
  private void forPurchaseInvoices(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String dateFrom, String dateTo, String[] accCurrencies, double[] accVals, int numCurrencies) throws Exception
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
  private void forDebitNotes(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String dateFrom, String dateTo, String[] accCurrencies, double[] accVals, int numCurrencies) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT CompanyCode, TotalTotal, Currency FROM debit WHERE Date >= {d '" + dateFrom + "'} AND Date <= {d '" + dateTo + "'} AND Status != 'C' AND Settled != 'Y'");

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
  private void forPurchaseDebitNotes(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String dateFrom, String dateTo, String[] accCurrencies, double[] accVals, int numCurrencies) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT CompanyCode, TotalTotal, Currency FROM pdebit WHERE Date >= {d '" + dateFrom + "'} AND Date <= {d '" + dateTo + "'} AND Status != 'C' AND Settled != 'Y'");

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
  private void forSalesCreditNotes(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String dateFrom, String dateTo, String[] accCurrencies, double[] accVals, int numCurrencies) throws Exception
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
  private void forPurchaseCreditNotes(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String dateFrom, String dateTo, String[] accCurrencies, double[] accVals, int numCurrencies) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT CompanyCode, TotalTotal, Currency FROM pcredit WHERE Date >= {d '" + dateFrom + "'} AND Date <= {d '" + dateTo
                         + "'} AND Status != 'C'");

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
  private double forReceiptVouchers(Connection con, Statement stmt, ResultSet rs, String dateFrom, String dateTo, String account) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT TotalTotal FROM rvoucher "
                         + "WHERE Status != 'C' AND Date >= {d '" + dateFrom + "'} AND Date <= {d '" + dateTo + "'} AND AccountDr = '" + account + "'");

    double totalTotal = 0.0;
    
    while(rs.next())
      totalTotal += generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(1)), '2');

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return totalTotal;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private double forReceiptVoucherLines(Connection con, Statement stmt, ResultSet rs, String dateFrom, String dateTo, String account) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.Amount2 FROM rvoucherl AS t2 INNER JOIN rvoucher AS t1 ON t2.VoucherCode = t1.VoucherCode "
                         + "WHERE t1.Date >= {d '" + dateFrom + "'} AND t1.Date <= {d '" + dateTo + "'} AND t1.Status != 'C' AND t2.AccountCr = '" + account + "'");

    double amount2 = 0.0;
    
    while(rs.next())
      amount2 += generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(1)), '2');

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return amount2;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private double forPaymentVouchers(Connection con, Statement stmt, ResultSet rs, String dateFrom, String dateTo, String account) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT TotalTotal FROM voucher "
                         + "WHERE Status != 'C' AND Date >= {d '" + dateFrom + "'} AND Date <= {d '" + dateTo + "'} AND AccountCr = '" + account + "'");

    double totalTotal = 0.0;
    
    while(rs.next())
      totalTotal += generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(1)), '2');
      
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return totalTotal;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private double forPaymentVoucherLines(Connection con, Statement stmt, ResultSet rs, String dateFrom, String dateTo, String account) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.Amount2 FROM voucherl AS t2 INNER JOIN voucher AS t1 "
                         + "ON t2.VoucherCode = t1.VoucherCode WHERE t1.Date >= {d '" + dateFrom + "'} AND t1.Date <= {d '"
                         + dateTo + "'} AND t1.Status != 'C' AND t2.AccountDr = '" + account + "'");

    double amount2 = 0.0;
    
    while(rs.next())
      amount2 += generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(1)), '2');

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return amount2;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private double forReceipts(Connection con, Statement stmt, ResultSet rs, String dateFrom, String dateTo, String account, String currency) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT BankedAmount, Currency, Rate FROM receipt WHERE Status != 'C' AND AccDebited = '" + account + "' AND DateReceived >= {d '" + dateFrom + "'} AND DateReceived <= {d '" + dateTo + "'}");

    double amount, rate, totalAmount = 0.0;

    while(rs.next())
    {
      amount = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(1)), '2');
      if(! currency.equals(rs.getString(2)))
      {
        rate = generalUtils.doubleFromStr(rs.getString(3));
        amount *= rate;
      }

      totalAmount += generalUtils.doubleDPs(amount, '2');
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return totalAmount;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void forReceiptsDebtors(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String dateFrom, String dateTo, String[] accCurrencies, double[] accVals, int numCurrencies) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT CompanyCode, TotalAmount, Currency FROM receipt WHERE Status != 'C' AND DateReceived >= {d '" + dateFrom + "'} AND DateReceived <= {d '" + dateTo + "'}");

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

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void forPaymentsCreditors(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String dateFrom, String dateTo, String[] accCurrencies, double[] accVals, int numCurrencies) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT CompanyCode, TotalAmount, Currency FROM payment WHERE Status != 'C' AND DatePaid >= {d '" + dateFrom + "'} AND DatePaid <= {d '" + dateTo + "'}");

    double totalAmount;
    
    while(rs.next())
    {
      if(existsSupplier(con, stmt2, rs2, rs.getString(1)))
      {
        totalAmount = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(2)), '2');
        
        updateValue(totalAmount, rs.getString(3), accCurrencies, accVals, numCurrencies);
      }
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private double forPayments(Connection con, Statement stmt, ResultSet rs, String dateFrom, String dateTo, String account, String currency) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT BankAmount, Charges, Currency, Rate FROM payment WHERE Status != 'C' AND AccCredited = '" + account + "' AND DatePaid >= {d '" + dateFrom + "'} AND DatePaid <= {d '" + dateTo + "'}");

    double amount, rate, totalAmount = 0.0;
    
    while(rs.next())
    {
      amount = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(1)), '2');      
      if(! currency.equals(rs.getString(3)))
      {
        rate = generalUtils.doubleFromStr(rs.getString(4));
        amount /= rate;
      }

      totalAmount += generalUtils.doubleDPs(amount, '2');

      totalAmount += generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(2)), '2');
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return totalAmount;
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private double forIATDr(Connection con, Statement stmt, ResultSet rs, String dateFrom, String dateTo, String account) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT AmountDr, ChargesDr FROM iat WHERE AccountDr = '" + account + "' AND Status != 'C' AND TransactionDate >= {d '" + dateFrom + "'} AND TransactionDate <= {d '" + dateTo + "'}");
    
    double amount = 0.0;

    while(rs.next())
    {
      amount += generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(1)), '2');
      amount -= generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(2)), '2');
    }

    if(rs   != null) rs.close();                                 
    if(stmt != null) stmt.close();
    
    return amount;
  }
    
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private double forIATCr(Connection con, Statement stmt, ResultSet rs, String dateFrom, String dateTo, String account) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT AmountCr, ChargesCr FROM iat WHERE AccountCr = '" + account + "' AND Status != 'C' AND TransactionDate >= {d '" + dateFrom + "'} AND TransactionDate <= {d '" + dateTo + "'}");
    
    double amount = 0.0;

    while(rs.next())
    {
      amount += generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(1)), '2');
      amount += generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(2)), '2');
    }

    if(rs   != null) rs.close();                                 
    if(stmt != null) stmt.close();
    
    return amount;
  }
    
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void forBatchDebtorsOrCreditors(Connection con, Statement stmt, ResultSet rs, String code, String dateFrom, String dateTo, String[] accCurrencies, double[] accValsDr, double[] accValsCr, int numCurrencies) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT Amount, DrCr, Currency FROM joubatchl WHERE Code = '" + generalUtils.sanitiseForSQL(code) + "' AND TransactionDate >= {d '" + dateFrom + "'} AND TransactionDate <= {d '" + dateTo + "'}");
  
    double amount;
    
    while(rs.next())
    {
      amount = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(1)), '2');
 
      if(rs.getString(2).equals("D"))
        updateValue(amount, rs.getString(3), accCurrencies, accValsDr, numCurrencies);
      else updateValue(amount, rs.getString(3), accCurrencies, accValsCr, numCurrencies);
    }

    if(rs   != null) rs.close();                                 
    if(stmt != null) stmt.close();
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private double forBatchAnAccount(Connection con, Statement stmt, ResultSet rs, String code, String dateFrom, String dateTo, String account) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT Amount, DrCr FROM joubatchl WHERE Code = '" + generalUtils.sanitiseForSQL(code) + "' AND TransactionDate >= {d '" + dateFrom + "'} AND TransactionDate <= {d '" + dateTo + "'} AND AccCode = '" + account + "'");
  
    double amount = 0.0, thisAmount;
    
    while(rs.next())
    {
      thisAmount = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(1)), '2');
      if(rs.getString(2).equals("D"))
        amount += thisAmount;
      else amount -= thisAmount;
    }

    if(rs   != null) rs.close();                                 
    if(stmt != null) stmt.close();
  
    return amount;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
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

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
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

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void updateValue(double val, String thisCurrency, String[] accCurrencies, double[] accVals, int numCurrencies) throws Exception
  {
    int x=0;

    while(x < numCurrencies && ! accCurrencies[x].equals(thisCurrency)) // just-in-case
      ++x;
    
    if(x == numCurrencies) // not found!
      System.out.println("Currency Not Found: >" + thisCurrency + "< " + val);
    else
    {
      val = generalUtils.doubleDPs(val, '2');
      accVals[x] += val;
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void keepChecking(PrintWriter out, String servlet, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {        
    scoutln(out, bytesOut, "function chkTimer(){chkTimerID=self.setTimeout('chk()',2000);}");
      
    scoutln(out, bytesOut, "var chkreq2;");    
    scoutln(out, bytesOut, "function initRequest2(url)");
    scoutln(out, bytesOut, "{if(window.XMLHttpRequest){chkreq2=new XMLHttpRequest();}else");
    scoutln(out, bytesOut, "if(window.ActiveXObject){chkreq2=new ActiveXObject(\"Microsoft.XMLHTTP\");}}");

    scoutln(out, bytesOut, "function chk(){");
    scoutln(out, bytesOut, "var url=\"http://" + men + "/central/servlet/FaxStatusDataFromReportTemp?p1=" + servlet + "&unm=\" + escape('" + unm + "') + \"&sid=\" + escape('" + sid + "') + \"&uty=\" + escape('" + uty + "') + \"&men=\" + escape('" + men
                         + "') + \"&den=\" + escape('" + den + "') + \"&bnm=\" + escape('" + bnm + "') + \"&dnm=\" + escape('" + dnm + "');");
    scoutln(out, bytesOut, "initRequest2(url);");
    scoutln(out, bytesOut, "chkreq2.onreadystatechange=processRequest2;");
    scoutln(out, bytesOut, "chkreq2.open(\"GET\",url,true);");
    scoutln(out, bytesOut, "chkreq2.send(null);}");
 
    scoutln(out, bytesOut, "function processRequest2(){");
    scoutln(out, bytesOut, "if(chkreq2.readyState==4){");
    scoutln(out, bytesOut, "if(chkreq2.status==200){");
    scoutln(out, bytesOut, "var res=chkreq2.responseXML.getElementsByTagName(\"res\")[0].childNodes[0].nodeValue;");
    scoutln(out, bytesOut, "if(res.length>0){");
    scoutln(out, bytesOut, "if(res=='.')clearTimeout(chkTimerID);else chkTimer();");
    scoutln(out, bytesOut, "var s=chkreq2.responseXML.getElementsByTagName(\"stuff\")[0].childNodes[0].nodeValue;");
    scoutln(out, bytesOut, "var messageElement=document.getElementById('stuff');");
    scoutln(out, bytesOut, "messageElement.innerHTML=s;");
    scoutln(out, bytesOut, "}}}}");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private String currencyForAccount(Connection accCon, String account) throws Exception
  {
    Statement stmt = null;
    ResultSet rs   = null;

    stmt = accCon.createStatement();

    rs = stmt.executeQuery("SELECT Currency FROM acctdefn WHERE AccCode = '" + account + "'");
    String currency = "";

    if(rs.next())
      currency = rs.getString(1);
    else System.out.println("no currency for " + account);

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    return currency;
  }

}

