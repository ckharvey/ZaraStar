// =======================================================================================================================================================================================================
// System: ZaraStar: Charts: Sales Invoice Turnover
// Module: ChartSalesInvoiceTurnoverExecute.java
// Author: C.K.Harvey
// Copyright (c) 2003-09 Christopher Harvey. All Rights Reserved.
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

import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.data.category.*;
import org.jfree.data.time.*;

public class ChartSalesInvoiceTurnoverExecute extends HttpServlet
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
      p1  = req.getParameter("p1"); // salesPerson
      p2  = req.getParameter("p2"); // monthFrom
      p3  = req.getParameter("p3"); // monthTo

      if(p1 == null) p1 = "ALL";

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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "ChartSalesInvoiceTurnover", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1103, bytesOut[0], 0, "ERR: " + e + " " + p1);
      if(out != null) out.flush(); 
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String salesPerson, String monthFrom, String monthTo, int[] bytesOut) throws Exception
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

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 1103, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "ChartSalesInvoiceTurnoverExecute", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1103, bytesOut[0], 0, "ACC:" + salesPerson);
      
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "ChartSalesInvoiceTurnoverExecute", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1103, bytesOut[0], 0, "SID:" + salesPerson);
      
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    doIt(con, stmt, rs, out, req, unm, sid, uty, men, den, dnm, bnm, salesPerson, monthFrom, monthTo, workingDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 1103, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), salesPerson);
    
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String salesPerson, String monthFrom,
                    String monthTo, String workingDir, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Sales Invoice Turnover</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\"></head>");

    int[] hmenuCount = new int[1];
    
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "1103", "", "ChartSalesInvoiceTurnover", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<form>");

    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Sales Invoice Turnover", "1103",  unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<table id=\"page\" width=100% border=0>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    String s;
    if(salesPerson.equals("ALL"))
      s = "Sales Invoices (and Debit Notes) for All SalesPeople";
    else s = "Sales Invoices (and Debit Notes) for " + salesPerson;

    JFreeChart chart = doChart(con, stmt, rs, s, salesPerson, monthFrom, monthTo, dnm, localDefnsDir, defnsDir);

    if(chart == null)
      scoutln(out, bytesOut, "<tr><td nowrap><p>Error creating chart</td></tr>");
    else
    {
      generalUtils.filesDeleteGivenPrefix(workingDir, "chart.");

      String newExtn = generalUtils.newRandom();

      FileOutputStream fout = new FileOutputStream(workingDir + "chart." + newExtn);

      ChartUtilities.writeChartAsPNG(fout, chart, 1000, 700);

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
  private JFreeChart doChart(Connection con, Statement stmt, ResultSet rs, String chartTitle, String salesPerson, String monthFrom, String monthTo, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    DefaultCategoryDataset dataSet = createDataSet(con, stmt, rs, salesPerson, monthFrom, monthTo, localDefnsDir, defnsDir);
    if(dataSet == null)
      return null;

    String baseCurrency = accountsUtils.getBaseCurrency(con, stmt, rs, dnm, localDefnsDir, defnsDir);

    JFreeChart chart = ChartFactory.createStackedBarChart(chartTitle, "Month Issued", "Value (" + baseCurrency + ")", dataSet, PlotOrientation.VERTICAL, true, true, false);

    return chart;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private DefaultCategoryDataset createDataSet(Connection con, Statement stmt, ResultSet rs, String salesPerson, String monthFromStr, String monthToStr, String localDefnsDir, String defnsDir) throws Exception
  {
    // given start month and end month in format: "4-2003"
    // determine # of months
    int numMonths = generalUtils.numMonths(monthFromStr, monthToStr, localDefnsDir, defnsDir);

    if(numMonths <= 0) // dateFrom < dateTo
      return null;

    String[] res1 = new String[1];

    // rtns stream of values, one value relating to each month
    getData(con, stmt, rs, salesPerson, numMonths, monthFromStr, localDefnsDir, defnsDir, res1);
    
    int[] monthFrom = new int[1];
    int[] yearFrom  = new int[1];

    generalUtils.monthYearStrToYearAndMonth2(monthFromStr, monthFrom, yearFrom);

    int yy = yearFrom[0];

    DefaultCategoryDataset dataset = new DefaultCategoryDataset();

    String invoiced;
    int count=0;
    int len1 = res1[0].length();
    int x = 3; // ok\001
    while(count < numMonths && x < len1)
    {
      invoiced="";
      while(x < len1 && res1[0].charAt(x) != '\001')
        invoiced += res1[0].charAt(x++);
      ++x;
      ++count;

      if(monthFrom[0] == 13)
      {
        monthFrom[0] = 1;
        ++yy;
      }

      dataset.addValue(generalUtils.doubleFromStr(invoiced), "Invoiced", new Month(monthFrom[0], yy));

      ++monthFrom[0];
    }

    return dataset;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void getData(Connection con, Statement stmt, ResultSet rs, String salesPerson, int numMonths, String monthFrom, String localDefnsDir, String defnsDir, String[] res1) throws Exception
  {
    int x;
    double[] invoiced = new double[numMonths];

    // get data: rtns stream of values, one value relating to each month
    //
    int[] firstDayOfEachMonth = new int[numMonths];
    int[] month               = new int[1];
    int[] year                = new int[1];

    boolean first = true;
    String firstDay="", dateFrom = "1970-01-01";
    generalUtils.monthYearStrToYearAndMonth2(monthFrom, month, year);

    for(x=0;x<numMonths;++x)
    {
      firstDay = "1." + month[0] + "." + year[0];

      if(first)
      {
        dateFrom = generalUtils.convertDateToSQLFormat(firstDay);
        first = false;
      }

      firstDayOfEachMonth[x] = generalUtils.encode(firstDay, localDefnsDir, defnsDir);
      if(++month[0] == 13)
      {
        month[0] = 1;
        ++year[0];
      }
    }

    String dateTo = generalUtils.convertDateToSQLFormat(generalUtils.lastDayOfMonth(firstDay, localDefnsDir, defnsDir));

    for(x=0;x<numMonths;++x)
      invoiced[x] = 0.0;

    determineData(con, stmt, rs, salesPerson, dateFrom, dateTo, numMonths, firstDayOfEachMonth, invoiced);

    res1[0] = "ok\001";
    for(x=0;x<numMonths;++x)
      res1[0] += generalUtils.doubleToStr(invoiced[x]) + "\001";
    res1[0] += "\002";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void determineData(Connection con, Statement stmt, ResultSet rs, String salesPerson, String dateFrom, String dateTo, int numMonths, int[] firstDayOfEachMonth, double[] invoiced) throws Exception
  {
    stmt = con.createStatement();

    // for each invoice in date range, add to month count

    String where = "";
    if(! salesPerson.equals("ALL"))
      where = " AND SalesPerson = '" + salesPerson + "' ";

    rs = stmt.executeQuery("SELECT TotalTotal, Date FROM invoice WHERE Status != 'C' " + where + " AND Date >= {d '" + dateFrom + "'} AND Date <= {d '" + dateTo + "'}");

    double totalTotal;
    int dateEncoded;

    while(rs.next())
    {
      totalTotal  = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(1)), '2');
      dateEncoded = generalUtils.encodeFromYYYYMMDD(rs.getString(2));

      updateMonth(invoiced, totalTotal, numMonths, dateEncoded, firstDayOfEachMonth, rs.getString(2));
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    stmt = con.createStatement();

    // for each DN in date range, add to month count

    rs = stmt.executeQuery("SELECT TotalTotal, Date FROM debit WHERE Status != 'C' " + where + " AND Date >= {d '" + dateFrom + "'} AND Date <= {d '" + dateTo + "'}");

    while(rs.next())
    {
      totalTotal  = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(1)), '2');
      dateEncoded = generalUtils.encodeFromYYYYMMDD(rs.getString(2));

      updateMonth(invoiced, totalTotal, numMonths, dateEncoded, firstDayOfEachMonth, rs.getString(2));
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void updateMonth(double[] updateThis, double amount, int numMonths, int dateEncoded, int[] firstDayOfEachMonth, String date) throws Exception
  {
    int x = numMonths - 1;

    boolean quit = false;

    while(! quit && dateEncoded < firstDayOfEachMonth[x])
    {
      --x;
      if(x == -1)
      {
        quit = true;
      }
    }
   
    if(quit) x=0;
      
    updateThis[x] += amount;
  }

}
