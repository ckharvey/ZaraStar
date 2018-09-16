// =======================================================================================================================================================================================================
// System: ZaraStar Cart: update
// Module: ProductCartUpdate.java
// Author: C.K.Harvey
// Copyright (c) 2006-08 Christopher Harvey. All Rights Reserved.
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
import java.util.Enumeration;

public class ProductCartUpdate extends HttpServlet
{
  GeneralUtils  generalUtils = new GeneralUtils();
  MessagePage  messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils  directoryUtils = new DirectoryUtils();
  Inventory  inventory = new Inventory();
  Customer customer = new Customer();
  Profile  profile = new Profile();
  
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", dnm="", line="", entry="", itemCode="", mfr="", mfrCode="", desc="", qty="", uom="", price="", currency="",
           newOrEdit="", cartTable="";

    try
    {
      directoryUtils.setContentHeaders(res);
      res.setHeader("Header", "HTTP/1.0 200 OK");
      res.setStatus(200);

      Enumeration en = req.getParameterNames();
      while(en.hasMoreElements())
      {
        String elementName = (String)en.nextElement();
        String[] value = req.getParameterValues(elementName);
        if(elementName.equals("unm"))
          unm = value[0];
        else
        if(elementName.equals("sid"))
          sid = value[0];
        else
        if(elementName.equals("uty"))
          uty = value[0];
        else
        if(elementName.equals("dnm"))
          dnm = value[0];
        else
        if(elementName.equals("p1"))
          line = value[0];
        else
        if(elementName.equals("p2"))
          entry = value[0];
        else
        if(elementName.equals("p3"))
          itemCode = value[0];
        else
        if(elementName.equals("p4"))
          mfr = value[0];
        else
        if(elementName.equals("p5"))
          mfrCode = value[0];
        else
        if(elementName.equals("p6"))
          desc = value[0];
        else
        if(elementName.equals("p7"))
          qty = value[0];
        else
        if(elementName.equals("p8"))
          uom = value[0];
        else
        if(elementName.equals("p9"))
          price = value[0];
        else
        if(elementName.equals("p10"))
          currency = value[0];
        else
        if(elementName.equals("p11"))
          cartTable = value[0];
        else
        if(elementName.equals("p12"))
          newOrEdit = value[0];
      }

      if(entry    == null) entry = line;
      if(itemCode == null) itemCode = "";
      if(mfr      == null) mfr = "";
      if(mfrCode  == null) mfrCode = "";
      if(desc     == null) desc = "";
      if(qty      == null) qty = "1";
      if(uom      == null) uom = "Each";
      if(price    == null) price = "0";
      if(currency == null) currency = "";
      
      doIt(req, res, unm, sid, uty, dnm, cartTable, line, entry, itemCode, mfr, mfrCode, desc, qty, uom, price, currency, newOrEdit, bytesOut);
    }    
    catch(Exception e)
    {
      System.out.println(("Unexpected System Error: 121a: " + e));
      res.getWriter().write("Unexpected System Error: 121a");
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(HttpServletRequest req, HttpServletResponse res, String unm, String sid, String uty, String dnm, String cartTable, String line,
                    String entry, String itemCode, String mfr, String mfrCode, String desc, String qty, String uom, String price, String currency,
                    String newOrEdit, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
 
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;
    
    String rtn="Unexpected System Error: 121a";
    
    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      rtn = "Access Denied - Duplicate SignOn or Session Timeout";
    }
    else
    {
      line     = generalUtils.stripLeadingAndTrailingSpaces(line);
      entry    = generalUtils.stripLeadingAndTrailingSpaces(entry);
      itemCode = generalUtils.stripLeadingAndTrailingSpaces(itemCode);
      mfrCode  = generalUtils.stripLeadingAndTrailingSpaces(mfrCode);
      desc     = generalUtils.stripLeadingAndTrailingSpaces(desc);
      qty      = generalUtils.stripLeadingAndTrailingSpaces(qty);

      if(line.length() == 0)
        rtn = "No Line Entered";
      else
      if(! generalUtils.isInteger(line))
        rtn = "Invalid Line Entered";
      else
      if(! generalUtils.isNumeric(qty))
        rtn = "Invalid Quantity Entered";
      else
      {
        boolean newRec;
        if(newOrEdit.equals("N"))
          newRec = true;
        else newRec = false;

        if(entry.length() == 0)
          entry = line;
   
        if(mfrCode.length() == 0)
          mfrCode = "-";
   
        if(qty.length() == 0)
          qty = "1";
   
        if(uom.length() == 0)
          uom = "Each";
   
        if(price.length() == 0)
          price = "0.00";
   
        if(currency.length() == 0)
          currency = "";
   
        String[] itemCodeOut = new String[1];
        String[] mfrOut      = new String[1];
        String[] mfrCodeOut  = new String[1];

        inventory.mapCodes(con, stmt, rs, itemCode, mfr, mfrCode, itemCodeOut, mfrOut, mfrCodeOut);
        if(mfrOut[0].length() == 0)
          mfrOut[0] = mfr;
        if(mfrCodeOut[0].length() == 0)
          mfrCodeOut[0] = mfrCode;

        if(newRec)
        {
          // determine band
          String band;
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
            
          String[] descs         = new String[1];
          String[] sellPrice     = new String[1];
          String[] salesCurrency = new String[1];
          String[] uom2           = new String[1];

          inventory.getPriceDetailsGivenCode(con, stmt, rs, itemCodeOut[0], band, dnm, localDefnsDir, defnsDir, descs, sellPrice, salesCurrency, uom2);

          if(descs[0] == null || descs[0].length() == 0)
            descs[0] = "-";
          else
          if(descs[0].length() > 80)
            descs[0] = descs[0].substring(0,80);
          desc = descs[0];
          
          //qty = "1";
          uom = uom2[0];
          price = sellPrice[0];
          currency = salesCurrency[0];
        }
   
        switch(update(con, stmt, rs, newRec, cartTable, line, entry, itemCodeOut[0], mfrOut[0], mfrCodeOut[0], desc, qty, uom, price, currency))
        {
          case ' ' : rtn = ".";                   break;
          case 'E' : rtn = "Line Already Exists"; break;
        }
      }
    }
    
    res.setContentType("text/xml");
    res.setHeader("Cache-Control", "no-cache");
    
    String s = "<msg><res>" + rtn + "</res></msg>";
    res.getWriter().write(s);
    
    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 121, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), itemCode);
    if(con  != null) con.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // rtns: ' ' if rec added/updated ok
  //       'E' if adding a rec that already exists
  //       'X' other error
  private char update(Connection con, Statement stmt, ResultSet rs, boolean newRec, String cartTable, String line, String entry, String itemCode,
                      String mfr, String mfrCode, String desc, String qty, String uom, String price, String currency) throws Exception
  {
    desc     = generalUtils.sanitiseForSQL(desc);
    entry    = generalUtils.sanitiseForSQL(entry);
    itemCode = generalUtils.sanitiseForSQL(itemCode);
    mfr      = generalUtils.sanitiseForSQL(mfr);
    mfrCode  = generalUtils.sanitiseForSQL(mfrCode);
    qty      = generalUtils.sanitiseForSQL(qty);
    uom      = generalUtils.sanitiseForSQL(uom);
    currency = generalUtils.sanitiseForSQL(currency);
    
    try
    {
      if(newRec)
      {    
        // if adding a new rec, check if already exists
        boolean alreadyExists=false;
        stmt = con.createStatement();
  
        rs = stmt.executeQuery("SELECT Line FROM " + cartTable + " WHERE Line = '" + line + "'"); 
        if(rs.next())
          alreadyExists = true;
        if(rs != null) rs.close() ;
  
        if(stmt != null) stmt.close();
      
        if(alreadyExists)
        {
          if(con != null) con.close();
          return 'E';
        }
  
        stmt = con.createStatement();
           
        String q = "INSERT INTO " + cartTable + " ( Line, Entry, ItemCode, Mfr, MfrCode, Description, Quantity, UoM, Price, Currency) VALUES ('"
                 + line + "','" + entry + "','" + itemCode + "','" + mfr + "','" + mfrCode + "','" + desc + "','" + qty + "','" + uom + "','" + price
                 + "','" + currency + "' )";

        stmt.executeUpdate(q);
        
        if(stmt != null) stmt.close();
        
        return ' ';
      }
  
      // else update existing rec  
      stmt = con.createStatement();
        
      String q = "UPDATE " + cartTable + " SET Line = '" + line + "', Entry = '" + entry + "', ItemCode = '" + itemCode + "', Mfr = '" + mfr
              + "', MfrCode = '" + mfrCode + "', Description = '" + desc + "', Quantity = '" + qty + "', UoM = '" + uom + "', Price = '" + price
              + "', Currency = '" + currency + "' WHERE Line = '" + line + "'";

      stmt.executeUpdate(q);
        
      if(stmt != null) stmt.close();        
            
      return ' ';
    }
    catch(Exception e)
    {
      System.out.println("121a: " + e);
      if(stmt != null) stmt.close();
    }
    
    return 'X';
  }

}
