// =======================================================================================================================================================================================================
// System: ZaraStar Accounts: Sales Invoice Receivables Verification
// Module: SalesInvoiceReceivablesVerificationExecute.java
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
import java.io.*;

public class SalesInvoiceReceivablesVerificationExecute extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();

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
      directoryUtils.setContentHeaders2(res);
      out = res.getWriter();

      unm = req.getParameter("unm");
      sid = req.getParameter("sid");
      uty = req.getParameter("uty");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");
      p1  = req.getParameter("p1"); // dateTo
      
      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, bytesOut);
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
 
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "SalesInvoiceReceivablesVerification", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3072, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush(); 
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String dateTo, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    
    Connection con = null;
    Statement stmt = null;
    ResultSet rs   = null;

    String uName = directoryUtils.getMySQLUserName();
    String pWord = directoryUtils.getMySQLPassWord();
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + uName + "&password=" + pWord);

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 3072, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "SalesInvoiceReceivablesVerification", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3072, bytesOut[0], 0, "ACC:" + dateTo);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "SalesInvoiceReceivablesVerification", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3072, bytesOut[0], 0, "SID:" + dateTo);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(dateTo.length() == 0)
      dateTo = "2099-12-31";
    else dateTo = generalUtils.convertDateToSQLFormat(dateTo);
    
    set(con, stmt, rs, out, req, dateTo, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 3072, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), dateTo);
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String dateTo, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir,
                   int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Sales Invoice Receivables Verification</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4101, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewCN(code){");
      scoutln(out, bytesOut, "var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/SalesCreditNotePage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4205, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewReceipt(code){");
      scoutln(out, bytesOut, "var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/ReceiptPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }
    
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4111, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewDN(code){");
      scoutln(out, bytesOut, "var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/SalesDebitNotePage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4067, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewInv(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/SalesInvoicePage?unm=" + unm + "&sid=" + sid + "&uty="+ uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
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
  
    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    int[] hmenuCount = new int[1];
 
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "3072", "", "SalesInvoiceReceivablesVerification", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Sales Invoice Receivables Verification", "3072", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<form><table id='page' width=100% border=0>");

    scoutln(out, bytesOut, "<table id='page' border=0 cellspacing=2 cellpadding=3><tr id='pageColumn'><td></td>");
    scoutln(out, bytesOut, "<td><p>Invoice/Debit Note Code</td>");
    scoutln(out, bytesOut, "<td></td>");
    scoutln(out, bytesOut, "<td><p>Receipt/Credit Note Code</td>");
    scoutln(out, bytesOut, "<td></td></tr>");
           
    checkInvoices(con, stmt, rs, out, dateTo, bytesOut);
    checkDebitNotes(con, stmt, rs, out, dateTo, bytesOut);

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void checkInvoices(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String dateTo, int[] bytesOut) throws Exception
  {
    String[] cssFormat = new String[1];  cssFormat[0] = "";
        
    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT InvoiceCode, Currency, Date FROM invoice WHERE Status != 'C' AND Date <= {d '" + dateTo + "'} ORDER BY Date");//InvoiceCode");
      
      String invoiceCode, currency, date;
      
      while(rs.next())
      {
        invoiceCode = rs.getString(1);
        currency    = rs.getString(2);
        date        = rs.getString(3);
         
        testReceipts(con, stmt, rs, out, invoiceCode, currency, date, cssFormat, bytesOut);
        testSalesCreditNotes(con, stmt, rs, out, invoiceCode, currency, date, cssFormat, bytesOut);
      }  
      
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();

    }
    catch(Exception e)
    {
    System.out.println(e);
      if(rs   != null) rs.close();     
      if(stmt != null) stmt.close();    
    }
  }
    
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void checkDebitNotes(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String dateTo, int[] bytesOut) throws Exception
  {
    String[] cssFormat = new String[1];  cssFormat[0] = "";
        
    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT DNCode, Currency, Date FROM debit WHERE Status != 'C' AND Date <= {d '" + dateTo + "'} ORDER BY Date");//DNCode");
      
      String dnCode, currency, date;
      
      while(rs.next())
      {
        dnCode   = rs.getString(1);
        currency = rs.getString(2);
        date     = rs.getString(3);
         
        testReceipts(con, stmt, rs, out, dnCode, currency, date, cssFormat, bytesOut);
        testSalesCreditNotes(con, stmt, rs, out, dnCode, currency, date, cssFormat, bytesOut);
      }  
      
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();

    }
    catch(Exception e)
    {
    System.out.println(e);
      if(rs   != null) rs.close();     
      if(stmt != null) stmt.close();    
    }
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void testReceipts(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String invoiceCode, String invoiceCurrency, String invoiceDate, String[] cssFormat, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t1.ReceiptCode, t1.Currency, t1.DateReceived FROM receiptl AS t2 INNER JOIN receipt AS t1 ON t1.ReceiptCode = t2.ReceiptCode " 
                         + "WHERE t2.InvoiceCode = '" + invoiceCode + "' AND t1.Status != 'C'");

    String receiptCode, currency, date;

    while(rs.next())
    {    
      receiptCode = rs.getString(1);
      currency    = rs.getString(2);
      date        = rs.getString(3);
      
      if(! currency.equals(invoiceCurrency))
      {
        if(cssFormat[0].equals("line1")) cssFormat[0] = "line2"; else cssFormat[0] = "line1";
        
        scoutln(out, bytesOut, "<tr id='" + cssFormat[0] + "'><td><p>Wrong Currency</td><td><p><a href=\"javascript:viewInv('" + invoiceCode + "')\">" + invoiceCode + "</a></td>");
        scoutln(out, bytesOut, "<td><p>" + invoiceCurrency + "</td>");
        scoutln(out, bytesOut, "<td><p><a href=\"javascript:viewReceipt('" + receiptCode + "')\">" + receiptCode + "</a></td>");
        scoutln(out, bytesOut, "<td><p>" + currency + "</td></tr>");
      }
      else
      if(generalUtils.encodeFromYYYYMMDD(date) < generalUtils.encodeFromYYYYMMDD(invoiceDate))
      {
        if(cssFormat[0].equals("line1")) cssFormat[0] = "line2"; else cssFormat[0] = "line1";
        
        scoutln(out, bytesOut, "<tr id='" + cssFormat[0] + "'><td><p>Date Inconsistency</td><td><p><a href=\"javascript:viewInv('" + invoiceCode + "')\">" + invoiceCode + "</a></td>");
        scoutln(out, bytesOut, "<td><p>" + generalUtils.convertFromYYYYMMDD(invoiceDate) + "</td>");
        scoutln(out, bytesOut, "<td><p><a href=\"javascript:viewReceipt('" + receiptCode + "')\">" + receiptCode + "</a></td>");
        scoutln(out, bytesOut, "<td><p>" + generalUtils.convertFromYYYYMMDD(date) + "</td></tr>");
      }
    }

    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void testSalesCreditNotes(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String invoiceCode, String invoiceCurrency, String invoiceDate, String[] cssFormat, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t1.CNCode, t1.Currency, t1.Date FROM creditl AS t2 INNER JOIN credit AS t1 ON t1.CNCode = t2.CNCode " 
                         + "WHERE t2.InvoiceCode = '" + invoiceCode + "' AND t1.Status != 'C'");

    String cnCode, currency, date;

    while(rs.next())
    {    
      cnCode   = rs.getString(1);
      currency = rs.getString(2);
      date     = rs.getString(3);

      if(! currency.equals(invoiceCurrency))
      {
        if(cssFormat[0].equals("line1")) cssFormat[0] = "line2"; else cssFormat[0] = "line1";
        
        scoutln(out, bytesOut, "<tr id='" + cssFormat[0] + "'><td><p>Wrong Currency</td><td><p><a href=\"javascript:viewInv('" + invoiceCode + "')\">" + invoiceCode + "</a></td>");
        scoutln(out, bytesOut, "<td><p>" + invoiceCurrency + "</td>");
        scoutln(out, bytesOut, "<td><p><a href=\"javascript:viewCN('" + cnCode + "')\">" + cnCode + "</a></td>");
        scoutln(out, bytesOut, "<td><p>" + currency + "</td></tr>");
      }
      else
      if(generalUtils.encodeFromYYYYMMDD(date) < generalUtils.encodeFromYYYYMMDD(invoiceDate))
      {
        if(cssFormat[0].equals("line1")) cssFormat[0] = "line2"; else cssFormat[0] = "line1";
        
        scoutln(out, bytesOut, "<tr id='" + cssFormat[0] + "'><td><p>Date Inconsistency</td><td><p><a href=\"javascript:viewInv('" + invoiceCode + "')\">" + invoiceCode + "</a></td>");
        scoutln(out, bytesOut, "<td><p>" + generalUtils.convertFromYYYYMMDD(invoiceDate) + "</td>");
        scoutln(out, bytesOut, "<td><p><a href=\"javascript:viewCN('" + cnCode + "')\">" + cnCode + "</a></td>");
        scoutln(out, bytesOut, "<td><p>" + generalUtils.convertFromYYYYMMDD(date) + "</td></tr>");
      }
    }

    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int[] bytesOut, String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);
  }
  
}
