// =======================================================================================================================================================================================================
// System: ZaraStar Info: Edit BlogGuide - fetch for edit
// Module: BlogsBlogGuideContentEditFetch.java
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

public class BlogsBlogGuideContentEditFetch extends HttpServlet
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

    String unm="", sid="", uty="", dnm="", name="", section="";

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
          name = value[0];
        else
        if(elementName.equals("p2"))
          section = value[0];
      }

      doIt(req, res, unm, sid, uty, dnm, name, section, bytesOut);
    }
    catch(Exception e)
    {
      System.out.println(("Unexpected System Error: 8112c: " + e));
      res.getWriter().write("Unexpected System Error: 8112c");
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(HttpServletRequest req, HttpServletResponse res, String unm, String sid, String uty, String dnm, String name, String section, int[] bytesOut) throws Exception
  {
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    String[] services = new String[1]; services[0]="";
    String[] remark   = new String[1]; remark[0]="";
    String[] title    = new String[1]; title[0]="";
    String[] abridged = new String[1]; abridged[0]="";

    String rtn = "Unexpected System Error: 8112c";

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      rtn = "Access Denied - Duplicate SignOn or Session Timeout";
    }
    else
    {
      if(fetch(dnm, name, section, title, services, remark, abridged))
        rtn = ".";

      if(title[0].length() == 0)
        title[0] = ".";

      if(services[0].length() == 0)
        services[0] = ".";

      if(remark[0].length() == 0)
        remark[0] = ".";
    }

    res.setContentType("text/xml");
    res.setHeader("Cache-Control", "no-cache");

    String s = "<msg><res>" + rtn + "</res><section><![CDATA[" + section + "]]></section><title><![CDATA[" + title[0] + "]]></title><services><![CDATA[" + services[0] + "]]></services><remark><![CDATA[" + remark[0]
             + "]]></remark><abridged><![CDATA[" + abridged[0] + "]]></abridged></msg>";

    res.getWriter().write(s);

    serverUtils.totalBytes(req, unm, dnm, 8112, bytesOut[0], 0, section);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // rtns: ' ' if rec added/updated ok
  //       'E' if adding a rec that already exists
  //       'X' other error
  private boolean fetch(String dnm, String name, String section, String[] title, String[] services, String[] remark, String[] abridged) throws Exception
  {
    Connection con = null;
    Statement stmt = null;
    ResultSet rs   = null;

    try
    {
      String uName = directoryUtils.getMySQLUserName();
      String pWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();

      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_info?user=" + uName + "&password=" + pWord);

      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT Title, Services, Remark, Abridged FROM blogguide WHERE Name = '" + generalUtils.sanitiseForSQL(name) + "' AND Section = '" + section + "'");

      if(rs.next())
      {
        title[0]    = rs.getString(1);
        services[0] = rs.getString(2);
        remark[0]   = rs.getString(3);
        abridged[0] = rs.getString(4);
      }

      if(title[0]    == null) title[0] = "";
      if(services[0] == null) services[0] = "";
      if(remark[0]   == null) remark[0] = "";
      if(abridged[0] == null) abridged[0] = "N";

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
      if(con  != null) con.close();

      return true;
    }
    catch(Exception e)
    {
      System.out.println("8112c: " + e);
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }

    return false;
  }

}
