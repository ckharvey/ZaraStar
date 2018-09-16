//====================================================================================================================================================================================================================================================
// System: ZaraStar: Utils: send msg to channel
// Module: ChannelSendMessage.java
// Author: C.K.Harvey
// Copyright (c) 2006-09 Christopher Harvey. All Rights Reserved.
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
import java.net.*;
 
public class ChannelSendMessage extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  Inbox inbox = new Inbox();
  ConnectionsUtils connectionsUtils = new ConnectionsUtils();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", msg="", channelType="", toUser="";

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
        if(name.equals("p1"))
          msg = value[0];
        else
        if(name.equals("p2"))
          channelType = value[0];
        else
        if(name.equals("p3"))
          toUser = value[0];
      }
     
      doIt(req, res, msg, channelType, toUser, unm, sid, uty, men, den, dnm, bnm, bytesOut);
    }    
    catch(Exception e)
    {
      System.out.println(("Unexpected System Error: 12702: " + e));
      res.getWriter().write("Unexpected System Error: 12702");
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(HttpServletRequest req, HttpServletResponse res, String msg, String channelType, String toUser, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
 
    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      ;
    }
    else
    {
      res.getWriter().write(update(msg, channelType, toUser, unm, sid, uty, dnm, men, den, bnm, localDefnsDir));
    }
    
    serverUtils.totalBytes(req, unm, dnm, 12702, bytesOut[0], 0, "");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String update(String msg, String channelType, String toUser, String unm, String sid, String uty, String dnm, String men, String den, String bnm, String localDefnsDir) throws Exception
  {
    String rtnStr = "";

    Connection con = null;
    Statement stmt = null, stmt2 = null;
    ResultSet rs = null;
    
    String uName = directoryUtils.getMySQLUserName();
    String pWord = directoryUtils.getMySQLPassWord();

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    
    con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + uName + "&password=" + pWord);

    try
    {
      stmt = con.createStatement();

      if(channelType.equals("P"))
      {
        stmt.executeUpdate("INSERT INTO chat (ToGroup, FromUser, ToUser, Msg, Status) VALUES ('','" + unm + "','" + toUser + "','" + generalUtils.sanitiseForSQL(msg) + "','U' )");
        
        // could add a check here to see if recepient is local... somehow

        String[] host      = new String[1];
        String[] targetDNM = new String[1];
        
        getInfoFromCloud(toUser, host, targetDNM, unm, dnm, localDefnsDir);

        if(! targetDNM[0].equals(dnm))
          sendMessageToTargetServer(host[0], targetDNM[0], toUser, unm, msg, channelType);
      }
      else
      {
        stmt.executeUpdate("INSERT INTO chat (ToGroup, FromUser, ToUser, Msg, Status) VALUES ('" + toUser + "','" + unm + "','','" + generalUtils.sanitiseForSQL(msg) + "','U' )");
        if(stmt != null) stmt.close();

        stmt = con.createStatement();
        stmt.setMaxRows(1);
        rs = stmt.executeQuery("SELECT MsgID from chat WHERE ToGroup = '" + toUser + "' AND FromUser = '" + unm + "' ORDER BY MsgID DESC");
        String msgID = "";
        if(rs.next()) // just-in-case
          msgID = rs.getString(1);
        if(rs   != null) rs.close();
        if(stmt != null) stmt.close();

        if(msgID.length() > 0)
        {
          if(toUser.equals("___Company___"))
            insertIntoChatUnReadForAllUsersInTheCompanyGroup(con, stmt, stmt2, rs, msgID);
          else
          {
            insertIntoChatUnReadForAllUsersInAGroup(con, stmt, stmt2, rs, msgID, toUser);
          }
        }
      }
                 
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println("12702: " + e);
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }    

    if(con != null) con.close();

    rtnStr = "Me: " + connectionsUtils.convertLinks(msg, unm, sid, uty, men, den, dnm, bnm) + "<br>"; // must start "Me:" (for 12708)

    return "<msg><res>.</res><toUser>" + toUser + "</toUser><stuff><![CDATA[" + rtnStr + "]]></stuff></msg>";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void insertIntoChatUnReadForAllUsersInAGroup(Connection con, Statement stmt, Statement stmt2, ResultSet rs, String msgID, String group) throws Exception
  {
    try
    {
      stmt = con.createStatement();
  
      rs = stmt.executeQuery("SELECT User FROM channelusers WHERE Name = '" + generalUtils.sanitiseForSQL(group) + "' AND Status = 'A'");

      while(rs.next())                  
        insertIntoChatUnReadForAUser(con, stmt2, msgID, rs.getString(1), group);
                 
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();        
    }
    catch(Exception e)
    {
      System.out.println("12702: " + e);
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
  }        

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void insertIntoChatUnReadForAllUsersInTheCompanyGroup(Connection con, Statement stmt, Statement stmt2, ResultSet rs, String msgID) throws Exception
  {   
    try
    {
      stmt = con.createStatement();
  
      rs = stmt.executeQuery("SELECT UserCode FROM profiles WHERE Status = 'L' AND UserCode != '___casual___' AND UserCode != '___registered___'");

      while(rs.next())                  
        insertIntoChatUnReadForAUser(con, stmt2, msgID, rs.getString(1), "___Company___");
            
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();        
    }
    catch(Exception e)
    {
      System.out.println("12702: " + e);
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void insertIntoChatUnReadForAUser(Connection con, Statement stmt, String msgID, String userCode, String group) throws Exception
  {   
    try
    {
      stmt = con.createStatement();

      stmt.executeUpdate("INSERT INTO chatunread (MsgID, ToUser, ToGroup, ReadOnce) VALUES ('" + msgID + "','" + userCode + "','" + generalUtils.sanitiseForSQL(group) + "','N')");
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println("12702: " + e);
      if(stmt != null) stmt.close();
    }
  }  

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void getInfoFromCloud(String zpn, String[] host, String[] targetDNM, String unm, String dnm, String localDefnsDir)
  {
    try
    {
      URL url = new URL("http://" + serverUtils.serverToCall("ZC", localDefnsDir) + "/central/servlet/_9080a?unm=" + unm + "&dnm=" + dnm + "&zpn=" + zpn);
                      
      URLConnection uc = url.openConnection();
      uc.setDoInput(true);
      uc.setUseCaches(false);
      uc.setDefaultUseCaches(false);

      uc.setRequestProperty("Content-Type", "application/octet-stream");

      BufferedReader di = new BufferedReader(new InputStreamReader(uc.getInputStream()));

      String s = di.readLine();

      if(s != null)
      {
        host[0] = "";
        int x = 0, len = s.length();
        while(x < len && s.charAt(x) != '\001') // just-in-case
          host[0] += s.charAt(x++);

        ++x;
        targetDNM[0] = "";
        while(x < len && s.charAt(x) != '\001') // just-in-case
          targetDNM[0] += s.charAt(x++);
      }
      
      di.close();
    }
    catch(Exception e) { }
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean sendMessageToTargetServer(String host, String targetDNM, String toUser, String fromUser, String msg, String channelType)
  {
    String res = "";
    
    try
    {
      if(! host.equals("http://"))
        host = "http://" + host;

      URL url = new URL(host + "/central/servlet/ChannelSendMessageExecute?dnm=" + targetDNM + "&p1=" + generalUtils.sanitise(msg) + "&p2=" + channelType + "&p3=" + generalUtils.sanitise(toUser) + "&p4=" + generalUtils.sanitise(fromUser));

      URLConnection uc = url.openConnection();
      uc.setDoInput(true);
      uc.setUseCaches(false);
      uc.setDefaultUseCaches(false);

      uc.setRequestProperty("Content-Type", "application/octet-stream");

      BufferedReader di = new BufferedReader(new InputStreamReader(uc.getInputStream()));

      res = di.readLine();
      
      di.close();
    }
    catch(Exception e) { }
    
    if(res.equals("OK:"))
      return true;
    return false;
  }

}
