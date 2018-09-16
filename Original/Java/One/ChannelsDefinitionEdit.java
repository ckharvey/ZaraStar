// ===================================================================================================================================================
// System: ZaraStar Admin: Channels definition - fetch for edit
// Module: ChannelsDefinitionEdit.java
// Author: C.K.Harvey
// Copyright (c) 2006-08 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
//
// ===================================================================================================================================================

package org.zarastar.zarastar;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.sql.*;
import java.util.Enumeration;

public class ChannelsDefinitionEdit extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", dnm="", name="";

    try
    {
      directoryUtils.setContentHeaders(res);
      res.setHeader("Header", "HTTP/1.0 200 OK");
      res.setStatus(200);

      Enumeration en = req.getParameterNames();
      while(en.hasMoreElements())
      {
        String elementName = (String)en.nextElement();
        String[] value = req.getParameterValues(elementName);
        if(elementName.equals("unm"))
          unm = value[0];
        else
        if(elementName.equals("sid"))
          sid = value[0];
        else
        if(elementName.equals("uty"))
          uty = value[0];
        else
        if(elementName.equals("dnm"))
          dnm = value[0];
        else
        if(elementName.equals("p1"))
          name = value[0];
      }

      doIt(req, res, unm, sid, uty, dnm, name, bytesOut);
    }    
    catch(Exception e)
    {      
      System.out.println(("Unexpected System Error: 12706c: " + e));
      res.getWriter().write("Unexpected System Error: 12706c");
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(HttpServletRequest req, HttpServletResponse res, String unm, String sid, String uty, String dnm, String name, int[] bytesOut)
                    throws Exception
  {
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
 
    String[] purpose      = new String[1]; purpose[0]="";
    String[] registerOnSC = new String[1]; registerOnSC[0]="";
    String[] type         = new String[1]; type[0]="";
    String[] status       = new String[1]; status[0]="";
    
    String rtn="Unexpected System Error: 12706c";
    
    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      rtn = "Access Denied - Duplicate SignOn or Session Timeout";
    }
    else
    {
      if(fetch(dnm, name, purpose, registerOnSC, type, status, localDefnsDir, defnsDir))
        rtn = ".";
    }
    
    res.setContentType("text/xml");
    res.setHeader("Cache-Control", "no-cache");
    
    if(purpose[0].length() == 0)
      purpose[0] = ".";
    
    String s = "<msg><res>" + rtn + "</res><name>" + name + "</name><purpose>" + purpose[0] + "</purpose><registerOnSC>" + registerOnSC[0]
             + "</registerOnSC><type>" + type[0] + "</type><status>" + status[0] + "</status></msg>";
    res.getWriter().write(s);
    
    serverUtils.totalBytes(req, unm, dnm, 12706, bytesOut[0], 0, name);
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // rtns: ' ' if rec added/updated ok
  //       'E' if adding a rec that already exists
  //       'X' other error
  private boolean fetch(String dnm, String name, String[] purpose, String[] registerOnSC, String[] type, String[] status, String localDefnsDir,
                        String defnsDir) throws Exception
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
  
      rs = stmt.executeQuery("SELECT Purpose, RegisterOnSC, Type, Status FROM channels WHERE Name = '" + name + "'"); 

      if(rs.next())                  
      {
        purpose[0]      = rs.getString(1);
        registerOnSC[0] = rs.getString(2);
        type[0]         = rs.getString(3);
        status[0]       = rs.getString(4);
      } 
                 
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();        
      if(con  != null) con.close();
            
      return true;
    }
    catch(Exception e)
    {
      System.out.println("12706c: " + e);
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }
    
    return false;
  }

}
