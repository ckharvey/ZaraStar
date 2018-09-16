// =======================================================================================================================================================================================================
// System: ZaraStar: Sales Control
// Module: SalesControlServices.java
// Author: C.K.Harvey
// Copyright (c) 2002-09 Christopher Harvey. All Rights Reserved.
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

public class SalesControlServices extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  
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
 
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "SalesControlServices", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 183, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush(); 
    }
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
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
    
    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 183, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "SalesControlServices", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 183, bytesOut[0], 0, "ACC:");
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "SalesControlServices", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 183, bytesOut[0], 0, "SID:");
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    set(con, stmt, rs, out, req, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 183, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir,
                   int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Sales Control</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");
  
    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\"></head>");
  
    int[] hmenuCount = new int[1];
    
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "183", "", "SalesControlServices", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
                                
    scoutln(out, bytesOut, "<form>");
  
    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Sales Control", "183", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<table id=\"page\" width=100% border=0>");

    boolean QuotationSales = authenticationUtils.verifyAccess(con, stmt, rs, req, 2029, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean SalesOrderReview = authenticationUtils.verifyAccess(con, stmt, rs, req, 2039, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean SalesOrdersProjects = authenticationUtils.verifyAccess(con, stmt, rs, req, 2027, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean SalesSalesManager = authenticationUtils.verifyAccess(con, stmt, rs, req, 2030, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean SalesOrderEngineering = authenticationUtils.verifyAccess(con, stmt, rs, req, 2031, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean SalesOrderPurchasing = authenticationUtils.verifyAccess(con, stmt, rs, req, 2032, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean SalesOrderScheduling = authenticationUtils.verifyAccess(con, stmt, rs, req, 2033, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean SalesOrderCoordinating = authenticationUtils.verifyAccess(con, stmt, rs, req, 2034, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean SalesOrdersInvoicing = authenticationUtils.verifyAccess(con, stmt, rs, req, 2037, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean SalesOrderTrades = authenticationUtils.verifyAccess(con, stmt, rs, req, 2042, unm, uty, dnm, localDefnsDir, defnsDir);

    if(QuotationSales || SalesOrderReview || SalesOrdersProjects || SalesSalesManager || SalesOrderEngineering || SalesOrderPurchasing || SalesOrderScheduling || SalesOrderCoordinating || SalesOrdersInvoicing || SalesOrderTrades)
    {
      scoutln(out, bytesOut, "<tr><td nowrap colspan=3><h1>Control Services</td></tr>");

      if(SalesOrderReview)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap colspan=2><p><a href=\"/central/servlet/SalesOrderReview?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                             + "\">Sales Order Processing - Contract Review Team</a></td><td width=70%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                             + "<a href=\"javascript:help('2039')\">(Service 2039)</a></span></td></tr>");
      }

      if(SalesOrdersProjects)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap colspan=2><p><a href=\"/central/servlet/SalesOrdersProjects?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                             + "\">Sales Order Processing - Sales</a></td><td width=70%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                             + "<a href=\"javascript:help('2027')\">(Service 2027)</a></span></td></tr>");
      }

      if(SalesOrderTrades)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap colspan=2><p><a href=\"/central/servlet/SalesOrderTrades?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                             + "\">Sales Order Processing - Sales - Trades</a></td><td width=70%><span id=\"service\">&nbsp;&nbsp;&nbsp;<a href=\"javascript:help('2042')\">(Service 2042)</a></span></td></tr>");
      }

      if(SalesSalesManager)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap colspan=2><p><a href=\"/central/servlet/SalesSalesManager?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                             + "\">Sales Order Processing - Sales Manager</a></td><td width=70%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                             + "<a href=\"javascript:help('2030')\">(Service 2030)</a></span></td></tr>");
      }

      if(SalesOrderEngineering)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap colspan=2><p><a href=\"/central/servlet/SalesOrderEngineering?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                             + "\">Sales Order Processing - Engineering</a></td><td width=70%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                             + "<a href=\"javascript:help('2031')\">(Service 2031)</a></span></td></tr>");
      }

      if(SalesOrderPurchasing)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap colspan=2><p><a href=\"/central/servlet/SalesOrderPurchasing?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                             + "\">Sales Order Processing - Purchasing</a></td><td width=70%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                             + "<a href=\"javascript:help('2032')\">(Service 2032)</a></span></td></tr>");
      }

      if(SalesOrderScheduling)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap colspan=2><p><a href=\"/central/servlet/SalesOrderScheduling?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                             + "\">Sales Order Processing - Scheduling</a></td><td width=70%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                             + "<a href=\"javascript:help('2033')\">(Service 2033)</a></span></td></tr>");
      }

      if(SalesOrderCoordinating)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap colspan=2><p><a href=\"/central/servlet/SalesOrderCoordinating?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                             + "\">Sales Order Processing - Coordinating</a></td><td width=70%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                             + "<a href=\"javascript:help('2034')\">(Service 2034)</a></span></td></tr>");
      }

      if(SalesOrdersInvoicing)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap colspan=2><p><a href=\"/central/servlet/SalesOrdersInvoicing?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                             + "\">Sales Order Processing - Invoicing</a></td><td width=70%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                             + "<a href=\"javascript:help('2037')\">(Service 2037)</a></span></td></tr>");
      }
    }
    
    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir));
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
