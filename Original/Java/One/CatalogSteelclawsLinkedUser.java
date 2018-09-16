// =======================================================================================================================================================================================================
// System: ZaraStar Catalog: For a SC catalog, linked-in user
// Module: CatalogSteelclawsLinkedUser.java
// Author: C.K.Harvey
// Copyright (c) 2002-08 Christopher Harvey. All Rights Reserved.
// Remark: On called server
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

public class CatalogSteelclawsLinkedUser extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  Profile profile = new Profile();
  
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    Writer r = null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", pwd="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p9="", p10="", p11="", p14="", p15="", p80="", p81="";

    try
    {
      res.setContentType("text/xml");
      res.setHeader("Cache-Control", "no-cache");
      r = res.getWriter();

      unm = req.getParameter("unm");
      pwd = req.getParameter("pwd");
      uty = req.getParameter("uty");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");
      p1  = req.getParameter("p1");  // mfr
      p9  = req.getParameter("p9");  // remoteSID
      p10 = req.getParameter("p10"); // catalogURL
      p11 = req.getParameter("p11"); // catalogUpline
      p14 = req.getParameter("p14"); // userType
      p15 = req.getParameter("p15"); // userName
      p80 = req.getParameter("p80"); // chapter
      p81 = req.getParameter("p81"); // section
      
      if(p80 == null) p80 = "";
      if(p81 == null) p81 = "";

      doIt(r, req, unm, pwd, uty, men, den, dnm, bnm, p1, p9, p10, p11, p14, p15, p80, p81, bytesOut);
    }
    catch(Exception e)
    {
      System.out.println("2004c: " + e);
      serverUtils.etotalBytes(req, unm, dnm, 2004, bytesOut[0], 0, "ERR:");
      try {  scoutln(r, bytesOut, "ERR:2004c "           +       e         ); } catch (Exception e2) { } 
      r.flush();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(Writer r, HttpServletRequest req, String unm, String pwd, String uty, String men, String den, String dnm, String bnm,
                    String p1, String p9, String p10, String p11, String p14, String p15, String p80, String p81, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');
    
    String userName = directoryUtils.getMySQLUserName();
    String passWord = directoryUtils.getMySQLPassWord();
    
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + userName + "&password=" + passWord);
    Statement stmt = null, stmt2 = null;
    ResultSet rs   = null, rs2   = null;
    
    if(p11.equals("Catalogs") || p14.equals("R")) // remote server
    {
      if(! serverUtils.checkSID(p15, p9, uty, p11, localDefnsDir, defnsDir))
      {
        serverUtils.etotalBytes(req, p15, p11, 2004, bytesOut[0], 0, "SID:2004c");
        if(con  != null) con.close();
        r.flush();
        return;
      }
    }
    else
    {
      if(! serverUtils.checkSID(unm, p9, uty, p11, localDefnsDir, defnsDir))
      {
        serverUtils.etotalBytes(req, p15, p11, 2004, bytesOut[0], 0, "SID:2004c");
        if(con  != null) con.close();
        r.flush();
        return;
      }
    }
    
    set(con, stmt, stmt2, rs, rs2, r, p1, p9, p10, p80, p81, p11, imagesDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, userName, p11, 2004, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con != null) con.close();
    r.flush();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, Writer r, String mfr, String remoteSID, String catalogURL, String p80, String p81, String catalogUpline, String imagesDir, String defnsDir,
                   int[] bytesOut) throws Exception
  {
    scoutln(r, bytesOut, "<form><table id='catalogPage'><tr><td colspan='2'>");

    outputOptions(con, stmt, rs, r, mfr, remoteSID, catalogURL, catalogUpline, defnsDir, bytesOut);
            
    scoutln(r, bytesOut, "</td></tr>");

    if(p80.length() == 0) // catalog home page
      displayContents(con, stmt, rs, r, mfr, remoteSID, imagesDir, bytesOut);
    else displaySection(con, stmt, stmt2, rs, rs2, r, mfr, p80, p81, remoteSID, bytesOut);
    
    scoutln(r, bytesOut, "<tr><td>&nbsp;</td></tr>");
  
    scoutln(r, bytesOut, "</table></form>");
  }
    
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void displayContents(Connection con, Statement stmt, ResultSet rs, Writer r, String mfr, String remoteSID, String imagesDir, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT Chapter, Section FROM catalogc WHERE Manufacturer = '" + generalUtils.sanitiseForSQL(mfr) + "' ORDER BY Position");

    String chapter, section, lastChapter = "", lastSection = "";
    boolean firstSection = true, firstChapter = true;
    
    while(rs.next())
    {
      chapter  = rs.getString(1);
      section  = rs.getString(2);

      if(! chapter.equals(lastChapter))
      {
        if(firstChapter)
          firstChapter = false;
        else scoutln(r, bytesOut, "</td></tr>");

        scoutln(r, bytesOut, "<tr><td>&nbsp;</td></tr>");
        scoutln(r, bytesOut, "<tr><td nowrap='nowrap' colspan='2'><h1>" + generalUtils.handleSuperScripts(chapter) + "</h1></td></tr>");

        firstSection = true; // again
        lastChapter = chapter;
      }

      if(! section.equals(lastSection))
      {
        if(firstSection)
        {
         scoutln(r, bytesOut, "<tr><td width='50'></td><td valign='center'><p>");
           firstSection = false;
        }
        else scoutln(r, bytesOut, "&nbsp;&nbsp;<img src=\"" + imagesDir + "d.png\" border='0'/>&nbsp;&nbsp;");
        
        scoutln(r, bytesOut, "<a href=\"javascript:section2005k('" + generalUtils.sanitise2(chapter) + "','" + generalUtils.sanitise2(section) + "','" + remoteSID
                           + "')\">" + generalUtils.handleSuperScripts(section) + "</a>");
        lastSection = section;
      }
    }

    if(lastChapter.length() > 0)
      scoutln(r, bytesOut, "</td></tr>");
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void displaySection(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, Writer r, String mfr, String chapter,
                              String section, String remoteSID, int[] bytesOut) throws Exception
  {
    scoutln(r, bytesOut, "<tr><td nowrap><h1>" + generalUtils.handleSuperScripts(chapter) + "</h1></td></tr>");
    scoutln(r, bytesOut, "<tr><td nowrap><h2>&nbsp;&nbsp;&nbsp;&nbsp;" + generalUtils.handleSuperScripts(section) + "</h2></td></tr>");

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT Page FROM catalogc WHERE Manufacturer = '" + generalUtils.sanitiseForSQL(mfr) + "' AND Chapter = '" + generalUtils.sanitiseForSQL(chapter) + "' AND Section = '" + generalUtils.sanitiseForSQL(section) + "' ORDER BY Position");

    String pageName;
    String[] cssFormat = new String[1];  cssFormat[0] = "2";

    while(rs.next())
    {
      pageName = rs.getString(1);

      listCategoriesOnAPage(con, stmt2, rs2, r, mfr, chapter, section, pageName, remoteSID, cssFormat, bytesOut);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void listCategoriesOnAPage(Connection con, Statement stmt, ResultSet rs, Writer r, String mfr, String chapter, String section, String pageName, String remoteSID, String[] cssFormat, int[] bytesOut) throws Exception
  {
    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT Title, Category FROM catalog WHERE Manufacturer = '" + generalUtils.sanitiseForSQL(mfr) + "' AND Chapter = '"
                           + generalUtils.sanitiseForSQL(chapter) + "' AND Section = '" + generalUtils.sanitiseForSQL(section) + "' AND PageName = '"
                           + generalUtils.sanitiseForSQL(pageName) + "' ORDER BY Title");

      String title, category, lastPageName="";
    
      while(rs.next())
      {
        title    = rs.getString(1);
        category = rs.getString(2);

        if(pageName.equals(lastPageName) && lastPageName.length() > 0)
          pageName = "";
        lastPageName = pageName;
  
        if(cssFormat[0].equals("1")) cssFormat[0] = "2"; else cssFormat[0] = "1";

        scoutln(r, bytesOut, "<tr id='CatalogPageLine" + cssFormat[0] + "'><td nowrap='nowrap'><p><a href=\"javascript:page2005e('','" + remoteSID + "','"
                             + generalUtils.sanitise2(chapter) + "','" + generalUtils.sanitise2(section) + "','" + generalUtils.sanitise2(pageName) + "')\">"
                             + generalUtils.handleSuperScripts(pageName) + "</a></p></td><td nowrap='nowrap' width='90%'><p><a href=\"javascript:page2005e('" + category + "','"
                             + remoteSID + "','','','')\">" + generalUtils.handleSuperScripts(title) + "</a></p></td></tr>");
      }
    }
    catch(Exception e) { System.out.println("2004c: " + e); }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void outputOptions(Connection con, Statement stmt, ResultSet rs, Writer r, String mfr, String remoteSID, String catalogURL, String catalogUpline, String defnsDir, int[] bytesOut) throws Exception
  {
    String imagesLibDir = directoryUtils.getImagesDir(catalogUpline);

    String[] title = new String[1];
    String[] image = new String[1];
    
    getImage(con, stmt, rs, mfr, title, image);

    outputOptionsBar(r, mfr, remoteSID, catalogURL, title[0], image[0], imagesLibDir, bytesOut);
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void outputOptionsBar(Writer r, String mfr, String remoteSID, String catalogURL, String title, String image, String imagesLibDir, int[] bytesOut) throws Exception
  {
    scoutln(r, bytesOut, "<div id='catalogBanner'><table>");

    if(! catalogURL.startsWith("http://"))
      catalogURL = "http://" + catalogURL;
    
    scoutln(r, bytesOut, "<tr><td valign='top' nowrap='nowrap' colspan='4'><h1>" + title + "</h1>");
    scoutln(r, bytesOut, "<td align='right' valign='top' rowspan='2'><img src=\"" + "http://www.xxx.com" + imagesLibDir + image + "\" border='0'/></td></tr>");

    scoutln(r, bytesOut, "<tr><td><p><a href=\"javascript:contents('" + mfr + "','" + remoteSID + "')\">Contents</a>&nbsp;&nbsp;&nbsp;&nbsp;");
    scoutln(r, bytesOut, "<a href=\"javascript:indecatalogSteelclawsDisplayIndex('C','" + remoteSID + "')\">Product Index</a>&nbsp;&nbsp;&nbsp;&nbsp;");
    scoutln(r, bytesOut, "<a href=\"javascript:newProducts('" + remoteSID + "')\">New Products</a>&nbsp;&nbsp;&nbsp;&nbsp;");
    scoutln(r, bytesOut, "Search for <input type='text' size='20' maxlength='80' name='searchPhrase'>&nbsp;&nbsp;<a href=\"javascript:search('"
                         + remoteSID + "')\">Find It</a></p></td></tr>");

    scoutln(r, bytesOut, "</table></div>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void getImage(Connection con, Statement stmt, ResultSet rs, String mfr, String[] title, String[] image) throws Exception
  {
    title[0] = image[0] = "";
    
    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT Title, Image FROM cataloglist WHERE Manufacturer = '" + generalUtils.sanitise(mfr) + "'");

      if(rs.next())
      {
        title[0] = rs.getString(1);
        image[0] = rs.getString(2);
      }
    }
    catch(Exception e) { }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(Writer r, int bytesOut[], String str) throws Exception
  {      
    r.write(str + "\n");
    bytesOut[0] += (str.length() + 1);    
  }
  
}
