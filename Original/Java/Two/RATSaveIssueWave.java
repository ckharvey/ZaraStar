// =======================================================================================================================================================================================================
// System: ZaraStar Forum: Save an issue
// Module: RATSaveIssue.java
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
import java.net.*;
import java.util.Enumeration;

public class RATSaveIssueWave extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  IssuesUtils issuesUtils = new IssuesUtils();
   AdminControlUtils adminControlUtils = new AdminControlUtils();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", ratCode="", title="", desc="", dateSubmitted="", userCode="", userCompany="", release="", category="", type="", status="", fixRelease="", dateClosed="", dateTargeted="",
           duplicateOf="", priority="", hoursSpent="", closedByUser="", isPrivate="", dateTime="", callingServlet="", service="";

    try
    {
      directoryUtils.setContentHeaders(res);
      out = res.getWriter();

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
        else
        if(name.equals("ratCode"))
          ratCode = value[0];
        else
        if(name.equals("title"))
          title = value[0];
        else
        if(name.equals("dateSubmitted"))
         dateSubmitted = value[0];
        else
        if(name.equals("userCode"))
         userCode = value[0];
        else
        if(name.equals("userCompany"))
         userCompany = value[0];
        else
        if(name.equals("release"))
         release = value[0];
        else
        if(name.equals("category"))
         category = value[0];
        else
        if(name.equals("type"))
         type = value[0];
        else
        if(name.equals("status"))
         status = value[0];
        else
        if(name.equals("fixRelease"))
         fixRelease = value[0];
        else
        if(name.equals("dateClosed"))
         dateClosed = value[0];
        else
        if(name.equals("dateTargeted"))
         dateTargeted = value[0];
        else
        if(name.equals("duplicateOf"))
         duplicateOf = value[0];
        else
        if(name.equals("priority"))
         priority = value[0];
        else
        if(name.equals("hoursSpent"))
         hoursSpent = value[0];
        else
        if(name.equals("closedByUser"))
         closedByUser = value[0];
        else
        if(name.equals("isPrivate"))
         isPrivate = value[0];
        else
        if(name.equals("service"))
         service = value[0];
        else
        if(name.equals("dateTime"))
          dateTime = value[0];
        else
        if(name.equals("p1"))
          desc = value[0];
        else
        if(name.equals("callingServlet"))
          callingServlet = value[0];
      }

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, callingServlet, ratCode, title, desc, dateSubmitted, userCode, userCompany, release, category, type, status, fixRelease, dateClosed, dateTargeted, duplicateOf, priority, hoursSpent,
           closedByUser, isPrivate, dateTime, service, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "RATSaveIssue", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 5902, bytesOut[0], 0, "ERR:" + ratCode);
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String callingServlet, String ratCode, String title, String desc, String dateSubmitted,
                    String userCode, String userCompany, String release, String category, String type, String status, String fixRelease, String dateClosed, String dateTargeted, String duplicateOf, String priority, String hoursSpent,
                    String closedByUser, String isPrivate, String dateTime, String service, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! adminControlUtils.notDisabled(con, stmt, rs, 7539) && ! uty.equals("A"))
    {
      messagePage.msgScreenW(out, req, 13, unm, sid, uty, men, den, dnm, bnm, "RATSaveIssue", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 5902, bytesOut[0], 0, "ACC:" + ratCode);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreenW(out, req, 12, unm, sid, uty, men, den, dnm, bnm, "RATSaveIssue", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 5902, bytesOut[0], 0, "SID:" + ratCode);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(callingServlet.equals("5905"))
    {
      saveHeader(out, unm, sid, uty, men, den, dnm, bnm, ratCode, title, desc, dateSubmitted, userCode, userCompany, release, category, type, status, fixRelease, dateClosed, dateTargeted, duplicateOf, priority, hoursSpent, closedByUser,
                 isPrivate, service, localDefnsDir);
    }
    else
    if(callingServlet.equals("5908"))
      saveLine(out, unm, sid, uty, men, den, dnm, bnm, ratCode, dateTime, desc, userCode, userCompany, localDefnsDir);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 5902, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), ratCode);
    if(con  != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void saveHeader(PrintWriter out, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String ratCode, String title, String desc, String dateSubmitted, String userCode, String userCompany, String release,
                          String category, String type, String status, String fixRelease, String dateClosed, String dateTargeted, String duplicateOf, String priority, String hoursSpent, String closedByUser, String isPrivate, String service,
                          String localDefnsDir) throws Exception
  {
    String t1 = generalUtils.replaceNewlinesWithSpaces(desc);

    String[] newCode = new String[1];

    if(title.length() == 0)
      title = "No Title!";

    if(status.length() == 0)
      status = "O";

    if(dateClosed.length() == 0)
      dateClosed = "1970-01-01";

    if(dateSubmitted.length() == 0)
      dateSubmitted = "1970-01-01";

    if(dateTargeted.length() == 0)
      dateTargeted = "1970-01-01";

    if(duplicateOf.length() == 0)
      duplicateOf = "0";

    if(priority.length() == 0)
      priority = "U";

    if(hoursSpent.length() == 0)
      hoursSpent = "0";

    if(isPrivate.length() == 0)
      isPrivate = "N";

    issuesUtils.addToRAT(ratCode, dateSubmitted, userCode, userCompany, title, t1, release, category, type, fixRelease, status, dateClosed, dateTargeted, duplicateOf, priority, hoursSpent, closedByUser, isPrivate, service, dnm, newCode);

    ratCode = newCode[0];

    display(out, unm, sid, uty, men, den, dnm, bnm, ratCode, localDefnsDir);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void saveLine(PrintWriter out, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String ratCode, String dateTime, String desc, String userCode, String userCompany, String localDefnsDir) throws Exception
  {
    String t1 = generalUtils.replaceNewlinesWithSpaces(desc);

    issuesUtils.addToRATL(ratCode, dateTime, userCode, userCompany, t1, dnm);

    display(out, unm, sid, uty, men, den, dnm, bnm, ratCode, localDefnsDir);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void display(PrintWriter out, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String ratCode, String localDefnsDir) throws Exception
  {
    URL url = new URL("http://" + serverUtils.serverToCall("INFO", localDefnsDir) + "/central/servlet/RATDisplayIssueWave?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=" + ratCode + "&bnm=" + bnm);

    URLConnection uc = url.openConnection();
    uc.setDoInput(true);
    uc.setUseCaches(false);
    uc.setDefaultUseCaches(false);

    uc.setRequestProperty("Content-Type", "application/octet-stream");

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
