// =======================================================================================================================================================================================================
// System: ZaraStar Info: Display guide page
// Module: BlogsDisplayBlogGuideDisplay.java
// Author: C.K.Harvey
// Copyright (c) 2002-09 Christopher Harvey. All Rights Reserved.
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

public class BlogsDisplayBlogBuideDisplayWave extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  Wiki wiki = new Wiki();
  BlogsUtils blogsUtils = new BlogsUtils();

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
      p1  = req.getParameter("p1"); // service codes
      p2  = req.getParameter("p2"); // title

      if(p1 == null) p1 = "";
      if(p2 == null) p2 = "";
      
      doIt(out, req, p1, p2, unm, sid, uty, men, den, dnm, bnm, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "BlogsDisplayBlogGuide", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8110, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String p1, String p2, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir       = directoryUtils.getSupportDirs('I');
    String defnsDir        = directoryUtils.getSupportDirs('D');
    String localDefnsDir   = directoryUtils.getLocalOverrideDir(dnm);
    String imageLibraryDir = directoryUtils.getImagesDir(dnm);
    String flashDir        = directoryUtils.getFlashDirectory(dnm);

    String uName = directoryUtils.getMySQLUserName();
    String pWord = directoryUtils.getMySQLPassWord();
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Connection conInfo = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_info?user=" + uName + "&password=" + pWord);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "BlogsDisplayBlogGuide", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8110, bytesOut[0], 0, "SID:" + p1);
      if(conInfo != null) conInfo.close();
      if(con     != null) con.close();
      if(out != null) out.flush();
      return;
    }

    scoutln(out, bytesOut, "8110\001BlogGuide\001" + p2 + "\001javascript:getHTML('BlogsDisplayBlogBuideDisplayWave','&p1=" + p1 + "&p2=" + p2 + "')\001\001Y\001\001\003");

    set(con, conInfo, stmt, rs, req, out, p1, unm, sid, uty, men, den, dnm, bnm, imageLibraryDir, flashDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 8110, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(conInfo != null) conInfo.close();
    if(con     != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Connection conInfo, Statement stmt, ResultSet rs, HttpServletRequest req, PrintWriter out, String services, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                   String imageLibraryDir, String flashDir, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    String cssDirectory = directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir);

    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + cssDirectory + "general.css\">");

    scoutln(out, bytesOut, "<script javascript=\"JavaScript\">");

    scoutln(out, bytesOut, "function editEntry(code,type){if(type=='0')getHTML('BlogsEditRawWave','&p1='+code);else getHTML('BlogsEditWave','&p1='+code);}");

    scoutln(out, bytesOut, "</script>");

    blogsUtils.drawTitleW(con, stmt, rs, req, out, "", "8110", "", false, false, false, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    scoutln(out, bytesOut, "<table id='page' width=100% border=0>");

    Statement stmtInfo = null;
    ResultSet rsInfo   = null;

    String service;
    int x = 0, len = services.length();
    while(x < len)
    {
      service = "";
      while(x < len && services.charAt(x) != ' ' && services.charAt(x) != ',')
        service += services.charAt(x++);

      getForAService(conInfo, stmtInfo, rsInfo, out, service, unm, sid, uty, men, den, dnm, bnm, imageLibraryDir, flashDir, localDefnsDir, bytesOut);

      while(x < len && (services.charAt(x) == ' ' || services.charAt(x) == ','))
        ++x;
    }

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "</table>");

    if(rsInfo   != null) rsInfo.close();
    if(stmtInfo != null) stmtInfo.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String getForAService(Connection conInfo, Statement stmtInfo, ResultSet rsInfo, PrintWriter out, String service, String unm, String sid, String uty, String men, String den, String dnm,
                                String bnm, String imageLibraryDir, String flashDir, String localDefnsDir, int[] bytesOut) throws Exception
  {
    stmtInfo = conInfo.createStatement();

    rsInfo = stmtInfo.executeQuery("SELECT * from blogs WHERE ServiceCode = '" + service + "' AND Published='Y' ORDER BY Date");

    String code="", owner, date="", title, type, image, topicName, text, html = "", t1;

    while(rsInfo.next())
    {
      code      = rsInfo.getString(1);
      owner     = rsInfo.getString(2);
      date      = rsInfo.getString(3);
      title     = rsInfo.getString(4);
      type      = rsInfo.getString(5);
      image     = rsInfo.getString(6);
      topicName = rsInfo.getString(7);
      text      = rsInfo.getString(8);

      if(title == null) title = "";
      if(image == null) image = "";
      if(text  == null) text = "";

      t1 = wiki.convertLinksW(text, unm, sid, uty, men, den, dnm, bnm, imageLibraryDir, flashDir, localDefnsDir);

      scoutln(out, bytesOut, "<tr><td colspan=3><table border=0 width=100%><tr bgcolor='darkgray'><td nowrap height=20><p><b>" + generalUtils.yymmddExpandGivenSQLFormat(true, date) + " - " + topicName + " - " + title + "</td>");
      if(unm.equals(owner))
        scoutln(out, bytesOut, "<td align=right><a href=\"javascript:editEntry('" + code + "','" + type + "')\">Edit</a></td>");
      scoutln(out, bytesOut, "</tr>");

      if(type.equals("2"))
        scoutln(out, bytesOut, "<tr><td colspan=2><table border=0><tr><td valign=top><img src=\"" + imageLibraryDir + image + "\" border=0></td><td><p>" + t1 + "</td></tr></table></td></tr>");
      else
      if(type.equals("3"))
        scoutln(out, bytesOut, "<tr><td colspan=3><table border=0><tr><td><p>" + t1 + "</td><td valign=top><img src=\"" + imageLibraryDir + image + "\" border=0></td></tr></table></td></tr>");
      else scoutln(out, bytesOut, "<tr><td colspan=3><p>" + t1 + "</td></tr>");

      scoutln(out, bytesOut, "</table></td></tr>");
    }

    if(rsInfo   != null) rsInfo.close();
    if(stmtInfo != null) stmtInfo.close();

    return html;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {
    out.println(str);
    bytesOut[0] += str.length() + 2;
  }

}
