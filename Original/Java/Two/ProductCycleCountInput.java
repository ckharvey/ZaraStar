// =======================================================================================================================================================================================================
// System: ZaraStar: Cycle count - Create params page
// Module: ProductCycleCountInput.java
// Author: C.K.Harvey
// Copyright (c) 2003-09 Christopher Harvey. All Rights Reserved.
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

public class ProductCycleCountInput extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  DefinitionTables definitionTables = new DefinitionTables();
  InventoryAdjustment inventoryAdjustment = new InventoryAdjustment();
  AdminUtils adminUtils = new AdminUtils();

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
      sid = req.getParameter("sid");
      uty = req.getParameter("uty");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "ProductCycleCountInput", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3083, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush(); 
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 3083, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "3083", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3083, bytesOut[0], 0, "ACC:");
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "3083", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3083, bytesOut[0], 0, "SID:");
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    set(con, stmt, rs, out, req, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 3083, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut)
                   throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Cycle Count Parameters</title>");

    scoutln(out, bytesOut, "<SCRIPT LANGUAGE=\"JavaScript\">");

    scoutln(out, bytesOut, "function generate(option){var p1;if(option=='Y')p1=document.forms[0].dateToY.value;else p1=document.forms[0].dateToN.value;");
    scoutln(out, bytesOut, "window.location.href='/central/servlet/ProductCycleCountGenerate?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2='+option+'&p1='+p1;}");

    scoutln(out, bytesOut, "function print(){var p1=document.forms[0].dateTo2.value;");
    scoutln(out, bytesOut, "window.location.href='/central/servlet/ProductCycleCountFriendly?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1='+p1;}");

    scoutln(out, bytesOut, "function sanitise(code){");
    scoutln(out, bytesOut, "var code2='';var x;var len=code.length;");
    scoutln(out, bytesOut, "for(x=0;x<len;++x)");
    scoutln(out, bytesOut, "if(code.charAt(x)=='#')code2+='%23';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='\"')code2+='%22';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='&')code2+='%26';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='%')code2+='%25';");
    scoutln(out, bytesOut, "else if(code.charAt(x)==' ')code2+='%20';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='?')code2+='%3F';");
    scoutln(out, bytesOut, "else code2+=code.charAt(x);");
    scoutln(out, bytesOut, "return code2};");

    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\"></head>");

    int[] hmenuCount = new int[1];
 
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "3083", "", "ProductCycleCountInput", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<form>");

    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Cycle Count Generation", "3083", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<table id=\"page\" width=100%>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td colspan=3><p>This option shows you (up to today's date) all days for which stock check records have not been generated for counting.</td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap><p>Upto Date</td><td><p>");
    getDays(con, stmt, rs, out, true, "dateToY", dnm, localDefnsDir, defnsDir, bytesOut);
    scoutln(out, bytesOut, "</td><td nowrap><p><a href=\"javascript:generate('Y')\">Generate</a> New Items to Count</td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=3><hr></td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td colspan=3><p>This option allows you to print all days for which stock check records have been generated for counting (but not yet updated).</td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap><p>Upto Date</td><td><p>");
    getGeneratedDays(con, stmt, rs, out, "dateTo2", bytesOut);
    scoutln(out, bytesOut, "</td><td nowrap><p><a href=\"javascript:print()\">Print</a> Not-updated Items</td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=3><hr></td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td colspan=3><p>This option shows you all outstanding days for which stock check records have been generated but not yet updated (after counting).</td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap><p>Upto Date</td><td><p>");
    getGeneratedDays(con, stmt, rs, out, "dateToN", bytesOut);
    scoutln(out, bytesOut, "</td><td nowrap><p><a href=\"javascript:generate('N')\">Update</a> Items Already Generated</td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir));
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // given accounting year start date, and today's date
  // for each day (ignoring days not included), check against cycled table to see if already generated
  // then generate stockc records for each day not done to-date
  private void getDays(Connection con, Statement stmt, ResultSet rs, PrintWriter out, boolean ignoreAlreadyGenerated, String name, String dnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    String[] ignoreMondays        = new String[1];
    String[] ignoreTuesdays       = new String[1];
    String[] ignoreWednesdays     = new String[1];
    String[] ignoreThursdays      = new String[1];
    String[] ignoreFridays        = new String[1];
    String[] ignoreSaturdays      = new String[1];
    String[] ignoreSundays        = new String[1];
    String[] ignorePublicHolidays = new String[1];

    scoutln(out, bytesOut, "<SELECT NAME='" + name + "'>");

    // determine year from today's date
    String today = generalUtils.todaySQLFormat(localDefnsDir, defnsDir);

    String yyyy = today.substring(0, 4);
    int year = generalUtils.strToInt(yyyy);

    inventoryAdjustment.getCycleForYear(con, stmt, rs, yyyy, ignoreMondays, ignoreTuesdays, ignoreWednesdays, ignoreThursdays, ignoreFridays, ignoreSaturdays, ignoreSundays, ignorePublicHolidays);

    String[] financialYearStartMonth = new String[1];
    String[] financialYearEndMonth   = new String[1];

    definitionTables.getAppConfigFinancialYearMonths(con, stmt, rs, dnm, financialYearStartMonth, financialYearEndMonth);

    int startMonth = generalUtils.detMonthNumFromMonthName(financialYearStartMonth[0]);

    int startDateEncoded = generalUtils.encode(("01." + startMonth + "." + year), localDefnsDir, defnsDir);

    int todayEncoded = generalUtils.encodeFromYYYYMMDD(today);

    int theDate = todayEncoded;

    short thisDay, thisMonth, thisYear;
    String s;
    while(theDate >= startDateEncoded)
    {
      thisDay   = generalUtils.getDay(theDate);
      thisMonth = generalUtils.getMonth(theDate);
      thisYear  = generalUtils.getYear(theDate);

      if(dayIsNotIgnored(con, stmt, rs, ignoreAlreadyGenerated, theDate, ignoreMondays[0], ignoreTuesdays[0], ignoreWednesdays[0], ignoreThursdays[0], ignoreFridays[0], ignoreSaturdays[0], ignoreSundays[0], ignorePublicHolidays[0],
                         localDefnsDir, defnsDir))
      {
        s = thisDay + " " + generalUtils.yearAndMonthToMonthYearStr(thisMonth, thisYear);
        scoutln(out, bytesOut, "<OPTION VALUE=\"" + thisYear + "-" + thisMonth + "-" + thisDay + "\">" + s + "\n");
      }

      --theDate;
    }

    scoutln(out, bytesOut, "</SELECT>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void getGeneratedDays(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String name, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<SELECT NAME='" + name + "'>");

    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT DISTINCT Date FROM stockc WHERE Level = '999999' ORDER BY Date DESC");

      String s;
      int dateEncoded;
      short dd, mm, yy;
      
      while(rs.next())
      {
        dateEncoded = generalUtils.encodeFromYYYYMMDD(rs.getString(1));

        dd = generalUtils.getDay(dateEncoded);
        mm = generalUtils.getMonth(dateEncoded);
        yy = generalUtils.getYear(dateEncoded);

        s = dd + " " + generalUtils.yearAndMonthToMonthYearStr(mm, yy);
        scoutln(out, bytesOut, "<OPTION VALUE=\"" + yy + "-" + mm + "-" + dd + "\">" + s + "\n");
      }

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }

    scoutln(out, bytesOut, "</SELECT>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean dayIsNotIgnored(Connection con, Statement stmt, ResultSet rs, boolean ignoreAlreadyGenerated, int theDate, String ignoreMondays, String ignoreTuesdays, String ignoreWednesdays, String ignoreThursdays, String ignoreFridays,
                                  String ignoreSaturdays, String ignoreSundays, String ignorePublicHolidays, String localDefnsDir, String defnsDir)
  {
    try
    {
      String dow, aPH;
      int[] dayOfWeek = new int[1];
      int x, len = ignorePublicHolidays.length();

      dow = generalUtils.getDayOfWeek(generalUtils.decode(theDate, localDefnsDir, defnsDir), dayOfWeek, localDefnsDir, defnsDir);

      if(dow.equals("Monday") && ignoreMondays.equals("Y"))
        return false;

      if(dow.equals("Tuesday") && ignoreTuesdays.equals("Y"))
        return false;

      if(dow.equals("Wednesday") && ignoreWednesdays.equals("Y"))
        return false;

      if(dow.equals("Thursday") && ignoreThursdays.equals("Y"))
        return false;

      if(dow.equals("Friday") && ignoreFridays.equals("Y"))
        return false;

      if(dow.equals("Saturday") && ignoreSaturdays.equals("Y"))
        return false;

      if(dow.equals("Sunday") && ignoreSundays.equals("Y"))
        return false;

      x = 0;
      while(x < len)
      {
        aPH = "";
        while(x < len && ignorePublicHolidays.charAt(x) != ' ' && ignorePublicHolidays.charAt(x) != ',')
          aPH += ignorePublicHolidays.charAt(x++);

        while(x < len && (ignorePublicHolidays.charAt(x) == ' ' || ignorePublicHolidays.charAt(x) == ','))
          ++x;

        if(theDate == generalUtils.encode(aPH, localDefnsDir, defnsDir))
          return false;
      }

      if(ignoreAlreadyGenerated && alreadyGenerated(con, stmt, rs, generalUtils.decodeToYYYYMMDD(theDate)))
        return false;
    }
    catch(Exception e) { return false; }

    return true;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean alreadyGenerated(Connection con, Statement stmt, ResultSet rs, String date) throws Exception
  {
    int rowCount = 0;

    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT COUNT(*) as rowcount FROM cycled WHERE CountDate = '" + date + "'");

      if(rs.next())
        rowCount = rs.getInt("rowcount");

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }

    if(rowCount == 0)
      return false;

    return true;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {
    out.println(str);
    bytesOut[0] += (str.length() + 2);
  }

}
