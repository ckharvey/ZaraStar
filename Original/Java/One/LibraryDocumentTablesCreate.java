// =======================================================================================================================================================================================================
// System: ZaraStar Library: Create Document Tables
// Module: LibraryDocumentTablesCreate.java
// Author: C.K.Harvey
// Copyright (c) 2006-09 Christopher Harvey. All Rights Reserved.
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

public class LibraryDocumentTablesCreate extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  LibraryUtils libraryUtils = new LibraryUtils();
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
      }
      
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "LibraryDocumentTablesCreate", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 403, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');
    
    long startTime = new java.util.Date().getTime();

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(serverUtils.isSysAdmin(con, stmt, rs, unm, dnm, sid, uty, localDefnsDir, defnsDir))
    {
      if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
      {
        messagePage.msgScreen(true, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "LibraryDocumentTablesCreate", imagesDir, localDefnsDir, defnsDir, bytesOut);
        serverUtils.etotalBytes(req, unm, dnm, 403, bytesOut[0], 0, "ACC:");
        if(con != null) con.close();
        if(out != null) out.flush();
        return;
      }

      libraryUtils.createTableDocuments(true, dnm, localDefnsDir, defnsDir);
      libraryUtils.createTablePermissions(true, dnm, localDefnsDir, defnsDir);
      libraryUtils.createTableProperties(true, dnm, localDefnsDir, defnsDir);
      libraryUtils.createTableDocmove(true, dnm, localDefnsDir, defnsDir);
     
      messagePage.msgScreen(true, out, req, 4, unm, sid, uty, men, den, dnm, bnm, "LibraryDocumentTablesCreate", imagesDir, localDefnsDir, defnsDir, bytesOut);
    }
    else // access denied
    {
      messagePage.msgScreen(true, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "LibraryDocumentTablesCreate", imagesDir, localDefnsDir, defnsDir, bytesOut);
    }
    
    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 403, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con != null) con.close();
    if(out != null) out.flush();
  }
  
}
