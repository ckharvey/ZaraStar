// =======================================================================================================================================================================================================
// System: ZaraStar Product: stock check - fetch for edit
// Module: StockCheckEdit.java
// Author: C.K.Harvey
// Copyright (c) 2006-09 Christopher Harvey. All Rights Reserved.
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
import java.util.Enumeration;

public class StockCheckEdit extends HttpServlet
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

    String unm="", sid="", uty="", dnm="", code="";

    try
    {
      directoryUtils.setContentHeaders(res);
      res.setHeader("Header", "HTTP/1.0 200 OK");
      res.setStatus(200);

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
          code = value[0];
      }

      doIt(req, res, unm, sid, uty, dnm, code, bytesOut);
    }    
    catch(Exception e)
    {
      serverUtils.etotalBytes(req, unm, dnm, 3018, bytesOut[0], 0, "ERR:" + code);
      System.out.println(("Unexpected System Error: 3018c: " + e));
      res.getWriter().write("Unexpected System Error: 3018c");
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(HttpServletRequest req, HttpServletResponse res, String unm, String sid, String uty, String dnm, String code, int[] bytesOut) throws Exception
  {
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
 
    String[] store      = new String[1]; store[0]="";
    String[] date       = new String[1]; date[0]="";
    String[] level      = new String[1]; level[0]="";
    String[] remark     = new String[1]; remark[0]="";
    String[] status     = new String[1]; status[0]="";
    String[] reconciled = new String[1]; reconciled[0]="";
    String[] signon     = new String[1]; signon[0]="";
    String[] type       = new String[1]; type[0]="";
    String[] location   = new String[1]; location[0]="";
    
    String rtn="Unexpected System Error: 3018c";
    
    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      rtn = "Access Denied - Duplicate SignOn or Session Timeout";
    }
    else
    {
      if(fetch(dnm, code, store, date, level, remark, status, reconciled, signon, type, location))
        rtn = ".";
    }
    
    res.setContentType("text/xml");
    res.setHeader("Cache-Control", "no-cache");
    
    if(remark[0].length() == 0)
      remark[0] = ".";
    
    if(status[0].length() == 0)
      status[0] = "L";
    
    if(reconciled[0].length() == 0)
      reconciled[0] = "N";
    
    if(type[0].length() == 0)
      type[0] = "S";
    
    String s = "<msg><res>" + rtn + "</res><code>" + code + "</code><store>" + store[0] + "</store><date>" + generalUtils.convertFromYYYYMMDD(date[0]) + "</date><level>" + level[0] + "</level><status>" + status[0] + "</status><reconciled>"
             + reconciled[0] + "</reconciled><signon>" + signon[0].substring(0, (signon[0].length() - 2)) + "</signon><remark>" + remark[0] + "</remark><location>" + location[0] + "</location><type>" + type[0] + "</type></msg>";

    res.getWriter().write(s);

    serverUtils.totalBytes(req, unm, dnm, 3018, bytesOut[0], 0, code);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean fetch(String dnm, String code, String[] store, String[] date, String[] level, String[] remark, String[] status, String[] reconciled, String[] signon, String[] type, String[] location) throws Exception
  {
    Connection con = null;
    Statement stmt = null;
    ResultSet rs   = null;

    try
    {
      String uName    = directoryUtils.getMySQLUserName();
      String passWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();
    
      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + uName + "&password=" + passWord);

      stmt = con.createStatement();
  
      rs = stmt.executeQuery("SELECT * FROM stockc WHERE CheckCode = '" + code + "' AND Level != '999999'");

      if(rs.next())                  
      {
        store[0]      = rs.getString(3);
        date[0]       = rs.getString(4);
        level[0]      = generalUtils.stripTrailingZeroesStr(rs.getString(5));
        remark[0]     = rs.getString(6);
        status[0]     = rs.getString(7);
        signon[0]     = rs.getString(8) + " @ " + rs.getString(9);
        location[0]   = rs.getString(10);
        reconciled[0] = rs.getString(12);
        type[0]       = rs.getString(13);
      } 
      
      if(remark[0]     == null) remark[0]     = "";
      if(reconciled[0] == null) reconciled[0] = "N";
      if(type[0]       == null) type[0]       = "S";
      if(location[0]   == null) location[0]   = "Unknown";
                 
      if(rs   != null) rs.close();        
      if(stmt != null) stmt.close();        
      if(con  != null) con.close();
            
      return true;
    }
    catch(Exception e)
    {
      System.out.println("3018c: " + e);
      if(rs   != null) rs.close();        
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }
    
    return false;
  }

}
