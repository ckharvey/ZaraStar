// =======================================================================================================================================================================================================
// System: ZaraStar Project: List
// Module: ProjectList.java
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

public class ProjectList extends HttpServlet
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

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="", p3="", p4="", p5="", p6="", p7="", p8="";

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
      p1  = req.getParameter("p1"); // show open projects 
      p2  = req.getParameter("p2"); // show completed projects 
      p3  = req.getParameter("p3"); // show proposed projects 
      p4  = req.getParameter("p4"); // show rejected proposals
      p5  = req.getParameter("p5"); // show abandoned projects 
      p6  = req.getParameter("p6"); // show cancelled projects 
      p7  = req.getParameter("p7"); // companyCode 
      p8  = req.getParameter("p8"); // orderedBy 

      if(p1 == null) p1 = "T";
      if(p2 == null) p2 = "";
      if(p3 == null) p3 = "";
      if(p4 == null) p4 = "";
      if(p5 == null) p5 = "";
      if(p6 == null) p6 = "";
      if(p8 == null) p8 = "N"; // by projectCode

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, p3, p4, p5, p6, p7, p8, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "ProjectList", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6800, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1, String p2, String p3, String p4, String p5, String p6, String p7, String p8,
                    int[] bytesOut) throws Exception
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
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "ProjectList", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6800, bytesOut[0], 0, "ACC:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }
    
    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "ProjectList", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6800, bytesOut[0], 0, "SID:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }
    
    set(con, stmt, rs, out, req, p1, p2, p3, p4, p5, p6, p7, p8, unm, sid, uty, men, den, dnm, bnm, imagesDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 6800, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String p1, String p2, String p3, String p4, String p5, String p6, String p7, String p8,
                   String unm, String sid, String uty, String men, String den, String dnm, String bnm, String imagesDir,
                   String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Project Listing</title>");
            
    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\""
                         + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");
    
    scoutln(out, bytesOut, "<script javascript=\"JavaScript\">");

    scoutln(out, bytesOut, "function fetch(code){");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/ProjectMain?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm="
                         + dnm + "&bnm=" + bnm + "&p1=\"+code;}");

    scoutln(out, bytesOut, "function go(){var p1,p2,p3,p4,p5,p6,p8;if(document.forms[0].open.checked)p1='T';else p1='F';");
    scoutln(out, bytesOut, "if(document.forms[0].completed.checked)p2='T';else p2='F';if(document.forms[0].proposed.checked)p3='T';");
    scoutln(out, bytesOut, "else p3='F';if(document.forms[0].rejected.checked)p4='T';else p4='F';if(document.forms[0].abandoned.checked)p5='T';");
    scoutln(out, bytesOut, "else p5='F';if(document.forms[0].cancelled.checked)p6='T';else p6='F';");
    scoutln(out, bytesOut, "if(document.forms[0].orderedby[1].checked)p8='N';else if(document.forms[0].orderedby[2].checked)p8='D';");
    scoutln(out, bytesOut, "else if(document.forms[0].orderedby[3].checked)p8='V';else if(document.forms[0].orderedby[4].checked)p8='O';");
    scoutln(out, bytesOut, "else if(document.forms[0].orderedby[5].checked)p8='E';else p8='C';");
    scoutln(out, bytesOut, "this.location.replace(\"/central/servlet/ProjectList?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men
                         + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                         + "&p1=\"+p1+\"&p2=\"+p2+\"&p3=\"+p3+\"&p4=\"+p4+\"&p5=\"+p5+\"&p6=\"+p6+\"&p8=\"+p8);}");

    scoutln(out, bytesOut, "</script>");

    int[] hmenuCount = new int[1];
    projectUtils.outputPageFrame(con, stmt, rs, out, req, "6800", "ProjectList", "", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount, bytesOut);
   
    projectUtils.drawTitle(con, stmt, rs, req, out, "ProjectList", "Project List", "6800", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
      
    scoutln(out, bytesOut, "<form><table id=\"page\" cellspacing=2 cellpadding=0 width=100%>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scout(out, bytesOut, "<tr><td><p><input type=checkbox name=open");
    if(p1.equals("T"))
      scout(out, bytesOut, " checked");
    scoutln(out, bytesOut, ">Open</td>");
    
    scout(out, bytesOut, "<td><p><input type=checkbox name=completed");
    if(p2.equals("T"))
      scout(out, bytesOut, " checked");
    scoutln(out, bytesOut, ">Completed</td>");
    
    scout(out, bytesOut, "<td><p><input type=checkbox name=proposed");
    if(p3.equals("T"))
      scout(out, bytesOut, " checked");
    scoutln(out, bytesOut, ">Proposed</td>");
    
    scout(out, bytesOut, "<td><p><input type=checkbox name=rejected");
    if(p4.equals("T"))
      scout(out, bytesOut, " checked");
    scoutln(out, bytesOut, ">Rejected</td>");
    
    scout(out, bytesOut, "<td><p><input type=checkbox name=abandoned");
    if(p5.equals("T"))
      scout(out, bytesOut, " checked");
    scoutln(out, bytesOut, ">Abandoned</td>");
    
    scout(out, bytesOut, "<td><p><input type=checkbox name=cancelled");
    if(p6.equals("T"))
      scout(out, bytesOut, " checked");
    scoutln(out, bytesOut, ">Cancelled</td></tr>");
    
    scout(out, bytesOut, "<tr><td>&nbsp;</td><tr>");

    scout(out, bytesOut, "<tr><td><p><input type=radio value='C' name=orderedby");
    if(p8.equals("C"))
      scout(out, bytesOut, " checked");
    scoutln(out, bytesOut, ">by Customer</td>");
    
    scout(out, bytesOut, "<td><p><input type=radio value='N' name=orderedby");
    if(p8.equals("N"))
      scout(out, bytesOut, " checked");
    scoutln(out, bytesOut, ">by Project Code</td>");
    
    scout(out, bytesOut, "<td><p><input type=radio value='D' name=orderedby");
    if(p8.equals("D"))
      scout(out, bytesOut, " checked");
    scoutln(out, bytesOut, ">by Delvery Date</td>");
    
    scout(out, bytesOut, "<td><p><input type=radio value='V' name=orderedby");
    if(p8.equals("V"))
      scout(out, bytesOut, " checked");
    scoutln(out, bytesOut, ">by Value</td>");
    
    scout(out, bytesOut, "<td><p><input type=radio value='O' name=orderedby");
    if(p8.equals("O"))
      scout(out, bytesOut, " checked");
    scoutln(out, bytesOut, ">by Country</td>");
    
    scout(out, bytesOut, "<td><p><input type=radio value='E' name=orderedby");
    if(p8.equals("E"))
      scout(out, bytesOut, " checked");
    scoutln(out, bytesOut, ">by End-User</td></tr>");
    
    scoutln(out, bytesOut, "<tr><td><a href=\"javascript:go()\"><p>Search</a></td></tr>");
     
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "</table>");
    
    scoutln(out, bytesOut, "<table id=\"page\" cellspacing=2 cellpadding=0 width=100%>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr id=\"pageColumn\">");
    scoutln(out, bytesOut, "<td align=center><p><b>Status</td>");
    scoutln(out, bytesOut, "<td align=center><p><b>Project Code</td>");
    scoutln(out, bytesOut, "<td align=center><p><b>Company Code</td>");
    scoutln(out, bytesOut, "<td align=center><p><b>Title</td>");
    scoutln(out, bytesOut, "<td align=center><p><b>Value</td>");
    scoutln(out, bytesOut, "<td align=center><p><b>Country</td>");
    scoutln(out, bytesOut, "<td align=center><p><b>End-User</td>");

    scoutln(out, bytesOut, "<td align=center><p><b>Delivery Date</td>");
    scoutln(out, bytesOut, "<td align=center><p><b>Date Completed</td></tr>");
 
    boolean openWanted, completedWanted, proposedWanted, rejectedWanted, abandonedWanted, cancelledWanted;
    if(p1.equals("F")) openWanted      = false; else openWanted      = true;
    if(p2.equals("F")) completedWanted = false; else completedWanted = true;
    if(p3.equals("F")) proposedWanted  = false; else proposedWanted  = true;
    if(p4.equals("F")) rejectedWanted  = false; else rejectedWanted  = true;
    if(p5.equals("F")) abandonedWanted = false; else abandonedWanted = true;
    if(p6.equals("F")) cancelledWanted = false; else cancelledWanted = true;

    projectUtils.listProjects(out, openWanted, completedWanted, proposedWanted, rejectedWanted, abandonedWanted, cancelledWanted, p8, dnm, imagesDir,
                       localDefnsDir, defnsDir, bytesOut);

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "</table></form>");     scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
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
