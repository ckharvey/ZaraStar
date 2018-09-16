// =======================================================================================================================================================================================================
// System: ZaraStar Sales: update SO
// Module: SalesOrdersUpdate.java
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
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;
import java.io.*;
import java.net.*;

public class SalesOrdersUpdate extends HttpServlet
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

      byte[] toEngCodes    = new byte[1000]; toEngCodes[0]    = '\000';
      int[]  toEngCodesLen = new int[1];    toEngCodesLen[0] = 1000;

      byte[] toProCodes    = new byte[1000]; toProCodes[0]    = '\000';
      int[]  toProCodesLen = new int[1];    toProCodesLen[0] = 1000;

      byte[] toSchCodes    = new byte[1000]; toSchCodes[0]    = '\000';
      int[]  toSchCodesLen = new int[1];    toSchCodesLen[0] = 1000;

      byte[] toManCodes    = new byte[1000]; toManCodes[0]    = '\000';
      int[]  toManCodesLen = new int[1];    toManCodesLen[0] = 1000;

      byte[] salesPeople   = new byte[1000]; salesPeople[0]    = '\000';
      int[]  salesPeopleLen = new int[1];   salesPeopleLen[0] = 1000;

      int thisEntryLen, inc;

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
        else // must be checkbox value
        {
          thisEntryLen = name.length() + 2;

          if(name.startsWith("toEng"))
          {          
            if((generalUtils.lengthBytes(toEngCodes, 0) + thisEntryLen) >= toEngCodesLen[0])
            {
              byte[] tmp = new byte[toEngCodesLen[0]];
              System.arraycopy(toEngCodes, 0, tmp, 0, toEngCodesLen[0]);
              if(thisEntryLen > 1000)
                inc = thisEntryLen;
              else inc = 1000;
              toEngCodesLen[0] += inc;
              toEngCodes = new byte[toEngCodesLen[0]];
              System.arraycopy(tmp, 0, toEngCodes, 0, toEngCodesLen[0] - inc);
            }
            
            generalUtils.catAsBytes(name + "\001", 0, toEngCodes, false);
          }
          else
          if(name.startsWith("toPro"))
          {          
            if((generalUtils.lengthBytes(toProCodes, 0) + thisEntryLen) >= toProCodesLen[0])
            {
              byte[] tmp = new byte[toProCodesLen[0]];
              System.arraycopy(toProCodes, 0, tmp, 0, toProCodesLen[0]);
              if(thisEntryLen > 1000)
                inc = thisEntryLen;
              else inc = 1000;
              toProCodesLen[0] += inc;
              toProCodes = new byte[toProCodesLen[0]];
              System.arraycopy(tmp, 0, toProCodes, 0, toProCodesLen[0] - inc);
            }
            
            generalUtils.catAsBytes(name + "\001", 0, toProCodes, false);
          }
          else
          if(name.startsWith("toSch"))
          {          
            if((generalUtils.lengthBytes(toSchCodes, 0) + thisEntryLen) >= toSchCodesLen[0])
            {
              byte[] tmp = new byte[toSchCodesLen[0]];
              System.arraycopy(toSchCodes, 0, tmp, 0, toSchCodesLen[0]);
              if(thisEntryLen > 1000)
                inc = thisEntryLen;
              else inc = 1000;
              toSchCodesLen[0] += inc;
              toSchCodes = new byte[toSchCodesLen[0]];
              System.arraycopy(tmp, 0, toSchCodes, 0, toSchCodesLen[0] - inc);
            }
            
            generalUtils.catAsBytes(name + "\001", 0, toSchCodes, false);
          }
          else
          if(name.startsWith("toMan"))
          {          
            if((generalUtils.lengthBytes(toManCodes, 0) + thisEntryLen) >= toManCodesLen[0])
            {
              byte[] tmp = new byte[toManCodesLen[0]];
              System.arraycopy(toManCodes, 0, tmp, 0, toManCodesLen[0]);
              if(thisEntryLen > 1000)
                inc = thisEntryLen;
              else inc = 1000;
              toManCodesLen[0] += inc;
              toManCodes = new byte[toManCodesLen[0]];
              System.arraycopy(tmp, 0, toManCodes, 0, toManCodesLen[0] - inc);
            }
            
            generalUtils.catAsBytes(name + "\001", 0, toManCodes, false);
          }
          else // salesPeople
          {          
            if((generalUtils.lengthBytes(salesPeople, 0) + thisEntryLen) >= salesPeopleLen[0])
            {
              byte[] tmp = new byte[salesPeopleLen[0]];
              System.arraycopy(salesPeople, 0, tmp, 0, salesPeopleLen[0]);
              if(thisEntryLen > 1000)
                inc = thisEntryLen;
              else inc = 1000;
              salesPeopleLen[0] += inc;
              salesPeople = new byte[salesPeopleLen[0]];
              System.arraycopy(tmp, 0, salesPeople, 0, salesPeopleLen[0] - inc);
            }
            
            generalUtils.catAsBytes(value[0] + "\001", 0, salesPeople, false);
          }
        }
      }
            
      doIt(out, req, toEngCodes, toProCodes, toSchCodes, toManCodes, salesPeople, unm, sid, uty, men, den, dnm, bnm, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "SalesOrdersUpdate", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2027, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, byte[] toEngCodes, byte[] toProCodes, byte[] toSchCodes, byte[] toManCodes,
                    byte[] salesPeople, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut)
                    throws Exception
  {
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');

    long startTime = new java.util.Date().getTime();

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 2027, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "SalesOrdersUpdate", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2027, bytesOut[0], 0, "ACC:");
      if(con  != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "SalesOrdersUpdate", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2027, bytesOut[0], 0, "SID:");
      if(con  != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    String today = generalUtils.todaySQLFormat(localDefnsDir, defnsDir);
    
    String code;

    int x=0, len = generalUtils.lengthBytes(toEngCodes, 0);
    while(x < len)
    {
      code = "";
      while(toEngCodes[x] != '\001' && toEngCodes[x] != '\000')
        code += (char)toEngCodes[x++];

      updateSO(con, stmt, code.substring(5), "toEngineering", today, unm);

      ++x;
    }

    x=0;
    len = generalUtils.lengthBytes(toProCodes, 0);
    while(x < len)
    {
      code = "";
      while(toProCodes[x] != '\001' && toProCodes[x] != '\000')
        code += (char)toProCodes[x++];

      updateSO(con, stmt, code.substring(5), "toProcurement", today, unm);

      ++x;
    }

    x=0;
    len = generalUtils.lengthBytes(toSchCodes, 0);
    while(x < len)
    {
      code = "";
      while(toSchCodes[x] != '\001' && toSchCodes[x] != '\000')
        code += (char)toSchCodes[x++];

      updateSO(con, stmt, code.substring(5), "toScheduling", today, unm);

      ++x;
    }

    x=0;
    len = generalUtils.lengthBytes(toManCodes, 0);
    while(x < len)
    {
      code = "";
      while(toManCodes[x] != '\001' && toManCodes[x] != '\000')
        code += (char)toManCodes[x++];

      updateSO(con, stmt, code.substring(5), "toManager", today, unm);

      ++x;
    }
    
    reDisplay(out, salesPeople, unm, sid, uty, men, den, dnm, bnm, localDefnsDir);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 2027, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con  != null) con.close();
    if(out != null) out.flush(); 
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void updateSO(Connection con, Statement stmt, String soCode, String fieldName, String today, String unm) throws Exception
  {
    stmt = con.createStatement();
    
    stmt.executeUpdate("UPDATE so SET " + fieldName + " = 'Y', " + fieldName + "Date = {d '" + today + "'}, " + fieldName + "SignOn = '" + unm
                     + "', DateLastModified = NULL, SignOn = '" + unm + "' WHERE SOCode = '" + soCode + "'");
    
    if(stmt != null) stmt.close();
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void reDisplay(PrintWriter out, byte[] salesPeople, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                         String localDefnsDir) throws Exception
  {
    URL url = new URL("http://" + serverUtils.serverToCall("OFSA", localDefnsDir) + "/central/servlet/SalesOrdersSales?unm=" + unm  + "&sid=" + sid + "&uty=" + uty
                    + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=" + generalUtils.replaceSpacesWith20(generalUtils.stringFromBytes(salesPeople, 0L))
                    + "&bnm=" + bnm);

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
