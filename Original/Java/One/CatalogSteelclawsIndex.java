// =======================================================================================================================================================================================================
// System: ZaraStar Catalog: For an SC catalog product index, linked-in user
// Module: CatalogSteelclawsIndex.java
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

public class CatalogSteelclawsIndex extends HttpServlet
{
  GeneralUtils  generalUtils  = new GeneralUtils();
  MessagePage  messagePage  = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  Profile  profile  = new Profile();
  CatalogSteelclawsLinkedUser catalogSteelclawsLinkedUser = new CatalogSteelclawsLinkedUser();
  
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    Writer r = null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", operation="", firstRecNum="", lastRecNum="", firstCodeOnPage="", lastCodeOnPage="", numRecs="", mfr="", userName="", catalogURL="", catalogUpline="", remoteSID="", userType="",
           whichIndex="";

    try
    {
      res.setContentType("text/xml");
      res.setHeader("Cache-Control", "no-cache");
      r = res.getWriter();

      int len = req.getContentLength();
      ServletInputStream in  = req.getInputStream();
      byte[] b = new byte[len];
      
      in.readLine(b, 0, len);          
          
      String name, value;
      int x=0;
      while(x < len)
      {
        ++x; // &
        name="";
        while(x < len && b[x] != '=')
          name += (char)b[x++];
        
        ++x; // =
        value="";
        while(x < len && b[x] != '&')
          value += (char)b[x++];
        value = generalUtils.deSanitise(value);
          
        if(name.equals("unm"))
          unm = value;
        else
        if(name.equals("uty"))
          uty = value;
        else
        if(name.equals("sid"))
          sid = value;
        else
        if(name.equals("dnm"))
          dnm = value;
        else
        if(name.equals("bnm"))
          bnm = value;
        else
        if(name.equals("p1")) // whichIndex
          whichIndex = value;
        else
        if(name.equals("p31")) // mfr
          mfr = value;
        else
        if(name.equals("p32")) // operation
          operation = value;
        else
        if(name.equals("p9")) // remoteSID
          remoteSID = value;
        else
        if(name.equals("p10")) // catalogURL
          catalogURL = value;
        else
        if(name.equals("p11")) // catalogUpline
          catalogUpline = value;
        else
        if(name.equals("p14")) // userType
          userType = value;
        else
        if(name.equals("p15")) // userName
          userName = value;
        else
        if(name.equals("p34")) // firstRecNum
          firstRecNum = value;
        else
        if(name.equals("p35")) // lastRecNum
          lastRecNum = value;
        else
        if(name.equals("p37")) // numRecs
          numRecs = value;
        else
        if(name.equals("p38")) // firstCodeOnPage
          firstCodeOnPage = value;
        else
        if(name.equals("p39")) // lastCodeOnPage
          lastCodeOnPage = value;
      }
      
      if(numRecs == null || numRecs.length() == 0) numRecs = "-1";

      if(firstCodeOnPage == null) firstCodeOnPage = "";
      firstCodeOnPage    = new String(firstCodeOnPage.getBytes("ISO-8859-1"), "UTF-8");

      if(lastCodeOnPage == null) lastCodeOnPage = "";
      lastCodeOnPage    = new String(lastCodeOnPage.getBytes("ISO-8859-1"), "UTF-8");
            
      doIt(r, req, unm, uty, dnm, whichIndex, mfr, operation, firstRecNum, lastRecNum, firstCodeOnPage, lastCodeOnPage, generalUtils.strToInt(numRecs), userName, catalogURL, catalogUpline, remoteSID, userType, bytesOut);
    }
    catch(Exception e)
    {
      serverUtils.etotalBytes(req, unm, dnm, 2005, bytesOut[0], 0, "ERR:");
      try {  scoutln(r, bytesOut, "ERR:2005m " + e); } catch (Exception e2) { } 
      r.flush();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(Writer r, HttpServletRequest req, String unm, String uty, String dnm, String whichIndex, String mfr, String operation, String firstRecNum,
                    String lastRecNum, String firstCodeOnPage, String lastCodeOnPage, int numRecs, String userName, String catalogURL,
                    String catalogUpline, String remoteSID, String userType, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(catalogUpline);
    String imagesDir     = directoryUtils.getSupportDirs('I');
    String imagesLibDir     = directoryUtils.getImagesDir(dnm);
    
    String uName = directoryUtils.getMySQLUserName();
    String pWord = directoryUtils.getMySQLPassWord();
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + catalogUpline + "_ofsa?user=" + uName + "&password=" + pWord);
    Statement stmt = null, stmt2 = null;
    ResultSet rs   = null, rs2   = null;

    if(catalogUpline.equals("Catalogs") || userType.equals("R")) // remote server
    {
      if(! serverUtils.checkSID(userName, remoteSID, uty, catalogUpline, localDefnsDir, defnsDir))
      {
        serverUtils.etotalBytes(req, userName, catalogUpline, 2005, bytesOut[0], 0, "SID:2005m");
        if(con != null) con.close();
        r.flush();
        return;
      }
    }
    else
    {
      if(! serverUtils.checkSID(unm, remoteSID, uty, catalogUpline, localDefnsDir, defnsDir))
      {
        serverUtils.etotalBytes(req, unm, catalogUpline, 2005, bytesOut[0], 0, "SID:2005m");
        if(con != null) con.close();
        r.flush();
        return;
      }
    }

    if(mfr == null || mfr.length() == 0 ) mfr = "";

    
    generate(con, stmt, stmt2, rs, rs2, r, whichIndex, operation.charAt(0), mfr, firstRecNum, lastRecNum, firstCodeOnPage, lastCodeOnPage, numRecs, catalogURL, catalogUpline, remoteSID, imagesDir, imagesLibDir, defnsDir, bytesOut);
    
    serverUtils.totalBytes(con, stmt, rs, req, unm, catalogUpline, 2005, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con != null) con.close();
    r.flush();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void generate(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, Writer r, String whichIndex, char operation,
                        String mfr, String firstRecNum, String lastRecNum, String currFirstCodeOnPage, String currLastCodeOnPage, int numRecsIn,
                        String catalogURL, String catalogUpline, String remoteSID, String imagesDir, String imagesLibDir, String defnsDir,
                        int[] bytesOut) throws Exception
  {
    int[] numRecs = new int[1];  numRecs[0] = numRecsIn;

    String[] newFirstCodeOnPage = new String[1]; newFirstCodeOnPage[0] = "";
    String[] newLastCodeOnPage  = new String[1];
    String[] newFirstRecNum     = new String[1];
    String[] newLastRecNum      = new String[1];

    String[] html = new String[1]; html[0] = "";
    createPage(con, stmt, stmt2, rs, rs2, html, whichIndex, operation, mfr, numRecs, newFirstCodeOnPage, newLastCodeOnPage, currFirstCodeOnPage,
               currLastCodeOnPage, firstRecNum, lastRecNum, newFirstRecNum, newLastRecNum, remoteSID);

    if(generalUtils.strToInt(newFirstRecNum[0]) <= 0) // prev call has gone back before rec 1
    {  
      newFirstRecNum[0] = "1";
      newLastRecNum[0] = "50";
    }


    scoutln(r, bytesOut, "<div id='catalogPage'>");
    scoutln(r, bytesOut, "<form>");

    catalogSteelclawsLinkedUser.outputOptions(con, stmt2, rs2, r, mfr, remoteSID, catalogURL, catalogUpline, defnsDir, bytesOut);
 
    setNav(r, true, whichIndex, mfr, numRecs[0], newFirstCodeOnPage[0], newLastCodeOnPage[0], imagesDir, newFirstRecNum[0], newLastRecNum[0],
           catalogURL, remoteSID, catalogUpline, bytesOut);

    if(numRecs[0] > 0)
      startBody(r, true, bytesOut);

    scoutln(r, bytesOut, html[0]);

    if(numRecs[0] > 0)
      startBody(r, false, bytesOut);
    
    setNav(r, false, whichIndex, mfr, numRecs[0], newFirstCodeOnPage[0], newLastCodeOnPage[0], imagesDir, newFirstRecNum[0], newLastRecNum[0],
           catalogURL, remoteSID, catalogUpline, bytesOut);

    scoutln(r, bytesOut, "</div>");
    scoutln(r, bytesOut, "</table></form>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void createPage(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String[] html, String whichIndex,
                          char operation, String mfr2, int[] numRecs, String[] newFirstCodeOnPage, String[] newLastCodeOnPage, String firstCodeOnPage,
                          String lastCodeOnPage, String firstRecNum, String lastRecNum, String[] newFirstRecNum, String[] newLastRecNum, 
                          String remoteSID) throws Exception
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

    // determine num of recs if not already known
    if(numRecs[0] == -1 || operation == 'F')
    {
      rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM catalogl WHERE Manufacturer='" + generalUtils.sanitiseForSQL(mfr) + "'");
      
      rs.next();
      numRecs[0] = rs.getInt("rowcount");
      rs.close();
    }

    String desc="", desc2="", mfrCode="", category="";
    
    String orderBy;
    if(whichIndex.equals("C"))
       orderBy = "ManufacturerCode";
    else orderBy = "Description";
     
    switch(operation)
    {
      case 'F' : // first page
                 q = "SELECT ManufacturerCode, Description, Description2, Category FROM catalogl WHERE Manufacturer='" + generalUtils.sanitiseForSQL(mfr)
                   + "' ORDER BY " + orderBy;
                 
                 stmt.setMaxRows(50);
                 rs = stmt.executeQuery(q);

                 while(rs.next())                  
                 {
                   mfrCode  = rs.getString(1);
                   desc     = rs.getString(2);
                   desc2    = rs.getString(3);
                   category = rs.getString(4);

                   appendBodyLine(con, stmt2, rs2, html, mfr, mfrCode, desc, desc2, category, remoteSID);
          
                   if(count++ == 1)
                     newFirstCodeOnPage[0] = mfrCode;
                 }
                 
                 if(rs != null) rs.close();
                                 
                 newLastCodeOnPage[0] = mfrCode;
 
                 newFirstRecNum[0] = "1";
                 newLastRecNum[0]  = generalUtils.intToStr(count - 1);
                 break;
      case 'L' : // last page
                 q = "SELECT ManufacturerCode, Description, Description2, Category FROM catalogl WHERE Manufacturer='" + generalUtils.sanitiseForSQL(mfr)
                   + "' ORDER BY " + orderBy + " DESC";

                 stmt.setMaxRows(50);
                 rs = stmt.executeQuery(q);
  
                 while(rs.next())                  
                 {
                   mfrCode  = rs.getString(1);
                   desc     = rs.getString(2);
                   desc2    = rs.getString(3);
                   category = rs.getString(4);

                   prependBodyLine(con, stmt2, rs2, html, mfr, mfrCode, desc, desc2, category, remoteSID);
          
                   if(count++ == 1)
                     newLastCodeOnPage[0] = mfrCode;
                 }
                 
                 if(rs != null) rs.close();
                                 
                 newFirstCodeOnPage[0]  = mfrCode;

                 newFirstRecNum[0] = generalUtils.intToStr(numRecs[0] - count + 2);
                 newLastRecNum[0]  = generalUtils.intToStr(numRecs[0]);
                 break;
      case 'N' : // next page
                 q = "SELECT ManufacturerCode, Description, Description2, Category FROM catalogl WHERE Manufacturer='" + generalUtils.sanitiseForSQL(mfr)
                   + "' AND ManufacturerCode > '" + lastCodeOnPage + "' ORDER BY " + orderBy;

                 stmt.setMaxRows(50);
                 rs = stmt.executeQuery(q);

                 while(rs.next())                  
                 {
                   mfrCode  = rs.getString(1);
                   desc     = rs.getString(2);
                   desc2    = rs.getString(3);
                   category = rs.getString(4);
                                                      
                   appendBodyLine(con, stmt2, rs2, html, mfr, mfrCode, desc, desc2, category, remoteSID);

                   if(count++ == 1)
                     newFirstCodeOnPage[0] = mfrCode;
                 }
                 
                 if(rs != null) rs.close();
                                 
                 newLastCodeOnPage[0] = mfrCode;

                 newFirstRecNum[0] = generalUtils.intToStr(generalUtils.intFromStr(lastRecNum) + 1);
                 newLastRecNum[0]  = generalUtils.intToStr(generalUtils.intFromStr(lastRecNum) + count - 1);
                 break;
      case 'P' : // prev page
                 q = "SELECT ManufacturerCode, Description, Description2, Category FROM catalogl WHERE Manufacturer='" + generalUtils.sanitiseForSQL(mfr)
                   + "' AND ManufacturerCode < '" + firstCodeOnPage + "' ORDER BY " + orderBy + " DESC";
                 
                 stmt.setMaxRows(50);
                 rs = stmt.executeQuery(q);

                 while(rs.next())                  
                 {
                   mfrCode  = rs.getString(1);
                   desc     = rs.getString(2);
                   desc2    = rs.getString(3);
                   category = rs.getString(4);
                                                      
                   prependBodyLine(con, stmt2, rs2, html, mfr, mfrCode, desc, desc2, category, remoteSID);
          
                   if(count++ == 1)
                     newLastCodeOnPage[0] = mfrCode;
                 }

                 if(rs != null) rs.close();
                                 
                 newFirstCodeOnPage[0] = mfrCode;

                 newFirstRecNum[0] = generalUtils.intToStr(generalUtils.intFromStr(firstRecNum) - count   + 1);
                 newLastRecNum[0]  = generalUtils.intToStr(generalUtils.intFromStr(firstRecNum) - 1);
                 break;
    }
  
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void setNav(Writer r, boolean first, String whichIndex, String mfr, int numRecs, String firstCodeOnPage, String lastCodeOnPage,
                      String imagesDir, String newFirstRecNum, String newLastRecNum,  String catalogURL, String remoteSID, String catalogUpline,
                      int[] bytesOut) throws Exception
  {
    if(first)
      scoutln(r, bytesOut, "<table id='catalogList'>");

    scoutln(r, bytesOut, "<tr><td height='20' nowrap='nowrap' colspan='3'><p>");
    
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

    if(newFirstRecNumI > 1)
    {
      scoutln(r, bytesOut, "&#160;<a href=\"javascript:page2005n('" + whichIndex + "','" + mfr + "','F','" + newFirstRecNum + "','" + newLastRecNum
                         + "','','','" + numRecs + "','" + remoteSID + "','" + catalogURL + "','" + catalogUpline + "')\">First</a>");
      
      scoutln(r, bytesOut, "&#160;&#160;<a href=\"javascript:page2005n('" + whichIndex + "','" + mfr + "','P','" + newFirstRecNum + "','"
                         + newLastRecNum + "','" + generalUtils.sanitise(firstCodeOnPage) + "','','" + numRecs + "','" + remoteSID
                         + "','" + catalogURL + "','" + catalogUpline + "')\">Previous</a>");
    }
     
    if(newLastRecNumI < numRecs)
    {
      scoutln(r, bytesOut, "&#160;&#160;<a href=\"javascript:page2005n('" + whichIndex + "','" + mfr + "','N','" + newFirstRecNum + "','"
                         + newLastRecNum + "','','" + generalUtils.sanitise(lastCodeOnPage) + "','" + numRecs + "','" + remoteSID
                         + "','" + catalogURL + "','" + catalogUpline + "')\">Next</a>");
      
      scoutln(r, bytesOut, "&#160;&#160;<a href=\"javascript:page2005n('" + whichIndex + "','" + mfr + "','L','" + newFirstRecNum + "','"
                         + newLastRecNum + "','','','" + numRecs + "','" + remoteSID + "','" + catalogURL + "','" + catalogUpline + "')\">Last</a>");
    }
    
    if(numRecs > 0)
      scoutln(r, bytesOut, "</p></td></tr><tr><td>\n");
    else scoutln(r, bytesOut, "</p>\n");
    
    scoutln(r, bytesOut, "</td></tr><tr><td><img src=\"" + imagesDir + "z402.gif\" /></td></tr>");
    scoutln(r, bytesOut, "<tr><td nowrap='nowrap' colspan='3'><p>\n");

    scoutln(r, bytesOut, "</td></tr>");

    if(first)
     scoutln(r, bytesOut, "<tr><td><img src=\"" + imagesDir + "z402.gif\"/></td></tr>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void startBody(Writer r, boolean first, int[] bytesOut) throws Exception
  {
    if(first)
    {
      scoutln(r, bytesOut, "<tr><td>");
      scoutln(r, bytesOut, "<table id='catalogList'>");
    }
 
    scoutln(r, bytesOut, "<tr><th>Manufacturer Code&#160;</th><th>Description</th></tr>");

    if(! first)
     scoutln(r, bytesOut, "</table></td></tr>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void appendBodyLine(Connection con, Statement stmt, ResultSet rs, String[] html, String mfr, String mfrCode, String desc, String desc2,
                              String category, String remoteSID) throws Exception
  {
    String[] title      = new String[1];
    String[] newProduct = new String[1];
            
    getCategoryDetails(con, stmt, rs, category, mfr, title, newProduct);

    html[0] += "<tr><td nowrap='nowrap'><p><a href=\"javascript:page2005e('" + category + "','" + remoteSID + "','','','')\">"
            + mfrCode + "</a>";

    if(newProduct[0].equals("Y"))
      html[0] += "<span class='new'><sup> &nbsp;&nbsp;New!</sup></span>";
          
    html[0] += "</p></td><td><p>" + generalUtils.handleSuperScripts(title[0] + " " + desc + " " + desc2) + "</p></td></tr>";
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void prependBodyLine(Connection con, Statement stmt, ResultSet rs, String[] html, String mfr, String mfrCode, String desc, String desc2,
                               String category, String remoteSID) throws Exception
  {
    String[] title      = new String[1];
    String[] newProduct = new String[1];
            
    getCategoryDetails(con, stmt, rs, category, mfr, title, newProduct);

    String line = "<tr><td nowrap='nowrap'><p><a href=\"javascript:page2005e('" + category + "','" + remoteSID + "','','','')\">" + mfrCode + "</a>";
    
    if(newProduct[0].equals("Y"))
      line += "<span class='new'><sup> &nbsp;&nbsp;New!</sup></span>";
          
    line += "</p></td><td><p>" + generalUtils.handleSuperScripts(title[0] + " " + desc + " " + desc2) + "</p></td>";

    html[0] = line + "\n" + html[0];
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void getCategoryDetails(Connection con, Statement stmt, ResultSet rs, String category, String mfr, String[] title, String[] newProduct)
                                  throws Exception
  {
    title[0] = newProduct[0] = "";

    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT Title, NewProduct FROM catalog WHERE Manufacturer='" + generalUtils.sanitiseForSQL(mfr) + "' AND Category='"
                           + category + "'"); 
                           
      if(rs.next())
      {
        title[0]      = rs.getString(1);
        newProduct[0] = rs.getString(2);
      }
      
      if(title         == null) title[0] = "";
      if(newProduct[0] == null) newProduct[0] = "N";
      
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
