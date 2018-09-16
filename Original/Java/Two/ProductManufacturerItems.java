// =======================================================================================================================================================================================================
// System: ZaraStar: Product: create mfr items selection
// Module: ProductManufacturerItems.java
// Author: C.K.Harvey
// Copyright (c) 1999-2008 Christopher Harvey. All Rights Reserved.
// Where: Local server
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;
import java.io.*;

public class ProductManufacturerItems extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  DrawingUtils drawingUtils = new DrawingUtils();
  AdminControlUtils adminControlUtils = new AdminControlUtils();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;
    
    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", operation="", searchType="", srchStr="", firstRecNum="", lastRecNum="", firstCodeOnPage="", lastCodeOnPage="", maxRows="", numRecs="", mfr="";
    try
    {
      directoryUtils.setContentHeaders(res);
      out = res.getWriter();

      unm         = req.getParameter("unm");
      sid         = req.getParameter("sid");
      uty         = req.getParameter("uty");
      men         = req.getParameter("men");
      den         = req.getParameter("den");
      dnm         = req.getParameter("dnm");
      bnm         = req.getParameter("bnm");
      mfr         = req.getParameter("p1");
      operation   = req.getParameter("p2");
      searchType  = req.getParameter("p10");

      srchStr     = req.getParameter("p3");
      if(srchStr == null) srchStr = "";
      srchStr     = new String(srchStr.getBytes("ISO-8859-1"), "UTF-8");

      firstRecNum = req.getParameter("p4");
      lastRecNum  = req.getParameter("p5");

      maxRows = req.getParameter("p6");
      if(maxRows == null || maxRows.length() == 0) maxRows = "50";

      numRecs    = req.getParameter("p7");
      if(numRecs == null || numRecs.length() == 0) numRecs = "-1";

      firstCodeOnPage = req.getParameter("p8");
      if(firstCodeOnPage == null) firstCodeOnPage = "";
      firstCodeOnPage    = new String(firstCodeOnPage.getBytes("ISO-8859-1"), "UTF-8");

      lastCodeOnPage = req.getParameter("p9");
      if(lastCodeOnPage == null) lastCodeOnPage = "";
      lastCodeOnPage    = new String(lastCodeOnPage.getBytes("ISO-8859-1"), "UTF-8");

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, mfr, operation, searchType, srchStr, firstRecNum, lastRecNum, firstCodeOnPage,
           lastCodeOnPage, generalUtils.strToInt(maxRows), generalUtils.strToInt(numRecs), bytesOut);
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
      
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "ProductManufacturerItems", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2020, bytesOut[0], 0, "ERR:" + mfr);
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                    String mfr, String operation, String searchType, String srchStr, String firstRecNum, String lastRecNum, String firstCodeOnPage,
                    String lastCodeOnPage, int maxRows, int numRecs, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');
    
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if((uty.equals("R") && ! adminControlUtils.notDisabled(con, stmt, rs, 902)) || (uty.equals("A") && ! adminControlUtils.notDisabled(con, stmt, rs, 802)) || (uty.equals("I") && ! authenticationUtils.verifyAccess(con, stmt, rs, req, 2003, unm, uty, dnm, localDefnsDir, defnsDir)))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "ProductManufacturerItems", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2020, bytesOut[0], 0, "ACC:" + mfr);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "ProductManufacturerItems", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2020, bytesOut[0], 0, "SID:" + mfr);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(mfr == null || mfr.length() == 0 ) mfr = "";

    if(srchStr == null || srchStr.equalsIgnoreCase("null"))
      srchStr = "";

    if(searchType == null || searchType.equalsIgnoreCase("null") || searchType.length() == 0)
      searchType = "C";

    generate(con, stmt, rs, out, req, unm, sid, uty, men, den, dnm, bnm, operation.charAt(0), searchType.charAt(0), mfr, srchStr, firstRecNum, lastRecNum, firstCodeOnPage, lastCodeOnPage, maxRows, numRecs, imagesDir, localDefnsDir, defnsDir,
             bytesOut);
    
    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 2020, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void generate(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty,
                        String men, String den, String dnm, String bnm, char operation, char searchType, String mfr, String srchStr,
                        String firstRecNum, String lastRecNum, String currFirstCodeOnPage, String currLastCodeOnPage, int maxRows, int numRecsIn,
                        String imagesDir, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    int[] numRecs = new int[1];  numRecs[0] = numRecsIn;

    String[] newFirstCodeOnPage = new String[1];
    String[] newLastCodeOnPage  = new String[1];
    String[] newFirstRecNum     = new String[1];
    String[] newLastRecNum      = new String[1];
 
    String[] html = new String[1]; html[0] = "";
    createPage(con, stmt, rs, html, operation, searchType, mfr, srchStr, numRecs, newFirstCodeOnPage, newLastCodeOnPage, currFirstCodeOnPage,
               currLastCodeOnPage, firstRecNum, lastRecNum, newFirstRecNum, newLastRecNum, maxRows, uty);

    if(generalUtils.strToInt(newFirstRecNum[0]) <= 0) // prev call has gone back before rec 1
    {  
      newFirstRecNum[0] = "1";
      newLastRecNum[0] = "50";
    }

    setHead(con, stmt, rs, out, req, unm, sid, uty, men, den, dnm, bnm, newFirstRecNum[0], newLastRecNum[0], numRecs[0], mfr, localDefnsDir, defnsDir, bytesOut);
 
    setNav(out, true, mfr, operation, searchType, numRecs[0], newFirstCodeOnPage[0], newLastCodeOnPage[0], srchStr, imagesDir, newFirstRecNum[0],
           newLastRecNum[0], maxRows, bytesOut);

    if(numRecs[0] > 0)
      startBody(out, true, imagesDir, bytesOut);

    scoutln(out, bytesOut, html[0]);

    if(numRecs[0] > 0)
      startBody(out, false, imagesDir, bytesOut);
    
    setNav(out, false, mfr, operation, searchType, numRecs[0], newFirstCodeOnPage[0], newLastCodeOnPage[0], srchStr, imagesDir, newFirstRecNum[0],
           newLastRecNum[0], maxRows, bytesOut);

    scoutln(out, bytesOut, "</table></form>");     scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void createPage(Connection con, Statement stmt, ResultSet rs, String[] html, char operation, char searchType, String mfr2, String srchStr,
                          int[] numRecs, String[] newFirstCodeOnPage, String[] newLastCodeOnPage, String firstCodeOnPage, String lastCodeOnPage,
                          String firstRecNum, String lastRecNum, String[] newFirstRecNum, String[] newLastRecNum, int maxRows, String uty)
                          throws Exception
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
      numRecs[0] = rs.getInt("rowcount") ;
      rs.close();
    }
  
    String itemCode="", desc="", mfrCode="", status;
     
    switch(operation)
    {
      case 'F' : // first page
                 if(searchType == 'C')
                 {
                   q = "SELECT ItemCode, Description, ManufacturerCode, Status FROM stock WHERE Manufacturer='" + generalUtils.sanitiseForSQL(mfr)
                     + "' AND ManufacturerCode LIKE '" + generalUtils.sanitiseForSQL(srchStr) + "%'" + showToWeb + " ORDER BY ManufacturerCode";
                 }
                 else
                 {
                   q = "SELECT ItemCode, Description, ManufacturerCode, Status FROM stock WHERE Manufacturer='" + generalUtils.sanitiseForSQL(mfr)
                     + "' AND Description LIKE '%" + generalUtils.sanitiseForSQL(srchStr) + "%'" + showToWeb + " ORDER BY ManufacturerCode";
                 }
                 
                 stmt.setMaxRows(maxRows);
                 rs = stmt.executeQuery(q);

                 while(rs.next())                  
                 {
                   itemCode = rs.getString(1);
                   desc     = rs.getString(2);
                   mfrCode  = rs.getString(3);
                   status   = rs.getString(4);

                   appendBodyLine(html, itemCode, desc, mfrCode, status);
          
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
                   q = "SELECT ItemCode, Description, ManufacturerCode, Status FROM stock WHERE Manufacturer='" + generalUtils.sanitiseForSQL(mfr)
                     + "' AND ManufacturerCode LIKE '" + generalUtils.sanitiseForSQL(srchStr) + "%"//' AND ManufacturerCode > '" + codeArg
                     + "'" + showToWeb + " ORDER BY ManufacturerCode DESC";
                 }
                 else
                 {
                   q = "SELECT ItemCode, Description, ManufacturerCode, Status FROM stock WHERE Manufacturer='" + generalUtils.sanitiseForSQL(mfr)
                     + "' AND Description LIKE '%" + generalUtils.sanitiseForSQL(srchStr) + "%"//' AND ManufacturerCode > '" + codeArg
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

                   prependBodyLine(html, itemCode, desc, mfrCode, status);
          
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
                   q = "SELECT ItemCode, Description, ManufacturerCode, Status FROM stock WHERE Manufacturer='" + generalUtils.sanitiseForSQL(mfr)
                     + "' AND ManufacturerCode LIKE '" + generalUtils.sanitiseForSQL(srchStr) + "%' AND ManufacturerCode > '" + lastCodeOnPage
                     + "'" + showToWeb + " ORDER BY ManufacturerCode";
                 }
                 else
                 {
                   q = "SELECT ItemCode, Description, ManufacturerCode, Status FROM stock WHERE Manufacturer='" + generalUtils.sanitiseForSQL(mfr)
                     + "' AND Description LIKE '%" + generalUtils.sanitiseForSQL(srchStr) + "%' AND ManufacturerCode > '" + lastCodeOnPage
                     + "'" + showToWeb + " ORDER BY ManufacturerCode";
                 }

                 stmt.setMaxRows(maxRows);
                 rs = stmt.executeQuery(q);

                 while(rs.next())                  
                 {
                   itemCode = rs.getString(1);
                   desc     = rs.getString(2);
                   mfrCode  = rs.getString(3);
                   status   = rs.getString(4);
                                                      
                   appendBodyLine(html, itemCode, desc, mfrCode, status);

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
                   q = "SELECT ItemCode, Description, ManufacturerCode, Status FROM stock WHERE Manufacturer='" + generalUtils.sanitiseForSQL(mfr)
                     + "' AND ManufacturerCode LIKE '" + generalUtils.sanitiseForSQL(srchStr) + "%' AND ManufacturerCode < '" + firstCodeOnPage//codeArg
                     + "'" + showToWeb + " ORDER BY ManufacturerCode DESC";
                 }
                 else
                 {
                   q = "SELECT ItemCode, Description, ManufacturerCode, Status FROM stock WHERE Manufacturer='" + generalUtils.sanitiseForSQL(mfr)
                     + "' AND Description LIKE '%" + generalUtils.sanitiseForSQL(srchStr) + "%' AND ManufacturerCode < '" + firstCodeOnPage//codeArg
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
                                                      
                   prependBodyLine(html, itemCode, desc, mfrCode, status);
          
                   if(count++ == 1)
                     newLastCodeOnPage[0] = mfrCode;
                 }

                 rs.close();
                                 
                 newFirstCodeOnPage[0] = mfrCode;

                 newFirstRecNum[0] = generalUtils.intToStr(generalUtils.intFromStr(firstRecNum) - count   + 1);
                 newLastRecNum[0]  = generalUtils.intToStr(generalUtils.intFromStr(firstRecNum) - 1);
                 break;
    }
  
    stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void setHead(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty,
                       String men, String den, String dnm, String bnm, String newFirstRecNum, String newLastRecNum, int numRecs, String mfr,
                       String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>" + mfr + " Listing</title>");
    scoutln(out, bytesOut, "<script javascript=\"JavaScript\">");
   
    scoutln(out, bytesOut, "function page(mfr,operation,firstNum,lastNum,firstCode,lastCode,topOrBottom,numRecs){");
    scoutln(out, bytesOut, "var srchStr='';if(topOrBottom=='T')srchStr=document.forms[0].srchStr1.value;");
    scoutln(out, bytesOut, "else srchStr=document.forms[0].srchStr2.value;");
    scoutln(out, bytesOut, "var maxRows;if(topOrBottom=='T')maxRows=document.forms[0].maxRows1.value;");
    scoutln(out, bytesOut, "else maxRows=document.forms[0].maxRows2.value;");    
    scoutln(out, bytesOut, "var firstCode1=sanitise(firstCode);var lastCode1=sanitise(lastCode);");

    scoutln(out, bytesOut, "var searchType='C';if(topOrBottom=='T'){if(document.forms[0].searchType1[0].checked)searchType='D';}");
    scoutln(out, bytesOut, "else {if(exists())if(document.forms[0].searchType2[0].checked)searchType='D';}");
    
    scoutln(out, bytesOut, "function exists(){var e=document.getElementsByTagName('input');");
    scoutln(out, bytesOut, "for(var i=0;i<e.length;i++){if(e[i].type==\"radio\")if(e[i].name==\"searchType2\")return true;}return false;}");
        
    scoutln(out, bytesOut, "window.location.replace(\"/central/servlet/ProductManufacturerItems?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                          + "&p1=\"+escape(mfr)+\"&p2=\"+operation+\"&p3=\"+srchStr+\"&p4=\"+firstNum+\"&p5=\"+lastNum+\"&p6=\"+maxRows+\"&p7=\"+numRecs+\"&p8=\"+firstCode+\"&p10=\"+searchType+\"&p9=\"+lastCode);}");

    if(! uty.equals("I") && (adminControlUtils.notDisabled(con, stmt, rs, 902) || adminControlUtils.notDisabled(con, stmt, rs, 802)))
    {
      scoutln(out, bytesOut, "function view(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/StockEnquiryExternal?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+p1;}");
    }
    else
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 1001, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function view(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/MainPageUtils1a?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+p1;}");
    }
    
    scoutln(out, bytesOut, "function sanitise(code){");
    scoutln(out, bytesOut, "var code2='';var x;var len=code.length;");
    scoutln(out, bytesOut, "for(x=0;x<len;++x)");
    scoutln(out, bytesOut, "if(code.charAt(x)=='#')code2+='%23';");
    scoutln(out, bytesOut, "else if(code.charAt(x)==\"'\")code2+='%27';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='\"')code2+='%22';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='&')code2+='%26';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='%')code2+='%25';");
    scoutln(out, bytesOut, "else if(code.charAt(x)==' ')code2+='%20';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='?')code2+='%3F';");
    scoutln(out, bytesOut, "else code2+=code.charAt(x);");
    scoutln(out, bytesOut, "return code2};");

    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");
    
    int[] hmenuCount = new int[1];

    drawingUtils.outputPageFrame(con, stmt, rs, out, req, "2020", "", "ProductManufacturerItems", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount, bytesOut);

    drawingUtils.drawTitle(out, false, false, "ProductManufacturerItems", "", mfr + " Listing", "2020", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<form>");
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void setNav(PrintWriter out, boolean first, String mfr, char operation, char searchType, int numRecs, String firstCodeOnPage,
                      String lastCodeOnPage, String srchStr, String imagesDir, String newFirstRecNum, String newLastRecNum, int maxRows,
                      int[] bytesOut) throws Exception
  {
    if(first)
    {
      scoutln(out, bytesOut, "<table id=\"pageColumn\" cellspacing=0 cellpadding=0 width=100%>");
    }

    scoutln(out, bytesOut, "<tr bgcolor=\"#C0C0C0\"><td height=20 nowrap colspan=3><p>");
    
    int newFirstRecNumI = generalUtils.strToInt(newFirstRecNum);
    int newLastRecNumI  = generalUtils.strToInt(newLastRecNum);

    if(numRecs == 0)
    {
      if(first)
        scoutln(out, bytesOut, "No Records");
    }
    else
    {
      scoutln(out, bytesOut, "Records " + newFirstRecNum + " to " + newLastRecNum + " of " + numRecs);
      if(newFirstRecNumI > 1 || newLastRecNumI < numRecs)
        scoutln(out, bytesOut, "<img src=\"" + imagesDir + "d.gif\">");
    }

    char topOrBottom;
    if(first)
      topOrBottom = 'T';
    else topOrBottom = 'B';
    
    if(newFirstRecNumI > 1)
    {
      String arg = "";

      int len = firstCodeOnPage.length();

      for(int y=0;y<len;++y)
      {
        if(firstCodeOnPage.charAt(y) == '\'')
          arg += "\\'";
        else arg += firstCodeOnPage.charAt(y);
      }
            
      scoutln(out, bytesOut, "&nbsp;<a href=\"javascript:page('" + mfr + "','F','" + newFirstRecNum + "','" + newLastRecNum + "','','','"
                             + topOrBottom + "','" + numRecs + "')\">First</a>");
      
      scoutln(out, bytesOut, "&nbsp;&nbsp;<a href=\"javascript:page('" + mfr + "','P','" + newFirstRecNum + "','" + newLastRecNum + "','"
                             + generalUtils.sanitise(firstCodeOnPage) + "','','" + topOrBottom + "','" + numRecs + "')\">Previous</a>");
    }
     
    if(newLastRecNumI < numRecs)
    {
      String arg = "";
      int len = lastCodeOnPage.length();
      for(int y=0;y<len;++y)
      {
        if(lastCodeOnPage.charAt(y) == '\'')
          arg += "\\'";
        else arg += lastCodeOnPage.charAt(y);
      }

      scoutln(out, bytesOut, "&nbsp;&nbsp;<a href=\"javascript:page('" + mfr + "','N','" + newFirstRecNum + "','" + newLastRecNum + "','','"
                             + generalUtils.sanitise(lastCodeOnPage) + "','" + topOrBottom + "','" + numRecs + "')\">Next</a>");
      
      scoutln(out, bytesOut, "&nbsp;&nbsp;<a href=\"javascript:page('" + mfr + "','L','" + newFirstRecNum + "','" + newLastRecNum + "','','','"
                             + topOrBottom + "','" + numRecs + "')\">Last</a>");
    }
    
    if(numRecs > 0)
      scoutln(out, bytesOut, "</p></td></tr><tr><td>\n");
    else scoutln(out, bytesOut, "</p>\n");
    
    scoutln(out, bytesOut, "</td></tr>");
    scoutln(out, bytesOut, "<tr><td><img src=\"" + imagesDir + "z402.gif\"></td></tr>");
    scoutln(out, bytesOut, "<tr bgcolor=\"#C0C0C0\"><td nowrap colspan=3><p>");
    
    if(first)
    {
      scoutln(out, bytesOut, "<input type=\"radio\" name=\"searchType1\"");
      if(searchType == 'D')
        scoutln(out, bytesOut, " checked='checked'");
      scoutln(out, bytesOut, " />Description &#160; <input type=\"radio\" name=\"searchType1\"");
      if(searchType == 'C')
        scoutln(out, bytesOut, " checked='checked'");
      scoutln(out, bytesOut, " />Manufacturer Code\n");
    }
    else
    {
      if(numRecs > 0)
      {
        scoutln(out, bytesOut, "<input type=\"radio\" name=\"searchType2\"");
        if(searchType == 'D')
          scoutln(out, bytesOut, " checked='checked'");
        scoutln(out, bytesOut, " />Description &#160; <input type=\"radio\" name=\"searchType2\"");
        if(searchType == 'C')
          scoutln(out, bytesOut, " checked='checked'");
        scoutln(out, bytesOut, " />Manufacturer Code");
      }
    }

    scoutln(out, bytesOut, "</p></font></td></tr>\n");
    scoutln(out, bytesOut, "<tr><td><img src=\"" + imagesDir + "z402.gif\"></td></tr>");
    scoutln(out, bytesOut, "<tr bgcolor=\"#C0C0C0\"><td nowrap='nowrap' colspan=\"3\"><p>\n");

    String arg = "";
    int len = lastCodeOnPage.length();
    for(int y=0;y<len;++y)
    {
      if(lastCodeOnPage.charAt(y) == '\'')
        arg += "\\'";
      else arg += lastCodeOnPage.charAt(y);
    }
      
    if(first)
    {
      scoutln(out, bytesOut, "<a href=\"javascript:page('" + mfr + "','F','" + newFirstRecNum + "','" + newLastRecNum + "','','','T','" + numRecs
                           + "')\">Search</a> for ");
    }
    else
    {
      if(numRecs > 0)
      {
        scoutln(out, bytesOut, "<a href=\"javascript:page('" + mfr + "','F','" + newFirstRecNum + "','" + newLastRecNum + "','','','B','" + numRecs
                             + "')\">Search</a> for ");
      }
    }

    if(first)
    {
     scoutln(out, bytesOut, "&nbsp;<input type=text size=20 maxlength=20 value=\"" + srchStr + "\" name=srchStr1>");
     scoutln(out, bytesOut, "&nbsp;&nbsp;PageSize&nbsp;&nbsp;<input type=text size=4 maxlength=4 value=\"" + maxRows
                       + "\" name=maxRows1></td></tr><tr><td><img src=\"" + imagesDir + "z402.gif\">");
    }
    else 
    {
      if(numRecs > 0)
      {
        scoutln(out, bytesOut, "&nbsp;<input type=text size=20 maxlength=20 value=\"" + srchStr + "\" name=srchStr2>");
        scoutln(out, bytesOut, "&nbsp;&nbsp;PageSize&nbsp;&nbsp;<input type=text size=4 maxlength=4 value=\"" + maxRows
                           + "\" name=maxRows2></td></tr><tr><td><img src=\""+imagesDir+"z402.gif\">");
      }
    }
    
   scoutln(out, bytesOut, "</td></tr>");//</table><table id=\"page\" border=10 width=100% cellspacing=0 cellpadding=0>");

    if(first)
     scoutln(out, bytesOut, "<tr><td><img src=\"" + imagesDir + "z402.gif\"></td></tr>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void startBody(PrintWriter out, boolean first, String imagesDir, int[] bytesOut) throws Exception
  {
    if(first)
    {
      scoutln(out, bytesOut, "<tr><td>");
      scoutln(out, bytesOut, "<table id=\"page\" cellspacing=1 cellpadding=1 width=100%>");
    }
    
    scoutln(out, bytesOut, "<tr><td bgcolor=\"#F0F0F0\" height=18><p>Our Code&nbsp;</td>");
    scoutln(out, bytesOut, "<td bgcolor=\"#F0F0F0\" height=18><p>Manufacturer Code&nbsp;</td>");
    scoutln(out, bytesOut, "<td bgcolor=\"#F0F0F0\" height=18><p>Description</td></tr>");

    if(! first)
     scoutln(out, bytesOut, "</td></tr></table>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void appendBodyLine(String[] html, String itemCode, String desc, String mfrCode, String status) throws Exception
  {
    html[0] += "<tr bgcolor=\"#FFFFFF\"><td nowrap><p><a href=\"javascript:view('" + itemCode + "')\">" + itemCode + "</a>";
    if(status.equals("C"))
      html[0] += "<font size=1 color=red><sup> Replaced</sup></font>";
          
    html[0] += "</td><td nowrap valign=top><p>" + mfrCode + "</td><td><p>" + desc + "</td></tr>";
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void prependBodyLine(String[] html, String itemCode, String desc, String mfrCode, String status) throws Exception
  {
    String line = "<tr bgcolor=\"#FFFFFF\"><td nowrap><p><a href=\"javascript:view('" + itemCode + "')\">" + itemCode + "</a>";
    if(status.equals("C"))
      line += "<font size=1 color=red><sup> Replaced</sup></font>";
          
    line += "</td><td nowrap valign=top><p>" + mfrCode + "</td><td><p>" + desc + "</td></tr>";

    html[0] = line + "\n" + html[0];
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
