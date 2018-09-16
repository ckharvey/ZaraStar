// =======================================================================================================================================================================================================
// System: Zara: General: get report details
// Module: ReportDetails.java
// Author: C.K.Harvey
// Copyright (c) 1998-2004 Christopher Harvey. All Rights Reserved.
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

public class ReportDetails extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  MessagePage messagePage = new MessagePage();
  PrintingLayout printingLayout = new PrintingLayout();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;

    String unm="", uty="", men="", dnm="", bnm="", p1="", sid="", den="";

    try
    {
      res.setContentType("text/html");
      out = res.getWriter();

      unm = req.getParameter("unm");
      sid = req.getParameter("sid");
      uty = req.getParameter("uty");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");
      p1  = req.getParameter("p1"); // fileName

      doIt(out, unm, dnm, p1);
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

      int[] bytesOut = new int[1];
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "ReportDetails", bytesOut);
      System.out.println("ReportDetails Err: " + p1 +  " " + e);
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, String unm, String dnm, String p1) throws Exception
  {
    String reportsDir = directoryUtils.getUserDir('R', dnm, unm);

    RandomAccessFile fh = generalUtils.fileOpenD(p1, reportsDir);

    if(fh != null)
    {
      int numPages = printingLayout.getNumPages(fh);

      fh.seek(0);
      double[] width  = new double[1];
      double[] height = new double[1];
      char[] orientation = new char[1];
      printingLayout.getPageSize(fh, width, height, orientation);
      fh.close();

      int w, h;
      w = (int)width[0];
      h = (int)height[0];

      out.println(numPages + ":" + w + ":" + h + ":" + orientation[0] + ":");
    }
    else
    {
      out.println(0 + ":" + 0 + ":" + 0 + ":" + "L" + ":");
    }

    out.flush(); 
  }

}
