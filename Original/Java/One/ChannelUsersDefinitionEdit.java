// ===================================================================================================================================================
// System: ZaraStar Admin: Channel users definition - fetch for edit
// Module: ChannelUsersDefinitionEdit.java
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

public class ChannelUsersDefinitionEdit extends HttpServlet
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

    String unm="", sid="", uty="", dnm="", name="", domain="", user="";

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
        else
        if(elementName.equals("p2"))
          user = value[0];
        else
        if(elementName.equals("p3"))
          domain = value[0];
      }

      doIt(req, res, unm, sid, uty, dnm, name, user, domain, bytesOut);
    }    
    catch(Exception e)
    {
      System.out.println(("Unexpected System Error: 12707c: " + e));
      res.getWriter().write("Unexpected System Error: 12707c");
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(HttpServletRequest req, HttpServletResponse res, String unm, String sid, String uty, String dnm, String name, String user,
                    String domain, int[] bytesOut) throws Exception
  {
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
 
    String[] passWord = new String[1]; passWord[0]="";
    String[] status   = new String[1]; status[0]="";
    
    String rtn="Unexpected System Error: 12707c";
    
    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      rtn = "Access Denied - Duplicate SignOn or Session Timeout";
    }
    else
    {
      if(fetch(dnm, name, user, domain, passWord, status, localDefnsDir, defnsDir))
        rtn = ".";
    }
    
    res.setContentType("text/xml");
    res.setHeader("Cache-Control", "no-cache");
    
    if(passWord[0].length() == 0)
      passWord[0] = ".";
    
    String s = "<msg><res>" + rtn + "</res><name>" + name + "</name><user>" + user + "</user><domain>" + domain + "</domain><password>" + passWord[0]
             + "</password><status>" + status[0] + "</status></msg>";
    res.getWriter().write(s);

    serverUtils.totalBytes(req, unm, dnm, 12707, bytesOut[0], 0, name);
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // rtns: ' ' if rec added/updated ok
  //       'E' if adding a rec that already exists
  //       'X' other error
  private boolean fetch(String dnm, String name, String user, String domain, String[] passWord, String[] status, String localDefnsDir,
                        String defnsDir) throws Exception
  {
    Connection con = null;
    Statement stmt = null;
    ResultSet rs   = null;

    try
    {
      String uName = directoryUtils.getMySQLUserName();
      String pWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();
    
      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + uName + "&password=" + pWord);

      stmt = con.createStatement();
  
      rs = stmt.executeQuery("SELECT PassWord, Status FROM channelusers WHERE Name = '" + name + "' AND User = '" + user + "' AND Domain = '" + domain
                           + "'");

      if(rs.next())                  
      {
        passWord[0] = rs.getString(1);
        status[0]   = rs.getString(2);
      } 
                 
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();        
      if(con  != null) con.close();
            
      return true;
    }
    catch(Exception e)
    {
      System.out.println("12707c: " + e);
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }
    
    return false;
  }

}
