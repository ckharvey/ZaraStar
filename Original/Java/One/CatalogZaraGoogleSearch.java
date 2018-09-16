// =======================================================================================================================================================================================================
// System: ZaraStar Catalogs: For a Zara-format catalog page - display page after google search
// Module: CatalogZaraGoogleSearch.java
// Author: C.K.Harvey
// Copyright (c) 2002-09 Christopher Harvey. All Rights Reserved.
// Where: On catalog server
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

public class CatalogZaraGoogleSearch extends HttpServlet
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
    PrintWriter out = null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="";

    try
    {
      res.setContentType("text/xml");
      res.setHeader("Cache-Control", "no-cache");
      out = res.getWriter();

      unm = req.getParameter("unm");
      sid = req.getParameter("sid");
      uty = req.getParameter("uty");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");
      p1  = req.getParameter("p1"); // mfr
      p2  = req.getParameter("p2"); // category

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, bytesOut);
    }
    catch(Exception e)
    {
      StringBuffer url = req.getRequestURL();
      String urlBit="";
      int x=0;
      if(url.charAt(x) == 'h' || url.charAt(x) == 'H')
        x += 7;
      while(url.charAt(x) != '\000' && url.charAt(x) != ',' && url.charAt(x) != '/')
        urlBit += url.charAt(x++);

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "CatalogZaraGoogleSearch", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2009, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush(); 
    }
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(Writer r, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1, String p2, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

 String p11="Stanley";   String catalogImagesDir = directoryUtils.getCatalogImagesDir(p11, p1); // TODO

    String uName = directoryUtils.getMySQLUserName();
    String pWord = directoryUtils.getMySQLPassWord();
    
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/Catalogs_ofsa?user=" + uName + "&password=" + pWord);
    Statement stmt = null, stmt2 = null, stmt3 = null;
    ResultSet rs   = null, rs2   = null, rs3   = null;

    String catalogURL = "http://catalogs.zaracloud.com"; // TODO
    set(con, stmt, stmt2, rs, rs2, r, p1, p2, catalogURL, catalogImagesDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 2009, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1 + ":" + p2);
    if(con  != null) con.close();
    r.flush();
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, Writer r, String mfr, String category, String catalogURL, String catalogImagesDir, int[] bytesOut) throws Exception
  {      

    scoutln(r, bytesOut, "<form><table id='catalogPage'><tr><td>");
    
    String[] mfrCodes       = new String[1];  mfrCodes[0] = "";
    String[] pricingDetails = new String[1];  pricingDetails[0] = "";

    forACategory(con, stmt, stmt2, rs, rs2, r, mfr, category, catalogURL, catalogImagesDir, bytesOut);
    
    scoutln(r, bytesOut, "</td></tr><tr><td>&#160;</td></tr>");
 
    scoutln(r, bytesOut, "</table></form>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void forACategory(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, Writer r, String mfr, String category, String catalogURL, String catalogImagesDir, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT ImageSmall, Title, Download, Text, CategoryLink, Text2, ExternalURL, Heading1, Heading2, Heading3, Heading4, Heading5, Heading6, Heading7, Heading8, Heading9, Heading10 FROM catalog WHERE Manufacturer = '"
                         + generalUtils.sanitiseForSQL(mfr) + "' AND Category = '" + category + "'");
    
    String imageSmall, title, download, text, categoryLink, text2, url, heading1, heading2, heading3, heading4, heading5, heading6, heading7, heading8, heading9, heading10;

    if(rs.next())
    {
      imageSmall     = rs.getString(1);
      title          = rs.getString(2);
      download       = rs.getString(3);
      text           = rs.getString(4);
      categoryLink   = rs.getString(5);
      text2          = rs.getString(6);
      url            = rs.getString(7);
      heading1       = rs.getString(8);
      heading2       = rs.getString(9);
      heading3       = rs.getString(10);
      heading4       = rs.getString(11);
      heading5       = rs.getString(12);
      heading6       = rs.getString(13);
      heading7       = rs.getString(14);
      heading8       = rs.getString(15);
      heading9       = rs.getString(16);
      heading10      = rs.getString(17);

      if(title == null) title = "";
      if(imageSmall == null) imageSmall = "";
      if(download == null) download = "0";
      if(categoryLink == null) categoryLink = "0";
      if(text == null) text = "";
      if(text2 == null) text2 = "";
      if(url == null) url = "";
      if(heading1 == null) heading1 = "";
      if(heading2 == null) heading2 = "";
      if(heading3 == null) heading3 = "";
      if(heading4 == null) heading4 = "";
      if(heading5 == null) heading5 = "";
      if(heading6 == null) heading6 = "";
      if(heading7 == null) heading7 = "";
      if(heading8 == null) heading8 = "";
      if(heading9 == null) heading9 = "";
      if(heading10 == null) heading10 = "";

      showCategory(con, stmt2, rs2, r, mfr, category, imageSmall, title, download, text, categoryLink, text2, url, heading1, heading2, heading3, heading4, heading5, heading6, heading7, heading8, heading9, heading10, catalogURL, catalogImagesDir,
                   bytesOut);
    }
   
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void showCategory(Connection con, Statement stmt, ResultSet rs, Writer r, String mfr, String category, String imageSmall, String title, String download, String text, String categoryLink, String text2, String url, String heading1,
                            String heading2, String heading3, String heading4, String heading5, String heading6, String heading7, String heading8, String heading9, String heading10, String catalogURL, String catalogImagesDir, int[] bytesOut)
                            throws Exception
  {
    scoutln(r, bytesOut, "<table id='catalogTable'>");
    scoutln(r, bytesOut, "<tr><td colspan='3'><h1>" + generalUtils.handleSuperScripts(title) + "</h1></td></tr>\n");

    String rowspan;
    if(text.length() == 0 || text.equals("<br>")) // no text
    {      
      text = " ";
      rowspan = "rowspan='2'";
    }
    else rowspan = "rowspan='1'";
             
    if(imageSmall.length() > 0) // image exists
    {
      scoutln(r, bytesOut, "<tr><td " + rowspan + " valign='top'><p><span id='imageSmall'><img border='1' src=\"" + catalogURL + catalogImagesDir + imageSmall + "\" /></span></p>\n");

      if(! download.equals("0")) // download exists
       scoutln(r, bytesOut, "<br /><p><a href=\"javascript:download('" + download + "')\">Download</a> more info</p>\n");
      if(url.length() > 0)
       scoutln(r, bytesOut, "<br /><p><a href=\"http://" + url + "\">Link</a> to Manufacturer</p>");
      if(! categoryLink.equals("0"))
        scoutln(r, bytesOut, "<br /><p><a href=\"javascript:display('" + categoryLink + "')\">Display</a> related items</p>\n");
        
      scoutln(r, bytesOut, "</td>");
        
      scoutln(r, bytesOut, "<td valign='top'><p>" + generalUtils.handleSuperScripts(text) + "</p></td></tr>");
      scoutln(r, bytesOut, "<tr><td></td><td>\n");
    }
    else // no image
    {
      scoutln(r, bytesOut, "<tr><td valign='top'><p>" + generalUtils.handleSuperScripts(text) + "</p></td></tr>\n");

      if(! download.equals("0")) // download exists
        scoutln(r, bytesOut, "<tr><td><p><a href=\"javascript:download('" + download + "')\">Download</a> more info</p></td></tr>\n");
      if(url.length() > 0)
        scoutln(r, bytesOut, "<tr><td colspan='2'><p><a href=\"http://" + url + "\">Link</a> to Manufacturer</p></td></tr>\n");
        
      if(! categoryLink.equals("0"))
        scoutln(r, bytesOut, "<tr><td colspan='2'><p><a href=\"javascript:display('" + categoryLink + "')\">Display</a> related items</p></td></tr>\n");
      scoutln(r, bytesOut, "<tr><td align='center'>");
    }
    
    scoutln(r, bytesOut, "<table id='catalogTable'><tr>");

    scoutln(r, bytesOut, "<th>Manufacturer Code</th><th>Description</th>\n");
    
    boolean wantEntry1, wantEntry2, wantEntry3, wantEntry4, wantEntry5, wantEntry6, wantEntry7, wantEntry8, wantEntry9, wantEntry10;
    
    if(heading1.length() > 0)  { scoutln(r, bytesOut, "<th>" + generalUtils.handleSuperScripts(heading1)  + "</th>"); wantEntry1  = true; } else wantEntry1  = false;
    if(heading2.length() > 0)  { scoutln(r, bytesOut, "<th>" + generalUtils.handleSuperScripts(heading2)  + "</th>"); wantEntry2  = true; } else wantEntry2  = false;
    if(heading3.length() > 0)  { scoutln(r, bytesOut, "<th>" + generalUtils.handleSuperScripts(heading3)  + "</th>"); wantEntry3  = true; } else wantEntry3  = false;
    if(heading4.length() > 0)  { scoutln(r, bytesOut, "<th>" + generalUtils.handleSuperScripts(heading4)  + "</th>"); wantEntry4  = true; } else wantEntry4  = false;
    if(heading5.length() > 0)  { scoutln(r, bytesOut, "<th>" + generalUtils.handleSuperScripts(heading5)  + "</th>"); wantEntry5  = true; } else wantEntry5  = false;
    if(heading6.length() > 0)  { scoutln(r, bytesOut, "<th>" + generalUtils.handleSuperScripts(heading6)  + "</th>"); wantEntry6  = true; } else wantEntry6  = false;
    if(heading7.length() > 0)  { scoutln(r, bytesOut, "<th>" + generalUtils.handleSuperScripts(heading7)  + "</th>"); wantEntry7  = true; } else wantEntry7  = false;
    if(heading8.length() > 0)  { scoutln(r, bytesOut, "<th>" + generalUtils.handleSuperScripts(heading8)  + "</th>"); wantEntry8  = true; } else wantEntry8  = false;
    if(heading9.length() > 0)  { scoutln(r, bytesOut, "<th>" + generalUtils.handleSuperScripts(heading9)  + "</th>"); wantEntry9  = true; } else wantEntry9  = false;
    if(heading10.length() > 0) { scoutln(r, bytesOut, "<th>" + generalUtils.handleSuperScripts(heading10) + "</th>"); wantEntry10 = true; } else wantEntry10 = false;
    
    showLines(con, stmt, rs, r, mfr, category, wantEntry1, wantEntry2, wantEntry3, wantEntry4, wantEntry5, wantEntry6, wantEntry7, wantEntry8, wantEntry9, wantEntry10, bytesOut);

    scoutln(r, bytesOut, "<tr><td valign='top' colspan='12'><p>" + generalUtils.handleSuperScripts(text2) + "</p></td></tr>");
    
    scoutln(r, bytesOut, "</table>");

    scoutln(r, bytesOut, "</td></tr></table>\n");

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void showLines(Connection con, Statement stmt, ResultSet rs, Writer r, String mfr, String category, boolean wantEntry1, boolean wantEntry2, boolean wantEntry3, boolean wantEntry4, boolean wantEntry5, boolean wantEntry6, boolean wantEntry7,
                         boolean wantEntry8, boolean wantEntry9, boolean wantEntry10, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT ManufacturerCode, Entry1, Entry2, Entry3, Entry4, Entry5, Entry6, Entry7, Entry8, Entry9, Entry10, Description, Description2 FROM catalogl WHERE Manufacturer = '" + generalUtils.sanitiseForSQL(mfr)
                         + "' AND Category = '" + category + "' ORDER BY Line");
    
    String mfrCode, entry1, entry2, entry3, entry4, entry5, entry6, entry7, entry8, entry9, entry10, desc, desc2;
    
    while(rs.next())
    {
      mfrCode = rs.getString(1);
      entry1  = rs.getString(2);
      entry2  = rs.getString(3);
      entry3  = rs.getString(4);
      entry4  = rs.getString(5);
      entry5  = rs.getString(6);
      entry6  = rs.getString(7);
      entry7  = rs.getString(8);
      entry8  = rs.getString(9);
      entry9  = rs.getString(10);
      entry10 = rs.getString(11);
      desc    = rs.getString(12);
      desc2   = rs.getString(13);
  
      if(mfrCode == null) mfrCode = "";
      if(desc    == null) desc = "";
      if(desc2   == null) desc2 = "";
      if(entry1  == null) entry1 = "";
      if(entry2  == null) entry2 = "";
      if(entry3  == null) entry3 = "";
      if(entry4  == null) entry4 = "";
      if(entry5  == null) entry5 = "";
      if(entry6  == null) entry6 = "";
      if(entry7  == null) entry7 = "";
      if(entry8  == null) entry8 = "";
      if(entry9  == null) entry9 = "";
      if(entry10 == null) entry10 = "";

      showLine(r, mfrCode, wantEntry1, wantEntry2, wantEntry3, wantEntry4, wantEntry5, wantEntry6, wantEntry7, wantEntry8, wantEntry9, wantEntry10, entry1, entry2, entry3, entry4, entry5, entry6, entry7, entry8, entry9, entry10, desc, desc2,
               bytesOut);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void showLine(Writer r, String mfrCode, boolean wantEntry1, boolean wantEntry2, boolean wantEntry3, boolean wantEntry4, boolean wantEntry5, boolean wantEntry6, boolean wantEntry7, boolean wantEntry8, boolean wantEntry9,
                        boolean wantEntry10, String entry1, String entry2, String entry3, String entry4, String entry5, String entry6, String entry7, String entry8, String entry9, String entry10, String desc, String desc2, int[] bytesOut)
                        throws Exception
  {
    scoutln(r, bytesOut, "<tr>");

    scoutln(r, bytesOut, "<td valign='top'><p>" + mfrCode + "</p></td>");

    scoutln(r, bytesOut, "<td valign='top'><p>" + generalUtils.handleSuperScripts(desc));
    if(desc2.length() > 0)
      scoutln(r, bytesOut, "<br />" + generalUtils.handleSuperScripts(desc2));
    scoutln(r, bytesOut, "</p></td>");

    if(wantEntry1)  scoutln(r, bytesOut, "<td valign='top'><p>" + entry1  + "</p></td>");
    if(wantEntry2)  scoutln(r, bytesOut, "<td valign='top'><p>" + entry2  + "</p></td>");
    if(wantEntry3)  scoutln(r, bytesOut, "<td valign='top'><p>" + entry3  + "</p></td>");
    if(wantEntry4)  scoutln(r, bytesOut, "<td valign='top'><p>" + entry4  + "</p></td>");
    if(wantEntry5)  scoutln(r, bytesOut, "<td valign='top'><p>" + entry5  + "</p></td>");
    if(wantEntry6)  scoutln(r, bytesOut, "<td valign='top'><p>" + entry6  + "</p></td>");
    if(wantEntry7)  scoutln(r, bytesOut, "<td valign='top'><p>" + entry7  + "</p></td>");
    if(wantEntry8)  scoutln(r, bytesOut, "<td valign='top'><p>" + entry8  + "</p></td>");
    if(wantEntry9)  scoutln(r, bytesOut, "<td valign='top'><p>" + entry9  + "</p></td>");
    if(wantEntry10) scoutln(r, bytesOut, "<td valign='top'><p>" + entry10 + "</p></td>");
      
    scoutln(r, bytesOut, "<td></td></tr>\n");
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(Writer r, int bytesOut[], String str) throws Exception
  {      
    r.write(str + "\n");
    bytesOut[0] += (str.length() + 1);    
  }
  
}
