// =======================================================================================================================================================================================================
// System: ZaraStar: Documents: get data from PL queue
// Module: PickingListQueueGet.java
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

public class PickingListQueueGet extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  DocumentUtils documentUtils = new DocumentUtils();
  MiscDefinitions miscDefinitions = new MiscDefinitions();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="";

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
      }

      doIt(req, res, unm, sid, uty, men, den, dnm, bnm, bytesOut);
    }
    catch(Exception e)
    {
      System.out.println(("Unexpected System Error: 3049: " + e));
      res.getWriter().write("Unexpected System Error: 3049");
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(HttpServletRequest req, HttpServletResponse res, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    String rtn="Unexpected System Error: 3049a";

    String stuff="";

    boolean _3051 = authenticationUtils.verifyAccess(con, stmt, rs, req, 3051, unm, uty, dnm, localDefnsDir, defnsDir);

    String defaultPrinter = miscDefinitions.getPickingListQueueDefaultPrinter(con, stmt, rs);

    stuff = getData(con, stmt, rs, _3051, defaultPrinter);
    if(stuff.length() > 0)
      rtn = ".";

    res.setContentType("text/xml");
    res.setHeader("Cache-Control", "no-cache");

    String s = "<msg><res>" + rtn + "</res><stuff><![CDATA[" + stuff + "]]></stuff></msg>";
    res.getWriter().write(s);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 3049, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "Get PL Data (3049a)");
    if(con  != null) con.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String getData(Connection con, Statement stmt, ResultSet rs, boolean _3051, String defaultPrinter) throws Exception
  {
    String storemen = documentUtils.getStoremanDDL(con, stmt, rs, false);

    String rtn = "";

    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT PLCode, DateTimeQueued, SalesPerson FROM pl WHERE Status != 'C' AND ReleaseToStore = 'Y' AND TimesPrinted = '0' ORDER BY DateTimeQueued");

      String plCode, dateTimeQueued, salesPerson, cssFormat = "line1";
      int count = 0;

      while(rs.next())
      {
        plCode         = rs.getString(1);
        dateTimeQueued = rs.getString(2);
        salesPerson    = rs.getString(3);

        if(cssFormat.equals("line1")) cssFormat = "line2"; else cssFormat = "line1";

        rtn += "<tr id=\"" + cssFormat + "\"><td><p><a href=\"javascript:view('" + plCode + "')\">" + plCode + "</a></td><td><p>" + generalUtils.convertFromTimestamp(dateTimeQueued) + "</td><td><p>" + salesPerson + "</td><td><p><select name='storeman"
            + count + "'>" + storemen + "</select></td>";
        
        if(_3051)
        {
          rtn += "<td nowrap><p><a href=\"javascript:defaultPrint('" + plCode + "','" + count + "')\">" + defaultPrinter + "</a>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>";
          rtn += "<td><p><a href=\"javascript:del('" + plCode + "')\"><font size=1>Remove</font></a></td>";
        }
        rtn += "</tr>";

        ++count;
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

    if(rtn.length() == 0)
      rtn = "<tr><td>&nbsp;</td></tr><tr><td><p><b>The Picking List Queue is Empty.</td></tr>";

    return rtn;
  }

}
