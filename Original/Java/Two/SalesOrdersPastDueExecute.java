// =======================================================================================================================================================================================================
// System: ZaraStar: Charts: Sales Orders Past Due
// Module: SalesOrdersPastDueExecute.java
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

public class SalesOrdersPastDueExecute extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();

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
 
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "ChartSalesOrdersPastDue", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1100, bytesOut[0], 0, "ERR:" + p1);
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
    
    Statement stmt = null, stmt2 = null;
    ResultSet rs   = null, rs2   = null;
        
    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 1100, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "SalesOrdersPastDueExecute", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1100, bytesOut[0], 0, "ACC:" + salesPerson);      
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "SalesOrdersPastDueExecute", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1100, bytesOut[0], 0, "SID:" + salesPerson);      
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    doIt(con, stmt, stmt2, rs, rs2, out, req, unm, sid, uty, men, den, dnm, bnm, salesPerson, monthFrom, monthTo, workingDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 1100, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), salesPerson);
    
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                    String salesPerson, String monthFrom, String monthTo, String workingDir, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Sales Orders Past Due</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");
  
    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\"></head>");
  
    int[] hmenuCount = new int[1];
    
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "1100", "", "ChartSalesOrdersPastDue", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
                                
    scoutln(out, bytesOut, "<form>");
  
    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Sales Orders Past Due", "1100",  unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<table id=\"page\" width=100% border=0>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    String s;
    if(salesPerson.equals("ALL"))
      s = "Sales Orders Past Due (All)";
    else s = "Sales Orders Past Due (" + salesPerson + ")";
    
    JFreeChart chart = doChart(con, stmt, stmt2, rs, rs2, unm, uty, sid, men, den, dnm, bnm, s, salesPerson, monthFrom, monthTo, localDefnsDir, defnsDir);

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
  private JFreeChart doChart(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String unm, String uty, String sid, String men, String den, String dnm, String bnm, String chartTitle,
                             String salesPerson, String monthFrom, String monthTo, String localDefnsDir, String defnsDir) throws Exception
  {
    DefaultCategoryDataset dataSet = createDataSet(con, stmt, stmt2, rs, rs2, unm, uty, sid, men, den, dnm, bnm, salesPerson, monthFrom, monthTo, localDefnsDir, defnsDir);
    if(dataSet == null)
      return null;

    JFreeChart chart = ChartFactory.createStackedBarChart(chartTitle, "Month Issued", "Number of Sales Orders", dataSet, PlotOrientation.VERTICAL, true, true, false);

    return chart;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public DefaultCategoryDataset createDataSet(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String unm, String uty, String sid, String men, String den, String dnm, String bnm, String salesPerson,
                                              String monthFromStr, String monthToStr, String localDefnsDir, String defnsDir) throws Exception
  {
    // given start month and end month in format: "4-2003"
    // determine # of months
    int numMonths = generalUtils.numMonths(monthFromStr, monthToStr, localDefnsDir, defnsDir);

    String[] res2 = new String[1];
    String[] res3 = new String[1];

    // rtns stream of values, one value relating to each month
    String data = getData(con, stmt, stmt2, rs, rs2, salesPerson, monthFromStr, monthToStr, localDefnsDir, defnsDir, res2, res3);
    if(data.length() == 0) // error fetching data
      return null;
    
    int[] monthFrom = new int[1];
    int[] yearFrom  = new int[1];

    generalUtils.monthYearStrToYearAndMonth2(monthFromStr, monthFrom, yearFrom);
    int mm = monthFrom[0];

    int yy = yearFrom[0];

    DefaultCategoryDataset dataset = new DefaultCategoryDataset();

    String early, wip, wipLate;
    int count=0;
    int len1 = data.length();
    int len2 = data.length();
    int len3 = data.length();
    int x = 3; // ok\001
    int y = 3; // ok\001
    int z = 3; // ok\001
    while(count < numMonths && x < len1)
    {
      early="";
      while(x < len1 && data.charAt(x) != '\001')
        early += data.charAt(x++);
      ++x;
      ++count;

      wip="";
      while(y < len2 && res2[0].charAt(y) != '\001')
        wip += res2[0].charAt(y++);
      ++y;

      wipLate="";
      while(z < len3 && res3[0].charAt(z) != '\001')
        wipLate += res3[0].charAt(z++);
      ++z;

      if(monthFrom[0] == 13)
      {
        monthFrom[0] = 1;
        ++yy;
      }
//      series1.add(new Month(monthFrom[0], yy), generalUtils.doubleFromStr(value));
      dataset.addValue(generalUtils.intFromStr(early),   "Early",      new Month(monthFrom[0], yy));
      dataset.addValue(generalUtils.intFromStr(wip),     "WIP",        new Month(monthFrom[0], yy));
      dataset.addValue(generalUtils.intFromStr(wipLate), "WIP - Late", new Month(monthFrom[0], yy));

      ++monthFrom[0];
    }

    return dataset;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String getData(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String salesPerson, String monthFrom, String monthTo, String localDefnsDir, String defnsDir, String[] res2, String[] res3) throws Exception
  {
    // given start month and end month in format: "4-2003"
    int numMonths = generalUtils.numMonths(monthFrom, monthTo, localDefnsDir, defnsDir);

    if(numMonths <= 0)
    {
      return "ok\001\002";
    }
    
    int x;
    int[] early   = new int[numMonths];
    int[] wip     = new int[numMonths];
    int[] wipLate = new int[numMonths];

    // get data: rtns stream of values, one value relating to each month

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
      early[x] = wip[x] = wipLate[x] = 0;

    determineData(con, stmt, stmt2, rs, rs2, salesPerson, numMonths, firstDayOfEachMonth, dateFrom, dateTo, localDefnsDir, defnsDir, early, wip, wipLate);

    String res = "ok\001";
    for(x=0;x<numMonths;++x)
      res += generalUtils.intToStr(early[x]) + "\001";
    res += "\002";

    res2[0] = "ok\001";
    for(x=0;x<numMonths;++x)
      res2[0] += generalUtils.intToStr(wip[x]) + "\001";
    res2[0] += "\002";

    res3[0] = "ok\001";
    for(x=0;x<numMonths;++x)
      res3[0] += generalUtils.intToStr(wipLate[x]) + "\001";
    res3[0] += "\002";

    return res;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void determineData(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String salesPerson, int numMonths, int[] firstDayOfEachMonth, String dateFrom, String dateTo, String localDefnsDir, String defnsDir,
                             int[] early, int[] wip, int[] wipLate) throws Exception
  {
    stmt = con.createStatement();

    // for each SO in date range
    //   if no DO line with SOCode, add to month count    
    
    String where = "";
    if(! salesPerson.equals("ALL"))
      where = " AND t1.SalesPerson = '" + salesPerson + "' ";      
    
    rs = stmt.executeQuery("SELECT t1.SOCode, t1.Date, t2.DeliveryDate FROM sol AS t2 INNER JOIN so AS t1 ON t2.SOCode = t1.SOCode WHERE t1.Status != 'C' " + where + " AND t1.Date >= {d '" + dateFrom + "'} AND t1.Date <= {d '" + dateTo + "'} "
                         + "ORDER BY SOCode");

    int dateEncoded, count;
    String soCode, deliveryDate;
    String[] lastSOCode = new String[1]; lastSOCode[0] = "";
    boolean[] alreadyCounted = new boolean[1]; alreadyCounted[0] = false;

    int todayEncoded = generalUtils.todayEncoded(localDefnsDir, defnsDir);

    while(rs.next())
    {    
      soCode       = rs.getString(1);
      dateEncoded  = generalUtils.encodeFromYYYYMMDD(rs.getString(2));
      deliveryDate = rs.getString(3);
      
      if(deliveryDate == null || deliveryDate.length() == 0)
        deliveryDate = "1970-01-01";

      count = checkDOLines(con, stmt2, rs2, soCode, deliveryDate);

      if(generalUtils.encodeFromYYYYMMDD(deliveryDate) < todayEncoded)
      {
        if(count > 0) // delivered
          maybeUpdate(early, soCode, lastSOCode, alreadyCounted, numMonths, dateEncoded, firstDayOfEachMonth);
        else maybeUpdate(wip, soCode, lastSOCode, alreadyCounted, numMonths, dateEncoded, firstDayOfEachMonth);
      }
      else // deliveryDate >= today
      {
        if(count > 0) // delivered
          maybeUpdate(early, soCode, lastSOCode, alreadyCounted, numMonths, dateEncoded, firstDayOfEachMonth);
        else maybeUpdate(wipLate, soCode, lastSOCode, alreadyCounted, numMonths, dateEncoded, firstDayOfEachMonth);
      }     
    }      

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }
      
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void maybeUpdate(int[] updateThis, String soCode, String[] lastSOCode, boolean[] alreadyCounted, int numMonths, int dateEncoded, int[] firstDayOfEachMonth) throws Exception
  {
    int x;
    
    if(soCode.equals(lastSOCode[0]))
    {
      if(! alreadyCounted[0])
      {
        x=0; // 1
        while(x < (numMonths-1) && dateEncoded > firstDayOfEachMonth[x])
          ++x;
        updateThis[x] += 1;
        
        alreadyCounted[0] = true;
      }
    }
    else // stepped-on to next SO
    {
      x=0; // 1
      while(x < (numMonths-1) && dateEncoded > firstDayOfEachMonth[x])
        ++x;
      updateThis[x] += 1;
      
      alreadyCounted[0] = true;
    }

    lastSOCode[0] = soCode;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int checkDOLines(Connection con, Statement stmt, ResultSet rs, String soCode, String deliveryDate) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t1.Date FROM dol AS t2 INNER JOIN do AS t1 ON t2.DOCode = t1.DOCode WHERE t2.SOCode = '" + soCode + "' AND t1.Status != 'C' AND t1.Date > {d '" + deliveryDate + "'}");

    int count;
    
    if(rs.next())
      count = 1;
    else count = 0;    
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return count;
  }

}
