// =======================================================================================================================================================================================================
// System: ZaraStar: UtilsEngine: SignOn
// Module: LocalAccessExecuteWave.java
// Author: C.K.Harvey
// Copyright (c) 1998-2009 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.sql.*;

public class LocalAccessExecuteWave extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  DefinitionTables definitionTables = new DefinitionTables();
  MainPageDisplayWave mainPageDisplayWave = new MainPageDisplayWave();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", pwd="", bnm="", dnm="", men="", den="", p1="", p2="", p3="", p4="", atunm="", which="";

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
      p1  = req.getParameter("p1"); // servlet
      p2  = req.getParameter("p2"); // server
      p3  = req.getParameter("p3"); // currentDNM
      p4  = req.getParameter("p4"); // docCode - for direct external access

      which  = req.getParameter("which"); // temp for ZaraWave UI

      if(p4 == null) p4 = "";

      doIt(out, req, atunm, p1, p2, p3, p4, which, unm, pwd, bnm, dnm, men, den, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, "", dnm, bnm, urlBit, men, den, "", "MainPageUtilsd", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 100, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush(); 
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String atunm, String p1, String p2, String p3, String p4, String which, String unm, String pwd, String bnm, String dnm, String men, String den, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    char[] userType  = new char[1];

    String[] sid       = new String[1];
    String[] userName  = new String[1];
    String[] actualUNM = new String[1];

    Connection con = null;
    Statement stmt = null;
    ResultSet rs   = null;

    String res = "{ res: [ { \"msg\":\"AD\" } ]}";

    try
    {
      String uName = directoryUtils.getMySQLUserName();
      String pWord = directoryUtils.getMySQLPassWord();
      Class.forName("com.mysql.jdbc.Driver").newInstance();
      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + uName + "&password=" + pWord);

      if(unm.length() == 0 || pwd.length() == 0)
      {
        out.println(res);
        serverUtils.etotalBytes(req, unm, dnm, 100, bytesOut[0], 0, "SIG:" + unm + ":" + pwd);
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
          res = "{ res: [ { \"msg\":\"NL\" } ]}";
          out.println(res);
          serverUtils.etotalBytes(req, unm, dnm, 100, bytesOut[0], 0, "ACC:NotLive:" + unm + ":" + pwd);
          if(con != null) con.close();
          if(out != null) out.flush(); 
          return;
        }
      }

      unm = generalUtils.capitalize(unm);

      if(! authenticationUtils.validateSignOn(con, stmt, rs, req, unm, pwd, dnm, userType, sid, userName, actualUNM))
      {
        out.println(res);
        serverUtils.etotalBytes(req, unm, dnm, 100, bytesOut[0], 0, "SIG:" + unm + ":" + pwd);
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

      res = "{ res: [ { \"msg\":\"OK" + userType[0] + unm + "\001" + sid[0] + "\" } ]}";
    }
    catch(Exception e) { System.out.println("100d: " + e); }

    out.println(res);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 100, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), unm);
    if(con != null) con.close();
    if(out != null) out.flush(); 
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
      System.out.println("100d: " + e);
      if(stmt != null) stmt.close();
    }
  }

}
