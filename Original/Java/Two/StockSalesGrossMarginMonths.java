// =======================================================================================================================================================================================================
// System: ZaraStar: Sales Analytics: Stock Sales - Gross Margin - months
// Module: StockSalesGrossMarginMonths.java
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

public class StockSalesGrossMarginMonths extends HttpServlet
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
      p1  = req.getParameter("p1"); // monthFrom
      p2  = req.getParameter("p2"); // monthTo

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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "StockSalesGrossMarginMonths", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1204, bytesOut[0], 0, "ERR:" + p1 + ":" + p2);
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
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "StockSalesGrossMarginMonths", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1204, bytesOut[0], 0, "ACC:" + p1 + ":" + p2);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "StockSalesGrossMarginMonths", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1204, bytesOut[0], 0, "SID:" + p1 + ":" + p2);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    set(con, stmt, rs, out, req, p1, p2, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 1204, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1 + ":" + p2);
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String dateFrom, String dateTo, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir,
                   String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Stock Sales: Gross Margin</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    scoutln(out, bytesOut, "function invs(dateFrom,dateTo){");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/StockSalesGrossMarginInvoices?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=" + dateFrom + "&p2=" + dateTo
                         + "&p5=\"+dateFrom+\"&p6=\"+dateTo+\"&p4=D&bnm=" + bnm + "\";}");

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

    drawingUtils.outputPageFrame(con, stmt, rs, out, req, "1204", "", "StockSalesGrossMarginMonths", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount, bytesOut);

    drawingUtils.drawTitle(out, false, false, "StockSalesGrossMarginMonths", "", "Stock Sales: Gross Margin Analysis: " + dateFrom + " to " + dateTo, "1204", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<form><table id=\"page\" width=100% border=0>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=8><p>All Invoices, Ordered by Month</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td></td></tr><tr id=\"pageColumn\">");

    String baseCurrency = accountsUtils.getBaseCurrency(con, stmt, rs, dnm, localDefnsDir, defnsDir);

    scoutln(out, bytesOut, "<td><p>Date Range</td>");
    scoutln(out, bytesOut, "<td align=right><p>" + baseCurrency + " Sale Amount</td>");
    scoutln(out, bytesOut, "<td align=right><p>" + baseCurrency + " Cost of Sale</td>");
    scoutln(out, bytesOut, "<td align=right><p>" + baseCurrency + " Gross Margin</td>");
    scoutln(out, bytesOut, "<td align=right><p>Gross Margin % (CoS)</td>");
    scoutln(out, bytesOut, "<td align=right><p>Gross Margin % (Sale)</td></tr>");

    String[] cssFormat = new String[1];  cssFormat[0] = "line1";

    double[] gmGrandTotal   = new double[1];  gmGrandTotal[0] = 0.0;
    double[] gmpcGrandTotal = new double[1];  gmpcGrandTotal[0] = 0.0;
    double[] amtGrandTotal  = new double[1];  amtGrandTotal[0] = 0.0;
    double[] cosGrandTotal  = new double[1];  cosGrandTotal[0] = 0.0;

    String tmpTable = unm + "StockSalesGrossMargin_tmp";

    // given start month and end month in format: "4-2003"
    int mm = generalUtils.getMonth(generalUtils.encodeFromYYYYMMDD(dateFrom));
    int yy = generalUtils.getYear(generalUtils.encodeFromYYYYMMDD(dateFrom));
    String monthFromStr = mm + "-" + yy;
    mm = generalUtils.getMonth(generalUtils.encodeFromYYYYMMDD(dateTo));
    yy = generalUtils.getYear(generalUtils.encodeFromYYYYMMDD(dateTo));
    String monthToStr = mm + "-" + yy;

    // given start month and end month in format: "4-2003", determine # of months
    int numMonths = generalUtils.numMonths(monthFromStr, monthToStr, localDefnsDir, defnsDir);

    int[] month = new int[1];
    int[] year  = new int[1];

    String dateFromYYYYMMDD, dateToYYYYMMDD, dateFromDDMMYYYY;
    generalUtils.monthYearStrToYearAndMonth2(monthFromStr, month, year);
    for(int x=0;x<numMonths;++x)
    {
      dateFromYYYYMMDD = year[0] + "-" + month[0] + "-01";
      dateFromDDMMYYYY = "01." + month[0] + "." + year[0];

      dateToYYYYMMDD = generalUtils.convertDateToSQLFormat(generalUtils.lastDayOfMonth(dateFromDDMMYYYY, localDefnsDir, defnsDir));

      forInvoices(con, stmt, rs, out, dateFromYYYYMMDD, dateToYYYYMMDD, tmpTable, cssFormat, gmGrandTotal, gmpcGrandTotal, amtGrandTotal, cosGrandTotal, bytesOut);

      if(++month[0] == 13)
      {
        month[0] = 1;
        ++year[0];
      }
    }

    scoutln(out, bytesOut, "<tr><td><p><b>TOTALS:</td>");
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
  private void forInvoices(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String dateFrom, String dateTo, String tmpTable, String[] cssFormat, double[] gmGrandTotal, double[] gmpcGrandTotal, double[] amtGrandTotal,
                           double[] cosGrandTotal, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT GrossMargin, GrossMarginPC, GrossMarginPC2, Amount, CostOfSale FROM " + tmpTable + " WHERE Date >= {d '" + dateFrom + "'} AND Date <= {d '" + dateTo + "'}");

    double gm, gmpc, gmpc2, amt, cos, gmTotal, gmpcTotal, gmpcTotal2, amtTotal, cosTotal;

    gmTotal = gmpcTotal = gmpcTotal2 = amtTotal = cosTotal = 0;

    while(rs.next())
    {
      gm    = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(1)), '2');
      gmpc  = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(2)), '2');
      gmpc2 = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(3)), '2');
      amt   = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(4)), '2');
      cos   = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(5)), '2');

      gmTotal    += gm;
      gmpcTotal  += gmpc;
      gmpcTotal2 += gmpc2;
      amtTotal   += amt;
      cosTotal   += cos;

      gmGrandTotal[0]   += gm;
      gmpcGrandTotal[0] += gmpc;
      amtGrandTotal[0]  += amt;
      cosGrandTotal[0]  += cos;
    }

    outputLine(out, dateFrom, dateTo, cssFormat, gmTotal, gmpcTotal, gmpcTotal2, amtTotal, cosTotal, bytesOut);

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void outputLine(PrintWriter out, String dateFrom, String dateTo, String[] cssFormat, double gmTotal, double gmpcTotal, double gmpcTotal2, double amtTotal, double cosTotal, int[] bytesOut) throws Exception
  {
    if(cssFormat[0].equals("line1")) cssFormat[0] = "line2"; else cssFormat[0] = "line1";

    scoutln(out, bytesOut, "<tr id=\"" + cssFormat[0] + "\">");

    scoutln(out, bytesOut, "<td><p><a href=\"javascript:invs('" + dateFrom + "','" + dateTo + "')\">" + dateFrom + " to " + dateTo + "</a></td>");

    scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(amtTotal, '2') + "</td>");

    scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(cosTotal, '2') + "</td>");

    scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(gmTotal, '2') + "</td>");

    if(cosTotal != 0)
      gmpcTotal = (gmTotal / cosTotal) * 100;
    else gmpcTotal = 0;

    scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(gmpcTotal, '2') + "</td>");
    scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(gmpcTotal2, '2') + "</td>");

    scout(out, bytesOut, "</tr>");
  }

}
