// =======================================================================================================================================================================================================
// System: ZaraStar: Utils: Page frame
// Module: pageFrameUtils.java
// Author: C.K.Harvey
// Copyright (c) 2006-09 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import javax.servlet.http.HttpServletRequest;
import java.sql.*;
import java.net.*;
import java.io.*;

public class PageFrameUtils
{
   GeneralUtils generalUtils = new GeneralUtils();
   AuthenticationUtils authenticationUtils = new AuthenticationUtils();
   DirectoryUtils directoryUtils = new DirectoryUtils();
   DashboardUtils dashboardUtils = new DashboardUtils();

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void outputPageFrame(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String service, String bodyStr, String callingServlet, String unm, String sid, String uty, String men, String mainDNM,
                              String dnm, String bnm, String localDefnsDir, String defnsDir, int[] hmenuCount, int[] bytesOut) throws Exception
  {
    outputPageFrame(con, stmt, rs, out, req, service, bodyStr, false, false, "", "", "", "", "", callingServlet, unm, sid, uty, men, mainDNM, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount, bytesOut);
  }
  public void outputPageFrame(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String service, String bodyStr, boolean friendlyWanted, boolean plain, String account, String dateFrom, String dateTo, String p5,
                              String p6, String callingServlet, String unm, String sid, String uty, String men, String mainDNM, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] hmenuCount, int[] bytesOut) throws Exception
  {
    outputPageFrame(con, stmt, rs, out, req, service, bodyStr, friendlyWanted, plain, account, dateFrom, dateTo, p5, p6, callingServlet, unm, sid, uty, men, mainDNM, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount, bytesOut);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void outputPageFrame(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String service, String bodyStr, boolean friendlyWanted, boolean plain, String account, String dateFrom, String dateTo, String p5,
                              String p6, String callingServlet, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String otherSetup, String localDefnsDir, String defnsDir, int[] hmenuCount, int[] bytesOut)
                              throws Exception
  {
    String subMenuText = dashboardUtils.buildSubMenuText(con, stmt, rs, req, service, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount);
    
    if(plain)
      scoutln(out, bytesOut, authenticationUtils.buildPlainScreen(con, stmt, rs, req, unm, sid, uty, men, den, dnm, bnm, callingServlet, subMenuText, hmenuCount[0], bodyStr, otherSetup, localDefnsDir, defnsDir));
    else scoutln(out, bytesOut, authenticationUtils.buildScreen(con, stmt, rs, req, true, unm, sid, uty, men, den, dnm, bnm, callingServlet, subMenuText, hmenuCount[0], bodyStr, otherSetup, localDefnsDir, defnsDir));
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String drawTitle(PrintWriter out, boolean friendlyWanted, boolean plain, String callingServlet, String account, String dateFrom, String dateTo, String p5, String p6, String title, String service, String unm, String sid, String uty,
                        String men, String den, String dnm, String bnm, int[] hmenuCount, int[] bytesOut) throws Exception
  {
    String s = scoutln(out, bytesOut, "<table id='title' width='100%'>");

    if(service.length() > 0)
      s += scoutln(out, bytesOut, "<tr><td colspan='2' nowrap='nowrap'><p>" + title + directoryUtils.buildHelp(service) + "</p></td></tr></table>");
    else s += scoutln(out, bytesOut, "<tr><td colspan='2' nowrap='nowrap'><p>" + title + "</p></td></tr></table>");

    s += scoutln(out, bytesOut, "<table id='submenu' width='100%'><tr><td>"
            + buildSubMenuText(friendlyWanted, plain, callingServlet, account, dateFrom, dateTo, p5, p6, unm, sid, uty, men, den, dnm, bnm, hmenuCount) + "</td></tr></table>");

    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void drawTitleW(PrintWriter out, boolean friendlyWanted, boolean plain, String callingServlet, String account, String dateFrom, String dateTo, String p5, String p6, String title, String service, String unm, String sid, String uty,
                         String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<table id='title' width=100%>");
    scoutln(out, bytesOut, "<tr><td colspan=2 nowrap><span id='x'>" + title);
    if(service.length() > 0)
      scoutln(out, bytesOut, directoryUtils.buildHelp(service));
    scoutln(out, bytesOut, "</span></td></tr></table>");

    int hmenuCount[] = new int[1];  hmenuCount[0] = 1;

    scoutln(out, bytesOut, "<table id='submenuX' width=100%><tr><td>" + buildSubMenuText(friendlyWanted, plain, callingServlet, account, dateFrom, dateTo, p5, p6, unm, sid, uty, men, den, dnm, bnm, hmenuCount) + "</td></tr></table>");

  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String buildSubMenuText(boolean friendlyWanted, boolean plain, String callingServlet, String account, String dateFrom, String dateTo, String p5, String p6, String unm, String sid, String uty, String men, String den, String dnm,
                                  String bnm, int[] hmenuCount) throws Exception
  {
    String s = "<div id='submenu'>";

    hmenuCount[0] = 1;

     // does friendly printing for GL
    if(friendlyWanted && ! plain)
    {
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\">";
      s += "<a href=\"/central/servlet/" + callingServlet + "?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm
              + "&bnm=" + bnm + "&p1=" + account + "&p2=" + generalUtils.convertFromYYYYMMDD(dateFrom) + "&p3="
        + generalUtils.convertFromYYYYMMDD(dateTo) + "&p5=" + p5 + "&p6=" + p6 + "&p4=P\">Friendly</a></dt></dl>";
    }

    s += "</div>";

    --hmenuCount[0];

    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void outputPageFrame(Connection con, Statement stmt, ResultSet rs, byte[] b, HttpServletRequest req, String service, String bodyStr, String callingServlet, String unm, String sid, String uty, String men, String den, String dnm,
                              String bnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    int[] hmenuCount = new int[1];

    String subMenuText = dashboardUtils.buildSubMenuText(con, stmt, rs, req, service, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount);

    scoutln(b, bytesOut, authenticationUtils.buildScreen(con, stmt, rs, req, true, unm, sid, uty, men, den, dnm, bnm, callingServlet, subMenuText, hmenuCount[0], bodyStr, "", localDefnsDir, defnsDir));
    
    scoutln(b, bytesOut, "<table border=0><tr><td valign=top><div id='second' style=\"{margin-top:0px;}\"></div></td>");
    scoutln(b, bytesOut, "<td valign=top><div id='third' style=\"{margin-top:0px;margin-left:0px;}\"></div></td></tr></table>");
  }

  public String outputPageFrame(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, String bodyStr, boolean friendlyWanted, boolean plain, String account, String dateFrom, String dateTo, String p5, String p6,
                                String callingServlet, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String otherSetup, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    int[] hmenuCount = new int[1];

    String subMenuText = buildSubMenuText2(friendlyWanted, plain, callingServlet, account, dateFrom, dateTo, p5, p6, unm, sid, uty, men, den, dnm, bnm, hmenuCount);

    return "";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String buildSubMenuText2(boolean friendlyWanted, boolean plain, String callingServlet, String account, String dateFrom, String dateTo, String p5, String p6, String unm, String sid, String uty, String men, String den, String dnm,
                                   String bnm, int[] hmenuCount) throws Exception
  {
    String s = "<div id='submenu'>";

    hmenuCount[0] = 1;            

    // does friendly printing for GL
    if(friendlyWanted && ! plain)
    {
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\">";
      s += "<a href=\"/central/servlet/" + callingServlet + "?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm
        + "&bnm=" + bnm + "&p1=" + account + "&p2=" + dateFrom + "&p3=" + dateTo + "&p5=" + p5 + "&p6=" + p6 + "&p4=P\">Friendly</a></dt></dl>";
    }
    
    s += "</div>";
    
    --hmenuCount[0];
    
    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String drawTitle(String title, String service, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    String s = "<table id='title' width=100%>";

    if(service.length() > 0)
      s += "<tr><td colspan=2 nowrap><p>" + title + directoryUtils.buildHelp(service) + "</td></tr></table>";
    else s += "<tr><td colspan=2 nowrap><p>" + title + "</td></tr></table>";

    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(byte[] b, int bytesOut[], String str) throws Exception
  {      
    generalUtils.catAsBytes(str, 0, b, false);
    bytesOut[0] += (str.length() + 2);    
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    if(out != null) out.println(str);
    bytesOut[0] += (str.length() + 2);
    return str + "\n";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // iwRtnInfo: <callbackURL> ` <unm> ` <sid> `
  public void postToIW(String iwRtnInfo, String str)
  {
    BufferedReader di = null;

    try
    {
      int x = 0, len = iwRtnInfo.length();

      String callbackURL = "";
      while(x < len && iwRtnInfo.charAt(x) != '`')
        callbackURL += iwRtnInfo.charAt(x++);
      ++x;

      if(! callbackURL.startsWith("http://"))
        callbackURL = "http://" + callbackURL;
      
      String callingUNM = "";
      while(x < len && iwRtnInfo.charAt(x) != '`')
        callingUNM += iwRtnInfo.charAt(x++);
      ++x;

      String callingSID = "";
      while(x < len && iwRtnInfo.charAt(x) != '`')
        callingSID += iwRtnInfo.charAt(x++);
      ++x;

      URL url = new URL(callbackURL + "/zs/" + callingUNM + "/" + callingSID + "/");

      HttpURLConnection uc = (HttpURLConnection)url.openConnection();
      uc.setDoInput(true);
      uc.setDoOutput(true);
      uc.setUseCaches(false);
      uc.setDefaultUseCaches(false);
      uc.setRequestProperty("Content-Type", "application/x-www-form.urlencoded");

      Integer l = new Integer(str.length());
      uc.setRequestProperty("Content-Length", l.toString());

      uc.setRequestMethod("POST");

      PrintWriter p = new PrintWriter(uc.getOutputStream());
      p.print(str);
      p.flush();
      p.close();

      di = new BufferedReader(new InputStreamReader(uc.getInputStream()));
      String s = di.readLine();
      while(s != null)
      {
        s = di.readLine();
      }

      di.close();
    }
    catch(Exception e)
    {
      System.out.println("pageFrameUtils: postToIW(): " + e);
    }

    try
    {
      if(di != null)  di.close();
    }
    catch(Exception e2) { }
  }

}
