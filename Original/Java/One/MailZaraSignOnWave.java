// =======================================================================================================================================================================================================
// System: ZaraWave: getMailAccounts
// Module: MailZaraSignOnWave.java
// Author: C.K.Harvey
// Copyright (c) 2009 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
//
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.io.*;
import javax.mail.*;

public class MailZaraSignOnWave extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="";

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

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, bytesOut);
    }
    catch(Exception e)
    {
      System.out.println("8014w: " + e);
      serverUtils.etotalBytes(req, unm, dnm, 8014, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush();
    }
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String uName = directoryUtils.getMySQLUserName();
    String pWord = directoryUtils.getMySQLPassWord();
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Connection conMail = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_mail?user=" + uName + "&password=" + pWord);
    Statement stmt = null;
    ResultSet rs   = null;

    getPOP3Accounts(conMail, stmt, rs, out, unm);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 8014, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(conMail != null) conMail.close();
    if(con  != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void getPOP3Accounts(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String unm)
  {
    String account, pop3Server, userName, passWord;

    Properties props;
    Session session;
    Store store = null;
    Folder folder = null;
    int numMsgs;

    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT AccountName, Server, UserName, PassWord FROM mailaccounts WHERE Owner = '" + unm + "' AND Type = 'POP3'");

      while(rs.next())
      {
        account    = rs.getString(1);
        pop3Server = rs.getString(2);
        userName   = rs.getString(3);
        passWord   = rs.getString(4);

        out.print(account + "\001");

        try
        {
          props = new Properties();
          session = Session.getDefaultInstance(props, null);

          store = session.getStore("pop3");
          store.connect(pop3Server, userName, passWord);

          folder = store.getFolder("INBOX");

          folder.open(Folder.READ_ONLY);

          numMsgs = folder.getMessageCount();

          folder.close(false);
          store.close();

          out.print(numMsgs + "\001");
        }
        catch(Exception e) // catch login failures
        {
          try
          {
            out.print("Busy\001");
          }
          catch(Exception e2) { }
        }
      }

      if(rs != null) rs.close();
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println("8014w: " + e);
      try
      {
        if(rs   != null) rs.close();
        if(stmt != null) stmt.close();
      }
      catch(Exception e2) { }
    }
  }

}

