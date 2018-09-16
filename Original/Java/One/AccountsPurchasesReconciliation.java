// =============================================================================================================================================================
// System: ZaraStar: Accounts: Purchases Reconciliation Analysis
// Module: AccountsPurchasesReconciliation.java
// Author: C.K.Harvey
// Copyright (c) 2002-08 Christopher Harvey. All Rights Reserved.
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
import java.net.*;

public class AccountsPurchasesReconciliation extends HttpServlet
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
      directoryUtils.setContentHeaders2(res);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "AccountsPurchasesReconciliation", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6033, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                    String year, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');
    String workingDir       = directoryUtils.getUserDir('W', dnm, unm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
     
   Statement stmt = null;
    ResultSet rs   = null;
    
    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 6033, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "6033", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6033, bytesOut[0], 0, "ACC:" + year);
      
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "6033", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6033, bytesOut[0], 0, "SID:" + year);
      
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    // updateCurrYearOnFile
    generalUtils.repInDefnFile(unm, year, "accounts.dfn", workingDir, "");

    set(con, stmt, rs, out, req, year, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 6033, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), year);
      
      if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String year, String unm, String sid,
                   String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut)
                   throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Purchases Reconciliation Analysis</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");
  
    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\""
                           + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\"></head>");
  
    int[] hmenuCount = new int[1];
 
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "6033", "", "AccountsPurchasesReconciliation", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount,
                          bytesOut);
                                
    scoutln(out, bytesOut, "<form>");
    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Purchases Reconciliation Analysis: " + year, "6033", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<table id='page' width=100% border=0>");
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr id='pageColumn'><td colspan=2></td>");
    scoutln(out, bytesOut, "<td align=right><p>Total</td>");
    scoutln(out, bytesOut, "<td align=right><p>Debits</td>");
    scoutln(out, bytesOut, "<td align=right><p>Credits</td></tr>");    
    
    double[] obPurchasesDRs            = new double[1];  obPurchasesDRs[0]            = 0.0;
    double[] obPurchasesCRs            = new double[1];  obPurchasesCRs[0]            = 0.0; 
    double[] obPurchasesBalance        = new double[1];  obPurchasesBalance[0]        = 0.0;
    
    double[] allPurchasesDRs           = new double[1];  allPurchasesDRs[0]           = 0.0;
    double[] allPurchasesCRs           = new double[1];  allPurchasesCRs[0]           = 0.0;
    double[] allPurchasesBalance       = new double[1];  allPurchasesBalance[0]       = 0.0;
    
    double[] purchasesIntoStockDRs     = new double[1];  purchasesIntoStockDRs[0]     = 0.0;
    double[] purchasesIntoStockCRs     = new double[1];  purchasesIntoStockCRs[0]     = 0.0;
    double[] purchasesIntoStockBalance = new double[1];  purchasesIntoStockBalance[0] = 0.0;
    
    double[] nonStockPurchasesDRs      = new double[1];  nonStockPurchasesDRs[0]      = 0.0;
    double[] nonStockPurchasesCRs      = new double[1];  nonStockPurchasesCRs[0]      = 0.0;
    double[] nonStockPurchasesBalance  = new double[1];  nonStockPurchasesBalance[0]  = 0.0;
    
    double[] salesFromStockDRs         = new double[1];  salesFromStockDRs[0]         = 0.0;
    double[] salesFromStockCRs         = new double[1];  salesFromStockCRs[0]         = 0.0;
    double[] salesFromStockBalance     = new double[1];  salesFromStockBalance[0]     = 0.0;
    
    double[] miscPurchasesDRs          = new double[1];  miscPurchasesDRs[0]          = 0.0;
    double[] miscPurchasesCRs          = new double[1];  miscPurchasesCRs[0]          = 0.0;
    double[] miscPurchasesBalance      = new double[1];  miscPurchasesBalance[0]      = 0.0;
    
    double[] miscStockDRs              = new double[1];  miscStockDRs[0]              = 0.0;
    double[] miscStockCRs              = new double[1];  miscStockCRs[0]              = 0.0;
    double[] miscStockBalance          = new double[1];  miscStockBalance[0]          = 0.0;
    
    double[] obStockDRs                = new double[1];  obStockDRs[0]                = 0.0;
    double[] obStockCRs                = new double[1];  obStockCRs[0]                = 0.0; 
    double[] obStockBalance            = new double[1];  obStockBalance[0]            = 0.0;
    
    double[] allStockDRs               = new double[1];  allStockDRs[0]               = 0.0;
    double[] allStockCRs               = new double[1];  allStockCRs[0]               = 0.0; 
    double[] allStockBalance           = new double[1];  allStockBalance[0]           = 0.0;
    
    double[] closingStockDRs           = new double[1];  closingStockDRs[0]           = 0.0;
    double[] closingStockCRs           = new double[1];  closingStockCRs[0]           = 0.0; 
    double[] closingStockBalance       = new double[1];  closingStockBalance[0]       = 0.0;
    
    double[] closingPurchasesDRs       = new double[1];  closingPurchasesDRs[0]       = 0.0;
    double[] closingPurchasesCRs       = new double[1];  closingPurchasesCRs[0]       = 0.0; 
    double[] closingPurchasesBalance   = new double[1];  closingPurchasesBalance[0]   = 0.0;
    
    String cssFormat = "";

    getData(localDefnsDir, unm, uty, sid, men, den, dnm, bnm, obPurchasesDRs, obPurchasesCRs, obPurchasesBalance, allPurchasesDRs, allPurchasesCRs,
            allPurchasesBalance, purchasesIntoStockDRs, purchasesIntoStockCRs, purchasesIntoStockBalance, nonStockPurchasesDRs, nonStockPurchasesCRs,
            nonStockPurchasesBalance, salesFromStockDRs, salesFromStockCRs, salesFromStockBalance, miscPurchasesDRs, miscPurchasesCRs, miscPurchasesBalance,
            obStockDRs, obStockCRs, obStockBalance, miscStockDRs, miscStockCRs, miscStockBalance, allStockDRs, allStockCRs, allStockBalance, closingStockDRs,
            closingStockCRs, closingStockBalance, closingPurchasesDRs, closingPurchasesCRs, closingPurchasesBalance);

    if(cssFormat.equals("line1")) cssFormat = "line2"; else cssFormat = "line1";

    scoutln(out, bytesOut, "<tr id='" + cssFormat + "'><td nowrap><p>Opening Balances Purchases</td>");
    scoutln(out, bytesOut, "<td><p>Purchases account opening balance entry</td>");

    scoutln(out, bytesOut, "<td nowrap align=right><p><a href=\"/central/servlet/PurchasesReconciliationAnalysis?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den="
                         + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=B&p2=N&p3=Y\">" + generalUtils.formatNumeric(obPurchasesBalance[0], '2') + "</a></td>");
    scoutln(out, bytesOut, "<td nowrap align=right><p>" + generalUtils.formatNumeric(obPurchasesDRs[0], '2') + "</td>");
    scoutln(out, bytesOut, "<td nowrap align=right><p>" + generalUtils.formatNumeric(obPurchasesCRs[0], '2') + "</td></tr>");

    if(cssFormat.equals("line1")) cssFormat = "line2"; else cssFormat = "line1";

    scoutln(out, bytesOut, "<tr id='" + cssFormat + "'><td nowrap><p>All Purchases</td>");
    scoutln(out, bytesOut, "<td><p>Purchase Invoices (- Purchase Credit Notes) for stock and non-stock line items</td>");

    scoutln(out, bytesOut, "<td nowrap align=right><p><a href=\"/central/servlet/PurchasesReconciliationAnalysis?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den="
                         + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=A&p2=N&p3=N\">" + generalUtils.formatNumeric(allPurchasesBalance[0], '2') + "</a></td>");
    scoutln(out, bytesOut, "<td nowrap align=right><p>" + generalUtils.formatNumeric(allPurchasesDRs[0], '2') + "</td>");
    scoutln(out, bytesOut, "<td nowrap align=right><p>" + generalUtils.formatNumeric(allPurchasesCRs[0], '2') + "</td></tr>");

    if(cssFormat.equals("line1")) cssFormat = "line2"; else cssFormat = "line1";

    scoutln(out, bytesOut, "<tr id='" + cssFormat + "'><td nowrap><p>Purchases into Stock</td>");
    scoutln(out, bytesOut, "<td><p>Purchase Invoices (- Purchase Credit Notes) for stock line items</td>");

    scoutln(out, bytesOut, "<td nowrap align=right><p><a href=\"/central/servlet/PurchasesReconciliationAnalysis?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den="
                         + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=I&p2=N&p3=N\">" + generalUtils.formatNumeric(purchasesIntoStockBalance[0], '2') + "</a></td>");
    scoutln(out, bytesOut, "<td nowrap align=right><p>" + generalUtils.formatNumeric(purchasesIntoStockDRs[0], '2') + "</td>");
    scoutln(out, bytesOut, "<td nowrap align=right><p>" + generalUtils.formatNumeric(purchasesIntoStockCRs[0], '2') + "</td></tr>");

    if(cssFormat.equals("line1")) cssFormat = "line2"; else cssFormat = "line1";

    scoutln(out, bytesOut, "<tr id='" + cssFormat + "'><td nowrap><p>Non-Stock Purchases</td>");
    scoutln(out, bytesOut, "<td><p>Purchase Invoices (- Purchase Credit Notes) for non-stock line items</td>");

    scoutln(out, bytesOut, "<td nowrap align=right><p><a href=\"/central/servlet/PurchasesReconciliationAnalysis?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den="
                         + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=N&p2=N&p3=N\">" + generalUtils.formatNumeric(nonStockPurchasesBalance[0], '2') + "</a></td>");
    scoutln(out, bytesOut, "<td nowrap align=right><p>" + generalUtils.formatNumeric(nonStockPurchasesDRs[0], '2') + "</td>");
    scoutln(out, bytesOut, "<td nowrap align=right><p>" + generalUtils.formatNumeric(nonStockPurchasesCRs[0], '2') + "</td></tr>");

    if(cssFormat.equals("line1")) cssFormat = "line2"; else cssFormat = "line1";

    scoutln(out, bytesOut, "<tr id='" + cssFormat + "'><td nowrap><p>Miscellaneous Purchases Entries</td>");
    scoutln(out, bytesOut, "<td><p>Entries which affect the purchases account (other than Invoices and Credit Notes)</td>");

    scoutln(out, bytesOut, "<td nowrap align=right><p><a href=\"/central/servlet/PurchasesReconciliationAnalysis?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den="
                         + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=M&p2=N&p3=N\">" + generalUtils.formatNumeric(miscPurchasesBalance[0], '2') + "</a></td>");
    scoutln(out, bytesOut, "<td nowrap align=right><p>" + generalUtils.formatNumeric(miscPurchasesDRs[0], '2') + "</td>");
    scoutln(out, bytesOut, "<td nowrap align=right><p>" + generalUtils.formatNumeric(miscPurchasesCRs[0], '2') + "</td></tr>");

    if(cssFormat.equals("line1")) cssFormat = "line2"; else cssFormat = "line1";

    scoutln(out, bytesOut, "<tr id='" + cssFormat + "'><td nowrap><p>Sales from Stock</td>");
    scoutln(out, bytesOut, "<td><p>Sales Invoice (- Sales Credit Notes) for stock line items</td>");

    scoutln(out, bytesOut, "<td nowrap align=right><p><a href=\"/central/servlet/PurchasesReconciliationAnalysis?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den="
                         + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=S&p2=N&p3=N\">" + generalUtils.formatNumeric(salesFromStockBalance[0], '2') + "</a></td>");
    scoutln(out, bytesOut, "<td nowrap align=right><p>" + generalUtils.formatNumeric(salesFromStockDRs[0], '2') + "</td>");
    scoutln(out, bytesOut, "<td nowrap align=right><p>" + generalUtils.formatNumeric(salesFromStockCRs[0], '2') + "</td></tr>");

    if(cssFormat.equals("line1")) cssFormat = "line2"; else cssFormat = "line1";

    scoutln(out, bytesOut, "<tr id='" + cssFormat + "'><td nowrap><p>Closing Purchases</td>");
    scoutln(out, bytesOut, "<td><p>Purchases account closing balance</td>");

    scoutln(out, bytesOut, "<td nowrap align=right><p><a href=\"/central/servlet/PurchasesReconciliationAnalysis?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den="
                         + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=E&p2=N&p3=N\">" + generalUtils.formatNumeric(closingPurchasesBalance[0], '2')
                         + "</a></td>");
    scoutln(out, bytesOut, "<td nowrap align=right><p>" + generalUtils.formatNumeric(closingPurchasesDRs[0], '2') + "</td>");
    scoutln(out, bytesOut, "<td nowrap align=right><p>" + generalUtils.formatNumeric(closingPurchasesCRs[0], '2') + "</td></tr>");

    if(cssFormat.equals("line1")) cssFormat = "line2"; else cssFormat = "line1";

    scoutln(out, bytesOut, "<tr id='" + cssFormat + "'><td nowrap><p>Opening Balances Stock</td>");
    scoutln(out, bytesOut, "<td><p>Stock account opening balance entry</td>");

    scoutln(out, bytesOut, "<td nowrap align=right><p><a href=\"/central/servlet/PurchasesReconciliationAnalysis?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den="
                         + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=T&p2=N&p3=Y\">" + generalUtils.formatNumeric(obStockBalance[0], '2') + "</a></td>");
    scoutln(out, bytesOut, "<td nowrap align=right><p>" + generalUtils.formatNumeric(obStockDRs[0], '2') + "</td>");
    scoutln(out, bytesOut, "<td nowrap align=right><p>" + generalUtils.formatNumeric(obStockCRs[0], '2') + "</td></tr>");

    if(cssFormat.equals("line1")) cssFormat = "line2"; else cssFormat = "line1";

    scoutln(out, bytesOut, "<tr id='" + cssFormat + "'><td nowrap><p>Miscellaneous Stock Entries</td>");
    scoutln(out, bytesOut, "<td><p>Entries which affect the stock account (other than Invoices and Credit Notes)</td>");

    scoutln(out, bytesOut, "<td nowrap align=right><p><a href=\"/central/servlet/PurchasesReconciliationAnalysis?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den="
                         + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=U&p2=N&p3=N\">" + generalUtils.formatNumeric(miscStockBalance[0], '2') + "</a></td>");
    scoutln(out, bytesOut, "<td nowrap align=right><p>" + generalUtils.formatNumeric(miscStockDRs[0], '2') + "</td>");
    scoutln(out, bytesOut, "<td nowrap align=right><p>" + generalUtils.formatNumeric(miscStockCRs[0], '2') + "</td></tr>");

    if(cssFormat.equals("line1")) cssFormat = "line2"; else cssFormat = "line1";

    scoutln(out, bytesOut, "<tr id='" + cssFormat + "'><td nowrap><p>Closing Stock</td>");
    scoutln(out, bytesOut, "<td><p>Stock account closing balance</td>");

    scoutln(out, bytesOut, "<td nowrap align=right><p><a href=\"/central/servlet/PurchasesReconciliationAnalysis?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den="
                         + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=F&p2=N&p3=N\">" + generalUtils.formatNumeric(closingStockBalance[0], '2') + "</a></td>");
    scoutln(out, bytesOut, "<td nowrap align=right><p>" + generalUtils.formatNumeric(closingStockDRs[0], '2') + "</td>");
    scoutln(out, bytesOut, "<td nowrap align=right><p>" + generalUtils.formatNumeric(closingStockCRs[0], '2') + "</td>");

    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void getData(String localDefnsDir, String unm, String uty, String sid, String men, String den, String dnm, String bnm, double[] obPurchasesDRs,
                       double[] obPurchasesCRs, double[] obPurchasesBalance, double[] allPurchasesDRs, double[] allPurchasesCRs, double[] allPurchasesBalance,
                       double[] purchasesIntoStockDRs, double[] purchasesIntoStockCRs, double[] purchasesIntoStockBalance, double[] nonStockPurchasesDRs,
                       double[] nonStockPurchasesCRs, double[] nonStockPurchasesBalance, double[] salesFromStockDRs, double[] salesFromStockCRs,
                       double[] salesFromStockBalance, double[] miscPurchasesDRs, double[] miscPurchasesCRs, double[] miscPurchasesBalance,
                       double[] obStockDRs, double[] obStockCRs, double[] obStockBalance, double[] miscStockDRs, double[] miscStockCRs,
                       double[] miscStockBalance, double[] allStockDRs, double[] allStockCRs, double[] allStockBalance, double[] closingStockDRs,
                       double[] closingStockCRs, double[] closingStockBalance, double[] closingPurchasesDRs, double[] closingPurchasesCRs,
                       double[] closingPurchasesBalance) throws Exception
  {
    URL url = new URL("http://" + serverUtils.serverToCall("OFSA", localDefnsDir) + "/central/servlet/PurchasesReconciliationTotals?unm=" + unm + "&sid=" + sid + "&uty=" + uty
                    + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm);
   
    URLConnection uc = url.openConnection();
    uc.setDoInput(true);
    uc.setUseCaches(false);
    uc.setDefaultUseCaches(false);

    uc.setRequestProperty("Content-Type", "application/octet-stream");

    BufferedReader di = new BufferedReader(new InputStreamReader(uc.getInputStream()));

    obPurchasesDRs[0] = obPurchasesCRs[0] = obPurchasesBalance[0] = allPurchasesDRs[0] = allPurchasesCRs[0] = allPurchasesBalance[0] = purchasesIntoStockDRs[0]
                      = purchasesIntoStockCRs[0] = purchasesIntoStockBalance[0] = nonStockPurchasesDRs[0] = nonStockPurchasesCRs[0]
                      = nonStockPurchasesBalance[0] = salesFromStockDRs[0] = salesFromStockCRs[0] = salesFromStockBalance[0] = miscPurchasesDRs[0]
                      = miscPurchasesCRs[0] = miscPurchasesBalance[0] = miscStockDRs[0] = miscStockCRs[0] = miscStockBalance[0] = obStockDRs[0] = obStockCRs[0]
                      = obStockBalance[0] = allStockDRs[0] = allStockCRs[0] = allStockBalance[0] =  closingStockDRs[0] = closingStockCRs[0]
                      = closingStockBalance[0] = closingPurchasesDRs[0] = closingPurchasesCRs[0] = closingPurchasesBalance[0] = 0.0;

    String s = di.readLine();
    if(s != null)
    {
      int x = 0, len = s.length();
      String t = "";
      while(x < len && s.charAt(x) != '\001') // just-in-case
        t += s.charAt(x++);
      obPurchasesDRs[0] = generalUtils.doubleFromStr(t);
      ++x;
        
      t = "";
      while(x < len && s.charAt(x) != '\001') // just-in-case
        t += s.charAt(x++);
      obPurchasesCRs[0] = generalUtils.doubleFromStr(t);
      ++x;
        
      t = "";
      while(x < len && s.charAt(x) != '\001') // just-in-case
        t += s.charAt(x++);
      obPurchasesBalance[0] = generalUtils.doubleFromStr(t);
      ++x;
        
      t = "";
      while(x < len && s.charAt(x) != '\001') // just-in-case
        t += s.charAt(x++);
      allPurchasesDRs[0] = generalUtils.doubleFromStr(t);
      ++x;
        
      t = "";
      while(x < len && s.charAt(x) != '\001') // just-in-case
        t += s.charAt(x++);
      allPurchasesCRs[0] = generalUtils.doubleFromStr(t);
      ++x;
        
      t = "";
      while(x < len && s.charAt(x) != '\001') // just-in-case
        t += s.charAt(x++);
      allPurchasesBalance[0] = generalUtils.doubleFromStr(t);
      ++x;
        
      t = "";
      while(x < len && s.charAt(x) != '\001') // just-in-case
        t += s.charAt(x++);
      purchasesIntoStockDRs[0] = generalUtils.doubleFromStr(t);
      ++x;
        
      t = "";
      while(x < len && s.charAt(x) != '\001') // just-in-case
        t += s.charAt(x++);
      purchasesIntoStockCRs[0] = generalUtils.doubleFromStr(t);
      ++x;
        
      t = "";
      while(x < len && s.charAt(x) != '\001') // just-in-case
        t += s.charAt(x++);
      purchasesIntoStockBalance[0] = generalUtils.doubleFromStr(t);
      ++x;
        
      t = "";
      while(x < len && s.charAt(x) != '\001') // just-in-case
        t += s.charAt(x++);
      nonStockPurchasesDRs[0] = generalUtils.doubleFromStr(t);
      ++x;
        
      t = "";
      while(x < len && s.charAt(x) != '\001') // just-in-case
        t += s.charAt(x++);
      nonStockPurchasesCRs[0] = generalUtils.doubleFromStr(t);
      ++x;
        
      t = "";
      while(x < len && s.charAt(x) != '\001') // just-in-case
        t += s.charAt(x++);
      nonStockPurchasesBalance[0] = generalUtils.doubleFromStr(t);
      ++x;
        
      t = "";
      while(x < len && s.charAt(x) != '\001') // just-in-case
        t += s.charAt(x++);
      salesFromStockDRs[0] = generalUtils.doubleFromStr(t);
      ++x;
        
      t = "";
      while(x < len && s.charAt(x) != '\001') // just-in-case
        t += s.charAt(x++);
      salesFromStockCRs[0] = generalUtils.doubleFromStr(t);
      ++x;
        
      t = "";
      while(x < len && s.charAt(x) != '\001') // just-in-case
        t += s.charAt(x++);
      salesFromStockBalance[0] = generalUtils.doubleFromStr(t);
      ++x;
        
      t = "";
      while(x < len && s.charAt(x) != '\001') // just-in-case
        t += s.charAt(x++);
      miscPurchasesDRs[0] = generalUtils.doubleFromStr(t);
      ++x;
        
      t = "";
      while(x < len && s.charAt(x) != '\001') // just-in-case
        t += s.charAt(x++);
      miscPurchasesCRs[0] = generalUtils.doubleFromStr(t);
      ++x;
        
      t = "";
      while(x < len && s.charAt(x) != '\001') // just-in-case
        t += s.charAt(x++);
      miscPurchasesBalance[0] = generalUtils.doubleFromStr(t);
      ++x;
        
      t = "";
      while(x < len && s.charAt(x) != '\001') // just-in-case
        t += s.charAt(x++);
      obStockDRs[0] = generalUtils.doubleFromStr(t);
      ++x;
        
      t = "";
      while(x < len && s.charAt(x) != '\001') // just-in-case
        t += s.charAt(x++);
      obStockCRs[0] = generalUtils.doubleFromStr(t);
      ++x;
        
      t = "";
      while(x < len && s.charAt(x) != '\001') // just-in-case
        t += s.charAt(x++);
      obStockBalance[0] = generalUtils.doubleFromStr(t);
      ++x;
        
      t = "";
      while(x < len && s.charAt(x) != '\001') // just-in-case
        t += s.charAt(x++);
      miscStockDRs[0] = generalUtils.doubleFromStr(t);
      ++x;
        
      t = "";
      while(x < len && s.charAt(x) != '\001') // just-in-case
        t += s.charAt(x++);
      miscStockCRs[0] = generalUtils.doubleFromStr(t);
      ++x;
        
      t = "";
      while(x < len && s.charAt(x) != '\001') // just-in-case
        t += s.charAt(x++);
      miscStockBalance[0] = generalUtils.doubleFromStr(t);
      ++x;
        
      t = "";
      while(x < len && s.charAt(x) != '\001') // just-in-case
        t += s.charAt(x++);
      allStockDRs[0] = generalUtils.doubleFromStr(t);
      ++x;
        
      t = "";
      while(x < len && s.charAt(x) != '\001') // just-in-case
        t += s.charAt(x++);
      allStockCRs[0] = generalUtils.doubleFromStr(t);
      ++x;
        
      t = "";
      while(x < len && s.charAt(x) != '\001') // just-in-case
        t += s.charAt(x++);
      allStockBalance[0] = generalUtils.doubleFromStr(t);
      ++x;
        
      t = "";
      while(x < len && s.charAt(x) != '\001') // just-in-case
        t += s.charAt(x++);
      closingStockDRs[0] = generalUtils.doubleFromStr(t);
      ++x;
        
      t = "";
      while(x < len && s.charAt(x) != '\001') // just-in-case
        t += s.charAt(x++);
      closingStockCRs[0] = generalUtils.doubleFromStr(t);
      ++x;
        
      t = "";
      while(x < len && s.charAt(x) != '\001') // just-in-case
        t += s.charAt(x++);
      closingStockBalance[0] = generalUtils.doubleFromStr(t);
      ++x;
        
      t = "";
      while(x < len && s.charAt(x) != '\001') // just-in-case
        t += s.charAt(x++);
      closingPurchasesDRs[0] = generalUtils.doubleFromStr(t);
      ++x;
        
      t = "";
      while(x < len && s.charAt(x) != '\001') // just-in-case
        t += s.charAt(x++);
      closingPurchasesCRs[0] = generalUtils.doubleFromStr(t);
      ++x;
        
      t = "";
      while(x < len && s.charAt(x) != '\001') // just-in-case
        t += s.charAt(x++);
      closingPurchasesBalance[0] = generalUtils.doubleFromStr(t);
      ++x;
        
      s = di.readLine();
    }

    di.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
