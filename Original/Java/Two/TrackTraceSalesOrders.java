// =======================================================================================================================================================================================================
// System: ZaraStar: TNT: Sales Orders
// Module: TrackTraceSalesOrders.java
// Author: C.K.Harvey
// Copyright (c) 2003-09 Christopher Harvey. All Rights Reserved.
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

public class TrackTraceSalesOrders extends HttpServlet
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
      directoryUtils.setContentHeaders(res);
      out = res.getWriter();

      unm = req.getParameter("unm");
      sid = req.getParameter("sid");
      uty = req.getParameter("uty");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");
      p1  = req.getParameter("p1"); // custCode
      
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
 
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "TrackTraceSalesOrders", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2021, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 2021, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "TrackTraceSalesOrders", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2021, bytesOut[0], 0, "ACC:");
      if(con  != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "TrackTraceSalesOrders", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2021, bytesOut[0], 0, "SID:");
      if(con  != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    set(con, stmt, rs, out, req, p1, unm, sid, uty, men, den, dnm, bnm, imagesDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 2021, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con  != null) con.close();
    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String customerCode, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String imagesDir,
                   String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Track &amp; Trace: Sales Orders</title>");
    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    scoutln(out, bytesOut, "function search(option){document.forms[0].which.value=option;document.options.submit();}");

    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\"></head>");
  
    int[] hmenuCount = new int[1];
 
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "2021", "", "TrackTraceSalesOrders", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<form action=\"TrackTraceSalesOrdersa\" name=options enctype=\"application/x-www-form-urlencoded\" method=POST>");
    scoutln(out, bytesOut, "<input type=hidden name=unm value=" + unm + ">");
    scoutln(out, bytesOut, "<input type=hidden name=sid value=" + sid + ">");
    scoutln(out, bytesOut, "<input type=hidden name=uty value=" + uty + ">");
    scoutln(out, bytesOut, "<input type=hidden name=men value=" + men + ">");
    scoutln(out, bytesOut, "<input type=hidden name=den value=" + den + ">");
    scoutln(out, bytesOut, "<input type=hidden name=dnm value=" + dnm + ">");
    scoutln(out, bytesOut, "<input type=hidden name=bnm value=" + bnm + ">");
    scoutln(out, bytesOut, "<input type=hidden name=which value=''>");

    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Track &amp; Trace: Sales Orders", "2021", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    if(customerCode.length() > 0) // called with a known custCode
      scoutln(out, bytesOut, "<input type=hidden name=custCode value=" + customerCode + ">");

    scoutln(out, bytesOut, "<table id=\"page\" width=100% border=0>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td colspan=5><p>This service provides a range of options for searching Sales Orders.</i>");
    scoutln(out, bytesOut, "Use one of the following:</td></tr>");

    scoutln(out, bytesOut, "<tr><td><img src=\"" + imagesDir + "z402.gif\"></td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=5><hr></td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=5><p>1. Provide a <i>Sales Order Code</i>, to process one specific Sales Order.</td></tr>");
    scoutln(out, bytesOut, "<tr><td><img src=\"" + imagesDir + "z402.gif\"></td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap><p>Sales Order Code</td>");
    scoutln(out, bytesOut, "<td><p><input type=text maxlength=20 size=20 name=soCode></td></tr>");
    scoutln(out, bytesOut, "<tr><td><img src=\"" + imagesDir + "z402.gif\"></td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=2><p><a href=\"javascript:search(1)\">Fetch</a> the Sales Order</td></tr>");

    scoutln(out, bytesOut, "<tr><td><img src=\"" + imagesDir + "z402.gif\"></td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=5><hr></td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=5><p>2. Provide a <i>Customer PO Code</i>, to process one specific Sales Order.</td></tr>");
    scoutln(out, bytesOut, "<tr><td><img src=\"" + imagesDir + "z402.gif\"></td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap><p>Customer PO Code</td>");
    scoutln(out, bytesOut, "<td><p><input type=text maxlength=30 size=20 name=cpoCode></td></tr>");
    scoutln(out, bytesOut, "<tr><td><img src=\"" + imagesDir + "z402.gif\"></td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=2><p><a href=\"javascript:search(2)\">Fetch</a> the Sales Order</td></tr>");

    scoutln(out, bytesOut, "<tr><td><img src=\"" + imagesDir + "z402.gif\"></td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=5><hr></td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=5><p>3. Provide an <i>Item Code</i>, to process all Sales Orders that contain that stock item.</td></tr>");
    scoutln(out, bytesOut, "<tr><td><img src=\"" + imagesDir + "z402.gif\"></td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap><p>Stock Item Code</td>");
    scoutln(out, bytesOut, "<td><p><input type=text maxlength=20 size=20 name=itemCode1></td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap><p>From date &nbsp;&nbsp;</td><td><p><input type=text maxlength=10 size=10 name=dateFrom1></td>");
    scoutln(out, bytesOut, "<td nowrap><p>and to date &nbsp;&nbsp;</td><td><p><input type=text maxlength=10 size=10 name=dateTo1>");
    scoutln(out, bytesOut, "&nbsp; (optional)</td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap><p><input type=radio name=dateType1 value=I checked>Issued Dates</td>");
    scoutln(out, bytesOut, "<td nowrap><p><input type=radio name=dateType1 value=D>Delivery Dates</td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap><p>Outstanding Only</td><td nowrap><p><input type=checkbox name=osOnly1 checked></td></tr>");
    scoutln(out, bytesOut, "<tr><td><img src=\"" + imagesDir + "z402.gif\"></td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=2><p><a href=\"javascript:search(3)\">Search</a> for matching Sales Orders</td></tr>");

    if(customerCode.length() == 0) // not called with a known custCode
    {
      scoutln(out, bytesOut, "<tr><td><img src=\"" + imagesDir + "z402.gif\"></td></tr>");
      scoutln(out, bytesOut, "<tr><td colspan=5><hr></td></tr>");
      scoutln(out, bytesOut, "<tr><td colspan=5><p>4. Provide a <i>Customer Code</i>, to process all Sales Orders for that Customer.</td></tr>");
      scoutln(out, bytesOut, "<tr><td><img src=\"" + imagesDir + "z402.gif\"></td></tr>");
      scoutln(out, bytesOut, "<tr><td nowrap><p>Customer Code</td>");
      scoutln(out, bytesOut, "<td><p><input type=text maxlength=20 size=20 name=custCode></td>");
      scoutln(out, bytesOut, "<tr><td nowrap><p>Stock Item Code</td>");
      scoutln(out, bytesOut, "<td><p><input type=text maxlength=20 size=20 name=itemCode2></td>");
      scoutln(out, bytesOut, "<td><p>&nbsp; (optional)</td></tr>");
      scoutln(out, bytesOut, "<tr><td nowrap><p>From date &nbsp;&nbsp;</td><td><p><input type=text maxlength=10 size=10 name=dateFrom2></td>");
      scoutln(out, bytesOut, "<td nowrap><p>and to date &nbsp;&nbsp;</td><td><p><input type=text maxlength=10 size=10 name=dateTo2>");
      scoutln(out, bytesOut, "&nbsp; (optional)</td></tr>");
      scoutln(out, bytesOut, "<tr><td nowrap><p><input type=radio name=dateType2 value=I checked>Issued Dates</td>");
      scoutln(out, bytesOut, "<td nowrap><p><input type=radio name=dateType2 value=D>Delivery Dates</td></tr>");
      scoutln(out, bytesOut, "<tr><td nowrap><p>Outstanding Only</td><td nowrap><p><input type=checkbox name=osOnly2 checked></td></tr>");
      scoutln(out, bytesOut, "<tr><td><img src=\"" + imagesDir + "z402.gif\"></td></tr>");
      scoutln(out, bytesOut, "<tr><td colspan=2><p><a href=\"javascript:search(4)\">Search</a> for matching Sales Orders</td></tr>");
    }
    
    scoutln(out, bytesOut, "<tr><td><img src=\"" + imagesDir + "z402.gif\"></td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=5><hr></td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=5><p>5. Provide a <i>Date Range</i>, to process all Sales Orders within that range; ");
    scoutln(out, bytesOut, "listing Sales Order lines.</td></tr>");
    scoutln(out, bytesOut, "<tr><td><img src=\"" + imagesDir + "z402.gif\"></td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap><p>From date &nbsp;&nbsp;</td><td><p><input type=text maxlength=10 size=10 name=dateFrom3></td>");
    scoutln(out, bytesOut, "<td nowrap><p>and to date &nbsp;&nbsp;</td><td><p><input type=text maxlength=10 size=10 name=dateTo3></td>");
    scoutln(out, bytesOut, "<td><p>&nbsp; (optional)</td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap><p><input type=radio name=dateType3 value=I checked>Issued Dates</td>");
    scoutln(out, bytesOut, "<td nowrap><p><input type=radio name=dateType3 value=D>Delivery Dates</td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap><p>Outstanding Only</td><td nowrap><p><input type=checkbox name=osOnly3 checked></td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap><p>SalesPerson</td><td nowrap><p>" + getSalesPeople(con, stmt, rs) + "</td></tr>");
    scoutln(out, bytesOut, "<tr><td><img src=\"" + imagesDir + "z402.gif\"></td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=2><p><a href=\"javascript:search(5)\">Search</a> for matching Sales Orders</td></tr>");
    
    scoutln(out, bytesOut, "<tr><td><img src=\"" + imagesDir + "z402.gif\"></td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=5><hr></td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=5><p>6. Provide a <i>Date Range</i>, to process all Sales Orders within that range; ");
    scoutln(out, bytesOut, "listing Sales Order headers only.</td></tr>");
    scoutln(out, bytesOut, "<tr><td><img src=\"" + imagesDir + "z402.gif\"></td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap><p>From date &nbsp;&nbsp;</td><td><p><input type=text maxlength=10 size=10 name=dateFrom4></td>");
    scoutln(out, bytesOut, "<td nowrap><p>and to date &nbsp;&nbsp;</td><td><p><input type=text maxlength=10 size=10 name=dateTo4></td>");
    scoutln(out, bytesOut, "<td><p>&nbsp; (optional)</td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap><p>Outstanding Only</td><td nowrap><p><input type=checkbox name=osOnly4 checked></td></tr>");
    scoutln(out, bytesOut, "<tr><td><img src=\"" + imagesDir + "z402.gif\"></td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=2><p><a href=\"javascript:search(6)\">Search</a> for matching Sales Orders</td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    
    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String getSalesPeople(Connection con, Statement stmt, ResultSet rs) throws Exception
  {
    String s = "<SELECT NAME=\"state5\">";

    s += "<OPTION VALUE=\"ALL\">All";

    String[][] names = new String[1][];
    int numNames = documentUtils.getSalesPersonNames(con, stmt, rs, names);

    for(int x=0;x<numNames;++x)
      s += "<OPTION VALUE=\"" + generalUtils.sanitise(names[0][x]) + "\">" + names[0][x];

    s += "</SELECT>";

    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}

