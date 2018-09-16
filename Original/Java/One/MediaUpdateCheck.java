// =======================================================================================================================================================================================================
// System: ZaraStar Media: Update Check
// Source: MediaUpdateCheck.java
// Author: C.K.Harvey
// Copyright (c) 2004-09 Christopher Harvey. All Rights Reserved.
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

public class MediaUpdateCheck extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  ServerUtils serverUtils = new ServerUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    OutputStream out = null;
    res.setHeader("Header", "HTTP/1.0 200 OK");
    res.setStatus(200);
  
    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="";

    try
    {
      unm = req.getParameter("unm");
      dnm = req.getParameter("dnm");
      uty = req.getParameter("uty");
      sid = req.getParameter("sid");
      men = req.getParameter("men");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");
      p1  = req.getParameter("p1");
      p2  = req.getParameter("p2"); // callingServlet
      
      if(p1 == null) p1 = "";
      if(p2 == null) p2 = "";

      doIt(out, req, res, p1, p2, unm, sid, uty, dnm, men, den, bnm, bytesOut);
    }
    catch(Exception e)
    {
      res.getWriter().write(("err:12600 " + e));
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(OutputStream out, HttpServletRequest req, HttpServletResponse res, String p1, String p2, String unm, String sid, String uty, String dnm, String men, String den, String bnm, int[] bytesOut) throws Exception
  {
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String sessionsDir   = directoryUtils.getSessionsDir(dnm);

    long timeNow = ((generalUtils.todayEncoded(localDefnsDir, defnsDir) - 1) * 86400L) + generalUtils.timeNowInSecs();
    String newTimeStamp = generalUtils.longToStr(timeNow);

    RandomAccessFile fh = generalUtils.fileOpen(sessionsDir + unm + ".hb");
    fh.seek(0);

    fh.write(newTimeStamp.getBytes());
    generalUtils.fileClose(fh);

    long assumedSignedOut = generalUtils.strToLong(generalUtils.getFromDefnFile("ASSUMESIGNEDOUT", "timeouts.dfn", localDefnsDir, defnsDir));
    
    File path2 = new File(sessionsDir);
    String fs2[] = new String[0];
 
    int y, z;
    byte[] lastTransactionTimeStamp = new byte[50];
    byte[] lastHeartBeatTimeStamp   = new byte[50];

    Connection con = null;
    Statement stmt = null;
    ResultSet rs   = null;
    
    try
    {
      String uName = directoryUtils.getMySQLUserName();
      String pWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();
    
      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + uName + "&password=" + pWord);

      stmt = con.createStatement();
  
      String userCode, status;
      boolean sidFound;

      if(uty.equals("I"))
      {  
        rs = stmt.executeQuery("SELECT UserCode, Status FROM profiles ORDER BY UserName"); 
      
        while(rs.next())                  
        {
          userCode        = rs.getString(1);
          status          = rs.getString(2);
          if(status.equals("L")) // live
          {
            {
              sidFound = false;
              lastTransactionTimeStamp[0] = lastHeartBeatTimeStamp[0] = '\000';
              fs2 = path2.list();
              for(y=0;y<fs2.length;++y)
              {
                if(fs2[y].startsWith(userCode + ".") && fs2[y].endsWith(".sid"))
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

              if(lastHeartBeatTimeStamp[0] != '\000')
              {
                if((timeNow - generalUtils.longFromBytesCharFormat(lastHeartBeatTimeStamp, (short)0)) > assumedSignedOut)
                {
                  serverUtils.removeSID(userCode, sessionsDir);
                  
                  serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 12600, bytesOut[0], 0, 0, "OUT:" + userCode + ":" + decodeTime(timeNow,localDefnsDir,defnsDir));
                }                      
              }
            }
          }
        }
      }
                 
      if(rs   != null) rs.close();                                 
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println("12600 (3) " + e);
      if(rs   != null) rs.close();                                 
      if(stmt != null) stmt.close();
    }    

    res.setContentType("text/xml");
    res.setHeader("Cache-Control", "no-cache");

    boolean set = false;
    try
    {
      int callingServlet;
      try
      {
        callingServlet = generalUtils.serviceToInt(p2);
      }
      catch(Exception e)
      {
        callingServlet = 0;
      }
      
      if(callingServlet != 12700)
        set = unprocessedChannelTransactions(con, stmt, rs, unm);
    }
    catch(Exception e)
    {
      System.out.println("12600 (1) " + e);
    }    

    String[] menuitemColor           = new String[1];
    String[] menuitemBackgroundColor = new String[1];

    try
    {
      getColours(con, stmt, rs, set, unm, dnm, menuitemColor, menuitemBackgroundColor);
    }
    catch(Exception e)
    {
      System.out.println("12600 (2) " + e);
      if(rs   != null) rs.close();                                 
      if(stmt != null) stmt.close();
    }    
   
    try
    {
    String s = "<msg><c>" + menuitemColor[0] + "</c><b>" + menuitemBackgroundColor[0] + "</b></msg>";

    res.getWriter().write(s);

    if(con  != null) con.close();
    }
    catch(Exception e)
    {
      System.out.println("12600 (3) " + e);
      if(con  != null) con.close();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String decodeTime(long time, String localDefnsDir, String defnsDir) throws Exception
  {
    String s = generalUtils.decode((int)((time / 86400) +         1)   , localDefnsDir, defnsDir);

    int secs = (int)(time - ((time / 86400) * 86400));
    
    s += (" " + generalUtils.convertSecsToHoursMinsAndSecs(secs)); 
  
    return s;
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean unprocessedChannelTransactions(Connection con, Statement stmt, ResultSet rs, String unm) throws Exception
  {
    int rowCount = 0;
    
    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM chat WHERE ToUser = '" + unm + "' AND Status = 'U'");
      
      if(rs.next())
        rowCount = rs.getInt("rowcount");
    }
    catch(Exception e) { System.out.println("12600" + e); }    
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    if(rowCount != 0)
      return true;

    // no personal msgs... may be a group msg exists
    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM chatunread WHERE ToUser = '" + unm + "'");
      
      if(rs.next())
        rowCount = rs.getInt("rowcount");
    }
    catch(Exception e) { System.out.println("12600 " + e); }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    if(rowCount != 0)
      return true;

    return false;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void getColours(Connection con, Statement stmt, ResultSet rs, boolean set, String unm, String dnm, String[] menuitemColor, String[] menuitemBackgroundColor) throws Exception
  {      
menuitemColor[0]           = "black";
menuitemBackgroundColor[0] = "transparent";    
if(true)return;

    String currentStyle = directoryUtils.getCurrentStyleForAUser(con, stmt, rs, unm, dnm);

    String fileName = "/Zara/Support/Css/Default/";

    if(currentStyle.length() > 0)
    {
      if(currentStyle.startsWith("Zara"))
        fileName = "/Zara/Support/Css/" + currentStyle + "/";

      if(styleExists(currentStyle, dnm)) // in case style has been deleted after user selected it
        fileName = "/Zara/" + dnm + "/Css/" + generalUtils.capitalize(currentStyle) + "/";
    }

    fileName += "general.css";

    menuitemColor[0] = menuitemBackgroundColor[0] = "";

    RandomAccessFile fh = generalUtils.fileOpen(fileName);

    if(fh != null)
    {      
      if(set)
      {        
        String highlightmenuitemLine = getLine(fh, "#highlightmenuitem");

        menuitemColor[0]           = getEntry(highlightmenuitemLine, "color:",            false);
        menuitemBackgroundColor[0] = getEntry(highlightmenuitemLine, "background-color:", false);
      }

      generalUtils.fileClose(fh);  
    }
    else // just-in-case
    {
      if(set)
      {        
        menuitemColor[0]           = "white";
        menuitemBackgroundColor[0] = "red";    
      }
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean styleExists(String reqdStyle, String dnm) throws Exception
  {
    String dir = "/Zara/" + dnm + "/Css/";
    File path = new File(dir);
    String fs[] = new String[0];
    fs = path.list();

    for(int x=0;x<fs.length;++x)
    {
      if(generalUtils.isDirectory(dir + fs[x])) // just-in-case
      {
        if(reqdStyle.equals(fs[x]))
          return true;
      }
    }
    
    return false;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  // #copyright { margin-top: 20px; font-size: 80%; color: darkblue; }
  private String getLine(RandomAccessFile fh, String entryToFind) throws Exception
  {
    fh.seek(0);

    int x, len, entryToFindLength = entryToFind.length();
    String s, t;

    try
    {
      while(true)
      {
        s = fh.readLine();
              
        len = s.length();
    
        x=0;
        while(x < len && s.charAt(x) == ' ') // just-in-case
          ++x;

        t = "";
        while(x < len && x < entryToFindLength) // just-in-case
          t += s.charAt(x++);

        if(t.equals(entryToFind))
          return s;
      }
    }
    catch(Exception e) { }
    
    return "";
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  // #copyright { margin-top: 20px; font-size: 8pt; color: darkblue; }
  private String getEntry(String entry, String elementToFind, boolean stripTrailing) throws Exception
  {
    int len = entry.length();
    if(len== 0)
      return "";

    int i;
    
    if((i = entry.indexOf(elementToFind)) == -1)
      return "";

    int x = i + elementToFind.length();
    while(x < len && entry.charAt(x) == ' ') // just-in-case
      ++x;

    String t = "";
    while(x < len && entry.charAt(x) != ';' && entry.charAt(x) != '}') // just-in-case
      t += entry.charAt(x++);

    if(stripTrailing)
    {
      x = t.length() - 1;
      while(x >= 0 && t.charAt(x) != ' ' && (t.charAt(x) < '0' || t.charAt(x) > '9')) // just-in-case
        --x;
      
      while(x >= 0 && entry.charAt(x) == ' ') // just-in-case
        --x;
      
      t = t.substring(0, x + 1);
    }
    
    return t;
  }

}

