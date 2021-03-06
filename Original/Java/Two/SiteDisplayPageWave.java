// =======================================================================================================================================================================================================
// System: ZaraStar Site: Display Page
// Module: SiteDisplayPage.java
// Author: C.K.Harvey
// Copyright (c) 2006-09 Christopher Harvey. All Rights Reserved.
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

public class SiteDisplayPageWave extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  Wiki wiki = new Wiki();
  BlogsUtils blogsUtils = new BlogsUtils();
  AdminControlUtils adminControlUtils = new AdminControlUtils();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="";

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
      p1  = req.getParameter("p1"); // code, 0 if homePage, -ve if serviceCode
      p2  = req.getParameter("p2"); // call from Signon

      if(p1 == null || p1.length() == 0) p1 = "0";
      if(p2 == null || p2.length() == 0) p2 = "F";

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "SiteDisplayPage", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6660, bytesOut[0], 0, "ERR:" + p1 + ":" + p2);
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1, String p2, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesLibraryDir = directoryUtils.getImagesDir(dnm);
    String flashDir         = directoryUtils.getFlashDirectory(dnm);
    String defnsDir         = directoryUtils.getSupportDirs('D');
    String localDefnsDir    = directoryUtils.getLocalOverrideDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

   if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
   {
     {
        String sessionsDir = directoryUtils.getSessionsDir(dnm);
        sid = serverUtils.newSessionID(unm, "A", dnm, sessionsDir, localDefnsDir, defnsDir);
        den = dnm;
        unm = "_" + sid;
        StringBuffer url = req.getRequestURL();
        int x=0;
        if(url.charAt(x) == 'h' || url.charAt(x) == 'H')
          x += 7;
        men="";
        while(url.charAt(x) != '\000' && url.charAt(x) != ',' && url.charAt(x) != '/')
          men += url.charAt(x++);
      }
    }

    create(con, stmt, rs, out, req, p1, unm, sid, uty, men, den, dnm, bnm, imagesLibraryDir, flashDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 6660, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);//req.getHeader("User-Agent"));
    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void create(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String p1, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String imagesLibraryDir, String flashDir,
                      String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    String html = "<script language=\"JavaScript\">";

    html += "function profile(userCode){var p1=sanitise(userCode);";
    html += "this.location.href=\"/central/servlet/ContactsProfileView?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+p1;}";

    html += "function page(code){getHTML('SiteDisplayPageWave','&p1='+code);}";

    html += "function blogs(){getHTML('BlogsBlogRollListWave','');}";

    html += "function edit(code,type){if(type=='0')getHTML('BlogsEditRawWave','&p1='+code);else getHTML('BlogsEditWave','&p1='+code);}";

    html += "</script>";

    html += "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"http://" + men + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">";

    html += "<table cellpadding=0 cellspacing=0 width=100% border=0>";

    html += "<tr><td width=75% valign=top>"; // td one
    getPage(out, p1, html, unm, sid, uty, men, den, dnm, bnm, imagesLibraryDir, flashDir, localDefnsDir, bytesOut);
    scoutln(out, bytesOut, "</td>");
    scoutln(out, bytesOut, "</tr>"); // /tr one
    scoutln(out, bytesOut, "</table>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {
    out.println(str);
    bytesOut[0] += (str.length() + 2);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void getPage(PrintWriter out, String code, String html, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String imageLibraryDir, String flashDir, String localDefnsDir, int[] bytesOut) throws Exception
  {
    String[] owner           = new String[1];
    String[] date            = new String[1];
    String[] title           = new String[1];
    String[] type            = new String[1];
    String[] image           = new String[1];
    String[] topicName       = new String[1];
    String[] text            = new String[1];
    String[] published       = new String[1];
    String[] isAMenuItem     = new String[1];
    String[] isASpecial      = new String[1];
    String[] shortTitle      = new String[1];
    String[] displayTheTitle = new String[1];
    String[] serviceCode     = new String[1];
    String[] isTheHomePage   = new String[1];
    String[] isSharable      = new String[1];
    String[] codeForEdit     = new String[1];

    if(code.equals("0"))
    {
      if(! blogsUtils.getHomePage(owner, date, title, type, image, topicName, text, published, isAMenuItem, isASpecial, shortTitle, displayTheTitle, isSharable, codeForEdit, dnm))
      {
        scoutln(out, bytesOut, "6660\001" + shortTitle[0] + "\001" + shortTitle[0] + "\001javascript:getHTML('SiteDisplayPageWave','&p1=" + code + "')\001\001\001\001\003");
        scoutln(out, bytesOut, html);
        scoutln(out, bytesOut, "<table border=0 id='page' width=100% style=\"{padding:10px;}\">");
        scoutln(out, bytesOut, "<tr><td><p><b><br><br><br><br><font size=5 color=red>Home Page Not Found</tr>");
        scoutln(out, bytesOut, "</table>");
        return;
      }
    }
    else
    if(code.charAt(0) == '-')
    {
      if(! blogsUtils.getGivenServiceCode(code.substring(1), owner, date, title, type, image, topicName, text, published, isAMenuItem, isTheHomePage, isASpecial, shortTitle, displayTheTitle, serviceCode, isSharable, dnm))
      {
        scoutln(out, bytesOut, "6660\001" + shortTitle[0] + "\001" + shortTitle[0] + "\001javascript:getHTML('SiteDisplayPageWave','&p1=" + code + "')\001\001\001\001\003");
        scoutln(out, bytesOut, html);
        scoutln(out, bytesOut, "<table border=0 id='page' width=100% style=\"{padding:10px;}\">");
        scoutln(out, bytesOut, "<tr><td><p><b><br><br><br><br><font size=5 color=red>Page Not Found</tr>");
        scoutln(out, bytesOut, "</table>");
        return;
      }

      codeForEdit[0] = serviceCode[0]; // serviceCode returned is code from above call
    }
    else
    {
      if(! blogsUtils.getGivenCode(code, owner, date, title, type, image, topicName, text, published, isAMenuItem, isTheHomePage, isASpecial, shortTitle, displayTheTitle, serviceCode, isSharable, dnm))
      {
        scoutln(out, bytesOut, "6660\001" + shortTitle[0] + "\001" + shortTitle[0] + "\001javascript:getHTML('SiteDisplayPageWave','&p1=" + code + "')\001\001\001\001\003");
        scoutln(out, bytesOut, html);
        scoutln(out, bytesOut, "<table border=0 id='page' width=100% style=\"{padding:10px;}\">");
        scoutln(out, bytesOut, "<tr><td><p><b><br><br><br><br><font size=5 color=red>Page Not Found</tr>");
        scoutln(out, bytesOut, "</table>");
        return;
      }

      codeForEdit[0] = code;
    }

    scoutln(out, bytesOut, "6660\001" + shortTitle[0] + "\001" + shortTitle[0] + "\001javascript:getHTML('SiteDisplayPageWave','&p1=" + code + "')\001\001\001\001\003");
    scoutln(out, bytesOut, html);

    text[0] = wiki.convertLinksW(text[0], unm, sid, uty, men, den, dnm, bnm, imageLibraryDir, flashDir, localDefnsDir);

    scoutln(out, bytesOut, "<table border=0 id='page' width=100% style=\"{padding:10px;}\">");

    int numCols = 1;

    if(displayTheTitle[0].equals("Y"))
      scoutln(out, bytesOut, "<tr><td><p><b><font size=3>" + title[0] + "</td></tr><tr><td>&nbsp;</td></tr>");

    if(type[0].equals("2"))
    {
      scoutln(out, bytesOut, "<tr><td valign=top><img src=\"" + imageLibraryDir + image[0] + "\" border=0></td><td valign=top><p>" + text[0] + "</td></tr>");
      ++numCols;
    }
    else
    if(type[0].equals("3"))
    {
      scoutln(out, bytesOut, "<tr><td valign=top><p>" + text[0] + "</td><td valign=top><img src=\"" + imageLibraryDir + image[0] + "\" border=0></td></tr>");
      ++numCols;
    }
    else scoutln(out, bytesOut, "<tr><td valign=top><p>" + text[0] + "</td></tr>"); // maybe type 1, maybe raw

    boolean canEdit = false;
    if(uty.equals("I"))
    {
      if(isSharable[0].equals("Y"))
        canEdit = true;
      else
      if(unm.equals(owner[0]))
        canEdit = true;
    }

    if(canEdit)
    {
      scoutln(out, bytesOut, "<tr><td align=right colspan='" + numCols + "'><p><i><font size=1><a href=\"javascript:edit('" + codeForEdit[0] + "','" + type[0] + "')\">" + owner[0] + "</a>, " + generalUtils.yymmddExpandGivenSQLFormat(true, date[0])
                           + "</td></tr>");
    }
    else scoutln(out, bytesOut, "<tr><td align=right colspan='" + numCols + "'><p><i><font size=1>" + owner[0] + ", " + generalUtils.yymmddExpandGivenSQLFormat(true, date[0]) + "</td></tr>");

    scoutln(out, bytesOut, "</table>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void getNews(PrintWriter out, String dnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
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
    }

    scoutln(out, bytesOut, "</table></td></tr>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean getSpecialsCount(Connection con, Statement stmt, ResultSet rs) throws Exception
  {
    int rowcount = 0;

    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM stock WHERE Special = 'Y'");

      if(rs.next())
      {
        rowcount = rs.getInt(1);
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

    if(rowcount == 0)
      return false;

    return true;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean getCatalogsCount(Connection con, Statement stmt, ResultSet rs) throws Exception
  {
    int rowcount = 0;

    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM cataloglist");

      if(rs.next())
        rowcount = rs.getInt(1);

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
  private void getSpecials(Connection con, Statement stmt, ResultSet rs, PrintWriter out, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<tr><td nowrap colspan=2><table id='page' width=100% style=\"{border: 2px solid darkgray;padding:10px;}\">");

    try
    {
      stmt = con.createStatement();

      stmt.setMaxRows(15);

      rs = stmt.executeQuery("SELECT Code, Description, Manufacturer FROM stock WHERE Special = 'Y' ORDER BY Description");

      String code, desc, mfr, cssFormat = "";

      while(rs.next())
      {
        code = rs.getString(1);
        desc = rs.getString(2);
        mfr  = rs.getString(3);

        if(desc == null) desc = "";
        if(mfr  == null) mfr  = "";

        if(cssFormat.equals("line1")) cssFormat = "line2"; else cssFormat = "line1";

        scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\"><td><p>" + mfr + ": <a href=\"javascript:item('" + code + "')\">" + desc + "</a></td></tr>");
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

    scoutln(out, bytesOut, "</table></td></tr>");
  }

}
