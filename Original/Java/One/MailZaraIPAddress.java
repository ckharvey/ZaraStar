// =======================================================================================================================================================================================================
// System: ZaraStar: Mail: get ip addresses
// Module: MailZaraIPAddress.java
// Author: C.K.Harvey
// Copyright (c) 2006-08 Christopher Harvey. All Rights Reserved. All rights reserved;
// Where: Mail Server
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

public class MailZaraIPAddress extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();

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
      out = res.getWriter();
      directoryUtils.setContentHeaders(res);

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
      String urlBit="";
      int x=0;
      if(url.charAt(x) == 'h' || url.charAt(x) == 'H')
        x += 7;
      while(url.charAt(x) != '\000' && url.charAt(x) != ',' && url.charAt(x) != '/')
        urlBit += url.charAt(x++);

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "MailZaraIPAddress", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8013, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    out.println(set(bytesOut));

    System.out.println("out 8013d");
    if(out != null) out.flush();
  }
 
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private String set(int[] bytesOut) throws Exception
  {
    String rtnStr = fetchData("/Zara/Exim/relay_from_hosts");
    
    bytesOut[0] += rtnStr.length();

    return rtnStr;
  } 
    
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private String fetchData(String fileName)
  {
    RandomAccessFile fh = null;
    int x, i;
    String str, ipAddr, location, value = "";

    try
    {
      if((fh = generalUtils.fileOpen(fileName)) == null) // just-in-case
        return "";

      fh.seek(0);

      try
      {
        boolean quit = false;
        while(! quit)
        {
          x = 0;
          str = fh.readLine();
          
          i = str.indexOf("#");
          if(i != -1)
            location = str.substring(i + 1);
          else
          {
            location = "";
            i = str.length();
          }
          
          ipAddr = str.substring(0, i);
          
          value += (ipAddr + "\001" + location + "\001");
        }
        fh.close();
      }
      catch(Exception ioErr)
      {
        fh.close();
      }
    }
    catch(Exception e)
    {
      return "8013d: " + e;
    }
    
    return value;
  }

}

