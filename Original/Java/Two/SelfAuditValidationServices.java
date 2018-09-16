// =======================================================================================================================================================================================================
// System: ZaraStar: Accounts: Self-Audit Validation
// Module: SelfAuditValidationServices.java
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

public class SelfAuditValidationServices extends HttpServlet
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

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="";
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
      p1  = req.getParameter("p1");
      
      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "SelfAuditValidationServices", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 108, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush(); 
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String year, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;
    
    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 108, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "108", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 108, bytesOut[0], 0, "ACC:" + year);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "108", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 108, bytesOut[0], 0, "SID:" + year);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    set(con, stmt, rs, out, req, year, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 108, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), year);
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String year, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir,
                   int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Self-Audit Validation</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");
  
    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\"></head>");
  
    int[] hmenuCount = new int[1];
    
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "108", "", "SelfAuditValidationServices", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
                                
    scoutln(out, bytesOut, "<form>");
  
    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Self-Audit Validation: " + year, "108", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<table id=\"page\" width=100% border=0>");

    boolean SalesClosureAnalysisInput = authenticationUtils.verifyAccess(con, stmt, rs, req, 1202, true, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean SalesAnalytics3 = authenticationUtils.verifyAccess(con, stmt, rs, req, 1203, true, unm, uty, dnm, localDefnsDir, defnsDir);
        
    if(SalesClosureAnalysisInput || SalesAnalytics3)
    {
      scoutln(out, bytesOut, "<tr><td nowrap colspan=3><h1>Sales & Purchases Services</td></tr>");

      if(SalesClosureAnalysisInput)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/SalesClosureAnalysisInput?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                              + "\">Sales: Closure Analysis</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(1202) + "</td></tr>");
      }

      if(SalesAnalytics3)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/SalesAnalytics3?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                              + "\">Purchases: Closure Analysis</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(1203) + "</td></tr>");
      }
    }    
    
    boolean StockStatusPage = authenticationUtils.verifyAccess(con, stmt, rs, req, 3062, true, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean StockCheckReconciliation = authenticationUtils.verifyAccess(con, stmt, rs, req, 3065, true, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean StockCheckValuation = authenticationUtils.verifyAccess(con, stmt, rs, req, 3066, true, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean InvoiceCostOfSaleVerification = authenticationUtils.verifyAccess(con, stmt, rs, req, 3070, true, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean AccountsPurchasesReconciliation = authenticationUtils.verifyAccess(con, stmt, rs, req, 6033, true, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean AnalyticsWIPAnalysisInput = authenticationUtils.verifyAccess(con, stmt, rs, req, 6036, true, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean AnalyticsSITAnalysisInput = authenticationUtils.verifyAccess(con, stmt, rs, req, 6037, true, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean StockAverageCostWeighting = authenticationUtils.verifyAccess(con, stmt, rs, req, 3074, true, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean AccountsListWACMeta = authenticationUtils.verifyAccess(con, stmt, rs, req, 7002, true, unm, uty, dnm, localDefnsDir, defnsDir);
        
    if(StockStatusPage || StockCheckValuation || InvoiceCostOfSaleVerification || AccountsPurchasesReconciliation || AnalyticsWIPAnalysisInput || AnalyticsSITAnalysisInput || StockAverageCostWeighting || AccountsListWACMeta)
    {
      scoutln(out, bytesOut, "<tr><td nowrap colspan=3><h1>Stock Services</td></tr>");

      if(AccountsPurchasesReconciliation)
      {  
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/AccountsPurchasesReconciliation?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=" + year + "&bnm=" + bnm
                              + "\">Purchases Reconciliation Analysis</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(6033) + "</td></tr>");
      }

      if(InvoiceCostOfSaleVerification)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/InvoiceCostOfSaleVerification?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=" + year + "&bnm=" + bnm
                              + "\">Invoice Cost-of-Sale Verification</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(3070) + "</td></tr>");
      }

      if(AnalyticsWIPAnalysisInput)
      {  
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/AnalyticsWIPAnalysisInput?unm=" + unm + "&sid=" + sid + "&uty="
                              + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=" + year + "&bnm=" + bnm + "\">Work-in-Progress Analysis</a></td>"
                              + "<td width=70% nowrap>" + directoryUtils.buildHelp(6036) + "</td></tr>");
      }

      if(AnalyticsSITAnalysisInput)
      {  
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/AnalyticsSITAnalysisInput?unm=" + unm + "&sid=" + sid + "&uty="
                              + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=" + year + "&bnm=" + bnm + "\">Stock-in-Transit Analysis</a></td>"
                              + "<td width=70% nowrap>" + directoryUtils.buildHelp(6037) + "</span></td></tr>");
      }

      if(StockStatusPage)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/StockStatusPageExecute?unm=" + unm + "&sid=" + sid + "&uty=" + uty
                               + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Stock Status Analysis</a></td><td width=70% nowrap>"
                               + directoryUtils.buildHelp(3062) + "</td></tr>");
      }

      if(StockCheckReconciliation)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/StockCheckReconciliation?unm=" + unm + "&sid=" + sid + "&uty=" + uty
                               + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Stock Check Reconciliation</a></td><td width=70% nowrap>"
                               + directoryUtils.buildHelp(3065) + "</td></tr>");
      }

      if(StockCheckValuation)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/StockCheckValuation?unm=" + unm + "&sid=" + sid + "&uty=" + uty
                               + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Stock Check Valuation</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(3066) + "</td></tr>");

        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/StockCheckCheck?unm=" + unm + "&sid=" + sid + "&uty=" + uty
                               + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Stock Check Record Check</a></td></tr>");
      }

      if(StockAverageCostWeighting)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/StockAverageCostWeighting?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                               + "\">Stock Average-Cost Weighting: Physical vs Financial Check</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(3074) + "</td></tr>");
      }

      if(AccountsListWACMeta)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/AccountsListWACMeta?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=" + year + "&bnm=" + bnm
                               + "\">View Stock Weighted Average Costs</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(7002) + "</td></tr>");
      }
    }

    boolean AuditDebtorOBVerification = authenticationUtils.verifyAccess(con, stmt, rs, req, 6034, true, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean AuditCreditorOBVerification = authenticationUtils.verifyAccess(con, stmt, rs, req, 6035, true, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean AccountsDebtorCreditorAnalysis = authenticationUtils.verifyAccess(con, stmt, rs, req, 6038, true, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean SalesInvoiceReceivablesVerification = authenticationUtils.verifyAccess(con, stmt, rs, req, 3072, true, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean PurchaseInvoicePayablesVerification = authenticationUtils.verifyAccess(con, stmt, rs, req, 3073, true, unm, uty, dnm, localDefnsDir, defnsDir);

    if(AuditDebtorOBVerification || AuditCreditorOBVerification || AccountsDebtorCreditorAnalysis || SalesInvoiceReceivablesVerification || PurchaseInvoicePayablesVerification)
    {
      scoutln(out, bytesOut, "<tr><td nowrap colspan=3><h1>Debtor and Creditor Services</td></tr>");

      if(AuditDebtorOBVerification)
      {  
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/AuditDebtorOBVerification?unm=" + unm + "&sid=" + sid + "&uty="
                              + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                              + "\">Debtor Opening Balance Verification</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(6034) + "</td></tr>");
      }

      if(AuditCreditorOBVerification)
      {  
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/AuditCreditorOBVerification?unm=" + unm + "&sid=" + sid + "&uty="
                              + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                              + "\">Creditor Opening Balance Verification</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(6035)
                              + "</td></tr>");
      }

      if(AccountsDebtorCreditorAnalysis)
      {  
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/AccountsDebtorCreditorAnalysis?unm=" + unm + "&sid=" + sid + "&uty="
                              + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                              + "\">Debtor and Creditor Analysis</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(6038) + "</td></tr>");
      }

      if(SalesInvoiceReceivablesVerification)
      {  
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/SalesInvoiceReceivablesVerification?unm=" + unm + "&sid=" + sid + "&uty="
                              + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                              + "\">Sales Invoice Receivables Verification</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(3072)
                              + "</td></tr>");
      }

      if(PurchaseInvoicePayablesVerification)
      {  
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/PurchaseInvoicePayablesVerification?unm=" + unm + "&sid=" + sid + "&uty="
                              + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                              + "\">Purchase Invoice Payables Verification</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(3073)
                              + "</td></tr>");
      }
    }
    
    boolean _6053 = authenticationUtils.verifyAccess(con, stmt, rs, req, 6053, true, unm, uty, dnm, localDefnsDir, defnsDir);

    if(_6053)
    {
      scoutln(out, bytesOut, "<tr><td nowrap colspan=3><h1>Statutory Reporting Services</td></tr>");

      if(_6053)
      {  
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/AnalyticsTrialBalanceInput?unm=" + unm + "&sid=" + sid + "&uty="
                             + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                             + "\">Trial Balance</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(6053) + "</td></tr>");
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
