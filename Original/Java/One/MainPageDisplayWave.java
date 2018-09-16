// =======================================================================================================================================================================================================
// System: ZaraStar Utils: Main Page re-display
// Module: MainPageDisplayWave.java
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
import java.sql.*;

public class MainPageDisplayWave extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  ServerUtils serverUtils = new ServerUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  MessagePage messagePage = new MessagePage();
  AdminControlUtils adminControlUtils = new AdminControlUtils();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="", p3="", p4="", which="";

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

      if(p1 == null) p1 = "";
      if(p4 == null) p4 = "";

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, p3, p4, which, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "MainPageUtilse", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 100, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush(); 
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1, String p2, String p3, String p4, String which, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String defnsDir      = directoryUtils.getSupportDirs('D');

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    set(con, stmt, rs, req, out, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, p1, p2, p3, p4, which, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 100, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, PrintWriter out, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir,
                   String currentServlet, String currentServer, String currentDNM, String docCode, String which, int[] bytesOut) throws Exception
  {
    boolean callHttp = false;

      if(uty.equals("R"))
      {
          if(adminControlUtils.notDisabled(con, stmt, rs, 901))
          {
            currentServlet = "ExternalUserServicesw";
            callHttp = true;
          }
          else
          if(adminControlUtils.notDisabled(con, stmt, rs, 5911))
            currentServlet = "RATIssuesMainWave";
          else
          if(adminControlUtils.notDisabled(con, stmt, rs, 8104))
          {
            currentServlet = "BlogsDisplayHomew";
            callHttp = true;
          }
          else
          {
            currentServlet = "SiteDisplayPageWave";
            callHttp = true;
          }
      }
      else
      if(uty.equals("I"))
      {
          currentServlet = "SiteDisplayPageWave";
          callHttp = true;
      }

    if(callHttp)
    {
      scoutln(out, bytesOut, "<script language=\"JavaScript\">");
      scoutln(out, bytesOut, "function go(){document.go2.submit();return true;}");
      scoutln(out, bytesOut, "</script>");
    }

    scoutln(out, bytesOut, "</head>");

    if(callHttp)
      scoutln(out, bytesOut, "<body onload=\"javascript:go()\">");
    else scoutln(out, bytesOut, "<body>");

    scoutln(out, bytesOut, "<form name=\"go2\" action=\"http://" + currentServer + "/central/servlet/" + currentServlet + "\" enctype=\"application/x-www-form-urlencoded\" method=POST>");

    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"unm\" value='" + unm + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"sid\" value='" + sid + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"uty\" value='" + uty + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"men\" value='" + men + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"den\" value='" + den + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"dnm\" value='" + dnm + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"bnm\" value='" + bnm + "'>");

    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"p1\" value='" + docCode + "'>"); // for direct external access
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"p2\" value='T'>"); // call from signon

    scoutln(out, bytesOut, "</form>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {
    out.println(str);
    bytesOut[0] += (str.length() + 2);
  }

}
