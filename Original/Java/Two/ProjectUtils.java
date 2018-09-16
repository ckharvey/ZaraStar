// =======================================================================================================================================================================================================
// System: ZaraStar Project: Utilities
// Module: ProjectUtils.java
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

public class ProjectUtils
{
  GeneralUtils generalUtils = new GeneralUtils();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  DashboardUtils dashboardUtils = new DashboardUtils();
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void outputPageFrame(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String service, String callingServlet, String bodyStr, String unm, String sid, String uty, String men, String den, String dnm,
                              String bnm, String otherSetup, String localDefnsDir, String defnsDir, int[] hmenuCount, int[] bytesOut) throws Exception
  {
    String subMenuText = dashboardUtils.buildSubMenuText(con, stmt, rs, req, service, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount);

    scoutln(out, bytesOut, authenticationUtils.buildScreen(con, stmt, rs, req, true, unm, sid, uty, men, den, dnm, bnm, callingServlet, subMenuText, hmenuCount[0], "", "", localDefnsDir, defnsDir));
    
    scoutln(out, bytesOut, "<table border=0><tr><td valign=top><div id='second' style=\"{margin-top:0px;}\"></div></td>");
    scoutln(out, bytesOut, "<td valign=top><div id='third' style=\"{margin-top:0px;margin-left:0px;}\"></div></td></tr></table>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String outputHorizontalMenu6800(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir,
                                          int[] hmenuCount) throws Exception
  {
    String s = "<div id='submenu'>";

    hmenuCount[0] = 1;            

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 6801, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\">";
      s += "<a href=\"/central/servlet/ProjectCreateEdit?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
        + "\">Create Project</a></dt></dl>";
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 6820, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\">";
      s += "<a href=\"/central/servlet/ProjectContactStyle?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
        + "\">Status</a></dt></dl>";
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 6804, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\">";
      s += "<a href=\"/central/servlet/ProjectTitleDefinition?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
        + "\">Titles</a></dt></dl>";
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 6811, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\">";
      s += "<a href=\"/central/servlet/_6811?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
        + "\">Options</a></dt></dl>";
    }

    s += "</div>";

    --hmenuCount[0];
    
    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String outputHorizontalMenu6801(int[] hmenuCount) throws Exception
  {
    String s = "<div id='submenu'>";

    hmenuCount[0] = 1;

    s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\">";
    s += "<a href=\"javascript:save()\">Save</a></dt></dl>";

    s += "</div>";

    --hmenuCount[0];
    
    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String outputHorizontalMenu6802(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, String unm, String sid, String uty, String men, String mainDNM, String dnm, String bnm, String localDefnsDir, String defnsDir,
                                          int[] hmenuCount) throws Exception
  {
    String s = "<div id='submenu'>";

    hmenuCount[0] = 1;

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 6801, unm, uty, dnm, localDefnsDir, defnsDir))
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\"><a href=\"javascript:details()\">Edit Details</a></dt></dl>";
    
    boolean ProjectOfficeTasks = authenticationUtils.verifyAccess(con, stmt, rs, req, 6803, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean _6805 = authenticationUtils.verifyAccess(con, stmt, rs, req, 6805, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean _6807 = authenticationUtils.verifyAccess(con, stmt, rs, req, 6807, unm, uty, dnm, localDefnsDir, defnsDir);
    if(ProjectOfficeTasks || _6805 || _6807)
    {
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0] + "');\">Tasks</dt><dd id='hmenu" + hmenuCount[0]++ + "'><ul>";
      if(ProjectOfficeTasks)
        s += "<li><a href=\"javascript:officetasks()\">Office</a></li>";
      if(_6805)
        s += "<li><a href=\"javascript:workshoptasks()\">Workshop</a></li>";
      if(_6807)
        s += "<li><a href=\"javascript:schedule()\">Schedule</a></li>";
      s += "</ul></dd></dl>\n";
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 6808, unm, uty, dnm, localDefnsDir, defnsDir))
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\"><a href=\"javascript:documents()\">Documents</a></dt></dl>";
   
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 6809, unm, uty, dnm, localDefnsDir, defnsDir))
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\"><a href=\"javascript:materials()\">Materials</a></dt></dl>";
   
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 6813, unm, uty, dnm, localDefnsDir, defnsDir))
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\"><a href=\"javascript:tests()\">Tests</a></dt></dl>";

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 6810, unm, uty, dnm, localDefnsDir, defnsDir))
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\"><a href=\"javascript:reports()\">Reports</a></dt></dl>";

    s += "</div>";

    --hmenuCount[0];
    
    return s;
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String outputHorizontalMenu6804(int[] hmenuCount) throws Exception
  {
    String s = "<div id='submenu'>";
    
    hmenuCount[0] = 1;

    s += "</div>";

    --hmenuCount[0];
    
    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void drawTitle(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, PrintWriter out, String callingServlet, String title, String service, String unm, String sid, String uty, String men, String den, String dnm,
                        String bnm, String localDefnsDir, String defnsDir, int[] hmenuCount, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<table id='title' width=100%>");
    scoutln(out, bytesOut, "<tr><td colspan=2 nowrap><p>" + title + directoryUtils.buildHelp(service) + "</td></tr></table>");

    scoutln(out, bytesOut, "<table id='submenu' width=100%><tr><td>" + buildSubMenuText(con, stmt, rs, req, callingServlet, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount) + "</td></tr></table>");
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String buildSubMenuText(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, String callingServlet, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir,
                                  String defnsDir, int[] hmenuCount) throws Exception
  {
    if(callingServlet.equals("ProjectList"))
      return outputHorizontalMenu6800(con, stmt, rs, req, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount);

    if(callingServlet.equals("ProjectCreateEdit"))
      return outputHorizontalMenu6801(hmenuCount);

    if(callingServlet.equals("ProjectTitleDefinition"))
      return outputHorizontalMenu6804(hmenuCount);

    return outputHorizontalMenu6802(con, stmt, rs, req, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // if file already exists in stated directory, that database record is updated (that is, a new record is NOT added) 
  public boolean updateProjects(String newOrEdit, String code, String title, String requestedDeliveryDate, String enquiryDate, String customerReference, String product, String note, String endUser, String contractor, String country,
                                String currency, String quotedValue, String remark, String dateOfPO, String dateIssuedToContracts, String status, String dateOfReview, String reviewedBy, String statedDeliveryDate, String companyCode, String owner,
                                String checkedBy, String dateCompleted, String dateIssuedToWorkshop, String dnm, String localDefnsDir, String defnsDir, String[] newCode) throws Exception
  {
    Connection con = null;
    Statement stmt = null;
    ResultSet rs   = null;

    try
    {
      String uName    = directoryUtils.getMySQLUserName();
      String passWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();
    
      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_project?user=" + uName + "&password=" + passWord);

      stmt = con.createStatement();

      if(newOrEdit.equals("E"))
      {
        String q = "UPDATE projects SET Title = '" + title + "', RequestedDeliveryDate = {d '" + requestedDeliveryDate + "' }, "
                 + "EnquiryDate = {d '" + enquiryDate + "' }, " 
                 + "DateIssuedToContracts = {d '" + dateIssuedToContracts + "' }, " 
                 + "DateOfPO = {d '" + dateOfPO + "' }, " 
                 + "DateOfReview = {d '" + dateOfReview + "' }, " 
                 + "StatedDeliveryDate = {d '" + statedDeliveryDate + "' }, " 
                 + "DateCompleted = {d '" + dateCompleted + "' }, " 
                 + "DateIssuedToWorkshop = {d '" + dateIssuedToWorkshop + "' }, "                 
                 + "CustomerReference = '" + customerReference + "', " 
                 + "Product = '" + product + "', " 
                 + "Note = '" + note + "', " 
                 + "EndUser = '" + endUser + "', " 
                 + "Contractor = '" + contractor + "', " 
                 + "Country = '" + country + "', " 
                 + "Currency = '" + currency + "', " 
                 + "QuotedValue = '" + quotedValue + "', " 
                 + "Remark = '" + remark + "', " 
                 + "Status = '" + status + "', " 
                 + "ReviewedBy = '" + reviewedBy + "', " 
                 + "CompanyCode = '" + companyCode + "', " 
                 + "CheckedBy = '" + checkedBy + "', "
                 + "Owner = '" + owner + "' "
                 + "WHERE ProjectCode = '" + code + "'";

        stmt.executeUpdate(q);
      }
      else // create new rec
      {
        // determine next projectCode      
        rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM projects");
        rs.next();
        int rowCount = rs.getInt("rowcount");
        rs.close();
        code = generalUtils.intToStr(rowCount + 1);
      
        if(stmt != null) stmt.close();
        
        stmt = con.createStatement();
           
        String q = "INSERT INTO projects ( ProjectCode, Title, RequestedDeliveryDate, EnquiryDate, CustomerReference, "
                                        + "Product, Note, EndUser, Contractor, Country, Currency, QuotedValue, Remark, DateOfPO, "
                                        + "DateIssuedToContracts, Status, DateOfReview, ReviewedBy, StatedDeliveryDate, "
                                        + "CompanyCode, Owner, CheckedBy, DateCompleted, DateIssuedToWorkshop ) VALUES ("
                                        + "'" + code + "', '" + title + "', {d '" + requestedDeliveryDate + "' }, "
                                        + "{d '" + enquiryDate + "' }, '" + customerReference + "', '" + product
                                        + "', '" + note + "', '" + endUser + "', '" + contractor + "', '" + country + "', '"
                                        + currency + "', '" + quotedValue + "', '" + remark + "', {d '" + dateOfPO + "' }, " 
                                        + "{d '" + dateIssuedToContracts + "' }, '" + status + "', {d '" + dateOfReview + "' }, "
                                        + "'" + reviewedBy + "', {d '" + statedDeliveryDate + "' }, '" + companyCode + "', '"
                                        + owner + "', '" + checkedBy + "', {d '" + dateCompleted + "' }, {d '" + dateIssuedToWorkshop + "' })";

        stmt.executeUpdate(q);
      }
      
      if(stmt != null) stmt.close();        
      if(con  != null) con.close();

      newCode[0] = code;

      return true;
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }
    
    newCode[0] = code;
    
    return false;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  public void listProjects(PrintWriter out, boolean openWanted, boolean completedWanted, boolean proposedWanted, boolean rejectedWanted,
                           boolean abandonedWanted, boolean cancelledWanted, String orderedBy, String dnm, String imagesDir, String localDefnsDir,
                           String defnsDir, int[] bytesOut) throws Exception
  {
    Connection con = null;
    Statement stmt = null;
    ResultSet rs   = null;

    try
    {
      String uName    = directoryUtils.getMySQLUserName();
      String passWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();

      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_project?user=" + uName + "&password=" + passWord);

      stmt = con.createStatement();

      String orderBy = "";

      switch(orderedBy.charAt(0))
      {
        case 'C' : orderBy = "ORDER BY CompanyCode, Country, ProjectCode"; break;
        case 'N' : orderBy = "ORDER BY ProjectCode";                       break;
        case 'D' : orderBy = "ORDER BY RequestedDeliveryDate";             break;
        case 'V' : orderBy = "ORDER BY QuotedValue";                       break;
        case 'O' : orderBy = "ORDER BY Country, CompanyCode, ProjectCode"; break;
        case 'E' : orderBy = "ORDER BY EndUser";                           break;
      }

      String q = "SELECT ProjectCode, CompanyCode, Title, Currency, QuotedValue, Country, EndUser, "
               + "RequestedDeliveryDate, DateCompleted, Status FROM projects " + orderBy;

      rs = stmt.executeQuery(q);

      String projectCode, companyCode, title, currency, value, country, endUser, dateCompleted, status,
             requestedDeliveryDate, cssFormat = "";

      while(rs.next())
      {
        projectCode     = rs.getString(1);
        companyCode     = rs.getString(2);
        title           = rs.getString(3);
        currency        = rs.getString(4);
        value           = rs.getString(5);
        country         = rs.getString(6);
        endUser         = rs.getString(7);
        requestedDeliveryDate = rs.getString(8);
        dateCompleted   = rs.getString(9);
        status          = rs.getString(10);

        boolean wanted = false;
        if(openWanted && status.equals("O"))
        {
          status = "Open";
          wanted = true;
        }
        else
        if(completedWanted && status.equals("C"))
        {
          status = "Completed";
          wanted = true;
        }
        else
        if(proposedWanted && status.equals("P"))
        {
          status = "Proposed";
          wanted = true;
        }
        else
        if(rejectedWanted && status.equals("R"))
        {
          status = "Rejected";
          wanted = true;
        }
        else
        if(abandonedWanted && status.equals("A"))
        {
          status = "Abandoned";
          wanted = true;
        }
        else
        if(cancelledWanted && status.equals("X"))
        {
          status = "Cancelled";
          wanted = true;
        }

        if(wanted)
        {
          if(cssFormat.equals("line2")) cssFormat = "line1"; else cssFormat = "line2";

          scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\">");
          scoutln(out, bytesOut, "<td align=center><p>" + status + "</td>");

          scoutln(out, bytesOut, "<td align=center><p><a href=\"javascript:fetch('" + projectCode + "')\">" + projectCode + "</td>");
          scoutln(out, bytesOut, "<td align=center><p>" + companyCode + "</td>");
          scoutln(out, bytesOut, "<td align=center><p>" + title + "</td>");
          scoutln(out, bytesOut, "<td align=center><p>" + currency + " " + value + "</td>");
          scoutln(out, bytesOut, "<td align=center><p>" + country + "</td>");
          scoutln(out, bytesOut, "<td align=center><p>" + endUser + "</td>");
          scoutln(out, bytesOut, "<td align=center><p>" + generalUtils.convertFromYYYYMMDD(requestedDeliveryDate) + "</td>");
          scoutln(out, bytesOut, "<td align=center><p>" + generalUtils.convertFromYYYYMMDD(dateCompleted) + "</td></tr>");
        }
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

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  public void listProjectsExternally(PrintWriter out, String reqdCompanyCode, String dnm, int[] bytesOut) throws Exception
  {
    Connection con = null;
    Statement stmt = null;
    ResultSet rs   = null;

    try
    {
      String uName    = directoryUtils.getMySQLUserName();
      String passWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();

      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_project?user=" + uName + "&password=" + passWord);

      stmt = con.createStatement();

      String q = "SELECT ProjectCode, Title, Country, EndUser, RequestedDeliveryDate, DateCompleted, Status FROM projects WHERE CompanyCode = '"
               + reqdCompanyCode + "' AND Status != 'X' ORDER BY ProjectCode";

      rs = stmt.executeQuery(q);

      String projectCode, title, country, endUser, dateCompleted, status, requestedDeliveryDate, cssFormat = "";

      while(rs.next())
      {
        projectCode           = rs.getString(1);
        title                 = rs.getString(2);
        country               = rs.getString(3);
        endUser               = rs.getString(4);
        requestedDeliveryDate = rs.getString(5);
        dateCompleted         = rs.getString(6);
        status                = rs.getString(7);

        if(status.equals("C")) status = "Completed"; else
        if(status.equals("P")) status = "Proposed";  else
        if(status.equals("R")) status = "Rejected";  else
        if(status.equals("A")) status = "Abandoned"; else
        if(status.equals("O")) status = "Open";

        if(cssFormat.equals("line2")) cssFormat = "line1"; else cssFormat = "line2";

        scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\">");

        scoutln(out, bytesOut, "<td align=center><p>" + status + "</td>");
        scoutln(out, bytesOut, "<td align=center><p><a href=\"javascript:fetch('" + projectCode + "')\">" + projectCode + "</a></td>");
        scoutln(out, bytesOut, "<td align=center><p>" + title + "</td>");
        scoutln(out, bytesOut, "<td align=center><p>" + country + "</td>");
        scoutln(out, bytesOut, "<td align=center><p>" + endUser + "</td>");
        scoutln(out, bytesOut, "<td align=center><p>" + generalUtils.convertFromYYYYMMDD(requestedDeliveryDate) + "</td>");
        scoutln(out, bytesOut, "<td align=center><p>" + generalUtils.convertFromYYYYMMDD(dateCompleted) + "</td></tr>");
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
  public String fetchProjectRec(String code, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    String[] title                 = new String[1];
    String[] requestedDeliveryDate = new String[1];
    String[] enquiryDate           = new String[1];
    String[] customerReference     = new String[1];
    String[] product               = new String[1];
    String[] note                  = new String[1];
    String[] endUser               = new String[1];
    String[] contractor            = new String[1];
    String[] country               = new String[1];
    String[] currency              = new String[1];
    String[] quotedValue           = new String[1];
    String[] remark                = new String[1];
    String[] dateOfPO              = new String[1];
    String[] dateIssuedToContracts = new String[1];
    String[] status                = new String[1];
    String[] dateOfReview          = new String[1];
    String[] reviewedBy            = new String[1];
    String[] statedDeliveryDate    = new String[1];
    String[] companyCode           = new String[1];
    String[] owner                 = new String[1];
    String[] checkedBy             = new String[1];
    String[] dateCompleted         = new String[1];
    String[] dateIssuedToWorkshop  = new String[1];
    
    fetchProjectRec(code, title, requestedDeliveryDate, enquiryDate, customerReference, product, note, endUser, contractor, country, currency, quotedValue, remark, dateOfPO, dateIssuedToContracts, status, dateOfReview, reviewedBy,
                    statedDeliveryDate, companyCode, owner, checkedBy, dateCompleted, dateIssuedToWorkshop, dnm, localDefnsDir, defnsDir);
    
    return companyCode[0];
  }
  public void fetchProjectRec(String code, String[] title, String[] requestedDeliveryDate, String[] enquiryDate, String[] customerReference, String[] product, String[] note, String[] endUser, String[] contractor, String[] country,
                              String[] currency, String[] quotedValue, String[] remark, String[] dateOfPO, String[] dateIssuedToContracts, String[] status, String[] dateOfReview, String[] reviewedBy, String[] statedDeliveryDate,
                              String[] companyCode, String[] owner, String[] checkedBy, String[] dateCompleted, String[] dateIssuedToWorkshop, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    Connection con = null;
    Statement stmt = null;
    ResultSet rs = null;
    
    title[0] = requestedDeliveryDate[0] = enquiryDate[0] = customerReference[0] = product[0] = note[0] = endUser[0] = contractor[0] = country[0] = currency[0] = quotedValue[0] = remark[0] = dateOfPO[0] = dateIssuedToContracts[0] = status[0]
             = dateOfReview[0] = reviewedBy[0] = statedDeliveryDate[0] = companyCode[0] = owner[0] = checkedBy[0] = dateCompleted[0] = dateIssuedToWorkshop[0] = "";

    try
    {
      String uName    = directoryUtils.getMySQLUserName();
      String passWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();
    
      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_project?user=" + uName + "&password=" + passWord);

      stmt = con.createStatement();
  
      String q = "SELECT Title, RequestedDeliveryDate, EnquiryDate, CustomerReference, Product, Note, EndUser, Contractor, Country, Currency, QuotedValue, Remark, DateOfPO, DateIssuedToContracts, Status, DateOfReview, ReviewedBy, "
               + "StatedDeliveryDate, CompanyCode, Owner, CheckedBy, DateCompleted, DateIssuedToWorkshop FROM projects WHERE ProjectCode='" + code + "'";

      rs = stmt.executeQuery(q);

      if(rs.next()) // just-in-case                  
      {
        title[0]                 = rs.getString(1);
        requestedDeliveryDate[0] = rs.getString(2);
        enquiryDate[0]           = rs.getString(3); 
        customerReference[0]     = rs.getString(4); 
        product[0]               = rs.getString(5); 
        note[0]                  = rs.getString(6); 
        endUser[0]               = rs.getString(7); 
        contractor[0]            = rs.getString(8); 
        country[0]               = rs.getString(9); 
        currency[0]              = rs.getString(10); 
        quotedValue[0]           = rs.getString(11); 
        remark[0]                = rs.getString(12); 
        dateOfPO[0]              = rs.getString(13); 
        dateIssuedToContracts[0] = rs.getString(14); 
        status[0]                = rs.getString(15); 
        dateOfReview[0]          = rs.getString(16);
        reviewedBy[0]            = rs.getString(17);
        statedDeliveryDate[0]    = rs.getString(18);
        companyCode[0]           = rs.getString(19);
        owner[0]                 = rs.getString(20);
        checkedBy[0]             = rs.getString(21);
        dateCompleted[0]         = rs.getString(22);
        dateIssuedToWorkshop[0]  = rs.getString(23);
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
  
  // -------------------------------------------------------------------------------------------------------------------------------
  public String fetchProjectTitleGivenCode(String code, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    String[] title                 = new String[1];
    String[] requestedDeliveryDate = new String[1];
    String[] enquiryDate           = new String[1];
    String[] customerReference     = new String[1];
    String[] product               = new String[1];
    String[] note                  = new String[1];
    String[] endUser               = new String[1];
    String[] contractor            = new String[1];
    String[] country               = new String[1];
    String[] currency              = new String[1];
    String[] quotedValue           = new String[1];
    String[] remark                = new String[1];
    String[] dateOfPO              = new String[1];
    String[] dateIssuedToContracts = new String[1];
    String[] status                = new String[1];
    String[] dateOfReview          = new String[1];
    String[] reviewedBy            = new String[1];
    String[] statedDeliveryDate    = new String[1];
    String[] companyCode           = new String[1];
    String[] owner                 = new String[1];
    String[] checkedBy             = new String[1];
    String[] dateCompleted         = new String[1];
    String[] dateIssuedToWorkshop  = new String[1];
    
    fetchProjectRec(code, title, requestedDeliveryDate, enquiryDate, customerReference, product, note, endUser, contractor, country,
                    currency, quotedValue, remark, dateOfPO, dateIssuedToContracts, status, dateOfReview, reviewedBy,
                    statedDeliveryDate, companyCode, owner, checkedBy, dateCompleted, dateIssuedToWorkshop, dnm, localDefnsDir,
                    defnsDir);
    
    return title[0];
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void createTableProjects(boolean dropTable, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    String userName = directoryUtils.getMySQLUserName();
    String passWord = directoryUtils.getMySQLPassWord();
    
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_project?user=" + userName + "&password=" + passWord);
    Statement stmt = con.createStatement();
    String q;
   
    try
    {
      if(dropTable)
      {  
        q = "DROP TABLE projects";
        stmt.executeUpdate(q);
      }
    }
    catch(Exception e) { }
    if(stmt != null) stmt.close();

    try
    {
      stmt = con.createStatement();
      q = "CREATE TABLE projects( ProjectCode integer not null, CompanyCode char(20) not null, Title char(100) not null, "
                                + "RequestedDeliveryDate date,   EnquiryDate date,              CustomerReference char(40), "
                                + "Product char(40),             Note char(100),                EndUser char(40),"
                                + "Contractor char(40),          Country char(40),              Currency char(3), "
                                + "QuotedValue decimal(15,2),    Remark char(250),              DateOfPO date, "
                                + "DateIssuedToContracts date,   DateIssuedToWorkshop date,     Status char(1), "
                                + "DateOfReview date,            ReviewedBy char(30),           Owner char(30), "
                                + "CheckedBy char(30),           DateCompleted date,            StatedDeliveryDate date, "
                                + "unique(ProjectCode))";
      stmt.executeUpdate(q);
    }
    catch(Exception e) { }

    if(stmt != null) stmt.close();

    try
    {
      stmt = con.createStatement();
      q = "CREATE INDEX companyCodeInx on projects(CompanyCode)";

      stmt.executeUpdate(q);
    }
    catch(Exception e) { }

    if(stmt != null) stmt.close();

    try
    {
      stmt = con.createStatement();
      q = "CREATE INDEX statusInx on projects(Status)";

      stmt.executeUpdate(q);
    }
    catch(Exception e) { }

    if(stmt != null) stmt.close();
    if(con  != null) con.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void createTableTitle(boolean dropTable, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    String userName = directoryUtils.getMySQLUserName();
    String passWord = directoryUtils.getMySQLPassWord();
    
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_project?user=" + userName + "&password=" + passWord);
    Statement stmt = con.createStatement();
    String q;
   
    try
    {
      if(dropTable)
      {  
        q = "DROP TABLE title";

        stmt.executeUpdate(q);
      }
    }
    catch(Exception e) { }
    if(stmt != null) stmt.close();

    try
    {
      stmt = con.createStatement();
      q = "CREATE TABLE title ( Name char(100) not null, unique(Name))";

      stmt.executeUpdate(q);
    }
    catch(Exception e) { }

    if(stmt != null) stmt.close();
    if(con  != null) con.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void createTableOfficeTasks(boolean dropTable, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    String userName = directoryUtils.getMySQLUserName();
    String passWord = directoryUtils.getMySQLPassWord();
    
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_project?user=" + userName + "&password=" + passWord);
    Statement stmt = con.createStatement();
    String q;
    
    try
    {
      if(dropTable)
      {  
        q = "DROP TABLE officetasks";
        stmt.executeUpdate(q);
      }
    }
    catch(Exception e) { }
    stmt.close();

    try
    {
      stmt = con.createStatement();
      q = "CREATE TABLE officetasks ( ProjectCode integer not null, TaskNumber integer not null, Task char(40), "
                                   + "Department char(40),           Status char(1),              StartDate date, "
                                   + "ExpectedFinishDate char(1),    ActualFinishDate date,       Remark char(100), "
                                   + "unique(ProjectCode, TaskNumber))";
      stmt.executeUpdate(q);
    }
    catch(Exception e) { }

    if(stmt != null) stmt.close();
    if(con  != null) con.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public void createTableWorkshopTasks(boolean dropTable, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    String userName = directoryUtils.getMySQLUserName();
    String passWord = directoryUtils.getMySQLPassWord();
    
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_project?user=" + userName + "&password="
                                                 + passWord);
    Statement stmt = con.createStatement();
    String q;
    
    try
    {
      if(dropTable)
      {  
        q = "DROP TABLE workshoptasks";
        stmt.executeUpdate(q);
      }
    }
    catch(Exception e) { }
    stmt.close();

    try
    {
      stmt = con.createStatement();
      q = "CREATE TABLE workshoptasks ( ProjectCode integer not null, TaskNumber integer not null,  StartDate date, "
                                     + "ExpectedFinishdate date,       ActualFinishDate date,        Task char(40), "
                                     + "Remark char(100),              AllocatedHours decimal(15,3), Priority char(1), "
                                     + "Status char(1), "
                                     + "unique(ProjectCode, TaskNumber))";
      stmt.executeUpdate(q);
    }
    catch(Exception e) { }

    if(stmt != null) stmt.close();
    if(con  != null) con.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public void createTableTaskAllocation(boolean dropTable, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    String userName = directoryUtils.getMySQLUserName();
    String passWord = directoryUtils.getMySQLPassWord();
    
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_project?user=" + userName + "&password="
                                                 + passWord);
    Statement stmt = con.createStatement();
    String q;
    
    try
    {
      if(dropTable)
      {  
        q = "DROP TABLE taskallocation";
        stmt.executeUpdate(q);
      }
    }
    catch(Exception e) { }
    stmt.close();

    try
    {
      stmt = con.createStatement();
      q = "CREATE TABLE taskallocation ( ProjectCode integer not null, TaskNumber integer not null,  UserCode char(40), "
                                      + "unique(ProjectCode, TaskNumber))";
      stmt.executeUpdate(q);
    }
    catch(Exception e) { }

    if(stmt != null) stmt.close();
    if(con  != null) con.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public void createTableTaskTime(boolean dropTable, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    String userName = directoryUtils.getMySQLUserName();
    String passWord = directoryUtils.getMySQLPassWord();
    
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_project?user=" + userName + "&password="
                                                 + passWord);
    Statement stmt = con.createStatement();
    String q;
    
    try
    {
      if(dropTable)
      {  
        q = "DROP TABLE tasktime";
        stmt.executeUpdate(q);
      }
    }
    catch(Exception e) { }
    stmt.close();

    try
    {
      stmt = con.createStatement();
      q = "CREATE TABLE tasktime ( ProjectCode integer not null, TaskNumber integer not null,  UserCode char(40), "
                                + "StartTime timestamp,       StopTime timestamp, "
                                + "unique(ProjectCode, TaskNumber))";
      stmt.executeUpdate(q);
    }
    catch(Exception e) { }

    if(stmt != null) stmt.close();
    if(con  != null) con.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public void createTableStockRequirements(boolean dropTable, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    String userName = directoryUtils.getMySQLUserName();
    String passWord = directoryUtils.getMySQLPassWord();
    
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_project?user=" + userName + "&password="
                                                 + passWord);
    Statement stmt = con.createStatement();
    String q;
    
    try
    {
      if(dropTable)
      {  
        q = "DROP TABLE stockrequirements";
        stmt.executeUpdate(q);
      }
    }
    catch(Exception e) { }
    stmt.close();

    try
    {
      stmt = con.createStatement();
      q = "CREATE TABLE stockrequirements ( ProjectCode integer not null,   ItemName char(60) not null, "
                                        + " QuantityRequired decimal(15,10), HaveStock char(1), Remark char(100))";
      stmt.executeUpdate(q);
    }
    catch(Exception e) { }

    if(stmt != null) stmt.close();
    if(con  != null) con.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public void createTablePermissions(boolean dropTable, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    String userName = directoryUtils.getMySQLUserName();
    String passWord = directoryUtils.getMySQLPassWord();
    
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_project?user=" + userName + "&password="
                                                 + passWord);
    Statement stmt = con.createStatement();
    String q;
    
    try
    {
      if(dropTable)
      {  
        q = "DROP TABLE permissions";
        stmt.executeUpdate(q);
      }
    }
    catch(Exception e) { }
    stmt.close();

    try
    {
      stmt = con.createStatement();
      q = "CREATE TABLE permissions( ProjectCode integer not null, PermissionGrant char(40) not null, "
                                  + "PermissionType char(1) not null, unique(ProjectCode, PermissionGrant))";
      stmt.executeUpdate(q);
    }
    catch(Exception e) { }

    if(stmt != null) stmt.close();
    if(con  != null) con.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void showStats(PrintWriter out, String dnm, int[] bytesOut) throws Exception
  {
    String userName = directoryUtils.getMySQLUserName();
    String passWord = directoryUtils.getMySQLPassWord();
    
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_project?user=" + userName + "&password=" + passWord);
    Statement stmt = con.createStatement();
   
    ResultSet rs = stmt.executeQuery("SELECT TABLE_NAME, CREATE_TIME, UPDATE_TIME, TABLE_ROWS FROM INFORMATION_SCHEMA.TABLES WHERE table_schema='" + dnm + "_project'");
    
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
  
  // -------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }
  
}
