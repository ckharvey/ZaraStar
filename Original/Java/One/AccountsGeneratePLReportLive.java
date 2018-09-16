// =======================================================================================================================================================================================================
// System: ZaraStar: Accounts: Generate P&L report - live
// Module: AccountsGeneratePLReportLive.java
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

public class AccountsGeneratePLReportLive extends HttpServlet
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "AccountsGeneratePLReportLive", bytesOut);
      System.out.println(e);
      serverUtils.etotalBytes(req, unm, dnm, 6055, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush();
    }
  }

  // ------------------------------------------------------------------------------------------------------------------------------------------------------
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

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 6055, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "6055e", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6055, bytesOut[0], 0, "ACC:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "6055e", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6055, bytesOut[0], 0, "SID:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }


    scoutln(out, bytesOut, "<html><head><title>P&L</title></head>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    scoutln(out, bytesOut, "function gl(account,dateFrom,dateTo){");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/AccountsViewGLAccountFromTB?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+account+\"&p2=\"+dateFrom+\"&p3=\"+dateTo+\"\";}");

    boolean plain;
    if(p2.equals("P"))
      plain = true;
    else plain = false;

    if(plain)
    {
      scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"print\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "generalPlain.css\">");
    }
    else
    {
      scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");
    }

    outputPageFrame(con, stmt, rs, out, req, plain, "", p1, "Profit &amp; Loss", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, bytesOut);

    scoutln(out, bytesOut, "<table id=\"page\" border=0 width=100%>");

    if(plain)
    {
      scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
      scoutln(out, bytesOut, "<tr><td colspan=4><p><b>Profit & Loss: " + p1 + "</td></tr>");
      scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    }

    int[] month = new int[1];
    int[] year  = new int[1];

    generalUtils.monthYearStrToYearAndMonth2(p1, month, year);

    String monthRequired = generalUtils.lastDayOfMonthYYYYMMDD(year[0] + "-" + month[0] + "-" + "1");
   
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
      
    generate(out, yearStartDate, yearEndDate, reqdMonth, monthRequired, financialYearStartDate, previousFinancialYearStartDate, dnm, unm, workingDir, localDefnsDir, defnsDir, bytesOut);
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir));

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 6055, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void generate(PrintWriter out, String dateFrom, String dateTo, int monthNumReqd, String monthRequired, String financialYearStartDate,
          String previousFinancialYearStartDate, String dnm, String unm, String workingDir, String localDefnsDir, String defnsDir, int[] bytesOut)
                        throws Exception
  {
    String yyyy = generalUtils.getFromDefnFile(unm, "accounts.dfn", workingDir, "");

    generate(out, yyyy, dateFrom, dateTo, monthNumReqd, monthRequired, financialYearStartDate, previousFinancialYearStartDate, dnm, unm, workingDir, localDefnsDir, defnsDir, bytesOut);
  }
  public double generate(PrintWriter out, String yyyy, String dateFrom, String dateTo, int monthNumReqd, String monthRequired,
          String financialYearStartDate, String previousFinancialYearStartDate, String dnm, String unm, String workingDir, String localDefnsDir, String defnsDir, int[] bytesOut)
                         throws Exception
  {
    double grossYTDTotal = 0.0;

    String[][] accCodes = new String[1][];
    double[][][] accValsDr = new double[1][][];
    double[][][] accValsCr = new double[1][][];
    int[] numAccs = new int[1];
    char[][] accTypes = new char[1][];
        
    int[] monthEnds = new int[13];
    int[] numMonths = new int[1];

    Connection accCon = null, ofsaCon  = null;
    Statement accStmt = null, ofsaStmt = null, accStmt2 = null;
    ResultSet accRs   = null, ofsaRs   = null, accRs2   = null;
    
    numAccs[0] = 0;

    try
    {
      String uName = directoryUtils.getMySQLUserName();
      String pWord = directoryUtils.getMySQLPassWord();
      Class.forName("com.mysql.jdbc.Driver").newInstance();
      accCon = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_accounts_" + yyyy + "?user=" + uName + "&password=" + pWord);

      ofsaCon = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa" + "?user=" + uName + "&password=" + pWord);

      allAccounts(accCon, ofsaCon, accStmt, ofsaStmt, accRs, ofsaRs, yyyy, dateFrom, dateTo, financialYearStartDate, previousFinancialYearStartDate, dnm, localDefnsDir, defnsDir, accCodes, accValsDr, accValsCr, numAccs, accTypes, monthEnds,
                 numMonths,      unm);

      scoutln(out, bytesOut, "<tr><td></td><td align=right><p><b>Month (" + monthRequired + ")</b></td><td align=right><p><b>Year (" + dateTo + ")</b></td>");

      double[] monthTotal = new double[1];
      double[] ytdTotal   = new double[1];
      
      // Sales Revenue
      
      double salesMonthTotal, salesYTDTotal;

      scoutln(out, bytesOut, "<tr><td><p><b>Sales Revenue</b></td></tr>");

      monthTotal[0] = ytdTotal[0] = 0.0;

      showSales(accCon, accStmt, accRs, out, monthNumReqd, accCodes[0], accValsDr[0], accValsCr[0], numAccs[0], bytesOut, monthTotal, ytdTotal);
      
      salesMonthTotal = monthTotal[0];
      salesYTDTotal   = ytdTotal[0];

      monthTotal[0] = ytdTotal[0] = 0.0;

      showDiscountGiven(accCon, accStmt, accRs, out, monthNumReqd, accCodes[0], accValsDr[0], accValsCr[0], numAccs[0], bytesOut, monthTotal, ytdTotal);
      
      salesMonthTotal += monthTotal[0];
      salesYTDTotal   += ytdTotal[0];

      salesMonthTotal *= -1;
      salesYTDTotal   *= -1;
      scoutln(out, bytesOut, "<tr bgcolor=\"#D0D0D0\"><td><p>Total Sales</td>");
      scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(salesMonthTotal, '2') + "</td>");
      scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(salesYTDTotal, '2') + "</td></tr>");

      // Cost of Sales
      
      double costOfSalesMonthTotal, costOfSalesYTDTotal;

      scoutln(out, bytesOut, "<tr><td><p><b>Less: Cost of Sales</b></td></tr>");

      monthTotal[0] = 0.0; ytdTotal[0] = 0.0;
      
      showCostOfSales(accCon, accStmt, accRs, out, monthNumReqd, accCodes[0], accValsDr[0], accValsCr[0], numAccs[0], bytesOut, monthTotal, ytdTotal);
      
      costOfSalesMonthTotal = monthTotal[0];
      costOfSalesYTDTotal   = ytdTotal[0];

      monthTotal[0] = 0.0; ytdTotal[0] = 0.0;

      showCarriageOutward(accCon, accStmt, accRs, out, monthNumReqd, accCodes[0], accValsDr[0], accValsCr[0], numAccs[0], bytesOut, monthTotal, ytdTotal);
      
      costOfSalesMonthTotal += monthTotal[0];
      costOfSalesYTDTotal   += ytdTotal[0];

      monthTotal[0] = 0.0; ytdTotal[0] = 0.0;

      showCarriageInward(accCon, accStmt, accRs, out, monthNumReqd, accCodes[0], accValsDr[0], accValsCr[0], numAccs[0], bytesOut, monthTotal, ytdTotal);
      
      costOfSalesMonthTotal += monthTotal[0];
      costOfSalesYTDTotal   += ytdTotal[0];

      monthTotal[0] = 0.0; ytdTotal[0] = 0.0;

      showDelivery(accCon, accStmt, accRs, out, monthNumReqd, accCodes[0], accValsDr[0], accValsCr[0], numAccs[0], bytesOut, monthTotal, ytdTotal);
      
      costOfSalesMonthTotal += monthTotal[0];
      costOfSalesYTDTotal   += ytdTotal[0];

      monthTotal[0] = 0.0; ytdTotal[0] = 0.0;

      showExchangeDifference(accCon, accStmt, accRs, out, monthNumReqd, accCodes[0], accValsDr[0], accValsCr[0], numAccs[0], bytesOut, monthTotal, ytdTotal);
      
      costOfSalesMonthTotal += monthTotal[0];
      costOfSalesYTDTotal   += ytdTotal[0];

      monthTotal[0] = 0.0; ytdTotal[0] = 0.0;

      showDiscountReceived(accCon, accStmt, accRs, out, monthNumReqd, accCodes[0], accValsDr[0], accValsCr[0], numAccs[0], bytesOut, monthTotal, ytdTotal);

      costOfSalesMonthTotal += monthTotal[0];
      costOfSalesYTDTotal   += ytdTotal[0];

      monthTotal[0] = ytdTotal[0] = 0.0;

      scoutln(out, bytesOut, "<tr bgcolor=\"#D0D0D0\"><td><p>Total Cost of Sales</td>");
      scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(costOfSalesMonthTotal, '2') + "</td>");
      scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(costOfSalesYTDTotal, '2') + "</td></tr>");

      // Other Income (Operating)
      
      double otherOperatingIncomeMonthTotal, otherOperatingIncomeYTDTotal;

      scoutln(out, bytesOut, "<tr><td><p><b>Plus: Other Income</b></td></tr>");

      monthTotal[0] = ytdTotal[0] = 0.0;
      
      showOtherOperatingIncome(accCon, accStmt, accStmt2, accRs, accRs2, out, monthNumReqd, accCodes[0], accValsDr[0], accValsCr[0], numAccs[0], bytesOut, monthTotal, ytdTotal);
      
      otherOperatingIncomeMonthTotal = monthTotal[0];
      otherOperatingIncomeYTDTotal   = ytdTotal[0];

      otherOperatingIncomeMonthTotal *= -1;
      otherOperatingIncomeYTDTotal   *= -1;
      scoutln(out, bytesOut, "<tr bgcolor=\"#D0D0D0\"><td><p>Total Other Income</td>");
      scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(otherOperatingIncomeMonthTotal, '2') + "</td>");
      scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(otherOperatingIncomeYTDTotal, '2') + "</td></tr>");

      // Gross Profit/Loss      

      double grossMonthTotal;
      
      grossMonthTotal = salesMonthTotal - costOfSalesMonthTotal + otherOperatingIncomeMonthTotal;
      grossYTDTotal   = salesYTDTotal   - costOfSalesYTDTotal   + otherOperatingIncomeYTDTotal;

      scoutln(out, bytesOut, "<tr bgcolor=\"#B0B0B0\"><td><p>Gross Profit/Loss</td>");
      scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(grossMonthTotal, '2') + "</td>");
      scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(grossYTDTotal, '2') + "</td></tr>");

      // Expenses

      double expensesMonthTotal, expensesYTDTotal;
           
      scoutln(out, bytesOut, "<tr><td><p><b>Less: Expenses</b></td></tr>");

      monthTotal[0] = ytdTotal[0] = 0.0;

      showExpenses(accCon, accStmt, accStmt2, accRs, accRs2, out, monthNumReqd, accCodes[0], accValsDr[0], accValsCr[0], numAccs[0], bytesOut, monthTotal, ytdTotal);

      expensesMonthTotal = monthTotal[0];
      expensesYTDTotal   = ytdTotal[0];

      scoutln(out, bytesOut, "<tr bgcolor=\"#D0D0D0\"><td><p>Total Operating Expenses</td>");
      scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(expensesMonthTotal, '2') + "</td>");
      scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(expensesYTDTotal, '2') + "</td></tr>");
      
      grossMonthTotal -= expensesMonthTotal;
      grossYTDTotal   -= expensesYTDTotal;

      // Net Operating Profit/Loss
      
      scoutln(out, bytesOut, "<tr bgcolor=\"#B0B0B0\"><td><p>Net Operating Profit/Loss</td>");
      scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(grossMonthTotal, '2') + "</td>");
      scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(grossYTDTotal, '2') + "</td></tr>");

      // Non-Operating Income
            
      double nonOperatingIncomeMonthTotal, nonOperatingIncomeYTDTotal;
      
      scoutln(out, bytesOut, "<tr><td><p><b>Plus: Non-Operating Income</b></td></tr>");

      monthTotal[0] = ytdTotal[0] = 0.0;

      showNonOperatingIncome(accCon, accStmt, accStmt2, accRs, accRs2, out, monthNumReqd, accCodes[0], accValsDr[0], accValsCr[0], numAccs[0], bytesOut, monthTotal, ytdTotal);
      
      nonOperatingIncomeMonthTotal = monthTotal[0];
      nonOperatingIncomeYTDTotal   = ytdTotal[0];

      nonOperatingIncomeMonthTotal *= -1;
      nonOperatingIncomeYTDTotal   *= -1;
      scoutln(out, bytesOut, "<tr bgcolor=\"#D0D0D0\"><td><p>Total Non-Operating Income</td>");
      scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(nonOperatingIncomeMonthTotal, '2') + "</td>");
      scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(nonOperatingIncomeYTDTotal, '2') + "</td></tr>");

      grossMonthTotal += nonOperatingIncomeMonthTotal;
      grossYTDTotal   += nonOperatingIncomeYTDTotal;

      // Provision for Income Tax

      monthTotal[0] = ytdTotal[0] = 0.0;

      scoutln(out, bytesOut, "<tr><td><p><b>Less: Provision for Income Tax</b></td></tr>");

      showProvisionForIncomeTax(accCon, accStmt, accRs, monthNumReqd, accCodes[0], accValsDr[0], accValsCr[0], numAccs[0], monthTotal, ytdTotal);

      scoutln(out, bytesOut, "<tr bgcolor=\"#D0D0D0\"><td><p>Total Provision for Income Tax</td>");
      scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(monthTotal[0], '2') + "</td>");
      scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(ytdTotal[0], '2') + "</td></tr>");

      grossMonthTotal -= monthTotal[0];
      grossYTDTotal   -= ytdTotal[0];

      // Provision for Income Tax

      monthTotal[0] = ytdTotal[0] = 0.0;

      scoutln(out, bytesOut, "<tr><td><p><b>Less: Provision for Deferred Tax</b></td></tr>");

      showProvisionForDeferredTax(accCon, accStmt, accRs, monthNumReqd, accCodes[0], accValsDr[0], accValsCr[0], numAccs[0], monthTotal, ytdTotal);

      scoutln(out, bytesOut, "<tr bgcolor=\"#D0D0D0\"><td><p>Total Provision for Deferred Tax</td>");
      scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(monthTotal[0], '2') + "</td>");
      scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(ytdTotal[0], '2') + "</td></tr>");

      grossMonthTotal -= monthTotal[0];
      grossYTDTotal   -= ytdTotal[0];

      // Provision for Dividend
            
      monthTotal[0] = ytdTotal[0] = 0.0;
      
      scoutln(out, bytesOut, "<tr><td><p><b>Less: Net Proposed Dividend</b></td></tr>");

      showProvisionForDividend(accCon, accStmt, accRs, monthNumReqd, accCodes[0], accValsDr[0], accValsCr[0], numAccs[0], monthTotal, ytdTotal);
      
      scoutln(out, bytesOut, "<tr bgcolor=\"#D0D0D0\"><td><p>Total Proposed Dividend</td>");
      scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(monthTotal[0], '2') + "</td>");
      scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(ytdTotal[0], '2') + "</td></tr>");

      grossMonthTotal += monthTotal[0];
      grossYTDTotal   += ytdTotal[0];

      // Profit/Loss      
      
      scoutln(out, bytesOut, "<tr bgcolor=\"#B0B0B0\"><td><p>Profit/Loss</td>");
      scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(grossMonthTotal, '2') + "</td>");
      scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(grossYTDTotal, '2') + "</td></tr>");

      if(ofsaCon != null) ofsaCon.close();
      if(accCon  != null) accCon.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(ofsaCon != null) ofsaCon.close();
      if(accCon  != null) accCon.close();
    }

    return grossYTDTotal;
  }
      
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void showSales(Connection con, Statement stmt, ResultSet rs, PrintWriter out, int month, String[] accCodes, double[][] accValsDr, double[][] accValsCr, int numAccs, int[] bytesOut, double[] monthTotal, double[] ytdTotal)
                         throws Exception
  {
    // Income - Sales
    // Income - Sales Category

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT AccCode FROM acctdefn WHERE (Category = 'Income - Sales' OR Category = 'Income - Sales Category') AND Active = 'Y'"); 

    double monthAmount = 0.0, ytdAmount = 0.0;
    String accCode;
      
    while(rs.next())
    {
      accCode = rs.getString(1);
 
      monthAmount += getAccAmt(accCode, accCodes, accValsDr[month], accValsCr[month], numAccs);
      ytdAmount   += getAccAmt(accCode, accCodes, accValsDr[13],    accValsCr[13],    numAccs);
    }

    monthTotal[0] += monthAmount;
    ytdTotal[0]   += ytdAmount;

    monthAmount *= -1;
    ytdAmount   *= -1;
    scoutln(out, bytesOut, "<tr bgcolor=\"#F0F0F0\"><td><p>Sales</td>");
    scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(monthAmount, '2') + "</td>");
    scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(ytdAmount, '2') + "</td></tr>");

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();    

    // Income - Sales Returned

    monthAmount = ytdAmount = 0.0;

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT AccCode FROM acctdefn WHERE Category = 'Income - Sales Returned' AND Active = 'Y'"); 

    while(rs.next())
    {
      accCode = rs.getString(1);
 
      monthAmount += getAccAmt(accCode, accCodes, accValsDr[month], accValsCr[month], numAccs);
      ytdAmount   += getAccAmt(accCode, accCodes, accValsDr[13],    accValsCr[13],    numAccs);
    }      
    
    monthTotal[0] += monthAmount;
    ytdTotal[0]   += ytdAmount;

    monthAmount *= -1;
    ytdAmount   *= -1;
    scoutln(out, bytesOut, "<tr bgcolor=\"#F0F0F0\"><td><p>Sales Returned</td>");
    scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(monthAmount, '2') + "</td>");
    scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(ytdAmount, '2') + "</td></tr>");
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();    
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void showDiscountGiven(Connection con, Statement stmt, ResultSet rs, PrintWriter out, int month, String[] accCodes, double[][] accValsDr, double[][] accValsCr, int numAccs, int[] bytesOut, double[] monthTotal, double[] ytdTotal)
                                 throws Exception
  {
    // Income - Discount Given
            
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT AccCode FROM acctdefn WHERE Category = 'Income - Discount Given' AND Active = 'Y'"); 

    double monthAmount = 0.0, ytdAmount = 0.0;
    String accCode;
      
    while(rs.next())
    {
      accCode = rs.getString(1);
 
      monthAmount += getAccAmt(accCode, accCodes, accValsDr[month], accValsCr[month], numAccs);
      ytdAmount   += getAccAmt(accCode, accCodes, accValsDr[13],    accValsCr[13],    numAccs);
    }
    
    monthTotal[0] += monthAmount;
    ytdTotal[0]   += ytdAmount;

    monthAmount *= -1;
    ytdAmount   *= -1;
    scoutln(out, bytesOut, "<tr bgcolor=\"#F0F0F0\"><td><p>Discount Given</td>");
    scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(monthAmount, '2') + "</td>");
    scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(ytdAmount, '2') + "</td></tr>");

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();    
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void showCostOfSales(Connection con, Statement stmt, ResultSet rs, PrintWriter out, int month, String[] accCodes, double[][] accValsDr, double[][] accValsCr, int numAccs, int[] bytesOut, double[] monthTotal, double[] ytdTotal)
                               throws Exception
  {
    // Cost of Sales
    // Cost of Sales - Purchases
    // Cost of Sales - Purchases Category

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT AccCode FROM acctdefn WHERE (Category = 'Cost of Sales' OR Category = 'Cost of Sales - Purchases' OR Category = 'Cost of Sales - Purchases Category') AND Active = 'Y'"); 

    double monthAmount = 0.0, ytdAmount = 0.0;
    String accCode;
      
    while(rs.next())
    {
      accCode = rs.getString(1);
 
      monthAmount += getAccAmt(accCode, accCodes, accValsDr[month], accValsCr[month], numAccs);
      ytdAmount   += getAccAmt(accCode, accCodes, accValsDr[13],    accValsCr[13],    numAccs);
    }
    
    scoutln(out, bytesOut, "<tr bgcolor=\"#F0F0F0\"><td><p>Cost of Sales</td>");
    scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(monthAmount, '2') + "</td>");
    scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(ytdAmount, '2') + "</td></tr>");

    monthTotal[0] += monthAmount;
    ytdTotal[0]   += ytdAmount;

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();    

    // Cost of Sales - Purchases Returned

    monthAmount = ytdAmount = 0.0;

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT AccCode FROM acctdefn WHERE Category = 'Cost of Sales - Purchases Returned' AND Active = 'Y'"); 

    while(rs.next())
    {
      accCode = rs.getString(1);
 
      monthAmount += getAccAmt(accCode, accCodes, accValsDr[month], accValsCr[month], numAccs);
      ytdAmount   += getAccAmt(accCode, accCodes, accValsDr[13],    accValsCr[13],    numAccs);
    }
    
    scoutln(out, bytesOut, "<tr bgcolor=\"#F0F0F0\"><td><p>Purchases Returned</td>");
    scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(monthAmount, '2') + "</td>");
    scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(ytdAmount, '2') + "</td></tr>");

    monthTotal[0] += monthAmount;
    ytdTotal[0]   += ytdAmount;
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();    
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void showCarriageOutward(Connection con, Statement stmt, ResultSet rs, PrintWriter out, int month, String[] accCodes, double[][] accValsDr, double[][] accValsCr, int numAccs, int[] bytesOut, double[] monthTotal, double[] ytdTotal)
                                   throws Exception
  {
    // Cost of Sales - Carriage Outward

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT AccCode FROM acctdefn WHERE Category = 'Cost of Sales - Carriage Outward' AND Active = 'Y'"); 

    double monthAmount = 0.0, ytdAmount = 0.0;
    String accCode;
      
    while(rs.next())
    {
      accCode = rs.getString(1);
 
      monthAmount += getAccAmt(accCode, accCodes, accValsDr[month], accValsCr[month], numAccs);
      ytdAmount   += getAccAmt(accCode, accCodes, accValsDr[13],    accValsCr[13],    numAccs);
    }
    
    monthTotal[0] += monthAmount;
    ytdTotal[0]   += ytdAmount;
        
    scoutln(out, bytesOut, "<tr bgcolor=\"#F0F0F0\"><td><p>Carriage Outward</td>");
    scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(monthAmount, '2') + "</td>");
    scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(ytdAmount, '2') + "</td></tr>");
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();    
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void showCarriageInward(Connection con, Statement stmt, ResultSet rs, PrintWriter out, int month, String[] accCodes, double[][] accValsDr, double[][] accValsCr, int numAccs, int[] bytesOut, double[] monthTotal, double[] ytdTotal)
                                  throws Exception
  {
    // Cost of Sales - Carriage Inward

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT AccCode FROM acctdefn WHERE Category = 'Cost of Sales - Carriage Inward' AND Active = 'Y'"); 

    double monthAmount = 0.0, ytdAmount = 0.0;
    String accCode;
      
    while(rs.next())
    {
      accCode = rs.getString(1);
 
      monthAmount += getAccAmt(accCode, accCodes, accValsDr[month], accValsCr[month], numAccs);
      ytdAmount   += getAccAmt(accCode, accCodes, accValsDr[13],    accValsCr[13],    numAccs);
    }
    
    monthTotal[0] += monthAmount;
    ytdTotal[0]   += ytdAmount;
        
    scoutln(out, bytesOut, "<tr bgcolor=\"#F0F0F0\"><td><p>Carriage Inward</td>");
    scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(monthAmount, '2') + "</td>");
    scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(ytdAmount, '2') + "</td></tr>");
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();    
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void showDelivery(Connection con, Statement stmt, ResultSet rs, PrintWriter out, int month, String[] accCodes, double[][] accValsDr, double[][] accValsCr, int numAccs, int[] bytesOut, double[] monthTotal, double[] ytdTotal)
                            throws Exception
  {
    // Cost of Sales - Delivery and Distribution
 
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT AccCode FROM acctdefn WHERE Category = 'Cost of Sales - Delivery and Distribution' AND Active = 'Y'"); 

    double monthAmount = 0.0, ytdAmount = 0.0;
    String accCode;
      
    while(rs.next())
    {
      accCode = rs.getString(1);
 
      monthAmount += getAccAmt(accCode, accCodes, accValsDr[month], accValsCr[month], numAccs);
      ytdAmount   += getAccAmt(accCode, accCodes, accValsDr[13],    accValsCr[13],    numAccs);
    }  
     
    monthTotal[0] += monthAmount;
    ytdTotal[0]   += ytdAmount;
        
    scoutln(out, bytesOut, "<tr bgcolor=\"#F0F0F0\"><td><p>Delivery and Distribution</td>");
    scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(monthAmount, '2') + "</td>");
    scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(ytdAmount, '2') + "</td></tr>");
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();    
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void showExchangeDifference(Connection con, Statement stmt, ResultSet rs, PrintWriter out, int month, String[] accCodes, double[][] accValsDr, double[][] accValsCr, int numAccs, int[] bytesOut, double[] monthTotal, double[] ytdTotal)
                                      throws Exception
  {
    // Income - Exchange Gain
    // Cost of Sales - Exchange Loss

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT AccCode FROM acctdefn WHERE (Category = 'Income - Exchange Gain' OR Category = 'Cost of Sales - Exchange Loss') AND Active = 'Y'"); 

    double monthAmount = 0.0, ytdAmount = 0.0;
    String accCode;
      
    while(rs.next())
    {
      accCode = rs.getString(1);
 
      monthAmount += getAccAmt(accCode, accCodes, accValsDr[month], accValsCr[month], numAccs);
      ytdAmount   += getAccAmt(accCode, accCodes, accValsDr[13],    accValsCr[13],    numAccs);
    }

    monthTotal[0] += monthAmount;
    ytdTotal[0]   += ytdAmount;

    scoutln(out, bytesOut, "<tr bgcolor=\"#F0F0F0\"><td><p>Exchange Difference</td>");
    scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(monthAmount, '2') + "</td>");
    scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(ytdAmount, '2') + "</td></tr>");
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();    
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void showDiscountReceived(Connection con, Statement stmt, ResultSet rs, PrintWriter out, int month, String[] accCodes, double[][] accValsDr, double[][] accValsCr, int numAccs, int[] bytesOut, double[] monthTotal, double[] ytdTotal)
                                    throws Exception
  {
    // Cost of Sales - Discount Received
   
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT AccCode FROM acctdefn WHERE Category = 'Cost of Sales - Discount Received' AND Active = 'Y'"); 

    double monthAmount = 0.0, ytdAmount = 0.0;
    String accCode;
      
    while(rs.next())
    {
      accCode = rs.getString(1);
 
      monthAmount += getAccAmt(accCode, accCodes, accValsDr[month], accValsCr[month], numAccs);
      ytdAmount   += getAccAmt(accCode, accCodes, accValsDr[13],    accValsCr[13],    numAccs);
    }
     
    monthTotal[0] += monthAmount;
    ytdTotal[0]   += ytdAmount;
        
    scoutln(out, bytesOut, "<tr bgcolor=\"#F0F0F0\"><td><p>Discount Received</td>");
    scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(monthAmount, '2') + "</td>");
    scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(ytdAmount, '2') + "</td></tr>");
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();    
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void showOtherOperatingIncome(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, int month, String[] accCodes, double[][] accValsDr, double[][] accValsCr, int numAccs, int[] bytesOut,
                                        double[] monthTotal, double[] ytdTotal) throws Exception
  {
    // Income (Operating)
 
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT DISTINCT Category FROM acctdefn WHERE Category LIKE 'Income (Operating)%' AND Active = 'Y'"); 

    String category;
      
    while(rs.next())
    {
      category = rs.getString(1);
 
      showOtherOperatingIncomeCategory(con, stmt2, rs2, out, category, month, accCodes, accValsDr, accValsCr, numAccs, bytesOut, monthTotal, ytdTotal);
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();    
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void showOtherOperatingIncomeCategory(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String category, int month, String[] accCodes, double[][] accValsDr, double[][] accValsCr, int numAccs, int[] bytesOut,
                                                double[] monthTotal, double[] ytdTotal) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT AccCode, Description FROM acctdefn WHERE Category = '" + category + "' AND Active = 'Y'"); 

    double monthAmount, ytdAmount;
    String accCode, desc;
      
    while(rs.next())
    {
      accCode = rs.getString(1);
      desc    = rs.getString(2);
 
      monthAmount = getAccAmt(accCode, accCodes, accValsDr[month], accValsCr[month], numAccs);
      ytdAmount   = getAccAmt(accCode, accCodes, accValsDr[13],    accValsCr[13],    numAccs);

      monthTotal[0] += monthAmount;
      ytdTotal[0]   += ytdAmount;

      monthAmount *= -1;
      ytdAmount   *= -1;     
      scoutln(out, bytesOut, "<tr bgcolor=\"#F0F0F0\"><td><p>" + desc + "</td>");
      scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(monthAmount, '2') + "</td>");
      scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(ytdAmount, '2') + "</td></tr>");
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();    
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void showExpenses(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, int month, String[] accCodes, double[][] accValsDr, double[][] accValsCr, int numAccs, int[] bytesOut, double[] monthTotal,
                            double[] ytdTotal) throws Exception
  {
    // Expenses - Bank Charges
    // Expenses - Finance
    // Expenses - General
    // Expenses - Premises
    // Expenses - Professional
    // Expenses - Promotional
    // Expenses - Maintenance
    // Expenses - Staff
    // Expenses - Travel
    
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT DISTINCT Category FROM acctdefn WHERE (Category LIKE 'Expenses%') AND Active = 'Y' ORDER BY Category, Description"); 

    String category;
      
    while(rs.next())
    {
      category = rs.getString(1);
 
      showExpensesCategory(con, stmt2, rs2, out, category, month, accCodes, accValsDr, accValsCr, numAccs, bytesOut, monthTotal, ytdTotal);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();    
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void showExpensesCategory(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String category, int month, String[] accCodes, double[][] accValsDr, double[][] accValsCr, int numAccs, int[] bytesOut, double[] monthTotal,
                                    double[] ytdTotal) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT AccCode, Description FROM acctdefn WHERE Category = '" + category + "' AND Active = 'Y'"); 

    double monthAmount, ytdAmount;
    String accCode, desc;
      
    while(rs.next())
    {
      accCode = rs.getString(1);
      desc    = rs.getString(2);
 
      monthAmount = getAccAmt(accCode, accCodes, accValsDr[month], accValsCr[month], numAccs);
      ytdAmount   = getAccAmt(accCode, accCodes, accValsDr[13],    accValsCr[13],    numAccs);
    
      scoutln(out, bytesOut, "<tr bgcolor=\"#F0F0F0\"><td><p>" + desc + "</td>");
      scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(monthAmount, '2') + "</td>");
      scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(ytdAmount, '2') + "</td></tr>");
      
      monthTotal[0] += monthAmount;
      ytdTotal[0]   += ytdAmount;
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();    
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void showNonOperatingIncome(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, int month, String[] accCodes, double[][] accValsDr, double[][] accValsCr, int numAccs, int[] bytesOut,
                                      double[] monthTotal, double[] ytdTotal) throws Exception
  {
    // Income

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT DISTINCT Category FROM acctdefn WHERE Category = 'Income' AND Active = 'Y'"); 

    String category;
      
    while(rs.next())
    {
      category = rs.getString(1);
 
      showNonOperatingIncomeCategory(con, stmt2, rs2, out, category, month, accCodes, accValsDr, accValsCr, numAccs, bytesOut, monthTotal, ytdTotal);
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();    
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void showNonOperatingIncomeCategory(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String category, int month, String[] accCodes, double[][] accValsDr, double[][] accValsCr, int numAccs, int[] bytesOut,
                                              double[] monthTotal, double[] ytdTotal) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT AccCode, Description FROM acctdefn WHERE Category = '" + category + "' AND Active = 'Y'"); 

    double monthAmount, ytdAmount;
    String accCode, desc;
      
    while(rs.next())
    {
      accCode = rs.getString(1);
      desc    = rs.getString(2);
 
      monthAmount = getAccAmt(accCode, accCodes, accValsDr[month], accValsCr[month], numAccs);
      ytdAmount   = getAccAmt(accCode, accCodes, accValsDr[13],    accValsCr[13],    numAccs);

      monthTotal[0] += monthAmount;
      ytdTotal[0]   += ytdAmount;

      monthAmount *= -1;
      ytdAmount   *= -1;
      scoutln(out, bytesOut, "<tr bgcolor=\"#F0F0F0\"><td><p>" + desc + "</td>");
      scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(monthAmount, '2') + "</td>");
      scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(ytdAmount, '2') + "</td></tr>");
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();    
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void showProvisionForIncomeTax(Connection con, Statement stmt, ResultSet rs, int month, String[] accCodes, double[][] accValsDr, double[][] accValsCr, int numAccs, double[] monthTotal, double[] ytdTotal) throws Exception
  {
    // PL Provision - Provision for Income Tax

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT AccCode FROM acctdefn WHERE Category = 'PL Provision - Provision for Income Tax' AND Active = 'Y'");

    double monthAmount = 0.0, ytdAmount = 0.0;
    String accCode;

    while(rs.next())
    {
      accCode = rs.getString(1);

      monthAmount += getAccAmt(accCode, accCodes, accValsDr[month], accValsCr[month], numAccs);
      ytdAmount   += getAccAmt(accCode, accCodes, accValsDr[13],    accValsCr[13],    numAccs);
    }

    monthTotal[0] += monthAmount;
    ytdTotal[0]   += ytdAmount;

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void showProvisionForDeferredTax(Connection con, Statement stmt, ResultSet rs, int month, String[] accCodes, double[][] accValsDr, double[][] accValsCr, int numAccs, double[] monthTotal, double[] ytdTotal) throws Exception
  {
    // PL Provision - Provision for Income Tax

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT AccCode FROM acctdefn WHERE Category = 'PL Provision - Deferred Tax' AND Active = 'Y'");

    double monthAmount = 0.0, ytdAmount = 0.0;
    String accCode;

    while(rs.next())
    {
      accCode = rs.getString(1);

      monthAmount += getAccAmt(accCode, accCodes, accValsDr[month], accValsCr[month], numAccs);
      ytdAmount   += getAccAmt(accCode, accCodes, accValsDr[13],    accValsCr[13],    numAccs);
    }

    monthTotal[0] += monthAmount;
    ytdTotal[0]   += ytdAmount;

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void showProvisionForDividend(Connection con, Statement stmt, ResultSet rs, int month, String[] accCodes, double[][] accValsDr, double[][] accValsCr, int numAccs, double[] monthTotal, double[] ytdTotal) throws Exception
  {
    // PL Provision - Proposed Dividend
    
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT AccCode FROM acctdefn WHERE Category = 'PL Provision - Proposed Dividend' AND Active = 'Y'"); 

    double monthAmount = 0.0, ytdAmount = 0.0;
    String accCode;
      
    while(rs.next())
    {
      accCode = rs.getString(1);
 
      monthAmount += getAccAmt(accCode, accCodes, accValsDr[month], accValsCr[month], numAccs);
      ytdAmount   += getAccAmt(accCode, accCodes, accValsDr[13],    accValsCr[13],    numAccs);
    }
    
    monthTotal[0] += monthAmount;
    ytdTotal[0]   += ytdAmount;
        
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();    
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private double getAccAmt(String reqdAccCode, String[] accCodes, double[] accValsDr, double[] accValsCr, int numAccs) throws Exception
  {
    double value = 0.0;
    int x = 0;
    while(x < numAccs && ! accCodes[x].equals(reqdAccCode))
      ++x;
    
    if(x < numAccs)
      value = generalUtils.doubleDPs(accValsDr[x], '2') + generalUtils.doubleDPs((accValsCr[x] * -1), '2');
 
      return generalUtils.doubleDPs(value, '2');
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void allAccounts(Connection accCon, Connection ofsaCon, Statement accStmt, Statement ofsaStmt, ResultSet accRs, ResultSet ofsaRs, String year, 
          String scanDateFrom, String scanDateTo, String financialYearStartDate, String previousFinancialYearStartDate, String dnm,
                           String localDefnsDir, String defnsDir, String[][] accCodes, double[][][] accValsDr, double[][][] accValsCr, int[] numAccs, char[][] accTypes, int[] monthEnds, int[] numMonths    , String unm) throws Exception
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

      String debtorsAccount = "", creditorsAccount = "", otherDebtorsAccount = "", otherCreditorsAccount = "", salesAccount = "",
             purchasesAccount = "", gstOutputAccount = "", gstInputAccount = "", exchangeGainAccount = "", exchangeLossAccount = "",
             salesReturnedAccount = "", purchasesReturnedAccount = "", stockAccount = "", discountReceivedAccount = "", discountGivenAccount = "";
      
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

      String baseCurrency = accountsUtils.getBaseCurrency(ofsaCon, ofsaStmt, ofsaRs, dnm, localDefnsDir, defnsDir);      

      accountsGenerateTBReport.scanDocuments(accCon, ofsaCon, accStmt, ofsaStmt, accRs, ofsaRs, true, scanDateFrom, scanDateTo, financialYearStartDate, previousFinancialYearStartDate, accCodes[0], accValsDr[0], accValsCr[0], numAccs[0], monthEnds, numMonths,
                           debtorsAccount, creditorsAccount, otherDebtorsAccount, otherCreditorsAccount, salesAccount, purchasesAccount, gstOutputAccount, gstInputAccount, exchangeGainAccount, exchangeLossAccount, salesReturnedAccount,
                           purchasesReturnedAccount, stockAccount, discountReceivedAccount, discountGivenAccount, baseCurrency, null, null, null, null, null, null, 0, 0, null     , unm, dnm);

      // accValsDr and accValsCr have data for all months of the year (from the effectiveStartDate if appropriate), upto the stated monthEnd
      // [12] has the OB values from the OB batch(es)
      
      // the YTD colunm [13] values are derived by totalling all months
      
      for(y=0;y<numAccs[0];++y)
      {
        accValsDr[0][13][y] = generalUtils.doubleDPs(accValsDr[0][12][y], '2');
        accValsCr[0][13][y] = generalUtils.doubleDPs(accValsCr[0][12][y], '2');
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
    if(out != null) // ignore printout for call from 6020a
    {
      out.println(str);
      bytesOut[0] += (str.length() + 2);
    }
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void outputPageFrame(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, boolean plain, String bodyStr, String monthTo, String title, String unm, String sid, String uty, String men, String den,
                               String dnm, String bnm, String otherSetup, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    int[] hmenuCount = new int[1];

    String subMenuText = dashboardUtils.buildSubMenuText(con, stmt, rs, req, "6055", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount);

    subMenuText += "<table id='title' width=100%><tr><td colspan=2 nowrap><p>" + title + directoryUtils.buildHelp(6055) + "</td></tr></table>";

    subMenuText += buildSubMenuText(plain, monthTo, unm, sid, uty, men, den, dnm, bnm, hmenuCount);

    if(plain)
      scoutln(out, bytesOut, authenticationUtils.buildPlainScreen(con, stmt, rs, req, unm, sid, uty, men, den, dnm, bnm, "AccountsGeneratePLReportLive", subMenuText, hmenuCount[0], bodyStr, otherSetup, localDefnsDir, defnsDir));
    else
    {
      scoutln(out, bytesOut, authenticationUtils.buildScreen(con, stmt, rs, req, true, unm, sid, uty, men, den, dnm, bnm, "AccountsGeneratePLReportLive", subMenuText, hmenuCount[0], bodyStr, otherSetup, localDefnsDir, defnsDir));

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
      s += "<a href=\"/central/servlet/AccountsGeneratePLReportLive?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + monthTo + "&p2=P\">Friendly</a></dt></dl>";
    }
    
    s += "</div>";
    
    --hmenuCount[0];
    
    return s;
  }

}
/*
 Fixed Assets                              
 Fixed Assets - Accumulated Depreciation   
 Fixed Assets - Investment                 
 
 Current Assets                            
 Current Assets - Contra                   
 Current Assets - Stock                    
 Current Assets - Trade Debtors            
 Current Assets - PrePayments              
 Current Assets - Recoverable Expenses     
 Current Assets - Cash                     
 Current Assets - Bank                     
 Current Assets - GST Clearing             
 Current Assets - Currency Exchange        
 Current Assets - OverPayments             
 Current Assets - Other Debtors            

 Liabilities                               
 Liabilities - PrePayments                 
 Liabilities - Trade Creditors             
 Liabilities - Other Creditors             
 Liabilities - Accruals                    
 Liabilities - Provision for Income Tax    
 Liabilities - Suspense                    
 Liabilities - GST Input                   
 Liabilities - GST Output                  
 
 Equity                                    
 Equity - Current Earnings               
 
 Cost of Sales                             
 Cost of Sales - Purchases               
 Cost of Sales - Purchases Returned        
 Cost of Sales - Carriage Outward          
 Cost of Sales - Carriage Inward           
 Cost of Sales - Delivery and Distribution 
 Cost of Sales - Exchange Loss             
 Cost of Sales - Discount Received         
 
 Income                                    
 Income - Sales                            
 Income - Sales Category                   
 Income (Operating) - Agents Commission    
 Income - Sales Returned                   
 Income - Exchange Gain                    
 Income - Discount Given                   
 
 Expenses - Professional                   
 Expenses - Promotion                      
 Expenses - Finance                        
 Expenses - Bank Charges                   
 Expenses - Staff                          
 Expenses - General                        
 Expenses - Travel                         
 Expenses - Repairs                        
 Expenses - Premises                       
*/
