// =======================================================================================================================================================================================================
// System: ZaraStar Library: Download a file (casual access)
// Module: LibraryDownloadCasualWave.java
// Author: C.K.Harvey
// Copyright (c) 2006-08 Christopher Harvey. All Rights Reserved.
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

public class LibraryDownloadCasualWave extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  LibraryUtils libraryUtils = new LibraryUtils();
  AdminControlUtils adminControlUtils = new AdminControlUtils();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="";

    try
    {
      directoryUtils.setContentHeaders(res);
      out = res.getWriter();

      unm = req.getParameter("unm");
      sid = req.getParameter("sid");
      uty = req.getParameter("uty");
      dnm = req.getParameter("dnm");
      men = req.getParameter("men");
      den = req.getParameter("den");
      bnm = req.getParameter("bnm");
      p1  = req.getParameter("p1"); // docCode

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "LibraryDownloaCasual", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 12013, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush(); 
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    boolean canAccess = false;

    if((uty.equals("A") && adminControlUtils.notDisabled(con, stmt, rs, 825)) || (uty.equals("R") && adminControlUtils.notDisabled(con, stmt, rs, 925)) || uty.equals("I"))
      canAccess = authenticationUtils.verifyAccessForLibrary(con, stmt, rs, req, p1, unm, uty, dnm);

    if(! canAccess)
    {
      messagePage.msgScreenW(out, req, 13, unm, sid, uty, men, den, dnm, bnm, "LibraryDownloaCasual", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 12013, bytesOut[0], 0, "ACC:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreenW(out, req, 12, unm, sid, uty, men, den, dnm, bnm, "LibraryDownloaCasual", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 12013, bytesOut[0], 0, "SID:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    scoutln(out, bytesOut, "12013\001Documents\001Document Download\001javascript:getHTML('LibraryDownloaCasualw','&p1=" + p1 + "')\001\001Y\001\001\003");

    set(con, stmt, rs, out, req, p1, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 12013, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String docCode, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir,
                   int[] bytesOut) throws Exception
  {
    String cssDirectory = directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir);

    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + cssDirectory + "general.css\">");

    libraryUtils.drawTitleW(con, stmt, rs, req, out, "File Download", "12013", unm, uty, dnm, localDefnsDir, defnsDir, bytesOut);

    scoutln(out, bytesOut, "<script javascript=\"JavaScript\">");
    scoutln(out, bytesOut, "function go(){postForm('LibraryDownloadFileExecuteWave','12013w');}");
    scoutln(out, bytesOut, "</script>");

    scoutln(out, bytesOut, "<form id=\"12013w\" enctype=\"application/x-www-form-urlencoded\" method=post>");

    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"unm\" value='" + unm + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"sid\" value='" + sid + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"uty\" value='" + uty + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"men\" value='" + men + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"den\" value='" + den + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"dnm\" value='" + dnm + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"bnm\" value='" + bnm + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"isForCheckout\" value='N'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"docCode\" value='" + docCode + "'>");

    String[] userName           = new String[1];
    String[] docName            = new String[1];
    String[] docType            = new String[1];
    String[] inOrOut            = new String[1];
    String[] lastDateIn         = new String[1];
    String[] directory          = new String[1];
    String[] projectCode        = new String[1];
    String[] internalOrExternal = new String[1];
    String[] archivedDate       = new String[1];
    String[] archivedBy         = new String[1];
    String[] fileSize           = new String[1];

    libraryUtils.getDocumentDetails(docCode, userName, docName, docType, inOrOut, lastDateIn, directory, projectCode, internalOrExternal, archivedDate, archivedBy, fileSize, dnm, localDefnsDir, defnsDir);

    String libraryDir;
    if(userName[0].equals("___REFERENCE___"))
      libraryDir = directoryUtils.getReferenceLibraryDir(dnm);
    else libraryDir = directoryUtils.getUserDir('L', dnm, userName[0]);

    scoutln(out, bytesOut, "<table border=0 cellpadding=5 id=\"directory\" width=100%>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td><p>Download</td><td width=90%><b>" + docName[0] + "</b></td></tr>");
    scoutln(out, bytesOut, "<tr><td><p>Of type</td><td><b>" + docType[0] + "</b></td></tr>");
    scoutln(out, bytesOut, "<tr><td><p>And size</td><td><b>" + fileSize[0] + "</b></td></tr>");

    bytesOut[0] += generalUtils.intFromStr(fileSize[0]);

    scoutln(out, bytesOut, "<tr><td><input type=hidden name=\"libDir\" value='" + libraryDir + directory[0].substring(1) + "/'></td></tr>");

    scoutln(out, bytesOut, "<tr><td><input type=hidden name=fileName value='" + generalUtils.sanitise(docName[0]) + "'></td></td>");
    scoutln(out, bytesOut, "<tr><td><p><a href=\"javascript:go()\">Download</a></td></tr>");

    scoutln(out, bytesOut, "</table></form>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {
    out.println(str);
    bytesOut[0] += str.length() + 2;
  }

}
