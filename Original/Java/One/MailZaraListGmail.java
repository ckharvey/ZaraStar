// =======================================================================================================================================================================================================
// System: ZaraStar MailEngine: List GMail
// Module: MailZaraListGmail.java
// Author: C.K.Harvey
// Copyright (c) 2001-08 Christopher Harvey. All Rights Reserved.
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
import java.net.*;

public class MailZaraListGmail extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  MailUtils mailUtils = new MailUtils();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", men="", den="", uty="", dnm="", bnm="", p1="";

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
      p1  = req.getParameter("p1"); // account

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "MailZaraListGmail", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8025, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1,
                    int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 8014, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "MailZaraListGmail", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8025, bytesOut[0], 0, "ACC:" + p1);
      
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "MailZaraListGmail", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8025, bytesOut[0], 0, "SID:" + p1);
      
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    checkMail(con, stmt, rs, out, req, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, p1, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 8025, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    
    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void checkMail(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String unm, String sid,
                         String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, String account, int[] bytesOut)
  {
    try
    {
      scoutln(out, bytesOut, "<html><head><title>Mail: List Mail</title>");

      scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\""
                             + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");
    }
    catch(Exception e2) { }

    try
    {
      String[] server       = new String[1];
      String[] fromAddress  = new String[1];
      String[] userName     = new String[1];
      String[] passWord     = new String[1];
      String[] userFullName = new String[1];
      String[] type         = new String[1];
      mailUtils.getAccountDetails(account, unm, dnm, server, fromAddress, userName, passWord, userFullName, type);

      int[] hmenuCount = new int[1];

      mailUtils.outputPageFrame(con, stmt, rs, out, req, false, account, "", "MailZaraListGmail", "8025", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount, bytesOut);

      mailUtils.drawTitle(con, stmt, rs, req, out, false, account, "List GMail", "8025", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
      
      scoutln(out, bytesOut, "<table id=\"page\" width=100%>");

      getGMail(out, account, userName[0], passWord[0], "M", unm, uty, sid, men, den, dnm, bnm, localDefnsDir, bytesOut);

      scoutln(out, bytesOut, "</table></form>");
      scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
    }
    catch(Exception e2) { } 
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void getGMail(PrintWriter out, String account, String userName, String passWord, String mode, String unm, String uty, String sid, String men,
                        String den, String dnm, String bnm, String localDefnsDir, int[] bytesOut) throws Exception
  { 
    URL url = new URL("http://" + serverUtils.serverToCall("MAIL", localDefnsDir) + "/central/servlet/MailZaraGetGmailATOM?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men="
                    + men + "&den=" + den + "&dnm=" + dnm + "&p1=" + generalUtils.sanitise(userName) + "&p2=" + generalUtils.sanitise(passWord) + "&p3=" + mode + "&bnm="
                    + bnm);
    
    URLConnection uc = url.openConnection();
    uc.setDoInput(true);
    uc.setUseCaches(false);
    uc.setDefaultUseCaches(false);

    uc.setRequestProperty("Content-Type", "application/octet-stream");

    BufferedReader di = new BufferedReader(new InputStreamReader(uc.getInputStream()));

    String s = di.readLine();

    if(s == null)
    {
      scoutln(out, bytesOut, "<tr><td nowrap><p>" + account + "</td>");
      scoutln(out, bytesOut, "<td align=center><p>Server Busy or Unavailable</td></tr>");
    }

    boolean first = true;
    String numMsgs = "", author, date, title, link;
    String[] line = new String[1];  line[0] = "";
    int x = 0, len = 0;
    
    if(s != null)
    {
      if(first)
      { 
        len = s.length();
        while(x < len && s.charAt(x) != '\001')
          numMsgs += s.charAt(x++);
        
        String t;
        if(numMsgs.equals("O"))
          t = "no messages.";
        else
        if(numMsgs.equals("1"))
          t = "the following message:";
        else t = "the following " + numMsgs + " messages:";
        
        scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
        scoutln(out, bytesOut, "<tr><td colspan=3><p>Your <b>" + account + "</b> inbox contains " + t + "</td></tr>");
        scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

        if(numMsgs.equals("O"))
        {
          scoutln(out, bytesOut, "<tr><td colspan=3><br><br><span id=\"textRedHighlighting\"><b>&nbsp;&nbsp;&nbsp;Empty</b></span><br><br><br></td></tr></table>");
        }
        else
        {
          scoutln(out, bytesOut, "<table id=\"page\" cellspacing=2 cellpadding=0 width=100%>");
  
          scoutln(out, bytesOut, "<tr id=\"pageColumn\">");
          if(bnm.equals("B"))
          {
            scoutln(out, bytesOut, "<td></td></tr>");          
          }
          else
          {
            scoutln(out, bytesOut, "<td><p><b>From</td>");
            scoutln(out, bytesOut, "<td><p><b>Subject</td>");
            scoutln(out, bytesOut, "<td><p><b>Date</td></tr>");
          }
        }

        first = false;
      }
      
      while(x < len)// && s.charAt(x) != '\002')
      {
        ++x;
        author = "";
        while(x < len && s.charAt(x) != '\001')
          author += s.charAt(x++);

        ++x;
        date = "";
        while(x < len && s.charAt(x) != '\001')
          date += s.charAt(x++);

        ++x;
        title = "";
        while(x < len && s.charAt(x) != '\001')
          title += s.charAt(x++);

        ++x;
        link = "";
        while(x < len && s.charAt(x) != '\001')
          link += s.charAt(x++);

        writeMsgLine(out, author, date, title, link, line, bytesOut);
      }
    }
    
    if(numMsgs.length() > 0 && ! numMsgs.equals("X"))
    {
          scoutln(out, bytesOut, "<tr id=\"pageColumn\">");
          if(bnm.equals("B"))
          {
            scoutln(out, bytesOut, "<td></td></tr>");          
          }
          else
          {
            scoutln(out, bytesOut, "<td><p><b>From</td>");
            scoutln(out, bytesOut, "<td><p><b>Subject</td>");
            scoutln(out, bytesOut, "<td><p><b>Date</td></tr>");
          }
    }

    scoutln(out, bytesOut, "</table>");
    
    di.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void writeMsgLine(PrintWriter out, String author, String date, String title, String link, String[] line, int[] bytesOut)
  {
    try
    {
      if(line[0].equals("line1")) line[0] = "line2"; else line[0] = "line1";

      scoutln(out, bytesOut, "<tr id=\"" + line[0] + "\">");

      scoutln(out, bytesOut, "<td><p>\n" + author + "\n</a></td><td><p><a href=\"" + link+ "\" target=_blank>" + title + "</a></td><td nowrap><p> " + date + "</td></tr>");
    }
    catch(Exception e) { System.out.println("8025: " + e); }
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }
  
}
