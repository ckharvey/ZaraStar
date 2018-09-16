// =======================================================================================================================================================================================================
// System: ZaraStar Utils: Options tabs
// Module: OptionTabs.java
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

public class OptionTabs extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  ServerUtils serverUtils = new ServerUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  MessagePage messagePage = new MessagePage();
  DashboardUtils dashboardUtils = new DashboardUtils();
  SignOnAdministrator  signOnAdministrator  = new SignOnAdministrator();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="";

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
      p1  = req.getParameter("p1"); // option
      p2  = req.getParameter("p2"); // svc (on subsequent Access call)

      if(p2 == null) p2 = "7500";
      
      doIt(out, req, p1, p2, unm, sid, uty, men, den, dnm, bnm, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "OptionTabs", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 400, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String p1, String p2, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if((! serverUtils.isSysAdmin(con, stmt, rs, unm, dnm, sid, uty, localDefnsDir, defnsDir)) && (! serverUtils.isDBAdmin(con, stmt, rs, unm)))
    {
      messagePage.msgScreen(true, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "SignOnAdministrator", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 400, bytesOut[0], 0, "ACC:");
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(true, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "SignOnAdministrator", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 400, bytesOut[0], 0, "SID:");
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    set(con, stmt, rs, req, out, p1, p2, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, imagesDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 400, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, PrintWriter out, String p1, String p2, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir,
                   String defnsDir, String imagesDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Zara Admin</title>");

    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    if(p1.equals("A")) // access
    {
      scoutln(out, bytesOut, "function again(svc){");
      scoutln(out, bytesOut, "window.location.href=\"/central/servlet/OptionTabs?p2=\"+svc+\"&p1=A&unm=" + unm + "&uty=" + uty + "&sid=" + sid + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\";}");

      scoutln(out, bytesOut, "function modify(svc,desc){var p3=sanitise(desc);");
      scoutln(out, bytesOut, "window.location.href=\"/central/servlet/AdminAccessUpdate?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=" + p2 + "&p3=\"+p3+\"&p1=\"+svc;}");

      scoutln(out, bytesOut, "function disable(svc){");
      scoutln(out, bytesOut, "window.location.href=\"/central/servlet/AdminSuspendUpdate?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=" + p2 + "&p1=\"+svc;}");

      scoutln(out, bytesOut, "function refine(svc,desc,userCode){var p2=sanitise(desc);var p4=sanitise(userCode);");
      scoutln(out, bytesOut, "window.location.href=\"/central/servlet/AdminAccessRefinements?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p3=" + p2 + "&p2=\"+p2+\"&p4=\"+p4+\"&p1=\"+svc;}");

      scoutln(out, bytesOut, "function sanitise(code){");
      scoutln(out, bytesOut, "var code2='';var x;var len=code.length;");
      scoutln(out, bytesOut, "for(x=0;x<len;++x)");
      scoutln(out, bytesOut, "if(code.charAt(x)=='#')code2+='%23';");
      scoutln(out, bytesOut, "else if(code.charAt(x)=='\"')code2+='%22';");
      scoutln(out, bytesOut, "else if(code.charAt(x)=='&')code2+='%26';");
      scoutln(out, bytesOut, "else if(code.charAt(x)=='%')code2+='%25';");
      scoutln(out, bytesOut, "else if(code.charAt(x)==' ')code2+='%20';");
      scoutln(out, bytesOut, "else if(code.charAt(x)=='+')code2+='%2b';");
      scoutln(out, bytesOut, "else if(code.charAt(x)=='?')code2+='%3F';");
      scoutln(out, bytesOut, "else code2+=code.charAt(x);");
      scoutln(out, bytesOut, "return code2};");
    }

    scoutln(out, bytesOut, "</script>");
    scoutln(out, bytesOut, "</head><body><form>");

    signOnAdministrator.heading(out, true, unm, sid, uty, bnm, dnm, men, den, imagesDir, bytesOut);

    if(p1.equals("A")) // access
    {
      AdminControlUtils adminControlUtils = new AdminControlUtils();

      adminControlUtils.createScreen(out, p2, unm, sid, uty, men, den, dnm, bnm, bytesOut);
    }
    else
    if(p1.equals("C")) // custom
    {
      DefintionsServices defintionsServices = new DefintionsServices();

      defintionsServices.set(con, stmt, rs, out, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);
    }
    else
    if(p1.equals("U")) // users
    {
      UsersTab usersTab = new UsersTab();
      usersTab.set(out, unm, sid, uty, men, den, dnm, bnm, bytesOut);
    }
    else
    if(p1.equals("W")) // network
    {
      NetworkTab networkTab = new NetworkTab();

      networkTab.set(con, stmt, rs, out, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, imagesDir, bytesOut);
    }
    else
    if(p1.equals("D")) // database
    {
      DataBaseTab dataBaseTab = new DataBaseTab();

      dataBaseTab.set(con, stmt, rs, out, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, imagesDir, bytesOut);
    }

    scoutln(out, bytesOut, "</form>");
    scoutln(out, bytesOut, "</body></html>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {
    out.println(str);
    bytesOut[0] += (str.length() + 2);
  }

}
