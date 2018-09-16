// =======================================================================================================================================================================================================
// System: ZaraStar Sales: update DOs returned
// Module: DeliveryOrdersReturned.java
// Author: C.K.Harvey
// Copyright (c) 2006-09 Christopher Harvey. All Rights Reserved.
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
import java.util.*;
import java.io.*;
import java.net.*;

public class DeliveryOrdersReturned extends HttpServlet
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

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="";

    try
    {
      directoryUtils.setContentHeaders(res);
      out = res.getWriter();

      String[] returnedCodes = new String[100];  int returnedCodesLen = 100;
      for(int x=0;x<100;++x) returnedCodes[x] = "";

      byte[] doCodes    = new byte[1000]; doCodes[0]    = '\000';
      int[]  doCodesLen = new int[1];     doCodesLen[0] = 1000;

      String[] notes = new String[100];  int notesLen = 100;
      for(int x=0;x<100;++x) notes[x] = "";

      String[] drivers = new String[100];  int driversLen = 100;
      for(int x=0;x<100;++x) drivers[x] = "";

      int thisEntryLen, inc, notesCount = 0, driversCount = 0, returnedCodesCount = 0;
      String doCode;

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
        if(name.length() > 2 && name.substring(1, 3).equals(".x")) ;
        else
        if(name.length() > 2 && name.substring(1, 3).equals(".y")) ;
        else
        {
          if(name.startsWith("d")) // drivers
          {
            if(driversCount == driversLen)
            {
              int z;
              String[] buf = new String[driversLen + 100];
              for(z=0;z<driversLen;++z)
                buf[z] = drivers[z];
              drivers = new String[driversLen + 100];
              for(z=0;z<driversLen;++z)
                drivers[z] = buf[z];
              driversLen += 100;
            }

            drivers[driversCount++] = (name.substring(1) + "\001" + value[0]);
            
            // and add to doCodes list also

            doCode = name.substring(1);

            thisEntryLen = doCode.length() + 2;

            if((generalUtils.lengthBytes(doCodes, 0) + thisEntryLen) >= doCodesLen[0])
            {
              byte[] tmp = new byte[doCodesLen[0]];
              System.arraycopy(doCodes, 0, tmp, 0, doCodesLen[0]);
              if(thisEntryLen > 1000)
                inc = thisEntryLen;
              else inc = 1000;
              doCodesLen[0] += inc;
              doCodes = new byte[doCodesLen[0]];
              System.arraycopy(tmp, 0, doCodes, 0, doCodesLen[0] - inc);
            }

            generalUtils.catAsBytes(doCode + "\001", 0, doCodes, false);
          }
          else // must be checkbox value
          if(name.startsWith("n")) // notes
          {
            if(notesCount == notesLen)
            {
              int z;
              String[] buf = new String[notesLen + 100];
              for(z=0;z<notesLen;++z)
                buf[z] = notes[z];
              notes = new String[notesLen + 100];
              for(z=0;z<notesLen;++z)
                notes[z] = buf[z];
              notesLen += 100;
            }

            notes[notesCount++] = (name.substring(1) + "\001" + value[0]);
          }
          else // must be checkbox value of one marked returned
          {
            thisEntryLen = name.length() + 2;

            if(returnedCodesCount == returnedCodesLen)
            {
              int z;
              String[] buf = new String[returnedCodesLen + 100];
              for(z=0;z<returnedCodesLen;++z)
                buf[z] = returnedCodes[z];
              returnedCodes = new String[returnedCodesLen + 100];
              for(z=0;z<returnedCodesLen;++z)
                returnedCodes[z] = buf[z];
              returnedCodesLen += 100;
            }

            returnedCodes[returnedCodesCount++] = (name.substring(1) + "\001" + value[0]);
          }
        }
      }

      doIt(out, req, returnedCodes, returnedCodesCount, doCodes, notes, notesCount, drivers, driversCount, unm, sid, uty, men, den, dnm, bnm, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "DeliveryOrdersReturned", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2043, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush(); 
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String[] returnedCodes, int returnedCodesCount, byte[] doCodes, String[] notes, int notesCount, String[] drivers, int driversCount, String unm, String sid, String uty, String men,
                    String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 2043, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "DeliveryOrdersReturned", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2043, bytesOut[0], 0, "ACC:");
      if(con  != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "DeliveryOrdersReturned", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2043, bytesOut[0], 0, "SID:");
      if(con  != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    String today = generalUtils.todaySQLFormat(localDefnsDir, defnsDir);

    String code;
    String[] note         = new String[1];
    String[] driver       = new String[1];
    String[] returnedCode = new String[1];

    int x=0, len = generalUtils.lengthBytes(doCodes, 0);
    while(x < len)
    {
      code = "";
      while(doCodes[x] != '\001' && doCodes[x] != '\000')
        code += (char)doCodes[x++];

      getCorrespondingDriver(code, drivers, driversCount, driver);

      getCorrespondingNote(code, notes, notesCount, note);

      note[0] = generalUtils.stripLeadingAndTrailingSpaces(note[0]);

      getCorrespondingReturned(code, returnedCodes, returnedCodesCount, returnedCode);

      updateDO(con, stmt, code, note[0], driver[0], returnedCode[0], today, unm);

      ++x;
    }

    reDisplay(out, unm, sid, uty, men, den, dnm, bnm, localDefnsDir);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 2043, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con  != null) con.close();
    if(out != null) out.flush(); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void getCorrespondingDriver(String doCode, String[] drivers, int driversCount, String[] driver) throws Exception
  {
    driver[0] = "";

    int x = 0, y, len;
    String thisDOCode;
    boolean found = false;

    while(! found && x < driversCount) // just-in-case
    {
      y = 0;
      len = drivers[x].length();
      thisDOCode = "";
      while(y < len && drivers[x].charAt(y) != '\001') // just-in-case
        thisDOCode += drivers[x].charAt(y++);
      ++y;

      if(thisDOCode.equals(doCode))
      {
        driver[0] = "";
        while(y < len)
          driver[0] += drivers[x].charAt(y++);
        found = true;
      }
      else ++x;
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void getCorrespondingNote(String doCode, String[] notes, int notesCount, String[] note) throws Exception
  {
    note[0] = "";

    int x = 0, y, len;
    String thisDOCode;
    boolean found = false;

    while(! found && x < notesCount) // just-in-case
    {
      y = 0;
      len = notes[x].length();
      thisDOCode = "";
      while(y < len && notes[x].charAt(y) != '\001') // just-in-case
        thisDOCode += notes[x].charAt(y++);
      ++y;

      if(thisDOCode.equals(doCode))
      {
        note[0] = "";
        while(y < len)
          note[0] += notes[x].charAt(y++);
        found = true;
      }
      else ++x;
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void getCorrespondingReturned(String doCode, String[] returnedCodes, int returnedCodesCount, String[] returnedCode) throws Exception
  {
    returnedCode[0] = "";

    int x = 0, y, len;
    String thisDOCode;
    boolean found = false;

    while(! found && x < returnedCodesCount) // just-in-case
    {
      y = 0;
      len = returnedCodes[x].length();
      thisDOCode = "";
      while(y < len && returnedCodes[x].charAt(y) != '\001') // just-in-case
        thisDOCode += returnedCodes[x].charAt(y++);
      ++y;

      if(thisDOCode.equals(doCode))
      {
        returnedCode[0] = "";
        while(y < len)
          returnedCode[0] += returnedCodes[x].charAt(y++);

        found = true;
      }
      else ++x;
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void updateDO(Connection con, Statement stmt, String doCode, String note, String driver, String returnedCode, String today, String unm) throws Exception
  {
    stmt = con.createStatement();

    if(returnedCode.length() == 0)
    {
      stmt.executeUpdate("UPDATE do SET Notes = '" + generalUtils.sanitiseForSQL(note) + "', DeliveryDriver = '" + generalUtils.sanitiseForSQL(driver) + "', DateLastModified = NULL, SignOn = '" + unm + "' WHERE DOCode = '" + doCode + "'");
    }
    else
    {
      stmt.executeUpdate("UPDATE do SET Returned = '1', Notes = '" + generalUtils.sanitiseForSQL(note) + "', DeliveryDriver = '" + generalUtils.sanitiseForSQL(driver) + "', DateReturned = {d '" + today + "'}, DateLastModified = NULL, SignOn = '" + unm
                       + "' WHERE DOCode = '" + doCode + "'");
    }

    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void reDisplay(PrintWriter out, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir) throws Exception
  {
    URL url = new URL("http://" + serverUtils.serverToCall("OFSA", localDefnsDir) + "/central/servlet/DeliveryOrderUpdating?unm=" + unm  + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm);

    HttpURLConnection uc = (HttpURLConnection)url.openConnection();
    uc.setDoInput(true);
    uc.setUseCaches(false);
    uc.setDefaultUseCaches(false);
    uc.setRequestMethod("GET");

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
