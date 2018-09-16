// =======================================================================================================================================================================================================
// System: ZaraStar: TNT: Goods Received Notes
// Module: TrackTraceGoodsReceivedExecute.java
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

public class TrackTraceGoodsReceivedExecute extends HttpServlet
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
      p1  = req.getParameter("p1"); // grCode
      
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
 
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "TrackTraceGoodsReceived", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2041, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush(); 
    }
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", grCode="", itemCode1="", suppCode="", dateFrom1="", dateFrom2="", dateFrom3="", dateFrom4="",
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
        if(name.equals("grCode"))
          grCode = value[0];
        else
        if(name.equals("itemCode1"))
          itemCode1 = value[0];
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
      
      if(grCode    == null) grCode    = "";
      if(itemCode1 == null) itemCode1 = "";
      if(suppCode  == null) suppCode  = "";
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

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, grCode, itemCode1, suppCode, dateFrom1, dateFrom2, dateFrom3, dateFrom4, dateTo1, dateTo2, dateTo3,
           dateTo4, osOnly1, osOnly2, osOnly3, osOnly4, which, bytesOut);
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

      System.out.println("2041a: " + e);
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "TrackTraceGoodsReceivedExecute", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2041, bytesOut[0], 0, "ERR:" + grCode);
      if(out != null) out.flush(); 
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String grCode,
                    String itemCode1, String suppCode, String dateFrom1, String dateFrom2, String dateFrom3, String dateFrom4, String dateTo1, String dateTo2,
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

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 2041, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "TrackTraceGoodsReceived", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2041, bytesOut[0], 0, "ACC:" + grCode);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "TrackTraceGoodsReceived", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2041, bytesOut[0], 0, "SID:" + grCode);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    set(con, stmt, stmt2, rs, rs2, out, req, unm, sid, uty, men, den, dnm, bnm, grCode, itemCode1, suppCode, dateFrom1, dateFrom2, dateFrom3, dateFrom4,
        dateTo1, dateTo2, dateTo3, dateTo4, osOnly1, osOnly2, osOnly3, osOnly4, which, imagesDir, localDefnsDir, defnsDir, bytesOut);
    }
    catch(Exception e) { } 

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 2041, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, HttpServletRequest req, String unm,
                   String sid, String uty, String men, String den, String dnm, String bnm, String grCode, String itemCode1, String suppCode, String dateFrom1,
                   String dateFrom2, String dateFrom3, String dateFrom4, String dateTo1, String dateTo2, String dateTo3, String dateTo4, String osOnly1,
                   String osOnly2, String osOnly3, String osOnly4, String which, String imagesDir, String localDefnsDir, String defnsDir, int[] bytesOut)
                   throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Track &amp; Trace: Goods Received Notes</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3025, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewGR(code){var p1=sanitise(code);");
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
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/TrackTraceGoodsReceivedExecute?unm=" + unm + "&sid=" + sid + "&uty="
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
 
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "2041", "", "TrackTraceGoodsReceived", unm, sid, uty, men, den, dnm, bnm,
                          localDefnsDir, defnsDir, hmenuCount, bytesOut);

    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Track &amp; Trace: Goods Received Notes Results", "2041", unm, sid, uty, men, den, dnm, bnm,
                    hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<form><table id=\"page\" width=100% border=0>");

    boolean error = false;
    if(which.equals("1") && grCode.length() == 0)
    {
      scoutln(out, bytesOut, "<br><br><span id=\"enquiryMessage\">&nbsp; No Goods Received Note Code Specified</span><br><br>");
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
        Customer customer = new Customer();
        if(! customer.existsCompanyRecGivenCode(con, stmt, rs, suppCode, dnm, localDefnsDir, defnsDir))
        {
          scoutln(out, bytesOut, "<br><br><span id=\"enquiryMessage\">Supplier Code &nbsp; &nbsp; " + suppCode);
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
      scoutln(out, bytesOut, "</table></form>");
      scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
      return;
    }
    
    scoutln(out, bytesOut, "<tr><td></td></tr><tr id=\"pageColumn\"><td></td>");

    if(which.equals("3") || which.equals("4"))
    {
      scoutln(out, bytesOut, "<td><p><b> Supplier Code </b></td>");
      scoutln(out, bytesOut, "<td><p><b> Supplier Name </b></td>");
      scoutln(out, bytesOut, "<td><p><b> GR Code </b></td>");
      scoutln(out, bytesOut, "<td><p><b> GR Date </b></td><td></td></tr>");
    }
    else
    {
      scoutln(out, bytesOut, "<td></td><td><p><b> Supplier Code </b></td>");
      scoutln(out, bytesOut, "<td><p><b> Supplier Name </b></td>");
      scoutln(out, bytesOut, "<td><p><b> GR Code </b></td>");
      scoutln(out, bytesOut, "<td><p><b> GR Line </b></td>");
      scoutln(out, bytesOut, "<td><p><b> GR Date </b></td>");
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
    {
      fetchGRGivenGRCode(con, out, grCode, dpOnQuantities, cssFormat, oCount, imagesDir, bytesOut);
    }
    else
    if(which.equals("2"))
    {
      fetchGRsGivenItemCode(con, stmt, rs, out, itemCode1, dateFrom1, dateTo1, osOnly1, dpOnQuantities, cssFormat, oCount, bytesOut);
    }
    else
    if(which.equals("3"))
    {
      forGRsForASupplier(con, stmt, rs, out, suppCode, dateFrom2, dateTo2, osOnly2, cssFormat, oCount, bytesOut);
    }
    else
    if(which.equals("4"))
    {
      forAllGRs(con, stmt, rs, out, dateFrom3, dateTo3, osOnly3, cssFormat, oCount, bytesOut);
    }
    else
    if(which.equals("5"))
    {
      forAllGRLines(con, stmt, stmt2, rs, rs2, out, dateFrom4, dateTo4, osOnly4, dpOnQuantities, cssFormat, oCount, bytesOut);
    }
    

    scoutln(out, bytesOut, "</table></form>");     scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void forAllGRs(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String dateFrom, String dateTo, String outstandingOnly,
                         String[] cssFormat, int[] oCount, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    String where = "";
      
    if(outstandingOnly.equals("on"))
      where += " AND Comgreted != 'Y' ";

    rs = stmt.executeQuery("SELECT GRCode, Date, CompanyCode, CompanyName FROM gr WHERE Status != 'C' AND Date >= {d '" + dateFrom
                         + "'} AND Date <= {d '" + dateTo + "'} " + where + " ORDER BY Date");
      
    String grCode, date, supplierCode, supplierName;
    
    while(rs.next())
    {    
      grCode       = rs.getString(1);
      date         = rs.getString(2);
      supplierCode = rs.getString(3);
      supplierName = rs.getString(4);
      
      processGRLine2(out, grCode, date, supplierCode, supplierName, cssFormat, oCount, bytesOut);
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void forGRsForASupplier(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String suppCode, String dateFrom, String dateTo,
                                  String outstandingOnly, String[] cssFormat, int[] oCount, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    suppCode = suppCode.toUpperCase();

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
    
    rs = stmt.executeQuery("SELECT GRCode, Date, CompanyName FROM gr WHERE Status != 'C' AND CompanyCode = '" + suppCode + "' "  + where
                         + " ORDER BY Date");
      
    String grCode, date, supplierName;
    
    while(rs.next())
    {    
      grCode       = rs.getString(1);
      date         = rs.getString(2);
      supplierName = rs.getString(3);
      
      processGRLine2(out, grCode, date, suppCode, supplierName, cssFormat, oCount, bytesOut);
    }
  
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void fetchGRGivenGRCode(Connection con, PrintWriter out, String grCode, char dpOnQuantities, String[] cssFormat, int[] oCount, String imagesDir,
                                  int[] bytesOut) throws Exception
  {
    Statement stmt = con.createStatement();

    grCode = grCode.toUpperCase();

    ResultSet rs = stmt.executeQuery("SELECT t1.Date, t1.CompanyCode, t2.Description, t2.Quantity, t2.Line, t2.ItemCode, "
                                   + "t1.Status, t1.CompanyName FROM grl AS t2 INNER JOIN gr AS t1 ON t2.grCode = t1.grCode WHERE t2.grCode = '"
                                   + grCode + "' ORDER BY t1.Date");

    String date, supplierCode, desc, quantity, line, itemCode, status, supplierName;

    while(rs.next())
    {    
      date             = rs.getString(1);
      supplierCode     = rs.getString(2);
      desc             = rs.getString(3);
      quantity         = rs.getString(4);
      line             = rs.getString(5);
      itemCode         = rs.getString(6);
      status           = rs.getString(7);
      supplierName     = rs.getString(8);
      
      processGRLine(out, grCode, line, date, supplierCode, supplierName, itemCode, quantity, desc, status, dpOnQuantities, cssFormat, imagesDir, oCount,
                    bytesOut);
    }
  
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void fetchGRsGivenItemCode(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String itemCode, String dateFrom, String dateTo,
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
      where += " AND Comgreted != 'Y' ";

    rs = stmt.executeQuery("SELECT t1.grCode, t1.Date, t1.CompanyCode, t2.Description, t2.Quantity, t2.Line, "
                         + "t1.CompanyName FROM grl AS t2 INNER JOIN gr AS t1 ON t2.grCode = t1.grCode WHERE t1.Status != 'C' AND " + where
                         + " ORDER BY t1.Date");
      
    String grCode, date, supplierCode, desc, quantity, line, supplierName;
    
    while(rs.next())
    {    
      grCode           = rs.getString(1);
      date             = rs.getString(2);
      supplierCode     = rs.getString(3);
      desc             = rs.getString(4);
      quantity         = rs.getString(5);
      line             = rs.getString(6);
      supplierName     = rs.getString(7);
      
      processGRLine(out, grCode, line, date, supplierCode, supplierName, itemCode, quantity, desc, "", dpOnQuantities, cssFormat, "",
                    oCount, bytesOut);
    }
  
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void processGRLine(PrintWriter out, String grCode, String grLine, String grDate, String supplierCode, String supplierName, String itemCode,
                             String quantity, String desc, String status, char dpOnQuantities, String[] cssFormat,
                             String imagesDir, int[] oCount, int[] bytesOut) throws Exception
  {
    byte[] b  = new byte[20];
    double qty = generalUtils.doubleFromStr(quantity);
    
    generalUtils.doubleToBytesCharFormat(qty, b, 0);
    generalUtils.bytesDPsGivenSeparator(true, '\000', dpOnQuantities, b, 20, 0);
    generalUtils.formatNumeric(b, dpOnQuantities);

    if(cssFormat[0].equals("line1")) cssFormat[0] = "line2"; else cssFormat[0] = "line1";

    scoutln(out, bytesOut, "<tr  id=\"" + cssFormat[0] + "\">");

    if(status.equals("C"))
      scout(out, bytesOut, "<td align=center><img src=\"" + imagesDir + "z0212.gif\" border=0></td>");
    else scout(out, bytesOut, "<td></td>");

    scoutln(out, bytesOut, "<td><p>" + oCount[0]++ + "</td>");
    scout(out, bytesOut, "<td><p><a href=\"javascript:viewSupp('" + supplierCode + "')\">" + supplierCode + "</a></td>");
    scout(out, bytesOut, "<td nowrap><p>" + supplierName + "</td>");
    scoutln(out, bytesOut, "<td><p><a href=\"javascript:viewGR('" + grCode + "')\">" + grCode + "</a></td>");
    scout(out, bytesOut, "<td nowrap><p>" + grLine + "</td>");
    scout(out, bytesOut, "<td><p>" + generalUtils.convertFromYYYYMMDD(grDate) + "</td>");
    scout(out, bytesOut, "<td><p><a href=\"javascript:viewItem('" + itemCode + "')\">" + itemCode + "</a></td>");
    scoutln(out, bytesOut, "<td align=center><p>" + generalUtils.stringFromBytes(b,  0L) + "</td>");
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
  private void forAllGRLines(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, String dateFrom, String dateTo,
                             String outstandingOnly, char dpOnQuantities, String[] cssFormat, int[] oCount, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();
    
    String where = "";
    
    if(outstandingOnly.equals("on"))
      where += " AND Comgreted != 'Y' ";

    rs = stmt.executeQuery("SELECT GRCode, Date, CompanyCode, CompanyName FROM gr WHERE Status != 'C' AND Date >= {d '" + dateFrom
                         + "'} AND Date <= {d '" + dateTo + "'} " + where + " ORDER BY Date");
      
    String grCode, date, supplierCode, companyName;
    
    while(rs.next())
    {    
      grCode       = rs.getString(1);
      date         = rs.getString(2);
      supplierCode = rs.getString(3);
      companyName  = rs.getString(4);
      
      forGRLines(con, stmt2, rs2, out, grCode, date, supplierCode, companyName, dpOnQuantities, cssFormat, oCount, bytesOut);
    }
  
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void forGRLines(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String grCode, String grDate, String supplierCode, String companyName,
                          char dpOnQuantities, String[] cssFormat, int[] oCount, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT Quantity, Line, ItemCode, Description FROM grl WHERE grCode = '" + grCode + "'");
      
    String line, quantity, itemCode, desc;
    String[] grCodes = new String[1];
    double qty, totalQty=0, totalActualQty=0;
    
    while(rs.next())
    {    
      quantity = rs.getString(1);
      line             = rs.getString(2);
      itemCode         = rs.getString(3);
      desc             = rs.getString(4);
      
      processGRLine(out, grCode, line, grDate, supplierCode, companyName, itemCode, quantity, desc, "", dpOnQuantities, cssFormat, "",
                    oCount, bytesOut);
    }
  
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void processGRLine2(PrintWriter out, String grCode, String grDate, String supplierCode, String companyName, String[] cssFormat, int[] oCount,
                              int[] bytesOut) throws Exception
  {
    if(cssFormat[0].equals("line1"))
      cssFormat[0] = "line2";
    else cssFormat[0] = "line1";
 
    scoutln(out, bytesOut, "<tr  id=\"" + cssFormat[0] + "\">");

    scoutln(out, bytesOut, "<td><p>" + oCount[0]++ + "</td>");
    scout(out, bytesOut, "<td><p><a href=\"javascript:viewSupp('" + supplierCode + "')\">" + supplierCode + "</a></td>");
    scout(out, bytesOut, "<td nowrap><p>" + companyName + "</td>");
    scoutln(out, bytesOut, "<td><p><a href=\"javascript:viewGR('" + grCode + "')\">" + grCode + "</a></td>");
    scout(out, bytesOut, "<td><p>" + generalUtils.convertFromYYYYMMDD(grDate) + "</td>");
    scoutln(out, bytesOut, "<td><p><a href=\"javascript:viewLines('" + grCode + "')\">View Lines</a></td></tr>");
  }

}
