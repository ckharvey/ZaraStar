// =======================================================================================================================================================================================================
// System: ZaraStar Analytic: Stock history enquiry params entry
// Module: StockHistoryEnquiryInput.java
// Author: C.K.Harvey
// Copyright (c) 2002-07 Christopher Harvey. All Rights Reserved.
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
import java.io.*;
import java.sql.*;

public class StockHistoryEnquiryInput extends HttpServlet
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
      p1  = req.getParameter("p1");

      if(p1 == null) p1 = "";
      
      doIt(out, req, p1, unm, sid, uty, men, den, dnm, bnm, bytesOut);
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
 
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "MainPageUtils2", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1002, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String p1, String unm, String sid, String uty, String men,
                    String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;
    
    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 1002, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "MainPageUtils2", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1002, bytesOut[0], 0, "ACC:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "MainPageUtils2", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1002, bytesOut[0], 0, "SID:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    String code;
    if(p1.equalsIgnoreCase("null") || p1.length() == 0)
      code = "";
    else code = p1;

    set(con, stmt, rs, out, req, code, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 1002, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String code, String unm, String sid, String uty, String men,
                   String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    String[] bodyStr  = new String[1]; bodyStr[0]  = "";
    String[] saveStr  = new String[1]; saveStr[0]  = "";
    int[] count = new int[1]; count[0] = 0;

    buildStrs("P", "Picking Lists",               bodyStr, saveStr, count, false);
    buildStrs("G", "Goods Received Notes",        bodyStr, saveStr, count, false);
    buildStrs("A", "Stock Adjustments",           bodyStr, saveStr, count, false);
    buildStrs("C", "Stock Check",                 bodyStr, saveStr, count, false);
    buildStrs("S", "Sales Orders",                bodyStr, saveStr, count, false);
    buildStrs("Y", "Purchase Orders",             bodyStr, saveStr, count, false);
    buildStrs("L", "Local Purchase Requisitions", bodyStr, saveStr, count, false);
    buildStrs("D", "Delivery Orders",             bodyStr, saveStr, count, false);
    buildStrs("I", "Invoices",                    bodyStr, saveStr, count, false);
    buildStrs("Q", "Quotations",                  bodyStr, saveStr, count, false);

    scoutln(out, bytesOut, "<html><head><title>Stock History Enquiry</title>");
    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    scoutln(out, bytesOut, "function search(){var p2='';" + saveStr[0] + "p2+='%01';");
    scoutln(out, bytesOut, "var itemCode=sanitise(document.forms[0].itemCode.value);");
    scoutln(out, bytesOut, "var dateFrom=document.forms[0].dateFrom.value;");
    scoutln(out, bytesOut, "var dateTo=document.forms[0].dateTo.value;");
    scoutln(out, bytesOut, "var mfr=document.forms[0].mfr.value;");
    scoutln(out, bytesOut, "var mfrCode=document.forms[0].mfrCode.value;");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/MainPageUtils2a?unm=" + unm + "&uty=" + uty + "&sid=" + sid + "&men=" + men + "&den=" + den
                           + "&dnm=" + dnm + "&p1=\"+itemCode+\"&p2=\"+p2+\"&p3=\"+dateFrom+\"&p4=\"+dateTo+\"&p5=\"+mfr+\"&p6=\"+mfrCode+\"&bnm="
                           + bnm + "\";}");

    scoutln(out, bytesOut, "function sanitise(code){");
    scoutln(out, bytesOut, "var code2='';var x;var len=code.length;");
    scoutln(out, bytesOut, "for(x=0;x<len;++x)");
    scoutln(out, bytesOut, "if(code.charAt(x)=='#')code2+='%23';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='\"')code2+='%22';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='&')code2+='%26';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='%')code2+='%25';");
    scoutln(out, bytesOut, "else if(code.charAt(x)==' ')code2+='%20';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='?')code2+='%3F';");
    scoutln(out, bytesOut, "else code2+=code.charAt(x);");
    scoutln(out, bytesOut, "return code2};");

    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\""
                           + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\"></head>");
  
    int[] hmenuCount = new int[1];

    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "1002", "", "MainPageUtils2", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir,
                          hmenuCount, bytesOut);
                                
    scoutln(out, bytesOut, "<form>");
  
    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Stock History Enquiry", "1002",  unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<table id=\"page\" width=100% border=0>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=5><p>Searches a range of documents for all records that refer to");
    scoutln(out, bytesOut, " a specfied stock item code. Note that this service performs a search of <i>all</i> historical documents.</td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap><p>Item Code</td>");
    scoutln(out, bytesOut, "<td><input type=text maxlength=20 size=20 name=itemCode value=\"" + code + "\"></td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=3><p><b>Or search by Manufacturer and Manufacturer Code:</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap><p>Manufacturer &nbsp;</td><td>");
    getMfrsDDL(con, stmt, rs, out, dnm, localDefnsDir, defnsDir, bytesOut);
    scoutln(out, bytesOut, "</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td width=1% nowrap><pManufacturer Code&nbsp;&nbsp;</td><td><p><input type=text maxlength=30 size=20 name=mfrCode>");
    scoutln(out, bytesOut, "</td></tr>");
    
    scoutln(out, bytesOut, "<tr><td colspan=6><hr></td></tr>");
    
    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
    scoutln(out, bytesOut, "and document issued from date &nbsp;&nbsp;</td><td><input type=text maxlength=10 size=10 name=dateFrom></td>");

    scoutln(out, bytesOut, "<td nowrap><p>and to date &nbsp;&nbsp;</td><td><input type=text maxlength=10 size=10 name=dateTo>");
    scoutln(out, bytesOut, "&nbsp; <span id=\"serviceNote\">(dates optional)</span></td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr id=\"pageColumn\"><td></td><td colspan=4><p>Select the documents to Search</td></tr>");

    scoutln(out, bytesOut, bodyStr[0]);

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td colspan=2><p><a href=\"javascript:search()\">Search</a> for matching documents</td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir));
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void getMfrsDDL(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String dnm, String localDefnsDir, String defnsDir,
                          int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT DISTINCT Manufacturer FROM stock ORDER BY Manufacturer");

    scoutln(out, bytesOut, "<select name=\"mfr\">");
    
    String mfr;
    
    while(rs.next())
    {
      mfr = rs.getString(1);
      if(mfr.length() > 0)
        scoutln(out, bytesOut, "<option value=\"" + mfr + "\">" + mfr);
    }

    scoutln(out, bytesOut, "</select>");
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void buildStrs(String abbrev, String desc, String[] bodyStr, String[] saveStr, int[] count, boolean checked) throws Exception
  {
    bodyStr[0] += "<tr><td><input type=checkbox name=action ";
    if(checked)
      bodyStr[0] += "checked ";

    bodyStr[0] += "></td>";

    bodyStr[0] += "<td colspan=4 nowrap><p>" + desc + "</td></tr>";

    saveStr[0] += "p2+=\""+abbrev+"%20\";if(document.forms[0].action["+count[0]+"].checked)p2+=\"G\";else p2+=\"R\";p2+=\"%20\";";
    ++count[0];
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}

