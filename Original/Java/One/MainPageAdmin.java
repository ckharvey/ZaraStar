// =======================================================================================================================================================================================================
// System: ZaraStar: Utils: MainPage - Admin
// Module: MainPageAdmin.java
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
import java.net.HttpURLConnection;
import java.net.URL;

public class MainPageAdmin extends HttpServlet
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

    String unm="", uty="", bnm="", dnm="",       men="";

    String urlBit="";

    try
    {
      res.setContentType("text/html");
      out = res.getWriter();

      unm = req.getParameter("unm");
      bnm = req.getParameter("bnm");
      dnm = req.getParameter("dnm");
      uty = req.getParameter("uty");
      men = req.getParameter("men");

      if(bnm == null || bnm.length() == 0) bnm = req.getHeader("User-Agent");

      if(uty == null) uty = "A";

      if(uty.equals("R"))
        if(! unm.endsWith("_"))
          unm += "_";

      unm = generalUtils.capitalize(generalUtils.stripLeadingAndTrailingSpaces(unm));

      if(bnm.indexOf("Blazer") != -1)
        bnm = "B";
      else
      if(bnm.indexOf("Pocket") != -1)
        bnm = "P";
      else
      if(bnm.indexOf("Black") != -1)
        bnm = "L";

      StringBuffer url = req.getRequestURL();
      int x=0;
      if(url.charAt(x) == 'h' || url.charAt(x) == 'H')
        x += 7;
      while(url.charAt(x) != '\000' && url.charAt(x) != ',' && url.charAt(x) != '/')
        urlBit += url.charAt(x++);

      doIt(out, req, unm, uty, ""+bnm.charAt(0), dnm, men);
    }
    catch(Exception e)
    {
      System.out.println("MainPageUtilsi: " + e);
      messagePage.errorPage(out, req, e, "Communications Error", unm, "", dnm, bnm, urlBit, "", "", "", "MainPageUtilsi", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 100, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush(); 
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String uty, String bnm, String dnm, String men) throws Exception
  {
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String sessionsDir = directoryUtils.getSessionsDir(dnm);

    String sid = serverUtils.newSessionID(unm, uty, dnm, sessionsDir, localDefnsDir, "");
    unm = "_" + sid;

    String p2;

    int x=0;
    String reqURL = req.getRequestURL().toString();
    p2="";
    if(! reqURL.startsWith("http://"))
      p2 = "http://";
    else x=7;
    while(reqURL.charAt(x) != '/' && reqURL.charAt(x) != '\\')
      p2 += reqURL.charAt(x++);

    display(out, p2, unm, sid, uty, men, dnm, dnm, bnm);

    if(out != null) out.flush(); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void display(PrintWriter out, String serverToCall, String unm, String sid, String uty, String men, String den, String dnm, String bnm) throws Exception
  {
    URL url = new URL("http://" + serverToCall + "/central/servlet/SignOnAdministrator?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm);

    HttpURLConnection uc = (HttpURLConnection)url.openConnection();
    uc.setDoInput(true);
    uc.setDoOutput(true);
    uc.setUseCaches(false);
    uc.setDefaultUseCaches(false);
    uc.setRequestProperty("Content-Type", "application/x-www-form.urlencoded");

    uc.setRequestMethod("GET");

    BufferedReader di = new BufferedReader(new InputStreamReader(uc.getInputStream()));
    String s = di.readLine();
    while(s != null)
    {
      out.println(s);
      s = di.readLine();
    }

    di.close();
  }
 
}
