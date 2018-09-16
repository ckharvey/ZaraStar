// =======================================================================================================================================================================================================
// System: ZaraStar Channels: Utilities
// Module: ChannelUtils.java
// Author: C.K.Harvey
// Copyright (c) 2006-08 Christopher Harvey. All Rights Reserved.
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

public class ChannelUtils
{
  GeneralUtils generalUtils = new GeneralUtils();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  DashboardUtils dashboardUtils = new DashboardUtils();

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void outputPageFrame(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String callingServlet, String bodyStr, String service, String unm, String sid, String uty, String men, String den, String dnm,
                              String bnm, String otherSetup, String localDefnsDir, String defnsDir, int[] hmenuCount, int[] bytesOut) throws Exception
  {
    String subMenuText = dashboardUtils.buildSubMenuText(con, stmt, rs, req, service, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount);
 
    scoutln(out, bytesOut, authenticationUtils.buildScreen(con, stmt, rs, req, true, unm, sid, uty, men, den, dnm, bnm, callingServlet, subMenuText, hmenuCount[0], bodyStr, otherSetup, localDefnsDir, defnsDir));
  }
    
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void drawTitle(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, PrintWriter out, String title, String service, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir,
                        String defnsDir, int[] hmenuCount, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<table id='title' width=100%>");
    scoutln(out, bytesOut, "<tr><td colspan=2 nowrap><p>" + title + directoryUtils.buildHelp(service) + "</td></tr></table>");

    scoutln(out, bytesOut, "<table id='submenu' width=100%><tr><td>" + buildSubMenuText(con, stmt, rs, req, service, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount) + "</td></tr></table>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String buildSubMenuText(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, String service, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir,
                                 int[] hmenuCount) throws Exception
  {
    String s = "<div id='submenu'>";

    hmenuCount[0] = 1;

    if((dnm.equals("101") && uty.equals("I")) ||
    authenticationUtils.verifyAccess(con, stmt, rs, req, 8801, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\">";
      s += "<a href=\"http://" + men + "/central/servlet/ContactsAddressBook?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm
        + "&bnm=" + bnm + "\">Contacts</a></dt></dl>";
    }

    if((dnm.equals("101") && uty.equals("I")) ||
    authenticationUtils.verifyAccess(con, stmt, rs, req, 12700, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\">";
      s += "<a href=\"http://" + men + "/central/servlet/IMMain?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm
        + "&bnm=" + bnm + "\">Connections</a></dt></dl>";
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 128, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\">";
      s += "<a href=\"http://" + men + "/central/servlet/CompanyPersonnelDirectory?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm
        + "&bnm=" + bnm + "\">Our Personnel</a></dt></dl>";
    }

    if(! uty.equals("A") && ! uty.equals("R") && authenticationUtils.verifyAccess(con, stmt, rs, req, 12700, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\">";
      s += "<a href=\"http://" + men + "/central/servlet/ContactsProfileView?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm
        + "&bnm=" + bnm + "\">My Profile</a></dt></dl>";
    }
    
    if((dnm.equals("101") && uty.equals("I")) ||
    authenticationUtils.verifyAccess(con, stmt, rs, req, 8014, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\">";
      s += "<a href=\"http://" + men + "/central/servlet/MailZaraSignOn?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm
        + "&bnm=" + bnm + "\">Mail</a></dt></dl>";
    }

    s += "</div>";
    
    --hmenuCount[0];
    
    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  public void createAnAccountsDB(String year, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    String userName = directoryUtils.getMySQLUserName();
    String passWord = directoryUtils.getMySQLPassWord();
    
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/?user=" + userName + "&password=" + passWord);
    Statement stmt = con.createStatement();
    String q;
   
    try
    {
      stmt = con.createStatement();
      q = "CREATE DATABASE " + dnm + "_accounts_" + year;
      stmt.executeUpdate(q);
    }
    catch(Exception e) { }

    if(stmt != null) stmt.close();
    if(con  != null) con.close(); 
  }  
    
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void showStats(PrintWriter out, String dnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    String userName = directoryUtils.getMySQLUserName();
    String passWord = directoryUtils.getMySQLPassWord();
    
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + userName + "&password="
                                                 + passWord);
    Statement stmt = con.createStatement();
   
    ResultSet rs = stmt.executeQuery("SELECT TABLE_NAME, CREATE_TIME, UPDATE_TIME, TABLE_ROWS FROM INFORMATION_SCHEMA.TABLES "
                                   + "where table_schema='" + dnm + "_ofsa'");

    String tableName="", createDate="", numRows="", modifyDate="", cssFormat="";
    
    while(rs.next())                  
    {
      tableName  = rs.getString(1);
      createDate = rs.getString(2);
      modifyDate = rs.getString(3);
      numRows    = rs.getString(4);
        
     if(cssFormat.equals("line1"))
         cssFormat = "line2";
      else cssFormat = "line1";

      scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\"><td>" + tableName + "</td>");
      scoutln(out, bytesOut, "<td align=center>" + numRows + "</td>");
      scoutln(out, bytesOut, "<td align=center>" + createDate + "</td>");
      scoutln(out, bytesOut, "<td align=center>" + modifyDate + "</td></tr>");
    }   
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    if(con  != null) con.close();
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public int listDataBases(String dnm, String[] dbNames, String localDefnsDir, String defnsDir) throws Exception
  {
    String userName = directoryUtils.getMySQLUserName();
    String passWord = directoryUtils.getMySQLPassWord();
    
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/?user=" + userName + "&password=" + passWord);
    Statement stmt = con.createStatement();
   
    ResultSet rs = stmt.executeQuery("SHOW DATABASES LIKE '" + dnm + "_accounts_%'");

    dbNames[0] = "";
    int count = 0;
    
    while(rs.next())                  
    {
      dbNames[0] += (rs.getString(1) + "\001");
      ++count;
    }   
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    if(con  != null) con.close();
    
    return count;
  }

}
