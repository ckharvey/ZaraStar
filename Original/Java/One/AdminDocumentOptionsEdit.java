// =======================================================================================================================================================================================================
// System: ZaraStar Admin: Edit document options
// Module: AdminDocumentOptionsEdit.java
// Author: C.K.Harvey
// Copyright (c) 1999-2008 Christopher Harvey. All Rights Reserved.
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

public class AdminDocumentOptionsEdit extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  DashboardUtils dashboardUtils = new DashboardUtils();
  DefinitionTables definitionTables = new DefinitionTables();
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
 
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "AdminDocumentOptionsEdit", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7070, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut)
                    throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if((! serverUtils.isSysAdmin(con, stmt, rs, unm, dnm, sid, uty, localDefnsDir, defnsDir)) && (! serverUtils.isDBAdmin(con, stmt, rs, unm)))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "AdminDocumentOptionsEdit", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7070, bytesOut[0], 0, "ACC:");
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "AdminDocumentOptionsEdit", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7070, bytesOut[0], 0, "SID:");
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }
        
    set(con, stmt, rs, out, unm, sid, uty, men, den, dnm, bnm, imagesDir, localDefnsDir, defnsDir, bytesOut);
    
    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 7071, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String unm, String sid, String uty, String men,
                   String den, String dnm, String bnm, String imagesDir, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Definitions: Document Options</title>");
   
    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    scoutln(out, bytesOut, "function update(){document.forms[0].submit();}");

    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\""
                         + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    signOnAdministrator.heading(out, true, unm, sid, uty, bnm, dnm, men, den, imagesDir, bytesOut);

    dashboardUtils.drawTitle(out, "Document Options", "7070", unm, sid, uty, men, den, dnm, bnm, bytesOut);

    scoutln(out, bytesOut, "<form action='AdminDocumentOptionsProcess' enctype=\"application/x-www-form-urlencoded\" method=POST>");
    scoutln(out, bytesOut, "<input type=hidden name=unm value="+unm+">");
    scoutln(out, bytesOut, "<input type=hidden name=sid value="+sid+">");
    scoutln(out, bytesOut, "<input type=hidden name=uty value="+uty+">");
    scoutln(out, bytesOut, "<input type=hidden name=men value=\""+men+"\">");
    scoutln(out, bytesOut, "<input type=hidden name=den value="+den+">");
    scoutln(out, bytesOut, "<input type=hidden name=dnm value="+dnm+">");
    scoutln(out, bytesOut, "<input type=hidden name=bnm value="+bnm+">");

    scoutln(out, bytesOut, "<table id='page' cellspacing=2 cellpadding=2 width=100%>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    String[][] options = new String[1][100];

    definitionTables.getDocumentOptions(con, stmt, rs, options);

    drawCheckBox(out, "Order Confirmation", "Block unless manager approved",                           "0",  options[0][0], bytesOut);
    drawCheckBox(out, "Picking List",       "Use Store/Quantity instead of showing two stores",        "1",  options[0][1], bytesOut);
    drawCheckBox(out, "Picking List",       "Prepend Manufacturer Code to printed line",               "2",  options[0][2], bytesOut);
    drawCheckBox(out, "Many",               "Prepend Manufacturer Code to printed line",               "10", options[0][10], bytesOut);
    drawCheckBox(out, "Many",               "Append Delivery Date to printed line",                    "3",  options[0][3], bytesOut);
    drawCheckBox(out, "Picking List",       "Draw horizontal line between item lines",                 "4",  options[0][4], bytesOut);
    drawCheckBox(out, "Many",               "Show Customer Item Code after Description",               "5",  options[0][5], bytesOut);
    drawCheckBox(out, "Many",               "Hide descriptions of duplicate entries of printed lines", "6",  options[0][6], bytesOut);
    drawCheckBox(out, "Many",               "Show both stock description lines on printed line",       "7",  options[0][7], bytesOut);
    drawCheckBox(out, "Picking List",       "Set Instruction to the Sales Order Remark field",         "8",  options[0][8], bytesOut);
    drawCheckBox(out, "Many",               "Include Remark from Stock record",                        "9",  options[0][9], bytesOut);
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td><p><a href=\"javascript:update()\">Update</a></td></tr>");
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "</table></form></body></html>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void drawCheckBox(PrintWriter out, String document, String desc, String option, String value, int[] bytesOut) throws Exception
  {
    scout(out, bytesOut, "<tr><td><input type=checkbox name='option" + option + "'");

    if(value.equals("Y"))
      scout(out, bytesOut, " checked");
    
    scoutln(out, bytesOut, "></td><td nowrap><p>&nbsp;&nbsp;" + document + "</td><td nowrap><p>&nbsp;&nbsp;" + desc + "</td></tr>");
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
    bytesOut[0] += str.length() + 2;    
  }

}
