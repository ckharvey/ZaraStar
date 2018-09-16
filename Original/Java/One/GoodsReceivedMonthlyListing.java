// =======================================================================================================================================================================================================
// System: ZaraStar Document: GR listing
// Module: GoodsReceivedMonthlyListing.java
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

public class GoodsReceivedMonthlyListing extends HttpServlet
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

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="", p3="";
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
      p2  = req.getParameter("p2");
      p3  = req.getParameter("p3"); // plain or not
      
      if(p1 == null) p1 = "";
      if(p2 == null) p2 = "";
      if(p3 == null) p3 = "";

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, p3, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "GoodsReceivedMonthlyListing", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 4320, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm,
                    String bnm, String p1, String p2, String p3, int[] bytesOut) throws Exception
  {
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');

    long startTime = new java.util.Date().getTime();

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 153, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "GoodsReceivedMonthlyListing", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 4320, bytesOut[0], 0, "ACC:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "GoodsReceivedMonthlyListing", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 4320, bytesOut[0], 0, "SID:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    String dateFrom, dateTo;
  
    if(p1.length() == 0)
    {  
      p1       = generalUtils.today(localDefnsDir, defnsDir);      
      dateFrom = generalUtils.todaySQLFormat(localDefnsDir, defnsDir);      
    }
    else dateFrom = generalUtils.convertDateToSQLFormat(p1);
    
    if(p2.length() == 0)
    {
      p2     = generalUtils.today(localDefnsDir, defnsDir);      
      dateTo = generalUtils.todaySQLFormat(localDefnsDir, defnsDir);
    }
    else dateTo = generalUtils.convertDateToSQLFormat(p2);

    boolean plain = false;
    if(p3.equals("P"))
      plain = true;

    generate(con, stmt, rs, out, req, plain, unm, sid, uty, men, den, dnm, bnm, p1, p2, dateFrom, dateTo, imagesDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 4320, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void generate(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, boolean plain, String unm, String sid,
                        String uty, String men, String den, String dnm, String bnm, String p1, String p2, String dateFrom, String dateTo, String imagesDir,
                        String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    setHead(con, stmt, rs, out, req, plain, p1, p2, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    startBody(true, out, p1, p2, imagesDir, bytesOut);

    String grCode, date, supplierCode, stockProcessed, dateStockProcessed, supplierName, status;
    int count = 0;
    
    String[] cssFormat = new String[1];  cssFormat[0] = "line1";

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT GRCode, Date, CompanyCode, StockProcessed, DateStockProcessed, CompanyName, Status "
                                   + "FROM gr WHERE Date >= {d '" + dateFrom + "'} AND Date <= {d '" + dateTo + "'} ORDER BY GRCode");

    while(rs.next())                  
    {
      grCode             = rs.getString(1);
      date               = rs.getString(2);
      supplierCode       = rs.getString(3);
      stockProcessed     = rs.getString(4);
      dateStockProcessed = rs.getString(5);
      supplierName       = rs.getString(6);
      status             = rs.getString(7);
      
      if(stockProcessed.equals("Y"))
        stockProcessed = "Yes";
      else stockProcessed = "";
      
      appendBodyLine(out, grCode, date, supplierCode, stockProcessed, dateStockProcessed, supplierName, status, imagesDir, cssFormat, bytesOut);

      ++count;
    }
    
    if(stmt != null) stmt.close();
    
    startBody(false, out, p1, p2, imagesDir, bytesOut);
    
    scoutln(out, bytesOut, "</table><table id='page'><tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=2><p>Number of Goods Received Notes: </td><td nowrap align=right><p>" + count + "</td></tr>");

    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void setHead(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, boolean plain, String p1, String p2, String unm,
                       String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut)
                       throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Goods Received Note Listing</title>");
    scoutln(out, bytesOut, "<script javascript=\"JavaScript\">");

    scoutln(out, bytesOut, "function fetch(code){var code2=sanitise(code);");
    scoutln(out, bytesOut, "this.location.href=\"http://" + men + "/central/servlet/GoodsReceivedPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty
                           + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p1=\"+code2;}");

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

    outputPageFrame(con, stmt, rs, out, req, plain, p1, p2, "", "Goods Received Note Listing", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, bytesOut);

    scoutln(out, bytesOut, "<form>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void startBody(boolean first, PrintWriter out, String dateFrom, String dateTo, String imagesDir, int[] bytesOut) throws Exception
  {
    if(first)
    {
      scoutln(out, bytesOut, "<table id=\"page\" border=0 cellspacing=2 cellpadding=0>");
      scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
      
      scoutln(out, bytesOut, "<tr><td colspan=6><p>Goods Received Note Listing for " + dateFrom + " to " + dateTo + "</td></tr>");
      
      scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    }  
    else scoutln(out, bytesOut, "<tr><td><img src=\"" + imagesDir + "z402.gif\"></td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap><p>Status &nbsp;</td>");

    scoutln(out, bytesOut, "<td id=\"pageColumn\">GRN Code &nbsp;</td>");
    scoutln(out, bytesOut, "<td id=\"pageColumn\">Date &nbsp;</td>");
    scoutln(out, bytesOut, "<td id=\"pageColumn\">Supplier Code &nbsp;</td>");
    scoutln(out, bytesOut, "<td id=\"pageColumn\">Set In-Stock &nbsp;</td>");
    scoutln(out, bytesOut, "<td id=\"pageColumn\">Date Set In-Stock &nbsp;</td>");
    scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Supplier Name &nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td><img src=\"" + imagesDir + "z402.gif\"></td></tr>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void appendBodyLine(PrintWriter out, String grCode, String date, String supplierCode, String stockProcessed, String dateStockProcessed,
                              String supplierName, String status, String imagesDir, String[] cssFormat, int[] bytesOut) throws Exception
  {
    String s = checkCode(grCode);

    if(! cssFormat[0].equals("line1"))
      cssFormat[0] = "line1";
    else cssFormat[0] = "line2";

    scoutln(out, bytesOut, "<tr id=\"" + cssFormat[0] + "\">");

    if(status.equals("C"))
      scoutln(out, bytesOut, "<td align=center><img src=\"" + imagesDir + "z0212.gif\" border=0></td>");
    else scoutln(out, bytesOut, "<td></td>");

    scoutln(out, bytesOut, "<td nowrap><p><a href=\"javascript:fetch('" + s + "')\">" + grCode + "</a>&nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>" + generalUtils.convertFromYYYYMMDD(date) + "&nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>" + supplierCode + "&nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>" + stockProcessed + "&nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>" + generalUtils.convertFromYYYYMMDD(dateStockProcessed) + "&nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>" + supplierName + "&nbsp;</td></tr>");
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
  private void outputPageFrame(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, boolean plain, String dateFrom,
                               String dateTo, String bodyStr, String title, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                               String otherSetup, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    int[] hmenuCount = new int[1];

    String subMenuText = dashboardUtils.buildSubMenuText(con, stmt, rs, req, "4320", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount);

    subMenuText += "<table id='title' width=100%><tr><td colspan=2 nowrap><p>" + title + directoryUtils.buildHelp(4320) + "</td></tr></table>";

    subMenuText += buildSubMenuText(plain, dateFrom, dateTo, unm, sid, uty, men, den, dnm, bnm, hmenuCount);

    if(plain)
      scoutln(out, bytesOut, authenticationUtils.buildPlainScreen(con, stmt, rs, req, unm, sid, uty, men, den, dnm, bnm, "GoodsReceivedMonthlyListing", subMenuText, hmenuCount[0], bodyStr, otherSetup, localDefnsDir, defnsDir));
    else
    {
      scoutln(out, bytesOut, authenticationUtils.buildScreen(con, stmt, rs, req, true, unm, sid, uty, men, den, dnm, bnm, "GoodsReceivedMonthlyListing", subMenuText, hmenuCount[0], bodyStr, otherSetup, localDefnsDir, defnsDir));

      scoutln(out, bytesOut, "<table border=0><tr><td valign=top><div id='second' style=\"{margin-top:0px;}\"></div></td>");
      scoutln(out, bytesOut, "<td valign=top><div id='third' style=\"{margin-top:0px;margin-left:0px;}\"></div></td></tr></table>");
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private String buildSubMenuText(boolean plain, String dateFrom, String dateTo, String unm, String sid, String uty, String men, String den, String dnm,
                                  String bnm, int[] hmenuCount) throws Exception
  {
    String s = "<div id='submenu'>";

    ++hmenuCount[0];

    if(! plain)
    {
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\">";
      s += "<a href=\"/central/servlet/GoodsReceivedMonthlyListing?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
        + "&p1=" + dateFrom + "&p2=" + dateTo + "&p3=P\">Friendly</a></dt></dl>";
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

