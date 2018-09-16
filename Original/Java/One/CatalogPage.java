// =======================================================================================================================================================================================================
// System: ZaraStar Catalogs: For a catalog page
// Module: CatalogPage.java
// Author: C.K.Harvey
// Copyright (c) 2002-08 Christopher Harvey. All Rights Reserved.
// Where: Local server
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

public class CatalogPage extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  Profile profile = new Profile();
  AdminControlUtils adminControlUtils = new AdminControlUtils();
  
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

      unm = req.getParameter("unm");
      sid = req.getParameter("sid");
      uty = req.getParameter("uty");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");
      p1  = req.getParameter("p1"); // mfr
      p2  = req.getParameter("p2"); // page
      
      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, bytesOut);
    }
    catch(Exception e)
    {
      StringBuffer url = req.getRequestURL();
      int x=0;
      if(url.charAt(x) == 'h' || url.charAt(x) == 'H')
        x += 7;
      String urlBit="";
      while(url.charAt(x) != '\000' && url.charAt(x) != ',' && url.charAt(x) != '/')
        urlBit += url.charAt(x++);
 
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "ProductCatalogsAdmin", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2003, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1, String p2, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String iDir             = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String textsDir         = directoryUtils.getTextsDir(dnm);
    String imagesDir        = directoryUtils.getImagesDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;
    
    if((uty.equals("R") && ! adminControlUtils.notDisabled(con, stmt, rs, 902)) || (uty.equals("A") && ! adminControlUtils.notDisabled(con, stmt, rs, 802)) || (uty.equals("I") && ! authenticationUtils.verifyAccess(con, stmt, rs, req, 2003, unm, uty, dnm, localDefnsDir, defnsDir)))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "CatalogPage", iDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2003, bytesOut[0], 0, "ACC:");
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "CatalogPage", iDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2003, bytesOut[0], 0, "SID:");
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    set(con, stmt, rs, out, req, p1, p2, unm, sid, uty, men, den, dnm, bnm, imagesDir, textsDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 2003, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String mfr, String page, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String imagesDir,
                   String textsDir, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Catalog Page</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    if(! uty.equals("I") && (adminControlUtils.notDisabled(con, stmt, rs, 902) || adminControlUtils.notDisabled(con, stmt, rs, 802)))
    {
      scoutln(out, bytesOut, "function view(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/StockEnquiryExternal?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+p1;}");
    }
    else
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 1001, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function view(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/MainPageUtils1a?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+p1;}");
    }

    scoutln(out, bytesOut, "function download(which){");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/LibraryDownloaCasual?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+which;}");

    scoutln(out, bytesOut, "function wiki(page){");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/_6600?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den
                         + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+page;}");

    scoutln(out, bytesOut, "function display(page){");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/CatalogPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den
                         + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + generalUtils.sanitise(mfr) + "&p2=\"+page;}");

    
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 6103, unm, uty, dnm, localDefnsDir, defnsDir))
    {    
      scoutln(out, bytesOut, "function edit(fileName){var p3=sanitise(fileName);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/TextEditText?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den
                           + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=&p1=&p3=\"+p3;}");
    }
    
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 6501, unm, uty, dnm, localDefnsDir, defnsDir))
    {    
      scoutln(out, bytesOut, "function list(dir){var p1=sanitise(dir);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/ImagesListDirectory?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den
                           + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+p1;}");
    }
    
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 121, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function addToCart(code){var p1=sanitise(code);");
      if(!uty.equals("I"))scoutln(out, bytesOut, "alert('Coming Soon');else ");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/ProductCartAddToCart?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den
                           + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=L&p1=\"+p1;}");
    }

    scoutln(out, bytesOut, "function sanitise(code){");
    scoutln(out, bytesOut, "var code2='';var x;var len=code.length;");
    scoutln(out, bytesOut, "for(x=0;x<len;++x)");
    scoutln(out, bytesOut, "if(code.charAt(x)=='#')code2+='%23';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='\"')code2+='%22';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='&')code2+='%26';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='%')code2+='%25';");
    scoutln(out, bytesOut, "else if(code.charAt(x)==' ')code2+='%20';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='?')code+='%3F';");
    scoutln(out, bytesOut, "else code2+=code.charAt(x);");
    scoutln(out, bytesOut, "return code2};");

    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\""
                         + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\"></head>");
  
    int[] hmenuCount = new int[1];
 
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "2003", "", "ProductCatalogsAdmin", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir,
                          hmenuCount, bytesOut);

    if(uty.equals("R"))
    {
      String[] name         = new String[1];
      String[] companyName  = new String[1];
      String[] accessRights = new String[1];
      int i = unm.indexOf("_");

      profile.getExternalAccessNameCompanyAndRights(con, stmt, rs, unm.substring(0, i), name, companyName, accessRights);

      pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", mfr + " Page " + page + " for " + name[0] + " of " + companyName[0], "2003", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);
    }
    else
    if(uty.equals("I"))
      pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", mfr + " Page " + page, "2003", unm, sid, uty, men, den, dnm, bnm, hmenuCount,bytesOut);
    else
    {
      pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", mfr + " Page " + page + " (Casual User)", "2003", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);
    }

    scoutln(out, bytesOut, "<form><table id=\"page\" width=100% border=0>");

    showPage(out, req, mfr, page, unm, uty, dnm, men, imagesDir, textsDir, localDefnsDir, defnsDir, bytesOut);
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
  
    scoutln(out, bytesOut, "</table></form>");     scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }
    
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void showPage(PrintWriter out, HttpServletRequest req, String mfr, String page, String unm, String uty, String dnm, String men, String imagesDir, String textsDir, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    String userName = directoryUtils.getMySQLUserName();
    String passWord = directoryUtils.getMySQLPassWord();
    
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + userName + "&password=" + passWord);
    Statement stmt = con.createStatement(), stmt2 = null, stmt3 = null;
    ResultSet rs2 = null, rs3 = null;

    String showToWeb = "";
    if(! uty.equals("I"))
      showToWeb = " AND ShowToWeb = 'Y'";

    ResultSet rs = stmt.executeQuery("SELECT CategoryCode, Image, Description, Download, Text, CategoryLink, Text2, NoPrices, NoAvailability, Style, URL, WikiPage, OrderByDescription FROM stockcat "
                                   + "WHERE Manufacturer = '" + mfr + "' AND Page = '" + page + "' ORDER BY Description");
    
    boolean first = true;
    String code, image, desc, download, text, categoryLink, text2, noPrices, noAvailability, style, url, wikiPage, order;
    while(rs.next())
    {
      code           = rs.getString(1);
      image          = rs.getString(2);
      desc           = rs.getString(3);
      download       = rs.getString(4);
      text           = rs.getString(5);
      categoryLink   = rs.getString(6);
      text2          = rs.getString(7);
      noPrices       = rs.getString(8);
      noAvailability = rs.getString(9);
      style          = rs.getString(10);
      url            = rs.getString(11);
      wikiPage       = rs.getString(12);
      order          = rs.getString(13);

      if(! first)
        scoutln(out, bytesOut, "<hr>");
      else first = false;
        
      showCategory(con, stmt2, stmt3, rs2, rs3, out, req, mfr, code, image, desc, download, text, categoryLink, text2, noPrices, noAvailability, style, url,
                   wikiPage, order, showToWeb, unm, uty, dnm, men, imagesDir, textsDir, localDefnsDir, defnsDir, bytesOut);
    }
   
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    if(con  != null) con.close(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
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
    return text;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private String dirPartOf(String image) throws Exception
  {
    int len = image.length();
    if(len == 0)
      return "";
    --len;
    while(len > 0 && image.charAt(len) != '/')
      --len;
    return image.substring(0, len);  
  }
        
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void showCategory(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, HttpServletRequest req,
                            String mfr, String code, String image, String catDesc, String download, String text, String categoryLink, String text2,
                            String noPrices, String noAvailability, String style, String url, String wikiPage, String order, String showToWeb, String unm, String uty,
                            String dnm, String men, String imagesDir, String textsDir, String localDefnsDir, String defnsDir, int[] bytesOut)
                            throws Exception
  {
    int band, i = unm.indexOf("_");

    if(i != -1)
    {
      String[] customerCode = new String[1];
      profile.getExternalAccessNameCustomerCode(con, stmt, rs, unm.substring(0, i), dnm, localDefnsDir, defnsDir, customerCode);
      Customer customer = new Customer();
      band = customer.getPriceBand(con, stmt, rs, customerCode[0]);
    }
    else
    if(uty.equals("I"))
      band = 888;
    else band = 0; // anon    

    scoutln(out, bytesOut, "<table id=\"page\" border=0>");
    scoutln(out, bytesOut, "<tr><td colspan=2><p><b>" + catDesc + "</td></tr>");

    if(text.length() == 0) // no text
        text = " ";
    
    if(image.length() > 0) // and image exists
    {
      scoutln(out, bytesOut, "<tr><td valign=top><p><img src=\"http://" + men + imagesDir + "/" + image + "\" border=1>");
      if(! wikiPage.equals("0")) // wikiPage exists
        scoutln(out, bytesOut, "<br><p><a href=\"javascript:wiki('" + wikiPage + "')\">View</a> more detail");
      if(! download.equals("0")) // download exists
        scoutln(out, bytesOut, "<br><p><a href=\"javascript:download('" + download + "')\">Download</a> more info");
      if(url.length() > 0)
        scoutln(out, bytesOut, "<br><p><a href=\"http://" + url + "\">Link</a> to Manufacturer");
      if(! categoryLink.equals("0"))
        scoutln(out, bytesOut, "<br><p><a href=\"javascript:display('" + categoryLink + "')\">Display</a> related items");

      if(text.length() > 0)
      {      
        if(authenticationUtils.verifyAccess(con, stmt, rs, req, 6103, unm, uty, dnm, localDefnsDir, defnsDir))
          scoutln(out, bytesOut, "<br><p><a href=\"javascript:edit('" + text + "')\">Edit</a> Text");
      }

      if(authenticationUtils.verifyAccess(con, stmt, rs, req, 6501, unm, uty, dnm, localDefnsDir, defnsDir))
        scoutln(out, bytesOut, "<br><p><a href=\"javascript:list('" + dirPartOf(image) + "')\">List</a> Images");
        
      scoutln(out, bytesOut, "</td>");
        
      scoutln(out, bytesOut, "<td valign=top><p>" + fetch(textsDir + text) + "</td></tr>");

      scoutln(out, bytesOut, "<tr><td></td><td>");
    }
    else // no image
    {
      scoutln(out, bytesOut, "<tr><td valign=top><p>" + fetch(textsDir + text) + "</td></tr>");
      if(! wikiPage.equals("0")) // wikiPage exists
        scoutln(out, bytesOut, "<tr><td colspan=2><p><a href=\"javascript:wiki('" + wikiPage + "')\">View</a> more detail</td></tr>");
      if(! download.equals("0")) // download exists
        scoutln(out, bytesOut, "<tr><td><p><a href=\"javascript:download('" + download + "')\">Download</a> more info</td></tr>");
      if(url.length() > 0)
        scoutln(out, bytesOut, "<tr><td colspan=2><p><a href=\"http://" + url + "\">Link</a> to Manufacturer</td></tr>");
        
      if(text.length() > 0)
      {      
        if(authenticationUtils.verifyAccess(con, stmt, rs, req, 6103, unm, uty, dnm, localDefnsDir, defnsDir))
          scoutln(out, bytesOut, "<tr><td colspan=2><p><a href=\"javascript:edit('" + text + "')\">Edit</a> text</td></tr>");
      }
        
      if(authenticationUtils.verifyAccess(con, stmt, rs, req, 6501, unm, uty, dnm, localDefnsDir, defnsDir))
        scoutln(out, bytesOut, "<tr><td colspan=2><p><a href=\"javascript:list('" + dirPartOf(image) + "')\">List</a> images</td></tr>");
        
      if(! categoryLink.equals("0"))
        scoutln(out, bytesOut, "<tr><td colspan=2><p><a href=\"javascript:display('" + categoryLink + "')\">Display</a> related items</td></tr>");
      scoutln(out, bytesOut, "<tr><td align=center>");
    }
    
    scoutln(out, bytesOut, "<table id='page' border=0>");
    scoutln(out, bytesOut, "<tr bgcolor='#E0E0E0'><td><p>Our Code</td><td><p>Manufacturer Code</td><td><p>Description</td>");

    scoutln(out, bytesOut, "</tr>");

    boolean ProductCart  = authenticationUtils.verifyAccess(con, stmt2, rs2, req,  121, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean StockEnquiryExternal = authenticationUtils.verifyAccess(con, stmt2, rs2, req, 2001, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean ProductStockRecord = authenticationUtils.verifyAccess(con, stmt2, rs2, req, 3001, unm, uty, dnm, localDefnsDir, defnsDir);

    String itemCode, mfrCode, desc, desc2, salesCurrency, rrp, sellPrice1, sellPrice2, sellPrice3, sellPrice4, thisItemCode;

    if(order.equals("S")) // size ordering
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT Description, ItemCode FROM stock WHERE CategoryCode = '" + code + "' " + showToWeb);

      int numEntries = 0;
      byte[] entries = new byte[1000];
      int[] entriesSize = new int[1];  entriesSize[0] = 1000;
      byte[] itemCodes = new byte[1000];
      int[] itemCodesSize = new int[1];  itemCodesSize[0] = 1000;
      byte[] newItem = new byte[82];
    
      while(rs.next())
      {
        generalUtils.strToBytes(newItem, (rs.getString(1) + "\001"));
        entries   = generalUtils.appendToList(true, newItem, entries, entriesSize);
        generalUtils.strToBytes(newItem, (rs.getString(2) + "\001"));      
        itemCodes = generalUtils.appendToList(true, newItem, itemCodes, itemCodesSize);
        ++numEntries;
      }

      String sortedItemCodes = "";
      if(numEntries > 0)
        sortedItemCodes = generalUtils.orderBySize(entries, itemCodes, numEntries, 82);

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
          
      int y=0;
      for(int x=0;x<numEntries;++x)
      {
        thisItemCode = "";
        while(sortedItemCodes.charAt(y) != '\001')
          thisItemCode += sortedItemCodes.charAt(y++);
        ++y;

        if(thisItemCode.length() > 0) // just-in-case
        {
          stmt = con.createStatement();

          rs = stmt.executeQuery("SELECT ItemCode, ManufacturerCode, Description, Description2, RRP, SellPrice1, SellPrice2, SellPrice3, SellPrice4, SalesCurrency FROM stock WHERE ItemCode = '" + generalUtils.sanitiseForSQL(thisItemCode) + "' "
                               + showToWeb);

          if(rs.next())
          {
            itemCode      = rs.getString(1);
            mfrCode       = rs.getString(2);
            desc          = rs.getString(3);
            desc2         = rs.getString(4);
            rrp           = rs.getString(5);
            sellPrice1    = rs.getString(6);
            sellPrice2    = rs.getString(7);
            sellPrice3    = rs.getString(8);
            sellPrice4    = rs.getString(9);
            salesCurrency = rs.getString(10);
      
            outputItem(out, itemCode, mfrCode, desc, desc2, salesCurrency, rrp, sellPrice1, sellPrice2, sellPrice3, sellPrice4, band, ProductCart, StockEnquiryExternal, ProductStockRecord, imagesDir, bytesOut);
          }
     
          if(rs   != null) rs.close();
          if(stmt != null) stmt.close();
        }
      }
    }
    else // not ordered by size
    {
      stmt = con.createStatement();

      if(order.equals("D"))
      {
        rs = stmt.executeQuery("SELECT ItemCode, ManufacturerCode, Description, Description2, RRP, SellPrice1, SellPrice2, SellPrice3, SellPrice4, SalesCurrency FROM stock WHERE CategoryCode = '" + code + "' " + showToWeb
                             + " ORDER BY Description");
      }
      else
      {
        rs = stmt.executeQuery("SELECT ItemCode, ManufacturerCode, Description, Description2, RRP, SellPrice1, SellPrice2, SellPrice3, SellPrice4, SalesCurrency FROM stock WHERE CategoryCode = '" + code + "' " + showToWeb
                             + " ORDER BY ManufacturerCode");
      }

      while(rs.next())
      {
        itemCode      = rs.getString(1);
        mfrCode       = rs.getString(2);
        desc          = rs.getString(3);
        desc2         = rs.getString(4);
        rrp           = rs.getString(5);
        sellPrice1    = rs.getString(6);
        sellPrice2    = rs.getString(7);
        sellPrice3    = rs.getString(8);
        sellPrice4    = rs.getString(9);
        salesCurrency = rs.getString(10);
      
        outputItem(out, itemCode, mfrCode, desc, desc2, salesCurrency, rrp, sellPrice1, sellPrice2, sellPrice3, sellPrice4, band, ProductCart, StockEnquiryExternal, ProductStockRecord, imagesDir, bytesOut);
      }
      
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }

    scoutln(out, bytesOut, "<tr><td valign=top colspan=12><p>" + fetch(textsDir + text2) + "</td></tr>");
    
    scoutln(out, bytesOut, "</table>");

    scoutln(out, bytesOut, "</td></tr></table>");

  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void outputItem(PrintWriter out, String itemCode, String mfrCode, String desc, String desc2, String salesCurrency, String rrp, String sellPrice1, String sellPrice2, String sellPrice3, String sellPrice4, int band, boolean ProductCart,
                          boolean StockEnquiryExternal, boolean ProductStockRecord, String imagesDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<tr bgcolor='#F0F0F0'><td valign=top><p><a href=\"javascript:view('" + itemCode + "')\">" + itemCode +"</a></td>");

    scoutln(out, bytesOut, "<td valign=top><p>" + mfrCode + "</td>");
    scoutln(out, bytesOut, "<td><p>" + desc + "<br>" + desc2 + "</td>");
      
    String price;
    
    switch(band)
    {
      case 1  : price = sellPrice1;  break;
      case 2  : price = sellPrice2;  break;
      case 3  : price = sellPrice3;  break;
      case 4  : price = sellPrice4;  break;
      default : price = rrp;         break;
    }

    scoutln(out, bytesOut, "</tr>");   
  }
  
}
