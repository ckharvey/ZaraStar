// =======================================================================================================================================================================================================
// System: ZaraStar MailEngine: Send the Mail
// Module: MailZaraMailSendExecute.java
// Author: C.K.Harvey
// Copyright (c) 2002-07 Christopher Harvey. All Rights Reserved.
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
import javax.activation.*;

public class MailZaraMailSendExecute extends HttpServlet
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

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="", p3="", p4="", p5="", p6="", p7="", p8="", p9="";

    try
    {
      directoryUtils.setContentHeaders(res);
      out = res.getWriter();

      int thisEntryLen, inc;
      Enumeration en = req.getParameterNames();
      while(en.hasMoreElements())
      {
        String name = (String)en.nextElement();
        String[] value = req.getParameterValues(name);
        if(name.equals("unm"))
          unm = value[0];
        else
        if(name.equals("uty"))
          uty = value[0];
        else
        if(name.equals("sid"))
          sid = value[0];
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
        if(name.equals("dnm"))
          dnm = value[0];
        else
        if(name.equals("to"))
          p1 = value[0];
        else
        if(name.equals("from"))
          p2 = value[0];
        else
        if(name.equals("ccList"))
          p3 = value[0];
        else
        if(name.equals("subject"))
          p4 = value[0];
        else
        if(name.equals("p1"))
          p5 = value[0];
        else
        if(name.equals("account"))
          p6 = value[0];
        else
        if(name.equals("mode"))
          p7 = value[0];
        else
        if(name.equals("msgNum"))
          p8 = value[0];
        else
        if(name.equals("forward"))
          p9 = value[0];
        else
        if(name.length() > 2 && name.substring(1, 3).equals(".x")) ;
        else
        if(name.length() > 2 && name.substring(1, 3).equals(".y")) ;
      }

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, p3, p4, p5, p6, p7, p8, p9, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "MailZaraMailSendExecute", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8007, bytesOut[0], 0, "ERR:" + p6);
      if(out != null) out.flush();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1, String p2, String p3, String p4, String p5, String p6, String p7, String p8, String p9,
                    int[] bytesOut) throws Exception
  {
    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String workingMailDir   = directoryUtils.getUserDir('W', dnm, unm) + "Mail/";
     
    long startTime = new java.util.Date().getTime();

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 8014, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "MailZaraMailSend", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8007, bytesOut[0], 0, "ACC:" + p6);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "MailZaraMailSend", imagesDir, localDefnsDir, defnsDir, bytesOut);
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
      if(p5.charAt(y) == '\003')
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

    p6 = generalUtils.deSanitise(p6);

    mailUtils.getAccountDetails(p6, unm, dnm, pop3Server, fromAddress, userName, passWord, userFullName, type);

    String res = sendIt(p1, p4, text, p2, p3, pop3Server[0], userFullName[0], p7, userName[0], passWord[0], p8, workingMailDir, p9);

    out.println("<html><head><title>Mail Transmission</title>");

    String cssDirectory = directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir);
            
    out.println("<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + cssDirectory + "general.css\">");
    
    int[] hmenuCount = new int[1];
    
    mailUtils.outputPageFrame(con, stmt, rs, out, req, false, p6, "", "MailZaraMailSendExecute", "8007", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount, bytesOut);
  
    mailUtils.drawTitle(con, stmt, rs, req, out, false, p6, "Compose Mail", "8007", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    out.println("<table id=\"generalMailMessagePage\" width=100%>");

    out.println("<tr><td><br><br>");
    
    if(res.length() == 0)
      out.println("<h1> Sent Successfully");
    else out.println("<h2>Error!<br><br>Message Not Sent<br><br>(" + res + ")");

    out.println("<br><br><br><br><br></td></tr></table>");
 
    out.println("</form></div></body></html>");
    
    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 8007, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p6);
    if(con  != null) con.close();
    if(out != null) out.flush();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private String sendIt(String recipient, String subject, String message, String from, String ccList, String pop3Server, String fullName,
                        String htmlOrNot, String userName, String passWord, String msgNum, String workingMailDir, String forward) throws Exception
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

    return postMail(recipient, subject, message, from, ccList, pop3Server, fullName, htmlOrNot, userName, passWord, msgNum, workingMailDir, forward);
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private String postMail(String recipient, String subject, String message, String from, String ccList, String pop3Server, String fullName,
                          String htmlOrNot, String userName, String passWord, String msgNum, String workingMailDir, String forward) throws Exception
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
    
    msg.addHeader("X-Mailer", "Zara Mail");
    
    // options:
    // 1. Forwarding a mail. Original mail is forwarded with (optional) additional text.
    // 2. Forwarding a mail. Original mail is forwarded with (optional) additional text, plus (optional) attachment(s).
    // 3. Replying to a mail. Original mail is editable, plus (optional) attachments.
    
     Store store = null;
     Folder folder = null;
        
    // attachments are *all* files in the user's /Working/Mail directory.
    
    File path = new File(workingMailDir);
    String fs[] = new String[0];
    fs = path.list();

    if(forward.equals("Y") || fs.length > 0) // forwarding and/or attachments exist, so must be multipart
    {
      MimeBodyPart newMessageBit = new MimeBodyPart();
      if(htmlOrNot.equals("Y"))
        newMessageBit.setContent(message2, "text/html");
      else newMessageBit.setContent(message2, "text/plain");
        
      Multipart multipartMessage = new MimeMultipart();
      multipartMessage.addBodyPart(newMessageBit);

      DataSource fileDataSource;
      DataHandler fileDataHandler;
      MimeBodyPart fileAttachment;
      for(x=0;x<fs.length;++x)
      {
        if(fs[0].equalsIgnoreCase("__libperm.cfs") || fs[0].equalsIgnoreCase("__libaccess.log"))
          ;
        else
        {
          fileDataSource  = new FileDataSource(workingMailDir + fs[x]);
          fileDataHandler = new DataHandler(fileDataSource);
    
          fileAttachment = new MimeBodyPart();
          fileAttachment.setDataHandler(fileDataHandler);

          multipartMessage.addBodyPart(fileAttachment);
        } 
      }

      if(forward.equals("Y"))
      {
        int msgNumI = generalUtils.strToInt(msgNum);
    
        store = session.getStore("pop3");
        store.connect(pop3Server, userName, passWord);

        folder = store.getFolder("INBOX");
        folder.open(Folder.READ_ONLY);

        Message msg2[] = folder.getMessages();
      
        MimeBodyPart oldMsg = new MimeBodyPart();
        oldMsg.setContent(msg2[msgNumI].getContent(), msg2[msgNumI].getContentType());
    
        multipartMessage.addBodyPart(oldMsg);
        msg.setContent(multipartMessage);
      }
    }
    else // not multipart
    {
      if(htmlOrNot.equals("Y"))
        msg.setContent(message2, "text/html");
     else msg.setContent(message2, "text/plain");
    }
     
    try
    {
      Transport.send(msg);
    }
    catch(Exception e)
    {   
      if(folder != null)
        folder.close(false);
      if(store != null)
        store.close();
      return "Error Sending Message (8007a): " + e.getMessage(); 
    }

    if(folder != null) folder.close(false);
    if(store  != null) store.close();

    return "";
  }

}
