// =======================================================================================================================================================================================================
// System: ZaraStar Utils: Update a PL being printed from the queue
// Module: PickingListQueuePrint.java
// Author: C.K.Harvey
// Copyright (c) 1999-2009 Christopher Harvey. All Rights Reserved.
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

public class PickingListQueuePrint extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  DocumentUtils documentUtils = new DocumentUtils();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="";

    try
    {
      directoryUtils.setContentHeaders(res);
      res.setHeader("Header", "HTTP/1.0 200 OK");
      res.setStatus(200);

      unm = req.getParameter("unm");
      uty = req.getParameter("uty");
      bnm = req.getParameter("bnm");
      sid = req.getParameter("sid");
      dnm = req.getParameter("dnm");
      men = req.getParameter("men");
      den = req.getParameter("den");
      p1  = req.getParameter("p1"); // plCode
      p2  = req.getParameter("p2"); // storeman

      doIt(req, res, p1, p2, unm, sid, uty, men, den, dnm, bnm, bytesOut);
    }
    catch(Exception e)
    {
      System.out.println(("Unexpected System Error: 3049: " + e));
      res.getWriter().write("Unexpected System Error: 3049");
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(HttpServletRequest req, HttpServletResponse res, String p1, String p2, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    String rtn="Unexpected System Error: 3049c";

    if(update(con, stmt, p1, p2, unm))
      rtn = ".";

    res.setContentType("text/xml");
    res.setHeader("Cache-Control", "no-cache");

    String s = "<msg><res>" + rtn + "</res></msg>";
    res.getWriter().write(s);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 3049, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "PL update (3049c)");
    if(con  != null) con.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean update(Connection con, Statement stmt, String plCode, String storeman, String unm) throws Exception
  {
    try
    {
      stmt = con.createStatement();

      stmt.executeUpdate("UPDATE pl SET DateTimeAssigned = NULL, DateTimeLastPrinted = NULL, AssignedStoreman = '" + generalUtils.sanitiseForSQL(storeman) + "', AssignedBy = '" + generalUtils.sanitiseForSQL(unm) + "', TimesPrinted = '1' WHERE PLCode = '"
                       + plCode + "'");

      if(stmt != null) stmt.close();

      return true;
    }
    catch(Exception e)
    {
      System.out.println("3049c: " + e);
      if(stmt != null) stmt.close();
    }

    return false;
  }

}
