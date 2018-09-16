// =======================================================================================================================================================================================================
// System: ZaraStar: Sales Analytics
// Module: SalesAnalytics.java
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

public class SalesAnalytics extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  DefinitionTables definitionTables = new DefinitionTables();
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
 
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "SalesAnalytics", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 120, bytesOut[0], 0, "ERR:");
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
    
    if(! adminControlUtils.anyOfACategory(con, stmt, rs, 7513, unm, uty, dnm))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "SalesAnalytics", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 120, bytesOut[0], 0, "ACC:");
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "SalesAnalytics", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 120, bytesOut[0], 0, "SID:");
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    set(con, stmt, rs, out, req, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 120, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut)
                   throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Sales Analytics</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");
  
    scoutln(out, bytesOut, "function detail(){");
    scoutln(out, bytesOut, "var p1=document.forms[0].month1.value;");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/SalesAnalyticsIntake?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=\"+p1+\"&bnm=" + bnm + "\";}");

    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\"></head>");
  
    int[] hmenuCount = new int[1];
    
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "120", "", "SalesAnalytics", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
                                
    scoutln(out, bytesOut, "<form>");
  
    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Sales Analytics", "120",  unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<table id=\"page\" width=100% border=0>");

    boolean ConsolidatedDebtorsInput = authenticationUtils.verifyAccess(con, stmt, rs, req, 1029, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean SalesInvoiceListingInput = authenticationUtils.verifyAccess(con, stmt, rs, req, 1023, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean SalesInvoiceListing = authenticationUtils.verifyAccess(con, stmt, rs, req, 1033, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean TrackTraceSalesOrders = authenticationUtils.verifyAccess(con, stmt, rs, req, 2021, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean TrackTraceQuotations = authenticationUtils.verifyAccess(con, stmt, rs, req, 2024, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean TrackTracePickingLists = authenticationUtils.verifyAccess(con, stmt, rs, req, 2025, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean SalesAnalyticsIntake = authenticationUtils.verifyAccess(con, stmt, rs, req, 1200, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean SalesAnalyticsBookOrders = authenticationUtils.verifyAccess(con, stmt, rs, req, 1201, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean SalesClosureAnalysisInput = authenticationUtils.verifyAccess(con, stmt, rs, req, 1202, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean StockSalesGrossMargin = authenticationUtils.verifyAccess(con, stmt, rs, req, 1204, unm, uty, dnm, localDefnsDir, defnsDir);
        
    if(SalesAnalyticsBookOrders || SalesClosureAnalysisInput || StockSalesGrossMargin || ConsolidatedDebtorsInput || SalesInvoiceListingInput || SalesInvoiceListing || TrackTraceSalesOrders || TrackTraceQuotations || TrackTracePickingLists)
    {
      scoutln(out, bytesOut, "<tr><td nowrap colspan=3><h1>Customer Services</td></tr>");

      if(SalesAnalyticsBookOrders)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/SalesAnalyticsBookOrders?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                              + "\">Sales: Book Orders</a></td><td width=70%><span id=\"service\">&nbsp;&nbsp;&nbsp;<a href=\"javascript:help('1201')\">(Service 1201)</a></span></td></tr>");
      }

      if(SalesClosureAnalysisInput)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/SalesClosureAnalysisInput?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                              + "\">Sales: Closure Analysis</a></td><td width=70%><span id=\"service\">&nbsp;&nbsp;&nbsp;<a href=\"javascript:help('1202')\">(Service 1202)</a></span></td></tr>");
      }

      if(SalesAnalyticsIntake)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/SalesAnalyticsIntake?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                               + "\">Sales: Intake</a></td><td width=70%><span id=\"service\">&nbsp;&nbsp;&nbsp;<a href=\"javascript:help('1200')\">(Service 1200)</a></span></td></tr>");
      }

      if(StockSalesGrossMargin)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/StockSalesGrossMargin?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                              + "\">Stock Sales: Gross Margin Analysis</a></td><td width=70%><span id=\"service\">&nbsp;&nbsp;&nbsp;<a href=\"javascript:help('1204')\">(Service 1204)</a></span></td></tr>");
      }

      if(ConsolidatedDebtorsInput)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/ConsolidatedDebtorsInput?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                              + "\">Consolidated Debtors</a></td><td width=70%><span id=\"service\">&nbsp;&nbsp;&nbsp;<a href=\"javascript:help('1029')\">(Service 1029)</a></span></td></tr>");
      }

      if(TrackTraceQuotations)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/TrackTraceQuotations?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                               + "\">Quotation Track and Trace</a></td><td width=70%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                               + "<a href=\"javascript:help('2024')\">(Service 2024)</a></span></td></tr>");
      }

      if(TrackTraceSalesOrders)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/TrackTraceSalesOrders?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                               + "\">Sales Order Track and Trace</a></td><td width=70%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                               + "<a href=\"javascript:help('2021')\">(Service 2021)</a></span></td></tr>");
      }

      if(TrackTracePickingLists)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/TrackTracePickingLists?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                               + "\">Picking List Track and Trace</a></td><td width=70%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                               + "<a href=\"javascript:help('2025')\">(Service 2025)</a></span></td></tr>");
      }

      if(SalesInvoiceListingInput)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/SalesInvoiceListingInput?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                               + "\">Sales Invoice Listing Enquiry by SalesPerson</a></td><td width=70%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                               + "<a href=\"javascript:help('1023')\">(Service 1023)</a></span></td></tr>");
      }

      if(SalesInvoiceListing)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/SalesInvoiceListing?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                               + "\">Sales Invoice Listing Enquiry by Date</a></td><td width=70%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                               + "<a href=\"javascript:help('1033')\">(Service 1033)</a></span></td></tr>");
      }
    }

    boolean ChartSalesOrdersPastDue = authenticationUtils.verifyAccess(con, stmt, rs, req, 1100, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean ChartSalesInvoiceTurnover = authenticationUtils.verifyAccess(con, stmt, rs, req, 1103, unm, uty, dnm, localDefnsDir, defnsDir);

    if(ChartSalesOrdersPastDue || ChartSalesInvoiceTurnover)
    {
      scoutln(out, bytesOut, "<tr><td nowrap colspan=3><h1>Charts</td></tr>");

      if(authenticationUtils.verifyAccess(con, stmt, rs, req, 1100, unm, uty, dnm, localDefnsDir, defnsDir))
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td><p><a href=\"/central/servlet/ChartSalesOrdersPastDue?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                             + "\">Sales Orders Past Due</a></td><td width=70%><span id=\"service\">&nbsp;&nbsp;&nbsp;<a href=\"javascript:help('1100')\">(Service 1100)</a></span></td></tr>");
      }

      if(authenticationUtils.verifyAccess(con, stmt, rs, req, 1103, unm, uty, dnm, localDefnsDir, defnsDir))
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td><p><a href=\"/central/servlet/ChartSalesInvoiceTurnover?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                             + "\">Sales Invoice Turnover</a></td><td width=70%><span id=\"service\">&nbsp;&nbsp;&nbsp;<a href=\"javascript:help('1103')\">(Service 1103)</a></span></td></tr>");
      }
    }

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
