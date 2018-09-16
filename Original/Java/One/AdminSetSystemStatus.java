// =======================================================================================================================================================================================================
// System: Zara: Admin: set system status
// Module: AdminSetSystemStatus.java
// Author: C.K.Harvey
// Copyright (c) 2001-08 Christopher Harvey. All Rights Reserved.
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

public class AdminSetSystemStatus extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  AdminUtils adminUtils = new AdminUtils();
  DefinitionTables definitionTables = new DefinitionTables();
  DashboardUtils dashboardUtils = new DashboardUtils();
  SignOnAdministrator  signOnAdministrator  = new SignOnAdministrator();
  
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="";

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
      
      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, bytesOut);
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
 
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "AdminSetSystemStatus", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7029, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                    int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    Connection con = null;
    Statement stmt = null;
    ResultSet rs   = null;

    try
    {
      String uName = directoryUtils.getMySQLUserName();
      String pWord = directoryUtils.getMySQLPassWord();
      Class.forName("com.mysql.jdbc.Driver").newInstance();
      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + uName + "&password=" + pWord);

      if((! serverUtils.isSysAdmin(con, stmt, rs, unm, dnm, sid, uty, localDefnsDir, defnsDir)) && (! serverUtils.isDBAdmin(con, stmt, rs, unm)))
      {
        messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "AdminSetSystemStatus", imagesDir, localDefnsDir, defnsDir, bytesOut);
        serverUtils.etotalBytes(req, unm, dnm, 7029, bytesOut[0], 0, "ACC:");
        if(con != null) con.close();
        if(out != null) out.flush();
        return;
      }

      if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
      {
        messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "AdminSetSystemStatus", imagesDir, localDefnsDir, defnsDir, bytesOut);
        serverUtils.etotalBytes(req, unm, dnm, 7029, bytesOut[0], 0, "SID:");
        if(con != null) con.close();
        if(out != null) out.flush();
        return;
      }
        
      set(con, stmt, rs, out, unm, sid, uty, men, den, dnm, bnm, imagesDir, localDefnsDir, defnsDir, bytesOut);
    }
    catch(Exception e) { } 
    
    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 7029, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String imagesDir, String localDefnsDir, String defnsDir, int[] bytesOut)
                   throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>System Status</title>");
   
    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    scoutln(out, bytesOut, "function update(){document.forms[0].submit();}");

    String cssDirectory = directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir);
    
    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + cssDirectory + "general.css\">");

    signOnAdministrator.heading(out, true, unm, sid, uty, bnm, dnm, men, den, imagesDir, bytesOut);

    dashboardUtils.drawTitle(out, "System Status", "7029", unm, sid, uty, men, den, dnm, bnm, bytesOut);

    scoutln(out, bytesOut, "<form action=\"AdminProcessSystem\" enctype=\"application/x-www-form-urlencoded\" method=POST>");
    scoutln(out, bytesOut, "<input type=hidden name=unm value="+unm+">");
    scoutln(out, bytesOut, "<input type=hidden name=sid value="+sid+">");
    scoutln(out, bytesOut, "<input type=hidden name=uty value="+uty+">");
    scoutln(out, bytesOut, "<input type=hidden name=men value=\""+men+"\">");
    scoutln(out, bytesOut, "<input type=hidden name=den value="+den+">");
    scoutln(out, bytesOut, "<input type=hidden name=dnm value="+dnm+">");
    scoutln(out, bytesOut, "<input type=hidden name=bnm value="+bnm+">");

    scoutln(out, bytesOut, "<table id=\"page\" cellspacing=2 cellpadding=2 width=100%>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    String[] systemStatus = new String[1];

    definitionTables.getSystem(con, stmt, rs, dnm, systemStatus);
    
    scoutln(out, bytesOut, "<tr><td nowrap><p>System Status</td><td>");
    setStatus(out, systemStatus[0], bytesOut);
    scoutln(out, bytesOut, "</td></tr>");
        
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td><p><a href=\"javascript:update()\">Update</a></td></tr>");
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "</table></form></head></head>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void setStatus(PrintWriter out, String current, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<select name=\"systemStatus\">");
    
    scout(out, bytesOut, "<option value='Live'");
    if(current.equals("L"))
      scout(out, bytesOut, " selected");
    scoutln(out, bytesOut, ">Live\n");    
    
    scout(out, bytesOut, "<option value='Maintenance'");
    if(current.equals("M"))
      scout(out, bytesOut, " selected");
    scoutln(out, bytesOut, ">Maintenance\n");    
    
    scoutln(out, bytesOut, "</select>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void scout(PrintWriter out, int[] bytesOut, String str) throws Exception
  {      
    out.print(str);
    bytesOut[0] += str.length();    
  }
  private void scoutln(PrintWriter out, int[] bytesOut, String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += str.length() + 2;    
  }

}
