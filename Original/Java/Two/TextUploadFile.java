// =======================================================================================================================================================================================================
// System: ZaraStar Text: upload file
// Module: TextUploadFile.java
// Author: C.K.Harvey
// Copyright (c) 2002-07 Christopher Harvey. All Rights Reserved.
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

public class TextUploadFile extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  TextUtils textUtils = new TextUtils();

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

      unm = req.getParameter("unm");
      sid = req.getParameter("sid");
      uty = req.getParameter("uty");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");

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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "TextUploadFile", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6102, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm,
                    String bnm, int[] bytesOut) throws Exception
  {
    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String textLibraryDir   = directoryUtils.getTextsDir(dnm);

    long startTime = new java.util.Date().getTime();

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 6102, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "TextUploadFile", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6102, bytesOut[0], 0, "ACC:");
      
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "TextUploadFile", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6102, bytesOut[0], 0, "SID:");
      
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    set(con, stmt, rs, out, req, unm, sid, uty, men, den, dnm, bnm, textLibraryDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 6102, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
      
      if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm,
                   String bnm, String textLibraryDir, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Text Library: Upload File</title>");

    String cssDirectory = directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir);
            
    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + cssDirectory + "general.css\">");
    
    scoutln(out, bytesOut, "<form enctype=\"multipart/form-data\" action=\"" + directoryUtils.getScriptsDirectory() + "upload4textlib.php\""
                         + "method=\"post\">");

    int[] hmenuCount = new int[1];

    textUtils.outputPageFrame(con, stmt, rs, out, req, "TextUploadFile", "6102", unm, sid, uty, men, den, dnm, bnm, "", "", localDefnsDir, defnsDir, hmenuCount, bytesOut);

    textUtils.drawTitle(con, stmt, rs, out, req, false, "", "File Upload to Text Library", "6102", unm, sid, uty, men, den, dnm, bnm, hmenuCount, localDefnsDir, defnsDir, bytesOut);

    scoutln(out, bytesOut, "<table border=0 cellpadding=5 id=\"directory\" width=100%>");

    scoutln(out, bytesOut, "<tr><td><p>File to Upload</td>");
    scoutln(out, bytesOut, "<td><p><input type=file name=\"userfile[]\" size=60 maxsize=100></td></tr>");
    
    int len = textLibraryDir.length();
    --len; // trailing '/'
    while(len > 0 && textLibraryDir.charAt(len) != '/') // just-in-case
      --len;    

    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"unm\" value='" + unm + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"sid\" value='" + sid + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"uty\" value='" + uty + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"men\" value='" + men + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"den\" value='" + den + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"dnm\" value='" + dnm + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"bnm\" value='" + bnm + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"lenOfBitToStrip\" value='" + len + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"host\" value='" + serverUtils.serverToCall("TEXT", localDefnsDir) + "'>");
    
    scoutln(out, bytesOut, "<tr><td><p>Target Directory</td>");
    scoutln(out, bytesOut, "<td><p><select name=libDir>");
    buildDirs(out, textLibraryDir, len, bytesOut);
    scoutln(out, bytesOut, "</select></td></tr>");
        
    scoutln(out, bytesOut, "<tr><td><p><input type=\"submit\" value=Upload></td></tr>");   

    scoutln(out, bytesOut, "</table></form>");     scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void buildDirs(PrintWriter out, String textLibraryDir, int textLibraryDirNameLen, int[] bytesOut) throws Exception
  {
    File path = new File(textLibraryDir);
    String fs[];
    fs = path.list();
    
    int len = fs.length;
    
    generalUtils.insertionSort(fs, len);
    
    for(int x=0;x<len;++x)
    {
      if(generalUtils.isDirectory(textLibraryDir + fs[x]))
      {
        scoutln(out, bytesOut, "<option value=\"" + generalUtils.deSanitise(textLibraryDir) + fs[x] + "\">" 
                               + textLibraryDir.substring(textLibraryDirNameLen) + fs[x] + "\n");  
        buildDirs(out, (textLibraryDir + fs[x] + "/"), textLibraryDirNameLen, bytesOut);
      }
    }
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
