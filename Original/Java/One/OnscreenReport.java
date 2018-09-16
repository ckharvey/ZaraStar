// =======================================================================================================================================================================================================
// System: ZaraStar Utils: OnScreen PPR report
// Module: OnscreenReport.java
// Author: C.K.Harvey
// Copyright (c) 1998-2008 Christopher Harvey. All Rights Reserved.
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

public class OnscreenReport extends HttpServlet
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
      directoryUtils.setContentHeaders(res);
      out = res.getWriter();

      unm = req.getParameter("unm");
      sid = req.getParameter("sid");
      uty = req.getParameter("uty");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");
      p1  = req.getParameter("p1"); // fileName

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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "_G003", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 4005, bytesOut[0], 0, "");
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                    String p1, int[] bytesOut) throws Exception
  {
    String reportsDir = directoryUtils.getUserDir('R', dnm, unm);
    String imagesDir  = directoryUtils.getSupportDirs('I');

    setHead(out, bytesOut);
    setBody(out, imagesDir, bytesOut);

    FileInputStream fis = new FileInputStream(reportsDir + p1);
    BufferedInputStream fileInput = new BufferedInputStream(fis);

    int ch=0, x, y=0;
    short quit=0;
    double dnCoord, lastDnCoord=-1;
    String font="", weight="", style="", size="", text="";
    String coord;
    String line="";

    boolean isWhiteLine = false;
    ch = fileInput.read();
    while(quit == 0)
    {
      if(ch == -1)
        quit = 1;
      else
      {
        x=0;
        line="";
        while(ch != 0x0a && ch != 0x0d)
        {
          line += (char)ch;
          ch = fileInput.read();
        }

        while(ch == 0x0a || ch == 0x0d)
          ch = fileInput.read();

        switch(line.charAt(0))
        {
          case 'D' : // data line
                     x=2; y=0;
                     coord="";
                     while(x < line.length() && line.charAt(x) != ',')
                     {
                       coord += line.charAt(x);
                       ++x;
                     }

                     ++x; // step over ','
                     y=0;
                     while(x < line.length() && line.charAt(x) != ',') // ignore ax coord
                       ++x;

                     ++x;
                     font="";
                     while(x < line.length() && line.charAt(x) != ',')
                     {
                       font += line.charAt(x);
                       ++x;
                     }

                     ++x;
                     weight="";
                     while(x < line.length() && line.charAt(x) != ',')
                     {
                       weight += line.charAt(x);
                       ++x;
                     }

                     ++x;
                     style="";
                     while(x < line.length() && line.charAt(x) != ',')
                     {
                       style += line.charAt(x);
                       ++x;
                     }

                     ++x;
                     size="";
                     while(x < line.length() && line.charAt(x) != ',')
                     {
                       size += line.charAt(x);
                       ++x;
                     }

                     ++x;
                    
                     while(x < line.length() && line.charAt(x) != ',')
                     {
                       ++x;
                     }

                     x+=2; // step over ',' and '"'
                     text="";
                     while(x < line.length() && line.charAt(x) != '"')
                     {
                       text += line.charAt(x);
                       ++x;
                     }

                     dnCoord = generalUtils.doubleFromStr(coord);
                     if(dnCoord != lastDnCoord)
                     {
                       if(isWhiteLine)
                         isWhiteLine = false;
                       else isWhiteLine = true;

                       setNextLine(out, isWhiteLine, imagesDir, bytesOut);

                       lastDnCoord = dnCoord;
                     }
                     setItem(out, isWhiteLine, font, weight, style, size, text, imagesDir, bytesOut);
                     break;
        }
      }
    }

    fileInput.close();
    fis.close();

    setFoot(out, bytesOut);

    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void setHead(PrintWriter out, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Report: OnScreen</title></head>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void setBody(PrintWriter out, String imagesDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<body id=\"generalPageFrame\">");
    scoutln(out, bytesOut, "<p><form><table border=0 cellpadding=3 cellspacing=0><tr>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void setFoot(PrintWriter out, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "</tr></table></form></font></body></html>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void setNextLine(PrintWriter out, boolean whiteLine, String imagesDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "</tr><tr><td nowrap width=30 background=\"" + imagesDir);

    if(whiteLine)
      scoutln(out, bytesOut, "z175");
    else scoutln(out, bytesOut, "z173");

    scoutln(out, bytesOut, ".gif\">.</td>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void setItem(PrintWriter out, boolean whiteLine, String font, String weight, String style, String size, String text, String imagesDir,
                       int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<td nowrap background=\"" + imagesDir);
    if(whiteLine)
      scoutln(out, bytesOut, "z176");
    else scoutln(out, bytesOut, "z174");

    scoutln(out, bytesOut, ".gif\">");

    if(weight.charAt(0) == 'B')
      scoutln(out, bytesOut, "<b>");

    if(weight.charAt(0) == 'I')
      scoutln(out, bytesOut, "<i>");

    scoutln(out, bytesOut, "<p>" + text);

    if(weight.charAt(0) == 'B')
      scoutln(out, bytesOut, "</i>");

    if(weight.charAt(0) == 'I')
      scoutln(out, bytesOut, "</b>");

    scoutln(out, bytesOut, "</td>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
