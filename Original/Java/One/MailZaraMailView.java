// =============================================================================================================================================================
// System: ZaraStar MailEngine: Zara Mail: View a Mail
// Module: MailZaraMailView.java
// Author: C.K.Harvey
// Copyright (c) 2000-08 Christopher Harvey. All Rights Reserved.
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
import javax.mail.*;
import javax.mail.internet.*;
import java.util.*;
import java.sql.*;

public class MailZaraMailView extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  MailUtils mailUtils = new MailUtils();
  
  static int attnum = 1;
  static int level = 0;

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="";

    try
    {
      directoryUtils.setContentHeaders(res);
      out = res.getWriter();

      unm = req.getParameter("unm");
      uty = req.getParameter("uty");
      sid = req.getParameter("sid");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");
      p1  = req.getParameter("p1"); // account
      p2  = req.getParameter("p2"); // msgNum

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "MailZaraMailView", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8005, bytesOut[0], 0, "ERR:" + p1 + ":" + p2);
      if(out != null) out.flush();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm,
                    String bnm, String p1, String p2, int[] bytesOut) throws Exception
  {
    String dataDir          = directoryUtils.getUserDir('U', dnm, unm);
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');
    String workingMailDir   = directoryUtils.getUserDir('W', dnm, unm) + "Mail/";

    long startTime = new java.util.Date().getTime();

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 8014, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "MailZaraMailView", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8005, bytesOut[0], 0, "ACC:" + p1 + ":" + p2);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "MailZaraMailView", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8005, bytesOut[0], 0, "SID:" + p1 + ":" + p2);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }    

    if(! generalUtils.isDirectory(directoryUtils.getUserDir('W', dnm, unm)))
      generalUtils.createDir(directoryUtils.getUserDir('W', dnm, unm), "755");

    generalUtils.directoryHierarchyDelete(workingMailDir);

    if(! generalUtils.isDirectory(workingMailDir))
      generalUtils.createDir(workingMailDir, "755");

    viewMail(con, stmt, rs, out, req, unm, sid, uty, men, den, dnm, bnm, imagesDir, workingMailDir, dataDir, localDefnsDir, defnsDir, p1, p2, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 8005, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1 + ":" + p2);
    if(con  != null) con.close();
    if(out != null) out.flush();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void viewMail(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm,
                        String bnm, String imagesDir, String workingMailDir, String dataDir, String localDefnsDir, String defnsDir, String account,
                        String msgNum, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>View Mail</title>");

    String cssDirectory = directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir);
            
    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + cssDirectory + "general.css\">");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    scoutln(out, bytesOut, "function del(p2,p3){");
    scoutln(out, bytesOut, "this.location.replace(\"/central/servlet/MailZaraMailDelete?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + generalUtils.sanitise(account)
                         + "&p3=\"+sanitise(p3)+\"&p2=\"+p2);}");

    scoutln(out, bytesOut, "function toLib(p2,p3){");
    scoutln(out, bytesOut, "this.location.replace(\"/central/servlet/_8009?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + generalUtils.sanitise(account)
                         + "&p3=\"+sanitise(p3)+\"&p2=\"+p2);}");

    scoutln(out, bytesOut, "function unattach(fName){");
    scoutln(out, bytesOut, "document.forms[0].fileName.value=fName;");
    scoutln(out, bytesOut, "document.attachment.submit();}");
    
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

    mailUtils.outputPageFrame(con, stmt, rs, out, req, false, "", "", "MailZaraMailView", "8005", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount, bytesOut);

    mailUtils.drawTitle(con, stmt, rs, req, out, false, account, "View Mail", "8005", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
 
    String[] pop3Server   = new String[1];
    String[] fromAddress  = new String[1];
    String[] userName     = new String[1];
    String[] passWord     = new String[1];
    String[] userFullName = new String[1];
    String[] type         = new String[1];
    mailUtils.getAccountDetails(account, unm, dnm, pop3Server, fromAddress, userName, passWord, userFullName, type);

    int msgNumI = generalUtils.strToInt(msgNum);

    Properties props = new Properties();
    Session session = Session.getDefaultInstance(props, null);

    Store store = session.getStore("pop3"); // or imap TODO
    store.connect(pop3Server[0], userName[0], passWord[0]);

    Folder folder = store.getFolder("INBOX");
    folder.open(Folder.READ_ONLY);

    Message message[] = folder.getMessages();

    scoutln(out, bytesOut, "<form name=\"attachment\" enctype=\"multipart/form-data\" action=\"" + directoryUtils.getScriptsDirectory() + "unattach.php\" method=\"post\">");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"workingDir\" value='" + workingMailDir + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"fileName\">");
   
    try // in case backing onto this page after having deleted the msg
    {
      int[]    outputCount = new int[1];    outputCount[0] = 0;
      int[]    imageCount  = new int[1];    imageCount[0] = 0;
      String[] imageList    = new String[1]; imageList[0] = "";
      String[] usedImageList = new String[1]; usedImageList[0] = "";

      determineImageAttachments(message[msgNumI], workingMailDir, imageList, imageCount);

      extractPart(out, message[msgNumI], msgNumI, imagesDir, workingMailDir, false, false, account, unm, sid, uty, men, den, dnm, bnm, imageList[0], imageCount[0], outputCount, usedImageList, bytesOut);
    }
    catch(Exception e) { System.out.println(e); }

    folder.close(false);
    store.close();
 
    scoutln(out, bytesOut, "</td></tr></table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void extractPart(PrintWriter out, Part p, int msgNumI, String imagesDir, String workingMailDir, boolean hasTextPlainIn,
                           boolean hasTextHtmlIn, String account, String unm, String sid, String uty,  String men, String den,
                           String dnm, String bnm, String imageList, int imageCount, int[] outputCount, String[] usedImageList, int[] bytesOut)
                           throws Exception
  {
    String[] from        = new String[1];  from[0] = "";
    String[] to          = new String[1];  to[0] = "";
    String[] cc          = new String[1];  cc[0] = "";
    String[] subject     = new String[1];  subject[0] = "";
    String[] sentDate    = new String[1];  sentDate[0] = "";
    String[] mailProgram = new String[1];  mailProgram[0] = "";

    if(p instanceof Message)
      extractEnvelope((Message)p, from, to, cc, subject, sentDate, mailProgram);

    // format the addresses
    // From
    String fromStuff="";
    int len = from[0].length();
    int x=0;
    while(x < len)
    {
      if(x > 0)
        fromStuff += "<br>";
      while(x < len && from[0].charAt(x) != ' ') // just-in-case
      {
        if(from[0].charAt(x) == '<')
          fromStuff += "&lt;";
        else
        if(from[0].charAt(x) == '>')
          fromStuff += "&gt;";
        else fromStuff += from[0].charAt(x);
        ++x;
      }
      ++x;
    }

    // To
    String toStuff="<tr><td><p><b>To:</td><td nowrap><p>";
    len = to[0].length();
    x=0;
    while(x < len)
    {
      if(to[0].charAt(x) == '<')
        toStuff += "&lt;";
      else
      if(to[0].charAt(x) == '>')
        toStuff += "&gt;";
      else toStuff += to[0].charAt(x);
      ++x;
    }
    toStuff += "</td></tr>";

    // CC
    String ccStuff="<tr><td><p><b>CC:</td><td nowrap><p>";
    len = cc[0].length();
    x=0;
    while(x < len)
    {
      if(cc[0].charAt(x) == '<')
        ccStuff += "&lt;";
      else
      if(cc[0].charAt(x) == '>')
        ccStuff += "&gt;";
      else ccStuff += cc[0].charAt(x);
      ++x;
    }
    ccStuff += "</td></tr>";

      boolean[] hasTextPlain = new boolean[1];  hasTextPlain[0] = false;
      boolean[] hasTextHtml  = new boolean[1];  hasTextHtml[0]  = false;
      // need to determine if both text/plain and text/html exist
      examinePart(p, hasTextPlain, hasTextHtml);

    scoutln(out, bytesOut, "<table id=\"pageColumn\" width=100%>");

    if(level == 0)
    {
      drawMessageHeader(out, true, msgNumI, generalUtils.sanitise(sentDate[0]), account, from[0], to[0], cc[0], hasTextHtml[0], unm, sid, uty, men, den, dnm, bnm,
                        bytesOut);
    }

    scoutln(out, bytesOut, "</table>");
      
    if(fromStuff.length() != 0)
    {
      scoutln(out, bytesOut, "<table id='pageColumn' width=100%>");

      scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
      scoutln(out, bytesOut, "<tr><td><p><b>From:</td><td width=99%><p>" + fromStuff + "</td></tr>");
      scoutln(out, bytesOut, toStuff);
      scoutln(out, bytesOut, ccStuff);
      scoutln(out, bytesOut, "<tr><td><p><b>Subject:</td>");
      scoutln(out, bytesOut, "<td><p>" + subject[0] + "</td></tr>");
      scoutln(out, bytesOut, "<tr><td nowrap><p><b>Sent Date:&nbsp;</td>");
      scoutln(out, bytesOut, "<td><p>" + sentDate[0] + "</td></tr>");
      scoutln(out, bytesOut, "<tr><td nowrap><p><b>Mail Program:</td><td width=99%><p>" + mailProgram[0] + "</td></tr></table>");
    } 
    
    scoutln(out, bytesOut, "<table id=\"page\" cellspacing=2 cellpadding=0 width=100%>");

    String fileName = p.getFileName();
    if(fileName != null)
    {
      if(! p.isMimeType("multipart/*"))
      {
        String disp = p.getDisposition();

        // many mailers don't include a Content-Disposition
        if(disp == null || disp.equalsIgnoreCase(Part.ATTACHMENT) || disp.equals(Part.INLINE))
        {
          try
          {
            // do not list as an attachment if already on used images list
            String imageName;
            int y=0;
            len=usedImageList[0].length();
            boolean quit=false, found=false;
            while(! found && ! quit) // just-in-case
            {    
              if(y == len)
                quit = true;
              else
              {
                imageName = "";
                while(usedImageList[0].charAt(y) != '\001')
                  imageName += usedImageList[0].charAt(y++);
                ++y;
                if(imageName.equals(fileName))
                  found = quit = true;                
              } 
            }

            if(! found)
            {
              scoutln(out, bytesOut, "<tr><td><h1>Attachment: <a href=\"javascript:unattach('" + generalUtils.stripAllSpaces(fileName) + "')\">" + fileName
                          + "</a></td></tr>");
            }
            
            File f = new File(workingMailDir + generalUtils.stripAllSpaces(fileName));
            ((MimeBodyPart)p).saveFile(f);
          }
          catch(IOException ex) { }
        }
      }  
    }
    
    scoutln(out, bytesOut, "<tr><td><p>");

    if(hasTextHtmlIn && p.isMimeType("text/plain"))
      ;
    else
    if(p.isMimeType("text/plain"))
    {
      outputPlainText(out, (String)p.getContent()); /// incorrectly puts "a href" into img tag // TODO
    }
    else
    if(p.isMimeType("multipart/*"))
    {
      Multipart mp = (Multipart)p.getContent();
      
      level++;
      int count = mp.getCount();
      for(x=0;x<count;++x)
      {    
        extractPart(out, mp.getBodyPart(x), msgNumI, imagesDir, workingMailDir, hasTextPlain[0], hasTextHtml[0], account, unm, sid, uty, men,
                    den, dnm, bnm, imageList, imageCount, outputCount, usedImageList, bytesOut);
      }

      level--;
    }
    else
    if(p.isMimeType("message/rfc822"))
    {
      level++;

      extractPart(out, (Part)p.getContent(), msgNumI, imagesDir, workingMailDir, false, false, account, unm, sid, uty, men, den, dnm, bnm,
                  imageList, imageCount, outputCount, usedImageList, bytesOut);
      level--;
    }
    else
    {
      // not a MIME type that we know, so fetch it and check its Java type
      Object o = p.getContent();
      if(o instanceof String)
      {
        outputString(out, (String)o, workingMailDir, outputCount, imageList, imageCount, usedImageList);
      }
      else
      if(o instanceof InputStream)
      {
      }
      else
      {
        outputString(out, "Unknown format", workingMailDir, outputCount, imageList, imageCount, usedImageList);
      }
    }

    scoutln(out, bytesOut, "</td></tr></table>");

    scoutln(out, bytesOut, "<table id='pageColumn' width=100%>");

    if(level == 0)
    {
      drawMessageHeader(out, false, msgNumI, generalUtils.sanitise(sentDate[0]), account, from[0], to[0], cc[0], hasTextHtml[0], unm, sid, uty, men, den, dnm, bnm,
                        bytesOut);
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void outputPlainText(PrintWriter out, String s) throws Exception 
  {
    boolean doingURL = false;
    String urlBit = "";
    int x;

    int len = s.length();
    for(x=0;x<len;++x)
    {
      if(s.charAt(x) == '\r' || s.charAt(x) == '\n')
      {
        if(doingURL)
        {
          out.print("\">");
          doingURL = false;
          out.print(urlBit);
          out.print("</a>");
          urlBit = "";
        }

        if(s.charAt(x) == '\n')
          out.print("<br>");
      }
      else
      {
        if((x + 4) < len && s.charAt(x) == 'h' && s.charAt(x+1) == 't' && s.charAt(x+2) == 't' && s.charAt(x+3) == 'p'
           && s.charAt(x+4) == ':')
        {
          out.print("<a href=\"");
          doingURL = true;               
        }

        if(s.charAt(x) == ' ' || s.charAt(x) == '\t' || s.charAt(x) == ')')
        {
          if(doingURL)
          {
            out.print("\">");
            doingURL = false;
            out.print(urlBit);
            out.print("</a>");
            urlBit = "";
          }
        }

        out.print(s.charAt(x));

        if(doingURL)
          urlBit += s.charAt(x);
      }
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void outputString(PrintWriter out, String s, String workingMailDir, int[] outputCount, String imageList, int imageCount,
                            String[] usedImageList) throws Exception 
  {
    int len = s.length();
    int x=0;
    while(x < len)
    {
      if(s.charAt(x) != '\r')
      {
        if((x + 3) < len && s.charAt(x) == 'c' && s.charAt(x+1) == 'i' && s.charAt(x+2) == 'd' && s.charAt(x+3) == ':')
        {
          int y=0, thisCount=0;
          while(thisCount < imageCount && thisCount < outputCount[0]) // just-in-case
          {    
            while(imageList.charAt(y) != '\001')
              ++y;
            ++y;
            ++thisCount;
          }  

          if(thisCount < imageCount) // just-in-case
          {    
            String imageName = "";
            while(imageList.charAt(y) != '\001')
              imageName += imageList.charAt(y++);
            
            out.print(generalUtils.stripAllSpaces(workingMailDir + imageName));
            
            // note the fact that this image has been used
            usedImageList[0] += generalUtils.stripAllSpaces(imageName + "\001");
            
            while(x < len && s.charAt(x) != '"' && s.charAt(x) != '>' && s.charAt(x) != ' ')
              ++x;
            
            ++outputCount[0];
          }
        }
      
        out.print(s.charAt(x++));
      }
      else ++x;
    }
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void extractEnvelope(Message m, String[] from, String[] to, String[] cc, String[] subject, String[] sentDate, String[] mailProgram)
                               throws Exception
  {
    Address[] a;
    int x, y, len;
    
    String msgFrom;
    from[0]="";
    if((a = m.getFrom()) != null)
    {
      for(x=0;x<a.length;++x)  
      {
        msgFrom = a[x].toString();
        len = msgFrom.length();
        if(msgFrom.indexOf("<") != -1)
        {
          x=0;
          while(x < len && msgFrom.charAt(x) != '<')
            ++x;
          ++x;

          while(x < len && msgFrom.charAt(x) != '>')
            from[0] += msgFrom.charAt(x++);
        }
        else from[0] += (msgFrom + " ");
        from[0] += " ";        
      }
    }
    else from[0] = "Unknown ";

  try
  {
      to[0]="";
  
    int z;
    String s;
    if((a = m.getRecipients(Message.RecipientType.TO)) != null)
    {
      for(x=0;x<a.length;++x)
      {
        s = a[x].toString();
        len = s.length();
        z=0;
        while(z < len)
        {
          if(s.charAt(z) != '"')
            to[0] += ("" + s.charAt(z));
          ++z;
        }
        to[0] += " ";
        InternetAddress ia = (InternetAddress)a[x];
        if(ia.isGroup())
        {
          InternetAddress[] aa = ia.getGroup(false);
          for(y=0;y<aa.length;++y)
          {    
            s = aa[y].toString();
            len = s.length();
            z=0;
            while(z < len)
            {
              if(s.charAt(z) != '"')
                to[0] += ("" + s.charAt(z));
              ++z;
            }
            to[0] += " ";
          }  
        }
      }
    }

    cc[0]="";
    if((a = m.getRecipients(Message.RecipientType.CC)) != null)
    {
      for(x=0;x<a.length;++x)
      {
        s = a[x].toString();
        len = s.length();
        z=0;
        while(z < len)
        {
          if(s.charAt(z) != '"')
            cc[0] += ("" + s.charAt(z));
          ++z;
        }
        cc[0] += " ";
        InternetAddress ia = (InternetAddress)a[x];
        if(ia.isGroup())
        {
          InternetAddress[] aa = ia.getGroup(false);
          for(y=0;y<aa.length;++y)
          {    
            s = aa[y].toString();
            len = s.length();
            z=0;
            while(z < len)
            {
              if(s.charAt(z) != '"')
                cc[0] += ("" + s.charAt(z));
              ++z;
            }
            cc[0] += " ";
          }  
        }
      }
    }
  }
  catch(Exception e) { to[0] = "Undisclosed Recipient"; }
  
    subject[0] = m.getSubject();
    if(subject[0] == null)
      subject[0] = "";

    java.util.Date d = m.getSentDate();
    if(d != null)
      sentDate[0] = d.toString();
    else sentDate[0] = "Unknown";    

    String[] t = m.getHeader("X-Mailer");
    if(t == null)
      mailProgram[0] = "";
    else mailProgram[0] = t[0];
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void examinePart(Part p, boolean[] hasTextPlain, boolean[] hasTextHtml) throws Exception
  {
    if(p.isMimeType("text/plain"))
    {
      hasTextPlain[0] = true;
    }
    else
    if(p.isMimeType("text/html"))
    {
      hasTextHtml[0] = true;  
    }
    else
    if(p.isMimeType("multipart/*"))
    {
      Multipart mp = (Multipart)p.getContent();
      int count = mp.getCount();
      for(int x=0;x<count;++x)
        examinePart(mp.getBodyPart(x), hasTextPlain, hasTextHtml);
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void drawMessageHeader(PrintWriter out, boolean onTop, int msgNumI, String sentDate, String account, String from, String to, String cc,
                                 boolean hasTextHtmlIn, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut)
                                 throws Exception
  {
    if(onTop)
    {
      scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
      scoutln(out, bytesOut, "<tr><td colspan=2 nowrap><p><b>Inbox Message: </b>" + (msgNumI + 1) + "</td><tr>");
      scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    }
    
    String html;
    if(hasTextHtmlIn)
      html = "Y";
    else html = "N";
    
    scoutln(out, bytesOut, "<tr><td nowrap><p>&nbsp<a href=\"javascript:del('" + msgNumI + "','" + sentDate + "')\">Delete</a>");

    scoutln(out, bytesOut, "</td></tr>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void determineImageAttachments(Part p, String workingMailDir, String[] imageList, int[] imageCount) throws Exception
  {
    String fileName = p.getFileName();

    if(fileName != null)
    {
      String fileNameUC = fileName.toUpperCase();

      if(! p.isMimeType("multipart/*"))
      {
        String disp = p.getDisposition();

        if(disp == null || disp.equalsIgnoreCase(Part.ATTACHMENT) || disp.equals(Part.INLINE))
        {
          if(fileNameUC.endsWith("JPG") || fileNameUC.endsWith("JPEG") || fileNameUC.endsWith("PNG") || fileNameUC.endsWith("GIF"))
          {
            imageList[0] += generalUtils.stripAllSpaces(fileName + '\001');
            ++imageCount[0];
          }            
        }
      }  
    }
    
    if(p.isMimeType("text/plain"))
      ;
    else
    if(p.isMimeType("text/plain"))
      ;
    else
    if(p.isMimeType("multipart/*"))
    {
      Multipart mp = (Multipart)p.getContent();
      int count = mp.getCount();
      for(int x=0;x<count;++x)
        determineImageAttachments(mp.getBodyPart(x), workingMailDir, imageList, imageCount);
    }
    else
    if(p.isMimeType("message/rfc822"))
      determineImageAttachments((Part)p.getContent(), workingMailDir, imageList, imageCount);
  }
   
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }
  
}
