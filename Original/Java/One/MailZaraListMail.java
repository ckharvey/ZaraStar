// =======================================================================================================================================================================================================
// System: ZaraStar MailEngine: List Mail
// Module: MailZaraListMail.java
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
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import java.util.*;

public class MailZaraListMail extends HttpServlet
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
      p1  = req.getParameter("p1");

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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "MailZaraListMail", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8004, bytesOut[0], 0, "ERR:" + p1);
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
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "MailZaraListMail", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8004, bytesOut[0], 0, "ACC:" + p1);
      
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "MailZaraListMail", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8004, bytesOut[0], 0, "SID:" + p1);
      
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    checkMail(con, stmt, rs, out, req, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, p1, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 8004, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    
    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void checkMail(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir,
                         String account, int[] bytesOut)
  {
    try
    {
      scoutln(out, bytesOut, "<html><head><title>Mail: List Mail</title>");

      scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");
    }
    catch(Exception e2) { }

    try
    {
      String[] pop3Server   = new String[1];
      String[] fromAddress  = new String[1];
      String[] userName     = new String[1];
      String[] passWord     = new String[1];
      String[] userFullName = new String[1];
      String[] type         = new String[1];
      mailUtils.getAccountDetails(account, unm, dnm, pop3Server, fromAddress, userName, passWord, userFullName, type);

      Properties props = new Properties();
      Session session = Session.getDefaultInstance(props, null);
      Store store = session.getStore("pop3");
      store.connect(pop3Server[0], userName[0], passWord[0]);
      Folder folder = store.getFolder("INBOX");
      folder.open(Folder.READ_ONLY);

      int numMsgs = folder.getMessageCount();

      Message message[] = folder.getMessages();

      scoutln(out, bytesOut, "<script javascript=\"JavaScript\">");

      scoutln(out, bytesOut, "function refetch(){");
      scoutln(out, bytesOut, "this.location.replace(\"/central/servlet/MailZaraListMail?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + generalUtils.sanitise(account) +"\");}");

      scoutln(out, bytesOut, "function view(msgNum){");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/MailZaraMailView?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + generalUtils.sanitise(account) + "&p2=\"+msgNum;}");

      scoutln(out, bytesOut, "function del(){document.messages.submit();}");

      scoutln(out, bytesOut, "function checkAll(){");
      scoutln(out, bytesOut, "var e,x;for(x=0;x<document.forms[0].length;++x)");
      scoutln(out, bytesOut, "{e=document.forms[0].elements[x];");
      scoutln(out, bytesOut, "if(e.type=='checkbox' && e.name != 'all')");
      scoutln(out, bytesOut, "if(e.checked)");
      scoutln(out, bytesOut, "e.checked=false;");
      scoutln(out, bytesOut, "else e.checked=true;}}");

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

      mailUtils.outputPageFrame(con, stmt, rs, out, req, false, account, "", "MailZaraListMail", "8004", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount, bytesOut);

      scoutln(out, bytesOut, "<form action=\"MailZaraMailDelete\" name=messages ENCTYPE=\"application/x-www-form-urlencoded\" method=POST>");
      scoutln(out, bytesOut, "<INPUT TYPE=hidden name=unm value=" + unm + ">");
      scoutln(out, bytesOut, "<INPUT TYPE=hidden name=sid value=" + sid + ">");
      scoutln(out, bytesOut, "<INPUT TYPE=hidden name=uty value=" + uty + ">");
      scoutln(out, bytesOut, "<INPUT TYPE=hidden name=men value=" + men + ">");
      scoutln(out, bytesOut, "<INPUT TYPE=hidden name=den value=" + den + ">");
      scoutln(out, bytesOut, "<INPUT TYPE=hidden name=dnm value=" + dnm + ">");
      scoutln(out, bytesOut, "<INPUT TYPE=hidden name=bnm value=" + bnm + ">");
      scoutln(out, bytesOut, "<INPUT TYPE=hidden name=p1  value=" + generalUtils.sanitise(account) + ">");

      // must set return page for a 'menu' call to MailZaraSignOn cos will lose track of which mail account are currently in
      
      mailUtils.drawTitle(con, stmt, rs, req, out, false, account, "List Mail", "8004", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
      
      scoutln(out, bytesOut, "<table id=\"generalMailMessageHeader\" width=100%>");
      
      String s;
      switch(numMsgs)
      {
        case 0  : s = "no messages.";                            break;
        case 1  : s = "the following message:";                  break;
        default : s = "the following " + numMsgs + " messages:"; break;
      }

      scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
      scoutln(out, bytesOut, "<tr><td colspan=3><p>Your <b>" + account + "</b> inbox contains " + s + "</td></tr>");
      scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

      if(numMsgs == 0)
      {
        scoutln(out, bytesOut, "<tr><td colspan=4><br><br><span id=\"textRedHighlighting\"><b>&nbsp;&nbsp;&nbsp;Empty</b></span><br><br><br></td></tr></table>");
      }
      else
      {
        scoutln(out, bytesOut, "</table><table id=\"pageColumn\" width=100%>");
        drawListingHeader(out, bytesOut);
        scoutln(out, bytesOut, "</table>");

        scoutln(out, bytesOut, "<table id=\"page\" cellspacing=2 cellpadding=0 width=100%>");

        scoutln(out, bytesOut, "<tr id=\"pageColumn\"><td><p><input type=checkbox name=all onClick=\"checkAll()\"></td>");
        if(bnm .equals("B"))
        {
          scoutln(out, bytesOut, "<td></td></tr>");          
        }
        else
        {
          scoutln(out, bytesOut, "<td><p><b>From</td>");
          scoutln(out, bytesOut, "<td><p><b>Subject</td>");
          scoutln(out, bytesOut, "<td><p><b>Date</td></tr>");
        }

        boolean quit, first;
        String shorterSentDate, line = "";
        String[] domain = new String[1];

        Multipart mp;
        int msgNum, x, z, len, count=0;
        Part part;
        String from, msgFrom, attachmentName, disposition, subject, sentDate, to;

        int n = message.length;
        for(int i=0;i<n;++i)
        {
          from = "";

          try // to catch empty 'from'
          {
            if(message[i].getFrom()[0] != null)
            {
              for(x=0;x<(message[i].getFrom()[0]).toString().length();++x)
              {
                msgFrom = message[i].getFrom()[0].toString();
                if(msgFrom.charAt(x) == '<')
                  from += "&lt;";
                else
                if(msgFrom.charAt(x) == '>')
                  from += "&gt;";
                else from += msgFrom.charAt(x);
              }
            }
          }
          catch(Exception e) { System.out.println("FROM: " + e + "(Message Number: " + (message[i].getMessageNumber() - 1) + ")"); from = ""; }
          
          to="";
          try
          {
            Address[] a;
  
            if((a = message[i].getRecipients(Message.RecipientType.TO)) != null)
            {
              for(x=0;x<a.length;++x)
              {
                s = a[x].toString();
                len = s.length();
                z=0;
                while(z < len)
                {
                  if(s.charAt(z) != '"')
                    to += ("" + s.charAt(z));
                  ++z;
                }
                to += " ";
                InternetAddress ia = (InternetAddress)a[x];
                if(ia.isGroup())
                {
                  InternetAddress[] aa = ia.getGroup(false);
                  for(int y=0;y<aa.length;++y)
                  {    
                    s = aa[y].toString();
                    len = s.length();
                    z=0;
                    while(z < len)
                    {
                      if(s.charAt(z) != '"')
                        to += ("" + s.charAt(z));
                      ++z;
                    }
                    to += " ";
                  }  
                }
              }
            }
          }
          catch(Exception e) { System.out.println("TO: " + e); to="Undisclosed Recipient"; System.out.println("noting: " + to); }

          msgNum = (message[i].getMessageNumber() - 1);
 
          attachmentName="";

          first = true;
          try
          {
            if(message[i].isMimeType("multipart/*"))
            {
              mp = (Multipart)message[i].getContent();
            }
          }
          catch(Exception e) { attachmentName = "Illegal Attachment"; System.out.println("noting: " + attachmentName); }

          if(message[i].getSubject() != null)
          {
            s = message[i].getSubject().toString();
            len = s.length();
            z=0;
            subject="";
            while(z < len)
            {
              if(s.charAt(z) == '\'')
                subject += '`';
              else
              if(s.charAt(z) == '"')
                subject += '`';
              else subject += s.charAt(z);
              ++z;
            }
          }
          else subject = "&lt;none&gt;";

          if(subject.length() == 0)
            subject = "&lt;none&gt;";

          if(generalUtils.stripTrailingSpacesStr(subject).length() == 0)
            subject = "&lt;none&gt;";

          if(message[i].getSentDate() != null)
            sentDate = message[i].getSentDate().toString();
          else sentDate = "";
          shorterSentDate = "";
          x=z=0;
          quit = false;
          while(! quit && x < sentDate.length())
          {
            if(sentDate.charAt(x) == ' ')
            {
              ++z;
              if(z == 4)
                quit = true;
            }

            shorterSentDate += sentDate.charAt(x++);
          }

          if(shorterSentDate.length() == 0)
          {
            sentDate = "&lt;none&gt;";
            shorterSentDate = "&lt;none&gt;";
          }

          if(line.equals("line1")) line = "line2"; else line = "line1";

          scoutln(out, bytesOut, "<tr id=\"" + line + "\"><td><input type=checkbox name=" + i + "\001" + generalUtils.sanitise(sentDate) + "></td>");

          if(bnm.equals("B"))
          {
            scoutln(out, bytesOut, "<td><p>\n" + from + "\n</a><br><a title=\"To: "+ to + "\" href=\"javascript:view('" + msgNum + "')\">" + subject + "</a>\n");

            if(attachmentName.length() > 0)
              scoutln(out, bytesOut, "<br><span id=\"textRedHighlighting\">Attachment: "+ attachmentName + "</span>");

            scoutln(out, bytesOut, "<br>" + shorterSentDate + "</td></tr>");
          }
          else
          {
            scoutln(out, bytesOut, "<td><p>\n" + from + "\n</a></td><td><p><a title=\"To: "+ to + "\" href=\"javascript:view('" + msgNum + "')\">" + subject + "</a>\n");

            if(attachmentName.length() > 0)
              scoutln(out, bytesOut, "<br><span id=\"textRedHighlighting\">Attachment: "+ attachmentName + "</span>");

            scoutln(out, bytesOut, "</td><td nowrap><p> " + shorterSentDate + "</td></tr>");
          }
        
          if(domain[0] == null || domain[0].length() == 0)
            ;
          else ++count;
        }

        scoutln(out, bytesOut, "</table>");
    
        if(numMsgs > 0)
        {
          scoutln(out, bytesOut, "<table id=\"pageColumn\" width=100%>");
          drawListingHeader(out, bytesOut);
          scoutln(out, bytesOut, "</table>");
        }
      }

      folder.close(false);
      store.close();
    }
    catch(Exception e)
    {
      try
      {
        scoutln(out, bytesOut, "<tr><td colspan=4><span id=\"textRedHighlighting\"><b>Error: Cannot read mail.<br><br>Try again later.<br><br>(" + e +")</b></span></td></tr>");    ///// replace by complete screen
      }
      catch(Exception e2) { }
    }
    
    try
    {
      scoutln(out, bytesOut, "</table></form>");     scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
    }
    catch(Exception e2) { } 
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void drawListingHeader(PrintWriter out, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<tr><td nowrap><p>&nbsp<a href=\"javascript:del()\">Delete</a>");
    scoutln(out, bytesOut, "&nbsp;&nbsp; | &nbsp;&nbsp;");
    scoutln(out, bytesOut, "</td></tr>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }
  
}
