// =======================================================================================================================================================================================================
// System: ZaraStar Product: Stock Location Upload Update
// Module: ProductStockUploadLocationUpdate.java
// Author: C.K.Harvey
// Copyright (c) 2006-12 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;
import java.util.*;
import java.io.*; 

public class ProductStockUploadLocationUpdate extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  Inventory inventory = new Inventory();
  
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2 = "";

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
        if(name.equals("p1")) // full filename of tmp file
          p1 = value[0];
        else
        if(name.equals("store")) // updating stockX
          p2 = value[0];
      }

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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "ProductStockUploadLocationUpdate", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3088, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1, String p2, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
     
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 3088, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "ProductStockUploadLocationUpdate", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3088, bytesOut[0], 0, "ACC:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }
    
    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "ProductStockUploadLocationUpdate", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3088, bytesOut[0], 0, "SID:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    analyze(con, stmt, rs, out, req, p1, p2, unm, sid, uty, men, den, dnm, bnm, imagesDir, localDefnsDir, defnsDir, bytesOut);
     
    File file = new File(p1);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 3088, bytesOut[0], (int)file.length(), (new java.util.Date().getTime() - startTime), p1);
    generalUtils.fileDelete(p1);

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void analyze(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String tmpFile, String p2, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String imagesDir,
                       String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {   
    scoutln(out, bytesOut, "<html><head><title>Stock Location Upload Update</title>");

    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\"></head>");

    int[] hmenuCount = new int[1];
 
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "3088", "", "ProductStockUploadLocation", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Stock Location Upload Update", "3088",  unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<table id=\"page\" width=100% border=0>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    
    // ensure that first line of file has legal field names

    RandomAccessFile fh;
    if((fh = generalUtils.fileOpen(tmpFile)) == null) // just-in-case: file not found
    {
      scoutln(out, bytesOut, "<tr><td><p>Uploaded File Not Found</td></tr>");
      generalUtils.fileClose(fh);
      return;
    }

    byte[] tmp = new byte[5000]; // plenty - max rec size    
      
    if(! getNextLine(fh, tmp, 5000)) // File Enpty
    {
      scoutln(out, bytesOut, "<tr><td><p>Uploaded File Empty!</td></tr>");
      generalUtils.fileClose(fh);
      return;
    }
    
    String[] fldEntry = new String[1];
    
    String itemCode = "", location = "";
    int x, count = 1;

    while(getNextLine(fh, tmp, 5000))
    {
      try
      {
        x = 0;
        while(x < 2)
        {
          switch(getEntry(tmp, x, fldEntry))
          {
            case   -1 : // illegal
                        scoutln(out, bytesOut, "<tr><td><p>Line " + count + " Not Legal (" + fldEntry[0] + ")</td></tr>");
                        generalUtils.fileClose(fh);
                        return;
            case   -2 : // premature end of imported flds ???
                        scoutln(out, bytesOut, "<tr><td><p>Premature End</td></tr>");
                        generalUtils.fileClose(fh);
                        return;
            default  :  if(x == 0)
                          itemCode = fldEntry[0];
                        else
                        if(x == 1)
                          location = fldEntry[0];
                        break;
          }

          ++x;
        }

        ++count;
        
        updateStockXRecord(itemCode, p2, location, dnm);
      }  
      catch(Exception e)
      {
        System.out.println(e);
      }
    }     
  
    generalUtils.fileClose(fh);

    messagePage.msgScreen(false, out, req, 4, unm, sid, uty, men, den, dnm, bnm, "ProductStockUploadLocationUpdate", imagesDir, localDefnsDir, defnsDir, bytesOut);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean getNextLine(RandomAccessFile fh, byte[] buf, int bufSize) throws Exception
  {
    int x=0, red=0;
    boolean inQuote = false;

    long curr = fh.getFilePointer();
    long high = fh.length();

    fh.seek(curr);

    if(curr == high)
      return false;

    fh.read(buf, 0, 1);
    while(curr < high && x < (bufSize - 1))
    {
      if(buf[x] == '"')
      {
        if(inQuote)
          inQuote = false;
        else inQuote = true;
      }
   
      if((buf[x] == 10 || buf[x] == 13 || buf[x] == 26) && ! inQuote)
      {
        while(buf[x] == 10 || buf[x] == 13 || buf[x] == 26)
        {
          red = fh.read(buf, x, 1);
          if(red < 0)
            break;
        }

        if(buf[x] == 26)
          ;
        else
        if(red > 0)
          fh.seek(fh.getFilePointer() -1);

        buf[x] = '\000';

        return true;
      }

      ++x;
      fh.read(buf, x, 1);
      ++curr;
    }

    // remove trailing spaces
    x = bufSize - 1;
    while(buf[x] == 32)
      --x;
    buf[++x] = '\000';

    return true;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // entryReqd is origin-0
  // rtns: -1 no entry
  //       -2 no closing
  private int getEntry(byte[] line, int entryReqd, String[] entry)
  {
    int x=0, commaCount=0;

    entry[0] = "";
    while(commaCount < entryReqd)
    {
      if(line[x] == '"')
      {
        ++x; // "
        while(line[x] != '\000' && line[x] != '"') // scan for the closing quotes
          ++x;

        if(line[x] == '\000') // no closing
          return -2;

        ++x; // "

        if(line[x] != '\000')
          ++x; // ,
      }
      else
      {
        while(line[x] != '\000' && line[x] != ',')
          ++x;

        if(line[x] == '\000') // no entry
        {
          entry[0] = "";
          return 0;//-1;
        }

        if(line[x] != '\000')
          ++x; // ,
      }

      ++commaCount;
    }

    if(line[x] == '\000') // no entry exists... ok it's a zero-length entry
    {
      entry[0] = "";
      return 0; //-1;
    }

    if(line[x] == '"')
    {
      ++x; // "
      while(line[x] != '\000' && line[x] != '"') // scan for the closing quotes
        entry[0] += (char)line[x++];

      if(line[x] == '\000') // no closing
        return -2;

      return 0;
    }
    // else
    
    while(line[x] != '\000' && line[x] != ',')
      entry[0] += (char)line[x++];

    if(entry[0].length() == 0) // no entry
      return 0;//-1;

    return 0;    
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void updateStockXRecord(String itemCode, String store, String location, String dnm) throws Exception
  {
    String userName = directoryUtils.getMySQLUserName();
    String passWord = directoryUtils.getMySQLPassWord();

    Class.forName("com.mysql.jdbc.Driver").newInstance();

    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + userName + "&password=" + passWord);

    // check if rec exists, if it does, update it else create it anew

    Statement stmt = con.createStatement();

    ResultSet rs = stmt.executeQuery("SELECT * FROM stockx WHERE ItemCode = '" + generalUtils.sanitiseForSQL(itemCode) + "' AND Store = '" + generalUtils.sanitiseForSQL(store) + "'");

    boolean exists = false;

    if(rs.next())
      exists = true;

    if(stmt != null) stmt.close();

    if(location.length() > 10)
      location = location.substring(0, 10);

    if(exists)
    {
      stmt = con.createStatement();

      stmt.executeUpdate("UPDATE stockx SET Location = '" + generalUtils.sanitiseForSQL(location) + "', DateLastModified = NULL WHERE ItemCode = '" + generalUtils.sanitiseForSQL(itemCode) + "' AND Store = '" + generalUtils.sanitiseForSQL(store) + "'");

      if(stmt != null) stmt.close();
    }
    else
    {
      stmt = con.createStatement();

      stmt.executeUpdate("INSERT INTO stockx (ItemCode, Store, Location) VALUES ('" + generalUtils.sanitiseForSQL(itemCode) + "', '" + generalUtils.sanitiseForSQL(store) + "', '" + generalUtils.sanitiseForSQL(location) + "')");

      if(stmt != null) stmt.close();
    }

    if(con  != null) con.close();
  }

}
