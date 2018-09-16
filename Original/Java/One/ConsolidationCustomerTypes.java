// =============================================================================================================================================================
// System: ZaraStar Consolidation: List customer types
// Module: ConsolidationCustomerTypes.java
// Author: C.K.Harvey
// Copyright (c) 2006-14 Christopher Harvey. All Rights Reserved.
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

public class ConsolidationCustomerTypes extends HttpServlet
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

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="";

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
      
      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "ConsolidationCustomerTypes", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6902, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm,
                    String bnm, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null, stmt2 = null;
    ResultSet rs   = null, rs2   = null;
    
    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "ConsolidationCustomerTypes", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6902, bytesOut[0], 0, "SID:");
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    process(con, stmt, stmt2, rs, rs2, out, req, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 6902, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void process(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir,
                       String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Detail</title>");

    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    scoutln(out, bytesOut, "<script javascript=\"JavaScript\">");

    scoutln(out, bytesOut, "function viewCompany(code){var p1=sanitise(code);");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/CustomerPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p1=\"+p1;}");

    scoutln(out, bytesOut, "function sanitise(code){");
    scoutln(out, bytesOut, "var code2='';var x;var len=code.length;");
    scoutln(out, bytesOut, "for(x=0;x<len;++x)");
    scoutln(out, bytesOut, "if(code.charAt(x)=='#')code2+='%23';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='\"')code2+='%22';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='&')code2+='%26';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='%')code2+='%25';");
    scoutln(out, bytesOut, "else if(code.charAt(x)==' ')code2+='%20';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='+')code2+='%2b';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='?')code2+='%3F';");
    scoutln(out, bytesOut, "else code2+=code.charAt(x);");
    scoutln(out, bytesOut, "return code2};");
    
    scoutln(out, bytesOut, "</script>");

    int[] hmenuCount = new int[1];
    
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "182", "", "DataAnalytics", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
     
    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Customer Types", "6902", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<form>");

    scoutln(out, bytesOut, "<table id='page' cellspacing=0 cellpadding=2 width=100%>");

    scoutln(out, bytesOut, "<tr><td><p>Customer Code</td><td><p>Customer</td><td><p>Industry Type</td><td><p>Company Type</td><td><p>SalesPerson</td></tr>");

    customers(con, stmt, rs, out, bytesOut);
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    
    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir));
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void customers(Connection con, Statement stmt, ResultSet rs, PrintWriter out, int[] bytesOut) throws Exception
  {
    String companyCode = "", name, cssFormat = "line1";

    stmt = con.createStatement();
 
    rs = stmt.executeQuery("SELECT CompanyCode, Name, IndustryType, CompanyType, SalesPerson, Address1, Address2, Address3, Address4, Address5, Phone1, Phone2, Currency, CreditLimit, CreditDays FROM company ORDER BY Name");
    
    String industryType, companyType, salesPerson, addr1, addr2, addr3, addr4, addr5, phone1, phone2, currency, creditLimit, creditDays;
    
    while(rs.next())
    {  
      companyCode  = rs.getString(1);
      name         = rs.getString(2);
      industryType = rs.getString(3);
      companyType  = rs.getString(4);
      salesPerson  = rs.getString(5);
      addr1        = rs.getString(6);
      addr2        = rs.getString(7);
      addr3        = rs.getString(8);
      addr4        = rs.getString(9);
      addr5        = rs.getString(10);
      phone1       = rs.getString(11);
      phone2       = rs.getString(12);
      currency     = rs.getString(13);
      creditLimit  = rs.getString(14);
      creditDays   = rs.getString(15);
      
      if(cssFormat.equals("line1")) cssFormat = "line2"; else cssFormat = "line1";
      
      scoutln(out, bytesOut, "<tr id='" + cssFormat + "'><td><p><a href=\"javascript:viewCompany('" + companyCode + "')\">" + companyCode + "</td><td><p>" + name + "<br>" + addr1);
      if(addr2.length() > 0) scoutln(out, bytesOut, "<br>" + addr2);
      if(addr3.length() > 0) scoutln(out, bytesOut, "<br>" + addr3);
      if(addr4.length() > 0) scoutln(out, bytesOut, "<br>" + addr4);
      if(addr5.length() > 0) scoutln(out, bytesOut, "<br>" + addr5);

      scoutln(out, bytesOut, "<br>Phone 1: " + phone1);
      scoutln(out, bytesOut, "<br>Phone 2: " + phone2);
      scoutln(out, bytesOut, "<br>Credit Limit: " + currency + " " + generalUtils.formatNumeric(creditLimit, '2') + " (" + creditDays + " days)");
      
      scoutln(out, bytesOut, "</td><td><p>" + industryType + "</td><td><p>" + companyType + "</td><td><p>" + salesPerson + "</td></tr>");
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();    
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }
  
}
