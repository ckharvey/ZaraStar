// =======================================================================================================================================================================================================
// System: ZaraStar: Mail: get gmail using ATOM
// Module: MailZaraGetGmailATOM.java
// Author: C.K.Harvey
// Copyright (c) 2006-08 Christopher Harvey. All Rights Reserved.
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
import java.sql.*;
import java.util.*;
import com.sun.syndication.feed.synd.*;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

public class MailZaraGetGmailATOM extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils(); 

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
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
      men = req.getParameter("men");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");
      p1  = req.getParameter("p1"); // userName
      p2  = req.getParameter("p2"); // passWord
      p3  = req.getParameter("p3"); // mode

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, p3, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "MailZaraGetGmailATOM", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8024, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1, String p2, String p3, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 8014, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "MailZaraGetGmailATOM", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8024, bytesOut[0], 0, "ACC:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }
    
    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "MailZaraGetGmailATOM", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8024, bytesOut[0], 0, "SID:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    getData(out, p1, p2, p3, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 8024, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con  != null) con.close();
    if(out != null) out.flush();
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void getData(PrintWriter out, String userName, String passWord, String mode, int[] bytesOut) throws Exception
  {
    try
    {
      URL feedUrl = new URL("https://gmail.google.com/gmail/feed/atom");

      HttpURLConnection httpcon = (HttpURLConnection)feedUrl.openConnection();
      
      String encoding = new sun.misc.BASE64Encoder().encode((userName + ":" + passWord).getBytes());

      httpcon.setRequestProperty ("Authorization", "Basic " + encoding);

      SyndFeedInput input = new SyndFeedInput();

      SyndFeed feed = input.build(new XmlReader(httpcon));

      List entries = feed.getEntries();
      int numEntries = entries.size();
      out.print(numEntries + "\001");
        
      if(mode.equals("M")) // message
      {
        String link, author, title, date;
        Iterator it = entries.iterator();
			
        while(it.hasNext())
        {
    	  SyndEntryImpl e = (SyndEntryImpl)it.next();
          
          link   = e.getLink(); // =http://mail.google.com/mail?account_id=testuser%40gmail.com&message_id=11d33ef8661e8ce7&view=conv&extsrc=atom
          date   = e.getPublishedDate().toString();    // SyndEntryImpl.publishedDate=Sat Oct 25 20:16:55 SGT 2008 
          author = e.getAuthor(); // SyndEntryImpl.authors[0].email=hotwheels@mattel-email.com
          title  = e.getTitle();            // SyndEntryImpl.title = Brand NEW piping hot HotWheels.com game!

          out.print(author + "\001" + date + "\001" + title + "\001" + link + "\001");
        }
      }
      
      bytesOut[0] += feed.toString().length();
    }
    catch (Exception ex)
    {
      System.out.println("ERROR: "+ex.getMessage());
      out.println("X\001");
    }
  }
}
