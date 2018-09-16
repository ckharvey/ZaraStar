// =======================================================================================================================================================================================================
// System: ZaraStar: Library: Checkin a file
// Module: LibraryFileCheckIn.java
// Author: C.K.Harvey
// Copyright (c) 2006 Christopher Harvey. All Rights Reserved.
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

public class LibraryFileCheckIn extends HttpServlet
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "LibraryFileCheckIn", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 12005, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush(); 
    }
  }
  // -------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men,
                      String den, String dnm, String bnm, String p1, int[] bytesOut) throws Exception
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
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "LibraryFileCheckIn", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 12005, bytesOut[0], 0, "SID:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }
 
    set(con, stmt, rs, out, req, p1, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 12005, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String docCode, String unm, String sid, String uty, 
                     String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut)
                     throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Library: CheckIn File</title>");

    String cssDirectory = directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir);
            
    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + cssDirectory + "general.css\">");

    scoutln(out, bytesOut, "<script javascript=\"JavaScript\">");

    scoutln(out, bytesOut, "function checkin(docName,directory,userName){p2=sanitise(docName);p3=sanitise(directory);p4=sanitise(userName);");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/LibraryFileCheckIna?unm=" + unm + "&sid=" + sid + "&uty="
                           + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=\"+p2+\"&p3=\"+p3+\"&p4=\"+p4+\"&p1="
                           + docCode + "\";");
    scoutln(out, bytesOut, "return true;}");

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
    libraryUtils.outputPageFrame(con, stmt, rs, out, req, "LibraryFileCheckIn", "12005", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount, bytesOut);

    libraryUtils.drawTitle(con, stmt, rs, req, out, "File CheckIn", "12005", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"unm\" value='" + unm + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"sid\" value='" + sid + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"uty\" value='" + uty + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"men\" value='" + men + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"den\" value='" + den + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"dnm\" value='" + dnm + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"bnm\" value='" + bnm + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"host\" value='" + serverUtils.serverToCall("LIBRARY", localDefnsDir) + "'>");
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
        
    libraryUtils.getDocumentDetails(docCode, userName, docName, docType, inOrOut, lastDateIn, directory, projectCode, internalOrExternal,
                             archivedDate,archivedBy, fileSize, dnm, localDefnsDir, defnsDir);
    
    String libraryDir = directoryUtils.getUserDir('L', dnm, userName[0]);
    int len = libraryDir.length();
    --len; // trailing '/'
    while(len > 0 && libraryDir.charAt(len) != '/') // just-in-case
      --len;    
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"lenOfBitToStrip\" value='" + len + "'>");

    scoutln(out, bytesOut, "<table border=0 cellpadding=5 id=\"page\" width=100%>");

    scoutln(out, bytesOut, "<tr><td><p>Check-In</td><td width=90%><p><b>" + docName[0] + "</b></td></tr>");
    scoutln(out, bytesOut, "<tr><td><p>To directory </td><td><p><b>" + directory[0] + "</b></td></tr>");
    scoutln(out, bytesOut, "<tr><td><p>Owned by</td><td><p><b>" + userName[0] + "</b></td></tr>");
    scoutln(out, bytesOut, "<tr><td><p>Of type</td><td><p><b>" + docType[0] + "</b></td></tr>");
    scoutln(out, bytesOut, "<tr><td><p>And size</td><td><p><b>" + generalUtils.formatNumeric(fileSize[0], '0')
                         + " bytes</b></td></tr>");
     
    if(projectCode[0].length() == 0)
      projectCode[0] = "&lt;none&gt;";  
    
    if(internalOrExternal[0].equals("E"))
      scoutln(out, bytesOut, "<tr><td><p>Belonging to project</td><td><p><b>" + projectCode[0] + "</b></td></tr>");
    else scoutln(out, bytesOut, "<tr><td nowrap><p>Belonging to category </td><td><p><b>" + projectCode[0] + "</b></td></tr>");    
    
    scoutln(out, bytesOut, "<tr><td colspan=2><p><a href=\"javascript:checkin('" + docName[0] + "','" + directory[0] + "','"
                          + userName[0] + "')\">Check-In</a> the Document</td></tr>");
    
    scoutln(out, bytesOut, "</table></form>");     scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }  
  
  // -------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
