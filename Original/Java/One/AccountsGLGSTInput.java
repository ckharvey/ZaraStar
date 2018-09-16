// =======================================================================================================================================================================================================
// System: ZaraStar: Accounts: GL GST Input
// Module: AccountsGLGSTInput.java
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

public class AccountsGLGSTInput extends HttpServlet
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

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="", p3="", p4="";

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
      p1  = req.getParameter("p1"); // account
      p2  = req.getParameter("p2"); // dateFrom
      p3  = req.getParameter("p3"); // dateTo
      p4  = req.getParameter("p4"); // plain or not
      
      if(p4 == null) p4 = "";

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, p3, p4, bytesOut);
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

      System.out.println("6007j: " + e);
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "AccountsGLGSTInput", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6007, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm,
                    String bnm, String p1, String p2, String p3, String p4, int[] bytesOut) throws Exception
  {
    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String workingDir       = directoryUtils.getUserDir('W', dnm, unm);

    long startTime = new java.util.Date().getTime();

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    Statement stmt2 = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 6028, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "AccountsGLGSTInput", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6007, bytesOut[0], 0, "ACC:" + p1);
    if(con  != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "AccountsGLGSTInput", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6007, bytesOut[0], 0, "SID:" + p1);
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
        
    set(con, stmt, stmt2, rs, out, req, dateFrom, dateTo, p1, p2, p3, plain, unm, sid, uty, men, den, dnm, bnm, workingDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 6007, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con  != null) con.close();
    if(out != null) out.flush();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, Statement stmt2, ResultSet rs, PrintWriter out, HttpServletRequest req, String dateFrom, String dateTo, String p1, String p2, String p3, boolean plain,
                   String unm, String sid, String uty, String men, String den, String dnm, String bnm, String workingDir, String localDefnsDir,
                   String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>GL Account: GST Input</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

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
  
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 5026, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewPCN(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/PurchaseCreditNotePage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men
                           + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }
  
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 5036, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewPDN(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/PurchaseDebitNotePage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men
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
 
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "6007", "", true, plain, "", p2, p3, "", "", "AccountsGLGSTInput", unm, sid, uty, men, den, dnm, bnm,
                          localDefnsDir, defnsDir, hmenuCount, bytesOut);

    String year = generalUtils.getFromDefnFile(unm, "accounts.dfn", workingDir, "");

    if(! plain)
    {
      pageFrameUtils.drawTitle(out, true, plain, "AccountsGLGSTInput", p1, dateFrom, dateTo, "", "", "GL Account: " + p1 + " - " + accountsUtils.getAccountDescriptionGivenAccCode(p1, year, dnm, localDefnsDir, defnsDir) + " ("
                                                                                 + accountsUtils.getAccountCurrencyGivenAccCode(p1, year, dnm, localDefnsDir, defnsDir) + " "
                                                                                 + accountsUtils.getAccountDrCrGivenAccCode(p1, year, dnm, localDefnsDir, defnsDir) + ")", "6007", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);
    }
    else
    {
      pageFrameUtils.drawTitle(out, false, plain, "", "", "", "", "", "", "GL Account: " + p1 + " - " + accountsUtils.getAccountDescriptionGivenAccCode(p1, year, dnm, localDefnsDir, defnsDir) + " ("
                                                               + accountsUtils.getAccountCurrencyGivenAccCode(p1, year, dnm, localDefnsDir, defnsDir) + " " + accountsUtils.getAccountDrCrGivenAccCode(p1, year, dnm, localDefnsDir, defnsDir) + ")", "",
                                                               unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);
    }

    scoutln(out, bytesOut, "<form><table id=\"page\" width=100% border=0>");

    
    String tmpTable = unm + "_tmp";
    
    directoryUtils.createTmpTable(true, con, stmt, "Type char(1), Code char(20), Reference char(20), Description char(100), Total decimal(19,8), "
                                  + "BaseTotal decimal(19,8), Date date, Currency char(3)", "", tmpTable);
    
    calc(con, stmt, stmt2, rs, p1, year, dateFrom, dateTo, dnm, localDefnsDir, defnsDir, tmpTable);
    
    processTmpTable(con, stmt, rs, tmpTable, out, localDefnsDir, defnsDir, bytesOut);
    
    scoutln(out, bytesOut, "</table></form>");     scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void calc(Connection con, Statement stmt, Statement stmt2, ResultSet rs, String account, String year, String dateFrom, String dateTo,
                   String dnm, String localDefnsDir, String defnsDir, String tmpTable) throws Exception
  {
    onPurchaseInvoice(con, stmt, stmt2, rs, dateFrom, dateTo, tmpTable);
    onPurchaseDebitNote(con, stmt, stmt2, rs, dateFrom, dateTo, tmpTable);
    onPurchaseCreditNote(con, stmt, stmt2, rs, dateFrom, dateTo, tmpTable);
    onPaymentVoucherNotStrictGST(con, stmt, stmt2, rs, dateFrom, dateTo, tmpTable);
    onPaymentVoucherStrictGST(con, stmt, stmt2, rs, dateFrom, dateTo, tmpTable);
    onVoucherLines(con, stmt, stmt2, rs, account, year, dateFrom, dateTo, tmpTable);
    onRVoucherLines(con, stmt, stmt2, rs, account, year, dateFrom, dateTo, tmpTable);
    onIATCr(con, stmt, stmt2, rs, year, account, dateFrom, dateTo, tmpTable);
    onIATDr(con, stmt, stmt2, rs, year, account, dateFrom, dateTo, tmpTable);
    onBatchCr(con, stmt2, year, account, dateFrom, dateTo, tmpTable, dnm, localDefnsDir, defnsDir);
    onBatchDr(con, stmt2, year, account, dateFrom, dateTo, tmpTable, dnm, localDefnsDir, defnsDir);
  }
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void onPurchaseInvoice(Connection con, Statement stmt, Statement stmt2, ResultSet rs, String dateFrom, String dateTo, String tmpTable)
                                 throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT InvoiceCode, CompanyName, GSTTotal, BaseGSTTotal, Date, Currency "
                         + "FROM pinvoice WHERE Status != 'C' AND Date >= {d '" + dateFrom + "'} AND Date <= {d '" + dateTo + "'}");
    double gstAmount, baseGSTAmount;           
            
    while(rs.next())
    {    
      gstAmount     = generalUtils.doubleFromStr(rs.getString(3));
      baseGSTAmount = generalUtils.doubleFromStr(rs.getString(4));

      if(baseGSTAmount != 0.0)
        addToTmpTable(con, stmt2, "N", rs.getString(1), rs.getString(2), gstAmount, baseGSTAmount, rs.getString(5), rs.getString(6), tmpTable);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void onPurchaseDebitNote(Connection con, Statement stmt, Statement stmt2, ResultSet rs, String dateFrom, String dateTo, String tmpTable)
                                    throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT PDNCode, CompanyName, GSTTotal, BaseGSTTotal, Date, Currency "
                         + "FROM pdebit WHERE Status != 'C' AND Date >= {d '" + dateFrom + "'} AND Date <= {d '" + dateTo + "'}");
    double gstAmount, baseGSTAmount;           
            
    while(rs.next())
    {    
      gstAmount     = generalUtils.doubleFromStr(rs.getString(3));
      baseGSTAmount = generalUtils.doubleFromStr(rs.getString(4));

      if(baseGSTAmount != 0.0)
        addToTmpTable(con, stmt2, "E", rs.getString(1), rs.getString(2), gstAmount, baseGSTAmount, rs.getString(5), rs.getString(6), tmpTable);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void onPurchaseCreditNote(Connection con, Statement stmt, Statement stmt2, ResultSet rs, String dateFrom, String dateTo, String tmpTable)
                                    throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT PCNCode, CompanyName, GSTTotal, BaseGSTTotal, Date, Currency "
                         + "FROM pcredit WHERE Status != 'C' AND Date >= {d '" + dateFrom + "'} AND Date <= {d '" + dateTo + "'}");
    double gstAmount, baseGSTAmount;           
            
    while(rs.next())
    {    
      gstAmount     = generalUtils.doubleFromStr(rs.getString(3));
      baseGSTAmount = generalUtils.doubleFromStr(rs.getString(4));

      if(baseGSTAmount != 0.0)
        addToTmpTable(con, stmt2, "D", rs.getString(1), rs.getString(2), gstAmount, baseGSTAmount, rs.getString(5), rs.getString(6), tmpTable);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void onPaymentVoucherNotStrictGST(Connection con, Statement stmt, Statement stmt2, ResultSet rs, String dateFrom, String dateTo, String tmpTable) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT VoucherCode, CompanyName, GSTTotal, BaseGSTTotal, Date, Currency "
                         + "FROM voucher WHERE Status != 'C' AND Type != 'C' AND Date >= {d '" + dateFrom + "'} AND Date <= {d '" + dateTo + "'}");
    double gstAmount, baseGSTAmount;           
            
    while(rs.next())
    {    
      gstAmount     = generalUtils.doubleFromStr(rs.getString(3));
      baseGSTAmount = generalUtils.doubleFromStr(rs.getString(4));

      if(baseGSTAmount != 0.0)
        addToTmpTable(con, stmt2, "V", rs.getString(1), rs.getString(2), gstAmount, baseGSTAmount, rs.getString(5), rs.getString(6), tmpTable);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void onPaymentVoucherStrictGST(Connection con, Statement stmt, Statement stmt2, ResultSet rs, String dateFrom, String dateTo, String tmpTable) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT VoucherCode, CompanyName, TotalTotal, BaseTotalTotal, Date, Currency "
                         + "FROM voucher WHERE Status != 'C' AND Type = 'C' AND Date >= {d '" + dateFrom + "'} AND Date <= {d '" + dateTo + "'}");
    double amount, baseAmount;           
            
    while(rs.next())
    {    
      amount     = generalUtils.doubleFromStr(rs.getString(3));
      baseAmount = generalUtils.doubleFromStr(rs.getString(4));

      if(baseAmount != 0.0)
        addToTmpTable(con, stmt2, "W", rs.getString(1), rs.getString(2), amount, baseAmount, rs.getString(5), rs.getString(6), tmpTable);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void onVoucherLines(Connection con, Statement stmt, Statement stmt2, ResultSet rs, String accCode, String year, String dateFrom,
                              String dateTo, String tmpTable) throws Exception
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

      addToTmpTable(con, stmt2, "K", rs.getString(1), rs.getString(2), amount, baseAmount, rs.getString(5), rs.getString(6), tmpTable);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void onRVoucherLines(Connection con, Statement stmt, Statement stmt2, ResultSet rs, String accCode, String year, String dateFrom,
                               String dateTo, String tmpTable) throws Exception
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

      addToTmpTable(con, stmt2, "L", rs.getString(1), rs.getString(2), amount, baseAmount, rs.getString(5), rs.getString(6), tmpTable);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean areOpeningBalances(String batchCode, String year, String dnm) throws Exception
  {
    String ob = "N";

    Connection con = null;
    Statement stmt = null;
    ResultSet rs   = null;

    try
    {
      String uName = directoryUtils.getMySQLUserName();
      String pWord = directoryUtils.getMySQLPassWord();
      Class.forName("com.mysql.jdbc.Driver").newInstance();
      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_accounts_" + year + "?user=" + uName + "&password=" + pWord);

      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT OpeningBalances FROM joubatch WHERE Code = '" + generalUtils.sanitiseForSQL(batchCode) + "'");

      if(rs.next())
        ob = rs.getString(1);

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }

    if(ob.equals("Y"))
      return true;

    return false;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void onBatchCr(Connection con, Statement stmt2, String year, String account, String dateFrom, String dateTo, String tmpTable, String dnm,
                         String localDefnsDir, String defnsDir) throws Exception
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

      accRs = accStmt.executeQuery("SELECT Code, Remark, Amount, BaseAmount, TransactionDate, Currency FROM joubatchl WHERE AccCode = '" + account
                                 + "' AND DrCr = 'C' AND TransactionDate >= {d '" + dateFrom + "'} AND TransactionDate <= {d '" + dateTo + "'}");
      double amount, baseAmount;
                
      while(accRs.next())
      {    
        amount = generalUtils.doubleFromStr(accRs.getString(3));
        if(areOpeningBalances(accRs.getString(1), year, dnm))
          baseAmount = generalUtils.doubleFromStr(accRs.getString(3));
        else baseAmount = generalUtils.doubleFromStr(accRs.getString(4));

        if(baseAmount != 0.0)
          addToTmpTable(con, stmt2, "B", accRs.getString(1), accRs.getString(2), amount, baseAmount, accRs.getString(5), accRs.getString(6), tmpTable);
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
  private void onBatchDr(Connection con, Statement stmt2, String year, String account, String dateFrom, String dateTo, String tmpTable, String dnm,
                         String localDefnsDir, String defnsDir) throws Exception
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

      accRs = accStmt.executeQuery("SELECT Code, Remark, Amount, BaseAmount, TransactionDate, Currency FROM joubatchl WHERE AccCode = '" + account
                                 + "' AND DrCr = 'D' AND TransactionDate >= {d '" + dateFrom + "'} AND TransactionDate <= {d '" + dateTo + "'}");
      double amount, baseAmount;
                
      while(accRs.next())
      {    
        amount = generalUtils.doubleFromStr(accRs.getString(3));
        if(areOpeningBalances(accRs.getString(1), year, dnm))
          baseAmount = generalUtils.doubleFromStr(accRs.getString(3));
        else baseAmount = generalUtils.doubleFromStr(accRs.getString(4));

        if(baseAmount != 0.0)
          addToTmpTable(con, stmt2, "C", accRs.getString(1), accRs.getString(2), amount, baseAmount, accRs.getString(5), accRs.getString(6), tmpTable);
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
  private void onIATCr(Connection con, Statement stmt, Statement stmt2, ResultSet rs, String year, String accCode, String dateFrom, String dateTo,
                       String tmpTable) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT IATCode, Remark, AmountCr, TransactionDate, CurrencyCr, RateCr "
                         + "FROM iat WHERE AccountCr = '" + accCode + "' AND Status != 'C' AND TransactionDate >= {d '"
                         + dateFrom + "'} AND TransactionDate <= {d '" + dateTo + "'}");
    double amount, baseAmount, rate;
            
    while(rs.next())
    {    
      amount = generalUtils.doubleFromStr(rs.getString(3));
      rate   = generalUtils.doubleFromStr(rs.getString(6));

      baseAmount = generalUtils.doubleDPs((amount * rate), '2');

      addToTmpTable(con, stmt2, "I", rs.getString(1), rs.getString(2), amount, baseAmount, rs.getString(4), rs.getString(5), tmpTable);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void onIATDr(Connection con, Statement stmt, Statement stmt2, ResultSet rs, String year, String accCode, String dateFrom, String dateTo,
                       String tmpTable) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT IATCode, Remark, AmountDr, TransactionDate, CurrencyDr, RateDr "
                         + "FROM iat WHERE AccountDr = '" + accCode + "' AND Status != 'C' AND TransactionDate >= {d '"
                         + dateFrom + "'} AND TransactionDate <= {d '" + dateTo + "'}");
    double amount, baseAmount, rate;
            
    while(rs.next())
    {    
      amount = generalUtils.doubleFromStr(rs.getString(3));
      rate   = generalUtils.doubleFromStr(rs.getString(6));

      baseAmount = generalUtils.doubleDPs((amount * rate), '2');

      addToTmpTable(con, stmt2, "J", rs.getString(1), rs.getString(2), amount, baseAmount, rs.getString(4), rs.getString(5), tmpTable);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void processTmpTable(Connection con, Statement stmt, ResultSet rs, String tmpTable, PrintWriter out, String localDefnsDir,
                               String defnsDir, int[] bytesOut) throws Exception
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

    rs = stmt.executeQuery("SELECT * FROM " + tmpTable + " ORDER BY Date, Type"); // ensuring the OB (in JB) come first

    String code, type, desc, date, currency, cssFormat="";
    double amount, baseAmount, baseBalance = 0.0, monthBalance = 0.0;
    int month, thisMonth = 0;

    String drCr = "";
    double totalCRs = 0.0, totalDRs = 0.0;

    while(rs.next())
    {
      type       = rs.getString(1);
      code       = rs.getString(2);
      desc       = rs.getString(4);
      amount     = generalUtils.doubleFromStr(generalUtils.doubleDPs(rs.getString(5), '2'));
      baseAmount = generalUtils.doubleFromStr(generalUtils.doubleDPs(rs.getString(6), '2'));
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

      if(type.charAt(0) == 'V')
      {
        type = "Payment Voucher";
        drCr = "Dr";
        totalDRs += baseAmount;
        baseBalance += baseAmount;
        monthBalance += baseAmount;
      }
      else
      if(type.charAt(0) == 'W')
      {
        type = "Payment Voucher (Strict)";
        drCr = "Dr";
        totalDRs += baseAmount;
        baseBalance += baseAmount;
        monthBalance += baseAmount;
      }
      else
      if(type.charAt(0) == 'K')
      {
        type = "Payment Voucher";
        drCr = "Dr";
        totalDRs += baseAmount;
        baseBalance += baseAmount;
        monthBalance += baseAmount;
      }
      else
      if(type.charAt(0) == 'L')
      {
        type = "Receipt Voucher";
        drCr = "Cr";
        totalCRs += baseAmount;
        baseBalance -= baseAmount;
        monthBalance -= baseAmount;
      }
      else
      if(type.charAt(0) == 'B')
      {
        type = "Journal Batch";
        drCr = "Cr";
        totalCRs += baseAmount;
        baseBalance -= baseAmount;
        monthBalance -= baseAmount;
      }
      else
      if(type.charAt(0) == 'C')
      {
        type = "Journal Batch";
        drCr = "Dr";
        totalDRs += baseAmount;
        baseBalance += baseAmount;
        monthBalance += baseAmount;
      }
      else
      if(type.charAt(0) == 'N')
      {
        type = "Purchase Invoice";
        drCr = "Dr";
        totalDRs += baseAmount;
        baseBalance += baseAmount;
        monthBalance += baseAmount;
      }
      else
      if(type.charAt(0) == 'E')
      {
        type = "Purchase Debit Note";
        drCr = "Dr";
        totalDRs += baseAmount;
        baseBalance += baseAmount;
        monthBalance += baseAmount;
      }
      else
      if(type.charAt(0) == 'D')
      {
        type = "Purchase Credit Note";
        drCr = "Cr";
        totalCRs += baseAmount;
        baseBalance -= baseAmount;
        monthBalance -= baseAmount;
      }
      else
      if(type.charAt(0) == 'I')
      {
        type = "Inter-Account Transfer";
        drCr = "Cr";
        totalCRs += baseAmount;
        baseBalance -= baseAmount;
        monthBalance -= baseAmount;
      }
      else
      if(type.charAt(0) == 'J')
      {
        type = "Inter-Account Transfer";
        drCr = "Dr";
        totalDRs += baseAmount;
        baseBalance += baseAmount;
        monthBalance += baseAmount;
      }

      generalUtils.repAlpha(data, 2000, (short)1, code);
      generalUtils.repAlpha(data, 2000, (short)2, type);
      generalUtils.repAlpha(data, 2000, (short)3, generalUtils.convertFromYYYYMMDD(date));

      generalUtils.repAlpha(data, 2000, (short)6, currency);

      generalUtils.repAlpha(data, 2000, (short)5, drCr);

      generalUtils.repAlpha(data, 2000, (short)7,  generalUtils.formatNumeric(amount, '2'));
      generalUtils.repAlpha(data, 2000, (short)8,  generalUtils.formatNumeric(baseAmount, '2'));
      generalUtils.repAlpha(data, 2000, (short)9,  generalUtils.formatNumeric(monthBalance, '2'));
      generalUtils.repAlpha(data, 2000, (short)10, generalUtils.formatNumeric(baseBalance, '2'));
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
  public void processTmpTableForOBs(Connection con, Statement stmt, ResultSet rs, String tmpTable, double[] totalDRs, double[] totalCRs) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT * FROM " + tmpTable);

    String type;
    double baseAmount;

    totalCRs[0] = totalDRs[0] = 0.0;

    while(rs.next())
    {
      type       = rs.getString(1);
      baseAmount = generalUtils.doubleFromStr(generalUtils.doubleDPs(rs.getString(6), '2'));

      if(type.charAt(0) == 'V')
      {
        totalDRs[0] += baseAmount;
      }
      else
      if(type.charAt(0) == 'W')
      {
        totalDRs[0] += baseAmount;
      }
      else
      if(type.charAt(0) == 'K')
      {
        totalDRs[0] += baseAmount;
      }
      else
      if(type.charAt(0) == 'L')
      {
        totalCRs[0] += baseAmount;
      }
      else
      if(type.charAt(0) == 'B')
      {
        totalCRs[0] += baseAmount;
      }
      else
      if(type.charAt(0) == 'C')
      {
        totalDRs[0] += baseAmount;
      }
      else
      if(type.charAt(0) == 'N')
      {
        totalDRs[0] += baseAmount;
      }
      else
      if(type.charAt(0) == 'E')
      {
        totalDRs[0] += baseAmount;
      }
      else
      if(type.charAt(0) == 'D')
      {
        totalCRs[0] += baseAmount;
      }
      else
      if(type.charAt(0) == 'I')
      {
        totalCRs[0] += baseAmount;
      }
      else
      if(type.charAt(0) == 'J')
      {
        totalDRs[0] += baseAmount;
      }
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void addToTmpTable(Connection con, Statement stmt, String type, String code, String desc, double amount, double baseAmount, String date,
                             String currency, String tmpTable) throws Exception
  {
    stmt = con.createStatement();
   
    String q = "INSERT INTO " + tmpTable + " ( Type, Code, Description, Total, BaseTotal, Date, Currency ) "
             + "VALUES ( '" + type + "', '" + code + "', '" + generalUtils.sanitiseForSQL(desc) + "', '" + amount + "', '" + baseAmount
             + "', {d '" + date + "'}, '" + currency + "' )";

    stmt.executeUpdate(q);
  
    if(stmt != null) stmt.close();
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

    rs = stmt.executeQuery("SELECT * FROM " + tmpTable + " ORDER BY Date, Type"); // ensuring the OB (in JB) come first

    String code, type, desc, date, currency, cssFormat="";
    double amount, baseAmount, baseBalance = 0.0, monthBalance = 0.0;
    int month, thisMonth = 0;

    String drCr = "";
    double totalCRs = 0.0, totalDRs = 0.0;

    while(rs.next())
    {
      type       = rs.getString(1);
      code       = rs.getString(2);
      desc       = rs.getString(4);
      amount     = generalUtils.doubleFromStr(generalUtils.doubleDPs(rs.getString(5), '2'));
      baseAmount = generalUtils.doubleFromStr(generalUtils.doubleDPs(rs.getString(6), '2'));
      date       = rs.getString(7);
      currency   = rs.getString(8);

      month = generalUtils.getMonth(generalUtils.encodeFromYYYYMMDD(date));
      if(month != thisMonth)
      {
        thisMonth = month;
        monthBalance = 0.0;
      }

      if(type.charAt(0) == 'V')
      {
        type = "Payment Voucher";
        drCr = "Dr";
        totalDRs += baseAmount;
        baseBalance += baseAmount;
        monthBalance += baseAmount;
      }
      else
      if(type.charAt(0) == 'W')
      {
        type = "Payment Voucher (Strict)";
        drCr = "Dr";
        totalDRs += baseAmount;
        baseBalance += baseAmount;
        monthBalance += baseAmount;
      }
      else
      if(type.charAt(0) == 'K')
      {
        type = "Payment Voucher";
        drCr = "Dr";
        totalDRs += baseAmount;
        baseBalance += baseAmount;
        monthBalance += baseAmount;
      }
      else
      if(type.charAt(0) == 'L')
      {
        type = "Receipt Voucher";
        drCr = "Cr";
        totalCRs += baseAmount;
        baseBalance -= baseAmount;
        monthBalance -= baseAmount;
      }
      else
      if(type.charAt(0) == 'B')
      {
        type = "Journal Batch";
        drCr = "Cr";
        totalCRs += baseAmount;
        baseBalance -= baseAmount;
        monthBalance -= baseAmount;
      }
      else
      if(type.charAt(0) == 'C')
      {
        type = "Journal Batch";
        drCr = "Dr";
        totalDRs += baseAmount;
        baseBalance += baseAmount;
        monthBalance += baseAmount;
      }
      else
      if(type.charAt(0) == 'N')
      {
        type = "Purchase Invoice";
        drCr = "Dr";
        totalDRs += baseAmount;
        baseBalance += baseAmount;
        monthBalance += baseAmount;
      }
      else
      if(type.charAt(0) == 'E')
      {
        type = "Purchase Debit Note";
        drCr = "Dr";
        totalDRs += baseAmount;
        baseBalance += baseAmount;
        monthBalance += baseAmount;
      }
      else
      if(type.charAt(0) == 'D')
      {
        type = "Purchase Credit Note";
        drCr = "Cr";
        totalCRs += baseAmount;
        baseBalance -= baseAmount;
        monthBalance -= baseAmount;
      }
      else
      if(type.charAt(0) == 'I')
      {
        type = "Inter-Account Transfer";
        drCr = "Cr";
        totalCRs += baseAmount;
        baseBalance -= baseAmount;
        monthBalance -= baseAmount;
      }
      else
      if(type.charAt(0) == 'J')
      {
        type = "Inter-Account Transfer";
        drCr = "Dr";
        totalDRs += baseAmount;
        baseBalance += baseAmount;
        monthBalance += baseAmount;
      }

      writeEntry(fh, account,      true);
      writeEntry(fh, code,         true);
      writeEntry(fh, type,         true);
      writeEntry(fh, date,         true);
      writeEntry(fh, "",           true);
      writeEntry(fh, drCr,         true);
      writeEntry(fh, currency,     true);
      writeEntry(fh, amount,       true);
      writeEntry(fh, baseAmount,   true);
      writeEntry(fh, monthBalance, true);
      writeEntry(fh, baseBalance,  true);
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
