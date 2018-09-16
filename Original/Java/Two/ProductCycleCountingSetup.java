// =======================================================================================================================================================================================================
// System: ZaraStar: Product: modify cycle counting setup
// Module: ProductCycleCountingSetup.java
// Author: C.K.Harvey
// Copyright (c) 2000-09 Christopher Harvey. All Rights Reserved.
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

public class ProductCycleCountingSetup extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  InventoryAdjustment inventoryAdjustment = new InventoryAdjustment();

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
      directoryUtils.setContentHeaders(res);
      out = res.getWriter();

      unm = req.getParameter("unm");
      sid = req.getParameter("sid");
      uty = req.getParameter("uty");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");
      p1  = req.getParameter("p1"); // year

      if(p1 == null) p1 = "";

      doIt(out, req, p1, unm, sid, uty, men, den, dnm, bnm, bytesOut);
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

      System.out.println("3081: " + e);
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "ProductCycleCountingSetup", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3081, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush(); 
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String p1, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 3081, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "ProductCycleCountingSetup", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3081, bytesOut[0], 0, "ACC:");
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "ProductCycleCountingSetup", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3081, bytesOut[0], 0, "SID:");
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    set(con, stmt, rs, out, req, p1, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 3081, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String year, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir,
                   int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Cycle Counting Setup</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    scoutln(out, bytesOut, "function update(){document.forms[0].submit()}");

    scoutln(out, bytesOut, "function change(){");
    scoutln(out, bytesOut, "this.location.href=\"ProductCycleCountDefinition?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + year + "\";}");

    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    int[] hmenuCount = new int[1];
 
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "3081", "", "ProductCycleCountSelection", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Cycle Counting Setup for " + year, "3081", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<form action=\"ProductCycleCountSetupSave\" enctype=\"application/x-www-form-urlencoded\" method=post>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"unm\" value='" + unm + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"sid\" value='" + sid + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"uty\" value='" + uty + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"men\" value='" + men + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"den\" value='" + den + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"dnm\" value='" + dnm + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"bnm\" value='" + bnm + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"year\" value='" + year + "'>");

    String[] ignoreMondays        = new String[1];
    String[] ignoreTuesdays       = new String[1];
    String[] ignoreWednesdays     = new String[1];
    String[] ignoreThursdays      = new String[1];
    String[] ignoreFridays        = new String[1];
    String[] ignoreSaturdays      = new String[1];
    String[] ignoreSundays        = new String[1];
    String[] ignorePublicHolidays = new String[1];

    inventoryAdjustment.getCycleForYear(con, stmt, rs, year, ignoreMondays, ignoreTuesdays, ignoreWednesdays, ignoreThursdays, ignoreFridays, ignoreSaturdays, ignoreSundays, ignorePublicHolidays);

    scoutln(out, bytesOut, "<table id=\"page\" width=100% border=0>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scout(out, bytesOut, "<tr><td nowrap><p>Ignore Mondays</td><td><p><input type=checkbox name=ignoreMondays");
    if(ignoreMondays[0].equals("Y"))
      scout(out, bytesOut, " checked");
    scoutln(out, bytesOut, "></td></tr>");

    scout(out, bytesOut, "<tr><td nowrap><p>Ignore Tuesdays</td><td><p><input type=checkbox name=ignoreTuesdays");
    if(ignoreTuesdays[0].equals("Y"))
      scout(out, bytesOut, " checked");
    scoutln(out, bytesOut, "></td></tr>");

    scout(out, bytesOut, "<tr><td nowrap><p>Ignore Wednesdays</td><td><p><input type=checkbox name=ignoreWednesdays");
    if(ignoreWednesdays[0].equals("Y"))
      scout(out, bytesOut, " checked");
    scoutln(out, bytesOut, "></td></tr>");

    scout(out, bytesOut, "<tr><td nowrap><p>Ignore Thursdays</td><td><p><input type=checkbox name=ignoreThursdays");
    if(ignoreThursdays[0].equals("Y"))
      scout(out, bytesOut, " checked");
    scoutln(out, bytesOut, "></td></tr>");

    scout(out, bytesOut, "<tr><td nowrap><p>Ignore Fridays</td><td><p><input type=checkbox name=ignoreFridays");
    if(ignoreFridays[0].equals("Y"))
      scout(out, bytesOut, " checked");
    scoutln(out, bytesOut, "></td></tr>");

    scout(out, bytesOut, "<tr><td nowrap><p>Ignore Saturdays</td><td><p><input type=checkbox name=ignoreSaturdays");
    if(ignoreSaturdays[0].equals("Y"))
      scout(out, bytesOut, " checked");
    scoutln(out, bytesOut, "></td></tr>");

    scout(out, bytesOut, "<tr><td nowrap><p>Ignore Sundays</td><td><p><input type=checkbox name=ignoreSundays");
    if(ignoreSundays[0].equals("Y"))
      scout(out, bytesOut, " checked");
    scoutln(out, bytesOut, "></td></tr>");

    scoutln(out, bytesOut, "<tr><td valign=top nowrap><p>Ignore Dates (Public Holidays):<br>(Separate dates using commas)</td><td><p><textarea name=ignorePublicHolidays cols=60 rows=5>" + ignorePublicHolidays[0] + "</textarea></td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td><p><a href=\"javascript:update()\">Update the Details Above</a></td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=2><hr></td></tr></table>");

    scoutln(out, bytesOut, "<table id='page' width=100% border=0>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr id=\"pageColumn\">");
    scoutln(out, bytesOut, "<td nowrap><p>Manufacturer &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Frequency &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Priority &nbsp;</td><td></td></tr>");

    list(con, stmt, rs, out, year, bytesOut);

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td><p><a href=\"javascript:change()\">Change</a> Manufacturer Setup</td></tr>");

    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir));
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void list(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String year, int[] bytesOut) throws Exception
  {
    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT * FROM cyclel WHERE Year = '" + year + "' ORDER BY Manufacturer");

      String mfr, priority, frequency, cssFormat = "";

      while(rs.next())
      {
        mfr       = rs.getString(2);
        priority  = rs.getString(3);
        frequency = rs.getString(4);

        if(cssFormat.equals("line2")) cssFormat = "line1"; else cssFormat = "line2";

        scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\">");
        scoutln(out, bytesOut, "<td><p>" + mfr + "</td>");
        scoutln(out, bytesOut, "<td><p>" + frequency + "</td>");

        if(priority.equals("H")) priority = "High"; else priority = "";

        scoutln(out, bytesOut, "<td><p>" + priority + "</td></tr>");
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
