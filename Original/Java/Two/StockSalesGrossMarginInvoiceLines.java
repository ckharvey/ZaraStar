// =======================================================================================================================================================================================================
// System: ZaraStar: Sales Analytics: Stock Sales - Gross Margin - invoice lines
// Module: StockSalesGrossMarginInvoiceLines.java
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

public class StockSalesGrossMarginInvoiceLines extends HttpServlet
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
  MiscDefinitions miscDefinitions = new MiscDefinitions();

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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "StockSalesGrossMarginInvoiceLines", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1204, bytesOut[0], 0, "ERR:" + p1);
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

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 1204, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "StockSalesGrossMarginInvoiceLines", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1204, bytesOut[0], 0, "ACC:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "StockSalesGrossMarginInvoiceLines", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1204, bytesOut[0], 0, "SID:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    set(con, stmt, rs, out, req, p1, p2, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 1204, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String dateFrom, String dateTo, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir,
                   String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Stock Sales: Gross Margin</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4067, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewInvoice(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/SalesInvoicePage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4001, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewCust(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/CustomerPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p1=\"+p1;}");
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

    drawingUtils.outputPageFrame(con, stmt, rs, out, req, "1204", "", "StockSalesGrossMarginInvoiceLines", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount, bytesOut);

    drawingUtils.drawTitle(out, false, false, "StockSalesGrossMarginInvoiceLines", "", "Stock Sales: Gross Margin Analytis: " + dateFrom + " to " + dateTo, "1204", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<form><table id=\"page\" width=100% border=0>");

    scoutln(out, bytesOut, "<tr><td></td></tr><tr id=\"pageColumn\">");

    String baseCurrency = accountsUtils.getBaseCurrency(con, stmt, rs, dnm, localDefnsDir, defnsDir);

    scoutln(out, bytesOut, "<td><p>Invoice Code</td>");
    scoutln(out, bytesOut, "<td><p>Date</td>");
    scoutln(out, bytesOut, "<td><p>Company Code</td>");
    scoutln(out, bytesOut, "<td><p>Item Code</td>");
    scoutln(out, bytesOut, "<td><p>SalesPerson</td>");
    scoutln(out, bytesOut, "<td><p>Manufacturer</td>");
    scoutln(out, bytesOut, "<td align=right><p>Quantity</td>");
    scoutln(out, bytesOut, "<td align=right><p>" + baseCurrency + " Sale Amount</td>");
    scoutln(out, bytesOut, "<td align=right><p>" + baseCurrency + " Cost of Sale</td>");
    scoutln(out, bytesOut, "<td align=right><p>" + baseCurrency + " Gross Margin</td>");
    scoutln(out, bytesOut, "<td align=right><p>Gross Margin % (CoS)</td>");
    scoutln(out, bytesOut, "<td align=right><p>Gross Margin % (Sale)</td></tr>");

    String[] cssFormat = new String[1];  cssFormat[0] = "line1";
    int[] oCount = new int[1];  oCount[0] = 1;
    double[] gmTotal    = new double[1];  gmTotal[0]    = 0.0;
    double[] gmpcTotal  = new double[1];  gmpcTotal[0]  = 0.0;
    double[] gmpcTotal2 = new double[1];  gmpcTotal2[0] = 0.0;
    double[] amtTotal   = new double[1];  amtTotal[0]   = 0.0;
    double[] cosTotal   = new double[1];  cosTotal[0]   = 0.0;

    char dpOnQuantities = miscDefinitions.dpOnQuantities(con, stmt, rs, "5");

    String tmpTable = unm + "StockSalesGrossMargin_tmp";

    byMonth(con, stmt, rs, out, tmpTable, dpOnQuantities, cssFormat, oCount, gmTotal, gmpcTotal, gmpcTotal2, amtTotal, cosTotal, bytesOut);

    if(oCount[0] == 0) oCount[0] = 1;

    scoutln(out, bytesOut, "<tr><td colspan=7><p><b>TOTALS:</td>");
    scoutln(out, bytesOut, "<td align=right><p><b>" + generalUtils.formatNumeric(amtTotal[0], '2') + "</td>");
    scoutln(out, bytesOut, "<td align=right><p><b>" + generalUtils.formatNumeric(cosTotal[0], '2') + "</td>");
    scoutln(out, bytesOut, "<td align=right><p><b>" + generalUtils.formatNumeric(gmTotal[0], '2') + "</td></tr>");

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
  private void byMonth(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String tmpTable, char dpOnQuantities, String[] cssFormat, int[] oCount, double[] gmTotal, double[] gmpcTotal, double[] gmpcTotal2, double[] amtTotal,
                       double[] cosTotal, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT InvoiceCode, Date, CompanyCode, SalesPerson, ItemCode, Manufacturer, Quantity, GrossMargin, GrossMarginPC, GrossMarginPC2, Amount, CostOfSale FROM " + tmpTable + " ORDER BY InvoiceCode");

    double gm, gmpc, gmpc2, amt, cos;
    String invoiceCode, itemCode, companyCode, salesPerson, mfr, qty, date;

    while(rs.next())
    {
      invoiceCode = rs.getString(1);
      date        = rs.getString(2);
      companyCode = rs.getString(3);
      salesPerson = rs.getString(4);
      itemCode    = rs.getString(5);
      mfr         = rs.getString(6);
      qty         = rs.getString(7);
      gm          = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(8)), '2');
      gmpc        = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(9)), '2');
      gmpc2       = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(10)), '2');
      amt         = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(11)), '2');
      cos         = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(12)), '2');

      gmTotal[0]   += gm;
      gmpcTotal[0] += gmpc;
      gmpcTotal2[0] += gmpc2;
      amtTotal[0]  += amt;
      cosTotal[0]  += cos;

      if(cssFormat[0].equals("line1")) cssFormat[0] = "line2"; else cssFormat[0] = "line1";

      scoutln(out, bytesOut, "<tr id=\"" + cssFormat[0] + "\">");

      scoutln(out, bytesOut, "<td><p><a href=\"javascript:viewInvoice('" + invoiceCode + "')\">" + invoiceCode + "</a></td>");

      scoutln(out, bytesOut, "<td><p>" + date + "</td>");

      scoutln(out, bytesOut, "<td><p>" + companyCode + "</td>");

      scoutln(out, bytesOut, "<td nowrap><p>" + salesPerson + "</td>");

      scoutln(out, bytesOut, "<td><p>" + itemCode + "</td>");

      scoutln(out, bytesOut, "<td><p>" + mfr + "</td>");

      scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(qty, dpOnQuantities) + "</td>");

      scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(amt, '2') + "</td>");

      scoutln(out, bytesOut, "<td align=right><p><a href=\"javascript:cos('" + invoiceCode + "')\">" + generalUtils.formatNumeric(cos, '2') + "</a></td>");

      scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(gm, '2') + "</td>");

      if(cos != 0)
        gmpc = (gm / cos) * 100;
      else gmpc = 0;

      if(amt != 0)
        gmpc2 = (gm / amt) * 100;
      else gmpc2 = 0;

      scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(gmpc, '2') + "</td>");
      scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(gmpc2, '2') + "</td>");

      scout(out, bytesOut, "</tr>");

      ++oCount[0];
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

}
