// =================================================================================================================================
// System: ZaraStar Admin: Directory definition - update
// Module: AdminDirectoryDefinitionUpdate.java
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

public class AdminDirectoryDefinitionUpdate extends HttpServlet
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

    String unm="", sid="", uty="", dnm="", code="", host="", zcn="", status="", newOrEdit="";

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
          host = value[0];
        else
        if(name.equals("p3"))
          zcn = value[0];
        else
        if(name.equals("p4"))
          status = value[0];
        else
        if(name.equals("p5"))
          newOrEdit = value[0];
      }

      doIt(req, res, unm, sid, uty, dnm, code, host, zcn, status, newOrEdit, bytesOut);
    }    
    catch(Exception e)
    {
      System.out.println(("Unexpected System Error: 7068a: " + e));
      res.getWriter().write("Unexpected System Error: 7068a");
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void doIt(HttpServletRequest req, HttpServletResponse res, String unm, String sid, String uty, String dnm,
                    String code, String host, String zcn, String status, String newOrEdit, int[] bytesOut) throws Exception
  {
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
 
    String rtn="Unexpected System Error: 7068a";
    
    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      rtn = "Access Denied - Duplicate SignOn or Session Timeout";
    }
    else
    {
      code = generalUtils.stripAllSpaces(code);
      host = generalUtils.stripAllSpaces(host);
      zcn  = generalUtils.stripAllSpaces(zcn);
    
      if(code.length() == 0)
        rtn = "No Zara Code Entered";
      else
      if(host.length() == 0)
        rtn = "No Host Domain Name Entered";
      else
      {
        boolean newRec;
        if(newOrEdit.equals("N"))
          newRec = true;
        else newRec = false;
   
        switch(update(newRec, dnm, code, host, zcn, status, localDefnsDir, defnsDir))
        { 
          case ' ' : rtn = ".";                        break;
          case 'E' : rtn = "Zara Code Already Exists"; break;
        }
      }
    }
    
    res.setContentType("text/xml");
    res.setHeader("Cache-Control", "no-cache");
    
    String s = "<msg><res>" + rtn + "</res></msg>";
    res.getWriter().write(s);
    
    serverUtils.totalBytes(req, unm, dnm, 7068, bytesOut[0], 0, code);
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  // rtns: ' ' if rec added/updated ok
  //       'E' if adding a rec that already exists
  //       'X' other error
  private char update(boolean newRec, String dnm, String code, String host, String zcn, String status, String localDefnsDir, String defnsDir)
                      throws Exception
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
        // if adding a new rec, check if code already exists
        boolean alreadyExists=false;
        stmt = con.createStatement();
  
        rs = stmt.executeQuery("SELECT ZPN FROM directory WHERE ZPN = '" + generalUtils.sanitiseForSQL(code) + "'"); 
        if(rs.next())
          alreadyExists = true;
        rs.close();
  
        if(stmt != null) stmt.close();
      
        if(alreadyExists)
        {
          if(con  != null) con.close();
          return 'E';
        }
  
        stmt = con.createStatement();
           
        String q = "INSERT INTO directory ( ZPN, Host, ZCN, Status ) VALUES ('" + generalUtils.sanitiseForSQL(code) + "','" + generalUtils.sanitiseForSQL(host) + "','" + generalUtils.sanitiseForSQL(zcn) + "','" + status + "')";
        stmt.executeUpdate(q);
        
        if(stmt != null) stmt.close();
        if(con  != null) con.close();
        
        return ' ';
      }
  
      // else update existing rec  
      stmt = con.createStatement();
        
      String q = "UPDATE directory SET Host = '" + host + "', ZCN = '" + zcn + "', Status = '" + status + "' WHERE ZPN = '" + generalUtils.sanitiseForSQL(code) + "'";
      stmt.executeUpdate(q);
        
      if(stmt != null) stmt.close();        
      if(con  != null) con.close();
            
      return ' ';
    }
    catch(Exception e)
    {
      System.out.println("7068a: " + e);
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }
    
    return 'X';
  }

}
