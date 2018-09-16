// =======================================================================================================================================================================================================
// System: ZaraStar Document: Quotes: create focus page
// Module: QuotationServices.java
// Author: C.K.Harvey
// Copyright (c) 2001-11 Christopher Harvey. All Rights Reserved.
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

public class QuotationServices extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
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

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", iw="";
    try
    {
      unm = req.getParameter("unm");
      sid = req.getParameter("sid");
      uty = req.getParameter("uty");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");      
      iw  = req.getParameter("iw");

      if(iw == null) iw = "";

      if(iw.length() == 0)
      {
        directoryUtils.setContentHeaders(res);
        out = res.getWriter();
      }

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, iw, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "QuotationServices", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 142, bytesOut[0], 0, "ERR:");

      if(iw.length() > 0) pageFrameUtils.postToIW(iw, "ERROR: ERR");

      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String iw, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;
    
    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 142, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "142", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 142, bytesOut[0], 0, "ACC:");
      if(con != null) con.close();
      if(out != null) out.flush(); 

      if(iw.length() > 0) pageFrameUtils.postToIW(iw, "ERROR: ACC");

      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "142", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 142, bytesOut[0], 0, "SID:");
      if(con != null) con.close();
      if(out != null) out.flush(); 

      if(iw.length() > 0) pageFrameUtils.postToIW(iw, "ERROR: SID");

      return;
    }

    String[] scripts = new String[1];  scripts[0] = "";

    String s = set(con, stmt, rs, out, req, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut, iw, scripts);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 142, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con != null) con.close();

    if(iw.length() > 0) pageFrameUtils.postToIW(iw, s);

    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private String set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut,
                     String iw, String[] scripts) throws Exception
  {
    String s = "";

    scoutln(out, bytesOut, "<html><head><title>Quotation Focus</title>");

    scripts[0] += scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    boolean DataBaseTab0 = authenticationUtils.verifyAccess(con, stmt, rs, req, 4020, unm, uty, dnm, localDefnsDir, defnsDir);
      
    scripts[0] += scoutln(out, bytesOut, "function fetch(){var p1=sanitise(document.forms[0].code.value);");
    scripts[0] += scoutln(out, bytesOut, "this.location.href=\"/central/servlet/QuotationPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&iw=" + iw + "&p2=A&p3=&p4=&p1=\"+p1;}");

    if(DataBaseTab0)
    {
      scripts[0] += scoutln(out, bytesOut, "function create(type){");
      scripts[0] += scoutln(out, bytesOut, "var p1;if(type=='Q')p1='CREATEBASEDONQUOTE:'+sanitise(document.forms[0].doccode2.value);");
      scripts[0] += scoutln(out, bytesOut, "else if(type=='D')p1='CREATEBASEDONDO:'+sanitise(document.forms[0].doccode.value);");
      scripts[0] += scoutln(out, bytesOut, "this.location.href=\"/central/servlet/QuotationPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    scripts[0] += scoutln(out, bytesOut, "function list(which){");
    scripts[0] += scoutln(out, bytesOut, "var p11=sanitise(document.forms[0].companyCode.value);");
    scripts[0] += scoutln(out, bytesOut, "this.location.href=\"/central/servlet/QuotationListing?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=X&p11=\"+p11+\"&p1=\"+which;}");

    scripts[0] += scoutln(out, bytesOut, "function sanitise(code){");
    scripts[0] += scoutln(out, bytesOut, "var code2='';var x;var len=code.length;");
    scripts[0] += scoutln(out, bytesOut, "for(x=0;x<len;++x)");
    scripts[0] += scoutln(out, bytesOut, "if(code.charAt(x)=='#')code2+='%23';");
    scripts[0] += scoutln(out, bytesOut, "else if(code.charAt(x)=='\"')code2+='%22';");
    scripts[0] += scoutln(out, bytesOut, "else if(code.charAt(x)=='&')code2+='%26';");
    scripts[0] += scoutln(out, bytesOut, "else if(code.charAt(x)=='%')code2+='%25';");
    scripts[0] += scoutln(out, bytesOut, "else if(code.charAt(x)==' ')code2+='%20';");
    scripts[0] += scoutln(out, bytesOut, "else if(code.charAt(x)=='?')code+='%3F';");
    scripts[0] += scoutln(out, bytesOut, "else code2+=code.charAt(x);");
    scripts[0] += scoutln(out, bytesOut, "return code2};");
  
    scripts[0] += scoutln(out, bytesOut, "</script>");

    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"http://" + men + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    int[] hmenuCount = new int[1];

    drawingUtils.outputPageFrame(con, stmt, rs, out, req, "142", "", "QuotationServices", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount, bytesOut);

    s += drawingUtils.drawTitle(out, false, false, "QuotationServices", "", "Quotation Focus", "142", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    s += scoutln(out, bytesOut, "<form><table id=\"page\" border=0 cellspacing=2 cellpadding=0 width=100%>");

    s += scoutln(out, bytesOut, "<tr><td colspan=5 nowrap><h1>Quotation Fetch</td></tr><tr><td></td><td nowrap colspan=2><p>Enter a Quotation code</td>");
    s += scoutln(out, bytesOut, "<td nowrap><p><input type=text maxlength=20 size=20 name=code> and <a href=\"javascript:fetch()\">fetch</a></td></tr>");

    if(DataBaseTab0)
    {
      s += scoutln(out, bytesOut, "<tr><td colspan=5 nowrap><h1>New Quotations</td></tr>");

      s += scoutln(out, bytesOut, "<tr><td></td><td nowrap colspan=2><p><a href=\"/central/servlet/QuotationPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                           + "&p1=&p3=&p4=&p2=C\">Create</a> a new Quotation</td></tr>");

      if(authenticationUtils.verifyAccess(con, stmt, rs, req, 148, unm, uty, dnm, localDefnsDir, defnsDir))
      {
        s += scoutln(out, bytesOut, "<tr><td></td><td colspan=2 nowrap><p>Enter a Delivery Order code</td><td nowrap><p><input type=text maxlength=20 size=20 name=doccode>");
        s += scoutln(out, bytesOut, "and <a href=\"javascript:create('D')\">create</a> a Quotation</td><td><span id=\"service\"><p>&nbsp;&nbsp;&nbsp;<a href=\"javascript:help('4021')\">(Service 4021)</a></span></td></tr>");
      }
      
      s += scoutln(out, bytesOut, "<tr><td></td><td colspan=2><p>Enter a Quotation code</td>");
      s += scoutln(out, bytesOut, "<td nowrap><p><input type=text maxlength=20 size=20 name=doccode2> and <a href=\"javascript:create('Q')\">create</a> a Quotation</td>");
      s += scoutln(out, bytesOut, "<td><span id=\"service\"><p>&nbsp;&nbsp;&nbsp;<a href=\"javascript:help('4022')\">(Service 4022)</a></span></td></tr>");
    }

    s += scoutln(out, bytesOut, "<tr><td colspan=5 nowrap><h1>Quotation Access Listings</td></tr>");

    s += scoutln(out, bytesOut, "<tr><td></td><td nowrap colspan=2><p>Enter a Customer Code</td>");
    s += scoutln(out, bytesOut, "<td><p><input type=text maxlength=20 size=20 name=companyCode></td><td><span id=\"optional\"><p>&nbsp;&nbsp;&nbsp;(Leave blank for all)</td></tr>");

    s += scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"javascript:list(1)\")>List</a> by Quotation Code</td></tr>");
    s += scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"javascript:list(2)\")>List</a> by Quotation Date</td></tr>");
    s += scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"javascript:list(3)\")>List</a> by Customer Code</td></tr>");

    s += scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    s += scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir));

    return s;
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private String scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    if(out != null) out.println(str);
    bytesOut[0] += (str.length() + 2);
    return str + "\n";
  }

}
