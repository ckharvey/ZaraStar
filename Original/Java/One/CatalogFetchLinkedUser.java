// =======================================================================================================================================================================================================
// System: ZaraStar Catalog: For an internal catalog, linked-in user
// Module: CatalogFetchLinkedUser.java
// Author: C.K.Harvey
// Copyright (c) 2002-08 Christopher Harvey. All Rights Reserved.
// Remark: On called server
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

public class CatalogFetchLinkedUser extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  Profile profile = new Profile();
  
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    Writer r = null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="", p9="", p10="", p11="", p12="", p13="", p14="", p15="", p16="",
           p17="", p18="", p19="", p20="", p21="", p22="";

    try
    {
      res.setContentType("text/xml");
      res.setHeader("Cache-Control", "no-cache");
      r = res.getWriter();

      unm = req.getParameter("unm");
      sid = req.getParameter("sid");
      uty = req.getParameter("uty");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");
      p1  = req.getParameter("p1"); // mfr
      p2  = req.getParameter("p2"); // page
      p9  = req.getParameter("p9"); // remoteSID
      p10 = req.getParameter("p10"); // catalogURL
      p11 = req.getParameter("p11"); // catalogUpline
      p12 = req.getParameter("p12"); // pricingURL
      p13 = req.getParameter("p13"); // pricingUpline
      p14 = req.getParameter("p14"); // userType 
      p15 = req.getParameter("p15"); // userName
      p16 = req.getParameter("p16"); // passWord
      p17 = req.getParameter("p17"); // userBand
      p18 = req.getParameter("p18"); // markup
      p19 = req.getParameter("p19"); // discount1
      p20 = req.getParameter("p20"); // discount2
      p21 = req.getParameter("p21"); // discount3
      p22 = req.getParameter("p22"); // discount4
      
      doIt(r, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18, p19, p20, p21, p22, bytesOut);
    }
    catch(Exception e)
    {
      System.out.println("2005b: " + e);
      serverUtils.etotalBytes(req, unm, dnm, 2005, bytesOut[0], 0, "ERR:");
      try {  scoutln(r, bytesOut, "ERR:"); } catch (Exception e2) { } 
      r.flush();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(Writer r, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1,
                    String p2, String p9, String p10, String p11, String p12, String p13, String p14, String p15, String p16, String p17, String p18,
                    String p19, String p20, String p21, String p22, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');//, p11);
    String localDefnsDir = directoryUtils.getLocalOverrideDir(p11);//, unm);
    String iDir             = directoryUtils.getSupportDirs('I');
    String imagesDir        = directoryUtils.getImagesDir(p11);
    String textsDir         = directoryUtils.getTextsDir(p11);

    String uName = directoryUtils.getMySQLUserName();
    String pWord = directoryUtils.getMySQLPassWord();
    
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + p11 + "_ofsa?user=" + uName + "&password=" + pWord);
    Statement stmt=null, stmt2=null, stmt3=null;
    ResultSet rs=null, rs2=null, rs3=null;

    if(p14.equals("L")) // local
    {
      if(! serverUtils.checkSID(unm, p9, uty, p11, localDefnsDir, defnsDir))
      {
        serverUtils.etotalBytes(req, unm, p11, 2004, bytesOut[0], 0, "SID:2005b");
        if(con  != null) con.close();
        r.flush();
        return;
      }
    }
    else // remote
    {
      if(! serverUtils.checkSID(p15, p9, uty, p11, localDefnsDir, defnsDir))
      {
        serverUtils.etotalBytes(req, p15, p11, 2004, bytesOut[0], 0, "SID:2005b");
        if(con  != null) con.close();
        r.flush();
        return;
      }
    }

    set(con, stmt2, stmt2, stmt3, rs, rs2, rs3, r, req, p1, p2, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18, p19, p20, p21, p22, unm, sid, uty,
        den, dnm, bnm, imagesDir, iDir, textsDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, p11, 2004, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1 + ":" + p2);
    if(con != null) con.close();
    r.flush();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, ResultSet rs3, Writer r,
                   HttpServletRequest req, String mfr, String page, String remoteSID, String catalogURL, String catalogUpline, String pricingURL,
                   String pricingUpline, String userType, String userName, String passWord, String userBand, String markup, String discount1,
                   String discount2, String discount3, String discount4, String unm, String sid, String uty, String den, String dnm, String bnm,
                   String imagesDir, String iDir, String textsDir, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(r, bytesOut, "<html><head><title>Catalog Page</title>");

    scoutln(r, bytesOut, "<script language=\"JavaScript\">");

    scoutln(r, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\"></head>");
        
    scoutln(r, bytesOut, "<form><table id=\"page\" width=\"100%\" border=\"0\">");

    showPage(con, stmt, stmt2, stmt3, rs, rs2, rs3, r, req, mfr, page, generalUtils.strToInt(userBand),
             generalUtils.doubleFromStr(markup), generalUtils.doubleFromStr(discount1), generalUtils.doubleFromStr(discount2), generalUtils.doubleFromStr(discount3),
             generalUtils.doubleFromStr(discount4), userType, unm, uty, dnm, catalogURL, pricingURL, pricingUpline, remoteSID, imagesDir, iDir, textsDir,
             localDefnsDir, defnsDir, bytesOut);
         
    scoutln(r, bytesOut, "<tr><td>&#160;</td></tr>");
 
    scoutln(r, bytesOut, "</table></form>");

    scoutln(r, bytesOut, "</div></body></html>");
  }
    
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void showPage(Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, ResultSet rs3, Writer r,
                        HttpServletRequest req, String mfr, String page, int userBand, double markup, double discount1, double discount2,
                        double discount3, double discount4, String userType, String unm, String uty, String dnm, String catalogURL, String pricingURL,
                        String pricingUpline, String remoteSID, String imagesDir, String iDir, String textsDir, String localDefnsDir,
                        String defnsDir, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT CategoryCode, Image, Description, Download, Text, CategoryLink, Text2, NoPrices, NoAvailability, URL, "
                         + "OrderByDescription FROM stockcat WHERE Manufacturer = '" + generalUtils.sanitiseForSQL(mfr) + "' AND Page = '" + page
                         + "' ORDER BY Description");
     
    boolean first = true;
    String catCode, image, catDesc, download, text, categoryLink, text2, noPrices, noAvailability, url, order;
    while(rs.next())
    {
      catCode        = rs.getString(1);
      image          = rs.getString(2);
      catDesc        = rs.getString(3);
      download       = rs.getString(4);
      text           = rs.getString(5);
      categoryLink   = rs.getString(6);
      text2          = rs.getString(7);
      noPrices       = rs.getString(8);
      noAvailability = rs.getString(9);
      url            = rs.getString(10);
      order          = rs.getString(11);

      if(! first)
        scoutln(r, bytesOut, "<hr/>");
      else first = false;
         
      showCategory(con, stmt2, stmt3, rs2, rs3, r, req, mfr, catCode, image, catDesc, download, text, categoryLink, text2, noPrices, noAvailability,
                   url, order, userBand, markup, discount1, discount2, discount3, discount4, userType, pricingURL, pricingUpline, remoteSID,
                   unm, uty, dnm, catalogURL, imagesDir, iDir, textsDir, localDefnsDir, defnsDir, bytesOut);
     }
   
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void showCategory(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, Writer r, HttpServletRequest req,
                            String mfr, String category, String imageSmall, String catDesc, String download, String text, String categoryLink,
                            String text2, String noPrices, String noAvailability, String url, String order, int userBand, double markup,
                            double discount1, double discount2, double discount3, double discount4, String userType, String pricingURL,
                            String pricingUpline, String remoteSID, String unm, String uty, String dnm, String catalogURL, String imagesDir,
                            String iDir, String textsDir, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(r, bytesOut, "<table id=\"page\" border=\"0\">");
    scoutln(r, bytesOut, "<tr><td colspan=\"2\"><p><b>" + catDesc + "</b></p></td></tr>\n");
 
    if(text.length() == 0) // no text
        text = " ";
    
    if(imageSmall.length() > 0) // image exists
    {
      scoutln(r, bytesOut, "<tr><td valign=\"top\"><p><img src=\"" + "http://www.xxx.com" + imagesDir + imageSmall + "\" border=\"1\" /></p>\n");
      if(! download.equals("0")) // download exists
        scoutln(r, bytesOut, "<br /><p><a href=\"javascript:download('" + download + "')\">Download</a> more info</p>\n");
      if(url.length() > 0)
        scoutln(r, bytesOut, "<br /><p><a href=\"http://" + url + "\">Link</a> to Manufacturer</p>");
      if(! categoryLink.equals("0"))
        scoutln(r, bytesOut, "<br /><p><a href=\"javascript:display('" + categoryLink + "')\">Display</a> related items</p>\n");
        
      scoutln(r, bytesOut, "</td>");
        
      scoutln(r, bytesOut, "<td valign=\"top\"><p>" + fetch(textsDir + text) + "</p></td></tr>");
      scoutln(r, bytesOut, "<tr><td></td><td>\n");
    }
    else // no image
    {
      scoutln(r, bytesOut, "<tr><td valign=\"top\"><p>" + fetch(textsDir + text) + "</p></td></tr>\n");
      if(! download.equals("0")) // download exists
        scoutln(r, bytesOut, "<tr><td><p><a href=\"javascript:download('" + download + "')\">Download</a> more info</p></td></tr>\n");
      if(url.length() > 0)
        scoutln(r, bytesOut, "<tr><td colspan=\"2\"><p><a href=\"http://" + url + "\">Link</a> to Manufacturer</p></td></tr>\n");
        
      if(! categoryLink.equals("0"))
        scoutln(r, bytesOut, "<tr><td colspan=\"2\"><p><a href=\"javascript:display('" + categoryLink + "')\">Display</a> related items</p></td></tr>\n");
      scoutln(r, bytesOut, "<tr><td align=\"center\">");
    }
    
    scoutln(r, bytesOut, "<table id=\"page\" border=\"0\">");
    scoutln(r, bytesOut, "<tr><td><p>Our Code</p></td><td><p>Manufacturer Code</p></td><td><p>Description</p></td>\n");
        
    if(userBand == 888)
      scoutln(r, bytesOut, "<td align=\"right\"><p>All Prices</p></td></tr>\n");
    else
    if(userBand == 0)
      scoutln(r, bytesOut, "<td align=\"right\"><p>List Price</p></td></tr>\n");
    else scoutln(r, bytesOut, "<td align=\"right\"><p>Your Price</p></td></tr>\n");

    if(order.equals("S")) /// order by size
    {
      showLinesBySize(con, stmt, stmt2, rs, rs2, r, req, mfr, category, userBand, order, markup, discount1, discount2, discount3, discount4, userType,
                      pricingURL, pricingUpline, remoteSID,  unm, uty, dnm, iDir, localDefnsDir, defnsDir, bytesOut);
    }
    else
    {
      showLines(con, stmt, stmt2, rs, rs2, r, req, mfr, category, userBand, order, markup, discount1, discount2, discount3, discount4, userType,
                pricingURL, pricingUpline, remoteSID,  unm, uty, dnm, iDir, localDefnsDir, defnsDir, bytesOut);
    }
 
    scoutln(r, bytesOut, "<tr><td valign=\"top\" colspan=\"12\"><p>" + fetch(textsDir + text2) + "</p></td></tr>");
    
    scoutln(r, bytesOut, "</table>");

    scoutln(r, bytesOut, "</td></tr></table>\n");

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void showLines(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, Writer r, HttpServletRequest req, String mfr,
                         String catCode, int userBand, String order, double markup, double discount1, double discount2, double discount3,
                         double discount4, String userType, String pricingURL, String pricingUpline, String remoteSID, String unm, String uty,
                         String dnm, String iDir, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();
 
    if(order.equals("D"))
    {
      rs = stmt.executeQuery("SELECT ItemCode, ManufacturerCode, Description, Description2, RRP, SellPrice1, SellPrice2, SellPrice3, SellPrice4, "
                           + " SalesCurrency FROM stock WHERE CategoryCode = '" + catCode + "' ORDER BY Description");
    }
    else
    {
      rs = stmt.executeQuery("SELECT ItemCode, ManufacturerCode, Description, Description2, RRP, SellPrice1, SellPrice2, SellPrice3, SellPrice4, "
                           + "SalesCurrency FROM stock WHERE CategoryCode = '" + catCode + "' ORDER BY ManufacturerCode");
    }
    
    String itemCode, mfrCode, desc, desc2, salesCurrency;
    double rrp, sellPrice1, sellPrice2, sellPrice3, sellPrice4;
     
    while(rs.next())
    {
      itemCode      = rs.getString(1);
      mfrCode       = rs.getString(2);
      desc          = rs.getString(3);
      desc2         = rs.getString(4);
      rrp           = generalUtils.doubleFromStr(rs.getString(5));
      sellPrice1    = generalUtils.doubleFromStr(rs.getString(6));
      sellPrice2    = generalUtils.doubleFromStr(rs.getString(7));
      sellPrice3    = generalUtils.doubleFromStr(rs.getString(8));
      sellPrice4    = generalUtils.doubleFromStr(rs.getString(9));
      salesCurrency = rs.getString(10);
   
      showLine(con, stmt2, rs2, r, req, itemCode, mfr, mfrCode, desc, desc2, rrp, sellPrice1, sellPrice2, sellPrice3, sellPrice4, userBand, markup,
               discount1, discount2, discount3, discount4, salesCurrency, userType, pricingURL, pricingUpline, remoteSID, unm, uty, dnm, iDir,
               localDefnsDir, defnsDir, bytesOut);
     }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void showLine(Connection con, Statement stmt, ResultSet rs, Writer r, HttpServletRequest req, String itemCode, String mfr, String mfrCode,
                        String desc, String desc2, double rrp, double sellPrice1, double sellPrice2, double sellPrice3, double sellPrice4,
                        int userBand, double markup, double discount1, double discount2, double discount3, double discount4, String salesCurrency,
                        String userType, String pricingURL, String pricingUpline, String remoteSID, String unm, String uty, String dnm, String iDir,
                        String localDefnsDir, String defnsDir, int[] bytesOut)
                        throws Exception
  {
    scoutln(r, bytesOut, "<tr bgcolor='#F0F0F0'><td valign=\"top\"><p>" + itemCode + "</p></td>\n");

    scoutln(r, bytesOut, "<td valign=\"top\"><p>" + mfrCode + "</p></td>");
    scoutln(r, bytesOut, "<td><p>" + desc + "<br />" + desc2 + "</p></td>\n");

    scoutln(r, bytesOut, "<td nowrap=\"nowrap\" valign=\"top\" align=\"right\"><p>" + salesCurrency + " ");
      
    double price;
    String priceForCart;
      
    markup /= 100;
      
    if(userType.equals("R")) // call from remote server
    {
      rrp        *= markup;
      sellPrice1  = rrp - (rrp * (discount1 / 100));
      sellPrice2  = rrp - (rrp * (discount2 / 100));
      sellPrice3  = rrp - (rrp * (discount3 / 100));
      sellPrice4  = rrp - (rrp * (discount4 / 100));

      if(uty.equals("I")) // internal on remote server
      {
        scoutln(r, bytesOut, generalUtils.doubleDPs('2', rrp) + " " + generalUtils.doubleDPs('2', sellPrice1) + " " + generalUtils.doubleDPs('2', sellPrice2)
              + " " + generalUtils.doubleDPs('2', sellPrice3) + " " + generalUtils.doubleDPs('2', sellPrice4) + "</p></td>\n");
        priceForCart = generalUtils.doubleDPs('2', rrp);
      }
      else
      if(uty.equals("R")) // registered user on remote server
      {
        switch(userBand)
        {
          case 1  : price = sellPrice1; priceForCart = generalUtils.doubleDPs('2', price);  break;
          case 2  : price = sellPrice2; priceForCart = generalUtils.doubleDPs('2', price);  break;
          case 3  : price = sellPrice3; priceForCart = generalUtils.doubleDPs('2', price);  break;
          case 4  : price = sellPrice4; priceForCart = generalUtils.doubleDPs('2', price);  break;
          default : price = rrp;        priceForCart = generalUtils.doubleDPs('2', rrp);    break; // just-in-case
        }
 
        scoutln(r, bytesOut, generalUtils.doubleDPs('2', price) + "</p></td>\n");
      }
      else // anon
      {
        scoutln(r, bytesOut, generalUtils.doubleDPs('2', rrp) + "</p></td>\n");
        priceForCart = generalUtils.doubleDPs('2', rrp);
      }
    }
    else // call from local server
    {
      if(uty.equals("I")) // internal
      {
        scoutln(r, bytesOut, generalUtils.doubleDPs('2', rrp) + " " + generalUtils.doubleDPs('2', sellPrice1) + " " + generalUtils.doubleDPs('2', sellPrice2)
              + " " + generalUtils.doubleDPs('2', sellPrice3) + " " + generalUtils.doubleDPs('2', sellPrice4) + "</p></td>\n");
        priceForCart = generalUtils.doubleDPs('2', rrp);
      }
      else
      if(uty.equals("R")) // registered
      {
        switch(userBand)
        {
          case 1  : price = sellPrice1; priceForCart = generalUtils.doubleDPs('2', price);  break;
          case 2  : price = sellPrice2; priceForCart = generalUtils.doubleDPs('2', price);  break;
          case 3  : price = sellPrice3; priceForCart = generalUtils.doubleDPs('2', price);  break;
          case 4  : price = sellPrice4; priceForCart = generalUtils.doubleDPs('2', price);  break;
          default : price = rrp;        priceForCart = generalUtils.doubleDPs('2', rrp);    break; // just-in-case
        }
  
        scoutln(r, bytesOut, generalUtils.doubleDPs('2', price) + "</p></td>\n");
      }
      else // anon
      {
        scoutln(r, bytesOut, generalUtils.doubleDPs('2', rrp) + "</p></td>\n");
        priceForCart = generalUtils.doubleDPs('2', rrp);
      }
    }
      
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 121, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      String descs = desc + " " + desc2;
      scoutln(r, bytesOut, "<td><a href=\"javascript:addToCart('" + itemCode + "','" + priceForCart + "','" + salesCurrency + "','" + generalUtils.sanitise2(mfrCode) + "','Each'," + generalUtils.sanitise2(descs) + "','" + remoteSID
                           + "')\"><img border=\"0\" src=\"" + iDir + "cart.png\" /></a></td>\n");
    }

    scoutln(r, bytesOut, "</tr>");   
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(Writer r, int bytesOut[], String str) throws Exception
  {      
    r.write(str + "\n");
    bytesOut[0] += (str.length() + 1);    
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
  private void showLinesBySize(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, Writer r, HttpServletRequest req,
                               String mfr, String catCode, int userBand, String order, double markup, double discount1, double discount2,
                               double discount3, double discount4, String userType, String pricingURL, String pricingUpline, String remoteSID,
                               String unm, String uty, String dnm, String iDir, String localDefnsDir, String defnsDir, int[] bytesOut)
                               throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT Description, ItemCode FROM stock WHERE CategoryCode = '" + catCode + "'");

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
    
    String sortedItemCodes = generalUtils.orderBySize(entries, itemCodes, numEntries, 82);
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    String itemCode, mfrCode, desc, desc2, salesCurrency, thisItemCode;
    double rrp, sellPrice1, sellPrice2, sellPrice3, sellPrice4;

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

        rs = stmt.executeQuery("SELECT ItemCode, ManufacturerCode, Description, Description2, RRP, SellPrice1, SellPrice2, SellPrice3, SellPrice4, "
                               + " SalesCurrency FROM stock WHERE ItemCode = '" + generalUtils.sanitiseForSQL(thisItemCode) + "'");

        if(rs.next())
        {
          itemCode      = rs.getString(1);
          mfrCode       = rs.getString(2);
          desc          = rs.getString(3);
          desc2         = rs.getString(4);
          rrp           = generalUtils.doubleFromStr(rs.getString(5));
          sellPrice1    = generalUtils.doubleFromStr(rs.getString(6));
          sellPrice2    = generalUtils.doubleFromStr(rs.getString(7));
          sellPrice3    = generalUtils.doubleFromStr(rs.getString(8));
          sellPrice4    = generalUtils.doubleFromStr(rs.getString(9));
          salesCurrency = rs.getString(10);
   
          showLine(con, stmt2, rs2, r, req, itemCode, mfr, mfrCode, desc, desc2, rrp, sellPrice1, sellPrice2, sellPrice3, sellPrice4, userBand,
                   markup, discount1, discount2, discount3, discount4, salesCurrency, userType, pricingURL, pricingUpline, remoteSID, unm, uty, dnm,
                   iDir, localDefnsDir, defnsDir, bytesOut);
         }
        
         if(rs   != null) rs.close();
         if(stmt != null) stmt.close();
      }
    }
  }

}
