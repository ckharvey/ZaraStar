// =======================================================================================================================================================================================================
// System: ZaraStar LibraryEngine: upload file
// Module: LibraryEngineUploadFile.java
// Author: C.K.Harvey
// Copyright (c) 2002-08 Christopher Harvey. All Rights Reserved.
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
import java.sql.*;

public class LibraryEngineUploadFileWave extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
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

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="", p3="";

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
      p1  = req.getParameter("p1"); // Reference, or User
      p2  = req.getParameter("p2"); // fromWhere
      p3  = req.getParameter("p3"); // fromWhereDocCode

      if(p2 == null) p2 = "";
      if(p3 == null) p3 = "";

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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "LibraryEngineUploadFile", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 12002, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush(); 
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1, String p2, String p3, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String libraryDir    = directoryUtils.getUserDir('L', dnm, unm);
    String referenceDir  = directoryUtils.getReferenceLibraryDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreenW(out, req, 12, unm, sid, uty, men, den, dnm, bnm, "LibraryEngineUploadFile", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 12002, bytesOut[0], 0, "SID:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    String lib;
    if(p1.equals("R"))
      lib = referenceDir;
    else lib = libraryDir;

    scoutln(out, bytesOut, "12002\001Documents\001Document Upload\001javascript:getHTML('LibraryEngineUploadFileWave','')\001\001\001\001\003");

    set(con, stmt, rs, out, req, p2, p3, unm, sid, uty, men, den, dnm, bnm, lib, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 12002, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String fromWhere, String fromWhereDocCode, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                   String libraryDir, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    String cssDirectory = directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir);

    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + cssDirectory + "general.css\">");

    scoutln(out, bytesOut, "<form enctype=\"multipart/form-data\" action=\"" + directoryUtils.getScriptsDirectory() + "upload4libW.php\" method=\"post\">");

    libraryUtils.drawTitleW(con, stmt, rs, req, out, "File Upload", "12001", unm, uty, dnm, localDefnsDir, defnsDir, bytesOut);

    scoutln(out, bytesOut, "<table border=0 cellpadding=5 id=\"page\" width=100%>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    if(fromWhere.length() > 0)
    {
      scoutln(out, bytesOut, "<tr><td colspan=2><p>WARNING: This upload will <i>automatically</i> set the document to an externally-visible state.</td></tr>");
      scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
      scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    }

    scoutln(out, bytesOut, "<tr><td><p>File to Upload</td><td><p><input type=file name=\"userfile[]\" size=60 maxsize=100></td></tr>");

    int len = libraryDir.length();
    --len; // trailing '/'
    while(len > 0 && libraryDir.charAt(len) != '/') // just-in-case
      --len;

    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"unm\" value='" + unm + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"sid\" value='" + sid + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"uty\" value='" + uty + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"men\" value='" + men + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"den\" value='" + den + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"dnm\" value='" + dnm + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"bnm\" value='" + bnm + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"lenOfBitToStrip\" value='" + len + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"host\" value='" + serverUtils.serverToCall("LIBRARY", localDefnsDir) + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"fromWhere\" value='" + fromWhere + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"fromWhereDocCode\" value='" + fromWhereDocCode + "'>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td><p>Target Directory</td>");
    
    scoutln(out, bytesOut, "<td><p><select name=libDir>");
    buildDirs(out, libraryDir, len, bytesOut);
    scoutln(out, bytesOut, "</select></td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td><p><input type='submit' value='Upload the File'></td></tr>");
//    scoutln(out, bytesOut, "<tr><td><p><a href=\"javascript:update()\">Upload</a> the File</td></tr>");

    scoutln(out, bytesOut, "</table></form>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void buildDirs(PrintWriter out, String libraryDir, int libraryDirNameLen, int[] bytesOut) throws Exception
  {
    File path = new File(libraryDir);
    String fs[];
    fs = path.list();

    if(fs == null)
    {
      generalUtils.createDir(libraryDir);
      fs = path.list();
    }

    int len = fs.length;

    generalUtils.insertionSort(fs, len);

    for(int x=0;x<len;++x)
    {
      if(generalUtils.isDirectory(libraryDir + fs[x]))
      {
        scoutln(out, bytesOut, "<option value=\"" + generalUtils.deSanitise(libraryDir) + fs[x] + "\">" + libraryDir.substring(libraryDirNameLen) + fs[x]
                             + "\n");
        buildDirs(out, (libraryDir + fs[x] + "/"), libraryDirNameLen, bytesOut);
      }
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {
    out.println(str);
    bytesOut[0] += (str.length() + 2);
  }

}
