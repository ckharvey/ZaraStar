// =======================================================================================================================================================================================================
// System: ZaraStar Product: stock adjustments - update for data entry mode
// Module: StockAdjustmentDataEntry.java
// Author: C.K.Harvey
// Copyright (c) 2006-09 Christopher Harvey. All Rights Reserved.
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

public class StockAdjustmentDataEntry extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  DocumentUtils documentUtils = new DocumentUtils(); 
  Inventory inventory = new Inventory();
  
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", dnm="", date="", itemCode="", storeFrom="", storeTo="", qty="", remark="", mfr="", mfrCode="", soCode="", poCode="", locationFrom="", locationTo="";

    try
    {
      directoryUtils.setContentHeaders(res);
      res.setHeader("Header", "HTTP/1.0 200 OK");
      res.setStatus(200);

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
        if(name.equals("dnm"))
          dnm = value[0];
        else
        if(name.equals("p1"))
          itemCode = value[0];
        else
        if(name.equals("p2"))
          storeFrom = value[0];
        else
        if(name.equals("p3"))
          storeTo = value[0];
        else
        if(name.equals("p4"))
          date = value[0];
        else
        if(name.equals("p5"))
          qty = value[0];
        else
        if(name.equals("p6"))
          remark = value[0];
        else
        if(name.equals("p7"))
          mfr = value[0];
        else
        if(name.equals("p8"))
          mfrCode = value[0];
        else
        if(name.equals("p9"))
          soCode = value[0];
        else
        if(name.equals("p10"))
          poCode = value[0];
        else
        if(name.equals("p11"))
          locationFrom = value[0];
        else
        if(name.equals("p12"))
          locationTo = value[0];
      }
      
      if(soCode == null) soCode = "";
      if(poCode == null) poCode = "";
      if(locationFrom == null) locationFrom = "";
      if(locationTo   == null) locationTo   = "";
      
      doIt(req, res, unm, sid, uty, dnm, itemCode, date, storeFrom, storeTo, locationFrom, locationTo, qty, remark, mfr, mfrCode, soCode, poCode, bytesOut);
    }    
    catch(Exception e)
    {
      serverUtils.etotalBytes(req, unm, dnm, 3012, bytesOut[0], 0, "ERR:" + itemCode);
      System.out.println(("Unexpected System Error: 3012a: " + e));
      res.getWriter().write("Unexpected System Error: 3012a");
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(HttpServletRequest req, HttpServletResponse res, String unm, String sid, String uty, String dnm, String itemCode, String date, String storeFrom, String storeTo, String locationFrom, String locationTo, String qty,
                    String remark, String mfr, String mfrCode, String soCode, String poCode, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
 
    String uName    = directoryUtils.getMySQLUserName();
    String passWord = directoryUtils.getMySQLPassWord();

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + uName + "&password=" + passWord);

    Statement stmt = null;
    ResultSet rs   = null;

    String rtn="Unexpected System Error: 3012a";
    
    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      rtn = "Access Denied - Duplicate SignOn or Session Timeout";
    }
    else
    {
      itemCode     = (generalUtils.stripLeadingAndTrailingSpaces(itemCode).toUpperCase());
      date         = generalUtils.stripLeadingAndTrailingSpaces(date);
      storeFrom    = generalUtils.stripLeadingAndTrailingSpaces(storeFrom);
      storeTo      = generalUtils.stripLeadingAndTrailingSpaces(storeTo);
      qty          = generalUtils.stripLeadingAndTrailingSpaces(qty);
      remark       = generalUtils.stripLeadingAndTrailingSpaces(remark);
      mfrCode      = generalUtils.stripLeadingAndTrailingSpaces(mfrCode);
      locationFrom = generalUtils.stripLeadingAndTrailingSpaces(locationFrom);
      locationTo   = generalUtils.stripLeadingAndTrailingSpaces(locationTo);
      soCode       = generalUtils.stripAllSpaces(soCode);
      poCode       = generalUtils.stripAllSpaces(poCode);

      if(qty.length() == 0)
        qty = "0";

      if(locationFrom.length() == 0)
        locationFrom = "Unknown";

      if(locationTo.length() == 0)
        locationTo = "Unknown";

      if(itemCode.length() == 0 && mfrCode.length() == 0)
        rtn = "No Item, or Manufacturer, Code Entered";
      else
      {
        if(itemCode.length() == 0)
        {
          String[] itemCodeOut = new String[1];
          String[] mfrOut      = new String[1];
          String[] mfrCodeOut  = new String[1];
          inventory.mapCodes(con, stmt, rs, itemCode, mfr, mfrCode, itemCodeOut, mfrOut, mfrCodeOut);
          itemCode = itemCodeOut[0];
        }
      
        if(! inventory.existsItemRecGivenCode(con, stmt, rs, itemCode))
          rtn = "Unknown Item, or Manufacturer, Code Entered";
        else      
        if(date.length() == 0)
          rtn = "No Date Entered";
        else
        if(! generalUtils.validateDate(false, date, localDefnsDir, defnsDir))
          rtn = "Invalid Date Entered";
        else
        if(! generalUtils.isNumeric(qty))
          rtn = "Invalid Stock Quantity Entered";
        else
        {
          date = generalUtils.convertDateToSQLFormat(date);
        
          byte[] checkCode = new byte[21];
   
          switch(update(con, stmt, rs, unm, itemCode, storeFrom, storeTo, locationFrom, locationTo, date, qty, remark, soCode, poCode, checkCode))
          {
            case ' ' : rtn = "Record Added: " + generalUtils.stringFromBytes(checkCode, 0L);  break;
          }
        }
      }
    }
    
    res.setContentType("text/xml");
    res.setHeader("Cache-Control", "no-cache");
    
    String s = "<msg><res>" + rtn + "</res></msg>";
    res.getWriter().write(s);
    
      serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 3012, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), itemCode);
    if(con  != null) con.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // rtns: ' ' if rec added/updated ok
  //       'X' other error
  private char update(Connection con, Statement stmt, ResultSet rs, String unm, String itemCode, String storeFrom, String storeTo, String locationFrom, String locationTo, String date, String qty, String remark, String soCode, String poCode,
                       byte[] newCode) throws Exception
  {
    try
    {
      documentUtils.getNextCode(con, stmt, rs, "stocka", true, newCode);

      stmt = con.createStatement();
           
      String q = "INSERT INTO stocka ( AdjustmentCode, ItemCode, StoreFrom, StoreTo, Date, Quantity, Remark, Status, SignOn, SOCode, POCode, LocationFrom, LocationTo ) "
               + "VALUES ('" + generalUtils.stringFromBytes(newCode, 0L) + "','" + itemCode + "','" + generalUtils.sanitiseForSQL(storeFrom) + "','" + generalUtils.sanitiseForSQL(storeTo) + "',{d '" + date + "'},'" + qty + "','" + generalUtils.sanitiseForSQL(remark)
               + "','L','" + unm + "','" +  generalUtils.sanitiseForSQL(soCode) + "','" +  generalUtils.sanitiseForSQL(poCode) + "','" +  generalUtils.sanitiseForSQL(locationFrom) + "','" +  generalUtils.sanitiseForSQL(locationTo) + "')";

      stmt.executeUpdate(q);
      
      if(stmt != null) stmt.close();
        
      return ' ';
    }
    catch(Exception e)
    {
      System.out.println("3012a: " + e);
      if(stmt != null) stmt.close();
    }
    
    return 'X';
  }

}
