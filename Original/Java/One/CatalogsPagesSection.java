// =======================================================================================================================================================================================================
// System: ZaraStar Catalogs: fetch pages for a section
// Module: CatalogsPagesSection.java
// Author: C.K.Harvey
// Copyright (c) 2002-08 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.sql.*;
import java.util.Enumeration;

public class CatalogsPagesSection extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  AdminUtils adminUtils = new AdminUtils();
  DefinitionTables definitionTables = new DefinitionTables();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", dnm="", mfr="", chapter="", section="";

    try
    {
      directoryUtils.setContentHeaders(res);
      res.setHeader("Header", "HTTP/1.0 200 OK");
      res.setStatus(200);
      
      res.setContentType("text/xml");
      res.setHeader("Cache-Control", "no-cache");

      Enumeration en = req.getParameterNames();
      while(en.hasMoreElements())
      {
        String name = (String)en.nextElement();
        String[] value = req.getParameterValues(name);
        if(name.equals("unm"))
          unm = value[0];
        else
        if(name.equals("sid"))
          sid = value[0];
        else
        if(name.equals("uty"))
          uty = value[0];
        else
        if(name.equals("dnm"))
          dnm = value[0];
        else
        if(name.equals("p1"))
          mfr = value[0];
        else
        if(name.equals("p2"))
          chapter = value[0];
        else
        if(name.equals("p3"))
          section = value[0];
      }
      
      chapter = generalUtils.deSanitise(chapter);
      section = generalUtils.deSanitise(section);

      doIt(req, res, mfr, chapter, section, unm, sid, uty, dnm, bytesOut);
    }    
    catch(Exception e)
    {
      System.out.println(("Unexpected System Error: 6402e: " + e));
      res.getWriter().write("Unexpected System Error: 6402e");
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(HttpServletRequest req, HttpServletResponse res, String mfr, String chapter, String section, String unm, String sid, String uty,
                    String dnm, int[] bytesOut) throws Exception
  {
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
 
    String rtn = "", dot = "Unexpected System Error: 6402e";
    
    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      dot = "Access Denied - Duplicate SignOn or Session Timeout";
    }
    else
    {
      Connection con = null;
      Statement stmt = null;
      ResultSet rs   = null;
    
      try
      {
        String userName = directoryUtils.getMySQLUserName();
        String passWord = directoryUtils.getMySQLPassWord();
    
        Class.forName("com.mysql.jdbc.Driver").newInstance();
    
        con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + userName + "&password=" + passWord);

        rtn = getData(con, stmt, rs, mfr, chapter, section);
        dot = ".";
          
        if(con  != null) con.close(); 
      }
       catch(Exception e) { }
    }
    
    String s = "<msg><res>" + dot + "</res><pages><![CDATA[" + rtn + "]]></pages></msg>";

    res.getWriter().write(s);
    
    serverUtils.totalBytes(req, unm, dnm, 6402, bytesOut[0], 0, chapter);
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private String getData(Connection con, Statement stmt, ResultSet rs, String mfr, String chapter, String section) throws Exception
  {
    String rtn = "<select name=\"pageName\">";
    
    try
    {
      stmt = con.createStatement();
  
      rs = stmt.executeQuery("SELECT Page FROM catalogc WHERE Manufacturer = '" + generalUtils.sanitiseForSQL(mfr) + "' AND Chapter = '"
                             + generalUtils.sanitiseForSQL(chapter) + "' AND Section = '" + generalUtils.sanitiseForSQL(section) + "' ORDER BY Position");

      String page;
      
      while(rs.next())
      {
        page = rs.getString(1);
        rtn += "<option value='" + generalUtils.sanitise(page) + "'>" + page;
      }

      rtn += "</select>";
            
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
    
    return rtn;
  }  
  
}
