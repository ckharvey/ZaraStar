// =======================================================================================================================================================================================================
// System: ZaraStar: TNT: Quotes
// Module: TrackTraceQuotationsExecute.java
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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;
import java.io.*;

public class TrackTraceQuotationsExecute extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  MiscDefinitions miscDefinitions = new MiscDefinitions();

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
      p1  = req.getParameter("p1"); // quoteCode
      
      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "1", bytesOut);
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
 
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "TrackTraceQuotations", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2024, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush(); 
    }
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", quoteCode="", itemCode1="", itemCode2="", custCode="", dateFrom1="", dateFrom2="", dateFrom3="", dateFrom4="", dateTo1="", dateTo2="", dateTo3="", dateTo4="", state1="",
           state2="", state3="", state4="", state5="", which="";

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
        if(name.equals("quoteCode"))
          quoteCode = value[0];
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
        if(name.equals("state1"))
          state1 = value[0];
        else
        if(name.equals("state2"))
          state2 = value[0];
        else
        if(name.equals("state3"))
          state3 = value[0];
        else
        if(name.equals("state4"))
          state4 = value[0];
        else
        if(name.equals("state5"))
          state5 = value[0];
        else
        if(name.equals("which"))
          which = value[0];
      }
      
      if(quoteCode == null) quoteCode = "";
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
      if(state1    == null) state1    = "";
      if(state2    == null) state2    = "";
      if(state3    == null) state3    = "";
      if(state4    == null) state4    = "";
      if(state5    == null) state5    = "";

      state5 = generalUtils.deSanitise(state5);

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, quoteCode, itemCode1, itemCode2, custCode, dateFrom1, dateFrom2, dateFrom3, dateFrom4, dateTo1, dateTo2, dateTo3, dateTo4, state1, state2, state3, state4, state5, which, bytesOut);
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

      System.out.println("2024a: " + e);
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "TrackTraceQuotationsa", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2024, bytesOut[0], 0, "ERR:" + quoteCode);
      if(out != null) out.flush(); 
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String quoteCode, String itemCode1, String itemCode2, String custCode, String dateFrom1,
                    String dateFrom2, String dateFrom3, String dateFrom4, String dateTo1, String dateTo2, String dateTo3, String dateTo4, String state1, String state2, String state3, String state4, String state5, String which, int[] bytesOut)
                    throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    
    String userName = directoryUtils.getMySQLUserName();
    String passWord = directoryUtils.getMySQLPassWord();
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + userName + "&password=" + passWord);
    Statement stmt = null;
    ResultSet rs = null;
    
    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 2024, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "TrackTraceQuotations", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2024, bytesOut[0], 0, "ACC:" + quoteCode);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "TrackTraceQuotations", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2024, bytesOut[0], 0, "SID:" + quoteCode);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    set(con, stmt, rs, out, req, unm, sid, uty, men, den, dnm, bnm, quoteCode, itemCode1, itemCode2, custCode, dateFrom1, dateFrom2, dateFrom3, dateFrom4, dateTo1, dateTo2, dateTo3, dateTo4, state1, state2, state3, state4, state5, which,
        imagesDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 2024, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con  != null) con.close();
    if(out != null) out.flush(); 
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String quoteCode, String itemCode1, String itemCode2,
                   String custCode, String dateFrom1, String dateFrom2, String dateFrom3, String dateFrom4, String dateTo1, String dateTo2, String dateTo3, String dateTo4, String state1, String state2, String state3, String state4, String state5,
                   String which, String imagesDir, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Track &amp; Trace: Quotations</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4019, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewQuote(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/QuotationPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3001, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewItem(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/ProductStockRecord?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4001, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewCustupp(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/CustomerPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p1=\"+p1;}");
    }

    scoutln(out, bytesOut, "function viewLines(code){var p1=sanitise(code);");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/TrackTraceQuotationsa?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p1=\"+p1;}");

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
 
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "2024", "", "TrackTraceQuotations", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Track &amp; Trace: Quotations Results", "2024", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<form><table id=\"page\" width=100% border=0>");

    boolean error = false;
    if(which.equals("1") && quoteCode.length() == 0)
    {
      scoutln(out, bytesOut, "<br><br><span id=\"enquiryMessage\">&nbsp; No Quotation Code Specified</span><br><br>");
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
          scoutln(out, bytesOut, "<br><br><span id=\"enquiryMessage\">quote Code &nbsp; &nbsp; " + custCode + "&nbsp; &nbsp; <font color=\"#FF0000\">Not Found</span><br><br>");
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

    if(which.equals("5"))
    {
      scoutln(out, bytesOut, "<td><p><b> Customer Code </b></td>");
      scoutln(out, bytesOut, "<td><p><b> Customer Name </b></td>");
      scoutln(out, bytesOut, "<td><p><b> Quotation Code </b></td>");
      scoutln(out, bytesOut, "<td><p><b> Quotation Date </b></td>");
      scoutln(out, bytesOut, "<td><p><b> Amount </b></td>");
      scoutln(out, bytesOut, "<td><p><b> Currency </b></td>");
      scoutln(out, bytesOut, "<td><p><b> SalesPerson </b></td>");
      scoutln(out, bytesOut, "<td><p><b> Status </b></td><td></td></tr>");
    }
    else
    {
      scoutln(out, bytesOut, "<td></td><td><p><b> Customer Code </b></td>");
      scoutln(out, bytesOut, "<td><p><b> Quotation Code </b></td>");
      scoutln(out, bytesOut, "<td><p><b> Quotation Date </b></td>");
      scoutln(out, bytesOut, "<td><p><b> ItemCode </b></td>");
      scoutln(out, bytesOut, "<td><p><b> Quantity </b></td>");
      scoutln(out, bytesOut, "<td><p><b> Status </b></td>");
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
      fetchQuoteGivenQuoteCode(con, stmt, rs, out, quoteCode, dpOnQuantities, cssFormat, oCount, dnm, imagesDir, localDefnsDir, defnsDir, bytesOut);
    }
    else
    if(which.equals("2"))
    {
      fetchQuotesGivenItemCode(con, stmt, rs, out, itemCode1, dateFrom1, dateTo1, state1, dpOnQuantities, cssFormat, oCount, dnm, localDefnsDir, defnsDir, bytesOut);
    }
    else
    if(which.equals("3"))
    {
      forQuotesForACustomer(con, stmt, rs, out, custCode, itemCode2, dateFrom2, dateTo2, state2, dpOnQuantities, cssFormat, oCount, bytesOut);
    }
    else
    if(which.equals("4"))
    {
      forAllQuotes(con, stmt, rs, out, dateFrom3, dateTo3, state3, dpOnQuantities, cssFormat, oCount, bytesOut);
    }
    else
    if(which.equals("5"))
    {
      forAllQuoteHeaders(con, stmt, rs, out, dateFrom4, dateTo4, state4, state5, cssFormat, oCount, bytesOut);
    }
    

    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void forAllQuotes(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String dateFrom, String dateTo, String state, char dpOnQuantities, String[] cssFormat, int[] oCount, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    String s = "";
    if(! state.equals("no-state-given"))
      s = " AND QuoteStatus = '" + state + "' ";
    
    rs = stmt.executeQuery("SELECT t1.QuoteCode, t1.QuoteDate, t1.CompanyCode, t1.QuoteStatus, t2.Description, t2.Quantity, t2.Line, t2.ItemCode FROM quotel AS t2 INNER JOIN quote AS t1 ON t2.quoteCode = t1.quoteCode "
                         + "WHERE t1.Status != 'C' AND t1.QuoteDate >= {d '" + dateFrom + "'} AND t1.QuoteDate <= {d '" + dateTo + "'} " + s + " ORDER BY t1.QuoteDate");
      
    String quoteCode, date, customerCode, desc, quantity, line, itemCode, quoteStatus;
    
    while(rs.next())
    {    
      quoteCode    = rs.getString(1);
      date         = rs.getString(2);
      customerCode = rs.getString(3);
      quoteStatus  = rs.getString(4);
      desc         = rs.getString(5);
      quantity     = rs.getString(6);
      line         = rs.getString(7);
      itemCode     = rs.getString(8);
      
      processLine(out, quoteCode, date, customerCode, itemCode, quantity, desc, quoteStatus, "", dpOnQuantities, cssFormat, "", oCount, bytesOut);
    }
  
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void forQuotesForACustomer(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String custCode, String itemCode, String dateFrom, String dateTo, String state, char dpOnQuantities, String[] cssFormat, int[] oCount,
                                     int[] bytesOut) throws Exception
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
      
      if(itemCode.length() == 0)
        where = "AND t1.QuoteDate >= {d '" + dateFrom + "'} AND t1.QuoteDate <= {d '" + dateTo + "'}";
      else where = "AND t2.ItemCode = '" + itemCode + "' AND t1.QuoteDate >= {d '" + dateFrom + "'} AND t1.QuoteDate <= {d '" + dateTo + "'}";
    }
    else // no date specified
    {
      if(itemCode.length() == 0)
        where = "";
      else where = "AND t2.ItemCode = '" + itemCode + "'";
    }
    
    String s = "";
    if(! state.equals("no-state-given"))
      s = " AND QuoteStatus = '" + state + "' ";
    
    rs = stmt.executeQuery("SELECT t1.quoteCode, t1.QuoteDate, t1.QuoteStatus, t2.Description, t2.Quantity, t2.Line, t2.ItemCode FROM quotel AS t2 INNER JOIN quote AS t1 ON t2.quoteCode = t1.quoteCode "
                         + "WHERE t1.Status != 'C' AND t1.CompanyCode = '" + custCode + "'" + s + where + " ORDER BY t1.QuoteDate");
      
    String quoteCode, date, desc, quantity, line, quoteStatus;
    
    while(rs.next())
    {    
      quoteCode   = rs.getString(1);
      date        = rs.getString(2);
      quoteStatus = rs.getString(3);
      desc        = rs.getString(4);
      quantity    = rs.getString(5);
      line        = rs.getString(6);
      itemCode    = rs.getString(7);
      
      processLine(out, quoteCode, date, custCode, itemCode, quantity, desc, quoteStatus, "", dpOnQuantities, cssFormat, "", oCount, bytesOut);
    }
  
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void fetchQuoteGivenQuoteCode(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String quoteCode, char dpOnQuantities, String[] cssFormat, int[] oCount, String dnm, String imagesDir, String localDefnsDir, String defnsDir,
                                        int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    quoteCode = quoteCode.toUpperCase();

    rs = stmt.executeQuery("SELECT t1.QuoteDate, t1.CompanyCode, t2.Description, t2.Quantity, t2.Line, t2.ItemCode, t1.Status, t1.QuoteStatus FROM quotel AS t2 INNER JOIN quote AS t1 ON t2.quoteCode = t1.quoteCode WHERE t2.quoteCode = '"
                         + quoteCode + "' ORDER BY t1.QuoteDate");

    String date, customerCode, desc, quantity, line, itemCode, status, state;

    while(rs.next())
    {    
      date         = rs.getString(1);
      customerCode = rs.getString(2);
      desc         = rs.getString(3);
      quantity     = rs.getString(4);
      line         = rs.getString(5);
      itemCode     = rs.getString(6);
      status       = rs.getString(7);
      state        = rs.getString(8);
      
      processLine(out, quoteCode, date, customerCode, itemCode, quantity, desc, state, status, dpOnQuantities, cssFormat, imagesDir, oCount, bytesOut);
    }
  
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void fetchQuotesGivenItemCode(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String itemCode, String dateFrom, String dateTo, String state, char dpOnQuantities, String[] cssFormat, int[] oCount, String dnm,
                                        String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
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
        where = "t1.QuoteDate >= {d '" + dateFrom + "'} AND t1.QuoteDate <= {d '" + dateTo + "'}";
      else where = "t2.ItemCode = '" + itemCode + "' AND t1.QuoteDate >= {d '" + dateFrom + "'} AND t1.QuoteDate <= {d '" + dateTo + "'}";
    }
    else // no date specified
    {
      if(itemCode.length() == 0)
        where = "";
      else where = "t2.ItemCode = '" + itemCode + "'";
    }
    
    String s = "";
    if(! state.equals("no-state-given"))
      s = " AND QuoteStatus = '" + state + "' ";

    rs = stmt.executeQuery("SELECT t1.QuoteCode, t1.QuoteDate, t1.CompanyCode, t1.QuoteStatus, t2.Description, t2.Quantity, t2.Line FROM quotel AS t2 INNER JOIN quote AS t1 ON t2.QuoteCode = t1.QuoteCode WHERE Status != 'C' AND " + where + s
                         + " ORDER BY t1.QuoteDate");
      
    String quoteCode, date, customerCode, desc, quantity, line, quoteStatus;
    
    while(rs.next())
    {    
      quoteCode    = rs.getString(1);
      date         = rs.getString(2);
      customerCode = rs.getString(3);
      quoteStatus  = rs.getString(4);
      desc         = rs.getString(5);
      quantity     = rs.getString(6);
      line         = rs.getString(7);
      
      processLine(out, quoteCode, date, customerCode, itemCode, quantity, desc, quoteStatus, "", dpOnQuantities, cssFormat, "", oCount, bytesOut);
    }
  
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void processLine(PrintWriter out, String quoteCode, String quoteDate, String customerCode, String itemCode, String quantity, String desc, String state, String status, char dpOnQuantities, String[] cssFormat, String imagesDir,
                           int[] oCount, int[] bytesOut) throws Exception
  {
    byte[] b = new byte[20];
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
    scout(out, bytesOut, "<td><p>" + customerCode + "</td>");
    scoutln(out, bytesOut, "<td><p><a href=\"javascript:viewQuote('" + quoteCode + "')\">" + quoteCode + "</a></td>");
    scout(out, bytesOut, "<td><p>" + generalUtils.convertFromYYYYMMDD(quoteDate) + "</td>");
    scout(out, bytesOut, "<td><p><a href=\"javascript:viewItem('" + itemCode + "')\">" + itemCode + "</a></td>");
    scoutln(out, bytesOut, "<td align=center><p>" + generalUtils.stringFromBytes(b, 0L) + "</td>");
    scout(out, bytesOut, "<td align=center><p>" + state + "</td>");
    scoutln(out, bytesOut, "<td nowrap><p>" + desc +"</td></tr>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
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
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void forAllQuoteHeaders(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String dateFrom, String dateTo, String state, String state2, String[] cssFormat, int[] oCount, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();
    
    String s = "";
    if(! state.equals("no-state-given"))
      s = " AND QuoteStatus = '" + state + "' ";

    String t = "";
    if(! state2.equals("ALL"))
      t = " AND SalesPerson = '" + state2 + "' ";

    rs = stmt.executeQuery("SELECT QuoteCode, QuoteDate, CompanyCode, TotalTotal, Currency, SalesPerson, CompanyName, QuoteStatus FROM quote WHERE Status != 'C' " + s + t + " AND QuoteDate >= {d '" + dateFrom + "'} AND QuoteDate <= {d '"
                         + dateTo + "'} ORDER BY QuoteDate");
      
    String quoteCode, date, customerCode, currency, salesPerson, customerName, quoteStatus;
    double totalTotal;
    
    while(rs.next())
    {    
      quoteCode    = rs.getString(1);
      date         = rs.getString(2);
      customerCode = rs.getString(3);
      totalTotal   = generalUtils.doubleFromStr(rs.getString(4));
      currency     = rs.getString(5);
      salesPerson  = rs.getString(6);
      customerName = rs.getString(7);
      quoteStatus  = rs.getString(8);
      
      processLine2(out, quoteCode, date, customerCode, customerName, totalTotal, currency, salesPerson, quoteStatus, cssFormat, oCount, bytesOut);
    }
  
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void processLine2(PrintWriter out, String quoteCode, String quoteDate, String customerCode, String customerName, double amount, String currency, String salesPerson, String quoteStatus, String[] cssFormat, int[] oCount, int[] bytesOut)
                            throws Exception
  {
    byte[] b = new byte[30];

    generalUtils.doubleToBytesCharFormat(amount, b, 0);
    generalUtils.bytesDPsGivenSeparator(true, '\000', '2', b, 20, 0);
    generalUtils.formatNumeric(b, '2');

    if(cssFormat[0].equals("line1"))
      cssFormat[0] = "line2";
    else cssFormat[0] = "line1";
 
    scoutln(out, bytesOut, "<tr  id=\"" + cssFormat[0] + "\">");

    scoutln(out, bytesOut, "<td><p>" + oCount[0]++ + "</td>");
    scout(out, bytesOut, "<td><p><a href=\"javascript:viewCust('" + customerCode + "')\">" + customerCode + "</a></td>");
    scout(out, bytesOut, "<td><p>" + customerName + "</td>");
    scoutln(out, bytesOut, "<td><p><a href=\"javascript:viewQuote('" + quoteCode + "')\">" + quoteCode + "</a></td>");
    scout(out, bytesOut, "<td><p>" + generalUtils.convertFromYYYYMMDD(quoteDate) + "</td>");
    scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.stringFromBytes(b, 0L) + "</td>");
    scoutln(out, bytesOut, "<td><p>" + currency + "</td>");
    scoutln(out, bytesOut, "<td><p>" + salesPerson + "</td>");
    scoutln(out, bytesOut, "<td><p>" + quoteStatus + "</td>");
    scoutln(out, bytesOut, "<td><p><a href=\"javascript:viewLines('" + quoteCode + "')\">View Lines</a></td></tr>");
  }

}
