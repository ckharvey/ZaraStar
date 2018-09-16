// =======================================================================================================================================================================================================
// System: ZaraStar Document: OA: create focus page
// Module: OrderAcknowledgementServices.java
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

public class OrderAcknowledgementServices extends HttpServlet
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "OrderAcknowledgementServices", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 170, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                    int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;
    
    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 170, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "170", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 170, bytesOut[0], 0, "ACC:");
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "170", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 170, bytesOut[0], 0, "SID:");
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    set(con, stmt, rs, out, req, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 170, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty,
                   String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Order Acknowledgement Focus</title>");
    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    boolean SalesOrderServices  = authenticationUtils.verifyAccess(con, stmt, rs, req, 140,  unm, uty, dnm, localDefnsDir, defnsDir);
    boolean _4131 = authenticationUtils.verifyAccess(con, stmt, rs, req, 4131, unm, uty, dnm, localDefnsDir, defnsDir);
      
    scoutln(out, bytesOut, "function fetch(){var p1=sanitise(document.forms[0].code.value);");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/OrderAcknowledgementPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");

    if(_4131)
    {
      scoutln(out, bytesOut, "function create(type){var p1;if(type=='S')p1='CREATEBASEDONSO:'+sanitise(document.forms[0].doccode.value);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/SalesOrderToOrderAcknowledgement?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    scoutln(out, bytesOut, "function list(which){var p11=sanitise(document.forms[0].companyCode.value);");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/OrderAcknowledgementListing?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=X&p11=\"+p11+\"&p1=\"+which;}");

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

    drawingUtils.outputPageFrame(con, stmt, rs, out, req, "170", "", "OrderAcknowledgementServices", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount, bytesOut);

    drawingUtils.drawTitle(out, false, false, "OrderAcknowledgementServices", "", "Order Acknowledgement Focus", "170", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<form><table id=\"page\" border=0 cellspacing=2 cellpadding=0 width=100%>");

    scoutln(out, bytesOut, "<tr><td colspan=5 nowrap><h1>Order Acknowledgement Fetch</td></tr>");
    scoutln(out, bytesOut, "<tr><td></td><td colspan=2 nowrap><p>Enter an Order Acknowledgement code</td>");
    scoutln(out, bytesOut, "<td nowrap><p><input type=text maxlength=20 size=20 name=code> and <a href=\"javascript:fetch()\">fetch</a></td></tr>");
   
    if(_4131)
    {
      scoutln(out, bytesOut, "<tr><td colspan=5 nowrap><h1>New Order Acknowledgements</td></tr>");

      scoutln(out, bytesOut, "<tr><td></td><td nowrap colspan=2><p><a href=\"/central/servlet/OrderAcknowledgementPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                            + "&p1=&p3=&p4=&p2=C\">Create</a> a new Order Acknowledgement</td></tr>");

      if(SalesOrderServices)
      {
        scoutln(out, bytesOut, "<tr><td></td><td colspan=2><p>Enter a Sales Order code</td>");
        scoutln(out, bytesOut, "<td nowrap><p><input type=text maxlength=20 size=20 name=doccode> and <a href=\"javascript:create('S')\">create</a> an Order Acknowledgement</td>");
        scoutln(out, bytesOut, "<td><span id=\"service\"><p>&nbsp;&nbsp;&nbsp;<a href=\"javascript:help('4132')\">(Service 4132)</a></span></td></tr>");
      }
    }

    scoutln(out, bytesOut, "<tr><td colspan=5 nowrap><h1>Order Acknowledgement Access Listings</td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td colspan=2><p>Enter a Customer Code</td>");
    scoutln(out, bytesOut, "<td><p><input type=text maxlength=20 size=20 name=companyCode></td>");
    scoutln(out, bytesOut, "<td><span id=\"optional\"><p>&nbsp;&nbsp;&nbsp;(Leave blank for all)</td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"javascript:list(1)\")>List</a> by Order Acknowledgement Code</td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"javascript:list(2)\")>List</a> by Order Acknowledgement Date</td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"javascript:list(3)\")>List</a> by Customer Code</td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"javascript:list(4)\")>List</a> by Customer PO Code</td></tr>");
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir));
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }
  
}
