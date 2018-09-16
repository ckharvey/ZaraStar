// ===================================================================================================================================================
// System: ZaraStar: Accounts: create bank reconciliation page
// Module: BankReconciliationPage.java
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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class BankReconciliationPage extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  AccountsUtils accountsUtils = new AccountsUtils();
  DrawingUtils drawingUtils = new DrawingUtils();

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
      p1  = req.getParameter("p1"); // bankAccount
 
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
 
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "BankReconciliationPage", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6002, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm,
                    String bnm, String p1, int[] bytesOut) throws Exception
  {
    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String workingDir    = directoryUtils.getUserDir('W', dnm, unm);

    long startTime = new java.util.Date().getTime();

    String uName = directoryUtils.getMySQLUserName();
    String pWord = directoryUtils.getMySQLPassWord();
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 6002, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "BankReconciliationPage", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6002, bytesOut[0], 0, "ACC:" + p1);
      
    if(con  != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "BankReconciliationPage", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6002, bytesOut[0], 0, "SID:" + p1);
      
    if(con  != null) con.close();
      if(out != null) out.flush();
      return;
    }

    double[] receiptTotal                = new double[1];
    double[] receiptPreviousTotal        = new double[1];
    double[] receiptVoucherTotal         = new double[1];
    double[] receiptVoucherPreviousTotal = new double[1];
    double[] paymentTotal                = new double[1];
    double[] paymentPreviousTotal        = new double[1];
    double[] paymentVoucherTotal         = new double[1];
    double[] paymentVoucherPreviousTotal = new double[1];
    double[] iatTotal                    = new double[1];
    double[] iatPreviousTotal            = new double[1];

    set(con, stmt, rs, out, req, unm, sid, uty, men, den, dnm, bnm, p1, workingDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 6002, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
       
   if(con  != null) con.close();
    if(out != null) out.flush();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm,
                  String bnm, String account, String workingDir, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    writeHead(con, stmt, rs, out, req, unm, sid, uty, men, den, dnm, bnm, account, workingDir, localDefnsDir, defnsDir, bytesOut);

    detTransactionsNotReconciled(out, dnm, true, account, localDefnsDir, defnsDir, bytesOut);

    scoutln(out, bytesOut, "</table>");
    
    scoutln(out, bytesOut, "<p><table border=0 width=100%");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    
    scoutln(out, bytesOut, "<tr><td colspan=2><p><a href=\"javascript:reconcile()\">Reconcile</a></td></tr>");

    scoutln(out, bytesOut, "</table></form>");     scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void writeHead(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm,
                         String bnm, String account, String workingDir, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Bank Reconciliation</title>");
    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    scoutln(out, bytesOut, "function change(docCode,sourceDocumentType){");
    scoutln(out, bytesOut, "switch(sourceDocumentType){case 'P':changePayment(docCode);break;");
    scoutln(out, bytesOut, "case 'V':changeVoucher(docCode);break;case 'R':changeReceipt(docCode);break;");
    scoutln(out, bytesOut, "case 'O':changeRVoucher(docCode);break;");
    scoutln(out, bytesOut, "default:changeIAT(docCode);break;}}");

    scoutln(out, bytesOut, "function changePayment(code){var code2=sanitise(code);");
    scoutln(out, bytesOut, "this.location.href=\"http://" + men + "/central/servlet/PaymentPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty
                           + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p1=\"+code2;}");

    scoutln(out, bytesOut, "function changeVoucher(code){var code2=sanitise(code);");
    scoutln(out, bytesOut, "this.location.href=\"http://" + men + "/central/servlet/PaymentVoucherPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty
                           + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p1=\"+code2;}");

    scoutln(out, bytesOut, "function changeReceipt(code){var code2=sanitise(code);");
    scoutln(out, bytesOut, "this.location.href=\"http://" + men + "/central/servlet/ReceiptPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty
                           + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p1=\"+code2;}");

    scoutln(out, bytesOut, "function changeRVoucher(code){var code2=sanitise(code);");
    scoutln(out, bytesOut, "this.location.href=\"http://" + men + "/central/servlet/ReceiptVoucherPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty
                           + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p1=\"+code2;}");

    scoutln(out, bytesOut, "function changeIAT(code){var code2=sanitise(code);");
    scoutln(out, bytesOut, "this.location.href=\"http://" + men + "/central/servlet/InterAccountTransferPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty
                           + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p1=\"+code2;}");

    scoutln(out, bytesOut, "function reconcile(){document.go.submit();}");
    
    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\""
                         + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

     int[] hmenuCount = new int[1];

    drawingUtils.outputPageFrame(con, stmt, rs, out, req, "6002", "", "BankReconciliationPage", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount, bytesOut);

    drawingUtils.drawTitle(out, false, false, "BankReconciliationPage", "", "Bank Reconciliation", "6002", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
        
    scoutln(out, bytesOut, "<form action=\"BankReconciliationUpdate\" name=go enctype=\"application/x-www-form-urlencoded\" method=POST>");
    scoutln(out, bytesOut, "<input type=hidden name=unm value=" + unm + ">");
    scoutln(out, bytesOut, "<input type=hidden name=sid value=" + sid + ">");
    scoutln(out, bytesOut, "<input type=hidden name=uty value=" + uty + ">");
    scoutln(out, bytesOut, "<input type=hidden name=men value=" + men + ">");
    scoutln(out, bytesOut, "<input type=hidden name=den value=" + den + ">");
    scoutln(out, bytesOut, "<input type=hidden name=dnm value=" + dnm + ">");
    scoutln(out, bytesOut, "<input type=hidden name=bnm value=" + bnm + ">");

    String year = generalUtils.getFromDefnFile(unm, "accounts.dfn", workingDir, "");

    scoutln(out, bytesOut, "<p>Not-Reconciled Entries for " + account + " " + accountsUtils.getAccountDescriptionGivenAccCode(account, year, dnm,
                                                                                                                      localDefnsDir, defnsDir));

    scoutln(out, bytesOut, "<table id=\"page\" border=0 cellspacing=2 cellpadding=2 width=100%><tr>");

    scoutln(out, bytesOut, "<td id=\"pageColumn\">&nbsp;</td>");
    scoutln(out, bytesOut, "<td id=\"pageColumn\"><p>Transaction</td>");
    scoutln(out, bytesOut, "<td id=\"pageColumn\" align=center><p>Issue<br>Currency</td>");
    scoutln(out, bytesOut, "<td id=\"pageColumn\" align=right><p>Issue<br>Amount</td>");
    scoutln(out, bytesOut, "<td id=\"pageColumn\" align=right><p>Base<br>Amount</td>");
    scoutln(out, bytesOut, "<td id=\"pageColumn\"><p>Date</td>");
    scoutln(out, bytesOut, "<td id=\"pageColumn\" align=right><p>Bank<br>Amount</td>");
    scoutln(out, bytesOut, "<td id=\"pageColumn\" align=right><p>Charges</td>");
    scoutln(out, bytesOut, "<td id=\"pageColumn\"><p>Company</td>");
    scoutln(out, bytesOut, "<td id=\"pageColumn\"><p>Document Type</td>");
    scoutln(out, bytesOut, "<td id=\"pageColumn\"><p>Document Code</td></tr>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void detTransactionsNotReconciled(PrintWriter out, String dnm, boolean wantBuildStrs, String account, String localDefnsDir,
                                            String defnsDir, int[] bytesOut) throws Exception
  {
    String userName = directoryUtils.getMySQLUserName();
    String passWord = directoryUtils.getMySQLPassWord();
    
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + userName + "&password=" + passWord);

    String[] cssFormat = new String[1];  cssFormat[0] = "line1";

    // for each payment rec ------------------------------------------------------------------------------------

    Statement stmt = con.createStatement();
  
    ResultSet rs = stmt.executeQuery("SELECT PaymentCode, DatePaid, CompanyCode, BaseTotalAmount, TotalAmount, Currency, CompanyName, ChequeNumber, "
                                   + "Charges, DiscountAllowed, BankAmount FROM payment WHERE AccCredited = '" + account + "' AND Status != 'C' AND "
                                   + "Reconciled != 'Y' ORDER BY DatePaid");

    String paymentCode, companyName="", companyCode="", date, currency="", status, chequeNumber, thisDate;
    double totalAmount, baseTotalAmount, thisCharges, discountAllowed, bankAmount, paymentSubTotal = 0.0;
    boolean first = true;

    while(rs.next())                  
    {
      paymentCode     = rs.getString(1);
      thisDate        = generalUtils.convertFromYYYYMMDD(rs.getString(2));
      companyCode     = rs.getString(3);
      baseTotalAmount = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(4)), '2');
      totalAmount     = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(5)), '2');
      currency        = rs.getString(6);
      companyName     = rs.getString(7);
      chequeNumber    = rs.getString(8);
      thisCharges     = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(9)), '2');
      discountAllowed = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(10)), '2');
      bankAmount      = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(11)), '2');

      if(first)
      {
        scoutln(out, bytesOut, "<tr><td colspan=11><hr></td></tr>");
        first = false;
      }

      paymentSubTotal += totalAmount;

      buildStrs(out, paymentCode, thisDate, companyCode + " " + companyName, bankAmount, thisCharges, totalAmount, baseTotalAmount, currency, "P",
                chequeNumber, cssFormat, bytesOut);
    }
  
    if(stmt != null) stmt.close();
    
    // for each voucher rec ------------------------------------------------------------------------------------

    stmt = con.createStatement();
  
    rs = stmt.executeQuery("SELECT VoucherCode, Date, CompanyCode, BaseTotalTotal, TotalTotal, Currency, CompanyName, ChequeNumber FROM voucher "
                         + "WHERE AccountCr = '" + account + "' AND Status != 'C' AND Reconciled != 'Y' ORDER BY Date");

    String voucherCode;
    double totalTotal, baseTotalTotal, voucherSubTotal = 0.0;
    first = true;
            
    while(rs.next())                  
    {
      voucherCode    = rs.getString(1);
      thisDate       = generalUtils.convertFromYYYYMMDD(rs.getString(2));
      companyCode    = rs.getString(3);
      baseTotalTotal = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(4)), '2');
      totalTotal     = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(5)), '2');
      currency       = rs.getString(6);
      companyName    = rs.getString(7);
      chequeNumber   = rs.getString(8);

      if(first)
      {
        scoutln(out, bytesOut, "<tr><td colspan=11><hr></td></tr>");
        first = false;
      }

      voucherSubTotal += totalTotal;
      
      buildStrs(out, voucherCode, thisDate, companyCode + " " + companyName, totalTotal, 0.0, totalTotal, baseTotalTotal, currency, "V", chequeNumber,
                cssFormat, bytesOut);
    }
    
    if(stmt != null) stmt.close();
      
    // for each receipt rec ------------------------------------------------------------------------------------
    // if receipt is base
    //    if bank is base
    //       use base figures (charges are assumed base and subtracted)
    //    else bank is nonbase
    //       convert base amt to nonbase (using ratebanked)
    //       amt banked is converted amt less charges
    // else receipt is nonbase
    //    if bank is nonbase
    //       use nonbase figures (charges are assumed nonbase and subtracted)
    //    else bank is base
    //       convert nonbase amt to base (using ratebanked)
    //       amt banked is converted amt less charges

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT ReceiptCode, DateReceived, CompanyCode, BaseTotalAmount, TotalAmount, Currency, CompanyName, ChequeNumber, "
                         + "Charges, DiscountAllowed, ReceiptReference, BankedAmount FROM receipt WHERE AccDebited = '" + account
                         + "' AND Status != 'C' AND Reconciled != 'Y' " + "ORDER BY ReceiptReference, DateReceived");

    String receiptCode="", dateReceivedSoFar="", sourceDocumentType="", lastReferenceCode="", thisReferenceCode="", allDocCodes="", currencySoFar="",
           companySoFar="";
    double bankedAmount, bankedAmountSoFar=0.0, charges, totalAmountSoFar=0.0, chargesSoFar=0.0, baseAmountSoFar=0.0, receiptSubTotal = 0.0;
    boolean veryFirst = true;
    first = true;
    
    while(rs.next())                  
    {
      receiptCode       = rs.getString(1);
      thisDate          = generalUtils.convertFromYYYYMMDD(rs.getString(2));
      companyCode       = rs.getString(3);
      baseTotalAmount   = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(4)), '2');
      totalAmount       = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(5)), '2');
      currency          = rs.getString(6);
      companyName       = rs.getString(7);
      chequeNumber      = rs.getString(8);
      thisCharges       = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(9)), '2');
      discountAllowed   = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(10)), '2');
      thisReferenceCode = rs.getString(11);
      bankedAmount      = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(12)), '2');

      if(thisReferenceCode.length() == 0)
      {
        if(! veryFirst) // then can display the previous line
        {
          if(first)
          {
            scoutln(out, bytesOut, "<tr><td colspan=11><hr></td></tr>");
            first = false;
          }
          
          receiptSubTotal += totalAmountSoFar;

          buildStrs(out, allDocCodes, dateReceivedSoFar, companySoFar, bankedAmountSoFar, chargesSoFar, totalAmountSoFar, baseAmountSoFar,
                    currencySoFar, "R", lastReferenceCode, cssFormat, bytesOut);
        }
          
        allDocCodes       = (receiptCode + " ");
        bankedAmountSoFar = bankedAmount;
        totalAmountSoFar  = totalAmount;
        chargesSoFar      = thisCharges;
        baseAmountSoFar   = baseTotalAmount;
        currencySoFar     = currency + "<br>";
        companySoFar      = (companyCode + " " + companyName + "<br>");
        dateReceivedSoFar = thisDate + "<br>";

        lastReferenceCode = thisReferenceCode;
        veryFirst = false;
      }
      else          
      if(thisReferenceCode.length() > 0 && thisReferenceCode.equalsIgnoreCase(lastReferenceCode))
      {
        allDocCodes       += (receiptCode + " ");
        bankedAmountSoFar += bankedAmount;
        totalAmountSoFar  += totalAmount;
        chargesSoFar      += thisCharges;
        baseAmountSoFar   += baseTotalAmount;
        currencySoFar     += currency + "<br>";
        companySoFar      += (companyCode + " " + companyName + "<br>");
        dateReceivedSoFar += thisDate + "<br>";

        veryFirst = false;
      }
      else // ! thisReferenceCode.equalsIgnoreCase(lastReferenceCode)
      {
        if(! veryFirst) // then can display the previous line
        {
          if(first)
          {
            scoutln(out, bytesOut, "<tr><td colspan=11><hr></td></tr>");
            first = false;
          }
          
          receiptSubTotal += totalAmountSoFar;

          buildStrs(out, allDocCodes, dateReceivedSoFar, companySoFar, bankedAmountSoFar, chargesSoFar, totalAmountSoFar, baseAmountSoFar,
                    currencySoFar, "R", lastReferenceCode, cssFormat, bytesOut);
        }
          
        allDocCodes       = (receiptCode + " ");
        bankedAmountSoFar = bankedAmount;
        totalAmountSoFar  = totalAmount;
        chargesSoFar      = thisCharges;
        baseAmountSoFar   = baseTotalAmount;
        currencySoFar     = currency + "<br>";
        companySoFar      = (companyCode + " " + companyName + "<br>");
        dateReceivedSoFar = thisDate + "<br>";

        lastReferenceCode = thisReferenceCode;
      }
    }

    if(! veryFirst) // something to output
    {
      if(first)
      {
        scoutln(out, bytesOut, "<tr><td colspan=11><hr></td></tr>");
        first = false;
      }

      receiptSubTotal += totalAmountSoFar;

      buildStrs(out, allDocCodes, dateReceivedSoFar, companyCode + " " + companyName, bankedAmountSoFar, chargesSoFar, totalAmountSoFar,
                baseAmountSoFar, currencySoFar, "R", lastReferenceCode, cssFormat, bytesOut);
    }

    if(stmt != null) stmt.close();

    // for each receipt voucher rec ------------------------------------------------------------------------------------
    stmt = con.createStatement();
  
    rs = stmt.executeQuery("SELECT VoucherCode, Date, CompanyCode, BaseTotalTotal, TotalTotal, Currency, CompanyName, ChequeNumber "
                         + "FROM rvoucher WHERE AccountDr = '" + account + "' AND Status != 'C' AND Reconciled != 'Y' ORDER BY Date");
    double receiptVoucherSubTotal = 0.0;
    
    first = true;

    
    while(rs.next())                  
    {
      voucherCode    = rs.getString(1);
      thisDate       = generalUtils.convertFromYYYYMMDD(rs.getString(2));
      companyCode    = rs.getString(3);
      baseTotalTotal = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(4)), '2');
      totalTotal     = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(5)), '2');
      currency       = rs.getString(6);
      companyName    = rs.getString(7);
      chequeNumber   = rs.getString(8);

      if(first)
      {
        scoutln(out, bytesOut, "<tr><td colspan=11><hr></td></tr>");
        first = false;
      }

      receiptVoucherSubTotal += totalTotal;

      buildStrs(out, voucherCode, thisDate, companyCode + " " + companyName, totalTotal, 0.0, totalTotal, baseTotalTotal, currency, "O", chequeNumber,
                cssFormat, bytesOut);
    }
    
    if(stmt != null) stmt.close();

    // for each IAT rec ------------------------------------------------------------------------------------
    // for each iat rec... only want if one of the 4 accounts is this bank

    stmt = con.createStatement();
  
    rs = stmt.executeQuery("SELECT IATCode, TransactionDate, AmountCr, AmountDr, RateCr, RateDr, CurrencyCr, CurrencyDr, ChargesCr, ChargesDr, "
                         + "AccountCr, AccountDr, ReconciledCr, ReconciledDr, Remark FROM iat WHERE (AccountCr = '" + account + "' OR AccountDr = '"
                         + account + "') AND Status != 'C' AND (ReconciledCr != 'Y' OR ReconciledDr != 'Y') ORDER BY TransactionDate");

    String iatCode, accountCr, accountDr, currencyCr, currencyDr, reconciledCr, reconciledDr, remark;
    double amountCr, amountDr, rateCr, rateDr, chargesCr, chargesDr, iatSubTotal = 0.0;
    first = true;

    while(rs.next())                  
    {
      iatCode      = rs.getString(1);
      thisDate     = generalUtils.convertFromYYYYMMDD(rs.getString(2));
      amountCr     = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(3)), '2');
      amountDr     = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(4)), '2');
      rateCr       = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(5)), '2');
      rateDr       = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(6)), '2');
      currencyCr   = rs.getString(7);
      currencyDr   = rs.getString(8);
      chargesCr    = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(9)), '2');
      chargesDr    = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(10)), '2');
      accountCr    = rs.getString(11);
      accountDr    = rs.getString(12);
      reconciledCr = rs.getString(13);
      reconciledDr = rs.getString(14);
      remark       = rs.getString(15);
      
      iatSubTotal += (amountDr - amountCr);

      if(first)
      {
        scoutln(out, bytesOut, "<tr><td colspan=11><hr></td></tr>");
        first = false;
      }
        
      if(! reconciledCr.equals("Y"))
      {  
        if(account.equals(accountCr))
        {
          buildStrs(out, iatCode, thisDate, remark, amountCr, chargesCr, amountCr, (amountCr * rateCr), currencyCr, "I", "", cssFormat, bytesOut);
        }
      }
      
      if(! reconciledDr.equals("Y"))
      {  
        if(account.equals(accountDr))
          buildStrs(out, iatCode, thisDate, remark, amountDr, chargesDr, amountDr, (amountDr * rateDr), currencyDr, "J", "", cssFormat, bytesOut);
      }
    }
   
    if(stmt != null) stmt.close();

    if(con  != null) con.close();
    
    writeSubTotals(out, voucherSubTotal, receiptVoucherSubTotal, paymentSubTotal, receiptSubTotal, iatSubTotal, bytesOut);  
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void buildStrs(PrintWriter out, String docCodes, String date, String company, double amount, double charges, double issueAmount,
                         double baseAmount, String currency, String sourceDocumentType, String chequeNumber, String[] cssFormat, int[] bytesOut)
                         throws Exception
  {
    int x=0, len = docCodes.length();
    String s="", thisDocCode;

    while(x < len)
    {
      thisDocCode="";
      while(x < len && docCodes.charAt(x) != ' ')
        thisDocCode += docCodes.charAt(x++);

      s += sourceDocumentType.charAt(0) + thisDocCode + "\001";
      ++x;
    }
 
    if(cssFormat[0].equals("line2"))
      cssFormat[0] = "line1";
    else cssFormat[0] = "line2";

    scoutln(out, bytesOut, "<tr id=\"" + cssFormat[0] + "\">");
    scoutln(out, bytesOut, "<td><input type=checkbox name=\"" + s + "\"></td>");

    scoutln(out, bytesOut, "<td nowrap><p>" + chequeNumber + "</td>");

    scoutln(out, bytesOut, "<td nowrap align=center><p>" + currency + "</td>");
    scoutln(out, bytesOut, "<td align=right nowrap><p>" + generalUtils.doubleDPs('2', issueAmount) + "</td>");
    scoutln(out, bytesOut, "<td align=right nowrap><p>" + generalUtils.doubleDPs('2', baseAmount) + "</td>");
    scoutln(out, bytesOut, "<td nowrap><p>" + date + "</td>");
    scoutln(out, bytesOut, "<td align=right nowrap><p>" + generalUtils.doubleDPs('2', amount) + "</td>");
    scoutln(out, bytesOut, "<td align=right nowrap><p>" + generalUtils.doubleDPs('2', charges) + "</td>");
        
    scoutln(out, bytesOut, "<td nowrap><p>" + company + "</td>");

    scoutln(out, bytesOut, "<td nowrap><p>");
    s = "";
    switch(sourceDocumentType.charAt(0))
    {
      case 'P' : s = "Payment";                     break;
      case 'V' : s = "Payment Voucher";             break;
      case 'R' : s = "Receipt";                     break;
      case 'O' : s = "Receipt Voucher";             break;
      case 'I' : s = "InterAccount Transfer (Cr)";  break;
      case 'J' : s = "InterAccount Transfer (Dr)";  break;
    }
    scoutln(out, bytesOut, s + "</td><td nowrap><p>");

    x=0;
    while(x < len)
    {
      thisDocCode="";
      while(x < len && docCodes.charAt(x) != ' ')
        thisDocCode += docCodes.charAt(x++);

      ++x;
      scoutln(out, bytesOut, "<a href=\"javascript:change('" + thisDocCode + "','" + sourceDocumentType.charAt(0) + "')\">" + thisDocCode + "</a><br>");
    }

    scoutln(out, bytesOut, "</td></tr>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void writeSubTotals(PrintWriter out, double voucherSubTotal, double receiptVoucherSubTotal, double paymentSubTotal, double receiptSubTotal,
                              double iatSubTotal, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td colspan=3><p>Payments:</td><td align=right nowrap><p>" + generalUtils.doubleDPs('2', paymentSubTotal) + "</td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=3><p>Receipts:</td><td align=right nowrap><p>" + generalUtils.doubleDPs('2', receiptSubTotal) + "</td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=3><p>Payment Vouchers:</td><td align=right nowrap><p>" + generalUtils.doubleDPs('2', voucherSubTotal) + "</td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=3><p>Receipt Vouchers:</td><td align=right nowrap><p>" + generalUtils.doubleDPs('2', receiptVoucherSubTotal) + "</td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap colspan=3><p>Inter-Account Transfers:</td><td align=right nowrap><p>" + generalUtils.doubleDPs('2', iatSubTotal) + "</td></tr>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
