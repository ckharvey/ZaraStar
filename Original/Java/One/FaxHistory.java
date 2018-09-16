// =======================================================================================================================================================================================================
// System: ZaraStar Fax: view history
// Module: FaxHistory.java
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

public class FaxHistory extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  FaxUtils faxUtils = new FaxUtils();

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
 
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "FaxHistory", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 11003, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm,
                    String bnm, String p1, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    
    Statement stmt = null;
    ResultSet rs   = null;
    
     if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 11000, unm, uty, dnm, localDefnsDir, defnsDir))
     {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "FaxHistory", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 11003, bytesOut[0], 0, "ACC:" + p1);
      if(con  != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "FaxHistory", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 11003, bytesOut[0], 0, "SID:" + p1);
      if(con  != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    set(con, stmt, rs, out, req, p1, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 11003, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con  != null) con.close();
    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String userOrAll, String unm, String sid, String uty, String men, String den,
                   String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Historial Faxes</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");
  
    scoutln(out, bytesOut, "function search(){");
    scoutln(out, bytesOut, "var dateFrom=sanitise(document.forms[0].dateFrom.value);");
    scoutln(out, bytesOut, "var dateTo=sanitise(document.forms[0].dateTo.value);");
    scoutln(out, bytesOut, "var number=sanitise(document.forms[0].number.value);");
    scoutln(out, bytesOut, "var person=sanitise(document.forms[0].person.value);");
    scoutln(out, bytesOut, "var company=sanitise(document.forms[0].company.value);");
    scoutln(out, bytesOut, "var companyCode=sanitise(document.forms[0].companyCode.value);");
    scoutln(out, bytesOut, "var docType=document.forms[0].docCode.value;");
    scoutln(out, bytesOut, "var docCode=sanitise(document.forms[0].docCode.value);");
    scoutln(out, bytesOut, "var subject=sanitise(document.forms[0].subject.value);");
    scoutln(out, bytesOut, "var companyType;if(document.forms[0].companyType[0].selected)companyType='C';else companyType='S';");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/FaxSearch?&p1=\"+dateFrom+\"&p2=\"+dateTo+\"&p3=\"+number+\"&p4=\"+person+\""
                         + "&p5=\"+company+\"&p6=\"+docType+\"&p7=\"+docCode+\"&p8=\"+subject+\"&p9=\"+companyCode+\"&p10=\"+companyType+\"&p11="
                         + userOrAll + "&unm=" + unm + "&uty=" + uty + "&sid=" + sid + "&men=" + men + "&den=" + den + "&dnm="
                         + dnm + "&bnm=" + bnm + "\";}");

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

    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir)
                         + "general.css\"></head>");
  
    int[] hmenuCount = new int[1];

    faxUtils.outputPageFrame(con, stmt, rs, out, req, "", "FaxHistory", "11003", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount, bytesOut);

    faxUtils.drawTitle(con, stmt, rs, req, out, "Historical Faxes", "11003", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<p>All the following search parameters are optional. However, you are advised to use at least one in order to narrow");
    scoutln(out, bytesOut, "the search.<br><br>Parameters (other than dates) may be partially supplied. For example, entering <b><i>Zara</i></b>");
    scoutln(out, bytesOut, "into the Company field will find all faxes sent to any company with the word <i>Zara</i> in the name.");

    scoutln(out, bytesOut, "<form><table id=\"page\" width=100% border=0>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap><p>Date From &nbsp;</td><td><input type=text maxlength=10 size=10 name=dateFrom>");
    scoutln(out, bytesOut, "&nbsp; &nbsp; &nbsp; Date To &nbsp;<input type=text maxlength=10 size=10 name=dateTo></td></tr>");
    
    scoutln(out, bytesOut, "<tr><td nowrap><p>Fax Number &nbsp;</td></td><td><input type=text maxlength=60 size=60 name=number></td>");
    scoutln(out, bytesOut, "<tr><td nowrap><p>Person</td><td><input type=text maxlength=60 size=60 name=person></td>");
    scoutln(out, bytesOut, "<tr><td nowrap><p>Company</td><td><input type=text maxlength=60 size=60 name=company></td>");

    scoutln(out, bytesOut, "<tr><td><p>Company Code:</td><td><p><input type=text name=companyCode size=20>");
    scoutln(out, bytesOut, "&nbsp;&nbsp;<input type=radio name=companyType value='C' selected>Customer");
    scoutln(out, bytesOut, "&nbsp;&nbsp;<input type=radio name=companyType value='S'>Supplier</td><tr>");

    buildTypes(out, bytesOut);
    
    scoutln(out, bytesOut, "<tr><td nowrap><p>Document Code&nbsp;</td></td><td><input type=text maxlength=60 size=60 name=docCode></td>");
    scoutln(out, bytesOut, "<tr><td nowrap><p>Subject</td><td><input type=text maxlength=60 size=60 name=subject></td>");
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td><a href=\"javascript:search()\">Search</a></td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir));
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void buildTypes(PrintWriter out, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<tr><td nowrap><p>Document Type &nbsp;</td><td><select name=\"docType\">");
    
    scoutln(out, bytesOut, "<option value=\"ALL\">All");
    scoutln(out, bytesOut, "<option value=\"Q\">Quotation");
    scoutln(out, bytesOut, "<option value=\"O\">Order Confirmation");
    scoutln(out, bytesOut, "<option value=\"P\">Picking List");
    scoutln(out, bytesOut, "<option value=\"R\">Proforma Invoice");
    scoutln(out, bytesOut, "<option value=\"Y\">Purchase Order");
    scoutln(out, bytesOut, "<option value=\"Z\">Local Requisition");

    scoutln(out, bytesOut, "</select></td></tr>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}

