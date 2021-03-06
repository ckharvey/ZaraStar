// =======================================================================================================================================================================================================
// System: ZaraStar CompanyEngine: Create customer listing report page
// Module: CustomerListingPage.java
// Author: C.K.Harvey
// Copyright (c) 2003-06 Christopher Harvey. All Rights Reserved.
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

public class CustomerListingPage extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  DrawingUtils drawingUtils = new DrawingUtils();

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

      unm = req.getParameter("unm");
      sid = req.getParameter("sid");
      uty = req.getParameter("uty");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "CustomerListingPage", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 4005, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men,
                      String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String reportsDir       = directoryUtils.getUserDir('R', dnm, unm);

    long startTime = new java.util.Date().getTime();

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 4005, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "4005", "", "", "", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 4005, bytesOut[0], 0, "ACC:");
      
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "4005", "", "", "", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 4005, bytesOut[0], 0, "SID:");
      
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }
    
    set(con, stmt, rs, out, req, unm, sid, uty, men, den, dnm, bnm, reportsDir, imagesDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 4005, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
      
      if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den,
                     String dnm, String bnm, String reportsDir, String imagesDir, String localDefnsDir, String defnsDir,
                     int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Customer Listing Report</title>");

    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" 
                           + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    int[] hmenuCount = new int[1];

    drawingUtils.outputPageFrame(con, stmt, rs, out, req, "4005", "", "CustomerListingPage", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount, bytesOut);

    drawingUtils.drawTitle(out, false, false, "CustomerListingPage", "", "Customer Listing Report", "4005", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<form><table id=\"page\" border=0 cellspacing=2 cellpadding=0 width=100%>");

    scoutln(out, bytesOut, "<tr><td nowrap><p>");
    scoutln(out, bytesOut, "<a href=\"/central/servlet/CustomerListingReport?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men="
                           + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm+"\">Generate</a> a new report</td></tr>");

    scoutln(out, bytesOut, "<tr><td><img src=\"" + imagesDir + "z402.gif\" border=0></td></tr>");
    scoutln(out, bytesOut, "<tr><td bgColor=#0033ff colspan=5><img height=1 src=\"" + imagesDir + "/1x1_red.gif\" width=100%></td></tr>");
    scoutln(out, bytesOut, "<tr><td><img src=\"" + imagesDir + "z402.gif\" border=0></td></tr>");

    scoutln(out, bytesOut, "<tr><td><h1>Reports on file:</td></tr>");

    File path = new File(reportsDir);
    String names[] = new String[0];
    names = path.list();
    String dateStr;
    boolean first = true;

    for(int i=0;i<names.length;++i)
    {
      if(names[i].endsWith("010"))
      {
        dateStr = generalUtils.yymmddExpand(true, names[i]);
        if(first)
        {
          out.println("<tr>");
          first = false;
        }
        scoutln(out, bytesOut, "<td></td>");

        scoutln(out, bytesOut, "<td nowrap><p>" + dateStr + "&nbsp;&nbsp;</td>");
                
        scoutln(out, bytesOut, "<td width=90% nowrap><p><a href=\"/central/servlet/OnscreenReport?unm=" + unm + "&sid=" + sid
                             + "&uty=" + uty + "&p1=" + names[i] + "&men=" + men + "&den=" + den + "&dnm=" + dnm
                             + "&bnm=" + bnm + "\">Screen</a>");
        
        scoutln(out, bytesOut, "&nbsp;&nbsp;&nbsp;<a href=\"/central/servlet/PrintPreview?unm=" + unm + "&uty=" + uty
                             + "&p1=" + names[i] + "&sid=" + sid + "&men=" + men + "&den=" + den + "&dnm=" + dnm
                             + "&bnm=" + bnm + "\">Printer</a>");

        scoutln(out, bytesOut, "&nbsp;&nbsp;&nbsp;<a href=\"/central/servlet/RemoveReport?unm=" + unm + "&uty=" + uty
                             + "&p1=" + names[i] + "&p2=CustomerListingPage&sid=" + sid + "&men=" + men + "&den=" + den + "&dnm="
                             + dnm + "&bnm=" + bnm + "\">Remove</a></td></tr>");
      }
    }

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td><a href=\"/central/servlet/CustomerListingPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">");
    scoutln(out, bytesOut, "<img src=\"" + imagesDir + "z358.gif\" border=0></a></td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "</form></div></table></body></html>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
