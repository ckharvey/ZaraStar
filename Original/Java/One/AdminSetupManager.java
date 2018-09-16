// =======================================================================================================================================================================================================
// System: ZaraStar Admin: Setup - Manager
// Module: AdminControlg.java
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

public class AdminSetupManager extends HttpServlet
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

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="";

    try
    {
      directoryUtils.setContentHeaders2(res);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "AdminControlg", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7500, bytesOut[0], 0, "ERR:");
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

    if((! serverUtils.isSysAdmin(con, stmt, rs, unm, dnm, sid, uty, localDefnsDir, defnsDir)) && (! serverUtils.isDBAdmin(con, stmt, rs, unm)))
    {
      messagePage.msgScreenW(out, req, 13, unm, sid, uty, men, den, dnm, bnm, "AdminControl", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7500, bytesOut[0], 0, "ACC:");
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreenW(out, req, 12, unm, sid, uty, men, den, dnm, bnm, "AdminControl", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7500, bytesOut[0], 0, "SID:");
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    scoutln(out, bytesOut, "7500\001Admin\001Setup: Mangaer\001javascript:getHTML('AdminControlWave','')\001\001\001\001\003");

    create(con, stmt, rs, out, req, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 7500, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void create(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir,
                      int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    dashboardUtils.drawTitleW(out, "Administration - Manager", "7500", unm, sid, uty, men, den, dnm, bnm, bytesOut);

    scoutln(out, bytesOut, "<table id=\"page\" border=0>");

    scoutln(out, bytesOut, "<tr><td nowrap><h1>System Settings</td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"javascript:getHTML('AdminSetSystemStatusw','')\">Modify</a> System Status</td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"javascript:getHTML('AdminTimeoutsDefinitionEditw','')\">Modify</a> Timeout Settings</td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap><h1>Feature Access</td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"javascript:getHTML('OptionTabsWave','&p1=A&p2=7519')\">Modify</a> Site Access</td></tr>");

    boolean AdminUserStyleChange = authenticationUtils.verifyAccess(con, stmt, rs, req, 7074, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean WikiStylingUserStyle = authenticationUtils.verifyAccess(con, stmt, rs, req, 7082, unm, uty, dnm, localDefnsDir, defnsDir);

    if(AdminUserStyleChange || WikiStylingUserStyle)
    {
      scoutln(out, bytesOut, "<tr><td nowrap><h1>Style Services</td></tr>");

      if(AdminUserStyleChange)
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"javascript:getHTML('AdminUserStyleChangew','')\">Change</a> your Presentation Style</td></tr>");

      if(WikiStylingUserStyle)
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"javascript:getHTML('WikiStylingUserStylew','')\">Change</a> Presentation Styles for All Users</td></tr>");
    }

    boolean adminServicesManager = authenticationUtils.verifyAccess(con, stmt, rs, req, 7034, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean adminListDetailedAccessRights = authenticationUtils.verifyAccess(con, stmt, rs, req, 7004, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean contactsUserProfileList = authenticationUtils.verifyAccess(con, stmt, rs, req, 8830, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean adminUserGroupCreate = authenticationUtils.verifyAccess(con, stmt, rs, req, 7051, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean adminPeopleManagementCreate = authenticationUtils.verifyAccess(con, stmt, rs, req, 7053, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean adminControlUtils = authenticationUtils.verifyAccess(con, stmt, rs, req, 7599, unm, uty, dnm, localDefnsDir, defnsDir);

    if(adminServicesManager || adminListDetailedAccessRights || contactsUserProfileList || adminUserGroupCreate || adminPeopleManagementCreate || adminControlUtils)
    {
      scoutln(out, bytesOut, "<tr><td nowrap><h1>User Management Services</td></tr>");

      if(contactsUserProfileList)
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"javascript:getHTML('ContactUserProfileListWave','')\">User Accounts</a></td></tr>");

      if(adminControlUtils)
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"javascript:getHTML('AdminControlUtilsAdminUser','')\">User Access</a></td></tr>");

      if(adminUserGroupCreate)
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"javascript:getHTML('AdminUserGroupCreateWave','')\">User Groups</a></td></tr>");

      if(adminPeopleManagementCreate)
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"javascript:getHTML('AdminPeopleManagementCreateWave','')\">SalesPeople Management</a></td></tr>");

      if(adminServicesManager)
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"javascript:getHTML('AdminServicesManagerw','')\">Access</a> the Services Manager</td></tr>");

      if(adminListDetailedAccessRights)
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"javascript:getHTML('AdminListDetailedAccessRightsw','')\">Access</a> Detailed User Access Rights</td></tr>");
    }

    boolean AdminLogManagerInternalUsers = authenticationUtils.verifyAccess(con, stmt, rs, req, 7010, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean AdminLogManager = authenticationUtils.verifyAccess(con, stmt, rs, req, 7030, unm, uty, dnm, localDefnsDir, defnsDir);

    if(AdminLogManagerInternalUsers || AdminLogManager)
    {
      scoutln(out, bytesOut, "<tr><td nowrap><h1>User Log Services</td></tr>");

      if(AdminLogManagerInternalUsers)
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"javascript:getHTML('AdminLogManagerInternalUsersw','')\">View</a> User Log Files (Internal Users)</td></tr>");

      if(AdminLogManager)
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"javascript:getHTML('AdminLogManagerw','')\">Manage</a> User Log Files (External Users)</td></tr>");
    }

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "</table>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {
    out.println(str);
    bytesOut[0] += (str.length() + 2);
  }

}
