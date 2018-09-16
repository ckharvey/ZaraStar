// =======================================================================================================================================================================================================
// System: ZaraStar Utils: For a buyers catalog page
// Module: CatalogBuyersPage.java
// Author: C.K.Harvey
// Copyright (c) 2002-07 Christopher Harvey. All Rights Reserved.
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

public class CatalogBuyersPage extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  Profile profile = new Profile();
  Inventory inventory = new Inventory();
  
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
      p1  = req.getParameter("p1"); // custCode
      p2  = req.getParameter("p2"); // section
      
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
 
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "CatalogBuyers", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2014, bytesOut[0], 0, "ERR:" + p1 + ":" + p2);
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                    String p1, String p2, int[] bytesOut) throws Exception
  {
    String iDir             = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String textsDir         = directoryUtils.getTextsDir(dnm);
    String imagesDir        = directoryUtils.getImagesDir(dnm);

    long startTime = new java.util.Date().getTime();

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null, stmt2 = null, stmt3 = null;
    ResultSet rs   = null, rs2 = null, rs3 = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 2014, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "CatalogBuyersPage", iDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2014, bytesOut[0], 0, "ACC:" + p1 + ":" + p2);
      if(con  != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "CatalogBuyersPage", iDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2014, bytesOut[0], 0, "SID:" + p1 + ":" + p2);
      if(con  != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    set(con, stmt, stmt2, stmt3, rs, rs2, rs3, out, req, p1, p2, unm, sid, uty, men, den, dnm, bnm, imagesDir, textsDir, localDefnsDir, defnsDir,
        bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 2014, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1 + ":" + p2);
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, ResultSet rs3, PrintWriter out,
                   HttpServletRequest req, String custCode, String section, String unm, String sid, String uty, String men, String den, String dnm,
                   String bnm, String imagesDir, String textsDir, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Catalog Page</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    if(! unm.equals("Sysadmin") && authenticationUtils.verifyAccess(con, stmt, rs, req, 2001, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function view(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/StockEnquiryExternal?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den
                           + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+p1;}");
    }
    else
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3001, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function view(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/ProductStockRecord?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den
                           + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p1=\"+p1;}");
    }

    scoutln(out, bytesOut, "function download(which){");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/LibraryDownloaCasual?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den
                         + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+which;}");

    scoutln(out, bytesOut, "function wiki(page){");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/_6600?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den
                         + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+page;}");

    scoutln(out, bytesOut, "function display(page){");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/CatalogBuyersPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den
                         + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + generalUtils.sanitise(custCode) + "&p2=\"+page;}");
    
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 6501, unm, uty, dnm, localDefnsDir, defnsDir))
    {    
      scoutln(out, bytesOut, "function list(dir){var p1=sanitise(dir);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/ImagesListDirectory?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den
                           + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+p1;}");
    }
    
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 6103, unm, uty, dnm, localDefnsDir, defnsDir))
    {    
      scoutln(out, bytesOut, "function edit(fileName){var p3=sanitise(fileName);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/TextEditText?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den
                           + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=&p1=&p3=\"+p3;}");
    }
    
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 121, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function addToCart(code){var p1=sanitise(code);");
        if(!uty.equals("I"))scoutln(out, bytesOut, "alert('Coming Soon');else ");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/ProductCartAddToCart?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den
                           + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=B&p1=\"+p1;}");
    }

    scoutln(out, bytesOut, "function sanitise(code){");
    scoutln(out, bytesOut, "var code2='';var x;var len=code.length;");
    scoutln(out, bytesOut, "for(x=0;x<len;++x)");
    scoutln(out, bytesOut, "if(code.charAt(x)=='#')code2+='%23';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='\"')code2+='%22';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='&')code2+='%26';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='%')code2+='%25';");
    scoutln(out, bytesOut, "else if(code.charAt(x)==' ')code2+='%20';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='?')code2+='%3F';");
    scoutln(out, bytesOut, "else code2+=code.charAt(x);");
    scoutln(out, bytesOut, "return code2};");
    
    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\""
                         + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\"></head>");
  
    int[] hmenuCount = new int[1];
 
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "2014", "", "CatalogBuyers", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount,
                          bytesOut);

    if(uty.equals("R"))
    {
      String[] name         = new String[1];
      String[] companyName  = new String[1];
      String[] accessRights = new String[1];
      int i = unm.indexOf("_");

      profile.getExternalAccessNameCompanyAndRights(con, stmt, rs, unm.substring(0, i), name, companyName, accessRights);

      pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Section " + section + " for " + name[0] + " of " + companyName[0], "2014",
                       unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

      scoutln(out, bytesOut, "<form><table id=\"page\" width=100% border=0>");

      String[] custCode2 = new String[1];
      profile.getExternalAccessNameCustomerCode(con, stmt, rs, unm.substring(0, i), dnm, localDefnsDir, defnsDir, custCode2);

      if(custCode.equals(custCode2[0])) // prevent user 'calling' this servlet for another customer
      {
        scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
        showSection(con, stmt, stmt2, stmt3, rs, rs2, rs3, out, req, custCode, section, unm, sid, uty, men, den, dnm, bnm, imagesDir, textsDir,
                    localDefnsDir, defnsDir, bytesOut);
      }
        
      scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr></table></form>");
    }
    else
    if(uty.equals("I"))
    {
      pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Section " + section, "2014", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);
      scoutln(out, bytesOut, "<form><table id=\"page\" width=100% border=0>");
      scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
      showSection(con, stmt, stmt2, stmt3, rs, rs2, rs3, out, req, custCode, section, unm, sid, uty, men, den, dnm, bnm, imagesDir, textsDir,
                  localDefnsDir, defnsDir, bytesOut);
      scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr></table></form>");
    }
    
    scoutln(out, bytesOut, "</div></body></html>");
  }
    
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void showSection(Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, ResultSet rs3,
                           PrintWriter out, HttpServletRequest req, String custCode, String section, String unm, String sid, String uty, String men,
                           String den, String dnm, String bnm, String imagesDir, String textsDir, String localDefnsDir, String defnsDir,
                           int[] bytesOut) throws Exception
  {
    byte[] data = new byte[5000];
    
    try
    {
      stmt = con.createStatement();
  
      rs = stmt.executeQuery("SELECT ItemCode, BuyerItemCode1, BuyerItemCode2, BuyerItemCode3, SectionName, Band, BelowSame FROM buyerscatalog "
                           + "WHERE CustomerCode = '" + custCode + "' AND Section = '" + section + "' ORDER BY Entry");

      String itemCode, buyerItemCode1, buyerItemCode2, buyerItemCode3, sectionName, band, belowSame;
      int belowSameCount = 0;
      boolean isFirst, isLast;
      
      while(rs.next())                  
      {
        itemCode       = rs.getString(1);
        buyerItemCode1 = rs.getString(2);
        buyerItemCode2 = rs.getString(3);
        buyerItemCode3 = rs.getString(4);
        sectionName    = rs.getString(5);
        band           = rs.getString(6);
        belowSame      = rs.getString(7);
        
        if(belowSame.equals("Y"))
        {
          if(belowSameCount == 0)
            isFirst = true;
          else isFirst = false;
          ++belowSameCount;
          isLast = false;
        }
        else
        {
          if(belowSameCount > 0)
            isFirst = false;
          else isFirst = true;
          belowSameCount = 0;
          isLast = true;
        }
         
        if(isFirst)
          scoutln(out, bytesOut, "<tr><td><hr></td></tr>");
 
        showItem(con, stmt2, stmt3, rs2, rs3, out, req, isFirst, isLast, itemCode, band, buyerItemCode1, buyerItemCode2, buyerItemCode3, data, unm,
                 uty, dnm, men, imagesDir, textsDir, localDefnsDir, defnsDir, bytesOut);
      }
      
      scoutln(out, bytesOut, "<tr><td><hr></td></tr>");

      if(rs   != null) rs.close();                                 
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(stmt != null) stmt.close();
    }    
  }  

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void showItem(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, HttpServletRequest req,
                        boolean isFirst, boolean isLast, String itemCode, String band, String buyerItemCode1, String buyerItemCode2,
                        String buyerItemCode3, byte[] data, String unm, String uty, String dnm, String men, String imagesDir, String textsDir,
                        String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    itemCode = itemCode.toUpperCase();
    inventory.getStockRecGivenCode(con, stmt, rs, itemCode, '\000', data);
    String mfr           = generalUtils.dfsAsStr(data, (short)3);
    String mfrCode       = generalUtils.dfsAsStr(data, (short)4);
    String desc          = generalUtils.dfsAsStr(data, (short)1);
    String desc2         = generalUtils.dfsAsStr(data, (short)2);
    String rrp           = generalUtils.dfsAsStr(data, (short)25);
    String sellPrice1    = generalUtils.dfsAsStr(data, (short)20);
    String sellPrice2    = generalUtils.dfsAsStr(data, (short)21);
    String sellPrice3    = generalUtils.dfsAsStr(data, (short)22);
    String sellPrice4    = generalUtils.dfsAsStr(data, (short)23);
    String salesCurrency = generalUtils.dfsAsStr(data, (short)52);
    String categoryCode  = generalUtils.dfsAsStr(data, (short)55);

    if(categoryCode.length() > 0 && ! categoryCode.equals("0")) // is a catalog page for this item
    {
      showItemFromCategory(con, stmt, stmt2, rs, rs2, out, req, isFirst, isLast, mfr, categoryCode, itemCode, sellPrice1, sellPrice2, sellPrice3,
                           sellPrice4, rrp, desc, desc2, salesCurrency, mfrCode, band, buyerItemCode1, buyerItemCode2, buyerItemCode3, unm, uty, dnm,
                           men, imagesDir, textsDir, localDefnsDir, defnsDir, bytesOut);  
    }
    else // no catalog page
    {
      writePlainItem(con, stmt, rs, out, req, isFirst, isLast, mfr, itemCode, sellPrice1, sellPrice2, sellPrice3, sellPrice4, rrp, desc, desc2,
                     salesCurrency, mfrCode, band, buyerItemCode1, buyerItemCode2, buyerItemCode3, unm, uty, dnm, men, imagesDir, localDefnsDir,
                     defnsDir, bytesOut);
    }
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void showItemFromCategory(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out,
                                    HttpServletRequest req, boolean isFirst, boolean isLast, String mfr, String categoryCode, String itemCode,
                                    String sellPrice1, String sellPrice2, String sellPrice3, String sellPrice4, String rrp, String desc,
                                    String desc2, String salesCurrency, String mfrCode, String band, String buyerItemCode1, String buyerItemCode2,
                                    String buyerItemCode3, String unm, String uty, String dnm, String men, String imagesDir, String textsDir,
                                    String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT Image, Description, Download, Text, CategoryLink, Text2, NoPrices, NoAvailability, Style, URL, WikiPage, "
                         + "OrderByDescription FROM stockcat WHERE CategoryCode = '" + categoryCode + "' ORDER BY Description");
    
    String image, catDesc, download, text, categoryLink, text2, noPrices, noAvailability, style, url, wikiPage, orderByDesc;
    while(rs.next())
    {
      image          = rs.getString(1);
      catDesc        = rs.getString(2);
      download       = rs.getString(3);
      text           = rs.getString(4);
      categoryLink   = rs.getString(5);
      text2          = rs.getString(6);
      noPrices       = rs.getString(7);
      noAvailability = rs.getString(8);
      style          = rs.getString(9);
      url            = rs.getString(10);
      wikiPage       = rs.getString(11);
      orderByDesc    = rs.getString(12);

      writeItem(con, stmt2, rs2, out, req, isFirst, isLast, mfr, itemCode, image, catDesc, download, text,
                categoryLink, text2, noPrices, noAvailability, style, url, wikiPage, orderByDesc, sellPrice1, sellPrice2, sellPrice3, sellPrice4, rrp,
                desc, desc2, salesCurrency, mfrCode, band, buyerItemCode1, buyerItemCode2, buyerItemCode3, unm, uty, dnm, men, imagesDir, textsDir,
                localDefnsDir, defnsDir, bytesOut);
    }
   
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void writeItem(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, boolean isFirst, boolean isLast, 
                         String mfr, String itemCode, String image, String catDesc, String download, String text, String categoryLink, String text2,
                         String noPrices, String noAvailability, String style, String url, String wikiPage, String orderByDesc, String sellPrice1,
                         String sellPrice2, String sellPrice3, String sellPrice4, String rrp, String desc, String desc2, String salesCurrency,
                         String mfrCode, String band, String buyerItemCode1, String buyerItemCode2, String buyerItemCode3, String unm, String uty,
                         String dnm, String men, String imagesDir, String textsDir, String localDefnsDir, String defnsDir, int[] bytesOut)
                         throws Exception
  {
    if(isFirst)
    {
      scoutln(out, bytesOut, "<table id=\"page\" border=0>");
      scoutln(out, bytesOut, "<tr><td colspan=2><p><b>" + catDesc + "</td></tr>");

      if(text.length() > 0) // text exists
      {
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
        
          if(authenticationUtils.verifyAccess(con, stmt, rs, req, 6103, unm, uty, dnm, localDefnsDir, defnsDir))
            scoutln(out, bytesOut, "<br><p><a href=\"javascript:edit('" + text + "')\">Edit</a> Text");

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
        
          if(authenticationUtils.verifyAccess(con, stmt, rs, req, 6103, unm, uty, dnm, localDefnsDir, defnsDir))
            scoutln(out, bytesOut, "<tr><td colspan=2><p><a href=\"javascript:edit('" + text + "')\">Edit</a> Text</td></tr>");
        
          if(authenticationUtils.verifyAccess(con, stmt, rs, req, 6501, unm, uty, dnm, localDefnsDir, defnsDir))
            scoutln(out, bytesOut, "<tr><td colspan=2><p><a href=\"javascript:list('" + dirPartOf(image) + "')\">List</a> Images</td></tr>");

          if(! categoryLink.equals("0"))
            scoutln(out, bytesOut, "<tr><td colspan=2><p><a href=\"javascript:display('" + categoryLink + "')\">Display</a> related items</td></tr>");
          scoutln(out, bytesOut, "<tr><td align=center>");
        }
      }
      else // no text
      {
        scoutln(out, bytesOut, "<tr><td valign=top><p><img src=\"http://" + men + imagesDir + "/" + image + "\" border=1>");
        if(! wikiPage.equals("0")) // wikiPage exists
          scoutln(out, bytesOut, "<br><br><p><a href=\"javascript:wiki('" + wikiPage + "')\">View</a> more detail");
        if(! download.equals("0")) // download exists
          scoutln(out, bytesOut, "<br><br><p><a href=\"javascript:download('" + download + "')\">Download</a> more info");
        if(url.length() > 0)
          scoutln(out, bytesOut, "<br><br><p><a href=\"http://" + url + "\">Link</a> to Manufacturer");
        if(! categoryLink.equals("0"))
          scoutln(out, bytesOut, "<br><br><p><a href=\"javascript:display('" + categoryLink + "')\">Display</a> related items");
        scoutln(out, bytesOut, "</td><td valign=top>");
      }
    
      scoutln(out, bytesOut, "<table id=\"page\" border=0>");
      scoutln(out, bytesOut, "<tr bgcolor='#E0E0E0'><td><p>Our Code</td><td><p>Manufacturer</td><td><p>Manufacturer Code</td>");

      if(buyerItemCode1.length() > 0)
      {
        if(buyerItemCode2.length() == 0 && buyerItemCode3.length() == 0)
          scoutln(out, bytesOut, "<td><p>Your Code</td>");
        else scoutln(out, bytesOut, "<td><p>Your Code 1</td>");
      }
      
      if(buyerItemCode2.length() > 0)
        scoutln(out, bytesOut, "<td><p>Your Code 2</td>");
      
      if(buyerItemCode3.length() > 0)
        scoutln(out, bytesOut, "<td><p>Your Code 3</td>");
    
      scoutln(out, bytesOut, "<td><p>Description</td>");
        
      scoutln(out, bytesOut, "<td align=right><p>Your Price</td></tr>");
    }
    
    if(   authenticationUtils.verifyAccess(con, stmt, rs, req, 3001, unm, uty, dnm, localDefnsDir, defnsDir)
       || authenticationUtils.verifyAccess(con, stmt, rs, req, 2001, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "<tr bgcolor='#F0F0F0'><td valign=top><p><a href=\"javascript:view('" + itemCode + "')\">" + itemCode + "</a></td>");
    }
    else scoutln(out, bytesOut, "<tr bgcolor='#F0F0F0'><td valign=top><p>" + itemCode + "</td>");

    scoutln(out, bytesOut, "<td valign=top><p>" + mfr + "</td>");
    scoutln(out, bytesOut, "<td valign=top><p>" + mfrCode + "</td>");

    if(buyerItemCode1.length() > 0)
      scoutln(out, bytesOut, "<td valign=top><p>" + buyerItemCode1 + "</td>");
        
    if(buyerItemCode2.length() > 0)
      scoutln(out, bytesOut, "<td valign=top><p>" + buyerItemCode2 + "</td>");
    
    if(buyerItemCode3.length() > 0)
      scoutln(out, bytesOut, "<td valign=top><p>" + buyerItemCode3 + "</td>");

    scoutln(out, bytesOut, "<td valign=top><p>" + desc + "<br>" + desc2 + "</td>");
    scoutln(out, bytesOut, "<td nowrap valign=top align=right><p>" + salesCurrency + " ");
      
    String price;
    
    switch(generalUtils.intFromStr(band))
    {
      case 1  : price = sellPrice1;  break;
      case 2  : price = sellPrice2;  break;
      case 3  : price = sellPrice3;  break;
      case 4  : price = sellPrice4;  break;
      default : price = rrp;         break;
    }
        
    scoutln(out, bytesOut, generalUtils.doubleDPs(price, '2') + "</td>");

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 121, unm, uty, dnm, localDefnsDir, defnsDir))
      scoutln(out, bytesOut, "<td><a href=\"javascript:addToCart('" + itemCode + "')\"><img border=0 src=\"" + imagesDir + "cart.png\"></a></td>");

    scoutln(out, bytesOut, "</tr>");   

    if(isLast)
    {
      scoutln(out, bytesOut, "<tr><td valign=top colspan=12><p>" + fetch(textsDir + text2) + "</td></tr>");
      
      scoutln(out, bytesOut, "</table>");

      scoutln(out, bytesOut, "</td></tr></table>");
    }
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void writePlainItem(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, boolean isFirst, boolean isLast,
                              String mfr, String itemCode, String sellPrice1, String sellPrice2, String sellPrice3, String sellPrice4, String rrp,
                              String desc, String desc2, String salesCurrency, String mfrCode, String band, String buyerItemCode1,
                              String buyerItemCode2, String buyerItemCode3, String unm, String uty, String dnm, String men, String imagesDir,
                              String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    if(isFirst)
    {
      scoutln(out, bytesOut, "<table id=\"page\" border=0>");
      scoutln(out, bytesOut, "<tr bgcolor='#E0E0E0'><td><p>Our Code</td><td><p>Manufacturer</td><td><p>Manufacturer Code</td>");

      if(buyerItemCode1.length() > 0)
      {
        if(buyerItemCode2.length() == 0 && buyerItemCode3.length() == 0)
          scoutln(out, bytesOut, "<td><p>Your Code</td>");
        else scoutln(out, bytesOut, "<td><p>Your Code 1</td>");
      }
      
      if(buyerItemCode2.length() > 0)
        scoutln(out, bytesOut, "<td><p>Your Code 2</td>");
      
      if(buyerItemCode3.length() > 0)
        scoutln(out, bytesOut, "<td><p>Your Code 3</td>");
    
      scoutln(out, bytesOut, "<td><p>Description</td>");
        
      scoutln(out, bytesOut, "<td align=right><p>Your Price</td></tr>");
    }
    
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3001, unm, uty, dnm, localDefnsDir, defnsDir))
      scoutln(out, bytesOut, "<tr bgcolor='#F0F0F0'><td valign=top><p><a href=\"javascript:view('" + itemCode + "')\">" + itemCode + "</a></td>");
    else scoutln(out, bytesOut, "<tr bgcolor='#F0F0F0'><td valign=top><p>" + itemCode + "</td>");

    scoutln(out, bytesOut, "<td valign=top><p>" + mfr + "</td>");
    scoutln(out, bytesOut, "<td valign=top><p>" + mfrCode + "</td>");

    if(buyerItemCode1.length() > 0)
      scoutln(out, bytesOut, "<td><p>" + buyerItemCode1 + "</td>");
    
    if(buyerItemCode2.length() > 0)
      scoutln(out, bytesOut, "<td><p>" + buyerItemCode2 + "</td>");
    
    if(buyerItemCode3.length() > 0)
      scoutln(out, bytesOut, "<td><p>" + buyerItemCode3 + "</td>");
    
    scoutln(out, bytesOut, "<td><p>" + desc + "<br>" + desc2 + "</td>");
    
    scoutln(out, bytesOut, "<td nowrap valign=top align=right><p>" + salesCurrency + " ");
      
    String price;
    
    switch(generalUtils.intFromStr(band))
    {
      case 1  : price = sellPrice1;  break;
      case 2  : price = sellPrice2;  break;
      case 3  : price = sellPrice3;  break;
      case 4  : price = sellPrice4;  break;
      default : price = rrp;         break;
    }
        
    scoutln(out, bytesOut, generalUtils.doubleDPs(price, '2') + "</td>");

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 121, unm, uty, dnm, localDefnsDir, defnsDir))
      scoutln(out, bytesOut, "<td><a href=\"javascript:addToCart('" + itemCode + "')\"><img border=0 src=\"" + imagesDir + "cart.png\"></a></td>");
    else scoutln(out, bytesOut, "</tr>");   
    
    if(isLast)
      scoutln(out, bytesOut, "</table>");
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
    --len;
    while(len > 0 && image.charAt(len) != '/')
      --len;
    return image.substring(0, len);  
  }
        
  // ------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }
  
}

