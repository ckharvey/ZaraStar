// =======================================================================================================================================================================================================
// System: ZaraStar Utils: Message Page Creation
// Module: MessagePage.java
// Author: C.K.Harvey
// Copyright (c) 1998-2009 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import java.io.*;
import java.sql.*;
import javax.servlet.http.*;

public class MessagePage
{
  FileOutputStream htmlOutput;

  GeneralUtils generalUtils = new GeneralUtils();
  ServerUtils serverUtils = new ServerUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void errorPage(PrintWriter out, HttpServletRequest req, Exception e, String msg, String unm, String sid, String dnm, String bnm, String urlBit, String men, String den, String uty, String caller, int [] bytesOut)
  {
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');

    msgScreen(false, out, req, 3, unm, sid, uty, men, den, dnm, bnm, caller, msg, urlBit, e.toString(), imagesDir, localDefnsDir, defnsDir, bytesOut);
  }  
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {
    if(out != null) out.println(str);
    bytesOut[0] += (str.length() + 2);
    return str + "\n";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void msgToSysAdmin(String msg, String unm, String dnm)
  {
    Connection con = null;
    Statement stmt = null;
    
    try
    {
      String uName = directoryUtils.getMySQLUserName();
      String pWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();
    
      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + uName + "&password=" + pWord);

      stmt = con.createStatement();

      stmt.executeUpdate("INSERT INTO chat (ToGroup, FromUser, ToUser, Msg, Status) VALUES ('','" + unm + "','Sysadmin','" + generalUtils.sanitiseForSQL(msg) + "','U' )");
                 
      if(stmt != null) stmt.close();
      if(con != null) con.close();
    }
    catch(Exception e)
    {
      System.out.println("MessagePage: " + e);
      try
      {
        if(stmt != null) stmt.close();
        if(con  != null) con.close();
      }
      catch(Exception e2) { }
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String msgScreen(boolean fromZA, PrintWriter out, HttpServletRequest req, int which, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String callingServlet, String imagesDir, String localDefnsDir,
                          String defnsDir, int[] bytesOut)
  {
    return msgScreen(fromZA, out, req, which, unm, sid, uty, men, den, dnm, bnm, callingServlet, "", "", "", imagesDir, localDefnsDir, defnsDir, bytesOut);
  }
  public String msgScreen(boolean fromZA, PrintWriter out, HttpServletRequest req, int which, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String callingServlet, String returnServletAndParams,
                          String returnMsg, String suppliedMsg, String imagesDir, String localDefnsDir, String defnsDir, int[] bytesOut)
  {
    String s = "";

    Connection con = null;

    try
    {
      if(uty.length() == 0) uty = "A";

      String title = "", msg = "", colour = "";

      switch(which)
      {
        case  0 : colour = "black"; title = "ZaraStar Message";            msg = suppliedMsg;                                          break;
        case  1 : colour = "red";   title = "Maintenance Mode";            msg = "Sorry, the System is currently in Maintenance Mode"; break;
        case  2 : colour = "red";   title = returnServletAndParams;        msg = "Sorry, a Connection Error has Occurred";             break;
        case  3 : colour = "red";   title = "System Error";                msg = "Sorry, a System Error has Occurred";                 break;
        case  4 : colour = "green"; title = "Update Successful";           msg = "Update Successful";                                  break;
        case  5 : colour = "red";   title = "Record Not Deleted";          msg = "Record Not Deleted";                                 break;
        case  6 : colour = "red";   title = "Record Not Found";            msg = "Record Not Found";                                   break;
        case  7 : colour = "red";   title = "Record Not Updated";          msg = "Record Not Updated";                                 break;
        case  8 : colour = "red";   title = "Record Already Exists";       msg = "Record Already Exists";                              break;
        case  9 : colour = "green"; title = "Mail Sent";                   msg = "Sent";                                               break;
        case 12 : colour = "red";   title = "Session Timeout";             msg = "Access Denied: Session Timeout";                     break;
        case 13 : colour = "red";   title = "Access Denied";               msg = "Access Denied";                                      break;
        case 16 : colour = "green"; title = "Generated OK";                msg = "Generated OK";                                       break;
        case 17 : colour = "red";   title = "Definition File Not Found";   msg = "Definition File Not Found";                          break;
        case 18 : colour = "red";   title = "Cannot Create Report Output"; msg = "Cannot Create Report Output";                        break;
        case 21 : colour = "green"; title = "Completed";                   msg = "Completed";                                          break;
        case 23 : colour = "red";   title = "Unexpected Error";            msg = "Unexpected Error";                                   break;
        case 27 : colour = "green"; title = "Successful";                  msg = "Successful";                                         break;
        case 31 : colour = "red";   title = "Source Document Not Found";   msg = "Source Document Not Found";                          break;
        case 33 : colour = "red";   title = "Unequal Ammounts";            msg = "Total Not Equal To Individual Ammounts";             break;
        case 39 : colour = "red";   title = "Unexpected Error";            msg = "Unexpected Error";                                   break;
        case 41 : colour = "green"; title = "Submitted";                   msg = "Submitted";                                          break;
      }

      scoutln(out, bytesOut, "<html><head><title>" + title + "</title></head>");

      String uName = directoryUtils.getMySQLUserName();
      String pWord = directoryUtils.getMySQLPassWord();
      Class.forName("com.mysql.jdbc.Driver").newInstance();
      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + uName + "&password=" + pWord);
      Statement stmt = null;
      ResultSet rs   = null;

      scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\"></head>");

      String service = callingServlet;
      if(service.length() == 0) service = "_";

      if(fromZA)
      {
        s += heading(out, true, unm, sid, uty, bnm, dnm, men, den, imagesDir, bytesOut);

        s += drawTitle(out, "DataBase Update", "407", unm, sid, uty, men, den, dnm, bnm, bytesOut);
      }
      else
      {
        int[] hmenuCount = new int[1];

        pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, service, "", callingServlet, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
        
        s += pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", title, service, unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);
      }

      if(which == 3)
      {
        s += scoutln(out, bytesOut, "<br><h1>A system error occurred during the last operation:");

        s += scoutln(out, bytesOut, "<br><h2>" + suppliedMsg + " in " + callingServlet);

        s += scoutln(out, bytesOut, "<br><br><p>These are important details:<br><br><br>");

        s += scoutln(out, bytesOut, "<table id=\"detailsTable\" border=0 cellspacing=2 cellpadding=2>");
        s += scoutln(out, bytesOut, "<tr><td nowrap>&nbsp;Domain:</td><td>"      + dnm    + "</td></tr>");
        s += scoutln(out, bytesOut, "<tr><td nowrap>&nbsp;User Name:</td><td>"   + unm    + "</td></tr>");
        s += scoutln(out, bytesOut, "<tr><td nowrap>&nbsp;User Type:</td><td>"   + uty    + "</td></tr>");
        s += scoutln(out, bytesOut, "<tr><td nowrap>&nbsp;Server:</td><td>"      + returnMsg + "</td></tr>");
        s += scoutln(out, bytesOut, "<tr><td nowrap>&nbsp;Menu Server:</td><td>" + men    + "</td></tr>");
        s += scoutln(out, bytesOut, "<tr><td nowrap>&nbsp;Menu Domain:</td><td>" + den    + "</td></tr>");
        s += scoutln(out, bytesOut, "<tr><td nowrap>&nbsp;Session ID:</td><td>"  + sid    + "</td></tr>");
        s += scoutln(out, bytesOut, "<tr><td nowrap>&nbsp;Browser ID:</td><td>"  + bnm    + "</td></tr>");
        s += scoutln(out, bytesOut, "</table><br>");
      }
      else
      {
        s += scoutln(out, bytesOut, "<br><br><p><font color='" + colour + "' size=7>" + msg + "</font</p><br><br>");

        if(returnMsg.length() > 0)
        {
          s += scoutln(out, bytesOut, "<br><br><br><p><a href=\"http://" + men + "/central/servlet/" + returnServletAndParams + "unm=" + unm + "&sid=" + sid + "&&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&dnm=" + dnm + "&bnm=" + bnm
                               + "\">Return to " + returnMsg + "</a></p><br><br>");
        }
      }

      if(fromZA)
        s += scoutln(out, bytesOut, "</body></html");

      if(con != null) con.close();
    }
    catch(Exception e2)
    {
      System.out.println(e2);
      try
      {
        if(con != null) con.close();
        s += scoutln(out, bytesOut, "<br><br><p><font color=red>Unknown Serious Error</font></p>");
      }
      catch(Exception e3)
      {
        System.out.println(e3);
      }
    }

    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void msgScreenW(PrintWriter out, HttpServletRequest req, int which, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String callingServlet, String imagesDir, String localDefnsDir,
                        String defnsDir, int[] bytesOut)
  {
    msgScreenW(out, req, which, unm, sid, uty, men, den, dnm, bnm, callingServlet, "", "", "", imagesDir, localDefnsDir, defnsDir, bytesOut);
  }
  public void msgScreenW(PrintWriter out, HttpServletRequest req, int which, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String callingServlet, String returnServletAndParams,
                        String returnMsg, String suppliedMsg, String imagesDir, String localDefnsDir, String defnsDir, int[] bytesOut)
  {
        Connection con = null;

    try
    {
      if(uty.length() == 0) uty = "A";

      String title = "", msg = "", colour = "";

      switch(which)
      {
        case  0 : colour = "black"; title = "ZaraStar Message";            msg = suppliedMsg;                                          break;
        case  1 : colour = "red";   title = "Maintenance Mode";            msg = "Sorry, the System is currently in Maintenance Mode"; break;
        case  2 : colour = "red";   title = returnServletAndParams;        msg = "Sorry, a Connection Error has Occurred";             break;
        case  3 : colour = "red";   title = "System Error";                msg = "Sorry, a System Error has Occurred";                 break;
        case  4 : colour = "green"; title = "Update Successful";           msg = "Update Successful";                                  break;
        case  5 : colour = "red";   title = "Record Not Deleted";          msg = "Record Not Deleted";                                 break;
        case  6 : colour = "red";   title = "Record Not Found";            msg = "Record Not Found";                                   break;
        case  7 : colour = "red";   title = "Record Not Updated";          msg = "Record Not Updated";                                 break;
        case  8 : colour = "red";   title = "Record Already Exists";       msg = "Record Already Exists";                              break;
        case  9 : colour = "green"; title = "Mail Sent";                   msg = "Sent";                                               break;
        case 12 : colour = "red";   title = "Session Timeout";             msg = "Access Denied: Session Timeout";                     break;
        case 13 : colour = "red";   title = "Access Denied";               msg = "Access Denied";                                      break;
        case 16 : colour = "green"; title = "Generated OK";                msg = "Generated OK";                                       break;
        case 17 : colour = "red";   title = "Definition File Not Found";   msg = "Definition File Not Found";                          break;
        case 18 : colour = "red";   title = "Cannot Create Report Output"; msg = "Cannot Create Report Output";                        break;
        case 21 : colour = "green"; title = "Completed";                   msg = "Completed";                                          break;
        case 23 : colour = "red";   title = "Unexpected Error";            msg = "Unexpected Error";                                   break;
        case 27 : colour = "green"; title = "Successful";                  msg = "Successful";                                         break;
        case 31 : colour = "red";   title = "Source Document Not Found";   msg = "Source Document Not Found";                          break;
        case 33 : colour = "red";   title = "Unequal Ammounts";            msg = "Total Not Equal To Individual Ammounts";             break;
        case 39 : colour = "red";   title = "Unexpected Error";            msg = "Unexpected Error";                                   break;
        case 41 : colour = "green"; title = "Submitted";                   msg = "Submitted";                                          break;
      }

      if(which != 3)
        scoutln(out, bytesOut, callingServlet + "\001Message\001" + title + "\001javascript:getHTML('_" + callingServlet + "','')\001\001\001\001\003");

      String uName = directoryUtils.getMySQLUserName();
      String pWord = directoryUtils.getMySQLPassWord();
      Class.forName("com.mysql.jdbc.Driver").newInstance();
      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + uName + "&password=" + pWord);
      Statement stmt = null;
      ResultSet rs   = null;

      scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");//</head>");

      String service = callingServlet;
      if(service.length() == 0) service = "_";

      pageFrameUtils.drawTitleW(out, false, false, "", "", "", "", "", "", title, service, unm, sid, uty, men, den, dnm, bnm, bytesOut);

      if(which == 3)
      {
        scoutln(out, bytesOut, "<br><h1>A system error occurred during the last operation:");

        scoutln(out, bytesOut, "<br><h2>" + suppliedMsg + " in " + callingServlet);

        scoutln(out, bytesOut, "<br><br><p>These are important details:<br><br><br>");

        scoutln(out, bytesOut, "<table id=\"detailsTable\" border=0 cellspacing=2 cellpadding=2>");
        scoutln(out, bytesOut, "<tr><td nowrap>&nbsp;Domain:</td><td>"      + dnm    + "</td></tr>");
        scoutln(out, bytesOut, "<tr><td nowrap>&nbsp;User Name:</td><td>"   + unm    + "</td></tr>");
        scoutln(out, bytesOut, "<tr><td nowrap>&nbsp;User Type:</td><td>"   + uty    + "</td></tr>");
        scoutln(out, bytesOut, "<tr><td nowrap>&nbsp;Server:</td><td>"      + returnMsg + "</td></tr>");
        scoutln(out, bytesOut, "<tr><td nowrap>&nbsp;Menu Server:</td><td>" + men    + "</td></tr>");
        scoutln(out, bytesOut, "<tr><td nowrap>&nbsp;Menu Domain:</td><td>" + den    + "</td></tr>");
        scoutln(out, bytesOut, "<tr><td nowrap>&nbsp;Session ID:</td><td>"  + sid    + "</td></tr>");
        scoutln(out, bytesOut, "<tr><td nowrap>&nbsp;Browser ID:</td><td>"  + bnm    + "</td></tr>");
        scoutln(out, bytesOut, "</table><br>");
      }
      else
      {
        scoutln(out, bytesOut, "<br><br><p><font color='" + colour + "' size=7>" + msg + "</font</p><br><br>");

        if(returnMsg.length() > 0)
        {
          scoutln(out, bytesOut, "<br><br><br><p><a href=\"http://" + men + "/central/servlet/" + returnServletAndParams + "unm=" + unm + "&sid=" + sid + "&&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&dnm=" + dnm + "&bnm=" + bnm
                               + "\">Return to " + returnMsg + "</a></p><br><br>");
        }
      }

      if(con != null) con.close();
        System.out.println("MessagePage: " + con.isClosed());
    }
    catch(Exception e2)
    {
      System.out.println(e2);
      try
      {
      if(con != null) con.close();
        scoutln(out, bytesOut, "<br><br><p><font color=red>Unknown Serious Error</font></p>");
      }
      catch(Exception e3)
      {
        System.out.println(e3);
      }
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // necessary local copy
  private String heading(PrintWriter out, boolean showTabs, String unm, String sid, String uty, String bnm, String dnm, String men, String den, String imagesDir, int[] bytesOut) throws Exception
  {
    String s = "";

    s += scoutln(out, bytesOut, "<script language=\"JavaScript\">");
    s += scoutln(out, bytesOut, "function set(option){");
    s += scoutln(out, bytesOut, "window.location.href=\"/central/servlet/OptionTabs?p1=\"+option+\"&unm=" + unm + "&uty=" + uty + "&sid=" + sid + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\";}");
    s += scoutln(out, bytesOut, "function help(svc){var newWindow=window.open('','zarahelp');");
    s += scoutln(out, bytesOut, "newWindow.location.href=\"/central/servlet/HelpguideJump?p1=\"+svc+\"&unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\";}");
    s += scoutln(out, bytesOut, "</script>");

    s += scoutln(out, bytesOut, "<table width='100%' border='0' cellpadding='0' cellspacing='0' bgcolor=darkred>");
    s += scoutln(out, bytesOut, "<tr><td width='100%'><img src='" + imagesDir + "zarastar.jpg' alt='ZaraStar Admin' border='0' />");
    s += scoutln(out, bytesOut, "</td></tr></table>");

    s += scoutln(out, bytesOut, "<table width='100%' border='0' cellpadding='0' cellspacing='0' bgcolor=black>");
    s += scoutln(out, bytesOut, "<tr><td align=right valign=top><p><font color=white size=2 face='verdana,arial,helvetica,sans-serif'>User: " + unm + " &nbsp;&nbsp; Server: " + dnm
                         + "&nbsp;<br>ZaraStar Administrator &nbsp;&nbsp;&nbsp;&nbsp; (c) 1997-2009 Christopher Harvey</td></tr></table>");

    if(showTabs)
    {
      s += scoutln(out, bytesOut, "<table width='100%' border='0' cellpadding='0' cellspacing='0' bgcolor=lightgrey><td>");
      s += scoutln(out, bytesOut, "<td nowrap bgcolor=lightblue><p><font color=white size=2 face='verdana,arial,helvetica,sans-serif'> &nbsp; <a href=\"javascript:set('A')\">Access</a> &nbsp; </td>"
                           + "<td nowrap bgColor=lightblue><p><font color=white size=2 face='verdana,arial,helvetica,sans-serif'> &nbsp; <a href=\"javascript:set('C')\">Customise</a> &nbsp; </td>"
                           + "<td nowrap bgcolor=lightblue><p><font color=white size=2 face='verdana,arial,helvetica,sans-serif'> &nbsp; <a href=\"javascript:set('U')\">Users</a> &nbsp; </td>"
                           + "<td nowrap bgcolor=lightblue><p><font color=white size=2 face='verdana,arial,helvetica,sans-serif'> &nbsp; <a href=\"javascript:set('N')\">NX</a> &nbsp; </td>"
                           + "<td nowrap bgcolor=lightblue><p><font color=white size=2 face='verdana,arial,helvetica,sans-serif'> &nbsp; <a href=\"javascript:set('S')\">Sun Ray</a> &nbsp; </td>"
                           + "<td nowrap bgcolor=lightblue><p><font color=white size=2 face='verdana,arial,helvetica,sans-serif'> &nbsp; <a href=\"javascript:set('B')\">Backup</a> &nbsp; </td>"
                           + "<td nowrap bgcolor=lightblue><p><font color=white size=2 face='verdana,arial,helvetica,sans-serif'> &nbsp; <a href=\"javascript:set('W')\">Network</a> &nbsp; </td>"
                           + "<td nowrap bgcolor=lightblue><p><font color=white size=2 face='verdana,arial,helvetica,sans-serif'> &nbsp; <a href=\"javascript:set('D')\">DataBase</a> &nbsp; </td>"

                           + "<td width=99%></td></tr></table>");
    }

    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String drawTitle(PrintWriter out, String title, String service, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    String s = scoutln(out, bytesOut, "<table id='title' width=100%>");
    s += scoutln(out, bytesOut, "<tr><td colspan=2 nowrap><p>" + title + directoryUtils.buildHelp(service) + "</td></tr></table>");
    return s;
  }

}
