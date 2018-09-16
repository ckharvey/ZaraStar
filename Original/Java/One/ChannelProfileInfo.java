// =======================================================================================================================================================================================================
// System: ZaraStar Channels: Get profile info and return to caller
// Module: ChannelProfileInfo.java
// Author: C.K.Harvey
// Copyright (c) 1999-2009 Christopher Harvey. All Rights Reserved.
// Where: On site
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

public class ChannelProfileInfo extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  Profile profile = new Profile();
  ChannelUtils channelUtils = new ChannelUtils();
  
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", men="", dnm="", p1="";

    try
    {
      directoryUtils.setContentHeaders(res);
      out = res.getWriter();

      unm = req.getParameter("unm");
      men = req.getParameter("men");
      dnm = req.getParameter("dnm");
      p1  = req.getParameter("p1"); // zpn

      doIt(out, req, unm, men, dnm, p1, bytesOut);
    }
    catch(Exception e)
    {
        System.out.println("12708e: " + e);
      serverUtils.etotalBytes(req, unm, dnm, 9018, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush(); 
    }
  }
  
  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String men, String dnm, String p1, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir    = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');
    String imagesLibraryDir = directoryUtils.getImagesDir(dnm);
    String sessionsDir      = directoryUtils.getSessionsDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    set(con, stmt, rs, out, p1, men, imagesDir, imagesLibraryDir, sessionsDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 12708, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con != null) con.close();    
    if(out != null) out.flush(); 
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String zpn, String men, String imagesDir, String imagesLibraryDir, String sessionsDir, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<table id='page' width=100% cellpadding=2 cellspacing=0   border=1>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    getProfile(con, stmt, rs, out, zpn, men, imagesDir, imagesLibraryDir, sessionsDir, localDefnsDir, defnsDir, bytesOut);
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "</table>");
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void getProfile(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String zpn, String men, String imagesDir, String imagesLibraryDir, String sessionsDir, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    try
    {
      stmt = con.createStatement();
  
      rs = stmt.executeQuery("SELECT UserName, JobTitle, Image FROM profiles WHERE Status = 'L' AND UserCode = '" + zpn + "'");
      
      String userName, jobTitle, image;

      if(rs.next())                  
      {
        userName = rs.getString(1);
        jobTitle = rs.getString(2);
        image    = rs.getString(3);
      
        writeProfile(out, zpn, userName, jobTitle, image, men, imagesDir, imagesLibraryDir, sessionsDir, localDefnsDir, defnsDir, bytesOut);
      }
      else scoutln(out, bytesOut, "<tr><td><p>Profile Not Available</td></tr>");

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
  private void writeProfile(PrintWriter out, String userCode, String userName, String jobTitle, String image, String men, String imagesDir, String imagesLibraryDir, String sessionsDir, String localDefnsDir, String defnsDir, int[] bytesOut)
                            throws Exception
  {
    scoutln(out, bytesOut, "<tr>");
    
    if(image.length() > 0 && generalUtils.fileExists(imagesLibraryDir + image))
      scoutln(out, bytesOut, "<td rowspan=4 valign=top><img src=\"" + men + imagesLibraryDir + image + "\" style='{border:3px ridge #000000;}' /></td>");
    else
    if(userCode.equals("Sysadmin"))
      scoutln(out, bytesOut, "<td rowspan=4 valign=top><img src=\"" + men + imagesDir + "zaralogosmall.png\" /></td>");
    else scoutln(out, bytesOut, "<td rowspan=3><p><i>No Image</i></td>");

    scoutln(out, bytesOut, "<td><p><b>" + userName + "</td>");

    String[] state = new String[1];
    long timeNow = ((generalUtils.todayEncoded(localDefnsDir, defnsDir) - 1) * 86400L) + generalUtils.timeNowInSecs();

    long assumedSignedOut = generalUtils.strToLong(generalUtils.getFromDefnFile("ASSUMESIGNEDOUT", "timeouts.dfn", localDefnsDir, defnsDir));
    long idleTime         = generalUtils.strToLong(generalUtils.getFromDefnFile("IDLETIME", "timeouts.dfn", localDefnsDir, defnsDir));
    
    File path2 = new File(sessionsDir);
    String fs2[] = new String[0];
    fs2 = path2.list();

    boolean online = onlineStatus(userCode, fs2, sessionsDir, timeNow, assumedSignedOut, idleTime, state);

    scoutln(out, bytesOut, "<td><p>" + state[0]);
    scoutln(out, bytesOut, " &nbsp; &nbsp; <a href=\"javascript:channel('" + userCode + "')\">");
            
    if(online)
      scoutln(out, bytesOut, "Open a Channel Now</a>");
    else scoutln(out, bytesOut, "Leave an offline channel message</a>");
            
    scoutln(out, bytesOut, "</td></tr>");

    scoutln(out, bytesOut, "<tr><td colspan=2><p><a href=\"javascript:send('" + userCode + "')\">Send the Document Now</a></td></tr>");
            
    scoutln(out, bytesOut, "<tr><td colspan=2><p><i>" + jobTitle + "</td></tr>");
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

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
