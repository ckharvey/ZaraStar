// =======================================================================================================================================================================================================
// System: ZaraStar Project: Main page
// Module: ProjectMain.java
// Author: C.K.Harvey
// Copyright (c) 2006-07 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
//
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

public class ProjectMain extends HttpServlet
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

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="";

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
      p1  = req.getParameter("p1"); // code

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "ProjectMain", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6802, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                    String p1, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

     if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 6800, unm, uty, dnm, localDefnsDir, defnsDir))
     {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "ProjectMain", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6802, bytesOut[0], 0, "ACC:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }
    
    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "ProjectMain", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6802, bytesOut[0], 0, "SID:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }
    
    set(con, stmt, rs, out, req, p1, unm, sid, uty, men, den, dnm, bnm, imagesDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 6802, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String p1, String unm, String sid,
                   String uty, String men, String den, String dnm, String bnm, String imagesDir, String localDefnsDir, String defnsDir,
                   int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Project Main Page</title>");

    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\""
                           + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");
  
    scoutln(out, bytesOut, "<script javascript=\"JavaScript\">");

    scoutln(out, bytesOut, "function details(){");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/ProjectCreateEdit?unm=" + unm + "&sid=" + sid + "&uty=" + uty
                         + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=E&p1=" + p1 + "\";}");

    scoutln(out, bytesOut, "function officetasks(){");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/ProjectOfficeTasks?unm=" + unm + "&sid=" + sid + "&uty=" + uty
                         + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + p1 + "\";}");

    scoutln(out, bytesOut, "function workshoptasks(){");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/_6805?unm=" + unm + "&sid=" + sid + "&uty=" + uty
                         + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + p1 + "\";}");

    scoutln(out, bytesOut, "function schedule(){");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/_6807?unm=" + unm + "&sid=" + sid + "&uty=" + uty
                         + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + p1 + "\";}");

    scoutln(out, bytesOut, "function documents(){");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/ProjectDocuments?unm=" + unm + "&sid=" + sid + "&uty=" + uty
                         + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + p1 + "\";}");

    scoutln(out, bytesOut, "function materials(){");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/_6809?unm=" + unm + "&sid=" + sid + "&uty=" + uty
                         + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + p1 + "\";}");

    scoutln(out, bytesOut, "function tests(){");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/_6813?unm=" + unm + "&sid=" + sid + "&uty=" + uty
                         + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + p1 + "\";}");

    scoutln(out, bytesOut, "function reports(){");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/_6810?unm=" + unm + "&sid=" + sid + "&uty=" + uty
                         + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + p1 + "\";}");

    scoutln(out, bytesOut, "</script>");

    int[] hmenuCount = new int[1];
    projectUtils.outputPageFrame(con, stmt, rs, out, req, "6802", "ProjectMain", "", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount, bytesOut);

    projectUtils.drawTitle(con, stmt, rs, req, out, "ProjectMain", "Project: " + p1, "6802", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<table id=\"page\" width=100% cellpadding=3 cellspacing=3 border=0>");
    
    String[] title                 = new String[1]; title[0]                 = "";
    String[] requestedDeliveryDate = new String[1]; requestedDeliveryDate[0] = "";
    String[] enquiryDate           = new String[1]; enquiryDate[0]           = "";
    String[] customerReference     = new String[1]; customerReference[0]     = "";
    String[] product               = new String[1]; product[0]               = "";
    String[] note                  = new String[1]; note[0]                  = "";
    String[] endUser               = new String[1]; endUser[0]               = "";
    String[] contractor            = new String[1]; contractor[0]            = "";
    String[] country               = new String[1]; country[0]               = "";
    String[] currency              = new String[1]; currency[0]              = "";
    String[] quotedValue           = new String[1]; quotedValue[0]           = "";
    String[] remark                = new String[1]; remark[0]                = "";
    String[] dateOfPO              = new String[1]; dateOfPO[0]              = "";
    String[] dateIssuedToContracts = new String[1]; dateIssuedToContracts[0] = "";
    String[] status                = new String[1]; status[0]                = "";
    String[] dateOfReview          = new String[1]; dateOfReview[0]          = "";
    String[] reviewedBy            = new String[1]; reviewedBy[0]            = "";
    String[] statedDeliveryDate    = new String[1]; statedDeliveryDate[0]    = "";
    String[] companyCode           = new String[1]; companyCode[0]           = "";
    String[] owner                 = new String[1]; owner[0]                 = "";
    String[] checkedBy             = new String[1]; checkedBy[0]             = "";
    String[] dateCompleted         = new String[1]; dateCompleted[0]         = "";
    String[] dateIssuedToWorkshop  = new String[1]; dateIssuedToWorkshop[0]  = "";
  
    projectUtils.fetchProjectRec(p1, title, requestedDeliveryDate, enquiryDate, customerReference, product, note, endUser, contractor, country, currency,
                          quotedValue, remark, dateOfPO, dateIssuedToContracts, status, dateOfReview, reviewedBy, statedDeliveryDate, companyCode,
                          owner, checkedBy, dateCompleted, dateIssuedToWorkshop, dnm, localDefnsDir, defnsDir); 

    scoutln(out, bytesOut, "<tr><td nowrap valign=center><p><b>Project Code</td><td><p>" + p1 + "</td>");  
    scoutln(out, bytesOut, "<td nowrap><p><b>Project Title</td><td><p>" + title[0] + "</td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap><p><b>Customer Code</td><td><p>" + companyCode[0] + "</td>");
    scoutln(out, bytesOut, "<td nowrap><p><b>Requested Delivery Date</td><td><p>"
                         + generalUtils.convertFromYYYYMMDD(requestedDeliveryDate[0]) + "</td></tr>");
    scoutln(out, bytesOut, "<td nowrap><p><b>Stated Delvery Date</td><td><p>" + generalUtils.convertFromYYYYMMDD(statedDeliveryDate[0])
                         + "</td></tr>");
    
    scoutln(out, bytesOut, "<tr><td nowrap><p><b>Enquiry Date</td><td><p>" + generalUtils.convertFromYYYYMMDD(enquiryDate[0]) + "</td>");
    scoutln(out, bytesOut, "<td nowrap><p><b>Customer Reference</td><td><p>" + customerReference[0] + "</td>");
    scoutln(out, bytesOut, "<td nowrap><p><b>Product</td><td><p>" + product[0] + "</td></tr>");
    
    scoutln(out, bytesOut, "<tr><td nowrap><p><b>Note</td><td colspan=5><p>" + note[0] + "</td></tr>");  
    
    scoutln(out, bytesOut, "<tr><td colspan=6><hr></td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap><p><b>Owner</td><td><p>" + owner[0] + "</td>");
    scoutln(out, bytesOut, "<td nowrap><p><b>End User</td><td><p>" + endUser[0] + "</td>");
    scoutln(out, bytesOut, "<td nowrap><p><b>Contractor</td><td><p>" + contractor[0] + "</td></tr>");
    
    scoutln(out, bytesOut, "<tr><td nowrap><p><b>Country</td><td colspan=5><p>" + country[0] + "</td></tr>");
    
    scoutln(out, bytesOut, "<tr><td nowrap><p><b>Currency</td><td><p>" + currency[0] + "</td>");  
    scoutln(out, bytesOut, "<td nowrap><p><b>Quoted Value</td><td><p>" + quotedValue[0] + "</td></tr>");
    
    scoutln(out, bytesOut, "<tr><td nowrap valign=top><p><b>Remark</td><td colspan=5><p>" + remark[0] + "</td></tr>");  
    
    scoutln(out, bytesOut, "<tr><td colspan=6><hr></td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap><p><b>Date of PO</td><td><p>" + generalUtils.convertFromYYYYMMDD(dateOfPO[0]) + "</td>");
    scoutln(out, bytesOut, "<td nowrap><p><b>Date Issued To Contracts</td><td><p>" + generalUtils.convertFromYYYYMMDD(dateIssuedToContracts[0]) + "</td>");
    scoutln(out, bytesOut, "<td nowrap><p><b>Date Issued To Workshop</td><td><p>" + generalUtils.convertFromYYYYMMDD(dateIssuedToWorkshop[0]) + "</td></tr>");  

    scout(out, bytesOut, "<tr><td nowrap><p><b>Status</td><td><p>");
    if(status[0].equals("O"))
      scout(out, bytesOut, "Open");
    else
    if(status[0].equals("C"))
      scout(out, bytesOut, "Completed");
    else
    if(status[0].equals("P"))
      scout(out, bytesOut, "Proposal");
    else
    if(status[0].equals("R"))
      scout(out, bytesOut, "Rejected");
    else
    if(status[0].equals("A"))
      scout(out, bytesOut, "Abandoned");
    else // 'X'
      scout(out, bytesOut, "Cancelled");
    scout(out, bytesOut, "</td>");

    scoutln(out, bytesOut, "<td nowrap><p><b>Date Of Review</td><td><p>" + generalUtils.convertFromYYYYMMDD(dateOfReview[0]) + "</td>");
    scoutln(out, bytesOut, "<td nowrap><p><b>Reviewed By</td><td><p>" + reviewedBy[0] + "</td></tr>");
    
    scoutln(out, bytesOut, "<tr><td nowrap><p><b>Checked By</td><td><p>" + checkedBy[0] + "</td>");
    scoutln(out, bytesOut, "<td nowrap><p><b>Date Completed</td><td><p>" + generalUtils.convertFromYYYYMMDD(dateCompleted[0]) + "</td></tr>");
  
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "</table>");     scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
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
  
}
