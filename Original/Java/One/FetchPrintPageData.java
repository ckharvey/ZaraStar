// =======================================================================================================================================================================================================
// System: Zara: Utils: fetch print page data
// Module: FetchPrintPageData.java
// Author: C.K.Harvey
// Copyright (c) 1998-2004 Christopher Harvey. All Rights Reserved.
// Where:  Server
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

public class FetchPrintPageData extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  PrintingLayout printingLayout = new PrintingLayout();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="";

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
      p2  = req.getParameter("p2"); // pageNum

      doIt(out, unm, sid, uty, men, den, dnm, bnm, p1, p2);
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
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "FetchPrintPageData", bytesOut);
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1, String p2) throws Exception
  {
    String reportsDir = directoryUtils.getUserDir('R', dnm, unm);
    out.println(printingLayout.getPageData(generalUtils.strToInt(p2), p1, reportsDir));
    if(out != null) out.flush(); 
  }

}
