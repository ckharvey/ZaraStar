// =======================================================================================================================================================================================================
// System: ZaraStar Catalogs: SC category edit - update
// Module: CatalogsSteelclawsCategoryEditUpdate.java
// Author: C.K.Harvey
// Copyright (c) 2002-08 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.sql.*;
import java.util.Enumeration;

public class CatalogsSteelclawsCategoryEditUpdate extends HttpServlet
{
  GeneralUtils  generalUtils = new GeneralUtils();
  MessagePage  messagePage = new MessagePage();
  ServerUtils  serverUtils = new ServerUtils();
  PageFrameUtils  pageFrameUtils = new PageFrameUtils();
  DirectoryUtils  directoryUtils = new DirectoryUtils();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", mfr="", category="", rows="", cols="", chapter="", title="", pageName="",
           imageSmall="", imageSmall2="", imageLarge="", text="", text2="", heading1="", heading2="", heading3="", heading4="", heading5="",
           heading6="", heading7="", heading8="", heading9="", heading10="", section="", originalPage="", option="", newProduct="";

    try
    {
      directoryUtils.setContentHeaders(res);
      res.setHeader("Header", "HTTP/1.0 200 OK");
      res.setStatus(200);
      
      byte[] lines = new byte[1000]; lines[0]    = '\000';
      int[]  linesLen = new int[1];  linesLen[0] = 1000;

      byte[] lineValues = new byte[1000]; lineValues[0]    = '\000';
      int[]  lineValuesLen = new int[1];  lineValuesLen[0] = 1000;

      int thisEntryLen, inc;
     
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
        if(name.length() > 2 && name.substring(1, 3).equals(".x")) ;
        else
        if(name.length() > 2 && name.substring(1, 3).equals(".y")) ;
        else
        if(name.equals("mfr"))
          mfr = value[0];
        else
        if(name.equals("category"))
          category = value[0];
        else
        if(name.equals("rows"))
          rows = value[0];
        else
        if(name.equals("cols"))
          cols = value[0];
        else
        if(name.equals("pageName"))
          pageName = value[0];
        else
        if(name.equals("chapter"))
          chapter = value[0];
        else
        if(name.equals("section"))
          section = value[0];
        else
        if(name.equals("title"))
          title = value[0];
        else
        if(name.equals("imageSmall"))
          imageSmall = value[0];
        else
        if(name.equals("imageSmall2"))
          imageSmall2 = value[0];
        else
        if(name.equals("imageLarge"))
          imageLarge = value[0];
        else
        if(name.equals("text"))
          text = value[0];
        else
        if(name.equals("text2"))
          text2 = value[0];
        else
        if(name.equals("heading1"))
          heading1 = value[0];
        else
        if(name.equals("heading2"))
          heading2 = value[0];
        else
        if(name.equals("heading3"))
          heading3 = value[0];
        else
        if(name.equals("heading4"))
          heading4 = value[0];
        else
        if(name.equals("heading5"))
          heading5 = value[0];
        else
        if(name.equals("heading6"))
          heading6 = value[0];
        else
        if(name.equals("heading7"))
          heading7 = value[0];
        else
        if(name.equals("heading8"))
          heading8 = value[0];
        else
        if(name.equals("heading9"))
          heading9 = value[0];
        else
        if(name.equals("heading10"))
          heading10 = value[0];
        else
        if(name.equals("originalPage"))
          originalPage = value[0];
        else
        if(name.equals("option"))
          option = value[0];
        else
        if(name.equals("newProduct"))
          newProduct = value[0];
        else
        {
          thisEntryLen = name.length() + 2;
          if((generalUtils.lengthBytes(lines, 0) + thisEntryLen) >= linesLen[0])
          {
            byte[] tmp = new byte[linesLen[0]];
            System.arraycopy(lines, 0, tmp, 0, linesLen[0]);
            if(thisEntryLen > 1000)
              inc = thisEntryLen;
            else inc = 1000;
            linesLen[0] += inc;
            lines = new byte[linesLen[0]];
            System.arraycopy(tmp, 0, lines, 0, linesLen[0] - inc);
          }

          generalUtils.catAsBytes(name + "\001", 0, lines, false); 

          thisEntryLen = value[0].length() + 2;
          if((generalUtils.lengthBytes(lineValues, 0) + thisEntryLen) >= lineValuesLen[0])
          {
            byte[] tmp = new byte[lineValuesLen[0]];
            System.arraycopy(lineValues, 0, tmp, 0, lineValuesLen[0]);
            if(thisEntryLen > 1000)
              inc = thisEntryLen;
            else inc = 1000;
            lineValuesLen[0] += inc;
            lineValues = new byte[lineValuesLen[0]];
            System.arraycopy(tmp, 0, lineValues, 0, lineValuesLen[0] - inc);
          }

          generalUtils.catAsBytes(value[0] + "\001", 0, lineValues, false);
        }  
      }

