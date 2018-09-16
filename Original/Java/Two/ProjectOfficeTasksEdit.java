// =======================================================================================================================================================================================================
// System: ZaraStar Project: office tasks - fetch for edit
// Module: _6083c.java
// Author: C.K.Harvey
// Copyright (c) 2006-07 Christopher Harvey. All Rights Reserved.
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

public class ProjectOfficeTasksEdit extends HttpServlet
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

    String unm="", sid="", uty="", dnm="", code="", task="";

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
          task = value[0];
      }

      doIt(req, res, unm, sid, uty, dnm, code, task, bytesOut);
    }    
    catch(Exception e)
    {
      System.out.println(("Unexpected System Error: 6803c: " + e));
      res.getWriter().write("Unexpected System Error: 6803c");
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void doIt(HttpServletRequest req, HttpServletResponse res, String unm, String sid, String uty, String dnm,
                    String code, String taskNumber, int[] bytesOut) throws Exception
  {
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
 
    String[] task               = new String[1]; task[0]="";
    String[] department         = new String[1]; department[0]="";
    String[] status             = new String[1]; status[0]="";
    String[] startDate          = new String[1]; startDate[0]="";
    String[] expectedFinishDate = new String[1]; expectedFinishDate[0]="";
    String[] actualFinishDate   = new String[1]; actualFinishDate[0]="";
    String[] remark             = new String[1]; remark[0]="";
    
    String rtn="Unexpected System Error: 6803c";
    
    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      rtn = "Access Denied - Duplicate SignOn or Session Timeout";
    }
    else
    {
      if(fetch(dnm, code, taskNumber, task, department, status, startDate, expectedFinishDate, actualFinishDate, remark,
               localDefnsDir, defnsDir))
      {
        rtn = ".";
      }
    }
    
    res.setContentType("text/xml");
    res.setHeader("Cache-Control", "no-cache");

    String s = "<msg><res>" + rtn + "</res><code>" + code + "</code><task>" + task[0] + "</task><department>" + department[0]
             + "</department><status>" + status[0] + "</status><startDate>" + startDate[0] + "</startDate><expectedFinishDate>"
             + expectedFinishDate[0] + "</expectedFinishDate><actualFinishDate>" + actualFinishDate[0]
             + "</actualFinishDate><remark>" + remark[0] + "</remark></msg>";
    res.getWriter().write(s);
    
    serverUtils.totalBytes(req, unm, dnm, 6803, bytesOut[0], 0, code);
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  // rtns: ' ' if rec added/updated ok
  //       'E' if adding a rec that already exists
  //       'X' other error
  private boolean fetch(String dnm, String code, String taskNumber, String[] task, String[] department, String[] status,
                        String[] startDate, String[] expectedFinishDate, String[] actualFinishDate, String[] remark,
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
    
      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_project?user=" + uName + "&password=" + passWord);

      stmt = con.createStatement();
  
      rs = stmt.executeQuery("SELECT * FROM officetasks WHERE ProjectCode = '" + code + "' AND TaskNumber = '" + taskNumber + "'"); 

      if(rs.next())                  
      {
        task[0]               = rs.getString(3);
        department[0]         = rs.getString(4);
        status[0]             = rs.getString(5);
        startDate[0]          = rs.getString(6);
        expectedFinishDate[0] = rs.getString(7);
        actualFinishDate[0]   = rs.getString(8);
        remark[0]             = rs.getString(9);
      } 
                 
      rs.close();
        
      if(stmt != null) stmt.close();        
      if(con  != null) con.close();
            
      return true;
    }
    catch(Exception e)
    {
      System.out.println("6803c: " + e);
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }
    
    return false;
  }

}
