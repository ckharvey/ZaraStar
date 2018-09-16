// =======================================================================================================================================================================================================
// System: ZaraStar MailEngine: Mail: Send a mail with GET
// Module: MailZaraMailSendExecuteGET.java
// Author: C.K.Harvey
// Copyright (c) 2001-06 Christopher Harvey. All Rights Reserved.
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
import java.util.*;
import java.io.*;
import java.sql.*;
import javax.mail.*;
import javax.mail.internet.*;

public class MailZaraMailSendExecuteGET extends HttpServlet implements SingleThreadModel
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

    String unm="", sid="", uty="", asu="", men="", den="", dnm="", bnm="", p1="", p2="", p3="", p4="", p5="", p6="";

    try
    {
      directoryUtils.setContentHeaders(res);
      out = res.getWriter();

      unm = req.getParameter("unm");
      sid = req.getParameter("sid");
      uty = req.getParameter("uty");
      den = req.getParameter("den");
      men = req.getParameter("men");
      den = req.getParameter("d");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");
      p1  = req.getParameter("to");
      p2  = req.getParameter("from");
      p3  = req.getParameter("ccList");
      p4  = req.getParameter("subject");
      p5  = req.getParameter("text");
      p6  = req.getParameter("account");

      doIt(out, req, unm, sid, uty, den, men, dnm, bnm, p1, p2, p3, p4, p5, p6, bytesOut);
    }
    catch(Exception e)
    {
      serverUtils.etotalBytes(req, unm, dnm, 8007, bytesOut[0], 0, "ERR:" + p6);
    }
  }

  // ------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String den, String men,
                      String dnm, String bnm, String p1, String p2, String p3,
                    String p4, String p5, String p6, int[] bytesOut) throws Exception
  {
    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    long startTime = new java.util.Date().getTime();

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 8014, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "8007", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8007, bytesOut[0], 0, "ACC:" + p6);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "8007", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8007, bytesOut[0], 0, "SID:" + p6);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }
    
    String text="";
    int textLen = p5.length();

    int y=0;
    while(y < textLen)
    {
      if(p5.charAt(y) == (char)'\003')
      {
        text += '\n';
      }
      else text += p5.charAt(y);
      ++y;
    }

    String[] pop3Server   = new String[1];
    String[] fromAddress  = new String[1];
    String[] userName     = new String[1];
    String[] passWord     = new String[1];
    String[] userFullName = new String[1];
    String[] type         = new String[1];

    p6 = generalUtils.deSanitise(p6); // added 23apr05

    mailUtils.getAccountDetails(p6, unm, dnm, pop3Server, fromAddress, userName, passWord, userFullName, type);

    String res = sendIt(p1, p4, text, p2, p3, pop3Server[0], userFullName[0]);

    String msg;
    if(res.length() == 0)
      msg = "Sent Successfully";
    else msg = "<font size=3 color=\"#FF0000\"><b>Error!</b><br><br></font>Message Not Sent<br><br>(" + res + ")";

    out.println("<body bgcolor=\"#ffffff\" link=\"#0000FF\" alink=\"#0000FF\" vlink=\"#0000FF\"><form>");

    out.println("<table border=0 cellspacing=2 cellpadding=0 width=100%>");

    out.println("<tr><td>&nbsp;</td></tr>");
    out.println("<tr><td><font face=\"Arial,Helvetica\" size=2 color=\"#000000\">"+msg+"</td></tr>");
    out.println("<tr><td>&nbsp;</td></tr>");

    out.println("</table><br>");

    out.println("<table border=0 cellspacing=0 cellpadding=0 width=100%>");
    out.println("<tr><td><img src=\""+imagesDir+"blm2.gif\" width=100% height=3></td></tr>");
    out.println("<tr><td bgcolor=\"#FFFFFF\"><img src=\""+imagesDir+"tscme.jpg\"></td></tr>");

    out.println("</table></form></body></html>");

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 8007, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p6);
    if(con  != null) con.close();
    if(out != null) out.flush();
  }

  // ------------------------------------------------------------------------------------------------------------------------------------------------------
  private String sendIt(String recipient, String subject, String message, String from, String ccList, String pop3Server, String fullName) throws Exception
  {
    System.out.println("Mail to: " + recipient + " From: " + from);

    if(recipient.length() == 0)
      return "No Recipient Specified";

    if(from.length() == 0)
      return "No Sender Specified";

    if(subject.length() == 0)
      return "No Subject Specified";

    if(message.length() == 0)
      return "No Message Specified";

    if(pop3Server.length() == 0)
      return "No POP3 Server Defined";

    return postMail(recipient, subject, message, from, ccList, pop3Server, fullName);
  }

  // ------------------------------------------------------------------------------------------------------------------------------------------------------
  private String postMail(String recipient, String subject, String message, String from, String ccList, String pop3Server, String fullName)
                          throws Exception
  {
    // Set the host smtp address
    Properties props = new Properties();
    props.put("mail.smtp.host", pop3Server);

    // create some properties and get the default Session
    Session session = Session.getDefaultInstance(props, null);
    session.setDebug(false);

    // create a message
    Message msg = new MimeMessage(session);

    // set the from and to address
    InternetAddress addressFrom = new InternetAddress(fullName + "<" + from + ">");
    msg.setFrom(addressFrom);

    InternetAddress[] addressTo = new InternetAddress[1];
    addressTo[0] = new InternetAddress(recipient);
    msg.setRecipients(Message.RecipientType.TO, addressTo);

    // CC list separated by ,; or space
    byte[] ccListB = new byte[1000]; // plenty
    int ccListLen = ccList.length();
    int x=0, y=0, numCC=0;

    while(x < ccListLen && ccList.charAt(x) != ',' && ccList.charAt(x) != ';' && ccList.charAt(x) != ' ')
      ccListB[y++] = (byte)ccList.charAt(x++);
    ccListB[y++] = (byte)'\000';
    if(ccListB[0] == '\000') // no entries
      ;
    else
    {
      ++numCC;
      boolean quit = false;
      while(! quit)
      {
        if(x == ccListLen) // no more entries
          quit = true;
        else
        {
          while(x < ccListLen && (ccList.charAt(x) == ',' || ccList.charAt(x) == ';' || ccList.charAt(x) == ' '))
            ++x;
          if(x == ccListLen) // no more entries
            quit = true;
          else
          {
            while(x < ccListLen && ccList.charAt(x) != ',' && ccList.charAt(x) != ';' && ccList.charAt(x) != ' ')
            ccListB[y++] = (byte)ccList.charAt(x++);
            ccListB[y++] = (byte)'\000';
            ++numCC;
          }
        }
      }

      // now know how many CC entries there are, and have them in a new buffer

      InternetAddress[] addressCC = new InternetAddress[numCC];
      for(x=0;x<numCC;++x)
        addressCC[x] = new InternetAddress(generalUtils.dfsAsStr(ccListB, (short)x));
      msg.setRecipients(Message.RecipientType.CC, addressCC);
    }

    // Setting the Subject and Content Type
    msg.setSubject(subject);

    int len = message.length();
    String message2="";
    for(x=0;x<len;++x)
    {
      if(message.charAt(x) == '\003')
        message2 += "\r\n";
      else message2 += message.charAt(x);
    }

    msg.setContent(message2, "text/plain");

    try
    {
      Transport.send(msg);
    }
    catch(Exception SendFailedException)
    {
      return "Unknown Address (" + SendFailedException + ")";
    }

    return "";
  }

}

