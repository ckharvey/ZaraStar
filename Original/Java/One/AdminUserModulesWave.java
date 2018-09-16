// =======================================================================================================================================================================================================
// System: ZaraStar Admin: User Modules Definition
// Module: AdminUserModules.java
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

public class AdminUserModulesWave extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  DashboardUtils dashboardUtils = new DashboardUtils();

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
      uty = req.getParameter("uty");
      sid = req.getParameter("sid");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");
      p1  = req.getParameter("p1"); // userCode

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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "AdminUserModules", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7008, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush();
    }
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String userCode, int[] bytesOut) throws Exception
  {
    String defnsDir         = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir        = directoryUtils.getSupportDirs('I');

    long startTime = new java.util.Date().getTime();

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 7008, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "AdminUserModules", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7008, bytesOut[0], 0, "ACC:" + userCode);
      if(con   != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "AdminUserModules", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7008, bytesOut[0], 0, "SID:" + userCode);
      if(con   != null) con.close();
      if(out != null) out.flush();
      return;
    }

    scoutln(out, bytesOut, "7008\001Admin\001User Access Rights\001javascript:getHTML('AdminUserModulesWave','')\001\001\001\001\003");

    create(con, stmt, rs, out, req, userCode, unm, sid, uty, men, den, dnm, bnm, imagesDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 7008, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), userCode);
    if(con   != null) con.close();
    if(out != null) out.flush();
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void create(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String userCode, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String imagesDir,
                      String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    scoutln(out, bytesOut, "function same(){var p2=document.forms[0].users.value;getHTML('AdminSetUserWave','&p1=" + userCode + "&p2=\"+p2)}");

    scoutln(out, bytesOut, "</script>");

    dashboardUtils.drawTitleW(out, "Administration - User Rights for: " + userCode, "7008", unm, sid, uty, men, den, dnm, bnm, bytesOut);

    scoutln(out, bytesOut, "<form><table id=\"page\" border=0 width=100%>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td colspan=3><p>&nbsp;<a href=\"javascript:same()\">Give</a> " + userCode + " the same access rights as user&nbsp;&nbsp;");
    userDDL(con, stmt, rs, out, userCode, bytesOut);
    scoutln(out, bytesOut, "</td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "</table></form>");
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void show(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String userCode, String moduleDesc, String module, boolean[] line1, String[] cssFormat, String[] modules, int[] bytesOut) throws Exception
  {
    if(moduleGloballyActivated(con, stmt, rs, module))
    {
      if(line1[0]) { cssFormat[0] = "line1"; line1[0] = false; } else { cssFormat[0] = "line2"; line1[0] = true; }

      scoutln(out, bytesOut, "<tr id=\"" + cssFormat[0] + "\">");

      scoutln(out, bytesOut, "<td><p><input type='checkbox' name='" + module + "'");
      if(moduleActivated(con, stmt, rs, userCode, module))
        scoutln(out, bytesOut, " checked");
      scoutln(out, bytesOut, ">");

      scoutln(out, bytesOut, "<td nowrap><p>" + moduleDesc + "</td>");
      scoutln(out, bytesOut, "<td><p><a href=\"javascript:rights('" + module + "','" + moduleDesc + "')\">Fine Tune</a></td></tr>");

      modules[0] += (module + "/");
    }
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean moduleGloballyActivated(Connection con, Statement stmt, ResultSet rs, String module) throws Exception
  {
    int rowCount = 0;

    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT count(*) AS rowcount FROM systemmodules WHERE Module = '" + module + "'");

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

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean moduleActivated(Connection con, Statement stmt, ResultSet rs, String userCode, String module) throws Exception
  {
    int rowCount = 0;

    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT count(*) AS rowcount FROM usermodules WHERE UserCode = '" + userCode + "' AND Module = '" + module + "'");

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

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void userDDL(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String thisUserCode, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<select name=\"users\">");

    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT UserCode, UserName FROM profiles WHERE UserCode != '" + thisUserCode + "' AND UserCode != 'Sysadmin' AND UserCode != '___registered___' AND UserCode != '___casual___' ORDER BY UserName");

      while(rs.next())
        scoutln(out, bytesOut, "<option value=\"" + rs.getString(1) + "\">" + rs.getString(2));

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }

    scoutln(out, bytesOut, "</select>");
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {
    out.println(str);
    bytesOut[0] += (str.length() + 2);
  }

}
