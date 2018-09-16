// =======================================================================================================================================================================================================
// System: ZaraStar Product: Replace Stock Records by Upload
// Module: StockRecordsUploadExecute.java
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

public class StockRecordsUploadExecute extends HttpServlet
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
        if(name.equals("p1")) // full filename of tmp file
          p1 = value[0];
      }

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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "StockRecordsUploadExecute", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3015, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm,
                    String bnm, String p1, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    
    Statement stmt = null;
    ResultSet rs   = null;
    
    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 3015, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "StockRecordsUploadExecute", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3015, bytesOut[0], 0, "ACC:" + p1);
      
      if(con != null) con.close();
     if(out != null) out.flush(); 
      return;
    }
    
    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "StockRecordsUploadExecute", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3015, bytesOut[0], 0, "SID:" + p1);
      
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    analyze(con, stmt, rs, out, req, p1, unm, sid, uty, men, den, dnm, bnm, imagesDir, localDefnsDir, defnsDir, bytesOut);
     
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "</table></form>");     scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
    File file = new File(p1);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 3015, bytesOut[0], (int)file.length(), (new java.util.Date().getTime() - startTime), p1);
    generalUtils.fileDelete(p1);
      
      if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void analyze(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String tmpFile, String unm, String sid, String uty, String men,
                       String den, String dnm, String bnm, String imagesDir, String localDefnsDir, String defnsDir, int[] bytesOut)
                       throws Exception
  {   
    scoutln(out, bytesOut, "<html><head><title>Replace Stock Records by Upload</title>");

    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir)
                         + "general.css\"></head>");

    int[] hmenuCount = new int[1];
 
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "3015", "", "StockRecordsUpload", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
                              
    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Replace Stock Records by Upload", "3015",  unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

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

    byte[] tmp       = new byte[5000]; // plenty - max rec size    
      
    if(! getNextLine(fh, tmp, 5000)) // File Enpty
    {
      scoutln(out, bytesOut, "<tr><td><p>Uploaded File Empty!</td></tr>");
      generalUtils.fileClose(fh);
      return;
    }
        
    // validate recs
    
    int count=1, errorCount=0;
    String[] fldEntry = new String[1];
    fh.seek(0L);
    
    while(getNextLine(fh, tmp, 5000))
    {
      try
      {
        switch(getEntry(tmp, 0, fldEntry))
        {
          case   -2 : 
          case   -1 : // illegal
                      scoutln(out, bytesOut, "<tr><td><p>Line " + count + " Not Legal (" + fldEntry[0] + ")</td></tr>");
                      ++errorCount;
                      break;
          default   : if(! inventory.existsItemRecGivenCode(con, stmt, rs, fldEntry[0]))
                      {
                        scoutln(out, bytesOut, "<tr><td><p>Line " + count + ": Item does not exist (" + fldEntry[0] + ")</td></tr>");
                        ++errorCount;
                      }
                      break;
        }
        
        switch(getEntry(tmp, 1, fldEntry))
        {
          case   -2 : 
          case   -1 : // illegal
                      scoutln(out, bytesOut, "<tr><td><p>Line " + count + " Not Legal (" + fldEntry[0] + ")</td></tr>");
                      ++errorCount;
                      break;
          default   : if(fldEntry[0].equals("-"))
                        ;
                      else
                      if(! inventory.existsItemRecGivenCode(con, stmt, rs, fldEntry[0]))
                      {
                        scoutln(out, bytesOut, "<tr><td><p>Line " + count + ": Item does not exist (" + fldEntry[0] + ")</td></tr>");
                        ++errorCount;
                      }
                      break;
        }
        
        ++count;
      }  
      catch(Exception e)
      {
        System.out.println(e);
        ++errorCount;
      }
    }     

    // update recs
    if(errorCount > 0)
    {
      scoutln(out, bytesOut, "<tr><td><p>Nothing Processed: " + errorCount + " Errors</td></tr>");
              generalUtils.fileClose(fh);
              return;
    }
            
    String oldItemCode, newItemCode;
    fh.seek(0L);
    
    while(getNextLine(fh, tmp, 5000))
    {
      try
      {
        getEntry(tmp, 0, fldEntry);
        oldItemCode = sanitise(fldEntry[0]);
        
        getEntry(tmp, 1, fldEntry);
        newItemCode = sanitise(fldEntry[0]);
        
        updateStockRecord(con, stmt, rs, oldItemCode, newItemCode, dnm, localDefnsDir, defnsDir);
      }  
      catch(Exception e)
      {
        System.out.println(e);
        ++errorCount;
      }
    }     
  
    generalUtils.fileClose(fh);

    messagePage.msgScreen(false, out, req, 4, unm, sid, uty, men, den, dnm, bnm, "StockRecordsUploadExecute", imagesDir, localDefnsDir, defnsDir, bytesOut);
  }

  //--------------------------------------------------------------------------------------------------------------------------------------------------
  private String sanitise(String thisEntry) throws Exception
  {
    int x=0, len = thisEntry.length();
    String s="";
    while(x < len)
    {
      if(thisEntry.charAt(x) == '\'')
        s += "''";
      else s += thisEntry.charAt(x);
       
      ++x;
    }
    
    return s;
  }

  //--------------------------------------------------------------------------------------------------------------------------------------------------
  private void updateStockRecord(Connection con, Statement stmt, ResultSet rs, String oldItemCode, String newItemCode, String dnm,
                                 String localDefnsDir, String defnsDir) throws Exception
  {
    oldItemCode = oldItemCode.toUpperCase();
    newItemCode = newItemCode.toUpperCase();

    process(con, stmt, "quotel",    oldItemCode, newItemCode);
    process(con, stmt, "sol",       oldItemCode, newItemCode);
    process(con, stmt, "ocl",       oldItemCode, newItemCode);
    process(con, stmt, "pll",       oldItemCode, newItemCode);
    process(con, stmt, "dol",       oldItemCode, newItemCode);
    process(con, stmt, "invoicel",  oldItemCode, newItemCode);
    process(con, stmt, "debitl",    oldItemCode, newItemCode);
    process(con, stmt, "creditl",   oldItemCode, newItemCode);
    process(con, stmt, "proformal", oldItemCode, newItemCode);
    process(con, stmt, "wol",       oldItemCode, newItemCode);

    process(con, stmt, "pol",       oldItemCode, newItemCode);
    process(con, stmt, "lpl",       oldItemCode, newItemCode);
    process(con, stmt, "grl",       oldItemCode, newItemCode);
    process(con, stmt, "pinvoicel", oldItemCode, newItemCode);
    process(con, stmt, "pdebitl",   oldItemCode, newItemCode);
    process(con, stmt, "pcreditl",  oldItemCode, newItemCode);

    process(con, stmt, "stocka",    oldItemCode, newItemCode);
    process(con, stmt, "stockc",    oldItemCode, newItemCode);

    // now remove old rec from stock file (also removes any stockx recs)
    inventory.stockDeleteRec(con, stmt, rs, oldItemCode, dnm, localDefnsDir, defnsDir);
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void process(Connection con, Statement stmt, String tableName, String oldItemCode, String newItemCode) throws Exception
  {
    stmt = con.createStatement();
    
    stmt.executeUpdate("UPDATE " + tableName + " SET ItemCode = '" + newItemCode + "' WHERE ItemCode = '" + oldItemCode + "'");

    if(stmt != null) stmt.close();
  }

  //--------------------------------------------------------------------------------------------------------------------------------------------------
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

  // -------------------------------------------------------------------------------------------------------------------------------------------------
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

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
