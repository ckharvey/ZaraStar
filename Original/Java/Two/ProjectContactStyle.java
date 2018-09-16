// =======================================================================================================================================================================================================
// System: ZaraStar: Project: Contact-style
// Module: ProjectContactStyle.java
// Author: C.K.Harvey
// Copyright (c) 2000-08 Christopher Harvey. All Rights Reserved.
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

public class ProjectContactStyle extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  ProjectUtils projectUtils = new ProjectUtils();
  
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="", p3="";

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
      p1  = req.getParameter("p1"); // searchChar
      p2  = req.getParameter("p2"); // option
      p3  = req.getParameter("p3"); // which
      
      if(p1 == null) p1 = " ";
      if(p2 == null) p2 = "M";
      if(p3 == null) p3 = "O";

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, p3, bytesOut);
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

      System.out.println("6820: " + e);
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "ProjectContactStyle", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6820, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1,
                    String p2, String p3, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    String uName = directoryUtils.getMySQLUserName();
    String pWord = directoryUtils.getMySQLPassWord();
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Connection projCon = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_project?user=" + uName + "&password=" + pWord);
    Statement stmt = null, stmt2 = null; 
    ResultSet rs   = null, rs2   = null; 

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 6820, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "ProjectContactStyle", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6820, bytesOut[0], 0, "ACC:" + p1);
      if(con     != null) con.close();
      if(projCon != null) projCon.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "ProjectContactStyle", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6820, bytesOut[0], 0, "SID:" + p1);
      if(con     != null) con.close();
      if(projCon != null) projCon.close();
      if(out != null) out.flush();
      return;
    }

    set(con, projCon, stmt, stmt2, rs, rs2, out, req, p1.charAt(0), p2, p3, unm, sid, uty, men, den, dnm, bnm, imagesDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 6820, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con     != null) con.close();
    if(projCon != null) projCon.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Connection projCon, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, HttpServletRequest req,
                   char searchChar, String option, String which, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                   String imagesDir, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Projects</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");
    
    scoutln(out, bytesOut, "function fetch(searchChar,option,which){");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/ProjectContactStyle?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm="
                         + dnm + "&p1=\"+searchChar+\"&p3=\"+which+\"&p2=\"+option;}");

    scoutln(out, bytesOut, "function proj(code){");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/ProjectMain?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm="
                         + dnm + "&bnm=" + bnm + "&p1=\"+code;}");

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4001, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function cust(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/CustomerPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm="
                           + dnm + "&bnm=" + bnm + "&p2=A&p1=\"+p1;}");
    }

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

    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\""
                         + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    int[] hmenuCount = new int[1];
    projectUtils.outputPageFrame(con, stmt, rs, out, req, "6820", "ProjectContactStyle", "", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount, bytesOut);

    String s;
    if(option.equals("M"))
    {
      if(which.equals("O"))
        s = "My Projects (Open)";
      else s = "My Projects (All)";
    }
    else
    {
      if(which.equals("O"))
        s = "All Projects (Open)";
      else s = "All Projects (All)";
    }

    projectUtils.drawTitle(con, stmt, rs, req, out, "ProjectContactStyle", s, "6820", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
 
    setHead(con, projCon, stmt, stmt2, rs, rs2, out, searchChar, option, which, unm, imagesDir, bytesOut);
  
    scoutln(out, bytesOut, "<table id='page' width=100% border=0>");
    
    scoutln(out, bytesOut, "<tr id='pageColumn'>");
    scoutln(out, bytesOut, "<td><p>Status</td>");
    scoutln(out, bytesOut, "<td><p>Project Code</td>");
    scoutln(out, bytesOut, "<td><p>Company Code</td>");
    scoutln(out, bytesOut, "<td><p>Company Name</td>");
    scoutln(out, bytesOut, "<td><p>Title</td>");
    scoutln(out, bytesOut, "<td><p>Value</td>");
    scoutln(out, bytesOut, "<td><p>Country</td>");
    scoutln(out, bytesOut, "<td><p>End-User</td>");
    scoutln(out, bytesOut, "<td><p>Delivery Date</td></tr>");

    if(searchChar == ' ') // first time
      ;
    else
    if(searchChar == '-')
      searchOthers(con, projCon, stmt, stmt2, rs, rs2, out, option, which, unm, bytesOut);
    else search(con, projCon, stmt, stmt2, rs, rs2, out, searchChar, option, which, unm, bytesOut);

    scoutln(out, bytesOut, "</table>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  // for all customer names that start with an 'A' (e.g.)
  //   pickup the customer code
  //   search for projects with that customer code
  private void search(Connection con, Connection projCon, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, char searchChar,
                      String option, String which, String unm, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();
 
    rs = stmt.executeQuery("SELECT CompanyCode, Name FROM company WHERE Name LIKE '" + searchChar + "%' ORDER BY Name");
    
    String cssFormat = "";

    String where = "";
    if(option.equals("M"))
      where += " AND Owner = '" + unm + "'";
      
    if(which.equals("O"))
      where += " AND Status = 'O'";
      
    while(rs.next())
    {    
      stmt2 = projCon.createStatement();

      rs2 = stmt2.executeQuery("SELECT ProjectCode, Title, Currency, QuotedValue, Country, EndUser, RequestedDeliveryDate, Status "
                             + "FROM projects WHERE CompanyCode = '" + rs.getString(1) + "'" + where);
      
      while(rs2.next())
      {    
        if(cssFormat.equals("line1")) cssFormat = "line2"; else cssFormat = "line1";
      
        writeBodyLine(out, rs.getString(1), rs.getString(2), rs2.getString(1), rs2.getString(2), rs2.getString(3), rs2.getString(4), rs2.getString(5),
                      rs2.getString(6), rs2.getString(7), rs2.getString(8), cssFormat, bytesOut);
      }

      if(rs2   != null) rs2.close();
      if(stmt2 != null) stmt2.close();
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  // for all customer names that do not start with an alpha
  //   pickup the customer code
  //   search for projects with that customer code
  private void searchOthers(Connection con, Connection projCon, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, String option,
                            String which, String unm, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();
 
    rs = stmt.executeQuery("SELECT CompanyCode, Name FROM company WHERE "
                         + "Name NOT LIKE 'A%' AND Name NOT LIKE 'B%' AND Name NOT LIKE 'C%' AND Name NOT LIKE 'D%' AND Name NOT LIKE 'E%' AND "
                         + "Name NOT LIKE 'F%' AND Name NOT LIKE 'G%' AND Name NOT LIKE 'H%' AND Name NOT LIKE 'I%' AND Name NOT LIKE 'J%' AND "
                         + "Name NOT LIKE 'K%' AND Name NOT LIKE 'L%' AND Name NOT LIKE 'M%' AND Name NOT LIKE 'N%' AND Name NOT LIKE 'O%' AND "
                         + "Name NOT LIKE 'P%' AND Name NOT LIKE 'Q%' AND Name NOT LIKE 'R%' AND Name NOT LIKE 'S%' AND Name NOT LIKE 'T%' AND "
                         + "Name NOT LIKE 'U%' AND Name NOT LIKE 'V%' AND Name NOT LIKE 'W%' AND Name NOT LIKE 'X%' AND Name NOT LIKE 'Y%' AND "
                         + "Name NOT LIKE 'Z%' ORDER BY Name");
    
    String cssFormat = "";

    String where = "";
    if(option.equals("M"))
      where += " AND Owner = '" + unm + "'";
      
    if(which.equals("O"))
      where += " AND Status = 'O'";
      
    while(rs.next())
    {    
      stmt2 = projCon.createStatement();
      rs2 = stmt2.executeQuery("SELECT ProjectCode, Title, Currency, QuotedValue, Country, EndUser, RequestedDeliveryDate, Status "
                             + "FROM projects WHERE CompanyCode = '" + rs.getString(1) + "'" + where);
      
      while(rs2.next())
      {    
        if(cssFormat.equals("line1")) cssFormat = "line2"; else cssFormat = "line1";
      
        writeBodyLine(out, rs.getString(1), rs.getString(2), rs2.getString(1), rs2.getString(2), rs2.getString(3), rs2.getString(4), rs2.getString(5),
                      rs2.getString(6), rs2.getString(7), rs2.getString(8), cssFormat, bytesOut);
      }

      if(rs2   != null) rs2.close();
      if(stmt2 != null) stmt2.close();
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void setHead(Connection con, Connection projCon, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, char searchChar,
                       String option, String which, String unm, String imagesDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<table id='page' width='100%' border='0' cellspacing='2' cellpadding='2'>");

    int[] count = new int[27];

    scoutln(out, bytesOut, "<tr><td colspan=20><p><b>By Company Name:</td></tr><tr>");

    searchCount(con, projCon, stmt, stmt2, rs, rs2, option, which, unm, count);

    for(int x=65;x<91;++x)
      writeHeaderLine(out, (char)x, option, which, count, bytesOut);

    scout(out, bytesOut, "<td align=center><p>");
    if(count[26] > 0)
      scout(out, bytesOut, "<a href=\"javascript:fetch('-','" + option + "','" + which + "')\">");
    scoutln(out, bytesOut, "Others</a></td></tr>");

    for(int x=65;x<92;++x)
      writeHeaderLine2(out, (char)x, count, bytesOut);
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=27><img src=\"" + imagesDir + "blm.gif\" width=100% height=18></td></tr>");
    
    if(searchChar != ' ')
    {
      String s;
      if(option.equals("M"))
      {
        if(which.equals("O"))
        {
          if(searchChar == '-')
            s = "My Open Projects for Customer Names starting with Non-Alphabetic";
          else s = "My Open Projects for Customer Names starting with " + searchChar;
        }
        else
        {
          if(searchChar == '-')
            s = "All My Projects for Customer Names starting with Non-Alphabetic";
          else s = "All My Projects for Customer Names starting with " + searchChar;
        }
      }
      else
      {
        if(which.equals("O"))
        {
          if(searchChar == '-')
            s = "All Open Projects for Customer Names starting with Non-Alphabetic";
          else s = "All Open Projects for Customer Names starting with " + searchChar;
        }
        else
        {
          if(searchChar == '-')
            s = "All Projects for Customer Names starting with Non-Alphabetic";
          else s = "All Projects for Customer Names starting with " + searchChar;
        }
      }
    
      scoutln(out, bytesOut, "<tr><td colspan=9><p>" + s + "</td></tr>");    
      scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    }

    scoutln(out, bytesOut, "</table>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void writeHeaderLine(PrintWriter out, char searchChar, String option, String which, int[] count, int[] bytesOut) throws Exception
  {
    int x = searchChar - 65;
    scout(out, bytesOut, "<td align=center width=4%><p>");
    
    if(count[x] > 0)
      scout(out, bytesOut, "<a href=\"javascript:fetch('" + searchChar + "','" + option + "','" + which + "')\">" + searchChar + "</a>");
    else scout(out, bytesOut, "" + searchChar);
    
    scoutln(out, bytesOut, "</td>");
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void writeHeaderLine2(PrintWriter out, char which, int[] count, int[] bytesOut) throws Exception
  {
    int x = which - 65;
    scout(out, bytesOut, "<td align=center width=4%><p>");
    
    if(count[x] > 0)
      scout(out, bytesOut, "" + count[x]);
    
    scoutln(out, bytesOut, "</td>");
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void writeBodyLine(PrintWriter out, String customerCode, String customerName, String projectCode, String title, String currency, String value, String country,
                             String endUser, String deliveryDate, String status, String cssFormat, int[] bytesOut) throws Exception
  {
         if(status.equals("O")) status = "Open";
    else if(status.equals("C")) status = "Completed";
    else if(status.equals("P")) status = "Proposed";
    else if(status.equals("R")) status = "Rejected";
    else if(status.equals("A")) status = "Abandoned";
    else if(status.equals("X")) status = "Cancelled";
    
    scoutln(out, bytesOut, "<tr id='" + cssFormat + "'><td nowrap><p>" + status + "</td>");
 
    scoutln(out, bytesOut, "<td><p><a href=\"javascript:proj('" + projectCode + "')\">" + projectCode + "</a></td><td><p>"
                         + "<a href=\"javascript:cust('" + customerCode + "')\">" + customerCode + "</a></td>");

    scoutln(out, bytesOut, "<td><p>" + customerName + "</td>");

    scoutln(out, bytesOut, "<td><p>" + title + "</td><td align=right><p>" + currency + " " + generalUtils.formatNumeric(value, '2') + "</td>");

    scoutln(out, bytesOut, "<td><p>" + country + "</td><td><p>" + endUser + "</td>");

    scoutln(out, bytesOut, "<td><p>" + generalUtils.convertFromYYYYMMDD(deliveryDate) + "</td></tr>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scout(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.print(str);
    bytesOut[0] += str.length();
  }
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void searchCount(Connection con, Connection projCon, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String option, String which,
                           String unm, int[] count) throws Exception
  {
    for(int x=0;x<27;++x)
      count[x] = 0;
    
    stmt = projCon.createStatement();
 
    String field = "", where = "WHERE";
    if(option.equals("M"))
      where += " Owner = '" + unm + "'";
          
    if(which.equals("O"))
    {
      if(! where.equals("WHERE")) // no longer
        where += " AND";
      where += " Status = 'O'";
    }

    if(where.equals("WHERE")) // still
      where = "";
    
    rs = stmt.executeQuery("SELECT CompanyCode FROM projects " + where + " ORDER BY CompanyCode");
    
    String thisCompanyCode, lastCompanyCode = "";
    
    while(rs.next())
    {  
      thisCompanyCode = rs.getString(1);
      
      if(! thisCompanyCode.equals(lastCompanyCode))
      {      
        stmt2 = con.createStatement();
        rs2   = stmt2.executeQuery("SELECT Name FROM company WHERE CompanyCode = '" + thisCompanyCode + "'");
      
        if(rs2.next())
        {    
          field = rs2.getString(1).toUpperCase();
      
          if(field.length() == 0 || field.charAt(0) < 65 || field.charAt(0) > 92)
            ++count[26];
          else ++count[(field.charAt(0) - 65)];
        }
        
        lastCompanyCode = thisCompanyCode;
      }
      else
      {
        if(field.length() == 0 || field.charAt(0) < 65 || field.charAt(0) > 92)
          ++count[26];
        else ++count[(field.charAt(0) - 65)];        
      }
      
      if(rs2   != null) rs2.close();
      if(stmt2 != null) stmt2.close();
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();    
  }

}
