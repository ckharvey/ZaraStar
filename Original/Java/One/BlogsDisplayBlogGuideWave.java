// =======================================================================================================================================================================================================
// System: ZaraStar Info: Display BlogGuide
// Module: BlogsDisplayBlogGuide.java
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

public class BlogsDisplayBlogGuideWave extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  BlogGuideUtils blogGuideUtils = new BlogGuideUtils();

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
      uty = req.getParameter("uty");
      sid = req.getParameter("sid");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");
      p1  = req.getParameter("p1"); // name

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "BlogsDisplayBlogGuide", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8110, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
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

    scoutln(out, bytesOut, "8110\001BlogGuide\001" + p1 + "\001javascript:getHTML('BlogsDisplayBlogGuideWave','&p1=" + p1 + "')\001\001Y\001\001\003");

    create(con, stmt, rs, out, req, p1, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 8110, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void create(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String name, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir,
                      String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"http://" + men + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    scoutln(out, bytesOut, "function fetch(svcs,title){getHTML('BlogsDisplayBlogBuideDisplayWave','&p1='+escape(svcs)+'&p2='+escape(title));}");

    boolean isSysAdminOrDBAdmin = false;

    if(serverUtils.isSysAdmin(con, stmt, rs, unm, dnm, sid, uty, localDefnsDir, defnsDir) || serverUtils.isDBAdmin(con, stmt, rs, unm))
    {
      scoutln(out, bytesOut, "function create(title,svcs){var p1=sanitise(title);getHTML('BlogsEditWave','&p2='+p1+'&p3=' + escape(svcs));}");

      isSysAdminOrDBAdmin = true;
    }

    scoutln(out, bytesOut, "function pdf(){getHTML('BlogsBlogGuideCreatePDFWave','');}");

    scoutln(out, bytesOut, "</script>");

    int[] hmenuCount = new int[1];
    hmenuCount[0] = 0;

    blogGuideUtils.drawTitleW(con, stmt, rs, req, out, "BlogGuide: " + name, "8110", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<form><table id=\"page\" border=0 width=100%>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap><p><a href=\"javascript:pdf()\">Download</a> a PDF Version</td></tr>");

    scoutln(out, bytesOut, "<tr><td colspan=3><span id=\"msg\"></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr id=\"pageColumn\">");
    scoutln(out, bytesOut, "<td><p>Section</td>");
    scoutln(out, bytesOut, "<td><p>Title</td>");
    scoutln(out, bytesOut, "<td><p>Services</td>");
    scoutln(out, bytesOut, "<td><p>Issues/Remarks</td></tr>");

    list(out, isSysAdminOrDBAdmin, name, dnm, bytesOut);

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=4><hr></td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "</table></form>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void list(PrintWriter out, boolean isSysAdminOrDBAdmin, String name, String dnm, int[] bytesOut) throws Exception
  {
    Connection conInfo = null;
    Statement infoStmt = null, infoStmt2 = null;
    ResultSet infoRs   = null, infoRs2   = null;

    try
    {
      String uName = directoryUtils.getMySQLUserName();
      String pWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();

      conInfo = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_info?user=" + uName + "&password=" + pWord);

      infoStmt = conInfo.createStatement();

      infoRs = infoStmt.executeQuery("SELECT Section FROM blogguide WHERE Name = '" + generalUtils.sanitiseForSQL(name) + "'");

      String section, orderList, entriesList = "";
      int numEntries = 0;

      while(infoRs.next())
      {
        entriesList += (infoRs.getString(1) + "\001");
        ++numEntries;
      }

      if(infoRs   != null) infoRs.close();
      if(infoStmt != null) infoStmt.close();

      orderList = generalUtils.sortDecimalDot(entriesList, numEntries);

      String title, services, remark, cssFormat = "line1", indent, servicesToShow, service;
      int y, len, i;
      boolean first, issuesAlreadyFound;

      for(int x=0;x<numEntries;++x)
      {
        infoStmt = conInfo.createStatement();

        section = getSectionByPosition(orderList, x, entriesList);

        infoRs = infoStmt.executeQuery("SELECT Title, Services, Remark FROM blogguide WHERE Name = '" + generalUtils.sanitiseForSQL(name) + "' AND Section = '" + section + "'");

        if(infoRs.next()) // just-in-case
        {
          title    = infoRs.getString(1);
          services = infoRs.getString(2);
          remark   = infoRs.getString(3);

          if(title    == null) title = "";
          if(services == null) services = "";
          if(remark   == null) remark = "";

          if(cssFormat.equals("line2")) cssFormat = "line1"; else cssFormat = "line2";

          scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\">");

          indent = "";
          for(y=0;y<section.length();++y)
            if(section.charAt(y) == '.')
              indent += "&nbsp;&nbsp;&nbsp;&nbsp;";

          scoutln(out, bytesOut, "<td><p>" + indent + section + "&nbsp;&nbsp;</td>");

          if(! anyBlogsForAService(conInfo, infoStmt2, infoRs2, services))
          {
            if(isSysAdminOrDBAdmin)
              scoutln(out, bytesOut, "<td nowrap>" + indent + "<a href=\"javascript:create('" + generalUtils.sanitise2(title) + "','" + generalUtils.sanitise2(services) + "')\">" + title + "</a> <font color='red'>ToDo</font></td>");
            else scoutln(out, bytesOut, "<td nowrap><p>" + indent+title + "</td>");
          }
          else scoutln(out, bytesOut, "<td nowrap>" + indent + "<a href=\"javascript:fetch('" + generalUtils.sanitise2(services) + "','" + generalUtils.sanitise2(title) + "')\">" + title + "</a></td>");

          // do not show services >= 20000
          first = true;
          len = services.length();
          y = 0;
          servicesToShow = "";
          issuesAlreadyFound = false;

          while(y < len)
          {
            service = "";
            while(y < len && services.charAt(y) != ',')
              service += services.charAt(y++);

            try
            {
              i = generalUtils.strToInt(service);

              {
                if(! first)
                  servicesToShow += ", ";
                else first = false;

                servicesToShow += service;
              }

              if(! issuesAlreadyFound && anyIssuesForAService(conInfo, infoStmt2, infoRs2, service))
              {
                String s = "<a href=\"javascript:getHTML('RATList','&p1=" + service + "')\">Issues</a>";
                if(remark.length() > 0)
                  s += ", ";
                remark = s + remark;
                issuesAlreadyFound = true;
              }
            }
            catch(Exception e) { }

            while(y < len && services.charAt(y) == ',')
              ++y;
          }

          scoutln(out, bytesOut, "<td><p>" + servicesToShow + "</td>");
          scoutln(out, bytesOut, "<td><p>" + remark + "</td></tr>");
        }

        if(infoRs   != null) infoRs.close();
        if(infoStmt != null) infoStmt.close();
      }

      if(conInfo  != null) conInfo.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(infoRs   != null) infoRs.close();
      if(infoStmt != null) infoStmt.close();
      if(conInfo  != null) conInfo.close();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String getSectionByPosition(String orderList, int entryWithinOrderList, String entriesList) throws Exception
  {
    int x, y = 0;

    for(x=0;x<entryWithinOrderList;++x)
    {
      while(orderList.charAt(y) != '\001')
        ++y;
      ++y;
    }

    String entryWithinEntriesList = "";
    while(orderList.charAt(y) != '\001')
      entryWithinEntriesList += orderList.charAt(y++);

    int offset = generalUtils.strToInt(entryWithinEntriesList);

    y = 0;
    for(x=0;x<offset;++x)
    {
      while(entriesList.charAt(y) != '\001')
        ++y;
      ++y;
    }

    String thisEntry = "";
    while(entriesList.charAt(y) != '\001')
      thisEntry += entriesList.charAt(y++);

    return thisEntry;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean anyBlogsForAService(Connection conInfo, Statement stmtInfo, ResultSet rsInfo, String services) throws Exception
  {
    int rowCount = 0;

    try
    {
      int len = services.length();
      int y = 0;
      String service;
      while(y < len)
      {
        service = "";
        while(y < len && services.charAt(y) != ',')
          service += services.charAt(y++);
        ++y;

        stmtInfo = conInfo.createStatement();

        rsInfo = stmtInfo.executeQuery("SELECT COUNT(*) AS rowcount from blogs WHERE ServiceCode = '" + service + "' AND Published='Y'");

        if(rsInfo.next())
          rowCount += rsInfo.getInt("rowcount");

        if(rsInfo   != null) rsInfo.close();
        if(stmtInfo != null) stmtInfo.close();
      }
    }
    catch(Exception e)
    {
      if(rsInfo   != null) rsInfo.close();
      if(stmtInfo != null) stmtInfo.close();
    }

    if(rowCount == 0)
      return false;
    return true;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean anyIssuesForAService(Connection conInfo, Statement stmtInfo, ResultSet rsInfo, String service) throws Exception
  {
    int rowCount = 0;

    try
    {
      stmtInfo = conInfo.createStatement();

      rsInfo = stmtInfo.executeQuery("SELECT COUNT(0) AS rowcount from rat WHERE Service = '" + service + "'");

      if(rsInfo.next())
        rowCount += rsInfo.getInt("rowcount");

      if(rsInfo   != null) rsInfo.close();
      if(stmtInfo != null) stmtInfo.close();
    }
    catch(Exception e)
    {
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