      if(chapter  == null) chapter = "";
      if(section  == null) section = "";
      if(pageName == null) pageName = "";
      
      doIt(req, res, mfr, category, chapter, section, title, pageName, imageSmall, imageSmall2, imageLarge, text, text2, heading1, heading2, heading3,
           heading4, heading5, heading6, heading7, heading8, heading9, heading10, originalPage, newProduct, lines, lineValues, unm, sid, uty, dnm,
           bnm, bytesOut);
    }
    catch(Exception e)
    {
      System.out.println(("Unexpected System Error: 6402a: " + e));
      res.getWriter().write("Unexpected System Error: 6402a");
    }
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(HttpServletRequest req, HttpServletResponse res, String mfr, String category, String chapter, String section, String title,
                    String pageName, String imageSmall, String imageSmall2, String imageLarge, String text, String text2, String heading1,
                    String heading2, String heading3, String heading4, String heading5, String heading6, String heading7, String heading8,
                    String heading9, String heading10, String originalPage, String newProduct, byte[] lines, byte[] lineValues, String unm,
                    String sid, String uty, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
 
    String rtn="Unexpected System Error: 6402a";
    
    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      rtn = "Access Denied - Duplicate SignOn or Session Timeout";
    }
    else
    {
      chapter     = generalUtils.deSanitise(chapter);
      section     = generalUtils.deSanitise(section);
      pageName    = generalUtils.deSanitise(pageName);

      mfr         = generalUtils.stripLeadingAndTrailingSpaces(mfr);
      category    = generalUtils.stripLeadingAndTrailingSpaces(category);
      title       = generalUtils.stripLeadingAndTrailingSpaces(title);
      imageSmall  = generalUtils.stripLeadingAndTrailingSpaces(imageSmall);
      imageSmall2 = generalUtils.stripLeadingAndTrailingSpaces(imageSmall2);
      imageLarge  = generalUtils.stripLeadingAndTrailingSpaces(imageLarge);
      text        = generalUtils.stripLeadingAndTrailingSpaces(text);
      text2       = generalUtils.stripLeadingAndTrailingSpaces(text2);
      heading1    = generalUtils.stripLeadingAndTrailingSpaces(heading1);
      heading2    = generalUtils.stripLeadingAndTrailingSpaces(heading2);
      heading3    = generalUtils.stripLeadingAndTrailingSpaces(heading3);
      heading4    = generalUtils.stripLeadingAndTrailingSpaces(heading4);
      heading5    = generalUtils.stripLeadingAndTrailingSpaces(heading5);
      heading6    = generalUtils.stripLeadingAndTrailingSpaces(heading6);
      heading7    = generalUtils.stripLeadingAndTrailingSpaces(heading7);
      heading8    = generalUtils.stripLeadingAndTrailingSpaces(heading8);
      heading9    = generalUtils.stripLeadingAndTrailingSpaces(heading9);
      heading10   = generalUtils.stripLeadingAndTrailingSpaces(heading10);
      originalPage = generalUtils.stripLeadingAndTrailingSpaces(originalPage);
      
      if(mfr.length() == 0)
        rtn = "No Manufacturer Name Entered";
      else
      if(chapter.length() == 0)
        rtn = "No Chapter Entered";
      else
      if(title.length() == 0)
        rtn = "No Title Entered";
      else
      {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs   = null;

        String uName    = directoryUtils.getMySQLUserName();
        String passWord = directoryUtils.getMySQLPassWord();

        Class.forName("com.mysql.jdbc.Driver").newInstance();
    
        con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + uName + "&password=" + passWord);

        String[] actualCategory = new String[1];  actualCategory[0] = category;
        
        switch(update(con, stmt, rs, mfr, actualCategory, chapter, section, title, pageName, imageSmall, imageSmall2, imageLarge, text, text2,
                      heading1, heading2, heading3, heading4, heading5, heading6, heading7, heading8, heading9, heading10, originalPage, newProduct))
        {
          case ' ' : rtn = "Updated: " + actualCategory[0]; 
                     char rtn2 = updateLines(con, stmt, rs, lines, lineValues, mfr, actualCategory[0]);
                     if(rtn2 != ' ')
                       rtn = "Missing MfrCode";
                     break;
          case 'E' : rtn = "Category Already Exists: " + actualCategory[0]; break;
        }
       
        if(con != null) con.close();
      }
    }
    
    res.setContentType("text/xml");
    res.setHeader("Cache-Control", "no-cache");
    
    String s = "<msg><res>" + rtn + "</res></msg>";
    res.getWriter().write(s);
    
    serverUtils.totalBytes(req, unm, dnm, 6402, bytesOut[0], 0, mfr + ":" + category);
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // rtns: ' ' if rec added/updated ok
  //       'E' if adding a rec that already exists
  //       'X' other error
  private char update(Connection con, Statement stmt, ResultSet rs, String mfr, String[] category, String chapter, String section, String title,
                      String pageName, String imageSmall, String imageSmall2, String imageLarge, String text, String text2, String heading1,
                      String heading2, String heading3, String heading4, String heading5, String heading6, String heading7, String heading8,
                      String heading9, String heading10, String originalPage, String newProduct) throws Exception
  {
    try
    {
      boolean isNew;
      if(category[0].length() == 0)
      {
        isNew = true;
        stmt = con.createStatement();
        stmt.setMaxRows(1);
        rs = stmt.executeQuery("SELECT Category FROM catalog WHERE Manufacturer = '" + mfr + "'ORDER BY Category DESC"); 
        if(rs.next())
          category[0] = generalUtils.intToStr(generalUtils.strToInt(rs.getString(1)) + 1);
        else category[0] = "1";
        
        if(rs   != null) rs.close();  
        if(stmt != null) stmt.close();
      }
      else // check if already exists
      {       
        boolean alreadyExists=false;
        stmt = con.createStatement();
        rs = stmt.executeQuery("SELECT Manufacturer FROM catalog WHERE Manufacturer = '" + mfr + "' AND Category = '" + category[0] + "'"); 
        if(rs.next())
         alreadyExists = true;
        if(rs   != null) rs.close();  
        if(stmt != null) stmt.close();

        if(alreadyExists)
          isNew = false;
        else isNew = true;
      }
      
      String q;
      
      if(newProduct.equals("on"))
        newProduct = "Y";
      else newProduct = "N";
      
      if(isNew)
      {
        q = "INSERT INTO catalog ( Manufacturer, Category, Chapter, PageName, Title, ImageSmall, ImageSmall2, Imagelarge, Download, "
          + "ExternalURL, Heading1, Heading2, Heading3, Heading4, Heading5, Heading6, Heading7, Heading8, Heading9, Heading10, NoAvailability, "
          + "CategoryLink, Text, Text2, Section, OriginalPage, NewProduct ) "
          + "VALUES ('" + mfr + "','" + category[0] + "','" + generalUtils.sanitiseForSQL(chapter) + "','" + generalUtils.sanitiseForSQL(pageName)
          + "','" + generalUtils.sanitiseForSQL(title) + "','" + generalUtils.sanitiseForSQL(imageSmall) + "','" + generalUtils.sanitiseForSQL(imageSmall2) + "','"
          + generalUtils.sanitiseForSQL(imageLarge) + "','0','','" + generalUtils.sanitiseForSQL(heading1) + "','" + generalUtils.sanitiseForSQL(heading2) + "','"
          + generalUtils.sanitiseForSQL(heading3) + "','" + generalUtils.sanitiseForSQL(heading4) + "','" + generalUtils.sanitiseForSQL(heading5) + "','"
          + generalUtils.sanitiseForSQL(heading6) + "','" + generalUtils.sanitiseForSQL(heading7) + "','" + generalUtils.sanitiseForSQL(heading8) + "','"
          + generalUtils.sanitiseForSQL(heading9) + "','" + generalUtils.sanitiseForSQL(heading10) + "','0','" + generalUtils.sanitiseForSQL(text) + "','"
          + generalUtils.sanitiseForSQL(text2) + "','" + generalUtils.sanitiseForSQL(section) + "','" + generalUtils.sanitiseForSQL(originalPage) + "','" + newProduct
          + "' )";
      }
      else
      {
        q = "UPDATE catalog SET Chapter = '" + generalUtils.sanitiseForSQL(chapter) + "', PageName = '" + generalUtils.sanitiseForSQL(pageName) + "', Title = '"
          + generalUtils.sanitiseForSQL(title) + "', ImageSmall = '" + generalUtils.sanitiseForSQL(imageSmall) + "', ImageSmall2 = '"
          + generalUtils.sanitiseForSQL(imageSmall2) + "', ImageLarge = '" + generalUtils.sanitiseForSQL(imageLarge)
          + "', Download = '0', ExternalURL = '', Heading1 = '" + generalUtils.sanitiseForSQL(heading1) + "', Heading2 = '" + generalUtils.sanitiseForSQL(heading2)
          + "', Heading3 = '" + generalUtils.sanitiseForSQL(heading3) + "', Heading4 = '" + generalUtils.sanitiseForSQL(heading4) + "', Heading5 = '"
          + generalUtils.sanitiseForSQL(heading5) + "', Heading6 = '" + generalUtils.sanitiseForSQL(heading6) + "', Heading7 = '" + generalUtils.sanitiseForSQL(heading7)
          + "', Heading8 = '" + generalUtils.sanitiseForSQL(heading8) + "', Heading9 = '" + generalUtils.sanitiseForSQL(heading9) + "', Heading10 = '"
          + generalUtils.sanitiseForSQL(heading10) + "', Text = '" + generalUtils.sanitiseForSQL(text) + "', Text2 = '" + generalUtils.sanitiseForSQL(text2)
          + "', OriginalPage = '" + generalUtils.sanitiseForSQL(originalPage) + "', Section = '" + generalUtils.sanitiseForSQL(section) + "', NewProduct = '"
          + newProduct + "' WHERE Manufacturer = '" + generalUtils.sanitiseForSQL(mfr) + "' AND Category = '" + category[0] + "'";
      }
  
      stmt = con.createStatement();
          
      stmt.executeUpdate(q);

      if(stmt != null) stmt.close();
            
      return ' ';
    }
    catch(Exception e)
    {
      System.out.println("6402a: " + e);
      if(stmt != null) stmt.close();
    }
  
    return 'X';
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // rtns: ' ' if rec added/updated ok
  //       'E' if mfrCode blank
  //       'X' other error
  private char updateLines(Connection con, Statement stmt, ResultSet rs, byte[] lines, byte[] lineValues, String mfr, String category)
                           throws Exception
  {
    // determine the highest line number
    String thisEntry;
    int x=0, len = generalUtils.lengthBytes(lines, 0), highestLineNumberSofar = 0, lineNumber;
    while(x < len)
    {
      thisEntry = "";
      while(x < len && lines[x] != '\001')
        thisEntry += (char)lines[x++];
      if(thisEntry.startsWith("mfrCode"))
      {
        lineNumber = generalUtils.strToInt(thisEntry.substring(7, thisEntry.length()));
        if(lineNumber > highestLineNumberSofar)
          highestLineNumberSofar = lineNumber;
      }      
      
      ++x;
    }
    
    String mfrCode, entry1, entry2, entry3, entry4, entry5, entry6, entry7, entry8, entry9, entry10, desc, desc2, linePosn;

    int count;
    // extract and save for each line
    for(int line=0;line<=highestLineNumberSofar;++line)
    {
      mfrCode = entry1 = entry2 = entry3 = entry4 = entry5 = entry6 = entry7 = entry8 = entry9 = entry10 = desc = desc2 = linePosn = "";

      x = count = 0;
      while(x < len)
      {
        thisEntry = "";
        while(x < len && lines[x] != '\001')
          thisEntry += (char)lines[x++];

        if(thisEntry.startsWith("mfrCode"))
        {
          lineNumber = generalUtils.strToInt(thisEntry.substring(7, thisEntry.length()));
          if(lineNumber == line)
            mfrCode = generalUtils.dfsAsStrGivenBinary1(lineValues, (short)count);
        }
        else
        if(thisEntry.startsWith("line"))
        {
          lineNumber = generalUtils.strToInt(thisEntry.substring(4, thisEntry.length()));
          if(lineNumber == line)
            linePosn = generalUtils.dfsAsStrGivenBinary1(lineValues, (short)count);
        }
        else
        if(thisEntry.startsWith("descb"))
        {
          lineNumber = generalUtils.strToInt(thisEntry.substring(5, thisEntry.length()));
          if(lineNumber == line)
            desc2 = generalUtils.dfsAsStrGivenBinary1(lineValues, (short)count);
        }
        else
        if(thisEntry.startsWith("desca"))
        {
          lineNumber = generalUtils.strToInt(thisEntry.substring(5, thisEntry.length()));
          if(lineNumber == line)
            desc = generalUtils.dfsAsStrGivenBinary1(lineValues, (short)count);
        }
        else
        if(thisEntry.startsWith("entry"))
        {
          lineNumber = generalUtils.strToInt(thisEntry.substring(6, thisEntry.length()));
          if(lineNumber == line)
          {
            switch(thisEntry.charAt(5))
            {
              case '1' : entry1  = generalUtils.dfsAsStrGivenBinary1(lineValues, (short)count); break;
              case '2' : entry2  = generalUtils.dfsAsStrGivenBinary1(lineValues, (short)count); break;
              case '3' : entry3  = generalUtils.dfsAsStrGivenBinary1(lineValues, (short)count); break;
              case '4' : entry4  = generalUtils.dfsAsStrGivenBinary1(lineValues, (short)count); break;
              case '5' : entry5  = generalUtils.dfsAsStrGivenBinary1(lineValues, (short)count); break;
              case '6' : entry6  = generalUtils.dfsAsStrGivenBinary1(lineValues, (short)count); break;
              case '7' : entry7  = generalUtils.dfsAsStrGivenBinary1(lineValues, (short)count); break;
              case '8' : entry8  = generalUtils.dfsAsStrGivenBinary1(lineValues, (short)count); break;
              case '9' : entry9  = generalUtils.dfsAsStrGivenBinary1(lineValues, (short)count); break;
              case '0' : entry10 = generalUtils.dfsAsStrGivenBinary1(lineValues, (short)count); break;
            }
          }
        }      
      
        ++x;
        ++count;      
      }
            
      if(mfrCode.length() == 0)
        return 'E';

      try
      {
        boolean alreadyExists = false;
        stmt = con.createStatement();

        rs = stmt.executeQuery("SELECT Manufacturer FROM catalogl WHERE Manufacturer = '" + mfr + "' AND Category = '" + category
                             + "' AND ManufacturerCode = '" + mfrCode + "'"); 
        if(rs.next())
         alreadyExists = true;
        if(rs   != null) rs.close();  
        if(stmt != null) stmt.close();
      
        String q;
      
        if(! alreadyExists)
        {
          q = "INSERT INTO catalogl ( Manufacturer, Category, ManufacturerCode, Entry1, Entry2, Entry3, Entry4, Entry5, Entry6, Entry7, Entry8, "
            + "Entry9, Entry10, Description, Description2, Line ) "
            + "VALUES ('" + mfr + "','" + category + "','" + generalUtils.sanitiseForSQL(mfrCode) + "','" + generalUtils.sanitiseForSQL(entry1) + "','"
            + generalUtils.sanitiseForSQL(entry2) + "','" + generalUtils.sanitiseForSQL(entry3) + "','" + generalUtils.sanitiseForSQL(entry4) + "','"
            + generalUtils.sanitiseForSQL(entry5) + "','" + generalUtils.sanitiseForSQL(entry6) + "','" + generalUtils.sanitiseForSQL(entry7) + "','"
            + generalUtils.sanitiseForSQL(entry8) + "','" + generalUtils.sanitiseForSQL(entry9) + "','" + generalUtils.sanitiseForSQL(entry10) + "','"
            + generalUtils.sanitiseForSQL(desc) + "','" + generalUtils.sanitiseForSQL(desc2) + "','" + linePosn + "')";
        }
        else
        {
          q = "UPDATE catalogl SET Entry1 = '" + generalUtils.sanitiseForSQL(entry1) + "', Entry2 = '" + generalUtils.sanitiseForSQL(entry2) + "', Entry3 = '"
            + generalUtils.sanitiseForSQL(entry3) + "', Entry4 = '" + generalUtils.sanitiseForSQL(entry4) + "', Entry5 = '" + generalUtils.sanitiseForSQL(entry5)
            + "', Entry6 = '" + generalUtils.sanitiseForSQL(entry6) + "', Entry7 = '" + generalUtils.sanitiseForSQL(entry7) + "', Entry8 = '"
            + generalUtils.sanitiseForSQL(entry8) + "', Entry9 = '" + generalUtils.sanitiseForSQL(entry9) + "', Entry10 = '" + generalUtils.sanitiseForSQL(entry10)
            + "', Description = '" + generalUtils.sanitiseForSQL(desc) + "', Description2 = '" + generalUtils.sanitiseForSQL(desc2) + "', Line = '" + linePosn + "' "
            + "WHERE Manufacturer = '" + mfr + "' AND Category = '" + category + "' AND ManufacturerCode = '" + mfrCode + "'";
        }
  
        stmt = con.createStatement();
          
        stmt.executeUpdate(q);

        if(stmt != null) stmt.close();
      }
      catch(Exception e)
      { 
        System.out.println("6402a: " + e);
        if(stmt != null) stmt.close();
      }
    }
  
    return ' ';
  }

}
