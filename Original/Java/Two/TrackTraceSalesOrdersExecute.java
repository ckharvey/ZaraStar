// =======================================================================================================================================================================================================
// System: ZaraStar: TNT: Sales Orders
// Module: TrackTraceSalesOrdersExecute.java
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

public class TrackTraceSalesOrdersExecute extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  MiscDefinitions miscDefinitions = new MiscDefinitions();
  DeliveryOrder deliveryOrder = new DeliveryOrder();

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
      p1  = req.getParameter("p1"); // soCode
      
      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "1", bytesOut);
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
 
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "TrackTraceSalesOrders", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2021, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush(); 
    }
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", soCode="", cpoCode="", itemCode1="", itemCode2="", custCode="", dateFrom1="", dateFrom2="", dateFrom3="", dateFrom4="", dateTo1="", dateTo2="", dateTo3="", dateTo4="",
           dateType1="", dateType2="", dateType3="", osOnly1="N", osOnly2="N", osOnly3="N", osOnly4="N", which="", state5="";

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
        if(name.equals("soCode"))
          soCode = value[0];
        else
        if(name.equals("cpoCode"))
          cpoCode = value[0];
        else
        if(name.equals("itemCode1"))
          itemCode1 = value[0];
        else
        if(name.equals("itemCode2"))
          itemCode2 = value[0];
        else
        if(name.equals("custCode"))
          custCode = value[0];
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
        if(name.equals("state5"))
          state5 = value[0];
        else
        if(name.equals("which"))
          which = value[0];
      }
      
      if(soCode    == null) soCode    = "";
      if(cpoCode   == null) cpoCode   = "";
      if(itemCode1 == null) itemCode1 = "";
      if(itemCode2 == null) itemCode2 = "";
      if(custCode  == null) custCode  = "";
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
      if(state5    == null) state5    = "";

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, soCode, cpoCode, itemCode1, itemCode2, custCode, dateFrom1, dateFrom2, dateFrom3, dateFrom4, dateTo1, dateTo2, dateTo3, dateTo4, dateType1, dateType2, dateType3, osOnly1, osOnly2, osOnly3,
           osOnly4, state5, which, bytesOut);
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

      System.out.println("2021a: " + e);
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "TrackTraceSalesOrdersa", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2021, bytesOut[0], 0, "ERR:" + soCode);
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String soCode, String cpoCode, String itemCode1, String itemCode2, String custCode, String dateFrom1,
                    String dateFrom2, String dateFrom3, String dateFrom4, String dateTo1, String dateTo2, String dateTo3, String dateTo4, String dateType1, String dateType2, String dateType3, String osOnly1, String osOnly2, String osOnly3,
                    String osOnly4, String state5, String which, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();
    
    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null, stmt2 = null;
    ResultSet rs   = null, rs2 = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 2021, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "TrackTraceSalesOrders", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2021, bytesOut[0], 0, "ACC:" + soCode);
      if(con  != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "TrackTraceSalesOrders", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2021, bytesOut[0], 0, "SID:" + soCode);
      if(con  != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    state5 = generalUtils.deSanitise(state5);

    set(con, stmt, stmt2, rs, rs2, out, req, unm, sid, uty, men, den, dnm, bnm, soCode, cpoCode, itemCode1, itemCode2, custCode, dateFrom1, dateFrom2, dateFrom3, dateFrom4, dateTo1, dateTo2, dateTo3, dateTo4, dateType1, dateType2, dateType3,
        osOnly1, osOnly2, osOnly3, osOnly4, state5, which, imagesDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 2021, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con  != null) con.close();
    if(out != null) out.flush(); 
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String soCode,
                   String cpoCode, String itemCode1, String itemCode2, String custCode, String dateFrom1, String dateFrom2, String dateFrom3, String dateFrom4, String dateTo1, String dateTo2, String dateTo3, String dateTo4, String dateType1,
                   String dateType2, String dateType3, String osOnly1, String osOnly2, String osOnly3, String osOnly4, String state5, String which, String imagesDir, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Track &amp; Trace: Sales Orders</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4031, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewSO(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/SalesOrderPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 5054, unm, uty, dnm, localDefnsDir, defnsDir))
    {
       scoutln(out, bytesOut, "function viewDO(code){var p1=sanitise(code);");
       scoutln(out, bytesOut, "this.location.href=\"/central/servlet/DeliveryOrderPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3001, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewItem(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/ProductStockRecord?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4001, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewCust(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/CustomerPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p1=\"+p1;}");
    }

    scoutln(out, bytesOut, "function viewLines(code){var p1=sanitise(code);");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/TrackTraceSalesOrdersa?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p1=\"+p1;}");

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
 
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "2021",  "", "TrackTraceSalesOrders", unm, sid, uty, men, den, dnm, bnm,
                          localDefnsDir, defnsDir, hmenuCount, bytesOut);

    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Track &amp; Trace: Sales Orders Results", "2021", unm, sid, uty, men,
                    den, dnm, bnm, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<form><table id=\"page\" width=100% border=0>");

    boolean error = false;
    if(which.equals("1") && soCode.length() == 0)
    {
      scoutln(out, bytesOut, "<br><br><span id=\"enquiryMessage\">&nbsp; No Sales Order Code Specified</span><br><br>");
      error = true;
    }
    else
    if(which.equals("2") && cpoCode.length() == 0)
    {
      scoutln(out, bytesOut, "<br><br><span id=\"enquiryMessage\">&nbsp; No Customer Purchase Order Code Specified</span><br><br>");
      error = true;
    }
    else
    if(which.equals("3"))
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
    if(which.equals("4"))
    {
      if(custCode.length() == 0)
      {
        scoutln(out, bytesOut, "<br><br><span id=\"enquiryMessage\">&nbsp; No Customer Code Specified</span><br><br>");
        error = true;
      }
      else
      {
        Customer customer = new Customer();
        if(! customer.existsCompanyRecGivenCode(con, stmt, rs, custCode, dnm, localDefnsDir, defnsDir))
        {
          scoutln(out, bytesOut, "<br><br><span id=\"enquiryMessage\">Customer Code &nbsp; &nbsp; " + custCode);
          scoutln(out, bytesOut, "&nbsp; &nbsp; <font color=\"#FF0000\">Not Found</span><br><br>");
          error = true;
        }
      }
    }
    else
    if(which.equals("5") && (dateFrom3.length() == 0 || dateTo3.length() == 0))
    {
      if(dateFrom3.length() == 0)
        dateFrom3 = "01.01.1970";
      
      if(dateTo3.length() == 0)
        dateTo3 = "31.12.2099";
    }
    else
    if(which.equals("6") && (dateFrom4.length() == 0 || dateTo4.length() == 0))
    {
      if(dateFrom4.length() == 0)
        dateFrom4 = "01.01.1970";
      
      if(dateTo4.length() == 0)
        dateTo4 = "31.12.2099";
    }

    if(error)
    {
      scoutln(out, bytesOut, "</table></form>");
      scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir));
      return;
    }
    
    scoutln(out, bytesOut, "<tr><td></td></tr><tr id=\"pageColumn\"><td></td>");

    if(which.equals("6"))
    {
      scoutln(out, bytesOut, "<td><p><b> Customer Code </b></td>");
      scoutln(out, bytesOut, "<td><p><b> Customer Name </b></td>");
      scoutln(out, bytesOut, "<td><p><b> Customer POCode </b></td>");
      scoutln(out, bytesOut, "<td><p><b> SO Code </b></td>");
      scoutln(out, bytesOut, "<td><p><b> SO Date </b></td>");
      scoutln(out, bytesOut, "<td><p><b> Amount </b></td>");
      scoutln(out, bytesOut, "<td><p><b> Currency </b></td><td></td></tr>");
    }
    else
    {
      scoutln(out, bytesOut, "<td></td><td><p><b> Customer Code </b></td>");
      scoutln(out, bytesOut, "<td><p><b> Delivery Date </b></td>");
      scoutln(out, bytesOut, "<td><p><b> Customer POCode </b></td>");
      scoutln(out, bytesOut, "<td><p><b> SO Code </b></td>");
      scoutln(out, bytesOut, "<td><p><b> SO Line </b></td>");
      scoutln(out, bytesOut, "<td><p><b> SO Date </b></td>");
      scoutln(out, bytesOut, "<td><p><b> Delivery Order </b></td>");
      scoutln(out, bytesOut, "<td><p><b> ItemCode </b></td>");
      scoutln(out, bytesOut, "<td><p><b> Manufacturer Code </b></td>");
      scoutln(out, bytesOut, "<td><p><b> Quantity<br>Required </b></td>");
      scoutln(out, bytesOut, "<td><p><b> Quantity<br>Outstanding </b></td>");    
      scoutln(out, bytesOut, "<td nowrap><p><b> Sales Person </b></td>");
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
      fetchSOGivenSOCode(con, stmt, stmt2, rs, rs2, out, soCode, dpOnQuantities, cssFormat, oCount, imagesDir, bytesOut);
    else
    if(which.equals("2"))
      fetchSOGivenCPO(con, stmt, stmt2, rs, rs2, out, cpoCode, dpOnQuantities, cssFormat, oCount, imagesDir, bytesOut);
    else
    if(which.equals("3"))
    {
      fetchSOsGivenItemCode(con, stmt, stmt2, rs, rs2, out, itemCode1, dateFrom1, dateTo1, dateType1, osOnly1, dpOnQuantities, cssFormat, oCount, bytesOut);
    }
    else
    if(which.equals("4"))
    {
      forSalesOrdersForACustomer(con, stmt, stmt2, rs, rs2, out, custCode, itemCode2, dateFrom2, dateTo2, dateType2, osOnly2, dpOnQuantities, cssFormat, oCount, bytesOut);
    }
    else
    if(which.equals("5"))
    {
      forAllSalesOrders(con, stmt, stmt2, rs, rs2, out, dateFrom3, dateTo3, dateType3, osOnly3, state5, dpOnQuantities, cssFormat, oCount, bytesOut);
    }
    else
    if(which.equals("6"))
    {
      forAllSalesOrderHeaders(con, stmt, rs, out, dateFrom4, dateTo4, osOnly4, dpOnQuantities, cssFormat, oCount, bytesOut);
    }
    
    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir));
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void forAllSalesOrders(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, String dateFrom, String dateTo, String dateType, String outstandingOnly, String reqdSalesPerson, char dpOnQuantities,
                                 String[] cssFormat, int[] oCount, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    String where;
    
    if(dateType.equals("I")) // issuedDate
    {
      where = "t1.Date >= {d '" + dateFrom + "'} AND t1.Date <= {d '" + dateTo + "'}";
    }
    else // deliveryDate
    {
      where = "t2.DeliveryDate >= {d '" + dateFrom + "'} AND t2.DeliveryDate <= {d '" + dateTo + "'}";
    }

    String t = "";
    if(! reqdSalesPerson.equals("ALL"))
      t = " AND t1.SalesPerson = '" + reqdSalesPerson + "' ";
    
    rs = stmt.executeQuery("SELECT t1.SOCode, t1.Date, t1.CompanyCode, t1.CustomerPOCode, t1.AllSupplied, t2.DeliveryDate, t2.Description, t2.Quantity, t2.Line, t2.ItemCode, t1.Salesperson, t2.ManufacturerCode FROM sol AS t2 INNER JOIN so AS t1 ON "
                        + "t2.SOCode = t1.SOCode WHERE t1.Status != 'C' AND " + where + t + " ORDER BY t1.Date, t2.SOCode, t2.Line");
      
    String soCode, date, customerCode, custPOCode, allSupplied, deliveryDate, desc, quantity, line, mfrCode, itemCode, salesPerson;
    
    while(rs.next())
    {    
      soCode       = rs.getString(1);
      date         = rs.getString(2);
      customerCode = rs.getString(3);
      custPOCode   = rs.getString(4);
      allSupplied  = rs.getString(5);
      deliveryDate = rs.getString(6);
      desc         = rs.getString(7);
      quantity     = rs.getString(8);
      line         = rs.getString(9);
      itemCode     = rs.getString(10);
      salesPerson  = rs.getString(11);
      mfrCode      = rs.getString(12);
      
      if(allSupplied.equals("Y") && outstandingOnly.equals("on"))
        ;
      else processSOLine(con, stmt2, rs2, out, soCode, line, mfrCode, date, custPOCode, customerCode, itemCode, quantity, deliveryDate, salesPerson, desc, outstandingOnly, "", dpOnQuantities, cssFormat, "", oCount, bytesOut);
    }
  
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void forSalesOrdersForACustomer(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, String custCode, String itemCode, String dateFrom, String dateTo, String dateType, String outstandingOnly,
                                          char dpOnQuantities, String[] cssFormat, int[] oCount, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    custCode = custCode.toUpperCase();
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
          where = "AND t2.DeliveryDate >= {d '" + dateFrom + "'} AND t2.DeliveryDate <= {d '" + dateTo + "'}";
        else where = "AND t2.ItemCode = '" + itemCode + "' AND t2.DeliveryDate >= {d '" + dateFrom + "'} AND t2.DeliveryDate <= {d '" + dateTo + "'}";
      }
    }
    else // no date specified
    {
      if(itemCode.length() == 0)
        where = "";
      else where = "AND t2.ItemCode = '" + itemCode + "'";
    }
    
    rs = stmt.executeQuery("SELECT t1.SOCode, t1.Date, t1.CustomerPOCode, t1.AllSupplied, t2.DeliveryDate, t2.Description, t2.Quantity, t2.Line, "
                         + "t2.ItemCode, t1.Salesperson, t2.ManufacturerCode FROM sol AS t2 INNER JOIN so AS t1 ON t2.SOCode = t1.SOCode WHERE t1.Status != 'C' AND t1.CompanyCode = '"
                         + custCode + "' " + where + " ORDER BY t1.Date, t2.SOCode, t2.Line");//////////////////
      
    String soCode, date, custPOCode, allSupplied, deliveryDate, desc, quantity, line, mfrCode, salesPerson;
    
    while(rs.next())
    {    
      soCode       = rs.getString(1);
      date         = rs.getString(2);
      custPOCode   = rs.getString(3);
      allSupplied  = rs.getString(4);
      deliveryDate = rs.getString(5);
      desc         = rs.getString(6);
      quantity     = rs.getString(7);
      line         = rs.getString(8);
      itemCode     = rs.getString(9);
      salesPerson  = rs.getString(10);
      mfrCode        = rs.getString(11);
      
      if(allSupplied.equals("Y") && outstandingOnly.equals("on"))
        ;
      else
      {
        processSOLine(con, stmt2, rs2, out, soCode, line, mfrCode, date, custPOCode, custCode, itemCode, quantity, deliveryDate, salesPerson, desc, outstandingOnly, "", dpOnQuantities, cssFormat, "", oCount, bytesOut);
      }
    }
  
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void fetchSOGivenSOCode(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, String soCode,
                                  char dpOnQuantities, String[] cssFormat, int[] oCount, String imagesDir, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    soCode = soCode.toUpperCase();

    rs = stmt.executeQuery("SELECT t1.Date, t1.CompanyCode, t1.CustomerPOCode, t2.DeliveryDate, t2.Description, "
                                   + "t2.Quantity, t2.Line, t2.ItemCode, t1.Status, t1.SalesPerson, t2.ManufacturerCode FROM sol AS t2 INNER JOIN so AS t1 ON "
                                   + "t2.SOCode = t1.SOCode WHERE t2.SOCode = '" + soCode + "' ORDER BY t1.Date");

    String date, customerCode, custPOCode, deliveryDate, desc, quantity, line, mfrCode, itemCode, status, salesPerson;

    while(rs.next())
    {    
      date         = rs.getString(1);
      customerCode = rs.getString(2);
      custPOCode   = rs.getString(3);
      deliveryDate = rs.getString(4);
      desc         = rs.getString(5);
      quantity     = rs.getString(6);
      line         = rs.getString(7);
      itemCode     = rs.getString(8);
      status       = rs.getString(9);
      salesPerson  = rs.getString(10);
      mfrCode      = rs.getString(11);
      
      processSOLine(con, stmt2, rs2, out, soCode, line, mfrCode, date, custPOCode, customerCode, itemCode, quantity, deliveryDate, salesPerson, desc, "", status, dpOnQuantities, cssFormat, imagesDir, oCount, bytesOut);
    }
  
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void fetchSOGivenCPO(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, String cpoCode,
                               char dpOnQuantities, String[] cssFormat, int[] oCount, String imagesDir, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t1.SOCode, t1.Date, t1.CompanyCode, t2.DeliveryDate, t2.Description, t2.Quantity, "
                                   + "t2.Line, t2.ItemCode, t1.Status, t1.SalesPerson, t2.ManufacturerCode FROM sol AS t2 INNER JOIN so AS t1 ON t2.SOCode = t1.SOCode "
                                   + "WHERE t1.CustomerPOCode = '" + cpoCode + "' ORDER BY t1.Date");

    String soCode, date, customerCode, deliveryDate, desc, quantity, line, mfrCode, itemCode, status, salesPerson;

    while(rs.next())
    {    
      soCode       = rs.getString(1);
      date         = rs.getString(2);
      customerCode = rs.getString(3);
      deliveryDate = rs.getString(4);
      desc         = rs.getString(5);
      quantity     = rs.getString(6);
      line         = rs.getString(7);
      itemCode     = rs.getString(8);
      status       = rs.getString(9);
      salesPerson  = rs.getString(10);
      mfrCode      = rs.getString(11);
      
      processSOLine(con, stmt2, rs2, out, soCode, line, mfrCode, date, cpoCode, customerCode, itemCode, quantity, deliveryDate, salesPerson, desc, "", status, dpOnQuantities, cssFormat, imagesDir, oCount, bytesOut);
    }
  
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void fetchSOsGivenItemCode(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, String itemCode,
                                     String dateFrom, String dateTo, String dateType, String outstandingOnly, char dpOnQuantities, String[] cssFormat,
                                     int[] oCount, int[] bytesOut) throws Exception
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
      else // deliveryDate
      {
        if(itemCode.length() == 0)
          where = "t2.DeliveryDate >= {d '" + dateFrom + "'} AND t2.DeliveryDate <= {d '" + dateTo + "'}";
        else 
        {
          where = "t2.ItemCode = '" + itemCode + "' AND t2.DeliveryDate >= {d '" + dateFrom + "'} AND t2.DeliveryDate <= {d '"
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
    
    rs = stmt.executeQuery("SELECT t1.SOCode, t1.Date, t1.CompanyCode, t1.CustomerPOCode, t1.AllSupplied, t2.DeliveryDate, "
                        + "t2.Description, t2.Quantity, t2.Line, t1.SalesPerson, t2.ManufacturerCode FROM sol AS t2 INNER JOIN so AS t1 ON t2.SOCode = t1.SOCode "
                        + "WHERE t1.Status != 'C' AND " + where + " ORDER BY t1.Date");
      
    String soCode, date, customerCode, custPOCode, allSupplied, deliveryDate, desc, quantity, line, mfrCode, salesPerson;
    
    while(rs.next())
    {    
      soCode       = rs.getString(1);
      date         = rs.getString(2);
      customerCode = rs.getString(3);
      custPOCode   = rs.getString(4);
      allSupplied  = rs.getString(5);
      deliveryDate = rs.getString(6);
      desc         = rs.getString(7);
      quantity     = rs.getString(8);
      line         = rs.getString(9);
      salesPerson  = rs.getString(10);
      mfrCode      = rs.getString(11);
      
      if(allSupplied.equals("Y") && outstandingOnly.equals("on"))
        ;
      else
      {
        processSOLine(con, stmt2, rs2, out, soCode, line, mfrCode, date, custPOCode, customerCode, itemCode, quantity, deliveryDate, salesPerson, desc, outstandingOnly, "", dpOnQuantities, cssFormat, "", oCount, bytesOut);
      }
    }
  
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void processSOLine(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String soCode, String soLine, String mfrCode, String soDate, String custPOCode, String customerCode, String itemCode, String quantity,
                             String deliveryDate, String salesPerson, String desc, String outstandingOnly, String status, char dpOnQuantities, String[] cssFormat, String imagesDir, int[] oCount, int[] bytesOut) throws Exception
  {
    byte[] b = new byte[20];
    String[] doCodes = new String[1];
    double qty = generalUtils.doubleFromStr(quantity);
    String outstanding;

    double actualQty = deliveryOrder.getTotalDeliveredForASOLine(con, stmt, rs, soCode, soLine, doCodes);

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
 
      if(cssFormat[0].equals("line1")) cssFormat[0] = "line2"; else cssFormat[0] = "line1";
      
      scoutln(out, bytesOut, "<tr  id=\"" + cssFormat[0] + "\">");

      if(status.equals("C"))
        scout(out, bytesOut, "<td align=center><img src=\"" + imagesDir + "z0212.gif\" border=0></td>");
      else scout(out, bytesOut, "<td></td>");
      
      scoutln(out, bytesOut, "<td><p>" + oCount[0]++ + "</td>");
      scout(out, bytesOut, "<td><p><a href=\"javascript:viewCust('" + customerCode + "')\">" + customerCode + "</a></td>");
      scout(out, bytesOut, "<td><p>" + generalUtils.convertFromYYYYMMDD(deliveryDate) + "</td>");
      scout(out, bytesOut, "<td nowrap><p>" + custPOCode + "</td>");
      scoutln(out, bytesOut, "<td><p><a href=\"javascript:viewSO('" + soCode + "')\">" + soCode + "</a></td>");
      scout(out, bytesOut, "<td nowrap><p>" + soLine + "</td>");
      scout(out, bytesOut, "<td><p>" + generalUtils.convertFromYYYYMMDD(soDate) + "</td>");
      scout(out, bytesOut, "<td><p>" + formatDOCodes(doCodes[0]) + "</td>");
      scout(out, bytesOut, "<td><p><a href=\"javascript:viewItem('" + itemCode + "')\">" + itemCode + "</a></td>");
      scout(out, bytesOut, "<td nowrap><p>" + mfrCode + "</td>");
      scoutln(out, bytesOut, "<td align=center><p>" + generalUtils.stringFromBytes(b, 0L) + "</td>");
      scout(out, bytesOut, "<td align=center><p>" + outstanding + "</td>");
      scoutln(out, bytesOut, "<td nowrap><p>" + salesPerson + "</td>");
      scoutln(out, bytesOut, "<td nowrap><p>" + desc + "</td></tr>");
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private String formatDOCodes(String doCodes) throws Exception
  {
    String doCode, formatted="";
    int len = doCodes.length();
    int x=0;
    while(x < len)
    {
      doCode = "";
      while(x < len && doCodes.charAt(x) != ' ')
        doCode += doCodes.charAt(x++);

      formatted += ("<a href=\"javascript:viewDO('" + doCode + "')\">" + doCode + "</a> ");

      ++x;
    }

    return formatted;
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
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void forAllSalesOrderHeaders(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String dateFrom, String dateTo, String outstandingOnly,
                                       char dpOnQuantities, String[] cssFormat, int[] oCount, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();
    Statement stmt2 = null, stmt3 = null;
    ResultSet rs2   = null, rs3   = null;
    
    rs = stmt.executeQuery("SELECT SOCode, Date, CompanyCode, CustomerPOCode, AllSupplied, TotalTotal, Currency2, CompanyName "
                         + "FROM so WHERE Status != 'C' AND Date >= {d '" + dateFrom + "'} AND Date <= {d '" + dateTo
                         + "'} ORDER BY Date");
      
    String soCode, date, customerCode, custPOCode, allSupplied, currency, companyName;
    double totalTotal;
    
    while(rs.next())
    {    
      soCode       = rs.getString(1);
      date         = rs.getString(2);
      customerCode = rs.getString(3);
      custPOCode   = rs.getString(4);
      allSupplied  = rs.getString(5);
      totalTotal   = generalUtils.doubleFromStr(rs.getString(6));
      currency     = rs.getString(7);
      companyName  = rs.getString(8);
      
      if(allSupplied.equals("Y") && outstandingOnly.equals("on"))
        ;
      else
      {
        forSalesOrderLines(con, stmt2, stmt3, rs2, rs3, out, outstandingOnly, soCode, date, custPOCode, customerCode, totalTotal, currency, companyName,
                           dpOnQuantities, cssFormat, oCount, bytesOut);
      }
    }
  
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void forSalesOrderLines(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, String outstandingOnly,
                                  String soCode, String soDate, String custPOCode, String customerCode, double totalTotal, String currency, String companyName,
                                  char dpOnQuantities, String[] cssFormat, int[] oCount, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT Quantity, Line FROM sol WHERE SOCode = '" + soCode + "'");
      
    String soLine;
    String[] doCodes = new String[1];
    double qty, totalQty=0, totalActualQty=0;    
    
    while(rs.next())
    {    
      qty    = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(1)), dpOnQuantities);
      soLine = rs.getString(2);
      
      totalQty += qty;

      totalActualQty += deliveryOrder.getTotalDeliveredForASOLine(con, stmt2, rs2, soCode, soLine, doCodes);
    }
  
    if(totalActualQty >= totalQty && outstandingOnly.equals("on"))
      ;
    else
    {
      processSOLine2(out, soCode, soDate, custPOCode, customerCode, totalTotal, currency, companyName, cssFormat, oCount, bytesOut);
    }
  
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void processSOLine2(PrintWriter out, String soCode, String soDate, String custPOCode, String customerCode, double totalTotal, String currency,
                              String companyName, String[] cssFormat, int[] oCount, int[] bytesOut) throws Exception
  {
    byte[] b  = new byte[20];

    generalUtils.doubleToBytesCharFormat(totalTotal, b, 0);
    generalUtils.bytesDPsGivenSeparator(true, '\000', '2', b, 20, 0);
    generalUtils.formatNumeric(b, '2');

    if(cssFormat[0].equals("line1"))
      cssFormat[0] = "line2";
    else cssFormat[0] = "line1";
 
    scoutln(out, bytesOut, "<tr  id=\"" + cssFormat[0] + "\">");

    scoutln(out, bytesOut, "<td><p>" + oCount[0]++ + "</td>");
    scout(out, bytesOut, "<td><p><a href=\"javascript:viewCust('" + customerCode + "')\">" + customerCode + "</a></td>");
    scout(out, bytesOut, "<td nowrap><p>" + companyName + "</td>");
    scout(out, bytesOut, "<td nowrap><p>" + custPOCode + "</td>");
    scoutln(out, bytesOut, "<td><p><a href=\"javascript:viewSO('" + soCode + "')\">" + soCode + "</a></td>");
    scout(out, bytesOut, "<td><p>" + generalUtils.convertFromYYYYMMDD(soDate) + "</td>");
    scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.stringFromBytes(b, 0L) + "</td>");
    scout(out, bytesOut, "<td nowrap><p>" + currency + "</td>");
    scoutln(out, bytesOut, "<td><p><a href=\"javascript:viewLines('" + soCode + "')\">View Lines</a></td></tr>");
  }

}
