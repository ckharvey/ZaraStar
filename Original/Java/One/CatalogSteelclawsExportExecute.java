// =======================================================================================================================================================================================================
// System: ZaraStar Product: Export SC catalog
// Module: CatalogSteelclawsExportExecute.java
// Author: C.K.Harvey
// Copyright (c) 2006-08 Christopher Harvey. All Rights Reserved.
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
import java.util.*;
import java.io.*;

public class CatalogSteelclawsExportExecute extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", mfr="";

    try
    {
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
        if(name.equals("mfr"))
          mfr = value[0];
      }
      
      doIt(req, res, unm, dnm, mfr, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "CatalogSteelclawsExport", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2015, bytesOut[0], 0, "ERR:" + mfr);
      if(out != null) out.flush(); 
    }
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(HttpServletRequest req, HttpServletResponse res, String unm, String dnm, String mfr, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String workingDir       = directoryUtils.getUserDir('W', dnm, unm);
    
    String userName = directoryUtils.getMySQLUserName();
    String passWord = directoryUtils.getMySQLPassWord();
    
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + userName + "&password=" + passWord);

    Statement stmt = con.createStatement(), stmt2 = null;
    ResultSet rs = null, rs2 = null;

    if(! generalUtils.isDirectory(workingDir))
      generalUtils.createDir(workingDir);
    
    RandomAccessFile fh;
    
    String fileName = "Zara_" + mfr + ".csv";

    fh = generalUtils.create(workingDir + fileName);
    
    mfr = generalUtils.deSanitise(mfr);
    
    rs = stmt.executeQuery("SELECT * FROM catalog WHERE Manufacturer = '" + mfr + "' ORDER BY Category");

    String category;
    
    while(rs.next())
    {
      category = rs.getString(2);
      
      fh.writeBytes("\"H\",\"Manufacturer\",\"Category\",\"Chapter\",\"Page\",\"PageName\",,\"ImageSmall\",\"ImageSmall2\",\"ImageLarge\",\""
                  + "Download\",\"ExternalURL\",\"Heading1\",\"Heading2\",\"Heading3\",\"Heading4\",\"Heading5\",\"Heading6\",\"Heading7\",\""
                  + "Heading8\",\"Heading9\",\"Heading10\",\"NoPrices\",\"NoAvailability\",\"CategoryLink\",\"Text\",\"Text2\",\""
                 + "OrderByDescription\"\n");

      fh.writeBytes("\"H\",");
      fh.writeBytes("\"" + mfr + "\",");
      fh.writeBytes("\"" + category + "\",");    
      fh.writeBytes("\"" + rs.getString(3) + "\",");    
      fh.writeBytes("\"" + rs.getString(4) + "\",");    
      fh.writeBytes("\"" + generalUtils.sanitise3(rs.getString(5)) + "\",");    
      fh.writeBytes("\"" + generalUtils.sanitise3(rs.getString(6)) + "\",");    
      fh.writeBytes("\"" + rs.getString(7) + "\",");    
      fh.writeBytes("\"" + rs.getString(8) + "\",");    
      fh.writeBytes("\"" + rs.getString(9) + "\",");    
      fh.writeBytes("\"" + rs.getString(10) + "\",");    
      fh.writeBytes("\"" + rs.getString(11) + "\",");    
      fh.writeBytes("\"" + generalUtils.sanitise3(rs.getString(12)) + "\",");    
      fh.writeBytes("\"" + generalUtils.sanitise3(rs.getString(13)) + "\",");    
      fh.writeBytes("\"" + generalUtils.sanitise3(rs.getString(14)) + "\",");    
      fh.writeBytes("\"" + generalUtils.sanitise3(rs.getString(15)) + "\",");    
      fh.writeBytes("\"" + generalUtils.sanitise3(rs.getString(16)) + "\",");    
      fh.writeBytes("\"" + generalUtils.sanitise3(rs.getString(17)) + "\",");    
      fh.writeBytes("\"" + generalUtils.sanitise3(rs.getString(18)) + "\",");    
      fh.writeBytes("\"" + generalUtils.sanitise3(rs.getString(19)) + "\",");    
      fh.writeBytes("\"" + generalUtils.sanitise3(rs.getString(20)) + "\",");    
      fh.writeBytes("\"" + generalUtils.sanitise3(rs.getString(21)) + "\",");    
      fh.writeBytes("\"" + rs.getString(22) + "\",");    
      fh.writeBytes("\"" + rs.getString(23) + "\",");    
      fh.writeBytes("\"" + rs.getString(24) + "\",");    
      fh.writeBytes("\"" + generalUtils.sanitise3(rs.getString(25)) + "\",");
      fh.writeBytes("\"" + generalUtils.sanitise3(rs.getString(26)) + "\",");
      fh.writeBytes("\"" + rs.getString(27) + "\"");
      
      fh.writeBytes("\n");
      
      outputLines(con, stmt2, rs2, fh, mfr, category);
    }
  
    generalUtils.fileClose(fh);
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    download(res, workingDir, fileName, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 2015, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), mfr);
    if(con != null) con.close();
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void outputLines(Connection con, Statement stmt, ResultSet rs, RandomAccessFile fh, String mfr, String category) throws Exception
  {
    fh.writeBytes("\"L\",\"Manufacturer\",\"Category\",\"ManufacturerCode\",\"\",\"\",\"\",\"\",\"\",\"\",\"Description\",\"Description2\","
                + "\"Entry1\",\"Entry2\",\"Entry3\",\"Entry4\",\"Entry5\",\"Entry6\",\"Entry7\",\"Entry8\",\"Entry9\",\"Entry10\"\n");

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT * FROM catalogl WHERE Manufacturer = '" + mfr + "' AND Category = '" + category + "' ORDER BY ManufacturerCode");

    int x;
    
    while(rs.next())
    {
      fh.writeBytes("\"L\",");
      fh.writeBytes("\"" + mfr + "\",");
      fh.writeBytes("\"" + category + "\",");    
      fh.writeBytes("\"" + generalUtils.sanitise3(rs.getString(3)) + "\",");    
      
      for(x=0;x<6;++x)
        fh.writeBytes("\"\",");    
      
      fh.writeBytes("\"" + generalUtils.sanitise3(rs.getString(14)) + "\",");    
      fh.writeBytes("\"" + generalUtils.sanitise3(rs.getString(15)) + "\",");    

      fh.writeBytes("\"" + generalUtils.sanitise3(rs.getString(4)) + "\",");    
      fh.writeBytes("\"" + generalUtils.sanitise3(rs.getString(5)) + "\",");    
      fh.writeBytes("\"" + generalUtils.sanitise3(rs.getString(6)) + "\",");    
      fh.writeBytes("\"" + generalUtils.sanitise3(rs.getString(7)) + "\",");    
      fh.writeBytes("\"" + generalUtils.sanitise3(rs.getString(8)) + "\",");    
      fh.writeBytes("\"" + generalUtils.sanitise3(rs.getString(9)) + "\",");    
      fh.writeBytes("\"" + generalUtils.sanitise3(rs.getString(10)) + "\",");    
      fh.writeBytes("\"" + generalUtils.sanitise3(rs.getString(11)) + "\",");    
      fh.writeBytes("\"" + generalUtils.sanitise3(rs.getString(12)) + "\",");    
      fh.writeBytes("\"" + generalUtils.sanitise3(rs.getString(13)) + "\"");    
      
      fh.writeBytes("\n");
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void download(HttpServletResponse res, String dirName, String fileName, int[] bytesOut) throws Exception
  {
  
    res.setContentType("application/x-download");
    res.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

    OutputStream out = res.getOutputStream();

    InputStream in = null;
    try 
    {
      in = new BufferedInputStream(new FileInputStream(dirName + fileName));
      byte[] buf = new byte[4096];
      int bytesRead;
      while((bytesRead = in.read(buf)) != -1)
        out.write(buf, 0, bytesRead);
    }
    catch(Exception e) //finally 
    {
      if(in != null)
        in.close();
    }
         
    File file = new File(dirName + fileName);
    long fileSize = file.length(); 

    bytesOut[0] += (int)fileSize;
  }

}
