// =======================================================================================================================================================================================================
// System: ZaraStar Utils: Anonymous User products page
// Module: MailZaraAnonymousUserProducts.java
// Author: C.K.Harvey
// Copyright (c) 2002-09 Christopher Harvey. All Rights Reserved.
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

public class MailZaraAnonymousUserProducts extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  AdminControlUtils adminControlUtils = new AdminControlUtils();
  
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
 
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "MailZaraAnonymousUserProducts", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 802, bytesOut[0], 0, "ERR:");
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

    if(! adminControlUtils.notDisabled(con, stmt, rs, 802))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "MailZaraAnonymousUserProducts", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 802, bytesOut[0], 0, "ACC:");
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      if(uty.equals("A"))
      {
        String sessionsDir = directoryUtils.getSessionsDir(dnm);
        sid = serverUtils.newSessionID(unm, "A", dnm, sessionsDir, localDefnsDir, defnsDir);
        den = dnm;
        unm = "_" + sid;
        StringBuffer url = req.getRequestURL();
        int x=0;
        if(url.charAt(x) == 'h' || url.charAt(x) == 'H')
          x += 7;
        men="";
        while(url.charAt(x) != '\000' && url.charAt(x) != ',' && url.charAt(x) != '/')
          men += url.charAt(x++);
      }
      else
      {
        messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "MailZaraAnonymousUserProducts", imagesDir, localDefnsDir, defnsDir, bytesOut);
        serverUtils.etotalBytes(req, unm, dnm, 802, bytesOut[0], 0, "SID:");
        if(con != null) con.close();
        if(out != null) out.flush();
        return;
      }
    }

    set(con, stmt, rs, out, req, unm, sid, uty, men, den, dnm, bnm, imagesDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 802, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String imagesDir, String localDefnsDir, String defnsDir,
                   int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Anonymous User Products Page</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");
    
    boolean ProductCatalogsAdmin = (adminControlUtils.notDisabled(con, stmt, rs, 902) || adminControlUtils.notDisabled(con, stmt, rs, 802));

    if(ProductCatalogsAdmin)
    {
      scoutln(out, bytesOut, "function browse(){");
      scoutln(out, bytesOut, "window.location.href=\"/central/servlet/CatalogList?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\";}");
    }

    scoutln(out, bytesOut, "function ourSearch(){var code=sanitise(document.forms[0].itemCode.value);");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/StockEnquiryExternal?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+code;}");

    scoutln(out, bytesOut, "function mfrSearch(){");
    scoutln(out, bytesOut, "var mfr=sanitise(document.forms[0].mfr.value);");
    scoutln(out, bytesOut, "var mfrcode=sanitise(document.forms[0].mfrCode.value);");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/StockEnquiryExternal?&p2=\"+mfr+\"&p3=\"+mfrcode+\"&unm=" + unm + "&uty=" + uty + "&sid=" + sid + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\";}");

    scoutln(out, bytesOut, "function wordSearch(){var phrase=sanitise(document.forms[0].phrase.value);");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/ProductWordPhraseSearch?p1=\"+phrase+\"&unm=" + unm + "&uty=" + uty + "&sid=" + sid + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\";}");

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
 
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "802", "", "MailZaraAnonymousUserProducts", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Product Services for User: Casual", "802", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<form><table id=\"page\" width=100% border=0>");

    if(ProductCatalogsAdmin)
    {
      scoutln(out, bytesOut, "<tr><td nowrap colspan=3><h1>Browse by Manufacturer Catalogs</td></tr>");

      scoutln(out, bytesOut, "<tr><td><img src=\"" + imagesDir + "z402.gif" + "\"></td></tr>");

      scoutln(out, bytesOut, "<tr><td nowrap><p><a href=\"javascript:browse()\">Browse</a> our Full Range of Product Catalogs and Listings</td></tr>");

      scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

      scoutln(out, bytesOut, "</table><table id=\"page\" width=100% border=0>");
    }

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
      
    scoutln(out, bytesOut, "<tr><td nowrap colspan=3><h1>Search our Stock DataBase</td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap><p>If you know our stock code, you may enter it here:</td></tr>");

    scoutln(out, bytesOut, "<tr><td><img src=\"" + imagesDir + "z402.gif" + "\"></td></tr>");
      
    scoutln(out, bytesOut, "<tr><td nowrap><p>Our Stock Code&nbsp;&nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p><input type=text name=itemCode size=20></td>");
    scoutln(out, bytesOut, "<td nowrap width=99%><p>&nbsp;&nbsp;&nbsp; and <a href=\"javascript:ourSearch()\">Search</a></td></tr>");
   
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=3><p>Or search by Manufacturer and Manufacturer Code:</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap><p>Manufacturer &nbsp;</td><td>");
    getMfrsDDL(out, dnm, bytesOut);
    scoutln(out, bytesOut, "</td></tr>");

    scoutln(out, bytesOut, "<tr><td><img src=\"" + imagesDir + "z402.gif" + "\"></td></tr>");
    scoutln(out, bytesOut, "<tr><td width=1% nowrap><p>Manufacturer Code&nbsp;&nbsp;</td><td nowrap><p><input type=text maxlength=30 size=20 name=mfrCode></td><td><p>");
    scoutln(out, bytesOut, "&nbsp;&nbsp;&nbsp; and <a href=\"javascript:mfrSearch()\">Search</a></td></tr>");
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap><p>Or Word Search&nbsp;&nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p><input type=text name=phrase size=30 maxlength=100></td>");
    scoutln(out, bytesOut, "<td nowrap width=99%><p>&nbsp;&nbsp;&nbsp; and <a href=\"javascript:wordSearch()\">Search</a></td></tr>");
   
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    
    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void getMfrsDDL(PrintWriter out, String dnm, int[] bytesOut) throws Exception
  {
    String userName = directoryUtils.getMySQLUserName();
    String passWord = directoryUtils.getMySQLPassWord();
    
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + userName + "&password=" + passWord);
    Statement stmt = con.createStatement();

    ResultSet rs = stmt.executeQuery("SELECT DISTINCT Manufacturer FROM stock WHERE ShowToWeb = 'Y' ORDER BY Manufacturer");

    scoutln(out, bytesOut, "<select name=\"mfr\">");
    
    String mfr;
    
    while(rs.next())
    {
      mfr = rs.getString(1);
      if(mfr.length() > 0)
        scoutln(out, bytesOut, "<option value=\"" + mfr + "\">" + mfr);
    }

    scoutln(out, bytesOut, "</select>");
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    if(con  != null) con.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }
  
}
