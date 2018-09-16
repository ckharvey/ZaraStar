// =======================================================================================================================================================================================================
// System: ZaraStar Product: Add Stock Records by Upload
// Module: ProductStockRecordsUploadAddExecute.java
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

import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;
import java.util.*;
import java.io.*; 

public class ProductStockRecordsUploadAddExecute extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  Inventory inventory = new Inventory();
  AccountsUtils accountsUtils = new AccountsUtils();
  DocumentUtils documentUtils = new DocumentUtils();
  
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "ProductStockRecordsUploadAddExecute", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3022, bytesOut[0], 0, "ERR:" + p1);
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
    
    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 3022, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "ProductStockRecordsUploadAddExecute", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3022, bytesOut[0], 0, "ACC:" + p1);
       
      if(con != null) con.close();
    if(out != null) out.flush(); 
      return;
    }
    
    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "ProductStockRecordsUploadAddExecute", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3022, bytesOut[0], 0, "SID:" + p1);
      
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

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 3022, (int)file.length(), 0, (new java.util.Date().getTime() - startTime), p1);
    generalUtils.fileDelete(p1);
      
      if(con != null) con.close();

    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void analyze(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String tmpFile, String unm, String sid, String uty, String men,
                       String den, String dnm, String bnm, String imagesDir, String localDefnsDir, String defnsDir, int[] bytesOut)
                       throws Exception
  {   
    scoutln(out, bytesOut, "<html><head><title>Add Stock Records by Upload</title>");

    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir)
                         + "general.css\"></head>");

    int[] hmenuCount = new int[1];
 
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "3022", "", "ProductStockRecordsUploadAdd", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Add Stock Records by Upload", "3022",  unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

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
        
    // validate recs
    
    boolean error;
    String mfr="", mfrCode="", desc="", desc2;
    int count=1;
    int[] errorCount = new int[1]; errorCount[0] = 0; 
    String[] fldEntry = new String[1];
    fh.seek(0L);
    
    while(getNextLine(fh, tmp, 5000))
    {
      try
      {
        error = false;
        switch(getEntry(tmp, 0, fldEntry))
        {
          case -888 : // premature end of imported flds ???
          case   -1 : // illegal
                      scoutln(out, bytesOut, "<tr><td><p>Line " + count + " Not Legal (" + fldEntry[0] + ")</td></tr>");
                      ++errorCount[0];
                      error = true;
                      break;
          default   : mfr = fldEntry[0];
                      break;
        }
        
        if(! error)
        {
          switch(getEntry(tmp, 1, fldEntry))
          {
            case -2 :
            case   -1 : // illegal
                        scoutln(out, bytesOut, "<tr><td><p>Line " + count + " Not Legal (" + fldEntry[0] + ")</td></tr>");
                        ++errorCount[0];
                        error = true;
                        break;
            default   : mfrCode = fldEntry[0];
                        break;
          }
        }
        
        if(! error)
        {
          switch(getEntry(tmp, 2, fldEntry))
          {
            case -2 :
            case   -1 : // illegal
                        scoutln(out, bytesOut, "<tr><td><p>Line " + count + " Not Legal (" + fldEntry[0] + ")</td></tr>");
                        ++errorCount[0];
                        error = true;
                        break;
            default   : desc = fldEntry[0];
                        break;
          }
        }
        
        if(! error)
        {
          if(inventory.existsItemRecGivenMfrAndMfrCode(con, stmt, rs, mfr, mfrCode))
          {
            scoutln(out, bytesOut, "<tr><td><p>Line " + count + ": Item already exists (" + mfr + " " + mfrCode + ")</td></tr>");
//            ++errorCount[0];
          }
        }
        
        ++count;
      }  
      catch(Exception e)
      {
        System.out.println(e);
        ++errorCount[0];
      }
    }     

    // update recs
    if(errorCount[0] > 0)
    {
      scoutln(out, bytesOut, "<tr><td><p>Nothing Processed: " + errorCount[0] + " Errors</td></tr>");
      generalUtils.fileClose(fh);
      return;
    }
            
    int[] addedCount = new int[1]; addedCount[0] = 0;
        
    fh.seek(0L);
    
    while(getNextLine(fh, tmp, 5000))
    {
      try
      {
        getEntry(tmp, 0, fldEntry);
        mfr = fldEntry[0];
        
        getEntry(tmp, 1, fldEntry);
        mfrCode = fldEntry[0];
        
        getEntry(tmp, 2, fldEntry);
        desc = fldEntry[0];
        
        getEntry(tmp, 3, fldEntry);
        desc2 = fldEntry[0];
        
        update(con, stmt, rs, out, mfr, mfrCode, desc, desc2, addedCount, errorCount, unm, dnm, localDefnsDir, defnsDir, bytesOut);
      }   
      catch(Exception e)
      {
        System.out.println(e);
        ++errorCount[0];
      }
    }     

    scoutln(out, bytesOut, "<tr><td><p>Records Added: " + addedCount[0] + "</td></tr>");
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
  private void update(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String mfr, String mfrCode, String desc, String desc2, int[] addedCount, int[] errorCount, String unm,
                      String dnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    String baseCurrency = accountsUtils.getBaseCurrency(con, stmt, rs, dnm, localDefnsDir, defnsDir);

    byte[] data     = new byte[3000];

    if(mfr.length() == 0 && mfrCode.length() == 0) // just-in-case
    {  
      ++errorCount[0];
      return;
    }
    
    byte[] mfrB = new byte[51];
    generalUtils.strToBytes(mfrB, mfr);

    String newItemCode;

    byte[] nextCode = new byte[21];
    documentUtils.getNextCode(con, stmt, rs, "stock", true, nextCode);
    newItemCode = generalUtils.stringFromBytes(nextCode, 0L);

    generalUtils.repAlpha(data, 3000, (short)0, newItemCode);

    generalUtils.repAlpha(data, 3000, (short)1,  desc);
    generalUtils.repAlpha(data, 3000, (short)2,  desc2);
    generalUtils.repAlpha(data, 3000, (short)3,  mfr);
    generalUtils.repAlpha(data, 3000, (short)4,  mfrCode);
    generalUtils.repAlpha(data, 3000, (short)34, unm);
    generalUtils.repAlpha(data, 3000, (short)32, "1970-01-01");
    generalUtils.repAlpha(data, 3000, (short)33, "1970-01-01");
    generalUtils.repAlpha(data, 3000, (short)45, "1970-01-01");
    generalUtils.repAlpha(data, 3000, (short)50, generalUtils.todaySQLFormat(localDefnsDir, defnsDir));
    generalUtils.repAlpha(data, 3000, (short)36, generalUtils.timeNow(2, ""));

    generalUtils.repAlpha(data, 3000, (short)19, baseCurrency);
    generalUtils.repAlpha(data, 3000, (short)52, baseCurrency);

    generalUtils.repAlpha(data, 3000, (short)14, "0.0");
    generalUtils.repAlpha(data, 3000, (short)15, "0.0");
    generalUtils.repAlpha(data, 3000, (short)16, "0.0");
    generalUtils.repAlpha(data, 3000, (short)18, "0.0");
    generalUtils.repAlpha(data, 3000, (short)20, "0.0");
    generalUtils.repAlpha(data, 3000, (short)21, "0.0");
    generalUtils.repAlpha(data, 3000, (short)22, "0.0");
    generalUtils.repAlpha(data, 3000, (short)23, "0.0");
    generalUtils.repAlpha(data, 3000, (short)24, "0.0");
    generalUtils.repAlpha(data, 3000, (short)25, "0.0");
    generalUtils.repAlpha(data, 3000, (short)26, "0.0");
    generalUtils.repAlpha(data, 3000, (short)31, "0.0");
    
    generalUtils.repAlpha(data, 3000, (short)38, "0.0");
    generalUtils.repAlpha(data, 3000, (short)39, "0.0");
    generalUtils.repAlpha(data, 3000, (short)40, "0.0");
    generalUtils.repAlpha(data, 3000, (short)41, "0.0");
    generalUtils.repAlpha(data, 3000, (short)44, "0.0");
    generalUtils.repAlpha(data, 3000, (short)46, "0.0");

    generalUtils.repAlpha(data, 3000, (short)47, "0");
    generalUtils.repAlpha(data, 3000, (short)55, "0");

    generalUtils.repAlpha(data, 3000, (short)56, "0");
    generalUtils.repAlpha(data, 3000, (short)57, "0");
    generalUtils.repAlpha(data, 3000, (short)58, "1970-01-01");    
    
    if(! inventory.putStockRecGivenCode(con, stmt, rs, '\000', newItemCode, 'N', data, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "<tr><td><p>*** Not Added: " + newItemCode + ": " + mfr + " " + mfrCode + " " + desc + " " + desc2 + "</td></tr>");
      ++errorCount[0];
    }
    else ++addedCount[0];
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
