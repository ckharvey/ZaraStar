// =======================================================================================================================================================================================================
// System: ZaraStar: Cart: Add to cart
// Module: ProductCartAddToCart.java
// Author: C.K.Harvey
// Copyright (c) 2000-08 Christopher Harvey. All Rights Reserved.
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
import java.net.*;
import java.sql.*;

public class ProductCartAddToCart extends HttpServlet
{
  GeneralUtils  generalUtils = new GeneralUtils();
  MessagePage  messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils  directoryUtils = new DirectoryUtils();
  Inventory  inventory = new Inventory();
  Profile  profile = new Profile();
  Customer customer = new Customer();
  ProductCart   productCart  = new ProductCart();
  AdminControlUtils adminControlUtils = new AdminControlUtils();
  
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="", p3="", p4="", p5="", p6="", p7="", p8="", p9="";

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
      p1  = req.getParameter("p1"); // itemCode
      p2  = req.getParameter("p2"); // sourceCall (R is remote, B if buyers, L local)
      p3  = req.getParameter("p3"); // sellPrice
      p4  = req.getParameter("p4"); // salesCurrency
      p5  = req.getParameter("p5"); // uom
      p6  = req.getParameter("p6"); // descs
      p7  = req.getParameter("p7"); // mfr
      p8  = req.getParameter("p8"); // mfrCode
      p9  = req.getParameter("p9"); // remoteSID
      
