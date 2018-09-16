// =======================================================================================================================================================================================================
// System: ZaraStar Issues: Utilities
// Module: IssuesUtils.java
// Author: C.K.Harvey
// Copyright (c) 2006-09 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
// =======================================================================================================================================================================================================
 
package org.zarastar.zarastar;

import java.io.*;
import java.sql.*;
import javax.servlet.http.HttpServletRequest;

public class IssuesUtils
{
  GeneralUtils generalUtils = new GeneralUtils();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  DashboardUtils dashboardUtils = new DashboardUtils();
  Wiki wiki = new Wiki();
 
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void outputPageFrame(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String callingServlet, String service, String bodyStr, String otherSetup, String unm, String sid, String uty, String men,
                              String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] hmenuCount, int[] bytesOut) throws Exception
  {
    String subMenuText = dashboardUtils.buildSubMenuText(con, stmt, rs, req, service, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount);

    scoutln(out, bytesOut, authenticationUtils.buildScreen(con, stmt, rs, req, true, unm, sid, uty, men, den, dnm, bnm, callingServlet, subMenuText, hmenuCount[0], bodyStr, otherSetup, localDefnsDir, defnsDir));
    
    scoutln(out, bytesOut, "<table border=0><tr><td valign=top><div id='second' style=\"{margin-top:0px;}\"></div></td><td valign=top><div id='third' style=\"{margin-top:0px;margin-left:0px;}\"></div></td></tr></table>");
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void drawTitle(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, PrintWriter out, String title, String service, String ratCode, boolean viewingIssue, boolean editingIssue, String unm, String sid, String uty,
                        String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] hmenuCount, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<table id='title' width=100%>");
    scoutln(out, bytesOut, "<tr><td colspan=2 nowrap><p>" + title + directoryUtils.buildHelp(service) + "</td></tr></table>");

    scoutln(out, bytesOut, "<table id='submenu' width=100%><tr><td>" + buildSubMenuText(con, stmt, rs, req, ratCode, viewingIssue, editingIssue, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount) + "</td></tr></table>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String buildSubMenuText(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, String ratCode, boolean viewingIssue, boolean editingIssue, String unm, String sid, String uty, String men, String mainDNM, String dnm,
                                  String bnm, String localDefnsDir, String defnsDir, int[] hmenuCount) throws Exception
  {
    String s = "";

    if(editingIssue)
    {
      s += "<form name=\"form5902\" action=\"RATSaveIssue\" enctype=\"application/x-www-form-urlencoded\" method=\"POST\" >";
      s += "<script language=\"JavaScript\">function go5902(){var d=document.getElementById('wikiview');"
        + "if(d!=null)document.getElementById('p1').value=d.contentWindow.document.body.innerHTML;";
      
      s += "document.forms['form5902'].submit();}</script>";

      s += "<input type='hidden' name='unm' value='" + unm + "'>";
      s += "<input type='hidden' name='sid' value='" + sid + "'>";
      s += "<input type='hidden' name='uty' value='" + uty + "'>";
      s += "<input type='hidden' name='men' value='" + men + "'>";
      s += "<input type='hidden' name='den' value='" + mainDNM + "'>";
      s += "<input type='hidden' name='dnm' value='" + dnm + "'>";
      s += "<input type='hidden' name='bnm' value='" + bnm + "'>";
      s += "<input type='hidden' id='p1' name='p1' value=''>";
    }

    s += "<div id='submenu'>";

    hmenuCount[0] = 1;

    if(editingIssue && (authenticationUtils.verifyAccess(con, stmt, rs, req, 5904, unm, uty, dnm, localDefnsDir, defnsDir)))
    {
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\">";
      s += "<a href=\"http://" + men + "/central/servlet/RATDelete?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + mainDNM + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + ratCode + "\">Delete Issue</a></dt></dl>\n";
    }
    
    if(editingIssue)
    {
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\"><a href=\"javascript:go5902()\">Save</a></dt></dl>\n";
    }
        
    s += "</div>";
    
    --hmenuCount[0];
    
    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void drawTitleW(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, PrintWriter out, String title, String service, String ratCode, boolean viewingIssue, boolean editingIssue, String unm, String sid, String uty,
                         String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<table id='title' width=100%>");
    scoutln(out, bytesOut, "<tr><td colspan=2 nowrap><span id='x'>" + title + directoryUtils.buildHelp(service) + "</span></td></tr></table>");

    int hmenuCount[] = new int[1];  hmenuCount[0] = 1;

    scoutln(out, bytesOut, authenticationUtils.createHorizSetupStringW());

    scoutln(out, bytesOut, "<table id='submenuX' width=100%><tr><td>" + buildSubMenuTextW(con, stmt, rs, req, ratCode, viewingIssue, editingIssue, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount) + "</td></tr></table>");

    --hmenuCount[0];

    scoutln(out, bytesOut, "<script language='JavaScript'>setup(" + hmenuCount[0] + ",'Y');</script>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String buildSubMenuTextW(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, String ratCode, boolean viewingIssue, boolean editingIssue, String unm, String sid, String uty, String men, String mainDNM, String dnm,
                                  String bnm, String localDefnsDir, String defnsDir, int[] hmenuCount) throws Exception
  {
    String s = "<div id='submenu'>";

    hmenuCount[0] = 1;

    if(editingIssue)
    {
      s += "<script language=\"JavaScript\">function go5902(){var d=document.getElementById('wikiview');if(d!=null)document.getElementById('p1').value=d.contentWindow.document.body.innerHTML;";

      s += "postForm('RATSaveIssueWave','form5902');}</script>";

      s += "<input type='hidden' name='unm' value='" + unm + "'>";
      s += "<input type='hidden' name='sid' value='" + sid + "'>";
      s += "<input type='hidden' name='uty' value='" + uty + "'>";
      s += "<input type='hidden' name='men' value='" + men + "'>";
      s += "<input type='hidden' name='den' value='" + mainDNM + "'>";
      s += "<input type='hidden' name='dnm' value='" + dnm + "'>";
      s += "<input type='hidden' name='bnm' value='" + bnm + "'>";
      s += "<input type='hidden' id='p1' name='p1' value=''>";
    }

    if(editingIssue && (authenticationUtils.verifyAccess(con, stmt, rs, req, 5904, unm, uty, dnm, localDefnsDir, defnsDir)))
    {
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\">";
      s += "<a href=\"javascript:getHTML('RATDeleteWave','&p1=" + ratCode + "')\">Delete Issue</a></dt></dl>\n";
    }

    if(editingIssue)
    {
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\"><a href=\"javascript:go5902()\">Save</a></dt></dl>\n";
    }

    s += "</div>";

    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void createTableRAT(boolean dropTable, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    Class.forName("com.mysql.jdbc.Driver").newInstance();

    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_info", conp);
    Statement stmt = con.createStatement();
    String q;

    try
    {
      if(dropTable)
      {
        q = "DROP TABLE rat";
        stmt.executeUpdate(q);
      }
    }
    catch(Exception e) { }
    if(stmt != null) stmt.close();

    try
    {
      stmt = con.createStatement();
      q = "CREATE TABLE rat ( RATCode integer not null, DateSubmitted date, UserCode char(20), UserCompany char(40), Title char(100), Description mediumtext, ReleaseLevel char(20), Status char(1), DateClosed date, "
                           + "ClosedByUser char(20), FixRelease char(20), DuplicateOf char(20), DateTargeted date, Type char(60), Category char(60), Priority char(1), HoursSpent decimal(10, 1), IsPrivate char(1), Service char(20))";

      stmt.executeUpdate(q);
    }
    catch(Exception e) { }

    if(stmt != null) stmt.close();

    stmt = con.createStatement();

    try
    {
      if(dropTable)
      {
        q = "DROP TABLE rata";
        stmt.executeUpdate(q);
      }
    }
    catch(Exception e) { }
    if(stmt != null) stmt.close();

    try
    {
      stmt = con.createStatement();
      q = "CREATE TABLE rata ( RATCode integer not null, LibraryDocCode integer not null, unique(RATCode, LibraryDocCode))";

      stmt.executeUpdate(q);
    }
    catch(Exception e) { }

    if(stmt != null) stmt.close();

    stmt = con.createStatement();

    try
    {
      if(dropTable)
      {
        q = "DROP TABLE ratl";
        stmt.executeUpdate(q);
      }
    }
    catch(Exception e) { }
    if(stmt != null) stmt.close();

    try
    {
      stmt = con.createStatement();
      q = "CREATE TABLE ratl ( RATCode integer not null, DateTime timestamp, UserCode char(20), UserCompany char(20), Description mediumtext )";

      stmt.executeUpdate(q);
    }
    catch(Exception e) { }

    if(stmt != null) stmt.close();
    if(con  != null) con.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void createTableRATCat(boolean dropTable, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    String uName = directoryUtils.getMySQLUserName();
    String pWord = directoryUtils.getMySQLPassWord();

    Class.forName("com.mysql.jdbc.Driver").newInstance();

    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_info", conp);
    Statement stmt = con.createStatement();
    String q;

    try
    {
      if(dropTable)
      {
        q = "DROP TABLE ratcat";
        stmt.executeUpdate(q);
      }
    }
    catch(Exception e) { }
    if(stmt != null) stmt.close();

    try
    {
      stmt = con.createStatement();
      q = "CREATE TABLE ratcat ( Name char(60), Type char(1), Position integer, Description mediumtext )";

      stmt.executeUpdate(q);
    }
    catch(Exception e) { }

    if(stmt != null) stmt.close();

    if(con  != null) con.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void createTableRATProblemTypes(boolean dropTable, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    Class.forName("com.mysql.jdbc.Driver").newInstance();

    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_info", conp);
    Statement stmt = con.createStatement();
    String q;

    try
    {
      if(dropTable)
      {
        q = "DROP TABLE ratprob";
        stmt.executeUpdate(q);
      }
    }
    catch(Exception e) { }
    if(stmt != null) stmt.close();

    try
    {
      stmt = con.createStatement();
      q = "CREATE TABLE ratprob ( Name char(60), Position integer )";

      stmt.executeUpdate(q);
    }
    catch(Exception e)
    {
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }

    if(stmt != null) stmt.close();

    if(con  != null) con.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void showStats(PrintWriter out, String dnm, int[] bytesOut) throws Exception
  {
    String uName = directoryUtils.getMySQLUserName();
    String pWord = directoryUtils.getMySQLPassWord();
    
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_info", conp);
    Statement stmt = con.createStatement();
   
    ResultSet rs = stmt.executeQuery("SELECT TABLE_NAME, CREATE_TIME, UPDATE_TIME, TABLE_ROWS FROM INFORMATION_SCHEMA.TABLES WHERE table_schema='" + dnm + "_info'");
    
    String tableName="", createDate="", numRows="", modifyDate="", cssFormat="";
    
    while(rs.next())                  
    {
      tableName = rs.getString(1);

      if(tableName.startsWith("rat"))
      {
        createDate = rs.getString(2);
        modifyDate = rs.getString(3);
        numRows    = rs.getString(4);
        
        if(cssFormat.equals("line1")) cssFormat = "line2"; else cssFormat = "line1";

        scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\"><td><p>" + tableName + "</td>");
        scoutln(out, bytesOut, "<td align=center><p>" + numRows + "</td>");
        scoutln(out, bytesOut, "<td align=center><p>" + createDate + "</td>");
        scoutln(out, bytesOut, "<td align=center><p>" + modifyDate + "</td></tr>");
      }
    }   
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    if(con  != null) con.close();
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean addToRAT(String ratCode, String date, String userCode, String userCompany, String title, String desc, String release, String category, String type, String fixRelease, String status, String dateClosed, String dateTargeted, String duplicateOf,
                          String priority, String hoursSpent, String closedByUser, String isPrivate, String service, String dnm, String[] newCode) throws Exception
  {
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

      rs = stmt.executeQuery("SELECT RATCode FROM rat WHERE RATCode='" + ratCode + "'");

      boolean isNew;

      if(rs.next())
        isNew = false;
      else isNew = true;

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();

      if(isNew)
      {
        stmt = con.createStatement();

        // determine next code
        rs = stmt.executeQuery("SELECT RATCode FROM rat ORDER BY RATCode DESC");
        if(rs.next())
        {
          int highestCode = generalUtils.strToInt(rs.getString(1));
          newCode[0]      = generalUtils.intToStr(highestCode + 1);
        }
        else newCode[0] = "1";

        if(rs   != null) rs.close();
        if(stmt != null) stmt.close();
      }
      else newCode[0] = ratCode;

      if(date.length()         == 0) date         = "1970-01-01";
      if(dateTargeted.length() == 0) dateTargeted = "1970-01-01";
      if(dateClosed.length()   == 0) dateClosed   = "1970-01-01";
      if(hoursSpent.length()   == 0) hoursSpent   = "0";

      String q;

      if(isNew)
      {
        q = "INSERT INTO rat ( RATCode, DateSubmitted, UserCode, UserCompany, Title, Description, ReleaseLevel, Status, DateClosed, ClosedByUser, FixRelease, DuplicateOf, Type, DateTargeted, Category, Priority, HoursSpent, IsPrivate, Service ) "
          + "VALUES ('" + newCode[0] + "', {d '" + date + "'}, '" + generalUtils.sanitiseForSQL(userCode) + "', '" + generalUtils.sanitiseForSQL(userCompany) + "', '" + generalUtils.sanitiseForSQL(title) + "','" + generalUtils.sanitiseForSQL(desc) + "','"
          + generalUtils.sanitiseForSQL(release) + "','O',{d '1970-01-01'},'" + generalUtils.sanitiseForSQL(closedByUser) + "','" + generalUtils.sanitiseForSQL(fixRelease) + "','" + duplicateOf + "','" + type + "',{d '" + dateTargeted + "'},'"
          + generalUtils.sanitiseForSQL(category) + "','U','0','N','" + generalUtils.sanitiseForSQL(service) + "')";
      }
      else
      {
        q = "UPDATE rat SET DateSubmitted = {d '" + date + "'}, UserCode = '" + generalUtils.sanitiseForSQL(userCode) + "', UserCompany = '" + generalUtils.sanitiseForSQL(userCompany) + "', Title = '" + generalUtils.sanitiseForSQL(title) + "', Description = '" + generalUtils.sanitiseForSQL(desc)
          + "', ReleaseLevel = '" + generalUtils.sanitiseForSQL(release) + "', Status = '" + status + "', DateClosed = {d '" + dateClosed + "'}, ClosedByUser = '" + generalUtils.sanitiseForSQL(closedByUser) + "', FixRelease = '" + generalUtils.sanitiseForSQL(fixRelease)
          + "', DuplicateOf = '" + duplicateOf + "', DateTargeted = {d '" + dateTargeted + "'}, Type = '" + type + "', Category = '" + generalUtils.sanitiseForSQL(category) + "', Priority = '" + priority + "', HoursSpent = '" + hoursSpent
          + "', IsPrivate = '" + isPrivate + "', Service = '" + generalUtils.sanitiseForSQL(service) + "' WHERE RATCode = '" + ratCode + "'";
      }

      stmt = con.createStatement();

      stmt.executeUpdate(q);

      if(stmt != null) stmt.close();
      if(con  != null) con.close();

      return true;
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }

    return false;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean addToRATL(String ratCode, String dateTime, String userCode, String userCompany, String desc, String dnm) throws Exception
  {
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

      rs = stmt.executeQuery("SELECT RATCode FROM ratl WHERE RATCode = '" + ratCode + "' AND DateTime ='" + dateTime + "'");

      boolean isNew;

      if(rs.next())
        isNew = false;
      else isNew = true;

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();

      String q;

      if(isNew)
      {
        q = "INSERT INTO ratl ( RATCode, DateTime, UserCode, UserCompany, Description ) VALUES ('" + ratCode + "', NULL, '" + generalUtils.sanitiseForSQL(userCode) + "', '" + generalUtils.sanitiseForSQL(userCompany) + "', '" + generalUtils.sanitiseForSQL(desc)
          + "')";
      }
      else
      {
        q = "UPDATE ratl SET UserCode = '" + generalUtils.sanitiseForSQL(userCode) + "', UserCompany = '" + generalUtils.sanitiseForSQL(userCompany) + "', Description = '" + generalUtils.sanitiseForSQL(desc) + "', DateTime ='" + dateTime + "' WHERE RATCode = '"
          + ratCode + "' AND DateTime ='" + dateTime + "'";
      }

      stmt = con.createStatement();

      stmt.executeUpdate(q);

      if(stmt != null) stmt.close();
      if(con  != null) con.close();

      return true;
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }

    return false;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void listRATs(PrintWriter out, String category, String reqdStatus, String unm, String dnm, String uty, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    Connection con = null;
    Statement stmt = null;
    ResultSet rs = null;

    try
    {
      String uName = directoryUtils.getMySQLUserName();
      String pWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();

      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_info?user=" + uName + "&password=" + pWord);

      stmt = con.createStatement();

      String where = "";
      if(reqdStatus.equals("O")) // open
        where = "AND status = 'O'";
      else
      if(reqdStatus.equals("C")) // closed
        where = "AND status = 'C'";
      else
      if(reqdStatus.equals("F")) /// awaiting feedback
        where = "AND status = 'F'";

      String q = "SELECT RATCode, Title, Status, Type FROM rat WHERE Category = '" + generalUtils.sanitiseForSQL(category) + "' " + where + " ORDER BY RATCode";

      rs = stmt.executeQuery(q);

      String cssFormat = "", ratCode, title, status, type;

      while(rs.next())
      {
        ratCode = rs.getString(1);
        title   = rs.getString(2);
        status  = rs.getString(3);
        type    = rs.getString(4);

        if(status.equals("O"))
          status = "Open";
        else
        if(status.equals("C"))
          status = "Closed";
        else status = "On-Hold";

        if(type.equals("P"))
          type = "Problem";
        else type = "Discussion";

        if(cssFormat.equals("line2")) cssFormat = "line1"; else cssFormat = "line2";

        scoutln(out, bytesOut, "<tr><td><p>" + ratCode + "</td><td><p><a href=\"javascript:view('" + ratCode + "')\">" + title + "</a></td><td><p>" + type + "&nbsp;&nbsp;</td><td><p>" + status + "</td></tr>");
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
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void listRATsGivenService(PrintWriter out, String service, String reqdStatus, String unm, String dnm, String uty, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    Connection con = null;
    Statement stmt = null;
    ResultSet rs = null;

    try
    {
      String uName = directoryUtils.getMySQLUserName();
      String pWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();

      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_info?user=" + uName + "&password=" + pWord);

      stmt = con.createStatement();

      String where = "";
      if(reqdStatus.equals("O")) // open
        where = "AND status = 'O'";
      else
      if(reqdStatus.equals("C")) // closed
        where = "AND status = 'C'";
      else
      if(reqdStatus.equals("F")) /// awaiting feedback
        where = "AND status = 'F'";

      String q = "SELECT RATCode, Title, Status, Type FROM rat WHERE Service = '" + generalUtils.sanitiseForSQL(service) + "' " + where + " ORDER BY RATCode";

      rs = stmt.executeQuery(q);

      String cssFormat = "", ratCode, title, status, type;

      while(rs.next())
      {
        ratCode = rs.getString(1);
        title   = rs.getString(2);
        status  = rs.getString(3);
        type    = rs.getString(4);

        if(status.equals("O"))
          status = "Open";
        else
        if(status.equals("C"))
          status = "Closed";
        else status = "On-Hold";

        if(type.equals("P"))
          type = "Problem";
        else type = "Discussion";

        if(cssFormat.equals("line2")) cssFormat = "line1"; else cssFormat = "line2";

        scoutln(out, bytesOut, "<tr><td><p>" + ratCode + "</td><td><p><a href=\"javascript:view('" + ratCode + "')\">" + title + "</a></td><td><p>" + type + "&nbsp;&nbsp;</td><td><p>" + status + "</td></tr>");
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
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean getRATLineGivenRATCodeAndDateTime(String ratCode, String dateTime, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String imageLibraryDir, String flashDir, String localDefnsDir,
                                                   String[] userCode, String[] userCompany, String[] desc) throws Exception
  {
    Connection con = null;
    Statement stmt = null;
    ResultSet rs = null;

    boolean res = false;

    try
    {
      String uName = directoryUtils.getMySQLUserName();
      String pWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();

      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_info?user=" + uName + "&password=" + pWord);

      stmt = con.createStatement();

      String q = "SELECT UserCode, UserCompany, Description FROM ratl WHERE RATCode='" + ratCode + "' AND dateTime='" + dateTime + "'";

      rs = stmt.executeQuery(q);

      if(rs.next())
      {
        userCode[0]    = generalUtils.deNull(rs.getString(1));
        userCompany[0] = generalUtils.deNull(rs.getString(2));
        desc[0]        = generalUtils.deNull(rs.getString(3));

        desc[0] = wiki.convertLinks(desc[0], unm, sid, uty, men, den, dnm, bnm, imageLibraryDir, flashDir, localDefnsDir);
      }

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
      if(con  != null) con.close();

      res = true;
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }

    return res;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getTypeGivenName(String name, String dnm) throws Exception
  {
    Connection con = null;
    Statement stmt = null;
    ResultSet rs = null;

    String type = "D"; // discussion, jic

    try
    {
      String uName = directoryUtils.getMySQLUserName();
      String pWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();

      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_info?user=" + uName + "&password=" + pWord);

      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT Type FROM ratcat WHERE name = '" + generalUtils.sanitiseForSQL(name) + "'");

      if(rs.next())
        type = rs.getString(1);

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

    return type;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean getRATGivenRATCode(String ratCode, String dnm, String[] title, String[] desc, String[] dateSubmitted, String[] userCode, String[] userCompany, String[] release, String[] category, String[] type, String[] status, String[] fixRelease,
                                    String[] dateClosed, String[] dateTargeted, String[] duplicateOf, String[] priority, String[] hoursSpent, String[] closedByUser, String[] isPrivate, String[] service) throws Exception
  {
    Connection con = null;
    Statement stmt = null;
    ResultSet rs = null;
    
    boolean res = false;
    
    try
    {
      String uName = directoryUtils.getMySQLUserName();
      String pWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();

      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_info?user=" + uName + "&password=" + pWord);

      stmt = con.createStatement();

      String q = "SELECT * FROM rat WHERE RATCode='" + ratCode + "'";

      rs = stmt.executeQuery(q);

      if(rs.next())                  
      {
        dateSubmitted[0] = generalUtils.deNull(rs.getString(2));
        userCode[0]      = generalUtils.deNull(rs.getString(3));
        userCompany[0]   = generalUtils.deNull(rs.getString(4));
        title[0]         = generalUtils.deNull(rs.getString(5));
        desc[0]          = generalUtils.deNull(rs.getString(6));
        release[0]       = generalUtils.deNull(rs.getString(7));
        status[0]        = generalUtils.deNull(rs.getString(8));
        dateClosed[0]    = generalUtils.deNull(rs.getString(9));
        closedByUser[0]  = generalUtils.deNull(rs.getString(10));
        fixRelease[0]    = generalUtils.deNull(rs.getString(11));
        duplicateOf[0]   = generalUtils.deNull(rs.getString(12));
        dateTargeted[0]  = generalUtils.deNull(rs.getString(13));
        type[0]          = generalUtils.deNull(rs.getString(14));
        category[0]      = generalUtils.deNull(rs.getString(15));
        priority[0]      = generalUtils.deNull(rs.getString(16));
        hoursSpent[0]    = generalUtils.deNull(rs.getString(17));
        isPrivate[0]     = generalUtils.deNull(rs.getString(18));
        service[0]       = generalUtils.deNull(rs.getString(19));

        if(isPrivate[0].length() == 0) isPrivate[0]  = "N";
        
        res = true;
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
    
    return res;
  }  

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // B - Open Bugs (Accepted)
  // U - Open Bugs (Unverified)
  // F - Open New Feature Requests
  // I - Open Feature Improvement Requests
  // N - Open Information Requests
  // C - All Closed Issues
  public void getSummaryData(int[] openBugsAccepted, int[] openBugsUnverified, int[] openNewFeatureRequests, int[] openFeatureImprovementRequests, int[] openInformationRequests, int[] all, int[] closed,
                             String dnm) throws Exception
  {
    Connection con = null;
    Statement stmt = null;
    ResultSet rs = null;

    try
    {
      String uName = directoryUtils.getMySQLUserName();
      String pWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();

      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_info?user=" + uName + "&password=" + pWord);

      stmt = con.createStatement();
      all[0] = 0;
      rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM rat");
      if(rs.next())
        all[0] = rs.getInt("rowcount");
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();

      stmt = con.createStatement();
      closed[0] = 0;
      rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM rat WHERE Status = 'C'");
      if(rs.next())
        closed[0] = rs.getInt("rowcount");
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();

      stmt = con.createStatement();
      openBugsAccepted[0] = 0;
      rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM rat WHERE Status = 'O' AND type = 'B'");
      if(rs.next())
        openBugsAccepted[0] = rs.getInt("rowcount");
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();

      stmt = con.createStatement();
      openBugsUnverified[0] = 0;
      rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM rat WHERE Status = 'O' AND type = 'U'");
      if(rs.next())
        openBugsUnverified[0] = rs.getInt("rowcount");
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();

      stmt = con.createStatement();
      openNewFeatureRequests[0] = 0;
      rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM rat WHERE Status = 'O' AND type = 'F'");
      if(rs.next())
        openNewFeatureRequests[0] = rs.getInt("rowcount");
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();

      stmt = con.createStatement();
      openFeatureImprovementRequests[0] = 0;
      rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM rat WHERE Status = 'O' AND type = 'I'");
      if(rs.next())
        openFeatureImprovementRequests[0] = rs.getInt("rowcount");
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();

      stmt = con.createStatement();
      openInformationRequests[0] = 0;
      rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM rat WHERE Status = 'O' AND type = 'N'");
      if(rs.next())
        openInformationRequests[0] = rs.getInt("rowcount");
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
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean delFromRAT(String ratCode, String dnm) throws Exception
  {
    Connection con = null;
    Statement stmt = null;

    try
    {
      String uName = directoryUtils.getMySQLUserName();
      String pWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();

      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_info?user=" + uName + "&password=" + pWord);

      stmt = con.createStatement();

      stmt.executeUpdate("DELETE FROM rat WHERE RATCode = '" + ratCode + "'");

      if(stmt != null) stmt.close();

      stmt = con.createStatement();

      stmt.executeUpdate("DELETE FROM ratl WHERE RATCode = '" + ratCode + "'");

      if(stmt != null) stmt.close();

      if(con  != null) con.close();

      return true;
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }

    return false;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean delFromRATL(String ratCode, String dateTime, String dnm) throws Exception
  {
    Connection con = null;
    Statement stmt = null;

    try
    {
      String uName = directoryUtils.getMySQLUserName();
      String pWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();

      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_info?user=" + uName + "&password=" + pWord);

      stmt = con.createStatement();

      stmt.executeUpdate("DELETE FROM ratl WHERE RATCode = '" + ratCode + "' AND DateTime ='" + dateTime + "'");

      if(stmt != null) stmt.close();
      if(con  != null) con.close();

      return true;
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }

    return false;
  }

}
