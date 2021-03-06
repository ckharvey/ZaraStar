// =======================================================================================================================================================================================================
// System: ZaraStar mail: view library
// Module: MailZaraViewLibrary.java
// Author: C.K.Harvey
// Copyright (c) 2002-08 Christopher Harvey. All Rights Reserved.
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

public class MailZaraViewLibrary extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  MailUtils mailUtils = new MailUtils();

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
      p1  = req.getParameter("p1"); // User or All
      
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
 
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "MailZaraViewLibrary", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8008, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                    String p1, int[] bytesOut) throws Exception
  {
    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    long startTime = new java.util.Date().getTime();

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 8014, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "MailZaraViewLibrary", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8008, bytesOut[0], 0, "ACC:" + p1);
      
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "MailZaraViewLibrary", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8008, bytesOut[0], 0, "SID:" + p1);
      
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    set(con, stmt, rs, out, req, p1, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 8008, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    
    if(con  != null) con.close();
    if(out != null) out.flush();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String userOrAll, String unm, String sid,
                   String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut)
                   throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Mail Library</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");
  
    scoutln(out, bytesOut, "function search(){");
    scoutln(out, bytesOut, "var dateFrom=sanitise(document.forms[0].dateFrom.value);");
    scoutln(out, bytesOut, "var dateTo=sanitise(document.forms[0].dateTo.value);");
    scoutln(out, bytesOut, "var mailFrom=sanitise(document.forms[0].mailFrom.value);");
    scoutln(out, bytesOut, "var mailTo=sanitise(document.forms[0].mailTo.value);");
    scoutln(out, bytesOut, "var domainFrom=sanitise(document.forms[0].domainFrom.value);");
    scoutln(out, bytesOut, "var domainTo=sanitise(document.forms[0].domainTo.value);");
    scoutln(out, bytesOut, "var companyCode=sanitise(document.forms[0].companyCode.value);");
    scoutln(out, bytesOut, "var projectCode=sanitise(document.forms[0].projectCode.value);");
    scoutln(out, bytesOut, "var textBody=sanitise(document.forms[0].textBody.value);");
    scoutln(out, bytesOut, "var subject=sanitise(document.forms[0].subject.value);");
    scoutln(out, bytesOut, "var sentOrReceived='B';if(document.forms[0].direction[1].checked)sentOrReceived='S';");
    scoutln(out, bytesOut, "else if(document.forms[0].direction[2].checked)sentOrReceived='R';");
    scoutln(out, bytesOut, "var companyType;if(document.forms[0].companyType[0].selected)companyType='C';else companyType='S';");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/MailZaraViewSearchLibrary?&p1=\"+dateFrom+\"&p2=\"+dateTo+\"&p3=\"+mailFrom+\"&p4=\"+mailTo+\""
                         + "&p5=\"+domainFrom+\"&p6=\"+domainTo+\"&p7=\"+companyCode+\"&p8=\"+companyType+\"&p9=\"+projectCode+\""
                         + "&p10=\"+textBody+\"&p11=\"+subject+\"&p12=\"+sentOrReceived+\"&p13=" + userOrAll + "&unm=" + unm
                         + "&uty=" + uty + "&sid=" + sid + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\";}");

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

    mailUtils.outputPageFrame(con, stmt, rs, out, req, false, "", "", "MailZaraViewLibrary", "8008", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount, bytesOut);

    mailUtils.drawTitle(con, stmt, rs, req, out, false, "", "Mail Library", "8008", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<form><table id=\"page\" width=100% border=0>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap><p>Date From &nbsp;</td><td><input type=text maxlength=10 size=10 name=dateFrom>");
    scoutln(out, bytesOut, "&nbsp; &nbsp; &nbsp; Date To &nbsp;<input type=text maxlength=10 size=10 name=dateTo></td></tr>");
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap><p>Mail From</td></td><td><input type=text maxlength=60 size=60 name=mailFrom></td>");
    scoutln(out, bytesOut, "<tr><td nowrap><p>Domain From</td><td><input type=text maxlength=60 size=60 name=domainFrom></td>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap><p>Mail To</td></td><td><input type=text maxlength=60 size=60 name=mailTo></td>");
    scoutln(out, bytesOut, "<tr><td nowrap><p>Domain To</td><td><input type=text maxlength=60 size=60 name=domainTo></td>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    
    scoutln(out, bytesOut, "<tr><td><p>Company Code</td><td><p><input type=text name=companyCode size=20>");
    scoutln(out, bytesOut, "&nbsp;&nbsp;<input type=radio name=companyType value='C' checked>Customer");
    scoutln(out, bytesOut, "&nbsp;&nbsp;<input type=radio name=companyType value='S'>Supplier</td><tr>");

    scoutln(out, bytesOut, "<tr><td nowrap><p>Project Code</td><td><input type=text maxlength=20 size=20 name=projectCode></td>");
        
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap><p>Subject</td><td><input type=text maxlength=100 size=60 name=subject></td>");
    scoutln(out, bytesOut, "<tr><td nowrap><p>Text Body</td><td><input type=text maxlength=100 size=60 name=textBody></td>");

    scoutln(out, bytesOut, "<tr><td nowrap colspan=2><p><input type=radio name=direction checked>Sent and Received</td>");
    scoutln(out, bytesOut, "<tr><td nowrap colspan=2><p><input type=radio name=direction>Sent Only</td>");
    scoutln(out, bytesOut, "<tr><td nowrap colspan=2><p><input type=radio name=direction>Received Only</td>");
           
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td><a href=\"javascript:search()\">Search</a></td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "</table></form>");     scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
