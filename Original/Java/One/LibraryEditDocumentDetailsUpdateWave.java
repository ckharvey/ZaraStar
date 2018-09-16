// =======================================================================================================================================================================================================
// System: ZaraStar Library: Update document details edit
// Module: LibraryEditDocumentDetailsUpdate.java
// Author: C.K.Harvey
// Copyright (c) 2006-07 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;
import java.io.*;
import java.util.Enumeration;

public class LibraryEditDocumentDetailsUpdateWave extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  LibraryUtils libraryUtils = new LibraryUtils();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", userName="", docName="", directory="", projectCode="", internalOrExternal="", status="";

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
        if(name.equals("p1")) // docCode
          p1 = value[0];
        else
        if(name.equals("userName"))
          userName = value[0];
        else
        if(name.equals("docName"))
          docName = value[0];
        else
        if(name.equals("directory"))
          directory = value[0];
        else
        if(name.equals("projectCode"))
          projectCode = value[0];
        else
        if(name.equals("internalOrExternal"))
          internalOrExternal = value[0];
        else
        if(name.equals("status"))
          status = value[0];
      }

      doIt(out, req, unm, sid, uty, dnm, men, den, bnm, p1, userName, docName, directory, projectCode, internalOrExternal, status, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "LibraryEditDocumentDetailsUpdate", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 12001, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush(); 
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1, String userName, String docName, String directory, String projectCode,
                    String internalOrExternal, String status, int[] bytesOut) throws Exception
  {
    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    long startTime = new java.util.Date().getTime();

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreenW(out, req, 12, unm, sid, uty, men, den, dnm, bnm, "LibraryEditDocumentDetailsUpdate", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 12001, bytesOut[0], 0, "SID:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    scoutln(out, bytesOut, "12001a\001Documents\001Document Save\001javascript:getHTML('LibraryEditDocumentDetailsUpdateWave','')\001\001\001\001\003");

    set(unm, dnm, p1, userName, docName, directory, projectCode, internalOrExternal, status, localDefnsDir, defnsDir);

    messagePage.msgScreenW(out, req, 27, unm, sid, uty, men, den, dnm, bnm, "LibraryCreateRecordUpload", imagesDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 12001, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(String unm, String dnm, String docCode, String userName, String docName, String directory, String projectCode, String internalOrExternal, String status, String localDefnsDir, String defnsDir) throws Exception
  {
    String[] existingUserName           = new String[1];
    String[] existingDocName            = new String[1];
    String[] existingDocType            = new String[1];
    String[] existingInOrOut            = new String[1];
    String[] existingLastDateIn         = new String[1];
    String[] existingDirectory          = new String[1];
    String[] existingProjectCode        = new String[1];
    String[] existingInternalOrExternal = new String[1];
    String[] existingArchivedDate       = new String[1];
    String[] existingArchivedBy         = new String[1];
    String[] existingFileSize           = new String[1];

    libraryUtils.getDocumentDetails(docCode, existingUserName, existingDocName, existingDocType, existingInOrOut, existingLastDateIn, existingDirectory, existingProjectCode, existingInternalOrExternal, existingArchivedDate, existingArchivedBy,
                             existingFileSize, dnm, localDefnsDir, defnsDir);

    boolean toBeUpdated=false, toBeMoved=false, toBeArchived=false;
    if(! userName.equals(existingUserName[0]))
    {
      existingUserName[0] = userName;
      toBeUpdated = true;
    }

    if(! docName.equals(existingDocName[0]))
    {
      existingDocName[0] = docName;
      int x = docName.length();
        --x;
      while(x > 0 && docName.charAt(x) != '.')
        --x;

      if(x == 0)
        existingDocType[0] = "";
      else existingDocType[0] = (docName.substring(++x)).toUpperCase();

      toBeMoved = toBeUpdated = true;
    }

    if(! internalOrExternal.equals(existingInternalOrExternal[0]))
    {
      existingInternalOrExternal[0] = internalOrExternal;
      toBeUpdated = true;
    }

    if(! projectCode.equals(existingProjectCode[0]))
    {
      existingProjectCode[0] = projectCode;
      toBeUpdated = true;
    }

    if(! status.equals(existingInOrOut[0]))
    {
      if(status.equals("I"))
      {
        existingInOrOut[0] = "I";
        toBeUpdated = true;
      }
      else
      if(status.equals("O"))
      {
        existingInOrOut[0] = "O";
        toBeUpdated = true;
      }
      else // if(status.equals("A"))
      {
        existingArchivedDate[0] = generalUtils.todaySQLFormat(localDefnsDir, defnsDir);
        existingArchivedBy[0] = unm;
        toBeArchived = toBeUpdated = true;
      }
    }

    if(toBeUpdated)
    {
      libraryUtils.updateDocuments(docCode, existingUserName[0], existingDocName[0], existingDocType[0], existingInOrOut[0], existingLastDateIn[0], existingDirectory[0], existingProjectCode[0], existingInternalOrExternal[0], "1970-01-01", "",
                            existingFileSize[0], dnm, localDefnsDir, defnsDir);
    }

    if(toBeMoved)
    {

    }

    if(toBeArchived)
    {

    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {
    out.println(str);
    bytesOut[0] += (str.length() + 2);
  }

}
