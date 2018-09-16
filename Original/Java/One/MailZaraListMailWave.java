// =======================================================================================================================================================================================================
// System: ZaraStar Mail: List Mail for Wave
// Module: MailZaraListMailWave.java
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

public class MailZaraListMailWave extends HttpServlet
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
      serverUtils.etotalBytes(req, unm, dnm, 8004, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1, int[] bytesOut) throws Exception
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

      if(numMsgs == 0)
      {
        out.print("ZERO:\001");
      }
      else
      {
        boolean quit, first;
        String shorterSentDate, s;
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
                  from += "<";
                else
                if(msgFrom.charAt(x) == '>')
                  from += ">";
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
          else subject = "<none>";

          if(subject.length() == 0)
            subject = "<none>";

          if(generalUtils.stripTrailingSpacesStr(subject).length() == 0)
            subject = "<none>";

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
            sentDate = "<none>";
            shorterSentDate = "<none>";
          }

          out.print(shorterSentDate + "\001" + from + "\001" + msgNum + "\001" + subject + "\001");

          if(attachmentName.length() > 0)
            out.print("ATT:\001");
          else out.print("\001");

          if(domain[0] == null || domain[0].length() == 0)
            ;
          else ++count;
        }
      }

      folder.close(false);
      store.close();
    }
    catch(Exception e)
    {
      try
      {
        out.print("ERR:\001");
        System.out.println("8004w: " + e);
      }
      catch(Exception e2) { }
    }
  }

}
