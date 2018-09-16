// =======================================================================================================================================================================================================
// System: ZaraStar ImageEngine: delete directory
// Module: ImagesDeleteDirectory.java
// Author: C.K.Harvey
// Copyright (c) 2002-06 Christopher Harvey. All Rights Reserved.
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
import java.sql.*;
import java.io.*;

public class ImagesDeleteDirectory extends HttpServlet
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
      p1  = req.getParameter("p1"); // dir to delete
      p2  = req.getParameter("p2"); // option

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "ImagesDeleteDirectory", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6509, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men,
                      String den, String dnm, String bnm, String p1, String p2, int[] bytesOut) throws Exception
  {
    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imageLibraryDir  = directoryUtils.getImagesDir(dnm);

    long startTime = new java.util.Date().getTime();

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 6509, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "ImagesDeleteDirectory", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6509, bytesOut[0], 0, "ACC:" + p1);
      
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "ImagesDeleteDirectory", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6509, bytesOut[0], 0, "SID:" + p1);
      
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }
    
    set(con, stmt, rs, out, req, p1, p2, unm, sid, uty, men, den, dnm, bnm, imagesDir, imageLibraryDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 6509, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    
    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String p1, String p2, String unm, String sid, String uty,
                     String men, String den, String dnm, String bnm, String imagesDir, String imageLibraryDir, String localDefnsDir,
                     String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Image Library: Delete Directory</title>");

    String cssDirectory = directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir);
            
    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + cssDirectory + "general.css\">");

    int[] hmenuCount = new int[1];

    imageLib.outputPageFrame(con, stmt, rs, out, req, "6509", "ImagesDeleteDirectory", "", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount, bytesOut);

    imageLib.drawTitle(con, stmt, rs, req, out, "Delete Directory", "6509", "ImagesDeleteDirectory", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<FORM ACTION=\"ImagesDeleteDirectoryExecute\" ENCTYPE=\"application/x-www-form-urlencoded\" METHOD=POST>");

    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"unm\" value='" + unm + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"sid\" value='" + sid + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"uty\" value='" + uty + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"men\" value='" + men + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"den\" value='" + den + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"dnm\" value='" + dnm + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"bnm\" value='" + bnm + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"p1\" value='" + generalUtils.sanitise(p1) + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"p2\" value='" + p2 + "'>");
    
    scoutln(out, bytesOut, "<table border=0 cellpadding=5 id=\"directory\" width=100%>");

    int len = imageLibraryDir.length();
    --len; // trailing '/'
    while(len > 0 && imageLibraryDir.charAt(len) != '/') // just-in-case
      --len;    

    scoutln(out, bytesOut, "<tr><td nowrap><p><font size=3>Delete Directory: &nbsp;&nbsp; </font></td><td width=90%><p>"
                         + "<font size=3><b>" + p1.substring(len) + "</b></font></td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    
    scoutln(out, bytesOut, "<tr><td colspan=2><span id=\"textErrorLarge\">WARNING: This operation will delete the stated directory ("
              + p1.substring(len) + ") and archive all files in that directory, PLUS do the same for all directories and files "
              + "contained under that directory!</span></td></tr>");
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td><p><b>Specifically:</b></td></tr>");
   
    directoryHierarchyList(out, p1 + "/", len, bytesOut);
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    
    scoutln(out, bytesOut, "<tr><td><p><input type=image src=\"" + imagesDir + "go.gif\"></td><td><p>Delete Directory</td></tr>");
    
    scoutln(out, bytesOut, "</table></form>");     scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------
  private void directoryHierarchyList(PrintWriter out, String directory, int len, int[] bytesOut)
  {
    try
    {
      File path = new File(directory);
      String fs[];
      fs = path.list();

      int lenFS = fs.length;
    
      generalUtils.insertionSort(fs, lenFS);

      for(int x=0;x<lenFS;++x)
      {
        if(generalUtils.isDirectory(directory + fs[x]))
          directoryHierarchyList(out, directory + fs[x] + "/", len, bytesOut);
        else scoutln(out, bytesOut, "<tr><td colspan=2><p>" + directory.substring(len) + fs[x] + "</td></tr>");
      }
    }
    catch(Exception e) { } 
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }
  
}
