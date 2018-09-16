// =======================================================================================================================================================================================================
// System: ZaraStar: Customer: Settlement History: Request params
// Module: CustomerSettlementHistoryInput.java
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

public class CustomerSettlementHistoryInput extends HttpServlet
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

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="";

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
      p1  = req.getParameter("p1"); // customerCode
      p2  = req.getParameter("p2"); // cashOnly

      if(p2 == null) p2 = " ";
      
      doIt(out, req, p1, p2, unm, sid, uty, men, den, dnm, bnm, bytesOut);
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
 
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "CustomerSettlementHistoryInput", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 4002, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String p1, String p2, String unm, String sid, String uty,
                    String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    
    long startTime = new java.util.Date().getTime();

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
     
   Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 4002, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "CustomerSettlementHistoryInput", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 4002, bytesOut[0], 0, "ACC:" + p1);
       
      if(con != null) con.close();
     if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "CustomerSettlementHistoryInput", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 4002, bytesOut[0], 0, "SID:" + p1);
       
      if(con != null) con.close();
    if(out != null) out.flush();
      return;
    }

    String customerCode;
    if(p1.equalsIgnoreCase("null") || p1.length() == 0)
      customerCode = "";
    else customerCode = p1;

    set(con, stmt, rs, out, req, customerCode, p2, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 4002, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
      
      if(con != null) con.close();
   if(out != null) out.flush();
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String customerCode,
                   String cashTransactions, String unm, String sid, String uty, String men,
                   String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Settlement Requirements</title>");
    scoutln(out, bytesOut, "<SCRIPT LANGUAGE=\"JavaScript\">");

    scoutln(out, bytesOut, "function search(){");
    if(cashTransactions.charAt(0) == 'C')
      scoutln(out, bytesOut, "var code2='';");
    else
    {
      scoutln(out, bytesOut, "var code=document.forms[0].companycode.value;");

      scoutln(out, bytesOut, "var code2='';var x;var len=code.length;");
      scoutln(out, bytesOut, "for(x=0;x<len;++x)");
      scoutln(out, bytesOut, "if(code.charAt(x)=='#')code2+='%23';");
      scoutln(out, bytesOut, "else if(code.charAt(x)=='\"')code2+='%22';");
      scoutln(out, bytesOut, "else if(code.charAt(x)=='&')code2+='%26';");
      scoutln(out, bytesOut, "else if(code.charAt(x)=='%')code2+='%25';");
      scoutln(out, bytesOut, "else if(code.charAt(x)==' ')code2+='%20';");
      scoutln(out, bytesOut, "else if(code.charAt(x)=='?')code+='%3F';");
      scoutln(out, bytesOut, "else code2+=code.charAt(x);");
    }
    
    scoutln(out, bytesOut, "var datefrom=document.forms[0].datefrom.value;");
    scoutln(out, bytesOut, "var dateto=document.forms[0].dateto.value;");
    scoutln(out, bytesOut, "var datecut=document.forms[0].datecut.value;");
    scoutln(out, bytesOut, "var unpaid;if(document.forms[0].unpaid.checked)unpaid=\"Y\";else unpaid=\"N\";");

    if(cashTransactions.charAt(0) == 'C')
      scoutln(out, bytesOut, "var cash='Y';");
    else scoutln(out, bytesOut, "var cash='N';");

    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/CustomerSettlementHistoryPage?unm=" + unm + "&uty=" + uty + "&sid=" + sid
                         + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                         + "&p1=\"+unpaid+\"&p6=\"+cash+\"&p2=\"+code2+\"&p3=\"+datefrom+\"&p4=\"+dateto+\"&p5=\"+datecut;}");

    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\""
                           + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\"></head>");
  
    int[] hmenuCount = new int[1];
 
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "4002", "", "CustomerSettlementHistoryInput", unm, sid, uty, men, den, dnm, bnm, localDefnsDir,
                          defnsDir, hmenuCount, bytesOut);
                                
    scoutln(out, bytesOut, "<form>");

    if(cashTransactions.charAt(0) == 'C')
      pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Invoice Settlement History for CASH Transactions", "4002",  unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);
    else pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Invoice Settlement History", "4002",  unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<table id=\"page\" width=100%>");
    scoutln(out, bytesOut, "<tr><td colspan=2 nowrap><p>Select the parameters to fetch settlement history:</td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td width=1% nowrap><p>Cut-off Date for Credit Notes and Receipts &nbsp;</td><td>"
                         + "<input type=text maxlength=10 size=10 name=datecut></td>");
    scoutln(out, bytesOut, "<td nowrap><span id=\"serviceNote\">&nbsp; (optional)</span></td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap><p>Select the Invoice date range you require: &nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap><p>Date From</td><td width=1%><input type=text maxlength=10 size=10 name=datefrom></td>");
    scoutln(out, bytesOut, "<td nowrap><span id=\"serviceNote\">&nbsp; (optional)</span></td></tr>");
    scoutln(out, bytesOut, "<td nowrap><p>Date To &nbsp;</td><td><input type=text maxlength=10 size=10 name=dateto></td>");
    scoutln(out, bytesOut, "<td nowrap><span id=\"serviceNote\">&nbsp; (optional)</span></td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    if(cashTransactions.charAt(0) != 'C')
    {
      scoutln(out, bytesOut, "<td><p>Enter a Customer Code</td>");
      scoutln(out, bytesOut, "<td colspan=2><p><input type=text maxlength=20 size=20 name=companycode value=\"" + customerCode + "\"></td></tr>");
    }

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td><p><input type=checkbox name=unpaid>&nbsp;&nbsp;Show outstanding only</td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap><p><a href=\"javascript:search()\">Show</a> Settlement Status</td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir));
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
