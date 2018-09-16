// =======================================================================================================================================================================================================
// System: ZaraStar DocumentEngine: Stock Check: create focus page
// Module: StockCheckServices.java
// Author: C.K.Harvey
// Copyright (c) 2001-08 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.sql.*;

public class StockCheckServices extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  DrawingUtils drawingUtils = new DrawingUtils();

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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "StockCheckServices", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 166, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush(); 
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    
    Statement stmt = null;
    ResultSet rs   = null;
    
    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 166, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "166", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 166, bytesOut[0], 0, "ACC:");
      
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "166", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 166, bytesOut[0], 0, "SID:");
      
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    set(con, stmt, rs, out, req, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 166, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
      
      if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir,
                   int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Stock Check Focus</title>");
    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    boolean StockCheckForItem = authenticationUtils.verifyAccess(con, stmt, rs, req, 3018, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean StockCheckAnyItem = authenticationUtils.verifyAccess(con, stmt, rs, req, 3019, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean StockCheckListing = authenticationUtils.verifyAccess(con, stmt, rs, req, 3020, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean StockCheckUploadUpdate = authenticationUtils.verifyAccess(con, stmt, rs, req, 3021, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean IncompletePickingLists = authenticationUtils.verifyAccess(con, stmt, rs, req, 3064, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean StockCheckReconciliation = authenticationUtils.verifyAccess(con, stmt, rs, req, 3065, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean ProductCycleCountSelection = authenticationUtils.verifyAccess(con, stmt, rs, req, 3081, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean StockCheckTrust = authenticationUtils.verifyAccess(con, stmt, rs, req, 3082, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean ProductCycleCountInput = authenticationUtils.verifyAccess(con, stmt, rs, req, 3083, unm, uty, dnm, localDefnsDir, defnsDir);
      
    if(StockCheckForItem)
    {
      scoutln(out, bytesOut, "function fetch(){");
      scoutln(out, bytesOut, "var p1=sanitise(document.forms[0].code.value);if(p1.length==0)");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/StockCheckAnyItem?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\";");
      scoutln(out, bytesOut, "else this.location.href=\"/central/servlet/StockCheckForItem?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+p1;}");

      scoutln(out, bytesOut, "function fetch2(){");
      scoutln(out, bytesOut, "var p1=sanitise(document.forms[0].mfr.value);var p2=sanitise(document.forms[0].mfrCode.value);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/StockCheckForItem?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=\"+p1+\"&p3=\"+p2;}");
    }

    if(StockCheckListing)
    {
      scoutln(out, bytesOut, "function list(which){");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/StockCheckListing?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=X&p1=\"+which;}");
    }

    if(StockCheckForItem)
    {
      scoutln(out, bytesOut, "function sanitise(code){");
      scoutln(out, bytesOut, "var code2='';var x;var len=code.length;");
      scoutln(out, bytesOut, "for(x=0;x<len;++x)");
      scoutln(out, bytesOut, "if(code.charAt(x)=='#')code2+='%23';");
      scoutln(out, bytesOut, "else if(code.charAt(x)=='\"')code2+='%22';");
      scoutln(out, bytesOut, "else if(code.charAt(x)=='&')code2+='%26';");
      scoutln(out, bytesOut, "else if(code.charAt(x)=='%')code2+='%25';");
      scoutln(out, bytesOut, "else if(code.charAt(x)==' ')code2+='%20';");
      scoutln(out, bytesOut, "else if(code.charAt(x)=='?')code+='%3F';");
      scoutln(out, bytesOut, "else code2+=code.charAt(x);");
      scoutln(out, bytesOut, "return code2};");
    }

    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    int[] hmenuCount = new int[1];

    drawingUtils.outputPageFrame(con, stmt, rs, out, req, "166", "", "StockCheckServices", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount, bytesOut);

    drawingUtils.drawTitle(out, false, false, "StockCheckServices", "", "Stock Check Focus", "166", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<form><table id=\"page\" border=0 cellspacing=2 cellpadding=0 width=100%>");

    if(StockCheckForItem)
    {
      scoutln(out, bytesOut, "<tr><td colspan=5 nowrap><h1>Stock Check Record Fetch</td></tr>");
      scoutln(out, bytesOut, "<tr><td></td><td colspan=2><p>Fetch and Amend by Item Code</td>");
      scoutln(out, bytesOut, "<td nowrap><p><input type=text maxlength=20 size=20 name=code>");
      scoutln(out, bytesOut, "and <a href=\"javascript:fetch()\">fetch</a></td>");
      scoutln(out, bytesOut, "<td width=99%><span id=\"service\"><p>&nbsp;&nbsp;&nbsp;<a href=\"javascript:help('3018')\">(Service 3018)</a></span></td></tr>");

      scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
      scoutln(out, bytesOut, "<tr><td></td><td colspan=2 nowrap><p>Or by Manufacturer and Manufacturer Code:</td></tr>");
      scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
      scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>Manufacturer &nbsp;</td><td>");
      getMfrsDDL(con, stmt, rs, out, bytesOut);
      scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>Manufacturer Code&nbsp;&nbsp;</td><td nowrap colspan=2><p><input type=text maxlength=30 size=20 name=mfrCode>");
      scoutln(out, bytesOut, "and <a href=\"javascript:fetch2()\">fetch</a></td></tr>");
    }

    if(StockCheckAnyItem | StockCheckUploadUpdate | IncompletePickingLists || StockCheckReconciliation)
    {
      scoutln(out, bytesOut, "<tr><td colspan=5 nowrap><h1>New Stock Check Records</td></tr>");

      if(StockCheckAnyItem)
      {
        scoutln(out, bytesOut, "<tr><td></td><td nowrap colspan=2><p><a href=\"/central/servlet/StockCheckAnyItem?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                             + "&p1=&p3=&p4=&p5=N&p2=C\">New</a> Stock Check Records Entry</td>");
        scoutln(out, bytesOut, "<td><span id=\"service\"><p>&nbsp;&nbsp;&nbsp;<a href=\"javascript:help('3019')\">(Service 3019)</a></span></td></tr>");
      }

      if(IncompletePickingLists)
      {
        scoutln(out, bytesOut, "<tr><td></td><td nowrap colspan=2><p><a href=\"/central/servlet/IncompletePickingLists?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                             + "\">List</a> Incomplete Picking Lists and GRNs</td>");
        scoutln(out, bytesOut, "<td><span id=\"service\"><p>&nbsp;&nbsp;&nbsp;<a href=\"javascript:help('3064')\">(Service 3064)</a></span></td></tr>");
      }

      if(StockCheckReconciliation)
      {
        scoutln(out, bytesOut, "<tr><td></td><td nowrap colspan=2><p><a href=\"/central/servlet/StockCheckReconciliation?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                             + "\">Reconcile</a> Stock Check Records</td>");
        scoutln(out, bytesOut, "<td><span id=\"service\"><p>&nbsp;&nbsp;&nbsp;<a href=\"javascript:help('3065')\">(Service 3065)</a></span></td></tr>");
      }

      if(StockCheckUploadUpdate)
      {
        scoutln(out, bytesOut, "<tr><td></td><td nowrap colspan=2><p><a href=\"/central/servlet/StockCheckUploadUpdate?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                             + "\">Import</a> Stock Check Records</td>");
        scoutln(out, bytesOut, "<td><span id=\"service\"><p>&nbsp;&nbsp;&nbsp;<a href=\"javascript:help('3021')\">(Service 3021)</a></span></td></tr>");
      }

      scoutln(out, bytesOut, "<tr><td></td><td nowrap colspan=2><p><a href=\"/central/servlet/StockCheckCheck?unm=" + unm + "&sid=" + sid + "&uty=" + uty
                             + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Stock Check Record Check</a></td></tr>");
    }

    if(ProductCycleCountSelection || StockCheckTrust || ProductCycleCountInput)
    {
      scoutln(out, bytesOut, "<tr><td colspan=5 nowrap><h1>Cycle Counting</td></tr>");

      if(ProductCycleCountSelection)
      {
        scoutln(out, bytesOut, "<tr><td></td><td nowrap colspan=2><p><a href=\"/central/servlet/ProductCycleCountSelection?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                             + "\">Configure</a> Cycle Count</td>");
        scoutln(out, bytesOut, "<td><span id=\"service\"><p>&nbsp;&nbsp;&nbsp;<a href=\"javascript:help('3081')\">(Service 3081)</a></span></td></tr>");
      }

      if(StockCheckTrust)
      {
        scoutln(out, bytesOut, "<tr><td></td><td nowrap colspan=2><p><a href=\"/central/servlet/StockCheckTrust?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                             + "\">Check</a> Manufacturer Trust Status</td>");
        scoutln(out, bytesOut, "<td><span id=\"service\"><p>&nbsp;&nbsp;&nbsp;<a href=\"javascript:help('3082')\">(Service 3082)</a></span></td></tr>");
      }

      if(ProductCycleCountInput)
      {
        scoutln(out, bytesOut, "<tr><td></td><td nowrap colspan=2><p><a href=\"/central/servlet/ProductCycleCountInput?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                             + "\">Generate and Update</a> Stock Check Records</td>");
        scoutln(out, bytesOut, "<td><span id=\"service\"><p>&nbsp;&nbsp;&nbsp;<a href=\"javascript:help('3083')\">(Service 3083)</a></span></td></tr>");
      }
    }

    if(StockCheckListing)
    {
      scoutln(out, bytesOut, "<tr><td colspan=5 nowrap><h1>Stock Check Records Access Listings</td></tr>");
      scoutln(out, bytesOut, "<tr><td></td><td><span id=\"service\"><p>&nbsp;&nbsp;&nbsp;<a href=\"javascript:help('3020')\">(Service 3020)</a></span></td></tr>");

      scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"javascript:list(1)\")>List</a> by Item Code</td></tr>");

      scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"javascript:list(2)\")>List</a> by Date</td></tr>");

      scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"javascript:list(3)\")>List</a> by Stock Check Code</td></tr>");
    }

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    
    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir));
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void getMfrsDDL(Connection con, Statement stmt, ResultSet rs, PrintWriter out, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT DISTINCT Manufacturer FROM stock ORDER BY Manufacturer");

    scoutln(out, bytesOut, "<select name=\"mfr\">");

    String mfr;

    while(rs.next())
    {
      mfr = rs.getString(1);
      if(mfr.length() > 0)
        scoutln(out, bytesOut, "<option value=\"" + mfr + "\">" + mfr);
    }

    scoutln(out, bytesOut, "</select>");

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
