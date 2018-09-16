// =======================================================================================================================================================================================================
// System: ZaraStar Utils: fetch IMs incoming
// Module: AdminControlUtilsIMsIncoming.java
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

public class AdminControlUtilsIMsIncoming extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  AdminUtils adminUtils = new AdminUtils();
  DefinitionTables definitionTables = new DefinitionTables();
  Profile profile = new Profile();
  AdminControlUtils adminControlUtils = new AdminControlUtils();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="";

    try
    {
      directoryUtils.setContentHeaders(res);
      res.setHeader("Header", "HTTP/1.0 200 OK");
      res.setStatus(200);
      res.setContentType("text/html");
      res.setHeader("Cache-Control", "no-cache");

      unm = req.getParameter("unm");
      sid = req.getParameter("sid");
      uty = req.getParameter("uty");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");

      doIt(req, res, unm, sid, uty, men, den, dnm, bnm, bytesOut);
    }
    catch(Exception e)
    {
      System.out.println(("Unexpected System Error: 7599g: " + e));
      res.getWriter().write("Unexpected System Error: 7599g");
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(HttpServletRequest req, HttpServletResponse res, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    res.getWriter().write( mergeData(req, unm, sid, uty, men, den, dnm, bnm, bytesOut));

    serverUtils.totalBytes(req, unm, dnm, 7599, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private String mergeData(HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    Class.forName("com.mysql.jdbc.Driver").newInstance();

    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);

    Statement stmt = null;
    ResultSet rs   = null;

    int[] count = new int[1];  count[0] = 1;

    String s = "";
    if(uty.equals("I"))
    {
      // s = getItems(con, stmt, rs, req, unm, sid, uty, dnm, localDefnsDir, defnsDir, count);
      --count[0];
    }
    else s = "\"x11\":\"Sign-In First\",\"x12\":\"AboutZaraw\",\"x13\":\"\"";

    if(con != null) con.close();

    return "{res:[{\"msg\":\"" + count[0] + "\"," + s + "}]}";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String addOption(String label, String servlet, String params, int[] count)
  {
    String s = "";

    try
    {
      if(count[0] > 1)
        s += ",";
      s += "\"x" + count[0] + "1\":\"" + label + "\",\"x" + count[0] + "2\":\"" + servlet + "\",\"x" + count[0] + "3\":\"" + params + "\"";
      ++count[0];
    }
    catch(Exception e) { }

    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String getItems(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, String unm, String sid, String uty, String dnm, String localDefnsDir, String defnsDir, int[] count) throws Exception
  {
    String s = "";

    Connection con2 = null;
    Statement stmt2 = null;
    ResultSet rs2   = null;

    try
    {
      String uName = directoryUtils.getMySQLUserName();
      String pWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();

      con2 = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_info?user=" + uName + "&password=" + pWord);

      String code, shortTitle, isTheHomePage;
      boolean wanted;

      stmt2 = con2.createStatement();

      rs2 = stmt2.executeQuery("SELECT Code, ShortTitle, IsTheHomePage FROM blogs WHERE IsAMenuItem='Y' AND Published = 'Y' ORDER BY ShortTitle");

      while(rs2.next())
      {
        code          = rs2.getString(1);
        shortTitle    = rs2.getString(2);
        isTheHomePage = rs2.getString(3);

        wanted = false;
        if(isTheHomePage.equals("Y"))
          ; // home page is automatically listed
        else wanted = true;

        if(wanted)
          s += addOption(shortTitle, "SiteDisplayPageWave", "&p1=" + code, count);
      }

      if(rs2   != null) rs2.close();
      if(stmt2 != null) stmt2.close();

      s += addOption("-", "", "", count);

      stmt2 = con2.createStatement();

      rs2 = stmt2.executeQuery("SELECT DISTINCT Name FROM blogguide ORDER BY Name");

      String name;

      while(rs2.next())
      {
        name = rs2.getString(1);
        s += addOption(name, "BlogsDisplayBlogGuideWave", "&p1=" + name, count);
      }

      if(rs2   != null) rs2.close();
      if(stmt2 != null) stmt2.close();
      if(con2  != null) con2.close();
    }
    catch(Exception e)
    {
      System.out.println("100k " + e);
      if(rs2   != null) rs2.close();
      if(stmt2 != null) stmt2.close();
      if(con2  != null) con2.close();
    }

    return s;
  }


}
