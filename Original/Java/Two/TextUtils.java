// =======================================================================================================================================================================================================
// System: ZaraStar Text: Utilities
// Module: TextUtils.java
// Author: C.K.Harvey
// Copyright (c) 2006-08 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.sql.*;

public class TextUtils
{
  GeneralUtils generalUtils = new GeneralUtils();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  DashboardUtils dashboardUtils = new DashboardUtils();
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void outputPageFrame(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String callingServlet, String service, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                              String bodyStr, String otherSetup, String localDefnsDir, String defnsDir, int[] hmenuCount, int[] bytesOut) throws Exception
  {
    String subMenuText = dashboardUtils.buildSubMenuText(con, stmt, rs, req, service, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount);

    scoutln(out, bytesOut, authenticationUtils.buildScreen(con, stmt, rs, req, true, unm, sid, uty, men, den, dnm, bnm, callingServlet, subMenuText, hmenuCount[0], bodyStr, otherSetup, localDefnsDir, defnsDir));
    
    scoutln(out, bytesOut, "<table border=0><tr><td valign=top><div id='second' style=\"{margin-top:0px;}\"></div></td>");
    scoutln(out, bytesOut, "<td valign=top><div id='third' style=\"{margin-top:0px;margin-left:0px;}\"></div></td></tr></table>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String buildSubMenuText(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, boolean editing, String directory, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir,
                                  String defnsDir, int[] hmenuCount) throws Exception
  {
    String s = "<div id='submenu'>";

    hmenuCount[0] = 1;            

    if(editing)
    {
      s += "<form name=\"form6103\" action=\"TextSaveText\" enctype=\"application/x-www-form-urlencoded\" method=\"POST\" >";
      s += "<script language=\"JavaScript\">function go6103(option){var d=document.getElementById('wikiview');"
        + "if(d!=null)document.getElementById('p1').value=d.contentWindow.document.body.innerHTML;";
      
      s += "document.forms['form6103'].submit();}</script>";
      s += "<input type=\"hidden\" name=\"unm\" value='" + unm + "'>";
      s += "<input type=\"hidden\" name=\"sid\" value='" + sid + "'>";
      s += "<input type=\"hidden\" name=\"uty\" value='" + uty + "'>";
      s += "<input type=\"hidden\" name=\"men\" value='" + men + "'>";
      s += "<input type=\"hidden\" name=\"den\" value='" + den + "'>";
      s += "<input type=\"hidden\" name=\"dnm\" value='" + dnm + "'>";
      s += "<input type=\"hidden\" name=\"bnm\" value='" + bnm + "'>";
      s += "<input type=\"hidden\" id=\"p1\" name=\"p1\" value=''>";
    }

    if(! editing)
    {
      if(authenticationUtils.verifyAccess(con, stmt, rs, req, 6100, unm, uty, dnm, localDefnsDir, defnsDir))
      {
        s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\">";
        s += "<a href=\"/central/servlet/TextMaintainDirectories?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=M&bnm=" + bnm
          + "\">Directories</a></dt></dl>";
      }

      if(authenticationUtils.verifyAccess(con, stmt, rs, req, 6102, unm, uty, dnm, localDefnsDir, defnsDir))
      {
        s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\">";
        s += "<a href=\"/central/servlet/TextUploadFile?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
          + "\">Upload</a></dt></dl>";
      }
     
      if(authenticationUtils.verifyAccess(con, stmt, rs, req, 6103, unm, uty, dnm, localDefnsDir, defnsDir))
      {
        s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\">";
        s += "<a href=\"/central/servlet/TextEditText?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p2=" + directory
          + "&bnm=" + bnm + "\">Create</a></dt></dl>";
      }     
    }
    else // editing
    {
      if(authenticationUtils.verifyAccess(con, stmt, rs, req, 6103, unm, uty, dnm, localDefnsDir, defnsDir))
      {
        s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\">";
        s += "<a href=\"javascript:go6103('Y')\">Save</a></dt></dl>";
      }     
    }

    s += "</div>";
    
    --hmenuCount[0];
    
    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void drawTitle(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, boolean editing, String directory, String title, String service, String unm, String sid, String uty, String men, String den,
                        String dnm, String bnm, int[] hmenuCount, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<table id='title' width=100%>");
    scoutln(out, bytesOut, "<tr><td colspan=2 nowrap><p>" + title + directoryUtils.buildHelp(service) + "</td></tr></table>");

    scoutln(out, bytesOut, "<table id='submenu' width=100%><tr><td>" + buildSubMenuText(con, stmt, rs, req, editing, directory, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount) + "</td></tr></table>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void drawTitleW(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, boolean editing, String directory, String title, String service, String unm, String sid, String uty, String men, String den,
                         String dnm, String bnm, int[] hmenuCount, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<table id='title' width=100%>");
    scoutln(out, bytesOut, "<tr><td colspan=2 nowrap><span id='x'>" + title + directoryUtils.buildHelp(service) + "</span></td></tr></table>");

    scoutln(out, bytesOut, "<table id='submenu' width=100%><tr><td>" + buildSubMenuText(con, stmt, rs, req, editing, directory, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount) + "</td></tr></table>");
  }
    
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
