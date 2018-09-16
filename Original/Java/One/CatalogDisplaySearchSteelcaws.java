// =======================================================================================================================================================================================================
// System: ZaraStar Catalog: Display search results, for a SC catalog
// Module: CatalogDisplaySearchSteelcaws.java
// Author: C.K.Harvey
// Copyright (c) 2002-08 Christopher Harvey. All Rights Reserved.
// Remark: Catalog server
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

public class CatalogDisplaySearchSteelcaws extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  CatalogDisplayZaraPage catalogDisplayZaraPage = new CatalogDisplayZaraPage();
  
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    Writer r = null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p9="", p10="", p11="", p12="", p13="", p14="", p15="", p16="", p17="",
           p18="", p19="", p20="", p21="", p22="", p23="", p24="", p25="", p26="", p60="";

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
      p1  = req.getParameter("p1");  // mfr
      p9  = req.getParameter("p9");  // remoteSID
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

      p60 = req.getParameter("p60"); // categories

      if(p25 == null) p25 = "N";
      if(p26 == null) p26 = "N";

      doIt(r, req, unm, uty, sid, men, den, dnm, bnm, p1, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18, p19, p20, p21, p22, p23, p24, p25, p26,
           p60, bytesOut);
    }
    catch(Exception e)
    {
      serverUtils.etotalBytes(req, unm, dnm, 2003, bytesOut[0], 0, "ERR:");
      try {  scoutln(r, bytesOut, "ERR:2003u "           +       e         ); } catch (Exception e2) { } 
      r.flush();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(Writer r, HttpServletRequest req, String unm, String uty, String sid, String men, String den, String dnm, String bnm,
                    String p1, String p9, String p10, String p11, String p12, String p13, String p14, String p15, String p16, String p17, String p18,
                    String p19, String p20, String p21, String p22, String p23, String p24, String p25, String p26, String p60, int[] bytesOut)
                    throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(p11);
    String catalogImagesDir = directoryUtils.getCatalogImagesDir(p11, p1);
    
    String imagesDir     = directoryUtils.getSupportDirs('I');    
    

    String userName = directoryUtils.getMySQLUserName();
    String passWord = directoryUtils.getMySQLPassWord();
    
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + p11 + "_ofsa?user=" + userName + "&password=" + passWord);
    Statement stmt = null, stmt2 = null, stmt3 = null;
    ResultSet rs   = null, rs2   = null, rs3   = null;
    
    if(p11.equals("Catalogs") || p14.equals("R")) // remote server
    {
      if(! serverUtils.checkSID(p15, p9, uty, p11, localDefnsDir, defnsDir))
      {
        serverUtils.etotalBytes(req, p15, p11, 2003, bytesOut[0], 0, "SID:2003u");
        if(con  != null) con.close();
        r.flush();
        return;
      }
    }
    else
    {
      if(! serverUtils.checkSID(unm, p9, uty, p11, localDefnsDir, defnsDir))
      {
        serverUtils.etotalBytes(req, p15, p11, 2004, bytesOut[0], 0, "SID:2003u");
        if(con  != null) con.close();
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
    
    scoutln(r, bytesOut, "<tr><td><table id='catalogTable'>");

    String[] mfrCodes       = new String[1];  mfrCodes[0] = "";
    String[] pricingDetails = new String[1];  pricingDetails[0] = "";

    getPricings(con, stmt, stmt2, stmt3, rs, rs2, rs3, p1, p60, p12, p13, p14, p15, p16, unm, uty, mfrCodes, pricingDetails);

    generate(con, stmt, stmt2, stmt3, rs, rs2, rs3, req, r, p60, p1, p9, p10, p12, p13, p14, p17, p18, p19, p20, p21, p22, mfrCodes[0],
             pricingDetails[0], p23, p24, canSeeCostPrice, canSeeRRPPrice, unm, uty, men, dnm, imagesDir, defnsDir, localDefnsDir,
             catalogImagesDir, bytesOut);
    
    scoutln(r, bytesOut, "<tr><td>&nbsp;</td></tr>");
  
    scoutln(r, bytesOut, "</table></td></tr>");

    serverUtils.totalBytes(con, stmt, rs, req, userName, p11, 2003, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con != null) con.close();
    r.flush();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void generate(Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, ResultSet rs3,
                        HttpServletRequest req, Writer r, String categories, String mfr, String remoteSID, String catalogURL, String pricingURL,
                        String pricingUpline, String userType, String userBand, String markup, String discount1, String discount2, String discount3,
                        String discount4, String mfrCodes, String pricingDetails, String catalogCurrency, String priceBasis, boolean canSeeCostPrice,
                        boolean canSeeRRPPrice, String unm, String uty, String men, String dnm, String imagesDir, String defnsDir,
                        String localDefnsDir, String catalogImagesDir, int[] bytesOut) throws Exception
  {
    int x=0, len=categories.length();
    String category;
    while(x < len)
    {
      category = "";
      while(x < len && categories.charAt(x) != '\001') // just-in-case
        category += categories.charAt(x++);

      writeBodyLine(con, stmt, stmt2, stmt3, rs, rs2, rs3, req, r, category, mfr, remoteSID, catalogURL, pricingURL, pricingUpline, userType, 
                    userBand, markup, discount1, discount2, discount3, discount4, mfrCodes, pricingDetails, catalogCurrency, priceBasis,
                    canSeeCostPrice, canSeeRRPPrice, unm, uty, men, dnm, imagesDir, defnsDir, localDefnsDir, catalogImagesDir, bytesOut);
      ++x;
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void writeBodyLine(Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, ResultSet rs3,
                             HttpServletRequest req, Writer r, String category, String mfr, String remoteSID, String catalogURL, String pricingURL,
                             String pricingUpline, String userType, String userBand, String markup, String discount1, String discount2,
                             String discount3, String discount4, String mfrCodes, String pricingDetails, String catalogCurrency, String priceBasis,
                             boolean canSeeCostPrice, boolean canSeeRRPPrice, String unm, String uty, String men, String dnm, String imagesDir,
                             String defnsDir, String localDefnsDir, String catalogImagesDir, int[] bytesOut) throws Exception
  {
    scoutln(r, bytesOut, "<tr><td>");

    catalogDisplayZaraPage.forACategory(con, stmt, stmt2, stmt3, rs, rs2, rs3, r, req, mfr, category, mfrCodes, pricingDetails, generalUtils.strToInt(userBand),
                        generalUtils.doubleFromStr(markup), generalUtils.doubleFromStr(discount1), generalUtils.doubleFromStr(discount2), generalUtils.doubleFromStr(discount3),
                        generalUtils.doubleFromStr(discount4), userType, canSeeCostPrice, canSeeRRPPrice, unm, uty, dnm, men, catalogURL, pricingURL,
                        pricingUpline, remoteSID, catalogCurrency, priceBasis, imagesDir, catalogImagesDir, localDefnsDir, defnsDir, bytesOut);
    
    scoutln(r, bytesOut, "</td></tr>");
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void getPricings(Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, ResultSet rs3, String mfr,
                           String categories, String pricingURL, String pricingUpline, String userType, String userName, String passWord, String unm,
                           String uty, String[] mfrCodes, String[] pricingDetails) throws Exception
  {
    int x=0, len=categories.length();
    String category;
    while(x < len)
    {
      category = "";
      while(x < len && categories.charAt(x) != '\001') // just-in-case
        category += categories.charAt(x++);

      setForACategory(con, stmt, stmt2, stmt3, rs, rs2, rs3, mfr, category, mfrCodes);
      
      ++x;
    }
    
    // call pricing/availability server (may be remote)
    // res: ItemCode \001 RRP \001 SellPrice1 \001 SellPrice2 \001 SellPrice3 \001 SellPrice4 \001 SalesCurrency

    catalogDisplayZaraPage.getPricingFromPricingServer(unm, uty, mfr, pricingURL, mfrCodes[0], pricingUpline, userType, userName, passWord, pricingDetails);
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void setForACategory(Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, ResultSet rs3,
                               String mfr, String category, String[] mfrCodes) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT Category FROM catalog WHERE Manufacturer = '" + generalUtils.sanitiseForSQL(mfr) + "' AND Category = '" + category + "'");
    
    while(rs.next())
    {
      addMfrCodeForACategory(con, stmt2, stmt3, rs2, rs3, mfr, rs.getString(1), mfrCodes);
    }
   
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void addMfrCodeForACategory(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String mfr, String category,
                                      String[] mfrCodes) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT ManufacturerCode FROM catalogl WHERE Manufacturer = '" + generalUtils.sanitiseForSQL(mfr) + "' AND Category = '"
                           + category + "' ORDER BY Line");
    
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

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private String checkForAnySynonyms(Connection con, Statement stmt, ResultSet rs, String mfr, String mfrCode) throws Exception
  {
    String synonyms = "";
    
    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT AlternativeManufacturerCode FROM catalogs WHERE Manufacturer = '" + generalUtils.sanitiseForSQL(mfr)
                           + "' AND CatalogManufacturerCode = '" + generalUtils.sanitiseForSQL(mfrCode) + "'");
    
      while(rs.next())
        synonyms += (rs.getString(1) + "\002");
    }
    catch(Exception e) { }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return synonyms;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(Writer r, int bytesOut[], String str) throws Exception
  {      
    r.write(str + "\n");
    bytesOut[0] += (str.length() + 1);    
  }
  
}
