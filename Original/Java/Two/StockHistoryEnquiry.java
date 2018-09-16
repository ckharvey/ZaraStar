// =======================================================================================================================================================================================================
// System: ZaraStar: Analytic: Stock History Enquiry - Search
// Module: StockHistoryEnquiry.java
// Author: C.K.Harvey
// Copyright (c) 2000-08 Christopher Harvey. All Rights Reserved.
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

public class StockHistoryEnquiry extends HttpServlet
{
  GeneralUtils  generalUtils = new GeneralUtils();
  MessagePage  messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils  pageFrameUtils = new PageFrameUtils();
  DirectoryUtils  directoryUtils = new DirectoryUtils();
  Inventory  inventory = new Inventory();
  MiscDefinitions  miscDefinitions = new MiscDefinitions();
  ReportGeneration  reportGeneration = new ReportGeneration();
  GoodsReceivedNote  goodsReceivedNote = new GoodsReceivedNote();
  PickingList  pickingList = new PickingList();
  PurchaseOrder  purchaseOrder = new PurchaseOrder();
  SalesOrder salesOrder = new SalesOrder();
  StockLevelsGenerate stockLevelsGenerate = new StockLevelsGenerate();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="", p3="", p4="", p5="", p6="";

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
      p2  = req.getParameter("p2"); // checkBoxes
      p3  = req.getParameter("p3"); // dateFrom
      p4  = req.getParameter("p4"); // dateTo
      p5  = req.getParameter("p5"); // mfr
      p6  = req.getParameter("p6"); // mfrCode
      
