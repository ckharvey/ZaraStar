// =======================================================================================================================================================================================================
// System: ZaraStar: Fax: abort fax
// Module: FaxAbortExecute.java
// Author: C.K.Harvey
// Copyright (c) 1999-2007 Christopher Harvey. All Rights Reserved.
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

public class FaxAbortExecute extends HttpServlet
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

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="";

    try
    {
      out = res.getWriter();
      directoryUtils.setContentHeaders(res);

      unm = req.getParameter("unm");
      sid = req.getParameter("sid");
      uty = req.getParameter("uty");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");
      p1  = req.getParameter("p1");

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "FaxAbortExecute", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 11004, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush(); 
    }
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String hylafaxCode, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    callFaxrm(hylafaxCode);
    
    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 6016, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), hylafaxCode);
    if(con  != null) con.close();
    if(out != null) out.flush(); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void callFaxrm(String hylafaxCode) throws Exception
  {
    Process p;
    BufferedReader reader;
    String currentLine;

    Runtime r = Runtime.getRuntime();

    String commandArray = "/Zara/Support/Scripts/Fax/faxrm.sh " + hylafaxCode;

    p = r.exec(commandArray);

    p.waitFor();
    System.out.println("Process exit code is: " + p.exitValue());

    InputStreamReader isr = new InputStreamReader(p.getInputStream());
    reader = new BufferedReader(isr);

    String res="";
    while((currentLine = reader.readLine()) != null)
      res += currentLine;

    reader.close();
    isr.close();
  }

}

