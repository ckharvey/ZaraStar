//====================================================================================================================================================================================================================================================
// System: ZaraStar: Chat: close channel
// Module: ChannelClose.java
// Author: C.K.Harvey
// Copyright (c) 2006-09 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
//====================================================================================================================================================================================================================================================

package org.zarastar.zarastar;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.sql.*;
import java.util.Enumeration;

public class ChannelClose extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  AdminUtils adminUtils = new AdminUtils();
  Inbox inbox = new Inbox();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", toUser="", type="";

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
          toUser = value[0];
        else
        if(name.equals("p2"))
          type = value[0];
      }

      doIt(req, res, unm, sid, uty, men, den, dnm, bnm, toUser, type, bytesOut);
    }    
    catch(Exception e)
    {
      System.out.println(("Unexpected System Error: 12709: " + e));
      res.getWriter().write("Unexpected System Error: 12709");
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(HttpServletRequest req, HttpServletResponse res, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String toUser, String type, int[] bytesOut) throws Exception
  {
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
 
    String rtn="Unexpected System Error: 12709";
    
    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      rtn = "Access Denied - Duplicate SignOn or Session Timeout";
    }
    else
    {
      set(res, toUser, type, unm, dnm);
    }

    serverUtils.totalBytes(req, unm, dnm, 12709, bytesOut[0], 0, "");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(HttpServletResponse res, String toUser, String type, String unm, String dnm) throws Exception
  {
    Connection con = null;
    Statement stmt = null, stmt2 = null;
    ResultSet  rs  = null;
    
    String uName = directoryUtils.getMySQLUserName();
    String pWord = directoryUtils.getMySQLPassWord();

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    
    con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + uName + "&password=" + pWord);

    try
    {
      stmt = con.createStatement();
    
      stmt.executeUpdate("DELETE FROM channelsopen WHERE UserCode = '" + unm + "' AND Name = '" + generalUtils.sanitiseForSQL(toUser) + "'");
    
      if(stmt != null) stmt.close();

      if(type.equals("P"))
      {
        stmt = con.createStatement();

        stmt.executeUpdate("UPDATE chat SET Status = 'R' WHERE FromUser = '" + unm + "' AND ToUser = '" + toUser + "'");
        if(stmt != null) stmt.close();
      }
      else // group
      {
        // if this is the last person to use 'close' for this group then can set the all chat records (for this group) to read
      
        stmt = con.createStatement();
        rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM chatunread WHERE ToGroup = '" + generalUtils.sanitiseForSQL(toUser) + "'");
        int rowCount = 1;
        if(rs.next())
          rowCount = rs.getInt("rowcount");
        if(rs   != null) rs.close();
        if(stmt != null) stmt.close();

        if(rowCount == 1) // is the last
        {
          stmt = con.createStatement();
          rs = stmt.executeQuery("SELECT MsgID FROM chatunread WHERE ToGroup = '" + toUser + "'");
        
          while(rs.next())
          {
            stmt2 = con.createStatement();
            stmt2.executeUpdate("UPDATE chat SET Status = 'R' WHERE MsgID = '" + rs.getString(1) + "'");
            if(stmt2 != null) stmt2.close();
          }
        
          if(rs   != null) rs.close();
          if(stmt != null) stmt.close();          
        }

        stmt = con.createStatement();
        stmt.executeUpdate("DELETE FROM chatunread WHERE ToUser = '" + unm + "' AND ToGroup = '" + generalUtils.sanitiseForSQL(toUser) + "'");
        if(stmt != null) stmt.close();        
      }          
    }
    catch(Exception e)
    {
      System.out.println("12709: " + e);
        if(rs   != null) rs.close();
      if(stmt  != null) stmt.close();
    }    
    
    if(con != null) con.close();
    
    String s = "<msg><res>.</res></msg>";

    res.getWriter().write(s);
  }

}
