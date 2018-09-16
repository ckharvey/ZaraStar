// =======================================================================================================================================================================================================
// System: ZaraStar AdminEngine: Set user to same as another user
// Module: AdminSetUser.java
// Author: C.K.Harvey
// Copyright (c) 2002-08 Christopher Harvey. All Rights Reserved;
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
import java.net.*;
import java.sql.*;

public class AdminSetUser extends HttpServlet
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

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="";

    try
    {
      directoryUtils.setContentHeaders(res);
      out = res.getWriter();

      unm = req.getParameter("unm");
      sid = req.getParameter("sid");
      uty = req.getParameter("uty");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");
      p1  = req.getParameter("p1"); // userCode
      p2  = req.getParameter("p2"); // same user

      doIt(out, req, p1, p2, unm, sid, uty, men, den, dnm, bnm, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "AdminSetUser", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7008, bytesOut[0], 0, "ERR:" + p1 + ":" + p2);
      if(out != null) out.flush();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String p1, String p2, String unm, String sid, String uty, String men, String den,
                    String dnm, String bnm, int[] bytesOut) throws Exception
  {
    String imagesDir        = directoryUtils.getSupportDirs('I');
    String defnsDir         = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    long startTime = new java.util.Date().getTime();

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null, stmt2 = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 7008, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "AdminUserModules", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7008, bytesOut[0], 0, "ACC:" + p1);
      if(con   != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "AdminUserModules", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7008, bytesOut[0], 0, "SID:" + p1);
      if(con   != null) con.close();
      if(out != null) out.flush();
      return;
    }

    removeAll(con, stmt, p1);

    forEachModule(con, stmt, stmt2, rs, p1, p2);
    forEachService(con, stmt, stmt2, rs, p1, p2, dnm);

    reFetch(out, unm, sid, uty, dnm, men, den, bnm, p1, localDefnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 7008, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1 + ":" + p2);
    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void forEachModule(Connection con, Statement stmt, Statement stmt2, ResultSet rs, String userCode, String sameUser) throws Exception
  {
    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT Module FROM usermodules WHERE UserCode = '" + sameUser + "'");

      while(rs.next())
        addModule(con, stmt2, userCode, rs.getString(1));

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void addModule(Connection con, Statement stmt, String userCode, String module) throws Exception
  {
    try
    {
      stmt = con.createStatement();

      stmt.executeUpdate("INSERT INTO usermodules (UserCode, Module) VALUES ('" + userCode + "','" + module + "')");

      if(stmt != null) stmt.close();
    }
    catch(Exception e) // already exists
    {
      if(stmt != null) stmt.close();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void forEachService(Connection con, Statement stmt, Statement stmt2, ResultSet rs, String userCode, String sameUser, String dnm)
                              throws Exception
  {
    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT Service FROM userservices WHERE UserCode = '" + sameUser + "'");

      while(rs.next())
        addService(con, stmt2, userCode, rs.getString(1));

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();

      profile.deleteRightsFile(userCode, dnm);
    }
    catch(Exception e)
    {
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
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
  private void removeAll(Connection con, Statement stmt, String userCode) throws Exception
  {
    stmt = con.createStatement();

    stmt.executeUpdate("DELETE FROM userservices WHERE UserCode = '" + userCode + "'");

    if(stmt != null) stmt.close();

    stmt = con.createStatement();

    stmt.executeUpdate("DELETE FROM usermodules WHERE UserCode = '" + userCode + "'");

    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void reFetch(PrintWriter out, String unm, String sid, String uty, String dnm, String men, String den, String bnm, String userCode,
                       String localDefnsDir, int[] bytesOut) throws Exception
  {
    URL url = new URL("http://" + serverUtils.serverToCall("OFSA", localDefnsDir) + "/central/servlet/AdminUserModules?unm=" + unm + "&sid=" + sid + "&uty=" + uty
                    + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=" + userCode + "&bnm=" + bnm);

    URLConnection uc = url.openConnection();
    uc.setDoInput(true);
    uc.setUseCaches(false);
    uc.setDefaultUseCaches(false);

    uc.setRequestProperty("Content-Type", "application/octet-stream");

    BufferedReader di = new BufferedReader(new InputStreamReader(uc.getInputStream()));

    String s = di.readLine();
    while(s != null)
    {
      out.println(s);
      bytesOut[0] += s.length();
      s = di.readLine();
    }

    di.close();
  }

}
