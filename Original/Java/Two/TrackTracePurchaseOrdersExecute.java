// =======================================================================================================================================================================================================
// System: ZaraStar: TNT: Purchase Orders
// Module: TrackTracePurchaseOrdersExecute.java
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
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;
import java.io.*;

public class TrackTracePurchaseOrdersExecute extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  MiscDefinitions miscDefinitions = new MiscDefinitions();
  GoodsReceivedNote goodsReceivedNote = new GoodsReceivedNote();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
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
      p1  = req.getParameter("p1"); // poCode
      
      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "",
           "", "1", bytesOut);
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
 
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "TrackTracePurchaseOrders", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2022, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush(); 
    }
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", poCode="", itemCode1="", itemCode2="", suppCode="",
           dateFrom1="", dateFrom2="", dateFrom3="", dateFrom4="", dateTo1="", dateTo2="", dateTo3="", dateTo4="", dateType1="",
           dateType2="", dateType3="", osOnly1="N", osOnly2="N", osOnly3="N", osOnly4="N", which="";

    try
    {
      directoryUtils.setContentHeaders(res);
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
        if(name.equals("poCode"))
          poCode = value[0];
        else
        if(name.equals("itemCode1"))
          itemCode1 = value[0];
        else
        if(name.equals("itemCode2"))
          itemCode2 = value[0];
        else
        if(name.equals("suppCode"))
          suppCode = value[0];
        else
        if(name.equals("dateFrom1"))
          dateFrom1 = value[0];
        else
        if(name.equals("dateFrom2"))
          dateFrom2 = value[0];
        else
        if(name.equals("dateFrom3"))
          dateFrom3 = value[0];
        else
        if(name.equals("dateFrom4"))
          dateFrom4 = value[0];
        else
        if(name.equals("dateTo1"))
          dateTo1 = value[0];
        else
        if(name.equals("dateTo2"))
          dateTo2 = value[0];
        else
        if(name.equals("dateTo3"))
          dateTo3 = value[0];
        else
        if(name.equals("dateTo4"))
          dateTo4 = value[0];
        else
        if(name.equals("dateType1"))
          dateType1 = value[0];
        else
        if(name.equals("dateType2"))
          dateType2 = value[0];
        else
        if(name.equals("dateType3"))
          dateType3 = value[0];
        else
        if(name.equals("osOnly1"))
          osOnly1 = value[0];
        else
        if(name.equals("osOnly2"))
          osOnly2 = value[0];
        else
        if(name.equals("osOnly3"))
          osOnly3 = value[0];
        else
        if(name.equals("osOnly4"))
          osOnly4 = value[0];
        else
        if(name.equals("which"))
          which = value[0];
      }
      
      if(poCode    == null) poCode    = "";
      if(itemCode1 == null) itemCode1 = "";
      if(itemCode2 == null) itemCode2 = "";
      if(suppCode  == null) suppCode  = "";
      if(dateFrom1 == null) dateFrom1 = "";
      if(dateFrom2 == null) dateFrom2 = "";
      if(dateFrom3 == null) dateFrom3 = "";
      if(dateFrom4 == null) dateFrom4 = "";
      if(dateTo1   == null) dateTo1   = "";
      if(dateTo2   == null) dateTo2   = "";
      if(dateTo3   == null) dateTo3   = "";
      if(dateTo4   == null) dateTo4   = "";
      if(dateType1 == null) dateType1 = "";
      if(dateType2 == null) dateType2 = "";
      if(dateType3 == null) dateType3 = "";
      if(osOnly1   == null) osOnly1   = "";
      if(osOnly2   == null) osOnly2   = "";
      if(osOnly3   == null) osOnly3   = "";
      if(osOnly4   == null) osOnly4   = "";

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, poCode, itemCode1, itemCode2, suppCode, dateFrom1, dateFrom2, 
           dateFrom3, dateFrom4, dateTo1, dateTo2, dateTo3, dateTo4, dateType1, dateType2, dateType3, osOnly1, osOnly2, osOnly3,
           osOnly4, which, bytesOut);
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

      System.out.println("2022a: " + e);
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "TrackTracePurchaseOrdersa", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2022, bytesOut[0], 0, "ERR:" + poCode);
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                    String poCode, String itemCode1, String itemCode2, String suppCode, String dateFrom1, String dateFrom2, String dateFrom3,
                    String dateFrom4, String dateTo1, String dateTo2, String dateTo3, String dateTo4, String dateType1, String dateType2,
                    String dateType3, String osOnly1, String osOnly2, String osOnly3, String osOnly4, String which, int[] bytesOut) throws Exception
  {
    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    
    long startTime = new java.util.Date().getTime();

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null, stmt2 = null, stmt3 = null;
    ResultSet rs   = null, rs2 = null, rs3 = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 2022, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "TrackTracePurchaseOrders", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2022, bytesOut[0], 0, "ACC:" + poCode);
      if(con  != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "TrackTracePurchaseOrders", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2022, bytesOut[0], 0, "SID:" + poCode);
      if(con  != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    set(con, stmt, stmt2, stmt3, rs, rs2, rs3, out, req, unm, sid, uty, men, den, dnm, bnm, poCode, itemCode1, itemCode2, suppCode, dateFrom1,
        dateFrom2, dateFrom3, dateFrom4, dateTo1, dateTo2, dateTo3, dateTo4, dateType1, dateType2, dateType3, osOnly1, osOnly2, osOnly3, osOnly4,
        which, imagesDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 2022, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con  != null) con.close();
    if(out != null) out.flush(); 
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, ResultSet rs3, PrintWriter out,
                   HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String poCode,
                   String itemCode1, String itemCode2, String suppCode, String dateFrom1, String dateFrom2, String dateFrom3, String dateFrom4,
                   String dateTo1, String dateTo2, String dateTo3, String dateTo4, String dateType1, String dateType2, String dateType3,
                   String osOnly1, String osOnly2, String osOnly3, String osOnly4, String which, String imagesDir, String localDefnsDir,
                   String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Track &amp; Trace: Purchase Orders</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 5006, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewPO(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/PurchaseOrderPage?unm=" + unm + "&sid=" + sid + "&uty="
                             + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }
  
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3025, unm, uty, dnm, localDefnsDir, defnsDir))
    {
       scoutln(out, bytesOut, "function viewGRN(code){var p1=sanitise(code);");
       scoutln(out, bytesOut, "this.location.href=\"/central/servlet/GoodsReceivedPage?unm=" + unm + "&sid=" + sid + "&uty="
                            + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3001, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewItem(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/ProductStockRecord?unm=" + unm + "&sid=" + sid + "&uty="
                           + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 5001, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewSupp(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/SupplierPage?unm=" + unm + "&sid=" + sid + "&uty="
                           + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p1=\"+p1;}");
    }

    scoutln(out, bytesOut, "function viewLines(code){var p1=sanitise(code);");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/TrackTracePurchaseOrdersa?unm=" + unm + "&sid=" + sid + "&uty="
                         + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p1=\"+p1;}");


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
 
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "2022", "", "TrackTracePurchaseOrders", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount,
                          bytesOut);

    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Track &amp; Trace: Purchase Orders Results", "2022", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<form><table id=\"page\" width=100% border=0>");

    boolean error = false;
    if(which.equals("1") && poCode.length() == 0)
    {
      scoutln(out, bytesOut, "<br><br><span id=\"enquiryMessage\">&nbsp; No Purchase Order Code Specified</span><br><br>");
      error = true;
    }
    else
    if(which.equals("2"))
    {
      if(itemCode1.length() == 0)
      {
        scoutln(out, bytesOut, "<br><br><span id=\"enquiryMessage\">&nbsp; No Stock Item Code Specified</span><br><br>");
        error = true;
      }
      else
      {
        Inventory inventory = new Inventory();
        if(! inventory.existsItemRecGivenCode(con, stmt, rs, itemCode1))
        {
          scoutln(out, bytesOut, "<br><br><span id=\"enquiryMessage\">Item Code &nbsp; &nbsp; " + itemCode1);
          scoutln(out, bytesOut, "&nbsp; &nbsp; <font color=\"#FF0000\">Not Found</span><br><br>");
          error = true;
        }
      }
    }
    else
    if(which.equals("3"))
    {
      if(suppCode.length() == 0)
      {
        scoutln(out, bytesOut, "<br><br><span id=\"enquiryMessage\">&nbsp; No Supplier Code Specified</span><br><br>");
        error = true;
      }
      else
      {
        Supplier supplier = new Supplier();
        if(! supplier.existsSupplierRecGivenCode(con, stmt, rs, suppCode, dnm, localDefnsDir, defnsDir))
        {
          scoutln(out, bytesOut, "<br><br><span id=\"enquiryMessage\">Supplier Code &nbsp; &nbsp; " + suppCode);
          scoutln(out, bytesOut, "&nbsp; &nbsp; <font color=\"#FF0000\">Not Found</span><br><br>");
          error = true;
        }
      }
    }
    else
    if((which.equals("4") || which.equals("41")) && (dateFrom3.length() == 0 || dateTo3.length() == 0))
    {
      if(dateFrom3.length() == 0)
        dateFrom3 = "01.01.1970";
      
      if(dateTo3.length() == 0)
        dateTo3 = "31.12.2099";
    }
    else
    if(which.equals("5") && (dateFrom4.length() == 0 || dateTo4.length() == 0))
    {
      if(dateFrom4.length() == 0)
        dateFrom4 = "01.01.1970";
      
      if(dateTo4.length() == 0)
        dateTo4 = "31.12.2099";
    }

    if(error)
    {
      scoutln(out, bytesOut, "</table></form>");     scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
      return;
    }
    
    scoutln(out, bytesOut, "<tr><td></td></tr><tr id=\"pageColumn\"><td></td>");

    if(which.equals("5"))
    {
      scoutln(out, bytesOut, "<td><p><b> Supplier Code </b></td>");
      scoutln(out, bytesOut, "<td><p><b> PO Code </b></td>");
      scoutln(out, bytesOut, "<td><p><b> PO Date </b></td>");
      scoutln(out, bytesOut, "<td><p><b> Quantity<br>Required </b></td>");
      scoutln(out, bytesOut, "<td><p><b> Quantity<br>Outstanding </b></td><td></td></tr>");
    }
    else
    {
      scoutln(out, bytesOut, "<td></td><td><p><b> Supplier Code </b></td>");
      scoutln(out, bytesOut, "<td><p><b> Required-by Date </b></td>");
      scoutln(out, bytesOut, "<td><p><b> Confirmed Date </b></td>");
      scoutln(out, bytesOut, "<td><p><b> PO Code </b></td>");
      scoutln(out, bytesOut, "<td><p><b> PO Line </b></td>");
      scoutln(out, bytesOut, "<td><p><b> PO Date </b></td>");
      scoutln(out, bytesOut, "<td><p><b> SO Code </b></td>");
      scoutln(out, bytesOut, "<td><p><b> GRN </b></td>");
      scoutln(out, bytesOut, "<td><p><b> ItemCode </b></td>");
      if(which.equals("3"))
      {
        scoutln(out, bytesOut, "<td><p><b> Manufacturer </b></td>");
        scoutln(out, bytesOut, "<td><p><b> MfrCode </b></td>");
      }
      scoutln(out, bytesOut, "<td><p><b> Quantity<br>Ordered </b></td>");
      scoutln(out, bytesOut, "<td><p><b> Quantity<br>Outstanding </b></td>");
      scoutln(out, bytesOut, "<td nowrap><p><b> Description </b></td></tr>");
    }
    
    if(dateFrom1.length() != 0) dateFrom1 = generalUtils.convertDateToSQLFormat(dateFrom1);
    if(dateFrom2.length() != 0) dateFrom2 = generalUtils.convertDateToSQLFormat(dateFrom2);
    if(dateFrom3.length() != 0) dateFrom3 = generalUtils.convertDateToSQLFormat(dateFrom3);
    if(dateFrom4.length() != 0) dateFrom4 = generalUtils.convertDateToSQLFormat(dateFrom4);
    if(dateTo1.length()   != 0) dateTo1   = generalUtils.convertDateToSQLFormat(dateTo1);
    if(dateTo2.length()   != 0) dateTo2   = generalUtils.convertDateToSQLFormat(dateTo2);
    if(dateTo3.length()   != 0) dateTo3   = generalUtils.convertDateToSQLFormat(dateTo3);
    if(dateTo4.length()   != 0) dateTo4   = generalUtils.convertDateToSQLFormat(dateTo4);
        
    if(dateType1.length() == 0) dateType1 = "I";
    if(dateType2.length() == 0) dateType2 = "I";
    if(dateType3.length() == 0) dateType3 = "I";

    String[] cssFormat = new String[1];  cssFormat[0] = "line1";
    int[] oCount = new int[1];  oCount[0] = 1;
    char dpOnQuantities = miscDefinitions.dpOnQuantities(con, stmt, rs, "5");
    
    if(which.equals("1"))
    {
      fetchPOGivenPOCode(con, stmt, stmt2, rs, rs2, out, poCode, dpOnQuantities, cssFormat, oCount, dnm, imagesDir, localDefnsDir, defnsDir,
                         bytesOut);
    }
    else
    if(which.equals("2"))
    {
      fetchPOsGivenItemCode(con, stmt, stmt2, rs, rs2, out, itemCode1, dateFrom1, dateTo1, dateType1, osOnly1, dpOnQuantities, cssFormat, oCount, dnm,
                            localDefnsDir, defnsDir, bytesOut);
    }
    else
    if(which.equals("3"))
    {
      forPurchaseOrdersForASupplier(con, stmt, stmt2, rs, rs2, out, suppCode, itemCode2, dateFrom2, dateTo2, dateType2, osOnly2, dpOnQuantities,
                                    cssFormat, oCount, dnm, localDefnsDir, defnsDir, bytesOut);
    }
    else
    if(which.equals("4") || which.equals("41"))
    {
      forAllPurchaseOrders(con, stmt, stmt2, rs, rs2, out, which, dateFrom3, dateTo3, dateType3, osOnly3, dpOnQuantities, cssFormat, oCount, dnm,
                           localDefnsDir, defnsDir, bytesOut);
    }
    else
    if(which.equals("5"))
    {
      forAllPurchaseOrderHeaders(con, stmt, stmt2, stmt3, rs, rs2, rs3, out, dateFrom4, dateTo4, osOnly4, dpOnQuantities, cssFormat, oCount, dnm,
                                 localDefnsDir, defnsDir, bytesOut);
    }
    
    scoutln(out, bytesOut, "</table></form>");     scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void forAllPurchaseOrders(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, String which,
                                    String dateFrom, String dateTo, String dateType, String outstandingOnly, char dpOnQuantities, String[] cssFormat,
                                    int[] oCount, String dnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    String orderBy;
    if(which.equals("4"))
      orderBy = "t1.Date, t2.POCode, t2.Line";
    else orderBy = "t2.DateConfirmed, t2.POCode, t2.Line";
    
    String where;
    
    if(dateFrom.length() != 0 || dateTo.length() != 0)
    {
      if(dateFrom.length() == 0)
        dateFrom = "1970-01-01";
      else
      if(dateTo.length() == 0)
        dateTo = "2099-12-31";
      
      if(dateType.equals("I")) // issuedDate
      {
        where = "t1.Date >= {d '" + dateFrom + "'} AND t1.Date <= {d '" + dateTo + "'}";
      }
      else // reqdDate
      {
        where = "t2.RequiredBy >= {d '" + dateFrom + "'} AND t2.RequiredBy <= {d '" + dateTo + "'}";
      }
    }
    else // no date specified
    {
      where = "";
    }
    
    rs = stmt.executeQuery("SELECT t1.poCode, t1.Date, t1.CompanyCode, t1.AllReceived, t2.RequiredBy, t2.Description, t2.Quantity, t2.Line, "
                         + "t2.ItemCode, t2.SOCode, t2.DateConfirmed FROM pol AS t2 INNER JOIN po AS t1 ON t2.poCode = t1.poCode WHERE "
                         + "t1.Status != 'C' AND " + where + " ORDER BY " + orderBy);
      
    String poCode, date, supplierCode, allReceived, reqdBy, desc, quantity, line, itemCode, soCode, dateConfirmed;
    
    while(rs.next())
    {    
      poCode       = rs.getString(1);
      date         = rs.getString(2);
      supplierCode = rs.getString(3);
      allReceived  = rs.getString(4);
      reqdBy       = rs.getString(5);
      desc         = rs.getString(6);
      quantity     = rs.getString(7);
      line         = rs.getString(8);
      itemCode     = rs.getString(9);
      soCode       = rs.getString(10);
      dateConfirmed = rs.getString(11);
      
      if(allReceived.equals("Y") && outstandingOnly.equals("on"))
        ;
      else
      {
        processPOLine(con, stmt2, rs2, out, dnm, poCode, line, date, soCode, supplierCode, itemCode, "","",quantity, reqdBy, dateConfirmed, desc,
                      outstandingOnly, "", dpOnQuantities, cssFormat, "", localDefnsDir, defnsDir, oCount, bytesOut);
      }
    }
  
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void forPurchaseOrdersForASupplier(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, String suppCode, String itemCode, String dateFrom, String dateTo,
                                             String dateType, String outstandingOnly, char dpOnQuantities, String[] cssFormat, int[] oCount,
                                             String dnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    suppCode = suppCode.toUpperCase();
    itemCode = itemCode.toUpperCase();

    String where;
    
    if(dateFrom.length() != 0 || dateTo.length() != 0)
    {
      if(dateFrom.length() == 0)
        dateFrom = "1970-01-01";
      else
      if(dateTo.length() == 0)
        dateTo = "2099-12-31";
      
      if(dateType.equals("I")) // issuedDate
      {
        if(itemCode.length() == 0)
          where = "AND t1.Date >= {d '" + dateFrom + "'} AND t1.Date <= {d '" + dateTo + "'}";
        else where = "AND t2.ItemCode = '" + itemCode + "' AND t1.Date >= {d '" + dateFrom + "'} AND t1.Date <= {d '" + dateTo + "'}";
      }
      else // deliveryDate
      {
        if(itemCode.length() == 0)
          where = "AND t2.RequiredBy >= {d '" + dateFrom + "'} AND t2.RequiredBy <= {d '" + dateTo + "'}";
        else 
        {
          where = "AND t2.ItemCode = '" + itemCode + "' AND t2.RequiredBy >= {d '" + dateFrom + "'} AND t2.RequiredBy <= {d '"
                + dateTo + "'}";
        }
      }
    }
    else // no date specified
    {
      if(itemCode.length() == 0)
        where = "";
      else where = "AND t2.ItemCode = '" + itemCode + "'";
    }
    
    rs = stmt.executeQuery("SELECT t1.poCode, t1.Date, t1.AllReceived, t2.RequiredBy, t2.Description, t2.Quantity, t2.Line, "
                         + "t2.ItemCode, t2.SOCode, t2.DateConfirmed, t2.Manufacturer, t2.ManufacturerCode FROM pol AS t2 INNER JOIN po AS t1 ON t2.poCode = t1.poCode WHERE t1.Status != 'C' AND "
                         + "t1.CompanyCode = '" + suppCode + "' " + where + " ORDER BY t1.Date");
      
    String poCode, date, allReceived, reqdBy, desc, quantity, line, soCode, dateConfirmed, mfr, mfrCode;
    
    while(rs.next())
    {    
      poCode      = rs.getString(1);
      date        = rs.getString(2);
      allReceived = rs.getString(3);
      reqdBy      = rs.getString(4);
      desc        = rs.getString(5);
      quantity    = rs.getString(6);
      line        = rs.getString(7);
      itemCode    = rs.getString(8);
      soCode      = rs.getString(9);
      dateConfirmed = rs.getString(10);
      mfr         = rs.getString(11);
      mfrCode     = rs.getString(12);
      
      if(allReceived.equals("Y") && outstandingOnly.equals("on"))
        ;
      else
      {
        processPOLine(con, stmt2, rs2, out, dnm, poCode, line, date, soCode, suppCode, itemCode, mfr, mfrCode, quantity, reqdBy, dateConfirmed, desc,
                      outstandingOnly, "", dpOnQuantities, cssFormat, "", localDefnsDir, defnsDir, oCount, bytesOut);
      }
    }
  
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void fetchPOGivenPOCode(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, String poCode,
                                  char dpOnQuantities, String[] cssFormat, int[] oCount, String dnm, String imagesDir, String localDefnsDir,
                                  String defnsDir, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    poCode = poCode.toUpperCase();

    rs = stmt.executeQuery("SELECT t1.Date, t1.CompanyCode, t2.RequiredBy, t2.Description, t2.Quantity, t2.Line, "
                                   + "t2.ItemCode, t2.SOCode, t1.Status, t2.DateConfirmed FROM pol AS t2 INNER JOIN po AS t1 ON t2.poCode = t1.poCode "
                                   + "WHERE t2.poCode = '" + poCode + "' ORDER BY t1.Date");

    String date, supplierCode, reqdBy, desc, quantity, line, itemCode, status, soCode, dateConfirmed;

    while(rs.next())
    {    
      date         = rs.getString(1);
      supplierCode = rs.getString(2);
      reqdBy       = rs.getString(3);
      desc         = rs.getString(4);
      quantity     = rs.getString(5);
      line         = rs.getString(6);
      itemCode     = rs.getString(7);
      soCode       = rs.getString(8);
      status       = rs.getString(9);
      dateConfirmed = rs.getString(10);
      
      processPOLine(con, stmt2, rs2, out, dnm, poCode, line, date, soCode, supplierCode, itemCode, "","",quantity, reqdBy, dateConfirmed, desc, "", status,
                    dpOnQuantities, cssFormat, imagesDir, localDefnsDir, defnsDir, oCount, bytesOut);
    }
  
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void fetchPOsGivenItemCode(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, String itemCode, String dateFrom, String dateTo,
                                     String dateType, String outstandingOnly, char dpOnQuantities, String[] cssFormat, int[] oCount,
                                     String dnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    itemCode = itemCode.toUpperCase();

    String where;
    
    if(dateFrom.length() != 0 || dateTo.length() != 0)
    {
      if(dateFrom.length() == 0)
        dateFrom = "1970-01-01";
      else
      if(dateTo.length() == 0)
        dateTo = "2099-12-31";
      
      if(dateType.equals("I")) // issuedDate
      {
        if(itemCode.length() == 0)
          where = "t1.Date >= {d '" + dateFrom + "'} AND t1.Date <= {d '" + dateTo + "'}";
        else where = "t2.ItemCode = '" + itemCode + "' AND t1.Date >= {d '" + dateFrom + "'} AND t1.Date <= {d '" + dateTo + "'}";
      }
      else // reqdBy
      {
        if(itemCode.length() == 0)
          where = "t2.RequiredBy >= {d '" + dateFrom + "'} AND t2.RequiredBy <= {d '" + dateTo + "'}";
        else 
        {
          where = "t2.ItemCode = '" + itemCode + "' AND t2.RequiredBy >= {d '" + dateFrom + "'} AND t2.RequiredBy <= {d '"
                + dateTo + "'}";
        }
      }
    }
    else // no date specified
    {
      if(itemCode.length() == 0)
        where = "";
      else where = "t2.ItemCode = '" + itemCode + "'";
    }
    
    rs = stmt.executeQuery("SELECT t1.poCode, t1.Date, t1.CompanyCode, t1.AllReceived, t2.RequiredBy, t2.Description, t2.Quantity, "
                         + "t2.Line, t2.SOCode, t2.DateConfirmed FROM pol AS t2 INNER JOIN po AS t1 ON t2.poCode = t1.poCode WHERE Status != 'C' AND "
                         + where + " ORDER BY t1.Date");
      
    String poCode, date, supplierCode, allReceived, reqdBy, desc, quantity, line, soCode, dateConfirmed;
    
    while(rs.next())
    {    
      poCode       = rs.getString(1);
      date         = rs.getString(2);
      supplierCode = rs.getString(3);
      allReceived  = rs.getString(4);
      reqdBy       = rs.getString(5);
      desc         = rs.getString(6);
      quantity     = rs.getString(7);
      line         = rs.getString(8);
      soCode       = rs.getString(9);
      dateConfirmed = rs.getString(10);
      
      if(allReceived.equals("Y") && outstandingOnly.equals("on"))
        ;
      else
      {
        processPOLine(con, stmt2, rs2, out, dnm, poCode, line, date, soCode, supplierCode, itemCode, "","",quantity, reqdBy, dateConfirmed, desc,
                      outstandingOnly, "", dpOnQuantities, cssFormat, "", localDefnsDir, defnsDir, oCount, bytesOut);
      }
    }
  
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void processPOLine(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String dnm, String poCode, String poLine, String poDate,
                             String soCode, String supplierCode, String itemCode, String mfr, String mfrCode, String quantity, String reqdBy, String dateConfirmed, String desc,
                             String outstandingOnly, String status, char dpOnQuantities, String[] cssFormat, String imagesDir,
                             String localDefnsDir, String defnsDir, int[] oCount, int[] bytesOut) throws Exception
  {
    byte[] b = new byte[20];
    String[] grnCodes = new String[1];
    double qty = generalUtils.doubleFromStr(quantity);
    String outstanding;

    double actualQty = goodsReceivedNote.getTotalReceivedForAPOLine(con, stmt, rs, poCode, poLine, grnCodes);

    boolean wanted = true;
    if(outstandingOnly.equals("on"))
    {
      if((qty - actualQty) <= 0)
        wanted = false;
    }
    
    if(wanted)
    {
      generalUtils.doubleToBytesCharFormat((qty - actualQty), b, 0);
      generalUtils.bytesDPsGivenSeparator(true, '\000', dpOnQuantities, b, 20, 0);
      generalUtils.formatNumeric(b, dpOnQuantities);
      outstanding = generalUtils.stringFromBytes(b, 0L);

      generalUtils.doubleToBytesCharFormat(qty, b, 0);
      generalUtils.bytesDPsGivenSeparator(true, '\000', dpOnQuantities, b, 20, 0);
      generalUtils.formatNumeric(b, dpOnQuantities);
 
      if(cssFormat[0].equals("line1"))
        cssFormat[0] = "line2";
      else cssFormat[0] = "line1";
 
      scoutln(out, bytesOut, "<tr  id=\"" + cssFormat[0] + "\">");

      if(status.equals("C"))
        scout(out, bytesOut, "<td align=center><img src=\"" + imagesDir + "z0212.gif\" border=0></td>");
      else scout(out, bytesOut, "<td></td>");
      
      scoutln(out, bytesOut, "<td><p>" + oCount[0]++ + "</td>");
      scout(out, bytesOut, "<td><p>" + supplierCode + "</td>");
      scout(out, bytesOut, "<td><p>" + generalUtils.convertFromYYYYMMDD(reqdBy) + "</td>");
      scout(out, bytesOut, "<td><p>" + generalUtils.convertFromYYYYMMDD(dateConfirmed) + "</td>");
      scoutln(out, bytesOut, "<td><p><a href=\"javascript:viewPO('" + poCode + "')\">" + poCode + "</a></td>");
      scout(out, bytesOut, "<td align=center><p>" + poLine + "</td>");
      scout(out, bytesOut, "<td><p>" + generalUtils.convertFromYYYYMMDD(poDate) + "</td>");
      scout(out, bytesOut, "<td align=center><p>" + soCode + "</td>");
      scout(out, bytesOut, "<td><p>" + formatGRNCodes(grnCodes[0]) + "</td>");
      scout(out, bytesOut, "<td><p><a href=\"javascript:viewItem('" + itemCode + "')\">" + itemCode + "</a></td>");
      scout(out, bytesOut, "<td><p>" + mfr + "</td>");
      scout(out, bytesOut, "<td><p>" + mfrCode + "</td>");
      scoutln(out, bytesOut, "<td align=center><p>" + generalUtils.stringFromBytes(b, 0L) + "</td>");
      scout(out, bytesOut, "<td align=center><p>" + outstanding + "</td>");
      scoutln(out, bytesOut, "<td nowrap><p>" + desc +"</td></tr>");
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private String formatGRNCodes(String grnCodes) throws Exception
  {
    String grnCode, formatted="";
    int len = grnCodes.length();
    int x=0;
    while(x < len)
    {
      grnCode = "";
      while(x < len && grnCodes.charAt(x) != ' ')
        grnCode += grnCodes.charAt(x++);

      formatted += ("<a href=\"javascript:viewGRN('" + grnCode + "')\">" + grnCode + "</a> ");

      ++x;
    }

    return formatted;
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
  private void forAllPurchaseOrderHeaders(Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2,
                                          ResultSet rs3, PrintWriter out, String dateFrom, String dateTo, String outstandingOnly, char dpOnQuantities,
                                          String[] cssFormat, int[] oCount, String dnm, String localDefnsDir, String defnsDir, int[] bytesOut)
                                          throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT POCode, Date, CompanyCode, AllReceived FROM po WHERE Status != 'C' AND "
                         + "Date >= {d '" + dateFrom + "'} AND Date <= {d '" + dateTo + "'} ORDER BY Date");
      
    String poCode, date, supplierCode, allReceived;
    
    while(rs.next())
    {    
      poCode       = rs.getString(1);
      date         = rs.getString(2);
      supplierCode = rs.getString(3);
      allReceived  = rs.getString(4);
     
      if(allReceived.equals("Y") && outstandingOnly.equals("on"))
        ;
      else
      {
        forPurchaseOrderLines(con, stmt2, stmt3, rs2, rs3, out, outstandingOnly, poCode, date, supplierCode, dpOnQuantities, cssFormat, oCount,
                              dnm, localDefnsDir, defnsDir, bytesOut);
      }
    }
  
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void forPurchaseOrderLines(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, String outstandingOnly, String poCode,
                                     String poDate, String supplierCode, char dpOnQuantities, String[] cssFormat, int[] oCount, String dnm,
                                     String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT Quantity, Line FROM pol WHERE POCode = '" + poCode + "'");
      
    String poLine;
    String[] grnCodes = new String[1];
    double qty, totalQty=0, totalActualQty=0;    
    
    while(rs.next())
    {    
      qty    = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(1)), dpOnQuantities);
      poLine = rs.getString(2);
      
      totalQty += qty;

      totalActualQty += goodsReceivedNote.getTotalReceivedForAPOLine(con, stmt2, rs2, poCode, poLine, grnCodes);
    }
  
    if(totalActualQty >= totalQty && outstandingOnly.equals("on"))
      ;
    else
    {
      processPOLine2(out, poCode, poDate, supplierCode, totalQty, totalActualQty, dpOnQuantities, cssFormat, localDefnsDir, defnsDir, oCount,
                     bytesOut);
    }
  
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void processPOLine2(PrintWriter out, String poCode, String poDate, String supplierCode, double totalQty, double totalActualQty,
                              char dpOnQuantities, String[] cssFormat, String localDefnsDir, String defnsDir, int[] oCount, int[] bytesOut)
                              throws Exception
  {
    byte[] b  = new byte[20];
    byte[] b2 = new byte[20];

    generalUtils.doubleToBytesCharFormat(totalQty, b, 0);
    generalUtils.bytesDPsGivenSeparator(true, '\000', dpOnQuantities, b, 20, 0);
    generalUtils.formatNumeric(b, dpOnQuantities);

    generalUtils.doubleToBytesCharFormat((totalQty - totalActualQty), b2, 0);
    generalUtils.bytesDPsGivenSeparator(true, '\000', dpOnQuantities, b2, 20, 0);
    generalUtils.formatNumeric(b2, dpOnQuantities);

    if(cssFormat[0].equals("line1"))
      cssFormat[0] = "line2";
    else cssFormat[0] = "line1";
 
    scoutln(out, bytesOut, "<tr  id=\"" + cssFormat[0] + "\">");

    scoutln(out, bytesOut, "<td><p>" + oCount[0]++ + "</td>");
    scout(out, bytesOut, "<td><p><a href=\"javascript:viewSupp('" + supplierCode + "')\">" + supplierCode + "</a></td>");
    scoutln(out, bytesOut, "<td><p><a href=\"javascript:viewPO('" + poCode + "')\">" + poCode + "</a></td>");
    scout(out, bytesOut, "<td><p>" + generalUtils.convertFromYYYYMMDD(poDate) + "</td>");
    scoutln(out, bytesOut, "<td align=center><p>" + generalUtils.stringFromBytes(b, 0L) + "</td>");
    scoutln(out, bytesOut, "<td align=center><p>" + generalUtils.stringFromBytes(b2, 0L) + "</td>");
    scoutln(out, bytesOut, "<td><p><a href=\"javascript:viewLines('" + poCode + "')\">View Lines</a></td></tr>");
  }

}
