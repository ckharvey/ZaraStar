// =======================================================================================================================================================================================================
// System: ZaraStar: DocumentEngine: Process build doc from doc (type 2)
// Module: DocumentBuildType2Execute.java
// Author: C.K.Harvey
// Copyright (c) 2003-07 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.io.*;
import java.sql.*;

public class DocumentBuildType2Execute extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="", p5="", renumberLines="N", renumberEntries="N";

    try
    {
      directoryUtils.setContentHeaders2(res);
      out = res.getWriter();

      byte[] quantities         = new byte[1000]; quantities[0]         = '\000';
      int[]  quantitiesLen      = new int[1];     quantitiesLen[0]      = 1000;
      int[]  quantitiesNames    = new int[1000];
      int[]  quantitiesNamesLen = new int[1];     quantitiesNamesLen[0] = 1000;

      int thisEntryLen, inc, x, count=0;
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
        if(name.equals("p1")) // sourceDocCode
          p1 = value[0];
        else
        if(name.equals("p2")) // serviceCode
          p2 = value[0];
        else
        if(name.equals("p5")) // numLines
          p5 = value[0];
        else
        if(name.equals("all"))
          ; // ignore
        else
        if(name.equals("renumberLines"))
          renumberLines = "Y";
        else
        if(name.equals("renumberEntries"))
          renumberEntries = "Y";
        else // must be (other) input value
        {
          if(count == quantitiesNamesLen[0])
          {
            int[] tmp = new int[quantitiesNamesLen[0]];
            for(x=0;x<count;++x)
              tmp[x] = quantitiesNames[x];
            quantitiesNamesLen[0] += 100;
            quantitiesNames = new int[quantitiesNamesLen[0]];
            for(x=0;x<quantitiesNamesLen[0];++x)
              quantitiesNames[x] = tmp[x];
          }

          quantitiesNames[count++] =  generalUtils.strToInt(name);

          thisEntryLen = value[0].length() + 2;

          if((generalUtils.lengthBytes(quantities, 0) + thisEntryLen) >= quantitiesLen[0])
          {
            byte[] tmp = new byte[quantitiesLen[0]];
            System.arraycopy(quantities, 0, tmp, 0, quantitiesLen[0]);
            if(thisEntryLen > 1000)
              inc = thisEntryLen;
            else inc = 1000;
            quantitiesLen[0] += inc;
            quantities = new byte[quantitiesLen[0]];
            System.arraycopy(tmp, 0, quantities, 0, quantitiesLen[0] - inc);
          }

          generalUtils.catAsBytes(value[0] + "\001", 0, quantities, false);
        }
      }

      doIt(out, req, renumberLines, renumberEntries, quantities, quantitiesNames, p1, p2, p5, unm, sid, uty, men, den, dnm, bnm, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "DocumentBuildType2Execute", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6097, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String renumberLines, String renumberEntries, byte[] quantities, int[] quantitiesNames,
                    String p1, String p2, String p5, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                    int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String localDefnsDir  = directoryUtils.getLocalOverrideDir(dnm);
    String defnsDir          = directoryUtils.getSupportDirs('D');
    String imagesDir         = directoryUtils.getSupportDirs('I');
    
    Connection con = null;
    Statement stmt = null;
    ResultSet rs   = null;
    Statement stmt2 = null;
    ResultSet rs2   = null;
    
    try
    {
      String uName    = directoryUtils.getMySQLUserName();
      String passWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();
    
      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + uName + "&password=" + passWord);

      if(! authenticationUtils.verifyAccess(con, stmt, rs, req, generalUtils.intFromStr(p2), unm, uty, dnm, localDefnsDir, defnsDir))
      {
        messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "DocumentBuildType2Execute", imagesDir, localDefnsDir, defnsDir, bytesOut);
        serverUtils.etotalBytes(req, unm, dnm, 6097, bytesOut[0], 0, "ACC:" + p1);
        if(con  != null) con.close();
        if(out != null) out.flush();
        return;
      }

      if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
      {
        messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "DocumentBuildType2Execute", imagesDir, localDefnsDir, defnsDir, bytesOut);
        serverUtils.etotalBytes(req, unm, dnm, 6097, bytesOut[0], 0, "SID:" + p1);
        if(con  != null) con.close();
        if(out != null) out.flush();
        return;
      }

      process(con, stmt, stmt2, rs, rs2, out, req, renumberLines, renumberEntries, p1, p2, quantities, quantitiesNames, p5, unm, sid, uty, men, den,
              dnm, bnm, imagesDir, localDefnsDir, defnsDir, bytesOut);
    }
    catch(Exception e) { }

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 6097, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con  != null) con.close();
    if(out != null) out.flush();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void process(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, HttpServletRequest req,
                       String renumberLines, String renumberEntries, String sourceDocCode, String serviceCode, byte[] quantities,
                       int[] quantitiesNames, String numQuantitiesNames, String unm, String sid, String uty, String men, String den, String dnm,
                       String bnm, String imagesDir, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    // sort the lines (they are in random order)
    int numLines = generalUtils.intFromStr(numQuantitiesNames);

    insertionSort(quantitiesNames, numLines, quantities, generalUtils.lengthBytes(quantities, 0)+1);
    
    byte[] codeB       = new byte[21];
    generalUtils.strToBytes(codeB, sourceDocCode);

    if(serviceCode.equals("4058"))
    {      
      
    } 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void insertionSort(int[] lineNums, int numElements, byte[] quantities, int listLen) throws Exception
  {
    try
    {
      int j;
      for(int i=1;i<numElements;++i)
      {
        for(j=0;j<i;++j)
        {
          if(lineNums[i-j-1] > lineNums[i-j])
          {  
            swap(lineNums,    i-j-1, i-j);
            swap2(quantities, i-j-1, i-j, listLen);
          }
          else j = i;
        }
      }
    }
    catch(Exception e){System.out.println(e);};
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void swap(int[] lineNums, int x, int y) throws Exception
  {
    int i = lineNums[x];
    lineNums[x] = lineNums[y];
    lineNums[y] = i;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void swap2(byte[] quantities, int x, int y, int listLen) throws Exception
  {
    byte[] entry1 = new byte[1001];
    byte[] entry2 = new byte[1002];
    generalUtils.getListEntryByNum(x, quantities, entry1);
    generalUtils.getListEntryByNum(y, quantities, entry2);
    repListEntry(x, entry2, quantities, listLen);
    repListEntry(y, entry1, quantities, listLen);
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void repListEntry(int entryNum, byte[] newEntry, byte[] list, int listLen) throws Exception
  {
    // determine start of entry to replace
    // determine end of entry to replace
    // copy start bit, new bit, then tail bit to new array; then copy back to old array
    
    int entryToReplaceStart = getEntryStartPosition(entryNum, list);
    int afterEntryToReplace = getEntryStartPosition((entryNum + 1), list);
    
    byte[] tmp = new byte[listLen + listLen];
    int x=0;
    while(x < entryToReplaceStart)
    {
      tmp[x] = list[x];
      ++x;
    }  
    
    int y=0;
    while(newEntry[y] != '\000')
      tmp[x++] = newEntry[y++];
    tmp[x++] = '\001';
            
    y = afterEntryToReplace;
    while(y < listLen)
    {
      tmp[x++] = list[y++];
    }  
    tmp[x] = '\000';
   
    for(x=0;x<listLen;++x)
      list[x] = tmp[x];
    tmp[listLen] = '\000';
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private int getEntryStartPosition(int entryNum, byte[] list) throws Exception
  {
    int x=0, count=0;

    while(count < entryNum)
    {
      while(list[x] != '\001')
        ++x;
      ++x;

      if(count == entryNum)
        return x;
 
      ++count;
    }

    return x;
  }

}