      if(p1 == null) p1 = "";
      if(p5 == null) p5 = "";
      if(p6 == null) p6 = "";

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, p3, p4, p5, p6, bytesOut);
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

      System.out.println("1002a: " + e);
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "MainPageUtils2a", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1002, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                    String p1, String p2, String p3, String p4, String p5, String p6, int[] bytesOut) throws Exception
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

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 1002, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "MainPageUtils2", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1002, bytesOut[0], 0, "ACC:" + p1);
      if(con != null) con.close();  
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "MainPageUtils2", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1002, bytesOut[0], 0, "SID:" + p1);
      if(con != null) con.close();  
      if(out != null) out.flush(); 
      return;
    }

    p1 = p1.toUpperCase();
    byte[] itemCode = new byte[21];
    generalUtils.strToBytes(itemCode, p1);

    if(p3 == null || p3.length() == 0) // dateFrom
      p3 = "1970-01-01";
    else p3 = generalUtils.convertDateToSQLFormat(p3);
    
    if(p4 == null || p4.length() == 0) // dateTo
      p4 = "2099-12-31";
    else p4 = generalUtils.convertDateToSQLFormat(p4);
        
    String itemCodeStr = generalUtils.stringFromBytes(itemCode, 0L);

    if(p6.length() > 0)
    {
      itemCodeStr = inventory.getItemCodeGivenMfrAndMfrCode(con, stmt, rs, p5, p6);

      if(itemCodeStr.length() == 0)
      {
        scoutln(out, bytesOut, "<br><br><p>Manufacturer: " + p5 + ", and Manufacturer Code: " + p6 + " Not Found<br><br>");
        scoutln(out, bytesOut, "</table></form>");
        scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir));
        if(con != null) con.close();  
        return;
      }
      
      generalUtils.strToBytes(itemCode, itemCodeStr);
      p1 = itemCodeStr;
    }
        
    set(con, stmt, stmt2, rs, rs2, out, req, itemCode, p1, p2, p3, p4, unm, sid, uty, men, den, dnm, bnm, imagesDir, localDefnsDir, defnsDir,
        bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 1002, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con != null) con.close();  
    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, HttpServletRequest req,
                   byte[] itemCodeB, String itemCode, String grOptions, String dateFrom, String dateTo, String unm, String sid, String uty,
                   String men, String den, String dnm, String bnm, String imagesDir, String localDefnsDir, String defnsDir, int[] bytesOut)
                   throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Stock History Enquiry</title>");

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
      scoutln(out, bytesOut, "function viewSO(code){");
      scoutln(out, bytesOut, "var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/SalesOrderPage?unm=" + unm + "&sid=" + sid + "&uty="
                           + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 149, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewInvoice(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/SalesInvoicePage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 142, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewQuote(code){");
      scoutln(out, bytesOut, "var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/QuotationPage?unm=" + unm + "&sid=" + sid + "&uty="
                           + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 167, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewAdj(code){");
      scoutln(out, bytesOut, "var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/StockAdjustmentItem?unm=" + unm + "&sid=" + sid + "&uty="
                           + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + itemCode + "\";}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 166, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewChk(code){");
      scoutln(out, bytesOut, "var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/StockCheckForItem?unm=" + unm + "&sid=" + sid + "&uty="
                           + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + itemCode + "\";}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 147, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewPL(code){");
      scoutln(out, bytesOut, "var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/GoodsReceivedPickingList?unm=" + unm + "&sid=" + sid + "&uty="
                          + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 148, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewDO(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/DeliveryOrderPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 153, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewGRN(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/GoodsReceivedPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3001, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewItem(code){");
      scoutln(out, bytesOut, "var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/ProductStockRecord?unm=" + unm + "&sid=" + sid + "&uty="
                           + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p1=\"+p1;}");
    }

    boolean ProductCart = authenticationUtils.verifyAccess(con, stmt, rs, req, 121, unm, uty, dnm, localDefnsDir, defnsDir);
    if(ProductCart)
    {
      scoutln(out, bytesOut, "function addToCart(code){var p1=sanitise(code);");
      if(!uty.equals("I"))scoutln(out, bytesOut, "alert('Coming Soon');else ");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/ProductCartAddToCart?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den
                           + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=L&p1=\"+p1;}");
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
  
    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" 
                           + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

  int[] hmenuCount = new int[1];
  pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "1002", "", "MainPageUtils2", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Stock History Enquiry Results", "1002", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<form><table id=\"page\" width=100% border=0>");

    byte[] b      = new byte[100];
    byte[] remark = new byte[100];
    byte[] data   = new byte[5000];

    if(itemCodeB[0] == '\000')
    {
      scoutln(out, bytesOut, "<br><br><span id=\"enquiryMessage\">&nbsp; No Item Code Specified</span><br><br>");
    }
    else
    if(inventory.getStockRecGivenCode(con, stmt, rs, itemCodeB, '\000', data) == -1) // rec not found
    {
      scoutln(out, bytesOut, "<br><br><span id=\"enquiryMessage\">Item Code &nbsp; &nbsp; " + itemCode);
      scoutln(out, bytesOut, "&nbsp; &nbsp; <font color=\"#FF0000\">Not Found</span><br><br>");
    }
    else // found
    {
      generalUtils.dfs(data, (short)1, b);
      generalUtils.dfs(data, (short)2, remark);
      String mfr     = generalUtils.dfsAsStr(data, (short)3);
      String mfrCode = generalUtils.dfsAsStr(data, (short)4);

      scoutln(out, bytesOut, "<p>For Item Code: &nbsp; &nbsp; ");
      scoutln(out, bytesOut, "<a href=\"javascript:viewItem('" + itemCode + "')\">" + itemCode + "</a>");
      scoutln(out, bytesOut, "&nbsp; &nbsp; <b>" + generalUtils.stringFromBytes(b, 0L) + " " + generalUtils.stringFromBytes(remark, 0L) + "</b>");

      scoutln(out, bytesOut, "<p>Manufacturer: &nbsp; &nbsp; " + mfr + ": &nbsp; &nbsp; " + mfrCode);

      scoutln(out, bytesOut, "<p>Pricing: &nbsp; &nbsp; ");

      generalUtils.dfs(data, (short)25, b);
      generalUtils.bytesDPsGivenSeparator(true, '\000', '2', b, 20, 0);
      scoutln(out, bytesOut, " &nbsp; &nbsp; List: <span id=\"textNumericValue\">" + generalUtils.stringFromBytes(b, 0L) + "</span>");

      generalUtils.dfs(data, (short)20, b);
      generalUtils.bytesDPsGivenSeparator(true, '\000', '2', b, 20, 0);
      scoutln(out, bytesOut, " &nbsp; &nbsp; 1: <span id=\"textNumericValue\">" + generalUtils.stringFromBytes(b, 0L) + "</span>");

      generalUtils.dfs(data, (short)21, b);
      generalUtils.bytesDPsGivenSeparator(true, '\000', '2', b, 20, 0);
      scoutln(out, bytesOut, " &nbsp; &nbsp; 2: <span id=\"textNumericValue\">" + generalUtils.stringFromBytes(b, 0L) + "</span>");

      generalUtils.dfs(data, (short)22, b);
      generalUtils.bytesDPsGivenSeparator(true, '\000', '2', b, 20, 0);
      scoutln(out, bytesOut, " &nbsp; &nbsp; 3: <span id=\"textNumericValue\">" + generalUtils.stringFromBytes(b, 0L) + "</span>");

      generalUtils.dfs(data, (short)23, b);
      generalUtils.bytesDPsGivenSeparator(true, '\000', '2', b, 20, 0);
      scoutln(out, bytesOut, " &nbsp; &nbsp; 4: <span id=\"textNumericValue\"><font size=1>" + generalUtils.stringFromBytes(b, 0L) + "</font></span>");

      char dpOnQuantities = miscDefinitions.dpOnQuantities(con, stmt, rs, "5");

      scoutln(out, bytesOut, "</td></tr></table><table id=\"page\" width=100% border=0>");
      stockLevels(con, stmt, stmt2, rs, out, itemCodeB, itemCode, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, dpOnQuantities,
                  bytesOut);

      if(ProductCart)
      {
        scoutln(out, bytesOut, "<tr><td><a href=\"javascript:addToCart('" + itemCode + "')\"><img border=0 src=\"" + imagesDir 
                             + "toCart.png\"></a></td></tr>");
      }


      short[] set = new short[1];

      short numItems = reportGeneration.countGRs(grOptions, set);

      if(set[0] > 0) // at least one set
      {
        String option;
        for(short x=0;x<numItems;++x)
        {
          if(reportGeneration.isGRSet(grOptions, x))
          {
            option = reportGeneration.getGRItem(grOptions, x);
            switch(option.charAt(0)) // defined in o102
            {
              case 'P' : // Picking Lists
                         onPL(con, stmt, rs, out, itemCode, dateFrom, dateTo, dpOnQuantities, dnm, localDefnsDir, defnsDir, bytesOut);
                         break;
              case 'G' : // Goods Received Notes
                         onGRN(con, stmt, rs, out, itemCode, dateFrom, dateTo, dpOnQuantities, localDefnsDir, defnsDir, bytesOut);
                         break;
              case 'A' : // Adjustments
                         onAdjustment(con, stmt, rs, out, itemCode, dateFrom, dateTo, dpOnQuantities, dnm, localDefnsDir, defnsDir, 
                                      bytesOut);
                         break;
              case 'C' : // Stock Check
                         onStockCheck(con, stmt, rs, out, itemCode, dateFrom, dateTo, dpOnQuantities, dnm, localDefnsDir, defnsDir, bytesOut);
                         break;
              case 'S' : // Sales Orders
                         onSO(con, stmt, stmt2, rs, rs2, out, itemCode, dateFrom, dateTo, dpOnQuantities, dnm, localDefnsDir, defnsDir, bytesOut);
                         break;
              case 'Y' : // Purchase Orders
                         onPO(con, stmt, stmt2, rs, rs2, out, itemCode, dateFrom, dateTo, dpOnQuantities, dnm, localDefnsDir, defnsDir, bytesOut);
                         break;
              case 'L' : // Local Purchase Requistions
                         onLP(con, stmt, stmt2, rs, rs2, out, itemCode, dateFrom, dateTo, dpOnQuantities, dnm, localDefnsDir, defnsDir, bytesOut);
                         break;
              case 'D' : // Delivery Orders
                         onDO(con, stmt, rs, out, itemCode, dateFrom, dateTo, dpOnQuantities, dnm, localDefnsDir, defnsDir, bytesOut);
                         break;
              case 'I' : // Invoices
                         onInvoice(con, stmt, rs, out, itemCode, dateFrom, dateTo, dpOnQuantities, dnm, localDefnsDir, defnsDir, bytesOut);
                         break;
              case 'Q' : // Quotes
                         onQuotes(con, stmt, rs, out, itemCode, dateFrom, dateTo, dpOnQuantities, dnm, localDefnsDir, defnsDir, bytesOut);
                         break;
            }
          }
        }
      }
      
    }

    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir));
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void stockLevels(Connection con, Statement stmt, Statement stmt2, ResultSet rs, PrintWriter out, byte[] itemCode, String itemCodeStr,
                          String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir,
                          String defnsDir, char dpOnQuantities, int[] bytesOut) throws Exception
  {
    // traceList = "SamLeong\001150000\001SyedAlwi\00150000\001\0"
    String traceList = getStockLevelsViaTrace(con, stmt, stmt2, rs, itemCodeStr, unm, uty, sid, men, den, dnm, bnm, localDefnsDir, defnsDir);

    byte[] b = new byte[40];
    
    double totalStockLevel = outputStoresLevels(out, traceList, itemCodeStr, unm, uty, sid, men, den, dnm, bnm, dpOnQuantities, localDefnsDir,
                                                defnsDir, bytesOut);

    generalUtils.doubleToBytesCharFormat(totalStockLevel, b, 0);
    generalUtils.bytesDPsGivenSeparator(true, '\000', dpOnQuantities, b, 20, 0);
    generalUtils.formatNumeric(b, dpOnQuantities);

    scoutln(out, bytesOut, "<tr><td></td><td></td><td><hr></td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap><p>Total in stores: </td><td></td><td align=right><span id=\"textNumericValue\">"
                           + generalUtils.stringFromBytes(b, 0L) + "</span></td>");

    scoutln(out, bytesOut, "<td nowrap width=99%>&nbsp;&nbsp;<a href=\"/central/servlet/ProductStockTraceGenerate?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men="
                           + men + "&den=" + den+ "&dnm=" + dnm + "&p1=" + itemCodeStr + "&bnm=" + bnm + "\">Stock Trace</a></td></tr>");
}

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private String getStockLevelsViaTrace(Connection con, Statement stmt, Statement stmt2, ResultSet rs, String itemCode, String unm, String uty,
                                        String sid, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir)
                                        throws Exception
  { 
    return stockLevelsGenerate.fetch(con, stmt, stmt2, rs, itemCode, "", "", unm, sid, uty, men, den, dnm, bnm);
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private double outputStoresLevels(PrintWriter out, String traceList, String itemCode, String unm, String uty, String sid, String men, String den,
                                    String dnm, String bnm, char dpOnQuantities, String localDefnsDir, String defnsDir, int[] bytesOut)
                                    throws Exception
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

      scoutln(out, bytesOut, "</td><td nowrap>&nbsp;&nbsp;<a href=\"/central/servlet/ProductStockTraceGenerate?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men="
                             + men + "&den=" + den + "&dnm=" + dnm + "&p1=" + itemCode + "&p2=" + thisStore + "\001&p3=Y&p4=1&p5=N&bnm=" + bnm
                             + "\">Stock Trace</a></td></tr>");

      totalStockLevel += generalUtils.doubleFromStr(thisQty);
    }

    return totalStockLevel;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void onPO(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, String itemCode, String dateFrom,
                    String dateTo, char dpOnQuantities, String dnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.POCode, t2.Line, t2.Quantity, t2.RequiredBy, t2.DateConfirmed, "
                                   + "t1.Date, t1.CompanyCode, t1.CompanyName, t1.AllReceived FROM pol AS t2 INNER JOIN po AS t1 "
                                   + "ON t2.POCode = t1.POCode WHERE t2.ItemCode = '" + generalUtils.sanitiseForSQL(itemCode)
                                   + "' AND t1.Status != 'C' AND t1.Date >= {d '" + dateFrom + "'} AND t1.Date <= {d '" + dateTo
                                   + "'} ORDER BY t1.Date, t2.POCode, t2.Line");

    String poCode, poLine, date, supplierCode, supplierName, outstanding, grnCodes, dateConfirmed, requiredBy, allReceived, cssFormat="";
    boolean first = true;
    double qty, actualQty;
    byte[] b = new byte[50];

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
      allReceived   = rs.getString(9);

      actualQty = goodsReceivedNote.getTotalReceivedForAPOLine(con, stmt2, rs2, poCode, poLine);

      if(first)
      {
        scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
        scoutln(out, bytesOut, "<tr id=\"pageColumn\"><td nowrap colspan=8><p>Purchase Orders:</td></tr>");
        scoutln(out, bytesOut, "<tr id=\"pageColumn\">");
        scoutln(out, bytesOut, "<td><p>Outstanding</td>");
        scoutln(out, bytesOut, "<td><p>Quantity-Ordered</td>");
        scoutln(out, bytesOut, "<td><p>PO Code</td>");
        scoutln(out, bytesOut, "<td><p>PO Date</td>");
        scoutln(out, bytesOut, "<td><p>Required-by Date</td>");
        scoutln(out, bytesOut, "<td><p>Confirmed Date</td>");
        scoutln(out, bytesOut, "<td><p>Supplier</td>");
        scoutln(out, bytesOut, "<td><p>GRN Code</td></tr>");
        first = false;
      }

      if((qty - actualQty) != 0)
      {
        if(allReceived.equals("Y")) // all received
          outstanding = "";
        else
        {
          generalUtils.doubleToBytesCharFormat((qty - actualQty), b, 0);
          generalUtils.bytesDPsGivenSeparator(true, '\000', dpOnQuantities, b, 20, 0);
          generalUtils.formatNumeric(b, dpOnQuantities);
          outstanding = generalUtils.stringFromBytes(b, 0L);
        }
      }
      else outstanding = "";

      generalUtils.doubleToBytesCharFormat(qty, b, 0);
      generalUtils.bytesDPsGivenSeparator(true, '\000', dpOnQuantities, b, 20, 0);
      generalUtils.formatNumeric(b, dpOnQuantities);

      grnCodes = goodsReceivedNote.getGRNCodesForAPOLine(con, stmt2, rs2, poCode, poLine, dnm, localDefnsDir, defnsDir);

      if(cssFormat.equals("line1"))
        cssFormat = "line2";
      else cssFormat = "line1";

      scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\"><td align=center><p>" + outstanding + "</td>");
      scoutln(out, bytesOut, "<td nowrap align=center><p>" + generalUtils.stringFromBytes(b, 0L) + "</td>");
      scoutln(out, bytesOut, "<td nowrap><p><a href=\"javascript:viewPO('" + poCode + "')\">" + poCode + "</a></td>");
      scoutln(out, bytesOut, "<td nowrap><p>" + generalUtils.convertFromYYYYMMDD(date) + "</td>");
      scoutln(out, bytesOut, "<td nowrap><p>" + generalUtils.convertFromYYYYMMDD(requiredBy) + "</td>");
      scoutln(out, bytesOut, "<td nowrap><p>" + generalUtils.convertFromYYYYMMDD(dateConfirmed) + "</td>");
      scoutln(out, bytesOut, "<td nowrap><p>" + supplierCode + " (" + supplierName +")</td>");
      scout(out, bytesOut, "<td nowrap><p>");
      outputGRNCodes(out, grnCodes, bytesOut);
      scoutln(out, bytesOut, "</td></tr>");
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void onLP(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, String itemCode, String dateFrom,
                    String dateTo, char dpOnQuantities, String dnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT t2.LPCode, t2.Line, t2.Quantity, t1.Date, t1.CompanyCode, t1.CompanyName, "
                         + "t1.AllReceived FROM lpl AS t2 INNER JOIN lp AS t1 "
                         + "ON t2.LPCode = t1.LPCode WHERE t2.ItemCode = '" + generalUtils.sanitiseForSQL(itemCode)
                         + "' AND t1.Status != 'C' AND t1.Date >= {d '" + dateFrom + "'} AND t1.Date <= {d '" + dateTo
                         + "'} ORDER BY t1.Date, t2.LPCode, t2.Line");

    String lpCode, lpLine, date, supplierCode, supplierName, outstanding, grnCodes, allReceived, cssFormat="";
    boolean first = true;
    double qty, actualQty;
    byte[] b = new byte[50];

    while(rs.next())
    {    
      lpCode        = rs.getString(1);
      lpLine        = rs.getString(2);
      qty           = generalUtils.doubleFromStr(rs.getString(3));
      date          = rs.getString(4);
      supplierCode  = rs.getString(5);
      supplierName  = rs.getString(6);
      allReceived   = rs.getString(7);

      actualQty = goodsReceivedNote.getTotalReceivedForAPOLine(con, stmt2, rs2, lpCode, lpLine);

      if(first)
      {
        scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
        scoutln(out, bytesOut, "<tr id=\"pageColumn\"><td nowrap colspan=8><p>Local Purchase Requisitions:</td></tr>");
        scoutln(out, bytesOut, "<tr id=\"pageColumn\">");
        scoutln(out, bytesOut, "<td><p>Outstanding</td>");
        scoutln(out, bytesOut, "<td><p>LP Code</td>");
        scoutln(out, bytesOut, "<td><p>LP Date</td>");
        scoutln(out, bytesOut, "<td>Supplier</td></tr>");
        first = false;
      }

      if((qty - actualQty) != 0)
      {
        if(allReceived.equals("Y")) // all received
          outstanding = "";
        else
        {
          generalUtils.doubleToBytesCharFormat((qty - actualQty), b, 0);
          generalUtils.bytesDPsGivenSeparator(true, '\000', dpOnQuantities, b, 20, 0);
          generalUtils.formatNumeric(b, dpOnQuantities);
          outstanding = generalUtils.stringFromBytes(b, 0L);
        }
      }
      else outstanding = "";

      generalUtils.doubleToBytesCharFormat(qty, b, 0);
      generalUtils.bytesDPsGivenSeparator(true, '\000', dpOnQuantities, b, 20, 0);
      generalUtils.formatNumeric(b, dpOnQuantities);

      grnCodes = goodsReceivedNote.getGRNCodesForAPOLine(con, stmt2, rs2, lpCode, lpLine, dnm, localDefnsDir, defnsDir);

      if(cssFormat.equals("line1"))
        cssFormat = "line2";
      else cssFormat = "line1";

      scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\"><td align=center>" + outstanding + "</td>");
      scoutln(out, bytesOut, "<td nowrap align=center>" + generalUtils.stringFromBytes(b, 0L) + "</td>");
      scoutln(out, bytesOut, "<td nowrap><a href=\"javascript:viewLP('" + lpCode + "')\">" + lpCode + "</a></td>");
      scoutln(out, bytesOut, "<td nowrap>" + generalUtils.convertFromYYYYMMDD(date) + "</td>");
      scoutln(out, bytesOut, "<td nowrap>" + supplierCode + " (" + supplierName +")</td>");
      scout(out, bytesOut, "<td nowrap><p>");
      outputGRNCodes(out, grnCodes, bytesOut);
      scoutln(out, bytesOut, "</td></tr>");
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void onSO(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, String itemCode, String dateFrom, String dateTo, char dpOnQuantities,
                    String dnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.SOCode, t2.Line, t2.Quantity, t1.Date, t1.CompanyCode, t1.CompanyName, "
                                   + "t1.AllSupplied FROM sol AS t2 INNER JOIN so AS t1 "
                                   + "ON t2.SOCode = t1.SOCode WHERE t2.ItemCode = '" + generalUtils.sanitiseForSQL(itemCode)
                                   + "' AND t1.Status != 'C' AND t1.Date >= {d '" + dateFrom + "'} AND t1.Date <= {d '" + dateTo
                                   + "'} ORDER BY t1.Date, t2.SOCode, t2.Line");

    String soCode, soLine, date, customerCode, customerName, outstanding, allSupplied, cssFormat="";
    double qty, actualQty;
    boolean first = true;
    byte[] b = new byte[50];

    while(rs.next())
    {    
      soCode        = rs.getString(1);
      soLine        = rs.getString(2);
      qty           = generalUtils.doubleFromStr(rs.getString(3));
      date          = rs.getString(4);
      customerCode  = rs.getString(5);
      customerName  = rs.getString(6);
      allSupplied   = rs.getString(7);
  
      actualQty = pickingList.getTotalPickedForASOLine(con, stmt2, rs2, soCode, soLine);

      if(first)
      {
        scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
        scoutln(out, bytesOut, "<tr id=\"pageColumn\"><td nowrap colspan=8><p>Sales Orders:</td></tr>");
        scoutln(out, bytesOut, "<tr id=\"pageColumn\">");
        scoutln(out, bytesOut, "<td nowrap><p>Outstanding</td>");
        scoutln(out, bytesOut, "<td nowrap><p>Quantity-Ordered</td>");
        scoutln(out, bytesOut, "<td nowrap><p>SO Code</td>");
        scoutln(out, bytesOut, "<td nowrap><p>SO Date</td>");
        scoutln(out, bytesOut, "<td nowrap><p>Customer</td></tr>");
        first = false;
     }

      if((qty - actualQty) != 0)
      {
        if(allSupplied.equals("Y"))
          outstanding = "";
        else
        {
          generalUtils.doubleToBytesCharFormat((qty - actualQty), b, 0);
          generalUtils.bytesDPsGivenSeparator(true, '\000', dpOnQuantities, b, 20, 0);
          generalUtils.formatNumeric(b, dpOnQuantities);
          outstanding = generalUtils.stringFromBytes(b, 0L);
        }
      }
      else outstanding = "";

      generalUtils.doubleToBytesCharFormat(qty, b, 0);
      generalUtils.bytesDPsGivenSeparator(true, '\000', dpOnQuantities, b, 20, 0);
      generalUtils.formatNumeric(b, dpOnQuantities);

      if(cssFormat.equals("line1")) cssFormat = "line2"; else cssFormat = "line1";

      scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\"><td align=center>" + outstanding + "</td>");
      scoutln(out, bytesOut, "<td align=center>" + generalUtils.stringFromBytes(b, 0L) + "</td>");
      scoutln(out, bytesOut, "<td nowrap><a href=\"javascript:viewSO('" + soCode + "')\">" + soCode + "</a></td>");
      scoutln(out, bytesOut, "<td nowrap>" + generalUtils.convertFromYYYYMMDD(date) + "</td>");
      scoutln(out, bytesOut, "<td nowrap>" + customerCode + " (" + customerName +")</td></tr>");
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void onPL(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String itemCode, String dateFrom, String dateTo, char dpOnQuantities,
                    String dnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.PLCode, t2.QuantityPacked, t1.Date, t1.CompanyCode, t1.CompanyName, t1.Completed "
                                   + "FROM pll AS t2 INNER JOIN pl AS t1 ON t2.PLCode = t1.PLCode WHERE t2.ItemCode = '"
                                   + generalUtils.sanitiseForSQL(itemCode) + "' AND t1.Status != 'C' AND t1.Date >= {d '" + dateFrom + "'} AND t1.Date <= {d '"
                                   + dateTo + "'} ORDER BY t1.Date, t2.PLCode, t2.Line");

    String plCode, date, customerCode, customerName, completed, cssFormat="";
    boolean first = true;
    double qtyPicked;
    byte[] b = new byte[50];

    while(rs.next())
    {    
      plCode       = rs.getString(1);
      qtyPicked    = generalUtils.doubleFromStr(rs.getString(2));
      date         = rs.getString(3);
      customerCode = rs.getString(4);
      customerName = rs.getString(5);
      completed    = rs.getString(6);

      if(first)
      {
        scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
        scoutln(out, bytesOut, "<tr id=\"pageColumn\"><td nowrap colspan=8><p>Picking Lists:</td></tr>");
        scoutln(out, bytesOut, "<tr id=\"pageColumn\"><td></td>");
        scoutln(out, bytesOut, "<td nowrap align=center><p>Quantity-Picked</td>");
        scoutln(out, bytesOut, "<td nowrap><p>PL Code</td>");
        scoutln(out, bytesOut, "<td nowrap><p>Date</td>");
        scoutln(out, bytesOut, "<td nowrap><p>Customer</td></tr>");
        first = false;
      }

      generalUtils.doubleToBytesCharFormat(qtyPicked, b, 0);
      generalUtils.bytesDPsGivenSeparator(true, '\000', dpOnQuantities, b, 20, 0);
      generalUtils.formatNumeric(b, dpOnQuantities);

      if(cssFormat.equals("line1"))
        cssFormat = "line2";
      else cssFormat = "line1";

      scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\"><td nowrap>");

      if(! completed.equals("Y"))
        scoutln(out, bytesOut, "<p>(Not Completed)");

      scoutln(out, bytesOut, "</td><td align=center><p>" + generalUtils.stringFromBytes(b, 0L) + "</td>");
      scoutln(out, bytesOut, "<td nowrap><p><a href=\"javascript:viewPL('" + plCode + "')\">" + plCode + "</a></td>");
      scoutln(out, bytesOut, "<td nowrap><p>" + generalUtils.convertFromYYYYMMDD(date) + "</td>");
      scoutln(out, bytesOut, "<td nowrap><p>" + customerCode + " (" + customerName +")</td></tr>");
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void onGRN(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String itemCode, String dateFrom, String dateTo,
                     char dpOnQuantities, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.GRCode, t2.Quantity, t1.Date, t1.CompanyCode, t1.CompanyName, t1.StockProcessed, "
                                   + "t2.POCode FROM grl AS t2 INNER JOIN gr AS t1 ON t2.GRCode = t1.GRCode WHERE t2.ItemCode = '"
                                   + generalUtils.sanitiseForSQL(itemCode) + "' AND t1.Status != 'C' AND t1.Date >= {d '" + dateFrom + "'} AND t1.Date <= {d '"
                                   + dateTo + "'} ORDER BY t1.Date, t2.GRCode, t2.Line");

    String grCode, date, supplierCode, supplierName, poCode, stockProcessed, qty, cssFormat="";
    boolean first = true;

    while(rs.next())
    {    
      grCode         = rs.getString(1);
      qty            = rs.getString(2);
      date           = rs.getString(3);
      supplierCode   = rs.getString(4);
      supplierName   = rs.getString(5);
      stockProcessed = rs.getString(6);
      poCode         = rs.getString(7);

      if(first)
      {
        scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
        scoutln(out, bytesOut, "<tr id=\"pageColumn\"><td nowrap colspan=8><p>Goods Received Notes:</td></tr>");
        scoutln(out, bytesOut, "<tr id=\"pageColumn\"><td></td>");
        scoutln(out, bytesOut, "<td nowrap align=center><p>Quantity-Received</td>");
        scoutln(out, bytesOut, "<td nowrap><p>GRN Code</td>");
        scoutln(out, bytesOut, "<td nowrap><p>Date</td>");
        scoutln(out, bytesOut, "<td nowrap><p>Supplier</td>");
        scoutln(out, bytesOut, "<td><p>PO Code</td></tr>");
        first = false;
      }

      qty = generalUtils.formatNumeric(qty, dpOnQuantities);

      if(cssFormat.equals("line1"))
        cssFormat = "line2";
      else cssFormat = "line1";

      scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\"><td nowrap>");

      if(! stockProcessed.equals("Y"))
        scoutln(out, bytesOut, "(In-Transit)");

      scoutln(out, bytesOut, "</td><td align=center><p>" + qty + "</td>");
      scoutln(out, bytesOut, "<td nowrap><p><a href=\"javascript:viewGRN('" + grCode+ "')\">" + grCode+ "</a></td>");
      scoutln(out, bytesOut, "<td nowrap><p>" + generalUtils.convertFromYYYYMMDD(date) + "</td>");
      scoutln(out, bytesOut, "<td nowrap><p>" + supplierCode + " (" + supplierName +")</td>");

      scoutln(out, bytesOut, "<td nowrap><a href=\"javascript:viewPO('" + poCode + "')\">" + poCode + "</a></td></tr>");
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void onAdjustment(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String itemCode, String dateFrom, String dateTo, char dpOnQuantities,
                            String dnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT AdjustmentCode, Quantity, Date, Remark, StoreFrom, StoreTo FROM stocka "
                                   + "WHERE ItemCode = '" + generalUtils.sanitiseForSQL(itemCode) + "' AND Status != 'C' AND Date >= {d '" + dateFrom
                                   + "'} AND Date <= {d '" + dateTo + "'} ORDER BY Date, AdjustmentCode");

    String adjustmentCode, date, qty, storeFrom, storeTo, remark, cssFormat="";
    boolean first = true;

    while(rs.next())
    {    
      adjustmentCode = rs.getString(1);
      qty            = rs.getString(2);
      date           = rs.getString(3);
      remark         = rs.getString(4);
      storeFrom      = rs.getString(5);
      storeTo        = rs.getString(6);

      if(first)
      {
        scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
        scoutln(out, bytesOut, "<tr id=\"pageColumn\"><td nowrap colspan=8><p>Adjustments:</td></tr>");
        scoutln(out, bytesOut, "<tr id=\"pageColumn\"><td></td>");
        scoutln(out, bytesOut, "<td nowrap align=center><p>Quantity-Adjusted</td>");
        scoutln(out, bytesOut, "<td nowrap><p>Adjustment Code</td>");
        scoutln(out, bytesOut, "<td nowrap><p>Date</td>");
        scoutln(out, bytesOut, "<td nowrap><p>Remark</td></tr>");
        first = false;
      }

      qty = generalUtils.formatNumeric(qty, dpOnQuantities);

      if(cssFormat.equals("line1"))
        cssFormat = "line2";
      else cssFormat = "line1";

      scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\"><td></td>");

      scoutln(out, bytesOut, "<td align=center><p>" + qty + "</td>");
      scoutln(out, bytesOut, "<td nowrap><p><a href=\"javascript:viewAdj('" + adjustmentCode + "')\">" + adjustmentCode + "</a></td>");
      scoutln(out, bytesOut, "<td nowrap><p>" + generalUtils.convertFromYYYYMMDD(date) + "</td>");
      
      scoutln(out, bytesOut, "<td nowrap><p>");
      if(storeFrom.length() > 0)
      {
        scout(out, bytesOut, storeFrom);
        if(storeTo.length() > 0)
          scout(out, bytesOut, " to " + storeTo);
      }
      else
      {
        if(storeTo.length() > 0)
          scout(out, bytesOut, "To " + storeTo);
      }
      
      scoutln(out, bytesOut, " (" + remark + ")</td></tr>");
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void onStockCheck(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String itemCode, String dateFrom, String dateTo, char dpOnQuantities,
                            String dnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT CheckCode, Level, Date, Remark, StoreCode FROM stockc WHERE ItemCode = '" + generalUtils.sanitiseForSQL(itemCode) + "' AND Status != 'C' AND Type = 'S' AND Date >= {d '" + dateFrom + "'} AND Date <= {d '" + dateTo
                         + "'} AND Level != '999999' ORDER BY Date, CheckCode");

    String checkCode, date, qty, store, remark, cssFormat="";
    boolean first = true;

    while(rs.next())
    {    
      checkCode = rs.getString(1);
      qty       = rs.getString(2);
      date      = rs.getString(3);
      remark    = rs.getString(4);
      store     = rs.getString(5);

      if(first)
      {
        scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
        scoutln(out, bytesOut, "<tr id=\"pageColumn\"><td nowrap colspan=8><p>Stock Checks:</td></tr>");
        scoutln(out, bytesOut, "<tr id=\"pageColumn\"><td></td>");
        scoutln(out, bytesOut, "<td nowrap align=center><p>Quantity</td>");
        scoutln(out, bytesOut, "<td nowrap><p>Check Code</td>");
        scoutln(out, bytesOut, "<td nowrap><p>Date</td>");
        scoutln(out, bytesOut, "<td nowrap><p>Store/Remark</td></tr>");
        first = false;
      }

      qty = generalUtils.formatNumeric(qty, dpOnQuantities);

      if(cssFormat.equals("line1"))
        cssFormat = "line2";
      else cssFormat = "line1";

      scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\"><td></td>");

      scoutln(out, bytesOut, "<td align=center><p>" + qty + "</td>");
      scoutln(out, bytesOut, "<td nowrap><p><a href=\"javascript:viewChk('" + checkCode + "')\">" + checkCode + "</a></td>");
      scoutln(out, bytesOut, "<td nowrap><p>" + generalUtils.convertFromYYYYMMDD(date) + "</td>");
      
      scoutln(out, bytesOut, "<td nowrap><p>" + store + " (" + remark + ")</td></tr>");
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void onDO(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String itemCode, String dateFrom, String dateTo, char dpOnQuantities,
                    String dnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();
   
    rs = stmt.executeQuery("SELECT t2.DOCode, t2.Quantity, t1.Date, t1.CompanyCode, t1.CompanyName "
                                   + "FROM dol AS t2 INNER JOIN do AS t1 ON t2.DOCode = t1.DOCode WHERE t2.ItemCode = '"
                                   + generalUtils.sanitiseForSQL(itemCode) + "' AND t1.Status != 'C' AND t1.Date >= {d '" + dateFrom + "'} AND t1.Date <= {d '"
                                   + dateTo + "'} ORDER BY t1.Date, t2.DOCode, t2.Line");

    String doCode, date, qty, customerCode, customerName, cssFormat="";
    boolean first = true;

    while(rs.next())
    {    
      doCode       = rs.getString(1);
      qty          = rs.getString(2);
      date         = rs.getString(3);
      customerCode = rs.getString(4);
      customerName = rs.getString(5);

      if(first)
      {
        scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
        scoutln(out, bytesOut, "<tr id=\"pageColumn\"><td nowrap colspan=8<p>Delivery Orders:</td></tr>");
        scoutln(out, bytesOut, "<tr id=\"pageColumn\"><td></td>");
        scoutln(out, bytesOut, "<td nowrap align=center><p>Quantity-Delivered</td>");
        scoutln(out, bytesOut, "<td nowrap><p>DO Code</td>");
        scoutln(out, bytesOut, "<td nowrap><p>Date</td>");
        scoutln(out, bytesOut, "<td nowrap><p>Customer</td></tr>");
        first = false;
      }

      qty = generalUtils.formatNumeric(qty, dpOnQuantities);

      if(cssFormat.equals("line1"))
        cssFormat = "line2";
      else cssFormat = "line1";

      scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\"><td nowrap>");
      scoutln(out, bytesOut, "</td><td align=center><p>" + qty + "</td>");
      scoutln(out, bytesOut, "<td nowrap><p><a href=\"javascript:viewDO('" + doCode + "')\">" + doCode + "</a></td>");
      scoutln(out, bytesOut, "<td nowrap><p>" + generalUtils.convertFromYYYYMMDD(date) + "</td>");
      scoutln(out, bytesOut, "<td nowrap><p>" + customerCode + " (" + customerName + ")</td></tr>");
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void onInvoice(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String itemCode, String dateFrom, String dateTo, char dpOnQuantities,
                         String dnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.InvoiceCode, t2.Quantity, t1.Date, t1.CompanyCode, t1.CompanyName, t2.UnitPrice, t1.Currency FROM invoicel "
                                   + "AS t2 INNER JOIN invoice AS t1 ON t2.InvoiceCode = t1.InvoiceCode WHERE t2.ItemCode = '"
                                   + generalUtils.sanitiseForSQL(itemCode) + "' AND t1.Status != 'C' AND t1.Date >= {d '" + dateFrom + "'} AND t1.Date <= {d '"
                                   + dateTo + "'} ORDER BY t1.Date, t2.InvoiceCode, t2.Line");

    String invoiceCode, date, customerCode, customerName, qty, cssFormat="", unitPrice, currency;
    boolean first = true;

    while(rs.next())
    {    
      invoiceCode  = rs.getString(1);
      qty          = rs.getString(2);
      date         = rs.getString(3);
      customerCode = rs.getString(4);
      customerName = rs.getString(5);
      unitPrice    = rs.getString(6);
      currency     = rs.getString(7);

      if(first)
      {
        scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
        scoutln(out, bytesOut, "<tr id=\"pageColumn\"><td nowrap colspan=8><p>Invoices:</td></tr>");
        scoutln(out, bytesOut, "<tr id=\"pageColumn\"><td></td>");
        scoutln(out, bytesOut, "<td nowrap align=center><p>Quantity-Invoiced</td>");
        scoutln(out, bytesOut, "<td nowrap><p>Unit Price</td>");
        scoutln(out, bytesOut, "<td nowrap><p>Invoice Code</td>");
        scoutln(out, bytesOut, "<td nowrap><p>Date</td>");
        scoutln(out, bytesOut, "<td nowrap><p>Customer</td></tr>");
        first = false;
      }

      qty = generalUtils.formatNumeric(qty, dpOnQuantities);
     
      if(cssFormat.equals("line1")) cssFormat = "line2"; else cssFormat = "line1";

      scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\"><td nowrap>");

      scoutln(out, bytesOut, "</td><td align=center><p>" + qty + "</td>");
      scoutln(out, bytesOut, "<td nowrap align='right'><p>" + currency + " " + generalUtils.formatNumeric(unitPrice, '2') + "</td>");
      scoutln(out, bytesOut, "<td nowrap><p><a href=\"javascript:viewInvoice('" + invoiceCode + "')\">" + invoiceCode + "</a></td>");
      scoutln(out, bytesOut, "<td nowrap><p>" + date + "</td>");
      scoutln(out, bytesOut, "<td nowrap><p>" + customerCode + " (" + customerName +")</td></tr>");
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void onQuotes(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String itemCode, String dateFrom, String dateTo, char dpOnQuantities,
                        String dnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.QuoteCode, t2.Quantity, t1.QuoteDate, t1.CompanyCode, t1.CompanyName "
                                   + "FROM quotel AS t2 INNER JOIN quote AS t1 ON t2.QuoteCode = t1.QuoteCode WHERE t2.ItemCode = '"
                                   + generalUtils.sanitiseForSQL(itemCode) + "' AND t1.Status != 'C' AND t1.QuoteDate >= {d '" + dateFrom +
                                   "'} AND t1.QuoteDate <= {d '" + dateTo + "'} ORDER BY t1.QuoteDate, t2.QuoteCode, t2.Line");

    String quoteCode, date, qty, customerCode, customerName, cssFormat="";
    boolean first = true;

    while(rs.next())
    {    
      quoteCode    = rs.getString(1);
      qty          = rs.getString(2);
      date         = rs.getString(3);
      customerCode = rs.getString(4);
      customerName = rs.getString(5);

      if(first)
      {
        scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
        scoutln(out, bytesOut, "<tr id=\"pageColumn\"><td nowrap colspan=8><p>Quotations:</td></tr>");
        scoutln(out, bytesOut, "<tr id=\"pageColumn\"><td></td>");
        scoutln(out, bytesOut, "<td nowrap align=center><p>Quantity-Quoted</td>");
        scoutln(out, bytesOut, "<td nowrap><p>Quotation Code</td>");
        scoutln(out, bytesOut, "<td nowrap><p>Date</td>");
        scoutln(out, bytesOut, "<td nowrap><p>Customer</td></tr>");
        first = false;
      }

      qty = generalUtils.formatNumeric(qty, dpOnQuantities);

      if(cssFormat.equals("line1"))
        cssFormat = "line2";
      else cssFormat = "line1";

      scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\"><td nowrap>");

      scoutln(out, bytesOut, "</td><td align=center><p>" + qty + "</td>");
      scoutln(out, bytesOut, "<td nowrap><p><a href=\"javascript:viewQuote('" + quoteCode + "')\">" + quoteCode + "</a></td>");
      scoutln(out, bytesOut, "<td nowrap><p>" + generalUtils.convertFromYYYYMMDD(date) + "</td>");
      scoutln(out, bytesOut, "<td nowrap width=99%><p>" + customerCode + " (" + customerName + ")</td></tr>");
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void outputGRNCodes(PrintWriter out, String grnCodes, int[] bytesOut) throws Exception
  {
    int y=0, len = grnCodes.length();
    String thisGRNCode;

    while(y < len) // just-in-case
    {
      thisGRNCode = "";
      while(y < len && grnCodes.charAt(y) != ' ')
        thisGRNCode += grnCodes.charAt(y++);
      ++y;

      scoutln(out, bytesOut, "<a href=\"javascript:viewGRN('" + thisGRNCode + "')\">" + thisGRNCode + "</a> ");
    }
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

}
