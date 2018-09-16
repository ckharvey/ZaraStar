// =======================================================================================================================================================================================================
// System: ZaraStar MailEngine: Send a Mail
// Module: MailZaraMailSend.java
// Author: C.K.Harvey
// Copyright (c) 2002-08 Christopher Harvey. All Rights Reserved.
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
import java.sql.*;
import java.io.*;
import javax.mail.*;
import javax.mail.internet.InternetAddress;

public class MailZaraMailSend extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  MailUtils mailUtils = new MailUtils();

  static int level = 0;

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="", p3="", p4="", p5="", p6="", p7="", p8="";

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
      p1  = req.getParameter("p1"); // from
      p2  = req.getParameter("p2"); // to
      p3  = req.getParameter("p3"); // account
      p4  = req.getParameter("p4"); // cc
      p5  = req.getParameter("p5"); // forward
      p6  = req.getParameter("p6"); // msgNum
      p7  = req.getParameter("p7"); // htmlOrNot
      p8  = req.getParameter("p8"); // subject (if a switch between html and not)

      if(p1 == null) p1 = "";
      if(p2 == null) p2 = "";
      if(p4 == null) p4 = "";
      if(p5 == null) p5 = "N";
      if(p6 == null) p6 = "-1";
      if(p7 == null) p7 = "N";
      if(p8 == null) p8 = "";
      
      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, p3, p4, p5, p6, p7, p8, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "MailZaraMailSend", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8007, bytesOut[0], 0, "ERR:" + p3);
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String from,
                    String to, String account, String cc, String forward, String msgNum, String htmlOrNot, String subject, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String workingMailDir   = directoryUtils.getUserDir('W', dnm, unm) + "Mail/";
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 8014, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "MailZaraMailSend", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8007, bytesOut[0], 0, "ACC:" + account);
      
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "MailZaraMailSend", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8007, bytesOut[0], 0, "SID:" + account);
      
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }    

    if(! generalUtils.isDirectory(directoryUtils.getUserDir('W', dnm, unm))) // for any attachments
      generalUtils.createDir(directoryUtils.getUserDir('W', dnm, unm), "755");

    if(! generalUtils.isDirectory(workingMailDir))
      generalUtils.createDir(workingMailDir, "755");
    
    boolean htmlOutput;
    if(htmlOrNot.equals("Y"))
      htmlOutput = true;
    else htmlOutput = false;
    
    String[] pop3Server   = new String[1];
    String[] fromAddress  = new String[1];
    String[] userName     = new String[1];
    String[] passWord     = new String[1];
    String[] userFullName = new String[1];
    String[] type         = new String[1];

    mailUtils.getAccountDetails(account, unm, dnm, pop3Server, fromAddress, userName, passWord, userFullName, type);
    from = fromAddress[0];
    
    scoutln(out, bytesOut, "<html><head><title>Zara Mail: Send Mail</title>");

    scoutln(out, bytesOut, "<SCRIPT LANGUAGE=\"JavaScript\">");

    scoutln(out, bytesOut, "function switchMode(htmlOrNot){");
    scoutln(out, bytesOut, "var p1=sanitise(document.forms[0].from.value);");
    scoutln(out, bytesOut, "var p2=sanitise(document.forms[0].to.value);");
    scoutln(out, bytesOut, "var p4=sanitise(document.forms[0].ccList.value);");
    scoutln(out, bytesOut, "var p8=sanitise(document.forms[0].subject.value);");
    scoutln(out, bytesOut, "window.location.replace(\"MailZaraMailSend?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men="
                + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p5=" + forward + "&p6=" + msgNum
                + "&p1=\" + p1 + \"&p2=\" + p2 + \"&p3=" + generalUtils.sanitise(account) + "&p7=\" + htmlOrNot +\"&p4=\" + p4);}");

    scoutln(out, bytesOut, "function remove(fileName){");
    scoutln(out, bytesOut, "var p1=sanitise(fileName);");
    scoutln(out, bytesOut, "window.location.replace(\"MailZaraMailSendc?unm=" + unm + "&sid=" + sid + "&uty=" + uty
                         + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm +  "&p1=\" + p1);}");

    scoutln(out, bytesOut, "var req4;");
    scoutln(out, bytesOut, "function remove(fileName){");
    scoutln(out, bytesOut, "var p1=sanitise(fileName);");
    scoutln(out, bytesOut, "var url = \"http://" + men + "/central/servlet/MailZaraMailSendc?unm=\" + escape('" + unm
                           + "') + \"&sid=\" + escape('" + sid + "') + \"&uty=\" + escape('"
                           + uty + "') + \"&men=\" + escape('" + men + "') + \"&den=\" + escape('" + den
                           + "') + \"&bnm=\" + escape('" + bnm + "') + \"&p1=\" + escape(' + p1 + ') + \"&dnm=\" + escape('" + dnm + "');");
    scoutln(out, bytesOut, "  initRequest4(url);");
    scoutln(out, bytesOut, "  req4.onreadystatechange = processRequest4;");
    scoutln(out, bytesOut, "  req4.open(\"GET\", url, true);");
    scoutln(out, bytesOut, "  req4.send(null);");
    scoutln(out, bytesOut, "}");

    scoutln(out, bytesOut, "function processRequest4()");
    scoutln(out, bytesOut, "{");
    scoutln(out, bytesOut, "  if(req4.readyState == 4)");
    scoutln(out, bytesOut, "  {");
    scoutln(out, bytesOut, "    if(req4.status == 200)");
    scoutln(out, bytesOut, "    {");
    
    scoutln(out, bytesOut, "    }");
    scoutln(out, bytesOut, "  }");
    scoutln(out, bytesOut, "}");
        
    scoutln(out, bytesOut, "function sanitise(code){");
    scoutln(out, bytesOut, "var code2='';var x;var len=code.length;");
    scoutln(out, bytesOut, "for(x=0;x<len;++x)");
    scoutln(out, bytesOut, "if(code.charAt(x)=='#')code2+='%23';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='\"')code2+='%22';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='&')code2+='%26';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='%')code2+='%25';");
    scoutln(out, bytesOut, "else if(code.charAt(x)==' ')code2+='%20';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='?')code2+='%3F';");
    scoutln(out, bytesOut, "else if(escape(code.charAt(x))=='%0A')code2+='\003';");
    scoutln(out, bytesOut, "else if(escape(code.charAt(x))=='%0D');");
    scoutln(out, bytesOut, "else code2+=code.charAt(x);");
    scoutln(out, bytesOut, "return code2};");

    scoutln(out, bytesOut, "</script>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\" type=\"text/javascript\" src=\"" + directoryUtils.getEditorDirectory() + "editor.js\"></script>"); 

    String cssDirectory = directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir);
            
    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + cssDirectory + "general.css\">");

    int[] hmenuCount = new int[1];
    
    mailUtils.outputPageFrame(con, stmt, rs, out, req, false, account, "", "MailZaraMailSend", "8007", unm, sid, uty, men, den, dnm, bnm, " onLoad=\"document.getElementById('mailview').contentWindow.document.designMode='on';\";", localDefnsDir, defnsDir,
                          hmenuCount, bytesOut);

    mailUtils.drawTitle(con, stmt, rs, req, out, false, account, "Compose Mail", "8007", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<form name=\"send\" action=\"MailZaraMailSendExecute\" enctype=\"application/x-www-form-urlencoded\" method=\"POST\" >");
    scoutln(out, bytesOut, "<script language=\"JavaScript\">function go8007(){");
    if(htmlOutput)
      scoutln(out, bytesOut, "var d=document.getElementById('mailview');if(d!=null)document.getElementById('p1').value=d.contentWindow.document.body.innerHTML;" );
    else scoutln(out, bytesOut, "document.getElementById('p1').value=document.forms['send'].text.value;" );
    scoutln(out, bytesOut, "document.forms['send'].submit();}</script>");  

    scoutln(out, bytesOut, "<input type=hidden name=unm value=" + unm + ">");
    scoutln(out, bytesOut, "<input type=hidden name=sid value=" + sid + ">");
    scoutln(out, bytesOut, "<input type=hidden name=uty value=" + uty + ">");
    scoutln(out, bytesOut, "<input type=hidden name=men value=\"" + men + "\">");
    scoutln(out, bytesOut, "<input type=hidden name=den value=" + den + ">");
    scoutln(out, bytesOut, "<input type=hidden name=dnm value=" + dnm + ">");
    scoutln(out, bytesOut, "<input type=hidden name=bnm value=" + bnm + ">");
    scoutln(out, bytesOut, "<input type=hidden name=account value=" + generalUtils.sanitise(account) + ">");
    scoutln(out, bytesOut, "<input type=hidden name=mode value=" + htmlOrNot + ">");
    scoutln(out, bytesOut, "<input type=hidden name=msgNum value=" + msgNum + ">");
    scoutln(out, bytesOut, "<input type=hidden name=forward value=" + forward + ">");

    scoutln(out, bytesOut, "<input type=\"hidden\" id=\"p1\" name=\"p1\" value=''>");      
    
    scoutln(out, bytesOut, "<table id=\"generalMailMessageHeader\" cellspacing=2 cellpadding=0 width=100%>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap><p><b>Compose Mail Message</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "</table>");

    if(level == 0)
    {
      scoutln(out, bytesOut, "<table id=\"generalMailMessageHeader\" width=100%>");
      drawMessageHeader(out, bytesOut);
      scoutln(out, bytesOut, "</table>");
    }
     
    scoutln(out, bytesOut, "<table id=\"generalMailMessageHeader\" cellspacing=2 cellpadding=0 width=100%    border=0>");

    Properties props = new Properties();
    Session session = Session.getDefaultInstance(props, null);

    Store store = session.getStore("pop3");
    store.connect(pop3Server[0], userName[0], passWord[0]);

    Folder folder = store.getFolder("INBOX");
    folder.open(Folder.READ_ONLY);

    //    String attachmentName="";

    int msgNumI = generalUtils.strToInt(msgNum);

    int x;

    Message message[] = folder.getMessages();

    if(subject.length() == 0 && msgNumI != -1) // not a new message compose
    {
      subject = fetchSubject(message, msgNumI);

      if(forward.charAt(0) == 'Y')
      {
        to = "";
        subject = "Fwd: " + subject;
      }
      else 
      {
        subject = "Re: " + subject;
       
      }
    }

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr><tr><td><p>From:</td><td colspan=2><input type=text name=from value='" + from + "' size=60}></td></tr>");

    scoutln(out, bytesOut, "<tr><td><p>To:</td><td colspan=2><input type=text name=to value='" + to + "' size=60}></td></tr>");

    // tidy e.g., "fredlim <fredlim@company.com>"

    String cc2 = "";
    boolean quit=false;

    if((x = cc.indexOf("<")) == -1)
      cc2 = cc;
    else
    {
      while(! quit)
      {
        if((x = cc.indexOf("<")) == -1)
          quit = true;
        else
        {
          cc = cc.substring(x);
          x=1;
          while(cc.charAt(x) != '>')
            cc2 += cc.charAt(x++);
          cc = cc.substring(x+1);
        }
      }

      cc2 += cc.substring(x+1);
    }

    scoutln(out, bytesOut, "<tr><td><p>CC:</td><td colspan=2><input type=text name=ccList value='" + cc2 + "' size=60></td></tr>");

    scoutln(out, bytesOut, "<tr><td><p>BCC:</td><td colspan=2><input type=text name=bccList size=60}></td></tr>");

    scoutln(out, bytesOut, "<tr><td><p>Subject:</td><td colspan=2><input type=text name=subject value='" + subject + "' size=60></td></tr>");

     scoutln(out, bytesOut, "<form name=\"attachment\" enctype=\"multipart/form-data\" action=\"" + directoryUtils.getScriptsDirectory() + "attach.php\""
                          + " method=\"post\">");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"sessionid\" value=\"<?= $sid ?>\">");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"workingDir\" value='" + workingMailDir + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"fileNames\">");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td><p>Attachments:</td>");
 
    File path = new File(workingMailDir);
    String fs[] = new String[0];
    fs = path.list();

   // String s;
    boolean first = true;

    for(x=0;x<fs.length;++x)
    {
      if(fs[0].equalsIgnoreCase("__libperm.cfs") || fs[0].equalsIgnoreCase("__libaccess.log"))
        ;
      else
      {
        if(! first)
          scoutln(out, bytesOut, "</tr><tr><td></td>");
        else first = false;
            
        scoutln(out, bytesOut, "<td nowrap><p>" + fs[x] + "</td><td width=90%><p>&nbsp;&nbsp;<a href=\"javascript:remove('" 
                               + generalUtils.stripAllSpaces(fs[x]) + "')\">Remove</a></td>");
      }
    }
    scoutln(out, bytesOut, "</tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    
    // text lines...
    if(htmlOutput)
    {
      scoutln(out, bytesOut, "<tr>");
      scoutln(out, bytesOut, "<td colspan=3><textarea id=\"mailtext\" name=\"mailtext\" cols=\"100\" rows=\"20\"> ");
    }
    else
    {
      scoutln(out, bytesOut, "<tr>");
      scoutln(out, bytesOut, "<td colspan=2><textarea id=text name=text cols=100 rows=20>");
    }

    if(msgNumI != -1) // not a new message compose
      fetchMsgText(out, htmlOutput, message, msgNumI, pop3Server[0], userName[0], passWord[0], unm, uty, dnm, bnm, bytesOut);

    if(htmlOutput)
    {
      scoutln(out, bytesOut, "</textarea><script language=\"JavaScript\">drawEditor('mailtext', 'mailview', '100%', '400', '#FFFFFF', '#F0F0F0', '#C0C0FF','"
                             + unm + "','" + sid + "','" + uty + "','" + men + "','" + den + "','" + dnm + "','" + bnm + "');</script></td></tr>");
    }
  else 
    {
     scoutln(out, bytesOut, "</textarea></td></tr>");
    }

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr></table>");

    if(level == 0)
    {
      scoutln(out, bytesOut, "<table id=\"generalMailMessageHeader\" width=100%>");
      drawMessageHeader(out, bytesOut);
      scoutln(out, bytesOut, "</table>");
    }
     
    scoutln(out, bytesOut, "</form></div></body></html>");
    
    folder.close(false);
    store.close();

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 8007, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), account);
    
    if(con  != null) con.close();
    if(out != null) out.flush();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private String fetchSubject(Message[] message, int msgNumI) throws Exception
  {
    String subject="";
    if(message[msgNumI].getSubject() != null)
    {
      String s = message[msgNumI].getSubject().toString();
      int len = s.length();
      int z=0;
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

    return subject;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void fetchMsgText(PrintWriter out, boolean htmlOutput, Message[] message, int msgNumI, String pop3Server, String userName, String passWord,
                            String unm, String uty, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    String workingDir      =  directoryUtils.getUserDir('W', dnm, unm);
    
    boolean[] done = new boolean[1];  done[0] = false;
    
    extractPart(out, done, htmlOutput, message[msgNumI], workingDir, false, bytesOut);
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void extractPart(PrintWriter out, boolean[] done, boolean htmlOutput, Part p, String workingDir, boolean hasTextHtmlIn, int[] bytesOut)
                           throws Exception
  {
    if(done[0]) return;
    
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
    try
    {
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
  }
  catch(Exception e) { toStuff+="Undisclosed Recipient"; }

    // CC
    String ccStuff="<tr><td><p><b>CC:</td><td nowrap><p>";
    try 
    {
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
    }
   catch(Exception e) { ccStuff+="Undisclosed Recipient"; }
    
    String fileName = p.getFileName();
    if(fileName != null)
    {
      if(! p.isMimeType("multipart/*"))
      {
        String disp = p.getDisposition();

        // many mailers don't include a Content-Disposition
        if(disp == null || disp.equalsIgnoreCase(Part.ATTACHMENT) || disp.equals(Part.INLINE))
        {
        }
      }  
    }
    
    scoutln(out, bytesOut, "<tr><td><p>");

    if(htmlOutput && p.isMimeType("text/plain"))
      ;
    else
    if(! htmlOutput && p.isMimeType("text/plain"))
    {
        outputString(out, true, (String)p.getContent(), bytesOut);
    }
    else
    if(p.isMimeType("multipart/*"))
    {
      Multipart mp = (Multipart)p.getContent();
      
      level++;
      int count = mp.getCount();
      for(x=0;x<count;++x)
      {    
      extractPart(out, done, htmlOutput, (Part)mp.getBodyPart(x), workingDir, false, bytesOut);
      }

      level--;
    }
    else
    if(p.isMimeType("message/rfc822"))
    {
      level++;

      extractPart(out, done, htmlOutput, (Part)p.getContent(), workingDir, false, bytesOut);
      level--;
    }
    else
    {
      // not a MIME type that we know, so fetch it and check its Java type
      Object o = p.getContent();
      if(o instanceof String)
      {
        outputString(out, true, (String)o, bytesOut);
      }
      else
      if(o instanceof InputStream)
      {
      }
      else
      {
      }
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void outputString(PrintWriter out, boolean param, String s, int[] bytesOut) throws Exception 
  {
    int len = s.length();
    for(int x=0;x<len;++x)
    {
      if(s.charAt(x) == '\r')
       ;
      else
      {
        if(param)
        {
          if(s.charAt(x) == '\n')
            ;//scout(out, bytesOut, "<br />");
          else scout(out, bytesOut, s.charAt(x));
        }
        else
        {    
          if(s.charAt(x) == '\n')
            scout(out, bytesOut, "\n&gt;");
          else scout(out, bytesOut, s.charAt(x));
        }  
      }     
    }
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void outputHtmlString(PrintWriter out, boolean param, String s) throws Exception 
  {
    if(param)  
      out.print("<br>");
    else out.print("\n>");
    
    String t;
    int len = s.length();
    int x=0;
    while(x < len)
    {
      if(s.charAt(x) == '<')
      {
        ++x;
        t="";
        while(x < len && s.charAt(x) != '>') // just-in-case
        {
          if(s.charAt(x) == '\r')
            ;
          else
          if(s.charAt(x) == '\n')
          {
            if(param)
              out.print("<br />");
            else out.print('\n');
          }
          
          t += s.charAt(x);
          ++x;
        }

        if(t.equalsIgnoreCase("br") || t.equalsIgnoreCase("br /"))
        {
          if(param)
            out.print("<br />");
          else out.print('\n');
        }
        else
          out.println("<" + t + ">");
      }
      else out.print(s.charAt(x));        
      
      ++x;
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

     int z;
 
    String s;
 try
 {
    to[0]="";
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
 }
catch(Exception e) { to[0]="Undisclosed Recipient"; }
    cc[0]="";
try
{
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
catch(Exception e) { cc[0]="Undisclosed Recipient"; }

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
      hasTextPlain[0] = true;  
    else
    if(p.isMimeType("text/html"))    
      hasTextHtml[0] = true;  
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
  private void drawMessageHeader(PrintWriter out, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<tr><td nowrap><p>&nbsp<a href=\"javascript:go8007()\">Send</a>&nbsp;&nbsp; | &nbsp;&nbsp;");

    scoutln(out, bytesOut, "<a href=\"xx\">Send &amp; Save to Library</a>&nbsp;&nbsp; | &nbsp;&nbsp;</td></tr>");
  }
 
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void scout(PrintWriter out, int bytesOut[], char ch) throws Exception
  {      
    out.print(ch);
    ++bytesOut[0];    
  }
  private void scout(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.print(str);
    bytesOut[0] += str.length();
  }
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);
  }
  
}
