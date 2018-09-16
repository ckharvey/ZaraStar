// =======================================================================================================================================================================================================
// System: ZaraStar: Contacts: Upload address book - update
// Module: ContactsAddressBookUpdate.java
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
import java.util.*;
import java.io.*;
import java.sql.*;

public class ContactsAddressBookUpdate extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  ReportGeneration reportGeneration = new ReportGeneration();
  Profile profile = new Profile();

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

      byte[] entries    = new byte[1000]; entries[0]    = '\000';
      int[]  entriesLen = new int[1];     entriesLen[0] = 1000;

      int thisEntryLen, inc;
      String value[];
      String name;

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
        if(name.length() > 2 && name.substring(1, 3).equals(".x")) ;
        else
        if(name.length() > 2 && name.substring(1, 3).equals(".y")) ;
        else // must be checkbox value
        {
          thisEntryLen = name.length() + 2;
          if((generalUtils.lengthBytes(entries, 0) + thisEntryLen) >= entriesLen[0])
          {
            byte[] tmp = new byte[entriesLen[0]];
            System.arraycopy(entries, 0, tmp, 0, entriesLen[0]);
            if(thisEntryLen > 1000)
              inc = thisEntryLen;
            else inc = 1000;
            entriesLen[0] += inc;
            entries = new byte[entriesLen[0]];
            System.arraycopy(tmp, 0, entries, 0, entriesLen[0] - inc);
          }

          generalUtils.catAsBytes(name, 0, entries, false);
        }
      }

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, entries, bytesOut);
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
 
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "ContactsAddressBookUpdate", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8812, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, byte[] entries, int[] bytesOut) throws Exception
  {
    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    
    long startTime = new java.util.Date().getTime();

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 8801, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "ContactsAddressBookUpdate", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8812, bytesOut[0], 0, "ACC:");
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "ContactsAddressBookUpdate", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8812, bytesOut[0], 0, "SID:");
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    go(con, stmt, rs, entries, unm);
    messagePage.msgScreen(false, out, req, 4, unm, sid, uty, men, den, dnm, bnm, "8812", imagesDir, localDefnsDir, defnsDir, bytesOut);
    
    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 8812, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con  != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void go(Connection con, Statement stmt, ResultSet rs, byte[] entries, String unm) throws Exception
  {
    int x=0;
    String eMailAddr, names, companyName;
    int len = generalUtils.lengthBytes(entries, 0);
    while(x < len)
    {
      eMailAddr = "";
      while(entries[x] != '\001' && entries[x] != '\000')
        eMailAddr += (char)entries[x++];
      eMailAddr = generalUtils.stripLeadingAndTrailingSpaces(generalUtils.deSanitise(eMailAddr));
      if(eMailAddr.length() >= 60)
        eMailAddr = eMailAddr.substring(0, 60);
      ++x;

      names = "";
      while(entries[x] != '\001' && entries[x] != '\000')
        names += (char)entries[x++];
      names = generalUtils.stripLeadingAndTrailingSpaces(generalUtils.deSanitise(names));
      if(names.length() >= 60)
        names = names.substring(0, 60);
      ++x;
      
      companyName = "";
      while(entries[x] != '\001' && entries[x] != '\000')
        companyName += (char)entries[x++];
      companyName = generalUtils.stripLeadingAndTrailingSpaces(generalUtils.deSanitise(companyName));
      if(companyName.length() >= 100)
        companyName = companyName.substring(0, 100);
      ++x;

      if(names.length() == 0)
        names = eMailAddr;
      
      profile.updateContacts(con, stmt, rs, 'N', "", names, companyName, "", "", "", "", "", "", "", "", "N", "", "", "", "", eMailAddr, "", "", unm, "");
    }
  }
  
}
