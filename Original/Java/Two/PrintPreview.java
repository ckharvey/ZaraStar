// =======================================================================================================================================================================================================
// System: ZaraStar Utils: Create print preview page
// Module: PrintPreview.java
// Author: C.K.Harvey
// Copyright (c) 1998-2009 Christopher Harvey. All Rights Reserved.
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

public class PrintPreview extends HttpServlet
{
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
      p1  = req.getParameter("p1");

      doIt(out, unm, sid, uty, men, den, dnm, bnm, p1);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "PrintPreview", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 18, bytesOut[0], 0, "");
      if(out != null) out.flush(); 
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1) throws Exception
  {
    out.println("<html><head><title>Print</title></head>");
    out.println("<BODY BGCOLOR=\"#000088\" text=\"#000000\" link=\"#0000ff\" vlink=\"#0000ff\" alink=\"#0000ff\">");

    if(bnm.charAt(0) == 'M')
    {
      out.println("<OBJECT classid=\"clsid:8AD9C840-044E-11D1-B3E9-00805F499D93\" HEIGHT=550 WIDTH=700 align=left>");
      out.println("<PARAM name=\"code\" value=\"PrintLayoutExecution.class\">");
      out.println("<PARAM name=\"codebase\" value=\"/Zara/webapps/central/WEB-INF/classes/\">");
      out.println("<PARAM name=\"unm\" value=\"" + unm + "\">");
      out.println("<PARAM name=\"sid\" value=\"" + sid + "\">");
      out.println("<PARAM name=\"men\" value=\"" + men + "\">");
      out.println("<PARAM name=\"den\" value=\"" + den + "\">");
      out.println("<PARAM name=\"uty\" value=\"" + uty + "\">");
      out.println("<PARAM name=\"dnm\" value=\"" + dnm + "\">");
      out.println("<PARAM name=\"bnm\" value=\"" + bnm + "\">");
      out.println("<PARAM name=\"p1\" value=\""+p1+"\">");
      out.println("<PARAM name=\"p2\" value=\"1\">");
      out.println("</OBJECT>");
    }
    else
    {
      out.println("<EMBED code=\"PrintLayoutExecution.class\" HEIGHT=550 WIDTH=700 align=left  ");
      out.println("codebase=\"/Zara/webapps/central/WEB-INF/classes/\" type=\"application/x-java-applet\" ");
      out.println("unm=\"" + unm + "\" ");
      out.println("sid=\"" + sid + "\" ");
      out.println("men=\"" + men + "\" ");
      out.println("den=\"" + den + "\" ");
      out.println("uty=\"" + uty + "\" ");
      out.println("dnm=\"" + dnm + "\" ");
      out.println("bnm=\"" + bnm + "\" ");
      out.println("p1=\""+p1+"\" ");
      out.println("p2=\"1\"   >");
      out.println("</EMBED>");
    }

    out.println("</body></html>");
  }

}
