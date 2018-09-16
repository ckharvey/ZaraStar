// =======================================================================================================================================================================================================
// System: ZaraStar: Charts: Balance Sheet
// Module: ChartBalanceSheetExecute.java
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

public class ChartBalanceSheetExecute extends HttpServlet
{
  GeneralUtils  generalUtils  = new GeneralUtils();
  MessagePage  messagePage  = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  AccountsUtils  accountsUtils  = new AccountsUtils();
  AccountsGenerateTBReport accountsGenerateTBReport = new AccountsGenerateTBReport();
  DefinitionTables  definitionTables  = new DefinitionTables();
 
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
 
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "ChartBalanceSheet", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1102, bytesOut[0], 0, "ERR:" + p1+":"+p2);
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
    
    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 1102, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "ChartBalanceSheetExecute", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1102, bytesOut[0], 0, "ACC:" + monthFrom+":"+monthTo);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "ChartBalanceSheetExecute", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1102, bytesOut[0], 0, "SID:" + monthFrom+":"+monthTo);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    doIt(con, stmt, rs, out, req, unm, sid, uty, men, den, dnm, bnm, monthFrom, monthTo, workingDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 1102, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), monthFrom+":"+monthTo);
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String monthFrom, String monthTo, String workingDir,
                    String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Balance Sheet</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");
  
    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\"></head>");
  
    int[] hmenuCount = new int[1];
    
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "1102", "", "ChartBalanceSheet", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
                                
    scoutln(out, bytesOut, "<form>");
  
    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Balance Sheet:" + monthFrom + " to " + monthTo, "1102",  unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<table id=\"page\" width=100% border=0>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    JFreeChart chart = doChart(con, stmt, rs, unm, uty, sid, men, den, dnm, bnm, "Balance Sheet", monthFrom, monthTo, localDefnsDir, defnsDir);

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
  private JFreeChart doChart(Connection ofsaCon, Statement ofsaStmt, ResultSet ofsaRs, String unm, String uty, String sid, String men, String den, String dnm, String bnm, String chartTitle, String monthFrom, String monthTo, String localDefnsDir,
                             String defnsDir) throws Exception
  {
    CategoryDataset dataSet = createDataSet(ofsaCon, ofsaStmt, ofsaRs, unm, uty, sid, men, den, dnm, bnm, monthFrom, monthTo, localDefnsDir, defnsDir);
    if(dataSet == null)
      return null;

    JFreeChart chart = ChartFactory.createStackedBarChart(chartTitle, "End of Month", "$ Value", dataSet, PlotOrientation.VERTICAL, true, true, false);

    return chart;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public CategoryDataset createDataSet(Connection ofsaCon, Statement ofsaStmt, ResultSet ofsaRs, String unm, String uty, String sid, String men, String den, String dnm, String bnm, String monthFromStr, String monthToStr, String localDefnsDir,
                                       String defnsDir) throws Exception
  {
    // given start month and end month in format: "4-2003"
    // determine # of months
    int numMonths = generalUtils.numMonths(monthFromStr, monthToStr, localDefnsDir, defnsDir);

    String[] financialYearStartMonth = new String[1];
    String[] financialYearEndMonth   = new String[1];

    definitionTables.getAppConfigFinancialYearMonths(ofsaCon, ofsaStmt, ofsaRs, dnm, financialYearStartMonth, financialYearEndMonth);

    int startMonth = generalUtils.detMonthNumFromMonthName(financialYearStartMonth[0]);

    double[] currentAssets = new double[1];
    double[] fixedAssets   = new double[1];
    double[] liabilities   = new double[1];
    double[] equity        = new double[1];
    double[] profitAndLoss = new double[1];

    int[] month = new int[1];
    int[] year  = new int[1];

    String monthStart, monthEnd;
    
    generalUtils.monthYearStrToYearAndMonth2(monthFromStr, month, year);

    DefaultCategoryDataset dataset = new DefaultCategoryDataset();
    monthStart = "1." + month[0] + "." + year[0];
 
    String financialYearStartDate = year[0] + "-" + generalUtils.detMonthNumFromMonthName(financialYearStartMonth[0]) + "-01";
    String previousFinancialYearStartDate = (year[0] - 1) + "-" + generalUtils.detMonthNumFromMonthName(financialYearStartMonth[0]) + "-01";

    int yyyy, count = 0;
    while(count < numMonths)
    {
      monthEnd   = generalUtils.lastDayOfMonth(("1." + month[0] + "." + year[0]), localDefnsDir, defnsDir);

      yyyy = year[0];
      if(month[0] < startMonth)
        --yyyy;

      getAccountsData(ofsaCon, ofsaStmt, ofsaRs, generalUtils.intToStr(yyyy), generalUtils.convertDateToSQLFormat(monthStart), generalUtils.convertDateToSQLFormat(monthEnd),
              financialYearStartDate, previousFinancialYearStartDate, dnm, localDefnsDir, defnsDir, currentAssets, fixedAssets, liabilities,
                      equity, profitAndLoss);

      dataset.addValue(currentAssets[0], "Current Assets", new Month(month[0], year[0]));
      dataset.addValue(fixedAssets[0],   "Fixed Assets",   new Month(month[0], year[0]));
      dataset.addValue(liabilities[0],   "Liabilities",    new Month(month[0], year[0]));
      dataset.addValue(equity[0],        "Equity",         new Month(month[0], year[0]));
      dataset.addValue(profitAndLoss[0], "P&L",            new Month(month[0], year[0]));
            
      if(++month[0] == 13)
      {
        month[0] = 1;
        ++year[0];
      }
      
      ++count;
    }

    return dataset;
  }
  
  // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void getAccountsData(Connection ofsaCon, Statement ofsaStmt, ResultSet ofsaRs, String year, String dateFrom, String dateTo,
          String financialYearStartDate, String previousFinancialYearStartDate, String dnm, String localDefnsDir, String defnsDir, double[] currentAssets,
                               double[] fixedAssets, double[] liabilities, double[] equity, double[] profitAndLoss) throws Exception
  {
    Connection accCon = null;
    Statement accStmt = null;
    ResultSet accRs   = null;
    
    currentAssets[0] = fixedAssets[0] = liabilities[0] = equity[0] = profitAndLoss[0] = 0.0;

    int numAccs = 0;

    try
    {
      String uName = directoryUtils.getMySQLUserName();
      String pWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();
    
      accCon = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_accounts_" + year + "?user=" + uName + "&password=" + pWord);

      accStmt = accCon.createStatement();

      accRs = accStmt.executeQuery("SELECT COUNT(*) AS rowcount FROM acctdefn WHERE Active = 'Y'"); 

      accRs.next();
      numAccs = accRs.getInt("rowcount");

      if(accRs   != null) accRs.close();                                 
      if(accStmt != null) accStmt.close();

      String[] accCodes = new String[numAccs];
      double[] accVals  = new double[numAccs];
      
      double[][] accEndValsDr = new double[1][];
      double[][] accEndValsCr = new double[1][];
      accEndValsDr = new double[14][numAccs];
      accEndValsCr = new double[14][numAccs];

      String debtorsAccount = "", creditorsAccount = "", otherDebtorsAccount = "", otherCreditorsAccount = "", salesAccount = "", purchasesAccount = "", gstOutputAccount = "", gstInputAccount = "", exchangeGainAccount = "",
             exchangeLossAccount = "", salesReturnedAccount = "", purchasesReturnedAccount = "", stockAccount = "", discountReceivedAccount = "", discountGivenAccount = "";
      
      accStmt = accCon.createStatement();

      accRs = accStmt.executeQuery("SELECT AccCode, Category FROM acctdefn WHERE Active = 'Y' ORDER BY AccCode"); 

      int count = 0;
      String category;
      
      while(accRs.next())
      {
        accCodes[count] = accRs.getString(1);
        category        = accRs.getString(2);

        if(category.equals("Current Assets - Trade Debtors"))
          debtorsAccount = accRs.getString(1);
        else
        if(category.equals("Current Assets - Other Debtors"))
          otherDebtorsAccount = accRs.getString(1);
        else
        if(category.equals("Liabilities - Trade Creditors"))
          creditorsAccount = accRs.getString(1);
        else
        if(category.equals("Liabilities - Other Creditors"))
          otherCreditorsAccount = accRs.getString(1);
        else
        if(category.equals("Income - Sales"))
          salesAccount = accRs.getString(1);
        else
        if(category.equals("Income - Sales Returned"))
          salesReturnedAccount = accRs.getString(1);
        else
        if(category.equals("Cost of Sales - Purchases"))
          purchasesAccount = accRs.getString(1);
        else
        if(category.equals("Cost of Sales - Purchases Returned"))
          purchasesReturnedAccount = accRs.getString(1);
        else
        if(category.equals("Liabilities - GST Output"))
          gstOutputAccount = accRs.getString(1);
        else
        if(category.equals("Liabilities - GST Input"))
          gstInputAccount = accRs.getString(1);
        else
        if(category.equals("Income - Exchange Gain"))
          exchangeGainAccount = accRs.getString(1);
        else
        if(category.equals("Cost of Sales - Exchange Loss"))
          exchangeLossAccount = accRs.getString(1);
        else
        if(category.equals("Current Assets - Stock"))
          stockAccount = accRs.getString(1);
        else
        if(category.equals("Cost of Sales - Discount Received"))
          discountReceivedAccount = accRs.getString(1);
        else
        if(category.equals("Income - Discount Given"))
          discountGivenAccount = accRs.getString(1);

        ++count;
      }

      if(accRs   != null) accRs.close();                                 
      if(accStmt != null) accStmt.close();

      String baseCurrency = accountsUtils.getBaseCurrency(ofsaCon, ofsaStmt, ofsaRs, dnm, localDefnsDir, defnsDir);

      int[] monthEnds = new int[13];
      int[] numMonths = new int[1];

      accountsGenerateTBReport.scanDocuments(accCon, ofsaCon, accStmt, ofsaStmt, accRs, ofsaRs, true, dateFrom, dateTo, financialYearStartDate, previousFinancialYearStartDate, accCodes, accEndValsDr, accEndValsCr, numAccs, monthEnds, numMonths, debtorsAccount, creditorsAccount,
                           otherDebtorsAccount, otherCreditorsAccount, salesAccount, purchasesAccount, gstOutputAccount, gstInputAccount, exchangeGainAccount, exchangeLossAccount, salesReturnedAccount, purchasesReturnedAccount, stockAccount,
                           discountReceivedAccount, discountGivenAccount, baseCurrency, null, null, null, null, null, null, 0, 0, ""   , "", dnm);

      double endBal;
      int accCount = 0;
        
      while(accCount < numAccs)
      {
//        accCode = accCodes[accCount];
        
        endBal = accEndValsDr[0][accCount] - accEndValsCr[0][accCount];

        if(endBal < 0) endBal *= -1;
        
        accVals[accCount++] = endBal;
      }

      totalEachSection(accCon, accStmt, accRs, year, dnm, numAccs, accCodes, accVals, currentAssets, fixedAssets, liabilities, equity, profitAndLoss, localDefnsDir, defnsDir);
      
      if(accCon != null) accCon.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(accRs   != null) accRs.close();                                 
      if(accStmt != null) accStmt.close();
      if(accCon != null) accCon.close();
    }
  }  

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void totalEachSection(Connection accCon, Statement accStmt, ResultSet accRs, String year, String dnm, int numAccs, String[] accCodes, double[] accVals, double[] currentAssets, double[] fixedAssets, double[] liabilities, double[] equity,
                                double[] profitAndLoss, String localDefnsDir, String defnsDir) throws Exception
  {
    String accCode, category;
    int accCount = 0;
        
    while(accCount < numAccs)
    {
      accCode = accCodes[accCount];
        
      accStmt = accCon.createStatement();
      accRs = accStmt.executeQuery("SELECT Category FROM acctdefn WHERE AccCode = '" + accCode + "'"); 
      
      if(accRs.next())
        category = accRs.getString(1);
      else category = "";
      
      if(accRs   != null) accRs.close();                                 
      if(accStmt != null) accStmt.close();
    
      if(category.startsWith("Equity - "))
        equity[0] += accVals[accCount];
      else
      if(category.startsWith("Liabilities"))
        liabilities[0] += accVals[accCount];
      else
      if(category.startsWith("Current Assets"))
        currentAssets[0] += accVals[accCount];
      else
      if(category.startsWith("Fixed Assets"))
        fixedAssets[0] += accVals[accCount];
      // else a Trading account
        
      ++accCount;
    }

    profitAndLoss[0] = calcPL(accCon, accStmt, accRs, year, accCodes, accVals, numAccs, dnm, localDefnsDir, defnsDir);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public double calcPL(Connection accCon, Statement accStmt, ResultSet accRs, String year, String[] accCodes, double[] accVals, int numAccs, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    double plTotal = 0.0;

    try
    {
      accStmt = accCon.createStatement();

      int accCount = 0;
      String accCode, type;

      while(accCount < numAccs)
      {
        accCode = accCodes[accCount];

        accStmt = accCon.createStatement();

        accRs = accStmt.executeQuery("SELECT Type FROM acctdefn WHERE AccCode = '" + accCode + "'");

        if(accRs.next())
          type = accRs.getString(1);
        else type = "B"; // just-in-case

        if(accRs   != null) accRs.close();
        if(accStmt != null) accStmt.close();

        if(! type.equals("B"))
          plTotal += generalUtils.doubleDPs(accVals[accCount], '2');

        ++accCount;
      }

      if(accCon  != null) accCon.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(accRs   != null) accRs.close();
      if(accStmt != null) accStmt.close();
      if(accCon  != null) accCon.close();
    }

    return plTotal * -1;
  }

}
