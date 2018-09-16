// =======================================================================================================================================================================================================
// System: ZaraStar: Accounts: GL Debtors (Individual)
// Module: AccountsGLDebtorsIndividualExecute.java
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
import java.net.*;

public class AccountsGLDebtorsIndividualExecute extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  AccountsUtils accountsUtils = new AccountsUtils();
  Customer customer = new Customer();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="", p3="", p4="", p5="", p6="";

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
      p1  = req.getParameter("p1"); // companyCode
      p2  = req.getParameter("p2"); // dateFrom
      p3  = req.getParameter("p3"); // dateTo
      p4  = req.getParameter("p4"); // plain or not
      p5  = req.getParameter("p5"); // companyName
      p6  = req.getParameter("p6"); // currency

      if(p4 == null) p4 = "";
      if(p5 == null) p5 = "";
      if(p6 == null) p6 = "";

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, p3, p4, p5, p6, bytesOut);
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

      System.out.println("6029a: " + e);
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "AccountsGLDebtorsIndividualExecute", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6029, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1, String p2, String p3, String p4, String p5, String p6, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String workingDir       = directoryUtils.getUserDir('W', dnm, unm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null, stmt2 = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 6029, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "AccountsGLDebtorsIndividualExecute", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6029, bytesOut[0], 0, "ACC:");
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "AccountsGLDebtorsIndividualExecute", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6029, bytesOut[0], 0, "SID:");
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    boolean plain = false;
    if(p4.equals("P"))
      plain = true;

    if(p5.length() == 0)
      p5 = customer.getCompanyNameGivenCode(con, stmt, rs, p1);

    if(p6.length() == 0)
      p6 = customer.getCompanyCurrencyGivenCode(con, stmt, rs, p1);

    set(con, stmt, stmt2, rs, out, req, p1, p2, p3, plain, p5, p6, unm, sid, uty, men, den, dnm, bnm, workingDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 6029, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con  != null) con.close();
    if(out != null) out.flush();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, Statement stmt2, ResultSet rs, PrintWriter out, HttpServletRequest req, String companyCode, String dateFrom, String dateTo, boolean plain, String companyName, String currency, String unm,
                   String sid, String uty, String men, String den, String dnm, String bnm, String workingDir, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>GL Account: " + companyCode + "</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4205, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewReceipt(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/ReceiptPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }
  
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4067, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewInv(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/SalesInvoicePage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }
  
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4111, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewDN(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/SalesDebitNotePage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }
  
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4101, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewCN(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/SalesCreditNotePage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }
  
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 6023, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewJB(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/AccountsBatchEntries?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+p1;}");
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
    {
      scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"print\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "generalPlain.css\">");
    }
    else
    {
      scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");
    }
    
    int[] hmenuCount = new int[1];
 
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "6029", "", true, plain, companyCode, dateFrom, dateTo, companyName, currency, "AccountsGLDebtorsIndividualExecute", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount, bytesOut);

    String year = generalUtils.getFromDefnFile(unm, "accounts.dfn", workingDir, "");

    if(! plain)
    {
      pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "GL Account: " + companyCode + " - " + companyName + " (" + currency + ")", "6029", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);
    }
    else
    {
      pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "GL Account: " + companyCode + " - " + companyName + " (" + currency + ")", "", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);
    }

    scoutln(out, bytesOut, "<form><table id=\"page\" width=100% border=0>");

    String tmpTable = unm + "_tmp";

    directoryUtils.createTmpTable(true, con, stmt, "Type char(1), Code char(20), Description char(100), Total decimal(19,8), Date date, Currency char(3), BaseTotal decimal(19, 8)", "", tmpTable);

    String baseCurrency = accountsUtils.getBaseCurrency(con, stmt, rs, dnm, localDefnsDir, defnsDir);

    calc(con, stmt, stmt2, rs, companyCode, year, dateFrom, dateTo, baseCurrency, dnm, tmpTable);

    processTmpTable(con, stmt, rs, tmpTable, out, currency, baseCurrency, dnm, localDefnsDir, defnsDir, bytesOut);

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    getOutstandingForACustomer(out, companyCode, dateTo, unm, sid, uty, men, den, dnm, bnm, bytesOut);

    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir));
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void calc(Connection con, Statement stmt, Statement stmt2, ResultSet rs, String companyCode, String year, String dateFrom, String dateTo, String baseCurrency, String dnm, String tmpTable) throws Exception
  {
    onInvoice(con, stmt, stmt2, rs, companyCode, dateFrom, dateTo, tmpTable);
    onDebitNote(con, stmt, stmt2, rs, companyCode, dateFrom, dateTo, tmpTable);
    onSalesCreditNote(con, stmt, stmt2, rs, companyCode, dateFrom, dateTo, tmpTable);
    onReceipt(con, stmt, stmt2, rs, companyCode, dateFrom, dateTo, tmpTable);
    forReceiptExchangeGainLoss(con, stmt, rs, companyCode, dateFrom, dateTo, baseCurrency, tmpTable);
    onBatchCr(con, stmt2, year, companyCode, dateFrom, dateTo, tmpTable, dnm);
    onBatchDr(con, stmt2, year, companyCode, dateFrom, dateTo, tmpTable, dnm);
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void onInvoice(Connection con, Statement stmt, Statement stmt2, ResultSet rs, String companyCode, String dateFrom, String dateTo, String tmpTable) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT InvoiceCode, CompanyName, TotalTotal, Date, Currency, BaseTotalTotal FROM invoice WHERE CompanyCode = '" + companyCode + "' AND Status != 'C' AND Date >= {d '" + dateFrom + "'} AND Date <= {d '" + dateTo
                         + "'}     AND Settled != 'Y'");
    double amount, baseAmount;
            
    while(rs.next())
    {    
      amount     = generalUtils.doubleFromStr(rs.getString(3));
      baseAmount = generalUtils.doubleFromStr(rs.getString(6));

      addToTmpTable(con, stmt2, "N", rs.getString(1), rs.getString(2), amount, baseAmount, rs.getString(4), rs.getString(5), tmpTable);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void onDebitNote(Connection con, Statement stmt, Statement stmt2, ResultSet rs, String companyCode, String dateFrom, String dateTo, String tmpTable) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT DNCode, CompanyName, TotalTotal, Date, Currency, BaseTotalTotal FROM debit WHERE CompanyCode = '" + companyCode + "' AND Status != 'C' AND Date >= {d '" + dateFrom + "'} AND Date <= {d '" + dateTo + "'}    AND Settled != 'Y'");
    double amount, baseAmount;
            
    while(rs.next())
    {    
      amount     = generalUtils.doubleFromStr(rs.getString(3));
      baseAmount = generalUtils.doubleFromStr(rs.getString(6));

      addToTmpTable(con, stmt2, "E", rs.getString(1), rs.getString(2), amount, baseAmount, rs.getString(4), rs.getString(5), tmpTable);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void onSalesCreditNote(Connection con, Statement stmt, Statement stmt2, ResultSet rs, String companyCode, String dateFrom, String dateTo, String tmpTable) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT CNCode, CompanyName, TotalTotal, Date, Currency, BaseTotalTotal FROM credit WHERE CompanyCode = '" + companyCode + "' AND Status != 'C' AND Date >= {d '" + dateFrom + "'} AND Date <= {d '" + dateTo + "'}");
    double amount, baseAmount;
            
    while(rs.next())
    {    
      amount     = generalUtils.doubleFromStr(rs.getString(3));
      baseAmount = generalUtils.doubleFromStr(rs.getString(6));

      addToTmpTable(con, stmt2, "D", rs.getString(1), rs.getString(2), amount, baseAmount, rs.getString(4), rs.getString(5), tmpTable);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void onReceipt(Connection con, Statement stmt, Statement stmt2, ResultSet rs, String companyCode, String dateFrom, String dateTo, String tmpTable) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT ReceiptCode, CompanyName, TotalAmount, DateReceived, Currency, BaseTotalAmount FROM receipt WHERE CompanyCode = '" + companyCode + "' AND Status != 'C' AND DateReceived >= {d '" + dateFrom
                         + "'} AND DateReceived <= {d '" + dateTo + "'}");
    double amount, baseAmount;
            
    while(rs.next())
    {    
      amount     = generalUtils.doubleFromStr(rs.getString(3));
      baseAmount = generalUtils.doubleFromStr(rs.getString(6));

      addToTmpTable(con, stmt2, "R", rs.getString(1), rs.getString(2), amount, baseAmount, rs.getString(4), rs.getString(5), tmpTable);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void forReceiptExchangeGainLoss(Connection con, Statement stmt, ResultSet rs, String companyCode, String dateFrom, String dateTo, String baseCurrency, String tmpTable) throws Exception
  {
    Statement stmt2 = null;

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t1.Rate, t2.OriginalRate, t2.AmountReceived, t1.DateReceived, t2.ReceiptCode, t1.Currency, t1.CompanyName FROM receiptl AS t2 INNER JOIN receipt AS t1 ON t1.ReceiptCode = t2.ReceiptCode "
                          +"WHERE CompanyCode = '" + companyCode + "' AND t1.Status != 'C' AND t1.DateReceived >= {d '" + dateFrom + "'} AND t1.DateReceived <= {d '" + dateTo + "'} ORDER BY t2.ReceiptCode");

    String receiptCode, lastReceiptCode = "", companyName= "", dateReceived = "";
    double amount, rate, originalRate, diff, runningDiff = 0.0;
    boolean first = true;

    while(rs.next())
    {
      receiptCode  = rs.getString(5);

      if(! receiptCode.equals(lastReceiptCode))
      {
        if(! first)
        {
          if(runningDiff < 0) // exch loss
            addToTmpTable(con, stmt2, "L", lastReceiptCode, companyName, 0, (runningDiff * -1), dateReceived, baseCurrency, tmpTable);

          if(runningDiff > 0) // exch gain
            addToTmpTable(con, stmt2, "G", lastReceiptCode, companyName, 0, runningDiff, dateReceived, baseCurrency, tmpTable);

          runningDiff = 0.0;
        }
        else first = false;

        lastReceiptCode = receiptCode;
      }

      rate         = generalUtils.doubleFromStr(rs.getString(1));
      originalRate = generalUtils.doubleFromStr(rs.getString(2));
      companyName  = rs.getString(7);
      dateReceived = rs.getString(4);

      if(rate != originalRate)
      {
        amount = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(3)), '2');

        //      100 * 1.53        100 * 1.54
        diff = (amount * rate) - (amount * originalRate);
        runningDiff += generalUtils.doubleDPs(diff, '2');
      }
    }

    if(runningDiff != 0.0)
    {
      if(runningDiff < 0) // exch loss
        addToTmpTable(con, stmt2, "L", lastReceiptCode, companyName, 0, (runningDiff * -1), dateReceived, baseCurrency, tmpTable);
      else
      if(runningDiff > 0) // exch gain
        addToTmpTable(con, stmt2, "G", lastReceiptCode, companyName, 0, runningDiff, dateReceived, baseCurrency, tmpTable);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void onBatchCr(Connection con, Statement stmt2, String year, String companyCode, String dateFrom, String dateTo, String tmpTable, String dnm) throws Exception
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

      accRs = accStmt.executeQuery("SELECT t2.Code, t2.Remark, t2.Amount, t2.TransactionDate, t2.Currency, t2.BaseAmount "
                                 + "FROM joubatchl AS t2 INNER JOIN joubatch AS t1 ON t1.Code = t2.Code WHERE t1.Type = 'D' AND t2.AccCode = '"
                                 + companyCode + "' AND t2.DrCr = 'C' AND t2.TransactionDate >= {d '" + dateFrom + "'} AND t2.TransactionDate <= {d '"
                                 + dateTo + "'}");
      double amount, baseAmount;
            
      while(accRs.next())
      {    
        amount     = generalUtils.doubleFromStr(accRs.getString(3));
        baseAmount = generalUtils.doubleFromStr(accRs.getString(6));

        addToTmpTable(con, stmt2, "B", accRs.getString(1), accRs.getString(2), amount, baseAmount, accRs.getString(4), accRs.getString(5), tmpTable);
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
   
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void onBatchDr(Connection con, Statement stmt2, String year, String companyCode, String dateFrom, String dateTo, String tmpTable, String dnm) throws Exception
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

      accRs = accStmt.executeQuery("SELECT t2.Code, t2.Remark, t2.Amount, t2.TransactionDate, t2.Currency, t2.BaseAmount "
                                 + "FROM joubatchl AS t2 INNER JOIN joubatch AS t1 ON t1.Code = t2.Code WHERE t1.Type = 'D' AND t2.AccCode = '"
                                 + companyCode + "' AND t2.DrCr = 'D' AND t2.TransactionDate >= {d '" + dateFrom + "'} AND t2.TransactionDate <= {d '"
                                 + dateTo + "'}");
      double amount, baseAmount;
            
      while(accRs.next())
      {    
        amount     = generalUtils.doubleFromStr(accRs.getString(3));
        baseAmount = generalUtils.doubleFromStr(accRs.getString(6));

        addToTmpTable(con, stmt2, "C", accRs.getString(1), accRs.getString(2), amount, baseAmount, accRs.getString(4), accRs.getString(5), tmpTable);
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

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void processTmpTable(Connection con, Statement stmt, ResultSet rs, String tmpTable, PrintWriter out, String accountCurrency, String baseCurrency, String dnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT * FROM " + tmpTable + " ORDER BY Date, Type"); // ensuring the OB (in JB) come first

    String code, type, desc, date, currency, cssFormat="";
    double amount, balance = 0.0, baseAmount;

    scout(out, bytesOut, "<tr id='pageColumn'>");
    scout(out, bytesOut, "<td><p>Document Code</td>");
    scout(out, bytesOut, "<td><p>Document Type</td>");
    scout(out, bytesOut, "<td><p>Date</td>");
    scout(out, bytesOut, "<td><p>Currency</td>");
    scout(out, bytesOut, "<td><p>Dr/Cr</td>");
    scout(out, bytesOut, "<td align=right><p>Amount</td>");
    scout(out, bytesOut, "<td align=right><p>" + baseCurrency + " Amount</td>");
    scout(out, bytesOut, "<td align=right><p>Balance</td>");
    scout(out, bytesOut, "<td align=right><p>" + baseCurrency + " at Rate</td>");
    scoutln(out, bytesOut, "<td><p>Description</td></tr>");

    boolean sameCurrency = true;
    String drCr = "";
    byte[] actualDate = new byte[20];
    double rate, totalIssueDRs = 0.0, totalIssueCRs = 0.0, totalIssueDRsBase = 0.0, totalIssueCRsBase = 0.0;

    while(rs.next())
    {
      type       = rs.getString(1);
      code       = rs.getString(2);
      desc       = rs.getString(3);
      amount     = generalUtils.doubleFromStr(generalUtils.doubleDPs(rs.getString(4), '2'));
      date       = rs.getString(5);
      currency   = rs.getString(6);
      baseAmount = generalUtils.doubleFromStr(generalUtils.doubleDPs(rs.getString(7), '2'));

      if(cssFormat.equals("line1")) cssFormat = "line2"; else cssFormat = "line1";

      scout(out, bytesOut, "<tr id=\"" + cssFormat + "\"><td><p><a href=\"javascript:");
      if(type.charAt(0) == 'R')
      {
        scout(out, bytesOut, "viewReceipt('");
        type = "Receipt";
        drCr = "Cr";
        balance -= amount;
        totalIssueCRs += amount;
        totalIssueCRsBase += baseAmount;
      }
      else
      if(type.charAt(0) == 'B')
      {
        scout(out, bytesOut, "viewJB('");
        type = "Journal Batch";
        drCr = "Cr";
        balance -= amount;
        totalIssueCRs += amount;
        totalIssueCRsBase += baseAmount;
      }
      else
      if(type.charAt(0) == 'C')
      {
        scout(out, bytesOut, "viewJB('");
        type = "Journal Batch";
        drCr = "Dr";
        balance += amount;
        totalIssueDRs += amount;
        totalIssueDRsBase += baseAmount;
      }
      else
      if(type.charAt(0) == 'N')
      {
        scout(out, bytesOut, "viewInv('");
        type = "Sales Invoice";
        drCr = "Dr";
        balance += amount;
        totalIssueDRs += amount;
        totalIssueDRsBase += baseAmount;
      }
      else
      if(type.charAt(0) == 'E')
      {
        scout(out, bytesOut, "viewDN('");
        type = "Debit Note";
        drCr = "Dr";
        balance += amount;
        totalIssueDRs += amount;
        totalIssueDRsBase += baseAmount;
      }
      else
      if(type.charAt(0) == 'D')
      {
        scout(out, bytesOut, "viewCN('");
        type = "Credit Note";
        drCr = "Cr";
        balance -= amount;
        totalIssueCRs += amount;
        totalIssueCRsBase += baseAmount;
      }
      else
      if(type.charAt(0) == 'G')
      {
        scout(out, bytesOut, "viewReceipt('");
        type = "Receipt";
        drCr = "Dr";
        balance += amount;
        totalIssueDRs += amount;
        totalIssueDRsBase += baseAmount;
        desc += " (Exchange Gain)";
      }
      else
      if(type.charAt(0) == 'L')
      {
        scout(out, bytesOut, "viewReceipt('");
        type = "Receipt";
        drCr = "Cr";
        balance -= amount;
        totalIssueCRs += amount;
        totalIssueCRsBase += baseAmount;
        desc += " (Exchange Loss)";
      }
      scoutln(out, bytesOut, code + "')\">" + code + "</a></td>");

      scoutln(out, bytesOut, "<td nowrap><p>" + type + "</td>");
      scoutln(out, bytesOut, "<td nowrap><p>" + generalUtils.convertFromYYYYMMDD(date) + "</td>");

      scoutln(out, bytesOut, "<td nowrap><p>" + currency + "</td>");

      if(drCr.equals("Dr"))
        scoutln(out, bytesOut, "<td nowrap><p>" + drCr + "</td>");
      else scoutln(out, bytesOut, "<td nowrap align=right><p>" + drCr + "</td>");

      scoutln(out, bytesOut, "<td nowrap align=right><p>" + generalUtils.formatNumeric(amount, '2') + "</td>");
      scoutln(out, bytesOut, "<td nowrap align=right><p>" + generalUtils.formatNumeric(baseAmount, '2') + "</td>");
      scoutln(out, bytesOut, "<td nowrap align=right><p>" + generalUtils.formatNumeric(balance, '2') + "</td>");

      rate = accountsUtils.getApplicableRate(con, stmt, rs, accountCurrency, generalUtils.convertFromYYYYMMDD(date), actualDate, dnm, localDefnsDir, defnsDir);
      scoutln(out, bytesOut, "<td nowrap align=right><p>" + generalUtils.formatNumeric((balance * rate), '2') + " (" + rate + ")</td>");

      scoutln(out, bytesOut, "<td nowrap><p>" + desc + "</td></tr>");
    }

    if(cssFormat.equals("line1")) cssFormat = "line2"; else cssFormat = "line1";

    scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\"><td nowrap colspan=3><p>Debits (Issue Currency): " + generalUtils.formatNumeric(totalIssueDRs, '2') + "</td></tr>");

    scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\"><td nowrap colspan=3><p>Credits (Issue Currency): " + generalUtils.formatNumeric(totalIssueCRs, '2') + "</td></tr>");

    scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\"><td nowrap colspan=3><p>Difference (Issue Currency): " + generalUtils.formatNumeric((totalIssueDRs - totalIssueCRs), '2') + "</td></tr>");

    if(cssFormat.equals("line1")) cssFormat = "line2"; else cssFormat = "line1";

    scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\"><td nowrap colspan=3><p>Debits (Base Currency): " + generalUtils.formatNumeric(totalIssueDRsBase, '2') + "</td></tr>");

    scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\"><td nowrap colspan=3><p>Credits (Base Currency): " + generalUtils.formatNumeric(totalIssueCRsBase, '2') + "</td></tr>");

    scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\"><td nowrap colspan=3><p>Difference (Base Currency): " + generalUtils.formatNumeric((totalIssueDRsBase - totalIssueCRsBase), '2') + "</td></tr>");

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void addToTmpTable(Connection con, Statement stmt, String type, String code, String desc, double amount, double baseAmount, String date, String currency, String tmpTable) throws Exception
  {
    stmt = con.createStatement();
   
    String q = "INSERT INTO " + tmpTable + " ( Type, Code, Description, Total, Date, Currency, BaseTotal ) VALUES ( '" + type + "', '" + code + "', '" + generalUtils.sanitiseForSQL(desc) + "', '" + amount + "', {d '" + date + "'}, '" + currency
             + "','" + baseAmount + "' )";

    stmt.executeUpdate(q);
  
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
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
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void getOutstandingForACustomer(PrintWriter out, String customerCode, String dateTo, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    URL url = new URL("http://" + men + "/central/servlet/CustomerPeriods?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=" + customerCode + "&p2=" + dateTo + "&bnm=" + bnm);

    URLConnection uc = url.openConnection();
    uc.setDoInput(true);
    uc.setUseCaches(false);
    uc.setDefaultUseCaches(false);

    uc.setRequestProperty("Content-Type", "application/octet-stream");

    BufferedReader di = new BufferedReader(new InputStreamReader(uc.getInputStream()));

    String values = di.readLine();
    di.close();
    
    int valuesLen = values.length();
    String total="", currency="";
    int x=0;
    while(x < valuesLen && x < 3) // just-in-case
      currency += values.charAt(x++);
    x=3;
    while(x < valuesLen && values.charAt(x) != ' ') // just-in-case
      total += values.charAt(x++);
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=4><p><b>Total from debtors ageing: " + currency + " " + generalUtils.doubleDPs(total, '2') + "</p></td></tr>"); 
  }
  
}
