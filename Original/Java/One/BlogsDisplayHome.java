// =======================================================================================================================================================================================================
// System: ZaraStar Blogs: Display page for home page
// Module: BlogsDisplayHome.java
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

public class BlogsDisplayHome extends HttpServlet
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

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", dateRequired="", topic="", codeRequired="", operation="", firstCodeOnPage="", lastCodeOnPage="", firstDateOnPage="", lastDateOnPage="";

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

      dateRequired    = req.getParameter("date");
      topic           = req.getParameter("topic");
      codeRequired    = req.getParameter("code"); // blogCode
      operation       = req.getParameter("operation");
      lastDateOnPage  = req.getParameter("lastDateOnPage");  // from previously-displayed page
      firstDateOnPage = req.getParameter("firstDateOnPage"); //
      lastCodeOnPage  = req.getParameter("lastCodeOnPage");  //
      firstCodeOnPage = req.getParameter("firstCodeOnPage"); //

      if(topic           == null) topic           = "";
      if(operation       == null) operation       = "L"; // latest
      if(firstCodeOnPage == null) firstCodeOnPage = "";
      if(lastCodeOnPage  == null) lastCodeOnPage  = "";
      if(firstDateOnPage == null) firstDateOnPage = "1970-01-01";
      if(lastDateOnPage  == null) lastDateOnPage  = "2099-12-31";

      doIt(out, req, dateRequired, topic, codeRequired, operation, firstCodeOnPage, lastCodeOnPage, firstDateOnPage, lastDateOnPage, unm, sid, uty, men, den, dnm, bnm, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "BlogsDisplayHome", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8104, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String dateRequired, String topic, String codeRequired, String operation, String firstCodeOnPage, String lastCodeOnPage, String firstDateOnPage, String lastDateOnPage, String unm,
                    String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
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
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "BlogsDisplayHome", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8104, bytesOut[0], 0, "SID:");
      if(conInfo != null) conInfo.close();
      if(con     != null) con.close();
      if(out != null) out.flush();
      return;
    }

    set(con, conInfo, stmt, rs, req, out, dateRequired, topic, codeRequired, operation, firstCodeOnPage, lastCodeOnPage, firstDateOnPage, lastDateOnPage, unm, sid, uty, men, den, dnm, bnm, imageLibraryDir, flashDir, localDefnsDir, defnsDir,
        bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 8104, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(conInfo != null) conInfo.close();
    if(con     != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Connection conInfo, Statement stmt, ResultSet rs, HttpServletRequest req, PrintWriter out, String dateRequired, String topic, String codeRequired, String operation, String firstCodeOnPage, String lastCodeOnPage,
                   String firstDateOnPage, String lastDateOnPage, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String imageLibraryDir, String flashDir, String localDefnsDir, String defnsDir, int[] bytesOut)
                   throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Home Page</title>");

    String cssDirectory = directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir);

    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + cssDirectory + "general.css\">");

    scoutln(out, bytesOut, "<script javascript=\"JavaScript\">");

    boolean canCreate = authenticationUtils.verifyAccess(con, stmt, rs, req, 8102, unm, uty, dnm, localDefnsDir, defnsDir);

    if(canCreate)
    {
      scoutln(out, bytesOut, "function editEntry(code,type){if(type=='0')");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/BlogsEditRaw?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+code;"); // /raw HTML
      scoutln(out, bytesOut, "else this.location.href=\"/central/servlet/BlogsEdit?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+code;}");
    }

    scoutln(out, bytesOut, "function page(date,topic,code,op,lastC,firstC,lastD,firstD){");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/BlogsDisplayHome?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                         + "&date=\"+date+\"&topic=\"+sanitise(topic)+\"&code=\"+code+\"&operation=\"+op+\"" + "&lastCodeOnPage=\"+lastC+\"&firstCodeOnPage=\"+firstC+\"&lastDateOnPage=\"+lastD+\"&firstDateOnPage=\"+firstD;}");

    scoutln(out, bytesOut, "function sanitise(code){");
    scoutln(out, bytesOut, "var code2='';var x;var len=code.length;");
    scoutln(out, bytesOut, "for(x=0;x<len;++x)");
    scoutln(out, bytesOut, "if(code.charAt(x)=='#')code2+='%23';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='\"')code2+='%22';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='&')code2+='%26';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='%')code2+='%25';");
    scoutln(out, bytesOut, "else if(code.charAt(x)==' ')code2+='%20';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='?')code2+='%3F';");
    scoutln(out, bytesOut, "else code2+=code.charAt(x);");
    scoutln(out, bytesOut, "return code2};");

    scoutln(out, bytesOut, "</script>");

    int[] hmenuCount = new int[1];

    blogsUtils.outputPageFrame(con, stmt, rs, out, req, "BlogsDisplayHome", "", "", "8104", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    blogsUtils.drawTitle(con, stmt, rs, req, out, "", "", "", false, false, false, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<table id='page' width=100% border=0>");

    Statement stmtInfo = null, stmtInfo2 = null;
    ResultSet rsInfo   = null, rsInfo2   = null;

    String[] newFirstCodeOnPage = new String[1];
    String[] newLastCodeOnPage  = new String[1];
    String[] newFirstDateOnPage = new String[1];
    String[] newLastDateOnPage  = new String[1];

    String html = getData(conInfo, stmtInfo, stmtInfo2, rsInfo, rsInfo2, canCreate, operation, codeRequired, dateRequired, topic, lastCodeOnPage, firstCodeOnPage, lastDateOnPage, firstDateOnPage, unm, sid, uty, men, den, dnm, bnm,
                          imageLibraryDir, flashDir, newLastCodeOnPage, newFirstCodeOnPage, newLastDateOnPage, newFirstDateOnPage, localDefnsDir);

    int[]    numEntriesInFile = new int[1];
    String[] firstCodeInFile  = new String[1];
    String[] lastCodeInFile   = new String[1];

    getStats(conInfo, stmtInfo, rsInfo, topic, numEntriesInFile, firstCodeInFile, lastCodeInFile);

    if(firstCodeOnPage.length() == 0) firstCodeOnPage = firstCodeInFile[0];
    if(lastCodeOnPage.length()  == 0) lastCodeOnPage  = lastCodeInFile[0];

    scoutln(out, bytesOut, "<tr><td colspan=3><p><b>Topics:</b> " + topicsList(conInfo, stmtInfo, rsInfo) + "</td></tr>");

    if(numEntriesInFile[0] > 10)
    {
      scoutln(out, bytesOut, "<tr><td colspan=3 nowrap><p><b>Entries:</b> ");

      if(! newFirstCodeOnPage[0].equals(firstCodeInFile[0]))
      {
        scoutln(out, bytesOut, "<a href=\"javascript:page('','','','F','','','','')\">Earliest</a> &nbsp;&nbsp; | &nbsp;&nbsp;");
        scoutln(out, bytesOut, "<a href=\"javascript:page('','','','P','','" + newFirstCodeOnPage[0] + "','','" + newFirstDateOnPage[0] + "')\">Previous</a>");
        if(! newLastCodeOnPage[0].equals(lastCodeInFile[0]))
          scoutln(out, bytesOut, " &nbsp;&nbsp; | &nbsp;&nbsp;");
      }

      if(! newLastCodeOnPage[0].equals(lastCodeInFile[0]))
      {
        scoutln(out, bytesOut, "<a href=\"javascript:page('','','','N','" + newLastCodeOnPage[0] + "','','" + newLastDateOnPage[0] + "','')\">Next</a> &nbsp;&nbsp; | &nbsp;&nbsp;");
        scoutln(out, bytesOut, "<a href=\"javascript:page('','','','L','','','','')\">Latest</a>");
      }

      scoutln(out, bytesOut, "</td></tr>");
    }

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, html);

    scoutln(out, bytesOut, "</table>");

    if(rsInfo   != null) rsInfo.close();
    if(stmtInfo != null) stmtInfo.close();

    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir));
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String topicsList(Connection con, Statement stmt, ResultSet rs) throws Exception
  {
    String topicNames = "";

    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT DISTINCT TopicName FROM blogs ORDER BY TopicName");

      String topicName;

      while(rs.next())
      {
        topicName = rs.getString(1);

        if(topicNames.length() > 0)
          topicNames += "&nbsp;&nbsp; | &nbsp;&nbsp;";
        topicNames += ("<a href=\"javascript:page('','" + generalUtils.sanitise(topicName) + "','','L','','','','')\">" + topicName + "</a>");
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

    return topicNames;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String getData(Connection conInfo, Statement stmtInfo, Statement stmtInfo2, ResultSet rsInfo, ResultSet rsInfo2, boolean canCreate, String operation, String codeRequired, String dateRequired, String topic, String lastCodeOnPage,
                         String firstCodeOnPage, String lastDateOnPage, String firstDateOnPage, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String imageLibraryDir, String flashDir,
                         String[] newLastCodeOnPage, String[] newFirstCodeOnPage, String[] newLastDateOnPage, String[] newFirstDateOnPage, String localDefnsDir) throws Exception
  {
    String s = "";
    if(topic.length() > 0)
    {
      if(operation.charAt(0) == 'F' || operation.charAt(0) == 'L')
        s += " WHERE";
      s += " TopicName = '" + generalUtils.sanitiseForSQL(topic) + "' ";
      if(operation.charAt(0) != 'F' && operation.charAt(0) != 'L')
        s += " AND ";
    }

    stmtInfo = conInfo.createStatement();
    stmtInfo.setMaxRows(10);

    switch(operation.charAt(0))
    {
      case 'F' : rsInfo = stmtInfo.executeQuery("SELECT * from blogs " + s + " ORDER BY Date, Code");
                 break;
      case 'L' : rsInfo = stmtInfo.executeQuery("SELECT * from blogs " + s + " ORDER BY Date DESC, Code DESC");
                 break;
      case 'N' : rsInfo = stmtInfo.executeQuery("SELECT * from blogs WHERE " + s + " Date >= {d '" + lastDateOnPage + "'} AND Code > '" + lastCodeOnPage + "' ORDER BY Date, Code");
                 break;
      case 'P' : rsInfo = stmtInfo.executeQuery("SELECT * from blogs WHERE " + s + " Date <= {d '" + firstDateOnPage + "'} AND Code < '" + firstCodeOnPage + "' ORDER BY Code DESC");
                 break;
      case 'C' : rsInfo = stmtInfo.executeQuery("SELECT * from blogs WHERE " + s + " Code = '" + codeRequired + "'");
                 break;
      case 'D' : rsInfo = stmtInfo.executeQuery("SELECT * from blogs WHERE " + s + " Date = {d '" + dateRequired + "'}");
                 break;
    }

    String code="", owner, date="", title, type, image, topicName, text, published, html = "";
    boolean first = true;

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
      published = rsInfo.getString(9);

      if(title == null) title = "";
      if(image == null) image = "";
      if(text  == null) text = "";
      if(published == null) published = "N";

      if(published.equals("Y") || (owner.equals(unm)))
      {
        if(operation.equals("L") || operation.equals("P"))
        {
          newFirstCodeOnPage[0] = code; // read each one but the last rec read will be the first on page
          newFirstDateOnPage[0] = date;

          if(first)
          {
            newLastCodeOnPage[0] = code;
            newLastDateOnPage[0] = date;
          }
        }
        else
        {
          if(first)
          {
            newFirstCodeOnPage[0] = code;
            newFirstDateOnPage[0] = date;
          }
        }

        if(first) first = false;

        text = wiki.convertLinks(text, unm, sid, uty, men, den, dnm, bnm, imageLibraryDir, flashDir, localDefnsDir);

        if(! operation.equals("L") && ! operation.equals("P"))
        {
          String h = "<tr><td colspan=3><table border=0 width=100%><tr bgcolor='darkgray'><td nowrap height=20><p><b>" + generalUtils.yymmddExpandGivenSQLFormat(true, date) + " - " + topicName + " - " + title + "</td>";

          if(canCreate)
          {
            if(unm.equals(owner) || isPublicTopic(conInfo, stmtInfo2, rsInfo2, topicName))
              h += "<td align=right><a href=\"javascript:editEntry('" + code + "','" + type + "')\">Edit</a></td>";
          }

          h += "</tr>\n";

          if(type.equals("2"))
            h += "<tr><td colspan=2><table border=0><tr><td valign=top><img src=\"" + imageLibraryDir + image + "\" border=0></td><td><p>" + text + "</td></tr></table></td></tr>\n";
          else
          if(type.equals("3"))
            h += "<tr><td colspan=3><table border=0><tr><td><p>" + text + "</td><td valign=top><img src=\"" + imageLibraryDir + image + "\" border=0></td></tr></table></td></tr>";
          else h += "<tr><td colspan=3><p>" + text + "</td></tr>\n";

          h += "</table>\n";
          h += "</td></tr>";

          html = h + html;
        }
        else
        {
          html += "<tr><td colspan=3>";
          html += "<table border=0 width=100%><tr bgcolor='darkgray'><td nowrap height=20><p><b>" + generalUtils.yymmddExpandGivenSQLFormat(true, date) + " - " + topicName + " - " + title + "</td>\n";

          if(canCreate)
          {
            if(unm.equals(owner) || isPublicTopic(conInfo, stmtInfo2, rsInfo2, topicName))
              html += "<td align=right><a href=\"javascript:editEntry('" + code + "','" + type + "')\">Edit</a></td>";
          }

          html += "</tr>\n";

          if(type.equals("2"))
            html += "<tr><td colspan=2><table border=0><tr><td valign=top><img src=\"" + imageLibraryDir + image + "\" border=0></td><td><p>" + text + "</td></tr></table></td></tr>\n";
          else
          if(type.equals("3"))
            html += "<tr><td colspan=3><table border=0><tr><td><p>" + text + "</td><td valign=top><img src=\"" + imageLibraryDir + image + "\" border=0></td></tr></table></td></tr>\n";
          else html += "<tr><td colspan=3><p>" + text + "</td></tr>";

          html += "</table>\n";
          html += "</td></tr>";
        }
      }
    }

    if(rsInfo   != null) rsInfo.close();
    if(stmtInfo != null) stmtInfo.close();

    if(! operation.equals("L") && ! operation.equals("P"))
    {
      newLastCodeOnPage[0] = code;
      newLastDateOnPage[0] = date;
    }

    return html;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void getStats(Connection conInfo, Statement stmtInfo, ResultSet rsInfo, String topic, int[] numEntries, String[] firstCode, String[] lastCode) throws Exception
  {
    String s = "";
    if(topic.length() > 0)
      s = " WHERE TopicName = '" + generalUtils.sanitiseForSQL(topic) + "'";

    stmtInfo = conInfo.createStatement();
    stmtInfo.setMaxRows(1);

    rsInfo = stmtInfo.executeQuery("SELECT Code from blogs " + s + " ORDER BY Date, Code");

    if(rsInfo.next())
      firstCode[0] = rsInfo.getString(1);
    else firstCode[0] = "";

    if(rsInfo   != null) rsInfo.close();
    if(stmtInfo != null) stmtInfo.close();

    stmtInfo = conInfo.createStatement();
    stmtInfo.setMaxRows(1);

    rsInfo = stmtInfo.executeQuery("SELECT Code from blogs " + s + " ORDER BY Date DESC, Code DESC");

    if(rsInfo.next())
      lastCode[0] = rsInfo.getString(1);
    else lastCode[0] = "";

    if(rsInfo   != null) rsInfo.close();
    if(stmtInfo != null) stmtInfo.close();

    stmtInfo = conInfo.createStatement();

    int rowcount = 0;
    rsInfo = stmtInfo.executeQuery("SELECT COUNT(Code) AS rowcount from blogs " + s + "");

    if(rsInfo.next())
      rowcount = rsInfo.getInt(1);

    numEntries[0] = rowcount;

    if(rsInfo   != null) rsInfo.close();
    if(stmtInfo != null) stmtInfo.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean isPublicTopic(Connection conInfo, Statement stmtInfo, ResultSet rsInfo, String topicName) throws Exception
  {
    String isPublicTopic = "N";

    try
    {
      stmtInfo = conInfo.createStatement();

      rsInfo = stmtInfo.executeQuery("SELECT PublicTopic FROM blogtopics WHERE TopicName = '" + generalUtils.sanitiseForSQL(topicName) + "'");

      if(rsInfo.next()) // just-in-case
        isPublicTopic = rsInfo.getString(1);

      if(rsInfo   != null) rsInfo.close();
      if(stmtInfo != null) stmtInfo.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(rsInfo   != null) rsInfo.close();
      if(stmtInfo != null) stmtInfo.close();
    }

    if(isPublicTopic.equals("Y"))
      return true;
    return false;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {
    out.println(str);
    bytesOut[0] += str.length() + 2;
  }

}
