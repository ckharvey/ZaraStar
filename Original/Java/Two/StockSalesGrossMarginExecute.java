// =======================================================================================================================================================================================================
// System: ZaraStar: Sales Analytics: Stock Sales - Gross Margin
// Module: StockSalesGrossMarginExecute.java
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

public class StockSalesGrossMarginExecute extends HttpServlet
{
  GeneralUtils  generalUtils = new GeneralUtils();
  MessagePage  messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils  pageFrameUtils = new PageFrameUtils();
  DirectoryUtils  directoryUtils = new DirectoryUtils();
  DrawingUtils  drawingUtils = new DrawingUtils();
  AccountsUtils  accountsUtils = new AccountsUtils();
  Inventory inventory = new Inventory();
  DefinitionTables definitionTables = new DefinitionTables();

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
      directoryUtils.setContentHeaders2(res);
      out = res.getWriter();

      unm = req.getParameter("unm");
      sid = req.getParameter("sid");
      uty = req.getParameter("uty");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");
      p1  = req.getParameter("p1");
      p2  = req.getParameter("p2");

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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "StockSalesGrossMargina", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1204, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush(); 
    }
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
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

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 1204, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "StockSalesGrossMargina", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1204, bytesOut[0], 0, "ACC:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "StockSalesGrossMargina", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1204, bytesOut[0], 0, "SID:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    // given month in format: "4-2003"

    int[] mm = new int[1];
    int[] yy = new int[1];

    generalUtils.monthYearStrToYearAndMonth2(p1, mm, yy);
    String dateFrom = yy[0] + "-" + mm[0] + "-01";

    generalUtils.monthYearStrToYearAndMonth2(p2, mm, yy);
    String dateTo   = yy[0] + "-" + mm[0] + "-" + generalUtils.numOfDaysInMonth((short)mm[0], (short)yy[0]);

    set(con, stmt, stmt2, rs, rs2, out, req, dateFrom, dateTo, p1, p2, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 1204, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, HttpServletRequest req, String dateFrom, String dateTo, String p1, String p2, String unm, String sid, String uty, String men,
                   String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Stock Sales: Gross Margin</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    scoutln(out, bytesOut, "function sps(){");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/StockSalesGrossMarginSalespersgr?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=" + dateFrom + "&p2=" + dateTo + "&bnm=" + bnm + "\";}");

    scoutln(out, bytesOut, "function months(){");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/StockSalesGrossMarginMonths?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=" + dateFrom + "&p2=" + dateTo + "&bnm=" + bnm + "\";}");

    scoutln(out, bytesOut, "function items(option){");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/StockSalesGrossMarginItems?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=" + dateFrom + "&p2=" + dateTo + "&p4=\"+option+\"&bnm=" + bnm + "\";}");

    scoutln(out, bytesOut, "function invs(){");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/StockSalesGrossMarginInvoices?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=" + dateFrom + "&p2=" + dateTo + "&bnm=" + bnm + "\";}");

    scoutln(out, bytesOut, "function invls(){");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/StockSalesGrossMarginInvoiceLines?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=" + dateFrom + "&p2=" + dateTo + "&bnm=" + bnm + "\";}");

    scoutln(out, bytesOut, "function mfrs(){");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/StockSalesGrossMarginByManfacturer?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=" + dateFrom + "&p2=" + dateTo + "&bnm=" + bnm + "\";}");

    scoutln(out, bytesOut, "function custs(option){");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/StockSalesGrossMarginByCompany?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=" + dateFrom + "&p2=" + dateTo + "&p3=\"+option+\"&bnm=" + bnm + "\";}");

    scoutln(out, bytesOut, "function chart(){");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/StockSalesGrossMarginCharts?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=" + dateFrom + "&p2=" + dateTo + "&bnm=" + bnm + "\";}");

    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    int[] hmenuCount = new int[1];

    drawingUtils.outputPageFrame(con, stmt, rs, out, req, "1204", "", "StockSalesGrossMargina", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount, bytesOut);

    drawingUtils.drawTitle(out, false, false, "StockSalesGrossMargina", "", "Stock Sales: Gross Margin Analysis: " + dateFrom + " to " + dateTo, "1204", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<form><table id=\"page\" width=100% border=0>");

    scoutln(out, bytesOut, "<tr><td></td></tr><tr id=\"pageColumn\"><td></td>");

    String tmpTable;
    tmpTable = unm + "StockSalesGrossMargin_tmp";

    directoryUtils.createTmpTable(true, con, stmt2, "InvoiceCode char(20), Date date, CompanyCode char(20), SalesPerson char(40), ItemCode char(20), Manufacturer char(30), Quantity decimal(19,8), GrossMargin decimal(19,8), GrossMarginPC decimal(19,8), "
                                         + "GrossMarginPC2 decimal(19,8), Amount decimal(19,8), CostOfSale decimal(19,8)", "docMfrInx ON " + tmpTable + " (Manufacturer)", tmpTable); // need inx????

    String[] financialYearStartMonth = new String[1];
    String[] financialYearEndMonth   = new String[1];
    definitionTables.getAppConfigFinancialYearMonths(con, stmt, rs, dnm, financialYearStartMonth, financialYearEndMonth);

    String startMonth;
    int i = generalUtils.detMonthNumFromMonthName(financialYearStartMonth[0]);
    if(i < 10)
      startMonth = "-0" + i;
    else startMonth = "-" + i;

    forAllSalesInvoices(con, stmt, stmt2, rs, rs2, dateFrom, dateTo, startMonth, tmpTable, dnm, localDefnsDir, defnsDir);

    scoutln(out, bytesOut, "<tr><td><p><a href=\"javascript:months()\">List by Month</a></td></tr>");
    scoutln(out, bytesOut, "<tr><td><p><a href=\"javascript:invs()\">List by Invoice</a></td></tr>");
    scoutln(out, bytesOut, "<tr><td><p><a href=\"javascript:invls()\">List by Invoice Lines</a></td></tr>");
    scoutln(out, bytesOut, "<tr><td><p><a href=\"javascript:mfrs()\">List by Manufacturer</a></td></tr>");

    scoutln(out, bytesOut, "<tr><td><p><a href=\"javascript:items('A')\">List by Stock Item (Ordered by Sales Value)</a></td></tr>");
    scoutln(out, bytesOut, "<tr><td><p><a href=\"javascript:items('M')\">List by Stock Item (Ordered by Gross Margin)</a></td></tr>");
    scoutln(out, bytesOut, "<tr><td><p><a href=\"javascript:items('Q')\">List by Stock Item (Ordered by Quantity)</a></td></tr>");

    scoutln(out, bytesOut, "<tr><td><p><a href=\"javascript:custs('S')\">List by Customer (Ordered by Sales Value)</a></td></tr>");
    scoutln(out, bytesOut, "<tr><td><p><a href=\"javascript:custs('M')\">List by Customer (Ordered by Gross Margin)</a></td></tr>");
    scoutln(out, bytesOut, "<tr><td><p><a href=\"javascript:sps()\">List by SalesPerson</a></td></tr>");
    scoutln(out, bytesOut, "<tr><td><p><a href=\"javascript:chart()\">Chart</a></td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir));
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {
    out.println(str);
    bytesOut[0] += (str.length() + 2);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void forAllSalesInvoices(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String dateFrom, String dateTo, String financialYearStartMonth, String tmpTable, String dnm, String localDefnsDir, String defnsDir)
                                   throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.InvoiceCode, t1.Date, t1.CompanyCode, t1.SalesPerson, t2.ItemCode, t2.Manufacturer, t2.Quantity, t2.Amount, t2.SOCode FROM invoicel AS t2 INNER JOIN invoice AS t1 ON t1.InvoiceCode = t2.InvoiceCode "
                         + "WHERE t1.Status != 'C' AND t1.Date >= {d '" + dateFrom + "'} AND t1.Date <= {d '" + dateTo + "'} ORDER BY t1.Date");

    double amt, gm, gmpc, gmpc2, wac, qty;
    String itemCode, date, financialYearStartDate;

    while(rs.next())
    {
      itemCode = rs.getString(5);

      if(inventory.existsItemRecGivenCode(con, stmt2, rs2, itemCode))
      {
        date = rs.getString(2);

        amt = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(8)), '2');
        qty = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(7)), '2');


        financialYearStartDate = accountsUtils.getAccountingYearForADate(con, stmt2, rs2, generalUtils.convertFromYYYYMMDD(date), dnm, localDefnsDir, defnsDir) + financialYearStartMonth + "-01";

        wac = inventory.getWAC(con, stmt2, rs2, itemCode, financialYearStartDate, date, dnm);

        wac *= qty;

        gm = amt - wac;

        if(wac != 0)
          gmpc = (gm / wac) * 100;
        else gmpc = 0;

        if(amt != 0)
          gmpc2 = (gm / amt) * 100;
        else gmpc2 = 0;

        addToTmpTable(con, stmt2, rs.getString(1), date, rs.getString(3), itemCode, rs.getString(4), rs.getString(6), qty, amt, wac, gm, gmpc, gmpc2, tmpTable);
      }
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void addToTmpTable(Connection con, Statement stmt, String invoiceCode, String date, String companyCode, String itemCode, String salesPerson, String mfr, double qty, double amt, double cos, double grossMargin, double grossMarginPC,
                             double grossMarginPC2, String tmpTable) throws Exception
  {
    stmt = con.createStatement();

    String q = "INSERT INTO " + tmpTable + " ( InvoiceCode, Date, CompanyCode, SalesPerson, ItemCode, Manufacturer, Quantity, GrossMargin, GrossMarginPC, GrossMarginPC2, Amount, CostOfSale ) VALUES ('" + invoiceCode + "', {d '" + date + "'}, '"
            + generalUtils.sanitiseForSQL(companyCode) + "', '" + generalUtils.sanitiseForSQL(salesPerson) + "', '" + generalUtils.sanitiseForSQL(itemCode) + "', '" + generalUtils.sanitiseForSQL(mfr) + "', '" + qty + "', '" + grossMargin + "', '" + grossMarginPC + "', '"
            + grossMarginPC2 + "', '" + amt + "', '" + cos + "')";

    stmt.executeUpdate(q);

    if(stmt != null) stmt.close();
  }

}
