// =======================================================================================================================================================================================================
// System: ZaraStar: TNT: Picking Lists
// Module: TrackTracePickingListsExecute.java
// Author: C.K.Harvey
// Copyright (c) 2001-07 Christopher Harvey. All Rights Reserved.
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

public class TrackTracePickingListsExecute extends HttpServlet
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
      p1  = req.getParameter("p1"); // plCode
      
      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, "", "", "", "", "", "", "", "", "", "", "", "", "", "", "1", bytesOut);
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
 
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "TrackTracePickingLists", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2025, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush(); 
    }
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", plCode="", itemCode1="", custCode="", dateFrom1="", dateFrom2="", dateFrom3="", dateFrom4="",
           dateTo1="", dateTo2="", dateTo3="", dateTo4="", osOnly1="N", osOnly2="N", osOnly3="N", osOnly4="N", which="";

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
        if(name.equals("plCode"))
          plCode = value[0];
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
        if(name.equals("osOnly4"))
          osOnly4 = value[0];
        else
        if(name.equals("which"))
          which = value[0];
      }
      
      if(plCode    == null) plCode    = "";
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
      if(osOnly4   == null) osOnly4   = "";

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, plCode, itemCode1, custCode, dateFrom1, dateFrom2, dateFrom3, dateFrom4,
           dateTo1, dateTo2, dateTo3, dateTo4, osOnly1, osOnly2, osOnly3, osOnly4, which, bytesOut);
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

      System.out.println("2025a: " + e);
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "TrackTracePickingListsa", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2025, bytesOut[0], 0, "ERR:" + plCode);
      if(out != null) out.flush(); 
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String plCode,
                    String itemCode1, String custCode, String dateFrom1, String dateFrom2, String dateFrom3, String dateFrom4, String dateTo1, String dateTo2,
                    String dateTo3, String dateTo4, String osOnly1, String osOnly2, String osOnly3, String osOnly4, String which, int[] bytesOut)
                    throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    
    Connection con = null;
    Statement stmt = null, stmt2 = null;
    ResultSet rs = null, rs2 = null;

    try
    {
      String uName = directoryUtils.getMySQLUserName();
      String pWord = directoryUtils.getMySQLPassWord();
      Class.forName("com.mysql.jdbc.Driver").newInstance();
      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + uName + "&password=" + pWord);

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 2025, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "TrackTracePickingLists", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2025, bytesOut[0], 0, "ACC:" + plCode);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "TrackTracePickingLists", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2025, bytesOut[0], 0, "SID:" + plCode);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    set(con, stmt, stmt2, rs, rs2, out, req, unm, sid, uty, men, den, dnm, bnm, plCode, itemCode1, custCode, dateFrom1, dateFrom2, dateFrom3, dateFrom4,
        dateTo1, dateTo2, dateTo3, dateTo4, osOnly1, osOnly2, osOnly3, osOnly4, which, imagesDir, localDefnsDir, defnsDir, bytesOut);
    }
    catch(Exception e) { } 

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 2025, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, HttpServletRequest req, String unm,
                   String sid, String uty, String men, String den, String dnm, String bnm, String plCode, String itemCode1, String custCode, String dateFrom1,
                   String dateFrom2, String dateFrom3, String dateFrom4, String dateTo1, String dateTo2, String dateTo3, String dateTo4, String osOnly1,
                   String osOnly2, String osOnly3, String osOnly4, String which, String imagesDir, String localDefnsDir, String defnsDir, int[] bytesOut)
                   throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Track &amp; Trace: Picking Lists</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3038, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewPL(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/GoodsReceivedPickingList?unm=" + unm + "&sid=" + sid + "&uty="
                             + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }
  
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3001, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewItem(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/ProductStockRecord?unm=" + unm + "&sid=" + sid + "&uty="
                           + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4001, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewCust(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/CustomerPage?unm=" + unm + "&sid=" + sid + "&uty="
                           + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p1=\"+p1;}");
    }

    scoutln(out, bytesOut, "function viewLines(code){var p1=sanitise(code);");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/TrackTracePickingListsa?unm=" + unm + "&sid=" + sid + "&uty="
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
 
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "2025", "", "TrackTracePickingLists", unm, sid, uty, men, den, dnm, bnm,
                          localDefnsDir, defnsDir, hmenuCount, bytesOut);

    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Track &amp; Trace: Picking Lists Results", "2025", unm, sid, uty, men, den, dnm, bnm, hmenuCount,
                    bytesOut);

    scoutln(out, bytesOut, "<form><table id=\"page\" width=100% border=0>");

    boolean error = false;
    if(which.equals("1") && plCode.length() == 0)
    {
      scoutln(out, bytesOut, "<br><br><span id=\"enquiryMessage\">&nbsp; No Picking List Code Specified</span><br><br>");
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

    if(which.equals("3") || which.equals("4"))
    {
      scoutln(out, bytesOut, "<td><p><b> Customer Code </b></td>");
      scoutln(out, bytesOut, "<td><p><b> Customer Name </b></td>");
      scoutln(out, bytesOut, "<td><p><b> PL Code </b></td>");
      scoutln(out, bytesOut, "<td><p><b> PL Date </b></td><td></td></tr>");
    }
    else
    {
      scoutln(out, bytesOut, "<td></td><td><p><b> Customer Code </b></td>");
      scoutln(out, bytesOut, "<td><p><b> Customer Name </b></td>");
      scoutln(out, bytesOut, "<td><p><b> PL Code </b></td>");
      scoutln(out, bytesOut, "<td><p><b> PL Line </b></td>");
      scoutln(out, bytesOut, "<td><p><b> PL Date </b></td>");
      scoutln(out, bytesOut, "<td><p><b> ItemCode </b></td>");
      scoutln(out, bytesOut, "<td><p><b> Quantity<br>Required </b></td>");
      scoutln(out, bytesOut, "<td><p><b> Quantity<br>Packed </b></td>");
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
    {
      fetchPLGivenPLCode(con, out, plCode, dpOnQuantities, cssFormat, oCount, imagesDir, bytesOut);
    }
    else
    if(which.equals("2"))
    {
      fetchPLsGivenItemCode(con, stmt, rs, out, itemCode1, dateFrom1, dateTo1, osOnly1, dpOnQuantities, cssFormat, oCount, bytesOut);
    }
    else
    if(which.equals("3"))
    {
      forPLsForACustomer(con, stmt, rs, out, custCode, dateFrom2, dateTo2, osOnly2, cssFormat, oCount, bytesOut);
    }
    else
    if(which.equals("4"))
    {
      forAllPLs(con, stmt, rs, out, dateFrom3, dateTo3, osOnly3, cssFormat, oCount, bytesOut);
    }
    else
    if(which.equals("5"))
    {
      forAllPLLines(con, stmt, stmt2, rs, rs2, out, dateFrom4, dateTo4, osOnly4, dpOnQuantities, cssFormat, oCount, bytesOut);
    }
    

    scoutln(out, bytesOut, "</table></form>");     scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void forAllPLs(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String dateFrom, String dateTo, String outstandingOnly,
                         String[] cssFormat, int[] oCount, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    String where = "";
      
    if(outstandingOnly.equals("on"))
      where += " AND Completed != 'Y' ";

    rs = stmt.executeQuery("SELECT PLCode, Date, CompanyCode, CompanyName FROM pl WHERE Status != 'C' AND Date >= {d '" + dateFrom
                         + "'} AND Date <= {d '" + dateTo + "'} " + where + " ORDER BY Date");
      
    String plCode, date, customerCode, customerName;
    
    while(rs.next())
    {    
      plCode       = rs.getString(1);
      date         = rs.getString(2);
      customerCode = rs.getString(3);
      customerName = rs.getString(4);
      
      processPLLine2(out, plCode, date, customerCode, customerName, cssFormat, oCount, bytesOut);
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void forPLsForACustomer(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String custCode, String dateFrom, String dateTo,
                                  String outstandingOnly, String[] cssFormat, int[] oCount, int[] bytesOut) throws Exception
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
      where += " AND Completed != 'Y' ";

    rs = stmt.executeQuery("SELECT PLCode, Date, CompanyName, Completed FROM pl WHERE Status != 'C' AND CompanyCode = '" + custCode + "' "  + where
                         + " ORDER BY Date");
      
    String plCode, date, customerName, completed;
    
    while(rs.next())
    {    
      plCode       = rs.getString(1);
      date         = rs.getString(2);
      customerName = rs.getString(3);
      completed    = rs.getString(4);
      
      processPLLine2(out, plCode, date, custCode, customerName, cssFormat, oCount, bytesOut);
    }
  
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void fetchPLGivenPLCode(Connection con, PrintWriter out, String plCode, char dpOnQuantities, String[] cssFormat, int[] oCount, String imagesDir,
                                  int[] bytesOut) throws Exception
  {
    Statement stmt = con.createStatement();

    plCode = plCode.toUpperCase();

    ResultSet rs = stmt.executeQuery("SELECT t1.Date, t1.CompanyCode, t2.Description, t2.QuantityRequired, t2.QuantityPacked, t2.Line, t2.ItemCode, "
                                   + "t1.Status, t1.CompanyName FROM pll AS t2 INNER JOIN pl AS t1 ON t2.plCode = t1.plCode WHERE t2.plCode = '"
                                   + plCode + "' ORDER BY t1.Date");

    String date, customerCode, desc, quantityRequired, quantityPacked, line, itemCode, status, customerName;

    while(rs.next())
    {    
      date             = rs.getString(1);
      customerCode     = rs.getString(2);
      desc             = rs.getString(3);
      quantityRequired = rs.getString(4);
      quantityPacked   = rs.getString(5);
      line             = rs.getString(6);
      itemCode         = rs.getString(7);
      status           = rs.getString(8);
      customerName     = rs.getString(9);
      
      processPLLine(out, plCode, line, date, customerCode, customerName, itemCode, quantityRequired, quantityPacked, desc, status, dpOnQuantities,
                    cssFormat, imagesDir, oCount, bytesOut);
    }
  
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void fetchPLsGivenItemCode(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String itemCode, String dateFrom, String dateTo,
                                     String outstandingOnly, char dpOnQuantities, String[] cssFormat, int[] oCount, int[] bytesOut) throws Exception
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
      where += " AND Completed != 'Y' ";

    rs = stmt.executeQuery("SELECT t1.plCode, t1.Date, t1.CompanyCode, t2.Description, t2.QuantityRequired, t2.QuantityPacked, t2.Line, "
                         + "t1.CompanyName FROM pll AS t2 INNER JOIN pl AS t1 ON t2.plCode = t1.plCode WHERE t1.Status != 'C' AND " + where
                         + " ORDER BY t1.Date");
      
    String plCode, date, customerCode, desc, quantityRequired, quantityPacked, line, customerName;
    
    while(rs.next())
    {    
      plCode           = rs.getString(1);
      date             = rs.getString(2);
      customerCode     = rs.getString(3);
      desc             = rs.getString(4);
      quantityRequired = rs.getString(5);
      quantityPacked   = rs.getString(6);
      line             = rs.getString(7);
      customerName     = rs.getString(8);
      
      processPLLine(out, plCode, line, date, customerCode, customerName, itemCode, quantityRequired, quantityPacked, desc, "", dpOnQuantities, cssFormat, "",
                    oCount, bytesOut);
    }
  
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void processPLLine(PrintWriter out, String plCode, String plLine, String plDate, String customerCode, String customerName, String itemCode,
                             String quantityRequired, String quantityPacked, String desc, String status, char dpOnQuantities, String[] cssFormat,
                             String imagesDir, int[] oCount, int[] bytesOut) throws Exception
  {
    byte[] b  = new byte[20];
    byte[] b2 = new byte[20];
    double qtyReqd = generalUtils.doubleFromStr(quantityRequired);
    double qtyPkd  = generalUtils.doubleFromStr(quantityPacked);
    
    generalUtils.doubleToBytesCharFormat(qtyReqd, b, 0);
    generalUtils.bytesDPsGivenSeparator(true, '\000', dpOnQuantities, b, 20, 0);
    generalUtils.formatNumeric(b, dpOnQuantities);

    generalUtils.doubleToBytesCharFormat(qtyPkd, b2, 0);
    generalUtils.bytesDPsGivenSeparator(true, '\000', dpOnQuantities, b2, 20, 0);
    generalUtils.formatNumeric(b2, dpOnQuantities);

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
    scoutln(out, bytesOut, "<td><p><a href=\"javascript:viewPL('" + plCode + "')\">" + plCode + "</a></td>");
    scout(out, bytesOut, "<td nowrap><p>" + plLine + "</td>");
    scout(out, bytesOut, "<td><p>" + generalUtils.convertFromYYYYMMDD(plDate) + "</td>");
    scout(out, bytesOut, "<td><p><a href=\"javascript:viewItem('" + itemCode + "')\">" + itemCode + "</a></td>");
    scoutln(out, bytesOut, "<td align=center><p>" + generalUtils.stringFromBytes(b,  0L) + "</td>");
    scoutln(out, bytesOut, "<td align=center><p>" + generalUtils.stringFromBytes(b2, 0L) + "</td>");
    scoutln(out, bytesOut, "<td nowrap><p>" + desc +"</td></tr>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
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
  private void forAllPLLines(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, String dateFrom, String dateTo,
                             String outstandingOnly, char dpOnQuantities, String[] cssFormat, int[] oCount, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();
    
    String where = "";
    
    if(outstandingOnly.equals("on"))
      where += " AND Completed != 'Y' ";

    rs = stmt.executeQuery("SELECT PLCode, Date, CompanyCode, CompanyName FROM pl WHERE Status != 'C' AND Date >= {d '" + dateFrom
                         + "'} AND Date <= {d '" + dateTo + "'} " + where + " ORDER BY Date");
      
    String plCode, date, customerCode, companyName;
    
    while(rs.next())
    {    
      plCode       = rs.getString(1);
      date         = rs.getString(2);
      customerCode = rs.getString(3);
      companyName  = rs.getString(4);
      
      forPLLines(con, stmt2, rs2, out, plCode, date, customerCode, companyName, dpOnQuantities, cssFormat, oCount, bytesOut);
    }
  
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void forPLLines(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String plCode, String plDate, String customerCode, String companyName,
                          char dpOnQuantities, String[] cssFormat, int[] oCount, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT QuantityRequired, QuantityPacked, Line, ItemCode, Description FROM pll WHERE plCode = '" + plCode + "'");
      
    String line, quantityRequired, quantityPacked, itemCode, desc;
    
    while(rs.next())
    {    
      quantityRequired = rs.getString(1);
      quantityPacked   = rs.getString(2);
      line             = rs.getString(3);
      itemCode         = rs.getString(4);
      desc             = rs.getString(5);
      
      processPLLine(out, plCode, line, plDate, customerCode, companyName, itemCode, quantityRequired, quantityPacked, desc, "", dpOnQuantities, cssFormat, "",
                    oCount, bytesOut);
    }
  
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void processPLLine2(PrintWriter out, String plCode, String plDate, String customerCode, String companyName, String[] cssFormat, int[] oCount,
                              int[] bytesOut) throws Exception
  {
    if(cssFormat[0].equals("line1"))
      cssFormat[0] = "line2";
    else cssFormat[0] = "line1";
 
    scoutln(out, bytesOut, "<tr  id=\"" + cssFormat[0] + "\">");

    scoutln(out, bytesOut, "<td><p>" + oCount[0]++ + "</td>");
    scout(out, bytesOut, "<td><p><a href=\"javascript:viewCust('" + customerCode + "')\">" + customerCode + "</a></td>");
    scout(out, bytesOut, "<td nowrap><p>" + companyName + "</td>");
    scoutln(out, bytesOut, "<td><p><a href=\"javascript:viewPL('" + plCode + "')\">" + plCode + "</a></td>");
    scout(out, bytesOut, "<td><p>" + generalUtils.convertFromYYYYMMDD(plDate) + "</td>");
    scoutln(out, bytesOut, "<td><p><a href=\"javascript:viewLines('" + plCode + "')\">View Lines</a></td></tr>");
  }

}
