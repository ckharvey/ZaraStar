// =======================================================================================================================================================================================================
// System: ZaraStar Support: Help hints
// Module: SupportHelpHints.java
// Author: C.K.Harvey
// Copyright (c) 2006-07 Christopher Harvey. All Rights Reserved.
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
import java.net.*;
import java.util.Enumeration;

public class SupportHelpHints extends HttpServlet
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

    String unm="", sid="", uty="", dnm="", bnm="", pageCode="";

    try
    {
      directoryUtils.setContentHeaders(res);
      res.setHeader("Header", "HTTP/1.0 200 OK");
      res.setStatus(200);

      Enumeration en = req.getParameterNames();
      while(en.hasMoreElements())
      {
        String elementName = (String)en.nextElement();
        String[] value = req.getParameterValues(elementName);
        if(elementName.equals("unm"))
          unm = value[0];
        else
        if(elementName.equals("sid"))
          sid = value[0];
        else
        if(elementName.equals("uty"))
          uty = value[0];
        else
        if(elementName.equals("dnm"))
          dnm = value[0];
        else
        if(elementName.equals("bnm"))
          bnm = value[0];
        else
        if(elementName.equals("p1"))
          pageCode = value[0];
      }

      doIt(req, res, unm, sid, uty, dnm, bnm, pageCode, bytesOut);
    }
    catch(Exception e)
    {
      System.out.println(("Unexpected System Error: 9009: " + e));
      res.getWriter().write("Unexpected System Error: 9009");
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(HttpServletRequest req, HttpServletResponse res, String unm, String sid, String uty, String dnm, String bnm,
                    String pageCode, int[] bytesOut) throws Exception
  {
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    String hint="";

    String rtn="Unexpected System Error: 9009";

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      rtn = "Access Denied - Duplicate SignOn or Session Timeout";
    }
    else
    {
      hint = fetch(pageCode, bnm, localDefnsDir, bytesOut);
    }

    res.setContentType("text/xml");
    res.setHeader("Cache-Control", "no-cache");

    if(hint == null || hint.length() == 0)
      hint = ".";
    else hint = generalUtils.sanitiseForXML(hint);

    String s = "<msg><res>.</res><hint>" + hint + "</hint></msg>";
    res.getWriter().write(s);

    serverUtils.totalBytes(req, unm, dnm, 9009, bytesOut[0], 0, pageCode);
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private String fetch(String p1, String bnm, String localDefnsDir, int[] bytesOut) throws Exception
  {
    // strip any leading '_' and trailing 'a' (e.g.,)
    int x=0, len=p1.length();
    String page="";
    while(x < len)
    {
      if(p1.charAt(x) >= '0' && p1.charAt(x) <= '9')
        page += p1.charAt(x);
      ++x;
    }

    String res;

    try
    {
      res = searchWiki(page, bnm, localDefnsDir, bytesOut);
    }
    catch(Exception e)
    {
      System.out.println("9009 " + e);
      res = "Not Found";
    }

    return res;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private String searchWiki(String p1, String bnm, String localDefnsDir, int[] bytesOut) throws Exception
  {
    URL url = new URL("http://" + serverUtils.serverToCall("ZC", localDefnsDir) + "/central/servlet/_6680?unm=&sid=&uty=A&men=www.zaracloud.com&den=Zaracloud&dnm=Zaracloud&p1=" + p1 + "&bnm=" + bnm);

    URLConnection uc = url.openConnection();
    uc.setDoInput(true);
    uc.setUseCaches(false);
    uc.setDefaultUseCaches(false);

    uc.setRequestProperty("Content-Type", "application/octet-stream");

    BufferedReader di = new BufferedReader(new InputStreamReader(uc.getInputStream()));

    String rtn="", s = di.readLine();
    bytesOut[0] += s.length();
    int x, len;
    while(s != null)
    {
      len = s.length();
      x = 0;
      while(x<len)
      {
        if(s.charAt(x) == '\n')
          rtn += '`';
        else rtn += s.charAt(x);
        ++x;
      }

      bytesOut[0] += s.length();
      s = di.readLine();
    }

    di.close();

    return rtn;
  }

}
