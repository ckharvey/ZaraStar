// =======================================================================================================================================================================================================
// System: ZaraStar: Analytics - WIP Analysis - by Item Code
// Module: AnalyticsWIPAnalysisItemCode.java
// Author: C.K.Harvey
// Copyright (c) 2001-09 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;
import java.io.*;

public class AnalyticsWIPAnalysisItemCode extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  DrawingUtils drawingUtils = new DrawingUtils();
  MiscDefinitions miscDefinitions = new MiscDefinitions();
  Inventory inventory = new Inventory();
  DefinitionTables definitionTables = new DefinitionTables();
  AccountsUtils accountsUtils = new AccountsUtils();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="";

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
      p1  = req.getParameter("p1"); // dateto

      if(p1 == null) p1 = "";

      doIt(out, req, p1, unm, sid, uty, men, den, dnm, bnm, bytesOut);
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

      System.out.println(e);
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "AnalyticsWIPAnalysisInput", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6036, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String p1, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String workingDir    = directoryUtils.getUserDir('W', dnm, unm);

    String userName = directoryUtils.getMySQLUserName();
    String passWord = directoryUtils.getMySQLPassWord();
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + userName + "&password=" + passWord);

    Statement stmt = null, stmt2 = null, stmt3 = null, stmt4 = null;
    ResultSet rs   = null, rs2   = null, rs3   = null, rs4   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 6036, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "AnalyticsWIPAnalysisItemCode", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6036, bytesOut[0], 0, "ACC:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "AnalyticsWIPAnalysisItemCode", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6036, bytesOut[0], 0, "SID:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    String dateTo;
    if(p1.length() == 0)
      dateTo = "2099-12-31";
    else dateTo = generalUtils.convertDateToSQLFormat(p1);

    String[] financialYearStartMonth = new String[1];
    String[] financialYearEndMonth   = new String[1];

    definitionTables.getAppConfigFinancialYearMonths(con, stmt, rs, dnm, financialYearStartMonth, financialYearEndMonth);

    String s;
    int startMonth = generalUtils.detMonthNumFromMonthName(financialYearStartMonth[0]);
    if(startMonth < 10)
      s = "-0" + startMonth;
    else s = "-" + startMonth;

    s += "-01";

    String dateStartAccountingYear = accountsUtils.getAccountingYearForADate(con, stmt, rs, generalUtils.convertFromYYYYMMDD(dateTo), dnm, localDefnsDir, defnsDir) + s;

    set(con, stmt, stmt2, stmt3, stmt4, rs, rs2, rs3, rs4, out, req, dateTo, dateStartAccountingYear, unm, sid, uty, men, den, dnm, bnm, workingDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 6036, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, Statement stmt2, Statement stmt3, Statement stmt4, ResultSet rs, ResultSet rs2, ResultSet rs3, ResultSet rs4, PrintWriter out, HttpServletRequest req, String dateTo,
                   String dateStartAccountingYear, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String workingDir, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>WIP Analysis</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3001, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewItem(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/ProductStockRecord?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p1=\"+p1;}");
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

    RandomAccessFile fhData  = generalUtils.create(workingDir + "6036c.data");
    RandomAccessFile fhState = generalUtils.create(workingDir + "6036c.state");
    generalUtils.fileClose(fhState);
    String stateFileName = workingDir + "6036c.state";
    keepChecking(out, "6036c", unm, sid, uty, men, den, dnm, bnm, bytesOut);

    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    int[] hmenuCount = new int[1];

    drawingUtils.outputPageFrame(con, stmt, rs, out, req, "6036", "", "AnalyticsWIPAnalysisDetails", unm, sid, uty, men, den, dnm, bnm, " chkTimer(); ", localDefnsDir, defnsDir, hmenuCount, bytesOut);

    drawingUtils.drawTitle(out, false, false, "AnalyticsWIPAnalysisInput", "", "Work-in-Progress Analysis", "6036b", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<span id='stuff'></span>");
    if(out != null) out.flush();
    out.close();

    scoutln(fhData, "<table id=\"page\" width=100% border=0>");

    scoutln(fhData, "<tr><td></td></tr>");

    String baseCurrency = accountsUtils.getBaseCurrency(con, stmt, rs, dnm, localDefnsDir, defnsDir);

    int dateToEncoded = generalUtils.encodeFromYYYYMMDD(dateTo);

    headings(fhData, baseCurrency);

    char dpOnQuantities = miscDefinitions.dpOnQuantities(con, stmt, rs, "5");

    String[] cssFormat = new String[1];  cssFormat[0] = "line1";
    int[] oCount = new int[1];  oCount[0] = 1;

    double[] type1TotalValue = new double[1];  type1TotalValue[0] = 0.0;
    double[] type2TotalValue = new double[1];  type2TotalValue[0] = 0.0;
    double[] type3TotalValue = new double[1];  type3TotalValue[0] = 0.0;
    double[] type4TotalValue = new double[1];  type4TotalValue[0] = 0.0;

    boolean atLeastOneSetAsAllSupplied = forAllSalesOrders(con, stmt, stmt2, stmt3, rs, rs2, rs3, fhData, stateFileName, dateTo, dateToEncoded, dateStartAccountingYear, dpOnQuantities, cssFormat, type1TotalValue, type2TotalValue,
                                                           type3TotalValue, type4TotalValue, dnm);

    headings(fhData, baseCurrency);

    scoutln(fhData, "<tr><td>&nbsp;</td></tr>");
    scoutln(fhData, "<tr><td>&nbsp;</td></tr>");
    scoutln(fhData, "<tr><td nowrap colspan=6><p>Type-1: Picked, no DO, no Invoice: " + baseCurrency + " " + generalUtils.formatNumeric(type1TotalValue[0], '2') + "</td></tr>");
    scoutln(fhData, "<tr><td nowrap colspan=6><p>Type-2: Picked, DO, no Invoice: "    + baseCurrency + " " + generalUtils.formatNumeric(type2TotalValue[0], '2') + "</td></tr>");
    scoutln(fhData, "<tr><td nowrap colspan=6><p>Type-3: Not Picked, Invoiced: "      + baseCurrency + " " + generalUtils.formatNumeric(type3TotalValue[0], '2') + "</td></tr>");
    scoutln(fhData, "<tr><td nowrap colspan=6><p>Type-4: Picked, no DO, Invoiced: "   + baseCurrency + " " + generalUtils.formatNumeric(type4TotalValue[0], '2') + "</td></tr>");

    if(atLeastOneSetAsAllSupplied)
    {
      scoutln(fhData, "<tr><td>&nbsp;</td></tr>");
      scoutln(fhData, "<tr><td nowrap colspan=4><p><font color=red size=2><sup>*</sup> All Supplied</font></td></tr>");
    }

    scoutln(fhData, "<tr><td>&nbsp;</td></tr>");

    scoutln(fhData, "</table></form>");
    scoutln(fhData, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir));

    generalUtils.fileClose(fhData);
    directoryUtils.updateState(stateFileName, "100");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void headings(RandomAccessFile fhData, String baseCurrency) throws Exception
  {
    scoutln(fhData, "<tr id=\"pageColumn\">");
    scoutln(fhData, "<td><p>ItemCode</td>");
    scoutln(fhData, "<td align=center><p>Sales Order Quantity</td>");
    scoutln(fhData, "<td align=center><p>Picking List<br>" + baseCurrency + "</td>");
    scoutln(fhData, "<td align=center><p>Delivery Order<br>" + baseCurrency + "</td>");
    scoutln(fhData, "<td align=center><p>Invoice Quantity<br>" + baseCurrency + "</td>");
    scoutln(fhData, "<td align=center><p>Type-1<br>" + baseCurrency + "</td>");
    scoutln(fhData, "<td align=center><p>Type-2<br>" + baseCurrency + "</td>");
    scoutln(fhData, "<td align=center><p>Type-3<br>" + baseCurrency + "</td>");
    scoutln(fhData, "<td align=center><p>Type-4<br>" + baseCurrency + "</td>");
    scoutln(fhData, "<td></td></tr>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {
    out.println(str);
    bytesOut[0] += (str.length() + 2);
  }
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(RandomAccessFile fh, String str) throws Exception
  {
    fh.writeBytes(str + "\n");
  }
  private void scout(RandomAccessFile fh, String str) throws Exception
  {
    fh.writeBytes(str);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean forAllSalesOrders(Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, ResultSet rs3, RandomAccessFile fhData, String stateFileName, String dateTo, int dateToEncoded,
                                    String dateStartAccountingYear, char dpOnQuantities, String[] cssFormat, double[] type1TotalValue, double[] type2TotalValue, double[] type3TotalValue, double[] type4TotalValue, String dnm) throws Exception
  {
    boolean atLeastOneSetAsAllSupplied = false;

    stmt = con.createStatement();
    rs = stmt.executeQuery("SELECT COUNT(t2.SOCode) FROM sol AS t2 INNER JOIN so AS t1 ON t2.SOCode = t1.SOCode WHERE t1.Status != 'C' AND t1.Date <= {d '" + dateTo + "'}");
    double numSOs = 1.0;
    if(rs.next())
      numSOs = rs.getInt(1);
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.SOCode, t2.ItemCode, t2.Quantity FROM sol AS t2 INNER JOIN so AS t1 ON t2.SOCode = t1.SOCode WHERE t1.Status != 'C' AND t1.Date <= {d '" + dateTo + "'} ORDER BY t1.SOCode");

    int thisSOItemCodesCount = 0;
    String[][] thisSOItemCodes = new String[1][100];
    double[][] thisSOItemQtys  = new double[1][100];
    int[] thisSOItemCodesSize  = new int[1];  thisSOItemCodesSize[0] = 100;

    int soItemCodesCount = 0;
    String[][] soItemCodes = new String[1][100];
    double[][] soItemQtys  = new double[1][100];
    int[] soItemCodesSize  = new int[1];  soItemCodesSize[0] = 100;

    String[][] plItemCodes = new String[1][100];
    double[][] plItemQtys  = new double[1][100];
    int[] plItemCodesSize  = new int[1];  plItemCodesSize[0] = 100;

    String[][] doItemCodes = new String[1][100];
    double[][] doItemQtys  = new double[1][100];
    int[] doItemCodesSize  = new int[1];  doItemCodesSize[0] = 100;

    String[][] saItemCodes = new String[1][100];
    double[][] saItemQtys  = new double[1][100];
    int[] saItemCodesSize  = new int[1];  saItemCodesSize[0] = 100;

    String[][] iItemCodes = new String[1][100];
    double[][] iItemQtys  = new double[1][100];
    int[] iItemCodesSize  = new int[1];  iItemCodesSize[0] = 100;

    String[][] cnItemCodes = new String[1][100];
    double[][] cnItemQtys  = new double[1][100];
    int[] cnItemCodesSize  = new int[1];  cnItemCodesSize[0] = 100;

    String[][] sowipItemCodes = new String[1][100];
    String[][] sowipItemDates = new String[1][100];
    int[] sowipItemCodesSize  = new int[1];  sowipItemCodesSize[0] = 100;

    String soCode, lastSOCode = "";
    boolean first = true;
    int count = 0;

    double[][] type1 = new double[1][100];
    double[][] type2 = new double[1][100];
    double[][] type3 = new double[1][100];
    double[][] type4 = new double[1][100];

    double[][] sQtys = new double[1][100];
    double[][] pQtys = new double[1][100];
    double[][] dQtys = new double[1][100];
    double[][] iQtys = new double[1][100];

    String[][] items = new String[1][100];
    int[] itemsSize  = new int[1];  itemsSize[0] = 100;
    int[] itemsCount = new int[1];  itemsCount[0] = 0;

    while(rs.next())
    {
      directoryUtils.updateState(stateFileName, "" + generalUtils.strDPs('0', ("" + generalUtils.doubleToStr((count++ / numSOs) * 100))));
      soCode = rs.getString(1);

      if(! lastSOCode.equals(soCode))
      {
        if(! first)
        {
          processSO(con, stmt, stmt2, stmt3, rs, rs2, rs3, lastSOCode, dateTo, dateToEncoded, thisSOItemCodes[0], thisSOItemQtys[0], thisSOItemCodesCount, plItemCodes, plItemCodesSize, plItemQtys, doItemCodes, doItemCodesSize, doItemQtys, saItemCodes, saItemCodesSize,
                    saItemQtys, iItemCodes, iItemCodesSize, iItemQtys, cnItemCodes, cnItemCodesSize, cnItemQtys, sowipItemCodes, sowipItemCodesSize, sowipItemDates, dpOnQuantities, items, itemsSize, itemsCount, type1, type2, type3, type4,
                    sQtys, pQtys, dQtys, iQtys);

          clearList(thisSOItemCodes, thisSOItemQtys, thisSOItemCodesCount);
          thisSOItemCodesCount = 0;
        }
        else first = false;

        lastSOCode = soCode;
      }

      thisSOItemCodesCount = addToList(con, stmt3, rs3, rs.getString(2), rs.getString(3), thisSOItemCodes, thisSOItemCodesSize, thisSOItemQtys, thisSOItemCodesCount);
      soItemCodesCount = addToList(con, stmt3, rs3, rs.getString(2), rs.getString(3), soItemCodes, soItemCodesSize, soItemQtys, soItemCodesCount);
    }

    double itemValue;
    String itemCode;

    for(int x=0;x<itemsCount[0];++x)
    {
      itemCode = items[0][x];

      itemValue = inventory.getWAC(con, stmt, rs, itemCode, dnm, dateStartAccountingYear, dateTo);

      type1TotalValue[0] += (generalUtils.doubleDPs(type1[0][x], '2') * itemValue);
      type2TotalValue[0] += (generalUtils.doubleDPs(type2[0][x], '2') * itemValue);
      type3TotalValue[0] += (generalUtils.doubleDPs(type3[0][x], '2') * itemValue);
      type4TotalValue[0] += (generalUtils.doubleDPs(type4[0][x], '2') * itemValue);

      if(cssFormat[0].equals("line1")) cssFormat[0] = "line2"; else cssFormat[0] = "line1";

      scout(fhData, "<tr id='" + cssFormat[0] + "'><td nowrap><p><a href=\"javascript:viewItem('" + itemCode + "')\">" + itemCode + "</a></td>");

      scoutln(fhData, "<td align=center><p>" + generalUtils.formatNumeric(sQtys[0][x], dpOnQuantities) + "</td>");

      scout(fhData, "<td align=center><p>" + generalUtils.formatNumeric(pQtys[0][x], dpOnQuantities) + "<br>");
      if((pQtys[0][x] * itemValue) != 0.0)
        scout(fhData, generalUtils.formatNumeric((pQtys[0][x] * itemValue), '2'));
      else scout(fhData, "&nbsp;");
      scoutln(fhData, "</td>");

      scout(fhData, "<td align=center><p>" + generalUtils.formatNumeric(dQtys[0][x], dpOnQuantities) + "<br>");
      if((dQtys[0][x] * itemValue) != 0.0)
        scout(fhData, generalUtils.formatNumeric((dQtys[0][x] * itemValue), '2'));
      else scout(fhData, "&nbsp;");
      scoutln(fhData, "</td>");

      scout(fhData, "<td align=center><p>" + generalUtils.formatNumeric(iQtys[0][x], dpOnQuantities) + "<br>");
      if((iQtys[0][x] * itemValue) != 0.0)
        scout(fhData, generalUtils.formatNumeric((iQtys[0][x] * itemValue), '2'));
      else scout(fhData, "&nbsp;");
      scoutln(fhData, "</td>");

      scout(fhData, "<td align=center><p>" + generalUtils.formatNumeric(type1[0][x], dpOnQuantities) + "<br>");
      if((type1[0][x] * itemValue) != 0.0)
        scout(fhData, generalUtils.formatNumeric((type1[0][x] * itemValue), '2'));
      else scout(fhData, "&nbsp;");
      scoutln(fhData, "</td>");

      scout(fhData, "<td align=center><p>" + generalUtils.formatNumeric(type2[0][x], dpOnQuantities) + "<br>");
      if((type2[0][x] * itemValue) != 0.0)
        scout(fhData, generalUtils.formatNumeric((type2[0][x] * itemValue), '2'));
      else scout(fhData, "&nbsp;");
      scoutln(fhData, "</td>");

      scout(fhData, "<td align=center><p>" + generalUtils.formatNumeric(type3[0][x], dpOnQuantities) + "<br>");
      if((type3[0][x] * itemValue) != 0.0)
        scout(fhData, generalUtils.formatNumeric((type3[0][x] * itemValue), '2'));
      else scout(fhData, "&nbsp;");
      scoutln(fhData, "</td>");

      scout(fhData, "<td align=center><p>" + generalUtils.formatNumeric(type4[0][x], dpOnQuantities) + "<br>");
      if((type4[0][x] * itemValue) != 0.0)
        scout(fhData, generalUtils.formatNumeric((type4[0][x] * itemValue), '2'));
      else scout(fhData, "&nbsp;");
      scoutln(fhData, "</td>");

      scoutln(fhData, "</tr>");
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    return atLeastOneSetAsAllSupplied;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void processSO(Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, ResultSet rs3, String soCode, String dateTo, int dateToEncoded, String[] thisSOItemCodes, double[] thisSOItemQtys,
                         int thisSOItemCodesCount, String[][] plItemCodes, int[] plItemCodesSize, double[][] plItemQtys, String[][] doItemCodes, int[] doItemCodesSize, double[][] doItemQtys, String[][] saItemCodes, int[] saItemCodesSize,
                         double[][] saItemQtys, String[][] iItemCodes, int[] iItemCodesSize, double[][] iItemQtys, String[][] cnItemCodes, int[]cnItemCodesSize, double[][] cnItemQtys, String[][] sowipItemCodes, int[] sowipItemCodesSize,
                         String[][] sowipItemDates, char dpOnQuantities, String[][] items, int[] itemsSize, int[] itemsCount, double[][] type1, double[][] type2, double[][] type3, double[][] type4, double[][] sQtys, double[][] pQtys,
                         double[][] dQtys, double[][] iQtys) throws Exception
  {
    int[] cnItemCodesCount = new int[1];  cnItemCodesCount[0] = 0;

    int plItemCodesCount = scanPickingLists(con, stmt, stmt2, rs, rs2, soCode, dateTo, plItemCodes, plItemCodesSize, plItemQtys);
    int doItemCodesCount = scanDOs(con, stmt, stmt2, rs, rs2, soCode, dateTo, doItemCodes, doItemCodesSize, doItemQtys);
    int iItemCodesCount  = scanInvoices(con, stmt, stmt2, stmt3, rs, rs2, rs3, soCode, dateTo, dpOnQuantities, iItemCodes, iItemCodesSize, iItemQtys, cnItemCodes, cnItemCodesSize, cnItemQtys, cnItemCodesCount);
    int saItemCodesCount = scanForStockA(con, stmt, stmt2, rs, rs2, soCode, dateTo, dpOnQuantities, saItemCodes, saItemCodesSize, saItemQtys);

    int sowipItemCodesCount = scanSOForWIPOverrides(con, stmt, stmt2, rs, rs2, soCode, sowipItemCodes, sowipItemCodesSize, sowipItemDates);

    String itemCode;
    int sowipDateEncoded;
    double plQty, iQty, doQty, saQty, cnQty, v, soQty;

    int x = 0;
    for(x=0;x<thisSOItemCodesCount;++x)
    {
      itemCode = thisSOItemCodes[x];

      soQty = qtyOnList(itemCode, thisSOItemCodes, thisSOItemQtys, thisSOItemCodesCount);

      plQty = qtyOnList(itemCode, plItemCodes[0], plItemQtys[0], plItemCodesCount);

      doQty = qtyOnList(itemCode, doItemCodes[0], doItemQtys[0], doItemCodesCount);

      iQty  = qtyOnList(itemCode, iItemCodes[0],  iItemQtys[0],  iItemCodesCount);

      saQty = qtyOnList(itemCode, saItemCodes[0], saItemQtys[0], saItemCodesCount);

      cnQty = qtyOnList(itemCode, cnItemCodes[0], cnItemQtys[0], cnItemCodesCount[0]);

      sowipDateEncoded = dateOnList(itemCode, sowipItemCodes[0], sowipItemDates[0], sowipItemCodesCount);

      if(saQty >= cnQty)
        plQty -= (saQty - cnQty); // those SA not for a CN (i.e., returned to stock before invoicing)

      iQty  -= cnQty; // those SA for a CN (i.e., returned to stock after invoicing)

      if(sowipDateEncoded != 0 && sowipDateEncoded <= dateToEncoded)
        ; // ignore
      else
      if(plQty == 0.0 && doQty == 0.0 && iQty == 0.0)
        ; // ignore book order
      else
      if(plQty == doQty && plQty == iQty)
        ; // complete
      else
      {
        // type4: picked, no DO, invoiced
        v = iQty - doQty;
        if(v > 0)
        {
          //type4[0] += v;
          addToListItemTypes(con, stmt, rs, itemCode, v, items, itemsSize, 4, type1, type2, type3, type4, sQtys, pQtys, dQtys, iQtys, soQty, plQty, doQty, iQty, itemsCount);
        }

        //else type4 += 0;

        // type1: picked, no DO, no invoice
        addToListItemTypes(con, stmt, rs, itemCode, (plQty - doQty - v), items, itemsSize, 1, type1, type2, type3, type4, sQtys, pQtys, dQtys, iQtys, soQty, plQty, doQty, iQty, itemsCount);

        // type2: picked, DO, no invoice
        v = doQty - iQty;
        if(v > 0)
        {
          addToListItemTypes(con, stmt, rs, itemCode, v, items, itemsSize, 2, type1, type2, type3, type4, sQtys, pQtys, dQtys, iQtys, soQty, plQty, doQty, iQty, itemsCount);
        }
        //else type2 = 0;

        // type3: not picked, invoiced
        v = iQty - plQty;
        if(v > 0)
        {
          addToListItemTypes(con, stmt, rs, itemCode, v, items, itemsSize, 3, type1, type2, type3, type4, sQtys, pQtys, dQtys, iQtys, soQty, plQty, doQty, iQty, itemsCount);
        }
      }
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private double onList(String itemCode, double[] qtys, int count) throws Exception
  {
    for(int x=0;x<count;++x)
    {
      if(itemCode.equals(qtys[x]))
        return qtys[x];
    }

    return 0;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private double qtyOnList(String itemCode, String[] itemCodes, double[] itemQtys, int itemCodesCount) throws Exception
  {
    for(int x=0;x<itemCodesCount;++x)
    {
      if(itemCode.equals(itemCodes[x]))
        return itemQtys[x];
    }

    return 0;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int dateOnList(String itemCode, String[] itemCodes, String[] itemDates, int itemCodesCount) throws Exception
  {
    for(int x=0;x<itemCodesCount;++x)
    {
      if(itemCode.equals(itemCodes[x]))
        return generalUtils.encodeFromYYYYMMDD(itemDates[x]);
      }

    return 0;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int scanPickingLists(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String soCode, String dateTo, String[][] plItemCodes, int[] plItemCodesSize, double[][] plItemQtys) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.ItemCode, t2.QuantityPacked FROM pll AS t2 INNER JOIN pl AS t1 ON t2.PLCode = t1.PLCode WHERE t1.Status != 'C' AND t2.SOCode = '" + soCode + "' AND t1.Date <= {d '" + dateTo + "'}");

    int itemCodesCount = 0;

    while(rs.next())
      itemCodesCount = addToList(con, stmt2, rs2, rs.getString(1), rs.getString(2), plItemCodes, plItemCodesSize, plItemQtys, itemCodesCount);

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    return itemCodesCount;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int scanDOs(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String soCode, String dateTo, String[][] doItemCodes, int[] doItemCodesSize, double[][] doItemQtys) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.ItemCode, t2.Quantity FROM dol AS t2 INNER JOIN do AS t1 ON t2.DOCode = t1.DOCode WHERE t1.Status != 'C' AND t2.SOCode = '" + soCode + "' AND t1.Date <= {d '" + dateTo + "'}");

    int itemCodesCount = 0;

    while(rs.next())
      itemCodesCount = addToList(con, stmt2, rs2, rs.getString(1), rs.getString(2), doItemCodes, doItemCodesSize, doItemQtys, itemCodesCount);

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    return itemCodesCount;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int scanInvoices(Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, ResultSet rs3, String soCode, String dateTo, char dpOnQuantities, String[][] iItemCodes, int[] iItemCodesSize,
                           double[][] iItemQtys, String[][] cnItemCodes, int[] cnItemCodesSize, double[][] cnItemQtys, int[] cnItemCodesCount) throws Exception
  {
    byte[] b = new byte[30];
    byte[] invoiceCodes    = new byte[1000]; invoiceCodes[0]    = '\000';
    int[]  invoiceCodesLen = new int[1];     invoiceCodesLen[0] = 1000;

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.ItemCode, t2.Quantity, t2.InvoiceCode FROM invoicel AS t2 INNER JOIN invoice AS t1 ON t2.InvoiceCode = t1.InvoiceCode WHERE t1.Status != 'C' AND t2.SOCode = '" + soCode + "' AND t1.Date <= {d '" + dateTo
                         + "'}");

    int itemCodesCount = 0;
    double qtyCount;
    String itemCode;

    while(rs.next())
    {
      itemCode = rs.getString(1);
      qtyCount = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(2)), dpOnQuantities);

      itemCodesCount = addToList(con, stmt2, rs2, itemCode, generalUtils.doubleToStr(qtyCount), iItemCodes, iItemCodesSize, iItemQtys, itemCodesCount);

      // note the invoiceCode
      generalUtils.strToBytes(b, (rs.getString(3) + "\001"));
      invoiceCodes = generalUtils.addToList(b, invoiceCodes, invoiceCodesLen);
    }

    int y, count = generalUtils.countListEntries(invoiceCodes);
    String invoiceCode;
    for(int x=0;x<count;++x)
    {
      generalUtils.getListEntryByNum(x, invoiceCodes, b);
      y=0;
      invoiceCode = "";
      while(b[y] != '\001' && b[y] != '\000') // just-in-case
        invoiceCode += (char)b[y++];

      scanForCNs(con, stmt2, stmt3, rs2, rs3, invoiceCode, dateTo, dpOnQuantities, cnItemCodes, cnItemCodesSize, cnItemQtys, cnItemCodesCount);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    return itemCodesCount;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int addToList(Connection con, Statement stmt, ResultSet rs, String itemCode, String qty, String[][] itemCodes, int[] itemCodesSize, double[][] itemQtys, int itemCodesCount) throws Exception
  {
    if(! inventory.existsItemRecGivenCode(con, stmt, rs, itemCode))
      return itemCodesCount;

    int x = 0;
    while(x < itemCodesCount)
    {
      if(itemCodes[0][x].equals(itemCode))
      {
        itemQtys[0][x] += generalUtils.doubleFromStr(qty);
        return itemCodesCount;
      }

      ++x;
    }

    if(itemCodesCount == itemCodesSize[0])
    {
      String[] tmp = new String[itemCodesSize[0]];
      for(x=0;x<itemCodesSize[0];++x)
        tmp[x] = itemCodes[0][x];
      itemCodes[0] = new String[itemCodesSize[0] + 20];
      for(x=0;x<itemCodesSize[0];++x)
        itemCodes[0][x] = tmp[x];

      double[] tmp2 = new double[itemCodesSize[0]];
      for(x=0;x<itemCodesSize[0];++x)
        tmp2[x] = itemQtys[0][x];
      itemQtys[0] = new double[itemCodesSize[0] + 20];
      for(x=0;x<itemCodesSize[0];++x)
        itemQtys[0][x] = tmp2[x];

      itemCodesSize[0] += 20;
    }

    itemCodes[0][itemCodesCount] = itemCode;

    itemQtys[0][itemCodesCount] = generalUtils.doubleFromStr(qty);

    return ++itemCodesCount;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void addToListItemTypes(Connection con, Statement stmt, ResultSet rs, String itemCode, double qty, String[][] items, int[] itemsSize, int which, double[][] type1, double[][] type2, double[][] type3, double[][] type4, double[][] sQtys,
                                  double[][] pQtys, double[][] dQtys, double[][] iQtys, double soQty, double plQty, double doQty, double iQty, int[] itemsCount) throws Exception
  {
    if(! inventory.existsItemRecGivenCode(con, stmt, rs, itemCode))
      return;

    int x = 0;
    while(x < itemsCount[0])
    {
      if(items[0][x].equals(itemCode))
      {
        switch(which)
        {
          case 1 : type1[0][x] += qty; break;
          case 2 : type2[0][x] += qty; break;
          case 3 : type3[0][x] += qty; break;
          case 4 : type4[0][x] += qty; break;
        }

        sQtys[0][x] += soQty;
        pQtys[0][x] += plQty;
        dQtys[0][x] += doQty;
        iQtys[0][x] += iQty;

        return;
      }

      ++x;
    }

    if(itemsCount[0] == itemsSize[0])
    {
      String[] tmp = new String[itemsSize[0]];
      for(x=0;x<itemsSize[0];++x)
        tmp[x] = items[0][x];
      items[0] = new String[itemsSize[0] + 20];
      for(x=0;x<itemsSize[0];++x)
        items[0][x] = tmp[x];

      double[] tmp2 = new double[itemsSize[0]];
      for(x=0;x<itemsSize[0];++x)
        tmp2[x] = type1[0][x];
      type1[0] = new double[itemsSize[0] + 20];
      for(x=0;x<itemsSize[0];++x)
        type1[0][x] = tmp2[x];

      for(x=0;x<itemsSize[0];++x)
        tmp2[x] = type2[0][x];
      type2[0] = new double[itemsSize[0] + 20];
      for(x=0;x<itemsSize[0];++x)
        type2[0][x] = tmp2[x];

      for(x=0;x<itemsSize[0];++x)
        tmp2[x] = type3[0][x];
      type3[0] = new double[itemsSize[0] + 20];
      for(x=0;x<itemsSize[0];++x)
        type3[0][x] = tmp2[x];

      for(x=0;x<itemsSize[0];++x)
        tmp2[x] = type4[0][x];
      type4[0] = new double[itemsSize[0] + 20];
      for(x=0;x<itemsSize[0];++x)
        type4[0][x] = tmp2[x];

      for(x=0;x<itemsSize[0];++x)
        tmp2[x] = sQtys[0][x];
      sQtys[0] = new double[itemsSize[0] + 20];
      for(x=0;x<itemsSize[0];++x)
        sQtys[0][x] = tmp2[x];

      for(x=0;x<itemsSize[0];++x)
        tmp2[x] = pQtys[0][x];
      pQtys[0] = new double[itemsSize[0] + 20];
      for(x=0;x<itemsSize[0];++x)
        pQtys[0][x] = tmp2[x];

      for(x=0;x<itemsSize[0];++x)
        tmp2[x] = dQtys[0][x];
      dQtys[0] = new double[itemsSize[0] + 20];
      for(x=0;x<itemsSize[0];++x)
        dQtys[0][x] = tmp2[x];

      for(x=0;x<itemsSize[0];++x)
        tmp2[x] = iQtys[0][x];
      iQtys[0] = new double[itemsSize[0] + 20];
      for(x=0;x<itemsSize[0];++x)
        iQtys[0][x] = tmp2[x];

      itemsSize[0] += 20;
    }

    items[0][itemsCount[0]] = itemCode;

    switch(which)
    {
      case 1 : type1[0][itemsCount[0]] += qty; break;
      case 2 : type2[0][itemsCount[0]] += qty; break;
      case 3 : type3[0][itemsCount[0]] += qty; break;
      case 4 : type4[0][itemsCount[0]] += qty; break;
    }

    sQtys[0][itemsCount[0]] += soQty;
    pQtys[0][itemsCount[0]] += plQty;
    dQtys[0][itemsCount[0]] += doQty;
    iQtys[0][itemsCount[0]] += iQty;

    ++itemsCount[0];
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int addToListDates(Connection con, Statement stmt, ResultSet rs, String itemCode, String date, String[][] itemCodes, int[] itemCodesSize, String[][] itemDates, int itemCodesCount) throws Exception
  {
    if(! inventory.existsItemRecGivenCode(con, stmt, rs, itemCode))
      return itemCodesCount;

    int x = 0;
    while(x < itemCodesCount)
    {
      if(itemCodes[0][x].equals(itemCode))
      {
        itemDates[0][x] = date;
        return itemCodesCount;
      }

      ++x;
    }

    if(itemCodesCount == itemCodesSize[0])
    {
      String[] tmp = new String[itemCodesSize[0]];
      for(x=0;x<itemCodesSize[0];++x)
        tmp[x] = itemCodes[0][x];
      itemCodes[0] = new String[itemCodesSize[0] + 20];
      for(x=0;x<itemCodesSize[0];++x)
        itemCodes[0][x] = tmp[x];

      String[] tmp2 = new String[itemCodesSize[0]];
      for(x=0;x<itemCodesSize[0];++x)
        tmp2[x] = itemDates[0][x];
      itemDates[0] = new String[itemCodesSize[0] + 20];
      for(x=0;x<itemCodesSize[0];++x)
        itemDates[0][x] = tmp2[x];

      itemCodesSize[0] += 20;
    }

    itemCodes[0][itemCodesCount] = itemCode;

    itemDates[0][itemCodesCount] = date;

    return ++itemCodesCount;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void clearList(String[][] itemCodes, double[][] itemQtys, int itemCodesCount) throws Exception
  {
    for(int x=0;x<itemCodesCount;++x)
    {
      itemCodes[0][x] = "";
      itemQtys[0][x] = 0.0;
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scanForCNs(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String invoiceCode, String dateTo, char dpOnQuantities, String[][] cnItemCodes, int[] cnItemCodesSize, double[][] cnItemQtys,
                          int[] cnItemCodesCount) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.ItemCode, t2.Quantity FROM creditl AS t2 INNER JOIN credit AS t1 ON t2.CNCode = t1.CNCode WHERE t1.Status != 'C' AND t2.InvoiceCode = '" + invoiceCode
                         + "' AND t2.CostOfSaleAdjustment = 'Y' AND t1.Date <= {d '" + dateTo + "'}");

    double qtyCount;
    String itemCode;

    while(rs.next())
    {
      itemCode = rs.getString(1);
      qtyCount = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(2)), dpOnQuantities);

      cnItemCodesCount[0] = addToList(con, stmt2, rs2, itemCode, generalUtils.doubleToStr(qtyCount), cnItemCodes, cnItemCodesSize, cnItemQtys, cnItemCodesCount[0]);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int scanForStockA(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String soCode, String dateTo, char dpOnQuantities, String[][] saItemCodes, int[] saItemCodesSize, double[][] saItemQtys) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT ItemCode, Quantity FROM stocka WHERE SOCode = '" + soCode + "' AND Status != 'C' AND StoreFrom = 'None' AND StoreTo != 'None' AND Date <= {d '" + dateTo + "'}");

    int itemCodesCount = 0;
    double qtyCount;
    String itemCode;

    while(rs.next())
    {
      itemCode = rs.getString(1);
      qtyCount = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(2)), dpOnQuantities);

      itemCodesCount = addToList(con, stmt2, rs2, itemCode, generalUtils.doubleToStr(qtyCount), saItemCodes, saItemCodesSize, saItemQtys, itemCodesCount);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    return itemCodesCount;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int scanSOForWIPOverrides(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String soCode, String[][] sowipItemCodes, int[] sowipItemCodesSize, String[][] sowipItemDates) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.ItemCode, t2.WIPOverride FROM sol AS t2 INNER JOIN so AS t1 ON t2.SOCode = t1.SOCode WHERE t1.Status != 'C' AND t2.SOCode = '" + soCode + "'");

    int itemCodesCount = 0;

    while(rs.next())
      itemCodesCount = addToListDates(con, stmt2, rs2, rs.getString(1), rs.getString(2), sowipItemCodes, sowipItemCodesSize, sowipItemDates, itemCodesCount);

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    return itemCodesCount;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void keepChecking(PrintWriter out, String servlet, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "function chkTimer(){chkTimerID=self.setTimeout('chk()',2000);}");

    scoutln(out, bytesOut, "var chkreq2;");
    scoutln(out, bytesOut, "function initRequest2(url)");
    scoutln(out, bytesOut, "{if(window.XMLHttpRequest){chkreq2=new XMLHttpRequest();}else");
    scoutln(out, bytesOut, "if(window.ActiveXObject){chkreq2=new ActiveXObject(\"Microsoft.XMLHTTP\");}}");

    scoutln(out, bytesOut, "function chk(){");
    scoutln(out, bytesOut, "var url=\"http://" + men + "/central/servlet/FaxStatusDataFromReportTemp?p1=" + servlet + "&unm=\" + escape('" + unm + "') + \"&sid=\" + escape('" + sid + "') + \"&uty=\" + escape('" + uty + "') + \"&men=\" + escape('" + men
                         + "') + \"&den=\" + escape('" + den + "') + \"&bnm=\" + escape('" + bnm + "') + \"&dnm=\" + escape('" + dnm + "');");
    scoutln(out, bytesOut, "initRequest2(url);");
    scoutln(out, bytesOut, "chkreq2.onreadystatechange=processRequest2;");
    scoutln(out, bytesOut, "chkreq2.open(\"GET\",url,true);");
    scoutln(out, bytesOut, "chkreq2.send(null);}");

    scoutln(out, bytesOut, "function processRequest2(){");
    scoutln(out, bytesOut, "if(chkreq2.readyState==4){");
    scoutln(out, bytesOut, "if(chkreq2.status==200){");
    scoutln(out, bytesOut, "var res=chkreq2.responseXML.getElementsByTagName(\"res\")[0].childNodes[0].nodeValue;");
    scoutln(out, bytesOut, "if(res.length>0){");
    scoutln(out, bytesOut, "if(res=='.')clearTimeout(chkTimerID);else chkTimer();");
    scoutln(out, bytesOut, "var s=chkreq2.responseXML.getElementsByTagName(\"stuff\")[0].childNodes[0].nodeValue;");
    scoutln(out, bytesOut, "var messageElement=document.getElementById('stuff');");
    scoutln(out, bytesOut, "messageElement.innerHTML=s;");
    scoutln(out, bytesOut, "}}}}");
  }

}
