// =======================================================================================================================================================================================================
// System: ZaraStar Product: stock check - update for an item
// Module: StockCheckEditUpdate.java
// Author: C.K.Harvey
// Copyright (c) 2006-11 Christopher Harvey. All Rights Reserved.
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

public class StockCheckEditUpdate extends HttpServlet
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

    String unm="", sid="", uty="", dnm="", code="", date="", itemCode="", store="", level="", remark="", status="", reconciled="", type="", newOrEdit="", location="";

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
          store = value[0];
        else
        if(name.equals("p4"))
          date = value[0];
        else
        if(name.equals("p5"))
          level = value[0];
        else
        if(name.equals("p6"))
          remark = value[0];
        else
        if(name.equals("p7"))
          status = value[0];
        else
        if(name.equals("p8"))
          reconciled = value[0];
        else
        if(name.equals("p9"))
          type = value[0];
        else
        if(name.equals("p10"))
          location = value[0];
        else
        if(name.equals("p11"))
          newOrEdit = value[0];
      }
    
      if(status == null)
        status = "L";
      
      doIt(req, res, unm, sid, uty, dnm, code, date, itemCode, store, location, level, remark, status, reconciled, type, newOrEdit, bytesOut);
    }    
    catch(Exception e)
    {
      serverUtils.etotalBytes(req, unm, dnm, 3018, bytesOut[0], 0, "ERR:" + code);
      System.out.println(("Unexpected System Error: 3018a: " + e));
      res.getWriter().write("Unexpected System Error: 3018a");
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(HttpServletRequest req, HttpServletResponse res, String unm, String sid, String uty, String dnm, String code, String date, String itemCode, String store, String location, String level, String remark, String status,
                    String reconciled, String type, String newOrEdit, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
 
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    String rtn="Unexpected System Error: 3018a";
    
    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      rtn = "Access Denied - Duplicate SignOn or Session Timeout";
    }
    else
    {
      code     = generalUtils.stripLeadingAndTrailingSpaces(code);
      date     = generalUtils.stripLeadingAndTrailingSpaces(date);
      itemCode = (generalUtils.stripLeadingAndTrailingSpaces(itemCode).toUpperCase());
      store    = generalUtils.stripLeadingAndTrailingSpaces(store);
      location = generalUtils.stripLeadingAndTrailingSpaces(location);
      level    = generalUtils.stripLeadingAndTrailingSpaces(level);
      remark   = generalUtils.stripLeadingAndTrailingSpaces(remark);

      if(level.length() == 0)
        level = "0";

      if(location.length() == 0)
        location = "Unknown";
      
      boolean newRec;
      if(newOrEdit.equals("N"))
        newRec = true;
      else newRec = false;

      if(! serverUtils.passLockCheck(con, stmt, rs, "stockc", generalUtils.convertDateToSQLFormat(date), unm))
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
      if(! generalUtils.isNumeric(level))
        rtn = "Invalid Stock Level Entered";
      else
      {
        date = generalUtils.convertDateToSQLFormat(date);  
   
        switch(update(con, stmt, rs, newRec, unm, code, itemCode, store, location, date, level, remark, status, reconciled, type))
        {
          case ' ' : rtn = ".";  break;
        }
      }
    }
    
    res.setContentType("text/xml");
    res.setHeader("Cache-Control", "no-cache");
    
    String s = "<msg><res>" + rtn + "</res></msg>";
    res.getWriter().write(s);
    
    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 3018, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), code);
    if(con  != null) con.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // rtns: ' ' if rec added/updated ok
  //       'E' if adding a rec that already exists
  //       'X' other error
  private char update(Connection con, Statement stmt, ResultSet rs, boolean newRec, String unm, String code, String itemCode, String store, String location, String date, String level, String remark, String status, String reconciled, String type)
                      throws Exception
  {
    try
    {
      if(newRec)
      {    
        byte[] newCode = new byte[21];
        documentUtils.getNextCode(con, stmt, rs, "stockc", true, newCode);

        stmt = con.createStatement();
           
        String q = "INSERT INTO stockc ( CheckCode, ItemCode, StoreCode, Date, Level, Remark, Status, Reconciled, SignOn, Type, Location ) VALUES ('" + generalUtils.stringFromBytes(newCode, 0L) + "','" + generalUtils.sanitiseForSQL(itemCode) + "','"
                 + generalUtils.sanitiseForSQL(store) + "',{d '" + date + "'},'" + level + "','" + generalUtils.sanitiseForSQL(remark) + "','" + status + "','" + reconciled + "','" + unm + "','" + type + "','" + generalUtils.sanitiseForSQL(location) + "')";

        stmt.executeUpdate(q);
        
        if(stmt != null) stmt.close();
        
        return ' ';
      }
  
      // else update existing rec  
      stmt = con.createStatement();
        
      String q = "UPDATE stockc SET ItemCode = '" + itemCode + "', StoreCode = '" + generalUtils.sanitiseForSQL(store) + "', Date = {d '" + date + "'}, Level = '" + level + "', Remark = '" + generalUtils.sanitiseForSQL(remark) + "', Status = '" + status
               + "', Reconciled = '" + reconciled + "', Type = '" + type + "', SignOn = '" + unm + "', Location = '" + generalUtils.sanitiseForSQL(location) + "' WHERE CheckCode = '" + code + "'";
     
      stmt.executeUpdate(q);
        
      if(stmt != null) stmt.close();        
            
      return ' ';
    }
    catch(Exception e)
    {
      System.out.println("3018a: " + e);
      if(stmt != null) stmt.close();
    }
    
    return 'X';
  }

}
