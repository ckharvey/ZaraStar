// =======================================================================================================================================================================================================
// System: ZaraStar Fax: Utilities
// Module: FaxUtils.java
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

public class FaxUtils
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

    scoutln(out, bytesOut, authenticationUtils.buildScreen(con, stmt, rs, req, true, unm, sid, uty, men, den, dnm, bnm, callingServlet, subMenuText, hmenuCount[0], "", otherSetup, localDefnsDir, defnsDir));
    
    scoutln(out, bytesOut, "<table border=0><tr><td valign=top><div id='second' style=\"{margin-top:0px;}\"></div></td>");
    scoutln(out, bytesOut, "<td valign=top><div id='third' style=\"{margin-top:0px;margin-left:0px;}\"></div></td></tr></table>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String buildSubMenuText(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] hmenuCount)
                                  throws Exception
  {
    String s = "<div id='submenu'>";

    hmenuCount[0] = 1;            

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 11000, unm, uty, dnm, localDefnsDir, defnsDir))
    {    
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\">";
      s += "<a href=\"/central/servlet/FaxServices?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Outgoing</a></dt></dl>";

      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0] + "');\">History</dt>";
      s += "<dd id='hmenu" + hmenuCount[0]++ + "'><ul>";
      s += "<li><a href=\"/central/servlet/FaxHistory?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=U&bnm=" + bnm + "\">My Faxes</a></li>";
      s += "<li><a href=\"/central/servlet/FaxHistory?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=A&bnm=" + bnm + "\">All Faxes</a></li>";
      s += "</ul></dd></dl>";

      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\">";
      s += "<a href=\"/central/servlet/FaxCompose?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Compose</a></dt></dl>";
    }
    
    if(serverUtils.isSysAdmin(con, stmt, rs, unm, dnm, sid, uty, localDefnsDir, defnsDir) || serverUtils.isDBAdmin(con, stmt, rs, unm))
    {
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\">";
      s += "<a href=\"/central/servlet/FaxOptions?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Options</a></dt></dl>";
    }

    if(serverUtils.isSysAdmin(con, stmt, rs, unm, dnm, sid, uty, localDefnsDir, defnsDir) || serverUtils.isDBAdmin(con, stmt, rs, unm))
    {
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\">";
      s += "<a href=\"http://" + men + "/central/servlet/_7505?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Access</a></dt></dl>";
    }

    s += "</div>";
    
    --hmenuCount[0];
    
    return s;
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
  public boolean addToFaxed(String docCode, String docType, String number, String person, String company, String companyCode, String companyType, String subject, String coverSheet, String comments, String text, String senderPhone,
                            String senderFax, String senderName, String senderCompany, String unm, String dnm, String localDefnsDir, String defnsDir, String[] faxCode) throws Exception
  {
    Connection con = null;
    Statement stmt = null;
    ResultSet rs   = null;

    try
    {
      String uName    = directoryUtils.getMySQLUserName();
      String passWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();
    
      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_fax?user=" + uName + "&password=" + passWord);

      stmt = con.createStatement();
      
      // determine next docCode      
     
      rs = stmt.executeQuery("SELECT FaxCode FROM faxed ORDER BY FaxCode DESC");
      if(rs.next())
        faxCode[0] = generalUtils.intToStr(generalUtils.strToInt(rs.getString(1)) + 1);
      else faxCode[0] = "1";
        
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
        
      stmt = con.createStatement();

      if(number == null)        number = "0";
      if(person == null)        person = "";
      if(company == null)       company = "";
      if(docType == null)       docType = "";
      if(docCode == null)       docCode = "";
      if(subject == null)       subject = "";
      if(comments == null)      comments = "";
      if(text == null)          text = "";
      if(senderPhone == null)   senderPhone = "0";
      if(senderFax == null)     senderFax = "0";
      if(senderCompany == null) senderCompany = "";
      if(senderName == null)    senderName = "";
      if(companyCode == null)   companyCode = "";
      if(companyType == null)   companyType = "";
        
      String q = "INSERT INTO faxed ( FaxCode, DateTime, Number, Person, Company, SignOn, DocumentType, DocumentCode, HylafaxID, Subject, Comments, "
               + "CoverSheet, Text, SenderPhone, SenderFax, SenderCompany, SenderName, Status, CompanyCode, CompanyType) "
               + "VALUES ('" + faxCode[0] + "',NULL,'" + number + "','" + generalUtils.sanitiseForSQL(person) + "','" + generalUtils.sanitiseForSQL(company)
               + "','" + unm + "','" + docType + "','" + docCode + "','0','" + generalUtils.sanitiseForSQL(subject) + "','"
               + generalUtils.sanitiseForSQL(comments) + "','" + coverSheet + "','" + generalUtils.sanitiseForSQL(text) + "','" + senderPhone + "','"
               + senderFax + "','" + senderCompany + "','" + senderName + "','','" + companyCode + "','" + companyType + "')";

      stmt.executeUpdate(q);
        
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
  public void createTableFaxed(boolean dropTable, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    String userName = directoryUtils.getMySQLUserName();
    String passWord = directoryUtils.getMySQLPassWord();
    
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_fax?user=" + userName + "&password=" + passWord);
    Statement stmt = con.createStatement();
    String q;
   
    try
    {
      if(dropTable)
      { 
        q = "DROP TABLE faxed";
        stmt.executeUpdate(q);
      }
    }
    catch(Exception e) { }
    if(stmt != null) stmt.close();

    try
    {
      stmt = con.createStatement();
      q = "CREATE TABLE faxed( FaxCode integer not null, DateTime Timestamp,    Number char(60),       Person char(60),      Company char(60), "
                            + "SignOn char(20),          DocumentType char(1),  DocumentCode char(20), HylafaxID integer,    Subject char(60), "
                            + "Comments varchar(500),    CoverSheet char(1),    Text mediumtext,       SenderPhone char(60), SenderFax char(60), "
                            + "SenderCompany char(60),   SenderName char(60),   Status char(80),       CompanyCode char(20), CompanyType char(1), "
                            + "unique(faxCode))";
      stmt.executeUpdate(q);
    }
    catch(Exception e) { }

    if(stmt != null) stmt.close();

    try
    {
      stmt = con.createStatement();
      q = "CREATE INDEX companyInx on faxed(Company)";

      stmt.executeUpdate(q);
    }
    catch(Exception e) { }

    if(stmt != null) stmt.close();

    try
    {
      stmt = con.createStatement();
      q = "CREATE INDEX companyCodeInx on faxed(CompanyCode)";

      stmt.executeUpdate(q);
    }
    catch(Exception e) { }

    if(stmt != null) stmt.close();

    try
    {
      stmt = con.createStatement();
      q = "CREATE INDEX hylafaxIDInx on faxed(HylafaxID)";

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
    
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_fax?user=" + userName + "&password=" + passWord);
    Statement stmt = con.createStatement();
   
    ResultSet rs = stmt.executeQuery("SELECT TABLE_NAME, CREATE_TIME, UPDATE_TIME, TABLE_ROWS FROM INFORMATION_SCHEMA.TABLES where table_schema='" + dnm + "_fax'");
    
    String tableName="", createDate="", numRows="", modifyDate="", cssFormat="";
    
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
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean updateFaxed(String faxCode, String hylafaxID, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    Connection con = null;
    Statement stmt = null;
    
    try
    {
      String uName    = directoryUtils.getMySQLUserName();
      String passWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();
    
      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_fax?user=" + uName + "&password=" + passWord);

      stmt = con.createStatement();
        
      String q = "UPDATE faxed SET HylafaxID = '" + hylafaxID + "' WHERE FaxCode = '" + faxCode + "'";

      stmt.executeUpdate(q);
        
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
  public void getDetailsGivenHylafaxCode(String hylafaxID, String[] faxCode, String[] dateTime, String[] number, String[] company, String[] signOn, String[] documentType, String[] documentCode, String[] companyCode, String[] companyType,
                                         String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    Connection con = null;
    Statement stmt = null;
    ResultSet rs = null;

    faxCode[0] = dateTime[0] = number[0] = company[0] = signOn[0] = documentType[0] = documentCode[0] = companyCode[0] = companyType[0] = "";

    try
    {
      String uName    = directoryUtils.getMySQLUserName();
      String passWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();
    
      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_fax?user=" + uName + "&password=" + passWord);

      stmt = con.createStatement();
  
      String q = "SELECT FaxCode, DateTime, Number, Company, SignOn, DocumentType, DocumentCode, CompanyCode, CompanyType FROM faxed WHERE HylafaxID ='" + hylafaxID + "'";

      rs = stmt.executeQuery(q);

      if(rs.next()) // just-in-case                  
      {
        faxCode[0]      = rs.getString(1);
        dateTime[0]     = rs.getString(2);
        number[0]       = rs.getString(3);
        company[0]      = rs.getString(4);
        signOn[0]       = rs.getString(5);
        documentType[0] = rs.getString(6);
        documentCode[0] = rs.getString(7);
        companyCode[0]  = rs.getString(8);
        companyType[0]  = rs.getString(9);
      }

      if(company[0]     == null) company[0] = "";
      if(companyCode[0] == null) companyCode[0] = "";

      if(documentType[0] == null || documentType[0].length() == 0)
        documentType[0] = " ";

      dateTime[0] = generalUtils.convertFromTimestamp(dateTime[0]);
      if(dateTime[0].length() > 3)
      {
        int len = dateTime[0].length() - 3;
        dateTime[0] = dateTime[0].substring(0, len);
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

}
