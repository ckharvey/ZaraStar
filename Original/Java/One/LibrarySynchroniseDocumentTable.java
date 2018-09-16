// =======================================================================================================================================================================================================
// System: ZaraStar LibraryEngine: Synchronize Document Table itself
// Module: LibrarySynchroniseDocumentTable.java
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
import java.util.Enumeration;

public class LibrarySynchroniseDocumentTable extends HttpServlet
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

      Enumeration en = req.getParameterNames();
      while(en.hasMoreElements())
      {
        String name = (String)en.nextElement();
        String[] value = req.getParameterValues(name);
        if(name.equals("unm"))
          unm = value[0];
        else
        if(name.equals("sid"))
          sid = value[0];
        else
        if(name.equals("uty"))
          uty = value[0];
        else
        if(name.equals("men"))
          men = value[0];
        else
        if(name.equals("den"))
          den = value[0];
        else
        if(name.equals("dnm"))
          dnm = value[0];
        else
        if(name.equals("bnm"))
          bnm = value[0];
        else
        if(name.equals("userName"))
          p1 = value[0];
      }
   
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "LibrarySynchroniseDocumentTable", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 12011, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String p1, String unm, String sid, String uty,
                      String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;
    
    if(serverUtils.isSysAdmin(con, stmt, rs, unm, dnm, sid, uty, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "LibrarySynchroniseDocumentTable", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 12011, bytesOut[0], 0, "ACC:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "LibrarySynchroniseDocumentTable", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 12011, bytesOut[0], 0, "SID:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    display(con, stmt, rs, out, req, p1, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);
    
    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 12011, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void display(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String userName, String unm, String sid, String uty,
                         String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut)
                         throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Library: Synchronize Document Table</title>");

    String cssDirectory = directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir);
            
    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + cssDirectory + "general.css\">");

    int[] hmenuCount = new int[1];
    libraryUtils.outputPageFrame(con, stmt, rs, out, req, "LibrarySynchroniseDocumentTable", "12011", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount, bytesOut);
 
    libraryUtils.drawTitle(con, stmt, rs, req, out, "Synchronize Documents DataBase", "12011", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    String dir;
    int prefixBitLen;
    
    if(userName.equals("___REFERENCE___"))
    {
      dir = directoryUtils.getReferenceLibraryDir(dnm);
      if(! generalUtils.fileExists(dir))
        generalUtils.createDir(dir);  
      prefixBitLen = dir.length() - 1;
    }
    else
    {
      dir = directoryUtils.getUserDir('L', dnm, userName);
      if(! generalUtils.fileExists(dir))
        generalUtils.createDir(dir);  
      prefixBitLen = (directoryUtils.getUserDir(dnm) + "/" + userName + "/Library").length();  
    }
        
    scoutln(out, bytesOut, "<table border=0 id=\"page\" width=100%><tr>");

    String[] errStr = new String[1]; errStr[0]="";
    
    int numRecsAdded = process(userName, dir, prefixBitLen, dnm, localDefnsDir, defnsDir, errStr);
    
    if(userName.equals("___REFERENCE___"))
      userName = "the Reference Library";
    
    scoutln(out, bytesOut, "<tr><td><p><br><br>A total of " + numRecsAdded +  " records have been added for " + userName + "</td></tr>");

    if(errStr[0].length() > 0)
      scoutln(out, bytesOut, "<tr><td><p><br><br>The following records could not be added" + errStr[0] +  "</td></tr>");
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "</table></form>");     scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private int process(String userName, String libraryDir, int prefixBitLen, String dnm, String localDefnsDir, String defnsDir, String[] errStr)
                      throws Exception
  {
    File path = new File(libraryDir);
    String fs[] = new String[0];
    fs = path.list();
    
    File file;
    long fileSizeL;
    String extn, directory;
    
    int y, fileCount=0;
    for(int x=0;x<fs.length;++x)
    {
      if(! generalUtils.isDirectory(libraryDir + fs[x]))
      {
        String[] docCode            = new String[1];
        String[] docType            = new String[1];
        String[] inOrOut            = new String[1];
        String[] lastDateIn         = new String[1];
        String[] projectCode        = new String[1];
        String[] internalOrExternal = new String[1];
        String[] archivedDate       = new String[1];
        String[] archivedBy         = new String[1];
        String[] fileSize           = new String[1];          
     
        directory = libraryDir.substring(prefixBitLen);
        if(directory.endsWith("/"))
          directory = directory.substring(0, directory.length() - 1);

        if(! libraryUtils.getDocumentDetailsUsingFileAndDirAndUserNames(fs[x], directory, userName, docCode, docType, inOrOut, lastDateIn,
                                                                 projectCode, internalOrExternal, archivedDate, archivedBy, fileSize, 
                                                                 dnm, localDefnsDir, defnsDir))
        {        
          file = new File(libraryDir + fs[x]);
          fileSizeL = file.length(); 
  
          y = fs[x].length();
          --y;
          while(y > 0 && fs[x].charAt(y) != '.')
            --y;
          if(y == 0)
            extn = "";
          else extn = (fs[x].substring(++y)).toUpperCase();
              
          if(libraryUtils.addToDocumentsAndPermissions(userName, fs[x], extn, "I", generalUtils.todaySQLFormat(localDefnsDir, defnsDir), 
                                                directory, "", "I", "1970:01:01", "", fileSizeL, dnm, localDefnsDir, defnsDir).length() > 0)
          {
            ++fileCount;
          }
          else errStr[0] += ("<br>Failed to add " + directory + fs[x]);
        }
      }
      else fileCount += process(userName, (libraryDir + fs[x] + "/"), prefixBitLen, dnm, localDefnsDir, defnsDir, errStr);
    }

    return fileCount;
  }
 
  // -------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
