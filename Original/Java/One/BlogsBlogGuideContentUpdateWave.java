// ========================================================================================================================================================================================================
// System: ZaraStar Info: Edit BlogGuide Contents - update
// Module: BlogsBlogGuideContentUpdate.java
// Author: C.K.Harvey
// Copyright (c) 2006-09 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
//
// ========================================================================================================================================================================================================

package org.zarastar.zarastar;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.sql.*;
import java.util.Enumeration;

public class BlogsBlogGuideContentUpdateWave extends HttpServlet
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

    String unm="", sid="", uty="", dnm="", name="", title="", section="", services="", remark="", abridged="", newOrEdit="";

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
          title = value[0];
        else
        if(elementName.equals("p3"))
          section = value[0];
        else
        if(elementName.equals("p4"))
          services = value[0];
        else
        if(elementName.equals("p5"))
          remark = value[0];
        else
        if(elementName.equals("p6"))
          abridged = value[0];
        else
        if(elementName.equals("p7"))
          newOrEdit = value[0];
      }

      doIt(req, res, unm, sid, uty, dnm, name, title, section, services, remark, abridged, newOrEdit, bytesOut);
    }
    catch(Exception e)
    {
      System.out.println(("Unexpected System Error: 8112d: " + e));
      res.getWriter().write("Unexpected System Error: 8112d");
    }
  }

  // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(HttpServletRequest req, HttpServletResponse res, String unm, String sid, String uty, String dnm, String name, String title, String section, String services, String remark, String abridged, String newOrEdit, int[] bytesOut)
                    throws Exception
  {
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    String rtn="Unexpected System Error: 8112d";

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      rtn = "Access Denied - Duplicate SignOn or Session Timeout";
    }
    else
    {
      remark   = generalUtils.deSanitise(remark);
      title    = generalUtils.deSanitise(title);
      services = generalUtils.deSanitise(services);

      title    = generalUtils.stripLeadingAndTrailingSpaces(title);
      services = generalUtils.stripLeadingAndTrailingSpaces(services);
      section  = generalUtils.stripAllSpaces(section);
      remark   = generalUtils.stripLeadingAndTrailingSpaces(remark);

      if(title.length() == 0)
        rtn = "No Title Entered";
      else
      if(section.length() == 0)
        rtn = "No Section Entered";
      else
      {
        boolean newRec;
        if(newOrEdit.equals("N"))
          newRec = true;
        else newRec = false;

        switch(update(newRec, name, title, section, services, remark, abridged, dnm))
        {
          case ' ' : rtn = ".";
                     break;
          case 'E' : rtn = "Name and Section Already Exists";
                     break;
        }
      }
    }

    res.setContentType("text/xml");
    res.setHeader("Cache-Control", "no-cache");

    String s = "<msg><res>" + rtn + "</res></msg>";
    res.getWriter().write(s);

    serverUtils.totalBytes(req, unm, dnm, 8112, bytesOut[0], 0, section);
  }

  // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // rtns: ' ' if rec added/updated ok
  //       'E' if adding a rec that already exists
  //       'X' other error
  private char update(boolean newRec, String name, String title, String section, String services, String remark, String abridged, String dnm) throws Exception
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

      if(newRec)
      {
        // if adding a new rec, check if already exists
        boolean alreadyExists = false;
        stmt = con.createStatement();

        rs = stmt.executeQuery("SELECT Section FROM blogguide WHERE Name = '" + generalUtils.sanitiseForSQL(name) + "' AND Section = '" + generalUtils.sanitiseForSQL(section) + "'");
        if(rs.next())
          alreadyExists = true;
        if(rs != null) rs.close();

        if(stmt != null) stmt.close();

        if(alreadyExists)
        {
          if(con != null) con.close();
          return 'E';
        }

        stmt = con.createStatement();

        String q = "INSERT INTO blogguide ( Name, Title, Section, Services, Remark, Abridged) VALUES ('" + generalUtils.sanitiseForSQL(name) + "','" + generalUtils.sanitiseForSQL(title) + "','" + generalUtils.sanitiseForSQL(section) + "','" + generalUtils.sanitiseForSQL(services)
                 + "','" + generalUtils.sanitiseForSQL(remark) + "','" + abridged + "')";
        stmt.executeUpdate(q);

        if(stmt != null) stmt.close();
        if(con  != null) con.close();

        return ' ';
      }

      // else update existing rec
      stmt = con.createStatement();

      String q = "UPDATE blogguide SET Title = '" + generalUtils.sanitiseForSQL(title) + "', Services = '" + generalUtils.sanitiseForSQL(services) + "', Remark = '" + generalUtils.sanitiseForSQL(remark) + "', Abridged = '" + abridged + "' WHERE Name = '"
                + generalUtils.sanitiseForSQL(name) + "' AND Section = '" + section + "'";

      stmt.executeUpdate(q);

      if(stmt != null) stmt.close();
      if(con  != null) con.close();

      return ' ';
    }
    catch(Exception e)
    {
      System.out.println("8112d: " + e);
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }

    return 'X';
  }

}
