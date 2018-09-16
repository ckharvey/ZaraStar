// =======================================================================================================================================================================================================
// System: ZaraStar Admin: Admin user services access page
// Module: AdminServicesAccess.java
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

public class AdminServicesAccess extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "AdminServicesAccess", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 112, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men,
                      String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;
    
    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 112, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "AdminServicesAccess", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 112, bytesOut[0], 0, "ACC:");
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "AdminServicesAccess", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 112, bytesOut[0], 0, "SID:");
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    set(con, stmt, rs, out, req, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 112, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm,
                   String bnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Admin - User Services Access</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");
    
    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");
    
    int[] hmenuCount = new int[1];

    adminUtils.outputPageFrame(con, stmt, rs, out, req, "AdminServicesAccess", "", "112", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount, bytesOut);
 
    adminUtils.drawTitle(con, stmt, rs, req, out, "Administration - User Services", "112", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
  
    scoutln(out, bytesOut, "<table id=\"page\" border=0 width=100%>");

    boolean AdminUserStyleChange = authenticationUtils.verifyAccess(con, stmt, rs, req, 7074, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean WikiStylingUserStyle = authenticationUtils.verifyAccess(con, stmt, rs, req, 7082, unm, uty, dnm, localDefnsDir, defnsDir);
    
    if(AdminUserStyleChange || WikiStylingUserStyle)
    {
      scoutln(out, bytesOut, "<tr><td nowrap><h1>Style Services</td></tr>");
    
      if(AdminUserStyleChange)
      {
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
        scoutln(out, bytesOut, "<a href=\"/central/servlet/AdminUserStyleChange?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den="
                             + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Change</a> your Presentation Style</td><td nowrap width=90% nowrap>"
                             + directoryUtils.buildHelp(7074) + "</td></tr>");
      }
      
      if(WikiStylingUserStyle)
      {
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
        scoutln(out, bytesOut, "<a href=\"/central/servlet/WikiStylingUserStyle?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den="
                             + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Change</a> Presentation Styles for All Users</td><td nowrap width=90% nowrap>" + directoryUtils.buildHelp(7082) + "</td></tr>");
      }
    }

    boolean adminServicesManager = authenticationUtils.verifyAccess(con, stmt, rs, req, 7034, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean adminListDetailedAccessRights = authenticationUtils.verifyAccess(con, stmt, rs, req, 7004, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean AdminDirectoryDefinition = authenticationUtils.verifyAccess(con, stmt, rs, req, 7068, unm, uty, dnm, localDefnsDir, defnsDir);

    if(adminServicesManager || adminListDetailedAccessRights)
    {
      scoutln(out, bytesOut, "<tr><td nowrap><h1>User Management Services</td></tr>");
    
      if(adminServicesManager)
      {
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
        scoutln(out, bytesOut, "<a href=\"/central/servlet/AdminServicesManager?unm=" + unm + "&sid=" + sid
                             + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Access</a>"
                             + " the Services Manager</td><td nowrap width=90% nowrap>" + directoryUtils.buildHelp(7034) + "</td></tr>");
      }

      if(adminListDetailedAccessRights)
      {
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
        scoutln(out, bytesOut, "<a href=\"/central/servlet/AdminListDetailedAccessRights?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm="
                             + dnm + "&bnm=" + bnm + "\">Access</a> Detailed User Access Rights</td><td nowrap width=90% nowrap>" + directoryUtils.buildHelp(7004)
                             + "</td></tr>");
      }
    
      if(dnm.equals("Zaracloud") && AdminDirectoryDefinition)
      {
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
        scoutln(out, bytesOut, "<a href=\"/central/servlet/AdminDirectoryDefinition?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm="
                             + dnm + "&bnm=" + bnm + "\">Access</a> Directory</td><td nowrap width=90% nowrap>" + directoryUtils.buildHelp(7068)
                             + "</td></tr>");
      }
    }

    boolean AdminLogManagerInternalUsers = authenticationUtils.verifyAccess(con, stmt, rs, req, 7010, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean AdminLogManager = authenticationUtils.verifyAccess(con, stmt, rs, req, 7030, unm, uty, dnm, localDefnsDir, defnsDir);

    if(AdminLogManagerInternalUsers || AdminLogManager)
    {
      scoutln(out, bytesOut, "<tr><td nowrap><h1>User Log Services</td></tr>");
    
      if(AdminLogManagerInternalUsers)
      {
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
        scoutln(out, bytesOut, "<a href=\"/central/servlet/AdminLogManagerInternalUsers?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm
                             + "&bnm=" + bnm + "\">View</a> User Log Files (Internal Users)</td><td nowrap width=90% nowrap>" + directoryUtils.buildHelp(7010)
                             + "</td></tr>");
      }
    
      if(AdminLogManager)
      {
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
        scoutln(out, bytesOut, "<a href=\"/central/servlet/AdminLogManager?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm
                             + "&bnm=" + bnm + "\">Manage</a> User Log Files (External Users)</td><td nowrap width=90% nowrap>" + directoryUtils.buildHelp(7030)
                             + "</td></tr>");
      }
    }

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "</table>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir));
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
