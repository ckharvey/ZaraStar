// =======================================================================================================================================================================================================
// System: ZaraStar WOs: get mfrs DDL
// Module: WorksOrderManufacturers.java
// Author: C.K.Harvey
// Copyright (c) 2006-09 Christopher Harvey. All Rights Reserved.
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
import java.util.Enumeration;

public class WorksOrderManufacturers extends HttpServlet
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

    String unm="", sid="", uty="", dnm="", mfr="", line="";

    try
    {
      directoryUtils.setContentHeaders(res);
      res.setHeader("Header", "HTTP/1.0 200 OK");
      res.setStatus(200);

      Enumeration en = req.getParameterNames();
      while(en.hasMoreElements())
      {
        String elementName = (String)en.nextElement();
        String[] value = req.getParameterValues(elementName);
        if(elementName.equals("unm"))
          unm = value[0];
        else
        if(elementName.equals("sid"))
          sid = value[0];
        else
        if(elementName.equals("uty"))
          uty = value[0];
        else
        if(elementName.equals("dnm"))
          dnm = value[0];
        else
        if(elementName.equals("p1"))
          mfr = value[0];
        else
        if(elementName.equals("p2"))
          line = value[0];
      }

      doIt(req, res, unm, sid, uty, dnm, mfr, line, bytesOut);
    }
    catch(Exception e)
    {
      System.out.println(("Unexpected System Error: 4431c: " + e));
      res.getWriter().write("Unexpected System Error: 4431c");
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(HttpServletRequest req, HttpServletResponse res, String unm, String sid, String uty, String dnm, String mfr, String line, int[] bytesOut) throws Exception
  {
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    String rtn="Unexpected System Error: 4431c";

    String list = "";

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      rtn = "Access Denied - Duplicate SignOn or Session Timeout";
    }
    else
    {
      mfr = generalUtils.deSanitise(mfr);

      Connection con = null;
      Statement stmt = null;
      ResultSet rs   = null;

      String uName    = directoryUtils.getMySQLUserName();
      String passWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();

      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + uName + "&password=" + passWord);

      list = getMfrsDDL(con, stmt, rs, mfr, line);

      if(con != null) con.close();

      rtn = ".";
    }

    res.setContentType("text/xml");
    res.setHeader("Cache-Control", "no-cache");

    String s = "<msg><res>" + rtn + "</res><line><![CDATA[" + line + "]]></line><ddl><![CDATA[" + list + "]]></ddl></msg>";

    res.getWriter().write(s);

    serverUtils.totalBytes(req, unm, dnm, 4431, bytesOut[0], 0, mfr + ":" + line);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String getMfrsDDL(Connection con, Statement stmt, ResultSet rs, String currentMfr, String line) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT DISTINCT Manufacturer FROM stock ORDER BY Manufacturer");

    String s = "<select name='mfr" + line + "'>";

    String mfr;
    boolean oneFound = false;

    while(rs.next())
    {
      mfr = rs.getString(1);
      if(mfr.length() > 0)
      {
        if(mfr.equals(currentMfr))
          s += "<option selected value=\"" + mfr + "\">" + mfr;
        else s += "<option value=\"" + mfr + "\">" + mfr;
      }

      oneFound = true;
    }

    if(oneFound == false)
      s += "<option selected value=\"-\">-";

    s += "</select>";

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    return s;
  }

}
