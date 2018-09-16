// =======================================================================================================================================================================================================
// System: ZaraStar Document: Drawing Utilities
// Module: DrawingUtils.java
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
import javax.servlet.http.HttpServletRequest;
import java.sql.*;

public class DrawingUtils
{
  GeneralUtils generalUtils = new GeneralUtils();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  DashboardUtils dashboardUtils = new DashboardUtils();

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void outputPageFrame(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String service, String bodyStr, String callingServlet, String unm, String sid, String uty, String men, String den, String dnm,
                              String bnm, String otherSetup, String localDefnsDir, String defnsDir, int[] hmenuCount, int[] bytesOut) throws Exception
  {
    String subMenuText = dashboardUtils.buildSubMenuText(con, stmt, rs, req, service, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount);
 
    scoutln(out, bytesOut, authenticationUtils.buildScreen(con, stmt, rs, req, true, unm, sid, uty, men, den, dnm, bnm, callingServlet, subMenuText, hmenuCount[0], bodyStr, otherSetup, localDefnsDir, defnsDir));
  }
       
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String drawReturnOption(int[] hmenuCount, String servlet, String code, String msg, String unm, String sid, String uty, String men, String den, String dnm, String bnm) throws Exception
  {  
    String s = "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\">";
    s += "<a href=\"/central/servlet/" + servlet + "?unm=" + unm + "&sid=" + sid + "&dnm=" + dnm + "&men=" + men + "&den=" + den + "&uty=" + uty + "&bnm=" + bnm + "&p1=" + code + "\">" + msg + "</a></dt></dl>\n";
    return s;    
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String drawTitle(PrintWriter out, boolean friendlyWanted, boolean plain, String callingServlet, String servlet, String title, String service, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                        String localDefnsDir, String defnsDir, int[] hmenuCount, int[] bytesOut) throws Exception
  {
    String s = scoutln(out, bytesOut, "<table id='title' width=100%>");
    s += scoutln(out, bytesOut, "<tr><td colspan=2 nowrap><p>" + title + directoryUtils.buildHelp(service) + "</td></tr></table>");

    s += scoutln(out, bytesOut, "<table id='submenu' width=100%><tr><td>" + buildSubMenuText(friendlyWanted, plain, callingServlet, servlet, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount) + "</td></tr></table>");

    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String buildSubMenuText(boolean friendlyWanted, boolean plain, String callingServlet, String servlet, String code, String msg, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] hmenuCount)
                                  throws Exception
  {
    String s = "<div id='submenu'>";

    ++hmenuCount[0];

    if(servlet.length() > 0)
      s += drawReturnOption(hmenuCount, servlet, code, msg, unm, sid, uty, men, den, dnm, bnm);

    if(friendlyWanted && ! plain)
    {
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\">";
      s += "<a href=\"/central/servlet/" + callingServlet + "?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + code + "&p2=P\">Friendly</a></dt></dl>";
    }

    s += "</div>";

    --hmenuCount[0];

    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void drawTitleW(PrintWriter out, boolean friendlyWanted, boolean plain, String callingServlet, String servlet, String title, String service, String code, String msg, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<table id='title' width=100%>");
    scoutln(out, bytesOut, "<tr><td colspan=2 nowrap><span id='x'>" + title);
    if(service.length() > 0)
      scoutln(out, bytesOut, directoryUtils.buildHelp(service));
    scoutln(out, bytesOut, "</span></td></tr></table>");

    int hmenuCount[] = new int[1];  hmenuCount[0] = 1;

    scoutln(out, bytesOut, authenticationUtils.createHorizSetupStringW());

    scoutln(out, bytesOut, "<table id='submenuX' width=100%><tr><td>" + buildSubMenuTextW(friendlyWanted, plain, callingServlet, servlet, code, msg, hmenuCount) + "</td></tr></table>");

    --hmenuCount[0];

    scoutln(out, bytesOut, "<script language='JavaScript'>setup(" + hmenuCount[0] + ",'Y');</script>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String buildSubMenuTextW(boolean friendlyWanted, boolean plain, String callingServlet, String servlet, String code, String msg, int[] hmenuCount) throws Exception
  {
    String s = "<div id='submenu'>";

    if(servlet.length() > 0)
      s += drawReturnOptionW(hmenuCount, servlet, code, msg);

    if(friendlyWanted && ! plain)
    {
      s += "<dl><dt onmouseover=\"setup('" + hmenuCount[0]++ + "','N');\">";
      s += "<a href=\"/javascript:getHTML('" + callingServlet + "','&p1=" + code + "&p2=P')\">Friendly</a></dt></dl>";
    }

    s += "</div>";

    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String drawReturnOptionW(int[] hmenuCount, String servlet, String code, String msg) throws Exception
  {
    String s = "<dl><dt onmouseover=\"setup('" + hmenuCount[0]++ + "','N');\">";
    s += "<a href=\"/javascript:getHTML('" + servlet + "','&p1=" + code + "')\">" + msg + "</a></dt></dl>\n";
    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    if(out != null) out.println(str);
    bytesOut[0] += (str.length() + 2);
    return str + "\n";
  }

}
