// =======================================================================================================================================================================================================
// System: ZaraStar UtilsEngine: Debtor Settlement services access page
// Module: DebtorSettlementServices.java
// Author: C.K.Harvey
// Copyright (c) 2002-12 Christopher Harvey. All Rights Reserved.
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

public class DebtorSettlementServices extends HttpServlet
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

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", iw = "";

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
 
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "DebtorSettlementServices", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 106, bytesOut[0], 0, "ERR:");

      if(iw.length() > 0) pageFrameUtils.postToIW(iw, "ERROR: ERR");

      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String iw, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    
    Statement stmt = null;
    ResultSet rs   = null;
    
    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 106, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "DebtorSettlementServices", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 106, bytesOut[0], 0, "ACC:");
      
      if(con != null) con.close();
      if(out != null) out.flush(); 

      if(iw.length() > 0) pageFrameUtils.postToIW(iw, "ERROR: ACC");

      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "DebtorSettlementServices", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 106, bytesOut[0], 0, "SID:");
      
      if(con != null) con.close();
      if(out != null) out.flush(); 

      if(iw.length() > 0) pageFrameUtils.postToIW(iw, "ERROR: SID");

      return;
    }

    String[] scripts = new String[1];  scripts[0] = "";

    String s = set(con, stmt, rs, out, req, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut, scripts);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 106, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
      
    if(con != null) con.close();

    if(iw.length() > 0) pageFrameUtils.postToIW(iw, s);

    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private String set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut,
                     String[] scripts) throws Exception
  {
    String s = "";

    scoutln(out, bytesOut, "<html><head><title>Debtor Settlement Services Access</title>");

    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    scoutln(out, bytesOut, "</head>");
  
    int[] hmenuCount = new int[1];
    
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "106", "", "DebtorSettlementServices", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
                                
    s += scoutln(out, bytesOut, "<form>");
  
    s += pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Debtor Settlement Services", "106",  unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    s += scoutln(out, bytesOut, "<table id=\"page\" width=100% border=0>");

    boolean StatementsOutstanding = authenticationUtils.verifyAccess(con, stmt, rs, req, 1011, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean Statements = authenticationUtils.verifyAccess(con, stmt, rs, req, 1012, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean ConsolidatedDebtorsInput = authenticationUtils.verifyAccess(con, stmt, rs, req, 1029, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean CustomerSettlementHistoryInput = authenticationUtils.verifyAccess(con, stmt, rs, req, 4002, unm, uty, dnm, localDefnsDir, defnsDir);
        
    if(StatementsOutstanding || Statements || CustomerSettlementHistoryInput || ConsolidatedDebtorsInput)
    {
      s += scoutln(out, bytesOut, "<tr><td nowrap colspan=3><h1>Debtor Settlement Services</td></tr>");

      if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4002, unm, uty, dnm, localDefnsDir, defnsDir))
      {  
        s += scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/CustomerSettlementHistoryInput?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm
                              + "&p1=&p2=N&bnm=" + bnm + "\">View</a> Settlement History</td><td width=70% nowrap>" + directoryUtils.buildHelp(4002) + "</td></tr>");

        s += scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/CustomerSettlementHistoryInput?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=&p2=C&bnm="
                              + bnm + "\">View</a> Settlement History for CASH Transactions</td><td width=70% nowrap>" + directoryUtils.buildHelp(4002) + "</td></tr>");
      }

      if(authenticationUtils.verifyAccess(con, stmt, rs, req, 1012, unm, uty, dnm, localDefnsDir, defnsDir))
      {
        s += scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/Statements?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm="
                              + bnm + "\">Generate</a> Statements of Account</td><td width=70% nowrap>" + directoryUtils.buildHelp(1012) + "</td></tr>");
      }

      if(authenticationUtils.verifyAccess(con, stmt, rs, req, 1012, unm, uty, dnm, localDefnsDir, defnsDir))
      {
        s += scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/StatementsOutstanding?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm="
                              + bnm + "\">Generate</a> Statements of Outstanding</td><td width=70% nowrap>" + directoryUtils.buildHelp(1011) + "</td></tr>");
      }

      if(authenticationUtils.verifyAccess(con, stmt, rs, req, 1004, unm, uty, dnm, localDefnsDir, defnsDir))
      {
        s += scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/MainPageUtils4?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm="
                              + bnm + "\">Generate</a> Debtors Ageing</td><td width=70% nowrap>" + directoryUtils.buildHelp(1004) + "</td></tr>");
      }

      if(authenticationUtils.verifyAccess(con, stmt, rs, req, 1029, unm, uty, dnm, localDefnsDir, defnsDir))
      {
        s += scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/ConsolidatedDebtorsInput?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm="
                              + bnm + "\">Generate</a> Consolidated Debtors</td><td width=70% nowrap>" + directoryUtils.buildHelp(1029) + "</td></tr>");
      }
      
      if(authenticationUtils.verifyAccess(con, stmt, rs, req, 6900, unm, uty, dnm, localDefnsDir, defnsDir))
      {
        s += scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/ConsolidationInfo?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm="
                              + bnm + "\">View</a> Document Consolidations</td><td width=70% nowrap>" + directoryUtils.buildHelp(6900) + "</td></tr>");
      }        
    }
    
    boolean SalesInvoiceServices  = authenticationUtils.verifyAccess(con, stmt, rs, req, 149, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean SalesCreditNoteServices  = authenticationUtils.verifyAccess(con, stmt, rs, req, 155, unm, uty, dnm, localDefnsDir, defnsDir);        
    boolean SalesDebitNoteServices  = authenticationUtils.verifyAccess(con, stmt, rs, req, 156, unm, uty, dnm, localDefnsDir, defnsDir);        
    boolean ReceiptServices  = authenticationUtils.verifyAccess(con, stmt, rs, req, 157, unm, uty, dnm, localDefnsDir, defnsDir);        
    boolean ProformaInvoiceServices  = authenticationUtils.verifyAccess(con, stmt, rs, req, 150, unm, uty, dnm, localDefnsDir, defnsDir);        
        
    if(SalesInvoiceServices || SalesCreditNoteServices || SalesDebitNoteServices || ReceiptServices || ProformaInvoiceServices)
    {
      s += scoutln(out, bytesOut, "<tr><td nowrap colspan=3><h1>Debtor Settlement Document Services</td></tr>");

      if(SalesInvoiceServices)
      {  
        s += scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/SalesInvoiceServices?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                             + "\">Invoice Focus</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(149) + "</td></tr>");
      }  

      if(SalesCreditNoteServices)
      {  
        s += scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/SalesCreditNoteServices?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                             + "\">Credit Note Focus</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(155) + "</td></tr>");
      }  

      if(SalesDebitNoteServices)
      {  
        s += scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/SalesDebitNoteServices?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                             + "\">Debit Note Focus</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(156) + "</td></tr>");
      }  

      if(ReceiptServices)
      {  
        s += scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/ReceiptServices?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                             + "\">Receipt Focus</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(157) + "</td></tr>");
      }  

      if(ProformaInvoiceServices)
      {  
        s += scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/ProformaInvoiceServices?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                             + "\">Proforma Invoice Focus</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(150) + "</td></tr>");
      }  
    }
    
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
