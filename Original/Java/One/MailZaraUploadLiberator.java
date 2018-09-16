// =======================================================================================================================================================================================================
// System: ZaraStar MailEngine: Upload from Liberator
// Module: MailZaraUploadLiberator.java
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
import java.sql.*;

public class MailZaraUploadLiberator extends HttpServlet
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

    String unm="", uty="", dnm="", p1="", p2="", sid="";

    try
    {
      res.setContentType("text/html");
      out = res.getWriter();

      unm = req.getParameter("unm");
      uty = req.getParameter("uty");
      dnm = req.getParameter("dnm");
      p1  = req.getParameter("p1");  // destFileName
      p2  = req.getParameter("p2");  // len of directory name to ignore

      doIt(out, req, unm, uty, sid, dnm, p1, p2, bytesOut);
    }
    catch(Exception e)
    {
      serverUtils.etotalBytes(req, unm, dnm, 8030, bytesOut[0], 0, "ERR:" + p1);
      out.println("ERR");
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String uty, String sid, String dnm, String p1, String p2, int[] bytesOut) throws Exception
  {
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String mailUploadDir    = directoryUtils.getUserDir('M', dnm, unm) + "/Upload/";

    long startTime = new java.util.Date().getTime();

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! serverUtils.isSysAdmin(con, stmt, rs, unm, dnm, sid, uty, localDefnsDir, defnsDir))
    {
      out.println("ERR");
      serverUtils.etotalBytes(req, unm, dnm, 8030, bytesOut[0], 0, "ACC:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    int x = p1.length();
    --x;
    while(x > 0 && p1.charAt(x) != '/') // just-in-case
      --x;

    if(x > 0)
      ++x;
    
    p1 = p1.substring(x);

    if(! generalUtils.fileExists(mailUploadDir))
      generalUtils.createDir(mailUploadDir, true);
    
    if(update(req, mailUploadDir+p1))
      out.println("OK");
    else out.println("ERR");

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 8030, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con  != null) con.close();
    if(out != null) out.flush();
  }

  // ------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean update(HttpServletRequest req, String mailUploadDirPlusFileName)
  {
    try
    {
      ServletInputStream in = req.getInputStream();
      if(in == null)
        throw new IOException();

      DataInputStream dis = new DataInputStream(in);
      if(dis == null)
      {
        in.close();
        throw new IOException();
      }

      RandomAccessFile fd;
      if((fd = generalUtils.create(mailUploadDirPlusFileName)) == null)
      {
        in.close();
        dis.close();
        throw new IOException();
      }

      byte[] b = new byte[1021];
      int red = dis.read(b, 0, 1020);

      int x;
      long total = red;
      while(red > 0)
      {
        for(x=0;x<red;++x)
          fd.writeByte(b[x]);

        red = dis.read(b, 0, 1020);
        total += red;
      }

      for(x=0;x<red;++x)
        fd.writeByte(b[x]);

      generalUtils.fileClose(fd);

      dis.close();

      in.close();

      System.out.println(" ...transferred");
    }
    catch(Exception e) { System.out.println("Exception " + e); }

    return true;
  }

}
