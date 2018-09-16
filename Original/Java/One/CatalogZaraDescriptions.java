// =======================================================================================================================================================================================================
// System: ZaraStar: Catalogs: Fetch descs from a non-SC catalog/listing
// Module: CatalogZaraDescriptions.java
// Author: C.K.Harvey
// Copyright (c) 2000-08 Christopher Harvey. All Rights Reserved.
// Remark: Catalog Server
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

public class CatalogZaraDescriptions extends HttpServlet
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

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="", p11="";

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
          
        if(name.equals("unm")) // unm
          unm = value;
        else
        if(name.equals("uty")) // uty
          uty = value;
        else
        if(name.equals("p1")) // mfr
          p1 = value;
        else
        if(name.equals("p2")) // mfrCode
          p2 = value;
        else
        if(name.equals("p11")) // catalogUpline
          p11 = value; 
      }   
 
      doIt(r, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, p11, bytesOut);
    }
    catch(Exception e)
    {
      System.out.println("2005q: " + e);
      serverUtils.etotalBytes(req, unm, dnm, 2005, bytesOut[0], 0, "ERR:");
      try {  scoutln(r, bytesOut, "ERR:"); } catch (Exception e2) { } 
      r.flush();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(Writer r, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1, String p2, String p11, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String uName = directoryUtils.getMySQLUserName();
    String pWord = directoryUtils.getMySQLPassWord();
    
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + p11 + "_ofsa?user=" + uName + "&password=" + pWord);
    Statement stmt = null;
    ResultSet rs = null;

    scoutln(r, bytesOut, getData(con, stmt, rs, p1, p2));

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 2005, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con != null) con.close();
    r.flush();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private String getData(Connection con, Statement stmt, ResultSet rs, String mfr, String mfrCode) throws Exception
  {
    String desc = "", desc2 = "";
    
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT Description, Description2 FROM stock WHERE Manufacturer = '" + generalUtils.sanitiseForSQL(mfr) + "' AND ManufacturerCode = '" + generalUtils.sanitiseForSQL(mfrCode) + "'");
    
    if(rs.next())
    {
      desc  = rs.getString(1);
      desc2 = rs.getString(2);
  
      if(desc  == null) desc  = "";
      if(desc2 == null) desc2 = "";
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return desc + "\001" + desc2 + "\001";
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(Writer r, int bytesOut[], String str) throws Exception
  {      
    r.write(str + "\n");
    bytesOut[0] += (str.length() + 1);    
  }

}
