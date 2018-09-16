// =======================================================================================================================================================================================================
// System: ZaraStar Text: Move uploaded file
// Module: TextMoveUploaded.java
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
import java.util.*;
import java.io.*; 
import java.sql.*;

public class TextMoveUploaded extends HttpServlet implements SingleThreadModel
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

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="", p3="";

    try
    {
      directoryUtils.setContentHeaders(res);
      out = res.getWriter();

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
        if(name.equals("men"))
          men = value[0];
        else
        if(name.equals("den"))
          den = value[0];
        else
        if(name.equals("dnm"))
          dnm = value[0];
        else
        if(name.equals("bnm"))
          bnm = value[0];
        else
        if(name.equals("p1")) // full filename of tmp file
          p1 = value[0];
        else
        if(name.equals("p2")) // filename
          p2 = value[0];
        else
        if(name.equals("p3")) // target dir (full)
          p3 = value[0];
      }

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, p3, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "TextMoveUploaded", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6102, bytesOut[0], 0, "ERR:" + p2);
      if(out != null) out.flush();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men,
                      String den, String dnm, String bnm, String p1, String p2, String p3, int[] bytesOut) throws Exception
  {
    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    long startTime = new java.util.Date().getTime();

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 6102, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "TextMoveUploaded", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6102, bytesOut[0], 0, "ACC:" + p2);
    if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }
    
    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "TextMoveUploaded", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6102, bytesOut[0], 0, "SID:" + p2);
    if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    int bytesIn = 0;
    
    if(p2 != null) // a filename has been specified
    {   
      bytesIn = analyze(out, req, p1, p2, p3, unm, sid, uty, men, den, dnm, bnm, imagesDir, localDefnsDir, defnsDir, bytesOut);
    }
    
    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 6102, bytesOut[0], bytesIn, (new java.util.Date().getTime() - startTime), p2);
    if(con  != null) con.close();
    if(out != null) out.flush();
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private int analyze(PrintWriter out, HttpServletRequest req, String tmpFile, String fileName, String fullDirectory, String unm,
                        String sid, String uty, String men, String den, String dnm, String bnm, String imagesDir,
                        String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {   
    generalUtils.copyFileToFile(tmpFile, fullDirectory + "/" + fileName);
    generalUtils.fileDelete(tmpFile);
     
    File file = new File(fullDirectory + "/" + fileName);
    long fileSize = file.length(); 
    
    messagePage.msgScreen(false, out, req, 4, unm, sid, uty, men, den, dnm, bnm, "TextMoveUploaded", imagesDir, localDefnsDir, defnsDir, bytesOut);
   
    return (int)fileSize;
  }
 
}
