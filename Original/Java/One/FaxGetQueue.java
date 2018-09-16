//====================================================================================================================================================================================================================================================
// System: ZaraStar: Fax: get queue
// Module: FaxGetQueue.java
// Author: C.K.Harvey
// Copyright (c) 2006-07 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
//
//====================================================================================================================================================================================================================================================

package org.zarastar.zarastar;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.sql.*;

public class FaxGetQueue extends HttpServlet
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "FaxGetQueue", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 11050, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, 
                    String bnm, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;
    
    out.println(set(bytesOut));

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 11050, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con  != null) con.close();
    if(out != null) out.flush(); 
  }
 
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private String set(int[] bytesOut) throws Exception
  {
    String rtnStr = fetchJobs("s", "/opt/csw/var/spool/hylafax/sendq/");
    
    rtnStr += fetchJobs("d", "/opt/csw/var/spool/hylafax/doneq/");

    bytesOut[0] += rtnStr.length();

    return rtnStr;
  } 
    
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private String fetchJobs(String which, String dir)
  {
    File path = new File(dir);
    String fs[] = new String[0];
    fs = path.list();
    
    String res = "";
    
    for(int x=0;x<fs.length;++x)
    {
      if(! generalUtils.isDirectory(dir + fs[x]))
      {
        if(fs[x].startsWith("q"))
        {
          res += (which + fs[x].substring(1) + "\001" + getStatusFromFile(dir, fs[x]) + "\001");
        }  
      }
    }

    return res;
  }
    
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private String getStatusFromFile(String dir, String fileName)
  {
    RandomAccessFile fh=null;
    short x;
    String str, buf, value;

    try
    {
      if((fh = generalUtils.fileOpenD(fileName, dir)) == null) // just-in-case
      {
        return "";
      }

      fh.seek(0);

      value = "";
      try
      {
        boolean quit = false;
        while(! quit)
        {
          x = 0;
          buf = "";
          str = fh.readLine();
          while(x < str.length() && str.charAt(x) != ':')
          {
            if(str.charAt(x) != (char)13 && str.charAt(x) != (char)10 && str.charAt(x) != (char)26)
               buf += str.charAt(x);
            ++x;
          }

          if(buf.equalsIgnoreCase("status"))
          {
            ++x;

            while(x < str.length() && str.charAt(x) != (char)13 && str.charAt(x) != (char)10 && str.charAt(x) != (char)26)
              value += str.charAt(x++);
            
            quit = true;
          }
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
      return "11050a: " + e;
    }
    
    return generalUtils.stripNewLines(value);
  }

}
