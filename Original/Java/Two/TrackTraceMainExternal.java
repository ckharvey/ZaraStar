// =======================================================================================================================================================================================================
// System: ZaraStar Documents: TNT main page - ext users
// Module: TrackTraceMainExternal.java
// Author: C.K.Harvey
// Copyright (c) 2003-09 Christopher Harvey. All Rights Reserved.
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

public class TrackTraceMainExternal extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  Customer customer = new Customer();
  Supplier supplier = new Supplier();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  Profile profile = new Profile();
  AdminControlUtils adminControlUtils = new AdminControlUtils();
  
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
      out = res.getWriter();
      directoryUtils.setContentHeaders(res);

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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "TrackTraceMainExternal", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2012, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush(); 
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! adminControlUtils.notDisabled(con, stmt, rs, 903))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "TrackTraceMainExternal", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2012, bytesOut[0], 0, "ACC:");
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "TrackTraceMainExternal", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2012, bytesOut[0], 0, "SID:");
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    set(con, stmt, rs, out, req, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 2012, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut)
                   throws Exception
  {
    String supplierCode = "";

    scoutln(out, bytesOut, "<html><head><title>Transaction Track &amp; Trace</title>");
    
    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");
    
    scoutln(out, bytesOut, "function docSearch(which){");
    scoutln(out, bytesOut, "var docCode;if(which=='Q')docCode=document.forms[0].docCodeQ.value;");
    scoutln(out, bytesOut, "else if(which=='S')docCode=document.forms[0].docCodeS.value;");
    scoutln(out, bytesOut, "else if(which=='D')docCode=document.forms[0].docCodeD.value;");
    scoutln(out, bytesOut, "else if(which=='P')docCode=document.forms[0].docCodeP.value;");
    scoutln(out, bytesOut, "else if(which=='I')docCode=document.forms[0].docCodeI.value;");
    scoutln(out, bytesOut, "else if(which=='R')docCode=document.forms[0].docCodeR.value;");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/TrackTraceCodeSearch?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p3=" + generalUtils.sanitise(supplierCode)
                         + "&p1=\"+docCode+\"&p2=\"+which+\"&bnm=" + bnm + "\";}");

    scoutln(out, bytesOut, "function search(){");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/MainPageUtils8?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\";}");
    
    scoutln(out, bytesOut, "function view(which){");

    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/TrackTraceDocumentListExternal?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p2=" + generalUtils.sanitise(supplierCode) + "&p1=\"+which+\"&bnm=" + bnm
                         + "\";}");

    scoutln(out, bytesOut, "</script>");

    int[] hmenuCount = new int[1];
 
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "2012", "", "TrackTraceMainExternal", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount,
                          bytesOut);

    String[] name         = new String[1];
    String[] companyName  = new String[1];
    String[] accessRights = new String[1];
    int i = unm.indexOf("_");

    profile.getExternalAccessNameCompanyAndRights(con, stmt, rs, unm.substring(0, i), name, companyName, accessRights);

    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Transaction Track &amp; Trace for " + name[0] + " of " + companyName[0], "2012", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);
   
    scoutln(out, bytesOut, "<form><table id=\"page\" cellspacing=2 cellpadding=0 border=0 width=100%>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    // -------------------------------------------------------------
    
    scoutln(out, bytesOut, "<tr><td nowrap colspan=3><h1>Document Line Search</td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap colspan=2><p><a href=\"javascript:search()\">Search</a> Document Lines for a Word or Phrase</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    
    // -------------------------------------------------------------
    
    scoutln(out, bytesOut, "<tr><td nowrap colspan=3><h1>Quotations</td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>Enter a Quotation Code </td><td nowrap width=99%><p><input type=text size=20 maxlength=20 name=docCodeQ>");
    scoutln(out, bytesOut, " and <a href=\"javascript:docSearch('Q')\">Search</a></td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td></td><td><p><a href=\"javascript:view('Q')\">List</a> all Quotations on file</td></tr>");
    
    // -------------------------------------------------------------
    
    scoutln(out, bytesOut, "<tr><td nowrap colspan=3><h1>Sales Orders</td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>Enter your Purchase Order Code </td><td nowrap><p><input type=text size=20 maxlength=40 name=docCodeP>");
    scoutln(out, bytesOut, " and <a href=\"javascript:docSearch('P')\">Search</a></td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>Enter a Sales Order Code </td><td nowrap><p><input type=text size=20 maxlength=20 name=docCodeS>");
    scoutln(out, bytesOut, " and <a href=\"javascript:docSearch('S')\">Search</a></td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td></td><td><p><a href=\"javascript:view('S')\">List</a> all Sales Orders on file</td></tr>");

    // -------------------------------------------------------------
    
    scoutln(out, bytesOut, "<tr><td nowrap colspan=3><h1>Delivery Orders</td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>Enter a Delivery Order Code </td><td nowrap><p><input type=text size=20 maxlength=20 name=docCodeD>");
    scoutln(out, bytesOut, " and <a href=\"javascript:docSearch('D')\">Search</a></td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td></td><td><p><a href=\"javascript:view('D')\">List</a> all Delivery Orders on file</td></tr>");

    // -------------------------------------------------------------
    
    scoutln(out, bytesOut, "<tr><td nowrap colspan=3><h1>Invoices</td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>Enter an Invoice Code </td><td nowrap><p><input type=text size=20 maxlength=20 name=docCodeI>");
    scoutln(out, bytesOut, " and <a href=\"javascript:docSearch('I')\">Search</a></td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td></td><td><p><a href=\"javascript:view('I')\">List</a> all Invoices on file</td></tr>");
    
    // -------------------------------------------------------------
    
    scoutln(out, bytesOut, "<tr><td nowrap colspan=3><h1>Proforma Invoices</td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>Enter a Proforma Invoice Code </td><td nowrap><p><input type=text size=20 maxlength=20 name=docCodeR>");
    scoutln(out, bytesOut, " and <a href=\"javascript:docSearch('R')\">Search</a></td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td></td><td><p><a href=\"javascript:view('R')\">List</a> all Proforma Invoices on file</td></tr>");
    
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
