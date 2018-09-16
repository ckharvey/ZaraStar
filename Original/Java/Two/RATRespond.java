// =======================================================================================================================================================================================================
// System: ZaraStar RAT: respond to issue
// Module: RATRespond.java
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

public class RATRespond extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
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

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="", p3="";

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
      p1  = req.getParameter("p1"); // code
      p2  = req.getParameter("p2"); // dateTime
      p3  = req.getParameter("p3"); // Respond or Edit

      if(p1 == null) p1 = "";
      if(p2 == null) p2 = "";
      if(p3 == null) p3 = "R";

      doIt(out, req, p1, p2, p3, unm, sid, uty, men, den, dnm, bnm, bytesOut);
    }
    catch(Exception e)
    {
      StringBuffer url = req.getRequestURL();
      int x=0;
      if(url.charAt(x) == 'h' || url.charAt(x) == 'H')
        x += 7;
      String urlBit="";
      while(url.charAt(x) != '\000' && url.charAt(x) != ',' && url.charAt(x) != '/')
        urlBit += url.charAt(x++);

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "RATRespond", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 5908, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String p1, String p2, String p3, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imageLibraryDir  = directoryUtils.getImagesDir(dnm);
    String flashDir         = directoryUtils.getFlashDirectory(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! adminControlUtils.notDisabled(con, stmt, rs, 7539) && ! uty.equals("A"))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "RATRespond", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 5908, bytesOut[0], 0, "ACC:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "RATRespond", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 5908, bytesOut[0], 0, "SID:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    set(con, stmt, rs, out, req, p1, p2, p3, unm, sid, uty, men, den, dnm, bnm, imageLibraryDir, flashDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 5908, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String p1, String p2, String p3, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String imageLibraryDir,
                   String flashDir, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Issue Response</title>");

    String cssDirectory = directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir);

    scoutln(out, bytesOut, "<script language=\"JavaScript\" type=\"text/javascript\" src=\"" + directoryUtils.getEditorDirectory() + "editor.js\"></script>");

    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + cssDirectory + "general.css\">");

    int[] hmenuCount = new int[1];

    issuesUtils.outputPageFrame(con, stmt, rs, out, req, "RATRespond", "5908", "", "document.getElementById('wikiview').contentWindow.document.designMode='on';", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    issuesUtils.drawTitle(con, stmt, rs, req, out, "Respond to Issue " + p1, "5908", p1, true, true, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    boolean canEditStuff = false;
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 5903, unm, uty, dnm, localDefnsDir, defnsDir))
      canEditStuff = true;

    scoutln(out, bytesOut, "<table id='page' cellspacing=2 cellpadding=0 width=100% border=0>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    String[] ratCode      = new String[1];
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

    ratCode[0] = p1;
    issuesUtils.getRATGivenRATCode(p1, dnm, title, desc, date, userCode, userCompany, release, category, type, status, fixRelease, dateClosed, dateTargeted, duplicateOf, priority, hoursSpent, closedByUser, isPrivate, service);

    if(canEditStuff && p2.length() >= 0 && p3.equals("E")) // edit
      issuesUtils.getRATLineGivenRATCodeAndDateTime(p1, p2, unm, sid, uty, men, den, dnm, bnm, imageLibraryDir, flashDir, localDefnsDir, userCode, userCompany, desc);
    else desc[0] = "";

    if(status[0].equals("O"))
      status[0] = "Open";
    else
    if(status[0].equals("F"))
      status[0] = "Awaiting Feedback";
    else status[0] = "Closed";

    scoutln(out, bytesOut, "<input type=hidden name=callingServlet value='5908'>");
    scoutln(out, bytesOut, "<input type=hidden name=userCode value=\"" + userCode[0] + "\">");
    scoutln(out, bytesOut, "<input type=hidden name=userCompany value=\"" + userCompany[0] + "\">");
    scoutln(out, bytesOut, "<input type=hidden name=ratCode value=\"" + ratCode[0] + "\">");
    scoutln(out, bytesOut, "<input type=hidden name=dateTime value=\"" + p2 + "\">");

    scoutln(out, bytesOut, "<tr><td nowrap><p><b>Issue Code:</p></td><td><p>" + ratCode[0] + "</p></td></tr>");

    scoutln(out, bytesOut, "<tr><td><p><b>Title: </td><td colspan=3><p>" + title[0] + "</td><td><p><b>Status: </td><td><p>" + status[0] + "</td></tr>");

    scoutln(out, bytesOut, "<tr><td><p><b>Category: </td><td colspan=3><p>" + category[0] + "</td><td><p><b>Type: </td><td><p>" + type[0] + "</td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td colspan=6><textarea id='text1' name='text1' cols='100' rows='20'>" + desc[0] + "</textarea>");
    scoutln(out, bytesOut, "<script language='JavaScript'>drawEditor('text1', 'wikiview', '100%', '400', '#FF8888', '#F0F0F0', '#880000','" + unm + "','" + sid + "','" + uty + "','" + men + "','" + den + "','" + dnm + "','" + bnm
                         + "');</script></td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir));
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {
    out.println(str);
    bytesOut[0] += str.length() + 2;
  }

}
