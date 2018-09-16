// =======================================================================================================================================================================================================
// System: ZaraStar: Chat: get open chats
// Module: ChannelOpenChats.java
// Author: C.K.Harvey
// Copyright (c) 2006-09 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.sql.*;
import java.util.Enumeration;

public class ChannelOpenChats extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  Chat chat = new Chat();
  ConnectionsUtils connectionsUtils = new ConnectionsUtils();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", newChannel="", newChannelType="";

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
        else
        if(name.equals("p1")) // has value if call requesting a new channel
          newChannel = value[0];
        else
        if(name.equals("p2")) // has value if call requesting a new channel
          newChannelType = value[0];
      }

      if(newChannel     == null) newChannel = "";
      if(newChannelType == null) newChannelType = "";

      doIt(req, res, unm, sid, uty, men, den, dnm, bnm, newChannel, newChannelType, bytesOut);
    }    
    catch(Exception e)
    {
      System.out.println(("Unexpected System Error: 12703: " + e));
      res.getWriter().write("Unexpected System Error: 12703");
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(HttpServletRequest req, HttpServletResponse res, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String newChannel, String newChannelType, int[] bytesOut) throws Exception
  {
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
 
    String rtn="Unexpected System Error: 12703";
    
    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      rtn = "Access Denied - Duplicate SignOn or Session Timeout";
    }
    else
    {
      get(res, newChannel, newChannelType, unm, sid, uty, men, den, dnm, bnm, bytesOut);
    }
    
    serverUtils.totalBytes(req, unm, dnm, 12703, bytesOut[0], 0, "");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void get(HttpServletResponse res, String newChannel, String newChannelType, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    String[] rtnStr = new String[1];  rtnStr[0] = "";

    Connection con = null;
    Statement stmt = null, stmt2 = null, stmt3 = null;
    ResultSet rs   = null, rs2   = null, rs3   = null;
    
    String uName = directoryUtils.getMySQLUserName();
    String pWord = directoryUtils.getMySQLPassWord();

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    
    con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + uName + "&password=" + pWord);

    String openChannels = "";
    String[] openChannelsTypes = new String[1];  openChannelsTypes[0] = "";
    
    try
    {
      if(newChannel.length() > 0) // requesting a new channel
      {
        createWindow1(newChannel, rtnStr, "1");
        createWindow3(con, stmt, rs, newChannel, newChannelType, "all", "", unm, sid, uty, men, den, dnm, bnm, rtnStr);
        openChannels += (newChannel + "&#1;");
        openChannelsTypes[0] += newChannelType;
      }
      
      openChannels = createWindowsForChannels(con, stmt, stmt2, stmt3, rs, rs2, rs3, unm, sid, uty, men, den, dnm, bnm, openChannels, rtnStr, openChannelsTypes);

    }
    catch(Exception e)
    {
      System.out.println("12703: " + e);
      if(rs    != null) rs.close();
      if(stmt  != null) stmt.close();
      if(rs2   != null) rs2.close();                                 
      if(stmt2 != null) stmt2.close();
    }    

    openChannels = createWindowsForChannelsWithNoUnreadMessages(con, stmt, stmt2, rs, rs2, unm, sid, uty, men, den, dnm, bnm, openChannels, openChannelsTypes, rtnStr);
    
    chat.updateOpenChannels(con, stmt, true, unm, openChannels, openChannelsTypes[0], "");
    
    if(con != null) con.close();
    
    String s = "<msg><res>.</res><stuff><![CDATA[" + rtnStr[0] + "]]></stuff><open><![CDATA[" + openChannels + "]]></open></msg>";

    res.getWriter().write(s);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String createWindowsForChannels(Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, ResultSet rs3, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                                          String openChannels, String[] rtnStr, String[] openChannelsTypes) throws Exception
  {   
    try
    {
      stmt = con.createStatement();
    
      rs = stmt.executeQuery("SELECT Name, Type, Host FROM channelsopen WHERE UserCode = '" + unm + "'");
    
      String name, type, fromUser, msgIDs = "", msgID, s;
      
      while(rs.next())
      {
        name = rs.getString(1);
        type = rs.getString(2);
        
        if(! isOnList(openChannels, name))
        {
          createWindow1(name, rtnStr, "2");
          
          stmt2 = con.createStatement();
  
          if(type.equals("P")) // personal
          {
            ///////////stmt2.setMaxRows(100);
            rs2 = stmt2.executeQuery("SELECT MsgID, FromUser, DateTime, Msg FROM chat WHERE Status != 'R' AND ((ToUser = '" + name + "' AND FromUser = '" + unm + "') OR (ToUser = '" + unm + "' AND FromUser = '" + name + "')) ORDER BY MsgID");
          
            msgIDs = "";
            while(rs2.next())
            {
              fromUser = rs2.getString(2);
              if(! fromUser.equals(unm))
              {
                createWindow2(fromUser, rs2.getString(3), rs2.getString(4), rtnStr, unm, sid, uty, men, den, dnm, bnm);

                if(! isOnList(openChannels, fromUser))
                {
                  openChannels += (fromUser + "&#1;");
                  openChannelsTypes[0] += type;
                }
           
                msgIDs += (rs2.getString(1) + "\001");
              }
              else
              {
                createWindow2(fromUser, rs2.getString(3), rs2.getString(4), rtnStr, unm, sid, uty, men, den, dnm, bnm);
                msgIDs += (rs2.getString(1) + "\001");                  
              }
            }
          }
          else // group
          {
            rs2 = stmt2.executeQuery("SELECT MsgID, FromUser, DateTime, Msg FROM chat WHERE ToGroup = '" + name + "' ORDER BY MsgID DESC");
          
            msgIDs = "";
            s = "";
            while(rs2.next())
            {
              msgID = rs2.getString(1);
              if(msgStillUnRead(con, stmt3, rs3, msgID, unm))
              {
                s = prepWindow2(rs2.getString(2), rs2.getString(3), rs2.getString(4), unm, sid, uty, men, den, dnm, bnm) + s;

                msgIDs += (msgID + "\001");
              
                openChannels += (name + "&#1;");
                openChannelsTypes[0] += type;

                if(s.length() > 0)
                  rtnStr[0] += s;
              }
            }
          }
                 
          if(rs2   != null) rs2.close();                                 
          if(stmt2 != null) stmt2.close();

          createWindow3(con, stmt2, rs2, name, type, "all", msgIDs, unm, sid, uty, men, den, dnm, bnm, rtnStr);
        }
      }
      
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println("12703: " + e);
      if(rs    != null) rs.close();
      if(stmt  != null) stmt.close();
      if(rs2   != null) rs2.close();                                 
      if(stmt2 != null) stmt2.close();
    }
    
    return openChannels;
  }  

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String createWindowsForChannelsWithNoUnreadMessages(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String openChannels,
                                                              String[] openChannelsTypes, String[] rtnStr) throws Exception
  {   
    try
    {
      stmt = con.createStatement();
    
      rs = stmt.executeQuery("SELECT Name FROM channelsopen WHERE UserCode = '" + unm + "' AND Type != 'G'");
    
      String name;
      
      while(rs.next())
      {
        name = rs.getString(1);

        if(! isOnList(openChannels, name))
        {
          createWindow1(name, rtnStr, "3");
          createWindow3(con, stmt2, rs2, name, "P", "all", "", unm, sid, uty, men, den, dnm, bnm, rtnStr);
          openChannels += (name + "&#1;");
          openChannelsTypes[0] += "P";
        }
      }
      
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
    
    return openChannels;
  }  

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean isOnList(String openChannels, String user) throws Exception
  {
    String entry;
    
    int i = openChannels.indexOf("&#1;");
    while(i != -1)
    {
      entry = openChannels.substring(0, i);
      if(entry.equals(user))
        return true;
        
      openChannels = openChannels.substring(i+4);
      i = openChannels.indexOf("&#1;");
    }
          
    return false;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void createWindow1(String thisToUser, String[] rtnStr, String xx) throws Exception
  {
    String s;
    if(thisToUser.startsWith("__"))
      s = "Company Broadcast";
    else
    if(thisToUser.startsWith("_"))
    {
      int len = thisToUser.length();
      s = "Casual-" + thisToUser.substring(len - 4, len);
    }
    else s = thisToUser;

    rtnStr[0] += "<div id='div" + thisToUser + "'><table border=1 width=100% id='channelHeader'><tr><td colspan=2>Channel to " + s + "</td></tr>";
    rtnStr[0] += "<tr id='channel'><td width=99%><div id='prev" + thisToUser + "'>";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String prepWindow2(String fromUser, String timeStamp, String msg, String unm, String sid, String uty, String men, String den, String dnm, String bnm) throws Exception
  {
    String s;
    if(fromUser.startsWith("__"))
       s = "Company Broadcast";
    else
    if(fromUser.startsWith("_"))
    {
      int len = fromUser.length();
      s = "Casual-" + fromUser.substring(len - 4, len);
    }
    else s = fromUser;

    return s + " (" + generalUtils.timeFromTimestamp(timeStamp) + "): " + connectionsUtils.convertLinks(msg, unm, sid, uty, men, den, dnm, bnm) + "<br>";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void createWindow2(String fromUser, String timeStamp, String msg, String[] rtnStr, String unm, String sid, String uty, String men, String den, String dnm, String bnm) throws Exception
  {
    String s;
    if(fromUser.startsWith("__"))
      s = "Company Broadcast";
    else
    if(fromUser.startsWith("_"))
    {
      int len = fromUser.length();
      s = "Casual-" + fromUser.substring(len - 4, len);
    }
    else s = fromUser;

    rtnStr[0] += (s + " (" + generalUtils.timeFromTimestamp(timeStamp) + "): " + connectionsUtils.convertLinks(msg, unm, sid, uty, men, den, dnm, bnm) + "<br>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void createWindow3(Connection con, Statement stmt, ResultSet rs, String thisToUser, String thisType, String allOrNewBit, String msgIDs, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String[] rtnStr)
                            throws Exception
  {
    rtnStr[0] += "</div></td><td valign=top id='channelButtons'><p><a href=\"javascript:close('" + thisToUser + "','" + allOrNewBit + "','div" + thisToUser + "','" + thisType + "')\">Close</a><br>";

    rtnStr[0] += "<a href=\"javascript:history('" + thisToUser + "','" + thisType + "')\">History</a><br>";

    rtnStr[0] += "<a href=\"javascript:prune('" + thisToUser + "','" + thisType + "')\">Prune</a></td></tr>";

    rtnStr[0] += "<tr id='channelMessage'><td><textarea id='in" + thisToUser + "' name='in" + thisToUser + "' style=\"width:100%;height:40px;\"}></textarea></td>";

    rtnStr[0] += "<td id='channelButtons'><p><a href=\"javascript:send('" + thisToUser + "','" + thisType + "')\">Send</a></td></tr></table></div>";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean msgStillUnRead(Connection con, Statement stmt, ResultSet rs, String msgID, String unm) throws Exception
  {
    int rowCount = 0;
    
    try
    {
      stmt = con.createStatement();
  
      rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM chatunread WHERE MsgID = '" + msgID + "' AND ToUser = '" + unm + "'");
      
      if(rs.next())
        rowCount = rs.getInt("rowcount");
                 
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println("12703: " + e);
      if(stmt != null) stmt.close();
    }
    
    if(rowCount == 0)
      return false; // read
    
    return true; // still unread
  }

}
