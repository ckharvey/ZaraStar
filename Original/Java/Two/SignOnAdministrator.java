// =======================================================================================================================================================================================================
// System: ZaraStar: Utils: SignOn Administrator
// Module: SignOnAdministrator.java
// Author: C.K.Harvey
// Copyright (c) 1998-2007 Christopher Harvey. All Rights Reserved.
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

public class SignOnAdministrator extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", bnm="", dnm="", men="", den="",    sid="", uty="";

    try
    {
      directoryUtils.setContentHeaders(res);
      out = res.getWriter();

      unm = req.getParameter("unm");
      bnm = req.getParameter("bnm");
      dnm = req.getParameter("dnm");
      men = req.getParameter("men");
      den = req.getParameter("den");

      sid = req.getParameter("sid");
      uty = req.getParameter("uty");

      if(unm == null) unm = "";
      if(bnm == null) bnm = "";
      if(dnm == null) dnm = "";

      doIt(out, req, unm, sid, uty, bnm, dnm, men, den, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, "", dnm, bnm, urlBit, men, den, "A", "SignOnAdministrator", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 400, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String bnm, String dnm, String men, String den, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    String cssDirectory = directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, localDefnsDir);

    scoutln(out, bytesOut, "<html><head><title>ZaraStar Administrator</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\"");

    scoutln(out, bytesOut, "<link rel='stylesheet' type='text/css' media='screen' href='" + cssDirectory + "general.css'>");

    scoutln(out, bytesOut, "function help(which){");
    scoutln(out, bytesOut, "zarahelp.location.href=\"/central/servlet/HelpguideJump?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+which;}");

    scoutln(out, bytesOut, "</script></head><body>");

    scoutln(out, bytesOut, "<form action=\"http://" + men + "/central/servlet/SignOn\" enctype=\"application/x-www-form-urlencoded\"" + "method=POST>");

    scoutln(out, bytesOut, "<input type='hidden' name='atunm' value='" + unm + "'>"); // casual name already allocated
    scoutln(out, bytesOut, "<input type='hidden' name='men' value='" + men + "'>");
    scoutln(out, bytesOut, "<input type='hidden' name='den' value='" + den + "'>");
    scoutln(out, bytesOut, "<input type='hidden' name='dnm' value='" + dnm + "'>");
    scoutln(out, bytesOut, "<input type='hidden' name='bnm' value='" + bnm + "'>");

    heading(out, false, unm, sid, uty, bnm, dnm, men, den, imagesDir, bytesOut);

    scoutln(out, bytesOut, "<table cellspacing='0' cellpadding='0' border='0'><tr><td width=400></td>");
    scoutln(out, bytesOut, "<td><br><br><br><br></td></tr><tr><td></td><td><p><font color=darkred size=2 face='verdana,arial,helvetica,sans-serif'>UserName: &nbsp;&nbsp;</td>");
    scoutln(out, bytesOut, "<td><input type='text' size='20' name='unm' />");
    scoutln(out, bytesOut, "</td></tr><tr><td><br></td></tr><tr><td></td><td><font color=darkred size=2 face='verdana,arial,helvetica,sans-serif'>PassWord:</td>");
    scoutln(out, bytesOut, "<td><input type='password' size='20' name='pwd'' />");
    scoutln(out, bytesOut, "</td></tr><tr><td><br><br><br><br><br><br></td><td></td><td><input type='submit' value='  SignOn  '></input></td></tr></table></form></body></html>");

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 400, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // another copy in MessagePage
  public void heading(PrintWriter out, boolean showTabs, String unm, String sid, String uty, String bnm, String dnm, String men, String den, String imagesDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<script language=\"JavaScript\">");
    scoutln(out, bytesOut, "function set(option){");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/OptionTabs?p1=\"+option+\"&unm=" + unm + "&uty=" + uty + "&sid=" + sid + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\";}");
    scoutln(out, bytesOut, "function help(svc){var newWindow=window.open('','zarahelp');");
    scoutln(out, bytesOut, "newWindow.location.href=\"/central/servlet/HelpguideJump?p1=\"+svc+\"&unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\";}");
    scoutln(out, bytesOut, "</script>");

    scoutln(out, bytesOut, "<table width='100%' border='0' cellpadding='0' cellspacing='0' bgcolor=darkred>");
    scoutln(out, bytesOut, "<tr><td width='100%'><img src='" + imagesDir + "zarastar.jpg' alt='ZaraStar Admin' border='0' />");
    scoutln(out, bytesOut, "</td></tr></table>");

    scoutln(out, bytesOut, "<table width='100%' border='0' cellpadding='0' cellspacing='0' bgcolor=black>");
    scoutln(out, bytesOut, "<tr><td align=right valign=top><p><font color=white size=2 face='verdana,arial,helvetica,sans-serif'>User: " + unm + " &nbsp;&nbsp; Server: " + dnm
                         + "&nbsp;<br>ZaraStar Administrator &nbsp;&nbsp;&nbsp;&nbsp; (c) 1997-2009 Christopher Harvey</td></tr></table>");

    if(showTabs)
    {
      scoutln(out, bytesOut, "<table width='100%' border='0' cellpadding='0' cellspacing='0' bgcolor=lightgrey><td>");
      scoutln(out, bytesOut, "<td nowrap bgcolor=lightblue><p><font color=white size=2 face='verdana,arial,helvetica,sans-serif'> &nbsp; <a href=\"javascript:set('A')\">Access</a> &nbsp; </td>"
                           + "<td nowrap bgColor=lightblue><p><font color=white size=2 face='verdana,arial,helvetica,sans-serif'> &nbsp; <a href=\"javascript:set('C')\">Customise</a> &nbsp; </td>"
                           + "<td nowrap bgcolor=lightblue><p><font color=white size=2 face='verdana,arial,helvetica,sans-serif'> &nbsp; <a href=\"javascript:set('U')\">Users</a> &nbsp; </td>"
                           + "<td nowrap bgcolor=lightblue><p><font color=white size=2 face='verdana,arial,helvetica,sans-serif'> &nbsp; <a href=\"javascript:set('N')\">NX</a> &nbsp; </td>"
                           + "<td nowrap bgcolor=lightblue><p><font color=white size=2 face='verdana,arial,helvetica,sans-serif'> &nbsp; <a href=\"javascript:set('S')\">Sun Ray</a> &nbsp; </td>"
                           + "<td nowrap bgcolor=lightblue><p><font color=white size=2 face='verdana,arial,helvetica,sans-serif'> &nbsp; <a href=\"javascript:set('B')\">Backup</a> &nbsp; </td>"
                           + "<td nowrap bgcolor=lightblue><p><font color=white size=2 face='verdana,arial,helvetica,sans-serif'> &nbsp; <a href=\"javascript:set('W')\">Network</a> &nbsp; </td>"
                           + "<td nowrap bgcolor=lightblue><p><font color=white size=2 face='verdana,arial,helvetica,sans-serif'> &nbsp; <a href=\"javascript:set('D')\">DataBase</a> &nbsp; </td>"

                           + "<td width=99%></td></tr></table>");
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {
    out.println(str);
    bytesOut[0] += (str.length() + 2);
  }

}
