// =======================================================================================================================================================================================================
// System: ZaraStar AdminEngine: Set user services for casual, registered, or demo
// Module: AdminSetUserCasualRegisteredDemo.java
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
import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.Enumeration;

public class AdminSetUserCasualRegisteredDemo extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  Profile profile = new Profile();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", userCode="", serviceList="";

    try
    {
      directoryUtils.setContentHeaders(res);
      out = res.getWriter();

      byte[] checkBoxes    = new byte[1000]; checkBoxes[0]    = '\000';
      int[]  checkBoxesLen = new int[1];     checkBoxesLen[0] = 1000;

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
        if(name.equals("userCode"))
          userCode = value[0];
        else
        if(name.equals("serviceList"))
          serviceList = value[0];
        else
        if(name.equals("x")) ;
        else
        if(name.equals("y")) ;
        else // must be checkbox value
        {
          thisEntryLen = name.length() + 2;
          if((generalUtils.lengthBytes(checkBoxes, 0) + thisEntryLen) >= checkBoxesLen[0])
          {
            byte[] tmp = new byte[checkBoxesLen[0]];
            System.arraycopy(checkBoxes, 0, tmp, 0, checkBoxesLen[0]);
            if(thisEntryLen > 1000)
              inc = thisEntryLen;
            else inc = 1000;
            checkBoxesLen[0] += inc;
            checkBoxes = new byte[checkBoxesLen[0]];
            System.arraycopy(tmp, 0, checkBoxes, 0, checkBoxesLen[0] - inc);
          }

          generalUtils.catAsBytes(name + "\001", 0, checkBoxes, false);
        }
      }

      doIt(out, req, userCode, serviceList, checkBoxes, unm, sid, uty, men, den, dnm, bnm, bytesOut);
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
 
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "AdminSetUserCasualRegisteredDemo", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7009, bytesOut[0], 0, "ERR:" + userCode);
      if(out != null) out.flush();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String userCode, String serviceList, byte[] checkBoxes, String unm, String sid,
                    String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    long startTime = new java.util.Date().getTime();

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 7009, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "AdminSetUserCasualRegisteredDemo", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7009, bytesOut[0], 0, "ACC:" + userCode);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "AdminSetUserCasualRegisteredDemo", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7009, bytesOut[0], 0, "SID:" + userCode);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    set(con, stmt, userCode, serviceList, checkBoxes, dnm);

    reFetch(out, unm, sid, uty, dnm, men, den, bnm, userCode, localDefnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 7009, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), userCode);
    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, String userCode, String serviceList, byte[] checkBoxes, String dnm) throws Exception
  {
    String s;
    int x = 0, len = generalUtils.lengthBytes(checkBoxes, 0);
    while(x < len)
    {
      s = "";
      while(checkBoxes[x] != '\001' && checkBoxes[x] != '\000')
        s += (char)checkBoxes[x++];

      addService(con, stmt, userCode, s, dnm);
      
      serviceList = removeFromList(s, serviceList);
    
      ++x;
    }

    len = serviceList.length();
    x = 0;
    while(x < len)
    {
      s = "";
      while(x < len && serviceList.charAt(x) != '\001')
        s += serviceList.charAt(x++);
      ++x;
      
      removeService(con, stmt, userCode, s);
    }  
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void addService(Connection con, Statement stmt, String userCode, String service, String dnm) throws Exception
  {
    try
    {
      stmt = con.createStatement();
  
      stmt.executeUpdate("INSERT INTO userservices (UserCode, Service) VALUES ('" + userCode + "','" + service + "')");

      if(stmt != null) stmt.close();
      
      profile.deleteRightsFile(userCode, dnm);
    }
    catch(Exception e) // already exists
    {
      if(stmt != null) stmt.close();
    }    
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void removeService(Connection con, Statement stmt, String userCode, String service) throws Exception
  {
    stmt = con.createStatement();
  
    stmt.executeUpdate("DELETE FROM userservices WHERE UserCode = '" + userCode + "' AND Service = '" + service + "'");

    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private String removeFromList(String service, String serviceList) throws Exception
  {
    int x = 0, len = serviceList.length();
    String s, t = "";
    while(x < len)
    {
      s = "";
      while(x < len && serviceList.charAt(x) != '\001')
        s += serviceList.charAt(x++);
      ++x;
      
      if(! s.equals(service))
        t += (s + "\001");
    }  

    return t;
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void reFetch(PrintWriter out, String unm, String sid, String uty, String dnm, String men, String den, String bnm, String userCode,
                       String localDefnsDir, int[] bytesOut) throws Exception
  {
    URL url = new URL("http://" + serverUtils.serverToCall("OFSA", localDefnsDir) + "/central/servlet/_7009?unm=" + unm + "&sid=" + sid + "&uty=" + uty
                    + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=" + userCode + "&bnm=" + bnm);

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
      bytesOut[0] += s.length();
      s = di.readLine();
    }

    di.close();
  }

}
