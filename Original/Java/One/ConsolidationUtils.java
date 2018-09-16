// =======================================================================================================================================================================================================
// System: ZaraStar Consolidation: Utilities
// Module: ConsolidationUtils.java
// Author: C.K.Harvey
// Copyright (c) 2006-08 Christopher Harvey. All Rights Reserved.
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

public class ConsolidationUtils
{
  GeneralUtils generalUtils = new GeneralUtils();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  DashboardUtils dashboardUtils = new DashboardUtils();
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void outputPageFrame(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String callingServlet, String service, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                              String localDefnsDir, String defnsDir, int[] hmenuCount, int[] bytesOut) throws Exception
  {
    String subMenuText = dashboardUtils.buildSubMenuText(con, stmt, rs, req, service, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount);

    scoutln(out, bytesOut, authenticationUtils.buildScreen(con, stmt, rs, req, false, unm, sid, uty, men, den, dnm, bnm, callingServlet, subMenuText, hmenuCount[0], "", "", localDefnsDir, defnsDir));
    
    scoutln(out, bytesOut, "<table border=0><tr><td valign=top><div id='second' style=\"{margin-top:0px;}\"></div></td><td valign=top><div id='third' style=\"{margin-top:0px;margin-left:0px;}\"></div></td></tr></table>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String buildSubMenuText(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] hmenuCount)
                                  throws Exception
  {
    String s = "<div id='submenu'>";

    ++hmenuCount[0];

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 6901, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\">";
      s += "<a href=\"http://" + men + "/central/servlet/ConsolidationListCompanies?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=C\">Customers</a></dt></dl>";

      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\">";
      s += "<a href=\"http://" + men + "/central/servlet/ConsolidationListCompanies?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=S\">Suppliers</a></dt></dl>";

      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\">";
      s += "<a href=\"http://" + men + "/central/servlet/ConsolidationListCompanies?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=O\">Organizations</a></dt></dl>";
    }
    
    s += "</div>";
    
    --hmenuCount[0];
    
    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void drawTitle(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, PrintWriter out, String title, String service, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir,
                        String defnsDir, int[] hmenuCount, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<table id='title' width=100%>");
    scoutln(out, bytesOut, "<tr><td colspan=2 nowrap><p>" + title);
    if(service.length() > 0)
    {
      scoutln(out, bytesOut, directoryUtils.buildHelp(service));
    }
    scoutln(out, bytesOut, "</td></tr></table>");
    
    scoutln(out, bytesOut, "<table id='submenu' width=100%><tr><td>" + buildSubMenuText(con, stmt, rs, req, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount) + "</td></tr></table>");  
    
    scoutln(out, bytesOut, authenticationUtils.createHorizSetupString(hmenuCount[0]));
  }
      
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }
  
}
