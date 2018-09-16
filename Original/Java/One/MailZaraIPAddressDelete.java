// =======================================================================================================================================================================================================
// System: ZaraStar Mail: ipaddr - delete
// Module: MailZaraIPAddressDelete.java
// Author: C.K.Harvey
// Copyright (c) 2006-08 Christopher Harvey. All Rights Reserved.
// Where: On mail server
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

public class MailZaraIPAddressDelete extends HttpServlet
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
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", ipAddr="";

    try
    {
      directoryUtils.setContentHeaders(res);
      res.setHeader("Header", "HTTP/1.0 200 OK");
      res.setStatus(200);

      unm = req.getParameter("unm");
      sid = req.getParameter("sid");
      uty = req.getParameter("uty");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");
      ipAddr = req.getParameter("p1");

      doIt(req, res, unm, sid, uty, dnm, ipAddr, bytesOut);
    }    
    catch(Exception e)
    {
      System.out.println(("Unexpected System Error: 8013c: " + e));
      res.getWriter().write("Unexpected System Error: 8013c");
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(HttpServletRequest req, HttpServletResponse res, String unm, String sid, String uty, String dnm, String ipAddr, int[] bytesOut)
                    throws Exception
  {
    String rtn="Unexpected System Error: 8013c";
    
    if(deleteEntry(ipAddr, "/Zara/Exim/relay_from_hosts", "/Zara/Exim/relay_from_hosts.tmp"))
      generalUtils.copyFileToFile("/Zara/Exim/relay_from_hosts.tmp", "/Zara/Exim/relay_from_hosts");
    
    res.setContentType("text/xml");
    res.setHeader("Cache-Control", "no-cache");
    
    String s = "<msg><res>" + rtn + "</res></msg>";
    res.getWriter().write(s);
    
    serverUtils.totalBytes(req, unm, dnm, 8013, bytesOut[0], 0, ipAddr);
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean deleteEntry(String reqdIPAddr, String fileName, String tmpFileName)
  {
    RandomAccessFile fh = null, fho = null;
    int x;
    String str;

    try
    {
      if((fh = generalUtils.fileOpen(fileName)) == null) // just-in-case
        return false;

      fh.seek(0);

      if((fho = generalUtils.create(tmpFileName)) == null) // just-in-case
        return false;

      try
      {
        boolean quit = false;
        while(! quit)
        {
          x = 0;
          str = fh.readLine();
          
          if(! str.startsWith(reqdIPAddr))
            fho.writeBytes(str + "\n");
        }

        fh.close();
        fho.close();
      }
      catch(Exception ioErr)
      {
        fh.close();
        fho.close();
      }
    }
    catch(Exception e)
    {
      System.out.println("8013c: " + e);
      try
      {
        if(fh != null) fh.close();
        if(fho != null) fho.close();
      }
      catch(Exception e2) { }
  
      return false;
    }
    
    return true;
  }

}
