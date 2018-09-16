// ==========================================================================================================================================================================================================
// System: ZaraStar UtilsEngine: Home Page Company personnel online
// Module: CompanyPersonnelOnline.java
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

import javax.servlet.http.*;
import java.io.*;
import java.sql.*;

public class CompanyPersonnelOnline
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir    = directoryUtils.getLocalOverrideDir(dnm);
    String sessionsDir      = directoryUtils.getSessionsDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;
    
    set(con, stmt, rs, out, sessionsDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 6661, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String sessionsDir, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    File path2 = new File(sessionsDir);
    String fs2[] = new String[0];
    fs2 = path2.list();

    long timeNow = ((generalUtils.todayEncoded(localDefnsDir, defnsDir) - 1) * 86400L) + generalUtils.timeNowInSecs();

    long assumedSignedOut = generalUtils.strToLong(generalUtils.getFromDefnFile("ASSUMESIGNEDOUT", "timeouts.dfn", localDefnsDir, defnsDir));
    long idleTime         = generalUtils.strToLong(generalUtils.getFromDefnFile("IDLETIME", "timeouts.dfn", localDefnsDir, defnsDir));

    list(con, stmt, rs, out, fs2, timeNow, assumedSignedOut, idleTime, sessionsDir, bytesOut);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void list(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String[] fs2, long timeNow, long assumedSignedOut, long idleTime, String sessionsDir, int[] bytesOut) throws Exception
  {
    try
    {
      stmt = con.createStatement();
  
      rs = stmt.executeQuery("SELECT UserCode, UserName FROM profiles WHERE Status = 'L' ORDER BY UserName");
      
      String userCode, userName, cssFormat = "";

      String[] state = new String[1];

      while(rs.next())                  
      {
        userCode = rs.getString(1);
        userName = rs.getString(2);
      
        if(! userCode.equals("___registered___") && ! userCode.equals("___casual___") && ! userCode.equals("Sysadmin"))
        {
          if(cssFormat.equals("line1")) cssFormat = "line2"; else cssFormat = "line1";

          if(onlineStatus(userCode, fs2, sessionsDir, timeNow, assumedSignedOut, idleTime, state))
            scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\"><td nowrap><p><a href=\"javascript:profile('" + userCode + "')\">" + userName + "</a> " + state[0] + "</td></tr>");
        }
      }

      if(rs   != null) rs.close();                                 
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println("6661 " + e);
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
          state[0] = ""; 
          offline = false;
        }  
      }
      else state[0] = "";
    }
    else state[0] = "";

    return ! offline;
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