      if(p2 == null) p2 = "";

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, p3, p4, p5, p6, p7, p8, p9, bytesOut);
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

      System.out.println("121d: " + e);
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "ProductCartAddToCart", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 121, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                    String p1, String p2, String p3, String p4, String p5, String p6, String p7, String p8, String p9, int[] bytesOut)
                    throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;
    
    if((uty.equals("R") && ! adminControlUtils.notDisabled(con, stmt, rs, 908)) || (uty.equals("A") && ! adminControlUtils.notDisabled(con, stmt, rs, 808)) || (uty.equals("I") && ! authenticationUtils.verifyAccess(con, stmt, rs, req, 121, unm, uty, dnm, localDefnsDir, defnsDir)))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "ProductCart", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 121, bytesOut[0], 0, "ACC:" + p1);
      if(con  != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "ProductCart", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 121, bytesOut[0], 0, "SID:" + p1);
      if(con  != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    String[] descs         = new String[1];
    String[] sellPrice     = new String[1];
    String[] salesCurrency = new String[1];
    String[] uom           = new String[1];
    String[] itemCodeOut   = new String[1];
    String[] mfrOut        = new String[1];
    String[] mfrCodeOut    = new String[1];

    if(! p2.equals("R")) // NOT remote
    {
      // determine band
      String band;
    
      // if call is from buyers catalog, use the band for the item in the buyers catalog
      if(p2.equals("B"))
      {
        String[] customerCode = new String[1];
        int i = unm.indexOf("_");
      
        if(profile.getExternalAccessNameCustomerCode(con, stmt, rs, unm.substring(0, i), dnm, localDefnsDir, defnsDir, customerCode))
          band = getPriceBandForBuyersCatalogItem(con, stmt, rs, customerCode[0], p1);
        else band = "0"; // listprice
      } 
      else
      if(uty.equals("R"))
      {
        String[] customerCode = new String[1];
        int i = unm.indexOf("_");
      
        if(profile.getExternalAccessNameCustomerCode(con, stmt, rs, unm.substring(0, i), dnm, localDefnsDir, defnsDir, customerCode))
          band = generalUtils.intToStr(customer.getPriceBand(con, stmt, rs, customerCode[0]));
        else band = "0"; // listprice
      }
      else // I or A
        band = "0"; // listprice

      inventory.mapCodes(con, stmt, rs, p1, "", "", itemCodeOut, mfrOut, mfrCodeOut);
      inventory.getPriceDetailsGivenCode(con, stmt, rs, itemCodeOut[0], band, dnm, localDefnsDir, defnsDir, descs, sellPrice, salesCurrency, uom);

      if(descs[0].length() == 0)
       descs[0] = "-";
      else
      if(descs[0].length() > 80)
        descs[0] = descs[0].substring(0,80);
    }
    else // remote add-to-cart (from a linked-in catalog or listing)
    {
      itemCodeOut[0]   = p1;
      sellPrice[0]     = p3;
      salesCurrency[0] = p4;
      uom[0]           = p5;
      descs[0]         = p6;
      mfrOut[0]        = p7;
      mfrCodeOut[0]    = p8;
    }
    
    update(con, stmt, rs, unm, dnm, itemCodeOut[0], mfrOut[0], mfrCodeOut[0], descs[0], sellPrice[0], salesCurrency[0], uom[0]);
    
    displayCart(out, unm, uty, sid, men, den, dnm, bnm, localDefnsDir);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 121, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con  != null) con.close();
    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void update(Connection con, Statement stmt, ResultSet rs, String unm, String dnm, String itemCode, String mfr, String mfrCode, String descs,
                      String sellPrice, String salesCurrency, String uom) throws Exception
  {
    try
    {
      if(descs.length() > 80)
        descs = descs.substring(0, 80);        
        
      // create cart table if exists not
      String cartTable = productCart.createCartTable(con, stmt, unm);

      stmt = con.createStatement();
      
      // determine next line number
      int nextLine = 1;
      rs = stmt.executeQuery("SELECT Line FROM " + cartTable + " ORDER BY Line DESC");
      if(rs.next())                  
        nextLine = (generalUtils.strToInt(rs.getString(1)) + 1);
       
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();

      stmt = con.createStatement();
           
      String q = "INSERT INTO " + cartTable + " ( Line, Entry, ItemCode, Mfr, MfrCode, Description, Quantity, UoM, Price, Currency) VALUES ('"
               + nextLine + "','" + nextLine + "','" + itemCode + "','" + generalUtils.sanitiseForSQL(mfr) + "','" + generalUtils.sanitiseForSQL(mfrCode) + "','"
               + generalUtils.sanitiseForSQL(descs) + "','1','" + generalUtils.sanitiseForSQL(uom) + "','" + sellPrice + "','" + salesCurrency + "' )";
      stmt.executeUpdate(q);
        
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println("121d: " + e);
      if(stmt != null) stmt.close();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private String getPriceBandForBuyersCatalogItem(Connection con, Statement stmt, ResultSet rs, String custCode, String itemCode) throws Exception
  {
    String band = "0";
      
    try
    {
      stmt = con.createStatement();
  
      rs = stmt.executeQuery("SELECT Band FROM buyerscatalog WHERE CustomerCode = '" + custCode + "' AND ItemCode = '" + itemCode + "' ");

      if(rs.next())                  
      {
        band = rs.getString(1);
      }
      
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
    
    return band;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void displayCart(PrintWriter out, String unm, String uty, String sid, String men, String den, String dnm, String bnm, String localDefnsDir)
                           throws Exception
  { 
    URL url = new URL("http://" + serverUtils.serverToCall("OFSA", localDefnsDir) + "/central/servlet/ProductCart?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men="
                      + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm);

    URLConnection uc = url.openConnection();
    uc.setDoInput(true);
    uc.setUseCaches(false);
    uc.setDefaultUseCaches(false);

    uc.setRequestProperty("Content-Type", "application/octet-stream");

    BufferedReader di = new BufferedReader(new InputStreamReader(uc.getInputStream()));

    String s = di.readLine();
    while(s != null)
    {
      out.println(s);
      s = di.readLine();
    }

    di.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void getStockDetailsFromPricingServer(String pricingURL, String pricingUpline, String itemCode, String remoteSID, String userType,
                                                String userName, String unm, String uty, String sid, String men, String den, String bnm,
                                                String[] descs, String[] mfr, String[] mfrCode, String[] uom) throws Exception
  { 
    descs[0] = mfr[0] = mfrCode[0] = uom[0] = "";

    if(! pricingURL.startsWith("http://"))
      pricingURL = "http://" + pricingURL;
      
    URL url = new URL(pricingURL + "/central/servlet/StockProvideStockItemDetails?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den
                    + "&dnm=" + pricingUpline + "&bnm=" + bnm + "&p1=" + itemCode + "&p9=" + remoteSID + "&p14=" + userType + "&p15=" + userName);

    URLConnection uc = url.openConnection();
    uc.setDoInput(true);
    uc.setUseCaches(false);
    uc.setDefaultUseCaches(false);

    uc.setRequestProperty("Content-Type", "application/octet-stream");

    BufferedReader di = new BufferedReader(new InputStreamReader(uc.getInputStream()));

    String s = di.readLine();
    if(s != null)
    {
      int x=0, len = s.length();
      while(x < len && s.charAt(x) != '\001')
        descs[0] += s.charAt(x++);
      if(x < len) ++x;
        
      while(x < len && s.charAt(x) != '\001')
        mfr[0] += s.charAt(x++);
      if(x < len) ++x;
        
      while(x < len && s.charAt(x) != '\001')
        mfrCode[0] += s.charAt(x++);
      if(x < len) ++x;
        
      while(x < len && s.charAt(x) != '\001')
        uom[0] += s.charAt(x++);
    }

    di.close();
  }

}
