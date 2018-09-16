// =======================================================================================================================================================================================================
// System: ZaraStar Connections: Utilities
// Module: ConnectionsUtils.java
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
import javax.servlet.http.*;
import java.sql.*;

public class ConnectionsUtils
{
  GeneralUtils generalUtils = new GeneralUtils();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  DashboardUtils dashboardUtils = new DashboardUtils();
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void outputPageFrame(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String bodyStr, String callingServlet, String service, String unm, String sid, String uty, String men, String den, String dnm,
                              String bnm, String otherSetup, String localDefnsDir, String defnsDir, int[] hmenuCount, int[] bytesOut) throws Exception
  {
    String subMenuText = dashboardUtils.buildSubMenuText(con, stmt, rs, req, service, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount);

    scoutln(out, bytesOut, authenticationUtils.buildScreen(con, stmt, rs, req, true, unm, sid, uty, men, den, dnm, bnm, callingServlet, subMenuText, hmenuCount[0], bodyStr, otherSetup, localDefnsDir, defnsDir));
    
    scoutln(out, bytesOut, "<table border=0><tr><td valign=top><div id='second' style=\"{margin-top:0px;}\"></div></td><td valign=top><div id='third' style=\"{margin-top:0px;margin-left:0px;}\"></div></td></tr></table>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void drawTitle(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, PrintWriter out, String title, String service, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] hmenuCount,
                        String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<table id='title' width=100%>");
    scoutln(out, bytesOut, "<tr><td colspan=2 nowrap><p>" + title + directoryUtils.buildHelp(service) + "</td></tr></table>");
    
    scoutln(out, bytesOut, "<table id='submenu' width=100%><tr><td>" + buildSubMenuText(con, stmt, rs, req, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount) + "</td></tr></table>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String buildSubMenuText(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] hmenuCount)
                                  throws Exception
  {
    String s = "<div id='submenu'>";

    hmenuCount[0] = 1;            

    s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\">";
    s += "<a href=\"/central/servlet/IMMain?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Current</a></dt></dl>";

    s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\">";
    s += "<a href=\"/central/servlet/ChannelMyChannels?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Channels</a></dt></dl>";

    if(serverUtils.isSysAdmin(con, stmt, rs, unm, dnm, sid, uty, localDefnsDir, defnsDir) || serverUtils.isDBAdmin(con, stmt, rs, unm))
    {
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\">";
      s += "<a href=\"/central/servlet/ChannelServices?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Options</a></dt></dl>";
    }
    
    s += "</div>";
    
    --hmenuCount[0];
    
    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String convertLinks(String text, String unm, String sid, String uty, String men, String den, String dnm, String bnm) throws Exception
  {
    String s="", link, linkTo, linkText;
    boolean inQuotes;

    int len = text.length();
    int x=0, y, linkLen;
    while(x < len)
    {
      if((x + 2) < len && text.charAt(x) == '_' && text.charAt(x+1) == '_' && text.charAt(x+2) == '_')
      {
        x += 3;
        while((x + 2) < len && ! (text.charAt(x) == '_' && text.charAt(x+1) == '_' && text.charAt(x+2) == '_'))
          s += text.charAt(x++);
        x += 3;
      }
      else
      if(text.charAt(x) == '[')
      {
        if(x < len && text.charAt(x + 1) == '[')
        {
          ++x;
          link = "[";
          while(x < len && text.charAt(x) != ']') // just-in-case
            link += text.charAt(x++);

          if(x == len) // unterminated potential link structure at EOF
            s += link; // append whatever it is and leave as-is
          else
          {
            if(text.charAt(x) != ']') // unterminated potential link structure
              s += link;              // append whatever it is and leave as-is
            else
            {
              x += 2; // ]]
              if(! (link.toLowerCase()).startsWith("[[blog "))
              {
                s += link;            // append whatever it is and leave as-is
              }
              else // is a link structure
              {
                linkLen = link.length();

                //y=8;
                y = 0;
                while(y < linkLen && link.charAt(y) != ' ') // just-in-case
                  ++y;
                ++y;

                while(y < linkLen && link.charAt(y) == ' ' || link.charAt(y) == '"')
                  ++y;
                if(y < linkLen && link.charAt(y - 1) == '"')
                  inQuotes = true;
                else inQuotes = false;

                linkTo = "";
                if(inQuotes)
                {
                  while(y < linkLen && link.charAt(y) != '"')
                    linkTo += link.charAt(y++);
                  ++y;
                }
                else
                {
                  while(y < linkLen && link.charAt(y) != ' ')
                    linkTo += link.charAt(y++);
                }

                while(y < linkLen && link.charAt(y) == ' ')
                  ++y;

                linkText = "";
                while(y < linkLen && link.charAt(y) != ']')
                  linkText += link.charAt(y++);

                if((link.toLowerCase()).startsWith("[[blog "))
                {
                  s += "<a href=\"http://" + men + "/central/servlet/SiteDisplayPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + linkTo + "\">" + linkText + "</a>";
                }
              }
            }
          }
        }
        else s += text.charAt(x++);
      }
      else s += text.charAt(x++);
    }

   return s;
  }

}
