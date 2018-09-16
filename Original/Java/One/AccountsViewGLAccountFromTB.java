// =======================================================================================================================================================================================================
// System: ZaraStar Accounts: View GL account from TB
// Module: AccountsViewGLAccountFromTB.java
// Author: C.K.Harvey
// Copyright (c) 2003-08 Christopher Harvey. All Rights Reserved.
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
import java.net.*;
import java.sql.*;

public class AccountsViewGLAccountFromTB extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  AccountsUtils accountsUtils = new AccountsUtils();
  DefinitionTables definitionTables = new DefinitionTables();
  
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="", p3="";

    try
    {
      directoryUtils.setContentHeaders(res);
      out = res.getWriter();

      unm = req.getParameter("unm");
      uty = req.getParameter("uty");
      sid = req.getParameter("sid");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");
      p1  = req.getParameter("p1"); // accCode
      p2  = req.getParameter("p2"); // dateFrom
      p3  = req.getParameter("p3"); // dateTo
      
      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, p3, bytesOut);
    }
    catch(Exception e)
    {
      StringBuffer url = req.getRequestURL();
      String urlBit="";
      int x=0;
      if(url.charAt(x) == 'h' || url.charAt(x) == 'H')
        x += 7;
      while(url.charAt(x) != '\000' && url.charAt(x) != ',' && url.charAt(x) != '/')
        urlBit += url.charAt(x++);

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "AccountsViewGLAccountFromTB", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6055, bytesOut[0], 0, "ERR:" + p1 + ":" + p2 + ":" + p3);
      if(out != null) out.flush();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm,
                    String bnm, String p1, String p2, String p3, int[] bytesOut) throws Exception
  {
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');
    String workingDir       = directoryUtils.getUserDir('W', dnm, unm);

    long startTime = new java.util.Date().getTime();

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 6055, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "AccountsViewGLAccountFromTB", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6055, bytesOut[0], 0, "ACC:" + p1 + ":" + p2 + ":" + p3);
    if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "AccountsViewGLAccountFromTB", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6055, bytesOut[0], 0, "SID:" + p1 + ":" + p2 + ":" + p3);
    if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }
  
    String yyyy = generalUtils.getFromDefnFile(unm, "accounts.dfn", workingDir, "");

    String dateFrom = generalUtils.convertFromYYYYMMDD(p2);
    String dateTo   = generalUtils.convertFromYYYYMMDD(p3);
            
    create(out, req, yyyy, p1, dateFrom, dateTo, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 6055, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1 + ":" + p2 + ":" + p3);
    if(con  != null) con.close();
    if(out != null) out.flush();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void create(PrintWriter out, HttpServletRequest req, String year, String accCode, String dateFrom, String dateTo, String unm, String sid,
                      String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut)
                      throws Exception
  {
     // Given account code, determine category, and call appropriate code
    
     String category = accountsUtils.getCategoryGivenAccCode(accCode, year, dnm, localDefnsDir, defnsDir);

     String servlet = "";
     
     if(category.equals("Income - Sales"))
       servlet = "AccountsGLSales?p5=S&p1=" + accCode + "&p2=" + dateFrom + "&p3=" + dateTo;
     else
     if(category.startsWith("Income (Operating)"))
       servlet = "AccountsGLSales?p5=C&p1=" + accCode + "&p2=" + dateFrom + "&p3=" + dateTo;
     else
     if(category.equals("Income - Discount Given"))
       servlet = "AccountsGLDiscounts?p5=G&p1=" + accCode + "&p2=" + dateFrom + "&p3=" + dateTo;
     else
     if(category.equals("Income - Sales Category"))
       servlet = "AccountsGLSales?p5=N&p1=" + accCode + "&p2=" + dateFrom + "&p3=" + dateTo;
     else
     if(category.equals("Cost of Sales - Purchases"))
       servlet = "AnalyticsPurchases?p5=P&p1=" + accCode + "&p2=" + dateFrom + "&p3=" + dateTo;
     else
     if(   category.equals("Cost of Sales - Purchases Category") || category.equals("Cost of Sales - Carriage Outward")
        || category.equals("Cost of Sales - Carriage Inward") || category.equals("Cost of Sales - Delivery and Distribution"))
     {
        servlet = "AnalyticsPurchases?p5=N&p1=" + accCode + "&p2=" + dateFrom + "&p3=" + dateTo;
     }
     else
     if(category.equals("Income - Exchange Gain"))
       servlet = "GLExchangeGainLoss?p5=G&p1=" + accCode + "&p2=" + dateFrom + "&p3=" + dateTo;
     else
     if(category.equals("Cost of Sales"))
       servlet = "AccountsGLCostOfSales?p1=" + accCode + "&p2=" + dateFrom + "&p3=" + dateTo;
     else
     if(category.equals("Cost of Sales - Discount Received"))
       servlet = "AccountsGLDiscounts?p5=R&p1=" + accCode + "&p2=" + dateFrom + "&p3=" + dateTo;
     else
     if(category.equals("Cost of Sales - Exchange Loss"))
       servlet = "GLExchangeGainLoss?p5=L&p1=" + accCode + "&p2=" + dateFrom + "&p3=" + dateTo;
     else
     if(category.equals("Liabilities - GST Input"))
       servlet = "AccountsGLGSTInput?p1=" + accCode + "&p2=" + dateFrom + "&p3=" + dateTo;
     else
     if(category.equals("Current Assets - GST Clearing"))
       servlet = "AccountsGLGSTClearing?p1=" + accCode + "&p2=" + dateFrom + "&p3=" + dateTo;
     else
     if(category.equals("Liabilities - GST Output"))
       servlet = "AccountsGLGSTOutput?p1=" + accCode + "&p2=" + dateFrom + "&p3=" + dateTo;
     else
     if(category.equals("Current Assets - Trade Debtors"))
       servlet = "AccountsGLTradeDebtors?p2=" + dateFrom + "&p3=" + dateTo;
     else
     if(category.equals("Current Assets - Other Debtors"))
       servlet = "AccountsGLOtherDebtors?p1=" + accCode + "&p2=" + dateFrom + "&p3=" + dateTo;
     else
     if(category.equals("Liabilities - Trade Creditors"))
       servlet = "AccountsGLTradeCreditors?p2=" + dateFrom + "&p3=" + dateTo;
     else
     if(category.equals("Liabilities - Other Creditors"))
       servlet = "AccountsGLOtherCreditors?p1=" + accCode + "&p2=" + dateFrom + "&p3=" + dateTo;
     else
     if(category.equals("Income - Sales Returned"))
       servlet = "AccountsGLSalesReturned?p1=" + accCode + "&p2=" + dateFrom + "&p3=" + dateTo;
     else
     if(category.equals("Cost of Sales - Purchases Returned"))
       servlet = "AccountsGLPurchasesReturned?p1=" + accCode + "&p2=" + dateFrom + "&p3=" + dateTo;
     else
     if(category.equals("Current Assets - Cash"))
       servlet = "AccountsGLBankTransactions?p1=" + accCode + "&p2=" + dateFrom + "&p3=" + dateTo;
     else
     if(category.equals("Current Assets - Bank"))
       servlet = "AccountsGLBankTransactions?p1=" + accCode + "&p2=" + dateFrom + "&p3=" + dateTo;
     else
     if(category.equals("Current Assets - Recoverable Expenses"))
       servlet = "AccountsGLRecoverableExpenses?p1=" + accCode + "&p2=" + dateFrom + "&p3=" + dateTo;
     else
     if(category.equals("Current Assets - Contra"))
       servlet = "AccountsGLCurrentAssetsContra?p1=" + accCode + "&p2=" + dateFrom + "&p3=" + dateTo;
     else
     if(category.equals("Current Assets - PrePayments"))
       servlet = "AccountsGLCurrentAssetsPrePayments?p1=" + accCode + "&p2=" + dateFrom + "&p3=" + dateTo;
     else
     if(category.equals("Liabilities - PrePayments"))
       servlet = "AccountsGLLiabilitiesPrePayments?p1=" + accCode + "&p2=" + dateFrom + "&p3=" + dateTo;
     else
 
     if(  category.equals("Liabilities") || category.equals("Liabilities - Suspense") || category.equals("Liabilities - Provision for Income Tax")
           || category.equals("Liabilities - Proposed Dividend"))
     {
       servlet = "AccountsGLLiabilities?p1=" + accCode + "&p2=" + dateFrom + "&p3=" + dateTo;
     }
     else
     if(category.equals("Income"))
       servlet = "AccountsGLIncome?p1=" + accCode + "&p2=" + dateFrom + "&p3=" + dateTo;
     else
     if(category.equals("Liabilities - Accruals"))
       servlet = "AccountsGLLiabilitiesAccruals?p1=" + accCode + "&p2=" + dateFrom + "&p3=" + dateTo;
     else
     if(category.equals("Equity - Current Earnings") || category.equals("Equity - Retained Earnings") || category.equals("Fixed Assets - Accumulated Depreciation"))
       servlet = "AccountsGLEquityEarnings?p1=" + accCode + "&p2=" + dateFrom + "&p3=" + dateTo;
     else
     if(category.equals("Current Assets - Currency Exchange"))
       servlet = "AccountsGLEquityCurrencyExchange?p1=" + accCode + "&p2=" + dateFrom + "&p3=" + dateTo;
     else
     if(category.equals("Current Assets - Stock"))
          servlet = "AnalyticsStock?p1=" + accCode + "&p2=" + dateFrom + "&p3=" + dateTo;
     else
     if(category.equals("Expenses - Bank Charges"))
       servlet = "AccountsGLBankCharges?p1=" + accCode + "&p2=" + dateFrom + "&p3=" + dateTo;
     else
     if(category.startsWith("Expenses - "))
       servlet = "AccountsGLExpenses?p1=" + accCode + "&p2=" + dateFrom + "&p3=" + dateTo;
     else
     if(   category.equals("Fixed Assets") || category.equals("Equity") || category.equals("Fixed Assets - Investment")
        || category.equals("Current Assets") || category.equals("Current Assets - OverPayments") || category.startsWith("PL Provision"))
     {
       servlet = "AccountsGLFixedAssetsEquity?p1=" + accCode + "&p2=" + dateFrom + "&p3=" + dateTo;
     }

     getGLAccount(out, servlet, unm, sid, uty, men, den, dnm, bnm, localDefnsDir);
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void getGLAccount(PrintWriter out, String servlet, String unm, String sid, String uty, String men, String den, String dnm,
                            String bnm, String localDefnsDir) throws Exception
  {
    URL url = new URL("http://" + serverUtils.serverToCall("OFSA", localDefnsDir) + "/central/servlet/" + servlet + "&unm=" + unm + "&sid=" + sid
                   + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm);

    HttpURLConnection uc = (HttpURLConnection)url.openConnection();
    uc.setDoInput(true);
    uc.setDoOutput(true);
    uc.setUseCaches(false);
    uc.setDefaultUseCaches(false);
    uc.setRequestProperty("Content-Type", "application/x-www-form.urlencoded");

    uc.setRequestMethod("GET");

    BufferedReader di = new BufferedReader(new InputStreamReader(uc.getInputStream()));
    String s = di.readLine();
    while(s != null)
    {
      out.println(s);
      s = di.readLine();
    }

    di.close();
  }
 
}
