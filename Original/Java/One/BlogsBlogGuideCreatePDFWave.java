// =======================================================================================================================================================================================================
// System: ZaraStar Utils: Create PDF BlogGuide
// Module: BlogsBlogGuideCreatePDFWave.java
// Author: C.K.Harvey
// Copyright (c) 1999-2009 Christopher Harvey. All Rights Reserved.
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

public class BlogsBlogGuideCreatePDFWave extends HttpServlet
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "BlogsBlogGuideCreatePDF", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8113, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreenW(out, req, 12, unm, sid, uty, men, den, dnm, bnm, "BlogsBlogGuideCreatePDF", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8113, bytesOut[0], 0, "SID:");
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    scoutln(out, bytesOut, "8113\001Blogs\001Blog Guide PDF\001javascript:getHTML('BlogsBlogGuideCreatePDFWave','')\001\001Y\001\001\003");

    set(con, stmt, rs, out, unm, sid, uty, men, den, dnm, bnm, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 8113, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    scoutln(out, bytesOut, "function pdf(name,type,version){");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/BlogsBlogGuideCreatePDFProcess?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p3=\" + version + \"&p2=\" + type + \"&p1=\"+escape(name);}");

    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    pageFrameUtils.drawTitleW(out, false, false, "", "", "", "", "", "", "PDF BlogGuide", "8113", unm, sid, uty, men, den, dnm, bnm, bytesOut);

    scoutln(out, bytesOut, "<form><table id=\"page\" width=100% border=0>");

    scoutln(out, bytesOut, "<tr><td nowrap colspan=3><h1>Select a Guide</td></tr>");

    scoutln(out, bytesOut, "<tr id='pageColumn'><td><p>Name</td><td><p>Version</td><td><p>Format</td></tr>");

    listBlogGuides(out, dnm, bytesOut);

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "</table></form>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void listBlogGuides(PrintWriter out, String dnm, int[] bytesOut) throws Exception
  {
    Connection conInfo  = null;
    Statement  stmtInfo = null, stmtInfo2 = null;
    ResultSet  rsInfo   = null, rsInfo2   = null;

    try
    {
      String uName = directoryUtils.getMySQLUserName();
      String pWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();

      conInfo = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_info?user=" + uName + "&password=" + pWord);

      stmtInfo = conInfo.createStatement();

      rsInfo = stmtInfo.executeQuery("SELECT DISTINCT Name FROM blogguide ORDER BY Name");

      String name;
      String[] cssFormat = new String[1];  cssFormat[0] = "";

      while(rsInfo.next())
      {
        name = rsInfo.getString(1);

        if(cssFormat[0].equals("line2")) cssFormat[0] = "line1"; else cssFormat[0] = "line2";

        if(hasAbridged(conInfo, stmtInfo2, rsInfo2, name))
        {
          scoutln(out, bytesOut, "<tr id=\"" + cssFormat[0] + "\"><td><p>" + name + "</td><td><p>Abridged</td><td><p>A4</td><td><a href=\"javascript:pdf('" + name + "','4','A')\">Download</a></tr>");
          scoutln(out, bytesOut, "<tr id=\"" + cssFormat[0] + "\"><td><p>" + name + "</td><td><p>Abridged</td><td><p>Letter</td><td><a href=\"javascript:pdf('" + name + "','L','A')\">Download</a></tr>");
        }

        scoutln(out, bytesOut, "<tr id=\"" + cssFormat[0] + "\"><td><p>" + name + "</td><td><p>Full</td><td><p>A4</td><td><a href=\"javascript:pdf('" + name + "','4','F')\">Download</a></tr>");
        scoutln(out, bytesOut, "<tr id=\"" + cssFormat[0] + "\"><td><p>" + name + "</td><td><p>Full</td><td><p>Letter</td><td><a href=\"javascript:pdf('" + name + "','L','F')\">Download</a></tr>");
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
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean hasAbridged(Connection conInfo, Statement stmtInfo, ResultSet rsInfo, String name) throws Exception
  {
    int rowCount = 0;

    try
    {
      stmtInfo = conInfo.createStatement();

      rsInfo = stmtInfo.executeQuery("SELECT COUNT(*) AS rowcount FROM blogguide WHERE Name = '" + generalUtils.sanitiseForSQL(name) + "' AND Abridged = 'Y'");

      if(rsInfo.next())
        rowCount = rsInfo.getInt(1);

      if(rsInfo   != null) rsInfo.close();
      if(stmtInfo != null) stmtInfo.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(rsInfo   != null) rsInfo.close();
      if(stmtInfo != null) stmtInfo.close();
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
