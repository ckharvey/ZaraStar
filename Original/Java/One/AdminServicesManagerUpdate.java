// =======================================================================================================================================================================================================
// System: ZaraStar Admin: Services manager - update
// Module: AdminServicesManagerUpdate.java
// Author: C.K.Harvey
// Copyright (c) 2003-08 Christopher Harvey. All Rights Reserved.
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
import java.util.*;

public class AdminServicesManagerUpdate extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  Profile profile = new Profile();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", operation="", service="";

    try
    {
      directoryUtils.setContentHeaders(res);
      out = res.getWriter();

      String[] users = new String[100];
      int numUsers = 0, usersLen = 100;
            
      int x;
      String value[];
      String name;

      Enumeration en = req.getParameterNames();
      while(en.hasMoreElements())
      {
        name = (String)en.nextElement();
        value = req.getParameterValues(name);
        if(name.equals("unm"))
          unm = value[0];
        else
        if(name.equals("sid"))
          sid = value[0];
        else
        if(name.equals("uty"))
          uty = value[0];
        else
        if(name.equals("men"))
          men = value[0];
        else
        if(name.equals("den"))
          den = value[0];
        else
        if(name.equals("dnm"))
          dnm = value[0];
        else
        if(name.equals("bnm"))
          bnm = value[0];
        else
        if(name.equals("service"))
          service = value[0];
        else
        if(name.equals("operation"))
          operation = value[0];
        else
        {          
          if((numUsers + 1) == usersLen)
          {
            String[] tmp = new String[usersLen];
            for(x=0;x<usersLen;++x)
              tmp[x] = users[x];
            usersLen += 100;
            users = new String[usersLen];
            for(x=0;x<(usersLen - 100);++x)
              users[x] = tmp[x];
          }  
          
          users[numUsers++] = name;
        }
      }

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, numUsers, users, service, operation, bytesOut);
    }
    catch(Exception e)
    {
      StringBuffer url = req.getRequestURL();
      int x=0;
      if(url.charAt(x) == 'h' || url.charAt(x) == 'H')
        x += 7;
      String urlBit="";
      while(url.charAt(x) != '\000' && url.charAt(x) != ',' && url.charAt(x) != '/')
        urlBit += url.charAt(x++);

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "AdminServicesManagerUpdate", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7034, bytesOut[0], 0, "ERR:" + service);
      if(out != null) out.flush();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                    int numUsers, String[] users, String service, String operation, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 7034, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "AdminServicesManagerUpdate", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7034, bytesOut[0], 0, "ACC:" + service);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "AdminServicesManagerUpdate", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7034, bytesOut[0], 0, "SID:" + service);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    process(con, stmt, operation, service, numUsers, users, dnm);

    messagePage.msgScreen(false, out, req, 27, unm, sid, uty, men, den, dnm, bnm, "LibraryCreateRecordUpload", imagesDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 7034, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), service);
    if(con  != null) con.close();
    if(out != null) out.flush();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void process(Connection con, Statement stmt, String operation, String service, int numUsers, String[] users, String dnm) throws Exception
  {
    for(int x=0;x<numUsers;++x)
    {
      if(operation.equals("a")) // add
        addService(con, stmt, users[x], service);
      else removeService(con, stmt, users[x], service);

      profile.deleteRightsFile(users[x], dnm);
    }
  }
        
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void addService(Connection con, Statement stmt, String userCode, String service) throws Exception
  {
    try
    {
      stmt = con.createStatement();
  
      stmt.executeUpdate("INSERT INTO userservices (UserCode, Service) VALUES ('" + userCode + "','" + service + "')");

      if(stmt != null) stmt.close();
    }
    catch(Exception e) // already exists
    {
      if(stmt != null) stmt.close();
    }    
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void removeService(Connection con, Statement stmt, String userCode, String service) throws Exception
  {
    stmt = con.createStatement();
  
    stmt.executeUpdate("DELETE FROM userservices WHERE UserCode = '" + userCode + "' AND Service = '" + service + "'");

    if(stmt != null) stmt.close();
  }

}
