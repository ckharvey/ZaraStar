// =======================================================================================================================================================================================================
// System: ZaraStar Utils: Get clipboard contents
// Module: ClipboardContents.java
// Author: C.K.Harvey
// Copyright (c) 2006 Christopher Harvey. All Rights Reserved.
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
import java.util.Enumeration;

public class ClipboardContents extends HttpServlet
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

    String unm="", sid="", uty="", dnm="";

    try
    {
      directoryUtils.setContentHeaders(res);
      res.setHeader("Header", "HTTP/1.0 200 OK");
      res.setStatus(200);

      Enumeration en = req.getParameterNames();
      while(en.hasMoreElements())
      {
        String name = (String)en.nextElement();
        String[] value = req.getParameterValues(name);
        if(name.equals("unm"))
          unm = value[0];
        else
        if(name.equals("sid"))
          sid = value[0];
        else
        if(name.equals("uty"))
          uty = value[0];
        else
        if(name.equals("dnm"))
          dnm = value[0];
      }

      doIt(req, res, unm, sid, uty, dnm, bytesOut);
    }
    catch(Exception e)
    {
      System.out.println(("Unexpected System Error: 8888a: " + e));
      res.getWriter().write("Unexpected System Error: 8888a");
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(HttpServletRequest req, HttpServletResponse res, String unm, String sid, String uty, String dnm, int[] bytesOut)
                    throws Exception
  {
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    String str = "";

    String rtn="Unexpected System Error: 8888";

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      rtn = "Access Denied - Duplicate SignOn or Session Timeout";
    }
    else
    {
      String workingDir = directoryUtils.getUserDir('W', dnm, unm);
      String pathName = workingDir + "clipboard";
      RandomAccessFile fh;
      if(generalUtils.fileExists(pathName))
      {
        fh = generalUtils.fileOpen(pathName);

        try
        {
          long high = fh.length();

          while(true)
            str += (char)fh.readByte();
        }
        catch(Exception e) { }

        fh.close();
        generalUtils.fileDelete(pathName);

        rtn = ".";
      }
      else rtn = ":";
    }

    res.setContentType("text/xml");
    res.setHeader("Cache-Control", "no-cache");

    String s = "<msg><res>" + rtn + "</res><val><![CDATA[" + str + "]]></val></msg>";
    res.getWriter().write(s);

    serverUtils.totalBytes(req, unm, dnm, 8888, bytesOut[0], 0, "");
  }

}
