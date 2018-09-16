// =============================================================================================================================================================
// System: ZaraStar: Analytics - SIT Analysis
// Module: AnalyticsSITAnalysis.java
// Author: C.K.Harvey
// Copyright (c) 2001-08 Christopher Harvey. All Rights Reserved.
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

public class AnalyticsSITAnalysis extends HttpServlet
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
 
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "AnalyticsSITAnalysisInput", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6037, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String p1, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                    int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String workingDir       = directoryUtils.getUserDir('W', dnm, unm);
    
    String userName = directoryUtils.getMySQLUserName();
    String passWord = directoryUtils.getMySQLPassWord();
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + userName + "&password=" + passWord);

    Statement stmt = null, stmt2 = null, stmt3 = null, stmt4 = null;
    ResultSet rs   = null, rs2   = null, rs3   = null, rs4   = null;
    
    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 6037, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "AnalyticsSITAnalysis", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6037, bytesOut[0], 0, "ACC:" + p1);
      
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "AnalyticsSITAnalysis", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6037, bytesOut[0], 0, "SID:" + p1);
      
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

    set(con, stmt, stmt2, stmt3, stmt4, rs, rs2, rs3, rs4, out, req, dateTo, dateStartAccountingYear, unm, sid, uty, men, den, dnm, bnm, workingDir,
        localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 6037, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    
    if(con != null) con.close();
    if(out != null) out.flush();
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, Statement stmt2, Statement stmt3, Statement stmt4, ResultSet rs, ResultSet rs2,
                   ResultSet rs3, ResultSet rs4, PrintWriter out, HttpServletRequest req, String dateTo, String dateStartAccountingYear, String unm, String sid,
                   String uty, String men, String den, String dnm, String bnm, String workingDir, String localDefnsDir, String defnsDir, int[] bytesOut)
                   throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>SIT Analysis</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 5006, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewPO(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/PurchaseOrderPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm="
                           + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    scoutln(out, bytesOut, "function details(code,item){var p1=sanitise(code);var p2=sanitise(item);");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/AnalyticsSITAnalysisDetails?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm="
                         + dnm + "&bnm=" + bnm + "&p3=" + dateTo + "&p2=\"+p2+\"&p1=\"+p1;}");

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
  
    RandomAccessFile fhData  = generalUtils.create(workingDir + "6037.data");
    RandomAccessFile fhState = generalUtils.create(workingDir + "6037.state");
    generalUtils.fileClose(fhState);
    String stateFileName = workingDir + "6037.state";
    keepChecking(out, "6037", unm, sid, uty, men, den, dnm, bnm, bytesOut);

    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\""
                         + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    int[] hmenuCount = new int[1];

    drawingUtils.outputPageFrame(con, stmt, rs, out, req, "6037", "", "AnalyticsSITAnalysis", unm, sid, uty, men, den, dnm, bnm, " chkTimer(); ", localDefnsDir, defnsDir, hmenuCount, bytesOut);

    drawingUtils.drawTitle(out, false, false, "AnalyticsSITAnalysis", "", "Stock-in-Transit Analysis", "6037", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
   
    scoutln(out, bytesOut, "<span id='stuff'></span>");
    if(out != null) out.flush();
    out.close();

    scoutln(fhData, "<table id='page' width=100% border=0>");

    scoutln(fhData, "<tr><td>&nbsp;</td></tr>");

    String baseCurrency = accountsUtils.getBaseCurrency(con, stmt, rs, dnm, localDefnsDir, defnsDir);
    
    headings(fhData, baseCurrency);

    char dpOnQuantities = miscDefinitions.dpOnQuantities(con, stmt, rs, "5");

    String[] cssFormat = new String[1];  cssFormat[0] = "line1";
    int[] oCount = new int[1];  oCount[0] = 1;

    double[] type1TotalValue = new double[1];  type1TotalValue[0] = 0.0;
    double[] type2TotalValue = new double[1];  type2TotalValue[0] = 0.0;
            
    boolean atLeastOneSetAsAllReceived = forAllPurchaseOrders(con, stmt, stmt2, stmt3, stmt4, rs, rs2, rs3, rs4, fhData, stateFileName, dateTo, dateStartAccountingYear, dpOnQuantities, cssFormat, type1TotalValue, type2TotalValue, dnm);
    
    headings(fhData, baseCurrency);

    scoutln(fhData, "<tr><td>&nbsp;</td></tr>");
    scoutln(fhData, "<tr><td>&nbsp;</td></tr>");
    scoutln(fhData, "<tr><td nowrap colspan=6><p>Type-1: Goods Received, no Invoice (at WAC): " + baseCurrency + " "
                  + generalUtils.formatNumeric(type1TotalValue[0], '2') + "</td></tr>");
    scoutln(fhData, "<tr><td nowrap colspan=6><p>Type-2: Goods not Received, Invoiced (Purchase Price): " + baseCurrency + " "
                  + generalUtils.formatNumeric(type2TotalValue[0], '2') + "</td></tr>");

    if(atLeastOneSetAsAllReceived)
    {
      scoutln(fhData, "<tr><td>&nbsp;</td></tr>");
      scoutln(fhData, "<tr><td nowrap colspan=4><p><font color=red size=2><sup>*</sup> All Received</font></td></tr>");
    }
    
    scoutln(fhData, "<tr><td>&nbsp;</td></tr>");

    scoutln(fhData, "</table>");

    scoutln(fhData, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
    
    generalUtils.fileClose(fhData);
    directoryUtils.updateState(stateFileName, "100");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void headings(RandomAccessFile fhData, String baseCurrency) throws Exception
  {      
    scoutln(fhData, "<tr id=\"pageColumn\">");
    scoutln(fhData, "<td><p>PO Code</td>");
    scoutln(fhData, "<td><p>PO Date</td>");
    scoutln(fhData, "<td><p>Supplier Code</td>");
    scoutln(fhData, "<td><p>Supplier Name</td>");
    scoutln(fhData, "<td><p>ItemCode</td>");
    scoutln(fhData, "<td align=center><p>Purchase Order Quantity</td>");
    scoutln(fhData, "<td align=center><p>Goods Received Note<br>" + baseCurrency + "</td>");
    scoutln(fhData, "<td align=center><p>Purchase Invoice Quantity<br>" + baseCurrency + "</td>");
    scoutln(fhData, "<td align=center><p>Type-1<br>" + baseCurrency + "</td>");
    scoutln(fhData, "<td align=center><p>Type-2<br>" + baseCurrency + "</td>");
    scoutln(fhData, "<td></td></tr>");
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(RandomAccessFile fh, String str) throws Exception
  {      
    fh.writeBytes(str + "\n");
  }
  private void scout(RandomAccessFile fh, String str) throws Exception
  {      
    fh.writeBytes(str);
  }
    
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean forAllPurchaseOrders(Connection con, Statement stmt, Statement stmt2, Statement stmt3, Statement stmt4, ResultSet rs, ResultSet rs2,
                                       ResultSet rs3, ResultSet rs4, RandomAccessFile fhData, String stateFileName, String dateTo,
                                       String dateStartAccountingYear, char dpOnQuantities, String[] cssFormat, double[] type1TotalValue,
                                       double[] type2TotalValue, String dnm) throws Exception
  {
    boolean atLeastOneSetAsAllReceived = false;
      
    stmt = con.createStatement();
    rs = stmt.executeQuery("SELECT COUNT(t2.POCode) FROM pol AS t2 INNER JOIN po AS t1 ON t2.POCode = t1.POCode "
                         + "WHERE t1.Status != 'C' AND t1.Date <= {d '" + dateTo + "'}");
    double numPOs = 1.0;
    if(rs.next())
      numPOs = rs.getInt(1);
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
      
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.POCode, t2.ItemCode, t2.Quantity FROM pol AS t2 INNER JOIN po AS t1 ON t2.POCode = t1.POCode "
                         + "WHERE t1.Status != 'C' AND t1.Date <= {d '" + dateTo + "'} ORDER BY t1.POCode");
      
    int poItemCodesCount = 0;
    String[][] poItemCodes = new String[1][100];
    double[][] poItemQtys  = new double[1][100];
    int[] poItemCodesSize  = new int[1];  poItemCodesSize[0] = 100;

    String[][] grItemCodes = new String[1][100];
    double[][] grItemQtys  = new double[1][100];
    int[] grItemCodesSize  = new int[1];  grItemCodesSize[0] = 100;

    String[][] iItemCodes      = new String[1][100];
    double[][] iItemQtys       = new double[1][100];
    double[][] iItemUnitPrices = new double[1][100];
    int[] iItemCodesSize       = new int[1];  iItemCodesSize[0] = 100;

    String[][] cnItemCodes = new String[1][100];
    double[][] cnItemQtys  = new double[1][100];
    int[] cnItemCodesSize  = new int[1];  cnItemCodesSize[0] = 100;
    
    String[][] stockItemCodes = new String[1][100];
    double[][] stockItemQtys  = new double[1][100];
    int[] stockItemCodesSize  = new int[1];  stockItemCodesSize[0] = 100;
    
    String[][] saItemCodes = new String[1][100];
    double[][] saItemQtys  = new double[1][100];
    int[] saItemCodesSize  = new int[1];  saItemCodesSize[0] = 100;
    
    String[] companyCode = new String[1];
    String[] companyName = new String[1];
    String[] poDate      = new String[1];
    String[] allReceived = new String[1];

    String poCode, lastPOCode = "";
    boolean first = true;
    int count = 0;
    
    while(rs.next())
    {
      directoryUtils.updateState(stateFileName, "" + generalUtils.strDPs('0', ("" + generalUtils.doubleToStr((count++ / numPOs) * 100))));

      poCode = rs.getString(1);
      
      if(! lastPOCode.equals(poCode))
      {
        if(! first)
        {
          poDetails(con, stmt2, rs2, lastPOCode, companyCode, companyName, poDate, allReceived);
          
          if(allReceived[0].equals("Y"))
            atLeastOneSetAsAllReceived = true;
        
          processPO(con, stmt2, stmt3, stmt4, rs2, rs3, rs4, fhData, false, lastPOCode, dateStartAccountingYear, dateTo, poItemCodes[0], poItemQtys[0],
                    poItemCodesCount, grItemCodes, grItemCodesSize, grItemQtys, iItemCodes, iItemCodesSize, iItemQtys, iItemUnitPrices, cnItemCodes,
                    cnItemCodesSize, cnItemQtys, saItemCodes, saItemCodesSize, saItemQtys, stockItemCodes, stockItemCodesSize, stockItemQtys, dpOnQuantities,
                    cssFormat, companyCode[0], companyName[0], poDate[0], allReceived[0], type1TotalValue, type2TotalValue, dnm);
        
          clearList(poItemCodes, poItemQtys, poItemCodesCount);
          poItemCodesCount = 0;
        }
        else first = false;

        lastPOCode = poCode;
      }
  
      poItemCodesCount = addToList(con, stmt3, rs3, rs.getString(2), rs.getString(3), poItemCodes, poItemCodesSize, poItemQtys, poItemCodesCount);
    }
      
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return atLeastOneSetAsAllReceived;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void processPO(Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, ResultSet rs3, RandomAccessFile fhData,
                         boolean onlyOneSO, String poCode, String dateStartAccountingYear, String dateTo, String[] poItemCodes, double[] poItemQtys,
                         int poItemCodesCount, String[][] grItemCodes, int[] grItemCodesSize, double[][] grItemQtys, String[][] iItemCodes,
                         int[] iItemCodesSize, double[][] iItemQtys, double[][] iItemUnitPrices, String[][] cnItemCodes, int[]cnItemCodesSize,
                         double[][] cnItemQtys, String[][] saItemCodes, int[] saItemCodesSize, double[][] saItemQtys, String[][] stockItemCodes,
                         int[] stockItemCodesSize, double[][] stockItemQtys, char dpOnQuantities, String[] cssFormat, String companyCode, String companyName,
                         String poDate, String allReceived, double[] type1TotalValue, double[] type2TotalValue, String dnm) throws Exception
  {
    int[] cnItemCodesCount = new int[1];  cnItemCodesCount[0] = 0; 
    
    int grItemCodesCount = scanGRNs(con, stmt, stmt2, rs, rs2, poCode, dateTo, grItemCodes, grItemCodesSize, grItemQtys);
    int iItemCodesCount  = scanInvoices(con, stmt, stmt2, stmt3, rs, rs2, rs3, poCode, dateTo, dpOnQuantities, iItemCodes, iItemCodesSize, iItemQtys,
                                        iItemUnitPrices, cnItemCodes, cnItemCodesSize, cnItemQtys, cnItemCodesCount);
    int saItemCodesCount = scanForStockA(con, stmt, stmt2, rs, rs2, poCode, dateTo, dpOnQuantities, saItemCodes, saItemCodesSize, saItemQtys);
    
    int stockItemCodesCount = getItemValuations(con, stmt, rs, dateStartAccountingYear, dateTo, poItemCodes, poItemCodesCount, stockItemCodes, stockItemCodesSize, stockItemQtys, dnm);

    String itemCode;
    boolean first = true, firstCSS = true;
    double grQty, iQty, cnQty, saQty, itemValue, type1, type2;
    
    int x = 0;
    for(x=0;x<poItemCodesCount;++x)
    {
      itemCode = poItemCodes[x];
      
      itemValue = itemValueOnList(itemCode, stockItemCodes[0], stockItemQtys[0], stockItemCodesCount);

      grQty = qtyOnList(itemCode, grItemCodes[0], grItemQtys[0], grItemCodesCount);

      iQty  = qtyOnList(itemCode, iItemCodes[0],  iItemQtys[0],  iItemCodesCount);

      saQty = qtyOnList(itemCode, saItemCodes[0], saItemQtys[0], saItemCodesCount);

      cnQty = qtyOnList(itemCode, cnItemCodes[0], cnItemQtys[0], cnItemCodesCount[0]);

      grQty -= saQty;

      iQty  -= cnQty;

      if(grQty == 0.0 && iQty == 0.0)
        ; // ignore order outstanding
      else
      if(grQty == iQty)
        ; // complete
      else
      {
        if(grQty > iQty)
        {
          type1 = (grQty - iQty); // type1: received, no invoice
          type2 = 0.0;
        }
        else
        {
          type2 = (iQty - grQty); // type2: not received, invoiced
          type1 = 0.0;
        }

        type1TotalValue[0] += (generalUtils.doubleDPs(type1, '2') * itemValue);
        
        if(iQty != 0)
          type2TotalValue[0] += (generalUtils.doubleDPs(type2, '2') * (itemValueOnList(itemCode, iItemCodes[0], iItemUnitPrices[0], iItemCodesCount)) / iQty);
                
        if(firstCSS)
        { 
          if(cssFormat[0].equals("line1")) cssFormat[0] = "line2"; else cssFormat[0] = "line1";
          firstCSS = false;
        }
        
        if(first)
        { 
          scout(fhData, "<tr id='" + cssFormat[0] + "'><td nowrap><p><a href=\"javascript:viewPO('" + poCode + "')\">" + poCode + "</a>");          
          if(allReceived.equals("Y"))
            scout(fhData, "<font color=red size=2><sup>*</sup></font>");
          scoutln(fhData, "</td>");
              
          scoutln(fhData, "<td><p>" + generalUtils.convertFromYYYYMMDD(poDate) + "</td>");
          scoutln(fhData, "<td><p>" + companyCode + "</td>");
          scoutln(fhData, "<td><p>" + companyName + "</td>");
          first = false;
        }
        else scoutln(fhData, "<tr id='" + cssFormat[0] + "'><td></td><td></td><td></td><td></td>");
            
        scoutln(fhData, "<td><p>" + poItemCodes[x] + "</td>");
        scoutln(fhData, "<td align=center><p>" + generalUtils.formatNumeric(poItemQtys[x], dpOnQuantities) + "</td>");

        scout(fhData, "<td align=center><p>" + generalUtils.formatNumeric(grQty, dpOnQuantities) + "<br>");
        if((grQty * itemValue) != 0.0)
          scout(fhData, generalUtils.formatNumeric((grQty * itemValue), '2'));
        else scout(fhData, "&nbsp;");
        scoutln(fhData, "</td>");

        scout(fhData, "<td align=center><p>" + generalUtils.formatNumeric(iQty, dpOnQuantities) + "<br>");
        if((iQty * itemValue) != 0.0)
          scout(fhData, generalUtils.formatNumeric((iQty * itemValue), '2'));
        else scout(fhData, "&nbsp;");
        scoutln(fhData, "</td>");
        
        scout(fhData, "<td align=center><p>" + generalUtils.formatNumeric(type1, dpOnQuantities) + "<br>");
        if((type1 * itemValue) != 0.0)
          scout(fhData, generalUtils.formatNumeric((type1 * itemValue), '2'));
        else scout(fhData, "&nbsp;");
        scoutln(fhData, "</td>");
        
        scout(fhData, "<td align=center><p>" + generalUtils.formatNumeric(type2, dpOnQuantities) + "<br>");
        if((type2 * itemValue) != 0.0)
          scout(fhData, generalUtils.formatNumeric((type2 * itemValue), '2'));
        else scout(fhData, "&nbsp;");
        scoutln(fhData, "</td>");
        
        scoutln(fhData, "<td><p><a href=\"javascript:details('" + poCode + "','" + itemCode + "')\">Details</a></td></tr>");
      }      
    }

    if(firstCSS && onlyOneSO)
      scoutln(fhData, "<tr><td colspan=4><br><br><br><br><p><b>No Stock-in-Transit Found</td></tr>");
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private double qtyOnList(String itemCode, String[] itemCodes, double[] itemQtys, int itemCodesCount) throws Exception
  {
    for(int x=0;x<itemCodesCount;++x)
    {
      if(itemCode.equals(itemCodes[x]))
        return itemQtys[x];
    }
    
    return 0;
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private int scanGRNs(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String poCode, String dateTo, String[][] grItemCodes,
                       int[] grItemCodesSize, double[][] grItemQtys) throws Exception
  {
    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT t2.ItemCode, t2.Quantity FROM grl AS t2 INNER JOIN gr AS t1 ON t2.GRCode = t1.GRCode "
                         + "WHERE t1.Status != 'C' AND t2.POCode = '" + poCode + "' AND t1.Date <= {d '" + dateTo + "'} AND t1.StockProcessed = 'Y'");
      
    int itemCodesCount = 0;
    
    while(rs.next())
      itemCodesCount = addToList(con, stmt2, rs2, rs.getString(1), rs.getString(2), grItemCodes, grItemCodesSize, grItemQtys, itemCodesCount);
  
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return itemCodesCount;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private int scanInvoices(Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, ResultSet rs3, String poCode,
                           String dateTo, char dpOnQuantities, String[][] iItemCodes, int[] iItemCodesSize, double[][] iItemQtys, double[][] iItemUnitPrices,
                           String[][] cnItemCodes, int[] cnItemCodesSize, double[][] cnItemQtys, int[] cnItemCodesCount) throws Exception
  {
    byte[] b = new byte[30];
    byte[] invoiceCodes    = new byte[1000]; invoiceCodes[0]    = '\000';
    int[]  invoiceCodesLen = new int[1];     invoiceCodesLen[0] = 1000;
    
    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT t2.ItemCode, t2.Quantity, t2.InvoiceCode, t2.UnitPrice FROM pinvoicel AS t2 INNER JOIN pinvoice AS t1 ON t2.InvoiceCode = t1.InvoiceCode "
                         + "WHERE t1.Status != 'C' AND t2.PORefNum = '" + poCode + "' AND t1.Date <= {d '" + dateTo + "'}");
      
    int itemCodesCount = 0;
    double qtyCount;
    String itemCode;
    
    while(rs.next())
    {
      itemCode = rs.getString(1);
      qtyCount = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(2)), dpOnQuantities);

      itemCodesCount = addToList2(con, stmt2, rs2, itemCode, generalUtils.doubleToStr(qtyCount), rs.getString(4), iItemCodes, iItemCodesSize, iItemQtys,
                                  iItemUnitPrices, itemCodesCount);

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

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private int addToList(Connection con, Statement stmt, ResultSet rs, String itemCode, String qty, String[][] itemCodes, int[] itemCodesSize,
                        double[][] itemQtys, int itemCodesCount) throws Exception
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

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private int addToList2(Connection con, Statement stmt, ResultSet rs, String itemCode, String qty, String unitPrice, String[][] itemCodes,
                         int[] itemCodesSize, double[][] itemQtys, double[][] itemUnitPrices, int itemCodesCount) throws Exception
  {
    if(! inventory.existsItemRecGivenCode(con, stmt, rs, itemCode))
      return itemCodesCount;

    int x = 0;
    while(x < itemCodesCount)
    {
      if(itemCodes[0][x].equals(itemCode))
      {
        itemQtys[0][x]       += generalUtils.doubleFromStr(qty);
        
        // new unitPrice is old unitPrice (unitPrice * qty) + this one        
        itemUnitPrices[0][x] += (generalUtils.doubleFromStr(unitPrice) * itemQtys[0][x]);
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

      double[] tmp3 = new double[itemCodesSize[0]];
      for(x=0;x<itemCodesSize[0];++x)
        tmp3[x] = itemUnitPrices[0][x];
      itemUnitPrices[0] = new double[itemCodesSize[0] + 20];
      for(x=0;x<itemCodesSize[0];++x)
        itemUnitPrices[0][x] = tmp3[x];

      itemCodesSize[0] += 20;
    }

    itemCodes[0][itemCodesCount] = itemCode;
        
    itemQtys[0][itemCodesCount] = generalUtils.doubleFromStr(qty);
      
    itemUnitPrices[0][itemCodesCount] = (generalUtils.doubleFromStr(unitPrice) * itemQtys[0][itemCodesCount]);
      
    return ++itemCodesCount;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void clearList(String[][] itemCodes, double[][] itemQtys, int itemCodesCount) throws Exception
  {
    for(int x=0;x<itemCodesCount;++x)
    {
      itemCodes[0][x] = "";
      itemQtys[0][x] = 0.0;
    }
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void poDetails(Connection con, Statement stmt, ResultSet rs, String poCode, String[] companyCode, String[] companyName, String[] poDate,
                         String[] allReceived) throws Exception
  {
    companyCode[0] = companyName[0] = "";
    
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT CompanyCode, CompanyName, Date, AllReceived FROM po WHERE POCode = '" + poCode + "'");
      
    if(rs.next())
    {
      companyCode[0] = rs.getString(1);
      companyName[0] = rs.getString(2);
      poDate[0]      = rs.getString(3);
      allReceived[0] = rs.getString(4);
    }
      
    if(allReceived[0] == null) allReceived[0] = "N";
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scanForCNs(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String invoiceCode, String dateTo, char dpOnQuantities,
                         String[][] cnItemCodes, int[] cnItemCodesSize, double[][] cnItemQtys, int[] cnItemCodesCount) throws Exception
  {
    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT t2.ItemCode, t2.Quantity FROM pcreditl AS t2 INNER JOIN pcredit AS t1 ON t2.PCNCode = t1.PCNCode "
                         + "WHERE t1.Status != 'C' AND t2.InvoiceCode = '" + invoiceCode + "' AND t1.Date <= {d '" + dateTo + "'}");
      
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

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private int scanForStockA(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String poCode, String dateTo, char dpOnQuantities,
                            String[][] saItemCodes, int[] saItemCodesSize, double[][] saItemQtys) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT ItemCode, Quantity FROM stocka WHERE POCode = '" + poCode
                         + "' AND StoreFrom != 'None' AND StoreTo = 'None' AND Date <= {d '" + dateTo + "'} AND Status != 'C'");

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

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private int getItemValuations(Connection con, Statement stmt, ResultSet rs, String dateStartAccountingYear, String dateTo, String[] poItemCodes,
                                int poItemCodesCount, String[][] stockItemCodes, int[] stockItemCodesSize, double[][] stockItemQtys, String dnm) throws Exception
  {
    String itemCode;
    double baseAmount;
    int stockItemCodesCount = 0;
    
    for(int x=0;x<poItemCodesCount;++x)
    {
      itemCode = poItemCodes[x];
      
      baseAmount = inventory.getWAC(con, stmt, rs, itemCode, dnm, dateStartAccountingYear, dateTo);

      stockItemCodesCount = addToList(con, stmt, rs, itemCode, generalUtils.doubleToStr(baseAmount), stockItemCodes, stockItemCodesSize, stockItemQtys,
                                      stockItemCodesCount);
    }
    
    return stockItemCodesCount;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------------------------
  private double itemValueOnList(String itemCode, String[] itemCodes, double[] itemQtys, int itemCodesCount) throws Exception
  {
    for(int x=0;x<itemCodesCount;++x)
    {
      if(itemCode.equals(itemCodes[x]))
        return itemQtys[x];
    }
    
    return 0;
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void keepChecking(PrintWriter out, String servlet, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut)
                            throws Exception
  {
    scoutln(out, bytesOut, "function chkTimer(){chkTimerID=self.setTimeout('chk()',4000);}");
      
    scoutln(out, bytesOut, "var chkreq2;");
    scoutln(out, bytesOut, "function initRequest2(url)");
    scoutln(out, bytesOut, "{if(window.XMLHttpRequest){chkreq2=new XMLHttpRequest();}else");
    scoutln(out, bytesOut, "if(window.ActiveXObject){chkreq2=new ActiveXObject(\"Microsoft.XMLHTTP\");}}");

    scoutln(out, bytesOut, "function chk(){");
    scoutln(out, bytesOut, "var url=\"http://" + men + "/central/servlet/FaxStatusDataFromReportTemp?p1=" + servlet + "&unm=\" + escape('" + unm
                         + "') + \"&sid=\" + escape('" + sid + "') + \"&uty=\" + escape('" + uty + "') + \"&men=\" + escape('" + men
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
