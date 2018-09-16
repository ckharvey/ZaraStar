// =======================================================================================================================================================================================================
// System: ZaraStar Product: Import SC catalog
// Module: CatalogZaraImportExecute.java
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

public class CatalogZaraImportExecute extends HttpServlet
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

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="";

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
        if(name.equals("p2")) // mfr
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "CatalogZaraImporta", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2010, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm,
                    String bnm, String p1, String p2, int[] bytesOut) throws Exception
  {
    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    long startTime = new java.util.Date().getTime();

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 2010, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "CatalogZaraImporta", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2010, bytesOut[0], 0, "ACC:" + p1);
       
      if(con != null) con.close();
     if(out != null) out.flush(); 
      return;
    }
    
    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "CatalogZaraImporta", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2010, bytesOut[0], 0, "SID:" + p1);
      
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    analyze(con, stmt, rs, out, req, p1, p2, unm, sid, uty, men, den, dnm, bnm, imagesDir, localDefnsDir, defnsDir, bytesOut);
     
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "</table></form>");     scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 

    File file = new File(p1);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 4311, bytesOut[0], (int)file.length(), (new java.util.Date().getTime() - startTime), p1);
    generalUtils.fileDelete(p1);
       
      if(con != null) con.close();

    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void analyze(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String tmpFile, String mfr, String unm, String sid, String uty,
                       String men, String den, String dnm, String bnm, String imagesDir, String localDefnsDir, String defnsDir, int[] bytesOut)
                       throws Exception
  {   
    scoutln(out, bytesOut, "<html><head><title>Import Zara-format Catalog</title>");

    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir)
                         + "general.css\"></head>");

    int[] hmenuCount = new int[1];
 
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "2010", "", "CatalogZaraImport", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Import Zara-format Catalog", "2010",  unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<table id=\"page\" width=100% border=0>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    
    // ensure that first line of file has legal field names
    
    if(mfr.length() == 0)
    {
      scoutln(out, bytesOut, "<tr><td><p>No Manufacturer specified</td></tr>");
      return;
    }

    RandomAccessFile fh;
    if((fh = generalUtils.fileOpen(tmpFile)) == null) // just-in-case: file not found
    {
      scoutln(out, bytesOut, "<tr><td><p>Upload File Not Found</td></tr>");
      generalUtils.fileClose(fh);
      return;
    }

    if(fh.length() == 0) // File Empty
    {
      scoutln(out, bytesOut, "<tr><td><p>Uploaded File Empty!</td></tr>");
      generalUtils.fileClose(fh);
      return;
    }
    
    String op, entry;
    boolean atLeastOneError = false;
     
    fh.seek(0L);
    
    String tmp = fh.readLine();
    while(tmp != null)
    {
      if(tmp.length() >= 2)
      {
        op = tmp.substring(0, 2);
        entry = generalUtils.stripLeadingAndTrailingSpaces(tmp.substring(2));

        if(   op.equals("ca") || op.equals("ch") || op.equals("ti") || op.equals("pa") || op.equals("i1") || op.equals("i2") || op.equals("im")
           || op.equals("h1") || op.equals("h2") || op.equals("h3") || op.equals("h4") || op.equals("h5") || op.equals("h6") || op.equals("h7")
           || op.equals("h8") || op.equals("h9") || op.equals("h0") || op.equals("mc") || op.equals("e1") || op.equals("e2") || op.equals("e3")
           || op.equals("e4") || op.equals("e5") || op.equals("e6") || op.equals("e7") || op.equals("e8") || op.equals("e9") || op.equals("e0")
           || op.equals("do") || op.equals("eu") || op.equals("cl") || op.equals("or") || op.equals("d1") || op.equals("d2") || op.equals("t1")
           || op.equals("t2")
          )
        {
          ;
        }
        else // unknown
        {
          scoutln(out, bytesOut, "<tr><td><p>Error: " + op + " " + entry + "</td></tr>");
          atLeastOneError = true;
        }
      }
      
      tmp = fh.readLine();
    }
    
    if(atLeastOneError)
    {
      generalUtils.fileClose(fh);
      return;
    }

    try
    {
      removeExistingRecords(con, stmt, mfr);
      
      fh.seek(0L);
      
      String category = "", manufacturerCode = "", entryT1 = "", entryT2 = "", page, pageName;
      boolean firstT1 = true, firstT2 = true;
      int x, len;
    
      tmp = fh.readLine();
      while(tmp != null)
      {
        if(tmp.length() >= 2)
        {
          op = tmp.substring(0,2);
          entry = generalUtils.stripLeadingAndTrailingSpaces(tmp.substring(2));

          if(op.equals("ca")) // category
          {
            if(! firstT1) // some text from previous category
            {
              updateCatalogRecord(con, stmt, mfr, category, "Text", entryT1);
              entryT1 = "";
              firstT1 = true;
            }
          
            if(! firstT2) // some text from previous category
            {
              updateCatalogRecord(con, stmt, mfr, category, "Text2", entryT2);
              entryT2 = "";
              firstT2 = true;
            }
          
            category = entry;
            createNewCatalogRecord(con, stmt, mfr, category);
          }
          else
          if(op.equals("ch")) // chapter
          {
            updateCatalogRecord(con, stmt, mfr, category, "Chapter", entry);
          }
          else
          if(op.equals("ti")) // title
          {
            updateCatalogRecord(con, stmt, mfr, category, "Title", entry);
          }
          else
          if(op.equals("pa")) // page
          {
            len = entry.length();
            page = "";
            x=0;
            while(x < len && entry.charAt(x) != ' ')
              page += entry.charAt(x++);
            
            updateCatalogRecord(con, stmt, mfr, category, "Page", page);
            
            while(x < len && entry.charAt(x) != ' ')
              ++x;

            pageName = "";
            while(x < len)
              pageName += entry.charAt(x++);
            
            updateCatalogRecord(con, stmt, mfr, category, "PageName", pageName);
          }
          else
          if(op.equals("i1")) // small image 1
          {
            updateCatalogRecord(con, stmt, mfr, category, "ImageSmall", entry);
          }
          else
          if(op.equals("i2")) // small image 2
          {
            updateCatalogRecord(con, stmt, mfr, category, "ImageSmall2", entry);
          }
          else
          if(op.equals("im")) // large image
          {
            updateCatalogRecord(con, stmt, mfr, category, "ImageLarge", entry);
          }
          else
          if(op.equals("h1")) // heading 1
          {
            updateCatalogRecord(con, stmt, mfr, category, "Heading1", entry);
          }
          else
          if(op.equals("h2")) // heading 2
          {
            updateCatalogRecord(con, stmt, mfr, category, "Heading2", entry);
          }
          else
          if(op.equals("h3")) // heading 3
          {
            updateCatalogRecord(con, stmt, mfr, category, "Heading3", entry);
          }
          else
          if(op.equals("h4")) // heading 4
          {
            updateCatalogRecord(con, stmt, mfr, category, "Heading4", entry);
          }
          else
          if(op.equals("h5")) // heading 5
          {
            updateCatalogRecord(con, stmt, mfr, category, "Heading5", entry);
          }
          else
          if(op.equals("h6")) // heading 6
          {
            updateCatalogRecord(con, stmt, mfr, category, "Heading6", entry);
          }
          else
          if(op.equals("h7")) // heading 7
          {
            updateCatalogRecord(con, stmt, mfr, category, "Heading7", entry);
          }
          else
          if(op.equals("h8")) // heading 8
          {
            updateCatalogRecord(con, stmt, mfr, category, "Heading8", entry);
          }
          else
          if(op.equals("h9")) // heading 9
          {
            updateCatalogRecord(con, stmt, mfr, category, "Heading9", entry);
          } 
          else
          if(op.equals("h0")) // heading 10
            updateCatalogRecord(con, stmt, mfr, category, "Heading10", entry);
          else
          if(op.equals("mc")) // mfrCode
          {
            manufacturerCode = entry;
            createNewCatalogLRecord(con, stmt, mfr, category, manufacturerCode);
          }
          else
          if(op.equals("e1")) // entry 1
            updateCatalogLRecord(con, stmt, mfr, category, manufacturerCode, "Entry1", entry);
          else
          if(op.equals("e2")) // entry 2
            updateCatalogLRecord(con, stmt, mfr, category, manufacturerCode, "Entry2", entry);
          else
          if(op.equals("e3")) // entry 3
            updateCatalogLRecord(con, stmt, mfr, category, manufacturerCode, "Entry3", entry);
          else
          if(op.equals("e4")) // entry 4
            updateCatalogLRecord(con, stmt, mfr, category, manufacturerCode, "Entry4", entry);
          else
          if(op.equals("e5")) // entry 5
            updateCatalogLRecord(con, stmt, mfr, category, manufacturerCode, "Entry5", entry);
          else
          if(op.equals("e6")) // entry 6
            updateCatalogLRecord(con, stmt, mfr, category, manufacturerCode, "Entry6", entry);
          else
          if(op.equals("e7")) // entry 7
            updateCatalogLRecord(con, stmt, mfr, category, manufacturerCode, "Entry7", entry);
          else
          if(op.equals("e8")) // entry 8
            updateCatalogLRecord(con, stmt, mfr, category, manufacturerCode, "Entry8", entry);
          else
          if(op.equals("e9")) // entry 9
            updateCatalogLRecord(con, stmt, mfr, category, manufacturerCode, "Entry9", entry);
          else
          if(op.equals("e0")) // entry 10
            updateCatalogLRecord(con, stmt, mfr, category, manufacturerCode, "Entry10", entry);
          else
          if(op.equals("do")) // download
            updateCatalogRecord(con, stmt, mfr, category, "Download", entry);
          else
          if(op.equals("eu")) // external URL
            updateCatalogRecord(con, stmt, mfr, category, "ExternalURL", entry);
          else
          if(op.equals("cl")) // category link
            updateCatalogRecord(con, stmt, mfr, category, "CategoryLink", entry);
          else
          if(op.equals("or")) // order by description
          {
            if(entry.length() == 0)
              entry = "Y";
            updateCatalogRecord(con, stmt, mfr, category, "OrderByDescription", entry);
          }
          else
          if(op.equals("d1")) // description
            updateCatalogLRecord(con, stmt, mfr, category, manufacturerCode, "Description", entry);
          else
          if(op.equals("d2")) // description 2
            updateCatalogLRecord(con, stmt, mfr, category, manufacturerCode, "Description2", entry);
          else
          if(op.equals("t1")) // text
          {
            entryT1 += entry;
            if(firstT1)
              firstT1 = false;
          }
          else
          if(op.equals("t2")) // text 2
          {
            entryT2 += entry;
            if(firstT2)
              firstT2 = false;
          }
        }

        tmp = fh.readLine();
      }   
      
      if(! firstT1) // some text from previous category
        updateCatalogRecord(con, stmt, mfr, category, "Text", entryT1);
          
      if(! firstT2) // some text from previous category
        updateCatalogRecord(con, stmt, mfr, category, "Text2", entryT2);

      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      scoutln(out, bytesOut, "<tr><td><p>Error: " + e + "</td></tr>");
      if(stmt != null) stmt.close();
    }
  }

  //--------------------------------------------------------------------------------------------------------------------------------------------------
  private String sanitise(String thisEntry) throws Exception
  {
    int x=0, len = thisEntry.length();
    String s="";
    while(x < len)
    {
      if(thisEntry.charAt(x) == '"')
        s += "''";
      else s += thisEntry.charAt(x);
       
      ++x;
    }
     
    return s;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void removeExistingRecords(Connection con, Statement stmt, String mfr) throws Exception
  {
    stmt = con.createStatement();
    
    String q = "DELETE FROM catalog WHERE Manufacturer = '" + mfr + "'";

    System.out.println(q);
    stmt.executeUpdate(q);

    if(stmt != null) stmt.close();
    
    stmt = con.createStatement();
    
    q = "DELETE FROM catalogl WHERE Manufacturer = '" + mfr + "'";
    
    System.out.println(q);
    stmt.executeUpdate(q);

    if(stmt != null) stmt.close();
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void createNewCatalogRecord(Connection con, Statement stmt, String mfr, String category) throws Exception
  {
    stmt = con.createStatement();
    
    String q = "INSERT INTO catalog ( Manufacturer, Category, Chapter, Page, Title, ImageSmall, ImageSmall2, Imagelarge, Download, ExternalURL, "
             + "Heading1, Heading2, Heading3, Heading4, Heading5, Heading6, Heading7, Heading8, Heading9, Heading10, NoPrices, NoAvailability, "
             + "CategoryLink, Text, Text2, OrderByDescription ) "
             + "VALUES ('" + mfr + "', '" + category + "', '0', '0', '', '', '', '', '0', '', '', '', '', '', '', '', '', '', '', '', 'N', 'N', '0', "
             + "'', '', 'N' )";
          
    System.out.println(q);
    stmt.executeUpdate(q);

    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void createNewCatalogLRecord(Connection con, Statement stmt, String mfr, String category, String mfrCode) throws Exception
  {
    stmt = con.createStatement();
    
    String q = "INSERT INTO catalogl ( Manufacturer, Category, ManufacturerCode, Entry1, Entry2, Entry3, Entry4, Entry5, Entry6, Entry7, Entry8, "
             + "Entry9, Entry10, Description, Description2 ) "
             + "VALUES ('" + mfr + "', '" + category + "', '" + mfrCode + "', '', '', '', '', '', '', '', '', '', '', '', '' )";
          
    System.out.println(q);
    stmt.executeUpdate(q);

    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void updateCatalogRecord(Connection con, Statement stmt, String mfr, String category, String fieldName, String entry) throws Exception
  {
    entry = sanitise(entry);
            
    stmt = con.createStatement();

    String q = "UPDATE catalog SET " + fieldName + " = '" + generalUtils.sanitiseForSQL(entry) + "' WHERE Manufacturer = '" + mfr + "' AND Category = '"
             + category + "' ";

    System.out.println(q);
    stmt.executeUpdate(q);

    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void updateCatalogLRecord(Connection con, Statement stmt, String mfr, String category, String mfrCode, String fieldName, String entry)
                                    throws Exception
  {
    entry = sanitise(entry);

    stmt = con.createStatement();

    String q = "UPDATE catalogl SET " + fieldName + " = '" + generalUtils.sanitiseForSQL(entry) + "' WHERE Manufacturer = '" + mfr + "' AND Category = '"
             + category + "' AND ManufacturerCode = '" + mfrCode + "' ";

    System.out.println(q);
    stmt.executeUpdate(q);

    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
