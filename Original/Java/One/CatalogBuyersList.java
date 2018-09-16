// =======================================================================================================================================================================================================
// System: ZaraStar Catalogs: List buyers catalog
// Module: CatalogBuyersList.java
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

public class CatalogBuyersList extends HttpServlet
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

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="";

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
      
      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, bytesOut);
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
      serverUtils.etotalBytes(req, unm, dnm, 2014, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                    String p1, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String iDir             = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir        = directoryUtils.getImagesDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null, stmt2 = null;
    ResultSet rs   = null, rs2 = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 2014, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "CatalogBuyersList", iDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2014, bytesOut[0], 0, "ACC:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "CatalogBuyersList", iDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2014, bytesOut[0], 0, "SID:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    set(con, stmt, stmt2, rs, rs2, out, req, p1, unm, sid, uty, men, den, dnm, bnm, imagesDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 2014, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, HttpServletRequest req,
                   String custCode, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String imagesDir,
                   String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>List of a Catalog</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    scoutln(out, bytesOut, "function view(sect){var p2=sanitise(sect);");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/CatalogBuyersPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den
                         + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + custCode + "&p2=\"+p2;}");

    boolean canCart = authenticationUtils.verifyAccess(con, stmt, rs, req, 121, unm, uty, dnm, localDefnsDir, defnsDir);
    if(canCart)
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

    int numItems = countItems(con, stmt, rs, custCode);

    if(uty.equals("R"))
    {
      String[] name         = new String[1];
      String[] companyName  = new String[1];
      String[] accessRights = new String[1];
      int i = unm.indexOf("_");

      profile.getExternalAccessNameCompanyAndRights(con, stmt, rs, unm.substring(0, i), name, companyName, accessRights);

      pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Buyers Catalog for " + name[0] + " of " + companyName[0], "2014", unm, sid, uty, men, den,
                      dnm, bnm, hmenuCount, bytesOut);

      String[] custCode2 = new String[1];
      profile.getExternalAccessNameCustomerCode(con, stmt, rs, unm.substring(0, i), dnm, localDefnsDir, defnsDir, custCode2);
      
      if(custCode.equals(custCode2[0])) // prevent user 'calling' this servlet for another customer
      {
        scoutln(out, bytesOut, "<table id=\"page\" border=0>");
        scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
        scoutln(out, bytesOut, "<tr><td><p>Listing has " + numItems + " Items</td></tr>");
        scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
        scoutln(out, bytesOut, "<tr bgcolor='#E0E0E0'><td><p><b>Section</td><td><p><b>Our Code</td><td><p><b>Manufacturer</td><td><p><b>Manufacturer Code</td>");
        scoutln(out, bytesOut, "<td><p><b>Your Code 1</td><td><p><b>Your Code 2</td><td><p><b>Your Code 3</td><td><p><b>Description</td><td align=right><p><b>Your Price</td></tr>");
         
        scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
        showSection(con, stmt, stmt2, rs, rs2, out, custCode, dnm, localDefnsDir, defnsDir, canCart, imagesDir, bytesOut);
      }
        
      scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr></table></form>");
    }
    else
    if(uty.equals("I"))
    {
      pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Catalog for " + custCode, "2014", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);
      scoutln(out, bytesOut, "<form><table id=\"page\" width=100% border=0>");
      scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
      scoutln(out, bytesOut, "<tr><td><p>Listing has " + numItems + " Items</td></tr>");
      scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
      scoutln(out, bytesOut, "<tr bgcolor='#E0E0E0'><td><p><b>Section</td><td><p><b>Our Code</td><td><p><b>Manufacturer</td><td><p><b>Manufacturer Code</td>");
      scoutln(out, bytesOut, "<td><p><b>Your Code 1</td><td><p><b>Your Code 2</td><td><p><b>Your Code 3</td><td><p><b>Description</td><td align=right><p><b>Your Price</td></tr>");

      showSection(con, stmt, stmt2, rs, rs2, out, custCode, dnm, localDefnsDir, defnsDir, canCart, imagesDir, bytesOut);
      
      scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr></table></form>");
    }
    
    scoutln(out, bytesOut, "</div></body></html>");
  }
    
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void showSection(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, String custCode,
                           String dnm, String localDefnsDir, String defnsDir, boolean canCart, String imagesDir, int[] bytesOut) throws Exception
  {
    byte[] data = new byte[5000];
    
    try
    {
      stmt = con.createStatement();
  
      rs = stmt.executeQuery("SELECT ItemCode, BuyerItemCode1, BuyerItemCode2, BuyerItemCode3, SectionName, Band, Section FROM buyerscatalog WHERE CustomerCode = '" + custCode + "' ORDER BY SectionName, BuyerItemCode1");

      String itemCode, buyerItemCode1, buyerItemCode2, buyerItemCode3, sectionName, section, band;
      String cssFormat = "";
      
      while(rs.next())                  
      {
        itemCode       = rs.getString(1);
        buyerItemCode1 = rs.getString(2);
        buyerItemCode2 = rs.getString(3);
        buyerItemCode3 = rs.getString(4);
        sectionName    = rs.getString(5);
        band           = rs.getString(6);
        section        = rs.getString(7);
        
        if(cssFormat.equals("F0F0F0")) cssFormat = "D0D0D0"; else cssFormat = "F0F0F0";
            
        showItem(con, stmt2, rs2, out, section, sectionName, itemCode, band, buyerItemCode1, buyerItemCode2, buyerItemCode3, data, cssFormat, canCart, imagesDir, bytesOut);
      }
      
      if(rs   != null) rs.close();                                 
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(stmt != null) stmt.close();
    }    
  }  

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void showItem(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String section, String sectionName, String itemCode, String band, String buyerItemCode1, String buyerItemCode2, String buyerItemCode3, byte[] data,
                        String cssFormat, boolean canCart, String imagesDir, int[] bytesOut) throws Exception
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

    writeItem(out, section, sectionName, mfr, itemCode, sellPrice1, sellPrice2, sellPrice3, sellPrice4, rrp, desc, desc2, salesCurrency, mfrCode, band, buyerItemCode1, buyerItemCode2, buyerItemCode3, cssFormat, canCart, imagesDir, bytesOut);
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void writeItem(PrintWriter out, String section, String sectionName, String mfr, String itemCode, String sellPrice1, String sellPrice2,
                         String sellPrice3, String sellPrice4, String rrp, String desc, String desc2, String salesCurrency, String mfrCode,
                         String band, String buyerItemCode1, String buyerItemCode2, String buyerItemCode3, String bgColor, boolean canCart,
                         String imagesDir, int[] bytesOut)
                         throws Exception
  {
    scoutln(out, bytesOut, "<tr bgcolor='" + bgColor + "'><td valign=top><p><a href=\"javascript:view('" + section + "')\">" + sectionName
                         + "</a></td>");
    scoutln(out, bytesOut, "<td valign=top><p>" + itemCode + "</td>");

    scoutln(out, bytesOut, "<td valign=top><p>" + mfr + "</td>");
    scoutln(out, bytesOut, "<td valign=top><p>" + mfrCode + "</td>");

    if(buyerItemCode1.length() > 0)
      scoutln(out, bytesOut, "<td valign=top><p>" + buyerItemCode1 + "</td>");
    else scoutln(out, bytesOut, "<td></td>");
    
    if(buyerItemCode2.length() > 0)
      scoutln(out, bytesOut, "<td valign=top><p>" + buyerItemCode2 + "</td>");
    else scoutln(out, bytesOut, "<td></td>");
    
    if(buyerItemCode3.length() > 0)
      scoutln(out, bytesOut, "<td valign=top><p>" + buyerItemCode3 + "</td>");
    else scoutln(out, bytesOut, "<td></td>");
    
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
  
    if(canCart)
      scoutln(out, bytesOut, "<td><a href=\"javascript:addToCart('" + itemCode + "')\"><img border=0 src=\"" + imagesDir + "cart.png\"></a></td>");
    //else 
    scoutln(out, bytesOut, "</tr>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private int countItems(Connection con, Statement stmt, ResultSet rs, String custCode) throws Exception
  {
    int rowCount = 0;
    
    try
    {
      stmt = con.createStatement();
  
      rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM buyerscatalog WHERE CustomerCode = '" + custCode + "'");

      if(rs.next())
        rowCount = rs.getInt("rowcount");
      
      if(rs   != null) rs.close();                                 
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(rs   != null) rs.close();                                 
      if(stmt != null) stmt.close();
    }
    
    return rowCount;
  }  

  // ------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }
  
}
