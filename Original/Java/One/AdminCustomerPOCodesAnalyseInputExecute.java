// =======================================================================================================================================================================================================
// System: ZaraStar: Admin: Analyze customer PO codes: do it
// Module: AdminCustomerPOCodesAnalyseInputExecute.java
// Author: C.K.Harvey
// Copyright (c) 2002-08 Christopher Harvey. All Rights Reserved.
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

public class AdminCustomerPOCodesAnalyseInputExecute extends HttpServlet
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
    String urlBit="";

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
      p1  = req.getParameter("p1");
      p2  = req.getParameter("p2");
      
      if(p1 == null) p1 = "01.01.1970";
      if(p2 == null) p2 = "0";
      
      StringBuffer url = req.getRequestURL();
      int x=0;
      if(url.charAt(x) == 'h' || url.charAt(x) == 'H')
        x += 7;
      urlBit="";
      while(url.charAt(x) != '\000' && url.charAt(x) != ',' && url.charAt(x) != '/')
        urlBit += url.charAt(x++);

      doIt(out, req, p1, p2, unm, sid, uty, men, den, dnm, bnm, urlBit, bytesOut);
    }
    catch(Exception e)
    {
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "AdminCustomerPOCodesAnalyseInputExecute", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7077, bytesOut[0], 0, "ERR:" + p1+":"+p2);
      if(out != null) out.flush();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String p1, String p2, String unm, String sid, String uty, String men, String den,
                    String dnm, String bnm, String urlBit, int[] bytesOut) throws Exception
  {
    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    long startTime = new java.util.Date().getTime();

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    
    Statement stmt = null, stmt2 = null;
    ResultSet rs   = null, rs2 = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 7077, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "AdminCustomerPOCodesAnalyseInputExecute", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7077, bytesOut[0], 0, "ACC:" + p1+":"+p2);
      
      if(con != null) con.close();
     if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "AdminCustomerPOCodesAnalyseInputExecute", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7077, bytesOut[0], 0, "SID:" + p1+":"+p2);
      
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }
    
    scoutln(out, bytesOut, "<html><head><title>Analyze Customer PO Codes</title>");

    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir)
                        + "general.css\">");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">"); 

    scoutln(out, bytesOut, "function v(phrase){var p1=sanitise(phrase);");    
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/AdminCustomerPOCodesAnalyzeViewDocs?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den
                         + "&dnm=" + dnm + "&p1=\"+p1+\"&bnm=" + bnm + "\";}");

    scoutln(out, bytesOut, "function r(phrase){var p1=sanitise(phrase);");    
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/AdminCustomerPOCodesCleanExecute?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den
                         + "&dnm=" + dnm + "&p1=\"+p1+\"&bnm=" + bnm + "\";}");

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
 
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "7077", "", "AdminCustomerPOCodesAnalyseInputExecute", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Analyze Customer PO Codes", "7077", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<table id=\"page\" border=0 cellpadding=2 cellspacing=0 width=100%>");
    scoutln(out, bytesOut, "<tr id=\"pageColumn\"><td><p>Table</td><td><p>Customer PO Code</td><td><p>Occurrences</td></tr>");

    String dateYYYYMMDD = generalUtils.convertDateToSQLFormat(p1);
    int occurrences = generalUtils.intFromStr(p2);

    process(con, stmt, stmt2, rs, rs2, out, "so",       "CustomerPOCode", "Sales Order",              dateYYYYMMDD, occurrences, bytesOut);
    process(con, stmt, stmt2, rs, rs2, out, "wo",       "CustomerPOCode", "Works Order",              dateYYYYMMDD, occurrences, bytesOut);
    process(con, stmt, stmt2, rs, rs2, out, "oc",       "CustomerPOCode", "Sales Order Confirmation", dateYYYYMMDD, occurrences, bytesOut);
    process(con, stmt, stmt2, rs, rs2, out, "pl",       "CustomerPOCode", "Picking List",             dateYYYYMMDD, occurrences, bytesOut);
    process(con, stmt, stmt2, rs, rs2, out, "do",       "PORefNum",       "Delivery Order",           dateYYYYMMDD, occurrences, bytesOut);
    process(con, stmt, stmt2, rs, rs2, out, "invoice",  "PORefNum",       "Sales Invoice",            dateYYYYMMDD, occurrences, bytesOut);
    process(con, stmt, stmt2, rs, rs2, out, "debit",    "PORefNum",       "Sales Debit Note",         dateYYYYMMDD, occurrences, bytesOut);
    process(con, stmt, stmt2, rs, rs2, out, "proforma", "PORefNum",       "Proforma Invoice",         dateYYYYMMDD, occurrences, bytesOut);

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir));
    
    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 7077, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1+":"+p2);
      
    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void process(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, String tableName,
                       String custPOCodeFldName, String tableNameStr, String date, int occurrences, int[] bytesOut) throws Exception
  {
    int num;
    String custPOCode;

    stmt = con.createStatement();
    
//System.out.println("SELECT DISTINCT " + custPOCodeFldName + " FROM " + tableName);
    rs = stmt.executeQuery("SELECT DISTINCT " + custPOCodeFldName + " FROM " + tableName);
    
    while(rs.next())
    {
      custPOCode = generalUtils.deNull(rs.getString(1));
      
      if(custPOCode.length() > 0)
      {
        stmt2 = con.createStatement();
      
        rs2 = stmt2.executeQuery("SELECT COUNT(*) AS rowcount FROM " + tableName + " WHERE " + custPOCodeFldName + " = '" + generalUtils.sanitiseForSQL(custPOCode) + "' AND Date >= {d '" + date + "'}");

        if(rs2.next())
        {
          num = rs2.getInt("rowcount");

          if(num > occurrences)
          {
            scoutln(out, bytesOut, "<tr><td><p>" + tableNameStr + "</td><td><p>" + custPOCode + "</td><td><p>" + num
                                 + "</td><td><p><a href='javascript:v(\"" + custPOCode + "\")'>View</a></td>"
                                 + "<td><p>&nbsp;&nbsp;&nbsp;&nbsp;<a href='javascript:r(\"" + custPOCode + "\")'>Remove</a></td></tr>");
          }
        }
      }
      
      if(rs2   != null) rs2.close();
      if(stmt2 != null) stmt2.close();
    }
  
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
