// =======================================================================================================================================================================================================
// System: ZaraStar Utils: Creditor Settlement services access page
// Module: CreditorSettlementServices.java
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

public class CreditorSettlementServices extends HttpServlet
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
 
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "CreditorSettlementServices", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 133, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den,
                    String dnm, String bnm, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    
    Statement stmt = null;
    ResultSet rs   = null;
    
    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 133, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "CreditorSettlementServices", imagesDir, localDefnsDir,
                         defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 133, bytesOut[0], 0, "ACC:");
      
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "CreditorSettlementServices", imagesDir, localDefnsDir,
                         defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 133, bytesOut[0], 0, "SID:");
      
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    set(con, stmt, rs, out, req, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 133, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
      
      if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den,
                   String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Creditor Settlement Services Access</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\""
                           + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\"></head>");
  
    int[] hmenuCount = new int[1];
    
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "133", "", "CreditorSettlementServices", unm, sid, uty, men, den, dnm, bnm, localDefnsDir,
                          defnsDir, hmenuCount, bytesOut);
                                
    scoutln(out, bytesOut, "<form>");
  
    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Creditor Settlement Services", "133", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<table id=\"page\" width=100% border=0>");

    boolean SupplierSettlementHistoryInput = authenticationUtils.verifyAccess(con, stmt, rs, req, 5002, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean CAAReport = authenticationUtils.verifyAccess(con, stmt, rs, req, 1013, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean ConsolidatedCreditorsInput = authenticationUtils.verifyAccess(con, stmt, rs, req, 1031, unm, uty, dnm, localDefnsDir, defnsDir);
        
    if(SupplierSettlementHistoryInput || CAAReport || ConsolidatedCreditorsInput)
    {
      scoutln(out, bytesOut, "<tr><td nowrap colspan=3><h1>Creditor Settlement Services</td></tr>");

      if(authenticationUtils.verifyAccess(con, stmt, rs, req, 5002, unm, uty, dnm, localDefnsDir, defnsDir))
      {  
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/SupplierSettlementHistoryInput?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=&p2=N&bnm="
                              + bnm + "\">View</a> Settlement History</td><td width=70%><span id=\"service\">"
                              + "&nbsp;&nbsp;&nbsp;<a href=\"javascript:help('5002')\">(Service 5002)</a></span></td></tr>");

        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/SupplierSettlementHistoryInput?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=&p2=C&bnm="
                              + bnm + "\">View</a> Settlement History for CASH Transactions</td><td width=70%><span id=\"service\">"
                              + "&nbsp;&nbsp;&nbsp;<a href=\"javascript:help('5002')\">(Service 5002)</a></span></td></tr>");
      }

      if(authenticationUtils.verifyAccess(con, stmt, rs, req, 1013, unm, uty, dnm, localDefnsDir, defnsDir))
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/CAAReport?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm="
                              + bnm + "\">Generate</a> Creditors Ageing</td><td width=70%><span id=\"service\">"
                              + "&nbsp;&nbsp;&nbsp;<a href=\"javascript:help('1013')\">(Service 1013)</a></span></td></tr>");
      }

      if(authenticationUtils.verifyAccess(con, stmt, rs, req, 1031, unm, uty, dnm, localDefnsDir, defnsDir))
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/ConsolidatedCreditorsInput?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm="
                              + bnm + "\">Generate</a> Consolidated Creditors</td><td width=70%><span id=\"service\">"
                              + "&nbsp;&nbsp;&nbsp;<a href=\"javascript:help('1031')\">(Service 1031)</a></span></td></tr>");
      }
    }
    
    boolean PurchaseInvoiceServices  = authenticationUtils.verifyAccess(con, stmt, rs, req, 154, unm, uty, dnm, localDefnsDir, defnsDir);       
    boolean PurchaseCreditNoteServices  = authenticationUtils.verifyAccess(con, stmt, rs, req, 162, unm, uty, dnm, localDefnsDir, defnsDir);        
    boolean PurchaseDebitNoteServices  = authenticationUtils.verifyAccess(con, stmt, rs, req, 163, unm, uty, dnm, localDefnsDir, defnsDir);        
    boolean PaymentServices  = authenticationUtils.verifyAccess(con, stmt, rs, req, 164, unm, uty, dnm, localDefnsDir, defnsDir);        

    if(PurchaseInvoiceServices || PurchaseCreditNoteServices || PurchaseDebitNoteServices || PaymentServices)
    {

      scoutln(out, bytesOut, "<tr><td nowrap colspan=3><h1>Creditor Settlement Document Services</td></tr>");

      if(PurchaseInvoiceServices)
      {  
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/PurchaseInvoiceServices?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                             + "\">Purchase Invoice Focus</a></td><td width=70%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                             + "<a href=\"javascript:help('154')\">(Service 154)</a></span></td></tr>");
      }  

      if(PurchaseCreditNoteServices)
      {  
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/PurchaseCreditNoteServices?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                             + "\">Purchase Credit Note Focus</a></td><td width=70%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                             + "<a href=\"javascript:help('162')\">(Service 162)</a></span></td></tr>");
      }  

      if(PurchaseDebitNoteServices)
      {  
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/PurchaseDebitNoteServices?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                             + "\">Purchase Debit Note Focus</a></td><td width=70%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                             + "<a href=\"javascript:help('163')\">(Service 163)</a></span></td></tr>");
      }  

      if(PaymentServices)
      {  
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/PaymentServices?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                             + "\">Payment Focus</a></td><td width=70%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                             + "<a href=\"javascript:help('164')\">(Service 164)</a></span></td></tr>");
      }  
    }
    
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
