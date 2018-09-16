// =======================================================================================================================================================================================================
// System: ZaraStar Text: Edit text
// Module: TextEditText.java
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

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.sql.*;

public class TextEditText extends HttpServlet
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
      p1  = req.getParameter("p1"); // fileName (if not new)
      p2  = req.getParameter("p2"); // directory
      p3  = req.getParameter("p3"); // directory + fileName (if coming-in from catalog page itself)
      
      if(p1 == null) p1 = "";
      if(p2 == null) p2 = "";
      if(p3 == null) p3 = "";
      
      doIt(out, req, p1, p2, p3, unm, sid, uty, men, den, dnm, bnm, bytesOut);
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
 
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "TextEditText", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6103, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String p1, String p2, String p3, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String textsDir         = directoryUtils.getTextsDir(dnm);
    
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 6103, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "TextEditText", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6103, bytesOut[0], 0, "ACC:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "TextEditText", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6103, bytesOut[0], 0, "SID:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }
        
    set(con, stmt, rs, out, req, p1, p2, p3, unm, sid, uty, men, den, dnm, bnm, textsDir, localDefnsDir, defnsDir, bytesOut);
    
    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 6103, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String p1, String p2, String p3, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String textsDir,
                   String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Edit Text</title>");
   
    String cssDirectory = directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir);
            
    scoutln(out, bytesOut, "<script language=\"JavaScript\" type=\"text/javascript\" src=\"" + directoryUtils.getEditorDirectory() + "editor.js\"></script>"); 

    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + cssDirectory + "general.css\">");
    
    int[] hmenuCount = new int[1];

    textUtils.outputPageFrame(con, stmt, rs, out, req, "TextEditText", "6103", unm, sid, uty, men, den, dnm, bnm, "", "document.getElementById('wikiview').contentWindow.document.designMode='on';", localDefnsDir, defnsDir, hmenuCount, bytesOut);

    textUtils.drawTitle(con, stmt, rs, out, req, true, p2, "Edit Text", "6103", unm, sid, uty, men, den, dnm, bnm, hmenuCount, localDefnsDir, defnsDir, bytesOut);

    scoutln(out, bytesOut, "<table id=\"page\" cellspacing=2 cellpadding=0 width=100%>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    
    String[] text1 = new String[1];  text1[0] = "";

    if(p3.length() > 0) // if coming-in from catalog page itself
    {
      fetch(textsDir + p3, text1);

      int len = p3.length();
      --len;
      while(len > 0 && p3.charAt(len) != '/')
        --len;

      p1 = p3.substring(len + 1);
      p2 = p3.substring(0, len);

      scoutln(out, bytesOut, "<input type=hidden name=textFile  value=\"" + p1 + "\">");
      scoutln(out, bytesOut, "<input type=hidden name=directory value=\"" + p2 + "\">");
      scoutln(out, bytesOut, "<tr><td><p>Directory: &nbsp;&nbsp; " + p2 + "</p></td></tr>");
      scoutln(out, bytesOut, "<tr><td><p>Text File Name: &nbsp;&nbsp; " + p1 + "</p></td></tr>");
    }
    else
    {
      if(p1.length() > 0)
        fetch(textsDir + p2 + "/" + p1, text1);

      if(p2.length() == 0) // coming-in from directories page (not any specific directory)
      {
        int len = textsDir.length();
        --len; // trailing '/'
        while(len > 0 && textsDir.charAt(len) != '/') // just-in-case
          --len;    

        scoutln(out, bytesOut, "<tr><td><p>Target Directory &nbsp;&nbsp; <select name=directory>");
        buildDirs(out, textsDir, len, bytesOut);
        scoutln(out, bytesOut, "</select></td></tr>");
      }     
      else
      {
        scoutln(out, bytesOut, "<input type=hidden name=directory value=\"" + p2 + "\">");
        scoutln(out, bytesOut, "<tr><td><p>Directory: &nbsp;&nbsp; " + p2 + "</p></td></tr>");
      }
      scoutln(out, bytesOut, "<tr><td><p>Text File Name: &nbsp;&nbsp; <input type=text size=60 maxlength=100 id=textFile name=textFile value=\"" + p1 + "\"></p></td></tr>");
    }
    
    if(! generalUtils.isDirectory(textsDir + p2))
      generalUtils.createDir((textsDir + p2), true);
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "</table><table cellspacing=2 cellpadding=0 width=100%>");
        
    scoutln(out, bytesOut, "<tr><td colspan=4><textarea id=\"text1\" name=\"text1\" cols=\"100\" rows=\"20\">");
    scoutln(out, bytesOut, text1[0] + "</textarea>");
    scoutln(out, bytesOut, "<script language=\"JavaScript\">"
                           + "drawEditor('text1', 'wikiview', '100%', '400', '#FF8888', '#F0F0F0', '#880000','" + unm + "','" + sid + "','" + uty + "','" + men + "','" + den + "','" + dnm + "','" + bnm + "');</script></td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "</table></form>");     scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }
 
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void buildDirs(PrintWriter out, String textLibraryDir, int textLibraryDirNameLen, int[] bytesOut) throws Exception
  {
    File path = new File(textLibraryDir);
    String fs[] = new String[0];
    fs = path.list();
    
    int len = fs.length;
    
    generalUtils.insertionSort(fs, len);

    for(int x=0;x<len;++x)
    {
      if(generalUtils.isDirectory(textLibraryDir + fs[x]))
      {
        scoutln(out, bytesOut, "<option value=\"" + generalUtils.deSanitise(textLibraryDir.substring(textLibraryDirNameLen)) + fs[x] + "\">" + textLibraryDir.substring(textLibraryDirNameLen) + fs[x] + "\n");  
        buildDirs(out, (textLibraryDir + fs[x] + "/"), textLibraryDirNameLen, bytesOut);
      }
    }
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean fetch(String fullPathName, String[] text) throws Exception
  {
    RandomAccessFile fh;
  
    if((fh = generalUtils.fileOpen(fullPathName)) != null)
    {
      String s;
      try
      {
        s = fh.readLine();
        while(s != null)
        {
          text[0] += s;
          s = fh.readLine();
        }
      }
      catch(Exception ioErr)
      {
        generalUtils.fileClose(fh);
        return false;
      }
    }
    else
    {
      return false;
    }
    
    generalUtils.fileClose(fh);
    return true;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += str.length() + 2;    
  }

}
