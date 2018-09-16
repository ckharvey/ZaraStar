// =======================================================================================================================================================================================================
// System: ZaraStar Product: Stock File Upload Update
// Module: StockFileUploadExecute.java
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

public class StockFileUploadExecute extends HttpServlet
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "StockFileUploadExecute", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3058, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush(); 
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
     
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 3058, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "StockFileUploadExecute", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3058, bytesOut[0], 0, "ACC:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }
    
    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "StockFileUploadExecute", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3058, bytesOut[0], 0, "SID:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    analyze(con, stmt, rs, out, req, p1, unm, sid, uty, men, den, dnm, bnm, imagesDir, localDefnsDir, defnsDir, bytesOut);
     
    File file = new File(p1);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 3058, bytesOut[0], (int)file.length(), (new java.util.Date().getTime() - startTime), p1);

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void analyze(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String tmpFile, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String imagesDir,
                       String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {   
    scoutln(out, bytesOut, "<html><head><title>Stock DataBase Upload Update</title>");

    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\"></head>");

    int[] hmenuCount = new int[1];
 
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "3058", "", "StockFileUploadUpdate", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Stock DataBase Upload Update", "3058",  unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

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
    
    String[] fieldNamesSupplied = new String[70]; // plenty
    int[]    fieldNumsOnList    = new int[70]; // plenty
    String fieldNamesStock = inventory.getFieldNamesStock();
    String fieldTypesStock = inventory.getFieldTypesStock();
    
    short[] fieldSizes = new short[200]; // plenty
    inventory.getFieldSizesStock(fieldSizes);

    int errorCount=0, fieldNumOnList, numFields=0, fieldNamesStockLen = fieldNamesStock.length();
    String[] fldEntry = new String[1];
    
    while(getEntry(tmp, numFields, fldEntry) == 0 && fldEntry[0].length() > 0)
    {
      fieldNumOnList = fieldIsValid(fldEntry[0], fieldNamesStock, fieldNamesStockLen);
      if(fieldNumOnList == -1) // illegal field name supplied
      {
        scoutln(out, bytesOut, "<tr><td><p>Illegal Field Name: " + fldEntry[0] + "</td></tr>");
        ++errorCount;
      }
      
      fieldNamesSupplied[numFields] = fldEntry[0];      
      fieldNumsOnList[numFields++] = fieldNumOnList;      
    }  
      
    if(errorCount > 0)
    {
      scoutln(out, bytesOut, "<tr><td><p>File not processed: " + errorCount + " Errors</td></tr>");
      generalUtils.fileClose(fh);
      return;
    }
    
    // check that each itemCode exists    
    // validate numeric fields
    // validate field lengths ???
    
    String itemCode="";
    String[] updateStr = new String[1];
    char thisFieldTypesStock;
    int x, count=1, thisLineErrorCount;
    boolean first;

    while(getNextLine(fh, tmp, 5000))
    {
      try
      {
        thisLineErrorCount = 0;
        first = true;
        updateStr[0] = "SET";
        x = 0;
        while(x < numFields)
        {
          switch(getEntry(tmp, x, fldEntry))
          {
            case   -1 : // illegal
                      scoutln(out, bytesOut, "<tr><td><p>Line " + count + " Not Legal (" + fldEntry[0] + ")</td></tr>");
                      generalUtils.fileClose(fh);
                      return;
            case   -2 : // premature end of imported flds ???
            default   : thisFieldTypesStock = fieldTypesStock.charAt(fieldNumsOnList[x]);

                        if(thisFieldTypesStock == 'D') // DATE
                        {
                          if(fldEntry[0].length() == 0 || ! generalUtils.validateDateSQL(false, fldEntry[0], localDefnsDir, defnsDir))
                          {
                            scoutln(out, bytesOut, "<tr><td><p>Line " + count + ", field " + x + ": Date Not Legal (" + fldEntry[0] + ")</td></tr>");
                            ++errorCount;
                            ++thisLineErrorCount;
                          }  
                          else first = appendToUpdateStr(first, fieldNamesSupplied[x], fldEntry[0], updateStr);
                        }
                        else
                        if(thisFieldTypesStock == 'I') // INTEGER
                        {
                          if(fldEntry[0].length() == 0 || ! generalUtils.isInteger(fldEntry[0]))
                          {
                            scoutln(out, bytesOut, "<tr><td><p>Line " + count + ", field " + x + ": Integer Not Legal (" + fldEntry[0] + ")</td></tr>");
                            ++errorCount;
                            ++thisLineErrorCount;
                          }  
                          else first = appendToUpdateStr(first, fieldNamesSupplied[x], fldEntry[0], updateStr);
                        }  
                        else
                        if(thisFieldTypesStock == 'F') // FLOAT (DECIMAL)
                        {
                          if(fldEntry[0].length() == 0 || ! generalUtils.isNumeric(fldEntry[0]))
                          {
                            scoutln(out, bytesOut, "<tr><td><p>Line " + count + ", field " + x + ": Float Not Legal (" + fldEntry[0] + ")</td></tr>");
                            ++errorCount;
                            ++thisLineErrorCount;
                          }
                          else first = appendToUpdateStr(first, fieldNamesSupplied[x], fldEntry[0], updateStr);
                        }
                        else // fieldType is CHAR
                        {
                          if(fldEntry[0].length() > fieldSizes[fieldNumsOnList[x]])
                          {
                            fldEntry[0] = fldEntry[0].substring(0, fieldSizes[fieldNumsOnList[x]]);
                          }

                          if(fieldNumsOnList[x] == 0) // itemCode
                          {
                            itemCode = fldEntry[0]; 
                          }
                          else 
                          {
                            fldEntry[0] = sanitise(fldEntry[0]);
                            first = appendToUpdateStr(first, fieldNamesSupplied[x], fldEntry[0], updateStr);
                          }
                        }
          }
      
          ++x;
        }
        
        if(thisLineErrorCount == 0)
        {
          updateStockRecord(itemCode, updateStr[0], dnm);
        }
      
        ++count;
      }  
      catch(Exception e)
      {
        System.out.println(e);
        ++errorCount;
      }
    }     
  
    generalUtils.fileClose(fh);

    if(errorCount > 0)
    {
      scoutln(out, bytesOut, "<tr><td><p>Some records not processed: " + errorCount + " Errors</td></tr>");
      return;
    }
    
    messagePage.msgScreen(false, out, req, 4, unm, sid, uty, men, den, dnm, bnm, "StockFileUploadExecute", imagesDir, localDefnsDir, defnsDir, bytesOut);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean appendToUpdateStr(boolean first, String thisFieldName, String thisEntry, String[] updateStr) throws Exception
  {
    if(! first)
      updateStr[0] += ",";
    else first = false;
    
    updateStr[0] += (" " + thisFieldName + " = '" + thisEntry + "'"); 
    
    return first;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
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

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void updateStockRecord(String itemCode, String updateStr, String dnm) throws Exception
  {
    Connection con = null;
    Statement stmt = null;

    try
    {
      String userName = directoryUtils.getMySQLUserName();
      String passWord = directoryUtils.getMySQLPassWord();
    
      Class.forName("com.mysql.jdbc.Driver").newInstance();
    
      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + userName + "&password=" + passWord);
      stmt = con.createStatement();
    
      stmt.executeUpdate("UPDATE stock " + updateStr + ", DateLastModified = NULL WHERE ItemCode = '" + generalUtils.sanitiseForSQL(itemCode) + "'");
    }
    catch(Exception e)
    {
      System.out.println("ERROR: UPDATE stock " + updateStr + ", DateLastModified = NULL WHERE ItemCode = '" + itemCode + "'");
    }      
     
    if(con  != null) con.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int fieldIsValid(String fieldName, String fieldNamesStock, int fieldNamesStockLen) throws Exception
  {
    int x=0, count=0;
    String field;
    
    while(x < fieldNamesStockLen)
    {
      field="";
      while(x < fieldNamesStockLen && fieldNamesStock.charAt(x) != ',')
        field += fieldNamesStock.charAt(x++);
      ++x;
      
      while(x < fieldNamesStockLen && fieldNamesStock.charAt(x) == ' ')
        ++x;
  
      if(fieldName.equals(field))
        return count;
      
      ++count;
    }
    
    return -1;
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

}
