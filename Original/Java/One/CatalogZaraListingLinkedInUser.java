// =======================================================================================================================================================================================================
// System: ZaraStar Catalog: For an internal listing, linked-in user
// Module: CatalogZaraListingLinkedInUser.java
// Author: C.K.Harvey
// Copyright (c) 2002-08 Christopher Harvey. All Rights Reserved.
// Remark: On called server
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

public class CatalogZaraListingLinkedInUser extends HttpServlet
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

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", operation="", searchType="", srchStr="", firstRecNum="", lastRecNum="",
           firstCodeOnPage="", lastCodeOnPage="", maxRows="", numRecs="", mfr="", userName="", catalogURL="", band="", markup="", discount1="",
           discount2="", discount3="", discount4="", catalogUpline="", remoteSID="", userType="", pricingURL="", pricingUpline="", catalogCurrency="",
           priceBasis="", p25="", p26="";

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
      mfr         = req.getParameter("p31");
      operation   = req.getParameter("p32");
      searchType  = req.getParameter("p40");
      
      remoteSID     = req.getParameter("p9");
      catalogURL    = req.getParameter("p10");
      catalogUpline = req.getParameter("p11");
      pricingURL    = req.getParameter("p12");
      pricingUpline = req.getParameter("p13");
      userType      = req.getParameter("p14");
      userName      = req.getParameter("p15");
      band          = req.getParameter("p17");
      markup        = req.getParameter("p18");
      discount1     = req.getParameter("p19");
      discount2     = req.getParameter("p20");
      discount3     = req.getParameter("p21");
      discount4     = req.getParameter("p22");
      catalogCurrency = req.getParameter("p23");
      priceBasis    = req.getParameter("p24");
      p25           = req.getParameter("p25");
      p26           = req.getParameter("p26");

      srchStr     = req.getParameter("p33");
      if(srchStr == null) srchStr = "";
      srchStr     = new String(srchStr.getBytes("ISO-8859-1"), "UTF-8");

      firstRecNum = req.getParameter("p34");
      lastRecNum  = req.getParameter("p35");

      maxRows = req.getParameter("p36");
      if(maxRows == null || maxRows.length() == 0) maxRows = "50";

      numRecs    = req.getParameter("p37");
      if(numRecs == null || numRecs.length() == 0) numRecs = "-1";

      firstCodeOnPage = req.getParameter("p38");
      if(firstCodeOnPage == null) firstCodeOnPage = "";
      firstCodeOnPage    = new String(firstCodeOnPage.getBytes("ISO-8859-1"), "UTF-8");

      lastCodeOnPage = req.getParameter("p39");
      if(lastCodeOnPage == null) lastCodeOnPage = "";
      lastCodeOnPage    = new String(lastCodeOnPage.getBytes("ISO-8859-1"), "UTF-8");
      
      doIt(r, req, unm, sid, uty, men, den, dnm, bnm, mfr, operation, searchType, srchStr, firstRecNum, lastRecNum, firstCodeOnPage, lastCodeOnPage,
           generalUtils.strToInt(maxRows), generalUtils.strToInt(numRecs), userName, catalogURL, band, markup, discount1, discount2, discount3, discount4,
           catalogUpline, remoteSID, userType, pricingURL, pricingUpline, catalogCurrency, priceBasis, p25, p26, bytesOut);
    }
    catch(Exception e)
    {
      serverUtils.etotalBytes(req, unm, catalogURL, 2005, bytesOut[0], 0, "ERR:");
      try {  scoutln(r, bytesOut, "ERR:2005h " + e); } catch (Exception e2) { } 
      r.flush();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(Writer r, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                    String mfr, String operation, String searchType, String srchStr, String firstRecNum, String lastRecNum, String firstCodeOnPage,
                    String lastCodeOnPage, int maxRows, int numRecs, String userName, String catalogURL, String band, String markup, String discount1,
                    String discount2, String discount3, String discount4, String catalogUpline, String remoteSID, String userType, String pricingURL,
                    String pricingUpline, String catalogCurrency, String priceBasis, String p25, String p26, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');//, catalogUpline);
    String localDefnsDir = directoryUtils.getLocalOverrideDir(catalogUpline);//, unm);
    String imagesDir     = directoryUtils.getSupportDirs('I');
    
    String uName = directoryUtils.getMySQLUserName();
    String pWord = directoryUtils.getMySQLPassWord();
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + catalogUpline + "_ofsa?user=" + uName + "&password=" + pWord);
    Statement stmt = null;
    ResultSet rs   = null;

    if(userType.equals("L")) // local
    {
      if(! serverUtils.checkSID(unm, remoteSID, uty, catalogUpline, localDefnsDir, defnsDir))
      {
        serverUtils.etotalBytes(req, unm, catalogUpline, 2005, bytesOut[0], 0, "SID:2005h");
        if(con != null) con.close();
        r.flush();
        return;
      }
    }
    else // remote
    {
      if(! serverUtils.checkSID(userName, remoteSID, uty, catalogUpline, localDefnsDir, defnsDir))
      {
        serverUtils.etotalBytes(req, userName, catalogUpline, 2005, bytesOut[0], 0, "SID:2005h");
        if(con != null) con.close();
        r.flush();
        return;
      }
    }

    if(mfr == null || mfr.length() == 0 ) mfr = "";

    if(srchStr == null || srchStr.equalsIgnoreCase("null"))
      srchStr = "";

    if(searchType == null || searchType.equalsIgnoreCase("null") || searchType.length() == 0)
      searchType = "C";

    boolean canSeeCostPrice, canSeeRRPPrice;
    if(p25.equals("Y"))
      canSeeCostPrice = true;
    else canSeeCostPrice = false;
    
    if(p26.equals("Y"))
      canSeeRRPPrice = true;
    else canSeeRRPPrice = false;
    
    generate(con, stmt, rs, r, req, unm, sid, uty, men, den, dnm, bnm, operation.charAt(0), searchType.charAt(0), mfr, srchStr, firstRecNum,
             lastRecNum, firstCodeOnPage, lastCodeOnPage, maxRows, numRecs, catalogURL, band, markup, discount1, discount2, discount3, discount4,
             catalogUpline, remoteSID, imagesDir, pricingURL, pricingUpline, userType, catalogCurrency, priceBasis, canSeeCostPrice, canSeeRRPPrice,
             bytesOut);
    
    serverUtils.totalBytes(con, stmt, rs, req, unm, catalogUpline, 2005, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con  != null) con.close();
    r.flush();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void generate(Connection con, Statement stmt, ResultSet rs, Writer r, HttpServletRequest req, String unm, String sid, String uty,
                        String men, String den, String dnm, String bnm, char operation, char searchType, String mfr, String srchStr,
                        String firstRecNum, String lastRecNum, String currFirstCodeOnPage, String currLastCodeOnPage, int maxRows, int numRecsIn,
                        String catalogURL, String band, String markup, String discount1, String discount2, String discount3, String discount4,
                        String catalogUpline, String remoteSID, String imagesDir, String pricingURL, String pricingUpline, String userType,
                        String catalogCurrency, String priceBasis, boolean canSeeCostPrice, boolean canSeeRRPPrice, int[] bytesOut) throws Exception
  {
    int[] numRecs = new int[1];  numRecs[0] = numRecsIn;

    String[] newFirstCodeOnPage = new String[1]; newFirstCodeOnPage[0] = "";
    String[] newLastCodeOnPage  = new String[1];
    String[] newFirstRecNum     = new String[1];
    String[] newLastRecNum      = new String[1];
 
    String[] html = new String[1]; html[0] = "";
    createPage(con, stmt, rs, html, operation, searchType, mfr, srchStr, numRecs, newFirstCodeOnPage, newLastCodeOnPage, currFirstCodeOnPage,
               currLastCodeOnPage, firstRecNum, lastRecNum, newFirstRecNum, newLastRecNum, maxRows, canSeeCostPrice, canSeeRRPPrice, uty, band,
               markup, discount1, discount2, discount3, discount4, pricingURL, pricingUpline, userType, catalogCurrency, priceBasis, remoteSID,
               imagesDir);

    if(generalUtils.strToInt(newFirstRecNum[0]) <= 0) // prev call has gone back before rec 1
    {  
      newFirstRecNum[0] = "1";
      newLastRecNum[0] = "50";
    }

    scoutln(r, bytesOut, "<form>");
 
    setNav(r, true, mfr, operation, searchType, numRecs[0], newFirstCodeOnPage[0], newLastCodeOnPage[0], srchStr, imagesDir, newFirstRecNum[0],
           newLastRecNum[0], maxRows, catalogURL, unm, remoteSID, uty, men, den, catalogUpline, bnm, bytesOut);

    if(numRecs[0] > 0)
      startBody(r, true, band, userType, canSeeCostPrice, canSeeRRPPrice, uty, bytesOut);

    scoutln(r, bytesOut, html[0]);

    if(numRecs[0] > 0)
      startBody(r, false, band, userType, canSeeCostPrice, canSeeRRPPrice, uty, bytesOut);
    
    setNav(r, false, mfr, operation, searchType, numRecs[0], newFirstCodeOnPage[0], newLastCodeOnPage[0], srchStr, imagesDir, newFirstRecNum[0],
           newLastRecNum[0], maxRows, catalogURL, unm, remoteSID, uty, men, den, catalogUpline, bnm, bytesOut);

    scoutln(r, bytesOut, "</table></form>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void createPage(Connection con, Statement stmt, ResultSet rs, String[] html, char operation, char searchType, String mfr2, String srchStr,
                          int[] numRecs, String[] newFirstCodeOnPage, String[] newLastCodeOnPage, String firstCodeOnPage, String lastCodeOnPage,
                          String firstRecNum, String lastRecNum, String[] newFirstRecNum, String[] newLastRecNum, int maxRows,
                          boolean canSeeCostPrice, boolean canSeeRRPPrice, String uty, String band, String markupStr, String discount1Str,
                          String discount2Str, String discount3Str, String discount4Str, String pricingURL, String pricingUpline, String userType,
                          String catalogCurrency, String priceBasis, String remoteSID, String imagesDir) throws Exception
  {
    stmt = con.createStatement();
    String q;
    int count=1;

    String mfr = "";
    int y, len = mfr2.length();
    for(y=0;y<len;++y)
    {
      if(mfr2.charAt(y) == '\'')
        mfr += "''";
      else mfr += mfr2.charAt(y);
    }

    String showToWeb = "";
    if(! uty.equals("I"))
      showToWeb = " AND ShowToWeb = 'Y'";

    // determine num of recs if not already known, or if doing a search and hence must re-determine
    if(numRecs[0] == -1 || operation == 'F')
    {
      if(searchType == 'C')
      {
        rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM stock WHERE Manufacturer='" + generalUtils.sanitiseForSQL(mfr)
                             + "' AND ManufacturerCode LIKE '" + generalUtils.sanitiseForSQL(srchStr) + "%'" + showToWeb);
      }
      else
      {
        rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM stock WHERE Manufacturer='" + generalUtils.sanitiseForSQL(mfr) + "' AND Description LIKE '%"
                               + generalUtils.sanitiseForSQL(srchStr) + "%'" + showToWeb);
      }
      
      rs.next();
      numRecs[0] = rs.getInt("rowcount");
      rs.close();
    }

    double markup    = generalUtils.doubleFromStr(markupStr) / 100;
    double discount1 = generalUtils.doubleFromStr(discount1Str);
    double discount2 = generalUtils.doubleFromStr(discount2Str);
    double discount3 = generalUtils.doubleFromStr(discount3Str);
    double discount4 = generalUtils.doubleFromStr(discount4Str);
    
    String itemCode="", desc="", mfrCode="", status, salesCurrency, rrp, sp1, sp2, sp3, sp4;
     
    switch(operation)
    {
      case 'F' : // first page
                 if(searchType == 'C')
                 {
                   q = "SELECT ItemCode, Description, ManufacturerCode, Status, SalesCurrency, RRP, SellPrice1, SellPrice2, SellPrice3, SellPrice4 "
                     + "FROM stock WHERE Manufacturer='" + generalUtils.sanitiseForSQL(mfr) + "' AND ManufacturerCode LIKE '" + generalUtils.sanitiseForSQL(srchStr)
                     + "%'" + showToWeb + " ORDER BY ManufacturerCode";
                 }
                 else
                 {
                   q = "SELECT ItemCode, Description, ManufacturerCode, Status, SalesCurrency, RRP, SellPrice1, SellPrice2, SellPrice3, SellPrice4 "
                     + "FROM stock WHERE Manufacturer='" + generalUtils.sanitiseForSQL(mfr) + "' AND Description LIKE '%" + generalUtils.sanitiseForSQL(srchStr)
                     + "%'" + showToWeb + " ORDER BY ManufacturerCode";
                 }
                 
                 stmt.setMaxRows(maxRows);
                 rs = stmt.executeQuery(q);

                 while(rs.next())                  
                 {
                   itemCode = rs.getString(1);
                   desc     = rs.getString(2);
                   mfrCode  = rs.getString(3);
                   status   = rs.getString(4);
                   salesCurrency = rs.getString(5);
                   rrp      = rs.getString(6);
                   sp1      = rs.getString(7);
                   sp2      = rs.getString(8);
                   sp3      = rs.getString(9);
                   sp4      = rs.getString(10);

                   appendBodyLine(html, itemCode, desc, mfrCode, status, band, salesCurrency, rrp, markup, discount1, discount2, discount3, discount4,
                                  remoteSID, userType, priceBasis, catalogCurrency, sp1, sp2, sp3, sp4, canSeeCostPrice, canSeeRRPPrice, uty,
                                  imagesDir);
          
                   if(count++ == 1)
                     newFirstCodeOnPage[0] = mfrCode;
                 }
                 
                 rs.close();
                                 
                 newLastCodeOnPage[0] = mfrCode;
 
                 newFirstRecNum[0] = "1";
                 newLastRecNum[0]  = generalUtils.intToStr(count - 1);
                 break;
      case 'L' : // last page
                 if(searchType == 'C')
                 {
                   q = "SELECT ItemCode, Description, ManufacturerCode, Status, SalesCurrency, RRP, SellPrice1, SellPrice2, SellPrice3, SellPrice4 "
                     + "FROM stock WHERE Manufacturer='" + generalUtils.sanitiseForSQL(mfr) + "' AND ManufacturerCode LIKE '" + generalUtils.sanitiseForSQL(srchStr)
                     + "%"//' AND ManufacturerCode > '" + codeArg
                     + "'" + showToWeb + " ORDER BY ManufacturerCode DESC";
                 }
                 else
                 {
                   q = "SELECT ItemCode, Description, ManufacturerCode, Status, SalesCurrency, RRP, SellPrice1, SellPrice2, SellPrice3, SellPrice4 "
                     + "FROM stock WHERE Manufacturer='" + generalUtils.sanitiseForSQL(mfr) + "' AND Description LIKE '%" + generalUtils.sanitiseForSQL(srchStr)
                     + "%"//' AND ManufacturerCode > '" + codeArg
                     + "'" + showToWeb + " ORDER BY ManufacturerCode DESC";
                 }

                 stmt.setMaxRows(maxRows);
                 rs = stmt.executeQuery(q);
  
                 while(rs.next())                  
                 {
                   itemCode = rs.getString(1);
                   desc     = rs.getString(2);
                   mfrCode  = rs.getString(3);
                   status   = rs.getString(4);
                   salesCurrency = rs.getString(5);
                   rrp      = rs.getString(6);
                   sp1      = rs.getString(7);
                   sp2      = rs.getString(8);
                   sp3      = rs.getString(9);
                   sp4      = rs.getString(10);

                   prependBodyLine(html, itemCode, desc, mfrCode, status, band, salesCurrency, rrp, markup, discount1, discount2, discount3,
                                   discount4, remoteSID, userType, priceBasis, catalogCurrency, sp1, sp2, sp3, sp4, canSeeCostPrice, canSeeRRPPrice,
                                   uty, imagesDir);
          
                   if(count++ == 1)
                     newLastCodeOnPage[0] = mfrCode;
                 }
                 
                 rs.close();
                                 
                 newFirstCodeOnPage[0]  = mfrCode;

                 newFirstRecNum[0] = generalUtils.intToStr(numRecs[0] - count + 2);
                 newLastRecNum[0]  = generalUtils.intToStr(numRecs[0]);
                 break;
      case 'N' : // next page
                 if(searchType == 'C')
                 {
                   q = "SELECT ItemCode, Description, ManufacturerCode, Status, SalesCurrency, RRP, SellPrice1, SellPrice2, SellPrice3, SellPrice4 "
                     + "FROM stock WHERE Manufacturer='" + generalUtils.sanitiseForSQL(mfr) + "' AND ManufacturerCode LIKE '" + generalUtils.sanitiseForSQL(srchStr)
                     + "%' AND ManufacturerCode > '" + lastCodeOnPage + "'" + showToWeb + " ORDER BY ManufacturerCode";
                 }
                 else
                 {
                   q = "SELECT ItemCode, Description, ManufacturerCode, Status, SalesCurrency, RRP, SellPrice1, SellPrice2, SellPrice3, SellPrice4 "
                     + "FROM stock WHERE Manufacturer='" + generalUtils.sanitiseForSQL(mfr) + "' AND Description LIKE '%" + generalUtils.sanitiseForSQL(srchStr)
                     + "%' AND ManufacturerCode > '" + lastCodeOnPage + "'" + showToWeb + " ORDER BY ManufacturerCode";
                 }

                 stmt.setMaxRows(maxRows);
                 rs = stmt.executeQuery(q);

                 while(rs.next())                  
                 {
                   itemCode = rs.getString(1);
                   desc     = rs.getString(2);
                   mfrCode  = rs.getString(3);
                   status   = rs.getString(4);
                   salesCurrency = rs.getString(5);
                   rrp      = rs.getString(6);
                   sp1      = rs.getString(7);
                   sp2      = rs.getString(8);
                   sp3      = rs.getString(9);
                   sp4      = rs.getString(10);
                                                      
                   appendBodyLine(html, itemCode, desc, mfrCode, status, band, salesCurrency, rrp, markup, discount1, discount2, discount3, discount4,
                                  remoteSID, userType, priceBasis, catalogCurrency, sp1, sp2, sp3, sp4, canSeeCostPrice, canSeeRRPPrice, uty,
                                  imagesDir);

                   if(count++ == 1)
                     newFirstCodeOnPage[0] = mfrCode;
                 }
                 
                 rs.close();
                                 
                 newLastCodeOnPage[0] = mfrCode;

                 newFirstRecNum[0] = generalUtils.intToStr(generalUtils.intFromStr(lastRecNum) + 1);
                 newLastRecNum[0]  = generalUtils.intToStr(generalUtils.intFromStr(lastRecNum) + count - 1);
                 break;
      case 'P' : // prev page
                 if(searchType == 'C')
                 {
                   q = "SELECT ItemCode, Description, ManufacturerCode, Status, SalesCurrency, RRP, SellPrice1, SellPrice2, SellPrice3, SellPrice4 "
                     + "FROM stock WHERE Manufacturer='" + generalUtils.sanitiseForSQL(mfr) + "' AND ManufacturerCode LIKE '" + generalUtils.sanitiseForSQL(srchStr)
                     + "%' AND ManufacturerCode < '" + firstCodeOnPage//codeArg
                     + "'" + showToWeb + " ORDER BY ManufacturerCode DESC";
                 }
                 else
                 {
                   q = "SELECT ItemCode, Description, ManufacturerCode, Status, SalesCurrency, RRP, SellPrice1, SellPrice2, SellPrice3, SellPrice4 "
                     + "FROM stock WHERE Manufacturer='" + generalUtils.sanitiseForSQL(mfr) + "' AND Description LIKE '%" + generalUtils.sanitiseForSQL(srchStr)
                     + "%' AND ManufacturerCode < '" + firstCodeOnPage//codeArg
                     + "'" + showToWeb + " ORDER BY ManufacturerCode DESC";
                 }
                 
                 stmt.setMaxRows(maxRows);
                 rs = stmt.executeQuery(q);

                 while(rs.next())                  
                 {
                   itemCode = rs.getString(1);
                   desc     = rs.getString(2);
                   mfrCode  = rs.getString(3);
                   status   = rs.getString(4);
                   salesCurrency = rs.getString(5);
                   rrp      = rs.getString(6);
                   sp1      = rs.getString(7);
                   sp2      = rs.getString(8);
                   sp3      = rs.getString(9);
                   sp4      = rs.getString(10);
                                                      
                   prependBodyLine(html, itemCode, desc, mfrCode, status, band, salesCurrency, rrp, markup, discount1, discount2, discount3,
                                   discount4, remoteSID, userType, priceBasis, catalogCurrency, sp1, sp2, sp3, sp4, canSeeCostPrice, canSeeRRPPrice,
                                   uty, imagesDir);
          
                   if(count++ == 1)
                     newLastCodeOnPage[0] = mfrCode;
                 }

                 rs.close();
                                 
                 newFirstCodeOnPage[0] = mfrCode;

                 newFirstRecNum[0] = generalUtils.intToStr(generalUtils.intFromStr(firstRecNum) - count   + 1);
                 newLastRecNum[0]  = generalUtils.intToStr(generalUtils.intFromStr(firstRecNum) - 1);
                 break;
    }
  
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void setNav(Writer r, boolean first, String mfr, char operation, char searchType, int numRecs, String firstCodeOnPage,
                      String lastCodeOnPage, String srchStr, String imagesDir, String newFirstRecNum, String newLastRecNum, int maxRows, 
                      String catalogURL, String unm, String remoteSID, String uty, String men, String den, String catalogUpline, String bnm, int[] bytesOut)
                      throws Exception
  {
    if(first)
    {
      scoutln(r, bytesOut, "<table id='pageColumn' cellspacing='0' cellpadding='0' width='100%'>");
    }

    scoutln(r, bytesOut, "<tr bgcolor=\"#C0C0C0\"><td height='20' nowrap='nowrap' colspan='3'><p>");
    
    int newFirstRecNumI = generalUtils.strToInt(newFirstRecNum);
    int newLastRecNumI  = generalUtils.strToInt(newLastRecNum);

    if(numRecs == 0)
    {
      if(first)
        scoutln(r, bytesOut, "No Records");
    }
    else
    {
      scoutln(r, bytesOut, "Records " + newFirstRecNum + " to " + newLastRecNum + " of " + numRecs);
      if(newFirstRecNumI > 1 || newLastRecNumI < numRecs)
        scoutln(r, bytesOut, "<img src=\"" + imagesDir + "d.gif\" />");
    }

    char topOrBottom;
    if(first)
      topOrBottom = 'T';
    else topOrBottom = 'B';
    
    if(newFirstRecNumI > 1)
    {
      scoutln(r, bytesOut, "&#160;<a href=\"javascript:page2005i('" + mfr + "','F','" + newFirstRecNum + "','" + newLastRecNum + "','','','"
                             + topOrBottom + "','" + numRecs + "','" + remoteSID + "')\">First</a>");
      
      scoutln(r, bytesOut, "&#160;&#160;<a href=\"javascript:page2005i('" + mfr + "','P','" + newFirstRecNum + "','" + newLastRecNum + "','"
                             + generalUtils.sanitise(firstCodeOnPage) + "','','" + topOrBottom + "','" + numRecs + "','" + remoteSID + "')\">Previous</a>");
    }
     
    if(newLastRecNumI < numRecs)
    {
      scoutln(r, bytesOut, "&#160;&#160;<a href=\"javascript:page2005i('" + mfr + "','N','" + newFirstRecNum + "','" + newLastRecNum + "','','"
                             + generalUtils.sanitise(lastCodeOnPage) + "','" + topOrBottom + "','" + numRecs + "','" + remoteSID + "')\">Next</a>");
      
      scoutln(r, bytesOut, "&#160;&#160;<a href=\"javascript:page2005i('" + mfr + "','L','" + newFirstRecNum + "','" + newLastRecNum + "','','','"
                             + topOrBottom + "','" + numRecs + "','" + remoteSID + "')\">Last</a>");
    }
    
    if(numRecs > 0)
      scoutln(r, bytesOut, "</p></td></tr><tr><td>\n");
    else scoutln(r, bytesOut, "</p>\n");
    
    scoutln(r, bytesOut, "</td></tr><tr><td><img src=\"" + imagesDir + "z402.gif\"></td></tr>");
    scoutln(r, bytesOut, "<tr bgcolor='#C0C0C0'><td nowrap='nowrap' colspan='3'><p>\n");

    if(first)
    {
      scoutln(r, bytesOut, "<a href=\"javascript:page2005i('" + mfr + "','F','" + newFirstRecNum + "','" + newLastRecNum + "','','','T','" + numRecs
                           + "','" + remoteSID + "')\">Search</a> for ");
    }
    else
    {
      if(numRecs > 0)
      {
        scoutln(r, bytesOut, "<a href=\"javascript:page2005i('" + mfr + "','F','" + newFirstRecNum + "','" + newLastRecNum + "','','','B','" + numRecs
                             + "','" + remoteSID + "')\">Search</a> for ");
      }
    }
    
    if(first)
    {
     scoutln(r, bytesOut, "&#160;<input type='text' size='20' maxlength='20' value=\"" + srchStr + "\" name='srchStr1' />");
    }
    else 
    {
      if(numRecs > 0)
      {
        scoutln(r, bytesOut, "&#160;<input type='text' size='20' maxlength='20' value=\"" + srchStr + "\" name='srchStr2' />");
      }
    }

    if(first)
    {
      scoutln(r, bytesOut, "&#160;&#160;<input type='radio' name='searchType1'");
      if(searchType == 'D')
        scoutln(r, bytesOut, " checked='checked'");
      scoutln(r, bytesOut, " />Description &#160; <input type='radio' name='searchType1'");
      if(searchType == 'C')
        scoutln(r, bytesOut, " checked='checked'");
      scoutln(r, bytesOut, " />Manufacturer Code\n");
    }
    else
    {
      if(numRecs > 0)
      {
        scoutln(r, bytesOut, "<input type='radio' name='searchType2'");
        if(searchType == 'D')
          scoutln(r, bytesOut, " checked='checked'");
        scoutln(r, bytesOut, " />Description &#160; <input type='radio' name='searchType2'");
        if(searchType == 'C')
          scoutln(r, bytesOut, " checked='checked'");
        scoutln(r, bytesOut, " />Manufacturer Code");
      }
    }

    scoutln(r, bytesOut, "</p>");

    scoutln(r, bytesOut, "</td></tr>");

    scoutln(r, bytesOut, "<tr><td><img src=\"" + imagesDir + "z402.gif\"/></td></tr>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void startBody(Writer r, boolean first, String band, String userType, boolean canSeeCostPrice, boolean canSeeRRPPrice, String uty, int[] bytesOut) throws Exception
  {
    if(first)
    {
      scoutln(r, bytesOut, "<tr><td>");
      scoutln(r, bytesOut, "<table id='page' cellspacing='1' cellpadding='1' width='100%'>");
    }
 
    String priceHeading;
    
    switch(generalUtils.intFromStr(band))
    {
      case 888 : priceHeading = "Our Sell Prices";  break;
      case 1   :
      case 2   :
      case 3   :
      case 4   : priceHeading = "Your Price";  break;
      default  : priceHeading = "List Price";  break;
    }
     
    scoutln(r, bytesOut, "<tr>");
    
    if(! userType.equals("R")) // not remote
      scoutln(r, bytesOut, "<td bgcolor='#F0F0F0'><p>Our Code&#160;</p></td>");

    scoutln(r, bytesOut, "<td bgcolor='#F0F0F0'><p>Manufacturer Code&#160;</p></td>");
    scoutln(r, bytesOut, "<td bgcolor='#F0F0F0'><p>Description</p></td>");
    
    if(uty.equals("I") && userType.equals("R")) // remote
    {    
      if(canSeeCostPrice)
        scoutln(r, bytesOut, "<td bgcolor='#F0F0F0' align='right'><p>Cost Price</p></td>");

      if(canSeeRRPPrice)
        scoutln(r, bytesOut, "<td bgcolor='#F0F0F0' align='right'><p>RRP</p></td>");
    }
    
    scoutln(r, bytesOut, "<td bgcolor='#F0F0F0' align='right'><p>" + priceHeading + "</p></td></tr>");

    if(! first)
     scoutln(r, bytesOut, "</table></td></tr>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void appendBodyLine(String[] html, String itemCode, String desc, String mfrCode, String status, String band, String salesCurrency,
                              String rrpStr, double markup, double discount1, double discount2, double discount3, double discount4, String remoteSID,
                              String userType, String priceBasis, String catalogCurrency, String sp1, String sp2, String sp3, String sp4,
                              boolean canSeeCostPrice, boolean canSeeRRPPrice, String uty, String imagesDir) throws Exception
  {
    html[0] += "<tr bgcolor='#FFFFFF'><td nowrap='nowrap'><p>";

    if(! userType.equals("R")) // not remote
    {
      html[0] += "<a href=\"javascript:s2000b('" + generalUtils.sanitise2(mfrCode) + "','" + generalUtils.sanitise2(itemCode) + "','" + remoteSID + "')\">"
              + itemCode + "</a>";
      if(status.equals("C"))
        html[0] += "<font size='1' color='red'><sup> Replaced</sup></font>";
          
      html[0] += "</p></td><td nowrap='nowrap' valign='top'><p>" + mfrCode + "</p></td><td><p>" + generalUtils.handleSuperScripts(desc) + "</p></td>";
    }
    else
    {
      html[0] += "<a href=\"javascript:s2000b('" + generalUtils.sanitise2(mfrCode) + "','" + generalUtils.sanitise2(itemCode) + "','" + remoteSID + "')\">"
              + mfrCode + "</a>";

      if(status.equals("C"))
        html[0] += "<font size='1' color='red'><sup> Replaced</sup></font>";
          
      html[0] += "</p></td><td><p>" + generalUtils.handleSuperScripts(desc) + "</p></td>";  
    }
    
    double list = generalUtils.doubleFromStr(rrpStr);
    double rrp = list * markup;
    double sellPrice1  = rrp - (rrp * (discount1 / 100));
    double sellPrice2  = rrp - (rrp * (discount2 / 100));
    double sellPrice3  = rrp - (rrp * (discount3 / 100));
    double sellPrice4  = rrp - (rrp * (discount4 / 100));

    String price, priceForCart;
    
    switch(generalUtils.intFromStr(band))
    {
      case 888 : price = generalUtils.doubleDPs('2', rrp) + " " + generalUtils.doubleDPs('2', sellPrice1) + " " + generalUtils.doubleDPs('2', sellPrice2) + " " + generalUtils.doubleDPs('2', sellPrice3) + " " + generalUtils.doubleDPs('2', sellPrice4);
                 priceForCart = generalUtils.doubleDPs('2', rrp);
                 break;
      case 1   : price = generalUtils.doubleDPs('2', sellPrice1); priceForCart = price; break;
      case 2   : price = generalUtils.doubleDPs('2', sellPrice2); priceForCart = price; break;
      case 3   : price = generalUtils.doubleDPs('2', sellPrice3); priceForCart = price; break;
      case 4   : price = generalUtils.doubleDPs('2', sellPrice4); priceForCart = price; break;
      default  : price = generalUtils.doubleDPs('2', rrp);        priceForCart = price; break; // just-in-case
    }

    if(uty.equals("I") && userType.equals("R")) // remote
    {            
      double costPrice;
      if(priceBasis.equals("L"))
        costPrice = list;
      else
      {
             if(band.equals("1")) costPrice = generalUtils.doubleFromStr(sp1);
        else if(band.equals("2")) costPrice = generalUtils.doubleFromStr(sp2);
        else if(band.equals("3")) costPrice = generalUtils.doubleFromStr(sp3);
        else if(band.equals("4")) costPrice = generalUtils.doubleFromStr(sp4);
        else                      costPrice = list;
      }
    
      if(canSeeCostPrice)
      {
        if(costPrice == 0.0)
          html[0] += "<td>&#160;</td>";
        else html[0] += "<td nowrap='nowrap' valign='top' align='right'><p>" + salesCurrency + " " + generalUtils.doubleDPs('2', costPrice) + "</p></td>";
      }

      if(canSeeRRPPrice)
      {
        if(list == 0.0)
          html[0] += "<td>&#160;</td>";
        else html[0] += "<td nowrap='nowrap' valign='top' align='right'><p>" + catalogCurrency + " " + generalUtils.doubleDPs('2', list) + "</p></td>";
      }
    }
    
    if(generalUtils.doubleFromStr(priceForCart) == 0.0)
      html[0] += "<td>&#160;</td>";
    else html[0] += "<td nowrap='nowrap' valign='top' align='right'><p>" + catalogCurrency + " " + price + "</p></td>";

    html[0] += "<td align='center'><a href=\"javascript:addToCart('" + itemCode + "','" + priceForCart + "','" + salesCurrency + "','"
            + generalUtils.sanitise2(mfrCode) + "','Each'," + generalUtils.sanitise2(desc) + "','" + salesCurrency + "','" + remoteSID + "')\"><img border='0' src=\"" + imagesDir
            + "cart.png\" /></a></td></tr>";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void prependBodyLine(String[] html, String itemCode, String desc, String mfrCode, String status, String band, String salesCurrency,
                               String rrpStr, double markup, double discount1, double discount2, double discount3, double discount4, String remoteSID,
                               String userType, String priceBasis, String catalogCurrency, String sp1, String sp2, String sp3, String sp4,
                               boolean canSeeCostPrice, boolean canSeeRRPPrice, String uty, String imagesDir) throws Exception
  {
    String line = "<tr bgcolor='#FFFFFF'><td nowrap='nowrap'><p>";

    if(! userType.equals("R")) // not remote
    {
      line += "<a href=\"javascript:s2000b('" + generalUtils.sanitise2(mfrCode) + "','" + generalUtils.sanitise2(itemCode) + "','" + remoteSID + "')\">" + itemCode
           + "</a>";
      if(status.equals("C"))
        line += "<font size='1' color='red'><sup> Replaced</sup></font>";
          
      line += "</p></td><td nowrap='nowrap' valign='top'><p>" + mfrCode + "</p></td><td><p>" + generalUtils.handleSuperScripts(desc) + "</p></td>";
    }
    else
    {
      line += "<a href=\"javascript:s2000b('" + generalUtils.sanitise2(mfrCode) + "','" + generalUtils.sanitise2(itemCode) + "','" + remoteSID + "')\">" + mfrCode
           + "</a>";

      if(status.equals("C"))
        line += "<font size='1' color='red'><sup> Replaced</sup></font>";
          
      line += "</p></td><td><p>" + generalUtils.handleSuperScripts(desc) + "</p></td>";  
    }

    double list = generalUtils.doubleFromStr(rrpStr);
    double rrp = list * markup;
    double sellPrice1  = rrp - (rrp * (discount1 / 100));
    double sellPrice2  = rrp - (rrp * (discount2 / 100));
    double sellPrice3  = rrp - (rrp * (discount3 / 100));
    double sellPrice4  = rrp - (rrp * (discount4 / 100));

    String price, priceForCart;
    
    switch(generalUtils.intFromStr(band))
    {
      case 888 : price = generalUtils.doubleDPs('2', rrp) + " " + generalUtils.doubleDPs('2', sellPrice1) + " " + generalUtils.doubleDPs('2', sellPrice2) + " "
                       + generalUtils.doubleDPs('2', sellPrice3) + " " + generalUtils.doubleDPs('2', sellPrice4);
                 priceForCart = generalUtils.doubleDPs('2', rrp);
                 break;
      case 1   : price = generalUtils.doubleDPs('2', sellPrice1); priceForCart = price; break;
      case 2   : price = generalUtils.doubleDPs('2', sellPrice2); priceForCart = price; break;
      case 3   : price = generalUtils.doubleDPs('2', sellPrice3); priceForCart = price; break;
      case 4   : price = generalUtils.doubleDPs('2', sellPrice4); priceForCart = price; break;
      default  : price = generalUtils.doubleDPs('2', rrp);        priceForCart = price; break; // just-in-case
    }
    
    if(uty.equals("I") && userType.equals("R")) // remote
    {   
      double costPrice;
      if(priceBasis.equals("L"))
        costPrice = list;
      else
      {
             if(band.equals("1")) costPrice = generalUtils.doubleFromStr(sp1);
        else if(band.equals("2")) costPrice = generalUtils.doubleFromStr(sp2);
        else if(band.equals("3")) costPrice = generalUtils.doubleFromStr(sp3);
        else if(band.equals("4")) costPrice = generalUtils.doubleFromStr(sp4);
        else                      costPrice = list;
      }
    
      if(canSeeCostPrice)
      {
        if(costPrice == 0.0)
          line += "<td>&#160;</td>";
        else line += "<td nowrap='nowrap' valign='top' align='right'><p>" + salesCurrency + " " + generalUtils.doubleDPs('2', costPrice) + "</p></td>";
      }
      
      if(canSeeRRPPrice)
      {
        if(list == 0.0)
          line += "<td>&#160;</td>";
        else line += "<td nowrap='nowrap' valign='top' align='right'><p>" + catalogCurrency + " " + generalUtils.doubleDPs('2', list) + "</p></td>";
      }
    }
    
    if(generalUtils.doubleFromStr(priceForCart) == 0.0)
      line += "<td>&#160;</td>";
    else line += "<td nowrap='nowrap' valign='top' align='right'><p>" + catalogCurrency + " " + price + "</p></td>";
    
    line += "<td align='center'><a href=\"javascript:addToCart('" + itemCode + "','" + priceForCart + "','" + salesCurrency + "','"
         + generalUtils.sanitise2(mfrCode) + "','Each'," + generalUtils.sanitise2(desc) + "','" + salesCurrency + "','" + remoteSID + "')\"><img border='0' src=\"" + imagesDir
         + "cart.png\" /></a></td></tr>";

    html[0] = line + "\n" + html[0];
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(Writer r, int bytesOut[], String str) throws Exception
  {      
    r.write(str + "\n");
    bytesOut[0] += (str.length() + 1);    
  }
  
}
