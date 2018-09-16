// =======================================================================================================================================================================================================
// System: ZaraStar Mail: Utilities
// Module: MailUtils.java
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
import javax.servlet.http.*;
import java.sql.*;

public class MailUtils
{
  GeneralUtils generalUtils = new GeneralUtils();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  DashboardUtils dashboardUtils = new DashboardUtils();
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void outputPageFrame(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, boolean fromSignOnPage, String account, String bodyStr, String callingServlet, String service, String unm, String sid,
                              String uty, String men, String den, String dnm, String bnm, String otherSetup, String localDefnsDir, String defnsDir, int[] hmenuCount, int[] bytesOut) throws Exception
  {
    String subMenuText = dashboardUtils.buildSubMenuText(con, stmt, rs, req, service, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount);

    scoutln(out, bytesOut, authenticationUtils.buildScreen(con, stmt, rs, req, true, unm, sid, uty, men, den, dnm, bnm, callingServlet, subMenuText, hmenuCount[0], "" ,"", localDefnsDir, defnsDir));
    
    scoutln(out, bytesOut, "<table border=0><tr><td valign=top><div id='second' style=\"{margin-top:0px;}\"></div></td>");
    scoutln(out, bytesOut, "<td valign=top><div id='third' style=\"{margin-top:0px;margin-left:0px;}\"></div></td></tr></table>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void drawTitle(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, PrintWriter out, boolean fromSignOnPage, String account, String title, String service, String unm, String sid, String uty, String men, String den,
                        String dnm, String bnm, String localDefnsDir, String defnsDir, int[] hmenuCount, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<table id='title' width=100%>");

    scoutln(out, bytesOut, "<tr><td colspan=2 nowrap><p>" + title + directoryUtils.buildHelp(service) + "</td></tr></table>");
    
    scoutln(out, bytesOut, "<table id='submenu' width=100%><tr><td>" + buildSubMenuText(con, stmt, rs, req, fromSignOnPage, account, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount) + "</td></tr></table>");
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String buildSubMenuText(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, boolean fromSignOnPage, String account, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                                  String localDefnsDir, String defnsDir, int[] hmenuCount) throws Exception
  {
    String s = "<div id='submenu'>";

    hmenuCount[0] = 1;

    if(! fromSignOnPage)
    {
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\">";
      s += "<a href=\"/central/servlet/MailZaraListMail?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=" + generalUtils.sanitise(account) + "&bnm=" + bnm + "\">Inbox</a></dt></dl>";
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 8801, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\">";
      s += "<a href=\"/central/servlet/ContactsAddressBook?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Address Book</a></dt></dl>";
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 8013, unm, uty, dnm, localDefnsDir, defnsDir) || authenticationUtils.verifyAccess(con, stmt, rs, req, 8022, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\">";
      s += "<a href=\"/central/servlet/MailZaraOptions?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Options</a></dt></dl>";
    }

    s += "</div>";

    --hmenuCount[0];

    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void showStats(PrintWriter out, String dnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    String userName = directoryUtils.getMySQLUserName();
    String passWord = directoryUtils.getMySQLPassWord();
    
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_mail?user=" + userName + "&password=" + passWord);
    Statement stmt = con.createStatement();
   
    ResultSet rs = stmt.executeQuery("SELECT TABLE_NAME, CREATE_TIME, UPDATE_TIME, TABLE_ROWS FROM INFORMATION_SCHEMA.TABLES where table_schema='" + dnm + "_mail'");
    
    String tableName, createDate, numRows, modifyDate, cssFormat="";
    
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
  public void createTableMail(boolean dropTable, String dnm) throws Exception
  {
    String userName = directoryUtils.getMySQLUserName();
    String passWord = directoryUtils.getMySQLPassWord();
    
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_mail?user=" + userName + "&password=" + passWord);
    Statement stmt = con.createStatement();
    String q;
   
    try
    {
      if(dropTable)
      { 
        q = "DROP TABLE mail";
        stmt.executeUpdate(q);
      }
    }
    catch(Exception e) { }
    if(stmt != null) stmt.close();

    try
    {
      stmt = con.createStatement();
      q = "CREATE TABLE mail ( MsgCode integer not null, SentOrReceived char(1), AddrFrom varchar(255), AddrTo varchar(255), Date date, Subject varchar(255), Attachments mediumtext, Text mediumtext, CCList mediumtext, BCCList mediumtext, "
                            + "CompanyCode char(20),     ProjectCode char(20),   CompanyType char(1),   SendType char(30),   unique(MsgCode) )";

      stmt.executeUpdate(q);
    }
    catch(Exception e) { }

    if(stmt != null) stmt.close();

    try
    {
      stmt = con.createStatement();
      q = "CREATE INDEX fromInx on mail(AddrFrom)";

      stmt.executeUpdate(q);
    }
    catch(Exception e) { }

    if(stmt != null) stmt.close();

    try
    {
      stmt = con.createStatement();
      q = "CREATE INDEX subjectInx on mail(SendType)";
  
      stmt.executeUpdate(q);
    }
    catch(Exception e) { }

    if(stmt != null) stmt.close();

    try
    {
      stmt = con.createStatement();
      q = "CREATE INDEX toInx on mail(AddrTo)";
  
      stmt.executeUpdate(q);
    }
    catch(Exception e) { }

    if(stmt != null) stmt.close();

    try
    {
      stmt = con.createStatement();
      q = "CREATE INDEX dateInx on mail(Date)";
  
      stmt.executeUpdate(q);
    }
    catch(Exception e) { }

    if(stmt != null) stmt.close();

    try
    {
      stmt = con.createStatement();
      q = "CREATE INDEX companyCodeInx on mail(CompanyCode)";
  
      stmt.executeUpdate(q);
    }
    catch(Exception e) { }

    if(stmt != null) stmt.close();
    if(con  != null) con.close();
  }
 
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void createTableMailAccounts(boolean dropTable, String dnm) throws Exception
  {
    String userName = directoryUtils.getMySQLUserName();
    String passWord = directoryUtils.getMySQLPassWord();
    
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_mail?user=" + userName + "&password=" + passWord);
    Statement stmt = con.createStatement();
    String q;
   
    try
    {
      if(dropTable)
      { 
        q = "DROP TABLE mailaccounts";

        stmt.executeUpdate(q);
      }
    }
    catch(Exception e) { }
    if(stmt != null) stmt.close();

    try
    {
      stmt = con.createStatement();
      q = "CREATE TABLE mailaccounts ( AccountName char(40), Owner char(20), Server char(60), EMailAddress char(80), UserName char(80), PassWord char(40), DisplayName char(100), Type char(10), "
                                    + "unique(AccountName) )";
      stmt.executeUpdate(q);
    }
    catch(Exception e) { }

    if(stmt != null) stmt.close();
    if(con  != null) con.close();
  }
 
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean addToMail(String msgCode, String sentOrReceived, String addrFrom, String addrTo, String subject, String attachments, String text, String ccList, String bccList, String companyCode, String companyType, String projectCode,
                           String date, String sendType, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    Connection con = null;
    Statement stmt = null;
    ResultSet rs   = null;

    try
    {
      String uName    = directoryUtils.getMySQLUserName();
      String passWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();
    
      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_mail?user=" + uName + "&password=" + passWord);

      if(msgCode.length() > 0)
      {
        stmt = con.createStatement();
        
        String q = "UPDATE mail SET SentOrReceived = '" + sentOrReceived + "', AddrFrom = '" + generalUtils.sanitiseForSQL(addrFrom) + "', AddrTo = '" + generalUtils.sanitiseForSQL(addrTo) + "', Subject = '" + generalUtils.sanitiseForSQL(subject)
                + "', Attachments = '" + attachments + "', Text = '" + generalUtils.sanitiseForSQL(text) + "', CCList = '" + generalUtils.sanitiseForSQL(ccList) + "', BCCList = '" + generalUtils.sanitiseForSQL(bccList) + "', CompanyCode = '" + companyCode
                + "', CompanyType = '" + companyType + "', ProjectCode = '" + projectCode + "', SendType = '" + sendType + "', Date = {d '" + date + "'} WHERE MsgCode = '" + msgCode + "'";

        stmt.executeUpdate(q);
        
        if(stmt != null) stmt.close();        
      }
      else // create new rec
      {
        stmt = con.createStatement();
      
        // determine next docCode      
        rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM mail");
        int rowCount = 0;
        if(rs.next())
          rowCount = rs.getInt("rowcount");
        if(rs != null) rs.close();
        
        msgCode = generalUtils.intToStr(rowCount + 1);
      
        if(stmt != null) stmt.close();
        
        stmt = con.createStatement();
           
        String q = "INSERT INTO mail ( MsgCode, SentOrReceived, AddrFrom, AddrTo, Date, Subject, Attachments, Text, CCList, BCCList, CompanyCode, ProjectCode, CompanyType, SendType) VALUES ('" + msgCode + "','" + sentOrReceived + "','"
                 + generalUtils.sanitiseForSQL(addrFrom) + "','" + generalUtils.sanitiseForSQL(addrTo) + "',{d'" + date + "'},'" + generalUtils.sanitiseForSQL(subject) + "','" + attachments + "','" + generalUtils.sanitiseForSQL(text) + "','" + generalUtils.sanitiseForSQL(ccList)
                 + "','" + generalUtils.sanitiseForSQL(bccList) + "','" + companyCode + "','" + projectCode + "','" + companyType + "','" + sendType + "')";

        stmt.executeUpdate(q);
        
        if(stmt != null) stmt.close();        
      }
      
      if(con != null) con.close();
      
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
  public boolean getAccountDetails(String account, String unm, String dnm, String[] server, String[] fromAddress, String[] userName, String[] passWord, String[] userFullName, String[] type) throws Exception
  {
    boolean res = false;
    Connection con  = null;
    Statement  stmt = null;
    ResultSet  rs   = null;

    try
    {
      String uName = directoryUtils.getMySQLUserName();
      String pWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();
    
      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_mail?user=" + uName + "&password=" + pWord);

      stmt = con.createStatement();
  
      rs = stmt.executeQuery("SELECT * FROM mailaccounts WHERE AccountName = '" + account + "' AND Owner = '" + unm + "'");

      if(rs.next())                  
      {
        server[0]       = rs.getString(3);
        fromAddress[0]  = rs.getString(4);
        userName[0]     = rs.getString(5);
        passWord[0]     = rs.getString(6);
        userFullName[0] = rs.getString(7);
        type[0]         = rs.getString(8);
      }
            
      if(rs != null) rs.close();
      if(stmt != null) stmt.close();        
      if(con  != null) con.close();
      
      res = true;
    }
    catch(Exception e)
    {
      System.out.println("mailUtils: " + e);
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }
    
    return res;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean updateMailAccounts(String account, String server, String address, String userName, String passWord, String displayName, String type, String unm, String dnm) throws Exception
  {
    boolean res = false;
    
    Connection con = null;
    Statement stmt = null;
    ResultSet rs   = null;

    try
    {
      String uName = directoryUtils.getMySQLUserName();
      String pWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();
    
      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_mail?user=" + uName + "&password=" + pWord);

      // check if rec already exists
      stmt = con.createStatement();
  
      rs = stmt.executeQuery("SELECT AccountName FROM mailaccounts WHERE AccountName= '" + generalUtils.sanitiseForSQL(account) + "' AND Owner = '" + unm  + "'"); 

      String s = null;
      if(rs.next())                  
        s = rs.getString(1);
      
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
      
      if(s != null) // already exists
      {
        stmt = con.createStatement();
        
        String q = "UPDATE mailaccounts SET Server = '" + generalUtils.sanitiseForSQL(server) + "', EMailAddress = '" + generalUtils.sanitiseForSQL(address) + "', UserName = '" + generalUtils.sanitiseForSQL(userName) + "', PassWord = '"
                 + generalUtils.sanitiseForSQL(passWord) + "', DisplayName = '" + generalUtils.sanitiseForSQL(displayName) + "', Type = '" + type + "' WHERE AccountName= '" + generalUtils.sanitiseForSQL(account) + "' AND Owner = '" + unm + "'";
        
        stmt.executeUpdate(q);
        
        if(stmt != null) stmt.close();        
      }
      else // create new rec
      {
        stmt = con.createStatement();
      
        String q = "INSERT INTO mailaccounts ( AccountName, Owner, Server, EMailAddress, UserName, PassWord, DisplayName, Type) VALUES ('" + generalUtils.sanitiseForSQL(account) + "','" + unm + "','" + generalUtils.sanitiseForSQL(server) + "','"
                 + generalUtils.sanitiseForSQL(address) + "','" + generalUtils.sanitiseForSQL(userName) + "','" + generalUtils.sanitiseForSQL(passWord) + "','" + generalUtils.sanitiseForSQL(displayName) + "','" + type + "')";

        stmt.executeUpdate(q);
        
        if(stmt != null) stmt.close();        
      }
      
      if(con  != null) con.close();
      
      res = true;
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }
    
    return res;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean deleteFromMailAccounts(String account, String unm, String dnm) throws Exception
  {
    boolean res = false;
    
    Connection con = null;
    Statement stmt = null;

    try
    {
      String uName = directoryUtils.getMySQLUserName();
      String pWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();
    
      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_mail?user=" + uName + "&password=" + pWord);

      stmt = con.createStatement();
        
      String q = "DELETE FROM mailaccounts WHERE AccountName= '" + generalUtils.sanitiseForSQL(account) + "' AND Owner = '" + unm  + "'"; 
        
      stmt.executeUpdate(q);
        
      if(stmt != null) stmt.close();        
      if(con  != null) con.close();
      
      res = true;
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }
    
    return res;
  }

}
