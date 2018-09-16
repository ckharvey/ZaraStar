// =======================================================================================================================================================================================================
// System: ZaraStar LibraryEngine: Create record for uploaded file
// Module: LibraryCreateRecordUploadWave.java
// Author: C.K.Harvey
// Copyright (c) 2006-09 Christopher Harvey. All Rights Reserved.
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

public class LibraryCreateRecordUploadWave extends HttpServlet
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

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="", p3="", p4="", p5="", p6="";

    try
    {
      directoryUtils.setContentHeaders(res);
      out = res.getWriter();

      unm = req.getParameter("unm");
      sid = req.getParameter("sid");
      uty = req.getParameter("uty");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");
      p1  = req.getParameter("p1"); // full filename of tmp file
      p2  = req.getParameter("p2"); // fileName
      p3  = req.getParameter("p3"); // target dir (full)
      p4  = req.getParameter("p4"); // target dir (stripped)
      p5  = req.getParameter("p5"); // fromWhere
      p6  = req.getParameter("p6"); // fromWhereDocCode

      if(p5 == null) p5 = "";
      if(p6 == null) p6 = "";

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, p3, p4, p5, p6, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "LibraryCreateRecordUpload", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 12011, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush(); 
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1, String p2, String p3, String p4, String p5, String p6, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');
    String referenceDir  = directoryUtils.getReferenceLibraryDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreenW(out, req, 12, unm, sid, uty, men, den, dnm, bnm, "LibraryCreateRecordUpload", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 12011, bytesOut[0], 0, "SID:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    int bytesIn = 0;

    if(p2 != null) // a filename has been specified
      bytesIn = analyze(con, stmt, out, req, p1, p2, p3, p4, p5, p6, unm, sid, uty, men, den, dnm, bnm, referenceDir, imagesDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 12011, bytesOut[0], bytesIn, (new java.util.Date().getTime() - startTime), p1);
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int analyze(Connection con, Statement stmt, PrintWriter out, HttpServletRequest req, String tmpFile, String fileName, String fullDirectory, String directory, String fromWhere, String fromWhereDocCode, String unm, String sid, String uty,
                      String men, String den, String dnm, String bnm, String referenceDir, String imagesDir, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    int x = fileName.length();
    --x;
    while(x > 0 && fileName.charAt(x) != '.')
     --x;

    String extn;
    if(x == 0)
      extn = "";
    else extn = (fileName.substring(++x)).toUpperCase();

    long fileSize;

    String internalExternalOrAnonymous;

    if(fromWhere.length() > 0)
      internalExternalOrAnonymous = "E";
    else internalExternalOrAnonymous = "I";

    if(fullDirectory.startsWith(referenceDir))
    {
      generalUtils.copyFileToFile(tmpFile, fullDirectory + "/" + fileName);

      generalUtils.fileDelete(tmpFile);

      File file = new File(fullDirectory + "/" + fileName);
      fileSize = file.length();

      String docNum = libraryUtils.addToDocumentsAndPermissions("___REFERENCE___", fileName, extn, "I", generalUtils.todaySQLFormat(localDefnsDir, defnsDir), directory, "", internalExternalOrAnonymous, "1970:01:01", "", fileSize, dnm, localDefnsDir, defnsDir);
      if(docNum.length() > 0)
      {
        if(fromWhere.length() > 0) // call from a document
          updateDocument(con, stmt, fromWhereDocCode, docNum, fromWhere);

        messagePage.msgScreenW(out, req, 0, unm, sid, uty, men, den, dnm, bnm, "12002", "", "", "Uploaded as Library Document: " + docNum, imagesDir, localDefnsDir, defnsDir, bytesOut);
      }
      else messagePage.msgScreenW(out, req, 39, unm, sid, uty, men, den, dnm, bnm, "LibraryCreateRecordUpload", imagesDir, localDefnsDir, defnsDir, bytesOut);
    }
    else // a user's directory
    {
      generalUtils.copyFileToFile(tmpFile, fullDirectory + "/" + fileName);

      generalUtils.fileDelete(tmpFile);

      File file = new File(fullDirectory + "/" + fileName);
      fileSize = file.length();

      String docNum = libraryUtils.addToDocumentsAndPermissions(unm, fileName, extn, "I", generalUtils.todaySQLFormat(localDefnsDir, defnsDir), directory, "", internalExternalOrAnonymous, "1970:01:01", "", fileSize, dnm, localDefnsDir, defnsDir);

      if(docNum.length() > 0)
      {
        if(fromWhere.length() > 0) // call from a document
          updateDocument(con, stmt, fromWhereDocCode, docNum, fromWhere);

        libraryUtils.addToDocmove(docNum, "U", (int)fileSize, unm, dnm);

        messagePage.msgScreenW(out, req, 0, unm, sid, uty, men, den, dnm, bnm, "12002", "", "", "Uploaded as Library Document: " + docNum, imagesDir, localDefnsDir, defnsDir, bytesOut);
      }
      else messagePage.msgScreenW(out, req, 39, unm, sid, uty, men, den, dnm, bnm, "LibraryCreateRecordUpload", imagesDir, localDefnsDir, defnsDir, bytesOut);
    }

    return (int)fileSize;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void updateDocument(Connection con, Statement stmt, String code, String libraryDocCode, String fileName) throws Exception
  {
    try
    {
      stmt = con.createStatement();

      String q = "INSERT INTO " + fileName + "a ( Code, LibraryDocCode ) VALUES ('" + code + "','" + libraryDocCode + "')";

      stmt.executeUpdate(q);
    }
    catch(Exception e) { } // already exists

    if(stmt != null) stmt.close();
  }

}
