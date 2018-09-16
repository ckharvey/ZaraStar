// ===================================================================================================================================================
// System: ZaraStar Channels: Channels definition - update
// Module: ChannelDefinitionUpdate.java
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

public class ChannelDefinitionUpdate extends HttpServlet
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

    String unm="", sid="", uty="", dnm="", name="", purpose="", registerOnSC="", type="", status="", newOrEdit="";

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
          purpose = value[0];
        else
        if(elementName.equals("p3"))
          registerOnSC = value[0];
        else
        if(elementName.equals("p4"))
          type = value[0];
        else
        if(elementName.equals("p5"))
          status = value[0];
        else
        if(elementName.equals("p6"))
          newOrEdit = value[0];
      }

      doIt(req, res, unm, sid, uty, dnm, name, purpose, registerOnSC, type, status, newOrEdit, bytesOut);
    }    
    catch(Exception e)
    {
      StringBuffer url = req.getRequestURL();
      int x=0;
      if(url.charAt(x) == 'h' || url.charAt(x) == 'H')
        x += 7;
      
      System.out.println(("Unexpected System Error: 12706a: " + e));
      res.getWriter().write("Unexpected System Error: 12706a");
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(HttpServletRequest req, HttpServletResponse res, String unm, String sid, String uty, String dnm, String name, String purpose,
                    String registerOnSC, String type, String status, String newOrEdit, int[] bytesOut) throws Exception
  {
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
 
    String rtn="Unexpected System Error: 12706a";
    
    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      rtn = "Access Denied - Duplicate SignOn or Session Timeout";
    }
    else
    {
      name = generalUtils.stripAllSpaces(name);
      purpose = generalUtils.stripLeadingAndTrailingSpaces(purpose);
    
      if(name.length() == 0)
        rtn = "No Channel Name Entered";
      else
      if(purpose.length() == 0)
        rtn = "No Channel Purpose Entered";
      else
      {
        boolean newRec;
        if(newOrEdit.equals("N"))
          newRec = true;
        else newRec = false;
   
        switch(update(newRec, dnm, name, purpose, registerOnSC, type, status, localDefnsDir, defnsDir))
        {
          case ' ' : rtn = ".";                      break;
          case 'E' : rtn = "Channel Already Exists"; break;
        }
      }
    }
    
    res.setContentType("text/xml");
    res.setHeader("Cache-Control", "no-cache");
    
    String s = "<msg><res>" + rtn + "</res></msg>";
    res.getWriter().write(s);
    
    serverUtils.totalBytes(req, unm, dnm, 12706, bytesOut[0], 0, name);
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // rtns: ' ' if rec added/updated ok
  //       'E' if adding a rec that already exists
  //       'X' other error
  private char update(boolean newRec, String dnm, String name, String purpose, String registerOnSC, String type, String status,
                      String localDefnsDir, String defnsDir) throws Exception
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

      if(newRec)
      {    
        // if adding a new rec, check if name already exists
        boolean alreadyExists=false;
        stmt = con.createStatement();
  
        rs = stmt.executeQuery("SELECT Name FROM channels WHERE Name = '" + name + "'"); 
        if(rs.next())
          alreadyExists = true;
        
        if(rs   != null) rs.close();  
        if(stmt != null) stmt.close();
      
        if(alreadyExists)
        {
          if(con != null) con.close();
          return 'E';
        }
  
        stmt = con.createStatement();
           
        String q = "INSERT INTO channels ( Name, Purpose, RegisterOnSC, Type, Status, Deleteable ) VALUES ('" + generalUtils.sanitiseForSQL(name) + "','"
                 + generalUtils.sanitiseForSQL(purpose) + "','" + registerOnSC + "','" + type + "','" + status + "','Y')";
        stmt.executeUpdate(q);

        if(stmt != null) stmt.close();
        if(con  != null) con.close();
        
        return ' ';
      }
  
      // else update existing rec
      stmt = con.createStatement();
        
      String q = "UPDATE channels SET Purpose = '" + generalUtils.sanitiseForSQL(purpose) + "', RegisterOnSC = '" + registerOnSC + "', Type = '" + type
               + "', Status = '" + status + "' WHERE Name = '" + generalUtils.sanitiseForSQL(name) + "'";
      stmt.executeUpdate(q);
        
      if(stmt != null) stmt.close();        
      if(con  != null) con.close();
            
      return ' ';
    }
    catch(Exception e)
    {
      System.out.println("12706a: " + e);
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }
    
    return 'X';
  }

}
