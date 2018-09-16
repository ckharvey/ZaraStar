// =======================================================================================================================================================================================================
// System: ZaraStar DocumentEngine: GR: create focus page
// Module: GoodsReceivedServices.java
// Author: C.K.Harvey
// Copyright (c) 2001-09 Christopher Harvey. All Rights Reserved.
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

public class GoodsReceivedServices extends HttpServlet
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "GoodsReceivedServices", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 153, bytesOut[0], 0, "ERR:");
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
    
    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 153, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "153", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 153, bytesOut[0], 0, "ACC:");
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "153", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 153, bytesOut[0], 0, "SID:");
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    set(con, stmt, rs, out, req, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 153, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut)
                   throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Goods Received Note Focus</title>");
    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    boolean _3026 = authenticationUtils.verifyAccess(con, stmt, rs, req, 3026, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean _5007 = authenticationUtils.verifyAccess(con, stmt, rs, req, 5007, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean _5017 = authenticationUtils.verifyAccess(con, stmt, rs, req, 5017, unm, uty, dnm, localDefnsDir, defnsDir);
      
    scoutln(out, bytesOut, "function fetch(){var p1=sanitise(document.forms[0].code.value);");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/GoodsReceivedPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");

    if(_5007)
    {
      scoutln(out, bytesOut, "function buildP(){var p1=sanitise(document.forms[0].doccode.value);var p2=sanitise(document.forms[0].doccodepo.value);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/_3033?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=\"+p2+\"&p1=\"+p1;}");
    }
  
    if(_5017)
    {
      scoutln(out, bytesOut, "function buildL(){var p1=sanitise(document.forms[0].doccode2.value);var p2=sanitise(document.forms[0].doccodelp.value);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/GoodsReceivedBuilderPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=\"+p2+\"&p1=\"+p1;}");
    }

    scoutln(out, bytesOut, "function listbydates(){p1=sanitise(document.forms[0].dateFrom.value);p2=sanitise(document.forms[0].dateTo.value);");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/GoodsReceivedMonthlyListing?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+p1+\"&p2=\"+p2;}");
    
    scoutln(out, bytesOut, "function list(which){");
    scoutln(out, bytesOut, "var p11=sanitise(document.forms[0].companyCode.value);");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/GoodsReceivedListing?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=X&p11=\"+p11+\"&p1=\"+which;}");

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

    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    int[] hmenuCount = new int[1];

    drawingUtils.outputPageFrame(con, stmt, rs, out, req, "153", "", "GoodsReceivedServices", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount, bytesOut);

    drawingUtils.drawTitle(out, false, false, "GoodsReceivedServices", "", "Goods Received Note Focus", "153", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<form><table id=\"page\" border=0 cellspacing=2 cellpadding=0 width=100%>");

    scoutln(out, bytesOut, "<tr><td colspan=5 nowrap><h1>Goods Received Note Fetch</td></tr>");
    scoutln(out, bytesOut, "<tr><td></td><td colspan=2><p>Enter a Goods Received Note code</td>");
    scoutln(out, bytesOut, "<td nowrap><p><input type=text maxlength=20 size=20 name=code>");
    scoutln(out, bytesOut, "and <a href=\"javascript:fetch()\">fetch</a></td></tr>");

    if(_3026 || _5007 || _5017)
    {
      scoutln(out, bytesOut, "<tr><td colspan=5 nowrap><h1>New Goods Received Notes</td></tr>");

      if(_3026)
      {
        scoutln(out, bytesOut, "<tr><td></td><td nowrap colspan=2><p><a href=\"/central/servlet/GoodsReceivedPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                             + "&p1=&p3=&p4=&p2=C\">Create</a> a new Goods Received Note</td></tr>");
      }

      if(_5007)
      {
        scoutln(out, bytesOut, "<tr><td></td><td nowrap colspan=2><p><a href=\"javascript:buildP()\">Build</a> from POs, given a Supplier Code</td>");
        scoutln(out, bytesOut, "<td nowrap><p><input type=text maxlength=20 size=20 name=doccode></td>");
        scoutln(out, bytesOut, "<td><span id=\"service\"><p>&nbsp;&nbsp;&nbsp;<a href=\"javascript:help('3033')\">(Service 3033)</a></span></td></tr>");

        scoutln(out, bytesOut, "<tr><td></td><td nowrap colspan=2><p><a href=\"javascript:buildP()\">Build</a> from one PO, given that PO Code</td>");
        scoutln(out, bytesOut, "<td nowrap><p><input type=text maxlength=20 size=20 name=doccodepo></td><td><span id=\"service\"><p>&nbsp;&nbsp;&nbsp;<a href=\"javascript:help('3033')\">(Service 3033)</a></span></td></tr>");
      }

      if(_5017)
      {
        scoutln(out, bytesOut, "<tr><td></td><td nowrap colspan=2><p><a href=\"javascript:buildL()\">Build</a> from LPs, given a Supplier Code</td>");
        scoutln(out, bytesOut, "<td nowrap><p><input type=text maxlength=20 size=20 name=doccode2></td>");
        scoutln(out, bytesOut, "<td><span id=\"service\"><p>&nbsp;&nbsp;&nbsp;<a href=\"javascript:help('3034')\">(Service 3034)</a></span></td></tr>");

        scoutln(out, bytesOut, "<tr><td></td><td nowrap colspan=2><p><a href=\"javascript:buildL()\">Build</a> from one LP, given that LP Code</td>");
        scoutln(out, bytesOut, "<td nowrap><p><input type=text maxlength=20 size=20 name=doccodelp></td><td><span id=\"service\"><p>&nbsp;&nbsp;&nbsp;<a href=\"javascript:help('3034')\">(Service 3034)</a></span></td></tr>");
      }
    }

    scoutln(out, bytesOut, "<tr><td colspan=5 nowrap><h1>Goods Received Note Access Listings</td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td colspan=2><p>Enter a Supplier Code</td>");
    scoutln(out, bytesOut, "<td><p><input type=text maxlength=20 size=20 name=companyCode></td>");
    scoutln(out, bytesOut, "<td><span id=\"optional\"><p>&nbsp;&nbsp;&nbsp;(Leave blank for all)</td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"javascript:list(1)\")>List</a> by Goods Received Note Code</td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"javascript:list(2)\")>List</a> by Goods Received Note Date</td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"javascript:list(3)\")>List</a> by Supplier Code</td></tr>");

    scoutln(out, bytesOut, "<tr><td colspan=5 nowrap><h1>Goods Received Note Reports</td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap colspan=2 valign=top><p><a href=\"javascript:listbydates()\">List</a> Goods Received Notes by Date:</td>");

    scoutln(out, bytesOut, "<td nowrap colspan=5><p>From &nbsp; &nbsp;<input type=text maxlength=10 size=10 name=dateFrom>");
    scoutln(out, bytesOut, "and To &nbsp; &nbsp;<input type=text maxlength=10 size=10 name=dateTo></td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir));
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
