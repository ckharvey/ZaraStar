// =======================================================================================================================================================================================================
// System: ZaraStar: Charts: Purchases vs. Sales generation
// Module: ChartPurchasesSales.java
// Author: C.K.Harvey
// Copyright (c) 2003-07 Christopher Harvey. All Rights Reserved.
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
import java.net.*;
import java.sql.*;
import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.*;
import org.jfree.data.time.*;
import org.jfree.data.xy.*;

public class ChartPurchasesSales extends HttpServlet
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
      p1  = req.getParameter("p1"); // itemCode
      p2  = req.getParameter("p2"); // monthFrom
      p3  = req.getParameter("p3"); // monthTo
      
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
 
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "MainPageUtils3", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1003, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den,
                    String dnm, String bnm, String itemCode, String monthFrom, String monthTo, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String workingDir       = directoryUtils.getUserDir('W', dnm, unm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;
    
    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 1003, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "MainPageUtils3a", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1003, bytesOut[0], 0, "ACC:" + itemCode);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "MainPageUtils3a", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1003, bytesOut[0], 0, "SID:" + itemCode);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    doIt(con, stmt, rs, out, req, unm, sid, uty, men, den, dnm, bnm, itemCode, monthFrom, monthTo, workingDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 1003, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), itemCode);
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm,
                    String bnm, String itemCode, String monthFrom, String monthTo, String workingDir, String localDefnsDir, String defnsDir,
                    int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Purchases vs. Sales</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");
  
    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir)
                         + "general.css\"></head>");
  
    int[] hmenuCount = new int[1];
        
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "1003", "", "MainPageUtils3", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
                                
    scoutln(out, bytesOut, "<form>");
  
    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Purchases vs. Sales", "1003",  unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<table id=\"page\" width=100% border=0>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    itemCode = itemCode.toUpperCase();

    JFreeChart chart = doChart(unm, uty, sid, men, den, dnm, bnm, "Purchases vs. Sales: " + itemCode, itemCode, monthFrom, monthTo,
                               localDefnsDir, defnsDir);
    if(chart == null)
    {
      scoutln(out, bytesOut, "<tr><td nowrap><p>Error creating chart</td></tr>");
    }
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

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private JFreeChart doChart(String unm, String uty, String sid, String men, String den, String dnm, String bnm, String chartTitle,
                             String itemCode, String monthFrom, String monthTo, String localDefnsDir, String defnsDir) throws Exception
  {
    XYDataset dataSet = createDataSet(unm, uty, sid, men, den, dnm, bnm, itemCode, monthFrom, monthTo, localDefnsDir, defnsDir);
    if(dataSet == null)
      return null;

    JFreeChart chart = ChartFactory.createTimeSeriesChart(chartTitle, "Time", "Quantity", dataSet, true, true, false);

    XYPlot plot = chart.getXYPlot();

    XYItemRenderer renderer = plot.getRenderer();
    if(renderer instanceof StandardXYItemRenderer)
    {
      StandardXYItemRenderer rr = (StandardXYItemRenderer)renderer;
    }

    return chart;
  }

  // ------------------------------------------------------------------------------------------------------------------------------------------------------
  public XYDataset createDataSet(String unm, String uty, String sid, String men, String den, String dnm, String bnm, String itemCode,
                                 String monthFromStr, String monthToStr, String localDefnsDir, String defnsDir) throws Exception
  {
    // given start month and end month in format: "4-2003"
    // determine # of months
    int numMonths = generalUtils.numMonths(monthFromStr, monthToStr, localDefnsDir, defnsDir);

    // callback to get data
    // rtns stream of values, one value relating to each month
    String data = getData(unm, uty, sid, men, den, dnm, bnm, itemCode, monthFromStr, monthToStr, localDefnsDir, defnsDir);
    if(data.length() == 0) // error fetching data
      return null;

    TimeSeries series1 = new TimeSeries("Purchases", Month.class);

    int[] monthFrom = new int[1];
    int[] yearFrom  = new int[1];

    generalUtils.monthYearStrToYearAndMonth2(monthFromStr, monthFrom, yearFrom);
    int mm = monthFrom[0];

    int yy = yearFrom[0];

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
      series1.add(new Month(monthFrom[0], yy), generalUtils.doubleFromStr(value));

      ++monthFrom[0];
    }

    TimeSeries series2 = new TimeSeries("Sales", Month.class);

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

      series2.add(new Month(monthFrom[0], yy), generalUtils.doubleFromStr(value));

      ++monthFrom[0];
    }

    TimeSeries series3 = new TimeSeries("Stock", Month.class);

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

      series3.add(new Month(monthFrom[0], yy), generalUtils.doubleFromStr(value));

      ++monthFrom[0];
    }

    TimeSeriesCollection dataset2 = new TimeSeriesCollection();
    dataset2.addSeries(series1);
    dataset2.addSeries(series2);
    dataset2.addSeries(series3);

    return dataset2;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------------
  private String getData(String unm, String uty, String sid, String men, String den, String dnm, String bnm, String itemCode,
                         String monthFrom, String monthTo, String localDefnsDir, String defnsDir) throws Exception
  {
    String userName = directoryUtils.getMySQLUserName();
    String passWord = directoryUtils.getMySQLPassWord();
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + userName + "&password=" + passWord);

    Statement stmt = null;
    ResultSet rs   = null;
    
    // given start month and end month in format: "4-2003"
    int numMonths = generalUtils.numMonths(monthFrom, monthTo, localDefnsDir, defnsDir);

    if(numMonths <= 0)
    {
      return "ok\001\002";
    }
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

    // purchases
    for(x=0;x<numMonths;++x)
      values[x] = 0.0;

    getPurchasesData(con, stmt, rs, itemCode, numMonths, firstDayOfEachMonth, highest, localDefnsDir, defnsDir, values);

    String res = "ok\001";

    for(x=0;x<numMonths;++x)
      res += generalUtils.doubleToStr(values[x]) + "\001";

    // sales
    for(x=0;x<numMonths;++x)
      values[x] = 0.0;

    getSalesData(con, stmt, rs, itemCode, numMonths, firstDayOfEachMonth, highest, localDefnsDir, defnsDir, values);

    for(x=0;x<numMonths;++x)
      res += generalUtils.doubleToStr(values[x]) + "\001";

    // stock
    for(x=0;x<numMonths;++x)
      values[x] = 0.0;

    getStockData(itemCode, numMonths, firstDayOfEachMonth, highest, unm, uty, sid, men, den, dnm, bnm, localDefnsDir, defnsDir, values);

    for(x=0;x<numMonths;++x)
      res += generalUtils.doubleToStr(values[x]) + "\001";

    res += "\002";

    if(con != null) con.close();  

    return res;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void getSalesData(Connection con, Statement stmt, ResultSet rs, String itemCode, int numMonths, int[] firstDayOfEachMonth, int highest,
                            String localDefnsDir, String defnsDir, double[] values) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.Quantity, t1.Date FROM sol AS t2 INNER JOIN so AS t1 ON t2.SOCode = t1.SOCode WHERE t2.ItemCode = '"
                           + itemCode + "' AND t1.Status != 'C'");

    int x, dateEncoded;
    double qty;

    while(rs.next())
    {    
      qty         = generalUtils.doubleFromStr(rs.getString(1));
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
        values[x] += qty;
      }
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // ------------------------------------------------------------------------------------------------------------------------------------------------------
  private void getPurchasesData(Connection con, Statement stmt, ResultSet rs, String itemCode, int numMonths, int[] firstDayOfEachMonth, int highest,
                                String localDefnsDir, String defnsDir, double[] values) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.Quantity, t1.Date FROM pol AS t2 INNER JOIN po AS t1 ON t2.POCode = t1.POCode WHERE t2.ItemCode = '"
                           + itemCode + "' AND t1.Status != 'C'");

    int x, dateEncoded;
    double qty;

    while(rs.next())
    {    
      qty         = generalUtils.doubleFromStr(rs.getString(1));
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
        values[x] += qty;
      }
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // ------------------------------------------------------------------------------------------------------------------------------------------------------
  private void getStockData(String itemCode, int numMonths, int[] firstDayOfEachMonth, int highest, String unm, String uty, String sid,
                            String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, double[] values)
                            throws Exception
  {
    String dateTo;

    for(int x=0;x<numMonths;++x)
    {
      if(x < (numMonths - 1))
        dateTo = generalUtils.decode((firstDayOfEachMonth[x + 1] - 1), localDefnsDir, defnsDir);
      else dateTo = generalUtils.decode(highest, localDefnsDir, defnsDir);

      values[x] = getStockLevels(itemCode, dateTo, unm, uty, sid, men, den, dnm, bnm, localDefnsDir);
    }
  }
  
  // ------------------------------------------------------------------------------------------------------------------------------------------------------
  private double getStockLevels(String itemCode, String dateTo, String unm, String uty, String sid, String men, String den, String dnm,
                                String bnm, String localDefnsDir) throws Exception
  {
    String traceList = getStockLevelsViaTrace(itemCode, dateTo, unm, uty, sid, men, den, dnm, bnm, localDefnsDir);
    // traceList = "SamLeong\001150000\001SyedAlwi\00150000\001\0"

    int y=0, len = traceList.length();
    String thisQty;
    double totalStockLevel = 0.0;

    while(y < len) // just-in-case
    {
      while(y < len && traceList.charAt(y) != '\001')
        ++y;
      ++y;

      thisQty = "";
      while(y < len && traceList.charAt(y) != '\001')
        thisQty += traceList.charAt(y++);
      ++y;

      totalStockLevel += generalUtils.doubleFromStr(thisQty);
    }

    return totalStockLevel;  
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private String getStockLevelsViaTrace(String itemCode, String dateTo, String unm, String uty, String sid, String men, String den,
                                        String dnm, String bnm, String localDefnsDir) throws Exception
  { 
    URL url = new URL("http://" + serverUtils.serverToCall("OFSA", localDefnsDir) + "/central/servlet/StockLevelsGenerate?unm=" + unm + "&sid=" + sid
                    + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=" + itemCode + "&p2=" + dateTo + "&bnm=" + bnm);

    URLConnection uc = url.openConnection();
    uc.setDoInput(true);
    uc.setUseCaches(false);
    uc.setDefaultUseCaches(false);

    uc.setRequestProperty("Content-Type", "application/octet-stream");

    BufferedReader di = new BufferedReader(new InputStreamReader(uc.getInputStream()));

    String t="", s = di.readLine();
    while(s != null)
    {
      t += s;
      s = di.readLine();
    }

    di.close();

    return t;
  }

}
