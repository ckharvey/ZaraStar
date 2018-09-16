// ========================================================================================================================================================================================================
// System: ZaraStar LibraryEngine: Upload from Liberator
// Module: LibraryUploadLiberator.java
// Author: C.K.Harvey
// Copyright (c) 2002-09 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
//
// ========================================================================================================================================================================================================

package org.zarastar.zarastar;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.sql.*;

public class LibraryUploadLiberator extends HttpServlet
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

    String unm="", uty="", dnm="", p1="", p2="";

    try
    {
      res.setContentType("text/html");
      out = res.getWriter();

      unm = req.getParameter("unm");
      uty = req.getParameter("uty");
      dnm = req.getParameter("dnm");
      p1  = req.getParameter("p1"); // destFileName
      p2  = req.getParameter("p2"); // len of directory name to ignore

      doIt(out, req, unm, uty, dnm, p1, p2, bytesOut);
    }
    catch(Exception e)
    {
      serverUtils.etotalBytes(req, unm, dnm, 12012, bytesOut[0], 0, "ERR:" +p1);
      out.println("ERR");
    }
  }

  // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String uty, String dnm, String p1, String p2, int[] bytesOut) throws Exception
  {
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String libraryDir       = directoryUtils.getUserDir('L', dnm, unm);

    long startTime = new java.util.Date().getTime();

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 12012, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      out.println("ERR");
      serverUtils.etotalBytes(req, unm, dnm, 12012, bytesOut[0], 0, "ACC:" + p1);
    if(con  != null) con.close();
      if(out != null) out.flush(); 
      return;
    }
    
    p1 = p1.substring(generalUtils.intFromStr(p2));
    
    // p1 is the full path name
    String libraryPlusFullPathName = libraryDir + p1.substring(1);
     
    int x = p1.length();
    --x;
    while(x > 0 && p1.charAt(x) != '/') // just-in-case
      --x;
    
    String directoryBit = p1.substring(0, x);
    String fileNameBit  = p1.substring(x+1);

    File newDir = new File(libraryDir + directoryBit);
    newDir.mkdirs();

    if(update(unm, dnm, req, fileNameBit, directoryBit, libraryPlusFullPathName, localDefnsDir, defnsDir))
      out.println("OK");
    else out.println("ERR");

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 12012, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p2);
    if(con  != null) con.close();
    if(out != null) out.flush(); 
  }

  // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean update(String unm, String dnm, HttpServletRequest req, String fileNameBit, String directoryBit, String libraryPlusFullPathName, String localDefnsDir, String defnsDir)
  {
    try
    {
      ServletInputStream in = req.getInputStream();
      if(in == null)
        throw new IOException();

      DataInputStream dis = new DataInputStream(in);
      if(dis == null)
      {
        in.close();
        throw new IOException();
      }

      RandomAccessFile fd;
      if((fd = generalUtils.create(libraryPlusFullPathName)) == null)
      {
        in.close();
        dis.close();
        throw new IOException();
      }

      byte[] b = new byte[1021];
      int red = dis.read(b, 0, 1020);

      int x;
      long total = red;
      while(red > 0)
      {
        for(x=0;x<red;++x)
          fd.writeByte(b[x]);

        red = dis.read(b, 0, 1020);
        total += red;
      }

      for(x=0;x<red;++x)
        fd.writeByte(b[x]);

      generalUtils.fileClose(fd);

      dis.close();

      in.close();

      System.out.println(" ...transferred");

      updateDB(unm, fileNameBit, directoryBit, libraryPlusFullPathName, dnm, localDefnsDir, defnsDir);
    }
    catch(Exception e) { System.out.println("Exception " + e); }

    return true;
  }

  // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void updateDB(String userName, String fileNameBit, String directoryBit, String libraryPlusFullPathName, String dnm, String localDefnsDir, String defnsDir) throws Exception
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

    if(! libraryUtils.getDocumentDetailsUsingFileAndDirAndUserNames(fileNameBit, directoryBit, userName, docCode, docType, inOrOut, lastDateIn, projectCode, internalOrExternal, archivedDate, archivedBy, fileSize, dnm, localDefnsDir, defnsDir))
    {        
      File file = new File(libraryPlusFullPathName);
      long fileSizeL = file.length(); 
  
      int y =fileNameBit.length();
      --y;
      while(y > 0 && fileNameBit.charAt(y) != '.')
        --y;
      String extn;
      if(y == 0)
        extn = "";
      else extn = (fileNameBit.substring(++y)).toUpperCase();
              
      if(libraryUtils.addToDocumentsAndPermissions(userName, fileNameBit, extn, "I", generalUtils.todaySQLFormat(localDefnsDir, defnsDir), directoryBit, "", "I", "1970:01:01", "", fileSizeL, dnm, localDefnsDir, defnsDir).length() > 0)
      {
        System.out.println("... updated");
      }
      else System.out.println("Failed to add " + libraryPlusFullPathName);
    }
    else // already exists; so update file length
    {
     ;   
        
    }        
  }
  
}
