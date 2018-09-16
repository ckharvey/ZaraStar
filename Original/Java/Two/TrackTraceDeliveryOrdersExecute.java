// =======================================================================================================================================================================================================
// System: ZaraStar: TNT: Delivery Orders
// Module: TrackTraceDeliveryOrdersExecute.java
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
import java.util.*;
import java.io.*;

public class TrackTraceDeliveryOrdersExecute extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  MiscDefinitions miscDefinitions = new MiscDefinitions();
  PickingList pickingList = new PickingList();
  DashboardUtils dashboardUtils = new DashboardUtils();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", itemCode1="", custCode="", dateFrom1="", dateFrom2="", dateFrom3="", dateFrom4="", dateTo1="", dateTo2="", dateTo3="", dateTo4="", osOnly1="N", osOnly2="N", osOnly3="N",
           osOnly3a = "N", osOnly4="N", which="", salesPerson4="", p2="";

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
      p1  = req.getParameter("p1"); // doCode
      p2  = req.getParameter("p2"); // plain or not
      itemCode1 = req.getParameter("itemCode1");
      custCode  = req.getParameter("custCode");
      dateFrom1 = req.getParameter("dateFrom1");
      dateFrom2 = req.getParameter("dateFrom2");
      dateFrom3 = req.getParameter("dateFrom3");
      dateFrom4 = req.getParameter("dateFrom4");
      dateTo1   = req.getParameter("dateTo1");
      dateTo2   = req.getParameter("dateTo2");
      dateTo3   = req.getParameter("dateTo3");
      dateTo4   = req.getParameter("dateTo4");
      osOnly1   = req.getParameter("osOnly1");
      osOnly2   = req.getParameter("osOnly2");
      osOnly3   = req.getParameter("osOnly3");
      osOnly3a  = req.getParameter("osOnly3a");
      osOnly4   = req.getParameter("osOnly4");
      which     = req.getParameter("which");
      salesPerson4 = req.getParameter("salesPerson4");
      
      if(p1        == null) p1        = "";
      if(itemCode1 == null) itemCode1 = "";
      if(custCode  == null) custCode  = "";
      if(dateFrom1 == null) dateFrom1 = "";
      if(dateFrom2 == null) dateFrom2 = "";
      if(dateFrom3 == null) dateFrom3 = "";
      if(dateFrom4 == null) dateFrom4 = "";
      if(dateTo1   == null) dateTo1   = "";
      if(dateTo2   == null) dateTo2   = "";
      if(dateTo3   == null) dateTo3   = "";
      if(dateTo4   == null) dateTo4   = "";
      if(osOnly1   == null) osOnly1   = "";
      if(osOnly2   == null) osOnly2   = "";
      if(osOnly3   == null) osOnly3   = "";
      if(osOnly3a  == null) osOnly3a  = "";
      if(osOnly4   == null) osOnly4   = "";
      if(salesPerson4 == null) salesPerson4 = "";
      if(p2        == null) p2 = "";
      
      salesPerson4 = generalUtils.deSanitise(salesPerson4);

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, itemCode1, custCode, dateFrom1, dateFrom2, dateFrom3, dateFrom4, dateTo1, dateTo2, dateTo3, dateTo4, osOnly1, osOnly2, osOnly3, osOnly3a, osOnly4, which, salesPerson4, p2, bytesOut);
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
 
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "TrackTraceDeliveryOrders", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2026, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush(); 
    }
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", doCode="", itemCode1="", custCode="", dateFrom1="", dateFrom2="", dateFrom3="", dateFrom4="", dateTo1="", dateTo2="", dateTo3="", dateTo4="", osOnly1="N", osOnly2="N",
           osOnly3="N", osOnly3a="N", osOnly4="N", which="", salesPerson4="", p2="";

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
        if(name.equals("doCode"))
          doCode = value[0];
        else
        if(name.equals("itemCode1"))
          itemCode1 = value[0];
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
        if(name.equals("osOnly1"))
          osOnly1 = value[0];
        else
        if(name.equals("osOnly2"))
          osOnly2 = value[0];
        else
        if(name.equals("osOnly3"))
          osOnly3 = value[0];
        else
        if(name.equals("osOnly3a"))
          osOnly3a = value[0];
        else
        if(name.equals("osOnly4"))
          osOnly4 = value[0];
        else
        if(name.equals("which"))
          which = value[0];
        else
        if(name.equals("salesPerson4"))
          salesPerson4 = value[0];
        else
        if(name.equals("p2"))
          p2 = value[0];
      }
      
      if(doCode    == null) doCode    = "";
      if(itemCode1 == null) itemCode1 = "";
      if(custCode  == null) custCode  = "";
      if(dateFrom1 == null) dateFrom1 = "";
      if(dateFrom2 == null) dateFrom2 = "";
      if(dateFrom3 == null) dateFrom3 = "";
      if(dateFrom4 == null) dateFrom4 = "";
      if(dateTo1   == null) dateTo1   = "";
      if(dateTo2   == null) dateTo2   = "";
      if(dateTo3   == null) dateTo3   = "";
      if(dateTo4   == null) dateTo4   = "";
      if(osOnly1   == null) osOnly1   = "";
      if(osOnly2   == null) osOnly2   = "";
      if(osOnly3   == null) osOnly3   = "";
      if(osOnly3a  == null) osOnly3a  = "";
      if(osOnly4   == null) osOnly4   = "";
      if(salesPerson4 == null) salesPerson4 = "";
      if(p2        == null) p2 = "";

      salesPerson4 = generalUtils.deSanitise(salesPerson4);

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, doCode, itemCode1, custCode, dateFrom1, dateFrom2, dateFrom3, dateFrom4, dateTo1, dateTo2, dateTo3, dateTo4, osOnly1, osOnly2, osOnly3, osOnly3a, osOnly4, which, salesPerson4, p2, bytesOut);
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

      System.out.println("2026a: " + e);
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "TrackTraceDeliveryOrdersa", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2026, bytesOut[0], 0, "ERR:" + doCode);
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String doCode, String itemCode1, String custCode, String dateFrom1, String dateFrom2,
                    String dateFrom3, String dateFrom4, String dateTo1, String dateTo2, String dateTo3, String dateTo4, String osOnly1, String osOnly2, String osOnly3, String osOnly3a, String osOnly4, String which, String salesPerson4, String p2,
                    int[] bytesOut) throws Exception
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
        
    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 2026, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "TrackTraceDeliveryOrders", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2026, bytesOut[0], 0, "ACC:" + doCode);
      if(con  != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "TrackTraceDeliveryOrders", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2026, bytesOut[0], 0, "SID:" + doCode);
      if(con  != null) con.close();
      if(out != null) out.flush(); 
      return;
    }
    
    boolean plain = false;
    if(p2.equals("P"))
      plain = true;

    set(con, stmt, stmt2, rs, rs2, out, req, plain, unm, sid, uty, men, den, dnm, bnm, doCode, itemCode1, custCode, dateFrom1, dateFrom2, dateFrom3, dateFrom4, dateTo1, dateTo2, dateTo3, dateTo4, osOnly1, osOnly2, osOnly3, osOnly3a, osOnly4,
        which, salesPerson4, imagesDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 2026, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con  != null) con.close();
    if(out != null) out.flush(); 
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, HttpServletRequest req, boolean plain, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                   String doCode, String itemCode1, String custCode, String dateFrom1, String dateFrom2, String dateFrom3, String dateFrom4, String dateTo1, String dateTo2, String dateTo3, String dateTo4, String osOnly1, String osOnly2,
                   String osOnly3, String osOnly3a, String osOnly4, String which, String salesPerson4, String imagesDir, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Track &amp; Trace: Delivery Orders</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4054, unm, uty, dnm, localDefnsDir, defnsDir))
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
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/TrackTraceDeliveryOrdersa?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p1=\"+p1;}");

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
    scoutln(out, bytesOut, "return code2};</script>");

    if(plain)
      scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"print\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "generalPlain.css\">");
    else scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    outputPageFrame(con, stmt, rs, out, req, plain, doCode, itemCode1, custCode, dateFrom1, dateFrom2, dateFrom3, dateFrom4, dateTo1, dateTo2, dateTo3, dateTo4, osOnly1, osOnly2, osOnly3, osOnly3a, osOnly4, which, salesPerson4, "",
                    "Track &amp; Trace: Delivery Order Results", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, bytesOut);

    scoutln(out, bytesOut, "<form><table id=\"page\" width=100% border=0>");

    boolean error = false;
    if(which.equals("1") && doCode.length() == 0)
    {
      scoutln(out, bytesOut, "<br><br><span id=\"enquiryMessage\">&nbsp; No Delivery Order Code Specified</span><br><br>");
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
    if(which.equals("4") && (dateFrom3.length() == 0 || dateTo3.length() == 0))
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

    if(which.equals("3"))
    {
      scoutln(out, bytesOut, "<td><p><b> Customer Code </b></td>");
      scoutln(out, bytesOut, "<td><p><b> Customer Name </b></td>");
      scoutln(out, bytesOut, "<td><p><b> DO Code </b></td>");
      scoutln(out, bytesOut, "<td><p><b> DO Date </b></td>");
      scoutln(out, bytesOut, "<td><p><b> Invoice </b></td>");
      scoutln(out, bytesOut, "<td><p><b> Returned </b></td><td></td></tr>");
    }
    else
    if(which.equals("4"))
    {
      scoutln(out, bytesOut, "<td><p><b> DO Date </b></td>");
      scoutln(out, bytesOut, "<td><p><b> DO Code </b></td>");
      scoutln(out, bytesOut, "<td><p><b> Customer Code </b></td>");
      scoutln(out, bytesOut, "<td><p><b> Customer Name </b></td>");
      scoutln(out, bytesOut, "<td><p><b> SalesPerson </b></td>");
      scoutln(out, bytesOut, "<td><p><b> Invoice </b></td>");
      scoutln(out, bytesOut, "<td><p><b> Total </b></td>");
      scoutln(out, bytesOut, "<td><p><b> Returned </b></td><td></td></tr>");
    }    
    else
    {
      scoutln(out, bytesOut, "<td></td><td><p><b> Customer Code </b></td>");
      scoutln(out, bytesOut, "<td><p><b> Customer Name </b></td>");
      scoutln(out, bytesOut, "<td><p><b> DO Code </b></td>");
      scoutln(out, bytesOut, "<td><p><b> DO Line </b></td>");
      scoutln(out, bytesOut, "<td><p><b> DO Date </b></td>");
      scoutln(out, bytesOut, "<td><p><b> ItemCode </b></td>");
      scoutln(out, bytesOut, "<td><p><b> Quantity </b></td>");
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
        
    String[] cssFormat = new String[1];  cssFormat[0] = "line1";
    int[] oCount = new int[1];  oCount[0] = 1;
    char dpOnQuantities = miscDefinitions.dpOnQuantities(con, stmt, rs, "5");
    
    if(which.equals("1"))
      fetchDOGivenDOCode(con, out, doCode, dpOnQuantities, cssFormat, oCount, dnm, imagesDir, localDefnsDir, defnsDir, bytesOut);
    else
    if(which.equals("2"))
      fetchDOsGivenItemCode(con, stmt, rs, out, itemCode1, dateFrom1, dateTo1, osOnly1, dpOnQuantities, cssFormat, oCount, localDefnsDir, defnsDir, bytesOut);
    else
    if(which.equals("3"))
      forDOsForACustomer(con, stmt, stmt2, rs, rs2, out, custCode, dateFrom2, dateTo2, osOnly2, cssFormat, oCount, bytesOut);
    else
    if(which.equals("4"))
      forAllDOs(con, stmt, rs, out, plain, dateFrom3, dateTo3, osOnly3, osOnly3a, salesPerson4, cssFormat, oCount, localDefnsDir, defnsDir, bytesOut);
    else
    if(which.equals("5"))
      forAllDOLines(con, stmt, stmt2, rs, rs2, out, dateFrom4, dateTo4, osOnly4, dpOnQuantities, cssFormat, oCount, localDefnsDir, defnsDir, bytesOut);
    
    scoutln(out, bytesOut, "</table></form>");     scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void forAllDOs(Connection con, Statement stmt, ResultSet rs, PrintWriter out, boolean plain, String dateFrom, String dateTo, String outstandingOnly, String nonInvoicedOnly, String salesPersonReqd, String[] cssFormat, int[] oCount,
                         String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    String where = "";
      
    if(outstandingOnly.equals("on"))
      where += " AND ! ( Returned = 'Y' OR Returned = '1' ) ";

    if(nonInvoicedOnly.equals("on"))
      where += " AND InvoiceCode = ''";

    if(! salesPersonReqd.equals("ALL"))
      where += " AND SalesPerson = '" + salesPersonReqd + "'";

    rs = stmt.executeQuery("SELECT DOCode, Date, CompanyCode, CompanyName, SalesPerson, InvoiceCode, Currency, TotalTotal, Returned FROM do "
                         + "WHERE Status != 'C' AND Date >= {d '" + dateFrom + "'} AND Date <= {d '" + dateTo + "'} " + where + " ORDER BY DOCode");
      
    String doCode, date, customerCode, customerName, salesPerson, invoiceCode, currency, totalTotal, returned;
    
    while(rs.next())
    {    
      doCode       = rs.getString(1);
      date         = rs.getString(2);
      customerCode = rs.getString(3);
      customerName = rs.getString(4);
      salesPerson  = rs.getString(5);
      invoiceCode  = rs.getString(6);
      currency     = rs.getString(7);
      totalTotal   = rs.getString(8);
      returned     = rs.getString(9);

      processDOLine3(out, plain, doCode, date, customerCode, customerName, salesPerson, invoiceCode, currency, totalTotal, returned, cssFormat, localDefnsDir, defnsDir, oCount, bytesOut);
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void forDOsForACustomer(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, String custCode, String dateFrom, String dateTo, String outstandingOnly, String[] cssFormat, int[] oCount,
                                  int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    custCode = custCode.toUpperCase();

    String where = "";
    
    if(dateFrom.length() != 0 || dateTo.length() != 0)
    {
      if(dateFrom.length() == 0)
        dateFrom = "1970-01-01";
      else
      if(dateTo.length() == 0)
        dateTo = "2099-12-31";
      
      where = "AND Date >= {d '" + dateFrom + "'} AND Date <= {d '" + dateTo + "'}";
    }
    
    if(outstandingOnly.equals("on"))
      where += " AND ! ( Returned = 'Y' OR Returned = '1' ) ";

    rs = stmt.executeQuery("SELECT DOCode, Date, CompanyName, Returned, InvoiceCode FROM do WHERE Status != 'C' AND CompanyCode = '" + custCode + "' " 
                          + where + " ORDER BY Date");
      
    String doCode, date, customerName, returned, invoiceCode;
    
    while(rs.next())
    {    
      doCode       = rs.getString(1);
      date         = rs.getString(2);
      customerName = rs.getString(3);
      returned     = rs.getString(4);
      invoiceCode  = rs.getString(5);
      
      processDOLine2(out, doCode, date, custCode, customerName, returned, invoiceCode, cssFormat, oCount, bytesOut);
    }
  
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void fetchDOGivenDOCode(Connection con, PrintWriter out, String doCode, char dpOnQuantities, String[] cssFormat, int[] oCount, String dnm,
                                  String imagesDir, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    Statement stmt = con.createStatement();

    doCode = doCode.toUpperCase();

    ResultSet rs = stmt.executeQuery("SELECT t1.Date, t1.CompanyCode, t2.Description, t2.Quantity, t2.Line, t2.ItemCode, "
                                   + "t1.Status, t1.CompanyName FROM dol AS t2 INNER JOIN do AS t1 ON t2.doCode = t1.doCode WHERE t2.doCode = '"
                                   + doCode + "' ORDER BY t1.Date");

    String date, customerCode, desc, quantity, line, itemCode, status, customerName;

    while(rs.next())
    {    
      date         = rs.getString(1);
      customerCode = rs.getString(2);
      desc         = rs.getString(3);
      quantity     = rs.getString(4);
      line         = rs.getString(5);
      itemCode     = rs.getString(6);
      status       = rs.getString(7);
      customerName = rs.getString(8);
      
      processDOLine(out, doCode, line, date, customerCode, customerName, itemCode, quantity, desc, "", status, dpOnQuantities, cssFormat, imagesDir,
                    localDefnsDir, defnsDir, oCount, bytesOut);
    }
  
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void fetchDOsGivenItemCode(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String itemCode, String dateFrom, String dateTo,
                                     String outstandingOnly, char dpOnQuantities, String[] cssFormat, int[] oCount, String localDefnsDir,
                                     String defnsDir, int[] bytesOut) throws Exception
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
      
      if(itemCode.length() == 0)
        where = "t1.Date >= {d '" + dateFrom + "'} AND t1.Date <= {d '" + dateTo + "'}";
      else where = "t2.ItemCode = '" + itemCode + "' AND t1.Date >= {d '" + dateFrom + "'} AND t1.Date <= {d '" + dateTo + "'}";
    }
    else // no date specified
    {
      if(itemCode.length() == 0)
        where = "";
      else where = "t2.ItemCode = '" + itemCode + "'";
    }
  
    if(outstandingOnly.equals("on"))
      where += " AND ! ( Returned = 'Y' OR Returned = '1' ) ";

    rs = stmt.executeQuery("SELECT t1.doCode, t1.Date, t1.CompanyCode, t2.Description, t2.Quantity, t2.Line, "
                         + "t1.CompanyName FROM dol AS t2 INNER JOIN do AS t1 ON t2.doCode = t1.doCode WHERE t1.Status != 'C' AND " + where
                         + " ORDER BY t1.Date");
      
    String doCode, date, customerCode, desc, quantity, line, customerName;
    
    while(rs.next())
    {    
      doCode       = rs.getString(1);
      date         = rs.getString(2);
      customerCode = rs.getString(3);
      desc         = rs.getString(4);
      quantity     = rs.getString(5);
      line         = rs.getString(6);
      customerName = rs.getString(7);
      
      processDOLine(out, doCode, line, date, customerCode, customerName, itemCode, quantity, desc, outstandingOnly, "", dpOnQuantities, cssFormat,
                    "", localDefnsDir, defnsDir, oCount, bytesOut);
    }
  
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void processDOLine(PrintWriter out, String doCode, String doLine, String doDate, String customerCode, String customerName, String itemCode,
                             String quantity, String desc, String outstandingOnly, String status, char dpOnQuantities, String[] cssFormat,
                             String imagesDir, String localDefnsDir, String defnsDir, int[] oCount, int[] bytesOut) throws Exception
  {
    byte[] b  = new byte[20];
    double qty = generalUtils.doubleFromStr(quantity);
    
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
    scout(out, bytesOut, "<td><p><a href=\"javascript:viewCust('" + customerCode + "')\">" + customerCode + "</a></td>");
    scout(out, bytesOut, "<td nowrap><p>" + customerName + "</td>");
    scoutln(out, bytesOut, "<td><p><a href=\"javascript:viewDO('" + doCode + "')\">" + doCode + "</a></td>");
    scout(out, bytesOut, "<td nowrap><p>" + doLine + "</td>");
    scout(out, bytesOut, "<td><p>" + generalUtils.convertFromYYYYMMDD(doDate) + "</td>");
    scout(out, bytesOut, "<td><p><a href=\"javascript:viewItem('" + itemCode + "')\">" + itemCode + "</a></td>");
    scoutln(out, bytesOut, "<td align=center><p>" + generalUtils.stringFromBytes(b,  0L) + "</td>");
    scoutln(out, bytesOut, "<td nowrap><p>" + desc +"</td></tr>");
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
  private void forAllDOLines(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, String dateFrom,
                             String dateTo, String outstandingOnly, char dpOnQuantities, String[] cssFormat, int[] oCount, String localDefnsDir,
                             String defnsDir, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();
    
    String where = "";
    
    if(outstandingOnly.equals("on"))
      where += " AND ! ( Returned = 'Y' OR Returned = '1' ) ";

    rs = stmt.executeQuery("SELECT DOCode, Date, CompanyCode, CompanyName FROM do WHERE Status != 'C' AND Date >= {d '" + dateFrom
                         + "'} AND Date <= {d '" + dateTo + "'} " + where + " ORDER BY Date");
      
    String doCode, date, customerCode, companyName;
    double totalTotal;
    
    while(rs.next())
    {    
      doCode       = rs.getString(1);
      date         = rs.getString(2);
      customerCode = rs.getString(3);
      companyName  = rs.getString(4);
      
      forDOLines(con, stmt2, rs2, out, outstandingOnly, doCode, date, customerCode, companyName, dpOnQuantities, cssFormat, oCount, localDefnsDir,
                 defnsDir, bytesOut);
    }
  
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void forDOLines(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String outstandingOnly, String doCode, String doDate,
                          String customerCode, String companyName, char dpOnQuantities, String[] cssFormat, int[] oCount, String localDefnsDir,
                          String defnsDir, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT Quantity, Line, ItemCode, Description FROM dol WHERE doCode = '" + doCode + "'");
      
    String line, quantity, itemCode, desc;
    
    while(rs.next())
    {    
      quantity = rs.getString(1);
      line     = rs.getString(2);
      itemCode = rs.getString(3);
      desc     = rs.getString(4);
      
      processDOLine(out, doCode, line, doDate, customerCode, companyName, itemCode, quantity, desc, outstandingOnly, "", dpOnQuantities, cssFormat,
                    "", localDefnsDir, defnsDir, oCount, bytesOut);
    }
  
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void processDOLine2(PrintWriter out, String doCode, String doDate, String customerCode, String companyName, String returned,
                              String invoiceCode, String[] cssFormat, int[] oCount, int[] bytesOut) throws Exception
  {
    if(cssFormat[0].equals("line1"))
      cssFormat[0] = "line2";
    else cssFormat[0] = "line1";
 
    scoutln(out, bytesOut, "<tr  id=\"" + cssFormat[0] + "\">");

    scoutln(out, bytesOut, "<td><p>" + oCount[0]++ + "</td>");
    scout(out, bytesOut, "<td><p><a href=\"javascript:viewCust('" + customerCode + "')\">" + customerCode + "</a></td>");
    scout(out, bytesOut, "<td nowrap><p>" + companyName + "</td>");
    scoutln(out, bytesOut, "<td><p><a href=\"javascript:viewDO('" + doCode + "')\">" + doCode + "</a></td>");
    scout(out, bytesOut, "<td><p>" + generalUtils.convertFromYYYYMMDD(doDate) + "</td>");
    scout(out, bytesOut, "<td><p>" + invoiceCode + "</td>");

    if(returned.equals("1") || returned.equals("Y"))
      scout(out, bytesOut, "<td nowrap align=center><p>Yes</td>");
    else scout(out, bytesOut, "<td nowrap align=center><p><b><font color=red>No</font></td>");
    
    scoutln(out, bytesOut, "<td><p><a href=\"javascript:viewLines('" + doCode + "')\">View Lines</a></td></tr>");
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void processDOLine3(PrintWriter out, boolean plain, String doCode, String doDate, String customerCode, String companyName, String salesPerson,
                              String invoiceCode, String currency, String totalTotal, String returned, String[] cssFormat, String localDefnsDir,
                              String defnsDir, int[] oCount, int[] bytesOut) throws Exception
  {
    if(cssFormat[0].equals("line1"))
      cssFormat[0] = "line2";
    else cssFormat[0] = "line1";
 
    scoutln(out, bytesOut, "<tr  id=\"" + cssFormat[0] + "\">");

    scoutln(out, bytesOut, "<td><p>" + oCount[0]++ + "</td>");
    scout(out, bytesOut, "<td><p>" + generalUtils.convertFromYYYYMMDD(doDate) + "</td>");
    scoutln(out, bytesOut, "<td><p><a href=\"javascript:viewDO('" + doCode + "')\">" + doCode + "</a></td>");
    scout(out, bytesOut, "<td><p><a href=\"javascript:viewCust('" + customerCode + "')\">" + customerCode + "</a></td>");
    scout(out, bytesOut, "<td nowrap><p>" + companyName + "</td>");
    scout(out, bytesOut, "<td nowrap><p>" + salesPerson + "</td>");
    scout(out, bytesOut, "<td nowrap><p>" + invoiceCode + "</td>");
    scout(out, bytesOut, "<td nowrap><p>" + currency + " " + generalUtils.doubleDPs(totalTotal, '2') + "</td>");

    if(returned.equals("1") || returned.equals("Y"))
      scout(out, bytesOut, "<td nowrap align=center><p>Yes</td>");
    else scout(out, bytesOut, "<td nowrap align=center><p><b><font color=red>No</font></td>");
    
    if(plain)
      scoutln(out, bytesOut, "</tr>");
    else scoutln(out, bytesOut, "<td><p><a href=\"javascript:viewLines('" + doCode + "')\">View Lines</a></td></tr>");
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void outputPageFrame(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, boolean plain, String doCode, String itemCode1, String custCode, String dateFrom1, String dateFrom2, String dateFrom3,
                               String dateFrom4, String dateTo1, String dateTo2, String dateTo3, String dateTo4, String osOnly1, String osOnly2, String osOnly3, String osOnly3a, String osOnly4, String which, String salesPerson4, String bodyStr,
                               String title, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String otherSetup, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    int[] hmenuCount = new int[1];

    String subMenuText = dashboardUtils.buildSubMenuText(con, stmt, rs, req, "2026", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount);

    subMenuText += "<table id='title' width=100%><tr><td colspan=2 nowrap><p>" + title + directoryUtils.buildHelp(2026) + "</td></tr></table>";

    subMenuText += buildSubMenuText(plain, doCode, itemCode1, custCode, dateFrom1, dateFrom2, dateFrom3, dateFrom4, dateTo1, dateTo2, dateTo3, dateTo4, osOnly1, osOnly2, osOnly3, osOnly3a, osOnly4, salesPerson4, which, unm, sid, uty, men, den,
                                    dnm, bnm, hmenuCount);

    if(plain)
      scoutln(out, bytesOut, authenticationUtils.buildPlainScreen(con, stmt, rs, req, unm, sid, uty, men, den, dnm, bnm, "TrackTraceDeliveryOrders", subMenuText, hmenuCount[0], bodyStr, otherSetup, localDefnsDir, defnsDir));
    else scoutln(out, bytesOut, authenticationUtils.buildScreen(con, stmt, rs, req, true, unm, sid, uty, men, den, dnm, bnm, "TrackTraceDeliveryOrders", subMenuText, hmenuCount[0], bodyStr, otherSetup, localDefnsDir, defnsDir));

    scoutln(out, bytesOut, "<table border=0><tr><td valign=top><div id='second' style=\"{margin-top:0px;}\"></div></td>");
    scoutln(out, bytesOut, "<td valign=top><div id='third' style=\"{margin-top:0px;margin-left:0px;}\"></div></td></tr></table>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String buildSubMenuText(boolean plain, String doCode, String itemCode1, String custCode, String dateFrom1, String dateFrom2, String dateFrom3, String dateFrom4, String dateTo1, String dateTo2, String dateTo3, String dateTo4,
                                  String osOnly1, String osOnly2, String osOnly3, String osOnly3a, String osOnly4, String salesPerson4, String which, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                                  int[] hmenuCount) throws Exception
  {
    String s = "<div id='submenu'>";

    ++hmenuCount[0];

    if(! plain)
    {
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\">";
      s += "<a href=\"/central/servlet/TrackTraceDeliveryOrdersa?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + doCode + "&itemCode1=" + itemCode1 + "&custCode=" + custCode + "&dateFrom1="
        + dateFrom1 + "&dateFrom2=" + dateFrom2 + "&dateFrom3=" + dateFrom3 + "&dateFrom4=" + dateFrom4 + "&dateTo1=" + dateTo1 + "&dateTo2=" + dateTo2 + "&dateTo3=" + dateTo3 + "&dateTo4=" + dateTo4 + "&osOnly1=" + osOnly1 + "&osOnly2="
        + osOnly2 + "&osOnly3=" + osOnly3 + "&osOnly3a=" + osOnly3a + "&osOnly4=" + osOnly4 + "&salesPerson4=" + salesPerson4 + "&which=" + which + "&p2=P\">Friendly</a></dt></dl>";
    }
    
    s += "</div>";
    
    --hmenuCount[0];
    
    return s;
  }

}
