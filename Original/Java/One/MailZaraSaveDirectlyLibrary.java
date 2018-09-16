// =======================================================================================================================================================================================================
// System: ZaraStar Mail: Save Mail Directly to Library
// Module: MailZaraSaveDirectlyLibrary.java
// Author: C.K.Harvey
// Copyright (c) 2001-09 Christopher Harvey. All Rights Reserved.
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

public class MailZaraSaveDirectlyLibrary extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  MailUtils mailUtils = new MailUtils();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", dnm="", p1="", p2="", p3="", p4="", p5="", p6="";

    try
    {
      directoryUtils.setContentHeaders(res);
      out = res.getWriter();

      int len = req.getContentLength();
      ServletInputStream in  = req.getInputStream();
      byte[] b = new byte[len];

      int red = in.readLine(b, 0, len);
      String name;
      int x, upto=0;
      while(red != -1 && upto < len)
      {
        name="";
        x=0;
        while(x < 6)
          name += (char)b[x++];

        if(name.equals("___un="))
        {
          --red;
          x = 6;
          while(x < red)
            unm += (char)b[x++];
        }
        else
        if(name.equals("___dn="))
        {
          --red;
          x = 6;
          while(x < red)
            dnm += (char)b[x++];
        }
        else
        if(name.equals("___p1=")) // sentOrReceived
        {
          --red;
          x = 6;
          while(x < red)
            p1 += (char)b[x++];
        }
        else
        if(name.equals("___p2=")) // addrFrom
        {
          --red;
          x = 6;
          while(x < red)
            p2 += (char)b[x++];
        }
        else
        if(name.equals("___p3=")) // addrTo
        {
          --red;
          x = 6;
          while(x < red)
            p3 += (char)b[x++];
        }
        else
        if(name.equals("___p4=")) // subject
        {
          --red;
          x = 6;
          while(x < red)
            p4 += (char)b[x++];
        }
        else
        if(name.equals("___p5=")) // origDate
        {
          --red;
          x = 6;
          while(x < red)
            p5 += (char)b[x++];
        }
        else
        if(name.equals("___p6=")) // text
        {
          --red;
          x = 6;
          while(x < red)
            p6 += (char)b[x++];
        }

        red = in.readLine(b, 0, len);
        upto += red;
      }

      doIt(out, req, unm, dnm, p1, p2, p3, p4, p5, p6, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, "", dnm, "", urlBit, "", "", "", "MailZaraSaveDirectlyLibrary", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8010, bytesOut[0], 0, "ERR:" + p2);
      if(out != null) out.flush();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String dnm, String sentOrReceived, String addrFrom, String addrTo, String subject, String origDate, String text, int[] bytesOut) throws Exception
  {
    String defnsDir = directoryUtils.getSupportDirs('D');

    if(origDate.length() == 0)
      origDate = generalUtils.todaySQLFormat("", defnsDir);

    mailUtils.addToMail("", sentOrReceived, addrFrom, addrTo, subject, "", text, "", "", "", "", "", origDate, "", dnm, "", defnsDir);

    serverUtils.totalBytes(req, unm, dnm, 8010, bytesOut[0], 0, addrFrom);
    if(out != null) out.flush();
  }

}
