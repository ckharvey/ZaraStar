// =======================================================================================================================================================================================================
// System: ZaraStar: Utils: SignOn - direct external access
// Module: ExternalAccess.java
// Author: C.K.Harvey
// Copyright (c) 1998-2009 Christopher Harvey
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.sql.*;

public class ExternalAccess extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", bnm="", sid="", dnm="", men="", den="", p1="", p2="", p3="";

    try
    {
      directoryUtils.setContentHeaders(res);
      out = res.getWriter();

      unm = req.getParameter("unm");
      bnm = req.getParameter("bnm");
      sid = req.getParameter("sid");
      dnm = req.getParameter("dnm");
      men = req.getParameter("men");
      den = req.getParameter("den");
      p1  = req.getParameter("p1"); // servlet
      p2  = req.getParameter("p2"); // docCode
      p3  = req.getParameter("p3"); // additional param ... not used

      if(p1 == null) p1 = "";
      if(p2 == null) p2 = "";
      if(p3 == null) p3 = "";

      doIt(out, req, p1, p2, p3, unm, sid, bnm, dnm, men, den, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, "", dnm, bnm, urlBit, men, den, "A", "MainPageUtilsb", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 100, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush(); 
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String p1, String p2, String p3, String unm, String sid, String bnm, String dnm, String men, String den, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    
    Statement stmt = null;
    ResultSet rs   = null;

    scoutln(out, bytesOut, "<html><head><title>SignOn</title>");

    String cssDirectory = directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, localDefnsDir);

    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + cssDirectory + "general.css\">");

    scoutln(out, bytesOut, "<script javascript=\"JavaScript\">function go(){document.go2.submit();return true;}");

    scoutln(out, bytesOut, "</script>");

    int[] hmenuCount = new int[1];

    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "100", "", false, false, "", "", "", "", "", "MainPageUtilsb", unm, sid, "A", men, den, dnm, bnm, "", localDefnsDir, "", hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<script type='text/javascript'>function setup(){}</script>");

    scoutln(out, bytesOut, "<form name=\"go2\" action=\"MainPageUtilsd\" enctype=\"application/x-www-form-urlencoded\" method=POST>");

    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"unm\" value='" + unm + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"atunm\" value='" + unm + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"men\" value='" + men + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"den\" value='" + den + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"dnm\" value='" + dnm + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"bnm\" value='" + bnm + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"p1\"  value='" + p1  + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"p2\"  value='" + men  + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"p4\"  value='" + p2  + "'>");

    scoutln(out, bytesOut, "<table id=\"page\" border=0 cellpadding=5 width=100%>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap width=150></td><td colspan=2><p>Please enter your password...</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td></td><td nowrap width=95%><p>PassWord &nbsp;&nbsp;&nbsp;<input type=password name=pwd size=20></td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td><p><a href=\"javascript:go()\">SignOn</a></td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, "", localDefnsDir));

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 100, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con  != null) con.close();
    if(out != null) out.flush(); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {
    out.println(str);
    bytesOut[0] += (str.length() + 2);
  }

}
