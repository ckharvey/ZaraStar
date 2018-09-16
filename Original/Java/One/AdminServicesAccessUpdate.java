// =======================================================================================================================================================================================================
// System: ZaraStar Admin: Services access update - update
// Module: AdminServicesAccessUpdate.java
// Author: C.K.Harvey
// Copyright (c) 2003-09 Christopher Harvey. All Rights Reserved.
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
import java.net.*;
import java.util.*;

public class AdminServicesAccessUpdate extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
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

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", service="", callingService="";

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
        if(name.equals("callingService"))
          callingService = value[0];
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

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, numUsers, users, service, callingService, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "AdminServicesAccessUpdate", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7502, bytesOut[0], 0, "ERR:" + service);
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int numUsers, String[] users, String service, String callingService, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null, stmt2 = null;
    ResultSet rs   = null;

    if((! serverUtils.isSysAdmin(con, stmt, rs, unm, dnm, sid, uty, localDefnsDir, defnsDir)) && (! serverUtils.isDBAdmin(con, stmt, rs, unm)))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "AdminServicesAccessUpdate", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7502, bytesOut[0], 0, "ACC:" + service);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "AdminServicesAccessUpdate", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7502, bytesOut[0], 0, "SID:" + service);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    process(con, stmt, stmt2, rs, service, users, numUsers, dnm);

    refetch(out, unm, sid, uty, dnm, men, den, bnm, localDefnsDir, callingService, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 7502, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), service);
    if(con  != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void process(Connection con, Statement stmt, Statement stmt2, ResultSet rs, String service, String[] users, int numUsers, String dnm) throws Exception
  {
    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT UserCode FROM profiles");

      String userCode;

      while(rs.next())
      {
        userCode = rs.getString(1);

        if(userCode.equals("Sysadmin") || userCode.startsWith("___"))
          ;
        else
        {
          if(isOnList(userCode, users, numUsers))
            addService(con, stmt2, userCode, service);
          else removeService(con, stmt2, userCode, service);

          profile.deleteRightsFile(userCode, dnm);
        }
      }

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean isOnList(String userCode, String[] users, int numUsers) throws Exception
  {
    for(int x=0;x<numUsers;++x)
    {
      if(users[x].equals(userCode))
        return true;
    }

    return false;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
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

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void removeService(Connection con, Statement stmt, String userCode, String service) throws Exception
  {
    stmt = con.createStatement();

    stmt.executeUpdate("DELETE FROM userservices WHERE Service = '" + service + "' AND UserCode = '" + userCode + "'");

    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void refetch(PrintWriter out, String unm, String sid, String uty, String dnm, String men, String den, String bnm, String localDefnsDir, String callingService, int[] bytesOut) throws Exception
  {
    URL url = new URL("http://" + serverUtils.serverToCall("OFSA", localDefnsDir) + "/central/servlet/OptionTabs?p1=A&p2=" + callingService + "&unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm);

    URLConnection uc = url.openConnection();
    uc.setDoInput(true);
    uc.setUseCaches(false);
    uc.setDefaultUseCaches(false);

    uc.setRequestProperty("Content-Type", "application/octet-stream");

    BufferedReader di = new BufferedReader(new InputStreamReader(uc.getInputStream()));

    String s = di.readLine();
    while(s != null)
    {
      scoutln(out, bytesOut, s);
      s = di.readLine();
    }

    di.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {
    out.println(str);
    bytesOut[0] += (str.length() + 2);
  }

}
