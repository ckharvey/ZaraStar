// =============================================================================================================================================================
// System: ZaraStar: Analytic: Purchases Reconciliation - get totals
// Module: PurchasesReconciliationTotals.java
// Author: C.K.Harvey
// Copyright (c) 2000-08 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.sql.*;

public class PurchasesReconciliationTotals extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  DefinitionTables definitionTables = new DefinitionTables();
  PurchasesReconciliationAnalysis purchasesReconciliationAnalysis = new PurchasesReconciliationAnalysis();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="";

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

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, bytesOut);
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

      System.out.println("6033b: " + e);
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "PurchasesReconciliationTotals", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6033, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut)
                    throws Exception
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
    Statement stmt2 = null;
    ResultSet rs2   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 6028, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "PurchasesReconciliationTotals", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6033, bytesOut[0], 0, "ACC:");
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "PurchasesReconciliationTotals", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6033, bytesOut[0], 0, "SID:");
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    String yyyy = generalUtils.getFromDefnFile(unm, "accounts.dfn", workingDir, "");
    int year = generalUtils.strToInt(yyyy);

    String[] financialYearStartMonth = new String[1];
    String[] financialYearEndMonth   = new String[1];

    definitionTables.getAppConfigFinancialYearMonths(con, stmt, rs, dnm, financialYearStartMonth, financialYearEndMonth);

    int startMonth = generalUtils.detMonthNumFromMonthName(financialYearStartMonth[0]);
    int endMonth   = generalUtils.detMonthNumFromMonthName(financialYearEndMonth[0]);

    String dateFrom = "01." + startMonth + "." + year;

    String dateTo;
    if(startMonth > endMonth)
      dateTo = generalUtils.numOfDaysInMonth((short)endMonth, (short)year) + "." + endMonth + "." + (year + 1);
    else dateTo = generalUtils.numOfDaysInMonth((short)endMonth, (short)year) + "." + endMonth + "." + year;

    String effectiveDate = definitionTables.getAppConfigEffectiveStartDate(con, stmt, rs, dnm);

    if(generalUtils.encode(effectiveDate, localDefnsDir, defnsDir) > generalUtils.encode(dateFrom, localDefnsDir, defnsDir))
      dateFrom = effectiveDate;
    
    dateFrom = generalUtils.convertDateToSQLFormat(dateFrom);
    dateTo   = generalUtils.convertDateToSQLFormat(dateTo);

    set(con, stmt, stmt2, rs, rs2, out, yyyy, dateFrom, dateTo, unm, dnm, workingDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 6033, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, String year, String dateFrom, String dateTo,
                   String unm, String dnm, String workingDir, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    String tmpTable = unm + "_tmp";

    double[] obPurchasesDRs            = new double[1];  obPurchasesDRs[0]            = 0.0;
    double[] obPurchasesCRs            = new double[1];  obPurchasesCRs[0]            = 0.0; 
    double[] obPurchasesBalance        = new double[1];  obPurchasesBalance[0]        = 0.0;
    
    double[] allPurchasesDRs           = new double[1];  allPurchasesDRs[0]           = 0.0;
    double[] allPurchasesCRs           = new double[1];  allPurchasesCRs[0]           = 0.0;
    double[] allPurchasesBalance       = new double[1];  allPurchasesBalance[0]       = 0.0;
    
    double[] purchasesIntoStockDRs     = new double[1];  purchasesIntoStockDRs[0]     = 0.0;
    double[] purchasesIntoStockCRs     = new double[1];  purchasesIntoStockCRs[0]     = 0.0;
    double[] purchasesIntoStockBalance = new double[1];  purchasesIntoStockBalance[0] = 0.0;
    
    double[] nonStockPurchasesDRs      = new double[1];  nonStockPurchasesDRs[0]      = 0.0;
    double[] nonStockPurchasesCRs      = new double[1];  nonStockPurchasesCRs[0]      = 0.0;
    double[] nonStockPurchasesBalance  = new double[1];  nonStockPurchasesBalance[0]  = 0.0;
    
    double[] salesFromStockDRs         = new double[1];  salesFromStockDRs[0]         = 0.0;
    double[] salesFromStockCRs         = new double[1];  salesFromStockCRs[0]         = 0.0;
    double[] salesFromStockBalance     = new double[1];  salesFromStockBalance[0]     = 0.0;
    
    double[] miscPurchasesDRs          = new double[1];  miscPurchasesDRs[0]          = 0.0;
    double[] miscPurchasesCRs          = new double[1];  miscPurchasesCRs[0]          = 0.0;
    double[] miscPurchasesBalance      = new double[1];  miscPurchasesBalance[0]      = 0.0;
    
    double[] miscStockDRs              = new double[1];  miscStockDRs[0]              = 0.0;
    double[] miscStockCRs              = new double[1];  miscStockCRs[0]              = 0.0;
    double[] miscStockBalance          = new double[1];  miscStockBalance[0]          = 0.0;
    
    double[] obStockDRs                = new double[1];  obStockDRs[0]                = 0.0;
    double[] obStockCRs                = new double[1];  obStockCRs[0]                = 0.0; 
    double[] obStockBalance            = new double[1];  obStockBalance[0]            = 0.0;
        
    double[] allStockDRs               = new double[1];  allStockDRs[0]               = 0.0;
    double[] allStockCRs               = new double[1];  allStockCRs[0]               = 0.0; 
    double[] allStockBalance           = new double[1];  allStockBalance[0]           = 0.0;
        
    double[] closingStockDRs           = new double[1];  closingStockDRs[0]           = 0.0;
    double[] closingStockCRs           = new double[1];  closingStockCRs[0]           = 0.0; 
    double[] closingStockBalance       = new double[1];  closingStockBalance[0]       = 0.0;
    
    double[] closingPurchasesDRs       = new double[1];  closingPurchasesDRs[0]       = 0.0;
    double[] closingPurchasesCRs       = new double[1];  closingPurchasesCRs[0]       = 0.0; 
    double[] closingPurchasesBalance   = new double[1];  closingPurchasesBalance[0]   = 0.0;
    
    directoryUtils.createTmpTable(true, con, stmt, "Type char(1), Code char(20), Reference char(20), Description char(100), Total decimal(19,8), "
                                        + "BaseTotal decimal(19,8), Date date, Currency char(3), StockDRs decimal(19,8), StockCRs decimal(19,8), "
                                        + "NonStockDRs decimal(19,8), NonStockCRs decimal(19,8)", "", tmpTable);
    
    purchasesReconciliationAnalysis.calc(con, stmt, stmt2, rs, rs2, '1', year, dateFrom, dateTo, unm, dnm, workingDir, localDefnsDir, defnsDir, tmpTable);
    
    processTmpTable(con, stmt, rs, tmpTable, obPurchasesDRs, obPurchasesCRs, obPurchasesBalance, allPurchasesDRs, allPurchasesCRs, allPurchasesBalance,
                    purchasesIntoStockDRs, purchasesIntoStockCRs, purchasesIntoStockBalance, nonStockPurchasesDRs, nonStockPurchasesCRs,
                    nonStockPurchasesBalance, salesFromStockDRs, salesFromStockCRs, salesFromStockBalance, miscPurchasesDRs, miscPurchasesCRs,
                    miscPurchasesBalance, closingPurchasesDRs, closingPurchasesCRs, closingPurchasesBalance);
    
    directoryUtils.removeTmpTable(con, stmt, tmpTable); 

    directoryUtils.createTmpTable(true, con, stmt, "Type char(1), Code char(20), Reference char(20), Description char(100), Total decimal(19,8), "
                                        + "BaseTotal decimal(19,8), Date date, Currency char(3), StockDRs decimal(19,8), StockCRs decimal(19,8), "
                                        + "NonStockDRs decimal(19,8), NonStockCRs decimal(19,8)", "", tmpTable);
    
    purchasesReconciliationAnalysis.calc(con, stmt, stmt2, rs, rs2, '2', year, dateFrom, dateTo, unm, dnm, workingDir, localDefnsDir, defnsDir, tmpTable);
    
    processTmpTable2(con, stmt, rs, tmpTable, obStockDRs, obStockCRs, obStockBalance, miscStockDRs, miscStockCRs, miscStockBalance, allStockDRs, allStockCRs,
                     allStockBalance, closingStockDRs, closingStockCRs, closingStockBalance);
    
    directoryUtils.removeTmpTable(con, stmt, tmpTable); 

    String s = obPurchasesDRs[0]        + "\001" + obPurchasesCRs[0]        + "\001" + obPurchasesBalance[0]        + "\001"
             + allPurchasesDRs[0]       + "\001" + allPurchasesCRs[0]       + "\001" + allPurchasesBalance[0]       + "\001"
             + purchasesIntoStockDRs[0] + "\001" + purchasesIntoStockCRs[0] + "\001" + purchasesIntoStockBalance[0] + "\001"
             + nonStockPurchasesDRs[0]  + "\001" + nonStockPurchasesCRs[0]  + "\001" + nonStockPurchasesBalance[0]  + "\001"
             + salesFromStockDRs[0]     + "\001" + salesFromStockCRs[0]     + "\001" + salesFromStockBalance[0]     + "\001"
             + miscPurchasesDRs[0]      + "\001" + miscPurchasesCRs[0]      + "\001" + miscPurchasesBalance[0]      + "\001"
             + obStockDRs[0]            + "\001" + obStockCRs[0]            + "\001" + obStockBalance[0]            + "\001"
             + miscStockDRs[0]          + "\001" + miscStockCRs[0]          + "\001" + miscStockBalance[0]          + "\001"
             + allStockDRs[0]           + "\001" + allStockCRs[0]           + "\001" + allStockBalance[0]           + "\001"
             + closingStockDRs[0]       + "\001" + closingStockCRs[0]       + "\001" + closingStockBalance[0]       + "\001"
             + closingPurchasesDRs[0]   + "\001" + closingPurchasesCRs[0]   + "\001" + closingPurchasesBalance[0]   + "\001";
    
    out.println(s);
    
    bytesOut[0] += s.length();
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void processTmpTable(Connection con, Statement stmt, ResultSet rs, String tmpTable, double[] obPurchasesDRs, double[] obPurchasesCRs,
                               double[] obPurchasesBalance, double[] allPurchasesDRs, double[] allPurchasesCRs, double[] allPurchasesBalance,
                               double[] purchasesIntoStockDRs, double[] purchasesIntoStockCRs, double[] purchasesIntoStockBalance,
                               double[] nonStockPurchasesDRs, double[] nonStockPurchasesCRs, double[] nonStockPurchasesBalance, double[] salesFromStockDRs,
                               double[] salesFromStockCRs, double[] salesFromStockBalance, double[] miscPurchasesDRs, double[] miscPurchasesCRs,
                               double[] miscPurchasesBalance, double[] closingPurchasesDRs, double[] closingPurchasesCRs, double[] closingPurchasesBalance)
                               throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT * FROM " + tmpTable + " ORDER BY Date, Type"); // ensuring the OB (in JB) come first

    String type, code;
    double stockDRs, stockCRs, nonStockDRs, nonStockCRs;
    
    while(rs.next())
    {    
      type        = rs.getString(1);
      code        = rs.getString(2);
      stockDRs    = generalUtils.doubleFromStr(generalUtils.doubleDPs(rs.getString(9),  '2'));
      stockCRs    = generalUtils.doubleFromStr(generalUtils.doubleDPs(rs.getString(10), '2'));
      nonStockDRs = generalUtils.doubleFromStr(generalUtils.doubleDPs(rs.getString(11), '2'));
      nonStockCRs = generalUtils.doubleFromStr(generalUtils.doubleDPs(rs.getString(12), '2'));

      if(type.charAt(0) == 'V') // voucher
      {
        miscPurchasesDRs[0]     += stockDRs;
        miscPurchasesBalance[0] += stockDRs;
        closingPurchasesDRs[0]     += stockDRs;
        closingPurchasesBalance[0] += stockDRs;
      }
      else
      if(type.charAt(0) == 'R') // rvoucher
      {
        miscPurchasesCRs[0]     += stockCRs;
        miscPurchasesBalance[0] -= stockCRs;
        closingPurchasesCRs[0]     += stockCRs;
        closingPurchasesBalance[0] -= stockCRs;
      }
      else
      if(type.charAt(0) == 'I') // iat
      {
        miscPurchasesCRs[0]     += stockCRs;
        miscPurchasesBalance[0] -= stockCRs;
        closingPurchasesCRs[0]     += stockCRs;
        closingPurchasesBalance[0] -= stockCRs;
      }
      else
      if(type.charAt(0) == 'J') // iat
      {
        miscPurchasesDRs[0]     += stockDRs;
        miscPurchasesBalance[0] += stockDRs;
        closingPurchasesDRs[0]     += stockDRs;
        closingPurchasesBalance[0] += stockDRs;
      }
      else
      if(type.charAt(0) == 'B') // jb
      {
        if(code.equals("Opening Balances"))
        {
          obPurchasesCRs[0]     += stockCRs;
          obPurchasesBalance[0] -= stockCRs;
          closingPurchasesCRs[0]     += stockCRs;
          closingPurchasesBalance[0] -= stockCRs;
        }
        else
        {
          miscPurchasesCRs[0]     += stockCRs;
          miscPurchasesBalance[0] -= stockCRs;
          closingPurchasesCRs[0]     += stockCRs;
          closingPurchasesBalance[0] -= stockCRs;
        }
      }
      else
      if(type.charAt(0) == 'C') // jb
      {
        if(code.equals("Opening Balances"))
        {
          obPurchasesDRs[0]     += stockDRs;
          obPurchasesBalance[0] += stockDRs;
          closingPurchasesDRs[0]     += stockDRs;
          closingPurchasesBalance[0] += stockDRs;
        }
        else
        {
          miscPurchasesDRs[0]     += stockDRs;
          miscPurchasesBalance[0] += stockDRs;
          closingPurchasesDRs[0]     += stockDRs;
          closingPurchasesBalance[0] += stockDRs;
        }
      }
      else
      if(type.charAt(0) == 'N') // pinvoice
      {
        allPurchasesDRs[0]           += (stockDRs + nonStockDRs);
        allPurchasesBalance[0]       += (stockDRs + nonStockDRs);
        nonStockPurchasesDRs[0]      += nonStockDRs;
        nonStockPurchasesBalance[0]  += nonStockDRs;
        purchasesIntoStockDRs[0]     += stockDRs;
        purchasesIntoStockBalance[0] += stockDRs;
        closingPurchasesDRs[0]           += nonStockDRs;
        closingPurchasesBalance[0]       += nonStockDRs;
      }
      else
      if(type.charAt(0) == 'Q') // pcn
      {
        allPurchasesCRs[0]           += (stockCRs + nonStockCRs);
        allPurchasesBalance[0]       -= (stockCRs + nonStockCRs);
        nonStockPurchasesCRs[0]      += nonStockCRs;
        nonStockPurchasesBalance[0]  -= nonStockCRs;
        purchasesIntoStockCRs[0]     += stockCRs;
        purchasesIntoStockBalance[0] -= stockCRs;
        closingPurchasesCRs[0]           += nonStockCRs;
        closingPurchasesBalance[0]       -= nonStockCRs;
      }
      else
      if(type.charAt(0) == 'E') // pdn
      {
        allPurchasesDRs[0]           += (stockDRs + nonStockDRs);
        allPurchasesBalance[0]       += (stockDRs + nonStockDRs);
        nonStockPurchasesDRs[0]      += nonStockDRs;
        nonStockPurchasesBalance[0]  += nonStockDRs;
        purchasesIntoStockDRs[0]     += stockDRs;
        purchasesIntoStockBalance[0] += stockDRs;
        closingPurchasesDRs[0]           += nonStockDRs;
        closingPurchasesBalance[0]       += nonStockDRs;
      }
      else
      if(type.charAt(0) == 'Y') // invoice
      {
        salesFromStockCRs[0]     += stockCRs;
        salesFromStockBalance[0] += stockCRs;
        closingPurchasesDRs[0]     += stockDRs;
        closingPurchasesBalance[0] += stockDRs;
      }
      else
      if(type.charAt(0) == 'W') // cn
      {
        salesFromStockDRs[0]     += stockDRs;
        salesFromStockBalance[0] -= stockDRs;
        closingPurchasesCRs[0]     += stockCRs;
        closingPurchasesBalance[0] -= stockCRs;
      }
      else
      if(type.charAt(0) == 'X') // dn
      {
        salesFromStockDRs[0]     += stockDRs;
        salesFromStockBalance[0] -= stockDRs;
        closingPurchasesDRs[0]     += stockDRs;
        closingPurchasesBalance[0] -= stockDRs;
      }
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void processTmpTable2(Connection con, Statement stmt, ResultSet rs, String tmpTable, double[] obStockDRs, double[] obStockCRs,
                                double[] obStockBalance, double[] miscStockDRs, double[] miscStockCRs, double[] miscStockBalance, double[] allStockDRs,
                                double[] allStockCRs, double[] allStockBalance, double[] closingStockDRs, double[] closingStockCRs,
                                double[] closingStockBalance) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT * FROM " + tmpTable + " ORDER BY Date, Type"); // ensuring the OB (in JB) come first

    String type, code;
    double stockDRs, stockCRs;
    
    while(rs.next())
    {    
      type        = rs.getString(1);
      code        = rs.getString(2);
      stockDRs    = generalUtils.doubleFromStr(generalUtils.doubleDPs(rs.getString(9),  '2'));
      stockCRs    = generalUtils.doubleFromStr(generalUtils.doubleDPs(rs.getString(10), '2'));

      if(type.charAt(0) == 'V') // voucher
      {
        miscStockDRs[0]     += stockDRs;
        miscStockBalance[0] += stockDRs;
        closingStockDRs[0]     += stockDRs;
        closingStockBalance[0] += stockDRs;
      }
      else
      if(type.charAt(0) == 'R') // rvoucher
      {
        miscStockCRs[0]     += stockCRs;
        miscStockBalance[0] -= stockCRs;
        closingStockCRs[0]     += stockCRs;
        closingStockBalance[0] -= stockCRs;
      }
      else
      if(type.charAt(0) == 'I') // iat
      {
        miscStockCRs[0]     += stockCRs;
        miscStockBalance[0] -= stockCRs;
        closingStockCRs[0]     += stockCRs;
        closingStockBalance[0] -= stockCRs;
      }
      else
      if(type.charAt(0) == 'J') // iat
      {
        miscStockDRs[0]     += stockDRs;
        miscStockBalance[0] += stockDRs;
        closingStockDRs[0]     += stockDRs;
        closingStockBalance[0] += stockDRs;
      }
      else
      if(type.charAt(0) == 'B') // jb
      {
        if(code.equals("Opening Balances"))
        {
          obStockCRs[0]     += stockCRs;
          obStockBalance[0] -= stockCRs;
          closingStockCRs[0]     += stockCRs;
          closingStockBalance[0] -= stockCRs;
        }
        else
        {
          miscStockCRs[0]     += stockCRs;
          miscStockBalance[0] -= stockCRs;
          closingStockCRs[0]     += stockCRs;
          closingStockBalance[0] -= stockCRs;
        }
      }
      else
      if(type.charAt(0) == 'C') // jb
      {
        if(code.equals("Opening Balances"))
        {
          obStockDRs[0]     += stockDRs;
          obStockBalance[0] += stockDRs;
          closingStockDRs[0]     += stockDRs;
          closingStockBalance[0] += stockDRs;
        }
        else
        {
          miscStockDRs[0]     += stockDRs;
          miscStockBalance[0] += stockDRs;
          closingStockDRs[0]     += stockDRs;
          closingStockBalance[0] += stockDRs;
        }
      }
      else
      if(type.charAt(0) == 'N') // pinvoice
      {
        allStockDRs[0]         += stockDRs;
        allStockBalance[0]     += stockDRs;
        closingStockDRs[0]     += stockDRs;
        closingStockBalance[0] += stockDRs;
      }
      else
      if(type.charAt(0) == 'Q') // pcn
      {
        allStockCRs[0]         += stockCRs;
        allStockBalance[0]     -= stockCRs;
        closingStockCRs[0]     += stockCRs;
        closingStockBalance[0] -= stockCRs;
      }
      else
      if(type.charAt(0) == 'E') // pdn
      {
        allStockDRs[0]         += stockDRs;
        allStockBalance[0]     += stockDRs;
        closingStockDRs[0]     += stockDRs;
        closingStockBalance[0] += stockDRs;
      }
      else
      if(type.charAt(0) == 'Y') // invoice
      {
        allStockCRs[0]         += stockCRs;
        allStockBalance[0]     -= stockCRs;
        closingStockCRs[0]     += stockCRs;
        closingStockBalance[0] -= stockCRs;
      }
      else
      if(type.charAt(0) == 'W') // cn
      {
        allStockDRs[0]         += stockDRs;
        allStockBalance[0]     += stockDRs;
        closingStockDRs[0]     += stockDRs;
        closingStockBalance[0] += stockDRs;
      }
      else
      if(type.charAt(0) == 'X') // dn
      {
        allStockCRs[0]         += stockCRs;
        allStockBalance[0]     -= stockCRs;
        closingStockCRs[0]     += stockCRs;
        closingStockBalance[0] -= stockCRs;
      }
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }
  
}
