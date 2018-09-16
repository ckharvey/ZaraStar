// =======================================================================================================================================================================================================
// System: ZaraStar Document: Invoice Enquiry for GST
// Module: SalesInvoiceEnquiryGenerate.java
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
import java.io.*;

public class SalesInvoiceEnquiryGenerate extends HttpServlet
{
  GeneralUtils  generalUtils = new GeneralUtils();
  MessagePage  messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils  directoryUtils = new DirectoryUtils();
  DashboardUtils  dashboardUtils = new DashboardUtils();
  AccountsUtils  accountsUtils = new AccountsUtils();
  Customer customer = new Customer();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="", p3="", p4="";
    try
    {
      req.setCharacterEncoding("UTF-8");
      directoryUtils.setContentHeaders(res);
      out = res.getWriter();

      unm = req.getParameter("unm");
      sid = req.getParameter("sid");
      uty = req.getParameter("uty");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");
      p1  = req.getParameter("p1"); // companyCode
      p2  = req.getParameter("p2"); // plain or not
      p3  = req.getParameter("p3"); // datefrom
      p4  = req.getParameter("p4"); // dateto

      if(p1 == null) p1 = "";
      if(p2 == null) p2 = "";
      if(p3 == null) p3 = "";
      if(p4 == null) p4 = "";

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, p3, p4, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "SalesInvoiceEnquiryGenerate", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1027, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                    String p1, String p2, String p3, String p4, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');

    Connection con = null;
    Statement stmt = null;
    ResultSet rs   = null;

    String uName = directoryUtils.getMySQLUserName();
    String pWord = directoryUtils.getMySQLPassWord();
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + uName + "&password=" + pWord);

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 1027, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "SalesInvoiceEnquiryGenerate", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1027, bytesOut[0], 0, "ACC:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "SalesInvoiceEnquiryGenerate", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1027, bytesOut[0], 0, "SID:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    String dateFrom, dateTo;
  
    if(p3.length() == 0)
      dateFrom = generalUtils.todaySQLFormat(localDefnsDir, defnsDir);
    else dateFrom = generalUtils.convertDateToSQLFormat(p3);
    
    if(p4.length() == 0)
      dateTo = generalUtils.todaySQLFormat(localDefnsDir, defnsDir);
    else dateTo = generalUtils.convertDateToSQLFormat(p4);
    
    boolean plain = false;
    if(p2.equals("P"))
      plain = true;

    String baseCurrency = accountsUtils.getBaseCurrency(con, stmt, rs, dnm, localDefnsDir, defnsDir);

    String customerName = customer.getCompanyNameGivenCode(con, stmt, rs, p1);

