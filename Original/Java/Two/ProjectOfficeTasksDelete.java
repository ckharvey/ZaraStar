// =======================================================================================================================================================================================================
// System: ZaraStar Project: Office tasks - delete
// Module: ProjectOfficeTasksDelete.java
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
import java.sql.Statement;
import java.util.Enumeration;

public class ProjectOfficeTasksDelete extends HttpServlet
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
      System.out.println(("Unexpected System Error: 6803b: " + e));
      res.getWriter().write("Unexpected System Error: 6803b");
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void doIt(HttpServletRequest req, HttpServletResponse res, String unm, String sid, String uty, String dnm,
                    String code, String task, int[] bytesOut) throws Exception
  {
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    String rtn="Unexpected System Error: 6803b";
    
    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      rtn = "Access Denied - Duplicate SignOn or Session Timeout";
    }
    else
    {
      if(deleteRec(dnm, code, task, localDefnsDir, defnsDir))
      rtn = ".";
    }
    
    res.setContentType("text/xml");
    res.setHeader("Cache-Control", "no-cache");
    
    String s = "<msg><res>" + rtn + "</res></msg>";
    res.getWriter().write(s);
    
    serverUtils.totalBytes(req, unm, dnm, 6803, bytesOut[0], 0, code);
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private boolean deleteRec(String dnm, String code, String task, String localDefnsDir, String defnsDir) throws Exception
  {
    Connection con = null;
    Statement stmt = null;

    try
    {
      String uName    = directoryUtils.getMySQLUserName();
      String passWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();
    
      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_project?user=" + uName + "&password=" + passWord);

      stmt = con.createStatement();
      int numRecsDeleted = stmt.executeUpdate("DELETE FROM officetasks WHERE ProjectCode = '" + code + "' AND TaskNumber = '" + task
                                            + "'");
              
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
      
      if(numRecsDeleted == 1)
        return true;
      
      return false;
    }
    catch(Exception e)
    {
      System.out.println("6803b: " + e);
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }
    
    return false;
  }

}
