// =======================================================================================================================================================================================================
// System: ZaraStar: Sales Analytics - Sales Closure Analysis
// Module: SalesClosureAnalysisInputExecute.java
// Author: C.K.Harvey
// Copyright (c) 2001-08 Christopher Harvey. All Rights Reserved.
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

public class SalesClosureAnalysisInputExecute extends HttpServlet
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
      p1  = req.getParameter("p1"); // SOCode (optional)
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
 
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "SalesClosureAnalysisInput", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1202, bytesOut[0], 0, "ERR:" + p1 + ":" + p2 + ":" + p3);
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
    
    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 1202, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "SalesClosureAnalysisInputa", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1202, bytesOut[0], 0, "ACC:" + p1 + ":" + p2 + ":" + p3);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "SalesClosureAnalysisInputa", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1202, bytesOut[0], 0, "SID:" + p1 + ":" + p2 + ":" + p3);
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
    
    set(con, stmt, stmt2, stmt3, stmt4, rs, rs2, rs3, rs4, out, req, p1, dateFrom, dateTo, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 1202, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1 + ":" + p2 + ":" + p3);
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, Statement stmt2, Statement stmt3, Statement stmt4, ResultSet rs, ResultSet rs2, ResultSet rs3,
                   ResultSet rs4, PrintWriter out, HttpServletRequest req, String soCode, String dateFrom, String dateTo, String unm, String sid, String uty,
                   String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Sales: Closure Analysis</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4031, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewSO(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/SalesOrderPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    scoutln(out, bytesOut, "function details(code,item){var p1=sanitise(code);var p2=sanitise(item);");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/SalesClosureAnalysisInputb?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=\"+p2+\"&p1=\"+p1;}");

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

    drawingUtils.outputPageFrame(con, stmt, rs, out, req, "1202", "", "SalesClosureAnalysisInput", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount, bytesOut);

    drawingUtils.drawTitle(out, false, false, "SalesClosureAnalysisInput", "", "Sales: Closure ", "1202", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<table id=\"page\" width=100% border=0>");

    scoutln(out, bytesOut, "<tr><td></td></tr><tr id=\"pageColumn\">");

    scoutln(out, bytesOut, "<td><p>SO Code</td><td></td>");
    scoutln(out, bytesOut, "<td><p>SO Date</td>");
    scoutln(out, bytesOut, "<td><p>Customer Code</td>");
    scoutln(out, bytesOut, "<td><p>Customer Name</td>");
    scoutln(out, bytesOut, "<td><p>ItemCode</td>");
    scoutln(out, bytesOut, "<td><p>Sales Order Quantity</td>");
    scoutln(out, bytesOut, "<td><p>Picking List Quantity</td>");
    scoutln(out, bytesOut, "<td><p>Adjustment Quantity</td>");
    scoutln(out, bytesOut, "<td><p>Delivery Order Quantity</td>");
    scoutln(out, bytesOut, "<td><p>Invoice Quantity</td>");
    scoutln(out, bytesOut, "<td><p>Credit Note Quantity (CoS checked)</td>");
    scoutln(out, bytesOut, "<td><p>Credit Note Quantity (All)</td>");
    scoutln(out, bytesOut, "<td></td></tr>");

    char dpOnQuantities = miscDefinitions.dpOnQuantities(con, stmt, rs, "5");

    String[] cssFormat = new String[1];  cssFormat[0] = "line1";
    int[] maybe  = new int[1];  maybe[0]  = 0;

    if(soCode.length() == 0)
      forAllSalesOrders(con, stmt, stmt2, stmt3, stmt4, rs, rs2, rs3, rs4, out, dateFrom, dateTo, dpOnQuantities, cssFormat, maybe, bytesOut);
    else forASalesOrder(con, stmt, stmt2, stmt3, rs, rs2, rs3, out, soCode, dpOnQuantities, cssFormat, maybe, bytesOut);
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=8><p>Possible Errors: Sales Orders with Issues: " + maybe[0] + "</td></tr>");
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
  private void forAllSalesOrders(Connection con, Statement stmt, Statement stmt2, Statement stmt3, Statement stmt4, ResultSet rs, ResultSet rs2, ResultSet rs3,
                                 ResultSet rs4, PrintWriter out, String dateFrom, String dateTo, char dpOnQuantities, String[] cssFormat, int[] maybe,
                                 int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.SOCode, t2.ItemCode, t2.Quantity FROM sol AS t2 INNER JOIN so AS t1 ON t2.SOCode = t1.SOCode "
                         + "WHERE t1.Status != 'C' AND t1.Date >= {d '" + dateFrom + "'} AND t1.Date <= {d '" + dateTo + "'} ORDER BY t1.SOCode");
      
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
    
    String[][] cnItemCodes2 = new String[1][100];
    double[][] cnItemQtys2  = new double[1][100];
    int[] cnItemCodesSize2  = new int[1];  cnItemCodesSize2[0] = 100;

    String[] companyCode = new String[1];
    String[] companyName = new String[1];
    String[] soDate      = new String[1];
    String[] allSupplied = new String[1];

    String soCode, lastSOCode = "";
    boolean first = true;
    
    while(rs.next())
    {
      soCode = rs.getString(1);
      
      if(! lastSOCode.equals(soCode))
      {
        if(! first)
        {
          soDetails(con, stmt2, rs2, lastSOCode, companyCode, companyName, soDate, allSupplied);
        
          processSO(con, stmt2, stmt3, stmt4, rs2, rs3, rs4, out, false, lastSOCode, soItemCodes[0], soItemQtys[0], soItemCodesCount, plItemCodes,
                    plItemCodesSize, plItemQtys, doItemCodes, doItemCodesSize, doItemQtys, saItemCodes, saItemCodesSize, saItemQtys, iItemCodes,
                    iItemCodesSize, iItemQtys, cnItemCodes, cnItemCodesSize, cnItemQtys, cnItemCodes2, cnItemCodesSize2, cnItemQtys2, dpOnQuantities, cssFormat,
                    companyCode[0], companyName[0], soDate[0], allSupplied[0], maybe, bytesOut);
        
          clearList(soItemCodes, soItemQtys, soItemCodesCount);
          soItemCodesCount = 0;
        }
        else first = false;

        lastSOCode = soCode;
      }
  
      soItemCodesCount = addToList(con, stmt3, rs3, rs.getString(2), rs.getString(3), soItemCodes, soItemCodesSize, soItemQtys, soItemCodesCount);
    }
      
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void forASalesOrder(Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, ResultSet rs3, PrintWriter out,
                              String soCode, char dpOnQuantities, String[] cssFormat, int[] maybe, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t1.Date, t1.AllSupplied, t1.CompanyCode, t1.CompanyName, t2.ItemCode, t2.Quantity "
                         + "FROM sol AS t2 INNER JOIN so AS t1 ON t2.SOCode = t1.SOCode WHERE t1.Status != 'C' AND t2.SOCode = '" + soCode + "'");
      
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
    
    String[][] cnItemCodes2 = new String[1][100];
    double[][] cnItemQtys2  = new double[1][100];
    int[] cnItemCodesSize2  = new int[1];  cnItemCodesSize2[0] = 100;
    
    String companyCode="", companyName="", soDate="", allSupplied="", itemCode, qty;

    while(rs.next())
    {
      soDate      = rs.getString(1);
      allSupplied = rs.getString(2);
      companyCode = rs.getString(3);
      companyName = rs.getString(4);
      itemCode    = rs.getString(5);
      qty         = rs.getString(6);
    
      soItemCodesCount = addToList(con, stmt3, rs3, itemCode, qty, soItemCodes, soItemCodesSize, soItemQtys, soItemCodesCount);
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    processSO(con, stmt, stmt2, stmt3, rs, rs2, rs3, out, true, soCode, soItemCodes[0], soItemQtys[0], soItemCodesCount, plItemCodes, plItemCodesSize,
              plItemQtys, doItemCodes, doItemCodesSize, doItemQtys, saItemCodes, saItemCodesSize, saItemQtys, iItemCodes, iItemCodesSize, iItemQtys,
              cnItemCodes, cnItemCodesSize, cnItemQtys, cnItemCodes2, cnItemCodesSize2, cnItemQtys2, dpOnQuantities, cssFormat, companyCode, companyName,
              soDate, allSupplied, maybe, bytesOut);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void processSO(Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, ResultSet rs3, PrintWriter out,
                         boolean onlyOneSO, String soCode, String[] soItemCodes, double[] soItemQtys, int soItemCodesCount, String[][] plItemCodes,
                         int[] plItemCodesSize, double[][] plItemQtys, String[][] doItemCodes, int[] doItemCodesSize, double[][] doItemQtys,
                         String[][] saItemCodes, int[] saItemCodesSize, double[][] saItemQtys, String[][] iItemCodes, int[] iItemCodesSize,
                         double[][] iItemQtys, String[][] cnItemCodes, int[]cnItemCodesSize, double[][] cnItemQtys, String[][] cnItemCodes2,
                         int[]cnItemCodesSize2, double[][] cnItemQtys2, char dpOnQuantities, String[] cssFormat,
                         String companyCode, String companyName, String soDate, String allSupplied, int[] maybe, int[] bytesOut) throws Exception
  {
    int[] cnItemCodesCount  = new int[1];  cnItemCodesCount[0]  = 0; 
    int[] cnItemCodesCount2 = new int[1];  cnItemCodesCount2[0] = 0; 
boolean[] red = new boolean[1]; red[0] = false;
    int plItemCodesCount = scanPickingLists(con, stmt, stmt2, rs, rs2, soCode, plItemCodes, plItemCodesSize, plItemQtys    ,red);
    int doItemCodesCount = scanDOs(con, stmt, stmt2, rs, rs2, soCode, doItemCodes, doItemCodesSize, doItemQtys);
    int iItemCodesCount  = scanInvoices(con, stmt, stmt2, stmt3, rs, rs2, rs3, soCode, dpOnQuantities, iItemCodes, iItemCodesSize, iItemQtys, cnItemCodes,
                                        cnItemCodesSize, cnItemQtys, cnItemCodesCount, cnItemCodes2, cnItemCodesSize2, cnItemQtys2, cnItemCodesCount2);
    int saItemCodesCount = scanForStockA(con, stmt, stmt2, rs, rs2, soCode, dpOnQuantities, saItemCodes, saItemCodesSize, saItemQtys     , red);
    
    String itemCode;
    boolean first = true, firstCSS = true;
    double plQty, iQty, doQty, saQty, cnQty, cnQty2;
    
    int x = 0;
    for(x=0;x<soItemCodesCount;++x)
    {
      itemCode = soItemCodes[x];
      
      plQty = qtyOnList(itemCode, plItemCodes[0], plItemQtys[0], plItemCodesCount);

      doQty = qtyOnList(itemCode, doItemCodes[0], doItemQtys[0], doItemCodesCount);

      iQty = qtyOnList(itemCode, iItemCodes[0], iItemQtys[0], iItemCodesCount);

      saQty = qtyOnList(itemCode, saItemCodes[0], saItemQtys[0], saItemCodesCount);

      cnQty = qtyOnList(itemCode, cnItemCodes[0], cnItemQtys[0], cnItemCodesCount[0]);

      cnQty2 = qtyOnList(itemCode, cnItemCodes2[0], cnItemQtys2[0], cnItemCodesCount2[0]);

///      if(plQty != soItemQtys[x] || doQty != soItemQtys[x] || iQty != soItemQtys[x])
      if(plQty != soItemQtys[x] || (doQty + saQty) != soItemQtys[x] || (iQty - cnQty + saQty) != soItemQtys[x])
      {
        if(firstCSS)
        { 
          if(cssFormat[0].equals("line1")) cssFormat[0] = "line2"; else cssFormat[0] = "line1";
          firstCSS = false;
        }
        
        if(first)
        { 
if(red[0])scoutln(out, bytesOut, "<tr bgcolor=tomato><td><p><a href=\"javascript:viewSO('" + soCode + "')\">" + soCode + "</a></td>"); else
          scoutln(out, bytesOut, "<tr id='" + cssFormat[0] + "'><td><p><a href=\"javascript:viewSO('" + soCode + "')\">" + soCode + "</a></td>");
          
          if(allSupplied.equals("Y"))
            scoutln(out, bytesOut, "<td><p><span id='textRedHighlighting'>All Supplied</span></p></td>");
          else scoutln(out, bytesOut, "<td></td>");
              
          scoutln(out, bytesOut, "<td><p>" + generalUtils.convertFromYYYYMMDD(soDate) + "</td>");
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
        
        scoutln(out, bytesOut, "<td><p>" + soItemCodes[x] + "</td>");
        scoutln(out, bytesOut, "<td align=center><p>" + generalUtils.formatNumeric(soItemQtys[x], dpOnQuantities) + "</td>");
        scoutln(out, bytesOut, "<td align=center><p>" + generalUtils.formatNumeric(plQty, dpOnQuantities) + "</td>");
        scoutln(out, bytesOut, "<td align=center><p>" + generalUtils.formatNumeric(saQty, dpOnQuantities) + "</td>");
        scoutln(out, bytesOut, "<td align=center><p>" + generalUtils.formatNumeric(doQty, dpOnQuantities) + "</td>");
        scoutln(out, bytesOut, "<td align=center><p>" + generalUtils.formatNumeric(iQty, dpOnQuantities) + "</td>");
        scoutln(out, bytesOut, "<td align=center><p>" + generalUtils.formatNumeric(cnQty, dpOnQuantities) + "</td>");
        scoutln(out, bytesOut, "<td align=center><p>" + generalUtils.formatNumeric(cnQty2, dpOnQuantities) + "</td>");
        scoutln(out, bytesOut, "<td><p><a href=\"javascript:details('" + soCode + "','" + itemCode + "')\">Details</a></td></tr>");
      }      
    }

    if(firstCSS && onlyOneSO)
    { 
      scoutln(out, bytesOut, "<tr><td><br><br><br><br><p><b>No Anomalies Found</td></tr>");
    } 
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
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
  private int scanPickingLists(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String soCode, String[][] plItemCodes,
                               int[] plItemCodesSize, double[][] plItemQtys          , boolean[] red) throws Exception
  {
    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT t2.ItemCode, t2.QuantityPacked     , t1.Date FROM pll AS t2 INNER JOIN pl AS t1 ON t2.PLCode = t1.PLCode "
                         + "WHERE t1.Status != 'C' AND t2.SOCode = '" + soCode + "'");
      
    int itemCodesCount = 0;

    while(rs.next())
    {
      itemCodesCount = addToList(con, stmt2, rs2, rs.getString(1), rs.getString(2), plItemCodes, plItemCodesSize, plItemQtys, itemCodesCount);
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return itemCodesCount;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private int scanDOs(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String soCode, String[][] doItemCodes,
                      int[] doItemCodesSize, double[][] doItemQtys) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.ItemCode, t2.Quantity FROM dol AS t2 INNER JOIN do AS t1 ON t2.DOCode = t1.DOCode "
                         + "WHERE t1.Status != 'C' AND t2.SOCode = '" + soCode + "'");
      
    int itemCodesCount = 0;
    
    while(rs.next())
      itemCodesCount = addToList(con, stmt2, rs2, rs.getString(1), rs.getString(2), doItemCodes, doItemCodesSize, doItemQtys, itemCodesCount);
  
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return itemCodesCount;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private int scanInvoices(Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, ResultSet rs3, String soCode,
                           char dpOnQuantities, String[][] iItemCodes, int[] iItemCodesSize, double[][] iItemQtys, String[][] cnItemCodes,
                           int[] cnItemCodesSize, double[][] cnItemQtys, int[] cnItemCodesCount, String[][] cnItemCodes2, int[] cnItemCodesSize2,
                           double[][] cnItemQtys2, int[] cnItemCodesCount2) throws Exception
  {
    byte[] b = new byte[30];
    byte[] invoiceCodes    = new byte[1000]; invoiceCodes[0]    = '\000';
    int[]  invoiceCodesLen = new int[1];     invoiceCodesLen[0] = 1000;
    
    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT t2.ItemCode, t2.Quantity, t2.InvoiceCode FROM invoicel AS t2 INNER JOIN invoice AS t1 ON t2.InvoiceCode = t1.InvoiceCode "
                         + "WHERE t1.Status != 'C' AND t2.SOCode = '" + soCode + "'");
      
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
      scanForCNs2(con, stmt2, stmt3, rs2, rs3, invoiceCode, dpOnQuantities, cnItemCodes2, cnItemCodesSize2, cnItemQtys2, cnItemCodesCount2);
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

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void clearList(String[][] itemCodes, double[][] itemQtys, int itemCodesCount) throws Exception
  {
    for(int x=0;x<itemCodesCount;++x)
    {
      itemCodes[0][x] = "";
      itemQtys[0][x] = 0.0;
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void soDetails(Connection con, Statement stmt, ResultSet rs, String soCode, String[] companyCode, String[] companyName, String[] soDate,
                         String[] allSupplied) throws Exception
  {
    companyCode[0] = companyName[0] = "";
    
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT CompanyCode, CompanyName, Date, AllSupplied FROM so WHERE soCode = '" + soCode + "'");
      
    if(rs.next())
    {
      companyCode[0] = rs.getString(1);
      companyName[0] = rs.getString(2);
      soDate[0]      = rs.getString(3);
      allSupplied[0] = rs.getString(4);
    }
      
    if(allSupplied[0] == null) allSupplied[0] = "N";
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scanForCNs(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String invoiceCode, char dpOnQuantities,
                         String[][] cnItemCodes, int[] cnItemCodesSize, double[][] cnItemQtys, int[] cnItemCodesCount) throws Exception
  {
    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT t2.ItemCode, t2.Quantity FROM creditl AS t2 INNER JOIN credit AS t1 ON t2.CNCode = t1.CNCode "
                         + "WHERE t1.Status != 'C' AND t2.InvoiceCode = '" + invoiceCode + "' AND t2.CostOfSaleAdjustment = 'Y'");
      
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
  private void scanForCNs2(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String invoiceCode, char dpOnQuantities,
                           String[][] cnItemCodes2, int[] cnItemCodesSize2, double[][] cnItemQtys2, int[] cnItemCodesCount2) throws Exception
  {
    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT t2.ItemCode, t2.Quantity FROM creditl AS t2 INNER JOIN credit AS t1 ON t2.CNCode = t1.CNCode "
                         + "WHERE t1.Status != 'C' AND t2.InvoiceCode = '" + invoiceCode + "'");
      
    double qtyCount;
    String itemCode;
    
    while(rs.next())
    {
      itemCode = rs.getString(1);
      qtyCount = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(2)), dpOnQuantities);
      cnItemCodesCount2[0] = addToList(con, stmt2, rs2, itemCode, generalUtils.doubleToStr(qtyCount), cnItemCodes2, cnItemCodesSize2, cnItemQtys2, cnItemCodesCount2[0]);
    }
  
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private int scanForStockA(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String soCode, char dpOnQuantities,
                            String[][] saItemCodes, int[] saItemCodesSize, double[][] saItemQtys      , boolean[] red) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT ItemCode, Quantity      , Date     FROM stocka WHERE SOCode = '" + soCode + "' AND Status != 'C' AND StoreFrom = 'None' AND StoreTo != 'None'");

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

}
