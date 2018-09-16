// =======================================================================================================================================================================================================
// System: ZaraStar MailEngine: Account Admin - Update Account Details
// Module: MailZaraAccountAdminUpdate.java
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
import java.util.*;
import java.io.*;
import java.sql.*;
import java.net.*;

public class MailZaraAccountAdminUpdate extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  MailUtils mailUtils = new MailUtils();
  
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", option="", userName="", passWord="", server="", account="", address="", desc="", type="", fromZaraAdmin="", userCode="";

    try
    {
      directoryUtils.setContentHeaders(res);
      out = res.getWriter(); 

      int len = req.getContentLength();
      ServletInputStream in  = req.getInputStream();

      Hashtable hashTable = HttpUtils.parsePostData(len, in);
      in.close();

      String value[];
      String s;
      for(Enumeration e=hashTable.keys();e.hasMoreElements();)
      {
        s = (String)e.nextElement();
        if(s.equals("unm"))
        {
          value = (String[])(hashTable.get("unm"));
          unm = value[0];
        }
        else
        if(s.equals("sid"))
        {
          value = (String[])(hashTable.get("sid"));
          sid = value[0];
        }
        else
        if(s.equals("uty"))
        {
          value = (String[])(hashTable.get("uty"));
          uty = value[0];
        }
        else
        if(s.equals("men"))
        {
          value = (String[])(hashTable.get("men"));
          men = value[0];
        }
        else
        if(s.equals("den"))
        {
          value = (String[])(hashTable.get("den"));
          den = value[0];
        }
        else
        if(s.equals("dnm"))
        {
          value = (String[])(hashTable.get("dnm"));
          dnm = value[0];
        }
        else
        if(s.equals("bnm"))
        {
          value = (String[])(hashTable.get("bnm"));
          bnm = value[0];
        }
        else
        if(s.length() > 2 && s.substring(1, 3).equals(".x"))
        {
          option = s;
        }
        else
        if(s.length() > 2 && s.substring(1, 3).equals(".y"))
        {
          ;
        }
        else
        if(s.equals("userName"))
        {
          value = (String[])(hashTable.get("userName"));
          userName = value[0];
        }
        else
        if(s.equals("passWord"))
        {
          value = (String[])(hashTable.get("passWord"));
          passWord = value[0];
        }
        else
        if(s.equals("server"))
        {
          value = (String[])(hashTable.get("server"));
          server = value[0];
        }
        else
        if(s.equals("account"))
        {
          value = (String[])(hashTable.get("account"));
          account = value[0];
        }
        else
        if(s.equals("address"))
        {
          value = (String[])(hashTable.get("address"));
          address = value[0];
        }
        else
        if(s.equals("desc"))
        {
          value = (String[])(hashTable.get("desc"));
          desc = value[0];
        }
        else
        if(s.equals("type"))
        {
          value = (String[])(hashTable.get("type"));
          type = value[0];
        }
        else
        if(s.equals("fromZaraAdmin"))
        {
          value = (String[])(hashTable.get("fromZaraAdmin"));
          fromZaraAdmin = value[0];
        }
        else
        if(s.equals("userCode"))
        {
          value = (String[])(hashTable.get("userCode"));
          userCode = value[0];
        }
      }

      account = generalUtils.deSanitise(account);
      
      doIt(out, req, fromZaraAdmin, userCode, option, userName, passWord, server, account, address, desc, type, unm, sid, uty, men, den, dnm, bnm, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "MailZaraAccountAdminUpdate", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8022, bytesOut[0], 0, "ERR:" + userName);
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String fromZaraAdmin, String userCode, String option, String userName, String passWord, String server, String account, String address, String desc, String type, String unm, String sid,
                    String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 8022, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "MailZaraAccountAdmin", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8022, bytesOut[0], 0, "ACC:" + userName);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "MailZaraAccountAdmin", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8022, bytesOut[0], 0, "SID:" + userName);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }
    
    if(option.length() > 0 && option.charAt(0) == 'D')
    {
      if(mailUtils.deleteFromMailAccounts(account, userCode, dnm))
        refetch(out, fromZaraAdmin, userCode, unm, sid, uty, dnm, men, den, bnm, localDefnsDir); // processed ok
      else messagePage.msgScreen(false, out, req, 7, unm, sid, uty, men, den, dnm, bnm, "8022", imagesDir, localDefnsDir, defnsDir, bytesOut); // Record not Updated
    }
    else
    {
      if(mailUtils.updateMailAccounts(account, server, address, userName, passWord, desc, type, userCode, dnm))
        refetch(out, fromZaraAdmin, userCode, unm, sid, uty, dnm, men, den, bnm, localDefnsDir);
      else messagePage.msgScreen(false, out, req, 7, unm, sid, uty, men, den, dnm, bnm, "8022", imagesDir, localDefnsDir, defnsDir, bytesOut); // Record not Updated
    }

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 8022, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), userName);
    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void refetch(PrintWriter out, String fromZaraAdmin, String userCode, String unm, String sid, String uty, String dnm, String men, String den, String bnm, String localDefnsDir) throws Exception
  {
    URL url = new URL("http://" + serverUtils.serverToCall("MAIL", localDefnsDir) + "/central/servlet/MailZaraAccountAdmin?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=" + userCode + "&p1="
                    + fromZaraAdmin);
    
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
