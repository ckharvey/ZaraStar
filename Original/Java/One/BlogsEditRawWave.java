// ==========================================================================================================================================================================================================
// System: ZaraStar Blog: Edit page - raw HTML
// Module: BlogsEditRaw.java
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

public class BlogsEditRawWave extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  BlogsUtils blogsUtils = new BlogsUtils();

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
      sid = req.getParameter("sid");
      uty = req.getParameter("uty");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");
      p1  = req.getParameter("p1");

      if(p1 == null) p1 = "";

      doIt(out, req, p1, unm, sid, uty, men, den, dnm, bnm, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "BlogsEditRaw", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8102, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush();
    }
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String p1, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 8102, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreenW(out, req, 13, unm, sid, uty, men, den, dnm, bnm, "BlogsEditRaw", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8102, bytesOut[0], 0, "ACC:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreenW(out, req, 12, unm, sid, uty, men, den, dnm, bnm, "BlogsEditRaw", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8102, bytesOut[0], 0, "SID:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    scoutln(out, bytesOut, "8102\001Blogs\001" + p1 + "\001javascript:getHTML('BlogsEditRawWave','" + p1 + "')\001\001\001\001\003");

    set(con, stmt, rs, out, req, p1, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 8102, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String p1, String unm, String sid, String uty,  String men, String den, String dnm, String bnm, String localDefnsDir,
                   String defnsDir, int[] bytesOut) throws Exception
  {
    String cssDirectory = directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir);

    scoutln(out, bytesOut, "<script javascript=\"JavaScript\">");

    scoutln(out, bytesOut, "function topics(){getHTML('BlogsTopicDefinitionWave','');}");

    scoutln(out, bytesOut, "function lib(){getHTML('ImagesMaintainDirectoriesWave','');}");

    scoutln(out, bytesOut, "function today(){var d=new Date();var t=d.getDate()+'.'+(d.getMonth() + 1)+'.'+d.getFullYear();document.forms[0].date.value=t;}");

    scoutln(out, bytesOut, "</script>");

    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + cssDirectory + "general.css\">");

    scoutln(out, bytesOut, "<form name='form8103' id='form8103' enctype='application/x-www-form-urlencoded' method='POST'>");//////////

    if(p1.length() == 0) // new page
      blogsUtils.drawTitleW(con, stmt, rs, req, out, "Create Blog Entry", "8102", p1, false, true, true, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);
    else blogsUtils.drawTitleW(con, stmt, rs, req, out, "Edit Blog Entry", "8102", p1, false, true, true, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    scoutln(out, bytesOut, "<table id=\"page\" cellspacing=2 cellpadding=0 width=100%>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    String[] code            = new String[1];
    String[] owner           = new String[1];
    String[] date            = new String[1];
    String[] title           = new String[1];
    String[] type            = new String[1];
    String[] image           = new String[1];
    String[] topicName       = new String[1];
    String[] text            = new String[1];
    String[] published       = new String[1];
    String[] isAMenuItem     = new String[1];
    String[] isTheHomePage   = new String[1];
    String[] isASpecial      = new String[1];
    String[] shortTitle      = new String[1];
    String[] displayTheTitle = new String[1];
    String[] serviceCode     = new String[1];
    String[] isSharable      = new String[1];

    if(p1.length() == 0) // new page
    {
      date[0] = generalUtils.today(localDefnsDir, defnsDir);
      code[0] = title[0] = text[0] = image[0] = topicName[0] = shortTitle[0] = serviceCode[0] = "";
      type[0] = "0"; // raw HTML
      isAMenuItem[0] = isTheHomePage[0] = isASpecial[0] = displayTheTitle[0] = isSharable[0] = "N";
      owner[0] = unm;
      published[0] = "Y";
    }
    else
    {
      code[0] = p1;
      blogsUtils.getGivenCode(code[0], owner, date, title, type, image, topicName, text, published, isAMenuItem, isTheHomePage, isASpecial, shortTitle, displayTheTitle, serviceCode, isSharable, dnm);
      date[0] = generalUtils.convertFromYYYYMMDD(date[0]);
    }

    scoutln(out, bytesOut, "<input type=hidden name=callingServlet value=\"BlogsEditRawWave\">");
    scoutln(out, bytesOut, "<input type=hidden name=code value=\"" + code[0] + "\">");
    scoutln(out, bytesOut, "<input type=hidden name=owner value=\"" + owner[0] + "\">");
    scoutln(out, bytesOut, "<input type=hidden name=type value=\"" + type[0] + "\">");
    scoutln(out, bytesOut, "<tr><td nowrap><p>Entry Code:</p></td><td><p>" + code[0] + "</p></td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap><p>Service Code:</p></td><td colspan=3><p><input type=text size=20 maxlength=20 name=serviceCode value=\"" + serviceCode[0] + "\"></p></td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td><p>Title:</p></td><td colspan=3><p><input type=text size=60 maxlength=100 name=title value=\"" + title[0] + "\">");
    scoutln(out, bytesOut, "&nbsp;&nbsp;Short Title: <input type=text size=20 maxlength=20 name=shortTitle value=\"" + shortTitle[0] + "\"></p></td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td><p>Date:</p></td><td colspan=3><p><input type=text size=10 maxlength=10 name=date value=\"" + date[0] + "\">");
    scoutln(out, bytesOut, "&nbsp;&nbsp;&nbsp;<a href=\"javascript:today()\">Set to Today</a>");
    scoutln(out, bytesOut, "</td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scout(out, bytesOut, "<tr><td><p>Topic:</p></td><td colspan=3>");
    buildTopicsList(out, topicName[0], unm, dnm, bytesOut);
    scoutln(out, bytesOut, "&nbsp;&nbsp;&nbsp;<a href=\"javascript:topics()\">Edit Topics</a>");
    scoutln(out, bytesOut, "</td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scout(out, bytesOut, "<tr><td><p>Options:</p></td><td nowrap colspan=3><p>");
    scout(out, bytesOut, "<input type=checkbox name=published");
    if(published[0].equals("Y"))
      scout(out , bytesOut, " checked");
    scout(out, bytesOut, "> Published &nbsp; &nbsp; ");
    scout(out, bytesOut, "<input type=checkbox name=isAMenuItem");
    if(isAMenuItem[0].equals("Y"))
      scout(out , bytesOut, " checked");
    scout(out, bytesOut, "> Menu Item &nbsp; &nbsp; ");
    scout(out, bytesOut, "<input type=checkbox name=isTheHomePage");
    if(isTheHomePage[0].equals("Y"))
      scout(out , bytesOut, " checked");
    scoutln(out, bytesOut, "> Home Page &nbsp; &nbsp; ");
    scout(out, bytesOut, "<input type=checkbox name=isASpecial");
    if(isASpecial[0].equals("Y"))
      scout(out , bytesOut, " checked");
    scout(out, bytesOut, "> Special &nbsp; &nbsp; ");
    scout(out, bytesOut, "<input type=checkbox name=displayTheTitle");
    if(displayTheTitle[0].equals("Y"))
      scout(out , bytesOut, " checked");
    scout(out, bytesOut, "> Display Title &nbsp; &nbsp; ");
    scout(out, bytesOut, "<input type=checkbox name=isSharable");
    if(isSharable[0].equals("Y"))
      scout(out , bytesOut, " checked");
    scout(out, bytesOut, "> Sharable");

    scoutln(out, bytesOut, "</p></td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td colspan=4><textarea id=\"text1\" name=\"text1\" cols=\"100\" rows=\"20\">" + text[0] + "</textarea></td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "</table></form>");
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void buildTopicsList(PrintWriter out, String currentTopic, String unm, String dnm, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<select name='topic'>");

    Connection con = null;
    Statement stmt = null;
    ResultSet rs   = null;

    try
    {
      String uName = directoryUtils.getMySQLUserName();
      String pWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();

      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_info?user=" + uName + "&password=" + pWord);

      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT TopicName FROM blogtopics WHERE Owner = '" + unm + "' OR PublicTopic = 'Y' ORDER BY TopicName");

      String topicName;

      while(rs.next())
      {
        topicName = rs.getString(1);
        scout(out, bytesOut, "<option value=\"" + topicName + "\"");
        if(currentTopic.equals(topicName))
          scout(out, bytesOut, " selected");
        scoutln(out, bytesOut, ">" + topicName);
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

    scoutln(out, bytesOut, "</select>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scout(PrintWriter out, int bytesOut[], String str) throws Exception
  {
    out.print(str);
    bytesOut[0] += str.length();
  }
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {
    out.println(str);
    bytesOut[0] += str.length() + 2;
  }

}
