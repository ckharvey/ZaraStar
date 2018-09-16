// =======================================================================================================================================================================================================
// System: ZaraStar AdminEngine - View a log
// Module: AdminViewLog.java
// Author: C.K.Harvey
// Copyright (c) 2003-08 Christopher Harvey. All Rights Reserved.
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

public class AdminViewLog extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils(); 
  AdminUtils adminUtils = new AdminUtils();
  DefinitionTables definitionTables = new DefinitionTables();
  
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="";

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
      p1  = req.getParameter("p1"); // externalCode
      p2  = req.getParameter("p2"); // name

      if(p2 == null) p2 = "";
      
      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, bytesOut);
    }
    catch(Exception e)
    {
      StringBuffer url = req.getRequestURL();
      String urlBit="";
      int x=0;
      if(url.charAt(x) == 'h' || url.charAt(x) == 'H')
        x += 7;
      while(url.charAt(x) != '\000' && url.charAt(x) != ',' && url.charAt(x) != '/')
        urlBit += url.charAt(x++);

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "AdminViewLog", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7030, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1,
                    String p2, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    
    Statement stmt = null;
    ResultSet rs   = null;
    Statement stmt2 = null;
    ResultSet rs2   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 7030, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "AdminLogManager", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7030, bytesOut[0], 0, "ACC:" + p1);
      if(con != null) con.close();
      
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "AdminLogManager", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7030, bytesOut[0], 0, "SID:" + p1);
      if(con != null) con.close();
      
      if(out != null) out.flush();
      return;
    }
  
    set(con, stmt, stmt2, rs, rs2, out, req, p1, p2, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 7030, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con != null) con.close();
    
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, HttpServletRequest req, String externalCode,
                   String name, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir,
                   int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Admin - User Access Log</title>");

    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\""
                           + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");
    
    int[] hmenuCount = new int[1];

    adminUtils.outputPageFrame(con, stmt, rs, out, req, "AdminLogManager", "", "7030", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount, bytesOut);

    adminUtils.drawTitle(con, stmt, rs, req, out, "User Access Log for <i>" + externalCode + ": " + name + "</i>", "7030", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<table id=\"page\" border=0>");  

    scoutln(out, bytesOut, "<tr id=\"pageColumn\">");
    scoutln(out, bytesOut, "<td nowrap><p>Date &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Service &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Message &nbsp;</td></tr>");

    getTrail(con, stmt, stmt2, rs, rs2, out, externalCode, unm, dnm, bytesOut);

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td colspan=4><p>");
    scoutln(out, bytesOut, "<a href=\"/central/servlet/AdminDeleteLogs?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm
                         + "&p1=" + generalUtils.sanitise(externalCode) + "&bnm=" + bnm + "\">Clear</a> this Log</td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "</table>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void getTrail(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, String externalCode, String unm,
                        String dnm, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();
    
    if(externalCode.startsWith("Demo"))
    {
      rs = stmt.executeQuery("SELECT Service, DateTime, Text FROM trail WHERE UserCode = '" + dnm + "' AND UserCode = '" + externalCode
                           + "' ORDER BY DateTime");
    }
    else
    {
      rs = stmt.executeQuery("SELECT Service, DateTime, Text FROM trail WHERE UserCode = '" + dnm + "' AND UserCode = '" + externalCode
                           + "_' ORDER BY DateTime");
    }

    String service, dateTime, text, cssFormat="";
    boolean wanted;
    
    while(rs.next())
    {    
      service  = rs.getString(1);
      dateTime = rs.getString(2);
      text     = rs.getString(3);

      if(unm.equals("Sysadmin"))
        wanted = true;
      else wanted = false;
      
      if(wanted)
      {        
        if(cssFormat.equals("line1")) cssFormat = "line2"; else cssFormat = "line1";

        scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\"><td nowrap><p>" + generalUtils.convertFromTimestamp(dateTime) + "</td>");
        scoutln(out, bytesOut, "<td nowrap><p>" + service + ": " + definitionTables.getDescriptionGivenService(con, stmt2, rs2, service) + "</td>");//////////////////// was conZC
        scoutln(out, bytesOut, "<td nowrap><p>" + text + "</td></tr>");
      }
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }
  
}
