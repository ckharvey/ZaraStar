// =======================================================================================================================================================================================================
// System: ZaraStar Utils: Create PDF Catalog
// Module: CatalogPDFCreateUpdate.java
// Author: C.K.Harvey
// Copyright (c) 1999-2009 Christopher Harvey. All Rights Reserved.
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
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

public class CatalogPDFCreateUpdate extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  Wiki wiki = new Wiki();
  
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="", p3="";

    try
    {
      unm = req.getParameter("unm");
      sid = req.getParameter("sid");
      uty = req.getParameter("uty");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");
      p1  = req.getParameter("p1"); // mfr
      p2  = req.getParameter("p2"); // type (user-created, or Zara)
      p3  = req.getParameter("p3"); // include desc2

      if(p1 == null) p1 = "";
      if(p3 == null) p3 = "Y";

      doIt(out, req, res, unm, sid, uty, men, den, dnm, bnm, p1, p2, p3, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "CatalogPDFCreateUpdate", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2038, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush(); 
    }
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, HttpServletResponse res, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String mfr, String type, String include, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');
    String workingDir    = directoryUtils.getUserDir('W', dnm, unm);
    String textsDir      = directoryUtils.getTextsDir(dnm);
    String imageLibDir   = directoryUtils.getImagesDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null, stmt2 = null, stmt3 = null;
    ResultSet rs=null, rs2 = null, rs3 = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 2038, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "2038b", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2038, bytesOut[0], 0, "ACC:" + mfr);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "2038b", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2038, bytesOut[0], 0, "SID:" + mfr);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    boolean includeDesc2;
    if(include.equals("Y"))
      includeDesc2 = true;
    else includeDesc2 = false;    
    
    if(type.equals("S"))
    {
      createSC(con, stmt, stmt2, stmt3, rs, rs2, rs3, workingDir + mfr + ".pdf", mfr, type, includeDesc2, dnm, textsDir, imageLibDir, localDefnsDir, defnsDir);
    }
    else
    {
      create(con, stmt, stmt2, rs, rs2, workingDir + mfr + ".pdf", mfr, type, includeDesc2, dnm, textsDir, imageLibDir, localDefnsDir, defnsDir);
    }

    download(res, workingDir, mfr + ".pdf", bytesOut);
    
    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 2038, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), mfr);
    if(con != null) con.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
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

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void create(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String fileName, String mfr, String type, boolean includeDesc2, String dnm, String textsDir, String imageLibDir, String localDefnsDir,
                      String defnsDir) throws Exception
  {
    try
    {
          
    Document document = new Document();

    PdfWriter.getInstance(document, new FileOutputStream(fileName));

    document.open();
    
    float[] widths = { 1f, 1f, 3f, 1f };
    PdfPTable table = new PdfPTable(widths);
    
    table.setWidthPercentage(100);
    
    table.setHeaderRows(1);
    
    String logo = wiki.getLogoFromStyling(dnm, localDefnsDir, defnsDir);

    PdfPCell h1 = new PdfPCell(Image.getInstance(imageLibDir + logo), false);
    h1.setGrayFill(0.9f);
    h1.setPadding(10);
    h1.setColspan(3);
    table.addCell(h1);

    PdfPCell h2 = new PdfPCell(new Paragraph(mfr + "\n" + generalUtils.todaySQLFormat(localDefnsDir, defnsDir)));
    h2.setGrayFill(0.8f);
    h2.setColspan(1);
    h2.setHorizontalAlignment(Element.ALIGN_RIGHT);
    table.addCell(h2);
   
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT CategoryCode, Image, Description, Download, Text, CategoryLink, Text2, NoPrices, NoAvailability, URL, WikiPage, OrderByDescription FROM stockcat WHERE Manufacturer = '" + mfr + "' ORDER BY Page, Description");
    
    String code, image, catDesc, download, text, categoryLink, text2, noPrices, noAvailability, url, wikiPage, order, itemCode, mfrCode, desc, desc2, rrp, salesCurrency;
    int[] row = new int[1];  row[0] = 1;
    PdfPCell cell;

    while(rs.next())
    {
      code           = rs.getString(1);
      image          = rs.getString(2);
      catDesc        = rs.getString(3);
      download       = rs.getString(4);
      text           = rs.getString(5);
      categoryLink   = rs.getString(6);
      text2          = rs.getString(7);
      noPrices       = rs.getString(8);
      noAvailability = rs.getString(9);
      url            = rs.getString(10);
      wikiPage       = rs.getString(11);
      order          = rs.getString(12);

      if(image != null && image.length() > 0 && generalUtils.fileExists(imageLibDir + image))
        cell = new PdfPCell(Image.getInstance(imageLibDir + image), true);
      else cell = new PdfPCell(new Paragraph(""));
      cell.setColspan(2);
      cell.setPadding(10);
      table.addCell(cell);
    
      if(text != null && text.length() > 0 && generalUtils.fileExists(textsDir + text))
      {
        cell = new PdfPCell(new Paragraph(fetch(textsDir + text)));
        cell.setColspan(2);
        table.addCell(cell);
      }
      else cell = new PdfPCell(new Paragraph(""));
    
      if(order.equals("S")) // order by size
      {
        stmt2 = con.createStatement();

        rs2 = stmt2.executeQuery("SELECT Description, ItemCode FROM stock WHERE CategoryCode = '" + code + "' AND Status != 'C'");

        int numEntries = 0;
        byte[] entries = new byte[1000];
        int[] entriesSize = new int[1];  entriesSize[0] = 1000;
        byte[] itemCodes = new byte[1000];
        int[] itemCodesSize = new int[1];  itemCodesSize[0] = 1000;
        byte[] newItem = new byte[82];
    
        while(rs2.next())
        {
          generalUtils.strToBytes(newItem, (rs2.getString(1) + "\001"));
          entries   = generalUtils.appendToList(true, newItem, entries, entriesSize);
          generalUtils.strToBytes(newItem, (rs2.getString(2) + "\001"));      
          itemCodes = generalUtils.appendToList(true, newItem, itemCodes, itemCodesSize);
          ++numEntries;
        }
    
        String sortedItemCodes = generalUtils.orderBySize(entries, itemCodes, numEntries, 82);
    
        if(rs2   != null) rs2.close();
        if(stmt2 != null) stmt2.close();
          
        String thisItemCode;
        int y=0;
        boolean[] first = new boolean[1];  first[0] = true;
        for(int x=0;x<numEntries;++x)
        {
          thisItemCode = "";
          while(sortedItemCodes.charAt(y) != '\001')
            thisItemCode += sortedItemCodes.charAt(y++);
          ++y;

          if(thisItemCode.length() > 0) // just-in-case
          {
            stmt2 = con.createStatement();

            rs2 = stmt2.executeQuery("SELECT ItemCode, ManufacturerCode, Description, Description2, RRP, SalesCurrency FROM stock WHERE ItemCode = '" + generalUtils.sanitiseForSQL(thisItemCode) + "' AND Status != 'C'");

            if(rs2.next())
            {
              itemCode      = rs2.getString(1);
              mfrCode       = rs2.getString(2);
              desc          = rs2.getString(3);
              desc2         = rs2.getString(4);
              rrp           = rs2.getString(5);
              salesCurrency = rs2.getString(6);
      
              outputItem(includeDesc2, document, cell, table, row, first, itemCode, mfrCode, desc, desc2, rrp, salesCurrency);
            }
     
            if(rs2   != null) rs2.close();
            if(stmt2 != null) stmt2.close();
          }
        }
      }
      else // not ordered by size
      {
        stmt2 = con.createStatement();

        rs2 = stmt2.executeQuery("SELECT ItemCode, ManufacturerCode, Description, Description2, RRP, SalesCurrency FROM stock WHERE CategoryCode = '" + code + "' AND Status != 'C' ORDER BY ManufacturerCode");
        boolean[] first = new boolean[1];  first[0] = true;
      
        while(rs2.next())
        {
          itemCode      = rs2.getString(1);
          mfrCode       = rs2.getString(2);
          desc          = rs2.getString(3);
          desc2         = rs2.getString(4);
          rrp           = rs2.getString(5);
          salesCurrency = rs2.getString(6);
      
          outputItem(includeDesc2, document, cell, table, row, first, itemCode, mfrCode, desc, desc2, rrp, salesCurrency);
        }
        
        if(rs2   != null) rs2.close();
        if(stmt2 != null) stmt2.close();
      }
      
      cell = new PdfPCell(new Paragraph(""));
      cell.setGrayFill(0f);
      cell.setColspan(4);
      table.addCell(cell);
    }

    document.add(table);
        
    document.close();  
    }
    catch(Exception e) { System.out.println("2038b: " + e); }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void outputItem(boolean includeDesc2, Document document, PdfPCell cell, PdfPTable table, int[] row, boolean[] first, String itemCode, String mfrCode, String desc, String desc2, String rrp, String salesCurrency) throws Exception
  {
    if(first[0])
    {
      cell = new PdfPCell(new Paragraph("Item Code"));
      cell.setGrayFill(0.8f);
      cell.setColspan(1);
      table.addCell(cell);
      cell = new PdfPCell(new Paragraph("Manufacturer Code"));
      cell.setGrayFill(0.8f);
      cell.setColspan(1);
      table.addCell(cell);
      cell = new PdfPCell(new Paragraph("Description"));
      cell.setGrayFill(0.8f);
      cell.setColspan(1);
      table.addCell(cell);
      cell = new PdfPCell(new Paragraph("List Price"));
      cell.setGrayFill(0.8f);
      cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
      cell.setColspan(1);
      table.addCell(cell);
      first[0] = false;
    }

    if(row[0] % 50 == 0)
    {
      document.add(table);
      table.deleteBodyRows();
      table.setSkipFirstHeader(true);
    }      
      
    cell = new PdfPCell(new Paragraph(itemCode));
    table.addCell(cell);
        
    cell = new PdfPCell(new Paragraph(mfrCode));
    table.addCell(cell);

    if(includeDesc2)
      cell = new PdfPCell(new Paragraph(desc + " " + desc2));
    else cell = new PdfPCell(new Paragraph(desc));
    table.addCell(cell);
        
    cell = new PdfPCell(new Paragraph(salesCurrency + " " + generalUtils.doubleDPs(rrp, '2')));
    cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
    table.addCell(cell);
        
    ++row[0];
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String fetch(String fullPathName) throws Exception
  {
    String text = "";
    RandomAccessFile fh;
  
    if((fh = generalUtils.fileOpen(fullPathName)) != null)
    {
      String s;
      try
      {
        s = fh.readLine();
        while(s != null)
        {
          text += s;
          s = fh.readLine();
        }
      }
      catch(Exception ioErr)
      {
        generalUtils.fileClose(fh);
        return text;
      }
    }
    else
    {
      return text;
    }
    
    generalUtils.fileClose(fh);
    
    return cleanse(text);
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String cleanse(String text) throws Exception
  {
    // strip html from text
    int x = 0, y, len = text.length();
    String text2 = "", tag;
    char ch;
    while(x < len)
    {
      ch = text.charAt(x);
      if(ch == '<')
      {
        y = x;
        tag = "";
        while(y < len && text.charAt(y) != '>' && text.charAt(y) != ' ')
          tag += text.charAt(y++);
        
        if(tag.equals("<br") || tag.equals("<br/") || tag.equals("<br"))
        {
          text2 += '\n';
          while(y < len && text.charAt(y) != '>')
            ++y;
          x = y;
        }
        else
        if(tag.equals("<li"))
        {
          text2 += "\n* ";
          while(y < len && text.charAt(y) != '>')
            ++y;
          x = y;
        }
        else
        if(tag.startsWith("<font") || tag.startsWith("<span") || tag.startsWith("<pre") || tag.startsWith("<ul"))
        {
          while(y < len && text.charAt(y) != '>')
            ++y;
          x = y;
        }
        else
        if(tag.startsWith("</"))
        {
          if(tag.equals("</li"))//      || tag.equals("</ul"))
            ;
          else text2 += '\n';

          while(y < len && text.charAt(y) != '>')
            ++y;
          x = y;
        }
        else text2 += '<';
      }
      else
      if(ch == '&')
      {
        y = x;
        tag = "";
        while(y < len && text.charAt(y) != ';')
          tag += text.charAt(y++);
        
        if(tag.equals("&nbsp"))
        {
          text2 += ' ';
          x = y;
        }
        else
        if(tag.equals("&amp"))
        {
          text2 += '&';
          x = y;
        }
        else text2 += '&';
      }
      else text2 += text.charAt(x);
      
      ++x;
    }
    
    text2 = removeDoubleNewlines(text2);

    return text2;
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String removeDoubleNewlines(String text) throws Exception
  {
    int x = 0, len = text.length();
    String text2 = "";
    char ch;
    while(x < len)
    {
      ch = text.charAt(x);
      if(ch == '\n')
      {
        if((x + 1) < len && text.charAt(x + 1) == '\n')
          ;//++x;
        else text2 += '\n';
      }
      else text2 += ch;
      
      ++x;
    }

    return text2;
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void createSC(Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, ResultSet rs3, String fileName, String mfr, String type, boolean includeDesc2, String dnm, String textsDir, String imageLibDir,
                        String localDefnsDir, String defnsDir) throws Exception
  {
    String catalogImagesDir = directoryUtils.getCatalogImagesDir(dnm, mfr);

    String logo = wiki.getLogoFromStyling(dnm, localDefnsDir, defnsDir);

    Document document = new Document();

    PdfWriter.getInstance(document, new FileOutputStream(fileName));

    document.open();
    
    int row;
    PdfPCell cell;

    float[] widths = { 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f };
    PdfPTable table = new PdfPTable(widths);
    
    table.setWidthPercentage(100);
    
    table.setHeaderRows(1);
    
    PdfPCell h1 = new PdfPCell(Image.getInstance(imageLibDir + logo), false);
    h1.setGrayFill(0.9f);
    h1.setPadding(10);
    h1.setColspan(12);
    table.addCell(h1);
    
    PdfPCell h2 = new PdfPCell(new Paragraph(mfr));
    h2.setGrayFill(0.8f);
    h2.setColspan(2);
    h2.setHorizontalAlignment(Element.ALIGN_RIGHT);
    table.addCell(h2);

    String mfrCode, entry1, entry2, entry3, entry4, entry5, entry6, entry7, entry8, entry9, entry10, desc, desc2;
    String itemCode, salesCurrency, rrp, sellPrice1, sellPrice2, sellPrice3, sellPrice4;

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT Category, ImageSmall, Title, Download, Text, CategoryLink, Text2, NoPrices, NoAvailability, "
                         + "ExternalURL, OrderByDescription, Heading1, Heading2, Heading3, Heading4, Heading5, Heading6, Heading7, "
                         + "Heading8, Heading9, Heading10 FROM catalog WHERE Manufacturer = '" + generalUtils.sanitiseForSQL(mfr) + "' ORDER BY Title");
    
    String category, imageSmall, title, download, text, categoryLink, text2, noPrices, noAvailability, url, orderByDesc, heading1, heading2, heading3, heading4, heading5, heading6, heading7, heading8, heading9, heading10;
    boolean wantEntry1, wantEntry2, wantEntry3, wantEntry4, wantEntry5, wantEntry6, wantEntry7, wantEntry8, wantEntry9, wantEntry10;
    int span1, span2, span3, span4, span5, span6, span7, span8, span9, span10;
    
    while(rs.next())
    {
      category       = rs.getString(1);
      imageSmall     = rs.getString(2);
      title          = rs.getString(3);
      download       = rs.getString(4);
      text           = rs.getString(5);
      categoryLink   = rs.getString(6);
      text2          = rs.getString(7);
      noPrices       = rs.getString(8);
      noAvailability = rs.getString(9);
      url            = rs.getString(10);
      orderByDesc    = rs.getString(11);
      heading1       = rs.getString(12);
      heading2       = rs.getString(13);
      heading3       = rs.getString(14);
      heading4       = rs.getString(15);
      heading5       = rs.getString(16);
      heading6       = rs.getString(17);
      heading7       = rs.getString(18);
      heading8       = rs.getString(19);
      heading9       = rs.getString(20);
      heading10      = rs.getString(21);

      if(heading1  != null && heading1.length() > 0)  wantEntry1  = true; else wantEntry1  = false;
      if(heading2  != null && heading2.length() > 0)  wantEntry2  = true; else wantEntry2  = false;
      if(heading3  != null && heading3.length() > 0)  wantEntry3  = true; else wantEntry3  = false;
      if(heading4  != null && heading4.length() > 0)  wantEntry4  = true; else wantEntry4  = false;
      if(heading5  != null && heading5.length() > 0)  wantEntry5  = true; else wantEntry5  = false;
      if(heading6  != null && heading6.length() > 0)  wantEntry6  = true; else wantEntry6  = false;
      if(heading7  != null && heading7.length() > 0)  wantEntry7  = true; else wantEntry7  = false;
      if(heading8  != null && heading8.length() > 0)  wantEntry8  = true; else wantEntry8  = false;
      if(heading9  != null && heading9.length() > 0)  wantEntry9  = true; else wantEntry9  = false;
      if(heading10 != null && heading10.length() > 0) wantEntry10 = true; else wantEntry10 = false;

      row = 1;

      if(imageSmall != null && imageSmall.length() > 0)
        cell = new PdfPCell(Image.getInstance(catalogImagesDir + imageSmall), true);
      else cell = new PdfPCell(new Paragraph(""));
      cell.setColspan(4);
      cell.setPadding(10);
      table.addCell(cell);
          
      cell = new PdfPCell(new Paragraph(cleanse(text)));
      cell.setColspan(10);
      table.addCell(cell);
    
      span1 = span2 = span3 = span4 = span5 = span6 = span7 = span8 = span9 = span10 = 0;

      if(! wantEntry1) // no cols
        span1 = 0;
      else
      if(! wantEntry2) // only 1 col
        span1 = 14;
      else
      if(! wantEntry3) // only 2 cols
      {
        span1 = 7;
        span2 = 7;
      }
      else
      if(! wantEntry4) // only 3 cols
      {
        span1 = 4;
        span2 = 5;
        span3 = 5;
      }      
      else
      if(! wantEntry5) // only 4 cols
      {
        span1 = 3;
        span2 = 3;
        span3 = 4;
        span4 = 4;
      }      
      else
      if(! wantEntry6) // only 5 cols
      {
        span1 = 2;
        span2 = 3;
        span3 = 3;
        span4 = 3;
        span5 = 3;
      }      
      else
      if(! wantEntry7) // only 6 cols
      {
        span1 = 2;
        span2 = 2;
        span3 = 2;
        span4 = 2;
        span5 = 3;
        span6 = 3;
      }      
      else
      if(! wantEntry8) // only 7 cols
      {
        span1 = 2;
        span2 = 2;
        span3 = 2;
        span4 = 2;
        span5 = 2;
        span6 = 2;
        span7 = 2;
      }      
      else
      if(! wantEntry9) // only 8 cols
      {
        span1 = 1;
        span2 = 1;
        span3 = 2;
        span4 = 2;
        span5 = 2;
        span6 = 2;
        span7 = 2;
        span8 = 2;
      }      
      else
      if(! wantEntry10) // only 9 cols
      {
        span1 = 1;
        span2 = 1;
        span3 = 1;
        span4 = 1;
        span5 = 2;
        span6 = 2;
        span7 = 2;
        span8 = 2;
        span9 = 2;
      }      
      else // all 10 cols
      {
        span1 = 1;
        span2 = 1;
        span3 = 1;
        span4 = 1;
        span5 = 1;
        span6 = 1;
        span7 = 2;
        span8 = 2;
        span9 = 2;
        span10 = 2;
      }      
      
      stmt2 = con.createStatement();
    
      rs2 = stmt2.executeQuery("SELECT ManufacturerCode, Entry1, Entry2, Entry3, Entry4, Entry5, Entry6, Entry7, Entry8, Entry9, Entry10, Description, Description2 FROM catalogl WHERE Manufacturer = '" + generalUtils.sanitiseForSQL(mfr)
                             + "' AND Category = '" + category + "' ORDER BY ManufacturerCode");
      boolean first = true;

      while(rs2.next())
      {
        mfrCode = rs2.getString(1);
        entry1  = rs2.getString(2);
        entry2  = rs2.getString(3);
        entry3  = rs2.getString(4);
        entry4  = rs2.getString(5);
        entry5  = rs2.getString(6);
        entry6  = rs2.getString(7);
        entry7  = rs2.getString(8);
        entry8  = rs2.getString(9);
        entry9  = rs2.getString(10);
        entry10 = rs2.getString(11);
        desc    = rs2.getString(12);
        desc2   = rs2.getString(13);

        if(first)
        {
          cell = new PdfPCell(new Paragraph("Item Code"));
          cell.setGrayFill(0.8f);
          cell.setColspan(2);
          table.addCell(cell);
          cell = new PdfPCell(new Paragraph("Manufacturer Code"));
          cell.setGrayFill(0.8f);
          cell.setColspan(2);
          table.addCell(cell);
          cell = new PdfPCell(new Paragraph("Description"));
          cell.setGrayFill(0.8f);
          cell.setColspan(8);
          table.addCell(cell);
          cell = new PdfPCell(new Paragraph("List Price"));
          cell.setGrayFill(0.8f);
          cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
          cell.setColspan(2);
          table.addCell(cell);
          first = false;
        }
          
        stmt3 = con.createStatement();
        
        rs3 = stmt3.executeQuery("SELECT ItemCode, RRP, SellPrice1, SellPrice2, SellPrice3, SellPrice4, SalesCurrency FROM stock WHERE Manufacturer = '" + generalUtils.sanitiseForSQL(mfr) + "' AND ManufacturerCode = '" + generalUtils.sanitiseForSQL(mfrCode)
                               + "' AND Status != 'C'");

        boolean first2 = true;
        
        if(rs3.next())
        {
          itemCode      = rs3.getString(1);
          rrp           = rs3.getString(2);
          sellPrice1    = rs3.getString(3);
          sellPrice2    = rs3.getString(4);
          sellPrice3    = rs3.getString(5);
          sellPrice4    = rs3.getString(6);
          salesCurrency = rs3.getString(7);
      
          if(row % 50 == 0)
          {
            document.add(table);
            table.deleteBodyRows();
            table.setSkipFirstHeader(true);
          }      
         
          // output first line 
          cell = new PdfPCell(new Paragraph(itemCode));
          cell.setColspan(2);
          table.addCell(cell);
        
          cell = new PdfPCell(new Paragraph(mfrCode));
          cell.setColspan(2);
          table.addCell(cell);

          if(includeDesc2)
            cell = new PdfPCell(new Paragraph(cleanse(desc + " " + desc2)));
          else cell = new PdfPCell(new Paragraph(cleanse(desc)));
          
          cell.setColspan(8);
          table.addCell(cell);

          cell = new PdfPCell(new Paragraph(salesCurrency + " " + generalUtils.doubleDPs(rrp, '2')));
          cell.setColspan(2);
          cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
          table.addCell(cell);

          // output second line 

          if(first2)
          {
            if(wantEntry1)
            {
              cell = new PdfPCell(new Paragraph(cleanse(heading1)));
              cell.setGrayFill(0.8f);
              cell.setColspan(span1);
              table.addCell(cell);
            }
          
            if(wantEntry2)
            {
              cell = new PdfPCell(new Paragraph(cleanse(heading2)));
              cell.setGrayFill(0.8f);
              cell.setColspan(span2);
              table.addCell(cell);
            }
          
            if(wantEntry3)
            {
              cell = new PdfPCell(new Paragraph(cleanse(heading3)));
              cell.setGrayFill(0.8f);
              cell.setColspan(span3);
              table.addCell(cell);
            }
          
            if(wantEntry4)
            {
              cell = new PdfPCell(new Paragraph(cleanse(heading4)));
              cell.setGrayFill(0.8f);
              cell.setColspan(span4);
              table.addCell(cell);
            }
          
            if(wantEntry5)
            {
              cell = new PdfPCell(new Paragraph(cleanse(heading5)));
              cell.setGrayFill(0.8f);
              cell.setColspan(span5);
              table.addCell(cell);
            }
          
            if(wantEntry6)
            {
              cell = new PdfPCell(new Paragraph(cleanse(heading6)));
              cell.setGrayFill(0.8f);
              cell.setColspan(span6);
              table.addCell(cell);
            }
          
            if(wantEntry7)
            {
              cell = new PdfPCell(new Paragraph(cleanse(heading7)));
              cell.setGrayFill(0.8f);
              cell.setColspan(span7);
              table.addCell(cell);
            }
          
            if(wantEntry8)
            {
              cell = new PdfPCell(new Paragraph(cleanse(heading8)));
              cell.setGrayFill(0.8f);
              cell.setColspan(span8);
              table.addCell(cell);
            }
          
            if(wantEntry9)
            {
              cell = new PdfPCell(new Paragraph(cleanse(heading9)));
              cell.setGrayFill(0.8f);
              cell.setColspan(span9);
              table.addCell(cell);
            }
          
            if(wantEntry10)
            {
              cell = new PdfPCell(new Paragraph(cleanse(heading10)));
              cell.setGrayFill(0.8f);
              cell.setColspan(span10);
              table.addCell(cell);
            }

            first2 = false;
          }


          if(wantEntry1)
          {
            cell = new PdfPCell(new Paragraph(entry1));
            cell.setColspan(span1);
            table.addCell(cell);
          }

          if(wantEntry2)
          {
            cell = new PdfPCell(new Paragraph(entry2));
            cell.setColspan(span2);
            table.addCell(cell);
          }

          if(wantEntry3)
          {
            cell = new PdfPCell(new Paragraph(entry3));
            cell.setColspan(span3);
            table.addCell(cell);
          }

          if(wantEntry4)
          {
            cell = new PdfPCell(new Paragraph(entry4));
            cell.setColspan(span4);
            table.addCell(cell);
          }

          if(wantEntry5)
          {
            cell = new PdfPCell(new Paragraph(entry5));
            cell.setColspan(span5);
            table.addCell(cell);
          }

          if(wantEntry6)
          {
            cell = new PdfPCell(new Paragraph(entry6));
            cell.setColspan(span6);
            table.addCell(cell);
          }

          if(wantEntry7)
          {
            cell = new PdfPCell(new Paragraph(entry7));
            cell.setColspan(span7);
            table.addCell(cell);
          }

          if(wantEntry8)
          {
            cell = new PdfPCell(new Paragraph(entry8));
            cell.setColspan(span8);
            table.addCell(cell);
          }

          if(wantEntry9)
          {
            cell = new PdfPCell(new Paragraph(entry9));
            cell.setColspan(span9);
            table.addCell(cell);
          }

          if(wantEntry10)
          {
            cell = new PdfPCell(new Paragraph(entry10));
            cell.setColspan(span10);
            table.addCell(cell);
          }

          ++row;
        }

        if(rs3   != null) rs3.close();
        if(stmt3 != null) stmt3.close();
      }

      if(rs2   != null) rs2.close();
      if(stmt2 != null) stmt2.close();

      cell = new PdfPCell(new Paragraph(""));
      cell.setGrayFill(0f);
      cell.setColspan(14);
      table.addCell(cell);
    }

    document.add(table);
        
    document.close();
  
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

}
