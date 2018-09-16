// =======================================================================================================================================================================================================
// System: ZaraStar Product: For a Zara-format catalog page - display page
// Module: CatalogDisplayZaraPage.java
// Author: C.K.Harvey
// Copyright (c) 2002-08 Christopher Harvey. All Rights Reserved.
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
import java.net.*;

public class CatalogDisplayZaraPage extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
   CatalogSteelclawsLinkedUser catalogSteelclawsLinkedUser = new CatalogSteelclawsLinkedUser();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    Writer r = null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="", p9="", p10="", p11="", p12="", p13="", p14="", p15="", p16="", p17="", p18="", p19="", p20="", p21="", p22="", p23="", p24="", p25="", p26="", p80="", p81="",
           p82="";

    try
    {
      res.setContentType("text/xml");
      res.setHeader("Cache-Control", "no-cache");
      r = res.getWriter();

      unm = req.getParameter("unm");
      sid = req.getParameter("sid");
      uty = req.getParameter("uty");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");
      p1  = req.getParameter("p1"); // mfr
      p2  = req.getParameter("p2"); // category
      p9  = req.getParameter("p9"); // remote SID
      p10 = req.getParameter("p10"); // catalogURL
      p11 = req.getParameter("p11"); // catalogUpline
      p12 = req.getParameter("p12"); // pricingURL
      p13 = req.getParameter("p13"); // pricingUpline
      p14 = req.getParameter("p14"); // userType 
      p15 = req.getParameter("p15"); // userName
      p16 = req.getParameter("p16"); // passWord
      p17 = req.getParameter("p17"); // userBand
      p18 = req.getParameter("p18"); // markup
      p19 = req.getParameter("p19"); // discount1
      p20 = req.getParameter("p20"); // discount2
      p21 = req.getParameter("p21"); // discount3
      p22 = req.getParameter("p22"); // discount4
      p23 = req.getParameter("p23"); // catalogCurrency
      p24 = req.getParameter("p24"); // priceBasis
      p25 = req.getParameter("p25"); // can see costPrice
      p26 = req.getParameter("p26"); // can see RRP
      p80 = req.getParameter("p80"); // chapter
      p81 = req.getParameter("p81"); // section
      p82 = req.getParameter("p82"); // page
      
      p80 = generalUtils.deSanitise(p80);
      p81 = generalUtils.deSanitise(p81);
      p82 = generalUtils.deSanitise(p82);
      
      if(p25 == null) p25 = "N";
      if(p26 == null) p26 = "N";

      doIt(r, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18, p19, p20, p21, p22, p23, p24, p25, p26, p80, p81, p82, bytesOut);
    }
    catch(Exception e)
    {
      System.out.println("2004d: " + e);
      serverUtils.etotalBytes(req, unm, dnm, 2004, bytesOut[0], 0, "ERR:");
      try {  scoutln(r, bytesOut, "ERR:"); } catch (Exception e2) { } 
      r.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(Writer r, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1, String p2, String p9, String p10, String p11, String p12, String p13, String p14, String p15,
                    String p16, String p17, String p18, String p19, String p20, String p21, String p22, String p23, String p24, String p25, String p26, String p80, String p81, String p82, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String iDir             = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');//, p11);
    String localDefnsDir    = directoryUtils.getLocalOverrideDir(p11);//, p15);
    String catalogImagesDir = directoryUtils.getCatalogImagesDir(p11, p1);

    String uName = directoryUtils.getMySQLUserName();
    String pWord = directoryUtils.getMySQLPassWord();
    
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + p11 + "_ofsa?user=" + uName + "&password=" + pWord);
    Statement stmt = null, stmt2 = null, stmt3 = null;
    ResultSet rs   = null, rs2   = null, rs3   = null;

    if(p11.equals("Catalogs") || p14.equals("R")) // remote server
    {
      if(! serverUtils.checkSID(p15, p9, uty, p11, localDefnsDir, defnsDir))
      {
        serverUtils.etotalBytes(req, p15, p11, 2004, bytesOut[0], 0, "SID:2004d");
        if(con != null) con.close();
        r.flush();
        return;
      }
    }
    else
    {
      if(! serverUtils.checkSID(unm, p9, uty, p11, localDefnsDir, defnsDir))
      {
        serverUtils.etotalBytes(req, unm, p11, 2004, bytesOut[0], 0, "SID:2004d");
        if(con != null) con.close();
        r.flush();
        return;
      }
    }
        
    boolean canSeeCostPrice = false;
    if(p25.equals("Y"))
      canSeeCostPrice = true;
    
    boolean canSeeRRPPrice = false;
    if(p26.equals("Y"))
      canSeeRRPPrice = true;
    
    set(con, stmt, stmt2, stmt3, rs, rs2, rs3, r, req, p1, p80, p81, p82, p2, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18, p19, p20, p21, p22, p23, p24, canSeeCostPrice, canSeeRRPPrice, unm, sid, uty, men, den, dnm, bnm, iDir,
        catalogImagesDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 2004, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1 + ":" + p2);
    if(con  != null) con.close();
    r.flush();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, ResultSet rs3, Writer r,
                   HttpServletRequest req, String mfr, String chapter, String section, String page, String category, String remoteSID,
                   String catalogURL, String catalogUpline, String pricingURL, String pricingUpline, String userType, String userName, String passWord,
                   String userBand, String markup, String discount1, String discount2, String discount3, String discount4, String catalogCurrency,
                   String priceBasis, boolean canSeeCostPrice, boolean canSeeRRPPrice, String unm, String sid, String uty, String men, String den,
                   String dnm, String bnm, String imagesDir, String catalogImagesDir, String localDefnsDir, String defnsDir, int[] bytesOut)
                   throws Exception
  {      

    scoutln(r, bytesOut, "<form><table id='catalogPage'><tr><td>");
    
    catalogSteelclawsLinkedUser.outputOptions(con, stmt, rs, r, mfr, remoteSID, catalogURL, catalogUpline, defnsDir, bytesOut);

    String[] mfrCodes       = new String[1];  mfrCodes[0] = "";
    String[] pricingDetails = new String[1];  pricingDetails[0] = "";

    if(category.length() != 0)
    {
      getPricings(con, stmt, stmt2, stmt3, rs, rs2, rs3, mfr, category, pricingURL, userType, pricingUpline, userName, passWord, unm, uty, mfrCodes,
                  pricingDetails);
      
      forACategory(con, stmt, stmt2, stmt3, rs, rs2, rs3, r, req, mfr, category, mfrCodes[0], pricingDetails[0], generalUtils.strToInt(userBand),
                   generalUtils.doubleFromStr(markup), generalUtils.doubleFromStr(discount1), generalUtils.doubleFromStr(discount2), generalUtils.doubleFromStr(discount3),
                   generalUtils.doubleFromStr(discount4), userType, canSeeCostPrice, canSeeRRPPrice, unm, uty, dnm, men, catalogURL, pricingURL,
                   pricingUpline, remoteSID, catalogCurrency, priceBasis, imagesDir, catalogImagesDir, localDefnsDir, defnsDir, bytesOut);
    }
    else
    {
      getAllPricings(con, stmt, stmt2, stmt3, rs, rs2, rs3, mfr, chapter, section, page, userType, unm, uty, pricingURL, pricingUpline, userName,
                     passWord, mfrCodes, pricingDetails);
      
      forAllACategoriesOnAPage(con, stmt, stmt2, stmt3, rs, rs2, rs3, r, req, mfr, chapter, section, page, mfrCodes[0], pricingDetails[0],
                               generalUtils.strToInt(userBand), generalUtils.doubleFromStr(markup), generalUtils.doubleFromStr(discount1), generalUtils.doubleFromStr(discount2),
                               generalUtils.doubleFromStr(discount3), generalUtils.doubleFromStr(discount4), userType, canSeeCostPrice, canSeeRRPPrice, unm, uty,
                               dnm, men, catalogURL, remoteSID, catalogCurrency, priceBasis, imagesDir, catalogImagesDir,
                               localDefnsDir, defnsDir, bytesOut);
    }
    
    scoutln(r, bytesOut, "</td></tr><tr><td>&#160;</td></tr>");
 
    scoutln(r, bytesOut, "</table></form>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void forACategory(Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, ResultSet rs3, Writer r,
                            HttpServletRequest req, String mfr, String category, String mfrCodes, String pricingDetails, int userBand, double markup,
                            double discount1, double discount2, double discount3, double discount4, String userType, boolean canSeeCostPrice,
                            boolean canSeeRRPPrice, String unm, String uty, String dnm, String men, String catalogURL, String pricingURL,
                            String pricingUpline, String remoteSID, String catalogCurrency, String priceBasis, String imagesDir,
                            String catalogImagesDir, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT ImageSmall, Title, Download, Text, CategoryLink, Text2, ExternalURL, Heading1, "
                         + "Heading2, Heading3, Heading4, Heading5, Heading6, Heading7, Heading8, Heading9, Heading10 FROM catalog WHERE "
                         + "Manufacturer = '" + generalUtils.sanitiseForSQL(mfr) + "' AND Category = '" + category + "'");
    
    String imageSmall, title, download, text, categoryLink, text2, url, heading1, heading2, heading3, heading4, heading5, heading6, heading7,
           heading8, heading9, heading10;

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

      showCategory(con, stmt2, stmt3, rs2, rs3, r, req, mfr, category, imageSmall, title, download, text, categoryLink, text2, url, heading1,
                   heading2, heading3, heading4, heading5, heading6, heading7, heading8, heading9, heading10, mfrCodes, pricingDetails, userBand,
                   markup, discount1, discount2, discount3, discount4, userType, remoteSID, catalogCurrency, priceBasis,
                   canSeeCostPrice, canSeeRRPPrice, unm, uty, dnm, men, catalogURL, imagesDir, catalogImagesDir, localDefnsDir, defnsDir,
                   bytesOut);
    }
   
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void forAllACategoriesOnAPage(Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, ResultSet rs3,
                                        Writer r, HttpServletRequest req, String mfr, String chapter, String section, String page, String mfrCodes,
                                        String pricingDetails, int userBand, double markup, double discount1, double discount2, double discount3,
                                        double discount4, String userType, boolean canSeeCostPrice, boolean canSeeRRPPrice, String unm, String uty,
                                        String dnm, String men, String catalogURL, String remoteSID,
                                        String catalogCurrency, String priceBasis, String imagesDir, String catalogImagesDir, String localDefnsDir,
                                        String defnsDir, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT Category, ImageSmall, Title, Download, Text, CategoryLink, Text2, ExternalURL, Heading1, Heading2, Heading3, "
                         + "Heading4, Heading5, Heading6, Heading7, Heading8, Heading9, Heading10 FROM catalog WHERE Manufacturer = '"
                         + generalUtils.sanitiseForSQL(mfr) + "' AND Chapter = '" + generalUtils.sanitiseForSQL(chapter) + "' AND Section = '"
                         + generalUtils.sanitiseForSQL(section) + "' AND PageName = '" + generalUtils.sanitiseForSQL(page) + "' ORDER BY Title");
    
    boolean first = true;
    String imageSmall, title, download, text, categoryLink, text2, url, heading1, heading2, heading3, heading4, heading5, heading6, heading7,
           heading8, heading9, heading10, category;
    
    while(rs.next())
    {
      category     = rs.getString(1);
      imageSmall   = rs.getString(2);
      title        = rs.getString(3);
      download     = rs.getString(4);
      text         = rs.getString(5);
      categoryLink = rs.getString(6);
      text2        = rs.getString(7);
      url          = rs.getString(8);
      heading1     = rs.getString(9);
      heading2     = rs.getString(10);
      heading3     = rs.getString(11);
      heading4     = rs.getString(12);
      heading5     = rs.getString(13);
      heading6     = rs.getString(14);
      heading7     = rs.getString(15);
      heading8     = rs.getString(16);
      heading9     = rs.getString(17);
      heading10    = rs.getString(18);

      if(category == null) category = "0";
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

      if(! first)
        scoutln(r, bytesOut, "<hr/>");
      else first = false;
        
      showCategory(con, stmt2, stmt3, rs2, rs3, r, req, mfr, category, imageSmall, title, download, text, categoryLink, text2, url, heading1,
                   heading2, heading3, heading4, heading5, heading6, heading7, heading8, heading9, heading10, mfrCodes, pricingDetails, userBand,
                   markup, discount1, discount2, discount3, discount4, userType, remoteSID, catalogCurrency, priceBasis,
                   canSeeCostPrice, canSeeRRPPrice, unm, uty, dnm, men, catalogURL, imagesDir, catalogImagesDir, localDefnsDir, defnsDir,
                   bytesOut);
    }
   
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void showCategory(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, Writer r, HttpServletRequest req,
                            String mfr, String category, String imageSmall, String title, String download, String text, String categoryLink,
                            String text2, String url, String heading1, String heading2, String heading3, String heading4, String heading5,
                            String heading6, String heading7, String heading8, String heading9, String heading10, String mfrCodes,
                            String pricingDetails, int userBand, double markup, double discount1, double discount2, double discount3,
                            double discount4, String userType, String remoteSID, String catalogCurrency,
                            String priceBasis, boolean canSeeCostPrice, boolean canSeeRRPPrice, String unm, String uty, String dnm, String men, 
                            String catalogURL, String imagesDir, String catalogImagesDir, String localDefnsDir, String defnsDir, int[] bytesOut)
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
      scoutln(r, bytesOut, "<tr><td " + rowspan + " valign='top'><p><span id='imageSmall'><img border='1' src=\"" + "http://www.xxx.com" + catalogImagesDir + imageSmall + "\" /></span></p>\n");

      if(dnm.equals("Catalogs") && unm.equals("Sysadmin"))
         scoutln(r, bytesOut, "<p><a href=\"javascript:edit('" + category + "')\">Edit</a></p>\n");
       
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

      if(dnm.equals("Catalogs") && unm.equals("Sysadmin"))
         scoutln(r, bytesOut, "<p><a href=\"javascript:edit('" + category + "')\">Edit</a></p>\n");

      if(! download.equals("0")) // download exists
        scoutln(r, bytesOut, "<tr><td><p><a href=\"javascript:download('" + download + "')\">Download</a> more info</p></td></tr>\n");
      if(url.length() > 0)
        scoutln(r, bytesOut, "<tr><td colspan='2'><p><a href=\"http://" + url + "\">Link</a> to Manufacturer</p></td></tr>\n");
        
      if(! categoryLink.equals("0"))
        scoutln(r, bytesOut, "<tr><td colspan='2'><p><a href=\"javascript:display('" + categoryLink + "')\">Display</a> related items</p></td></tr>\n");
      scoutln(r, bytesOut, "<tr><td align='center'>");
    }
    
    scoutln(r, bytesOut, "<table id='catalogTable'><tr>");

    if(userType.equals("R")) // call from remote server
      ; // no ItemCode
    else scoutln(r, bytesOut, "<th valign='center'>Our Code</th>\n");    
    
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
    
    if(userBand == 888)
    {
      if(userType.equals("R")) // call from remote server
      {
        if(canSeeCostPrice) 
          scoutln(r, bytesOut, "<th align='right'>Cost Price</th>");
        if(canSeeRRPPrice)
          scoutln(r, bytesOut, "<th align='right'>RRP</th>");
      }
      scoutln(r, bytesOut, "<th align='right'>Our Sell Prices</th>");
    }
    else
    if(userBand == 0)
      scoutln(r, bytesOut, "<th align='right'>List Price</th>");
    else scoutln(r, bytesOut, "<th align='right'>Your Price</th>");

    scoutln(r, bytesOut, "<th>Cart</th></tr>\n");

    showLines(con, stmt, stmt2, rs, rs2, r, req, mfr, category, mfrCodes, pricingDetails, wantEntry1, wantEntry2, wantEntry3, wantEntry4, wantEntry5, wantEntry6, wantEntry7, wantEntry8, wantEntry9, wantEntry10, userBand, markup, discount1,
              discount2, discount3, discount4, userType, remoteSID, catalogCurrency, priceBasis, canSeeCostPrice, canSeeRRPPrice, unm, uty, dnm, imagesDir, localDefnsDir, defnsDir, bytesOut);

    scoutln(r, bytesOut, "<tr><td valign='top' colspan='12'><p>" + generalUtils.handleSuperScripts(text2) + "</p></td></tr>");
    
    scoutln(r, bytesOut, "</table>");

    scoutln(r, bytesOut, "</td></tr></table>\n");

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void showLines(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, Writer r, HttpServletRequest req, String mfr,
                         String category, String mfrCodes, String pricingDetails, boolean wantEntry1, boolean wantEntry2, boolean wantEntry3,
                         boolean wantEntry4, boolean wantEntry5, boolean wantEntry6, boolean wantEntry7, boolean wantEntry8, boolean wantEntry9,
                         boolean wantEntry10, int userBand, double markup, double discount1, double discount2, double discount3, double discount4,
                         String userType, String remoteSID, String catalogCurrency, String priceBasis,
                         boolean canSeeCostPrice, boolean canSeeRRPPrice, String unm, String uty, String dnm, String imagesDir,
                         String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
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

      showLine(r, req, mfr, category, mfrCode, mfrCodes, pricingDetails, wantEntry1, wantEntry2, wantEntry3, wantEntry4, wantEntry5, wantEntry6, wantEntry7, wantEntry8, wantEntry9, wantEntry10, entry1, entry2, entry3, entry4, entry5, entry6,
               entry7, entry8, entry9, entry10, desc, desc2, userBand, markup, discount1, discount2, discount3, discount4, userType, remoteSID, catalogCurrency, priceBasis, canSeeCostPrice, canSeeRRPPrice, uty, imagesDir, bytesOut);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void showLine(Writer r, HttpServletRequest req, String mfr, String category, String mfrCode,
                        String mfrCodes, String pricingDetails, boolean wantEntry1, boolean wantEntry2, boolean wantEntry3, boolean wantEntry4,
                        boolean wantEntry5, boolean wantEntry6, boolean wantEntry7, boolean wantEntry8, boolean wantEntry9, boolean wantEntry10,
                        String entry1, String entry2, String entry3, String entry4, String entry5, String entry6, String entry7, String entry8,
                        String entry9, String entry10, String desc, String desc2, int userBand, double markup, double discount1, double discount2,
                        double discount3, double discount4, String userType, String remoteSID, String catalogCurrency, String priceBasis,
                        boolean canSeeCostPrice, boolean canSeeRRPPrice, String uty, String imagesDir, int[] bytesOut) throws Exception
  {
    String[] itemCode      = new String[1];
    String[] salesCurrency = new String[1];
    String[] suppliersBand = new String[1];
    String[] sellPrices    = new String[1];
    double[] rrp           = new double[1];
    double[] list          = new double[1];
    double[] costPrice     = new double[1];
    double[] priceForCart  = new double[1];
    String itemCodeForCart;
    
    if(calcPrices(mfrCodes, pricingDetails, mfrCode, userType, userBand, catalogCurrency, priceBasis, markup, discount1, discount2, discount3, discount4, uty, itemCode, salesCurrency, suppliersBand, rrp, list, priceForCart, costPrice,
                  sellPrices))
    {
      scoutln(r, bytesOut, "<tr>");
      if(userType.equals("R")) // call from remote server
        itemCodeForCart = "-"; // noItemCode
      else
      {
        scoutln(r, bytesOut, "<td valign='top'><a href=\"javascript:view('" + itemCode[0] + "')\">" + itemCode[0] + "</a></td>\n");
        
        itemCodeForCart = itemCode[0];
      }

      if(userType.equals("R")) // call from remote server
      {
        scoutln(r, bytesOut, "<td valign='top'><a href=\"javascript:s2000('" + generalUtils.sanitise2(mfrCode) + "','" + itemCode[0] + "','" + category
                           + "','" + catalogCurrency + "','" + priceForCart[0] + "','" + remoteSID + "')\">" + mfrCode + "</a></td>\n");
      }
      else scoutln(r, bytesOut, "<td valign='top'><p>" + mfrCode + "</p></td>");
      
      scoutln(r, bytesOut, "<td valign='top'><p>" + generalUtils.handleSuperScripts(desc));
      if(desc2.length() > 0)
        scoutln(r, bytesOut, "<br />" + generalUtils.handleSuperScripts(desc2));
      scoutln(r, bytesOut, "</p></td>\n");
      
      if(wantEntry1)  scoutln(r, bytesOut, "<td valign='top'><p>" + generalUtils.handleSuperScripts(entry1)  + "</p></td>");
      if(wantEntry2)  scoutln(r, bytesOut, "<td valign='top'><p>" + generalUtils.handleSuperScripts(entry2)  + "</p></td>");
      if(wantEntry3)  scoutln(r, bytesOut, "<td valign='top'><p>" + generalUtils.handleSuperScripts(entry3)  + "</p></td>");
      if(wantEntry4)  scoutln(r, bytesOut, "<td valign='top'><p>" + generalUtils.handleSuperScripts(entry4)  + "</p></td>");
      if(wantEntry5)  scoutln(r, bytesOut, "<td valign='top'><p>" + generalUtils.handleSuperScripts(entry5)  + "</p></td>");
      if(wantEntry6)  scoutln(r, bytesOut, "<td valign='top'><p>" + generalUtils.handleSuperScripts(entry6)  + "</p></td>");
      if(wantEntry7)  scoutln(r, bytesOut, "<td valign='top'><p>" + generalUtils.handleSuperScripts(entry7)  + "</p></td>");
      if(wantEntry8)  scoutln(r, bytesOut, "<td valign='top'><p>" + generalUtils.handleSuperScripts(entry8)  + "</p></td>");
      if(wantEntry9)  scoutln(r, bytesOut, "<td valign='top'><p>" + generalUtils.handleSuperScripts(entry9)  + "</p></td>");
      if(wantEntry10) scoutln(r, bytesOut, "<td valign='top'><p>" + generalUtils.handleSuperScripts(entry10) + "</p></td>\n");
      
      if(userType.equals("R")) // call from remote server
      {
        if(uty.equals("I")) // internal on the remote server that is contacting this server
        {
          if(canSeeCostPrice) 
          {
            if(costPrice[0] == 0.0)
              scoutln(r, bytesOut, "<td></td>\n");
            else
            {
              scoutln(r, bytesOut, "<td nowrap='nowrap' valign='top' align='right'><p>" + catalogCurrency + " " + generalUtils.doubleDPs('2', costPrice[0])
                                 + "</p></td>\n");
            }
          }
          
          if(canSeeRRPPrice) 
          {
            if(rrp[0] == 0.0)
              scoutln(r, bytesOut, "<td></td>\n");
            else
            {
              scoutln(r, bytesOut, "<td nowrap='nowrap' valign='top' align='right'><p>" + salesCurrency[0] + " ");
              scoutln(r, bytesOut, generalUtils.doubleDPs('2', list[0]) + "</p></td>\n");
            }
          }
          
          scoutln(r, bytesOut, "<td nowrap='nowrap' valign='top' align='right'><p>" + sellPrices[0] + "</p></td>\n");
        }
        else // registered or anon user on remote server
        {
          scoutln(r, bytesOut, "<td nowrap='nowrap' valign='top' align='right'><p>" + sellPrices[0] + "</p></td>\n");
        }
      }
      else // call from local server
      {
        scoutln(r, bytesOut, "<td nowrap='nowrap' valign='top' align='right'><p>" + sellPrices[0] + "</p></td>\n");
      }
      
     scoutln(r, bytesOut, "<td align='center'><a href=\"javascript:addToCart('" + itemCodeForCart + "','" + priceForCart[0] + "','" + catalogCurrency
                           + "','" + generalUtils.sanitise2(mfrCode) + "','Each','" + generalUtils.sanitise2(desc + " " + desc2) + "','" + remoteSID
                           + "')\"><img border='0' src=\"" + imagesDir + "cart.png\" /></a></td></tr>\n");
    }
    else // not on stock DB
    {
      scoutln(r, bytesOut, "<tr>");

      if(userType.equals("R")) // call from remote server
        ;
      else scoutln(r, bytesOut, "<td></td>");

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
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(Writer r, int bytesOut[], String str) throws Exception
  {      
    r.write(str + "\n");
    bytesOut[0] += (str.length() + 1);    
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void getPricings(Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, ResultSet rs3, String mfr, String category, String pricingURL, String userType, String pricingUpline, String userName,
                           String passWord, String unm, String uty, String[] mfrCodes, String[] pricingDetails) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT Category FROM catalog WHERE Manufacturer = '" + generalUtils.sanitiseForSQL(mfr) + "' AND Category = '" + category + "'");
    
    while(rs.next())
    {
      addMfrCodeForACategory(con, stmt2, stmt3, rs2, rs3, mfr, rs.getString(1), mfrCodes);
    }
   
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    // call pricing/availability server (may be remote)
    // res: ItemCode \001 RRP \001 SellPrice1 \001 SellPrice2 \001 SellPrice3 \001 SellPrice4 \001 SalesCurrency

    getPricingFromPricingServer(unm, uty, mfr, pricingURL, mfrCodes[0], pricingUpline, userType, userName, passWord, pricingDetails);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void getAllPricings(Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, ResultSet rs3, String mfr, String chapter, String section, String page, String userType, String unm, String uty,
                              String pricingURL, String pricingUpline, String userName, String passWord, String[] mfrCodes, String[] pricingDetails) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT Category FROM catalog WHERE Manufacturer = '" + generalUtils.sanitiseForSQL(mfr) + "' AND Chapter = '" + generalUtils.sanitiseForSQL(chapter) + "' AND Section = '" + generalUtils.sanitiseForSQL(section) + "' AND PageName = '"
                         + generalUtils.sanitiseForSQL(page) + "' ORDER BY Title");
    
    while(rs.next())
    {
      addMfrCodeForACategory(con, stmt2, stmt3, rs2, rs3, mfr, rs.getString(1), mfrCodes);
    }
   
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    // call pricing/availability server (may be remote)
    // res: ItemCode \001 RRP \001 SellPrice1 \001 SellPrice2 \001 SellPrice3 \001 SellPrice4 \001 SalesCurrency

    getPricingFromPricingServer(unm, uty, mfr, pricingURL, mfrCodes[0], pricingUpline, userType, userName, passWord, pricingDetails);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void addMfrCodeForACategory(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String mfr, String category, String[] mfrCodes) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT ManufacturerCode FROM catalogl WHERE Manufacturer = '" + generalUtils.sanitiseForSQL(mfr) + "' AND Category = '" + category + "' ORDER BY Line");
    
    String mfrCode, synonyms;
    
    while(rs.next())
    {
      mfrCode = rs.getString(1);
      synonyms = checkForAnySynonyms(con, stmt2, rs2, mfr, mfrCode);
      mfrCodes[0] += (mfrCode + "\002" + synonyms + "\001");
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String checkForAnySynonyms(Connection con, Statement stmt, ResultSet rs, String mfr, String mfrCode) throws Exception
  {
    String synonyms = "";
    
    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT AlternativeManufacturerCode FROM catalogs WHERE Manufacturer = '" + generalUtils.sanitiseForSQL(mfr) + "' AND CatalogManufacturerCode = '" + generalUtils.sanitiseForSQL(mfrCode) + "'");
    
      while(rs.next())
        synonyms += (rs.getString(1) + "\002");
    }
    catch(Exception e) { }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return synonyms;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // res: ItemCode \001 RRP \001 SellPrice1 \001 SellPrice2 \001 SellPrice3 \001 SellPrice4 \001 SalesCurrency
  public void getPricingFromPricingServer(String unm, String uty, String mfr, String pricingURL, String mfrCodes, String pricingUpline, String userType, String userName, String passWord, String[] pricingDetails) throws Exception
  { 
    if(! pricingURL.startsWith("http://"))
        pricingURL = "http://" + pricingURL;

    URL url = new URL(pricingURL + "/central/servlet/CatalogDisplayZaraPagePricing");

    HttpURLConnection uc = (HttpURLConnection)url.openConnection();
    uc.setDoInput(true);
    uc.setDoOutput(true);
    uc.setUseCaches(false);
    uc.setDefaultUseCaches(false);
    uc.setRequestProperty("Content-Type", "application/x-www-form.urlencoded");

    String s2 = "&unm=" + unm + "&uty=" + uty + "&p1=" + mfr + "&p2=" + mfrCodes + "&p13=" + pricingUpline + "&p14=" + userType + "&p15=" + userName + "&p16=" + passWord;

    Integer len = new Integer(s2.length());
    uc.setRequestProperty("Content-Length", len.toString());
    
    uc.setRequestMethod("POST");

    PrintWriter p = new PrintWriter(uc.getOutputStream());
    p.print(s2);    
    p.flush();
    p.close();

    pricingDetails[0] = "";
    
    BufferedReader di = new BufferedReader(new InputStreamReader(uc.getInputStream()));
    String s = di.readLine();
    while(s != null)
    {
      pricingDetails[0] += s;
      s = di.readLine();
    }

    di.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // ItemCode \001 RRP \001 SellPrice1 \001 SellPrice2 \001 SellPrice3 \001 SellPrice4 \001 SalesCurrency \001 SuppliersBand \001 ...
  private boolean getPricingDetailsForMfrAndMfrCode(String mfrCodes, String pricingDetails, String mfrCode, String[] itemCode, String[] salesCurrency, double[] rrp, double[] sellPrice1, double[] sellPrice2, double[] sellPrice3,
                                                    double[] sellPrice4, String[] suppliersBand) throws Exception
  {
    String thisMfrCode;
    boolean found = false;
    int x = 0, len = mfrCodes.length(), posn = 0;

    while(! found && x < len)
    {
      thisMfrCode = "";
      while(x < len && mfrCodes.charAt(x) != '\002') // just-in-case
        thisMfrCode += mfrCodes.charAt(x++);

      if(thisMfrCode.equals(mfrCode))
        found = true;
      else
      {
        while(x < len && mfrCodes.charAt(x) != '\001') // just-in-case
          ++x;
        ++x;
        ++posn;
      }
    }

    if(! found) // just-in-case
      return false;
    
    len = pricingDetails.length();
    x = 0;
    int y, entriesCount = 0;
    
    while(x < len && entriesCount < posn)
    {
      for(y=0;y<8;++y)
      {
        while(x < len && pricingDetails.charAt(x) != '\001') // just-in-case
          ++x;
        ++x;
      }
      
      ++entriesCount;
    }

    if(x >= len) // just-in-case
      return false;
    
    itemCode[0] = "";
    while(x < len && pricingDetails.charAt(x) != '\001') // just-in-case
      itemCode[0] += pricingDetails.charAt(x++);
    ++x;

    salesCurrency[0] = "";
    while(x < len && pricingDetails.charAt(x) != '\001') // just-in-case
       salesCurrency[0] += pricingDetails.charAt(x++);
    ++x;

    String s = "";
    while(x < len && pricingDetails.charAt(x) != '\001') // just-in-case
      s += pricingDetails.charAt(x++);
    ++x;
    rrp[0] = generalUtils.doubleFromStr(s); 

    s = "";
    while(x < len && pricingDetails.charAt(x) != '\001') // just-in-case
      s += pricingDetails.charAt(x++);
    ++x;
    sellPrice1[0] = generalUtils.doubleFromStr(s); 

    s = "";
    while(x < len && pricingDetails.charAt(x) != '\001') // just-in-case
      s += pricingDetails.charAt(x++);
    ++x;
    sellPrice2[0] = generalUtils.doubleFromStr(s); 

    s = "";
    while(x < len && pricingDetails.charAt(x) != '\001') // just-in-case
      s += pricingDetails.charAt(x++);
    ++x;
    sellPrice3[0] = generalUtils.doubleFromStr(s); 

    s = "";
    while(x < len && pricingDetails.charAt(x) != '\001') // just-in-case
      s += pricingDetails.charAt(x++);
    ++x;
    sellPrice4[0] = generalUtils.doubleFromStr(s); 

    suppliersBand[0] = "";
    while(x < len && pricingDetails.charAt(x) != '\001') // just-in-case
      suppliersBand[0] += pricingDetails.charAt(x++);
    ++x;

   return true;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean calcPrices(String mfrCodes, String pricingDetails, String mfrCode, String userType, int userBand, String catalogCurrency, String priceBasis, double markup, double discount1, double discount2, double discount3, double discount4,
                             String uty, String[] itemCode, String[] salesCurrency, String[] suppliersBand, double[] rrp, double[] list, double[] priceForCart, double[] costPrice, String[] sellPrices) throws Exception
  {
    double[] sellPrice1    = new double[1];
    double[] sellPrice2    = new double[1];
    double[] sellPrice3    = new double[1];
    double[] sellPrice4    = new double[1];

    if(getPricingDetailsForMfrAndMfrCode(mfrCodes, pricingDetails, mfrCode, itemCode, salesCurrency, rrp, sellPrice1, sellPrice2, sellPrice3, sellPrice4, suppliersBand))
    {
      double price;
   
      markup /= 100;

      if(userType.equals("R")) // call from remote server
      {
        list[0] = rrp[0];
        double sp1  = sellPrice1[0];
        double sp2  = sellPrice2[0];
        double sp3  = sellPrice3[0];
        double sp4  = sellPrice4[0];
        
        if(! priceBasis.equals("L"))
        {
               if(suppliersBand[0].equals("1")) rrp[0] = sp2;
          else if(suppliersBand[0].equals("2")) rrp[0] = sp2;
          else if(suppliersBand[0].equals("3")) rrp[0] = sp3;
          else if(suppliersBand[0].equals("4")) rrp[0] = sp4;
          else                                  rrp[0] = list[0];
        }

        rrp[0] *= markup;
        sellPrice1[0]  = rrp[0] - (rrp[0] * (discount1 / 100));
        sellPrice2[0]  = rrp[0] - (rrp[0] * (discount2 / 100));
        sellPrice3[0]  = rrp[0] - (rrp[0] * (discount3 / 100));
        sellPrice4[0]  = rrp[0] - (rrp[0] * (discount4 / 100));

        if(uty.equals("I")) // internal on the remote server that is contacting this server
        {
          // 
          {
            if(priceBasis.equals("L"))
              costPrice[0] = list[0] * markup;
            else
            {
                   if(suppliersBand[0].equals("1")) costPrice[0] = sp1;
              else if(suppliersBand[0].equals("2")) costPrice[0] = sp2;
              else if(suppliersBand[0].equals("3")) costPrice[0] = sp3;
              else if(suppliersBand[0].equals("4")) costPrice[0] = sp4;
              else                                  costPrice[0] = list[0];
            }
          }
          
          if(rrp[0] == 0.0 && sellPrice1[0] == 0.0 && sellPrice2[0] == 0.0 && sellPrice3[0] == 0.0 && sellPrice4[0] == 0.0)
            sellPrices[0] = "";
          else
          {
            sellPrices[0] = catalogCurrency + " " + generalUtils.doubleDPs('2', rrp[0]) + " " + generalUtils.doubleDPs('2', sellPrice1[0]) + " " + generalUtils.doubleDPs('2', sellPrice2[0]) + " " + generalUtils.doubleDPs('2', sellPrice3[0]) + " "
                          + generalUtils.doubleDPs('2', sellPrice4[0]);
          }
          
          priceForCart[0] = rrp[0];
        }
        else
        if(uty.equals("R")) // registered user on remote server
        {
          switch(userBand)
          {
            case 1  : price = sellPrice1[0]; priceForCart[0] = price;  break;
            case 2  : price = sellPrice2[0]; priceForCart[0] = price;  break;
            case 3  : price = sellPrice3[0]; priceForCart[0] = price;  break;
            case 4  : price = sellPrice4[0]; priceForCart[0] = price;  break;
            default : price = rrp[0];        priceForCart[0] = rrp[0]; break; // just-in-case
          }
  
          if(price == 0.0)
            sellPrices[0] = "";
          else sellPrices[0] = catalogCurrency + " " + generalUtils.doubleDPs('2', price);
        }
        else // anon
        {
          if(rrp[0] == 0.0)
            sellPrices[0] = "";
          else sellPrices[0] = catalogCurrency + " " + generalUtils.doubleDPs('2', rrp[0]);
          priceForCart[0] = rrp[0];
        }
      }
      else // call from local server
      {
        sellPrices[0] = catalogCurrency + " ";
        if(uty.equals("I")) // internal
        {
          if(rrp[0] == 0.0 && sellPrice1[0] == 0.0 && sellPrice2[0] == 0.0 && sellPrice3[0] == 0.0 && sellPrice4[0] == 0.0)
            sellPrices[0] = "";
          else sellPrices[0] += generalUtils.doubleDPs('2', rrp[0]) + " " + generalUtils.doubleDPs('2', sellPrice1[0]) + " " + generalUtils.doubleDPs('2', sellPrice2[0]) + " " + generalUtils.doubleDPs('2', sellPrice3[0]) + " " + generalUtils.doubleDPs('2', sellPrice4[0]);
          
          priceForCart[0] = rrp[0]; 
        }
        else
        if(uty.equals("R")) // registered
        {
          switch(userBand)
          {
            case 1  : price = sellPrice1[0]; priceForCart[0] = price;  break;
            case 2  : price = sellPrice2[0]; priceForCart[0] = price;  break;
            case 3  : price = sellPrice3[0]; priceForCart[0] = price;  break;
            case 4  : price = sellPrice4[0]; priceForCart[0] = price;  break;
            default : price = rrp[0];        priceForCart[0] = rrp[0]; break; // just-in-case
          }
  
          if(price == 0.0)
            sellPrices[0] = "";
          else sellPrices[0] += " " + generalUtils.doubleDPs('2', price);
        }
        else // anon
        {
          if(rrp[0] == 0.0)
            sellPrices[0] = "";
          else sellPrices[0] += " " + generalUtils.doubleDPs('2', rrp[0]);
          priceForCart[0] = rrp[0];
        }
      }
      return true;
    }
    
    return false;
  }
  
}

