// =======================================================================================================================================================================================================
// System: ZaraStar Library: Edit document details
// Module: LibraryEditDocumentDetails.java
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

public class LibraryEditDocumentDetailsWave extends HttpServlet
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

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="";

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
      p1  = req.getParameter("p1"); // docCode

      doIt(out, req, p1, unm, sid, uty, men, den, dnm, bnm, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "LibraryEditDocumentDetails", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 12001, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush(); 
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String p1, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String libraryDir    = directoryUtils.getUserDir('L', dnm, unm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreenW(out, req, 12, unm, sid, uty, men, den, dnm, bnm, "LibraryEditDocumentDetails", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 12001, bytesOut[0], 0, "SID:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    scoutln(out, bytesOut, "12001\001Documents\001Document Edit\001javascript:getHTML('LibraryEditDocumentDetailsWave','&p1=" + p1 + "')\001\001\001\001\003");

    set(con, stmt, rs, out, req, p1, unm, sid, uty, men, den, dnm, bnm, imagesDir, libraryDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 12001, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String docCode, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String imagesDir, String libraryDir,
                   String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    String cssDirectory = directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir);

    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + cssDirectory + "general.css\">");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");
    scoutln(out, bytesOut, "function update(){postForm('LibraryEditDocumentDetailsUpdateWave','12001w');}");
    scoutln(out, bytesOut, "</script>");

    scoutln(out, bytesOut, "<form id=\"12001w\" enctype=\"application/x-www-form-urlencoded\" method=post>");

    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"unm\" value='" + unm + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"sid\" value='" + sid + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"uty\" value='" + uty + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"men\" value='" + men + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"den\" value='" + den + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"dnm\" value='" + dnm + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"bnm\" value='" + bnm + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"p1\" value='"  + docCode + "'>");

    libraryUtils.drawTitleW(con, stmt, rs, req, out, "Edit Document Details", "12001", unm, uty, dnm, localDefnsDir, defnsDir, bytesOut);

    scoutln(out, bytesOut, "<table id=\"page\" width=100% cellpadding=3 cellspacing=3><tr nowrap><td colspan=3>");

    scoutln(out, bytesOut, "<tr><td nowrap><p>Document Code:</td><td width=90%><h1>" + docCode + "</td></tr>");

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

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td><p>Owner:</td><td><p>" + userName[0]);

    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"userName\" value='"  + userName[0] + "'>");
    scoutln(out, bytesOut, "</td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap><p>Latest Date Checked-In: &nbsp; </td><td><p>" + lastDateIn[0] + "</td></tr>");

    scoutln(out, bytesOut, "<tr><td><p>File Name:</td><td><p>" + docName[0] + "</td>");
    scoutln(out, bytesOut, "<td><input type=hidden name=docName value='" + docName[0] + "'></td></tr>");

    scoutln(out, bytesOut, "<tr><td><p>File Type:</td><td><p>" + docType[0] + "</td></tr>");

    scoutln(out, bytesOut, "<tr><td><p>File Size:</td><td><p>" + generalUtils.formatNumeric(fileSize[0], '0') + " bytes</td></tr>");

    scout(out, bytesOut, "<tr><td><p>Status:</td><td nowrap><p><input type=radio name=status value=I ");
    if(archivedBy[0].length() == 0 && inOrOut[0].equals("I"))
      scout(out, bytesOut, "CHECKED");
    scout(out, bytesOut, ">Checked-In &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
    scout(out, bytesOut, "<input type=radio name=status value=O ");
    if(archivedBy[0].length() == 0 && inOrOut[0].equals("O"))
      scout(out, bytesOut, "CHECKED");
    scout(out, bytesOut, ">Checked-Out &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
    scout(out, bytesOut, "</td></tr>");

    scoutln(out, bytesOut, "<tr><td><p>Directory:</td><td><p>" + directory[0]);

    scoutln(out, bytesOut, "</td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap><p>Internal or External:</td><td><p><input type=radio name=internalOrExternal value=I ");
    if(internalOrExternal[0].equals("I"))
      scout(out, bytesOut, "CHECKED");
    scout(out, bytesOut, ">Internal &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
    scoutln(out, bytesOut, "<input type=radio name=internalOrExternal value=E ");
    if(internalOrExternal[0].equals("E"))
      scout(out, bytesOut, "CHECKED");
    scoutln(out, bytesOut, ">External</td><tr>");

    scoutln(out, bytesOut, "<tr><td nowrap><p>Project Code:</td><td><p>");

    scoutln(out, bytesOut, "<input type=text name=projectCode value=" + projectCode[0] + ">");

    scoutln(out, bytesOut, "</td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td><p><a href=\"javascript:update()\">Update</a> the DataBase</td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "</table></form>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {
    out.println(str);
    bytesOut[0] += (str.length() + 2);
  }
  private void scout(PrintWriter out, int bytesOut[], String str) throws Exception
  {
    out.print(str);
    bytesOut[0] += str.length();
  }

}
