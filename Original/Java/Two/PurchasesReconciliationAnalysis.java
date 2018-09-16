// =============================================================================================================================================================
// System: ZaraStar: Analytic: Purchases Reconciliation Analysis
// Module: PurchasesReconciliationAnalysis.java
// Author: C.K.Harvey
// Copyright (c) 2000-08 Christopher Harvey. All Rights Reserved.
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

public class PurchasesReconciliationAnalysis extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  AccountsUtils accountsUtils = new AccountsUtils();
  MiscDefinitions miscDefinitions = new MiscDefinitions();
  DefinitionTables definitionTables = new DefinitionTables();
  Inventory inventory = new Inventory();
  SalesInvoice salesInvoice = new SalesInvoice();

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
      directoryUtils.setContentHeaders2(res);
      out = res.getWriter();

      unm = req.getParameter("unm");
      sid = req.getParameter("sid");
      uty = req.getParameter("uty");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");
      p1  = req.getParameter("p1"); // search type
      p2  = req.getParameter("p2"); // plain or not
      p3  = req.getParameter("p3"); // include OBs

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, p3, bytesOut);
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

      System.out.println("6033a: " + e);
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "PurchasesReconciliationAnalysis", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6033, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1,
                    String p2, String p3, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String workingDir       = directoryUtils.getUserDir('W', dnm, unm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;
    Statement stmt2 = null;
    ResultSet rs2   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 6028, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "PurchasesReconciliationAnalysis", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6033, bytesOut[0], 0, "ACC:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "PurchasesReconciliationAnalysis", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6033, bytesOut[0], 0, "SID:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    String yyyy = generalUtils.getFromDefnFile(unm, "accounts.dfn", workingDir, "");
    int year = generalUtils.strToInt(yyyy);

    String[] financialYearStartMonth = new String[1];
    String[] financialYearEndMonth   = new String[1];

    definitionTables.getAppConfigFinancialYearMonths(con, stmt, rs, dnm, financialYearStartMonth, financialYearEndMonth);

    int startMonth = generalUtils.detMonthNumFromMonthName(financialYearStartMonth[0]);
    int endMonth   = generalUtils.detMonthNumFromMonthName(financialYearEndMonth[0]);

    String dateFrom = "01." + startMonth + "." + year;

    String dateTo;
    if(startMonth > endMonth)
      dateTo = generalUtils.numOfDaysInMonth((short)endMonth, (short)year) + "." + endMonth + "." + (year + 1);
    else dateTo = generalUtils.numOfDaysInMonth((short)endMonth, (short)year) + "." + endMonth + "." + year;

    String effectiveDate = definitionTables.getAppConfigEffectiveStartDate(con, stmt, rs, dnm);

    if(generalUtils.encode(effectiveDate, localDefnsDir, defnsDir) > generalUtils.encode(dateFrom, localDefnsDir, defnsDir))
      dateFrom = effectiveDate;
    
    dateFrom = generalUtils.convertDateToSQLFormat(dateFrom);
    dateTo   = generalUtils.convertDateToSQLFormat(dateTo);
    
    boolean plain = false;
    if(p2.equals("P"))
      plain = true;
    
    boolean obWanted;
    if(p3.equals("Y"))
      obWanted = true;
    else obWanted = false;
        
    set(con, stmt, stmt2, rs, rs2, out, req, yyyy, dateFrom, dateTo, p1, plain, obWanted, unm, sid, uty, men, den, dnm, bnm, workingDir, localDefnsDir,
        defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 6033, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, HttpServletRequest req, String year,
                   String dateFrom, String dateTo, String p1, boolean plain, boolean obWanted, String unm, String sid, String uty, String men, String den,
                   String dnm, String bnm, String workingDir, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Purchases Reconciliation Analysis</title>");

    scoutln(out, bytesOut, "<script language='JavaScript'>");

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 6066, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewVoucher(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/PaymentVoucherPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den
                           + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }
  
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 6056, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewRVoucher(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/ReceiptVoucherPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den
                           + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }
  
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4067, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewInvoice(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/SalesInvoicePage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den
                           + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }
  
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4101, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewCN(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/SalesCreditNotePage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm="
                           + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }
  
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 5026, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewPCN(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/PurchaseCreditNotePage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men
                           + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }
  
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 5080, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewPInv(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/PurchaseInvoicePage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men
                           + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }
  
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 5036, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewPDN(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/PurchaseDebitNotePage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men
                           + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }
  
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4111, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewDN(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/SalesDebitNotePage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men
                           + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }
  
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 6077, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewIAT(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/InterAccountTransferPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men
                           + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }
  
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 6023, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewJB(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/AccountsBatchEntries?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men
                           + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+p1;}");
    }
  
    scoutln(out, bytesOut, "function wac(code,date){var p1=sanitise(code);");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/AccountsWACDerivation?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men
                         + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=\"+date+\"&p1=\"+p1;}");
  
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
  
    if(plain)
    {
      scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"print\" href=\""
                             + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "generalPlain.css\">");
    }
    else
    {
      scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\""
                             + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");
    }

    int[] hmenuCount = new int[1];
 
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "6033", "", true, plain, "", dateFrom, dateTo, "", "", "PurchasesReconciliationAnalysis", unm, sid, uty, men, den, dnm, bnm, localDefnsDir,
                          defnsDir, hmenuCount, bytesOut);

    String s = "";
    switch(p1.charAt(0))
    {
      case 'A' : s = "All Purchases";                          break;
      case 'I' : s = "Purchases into Stock";                   break;
      case 'N' : s = "Non-Stock Purchases";                    break;
      case 'S' : s = "Sales from Stock";                       break;
      case 'M' : s = "Miscellaneous Purchases";                break;
      case 'B' : s = "Purchases Opening Balances";             break;
      case 'E' : s = "Everything affecting Purchases Account"; break; // to give closing
      case 'F' : s = "Everything affecting Stock Account";     break; // to give closing
      case 'T' : s = "Stock Opening Balances";                 break;
      case 'U' : s = "Miscellaneous Stock";                    break;
    }          

    if(! plain)
      pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Purchases Reconciliation Analysis: " + s, "6033", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);
    else pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Purchases Reconciliation Analysis: " + s, "", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<form><table id='page' width=100% border=0>");

    String tmpTable = unm + "_tmp";

    directoryUtils.createTmpTable(true, con, stmt, "Type char(1), Code char(20), Reference char(20), Description char(100), Total decimal(19,8), "
                                        + "BaseTotal decimal(19,8), Date date, Currency char(3), stockDRs decimal(19,8), stockCRs decimal(19,8), "
                                        + "NonStockDRs decimal(19,8), NonStockCRs decimal(19,8)", "", tmpTable);
    
    calc(con, stmt, stmt2, rs, rs2, p1.charAt(0), year, dateFrom, dateTo, unm, dnm, workingDir, localDefnsDir, defnsDir, tmpTable);
    
    processTmpTable(con, stmt, rs, p1.charAt(0), tmpTable, out, bytesOut);

    directoryUtils.removeTmpTable(con, stmt, tmpTable); 

    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  public void calc(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, char searchType, String year, String dateFrom, String dateTo,
                   String unm, String dnm, String workingDir, String localDefnsDir, String defnsDir, String tmpTable) throws Exception
  {
    // need to know financial year start date for WAC start-from
    String yyyy = generalUtils.getFromDefnFile(unm, "accounts.dfn", workingDir, "");
    String[] financialYearStartMonth = new String[1];
    String[] financialYearEndMonth   = new String[1];
    definitionTables.getAppConfigFinancialYearMonths(con, stmt, rs, dnm, financialYearStartMonth, financialYearEndMonth);
    String financialYearStartDate = yyyy + "-" + generalUtils.detMonthNumFromMonthName(financialYearStartMonth[0]) + "-01";

    boolean useWAC = false;
    if(miscDefinitions.inventoryCostingMethod(con, stmt, rs).equals("WAC"))
      useWAC = true;

    String salesAccount = accountsUtils.getSalesAccCode(year, dnm, localDefnsDir, defnsDir);
    String purchasesAccount = accountsUtils.getPurchasesAccCode(year, dnm, localDefnsDir, defnsDir);

    if(searchType == '1') // call from 6033b
    {
      calc2(con, stmt, stmt2, rs, rs2, 'A', year, dateFrom, dateTo, financialYearStartDate, useWAC, salesAccount, purchasesAccount, unm, dnm, workingDir,
            localDefnsDir, defnsDir, tmpTable);
    }
    else
    if(searchType == '2') // call from 6033b
    {
      calc2(con, stmt, stmt2, rs, rs2, 'F', year, dateFrom, dateTo, financialYearStartDate, useWAC, salesAccount, purchasesAccount, unm, dnm, workingDir,
            localDefnsDir, defnsDir, tmpTable);
    }
    else
    {
      calc2(con, stmt, stmt2, rs, rs2, searchType, year, dateFrom, dateTo, financialYearStartDate, useWAC, salesAccount, purchasesAccount, unm, dnm, workingDir,
            localDefnsDir, defnsDir, tmpTable);
    }   
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  public void calc2(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, char searchType, String year, String dateFrom, String dateTo,
                    String financialYearStartDate, boolean useWAC, String salesAccount, String purchasesAccount, String unm, String dnm, String workingDir,
                    String localDefnsDir, String defnsDir, String tmpTable) throws Exception
  {
    switch(searchType)
    {
      case 'A' : // All Purchases
      case 'I' : // Purchases into Stock
      case 'N' : // Non-Stock Purchases
      case 'S' : // Sales from Stock
      case 'M' : // Miscellaneous Purchases
      case 'B' : // Purchases Opening Balances
      case 'E' : // Everything affecting Purchases Account (for closing)
                 onPurchaseInvoiceLine(con, stmt, stmt2, rs, rs2, purchasesAccount, dateFrom, dateTo, tmpTable);
                 onPurchaseDebitNote(con, stmt, stmt2, rs, dateFrom, dateTo, tmpTable);
                 onPurchaseCreditNoteLine(con, stmt, stmt2, rs, rs2, purchasesAccount, dateFrom, dateTo, tmpTable);

                 onPaymentVoucher(con, stmt, stmt2, rs, purchasesAccount, dateFrom, dateTo, tmpTable);
                 onReceiptVoucher(con, stmt, stmt2, rs, purchasesAccount, dateFrom, dateTo, tmpTable);
    
                 onIATDr(con, stmt, stmt2, rs, purchasesAccount, dateFrom, dateTo, tmpTable);
                 onIATCr(con, stmt, stmt2, rs, purchasesAccount, dateFrom, dateTo, tmpTable);

                 onBatchCr(con, stmt2, purchasesAccount, year, dateFrom, dateTo, tmpTable, dnm);
                 onBatchDr(con, stmt2, purchasesAccount, year, dateFrom, dateTo, tmpTable, dnm);

                 onInvoiceLine(useWAC, con, stmt, stmt2, rs, rs2, dateFrom, dateTo, financialYearStartDate, salesAccount, tmpTable, dnm);
                 onSalesCreditNoteLine(useWAC, con, stmt, stmt2, rs, rs2, dateFrom, dateTo, financialYearStartDate, tmpTable, dnm);
                 break;
      case 'F' : // Everything affecting Stock Account (for closing)
      case 'T' : // Stock Opening Balances
      case 'U' : // Miscellaneous Stock
                 String stockAccount = accountsUtils.getStockAccCode(year, dnm, localDefnsDir, defnsDir);

                 onPurchaseInvoiceLine(con, stmt, stmt2, rs, rs2, purchasesAccount, dateFrom, dateTo, tmpTable);
                 onPurchaseDebitNote(con, stmt, stmt2, rs, dateFrom, dateTo, tmpTable);
                 onPurchaseCreditNoteLine(con, stmt, stmt2, rs, rs2, purchasesAccount, dateFrom, dateTo, tmpTable);

                 onIATDr(con, stmt, stmt2, rs, stockAccount, dateFrom, dateTo, tmpTable);
                 onIATCr(con, stmt, stmt2, rs, stockAccount, dateFrom, dateTo, tmpTable);

                 onBatchCr(con, stmt2, stockAccount, year, dateFrom, dateTo, tmpTable, dnm);
                 onBatchDr(con, stmt2, stockAccount, year, dateFrom, dateTo, tmpTable, dnm);

                 onPaymentVoucher(con, stmt, stmt2, rs, stockAccount, dateFrom, dateTo, tmpTable);
                 onReceiptVoucher(con, stmt, stmt2, rs, stockAccount, dateFrom, dateTo, tmpTable);

                 onInvoiceLine(useWAC, con, stmt, stmt2, rs, rs2, dateFrom, dateTo, financialYearStartDate, salesAccount, tmpTable, dnm);
                 onDebitNote(con, stmt, stmt2, rs, dateFrom, dateTo, tmpTable);
                 onSalesCreditNoteLine(useWAC, con, stmt, stmt2, rs, rs2, dateFrom, dateTo, financialYearStartDate, tmpTable, dnm);
                 break;
    }  
    }
    
      
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void onPaymentVoucher(Connection con, Statement stmt, Statement stmt2, ResultSet rs, String accCode, String dateFrom, String dateTo, String tmpTable)
                                throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.VoucherCode, t1.CompanyName, t2.Amount2, t2.Amount, t1.Date, t1.Currency "
                         + "FROM voucherl AS t2 INNER JOIN voucher AS t1 ON t1.VoucherCode = t2.VoucherCode WHERE t2.AccountDr = '" + accCode
                         + "' AND t1.Status != 'C' AND t1.Date >= {d '" + dateFrom + "'} AND t1.Date <= {d '" + dateTo + "'}");
    double amount, baseAmount;           
            
    while(rs.next())
    {    
      amount     = generalUtils.doubleFromStr(rs.getString(3));
      baseAmount = generalUtils.doubleFromStr(rs.getString(4));
      
      addToTmpTable(con, stmt2, "V", rs.getString(1), rs.getString(2), amount, baseAmount, rs.getString(5), rs.getString(6), baseAmount, 0, 0, 0, tmpTable);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void onReceiptVoucher(Connection con, Statement stmt, Statement stmt2, ResultSet rs, String accCode, String dateFrom, String dateTo, String tmpTable)
                                throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.VoucherCode, t1.CompanyName, t2.Amount2, t2.Amount, t1.Date, t1.Currency "
                         + "FROM rvoucherl AS t2 INNER JOIN rvoucher AS t1 ON t1.VoucherCode = t2.VoucherCode WHERE t2.AccountCr = '" + accCode
                         + "' AND t1.Status != 'C' AND t1.Date >= {d '" + dateFrom + "'} AND t1.Date <= {d '" + dateTo + "'}");
    double amount, baseAmount;           
            
    while(rs.next())
    {    
      amount     = generalUtils.doubleFromStr(rs.getString(3));
      baseAmount = generalUtils.doubleFromStr(rs.getString(4));
      
      addToTmpTable(con, stmt2, "R", rs.getString(1), rs.getString(2), amount, baseAmount, rs.getString(5), rs.getString(6), 0, baseAmount, 0, 0, tmpTable);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void onPurchaseInvoiceLine(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String accCode, String dateFrom,
                                     String dateTo, String tmpTable) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.InvoiceCode, t1.CompanyName, t2.Amount2, t2.Amount, t1.Date, t1.Currency, t2.ItemCode, t1.GroupDiscount, "
                         + "t1.GroupDiscountType, t1.TotalTotal, t1.GSTTotal  "
                         + "FROM pinvoicel AS t2 INNER JOIN pinvoice AS t1 ON t1.InvoiceCode = t2.InvoiceCode WHERE t2.Category = '" + accCode
                         + "' AND t1.Status != 'C' AND t1.Date >= {d '" + dateFrom + "'} AND t1.Date <= {d '" + dateTo + "'} ORDER BY t2.InvoiceCode");
    
    double amt = 0.0, bAmt = 0.0, amount, baseAmount, groupDiscount = 1, totalTotal, gstTotal, stockDRs = 0.0, nonStockDRs = 0.0;
    String itemCode, invoiceCode, companyName="", date="", currency="", lastInvoiceCode = "", groupDiscountType;
    boolean first = true;

    while(rs.next())
    {    
      invoiceCode = rs.getString(1);

      if(! invoiceCode.equals(lastInvoiceCode))
      {
        if(! first)
        {
          amt = generalUtils.doubleDPs(generalUtils.doubleDPs(amt, '2') - generalUtils.doubleDPs((generalUtils.doubleDPs(amt, '2') * groupDiscount), '2'), '2');
          bAmt = generalUtils.doubleDPs(generalUtils.doubleDPs(bAmt, '2') - generalUtils.doubleDPs((generalUtils.doubleDPs(bAmt, '2') * groupDiscount), '2'), '2');
          if(bAmt != 0.0)
            addToTmpTable(con, stmt2, "N", lastInvoiceCode, companyName, amt, bAmt, date, currency, stockDRs, 0, nonStockDRs, 0, tmpTable);
          amt  = 0.0;
          bAmt = 0.0;
          stockDRs = 0.0;
          nonStockDRs = 0.0;
        }
        else first = false;

        lastInvoiceCode = invoiceCode;
      }

      companyName       = rs.getString(2);
      amount            = generalUtils.doubleFromStr(rs.getString(3));
      baseAmount        = generalUtils.doubleFromStr(rs.getString(4));
      date              = rs.getString(5);
      currency          = rs.getString(6);     
      itemCode          = rs.getString(7);
      groupDiscount     = generalUtils.doubleFromStr(rs.getString(8));
      groupDiscountType = rs.getString(9);
      totalTotal        = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(10)), '2');
      gstTotal          = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(11)), '2');

      if(groupDiscount != 0.0) // groupDiscount exists
      {
        if(! groupDiscountType.equals("%")) // then must convert it into a percentage
          groupDiscount = groupDiscount / (totalTotal + groupDiscount - gstTotal);
        else groupDiscount /= 100.0;
      }
      
      if(itemExists(con, stmt2, rs2, itemCode))
        stockDRs += baseAmount;
      else nonStockDRs += baseAmount;
          
      amt  += amount;
      bAmt += baseAmount;
    }

    if(lastInvoiceCode.length() > 0)
    {
      amt = generalUtils.doubleDPs(generalUtils.doubleDPs(amt, '2') - generalUtils.doubleDPs((generalUtils.doubleDPs(amt, '2') * groupDiscount), '2'), '2');
      bAmt = generalUtils.doubleDPs(generalUtils.doubleDPs(bAmt, '2') - generalUtils.doubleDPs((generalUtils.doubleDPs(bAmt, '2') * groupDiscount), '2'), '2');
      if(bAmt != 0.0)
        addToTmpTable(con, stmt2, "N", lastInvoiceCode, companyName, amt, bAmt, date, currency, stockDRs, 0.0, nonStockDRs, 0.0, tmpTable);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void onPurchaseCreditNoteLine(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String accCode, String dateFrom,
                                        String dateTo, String tmpTable) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.PCNCode, t1.CompanyName, t2.Amount2, t2.Amount, t1.Date, t1.Currency, t2.ItemCode, "
                         + "t1.GroupDiscount, t1.GroupDiscountType, t1.TotalTotal, t1.gstTotal "
                         + "FROM pcreditl AS t2 INNER JOIN pcredit AS t1 ON t1.PCNCode = t2.PCNCode WHERE t2.Category = '" + accCode
                         + "' AND t1.Status != 'C' AND t1.Date >= {d '" + dateFrom + "'} AND t1.Date <= {d '" + dateTo + "'} ORDER BY t2.PCNCode");
    
    double amt = 0.0, bAmt = 0.0, amount, baseAmount, groupDiscount = 1, totalTotal, lastGroupDiscount = 0, gstTotal;
    String itemCode, pcnCode, companyName="", date="", currency="", lastCurrency="", lastDate="", lastPCNCode = "", groupDiscountType;
    boolean first = true;
    
    while(rs.next())
    {    
      itemCode = rs.getString(7);
      
      if(! itemExists(con, stmt2, rs2, itemCode))
      {      
        pcnCode     = rs.getString(1);
        companyName = rs.getString(2);
        amount      = generalUtils.doubleFromStr(rs.getString(3));
        baseAmount  = generalUtils.doubleFromStr(rs.getString(4));
        date        = rs.getString(5);
        currency    = rs.getString(6);     
        groupDiscount     = generalUtils.doubleFromStr(rs.getString(8));
        groupDiscountType = rs.getString(9);
        totalTotal        = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(10)), '2');
        gstTotal          = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(11)), '2');

        if(groupDiscount != 0.0) // groupDiscount exists
        {
          if(! groupDiscountType.equals("%")) // then must convert it into a percentage
            groupDiscount = groupDiscount / (totalTotal + groupDiscount - gstTotal);
          else groupDiscount /= 100.0;
        }

        if(amount != 0.0)
        {
          if(pcnCode.equals(lastPCNCode) || first)
          {
            amt  += amount;
            bAmt += baseAmount;
            if(first)
            {
              lastPCNCode  = pcnCode;
              lastCurrency = currency;
              lastDate     = date;
              lastGroupDiscount = groupDiscount;
              first = false;
            }
          }
          else
          {
            amt = generalUtils.doubleDPs(generalUtils.doubleDPs(amt, '2') - generalUtils.doubleDPs((generalUtils.doubleDPs(amt, '2') * lastGroupDiscount), '2'), '2');
            bAmt = generalUtils.doubleDPs(generalUtils.doubleDPs(bAmt, '2') - generalUtils.doubleDPs((generalUtils.doubleDPs(bAmt, '2') * lastGroupDiscount), '2'), '2');
            addToTmpTable(con, stmt2, "Q", lastPCNCode, companyName, amt, bAmt, lastDate, lastCurrency, 0, 0, 0, bAmt, tmpTable);
            amt  = amount;
            bAmt = baseAmount;
            lastPCNCode  = pcnCode;
            lastCurrency = currency;
            lastGroupDiscount = groupDiscount;
            lastDate     = date;
          }
        }          
      }        
    }

    if(lastPCNCode.length() > 0)
    {
      if(amt != 0.0)
      {
        amt = generalUtils.doubleDPs(generalUtils.doubleDPs(amt, '2') - generalUtils.doubleDPs((generalUtils.doubleDPs(amt, '2') * lastGroupDiscount), '2'), '2');
        bAmt = generalUtils.doubleDPs(generalUtils.doubleDPs(bAmt, '2') - generalUtils.doubleDPs((generalUtils.doubleDPs(bAmt, '2') * lastGroupDiscount), '2'), '2');
        addToTmpTable(con, stmt2, "Q", lastPCNCode, companyName, amt, bAmt, lastDate, lastCurrency, 0, 0, 0, bAmt, tmpTable);
      }
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void onInvoiceLine(boolean useWAC, Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String dateFrom, String dateTo,
                             String financialYearStartDate, String salesAccount, String tmpTable, String dnm) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.InvoiceCode, t1.CompanyName, t2.Amount2, t1.Date, t1.Currency, t2.ItemCode, t2.Quantity, "
                         + "t1.GroupDiscount, t1.GroupDiscountType, t1.TotalTotal, t1.GSTTotal "
                         + "FROM invoicel AS t2 INNER JOIN invoice AS t1 ON t1.InvoiceCode = t2.InvoiceCode WHERE Category = '" + salesAccount
                         + "' AND t1.Status != 'C' AND t1.Date >= {d '" + dateFrom + "'} AND t1.Date <= {d '" + dateTo + "'} ORDER BY t2.InvoiceCode");

    double bAmt = 0.0, baseAmount, quantity, groupDiscount = 1, totalTotal, gstTotal;
    String itemCode, invoiceCode, companyName="", date="", currency="", lastInvoiceCode = "", groupDiscountType;
    boolean first = true;
    
    while(rs.next())
    {    
      invoiceCode = rs.getString(1);
      
      if(! invoiceCode.equals(lastInvoiceCode))
      {
        if(! first)
        {
          bAmt = generalUtils.doubleDPs(generalUtils.doubleDPs(bAmt, '2') - generalUtils.doubleDPs((generalUtils.doubleDPs(bAmt, '2') * groupDiscount), '2'), '2');
          if(bAmt != 0.0)
            addToTmpTable(con, stmt2, "Y", lastInvoiceCode, companyName, 0, bAmt, date, currency, 0, bAmt, 0, 0, tmpTable);
          bAmt = 0.0;
        }
        else first = false;

        lastInvoiceCode = invoiceCode;
      }
      
      companyName       = rs.getString(2);
      date              = rs.getString(4);
      currency          = rs.getString(5);
      itemCode          = rs.getString(6);
      quantity          = generalUtils.doubleFromStr(rs.getString(7));
      groupDiscount     = generalUtils.doubleFromStr(rs.getString(8));
      groupDiscountType = rs.getString(9);
      totalTotal        = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(10)), '2');
      gstTotal          = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(11)), '2');

      if(groupDiscount != 0.0) // groupDiscount exists
      {
        if(! groupDiscountType.equals("%")) // then must convert it into a percentage
          groupDiscount = groupDiscount / (totalTotal + groupDiscount - gstTotal);
        else groupDiscount /= 100.0;
      }

      if(itemExists(con, stmt2, rs2, itemCode))
      {      
        if(useWAC)
        {
          baseAmount = inventory.getWAC(con, stmt2, rs2, itemCode, financialYearStartDate, date, dnm);
          baseAmount *= quantity;
        }
        else baseAmount = (generalUtils.doubleFromStr(rs.getString(3)) * quantity);
        
        bAmt += baseAmount;
      }        
    }

    if(lastInvoiceCode.length() > 0)
    {
      bAmt = generalUtils.doubleDPs(generalUtils.doubleDPs(bAmt, '2') - generalUtils.doubleDPs((generalUtils.doubleDPs(bAmt, '2') * groupDiscount), '2'), '2');
      if(bAmt != 0.0)
        addToTmpTable(con, stmt2, "Y", lastInvoiceCode, companyName, 0, bAmt, date, currency, 0, bAmt, 0, 0, tmpTable);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void onSalesCreditNoteLine(boolean useWAC, Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String dateFrom, String dateTo,
                                String financialYearStartDate, String tmpTable, String dnm) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.CNCode, t1.CompanyName, t2.Amount2, t2.Amount, t1.Date, t1.Currency, t2.ItemCode, t2.Quantity, t2.InvoiceCode "
                         + "FROM creditl AS t2 INNER JOIN credit AS t1 ON t1.CNCode = t2.CNCode WHERE      CostOfSaleAdjustment = 'Y' AND  "
                         + "t1.Status != 'C' AND t1.Date >= {d '" + dateFrom + "'} AND t1.Date <= {d '" + dateTo + "'}");

    double amt = 0, bAmt = 0.0, amount, baseAmount, quantity;
    String itemCode, cnCode, companyName="", date="", currency="", lastCurrency="", lastDate="", lastCNCode = "", lastCompanyName="", invoiceCode,
           dateOfInvoice;
    boolean first = true;
    
    while(rs.next())
    {    
      itemCode    = rs.getString(7);
      date        = rs.getString(5);
      quantity    = generalUtils.doubleFromStr(rs.getString(8));
      invoiceCode = rs.getString(9);

      if(itemExists(con, stmt2, rs2, itemCode))
      {      
        if(useWAC)
        {
          // date must be of the original invoice in order to get the correct WAC
          dateOfInvoice = salesInvoice.getAnInvoiceFieldGivenCode(con, stmt, rs, "date", invoiceCode);

          baseAmount = inventory.getWAC(con, stmt2, rs2, itemCode, financialYearStartDate, dateOfInvoice, dnm);
          baseAmount *= quantity;
        }
        else baseAmount = generalUtils.doubleFromStr(rs.getString(4));

        if(baseAmount != 0.0)
        {
          cnCode      = rs.getString(1);
          companyName = rs.getString(2);
          amount      = generalUtils.doubleFromStr(rs.getString(3));
          currency    = rs.getString(6);     

          if(cnCode.equals(lastCNCode) || first)
          {
            amt  += amount;
            bAmt += baseAmount;
            if(first)
            {
              lastCNCode   = cnCode;
              lastCurrency = currency;
              lastDate     = date;
              lastCompanyName = companyName;
              first = false;
            }
          }
          else
          {
            addToTmpTable(con, stmt2, "W", lastCNCode, lastCompanyName, 0, bAmt, lastDate, lastCurrency, bAmt, 0, 0, 0, tmpTable);
            amt  = amount;
            bAmt = baseAmount;
            lastCNCode   = cnCode;
            lastCurrency = currency;
            lastDate     = date;
            lastCompanyName = companyName;
          }
        }          
      }        
    }

    if(lastCNCode.length() > 0)
    {
      if(bAmt != 0.0)
        addToTmpTable(con, stmt2, "W", lastCNCode, lastCompanyName, 0, bAmt, lastDate, lastCurrency, bAmt,0,  0, 0, tmpTable);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void onDebitNote(Connection con, Statement stmt, Statement stmt2, ResultSet rs, String dateFrom, String dateTo, String tmpTable) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT DNCode, CompanyName, TotalTotal, BaseTotalTotal, Date, Currency "
                         + "FROM debit WHERE Status != 'C' AND Date >= {d '" + dateFrom + "'} AND Date <= {d '" + dateTo + "'}");
    double amount, baseAmount;           
            
    while(rs.next())
    {    
      amount     = generalUtils.doubleFromStr(rs.getString(3));
      baseAmount = generalUtils.doubleFromStr(rs.getString(4));

      addToTmpTable(con, stmt2, "X", rs.getString(1), rs.getString(2), amount, baseAmount, rs.getString(5), rs.getString(6), 0, baseAmount, 0, 0, tmpTable);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void onPurchaseDebitNote(Connection con, Statement stmt, Statement stmt2, ResultSet rs, String dateFrom, String dateTo, String tmpTable)
                                   throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT PDNCode, CompanyName, TotalTotal, BaseTotalTotal, Date, Currency "
                         + "FROM pdebit WHERE Status != 'C' AND Date >= {d '" + dateFrom + "'} AND Date <= {d '" + dateTo + "'}");
    double amount, baseAmount;           
            
    while(rs.next())
    {    
      amount     = generalUtils.doubleFromStr(rs.getString(3));
      baseAmount = generalUtils.doubleFromStr(rs.getString(4));

      addToTmpTable(con, stmt2, "E", rs.getString(1), rs.getString(2), amount, baseAmount, rs.getString(5), rs.getString(6), 0, baseAmount, 0, 0, tmpTable);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void onIATCr(Connection con, Statement stmt, Statement stmt2, ResultSet rs, String accCode, String dateFrom, String dateTo, String tmpTable)
                       throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT IATCode, Remark, AmountCr, TransactionDate, CurrencyCr, RateCr "
                         + "FROM iat WHERE AccountCr = '" + accCode + "' AND Status != 'C' AND TransactionDate >= {d '" + dateFrom
                         + "'} AND TransactionDate <= {d '" + dateTo + "'}");
    
    double amount, baseAmount, rate;
            
    while(rs.next())
    {    
      amount = generalUtils.doubleFromStr(rs.getString(3));
      rate   = generalUtils.doubleFromStr(rs.getString(6));

      baseAmount = generalUtils.doubleDPs((amount * rate), '2');

      addToTmpTable(con, stmt2, "I", rs.getString(1), rs.getString(2), amount, baseAmount, rs.getString(4), rs.getString(5), 0, baseAmount, 0, 0, tmpTable);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void onIATDr(Connection con, Statement stmt, Statement stmt2, ResultSet rs, String accCode, String dateFrom, String dateTo, String tmpTable)
                       throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT IATCode, Remark, AmountDr, TransactionDate, CurrencyDr, RateDr "
                         + "FROM iat WHERE AccountDr = '" + accCode + "' AND Status != 'C' AND TransactionDate >= {d '" + dateFrom
                         + "'} AND TransactionDate <= {d '" + dateTo + "'}");
    
    double amount, baseAmount, rate;
            
    while(rs.next())
    {    
      amount = generalUtils.doubleFromStr(rs.getString(3));
      rate   = generalUtils.doubleFromStr(rs.getString(6));

      baseAmount = generalUtils.doubleDPs((amount * rate), '2');

      addToTmpTable(con, stmt2, "J", rs.getString(1), rs.getString(2), amount, baseAmount, rs.getString(4), rs.getString(5), baseAmount, 0, 0, 0, tmpTable);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void onBatchCr(Connection con, Statement stmt2, String accCode, String year, String dateFrom, String dateTo, String tmpTable, String dnm)
                         throws Exception
  {
    Connection accCon  = null;
    Statement  accStmt = null;
    ResultSet  accRs   = null;
    
    try
    {
      String uName = directoryUtils.getMySQLUserName();
      String pWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();
    
      accCon = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_accounts_" + year + "?user=" + uName + "&password=" + pWord);

      accStmt = accCon.createStatement();

      accRs = accStmt.executeQuery("SELECT Code, Remark, Amount, BaseAmount, TransactionDate, Currency "
                                 + "FROM joubatchl WHERE AccCode = '" + accCode + "' AND DrCr = 'C' AND TransactionDate >= {d '" + dateFrom
                                 + "'} AND TransactionDate <= {d '" + dateTo + "'}");
      
      double amount, baseAmount;
            
      while(accRs.next())
      {    
        amount     = generalUtils.doubleFromStr(accRs.getString(3));
        baseAmount = generalUtils.doubleFromStr(accRs.getString(4));

        addToTmpTable(con, stmt2, "B", accRs.getString(1), accRs.getString(2), amount, baseAmount, accRs.getString(5), accRs.getString(6), 0, baseAmount,
                      0, 0, tmpTable);
      }

      if(accRs   != null) accRs.close();                                 
      if(accStmt != null) accStmt.close();
      if(accCon  != null) accCon.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(accRs   != null) accRs.close();                                 
      if(accStmt != null) accStmt.close();
      if(accCon  != null) accCon.close();
    }    
  }
   
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void onBatchDr(Connection con, Statement stmt2, String accCode, String year, String dateFrom, String dateTo, String tmpTable, String dnm)
                         throws Exception
  {
    Connection accCon  = null;
    Statement  accStmt = null;
    ResultSet  accRs   = null;
    
    try
    {
      String uName = directoryUtils.getMySQLUserName();
      String pWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();
    
      accCon = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_accounts_" + year + "?user=" + uName + "&password=" + pWord);

      accStmt = accCon.createStatement();

      accRs = accStmt.executeQuery("SELECT Code, Remark, Amount, BaseAmount, TransactionDate, Currency "
                                 + "FROM joubatchl WHERE AccCode = '" + accCode + "' AND DrCr = 'D' AND TransactionDate >= {d '" + dateFrom
                                 + "'} AND TransactionDate <= {d '" + dateTo + "'}");
      
      double amount, baseAmount;
            
      while(accRs.next())
      {    
        amount     = generalUtils.doubleFromStr(accRs.getString(3));
        baseAmount = generalUtils.doubleFromStr(accRs.getString(4));

        addToTmpTable(con, stmt2, "C", accRs.getString(1), accRs.getString(2), amount, baseAmount, accRs.getString(5), accRs.getString(6), baseAmount, 0,
                      0, 0, tmpTable);
      }

      if(accRs   != null) accRs.close();                                 
      if(accStmt != null) accStmt.close();
      if(accCon  != null) accCon.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(accRs   != null) accRs.close();                                 
      if(accStmt != null) accStmt.close();
      if(accCon  != null) accCon.close();
    }    
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void processTmpTable(Connection con, Statement stmt, ResultSet rs, char searchType, String tmpTable, PrintWriter out, int[] bytesOut)
                               throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT * FROM " + tmpTable + " ORDER BY Date, Type"); // ensuring the OB (in JB) come first

    String code, type, desc, date, currency, cssFormat = "line1";
    double amount, baseAmount, baseBalance = 0.0, monthBalance = 0.0, stockDRs, stockCRs, nonStockDRs, nonStockCRs;
    int month, thisMonth = 0;

    scout(out, bytesOut, "<tr id='pageColumn'>");
    scout(out, bytesOut, "<td><p>Document Code</td>");
    scout(out, bytesOut, "<td><p>Document Type</td>");
    scout(out, bytesOut, "<td><p>Date</td>");
    scout(out, bytesOut, "<td><p>Dr/Cr</td>");
    scout(out, bytesOut, "<td><p>Issue Currency</td>");
    scout(out, bytesOut, "<td><p>Issue Amount</td>");
    scout(out, bytesOut, "<td><p>Base Amount</td>");
    scout(out, bytesOut, "<td><p>Month Balance</td>");
    scout(out, bytesOut, "<td><p>Base Balance</td>");
    scoutln(out, bytesOut, "<td><p>Description</td></tr>");
    
    boolean pInvoice = false, pCredit = false, invoice = false, credit = false, jBatch = false, voucher = false, rVoucher = false, iat = false, stock = false,
            nonStock = false, ob = false, debit = false, pDebit = false, forStockAccount = false;

    switch(searchType)
    {
      case 'A' : // All Purchases
                 pInvoice = true;
                 pDebit   = true;
                 pCredit  = true;
                 stock    = true;
                 nonStock = true;
                 break;
      case 'I' : // Purchases into Stock
                 pInvoice = true;
                 pDebit   = true;
                 pCredit  = true;
                 stock    = true;
                 break;
      case 'N' : // Non-Stock Purchases
                 pInvoice = true;
                 pDebit   = true;
                 pCredit  = true;
                 nonStock = true;
                 break;
      case 'S' : // Sales from Stock
                 invoice = true;
                 debit   = true;
                 credit  = true;
                 forStockAccount = true;
                 break;
      case 'M' : // Miscellaneous Purchases
      case 'U' : // Miscellaneous Stock
                 jBatch   = true;
                 iat      = true;
                 voucher  = true;
                 rVoucher = true;
                 break;
      case 'B' : // Purchases Opening Balances
      case 'T' : // Stock Opening Balances
                 jBatch = true;
                 ob     = true;
                 break;
      case 'E' : // Everything affecting Purchases Account (for closing)
                 ob       = true;
                 nonStock = true;
                 pInvoice = true;
                 pDebit   = true;
                 pCredit  = true;
                 invoice  = true;
                 debit    = true;
                 credit   = true;
                 jBatch   = true;
                 iat      = true;
                 voucher  = true;
                 rVoucher = true;
                 break;
      case 'F' : // Everything affecting Stock Account (for closing)
                 ob       = true;
                 stock    = true;
                 pInvoice = true;
                 pDebit   = true;
                 pCredit  = true;
                 invoice  = true;
                 debit    = true;
                 credit   = true;
                 jBatch   = true;
                 iat      = true;
                 voucher  = true;
                 rVoucher = true;
                 forStockAccount = true;
                 break;
    }

    String drCr = "";
    double totalCRs = 0.0, totalDRs = 0.0;
    boolean wanted;
   
    while(rs.next())
    {    
      type       = rs.getString(1);
      code       = rs.getString(2);
      desc       = rs.getString(4);
      amount     = generalUtils.doubleFromStr(generalUtils.doubleDPs(rs.getString(5), '2'));
      baseAmount = generalUtils.doubleFromStr(generalUtils.doubleDPs(rs.getString(6), '2'));
      date       = rs.getString(7);
      currency   = rs.getString(8);
      stockDRs      = generalUtils.doubleFromStr(generalUtils.doubleDPs(rs.getString(9),  '2'));
      stockCRs      = generalUtils.doubleFromStr(generalUtils.doubleDPs(rs.getString(10), '2'));
      nonStockDRs   = generalUtils.doubleFromStr(generalUtils.doubleDPs(rs.getString(11), '2'));
      nonStockCRs   = generalUtils.doubleFromStr(generalUtils.doubleDPs(rs.getString(12), '2'));
      
      month = generalUtils.getMonth(generalUtils.encodeFromYYYYMMDD(date));
      if(month != thisMonth)
      {
        if(! ob)
          scoutln(out, bytesOut, "<tr><td colspan=10><hr></td></tr>");
        thisMonth = month;
        monthBalance = 0.0;
      }      

      wanted = false;

      switch(type.charAt(0))
      {
        case 'V' : if(voucher)
                   {
                     scout(out, bytesOut, "<tr id=\"" + cssFormat + "\"><td><p><a href=\"javascript:viewVoucher('");
                     type = "Payment Voucher";
                     drCr = "Dr";
                     totalDRs += baseAmount;
                     baseBalance += baseAmount;
                     monthBalance += baseAmount;
                     wanted = true;
                   }
                   break;
        case 'R' : if(rVoucher)
                   {
                     scout(out, bytesOut, "<tr id=\"" + cssFormat + "\"><td><p><a href=\"javascript:viewRVoucher('");
                     type = "Receipt Voucher";
                     drCr = "Cr";
                     totalCRs += baseAmount;
                     baseBalance -= baseAmount;
                     monthBalance -= baseAmount;
                     wanted = true;
                   }
                   break;
        case 'I' : if(iat)
                   {
                     scout(out, bytesOut, "<tr id=\"" + cssFormat + "\"><td><p><a href=\"javascript:viewIAT('");
                     type = "Inter-Account Transfer";
                     drCr = "Cr";
                     totalCRs     += baseAmount;
                     baseBalance  -= baseAmount;
                     monthBalance -= baseAmount;
                     wanted = true;
                   }
                   break;
        case 'J' : if(iat)
                   {
                     scout(out, bytesOut, "<tr id=\"" + cssFormat + "\"><td><p><a href=\"javascript:viewIAT('");
                     type = "Inter-Account Transfer";
                     drCr = "Dr";
                     totalDRs     += baseAmount;
                     baseBalance  += baseAmount;
                     monthBalance += baseAmount;
                     wanted = true;
                   }
                   break;
        case 'B' : if(ob)
                   {
                     if(code.equals("Opening Balances"))
                       wanted = true;
                   }
                   
                   if(jBatch)
                   {
                     if(! code.equals("Opening Balances"))
                       wanted = true;
                   }
          
                   if(wanted)
                   {
                     scout(out, bytesOut, "<tr id=\"" + cssFormat + "\"><td><p><a href=\"javascript:viewJB('");
                     type = "Journal Batch";
                     drCr = "Cr";
                     totalCRs     += baseAmount;
                     baseBalance  -= baseAmount;
                     monthBalance -= baseAmount;
                   }
                   break;
        case 'C' : if(ob)
                   {
                     if(code.equals("Opening Balances"))
                       wanted = true;            
                   }
                   
                   if(jBatch)
                   {
                     if(! code.equals("Opening Balances"))
                       wanted = true;
                   }
          
                   if(wanted)
                   {
                     scout(out, bytesOut, "<tr id=\"" + cssFormat + "\"><td><p><a href=\"javascript:viewJB('");
                     type = "Journal Batch";
                     drCr = "Dr";
                     totalDRs     += baseAmount;
                     baseBalance  += baseAmount;
                     monthBalance += baseAmount;
                   }
                   break;
        case 'N' : if(pInvoice)
                   {
                     if(stock || nonStock)
                     {    
                       if(stockDRs != 0.0 || nonStockDRs != 0.0)
                       {
                         scout(out, bytesOut, "<tr id=\"" + cssFormat + "\"><td><p><a href=\"javascript:viewPInv('");
                         type = "Purchase Invoice";
                         drCr = "Dr";
                         wanted = true;
                       }
                     }
                     
                     if(stock)
                     {    
                       if(stockDRs != 0.0)
                       {
                         totalDRs     += stockDRs;
                         baseBalance  += stockDRs;
                         monthBalance += stockDRs;
                       }
                     }
                     
                     if(nonStock)
                     {
                       if(nonStockDRs != 0.0)
                       {
                         totalDRs     += nonStockDRs;
                         baseBalance  += nonStockDRs;
                         monthBalance += nonStockDRs;
                       }
                     }
                   }  
                   break;
        case 'Q' : if(pCredit)
                   {
                     if(stock || nonStock)
                     {    
                       if(stockDRs != 0.0 || nonStockDRs != 0.0)
                       {
                         scout(out, bytesOut, "<tr id=\"" + cssFormat + "\"><td><p><a href=\"javascript:viewPCN('");
                         type = "Purchase Credit Note";
                         drCr = "Cr";
                         wanted = true;
                       }
                     }

                     if(stock)
                     {
                       if(stockCRs != 0.0)
                       {
                         totalCRs     += stockCRs;
                         baseBalance  -= stockCRs;
                         monthBalance -= stockCRs;
                       }
                     }
                     
                     if(nonStock)
                     {
                       if(nonStockCRs != 0.0)
                       {
                         totalCRs     += nonStockCRs;
                         baseBalance  -= nonStockCRs;
                         monthBalance -= nonStockCRs;
                       }
                     }
                   }
                   break;
        case 'E' : if(pDebit)
                   {
                     if(stock || nonStock)
                     {    
                       if(stockDRs != 0.0 || nonStockDRs != 0.0)
                       {
                         scout(out, bytesOut, "<tr id=\"" + cssFormat + "\"><td><p><a href=\"javascript:viewPDN('");
                         type = "Purchase Debit Note";
                         drCr = "Dr";
                         wanted = true;
                       }
                     }

                     if(stock)
                     {
                       if(stockDRs != 0.0)
                       {
                         totalDRs     += stockDRs;
                         baseBalance  += stockDRs;
                         monthBalance += stockDRs;
                       }
                     }
                     
                     if(nonStock)
                     {
                       if(nonStockDRs != 0.0)
                       {
                         totalDRs     += nonStockDRs;
                         baseBalance  += nonStockDRs;
                         monthBalance += nonStockDRs;
                       }
                     }
                   }
                   break;
        case 'Y' : if(invoice)
                   {
                     scout(out, bytesOut, "<tr id=\"" + cssFormat + "\"><td><p><a href=\"javascript:viewInvoice('");
                     type = "Sales Invoice";
                     
                     if(forStockAccount)
                     {
                       drCr = "Cr";
                       totalCRs     += baseAmount;
                       baseBalance  -= baseAmount;
                       monthBalance -= baseAmount;
                     }
                     else
                     {                    
                       drCr = "Dr";
                       totalDRs     += baseAmount;
                       baseBalance  += baseAmount;
                       monthBalance += baseAmount;
                     }
                     wanted = true;
                   }
                   break;
        case 'W' : if(credit)
                   {
                     scout(out, bytesOut, "<tr id=\"" + cssFormat + "\"><td><p><a href=\"javascript:viewCN('");
                     type = "Sales Credit Note";

                     if(forStockAccount)
                     {
                       drCr = "Dr";
                       totalDRs     += baseAmount;
                       baseBalance  += baseAmount;
                       monthBalance += baseAmount;
                     }
                     else
                     {                    
                       drCr = "Cr";
                       totalCRs     += baseAmount;
                       baseBalance  -= baseAmount;
                       monthBalance -= baseAmount;
                     }
                     
                     wanted = true;
                   }
                   break;
        case 'X' : if(debit)
                   {
                     scout(out, bytesOut, "<tr id=\"" + cssFormat + "\"><td><p><a href=\"javascript:viewDN('");
                     type = "Sales Debit Note";

                     if(forStockAccount)
                     {
                       drCr = "Cr";
                       totalCRs     += baseAmount;
                       baseBalance  -= baseAmount;
                       monthBalance -= baseAmount;
                     }
                     else
                     {                    
                       drCr = "Dr";
                       totalDRs     += baseAmount;
                       baseBalance  += baseAmount;
                       monthBalance += baseAmount;
                     }
                     wanted = true;
                   }
                   break;
      }
            
      if(wanted)
      {
        scoutln(out, bytesOut, code + "')\">" + code + "</a></td>");
    
        scoutln(out, bytesOut, "<td nowrap><p>" + type + "</td>"); 
        scoutln(out, bytesOut, "<td nowrap><p>" + generalUtils.convertFromYYYYMMDD(date) + "</td>");

        if(drCr.equals("Dr"))
          scoutln(out, bytesOut, "<td nowrap><p>" + drCr + "</td>");
        else scoutln(out, bytesOut, "<td nowrap align=right><p>" + drCr + "</td>");       

        scoutln(out, bytesOut, "<td nowrap align=right><p>" + currency + "</td>"); 

        if(amount == 0.0)
          scoutln(out, bytesOut, "<td nowrap align=right><p>-</td>");
        else scoutln(out, bytesOut, "<td nowrap align=right><p>" + generalUtils.formatNumeric(amount, '2') + "</td>");
      
        if(type.equals("Sales Invoice"))
        {
          scoutln(out, bytesOut, "<td nowrap align=right><p><a href=\"javascript:wac('" + code + "','" + date + "')\">"
                                 + generalUtils.formatNumeric(baseAmount, '2') + "</a></td>");
        }
        else scoutln(out, bytesOut, "<td nowrap align=right><p>" + generalUtils.formatNumeric(baseAmount, '2') + "</td>");

        scoutln(out, bytesOut, "<td nowrap align=right><p>" + generalUtils.formatNumeric(monthBalance, '2') + "</td>");
        scoutln(out, bytesOut, "<td nowrap align=right><p>" + generalUtils.formatNumeric(baseBalance, '2') + "</td>");
        scoutln(out, bytesOut, "<td nowrap><p>" + desc + "</td></tr>");
      }

      if(cssFormat.equals("line1")) cssFormat = "line2"; else cssFormat = "line1";      
    }

    scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\"><td nowrap colspan=3><p>Debits: " + generalUtils.formatNumeric(totalDRs, '2') + "</td></tr>");

    scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\"><td nowrap colspan=3><p>Credits: " + generalUtils.formatNumeric(totalCRs, '2') + "</td></tr>");

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void addToTmpTable(Connection con, Statement stmt, String type, String code, String desc, double amount, double baseAmount, String date,
                             String currency, double stockDRs, double stockCRs, double nonStockDRs, double nonStockCRs, String tmpTable) throws Exception
  {
    stmt = con.createStatement();
   
    amount     = generalUtils.doubleDPs(amount, '2');
    baseAmount = generalUtils.doubleDPs(baseAmount, '2');
  
    String q = "INSERT INTO " + tmpTable + " ( Type, Code, Description, Total, BaseTotal, Date, Currency, StockDRs, StockCRs, NonStockDRs, NonStockCRs ) "
             + "VALUES ( '" + type + "', '" + code + "', '" + generalUtils.sanitiseForSQL(desc) + "', '" + amount + "', '" + baseAmount + "', {d '" + date + "'}, '"
             + currency + "', '" + stockDRs + "', '" + stockCRs + "', '" + nonStockDRs + "', '" + nonStockCRs + "' )";

    stmt.executeUpdate(q);
  
    if(stmt != null) stmt.close();
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean itemExists(Connection con, Statement stmt, ResultSet rs, String itemCode) throws Exception
  {
    if(itemCode.length() == 0) // just-in-case
      return false;

    if(itemCode.equals("-")) // quick check
      return false;

    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM stock WHERE ItemCode = '" + itemCode + "'");

    int numRecs = 0;
    
    if(rs.next())
      numRecs = rs.getInt("rowcount") ;

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    if(numRecs == 1)
      return true;
    
    return false;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scout(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.print(str);
    bytesOut[0] += str.length();    
  }
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }
    
}
