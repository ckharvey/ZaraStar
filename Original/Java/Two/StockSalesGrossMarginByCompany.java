// =======================================================================================================================================================================================================
// System: ZaraStar: Sales Analytics: Stock Sales - Gross Margin - by company
// Module: StockSalesGrossMarginByCompany.java
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

public class StockSalesGrossMarginByCompany extends HttpServlet
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

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="", p3="";

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
      p3  = req.getParameter("p3");

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, p3, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "StockSalesGrossMarginByCompany", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1204, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush(); 
    }
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1, String p2, String p3, int[] bytesOut) throws Exception
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
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "StockSalesGrossMarginByCompany", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1204, bytesOut[0], 0, "ACC:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "StockSalesGrossMarginByCompany", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1204, bytesOut[0], 0, "SID:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    set(con, stmt, stmt2, rs, rs2, out, req, p1, p2, p3, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 1204, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, HttpServletRequest req, String dateFrom, String dateTo, String option, String unm, String sid, String uty, String men, String den,
                   String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Stock Sales: Gross Margin</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    scoutln(out, bytesOut, "function invs(code){var p3=sanitise(code);");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/StockSalesGrossMarginInvoices?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=" + dateFrom + "&p2=" + dateTo + "&p3=\"+p3+\"&p4=C&bnm=" + bnm + "\";}");

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

    drawingUtils.outputPageFrame(con, stmt, rs, out, req, "1204", "", "StockSalesGrossMarginByCompany", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount, bytesOut);

    drawingUtils.drawTitle(out, false, false, "StockSalesGrossMarginByCompany", "", "Stock Sales: Gross Margin Analysis: " + dateFrom + " to " + dateTo, "1204", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    String baseCurrency = accountsUtils.getBaseCurrency(con, stmt, rs, dnm, localDefnsDir, defnsDir);

    scoutln(out, bytesOut, "<form><table id=\"page\" width=100% border=0>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    if(option.equals("S"))
      scoutln(out, bytesOut, "<tr><td colspan=5><p>Ordered by " + baseCurrency + " Sale Amount, descending</td></tr>");
    else scoutln(out, bytesOut, "<tr><td colspan=5><p>Ordered by " + baseCurrency + " Gross Margin, descending</td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td></td></tr><tr id=\"pageColumn\">");

    scoutln(out, bytesOut, "<td><p>Company Code</td>");
    scoutln(out, bytesOut, "<td><p>Company Name</td>");
    scoutln(out, bytesOut, "<td align=right><p>" + baseCurrency + " Sale Amount</td>");
    scoutln(out, bytesOut, "<td align=right><p>% of Total Sales</td>");
    scoutln(out, bytesOut, "<td align=right><p>" + baseCurrency + " Cost of Sale</td>");
    scoutln(out, bytesOut, "<td align=right><p>" + baseCurrency + " Gross Margin</td>");
    scoutln(out, bytesOut, "<td align=right><p>% of Gross Margin</td>");
    scoutln(out, bytesOut, "<td align=right><p>Gross Margin % (CoS)</td>");
    scoutln(out, bytesOut, "<td align=right><p>Gross Margin % (Sale)</td></tr>");

    int[] oCount = new int[1];  oCount[0] = 1;
    double[] gmGrandTotal   = new double[1];  gmGrandTotal[0] = 0.0;
    double[] gmpcGrandTotal = new double[1];  gmpcGrandTotal[0] = 0.0;
    double[] amtGrandTotal  = new double[1];  amtGrandTotal[0] = 0.0;
    double[] cosGrandTotal  = new double[1];  cosGrandTotal[0] = 0.0;

    String outputTable = unm + "StockSalesGrossMarginByCompany_tmp";

    directoryUtils.createTmpTable(true, con, stmt2, "CompanyCode char(20), CompanyName char(60), Amount decimal(19,8), CostOfSale decimal(19,8), GrossMargin decimal(19,8), GrossMarginPC decimal(19,8), GrossMarginPC2 decimal(19,8)", "", outputTable);

    String tmpTable = unm + "StockSalesGrossMargin_tmp";

    forCusts(con, stmt, stmt2, rs, rs2, tmpTable, outputTable, gmGrandTotal, gmpcGrandTotal, amtGrandTotal, cosGrandTotal, oCount);

    outputCusts(con, stmt, rs, out, option, outputTable, amtGrandTotal[0], cosGrandTotal[0], bytesOut);

    directoryUtils.removeTmpTable(con, stmt, outputTable);

    if(oCount[0] == 0) oCount[0] = 1;

    scoutln(out, bytesOut, "<tr><td colspan=2><p><b>TOTALS:</td>");
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
  private void forCusts(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String tmpTable, String outputTable, double[] gmGrandTotal, double[] gmpcGrandTotal, double[] amtGrandTotal, double[] cosGrandTotal,
                        int[] oCount) throws Exception
  {
    double[] gm    = new double[1];  gm[0]    = 0.0;
    double[] gmpc  = new double[1];  gmpc[0]  = 0.0;
    double[] gmpc2 = new double[1];  gmpc2[0] = 0.0;
    double[] amt   = new double[1];  amt[0]   = 0.0;
    double[] cos   = new double[1];  cos[0]   = 0.0;
    String[] cssFormat = new String[1];  cssFormat[0] = "line1";

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT CompanyCode, Name FROM company");

    String companyCode, name;

    while(rs.next())
    {
      gm[0] = gmpc[0] = gmpc2[0] = amt[0] = cos[0] = 0.0;

      companyCode = rs.getString(1);
      name        = rs.getString(2);

      forACompany(con, stmt2, rs2, companyCode, tmpTable, gm, amt, cos);

      if(amt[0] != 0)
      {
        if(cos[0] != 0)
          gmpc[0] = (gm[0] / cos[0]) * 100;
        else gmpc[0] = 0;

        if(cos[0] != 0)
          gmpc2[0] = (gm[0] / amt[0]) * 100;
        else gmpc2[0] = 0;

        addToOutputTable(con, stmt2, companyCode, name, amt[0], cos[0], gm[0], gmpc[0], gmpc2[0], outputTable);

        gmGrandTotal[0]   += gm[0];
        gmpcGrandTotal[0] += gmpc[0];
        amtGrandTotal[0]  += amt[0];
        cosGrandTotal[0]  += cos[0];

        ++oCount[0];
      }
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void forACompany(Connection con, Statement stmt, ResultSet rs, String companyCode, String tmpTable, double[] gmTotal, double[] amtTotal, double[] cosTotal) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT Quantity, GrossMargin, GrossMarginPC, Amount, CostOfSale FROM " + tmpTable + " WHERE CompanyCode = '" + generalUtils.sanitiseForSQL(companyCode) + "'");

    double gm, amt, cos;

    while(rs.next())
    {
      gm   = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(2)), '2');
      amt  = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(4)), '2');
      cos  = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(5)), '2');

      gmTotal[0]   += gm;
      amtTotal[0]  += amt;
      cosTotal[0]  += cos;
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void outputCusts(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String option, String outputTable, double amtGrandTotal, double cosGrandTotal, int[] bytesOut) throws Exception
  {
    double gm, gmpc, gmpc2, amt, cos;
    String[] cssFormat = new String[1];  cssFormat[0] = "line1";

    stmt = con.createStatement();

    if(option.equals("S"))
      rs = stmt.executeQuery("SELECT * FROM " + outputTable + " ORDER BY Amount DESC");
    else rs = stmt.executeQuery("SELECT * FROM " + outputTable + " ORDER BY GrossMargin DESC");

    String companyCode, companyName;

    while(rs.next())
    {
      companyCode = rs.getString(1);
      companyName = rs.getString(2);
      amt         = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(3)), '2');
      cos         = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(4)), '2');
      gm          = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(5)), '2');
      gmpc        = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(6)), '2');
      gmpc2       = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(7)), '2');

      if(cssFormat[0].equals("line1")) cssFormat[0] = "line2"; else cssFormat[0] = "line1";

      scoutln(out, bytesOut, "<tr id=\"" + cssFormat[0] + "\">");

      scoutln(out, bytesOut, "<td><p><a href=\"javascript:invs('" + companyCode + "')\">" + companyCode + "</a></td>");

      scoutln(out, bytesOut, "<td><p>" + companyName + "</td>");

      scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(amt, '2') + "</td>");

      scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatJustEnough((amt / amtGrandTotal) * 100) + "</td>");

      scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(cos, '2') + "</td>");

      scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(gm, '2') + "</td>");

      scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatJustEnough((gm / (amtGrandTotal - cosGrandTotal)) * 100) + "</td>");

      scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(gmpc, '2') + "</td>");
      scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(gmpc2, '2') + "</td>");

      scout(out, bytesOut, "</tr>");
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void addToOutputTable(Connection con, Statement stmt, String companyCode, String companyName, double amt, double cos, double grossMargin, double grossMarginPC, double grossMarginPC2, String tmpTable) throws Exception
  {
    stmt = con.createStatement();

    String q = "INSERT INTO " + tmpTable + " ( CompanyCode, CompanyName, Amount, CostOfSale, GrossMargin, GrossMarginPC, GrossMarginPC2 ) VALUES ('" + generalUtils.sanitiseForSQL(companyCode) + "', '" + generalUtils.sanitiseForSQL(companyName) + "', '" + amt
             + "', '" + cos + "', '" + grossMargin + "', '" + grossMarginPC + "', '" + grossMarginPC2 + "')";

    stmt.executeUpdate(q);

    if(stmt != null) stmt.close();
  }

}
