// =======================================================================================================================================================================================================
// System: ZaraStar Utils: Print to a default printer
// Module: AdminPrintControlDefault.java
// Author: C.K.Harvey
// Copyright (c) 1999-2009 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.net.*;

public class AdminPrintControlDefaultWave extends HttpServlet
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

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="", p3="", p4="", p5="", p6="";

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
      p1  = req.getParameter("p1"); // servlet  - set if coming from a document rather a report
      p2  = req.getParameter("p2"); // code     - set if coming from a document rather a report
      p3  = req.getParameter("p3"); // option   - set if coming from a document rather a report
      p4  = req.getParameter("p4"); // numPages - set if coming from a report rather a document
      p5  = req.getParameter("p5"); // returnServletAndParams
      p6  = req.getParameter("p6"); // returnMsg

      if(p1 == null) p1 = "";
      if(p2 == null) p2 = "";
      if(p3 == null) p3 = "";
      if(p4 == null) p4 = "";
      if(p5 == null) p5 = "";
      if(p6 == null) p6 = "";

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, p3, p4, p5, p6, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "AdminPrintControl", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7400, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1, String p2, String p3, String p4, String p5, String p6, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreenW(out, req, 12, unm, sid, uty, men, den, dnm, bnm, "7400", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7400, bytesOut[0], 0, "SID:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    set(con, stmt, rs, out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, p3, p4, p5, p6, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 7400, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p2);
    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1, String p2, String p3, String p4,
                   String returnServletAndParams, String returnMsg, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    String numPages;
    if(p4.length() == 0) // coming from a document rather a report
      numPages = generate(unm, sid, uty, men, den, dnm, bnm, p1, p2, p3);
    else
    {
      numPages = p4;
      p2 = "Report";
    }

    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    if(uty.equals("I"))
    {
      scoutln(out, bytesOut, "<script language=\"JavaScript\">");

      scoutln(out, bytesOut, "function netprint(ptr){getHTML('AdminPrintControlNetworkWave','&p2=" + numPages + "&p4=" + generalUtils.sanitise(returnServletAndParams) + "&p5=" + generalUtils.sanitiseReplacingNewlines(returnMsg) + "&p1=0.000&p3='+ptr);}");

      scoutln(out, bytesOut, "</script>");
    }

    scoutln(out, bytesOut, "</head>");

    int[] hmenuCount = new int[1];
    hmenuCount[0]=0;

    scoutln(out, bytesOut, "<form>");

    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Print Options", "7400",  unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<table id=\"page\" width=100% cellpadding=3 cellspacing=3>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap colspan=3><p>" + p2 + " has " + numPages);
    if(numPages.equals("1"))
      scoutln(out, bytesOut, " page.</td></tr>");
    else scoutln(out, bytesOut, " pages.</td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    if(uty.equals("I"))
    {
      scoutln(out, bytesOut, "<tr><td nowrap><p>Print to network printer:</td>");
      getCUPSPrinters(out);
      scoutln(out, bytesOut, "</tr>");

      scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    }

    scoutln(out, bytesOut, "<tr><td nowrap><p>Print to <a href=\"/central/servlet/PrintPreview?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=0.000&bnm=" + bnm
                         + "\">local printer</a></td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "</table></form>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void getCUPSPrinters(PrintWriter out)
  {
    try
    {
      Process p;
      BufferedReader reader;

      Runtime r = Runtime.getRuntime();

      String commandArray = "/opt/csw/bin/lpstat -a";
      p = r.exec(commandArray);

      p.waitFor();

      InputStreamReader isr = new InputStreamReader(p.getInputStream());
      reader = new BufferedReader(isr);

      int x, y, len;
      String ptr;
      boolean first = true;
      while((ptr = reader.readLine()) != null)
      {
        if(ptr.charAt(0) == ' '|| ptr.charAt(0) == '\t')
          ;
        else
        {
          x=0;
          len = ptr.length();
          while(x < len && ptr.charAt(x) != ' ') // just-in-case
            ++x;

          if(first)
            first = false;
          else out.println("</tr><tr><td></td>");

          y = ptr.indexOf(" since");
          if(y == -1)
            y = ptr.length();

          out.println("<td><p>");

          if(ptr.charAt(ptr.length()-1) == '-')
            out.println(ptr.substring(0, x));
          else out.println("<a href=\"javascript:netprint('" + ptr.substring(0, x) + "')\">" + ptr.substring(0, x) + "</a>");

          out.println("</td><td><p>" + ptr.substring(x+1, y) + "</td>");
        }
      }
      reader.close();
      isr.close();
    }
    catch(Exception e)
    {
      out.println("<td colspan=2><p>Error connecting to printer</td>");
      System.out.println("7400: " + e);
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String generate(String unm, String sid, String uty, String men, String den, String dnm, String bnm, String servlet, String code, String option) throws Exception
  {
    URL url = new URL("http://" + men + "/central/servlet/" + servlet + "?unm=" + unm + "&sid=" + sid + "&&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=" + code + "&p2=" + option + "&dnm=" + dnm + "&bnm=" + bnm);

    URLConnection uc = url.openConnection();
    uc.setDoInput(true);
    uc.setUseCaches(false);
    uc.setDefaultUseCaches(false);

    uc.setRequestProperty("Content-Type", "application/octet-stream");

    BufferedReader di = new BufferedReader(new InputStreamReader(uc.getInputStream()));

    String s = di.readLine();

    String numPages = s;
    while(s != null)
      s = di.readLine();

    di.close();

    return numPages;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {
    out.println(str);
    bytesOut[0] += (str.length() + 2);
  }

}
