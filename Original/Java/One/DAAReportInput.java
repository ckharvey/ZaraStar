// =======================================================================================================================================================================================================
// System: ZaraStar Analytic: Get DAA report params 
// Module: DAAReportInput.java
// Author: C.K.Harvey
// Copyright (c) 1999-2008 Christopher Harvey. All Rights Reserved.
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

public class DAAReportInput extends HttpServlet
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "MainPageUtils4a", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1004, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm,
                    String bnm, int[] bytesOut) throws Exception
   {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');
    
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;
    
    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 1004, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "1004a", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1004, bytesOut[0], 0, "ACC:");
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "1004a", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1004, bytesOut[0], 0, "SID:");
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    set(con, stmt, rs, out, req, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 1004, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty,
                   String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Debtors Ageing Report Parameters</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    scoutln(out, bytesOut, "function generate(){");
    scoutln(out, bytesOut, "var dateFrom=document.forms[0].datefrom.value;");
    scoutln(out, bytesOut, "var dateTo=document.forms[0].dateto.value;");
    scoutln(out, bytesOut, "var range1=document.forms[0].range1.value;");
    scoutln(out, bytesOut, "var range2=document.forms[0].range2.value;");
    scoutln(out, bytesOut, "var range3=document.forms[0].range3.value;");
    scoutln(out, bytesOut, "var range4=document.forms[0].range4.value;");
    scoutln(out, bytesOut, "var type;if(document.forms[0].style[0].checked)type='S';else type='D';");
    scoutln(out, bytesOut, "var order;if(document.forms[0].order[0].checked)order='C';else order='N';");
    scoutln(out, bytesOut, "var ignoreZero;if(document.forms[0].ignoreZero.checked)ignoreZero='Y';else ignoreZero='N';");
    scoutln(out, bytesOut, "var customer=sanitise(document.forms[0].customer.value);");
    scoutln(out, bytesOut, "window.location.href='/central/servlet/MainPageUtils4b?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den
                           + "&dnm=" + dnm + "&bnm=" + bnm + "&p1='+type+'&p2='+dateFrom+'&p3='+dateTo+'&p4='+ignoreZero+'&p5='+range1+'&p6='+range2"
                           + "+'&p7='+range3+'&p8='+range4+'&p10='+order+'&p9='+customer;}");

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
    
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "1004", "", "MainPageUtils4", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
                                
    scoutln(out, bytesOut, "<form>");
  
    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Debtors Ageing Generation", "1004",  unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<table id=\"page\" width=100%>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap><p>For Date Starting &nbsp;</td>");
    scoutln(out, bytesOut, "<td><input name=datefrom type=text maxlength=10 size=10></td>");
    scoutln(out, bytesOut, "<td width=90% align=left><p>&nbsp; Consider invoices dated after this date. Usually left blank.</td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap><p>For Date Ending &nbsp;</td>");
    scoutln(out, bytesOut, "<td><input name=dateto type=text maxlength=10 size=10></td>");
    scoutln(out, bytesOut, "<td><p>&nbsp; The date backwards from which date ranges are applied.</td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap><p>For Range 1 &nbsp;</td>");
    scoutln(out, bytesOut, "<td><input name=range1 type=text value=\"30\" maxlength=6 size=6></td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap><p>For Range 2 &nbsp;</td>");
    scoutln(out, bytesOut, "<td><input name=range2 type=text value=\"60\" maxlength=6 size=6></td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap><p>For Range 3 &nbsp;</td>");
    scoutln(out, bytesOut, "<td><input name=range3 type=text value=\"90\" maxlength=6 size=6></td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap><p>For Range 4 &nbsp;</td>");
    scoutln(out, bytesOut, "<td><input name=range4 type=text value=\"120\" maxlength=6 size=6></td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap><p>Detail Required: &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Summary &nbsp;<input name=style type=radio checked>&nbsp; Detailed &nbsp;");
    scoutln(out, bytesOut, "<input name=style type=radio></td>");
    scoutln(out, bytesOut, "<td>&nbsp;(Detailed shows each invoice outstanding).</td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap><p>Ordered By: &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Customer Code &nbsp;<input name=order type=radio checked>&nbsp; Customer Name &nbsp;");
    scoutln(out, bytesOut, "<input name=order type=radio></td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td><input name=ignoreZero type=checkbox checked></td>");
    scoutln(out, bytesOut, "<td nowrap><p>&nbsp; Ignore Outstanding Balances of Zero</td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap><p>Customer Code &nbsp;</td>");
    scoutln(out, bytesOut, "<td><input name=customer type=text maxlength=20 size=20></td>");
    scoutln(out, bytesOut, "<td nowrap><p>&nbsp; Leave blank for all.</td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=4 nowrap><p><a href=\"javascript:generate()\">Generate</a> new Report</td></tr>");
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir));
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
