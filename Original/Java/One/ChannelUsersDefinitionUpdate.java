// ===================================================================================================================================================
// System: ZaraStar Channels: Channel Users definition - update
// Module: ChannelUsersDefinitionUpdate.java
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

public class ChannelUsersDefinitionUpdate extends HttpServlet
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

    String unm="", sid="", uty="", dnm="", name="", user="", domain="", password="", status="", newOrEdit="";

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
        else
        if(elementName.equals("p4"))
          password = value[0];
        else
        if(elementName.equals("p5"))
          status = value[0];
        else
        if(elementName.equals("p6"))
          newOrEdit = value[0];
      }

      doIt(req, res, unm, sid, uty, dnm, name, user, domain, password, status, newOrEdit, bytesOut);
    }    
    catch(Exception e)
    {
      StringBuffer url = req.getRequestURL();
      int x=0;
      if(url.charAt(x) == 'h' || url.charAt(x) == 'H')
        x += 7;
      
      System.out.println(("Unexpected System Error: 12707a: " + e));
      res.getWriter().write("Unexpected System Error: 12707a");
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(HttpServletRequest req, HttpServletResponse res, String unm, String sid, String uty, String dnm, String name, String user,
                    String domain, String password, String status, String newOrEdit, int[] bytesOut) throws Exception
  {
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
 
    String rtn="Unexpected System Error: 12707a";
    
    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      rtn = "Access Denied - Duplicate SignOn or Session Timeout";
    }
    else
    {
      name     = generalUtils.stripLeadingAndTrailingSpaces(name);
      user     = generalUtils.stripLeadingAndTrailingSpaces(user);
      domain   = generalUtils.stripLeadingAndTrailingSpaces(domain);
      password = generalUtils.stripLeadingAndTrailingSpaces(password);
    
      if(name.length() == 0)
        rtn = "No Channel Name Entered";
      else
      if(user.length() == 0)
        rtn = "No User Entered";
      else
      if(name.length() == 0)
        rtn = "No Domain Entered";
      else
      {
        boolean newRec;
        if(newOrEdit.equals("N"))
          newRec = true;
        else newRec = false;
   
        switch(update(newRec, dnm, name, user, domain, password, status, localDefnsDir, defnsDir))
        {
          case ' ' : rtn = ".";                   break;
          case 'E' : rtn = "User Already Exists"; break;
        }
      }
    }
    
    res.setContentType("text/xml");
    res.setHeader("Cache-Control", "no-cache");
    
    String s = "<msg><res>" + rtn + "</res></msg>";
    res.getWriter().write(s);
    
    serverUtils.totalBytes(req, unm, dnm, 12707, bytesOut[0], 0, name);
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // rtns: ' ' if rec added/updated ok
  //       'E' if adding a rec that already exists
  //       'X' other error
  private char update(boolean newRec, String dnm, String name, String user, String domain, String passWord, String status, String localDefnsDir,
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

      if(newRec)
      {    
        // if adding a new rec, check if name already exists
        boolean alreadyExists=false;
        stmt = con.createStatement();
  
        rs = stmt.executeQuery("SELECT Name FROM channelusers WHERE Name = '" + name + "' AND User = '" + user + "' AND Domain = '" + domain + "'");
        if(rs.next())
          alreadyExists = true;
        rs.close() ;
  
        if(stmt != null) stmt.close();
      
        if(alreadyExists)
        {
          if(con != null) con.close();
          return 'E';
        }
  
        stmt = con.createStatement();
           
        String q = "INSERT INTO channelusers ( Name, User, Domain, PassWord, Status ) VALUES ('" + generalUtils.sanitiseForSQL(name) + "','"
                 + generalUtils.sanitiseForSQL(user) + "','" + generalUtils.sanitiseForSQL(domain) + "','" + generalUtils.sanitiseForSQL(passWord) + "','" + status + "')";
        stmt.executeUpdate(q);
        
        if(stmt != null) stmt.close();
        if(con  != null) con.close();
        
        return ' ';
      }
  
      // else update existing rec
      stmt = con.createStatement();
        
      String q = "UPDATE channelusers SET PassWord = '" + generalUtils.sanitiseForSQL(passWord) + "', Status = '" + status + "' WHERE Name = '"
               + generalUtils.sanitiseForSQL(name) + "' AND User = '" + user + "' AND Domain = '" + domain + "'";
      stmt.executeUpdate(q);
        
      if(stmt != null) stmt.close();        
      if(con  != null) con.close();
            
      return ' ';
    }
    catch(Exception e)
    {
      System.out.println("12707a: " + e);
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }
    
    return 'X';
  }

}
