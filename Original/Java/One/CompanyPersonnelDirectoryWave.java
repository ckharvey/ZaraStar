// =======================================================================================================================================================================================================
// System: ZaraStar Utils: Company personnel directory
// Module: CompanyPersonnelDirectoryWave.java
// Author: C.K.Harvey
// Copyright (c) 2003-09 Christopher Harvey. All Rights Reserved.
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

public class CompanyPersonnelDirectoryWave extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  DashboardUtils dashboardUtils = new DashboardUtils();
  AdminControlUtils adminControlUtils = new AdminControlUtils();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="";

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

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "CompanyPersonnelDirectory", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 128, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush(); 
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir    = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');
    String imagesLibraryDir = directoryUtils.getImagesDir(dnm);
    String sessionsDir      = directoryUtils.getSessionsDir(dnm);

    String uName = directoryUtils.getMySQLUserName();
    String pWord = directoryUtils.getMySQLPassWord();
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null, stmt2 = null, stmt3 = null;
    ResultSet rs   = null, rs2   = null, rs3   = null;

    if((uty.equals("R") && ! adminControlUtils.notDisabled(con, stmt, rs, 909)) || (uty.equals("A") && ! adminControlUtils.notDisabled(con, stmt, rs, 809)) || (uty.equals("I") && ! authenticationUtils.verifyAccess(con, stmt, rs, req, 128, unm, uty, dnm, localDefnsDir, defnsDir)))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "CompanyPersonnelDirectory", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 128, bytesOut[0], 0, "ACC:");
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "CompanyPersonnelDirectory", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 128, bytesOut[0], 0, "SID:");
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    scoutln(out, bytesOut, "128\001People\001Company People\001javascript:getHTML('CompanyPersonnelDirectoryw','')\001\001\001\001\003");

    set(con, stmt, stmt2, stmt3, rs, rs2, rs3, out, req, unm, sid, uty, men, den, dnm, bnm, imagesDir, imagesLibraryDir, sessionsDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 128, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, ResultSet rs3, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String imagesDir,
                   String imagesLibraryDir, String sessionsDir, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
//    scoutln(out, bytesOut, "<html><head><title>People</title>");

    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 12700, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function channel(userCode){var p1=sanitise(userCode);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/IMMain?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=P&p1=\"+p1;}");
    }

    scoutln(out, bytesOut, "function blog(page){");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/SiteDisplayPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+page;}");

    scoutln(out, bytesOut, "</script>");

    int[] hmenuCount = new int[1];

    hmenuCount[0] = 0;

    dashboardUtils.drawTitleW(out, "People", "128", unm, sid, uty, men, den, dnm, bnm, bytesOut);

    scoutln(out, bytesOut, "<table id=\"page\" width=100% cellpadding=2 cellspacing=0>");
    scoutln(out, bytesOut, "</tr><tr><td>&nbsp;</td></tr>");

    File path2 = new File(sessionsDir);
    String fs2[] = new String[0];
    fs2 = path2.list();

    long timeNow = ((generalUtils.todayEncoded(localDefnsDir, defnsDir) - 1) * 86400L) + generalUtils.timeNowInSecs();

    long assumedSignedOut = generalUtils.strToLong(generalUtils.getFromDefnFile("ASSUMESIGNEDOUT", "timeouts.dfn", localDefnsDir, defnsDir));
    long idleTime         = generalUtils.strToLong(generalUtils.getFromDefnFile("IDLETIME", "timeouts.dfn", localDefnsDir, defnsDir));

    String[] cssFormat = new String[1];  cssFormat[0] = "line1";

    list(con, stmt, stmt2, stmt3, rs, rs2, rs3, req, out, unm, uty, men, dnm, fs2, timeNow, assumedSignedOut, idleTime, sessionsDir, imagesDir, imagesLibraryDir, defnsDir, localDefnsDir, cssFormat, bytesOut);

    anyOutstanding(con, stmt, stmt2, rs, rs2, req, out, unm, uty, men, dnm, fs2, timeNow, assumedSignedOut, idleTime, sessionsDir, imagesDir, imagesLibraryDir, defnsDir, localDefnsDir, cssFormat, bytesOut);

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "</table>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void list(Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, ResultSet rs3, HttpServletRequest req, PrintWriter out, String unm, String uty, String men, String dnm, String[] fs2, long timeNow,
                    long assumedSignedOut, long idleTime, String sessionsDir, String imagesDir, String imagesLibraryDir, String defnsDir, String localDefnsDir, String[] cssFormat, int[] bytesOut) throws Exception
  {
    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT UserCode FROM personposition ORDER BY Position, UserCode");

      String userCode;

      while(rs.next())
      {
        userCode = rs.getString(1);

        aPerson(con, stmt2, stmt3, rs2, rs3, req, out, userCode, unm, uty, men, dnm, fs2, timeNow, assumedSignedOut, idleTime, sessionsDir, imagesDir, imagesLibraryDir, defnsDir, localDefnsDir, cssFormat, bytesOut);
      }

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void aPerson(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, HttpServletRequest req, PrintWriter out, String userCode, String unm, String uty, String men, String dnm, String[] fs2, long timeNow,
                       long assumedSignedOut, long idleTime, String sessionsDir, String imagesDir, String imagesLibraryDir, String defnsDir, String localDefnsDir, String[] cssFormat, int[] bytesOut) throws Exception
  {
    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT UserName, JobTitle, Image, Bio, BioPage FROM profiles WHERE UserCode = '" + userCode + "' AND Status = 'L' AND ShowInDirectory = 'Y'");

      String userName, jobTitle, bio, image, bioPage;

      while(rs.next())
      {
        userName        = rs.getString(1);
        jobTitle        = rs.getString(2);
        image           = rs.getString(3);
        bio             = rs.getString(4);
        bioPage         = rs.getString(5);

        if(cssFormat[0].equals("line1")) cssFormat[0] = "line2"; else cssFormat[0] = "line1";

        writeLine(con, stmt2, rs2, out, req, userCode, userName, jobTitle, bio, image, bioPage, unm, uty, dnm, men, fs2, timeNow, assumedSignedOut, idleTime, imagesDir, imagesLibraryDir, sessionsDir, localDefnsDir, defnsDir, cssFormat,
                  bytesOut);
      }

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void anyOutstanding(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, HttpServletRequest req, PrintWriter out, String unm, String uty, String men, String dnm, String[] fs2, long timeNow,
                              long assumedSignedOut, long idleTime, String sessionsDir, String imagesDir, String imagesLibraryDir, String defnsDir, String localDefnsDir, String[] cssFormat, int[] bytesOut) throws Exception
  {
    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT UserCode, UserName, JobTitle, Image, Bio, BioPage FROM profiles WHERE Status = 'L' AND ShowInDirectory = 'Y' ORDER BY UserName");

      String userCode, userName, jobTitle, bio, image, bioPage;

      while(rs.next())
      {
        userCode = rs.getString(1);

        if(! userCode.equals("___registered___") && ! userCode.equals("___casual___") && ! userCode.equals("Sysadmin"))
        {
          if(! hasPosition(con, stmt2, rs2, userCode))
          {
            userName        = rs.getString(2);
            jobTitle        = rs.getString(3);
            image           = rs.getString(4);
            bio             = rs.getString(5);
            bioPage         = rs.getString(6);

            if(cssFormat[0].equals("line1")) cssFormat[0] = "line2"; else cssFormat[0] = "line1";

            writeLine(con, stmt2, rs2, out, req, userCode, userName, jobTitle, bio, image, bioPage, unm, uty, dnm, men, fs2, timeNow, assumedSignedOut, idleTime, imagesDir, imagesLibraryDir, sessionsDir, localDefnsDir, defnsDir, cssFormat,
                      bytesOut);
          }
        }
      }

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean hasPosition(Connection con, Statement stmt, ResultSet rs, String userCode) throws Exception
  {
    int position = 0;

    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM personposition WHERE UserCode = '" + userCode + "'");

      if(rs.next())
        position = rs.getInt("rowcount");

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }

    if(position == 0)
      return false;
    return true;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void writeLine(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String userCode, String userName, String jobTitle, String bio, String image, String bioPage, String unm, String uty, String dnm,
                         String men, String[] fs2, long timeNow, long assumedSignedOut, long idleTime, String imagesDir, String imagesLibraryDir, String sessionsDir, String localDefnsDir, String defnsDir, String[] cssFormat, int[] bytesOut)
                         throws Exception
  {
    scoutln(out, bytesOut, "<tr id=\"" + cssFormat[0] + "\">");

    if(image.length() > 0 && generalUtils.fileExists(imagesLibraryDir + image))
      scoutln(out, bytesOut, "<td rowspan=4 valign=top><img src=\"http://" + men + imagesLibraryDir + image + "\" style='{border:3px ridge #000000;}' /></td>");
    else
    if(userCode.equals("Sysadmin"))
      scoutln(out, bytesOut, "<td rowspan=4 valign=top><img src=\"http://" + men + imagesDir + "zaralogosmall.png\" /></td>");
    else scoutln(out, bytesOut, "<td rowspan=3><p><i>No Image</i></td>");

    scoutln(out, bytesOut, "<td><p><b>" + userName + "</td>");

    String[] state = new String[1];

    boolean online = onlineStatus(userCode, fs2, sessionsDir, timeNow, assumedSignedOut, idleTime, state);

    scoutln(out, bytesOut, "<td><p>" + state[0]);
    if(! userCode.equals(unm))
    {
      if(   ( uty.equals("R") && adminControlUtils.notDisabled(con, stmt, rs, 920) )
         || ( uty.equals("A") && adminControlUtils.notDisabled(con, stmt, rs, 820) )
         || ( uty.equals("I") && authenticationUtils.verifyAccess(con, stmt, rs, req, 12700, unm, uty, dnm, localDefnsDir, defnsDir) ) )
      {
        scoutln(out, bytesOut, " &nbsp; &nbsp; <a href=\"javascript:channel('" + userCode + "')\">");

        if(online)
          scoutln(out, bytesOut, "Open a Channel Now</a>");
        else scoutln(out, bytesOut, "Leave an offline channel message</a>");
      }
    }

    scoutln(out, bytesOut, "</td></tr>");

    scoutln(out, bytesOut, "<tr id=\"" + cssFormat[0] + "\"><td colspan=2><p><i>" + jobTitle + "</td></tr>");

    scoutln(out, bytesOut, "<tr id=\"" + cssFormat[0] + "\"><td colspan=2><p>" + bio + "</td></tr>");

    if(bioPage.length() > 0 && ! bioPage.equals("0"))
    {
      scoutln(out, bytesOut, "<tr id=\"" + cssFormat[0] + "\"><td colspan=2><p><a href=\"javascript:blog('" + bioPage + "')\">More</a> about " + userName + "</td></tr>");
      scoutln(out, bytesOut, "<tr id=\"" + cssFormat[0] + "\"><td>&nbsp;</td></tr>");
    }
    else scoutln(out, bytesOut, "<tr id=\"" + cssFormat[0] + "\"><td colspan=3>&nbsp;</td></tr>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean onlineStatus(String userCode, String[] fs2, String sessionsDir, long timeNow, long assumedSignedOut, long idleTime, String[] state) throws Exception
  {
    int y, z, hrs;
    long diff;
    boolean done, offline, sidFound = false;
    String hrsStr;
    RandomAccessFile fh;

    byte[] lastTransactionTimeStamp = new byte[50];
    byte[] lastHeartBeatTimeStamp   = new byte[50];

    lastTransactionTimeStamp[0] = lastHeartBeatTimeStamp[0] = '\000';

    for(y=0;y<fs2.length;++y)
    {
      if(fs2[y].startsWith(userCode + ".") && fs2[y].endsWith("sid"))
        sidFound = true;
      else
      if(fs2[y].equals(userCode + ".chk"))
      {
        fh = generalUtils.fileOpen(sessionsDir + fs2[y]);
        z=0;
        try
        {
          while(true)
            lastTransactionTimeStamp[z++] = fh.readByte();
        }
        catch(Exception e) { }
        lastTransactionTimeStamp[z] = '\000';
        generalUtils.fileClose(fh);
      }
      else
      if(fs2[y].equals(userCode + ".hb"))
      {
        fh = generalUtils.fileOpen(sessionsDir + fs2[y]);
        z=0;
        try
        {
          while(true)
            lastHeartBeatTimeStamp[z++] = fh.readByte();
        }
        catch(Exception e) { }
        lastHeartBeatTimeStamp[z] = '\000';
        generalUtils.fileClose(fh);
      }
    }

    if(! sidFound)
      lastHeartBeatTimeStamp[0] = '\000';

    done = false;
    offline = true;
    if(lastHeartBeatTimeStamp[0] != '\000')
    {
      if((timeNow - generalUtils.longFromBytesCharFormat(lastHeartBeatTimeStamp, (short)0)) > assumedSignedOut)
      {
        serverUtils.removeSID(userCode, sessionsDir);
        // Offline
        done = true;
      }
    }

    if(! done)
    {
      if(lastTransactionTimeStamp[0] != '\000' && lastHeartBeatTimeStamp[0] != '\000')
      {
        diff = generalUtils.longFromBytesCharFormat(lastHeartBeatTimeStamp, (short)0) - generalUtils.longFromBytesCharFormat(lastTransactionTimeStamp, (short)0);
        if(diff > idleTime)
        {
          if(diff < 60)
            diff = 1; // min
          else diff = (diff / 60);

          if(diff == 1) // min
          {
            state[0] = ("Idle (" + generalUtils.longToStr(diff) + " min)");
            offline = false;
          }
          else
          if(diff < 60) // mins
          {
            state[0] = ("Idle (" + generalUtils.longToStr(diff) + " mins)");
            offline = false;
          }
          else
          {
            hrs = (int)(diff / 60);
            diff = diff - (hrs * 60);
            if(hrs > 1) hrsStr = "s"; else hrsStr = "";

            state[0] = ("Idle (" + generalUtils.longToStr(hrs) + " hour" + hrsStr + " " + generalUtils.longToStr(diff) + " mins)");
            offline = false;
          }
        }
        else
        {
          state[0] = "Online";
          offline = false;
        }
      }
      else state[0] = "Offline";
    }
    else state[0] = "Offline";

    return ! offline;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {
    out.println(str);
    bytesOut[0] += (str.length() + 2);
  }

}
