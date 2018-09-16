// =======================================================================================================================================================================================================
// System: ZaraStar: Channels: historical
// Module: ChannelHistorical.java
// Author: C.K.Harvey
// Copyright (c) 2000-09 Christopher Harvey. All Rights Reserved.
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

public class ChannelHistorical extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  ConnectionsUtils  connectionsUtils = new ConnectionsUtils();
  
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="";
    
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
      p1  = req.getParameter("p1"); // channel name
      p2  = req.getParameter("p2"); // channel type
      
      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, bytesOut);
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

      System.out.println("12712: " + e);
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "ChannelHistorical", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 12712, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush(); 
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1, String p2, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;
    
    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "ChannelHistorical", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 12712, bytesOut[0], 0, "SID:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }
                            
    set(con, stmt, rs, out, req, p1, p2, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 12712, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String name, String type, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                   String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Historical Messages</title>");

    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    int[] hmenuCount = new int[1];
    connectionsUtils.outputPageFrame(con, stmt, rs, out, req, "ChannelHistorical", "", "12712", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount, bytesOut);

    connectionsUtils.drawTitle(con, stmt, rs, req, out, "Historial Messages", "12712", unm, sid, uty, men, den, dnm, bnm, hmenuCount, localDefnsDir, defnsDir, bytesOut);

    scoutln(out, bytesOut, "<form><table id=\"page\" width=100% border=0>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    String s;
    if(name.startsWith("__"))
      s = "Company Broadcast";
    else s = name;
    
    scoutln(out, bytesOut, "<tr><td colspan=4 nowrap><p><b>Channel to " + s);
    if(type.equals("P"))
      scout(out, bytesOut, " (Personal)</td></tr>");
    else scout(out, bytesOut, " (Group)</td></tr>");
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    if(type.equals("P"))
      getP(con, stmt, rs, out, name, unm, sid, uty, men, den, dnm, bnm, bytesOut);
    else getG(con, stmt, rs, out, name, unm, sid, uty, men, den, dnm, bnm, bytesOut);

    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir));
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void getP(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String name, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT DateTime, Msg, FromUser, ToUser, Status, MsgID FROM chat WHERE (ToUser = '" + unm + "' AND FromUser = '" + name + "') OR (ToUser = '" + name + "' AND FromUser = '" + unm + "') ORDER BY MsgID");
 
    String dateTime, msg, fromUser, toUser, status, cssFormat="";
    boolean first = true;
    
    while(rs.next())
    {    
      dateTime = rs.getString(1);
      msg      = rs.getString(2);
      fromUser = rs.getString(3);
      toUser   = rs.getString(4);
      status   = rs.getString(5);
      
      dateTime = generalUtils.convertFromTimestamp(dateTime);
      int len = dateTime.length() - 3;
      dateTime = dateTime.substring(0, len);
      
      if(status.equals("U"))
        status = "Unread";
      else status = "";
      
      if(first)
      {
        scoutln(out, bytesOut, "<tr id=\"pageColumn\"><td><p>#</td><td><p>Status</td><td><p>Date</td>");
        scoutln(out, bytesOut, "<td><p>From</td>");
        scoutln(out, bytesOut, "<td><p>To</td>");
        scoutln(out, bytesOut, "<td><p>Message</td></tr>");
        first = false;
      }

      if(cssFormat.equals("line1")) cssFormat = "line2"; else cssFormat = "line1";

      scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\"><td><p>" + rs.getString(6) + "</td><td><p>" + status + "</td><td><p>" + dateTime + "</td>");
      scoutln(out, bytesOut, "<td nowrap><p>" + fromUser + "</td>");
      scoutln(out, bytesOut, "<td nowrap><p>" + toUser + "</td>");
      scoutln(out, bytesOut, "<td><p>" + connectionsUtils.convertLinks(msg, unm, sid, uty, men, den, dnm, bnm) + "</td></tr>");
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void getG(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String name, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT DateTime, Msg, FromUser, MsgID FROM chat WHERE ToGroup = '" + name + "' ORDER BY MsgID");
 
    String dateTime, msg, fromUser, cssFormat="";
    boolean first = true;
    
    while(rs.next())
    {    
      dateTime = rs.getString(1);
      msg      = rs.getString(2);
      fromUser = rs.getString(3);
      
      dateTime = generalUtils.convertFromTimestamp(dateTime);
      int len = dateTime.length() - 3;
      dateTime = dateTime.substring(0, len);
      
      if(first)
      {
        scoutln(out, bytesOut, "<tr id=\"pageColumn\"><td><p>#</td><td><p>Date</td>");
        scoutln(out, bytesOut, "<td><p>From</td>");
        scoutln(out, bytesOut, "<td><p>Message</td></tr>");
        first = false;
      }

      if(cssFormat.equals("line1")) cssFormat = "line2"; else cssFormat = "line1";

      scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\"><td><p>" + rs.getString(4) + "</td><td><p>" + dateTime + "</td>");
      scoutln(out, bytesOut, "<td nowrap><p>" + fromUser + "</td>");
      scoutln(out, bytesOut, "<td><p>" + connectionsUtils.convertLinks(msg, unm, sid, uty, men, den, dnm, bnm) + "</td></tr>");
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scout(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.print(str);
    bytesOut[0] += str.length();    
  }
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
