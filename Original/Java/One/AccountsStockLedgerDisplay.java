// =======================================================================================================================================================================================================
// System: ZaraStar: Accounts: Stock Ledger - display
// Module: AccountsStockLedgerDisplay.java
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

public class AccountsStockLedgerDisplay extends HttpServlet
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

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="";

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
      p1  = req.getParameter("p1"); // year
      p2  = req.getParameter("p2"); // itemCode

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, bytesOut);
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

      System.out.println("6005b: " + e);
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "AccountsStockLedgerDisplay", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6005, bytesOut[0], 0, "ERR:" + p1 + ":" + p2);
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1, String p2, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;
    Statement stmt2 = null;
    ResultSet rs2   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 6028, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "AccountsStockLedgerDisplay", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6005, bytesOut[0], 0, "ACC:" + p1 + ":" + p2);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "AccountsStockLedgerDisplay", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6005, bytesOut[0], 0, "SID:" + p1 + ":" + p2);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    String[] yearStartDate = new String[1];
    String[] yearEndDate   = new String[1];

    accountsUtils.getAccountingYearStartAndEndDatesForAYear(con, stmt, rs, p1, dnm, localDefnsDir, defnsDir, yearStartDate, yearEndDate);

    set(con, stmt, stmt2, rs, rs2, out, req, p2, p1, yearStartDate[0], yearEndDate[0], unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 6005, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1 + ":" + p2);
    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, HttpServletRequest req, String itemCode, String year, String yearStartDate, String yearEndDate, String unm, String sid, String uty,
                   String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>WAC Derivation</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    scoutln(out, bytesOut, "function fin(){");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/AccountsWACDerivation?p4=" + yearStartDate + "&p5=" + yearEndDate + "&p3=" + itemCode + "&unm=" + unm + "&uty=" + uty + "&sid=" + sid + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm="
                         + bnm + "\";}");

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3038, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewPL(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/GoodsReceivedPickingList?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3025, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewGRN(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/GoodsReceivedPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

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

    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "6005", "", true, false, "", "", "", "", "", "AccountsStockLedgerDisplay", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Stock Ledger for " + year, "6005", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<form><table id=\"page\" width=100% border=0>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    char dpOnQuantities = miscDefinitions.dpOnQuantities(con, stmt, rs, "5");

    String baseCurrency = accountsUtils.getBaseCurrency(con, stmt, rs, dnm, localDefnsDir, defnsDir);

    String[] cssFormat = new String[1];  cssFormat[0] = "line1";

    process(con, stmt, stmt2, rs, rs2, out, itemCode, yearStartDate, yearEndDate, dpOnQuantities, baseCurrency, cssFormat, bytesOut);

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<td><p><a href=\"javascript:fin()\")>Financial</a></td>");

    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir));
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void outputLine3(PrintWriter out, String itemCode, double quantity, double currentLevel, double currentValue, char dpOnQuantities, String baseCurrency, String cssFormat, int[] bytesOut) throws Exception
  {
    if(cssFormat.equals("line2")) cssFormat = "line1"; else cssFormat = "line2";

    scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\">");

    String s = " item";
    if(quantity != 1)
      s += "s";

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
  private void process(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, String itemCode, String yearStartDate, String yearEndDate, char dpOnQuantities, String baseCurrency, String[] cssFormat,
                       int[] bytesOut) throws Exception
  {
    String[] mfr     = new String[1];
    String[] mfrCode = new String[1];
        
    inventory.getMfrAndMfrCodeGivenItemCode(con, stmt, rs, itemCode, mfr, mfrCode);

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr><tr id=\"pageColumn\"><td colspan=12><p><b>Item Code: <a href=\"javascript:viewItem('" + itemCode + "')\">" + itemCode + "</a></b> &nbsp;&nbsp;" + mfr[0] + " " + mfrCode[0] + "</td></tr>");
    scoutln(out, bytesOut, "<tr id=\"pageColumn\"><td></td><td colspan=4 align=center><p><b>Goods In</td><td colspan=4 align=center><p><b>Goods Out</td><td colspan=3 align=center><p><b>Totals</td></tr>");
    scoutln(out, bytesOut, "<tr id=\"pageColumn\"><td></td><td><p>GRN Code</td><td align=center><p>Quantity</td><td align=center><p>" + baseCurrency + " Unit Price</td><td align=center><p>" + baseCurrency + " Amount</td>"
                         + "<td><p>PL Code</td><td align=center><p>Quantity</td><td align=center><p>" + baseCurrency + " Unit Price</td><td align=center><p>" + baseCurrency + " Amount</td>"
                         + "<td align=center><p>Quantity</td><td align=center><p>" + baseCurrency + " Unit Price</td><td align=center><p>" + baseCurrency + " Amount</td></tr>");
    cssFormat[0] = "line2";

    double[] openingLevel  = new double[1];
    double[] openingWAC    = new double[1];

    inventory.getWACDetailsGivenCode(con, stmt, rs, itemCode, openingLevel, openingWAC, yearStartDate);

    double currentLevel = openingLevel[0];
    double currentValuation = openingLevel[0] * openingWAC[0];

    outputLine4(out, 'G', ' ', yearStartDate, "Opening", openingLevel[0], openingWAC[0], currentValuation, "", 0, 0, 0, currentLevel, openingWAC[0], currentValuation, dpOnQuantities, cssFormat, bytesOut);

    byte[] list = new byte[1000];
    int[] listLen = new int[1];  listLen[0] = 1000;

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.GRCode, t1.DateStockProcessed, t2.Quantity, t2.POCode, t2.POLine, t2.Line FROM grl AS t2 INNER JOIN gr AS t1 ON t1.GRCode = t2.GRCode WHERE t2.ItemCode = '" + itemCode
                         + "' AND t1.Status != 'C' AND t1.DateStockProcessed >= {d '" + yearStartDate + "'} AND t1.DateStockProcessed <= {d '" + yearEndDate + "'}");

    double baseUnitPrice;
    double[] d = new double[1];

    while(rs.next())
    {
      if(hasPurchaseInvoice(con, stmt, rs, rs.getString(1), rs.getString(6), d))
      {
        baseUnitPrice = d[0];
        list = addToTmp("GI", generalUtils.doubleFromStr(rs.getString(3)), baseUnitPrice, rs.getString(2), rs.getString(1), list, listLen);
      }
      else
      {
        baseUnitPrice = getUnitPriceFromPO(con, stmt2, rs2, rs.getString(4), rs.getString(5));
        list = addToTmp("GO", generalUtils.doubleFromStr(rs.getString(3)), baseUnitPrice, rs.getString(2), rs.getString(1), list, listLen);
      }
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.PLCode, t1.Date, t2.QuantityPacked FROM pll AS t2 INNER JOIN pl AS t1 ON t1.PLCode = t2.PLCode WHERE t2.ItemCode = '" + itemCode + "' AND t1.Status != 'C' AND t1.Date >= {d '" + yearStartDate
                         + "'} AND t1.Date <= {d '" + yearEndDate + "'}");

    while(rs.next())
    {
      list = addToTmp("PL", generalUtils.doubleFromStr(rs.getString(3)), 0, rs.getString(2), rs.getString(1), list, listLen);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    String docType, qtyStr, dateAndType, docCode, currency, unitPriceStr, date, rateStr;
    double qty, amt, wacAtInvoiceTime, wacSave, rate;
    double currentWAC   = openingWAC[0];
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

      docCode = "";
      while(x < len && entry[x] != '\002') // just-in-case
        docCode += (char)entry[x++];
      ++x;

      qtyStr = "";
      while(x < len && entry[x] != '\002') // just-in-case
        qtyStr += (char)entry[x++];
      ++x;

      unitPriceStr = "";
      while(x < len && entry[x] != '\002') // just-in-case
        unitPriceStr += (char)entry[x++];
      ++x;

      qty       = generalUtils.doubleDPs(generalUtils.doubleFromStr(qtyStr), '2');
      unitPrice = generalUtils.doubleDPs(generalUtils.doubleFromStr(unitPriceStr), '4');

      amt = generalUtils.doubleDPs((qty * unitPrice), '2');

      if(docType.startsWith("G"))
      {
        currentLevel     += qty;
        currentValuation += amt;

        if(currentLevel != 0)
          currentWAC = (currentValuation / currentLevel);

        outputLine4(out, 'G', docType.charAt(1), date, docCode, qty, unitPrice, (qty * unitPrice), "", 0, 0, 0, currentLevel, currentWAC, currentValuation, dpOnQuantities, cssFormat, bytesOut);
      }
      else
      if(docType.equals("PL"))
      {
        // update the WAC used on the SI back into the results list (in case it should be needed by a future SCN)

        currentLevel     -= qty;
        currentValuation -= (currentWAC * qty);

        outputLine4(out, 'P', ' ', date, "", 0, 0, 0, docCode, qty, currentWAC, (currentWAC * qty), currentLevel, currentWAC, currentValuation, dpOnQuantities, cssFormat, bytesOut);

      }
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private byte[] addToTmp(String docType, double qty, double unitPrice, String date, String docCode, byte[] list, int[] listLen) throws Exception
  {
    String s = date + "-" + docType + "\002" + docCode + "\002" + + qty + "\002" + unitPrice + "\002\001";

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
  private void outputLine4(PrintWriter out, char type, char fromWhich, String date, String grnCode, double grnQty, double grnUnitPrice, double grnAmount, String plCode, double plQty, double plUnitPrice, double plAmount, double currentLevel,
                           double currentWAC, double currentValue, char dpOnQuantities, String[] cssFormat, int[] bytesOut) throws Exception
  {
    if(cssFormat[0].equals("line2")) cssFormat[0] = "line1"; else cssFormat[0] = "line2";

    scoutln(out, bytesOut, "<tr id=\"" + cssFormat[0] + "\">");
    scoutln(out, bytesOut, "<td><p>" + date + "</td>");

    if(type == 'G')
    {
      scoutln(out, bytesOut, "<td><p><a href=\"javascript:viewGRN('" + grnCode + "')\")>" + grnCode + "</a></td>");
      scoutln(out, bytesOut, "<td align=center><p>" + generalUtils.formatNumeric(grnQty, dpOnQuantities) + "</td>");
      scoutln(out, bytesOut, "<td align=center><p>" + generalUtils.formatNumeric(grnUnitPrice, '4') + "</td>");
      if(fromWhich == 'O') // PO
        scoutln(out, bytesOut, "<td align=center><p>" + generalUtils.formatNumeric(grnAmount, '2') + " *PO</td>");
      else scoutln(out, bytesOut, "<td align=center><p>" + generalUtils.formatNumeric(grnAmount, '2') + "</td>");
    }
    else scoutln(out, bytesOut, "<td colspan=4></td>");

    if(type == 'P')
    {
      scoutln(out, bytesOut, "<td><p><a href=\"javascript:viewPL('" + plCode + "')\")>" + plCode + "</a></td>");
      scoutln(out, bytesOut, "<td align=center><p>" + generalUtils.formatNumeric(plQty, dpOnQuantities) + "</td>");
      scoutln(out, bytesOut, "<td align=center><p>" + generalUtils.formatNumeric(plUnitPrice, '4') + "</td>");
      scoutln(out, bytesOut, "<td align=center><p>" + generalUtils.formatNumeric(plAmount, '2') + "</td>");
    }
    else scoutln(out, bytesOut, "<td colspan=4></td>");

    scoutln(out, bytesOut, "<td align=center><p>" + generalUtils.formatNumeric(currentLevel, dpOnQuantities) + "</td>");
    scoutln(out, bytesOut, "<td align=center><p>" + generalUtils.formatNumeric(currentWAC, '4') + "</td>");
    scoutln(out, bytesOut, "<td align=center><p>" + generalUtils.formatNumeric(currentValue, '2') + "</td></tr>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private double getUnitPriceFromPO(Connection con, Statement stmt, ResultSet rs, String poCode, String poLine) throws Exception
  {
    double baseUnitPrice = 0; // just-in-case

    if(poCode.length() > 0)
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT t2.UnitPrice, t1.Rate FROM pol AS t2 INNER JOIN po AS t1 ON t1.POCode = t2.POCode WHERE t2.POCode = '" + poCode + "' AND t2.Line = '" + poLine + "'");

      double unitPrice, rate;

      if(rs.next()) // just-in-case
      {
        unitPrice     = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(1)), '4');
        rate          = generalUtils.doubleFromStr(rs.getString(2));
        baseUnitPrice = generalUtils.doubleDPs((unitPrice * rate), '2');
      }

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }

    return baseUnitPrice;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean hasPurchaseInvoice(Connection con, Statement stmt, ResultSet rs, String grCode, String grLine, double[] baseUnitPrice) throws Exception
  {
    boolean piExists = false;

    if(grCode.length() > 0)
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT t2.UnitPrice, t1.Rate FROM pinvoicel AS t2 INNER JOIN pinvoice AS t1 ON t1.InvoiceCode = t2.InvoiceCode WHERE t2.GRCode = '" + grCode + "' AND t2.GRLine = '" + grLine + "'");

      double unitPrice, rate;

      if(rs.next()) // just-in-case
      {
        unitPrice        = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(1)), '4');
        rate             = generalUtils.doubleFromStr(rs.getString(2));
        baseUnitPrice[0] = generalUtils.doubleDPs((unitPrice * rate), '2');
        piExists = true;
      }

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }

    return piExists;
  }

}
