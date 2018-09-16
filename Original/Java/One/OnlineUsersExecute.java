// =======================================================================================================================================================================================================
// System: ZaraStar: Utils: get data for online users
// Module: OnlineUsersExecute.java
// Author: C.K.Harvey
// Copyright (c) 2006-08 Christopher Harvey. All Rights Reserved.
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
import java.util.Enumeration;
import java.sql.*;

public class OnlineUsersExecute extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  AdminUtils adminUtils = new AdminUtils();
  DefinitionTables definitionTables = new DefinitionTables();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="";

    try
    {
      directoryUtils.setContentHeaders(res);
      res.setHeader("Header", "HTTP/1.0 200 OK");
      res.setStatus(200);
      res.setContentType("text/xml");
      res.setHeader("Cache-Control", "no-cache");

      Enumeration en = req.getParameterNames();
      while(en.hasMoreElements())
      {
        String name = (String)en.nextElement();
        String[] value = req.getParameterValues(name);
        if(name.equals("unm"))
          unm = value[0];
        else
        if(name.equals("sid"))
          sid = value[0];
        else
        if(name.equals("uty"))
          uty = value[0];
        else
        if(name.equals("men"))
          men = value[0];
        else
        if(name.equals("den"))
          den = value[0];
        else
        if(name.equals("dnm"))
          dnm = value[0];
        else
        if(name.equals("bnm"))
          bnm = value[0];
      }

      doIt(req, res, unm, sid, uty, men, den, dnm, bnm, bytesOut);
    }    
    catch(Exception e)
    {
      System.out.println(("Unexpected System Error: 12601a: " + e));
      res.getWriter().write("Unexpected System Error: 12601a");
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(HttpServletRequest req, HttpServletResponse res, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                    int[] bytesOut) throws Exception
  {
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
 
    String rtn="Unexpected System Error: 12601a";
    
    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      rtn = "Access Denied - Duplicate SignOn or Session Timeout";
    }
    else
    {
      mergeData(res, unm, sid, uty, men, den, dnm, bnm, bytesOut);
    }
    
    serverUtils.totalBytes(req, unm, dnm, 12601, bytesOut[0], 0, "");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void mergeData(HttpServletResponse res, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut)
                         throws Exception
  {
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String sessionsDir      = directoryUtils.getSessionsDir(dnm);

    long timeNow = ((generalUtils.todayEncoded(localDefnsDir, defnsDir) - 1) * 86400L) + generalUtils.timeNowInSecs();

    long assumedSignedOut = generalUtils.strToLong(generalUtils.getFromDefnFile("ASSUMESIGNEDOUT", "timeouts.dfn", localDefnsDir, defnsDir));
    long idleTime         = generalUtils.strToLong(generalUtils.getFromDefnFile("IDLETIME", "timeouts.dfn", localDefnsDir, defnsDir));
    
    File path2 = new File(sessionsDir);
    String fs2[] = new String[0];
    fs2 = path2.list();
 
    String[] cssFormat = new String[1]; cssFormat[0] = "";
    
    String rtnStr = "";

    Connection con = null;
    Statement stmt = null, stmt2 = null;
    ResultSet rs   = null, rs2 = null;
    
    String uName = directoryUtils.getMySQLUserName();
    String pWord = directoryUtils.getMySQLPassWord();

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    
    con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + uName + "&password=" + pWord);

    
    Statement stmtZC = null;
    ResultSet rsZC = null;
    
    // internal users

    try
    {
      stmt = con.createStatement();
  
      String userCode, userName, status, showInDirectory;

      rs = stmt.executeQuery("SELECT UserCode, UserName, Status, ShowInDirectory FROM profiles ORDER BY UserName"); 
      
      while(rs.next())                  
      {
        userCode        = rs.getString(1);
        userName        = rs.getString(2);
        status          = rs.getString(3);
        showInDirectory = rs.getString(4);
        
        if(showInDirectory == null) showInDirectory = "N";

        rtnStr += check(con, stmt2, rs2, stmtZC, rsZC, "Internal", userCode, userName, fs2, status, sessionsDir, timeNow, assumedSignedOut, idleTime, unm, dnm,
                        showInDirectory, cssFormat, localDefnsDir, defnsDir);
      }
                 
      if(rs   != null) rs.close();                                 
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println("12601a: " + e);
      if(rs != null) rs.close();
      if(stmt != null) stmt.close();
    }    

    // registered users
    
    try
    {
      stmt = con.createStatement();
  
      rs = stmt.executeQuery("SELECT ExternalCode, Name, CompanyName FROM contacts WHERE ExternalCode != '' ORDER BY ExternalCode");

      String externalCode, name, companyName;
      
      while(rs.next())                  
      {
        externalCode = rs.getString(1);
        name         = rs.getString(2);
        companyName  = rs.getString(3);

        rtnStr += check(con, stmt2, rs2, stmtZC, rsZC, "Registered", externalCode, name + " (" + companyName + ")", fs2, "L", sessionsDir, timeNow,
                        assumedSignedOut, idleTime, unm, dnm, "Y", cssFormat, localDefnsDir, defnsDir);
      }
            
      if(rs != null) rs.close();
      if(stmt != null) stmt.close();        

    }
    catch(Exception e)
    {
      System.out.println("12601a: " + e);
      if(rs != null) rs.close();
      if(stmt != null) stmt.close();
    }

    if(con != null) con.close();

    String s = "<msg><res>.</res><stuff><![CDATA[" + rtnStr + "]]></stuff></msg>";

    res.getWriter().write(s);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private String check(Connection con, Statement stmt, ResultSet rs, Statement stmtZC, ResultSet rsZC, String userType, String userCode, String userName, String[] fs2, String status,
                       String sessionsDir, long timeNow, long assumedSignedOut, long idleTime, String unm, String dnm, String showInDirectory,
                       String[] cssFormat, String localDefnsDir, String defnsDir) throws Exception
  {
    String rtnStr = "";
    
    if(status.equals("L")) // live
    {
      int y, z, hrs;
      long diff;
      boolean done, offline, sidFound = false;
      String state="", hrsStr;
      String[] service = new String[1];
      String[] host    = new String[1];
      String[] text    = new String[1];
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
              state = ("Idle (" + generalUtils.longToStr(diff) + " min)");
              offline = false;
            }
            else 
            if(diff < 60) // mins
            {
              state = ("Idle (" + generalUtils.longToStr(diff) + " mins)");
              offline = false;
            }
            else 
            {
              hrs = (int)(diff / 60);
              diff = diff - (hrs * 60);
              if(hrs > 1) hrsStr = "s"; else hrsStr = ""; 
            
              state = ("Idle (" + generalUtils.longToStr(hrs) + " hour" + hrsStr + " " + generalUtils.longToStr(diff) + " mins)");
              offline = false;
            }  
          }  
          else
          {
            state = "Online"; 
            offline = false;
          }  
        }
        // else Offline
      }

      if(! offline)
      {
        if(unm.equals("Sysadmin") || showInDirectory.equals("Y") || unm.equals(userName))
        {
          if(cssFormat[0].equals("line1"))
            cssFormat[0] = "line2";
          else cssFormat[0] = "line1";

          lastEntryFromTrail(con, stmt, rs, userCode, service, host, text);

          rtnStr += "<tr id=\"" + cssFormat[0] + "\"><td><p>" + userName + "</td><td><p>" + userType + "</td><td><p>" + state + "</td><td><p>"
                 + host[0] + "</td><td><p>" + service[0] + " " + definitionTables.getDescriptionGivenService(con,        stmtZC, rsZC, service[0]) + "</td><td><p>" ////////////// was conZC
                 + text[0] + "</td></tr>";
        }
      }  
    }
    
    return rtnStr;
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void lastEntryFromTrail(Connection con, Statement stmt, ResultSet rs, String userCode, String[] service, String[] host, String[] text)
  {
    service[0] = host[0] = text[0] = "";
    
    try
    {
      stmt = con.createStatement();
      stmt.setMaxRows(1);
      rs = stmt.executeQuery("SELECT Service, Host, Text FROM lasttrail WHERE UserCode = '" + userCode + "'");
      
      if(rs.next())
      {
        service[0] = rs.getString(1);
        host[0]    = rs.getString(2);
        text[0]    = rs.getString(3);
      }
            
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println("12601a: " + e);
      try
      {
        if(rs   != null) rs.close();
        if(stmt != null) stmt.close();
      }
      catch(Exception e2) { }
    }
  }

}
