// =======================================================================================================================================================================================================
// System: ZaraStar RAT: Display Page - fetch data for editor
// Module: RATCreateEditIssuec.java
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

public class RATPageEditWave extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  Wiki wiki = new Wiki();
  IssuesUtils issuesUtils = new IssuesUtils();
  AdminControlUtils adminControlUtils = new AdminControlUtils();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="";

    try
    {
      directoryUtils.setContentHeaders(res);
      res.setHeader("Header", "HTTP/1.0 200 OK");
      res.setStatus(200);
//      res.setContentType("text/xml");
      res.setContentType("text/html");
      res.setHeader("Cache-Control", "no-cache");

      unm = req.getParameter("unm");
      sid = req.getParameter("sid");
      uty = req.getParameter("uty");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");
      p1  = req.getParameter("p1"); // ratCode \001 dateTime; or just ratcode

      if(p1 == null || p1.length() == 0) p1 = "0";

      doIt(req, res, unm, sid, uty, men, den, dnm, bnm, p1, bytesOut);
    }
    catch(Exception e)
    {
      System.out.println(("Unexpected System Error: 5905c: " + e));
      res.getWriter().write("Unexpected System Error: 5905c");
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(HttpServletRequest req, HttpServletResponse res, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesLibraryDir = directoryUtils.getImagesDir(dnm);
    String flashDir         = directoryUtils.getFlashDirectory(dnm);
    String localDefnsDir    = directoryUtils.getLocalOverrideDir(dnm);

    int x = 0, len = p1.length();
    String ratCode = "";
    while(x < len && p1.charAt(x) != '\001')
      ratCode += p1.charAt(x++);
    ++x;
    String dateTime = "";
    while(x < len)
      dateTime += p1.charAt(x++);

    String text;
    
    if(dateTime.length() == 0) // fetching issue (not issue line)
      text = generalUtils.sanitise4(getIssue(ratCode, unm, sid, uty, men, den, dnm, bnm, imagesLibraryDir, flashDir, localDefnsDir));
    else text = generalUtils.sanitise4(getIssueLine(ratCode, dateTime, unm, sid, uty, men, den, dnm, bnm, imagesLibraryDir, flashDir, localDefnsDir));

    if(text.length() == 0)
      text = ".";

    res.getWriter().write("{ res: [{ \"msg\":\"" + text + "\"}]}");

    serverUtils.totalBytes(req, unm, dnm, 5905, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String getIssue(String ratCode, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String imageLibraryDir, String flashDir, String localDefnsDir) throws Exception
  {
    String[] title        = new String[1];
    String[] date         = new String[1];
    String[] desc         = new String[1];
    String[] userCode     = new String[1];
    String[] userCompany  = new String[1];
    String[] release      = new String[1];
    String[] category     = new String[1];
    String[] type         = new String[1];
    String[] status       = new String[1];
    String[] fixRelease   = new String[1];
    String[] dateClosed   = new String[1];
    String[] dateTargeted = new String[1];
    String[] duplicateOf  = new String[1];
    String[] priority     = new String[1];
    String[] hoursSpent   = new String[1];
    String[] closedByUser = new String[1];
    String[] isPrivate    = new String[1];
    String[] service      = new String[1];

    if(! issuesUtils.getRATGivenRATCode(ratCode, dnm, title, desc, date, userCode, userCompany, release, category, type, status, fixRelease, dateClosed, dateTargeted, duplicateOf, priority, hoursSpent, closedByUser, isPrivate, service))
      return "<font size=5 color=red>Entry Not Found</font>";

    desc[0] = wiki.convertLinksW(desc[0], unm, sid, uty, men, den, dnm, bnm, imageLibraryDir, flashDir, localDefnsDir);

    return desc[0];
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String getIssueLine(String ratCode, String dateTime, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String imageLibraryDir, String flashDir, String localDefnsDir) throws Exception
  {
    String[] desc          = new String[1];
    String[] userCode      = new String[1];
    String[] userCompany   = new String[1];

    if(! issuesUtils.getRATLineGivenRATCodeAndDateTime(ratCode, dateTime, unm, sid, uty, men, den, dnm, bnm, imageLibraryDir, flashDir, localDefnsDir, userCode, userCompany, desc))
      return "<font size=5 color=red>Entry Not Found</font>";

    return desc[0];
  }

}
