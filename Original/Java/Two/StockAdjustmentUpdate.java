// =======================================================================================================================================================================================================
// System: ZaraStar Product: stock adjustment - update for an item
// Module: StockAdjustmentUpdate.java
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

public class StockAdjustmentUpdate extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  DocumentUtils documentUtils = new DocumentUtils(); 
  
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", dnm="", code="", date="", itemCode="", storeFrom="", storeTo="", qty="", remark="", status="", soCode="", poCode="", locationFrom="", locationTo="", newOrEdit="";

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
          code = value[0];
        else
        if(name.equals("p2"))
          itemCode = value[0];
        else
        if(name.equals("p3"))
          storeFrom = value[0];
        else
        if(name.equals("p4"))
          storeTo = value[0];
        else
        if(name.equals("p5"))
          date = value[0];
        else
        if(name.equals("p6"))
          qty = value[0];
        else
        if(name.equals("p7"))
          remark = value[0];
        else
        if(name.equals("p8"))
          status = value[0];
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
        else
        if(name.equals("p13"))
          newOrEdit = value[0];
      }

      if(status == null) status = "L";
      if(soCode == null) soCode = "";
      if(poCode == null) poCode = "";
      if(locationFrom == null) locationFrom = "";
      if(locationTo   == null) locationTo   = "";
      
      doIt(req, res, unm, sid, uty, dnm, code, date, itemCode, storeFrom, storeTo, qty, soCode, poCode, remark, status, locationFrom, locationTo, newOrEdit, bytesOut);
    }    
    catch(Exception e)
    {
      serverUtils.etotalBytes(req, unm, dnm, 3011, bytesOut[0], 0, "ERR:" + code);
      System.out.println(("Unexpected System Error: 3011a: " + e));
      res.getWriter().write("Unexpected System Error: 3011a");
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(HttpServletRequest req, HttpServletResponse res, String unm, String sid, String uty, String dnm, String code, String date, String itemCode, String storeFrom, String storeTo, String qty, String soCode, String poCode,
                    String remark, String status, String locationFrom, String locationTo, String newOrEdit, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
 
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    String rtn="Unexpected System Error: 3011a";
    
    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      rtn = "Access Denied - Duplicate SignOn or Session Timeout";
    }
    else
    {
      code      = generalUtils.stripLeadingAndTrailingSpaces(code);
      date      = generalUtils.stripLeadingAndTrailingSpaces(date);
      itemCode  = (generalUtils.stripLeadingAndTrailingSpaces(itemCode).toUpperCase());
      storeFrom = generalUtils.stripLeadingAndTrailingSpaces(storeFrom);
      storeTo   = generalUtils.stripLeadingAndTrailingSpaces(storeTo);
      qty       = generalUtils.stripLeadingAndTrailingSpaces(qty);
      soCode    = generalUtils.stripAllSpaces(soCode);
      poCode    = generalUtils.stripAllSpaces(poCode);
      remark    = generalUtils.stripLeadingAndTrailingSpaces(remark);
      status    = generalUtils.stripLeadingAndTrailingSpaces(status);
      locationFrom = generalUtils.stripLeadingAndTrailingSpaces(locationFrom);
      locationTo   = generalUtils.stripLeadingAndTrailingSpaces(locationTo);

      if(soCode.equals("."))
        soCode = "";
      
      if(poCode.equals("."))
        poCode = "";
      
      if(qty.length() == 0)
        qty = "0";

      if(locationFrom.length() == 0)
        locationFrom = "Unknown";

      if(locationTo.length() == 0)
        locationTo = "Unknown";

      boolean newRec;
      if(newOrEdit.equals("N"))
        newRec = true;
      else newRec = false;

      if(! serverUtils.passLockCheck(con, stmt, rs, "stocka", generalUtils.convertDateToSQLFormat(date), unm))
        rtn = "Cannot use a Date in a Locked Period";
      else
      if(date.length() == 0)
        rtn = "No Date Entered";
      else
      if(! generalUtils.validateDate(false, date, localDefnsDir, defnsDir))
        rtn = "Invalid Date Entered";
      else
      if(itemCode.length() == 0)
      {
        if(! newRec)
          rtn = "No Item Code Entered";
      }
      else
      if(! generalUtils.isNumeric(qty))
        rtn = "Invalid Stock Quantity Entered";
      else
      {
        date = generalUtils.convertDateToSQLFormat(date);  
   
        switch(update(con, stmt, rs, newRec, unm, code, itemCode, storeFrom, storeTo, date, qty, soCode, poCode, remark, status, locationFrom, locationTo))
        {
          case ' ' : rtn = ".";  break;
        }
      }
    }
    
    res.setContentType("text/xml");
    res.setHeader("Cache-Control", "no-cache");
    
    String s = "<msg><res>" + rtn + "</res></msg>";
    res.getWriter().write(s);
    
      serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 3011, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), code);
      if(con  != null) con.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // rtns: ' ' if rec added/updated ok
  //       'E' if adding a rec that already exists
  //       'X' other error
  private char update(Connection con, Statement stmt, ResultSet rs, boolean newRec, String unm, String code, String itemCode, String storeFrom, String storeTo, String date, String qty, String soCode, String poCode, String remark, String status,
                      String locationFrom, String locationTo) throws Exception
  {
    try
    {
      if(newRec)
      {    
        byte[] newCode = new byte[21];
        documentUtils.getNextCode(con, stmt, rs, "stocka", true, newCode);

        stmt = con.createStatement();

        String q = "INSERT INTO stocka ( AdjustmentCode, ItemCode, Date, Quantity, SignOn, Remark, StoreFrom, StoreTo, Status, SOCode, POCode, LocationFrom, LocationTo ) "
                 + "VALUES ('" + generalUtils.stringFromBytes(newCode, 0L) + "','" + itemCode + "',{d '" + date + "'},'" + qty + "','" + unm + "','" + generalUtils.sanitiseForSQL(remark) + "','" + generalUtils.sanitiseForSQL(storeFrom) + "','"
                 + generalUtils.sanitiseForSQL(storeTo) + "','" + status + "','" + generalUtils.sanitiseForSQL(soCode) + "','" + generalUtils.sanitiseForSQL(poCode) + "','" + generalUtils.sanitiseForSQL(locationFrom) + "','" + generalUtils.sanitiseForSQL(locationTo) + "')";

        stmt.executeUpdate(q);
        
        if(stmt != null) stmt.close();
        
        return ' ';
      }
  
      // else update existing rec  
      stmt = con.createStatement();
        
      String q = "UPDATE stocka SET ItemCode = '" + itemCode + "', StoreFrom = '" + generalUtils.sanitiseForSQL(storeFrom) + "', StoreTo = '" + generalUtils.sanitiseForSQL(storeTo) + "', Date = {d '" + date + "'}, Quantity = '" + qty + "', Remark = '"
               + generalUtils.sanitiseForSQL(remark) + "', Status = '" + status + "', SOCode = '" + generalUtils.sanitiseForSQL(soCode) + "', POCode = '" + generalUtils.sanitiseForSQL(poCode) + "', LocationFrom = '" + generalUtils.sanitiseForSQL(locationFrom)
               + "', LocationTo = '" + generalUtils.sanitiseForSQL(locationTo) + "' WHERE AdjustmentCode = '" + code + "'";
     
      stmt.executeUpdate(q);
        
      if(stmt != null) stmt.close();        
            
      return ' ';
    }
    catch(Exception e)
    {
      System.out.println("3011a: " + e);
      if(stmt != null) stmt.close();
    }
    
    return 'X';
  }

}
