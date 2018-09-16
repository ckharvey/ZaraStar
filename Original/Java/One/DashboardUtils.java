// =======================================================================================================================================================================================================
// System: ZaraStar Dashboard: Utilities
// Module: DashboardUtils.java
// Author: C.K.Harvey
// Copyright (c) 2006-09 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import java.io.*;
import java.sql.*;
import javax.servlet.http.HttpServletRequest;

public class DashboardUtils
{
  GeneralUtils generalUtils = new GeneralUtils();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  AdminControlUtils adminControlUtils = new AdminControlUtils();

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void outputPageFrame(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String callingServlet, String bodyStr, String service, String unm, String sid, String uty, String men, String den, String dnm,
                              String bnm, String otherSetup, String localDefnsDir, String defnsDir, int[] hmenuCount, int[] bytesOut) throws Exception
  {
    String subMenuText = buildSubMenuText(con, stmt, rs, req, service, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount);

    scoutln(out, bytesOut, authenticationUtils.buildScreen(con, stmt, rs, req, true, unm, sid, uty, men, den, dnm, bnm, callingServlet, subMenuText, hmenuCount[0], bodyStr, otherSetup, localDefnsDir, defnsDir));
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String buildSubMenuText(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, String service, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir,
                                 int[] hmenuCount) throws Exception
  {
    String s = "<table width=100%><tr><td>";

    s += "<div id='submenu'>";

    hmenuCount[0] = 1;

    if(adminControlUtils.notDisabled(con, stmt, rs, 7501))
    {
      if(authenticationUtils.verifyAccess(con, stmt, rs, req, 8801, unm, uty, dnm, localDefnsDir, defnsDir))
      {
        s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\">";
        s += "<a href=\"http://" + men + "/central/servlet/ContactsAddressBook?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Contacts</a></dt></dl>";
      }

      if(authenticationUtils.verifyAccess(con, stmt, rs, req, 7900, unm, uty, dnm, localDefnsDir, defnsDir))
      {
        s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\">";
        s += "<a href=\"http://" + men + "/central/servlet/TasksMain?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Tasks</a></dt></dl>";
      }

      if(uty.equals("I") && authenticationUtils.verifyAccess(con, stmt, rs, req, 12700, unm, uty, dnm, localDefnsDir, defnsDir))
      {
        s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\">";
        s += "<a href=\"http://" + men + "/central/servlet/IMMain?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">IM</a></dt></dl>";
      }

      if(authenticationUtils.verifyAccess(con, stmt, rs, req, 8014, unm, uty, dnm, localDefnsDir, defnsDir))
      {
        s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\">";
        s += "<a href=\"http://" + men + "/central/servlet/MailZaraSignOn?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Mail</a></dt></dl>";
      }

      if(authenticationUtils.verifyAccess(con, stmt, rs, req, 8114, unm, uty, dnm, localDefnsDir, defnsDir))
      {
        s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\">";
        s += "<a href=\"http://" + men + "/central/servlet/BlogsBlogRollList?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Blogs</a></dt></dl>";
      }

      if(authenticationUtils.verifyAccess(con, stmt, rs, req, 12000, unm, uty, dnm, localDefnsDir, defnsDir))
      {
        s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\">";
        s += "<a href=\"http://" + men + "/central/servlet/LibraryListDirectory?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=&p2=" + unm + "&bnm=" + bnm + "\">Documents</a></dt></dl>";
      }

      if(authenticationUtils.verifyAccess(con, stmt, rs, req, 5900, unm, uty, dnm, localDefnsDir, defnsDir))
      {
        s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\">";
        s += "<a href=\"http://www.zarastar.org/central/servlet/RATIssuesMain?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Issues</a></dt></dl>";
      }
    }

    s += "</div>";

    s += "</td></tr></table>";

    --hmenuCount[0];

    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void drawTitle(PrintWriter out, String title, String service, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<table id='title' width=100%>");
    scoutln(out, bytesOut, "<tr><td colspan=2 nowrap><p>" + title + directoryUtils.buildHelp(service) + "</td></tr></table>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void drawTitleW(PrintWriter out, String title, String service, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<table id='title' width=100%>");
    scoutln(out, bytesOut, "<tr><td colspan=2 nowrap><span='title'>" + title + directoryUtils.buildHelp(service) + "</span></td></tr></table>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {
    out.println(str);
    bytesOut[0] += (str.length() + 2);
  }


}























