// =============================================================================================================================================================
// System: ZaraStar: Analytics - SIT Analysis - details
// Module: AnalyticsSITAnalysisDetails.java
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

public class AnalyticsSITAnalysisDetails extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  DrawingUtils drawingUtils = new DrawingUtils();
  MiscDefinitions miscDefinitions = new MiscDefinitions();
  Inventory inventory = new Inventory();
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
      p1  = req.getParameter("p1"); // POCode
      p2  = req.getParameter("p2"); // itemCode
      p3  = req.getParameter("p3"); // dateTo
      
      if(p1 == null) p1 = "";
      if(p2 == null) p2 = "";
      
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
 
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "AnalyticsSITAnalysisInput", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6037, bytesOut[0], 0, "ERR:" + p1 + ":" + p2);
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

    Statement stmt = null, stmt2 = null;
    ResultSet rs   = null, rs2   = null;
    
    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 6037, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "AnalyticsSITAnalysisDetails", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6037, bytesOut[0], 0, "ACC:" + p1 + ":" + p2);
      
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "AnalyticsSITAnalysisDetails", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6037, bytesOut[0], 0, "SID:" + p1 + ":" + p2);
      
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    set(con, stmt, stmt2, rs, rs2, out, req, p1, p2, p3, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 6037, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1 + ":" + p2);
    
    if(con != null) con.close();
    if(out != null) out.flush();
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, HttpServletRequest req,
                   String poCode, String itemCode, String dateTo, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                   String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>SIT Analysis</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 5006, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewPO(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/PurchaseOrderPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm="
                           + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 5080, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewInvoice(code){");
      scoutln(out, bytesOut, "var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/PurchaseInvoicePage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm="
                           + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3011, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewSA(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/StockAdjustmentItem?unm=" + unm + "&sid=" + sid + "&uty="
                           + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + itemCode + "\";}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3025, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewGR(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/GoodsReceivedPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm="
                           + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 5026, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewPCN(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/PurchaseCreditNotePage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm="
                           + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }
      
    scoutln(out, bytesOut, "function item(code){");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/ProductStockRecord?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den
                           + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p1=\"+code;}");

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

    drawingUtils.outputPageFrame(con, stmt, rs, out, req, "6037", "", "AnalyticsSITAnalysisDetails", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount, bytesOut);

    drawingUtils.drawTitle(out, false, false, "AnalyticsSITAnalysisDetails", "", "Stock-in-Transit Analysis", "6037", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<table id=\"page\" width=100% border=0>");

    scoutln(out, bytesOut, "<tr><td></td></tr><tr id=\"pageColumn\">");

    scoutln(out, bytesOut, "<td><p>Document</td>");
    scoutln(out, bytesOut, "<td><p>Code</td>");
    scoutln(out, bytesOut, "<td><p>Date</td>");
    scoutln(out, bytesOut, "<td><p>ItemCode</td>");
    scoutln(out, bytesOut, "<td><p>Quantity</td>");
    scoutln(out, bytesOut, "<td><p>UnitPrice</td></tr>");

    char dpOnQuantities = miscDefinitions.dpOnQuantities(con, stmt, rs, "5");

    allLines(con, stmt, stmt2, rs, rs2, out, poCode, itemCode, dpOnQuantities, bytesOut);

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    String baseCurrency = accountsUtils.getBaseCurrency(con, stmt, rs, dnm, localDefnsDir, defnsDir);
    
    getTrail(con, stmt, rs, out, itemCode, dateTo, dpOnQuantities, baseCurrency, bytesOut);

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

  // ----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void allLines(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, String poCode, String itemCode,
                        char dpOnQuantities, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t1.Date, t2.Quantity "
                         + "FROM pol AS t2 INNER JOIN po AS t1 ON t2.POCode = t1.POCode WHERE t1.Status != 'C' AND t2.POCode = '" + poCode
                         + " 'AND t2.ItemCode = '" + itemCode + "'");
      
    String date, qty;
    String[] cssFormat = new String[1];  cssFormat[0] = "";

    while(rs.next())
    {
      date = rs.getString(1);
      qty  = rs.getString(2);
    
      if(cssFormat[0].equals("line1")) cssFormat[0] = "line2"; else cssFormat[0] = "line1";

      scoutln(out, bytesOut, "<tr id='" + cssFormat[0] + "'><td><p>Purchase Order</td><td><p><a href=\"javascript:viewPO('" + poCode + "')\">" + poCode
                           + "</a></td>");
      scoutln(out, bytesOut, "<td><p>" + generalUtils.convertFromYYYYMMDD(date) + "</td><td><p>" + itemCode + "</td>");
      scoutln(out, bytesOut, "<td><p>" + generalUtils.formatNumeric(qty, dpOnQuantities) + "</td>");
      scoutln(out, bytesOut, "<td></td></tr>");
    }
      
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t1.GRCode, t1.Date, t2.Quantity "
                         + "FROM grl AS t2 INNER JOIN gr AS t1 ON t2.GRCode = t1.GRCode WHERE t1.Status != 'C' AND t2.POCode = '" + poCode
                         + "' AND t2.ItemCode = '" + itemCode + "'");
      
    String code;

    while(rs.next())
    {
      code = rs.getString(1);
      date = rs.getString(2);
      qty  = rs.getString(3);
    
      if(cssFormat[0].equals("line1")) cssFormat[0] = "line2"; else cssFormat[0] = "line1";

      scoutln(out, bytesOut, "<tr id='" + cssFormat[0] + "'><td><p>Goods Received Note</td><td><p><a href=\"javascript:viewGR('" + code + "')\">" + code
                           + "</a></td>");
      scoutln(out, bytesOut, "<td><p>" + generalUtils.convertFromYYYYMMDD(date) + "</td><td><p>" + itemCode + "</td>");
      scoutln(out, bytesOut, "<td><p>" + generalUtils.formatNumeric(qty, dpOnQuantities) + "</td>");
      scoutln(out, bytesOut, "<td></td></tr>");
    }
      
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t1.InvoiceCode, t1.Date, t2.Quantity, t2.UnitPrice "
                         + "FROM pinvoicel AS t2 INNER JOIN pinvoice AS t1 ON t2.InvoiceCode = t1.InvoiceCode WHERE t1.Status != 'C' AND t2.POrefNum = '"
                         + poCode + "' AND t2.ItemCode = '" + itemCode + "'");
      
    String unitPrice;
    
    while(rs.next())
    {
      code      = rs.getString(1);
      date      = rs.getString(2);
      qty       = rs.getString(3);
      unitPrice = rs.getString(4);
    
      if(cssFormat[0].equals("line1")) cssFormat[0] = "line2"; else cssFormat[0] = "line1";

      scoutln(out, bytesOut, "<tr id='" + cssFormat[0] + "'><td><p>Purchase Invoice</td><td><p><a href=\"javascript:viewInvoice('" + code + "')\">" + code
                           + "</a></td>");
      scoutln(out, bytesOut, "<td><p>" + generalUtils.convertFromYYYYMMDD(date) + "</td><td><p>" + itemCode + "</td>");
      scoutln(out, bytesOut, "<td><p>" + generalUtils.formatNumeric(qty, dpOnQuantities) + "</td>");
      scoutln(out, bytesOut, "<td><p>" + generalUtils.formatNumeric(unitPrice, '2') + "</td></tr>");

      cnLines(con, stmt2, rs2, out, code, itemCode, dpOnQuantities, cssFormat, bytesOut);
    }
      
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT AdjustmentCode, Date, Quantity FROM stocka WHERE POCode = '" + poCode
                         + "' AND StoreFrom != 'None' AND StoreTo = 'None' AND ItemCode = '" + itemCode + "'");
      
    while(rs.next())
    {
      code = rs.getString(1);
      date = rs.getString(2);
      qty  = rs.getString(3);
    
      if(cssFormat[0].equals("line1")) cssFormat[0] = "line2"; else cssFormat[0] = "line1";

      scoutln(out, bytesOut, "<tr id='" + cssFormat[0] + "'><td><p>Stock Adjustment</td><td><p><a href=\"javascript:viewSA('" + code + "')\">" + code
                           + "</a></td>");
      scoutln(out, bytesOut, "<td><p>" + generalUtils.convertFromYYYYMMDD(date) + "</td><td><p>" + itemCode + "</td>");
      scoutln(out, bytesOut, "<td><p>" + generalUtils.formatNumeric(qty, dpOnQuantities) + "</td></tr>");
    }
      
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }    
    
  // ----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void cnLines(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String invoiceCode, String itemCode, char dpOnQuantities,
                       String[] cssFormat, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t1.PCNCode, t1.Date, t2.Quantity "
                         + "FROM pcreditl AS t2 INNER JOIN pcredit AS t1 ON t2.PCNCode = t1.PCNCode WHERE t1.Status != 'C' AND t2.InvoiceCode = '"
                         + invoiceCode + "' AND t2.ItemCode = '" + itemCode + "'");
      
    String code, date, qty;
    
    while(rs.next())
    {
      code = rs.getString(1);
      date = rs.getString(2);
      qty  = rs.getString(3);
    
      if(cssFormat[0].equals("line1")) cssFormat[0] = "line2"; else cssFormat[0] = "line1";

      scoutln(out, bytesOut, "<tr id='" + cssFormat[0] + "'><td><p>Purchase Credit Note</td><td><p><a href=\"javascript:viewPCN('" + code + "')\">" + code
                           + "</a></td>");
      scoutln(out, bytesOut, "<td><p>" + generalUtils.convertFromYYYYMMDD(date) + "</td><td><p>" + itemCode + "</td>");
      scoutln(out, bytesOut, "<td><p>" + generalUtils.formatNumeric(qty, dpOnQuantities) + "</td>");
      scoutln(out, bytesOut, "<td></td></tr>");
    }
      
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  } 
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void getTrail(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String itemCode, String dateTo, char dpOnQuantities, String baseCurrency,
                        int[] bytesOut) throws Exception
  {
    String dateFrom, desc, cssFormat = "";
    byte[] data = new byte[5000];
    
    if(inventory.getStockRecGivenCode(con, stmt, rs, itemCode, '\000', data) == 0)
    {
      dateFrom = generalUtils.dfsAsStr(data, (short)58);
      desc     = generalUtils.dfsAsStr(data, (short)1);
        
      if(cssFormat.equals("line1")) cssFormat = "line2"; else cssFormat = "line1";

      getWAC(con, stmt, rs, out, itemCode, desc, dateFrom, dateTo, dpOnQuantities, baseCurrency, cssFormat, bytesOut);
    }
  }
 
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private double getWAC(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String itemCode, String desc, String dateFrom, String dateTo,
                        char dpOnQuantities, String baseCurrency, String cssFormat, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\"><td colspan=8><hr></td></tr>");
    scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\"><td colspan=8><p><b>ItemCode : " + itemCode + " - " + desc + "</td></tr>");

    scoutln(out, bytesOut, "<tr id='pageColumn'><td><p>Type</td>");
    scoutln(out, bytesOut, "<td><p>Code</td>");
    scoutln(out, bytesOut, "<td><p>Date</td>");
    scoutln(out, bytesOut, "<td align=right><p>Quantity</td>");
    scoutln(out, bytesOut, "<td align=right><p>Value " + baseCurrency + "</td>");
    scoutln(out, bytesOut, "<td align=right><p>Total Quantity</td>");
    scoutln(out, bytesOut, "<td align=right><p>Total Value " + baseCurrency + "</td></tr>");

    double[] openingLevel = new double[1];
    double[] openingWAC   = new double[1];
            
    inventory.getWACDetailsGivenCode(con, stmt, rs, itemCode, openingLevel, openingWAC, dateFrom);
     
    double invoiceQty = openingLevel[0], invoiceBaseAmount = openingWAC[0] * openingLevel[0];
    
    scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\"><td><p>Opening Stock Level</td><td><a href=\"javascript:item('" + itemCode + "')\">"
                         + itemCode + "</a></td><td><p>" + generalUtils.convertFromYYYYMMDD(dateFrom)
                         + "</td><td align=right><p>"
                         + generalUtils.formatNumeric(openingLevel[0], dpOnQuantities) + "</td><td align=right></p>"
                         + generalUtils.formatNumeric((openingWAC[0] * openingLevel[0]), '2') + "</td><td align=right><p>"
                         + generalUtils.formatNumeric(invoiceQty, dpOnQuantities) + "</td><td align=right><p>" + generalUtils.formatNumeric(invoiceBaseAmount, '2')
                         + "</td></tr>");

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.Quantity, t2.Amount, t1.InvoiceCode, t1.Date "
                         + "FROM pinvoicel AS t2 INNER JOIN pinvoice AS t1 ON t1.InvoiceCode = t2.InvoiceCode WHERE t2.ItemCode = '" + itemCode
                         + "' AND t1.Status != 'C' AND t1.Date >= {d '" + dateFrom + "'} AND t1.Date <= {d '" + dateTo + "'}");

    String code;
 
    while(rs.next())
    {    
      invoiceQty        += generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(1)), '2');
      invoiceBaseAmount += (generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(2)), '2') );
      code = rs.getString(3);

      scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\"><td><p>Purchase Invoice</td><td><p><a href=\"javascript:invoice('" + code + "')\">"
                           + code + "</a></td><td><p>" + generalUtils.convertFromYYYYMMDD(rs.getString(4)) + "</td><td align=right><p>"
                           + generalUtils.formatNumeric(rs.getString(1), dpOnQuantities) + "</td><td align=right><p>"
                           + generalUtils.formatNumeric(rs.getString(2), '2') + "</td><td align=right><p>"
                           + generalUtils.formatNumeric(invoiceQty, dpOnQuantities) + "</td><td align=right><p>"
                           + generalUtils.formatNumeric(invoiceBaseAmount, '2') + "</td></tr>");
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
   
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.Quantity, t2.Amount, t1.PCNCode, t1.Date "
                         + "FROM pcreditl AS t2 INNER JOIN pcredit AS t1 ON t1.PCNCode = t2.PCNCode WHERE t2.ItemCode = '" + itemCode
                         + "' AND t1.Status != 'C' AND t1.Date >= {d '" + dateFrom + "'} AND t1.Date <= {d '" + dateTo + "'}");

    double cnQty = 0.0, cnBaseAmount = 0.0;
    
    while(rs.next())
    {    
      cnQty        += generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(1)), '2');
      cnBaseAmount += (generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(2)), '2') );
      code = rs.getString(3);

      scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\"><td><p>Purchase Credit Note</td><td><p><a href=\"javascript:pcn('" + code + "')\">"
                           + code + "</a></td><td align=right><p>"
                           + generalUtils.convertFromYYYYMMDD(rs.getString(4)) + "</td><td align=right><p>" + generalUtils.formatNumeric(rs.getString(1), dpOnQuantities)
                           + "</td><td align=right><p>" + generalUtils.formatNumeric(rs.getString(2), '2') + "</td><td align=right><p>"
                           + generalUtils.formatNumeric(cnQty, dpOnQuantities) + "</td><td align=right><p>" + generalUtils.formatNumeric(cnBaseAmount, '2') + "</td></tr>");
    }
    
    double currentLevel = invoiceQty - cnQty;
    
    if(currentLevel == 0)
      currentLevel = 1;
    
    double currentWAC;

    if(currentLevel == 0)
      currentLevel = 1;

    currentWAC = generalUtils.doubleDPs((invoiceBaseAmount - cnBaseAmount) / currentLevel, '2');
        
    scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\"><td colspan=6 align=right><p><b>WAC:</td><td><p><b>" + baseCurrency + " " 
                         + generalUtils.formatNumeric(currentWAC, '2') + "</td></tr>");

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return currentWAC;
  }

}
