// =======================================================================================================================================================================================================
// System: ZaraStar: Document: Sales Lines Enquiry - search
// Module: SalesLinesEnquiryExecute.java
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
import java.util.*;
import java.io.*;

public class SalesLinesEnquiryExecute extends HttpServlet
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

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", custCode="", itemCode="", q="", s="", p="", d="", i="", r="", dateFrom="", dateTo="";

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
        if(name.equals("Q"))
          q = value[0];
        else
        if(name.equals("S"))
          s = value[0];
        else
        if(name.equals("P"))
          p = value[0];
        else
        if(name.equals("D"))
          d = value[0];
        else
        if(name.equals("I"))
          i = value[0];
        else
        if(name.equals("R"))
          r = value[0];
        else
        if(name.equals("itemCode"))
          itemCode = value[0];
        else
        if(name.equals("custCode"))
          custCode = value[0];
        else
        if(name.equals("dateFrom"))
          dateFrom = value[0];
        else
        if(name.equals("dateTo"))
          dateTo = value[0];
      }
      
      if(q == null) q = "";
      if(s == null) s = "";
      if(p == null) p = "";
      if(d == null) d = "";
      if(i == null) i = "";
      if(r == null) r = "";
      if(itemCode == null) itemCode = "";
      if(custCode == null) custCode  = "";
      if(dateFrom == null) dateFrom = "";
      if(dateTo   == null) dateTo   = "";

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, custCode, itemCode, dateFrom, dateTo, q, s, p, d, i, r, bytesOut);
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

      System.out.println("1030a: " + e);
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "SalesLinesEnquiryExecute", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1030, bytesOut[0], 0, "ERR:" + custCode);
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den,
                    String dnm, String bnm, String custCode, String itemCode, String dateFrom, String dateTo, String q, String s,
                    String p, String d, String i, String r, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;
    
    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 1030, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "SalesLinesEnquiry", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1030, bytesOut[0], 0, "ACC:" + custCode);
    if(con  != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "SalesLinesEnquiry", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1030, bytesOut[0], 0, "SID:" + custCode);
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
    
    custCode = custCode.toUpperCase();
    itemCode = itemCode.toUpperCase();

    set(con, stmt, rs, out, req, unm, sid, uty, men, den, dnm, bnm, custCode, itemCode, dateFrom, dateTo, dateFromText, dateToText, q, s, p,
        d, i, r, imagesDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 1030, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con  != null) con.close();
    if(out != null) out.flush(); 
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den,
                   String dnm, String bnm, String custCode, String itemCode, String dateFrom, String dateTo, String dateFromText,
                   String dateToText, String q, String s, String p, String d, String i, String r, String imagesDir,
                   String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Sales Document Lines Enquiry</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4019, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function quote(code){");
      scoutln(out, bytesOut, "var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/QuotationPage?unm=" + unm + "&sid=" + sid + "&uty="
                           + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4031, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function so(code){");
      scoutln(out, bytesOut, "var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/SalesOrderPage?unm=" + unm + "&sid=" + sid + "&uty="
                           + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3038, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function pl(code){");
      scoutln(out, bytesOut, "var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/GoodsReceivedPickingList?unm=" + unm + "&sid=" + sid + "&uty="
                          + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4054, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function dorder(code){");
      scoutln(out, bytesOut, "var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/DeliveryOrderPage?unm=" + unm + "&sid=" + sid + "&uty="
                            + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4067, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function invoice(code){");
      scoutln(out, bytesOut, "var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/SalesInvoicePage?unm=" + unm + "&sid=" + sid + "&uty="
                           + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4080, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function proforma(code){");
      scoutln(out, bytesOut, "var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/ProformaInvoicePage?unm=" + unm + "&sid=" + sid + "&uty="
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
    
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "1030", "", "SalesLinesEnquiry", unm, sid, uty, men, den, dnm, bnm,
                          localDefnsDir, defnsDir, hmenuCount, bytesOut);

    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Sales Document Lines Enquiry Results", "1030", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<form><table id=\"page\" width=100% border=0>");
    
    if(custCode.length() == 0)
    {
      scoutln(out, bytesOut, "<br><br><span id=\"enquiryMessage\">&nbsp; No Customer Code Specified</span><br><br>");
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
        return;
      }
    }
    else
    if(custCode.length() == 0)
    {
      Customer customer = new Customer();
      if(! customer.existsCompanyRecGivenCode(con, stmt, rs, custCode, dnm, localDefnsDir, defnsDir))
      {
        scoutln(out, bytesOut, "<br><br><span id=\"enquiryMessage\">Customer Code &nbsp; &nbsp; " + custCode);
        scoutln(out, bytesOut, "&nbsp; &nbsp; <font color=\"#FF0000\">Not Found</span><br><br>");
        return;
      }
    }

    char dpOnQuantities = miscDefinitions.dpOnQuantities(con, stmt, rs, "5");
    
    if(q.equals("on"))
      forAllQuotes(con, stmt, rs, out, custCode, itemCode, dateFrom, dateTo, dpOnQuantities, dnm, imagesDir, localDefnsDir, defnsDir, bytesOut);
  
    if(s.equals("on"))
    {
      forAllSalesOrders(con, stmt, rs, out, custCode, itemCode, dateFrom, dateTo, dpOnQuantities, dnm, imagesDir, localDefnsDir, defnsDir,
                        bytesOut);
    }
  
    if(p.equals("on"))
    {
      forAllPickingLists(con, stmt, rs, out, custCode, itemCode, dateFrom, dateTo, dpOnQuantities, dnm, imagesDir, localDefnsDir, defnsDir,
                         bytesOut);
    }
  
    if(d.equals("on"))
      forAllDOs(con, stmt, rs, out, custCode, itemCode, dateFrom, dateTo, dpOnQuantities, dnm, imagesDir, localDefnsDir, defnsDir, bytesOut);
  
    if(i.equals("on"))
      forAllInvoices(con, stmt, rs, out, custCode, itemCode, dateFrom, dateTo, dpOnQuantities, dnm, imagesDir, localDefnsDir, defnsDir, bytesOut);
  
    if(r.equals("on"))
      forAllProformas(con, stmt, rs, out, custCode, itemCode, dateFrom, dateTo, dpOnQuantities, dnm, imagesDir, localDefnsDir, defnsDir, bytesOut);

    scoutln(out, bytesOut, "</table></form>");     scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void forAllQuotes(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String custCode, String itemCode, String dateFrom, String dateTo, char dpOnQuantities, String dnm, String imagesDir, String localDefnsDir, String defnsDir,
                            int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    String[] cssFormat = new String[1];  cssFormat[0] = "line1";

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr id=\"pageColumn\"><td nowrap colspan=10><p>Quotations:</td></tr>");
    scoutln(out, bytesOut, "<tr id=\"pageColumn\"><td></td>");
    scoutln(out, bytesOut, "<td><p> Document Code </td>");
    scoutln(out, bytesOut, "<td><p> Line </td>");
    scoutln(out, bytesOut, "<td><p> Date </td>");
    scoutln(out, bytesOut, "<td><p> Item Code </td>");
    scoutln(out, bytesOut, "<td><p> Manufacturer </td>");
    scoutln(out, bytesOut, "<td><p> Manufacturer Code </td>");
    scoutln(out, bytesOut, "<td><p> Unit Price </td>");
    scoutln(out, bytesOut, "<td><p> Quantity </td>");
    scoutln(out, bytesOut, "<td><p> Discount </td>");
    scoutln(out, bytesOut, "<td><p> Amount </td>");
    scoutln(out, bytesOut, "<td><p> Currency </td>");
    scoutln(out, bytesOut, "<td nowrap><p> Description </td>");
    scoutln(out, bytesOut, "<td nowrap><p> Reference </td></tr>");

    String itemCodeCriteria;
    if(itemCode.length() > 0)
      itemCodeCriteria = "AND ItemCode = '" + itemCode + "'";
    else itemCodeCriteria = "";
    
    rs = stmt.executeQuery("SELECT t1.QuoteCode, t1.QuoteDate, t1.Currency, t1.DocumentStatus, t1.EnquiryCode, t2.Description, t2.Quantity, t2.Line, t2.ItemCode, t2.UnitPrice, t2.Discount, t2.Amount2, t2.Manufacturer, t2.ManufacturerCode "
                         + "FROM quotel AS t2 INNER JOIN quote AS t1 ON t2.QuoteCode = t1.QuoteCode WHERE t1.CompanyCode = '" + custCode + "' AND QuoteDate >= {d '" + dateFrom + "'} AND QuoteDate <= {d '" + dateTo + "'} " + itemCodeCriteria
                         + " ORDER BY t1.QuoteDate, t1.QuoteCode, t2.Line");
      
    String code, date, currency, status, enquiryCode, unitPrice, discount, amount, desc, quantity, line, mfr, mfrCode;
    
    while(rs.next())
    {    
      code        = rs.getString(1);
      date        = rs.getString(2);
      currency    = rs.getString(3);
      status      = rs.getString(4);
      enquiryCode = rs.getString(5);
      desc        = rs.getString(6);
      quantity    = rs.getString(7);
      line        = rs.getString(8);
      itemCode    = rs.getString(9);
      unitPrice   = rs.getString(10);
      discount    = rs.getString(11);
      amount      = rs.getString(12);
      mfr         = rs.getString(13);
      mfrCode     = rs.getString(14);
      
      writeLine(out, dnm, "quote", code, line, date, enquiryCode, itemCode, mfr, mfrCode, quantity, desc, unitPrice, discount, amount, currency, status, dpOnQuantities, cssFormat, imagesDir, localDefnsDir, defnsDir, bytesOut);
    }
  
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void forAllSalesOrders(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String custCode, String itemCode,
                                 String dateFrom, String dateTo, char dpOnQuantities, String dnm, String imagesDir, 
                                 String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    String[] cssFormat = new String[1];  cssFormat[0] = "line1";

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr id=\"pageColumn\"><td nowrap colspan=10><p>Sales Orders:</td></tr>");
    scoutln(out, bytesOut, "<tr id=\"pageColumn\"><td></td>");
    scoutln(out, bytesOut, "<td><p> Document Code </td>");
    scoutln(out, bytesOut, "<td><p> Line </td>");
    scoutln(out, bytesOut, "<td><p> Date </td>");
    scoutln(out, bytesOut, "<td><p> Item Code </td>");
    scoutln(out, bytesOut, "<td><p> Manufacturer </td>");
    scoutln(out, bytesOut, "<td><p> Manufacturer Code </td>");
    scoutln(out, bytesOut, "<td><p> Unit Price </td>");
    scoutln(out, bytesOut, "<td><p> Quantity </td>");
    scoutln(out, bytesOut, "<td><p> Discount </td>");
    scoutln(out, bytesOut, "<td><p> Amount </td>");
    scoutln(out, bytesOut, "<td><p> Currency </td>");
    scoutln(out, bytesOut, "<td nowrap><p> Description </td>");
    scoutln(out, bytesOut, "<td nowrap><p> Customer PO Code </td></tr>");

    String itemCodeCriteria;
    if(itemCode.length() > 0)
      itemCodeCriteria = "AND ItemCode = '" + itemCode + "'";
    else itemCodeCriteria = "";
    
    rs = stmt.executeQuery("SELECT t1.SOCode, t1.Date, t1.CustomerPOCode, t1.Currency2, t1.Status, t2.Description, t2.Quantity, t2.Line, t2.ItemCode, t2.unitPrice, t2.discount, t2.amount2, t2.Manufacturer, t2.ManufacturerCode "
                         + "FROM sol AS t2 INNER JOIN so AS t1 ON t2.SOCode = t1.SOCode WHERE t1.CompanyCode = '" + custCode + "' AND t1.Date >= {d '" + dateFrom + "'} AND t1.Date <= {d '" + dateTo + "'} " + itemCodeCriteria
                         + " ORDER BY t1.Date, t1.SOCode");
      
    String code, date, custPOCode, currency, status, unitPrice, discount, amount, desc, quantity, line, mfr, mfrCode;
    
    while(rs.next())
    {    
      code       = rs.getString(1);
      date       = rs.getString(2);
      custPOCode = rs.getString(3);
      currency   = rs.getString(4);
      status     = rs.getString(5);
      desc       = rs.getString(6);
      quantity   = rs.getString(7);
      line       = rs.getString(8);
      itemCode   = rs.getString(9);
      unitPrice  = rs.getString(10);
      discount   = rs.getString(11);
      amount     = rs.getString(12);
      mfr     = rs.getString(13);
      mfrCode     = rs.getString(14);
      
      writeLine(out, dnm, "so", code, line, date, custPOCode, itemCode, mfr, mfrCode, quantity, desc, unitPrice, discount, amount, currency,
                status, dpOnQuantities, cssFormat, imagesDir, localDefnsDir, defnsDir, bytesOut);
    }
  
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void forAllPickingLists(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String custCode, String itemCode,
                                  String dateFrom, String dateTo, char dpOnQuantities, String dnm, String imagesDir, 
                                  String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    String[] cssFormat = new String[1];  cssFormat[0] = "line1";

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr id=\"pageColumn\"><td nowrap colspan=10><p>Picking Lists:</td></tr>");
    scoutln(out, bytesOut, "<tr id=\"pageColumn\"><td></td>");
    scoutln(out, bytesOut, "<td><p> Document Code </td>");
    scoutln(out, bytesOut, "<td><p> Line </td>");
    scoutln(out, bytesOut, "<td><p> Date </td>");
    scoutln(out, bytesOut, "<td><p> Item Code </td>");
    scoutln(out, bytesOut, "<td><p> Manufacturer </td>");
    scoutln(out, bytesOut, "<td><p> Manufacturer Code </td>");
    scoutln(out, bytesOut, "<td><p> Unit Price </td>");
    scoutln(out, bytesOut, "<td><p> Quantity </td>");
    scoutln(out, bytesOut, "<td><p> Discount </td>");
    scoutln(out, bytesOut, "<td><p> Amount </td>");
    scoutln(out, bytesOut, "<td><p> Currency </td>");
    scoutln(out, bytesOut, "<td nowrap><p> Description </td></tr>");

    String itemCodeCriteria;
    if(itemCode.length() > 0)
      itemCodeCriteria = "AND ItemCode = '" + itemCode + "'";
    else itemCodeCriteria = "";
    
    rs = stmt.executeQuery("SELECT t1.PLCode, t1.Date, t1.Currency, t1.Status, t2.Description, t2.QuantityPacked, t2.Line, "
                         + "t2.ItemCode, t2.UnitPrice, t2.Discount, t2.Amount2, t2.Manufacturer, t2.ManufacturerCode FROM pll AS t2 INNER JOIN pl AS t1 "
                         + "ON t2.PLCode = t1.PLCode WHERE t1.CompanyCode = '" + custCode + "' AND Date >= {d '" + dateFrom
                         + "'} AND Date <= {d '" + dateTo + "'} " + itemCodeCriteria + " ORDER BY t1.Date, t1.PLCode");
      
    String code, date, currency, status, unitPrice, discount, amount, desc, quantity, line, mfr, mfrCode;
    
    while(rs.next())
    {    
      code       = rs.getString(1);
      date       = rs.getString(2);
      currency   = rs.getString(3);
      status     = rs.getString(4);
      desc       = rs.getString(5);
      quantity   = rs.getString(6);
      line       = rs.getString(7);
      itemCode   = rs.getString(8);
      unitPrice  = rs.getString(9);
      discount   = rs.getString(10);
      amount     = rs.getString(11);
      mfr     = rs.getString(12);
      mfrCode     = rs.getString(13);
      
      writeLine(out, dnm, "pl", code, line, date, "", itemCode, mfr, mfrCode, quantity, desc, unitPrice, discount, amount, currency, status, 
                dpOnQuantities, cssFormat, imagesDir, localDefnsDir, defnsDir, bytesOut);
    }
  
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void forAllDOs(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String custCode, String itemCode,
                         String dateFrom, String dateTo, char dpOnQuantities, String dnm, String imagesDir, String localDefnsDir,
                         String defnsDir, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    String[] cssFormat = new String[1];  cssFormat[0] = "line1";

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr id=\"pageColumn\"><td nowrap colspan=10><p>Delivery Orders:</td></tr>");
    scoutln(out, bytesOut, "<tr id=\"pageColumn\"><td></td>");
    scoutln(out, bytesOut, "<td><p> Document Code </td>");
    scoutln(out, bytesOut, "<td><p> Line </td>");
    scoutln(out, bytesOut, "<td><p> Date </td>");
    scoutln(out, bytesOut, "<td><p> Item Code </td>");
    scoutln(out, bytesOut, "<td><p> Manufacturer </td>");
    scoutln(out, bytesOut, "<td><p> Manufacturer Code </td>");
    scoutln(out, bytesOut, "<td><p> Unit Price </td>");
    scoutln(out, bytesOut, "<td><p> Quantity </td>");
    scoutln(out, bytesOut, "<td><p> Discount </td>");
    scoutln(out, bytesOut, "<td><p> Amount </td>");
    scoutln(out, bytesOut, "<td><p> Currency </td>");
    scoutln(out, bytesOut, "<td nowrap><p> Description </td>");
    scoutln(out, bytesOut, "<td nowrap><p> Customer PO Code </td></tr>");

    String itemCodeCriteria;
    if(itemCode.length() > 0)
      itemCodeCriteria = "AND ItemCode = '" + itemCode + "'";
    else itemCodeCriteria = "";
    
    rs = stmt.executeQuery("SELECT t1.DOCode, t1.Date, t1.PORefNum, t1.Currency, t1.Status, t2.Description, t2.Quantity, t2.Line, "
                         + "t2.ItemCode, t2.UnitPrice, t2.Discount, t2.Amount2, t2.Manufacturer, t2.ManufacturerCode FROM dol AS t2 INNER JOIN do AS t1 "
                         + "ON t2.DOCode = t1.DOCode WHERE t1.CompanyCode = '" + custCode + "' AND Date >= {d '" + dateFrom
                         + "'} AND Date <= {d '" + dateTo + "'} " + itemCodeCriteria + " ORDER BY t1.Date, t1.DOCode");
      
    String code, date, custPOCode, currency, status, unitPrice, discount, amount, desc, quantity, line, mfr, mfrCode;
    
    while(rs.next())
    {    
      code       = rs.getString(1);
      date       = rs.getString(2);
      custPOCode = rs.getString(3);
      currency   = rs.getString(4);
      status     = rs.getString(5);
      desc       = rs.getString(6);
      quantity   = rs.getString(7);
      line       = rs.getString(8);
      itemCode   = rs.getString(9);
      unitPrice  = rs.getString(10);
      discount   = rs.getString(11);
      amount     = rs.getString(12);
      mfr     = rs.getString(13);
      mfrCode     = rs.getString(14);
      
      writeLine(out, dnm, "dorder", code, line, date, custPOCode, itemCode, mfr, mfrCode, quantity, desc, unitPrice, discount, amount, currency,
                status, dpOnQuantities, cssFormat, imagesDir, localDefnsDir, defnsDir, bytesOut);
    }
  
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }
 
  // -------------------------------------------------------------------------------------------------------------------------------
  private void forAllInvoices(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String custCode, String itemCode,
                              String dateFrom, String dateTo, char dpOnQuantities, String dnm, String imagesDir,
                              String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    String[] cssFormat = new String[1];  cssFormat[0] = "line1";

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr id=\"pageColumn\"><td nowrap colspan=10><p>Sales Invoices:</td></tr>");
    scoutln(out, bytesOut, "<tr id=\"pageColumn\"><td></td>");
    scoutln(out, bytesOut, "<td><p> Document Code </td>");
    scoutln(out, bytesOut, "<td><p> Line </td>");
    scoutln(out, bytesOut, "<td><p> Date </td>");
    scoutln(out, bytesOut, "<td><p> Item Code </td>");
    scoutln(out, bytesOut, "<td><p> Manufacturer </td>");
    scoutln(out, bytesOut, "<td><p> Manufacturer Code </td>");
    scoutln(out, bytesOut, "<td><p> Unit Price </td>");
    scoutln(out, bytesOut, "<td><p> Quantity </td>");
    scoutln(out, bytesOut, "<td><p> Discount </td>");
    scoutln(out, bytesOut, "<td><p> Amount </td>");
    scoutln(out, bytesOut, "<td><p> Currency </td>");
    scoutln(out, bytesOut, "<td nowrap><p> Description </td>");
    scoutln(out, bytesOut, "<td nowrap><p> Customer PO Code </td></tr>");

    String itemCodeCriteria;
    if(itemCode.length() > 0)
      itemCodeCriteria = "AND ItemCode = '" + itemCode + "'";
    else itemCodeCriteria = "";
    
    rs = stmt.executeQuery("SELECT t1.InvoiceCode, t1.Date, t1.PORefNum, t1.Currency, t1.Status, t2.Description, t2.Quantity, "
                         + "t2.Line, t2.ItemCode, t2.UnitPrice, t2.Discount, t2.Amount, t2.Manufacturer, t2.ManufacturerCode FROM invoicel AS t2 INNER JOIN invoice AS t1 "
                         + "ON t2.InvoiceCode = t1.InvoiceCode WHERE t1.CompanyCode = '" + custCode + "' AND Date >= {d '" + dateFrom
                         + "'} AND Date <= {d '" + dateTo + "'} " + itemCodeCriteria + " ORDER BY t1.Date, t1.InvoiceCode");
      
    String code, date, custPOCode, currency, status, unitPrice, discount, amount, desc, quantity, line, mfr, mfrCode;
    
    while(rs.next())
    {    
      code       = rs.getString(1);
      date       = rs.getString(2);
      custPOCode = rs.getString(3);
      currency   = rs.getString(4);
      status     = rs.getString(5);
      desc       = rs.getString(6);
      quantity   = rs.getString(7);
      line       = rs.getString(8);
      itemCode   = rs.getString(9);
      unitPrice  = rs.getString(10);
      discount   = rs.getString(11);
      amount     = rs.getString(12);
      mfr     = rs.getString(13);
      mfrCode     = rs.getString(14);
      
      writeLine(out, dnm, "invoice", code, line, date, custPOCode, itemCode, mfr, mfrCode, quantity, desc, unitPrice, discount, amount, currency,
                status, dpOnQuantities, cssFormat, imagesDir, localDefnsDir, defnsDir, bytesOut);
    }
  
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void forAllProformas(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String custCode, String itemCode,
                               String dateFrom, String dateTo, char dpOnQuantities, String dnm, String imagesDir,
                               String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    String[] cssFormat = new String[1];  cssFormat[0] = "line1";

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr id=\"pageColumn\"><td nowrap colspan=10><p>Proforma Invoices:</td></tr>");
    scoutln(out, bytesOut, "<tr id=\"pageColumn\"><td></td>");
    scoutln(out, bytesOut, "<td><p> Document Code </td>");
    scoutln(out, bytesOut, "<td><p> Line </td>");
    scoutln(out, bytesOut, "<td><p> Date </td>");
    scoutln(out, bytesOut, "<td><p> Item Code </td>");
    scoutln(out, bytesOut, "<td><p> Manufacturer </td>");
    scoutln(out, bytesOut, "<td><p> Manufacturer Code </td>");
    scoutln(out, bytesOut, "<td><p> Unit Price </td>");
    scoutln(out, bytesOut, "<td><p> Quantity </td>");
    scoutln(out, bytesOut, "<td><p> Discount </td>");
    scoutln(out, bytesOut, "<td><p> Amount </td>");
    scoutln(out, bytesOut, "<td><p> Currency </td>");
    scoutln(out, bytesOut, "<td nowrap><p> Description </td>");
    scoutln(out, bytesOut, "<td nowrap><p> Customer PO Code </td></tr>");

    String itemCodeCriteria;
    if(itemCode.length() > 0)
      itemCodeCriteria = "AND ItemCode = '" + itemCode + "'";
    else itemCodeCriteria = "";
    
    rs = stmt.executeQuery("SELECT t1.ProformaCode, t1.Date, t1.PORefNum, t1.Currency, t1.Status, t2.Description, t2.Quantity, t2.Line, "
                         + "t2.ItemCode, t2.unitPrice, t2.discount, t2.amount, t2.Manufacturer, t2.ManufacturerCode FROM proformal AS t2 INNER JOIN proforma AS t1 "
                         + "ON t2.ProformaCode = t1.ProformaCode WHERE t1.CompanyCode = '" + custCode + "' AND Date >= {d '" + dateFrom
                         + "'} AND Date <= {d '" + dateTo + "'} " + itemCodeCriteria + " ORDER BY t1.Date, t1.ProformaCode");
      
    String code, date, custPOCode, currency, status, unitPrice, discount, amount, desc, quantity, line, mfr, mfrCode;
    
    while(rs.next())
    {    
      code       = rs.getString(1);
      date       = rs.getString(2);
      custPOCode = rs.getString(3);
      currency   = rs.getString(4);
      status     = rs.getString(5);
      desc       = rs.getString(6);
      quantity   = rs.getString(7);
      line       = rs.getString(8);
      itemCode   = rs.getString(9);
      unitPrice  = rs.getString(10);
      discount   = rs.getString(11);
      amount     = rs.getString(12);
      mfr     = rs.getString(13);
      mfrCode     = rs.getString(14);
      
      writeLine(out, dnm, "proforma", code, line, date, custPOCode, itemCode, mfr, mfrCode, quantity, desc, unitPrice, discount, amount, currency,
                status, dpOnQuantities, cssFormat, imagesDir, localDefnsDir, defnsDir, bytesOut);
    }
  
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void writeLine(PrintWriter out, String dnm, String jsName, String code, String line, String date, String custPOCode, String itemCode, String mfr, String mfrCode, String quantity, String desc, String unitPrice, String discount,
                         String amount, String currency, String status, char dpOnQuantities, String[] cssFormat, String imagesDir, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
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

    if(mfr == null) mfr = "";
    if(mfrCode == null) mfrCode = "";
    
    scout(out, bytesOut, "<td><p><a href=\"javascript:" + jsName + "('" + code + "')\">" + code + "</td>");
    scout(out, bytesOut, "<td align=center><p>" + line + "</td>");
    scout(out, bytesOut, "<td><p>" + generalUtils.convertFromYYYYMMDD(date) + "</td>");
    scout(out, bytesOut, "<td><p><a href=\"javascript:item('" + itemCode + "')\">" + itemCode + "</a></td>");
    scout(out, bytesOut, "<td><p>" + mfr + "</td>");
    scout(out, bytesOut, "<td><p>" + mfrCode + "</td>");
    scout(out, bytesOut, "<td align=right><p>" + generalUtils.doubleDPs(unitPrice, '2') + "</td>");
    scout(out, bytesOut, "<td align=center><p>" + generalUtils.stringFromBytes(qty, 0L) + "</td>");
    scout(out, bytesOut, "<td align=right><p>" + generalUtils.doubleDPs(discount, '2') + "</td>");
    scout(out, bytesOut, "<td align=right><p>" + generalUtils.doubleDPs(amount, '2') + "</td>");
    scout(out, bytesOut, "<td><p>" + currency + "</td>");
    scout(out, bytesOut, "<td nowrap><p>" + desc + "</td>");
    scout(out, bytesOut, "<td><p>" + custPOCode + "</td></tr>");
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

