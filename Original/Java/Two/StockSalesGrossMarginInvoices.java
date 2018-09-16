// =======================================================================================================================================================================================================
// System: ZaraStar: Sales Analytics: Stock Sales - Gross Margin - invoices
// Module: StockSalesGrossMarginInvoices.java
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

public class StockSalesGrossMarginInvoices extends HttpServlet
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
      p1  = req.getParameter("p1"); // monthFrom
      p2  = req.getParameter("p2"); // monthTo
      p3  = req.getParameter("p3"); // mfr (optional)
      p4  = req.getParameter("p4"); // type (optional), M (mfr), C (cust), S (salesperson), I (itemCode), D (date range), null (all)
      p5  = req.getParameter("p5"); // dateFrom (optional)
      p6  = req.getParameter("p6"); // dateTo (optional)

      if(p3 == null) p3 = "";
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "StockSalesGrossMarginInvoices", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1204, bytesOut[0], 0, "ERR:" + p1 + ":" + p2 + ":" + p3);
      if(out != null) out.flush(); 
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1, String p2, String p3, String p4, String p5, String p6, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 1204, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "StockSalesGrossMarginInvoices", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1204, bytesOut[0], 0, "ACC:" + p1 + ":" + p2 + ":" + p3);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "StockSalesGrossMarginInvoices", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1204, bytesOut[0], 0, "SID:" + p1 + ":" + p2 + ":" + p3);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    set(con, stmt, rs, out, req, p1, p2, p3, p4, p5, p6, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 1204, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1 + ":" + p2 + ":" + p3);
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String dateFrom, String dateTo, String mfrOrCustCodeOrSalesPersonOrItemCode, String filterType, String dateRangeFrom, String dateRangeTo,
                   String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Stock Sales: Gross Margin</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4067, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewInvoice(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/SalesInvoicePage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    scoutln(out, bytesOut, "function cos(code){var p1=sanitise(code);");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/AccountsWACDerivation?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+p1;}");

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

    drawingUtils.outputPageFrame(con, stmt, rs, out, req, "1204", "", "StockSalesGrossMarginInvoices", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount, bytesOut);

    drawingUtils.drawTitle(out, false, false, "StockSalesGrossMarginInvoices", "", "Stock Sales: Gross Margin Analysis: " + dateFrom + " to " + dateTo, "1204", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<form><table id=\"page\" width=100% border=0>");

    String s;
    if(filterType.length() != 0)
      s = mfrOrCustCodeOrSalesPersonOrItemCode;
    else s = "All";

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=8><p>Invoices for " + s + ", Ordered by Invoice Code</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td></td></tr><tr id=\"pageColumn\">");

    String baseCurrency = accountsUtils.getBaseCurrency(con, stmt, rs, dnm, localDefnsDir, defnsDir);

    scoutln(out, bytesOut, "<td><p>Invoice Code</td>");
    scoutln(out, bytesOut, "<td><p>Date</td>");
    scoutln(out, bytesOut, "<td><p>Company Code</td>");
    scoutln(out, bytesOut, "<td><p>SalesPerson</td>");
    scoutln(out, bytesOut, "<td align=right><p>" + baseCurrency + " Sale Amount</td>");
    scoutln(out, bytesOut, "<td align=right><p>" + baseCurrency + " Cost of Sale</td>");
    scoutln(out, bytesOut, "<td align=right><p>" + baseCurrency + " Gross Margin</td>");
    scoutln(out, bytesOut, "<td align=right><p>Gross Margin % (CoS)</td>");
    scoutln(out, bytesOut, "<td align=right><p>Gross Margin % (Sale)</td></tr>");

    String[] cssFormat = new String[1];  cssFormat[0] = "line1";
    int[] oCount = new int[1];  oCount[0] = 1;
    double[] gmGrandTotal   = new double[1];  gmGrandTotal[0] = 0.0;
    double[] amtGrandTotal  = new double[1];  amtGrandTotal[0] = 0.0;
    double[] cosGrandTotal  = new double[1];  cosGrandTotal[0] = 0.0;

    String tmpTable = unm + "StockSalesGrossMargin_tmp";

    forInvoices(con, stmt, rs, out, mfrOrCustCodeOrSalesPersonOrItemCode, filterType, dateRangeFrom, dateRangeTo, tmpTable, cssFormat, oCount, gmGrandTotal, amtGrandTotal, cosGrandTotal, bytesOut);

    if(oCount[0] == 0) oCount[0] = 1;

    scoutln(out, bytesOut, "<tr><td colspan=4><p><b>TOTALS:</td>");
    scoutln(out, bytesOut, "<td align=right><p><b>" + generalUtils.formatNumeric(amtGrandTotal[0], '2') + "</td>");
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
  private void forInvoices(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String mfrOrCustCodeOrSalesPersonOrItemCode, String filterType, String dateRangeFrom, String dateRangeTo, String tmpTable, String[] cssFormat, int[] oCount,
                           double[] gmGrandTotal, double[] amtGrandTotal, double[] cosGrandTotal, int[] bytesOut) throws Exception
  {
    String where = "";
    if(filterType.equals("M"))
      where = " WHERE Manufacturer = '" + generalUtils.sanitiseForSQL(mfrOrCustCodeOrSalesPersonOrItemCode) + "'";
    else
    if(filterType.equals("C"))
      where = " WHERE CompanyCode = '" + generalUtils.sanitiseForSQL(mfrOrCustCodeOrSalesPersonOrItemCode) + "'";
    else
    if(filterType.equals("S"))
      where = " WHERE SalesPerson = '" + generalUtils.sanitiseForSQL(mfrOrCustCodeOrSalesPersonOrItemCode) + "'";
    else
    if(filterType.equals("I"))
      where = " WHERE ItemCode = '" + generalUtils.sanitiseForSQL(mfrOrCustCodeOrSalesPersonOrItemCode) + "'";
    else
    if(filterType.equals("D"))
      where = " WHERE Date >= {d '" + dateRangeFrom + "'} AND Date <= {d '" + dateRangeTo + "'}";

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT InvoiceCode, Date, CompanyCode, SalesPerson, ItemCode, Manufacturer, Quantity, GrossMargin, GrossMarginPC, GrossMarginPC2, Amount, CostOfSale FROM " + tmpTable + where + " ORDER BY InvoiceCode");

    double gm, gmpc, gmpc2, amt, cos, gmTotal, gmpcTotal, gmpcTotal2, amtTotal, cosTotal;
    String invoiceCode, companyCode, salesPerson, date, lastInvoiceCode = "", lastDate = "", lastCompanyCode = "", lastSalesPerson = "";

    gmTotal = gmpcTotal = gmpcTotal2 = amtTotal = cosTotal = 0;

    while(rs.next())
    {
      invoiceCode = rs.getString(1);
      date        = rs.getString(2);
      companyCode = rs.getString(3);
      salesPerson = rs.getString(4);
      gm          = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(8)), '2');
      gmpc        = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(9)), '2');
      gmpc2       = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(10)), '2');
      amt         = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(11)), '2');
      cos         = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(12)), '2');

      if(lastInvoiceCode.length() > 0 && ! invoiceCode.equals(lastInvoiceCode))
      {
        outputLine(out, lastInvoiceCode, lastDate, lastCompanyCode, lastSalesPerson, cssFormat, gmTotal, gmpcTotal, gmpcTotal2, amtTotal, cosTotal, bytesOut);
        gmTotal = gmpcTotal = amtTotal = cosTotal = 0;
      }
      
      lastInvoiceCode = invoiceCode;
      lastDate        = date;
      lastCompanyCode = companyCode;
      lastSalesPerson = salesPerson;

      gmTotal    += gm;
      gmpcTotal  += gmpc;
      gmpcTotal2 += gmpc2;
      amtTotal   += amt;
      cosTotal   += cos;

      gmGrandTotal[0]   += gm;
      amtGrandTotal[0]  += amt;
      cosGrandTotal[0]  += cos;

      ++oCount[0];
    }

    if(lastInvoiceCode.length() > 0)
      outputLine(out, lastInvoiceCode, lastDate, lastCompanyCode, lastSalesPerson, cssFormat, gmTotal, gmpcTotal, gmpcTotal2, amtTotal, cosTotal, bytesOut);

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void outputLine(PrintWriter out, String lastInvoiceCode, String date, String companyCode, String salesPerson, String[] cssFormat, double gmTotal, double gmpcTotal, double gmpcTotal2, double amtTotal, double cosTotal, int[] bytesOut)
                          throws Exception
  {
    if(cssFormat[0].equals("line1")) cssFormat[0] = "line2"; else cssFormat[0] = "line1";

    scoutln(out, bytesOut, "<tr id=\"" + cssFormat[0] + "\">");

    scoutln(out, bytesOut, "<td><p><a href=\"javascript:viewInvoice('" + lastInvoiceCode + "')\">" + lastInvoiceCode + "</a></td>");

    scoutln(out, bytesOut, "<td><p>" + date + "</td>");

    scoutln(out, bytesOut, "<td><p>" + companyCode + "</td>");

    scoutln(out, bytesOut, "<td nowrap><p>" + salesPerson + "</td>");

    scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(amtTotal, '2') + "</td>");

    scoutln(out, bytesOut, "<td align=right><p><a href=\"javascript:cos('" + lastInvoiceCode + "')\">" + generalUtils.formatNumeric(cosTotal, '2') + "</a></td>");

    scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(gmTotal, '2') + "</td>");

    if(cosTotal != 0)
      gmpcTotal = (gmTotal / cosTotal) * 100;
    else gmpcTotal = 0;

    if(amtTotal != 0)
      gmpcTotal2 = (gmTotal / amtTotal) * 100;
    else gmpcTotal2 = 0;

    scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(gmpcTotal, '2') + "</td>");
    scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(gmpcTotal2, '2') + "</td>");

    scout(out, bytesOut, "</tr>");
  }

}
