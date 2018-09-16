// =======================================================================================================================================================================================================
// System: ZaraStar: Utils: Authentication, logging, misc
// Module: serverUtils.java
// Author: C.K.Harvey
// Copyright (c) 1998-2009 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import javax.servlet.http.*;
import java.sql.*;
import java.io.*;
import java.util.*;

public class ServerUtils
{
  GeneralUtils generalUtils = new GeneralUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  Trail trail = new Trail();
  DefinitionTables definitionTables = new DefinitionTables();
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean isSysAdmin(Connection con, Statement stmt, ResultSet rs, String unm, String dnm, String sid, String uty, String localDefnsDir, String defnsDir) throws Exception
  {
    if(unm.equals("Sysadmin"))
    {
      if(! checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
        return false;

      String[] userName = new String[61];

      if(newValidateSignOn(con, stmt, rs, false, unm, "", userName)) // validated as an internal user
        return true;

      return false;
    }

    return false;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean newValidateSignOn(Connection con, Statement stmt, ResultSet rs, boolean needToCheckPassWord, String unm, String pwd, String[] userName) throws Exception
  {
    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT UserName, PassWord, Status FROM profiles WHERE UserCode = '" + generalUtils.sanitiseForSQL(unm) + "'");

      String passWord="", status="";

      if(rs.next())
      {
        userName[0] = rs.getString(1);
        passWord    = rs.getString(2);
        status      = rs.getString(3);
      }

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();

      if(status.equals("L")) // live
      {
        if(needToCheckPassWord)
        {
          if(pwd.equals(passWord))
            return true;

          return false;
        }

        return true;
      }

      return false;
    }
    catch(Exception e)
    {
      System.out.println("ServerUtils: " + e);
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }

    return false;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean isDBAdmin(Connection con, Statement stmt, ResultSet rs, String unm) throws Exception
  {
    String isDBAdmin = "N";

    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT IsDBAdmin FROM profilesd WHERE UserCode = '" + generalUtils.sanitiseForSQL(unm) + "'");

      if(rs.next()) // just-in-case
        isDBAdmin = rs.getString(1);

      if(isDBAdmin == null) isDBAdmin = "N";

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println("ServerUtils: " + e);
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }

    if(isDBAdmin.equals("Y"))
      return true;

    return false;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String targetDNM(String serverName, String dnm, String defnsDir) throws Exception
  {

    String s = generalUtils.getFromDefnFile(serverName + "DNM", "local.dfn", "", defnsDir);
    if (s.length() == 0) // just-in-case
      return dnm;

    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // the IP address of the local machine
  public String serverToCall(String serverName, String defnsDir) throws Exception
  {
    String s = generalUtils.getFromDefnFile(serverName, "local.dfn", "", defnsDir);
    int len = s.length();
    if(len == 0) // just-in-case
      return "127.0.0.1";

    String localServer = "";
    int x = 0;
    while(x < len && s.charAt(x) != ' ') // just-in-case
      localServer += s.charAt(x++);

    return localServer;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String serverToCall(HttpServletRequest req, String serverName, String defnsDir) throws Exception
  {
    String localLAN = generalUtils.getFromDefnFile("LOCALLAN", "site.dfn", "", defnsDir);
   
    String s = generalUtils.getFromDefnFile(serverName, "local.dfn", "", defnsDir);
    int len = s.length();
    if(len == 0) // just-in-case
      return "";

    String localServer = "", remoteServer = "";
    int x = 0;
    while(x < len && s.charAt(x) != ' ') // just-in-case
      localServer += s.charAt(x++);
    while(x < len && s.charAt(x) == ' ')
      ++x;
    while(x < len) // just-in-case
      remoteServer += s.charAt(x++);

    String reqURL = req.getRequestURL().toString();

    if(reqURL.startsWith("http://"))
    {
      if(reqURL.substring(7).startsWith(localLAN)) 
        return localServer;
      return remoteServer;
    }

    if(reqURL.startsWith(localLAN))
      return localServer;

    return remoteServer;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void totalBytes(HttpServletRequest req, String unm, String dnm, int service, int bytesOut, int bytesIn, String msg)
  {
    try
    {
      totalBytes(req, unm, dnm, service, bytesOut, bytesIn, 0L, msg);
    }
    catch(Exception e) { System.out.println("2 " + e); }
  }
  
  // always needed for places (usually errors) in servlets before duration timing starts
  public void etotalBytes(HttpServletRequest req, String unm, String dnm, int service, int bytesOut, int bytesIn, String msg)
  {
    try
    {
      totalBytes(req, unm, dnm, service, bytesOut, bytesIn, 0L, msg);
    }
    catch(Exception e) { System.out.println("serverUtils: " + e); }
  }

  // always needed for places (usually errors) in servlets before db opened
  public void totalBytes(HttpServletRequest req, String unm, String dnm, int service, int bytesOut, int bytesIn, long duration, String msg) throws Exception
  {
    Connection con = null;
    Statement stmt = null;
    ResultSet rs   = null;

    try
    {
      String uName = directoryUtils.getMySQLUserName();
      String pWord = directoryUtils.getMySQLPassWord();
      Class.forName("com.mysql.jdbc.Driver").newInstance();
      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + uName + "&password=" + pWord);
      totalBytes(con, stmt, rs, req, unm, dnm, service, bytesOut, bytesIn, duration, msg);
    }
    catch(Exception e)
    {
      System.out.println("serverUtils: totalBytes: " + e);
    }
    
    if(con  != null) con.close();
  }
  
  public void totalBytes(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, String unm, String dnm, int service, int bytesOut,
                         int bytesIn, long duration, String msg)
  {
    bytesIn += req.getContentLength();

    if(req.getRequestURL() != null) bytesIn += req.getRequestURL().length();

    if(req.getQueryString() != null) bytesIn += req.getQueryString().length();

    if(req.getServletPath() != null) bytesIn += req.getServletPath().length();

    if(req.getPathInfo() != null) bytesIn += req.getPathInfo().length();

    if(unm == null || unm.length() == 0) unm = "Casual";

    if(msg == null || msg.length() == 0) msg = ".";

    String today="", time="";
    try
    {
      today = today();
      time = generalUtils.timeNow(4, "");
    }
    catch(Exception e) {}

    String serviceStr = generalUtils.intToStr(service);
    String service2 = serviceStr;
    if(serviceStr.length() == 3)
      serviceStr = "  " + serviceStr;
    else
    if(serviceStr.length() == 4)
      serviceStr = " " + serviceStr;

    String bytesInStr = generalUtils.intToStr(bytesIn);
    String bytesIn2 = bytesInStr;
    if(bytesInStr.length() == 1)
      bytesInStr = "      " + bytesInStr;
    else
    if(bytesInStr.length() == 2)
      bytesInStr = "     " + bytesInStr;
    else
    if(bytesInStr.length() == 3)
      bytesInStr = "    " + bytesInStr;
    else
    if(bytesInStr.length() == 4)
      bytesInStr = "   " + bytesInStr;
    else
    if(bytesInStr.length() == 5)
      bytesInStr = "  " + bytesInStr;
    else
    if(bytesInStr.length() == 6)
      bytesInStr = " " + bytesInStr;
    
    String bytesOutStr = generalUtils.intToStr(bytesOut);
    String bytesOut2 = bytesOutStr;
    if(bytesOutStr.length() == 0)
      bytesOutStr = "       " + bytesOutStr;
    else
    if(bytesOutStr.length() == 1)
      bytesOutStr = "      " + bytesOutStr;
    else
    if(bytesOutStr.length() == 2)
      bytesOutStr = "     " + bytesOutStr;
    else
    if(bytesOutStr.length() == 3)
      bytesOutStr = "    " + bytesOutStr;
    else
    if(bytesOutStr.length() == 4)
      bytesOutStr = "   " + bytesOutStr;
    else
    if(bytesOutStr.length() == 5)
      bytesOutStr = "  " + bytesOutStr;
    else
    if(bytesOutStr.length() == 6)
      bytesOutStr = " " + bytesOutStr;

    String userCode = unm;
    if(unm.length() == 1)
      unm = "                   " + unm;
    else
    if(unm.length() == 2)
      unm = "                  " + unm;
    else
    if(unm.length() == 3)
      unm = "                 " + unm;
    else
    if(unm.length() == 4)
      unm = "                " + unm;
    else
    if(unm.length() == 5)
      unm = "               " + unm;
    else
    if(unm.length() == 6)
      unm = "              " + unm;
    else
    if(unm.length() == 7)
      unm = "             " + unm;
    else
    if(unm.length() == 8)
      unm = "            " + unm;
    else
    if(unm.length() == 9)
      unm = "           " + unm;
    else
    if(unm.length() == 10)
      unm = "          " + unm;
    else
    if(unm.length() == 11)
      unm = "         " + unm;
    else
    if(unm.length() == 12)
      unm = "        " + unm;
    else
    if(unm.length() == 13)
      unm = "       " + unm;
    else
    if(unm.length() == 14)
      unm = "      " + unm;
    else
    if(unm.length() == 15)
      unm = "     " + unm;
    else
    if(unm.length() == 16)
      unm = "    " + unm;
    else
    if(unm.length() == 17)
      unm = "   " + unm;
    else
    if(unm.length() == 18)
      unm = "  " + unm;
    else
    if(unm.length() == 19)
      unm = " " + unm;
  
    String host = req.getRemoteHost();
    String host2 = host;
    if(host.length() == 7)
      host += "        ";
    else
    if(host.length() == 8)
      host += "       ";
    else
    if(host.length() == 9)
      host += "      ";
    else
    if(host.length() == 10)
      host += "     ";
    else
    if(host.length() == 11)
      host += "    ";
    else
    if(host.length() == 12)
      host += "   ";
    else
    if(host.length() == 13)
      host += "  ";
    else
    if(host.length() == 14)
      host += " ";

    String durationStr = generalUtils.longToStr(duration);
    if(durationStr.length() == 1)
      durationStr = "     " + durationStr;
    else
    if(durationStr.length() == 2)
      durationStr = "    " + durationStr;
    else
    if(durationStr.length() == 3)
      durationStr = "   " + durationStr;
    else
    if(durationStr.length() == 4)
      durationStr = "  " + durationStr;
    else
    if(durationStr.length() == 5)
      durationStr = " " + durationStr;
    
    if(serviceStr.equals("8612") || serviceStr.equals("11200") || serviceStr.startsWith("1270") || serviceStr.equals("12601"))
      ;
    else
    if(serviceStr.equals("12600") && ! msg.startsWith("OUT:"))
      ;
    else
    {
      if(msg.length() > 80)
        msg = msg.substring(0, 80);
      
      System.out.println(unm + " " + dnm + " " + serviceStr + " o:" + bytesOutStr + " i:" + bytesInStr + " " + today + " " + time.charAt(0) + time.charAt(1) + ":" + time.charAt(2) + time.charAt(3) + ":" + time.charAt(4) + time.charAt(5) + " "
                       + host + " " + durationStr + " " + msg);
    
      trail.addToTrail(con, stmt, rs, dnm, userCode, service2, bytesOut2, bytesIn2, host2, msg);
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String today() throws Exception
  {
    int   first=0, second=0, dd, mm, yy;

    GregorianCalendar g = new GregorianCalendar();
    Integer i1 = new Integer(g.get(GregorianCalendar.DAY_OF_MONTH));
    dd = i1.intValue();

    Integer i2 = new Integer(g.get(GregorianCalendar.MONTH));
    mm = i2.intValue() + 1;

    Integer i3 = new Integer(g.get(GregorianCalendar.YEAR));
    yy = i3.intValue();

    first = dd;  second = mm;

    String str = "";
    if(first < 10)
      str = "0";

    str += first;
    str += ".";

    if(second < 10)
      str += "0";

    str += second;
    str += ".";

    String year = "" + yy;

    str += year;

    return str;
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean checkSID(String unm, String sid, String uty, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    if(uty == null)
      return false;

    if(! uty.equals("A") && generalUtils.isInteger(sid))
      return false;

    String sessionsDir = directoryUtils.getSessionsDir(dnm);

    try
    {
      File path = new File(sessionsDir);
      String fs[];
      fs = path.list();

      for(int x=0;x<fs.length;++x)
      {
        if(fs[x].equals(unm + "." + sid + ".sid") || fs[x].equals(unm + ".sid"))
        {
          // chk if is valid SID

          String newTimeStamp = generalUtils.longToStr(((generalUtils.todayEncoded(localDefnsDir, defnsDir) - 1) * 86400L) + generalUtils.timeNowInSecs());
          
          RandomAccessFile fh = generalUtils.fileOpen(sessionsDir + unm + ".chk");
          fh.write(newTimeStamp.getBytes());
          generalUtils.fileClose(fh);
          
          fh = generalUtils.fileOpen(sessionsDir + unm + ".hb");
          fh.write(newTimeStamp.getBytes());
          generalUtils.fileClose(fh);

          return true;
        }
      }
    }
    catch (Exception e)
    {
      System.out.println("serverUtils checkSID: (" + sid + " " + unm + " " + dnm + ")" + e);
    }

    return false;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void removeSID(String unm, String sessionsDir)
  {
    try
    {
      File path = new File(sessionsDir);
      String fs[];
      fs = path.list();

      for(int x=0;x<fs.length;++x)
      {
        if(fs[x].startsWith(unm + ".") && fs[x].endsWith("sid"))
          generalUtils.fileDelete(sessionsDir + fs[x]);
      }
    }
    catch (Exception e) { System.out.println("5 " + e); }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void removeAllSIDRelated(String unm, String sessionsDir)
  {
    try
    {
      File path = new File(sessionsDir);
      String fs[];
      fs = path.list();

      for(int x=0;x<fs.length;++x)
      {
        if(fs[x].startsWith(unm + ".") && fs[x].endsWith("sid"))
          generalUtils.fileDelete(sessionsDir + fs[x]);
        else
        if(fs[x].startsWith(unm + ".") && fs[x].endsWith("hb"))
          generalUtils.fileDelete(sessionsDir + fs[x]);
        else
        if(fs[x].startsWith(unm + ".") && fs[x].endsWith("chk"))
          generalUtils.fileDelete(sessionsDir + fs[x]);
      }
    }
    catch (Exception e) { System.out.println("6 " + e); }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String newSessionID(String unm, String uty, String dnm, String sessionsDir, String localDefnsDir, String defnsDir) throws Exception
  {
    long l = new java.util.Date().getTime();
    int ii = new Random(l).nextInt();
    if(ii < 0)
      ii *= -1;

    String sid;
    if(uty.equals("A"))
      sid = generalUtils.longToStr(l).substring(3) + generalUtils.intToStr(ii).substring(0, 5);
    else sid = Long.toHexString(ii * l);

    try
    {
      if(! uty.equals("A"))
      {
        generalUtils.create(sessionsDir + unm + "." + sid + ".sid");

        if(! generalUtils.fileExists(sessionsDir + unm + ".chk"))
          generalUtils.create(sessionsDir + unm + ".chk");

        if(! generalUtils.fileExists(sessionsDir + unm + ".hb"))
          generalUtils.create(sessionsDir + unm + ".hb");
 
        checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir); // init
      }
      else // anon
      {
        generalUtils.create(sessionsDir + "_" + sid + ".sid");

        if(! generalUtils.fileExists(sessionsDir + "_" + sid + ".chk"))
          generalUtils.create(sessionsDir + "_" + sid + ".chk");

        if(! generalUtils.fileExists(sessionsDir + "_" + sid + ".hb"))
          generalUtils.create(sessionsDir + "_" + sid + ".hb");

        checkSID("_" + sid, sid, uty, dnm, localDefnsDir, defnsDir); // init
      }
    }
    catch(Exception e) { System.out.println("serverUtils: a : " + e); }
            
    return sid;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void removeExtinctSIDs(String sessionsDir, String localDefnsDir, String defnsDir) throws Exception
  {
    long timeNow = ((generalUtils.todayEncoded(localDefnsDir, defnsDir) - 1) * 86400L) + generalUtils.timeNowInSecs();

    int z;
    String prefix;
    RandomAccessFile fh;
    byte[] lastHeartBeatTimeStamp = new byte[50];
    
    try
    {
      File path = new File(sessionsDir);
      String fs[];
      fs = path.list();

      for(int x=0;x<fs.length;++x)
      {
        if(fs[x].endsWith(".hb"))
        {
          if((fh = generalUtils.fileOpen(sessionsDir + fs[x])) == null)
            System.out.println("serverUtils: >>> failed to open: >" + sessionsDir + fs[x] + "<");
          else
          {
            z=0;
            try
            {
              while(true)
                lastHeartBeatTimeStamp[z++] = fh.readByte();                    
            }
            catch(Exception e) { }
            lastHeartBeatTimeStamp[z] = '\000';
            generalUtils.fileClose(fh);
            
            if(timeNow > (generalUtils.longFromBytesCharFormat(lastHeartBeatTimeStamp, (short)0) + 3600L))
            {
              prefix = fs[x].substring(0, fs[x].length() - 3);

              generalUtils.fileDelete(sessionsDir + fs[x]);
              generalUtils.fileDelete(sessionsDir + prefix + ".sid");
              generalUtils.fileDelete(sessionsDir + prefix + ".chk");
              if(prefix.length() > 0) // avoid deleting dirs if processing the ".hb" (erroneous) entry
                generalUtils.directoryHierarchyDelete(sessionsDir + prefix);
            }
          }
        }
      }
    }
    catch (Exception e) { System.out.println("out removeExtinctSIDs: " + e); }
  }


  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean passLockCheck(Connection con, Statement stmt, ResultSet rs, String docType, String docDate, String unm)
  {
    boolean res = false;
    
    if(unm.equals("Sysadmin")) return true;
    
    try
    {
      stmt = con.createStatement();
  
      rs = stmt.executeQuery("SELECT LockedUpto, OpenTo FROM locks WHERE DocumentAbbrev = '" + docType + "'");
      
      String lockedUpto, openTo;
      
      if(rs.next())
      {
        lockedUpto = rs.getString(1);
        openTo     = rs.getString(2);
        
        if(lockedUpto.equals("1970-01-01"))
          res = true;
        else
        if(generalUtils.encodeFromYYYYMMDD(docDate) > generalUtils.encodeFromYYYYMMDD(lockedUpto))
          res = true;
        else
        {
          String thisUNM;
          boolean found = false;
          int x = 0, len = openTo.length();
          while(x < len && ! found)
          {
            thisUNM = "";
            while(x < len && openTo.charAt(x) != ' ' && openTo.charAt(x) != ',' && openTo.charAt(x) != ';' && openTo.charAt(x) != ':')
              thisUNM += openTo.charAt(x++);
            if(thisUNM.equalsIgnoreCase(unm))
            {
              res   = true;
              found = true;
            }
            else
            {
              while(x < len && (openTo.charAt(x) == ' ' || openTo.charAt(x) == ',' || openTo.charAt(x) == ';' || openTo.charAt(x) == ':'))
                ++x;
            }
          }            
        }
      }
      
      if(rs   != null) rs.close();                                 
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println("serverUtils lockChk " + e);
      try
      {
        if(rs   != null) rs.close();                                 
        if(stmt != null) stmt.close();
      }
      catch(Exception e2) { }
    }
      
    return res;
  }  

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void syncToIW(Connection con, String docCode, String quoteCode, String soCode, char docType, String docDate, String userCode, String customerCode, String dnm) throws Exception
  {
    if(! doIWSyncing()) return;

    try
    {
      Statement stmt = null, stmt2 = null;
      ResultSet rs = null, rs2 = null;
      String waveID;

      if(docType =='Q')
        createNew(con, stmt, rs, userCode, "Q", docCode, customerCode, docDate);
      else
      if(docType == 'S')
      {
        if(quoteCode.length() > 0)
        {
          waveID = getWaveIDForDoc(con, stmt, rs, quoteCode, 'Q');
          addToWave(con, stmt, rs, waveID, userCode, "S", docCode);
        }
        else createNew(con, stmt, rs, userCode, "S", docCode, customerCode, docDate);
      }
      else
      if(docType == 'D')
      {
        if(soCode.length() > 0) // soCode var used to pass-in plCode
        {
          soCode = getSOCodeGivenPLCode(con, stmt, rs, soCode);
          if(soCode.length() > 0)
          {
            waveID = getWaveIDForDoc(con, stmt, rs, soCode, 'S');
            addToWave(con, stmt, rs, waveID, userCode, "D", docCode);
          }
        }

        forEachSOOnDO(con, stmt, stmt2, rs, rs2, docCode, userCode);
      }
    }
    catch(Exception e)
    {
      System.out.println("ServerUtils syncToIW(): " + e);
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String getWaveIDForDoc(Connection con, Statement stmt, ResultSet rs, String docCode, char docType) throws Exception
  {
    String waveID = "";

    try
    {
      stmt = con.createStatement();

      stmt.setMaxRows(1);

      rs = stmt.executeQuery("SELECT WaveID FROM waveletc WHERE DocCode = '" + docCode + "' AND DocType = '" + docType + "'");

      if(rs.next())
        waveID = rs.getString(1);

      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println("ServerUtils: " + e);
      if(stmt != null) stmt.close();
      return "";
    }

    return waveID;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean addToWave(Connection con, Statement stmt, ResultSet rs, String waveID, String userCode, String docType, String docCode) throws Exception
  {
    if(waveID.length() == 0) // just-in-case
      return false;

    String posn = getNextPositionInWavelet(con, stmt, rs, waveID);

    String waveletID = createNewWavelet(con, stmt, rs, waveID, userCode, docCode, docType, posn);
  
    if(waveletID.length() == 0)
      return false;

    return true;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean createNew(Connection con, Statement stmt, ResultSet rs, String userCode, String docType, String docCode, String custCode, String waveDated) throws Exception
  {
    String waveID = createNewWave(con, stmt, rs, userCode, waveDated);
    if(waveID.length() == 0)
      return false;

    String waveletID = createNewWavelet(con, stmt, rs, waveID, userCode, docCode, docType, "10");

    if(waveletID.length() == 0)
    {
      deleteWave(con, stmt, waveID);
      return false;
    }

    createNewWavechannelsRec(con, stmt, rs, waveID, custCode, waveDated, userCode, "xxx");
    
    return true;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String getNextPositionInWavelet(Connection con, Statement stmt, ResultSet rs, String waveID) throws Exception
  {
    String posn = "0";

    try
    {
      stmt = con.createStatement();

      stmt.setMaxRows(1);

      rs = stmt.executeQuery("SELECT Position FROM waveletc WHERE WaveID = '" + waveID + "' ORDER BY Position DESC");

      if(rs.next())
        posn = rs.getString(1);

      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println("ServerUtils: " + e);
      if(stmt != null) stmt.close();
      return "";
    }

    return "" + (generalUtils.intFromStr(posn) + 10);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String createNewWave(Connection con, Statement stmt, ResultSet rs, String userCode, String dated) throws Exception
  {
    String waveID = "";

    try
    {
      stmt = con.createStatement();

      stmt.setMaxRows(1);

      rs = stmt.executeQuery("SELECT WaveID FROM wavec ORDER BY WaveID DESC");

      if(rs.next())
      {
        waveID = rs.getString(1);
        waveID = "" + (generalUtils.strToInt(waveID) + 1);
      }
      else waveID = "1";

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
      System.out.println("ServerUtils: " + e);
      return "";
    }

    try
    {
      stmt = con.createStatement();

      stmt.executeUpdate("INSERT INTO wavec (WaveID, Owner, Closed, Status, IsLocked, IsAboutWave, dated, IsInfo, IsAdvert, DNMOwner, IsEditorial, PaperID, IsOffer) VALUES ('" + waveID + "','" + userCode + "','F','L','F','F','"+dated+"','F','F','xxx','F','','F')");
      
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println("ServerUtils: " + e);
      if(stmt != null) stmt.close();
      return "";
    }

    return waveID;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void deleteWaveletRecord(Connection con, Statement stmt, String waveletID) throws Exception
  {
    try
    {
      stmt = con.createStatement();

      stmt.executeUpdate("DELETE FROM waveletc WHERE WaveletID = '" + waveletID + "'");

      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println("ServerUtils: " + e);
      if(stmt != null) stmt.close();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String createNewWavelet(Connection con, Statement stmt, ResultSet rs, String waveID, String userCode, String docCode, String docType, String posn) throws Exception
  {
    String waveletID = "";

    try
    {
      stmt = con.createStatement();

      stmt.setMaxRows(1);

      rs = stmt.executeQuery("SELECT WaveletID FROM waveletc ORDER BY WaveletID DESC");

      if(rs.next())
      {
        waveletID = rs.getString(1);
        waveletID = "" + (generalUtils.strToInt(waveletID) + 1);
      }
      else waveletID = "1";

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
      System.out.println("ServerUtils: " + e);
      return "";
    }

    try
    {
      String text = "";
      if(docType.equals("Q"))
        text = "Quotation: " + docCode;
      else
      if(docType.equals("S"))
        text = "Sales Order: " + docCode;
      else
      if(docType.equals("D"))
        text = "Delivery Order: " + docCode;

      String path = "";
      String youTubeID = "";
      String closureAchieved = "";
      String type = "B";
      
      String internalOnly = "N";
      if(docType.equals("P"))
        internalOnly = "Y";
      
      stmt = con.createStatement();

      stmt.executeUpdate("INSERT INTO waveletc (WaveletID, Position, WaveID, Owner, Type, Status, Text, Path, YouTubeID, DocCode, DocType, ClosureAchieved, InternalOnly) VALUES ('" + waveletID + "','" + posn + "','" + waveID + "','" + userCode
                       + "','" + type + "','L','" + text + "','" + path + "','" + youTubeID + "','" + docCode + "','" + docType + "','" + closureAchieved + "','" + internalOnly + "')");

      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println("ServerUtils: " + e);
      if(stmt != null) stmt.close();
    }

    return waveletID;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void deleteWave(Connection con, Statement stmt, String waveID) throws Exception
  {
    try
    {
      stmt = con.createStatement();

      stmt.executeUpdate("DELETE FROM wavec WHERE WaveID = '" + waveID + "'");

      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println("ServerUtils: " + e);
      if(stmt != null) stmt.close();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean createNewBizDoc(Connection con, Statement stmt, String waveletID, String docCode, char docType, String date) throws Exception
  {
    try
    {
      stmt = con.createStatement();

      stmt.executeUpdate("INSERT INTO bizdoc (WaveletID, DocCode, DocType, CreationTimeStamp, ClosureAchieved) VALUES ('" + waveletID + "','" + generalUtils.sanitiseForSQL(docCode) + "','" +docType + "','"+date+"','N')");

      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println("ServerUtils: " + e);
      if(stmt != null) stmt.close();
      return false;
    }

    return true;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean doIWSyncing() throws Exception
  {
    String sync = generalUtils.getFromDefnFile("SYNC", "sync.dfn", "/iotaWave/", "");
    if(sync.equals("Y") || sync.equals("y"))
      return true;
    return false;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void syncToIWCreateBizDocOp(Connection con, Statement stmt, String docCode, char docType, String userCode, String content, String host, String bytesIn, String bytesOut, String service) throws Exception
  {
    if(! doIWSyncing()) return;

    try
    {
      String[] docType2  = new String[1];
      String[] operation = new String[1];

      getDocTypeForService(service, docType2, operation);

      createNewBizDocOpRecord(con, stmt, docCode, docType, userCode, "", operation[0], content, host, bytesIn, bytesOut, service);
    }
    catch(Exception e)
    {
      System.out.println("ServerUtils: " + e);
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean createNewBizDocOpRecord(Connection con, Statement stmt, String docCode, char docType, String userCode, String fieldName, String operation, String content, String host, String bytesIn, String bytesOut, String service)
                                          throws Exception
  {
     try
    {
      stmt = con.createStatement();

      stmt.executeUpdate("INSERT INTO bizdocops (DocumentCode, DocumentType, UserCode, FieldName, Operation, Content, Host, BytesIn, BytesOut, Service) VALUES ('" + docCode + "','" + docType + "','" + userCode + "','" + fieldName + "','"
                        + operation + "','" + content + "','" + host + "','" + bytesIn + "','" + bytesOut + "','" + service + "')");

      if(stmt != null) stmt.close();
    }
    catch(Exception e) // dups can occur
    {
      if(stmt != null) stmt.close();
    }
    return true;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void getDocTypeForService(String service, String[] docType, String[] op)
  {
    docType[0] = "";

    // Records - Customer
    if(service.equals("103"))  { docType[0] = "1"; op[0] = "PGE"; }  else // Screen: Customer Services
    if(service.equals("141"))  { docType[0] = "1"; op[0] = "PGE"; }  else // Screen: Customer Focus

    if(service.equals("4001")) { docType[0] = "1"; op[0] = "FTC"; }  else // Record: Fetch Customer Record
    if(service.equals("4003")) { docType[0] = "1"; op[0] = "CRT"; }  else // Record: Create Customer Record
    if(service.equals("4226")) { docType[0] = "1"; op[0] = "CHN"; }  else // Record: Change Customer Record
    if(service.equals("4227")) { docType[0] = "1"; op[0] = "UPD"; }  else // Record: Update Customer Record

    // Query - Customer
    if(service.equals("4004")) { docType[0] = "1"; op[0] = "LST"; }  else // Record: List Customer Records
    if(service.equals("4005")) { docType[0] = "1"; op[0] = "RPT"; }  else // Record: Print Customer Listing
    if(service.equals("4225")) { docType[0] = "1"; op[0] = "PGE"; }  else // Record: Replace Customer Code
    if(service.equals("4230")) { docType[0] = "1"; op[0] = "PGE"; }  else // Record: Select Customer Code
    if(service.equals("1004")) { docType[0] = "1"; op[0] = "RPT"; }  else // Report: Debtors Ageing
    if(service.equals("1029")) { docType[0] = "1"; op[0] = "ENQ"; }  else // Query: Consolidated Debtors
    if(service.equals("1012")) { docType[0] = "1"; op[0] = "RPT"; }  else // Report: Statements of Account

    // Supplier
    if(service.equals("104"))  { docType[0] = "2"; op[0] = "PGE"; }  else // Screen: Supplier Services
    if(service.equals("138"))  { docType[0] = "2"; op[0] = "PGE"; }  else // Screen: Supplier Focus
    if(service.equals("5001")) { docType[0] = "2"; op[0] = "FTC"; }  else // Record: Fetch Supplier Record
    if(service.equals("5003")) { docType[0] = "2"; op[0] = "CRT"; }  else // Record: Create Supplier Record
    if(service.equals("5070")) { docType[0] = "2"; op[0] = "CHN"; }  else // Record: Change Supplier Record
    if(service.equals("5071")) { docType[0] = "2"; op[0] = "UPD"; }  else // Record: Update Supplier Record

    // Query - Supplier
    if(service.equals("5004")) { docType[0] = "2"; op[0] = "LST"; }  else // Record: List Supplier Records
    if(service.equals("5068")) { docType[0] = "2"; op[0] = "PGE"; }  else // Record: Replace Supplier Code
    if(service.equals("5069")) { docType[0] = "2"; op[0] = "PGE"; }  else // Record: Select Supplier Code
    if(service.equals("1013")) { docType[0] = "2"; op[0] = "RPT"; }  else // Report: Creditors Ageing
    if(service.equals("1031")) { docType[0] = "2"; op[0] = "ENQ"; }  else // Query: Consolidated Creditors

    // Document - Quote
    if(service.equals("142"))  { docType[0] = "Q"; op[0] = "PGE"; }  else // Screen: Quotation Focus
    if(service.equals("4019")) { docType[0] = "Q"; op[0] = "FTC"; }  else // Document: Fetch Quotation
    if(service.equals("4020")) { docType[0] = "Q"; op[0] = "CRT"; }  else // Document: Create New Quotation
    if(service.equals("4021")) { docType[0] = "Q"; op[0] = "CNV"; }  else // Document: Create Quotation from Delivery Order
    if(service.equals("4022")) { docType[0] = "Q"; op[0] = "CNV"; }  else // Document: Create Quotation from Quotation
    if(service.equals("3140")) { docType[0] = "Q"; op[0] = "CNV"; }  else // Document: Create Quotation from Inbox Record
    if(service.equals("4065")) { docType[0] = "Q"; op[0] = "CNV"; }  else // Document: Create Quotation from Sales Order
    if(service.equals("4023")) { docType[0] = "Q"; op[0] = "CHN"; }  else // Document: Change Quotation Header
    if(service.equals("4024")) { docType[0] = "Q"; op[0] = "UPD"; }  else // Document: Update Quotation Header
    if(service.equals("4025")) { docType[0] = "Q"; op[0] = "AMN"; }  else // Document: Change Quotation Line
    if(service.equals("4026")) { docType[0] = "Q"; op[0] = "UPT"; }  else // Document: Update Quotation Line
    if(service.equals("4027")) { docType[0] = "Q"; op[0] = "DLL"; }  else // Document: Delete Quotation Line
    if(service.equals("4028")) { docType[0] = "Q"; op[0] = "PRN"; }  else // Document: Print Quotation
    if(service.equals("4180")) { docType[0] = "Q"; op[0] = "FAX"; }  else // Document: Fax Quotation
    if(service.equals("2200")) { docType[0] = "Q"; op[0] = "CNV"; }  else // Document: Save Cart to Quotation

    // Query - Quote
    if(service.equals("4030")) { docType[0] = "Q"; op[0] = "LST"; }  else // Document: List Quotations
    if(service.equals("2024")) { docType[0] = "Q"; op[0] = "ENQ"; }  else // Query: Quotation Track and Trace
    if(service.equals("2029")) { docType[0] = "Q"; op[0] = "PGE"; }  else // Control: Quotation Processing Sales

    // Document - SO
    if(service.equals("140"))  { docType[0] = "S"; op[0] = "PGE"; }  else // Screen: Sales Order Focus
    if(service.equals("4031")) { docType[0] = "S"; op[0] = "FTC"; }  else // Document: Fetch Sales Order
    if(service.equals("4032")) { docType[0] = "S"; op[0] = "CRT"; }  else // Document: Create New Sales Order
    if(service.equals("4033")) { docType[0] = "S"; op[0] = "CNV"; }  else // Document: Create Sales Order from Quotation
    if(service.equals("4034")) { docType[0] = "S"; op[0] = "CNV"; }  else // Document: Create Sales Order from Sales Order
    if(service.equals("3141")) { docType[0] = "S"; op[0] = "CNV"; }  else // Document: Create Sales Order from Inbox Record
    if(service.equals("4035")) { docType[0] = "S"; op[0] = "CHN"; }  else // Document: Change Sales Order Header
    if(service.equals("4036")) { docType[0] = "S"; op[0] = "UPD"; }  else // Document: Update Sales Order Header
    if(service.equals("4037")) { docType[0] = "S"; op[0] = "AMN"; }  else // Document: Change Sales Order Line
    if(service.equals("4038")) { docType[0] = "S"; op[0] = "UPT"; }  else // Document: Update Sales Order Line
    if(service.equals("4039")) { docType[0] = "S"; op[0] = "DLL"; }  else // Document: Delete Sales Order Line
    if(service.equals("4040")) { docType[0] = "S"; op[0] = "PRN"; }  else // Document: Print Sales Order
    if(service.equals("4181")) { docType[0] = "S"; op[0] = "FAX"; }  else // Document: Fax Sales Order

    // Query - SO
    if(service.equals("4042")) { docType[0] = "S"; op[0] = "LST"; }  else // Document: List Sales Orders
    if(service.equals("2021")) { docType[0] = "S"; op[0] = "ENQ"; }  else // Sales Order Track and Trace
    if(service.equals("7805")) { docType[0] = "S"; op[0] = "ENQ"; }  else // Query: Sales Order Lines Without Delivery Date
    if(service.equals("1100")) { docType[0] = "S"; op[0] = "CHR"; }  else // Chart: Sales Orders Past Due

    // Document - OC
    if(service.equals("146"))  { docType[0] = "O"; op[0] = "PGE"; }  else // Screen: Order Confirmation Focus
    if(service.equals("4043")) { docType[0] = "O"; op[0] = "FTC"; }  else // Document: Fetch Order Confirmation
    if(service.equals("4044")) { docType[0] = "O"; op[0] = "CRT"; }  else // Document: Create New Order Confirmation
    if(service.equals("4045")) { docType[0] = "O"; op[0] = "CNV"; }  else // Document: Create Order Confirmation from Sales Order
    if(service.equals("4046")) { docType[0] = "O"; op[0] = "CHN"; }  else // Document: Change Order Confirmation Header
    if(service.equals("4047")) { docType[0] = "O"; op[0] = "UPD"; }  else // Document: Update Order Confirmation Header
    if(service.equals("4048")) { docType[0] = "O"; op[0] = "AMN"; }  else // Document: Change Order Confirmation Line
    if(service.equals("4049")) { docType[0] = "O"; op[0] = "UPT"; }  else // Document: Update Order Confirmation Line
    if(service.equals("4050")) { docType[0] = "O"; op[0] = "DLL"; }  else // Document: Delete Order Confirmation Line
    if(service.equals("4051")) { docType[0] = "O"; op[0] = "PRN"; }  else // Document: Print Order Confirmation
    if(service.equals("4182")) { docType[0] = "O"; op[0] = "FAX"; }  else // Document: Fax Order Confirmation

    // Query - OC
    if(service.equals("4053")) { docType[0] = "O"; op[0] = "LST"; }  else // Document: List Order Confirmations

    // Document - OA
    if(service.equals("170"))  { docType[0] = "B"; op[0] = "PGE"; }  else // Screen: Order Acknowledgement Focus
    if(service.equals("4130")) { docType[0] = "B"; op[0] = "FTC"; }  else // Document: Fetch Order Acknowledgement
    if(service.equals("4131")) { docType[0] = "B"; op[0] = "CRT"; }  else // Document: Create New Order Acknowledgement
    if(service.equals("4132")) { docType[0] = "B"; op[0] = "CNV"; }  else // Document: Create Order Acknowledgement from Sales Order
    if(service.equals("4133")) { docType[0] = "B"; op[0] = "CHN"; }  else // Document: Change Order Acknowledgement Header
    if(service.equals("4134")) { docType[0] = "B"; op[0] = "UPD"; }  else // Document: Update Order Acknowledgement Header
    if(service.equals("4135")) { docType[0] = "B"; op[0] = "AMN"; }  else // Document: Change Order Acknowledgement Line
    if(service.equals("4136")) { docType[0] = "B"; op[0] = "UPT"; }  else // Document: Update Order Acknowledgement Line
    if(service.equals("4137")) { docType[0] = "B"; op[0] = "DLL"; }  else // Document: Delete Order Acknowledgement Line
    if(service.equals("4138")) { docType[0] = "B"; op[0] = "PRN"; }  else // Document: Print Order Acknowledgement
    if(service.equals("4140")) { docType[0] = "B"; op[0] = "FAX"; }  else // Document: Fax Order Acknowledgement
    if(service.equals("4141")) { docType[0] = "B"; op[0] = "PGE"; }  else // Document: Mail Order Acknowledgement

    // Query - OA
    if(service.equals("4139")) { docType[0] = "B"; op[0] = "LST"; }  else // Document: List Order Acknowledgements

    // Document - PL
    if(service.equals("147"))  { docType[0] = "P"; op[0] = "PGE"; }  else // Screen: Picking List Focus
    if(service.equals("3038")) { docType[0] = "P"; op[0] = "FTC"; }  else // Document: Fetch Picking List
    if(service.equals("3039")) { docType[0] = "P"; op[0] = "CRT"; }  else // Document: Create New Picking List
    if(service.equals("3040")) { docType[0] = "P"; op[0] = "CNV"; }  else // Document: Create Picking List from DO
    if(service.equals("3041")) { docType[0] = "P"; op[0] = "CNV"; }  else // Document: Create Picking List from Invoice
    if(service.equals("3042")) { docType[0] = "P"; op[0] = "CNV"; }  else // Document: Create Picking List from SO
    if(service.equals("3043")) { docType[0] = "P"; op[0] = "CHN"; }  else // Document: Change Picking List Header
    if(service.equals("3044")) { docType[0] = "P"; op[0] = "UPD"; }  else // Document: Update Picking List Header
    if(service.equals("3045")) { docType[0] = "P"; op[0] = "AMN"; }  else // Document: Change Picking List Line
    if(service.equals("3046")) { docType[0] = "P"; op[0] = "UPT"; }  else // Document: Update Picking List Line
    if(service.equals("3047")) { docType[0] = "P"; op[0] = "DLL"; }  else // Document: Delete Picking List Line
    if(service.equals("3048")) { docType[0] = "P"; op[0] = "PRN"; }  else // Document: Print Picking List
    if(service.equals("4183")) { docType[0] = "P"; op[0] = "FAX"; }  else // Document: Fax Picking List
    if(service.equals("3054")) { docType[0] = "P"; op[0] = "UPT"; }  else // Document: Update Quantity Packed
    if(service.equals("3056")) { docType[0] = "P"; op[0] = "UPD"; }  else // Document: Update Set Completed

    // Query - PL
    if(service.equals("3050")) { docType[0] = "P"; op[0] = "LST"; }  else // Document: List Picking Lists
    if(service.equals("2025")) { docType[0] = "P"; op[0] = "ENQ"; }  else // Query: Picking List Track and Trace

    // Definitions - PL
    if(service.equals("3060")) { docType[0] = "P"; op[0] = "PGE"; }  else // Document: Edit Picking List Once Completed

    // Document - DO
    if(service.equals("148"))  { docType[0] = "D"; op[0] = "PGE"; }  else // Screen: Delivery Order Focus
    if(service.equals("4054")) { docType[0] = "D"; op[0] = "FTC"; }  else // Document: Fetch Delivery Order
    if(service.equals("4055")) { docType[0] = "D"; op[0] = "CRT"; }  else // Document: Create New Delivery Order
    if(service.equals("4056")) { docType[0] = "D"; op[0] = "CNV"; }  else // Document: Create Delivery Order from Quotation
    if(service.equals("4057")) { docType[0] = "D"; op[0] = "CNV"; }  else // Document: Create Delivery Order from Picking List
    if(service.equals("4058")) { docType[0] = "D"; op[0] = "CNV"; }  else // Document: Create Delivery Order from Sales Order
    if(service.equals("4059")) { docType[0] = "D"; op[0] = "CHN"; }  else // Document: Change Delivery Order Header
    if(service.equals("4060")) { docType[0] = "D"; op[0] = "UPD"; }  else // Document: Update Delivery Order Header
    if(service.equals("4061")) { docType[0] = "D"; op[0] = "AMN"; }  else // Document: Change Delivery Order Line
    if(service.equals("4062")) { docType[0] = "D"; op[0] = "UPT"; }  else // Document: Update Delivery Order Line
    if(service.equals("4063")) { docType[0] = "D"; op[0] = "DLL"; }  else // Document: Delete Delivery Order Line
    if(service.equals("4064")) { docType[0] = "D"; op[0] = "PRN"; }  else // Document: Print Delivery Order
    if(service.equals("4184")) { docType[0] = "D"; op[0] = "FAX"; }  else // Document: Fax Delivery Order

    // Query - DO
    if(service.equals("4066")) { docType[0] = "D"; op[0] = "LST"; }  else // Document: List Delivery Orders
    if(service.equals("2026")) { docType[0] = "D"; op[0] = "ENQ"; }  else // Query: Delivery Order Track and Trace

    // Document - SI
    if(service.equals("149"))  { docType[0] = "I"; op[0] = "PGE"; }  else // Screen: Sales Invoice Focus
    if(service.equals("4067")) { docType[0] = "I"; op[0] = "FTC"; }  else // Document: Fetch Sales Invoice
    if(service.equals("4224")) { docType[0] = "I"; op[0] = "CRT"; }  else // Document: Create New Sales Invoice
    if(service.equals("4068")) { docType[0] = "I"; op[0] = "CNV"; }  else // Document: Create Cash Sales Invoice from Picking List
    if(service.equals("4069")) { docType[0] = "I"; op[0] = "CNV"; }  else // Document: Create Sales Invoice from Picking List
    if(service.equals("4070")) { docType[0] = "I"; op[0] = "CNV"; }  else // Document: Create Sales Invoice from Delivery Order
    if(service.equals("4071")) { docType[0] = "I"; op[0] = "CNV"; }  else // Document: Create Sales Invoice from Quotation
    if(service.equals("4072")) { docType[0] = "I"; op[0] = "CHN"; }  else // Document: Change Sales Invoice Header
    if(service.equals("4073")) { docType[0] = "I"; op[0] = "UPD"; }  else // Document: Update Sales Invoice Header
    if(service.equals("4074")) { docType[0] = "I"; op[0] = "AMN"; }  else // Document: Change Sales Invoice Line
    if(service.equals("4075")) { docType[0] = "I"; op[0] = "UPT"; }  else // Document: Update Sales Invoice Line
    if(service.equals("4076")) { docType[0] = "I"; op[0] = "DLL"; }  else // Document: Delete Sales Invoice Line
    if(service.equals("4077")) { docType[0] = "I"; op[0] = "PRN"; }  else // Document: Print Sales Invoice
    if(service.equals("4185")) { docType[0] = "I"; op[0] = "FAX"; }  else //  Document: Fax Sales Invoice

    // Query - SI
    if(service.equals("4079")) { docType[0] = "I"; op[0] = "LST"; }  else // Document: List Sales Invoices
    if(service.equals("4313")) { docType[0] = "I"; op[0] = "ENQ"; }  else // Report: Sales Invoices Monthly Listing
    if(service.equals("2028")) { docType[0] = "I"; op[0] = "ENQ"; }  else // Query: Sales Invoice Track and Trace
    if(service.equals("1023")) { docType[0] = "I"; op[0] = "ENQ"; }  else // Query: Sales Invoice Listing Enquiry by SalesPerson
    if(service.equals("1033")) { docType[0] = "I"; op[0] = "ENQ"; }  else // Query: Sales Invoice Listing Enquiry by Date
    if(service.equals("1038")) { docType[0] = "I"; op[0] = "ENQ"; }  else // Query: Sales Invoice Lines Enquiry for all Manufacturers
    if(service.equals("1027")) { docType[0] = "I"; op[0] = "ENQ"; }  else // Query: Sales Invoice Enquiry for GST

    // Document - Proforma
    if(service.equals("150"))  { docType[0] = "R"; op[0] = "PGE"; }  else // Screen: Proforma Invoice Focus
    if(service.equals("4080")) { docType[0] = "R"; op[0] = "FTC"; }  else // Document: Fetch Proforma Invoice
    if(service.equals("4081")) { docType[0] = "R"; op[0] = "CRT"; }  else // Document: Create New Proforma Invoice
    if(service.equals("4082")) { docType[0] = "R"; op[0] = "CNV"; }  else // Document: Create Proforma Invoice from Sales Order
    if(service.equals("4083")) { docType[0] = "R"; op[0] = "CNV"; }  else // Document: Create Proforma Invoice from Sales Invoice
    if(service.equals("4084")) { docType[0] = "R"; op[0] = "CHN"; }  else // Document: Change Proforma Invoice Header
    if(service.equals("4085")) { docType[0] = "R"; op[0] = "UPD"; }  else // Document: Update Proforma Invoice Header
    if(service.equals("4086")) { docType[0] = "R"; op[0] = "AMN"; }  else // Document: Change Proforma Invoice Line
    if(service.equals("4087")) { docType[0] = "R"; op[0] = "UPT"; }  else // Document: Update Proforma Invoice Line
    if(service.equals("4088")) { docType[0] = "R"; op[0] = "DLL"; }  else // Document: Delete Proforma Invoice Line
    if(service.equals("4089")) { docType[0] = "R"; op[0] = "PRN"; }  else // Document: Print Proforma Invoice
    if(service.equals("4186")) { docType[0] = "R"; op[0] = "FAX"; }  else // Document: Fax Proforma Invoice

    // Query - Proforma
    if(service.equals("4091")) { docType[0] = "R"; op[0] = "LST"; }  else // Document: List Proforma Invoices

    // Document - SCN
    if(service.equals("155"))  { docType[0] = "C"; op[0] = "PGE"; }  else // Screen: Sales Credit Note Focus
    if(service.equals("4101")) { docType[0] = "C"; op[0] = "FTC"; }  else // Document: Fetch Sales Credit Note
    if(service.equals("4102")) { docType[0] = "C"; op[0] = "CRT"; }  else // Document: Create New Sales Credit Note
    if(service.equals("4103")) { docType[0] = "C"; op[0] = "CHN"; }  else // Document: Change Sales Credit Note Header
    if(service.equals("4104")) { docType[0] = "C"; op[0] = "UPD"; }  else // Document: Update Sales Credit Note Header
    if(service.equals("4105")) { docType[0] = "C"; op[0] = "AMN"; }  else // Document: Change Sales Credit Note Line
    if(service.equals("4106")) { docType[0] = "C"; op[0] = "UPT"; }  else // Document: Update Sales Credit Note Line
    if(service.equals("4107")) { docType[0] = "C"; op[0] = "DLL"; }  else // Document: Delete Sales Credit Note Line
    if(service.equals("4108")) { docType[0] = "C"; op[0] = "PRN"; }  else // Document: Print Sales Credit Note
    if(service.equals("4187")) { docType[0] = "C"; op[0] = "FAX"; }  else // Document: Fax Sales Credit Note

    // Query - SCN
    if(service.equals("4110")) { docType[0] = "C"; op[0] = "LST"; }  else // Document: List Sales Credit Notes
    if(service.equals("4311")) { docType[0] = "C"; op[0] = "ENQ"; }  else // Report: Sales Credit Notes Monthly Listing

    // Document - SDN
    if(service.equals("156"))  { docType[0] = "N"; op[0] = "PGE"; }  else // Screen: Sales Debit Note Focus
    if(service.equals("4111")) { docType[0] = "N"; op[0] = "FTC"; }  else // Document: Fetch Sales Debit Note
    if(service.equals("4112")) { docType[0] = "N"; op[0] = "CRT"; }  else // Document: Create New Sales Debit Note
    if(service.equals("4113")) { docType[0] = "N"; op[0] = "CHN"; }  else // Document: Change Sales Debit Note Header
    if(service.equals("4114")) { docType[0] = "N"; op[0] = "UPD"; }  else // Document: Update Sales Debit Note Header
    if(service.equals("4115")) { docType[0] = "N"; op[0] = "AMN"; }  else // Document: Change Sales Debit Note Line
    if(service.equals("4116")) { docType[0] = "N"; op[0] = "UPT"; }  else // Document: Update Sales Debit Note Line
    if(service.equals("4117")) { docType[0] = "N"; op[0] = "DLL"; }  else // Document: Delete Sales Debit Note Line
    if(service.equals("4118")) { docType[0] = "N"; op[0] = "PRN"; }  else // Document: Print Sales Debit Note
    if(service.equals("4188")) { docType[0] = "N"; op[0] = "FAX"; }  else // Document: Fax Sales Debit Note

    // Query - SDN
    if(service.equals("4120")) { docType[0] = "N"; op[0] = "LST"; }  else // Document: List Sales Debit Notes
    if(service.equals("4316")) { docType[0] = "N"; op[0] = "ENQ"; }  else // Report: Sales Debit Notes Monthly Listing

    // Sales Transactions
    if(service.equals("183"))  { docType[0] = "S"; op[0] = "PGE"; }  else // Screen: Sales Control
    if(service.equals("2027")) { docType[0] = "S"; op[0] = "ENQ"; }  else // Sales Order Processing Sales
    if(service.equals("2039")) { docType[0] = "S"; op[0] = "ENQ"; }  else // Sales Order Processing Contract Review
    if(service.equals("2030")) { docType[0] = "S"; op[0] = "ENQ"; }  else // Sales Order Processing Sales Manager
    if(service.equals("2031")) { docType[0] = "S"; op[0] = "ENQ"; }  else // Sales Order Processing Engineer
    if(service.equals("2032")) { docType[0] = "S"; op[0] = "ENQ"; }  else // Sales Order Processing Purchaser
    if(service.equals("2033")) { docType[0] = "S"; op[0] = "ENQ"; }  else // Sales Order Processing Scheduler
    if(service.equals("2034")) { docType[0] = "S"; op[0] = "ENQ"; }  else // Sales Order Processing Coordinator

    if(service.equals("1007")) { docType[0] = "S"; op[0] = "ENQ"; }  else // Delivery Status by Sales
    if(service.equals("107"))  { docType[0] = "S"; op[0] = "PGE"; }  else // Sales Order Tracking Menu

    if(service.equals("1200")) { docType[0] = ""; op[0] = "ENQ"; }  else // Sales: Detail
    if(service.equals("1201")) { docType[0] = ""; op[0] = "ENQ"; }  else // Sales: Book Orders
    if(service.equals("1202")) { docType[0] = ""; op[0] = "ENQ"; }  else // Sales: Closure Analysis
    if(service.equals("4002")) { docType[0] = ""; op[0] = "ENQ"; }  else // Customer Settlement History
    if(service.equals("120"))  { docType[0] = ""; op[0] = "PGE"; }  else // Sales Analytics

    // Document - PO
    if(service.equals("139"))  { docType[0] = "Y"; op[0] = "PGE"; }  else // Screen: Purchase Order Focus
    if(service.equals("1035")) { docType[0] = "Y"; op[0] = "ENQ"; }  else // Query: Purchase Order Lines Enquiry for a Manufacturer
    if(service.equals("1036")) { docType[0] = "Y"; op[0] = "ENQ"; }  else // Query: Purchase Order Lines Enquiry for a Supplier
    if(service.equals("1037")) { docType[0] = "Y"; op[0] = "ENQ"; }  else // Query: Purchase Order Delivery Performance
    if(service.equals("5006")) { docType[0] = "Y"; op[0] = "FTC"; }  else // Document: Fetch Purchase Order
    if(service.equals("5007")) { docType[0] = "Y"; op[0] = "CRT"; }  else // Document: Create New Purchase Order
    if(service.equals("5008")) { docType[0] = "Y"; op[0] = "CHN"; }  else // Document: Change Purchase Order Header
    if(service.equals("5009")) { docType[0] = "Y"; op[0] = "UPD"; }  else // Document: Update Purchase Order Header
    if(service.equals("5010")) { docType[0] = "Y"; op[0] = "AMN"; }  else // Document: Change Purchase Order Line
    if(service.equals("5011")) { docType[0] = "Y"; op[0] = "UPT"; }  else // Document: Update Purchase Order Line
    if(service.equals("5012")) { docType[0] = "Y"; op[0] = "DLL"; }  else // Document: Delete Purchase Order Line
    if(service.equals("5013")) { docType[0] = "Y"; op[0] = "PRN"; }  else // Document: Print Purchase Order
    if(service.equals("5014")) { docType[0] = "Y"; op[0] = "UPT"; }  else // Document: Set Confirmed Dates for all Lines on a Purchase Order
    if(service.equals("4190")) { docType[0] = "Y"; op[0] = "FAX"; }  else // Document: Fax Purchase Order

    // Query - PO
    if(service.equals("5015")) { docType[0] = "Y"; op[0] = "LST"; }  else // Document: List Purchase Orders
    if(service.equals("5072")) { docType[0] = "Y"; op[0] = "PGE"; }  else // Document: Create Purchase Order from Sales Order
    if(service.equals("7806")) { docType[0] = "Y"; op[0] = "ENQ"; }  else // Query: PO Lines Without Required-by Date
    if(service.equals("2022")) { docType[0] = "Y"; op[0] = "ENQ"; }  else // Query: Purchase Order Track and Trace

    // Document - LP
    if(service.equals("152"))  { docType[0] = "Z"; op[0] = "PGE"; }  else // Screen: Local Requisition Focus
    if(service.equals("5016")) { docType[0] = "Z"; op[0] = "FTC"; }  else // Document: Fetch Local Requisition
    if(service.equals("5017")) { docType[0] = "Z"; op[0] = "CRT"; }  else // Document: Create New Local Requisition
    if(service.equals("5018")) { docType[0] = "Z"; op[0] = "CHN"; }  else // Document: Change Local Requisition Header
    if(service.equals("5019")) { docType[0] = "Z"; op[0] = "UPD"; }  else // Document: Update Local Requisition Header
    if(service.equals("5020")) { docType[0] = "Z"; op[0] = "AMN"; }  else // Document: Change Local Requisition Line
    if(service.equals("5021")) { docType[0] = "Z"; op[0] = "UPT"; }  else // Document: Update Local Requisition Line
    if(service.equals("5022")) { docType[0] = "Z"; op[0] = "DLL"; }  else // Document: Delete Local Requisition Line
    if(service.equals("5023")) { docType[0] = "Z"; op[0] = "PRN"; }  else // Document: Print Local Requisition
    if(service.equals("4191")) { docType[0] = "Z"; op[0] = "FAX"; }  else // Document: Fax Local Requisition

    // Query - LP
    if(service.equals("5025")) { docType[0] = "Z"; op[0] = "LST"; }  else // Document: List Local Requisitions

    // Document - GRN
    if(service.equals("153"))  { docType[0] = "G"; op[0] = "PGE"; }  else // Screen: Goods Received Note Focus
    if(service.equals("3025")) { docType[0] = "G"; op[0] = "FTC"; }  else // Document: Fetch Goods Received Note
    // Document - PO
    if(service.equals("139"))  { docType[0] = "Y"; op[0] = "PGE"; }  else // Screen: Purchase Order Focus
    if(service.equals("1035")) { docType[0] = "Y"; op[0] = "ENQ"; }  else // Query: Purchase Order Lines Enquiry for a Manufacturer
    if(service.equals("1036")) { docType[0] = "Y"; op[0] = "ENQ"; }  else // Query: Purchase Order Lines Enquiry for a Supplier
    if(service.equals("1037")) { docType[0] = "Y"; op[0] = "ENQ"; }  else // Query: Purchase Order Delivery Performance
    if(service.equals("5006")) { docType[0] = "Y"; op[0] = "FTC"; }  else // Document: Fetch Purchase Order
    if(service.equals("5007")) { docType[0] = "Y"; op[0] = "CRT"; }  else // Document: Create New Purchase Order
    if(service.equals("5008")) { docType[0] = "Y"; op[0] = "CHN"; }  else // Document: Change Purchase Order Header
    if(service.equals("5009")) { docType[0] = "Y"; op[0] = "UPD"; }  else // Document: Update Purchase Order Header
    if(service.equals("5010")) { docType[0] = "Y"; op[0] = "AMN"; }  else // Document: Change Purchase Order Line
    if(service.equals("5011")) { docType[0] = "Y"; op[0] = "UPT"; }  else // Document: Update Purchase Order Line
    if(service.equals("5012")) { docType[0] = "Y"; op[0] = "DLL"; }  else // Document: Delete Purchase Order Line
    if(service.equals("5013")) { docType[0] = "Y"; op[0] = "PRN"; }  else // Document: Print Purchase Order
    if(service.equals("5014")) { docType[0] = "Y"; op[0] = "UPT"; }  else // Document: Set Confirmed Dates for all Lines on a Purchase Order
    if(service.equals("4190")) { docType[0] = "Y"; op[0] = "FAX"; }  else // Document: Fax Purchase Order

    // Query - PO
    if(service.equals("5015")) { docType[0] = "Y"; op[0] = "LST"; }  else // Document: List Purchase Orders
    if(service.equals("5072")) { docType[0] = "Y"; op[0] = "PGE"; }  else // Document: Create Purchase Order from Sales Order
    if(service.equals("7806")) { docType[0] = "Y"; op[0] = "ENQ"; }  else // Query: PO Lines Without Required-by Date
    if(service.equals("2022")) { docType[0] = "Y"; op[0] = "ENQ"; }  else // Query: Purchase Order Track and Trace

    // Document - LP
    if(service.equals("152"))  { docType[0] = "Z"; op[0] = "PGE"; }  else // Screen: Local Requisition Focus
    if(service.equals("5016")) { docType[0] = "Z"; op[0] = "FTC"; }  else // Document: Fetch Local Requisition
    if(service.equals("5017")) { docType[0] = "Z"; op[0] = "CRT"; }  else // Document: Create New Local Requisition
    if(service.equals("5018")) { docType[0] = "Z"; op[0] = "CHN"; }  else // Document: Change Local Requisition Header
    if(service.equals("5019")) { docType[0] = "Z"; op[0] = "UPD"; }  else // Document: Update Local Requisition Header
    if(service.equals("5020")) { docType[0] = "Z"; op[0] = "AMN"; }  else // Document: Change Local Requisition Line
    if(service.equals("5021")) { docType[0] = "Z"; op[0] = "UPT"; }  else // Document: Update Local Requisition Line
    if(service.equals("5022")) { docType[0] = "Z"; op[0] = "DLL"; }  else // Document: Delete Local Requisition Line
    if(service.equals("5023")) { docType[0] = "Z"; op[0] = "PRN"; }  else // Document: Print Local Requisition
    if(service.equals("4191")) { docType[0] = "Z"; op[0] = "FAX"; }  else // Document: Fax Local Requisition

    // Query - LP
    if(service.equals("5025")) { docType[0] = "Z"; op[0] = "LST"; }  else // Document: List Local Requisitions

    // Document - GRN
    if(service.equals("153"))  { docType[0] = "G"; op[0] = "PGE"; }  else // Screen: Goods Received Note Focus
    if(service.equals("3025")) { docType[0] = "G"; op[0] = "FTC"; }  else // Document: Fetch Goods Received Note
    if(service.equals("3026")) { docType[0] = "G"; op[0] = "CRT"; }  else // Document: Create New Goods Received Note
    if(service.equals("3027")) { docType[0] = "G"; op[0] = "CHN"; }  else // Document: Change Goods Received Note header
    if(service.equals("3028")) { docType[0] = "G"; op[0] = "UPD"; }  else // Document: Update Goods Received Note Header
    if(service.equals("3029")) { docType[0] = "G"; op[0] = "AMN"; }  else // Document: Change Goods Received Note Line
    if(service.equals("3030")) { docType[0] = "G"; op[0] = "UPT"; }  else // Document: Update Goods Received Note Line
    if(service.equals("3031")) { docType[0] = "G"; op[0] = "DLL"; }  else // Document: Delete Goods Received Note Line
    if(service.equals("3033")) { docType[0] = "G"; op[0] = "PGE"; }  else // Document: Build Goods Received Note from Purchase Orders
    if(service.equals("3034")) { docType[0] = "G"; op[0] = "PGE"; }  else // Document: Build Goods Received Note from Local Requisitions
    if(service.equals("3035")) { docType[0] = "G"; op[0] = "CRT"; }  else // Document: Create Purchase Invoice from Goods Received Note
    if(service.equals("3037")) { docType[0] = "G"; op[0] = "UPD"; }  else // Document: Update GRN Stock Position

    // Query - GRN
    if(service.equals("3036")) { docType[0] = "G"; op[0] = "LST"; }  else // Document: List Goods Received Notes
    if(service.equals("3024")) { docType[0] = "G"; op[0] = "ENQ"; }  else // Query: Check Waiting Orders For An Item

    // Document - PI
    if(service.equals("154"))  { docType[0] = "J"; op[0] = "PGE"; }  else // Screen: Purchase Invoice Focus
    if(service.equals("5080")) { docType[0] = "J"; op[0] = "FTC"; }  else // Document: Fetch Purchase Invoice
    if(service.equals("5081")) { docType[0] = "J"; op[0] = "CRT"; }  else // Document: Create New Purchase Invoice
    if(service.equals("5082")) { docType[0] = "J"; op[0] = "CHN"; }  else // Document: Change Purchase Invoice Header
    if(service.equals("5083")) { docType[0] = "J"; op[0] = "UPD"; }  else // Document: Update Purchase Invoice Header
    if(service.equals("5084")) { docType[0] = "J"; op[0] = "AMN"; }  else // Document: Change Purchase Invoice Line
    if(service.equals("5085")) { docType[0] = "J"; op[0] = "UPT"; }  else // Document: Update Purchase Invoice Line
    if(service.equals("5086")) { docType[0] = "J"; op[0] = "DLL"; }  else // Document: Delete Purchase Invoice Line

    // Query - PI
    if(service.equals("5088")) { docType[0] = "J"; op[0] = "LST"; }  else // Document: List Purchase Invoices
    if(service.equals("4314")) { docType[0] = "J"; op[0] = "ENQ"; }  else // Report: Purchase Invoices Monthly Listing
    if(service.equals("2040")) { docType[0] = "J"; op[0] = "ENQ"; }  else // Query: Purchase Invoice Track and Trace

    // Document - PCN
    if(service.equals("162"))  { docType[0] = "F"; op[0] = "PGE"; }  else // Screen: Purchase Credit Note Focus
    if(service.equals("5026")) { docType[0] = "F"; op[0] = "FTC"; }  else // Document: Fetch Purchase Credit Note
    if(service.equals("5027")) { docType[0] = "F"; op[0] = "CRT"; }  else // Document: Create New Purchase Credit Note
    if(service.equals("5028")) { docType[0] = "F"; op[0] = "CHN"; }  else // Document: Change Purchase Credit Note Header
    if(service.equals("5029")) { docType[0] = "F"; op[0] = "UPD"; }  else // Document: Update Purchase Credit Note Header
    if(service.equals("5030")) { docType[0] = "F"; op[0] = "AMN"; }  else // Document: Change Purchase Credit Note Line
    if(service.equals("5031")) { docType[0] = "F"; op[0] = "UPT"; }  else // Document: Update Purchase Credit Note Line
    if(service.equals("5032")) { docType[0] = "F"; op[0] = "DLL"; }  else // Document: Delete Purchase Credit Note Line

    // Query - PCN
    if(service.equals("5035")) { docType[0] = "F"; op[0] = "LST"; }  else // Document: List Purchase Credit Notes
    if(service.equals("4315")) { docType[0] = "F"; op[0] = "ENQ"; }  else // Report: Purchase Credit Notes Monthly Listing

    // Document - PDN
    if(service.equals("163"))  { docType[0] = "H"; op[0] = "PGE"; }  else // Screen: Purchase Debit Note Focus
    if(service.equals("5036")) { docType[0] = "H"; op[0] = "FTC"; }  else // Document: Fetch Purchase Debit Note
    if(service.equals("5037")) { docType[0] = "H"; op[0] = "CRT"; }  else // Document: Create New Purchase Debit Note
    if(service.equals("5038")) { docType[0] = "H"; op[0] = "CHN"; }  else // Document: Change Purchase Debit Note Header
    if(service.equals("5039")) { docType[0] = "H"; op[0] = "UPD"; }  else // Document: Update Purchase Debit Note Header
    if(service.equals("5040")) { docType[0] = "H"; op[0] = "AMN"; }  else // Document: Change Purchase Debit Note Line
    if(service.equals("5041")) { docType[0] = "H"; op[0] = "UPT"; }  else // Document: Update Purchase Debit Note Line
    if(service.equals("5042")) { docType[0] = "H"; op[0] = "DLL"; }  else // Document: Delete Purchase Debit Note Line

    // Query - PDN
    if(service.equals("5045")) { docType[0] = "H"; op[0] = "LST"; }  else // Document: List Purchase Debit Notes
    if(service.equals("4317")) { docType[0] = "H"; op[0] = "ENQ"; }  else // Report: Purchase Debit Notes Monthly Listing

    // Purchases Transactions
    if(service.equals("1028")) { docType[0] = ""; op[0] = "ENQ"; }  else // Word Search Purchasing Document Lines
    if(service.equals("1034")) { docType[0] = ""; op[0] = "ENQ"; }  else // Query: Purchase Invoice Listing Enquiry by Date
    if(service.equals("1032")) { docType[0] = ""; op[0] = "PGE"; }  else // Purchasing Document Lines Enquiry
    if(service.equals("122"))  { docType[0] = ""; op[0] = "PGE"; }  else // Screen: Purchasing Analytics Menu
    if(service.equals("5002")) { docType[0] = ""; op[0] = "ENQ"; }  else // Creditor Settlement History
    if(service.equals("182"))  { docType[0] = ""; op[0] = "PGE"; }  else // Purchasing Control

    // Accounts Transactions
    if(service.equals("105"))  { docType[0] = ""; op[0] = "PGE"; } else // Accounts Year Selection
    if(service.equals("109"))  { docType[0] = ""; op[0] = "PGE"; } else // Screen: Accounts Services
    if(service.equals("106"))  { docType[0] = ""; op[0] = "PGE"; } else // Screen: Debtor Settlement Services
    if(service.equals("133"))  { docType[0] = ""; op[0] = "PGE"; } else // Screen: Creditor Settlement Services
    if(service.equals("6032")) { docType[0] = ""; op[0] = "PGE"; } else // Screen: Verification Services
    if(service.equals("110"))  { docType[0] = ""; op[0] = "PGE"; } else // Screen: Accounts Analytics Charts
    if(service.equals("6002")) { docType[0] = ""; op[0] = "PGE"; } else // Process: Bank Reconciliation
    if(service.equals("6003")) { docType[0] = ""; op[0] = "PGE"; } else // Process: GST Reconciliation
    if(service.equals("6006")) { docType[0] = ""; op[0] = "PGE"; } else // Process: Detail WAC Derivation
    if(service.equals("6007")) { docType[0] = ""; op[0] = "ENQ"; } else // Process: View Account
    if(service.equals("6008")) { docType[0] = ""; op[0] = "ENQ"; } else // Process: View Account
    if(service.equals("6015")) { docType[0] = ""; op[0] = "ENQ"; } else // Listing: Chart of Accounts
    if(service.equals("6016")) { docType[0] = ""; op[0] = "PGE"; } else // Process: Modify Chart of Accounts
    if(service.equals("6027")) { docType[0] = ""; op[0] = "ENQ"; } else // Listing: Journal Batch
    if(service.equals("6022")) { docType[0] = ""; op[0] = "PGE"; } else // Process: Modify Journal Batches
    if(service.equals("6023")) { docType[0] = ""; op[0] = "PGE"; } else // Process: Modify Journal Batches
    if(service.equals("6045")) { docType[0] = ""; op[0] = "PGE"; } else // Process: Verify Journal Batches
    if(service.equals("6028")) { docType[0] = ""; op[0] = "ENQ"; } else // Process: View the General Ledger
    if(service.equals("6029")) { docType[0] = ""; op[0] = "ENQ"; } else // Process: View the Debtors Ledger
    if(service.equals("6030")) { docType[0] = ""; op[0] = "ENQ"; } else // Process: View the Creditors Ledger
    if(service.equals("6031")) { docType[0] = ""; op[0] = "PGE"; } else // Listing: General Ledger
    if(service.equals("6053")) { docType[0] = ""; op[0] = "ENQ"; } else // Report: Trial Balance
    if(service.equals("6054")) { docType[0] = ""; op[0] = "ENQ"; } else // Report: Balance Sheet
    if(service.equals("6055")) { docType[0] = ""; op[0] = "ENQ"; } else // Report: Profit and Loss
    if(service.equals("1101")) { docType[0] = ""; op[0] = "CHR"; } else // Chart: Profit and Loss
    if(service.equals("1102")) { docType[0] = ""; op[0] = "CHR"; } else // Chart: Balance Sheet
    if(service.equals("2037")) { docType[0] = ""; op[0] = "PGE"; } else // Control: Sales Order Processing Invoicer
    if(service.equals("6020")) { docType[0] = ""; op[0] = "PGE"; } else // Process: Year Closing and Starting
    if(service.equals("6033")) { docType[0] = ""; op[0] = "PGE"; } else // Process: Stock Reconciliation Analysis
    if(service.equals("6034")) { docType[0] = ""; op[0] = "PGE"; } else // Process: Debtors Opening Balances Verification
    if(service.equals("6035")) { docType[0] = ""; op[0] = "PGE"; } else // Process: Creditors Opening Balances Verification
    if(service.equals("6036")) { docType[0] = ""; op[0] = "PGE"; } else // Process: Work-in-Progress Analysis
    if(service.equals("6037")) { docType[0] = ""; op[0] = "PGE"; } else // Process: Stock-in-Transit Analysis
    if(service.equals("108"))  { docType[0] = ""; op[0] = "PGE"; } else // Screen: Self-Audit Validation

    // Accounts - Definitions
    if(service.equals("7067")) { docType[0] = ""; op[0] = "PGE"; }  else // Definitions: Bank Accounts
    if(service.equals("7054")) { docType[0] = ""; op[0] = "PGE"; }  else // Definitions: Currencies
    if(service.equals("7055")) { docType[0] = ""; op[0] = "PGE"; }  else // Definitions: Currency Rates
    if(service.equals("7061")) { docType[0] = ""; op[0] = "PGE"; }  else // Definitions: GST Rates

    // Accounts - Payables
    if(service.equals("165"))  { docType[0] = "A"; op[0] = "PGE"; } else // Payables Selection
    if(service.equals("5047")) { docType[0] = "A"; op[0] = "PGE"; } else // Process: Payables Builder
    if(service.equals("164"))  { docType[0] = "A"; op[0] = "PGE"; } else // Screen: Payment Record Focus
    if(service.equals("5049")) { docType[0] = "A"; op[0] = "FTC"; } else // fetch payment record
    if(service.equals("5050")) { docType[0] = "A"; op[0] = "CRT"; } else // Record: Create New Payment Record
    if(service.equals("5051")) { docType[0] = "A"; op[0] = "CHN"; } else // Record: Change Payment Record Header
    if(service.equals("5052")) { docType[0] = "A"; op[0] = "UPD"; } else // Record: Update Payment Record Header
    if(service.equals("5053")) { docType[0] = "A"; op[0] = "AMN"; } else // Record: Change Payment Record Line
    if(service.equals("5054")) { docType[0] = "A"; op[0] = "UPT"; } else // Record: Update Payment Record Line
    if(service.equals("5055")) { docType[0] = "A"; op[0] = "DLL"; } else // Record: Delete Payment Record Line
    if(service.equals("5056")) { docType[0] = "A"; op[0] = "PRN"; } else // Record: Print Payment Advice
    if(service.equals("5057")) { docType[0] = "A"; op[0] = "LST"; } else // Record: List Payment Records
    if(service.equals("4192")) { docType[0] = "A"; op[0] = "FAX"; } else // Record: Fax Payment Advice
    if(service.equals("4312")) { docType[0] = "A"; op[0] = "ENQ"; } else // Listing: Payments Monthly

    // Accounts - Receivables
    if(service.equals("158"))  { docType[0] = "R"; op[0] = "PGE"; } else // Receivables Selection
    if(service.equals("4203")) { docType[0] = "R"; op[0] = "PGE"; } else // Process: Receivables Builder
    if(service.equals("157"))  { docType[0] = "R"; op[0] = "PGE"; } else // Screen: Receipt Record Focus
    if(service.equals("4205")) { docType[0] = "R"; op[0] = "FTC"; } else // Record: Fetch Receipt Record
    if(service.equals("4206")) { docType[0] = "R"; op[0] = "CRT"; } else // Record: Create New Receipt Record
    if(service.equals("4207")) { docType[0] = "R"; op[0] = "CHN"; } else // Record: Change Receipt Record Header
    if(service.equals("4208")) { docType[0] = "R"; op[0] = "UPD"; } else // Record: Update Receipt Record Header
    if(service.equals("4209")) { docType[0] = "R"; op[0] = "AMN"; } else // Record: Change Receipt Record Line
    if(service.equals("4210")) { docType[0] = "R"; op[0] = "UPT"; } else // Record: Update Receipt Record Line
    if(service.equals("4211")) { docType[0] = "R"; op[0] = "DLL"; } else // Record: Delete Receipt Record Line
    if(service.equals("4212")) { docType[0] = "R"; op[0] = "PRN"; } else // Record: Print Receipt
    if(service.equals("4213")) { docType[0] = "R"; op[0] = "LST"; } else // Record: List Receipt Records
    if(service.equals("4189")) { docType[0] = "R"; op[0] = "FAX"; } else // Record: Fax Receipt
    if(service.equals("4310")) { docType[0] = "R"; op[0] = "ENQ"; } else // Listing: Receipts Monthly Listing

    // Accounts - IAT
    if(service.equals("161"))  { docType[0] = "T"; op[0] = "PGE"; } else // Screen: Inter-Account Transfer Focus
    if(service.equals("6077")) { docType[0] = "T"; op[0] = "FTC"; } else // Record: Fetch InterAccount Transfer
    if(service.equals("6078")) { docType[0] = "T"; op[0] = "CRT"; } else // Record: Create New InterAccount Transfer
    if(service.equals("6079")) { docType[0] = "T"; op[0] = "CHN"; } else // Record: Change InterAccount Transfer Header
    if(service.equals("6080")) { docType[0] = "T"; op[0] = "UPD"; } else // Record: Update InterAccount Transfer Header

    // Query - IAT
    if(service.equals("6085")) { docType[0] = "T"; op[0] = "LST"; } else // Record: List InterAccount Transfers

    // Accounts - RV
    if(service.equals("159"))  { docType[0] = "U"; op[0] = "PGE"; } else // Screen: Receipt Voucher Focus
    if(service.equals("6056")) { docType[0] = "U"; op[0] = "FTC"; } else // Record: Fetch Receipt Voucher
    if(service.equals("6057")) { docType[0] = "U"; op[0] = "CRT"; } else // Record: Create New Receipt Voucher
    if(service.equals("6058")) { docType[0] = "U"; op[0] = "CHN"; } else // Record: Change Receipt Voucher Header
    if(service.equals("6059")) { docType[0] = "U"; op[0] = "UPD"; } else // Record: Update Receipt Voucher Header
    if(service.equals("6060")) { docType[0] = "U"; op[0] = "AMN"; } else // Record: Change Receipt Voucher Line
    if(service.equals("6061")) { docType[0] = "U"; op[0] = "UPT"; } else // Record: Update Receipt Voucher Line
    if(service.equals("6062")) { docType[0] = "U"; op[0] = "DLL"; } else // Record: Delete Receipt Voucher Line

    // Query - RV
    if(service.equals("6064")) { docType[0] = "U"; op[0] = "LST"; } else // Record: List Receipt Vouchers
    if(service.equals("4318")) { docType[0] = "U"; op[0] = "ENQ"; } else // Listing: Receipt Vouchers Monthly

    // Accounts - PV
    if(service.equals("160"))  { docType[0] = "V"; op[0] = "PGE"; } else // Screen: Payment Voucher Focus
    if(service.equals("6066")) { docType[0] = "V"; op[0] = "FTC"; } else // Record: Fetch Payment Voucher
    if(service.equals("6067")) { docType[0] = "V"; op[0] = "CRT"; } else // Record: Create New Payment Voucher
    if(service.equals("6068")) { docType[0] = "V"; op[0] = "CHN"; } else // Record: Change Payment Voucher Header
    if(service.equals("6069")) { docType[0] = "V"; op[0] = "UPD"; } else // Record: Update Payment Voucher Header
    if(service.equals("6070")) { docType[0] = "V"; op[0] = "AMN"; } else // Record: Change Payment Voucher Line
    if(service.equals("6071")) { docType[0] = "V"; op[0] = "UPT"; } else // Record: Update Payment Voucher Line
    if(service.equals("6072")) { docType[0] = "V"; op[0] = "DLL"; } else // Record: Delete Payment Voucher Line
    if(service.equals("6073")) { docType[0] = "V"; op[0] = "PRN"; } else // Record: Print Payment Voucher

    // Query - PV
    if(service.equals("6074")) { docType[0] = "V"; op[0] = "LST"; } else // Record: List Payment Vouchers
    if(service.equals("4319")) { docType[0] = "V"; op[0] = "ENQ"; } else // Listing: Payment Vouchers Monthly

    // Definitions
    if(service.equals("115"))  { docType[0] = ""; op[0] = "PGE"; } else // Screen: Definition Services

    if(service.equals("7001")) { docType[0] = ""; op[0] = "OPU"; } else // Definitions: SalesPeople
    if(service.equals("7062")) { docType[0] = ""; op[0] = "OPU"; } else // Definitions: Configuration
    if(service.equals("7058")) { docType[0] = ""; op[0] = "OPU"; } else // Definitions: Company Types
    if(service.equals("7057")) { docType[0] = ""; op[0] = "OPU"; } else // Definitions: Countries
    if(service.equals("7031")) { docType[0] = ""; op[0] = "OPU"; } else // Definitions: Document Codes
    if(service.equals("7059")) { docType[0] = ""; op[0] = "OPU"; } else // Definitions: Industry Types
    if(service.equals("7063")) { docType[0] = ""; op[0] = "OPU"; } else // Definitions: Likelihood Ratings
    if(service.equals("7064")) { docType[0] = ""; op[0] = "OPU"; } else // Definitions: Quotation Status
    if(service.equals("7065")) { docType[0] = ""; op[0] = "OPU"; } else // Definitions: Quotation Reasons
    if(service.equals("7060")) { docType[0] = ""; op[0] = "OPU"; } else // Definitions: Stores
    if(service.equals("7070")) { docType[0] = ""; op[0] = "OPU"; } else // Definitions: Document Print Options

    // Admin
    if(service.equals("117"))  { docType[0] = ""; op[0] = "PGE"; } else // Screen: Administration System Services

    if(service.equals("7029")) { docType[0] = ""; op[0] = "OPU"; } else // Process: Set System Status
    if(service.equals("7049")) { docType[0] = ""; op[0] = "OPU"; } else // Definitions: Modify Site Style
    if(service.equals("7081")) { docType[0] = ""; op[0] = "OPU"; } else // Definitions: Edit Site Styling

    if(service.equals("111"))  { docType[0] = ""; op[0] = "PGE"; } else // Screen: Administration Services
    if(service.equals("188"))  { docType[0] = ""; op[0] = "ENQ"; } else // About
    if(service.equals("129"))  { docType[0] = ""; op[0] = "ENQ"; } else // Process: Location Services

    if(service.equals("112"))  { docType[0] = ""; op[0] = "PGE"; } else // Screen: Administration User Services

    if(service.equals("7008")) { docType[0] = ""; op[0] = "OPU"; } else // Definitions: User Modules
    if(service.equals("7009")) { docType[0] = ""; op[0] = "OPU"; } else // Definitions: Casual, Registered, and Demo User Services
    if(service.equals("7003")) { docType[0] = ""; op[0] = "OPU"; } else // Modify Casual & Registered User Access
    if(service.equals("7004")) { docType[0] = ""; op[0] = "OPU"; } else // List Detailed Access Rights
    if(service.equals("7074")) { docType[0] = ""; op[0] = "OPU"; } else // Definitions: Change User Style
    if(service.equals("7051")) { docType[0] = ""; op[0] = "OPU"; } else // User Group Manager
    if(service.equals("7030")) { docType[0] = ""; op[0] = "PGE"; } else // Process: Access Log Manager
    if(service.equals("7034")) { docType[0] = ""; op[0] = "PGE"; } else // Process: Access Services Manager

    // Contacts
    if(service.equals("8801")) { docType[0] = ""; op[0] = "ENQ"; } else // Contacts: Address Book
    if(service.equals("8802")) { docType[0] = ""; op[0] = "ENQ"; } else // View Others' Contacts
    if(service.equals("8803")) { docType[0] = ""; op[0] = "OPU"; } else // Sharing
    if(service.equals("8804")) { docType[0] = ""; op[0] = "ENQ"; } else // Show Contacts for a Company
    if(service.equals("8805")) { docType[0] = ""; op[0] = "ENQ"; } else // Scan Contacts File for a Company
    if(service.equals("8806")) { docType[0] = ""; op[0] = "ENQ"; } else // List All Companies' Contacts
    if(service.equals("8811")) { docType[0] = ""; op[0] = "OPU"; } else // Contacts: Add New Contact
    if(service.equals("8812")) { docType[0] = ""; op[0] = "UPL"; } else // Contacts: Upload Address Book
    if(service.equals("8816")) { docType[0] = ""; op[0] = "OPU"; } else // Process: Create External Access
    if(service.equals("8817")) { docType[0] = ""; op[0] = "ENQ"; } else // Process: List/Select External Access
    if(service.equals("8818")) { docType[0] = ""; op[0] = "OPU"; } else // Process: Delete External Access
    if(service.equals("8819")) { docType[0] = ""; op[0] = "OPU"; } else // Process: Approval Rights External Access
    if(service.equals("8821")) { docType[0] = ""; op[0] = "ENQ"; } else // Contact Details
    if(service.equals("8830")) { docType[0] = ""; op[0] = "ENQ"; } else // Profile: List all Profiles
    if(service.equals("8831")) { docType[0] = ""; op[0] = "ENQ"; } else // Profile: View My Profile
    if(service.equals("8841")) { docType[0] = ""; op[0] = "ENQ"; } else // Contacts: Services
    if(service.equals("8860")) { docType[0] = ""; op[0] = "ENQ"; } else // Organization Definition

    // Fax
    if(service.equals("11000")) { docType[0] = ""; op[0] = "ENQ"; } else // Process: View Outgoing Faxes
    if(service.equals("11002")) { docType[0] = ""; op[0] = "PGE"; } else // Send a Fax
    if(service.equals("11003")) { docType[0] = ""; op[0] = "ENQ"; } else // View Historical Faxes
    if(service.equals("11004")) { docType[0] = ""; op[0] = "OPU"; } else // Abort an Outgoing Fax
    if(service.equals("11005")) { docType[0] = ""; op[0] = "OPU"; } else // ReSend an Historical Fax
    if(service.equals("11006")) { docType[0] = ""; op[0] = "PGE"; } else // Compose a Fax
    if(service.equals("11011")) { docType[0] = ""; op[0] = "OPU"; } else // Access Fax Options

    // Document Library
    if(service.equals("12000")) { docType[0] = ""; op[0] = "PGE"; }  else // Process: View Directory Contents
    if(service.equals("12001")) { docType[0] = ""; op[0] = "OPU"; }  else // Process: Edit Document Details
    if(service.equals("12002")) { docType[0] = ""; op[0] = "UPL"; }  else // Process: Upload a File
    if(service.equals("12003")) { docType[0] = ""; op[0] = "DNL"; }  else // Process: Download a File
    if(service.equals("12004")) { docType[0] = ""; op[0] = "DNL"; }  else // Process: Check-out a File
    if(service.equals("12005")) { docType[0] = ""; op[0] = "UPL"; }  else // Process: Check-in a File
    if(service.equals("12006")) { docType[0] = ""; op[0] = "ENQ"; }  else // Process: View Archive
    if(service.equals("12008")) { docType[0] = ""; op[0] = "OPU"; }  else // Process: Directory Insert
    if(service.equals("12009")) { docType[0] = ""; op[0] = "OPU"; }  else // Process: Directory Delete
    if(service.equals("12010")) { docType[0] = ""; op[0] = "ENQ"; }  else // Process: Directory List
    if(service.equals("12011")) { docType[0] = ""; op[0] = "OPU"; }  else // Process: Options Access
    if(service.equals("12013")) { docType[0] = ""; op[0] = "DNL"; }  else // Process: External User Download
    if(service.equals("12014")) { docType[0] = ""; op[0] = "ENQ"; }  else // Process: View All Directories
    if(service.equals("12015")) { docType[0] = ""; op[0] = "ENQ"; }  else // Process: View Properties
    if(service.equals("12016")) { docType[0] = ""; op[0] = "OPU"; }  else // Process: Manage a Document

    // Casual
    if(service.equals("801")) { docType[0] = ""; op[0] = "PGE"; }  else // Screen: Main Page
    if(service.equals("802")) { docType[0] = ""; op[0] = "PGE"; }  else // Screen: Products Page
    if(service.equals("803")) { docType[0] = ""; op[0] = "PGE"; }  else // Screen: Transactions Page
    if(service.equals("804")) { docType[0] = ""; op[0] = "PGE"; }  else // Screen: Registration Page
    if(service.equals("808")) { docType[0] = ""; op[0] = "PGE"; }  else // Screen: Catalogs Page
    if(service.equals("811")) { docType[0] = ""; op[0] = "ENQ"; }  else // Process: View Cart

    // Registered
    if(service.equals("901"))  { docType[0] = ""; op[0] = "PGE"; }  else // Process: Main Page
    if(service.equals("902"))  { docType[0] = ""; op[0] = "PGE"; }  else // Process: Products Page
    if(service.equals("903"))  { docType[0] = ""; op[0] = "PGE"; }  else // Process: Transactions Page
    if(service.equals("904"))  { docType[0] = ""; op[0] = "PGE"; }  else // Process: Accounts Page
    if(service.equals("905"))  { docType[0] = ""; op[0] = "PGE"; }  else // Process: Reports Page
    if(service.equals("906"))  { docType[0] = ""; op[0] = "PGE"; }  else // Process: Registration Page
    if(service.equals("907"))  { docType[0] = ""; op[0] = "PGE"; }  else // Process: Projects Page
    if(service.equals("908"))  { docType[0] = ""; op[0] = "PGE"; }  else // Process: Catalogs Page
    if(service.equals("909"))  { docType[0] = ""; op[0] = "ENQ"; }  else // Process: Sales Document Word Search
    if(service.equals("911"))  { docType[0] = ""; op[0] = "ENQ"; }  else // Process: View Cart
    if(service.equals("914"))  { docType[0] = ""; op[0] = "OPU"; }  else // Process: Edit Checkout Information
    if(service.equals("915"))  { docType[0] = ""; op[0] = "UPD"; }  else // Process: Save to Inbox
    if(service.equals("4121")) { docType[0] = ""; op[0] = "FTC"; }  else // Document: View a Sales Order
    if(service.equals("4122")) { docType[0] = ""; op[0] = "FTC"; }  else // Document: View a Quotation
    if(service.equals("4126")) { docType[0] = ""; op[0] = "FTC"; }  else // Document: View an Order Confirmation
    if(service.equals("4127")) { docType[0] = ""; op[0] = "FTC"; }  else // Document: View an Order Acknowledgement
    if(service.equals("4123")) { docType[0] = ""; op[0] = "FTC"; }  else // Document: View a Delivery Order
    if(service.equals("4124")) { docType[0] = ""; op[0] = "FTC"; }  else // Document: View a Sales Invoice
    if(service.equals("4125")) { docType[0] = ""; op[0] = "FTC"; }  else // Document: View a Proforma Invoice
    if(service.equals("1016")) { docType[0] = ""; op[0] = "ENQ"; }  else // Process: Generate a Statement of Account
    if(service.equals("2012")) { docType[0] = ""; op[0] = "ENQ"; }  else // Query: ExtUsers Transaction Track & Trace

    // Transactions - Stock
    if(service.equals("118"))  { docType[0] = "3"; op[0] = "PGE"; }  else // Screen: Stock Charts Services Menu
    if(service.equals("180"))  { docType[0] = "3"; op[0] = "PGE"; }  else // Screen: Stock Control Menu
    if(service.equals("102"))  { docType[0] = "3"; op[0] = "PGE"; }  else // Screen: Product Services Menu Screen

    if(service.equals("3002")) { docType[0] = "3"; op[0] = "ENQ"; }  else // View Stock Item Purchase Prices
    if(service.equals("3059")) { docType[0] = "3"; op[0] = "ENQ"; }  else // Process: Get Stock Pendings
    if(service.equals("1003")) { docType[0] = "3"; op[0] = "CHR"; }  else // Chart: Stock Purchasing vs. Sales
    if(service.equals("1014")) { docType[0] = "3"; op[0] = "ENQ"; }  else // Report: Stock Usage Enquiry for Customer
    if(service.equals("1017")) { docType[0] = "3"; op[0] = "ENQ"; }  else // Report: Stock Usage Enquiry for Supplier
    if(service.equals("1021")) { docType[0] = "3"; op[0] = "ENQ"; }  else // Stock ReOrder Generation
    if(service.equals("1022")) { docType[0] = "3"; op[0] = "ENQ"; }  else // Report: Manufacturer Sales by Customer
    if(service.equals("3014")) { docType[0] = "3"; op[0] = "ENQ"; }  else // Report: Stock Usage Spread for a Manufacturer
    if(service.equals("7802")) { docType[0] = "3"; op[0] = "ENQ"; }  else // Report: Stock Requirements Status
    if(service.equals("3010")) { docType[0] = "3"; op[0] = "ENQ"; }  else // Listing: Print Stock Records List
    if(service.equals("3062")) { docType[0] = "3"; op[0] = "ENQ"; }  else // Report: Stock Status
    if(service.equals("3063")) { docType[0] = "3"; op[0] = "ENQ"; }  else // Report: Stock Status Valuation Differences
    if(service.equals("2020")) { docType[0] = "3"; op[0] = "LST"; }  else // Paged Stock Listing
    if(service.equals("3057")) { docType[0] = "3"; op[0] = "DNL"; }  else // Process: Stock File Download
    if(service.equals("3058")) { docType[0] = "3"; op[0] = "UPL"; }  else // Process: Stock File Upload Update
    if(service.equals("3053")) { docType[0] = "3"; op[0] = "OPU"; }  else // Process: Update Stock Prices
    if(service.equals("3061")) { docType[0] = "3"; op[0] = "ENQ"; }  else // Query: Stock Prices Out-of-Date

    // Queries - Stock
    if(service.equals("1001")) { docType[0] = "3"; op[0] = "PGE"; }  else // Query: Stock Enquiry
    if(service.equals("1002")) { docType[0] = "3"; op[0] = "PGE"; }  else // Query: Stock History Enquiry
    if(service.equals("3052")) { docType[0] = "3"; op[0] = "ENQ"; }  else // Query: Stock Trace
    if(service.equals("2001")) { docType[0] = "3"; op[0] = "PGE"; }  else // Query: Fast Fetch by Our Stock Code
    if(service.equals("2002")) { docType[0] = "3"; op[0] = "PGE"; }  else // Query: Word Search of Stock File
    if(service.equals("2006")) { docType[0] = "3"; op[0] = "LST"; }  else // Select by Our Stock Listings
    if(service.equals("2008")) { docType[0] = "3"; op[0] = "PGE"; }  else // Search by Manufacturer (List)

    // Records - Stock
    if(service.equals("144"))  { docType[0] = "3"; op[0] = "PGE"; }  else // Screen: Stock Records Focus Screen
    if(service.equals("3001")) { docType[0] = "3"; op[0] = "FTC"; }  else // Record: Fetch Stock Record
    if(service.equals("3003")) { docType[0] = "3"; op[0] = "CRT"; }  else // Record: Create new Stock Record
    if(service.equals("3004")) { docType[0] = "3"; op[0] = "CHN"; }  else // Record: Change Stock Record
    if(service.equals("3005")) { docType[0] = "3"; op[0] = "UPD"; }  else // Record: Update Stock Record
    if(service.equals("3006")) { docType[0] = "3"; op[0] = "AMN"; }  else // Record: Change Stock Record Store Line
    if(service.equals("3007")) { docType[0] = "3"; op[0] = "UPT"; }  else // Record: Update Stock Record Store Line
    if(service.equals("3008")) { docType[0] = "3"; op[0] = "DLL"; }  else // Record: Delete Stock Record Store Line

    // DataBase Updating - Stock
    if(service.equals("125"))  { docType[0] = "3"; op[0] = "PGE"; }  else // Screen: Stock Updating Services

    if(service.equals("3009")) { docType[0] = "3"; op[0] = "PGE"; }  else // Record: Replace Stock Records
    if(service.equals("3015")) { docType[0] = "3"; op[0] = "PGE"; }  else // Record: Replace Stock Records by Upload
    if(service.equals("3016")) { docType[0] = "3"; op[0] = "PGE"; }  else // Record: Remove a Stock Record
    if(service.equals("3017")) { docType[0] = "3"; op[0] = "PGE"; }  else // Record: Remove Stock Records by Upload
    if(service.equals("3022")) { docType[0] = "3"; op[0] = "PGE"; }  else // Record: Add Stock Records by Upload
    if(service.equals("3023")) { docType[0] = "3"; op[0] = "PGE"; }  else // Record: Renumber a Stock Record

    // Stock Transactions

    // Catalogs
    if(service.equals("114"))  { docType[0] = "4"; op[0] = "PGE"; }  else // Screen: Catalog Services Menu

    if(service.equals("2000")) { docType[0] = "4"; op[0] = "ENQ"; }  else // Process: Stock Availability
    if(service.equals("2003")) { docType[0] = "4"; op[0] = "ENQ"; }  else // Process: Browse Catalogs
    if(service.equals("2014")) { docType[0] = "4"; op[0] = "ENQ"; }  else // Process: Browse Buyers Catalog
    if(service.equals("2004")) { docType[0] = "4"; op[0] = "ENQ"; }  else // Process: Browse WCF-Linked Catalog
    if(service.equals("2005")) { docType[0] = "4"; op[0] = "ENQ"; }  else // Process: Browse Non-WCF-Linked Catalog
    if(service.equals("2010")) { docType[0] = "4"; op[0] = "OPU"; }  else // Process: Import WCF  Catalog
    if(service.equals("2015")) { docType[0] = "4"; op[0] = "OPU"; }  else // Process: Export WCF Catalog
    if(service.equals("2011")) { docType[0] = "4"; op[0] = "OPU"; }  else // Process: Manage Linked Catalogs
    if(service.equals("7024")) { docType[0] = "4"; op[0] = "OPU"; }  else // Process: Maintain Buyers Catalog
    if(service.equals("7078")) { docType[0] = "4"; op[0] = "OPU"; }  else // Process: Scan Documents to Create Buyers  Catalog
    if(service.equals("2038")) { docType[0] = "4"; op[0] = "CNV"; }  else // Process: Print Catalog as PDF
    if(service.equals("7071")) { docType[0] = "4"; op[0] = "OPU"; }  else // Definitions: Stock Category
    if(service.equals("7072")) { docType[0] = "4"; op[0] = "OPU"; }  else // Process: Categorize Stock for Catalogs
    if(service.equals("7079")) { docType[0] = "4"; op[0] = "OPU"; }  else // Process: Set Show-to-Web for a Manufacturer
    if(service.equals("7032")) { docType[0] = "4"; op[0] = "OPU"; }  else // Process: Modify  Catalogs List Page
    if(service.equals("7006")) { docType[0] = "4"; op[0] = "ENQ"; }  else // Process: Scan for Published  Catalogs
    if(service.equals("2007")) { docType[0] = "4"; op[0] = "OPU"; }  else // Operation: Let Internal users see the Cost Price
    if(service.equals("2009")) { docType[0] = "4"; op[0] = "OPU"; }  else // Operation: Let Internal Users see the RRP (List) Price

    if(service.equals("6100")) { docType[0] = ""; op[0] = "ENQ"; }  else // Process: View Directory Contents
    if(service.equals("6101")) { docType[0] = ""; op[0] = "ENQ"; }  else // Process: List Directories
    if(service.equals("6102")) { docType[0] = ""; op[0] = "UPL"; }  else // Process: File Upload
    if(service.equals("6103")) { docType[0] = ""; op[0] = "OPU"; }  else // Process: Edit Text
    if(service.equals("6107")) { docType[0] = ""; op[0] = "OPU"; }  else // Process: Delete Text
    if(service.equals("6108")) { docType[0] = ""; op[0] = "OPU"; }  else // Process: Insert Directory
    if(service.equals("6109")) { docType[0] = ""; op[0] = "OPU"; }  else // Process: Delete Directory

    // Stock - Adjustment
    if(service.equals("167"))  { docType[0] = "5"; op[0] = "PGE"; }  else // Screen: Stock Adjustment Focus

    if(service.equals("3011")) { docType[0] = "5"; op[0] = "CHN"; }  else // Record: Stock Adjustment Edit for an Item
    if(service.equals("3012")) { docType[0] = "5"; op[0] = "CRT"; }  else // Record: Stock Adjustment Record Entry
    if(service.equals("3013")) { docType[0] = "5"; op[0] = "LST"; }  else // Record: List Adjustment Records

    // Stock - Check
    if(service.equals("166"))  { docType[0] = "6"; op[0] = "PGE"; }  else // Screen: Stock Check Focus

    if(service.equals("3018")) { docType[0] = "6"; op[0] = "LST"; }  else // Record: List Stock Check Records for an Item
    if(service.equals("3019")) { docType[0] = "6"; op[0] = "CRT"; }  else // Record: Stock Check Record Entry
    if(service.equals("3020")) { docType[0] = "6"; op[0] = "LST"; }  else // Record: List Stock Check Records
    if(service.equals("3021")) { docType[0] = "6"; op[0] = "PGE"; }  else // Record: Stock Check Import
    if(service.equals("3064")) { docType[0] = "6"; op[0] = "ENQ"; }  else // Record: List Incomplete Picking Lists and GRNs
    if(service.equals("3065")) { docType[0] = "6"; op[0] = "ENQ"; }  else // Process: Stock Check Reconciliation

    // Users
    if(service.equals("128"))  { docType[0] = ""; op[0] = "ENQ"; }  else // Process: Company Personnel Directory

    //
    if(service.equals("1025"))  { docType[0] = ""; op[0] = "ENQ"; }  else // Process: Document Trace (Cyclical)
    if(service.equals("1026"))  { docType[0] = ""; op[0] = "ENQ"; }  else // Process: Document Trace (Forward)
    if(service.equals("6094"))  { docType[0] = ""; op[0] = "UPD"; }  else // Process: Modify Document Attachments
    if(service.equals("11200")) { docType[0] = ""; op[0] = "CNV"; }  else // Document: Convert a Document to PDF
    if(service.equals("11300")) { docType[0] = ""; op[0] = "CNV"; }  else // Document: Convert a Document to CSV
    if(service.equals("11800")) { docType[0] = ""; op[0] = "ENQ"; }  else // Document: Display a Document Access Trail
    if(service.equals("11801")) { docType[0] = ""; op[0] = "ENQ"; }  else // Document: Display an Audit Trail
    if(service.equals("1008"))  { docType[0] = ""; op[0] = "ENQ"; }  else // Word Search Sales Document Lines
    if(service.equals("1030"))  { docType[0] = ""; op[0] = "ENQ"; }  else // Sales Document Lines Enquiry
    if(service.equals("6092"))  { docType[0] = ""; op[0] = "UPT"; }  else // Document: Change GST Rates for a Document
    if(service.equals("6093"))  { docType[0] = ""; op[0] = "UPT"; }  else // Document: Change Prices for a Document
    if(service.equals("6096"))  { docType[0] = ""; op[0] = "UPT"; }  else // Document: Change Stores for a Document
    if(service.equals("6098"))  { docType[0] = ""; op[0] = "UPT"; }  else // Document: Change Discounts for a Document
    if(service.equals("121"))   { docType[0] = ""; op[0] = "UPT"; }  else // Process: Process: Add to Cart
    if(service.equals("1018"))  { docType[0] = ""; op[0] = "OPU"; }       // Process: Remove Report

    // DataBase Admin
    if(service.equals("7052")) { docType[0] = ""; op[0] = "PGE"; }  else // DataBase Tables Manager

    if(service.equals("7048")) { docType[0] = ""; op[0] = "ENQ"; }  else // Process: DataBase Statistics
    if(service.equals("7076")) { docType[0] = ""; op[0] = "OPU"; }  else // Process: Clean Customer PO Codes
    if(service.equals("7077")) { docType[0] = ""; op[0] = "ENQ"; }  else // Process: Analyze Customer PO Codes
    if(service.equals("116"))  { docType[0] = ""; op[0] = "PGE"; }  else // Screen: DataBase Services
    if(service.equals("7033")) { docType[0] = ""; op[0] = "PGE"; }       // Screen: DataBase Locking
  }

  // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void forEachSOOnDO(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String doCode, String userCode) throws Exception
  {
    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT SOCode FROM dol WHERE DOCode = '" + doCode + "' AND SOCode != ''");

      String soCode, waveID;

      while(rs.next())
      {
        soCode = rs.getString(1);
        
        waveID = getWaveIDForDoc(con, stmt, rs, soCode, 'S');

        if(! alreadyOnWave(con, stmt2, rs2, doCode, "D", waveID))
          addToWave(con, stmt2, rs2, waveID, userCode, "D", doCode);
      }

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println("ServerUtils: " + e);
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
  }

  // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean alreadyOnWave(Connection con, Statement stmt, ResultSet rs, String docCode, String docType, String waveID) throws Exception
  {
    boolean res = false;
    
    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT WaveID FROM waveletc WHERE DocCode = '" + generalUtils.sanitiseForSQL(docCode) + "' AND DocType = '" + docType + "' AND WaveID = '" + waveID + "'");

      if(rs.next())
        res = true;

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println("ServerUtils: alreadyOnWave(): " + e);
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
    
    return res;
  }

  // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String getSOCodeGivenPLCode(Connection con, Statement stmt, ResultSet rs, String plCode) throws Exception
  {
    String soCode = "";

    try
    {
      stmt = con.createStatement();

      stmt.setMaxRows(1);

      rs = stmt.executeQuery("SELECT SOCode FROM pll WHERE PLCode = '" + plCode + "'");

      if(rs.next())
        soCode = rs.getString(1);

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println("ServerUtils: " + e);
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }

    return soCode;
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void createNewWavechannelsRec(Connection con, Statement stmt, ResultSet rs, String waveID, String custCode, String waveDated, String owner, String DNM) throws Exception
  {
    try
    {
      String channelID = custCode;

      if(! generalUtils.isInteger(channelID))
        channelID = "0";
      
      stmt = con.createStatement();

      stmt.executeUpdate("INSERT INTO wavechannels (WaveID, ChannelID, DNM, WaveDated, WaveType, ChannelType) VALUES ('" + waveID + "','" + channelID + "','" + DNM + "','" + waveDated + "','c','C')");
     }
    catch(Exception e)
    {
      System.out.println("ServerUtils: " + e);
    }

    if(stmt != null) stmt.close();
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void updateNewWavechannelsRec(Connection con, Statement stmt, ResultSet rs, String docCode, String docType, String custCode) throws Exception
  {
    try
    {
      String waveID = getWaveID(con, stmt, rs, docCode, docType);

      String channelID = getChannelID(con, stmt, rs, waveID);

      String newChannelID = custCode;

      if(! generalUtils.isInteger(newChannelID))
        newChannelID = "0";

      if(channelID.equals(newChannelID))
        return;
      
      stmt = con.createStatement();

      stmt.executeUpdate("UPDATE wavechannels SET ChannelID = '" + newChannelID + "' WHERE WaveID = '" + waveID + "' AND ChannelID = '" + channelID + "'");
     }
    catch(Exception e)
    {
      System.out.println("ServerUtils: " + e);
    }

    if(stmt != null) stmt.close();
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String getChannelID(Connection con, Statement stmt, ResultSet rs, String waveID) throws Exception
  {
    String channelID = "";

    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT ChannelID FROM wavechannels WHERE WaveID = '" + waveID + "'");

      if(rs.next())
        channelID = rs.getString(1);

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
      System.out.println("ServerUtils: getChannelID(): " + e);
      return "";
    }
    
    return channelID;
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String getWaveID(Connection con, Statement stmt, ResultSet rs, String docCode, String docType) throws Exception
  {
    String waveID = "";

    try
    {
      stmt = con.createStatement();

      stmt.setMaxRows(1);

      rs = stmt.executeQuery("SELECT WaveID FROM waveletc WHERE DocCode = '" + generalUtils.sanitiseForSQL(docCode) + "' AND DocType = '" + docType + "'");

      if(rs.next())
        waveID = rs.getString(1);

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
      System.out.println("ServerUtils: getWaveID(): " + e);
      return "";
    }
    
    return waveID;
  }
  
}
