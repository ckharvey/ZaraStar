// =======================================================================================================================================================================================================
// System: ZaraStar Utils: fetch libraries
// Module: AdminControlUtilsLibraries.java
// Author: C.K.Harvey
// Copyright (c) 2002-09 Christopher Harvey. All Rights Reserved.
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

public class AdminControlUtilsLibraries extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  AdminUtils adminUtils = new AdminUtils();
  DefinitionTables definitionTables = new DefinitionTables();
  Profile profile = new Profile();
  AdminControlUtils adminControlUtils = new AdminControlUtils();

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
      res.setContentType("text/html");
      res.setHeader("Cache-Control", "no-cache");

      unm = req.getParameter("unm");
      sid = req.getParameter("sid");
      uty = req.getParameter("uty");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");

      doIt(req, res, unm, sid, uty, men, den, dnm, bnm, bytesOut);
    }
    catch(Exception e)
    {
      System.out.println(("Unexpected System Error: 7599b: " + e));
      res.getWriter().write("Unexpected System Error: 7599b");
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(HttpServletRequest req, HttpServletResponse res, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    res.getWriter().write( mergeData(req, unm, sid, uty, men, den, dnm, bnm, bytesOut));

    serverUtils.totalBytes(req, unm, dnm, 7599, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private String mergeData(HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    int[] count = new int[1];  count[0] = 1;

    String s = "";
    if(uty.equals("I"))
    {
      s = getItems(count);
      --count[0];
    }
    else s = "\"x11\":\"Sign-In First\",\"x12\":\"AboutZaraw\",\"x13\":\"\"";

    return "{res:[{\"msg\":\"" + count[0] + "\"," + s + "}]}";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String addOption(String label, String servlet, String params, int[] count)
  {
    String s = "";

    try
    {
      if(count[0] > 1)
        s += ",";
      s += "\"x" + count[0] + "1\":\"" + label + "\",\"x" + count[0] + "2\":\"" + servlet + "\",\"x" + count[0] + "3\":\"" + params + "\"";
      ++count[0];
    }
    catch(Exception e) { }

    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String getItems(int[] count) throws Exception
  {
    String s = addOption("Documents", "LibraryListDirectoryWave", "", count);

    return s;
  }

}
