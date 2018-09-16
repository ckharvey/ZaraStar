// =======================================================================================================================================================================================================
// System: ZaraStar Site: fetch Page for editor
// Module: _8102c.java
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

public class BlogsFetchPage extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  Wiki wiki = new Wiki();
  BlogsUtils blogsUtils = new BlogsUtils();
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
      p1  = req.getParameter("p1"); // pageCode

      if(p1 == null || p1.length() == 0) p1 = "0";

      doIt(req, res, unm, sid, uty, men, den, dnm, bnm, p1, bytesOut);
    }
    catch(Exception e)
    {
      System.out.println(("Unexpected System Error: 8102c: " + e));
      res.getWriter().write("Unexpected System Error: 8102c");
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(HttpServletRequest req, HttpServletResponse res, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesLibraryDir = directoryUtils.getImagesDir(dnm);
    String flashDir         = directoryUtils.getFlashDirectory(dnm);
    String localDefnsDir    = directoryUtils.getLocalOverrideDir(dnm);

    String text = generalUtils.sanitise4(getPage(p1, unm, sid, uty, men, den, dnm, bnm, imagesLibraryDir, flashDir, localDefnsDir));

    res.getWriter().write("{ res: [{ \"msg\":\"" + text + "\"}]}");

    serverUtils.totalBytes(req, unm, dnm, 8102, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String getPage(String code, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String imageLibraryDir, String flashDir, String localDefnsDir) throws Exception
  {
    String[] owner           = new String[1];
    String[] date            = new String[1];
    String[] title           = new String[1];
    String[] type            = new String[1];
    String[] image           = new String[1];
    String[] topicName       = new String[1];
    String[] text            = new String[1];
    String[] published       = new String[1];
    String[] isAMenuItem     = new String[1];
    String[] isASpecial      = new String[1];
    String[] shortTitle      = new String[1];
    String[] displayTheTitle = new String[1];
    String[] serviceCode     = new String[1];
    String[] isTheHomePage   = new String[1];
    String[] isSharable      = new String[1];
    
    if(! blogsUtils.getGivenCode(code, owner, date, title, type, image, topicName, text, published, isAMenuItem, isTheHomePage, isASpecial, shortTitle, displayTheTitle, serviceCode, isSharable, dnm))
      return "<font size=5 color=red>Page Not Found</font>";

    return text[0];
  }

}
