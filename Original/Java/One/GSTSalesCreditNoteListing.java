// =======================================================================================================================================================================================================
// System: ZaraStar Document: GST: CNs listing
// Module: GSTSalesCreditNoteListing.java
// Author: C.K.Harvey
// Copyright (c) 2001-07 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.io.*;

public class GSTSalesCreditNoteListing extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  DashboardUtils dashboardUtils = new DashboardUtils();

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
      directoryUtils.setContentHeaders2(res);
      out = res.getWriter();

      unm = req.getParameter("unm");
      sid = req.getParameter("sid");
      uty = req.getParameter("uty");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");
      p1  = req.getParameter("p1"); // dateFrom
      p2  = req.getParameter("p2"); // dateTo
      p3  = req.getParameter("p3"); // gstRate
      p4  = req.getParameter("p4"); // plain or not

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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "GSTSalesCreditNoteListing", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6003, bytesOut[0], 0, "ERR:" + p1 + " " + p2 + " " + p3);
      if(out != null) out.flush();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den,
                    String dnm, String bnm, String p1, String p2, String p3, String p4, int[] bytesOut) throws Exception
  {
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');

    long startTime = new java.util.Date().getTime();

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 6003, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "GSTSalesCreditNoteListing", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6003, bytesOut[0], 0, "ACC:" + p1 + " " + p2 + " " + p3);
      if(con  != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "GSTSalesCreditNoteListing", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6003, bytesOut[0], 0,  "SID:" + p1 + " " + p2 + " " + p3);
      if(con  != null) con.close();
      if(out != null) out.flush();
      return;
    }

    boolean plain = false;
    if(p4.equals("P"))
      plain = true;

    generate(con, stmt, rs, out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, p3, plain, imagesDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 6003, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1 + " " + p2 + " " + p3);
    if(con  != null) con.close();
    if(out != null) out.flush();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void generate(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty,
                        String men, String den, String dnm, String bnm, String dateFrom, String dateTo, String gstRate, boolean plain, String imagesDir,
                        String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    setHead(con, stmt, rs, out, req, plain, dateFrom, dateTo, gstRate, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    startBody(true, out, imagesDir, bytesOut);

    stmt = con.createStatement();
  
    String cnCode, date, companyCode, baseGSTTotal, baseTotalTotal, totalTotal, gstTotal, currency;
     
    rs = stmt.executeQuery("SELECT DISTINCT t1.CNCode, t1.Date, t1.CompanyCode, t1.BaseGSTTotal, t1.BaseTotalTotal, "
                                   + " t1.TotalTotal, t1.GSTTotal, t1.Currency FROM creditl AS t2 INNER JOIN credit AS t1 "
                                   + "ON t2.CNCode = t1.CNCode WHERE t1.Date >= {d '" + dateFrom
                                   + "'} AND t1.Date <= {d '" + dateTo + "'} AND t1.Status != 'C' AND t2.GSTRate = '" + gstRate
                                   + "'");

    String cssFormat = "";
    
    while(rs.next())                  
    {
      cnCode    = rs.getString(1);
      date           = rs.getString(2);
      companyCode    = rs.getString(3);
      baseGSTTotal   = rs.getString(4);
      baseTotalTotal = rs.getString(5);
      totalTotal     = rs.getString(6);
      gstTotal       = rs.getString(7);
      currency       = rs.getString(8);
    
      if(cssFormat.equals("line1")) cssFormat = "line2"; else cssFormat = "line1"; 

      appendBodyLine(out, cnCode, date, companyCode, baseGSTTotal, baseTotalTotal, totalTotal, gstTotal, currency, cssFormat, bytesOut);
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    startBody(false, out, imagesDir, bytesOut);
    
    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir));
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void setHead(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, boolean plain, String dateFrom, String dateTo, String gstRate, String unm, String sid, String uty,
                       String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>GST Credit Note Listing</title>");
    scoutln(out, bytesOut, "<script javascript=\"JavaScript\">");

    scoutln(out, bytesOut, "function fetch(code){");
    scoutln(out, bytesOut, "var code2=sanitise(code);");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/SalesCreditNotePage?unm=" + unm + "&sid="
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
      scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"print\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "generalPlain.css\">");
    else scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    outputPageFrame(con, stmt, rs, out, req, plain, dateFrom, dateTo, gstRate, "", "GST Reconciliation: Credit Note Listing", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, bytesOut);

    scoutln(out, bytesOut, "<form><br>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void startBody(boolean first, PrintWriter out, String imagesDir, int[] bytesOut) throws Exception
  {
    if(first)
      scoutln(out, bytesOut, "<table id=\"page\" border=0 cellspacing=2 cellpadding=0>");
    else scoutln(out, bytesOut, "<tr><td><img src=\"" + imagesDir + "z402.gif\"></td></tr>");

    scoutln(out, bytesOut, "<tr id=\"pageColumn\"><td nowrap><p>Credit Note Code &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Date &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Customer Code &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Currency &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Base GST Total &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Base Total &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Issue Total &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Issue GST Total &nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td><img src=\"" + imagesDir + "z402.gif\"></td></tr>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void appendBodyLine(PrintWriter out, String cnCode, String date, String companyCode, String baseGSTTotal, String baseTotalTotal,
                              String totalTotal, String gstTotal, String currency, String cssFormat, int[] bytesOut) throws Exception
  {
    String s = checkCode(cnCode);

    scoutln(out, bytesOut, "<tr  id=\"" + cssFormat + "\">");

    scoutln(out, bytesOut, "<td nowrap><p><a href=\"javascript:fetch('" + s + "')\">" + cnCode + "</a>&nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>" + generalUtils.convertFromYYYYMMDD(date) + "&nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>" + companyCode + "&nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>" + currency + "&nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap align=right><p>" + generalUtils.doubleDPs(baseGSTTotal, '2') + "&nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap align=right><p>" + generalUtils.doubleDPs(baseTotalTotal, '2') + "&nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap align=right><p>" + generalUtils.doubleDPs(totalTotal, '2') + "&nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap align=right><p>" + generalUtils.doubleDPs(gstTotal, '2') + "&nbsp;</td></tr>");
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
  private void outputPageFrame(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, boolean plain, String dateFrom, String dateTo, String gstRate, String bodyStr, String title, String unm, String sid, String uty,
                               String men, String den, String dnm, String bnm, String otherSetup, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    int[] hmenuCount = new int[1];

    String subMenuText = dashboardUtils.buildSubMenuText(con, stmt, rs, req, "6003", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount);

    subMenuText += "<table id='title' width=100%><tr><td colspan=2 nowrap><p>" + title + directoryUtils.buildHelp(6003) + "</td></tr></table>";

    subMenuText += buildSubMenuText(plain, dateFrom, dateTo, gstRate, unm, sid, uty, men, den, dnm, bnm, hmenuCount);

    if(plain)
      scoutln(out, bytesOut, authenticationUtils.buildPlainScreen(con, stmt, rs, req, unm, sid, uty, men, den, dnm, bnm, "GSTReconciliation", subMenuText, hmenuCount[0], bodyStr, otherSetup, localDefnsDir, defnsDir));
    else
    {
      scoutln(out, bytesOut, authenticationUtils.buildScreen(con, stmt, rs, req, true, unm, sid, uty, men, den, dnm, bnm, "GSTReconciliation", subMenuText, hmenuCount[0], bodyStr, otherSetup, localDefnsDir, defnsDir));

      scoutln(out, bytesOut, "<table border=0><tr><td valign=top><div id='second' style=\"{margin-top:0px;}\"></div></td>");
      scoutln(out, bytesOut, "<td valign=top><div id='third' style=\"{margin-top:0px;margin-left:0px;}\"></div></td></tr></table>");
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String buildSubMenuText(boolean plain, String dateFrom, String dateTo, String gstRate, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] hmenuCount) throws Exception
  {
    String s = "<div id='submenu'>";

    ++hmenuCount[0];

    if(! plain)
    {
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\">";
      s += "<a href=\"/central/servlet/GSTSalesCreditNoteListing?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + dateFrom + "&p2=" + dateTo + "&p3=" + generalUtils.sanitise(gstRate) + "&p4=P\">Friendly</a></dt></dl>";
    }

    s += "</div>";

    --hmenuCount[0];

    return s;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
