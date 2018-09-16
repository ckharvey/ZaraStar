// =======================================================================================================================================================================================================
// System: ZaraStar Catalogs: For a Zara-format catalog
// Module: CatalogsZara.java
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

public class CatalogsZara extends HttpServlet
{
  GeneralUtils  generalUtils = new GeneralUtils();
  MessagePage  messagePage = new MessagePage();
  ServerUtils  serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils  directoryUtils = new DirectoryUtils();
  SCCatalogsUtils sCCatalogsUtils = new SCCatalogsUtils();
    
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
      p2  = req.getParameter("p2");
      
      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, bytesOut);
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
 
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "CatalogsZara", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6401, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                    String p1, String p2, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 6401, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "CatalogsZara", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6401, bytesOut[0], 0, "ACC:");
      
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "CatalogsZara", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6401, bytesOut[0], 0, "SID:");
      
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    set(con, stmt, rs, out, req, p1, p2, unm, sid, uty, men, den, dnm, bnm, imagesDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 6401, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
      
      if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String mfr, String viewOption, String unm,
                   String sid, String uty, String men, String den, String dnm, String bnm, String imagesDir, String localDefnsDir, String defnsDir,
                   int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Zara Catalogs</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    scoutln(out, bytesOut, "function cate(which){");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/CatalogsSteelclawsCategoryEdit?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den
                         + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + generalUtils.sanitise(mfr) + "&p2=\"+which;}");

    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\""
                         + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\"></head>");
  
    int[] hmenuCount = new int[1];

    sCCatalogsUtils.outputPageFrame(con, stmt, rs, out, req, "6401", "CatalogsZara", unm, sid, uty, men, den, dnm, bnm, "", "", localDefnsDir, defnsDir, hmenuCount, bytesOut);

    sCCatalogsUtils.drawTitle(con, stmt, rs, req, out, mfr, "Catalog: " + mfr, "6401", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<form><table id=\"page\" width=100% border=0>");

    if(viewOption.equals("C"))
      listPages(out, mfr, dnm, localDefnsDir, defnsDir, bytesOut);
    else listOriginalPages(out, mfr, dnm, localDefnsDir, defnsDir, bytesOut);
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
  
    scoutln(out, bytesOut, "</table></form>");     scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }
    
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void listPages(PrintWriter out, String mfr, String dnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    String userName = directoryUtils.getMySQLUserName();
    String passWord = directoryUtils.getMySQLPassWord();
    
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + userName + "&password=" + passWord);
    Statement stmt = con.createStatement(), stmt2 = null;
    ResultSet rs = null, rs2= null;

    rs = stmt.executeQuery("SELECT Chapter, Section, Page FROM catalogc WHERE Manufacturer = '" + generalUtils.sanitiseForSQL(mfr) + "' ORDER BY Position");

    String chapter, pageName, section, lastPageName="", lastSection="", lastChapter="";
    String[] cssFormat = new String[1];  cssFormat[0] = "";
    
    while(rs.next())
    {
      chapter  = rs.getString(1);
      section  = rs.getString(2);
      pageName = rs.getString(3);

      listCategoriesOnAPage(con, stmt2, rs2, out, mfr, chapter, section, pageName, cssFormat, bytesOut);
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    if(con  != null) con.close(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void listCategoriesOnAPage(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String mfr, String chapter, String section,
                                     String pageName, String[] cssFormat, int[] bytesOut) throws Exception
  {
    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT Title, Category FROM catalog WHERE Manufacturer = '" + generalUtils.sanitiseForSQL(mfr) + "' AND Chapter = '"
                           + generalUtils.sanitiseForSQL(chapter) + "' AND Section = '" + generalUtils.sanitiseForSQL(section) + "' AND PageName = '"
                           + generalUtils.sanitiseForSQL(pageName) + "' ORDER BY Title");

      String title, category, lastPageName="", lastSection="", lastChapter="";
    
      while(rs.next())
      {
        title    = rs.getString(1);
        category = rs.getString(2);

        if(cssFormat[0].equals("line1")) cssFormat[0] = "line2"; else cssFormat[0] = "line1";

        scoutln(out, bytesOut, "<tr id='" + cssFormat[0] + "'><td nowrap><p>" + chapter + "</td><td nowrap><p>" + section + "</td><td nowrap><p>"
                             + pageName + "</td><td nowrap><p><a href=\"javascript:cate('" + category + "')\">" + title + "</a></td></tr>");
      }
    }
    catch(Exception e) { System.out.println("6401: " + e); }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void listOriginalPages(PrintWriter out, String mfr, String dnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    String userName = directoryUtils.getMySQLUserName();
    String passWord = directoryUtils.getMySQLPassWord();
    
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + userName + "&password=" + passWord);
    Statement stmt = con.createStatement();

    ResultSet rs = stmt.executeQuery("SELECT Title, Category, OriginalPage FROM catalog WHERE Manufacturer = '" + mfr
                                   + "' ORDER BY OriginalPage, Category");

    String title, category, originalPage, lastOriginalPage="", cssFormat = "";
    
    while(rs.next())
    {
      title        = rs.getString(1);
      category     = rs.getString(2);
      originalPage = rs.getString(3);
      
      if(! originalPage.equals(lastOriginalPage))
      {  
        if(cssFormat.equals("line1")) cssFormat = "line2"; else cssFormat = "line1";

        scoutln(out, bytesOut, "<tr id='" + cssFormat + "'><td nowrap><p>Original Page " + originalPage + ": &nbsp;&nbsp;&nbsp;</td>");
        lastOriginalPage = originalPage;
      }
      else scoutln(out, bytesOut, "<tr id='" + cssFormat + "'><td></td>");
        
      scoutln(out, bytesOut, "<td><p>" + category + "</td>");
      scoutln(out, bytesOut, "<td nowrap width=99%><p><a href=\"javascript:cate('" + category + "')\">" + title + "</a></td></tr>");
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    if(con  != null) con.close(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }
  
}
