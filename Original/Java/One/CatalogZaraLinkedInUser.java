// =======================================================================================================================================================================================================
// System: ZaraStar Catalog: For an internal catalog, linked-in user
// Module: CatalogZaraLinkedInUser.java
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

public class CatalogZaraLinkedInUser extends HttpServlet
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

    String unm="", p1="", p9="", p11="", p14="", p15="", uty="";

    try
    {
      res.setContentType("text/xml");
      res.setHeader("Cache-Control", "no-cache");
      r = res.getWriter();

      unm = req.getParameter("unm");
      uty = req.getParameter("uty");
      p1  = req.getParameter("p1");  // mfr
      p9  = req.getParameter("p9");  // remoteSID
      p11 = req.getParameter("p11"); // catalogUpline
      p14 = req.getParameter("p14"); // userType
      p15 = req.getParameter("p15"); // userName
      
      doIt(r, req, unm, p1, p9, p11, p14, p15, uty, bytesOut);
    }
    catch(Exception e)
    {
      serverUtils.etotalBytes(req, unm, p11, 2005, bytesOut[0], 0, "ERR2005g:");
      try {  scoutln(r, bytesOut, "ERR:"); } catch (Exception e2) { } 
      r.flush();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(Writer r, HttpServletRequest req, String unm, String p1, String p9, String p11, String p14, String p15, String uty, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');//, p11);
    String localDefnsDir = directoryUtils.getLocalOverrideDir(p11);//, unm);

    String userName = directoryUtils.getMySQLUserName();
    String passWord = directoryUtils.getMySQLPassWord();
    
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + p11 + "_ofsa?user=" + userName + "&password=" + passWord);
    Statement stmt = null;
    ResultSet rs   = null;
    

    if(p14.equals("L")) // local
    {
      if(! serverUtils.checkSID(unm, p9, uty, p11, localDefnsDir, defnsDir))
      {
        serverUtils.totalBytes(con, stmt, rs, req, unm, p11, 2005, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "SID:2005g");
        if(con  != null) con.close();
        r.flush();
        return;
      }
    }
    else // remote
    {
      if(! serverUtils.checkSID(p15, p9, uty, p11, localDefnsDir, defnsDir))
      {
        serverUtils.totalBytes(con, stmt, rs, req, p15, p11, 2005, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "SID:2005g");
        if(con  != null) con.close();
        r.flush();
        return;
      }
    }
    
    set(con, stmt, rs, r, p1, p9, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, p15, p11, 2005, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con != null) con.close();
    r.flush();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, Writer r, String mfr, String remoteSID, int[] bytesOut) throws Exception
  {
    scoutln(r, bytesOut, "<form><table id=\"page\" width=100% border=0>");

    scoutln(r, bytesOut, "<tr><td nowrap colspan=3><h1>Select a Catalog Page</td></tr>");
    
    listPages(con, stmt, rs, r, mfr, remoteSID, bytesOut);
    
    scoutln(r, bytesOut, "<tr><td>&nbsp;</td></tr>");
  
    scoutln(r, bytesOut, "</table></form>");
  }
    
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void listPages(Connection con, Statement stmt, ResultSet rs, Writer r, String mfr, String remoteSID, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT Page, Description FROM stockcat WHERE Manufacturer = '" + generalUtils.sanitiseForSQL(mfr) + "' ORDER BY Page, Description");

    String page, desc, lastPage="";
    
    while(rs.next())
    {
      page = rs.getString(1);
      desc = rs.getString(2);
      
      if(page.equals(lastPage))
        scoutln(r, bytesOut, "<tr><td></td>");
      else
      {
        scoutln(r, bytesOut, "<tr><td nowrap><p><a href=\"javascript:page2005g('" + page + "','" + remoteSID + "')\">Page " + page + "</a></td>");
        lastPage = page;
      }
      
      scoutln(r, bytesOut, "<td nowrap width=99%><p>" + desc + "</a></td></tr>");
    }
    
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
