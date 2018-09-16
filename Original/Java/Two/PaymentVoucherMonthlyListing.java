// =======================================================================================================================================================================================================
// System: ZaraStar Document: Payment Voucher monthly listing
// Module: PaymentVoucherMonthlyListing.java
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
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.io.*;

public class PaymentVoucherMonthlyListing extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  DashboardUtils dashboardUtils = new DashboardUtils();
  AccountsUtils accountsUtils = new AccountsUtils();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="";
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
      p1  = req.getParameter("p1");
      p2  = req.getParameter("p2"); // plain or not
      
      if(p2 == null) p2 = "";

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "PaymentVoucherMonthlyListing", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 4319, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den,
                    String dnm, String bnm, String p1, String p2, int[] bytesOut) throws Exception
  {
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');

    long startTime = new java.util.Date().getTime();

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 4319, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "PaymentVoucherMonthlyListing", imagesDir, localDefnsDir,
                         defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 4319, bytesOut[0], 0, "ACC:" + p1);
    if(con  != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "PaymentVoucherMonthlyListing", imagesDir, localDefnsDir,
                         defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 4319, bytesOut[0], 0, "SID:" + p1);
    if(con  != null) con.close();
      if(out != null) out.flush();
      return;
    }

    String[] dateFrom = new String[1];
    String[] dateTo   = new String[1];
  
    generalUtils.monthYearStrToYYYYMMDDDates(p1, dateFrom, dateTo);
    
    String[][] currencies = new String[1][];  
    
    int numCurrencies = accountsUtils.getCurrencyNames(con, stmt, rs, currencies, dnm, localDefnsDir, defnsDir);

    boolean plain = false;
    if(p2.equals("P"))
      plain = true;

    generate(con, stmt, rs, out, req, p1, plain, unm, sid, uty, men, den, dnm, bnm, dateFrom[0], dateTo[0], numCurrencies, currencies[0],
             imagesDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 4319, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con  != null) con.close();
    if(out != null) out.flush();
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void generate(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String p1, boolean plain, String unm, String sid,
                        String uty, String men, String den, String dnm, String bnm, String dateFrom, String dateTo,
                        int numCurrencies, String[] currencies, String imagesDir, String localDefnsDir, String defnsDir,
                        int[] bytesOut) throws Exception
  {
    setHead(con, stmt, rs, out, req, p1, plain, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    startBody(true, out, p1, imagesDir, bytesOut);

    stmt = con.createStatement();
  
    String voucherCode, name, companyCode, date, totalTotal, currency, baseTotalTotal, status, gstTotal;
    int x, count = 0;
    double[] grandTotals = new double[numCurrencies];
    for(x=0;x<numCurrencies;++x)
      grandTotals[x] = 0.0;
    double baseGrandTotal = 0.0;
    
    String[] cssFormat = new String[1];  cssFormat[0] = "line1";
     
    rs = stmt.executeQuery("SELECT VoucherCode, Date, CompanyCode, BaseTotalTotal, TotalTotal, Currency, CompanyName,"
                                   + " GSTTotal, Status FROM voucher WHERE Date >= {d '" + dateFrom + "'} AND Date <= {d '" + dateTo
                                   + "'} ORDER BY VoucherCode");

    while(rs.next())                  
    {
      voucherCode    = rs.getString(1);
      date           = rs.getString(2);
      companyCode    = rs.getString(3);
      baseTotalTotal = rs.getString(4);
      totalTotal     = rs.getString(5);
      currency       = rs.getString(6);
      name           = rs.getString(7);
      gstTotal       = rs.getString(8);
      status         = rs.getString(9);
      
      appendBodyLine(out, voucherCode, date, companyCode, baseTotalTotal, totalTotal, currency, name, gstTotal, status, imagesDir,
                     cssFormat, bytesOut);
      
      if(! status.equals("C"))
      {
        for(x=0;x<numCurrencies;++x)
        {
          if(currency.equals(currencies[x]))
          {
            grandTotals[x] += generalUtils.doubleDPs(generalUtils.doubleFromStr(totalTotal), '2');
            x = numCurrencies;
          }
        }
      
        baseGrandTotal += generalUtils.doubleDPs(generalUtils.doubleFromStr(baseTotalTotal), '2');
      }

      ++count;
    }
    
    if(stmt != null) stmt.close();
    
    startBody(false, out, p1, imagesDir, bytesOut);
    
    scoutln(out, bytesOut, "</table><table id=\"page\"><tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=2><p>Number of Payment Vouchers:</td><td nowrap align=right><p>" + count + "</td></tr>");

    for(x=0;x<numCurrencies;++x)
    {
      scoutln(out, bytesOut, "<tr><td colspan=2><p>Total " + currencies[x] + ":</td><td nowrap align=right><p>"
                             + generalUtils.formatNumeric(grandTotals[x], '2') + "&nbsp;</td></tr>");
    }    

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=2><p>Base Total:</td><td nowrap align=right><p>"
                             + generalUtils.formatNumeric(baseGrandTotal, '2') + "&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=8><p>Note: Totals do <b>not</b> include cancelled documents:</td></tr>");

    scoutln(out, bytesOut, "</table></form>");     scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void setHead(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String p1, boolean plain, String unm, String sid,
                       String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir,
                       int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Payment Voucher Monthly Listing</title>");
    scoutln(out, bytesOut, "<script javascript=\"JavaScript\">");

    scoutln(out, bytesOut, "function fetch(code){var code2=sanitise(code);");
    scoutln(out, bytesOut, "this.location.href=\"http://" + men + "/central/servlet/PaymentVoucherPage?unm=" + unm + "&sid="
                           + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                           + "&p2=A&p1=\"+code2;}");

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
    {
      scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"print\" href=\""
                             + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "generalPlain.css\">");
    }
    else
    {
      scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\""
                             + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");
    }

    outputPageFrame(con, stmt, rs, out, req, plain, p1, "", "Payment Voucher Monthly Listing", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, bytesOut);

    scoutln(out, bytesOut, "<form>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void startBody(boolean first, PrintWriter out, String p1, String imagesDir, int[] bytesOut) throws Exception
  {
    if(first)
    {
      scoutln(out, bytesOut, "<table id=\"page\" border=0 cellspacing=2 cellpadding=0>");
      scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
      
      int[] month = new int[1];
      int[] year  = new int[1];
      
      generalUtils.monthYearStrToYearAndMonth2(p1, month, year);
      String s = generalUtils.yearAndMonthToMonthYearStr(month[0], year[0]);
      
      scoutln(out, bytesOut, "<tr><td colspan=8><p>Payment Voucher Listing for the month of " + s + "</td></tr>");
      
      scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    }  
    else scoutln(out, bytesOut, "<tr><td><img src=\"" + imagesDir + "z402.gif\"></td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap><p>Status &nbsp;</td>");

    scoutln(out, bytesOut, "<td id=\"pageColumn\">Payment Voucher Code &nbsp;</td>");
    scoutln(out, bytesOut, "<td id=\"pageColumn\">Supplier Code &nbsp;</td>");
    scoutln(out, bytesOut, "<td id=\"pageColumn\">Date &nbsp;</td>");
    scoutln(out, bytesOut, "<td id=\"pageColumn\">Total Amount &nbsp;</td>");
    scoutln(out, bytesOut, "<td id=\"pageColumn\">Currency &nbsp;</td>");
    scoutln(out, bytesOut, "<td id=\"pageColumn\">GST Total &nbsp;</td>");
    scoutln(out, bytesOut, "<td id=\"pageColumn\">Base Total Amount &nbsp;</td>");
    scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Supplier Name &nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td><img src=\"" + imagesDir + "z402.gif\"></td></tr>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void appendBodyLine(PrintWriter out, String voucherCode, String date, String companyCode, String baseTotalTotal, 
                              String totalTotal, String currency, String name, String gstTotal, String status, String imagesDir,
                              String[] cssFormat, int[] bytesOut) throws Exception
  {
    String s = checkCode(voucherCode);

    if(! cssFormat[0].equals("line1"))
      cssFormat[0] = "line1";
    else cssFormat[0] = "line2";

    scoutln(out, bytesOut, "<tr id=\"" + cssFormat[0] + "\">");
        
    if(status.equals("C"))
      scoutln(out, bytesOut, "<td align=center><img src=\"" + imagesDir + "z0212.gif\" border=0></td>");
    else scoutln(out, bytesOut, "<td></td>");

    scoutln(out, bytesOut, "<td nowrap><p><a href=\"javascript:fetch('" + s + "')\">" + voucherCode + "</a>&nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>" + companyCode + "&nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>" + generalUtils.convertFromYYYYMMDD(date) + "&nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap align=right><p>" + generalUtils.doubleDPs(totalTotal, '2') + "&nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>" + currency + "&nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap align=right><p>" + generalUtils.doubleDPs(gstTotal, '2') + "&nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap align=right><p>" + generalUtils.doubleDPs(baseTotalTotal, '2') + "&nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>" + name + "&nbsp;</td></tr>");
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------
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
  
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void outputPageFrame(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, boolean plain, 
                               String month, String bodyStr, String title, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                               String otherSetup, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    int[] hmenuCount = new int[1];

    String subMenuText = dashboardUtils.buildSubMenuText(con, stmt, rs, req, "4319", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount);

    subMenuText += "<table id='title' width=100%><tr><td colspan=2 nowrap><p>" + title + directoryUtils.buildHelp(4319) + "</td></tr></table>";

    subMenuText += buildSubMenuText(plain, month, unm, sid, uty, men, den, dnm, bnm, hmenuCount);

    if(plain)
      scoutln(out, bytesOut, authenticationUtils.buildPlainScreen(con, stmt, rs, req, unm, sid, uty, men, den, dnm, bnm, "PaymentVoucherMonthlyListing", subMenuText, hmenuCount[0], bodyStr, otherSetup, localDefnsDir, defnsDir));
    else
    {
      scoutln(out, bytesOut, authenticationUtils.buildScreen(con, stmt, rs, req, true, unm, sid, uty, men, den, dnm, bnm, "PaymentVoucherMonthlyListing", subMenuText, hmenuCount[0], bodyStr, otherSetup, localDefnsDir, defnsDir));

      scoutln(out, bytesOut, "<table border=0><tr><td valign=top><div id='second' style=\"{margin-top:0px;}\"></div></td>");
      scoutln(out, bytesOut, "<td valign=top><div id='third' style=\"{margin-top:0px;margin-left:0px;}\"></div></td></tr></table>");
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private String buildSubMenuText(boolean plain, String month, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] hmenuCount) throws Exception
  {
    String s = "<div id='submenu'>";

    ++hmenuCount[0];

    if(! plain)
    {
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\">";
      s += "<a href=\"/central/servlet/PaymentVoucherMonthlyListing?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + month + "&p2=P\">Friendly</a></dt></dl>";
    }
    
    s += "</div>";
    
    --hmenuCount[0];
    
    return s;
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
