// =======================================================================================================================================================================================================
// System: ZaraStar Product: Cycle counting mfr definition - update
// Module: ProductCycleCountDefinitionUpdate.java
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

public class ProductCycleCountDefinitionUpdate extends HttpServlet
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

    String unm="", sid="", uty="", dnm="", mfr="", year="", frequency="", priority="", newOrEdit="";

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
          year = value[0];
        else
        if(elementName.equals("p2"))
          mfr = value[0];
        else
        if(elementName.equals("p3"))
          frequency = value[0];
        else
        if(elementName.equals("p4"))
          priority = value[0];
        else
        if(elementName.equals("p5"))
          newOrEdit = value[0];
      }

      doIt(req, res, unm, sid, uty, dnm, year, mfr, frequency, priority, newOrEdit, bytesOut);
    }
    catch(Exception e)
    {
      System.out.println(("Unexpected System Error: 3081d: " + e));
      res.getWriter().write("Unexpected System Error: 3081d");
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(HttpServletRequest req, HttpServletResponse res, String unm, String sid, String uty, String dnm, String year, String mfr, String frequency, String priority, String newOrEdit, int[] bytesOut) throws Exception
  {
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    String rtn="Unexpected System Error: 3081d";

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      rtn = "Access Denied - Duplicate SignOn or Session Timeout";
    }
    else
    {
      frequency = generalUtils.stripAllSpaces(frequency);

      if(mfr.length() == 0)
        rtn = "No Manufacturer Entered";
      else
      if(frequency.length() == 0)
        rtn = "No Frequency Entered";
      else
      if(! generalUtils.isInteger(frequency))
        rtn = "Frequency must be an Integer";
      else
      {
        boolean newRec;
        if(newOrEdit.equals("N"))
          newRec = true;
        else newRec = false;

        switch(update(newRec, dnm, year, mfr, frequency, priority))
        {
          case ' ' : rtn = ".";                           break;
          case 'E' : rtn = "Manufacturer Already Exists"; break;
        }
      }
    }

    res.setContentType("text/xml");
    res.setHeader("Cache-Control", "no-cache");

    String s = "<msg><res>" + rtn + "</res></msg>";
    res.getWriter().write(s);

    serverUtils.totalBytes(req, unm, dnm, 3081, bytesOut[0], 0, year+":"+mfr);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // rtns: ' ' if rec added/updated ok
  //       'E' if adding a rec that already exists
  //       'X' other error
  private char update(boolean newRec, String dnm, String year, String mfr, String frequency, String priority) throws Exception
  {
    Connection con = null;
    Statement stmt = null;
    ResultSet rs   = null;

    try
    {
      String uName    = directoryUtils.getMySQLUserName();
      String passWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();

      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + uName + "&password=" + passWord);

      if(newRec)
      {
        // if adding a new rec, check if mfr already exists
        boolean alreadyExists = false;
        stmt = con.createStatement();

        rs = stmt.executeQuery("SELECT Manufacturer FROM cyclel WHERE Year = '" + year + "' AND Manufacturer = '" + generalUtils.sanitiseForSQL(mfr) + "'");
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

        String q = "INSERT INTO cyclel ( Year, Manufacturer, Frequency, Priority ) VALUES ('" + year + "','" + generalUtils.sanitiseForSQL(mfr) + "','" + frequency + "','" + priority + "')";
        stmt.executeUpdate(q);

        if(stmt != null) stmt.close();
        if(con  != null) con.close();

        return ' ';
      }

      // else update existing rec
      stmt = con.createStatement();

      String q = "UPDATE cyclel SET Frequency = '" + frequency + "', Priority = '" + priority + "' WHERE Year = '" + year + "' AND Manufacturer = '" + generalUtils.sanitiseForSQL(mfr) + "'";
      stmt.executeUpdate(q);

      if(stmt != null) stmt.close();
      if(con  != null) con.close();

      return ' ';
    }
    catch(Exception e)
    {
      System.out.println("3081d: " + e);
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }

    return 'X';
  }

}
