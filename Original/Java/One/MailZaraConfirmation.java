// =======================================================================================================================================================================================================
// System: ZaraStar MailEngine: Display confirmation for mail
// Module: MailZaraConfirmation.java
// Author: C.K.Harvey
// Copyright (c) 2002-09 Christopher Harvey. All Rights Reserved.
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
import java.sql.*;
import java.io.*;
import java.util.Enumeration;

public class MailZaraConfirmation extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  DrawingUtils drawingUtils = new DrawingUtils();
  Profile profile = new Profile();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", callBackServlet="", companyType="", companyCode="", projectCode="", to="", from="", ccList="", bccList="", subject="", text="", docCode = "", contactCode = "", sendType = "";

    try
    {
      directoryUtils.setContentHeaders(res);
      out = res.getWriter();

      Enumeration en = req.getParameterNames();
      while(en.hasMoreElements())
      {
        String name = (String)en.nextElement();
        String[] value = req.getParameterValues(name);
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
        if(name.equals("callBackServlet")) 
          callBackServlet = value[0];
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
        if(name.equals("contactCode"))
          contactCode = value[0];
        else
        if(name.equals("docCode"))
          docCode = value[0];
        else
        if(name.equals("sendType"))
          sendType = value[0];
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
        if(name.equals("subject"))
          subject = value[0];
        else
        if(name.equals("text"))
          text = value[0];
      }  

      docCode = generalUtils.stripLeadingAndTrailingSpaces(docCode);
      companyCode = generalUtils.stripLeadingAndTrailingSpaces(companyCode);
      companyType = generalUtils.stripLeadingAndTrailingSpaces(companyType);
      projectCode = generalUtils.stripLeadingAndTrailingSpaces(projectCode);
      from   = generalUtils.stripLeadingAndTrailingSpaces(from);
      ccList = generalUtils.stripLeadingAndTrailingSpaces(ccList);
      bccList = generalUtils.stripLeadingAndTrailingSpaces(bccList);

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, callBackServlet, docCode, contactCode, companyCode, companyType, projectCode, to, from, ccList, bccList, subject, text, sendType, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "MailZaraConfirmation", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8019, bytesOut[0], 0, "ERR:" + to);
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String callBackServlet, String docCode, String contactCode, String companyCode, String companyType,
                   String projectCode, String to, String from, String ccList, String bccList, String subject, String text, String sendType, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir           = directoryUtils.getSupportDirs('I');
    String defnsDir            = directoryUtils.getSupportDirs('D');
    String localDefnsDir       = directoryUtils.getLocalOverrideDir(dnm);
    String signatureLibraryDir = directoryUtils.getSignaturesDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 8014, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "MailZaraConfirmation", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8019, bytesOut[0], 0, "ACC:" + to);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "MailZaraConfirmation", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8019, bytesOut[0], 0, "SID:" + to);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }
    
    scoutln(out, bytesOut, "<html><head><title>Mail Confirmation</title>");

    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    scoutln(out, bytesOut, "function send(){document.go.submit();}");

    scoutln(out, bytesOut, "</script>");

    int[] hmenuCount = new int[1];

    drawingUtils.outputPageFrame(con, stmt, rs, out, req, "8019", "", "MailZaraConfirmation", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount, bytesOut);
 
    drawingUtils.drawTitle(out, false, false, "MailZaraConfirmation", "", "Mail Message Confirmation", "8019", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<form name=\"go\" action=\"MailZaraSendMailServlet\" enctype=\"application/x-www-form-urlencoded\" method=POST>");

    String[] owner            = new String[1];
    String[] name             = new String[1];
    String[] companyName      = new String[1];
    String[] eMail            = new String[1];
    String[] userCode         = new String[1];
    String[] domain           = new String[1];
    String[] jobTitle         = new String[1];
    String[] notes            = new String[1];
    String[] customerCode     = new String[1];
    String[] supplierCode     = new String[1];
    String[] organizationCode = new String[1];
    String[] externalCode     = new String[1];
    String[] externalPassWord = new String[1];
    String[] externalRights   = new String[1];
    String[] externalApproved = new String[1];
    String[] phone1           = new String[1];
    String[] phone2           = new String[1];
    String[] phone3           = new String[1];
    String[] fax              = new String[1];
    String[] mailingList      = new String[1];

    profile.getContact(contactCode, owner, name, companyName, eMail, domain, userCode, jobTitle, notes, customerCode, supplierCode, organizationCode, externalCode, externalPassWord, externalRights, externalApproved, phone1, phone2, phone3, fax,
                     mailingList, dnm, localDefnsDir, defnsDir);

    String senderMailAddr = getEMail(con, stmt, rs, from);

    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=unm value=" + unm + ">");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=sid value=" + sid + ">");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=uty value=" + uty + ">");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=men value=\"" + men + "\">");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=den value=" + den + ">");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=dnm value=" + dnm + ">");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=bnm value=" + bnm + ">");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=callBackServlet value=\"" + callBackServlet + "\">");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=companyCode value=\"" + companyCode + "\">");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=companyType value=\"" + companyType + "\">");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=projectCode value=\"" + projectCode + "\">");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=to value=\"" + eMail[0] + "\">");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=sendType value=\"" + sendType + "\">");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=from value=\"" + from + "\">");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=ccList value=\"" + ccList + "\">");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=bccList value=\"" + bccList + "\">");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=subject value=\"" + subject + "\">");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=senderMailAddr value=\"" + senderMailAddr + "\">");

    scoutln(out, bytesOut, "<table id=\"page\" cellspacing=2 cellpadding=0 border=0 width=100%>");

    outputMail(con, stmt, rs, out, to, from, senderMailAddr, ccList, bccList, subject, text, name[0], companyName[0], eMail[0], externalCode[0], callBackServlet, docCode, contactCode, companyCode, companyType, projectCode, unm,
               signatureLibraryDir, localDefnsDir, defnsDir, bytesOut);

    scoutln(out, bytesOut, "</table>");    
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 8019, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), to);
    if(con  != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void outputMail(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String to, String from, String senderMailAddr, String ccList, String bccList, String subject, String text, String name, String companyName, String eMail,
                          String externalCode, String callBackServlet, String docCode, String contactCode, String companyCode, String companyType, String projectCode, String unm, String signatureLibraryDir, String localDefnsDir, String defnsDir,
                          int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<tr><td colspan=2><p>This is the mail that will be sent to: ");

    if(companyCode.length() == 0)
      scoutln(out, bytesOut, "<font color=red size=3>Error: No Company Code Specified</font>");
    else scoutln(out, bytesOut, "<b>" + name + "</b> at <b>" + companyName + "</b>");

    scoutln(out, bytesOut, "<br><br>You may click on any embedded link in order to confirm it's operation.</td></tr>");

    scoutln(out, bytesOut, "<tr><td colspan=2><hr></td></tr>");

    boolean anError = false;
    
    if(contactCode.length() == 0)
    {
      scoutln(out, bytesOut, "<tr><td><p><b>To:</td><td><p><font color=red size=3>Error: None Specified</font></td></tr>");
      anError = true;
    }
    else scoutln(out, bytesOut, "<tr><td><p><b>To:</td><td><p>" + eMail + "</td></tr>");

    if(from.length() == 0)
    {
      scoutln(out, bytesOut, "<tr><td><p><b>From:</td><td><p><font color=red size=3>Error: None Specified</font></td></tr>");
      anError = true;
    }
    else scoutln(out, bytesOut, "<tr><td><p><b>From:</td><td><p>" + senderMailAddr + "</td></tr>");

    scoutln(out, bytesOut, "<tr><td><p><b>CC:</td><td><p>" + ccList + "</td></tr>");
    scoutln(out, bytesOut, "<tr><td><p><b>BCC:</td><td><p>" + bccList + "</td></tr>");

    if(subject.length() == 0)
    {
      scoutln(out, bytesOut, "<tr><td nowrap><p><b>Subject: &nbsp; &nbsp;</td><td><p><font color=red size=3>Error: None Specified</font></td></tr>");
      anError = true;
    }
    else scoutln(out, bytesOut, "<tr><td nowrap><p><b>Subject: &nbsp; &nbsp;</td><td><p>" + subject + "</td></tr>");
    
    // text lines...
    scoutln(out, bytesOut, "<tr><td valign=top nowrap><p><b>Text:</td><td><p>");

    text = text.replace("___ExtUserCode___", externalCode);

    text = "<font face='Verdana,Arial,Helvetica,sans-serif' size=2>" + text + "</font>";

    String[] signatureText = new String[1];

    if(getSignature(signatureLibraryDir + from, signatureText))
      text += ("<br><br>" + signatureText[0]);

    text = makeLinkLive(text);

    text = generalUtils.replaceNewLinesByBR(text);

    scoutln(out, bytesOut, text);

    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=text value=\"" + generalUtils.sanitise(text) + "\">");

    scoutln(out, bytesOut, "</td></tr>");

    scoutln(out, bytesOut, "<tr><td colspan=2><hr></td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    if(! anError)
      scoutln(out, bytesOut, "<tr><td nowrap colspan=4><p><a href=\"javascript:send()\">Send</a> Mail</td>");
 
    scoutln(out, bytesOut, "</tr>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean getSignature(String fullPathName, String[] text) throws Exception
  {
    text[0] = "";

    try
    {
      RandomAccessFile fh;

      if((fh = generalUtils.fileOpen(fullPathName)) != null)
      {
        String s;
        try
        {
          s = fh.readLine();
          while(s != null)
          {
            text[0] += s;
            s = fh.readLine();
          }
        }
        catch(Exception ioErr)
        {
          generalUtils.fileClose(fh);
          return false;
        }
      }
      else return false;

      generalUtils.fileClose(fh);
    }
    catch(Exception e) { }

    return true;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String makeLinkLive(String text) throws Exception
  {
    String s = "", link;

    int len = text.length();
    int x = 0, y;
    while(x < len)
    {
      if(text.charAt(x) == 'h')
      {
        y = x;
        link = "";
        while(y < len && text.charAt(y) != '/') // just-in-case
          link += text.charAt(y++);

        if(link.equals("http:"))
        {
          while(y < len && text.charAt(y) != ' ' && text.charAt(y) != '\n' && text.charAt(y) != '<') // just-in-case
            link += text.charAt(y++);

          s += ("<a href=\"" + link + "\" target=_new>" + link + "</a>");

          x = y - 1;
        }
        else s += 'h';
      }
      else s += text.charAt(x);

      ++x;
    }

    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void outputText(PrintWriter out, String text, String extUserCode, String recipientPwd, String callBackServlet, String originatingDocCode, String dnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    String s="", link;

    int len = text.length();
    int x=0;
    while(x < len)
    {
      if(text.charAt(x) == '\n')
      {
        scoutln(out, bytesOut, "<br>");
        ++x;
      }
      else
      if(text.charAt(x) == '[')
      {
        ++x;
        link = "[";
        while(x < len && text.charAt(x) != ']') // just-in-case
          link += text.charAt(x++);

        if(x == len) // unterminated potential link structure at EOF
          s += link; // append whatever it is and leave as-is
        else
        {
          if(text.charAt(x) != ']') // unterminated potential link structure
          {
            s += link;              // append whatever it is and leave as-is
         x += link.length();
          }
          else
          {
            ++x; // ']'
            if(! link.equals("[LINK WILL GO HERE")) // not a link structure
            {
              x += link.length();
              s += link;                         // append whatever it is and leave as-is
            }
            else // is a link structure
            {
              x += link.length();

              String externalAccessServer = generalUtils.getFromDefnFile("EXTERNALACCESSSERVER", "local.dfn", localDefnsDir, defnsDir);
              if(externalAccessServer.length() == 0) // just-in-case
                externalAccessServer = "UNKNOWNSERVER";

              scoutln(out, bytesOut, externalAccessServer + "/central/servlet/MainPageUtils?unm=" + extUserCode //////////+ "&pwd=" + recipientPwd
                                     + "&dnm=" + dnm + "&bnm=" + "&p1=" + callBackServlet + "&p2=" + originatingDocCode + "'");
            }
          }
        }
      }
      else s += text.charAt(x++);
    }

    scoutln(out, bytesOut, s);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String getEMail(Connection con, Statement stmt, ResultSet rs, String userCode) throws Exception
  {
    String eMail = "";

    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT EMail FROM profiles WHERE UserCode = '" + userCode + "'");

      if(rs.next())
        eMail = rs.getString(1);

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }

    return eMail;
  }

}
