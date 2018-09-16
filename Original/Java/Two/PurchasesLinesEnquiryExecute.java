// =======================================================================================================================================================================================================
// System: ZaraStar: Document: Purchase Lines Enquiry - search
// Module: PurchasesLinesEnquiryExecute.java
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
import java.util.*;
import java.io.*;

public class PurchasesLinesEnquiryExecute extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  MiscDefinitions miscDefinitions = new MiscDefinitions();
  PickingList pickingList = new PickingList();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", suppCode="", itemCode="", y="", z="", g="", i="", dateFrom="", dateTo="";

    try
    {
      directoryUtils.setContentHeaders2(res);
      out = res.getWriter();

      Enumeration en = req.getParameterNames();
      while(en.hasMoreElements())
      {
        String name = (String)en.nextElement();
        String[] value = req.getParameterValues(name);
        if(name.equals("unm"))
          unm = value[0];
        else
        if(name.equals("sid"))
          sid = value[0];
        else
        if(name.equals("uty"))
          uty = value[0];
        else
        if(name.equals("men"))
          men = value[0];
        else
        if(name.equals("den"))
          den = value[0];
        else
        if(name.equals("dnm"))
          dnm = value[0];
        else
        if(name.equals("bnm"))
          bnm = value[0];
        else
        if(name.equals("Y"))
          y = value[0];
        else
        if(name.equals("Z"))
          z = value[0];
        else
        if(name.equals("G"))
          g = value[0];
        else
        if(name.equals("I"))
          i = value[0];
        else
        if(name.equals("itemCode"))
          itemCode = value[0];
        else
        if(name.equals("suppCode"))
          suppCode = value[0];
        else
        if(name.equals("dateFrom"))
          dateFrom = value[0];
        else
        if(name.equals("dateTo"))
          dateTo = value[0];
      }
      
      if(y == null) y = "";
      if(z == null) z = "";
      if(g == null) g = "";
      if(i == null) i = "";
      if(itemCode == null) itemCode = "";
      if(suppCode == null) suppCode  = "";
      if(dateFrom == null) dateFrom = "";
      if(dateTo   == null) dateTo   = "";

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, suppCode, itemCode, dateFrom, dateTo, y, z, g, i, bytesOut);
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

      System.out.println("1032a: " + e);
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "PurchasesLinesEnquiryExecute", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1032, bytesOut[0], 0, "ERR:" + suppCode);
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                    String suppCode, String itemCode, String dateFrom, String dateTo, String y, String z, String g, String i, int[] bytesOut)
                    throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;
    
    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 1032, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "PurchasesLinesEnquiry", imagesDir, localDefnsDir,
                         defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1032, bytesOut[0], 0, "ACC:" + suppCode);
    if(con  != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "PurchasesLinesEnquiry", imagesDir, localDefnsDir,
                         defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1032, bytesOut[0], 0, "SID:" + suppCode);
    if(con  != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    String dateFromText;
    if(dateFrom.length() == 0)
    {
      dateFromText = "Earliest";
      dateFrom = "1970-01-01";
    }
    else
    {
      dateFromText = dateFrom;
      dateFrom = generalUtils.convertDateToSQLFormat(dateFrom);
    }
    
    String dateToText;
    if(dateTo.length() == 0)
    {
      dateToText = "Latest";
      dateTo = "2099-12-31";
    }
    else
    {
      dateToText = dateTo;
      dateTo = generalUtils.convertDateToSQLFormat(dateTo);
    }
    
    suppCode = suppCode.toUpperCase();
    itemCode = itemCode.toUpperCase();

    set(con, stmt, rs, out, req, unm, sid, uty, men, den, dnm, bnm, suppCode, itemCode, dateFrom, dateTo, dateFromText, dateToText, y, z, g,
        i, imagesDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 1032, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con  != null) con.close();
    if(out != null) out.flush(); 
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den,
                   String dnm, String bnm, String suppCode, String itemCode, String dateFrom, String dateTo, String dateFromText,
                   String dateToText, String y, String z, String g, String i, String imagesDir, String localDefnsDir,
                   String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Purchases Document Lines Enquiry</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 5006, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function po(code){");
      scoutln(out, bytesOut, "var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/PurchaseOrderPage?unm=" + unm + "&sid=" + sid + "&uty="
                           + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3025, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function grn(code){");
      scoutln(out, bytesOut, "var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/GoodsReceivedPage?unm=" + unm + "&sid=" + sid + "&uty="
                           + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 5016, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function lp(code){");
      scoutln(out, bytesOut, "var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/LocalPurchasePage?unm=" + unm + "&sid=" + sid + "&uty="
                          + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 5080, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function pinvoice(code){");
      scoutln(out, bytesOut, "var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/PurchaseInvoicePage?unm=" + unm + "&sid=" + sid + "&uty="
                            + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3001, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function item(code){");
      scoutln(out, bytesOut, "var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/ProductStockRecord?unm=" + unm + "&sid=" + sid + "&uty="
                           + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p1=\"+p1;}");
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
    
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "1032", "", "PurchasesLinesEnquiry", unm, sid, uty, men, den, dnm, bnm,
                          localDefnsDir, defnsDir, hmenuCount, bytesOut);

    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Purchase Document Lines Enquiry Results", "1032", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<form><table id=\"page\" width=100% border=0>");
    
    if(suppCode.length() == 0)
    {
      scoutln(out, bytesOut, "<br><br><span id=\"enquiryMessage\">&nbsp; No Supplier Code Specified</span><br><br>");
      if(con != null) con.close();
      return;
    }
    else
    if(itemCode.length() != 0)
    {
      Inventory inventory = new Inventory();
      if(! inventory.existsItemRecGivenCode(con, stmt, rs, itemCode))
      {
        scoutln(out, bytesOut, "<br><br><span id=\"enquiryMessage\">Item Code &nbsp; &nbsp; " + itemCode);
        scoutln(out, bytesOut, "&nbsp; &nbsp; <font color=\"#FF0000\">Not Found</span><br><br>");
        if(con != null) con.close();
        return;
      }
    }
    else
    if(suppCode.length() == 0)
    {
      Supplier supplier = new Supplier();
      if(! supplier.existsSupplierRecGivenCode(con, stmt, rs, suppCode, dnm, localDefnsDir, defnsDir))
      {
        scoutln(out, bytesOut, "<br><br><span id=\"enquiryMessage\">Supplier Code &nbsp; &nbsp; " + suppCode);
        scoutln(out, bytesOut, "&nbsp; &nbsp; <font color=\"#FF0000\">Not Found</span><br><br>");
        if(con != null) con.close();
        return;
      }
    }

    char dpOnQuantities = miscDefinitions.dpOnQuantities(con, stmt, rs, "5");
    
    if(y.equals("on"))
    {
      forAllPurchaseOrders(con, stmt, rs, out, suppCode, itemCode, dateFrom, dateTo, dpOnQuantities, dnm, imagesDir, localDefnsDir, defnsDir,
                           bytesOut);
    }
  
    if(z.equals("on"))
      forAllLPRs(con, stmt, rs, out, suppCode, itemCode, dateFrom, dateTo, dpOnQuantities, dnm, imagesDir, localDefnsDir, defnsDir, bytesOut);
  
    if(g.equals("on"))
      forAllGRNs(con, stmt, rs, out, suppCode, itemCode, dateFrom, dateTo, dpOnQuantities, dnm, imagesDir, localDefnsDir, defnsDir, bytesOut);
  
    if(i.equals("on"))
    {
      forAllPurchaseInvoices(con, stmt, rs, out, suppCode, itemCode, dateFrom, dateTo, dpOnQuantities, dnm, imagesDir, localDefnsDir, defnsDir,
                             bytesOut);
    }
  
    scoutln(out, bytesOut, "</table></form>");     scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void forAllPurchaseOrders(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String suppCode, String itemCode,
                                    String dateFrom, String dateTo, char dpOnQuantities, String dnm, String imagesDir, 
                                    String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    String[] cssFormat = new String[1];  cssFormat[0] = "line1";

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr id=\"pageColumn\"><td nowrap colspan=10><p>Purchase Orders:</td></tr>");
    scoutln(out, bytesOut, "<tr id=\"pageColumn\"><td></td>");
    scoutln(out, bytesOut, "<td><p> Document Code </td>");
    scoutln(out, bytesOut, "<td><p> Line </td>");
    scoutln(out, bytesOut, "<td><p> Date </td>");
    scoutln(out, bytesOut, "<td><p> Item Code </td>");
    scoutln(out, bytesOut, "<td><p> Mfr </td>");
    scoutln(out, bytesOut, "<td><p> Mfr Code </td>");
    scoutln(out, bytesOut, "<td><p> Quantity </td>");
    scoutln(out, bytesOut, "<td><p> Discount </td>");
    scoutln(out, bytesOut, "<td><p> Unit Price </td>");
    scoutln(out, bytesOut, "<td><p> Currency </td>");
    scoutln(out, bytesOut, "<td nowrap><p> Description </td>");
    scoutln(out, bytesOut, "<td nowrap><p> Sales Order Code </td></tr>");

    String itemCodeCriteria;
    if(itemCode.length() > 0)
      itemCodeCriteria = "AND ItemCode = '" + itemCode + "'";
    else itemCodeCriteria = "";
    
    rs = stmt.executeQuery("SELECT t1.POCode, t1.Date, t1.Currency, t1.Status, t2.Description, t2.Quantity, t2.SOCode, "
                         + "t2.Line, t2.ItemCode, t2.Manufacturer, t2.ManufacturerCode, t2.UnitPrice, t2.Discount, t2.amount2 FROM pol AS t2 INNER JOIN po AS t1 "
                         + "ON t2.POCode = t1.POCode WHERE t1.CompanyCode = '" + suppCode + "' AND Date >= {d '" + dateFrom
                         + "'} AND Date <= {d '" + dateTo + "'} " + itemCodeCriteria + " ORDER BY t1.Date, t1.POCode");
      
    String code, date, soCode, currency, status, unitPrice, discount, amount, desc, quantity, line, mfr, mfrCode;
    
    while(rs.next())
    {    
      code      = rs.getString(1);
      date      = rs.getString(2);
      currency  = rs.getString(3);
      status    = rs.getString(4);
      desc      = rs.getString(5);
      quantity  = rs.getString(6);
      soCode    = rs.getString(7);
      line      = rs.getString(8);
      itemCode  = rs.getString(9);
      mfr       = rs.getString(10);
      mfrCode   = rs.getString(11);
      unitPrice = rs.getString(12);
      discount  = rs.getString(13);
      amount    = rs.getString(14);
      
      writeLine(out, dnm, "po", code, line, date, soCode, itemCode, mfr, mfrCode, quantity, desc, unitPrice, discount, amount, currency,
                status, dpOnQuantities, cssFormat, imagesDir, localDefnsDir, defnsDir, bytesOut);
    }
  
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void forAllGRNs(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String suppCode, String itemCode,
                          String dateFrom, String dateTo, char dpOnQuantities, String dnm, String imagesDir,
                          String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    String[] cssFormat = new String[1];  cssFormat[0] = "line1";

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr id=\"pageColumn\"><td nowrap colspan=10><p>Goods Received Notes:</td></tr>");
    scoutln(out, bytesOut, "<tr id=\"pageColumn\"><td></td>");
    scoutln(out, bytesOut, "<td><p> Document Code </td>");
    scoutln(out, bytesOut, "<td><p> Line </td>");
    scoutln(out, bytesOut, "<td><p> Date </td>");
    scoutln(out, bytesOut, "<td><p> Item Code </td>");
    scoutln(out, bytesOut, "<td><p> Mfr </td>");
    scoutln(out, bytesOut, "<td><p> Mfr Code </td>");
    scoutln(out, bytesOut, "<td><p> Quantity </td>");
    scoutln(out, bytesOut, "<td><p> Amount </td>");
    scoutln(out, bytesOut, "<td nowrap><p> Description </td></tr>");

    String itemCodeCriteria;
    if(itemCode.length() > 0)
      itemCodeCriteria = "AND ItemCode = '" + itemCode + "'";
    else itemCodeCriteria = "";
    
    rs = stmt.executeQuery("SELECT t1.GRCode, t1.Date, t1.Status, t2.Description, t2.Quantity, t2.Line, "
                         + "t2.ItemCode, t2.Manufacturer, t2.ManufacturerCode, t2.UnitPrice, t2.Amount FROM grl AS t2 INNER JOIN gr AS t1 "
                         + "ON t2.GRCode = t1.GRCode WHERE t1.CompanyCode = '" + suppCode + "' AND Date >= {d '" + dateFrom
                         + "'} AND Date <= {d '" + dateTo + "'} " + itemCodeCriteria + " ORDER BY t1.Date, t1.GRCode");
      
    String code, date, status, unitPrice, amount, desc, quantity, line, mfr, mfrCode;
    
    while(rs.next())
    {    
      code       = rs.getString(1);
      date       = rs.getString(2);
      status     = rs.getString(3);
      desc       = rs.getString(4);
      quantity   = rs.getString(5);
      line       = rs.getString(6);
      itemCode   = rs.getString(7);
      mfr        = rs.getString(8);
      mfrCode    = rs.getString(9);
      unitPrice  = rs.getString(10);
      amount     = rs.getString(11);
      
      writeLine(out, dnm, "grn", code, line, date, "", itemCode, mfr, mfrCode, quantity, desc, unitPrice, "", amount, "", status, dpOnQuantities,
                cssFormat, imagesDir, localDefnsDir, defnsDir, bytesOut);
    }
  
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void forAllLPRs(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String suppCode, String itemCode,
                          String dateFrom, String dateTo, char dpOnQuantities, String dnm, String imagesDir,
                          String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    String[] cssFormat = new String[1];  cssFormat[0] = "line1";

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr id=\"pageColumn\"><td nowrap colspan=10><p>Local Purchase Requisitions:</td></tr>");
    scoutln(out, bytesOut, "<tr id=\"pageColumn\"><td></td>");
    scoutln(out, bytesOut, "<td><p> Document Code </td>");
    scoutln(out, bytesOut, "<td><p> Line </td>");
    scoutln(out, bytesOut, "<td><p> Date </td>");
    scoutln(out, bytesOut, "<td><p> Item Code </td>");
    scoutln(out, bytesOut, "<td><p> Mfr </td>");
    scoutln(out, bytesOut, "<td><p> Mfr Code </td>");
    scoutln(out, bytesOut, "<td><p> Quantity </td>");
    scoutln(out, bytesOut, "<td><p> Discount </td>");
    scoutln(out, bytesOut, "<td><p> Amount </td>");
    scoutln(out, bytesOut, "<td><p> Currency </td>");
    scoutln(out, bytesOut, "<td nowrap><p> Description </td>");
    scoutln(out, bytesOut, "<td nowrap><p>Sales Order Code </td></tr>");

    String itemCodeCriteria;
    if(itemCode.length() > 0)
      itemCodeCriteria = "AND ItemCode = '" + itemCode + "'";
    else itemCodeCriteria = "";
    
    rs = stmt.executeQuery("SELECT t1.LPCode, t1.Date, t1.Currency, t1.Status, t2.Description, t2.Quantity, t2.Line, t2.SOCode, "
                         + "t2.ItemCode, t2.Manufacturer, t2.ManufacturerCode, t2.UnitPrice, t2.Discount, t2.Amount2 FROM lpl AS t2 INNER JOIN lp AS t1 "
                         + "ON t2.LPCode = t1.LPCode WHERE t1.CompanyCode = '" + suppCode + "' AND Date >= {d '" + dateFrom
                         + "'} AND Date <= {d '" + dateTo + "'} " + itemCodeCriteria + " ORDER BY t1.Date, t1.LPCode");
      
    String code, date, soCode, currency, status, unitPrice, discount, amount, desc, quantity, line, mfr, mfrCode;
    
    while(rs.next())
    {    
      code      = rs.getString(1);
      date      = rs.getString(2);
      currency  = rs.getString(3);
      status    = rs.getString(4);
      desc      = rs.getString(5);
      quantity  = rs.getString(6);
      line      = rs.getString(7);
      soCode    = rs.getString(8);
      itemCode  = rs.getString(9);
      mfr       = rs.getString(10);
      mfrCode   = rs.getString(11);
      unitPrice = rs.getString(12);
      discount  = rs.getString(13);
      amount    = rs.getString(14);
      
      writeLine(out, dnm, "lp", code, line, date, soCode, itemCode, mfr, mfrCode, quantity, desc, unitPrice, discount, amount, currency, status,
                dpOnQuantities, cssFormat, imagesDir, localDefnsDir, defnsDir, bytesOut);
    }
  
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }
 
  // -------------------------------------------------------------------------------------------------------------------------------
  private void forAllPurchaseInvoices(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String suppCode,
                                      String itemCode, String dateFrom, String dateTo, char dpOnQuantities, String dnm,
                                      String imagesDir, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    String[] cssFormat = new String[1];  cssFormat[0] = "line1";

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr id=\"pageColumn\"><td nowrap colspan=10><p>Purchase Invoices:</td></tr>");
    scoutln(out, bytesOut, "<tr id=\"pageColumn\"><td></td>");
    scoutln(out, bytesOut, "<td><p> Document Code </td>");
    scoutln(out, bytesOut, "<td><p> Line </td>");
    scoutln(out, bytesOut, "<td><p> Date </td>");
    scoutln(out, bytesOut, "<td><p> Item Code </td>");
    scoutln(out, bytesOut, "<td><p> Mfr </td>");
    scoutln(out, bytesOut, "<td><p> Mfr Code </td>");
    scoutln(out, bytesOut, "<td><p> Quantity </td>");
    scoutln(out, bytesOut, "<td><p> Discount </td>");
    scoutln(out, bytesOut, "<td><p> Amount </td>");
    scoutln(out, bytesOut, "<td><p> Currency </td>");
    scoutln(out, bytesOut, "<td nowrap><p> Description </td>");
    scoutln(out, bytesOut, "<td nowrap><p> Supplier Invoice Code </td></tr>");

    String itemCodeCriteria;
    if(itemCode.length() > 0)
      itemCodeCriteria = "AND ItemCode = '" + itemCode + "'";
    else itemCodeCriteria = "";
    
    rs = stmt.executeQuery("SELECT t1.InvoiceCode, t1.Date, t1.SupplierInvoiceCode, t1.Currency, t1.Status, t2.Description, "
                         + "t2.Quantity, t2.Line, t2.ItemCode, t2.Manufacturer, t2.ManufacturerCode, t2.UnitPrice, t2.Discount, t2.Amount FROM pinvoicel AS t2 "
                         + "INNER JOIN pinvoice AS t1 ON t2.InvoiceCode = t1.InvoiceCode WHERE t1.CompanyCode = '" + suppCode
                         + "' AND Date >= {d '" + dateFrom + "'} AND Date <= {d '" + dateTo + "'} " + itemCodeCriteria
                         + " ORDER BY t1.Date, t1.InvoiceCode");
      
    String code, date, suppInvoiceCode, currency, status, unitPrice, discount, amount, desc, quantity, line, mfr, mfrCode;
    
    while(rs.next())
    {    
      code            = rs.getString(1);
      date            = rs.getString(2);
      suppInvoiceCode = rs.getString(3);
      currency        = rs.getString(4);
      status          = rs.getString(5);
      desc            = rs.getString(6);
      quantity        = rs.getString(7);
      line            = rs.getString(8);
      itemCode        = rs.getString(9);
      mfr             = rs.getString(10);
      mfrCode         = rs.getString(11);
      unitPrice       = rs.getString(12);
      discount        = rs.getString(13);
      amount          = rs.getString(14);
      
      writeLine(out, dnm, "pinvoice", code, line, date, suppInvoiceCode, itemCode, mfr, mfrCode, quantity, desc, unitPrice, discount, amount,
                currency, status, dpOnQuantities, cssFormat, imagesDir, localDefnsDir, defnsDir, bytesOut);
    }
  
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void writeLine(PrintWriter out, String dnm, String jsName, String code, String line, String date, String soCode,
                         String itemCode, String mfr, String mfrCode, String quantity, String desc, String unitPrice, String discount, String amount,
                         String currency, String status, char dpOnQuantities, String[] cssFormat, String imagesDir,
                         String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    byte[] qty = new byte[20];
    generalUtils.strToBytes(qty, quantity);
    generalUtils.bytesDPsGivenSeparator(true, '\000', dpOnQuantities, qty, 20, 0);
    generalUtils.formatNumeric(qty, dpOnQuantities);
 
    if(cssFormat[0].equals("line1"))
      cssFormat[0] = "line2";
    else cssFormat[0] = "line1";
 
    scoutln(out, bytesOut, "<tr  id=\"" + cssFormat[0] + "\">");
    
    if(status.equals("C"))
      scout(out, bytesOut, "<td align=center><img src=\"" + imagesDir + "z0212.gif\" border=0></td>");
    else scout(out, bytesOut, "<td></td>");

    if(mfr     == null) mfr     = "";
    if(mfrCode == null) mfrCode = "";
    
    scout(out, bytesOut, "<td><p><a href=\"javascript:" + jsName + "('" + code + "')\">" + code + "</td>");
    scout(out, bytesOut, "<td align=center><p>" + line + "</td>");
    scout(out, bytesOut, "<td><p>" + generalUtils.convertFromYYYYMMDD(date) + "</td>");
    scout(out, bytesOut, "<td><p><a href=\"javascript:item('" + itemCode + "')\">" + itemCode + "</a></td>");
    scout(out, bytesOut, "<td><p>" + mfr + "</td>");
    scout(out, bytesOut, "<td><p>" + mfrCode + "</td>");
    scout(out, bytesOut, "<td align=center><p>" + generalUtils.stringFromBytes(qty, 0L) + "</td>");
    scout(out, bytesOut, "<td align=right><p>" + generalUtils.doubleDPs(discount, '2') + "</td>");
    scout(out, bytesOut, "<td align=right><p>" + generalUtils.doubleDPs(unitPrice, '2') + "</td>");
    scout(out, bytesOut, "<td><p>" + currency + "</td>");
    scout(out, bytesOut, "<td nowrap><p>" + desc + "</td>");
    scout(out, bytesOut, "<td><p>" + soCode + "</td></tr>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------
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

