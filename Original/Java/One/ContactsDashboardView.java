// =======================================================================================================================================================================================================
// System: ZaraStar: Contacts: view dashboard
// Module: ContactsDashboardView.java
// Author: C.K.Harvey
// Copyright (c) 2000-09 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import javax.servlet.http.*;
import java.io.*;
import java.sql.*;

public class ContactsDashboardView
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  Profile profile = new Profile();
  DashboardUtils dashboardUtils = new DashboardUtils();

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null, stmt2 = null;
    ResultSet rs   = null, rs2   = null;

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "ContactsDashboardView", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8200, bytesOut[0], 0, "SID:");
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    set(con, stmt, stmt2, rs, rs2, out, req, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 8200, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                   String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Dashboard</title>");

    scoutln(out, bytesOut, "<SCRIPT LANGUAGE=\"JavaScript\">");

    scoutln(out, bytesOut, "function view(){");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/IMMain?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\";}");

    scoutln(out, bytesOut, "function connect(zcn,zpn,docCode,docType){");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/ExternalUserServices8?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=\"+zcn+\"&p2=\"+zpn+\"&p3=\"+docCode+\"&p4=\"+docType+\"&bnm=" + bnm
                         + "\";}");

    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    int[] hmenuCount = new int[1];
            
    dashboardUtils.outputPageFrame(con, stmt, rs, out, req, "ContactsDashboardView", "", "8200", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount, bytesOut);

    dashboardUtils.drawTitle(out, "Dashboard for " + unm, "8200", unm, sid, uty, men, den, dnm, bnm, bytesOut);

    String html = unprocessedChannelTransactions(con, stmt, stmt2, rs, rs2, unm);

    scoutln(out, bytesOut, "<table id='page' width=100% border=0>");

    if(html.length() == 0)
      scoutln(out, bytesOut, "<tr><td><p><br><br><br>No Unread Messages</td></tr>");
    else scoutln(out, bytesOut, "<tr><td colspan=4><p>Unread Messages &nbsp;&nbsp;<a href=\"javascript:view()\">View</a></td></tr>" + html);

    scoutln(out, bytesOut, "<tr><td><p>&nbsp;</td></tr>");
      
    scoutln(out, bytesOut, "</table>");
    
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }
 
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String unprocessedChannelTransactions(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String unm) throws Exception
  {
    String dateTime, fromUser, msg, toGroup, cssFormat = "", html = "";

    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT DateTime, FromUser, Msg FROM chat WHERE ToUser = '" + unm + "' AND Status = 'U'");
      
      while(rs.next())
      {
        dateTime = rs.getString(1);
        fromUser = rs.getString(2);
        msg      = rs.getString(3);
        
        if(cssFormat.equals("line1")) cssFormat = "line2"; else cssFormat = "line1";

        if(fromUser.startsWith("_"))
        {
          int len = fromUser.length();
          fromUser = "Casual-" + fromUser.substring(len - 4, len);
        }

        html += "<tr id='" + cssFormat + "'><td><p>" + fromUser + " (" + generalUtils.timeFromTimestamp(dateTime) + "): " + msg + "</td></tr>";
      }
    }
    catch(Exception e) { System.out.println("8200" + e); }    
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    // no personal msgs... may be a group msg exists
    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT MsgID, ToGroup FROM chatunread WHERE ToUser = '" + unm + "'");
      
      if(rs.next())
      {
        toGroup = rs.getString(2);

        stmt2 = con.createStatement();

        rs2 = stmt2.executeQuery("SELECT DateTime, FromUser, Msg FROM chat WHERE MsgID = '" + rs.getString(1) + "'");
      
        if(rs2.next()) // just-in-case
        {
          dateTime = rs2.getString(1);
          fromUser = rs2.getString(2);
          msg      = rs2.getString(3);
        
          if(cssFormat.equals("line1")) cssFormat = "line2"; else cssFormat = "line1";

          if(toGroup.startsWith("__"))
            toGroup = "Company Broadcast";
    
          html += "<tr id='" + cssFormat + "'><td><p>" + fromUser + " in Group: " + toGroup + " (" + generalUtils.timeFromTimestamp(dateTime) + "): " + msg + "</td></tr>";
        }

        if(rs2   != null) rs2.close();
        if(stmt2 != null) stmt2.close();
      }
    }
    catch(Exception e) { System.out.println("8200 " + e); }
    
    if(rs    != null) rs.close();
    if(stmt  != null) stmt.close();
    if(rs2   != null) rs2.close();
    if(stmt2 != null) stmt2.close();
    
    return html;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);
  }

}
