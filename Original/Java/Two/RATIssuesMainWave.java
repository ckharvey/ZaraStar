 // =======================================================================================================================================================================================================
// System: ZaraStar RAT: Issues main
// Module: RATIssuesMainWave.java
// Author: C.K.Harvey
// Copyright (c) 2002-09 Christopher Harvey. All Rights Reserved.
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
import java.net.*;
import java.sql.*;

public class RATIssuesMainWave extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  IssuesUtils issuesUtils = new IssuesUtils();
  AdminControlUtils adminControlUtils = new AdminControlUtils();

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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "RATIssuesMain", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 5900, bytesOut[0], 0, "ERR:");
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

    if(! adminControlUtils.notDisabled(con, stmt, rs, 7539))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "RATIssuesMain", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 5900, bytesOut[0], 0, "ACC:");
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "RATIssuesMain", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 5900, bytesOut[0], 0, "SID:");
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    scoutln(out, bytesOut, "5900\001Forum\001Forum Main\001javascript:getHTML('RATIssuesMainWave','')\001\001Y\001\001\003");

    set(con, stmt, rs, out, req, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 5900, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut)
                   throws Exception
  {
    scoutln(out, bytesOut, "<script javascript=\"JavaScript\">");

    scoutln(out, bytesOut, "function cat(name){var p1=sanitise(name);getHTML('RATListWave','&p1='+p1);}");

    scoutln(out, bytesOut, "function fetchIssue(ratCode){var code=sanitise(document.forms[0].fetch.value);getHTML('RATDisplayIssueWave','&p1='+code);}");
    scoutln(out, bytesOut, "function page(code){getHTML('SiteDisplayPageWave','&p1='+code);}");
    scoutln(out, bytesOut, "function edit(code,type){if(type=='0')getHTML('MailZaraViewFromLibrarybw','&p1='+code);else getHTML('BlogsEditWave','&p1='+code);}");

    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    issuesUtils.drawTitleW(con, stmt, rs, req, out, "Issues", "5900", "", true, false, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    scoutln(out, bytesOut, "<form><table id='page' cellspacing=2 cellpadding=0 width=100% border=0>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td width=75% valign=top>"); // td one
    listCats(out, dnm, bytesOut);
    scoutln(out, bytesOut, "</td>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "</table></form>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {
    out.println(str);
    bytesOut[0] += str.length() + 2;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void listCats(PrintWriter out, String dnm, int bytesOut[]) throws Exception
  {
    scoutln(out, bytesOut, "<table id='page' cellspacing=2 cellpadding=4 width=100% border=0");

    Connection con = null;
    Statement stmt = null, stmt2 = null;
    ResultSet rs   = null, rs2   = null;

    try
    {
      String uName = directoryUtils.getMySQLUserName();
      String pWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();

      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_info?user=" + uName + "&password=" + pWord);

      stmt = con.createStatement();

      String q = "SELECT Name, Description FROM ratcat ORDER BY Position";

      rs = stmt.executeQuery(q);

      String name, desc, cssFormat = "";
      int count;

      while(rs.next())
      {
        name = rs.getString(1);
        desc = rs.getString(2);

        if(cssFormat.equals("line2")) cssFormat = "line1"; else cssFormat = "line2";

        count = countIssues(con, stmt2, rs2, name);

        scoutln(out, bytesOut, "<tr id='" + cssFormat + "'><td><p><a href=\"javascript:cat('" + name + "')\">" + name + "</a></td><td align=right><p>" + count);
        if(count == 1)
          scoutln(out, bytesOut, " entry");
        else scoutln(out, bytesOut, " entries");

        scoutln(out, bytesOut, "</td></tr><tr id='" + cssFormat + "'><td colspan=2><p>" + desc + "</td></tr>");
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

    scoutln(out, bytesOut, "</table>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int countIssues(Connection con, Statement stmt, ResultSet rs, String cat) throws Exception
  {
    int rowCount = 0;

    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT COUNT(DISTINCT RATCode) AS rowcount FROM rat WHERE Category = '" + generalUtils.sanitiseForSQL(cat) + "'");

      if(rs.next())
        rowCount = rs.getInt("rowcount");

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }

    return rowCount;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void getNews(PrintWriter out, String dnm, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<tr><td nowrap colspan=2><table id='page' width=100% style=\"{border: 2px solid darkgray;padding:10px;}\">");

    Connection conInfo = null;
    Statement stmtInfo = null;
    ResultSet rsInfo = null;

    try
    {
      String uName = directoryUtils.getMySQLUserName();
      String pWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();

      conInfo = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_info?user=" + uName + "&password=" + pWord);

      stmtInfo = conInfo.createStatement();

      stmtInfo.setMaxRows(15);

      rsInfo = stmtInfo.executeQuery("SELECT Code, Date, Title FROM blogs WHERE Published = 'Y' ORDER BY Date DESC, Code DESC");

      String code="", date="", title, cssFormat = "";

      while(rsInfo.next())
      {
        code  = rsInfo.getString(1);
        date  = rsInfo.getString(2);
        title = rsInfo.getString(3);

        if(title == null) title = "";

        if(cssFormat.equals("line1")) cssFormat = "line2"; else cssFormat = "line1";

        scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\"><td><p>" + date + ": <a href=\"javascript:page('" + code + "')\">" + title + "</a></td></tr>");
      }

      if(rsInfo   != null) rsInfo.close();
      if(stmtInfo != null) stmtInfo.close();
      if(conInfo  != null) conInfo.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(rsInfo   != null) rsInfo.close();
      if(stmtInfo != null) stmtInfo.close();
      if(conInfo  != null) conInfo.close();
    }

    scoutln(out, bytesOut, "</table></td></tr>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void getPeople(PrintWriter out, String unm, String uty, String sid, String dnm, String men, String den, String bnm, String localDefnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<tr><td nowrap colspan=2><table border=0 id='page' width=100% style=\"{border: 2px solid darkgray;padding:10px;}\">");

    try
    {
      URL url = new URL("http://" + serverUtils.serverToCall("OFSA", localDefnsDir) + "/central/servlet/CompanyPersonnelOnline?unm=" + unm + "&uty=" + uty + "&dnm=" + dnm + "&den=" + den + "&men=" + men + "&bnm=" + bnm + "&sid=" + sid);

      URLConnection uc = url.openConnection();
      uc.setDoInput(true);
      uc.setUseCaches(false);
      uc.setDefaultUseCaches(false);

      uc.setRequestProperty("Content-Type", "application/octet-stream");

      BufferedReader di = new BufferedReader(new InputStreamReader(uc.getInputStream()));

      String s = di.readLine();
      while(s != null)
      {
        scoutln(out, bytesOut, s);
        s = di.readLine();
      }

      di.close();
    }
    catch(Exception e) { }

    scoutln(out, bytesOut, "</table></td></tr>");
  }

}
