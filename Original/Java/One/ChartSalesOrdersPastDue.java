// =======================================================================================================================================================================================================
// System: ZaraStar: Charts: Sales Orders Past Due
// Module: ChartSalesOrdersPastDue.java
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

public class ChartSalesOrdersPastDue extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  DocumentUtils documentUtils = new DocumentUtils();
  DefinitionTables definitionTables = new DefinitionTables();

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
      directoryUtils.setContentHeaders(res);
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
 
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "ChartSalesOrdersPastDue", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1100, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush(); 
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    
    Statement stmt = null;
    ResultSet rs   = null;
    
    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 1100, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "ChartSalesOrdersPastDue", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1100, bytesOut[0], 0, "ACC:");
      
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "ChartSalesOrdersPastDue", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1100, bytesOut[0], 0, "SID:");
      
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    set(con, stmt, rs, out, req, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 1100, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir,
                   int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Sales Orders Past Due</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");
  
    scoutln(out, bytesOut, "function generate(){");
    scoutln(out, bytesOut, "var p1=document.forms[0].salesPerson.value;");
    scoutln(out, bytesOut, "var p2=document.forms[0].from.value;");
    scoutln(out, bytesOut, "var p3=document.forms[0].to.value;");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/SalesOrdersPastDueExecute?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=\"+p1+\"&p2=\"+p2+\"&p3=\"+p3+\"&bnm=" + bnm + "\";}");

    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\"></head>");
  
    int[] hmenuCount = new int[1];
    
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "1100", "", "ChartSalesOrdersPastDue", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
                                
    scoutln(out, bytesOut, "<form>");
  
    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Sales Orders Past Due", "1100",  unm, sid, uty, men, den, dnm, bnm,hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<table id=\"page\" width=100% border=0>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=5><p>Generates a chart using Sales Order data,");
    scoutln(out, bytesOut, " for an optional SalesPerson, over a specified date range.</td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap><p>SalesPerson</td><td>");
    getSalesPeople(con, stmt, rs, out, bytesOut);

    scoutln(out, bytesOut, "</td></tr><tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap><p>From Month</td><td>");
    getMonths(con, stmt, rs, out, "from", dnm, localDefnsDir, defnsDir, bytesOut);
    scoutln(out, bytesOut, "</td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap><p>");
    scoutln(out, bytesOut, "To Month</td><td>");
    getMonths(con, stmt, rs, out, "to", dnm, localDefnsDir, defnsDir, bytesOut);
    scoutln(out, bytesOut, "</td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td colspan=2><p>");
    scoutln(out, bytesOut, "<a href=\"javascript:generate()\">Generate</a> the chart</td></tr>");
   
    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void getSalesPeople(Connection con, Statement stmt, ResultSet rs, PrintWriter out, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<SELECT NAME=\"salesPerson\">");

    scoutln(out, bytesOut, "<OPTION VALUE=\"ALL\">All");

    String[][] names = new String[1][];  
    int numNames = documentUtils.getSalesPersonNames(con, stmt, rs, names);

    for(int x=0;x<numNames;++x)
      scoutln(out, bytesOut, "<OPTION VALUE=\"" + generalUtils.sanitise(names[0][x]) + "\">" + names[0][x]);

    scoutln(out, bytesOut, "</SELECT>");
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void getMonths(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String which, String localDefnsDir, String defnsDir, String dnm, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<SELECT NAME=\"" + which + "\">");

    byte[] nextDate = new byte[20];
    byte[] prevDate = new byte[20];

    int systemStartDate = generalUtils.encode(definitionTables.getAppConfigApplicationStartDate(con, stmt, rs, dnm), localDefnsDir, defnsDir);

    int theDate = generalUtils.todayEncoded(localDefnsDir, defnsDir);
    short thisMonth, thisYear;
    String s;

    while(theDate >= systemStartDate)
    {
      thisMonth = generalUtils.getMonth(theDate);
      thisYear  = generalUtils.getYear(theDate);
      s = generalUtils.yearAndMonthToMonthYearStr(thisMonth, thisYear);
      scoutln(out, bytesOut, "<OPTION VALUE=\"" + thisMonth + "-" + thisYear + "\">" + s + "\n");

      generalUtils.decode(theDate, nextDate, localDefnsDir, defnsDir);
      generalUtils.previousMonthStart(nextDate, prevDate, localDefnsDir, defnsDir);
      theDate = generalUtils.encode(prevDate, localDefnsDir, defnsDir);
    }

    scoutln(out, bytesOut, "</SELECT>");
  }

}

