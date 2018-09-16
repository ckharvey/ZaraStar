// =======================================================================================================================================================================================================
// System: ZaraStar Document: DO: create focus page
// Module: DeliveryOrderServicesWave.java
// Author: C.K.Harvey
// Copyright (c) 2001-07 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;
import java.io.*;

public class DeliveryOrderServicesWave extends HttpServlet
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "DeliveryOrderServices", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 148, bytesOut[0], 0, "ERR:");
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

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 148, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreenW(out, req, 13, unm, sid, uty, men, den, dnm, bnm, "148", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 148, bytesOut[0], 0, "ACC:");
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreenW(out, req, 12, unm, sid, uty, men, den, dnm, bnm, "148", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 148, bytesOut[0], 0, "SID:");
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    scoutln(out, bytesOut, "148\001DOs\001DO Focus\001javascript:getHTML('DeliveryOrderServicesw','')\001\001Y\001\001\003");

    set(con, stmt, rs, out, req, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 148, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut)
                   throws Exception
  {
    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    boolean PickingListServices  = authenticationUtils.verifyAccess(con, stmt, rs, req, 147,  unm, uty, dnm, localDefnsDir, defnsDir);
    boolean DataBaseMain5 = authenticationUtils.verifyAccess(con, stmt, rs, req, 4055, unm, uty, dnm, localDefnsDir, defnsDir);

    scoutln(out, bytesOut, "function fetch(){var p1=sanitise(document.forms[0].code.value);getHTML('DeliveryOrderPagew','&p2=A&p3=&p4=&p1='+p1);}");

    if(DataBaseMain5)
    {
      scoutln(out, bytesOut, "function create(type){var p1;if(type=='P')p1='CREATEBASEDOPL:'+sanitise(document.forms[0].doccode.value);getHTML('DeliveryOrderPagew','&p2=A&p3=&p4=&p1='+p1);}");
    }

    scoutln(out, bytesOut, "function list(which){var p11=sanitise(document.forms[0].companyCode.value);getHTML('DeliveryOrderListingw','&p2=X&p11='+p11+'&p1='+which);}");

    scoutln(out, bytesOut, "</script>");

    scoutln(out, bytesOut, "<link rel='stylesheet' type='text/css' media='screen' href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    drawingUtils.drawTitleW(out, false, false, "DeliveryOrderServices", "", "Delivery Order Focus", "148", "", "", bytesOut);

    scoutln(out, bytesOut, "<form><table id='page' border=0 cellspacing=2 cellpadding=0 width=100%>");

    scoutln(out, bytesOut, "<tr><td colspan=5 nowrap><h1>Delivery Order Fetch</td></tr><tr><td></td><td colspan=2><p>Enter a Delivery Order code</td>");
    scoutln(out, bytesOut, "<td nowrap><p><input type=text maxlength=20 size=20 name=code> and <a href=\"javascript:fetch()\">fetch</a></td></tr>");

    if(DataBaseMain5)
    {
      scoutln(out, bytesOut, "<tr><td colspan=5 nowrap><h1>New Delivery Orders</td></tr>");

      scoutln(out, bytesOut, "<tr><td></td><td nowrap colspan=2><p><a href=\"javascript:getHTML('DeliveryOrderPagew','&p1=&p3=&p4=&p2=C')\">Create</a> a new Delivery Order</td></tr>");

      if(PickingListServices)
      {
        scoutln(out, bytesOut, "<tr><td></td><td colspan=2 nowrap><p>Enter a Picking List code</td>");
        scoutln(out, bytesOut, "<td nowrap><p><input type=text maxlength=20 size=20 name=doccode> and <a href=\"javascript:create('P')\">Create</a> a Delivery Order</td></tr>");
      }
    }

    scoutln(out, bytesOut, "<tr><td colspan=5 nowrap><h1>Delivery Order Access Listings</td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td colspan=2><p>Enter a Customer Code</td><td><p><input type=text maxlength=20 size=20 name=companyCode></td><td><span id='optional'><p>&nbsp;&nbsp;&nbsp;(Leave blank for all)</td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"javascript:list(1)\")>List</a> by Delivery Order Code</td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"javascript:list(2)\")>List</a> by Delivery Order Date</td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"javascript:list(3)\")>List</a> by Customer Code</td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap colspan=2><p><a href=\"javascript:list(4)\")>List</a> by Delivery Order Code (showing Customer PO Code)</td></tr>");

    boolean DeliveryOrderUpdating = authenticationUtils.verifyAccess(con, stmt, rs, req, 2043, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean DeliveryOrdersReturned = authenticationUtils.verifyAccess(con, stmt, rs, req, 1019, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean DeliveryOrdersNotInvoiced = authenticationUtils.verifyAccess(con, stmt, rs, req, 1020, unm, uty, dnm, localDefnsDir, defnsDir);

    if(DeliveryOrderUpdating || DeliveryOrdersReturned || DeliveryOrdersNotInvoiced)
    {
      scoutln(out, bytesOut, "<tr><td nowrap colspan=5><h1>Update Services</td></tr>");

      if(DeliveryOrderUpdating)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap colspan=2><p><a href=\"javascript:getHTML('DeliveryOrderUpdatingw','');\">Delivery Order Processing - Update Returns</a></td></tr>");
      }

      if(DeliveryOrdersReturned)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap colspan=2><p><a href=\"javascript:getHTML('DeliveryOrdersReturnedw','')\">Delivery Orders Returned for a Date</a></td></tr>");
      }

      if(DeliveryOrdersNotInvoiced)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap colspan=2><p><a href=\"javascript:getHTML('DeliveryOrdersNotInvoicedw','');\">Delivery Orders Not Invoiced</a></td></tr>");
      }
    }

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "</table></form>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {
    out.println(str);
    bytesOut[0] += (str.length() + 2);
  }

}
