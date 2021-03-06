// =============================================================================================================================================================
// System: ZaraStar Accounts: Debtor & Creditor Analysis
// Module: AccountsDebtorCreditorAnalysis.java
// Author: C.K.Harvey
// Copyright (c) 1999-2007 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.sql.*;

public class AccountsDebtorCreditorAnalysis extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "AccountsDebtorCreditorAnalysis", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6038, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut)
                    throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 6038, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "6038", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6038, bytesOut[0], 0, "ACC:");
      
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "6038", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6038, bytesOut[0], 0, "SID:");
      
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    set(con, stmt, rs, out, req, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 6038, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    
    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty,
                   String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Debtor and Creditor Analysis</title>");

    scoutln(out, bytesOut, "<SCRIPT LANGUAGE=\"JavaScript\">");

    scoutln(out, bytesOut, "function go(){var monthTo=document.forms[0].monthTo.value;");
    scoutln(out, bytesOut, "window.location.href='/central/servlet/AnalyticsDebtorCreditorAnalysis?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm="
                         + dnm + "&bnm=" + bnm + "&p1='+monthTo;}");

    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\""
                           + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\"></head>");
  
    int[] hmenuCount = new int[1];
 
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "6038", "", "AccountsDebtorCreditorAnalysis", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
                                
    scoutln(out, bytesOut, "<form>");

    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Debtor and Creditor Analysis", "6038", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<table id='page' width=100%>");

    scoutln(out, bytesOut, "<tr><td nowrap><p>For Month Ending </td><td><p>");
    getMonths(con, stmt, rs, out, "monthTo", dnm, localDefnsDir, defnsDir, bytesOut);
    scoutln(out, bytesOut, "</td></tr>");
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap><p><a href=\"javascript:go()\">Analyse</a> all Debtor and Creditor Data</td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void getMonths(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String which, String dnm, String localDefnsDir, String defnsDir, int[] bytesOut)
                         throws Exception
  {
    scoutln(out, bytesOut, "<SELECT NAME=\"" + which + "\">");

    byte[] nextDate = new byte[20];
    byte[] prevDate = new byte[20];

    int effectiveStartDate = generalUtils.encode(definitionTables.getAppConfigEffectiveStartDate(con, stmt, rs, dnm), localDefnsDir, defnsDir);
    
    int theDate = generalUtils.todayEncoded(localDefnsDir, defnsDir);
    short thisMonth, thisYear;
    String s;

    while(theDate >= effectiveStartDate)
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

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
