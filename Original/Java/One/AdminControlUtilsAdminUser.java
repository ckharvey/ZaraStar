// =======================================================================================================================================================================================================
// System: ZaraStar Admin: Admin user access
// Module: AdminControlUtilsAdminUser.java
// Author: C.K.Harvey
// Copyright (c) 2003-09 Christopher Harvey. All Rights Reserved.
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

public class AdminControlUtilsAdminUser extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  AdminUtils adminUtils = new AdminUtils();
  AdminControlUtilsWave adminControlUtilsWave = new AdminControlUtilsWave();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p2="";

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

      p2  = req.getParameter("p2"); // svc (on subsequent Access call)

      if(p2 == null) p2 = "7500";

      doIt(out, req, p2, unm, sid, uty, men, den, dnm, bnm, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "AdminControlUtils", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7599, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String p2, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null, stmt2 = null;
    ResultSet rs   = null, rs2   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 7599, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreenW(out, req, 13, unm, sid, uty, men, den, dnm, bnm, "AdminControlUtils", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7599, bytesOut[0], 0, "ACC:");
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreenW(out, req, 12, unm, sid, uty, men, den, dnm, bnm, "AdminControlUtils", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7599, bytesOut[0], 0, "SID:");
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    scoutln(out, bytesOut, "7599\001Admin\001Admin User Access\001javascript:getHTML('AdminControlUtilsAdminUser','')\001\001Y\001\001\003");

    set(con, stmt, stmt2, rs, rs2, out, req, p2, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 7599, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, HttpServletRequest req, String reqdSvc, String unm, String sid, String uty, String men, String den, String dnm,
                   String bnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    scoutln(out, bytesOut, "function again(svc){  getHTML('AdminControlUtilsAdminUser','&p2='+svc); }  ");

    scoutln(out, bytesOut, "function modify(svc,desc){var p3=sanitise(desc);getHTML('AdminAccessUpdateWave','&p3='+p3+'&p2=" + reqdSvc + "&p1='+svc);}");

    scoutln(out, bytesOut, "function disable(svc){getHTML('AdminSuspendUpdateWave','&p2=" + reqdSvc + "&p1=\"+svc');}");

    scoutln(out, bytesOut, "function refine(svc,desc,userCode){var p2=sanitise(desc);var p4=sanitise(userCode);getHTML('AdminAccessRefinementsWave','&p2='+p2+'&p4='+p4+'&p1='+svc);}");

    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    adminUtils.drawTitleW(out, "Administration - User Access", "7599", bytesOut);

    String table = adminControlUtilsWave.getTable();

    createScreen(con, stmt, stmt2, rs, rs2, req, out, reqdSvc, table, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "</table>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void createScreen(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, HttpServletRequest req, PrintWriter out, String reqdSvc, String table, String unm, String sid, String uty, String men, String den, String dnm,
                            String bnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    int tableLen = table.length();

    try
    {
      String uName = directoryUtils.getMySQLUserName();
      String pWord = directoryUtils.getMySQLPassWord();
      Class.forName("com.mysql.jdbc.Driver").newInstance();
      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + uName + "&password=" + pWord);

      String svc, svc2, name, name2, allServices;
      boolean first = true;

      int x = 0;
      while(x < tableLen)
      {
        svc = "";
        while(table.charAt(x) != ',')
          svc += table.charAt(x++);
        ++x;
        name = "";
        while(table.charAt(x) != ',')
          name += table.charAt(x++);

        ++x;
        svc2 = "";
        while(table.charAt(x) != ',')
          svc2 += table.charAt(x++);
        ++x;
        name2 = "";
        while(table.charAt(x) != ',' && table.charAt(x) != ';' && table.charAt(x) != '.')
          name2 += table.charAt(x++);

        if(table.charAt(x) == ',') // more services
        {
          ++x;
          allServices = "";
          while(table.charAt(x) != ';')
            allServices += table.charAt(x++);
        }
        else allServices = "";

        if(svc.equals(reqdSvc))
        {
          if(first)
          {
            scoutln(out, bytesOut, "<table id='page' border=0 width=100%>");
            first = false;
          }

          if(table.charAt(x) == '.') // more services
            aModule(con, stmt, rs, out, svc2, name2, bytesOut);
          else
          if(anyFurtherLevels(table, svc2, tableLen))
            aLevel(con, stmt, rs, out, svc2, name2, bytesOut);
          else aService(con, stmt, stmt2, rs, rs2, out, svc2, name2, allServices, bytesOut);
        }

        ++x;
      }

      scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
      scoutln(out, bytesOut, "</table>");
    }
    catch(Exception e)
    {
      System.out.println(e);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    if(con != null) con.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void aLevel(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String service, String desc, int[] bytesOut) throws Exception
  {
    String status, action;

    if(adminControlUtilsWave.isSuspended(con, stmt, rs, service)) { status = "Disabled"; action = "Enable"; } else { status = "Live"; action = "Disable"; }

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap><p><a href=\"javascript:help('" + service + "')\">" + service + "</a>: " + desc + " &nbsp;&nbsp;&nbsp;(Status is: " + status + "... <a href=\"javascript:disable('" + service + "')\">" + action
                         + "</a>)</td></tr>");
    scoutln(out, bytesOut, "<tr id=\"pageColumn\"><td></td>");

    scoutln(out, bytesOut, "<td width=99% nowrap><p><a href=\"javascript:again('" + service + "')\">Further Options</a></td></tr>");

    list(con, stmt, null, rs, null, out, service, desc, "", bytesOut);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void aService(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, String service, String desc, String allServices, int[] bytesOut) throws Exception
  {
    String status, action;

    if(adminControlUtilsWave.isSuspended(con, stmt, rs, service)) { status = "Disabled"; action = "Enable"; } else { status = "Live"; action = "Disable"; }

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap><p><a href=\"javascript:help('" + service + "')\">" + service + "</a>: " + desc + " &nbsp;&nbsp;&nbsp;(Status is: " + status + "... <a href=\"javascript:disable('" + service + "')\">" + action
                         + "</a>)</td></tr>");
    scoutln(out, bytesOut, "<tr id=\"pageColumn\"><td></td>");
    scoutln(out, bytesOut, "<td width=80% nowrap colspan=2><p>User Code</td>");
    scoutln(out, bytesOut, "<td nowrap align=right><p><a href=\"javascript:modify('" + service + "','Enable " + desc + "')\">Modify</a></td></tr>");

    list(con, stmt, stmt2, rs, rs2, out, service, desc, allServices, bytesOut);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void aModule(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String service, String desc, int[] bytesOut) throws Exception
  {
    String status, action;

    if(adminControlUtilsWave.isSuspended(con, stmt, rs, service)) { status = "Disabled"; action = "Enable"; } else { status = "Live"; action = "Disable"; }

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap><p><a href=\"javascript:help('" + service + "')\">" + service + "</a>: " + desc + " &nbsp;&nbsp;&nbsp;(Status is: " + status + "... <a href=\"javascript:disable('" + service + "')\">" + action
                         + "</a>)</td></tr>");
    scoutln(out, bytesOut, "<tr id=\"pageColumn\"><td></td>");
    scoutln(out, bytesOut, "<td width=80% nowrap colspan=2></td></tr>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {
    out.println(str);
    bytesOut[0] += (str.length() + 2);
  }


  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void list(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, String service, String desc, String allServices, int[] bytesOut) throws Exception
  {
    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT UserCode FROM userservices WHERE Service = '" + service + "' ORDER BY UserCode");

      String userCode, cssFormat = "", subServices;

      while(rs.next())
      {
        userCode = rs.getString(1);

        if(userCode.equals("Sysadmin") || userCode.startsWith("___"))
          ;
        else
        {
          if(cssFormat.equals("line2")) cssFormat = "line1"; else cssFormat = "line2";

          scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\"><td></td>");

          if(allServices.length() > 0)
          {
            subServices = getSubServicesForThisUser(con, stmt2, rs2, allServices, userCode);
            scoutln(out, bytesOut, "<td><p>" + userCode + "</td><td width=80% nowrap><p>View" + subServices + "</td>");
            scoutln(out, bytesOut, "<td nowrap align=right><p><a href=\"javascript:refine('" + allServices + "','" + desc + "','" + userCode + "')\">Refine</a></td>");
          }
          else scoutln(out, bytesOut, "<td colspan=2><p>" + userCode + "</td>");

          scoutln(out, bytesOut, "</tr>");
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

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String getSubServicesForThisUser(Connection con, Statement stmt, ResultSet rs, String allServices, String userCode) throws Exception
  {
    int x = 0, len = allServices.length();
    String s = "", service, op;

    while(x < len)
    {
      service = "";
      while(x < len && allServices.charAt(x) != ',')
        service += allServices.charAt(x++);
      ++x;

      op = "";
      while(x < len && allServices.charAt(x) != ',')
        op += allServices.charAt(x++);
      ++x;

      if(alreadyHasAccess(con, stmt, rs, service, userCode))
        s += (", " + op);
    }

    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean alreadyHasAccess(Connection con, Statement stmt, ResultSet rs, String service, String userCode) throws Exception
  {
    int rowcount = 0;

    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM userservices WHERE Service = '" + service + "' AND UserCode = '" + userCode + "'");

      if(rs.next())
        rowcount = rs.getInt("rowcount");

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }

    if(rowcount == 0)
      return false;
    return true;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean anyFurtherLevels(String table, String service, int tableLen) throws Exception
  {
    String svc;
    int x = 0;
    while(x < tableLen)
    {
      svc = "";
      while(table.charAt(x) != ',')
        svc += table.charAt(x++);
      ++x;

      if(svc.equals(service))
        return true;

      while(table.charAt(x) != ';' && table.charAt(x) != '.')
        ++x;
      ++x;
    }

    return false;
  }

}
