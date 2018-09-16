// =======================================================================================================================================================================================================
// System: ZaraStar Accouts: View GL
// Module: AccountsViewGL.java
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
import java.io.*;
import java.sql.*;
 
public class AccountsViewGL extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  DefinitionTables definitionTables = new DefinitionTables();
  AccountsUtils accountsUtils = new AccountsUtils();
  
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
      directoryUtils.setContentHeaders(res);
      out = res.getWriter();

      unm = req.getParameter("unm");
      uty = req.getParameter("uty");
      sid = req.getParameter("sid");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");
      
      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "AccountsViewGL", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6028, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                    int[] bytesOut) throws Exception
  {
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');
    String workingDir       = directoryUtils.getUserDir('W', dnm, unm);

    long startTime = new java.util.Date().getTime();

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 6028, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      serverUtils.etotalBytes(req, unm, dnm, 6028, bytesOut[0], 0, "ACC:");
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "AccountsViewGL", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6028, bytesOut[0], 0, "SID:");
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }
  
    String yyyy = generalUtils.getFromDefnFile(unm, "accounts.dfn", workingDir, "");
    int year = generalUtils.strToInt(yyyy);

    String[] financialYearStartMonth = new String[1];
    String[] financialYearEndMonth   = new String[1];

    definitionTables.getAppConfigFinancialYearMonths(con, stmt, rs, dnm, financialYearStartMonth, financialYearEndMonth);

    int startMonth = generalUtils.detMonthNumFromMonthName(financialYearStartMonth[0]);
    int endMonth   = generalUtils.detMonthNumFromMonthName(financialYearEndMonth[0]);

    String dateFrom = "01." + startMonth + "." + year;

    String dateTo;
    if(startMonth > endMonth)
      dateTo = generalUtils.numOfDaysInMonth((short)endMonth, (short)year) + "." + endMonth + "." + (year + 1);
    else dateTo = generalUtils.numOfDaysInMonth((short)endMonth, (short)year) + "." + endMonth + "." + year;

    String effectiveDate = definitionTables.getAppConfigEffectiveStartDate(con, stmt, rs, dnm);

    if(generalUtils.encode(effectiveDate, localDefnsDir, defnsDir) > generalUtils.encode(dateFrom, localDefnsDir, defnsDir))
      dateFrom = effectiveDate;
    
    create(con, stmt, rs, out, req, yyyy, dateFrom, dateTo, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 6028, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con  != null) con.close();
    if(out != null) out.flush();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void create(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String year, String dateFrom, String dateTo, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                      String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Accounts - List GL</title>");
    
    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">"); 

    scoutln(out, bytesOut, "function tradeDebtors(curr){");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/AccountsGLTradeDebtors?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p5=\"+curr+\"&p2=" + dateFrom + "&p3=" + dateTo + "\";}");

    scoutln(out, bytesOut, "function otherDebtors(account){");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/AccountsGLOtherDebtors?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+account+\"&p2=" + dateFrom + "&p3=" + dateTo
                         + "\";}");

    scoutln(out, bytesOut, "function tradeCreditors(curr){");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/AccountsGLTradeCreditors?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p5=\"+curr+\"&p2=" + dateFrom + "&p3=" + dateTo + "\";}");

    scoutln(out, bytesOut, "function otherCreditors(account){");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/AccountsGLOtherCreditors?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+account+\"&p2=" + dateFrom + "&p3=" + dateTo
                         + "\";}");

    scoutln(out, bytesOut, "function gstInput(account){");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/AccountsGLGSTInput?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+account+\"&p2=" + dateFrom + "&p3=" + dateTo
                         + "\";}");

    scoutln(out, bytesOut, "function gstOutput(account){");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/AccountsGLGSTOutput?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+account+\"&p2=" + dateFrom + "&p3=" + dateTo
                         + "\";}");

    scoutln(out, bytesOut, "function gstClearing(account){");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/AccountsGLGSTClearing?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+account+\"&p2=" + dateFrom + "&p3=" + dateTo
                         + "\";}");

    scoutln(out, bytesOut, "function expense(account){");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/AccountsGLExpenses?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+account+\"&p2=" + dateFrom + "&p3=" + dateTo
                         + "\";}");

    scoutln(out, bytesOut, "function charges(account){");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/AccountsGLBankCharges?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+account+\"&p2=" + dateFrom + "&p3=" + dateTo
                         + "\";}");

    scoutln(out, bytesOut, "function discounts(account,which){");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/AccountsGLDiscounts?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p5=\"+which+\"&p1=\"+account+\"&p2=" + dateFrom + "&p3="
                         + dateTo + "\";}");

    scoutln(out, bytesOut, "function sales(account,option){");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/AccountsGLSales?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p5=\"+option+\"&p1=\"+account+\"&p2=" + dateFrom + "&p3="
                         + dateTo + "\";}");

    scoutln(out, bytesOut, "function salesCategory(account){");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/AccountsGLSales?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p5=N&p1=\"+account+\"&p2=" + dateFrom + "&p3=" + dateTo
                         + "\";}");

    scoutln(out, bytesOut, "function exch(account,gainOrLoss){");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/GLExchangeGainLoss?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+account+\"&p5=\"+gainOrLoss+\"&p2=" + dateFrom
                         + "&p3=" + dateTo + "\";}");

    scoutln(out, bytesOut, "function purchases(account){");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/AnalyticsPurchases?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p5=P&p1=\"+account+\"&p2=" + dateFrom + "&p3=" + dateTo
                         + "\";}");

    scoutln(out, bytesOut, "function purchasesCategory(account){");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/AnalyticsPurchases?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p5=N&p1=\"+account+\"&p2=" + dateFrom + "&p3=" + dateTo
                         + "\";}");

    scoutln(out, bytesOut, "function bank(account){");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/AccountsGLBankTransactions?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+account+\"&p2=" + dateFrom + "&p3=" + dateTo
                         + "\";}");

    scoutln(out, bytesOut, "function contra(account){");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/AccountsGLCurrentAssetsContra?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+account+\"&p2=" + dateFrom + "&p3=" + dateTo
                         + "\";}");

    scoutln(out, bytesOut, "function re(account){");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/AccountsGLRecoverableExpenses?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+account+\"&p2=" + dateFrom + "&p3=" + dateTo
                         + "\";}");

    scoutln(out, bytesOut, "function caPrePayments(account){");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/AccountsGLCurrentAssetsPrePayments?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+account+\"&p2=" + dateFrom + "&p3=" + dateTo
                         + "\";}");

    scoutln(out, bytesOut, "function lPrePayments(account){");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/AccountsGLLiabilitiesPrePayments?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+account+\"&p2=" + dateFrom + "&p3=" + dateTo
                         + "\";}");

    scoutln(out, bytesOut, "function salesReturned(account){");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/AccountsGLSalesReturned?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+account+\"&p2=" + dateFrom + "&p3=" + dateTo
                         + "\";}");

    scoutln(out, bytesOut, "function purchasesReturned(account){");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/AccountsGLPurchasesReturned?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+account+\"&p2=" + dateFrom + "&p3=" + dateTo
                         + "\";}");

    scoutln(out, bytesOut, "function fa(account){");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/AccountsGLFixedAssetsEquity?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+account+\"&p2=" + dateFrom + "&p3=" + dateTo
                         + "\";}");

    scoutln(out, bytesOut, "function liabilities(account){");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/AccountsGLLiabilities?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+account+\"&p2=" + dateFrom + "&p3=" + dateTo
                         + "\";}");

    scoutln(out, bytesOut, "function income(account){");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/AccountsGLIncome?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+account+\"&p2=" + dateFrom + "&p3=" + dateTo
                         + "\";}");

    scoutln(out, bytesOut, "function ce(account){");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/AccountsGLEquityEarnings?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+account+\"&p2=" + dateFrom + "&p3=" + dateTo
                         + "\";}");

    scoutln(out, bytesOut, "function accruals(account){");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/AccountsGLLiabilitiesAccruals?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+account+\"&p2=" + dateFrom + "&p3=" + dateTo
                         + "\";}");

    scoutln(out, bytesOut, "function currencyExch(account){");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/AccountsGLEquityCurrencyExchange?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+account+\"&p2=" + dateFrom + "&p3=" + dateTo
                         + "\";}");

    scoutln(out, bytesOut, "function costOfSales(account){");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/AccountsGLCostOfSales?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+account+\"&p2=" + dateFrom + "&p3=" + dateTo
                         + "\";}");

    scoutln(out, bytesOut, "function stock(account){");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/AnalyticsStock?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+account+\"&p2=" + dateFrom + "&p3=" + dateTo
                         + "\";}");

    scoutln(out, bytesOut, "function wip(account){");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/AnalyticsWIP?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+account+\"&p2=" + dateFrom + "&p3=" + dateTo
                         + "\";}");

    scoutln(out, bytesOut, "function sanitise(code){");
    scoutln(out, bytesOut, "var code2='';var x;var len=code.length;");
    scoutln(out, bytesOut, "for(x=0;x<len;++x)");
    scoutln(out, bytesOut, "if(code.charAt(x)=='#')code2+='%23';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='\"')code2+='%22';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='&')code2+='%26';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='%')code2+='%25';");
    scoutln(out, bytesOut, "else if(code.charAt(x)==' ')code2+='%20';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='+')code2+='%2b';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='?')code2+='%3F';");
    scoutln(out, bytesOut, "else code2+=code.charAt(x);");
    scoutln(out, bytesOut, "return code2};");

    scoutln(out, bytesOut, "</script>");

    int[] hmenuCount = new int[1];
 
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "6028", "", "AccountsViewGL", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Accounts - List General Ledger: " + year, "6028", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);
  
    scoutln(out, bytesOut, "<form><table id=\"page\" border=0 width=100%>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr id=\"pageColumn\">");
    scoutln(out, bytesOut, "<td nowrap><p>Account Code &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Description &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Type &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Type &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Currency &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Category &nbsp;</td></tr>");
        
    String[][] accCurrencies = new String[1][];
    int numCurrencies = accountsUtils.getCurrencyNames(con, stmt, rs, accCurrencies, dnm, localDefnsDir, defnsDir);
      
    list(out, year, numCurrencies, accCurrencies[0], dnm, bytesOut);
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void list(PrintWriter out, String year, int numCurrencies, String[] accCurrencies, String dnm, int[] bytesOut) throws Exception
  {
    Connection con = null;
    Statement stmt = null;
    ResultSet rs   = null;
    
    try
    {
      String uName = directoryUtils.getMySQLUserName();
      String pWord = directoryUtils.getMySQLPassWord();
      Class.forName("com.mysql.jdbc.Driver").newInstance();
      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_accounts_" + year + "?user=" + uName + "&password=" + pWord);
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT AccCode, Description, DrCr, Type, Currency, Category FROM acctdefn WHERE Active = 'Y' ORDER BY AccCode"); 

      String accCode, drcr, desc, type, currency, category, cssFormat="", jsCall;
      int x;

      while(rs.next())                  
      {
        accCode     = rs.getString(1);
        desc        = rs.getString(2);
        drcr        = rs.getString(3);
        type        = rs.getString(4);
        currency    = rs.getString(5);
        category    = rs.getString(6);
      
        if(cssFormat.equals("line1")) cssFormat = "line2"; else cssFormat = "line1";

        if(category.equals("Current Assets - Trade Debtors"))
        {
          for(x=0;x<numCurrencies;++x)
          {
            jsCall = "tradeDebtors('" + accCurrencies[x] + "')";
            scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\">");
            scoutln(out, bytesOut, "<td><a href=\"javascript:" + jsCall + "\">" + accCode + "</td>");
            scoutln(out, bytesOut, "<td nowrap>" + desc + " (" + accCurrencies[x] + ")</td>");

            if(drcr.equals("D")) drcr = "Debit"; else drcr = "Credit";
            scoutln(out, bytesOut, "<td>" + drcr + "</td>");

            if(type.equals("T")) type = "Trading"; else type = "Balance Sheet";
            scoutln(out, bytesOut, "<td>" + type + "</td>");        
            scoutln(out, bytesOut, "<td>" + accCurrencies[x] + "</td>");
            scoutln(out, bytesOut, "<td>" + category + "</td></tr>");
          }
        }
        else
        if(category.equals("Liabilities - Trade Creditors"))
        {
          for(x=0;x<numCurrencies;++x)
          {
            jsCall = "tradeCreditors('" + accCurrencies[x] + "')";
            scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\">");
            scoutln(out, bytesOut, "<td><a href=\"javascript:" + jsCall + "\">" + accCode + "</td>");
            scoutln(out, bytesOut, "<td nowrap>" + desc + " (" + accCurrencies[x] + ")</td>");

            if(drcr.equals("D")) drcr = "Debit"; else drcr = "Credit";
            scoutln(out, bytesOut, "<td>" + drcr + "</td>");

            if(type.equals("T")) type = "Trading"; else type = "Balance Sheet";
            scoutln(out, bytesOut, "<td>" + type + "</td>");        
            scoutln(out, bytesOut, "<td>" + accCurrencies[x] + "</td>");
            scoutln(out, bytesOut, "<td>" + category + "</td></tr>");
          }
        }
        else
        {
          scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\">");
        
          if(category.equals("Income - Sales"))
            jsCall = "sales('" + accCode + "','S')";
          else
          if(category.startsWith("Income (Operating)"))
            jsCall = "sales('" + accCode + "','C')";
          else
          if(category.equals("Income - Sales Category"))
            jsCall = "salesCategory('" + accCode + "')";
          else
          if(category.equals("Cost of Sales - Purchases"))
            jsCall = "purchases('" + accCode + "')";
          else
          if(category.equals("Cost of Sales - Purchases Category") || category.equals("Cost of Sales - Carriage Outward") || category.equals("Cost of Sales - Carriage Inward") || category.equals("Cost of Sales - Delivery and Distribution"))
            jsCall = "purchasesCategory('" + accCode + "')";
          else
          if(category.equals("Income - Exchange Gain"))
            jsCall = "exch('" + accCode + "','G')";
          else
          if(category.equals("Cost of Sales"))
            jsCall = "costOfSales('" + accCode + "')";
          else
          if(category.equals("Cost of Sales - Discount Received"))
            jsCall = "discounts('" + accCode + "','R')";
          else
          if(category.equals("Cost of Sales - Exchange Loss"))
            jsCall = "exch('" + accCode + "','L')";
          else
          if(category.equals("Current Assets - GST Clearing"))
            jsCall = "gstClearing('" + accCode + "')";
          else
          if(category.equals("Liabilities - GST Input"))
            jsCall = "gstInput('" + accCode + "')";
          else
          if(category.equals("Liabilities - GST Output"))
            jsCall = "gstOutput('" + accCode + "')";
          else
          if(category.equals("Current Assets - Other Debtors"))
            jsCall = "otherDebtors()";
          else
          if(category.equals("Liabilities - Other Creditors"))
            jsCall = "otherCreditors()";
          else
          if(category.equals("Income - Sales Returned"))
            jsCall = "salesReturned('" + accCode + "')";
          else
          if(category.equals("Cost of Sales - Purchases Returned"))
            jsCall = "purchasesReturned('" + accCode + "')";
          else
          if(category.equals("Current Assets - Cash"))
            jsCall = "bank('" + accCode + "')";
          else
          if(category.equals("Current Assets - Bank"))
            jsCall = "bank('" + accCode + "')";
          else
          if(category.equals("Current Assets - Recoverable Expenses"))
            jsCall = "re('" + accCode + "')";
          else
          if(category.equals("Current Assets - Contra"))
            jsCall = "contra('" + accCode + "')";
          else
          if(category.equals("Current Assets - PrePayments"))
            jsCall = "caPrePayments('" + accCode + "')";
          else
          if(category.equals("Liabilities - PrePayments"))
            jsCall = "lPrePayments('" + accCode + "')";
          else
          if(category.equals("Liabilities") || category.equals("Liabilities - Suspense") || category.equals("Liabilities - Provision for Income Tax") || category.equals("Liabilities - Proposed Dividend"))
            jsCall = "liabilities('" + accCode + "')";
          else
          if(category.equals("Income"))
            jsCall = "income('" + accCode + "')";
          else
          if(category.equals("Income - Discount Given"))
            jsCall = "discounts('" + accCode + "','G')";
          else
          if(category.equals("Liabilities - Accruals"))
            jsCall = "accruals('" + accCode + "')";
          else
          if(category.equals("Equity - Current Earnings") || category.equals("Equity - Retained Earnings") || category.equals("Fixed Assets - Accumulated Depreciation"))
            jsCall = "ce('" + accCode + "')";
          else
          if(category.equals("Current Assets - Currency Exchange"))
            jsCall = "currencyExch('" + accCode + "')";
          else
          if(category.equals("Current Assets - Stock"))
            jsCall = "stock('" + accCode + "')";
          else
          if(category.equals("Current Assets - WIP"))
            jsCall = "wip('" + accCode + "')";
          else
          if(category.equals("Expenses - Bank Charges"))
            jsCall = "charges('" + accCode + "')";
          else
          if(category.startsWith("Expenses - "))
            jsCall = "expense('" + accCode + "')";
          else
          if(   category.equals("Fixed Assets") || category.equals("Fixed Assets - Investment") || category.equals("Equity") || category.equals("Current Assets") || category.equals("Current Assets - OverPayments")
             || category.startsWith("PL Provision"))
          {
            jsCall = "fa('" + accCode + "')";
          }
          else jsCall = "";

          scoutln(out, bytesOut, "<td><a href=\"javascript:" + jsCall + "\">" + accCode + "</td>");
          scoutln(out, bytesOut, "<td nowrap>" + desc + "</td>");

          if(drcr.equals("D")) drcr = "Debit"; else drcr = "Credit";
          scoutln(out, bytesOut, "<td>" + drcr + "</td>");

          if(type.equals("T")) type = "Trading"; else type = "Balance Sheet";
          scoutln(out, bytesOut, "<td>" + type + "</td>");
        
          scoutln(out, bytesOut, "<td>" + currency + "</td>");
  
          scoutln(out, bytesOut, "<td>" + category + "</td></tr>");
        }
      }
                 
      if(rs   != null) rs.close();                                 
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(rs   != null) rs.close();                                 
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }    
  }  

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
