// =======================================================================================================================================================================================================
// System: ZaraStar: Analytic: Stock Enquiry page
// Module: StockEnquiry.java
// Author: C.K.Harvey
// Copyright (c) 2000-12 Christopher Harvey. All Rights Reserved.
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

public class StockEnquiry extends HttpServlet
{
  GeneralUtils  generalUtils = new GeneralUtils();
  MessagePage  messagePage = new MessagePage();
  MiscDefinitions  miscDefinitions = new MiscDefinitions();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils  pageFrameUtils = new PageFrameUtils();
  DirectoryUtils  directoryUtils = new DirectoryUtils();
  Inventory  inventory = new Inventory();
  GoodsReceivedNote  goodsReceivedNote = new GoodsReceivedNote();
  DeliveryOrder  deliveryOrder = new DeliveryOrder();
  StockLevelsGenerate stockLevelsGenerate = new StockLevelsGenerate();
  
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
      p2  = req.getParameter("p2"); // mfr
      p3  = req.getParameter("p3"); // mfrCode
      
      if(p1 == null) p1 = "";
      if(p2 == null) p2 = "";
      if(p3 == null) p3 = "";
      
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

      System.out.println("1001a: " + e);
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "MainPageUtils1a", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1001, bytesOut[0], 0, "ERR:" + p1 + ":" + p2 + ":" + p3);
      if(out != null) out.flush(); 
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1, String p2, String p3, int[] bytesOut) throws Exception
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
    ResultSet rs = null, rs2 = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 1001, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "MainPageUtils1", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1001, bytesOut[0], 0, "ACC:" + p1 + ":" + p2 + ":" + p3);
      if(con != null) con.close();  
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "MainPageUtils1", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1001, bytesOut[0], 0, "SID:" + p1 + ":" + p2 + ":" + p3);
      if(con != null) con.close();  
      if(out != null) out.flush(); 
      return;
    }

    byte[] itemCode = new byte[21];
    generalUtils.strToBytes(itemCode, p1);
    generalUtils.toUpper(itemCode, 0);

    set(con, stmt, stmt2, rs, rs2, out, req, itemCode, p1, p2, p3, unm, sid, uty, men, den, dnm, bnm, imagesDir, localDefnsDir, defnsDir, bytesOut, startTime);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 1001, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1 + ":" + p2 + ":" + p3);
    if(con != null) con.close();  
    if(out != null) out.flush(); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, HttpServletRequest req, byte[] itemCode, String itemCodeStr, String mfr, String mfrCode, String unm, String sid, String uty,
                   String men, String den, String dnm, String bnm, String imagesDir, String localDefnsDir, String defnsDir, int[] bytesOut, long startTime) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Stock Enquiry</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 139, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewPO(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/PurchaseOrderPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }
    
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 152, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewLP(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/LocalPurchasePage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 140, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewSO(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/SalesOrderPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 147, unm, uty, dnm, localDefnsDir, defnsDir))
    {
       scoutln(out, bytesOut, "function viewPL(code){var p1=sanitise(code);");
       scoutln(out, bytesOut, "this.location.href=\"/central/servlet/GoodsReceivedPickingList?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 153, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewGR(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/GoodsReceivedPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3001, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewItem(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/ProductStockRecord?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p1=\"+p1;}");
    }

    boolean ProductCart = authenticationUtils.verifyAccess(con, stmt, rs, req, 121, unm, uty, dnm, localDefnsDir, defnsDir);

    if(ProductCart)
    {
      scoutln(out, bytesOut, "function addToCart(code){var p1=sanitise(code);");
      if(!uty.equals("I"))scoutln(out, bytesOut, "alert('Coming Soon');else ");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/ProductCartAddToCart?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=L&p1=\"+p1;}");
    }

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

    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "1001", "", "MainPageUtils1", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
  
    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Stock Enquiry Results", "1001", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<form><table id=\"page\" width=100% border=0><tr><td>");

    byte[] b      = new byte[100];
    byte[] remark = new byte[100];
    byte[] data   = new byte[5000];

    if(mfrCode.length() > 0)
    {
      itemCodeStr = inventory.getItemCodeGivenMfrAndMfrCode(con, stmt, rs, mfr, mfrCode);
      if(itemCodeStr.length() == 0)
      {
        scoutln(out, bytesOut, "<br><br><p>Manufacturer: " + mfr + ", and Manufacturer Code: " + mfrCode + " Not Found<br><br>");
        scoutln(out, bytesOut, "</table></form>");
        scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir));
        return;
      }
      
      generalUtils.strToBytes(itemCode, itemCodeStr);
    }

    if(itemCodeStr.length() == 0)
    {
      scoutln(out, bytesOut, "<br><br><p>No Item Code Specified<br><br>");
    }
    else
    if(inventory.getStockRecGivenCode(con, stmt, rs, itemCode, '\000', data) == -1) // rec not found
    {
      scoutln(out, bytesOut, "<br><br><p>Item Code &nbsp; &nbsp; " + itemCodeStr);
      scoutln(out, bytesOut, "&nbsp; &nbsp; <span id=\"generalMessage\">Not Found</span><br><br>");
    }
    else // found
    {
      generalUtils.dfs(data, (short)1, b);
      generalUtils.dfs(data, (short)2, remark);
      mfr     = generalUtils.dfsAsStr(data, (short)3);
      mfrCode = generalUtils.dfsAsStr(data, (short)4);

      scoutln(out, bytesOut, "<p>For Item Code: &nbsp; &nbsp; ");
      scoutln(out, bytesOut, "<a href=\"javascript:viewItem('" + itemCodeStr + "')\">" + itemCodeStr + "</a>");
      scoutln(out, bytesOut, "&nbsp; &nbsp; <b>" + generalUtils.stringFromBytes(b, 0L) + " " + generalUtils.stringFromBytes(remark, 0L) + "</b>");

      scoutln(out, bytesOut, "<p>Manufacturer: &nbsp; &nbsp; " + mfr + ": &nbsp; &nbsp; " + mfrCode);

      scoutln(out, bytesOut, "<p><table border=0>");
      scoutln(out, bytesOut, "<tr><td><p>Pricing: &nbsp; &nbsp;</td>");

      generalUtils.dfs(data, (short)25, b);
      generalUtils.bytesDPsGivenSeparator(true, '\000', '2', b, 20, 0);
      scoutln(out, bytesOut, "<td align=right><p> &nbsp; &nbsp; List: <span id=\"textNumericValue\">" + generalUtils.stringFromBytes(b, 0L) + "</span></td>");

      generalUtils.dfs(data, (short)20, b);
      generalUtils.bytesDPsGivenSeparator(true, '\000', '2', b, 20, 0);
      scoutln(out, bytesOut, "<td align=right><p> &nbsp; &nbsp; 1: <span id=\"textNumericValue\">" + generalUtils.stringFromBytes(b, 0L) + "</span></td>");

      generalUtils.dfs(data, (short)21, b);
      generalUtils.bytesDPsGivenSeparator(true, '\000', '2', b, 20, 0);
      scoutln(out, bytesOut, "<td align=right><p> &nbsp; &nbsp; 2: <span id=\"textNumericValue\">" + generalUtils.stringFromBytes(b, 0L) + "</span></td>");

      generalUtils.dfs(data, (short)22, b);
      generalUtils.bytesDPsGivenSeparator(true, '\000', '2', b, 20, 0);
      scoutln(out, bytesOut, "<td align=right><p> &nbsp; &nbsp; 3: <span id=\"textNumericValue\">" + generalUtils.stringFromBytes(b, 0L) + "</span></td>");

      generalUtils.dfs(data, (short)23, b);
      generalUtils.bytesDPsGivenSeparator(true, '\000', '2', b, 20, 0);
      scoutln(out, bytesOut, "<td align=right><p> &nbsp; &nbsp; 4: <span id=\"textNumericValue\"><font size=1>" + generalUtils.stringFromBytes(b, 0L) + "</font></span></td></tr>");

      scoutln(out, bytesOut, "<tr><td><p>Discounts: &nbsp; &nbsp; </td>");

      scoutln(out, bytesOut, "<td></td>");

      generalUtils.dfs(data, (short)38, b);
      generalUtils.bytesDPsGivenSeparator(true, '\000', '2', b, 20, 0);
      scoutln(out, bytesOut, "<td align=right><p> &nbsp; &nbsp;    <font size=2>" + generalUtils.stringFromBytes(b, 0L) + "</td>");

      generalUtils.dfs(data, (short)39, b);
      generalUtils.bytesDPsGivenSeparator(true, '\000', '2', b, 20, 0);
      scoutln(out, bytesOut, "<td align=right><p> &nbsp; &nbsp;    <font size=2>" + generalUtils.stringFromBytes(b, 0L) + "</td>");

      generalUtils.dfs(data, (short)40, b);
      generalUtils.bytesDPsGivenSeparator(true, '\000', '2', b, 20, 0);
      scoutln(out, bytesOut, "<td align=right><p> &nbsp; &nbsp;    <font size=2>" + generalUtils.stringFromBytes(b, 0L) + "</td>");

      generalUtils.dfs(data, (short)42, b);
      generalUtils.bytesDPsGivenSeparator(true, '\000', '2', b, 20, 0);
      scoutln(out, bytesOut, "<td align=right><p> &nbsp; &nbsp;    <font size=2>" + generalUtils.stringFromBytes(b, 0L) + "</td></tr>");
      scoutln(out, bytesOut, "</table>");

      char dpOnQuantities = miscDefinitions.dpOnQuantities(con, stmt, rs, "5");

      scoutln(out, bytesOut, "</td></tr></table>");

      if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3002, unm, uty, dnm, localDefnsDir, defnsDir))
      {
        scoutln(out, bytesOut, "<table id=\"page\" width=100% border=0>");

        scoutln(out, bytesOut, "<tr><td>&nbsp; &nbsp;</td></tr>");
        scoutln(out, bytesOut, "<tr><td nowrap colspan=4><p>Last five Purchase Order Purchases:</td></tr>\n");
        onPO(con, stmt, rs, out, itemCodeStr, dpOnQuantities, bytesOut);
        scoutln(out, bytesOut, "<tr><td>&nbsp; &nbsp;</td></tr>");
        scoutln(out, bytesOut, "<tr><td nowrap colspan=4><p>Last five Local Requisition Purchases:</td></tr>\n");
        onLP(con, stmt, rs, out, itemCodeStr, dpOnQuantities, bytesOut);
        scoutln(out, bytesOut, "<tr><td>&nbsp; &nbsp;</td></tr>");

        scoutln(out, bytesOut, "</table>");
      }
      
      scoutln(out, bytesOut, "<table id=\"page\" width=100% border=0>");
      stockLevels(con, stmt, stmt2, rs, out, itemCode, itemCodeStr, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, dpOnQuantities, bytesOut, startTime);

      // from this point on, store in string 'html' so that we can total and output totals before the details

      String[] html = new String[1];
      html[0] = "";

      double[] total = new double[1]; total[0] = 0.0;
      onOrder(con, stmt, stmt2, rs, rs2, itemCode, dpOnQuantities, total, html);
      generalUtils.doubleToBytesCharFormat(total[0], b, 0);
      generalUtils.bytesDPsGivenSeparator(true, '\000', dpOnQuantities, b, 20, 0);
      generalUtils.formatNumeric(b, dpOnQuantities);
      scoutln(out, bytesOut, "<tr><td nowrap><p>Total On Order (PO):</td><td align=right><span id=\"textNumericValue\">" + generalUtils.stringFromBytes(b, 0L) + "</span></td></tr>\n");

      total[0] = 0.0;
      onLocalOrder(con, stmt, stmt2, rs, rs2, itemCode, dpOnQuantities, total, html);
      generalUtils.doubleToBytesCharFormat(total[0], b, 0);
      generalUtils.bytesDPsGivenSeparator(true, '\000', dpOnQuantities, b, 20, 0);
      generalUtils.formatNumeric(b, dpOnQuantities);
      scoutln(out, bytesOut, "<tr><td nowrap><p>Total On Order (LP):</td><td align=right><span id=\"textNumericValue\">" + generalUtils.stringFromBytes(b, 0L) + "</span></td></tr>\n");

      total[0] = 0.0;
      onSO(con, stmt, stmt2, rs2, rs, itemCode, dpOnQuantities, total, html, startTime);
      generalUtils.doubleToBytesCharFormat(total[0], b, 0);
      generalUtils.bytesDPsGivenSeparator(true, '\000', dpOnQuantities, b, 20, 0);
      generalUtils.formatNumeric(b, dpOnQuantities);
      scoutln(out, bytesOut, "<tr><td nowrap><p>Total On Order (SO):</td><td align=right><span id=\"textNumericValue\">" + generalUtils.stringFromBytes(b, 0L) + "</span></td></tr>\n");

      total[0] = 0.0;
      onPLNotCompleted(con, stmt, rs, itemCode, dnm, localDefnsDir, defnsDir, dpOnQuantities, total, html, bytesOut);
      generalUtils.doubleToBytesCharFormat(total[0], b, 0);
      generalUtils.bytesDPsGivenSeparator(true, '\000', dpOnQuantities, b, 20, 0);
      generalUtils.formatNumeric(b, dpOnQuantities);
      scoutln(out, bytesOut, "<tr><td nowrap><p>Total Picking Lists Not Completed:</td><td align=right><span id=\"textNumericValue\">" + generalUtils.stringFromBytes(b, 0L) + "</span></td></tr>\n");

      total[0] = 0.0;
      onGRNInTransit(con, stmt, rs, itemCode, dnm, localDefnsDir, defnsDir, dpOnQuantities, total, html, bytesOut);
      generalUtils.doubleToBytesCharFormat(total[0], b, 0);
      generalUtils.bytesDPsGivenSeparator(true, '\000', dpOnQuantities, b, 20, 0);
      generalUtils.formatNumeric(b, dpOnQuantities);
      scoutln(out, bytesOut, "<tr><td nowrap><p>Total GRNs In-Transit:</td><td align=right><span id=\"textNumericValue\">" + generalUtils.stringFromBytes(b, 0L) + "</span></td></tr>\n");

      if(ProductCart)
      {
        scoutln(out, bytesOut, "<tr><td><a href=\"javascript:addToCart('" + itemCodeStr + "')\"><img border=0 src=\"" + imagesDir + "toCart.png\"></a></td></tr>");
      }
      
      scoutln(out, bytesOut, "</table><table id=\"page\" width=100%>");
      scoutln(out, bytesOut, "<tr><td colspan=7><hr></td></tr>");

      scoutln(out, bytesOut, html[0]);
    }

    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir));
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void stockLevels(Connection con, Statement stmt, Statement stmt2, ResultSet rs, PrintWriter out, byte[] itemCode, String itemCodeStr, String unm,
                          String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, char dpOnQuantities,
                          int[] bytesOut, long startTime) throws Exception
  {
    String traceList = getStockLevelsViaTrace(con, stmt, stmt2, rs, itemCodeStr, unm, uty, sid, men, den, dnm, bnm);
    // traceList = "SamLeong\001150000\001SyedAlwi\00150000\001\0"

    byte[] b = new byte[40];
    double totalStockLevel = outputStoresLevels(out, traceList, itemCodeStr, unm, uty, sid, men, den, dnm, bnm, dpOnQuantities, bytesOut);

    generalUtils.doubleToBytesCharFormat(totalStockLevel, b, 0);
    generalUtils.bytesDPsGivenSeparator(true, '\000', dpOnQuantities, b, 20, 0);
    generalUtils.formatNumeric(b, dpOnQuantities);

    scoutln(out, bytesOut, "<tr><td></td><td></td><td><hr></td></tr>");
    
    scoutln(out, bytesOut, "<tr><td nowrap><p>Total in stores: </td><td></td><td align=right><span id=\"textNumericValue\">" + generalUtils.stringFromBytes(b, 0L) + "</span></td>");

    scoutln(out, bytesOut, "<td nowrap width=99%>&nbsp;&nbsp;<a href=\"/central/servlet/ProductStockTraceGenerate?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den+ "&dnm=" + dnm + "&p1=" + itemCodeStr + "&bnm=" + bnm
                         + "\">Stock Trace</a></td></tr>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String getStockLevelsViaTrace(Connection con, Statement stmt, Statement stmt2, ResultSet rs, String itemCode, String unm, String uty, String sid, String men, String den, String dnm, String bnm) throws Exception
  { 
    return stockLevelsGenerate.fetch(con, stmt, stmt2, rs, itemCode, "", "", unm, sid, uty, men, den, dnm, bnm);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private double outputStoresLevels(PrintWriter out, String traceList, String itemCode, String unm, String uty, String sid, String men, String den, String dnm, String bnm, char dpOnQuantities, int[] bytesOut) throws Exception
  {
    int y=0, len = traceList.length();
    String thisStore, thisQty;
    double totalStockLevel = 0.0;
    boolean first = true;

    while(y < len) // just-in-case
    {
      thisStore = "";
      while(y < len && traceList.charAt(y) != '\001')
        thisStore += traceList.charAt(y++);
      ++y;

      thisQty = "";
      while(y < len && traceList.charAt(y) != '\001')
        thisQty += traceList.charAt(y++);
      ++y;

      scout(out, bytesOut, "<tr>");
      if(first)
      {
        scout(out, bytesOut, "<td nowrap><p>In store:</td>");
        first = false;
      }
      else scout(out, bytesOut, "<td></td>");

      thisQty = generalUtils.formatNumeric(thisQty, dpOnQuantities);

      scoutln(out, bytesOut, "<td nowrap><p>" + thisStore + "&nbsp; &nbsp; </td><td align=right><span id=\"textNumericValue\">" + thisQty + "</span>");
      
      scoutln(out, bytesOut, "</td><td nowrap>&nbsp;&nbsp;<a href=\"/central/servlet/ProductStockTraceGenerate?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=" + itemCode + "&p2=" + thisStore
                           + "\001&p3=Y&p4=1&p5=N&bnm=" + bnm + "\">Stock Trace</a></td></tr>");

     totalStockLevel += generalUtils.doubleFromStr(thisQty);
    }

    return totalStockLevel;  
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void onOrder(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, byte[] itemCode, char dpOnQuantities, double[] total, String[] html) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.POCode, t2.Line, t2.Quantity, t2.RequiredBy, t2.DateConfirmed, t1.Date, t1.CompanyCode, t1.CompanyName FROM pol AS t2 INNER JOIN po AS t1 ON t2.POCode = t1.POCode WHERE t2.ItemCode = '"
                         + generalUtils.stringFromBytes(itemCode, 0L) + "' AND t2.Received != 'R' AND t1.Status != 'C' AND t1.AllReceived != 'Y' ORDER BY t1.Date, t2.POCode, t2.Line");

    String poCode, poLine, requiredBy, dateConfirmed, date, supplierCode, supplierName, cssFormat="";
    byte[] b      = new byte[50];
    boolean first = true;
    double qty, actualQty;

    while(rs.next())
    {
      poCode        = rs.getString(1);
      poLine        = rs.getString(2);
      qty           = generalUtils.doubleFromStr(rs.getString(3));
      requiredBy    = rs.getString(4);
      dateConfirmed = rs.getString(5);
      date          = rs.getString(6);
      supplierCode  = rs.getString(7);
      supplierName  = rs.getString(8);

      actualQty = goodsReceivedNote.getTotalReceivedForAPOLine(con, stmt2, rs2, poCode, poLine);

      if(actualQty < qty)
      {
        if(first)
        {
          html[0] += "<tr><td>&nbsp;</td></tr>";
          html[0] += "<tr id=\"pageColumn\"><td colspan=7><p>On Order:</td></tr>";
          html[0] += "<tr id=\"pageColumn\"><td></td><td><p>Outstanding</td>";
          html[0] += "<td><p>PO Code</td>";
          html[0] += "<td><p>PO Date</td>";
          html[0] += "<td><p>Required-by Date</td>";
          html[0] += "<td><p>Confirmed Date</td>";
          html[0] += "<td colspan=2><p>Supplier</td></tr>";
          first = false;
        }

        total[0] += (qty - actualQty);
        generalUtils.doubleToBytesCharFormat((qty - actualQty), b, 0);
        generalUtils.bytesDPsGivenSeparator(true, '\000', dpOnQuantities, b, 20, 0);
        generalUtils.formatNumeric(b, dpOnQuantities);

        if(cssFormat.equals("line1")) cssFormat = "line2"; else cssFormat = "line1";

        html[0] += "<tr id=\"" + cssFormat + "\"><td></td><td nowrap align=center><p>" + generalUtils.stringFromBytes(b, 0L) + "</td>";
        html[0] += "<td nowrap><p><a href=\"javascript:viewPO('" + poCode + "')\">" + poCode + "</a></td>";
        html[0] += "<td nowrap><p>" + generalUtils.convertFromYYYYMMDD(date) + "</td>";
        html[0] += "<td nowrap><p>" + generalUtils.convertFromYYYYMMDD(requiredBy) + "</td>";
        html[0] += "<td nowrap><p>" + generalUtils.convertFromYYYYMMDD(dateConfirmed) + "</td>";
        html[0] += "<td colspan=2 nowrap><p>" + supplierCode + " (" + supplierName +")</td></tr>";
      }
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void onLocalOrder(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, byte[] itemCode, char dpOnQuantities, double[] total, String[] html) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.LPCode, t2.Line, t2.Quantity, t2.RequiredBy, t2.DateConfirmed, t1.Date, t1.CompanyCode, t1.CompanyName FROM lpl AS t2 INNER JOIN lp AS t1 ON t2.LPCode = t1.LPCode WHERE t2.ItemCode = '"
                         + generalUtils.stringFromBytes(itemCode, 0L) + "' AND t2.Received != 'R' AND t1.Status != 'C' AND t1.AllReceived != 'Y' ORDER BY t1.Date, t2.LPCode, t2.Line");

    String lpCode, lpLine, requiredBy, dateConfirmed, date, supplierCode, supplierName, cssFormat="";
    byte[] b      = new byte[50];
    boolean first = true;
    double qty, actualQty;

    while(rs.next())
    {
      lpCode        = rs.getString(1);
      lpLine        = rs.getString(2);
      qty           = generalUtils.doubleFromStr(rs.getString(3));
      requiredBy    = rs.getString(4);
      dateConfirmed = rs.getString(5);
      date          = rs.getString(6);
      supplierCode  = rs.getString(7);
      supplierName  = rs.getString(8);

      actualQty = goodsReceivedNote.getTotalReceivedForAPOLine(con, stmt2, rs2, lpCode, lpLine);

      if(actualQty < qty)
      {
        if(first)
        {
          html[0] += "<tr><td>&nbsp;</td></tr>";
          html[0] += "<tr id=\"pageColumn\"><td colspan=7><p>On Order:</td></tr>";
          html[0] += "<tr id=\"pageColumn\"><td></td><td><p>Outstanding</td>";
          html[0] += "<td><p>LP Code</td>";
          html[0] += "<td><p>LP Date</td>";
          html[0] += "<td><p>Required-by Date</td>";
          html[0] += "<td><p>Confirmed Date</td>";
          html[0] += "<td colspan=2><p>Supplier</td></tr>";
          first = false;
        }

        total[0] += (qty - actualQty);
        generalUtils.doubleToBytesCharFormat((qty - actualQty), b, 0);
        generalUtils.bytesDPsGivenSeparator(true, '\000', dpOnQuantities, b, 20, 0);
        generalUtils.formatNumeric(b, dpOnQuantities);

        if(cssFormat.equals("line1"))
          cssFormat = "line2";
        else cssFormat = "line1";

        html[0] += "<tr id=\"" + cssFormat + "\"><td></td><td nowrap align=center><p>" + generalUtils.stringFromBytes(b, 0L) + "</td>";
        html[0] += "<td nowrap><p><a href=\"javascript:viewLP('" + lpCode + "')\">" + lpCode + "</a></td>";
        html[0] += "<td nowrap><p>" + generalUtils.convertFromYYYYMMDD(date) + "</td>";
        html[0] += "<td nowrap><p>" + generalUtils.convertFromYYYYMMDD(requiredBy) + "</td>";
        html[0] += "<td nowrap><p>" + generalUtils.convertFromYYYYMMDD(dateConfirmed) + "</td>";
        html[0] += "<td colspan=2 nowrap><p>" + supplierCode + " (" + supplierName +")</td></tr>";
      }
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void onSO(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, byte[] itemCode, char dpOnQuantities, double[] total, String[] html, long startTime) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT SOCode, Line, Quantity, DeliveryDate FROM sol WHERE ItemCode = '" + generalUtils.stringFromBytes(itemCode, 0L) + "' ORDER BY SOCode, Line");

    String soCode, soLine, deliveryDate, cssFormat = "";
    double qty, actualQty;
    byte[] b = new byte[20];
    byte[] b2 = new byte[20];
    boolean first = true;
    String[] date = new String[1];
    String[] customerCode = new String[1];
    String[] customerName = new String[1];
    String[] customerPOCode = new String[1];

    while(rs.next())
    {
      soCode = rs.getString(1);

      if(! soHasCashInvoice(con, stmt2, rs2, itemCode, soCode))
      {
        soLine         = rs.getString(2);
        qty            = generalUtils.doubleFromStr(rs.getString(3));
        deliveryDate   = rs.getString(4);

        if(getSOData(con, stmt2, rs2, soCode, date, customerCode, customerName, customerPOCode))
        {
          actualQty = deliveryOrder.getTotalDeliveredForASOLine(con, stmt2, rs2, soCode, soLine);

          if(actualQty < qty)
          {
            if(first)
            {
              html[0] += "<tr><td><p>&nbsp;</td></tr>";
              html[0] += "<tr id=\"pageColumn\"><td colspan=7><p>Total SO Requirements:</td></tr>";
              html[0] += "<tr id=\"pageColumn\"><td><p>Ordered</td><td><p>Outstanding</td>";
              html[0] += "<td nowrap><p>SO Code</td>";
              html[0] += "<td nowrap><p>SO Date</td>";
              html[0] += "<td nowrap><p>Customer PO Code</td>";
              html[0] += "<td nowrap><p>Delivery Date</td>";
              html[0] += "<td nowrap><p>Customer</td></tr>";
              first = false;
            }

            total[0] += (qty - actualQty);

            generalUtils.doubleToBytesCharFormat((qty - actualQty), b, 0);
            generalUtils.bytesDPsGivenSeparator(true, '\000', dpOnQuantities, b, 20, 0);
            generalUtils.formatNumeric(b, dpOnQuantities);

            generalUtils.doubleToBytesCharFormat(qty, b2, 0);
            generalUtils.bytesDPsGivenSeparator(true, '\000', dpOnQuantities, b2, 20, 0);
            generalUtils.formatNumeric(b2, dpOnQuantities);

            if(cssFormat.equals("line1")) cssFormat = "line2"; else cssFormat = "line1";

            html[0] += "<tr id=\"" + cssFormat + "\"><td nowrap align=center><p>" + generalUtils.stringFromBytes(b2, 0L) + "</td><td nowrap align=center><p>" + generalUtils.stringFromBytes(b, 0L) + "</td>";
            html[0] += "<td nowrap><p><a href=\"javascript:viewSO('" + soCode + "')\">" + soCode + "</a></td>";
            html[0] += "<td nowrap><p>" + generalUtils.convertFromYYYYMMDD(date[0]) + "</td>";
            html[0] += "<td nowrap><p>" + customerPOCode[0] + "</td>";
            html[0] += "<td nowrap><p>" + generalUtils.convertFromYYYYMMDD(deliveryDate) + "</td>";
            html[0] += "<td nowrap><p>" + customerCode[0] + " (" + customerName[0] +")</td></tr>";
          }
        }
      }
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean getSOData(Connection con, Statement stmt, ResultSet rs, String soCode, String[] date, String[] customerCode, String[] customerName, String[] customerPOCode) throws Exception
  {
    boolean found = false;

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT Date, CompanyCode, CompanyName, CustomerPOCode FROM so WHERE SOCode = '" + soCode + "' AND Status != 'C' AND AllSupplied != 'Y'");

    if(rs.next())
    {
      date[0]           = rs.getString(1);
      customerCode[0]   = rs.getString(2);
      customerName[0]   = rs.getString(3);
      customerPOCode[0] = rs.getString(4);

      found = true;
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    return found;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void onPLNotCompleted(Connection con, Statement stmt, ResultSet rs, byte[] itemCode, String dnm, String localDefnsDir, String defnsDir, char dpOnQuantities, double[] total, String[] html, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT t2.PLCode, t2.QuantityRequired, t2.QuantityPacked, t1.Date, t1.CompanyCode, t1.CompanyName "
                         + " FROM pll AS t2 INNER JOIN pl AS t1 ON t2.PLCode = t1.PLCode"
                         + " WHERE t2.ItemCode = '" + generalUtils.stringFromBytes(itemCode, 0L) + "' AND t1.Status != 'C' AND t1.Completed != 'Y' ORDER BY t1.Date, t2.PLCode, t2.Line");

    String plCode, qtyRequired, qtyPicked, date, customerCode, customerName, cssFormat="";
    boolean first = true;

    while(rs.next())
    {    
      plCode       = rs.getString(1);
      qtyRequired  = rs.getString(2);
      qtyPicked    = rs.getString(3);
      date         = rs.getString(4);
      customerCode = rs.getString(5);
      customerName = rs.getString(6);

      if(first)
      {
        html[0] += "<tr><td>&nbsp;</td></tr>";
        html[0] += "<tr id=\"pageColumn\"><td colspan=7><p>Not Completed Picking Lists:</td></tr>";
        html[0] += "<tr id=\"pageColumn\"><td><p>Required</td><td><p>Packed</td>";
        html[0] += "<td nowrap><p>PL Code</td>";
        html[0] += "<td nowrap><p>Date</td>";
        html[0] += "<td colspan=3 nowrap><p>Customer</td></tr>";
        first = false;
      }

      qtyRequired = generalUtils.formatNumeric(qtyRequired, dpOnQuantities);
      qtyPicked   = generalUtils.formatNumeric(qtyPicked,   dpOnQuantities);

      total[0] += generalUtils.doubleFromStr(qtyPicked);

      if(cssFormat.equals("line1")) cssFormat = "line2"; else cssFormat = "line1";

      html[0] += "<tr id=\"" + cssFormat + "\"><td nowrap align=center><p>" + qtyRequired + "</td><td nowrap align=center><p>" + qtyPicked + "</td>";
      html[0] += "<td nowrap><p><a href=\"javascript:viewPL('" + plCode + "')\">" + plCode + "</a></td>";
      html[0] += "<td nowrap><p>" + generalUtils.convertFromYYYYMMDD(date) + "</td>";
      html[0] += "<td colspan=3 nowrap><p>" + customerCode + " (" + customerName +")</td></tr>";
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void onGRNInTransit(Connection con, Statement stmt, ResultSet rs, byte[] itemCode, String dnm, String localDefnsDir, String defnsDir,
                              char dpOnQuantities, double[] total, String[] html, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT t2.GRCode, t2.Quantity, t1.Date, t1.CompanyCode, t1.CompanyName, t1.Notes, t2.Remark FROM grl AS t2 INNER JOIN gr AS t1 ON t2.GRCode = t1.GRCode WHERE t2.ItemCode = '" + generalUtils.stringFromBytes(itemCode, 0L)
                         + "' AND t1.Status != 'C' AND t1.StockProcessed != 'Y' ORDER BY t1.Date, t2.GRCode, t2.Line");

    String grCode, qty, date, supplierCode, supplierName, cssFormat="", notes, remark;
    boolean first = true;

    while(rs.next())
    {    
      grCode       = rs.getString(1);
      qty          = rs.getString(2);
      date         = rs.getString(3);
      supplierCode = rs.getString(4);
      supplierName = rs.getString(5);
      notes        = generalUtils.deNull(rs.getString(6));
      remark       = generalUtils.deNull(rs.getString(7));

      if(first)
      {
        html[0] += "<tr><td>&nbsp;</td></tr>";
        html[0] += "<tr id=\"pageColumn\"><td colspan=7><p>In-Transit Goods Received Notes:</td></tr>";
        html[0] += "<tr id=\"pageColumn\"><td></td><td><p>Quantity</td>";
        html[0] += "<td nowrap><p>GRN Code</td>";
        html[0] += "<td nowrap><p>Date</td>";
        html[0] += "<td colspan=1 nowrap><p>Supplier</td>";
        html[0] += "<td colspan=2 nowrap><p>Notes/Remark</td>";
        html[0] += "</tr>";
        first = false;
      }

      qty = generalUtils.formatNumeric(qty, dpOnQuantities);

      total[0] += generalUtils.doubleFromStr(qty);

      if(cssFormat.equals("line1"))
        cssFormat = "line2";
      else cssFormat = "line1";

      html[0] += "<tr id=\"" + cssFormat + "\"><td></td><td nowrap align=center><p>" + qty + "</td>";
      html[0] += "<td nowrap><p><a href=\"javascript:viewGR('" + grCode + "')\">" + grCode + "</a></td>";
      html[0] += "<td nowrap><p>" + generalUtils.convertFromYYYYMMDD(date) + "</td>";
      html[0] += "<td nowrap><p>" + supplierCode + " (" + supplierName +")</td>";
      html[0] += "<td colspan=2><p>" + notes + " " + remark + "</td>";
      html[0] += "</tr>";
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void scout(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.print(str);
    bytesOut[0] += str.length();    
  }
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void onPO(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String itemCode, char dpOnQuantities, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    stmt.setMaxRows(5);

    rs = stmt.executeQuery("SELECT t2.POCode, t2.Line, t2.Quantity, t2.UnitPrice, t1.Date, t1.CompanyCode, t1.CompanyName FROM pol AS t2 INNER JOIN po AS t1 ON t2.POCode = t1.POCode WHERE t2.ItemCode = '" + generalUtils.sanitiseForSQL(itemCode)
                         + "' AND t1.Status != 'C' ORDER BY t1.Date DESC");

    String poCode, poLine, date, supplierCode, supplierName, cssFormat="";
    boolean first = true;
    double qty, unitPrice;
    byte[] b  = new byte[50];
    byte[] b2 = new byte[50];

    while(rs.next())
    {
      poCode       = rs.getString(1);
      poLine       = rs.getString(2);
      qty          = generalUtils.doubleFromStr(rs.getString(3));
      unitPrice    = generalUtils.doubleFromStr(rs.getString(4));
      date         = rs.getString(5);
      supplierCode = rs.getString(6);
      supplierName = rs.getString(7);

      if(first)
      {
        scoutln(out, bytesOut, "<tr id=\"pageColumn\">");
        scoutln(out, bytesOut, "<td><p>PO Code</td>");
        scoutln(out, bytesOut, "<td><p>PO Line</td>");
        scoutln(out, bytesOut, "<td><p>PO Date</td>");
        scoutln(out, bytesOut, "<td align='center'><p>Quantity</td>");
        scoutln(out, bytesOut, "<td align='center'><p>Unit Price</td>");
        scoutln(out, bytesOut, "<td><p>Supplier</td></tr>");
        first = false;
      }

      generalUtils.doubleToBytesCharFormat(qty, b, 0);
      generalUtils.bytesDPsGivenSeparator(true, '\000', dpOnQuantities, b, 20, 0);
      generalUtils.formatNumeric(b, dpOnQuantities);

      generalUtils.doubleToBytesCharFormat(unitPrice, b2, 0);
      generalUtils.bytesDPsGivenSeparator(true, '\000', '2', b2, 20, 0);
      generalUtils.formatNumeric(b2, '2');

      if(cssFormat.equals("line1")) cssFormat = "line2"; else cssFormat = "line1";

      scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\">");
      scoutln(out, bytesOut, "<td nowrap><p><a href=\"javascript:viewPO('" + poCode + "')\">" + poCode + "</a></td>");
      scoutln(out, bytesOut, "<td nowrap><p>" + poLine + "</td>");
      scoutln(out, bytesOut, "<td nowrap><p>" + generalUtils.convertFromYYYYMMDD(date) + "</td>");
      scoutln(out, bytesOut, "<td nowrap align=center><p>" + generalUtils.stringFromBytes(b, 0L) + "</td>");
      scoutln(out, bytesOut, "<td nowrap align=center><p>" + generalUtils.stringFromBytes(b2, 0L) + "</td>");
      scoutln(out, bytesOut, "<td nowrap><p>" + supplierCode + " (" + supplierName +")</td>");
      scoutln(out, bytesOut, "</tr>");
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void onLP(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String itemCode, char dpOnQuantities, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    stmt.setMaxRows(5);

    rs = stmt.executeQuery("SELECT t2.LPCode, t2.Line, t2.Quantity, t2.UnitPrice, t1.Date, t1.CompanyCode, t1.CompanyName FROM lpl AS t2 INNER JOIN lp AS t1 ON t2.LPCode = t1.LPCode WHERE t2.ItemCode = '" + generalUtils.sanitiseForSQL(itemCode)
                         + "' AND t1.Status != 'C' ORDER BY t1.Date DESC");

    String lpCode, lpLine, date, supplierCode, supplierName, cssFormat="";
    boolean first = true;
    double qty, unitPrice;
    byte[] b = new byte[50];
    byte[] b2 = new byte[50];

    while(rs.next())
    {
      lpCode       = rs.getString(1);
      lpLine       = rs.getString(2);
      qty          = generalUtils.doubleFromStr(rs.getString(3));
      unitPrice    = generalUtils.doubleFromStr(rs.getString(4));
      date         = rs.getString(5);
      supplierCode = rs.getString(6);
      supplierName = rs.getString(7);

      if(first)
      {
        scoutln(out, bytesOut, "<tr id=\"pageColumn\">");
        scoutln(out, bytesOut, "<td><p>LO Code</td>");
        scoutln(out, bytesOut, "<td><p>LP Line</td>");
        scoutln(out, bytesOut, "<td><p>LP Date</td>");
        scoutln(out, bytesOut, "<td align='center'><p>Quantity</td>");
        scoutln(out, bytesOut, "<td align='center'><p>Unit Price</td>");
        scoutln(out, bytesOut, "<td><p>Supplier</td></tr>");
        first = false;
      }

      generalUtils.doubleToBytesCharFormat(qty, b, 0);
      generalUtils.bytesDPsGivenSeparator(true, '\000', dpOnQuantities, b, 20, 0);
      generalUtils.formatNumeric(b, dpOnQuantities);

      generalUtils.doubleToBytesCharFormat(unitPrice, b2, 0);
      generalUtils.bytesDPsGivenSeparator(true, '\000', '2', b2, 20, 0);
      generalUtils.formatNumeric(b2, '2');

      if(cssFormat.equals("line1")) cssFormat = "line2"; else cssFormat = "line1";

      scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\">");
      scoutln(out, bytesOut, "<td nowrap><p><a href=\"javascript:viewLP('" + lpCode + "')\">" + lpCode + "</a></td>");
      scoutln(out, bytesOut, "<td nowrap><p>" + lpLine + "</td>");
      scoutln(out, bytesOut, "<td nowrap><p>" + generalUtils.convertFromYYYYMMDD(date) + "</td>");
      scoutln(out, bytesOut, "<td nowrap align=center><p>" + generalUtils.stringFromBytes(b, 0L) + "</td>");
      scoutln(out, bytesOut, "<td nowrap align=center><p>" + generalUtils.stringFromBytes(b2, 0L) + "</td>");
      scoutln(out, bytesOut, "<td nowrap><p>" + supplierCode + " (" + supplierName +")</td>");
      scoutln(out, bytesOut, "</tr>");
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean soHasCashInvoice(Connection con, Statement stmt, ResultSet rs, byte[] itemCode, String soCode) throws Exception
  {
    boolean isCash = false;

    if(soCode.length() > 0)
    {
      try
      {
        stmt = con.createStatement();

        rs = stmt.executeQuery("SELECT t1.CashOrAccount FROM invoicel AS t2 INNER JOIN invoice AS t1 ON t1.InvoiceCode = t2.InvoiceCode "
                             + "WHERE t2.ItemCode = '" + generalUtils.stringFromBytes(itemCode, 0L) + "' AND t2.SOCode = '" + soCode + "' AND t1.Status != 'C");

        if(rs.next())
        {
          if(rs.getString(1).equals("C"))
            isCash = true;
        }

        if(rs   != null) rs.close();
        if(stmt != null) stmt.close();
      }
      catch(Exception e) { }
    }

    return isCash;
  }

}
