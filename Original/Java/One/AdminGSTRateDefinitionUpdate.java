// =================================================================================================================================
// System: ZaraStar AdminEngine: GSTRate definition - update
// Module: AdminGSTRateDefinitionUpdate.java
// Author: C.K.Harvey
// Copyright (c) 2006 Christopher Harvey. All Rights Reserved.
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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Enumeration;

public class AdminGSTRateDefinitionUpdate extends HttpServlet
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

    String unm="", sid="", uty="", dnm="", code="", rate="", type="", newOrEdit="";

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
        else
        if(name.equals("p2"))
          rate = value[0];
        else
        if(name.equals("p3"))
          type = value[0];
        else
        if(name.equals("p4"))
          newOrEdit = value[0];
      }

      doIt(req, res, unm, sid, uty, dnm, code, rate, type, newOrEdit, bytesOut);
    }    
    catch(Exception e)
    {
      System.out.println(("Unexpected System Error: 7061a: " + e));
      res.getWriter().write("Unexpected System Error: 7061a");
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void doIt(HttpServletRequest req, HttpServletResponse res, String unm, String sid, String uty, String dnm,
                     String code, String rate, String type, String newOrEdit, int[] bytesOut) throws Exception
  {
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
 
    String rtn="Unexpected System Error: 7061a";
    
    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      rtn = "Access Denied - Duplicate SignOn or Session Timeout";
    }
    else
    {
      code = generalUtils.stripLeadingAndTrailingSpaces(code);
      rate = generalUtils.stripLeadingAndTrailingSpaces(rate);
    
      if(code.length() == 0)
        rtn = "No GST Rate Name Entered";
      else
      if(rate.length() == 0)
        rtn = "No GST Rate Entered";
      else
      {
        boolean newRec;
        if(newOrEdit.equals("N"))
          newRec = true;
        else newRec = false;
   
        switch(update(newRec, dnm, code, rate, type, localDefnsDir, defnsDir))
        { 
          case ' ' : rtn = ".";                            break;
          case 'E' : rtn = "GST Rate Name Already Exists"; break;
        }
      }
    }
    
    res.setContentType("text/xml");
    res.setHeader("Cache-Control", "no-cache");
    
    String s = "<msg><res>" + rtn + "</res></msg>";
    res.getWriter().write(s);
    
    serverUtils.totalBytes(req, unm, dnm, 7061, bytesOut[0], 0, code);
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  // rtns: ' ' if rec added/updated ok
  //       'E' if adding a rec that already exists
  //       'X' other error
  private char update(boolean newRec, String dnm, String code, String rate, String type, String localDefnsDir,
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

      // if this record is set as the base currency then need to reset any existing base currency
      if(type.equals("D"))
      {
        stmt = con.createStatement();
        rs = stmt.executeQuery("SELECT GSTRateName FROM gstrate WHERE Type = 'D'"); 
        if(rs.next()) // just-in-case
        {
          String existingDefaultName = rs.getString(1);
          rs.close();
          if(stmt != null) stmt.close();
  
          stmt = con.createStatement();
          stmt.executeUpdate("UPDATE gstrate SET Type = 'N' WHERE GstRateName = '" + existingDefaultName + "'");
          if(stmt != null) stmt.close();
        }  
      }
      
      if(newRec)
      {    
        // if adding a new rec, check if code already exists
        boolean alreadyExists=false;
        stmt = con.createStatement();
  
        rs = stmt.executeQuery("SELECT GSTRateName FROM gstrate WHERE GSTRateName = '" + code + "'"); 
        if(rs.next())
          alreadyExists = true;
        rs.close() ;
  
        if(stmt != null) stmt.close();
      
        if(alreadyExists)
        {
          if(con  != null) con.close();
          return 'E';
        }
  
        stmt = con.createStatement();
           
        String q = "INSERT INTO gstrate ( GSTRateName, GSTRate, Type ) VALUES ('" + code + "','" + rate + "','" + type+ "')";
        stmt.executeUpdate(q);
        
        if(stmt != null) stmt.close();
        if(con  != null) con.close();
        
        return ' ';
      }
  
      // else update existing rec  
      stmt = con.createStatement();
        
      String q = "UPDATE gstrate SET GSTRate = '" + rate + "', Type = '" + type + "' WHERE GSTRateName = '" + code + "'";
      stmt.executeUpdate(q);
        
      if(stmt != null) stmt.close();        
      if(con  != null) con.close();
            
      return ' ';
    }
    catch(Exception e)
    {
      System.out.println("7061a: " + e);
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }
    
    return 'X';
  }

}
