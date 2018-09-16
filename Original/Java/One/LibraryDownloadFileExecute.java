// =======================================================================================================================================================================================================
// System: ZaraStar Library: File download
// Module: LibraryDownloadFileExecute.java
// Author: C.K.Harvey
// Copyright (c) 2006-09 Christopher Harvey. All Rights Reserved.
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

public class LibraryDownloadFileExecute extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  LibraryUtils libraryUtils = new LibraryUtils();
  AdminControlUtils adminControlUtils = new AdminControlUtils();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    String unm="", sid="", uty="", dnm="", fileName="", libDir="", isForCheckout="", docCode="", asPDF="";

    try
    {
      unm           = req.getParameter("unm");
      sid           = req.getParameter("sid");
      uty           = req.getParameter("uty");
      dnm           = req.getParameter("dnm");
      fileName      = req.getParameter("fileName");
      libDir        = req.getParameter("libDir");
      isForCheckout = req.getParameter("isForCheckout");
      docCode       = req.getParameter("docCode");
      asPDF         = req.getParameter("asPDF");
      
      if(asPDF == null) asPDF = "";
      
      doIt(req, res, unm, sid, uty, dnm, fileName, libDir, isForCheckout, docCode, asPDF);
    }
    catch(Exception e)
    {
      ;
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(HttpServletRequest req, HttpServletResponse res, String unm, String sid, String uty, String dnm, String fileName, String libDir, String isForCheckout, String docCode, String asPDF) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String workingDir    = directoryUtils.getUserDir('W', dnm, unm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(isForCheckout.equals("Y"))
    {
      if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 12003, unm, uty, dnm, localDefnsDir, defnsDir))
      {
        if(con != null) con.close();
        return;
      }
    }
    else
    {
      // anon, registered, and internal users could come here
      if(! (uty.equals("A") && adminControlUtils.notDisabled(con, stmt, rs, 825)) || (uty.equals("R") && adminControlUtils.notDisabled(con, stmt, rs, 925)))
      {
        if(! authenticationUtils.verifyAccessForLibrary(con, stmt, rs, req, docCode, unm, uty, dnm))
        {
          if(con != null) con.close();
          return;
        }
      }
    }
   
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      if(isForCheckout.equals("Y"))
        serverUtils.etotalBytes(req, unm, dnm, 12004, bytesOut[0], 0, "SID:" + docCode);
      else serverUtils.etotalBytes(req, unm, dnm, 12003, bytesOut[0], 0, "SID:" + docCode);
      if(con != null) con.close();
      return;
    }

    // if the document is a .tif AND user may have requested conversion to PDF
    
    String fullPathName;

    if(asPDF.equals("on"))
    {
      fileName = generalUtils.deSanitise(fileName);

      fullPathName = workingDir + fileName + ".pdf";

      convertToPDF(libDir + fileName, fullPathName);
      
      fileName += ".pdf";
    }
    else
    {
      fileName = generalUtils.deSanitise(fileName);
      fullPathName = libDir + fileName;
    }
    
    res.setContentType("application/x-download");
    res.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

    OutputStream out = res.getOutputStream();

    InputStream in = null;
    try 
    {
      in = new BufferedInputStream(new FileInputStream(fullPathName));
      byte[] buf = new byte[4096];
      int bytesRead;
      while((bytesRead = in.read(buf)) != -1)
      {
        out.write(buf, 0, bytesRead);
      }
    }
    catch(Exception e) 
    {
      if(in != null)
        in.close();
    }
      
    File file = new File(fullPathName);
    long fileSize = file.length(); 
    
    bytesOut[0] += (int)fileSize;

    if(isForCheckout.equals("Y"))
      libraryUtils.checkOutOrInDocument(docCode, "Out", generalUtils.todaySQLFormat(localDefnsDir, defnsDir), fileSize, dnm, localDefnsDir, defnsDir);

    libraryUtils.addToDocmove(docCode, "D", (int)fileSize, unm, dnm);
     
    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 12003, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), docCode);
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void convertToPDF(String fileName, String newFileName) throws Exception
  {
    Process p;
    BufferedReader reader;

    Runtime r = Runtime.getRuntime();
    
    String commandArray = "/Zara/Support/Scripts/Pdf/tiff2pdf.sh \"" + fileName + "\" \"" + newFileName + "\"";

    p = r.exec(commandArray);

    p.waitFor();
    System.out.println("Process exit code is: " + p.exitValue());

    InputStreamReader isr = new InputStreamReader(p.getInputStream());
    reader = new BufferedReader(isr);

    while(reader.readLine() != null) ;

    reader.close();
    isr.close();
  }
}
