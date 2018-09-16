// =======================================================================================================================================================================================================
// System: ZaraStar: Product: Search buyers catalog
// Module: ProductSearchBuyersCatalog.java
// Author: C.K.Harvey
// Copyright (c) 2001-08 Christopher Harvey. All Rights Reserved.
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

public class ProductSearchBuyersCatalog extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  Inventory inventory = new Inventory();
  ReportGenDetails reportGenDetails = new ReportGenDetails();
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

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="";

    try
    {
      directoryUtils.setContentHeaders2(res);
      out = res.getWriter();

      unm = req.getParameter("unm");
      sid = req.getParameter("sid");      
      uty = req.getParameter("uty");
      dnm = req.getParameter("dnm");
      men = req.getParameter("men");
      den = req.getParameter("den");
      bnm = req.getParameter("bnm");
      p1  = req.getParameter("p1"); // phrase
      p2  = req.getParameter("p2"); // customerCode

      if(p1 == null) p1 = "";
      
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "ProductSearchBuyersCatalog", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2002, bytesOut[0], 0, "ERR:" + p1);
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                    String p1, String p2, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    String userName = directoryUtils.getMySQLUserName();
    String passWord = directoryUtils.getMySQLPassWord();
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + userName + "&password=" + passWord);
    ResultSet rs = null, rs2 = null;
    Statement stmt = null, stmt2 = null;

    byte[] list           = new byte[1000];  list[0] = '\000';
    int[]  lenListEntries = new int[1];      lenListEntries[0] = 0;
    int[]  listLen        = new int[1];      listLen[0] = 1000;

    long[] totalMilliSeconds = new long[1];

    list = set(con, stmt, stmt2, rs, rs2, p1, p2, list, listLen, lenListEntries, totalMilliSeconds, dnm, localDefnsDir, defnsDir);

    returnList(out, totalMilliSeconds[0], list, lenListEntries);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 2002, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con  != null) con.close();
    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private byte[] set(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String phrase, String customerCode, byte[] list,
                     int[] listLen, int[] lenListEntries, long[] totalMilliSeconds, String dnm, String localDefnsDir, String defnsDir)
                     throws Exception
  {
    phrase = generalUtils.stripNoise(generalUtils.stripNonDisplayable(phrase));

    phrase = phrase.toLowerCase();

    list[0] = '\000';
  
    if(phrase.length() > 0)
      list = search(con, stmt, stmt2, rs, rs2, phrase, customerCode, list, listLen, lenListEntries, totalMilliSeconds, dnm, localDefnsDir, defnsDir);

    return list;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private byte[] search(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String phrase, String customerCode, byte[] list,
                        int[] listLen, int[] lenListEntries, long[] totalMilliSeconds, String dnm, String localDefnsDir, String defnsDir)
                        throws Exception
  {
    long startTime = new java.util.Date().getTime();

    boolean firstWord = true;
    int x=0, count;
    String word;
    byte[] b = new byte[50];

    int len = phrase.length();
  
    while(x < len)
    {
      resetAllFlags(list, lenListEntries);
  
      word = "";
      while(x < len && phrase.charAt(x) != ' ')
        word += phrase.charAt(x++);
      ++x;

      word = generalUtils.sanitiseForSQL(word);
      
      // check buyers catalog
      
      stmt = con.createStatement();
    
      rs = stmt.executeQuery("SELECT ItemCode FROM buyerscatalog WHERE CustomerCode = '" + customerCode + "' AND (BuyerItemCode1 LIKE '%" + word
                           + "%' OR BuyerItemCode2 LIKE '%" + word + "%' OR BuyerItemCode3 LIKE '%" + word + "%')");

      count = 0;
      while(rs.next())
      {
        generalUtils.strToBytes(b, rs.getString(1) + "\002Buyers");
        list = checkOnList(firstWord, b, list, listLen, lenListEntries);
        ++count;
      }
      
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
      
      // check text from stock cat, but only for those items in the buyers catalog

      stmt = con.createStatement();
    
      rs = stmt.executeQuery("SELECT ItemCode FROM buyerscatalog WHERE CustomerCode = '" + customerCode + "'");

      byte[] data = new byte[5000];
      String itemCode;
      
      while(rs.next())
      {
        itemCode = rs.getString(1);
        inventory.getStockRecGivenCode(con, stmt2, rs2, itemCode, '\000', data);
        String categoryCode  = generalUtils.dfsAsStr(data, (short)55);

        if(categoryCode.length() > 0 && ! categoryCode.equals("0")) // is a catalog page for this item
        {
          stmt2 = con.createStatement();
          
          rs2 = stmt2.executeQuery("SELECT Description, Text, Text2 FROM stockcat WHERE CategoryCode = '" + categoryCode
                                 + "' AND (Description LIKE '%" + word + "%' OR Text LIKE '%" + word + "%' OR Text2 LIKE '%" + word + "%')");
    
          if(rs2.next())
          {
            generalUtils.strToBytes(b, itemCode + "\002Buyers");
            list = checkOnList(firstWord, b, list, listLen, lenListEntries);
            ++count;
          }

          if(rs2   != null) rs2.close();
          if(stmt2 != null) stmt2.close();
        }
      }
      
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();

      // check text from stock DB, but only for those items in the buyers catalog

      stmt = con.createStatement();
    
      rs = stmt.executeQuery("SELECT ItemCode FROM buyerscatalog WHERE CustomerCode = '" + customerCode + "'");

      while(rs.next())
      {
        stmt2 = con.createStatement();
    
        rs2 = stmt2.executeQuery("SELECT ItemCode FROM stock WHERE ItemCode = '" + rs.getString(1) + "' AND ShowToWeb = 'Y' AND ( Description LIKE '%"
                               + word + "%' OR Description2 LIKE '%" + word + "%' OR Manufacturer LIKE '%"
                               + word + "%' OR ManufacturerCode LIKE '%" + word + "%' )");

        while(rs2.next())
        {
          generalUtils.strToBytes(b, rs2.getString(1) + "\002Buyers");
          list = checkOnList(firstWord, b, list, listLen, lenListEntries);
          ++count;
        }

        if(rs2   != null) rs2.close();
        if(stmt2 != null) stmt2.close();
      }
      
      if(firstWord) firstWord = false;
        
      if(count == 0) // no matches for this word
      //if(first)// && allWords) // no matches
        removeIfFlagNotSet(list, listLen[0], lenListEntries);

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
    
    totalMilliSeconds[0] = (new java.util.Date().getTime() - startTime);

    return list;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // if not on list then fails (unless first word); else sets checked flag
  private byte[] checkOnList(boolean firstWord, byte[] docCode, byte[] list, int[] listLen, int[] lenListEntries) throws Exception
  {
    byte[] flagPlusItem = new byte[50];
    flagPlusItem[0] = (byte)'X';
    generalUtils.bytesToBytes(flagPlusItem, 1, docCode, 0);

    int entryPosn = chkList(flagPlusItem, list, lenListEntries);
    if(entryPosn == -1) // not on list
    {
      if(firstWord)
        list = putOnList(flagPlusItem, list, listLen, lenListEntries);
    }
    // else list[entryPosn] = 'X';

    return list;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public byte[] putOnList(byte[] newItem, byte[] list, int[] listLen, int[] lenListEntries) throws Exception
  {
    int x, y=0, len, start;
    byte[] entry = new byte[50];
    start=0;

    while(y < lenListEntries[0])
    {
      x=0;
      start = y;
      while(list[y] != '\001')
        entry[x++] = list[y++];
      entry[x] = '\000';
      if(generalUtils.matchFixed('=', entry, 1, newItem, 1, x)) // same
        return list;

      if(generalUtils.matchFixed('<', entry, 1, newItem, 1, x)) // newitem is > this entry, so read next
        ++y; // '\001'
      else // newitem > this entry, so insert
      {
        len = generalUtils.lengthBytes(newItem, 0);
        if((lenListEntries[0] + len + 1) >= listLen[0])
        {
          byte[] tmp = new byte[listLen[0]];
          System.arraycopy(list, 0, tmp, 0, listLen[0]);
          listLen[0] += 1000;
          list = new byte[listLen[0]];
          System.arraycopy(tmp, 0, list, 0, listLen[0] - 1000);
        }

        ++y;
        for(x=(listLen[0]-2);x>(start+len-1);--x) // shunt-up
          list[x+1] = list[x - len];

        for(x=0;x<len;++x)
          list[start + x] = newItem[x];
        list[start + len] = '\001';
        lenListEntries[0] += (len + 1);
        return list;
      }
    }

    // insert at end
    len = generalUtils.lengthBytes(newItem, 0);

    if((lenListEntries[0] + len + 1) >= listLen[0])
    {
      byte[] tmp = new byte[listLen[0]];
      System.arraycopy(list, 0, tmp, 0, listLen[0]);
      listLen[0] += 1000;
      list = new byte[listLen[0]];
      System.arraycopy(tmp, 0, list, 0, listLen[0] - 1000);
    }

    for(x=(listLen[0]-1);x>(y+len);--x) // shunt-up
      list[x] = list[x - len];

    for(x=0;x<len;++x)
      list[y + x] = newItem[x];
    list[y + len] = '\001';
    lenListEntries[0] += (len + 1);

    return list;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private int chkList(byte[] reqdItem, byte[] list, int[] lenListEntries) throws Exception
  {
    int x, y=0, entryPosn;
    byte[] entry = new byte[50];

    while(y < lenListEntries[0])
    {
      x=0;
      entryPosn = y;
      while(list[y] != '\001')
        entry[x++] = list[y++];
      entry[x] = '\000';
      if(generalUtils.matchFixed('=', entry, 1, reqdItem, 1, x))
      {
        list[entryPosn] = (byte)'X';
        return entryPosn;
      }
      ++y;
    }

    return -1;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void resetAllFlags(byte[] list, int[] lenListEntries) throws Exception
  {
    int y=0;

    while(y < lenListEntries[0])
    {
      list[y] = (byte)' ';
      while(list[y] != '\001')
        ++y;
      ++y;
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void removeIfFlagNotSet(byte list[], int listLen, int[] lenListEntries) throws Exception
  {
    int x, y=0, z, docCodeLen;

    while(y < lenListEntries[0])
    {
      if(list[y] == (byte)' ')
      {
        x=y;   z=y;
        docCodeLen = 1;
        while(list[y] != '\001')
        {
          ++y;
          ++docCodeLen;
        }

        while(x<(listLen-docCodeLen))
        {
          list[x] = list[(x + docCodeLen)];
          ++x;
        }
        lenListEntries[0] -= docCodeLen;
        y=z;
      }
      else
      {
        while(list[y] != '\001')
          ++y;
        ++y;
      }
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void returnList(PrintWriter out, long totalMilliSeconds, byte[] list, int[] lenListEntries) throws Exception
  {
    byte[] entry   = new byte[41];
    byte[] docCode = new byte[50];
    byte[] docType = new byte[20];
    int i, x, y=0, z;
    long count=0;

    while(y < lenListEntries[0])
    {
      x=0;
      if(list[y] == 'X')
      {
        z=0;
        ++y; // flag
        while(list[y] != '\001')
        {
          entry[z++] = list[y++];
        }
        entry[z] = '\000';
        ++y; // \001

        i=z=0;
        while(entry[i] != '\002' && entry[i] != '\001') // just-in-case
          docCode[z++] = entry[i++];
        docCode[z] = '\000';

        z=0;
        if(entry[i] == '\002') // just-in-case
        {
          ++i; // \002
          while(entry[i] != '\000')
            docType[z++] = entry[i++];
        }
        docType[z] = '\000';

        out.println(generalUtils.stringFromBytes(docType, 0L) + " " + generalUtils.stringFromBytes(docCode, 0L));
        ++count;
      }
      else
      {
        ++y; // flag
        while(list[y] != '\001')
          docCode[x++] = list[y++];
        docCode[x] = '\000';
        ++y; // \001
      }
    }

    out.println("COUNTANDTIME " + generalUtils.longToStr(count) + " " + generalUtils.doubleToStr('2', (double)((double)totalMilliSeconds / 1000.0)));
  }

}
