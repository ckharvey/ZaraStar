// =======================================================================================================================================================================================================
// System: ZaraStar Admin: Category definition - fetch for edit
// Module: RATCategoryDefinition.java
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
import java.sql.*;
import java.util.Enumeration;

public class RATCategoryDefinition extends HttpServlet
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

    String unm="", sid="", uty="", dnm="", code="";

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
      }

      doIt(req, res, unm, sid, uty, dnm, code, bytesOut);
    }
    catch(Exception e)
    {
      System.out.println(("Unexpected System Error: 5907c: " + e));
      res.getWriter().write("Unexpected System Error: 5907c");
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(HttpServletRequest req, HttpServletResponse res, String unm, String sid, String uty, String dnm, String code, int[] bytesOut) throws Exception
  {
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    code = generalUtils.deSanitise(code);

    String[] type = new String[1]; type[0] = "D";
    String[] posn = new String[1]; posn[0] = "1";
    String[] desc = new String[1]; desc[0] = "";

    String rtn="Unexpected System Error: 5907c";

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      rtn = "Access Denied - Duplicate SignOn or Session Timeout";
    }
    else
    {
      if(fetch(dnm, code, type, posn, desc))
        rtn = ".";
    }

    res.setContentType("text/xml");
    res.setHeader("Cache-Control", "no-cache");

    String s = "<msg><res>" + rtn + "</res><code>" + code + "</code><type>" + type[0] + "</type><posn>" + posn[0] + "</posn><desc><![CDATA[" +desc[0] + "]]></desc></msg>";
    res.getWriter().write(s);

    serverUtils.totalBytes(req, unm, dnm, 5907, bytesOut[0], 0, code);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // rtns: ' ' if rec added/updated ok
  //       'E' if adding a rec that already exists
  //       'X' other error
  private boolean fetch(String dnm, String code, String[] type, String[] posn, String[] desc) throws Exception
  {
    Connection con = null;
    Statement stmt = null;
    ResultSet rs   = null;

    try
    {
      String uName    = directoryUtils.getMySQLUserName();
      String passWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();

      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_info?user=" + uName + "&password=" + passWord);

      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT Type, Position, Description FROM ratcat WHERE Name = '" + generalUtils.sanitiseForSQL(code) + "'");

      if(rs.next())
      {
        type[0] = rs.getString(1);
        posn[0] = rs.getString(2);
        desc[0] = rs.getString(3);
      }

      if(type[0] == null || type[0].length() == 0)
        type[0] = "I";

      if(posn[0] == null || posn[0].length() == 0)
       posn[0] = "1";

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
      if(con  != null) con.close();

      return true;
    }
    catch(Exception e)
    {
      System.out.println("5907c: " + e);
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }

    return false;
  }

}
