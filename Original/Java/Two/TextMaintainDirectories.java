// =======================================================================================================================================================================================================
// System: ZaraStar Text: Maintain Directories
// Module: TextMaintainDirectories.java
// Author: C.K.Harvey
// Copyright (c) 2006-07 Christopher Harvey. All Rights Reserved.
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

public class TextMaintainDirectories extends HttpServlet
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

      doIt(out, req, p1, unm, sid, uty, men, den, dnm, bnm, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "TextMaintainDirectories", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6100, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String p1, String unm, String sid, String uty, String men,
                      String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');

    long startTime = new java.util.Date().getTime();

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 6100, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "TextMaintainDirectories", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6100, bytesOut[0], 0, "ACC:" + p1);
      
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "TextMaintainDirectories", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6100, bytesOut[0], 0, "SID:" + p1);
      
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }
                
    boolean canDelete;
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 6109, unm, uty, dnm, localDefnsDir, defnsDir))
      canDelete = true;
    else canDelete = false;
                
    display(con, stmt, rs, out, req, canDelete, p1, unm, sid, uty, men, den, dnm, bnm, imagesDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 6100, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
      
      if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void display(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, boolean canDelete, String option, String unm,
                         String sid, String uty, String men, String den, String dnm, String bnm, String imagesDir,
                         String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Text Library: Maintain Directories</title>");

    String cssDirectory = directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir);
            
    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + cssDirectory + "general.css\">");

    scoutln(out, bytesOut, "<script javascript=\"JavaScript\">");

    scoutln(out, bytesOut, "function insertDir(dir){var p1=sanitise(dir);");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/TextInsertDirectory?unm=" + unm + "&sid=" + sid + "&uty="
                           + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=" + option
                           + "&p1=\"+p1;}");
  
    if(canDelete)
    {
      scoutln(out, bytesOut, "function deleteDir(dir){var p1=sanitise(dir);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/TextDeleteDirectory?unm=" + unm + "&sid=" + sid + "&uty="
                             + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=" + option
                             + "&p1=\"+p1;}");
    }

    scoutln(out, bytesOut, "function list(dir,userName){var p1=sanitise(dir);");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/TextListDirectory?unm=" + unm + "&sid=" + sid + "&uty="
                           + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                           + "&p2=\"+userName+\"&p1=\"+p1;}");

    scoutln(out, bytesOut, "function sanitise(code){");
    scoutln(out, bytesOut, "var code2='';var x;var len=code.length;");
    scoutln(out, bytesOut, "for(x=0;x<len;++x)");
    scoutln(out, bytesOut, "if(code.charAt(x)=='#')code2+='%23';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='\"')code2+='%22';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='&')code2+='%26';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='%')code2+='%25';");
    scoutln(out, bytesOut, "else if(code.charAt(x)==' ')code2+='%20';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='+')code2+='%2b';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='?')code2+='%3F';");
    scoutln(out, bytesOut, "else code2+=code.charAt(x);");
    scoutln(out, bytesOut, "return code2};");
 
    scoutln(out, bytesOut, "</script>");

    int[] hmenuCount = new int[1];

    textUtils.outputPageFrame(con, stmt, rs, out, req, "TextMaintainDirectories", "6100", unm, sid, uty, men, den, dnm, bnm, "", "", localDefnsDir, defnsDir, hmenuCount, bytesOut);
     
    textUtils.drawTitle(con, stmt, rs, out, req, false, "", "Text Library Directory List", "6100", unm, sid, uty, men, den, dnm, bnm, hmenuCount, localDefnsDir, defnsDir, bytesOut);

    String dir = directoryUtils.getTextsDir(dnm);
    int directoryPrefixBitLen;

    if(! generalUtils.fileExists(dir))
      generalUtils.createDir(dir);  
    directoryPrefixBitLen = dir.length() - 1;
    
    scoutln(out, bytesOut, "<table border=0 id=\"directory\" width=100%><tr>");
      
    scoutln(out, bytesOut, "<td id=\"directoryCell\"><table width=100%><tr><td width=90%><p> &nbsp; / &nbsp; </td>");
    scoutln(out, bytesOut, "<td align=right><a href=\"javascript:insertDir('" + dir + "')\"><img src=\"" + imagesDir
                         + "insertdirectory.gif\" border=0></td></tr></table></td>");
            
    drawTable(out, canDelete, dir, 1, imagesDir, directoryPrefixBitLen, bytesOut);
    scoutln(out, bytesOut, "</tr></table></form>");

    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private int drawTable(PrintWriter out, boolean canDelete, String textDir, int depthCount, String imagesDir, int directoryPrefixBitLen, int[] bytesOut)
                        throws Exception
  {
    File path = new File(textDir);
    String fs[] = path.list();
    
    int len = fs.length;
    
    generalUtils.insertionSort(fs, len);
    
    int fileCount;
    int dirCount=0;
    for(int x=0;x<len;++x)
    {
      if(generalUtils.isDirectory(textDir + fs[x]))
      {
        ++dirCount;
        if(x > 0 && dirCount > 1)
        {
          for(int y=0;y<depthCount;++y)
          {
            if(y == (depthCount - 1))
            {    
              if(x == (fs.length - 1))
                scoutln(out, bytesOut, "<td valign=center><img src=\"" + imagesDir + "libraryl.gif\" border=0></td>");
              else                
                scoutln(out, bytesOut, "<td valign=center><img src=\"" + imagesDir + "librarybarright.gif\" border=0></td>");
            }
            else scoutln(out, bytesOut, "<td valign=center><img src=\"" + imagesDir + "librarybar.gif\" border=0></td>");
          }  
        }

        scoutln(out, bytesOut, "<td id=\"directoryCell\"><table width=100%><tr><td width=90%><p>" + fs[x]);

        scoutln(out, bytesOut, "</td><td align=right>");
        
        if(canDelete)
        {
          scoutln(out, bytesOut, "<a href=\"javascript:deleteDir('" + textDir + fs[x] + "')\"><img src=\"" + imagesDir
                               + "deletedirectory.gif\" border=0>");
        }
        
        scoutln(out, bytesOut, "<a href=\"javascript:insertDir('" + textDir + fs[x] + "')\"><img src=\"" + imagesDir
                               + "insertdirectory.gif\" border=0>");
                                    
        scoutln(out, bytesOut, "</td></tr>");
        
        if((fileCount = countFiles((textDir + fs[x] + "/"))) > 0)
        {    
          scoutln(out, bytesOut, "<tr><td><p>");
          scoutln(out, bytesOut, "<a href=\"javascript:list('" + generalUtils.sanitise2((textDir + fs[x]).substring(directoryPrefixBitLen)) + "')\">");
          
          scout(out, bytesOut, ""+fileCount);
          if(fileCount == 1)
            scout(out, bytesOut, "file");
          else scout(out, bytesOut, " files");

          scout(out, bytesOut, "</a></td></tr>");
        }
        scoutln(out, bytesOut, "</table>");
  
        if(drawTable(out, canDelete, (textDir + fs[x] + "/"), (depthCount + 1), imagesDir, directoryPrefixBitLen, bytesOut) == 0)
        {
          scoutln(out, bytesOut, "</tr><tr>");
        }
      }
    }
    
    return countDirectories(textDir);
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------
  private int countFiles(String textDir) throws Exception
  {
    File path = new File(textDir);
    String fs[] = path.list();

    int fileCount=0;
    for(int x=0;x<fs.length;++x)
    {
      if(! generalUtils.isDirectory(textDir + fs[x]))
        ++fileCount;
    }

    return fileCount;
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private int countDirectories(String textDir) throws Exception
  {
    File path = new File(textDir);
    String fs[] = path.list();

    int dirCount=0;
    for(int x=0;x<fs.length;++x)
    {
      if(generalUtils.isDirectory(textDir + fs[x]))
        ++dirCount;
    }

    return dirCount;
  }

  // -------------------------------------------------------------------------------------------------------------------------------
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

