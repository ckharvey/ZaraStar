// =======================================================================================================================================================================================================
// System: ZaraStar BlogGuide: Utilities
// Module: BlogGuideUtils.java
// Author: C.K.Harvey
// Copyright (c) 2006-09 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.sql.*;

public class BlogGuideUtils
{
  GeneralUtils generalUtils = new GeneralUtils();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  DashboardUtils dashboardUtils = new DashboardUtils();

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void outputPageFrame(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String bodyStr, String callingServlet, String service, String unm, String sid, String uty, String men, String den, String dnm,
                              String bnm, String otherSetup, String localDefnsDir, String defnsDir, int[] hmenuCount, int[] bytesOut) throws Exception
  {
    String subMenuText = dashboardUtils.buildSubMenuText(con, stmt, rs, req, service, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount);

    scoutln(out, bytesOut, authenticationUtils.buildScreen(con, stmt, rs, req, true, unm, sid, uty, men, den, dnm, bnm, callingServlet, subMenuText, hmenuCount[0], bodyStr, otherSetup, localDefnsDir, defnsDir));

    scoutln(out, bytesOut, "<table border=0><tr><td valign=top><div id='second' style=\"{margin-top:0px;}\"></div></td>");
    scoutln(out, bytesOut, "<td valign=top><div id='third' style=\"{margin-top:0px;margin-left:0px;}\"></div></td></tr></table>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void drawTitle(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, PrintWriter out, String title, String service, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir,
                        String defnsDir, int[] hmenuCount, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<table id='title' width=100%>");
    scoutln(out, bytesOut, "<tr><td colspan=2 nowrap><p>" + title + directoryUtils.buildHelp(service) + "</td></tr></table>");

    scoutln(out, bytesOut, "<table id='submenu' width=100%><tr><td>" + buildSubMenuText(con, stmt, rs, req, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount) + "</td></tr></table>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void drawTitleW(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, PrintWriter out, String title, String service, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir,
                        String defnsDir, int[] hmenuCount, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<table id='title' width=100%>");
    scoutln(out, bytesOut, "<tr><td colspan=2 nowrap><span id='x'>" + title + directoryUtils.buildHelp(service) + "</span></td></tr></table>");

    scoutln(out, bytesOut, "<table id='submenu' width=100%><tr><td>" + buildSubMenuText(con, stmt, rs, req, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount) + "</td></tr></table>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String buildSubMenuText(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] hmenuCount)
                                  throws Exception
  {
    String s = "<div id='submenu'>";

    hmenuCount[0] = 1;

    --hmenuCount[0];

    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void createTableBlogGuide(boolean dropTable, String dnm) throws Exception
  {
    String userName = directoryUtils.getMySQLUserName();
    String passWord = directoryUtils.getMySQLPassWord();

    Class.forName("com.mysql.jdbc.Driver").newInstance();

    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_info?user=" + userName + "&password=" + passWord);
    Statement stmt = con.createStatement();
    String q;

    try
    {
      if(dropTable)
      {
        q = "DROP TABLE blogguide";
        stmt.executeUpdate(q);
      }
    }
    catch(Exception e) { }
    if(stmt != null) stmt.close();

    try
    {
      stmt = con.createStatement();
      q = "CREATE TABLE blogguide ( Name char(60), Title char(60), Section char(10), Services char(40), Remark char(100), Abridged char(1) )";
      stmt.executeUpdate(q);
    }
    catch(Exception e) { }

    if(stmt != null) stmt.close();
    if(con  != null) con.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void listBlogGuides(Statement stmt, ResultSet rs, PrintWriter out, String dnm, int[] bytesOut) throws Exception
  {
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

      rsInfo = stmtInfo.executeQuery("SELECT DISTINCT Name FROM blogguide ORDER BY Name");

      String name, cssFormat = "";

      while(rsInfo.next())
      {
        name = rsInfo.getString(1);

        if(cssFormat.equals("line2")) cssFormat = "line1"; else cssFormat = "line2";

        scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\">");

        scoutln(out, bytesOut, "<td width=180></td><td><p><a href=\"javascript:edit('" + name + "')\">" + name + "</a></td></tr>");
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
  public void showStats(PrintWriter out, String dnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    String userName = directoryUtils.getMySQLUserName();
    String passWord = directoryUtils.getMySQLPassWord();

    Class.forName("com.mysql.jdbc.Driver").newInstance();

    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_info?user=" + userName + "&password=" + passWord);
    Statement stmt = con.createStatement();

    ResultSet rs = stmt.executeQuery("SELECT TABLE_NAME, CREATE_TIME, UPDATE_TIME, TABLE_ROWS FROM INFORMATION_SCHEMA.TABLES where table_schema='" + dnm + "_sms'");

    String tableName, createDate, numRows, modifyDate, cssFormat="";

    while(rs.next())
    {
      tableName  = rs.getString(1);
      createDate = rs.getString(2);
      modifyDate = rs.getString(3);
      numRows    = rs.getString(4);

     if(cssFormat.equals("line1")) cssFormat = "line2"; else cssFormat = "line1";

      scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\"><td>" + tableName + "</td>");
      scoutln(out, bytesOut, "<td align=center>" + numRows + "</td>");
      scoutln(out, bytesOut, "<td align=center>" + createDate + "</td>");
      scoutln(out, bytesOut, "<td align=center>" + modifyDate + "</td></tr>");
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    if(con  != null) con.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {
    out.println(str);
    bytesOut[0] += (str.length() + 2);
  }

}
