// =======================================================================================================================================================================================================
// System: ZaraStar Library: Library list directory
// Module: LibraryListDirectory.java
// Author: C.K.Harvey
// Copyright (c) 2002-09 Christopher Harvey. All Rights Reserved.
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

public class LibraryListDirectoryWave extends HttpServlet
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

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="";

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
      p1  = req.getParameter("p1");
      p2  = req.getParameter("p2");

      if(p1 == null) p1="";
      if(p2 == null) p2=unm;

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "LibraryListDirectory", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 12000, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush(); 
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1, String p2, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreenW(out, req, 12, unm, sid, uty, men, den, dnm, bnm, "LibraryListDirectory", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 12000, bytesOut[0], 0, "SID:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    boolean canDownload;
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 12003, unm, uty, dnm, localDefnsDir, defnsDir))
      canDownload = true;
    else canDownload = false;

    boolean canCheckout;
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 12004, unm, uty, dnm, localDefnsDir, defnsDir))
      canCheckout = true;
    else canCheckout = false;

    boolean canCheckin;
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 12005, unm, uty, dnm, localDefnsDir, defnsDir))
      canCheckin = true;
    else canCheckin = false;

    boolean canEditDetails;
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 12001, unm, uty, dnm, localDefnsDir, defnsDir))
      canEditDetails = true;
    else canEditDetails = false;

    scoutln(out, bytesOut, "12000\001Documents\001Documents Main\001javascript:getHTML('LibraryListDirectoryWave','&p1=" + p1 + "&p2=" + p2 + "')\001\001Y\001\001\003");

    set(con, stmt, rs, out, req, p1, p2, canDownload, canCheckout, canCheckin, canEditDetails, unm, sid, uty, men, den, dnm, bnm, imagesDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 12000, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String directory, String userName, boolean canDownload, boolean canCheckout, boolean canCheckin, boolean canEditDetails,
                   String unm, String sid, String uty, String men, String den, String dnm, String bnm, String imagesDir, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    String cssDirectory = directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir);

    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + cssDirectory + "general.css\">");

    scoutln(out, bytesOut, "<script javascript=\"JavaScript\">");

    if(canEditDetails)
    {
      scoutln(out, bytesOut, "function details(docCode){getHTML('LibraryEditDocumentDetailsWave','&p1='+docCode);}");

      scoutln(out, bytesOut, "function manage(docCode){getHTML('LibraryManageDocumentw','&p1='+docCode);}");
    }

    if(canDownload)
    {
      scoutln(out, bytesOut, "function read(docCode){getHTML('LibraryDownloadFileWave','&p1='+docCode);}");
    }

    if(canCheckout)
    {
      scoutln(out, bytesOut, "function checkout(docCode){getHTML('LibraryCheckoutFileWave','&p1='+docCode);}");
    }

    if(canCheckin)
    {
      scoutln(out, bytesOut, "function checkin(docCode){getHTML('LibraryFileCheckInw','&p1='+docCode);}");
    }

    scoutln(out, bytesOut, "</script>");

    libraryUtils.drawTitleW(con, stmt, rs, req, out, "Document List", "12000", unm, uty, dnm, localDefnsDir, defnsDir, bytesOut);

    if(directory.length() > 0)
    {
      scoutln(out, bytesOut, "<table id='page' cellspacing=2 cellpadding=0 width=100%>");

      scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

      if(userName.equals("___REFERENCE___"))
        scoutln(out, bytesOut, "<tr><td nowrap colspan=11><p>Reference library directory: <b>" + directory + "</b></td></tr>");
      else scoutln(out, bytesOut, "<tr><td nowrap colspan=11><p>For User: <b>" + userName + "</b>, and directory: <b>" + directory + "</b></td></tr>");

      scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

      scoutln(out, bytesOut, "<tr id=\"pageColumn\">");
      if(canDownload)
        scoutln(out, bytesOut, "<td align=center><p><b>Read</td>");
      scoutln(out, bytesOut, "<td align=center><p><b>In/Out</td>");
      if(canEditDetails)
      {
        scoutln(out, bytesOut, "<td align=center><p><b>Details</td>");
        scoutln(out, bytesOut, "<td align=center><p><b>Manage</td>");
      }
      scoutln(out, bytesOut, "<td align=center><p><b>Document Code</td>");
      scoutln(out, bytesOut, "<td align=center><p><b>Document Type</td>");
      scoutln(out, bytesOut, "<td align=center><p><b>Last Check-in Date</td>");
      scoutln(out, bytesOut, "<td align=center><p><b>Internal<br>Or<br>External</td>");
      scoutln(out, bytesOut, "<td align=center><p><b>Project Code</td>");
      scoutln(out, bytesOut, "<td align=center><p><b>Name</td>");
      scoutln(out, bytesOut, "<td align=center><p><b>File Size</td>");
      scoutln(out, bytesOut, "<td align=center><p><b>Permissions</td></tr>");

      libraryUtils.listDocuments(out, directory, userName, canDownload, canCheckout, canCheckin, canEditDetails, dnm, imagesDir, localDefnsDir, defnsDir, bytesOut);

      scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr></table>");
    }
    else scoutln(out, bytesOut, "<br><br><br><br><br><p>&nbsp;&nbsp;&nbsp;&nbsp;Select an option from the above menu</p>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {
    out.println(str);
    bytesOut[0] += str.length() + 2;
  }

}
