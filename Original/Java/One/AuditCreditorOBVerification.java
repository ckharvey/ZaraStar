// =============================================================================================================================================================
// System: ZaraStar Audit: Creditor OB Verification  
// Module: AuditCreditorOBVerification.java
// Author: C.K.Harvey
// Copyright (c) 1999-2008 Christopher Harvey. All Rights Reserved.
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

public class AuditCreditorOBVerification extends HttpServlet
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

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="";

    try
    {
      res.setContentType("text/html");
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "AuditCreditorOBVerification", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6035, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut)
                    throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');
    String workingDir       = directoryUtils.getUserDir('W', dnm, unm);

    String userName = directoryUtils.getMySQLUserName();
    String passWord = directoryUtils.getMySQLPassWord();
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + userName + "&password=" + passWord);
    Statement stmt = null, stmt2 = null;
    ResultSet rs = null, rs2 = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 6035, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "6035", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6035, bytesOut[0], 0, "ACC:");
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "6035", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6035, bytesOut[0], 0, "SID:");
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

    String dateFrom = "01." + startMonth + "." + year;

    String effectiveDate = definitionTables.getAppConfigEffectiveStartDate(con, stmt, rs, dnm);

    if(generalUtils.encode(effectiveDate, localDefnsDir, defnsDir) > generalUtils.encode(dateFrom, localDefnsDir, defnsDir))
      dateFrom = effectiveDate;
    
    String dateToDDMMYY = dateFrom;

    int date = generalUtils.encode(dateFrom, localDefnsDir, defnsDir);
    String dayBeforeDateToDDMMYY = generalUtils.decode((date - 1), localDefnsDir, defnsDir);
    
    dateFrom = generalUtils.convertDateToSQLFormat(dateFrom);

    allCompanies(con, stmt, stmt2, rs, rs2, req, out, year, dateFrom, dateToDDMMYY, dayBeforeDateToDDMMYY, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 6035, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con  != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void allCompanies(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, HttpServletRequest req, PrintWriter out, int year,
                            String dateTo, String dateToDDMMYY, String dayBeforeDateToDDMMYY, String unm, String sid, String uty, String men, String den,
                            String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Creditor Opening Balances Verification</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    scoutln(out, bytesOut, "function viewSupp(code){var p1=sanitise(code);");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/SupplierPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm="
                         + dnm + "&bnm=" + bnm + "&p2=A&p1=\"+p1;}");
   
    scoutln(out, bytesOut, "function viewJB(code){var p1=sanitise(code);");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/AccountsBatchEntries?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm="
                         + dnm + "&bnm=" + bnm + "&p1=\"+p1;}");
    
    scoutln(out, bytesOut, "function sett(code){code=sanitise(code);");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/SupplierSettlementHistoryCreate?unm=" + unm + "&uty=" + uty + "&sid=" + sid + "&men=" + men + "&den=" + den + "&dnm="
                         + dnm + "&bnm=" + bnm + "&p1=Y&p6=N&p2=\"+code+\"&p3=&p4=" + dayBeforeDateToDDMMYY + "&p5=" + dayBeforeDateToDDMMYY + "\";}");

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
  
    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");
    
    int[] hmenuCount = new int[1];
 
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "6035", "",
                          "AuditCreditorOBVerification", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Creditor Opening Balances Verification", "6035", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);
 
    scoutln(out, bytesOut, "<form><table id='page' width=100% border=0>");
    scoutln(out, bytesOut, "<tr id='pageColumn'><td><p>Supplier Code</td><td><p>Supplier Name</td><td align=right><p>Batch Opening Balance"
                         + "</td><td align=right><p>Document Total</td></tr>");

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT SupplierCode, Name, Currency FROM supplier ORDER BY SupplierCode");
            
    String companyCode, companyName, currency;
    String[] cssFormat = new String[1];  cssFormat[0] = "";
    double[] obBaseTotal            = new double[1];  obBaseTotal[0]            = 0.0;
    double[] obBaseTotalShouldBe    = new double[1];  obBaseTotalShouldBe[0]    = 0.0;
        
    while(rs.next())
    {    
      companyCode = rs.getString(1);
      companyName = rs.getString(2);
      currency    = rs.getString(3);
      
      generate(con, stmt2, rs2, out, year, companyCode, companyName, currency, dateTo, dnm, cssFormat, obBaseTotal, obBaseTotalShouldBe, bytesOut);
    }

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>"); 

    obBaseTotal[0] *= -1;

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
 
    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir));  
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void generate(Connection con, Statement stmt, ResultSet rs, PrintWriter out, int year, String companyCode, String companyName, String currency, String dateTo, String dnm, String[] cssFormat, double[] obBaseTotal,
                        double[] obBaseTotalShouldBe, int[] bytesOut) throws Exception
  {
    double[] invoiceBaseTotal = new double[1];  invoiceBaseTotal[0] = 0.0;
    double[] debitBaseTotal   = new double[1];  debitBaseTotal[0]   = 0.0;
    double[] creditBaseTotal  = new double[1];  creditBaseTotal[0]  = 0.0;
    double[] paymentBaseTotal = new double[1];  paymentBaseTotal[0] = 0.0;
    
    double invoiceTotal    = getPurchaseInvoices(con, stmt, rs, companyCode, dateTo, invoiceBaseTotal);
    double debitNoteTotal  = getPurchaseDebitNotes(con, stmt, rs, companyCode, dateTo, debitBaseTotal);
    double salesSalesCreditNoteTotal = getPurchaseCreditNotes(con, stmt, rs, companyCode, dateTo, creditBaseTotal);
    double paymentTotal    = getPayments(con, stmt, rs, companyCode, dateTo, paymentBaseTotal);

    String[] jbCode = new String[1];
    double[] obBaseAmount = new double[1];  obBaseAmount[0] = 0.0;
    double openingBalance = getOpeningBalance(year, companyCode, dnm, jbCode, obBaseAmount);
    openingBalance *= -1;
      
    double total = (generalUtils.doubleDPs(invoiceTotal, '2') + generalUtils.doubleDPs(debitNoteTotal, '2')) - (generalUtils.doubleDPs(salesSalesCreditNoteTotal, '2') + generalUtils.doubleDPs(paymentTotal, '2'));
 
    if(generalUtils.doubleDPs(openingBalance, '2') != generalUtils.doubleDPs(total, '2'))
    {
      if(cssFormat[0].equals("line1")) cssFormat[0] = "line2"; else cssFormat[0] = "line1";
              
      scoutln(out, bytesOut, "<tr id='" + cssFormat[0] + "'><td><p><a href=\"javascript:viewSupp('" + companyCode + "')\">" + companyCode + "</a></td><td><p>" + companyName + "</td><td align=right><p>" + currency
                           + " <a href=\"javascript:viewJB('" + jbCode[0] + "')\">" + generalUtils.formatNumeric(openingBalance, '2') + "</a></td><td align=right><p>"
                           + currency + " <a href=\"javascript:sett('" + companyCode + "')\">" + generalUtils.formatNumeric(total, '2') + "</a></td></tr>");
    }

    obBaseTotal[0]         += generalUtils.doubleDPs(obBaseAmount[0], '2');
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private double getOpeningBalance(int year, String companyCode, String dnm, String[] jbCode, double[] baseAmount) throws Exception
  {
    double total = 0.0;
    
    Connection accCon  = null;
    Statement  accStmt = null;
    ResultSet  accRs   = null;
    
    try
    {
      String uName = directoryUtils.getMySQLUserName();
      String pWord = directoryUtils.getMySQLPassWord();

      accCon = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_accounts_" + year + "?user=" + uName + "&password=" + pWord);

      accStmt = accCon.createStatement();

      accRs = accStmt.executeQuery("SELECT t2.Amount, t2.DrCr, t2.BaseAmount, t2.Code FROM joubatchl AS t2 INNER JOIN joubatch AS t1 ON t1.Code = t2.Code WHERE t1.Type = 'C' AND t2.AccCode = '" + companyCode + "' AND t1.OpeningBalances = 'Y'");

      double amount, baseAmt;
      String drCr;
            
      while(accRs.next())
      {    
        amount    = generalUtils.doubleDPs(generalUtils.doubleFromStr(accRs.getString(1)), '2');
        drCr      = accRs.getString(2);
        baseAmt   = generalUtils.doubleDPs(generalUtils.doubleFromStr(accRs.getString(3)), '2');
        jbCode[0] = accRs.getString(4);

        if(drCr.equals("D"))
          total += amount;
        else
        {
          total -= amount;
          baseAmt *= -1;
        }

        baseAmount[0] += baseAmt;
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
    
    return total;
  }
   
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private double getPayments(Connection con, Statement stmt, ResultSet rs, String companyCode, String dateTo, double[] baseTotal) throws Exception
  {
    double total = 0.0;
    
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT TotalAmount, BaseTotalAmount FROM payment WHERE CompanyCode = '" + companyCode + "' AND Status != 'C' AND DatePaid < {d '" + dateTo + "'}");

    while(rs.next())
    {
      total += generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(1)), '2');
      baseTotal[0] += generalUtils.doubleFromStr(generalUtils.doubleDPs(rs.getString(2), '2'));
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    return total;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private double getPurchaseCreditNotes(Connection con, Statement stmt, ResultSet rs, String companyCode, String dateTo, double[] baseTotal) throws Exception
  {
    double total = 0.0;
    
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT TotalTotal, BaseTotalTotal FROM pcredit WHERE CompanyCode = '" + companyCode + "' AND Status != 'C' AND Date < {d '" + dateTo + "'}");

    while(rs.next())
    {    
      total += generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(1)), '2');
      baseTotal[0] += generalUtils.doubleFromStr(rs.getString(2));
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return total;
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private double getPurchaseInvoices(Connection con, Statement stmt, ResultSet rs, String companyCode, String dateTo, double[] baseTotal) throws Exception
  {
    double total = 0.0;
    
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT TotalTotal, BaseTotalTotal FROM pinvoice WHERE CompanyCode = '" + companyCode + "' AND Status != 'C' AND Settled != 'Y' AND Date < {d '" + dateTo + "'}");

    while(rs.next())
    {    
      total += generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(1)), '2');
      baseTotal[0] += generalUtils.doubleFromStr(rs.getString(2));
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return total;
  }
   
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private double getPurchaseDebitNotes(Connection con, Statement stmt, ResultSet rs, String companyCode, String dateTo, double[] baseTotal) throws Exception
  {
    double total = 0.0;
    
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT TotalTotal, BaseTotalTotal FROM pdebit "
                          + "WHERE CompanyCode = '" + companyCode + "' AND Status != 'C' AND Settled != 'Y' AND Date < {d '" + dateTo + "'}");

    while(rs.next())
    {    
      total += generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(1)), '2');
      baseTotal[0] += generalUtils.doubleFromStr(rs.getString(2));
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return total;
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
