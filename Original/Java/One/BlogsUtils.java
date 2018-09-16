// =======================================================================================================================================================================================================
// System: ZaraStar Blogs: Utilities
// Module: BlogsUtils.java
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

public class BlogsUtils
{
  GeneralUtils generalUtils = new GeneralUtils();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  DashboardUtils dashboardUtils = new DashboardUtils();

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void outputPageFrame(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String callingServlet, String bodyStr, String otherSetup, String service, String unm, String sid, String uty, String men,
                              String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] hmenuCount, int[] bytesOut) throws Exception
  {
    String subMenuText = dashboardUtils.buildSubMenuText(con, stmt, rs, req, service, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount);

    scoutln(out, bytesOut, authenticationUtils.buildScreen(con, stmt, rs, req, false, unm, sid, uty, men, den, dnm, bnm, callingServlet, subMenuText, hmenuCount[0], bodyStr, otherSetup, localDefnsDir, defnsDir));
    
    scoutln(out, bytesOut, "<table border=0><tr><td valign=top><div id='second' style=\"{margin-top:0px;}\"></div></td>");
    scoutln(out, bytesOut, "<td valign=top><div id='third' style=\"{margin-top:0px;margin-left:0px;}\"></div></td></tr></table>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void drawTitle(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, PrintWriter out, String title, String service, String pageCode, boolean viewingBlogRoll, boolean editing, boolean htmlPage, String unm, String sid,
                        String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] hmenuCount, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<table id='title' width=100%>");
    scoutln(out, bytesOut, "<tr><td colspan=2 nowrap><p>" + title);
    if(service.length() > 0)
      scoutln(out, bytesOut, directoryUtils.buildHelp(service));
    scoutln(out, bytesOut, "</td></tr></table>");

    scoutln(out, bytesOut, "<table id='submenu' width=100%><tr><td>" + buildSubMenuText(con, stmt, rs, req, pageCode, viewingBlogRoll, editing, htmlPage, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount)
                         + "</td></tr></table>");

    scoutln(out, bytesOut, authenticationUtils.createHorizSetupString(hmenuCount[0]));
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String buildSubMenuText(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, String pageCode, boolean viewingBlogRoll, boolean editing, boolean htmlPage, String unm, String sid, String uty, String men,
                                  String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] hmenuCount) throws Exception
  {
    String s = "";

    s += "<div id='submenu'>";
    s += "<script language=\"JavaScript\" type=\"text/javascript\" src=\"http://" + men + directoryUtils.getEditorDirectory() + "editor.js\"></script>";

    if(editing)
      s += "<form name='form8103' id='form8103' action='BlogsSave'       enctype='application/x-www-form-urlencoded' method='POST'>";

    if(editing)
    {
      if(! htmlPage)
      {
        s += "<script language='JavaScript'>function go8103(option){var d=document.getElementById('wikiview');if(d!=null)document.getElementById('p1').value=d.contentWindow.document.body.innerHTML;"
          +  "document.getElementById('stay').value=option;";
      }
      else // htmlPage
      {
        s += "<script language='JavaScript'>function go8103(option){document.getElementById('p1').value=document.forms['form8103'].text1.value;document.getElementById('stay').value=option;";
      }

      s += "document.forms['form8103'].submit();}</script>";

      s += "<input type='hidden' name='unm' value='" + unm + "'>";
      s += "<input type='hidden' name='sid' value='" + sid + "'>";
      s += "<input type='hidden' name='uty' value='" + uty + "'>";
      s += "<input type='hidden' name='men' value='" + men + "'>";
      s += "<input type='hidden' name='den' value='" + den + "'>";
      s += "<input type='hidden' name='dnm' value='" + dnm + "'>";
      s += "<input type='hidden' name='bnm' value='" + bnm + "'>";
      s += "<input type='hidden' id='p1' name='p1' value=''>";
      s += "<input type='hidden' id='stay' name='stay' value=''>\n";
    }

    if(! editing && (authenticationUtils.verifyAccess(con, stmt, rs, req, 8102, unm, uty, dnm, localDefnsDir, defnsDir)))
    {
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0] + "');\">Create</dt>";
      s += "<dd id='hmenu" + hmenuCount[0]++ + "'><ul>";
      s += "<li><a href=\"http://" + men + "/central/servlet/BlogsEdit?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=" + pageCode + "&bnm=" + bnm + "\">Formatted Text</a></li>";
      s += "<li><a href=\"http://" + men + "/central/servlet/BlogsEditRaw?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=&bnm=" + bnm + "\">Raw HTML</a></li>";
      s += "</ul></dd></dl>";
    }

    if(pageCode.length() > 0 && editing)
    {
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\">";
      s += "<a href=\"http://" + men + "/central/servlet/BlogsDelete?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + pageCode + "\">Delete</a></dt></dl>\n";
    }

    if(editing)
    {
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0] + "');\">Save</dt>";
      s += "<dd id='hmenu" + hmenuCount[0]++ + "'><ul>";
      s += "<li><a href=\"javascript:go8103('Y')\">Save & Stay</a></li>";
      s += "<li><a href=\"javascript:go8103('N')\">Save & Finish</a></li>";
      s += "</ul></dd></dl>";
    }

    if(! editing && (    authenticationUtils.verifyAccess(con, stmt, rs, req, 8107, unm, uty, dnm, localDefnsDir, defnsDir)
                      || authenticationUtils.verifyAccess(con, stmt, rs, req, 8108, unm, uty, dnm, localDefnsDir, defnsDir)
                      || authenticationUtils.verifyAccess(con, stmt, rs, req, 8112, unm, uty, dnm, localDefnsDir, defnsDir)))
    {
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\"><a href=\"/central/servlet/BlogsOptions?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Options</a></dt></dl>";
    }

    s += "</div>";

    --hmenuCount[0];

    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void drawTitleW(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, PrintWriter out, String title, String service, String pageCode, boolean viewingBlogRoll, boolean editing, boolean htmlPage, String unm, String sid,
                        String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<table id='title' width=100%>");
    scoutln(out, bytesOut, "<tr><td colspan=2 nowrap><span id='x'>" + title);
    if(service.length() > 0)
      scoutln(out, bytesOut, directoryUtils.buildHelp(service));
    scoutln(out, bytesOut, "</span></td></tr></table>");

    int hmenuCount[] = new int[1];  hmenuCount[0] = 1;

    scoutln(out, bytesOut, authenticationUtils.createHorizSetupStringW());

    scoutln(out, bytesOut, "<table id='submenuX' width=100%><tr><td>" + buildSubMenuTextW(con, stmt, rs, req, pageCode, viewingBlogRoll, editing, htmlPage, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount)
                         + "</td></tr></table>");

    --hmenuCount[0];

    scoutln(out, bytesOut, "<script language='JavaScript'>setup(" + hmenuCount[0] + ",'Y');</script>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String buildSubMenuTextW(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, String pageCode, boolean viewingBlogRoll, boolean editing, boolean htmlPage, String unm, String sid, String uty, String men,
                                   String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] hmenuCount) throws Exception
  {
    String s = "<div id='submenu'>";

    if(editing)
    {
      if(! htmlPage)
        s += "<script language='JavaScript'>function go8103w(option){var d=document.getElementById('wikiview');if(d!=null)document.getElementById('p1').value=d.contentWindow.document.body.innerHTML;document.getElementById('stay').value=option;";
      else // htmlPage
      {
        s += "<script language='JavaScript'>function go8103w(option){document.getElementById('p1').value=document.forms['form8103'].text1.value;document.getElementById('stay').value=option;";
      }

      s += "postForm('BlogsSaveWave','form8103');}</script>";

      s += "<input type='hidden' name='unm' value='" + unm + "'>";
      s += "<input type='hidden' name='sid' value='" + sid + "'>";
      s += "<input type='hidden' name='uty' value='" + uty + "'>";
      s += "<input type='hidden' name='men' value='" + men + "'>";
      s += "<input type='hidden' name='den' value='" + den + "'>";
      s += "<input type='hidden' name='dnm' value='" + dnm + "'>";
      s += "<input type='hidden' name='bnm' value='" + bnm + "'>";
      s += "<input type='hidden' id='p1' name='p1' value=''>";
      s += "<input type='hidden' id='stay' name='stay' value=''>\n";
    }

    if(pageCode.length() > 0 && editing)
    {
      s += "<dl><dt onmouseover=\"setup(" + hmenuCount[0]++ + ",'N');\">";
      s += "<a href=\"javascript:getHTML('BlogsDeleteWave','&p1=" + pageCode + "')\">Delete</a></dt></dl>\n";
    }

    if(editing)
    {
      s += "<dl><dt onmouseover=\"setup(" + hmenuCount[0] + ",'N');\">Save</dt>";
      s += "<dd id='hmenu" + hmenuCount[0]++ + "'><ul>";
      s += "<li><a href=\"javascript:go8103w('Y')\">Save & Stay</a></li>";
      s += "<li><a href=\"javascript:go8103w('N')\">Save & Finish</a></li>";
      s += "</ul></dd></dl>";
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
  public void createTableBlogs(boolean dropTable, String dnm, String localDefnsDir, String defnsDir) throws Exception
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
        q = "DROP TABLE blogs";
        stmt.executeUpdate(q);
      }
    }
    catch(Exception e) { }
    if(stmt != null) stmt.close();

    try
    {
      stmt = con.createStatement();
      q = "CREATE TABLE blogs( Code integer not null, Owner char(20), Date date, Title char(100), Type char(1), Image char(100), TopicName char(40), Text mediumtext, Published char(1), IsAMenuItem char(1), IsTheHomePage char(1), "
        + "IsASpecial char(1), ShortTitle char(20), DisplayTheTitle char(1), ServiceCode char(20), IsSharable char(1), unique(Code))";
      stmt.executeUpdate(q);
    }
    catch(Exception e) { }
 
    if(stmt != null) stmt.close();

    try
    {
      stmt = con.createStatement();
      q = "CREATE INDEX dateInx on blogs(Date)";
      stmt.executeUpdate(q);
    }
    catch(Exception e) { }
 
    if(stmt != null) stmt.close();

    try
    {
      stmt = con.createStatement();
      q = "CREATE INDEX topicNameInx on blogs(TopicName)";
      stmt.executeUpdate(q);
    }
    catch(Exception e) { }

    if(stmt != null) stmt.close();

    try
    {
      stmt = con.createStatement();
      q = "CREATE INDEX ownerInx on blogs(Owner)";
      stmt.executeUpdate(q);
    }
    catch(Exception e) { }

    if(stmt != null) stmt.close();
    if(con  != null) con.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void createTableBlogTopics(boolean dropTable, String dnm) throws Exception
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
        q = "DROP TABLE blogtopics";
        stmt.executeUpdate(q);
      }
    }
    catch(Exception e) { }
    if(stmt != null) stmt.close();

    try
    {
      stmt = con.createStatement();
      q = "CREATE TABLE blogtopics ( TopicName char(40), Owner char(20), PublicTopic char(1) )";
      stmt.executeUpdate(q);
    }
    catch(Exception e) { }
    
    if(stmt != null) stmt.close();
    if(con  != null) con.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void showStats(PrintWriter out, String dnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    String userName = directoryUtils.getMySQLUserName();
    String passWord = directoryUtils.getMySQLPassWord();
    
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_info?user=" + userName + "&password=" + passWord);
    Statement stmt = con.createStatement();
   
    ResultSet rs = stmt.executeQuery("SELECT TABLE_NAME, CREATE_TIME, UPDATE_TIME, TABLE_ROWS FROM INFORMATION_SCHEMA.TABLES WHERE table_schema='" + dnm + "_info'");
    
    String tableName="", createDate="", numRows="", modifyDate="", cssFormat="";
    
    while(rs.next())                  
    {
      tableName = rs.getString(1);
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
  public boolean add(String dnm, String code, String owner, String date, String title, String type, String image, String topic, String text, String published, String isAMenuItem, String isTheHomePage, String isASpecial, String shortTitle,
                     String displayTheTitle, String serviceCode, String isSharable, String[] newCode) throws Exception
  {
    Connection con = null;
    Statement stmt = null;
    ResultSet rs   = null;

    try
    {
      String uName    = directoryUtils.getMySQLUserName();
      String passWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();
    
      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_info?user=" + uName + "&password=" + passWord);

      // if code is blank then is new
      // else is an update
      
      boolean isNew;
      
      // if this record is set as the homepage then need to reset any existing homepage
      if(isTheHomePage.equals("Y"))
      {
        stmt = con.createStatement();
        rs = stmt.executeQuery("SELECT Code FROM blogs WHERE IsTheHomePage = 'Y'"); 
        if(rs.next())
        {
          String existingHomePage = rs.getString(1);
          if(rs   != null) rs.close();
          if(stmt != null) stmt.close();
  
          stmt = con.createStatement();
          stmt.executeUpdate("UPDATE blogs SET IsTheHomePage = 'N' WHERE Code = '" + existingHomePage + "'");
          if(stmt != null) stmt.close();
        }  
      }
      
      if(code.length() == 0) // new rec
      {
        stmt = con.createStatement();

        // determine next code
        rs = stmt.executeQuery("SELECT Code FROM blogs ORDER BY Code DESC");
        if(rs.next())                  
        {
          int highestCode = generalUtils.strToInt(rs.getString(1));
          newCode[0]      = generalUtils.intToStr(highestCode + 1);
        }
        else newCode[0] = "1";
        
        if(rs   != null) rs.close();
        if(stmt != null) stmt.close();
        
        isNew = true;
      }
      else
      {
        stmt = con.createStatement();
        rs = stmt.executeQuery("SELECT Code FROM blogs WHERE Code='" + code + "'"); 
        if(rs.next())
          isNew = false;
        else 
        {
          isNew = true;
          newCode[0] = code;
        }

        if(rs   != null) rs.close();
        if(stmt != null) stmt.close();
      }

      String q;
      
      if(isNew)
      {
        q = "INSERT INTO blogs ( Code, Owner, Date, Title, Type, Image, TopicName, Text, Published, IsAMenuItem, IsTheHomePage, IsASpecial, ShortTitle, DisplayTheTitle, ServiceCode, IsSharable )"
          + "VALUES ('" + newCode[0] + "','" + owner + "',{d '" + generalUtils.convertDateToSQLFormat(date) + "'},'" + generalUtils.sanitiseForSQL(title) + "','" + type + "','" + image + "','" + generalUtils.sanitiseForSQL(topic) + "','" + generalUtils.sanitiseForSQL(text)
          + "','" + published + "','" + isAMenuItem + "','" + isTheHomePage + "','" + isASpecial + "','" + generalUtils.sanitiseForSQL(shortTitle) + "','" + displayTheTitle + "','" + serviceCode + "','" + isSharable + "')";
      }  
      else
      {
        newCode[0] = code;       
        q = "UPDATE blogs SET Owner = '" + owner + "', Date= {d '" + generalUtils.convertDateToSQLFormat(date) + "'}, Title = '" + generalUtils.sanitiseForSQL(title) + "', Type = '" + type + "', Image = '" + image + "', TopicName = '"
          + generalUtils.sanitiseForSQL(topic) + "', Text = '" + generalUtils.sanitiseForSQL(text) + "', Published = '" + published + "', IsAMenuItem = '" + isAMenuItem + "', IsTheHomePage = '" + isTheHomePage + "', IsASpecial = '" + isASpecial
          + "', ShortTitle = '" + generalUtils.sanitiseForSQL(shortTitle) + "', DisplayTheTitle = '" + displayTheTitle + "', ServiceCode = '" + serviceCode + "', IsSharable = '" + isSharable + "' WHERE Code = '" + code + "'";
      }
      
      stmt = con.createStatement();
      
      stmt.executeUpdate(q);

      if(stmt != null) stmt.close();
      if(con  != null) con.close();
      
      return true;
    }
    catch(Exception e)
    {
      System.out.println("blogsUtils: " + e);
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }
    
    return false;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void listBlogRoll(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String dnm, int[] bytesOut) throws Exception
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

      rsInfo = stmtInfo.executeQuery("SELECT DISTINCT Owner FROM blogs ORDER BY Owner");

      String userCode, userName, cssFormat = "";

      while(rsInfo.next())
      {
        userCode = rsInfo.getString(1);
        userName = authenticationUtils.getUserNameGivenUserCode(con, stmt, rs, userCode);

        if(cssFormat.equals("line2")) cssFormat = "line1"; else cssFormat = "line2";

        scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\">");

        scoutln(out, bytesOut, "<td width=180></td><td><p><a href=\"javascript:view('" + userCode + "')\">" + userName + "</a></td></tr>");
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
  public void listBlogs(PrintWriter out, String type, String owner, String unm, String dnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
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
  
      String s = "WHERE Type = '"  + type +  "'";
      
      if(unm.equals("Sysadmin"))
        ;
      else
      if(owner.length() > 0)
        s = " AND Owner = '" + owner + "'";
      
      String q = "SELECT Code, Owner, Date, Title, TopicName, HeaderName, FooterName, Width, TextType, Text FROM info " + s + " ORDER BY Code";

      rs = stmt.executeQuery(q);

      String cssFormat, code, date, title, topicName, headerName, footerName, width, textType, text;
      boolean line1 = true;

      while(rs.next())                  
      {
        code       = rs.getString(1);
        owner      = rs.getString(2);
        date       = rs.getString(3);
        title      = rs.getString(4);
        topicName  = rs.getString(5);
        headerName = rs.getString(6);
        footerName = rs.getString(7);
        width      = rs.getString(8);
        textType   = rs.getString(9);
        text       = rs.getString(10);
        
        if(line1) { cssFormat = "line1"; line1 = false; } else { cssFormat = "line2"; line1 = true; }
                    
        scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\">");

        scoutln(out, bytesOut, "<td align=center><p><a href=\"javascript:view('" + code + "','" + generalUtils.sanitise(text) + "','" + generalUtils.sanitise(title)
                             + "','" + width + "','" + generalUtils.sanitise(headerName) + "','" + footerName + "')\">" + code + "</a></td>");

        scoutln(out, bytesOut, "<td align=center><p>" + title + "</td>");
        scoutln(out, bytesOut, "<td align=center><p>" + date + "</td>");
        scoutln(out, bytesOut, "<td align=center><p>" + owner + "</td>");
        scoutln(out, bytesOut, "<td align=center><p>" + topicName + "</td>");
        scoutln(out, bytesOut, "<td align=center><p>" + headerName + "</td>");
        scoutln(out, bytesOut, "<td align=center><p>" + footerName + "</td>");
        scoutln(out, bytesOut, "<td align=center><p>" + width + "</td>");
        scoutln(out, bytesOut, "<td align=center><p>" + textType + "</td></tr>");
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
  public boolean getGivenCode(String code, String[] owner, String[] date, String[] title, String[] type, String[] image, String[] topic, String[] text, String[] published, String[] isAMenuItem, String[] isTheHomePage, String[] isASpecial,
                              String[] shortTitle, String[] displayTheTitle, String[] serviceCode, String[] isSharable, String dnm) throws Exception
  {
    boolean res = false;
    
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

      String q = "SELECT Owner, Date, Title, Type, Image, TopicName, Text, Published, IsAMenuItem, IsTheHomePage, IsASpecial, ShortTitle, DisplayTheTitle, ServiceCode, IsSharable FROM blogs WHERE Code = '" + code + "'";

      rs = stmt.executeQuery(q);

      if(rs.next())                  
      {
        owner[0]           = rs.getString(1);
        date[0]            = rs.getString(2);
        title[0]           = rs.getString(3);
        type[0]            = rs.getString(4);
        image[0]           = rs.getString(5);
        topic[0]           = rs.getString(6);
        text[0]            = rs.getString(7);
        published[0]       = rs.getString(8);
        isAMenuItem[0]     = rs.getString(9);
        isTheHomePage[0]   = rs.getString(10);
        isASpecial[0]      = rs.getString(11);
        shortTitle[0]      = rs.getString(12);
        displayTheTitle[0] = rs.getString(13);
        serviceCode[0]     = rs.getString(14);
        isSharable[0]      = rs.getString(15);
        
        res = true;
      }

      if(published[0]       == null) published[0]       = "N";
      if(isAMenuItem[0]     == null) isAMenuItem[0]     = "N";
      if(isTheHomePage[0]   == null) isTheHomePage[0]   = "N";
      if(isASpecial[0]      == null) isASpecial[0]      = "N";
      if(shortTitle[0]      == null) shortTitle[0]      = "";
      if(displayTheTitle[0] == null) displayTheTitle[0] = "N";
      if(serviceCode[0]     == null) serviceCode[0]     = "";
      if(isSharable[0]      == null) isSharable[0]      = "N";
      if(type[0]            == null) type[0]            = "";
      if(image[0]           == null) image[0]           = "";
      if(topic[0]           == null) topic[0]            = "";
      if(text[0]            == null) text[0]            = "";
      
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
  public boolean getGivenServiceCode(String serviceCode, String[] owner, String[] date, String[] title, String[] type, String[] image, String[] topic, String[] text, String[] published, String[] isAMenuItem, String[] isTheHomePage,
                                     String[] isASpecial, String[] shortTitle, String[] displayTheTitle, String[] code, String[] isSharable, String dnm) throws Exception
  {
    boolean res = false;
    
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

      String q = "SELECT Owner, Date, Title, Type, Image, TopicName, Text, Published, IsAMenuItem, IsTheHomePage, IsASpecial, ShortTitle, DisplayTheTitle, Code, IsSharable FROM blogs WHERE ServiceCode = '" + serviceCode + "'";

      rs = stmt.executeQuery(q);

      if(rs.next())                  
      {
        owner[0]           = rs.getString(1);
        date[0]            = rs.getString(2);
        title[0]           = rs.getString(3);
        type[0]            = rs.getString(4);
        image[0]           = rs.getString(5);
        topic[0]           = rs.getString(6);
        text[0]            = rs.getString(7);
        published[0]       = rs.getString(8);
        isAMenuItem[0]     = rs.getString(9);
        isTheHomePage[0]   = rs.getString(10);
        isASpecial[0]      = rs.getString(11);
        shortTitle[0]      = rs.getString(12);
        displayTheTitle[0] = rs.getString(13);
        code[0]            = rs.getString(14);
        isSharable[0]      = rs.getString(15);
        
        res = true;
      }

      if(published[0]       == null) published[0]       = "N";
      if(isAMenuItem[0]     == null) isAMenuItem[0]     = "N";
      if(isTheHomePage[0]   == null) isTheHomePage[0]   = "N";
      if(isASpecial[0]      == null) isASpecial[0]      = "N";
      if(shortTitle[0]      == null) shortTitle[0]      = "";
      if(displayTheTitle[0] == null) displayTheTitle[0] = "N";
      if(code[0]            == null) code[0]            = "";
      if(isSharable[0]      == null) isSharable[0]      = "";
      
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
  public void getHeaderOrFooterText(Connection con, Statement stmt, ResultSet rs, String which, String name, String[] text) throws Exception
  {
    text[0] = "";
    
    try
    {
      stmt = con.createStatement();

      String q = "SELECT Text FROM " + which + " WHERE " + generalUtils.capitalize(which) + "Code = '" + generalUtils.sanitiseForSQL(name) + "'";

      rs = stmt.executeQuery(q);

      if(rs.next())                  
        text[0] = rs.getString(1);

      if(rs   != null) rs.close();                                 
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(rs   != null) rs.close();                                 
      if(stmt != null) stmt.close();
    }
  }  
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void createTableStyles(boolean dropTable, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    String userName = directoryUtils.getMySQLUserName();
    String passWord = directoryUtils.getMySQLPassWord();
    
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + userName + "&password=" + passWord);
    Statement stmt = con.createStatement();
    String q;
   
    try
    {
      if(dropTable)
      {
        q = "DROP TABLE styles";
        stmt.executeUpdate(q);
      }
    }
    catch(Exception e) { }
    if(stmt != null) stmt.close();

    try
    {
      stmt = con.createStatement();
      q = "CREATE TABLE styles ( StyleName char(30),  FontFace char(30),  FontSize integer,  FontColour char(30), FontStyle char(20), BgColour char(30),  BgImage char(100),  Alignment char(10) )";
      stmt.executeUpdate(q);
    }
    catch(Exception e) { }

    if(stmt != null) stmt.close();

    try
    {
      stmt = con.createStatement();
      q = "INSERT INTO styles ( StyleName, FontFace, FontSize, FontColour, FontStyle, BgColour, BgImage, Alignment ) VALUES ('Default', 'Sans-Serif', '12', 'black', 'Normal', '#F0F0F0', '', 'Left' )";
          
      stmt.executeUpdate(q);
    }
    catch(Exception e)
    {
      System.out.println(e);
    }
        
    if(stmt != null) stmt.close();
    if(con  != null) con.close();
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void getStyle(String dnm, String styleName, String localDefnsDir, String defnsDir, String[] fontFace, String[] fontSize, String[] fontColour, String[] fontStyle, String[] bgColour, String[] bgImage, String[] alignment) throws Exception
  {
    Connection con  = null;
    Statement  stmt = null;
    ResultSet  rs   = null;
    
    try
    {
      String uName    = directoryUtils.getMySQLUserName();
      String passWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();
    
      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + uName + "&password=" + passWord);

      stmt = con.createStatement();
  
      String q = "SELECT * FROM styles WHERE StyleName = '" + styleName + "'";

      rs = stmt.executeQuery(q);

      if(rs.next())                  
      {
        fontFace[0]   = rs.getString(2);
        fontSize[0]   = rs.getString(3);
        fontColour[0] = rs.getString(4);
        fontStyle[0]  = rs.getString(5);
        bgColour[0]   = rs.getString(6);
        bgImage[0]    = rs.getString(7);
        alignment[0]  = rs.getString(8);
      }

      if(rs   != null) rs.close();                                 
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }
  }  
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean getHomePage(String[] owner, String[] date, String[] title, String[] type, String[] image, String[] topic, String[] text, String[] published, String[] isAMenuItem, String[] isASpecial, String[] shortTitle,
                             String[] displayTheTitle, String[] isSharable, String[] code, String dnm) throws Exception
  {
    boolean res = false;
    
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

      String q = "SELECT Code, Owner, Date, Title, Type, Image, TopicName, Text, Published, IsAMenuItem, IsASpecial, ShortTitle, DisplayTheTitle, IsSharable FROM blogs WHERE IsTheHomePage = 'Y'";

      rs = stmt.executeQuery(q);

      if(rs.next())                  
      {
        code[0]            = rs.getString(1);
        owner[0]           = rs.getString(2);
        date[0]            = rs.getString(3);
        title[0]           = rs.getString(4);
        type[0]            = rs.getString(5);
        image[0]           = rs.getString(6);
        topic[0]           = rs.getString(7);
        text[0]            = rs.getString(8);
        published[0]       = rs.getString(9);
        isAMenuItem[0]     = rs.getString(10);
        isASpecial[0]      = rs.getString(11);
        shortTitle[0]      = rs.getString(12);
        displayTheTitle[0] = rs.getString(13);
        isSharable[0]      = rs.getString(14);
        
        res = true;
      }

      if(published[0]       == null) published[0]       = "N";
      if(isAMenuItem[0]     == null) isAMenuItem[0]     = "N";
      if(isASpecial[0]      == null) isASpecial[0]      = "N";
      if(shortTitle[0]      == null) shortTitle[0]      = "";
      if(displayTheTitle[0] == null) displayTheTitle[0] = "N";
      if(type[0]            == null) type[0]            = "";
      if(isSharable[0]      == null) isSharable[0]      = "N";

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

}
