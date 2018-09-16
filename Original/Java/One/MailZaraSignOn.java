// =======================================================================================================================================================================================================
// System: ZaraStar Mail: SignOn
// Module: MailZaraSignOn.java
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

import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.io.*;
import java.net.*;
import javax.mail.*;

public class MailZaraSignOn extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
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
      men = req.getParameter("men");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, bytesOut);
    }
    catch(Exception e)
    {
      StringBuffer url = req.getRequestURL();
      String urlBit="";
      int x=0;
      if(url.charAt(x) == 'h' || url.charAt(x) == 'H')
        x += 7;
      while(url.charAt(x) != '\000' && url.charAt(x) != ',' && url.charAt(x) != '/')
        urlBit += url.charAt(x++);

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "MailZaraSignOn", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8014, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush();
    }
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    String uName = directoryUtils.getMySQLUserName();
    String pWord = directoryUtils.getMySQLPassWord();
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Connection conMail = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_mail?user=" + uName + "&password=" + pWord);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 8014, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "MailZaraSignOn", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8014, bytesOut[0], 0, "ACC:");
      if(conMail != null) conMail.close();
      if(con     != null) con.close();
      if(out != null) out.flush();
      return;
    }
    
    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "MailZaraSignOn", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8014, bytesOut[0], 0, "SID:");
      if(conMail != null) conMail.close();
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    set(con, conMail, stmt, rs, out, req, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 8014, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(conMail != null) conMail.close();
    if(con  != null) con.close();
    if(out != null) out.flush();
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Connection conMail, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir,
                   String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Mail Account Selection</title>");
   
    String cssDirectory = directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir);
            
    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + cssDirectory + "general.css\">");
    
    scoutln(out, bytesOut, "<script javascript=\"JavaScript\">");
   
    scoutln(out, bytesOut, "function access(account){var p1=sanitise(account);");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/MailZaraListMail?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm="
                         + dnm + "&bnm=" + bnm + "&p1=\"+p1;}");

    scoutln(out, bytesOut, "function accessG(account){var p1=sanitise(account);");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/MailZaraListGmail?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm="
                         + dnm + "&bnm=" + bnm + "&p1=\"+p1;}");

    scoutln(out, bytesOut, "function sanitise(code){");
    scoutln(out, bytesOut, "var code2='';var x;var len=code.length;");
    scoutln(out, bytesOut, "for(x=0;x<len;++x)");
    scoutln(out, bytesOut, "if(code.charAt(x)=='#')code2+='%23';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='\"')code2+='%22';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='&')code2+='%26';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='%')code2+='%25';");
    scoutln(out, bytesOut, "else if(code.charAt(x)==' ')code2+='%20';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='?')code2+='%3F';");
    scoutln(out, bytesOut, "else code2+=code.charAt(x);");
    scoutln(out, bytesOut, "return code2};");

    scoutln(out, bytesOut, "</script>");

    int[] hmenuCount = new int[1];

    mailUtils.outputPageFrame(con, stmt, rs, out, req, true, "", "", "MailZaraSignOn", "8014", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount, bytesOut);

    mailUtils.drawTitle(con, stmt, rs, req, out, true, "", "Mail Accounts", "8014", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
     
    scoutln(out, bytesOut, "<table id=\"page\" width=100%>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap><p><b>Select an Account:</b></td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "</table><table id=\"page\" border=\"0\" cellspacing=\"3\" cellpadding=\"3\" width=100%>");

    scoutln(out, bytesOut, "<tr id=\"pageColumn\"><td><p><b>Account</b></td>");
    scoutln(out, bytesOut, "<td align=center width=90%><p><b> &nbsp;Messages&nbsp; </b></td></tr>");

    getPOP3Accounts(conMail, stmt, rs, out, unm, uty, sid, men, den, dnm, bnm, localDefnsDir, bytesOut);

    getGMailAccounts(conMail, stmt, rs, out, unm, uty, sid, men, den, dnm, bnm, localDefnsDir, bytesOut);
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "</div>"); // main

    scoutln(out, bytesOut, "</table>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void getPOP3Accounts(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String unm, String uty, String sid, String men, String den,
                               String dnm, String bnm, String localDefnsDir, int[] bytesOut)
  {
    String account, pop3Server, userName, passWord;

    Properties props;
    Session session;
    Store store = null;
    Folder folder = null;
    int numMsgs;

    try
    {
      stmt = con.createStatement();
  
      rs = stmt.executeQuery("SELECT AccountName, Server, UserName, PassWord FROM mailaccounts WHERE Owner = '" + unm + "' AND Type = 'POP3'");

      while(rs.next())                  
      {
        account    = rs.getString(1);
        pop3Server = rs.getString(2);
        userName   = rs.getString(3);
        passWord   = rs.getString(4);

        try
        {
          props = new Properties();
          session = Session.getDefaultInstance(props, null);

          store = session.getStore("pop3");
          store.connect(pop3Server, userName, passWord);

          folder = store.getFolder("INBOX");

          folder.open(Folder.READ_ONLY);

          numMsgs = folder.getMessageCount();

          folder.close(false);
          store.close();

          scoutln(out, bytesOut, "<tr><td nowrap align=\"left\"><p><a href=\"javascript:access('" + account + "')\">" + account + "</a></td>");
          scoutln(out, bytesOut, "<td align=\"center\"><p> &nbsp;" + numMsgs + "</td></tr>");
        }
        catch(Exception e) // catch login failures
        {
          try
          {
            scoutln(out, bytesOut, "<tr><td nowrap><p>" + account + "</td>");
            scoutln(out, bytesOut, "<td align=center><p>Server Busy or Unavailable</td></tr>");
          }  
          catch(Exception e2) { } 
        }
      }
      
      if(rs != null) rs.close();
      if(stmt != null) stmt.close();        
    }
    catch(Exception e)
    {
      System.out.println("mailUtils: " + e);
      try
      {
        if(rs   != null) rs.close();
        if(stmt != null) stmt.close();
      }
      catch(Exception e2) { }
    }
  }
   
  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void getGMailAccounts(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String unm, String uty, String sid, String men, String den,
                                String dnm, String bnm, String localDefnsDir, int[] bytesOut)
  {
    String account, userName, passWord;

    try
    {
      stmt = con.createStatement();
  
      rs = stmt.executeQuery("SELECT AccountName, UserName, PassWord FROM mailaccounts WHERE Owner = '" + unm + "' AND Type = 'GMail'");

      while(rs.next())                  
      {
        account    = rs.getString(1);
        userName   = rs.getString(2);
        passWord   = rs.getString(3);

        try
        {
          getGMail(out, account, userName, passWord, "N", unm, uty, sid, men, den, dnm, bnm, localDefnsDir, bytesOut);
        }
        catch(Exception e) // catch login failures
        {
          System.out.println(e);
        }
      }
      
      if(rs != null) rs.close();
      if(stmt != null) stmt.close();        
    }
    catch(Exception e)
    {
      System.out.println("mailUtils: " + e);
      try
      {
        if(rs   != null) rs.close();
        if(stmt != null) stmt.close();
      }
      catch(Exception e2) { }
    }
  }
   
  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void getGMail(PrintWriter out, String account, String userName, String passWord, String mode, String unm, String uty, String sid, String men, String den, String dnm, String bnm, String localDefnsDir, int[] bytesOut) throws Exception
  { 
    URL url = new URL("http://" + serverUtils.serverToCall("MAIL", localDefnsDir) + "/central/servlet/MailZaraGetGmailATOM?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=" + generalUtils.sanitise(userName) + "&p2="
                    + generalUtils.sanitise(passWord) + "&p3=" + mode + "&bnm=" + bnm);
    
    URLConnection uc = url.openConnection();
    uc.setDoInput(true);
    uc.setUseCaches(false);
    uc.setDefaultUseCaches(false);

    uc.setRequestProperty("Content-Type", "application/octet-stream");

    BufferedReader di = new BufferedReader(new InputStreamReader(uc.getInputStream()));

    String s = di.readLine();
    if(s != null)
    {
      int x=0, len = s.length();
      String numMsgs = "";
      while(x < len && s.charAt(x) != '\001')
        numMsgs += s.charAt(x++);
     
      if(numMsgs.equals("X"))
      {
        scoutln(out, bytesOut, "<tr><td nowrap><p>" + account + "</td><td align=center><p>Server Busy or Unavailable</td></tr>");
      }
      else
      {
        scoutln(out, bytesOut, "<tr><td nowrap align=\"left\"><p><a href=\"javascript:accessG('" + account + "')\">" + account + "</a></td><td align=\"center\"><p> &nbsp;" + numMsgs + "</td></tr>");
      }
    }
    else
    {
      scoutln(out, bytesOut, "<tr><td nowrap><p>" + account + "</td>");
      scoutln(out, bytesOut, "<td align=center><p>Server Busy or Unavailable</td></tr>");
    }
    
    di.close();
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }
  
}
