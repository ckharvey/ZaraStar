// =======================================================================================================================================================================================================
// System: ZaraStar UtilsEngine: System services access page
// Module: SystemServices.java
// Author: C.K.Harvey
// Copyright (c) 2002-08 Christopher Harvey. All Rights Reserved.
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

public class SystemServicesWave extends HttpServlet
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "SystemServices", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 117, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                    int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 117, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "SystemServices", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 117, bytesOut[0], 0, "ACC:");
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "SystemServices", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 117, bytesOut[0], 0, "SID:");
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    scoutln(out, bytesOut, "117\001Admin\001Admin System\001javascript:getHTML('SystemServicesWave','')\001\001Y\001\001\003");

    set(con, stmt, rs, out, req, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 117, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut)
                   throws Exception
  {
    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    adminUtils.drawTitleW(out, "Administration - User Services", "117", bytesOut);

    scoutln(out, bytesOut, "<table id=\"page\" border=0 width=100%>");

    scoutln(out, bytesOut, "<tr><td nowrap><h1>Application-Related</td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
    scoutln(out, bytesOut, "<a href=\"javascript:getHTML('AdminAppconfigEditw','')\">Change</a> Configuration</td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
    scoutln(out, bytesOut, "<a href=\"/central/servlet/AdminPeoplePositioning?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Change</a> Person Positioning</td><td nowrap width=90% nowrap>"
                            + directoryUtils.buildHelp(7056) + "</td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"javascript:getHTML('WikiStylingWave','')\">Change</a> Styling</a></td></tr>");

    boolean AdminSteelclawsSites = authenticationUtils.verifyAccess(con, stmt, rs, req, 7005, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean ExternalUserServices3 = authenticationUtils.verifyAccess(con, stmt, rs, req, 9013, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean ExternalUserServices1 = authenticationUtils.verifyAccess(con, stmt, rs, req, 9011, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean ExternalUserServices2 = authenticationUtils.verifyAccess(con, stmt, rs, req, 9012, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean ExternalUserServices4 = authenticationUtils.verifyAccess(con, stmt, rs, req, 9014, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean ZaracloudDataBase = authenticationUtils.verifyAccess(con, stmt, rs, req, 9015, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean _19500 = authenticationUtils.verifyAccess(con, stmt, rs, req, 19500, unm, uty, dnm, localDefnsDir, defnsDir);

    if(AdminSteelclawsSites || ExternalUserServices3 || ExternalUserServices1 || ExternalUserServices2 || ExternalUserServices4 || ZaracloudDataBase || _19500)
    {
      scoutln(out, bytesOut, "<tr><td nowrap><h1>Zara Cloud Services</td></tr>");

      if(ZaracloudDataBase)
      {
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
        scoutln(out, bytesOut, "<a href=\"/central/servlet/ZaracloudDataBase?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm="
                             + dnm + "&bnm=" + bnm + "\">Create</a> Zaracloud_scbs DataBase</td><td nowrap width=90% nowrap>" + directoryUtils.buildHelp(9015) + "</td></tr>");

        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
        scoutln(out, bytesOut, "<a href=\"/central/servlet/ZaracloudTable?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm="
                             + dnm + "&bnm=" + bnm + "\">ReCreate</a> scbs Table</td><td nowrap width=90% nowrap>" + directoryUtils.buildHelp(9015) + "</td></tr>");

        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
        scoutln(out, bytesOut, "<a href=\"/central/servlet/ServicesUpload?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm="
                             + dnm + "&bnm=" + bnm + "\">Import</a> scbs.dfn into scbs Table</td><td nowrap width=90% nowrap>" + directoryUtils.buildHelp(9015)
                             + "</td></tr>");

        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
        scoutln(out, bytesOut, "<a href=\"/central/servlet/ZaracloudHelp?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm="
                             + dnm + "&bnm=" + bnm + "\">ReCreate</a> help Table</td><td nowrap width=90% nowrap>" + directoryUtils.buildHelp(9015) + "</td></tr>");
      }

      if(ExternalUserServices2)
      {
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
        scoutln(out, bytesOut, "<a href=\"/central/servlet/ExternalUserServices2?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm="
                             + dnm + "&bnm=" + bnm + "\">Reset</a> Consolidated DataBase and Tables</td><td nowrap width=90% nowrap>" + directoryUtils.buildHelp(9012) + "</td></tr>");
      }

      if(_19500)
      {
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
        scoutln(out, bytesOut, "<a href=\"/central/servlet/_19500?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm="
                             + dnm + "&bnm=" + bnm + "\">Scan</a> the Green Book</td><td nowrap width=90% nowrap>" + directoryUtils.buildHelp(19500) + "</td></tr>");
      }

      if(AdminSteelclawsSites)
      {
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
        scoutln(out, bytesOut, "<a href=\"/central/servlet/AdminSteelclawsSites?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm="
                             + dnm + "&bnm=" + bnm + "\">View</a> Sites</td><td nowrap width=90% nowrap>" + directoryUtils.buildHelp(7005) + "</td></tr>");
      }

      if(ExternalUserServices3)
      {
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
        scoutln(out, bytesOut, "<a href=\"/central/servlet/ExternalUserServices3?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm="
                             + dnm + "&bnm=" + bnm + "\">Launch</a> Robot</td><td nowrap width=90% nowrap>" + directoryUtils.buildHelp(9013) + "</td></tr>");
      }

      if(ExternalUserServices4)
      {
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
        scoutln(out, bytesOut, "<a href=\"/central/servlet/ExternalUserServices4?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm="
                             + dnm + "&bnm=" + bnm + "\">Launch</a> Spider</td><td nowrap width=90% nowrap>" + directoryUtils.buildHelp(9014) + "</td></tr>");
      }

      if(ExternalUserServices1)
      {
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
        scoutln(out, bytesOut, "<a href=\"/central/servlet/ExternalUserServices1?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm="
                             + dnm + "&bnm=" + bnm + "\">Search</a></td><td nowrap width=90% nowrap>" + directoryUtils.buildHelp(9011) + "</td></tr>");
      }
    }

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "</table>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {
    out.println(str);
    bytesOut[0] += (str.length() + 2);
  }

}
