// =======================================================================================================================================================================================================
// System: ZaraStar Utils: database tab
// Module: DataBaseTab.java
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

import java.io.*;
import java.sql.*;

public class DataBaseTab
{
  GeneralUtils generalUtils = new GeneralUtils();
  ServerUtils serverUtils = new ServerUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  MessagePage messagePage = new MessagePage();
  DashboardUtils dashboardUtils = new DashboardUtils();

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir,
                   String defnsDir, String imagesDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    scoutln(out, bytesOut, "function mail(){window.location.href=\"/central/servlet/MailDataBase?unm=" + unm + "&uty=" + uty + "&sid=" + sid + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\";}");
    scoutln(out, bytesOut, "function lib(){window.location.href=\"/central/servlet/DocLibEdit?unm=" + unm + "&uty=" + uty + "&sid=" + sid + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\";}");
    scoutln(out, bytesOut, "function blogs(){window.location.href=\"/central/servlet/BlogsDateBase?unm=" + unm + "&uty=" + uty + "&sid=" + sid + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\";}");
    scoutln(out, bytesOut, "function forum(){window.location.href=\"/central/servlet/ForumDataBase?unm=" + unm + "&uty=" + uty + "&sid=" + sid + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\";}");
    scoutln(out, bytesOut, "function projects(){window.location.href=\"/central/servlet/ProjectDataBase?unm=" + unm + "&uty=" + uty + "&sid=" + sid + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\";}");

    if(serverUtils.isSysAdmin(con, stmt, rs, unm, dnm, sid, uty, localDefnsDir, defnsDir))
      scoutln(out, bytesOut, "function db(){window.location.href=\"/central/servlet/DataBaseMain?unm=" + unm + "&uty=" + uty + "&sid=" + sid + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\";}");

    scoutln(out, bytesOut, "</script>");

    dashboardUtils.drawTitle(out, "DataBase Operations", "402", unm, sid, uty, men, den, dnm, bnm, bytesOut);

    scoutln(out, bytesOut, "<form><table id=\"page\" border=0 width=100%>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    if(serverUtils.isSysAdmin(con, stmt, rs, unm, dnm, sid, uty, localDefnsDir, defnsDir))
      scoutln(out, bytesOut, "<tr><td nowrap><p><a href=\"javascript:db()\">Manage</a> Main DataBase</td></tr>");
    
    scoutln(out, bytesOut, "<tr><td nowrap><p><a href=\"javascript:mail()\">Manage</a> Mail</td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap><p><a href=\"javascript:lib()\">Manage</a> Document Library</td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap><p><a href=\"javascript:blogs()\">Manage</a> Blogs</td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap><p><a href=\"javascript:forum()\">Manage</a> Forum</td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap><p><a href=\"javascript:projects()\">Manage</a> Projects</td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "</table>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {
    out.println(str);
    bytesOut[0] += (str.length() + 2);
  }

}
