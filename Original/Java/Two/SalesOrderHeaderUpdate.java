// =======================================================================================================================================================================================================
// System: SteeClaws DocumentEngine: SO update header rec
// Module: SalesOrderHeaderUpdate.java
// Author: C.K.Harvey
// Copyright (c) 2001-06 Christopher Harvey. All Rights Reserved.
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
import java.net.*;
import java.io.*;
import java.sql.*;

public class SalesOrderHeaderUpdate extends HttpServlet
{
  GeneralUtils  generalUtils = new GeneralUtils();
  MessagePage  messagePage = new MessagePage();
  ErrorValidation  errorValidation = new ErrorValidation();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils  directoryUtils = new DirectoryUtils();
  SalesOrder salesOrder = new SalesOrder();

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
      directoryUtils.setContentHeaders(res);
      out = res.getWriter();

      unm = req.getParameter("unm");
      sid = req.getParameter("sid");
      uty = req.getParameter("uty");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");
      p1  = req.getParameter("p1"); // svc

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "SalesOrderHeaderUpdate", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 9400, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    set(p1, unm, sid, men, dnm, bnm);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 9400, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(out != null) out.flush();
    if(con != null) con.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String set(String svc, String unm, String sid, String men, String dnm, String bnm) throws Exception
  {
    String s ="";

    try
    {
      bnm = "M";

      fetchPage(men + "/central/servlet/_" + svc + "?unm=" + unm + "&sid=" + sid + "&uty=I&men=www.xxx.com.sg&den=" + dnm + "&dnm=" + dnm + "&bnm=" + bnm + "&iw=xxx.com:8080/xxx`" + unm + "`" + sid + "`");
    }
    catch(Exception e)
    {
      System.out.println("9400: " + e);
    }

    return s;
  }

  // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String fetchPage(String url)
  {
    String s = "";

    try
    {
      BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(url).openStream()));

      String line = reader.readLine();
      while(line != null)
      {
        s += line;
	line = reader.readLine();
     }
    }
    catch(Exception e)
    {
      System.out.println("9400: " + e);
    }

    return s;
  }

}
