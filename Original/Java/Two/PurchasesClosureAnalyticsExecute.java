// =============================================================================================================================================================
// System: ZaraStar: Purchases Analytics - Purchases Closure Analysis
// Module: PurchasesClosureAnalyticsExecute.java
// Author: C.K.Harvey
// Copyright (c) 2001-08 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
//
// =============================================================================================================================================================

package org.zarastar.zarastar;

import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;
import java.io.*;

public class PurchasesClosureAnalyticsExecute extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  DrawingUtils drawingUtils = new DrawingUtils();
  MiscDefinitions miscDefinitions = new MiscDefinitions();
  Inventory inventory = new Inventory();
  
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
      directoryUtils.setContentHeaders2(res);
      out = res.getWriter();

      unm = req.getParameter("unm");
      sid = req.getParameter("sid");
      uty = req.getParameter("uty");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");
      p1  = req.getParameter("p1"); // POCode (optional)
      p2  = req.getParameter("p2"); // datefrom
      p3  = req.getParameter("p3"); // dateto
      
      if(p1 == null) p1 = "";
      if(p2 == null) p2 = "";
      if(p3 == null) p3 = "";
      
      doIt(out, req, p1, p2, p3, unm, sid, uty, men, den, dnm, bnm, bytesOut);
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
 
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "SalesAnalytics3", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1203, bytesOut[0], 0, "ERR:" + p1 + ":" + p2 + ":" + p3);
      if(out != null) out.flush(); 
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String p1, String p2, String p3, String unm, String sid, String uty, String men, String den,
                    String dnm, String bnm, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    
    String userName = directoryUtils.getMySQLUserName();
    String passWord = directoryUtils.getMySQLPassWord();
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + userName + "&password=" + passWord);

    Statement stmt = null, stmt2 = null, stmt3 = null, stmt4 = null;
    ResultSet rs   = null, rs2   = null, rs3   = null, rs4   = null;
    
    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 1203, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "SalesAnalytics3a", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1203, bytesOut[0], 0, "ACC:" + p1 + ":" + p2 + ":" + p3);
      
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "SalesAnalytics3a", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1203, bytesOut[0], 0, "SID:" + p1 + ":" + p2 + ":" + p3);
      
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    String dateFrom;
    if(p2.length() == 0)
      dateFrom = "1970-01-01";
    else dateFrom = generalUtils.convertDateToSQLFormat(p2);
    
    String dateTo;
    if(p3.length() == 0)
      dateTo = "2099-12-31";
    else dateTo = generalUtils.convertDateToSQLFormat(p3);
    
    set(con, stmt, stmt2, stmt3, stmt4, rs, rs2, rs3, rs4, out, req, p1, dateFrom, dateTo, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir,
        bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 1203, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1 + ":" + p2 + ":" + p3);
    
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, Statement stmt2, Statement stmt3, Statement stmt4, ResultSet rs, ResultSet rs2, ResultSet rs3,
                   ResultSet rs4, PrintWriter out, HttpServletRequest req, String poCode, String dateFrom, String dateTo, String unm, String sid, String uty,
                   String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Purchases: Closure Analysis</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 5006, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewPO(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/PurchaseOrderPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm="
                           + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    scoutln(out, bytesOut, "function details(code,item){var p1=sanitise(code);var p2=sanitise(item);");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/SalesAnalytics3b?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm="
                         + dnm + "&bnm=" + bnm + "&p2=\"+p2+\"&p1=\"+p1;}");

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
  
    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\""
                         + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    int[] hmenuCount = new int[1];

    drawingUtils.outputPageFrame(con, stmt, rs, out, req, "1203", "", "SalesAnalytics3", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount, bytesOut);

    drawingUtils.drawTitle(out, false, false, "SalesAnalytics3", "", "Purchases: Closure Analysis", "1203", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<table id=\"page\" width=100% border=0>");

    scoutln(out, bytesOut, "<tr><td></td></tr><tr id=\"pageColumn\">");

    scoutln(out, bytesOut, "<td><p>PO Code</td><td></td>");
    scoutln(out, bytesOut, "<td><p>PO Date</td>");
    scoutln(out, bytesOut, "<td><p>Supplier Code</td>");
    scoutln(out, bytesOut, "<td><p>Supplier Name</td>");
    scoutln(out, bytesOut, "<td><p>ItemCode</td>");
    scoutln(out, bytesOut, "<td><p>Purchase Order Quantity</td>");
    scoutln(out, bytesOut, "<td><p>GRN Quantity</td>");
    scoutln(out, bytesOut, "<td><p>Adjustment Quantity</td>");
    scoutln(out, bytesOut, "<td><p>Invoice Quantity</td>");
    scoutln(out, bytesOut, "<td><p>Credit Note Quantity</td><td></td></tr>");

    char dpOnQuantities = miscDefinitions.dpOnQuantities(con, stmt, rs, "5");

    String[] cssFormat = new String[1];  cssFormat[0] = "line1";
    int[] maybe  = new int[1];  maybe[0]  = 0;

    if(poCode.length() == 0)
      forAllPurchaseOrders(con, stmt, stmt2, stmt3, stmt4, rs, rs2, rs3, rs4, out, dateFrom, dateTo, dpOnQuantities, cssFormat, maybe, bytesOut);
    else forAPurchaseOrder(con, stmt, stmt2, stmt3, rs, rs2, rs3, out, poCode, dpOnQuantities, cssFormat, maybe, bytesOut);
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=8><p>Possible Errors: Purchase Orders with Issues: " + maybe[0] + "</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void forAllPurchaseOrders(Connection con, Statement stmt, Statement stmt2, Statement stmt3, Statement stmt4, ResultSet rs, ResultSet rs2,
                                    ResultSet rs3, ResultSet rs4, PrintWriter out, String dateFrom, String dateTo, char dpOnQuantities, String[] cssFormat,
                                    int[] maybe, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.POCode, t2.ItemCode, t2.Quantity FROM pol AS t2 INNER JOIN po AS t1 ON t2.POCode = t1.POCode "
                         + "WHERE t1.Status != 'C' AND t1.Date >= {d '" + dateFrom + "'} AND t1.Date <= {d '" + dateTo + "'} ORDER BY t1.POCode");
      
    int poItemCodesCount = 0;
    String[][] poItemCodes = new String[1][100];
    double[][] poItemQtys  = new double[1][100];
    int[] poItemCodesSize  = new int[1];  poItemCodesSize[0] = 100;

    String[][] grItemCodes = new String[1][100];
    double[][] grItemQtys  = new double[1][100];
    int[] grItemCodesSize  = new int[1];  grItemCodesSize[0] = 100;

    String[][] saItemCodes = new String[1][100];
    double[][] saItemQtys  = new double[1][100];
    int[] saItemCodesSize  = new int[1];  saItemCodesSize[0] = 100;

    String[][] iItemCodes = new String[1][100];
    double[][] iItemQtys  = new double[1][100];
    int[] iItemCodesSize  = new int[1];  iItemCodesSize[0] = 100;
    
    String[][] cnItemCodes = new String[1][100];
    double[][] cnItemQtys  = new double[1][100];
    int[] cnItemCodesSize  = new int[1];  cnItemCodesSize[0] = 100;

    String[] companyCode = new String[1];
    String[] companyName = new String[1];
    String[] poDate      = new String[1];
    String[] allReceived = new String[1];

    String poCode, lastPOCode = "";
    boolean first = true;
    
    while(rs.next())
    {
      poCode = rs.getString(1);
      
      if(! lastPOCode.equals(poCode))
      {
        if(! first)
        {
          poDetails(con, stmt2, rs2, lastPOCode, companyCode, companyName, poDate, allReceived);
        
          processPO(con, stmt2, stmt3, stmt4, rs2, rs3, rs4, out, false, lastPOCode, poItemCodes[0], poItemQtys[0], poItemCodesCount, grItemCodes,
                    grItemCodesSize, grItemQtys, saItemCodes, saItemCodesSize, saItemQtys, iItemCodes, iItemCodesSize, iItemQtys, cnItemCodes, cnItemCodesSize,
                    cnItemQtys, dpOnQuantities, cssFormat, companyCode[0], companyName[0], poDate[0], allReceived[0], maybe, bytesOut);
        
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
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void forAPurchaseOrder(Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, ResultSet rs3, PrintWriter out,
                                 String poCode, char dpOnQuantities, String[] cssFormat, int[] maybe, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t1.Date, t1.AllReceived, t1.CompanyCode, t1.CompanyName, t2.ItemCode, t2.Quantity "
                         + "FROM pol AS t2 INNER JOIN po AS t1 ON t2.POCode = t1.POCode WHERE t1.Status != 'C' AND t2.POCode = '" + poCode + "'");
      
    int poItemCodesCount = 0;
    String[][] poItemCodes = new String[1][100];
    double[][] poItemQtys  = new double[1][100];
    int[] poItemCodesSize  = new int[1];  poItemCodesSize[0] = 100;

    String[][] grItemCodes = new String[1][100];
    double[][] grItemQtys  = new double[1][100];
    int[] grItemCodesSize  = new int[1];  grItemCodesSize[0] = 100;

    String[][] saItemCodes = new String[1][100];
    double[][] saItemQtys  = new double[1][100];
    int[] saItemCodesSize  = new int[1];  saItemCodesSize[0] = 100;

    String[][] iItemCodes = new String[1][100];
    double[][] iItemQtys  = new double[1][100];
    int[] iItemCodesSize  = new int[1];  iItemCodesSize[0] = 100;
    
    String[][] cnItemCodes = new String[1][100];
    double[][] cnItemQtys  = new double[1][100];
    int[] cnItemCodesSize  = new int[1];  cnItemCodesSize[0] = 100;

    String companyCode="", companyName="", poDate="", allReceived="", itemCode, qty;

    while(rs.next())
    {
      poDate      = rs.getString(1);
      allReceived = rs.getString(2);
      companyCode = rs.getString(3);
      companyName = rs.getString(4);
      itemCode    = rs.getString(5);
      qty         = rs.getString(6);
    
      poItemCodesCount = addToList(con, stmt3, rs3, itemCode, qty, poItemCodes, poItemCodesSize, poItemQtys, poItemCodesCount);
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    processPO(con, stmt, stmt2, stmt3, rs, rs2, rs3, out, true, poCode, poItemCodes[0], poItemQtys[0], poItemCodesCount, grItemCodes, grItemCodesSize,
              grItemQtys, saItemCodes, saItemCodesSize, saItemQtys, iItemCodes, iItemCodesSize, iItemQtys, cnItemCodes, cnItemCodesSize, cnItemQtys,
              dpOnQuantities, cssFormat, companyCode, companyName, poDate, allReceived, maybe, bytesOut);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void processPO(Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, ResultSet rs3, PrintWriter out, boolean onlyOnePO, String poCode, String[] poItemCodes, double[] poItemQtys, int poItemCodesCount, String[][] grItemCodes,
                         int[] grItemCodesSize, double[][] grItemQtys, String[][] saItemCodes, int[] saItemCodesSize, double[][] saItemQtys,
                         String[][] iItemCodes, int[] iItemCodesSize, double[][] iItemQtys, String[][] cnItemCodes, int[] cnItemCodesSize,
                         double[][] cnItemQtys, char dpOnQuantities, String[] cssFormat, String companyCode, String companyName, String poDate,
                         String allReceived, int[] maybe, int[] bytesOut) throws Exception
  {
    int[] cnItemCodesCount = new int[1];  cnItemCodesCount[0] = 0; 
    int iItemCodesCount  = scanInvoices(con, stmt, stmt2, stmt3, rs, rs2, rs3, poCode, dpOnQuantities, iItemCodes, iItemCodesSize, iItemQtys, cnItemCodes, cnItemCodesSize, cnItemQtys, cnItemCodesCount);

    boolean[] red = new boolean[1]; red[0] = false;
    int grItemCodesCount = scanGRNs(con, stmt, stmt2, rs, rs2, poCode, grItemCodes, grItemCodesSize, grItemQtys, red);
    int saItemCodesCount = scanForStockA(con, stmt, stmt2, rs, rs2, poCode, dpOnQuantities, saItemCodes, saItemCodesSize, saItemQtys, red);
    
    String itemCode;
    boolean first = true, firstCSS = true;
    double grQty, iQty, cnQty, saQty, qty;
    
    int x = 0;
    for(x=0;x<poItemCodesCount;++x)
    {
      itemCode = poItemCodes[x];
      
      grQty = qtyOnList(itemCode, grItemCodes[0], grItemQtys[0], grItemCodesCount);

      iQty = qtyOnList(itemCode, iItemCodes[0], iItemQtys[0], iItemCodesCount);

      cnQty = qtyOnList(itemCode, cnItemCodes[0], cnItemQtys[0], cnItemCodesCount[0]);

      saQty = qtyOnList(itemCode, saItemCodes[0], saItemQtys[0], saItemCodesCount);

      if(saQty >= cnQty)
        qty = cnQty;
      else qty = 0;
          
      if(grQty != poItemQtys[x] || (iQty - (qty + saQty)) != poItemQtys[x])
//      if(grQty != poItemQtys[x] || (iQty - cnQty + saQty) != poItemQtys[x])
      {
        if(firstCSS)
        { 
          if(cssFormat[0].equals("line1")) cssFormat[0] = "line2"; else cssFormat[0] = "line1";
          firstCSS = false;
        }
        
        if(first)
        {
          if(red[0])
            scoutln(out, bytesOut, "<tr bgcolor=tomato><td><p><a href=\"javascript:viewPO('" + poCode + "')\">" + poCode + "</a></td>");
          else scoutln(out, bytesOut, "<tr id='" + cssFormat[0] + "'><td><p><a href=\"javascript:viewPO('" + poCode + "')\">" + poCode + "</a></td>");
          
          if(allReceived.equals("Y"))
            scoutln(out, bytesOut, "<td><p><span id='textRedHighlighting'>All Received</span></p></td>");
          else scoutln(out, bytesOut, "<td></td>");
              
          scoutln(out, bytesOut, "<td><p>" + generalUtils.convertFromYYYYMMDD(poDate) + "</td>");
          scoutln(out, bytesOut, "<td><p>" + companyCode + "</td>");
          scoutln(out, bytesOut, "<td><p>" + companyName + "</td>");
          first = false;
          
          ++maybe[0];
        }
        else
        {
          if(red[0]) scoutln(out, bytesOut, "<tr bgcolor=tomato><td></td><td></td><td></td><td></td><td></td>"); else
            scoutln(out, bytesOut, "<tr id='" + cssFormat[0] + "'><td></td><td></td><td></td><td></td><td></td>");
        }
        
        scoutln(out, bytesOut, "<td><p>" + poItemCodes[x] + "</td>");
        scoutln(out, bytesOut, "<td align=center><p>" + generalUtils.formatNumeric(poItemQtys[x], dpOnQuantities) + "</td>");
        scoutln(out, bytesOut, "<td align=center><p>" + generalUtils.formatNumeric(grQty, dpOnQuantities) + "</td>");
        scoutln(out, bytesOut, "<td align=center><p>" + generalUtils.formatNumeric(saQty, dpOnQuantities) + "</td>");
        scoutln(out, bytesOut, "<td align=center><p>" + generalUtils.formatNumeric(iQty, dpOnQuantities) + "</td>");
        scoutln(out, bytesOut, "<td align=center><p>" + generalUtils.formatNumeric(cnQty, dpOnQuantities) + "</td>");
        scoutln(out, bytesOut, "<td><p><a href=\"javascript:details('" + poCode + "','" + itemCode + "')\">Details</a></td></tr>");
      }      
    }

    if(firstCSS && onlyOnePO)
    { 
      scoutln(out, bytesOut, "<tr><td><br><br><br><br><p><b>No Anomalies Found</td></tr>");
    } 
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
  private int scanGRNs(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String poCode, String[][] grItemCodes,
                       int[] grItemCodesSize, double[][] grItemQtys           , boolean[] red) throws Exception
  {
    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT t2.ItemCode, t2.Quantity    ,t1.DateStockProcessed    FROM grl AS t2 INNER JOIN gr AS t1 ON t2.GRCode = t1.GRCode "
                         + "WHERE t1.Status != 'C' AND t2.POCode = '" + poCode + "'");
      
    int itemCodesCount = 0;

    int encodedDate;
    int from = generalUtils.encodeFromYYYYMMDD("2008-01-01");
    int to = generalUtils.encodeFromYYYYMMDD("2008-01-31");
    
    while(rs.next())
    {
      itemCodesCount = addToList(con, stmt2, rs2, rs.getString(1), rs.getString(2), grItemCodes, grItemCodesSize, grItemQtys, itemCodesCount);

      encodedDate = generalUtils.encodeFromYYYYMMDD(rs.getString(3));
      if(encodedDate >= from && encodedDate <= to)
      {  
        red[0] = true;   
      }
    }
  
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return itemCodesCount;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private int scanInvoices(Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, ResultSet rs3, String poCode,
                            char dpOnQuantities, String[][] iItemCodes, int[] iItemCodesSize, double[][] iItemQtys,
                            String[][] cnItemCodes, int[] cnItemCodesSize, double[][] cnItemQtys, int[] cnItemCodesCount) throws Exception
  {
    byte[] b = new byte[30];
    byte[] invoiceCodes    = new byte[1000]; invoiceCodes[0]    = '\000';
    int[]  invoiceCodesLen = new int[1];     invoiceCodesLen[0] = 1000;
    
    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT t2.ItemCode, t2.Quantity, t2.InvoiceCode FROM pinvoicel AS t2 INNER JOIN pinvoice AS t1 ON t2.InvoiceCode = t1.InvoiceCode "
                         + "WHERE t1.Status != 'C' AND t2.POrefNum = '" + poCode + "'");
      
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

      scanForCNs(con, stmt2, stmt3, rs2, rs3, invoiceCode, dpOnQuantities, cnItemCodes, cnItemCodesSize, cnItemQtys, cnItemCodesCount);
    }
  
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return itemCodesCount;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scanForCNs(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String invoiceCode, char dpOnQuantities,
                         String[][] cnItemCodes, int[] cnItemCodesSize, double[][] cnItemQtys, int[] cnItemCodesCount) throws Exception
  {
    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT t2.ItemCode, t2.Quantity FROM pcreditl AS t2 INNER JOIN pcredit AS t1 ON t2.PCNCode = t1.PCNCode "
                         + "WHERE t1.Status != 'C' AND t2.InvoiceCode = '" + invoiceCode + "'");
      
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
  private void clearList(String[][] itemCodes, double[][] itemQtys, int itemCodesCount) throws Exception
  {
    for(int x=0;x<itemCodesCount;++x)
    {
      itemCodes[0][x] = "";
      itemQtys[0][x] = 0.0;
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
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
  private int scanForStockA(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String poCode, char dpOnQuantities,
                            String[][] saItemCodes, int[] saItemCodesSize, double[][] saItemQtys       , boolean[] red) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT ItemCode, Quantity, Date FROM stocka WHERE POCode = '" + poCode + "' AND Status != 'C' AND StoreFrom != 'None' AND StoreTo = 'None'");

    int itemCodesCount = 0;
    double qtyCount;
    String itemCode;

    int encodedDate;
    int from = generalUtils.encodeFromYYYYMMDD("2008-01-01");
    int to = generalUtils.encodeFromYYYYMMDD("2008-01-31");

    while(rs.next())
    {
      itemCode = rs.getString(1);
      qtyCount = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(2)), dpOnQuantities);
   
      itemCodesCount = addToList(con, stmt2, rs2, itemCode, generalUtils.doubleToStr(qtyCount), saItemCodes, saItemCodesSize, saItemQtys, itemCodesCount);

      encodedDate = generalUtils.encodeFromYYYYMMDD(rs.getString(3));
      if(encodedDate >= from && encodedDate <= to)
      red[0] = true;   
    }
      
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return itemCodesCount;
  }

}
