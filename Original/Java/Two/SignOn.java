// =======================================================================================================================================================================================================
// System: ZaraStar: UtilsEngine: SignOn
// Module: SignOn.java
// Author: C.K.Harvey
// Copyright (c) 1998-2009 Christopher Harvey. All Rights Reserved.
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
import java.net.URL;
import java.net.URLConnection;
import java.sql.*;

public class SignOn extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  DefinitionTables definitionTables = new DefinitionTables();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", pwd="", bnm="", dnm="", men="", den="", atunm="";

    try
    {
      directoryUtils.setContentHeaders(res);
      out = res.getWriter();

      atunm = req.getParameter("atunm");
      unm = req.getParameter("unm");
      pwd = req.getParameter("pwd");
      bnm = req.getParameter("bnm");
      dnm = req.getParameter("dnm");
      men = req.getParameter("men");
      den = req.getParameter("den");

      doIt(out, req, atunm, unm, pwd, bnm, dnm, men, den, bytesOut);
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

      System.out.println("SignOn: " + e);
      messagePage.errorPage(out, req, e, "Communications Error", unm, "", dnm, bnm, urlBit, men, den, "", "SignOn", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 400, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String atunm, String unm, String pwd, String bnm, String dnm, String men, String den, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    char[] userType  = new char[1];

    String[] sid       = new String[1];
    String[] userName  = new String[1];
    String[] actualUNM = new String[1];

    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    Connection con = null;
    Statement stmt = null;
    ResultSet rs   = null;

    try
    {
      String uName = directoryUtils.getMySQLUserName();
      String pWord = directoryUtils.getMySQLPassWord();
      Class.forName("com.mysql.jdbc.Driver").newInstance();
      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + uName + "&password=" + pWord);

      if(unm.length() == 0 || pwd.length() == 0)
      {
        messagePage.msgScreen(true, out, req, 13, unm, sid[0], "", men, den, dnm, bnm, "SignOn", "", "", "", bytesOut);
        serverUtils.etotalBytes(req, unm, dnm, 400, bytesOut[0], 0, "SIG:" + unm + ":" + pwd);
        if(con != null) con.close();
        if(out != null) out.flush();
        return;
      }

      if(! unm.equalsIgnoreCase("SysAdmin"))
      {
        String[] systemStatus = new String[1];
        definitionTables.getSystem(con, stmt, rs, dnm, systemStatus);
        if(! systemStatus[0].equals("L")) // ! live
        {
          messagePage.msgScreen(true, out, req, 1, unm, "", "", men, den, dnm, bnm, "SignOn", "", "", "", bytesOut);
          serverUtils.etotalBytes(req, unm, dnm, 400, bytesOut[0], 0, "ACC:NotLive:" + unm + ":" + pwd);
          if(con != null) con.close();
          if(out != null) out.flush();
          return;
        }
      }

      if(! authenticationUtils.validateSignOn(con, stmt, rs, req, unm, pwd, dnm, userType, sid, userName, actualUNM))
      {
        messagePage.msgScreen(true, out, req, 13, unm, sid[0], "", men, den, dnm, bnm, "SignOn", "", "", "", bytesOut);
        serverUtils.etotalBytes(req, unm, dnm, 400, bytesOut[0], 0, "SIG:" + unm + ":" + pwd);
        if(con != null) con.close();
        if(out != null) out.flush();
        return;
      }
      else // signed on ok, remove the casual SID entry from trail
      {
        removeSIDEntries(con, stmt, atunm.substring(1));
        serverUtils.removeAllSIDRelated(atunm, directoryUtils.getSessionsDir(dnm));
      }

      unm = generalUtils.capitalize(actualUNM[0]); // unm);

      reDisplay(out, unm, sid[0], ""+userType[0], dnm, men, den, bnm, localDefnsDir);
    }
    catch(Exception e) { }

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 400, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), unm);
    if(con  != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void reDisplay(PrintWriter out, String unm, String sid, String uty, String dnm, String men, String den, String bnm, String localDefnsDir) throws Exception
  {
    URL url = new URL("http://" + serverUtils.serverToCall("SIGNON", localDefnsDir) + "/central/servlet/MainPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm);

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
      s = di.readLine();
    }

    di.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void removeSIDEntries(Connection con, Statement stmt, String sid) throws Exception
  {
    try
    {
      stmt = con.createStatement();

      stmt.executeUpdate("DELETE FROM trail where UserCode = '" + sid + "'");

      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println("400a: " + e);
      if(stmt != null) stmt.close();
    }
  }

}
