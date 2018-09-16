// =======================================================================================================================================================================================================
// System: ZaraStar Chat: View Channel Options
// Module: ChannelViewOptions.java
// Author: C.K.Harvey
// Copyright (c) 2003-08 Christopher Harvey. All Rights Reserved.
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

public class ChannelViewOptions extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  ConnectionsUtils connectionsUtils = new ConnectionsUtils();

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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "ChannelViewOptions", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 12713, bytesOut[0], 0, "ERR:");
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
    ResultSet rs   = null, rs2 = null;
    
    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 12713, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "ChannelViewOptions", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 12713, bytesOut[0], 0, "ACC:");
      
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "ChannelViewOptions", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 12713, bytesOut[0], 0, "SID:");
      
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    set(con, stmt, stmt2, rs, rs2, out, req, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 12713, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
      
      if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, HttpServletRequest req, String unm,
                   String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut)
                   throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Channel Options</title>");

    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\""
                           + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 12700, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function join(name,type){var p1=sanitise(name);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/IMMain?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den
                           + "&dnm=" + dnm + "&p2=\"+type+\"&bnm=" + bnm + "&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 128, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function dir(){");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/CompanyPersonnelDirectory?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den
                           + "&dnm=" + dnm + "&bnm=" + bnm + "\";}");
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
            
    connectionsUtils.outputPageFrame(con, stmt, rs, out, req, "ChannelViewOptions", "", "12713", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount, bytesOut);

    connectionsUtils.drawTitle(con, stmt, rs, req, out, "Business Channel Options", "12713", unm, sid, uty, men, den, dnm, bnm, hmenuCount, localDefnsDir, defnsDir, bytesOut);

    scoutln(out, bytesOut, "<table id=\"page\" width=100% cellpadding=3>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 128, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "<tr><td nowrap><p><a href=\"javascript:dir()\">Open a Channel</a> and chat now with our people.</td></tr>");
      scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
      scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    }

    // check for any public channels    
    list(con, stmt, rs, out, uty, bytesOut);
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir));
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void list(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String uty, int[] bytesOut) throws Exception
  {
    try
    {
      stmt = con.createStatement();
  
      rs = stmt.executeQuery("SELECT Name, Purpose, Type, Status FROM channels WHERE Type != 'I' ORDER BY Purpose");

      String name, type, purpose, status, cssFormat;
      boolean line1 = false, first = true, wanted;

      while(rs.next())                  
      {
        name    = rs.getString(1);
        purpose = rs.getString(2);
        type    = rs.getString(3);
        status  = rs.getString(4);

        wanted = false;
        if(type.equals("R") && (uty.equals("R") || uty.equals("R")))
          wanted = true;
        else
        if(! type.equals("I") && ! type.equals("R")) // global, public
          wanted = true;

        if(wanted)
        {
          if(first)
          {
            scoutln(out, bytesOut, "<tr id=\"pageColumn\">");
            scoutln(out, bytesOut, "<td nowrap><p>&nbsp; Channel Purpose &nbsp;</td>");
            scoutln(out, bytesOut, "<td nowrap><p>&nbsp; Type &nbsp;</td>");
            scoutln(out, bytesOut, "<td nowrap><p>&nbsp; Status &nbsp;</td>");
            scoutln(out, bytesOut, "<td nowrap><p>&nbsp; Location &nbsp;</td><td></td></tr>");
            first = false;
          }

          scoutln(out, bytesOut, "<tr><td><p>" + purpose + "</td>");

          if(status.equals("U"))
            status = "(Currently Available)";
          else status = "(Currently Not Available)";
            
          scoutln(out, bytesOut, "<td><p>" + status + "</td>");

          scoutln(out, bytesOut, "<td nowrap><p><a href=\"javascript:add('" + generalUtils.sanitise(name) + "')\">Add</a> the Channel Now</td></tr>");
        }
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
  private void channelStatusAndPurpose(Connection con, Statement stmt, ResultSet rs, String name, String[] purpose, String[] status) throws Exception
  {
    purpose[0] = status[0] = "";
    
    try
    {
      stmt = con.createStatement();
  
      rs = stmt.executeQuery("SELECT Purpose, Status FROM channels WHERE Name = '" + name + "'");

      if(rs.next())                  
      {
        purpose[0] = rs.getString(1);
        status[0]  = rs.getString(2);
        
        if(status[0].equals("U"))
          status[0] = "Currently Available";
        else status[0] = "Currently Not Available";
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
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
