// =======================================================================================================================================================================================================
// System: ZaraStar Engineer: update SO
// Module: SalesOrderEngineerUpdate.java
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

public class SalesOrderEngineerUpdate extends HttpServlet
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

      byte[] drawCodes    = new byte[1000]; drawCodes[0]    = '\000';
      int[]  drawCodesLen = new int[1];    drawCodesLen[0] = 1000;

      byte[] engAppCodes    = new byte[1000]; engAppCodes[0]    = '\000';
      int[]  engAppCodesLen = new int[1];    engAppCodesLen[0] = 1000;

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

          if(name.startsWith("draw"))
          {          
            if((generalUtils.lengthBytes(drawCodes, 0) + thisEntryLen) >= drawCodesLen[0])
            {
              byte[] tmp = new byte[drawCodesLen[0]];
              System.arraycopy(drawCodes, 0, tmp, 0, drawCodesLen[0]);
              if(thisEntryLen > 1000)
                inc = thisEntryLen;
              else inc = 1000;
              drawCodesLen[0] += inc;
              drawCodes = new byte[drawCodesLen[0]];
              System.arraycopy(tmp, 0, drawCodes, 0, drawCodesLen[0] - inc);
            }
            
            generalUtils.catAsBytes(name + "\001", 0, drawCodes, false);
          }
          else
          if(name.startsWith("engApp"))
          {          
            if((generalUtils.lengthBytes(engAppCodes, 0) + thisEntryLen) >= engAppCodesLen[0])
            {
              byte[] tmp = new byte[engAppCodesLen[0]];
              System.arraycopy(engAppCodes, 0, tmp, 0, engAppCodesLen[0]);
              if(thisEntryLen > 1000)
                inc = thisEntryLen;
              else inc = 1000;
              engAppCodesLen[0] += inc;
              engAppCodes = new byte[engAppCodesLen[0]];
              System.arraycopy(tmp, 0, engAppCodes, 0, engAppCodesLen[0] - inc);
            }
            
            generalUtils.catAsBytes(name + "\001", 0, engAppCodes, false);
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
            
      doIt(out, req, drawCodes, engAppCodes, salesPeople, unm, sid, uty, men, den, dnm, bnm, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "SalesOrderEngineerUpdate", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2031, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, byte[] drawCodes, byte[] engAppCodes, byte[] salesPeople, String unm, String sid,
                    String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');

    long startTime = new java.util.Date().getTime();

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 2031, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "SalesOrderEngineerUpdate", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2031, bytesOut[0], 0, "ACC:");
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "SalesOrderEngineerUpdate", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2031, bytesOut[0], 0, "SID:");
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    String today = generalUtils.todaySQLFormat(localDefnsDir, defnsDir);
    
    String code;

    int x=0, len = generalUtils.lengthBytes(drawCodes, 0);
    while(x < len)
    {
      code = "";
      while(drawCodes[x] != '\001' && drawCodes[x] != '\000')
        code += (char)drawCodes[x++];

      updateSO(con, stmt, code.substring(4), "DrawingsFiled", today, unm);

      ++x;
    }

    x=0;
    len = generalUtils.lengthBytes(engAppCodes, 0);
    while(x < len)
    {
      code = "";
      while(engAppCodes[x] != '\001' && engAppCodes[x] != '\000')
        code += (char)engAppCodes[x++];

      updateSO(con, stmt, code.substring(6), "EngineeringApproved", today, unm);

      ++x;
    }

    reDisplay(out, salesPeople, unm, sid, uty, men, den, dnm, bnm, localDefnsDir);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 2031, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
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
    URL url = new URL("http://" + serverUtils.serverToCall("OFSA", localDefnsDir) + "/central/servlet/SalesOrderEngineeringExecute?unm=" + unm  + "&sid=" + sid + "&uty=" + uty
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

