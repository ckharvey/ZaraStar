// =======================================================================================================================================================================================================
// System: ZaraStar: Purchases Analytics
// Module: PurchasesAnalytics.java
// Author: C.K.Harvey
// Copyright (c) 2002-07 Christopher Harvey. All Rights Reserved.
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

public class PurchasesAnalytics extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
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
 
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "PurchasesAnalytics", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 122, bytesOut[0], 0, "ERR:");
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
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "PurchasesAnalytics", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 122, bytesOut[0], 0, "ACC:");
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "PurchasesAnalytics", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 122, bytesOut[0], 0, "SID:");
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    set(con, stmt, rs, out, req, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 122, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm,
                   String bnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Purchases Analytics</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");
  
    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\""
                           + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\"></head>");
  
    int[] hmenuCount = new int[1];
    
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "122", "", "PurchasesAnalytics", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
                                
    scoutln(out, bytesOut, "<form>");
  
    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Purchases Analytics", "122",  unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<table id=\"page\" width=100% border=0>");

    boolean ConsolidatedCreditorsInput = authenticationUtils.verifyAccess(con, stmt, rs, req, 1031, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean PurchaseInvoiceListing = authenticationUtils.verifyAccess(con, stmt, rs, req, 1034, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean TrackTracePurchaseOrders = authenticationUtils.verifyAccess(con, stmt, rs, req, 2022, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean SalesAnalytics3 = authenticationUtils.verifyAccess(con, stmt, rs, req, 1203, unm, uty, dnm, localDefnsDir, defnsDir);
            
    if(ConsolidatedCreditorsInput || TrackTracePurchaseOrders || PurchaseInvoiceListing || SalesAnalytics3)
    {
      scoutln(out, bytesOut, "<tr><td nowrap colspan=3><h1>Supplier Services</td></tr>");

      if(SalesAnalytics3)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/SalesAnalytics3?unm=" + unm + "&sid=" + sid + "&uty="
                              + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                              + "\">Purchases: Closure Analysis</a></td><td width=70%><span id=\"service\">"
                              + "&nbsp;&nbsp;&nbsp;<a href=\"javascript:help('1203')\">(Service 1203)</a></span></td></tr>");
      }

      if(ConsolidatedCreditorsInput)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/ConsolidatedCreditorsInput?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm="
                              + bnm + "\">Consolidated Creditors</a></td><td width=70%><span id=\"service\">"
                              + "&nbsp;&nbsp;&nbsp;<a href=\"javascript:help('1031')\">(Service 1031)</a></span></td></tr>");
      }

      if(TrackTracePurchaseOrders)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/TrackTracePurchaseOrders?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                             + "\">Purchase Order Track and Trace</a></td><td width=70%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                             + "<a href=\"javascript:help('2022')\">(Service 2022)</a></span></td></tr>");
      }  
      
      if(PurchaseInvoiceListing)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/PurchaseInvoiceListing?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                             + "\">Purchase Invoice Lines Enquiry by Date</a></td><td width=70%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                             + "<a href=\"javascript:help('1034')\">(Service 1034)</a></span></td></tr>");
      }  
    }
    
    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir));
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
