// =======================================================================================================================================================================================================
// System: ZaraStar: TNT: Purchase Invoices
// Module: TrackTracePurchaseInvoices.java
// Author: C.K.Harvey
// Copyright (c) 2003-08 Christopher Harvey. All Rights Reserved.
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

public class TrackTracePurchaseInvoices extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  DocumentUtils documentUtils = new DocumentUtils();
  
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
      p1  = req.getParameter("p1"); // suppCode
      
      if(p1 == null) p1 = "";
      
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
 
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "TrackTracePurchaseInvoices", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2040, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush(); 
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1,
                    int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 2040, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "TrackTracePurchaseInvoices", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2040, bytesOut[0], 0, "ACC:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "TrackTracePurchaseInvoices", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2040, bytesOut[0], 0, "SID:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    set(con, stmt, rs, out, req, p1, unm, sid, uty, men, den, dnm, bnm, imagesDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 2040, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String supplierCode, String unm, String sid,
                   String uty, String men, String den, String dnm, String bnm, String imagesDir, String localDefnsDir, String defnsDir, int[] bytesOut)
                   throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Track &amp; Trace: Purchase Invoices</title>");
    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    scoutln(out, bytesOut, "function search(option){document.forms[0].which.value=option;document.options.submit();}");

    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\""
                         + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\"></head>");
  
    int[] hmenuCount = new int[1];
 
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "2040", "", "TrackTracePurchaseInvoices", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<form action=\"TrackTracePurchaseInvoicesa\" name=options enctype=\"application/x-www-form-urlencoded\" method=POST>");
    scoutln(out, bytesOut, "<input type=hidden name=unm value=" + unm + ">");
    scoutln(out, bytesOut, "<input type=hidden name=sid value=" + sid + ">");
    scoutln(out, bytesOut, "<input type=hidden name=uty value=" + uty + ">");
    scoutln(out, bytesOut, "<input type=hidden name=men value=" + men + ">");
    scoutln(out, bytesOut, "<input type=hidden name=den value=" + den + ">");
    scoutln(out, bytesOut, "<input type=hidden name=dnm value=" + dnm + ">");
    scoutln(out, bytesOut, "<input type=hidden name=bnm value=" + bnm + ">");
    scoutln(out, bytesOut, "<input type=hidden name=which value=''>");

    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Track &amp; Trace: Purchase Invoices", "2040", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    if(supplierCode.length() > 0) // called with a known suppCode
      scoutln(out, bytesOut, "<input type=hidden name=suppCode value=" + supplierCode + ">");

    scoutln(out, bytesOut, "<table id=\"page\" width=100% border=0>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td colspan=5><p>This service provides a range of options for searching Purchase Invoices.</i>");
    scoutln(out, bytesOut, "Use one of the following:</td></tr>");

    scoutln(out, bytesOut, "<tr><td><img src=\"" + imagesDir + "z402.gif\"></td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=5><hr></td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=5><p>1. Provide a <i>Purchase Invoice Code</i>, to process one specific Purchase Invoice.</td></tr>");
    scoutln(out, bytesOut, "<tr><td><img src=\"" + imagesDir + "z402.gif\"></td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap><p>Purchase Invoice Code</td>");
    scoutln(out, bytesOut, "<td><p><input type=text maxlength=20 size=20 name=invoiceCode></td></tr>");
    scoutln(out, bytesOut, "<tr><td><img src=\"" + imagesDir + "z402.gif\"></td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=2><p><a href=\"javascript:search(1)\">Fetch</a> the Purchase Invoice</td></tr>");

    scoutln(out, bytesOut, "<tr><td><img src=\"" + imagesDir + "z402.gif\"></td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=5><hr></td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=5><p>2. Provide an <i>Item Code</i>, to process all Purchase Invoices that contain that stock item.</td></tr>");
    scoutln(out, bytesOut, "<tr><td><img src=\"" + imagesDir + "z402.gif\"></td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap><p>Stock Item Code</td>");
    scoutln(out, bytesOut, "<td><p><input type=text maxlength=20 size=20 name=itemCode1></td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap><p>From date &nbsp;&nbsp;</td><td><p><input type=text maxlength=10 size=10 name=dateFrom1></td>");
    scoutln(out, bytesOut, "<td nowrap><p>and to date &nbsp;&nbsp;</td><td><p><input type=text maxlength=10 size=10 name=dateTo1>");
    scoutln(out, bytesOut, "&nbsp; (optional)</td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap><p>Unpaid Cash Only</td><td nowrap><p><input type=checkbox name=osOnly1 checked></td></tr>");
    scoutln(out, bytesOut, "<tr><td><img src=\"" + imagesDir + "z402.gif\"></td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=2><p><a href=\"javascript:search(2)\">Search</a> for matching Purchase Invoice</td></tr>");

    if(supplierCode.length() == 0) // not called with a known suppCode
    {
      scoutln(out, bytesOut, "<tr><td><img src=\"" + imagesDir + "z402.gif\"></td></tr>");
      scoutln(out, bytesOut, "<tr><td colspan=5><hr></td></tr>");
      scoutln(out, bytesOut, "<tr><td colspan=5><p>3. Provide a <i>Supplier Code</i>, to process all Purchase Invoices for that Supplier.</td></tr>");
      scoutln(out, bytesOut, "<tr><td><img src=\"" + imagesDir + "z402.gif\"></td></tr>");
      scoutln(out, bytesOut, "<tr><td nowrap><p>Supplier Code</td>");
      scoutln(out, bytesOut, "<td><p><input type=text maxlength=20 size=20 name=suppCode></td>");
      scoutln(out, bytesOut, "<tr><td nowrap><p>From date &nbsp;&nbsp;</td><td><p><input type=text maxlength=10 size=10 name=dateFrom2></td>");
      scoutln(out, bytesOut, "<td nowrap><p>and to date &nbsp;&nbsp;</td><td><p><input type=text maxlength=10 size=10 name=dateTo2>");
      scoutln(out, bytesOut, "&nbsp; (optional)</td></tr>");
      scoutln(out, bytesOut, "<tr><td nowrap><p>Unpaid Cash Only</td><td nowrap><p><input type=checkbox name=osOnly2 checked></td></tr>");
      scoutln(out, bytesOut, "<tr><td><img src=\"" + imagesDir + "z402.gif\"></td></tr>");
      scoutln(out, bytesOut, "<tr><td colspan=2><p><a href=\"javascript:search(3)\">Search</a> for matching Purchase Invoices</td></tr>");
    }
    
    scoutln(out, bytesOut, "<tr><td><img src=\"" + imagesDir + "z402.gif\"></td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=5><hr></td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=5><p>4. Provide a <i>Date Range</i>, to process all Purchase Invoices within that range.</td></tr>");
    scoutln(out, bytesOut, "<tr><td><img src=\"" + imagesDir + "z402.gif\"></td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap><p>SalesPerson</td><td><p>");
    getSalesPeople(out, "salesPerson4", dnm, bytesOut);
    scoutln(out, bytesOut, "</td></tr>"); 
    scoutln(out, bytesOut, "<tr><td nowrap><p>From date &nbsp;&nbsp;</td><td><p><input type=text maxlength=10 size=10 name=dateFrom3></td>");
    scoutln(out, bytesOut, "<td nowrap><p>and to date &nbsp;&nbsp;</td><td><p><input type=text maxlength=10 size=10 name=dateTo3></td>");
    scoutln(out, bytesOut, "<td><p>&nbsp; (optional)</td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap><p>Unpaid Cash Only</td><td nowrap><p><input type=checkbox name=osOnly3 checked></td></tr>");
    scoutln(out, bytesOut, "<tr><td><img src=\"" + imagesDir + "z402.gif\"></td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=2><p><a href=\"javascript:search(4)\">Search</a> for matching Purchase Invoices</td></tr>");
        
    scoutln(out, bytesOut, "<tr><td><img src=\"" + imagesDir + "z402.gif\"></td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=5><hr></td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=5><p>5. Provide a <i>Date Range</i>, to process all Purchase Invoices within that range; ");
    scoutln(out, bytesOut, "listing Purchase Invoice lines.</td></tr>");
    scoutln(out, bytesOut, "<tr><td><img src=\"" + imagesDir + "z402.gif\"></td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap><p>From date &nbsp;&nbsp;</td><td><p><input type=text maxlength=10 size=10 name=dateFrom4></td>");
    scoutln(out, bytesOut, "<td nowrap><p>and to date &nbsp;&nbsp;</td><td><p><input type=text maxlength=10 size=10 name=dateTo4></td>");
    scoutln(out, bytesOut, "<td><p>&nbsp; (optional)</td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap><p>Unpaid Cash Only</td><td nowrap><p><input type=checkbox name=osOnly4 checked></td></tr>");
    scoutln(out, bytesOut, "<tr><td><img src=\"" + imagesDir + "z402.gif\"></td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=2><p><a href=\"javascript:search(5)\">Search</a> for matching Purchase Invoices</td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    
    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void getSalesPeople(PrintWriter out, String name, String dnm, int[] bytesOut) throws Exception
  {
    Connection con = null;
    Statement stmt = null;
    ResultSet rs   = null;
      
    try
    {
      scoutln(out, bytesOut, "<SELECT name=\"" + name + "\">");
      scoutln(out, bytesOut, "<OPTION value=\"ALL\">All");

      String uName    = directoryUtils.getMySQLUserName();
      String passWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();
    
      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + uName + "&password=" + passWord);

      stmt = con.createStatement();
      rs = stmt.executeQuery("SELECT Name FROM salesper ORDER BY Name");
      
      String salesPerson;
      
      while(rs.next())
      {
        salesPerson = rs.getString(1);
        scoutln(out, bytesOut, "<OPTION value=\"" + generalUtils.sanitise(salesPerson) + "\">" + salesPerson);
      }
      
      scoutln(out, bytesOut, "</SELECT>");

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }
    catch(Exception e)
    {
      System.out.println("2040: " + e);
      try
      {
        if(rs   != null) rs.close();     
        if(stmt != null) stmt.close();
        if(con  != null) con.close();
      }
      catch(Exception e2) { }
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
