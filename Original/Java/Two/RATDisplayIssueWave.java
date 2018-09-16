// =======================================================================================================================================================================================================
// System: ZaraStar RAT: Display Issue
// Module: RATDisplayIssue.java
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

public class RATDisplayIssueWave extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
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
      p1  = req.getParameter("p1"); // ratCode

      doIt(out, req, p1, unm, sid, uty, men, den, dnm, bnm, bytesOut);
    }
    catch(Exception e)
    {
      messagePage.msgScreenW(out, req, 3, unm, sid, uty, men, den, dnm, bnm, "5901", "", "", e.toString(), "", "", directoryUtils.getSupportDirs('D'), bytesOut);

      serverUtils.etotalBytes(req, unm, dnm, 5901, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String ratCode, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imageLibraryDir  = directoryUtils.getImagesDir(dnm);
    String flashDir         = directoryUtils.getFlashDirectory(dnm);
    String defnsDir         = directoryUtils.getSupportDirs('D');
    String localDefnsDir    = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir        = directoryUtils.getSupportDirs('I');

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if((uty.equals("R") && ! authenticationUtils.verifyAccess(con, stmt, rs, req, 923, unm, uty, dnm, localDefnsDir, defnsDir)) || (uty.equals("I") && ! adminControlUtils.notDisabled(con, stmt, rs, 7539)))
    {
      messagePage.msgScreenW(out, req, 13, unm, sid, uty, men, den, dnm, bnm, "RATDisplayIssue", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 5901, bytesOut[0], 0, "ACC:");
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreenW(out, req, 12, unm, sid, uty, men, den, dnm, bnm, "RATDisplayIssue", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 5901, bytesOut[0], 0, "SID:");
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    String[] title         = new String[1];
    String[] desc          = new String[1];
    String[] date          = new String[1];
    String[] userCode      = new String[1];
    String[] userCompany   = new String[1];
    String[] release       = new String[1];
    String[] category      = new String[1];
    String[] type          = new String[1];
    String[] status        = new String[1];
    String[] fixRelease    = new String[1];
    String[] dateClosed    = new String[1];
    String[] dateTargeted  = new String[1];
    String[] duplicateOf   = new String[1];
    String[] priority      = new String[1];
    String[] hoursSpent    = new String[1];
    String[] closedByUser  = new String[1];
    String[] isPrivate     = new String[1];
    String[] service       = new String[1];

    if(issuesUtils.getRATGivenRATCode(ratCode, dnm, title, desc, date, userCode, userCompany, release, category, type, status, fixRelease, dateClosed, dateTargeted, duplicateOf, priority, hoursSpent, closedByUser, isPrivate, service))
    {
      boolean canEdit = false;
      if(serverUtils.isSysAdmin(con, stmt, rs, unm, dnm, sid, uty, localDefnsDir, defnsDir) || serverUtils.isDBAdmin(con, stmt, rs, unm) || authenticationUtils.verifyAccess(con, stmt, rs, req, 5903, unm, uty, dnm, localDefnsDir, defnsDir))
        canEdit = true;

      scoutln(out, bytesOut, "5901\001Issues\001Issue: " + title[0] + "\001javascript:getHTML('RATDisplayIssueWave','')\001\001Y\001\001\003");

      set(con, stmt, rs, req, out, canEdit, ratCode, date[0], title[0], desc[0], release[0], category[0], type[0], userCode[0], userCompany[0], status[0], fixRelease[0], dateClosed[0], dateTargeted[0], duplicateOf[0], priority[0], hoursSpent[0],
          closedByUser[0], isPrivate[0], service[0],  unm, sid, uty, men, den, dnm, bnm, imageLibraryDir, flashDir, localDefnsDir, defnsDir, bytesOut);
    }
    else // not found
    {
      messagePage.msgScreenW(out, req, 6, unm, sid, uty, men, den, dnm, bnm, "5901", imagesDir, localDefnsDir, defnsDir, bytesOut);
    }

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 5901, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), ratCode);
    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, PrintWriter out, boolean canEdit, String ratCode, String date, String title, String desc, String release, String category, String type, String userCode,
                   String userCompany, String status, String fixRelease, String dateClosed, String dateTargeted, String duplicateOf, String priority, String hoursSpent, String closedByUser, String isPrivate, String service, String unm, String sid, String uty,
                   String men, String den, String dnm, String bnm, String imageLibraryDir, String flashDir, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    if(canEdit)
    {
      scoutln(out, bytesOut, "<script javascript=\"JavaScript\">");

      scoutln(out, bytesOut, "function edit(){getHTML('RATCreateEditIssueWave','&p2=" + category + "&p1=" + ratCode + "');}");

      scoutln(out, bytesOut, "function editl(dateTime){var p2=sanitise(dateTime);getHTML('RATRespondWave','&p3=E&p2='+p2+'&p1=" + ratCode + "');}");

      scoutln(out, bytesOut, "function del(){getHTML('RATDeleteIssueWave','&p3=" + category + "&p2=&p1=" + ratCode + "');}");

      scoutln(out, bytesOut, "function dell(dateTime){var p2=sanitise(dateTime);getHTML('RATDeleteIssueWave','&p3=&p2='+p2+'&p1=" + ratCode + "');}");

      scoutln(out, bytesOut, "</script>");
    }

    if(! uty.equals("A"))
    {
      scoutln(out, bytesOut, "<script javascript=\"JavaScript\">");

      scoutln(out, bytesOut, "function resp(){getHTML('RATRespondWave','&p3=R&p1=" + ratCode + "');}");

      scoutln(out, bytesOut, "</script>");
    }

    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    if(title.length() == 0)
      title = "No title!";

    issuesUtils.drawTitleW(con, stmt, rs, req, out, "Issue: " + ratCode + " (" + title +")", "5901", "", true, false, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    desc = wiki.convertLinksW(desc, unm, sid, uty, men, den, dnm, bnm, imageLibraryDir, flashDir, localDefnsDir);

    String categoryType = issuesUtils.getTypeGivenName(category, dnm);

    if(status.equals("O"))
      status = "Open";
    else
    if(status.equals("F"))
      status = "Awaiting Feedback";
    else status = "Closed";

    if(categoryType.equals("P") || categoryType.equals("R"))
    {
      if(priority.equals("L"))
        priority = "Low";
      else
      if(priority.equals("M"))
        priority = "Medium";
      else
      if(priority.equals("H"))
        priority = "High";
      else
      if(priority.equals("C"))
        priority = "Critical";
      else priority = "Unassigned";
    }

    if(dateTargeted.equals("1970-01-01"))
      dateTargeted = "";

    if(dateClosed.equals("1970-01-01"))
      dateClosed = "";

    if(duplicateOf.equals("0"))
      duplicateOf = "";

    if(closedByUser.length() == 0)
      closedByUser = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";

    if(duplicateOf.length() == 0)
      duplicateOf = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";

    scoutln(out, bytesOut, "<table id='page' cellspacing=2 cellpadding=0 width=100% border=0>");

    if(! uty.equals("A"))
      scoutln(out, bytesOut, "<tr><td colspan=6><p><a href=\"javascript:resp()\"><i>Respond to this Issue</a></td></tr>");

    scoutln(out, bytesOut, "<tr><td colspan=6><hr></td></tr>");

    if(categoryType.equals("P") || categoryType.equals("R")) // Problem || Request
    {
      scoutln(out, bytesOut, "<tr><td><p><b>Title: </td><td colspan=3><p>" + title + "</td><td><p><b>Status: </td><td><p>" + status + "</td></tr>");

      scoutln(out, bytesOut, "<tr><td><p><b>Category: </td><td colspan=3><p>" + category + "</td>");
      if(categoryType.equals("P")) // Problem
        scoutln(out, bytesOut, "<td><p><b>Type: </td><td><p>" + type + "</td>");
      scoutln(out, bytesOut, "</tr>");

      scoutln(out, bytesOut, "<tr><td><p><b>Service: </td><td colspan=3><p>" + service + "</td></tr>");

      scoutln(out, bytesOut, "<tr><td><p><b>Closed By: </td><td colspan=3><p>" + closedByUser + "</td><td><p><b>Date Closed: </td><td><p>" + dateClosed + "</td></tr>");

      scout(out, bytesOut, "<tr>");

      if(userCompany.length() > 0)
        userCompany = "(" + userCompany + ")";

      scoutln(out, bytesOut, "<tr><td><p><b>Submitted By: </td><td colspan=3><p>" + userCode + " " + userCompany + "</td><td><p><b>Date Submitted: </td><td><p>" + date + "</td></tr>");

      scoutln(out, bytesOut, "<tr><td><p><b>Release: </td><td colspan=3><p>" + release + "</td><td><p><b>Duplicate Of: </td><td><p>" + duplicateOf + "</td></tr>");

      scoutln(out, bytesOut, "<tr><td><p><b>Priority: </td><td colspan=3><p>" + priority + "</td><td><p><b>Date Targeted: </td><td><p>" + dateTargeted + "</td></tr>");

      scoutln(out, bytesOut, "<tr><td><p><b>Fixed in Release: </td><td colspan=3><p>" + fixRelease + "</td><td><p><b>Hours Spent: </td><td><p>" + hoursSpent + "</td>");

      scoutln(out, bytesOut, "<tr><td valign=top colspan=5><p><b>Description:</td>");

      scout(out, bytesOut, "<td align=right><p>Is ");
      if(! isPrivate.equals("Y"))
        scout(out, bytesOut, "Not ");
      scout(out, bytesOut, "Private</td></tr>");

      if(canEdit)
        scoutln(out, bytesOut, "<tr><td align=right colspan=6><p><a href=\"javascript:edit()\">Edit</a>&nbsp;&nbsp;<a href=\"javascript:del()\"><font size=1>Delete</a></td></tr>");

      scoutln(out, bytesOut, "<tr><td colspan=6><p>" + desc + "</td></tr>");
    }
    else // Discussion
    {
      scoutln(out, bytesOut, "<tr><td><p><b>Title: </td><td colspan=3><p>" + title + "</td><td><p><b>Status: </td><td><p>" + status + "</td></tr>");

      scoutln(out, bytesOut, "<tr><td><p><b>Category: </td><td colspan=3><p>" + category + "</td><td><p><b>Service: </td><td><p>" + service + "</td></tr>");

      scoutln(out, bytesOut, "<tr><td><p><b>Closed By: </td><td colspan=3><p>" + closedByUser + "</td><td><p><b>Date Closed: </td><td><p>" + dateClosed + "</td></tr>");

      scout(out, bytesOut, "<tr>");

      if(userCompany.length() > 0)
      userCompany = "(" + userCompany + ")";

      scoutln(out, bytesOut, "<tr><td><p><b>Submitted By: </td><td colspan=3><p>" + userCode + " " + userCompany + "</td><td><p><b>Date Submitted: </td><td><p>" + date + "</td></tr>");

      scoutln(out, bytesOut, "<tr><td valign=top colspan=5><p><b>Description:</td>");

      scout(out, bytesOut, "<td align=right><p>Is ");
      if(! isPrivate.equals("Y"))
        scout(out, bytesOut, "Not ");
      scout(out, bytesOut, "Private</td></tr>");

      if(canEdit)
        scoutln(out, bytesOut, "<tr><td align=right colspan=6><p><a href=\"javascript:edit()\">Edit</a>&nbsp;&nbsp;<a href=\"javascript:del()\"><font size=1>Delete</a></td></tr>");

      scoutln(out, bytesOut, "<tr><td colspan=6><p>" + desc + "</td></tr>");
    }

    getRATLinesGivenRATCode(out, ratCode, canEdit, unm, sid, uty, men, den, dnm, bnm, imageLibraryDir, flashDir, localDefnsDir, bytesOut);

    if(! uty.equals("A"))
      scoutln(out, bytesOut, "<tr><td colspan=6><hr></td></tr><tr><td colspan=6><p><a href=\"javascript:resp()\"><i>Respond to this Issue</a></td></tr><tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "</table>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean getRATLinesGivenRATCode(PrintWriter out, String ratCode, boolean canEdit, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String imageLibraryDir, String flashDir, String localDefnsDir,
                                          int[] bytesOut) throws Exception
  {
    Connection con = null;
    Statement stmt = null;
    ResultSet rs = null;

    boolean res = false;

    try
    {
      String uName = directoryUtils.getMySQLUserName();
      String pWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();

      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_info?user=" + uName + "&password=" + pWord);

      stmt = con.createStatement();

      String q = "SELECT * FROM ratl WHERE RATCode='" + ratCode + "' ORDER BY DateTime";

      rs = stmt.executeQuery(q);

      String desc, dateTime, userCode, userCompany;

      while(rs.next())
      {
        dateTime    = rs.getString(2);
        userCode    = rs.getString(3);
        userCompany = rs.getString(4);
        desc        = rs.getString(5);

        desc = wiki.convertLinks(desc, unm, sid, uty, men, den, dnm, bnm, imageLibraryDir, flashDir, localDefnsDir);
        if(desc  == null) desc = "";

        scoutln(out, bytesOut, "<tr><td colspan=6><hr></td></tr>");

        if(userCompany.length() > 0)
          userCompany = "(" + userCompany + ")";

        scoutln(out, bytesOut, "<tr><td colspan=3><p><b>From: </b>" + userCode + " " + userCompany + "</td><td colspan=3 align=right><p><b>Date: </b>" + generalUtils.convertFromTimestampDoW(dateTime) + "</td></tr>");

        if(canEdit)
          scoutln(out, bytesOut, "<tr><td align=right colspan=6><p><a href=\"javascript:editl('" + dateTime + "')\">Edit</a>&nbsp;&nbsp;<a href=\"javascript:dell('" + dateTime + "')\"><font size=1>Delete</a></td></tr>");

        scoutln(out, bytesOut, "<tr><td colspan=6><p>" + desc + "</td></tr>");
      }

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }

    return res;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scout(PrintWriter out, int bytesOut[], String str) throws Exception
  {
    out.print(str);
    bytesOut[0] += str.length();
  }
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {
    out.println(str);
    bytesOut[0] += str.length() + 2;
  }

}
