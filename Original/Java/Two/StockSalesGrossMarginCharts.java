// =======================================================================================================================================================================================================
// System: ZaraStar: Charts: Gross Sales Margin
// Module: StockSalesGrossMarginCharts.java
// Author: C.K.Harvey
// Copyright (c) 2003-08 Christopher Harvey. All Rights Reserved.
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
import java.io.*;
import java.sql.*;

import org.jfree.data.category.*;
import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.data.time.*;

public class StockSalesGrossMarginCharts extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  AccountsUtils accountsUtils = new AccountsUtils();

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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "StockSalesGrossMargin", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1204, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush(); 
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String monthFrom, String monthTo, int[] bytesOut) throws Exception
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

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 1204, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "StockSalesGrossMarginCharts", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1204, bytesOut[0], 0, "ACC:");
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "StockSalesGrossMarginCharts", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1204, bytesOut[0], 0, "SID:");
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    String tmpTable = unm + "StockSalesGrossMargin_tmp";

    doIt(con, stmt, rs, out, req, unm, sid, uty, men, den, dnm, bnm, tmpTable, monthFrom, monthTo, workingDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 1204, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");

    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String tmpTable, String monthFrom, String monthTo,
                    String workingDir, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Gross Sales Margin</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\"></head>");

    int[] hmenuCount = new int[1];

    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "1204", "", "StockSalesGrossMargin", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<form>");

    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Gross Sales Margin", "1204",  unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<table id=\"page\" width=100% border=0>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");


    JFreeChart chart = doChart(con, stmt, rs, unm, uty, sid, men, den, dnm, bnm, "Gross Sales Margin", tmpTable, monthFrom, monthTo, localDefnsDir, defnsDir);
    if(chart == null)
      scoutln(out, bytesOut, "<tr><td nowrap><p>Error creating chart</td></tr>");
    else
    {
      generalUtils.filesDeleteGivenPrefix(workingDir, "chart.");

      String newExtn = generalUtils.newRandom();

      FileOutputStream fout = new FileOutputStream(workingDir + "chart." + newExtn);

      ChartUtilities.writeChartAsPNG(fout, chart, 800, 600);

      scoutln(out, bytesOut, "<tr><td nowrap><img src=\"" + workingDir + "chart." + newExtn + "\" border=0></td></tr>");
    }

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
  private JFreeChart doChart(Connection con, Statement stmt, ResultSet rs, String unm, String uty, String sid, String men, String den, String dnm, String bnm, String chartTitle, String tmpTable, String monthFrom, String monthTo,
                             String localDefnsDir, String defnsDir) throws Exception
  {
    CategoryDataset dataSet = createDataSet(con, stmt, rs, unm, uty, sid, men, den, dnm, bnm, tmpTable, monthFrom, monthTo, localDefnsDir, defnsDir);
    if(dataSet == null)
      return null;

    String baseCurrency = accountsUtils.getBaseCurrency(con, stmt, rs, dnm, localDefnsDir, defnsDir);

    JFreeChart chart = ChartFactory.createBarChart3D(chartTitle, "Months", baseCurrency + " Value", dataSet, PlotOrientation.VERTICAL, true, true, false);

    return chart;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public CategoryDataset createDataSet(Connection con, Statement stmt, ResultSet rs, String unm, String uty, String sid, String men, String den, String dnm, String bnm, String tmpTable, String monthFromStr, String monthToStr,
                                       String localDefnsDir, String defnsDir) throws Exception
  {
    int mm = generalUtils.getMonth(generalUtils.encodeFromYYYYMMDD(monthFromStr));
    int yy = generalUtils.getYear(generalUtils.encodeFromYYYYMMDD(monthFromStr));
    monthFromStr = mm + "-" + yy;
    mm = generalUtils.getMonth(generalUtils.encodeFromYYYYMMDD(monthToStr));
    yy = generalUtils.getYear(generalUtils.encodeFromYYYYMMDD(monthToStr));
    monthToStr = mm + "-" + yy;

    // given start month and end month in format: "4-2003", determine # of months
    int numMonths = generalUtils.numMonths(monthFromStr, monthToStr, localDefnsDir, defnsDir);

    // callback to get data
    // rtns stream of values, one value relating to each month
    String data = getData(con, stmt, rs, tmpTable, monthFromStr, monthToStr, localDefnsDir, defnsDir);
    if(data.length() == 0) // error fetching data
      return null;

    int[] monthFrom = new int[1];
    int[] yearFrom  = new int[1];

    generalUtils.monthYearStrToYearAndMonth2(monthFromStr, monthFrom, yearFrom);
    mm = monthFrom[0];
    yy = yearFrom[0];

    DefaultCategoryDataset dataset2 = new DefaultCategoryDataset();

    String value;
    int count=0;
    int len = data.length();
    int x = 3; // ok\001
    while(count < numMonths && x < len)
    {
      value="";
      while(x < len && data.charAt(x) != '\001')
        value += data.charAt(x++);
      ++x;
      ++count;

      if(monthFrom[0] == 13)
      {
        monthFrom[0] = 1;
        ++yy;
      }

      dataset2.addValue(generalUtils.doubleFromStr(value), "Sales", new Month(monthFrom[0], yy));

      ++monthFrom[0];
    }

    yy = yearFrom[0];
    monthFrom[0] = mm;
    count=0;
    while(count < numMonths && x < len)
    {
      value="";
      while(x < len && data.charAt(x) != '\001')
        value += data.charAt(x++);
      ++x;
      ++count;

      if(monthFrom[0] == 13)
      {
        monthFrom[0] = 1;
        ++yy;
      }

      dataset2.addValue(generalUtils.doubleFromStr(value), "Cost of Sales", new Month(monthFrom[0], yy));

      ++monthFrom[0];
    }

    return dataset2;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String getData(Connection con, Statement stmt, ResultSet rs, String tmpTable, String monthFrom, String monthTo, String localDefnsDir, String defnsDir) throws Exception
  {
    // given start month and end month in format: "4-2003"
    int numMonths = generalUtils.numMonths(monthFrom, monthTo, localDefnsDir, defnsDir);

    if(numMonths <= 0)
      return "ok\001\002";

    int x;
    double[] values = new double[numMonths];

    // get data: rtns stream of values, one value relating to each month
    //
    int[] firstDayOfEachMonth = new int[numMonths];
    int[] month               = new int[1];
    int[] year                = new int[1];

    String firstDay="";
    generalUtils.monthYearStrToYearAndMonth2(monthFrom, month, year);
    for(x=0;x<numMonths;++x)
    {
      firstDay = "1." + month[0] + "." + year[0];
      firstDayOfEachMonth[x] = generalUtils.encode(firstDay, localDefnsDir, defnsDir);
      if(++month[0] == 13)
      {
        month[0] = 1;
        ++year[0];
      }
    }

    int highest = generalUtils.encode(generalUtils.lastDayOfMonth(firstDay, localDefnsDir, defnsDir), localDefnsDir, defnsDir) + 1;

    String res = "ok\001";

    // sales
    for(x=0;x<numMonths;++x)
      values[x] = 0.0;

    getSalesData(con, stmt, rs, tmpTable, numMonths, firstDayOfEachMonth, highest, values);

    for(x=0;x<numMonths;++x)
      res += generalUtils.doubleToStr(values[x]) + "\001";

    // CoS
    for(x=0;x<numMonths;++x)
      values[x] = 0.0;

    getCostOfSalesData(con, stmt, rs, tmpTable, numMonths, firstDayOfEachMonth, highest, values);

    for(x=0;x<numMonths;++x)
      res += generalUtils.doubleToStr(values[x]) + "\001";

    res += "\002";

    return res;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void getSalesData(Connection con, Statement stmt, ResultSet rs, String tmpTable, int numMonths, int[] firstDayOfEachMonth, int highest, double[] values) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT Amount, Date FROM " + tmpTable);

    int x, dateEncoded;
    double amt;

    while(rs.next())
    {
      amt         = generalUtils.doubleFromStr(rs.getString(1));
      dateEncoded = generalUtils.encodeFromYYYYMMDD(rs.getString(2));

      // add to app. month
      if(dateEncoded < firstDayOfEachMonth[0] || dateEncoded >= highest)
        ; // out of range
      else
      {
        x = 0;
        while(x < numMonths && dateEncoded > firstDayOfEachMonth[x])
          ++x;
        --x;
        values[x] += amt;
      }
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void getCostOfSalesData(Connection con, Statement stmt, ResultSet rs, String tmpTable, int numMonths, int[] firstDayOfEachMonth, int highest, double[] values) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT CostOfSale, Date FROM " + tmpTable);

    int x, dateEncoded;
    double cos;

    while(rs.next())
    {
      cos         = generalUtils.doubleFromStr(rs.getString(1));
      dateEncoded = generalUtils.encodeFromYYYYMMDD(rs.getString(2));

      // add to app. month
      if(dateEncoded < firstDayOfEachMonth[0] || dateEncoded >= highest)
        ; // out of range
      else
      {
        x = 0;
        while(x < numMonths && dateEncoded > firstDayOfEachMonth[x])
          ++x;
        --x;
        values[x] += cos;
      }
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

}
