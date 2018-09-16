// =======================================================================================================================================================================================================
// System: ZaraStar: Sales Orders Processing - Sales - Trades - details
// Module: SalesOrderTradesDetails.java
// Author: C.K.Harvey
// Copyright (c) 2001-09 Christopher Harvey. All Rights Reserved.
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

public class SalesOrderTradesDetails extends HttpServlet
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

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="";

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
      p1  = req.getParameter("p1"); // SOCode

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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "SalesOrderTrades", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2042, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush(); 
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String p1, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
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

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 2042, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "SalesOrderTradesDetails", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2042, bytesOut[0], 0, "ACC:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "SalesOrderTradesDetails", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2042, bytesOut[0], 0, "SID:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    set(con, stmt, stmt2, rs, rs2, out, req, p1, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 2042, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, HttpServletRequest req, String soCode, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                   String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Sales: Trades</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4031, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewSO(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/SalesOrderPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4067, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewInvoice(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/SalesInvoicePage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3011, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewSA(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/StockAdjustmentItem?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3038, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewPL(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/GoodsReceivedPickingList?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3001, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewItem(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/ProductStockRecord?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4054, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewDO(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/DeliveryOrderPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4101, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewCN(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/SalesCreditNotePage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    scoutln(out, bytesOut, "function mark(){");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/SalesOrderTradesa?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=" + unm + "&p2=" + soCode + "&bnm=" + bnm + "\";}");
    
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

    drawingUtils.outputPageFrame(con, stmt, rs, out, req, "2042", "", "SalesOrderTrades", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount, bytesOut);

    drawingUtils.drawTitle(out, false, false, "SalesOrderTrades", "", "Sales: Trades - Sales Order: " + soCode, "2042", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<table id=\"page\" width=100% border=0>");

    scoutln(out, bytesOut, "<tr><td></td></tr><tr id=\"pageColumn\">");

    scoutln(out, bytesOut, "<td><p>Document</td>");
    scoutln(out, bytesOut, "<td><p>Code</td>");
    scoutln(out, bytesOut, "<td><p>Date</td>");
    scoutln(out, bytesOut, "<td><p>Entry</td>");
    scoutln(out, bytesOut, "<td><p>Quantity</td>");
    scoutln(out, bytesOut, "<td><p>Description</td></tr>");

    char dpOnQuantities = miscDefinitions.dpOnQuantities(con, stmt, rs, "5");

    allLines(con, stmt, stmt2, rs, rs2, out, soCode, dpOnQuantities, bytesOut);

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td colspan=4><p><a href=\"javascript:mark()\">Mark</a> as Supplied</td></tr>");

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
  private void allLines(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, String soCode, char dpOnQuantities, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.ItemCode, t2.Quantity FROM sol AS t2 INNER JOIN so AS t1 ON t2.SOCode = t1.SOCode WHERE t1.Status != 'C' AND t2.SOCode = '" + soCode + "'");

    String code, qty;
    String[] codes = new String[1];  codes[0] = "";
    String[] html  = new String[1];

    while(rs.next())
      addToList(codes, rs.getString(1), rs.getString(2));

    int x = 0, len = codes[0].length();
    while(x < len)
    {
      code = "";
      while(x < len && codes[0].charAt(x) != '\002') // just-in-case
        code += codes[0].charAt(x++);
      ++x;

      qty = "";
      while(x < len && codes[0].charAt(x) != '\001') // just-in-case
        qty += codes[0].charAt(x++);
      ++x;

      scoutln(out, bytesOut, "<tr><td colspan=6><hr></td></tr><tr><td><p><a href=\"javascript:viewItem('" + code + "')\">" + code + "</a> (Sales Order Quantity: " + generalUtils.doubleDPs(qty, dpOnQuantities) + ")</td></tr>");

      html[0] = "";

      if(forAnSOLine(con, stmt, stmt2, rs, rs2, soCode, code, dpOnQuantities, html) >= generalUtils.doubleFromStr(qty)) // supplied
      {
        html[0] = html[0].replaceAll("id='line1'", "bgColor='lightgreen'");
        html[0] = html[0].replaceAll("id='line2'", "bgColor='lightgreen'");
      }
     
      scoutln(out, bytesOut, html[0]);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private double forAnSOLine(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String soCode, String itemCode, char dpOnQuantities, String[] html) throws Exception
  {
    double qtyOnDOs = 0.0;

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t1.PLCode, t1.Date, t2.QuantityPacked, t2.Entry, t2.Description "
                         + "FROM pll AS t2 INNER JOIN pl AS t1 ON t2.PLCode = t1.PLCode WHERE t1.Status != 'C' AND t2.SOCode = '" + soCode + "' AND t2.ItemCode = '" + itemCode + "'");

    String code, date, qty;
    String[] cssFormat = new String[1];  cssFormat[0] = "";

    while(rs.next())
    {
      code = rs.getString(1);
      date = rs.getString(2);
      qty  = rs.getString(3);

      if(cssFormat[0].equals("line1")) cssFormat[0] = "line2"; else cssFormat[0] = "line1";

      html[0] += "<tr id='" + cssFormat[0] + "'><td><p>Picking List</td><td><p><a href=\"javascript:viewPL('" + code + "')\">" + code + "</a></td>";
      html[0] += "<td><p>" + generalUtils.convertFromYYYYMMDD(date) + "</td><td><p>" + rs.getString(4) + "</td>";
      html[0] += "<td><p>" + generalUtils.formatNumeric(qty, dpOnQuantities) + "</td>";
      html[0] += "<td><p>" + rs.getString(5) + "</td></tr>";
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t1.DOCode, t1.Date, t2.Quantity, t2.Entry, t2.Description "
                         + "FROM dol AS t2 INNER JOIN do AS t1 ON t2.DOCode = t1.DOCode WHERE t1.Status != 'C' AND t2.SOCode = '" + soCode + "' AND t2.ItemCode = '" + itemCode + "'");

    while(rs.next())
    {
      code = rs.getString(1);
      date = rs.getString(2);
      qty  = rs.getString(3);

      qtyOnDOs += generalUtils.doubleFromStr(qty);

      if(cssFormat[0].equals("line1")) cssFormat[0] = "line2"; else cssFormat[0] = "line1";

      html[0] += "<tr id='" + cssFormat[0] + "'><td><p>Delivery Order</td><td><p><a href=\"javascript:viewDO('" + code + "')\">" + code + "</a></td>";
      html[0] += "<td><p>" + generalUtils.convertFromYYYYMMDD(date) + "</td><td><p>" + rs.getString(4) + "</td>";
      html[0] += "<td><p>" + generalUtils.formatNumeric(qty, dpOnQuantities) + "</td>";
      html[0] += "<td><p>" + rs.getString(5) + "</td></tr>";
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t1.InvoiceCode, t1.Date, t2.Quantity, t2.Entry, t2.Description "
                         + "FROM invoicel AS t2 INNER JOIN invoice AS t1 ON t2.InvoiceCode = t1.InvoiceCode WHERE t1.Status != 'C' AND t2.SOCode = '" + soCode + "' AND t2.ItemCode = '" + itemCode + "'");

    while(rs.next())
    {
      code = rs.getString(1);
      date = rs.getString(2);
      qty  = rs.getString(3);

      if(cssFormat[0].equals("line1")) cssFormat[0] = "line2"; else cssFormat[0] = "line1";

      html[0] += "<tr id='" + cssFormat[0] + "'><td><p>Invoice</td><td><p><a href=\"javascript:viewInvoice('" + code + "')\">" + code + "</a></td>";
      html[0] += "<td><p>" + generalUtils.convertFromYYYYMMDD(date) + "</td><td><p>" + rs.getString(4) + "</td>";
      html[0] += "<td><p>" + generalUtils.formatNumeric(qty, dpOnQuantities) + "</td>";
      html[0] += "<td><p>" + rs.getString(5) + "</td></tr>";

      cnLines(con, stmt2, rs2, code, itemCode, dpOnQuantities, cssFormat, html);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT AdjustmentCode, Date, Quantity FROM stocka WHERE SOCode = '" + soCode + "' AND StoreFrom = 'None' AND StoreTo != 'None' AND ItemCode = '" + itemCode + "'");

    while(rs.next())
    {
      code = rs.getString(1);
      date = rs.getString(2);
      qty  = rs.getString(3);

      if(cssFormat[0].equals("line1")) cssFormat[0] = "line2"; else cssFormat[0] = "line1";

      html[0] += "<tr id='" + cssFormat[0] + "'><td><p>Stock Adjustment</td><td><p><a href=\"javascript:viewSA('" + code + "')\">" + code + "</a></td>";
      html[0] += "<td><p>" + generalUtils.convertFromYYYYMMDD(date) + "</td><td><p>" + itemCode + "</td>";
      html[0] += "<td><p>" + generalUtils.formatNumeric(qty, dpOnQuantities) + "</td></tr>";
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    return qtyOnDOs;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void cnLines(Connection con, Statement stmt, ResultSet rs, String invoiceCode, String itemCode, char dpOnQuantities, String[] cssFormat, String[] html) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t1.CNCode, t1.Date, t2.Quantity "
                         + "FROM creditl AS t2 INNER JOIN credit AS t1 ON t2.CNCode = t1.CNCode WHERE t1.Status != 'C' AND t2.InvoiceCode = '" + invoiceCode + "' AND t2.ItemCode = '" + itemCode + "'");

    String code, date, qty;

    while(rs.next())
    {
      code = rs.getString(1);
      date = rs.getString(2);
      qty  = rs.getString(3);

      if(cssFormat[0].equals("line1")) cssFormat[0] = "line2"; else cssFormat[0] = "line1";

      html[0] += "<tr id='" + cssFormat[0] + "'><td><p>Credit Note</td><td><p><a href=\"javascript:viewCN('" + code + "')\">" + code + "</a></td>";
      html[0] += "<td><p>" + generalUtils.convertFromYYYYMMDD(date) + "</td><td><p>" + itemCode + "</td>";
      html[0] += "<td><p>" + generalUtils.formatNumeric(qty, dpOnQuantities) + "</td></tr>";
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void addToList(String[] codes, String newCode, String newQty) throws Exception
  {
    String code, qty;
    double qtyI, qtyJ;
    int x = 0, y, len = codes[0].length();
    while(x < len)
    {
      code = "";
      while(x < len && codes[0].charAt(x) != '\002') // just-in-case
        code += codes[0].charAt(x++);
      y=x;
      ++x;

      qty = "";
      while(x < len && codes[0].charAt(x) != '\001') // just-in-case
        qty += codes[0].charAt(x++);
      ++x;
      
      if(code.equals(newCode))
      {
        qtyI = generalUtils.doubleFromStr(qty);
        qtyJ = generalUtils.doubleFromStr(newQty);

        qty = generalUtils.doubleToStr(qtyI + qtyJ);

        String newCodesAndQtys = codes[0].substring(0, y) + "\002" + qty + "\001" + codes[0].substring(x);

        codes[0] = newCodesAndQtys;

        return;
      }
    }

    codes[0] += (newCode + "\002" + newQty + "\001");
  }

}
