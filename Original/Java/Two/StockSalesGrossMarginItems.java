// =======================================================================================================================================================================================================
// System: ZaraStar: Sales Analytics: Stock Sales - Gross Margin - items
// Module: StockSalesGrossMarginItems.java
// Author: C.K.Harvey
// Copyright (c) 2001-09 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
//
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;
import java.io.*;

public class StockSalesGrossMarginItems extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  DrawingUtils drawingUtils = new DrawingUtils();
  AccountsUtils accountsUtils = new AccountsUtils();
  Inventory inventory = new Inventory();
  DefinitionTables definitionTables = new DefinitionTables();
  MiscDefinitions miscDefinitions = new MiscDefinitions();

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
      directoryUtils.setContentHeaders2(res);
      out = res.getWriter();

      unm = req.getParameter("unm");
      sid = req.getParameter("sid");
      uty = req.getParameter("uty");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");
      p1  = req.getParameter("p1"); // monthFrom
      p2  = req.getParameter("p2"); // monthTo
      p3  = req.getParameter("p3"); // mfr (optional)
      p4  = req.getParameter("p4"); // orderedBy

      if(p3 == null) p3 = "";
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "StockSalesGrossMarginItems", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1204, bytesOut[0], 0, "ERR:" + p1 + ":" + p2 + ":" + p3);
      if(out != null) out.flush(); 
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1, String p2, String p3, String p4, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null, stmt2 = null;
    ResultSet rs   = null, rs2   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 1204, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "StockSalesGrossMarginItems", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1204, bytesOut[0], 0, "ACC:" + p1 + ":" + p2 + ":" + p3);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "StockSalesGrossMarginItems", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1204, bytesOut[0], 0, "SID:" + p1 + ":" + p2 + ":" + p3);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    set(con, stmt, stmt2, rs, rs2, out, req, p1, p2, p3, p4, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 1204, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1 + ":" + p2 + ":" + p3);
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, HttpServletRequest req, String dateFrom, String dateTo, String mfr, String orderedBy, String unm, String sid, String uty,
                   String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Stock Sales: Gross Margin</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    scoutln(out, bytesOut, "function invs(code){var p3=sanitise(code);");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/StockSalesGrossMarginInvoices?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=" + dateFrom + "&p2=" + dateTo + "&p3=\"+p3+\"&p4=I&bnm=" + bnm + "\";}");

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

    drawingUtils.outputPageFrame(con, stmt, rs, out, req, "1204", "", "StockSalesGrossMarginItems", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount, bytesOut);

    drawingUtils.drawTitle(out, false, false, "StockSalesGrossMarginItems", "", "Stock Sales: Gross Margin Analysis: " + dateFrom + " to " + dateTo, "1204", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    String baseCurrency = accountsUtils.getBaseCurrency(con, stmt, rs, dnm, localDefnsDir, defnsDir);

    scoutln(out, bytesOut, "<form><table id=\"page\" width=100% border=0>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    String order;
    if(orderedBy.equals("A"))
      order = baseCurrency + " Sale Amount";
    else
    if(orderedBy.equals("Q"))
      order = "Quantity";
    else order = baseCurrency + " Gross Margin";
    
    scoutln(out, bytesOut, "<tr><td colspan=8><p>Stock Items, Ordered by " + order + ", descending</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td></td></tr><tr id=\"pageColumn\">");

    scoutln(out, bytesOut, "<td><p>Item Code</td>");
    scoutln(out, bytesOut, "<td><p>Manufacturer</td>");
    scoutln(out, bytesOut, "<td><p>Description</td>");
    scoutln(out, bytesOut, "<td align=right><p>Quantity</td>");
    scoutln(out, bytesOut, "<td align=right><p>" + baseCurrency + " Sale Amount</td>");
    scoutln(out, bytesOut, "<td align=right><p>% of Total Sales</td>");
    scoutln(out, bytesOut, "<td align=right><p>" + baseCurrency + " Cost of Sale</td>");
    scoutln(out, bytesOut, "<td align=right><p>" + baseCurrency + " Gross Margin</td>");
    scoutln(out, bytesOut, "<td align=right><p>% of Gross Margin</td>");
    scoutln(out, bytesOut, "<td align=right><p>Gross Margin % (CoS)</td>");
    scoutln(out, bytesOut, "<td align=right><p>Gross Margin % (Sale)</td></tr>");

    String[] cssFormat = new String[1];  cssFormat[0] = "line1";
    int[] oCount = new int[1];  oCount[0] = 1;
    double[] gmGrandTotal   = new double[1];  gmGrandTotal[0] = 0.0;
    double[] gmpcGrandTotal = new double[1];  gmpcGrandTotal[0] = 0.0;
    double[] amtGrandTotal  = new double[1];  amtGrandTotal[0] = 0.0;
    double[] cosGrandTotal  = new double[1];  cosGrandTotal[0] = 0.0;

    String outputTable = unm + "StockSalesGrossMarginItems_tmp";

    directoryUtils.createTmpTable(true, con, stmt2, "ItemCode char(20), Manufacturer char(30), Quantity decimal(19,8), Amount decimal(19,8), CostOfSale decimal(19,8), GrossMargin decimal(19,8), GrossMarginPC decimal(19,8), GrossMarginPC2 decimal(19,8)",
                         "", outputTable);

    String tmpTable = unm + "StockSalesGrossMargin_tmp";

    char dpOnQuantities = miscDefinitions.dpOnQuantities(con, stmt, rs, "5");

    forItems(con, stmt, stmt2, rs, mfr, tmpTable, outputTable, dpOnQuantities, oCount, gmGrandTotal, gmpcGrandTotal, amtGrandTotal, cosGrandTotal);

    outputItems(con, stmt, stmt2, rs, rs2, out, orderedBy, outputTable, dpOnQuantities, amtGrandTotal[0], cosGrandTotal[0], bytesOut);

    directoryUtils.removeTmpTable(con, stmt, outputTable);

    if(oCount[0] == 0) oCount[0] = 1;

    scoutln(out, bytesOut, "<tr><td colspan=5><p><b>TOTALS:</td>");
    scoutln(out, bytesOut, "<td align=right><p><b>" + generalUtils.formatNumeric(amtGrandTotal[0], '2') + "</td><td></td>");
    scoutln(out, bytesOut, "<td align=right><p><b>" + generalUtils.formatNumeric(cosGrandTotal[0], '2') + "</td>");
    scoutln(out, bytesOut, "<td align=right><p><b>" + generalUtils.formatNumeric(gmGrandTotal[0], '2') + "</td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir));
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
  private void forItems(Connection con, Statement stmt, Statement stmt2, ResultSet rs, String mfr, String tmpTable, String  outputTable, char dpOnQuantities, int[] oCount, double[] gmGrandTotal, double[] gmpcGrandTotal, double[] amtGrandTotal,
                        double[] cosGrandTotal) throws Exception
  {
    String where = "";
    if(mfr.length() > 0)
      where = " WHERE Manufacturer = '" + generalUtils.sanitiseForSQL(mfr) + "'";

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT InvoiceCode, Date, CompanyCode, SalesPerson, ItemCode, Manufacturer, Quantity, GrossMargin, GrossMarginPC, GrossMarginPC2, Amount, CostOfSale FROM " + tmpTable + where + " ORDER BY ItemCode");

    double gm, gmpc, amt, cos, gmTotal, gmpcTotal, gmpcTotal2, amtTotal, cosTotal, qty, lastQty = 0;
    String itemCode, lastItemCode = "", lastMfr = "";

    gmTotal = gmpcTotal = gmpcTotal2 = amtTotal = cosTotal = 0;

    while(rs.next())
    {
      itemCode = rs.getString(5);
      mfr      = rs.getString(6);
      qty      = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(7)), dpOnQuantities);
      gm       = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(8)), '2');
      gmpc     = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(9)), '2');
      amt      = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(10)), '2');
      cos      = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(11)), '2');

      if(lastItemCode.length() > 0 && ! itemCode.equals(lastItemCode))
      {
        addToOutputTable(con, stmt2, lastItemCode, lastMfr, lastQty, amtTotal, cosTotal, gmTotal, gmpcTotal, gmpcTotal2, outputTable);
        gmTotal = gmpcTotal = gmpcTotal2 = amtTotal = cosTotal = lastQty = 0;
      }

      lastItemCode = itemCode;
      lastMfr      = mfr;
      lastQty      += qty;

      gmTotal   += gm;
      gmpcTotal += gmpc;
      amtTotal  += amt;
      cosTotal  += cos;

      gmGrandTotal[0]   += gm;
      gmpcGrandTotal[0] += gmpc;
      amtGrandTotal[0]  += amt;
      cosGrandTotal[0]  += cos;

      ++oCount[0];
    }

    if(lastItemCode.length() > 0)
      addToOutputTable(con, stmt2, lastItemCode, lastMfr, lastQty, amtTotal, cosTotal, gmTotal, gmpcTotal, gmpcTotal2, outputTable);

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void outputItems(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, String orderedBy, String outputTable, char dpOnQuantities, double amtGrandTotal, double cosGrandTotal, int[] bytesOut)
                           throws Exception
  {
    String order;

    if(orderedBy.equals("A"))
      order = "Amount";
    else
    if(orderedBy.equals("Q"))
      order = "Quantity";
    else order = "GrossMargin";

    double gm, gmpc, gmpc2, amt, cos;
    String[] cssFormat = new String[1];  cssFormat[0] = "line1";

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT * FROM " + outputTable + " ORDER BY " + order + " DESC");

    String itemCode, mfr;
    double qty;

    while(rs.next())
    {
      itemCode = rs.getString(1);
      mfr      = rs.getString(2);
      qty      = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(3)), '2');
      amt      = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(4)), '2');
      cos      = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(5)), '2');
      gm       = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(6)), '2');
  //    gmpc     = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(7)), '2');

      if(amt != 0.0 && cos != 0.0)
      {
        if(cssFormat[0].equals("line1")) cssFormat[0] = "line2"; else cssFormat[0] = "line1";

        scoutln(out, bytesOut, "<tr id=\"" + cssFormat[0] + "\">");

        scoutln(out, bytesOut, "<td><p><a href=\"javascript:invs('" + itemCode + "')\">" + itemCode + "</a></td>");

        scoutln(out, bytesOut, "<td><p>" + mfr + "</td>");

        scoutln(out, bytesOut, "<td><p>" + inventory.getDescriptionGivenCode(con, stmt2, rs2, itemCode) + "</td>");

        scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(qty, dpOnQuantities) + "</td>");

        scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(amt, '2') + "</td>");

        scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatJustEnough((amt / amtGrandTotal) * 100) + "</td>");

        scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(cos, '2') + "</td>");

        scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(gm, '2') + "</td>");

        scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatJustEnough((gm / (amtGrandTotal - cosGrandTotal)) * 100) + "</td>");

        if(cos != 0)
          gmpc = (gm / cos) * 100;
        else gmpc = 0;

        if(amt != 0)
          gmpc2 = (gm / amt) * 100;
        else gmpc2 = 0;

        scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(gmpc, '2') + "</td>");
        scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(gmpc2, '2') + "</td>");

        scout(out, bytesOut, "</tr>");
      }
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void addToOutputTable(Connection con, Statement stmt, String itemCode, String mfr, double qty, double amt, double cos, double grossMargin, double grossMarginPC, double grossMarginPC2, String tmpTable) throws Exception
  {
    stmt = con.createStatement();

    String q = "INSERT INTO " + tmpTable + " ( ItemCode, Manufacturer, Quantity, Amount, CostOfSale, GrossMargin, GrossMarginPC, GrossMarginPC2 ) VALUES ('" + generalUtils.sanitiseForSQL(itemCode) + "', '" + generalUtils.sanitiseForSQL(mfr) + "', '" + qty
             + "', '" + amt + "', '" + cos + "', '" + grossMargin + "', '" + grossMarginPC + "', '" + grossMarginPC2 + "')";

    stmt.executeUpdate(q);

    if(stmt != null) stmt.close();
  }

}
