// =======================================================================================================================================================================================================
// System: ZaraStar Catalog: New Products Page, for a SC catalog
// Module: CatalogNewProductsPageSteelclaws.java
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

public class CatalogNewProductsPageSteelclaws extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
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

    String unm="", pwd="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p9="", p10="", p11="", p14="", p15="";

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
      
      doIt(r, req, unm, pwd, uty, men, den, dnm, bnm, p1, p9, p10, p11, p14, p15, bytesOut);
    }
    catch(Exception e)
    {
      System.out.println("2003o: " + e);
      serverUtils.etotalBytes(req, unm, dnm, 2004, bytesOut[0], 0, "ERR:");
      try {  scoutln(r, bytesOut, "ERR:2003o "           +       e         ); } catch (Exception e2) { } 
      r.flush();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(Writer r, HttpServletRequest req, String unm, String pwd, String uty, String men, String den, String dnm, String bnm, String p1, String p9, String p10, String p11, String p14, String p15, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');//, p11);
    String localDefnsDir = directoryUtils.getLocalOverrideDir(p11);//, p9);
    String catalogImagesDir = directoryUtils.getCatalogImagesDir(p11, p1);

    String userName = directoryUtils.getMySQLUserName();
    String passWord = directoryUtils.getMySQLPassWord();
    
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + p11 + "_ofsa?user=" + userName + "&password=" + passWord);
    Statement stmt = null;
    ResultSet rs   = null;
    
    if(p11.equals("Catalogs") || p14.equals("R")) // remote server
    {
      if(! serverUtils.checkSID(p15, p9, uty, p11, localDefnsDir, defnsDir))
      {
        serverUtils.etotalBytes(req, p15, p11, 2003, bytesOut[0], 0, "SID:2003o");
        if(con  != null) con.close();
        r.flush();
        return;
      }
    }
    else
    {
      if(! serverUtils.checkSID(unm, p9, uty, p11, localDefnsDir, defnsDir))
      {
        serverUtils.etotalBytes(req, p15, p11, 2004, bytesOut[0], 0, "SID:2003o");
        if(con  != null) con.close();
        r.flush();
        return;
      }
    }
    
    set(con, stmt, rs, r, p1, p9, p10, p11, catalogImagesDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, userName, p11, 2003, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con != null) con.close();
    r.flush();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, Writer r, String mfr, String remoteSID, String catalogURL, String catalogUpline,
                   String catalogImagesDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(r, bytesOut, "<form><table id='catalogPage'><tr><td colspan='3'>");

    catalogSteelclawsLinkedUser.outputOptions(con, stmt, rs, r, mfr, remoteSID, catalogURL, catalogUpline, defnsDir, bytesOut);
            
    scoutln(r, bytesOut, "</td></tr>");

    list(con, stmt, rs, r, mfr, remoteSID, catalogURL, catalogImagesDir, bytesOut);

    scoutln(r, bytesOut, "<tr><td>&nbsp;</td></tr>");
  
    scoutln(r, bytesOut, "</table></form>");
  }
    
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void list(Connection con, Statement stmt, ResultSet rs, Writer r, String mfr, String remoteSID, String catalogURL, String catalogImagesDir,
                    int[] bytesOut) throws Exception
  {
    scoutln(r, bytesOut, "<tr>");

    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT Title, Category, ImageSmall FROM catalog WHERE Manufacturer = '" + generalUtils.sanitiseForSQL(mfr)
                           + "' AND NewProduct='Y' ORDER BY Category");

      String title, category, imageSmall="";
      int count = 0;
    
      while(rs.next())
      {
        title      = rs.getString(1);
        category   = rs.getString(2);
        imageSmall = rs.getString(3);

        if(++count == 4)
        {
          scoutln(r, bytesOut, "</tr><tr>");
          count = 1;
        }
        
        scoutln(r, bytesOut, "<td align='center'><p><a href=\"javascript:page2005e('" + category + "','" + remoteSID
                           + "','','','')\"><span id='imageSmall'><img src=\"" + "http://www.xxx.com" + catalogImagesDir + imageSmall
                           + "\" border='1'/><br/><a href=\"javascript:page2005e('" + category + "','" + remoteSID + "','','','')\">"
                           + generalUtils.handleSuperScripts(title) + "</a></span></td>");
      }
    }
    catch(Exception e) { System.out.println("2003o: " + e); }

    scoutln(r, bytesOut, "</tr>");

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
