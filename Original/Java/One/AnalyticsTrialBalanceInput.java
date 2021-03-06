// =======================================================================================================================================================================================================
// System: ZaraStar: Analytic - Trial Balance: Create report params page
// Module: AnalyticsTrialBalanceInput.java
// Author: C.K.Harvey
// Copyright (c) 2003-09 Christopher Harvey. All Rights Reserved.
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

public class AnalyticsTrialBalanceInput extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  DefinitionTables definitionTables = new DefinitionTables();
  AdminUtils adminUtils = new AdminUtils();
  
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
      p1  = req.getParameter("p1");

      if(p1 == null) p1 = "G";

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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "AnalyticsTrialBalanceInput", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6053, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String which, int[] bytesOut) throws Exception
   {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');
    String workingDir    = directoryUtils.getUserDir('W', dnm, unm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 6053, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "6053e", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6053, bytesOut[0], 0, "ACC:");
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "6053e", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6053, bytesOut[0], 0, "SID:");
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    set(con, stmt, rs, out, req, which, unm, sid, uty, men, den, dnm, bnm, workingDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 6053, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String which, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String workingDir, String localDefnsDir,
                   String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Trial Balance Parameters</title>");

    scoutln(out, bytesOut, "<SCRIPT LANGUAGE=\"JavaScript\">");

    scoutln(out, bytesOut, "function generate(which){");
    scoutln(out, bytesOut, "var p1=document.forms[0].monthFrom.value;");
    scoutln(out, bytesOut, "var p2=document.forms[0].monthTo.value;");
    scoutln(out, bytesOut, "window.location.href='/central/servlet/AccountsGenerateTBReport?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p5=" + which + "&p4='+which+'&p1='+p1+'&p2='+p2;}");

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

    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\"></head>");
  
    int[] hmenuCount = new int[1];
 
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "6053", "", "_6053", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
                                
    scoutln(out, bytesOut, "<form>");

    String s = "Trail Balance Generation";
    if(which.equals("S"))
      s += " (Debtors and Creditors Detail)";

    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", s, "6053", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<table id=\"page\" width=100%>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    String yyyy = generalUtils.getFromDefnFile(unm, "accounts.dfn", workingDir, "");
    int year = generalUtils.strToInt(yyyy);

    scoutln(out, bytesOut, "<tr><td nowrap><p>Month From </td><td><p>");
    getMonths(con, stmt, rs, out, "monthFrom", year, dnm, bytesOut);
    scoutln(out, bytesOut, "</td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap><p>Month To </td><td><p>");
    getMonths(con, stmt, rs, out, "monthTo", year, dnm, bytesOut);
    scoutln(out, bytesOut, "</td></tr>");
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
        
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=4 nowrap><p><a href=\"javascript:generate('S')\">Generate</a> new Report: Separate Debits and Credits</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=4 nowrap><p><a href=\"javascript:generate('C')\">Generate</a> new Report: Combined Debits and Credits</td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void getMonths(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String which, int year, String dnm, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<SELECT NAME=\"" + which + "\">");

    String[] financialYearStartMonth = new String[1];
    String[] financialYearEndMonth   = new String[1];

    definitionTables.getAppConfigFinancialYearMonths(con, stmt, rs, dnm, financialYearStartMonth, financialYearEndMonth);

    int startMonth = generalUtils.detMonthNumFromMonthName(financialYearStartMonth[0]);

    int x = 0;
    int thisMonth = startMonth;
    int thisYear  = year;
    String s;

    while(x < 12)
    {
      s = generalUtils.yearAndMonthToMonthYearStr(thisMonth, thisYear);
      scoutln(out, bytesOut, "<OPTION VALUE=\"" + thisMonth + "-" + thisYear + "\">" + s + "\n");

      ++thisMonth;
      if(thisMonth == 13)
      {
        thisMonth = 1;
        ++thisYear;
      }

      ++x;
    }

    scoutln(out, bytesOut, "</SELECT>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
