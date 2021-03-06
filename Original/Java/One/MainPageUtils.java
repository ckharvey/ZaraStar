// =======================================================================================================================================================================================================
// System: ZaraStar: Utils: MainPage
// Module: MainPageUtils.java
// Author: C.K.Harvey
// Copyright (c) 2006-08 Christopher Harvey. All Rights Reserved.
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
import java.net.*;
import java.sql.*;

public class MainPageUtils extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  AdminControlUtils adminControlUtils = new AdminControlUtils();
  
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }
 
  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", uty="", bnm="", dnm="", p1="", p2="", p3="", men="";

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
      p1  = req.getParameter("p1"); // servlet (if on signon-call else null)
      p2  = req.getParameter("p2"); // docCode (if on direct (ext user) signon-call else null)
      p3  = req.getParameter("p3"); // another param ==> p2

      if(p1 == null) p1 = "";
      
      if(bnm == null || bnm.length() == 0) bnm = req.getHeader("User-Agent");

      if(uty == null) uty = "A";

      if(uty.equals("R"))
      {
        if(p1.length() == 0)
          if(! unm.endsWith("_"))
            unm += "_";
      }

      if(p2 == null) p2="";
      if(p3 == null) p3="";
      
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

      doIt(out, req, p1, p2, p3, unm, uty, ""+bnm.charAt(0), dnm, men, bytesOut);
    }
    catch(Exception e)
    {
      System.out.println("MainPageUtils: " + e);
      messagePage.errorPage(out, req, e, "Communications Error", unm, "", dnm, bnm, urlBit, "", "", "", "MainPageUtils", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 100, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush(); 
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String p1, String p3, String param2, String unm, String uty, String bnm, String dnm, String men, int[] bytesOut) throws Exception
  {
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String sessionsDir   = directoryUtils.getSessionsDir(dnm);

    String p2;
    
    String sid = serverUtils.newSessionID(unm, uty, dnm, sessionsDir, localDefnsDir, "");

    if(p1 == null || p1.length() == 0)
    {
      unm = "_" + sid;

      Class.forName("com.mysql.jdbc.Driver").newInstance();
      Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
      Statement stmt = null;
      ResultSet rs   = null;

      if(adminControlUtils.notDisabled(con, stmt, rs, 5910))
      {
        RATIssuesMain rATIssuesMain = new RATIssuesMain();
        rATIssuesMain.doIt(out, req, unm, sid, uty, men,    dnm,      dnm, bnm, bytesOut);
      }
      else
      if(adminControlUtils.notDisabled(con, stmt, rs, 8104))
      {
        p2 = serverUtils.serverToCall(req, "MAINPAGESERVER", localDefnsDir);
        display(out, "BlogsDisplayHome", p2, p3, param2, unm, sid, uty, men, dnm, dnm, bnm);
      }
      else
      {
        p2 = serverUtils.serverToCall(req, "MAINPAGESERVER", localDefnsDir);
        display(out, "SiteDisplayPage", p2, p3, param2, unm, sid, uty, men, dnm, dnm, bnm);
      }

      if(con != null) con.close();
    }
    else
    {
      int x=0;
      String reqURL = req.getRequestURL().toString();
  
      p2="";
      if(! reqURL.startsWith("http://"))
        p2 = "http://";
      else x=7;
      while(reqURL.charAt(x) != '/' && reqURL.charAt(x) != '\\')
        p2 += reqURL.charAt(x++);

      men = p2;

      validateExternal(out, p2, p1, p3, param2, unm, sid, uty, men, dnm, dnm, bnm); // and subsequently call desired servlet if signon is successful

      if(out != null) out.flush(); 
      return;
    }

    if(out != null) out.flush(); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void display(PrintWriter out, String servlet, String serverToCall, String docCode, String param2, String unm, String sid, String uty, String men, String den, String dnm, String bnm) throws Exception
  {
    URL url = new URL("http://" + serverToCall + "/central/servlet/" + servlet + "?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + docCode + "&p2=" + param2);

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

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void validateExternal(PrintWriter out, String serverToCall, String servlet, String docCode, String param2, String unm, String sid, String uty, String men, String den, String dnm, String bnm) throws Exception
  {
    URL url = new URL("http://" + serverToCall + "/central/servlet/MainPageUtilsb?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + servlet + "&p2=" + docCode + "&p3=" + param2);// p3 not used

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
