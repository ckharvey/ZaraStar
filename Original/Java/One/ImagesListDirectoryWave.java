// =======================================================================================================================================================================================================
// System: ZaraStar ImageEngine: List directory
// Module: ImagesListDirectory.java
// Author: C.K.Harvey
// Copyright (c) 2002-08 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
//
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

public class ImagesListDirectoryWave extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  ImageLib imageLib = new ImageLib();

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
      p1  = req.getParameter("p1");

      if(p1 == null) p1="";

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "ImagesListDirectory", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6501, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                    String p1, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesLibraryDir = directoryUtils.getImagesDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);

    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 6501, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "ImagesListDirectory", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6501, bytesOut[0], 0, "ACC:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "ImagesListDirectory", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6501, bytesOut[0], 0, "SID:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    scoutln(out, bytesOut, "6501\001Images\001Images List\001javascript:getHTML('ImagesListDirectoryWave','')\001\001Y\001\001\003");

    set(con, stmt, rs, out, req, p1, unm, sid, uty, men, den, dnm, bnm, imagesDir, imagesLibraryDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 6501, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);

    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String directory, String unm, String sid,
                   String uty, String men, String den, String dnm, String bnm, String imagesDir, String imagesLibraryDir, String localDefnsDir,
                   String defnsDir, int[] bytesOut) throws Exception
  {
    String cssDirectory = directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir);

    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + cssDirectory + "general.css\">");

    scoutln(out, bytesOut, "<script javascript=\"JavaScript\">");

    boolean canDelete;
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 6507, unm, uty, dnm, localDefnsDir, defnsDir))
      canDelete = true;
    else canDelete = false;

    if(canDelete)
    {
      scoutln(out, bytesOut, "function del(fileName,directory){var p1=sanitise(fileName);var p2=sanitise(directory);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/ImagesDelete?unm=" + unm + "&sid=" + sid + "&uty="
                             + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=\"+p2+\"&p1=\"+p1;}");
    }

    scoutln(out, bytesOut, "function view(fullPathName)");
    scoutln(out, bytesOut, "{");
    scoutln(out, bytesOut, "  var img = document.createElement('img');");
    scoutln(out, bytesOut, "  img.onload = function(evt)");
    scoutln(out, bytesOut, "  {");
    scoutln(out, bytesOut, "    document.getElementById('imgID').src=this.src;");
    scoutln(out, bytesOut, "    document.getElementById('imgID').width=this.width;");
    scoutln(out, bytesOut, "    document.getElementById('imgID').height=this.height;");
    scoutln(out, bytesOut, "  }");
    scoutln(out, bytesOut, "  img.src = fullPathName;");
    scoutln(out, bytesOut, "}");

    scoutln(out, bytesOut, "</script>");

    int[] hmenuCount = new int[1];
    hmenuCount[0] = 0;

    imageLib.drawTitleW(con, stmt, rs, req, out, "Image List", "6501", "ImagesListDirectory", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<table id=\"page\" cellspacing=2 cellpadding=0 width=100%>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap colspan=11><p>For Directory: <b>" + directory + "</b></td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr id=\"pageColumn\">");
    scoutln(out, bytesOut, "<td align=center><p><b>&nbsp;</td>");
    scoutln(out, bytesOut, "<td align=center><p><b>File Name</td>");
    scoutln(out, bytesOut, "<td align=center><p><b>File Size</td>");
    scoutln(out, bytesOut, "<td><p><b>&nbsp;</td></tr>");

    directory = directory.substring(1);
    File path = new File(imagesLibraryDir + directory);
    String fs[] = new String[0];
    fs = path.list();

    int len = fs.length;

    generalUtils.insertionSort(fs, len);

    String cssFormat = "";
    RandomAccessFile fh;

    for(int x=0;x<len;++x)
    {
      if(! generalUtils.isDirectory(imagesLibraryDir + directory + "/" + fs[x]))
      {
        if(cssFormat.equals("line2")) cssFormat = "line1"; else cssFormat = "line2";

        scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\">");

        scoutln(out, bytesOut, "<td align=center><p><a href=\"javascript:view('" + imagesLibraryDir + directory + "/" + fs[x] + "')\">View &nbsp; </a></td>");

        scoutln(out, bytesOut, "<td align=center><p>" + fs[x] + "</td>");
        fh = generalUtils.fileOpen(imagesLibraryDir + directory  + "/" + fs[x]);
        if(fh == null)
          scoutln(out, bytesOut, "<td align=center><p>Unknown</td>");
        else
        {
          scoutln(out, bytesOut, "<td align=center><p>" + fh.length() + "</td>");
          generalUtils.fileClose(fh);
        }

        if(canDelete)
          scoutln(out, bytesOut, "<td align=right><p><a href=\"javascript:del('" + imagesLibraryDir + directory + "/" + fs[x] + "','" + ("/" + directory + "/") + "')\">Delete &nbsp; </a></td>");
        scoutln(out, bytesOut, "</tr>");
      }
    }

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td colspan=4><img id=\"imgID\" border=\"0\" src=\"" + imagesDir + "blank.png\" /></td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "</table>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {
    out.println(str);
    bytesOut[0] += str.length() + 2;
  }

}
