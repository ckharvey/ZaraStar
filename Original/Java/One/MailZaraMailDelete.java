// =======================================================================================================================================================================================================
// System: ZaraStar MailEngine: Delete Mail
// Module: MailZaraMailDelete.java
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
import java.net.*;
import javax.mail.*;
import java.util.*;
import java.sql.*;

public class MailZaraMailDelete extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  MailUtils mailUtils = new MailUtils();

  // doGet is used when deleting a single msg (from the view msg screen)
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="", p3="";

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
      p2  = req.getParameter("p2"); // msgNum
      p3  = req.getParameter("p3"); // sentDate
 
      int[] msgNums = new int[1];  msgNums[0] = generalUtils.strToInt(p2);

      byte[] sentDates = new byte[100]; // plenty
      generalUtils.strToBytes(sentDates, p3 + "\001");

      doIt(out, req, msgNums, sentDates, 1, unm, sid, uty, men, den, dnm, bnm, p1, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "MailZaraMailDelete", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8006, bytesOut[0], 0, "ERR:" + p1 + ":" + p2);
      if(out != null) out.flush();
    }
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="";

    try
    {
      directoryUtils.setContentHeaders(res);
      out = res.getWriter();

      int[] msgNums    = new int[1000]; msgNums[0] = '\000';
      int msgNumsLen   = 1000;
      byte[] sentDates = new byte[1000]; sentDates[0] = '\000';
      int sentDatesLen = 1000;
      
      int z, len, inc, thisEntryLen, count=0;
      String value[];
      String name, sentDate, msgNum;

      Enumeration en = req.getParameterNames();
      while(en.hasMoreElements())
      {
        name = (String)en.nextElement();
        value = req.getParameterValues(name);
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
        if(name.equals("p1"))
          p1 = value[0];
        else
        if(name.equals("all"))
          ; // gets passed-in but we can ignore it
        else // must be checkbox value
        {
          thisEntryLen = name.length() + 2;
          if((generalUtils.lengthBytes(sentDates, 0) + thisEntryLen) >= sentDatesLen)
          {
            byte[] tmp = new byte[sentDatesLen];
            System.arraycopy(sentDates, 0, tmp, 0, sentDatesLen);
            if(thisEntryLen > 1000)
              inc = thisEntryLen;
            else inc = 1000;
            sentDatesLen += inc;
            sentDates = new byte[sentDatesLen];
            System.arraycopy(tmp, 0, sentDates, 0, sentDatesLen - inc);
          }

          len = name.length();
          msgNum = "";
          z=0;
          while(z < len && name.charAt(z) != '\001') // just-in-case
            msgNum += name.charAt(z++);
          
          if(z < len && name.charAt(z) == '\001') // just-in-case
            sentDate = name.substring(z+1);
          else sentDate = "";          
            
          generalUtils.catAsBytes(sentDate + "\001", 0, sentDates, false);
          
          if((count + 1) >= msgNumsLen)
          {
            int i;
            int[] tmp = new int[msgNumsLen];
            for(i=0;i<msgNumsLen;++i)
              tmp[i] = msgNums[i];
            msgNumsLen += 1000;
            msgNums = new int[msgNumsLen];
            for(i=0;i<(msgNumsLen-1000);++i)
              msgNums[i] = tmp[i];
          }
          
          msgNums[count++] = generalUtils.strToInt(msgNum);
        }
      }

      doIt(out, req, msgNums, sentDates, count, unm, sid, uty, men, den, dnm, bnm, p1, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "MailZaraMailDelete", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8006, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, int[] msgNums, byte[] sentDates, int msgNumAndSentDatesCount,
                      String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1,
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
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "MailZaraMailDelete", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8006, bytesOut[0], 0, "ACC:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "MailZaraMailDelete", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8006, bytesOut[0], 0, "SID:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    p1 = generalUtils.deSanitise(p1);

    int z=0;
    String[] sentDatesStrs = new String[msgNumAndSentDatesCount];
    for(int x=0;x<msgNumAndSentDatesCount;++x)
    {
      sentDatesStrs[x] = "";
      while(sentDates[z] != '\001')
        sentDatesStrs[x] += (char)sentDates[z++];
      ++z;
      sentDatesStrs[x] = generalUtils.deSanitise(sentDatesStrs[x]);
    }
    
    deleteMsgs(p1, msgNums, sentDatesStrs, msgNumAndSentDatesCount, unm, dnm);

    reDisplay(out, unm, sid, uty, dnm, men, den, bnm, localDefnsDir, p1);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 8006, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con  != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void deleteMsgs(String account, int[] msgNums, String[] sentDates, int msgNumAndSentDatesLen, String unm, String dnm) throws Exception
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

    // Get folder
    Folder folder = store.getFolder("INBOX");
    folder.open(Folder.READ_WRITE);

    // Get directory
    Message message[] = folder.getMessages();
    int numMsgs = folder.getMessageCount();
    
    String sentDate;

    try // in case backing onto this page after having deleted the msg
    {
      int i;
      for(int x=0;x<numMsgs;++x)
      {
        if((i = isOnListToDelete(x, msgNums, msgNumAndSentDatesLen)) != -1)
        {  
          if(message[x].getSentDate() != null)
            sentDate = message[x].getSentDate().toString();
          else sentDate = "";

          if(sentDate.length() == 0)
            sentDate = "&lt;none&gt;";
          if(sentDates[i].equals(sentDate))
          {
            message[x].setFlag(Flags.Flag.DELETED, true);
          }
        }
      }
    }
    catch(Exception e) { System.out.println(e); }

    folder.close(true); // removes deleted messages
    store.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private int isOnListToDelete(int x, int[] msgNumAndSentDates, int msgNumAndSentDatesLen) throws Exception
  {
    for(int i=0;i<msgNumAndSentDatesLen;++i)
    {
      if(msgNumAndSentDates[i] == x)
        return i;
    }
    
    return -1;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void reDisplay(PrintWriter out, String unm, String sid, String uty, String dnm, String men, String den, String bnm, String localDefnsDir, String p1) throws Exception
  {
    URL url = new URL("http://" + serverUtils.serverToCall("MAIL", localDefnsDir) + "/central/servlet/MailZaraListMail?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=" + generalUtils.sanitise(p1) + "&bnm=" + bnm);
    URLConnection uc = url.openConnection();
    uc.setDoInput(true);
    uc.setUseCaches(false);
    uc.setDefaultUseCaches(false);

    uc.setRequestProperty("Content-Type", "application/octet-stream");

    BufferedReader di = new BufferedReader(new InputStreamReader(uc.getInputStream()));

    String s = di.readLine();
    while(s != null)
    {
      out.println(s);
      s = di.readLine();
    }

    di.close();
  }

}
