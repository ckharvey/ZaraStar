// =======================================================================================================================================================================================================
// System: ZaraStar: Support: Jump into helpguide
// Module: HelpguideJump.java
// Author: C.K.Harvey
// Copyright (c) 2002-09 Christopher Harvey. All Rights Reserved.
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
import java.net.*;

public class HelpguideJump extends HttpServlet
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
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="";

    try
    {
      directoryUtils.setContentHeaders(res);
      out = res.getWriter();

      unm = req.getParameter("unm");
      sid = req.getParameter("sid");
      uty = req.getParameter("uty");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");
      p1  = req.getParameter("p1");

      if(p1 == null) p1 = "";

      doIt(out, req, p1, unm, sid, uty, men, den, dnm, bnm, bytesOut);
    }
    catch(Exception e)
    {
      StringBuffer url = req.getRequestURL();
      String urlBit="";
      int x=0;
      if(url.charAt(x) == 'h' || url.charAt(x) == 'H')
        x += 7;
      while(url.charAt(x) != '\000' && url.charAt(x) != ',' && url.charAt(x) != '/')
        urlBit += url.charAt(x++);

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "HelpguideJump", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 9008, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String p1, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    // strip any leading '_' and trailing 'a' (e.g.,)
    int x=0, len=p1.length();
    String page="";
    while(x < len)
    {
      if(p1.charAt(x) >= '0' && p1.charAt(x) <= '9')
        page += p1.charAt(x);
      ++x;
    }

    try
    {
      searchBlogs(out, page, den, bnm, bytesOut);
    }
    catch(Exception e) { System.out.println("9008 " + e); }

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 9008, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void searchBlogs(PrintWriter out, String p1, String den, String bnm, int[] bytesOut) throws Exception
  {
    URL url;

    if(p1.length() == 0)
    {
      p1 = "ZaraStar (User)";
      url = new URL("http://www.zarastar.org/central/servlet/BlogsDisplayBlogGuide?unm=&sid=&uty=A&men=www.zarastar.org&den=" + den + "&dnm=Zarastarorg&p1=" + generalUtils.sanitise(p1) + "&bnm=" + bnm);
    }
    else
    {
      p1 = "-" + p1; // turn -ve
      url = new URL("http://www.zarastar.org/central/servlet/SiteDisplayPage?unm=&sid=&uty=A&men=www.zarastar.org&den=" + den + "&dnm=Zarastarorg&p1=" + p1 + "&bnm=" + bnm);
    }

    URLConnection uc = url.openConnection();
    uc.setDoInput(true);
    uc.setUseCaches(false);
    uc.setDefaultUseCaches(false);

    uc.setRequestProperty("Content-Type", "application/octet-stream");

    BufferedReader di = new BufferedReader(new InputStreamReader(uc.getInputStream()));

    String s = di.readLine();
    bytesOut[0] += s.length();
    while(s != null)
    {
      out.println(s);
      bytesOut[0] += s.length();
      s = di.readLine();
    }

    di.close();
  }

}
