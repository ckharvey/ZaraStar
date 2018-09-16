// =======================================================================================================================================================================================================
// System: ZaraStar Chat: My Channels
// Module: ChannelMyChannels.java
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

public class ChannelMyChannels extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  ConnectionsUtils connectionsUtils = new ConnectionsUtils();
  
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "ChannelMyChannels", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 12704, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush(); 
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');
    String sessionsDir   = directoryUtils.getSessionsDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null, stmt2 = null;
    ResultSet rs   = null, rs2 = null;
    
    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 12704, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "ChannelMyChannels", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 12704, bytesOut[0], 0, "ACC:");
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "ChannelMyChannels", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 12704, bytesOut[0], 0, "SID:");
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    set(con, stmt, stmt2, rs, rs2, out, req, unm, sid, uty, men, den, dnm, bnm, sessionsDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 12704, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                   String sessionsDir, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>My Channels</title>");

    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

//    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 8200, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function join(name,type){var p1=sanitise(name);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/IMMain?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p2=\"+type+\"&bnm=" + bnm + "&p1=\"+p1;}");

      scoutln(out, bytesOut, "function updateLocal(){");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/ChannelAddMessage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\";}");
    }

    scoutln(out, bytesOut, "function sanitise(code){");
    scoutln(out, bytesOut, "var code2='';var x;var len=code.length;");
    scoutln(out, bytesOut, "for(x=0;x<len;++x)");
    scoutln(out, bytesOut, "if(code.charAt(x)=='#')code2+='%23';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='\"')code2+='%22';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='&')code2+='%26';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='%')code2+='%25';");
    scoutln(out, bytesOut, "else if(code.charAt(x)==' ')code2+='%20';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='+')code2+='%2b';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='?')code2+='%3F';");
    scoutln(out, bytesOut, "else code2+=code.charAt(x);");
    scoutln(out, bytesOut, "return code2};");
    
    scoutln(out, bytesOut, "</script>");

    int[] hmenuCount = new int[1];
    
    connectionsUtils.outputPageFrame(con, stmt, rs, out, req, "ChannelMyChannels", "", "12704", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount, bytesOut);

    connectionsUtils.drawTitle(con, stmt, rs, req, out, "My Channels", "12704", unm, sid, uty, men, den, dnm, bnm, hmenuCount, localDefnsDir, defnsDir, bytesOut);
  
    scoutln(out, bytesOut, "<table id=\"page\" width=100% cellpadding=3>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr id=\"pageColumn\">");
    scoutln(out, bytesOut, "<td nowrap><p>Channel</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Type</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Status</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Location</td><td></td></tr>");

    listContacts(con, stmt, stmt2, rs, rs2, out, bytesOut);

    listGroups(con, stmt, stmt2, rs, rs2, out, bytesOut);

    listPersonnel(con, stmt, rs, out, sessionsDir, localDefnsDir, defnsDir, bytesOut);
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "</table>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void listPersonnel(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String sessionsDir, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception

  {
    File path2 = new File(sessionsDir);
    String fs2[] = new String[0];
    fs2 = path2.list();

    long timeNow = ((generalUtils.todayEncoded(localDefnsDir, defnsDir) - 1) * 86400L) + generalUtils.timeNowInSecs();

    long assumedSignedOut = generalUtils.strToLong(generalUtils.getFromDefnFile("ASSUMESIGNEDOUT", "timeouts.dfn", localDefnsDir, defnsDir));
    long idleTime         = generalUtils.strToLong(generalUtils.getFromDefnFile("IDLETIME", "timeouts.dfn", localDefnsDir, defnsDir));

    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT UserCode, UserName FROM profiles WHERE Status = 'L' ORDER BY UserName");

      String userCode, userName, msg, cssFormat = "line1";
      String[] state = new String[1];

      while(rs.next())
      {
        userCode        = rs.getString(1);
        userName        = rs.getString(2);
      
        if(! userCode.equals("___registered___") && ! userCode.equals("___casual___") && ! userCode.equals("Sysadmin"))
        {
          if(cssFormat.equals("line1")) cssFormat = "line2"; else cssFormat = "line1";

          scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\">");
          scoutln(out, bytesOut, "<td><p>" + userName + "</td>");

          scoutln(out, bytesOut, "<td><p>Personal</td>");

          if(onlineStatus(userCode, fs2, sessionsDir, timeNow, assumedSignedOut, idleTime, state))
            msg = "Open the Channel Now";
          else msg = "Leave an Offline Message";

          scoutln(out, bytesOut, "<td><p>" + state[0] + "</td>");
          scoutln(out, bytesOut, "<td><p>Local</td>");

          scoutln(out, bytesOut, "<td nowrap><p><a href=\"javascript:join('" + generalUtils.sanitise(userCode) + "','P')\">" + msg + "</a></td></tr>");
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

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
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
  private void listGroups(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<tr id=\"line1\"><td><p>Broadcast Channel for Internal Users</td><td><p>Group</td><td><p>Currently Available</td>"
                         + "<td><p>Local</td><td><p><a href=\"javascript:join('___Company___','G')\">Open</a> the Channel Now</td></tr>");

    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT Name, Type, Host FROM channelsmine ORDER BY Name");

      String name, type, host, cssFormat = "";
      String[] purpose = new String[1];
      String[] status  = new String[1];

      while(rs.next())
      {
        name = rs.getString(1);
        type = rs.getString(2);
        host = rs.getString(3);

        channelStatusAndPurpose(con, stmt2, rs2, name, purpose, status);

        if(cssFormat.equals("line2")) cssFormat = "line1"; else cssFormat = "line2";

        scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\">");
        scoutln(out, bytesOut, "<td><p>" + purpose[0] + "</td>");

        if(type.equals("P"))
          type = "Personal";
        else type = "Group";

        scoutln(out, bytesOut, "<td><p>" + type + "</td>");
        scoutln(out, bytesOut, "<td><p>" + status[0] + "</td>");
        scoutln(out, bytesOut, "<td><p>" + host + "</td>");

        scoutln(out, bytesOut, "<td nowrap><p><a href=\"javascript:join('" + generalUtils.sanitise(name) + "','G')\">Open</a> the Channel Now</td></tr>");

//        scoutln(out, bytesOut, "<td nowrap><p><a href=\"javascript:remove('" + generalUtils.sanitise(name) + "')\">Remove</a></td></tr>");
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
  private void listContacts(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, int[] bytesOut) throws Exception
  {
    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT Name, CompanyName, ZPN FROM contacts WHERE ZPN != '' ORDER BY CompanyName");

      String name, companyName, zpn, cssFormat = "";
    //  String[] status  = new String[1];

      while(rs.next())
      {
        name        = rs.getString(1);
        companyName = rs.getString(2);
        zpn         = rs.getString(3);


        if(cssFormat.equals("line2")) cssFormat = "line1"; else cssFormat = "line2";

        scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\">");
        scoutln(out, bytesOut, "<td><p>" + companyName + ": " + name + "</td>");

        scoutln(out, bytesOut, "<td><p>Personal</td>");
        scoutln(out, bytesOut, "<td><p>" + "Unknown" + "</td>"); //////////////////////////////////////
        scoutln(out, bytesOut, "<td><p>" + companyName + "</td>"); ////////////

        scoutln(out, bytesOut, "<td nowrap><p><a href=\"javascript:join('" + generalUtils.sanitise(zpn) + "','P')\">Open</a> the Channel Now</td></tr>");
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
  private void channelStatusAndPurpose(Connection con, Statement stmt, ResultSet rs, String name, String[] purpose, String[] status) throws Exception
  {
    purpose[0] = status[0] = "";
    
    try
    {
      stmt = con.createStatement();
  
      rs = stmt.executeQuery("SELECT Purpose, Status FROM channels WHERE Name = '" + name + "'");

      if(rs.next())                  
      {
        purpose[0] = rs.getString(1);
        status[0]  = rs.getString(2);
        
        if(status[0].equals("U"))
          status[0] = "Currently Available";
        else status[0] = "Currently Not Available";
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
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
