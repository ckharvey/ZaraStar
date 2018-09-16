// =======================================================================================================================================================================================================
// System: ZaraStar: Catalogs: Fetch number of items in non-steelclaws catalog
// Module: CatalogItemsCountZara.java
// Author: C.K.Harvey
// Copyright (c) 2000-08 Christopher Harvey. All Rights Reserved.
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

public class CatalogItemsCountZara extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
   
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    Writer r = null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="", p3="";

    try
    {
      res.setContentType("text/xml");
      res.setHeader("Cache-Control", "no-cache");
      r = res.getWriter();

      p1  = req.getParameter("p1"); // mfr
      p2  = req.getParameter("p2"); // dnm
      p3  = req.getParameter("p3"); // catalogType

      doIt(r, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, p3, bytesOut);
    }
    catch(Exception e)
    {
      System.out.println("2005j: " + e);
      serverUtils.etotalBytes(req, unm, dnm, 2005, bytesOut[0], 0, "ERR:");
      try {  scoutln(r, bytesOut, "ERR:"); } catch (Exception e2) { } 
      r.flush();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(Writer r, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1,
                    String p2, String p3, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String uName = directoryUtils.getMySQLUserName();
    String pWord = directoryUtils.getMySQLPassWord();
    
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + p2 + "_ofsa?user=" + uName + "&password=" + pWord);
    Statement stmt = null, stmt2 = null;
    ResultSet rs = null,   rs2   = null;

    if(p3.equals("C"))
      scoutln(r, bytesOut, generalUtils.intToStr(forMfr(con, stmt, stmt2, rs, rs2, p1)));
    else scoutln(r, bytesOut, generalUtils.intToStr(getSizesForAListing(con, stmt, rs, p1)));

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 2005, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con != null) con.close();
    r.flush();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private int forMfr(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String mfr) throws Exception
  {
    int[] count = new int[1];  count[0] = 0;
    
    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT CategoryCode FROM stockcat WHERE Manufacturer = '" + generalUtils.sanitiseForSQL(mfr) + "'");

      while(rs.next())
        getSizesForACategory(con, stmt2, rs2, rs.getString(1), count);
    
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
    
    return count[0];
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void getSizesForACategory(Connection con, Statement stmt, ResultSet rs, String categoryCode, int[] catCount) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM stock WHERE CategoryCode = '" + categoryCode + "' AND ShowToWeb = 'Y'");

    if(rs.next())
      catCount[0] += rs.getInt("rowcount");
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private int getSizesForAListing(Connection con, Statement stmt, ResultSet rs, String mfr) throws Exception
  {
    int itemCount = 0;
    
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM stock WHERE Manufacturer = '" + generalUtils.sanitiseForSQL(mfr) + "' AND ShowToWeb = 'Y'");

    if(rs.next())
      itemCount = rs.getInt("rowcount");    
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    return itemCount;
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(Writer r, int bytesOut[], String str) throws Exception
  {      
    r.write(str + "\n");
    bytesOut[0] += (str.length() + 1);    
  }

}
