// =======================================================================================================================================================================================================
// System: ZaraStar Chat: Add to My Channels
// Module: ChannelAddMessage.java
// Author: C.K.Harvey
// Copyright (c) 2003-08 Christopher Harvey. All Rights Reserved.
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

public class ChannelAddMessage extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  ChannelUtils channelUtils = new ChannelUtils();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="";

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

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "ChannelAddMessage", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 12710, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                    int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    
    Statement stmt = null, stmt2 = null;
    ResultSet rs   = null, rs2   = null;
    
    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 12710, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "ChannelAddMessage", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 12710, bytesOut[0], 0, "ACC:");
      
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "ChannelAddMessage", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 12710, bytesOut[0], 0, "SID:");
      
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    set(con, stmt, stmt2, rs, rs2, out, req, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 12710, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
      
      if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, HttpServletRequest req, String unm,
                   String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut)
                   throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Local Channels</title>");

    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\""
                           + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 12700, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function update(name,op){var p1=sanitise(name);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/ChannelAddMessageUpdate?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den="
                             + den + "&dnm=" + dnm + "&p2=\"+op+\"&bnm=" + bnm + "&p1=\"+p1;}");
    }

    scoutln(out, bytesOut, "function sanitise(code){");
    scoutln(out, bytesOut, "var code2='';var x;var len=code.length;");
    scoutln(out, bytesOut, "for(x=0;x<len;++x)");
    scoutln(out, bytesOut, "if(code.charAt(x)=='#')code2+='%23';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='\"')code2+='%22';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='&')code2+='%26';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='%')code2+='%25';");
    scoutln(out, bytesOut, "else if(code.charAt(x)==' ')code2+='%20';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='+')code2+='%2b';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='?')code2+='%3F';");
    scoutln(out, bytesOut, "else code2+=code.charAt(x);");
    scoutln(out, bytesOut, "return code2};");
    
    scoutln(out, bytesOut, "</script>");

    int[] hmenuCount = new int[1];
            
    channelUtils.outputPageFrame(con, stmt, rs, out, req, "ChannelAddMessage", "", "12710", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount, bytesOut);

    channelUtils.drawTitle(con, stmt, rs, req, out, "Available Local Channels", "12710", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
  
    scoutln(out, bytesOut, "<table id=\"page\" width=100% cellpadding=3>");
    scoutln(out, bytesOut, "</tr><tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr id=\"pageColumn\">");
    scoutln(out, bytesOut, "<td nowrap><p>&nbsp; Channel Name &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>&nbsp; Purpose &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>&nbsp; Type &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>&nbsp; Status &nbsp;</td><td></td></tr>");

    list(con, stmt, stmt2, rs, rs2, out, unm, bytesOut);
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "</table>");     scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void list(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, String unm, int[] bytesOut)
                    throws Exception
  {
    scoutln(out, bytesOut, "<tr id=\"line1\"><td><p>___Company___</td><td><p>Broadcast Channel for Internal Users</td><td><p>Internal, private</td>"
                         + "<td><p>Available</td><td></td></tr>");

    try
    {
      stmt = con.createStatement();
  
      rs = stmt.executeQuery("SELECT Name, Purpose, Type, Status FROM channels WHERE Name != '___Company___' ORDER BY Name");

      String name, purpose, type, status, cssFormat;
      boolean line1=false;

      while(rs.next())                  
      {
        name    = rs.getString(1);
        purpose = rs.getString(2);
        type    = rs.getString(3);
        status  = rs.getString(4);
        
        if(type.equals("I"))
          type = "Internal, private";
        else
        if(type.equals("R"))
          type = "Global, registered";
        else type = "Global, public";
            
        if(status.equals("U"))
          status = "Currently Available";
        else status = "Currently Not Available";
        
        if(line1) { cssFormat = "line1"; line1 = false; } else { cssFormat = "line2"; line1 = true; }
                    
        scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\">");
        scoutln(out, bytesOut, "<td><p>" + name + "</td>");
        scoutln(out, bytesOut, "<td><p>" + purpose + "</td>");
        scoutln(out, bytesOut, "<td><p>" + type + "</td>");
        scoutln(out, bytesOut, "<td><p>" + status + "</td>");

        if(alreadyUseTheChannel(con, stmt2, rs2, unm, name))
          scoutln(out, bytesOut, "<td nowrap><a href=\"javascript:update('" + generalUtils.sanitise(name) + "','R')\">Remove</a> the Channel</td></tr>");
        else scoutln(out, bytesOut, "<td nowrap><p><a href=\"javascript:update('" + generalUtils.sanitise(name) + "','A')\">Add</a> the Channel</td></tr>");        
      }
                 
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

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean alreadyUseTheChannel(Connection con, Statement stmt, ResultSet rs, String unm, String name) throws Exception
  {
    int rowcount = 0;

    try
    {
      stmt = con.createStatement();
    
      rs = stmt.executeQuery("SELECT COUNT(*) as rowcount FROM channelsmine WHERE UserCode = '" + unm + "' AND Name = '" + generalUtils.sanitise(name) + "'");

      
      if(rs.next())                  
        rowcount = rs.getInt(1);
      
      if(rs   != null) rs.close();                                 
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
    
    if(rowcount == 0)
      return false;
    return true;
  }  

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
