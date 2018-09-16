// =======================================================================================================================================================================================================
// System: ZaraStar: Product: create items section
// Module: CatalogItemsSection.java
// Author: C.K.Harvey
// Copyright (c) 1999-2008 Christopher Harvey. All Rights Reserved.
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
import java.sql.*;
import java.io.*;

public class CatalogItemsSection extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    int[] bytesOut = new int[1];  bytesOut[0] = 0;
    
    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", operation="", searchType="", srchStr="", firstRecNum="", lastRecNum="", firstCodeOnPage="", lastCodeOnPage="", maxRows="", numRecs="", mfr="";
    try
    {
      directoryUtils.setContentHeaders(res);
      res.setHeader("Header", "HTTP/1.0 200 OK");
      res.setStatus(200);

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

      doIt(req, res, unm, sid, uty, men, den, dnm, bnm, mfr, operation, searchType, srchStr, firstRecNum, lastRecNum, firstCodeOnPage, lastCodeOnPage, generalUtils.strToInt(maxRows), generalUtils.strToInt(numRecs), bytesOut);
    }
    catch(Exception e)
    {
      serverUtils.etotalBytes(req, unm, dnm, 2008, bytesOut[0], 0, "ERR:");
      System.out.println(("Unexpected System Error: 2008a: " + e));
      res.getWriter().write("Unexpected System Error: 2008a");
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(HttpServletRequest req, HttpServletResponse res, String unm, String sid, String uty, String men, String den,
                    String dnm, String bnm, String mfr, String operation, String searchType, String srchStr, String firstRecNum, String lastRecNum,
                    String firstCodeOnPage, String lastCodeOnPage, int maxRows, int numRecs, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    res.setContentType("text/xml");
    res.setHeader("Cache-Control", "no-cache");

    Writer r = res.getWriter();

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      r.write("Access Denied - Duplicate SignOn or Session Timeout");
      serverUtils.etotalBytes(req, unm, dnm, 2008, bytesOut[0], 0, "SID:");
      return;
    }

    if(mfr == null || mfr.length() == 0 ) mfr = "";

    if(srchStr == null || srchStr.equalsIgnoreCase("null"))
      srchStr = "";

    if(searchType == null || searchType.equalsIgnoreCase("null") || searchType.length() == 0)
      searchType = "C";

    generate(con, stmt, rs, r, unm, sid, uty, men, den, dnm, bnm, operation.charAt(0), searchType.charAt(0), mfr, srchStr, firstRecNum, lastRecNum,
             firstCodeOnPage, lastCodeOnPage, maxRows, numRecs, imagesDir, localDefnsDir, defnsDir, bytesOut);
    
//    r.write("</table>");

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 2008, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con  != null) con.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void generate(Connection con, Statement stmt, ResultSet rs, Writer r, String unm, String sid, String uty, String men, String den, String dnm, String bnm, char operation,
                        char searchType, String mfr, String srchStr, String firstRecNum, String lastRecNum, String currFirstCodeOnPage,
                        String currLastCodeOnPage, int maxRows, int numRecsIn, String imagesDir, String localDefnsDir, String defnsDir,
                        int[] bytesOut) throws Exception
  {
    int[] numRecs = new int[1];  numRecs[0] = numRecsIn;

    String[] newFirstCodeOnPage = new String[1];
    String[] newLastCodeOnPage  = new String[1];
    String[] newFirstRecNum     = new String[1];
    String[] newLastRecNum      = new String[1];
 
    String[] html = new String[1]; html[0] = "";
    createPage(html, operation, searchType, mfr, srchStr, numRecs, newFirstCodeOnPage, newLastCodeOnPage, currFirstCodeOnPage, currLastCodeOnPage,
               firstRecNum, lastRecNum, newFirstRecNum, newLastRecNum, maxRows, dnm, localDefnsDir, defnsDir);

    if(generalUtils.strToInt(newFirstRecNum[0]) <= 0) // prev call has gone back before rec 1
    {  
      newFirstRecNum[0] = "1";
      newLastRecNum[0] = "50";
    }

    setHead(con, stmt, rs, r, unm, dnm, newFirstRecNum[0], newLastRecNum[0], numRecs[0], mfr, defnsDir, bytesOut);

    setNav(r, true, mfr, operation, searchType, numRecs[0], newFirstCodeOnPage[0], newLastCodeOnPage[0], srchStr, imagesDir, newFirstRecNum[0],
           newLastRecNum[0], maxRows, unm, sid, uty, men, den, dnm, bnm, bytesOut);

    if(numRecs[0] > 0)
      startBody(r, true, imagesDir, bytesOut);

    scout(r, bytesOut, html[0]);

    if(numRecs[0] > 0)
      startBody(r, false, imagesDir, bytesOut);
    
    setNav(r, false, mfr, operation, searchType, numRecs[0], newFirstCodeOnPage[0], newLastCodeOnPage[0], srchStr, imagesDir, newFirstRecNum[0],
           newLastRecNum[0], maxRows, unm, sid, uty, men, den, dnm, bnm, bytesOut);

    scout(r, bytesOut, "</table></form>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void createPage(String[] html, char operation, char searchType, String mfr2, String srchStr, int[] numRecs, String[] newFirstCodeOnPage,
                          String[] newLastCodeOnPage, String firstCodeOnPage, String lastCodeOnPage, String firstRecNum, String lastRecNum,
                          String[] newFirstRecNum, String[] newLastRecNum, int maxRows, String dnm, String localDefnsDir, String defnsDir)
                          throws Exception
  {
    String userName = directoryUtils.getMySQLUserName();
    String passWord = directoryUtils.getMySQLPassWord();
    
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + userName + "&password=" + passWord);
    Statement stmt = con.createStatement();
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

    ResultSet rs = null;

    // determine num of recs if not already known, or if doing a search and hence must re-determine
    if(numRecs[0] == -1 || operation == 'F')
    {
      if(searchType == 'C')
      {
        rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM stock WHERE Manufacturer='" + generalUtils.sanitiseForSQL(mfr)
                             + "' AND ManufacturerCode LIKE '" + generalUtils.sanitiseForSQL(srchStr) + "%'");
      }
      else
      {
        rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM stock WHERE Manufacturer='" + generalUtils.sanitiseForSQL(mfr) + "' AND Description LIKE '%"
                             + generalUtils.sanitiseForSQL(srchStr) + "%'");
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
                     + "' AND ManufacturerCode LIKE '" + generalUtils.sanitiseForSQL(srchStr) + "%' ORDER BY ManufacturerCode";
                 }
                 else
                 {
                   q = "SELECT ItemCode, Description, ManufacturerCode, Status FROM stock WHERE Manufacturer='" + generalUtils.sanitiseForSQL(mfr)
                     + "' AND Description LIKE '%" + generalUtils.sanitiseForSQL(srchStr) + "%' ORDER BY ManufacturerCode";
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
                     + "' ORDER BY ManufacturerCode DESC";
                 }
                 else
                 {
                   q = "SELECT ItemCode, Description, ManufacturerCode, Status FROM stock WHERE Manufacturer='" + generalUtils.sanitiseForSQL(mfr)
                     + "' AND Description LIKE '%" + generalUtils.sanitiseForSQL(srchStr) + "%"//' AND ManufacturerCode > '" + codeArg
                     + "' ORDER BY ManufacturerCode DESC";
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
                     + "' ORDER BY ManufacturerCode";
                 }
                 else
                 {
                   q = "SELECT ItemCode, Description, ManufacturerCode, Status FROM stock WHERE Manufacturer='" + generalUtils.sanitiseForSQL(mfr)
                     + "' AND Description LIKE '%" + generalUtils.sanitiseForSQL(srchStr) + "%' AND ManufacturerCode > '" + lastCodeOnPage
                     + "' ORDER BY ManufacturerCode";
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
                     + "' ORDER BY ManufacturerCode DESC";
                 }
                 else
                 {  
                   q = "SELECT ItemCode, Description, ManufacturerCode, Status FROM stock WHERE Manufacturer='" + generalUtils.sanitiseForSQL(mfr)
                     + "' AND Description LIKE '%" + generalUtils.sanitiseForSQL(srchStr) + "%' AND ManufacturerCode < '" + firstCodeOnPage//codeArg
                     + "' ORDER BY ManufacturerCode DESC";
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
    con.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void setHead(Connection con, Statement stmt, ResultSet rs, Writer r, String unm, String dnm, String newFirstRecNum, String newLastRecNum, int numRecs, String mfr,
                         String defnsDir, int[] bytesOut) throws Exception
  {
    scout(r, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");
    
    scout(r, bytesOut, "<form>");
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void setNav(Writer r, boolean first, String mfr, char operation, char searchType, int numRecs, String firstCodeOnPage,
                      String lastCodeOnPage, String srchStr, String imagesDir, String newFirstRecNum, String newLastRecNum, int maxRows, String unm,
                      String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    if(first)
    {
      scout(r, bytesOut, "<table id=\"pageColumn\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\">\n");
    
      scout(r, bytesOut, "<tr bgcolor=\"#FFFFFF\"><td colspan=\"3\"><font face=\"Arial,Helvetica\" size=\"2\" color=\"#000000\">"
                       + "<b>Manufacturer Items for " + mfr + "</b></font></td></tr>\n");
    }

    scout(r, bytesOut, "<tr bgcolor=\"#C0C0C0\"><td height=\"20\" nowrap='nowrap' colspan=\"3\">"
                     + "<font face=\"Arial,Helvetica\" size=\"2\" color=\"#000000\"><p>\n");
    
    int newFirstRecNumI = generalUtils.strToInt(newFirstRecNum);
    int newLastRecNumI  = generalUtils.strToInt(newLastRecNum);

    if(numRecs == 0)
    {
      if(first)
        scout(r, bytesOut, "No Records");
    }
    else
    {
      scout(r, bytesOut, "Records " + newFirstRecNum + " to " + newLastRecNum + " of " + numRecs);
      if(newFirstRecNumI > 1 || newLastRecNumI < numRecs)
        scout(r, bytesOut, "<img src=\"" + imagesDir + "d.gif\" />\n");
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
            
      scout(r, bytesOut, "&#160;<a href=\"javascript:page('" + mfr + "','F','" + newFirstRecNum + "','" + newLastRecNum + "','','','" + topOrBottom
                       + "','" + numRecs + "')\">First</a>\n");
      scout(r, bytesOut, "&#160;&#160;<a href=\"javascript:page('" + mfr + "','P','" + newFirstRecNum + "','" + newLastRecNum + "','"
                        + generalUtils.sanitise(firstCodeOnPage) + "','','" + topOrBottom + "','" + numRecs + "')\">Previous</a>\n");
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

      scout(r, bytesOut, "&#160;&#160;<a href=\"javascript:page('" + mfr + "','N','" + newFirstRecNum + "','" + newLastRecNum
                       + "','','" + generalUtils.sanitise(lastCodeOnPage) + "','" + topOrBottom + "','" + numRecs + "')\">Next</a>\n");
      scout(r, bytesOut, "&#160;&#160;<a href=\"javascript:page('" + mfr + "','L','" + newFirstRecNum + "','" + newLastRecNum
                       + "','','','" + topOrBottom + "','" + numRecs + "')\">Last</a>\n");
    }
    
    if(numRecs > 0)
      scout(r, bytesOut, "</p></font></td></tr><tr><td>\n");
    else scout(r, bytesOut, "</p></font>\n");

    scout(r, bytesOut, "</td></tr><tr bgcolor=\"#C0C0C0\"><td  nowrap='nowrap' colspan=\"3\"><font face=\"Arial,Helvetica\" size=\"2\" color=\"#000000\"><p>\n");

    if(first)
    {
      scout(r, bytesOut, "<input type=\"radio\" name=\"searchType1\"");
      if(searchType == 'D')
        scout(r, bytesOut, " checked='checked'");
      scout(r, bytesOut, " />Description &#160; <input type=\"radio\" name=\"searchType1\"");
      if(searchType == 'C')
        scout(r, bytesOut, " checked='checked'");
      scout(r, bytesOut, " />Manufacturer Code\n");
    }
    else
    {
      if(numRecs > 0) // would need changes to page() call similar to 2020
      {
        scout(r, bytesOut, "<input type=\"radio\" name=\"searchType2\"");
        if(searchType == 'D')
          scout(r, bytesOut, " checked='checked'");
        scout(r, bytesOut, " />Description &#160; <input type=\"radio\" name=\"searchType2\"");
        if(searchType == 'C')
          scout(r, bytesOut, " checked='checked'");
        scout(r, bytesOut, " />Manufacturer Code");
      }
    }

    scout(r, bytesOut, "</p></font></td></tr><tr bgcolor=\"#C0C0C0\"><td nowrap='nowrap' colspan=\"3\">"
                     + "<font face=\"Arial,Helvetica\" size=\"2\" color=\"#000000\"><p>\n");
  
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
      scout(r, bytesOut, "<a href=\"javascript:page('" + mfr + "','F','" + newFirstRecNum + "','" + newLastRecNum + "','','','T','" + numRecs
                       + "')\">Search</a> for ");
    }
    else
    {
      scout(r, bytesOut, "<a href=\"javascript:page('" + mfr + "','F','" + newFirstRecNum + "','" + newLastRecNum + "','','','B','" + numRecs
                       + "')\">Search</a> for ");
    }
    
    if(first)
    {
      scout(r, bytesOut, "&#160;<input type=\"text\" size=\"20\" maxlength=\"20\" value=\"" + srchStr + "\" name=\"srchStr1\" />\n");
      scout(r, bytesOut, "&#160;&#160;PageSize&#160;&#160;<input type=\"text\" size=\"4\" maxlength=\"4\" value=\"" + maxRows
                       + "\" name=\"maxRows1\" /></p></font></td></tr><tr><td><img src=\"" + imagesDir + "z402.gif\" />\n");
    }
    else 
    {
      scout(r, bytesOut, "&#160;<input type=\"text\" size=\"20\" maxlength=\"20\" value=\"" + srchStr + "\" name=\"srchStr2\" />\n");
      scout(r, bytesOut, "&#160;&#160;PageSize&#160;&#160;<input type=\"text\" size=\"4\" maxlength=\"4\" value=\"" + maxRows
                       + "\" name=\"maxRows2\"></p></font></td></tr><tr><td><img src=\"" + imagesDir + "z402.gif\" />\n");
    }
    
    scout(r, bytesOut, "</td></tr>");

    if(first)
      scout(r, bytesOut, "<tr><td><img src=\"" + imagesDir + "z402.gif\" /></td></tr>\n");
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void startBody(Writer r, boolean first, String imagesDir, int[] bytesOut) throws Exception
  {
    if(first)
    {
      scout(r, bytesOut, "<tr><td><table id=\"page\" cellspacing=\"1\" cellpadding=\"1\" width=\"100%\">\n");
    }

    scout(r, bytesOut, "<tr><td bgcolor=\"#F0F0F0\" height=\"18\"><font face=\"Arial, Helvetica\" color=\"#000000\" size=\"2\">Our Code&#160;</font></td>\n");
    scout(r, bytesOut, "<td bgcolor=\"#F0F0F0\" height=\"18\"><font face=\"Arial, Helvetica\" color=\"#000000\" size=\"2\">Manufacturer Code&#160;</font></td>\n");
    scout(r, bytesOut, "<td bgcolor=\"#F0F0F0\" height=\"18\"><font face=\"Arial, Helvetica\" color=\"#000000\" size=\"2\">Description</font></td></tr>\n");

    if(! first)
      scout(r, bytesOut, "</td></tr></table>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void appendBodyLine(String[] html, String itemCode, String desc, String mfrCode, String status) throws Exception
  {
    if(itemCode == null) itemCode = "";
    if(mfrCode  == null) mfrCode = "";
    if(desc     == null) desc = "";
    if(status   == null) status = "L";
        
    html[0] += "<tr bgcolor=\"#FFFFFF\"><td  nowrap='nowrap'><a href=\"javascript:setCode('" + itemCode
            + "')\"><font face=\"Arial, Helvetica\" color=\"#000000\" size=\"2\">" + itemCode + "</font></a>";
    
    if(status.equals("C"))
      html[0] += "<font size=\"3\" color=\"red\"> * </font>";
          
    html[0] += "</td><td  nowrap='nowrap' valign=\"top\"><font face=\"Arial, Helvetica\" color=\"#000000\" size=\"2\">" + mfrCode + "</font></td>"
            + "<td><font face=\"Arial, Helvetica\" color=\"#000000\" size=\"2\">" + desc + "</font></td></tr>";
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void prependBodyLine(String[] html, String itemCode, String desc, String mfrCode, String status) throws Exception
  {
    String line = "<tr bgcolor=\"#FFFFFF\"><td  nowrap='nowrap' xxxvalign=\"top\"><a href=\"javascript:setCode('" + itemCode
                + "')\"><font face=\"Arial, Helvetica\" color=\"#000000\" size=\"2\">" + itemCode + "</font></a>\n";
    if(status.equals("C"))
      line += "<font size=\"3\" color=\"red\"> * </font>";
          
    line += "</td><td  nowrap='nowrap' valign=\"top\"><font face=\"Arial, Helvetica\" color=\"#000000\" size=\"2\">" + mfrCode + "</font></td>"
         + "<td><font face=\"Arial, Helvetica\" color=\"#000000\" size=\"2\">" + desc + "</font></td></tr>\n";

    html[0] = line + "\n" + html[0];
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void scout(Writer r, int bytesOut[], String str) throws Exception
  {
    r.write(str);
    bytesOut[0] += str.length();
  }

}
