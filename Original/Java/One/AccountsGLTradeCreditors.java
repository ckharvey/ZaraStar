// =======================================================================================================================================================================================================
// System: ZaraStar: Accounts: GL Trade Creditors
// Module: AccountsGLTradeCreditors.java
// Author: C.K.Harvey
// Copyright (c) 2000-07 Christopher Harvey. All Rights Reserved.
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

public class AccountsGLTradeCreditors extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  AccountsUtils accountsUtils = new AccountsUtils();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p2="", p3="", p4="", p5="";

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
      p2  = req.getParameter("p2"); // dateFrom
      p3  = req.getParameter("p3"); // dateTo
      p4  = req.getParameter("p4"); // plain or not
      p5  = req.getParameter("p5"); // currency
      
      if(p4 == null) p4 = "";
      if(p5 == null) p5 = "";

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p2, p3, p4, p5, bytesOut);
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

      System.out.println("6007h: " + e);
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "AccountsGLTradeCreditors", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6007, bytesOut[0], 0, "ERR:" + p2 + " " + p3 + " " + p5);
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p2,
                    String p3, String p4, String p5, int[] bytesOut) throws Exception
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
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "AccountsGLTradeCreditors", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6007, bytesOut[0], 0, "ACC:" + p2 + " " + p3 + " " + p5);
      if(con  != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "AccountsGLTradeCreditors", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6007, bytesOut[0], 0, "SID:" + p2 + " " + p3 + " " + p5);
      if(con  != null) con.close();
      if(out != null) out.flush();
      return;
    }

    String dateFrom, dateTo;
    if(p2 == null || p2.length() == 0) // dateFrom
    {  
      dateFrom = "1970-01-01";
      p2       = "01.01.1970";
    }
    else dateFrom = generalUtils.convertDateToSQLFormat(p2);
    
    if(p3 == null || p3.length() == 0) // dateTo
    {
      dateTo = "2099-12-31";
      p3     = "31.12.2099";
    }
    else dateTo = generalUtils.convertDateToSQLFormat(p3);
    
    boolean plain = false;
    if(p4.equals("P"))
      plain = true;
        
    set(con, stmt, stmt2, rs, rs2, out, req, dateFrom, dateTo, p2, p3, plain, p5, unm, sid, uty, men, den, dnm, bnm, workingDir, localDefnsDir, defnsDir,
        bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 6007, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p2 + " " + p3 + " " + p5);
    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, HttpServletRequest req,
                   String dateFrom, String dateTo, String p2, String p3, boolean plain, String p5, String unm, String sid, String uty, String men,
                   String den, String dnm, String bnm, String workingDir, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>GL Account: Trade Creditors</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 6056, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewRVoucher(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/ReceiptVoucherPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men
                           + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }
  
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 6066, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewVoucher(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/PaymentVoucherPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men
                           + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }
  
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 5080, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewPInv(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/PurchaseInvoicePage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men
                           + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }
  
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 5038, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewPDN(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/PurchaseDebitNoteHeaderEdit?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men
                           + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 5026, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewPCN(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/PurchaseCreditNotePage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men
                           + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 5049, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewPayment(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/PaymentPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men
                           + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 6023, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewJB(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/AccountsBatchEntries?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men
                           + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+p1;}");
    }
  
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
      scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"print\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "generalPlain.css\">");
    else scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");
    
    int[] hmenuCount = new int[1];
 
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "6007", "", true, plain, "", p2, p3, "", "", "AccountsGLTradeCreditors", unm, sid, uty, men, den, dnm,
                          bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    String year = generalUtils.getFromDefnFile(unm, "accounts.dfn", workingDir, "");

    String account = accountsUtils.getTradeCreditorsAccCode(year, dnm, localDefnsDir, defnsDir);

    if(! plain)
    {
      pageFrameUtils.drawTitle(out, true, plain, "AccountsGLTradeCreditors", account, dateFrom, dateTo, p5, "", "GL Account: " + account + " - " + accountsUtils.getAccountDescriptionGivenAccCode(account, year, dnm, localDefnsDir, defnsDir) + " ("
                                                                                 + accountsUtils.getAccountCurrencyGivenAccCode(account, year, dnm, localDefnsDir, defnsDir) + " "
                                                                                 + accountsUtils.getAccountDrCrGivenAccCode(account, year, dnm, localDefnsDir, defnsDir) + ")", "6007", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);
    }
    else
    {
      pageFrameUtils.drawTitle(out, false, plain, "", "", "", "", "", "", "GL Account: " + account + " - " + accountsUtils.getAccountDescriptionGivenAccCode(account, year, dnm, localDefnsDir, defnsDir) + " ("
                                                               + accountsUtils.getAccountCurrencyGivenAccCode(account, year, dnm, localDefnsDir, defnsDir) + " " + accountsUtils.getAccountDrCrGivenAccCode(account, year, dnm, localDefnsDir, defnsDir) + ")", "",
                                                               unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);
    }

    String creditorsAccount = accountsUtils.getTradeCreditorsAccCode(year, dnm, localDefnsDir, defnsDir);

    scoutln(out, bytesOut, "<form><table id=\"page\" width=100% border=0>");

    String tmpTable = unm + "_tmp";

    directoryUtils.createTmpTable(true, con, stmt, "Type char(1), Code char(20), Reference char(20), Description char(100), Total decimal(19,8), "
                                        + "BaseTotal decimal(19,8), Date date, Currency char(3)", "", tmpTable);
    
    String baseCurrency = accountsUtils.getBaseCurrency(con, stmt, rs, dnm, localDefnsDir, defnsDir);

    calc(con, stmt, stmt2, rs, rs2, p5, baseCurrency, year, creditorsAccount, dateFrom, dateTo, dnm, localDefnsDir, defnsDir, tmpTable);
        
    processTmpTable(con, stmt, rs, tmpTable, out, baseCurrency, p5, dateTo, dnm, localDefnsDir, defnsDir, bytesOut);
    
    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  public void calc(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String currency, String baseCurrency, String year,
                   String creditorsAccount, String dateFrom, String dateTo, String dnm, String localDefnsDir, String defnsDir, String tmpTable) throws Exception
  {    
    onVoucherLines(con, stmt, stmt2, rs, currency, creditorsAccount, dateFrom, dateTo, tmpTable);
    onRVoucherLines(con, stmt, stmt2, rs, currency, creditorsAccount, dateFrom, dateTo, tmpTable);

    onPurchaseInvoice(con, stmt, stmt2, rs, rs2, currency, dateFrom, dateTo, tmpTable);
    onPurchaseDebitNote(con, stmt, stmt2, rs, rs2, currency, dateFrom, dateTo, tmpTable);
    onPurchaseCreditNote(con, stmt, stmt2, rs, rs2, currency, dateFrom, dateTo, tmpTable);
    onPayment(con, stmt, stmt2, rs, rs2, currency, dateFrom, dateTo, tmpTable);

    if(currency.equals(baseCurrency))
      onPaymentGainLoss(con, stmt, stmt2, rs, rs2, currency, dateFrom, dateTo, tmpTable);
    
    onBatchCr(con, stmt2, currency, year, creditorsAccount, dateFrom, dateTo, tmpTable, dnm);
    onBatchDr(con, stmt2, currency, year, creditorsAccount, dateFrom, dateTo, tmpTable, dnm);
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void onVoucherLines(Connection con, Statement stmt, Statement stmt2, ResultSet rs, String currency, String creditorsAccount, String dateFrom, String dateTo,
                              String tmpTable) throws Exception
  {
    String s;
    if(currency.length() == 0)
      s = "";
    else s = " AND t1.Currency = '" + currency + "'";

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.VoucherCode, t1.CompanyName, t2.Amount2, t2.Amount, t1.Date, t1.Currency "
                         + "FROM voucherl AS t2 INNER JOIN voucher AS t1 ON t1.VoucherCode = t2.VoucherCode WHERE t2.AccountDr = '" + creditorsAccount
                         + "' AND t1.Status != 'C' AND t1.Date >= {d '" + dateFrom + "'} AND t1.Date <= {d '" + dateTo + "'}" + s);
    double amount, baseAmount;           
            
    while(rs.next())
    {    
      amount     = generalUtils.doubleFromStr(rs.getString(3));
      baseAmount = generalUtils.doubleFromStr(rs.getString(4));

      addToTmpTable(con, stmt2, "K", rs.getString(1), rs.getString(2), amount, baseAmount, rs.getString(5), rs.getString(6), "", tmpTable);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void onRVoucherLines(Connection con, Statement stmt, Statement stmt2, ResultSet rs, String currency, String creditorsAccount, String dateFrom, String dateTo,
                               String tmpTable) throws Exception
  {
    String s;
    if(currency.length() == 0)
      s = "";
    else s = " AND t1.Currency = '" + currency + "'";

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.VoucherCode, t1.CompanyName, t2.Amount2, t2.Amount, t1.Date, t1.Currency "
                         + "FROM rvoucherl AS t2 INNER JOIN rvoucher AS t1 ON t1.VoucherCode = t2.VoucherCode WHERE t2.AccountCr = '"
                         + creditorsAccount + "' AND t1.Status != 'C' AND t1.Date >= {d '" + dateFrom + "'} AND t1.Date <= {d '" + dateTo + "'}" + s);
    double amount, baseAmount;           
            
    while(rs.next())
    {    
      amount     = generalUtils.doubleFromStr(rs.getString(3));
      baseAmount = generalUtils.doubleFromStr(rs.getString(4));

      addToTmpTable(con, stmt2, "L", rs.getString(1), rs.getString(2), amount, baseAmount, rs.getString(5), rs.getString(6), "", tmpTable);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void onPurchaseInvoice(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String currency, String dateFrom, String dateTo,
                                 String tmpTable) throws Exception
  {
    String s;
    if(currency.length() == 0)
      s = "";
    else s = " AND Currency = '" + currency + "'";

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT InvoiceCode, CompanyCode, CompanyName, TotalTotal, BaseTotalTotal, Date, Currency "
                         + "FROM pinvoice WHERE Status != 'C' AND Date >= {d '" + dateFrom + "'} AND Date <= {d '" + dateTo + "'} " + s + " AND Settled != 'Y'");
    double amount, baseAmount;
    
    while(rs.next())
    {    
      if(existsSupplier(con, stmt2, rs2, rs.getString(2)))
      {
        amount     = generalUtils.doubleFromStr(rs.getString(4));
        baseAmount = generalUtils.doubleFromStr(rs.getString(5));
 
        if(baseAmount != 0.0)
          addToTmpTable(con, stmt2, "N", rs.getString(1), rs.getString(3) + " (" + rs.getString(2) + ")", amount, baseAmount, rs.getString(6), rs.getString(7), "", tmpTable);
      }
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void onPurchaseDebitNote(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String currency, String dateFrom, String dateTo,
                                   String tmpTable) throws Exception
  {
    String s;
    if(currency.length() == 0)
      s = "";
    else s = " AND Currency = '" + currency + "'";

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT PDNCode, CompanyCode, CompanyName, TotalTotal, BaseTotalTotal, Date, Currency "
                         + "FROM pdebit WHERE Status != 'C' AND Date >= {d '" + dateFrom + "'} AND Date <= {d '" + dateTo + "'}" + s);
    double amount, baseAmount;
    
    while(rs.next())
    {    
      if(existsSupplier(con, stmt2, rs2, rs.getString(2)))
      {
        amount     = generalUtils.doubleFromStr(rs.getString(4));
        baseAmount = generalUtils.doubleFromStr(rs.getString(5));

        if(baseAmount != 0.0)
          addToTmpTable(con, stmt2, "E", rs.getString(1), rs.getString(3), amount, baseAmount, rs.getString(6), rs.getString(7), "", tmpTable);
      }
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void onPurchaseCreditNote(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String currency, String dateFrom, String dateTo,
                                    String tmpTable) throws Exception
  {
    String s;
    if(currency.length() == 0)
      s = "";
    else s = " AND Currency = '" + currency + "'";

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT PCNCode, CompanyCode, CompanyName, TotalTotal, BaseTotalTotal, Date, Currency "
                         + "FROM pcredit WHERE Status != 'C' AND Date >= {d '" + dateFrom + "'} AND Date <= {d '" + dateTo + "'}" + s);
    double amount, baseAmount;
    
    while(rs.next())
    {    
      if(existsSupplier(con, stmt2, rs2, rs.getString(2)))
      {
        amount     = generalUtils.doubleFromStr(rs.getString(4));
        baseAmount = generalUtils.doubleFromStr(rs.getString(5));

        if(baseAmount != 0.0)
          addToTmpTable(con, stmt2, "D", rs.getString(1), rs.getString(3), amount, baseAmount, rs.getString(6), rs.getString(7), "", tmpTable);
      }
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void onPayment(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String currency, String dateFrom, String dateTo,
                         String tmpTable) throws Exception
  {
    String s;
    if(currency.length() == 0)
      s = "";
    else s = " AND Currency = '" + currency + "'";

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT PaymentCode, CompanyCode, CompanyName, TotalAmount, BaseTotalAmount, DatePaid, Currency, PaymentReference "
                         + "FROM payment WHERE Status != 'C' AND DatePaid >= {d '" + dateFrom + "'} AND DatePaid <= {d '" + dateTo + "'}" + s);
    
    double amount, baseAmount;
    
    while(rs.next())
    {    
      if(existsSupplier(con, stmt2, rs2, rs.getString(2)))
      {
        amount     = generalUtils.doubleFromStr(rs.getString(4));
        baseAmount = generalUtils.doubleFromStr(rs.getString(5));

        if(baseAmount != 0.0)
        {
          addToTmpTable(con, stmt2, "P", rs.getString(1), rs.getString(3) + " (" + rs.getString(2) + ")", amount, baseAmount, rs.getString(6), rs.getString(7), rs.getString(8), tmpTable);
        }
      }
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void onPaymentGainLoss(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String currency, String dateFrom, String dateTo,
                                 String tmpTable) throws Exception
  {
    String s;
    if(currency.length() == 0)
      s = "";
    else s = " AND t1.Currency = '" + currency + "'";

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t1.CompanyCode, t1.PaymentCode, t1.CompanyName, t1.Rate, t2.OriginalRate, t2.AmountPaid, t1.DatePaid, "
                         + "t1.Currency, t1.PaymentReference FROM paymentl AS t2 INNER JOIN payment AS t1 ON t1.PaymentCode = t2.PaymentCode "
                         + "WHERE t1.Status != 'C' AND t1.DatePaid >= {d '" + dateFrom + "'} AND t1.DatePaid <= {d '" + dateTo + "'}" + s + " ORDER BY t2.PaymentCode");
    double amount, rate, originalRate, diff, runningDiff = 0.0;
    String companyCode="", lastCompanyCode = "", paymentCode="", lastPaymentCode="", companyName="", datePaid="", currency2="", reference="";
    boolean first = true;
    
    while(rs.next())
    { 
      companyCode = rs.getString(1);
      paymentCode = rs.getString(2);
      
      if(! paymentCode.equals(lastPaymentCode))
      {
        if(! first)
        {
          if(existsSupplier(con, stmt2, rs2, lastCompanyCode))
          {
            if(runningDiff > 0) // exch loss
              addToTmpTable(con, stmt2, "Q", lastPaymentCode, companyName, 0, runningDiff, datePaid, currency2, reference, tmpTable);
            else
            if(runningDiff < 0) // exch gain
              addToTmpTable(con, stmt2, "S", lastPaymentCode, companyName, 0, -runningDiff, datePaid, currency2, reference, tmpTable);
          
            runningDiff = 0.0;
          }
        }
        else first = false;
        
        lastCompanyCode = companyCode;
        lastPaymentCode = paymentCode;
      }

      companyName  = rs.getString(3);
      rate         = generalUtils.doubleFromStr(rs.getString(4));
      originalRate = generalUtils.doubleFromStr(rs.getString(5));
      datePaid     = rs.getString(7);
      currency2     = rs.getString(8);
      reference    = rs.getString(9);
      
      if(rate != originalRate)
      {
        amount = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(6)), '2');
        //      100 * 3.1         100 * 3.06
        diff = (amount * rate) - (amount * originalRate);

        runningDiff += generalUtils.doubleDPs(diff, '2');
      }
    }      

    if(runningDiff != 0.0)
    {
      if(existsSupplier(con, stmt2, rs2, companyCode))
      {
        if(runningDiff > 0) // exch loss
          addToTmpTable(con, stmt2, "Q", paymentCode, companyName, 0, runningDiff, datePaid, currency2, reference, tmpTable);
        else
        if(runningDiff < 0) // exch gain
          addToTmpTable(con, stmt2, "S", paymentCode, companyName, 0, -runningDiff, datePaid, currency2, reference, tmpTable);
      }
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }
    
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void onBatchCr(Connection con, Statement stmt2, String currency, String year, String creditorsAccount, String dateFrom, String dateTo, String tmpTable, String dnm)
                         throws Exception
  {
    String s;
    if(currency.length() == 0)
      s = "";
    else s = " AND t2.Currency = '" + currency + "'";

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

      accRs = accStmt.executeQuery("SELECT t2.Code, t2.Remark, t2.Amount, t2.BaseAmount, t2.TransactionDate, t2.Currency, t2.AccCode FROM joubatchl AS t2 INNER JOIN joubatch AS t1 ON t1.Code = t2.Code "
                                 + "WHERE (t1.Type = 'C' OR ((t1.Type != 'C' AND t1.Type != 'D') AND t2.AccCode = '" + creditorsAccount + "')) AND t2.DrCr = 'C' AND t2.TransactionDate >= {d '" + dateFrom + "'} AND t2.TransactionDate <= {d '"
                                 + dateTo + "'}" + s);
      double amount, baseAmount;
      String desc;
            
      while(accRs.next())
      {    
        amount     = generalUtils.doubleFromStr(accRs.getString(3));
        baseAmount = generalUtils.doubleFromStr(accRs.getString(4));
        desc       = accRs.getString(2);
        if(desc.length() == 0)
          desc = accRs.getString(7);

        addToTmpTable(con, stmt2, "B", accRs.getString(1), desc, amount, baseAmount, accRs.getString(5), accRs.getString(6), "", tmpTable);
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
  private void onBatchDr(Connection con, Statement stmt2, String currency, String year, String creditorsAccount, String dateFrom, String dateTo, String tmpTable, String dnm)
                         throws Exception
  {
    String s;
    if(currency.length() == 0)
      s = "";
    else s = " AND t2.Currency = '" + currency + "'";

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

      accRs = accStmt.executeQuery("SELECT t2.Code, t2.Remark, t2.Amount, t2.BaseAmount, t2.TransactionDate, t2.Currency, t2.AccCode FROM joubatchl AS t2 INNER JOIN joubatch AS t1 ON t1.Code = t2.Code "
                                 + "WHERE (t1.Type = 'C' OR ((t1.Type != 'C' AND t1.Type != 'D') AND t2.AccCode = '" + creditorsAccount + "')) AND t2.DrCr = 'D' AND t2.TransactionDate >= {d '" + dateFrom + "'} AND t2.TransactionDate <= {d '"
                                 + dateTo + "'}" + s);
      double amount, baseAmount;
      String desc;
            
      while(accRs.next())
      {    
        amount     = generalUtils.doubleFromStr(accRs.getString(3));
        baseAmount = generalUtils.doubleFromStr(accRs.getString(4));
        desc       = accRs.getString(2);
        if(desc.length() == 0)
          desc = accRs.getString(7);

        addToTmpTable(con, stmt2, "C", accRs.getString(1), desc, amount, baseAmount, accRs.getString(5), accRs.getString(6), "", tmpTable);
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
  private void processTmpTable(Connection con, Statement stmt, ResultSet rs, String tmpTable, PrintWriter out, String baseCurrency, String reqdCurrency,
                               String dateTo, String dnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {

  }  

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void processTmpTableForListing(Connection con, Statement stmt, ResultSet rs, String tmpTable, String unm, String dnm,
                                        byte[] data, byte[] fldNames, double[] tm, double[] bm, double[] lm, double[] rm, double[] pageSizeWidth,
                                        double[] pageSizeLength, double[] currDown, double[] currAcross, short[] currFont, short[] currPage,
                                        short[] oPtr, short oBufLen, boolean[] lastOperationPF, String[] userFontName, String[] userFontStyle,
                                        String[] userFontItalic, String[] userFontSize, RandomAccessFile[] fhPPR, RandomAccessFile[] fhO,
                                        char[] fontDefType, char[] fontDefStyle, char[] fontDefItalic, int[] fontDefSize, double[] down2Coord,
                                        double[] across2Coord, byte[] oBuf, String localDefnsDir, String defnsDir) throws Exception
  {
    ReportGenDetails reportGenDetails = new ReportGenDetails();

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT * FROM " + tmpTable + " ORDER BY Date, Type, Code"); // ensuring the OB (in JB) come first

    String code, type, date, desc, currency, reference;
    double balance = 0.0, monthBalance = 0.0, amount;
    int month, thisMonth = 0;

    String drCr = "";
    double totalCRs = 0.0, totalDRs = 0.0;
    boolean gainOrLoss;

    while(rs.next())
    {
      type       = rs.getString(1);
      code       = rs.getString(2);
      reference  = rs.getString(3);
      desc       = rs.getString(4);
      amount     = generalUtils.doubleFromStr(generalUtils.doubleDPs(rs.getString(5), '2'));
      date       = rs.getString(7);
      currency   = rs.getString(8);

      month = generalUtils.getMonth(generalUtils.encodeFromYYYYMMDD(date));
      if(month != thisMonth)
      {
        reportGenDetails.processSection("BL4", data, fldNames, (short)14, dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0], pageSizeWidth[0],
                             pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName, userFontStyle,
                             userFontItalic, userFontSize, fhPPR[0], fhO[0], fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord,
                             across2Coord, oBuf, null);
        thisMonth = month;
        monthBalance = 0.0;
      }

       gainOrLoss = false;

      if(type.charAt(0) == 'P')
      {
        type = "Payment";
        drCr = "Dr";
        totalDRs += amount;
        balance += amount;
        monthBalance += amount;
      }
      else
      if(type.charAt(0) == 'S')
      {
        type = "Payment";
        drCr = "Dr";
        totalDRs += amount;
        balance += amount;
        monthBalance += amount;
        desc += " (Gain)";
        gainOrLoss = true;
      }
      else
      if(type.charAt(0) == 'Q')
      {
        type = "Payment";
        drCr = "Cr";
        totalCRs += amount;
        balance -= amount;
        monthBalance -= amount;
        desc += " (Loss)";
        gainOrLoss = true;
      }
      else
      if(type.charAt(0) == 'B')
      {
        type = "Journal Batch";
        drCr = "Cr";
        totalCRs += amount;
        balance -= amount;
        monthBalance -= amount;
      }
      else
      if(type.charAt(0) == 'C')
      {
        type = "Journal Batch";
        drCr = "Dr";
        totalDRs += amount;
        balance += amount;
        monthBalance += amount;
      }
      else
      if(type.charAt(0) == 'N')
      {
        type = "Purchase Invoice";
        drCr = "Cr";
        totalCRs += amount;
        balance -= amount;
        monthBalance -= amount;
      }
      else
      if(type.charAt(0) == 'E')
      {
        type = "Purchase Debit Note";
        drCr = "Cr";
        totalCRs += amount;
        balance -= amount;
        monthBalance -= amount;
      }
      else
      if(type.charAt(0) == 'D')
      {
        type = "Purchase Credit Note";
        drCr = "Dr";
        totalDRs += amount;
        balance += amount;
        monthBalance += amount;
      }
      else
      if(type.charAt(0) == 'K')
      {
        type = "Payment Voucher";
        drCr = "Dr";
        totalDRs += amount;
        balance += amount;
        monthBalance += amount;
      }
      else
      if(type.charAt(0) == 'L')
      {
        type = "Receipt Voucher";
        drCr = "Cr";
        totalCRs += amount;
        balance -= amount;
        monthBalance -= amount;
      }

      generalUtils.repAlpha(data, 2000, (short)1, code);
      generalUtils.repAlpha(data, 2000, (short)2, type);
      generalUtils.repAlpha(data, 2000, (short)3, generalUtils.convertFromYYYYMMDD(date));

      generalUtils.repAlpha(data, 2000, (short)4, reference);
      generalUtils.repAlpha(data, 2000, (short)6, currency);

      generalUtils.repAlpha(data, 2000, (short)5, drCr);

      if(gainOrLoss)
        generalUtils.repAlpha(data, 2000, (short)7, "-");
      else generalUtils.repAlpha(data, 2000, (short)7,  generalUtils.formatNumeric(amount, '2'));

      generalUtils.repAlpha(data, 2000, (short)9,  generalUtils.formatNumeric(monthBalance, '2'));
      generalUtils.repAlpha(data, 2000, (short)10, generalUtils.formatNumeric(balance, '2'));
      generalUtils.repAlpha(data, 2000, (short)11, desc);

      reportGenDetails.processSection("BL2", data, fldNames, (short)14, dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0], pageSizeWidth[0],
                           pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName, userFontStyle,
                           userFontItalic, userFontSize, fhPPR[0], fhO[0], fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord,
                           across2Coord, oBuf, null);
    }

    generalUtils.repAlpha(data, 2000, (short)12, "Debits: "  + generalUtils.formatNumeric(totalDRs, '2'));
    generalUtils.repAlpha(data, 2000, (short)13, "Credits: " + generalUtils.formatNumeric(totalCRs, '2'));

    reportGenDetails.processSection("BL3", data, fldNames, (short)14, dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0], pageSizeWidth[0],
                         pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName, userFontStyle,
                         userFontItalic, userFontSize, fhPPR[0], fhO[0], fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord,
                         across2Coord, oBuf, null);

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void addToTmpTable(Connection con, Statement stmt, String type, String code, String desc, double amount, double baseAmount, String date,
                             String currency, String reference, String tmpTable) throws Exception
  {
    stmt = con.createStatement();
  
    String q = "INSERT INTO " + tmpTable + " ( Type, Code, Reference, Description, Total, BaseTotal, Date, Currency ) "
             + "VALUES ( '" + type + "', '" + code + "', '" + generalUtils.sanitiseForSQL(reference) + "', '" + generalUtils.sanitiseForSQL(desc) + "', '" + amount
             + "', '" + baseAmount + "', {d '" + date + "'}, '" + currency + "' )";

    stmt.executeUpdate(q);
  
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean existsSupplier(Connection con, Statement stmt, ResultSet rs, String companyCode) throws Exception
  {
    int numRecs = 0;
        
    try
    {
      if(companyCode.length() == 0)
        return false;

      if(companyCode.equals("-"))
        return false;

      stmt = con.createStatement();
    
      rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM supplier WHERE SupplierCode = '" + companyCode + "'");
      rs.next();
      numRecs = rs.getInt("rowcount") ;
    }
    catch(Exception e) { }
      
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    if(numRecs == 1)
      return true;
    
    return false;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
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

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void processTmpTableForExport(Connection con, Statement stmt, ResultSet rs, String tmpTable, String account, RandomAccessFile fh) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT * FROM " + tmpTable + " ORDER BY Date, Type, Code"); // ensuring the OB (in JB) come first

    String code, type, date, desc, currency, reference;
    double balance = 0.0, monthBalance = 0.0, amount;
    int month, thisMonth = 0;

    String drCr = "";
    double totalCRs = 0.0, totalDRs = 0.0;
    boolean gainOrLoss;

    while(rs.next())
    {
      type       = rs.getString(1);
      code       = rs.getString(2);
      reference  = rs.getString(3);
      desc       = rs.getString(4);
      amount     = generalUtils.doubleFromStr(generalUtils.doubleDPs(rs.getString(5), '2'));
      date       = rs.getString(7);
      currency   = rs.getString(8);

      month = generalUtils.getMonth(generalUtils.encodeFromYYYYMMDD(date));
      if(month != thisMonth)
      {
        thisMonth = month;
        monthBalance = 0.0;
      }

       gainOrLoss = false;

      if(type.charAt(0) == 'P')
      {
        type = "Payment";
        drCr = "Dr";
        totalDRs += amount;
        balance += amount;
        monthBalance += amount;
      }
      else
      if(type.charAt(0) == 'S')
      {
        type = "Payment";
        drCr = "Dr";
        totalDRs += amount;
        balance += amount;
        monthBalance += amount;
        desc += " (Gain)";
        gainOrLoss = true;
      }
      else
      if(type.charAt(0) == 'Q')
      {
        type = "Payment";
        drCr = "Cr";
        totalCRs += amount;
        balance -= amount;
        monthBalance -= amount;
        desc += " (Loss)";
        gainOrLoss = true;
      }
      else
      if(type.charAt(0) == 'B')
      {
        type = "Journal Batch";
        drCr = "Cr";
        totalCRs += amount;
        balance -= amount;
        monthBalance -= amount;
      }
      else
      if(type.charAt(0) == 'C')
      {
        type = "Journal Batch";
        drCr = "Dr";
        totalDRs += amount;
        balance += amount;
        monthBalance += amount;
      }
      else
      if(type.charAt(0) == 'N')
      {
        type = "Purchase Invoice";
        drCr = "Cr";
        totalCRs += amount;
        balance -= amount;
        monthBalance -= amount;
      }
      else
      if(type.charAt(0) == 'E')
      {
        type = "Purchase Debit Note";
        drCr = "Cr";
        totalCRs += amount;
        balance -= amount;
        monthBalance -= amount;
      }
      else
      if(type.charAt(0) == 'D')
      {
        type = "Purchase Credit Note";
        drCr = "Dr";
        totalDRs += amount;
        balance += amount;
        monthBalance += amount;
      }
      else
      if(type.charAt(0) == 'K')
      {
        type = "Payment Voucher";
        drCr = "Dr";
        totalDRs += amount;
        balance += amount;
        monthBalance += amount;
      }
      else
      if(type.charAt(0) == 'L')
      {
        type = "Receipt Voucher";
        drCr = "Cr";
        totalCRs += amount;
        balance -= amount;
        monthBalance -= amount;
      }

      writeEntry(fh, account,      true);
      writeEntry(fh, code,         true);
      writeEntry(fh, type,         true);
      writeEntry(fh, date,         true);
      writeEntry(fh, "",           true);
      writeEntry(fh, drCr,         true);
      writeEntry(fh, currency,     true);
      if(gainOrLoss)
        writeEntry(fh, amount,     true);
      else writeEntry(fh, amount,  true);
      writeEntry(fh, "",           true);
      writeEntry(fh, monthBalance, true);
      writeEntry(fh, balance,      true);
      writeEntry(fh, desc,         true);
      writeEntry(fh, totalDRs,     true);
      writeEntry(fh, totalCRs,     false);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void writeEntry(RandomAccessFile fh, double d, boolean comma) throws Exception
  {
    writeEntry(fh, generalUtils.doubleToStr(d), comma);
  }
  private void writeEntry(RandomAccessFile fh, String s, boolean comma) throws Exception
  {
    fh.writeBytes("\"" + generalUtils.sanitise3(s) + "\"");
    if(comma)
      fh.writeBytes(",");
    else fh.writeBytes("\n");
  }

}
