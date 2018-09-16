// =======================================================================================================================================================================================================
// System: ZaraStar MailEngine: Send Mail from servlet
// Module: MailZaraSendMailServlet.java
// Author: C.K.Harvey
// Copyright (c) 2000-09 Christopher Harvey. All Rights Reserved.
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
import javax.mail.internet.*;
import java.util.*;

public class MailZaraSendMailServlet extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  MailUtils mailUtils = new MailUtils();
  Profile profile = new Profile();
 
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", companyCode="", companyType="", projectCode="", to="", from="", ccList="", bccList="", subject="", text="", senderMailAddr = "", sendType = "";

    try
    {
      directoryUtils.setContentHeaders(res);
      out = res.getWriter();

      Enumeration en = req.getParameterNames();
  
      {
        String name;
        String[] value;

        while(en.hasMoreElements())
        {
          name = (String)en.nextElement();
          value = req.getParameterValues(name);

          value[0] = generalUtils.deSanitise(value[0]);

          if(name.equals("unm"))
            unm = value[0];
          else
          if(name.equals("sid"))
            sid = value[0];
          else
          if(name.equals("uty"))
            uty = value[0];
          else
          if(name.equals("men"))
            men = value[0];
          else
          if(name.equals("den"))
            den = value[0];
          else
          if(name.equals("dnm"))
            dnm = value[0];
          else
          if(name.equals("bnm"))
            bnm = value[0];
          else
          if(name.equals("companyCode"))
            companyCode = value[0];
          else
          if(name.equals("companyType")) 
            companyType = value[0];
          else
          if(name.equals("projectCode"))
            projectCode = value[0];
          else
          if(name.equals("to"))
            to = value[0];
          else
          if(name.equals("from"))
            from = value[0];
          else
          if(name.equals("ccList"))
            ccList = value[0];
          else
          if(name.equals("bccList"))
            bccList = value[0];
          else
          if(name.equals("senderMailAddr"))
            senderMailAddr = value[0];
          else
          if(name.equals("subject"))
            subject = value[0];
          else
          if(name.equals("text"))
            text = value[0];
          else
          if(name.equals("sendType"))
            sendType = value[0];
        }  
      }

      companyCode = generalUtils.stripLeadingAndTrailingSpaces(companyCode);
      companyType = generalUtils.stripLeadingAndTrailingSpaces(companyType);
      projectCode = generalUtils.stripLeadingAndTrailingSpaces(projectCode);
      to          = generalUtils.stripLeadingAndTrailingSpaces(to);
      from        = generalUtils.stripLeadingAndTrailingSpaces(from);
      ccList      = generalUtils.stripLeadingAndTrailingSpaces(ccList);
      bccList     = generalUtils.stripLeadingAndTrailingSpaces(bccList);

      if(men == null) men = "";

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, senderMailAddr, companyCode, companyType, projectCode, to, from, ccList, bccList, subject, text, sendType, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "MailZaraSendMailServlet", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8015, bytesOut[0], 0, "ERR:" + to);
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String senderMailAddr, String companyCode, String companyType, String projectCode, String to,
                   String from, String ccList, String bccList, String subject, String text, String sendType, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String mailServer    = serverUtils.serverToCall("SMTP", localDefnsDir);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 8014, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "MailZaraSendMailServlet", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8015, bytesOut[0], 0, "ACC:" + to);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "MailZaraSendMailServlet", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8015, bytesOut[0], 0, "SID:" + to);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    try
    {
      postMail(con, stmt, rs, senderMailAddr, to, subject, text, from, ccList, bccList, mailServer, companyCode, companyType, projectCode, sendType, dnm, localDefnsDir, defnsDir);
    }
    catch(Exception e)
    {
      System.out.println(e);
      messagePage.msgScreen(false, out, req, 39, unm, sid, uty, men, den, dnm, bnm, "8015", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 8015, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), to);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    messagePage.msgScreen(false, out, req, 9, unm, sid, uty, men, den, dnm, bnm, "8015", imagesDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 8015, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), to);
    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void postMail(Connection con, Statement stmt, ResultSet rs, String senderMailAddr, String recipient, String subject, String message, String from, String ccList, String bccList, String smtpServer, String companyCode, String companyType,
                        String projectCode, String sendType, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    // Set the host smtp address
    Properties props = new Properties();
    props.put("mail.smtp.host", smtpServer);

    // create some properties and get the default Session
    Session session = Session.getDefaultInstance(props, null);
    session.setDebug(false);

    // create a message
    Message msg = new MimeMessage(session);

    String fullName = profile.getNameFromProfile(con, stmt, rs, from);

    // set the from and to address
    InternetAddress addressFrom = new InternetAddress(fullName + "<" + senderMailAddr + ">");
    msg.setFrom(addressFrom);

    InternetAddress[] addressTo = new InternetAddress[1];

    addressTo[0] = new InternetAddress(recipient);
    msg.setRecipients(Message.RecipientType.TO, addressTo);

    // CC list separates by ,; or space
    byte[] ccListB = new byte[1000]; // plenty
    int ccListLen = ccList.length();
    int x=0, y=0, numCC=0;

    while(x < ccListLen && ccList.charAt(x) != ',' && ccList.charAt(x) != ';' && ccList.charAt(x) != ' ' && ccList.charAt(x) != '\n')
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
          while(x < ccListLen && (ccList.charAt(x) == ',' || ccList.charAt(x) == ';' || ccList.charAt(x) == ' ' && ccList.charAt(x) != '\n'))
            ++x;
          if(x == ccListLen) // no more entries
            quit = true;
          else
          {
            while(x < ccListLen && ccList.charAt(x) != ',' && ccList.charAt(x) != ';' && ccList.charAt(x) != ' ' && ccList.charAt(x) != '\n')
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

    // BCC list separates by ,; or space
    byte[] bccListB = new byte[1000]; // plenty
    int bccListLen = bccList.length();
    x = y = 0;
    int numBCC = 0;

    while(x < bccListLen && bccList.charAt(x) != ',' && bccList.charAt(x) != ';' && bccList.charAt(x) != ' ' && bccList.charAt(x) != '\n')
      bccListB[y++] = (byte)bccList.charAt(x++);
    bccListB[y++] = (byte)'\000';
    if(bccListB[0] == '\000') // no entries
      ;
    else
    {
      ++numBCC;
      boolean quit = false;
      while(! quit)
      {
        if(x == bccListLen) // no more entries
          quit = true;
        else
        {
          while(x < bccListLen && (bccList.charAt(x) == ',' || bccList.charAt(x) == ';' || bccList.charAt(x) == ' ' && bccList.charAt(x) != '\n'))
            ++x;
          if(x == bccListLen) // no more entries
            quit = true;
          else
          {
            while(x < bccListLen && bccList.charAt(x) != ',' && bccList.charAt(x) != ';' && bccList.charAt(x) != ' ' && bccList.charAt(x) != '\n')
              bccListB[y++] = (byte)bccList.charAt(x++);
            bccListB[y++] = (byte)'\000';
            ++numBCC;
          }
        }
      }

      // now know how many BCC entries there are, and have them in a new buffer

      InternetAddress[] addressBCC = new InternetAddress[numBCC];
      for(x=0;x<numBCC;++x)
        addressBCC[x] = new InternetAddress(generalUtils.dfsAsStr(bccListB, (short)x));
      msg.setRecipients(Message.RecipientType.BCC, addressBCC);
    }

    // Setting the Subject and Content Type
    msg.setSubject(subject);

    msg.setContent(message, "text/html");

    Transport.send(msg);
    
    // save to library
    String date = generalUtils.todaySQLFormat(localDefnsDir, defnsDir);

    mailUtils.addToMail("", "S", senderMailAddr, recipient, subject, "", message, ccList, bccList, companyCode, companyType, projectCode, date, sendType, dnm, localDefnsDir, defnsDir);
  }

}
