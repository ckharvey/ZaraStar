// =======================================================================================================================================================================================================
// System: ZaraStar Library: Utilities
// Module: LibraryUtils.java
// Author: C.K.Harvey
// Copyright (c) 2006-09 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.sql.*;

public class LibraryUtils
{
  GeneralUtils generalUtils = new GeneralUtils();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  DashboardUtils dashboardUtils = new DashboardUtils();

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void outputPageFrame(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String callingServlet, String service, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                              String otherSetup, String localDefnsDir, String defnsDir, int[] hmenuCount, int[] bytesOut) throws Exception
  {
    String subMenuText = dashboardUtils.buildSubMenuText(con, stmt, rs, req, service, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount);

    scoutln(out, bytesOut, authenticationUtils.buildScreen(con, stmt, rs, req, false, unm, sid, uty, men, den, dnm, bnm, callingServlet, subMenuText, hmenuCount[0], "", otherSetup, localDefnsDir, defnsDir));

    scoutln(out, bytesOut, "<table border=0><tr><td valign=top><div id='second' style=\"{margin-top:0px;}\"></div></td><td valign=top><div id='third' style=\"{margin-top:0px;margin-left:0px;}\"></div></td></tr></table>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void drawTitle(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, PrintWriter out, String title, String service, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir,
                        String defnsDir, int[] hmenuCount, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<table id='title' width=100%>");

    scoutln(out, bytesOut, "<tr><td colspan=2 nowrap><p>" + title + directoryUtils.buildHelp(service) + "</td></tr></table>");

    scoutln(out, bytesOut, "<table id='submenu' width=100%><tr><td>" + buildSubMenuText(con, stmt, rs, req, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount) + "</td></tr></table>");

    scoutln(out, bytesOut, authenticationUtils.createHorizSetupString(hmenuCount[0]));
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String buildSubMenuText(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] hmenuCount)
                                  throws Exception
  {
    String s = "<div id='submenu'>";

    ++ hmenuCount[0];
if(uty.equals("I"))
{

    boolean LibraryListDirectory = authenticationUtils.verifyAccess(con, stmt, rs, req, 12000, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean LibraryMaintainDirectories = authenticationUtils.verifyAccess(con, stmt, rs, req, 12014, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean LibraryTransactions = authenticationUtils.verifyAccess(con, stmt, rs, req, 12017, unm, uty, dnm, localDefnsDir, defnsDir);

    s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0] + "');\">Directories</dt><dd id='hmenu" + hmenuCount[0]++ + "'><ul>";

    if(LibraryListDirectory)
      s += "<li><a href=\"/central/servlet/LibraryMaintainDirectories?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=M&bnm=" + bnm + "\">My Directories</a></li>";

    if(LibraryMaintainDirectories)
      s += "<li><a href=\"/central/servlet/LibraryMaintainDirectories?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=A&bnm=" + bnm + "\">All Directories</a></li>";

    s += "<li><a href=\"/central/servlet/LibraryMaintainDirectories?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=R&bnm=" + bnm + "\">Reference</a></li>";

    s += "</ul></dt></dl>";

    s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0] + "');\">Upload</dt><dd id='hmenu" + hmenuCount[0]++ + "'><ul>";
    s += "<li><a href=\"/central/servlet/LibraryEngineUploadFile?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=U&bnm=" + bnm + "\">My Directories</a></li>";
    s += "<li><a href=\"/central/servlet/LibraryEngineUploadFile?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=R&bnm=" + bnm + "\">Reference</a></li>";
    s += "</ul></dt></dl>";

    if(LibraryTransactions)
    {
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\">";
      s += "<a href=\"/central/servlet/LibraryTransactions?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Transactions</a></dt></dl>";
    }

    s += "</div>";
}
    --hmenuCount[0];

    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void drawTitleW(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, PrintWriter out, String title, String service, String unm, String uty, String dnm, String localDefnsDir, String defnsDir, int[] bytesOut)
                         throws Exception
  {
    scoutln(out, bytesOut, "<table id='title' width=100%>");
    scoutln(out, bytesOut, "<tr><td colspan=2 nowrap><span id='x'>" + title);
    if(service.length() > 0)
      scoutln(out, bytesOut, directoryUtils.buildHelp(service));
    scoutln(out, bytesOut, "</span></td></tr></table>");

    int hmenuCount[] = new int[1];  hmenuCount[0] = 1;

    scoutln(out, bytesOut, authenticationUtils.createHorizSetupStringW());

    scoutln(out, bytesOut, "<table id='submenuX' width=100%><tr><td>" + buildSubMenuTextW(con, stmt, rs, req, unm, uty, dnm, localDefnsDir, defnsDir, hmenuCount) + "</td></tr></table>");

    --hmenuCount[0];

    scoutln(out, bytesOut, "<script language='JavaScript'>setup(" + hmenuCount[0] + ",'Y');</script>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String buildSubMenuTextW(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, String unm, String uty, String dnm, String localDefnsDir, String defnsDir, int[] hmenuCount) throws Exception
  {
    String s = "<div id='submenu'>";

//    ++ hmenuCount[0];

    boolean LibraryListDirectory = authenticationUtils.verifyAccess(con, stmt, rs, req, 12000, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean LibraryMaintainDirectories = authenticationUtils.verifyAccess(con, stmt, rs, req, 12014, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean LibraryTransactions = authenticationUtils.verifyAccess(con, stmt, rs, req, 12017, unm, uty, dnm, localDefnsDir, defnsDir);

    s += "<dl><dt onmouseover=\"setup(" + hmenuCount[0] + ",'N');\">Directories</dt><dd id='hmenu" + hmenuCount[0]++ + "'><ul>";

    if(LibraryListDirectory)
      s += "<li><a href=\"javascript:getHTML('LibraryMaintainDirectoriesw','&p1=M')\">My Directories</a></li>";

    if(LibraryMaintainDirectories)
      s += "<li><a href=\"javascript:getHTML('LibraryMaintainDirectoriesw','&p1=A')\">All Directories</a></li>";

    s += "<li><a href=\"javascript:getHTML('LibraryMaintainDirectoriesw','&p1=R')\">Reference</a></li>";

    s += "</ul></dt></dl>";

    s += "<dl><dt onmouseover=\"setup(" + hmenuCount[0] + ",'N');\">Upload</dt><dd id='hmenu" + hmenuCount[0]++ + "'><ul>";
    s += "<li><a href=\"javascript:getHTML('LibraryEngineUploadFileWave','&p1=U')\">My Directories</a></li>";
    s += "<li><a href=\"javascript:getHTML('LibraryEngineUploadFileWave','&p1=R')\">Reference</a></li>";
    s += "</ul></dt></dl>";

    if(LibraryTransactions)
    {
      s += "<dl><dt onmouseover=\"setup(" + hmenuCount[0]++ + ",'N');\">";
      s += "<a href=\"javascript:getHTML('LibraryTransactionsw', '')\">Transactions</a></dt></dl>";
    }

    s += "</div>";

    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean checkOutOrInDocument(String docCode, String inOrOut, String date, long fileSize, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    Connection con = null;
    Statement stmt = null;

    try
    {
      String uName    = directoryUtils.getMySQLUserName();
      String passWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();

      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + uName + "&password=" + passWord);

      stmt = con.createStatement();

      String q;
      if(inOrOut.equals("In"))
        q = "UPDATE documents SET InOrOut = 'I', LastDateIn = {d '" + date+ "' }, FileSize = '" + fileSize + "' WHERE DocCode = '" + docCode + "'";
      else q = "UPDATE documents SET InOrOut = 'O' WHERE DocCode = '" + docCode + "'";

      stmt.executeUpdate(q);

      if(stmt != null) stmt.close();
      if(con  != null) con.close();

      return true;
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }

    return false;
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // if file already exists in stated directory, that database record is updated (that is, a new record is NOT added)
  public String addToDocumentsAndPermissions(String userName, String docName, String docType, String inOrOut, String lastDateIn, String directory, String projectCode, String internalOrExternalOrAnonymous, String archivedDate, String archivedBy,
                                             long fileSize, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    Connection con = null;
    Statement stmt = null;
    ResultSet rs   = null;

    try
    {
      String uName    = directoryUtils.getMySQLUserName();
      String passWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();

      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + uName + "&password=" + passWord);

      // check if rec already exists
      stmt = con.createStatement();

//      docName = sanitise(docName);//
//      directory = sanitise(directory);
      rs = stmt.executeQuery("SELECT DocCode FROM documents WHERE DocName = '" + generalUtils.sanitiseForSQL(docName) + "' AND Directory = '" + generalUtils.sanitiseForSQL(directory) + "'");

      String docCode = null;
      if(rs.next())
        docCode = rs.getString(1);

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();

      if(docCode != null) // already exists
      {
        stmt = con.createStatement();

        String q = "UPDATE documents SET InOrOut = 'I', LastDateIn = {d '" + lastDateIn + "' }, FileSize = '" + fileSize + "' WHERE DocCode = '" + docCode + "'";

        stmt.executeUpdate(q);

        if(stmt != null) stmt.close();
      }
      else // create new rec
      {
        stmt = con.createStatement();

        // determine next docCode
        rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM documents");

        int rowCount = 0;

        if(rs.next())
        {
          rowCount = rs.getInt("rowcount");
        }

        if(rs   != null) rs.close();
        docCode = generalUtils.intToStr(rowCount + 1);

        if(stmt != null) stmt.close();

        stmt = con.createStatement();

        String q = "INSERT INTO documents ( DocCode, UserName, DocName, DocType, InOrOut, LastDateIn, Directory, ProjectCode, InternalOrExternalOrAnonymous, ArchivedDate, ArchivedBy, FileSize) "
                 + "VALUES ('" + docCode + "','" + userName + "','" + generalUtils.sanitiseForSQL(docName) + "','" + docType + "','" + inOrOut + "',{d '" + lastDateIn + "' },'" + generalUtils.sanitiseForSQL(directory) + "','" + projectCode + "','"
                 + internalOrExternalOrAnonymous + "','" + archivedDate + "','" + archivedBy + "','" + fileSize + "')";

        stmt.executeUpdate(q);

        if(stmt != null) stmt.close();

        // check for new code
        stmt = con.createStatement();

        rs = stmt.executeQuery("SELECT DocCode FROM documents WHERE DocName = '" + generalUtils.sanitiseForSQL(docName) + "' AND Directory = '" + generalUtils.sanitiseForSQL(directory) + "'");
        if(rs.next())
          docCode = rs.getString(1);

        if(rs   != null) rs.close();
        if(stmt != null) stmt.close();
      }

      if(con  != null) con.close();

      return docCode;
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }

    return "";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void listDocuments(PrintWriter out, String directory, String forUserName, boolean canDownload, boolean canCheckout, boolean canCheckin, boolean canEditDetails, String dnm, String imagesDir, String localDefnsDir, String defnsDir,
                            int[] bytesOut) throws Exception
  {
    Connection con = null;
    Statement stmt = null;
    ResultSet rs = null;
    ResultSetMetaData rsmd = null;

    try
    {
      String uName    = directoryUtils.getMySQLUserName();
      String passWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();

      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + uName + "&password=" + passWord);

      stmt = con.createStatement();

      String q = "SELECT DocCode, DocName, DocType, InOrOut, LastDateIn, ProjectCode, InternalOrExternalOrAnonymous, ArchivedBy, FileSize FROM documents WHERE UserName='" + forUserName + "' AND Directory='" + generalUtils.sanitiseForSQL(directory)
               + "' ORDER BY DocName";

      rs = stmt.executeQuery(q);
      rsmd = rs.getMetaData();

      String docCode, docName, docType, inOrOut, lastDateIn, projectCode, internalOrExternalOrAnonymous, archivedBy, fileSize, cssFormat = "";

      while(rs.next())
      {
        docCode                       = getValue(1, ' ', rs, rsmd);
        docName                       = getValue(2, ' ', rs, rsmd);
        docType                       = getValue(3, ' ', rs, rsmd);
        inOrOut                       = getValue(4, ' ', rs, rsmd);
        lastDateIn                    = getValue(5, 'D', rs, rsmd);
        projectCode                   = getValue(6, ' ', rs, rsmd);
        internalOrExternalOrAnonymous = getValue(7, ' ', rs, rsmd);
        archivedBy                    = getValue(8, ' ', rs, rsmd);
        fileSize                      = getValue(9, ' ', rs, rsmd);

        if(internalOrExternalOrAnonymous.equals("E"))
          internalOrExternalOrAnonymous = "External";
        else
        if(internalOrExternalOrAnonymous.equals("A"))
          internalOrExternalOrAnonymous = "Anonymous";
        else internalOrExternalOrAnonymous = "Internal";

        if(cssFormat.equals("line2")) cssFormat = "line1"; else cssFormat = "line2";

        scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\">");

        if(canDownload)
          scoutln(out, bytesOut, "<td align=center><p><a href=\"javascript:read('" + docCode + "')\"><img src=\"" + imagesDir + "libraryread.gif\" border=0></a></td>");

        if(archivedBy.length() > 0)
          scoutln(out, bytesOut, "<td align=center><p><img src=\"" + imagesDir + "libraryarchived.gif\" border=0></a></td>");
        else
        if(inOrOut.equals("I"))
        {
          scout(out, bytesOut, "<td align=center><p>");
          if(canCheckout)
            scout(out, bytesOut, "<a href=\"javascript:checkout('" + docCode + "')\">");
          scout(out, bytesOut, "<img src=\"" + imagesDir + "librarycheckout.gif\" border=0>");
          if(canCheckout)
            scout(out, bytesOut, "</a>");
          scoutln(out, bytesOut, "</td>");
        }
        else
        {
          scout(out, bytesOut, "<td align=center><p>");
          if(canCheckin)
            scout(out, bytesOut, "<a href=\"javascript:checkin('" + docCode + "')\">");
          scout(out, bytesOut, "<img src=\"" + imagesDir + "librarycheckin.gif\" border=0>");
          if(canCheckin)
            scout(out, bytesOut, "</a>");
          scoutln(out, bytesOut, "</td>");
        }

        if(canEditDetails)
        {
          if(archivedBy.length() == 0)
          {
            scoutln(out, bytesOut, "<td align=center><p><a href=\"javascript:details('" + docCode + "')\"><img src=\"" + imagesDir + "librarydetails.gif\" border=0></a></td>");
            scoutln(out, bytesOut, "<td align=center><p><a href=\"javascript:manage('" + docCode + "')\"><img src=\"" + imagesDir + "libraryread.gif\" border=0></a></td>");
          }
          else scoutln(out, bytesOut, "<td></td><td></td>");
        }

        scoutln(out, bytesOut, "<td align=center><p>" + docCode + "</td>");
        scoutln(out, bytesOut, "<td align=center><p>" + docType + "</td>");
        scoutln(out, bytesOut, "<td align=center><p>" + lastDateIn + "</td>");
        scoutln(out, bytesOut, "<td align=center><p>" + internalOrExternalOrAnonymous + "</td>");
        scoutln(out, bytesOut, "<td align=center><p>" + projectCode + "</td>");
        scoutln(out, bytesOut, "<td><p>" + docName + "</td>");
        scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(fileSize, '0') + "&nbsp;</td>");
        scoutln(out, bytesOut, "<td><p>&nbsp;" + getPermissionsList(docCode, forUserName, dnm, localDefnsDir, defnsDir) + "</td></tr>");
      }

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void listArchivedDocuments(PrintWriter out, String unm, String dnm, String imagesDir, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    Connection con = null;
    Statement stmt = null;
    ResultSet rs = null;
    ResultSetMetaData rsmd = null;

    try
    {
      String uName    = directoryUtils.getMySQLUserName();
      String passWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();

      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + uName + "&password=" + passWord);

      stmt = con.createStatement();

      String q = "SELECT DocCode, UserName, DocName, DocType, InOrOut, LastDateIn, Directory, ProjectCode, InternalOrExternalOrAnonymous, ArchivedDate, ArchivedBy, FileSize FROM documents WHERE UserName='" + unm
               + "' AND ArchivedBy != '' ORDER BY DocCode";

      rs = stmt.executeQuery(q);
      rsmd = rs.getMetaData();

      String docCode, docName, docType, lastDateIn, directory, projectCode, internalOrExternalOrAnonymous, archivedDate, archivedBy, fileSize, cssFormat = "";

      while(rs.next())
      {
        docCode                       = getValue(1,  ' ', rs, rsmd);
        docName                       = getValue(2,  ' ', rs, rsmd);
        docType                       = getValue(3,  ' ', rs, rsmd);
        lastDateIn                    = getValue(4,  'D', rs, rsmd);
        directory                     = getValue(5,  ' ', rs, rsmd);
        projectCode                   = getValue(6,  ' ', rs, rsmd);
        internalOrExternalOrAnonymous = getValue(7,  ' ', rs, rsmd);
        archivedDate                  = getValue(8,  'D', rs, rsmd);
        archivedBy                    = getValue(9,  ' ', rs, rsmd);
        fileSize                      = getValue(10, ' ', rs, rsmd);

        if(internalOrExternalOrAnonymous.equals("E"))
          internalOrExternalOrAnonymous = "External";
        else
        if(internalOrExternalOrAnonymous.equals("A"))
          internalOrExternalOrAnonymous = "Anonymous";
        else internalOrExternalOrAnonymous = "Internal";

        if(cssFormat.equals("line2")) cssFormat = "line1"; else cssFormat = "line2";

        scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\">");
        scoutln(out, bytesOut, "<td align=center><p><a href=\"javascript:read('" + docCode + "')\"><img src=\"" + imagesDir + "libraryread.gif\" border=0></a></td>");

        scoutln(out, bytesOut, "<td align=center><p><a href=\"javascript:restore('" + docCode + "')\"><img src=\"" + imagesDir + "libraryrestore.gif\" border=0></a></td>");

        scoutln(out, bytesOut, "<td align=center><p>" + docCode + "</td>");
        scoutln(out, bytesOut, "<td align=center><p>" + docType + "</td>");
        scoutln(out, bytesOut, "<td align=center><p>" + lastDateIn + "</td>");
        scoutln(out, bytesOut, "<td align=center><p>" + internalOrExternalOrAnonymous + "</td>");
        scoutln(out, bytesOut, "<td align=center><p>" + projectCode + "</td>");
        scoutln(out, bytesOut, "<td><p>" + docName + "</td>");
        scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(fileSize, '0') + "&nbsp;</td>");
        scoutln(out, bytesOut, "<td><p>&nbsp;" + directory + "</td>");
        scoutln(out, bytesOut, "<td><p>"     + "</td>");
        scoutln(out, bytesOut, "<td><p>" + archivedDate + "</td>");
        scoutln(out, bytesOut, "<td><p>" + archivedBy + "</td></tr>");
      }

      rs.close();

      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getDocumentName(String docCode, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    String[] internalOrExternalOrAnonymous = new String[1];
    String[] userName                      = new String[1];
    String[] docName                       = new String[1];
    String[] docType                       = new String[1];
    String[] inOrOut                       = new String[1];
    String[] lastDateIn                    = new String[1];
    String[] directory                     = new String[1];
    String[] archivedDate                  = new String[1];
    String[] archivedBy                    = new String[1];
    String[] fileSize                      = new String[1];
    String[] projectCode                   = new String[1];

    getDocumentDetails(docCode, userName, docName, docType, inOrOut, lastDateIn, directory, projectCode, internalOrExternalOrAnonymous, archivedDate, archivedBy, fileSize, dnm, localDefnsDir, defnsDir);

    return docName[0];
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void getDocumentDetails2(String docCode, String[] projectCode, String[] internalOrExternalOrAnonymous, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    String[] userName           = new String[1];
    String[] docName            = new String[1];
    String[] docType            = new String[1];
    String[] inOrOut            = new String[1];
    String[] lastDateIn         = new String[1];
    String[] directory          = new String[1];
    String[] archivedDate       = new String[1];
    String[] archivedBy         = new String[1];
    String[] fileSize           = new String[1];

    getDocumentDetails(docCode, userName, docName, docType, inOrOut, lastDateIn, directory, projectCode, internalOrExternalOrAnonymous, archivedDate, archivedBy, fileSize, dnm, localDefnsDir, defnsDir);
  }
  public void getDocumentDetails(String docCode, String[] userName, String[] docName, String[] docType, String[] inOrOut, String[] lastDateIn, String[] directory, String[] projectCode, String[] internalOrExternalOrAnonymous, String[] archivedDate,
                                 String[] archivedBy, String[] fileSize, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    Connection con = null;
    Statement stmt = null;
    ResultSet rs = null;
    ResultSetMetaData rsmd = null;

    userName[0] = docName[0] = docType[0] = inOrOut[0] = lastDateIn[0] = directory[0] = projectCode[0] = internalOrExternalOrAnonymous[0] = archivedDate[0] = archivedBy[0] = fileSize[0] = "";

    try
    {
      String uName    = directoryUtils.getMySQLUserName();
      String passWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();

      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + uName + "&password=" + passWord);

      stmt = con.createStatement();

      String q = "SELECT UserName, DocName, DocType, InOrOut, LastDateIn, Directory, ProjectCode, InternalOrExternalOrAnonymous, ArchivedDate, ArchivedBy, FileSize FROM documents WHERE DocCode='" + docCode + "'";

      rs = stmt.executeQuery(q);
      rsmd = rs.getMetaData();

      if(rs.next()) // just-in-case
      {
        userName[0]                      = getValue(1,  ' ', rs, rsmd);
        docName[0]                       = getValue(2,  ' ', rs, rsmd);
        docType[0]                       = getValue(3,  ' ', rs, rsmd);
        inOrOut[0]                       = getValue(4,  ' ', rs, rsmd);
        lastDateIn[0]                    = getValue(5,  'D', rs, rsmd);
        directory[0]                     = getValue(6,  ' ', rs, rsmd);
        projectCode[0]                   = getValue(7,  ' ', rs, rsmd);
        internalOrExternalOrAnonymous[0] = getValue(8,  ' ', rs, rsmd);
        archivedDate[0]                  = getValue(9,  'D', rs, rsmd);
        archivedBy[0]                    = getValue(10, ' ', rs, rsmd);
        fileSize[0]                      = getValue(11, ' ', rs, rsmd);
      }

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean getDocumentDetailsUsingFileAndDirAndUserNames(String docName, String directory, String userName, String[] docCode, String[] docType, String[] inOrOut, String[] lastDateIn, String[] projectCode,
                                                               String[] internalOrExternalOrAnonymous, String[] archivedDate, String[] archivedBy, String[] fileSize,  String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    Connection con = null;
    Statement stmt = null;
    ResultSet rs = null;
    ResultSetMetaData rsmd = null;

    docCode[0] = docType[0] = inOrOut[0] = lastDateIn[0] = projectCode[0] = internalOrExternalOrAnonymous[0] = archivedDate[0] = archivedBy[0] = fileSize[0] = "";

    boolean found = false;
    try
    {
      String uName    = directoryUtils.getMySQLUserName();
      String passWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();

      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + uName + "&password=" + passWord);

      stmt = con.createStatement();

      docName = sanitise(docName);
      directory = sanitise(directory);
      String q = "SELECT DocCode, DocType, InOrOut, LastDateIn, ProjectCode, InternalOrExternalOrAnonymous, ArchivedDate, ArchivedBy, FileSize FROM documents WHERE DocName='" + generalUtils.sanitiseForSQL(docName) + "' AND Directory = '"
               + generalUtils.sanitiseForSQL(directory) + "' AND UserName='" + userName + "'";

      rs = stmt.executeQuery(q);
      rsmd = rs.getMetaData();

      if(rs.next()) // just-in-case
      {
        found = true;
        docCode[0]                       = getValue(1, ' ', rs, rsmd);
        docType[0]                       = getValue(2, ' ', rs, rsmd);
        inOrOut[0]                       = getValue(3, ' ', rs, rsmd);
        lastDateIn[0]                    = getValue(4, 'D', rs, rsmd);
        projectCode[0]                   = getValue(5, ' ', rs, rsmd);
        internalOrExternalOrAnonymous[0] = getValue(6, ' ', rs, rsmd);
        archivedDate[0]                  = getValue(7, 'D', rs, rsmd);
        archivedBy[0]                    = getValue(8, ' ', rs, rsmd);
        fileSize[0]                      = getValue(9, ' ', rs, rsmd);
      }

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }

    return found;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getPermissionsList(String docCode, String userName, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    Connection con = null;
    Statement stmt = null;
    ResultSet rs = null;
    ResultSetMetaData rsmd = null;

    String grant, type, list="";
    boolean first=true;

    try
    {
      String uName    = directoryUtils.getMySQLUserName();
      String passWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();

      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + uName + "&password=" + passWord);

      stmt = con.createStatement();

      String q = "SELECT PermissionGrant, PermissionType FROM permissions WHERE DocCode='" + docCode + "'";

      rs = stmt.executeQuery(q);
      rsmd = rs.getMetaData();

      while(rs.next()) // just-in-case
      {
        if(! first)
          list += ", ";
        else first = false;

        grant = getValue(1, ' ', rs, rsmd);
        type  = getValue(2, ' ', rs, rsmd);

        list += (grant + " (" + type + ")");
      }

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }

    if(first) // no recs found
      return (userName + " (RW)");

    return list;
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void createTableDocuments(boolean dropTable, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    String userName = directoryUtils.getMySQLUserName();
    String passWord = directoryUtils.getMySQLPassWord();

    Class.forName("com.mysql.jdbc.Driver").newInstance();

    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + userName + "&password=" + passWord);
    Statement stmt = con.createStatement();
    String q;

    try
    {
      if(dropTable)
      {
        q = "DROP TABLE documents";
        stmt.executeUpdate(q);
      }
    }
    catch(Exception e) { }
    if(stmt != null) stmt.close();

    try
    {
      stmt = con.createStatement();
      q = "CREATE TABLE documents ( DocCode integer not null, UserName char(40) not null, DocName char(200) not null, DocType char(20), InOrOut char(1), LastDateIn date, Directory char(200), ProjectCode char(20), "
                                 + "InternalOrExternalOrAnonymous char(1), ArchivedBy char(20), ArchivedDate date, FileSize integer, unique(DocCode))";

      stmt.executeUpdate(q);
    }
    catch(Exception e) { }

    if(stmt != null) stmt.close();

    try
    {
      stmt = con.createStatement();
      q = "CREATE INDEX userNameInx on documents(UserName)";

      stmt.executeUpdate(q);
    }
    catch(Exception e) { }

    if(stmt != null) stmt.close();

    try
    {
      stmt = con.createStatement();
      q = "CREATE INDEX lastDateInInx on documents(LastDateIn)";

      stmt.executeUpdate(q);
    }
    catch(Exception e) { }

    if(stmt != null) stmt.close();

    try
    {
      stmt = con.createStatement();
      q = "CREATE INDEX projectCodeInx on documents(ProjectCode)";

      stmt.executeUpdate(q);
    }
    catch(Exception e) { }

    if(stmt != null) stmt.close();

    if(con  != null) con.close();
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void createTablePermissions(boolean dropTable, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    String userName = directoryUtils.getMySQLUserName();
    String passWord = directoryUtils.getMySQLPassWord();

    Class.forName("com.mysql.jdbc.Driver").newInstance();

    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + userName + "&password=" + passWord);
    Statement stmt = con.createStatement();
    String q;

    try
    {
      if(dropTable)
      {
        q = "DROP TABLE permissions";
        stmt.executeUpdate(q);
      }
    }
    catch(Exception e) { }
    stmt.close();

    try
    {
      stmt = con.createStatement();
      q = "CREATE TABLE permissions ( DocCode integer not null, PermissionGrant char(40) not null, PermissionType char(1) not null, unique(DocCode, PermissionGrant))";

      stmt.executeUpdate(q);
    }
    catch(Exception e) { }

    if(stmt != null) stmt.close();
    if(con  != null) con.close();
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void createTableDocmove(boolean dropTable, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    String userName = directoryUtils.getMySQLUserName();
    String passWord = directoryUtils.getMySQLPassWord();

    Class.forName("com.mysql.jdbc.Driver").newInstance();

    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + userName + "&password=" + passWord);
    Statement stmt = con.createStatement();
    String q;

    try
    {
      if(dropTable)
      {
        q = "DROP TABLE docmove";
        stmt.executeUpdate(q);
      }
    }
    catch(Exception e) { }
    stmt.close();

    try
    {
      stmt = con.createStatement();
      q = "CREATE TABLE docmove ( DocCode integer not null, DateTime timestamp, UploadOrDownload char(1), UserCode char(40), FileSize integer )";

      stmt.executeUpdate(q);
    }
    catch(Exception e) { }

    if(stmt != null) stmt.close();
    if(con  != null) con.close();
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void createTableProperties(boolean dropTable, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    String userName = directoryUtils.getMySQLUserName();
    String passWord = directoryUtils.getMySQLPassWord();

    Class.forName("com.mysql.jdbc.Driver").newInstance();

    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + userName + "&password=" + passWord);
    Statement stmt = con.createStatement();
    String q;

    try
    {
      if(dropTable)
      {
        q = "DROP TABLE properties";
        stmt.executeUpdate(q);
      }
    }
    catch(Exception e) { }
    if(stmt != null) stmt.close();

    try
    {
      stmt = con.createStatement();
      q = "CREATE TABLE properties ( UserCode char(20) not null, DirectoryName char(200) not null, CompanyCode char(20), CompanyType char(1), DocumentsType char(20), DateLastModified timestamp, unique(userCode, DirectoryName))";
      stmt.executeUpdate(q);
    }
    catch(Exception e) { }

    if(stmt != null) stmt.close();
    if(con  != null) con.close();
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void showStats(PrintWriter out, String dnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    String userName = directoryUtils.getMySQLUserName();
    String passWord = directoryUtils.getMySQLPassWord();

    Class.forName("com.mysql.jdbc.Driver").newInstance();

    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + userName + "&password="
                                                 + passWord);
    Statement stmt = con.createStatement();

    ResultSet rs = stmt.executeQuery("SELECT TABLE_NAME, CREATE_TIME, UPDATE_TIME, TABLE_ROWS FROM INFORMATION_SCHEMA.TABLES WHERE table_schema='" + dnm + "_ofsa'");

    String tableName="", createDate="", numRows="", modifyDate="", cssFormat="";

    while(rs.next())
    {
      tableName = rs.getString(1);
      createDate = rs.getString(2);
      modifyDate = rs.getString(3);
      numRows    = rs.getString(4);

      if(cssFormat.equals("line2")) cssFormat = "line1"; else cssFormat = "line2";

      scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\"><td>" + tableName + "</td>");
      scoutln(out, bytesOut, "<td align=center>" + numRows + "</td>");
      scoutln(out, bytesOut, "<td align=center>" + createDate + "</td>");
      scoutln(out, bytesOut, "<td align=center>" + modifyDate + "</td></tr>");
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    if(con  != null) con.close();
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String getValue(int colNum, char type, ResultSet rs, ResultSetMetaData rsmd)
  {
    if(colNum < 0)
      return "";

    try
    {
      Integer f;
      java.sql.Date d;
      Time t;

      String str="";

      switch(rsmd.getColumnType(colNum))
      {
        case java.sql.Types.CHAR    : str = rs.getString(colNum);
                                      str = generalUtils.stripTrailingSpacesStr(str);
                                      break;
        case java.sql.Types.INTEGER : f   = rs.getInt(colNum);
                                      str = f.toString();
                                      break;
        case 91                     : if(type == 'D')
                                      {
                                        d  = rs.getDate(colNum);
                                        str = d.toString();
                                      }
                                      else
                                      {
                                        t = rs.getTime(colNum);
                                        str = t.toString();
                                      }
                                      break;
      }

      return str;
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }

    return "";
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String sanitise(String s)
  {
    int x=0;
    String t="";
    while(x < s.length())
    {
      if(s.charAt(x) == '\'')
        t += "\\\'";
      else t += s.charAt(x);
      ++x;
    }
    return t;
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
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

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getDocumentNameGivenCode(String docCode, String unm, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    Connection con  = null;
    Statement  stmt = null;
    ResultSet  rs   = null;

    String fullPathName = "";

    try
    {
      String uName    = directoryUtils.getMySQLUserName();
      String passWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();

      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + uName + "&password=" + passWord);

      fullPathName = getDocumentNameGivenCode(con, stmt, rs, docCode, dnm);
    }
    catch(Exception e) { }

    if(con  != null) con.close();

    return fullPathName;
  }
  public String getDocumentNameGivenCode(Connection con, Statement stmt, ResultSet rs, String docCode, String dnm) throws Exception
  {
    String fullPathName = "";

    try
    {
      stmt = con.createStatement();

      String q = "SELECT DocName, Directory, UserName FROM documents WHERE DocCode='" + docCode + "'";

      rs = stmt.executeQuery(q);

      String dir;

      if(rs.next()) // just-in-case
      {
        dir = rs.getString(2);
        if(! dir.endsWith("/"))
          dir += "/";

        if(rs.getString(3).equals("___REFERENCE___"))
          fullPathName = "/Zara/" + dnm + "/Reference" + dir + rs.getString(1);
        else fullPathName = directoryUtils.getUserDir('L', dnm, rs.getString(3)) + dir + rs.getString(1);
      }

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }

    return fullPathName;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean updateDocuments(String docCode, String userName, String docName, String docType, String inOrOut, String lastDateIn, String directory, String projectCode, String internalOrExternalOrAnonymous, String archivedDate,
                                 String archivedBy, String fileSize, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    Connection con = null;
    Statement stmt = null;

    try
    {
      String uName    = directoryUtils.getMySQLUserName();
      String passWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();

      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + uName + "&password=" + passWord);

      docName = sanitise(docName);
      directory = sanitise(directory);

      stmt = con.createStatement();

      String q = "UPDATE documents SET InOrOut = '" + inOrOut + "', UserName = '" + userName + "', DocName = '" + docName + "', DocType = '" + docType + "', LastDateIn = {d '" + lastDateIn + "'}, Directory = '" + directory + "', ProjectCode = '"
               + projectCode + "', InternalOrExternalOrAnonymous = '" + internalOrExternalOrAnonymous + "', ArchivedBy = '" + archivedBy + "', ArchivedDate = '" + archivedDate + "', FileSize = '" + fileSize + "' WHERE DocCode = '" + docCode
               + "'";

      stmt.executeUpdate(q);

      if(stmt != null) stmt.close();
      if(con  != null) con.close();

      return true;
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }

    return false;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // if file already exists in stated directory, that database record is updated (that is, a new record is NOT added)
  public boolean addToProperties(String directory, String companyCode, String companyType, String documentsType, String unm, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    Connection con = null;
    Statement stmt = null;
    ResultSet rs   = null;

    try
    {
      String uName    = directoryUtils.getMySQLUserName();
      String passWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();

      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + uName + "&password=" + passWord);

      // check if rec already exists
      stmt = con.createStatement();

      directory = sanitise(directory);

      boolean exists = false;

      rs = stmt.executeQuery("SELECT UserCode, DirectoryName FROM properties WHERE UserCode = '" + unm + "' AND DirectoryName = '" + generalUtils.sanitiseForSQL(directory) + "'");

      if(rs.next())
        exists = true;

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();

      if(exists)
      {
        stmt = con.createStatement();

        String q = "UPDATE properties SET CompanyCode = '" + companyCode + "', companyType= '" + companyType + "', DocumentsType = '" + documentsType + "', DatelastModified = NULL WHERE UserCode = '" + unm + "' AND DirectoryName = '" + directory
                 + "'";

        stmt.executeUpdate(q);

        if(stmt != null) stmt.close();
      }
      else // create new rec
      {
        stmt = con.createStatement();

        String q = "INSERT INTO properties ( UserCode, DirectoryName, CompanyCode, CompanyType, DocumentsType ) VALUES ('" + unm + "','" + directory + "','" + companyCode + "','" + companyType + "','" + documentsType + "')";

        stmt.executeUpdate(q);

        if(stmt != null) stmt.close();
      }

      if(con  != null) con.close();

      return true;
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }

    return false;
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void getProperties(String directory, String unm, String dnm, String localDefnsDir, String defnsDir, String[] companyCode, String[] companyType, String[] documentsType) throws Exception
  {
    Connection con  = null;
    Statement  stmt = null;
    ResultSet  rs   = null;

    try
    {
      String uName    = directoryUtils.getMySQLUserName();
      String passWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();

      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + uName + "&password=" + passWord);

      stmt = con.createStatement();

      String q = "SELECT CompanyCode, CompanyType, DocumentsType FROM properties WHERE UserCode = '" + unm + "' AND DirectoryName = '" + generalUtils.sanitiseForSQL(directory) + "'";

      rs = stmt.executeQuery(q);

      if(rs.next()) // just-in-case
      {
        companyCode[0]   = rs.getString(1);
        companyType[0]   = rs.getString(2);
        documentsType[0] = rs.getString(3);
      }
      else
      {
        companyCode[0]   = "";
        companyType[0]   = "";
        documentsType[0] = "";
      }

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }
    catch(Exception e)
    {
      companyCode[0]   = "";
      companyType[0]   = "";
      documentsType[0] = "";

      System.out.println(e);

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean deleteFromDocuments(String docCode, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    Connection con = null;
    Statement stmt = null;

    try
    {
      String uName    = directoryUtils.getMySQLUserName();
      String passWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();

      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + uName + "&password=" + passWord);

      stmt = con.createStatement();

      String q = "DELETE FROM documents WHERE DocCode = '" + docCode + "'";

      stmt.executeUpdate(q);

      if(stmt != null) stmt.close();
      if(con  != null) con.close();

      return true;
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }

    return false;
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getDocumentCodeGivenName(String docName, String directory, String unm, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    Connection con  = null;
    Statement  stmt = null;
    ResultSet  rs   = null;

    String docCode = "";

    try
    {
      String uName    = directoryUtils.getMySQLUserName();
      String passWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();

      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + uName + "&password=" + passWord);

      stmt = con.createStatement();

      String q = "SELECT DocCode FROM documents WHERE DocName = '" + generalUtils.sanitiseForSQL(docName) + "' AND Directory = '" + generalUtils.sanitiseForSQL(directory) + "' AND UserName = '" + unm + "'";

      rs = stmt.executeQuery(q);

      if(rs.next()) // just-in-case
        docCode = rs.getString(1);

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }

    return docCode;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean addToDocmove(String docCode, String uploadOrDownload, int fileSize, String unm, String dnm) throws Exception
  {
    Connection con = null;
    Statement stmt = null;

    try
    {
      String uName    = directoryUtils.getMySQLUserName();
      String passWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();

      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + uName + "&password=" + passWord);

      stmt = con.createStatement();

      String q = "INSERT INTO docmove ( DocCode, DateTime, UploadOrDownload, UserCode, FileSize ) VALUES ('" + docCode + "', NULL, '" + uploadOrDownload + "','" + unm + "','" + fileSize + "')";

      stmt.executeUpdate(q);

      if(stmt != null) stmt.close();
      if(con  != null) con.close();

      return true;
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }

    return false;
  }

}
