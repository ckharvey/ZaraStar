// =======================================================================================================================================================================================================
// System: ZaraStar: Fax: get status data from report temp file
// Module: FaxStatusDataFromReportTemp.java
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
import java.sql.*;
import java.util.Enumeration;

public class FaxStatusDataFromReportTemp extends HttpServlet
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
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="";

    try
    {
      directoryUtils.setContentHeaders(res);
      res.setHeader("Header", "HTTP/1.0 200 OK");
      res.setStatus(200);

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
        if(name.equals("p1"))
          p1 = value[0];
      }
      
      doIt(req, res, p1, unm, sid, uty, men, den, dnm, bnm, bytesOut);
    }    
    catch(Exception e)
    {
      System.out.println(("Unexpected System Error: 12602: " + e));
      res.getWriter().write("Unexpected System Error: 12602");
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(HttpServletRequest req, HttpServletResponse res, String p1, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String workingDir    = directoryUtils.getUserDir('W', dnm, unm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;
    
    String stuff="";
    
    String[] rtn = new String[1];
    String[] percentage = new String[1];
    stuff = getData(p1, workingDir, rtn, percentage);
    
    res.setContentType("text/xml");
    res.setHeader("Cache-Control", "no-cache");

    String s = "<msg><res>" + rtn[0] + "</res><stuff><![CDATA[" + stuff + "]]></stuff></msg>";
    res.getWriter().write(s);
    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 12602, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1+":"+percentage[0]);
    if(con != null) con.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String getData(String servlet, String workingDir, String[] rtn, String[] percentage) throws Exception
  {        
    String s = "";
    
    try
    {
      RandomAccessFile fhState = generalUtils.fileOpen(workingDir + servlet + ".state");  
      
      s = fhState.readLine();

      if(s == null)
        s = "";
      if(s.equals("100"))
      {
        rtn[0] = ".";
        percentage[0] = s;
        generalUtils.fileClose(fhState);

        RandomAccessFile fhData = generalUtils.fileOpen(workingDir + servlet + ".data");
        fhData.seek(0);
        s = "";
 
        try
        {
          String t = fhData.readLine();
          while(t != null)
          {
            s += (t + "\n");
            t = fhData.readLine();
          }
        }
        catch(Exception ee) { }
    
        generalUtils.fileClose(fhData);
      }
      else
      {
        rtn[0] = "%";
        percentage[0] = s;
        s = "<font size=7><br><br><br><br>" + s + "% complete, please wait...</font>";
        generalUtils.fileClose(fhState);
      }
    }
    catch (Exception e)
    {
      System.out.println("12602: " + e);
      rtn[0] = ".";
      return "";
    }
    
    return s;
  }
  
}
