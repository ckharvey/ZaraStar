// =======================================================================================================================================================================================================
// System: ZaraStar: Accounts: WAC Derivation
// Module: AccountsWACDerivation.java
// Author: C.K.Harvey
// Copyright (c) 2000-09 Christopher Harvey. All Rights Reserved.
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

public class AccountsWACDerivation extends HttpServlet
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
  PickingList pickingList = new PickingList();
  SalesInvoice salesInvoice = new SalesInvoice();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="", p3="", p4="", p5="";

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
      p1  = req.getParameter("p1"); // invoiceCode
      p2  = req.getParameter("p2"); // invoiceDate
      p3  = req.getParameter("p3"); // itemCode (call from GL)
      p4  = req.getParameter("p4"); // yearStartDate (call from GL)
      p5  = req.getParameter("p5"); // yearEndDate (call from GL)

      if(p2 == null) // call from invoice audit trail
        p2 = "";

      if(p3 == null) // not a call from GL stock ledger
        p3 = p4 = p5 = "";

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, p3, p4, p5, bytesOut);
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

      System.out.println("6006: " + e);
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "AccountsWACDerivation", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6006, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1, String p2, String p3, String p4, String p5, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String workingDir    = directoryUtils.getUserDir('W', dnm, unm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;
    Statement stmt2 = null;
    ResultSet rs2   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 6006, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "AccountsWACDerivation", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6006, bytesOut[0], 0, "ACC:" + p1);
      if(con  != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "AccountsWACDerivation", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6006, bytesOut[0], 0, "SID:" + p1);
      if(con  != null) con.close();
      if(out != null) out.flush();
      return;
    }

    String[] invoiceDate            = new String[1];  invoiceDate[0] = p2;
    String[] financialYearStartDate = new String[1];

    getDates(con, stmt, rs, p1, dnm, invoiceDate, financialYearStartDate);

    set(con, stmt, stmt2, rs, rs2, out, req, p1, invoiceDate[0], financialYearStartDate[0], p3, p4, p5, unm, sid, uty, men, den, dnm, bnm, workingDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 6006, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con  != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, HttpServletRequest req, String invoiceCode, String invoiceDate, String financialYearStartDate, String itemCode,
                   String yearStartDate, String yearEndDate, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String workingDir, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>WAC Derivation</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");
  
    scoutln(out, bytesOut, "function opening(code){var p1=sanitise(code);");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/StockLevelValuesItem?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+p1;}");

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3001, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewItem(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/ProductStockRecord?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p1=\"+p1;}");
    }
  
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4067, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewInv(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/SalesInvoicePage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }
  
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 5026, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewPCN(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/PurchaseCreditNotePage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4101, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewCN(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/SalesCreditNotePage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 5080, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewPInv(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/PurchaseInvoicePage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
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
  
    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\"></head>");

    int[] hmenuCount = new int[1];
 
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "6006", "", true, false, "", "", "", "", "", "AccountsWACDerivation", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    String year = generalUtils.getFromDefnFile(unm, "accounts.dfn", workingDir, "");

    if(itemCode.length() != 0)
      pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "WAC Derivation for: " + itemCode + " (" + yearStartDate + " to " + yearEndDate + ")", "6006", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);
    else
    {
      pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "WAC Derivation for: <a href=\"javascript:viewInv('" + invoiceCode + "')\")>" + invoiceCode + "</a> (" + invoiceDate + ")", "6006", unm, sid, uty, men, den, dnm, bnm, hmenuCount,
                      bytesOut);
    }

    scoutln(out, bytesOut, "<form><table id=\"page\" width=100% border=0>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    String salesAccount = accountsUtils.getSalesAccCode(year, dnm, localDefnsDir, defnsDir);

    char dpOnQuantities = miscDefinitions.dpOnQuantities(con, stmt, rs, "5");

    String baseCurrency = accountsUtils.getBaseCurrency(con, stmt, rs, dnm, localDefnsDir, defnsDir);

    if(itemCode.length() != 0) // call from GL
    {
      String[] cssFormat = new String[1];  cssFormat[0] = "line1";

      calcWAC(con, stmt, rs, out, "", itemCode, yearStartDate, yearEndDate, 0, dpOnQuantities, baseCurrency, dnm, cssFormat, true, bytesOut);
    }
    else onInvoiceLines(con, stmt, stmt2, rs, rs2, out, invoiceCode, financialYearStartDate, invoiceDate, salesAccount, dpOnQuantities, baseCurrency, dnm, bytesOut);
      
    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void onInvoiceLines(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, String invoiceCode, String dateFrom, String dateTo, String salesAccount, char dpOnQuantities, String baseCurrency,
                              String dnm, int[] bytesOut) throws Exception
  {    
    String[] cssFormat = new String[1];  cssFormat[0] = "line1";

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.ItemCode, t2.Quantity, t2.SOCode FROM invoicel AS t2 INNER JOIN invoice AS t1 ON t1.InvoiceCode = t2.InvoiceCode WHERE t2.InvoiceCode = '" + invoiceCode + "' AND t2.Category = '" + salesAccount + "'");

    double quantity, total = 0.0;
    String itemCode, plDate, soCode;
    
    while(rs.next())
    {    
      itemCode = rs.getString(1);

      if(itemExists(con, stmt2, rs2, itemCode))
      {      
        quantity = generalUtils.doubleFromStr(rs.getString(2));
        total += calcWAC(con, stmt, rs, out, invoiceCode, itemCode, dateFrom, dateTo, quantity, dpOnQuantities, baseCurrency, dnm, cssFormat, false, bytesOut);
      }
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td><p>Total: " + generalUtils.formatNumeric(total, '2') + "</td></tr>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void outputLine3(PrintWriter out, String itemCode, double quantity, double currentLevel, double currentValue, char dpOnQuantities, String baseCurrency, String cssFormat, int[] bytesOut) throws Exception
  {
    if(cssFormat.equals("line2")) cssFormat = "line1"; else cssFormat = "line2";

    scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\">");

    String s = " item";
    if(quantity != 1)
      s += "s";

    if(currentLevel == 0.0)
      currentLevel = 1;

    scoutln(out, bytesOut, "<td colspan=6><p>ItemCode " + itemCode + ": " + generalUtils.formatNumeric(quantity, dpOnQuantities) + s + " required at " + generalUtils.formatNumeric((currentValue / currentLevel), '2') + " ("
                           + generalUtils.formatNumeric(currentValue, '2') + " / " + generalUtils.formatNumeric(currentLevel, dpOnQuantities) + ") = " + baseCurrency + " " + generalUtils.formatNumeric(((currentValue / currentLevel) * quantity), '2') + "</td></tr>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
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

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void calcStockOpenings(Connection con, Statement stmt, ResultSet rs, String itemCode, String dnm, double[] numItemsPurchased, double[] closingCost) throws Exception
  {
    Statement stmt2 = null;
    ResultSet rs2   = null;

    if(miscDefinitions.inventoryCostingMethod(con, stmt, rs).equals("WAC"))
    {
      String[] yearStartDate = new String[1];
      String[] yearEndDate   = new String[1];

      closingCost[0] = generalUtils.doubleDPs(inventory.getWAC(con, stmt2, rs2, itemCode, "2008-01-01", "2008-12-31", dnm, numItemsPurchased), '2');
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private double calcWAC(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String thisInvoiceCode, String itemCode, String dateFrom, String dateTo, double quantity, char dpOnQuantities, String baseCurrency, String dnm,
                         String[] cssFormat, boolean fromGL, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr><tr id=\"pageColumn\"><td colspan=4><p><b>Item Code: " + itemCode + "</td></tr>");

    scoutln(out, bytesOut, "<tr id=\"pageColumn\"><td></td><td colspan=4 align=center><p><b>Purchase Invoice</td><td colspan=4 align=center><p><b>Sales Invoice</td><td colspan=3 align=center><p><b>Totals</td></tr>");
    scoutln(out, bytesOut, "<tr id=\"pageColumn\"><td></td><td><p>Code</td><td align=center><p>Quantity</td><td align=center><p>" + baseCurrency + " Unit Price</td><td align=center><p>" + baseCurrency + " Amount</td>"
                         + "<td><p>Code</td><td align=center><p>Quantity</td><td align=center><p>" + baseCurrency + " Unit Price</td><td align=center><p>" + baseCurrency + " Amount</td>"
                         + "<td align=center><p>Quantity</td><td align=center><p>" + baseCurrency + " Unit Price</td><td align=center><p>" + baseCurrency + " Amount</td></tr>");
    cssFormat[0] = "line2";

    double[] openingLevel  = new double[1];
    double[] openingWAC    = new double[1];

    inventory.getWACDetailsGivenCode(con, stmt, rs, itemCode, openingLevel, openingWAC, dateFrom);

    double currentLevel = openingLevel[0];
    double currentValuation = openingLevel[0] * openingWAC[0];

    outputLine4(out, 'P', dateFrom, itemCode, "Opening", openingLevel[0], openingWAC[0], currentValuation, "", 0, 0, 0, currentLevel, openingWAC[0], currentValuation, dpOnQuantities, cssFormat, bytesOut);

    byte[] list = new byte[1000];  list[0] = '\000';
    int[] listLen = new int[1];  listLen[0] = 1000;

    String yearEndDate = accountsUtils.getAccountingYearEndDateForADate(con, stmt, rs, dateFrom, dnm);

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.Quantity, t2.Amount, t1.Date, t1.InvoiceCode, t1.Currency, t2.UnitPrice, t1.Rate FROM pinvoicel AS t2 INNER JOIN pinvoice AS t1 ON t1.InvoiceCode = t2.InvoiceCode WHERE t2.ItemCode = '" + itemCode
                         + "' AND t1.Status != 'C' AND t2.PODate != {d '1970-01-01'} AND t2.PODate < {d '" + dateFrom + "'}    AND t1.Date >= {d '" + dateFrom + "'}    AND t1.Date <= {d '" + dateTo + "'} AND t1.Date <= {d '" + yearEndDate + "'}");

    while(rs.next())
      list = addToTmp("PI", generalUtils.doubleFromStr(rs.getString(1)), generalUtils.doubleFromStr(rs.getString(2)), rs.getString(3), rs.getString(4), rs.getString(5), generalUtils.doubleFromStr(rs.getString(6)), generalUtils.doubleFromStr(rs.getString(7)), list, listLen);

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();


    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.Quantity, t2.Amount, t2.PODate, t1.InvoiceCode, t1.Currency, t2.UnitPrice, t1.Rate FROM pinvoicel AS t2 INNER JOIN pinvoice AS t1 ON t1.InvoiceCode = t2.InvoiceCode WHERE t2.ItemCode = '" + itemCode
                         + "' AND t1.Status != 'C' AND t2.PODate != {d '1970-01-01'} AND t2.PODate < {d '" + dateFrom + "'} AND t1.Date > {d '" + dateTo + "'} AND t1.Date <= {d '" + yearEndDate + "'}");

    while(rs.next())
      list = addToTmp("PI", generalUtils.doubleFromStr(rs.getString(1)), generalUtils.doubleFromStr(rs.getString(2)), rs.getString(3), rs.getString(4), rs.getString(5), generalUtils.doubleFromStr(rs.getString(6)), generalUtils.doubleFromStr(rs.getString(7)), list, listLen);

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();


    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.Quantity, t2.Amount, t2.PODate, t1.InvoiceCode, t1.Currency, t2.UnitPrice, t1.Rate FROM pinvoicel AS t2 INNER JOIN pinvoice AS t1 ON t1.InvoiceCode = t2.InvoiceCode WHERE t2.ItemCode = '" + itemCode
                         + "' AND t1.Status != 'C' AND t2.PODate != {d '1970-01-01'} AND t2.PODate >= {d '" + dateFrom + "'} AND t1.Date > {d '" + dateTo + "'} AND t1.Date <= {d '" + yearEndDate + "'}");

    while(rs.next())
      list = addToTmp("PI", generalUtils.doubleFromStr(rs.getString(1)), generalUtils.doubleFromStr(rs.getString(2)), rs.getString(3), rs.getString(4), rs.getString(5), generalUtils.doubleFromStr(rs.getString(6)), generalUtils.doubleFromStr(rs.getString(7)), list, listLen);

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();


    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.Quantity, t2.Amount, t1.Date, t1.InvoiceCode, t1.Currency, t2.UnitPrice, t1.Rate FROM pinvoicel AS t2 INNER JOIN pinvoice AS t1 ON t1.InvoiceCode = t2.InvoiceCode WHERE t2.ItemCode = '" + itemCode
                         + "' AND t1.Status != 'C' AND t2.PODate != {d '1970-01-01'} AND t2.PODate >= {d '" + dateFrom + "'} AND t1.Date <= {d '" + dateTo + "'} AND t1.Date <= {d '" + yearEndDate + "'}");

    while(rs.next())
      list = addToTmp("PI", generalUtils.doubleFromStr(rs.getString(1)), generalUtils.doubleFromStr(rs.getString(2)), rs.getString(3), rs.getString(4), rs.getString(5), generalUtils.doubleFromStr(rs.getString(6)), generalUtils.doubleFromStr(rs.getString(7)), list, listLen);

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();



    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.Quantity, t2.Amount, t1.Date, t1.InvoiceCode, t1.Currency, t2.UnitPrice, t1.Rate  FROM pinvoicel AS t2 INNER JOIN pinvoice AS t1 ON t1.InvoiceCode = t2.InvoiceCode WHERE t2.ItemCode = '" + itemCode
                         + "' AND t1.Status != 'C' AND t2.PODate = {d '1970-01-01'} AND t1.Date >= {d '" + dateFrom + "'} AND t1.Date <= {d '" + dateTo + "'}");

    while(rs.next())
      list = addToTmp("PI", generalUtils.doubleFromStr(rs.getString(1)), generalUtils.doubleFromStr(rs.getString(2)), rs.getString(3), rs.getString(4), rs.getString(5), generalUtils.doubleFromStr(rs.getString(6)), generalUtils.doubleFromStr(rs.getString(7)), list, listLen);

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();






    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.Quantity, t2.Amount, t1.Date, t1.PCNCode, t1.Currency, t2.UnitPrice, t1.Rate FROM pcreditl AS t2 INNER JOIN pcredit AS t1 ON t1.PCNCode = t2.PCNCode WHERE t2.ItemCode = '" + itemCode
                         + "' AND t1.Status != 'C' AND t1.Date >= {d '" + dateFrom + "'} AND t1.Date <= {d '" + dateTo + "'}");

    while(rs.next())
      list = addToTmp("PC", generalUtils.doubleFromStr(rs.getString(1)), generalUtils.doubleFromStr(rs.getString(2)), rs.getString(3), rs.getString(4), rs.getString(5), generalUtils.doubleFromStr(rs.getString(6)), generalUtils.doubleFromStr(rs.getString(7)), list, listLen);

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.Quantity, t2.Amount, t1.Date, t1.InvoiceCode, t1.Currency, t2.UnitPrice, t1.Rate FROM invoicel AS t2 INNER JOIN invoice AS t1 ON t1.InvoiceCode = t2.InvoiceCode WHERE t2.ItemCode = '" + itemCode
                         + "' AND t1.Status != 'C' AND t1.Date >= {d '" + dateFrom + "'} AND t1.Date < {d '" + dateTo + "'} AND t1.InvoiceCode != '" + thisInvoiceCode + "'");

    while(rs.next())
      list = addToTmp("SI", generalUtils.doubleFromStr(rs.getString(1)), generalUtils.doubleFromStr(rs.getString(2)), rs.getString(3), rs.getString(4), rs.getString(5),
              generalUtils.doubleFromStr(rs.getString(6)), generalUtils.doubleFromStr(rs.getString(7)), list, listLen);

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.Quantity, t2.Amount, t1.Date, t2.InvoiceCode, t1.Currency, t2.UnitPrice, t1.Rate FROM creditl AS t2 INNER JOIN credit AS t1 ON t1.CNCode = t2.CNCode WHERE t2.ItemCode = '" + itemCode
                         + "' AND t1.Status != 'C' AND t1.Date >= {d '" + dateFrom + "'} AND t1.Date < {d '" + dateTo + "'} AND t2.InvoiceCode != '" + thisInvoiceCode + "' AND t2.CostOfSaleAdjustment = 'Y'");
                         // note: if SCN is for a salesprice discount then it can be ignored

    while(rs.next())
      list = addToTmp("SC", generalUtils.doubleFromStr(rs.getString(1)), generalUtils.doubleFromStr(rs.getString(2)), rs.getString(3), rs.getString(4), rs.getString(5), generalUtils.doubleFromStr(rs.getString(6)), generalUtils.doubleFromStr(rs.getString(7)), list, listLen);

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    String docType, qtyStr, amtStr, dateAndType, docCode, currency, unitPriceStr, date, rateStr;
    double qty, amt, wacAtInvoiceTime, rate;
    double currentWAC = openingWAC[0];
    double unitPrice;

    byte[] entry = new byte[1000]; // plenty
    int x, y, len, entryNum = 0;

    while(generalUtils.getListEntryByNum(entryNum++, list, entry))
    {
      x = 0;
      len = generalUtils.lengthBytes(entry, 0);
      dateAndType ="";
      while(x < len && entry[x] != '\002') // just-in-case
        dateAndType += (char)entry[x++];
      y = dateAndType.length();
      docType = "" + dateAndType.charAt(y - 2) + dateAndType.charAt(y - 1);
      date = dateAndType.substring(0, (y - 3));
      ++x;
      qtyStr = "";
      while(x < len && entry[x] != '\002') // just-in-case
        qtyStr += (char)entry[x++];
      ++x;
      amtStr = "";
      while(x < len && entry[x] != '\002') // just-in-case
        amtStr += (char)entry[x++];
      ++x;
      docCode = "";
      while(x < len && entry[x] != '\002') // just-in-case
        docCode += (char)entry[x++];
      ++x;
      currency = "";
      while(x < len && entry[x] != '\002') // just-in-case
        currency += (char)entry[x++];
      ++x;
      unitPriceStr = "";
      while(x < len && entry[x] != '\002') // just-in-case
        unitPriceStr += (char)entry[x++];
      ++x;
      rateStr = "";
      while(x < len && entry[x] != '\002') // just-in-case
        rateStr += (char)entry[x++];

      qty       = generalUtils.doubleDPs(generalUtils.doubleFromStr(qtyStr), '2');
      amt       = generalUtils.doubleDPs(generalUtils.doubleFromStr(amtStr), '2');
      rate      = generalUtils.doubleFromStr(rateStr);
      unitPrice = (generalUtils.doubleDPs((generalUtils.doubleFromStr(unitPriceStr) * rate), '4'));

      if(docType.equals("PI"))
      {
        currentLevel     += qty;
        currentValuation += amt;

        if(currentLevel != 0)
          currentWAC = (currentValuation / currentLevel);

        outputLine4(out, 'P', date, itemCode, docCode, qty, unitPrice, amt, "", 0, 0, 0, currentLevel, currentWAC, currentValuation, dpOnQuantities, cssFormat, bytesOut);
      }
      else
      if(docType.equals("PC"))
      {
        currentLevel -= qty; // need to know if it's a discount or goods returned... assumed goods returned  FIXME
        currentValuation -= amt;
        if(currentLevel != 0)
          currentWAC = (currentValuation / currentLevel);

        outputLine4(out, 'C', date, itemCode, docCode, qty, unitPrice, amt, "", 0, 0, 0, currentLevel, currentWAC, currentValuation, dpOnQuantities, cssFormat, bytesOut);
      }
      else
      if(docType.equals("SI"))
      {
        // update the WAC used on the SI back into the results list (in case it should be needed by a future SCN)
        list = repInTmp((entryNum - 1), qty, currentWAC, dateAndType, docCode, currency, unitPrice, rate, list, listLen);

        currentLevel     -= qty;
        currentValuation -= (currentWAC * qty);

        outputLine4(out, 'S', date, itemCode, "", 0, 0, 0, docCode, qty, currentWAC, (currentWAC * qty), currentLevel, currentWAC, currentValuation, dpOnQuantities, cssFormat, bytesOut);
      }
      else
      if(docType.equals("SC"))
      {
        currentLevel += qty;
        wacAtInvoiceTime = wacAtInvoiceTime(docCode, list); // for SCN the docCode is the invoiceCode from the CN line
        currentValuation += (wacAtInvoiceTime * qty);
        if(currentLevel != 0)
          currentWAC = (currentValuation / currentLevel);

        outputLine4(out, 'D', date, itemCode, "", 0, 0, 0, docCode, qty, wacAtInvoiceTime, (wacAtInvoiceTime * qty), currentLevel, currentWAC,
                    currentValuation, dpOnQuantities, cssFormat, bytesOut);
      }
    }

    if(! fromGL)
      outputLine3(out, itemCode, quantity, currentLevel, currentValuation, dpOnQuantities, baseCurrency, cssFormat[0], bytesOut);

    return generalUtils.doubleDPs((currentWAC * quantity), '2');
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private byte[] addToTmp(String docType, double qty, double amt, String date, String docCode, String currency, double unitPrice, double rate, byte[] list, int[] listLen) throws Exception
  {
    String s = date + "-" + docType + "\002" + qty + "\002" + amt + "\002" + docCode + "\002" + currency + "\002" + unitPrice + "\002" + rate + "\002\001";

    byte[] newItem = new byte[s.length() + 1];
    generalUtils.strToBytes(newItem, s);

    list = generalUtils.addToList(true, newItem, list, listLen);

    return list;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private byte[] repInTmp(int entryNum, double qty, double amt, String dateAndType, String docCode, String currency, double unitPrice, double rate, byte[] list, int[] listLen) throws Exception
  {
    String s = dateAndType + "\002" + qty + "\002" + amt + "\002" + docCode + "\002" + currency + "\002" + unitPrice + "\002" + rate + "\002\001";

    byte[] newEntry = new byte[s.length() + 1];
    generalUtils.strToBytes(newEntry, s);

    list = generalUtils.repListEntryByNum(entryNum, newEntry, list, listLen);

    return list;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private double wacAtInvoiceTime(String invoiceCode, byte[] list) throws Exception
  {
    String dateAndType, docType, amtStr, docCode;
    byte[] entry = new byte[1001]; // plenty
    int x, y, len, entryNum = 0;
    while(generalUtils.getListEntryByNum(entryNum++, list, entry))
    {
      x = 0;
      len = generalUtils.lengthBytes(entry, 0);

      dateAndType ="";
      while(x < len && entry[x] != '\002') // just-in-case
        dateAndType += (char)entry[x++];
      y = dateAndType.length();
      docType = "" + dateAndType.charAt(y - 2) + dateAndType.charAt(y - 1);

      if(docType.equals("SI")) // in case we match against the CN entry
      {
        ++x;
        while(x < len && entry[x] != '\002') // just-in-case
          ++x;
        ++x;
        amtStr = "";
        while(x < len && entry[x] != '\002') // just-in-case
         amtStr += (char)entry[x++];
        ++x;
        docCode = "";
        while(x < len && entry[x] != '\002') // just-in-case
          docCode += (char)entry[x++];

        if(docCode.equals(invoiceCode))
          return generalUtils.doubleDPs(generalUtils.doubleFromStr(amtStr), '2');
      }
    }

    return 0.0; // just-in-case
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void outputLine4(PrintWriter out, char type, String date, String itemCode, String piCode, double piQty, double piUnitPrice, double piAmount,
          String iCode, double iQty, double iUnitPrice, double iAmount, double currentLevel,
                           double currentWAC, double currentValue, char dpOnQuantities, String[] cssFormat, int[] bytesOut) throws Exception
  {
    if(cssFormat[0].equals("line2")) cssFormat[0] = "line1"; else cssFormat[0] = "line2";

    scoutln(out, bytesOut, "<tr id=\"" + cssFormat[0] + "\">");
    scoutln(out, bytesOut, "<td><p>" + date + "</td>");

    if(type == 'P')
    {
      if(piCode.equals("Opening"))
        scoutln(out, bytesOut, "<td><p><a href=\"javascript:opening('" + itemCode + "')\")>Opening</a></td>");
      else scoutln(out, bytesOut, "<td><p><a href=\"javascript:viewPInv('" + piCode + "')\")>" + piCode + "</a></td>");

      scoutln(out, bytesOut, "<td align=center><p>" + generalUtils.formatNumeric(piQty, dpOnQuantities) + "</td>");
      scoutln(out, bytesOut, "<td align=center><p>" + generalUtils.formatNumeric(piUnitPrice, '4') + "</td>");
      scoutln(out, bytesOut, "<td align=center><p>" + generalUtils.formatNumeric(piAmount, '2') + "</td>");
    }
    else
    if(type == 'C')
    {
      scoutln(out, bytesOut, "<td><p>PCN for <a href=\"javascript:viewPInv('" + piCode + "')\")>" + piCode + "</a></td>");
      scoutln(out, bytesOut, "<td align=center><p>" + generalUtils.formatNumeric(piQty, dpOnQuantities) + "</td>");
      scoutln(out, bytesOut, "<td align=center><p>" + generalUtils.formatNumeric(piUnitPrice, '4') + "</td>");
      scoutln(out, bytesOut, "<td align=center><p>" + generalUtils.formatNumeric(piAmount, '2') + "</td>");
    }
    else scoutln(out, bytesOut, "<td colspan=4></td>");

    if(type == 'S')
    {
      scoutln(out, bytesOut, "<td><p><a href=\"javascript:viewInv('" + iCode + "')\")>" + iCode + "</a></td>");
      scoutln(out, bytesOut, "<td align=center><p>" + generalUtils.formatNumeric(iQty, dpOnQuantities) + "</td>");
      scoutln(out, bytesOut, "<td align=center><p>" + generalUtils.formatNumeric(iUnitPrice, '4') + "</td>");
      scoutln(out, bytesOut, "<td align=center><p>" + generalUtils.formatNumeric(iAmount, '2') + "</td>");
    }
    else
    if(type == 'D')
    {
      scoutln(out, bytesOut, "<td><p>CN for <a href=\"javascript:viewInv('" + iCode + "')\")>" + iCode + "</a></td>");
      scoutln(out, bytesOut, "<td align=center><p>" + generalUtils.formatNumeric(iQty, dpOnQuantities) + "</td>");
      scoutln(out, bytesOut, "<td align=center><p></td>");
      scoutln(out, bytesOut, "<td align=center><p></td>");
    }
    else scoutln(out, bytesOut, "<td colspan=4></td>");

    scoutln(out, bytesOut, "<td align=center><p>" + generalUtils.formatNumeric(currentLevel, dpOnQuantities) + "</td>");
    scoutln(out, bytesOut, "<td align=center><p>" + generalUtils.formatNumeric(currentWAC, '4') + "</td>");
    scoutln(out, bytesOut, "<td align=center><p>" + generalUtils.formatNumeric(currentValue, '2') + "</td></tr>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void getDates(Connection con, Statement stmt, ResultSet rs, String invoiceCode, String dnm, String[] invoiceDate, String[] financialYearStartDate) throws Exception
  {
    if(invoiceDate[0].length() == 0)
    {
      invoiceDate[0] = "1970-01-01"; // just-in-case

      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT Date FROM invoice WHERE InvoiceCode = '" + invoiceCode + "'");

      if(rs.next()) // just-in-case
        invoiceDate[0] = rs.getString(1);

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }

    financialYearStartDate[0] = accountsUtils.getAccountingYearStartDateForADate(con, stmt, rs, dnm, invoiceDate[0]);
  }

}
