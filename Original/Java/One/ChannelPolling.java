//====================================================================================================================================================================================================================================================
// System: ZaraStar: Utils: polling for channels
// Module: ChannelPolling.java
// Author: C.K.Harvey
// Copyright (c) 2006-08 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
//
//====================================================================================================================================================================================================================================================

package org.zarastar.zarastar;


import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.Enumeration;
import java.sql.*;

public class ChannelPolling extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  Chat chat = new Chat();
  ChannelOpenChats channelOpenChats = new ChannelOpenChats();
  ConnectionsUtils connectionsUtils = new ConnectionsUtils();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="";

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
        if(name.equals("p1")) // openChannels
          p1 = value[0];
      }

      doIt(req, res, unm, sid, uty, men, den, dnm, bnm, p1, bytesOut);
    }    
    catch(Exception e)
    {
      System.out.println(("Unexpected System Error: 12701: " + e));
      res.getWriter().write("Unexpected System Error: 12701");
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(HttpServletRequest req, HttpServletResponse res, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1, int[] bytesOut) throws Exception
  {
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
 
    String rtn="Unexpected System Error: 12701";
    
    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      rtn = "Access Denied - Duplicate SignOn or Session Timeout";
    }
    else
    {
      // get msgs from this server
      getMsgs(res, p1, unm, sid, uty, men, den, dnm, bnm);

    }
    
    serverUtils.totalBytes(req, unm, dnm, 12701, bytesOut[0], 0, "");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void getMsgs(HttpServletResponse res, String p1, String unm, String sid, String uty, String men, String den, String dnm, String bnm) throws Exception
  {
    Connection con = null;
    Statement stmt = null, stmt2 = null, stmt3 = null;
    ResultSet rs   = null, rs2 = null, rs3 = null;
    
    String uName = directoryUtils.getMySQLUserName();
    String pWord = directoryUtils.getMySQLPassWord();

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    
    con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + uName + "&password=" + pWord);

    String[] rtnStr          = new String[1];  rtnStr[0] = "";
    String[] fromUsersRtnStr = new String[1];  fromUsersRtnStr[0] = "";
    String[] newStr          = new String[1];  newStr[0] = "";
    String[] links           = new String[1];  links[0] = "";
    String[] openChannels    = new String[1];  openChannels[0] = p1;

    fetchLocalMessages(con, stmt, stmt2, stmt3, rs, rs2, rs3, unm, sid, uty, men, den, dnm, bnm, openChannels, rtnStr, fromUsersRtnStr, newStr, links);

    if(con != null) con.close();
    
    if(fromUsersRtnStr[0].length() == 0)
      fromUsersRtnStr[0] = ".";
    
    if(rtnStr[0].length() == 0)
      rtnStr[0] = ".";
      
    String s = "<msg><res>.</res><toUsers><![CDATA[" + fromUsersRtnStr[0] + "]]></toUsers><stuff><![CDATA[" + rtnStr[0] + "]]></stuff>"
             + "<newStuff><![CDATA[" + newStr[0] + "]]></newStuff><open><![CDATA[" + openChannels[0] + "]]></open><links><![CDATA[" + links[0]
             + "]]></links></msg>";

    res.getWriter().write(s);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void fetchLocalMessages(Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, ResultSet rs3, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                                  String[] openChannels, String[] rtnStr, String[] fromUsersRtnStr, String[] newStr, String[] links) throws Exception
  {
    String msgIDs = "", msgID;      

    try
    {
      stmt = con.createStatement();
  
      rs = stmt.executeQuery("SELECT DISTINCT FromUser FROM chat WHERE ToUser = '" + unm + "' AND Status = 'U'");
      String fromUser;
      
      while(rs.next())                  
      {
        fromUser = rs.getString(1);
        
        stmt2 = con.createStatement();
  
        rs2 = stmt2.executeQuery("SELECT MsgID, DateTime, Msg FROM chat WHERE FromUser = '" + fromUser + "' AND ToUser = '" + unm + "' AND Status = 'U' ORDER BY MsgID");
      
        while(rs2.next())
        { 
          msgID = rs2.getString(1);
      
          rtnStr[0] += ("<b>" + fromUser + " (" + generalUtils.timeFromTimestamp(rs2.getString(2)) + "): " + connectionsUtils.convertLinks(rs2.getString(3), unm, sid, uty, men, den, dnm, bnm) + "</b><br>&#1;");

          fromUsersRtnStr[0] += (fromUser + ":");
          
          updateAsNotMarkedAsRead(con, stmt2, msgID); // so it appears in openChannels until closed
          
          // create new window if the message is for a channel window that does not exist on the screen

          if(! isOnList(openChannels[0], fromUser,        unm))
          {
            newStr[0] += buildNewStuff(con, stmt3, rs3, fromUser, "P", unm, sid, uty, men, den, dnm, bnm);
            openChannels[0] += (fromUser + "&#1;");

            chat.updateOpenChannels(con, stmt3, false, unm, (fromUser + "&#1;"), "P", "");
          }
          
          msgIDs += (msgID + "\001");
        }
                 
        if(rs2   != null) rs2.close();                                 
        if(stmt2 != null) stmt2.close();
      }
                 
      if(rs   != null) rs.close();                                 
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println("12701: " + e);
      if(rs    != null) rs.close();
      if(stmt  != null) stmt.close();
      if(rs2   != null) rs2.close();                                 
      if(stmt2 != null) stmt2.close();
    }    

    // get msgs for broadcast group
    
    try
    {
      String name = "___Company___";
      
      msgIDs = "";
      
      stmt2 = con.createStatement();
  
      rs2 = stmt2.executeQuery("SELECT MsgID, DateTime, Msg, FromUser FROM chat WHERE ToGroup = '" + name + "' AND Status != 'R' ORDER BY MsgID");
      // Status != 'R' above means at least one user still not read
      
      while(rs2.next())
      { 
        msgID = rs2.getString(1);

        if(msgStillUnRead(con, stmt3, rs3, msgID, unm))
        {
          rtnStr[0] += ("<b>" + rs2.getString(4) + " (" + generalUtils.timeFromTimestamp(rs2.getString(2)) + "): " + connectionsUtils.convertLinks(rs2.getString(3), unm, sid, uty, men, den, dnm, bnm) + "</b><br>&#1;");
 
          fromUsersRtnStr[0] += (name + ":");
          
          updateAsReadOnce(con, stmt2, msgID, unm);
          
          // create new window if the message is for a channel window that does not exist on the screen
          if(! isOnList(openChannels[0], name,        unm))
          { 
            newStr[0] += buildNewStuff(con, stmt3, rs3, name, "G", unm, sid, uty, men, den, dnm, bnm);
            openChannels[0] += (name + "&#1;");
            chat.updateOpenChannels(con, stmt3, false, unm, (name + "&#1;"), "G", "");
          }
           
          msgIDs += (msgID + "\001");
        }
      }
   
      if(rs2   != null) rs2.close();                                 
      if(stmt2 != null) stmt2.close();
    }
    catch(Exception e)
    {
      if(rs2   != null) rs2.close();                                 
      if(stmt2 != null) stmt2.close();
    }
    
    // get msgs for other groups
    
    try
    {
      stmt = con.createStatement();
    
      rs = stmt.executeQuery("SELECT Name, Type, Host FROM channelsmine WHERE UserCode = '" + unm + "'");

      String name, type, host;
      
      msgIDs = "";
      
      while(rs.next())                  
      {
        name = rs.getString(1);
        type = rs.getString(2);
        host = rs.getString(3);
        
        if(host == null)
          host = "";

        if(type.equals("G")) // group
        {
          if(host.length() > 0 && ! host.equalsIgnoreCase("local"))
            ;
          else
          {            
            stmt2 = con.createStatement();
  
            rs2 = stmt2.executeQuery("SELECT MsgID, DateTime, Msg, FromUser FROM chat WHERE ToGroup = '" + name + "' AND Status = 'U' ORDER BY MsgID");
      
            while(rs2.next())
            { 
              msgID = rs2.getString(1);
      
              if(msgStillUnRead(con, stmt3, rs3, msgID, unm))
              {
                rtnStr[0] += ("<b>" + rs2.getString(4) + " (" + generalUtils.timeFromTimestamp(rs2.getString(2)) + "): " + connectionsUtils.convertLinks(rs2.getString(3), unm, sid, uty, men, den, dnm, bnm) + "</b><br>&#1;");

                fromUsersRtnStr[0] += (name + ":");
          
                updateAsReadOnce(con, stmt2, msgID, unm);
          
                // create new window if the message is for a channel window that does not exist on the screen
                if(! isOnList(openChannels[0], name,        unm))
                {
                  newStr[0] += buildNewStuff(con, stmt3, rs3, name, "G", unm, sid, uty, men, den, dnm, bnm);
                  openChannels[0] += (name + "&#1;");
                  chat.updateOpenChannels(con, stmt3, false, unm, (name + "&#1;"), "G", host);
                }
          
                msgIDs += (msgID + "\001");
              }
            }
                 
            if(rs2   != null) rs2.close();                                 
            if(stmt2 != null) stmt2.close(); 
          }
        }
      }
      
      if(rs   != null) rs.close();                                 
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
      if(rs2   != null) rs2.close();                                 
      if(stmt2 != null) stmt2.close();
    }
  }  

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String buildNewStuff(Connection con, Statement stmt, ResultSet rs, String fromUser, String channelType, String unm, String sid, String uty, String men, String den, String dnm, String bnm) throws Exception
  {
    String[] rtnStr = new String[1];  rtnStr[0] = "";
    channelOpenChats.createWindow1(fromUser, rtnStr, "99");
    channelOpenChats.createWindow3(con, stmt, rs, fromUser, channelType, "newBit", "", unm, sid, uty, men, den, dnm, bnm, rtnStr); 
    
    return rtnStr[0];
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean isOnList(String openChannels, String user, String unm) throws Exception
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
  private boolean msgStillUnRead(Connection con, Statement stmt, ResultSet rs, String msgID, String unm) throws Exception
  {
    int rowCount = 0;
    
    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM chatunread WHERE MsgID = '" + msgID + "' AND ToUser = '" + unm + "' AND ReadOnce='N'");
      
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
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void updateAsReadOnce(Connection con, Statement stmt, String msgID, String unm) throws Exception
  {
    try
    {
      stmt = con.createStatement();
  
      stmt.executeUpdate("UPDATE chatunread SET ReadOnce = 'Y' WHERE MsgID = '" + msgID + "' AND ToUser = '" + unm + "' ");
                 
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println("12701: " + e);
      if(stmt != null) stmt.close();
    }    
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void updateAsNotMarkedAsRead(Connection con, Statement stmt, String msgID) throws Exception
  {
    try
    {
      stmt = con.createStatement();
  
      stmt.executeUpdate("UPDATE chat SET Status = 'N' WHERE MsgID = '" + msgID + "'");
                 
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println("12701: " + e);
      if(stmt != null) stmt.close();
    }    
  }

}
