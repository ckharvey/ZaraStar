// =======================================================================================================================================================================================================
// System: ZaraStar Product: Stock Check Upload Update
// Module: StockCheckUploadUpdateExecute.java
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
import java.util.*;
import java.io.*; 

public class StockCheckUploadUpdateExecute extends HttpServlet implements SingleThreadModel
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  Inventory inventory = new Inventory();
  DocumentUtils documentUtils = new DocumentUtils();
  
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="", p3="", p4="", p5="";

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
        if(name.equals("p2")) // store
          p2 = value[0];
        else
        if(name.equals("p3")) // date
          p3 = value[0];
        else
        if(name.equals("p4")) // remark
          p4 = value[0];
        else
        if(name.equals("p5")) // type
          p5 = value[0];
      }

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, p3, p4, p5, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "StockCheckUploadUpdateExecute", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3021, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush(); 
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1, String p2, String p3, String p4, String p5, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 3021, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "StockCheckUploadUpdateExecute", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3021, bytesOut[0], 0, "ACC:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }
    
    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "StockCheckUploadUpdateExecute", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3021, bytesOut[0], 0, "SID:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(p3.length() == 0)
      scoutln(out, bytesOut, "No Date Entered");
    else
    if(! generalUtils.validateDate(false, p3, localDefnsDir, defnsDir))
      scoutln(out, bytesOut, "Invalid Date Entered");
    else 
    {
      analyze(con, stmt, rs, out, req, p1, p2, generalUtils.convertDateToSQLFormat(p3), p4, p5, unm, sid, uty, men, den, dnm, bnm, imagesDir, localDefnsDir, defnsDir, bytesOut);
    }
    
    File file = new File(p1);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 103, bytesOut[0], (int)file.length(), (new java.util.Date().getTime() - startTime), "");
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
  private void analyze(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String tmpFile, String store, String date, String remark, String type, String unm, String sid, String uty, String men,
                       String den, String dnm, String bnm, String imagesDir, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {   
    scoutln(out, bytesOut, "<html><head><title>Stock Check Upload Update</title>");

    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\"></head>");

    int[] hmenuCount = new int[1];
 
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "3021", "", "StockCheckUploadUpdate", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
         
    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Stock Check Upload Update", "3021",  unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<table id=\"page\" width=100% border=0>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    
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
    
    try
    {
      String[] fldEntry = new String[1];
    
      // check that each itemCode exists    
      // validate numeric fields
    
      int count=1, errorCount=0;
    
      while(getNextLine(fh, tmp, 5000))
      {
        switch(getEntry(tmp, 0, fldEntry))
        {
          case   -1 : // illegal
                      scoutln(out, bytesOut, "<tr><td><p>Line " + count + " Not Legal (" + fldEntry[0] + ")</td></tr>");
                      ++errorCount;
                      break;
          case   -2 :
          default   : if(! inventory.existsItemRecGivenCode(con, stmt, rs, fldEntry[0]))
                      {
                        scoutln(out, bytesOut, "<tr><td><p>Line " + count + ", Unknown ItemCode (" + fldEntry[0] + ")</td></tr>");
                        ++errorCount;
                      }  
        }

        switch(getEntry(tmp, 1, fldEntry))
        {
          case   -1 : // illegal
                      scoutln(out, bytesOut, "<tr><td><p>Line " + count + " Not Legal (" + fldEntry[0] + ")</td></tr>");
                      ++errorCount;
                      break;
          case -2 :
          default   : if(! generalUtils.isNumeric(fldEntry[0]))
                      {
                        scoutln(out, bytesOut, "<tr><td><p>Line " + count + ", Stock Level Not Legal (" + fldEntry[0] + ")</td></tr>");
                        ++errorCount;
                      }
                      break;
        }
        
        ++count;
      }  
        
      if(errorCount > 0)
      {
        scoutln(out, bytesOut, "<tr><td><p>File not processed: " + errorCount + " Errors</td></tr>");
        generalUtils.fileClose(fh);
        return;
      }

      fh.seek(0L);
    
      String itemCode, level, location;
      
      while(getNextLine(fh, tmp, 5000))
      {
        getEntry(tmp, 0, fldEntry);
        itemCode = fldEntry[0];

        getEntry(tmp, 1, fldEntry);
        level = fldEntry[0];

        if(getEntry(tmp, 2, fldEntry) < 0)
          location = "Unknown";
        else location = fldEntry[0];

        System.out.println(itemCode + " " + level);
        insert(con, stmt, rs, unm, itemCode, store, location, date, level, remark, type);
      
        ++count;
      }
    }
    catch(Exception e)
    {
      System.out.println(e);
    }     
  
    generalUtils.fileClose(fh);
    
    messagePage.msgScreen(false, out, req, 4, unm, sid, uty, men, den, dnm, bnm, "StockCheckUploadUpdateExecute", imagesDir, localDefnsDir, defnsDir, bytesOut);
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
          return -1;
        }

        if(line[x] != '\000')
          ++x; // ,
      }

      ++commaCount;
    }

    if(line[x] == '\000') // no entry exists
    {
      entry[0] = "";
      return -1;
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
      return -1;

    return 0;    
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // rtns: ' ' if rec added/updated ok
  //       'E' if adding a rec that already exists
  //       'X' other error
  private char insert(Connection con, Statement stmt, ResultSet rs, String unm, String itemCode, String store, String location, String date, String level, String remark, String type) throws Exception
  {
    try
    {
      byte[] newCode = new byte[21];
      documentUtils.getNextCode(con, stmt, rs, "stockc", true, newCode);

      stmt = con.createStatement();
           
      String q = "INSERT INTO stockc ( CheckCode, ItemCode, StoreCode, Location, Date, Level, Remark, Status, Reconciled, SignOn, Type ) VALUES ('"
               + generalUtils.stringFromBytes(newCode, 0L) + "','" + itemCode + "','" + generalUtils.sanitiseForSQL(store) + "','" + generalUtils.sanitiseForSQL(location) + "',{d '" + date + "'},'" + level + "','" + generalUtils.sanitiseForSQL(remark) + "','L','N','" + unm
               + "','" + type + "')";

      stmt.executeUpdate(q);
        
      if(stmt != null) stmt.close();

      return ' ';
    }
    catch(Exception e)
    {
      System.out.println("3021a: " + e);
      if(stmt != null) stmt.close();
    }
    
    return 'X';
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