    generate(con, stmt, rs, out, req, plain, unm, sid, uty, men, den, dnm, bnm, p1, customerName, dateFrom, dateTo, baseCurrency, p3, p4, imagesDir,
             localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 1027, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void generate(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, boolean plain, String unm, String sid, String uty, String men, String den,
                        String dnm, String bnm, String customerCode, String customerName, String dateFrom, String dateTo, String baseCurrency,
                        String p3, String p4, String imagesDir, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    setHead(con, stmt, rs, out, req, customerCode, p3, p4, plain, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    startBody(true, out, customerCode, customerName, dateFrom, dateTo, baseCurrency, imagesDir, bytesOut);

    stmt = con.createStatement();
  
    String invoiceCode, name, date, totalTotal, currency, baseGSTTotal, gstTotal;
    int x, count = 0;
    double grandBaseGSTTotal = 0.0;
     
    String[] cssFormat = new String[1];  cssFormat[0] = "line1";

    rs = stmt.executeQuery("SELECT InvoiceCode, Date, BaseGSTTotal, TotalTotal, Currency, CompanyName, GSTTotal "
                                   + "FROM invoice WHERE CompanyCode = '" + customerCode + "' AND Status != 'C' AND Date >= {d '" + dateFrom
                                   + "'} AND Date <= {d '" + dateTo + "'} ORDER BY Date, InvoiceCode");

    while(rs.next())                  
    {
      invoiceCode  = rs.getString(1);
      date         = rs.getString(2);
      baseGSTTotal = rs.getString(3);
      totalTotal   = rs.getString(4);
      currency     = rs.getString(5);
      name         = rs.getString(6);
      gstTotal     = rs.getString(7);
      
      appendBodyLine(out, invoiceCode, date, totalTotal, currency, gstTotal, baseGSTTotal, baseCurrency, cssFormat, bytesOut);
      
      grandBaseGSTTotal += generalUtils.doubleDPs(generalUtils.doubleFromStr(baseGSTTotal), '2');

      ++count;
    }
    
    if(stmt != null) stmt.close();
    
    startBody(false, out, customerCode, customerName, dateFrom, dateTo, baseCurrency, imagesDir, bytesOut);
    
//    scoutln(out, bytesOut, "</table><table id=\"page\"><tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=3><p>Number of Invoices: " + count + "</td>");
    scoutln(out, bytesOut, "<td colspan=3 align=right><p>Total GST (" + baseCurrency + "): "
                         + generalUtils.formatNumeric(grandBaseGSTTotal, '2') + "</td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "</table></form>");     scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void setHead(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String p1, String p3, String p4, boolean plain, String unm, String sid, String uty,
                       String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Invoice Enquiry For GST</title>");
    scoutln(out, bytesOut, "<script javascript=\"JavaScript\">");

    scoutln(out, bytesOut, "function fetch(code){var code2=sanitise(code);");
    scoutln(out, bytesOut, "this.location.href=\"http://" + men + "/central/servlet/SalesInvoicePage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men
                         + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p1=\"+code2;}");

    scoutln(out, bytesOut, "function sanitise(code){");
    scoutln(out, bytesOut, "var code2='';var x;var len=code.length;");
    scoutln(out, bytesOut, "for(x=0;x<len;++x)");
    scoutln(out, bytesOut, "if(code.charAt(x)=='#')code2+='%23';");
    scoutln(out, bytesOut, "else if(code.charAt(x)==\"'\")code2+='%27';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='\"')code2+='%22';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='&')code2+='%26';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='%')code2+='%25';");
    scoutln(out, bytesOut, "else if(code.charAt(x)==' ')code2+='%20';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='?')code2+='%3F';");
    scoutln(out, bytesOut, "else code2+=code.charAt(x);");
    scoutln(out, bytesOut, "return code2};");

    if(plain)
      scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"print\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "generalPlain.css\">");
    else scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    outputPageFrame(con, stmt, rs, out, req, plain, p1, p3, p4, "", "Invoice Enquiry for GST", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, bytesOut);

    scoutln(out, bytesOut, "<form>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void startBody(boolean first, PrintWriter out, String customerCode, String customerName, String dateFrom, String dateTo, String baseCurrency, String imagesDir, int[] bytesOut) throws Exception
  {
    if(first)
    {
      scoutln(out, bytesOut, "<table id=\"page\" border=0 cellspacing=2 cellpadding=0>");
      scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
      
      scoutln(out, bytesOut, "<tr><td colspan=6><p>Invoice Enquiry for GST</td></tr>");
      scoutln(out, bytesOut, "<tr><td><img src=\"" + imagesDir + "z402.gif\"></td></tr>");
      scoutln(out, bytesOut, "<tr><td colspan=6><p>" + customerCode + " - " + customerName + " (" + dateFrom + " to " + dateTo + ")</td></tr>");
     
      scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    }  
    else scoutln(out, bytesOut, "<tr><td><img src=\"" + imagesDir + "z402.gif\"></td></tr>");

    scoutln(out, bytesOut, "<tr><td id=\"pageColumn\">Invoice Code &nbsp;</td>");

    scoutln(out, bytesOut, "<td id=\"pageColumn\">Date &nbsp;</td>");
    scoutln(out, bytesOut, "<td id=\"pageColumn\">Invoice Amount &nbsp;</td>");
    scoutln(out, bytesOut, "<td id=\"pageColumn\">Invoice Amount<br>(Exclusive of GST) &nbsp;</td>");
    scoutln(out, bytesOut, "<td id=\"pageColumn\">GST Amount &nbsp;</td>");
    scoutln(out, bytesOut, "<td id=\"pageColumn\">GST Amount (" + baseCurrency + ") &nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td><img src=\"" + imagesDir + "z402.gif\"></td></tr>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void appendBodyLine(PrintWriter out, String invoiceCode, String date, String totalTotal, String currency, String gstTotal, String baseGSTTotal, String baseCurrency, String[] cssFormat, int[] bytesOut) throws Exception
  {
    String s = checkCode(invoiceCode);

    if(! cssFormat[0].equals("line1")) cssFormat[0] = "line1"; else cssFormat[0] = "line2";

    scoutln(out, bytesOut, "<tr id=\"" + cssFormat[0] + "\">");
        
    scoutln(out, bytesOut, "<td nowrap><p><a href=\"javascript:fetch('" + s + "')\">" + invoiceCode + "</a>&nbsp;</td>");

    scoutln(out, bytesOut, "<td nowrap><p>" + generalUtils.convertFromYYYYMMDD(date) + "&nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap align=right><p>" + currency + " " + generalUtils.doubleDPs(totalTotal, '2') + "&nbsp;</td>");

    double totalTotalD = generalUtils.doubleDPs(generalUtils.doubleFromStr(totalTotal), '2');
    double gstTotalD   = generalUtils.doubleDPs(generalUtils.doubleFromStr(gstTotal), '2');
    
    scoutln(out, bytesOut, "<td nowrap align=right><p>" + currency + " " + generalUtils.doubleDPs((totalTotalD - gstTotalD), '2') + "&nbsp;</td>");
    
    scoutln(out, bytesOut, "<td nowrap align=right><p>" + currency + " " + generalUtils.doubleDPs(gstTotal, '2') + "&nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap align=right><p>" + baseCurrency + " " + generalUtils.doubleDPs(baseGSTTotal, '2') + "&nbsp;</td></tr>");
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private String checkCode(String code) throws Exception
  {
    String s="";
    if(code.indexOf('\'') != -1)
    {
      int len = code.length();
      for(int x=0;x<len;++x)
      {
        if(code.charAt(x) == '\'')
          s += "\\'";
        else s += code.charAt(x);
      }
    }
    else s = code;
    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void outputPageFrame(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, boolean plain, String customerCode, String dateFrom, String dateTo, String bodyStr, String title, String unm, String sid,
                               String uty, String men, String den, String dnm, String bnm, String otherSetup, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    int[] hmenuCount = new int[1];

    String subMenuText = dashboardUtils.buildSubMenuText(con, stmt, rs, req, "1027", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount);

    subMenuText += "<table id='title' width=100%><tr><td colspan=2 nowrap><p>" + title + directoryUtils.buildHelp(1027) + "</td></tr></table>";

    subMenuText += buildSubMenuText(plain, customerCode, dateFrom, dateTo, unm, sid, uty, men, den, dnm, bnm, hmenuCount);

    if(plain)
      scoutln(out, bytesOut, authenticationUtils.buildPlainScreen(con, stmt, rs, req, unm, sid, uty, men, den, dnm, bnm, "SalesInvoiceEnquiryInput", subMenuText, hmenuCount[0], bodyStr, otherSetup, localDefnsDir, defnsDir));
    else scoutln(out, bytesOut, authenticationUtils.buildScreen(con, stmt, rs, req, true, unm, sid, uty, men, den, dnm, bnm, "SalesInvoiceEnquiryInput", subMenuText, hmenuCount[0], bodyStr, otherSetup, localDefnsDir, defnsDir));

    scoutln(out, bytesOut, "<table border=0><tr><td valign=top><div id='second' style=\"{margin-top:0px;}\"></div></td>");
    scoutln(out, bytesOut, "<td valign=top><div id='third' style=\"{margin-top:0px;margin-left:0px;}\"></div></td></tr></table>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String buildSubMenuText(boolean plain, String customerCode, String dateFrom, String dateTo, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] hmenuCount) throws Exception
  {
    String s = "<div id='submenu'>";

    ++hmenuCount[0];

    if(! plain)
    {
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\">";
      s += "<a href=\"/central/servlet/SalesInvoiceEnquiryGenerate?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + customerCode + "&p3=" + dateFrom + "&p4=" + dateTo
                           + "&p2=P\">Friendly</a></dt></dl>";
    }
    
    s += "</div>";
    
    --hmenuCount[0];
    
    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}


