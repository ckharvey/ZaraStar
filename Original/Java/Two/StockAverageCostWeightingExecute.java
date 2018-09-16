// =======================================================================================================================================================================================================
// System: ZaraStar Accounts: Stock: Physical vs Financial
// Module: StockAverageCostWeightingExecute.java
// Author: C.K.Harvey
// Copyright (c) 2001-09 Christopher Harvey. All Rights Reserved.
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
import java.sql.*;
import java.io.*;

public class StockAverageCostWeightingExecute extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  MiscDefinitions miscDefinitions = new MiscDefinitions();
  Inventory inventory = new Inventory();
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

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="", p3="";

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
      p1  = req.getParameter("p1"); // mfr
      p2  = req.getParameter("p2"); // dateFrom
      p3  = req.getParameter("p3"); // dateTo

      if(p2 == null) p2 = "";

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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "StockAverageCostWeighting", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3074, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush(); 
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String mfr, String dateFrom, String dateTo, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    Connection con = null;
    Statement stmt = null, stmt2 = null;
    ResultSet rs = null;

    String uName = directoryUtils.getMySQLUserName();
    String pWord = directoryUtils.getMySQLPassWord();
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + uName + "&password=" + pWord);

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 3074, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "StockAverageCostWeighting", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3074, bytesOut[0], 0, "ACC:" + mfr);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "StockAverageCostWeighting", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3074, bytesOut[0], 0, "SID:" + mfr);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(dateFrom.length() == 0)
      dateFrom = "1970-01-01";
    else dateFrom = generalUtils.convertDateToSQLFormat(dateFrom);

    if(dateTo.length() == 0)
      dateTo = "1970-01-01";
    else dateTo = generalUtils.convertDateToSQLFormat(dateTo);

    String[] financialYearStartMonth = new String[1];
    String[] financialYearEndMonth   = new String[1];

    definitionTables.getAppConfigFinancialYearMonths(con, stmt, rs, dnm, financialYearStartMonth, financialYearEndMonth);

    set(con, stmt, stmt2, rs, out, req, mfr, dateFrom, dateTo, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 3074, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), mfr);
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, Statement stmt2, ResultSet rs, PrintWriter out, HttpServletRequest req, String mfr, String dateFrom, String dateTo, String unm, String sid, String uty, String men, String den, String dnm,
                   String bnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Stock: Physical vs Financial</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3001, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewItem(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/ProductStockRecord?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p1=\"+p1;}");
    }

    scoutln(out, bytesOut, "function reset(){");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/StockAverageCostWeightingReset?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=" + mfr + "&p2=" + dateFrom + "&p3=" + dateTo + "&bnm=" + bnm + "\";}");

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

    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    int[] hmenuCount = new int[1];

    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "3074", "", "StockAverageCostWeighting", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Stock Average-Cost Weighting: Physical vs Financial", "3074", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    String baseCurrency = accountsUtils.getBaseCurrency(con, stmt, rs, dnm, localDefnsDir, defnsDir);

    scoutln(out, bytesOut, "<form><table id=\"page\" border=0 cellspacing=2 cellpadding=3><tr><td colspan=10><p>Differences between System Stock Trace levels with Accounts Opening Records</td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    drawHeader(out, baseCurrency, dateFrom, dateTo, bytesOut);

    forEachStockItem(con, stmt, stmt2, rs, out, mfr, dateFrom, dateTo, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    drawHeader(out, baseCurrency, dateFrom, dateTo, bytesOut);

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td colspan=6><p><a href=\"javascript:reset()\">Reset</a> the <i>Opening (Accounts) Level</i> to the same as the <i>Stock Trace (Physical) Level</i></td></tr>");

    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir));
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void drawHeader(PrintWriter out, String baseCurrency, String dateFrom ,String dateTo, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<tr id=\"pageColumn\">");
    scoutln(out, bytesOut, "<td><p>Item Code</td>");
    scoutln(out, bytesOut, "<td><p>Manufacturer</td>");
    scoutln(out, bytesOut, "<td><p>Manufacturer Code</td>");
    scoutln(out, bytesOut, "<td><p>Stock Trace (Physical) Level</td>");
    scoutln(out, bytesOut, "<td><p>Opening (Accounts) Level</td>");
    scoutln(out, bytesOut, "<td><p>Opening (Accounts) WAC (" + baseCurrency + ") </td>");
    scoutln(out, bytesOut, "<td><p>Calculated WAC (" + baseCurrency + ") <br>" + dateFrom + " to " + dateTo + "</td></tr>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void forEachStockItem(Connection con, Statement stmt, Statement stmt2, ResultSet rs, PrintWriter out, String mfrReqd, String dateFrom, String date, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                                String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    char dpOnQuantities = miscDefinitions.dpOnQuantities(con, stmt, rs, "5");
    String[] cssFormat = new String[1];  cssFormat[0] = "";

    try
    {
      String where = "", orderBy;
      if(! mfrReqd.equals("___ALL___"))
      {
        where = "WHERE Manufacturer = '" + generalUtils.sanitiseForSQL(mfrReqd) + "'";
        orderBy = "ManufacturerCode";
      }
      else orderBy = "ItemCode";

      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT ItemCode, Manufacturer, ManufacturerCode FROM stock " + where + " ORDER BY " + orderBy);

      String itemCode, mfr, mfrCode;
      while(rs.next())
      {
        itemCode = rs.getString(1);
        mfr      = rs.getString(2);
        mfrCode  = rs.getString(3);

        process(con, stmt, stmt2, rs, out, dateFrom, date, itemCode, mfr, mfrCode, dpOnQuantities, cssFormat, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);
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

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void process(Connection con, Statement stmt, Statement stmt2, ResultSet rs, PrintWriter out, String dateFrom, String date, String itemCode, String mfr, String mfrCode, char dpOnQuantities, String[] cssFormat, String unm, String sid,
                       String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    String yesterdaysDate = generalUtils.decode((generalUtils.encodeFromYYYYMMDD(date) - 1), localDefnsDir, defnsDir);

    double systemLevel = inventory.stockLevelForAStore(con, stmt, stmt2, rs, "", itemCode, yesterdaysDate, unm, sid, uty, men, den, dnm, bnm);

    double openingLevel = 0.0, openingWAC = 0.0;

    double[] numItemsPurchased = new double[1];

    double calculatedWAC = inventory.getWAC(con, stmt, rs, itemCode, dateFrom, date, dnm, numItemsPurchased);

    try
    {
      // get financial from stockopen

      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT Level, Cost FROM stockopen WHERE ItemCode = '" + generalUtils.sanitiseForSQL(itemCode) + "' AND Date <= {d '" + date + "'} ORDER BY Date DESC");

      if(rs.next()) // pickup the first one
      {
        openingLevel = generalUtils.doubleFromStr(rs.getString(1));
        openingWAC   = generalUtils.doubleFromStr(rs.getString(2));
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

    if(cssFormat[0].equals("line1")) cssFormat[0] = "line2"; else cssFormat[0] = "line1";

    scoutln(out, bytesOut, "<tr id='" + cssFormat[0] + "'><td><p><a href=\"javascript:viewItem('" + itemCode + "')\">" + itemCode + "</a></td>");
    scoutln(out, bytesOut, "<td><p>" + mfr + "</td>");
    scoutln(out, bytesOut, "<td><p>" + mfrCode + "</td>");
    scoutln(out, bytesOut, "<td align=center><p>" + generalUtils.formatNumeric(systemLevel, dpOnQuantities) + "</td>");
    scoutln(out, bytesOut, "<td align=center><p>" + generalUtils.formatNumeric(openingLevel, dpOnQuantities) + "</td>");
    scoutln(out, bytesOut, "<td align=center><p>" + generalUtils.formatNumeric(openingWAC, '2') + "</td>");
    scoutln(out, bytesOut, "<td align=center><p>" + generalUtils.formatNumeric(calculatedWAC, '2') + "</td></tr>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int[] bytesOut, String str) throws Exception
  {
    out.println(str);
    bytesOut[0] += (str.length() + 2);
  }

}
