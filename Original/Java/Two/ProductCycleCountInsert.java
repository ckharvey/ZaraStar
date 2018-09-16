// =======================================================================================================================================================================================================
// System: ZaraStar: Prodcut: Cycle count - insert after
// Module: ProductCycleCountInsert.java
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
import java.util.Enumeration;

public class ProductCycleCountInsert extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  DrawingUtils drawingUtils = new DrawingUtils();
  Inventory inventory = new Inventory();
  InventoryAdjustment inventoryAdjustment = new InventoryAdjustment();
  DocumentUtils documentUtils = new DocumentUtils();
  DefinitionTables definitionTables = new DefinitionTables();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", dnm="", p1="";

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
          p1 = value[0];
      }

      if(p1 == null) p1 = "";

      doIt(req, res, unm, sid, uty, dnm, p1, bytesOut);
    }
    catch(Exception e)
    {
      System.out.println(("Unexpected System Error: 3083d: " + e));
      res.getWriter().write("Unexpected System Error: 3083d");
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(HttpServletRequest req, HttpServletResponse res, String unm, String sid, String uty, String dnm, String itemCode, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    String[] checkCode = new String[1]; checkCode[0]="";

    String rtn="Unexpected System Error: 3083d";

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      rtn = "Access Denied - Duplicate SignOn or Session Timeout";
    }
    else
    {
      if(createTheStockCheckRec(con, stmt, rs, itemCode, generalUtils.todaySQLFormat(localDefnsDir, defnsDir), unm, checkCode))
        rtn = ".";
    }

    res.setContentType("text/xml");
    res.setHeader("Cache-Control", "no-cache");

    String s = "<msg><res>" + rtn + "</res><checkCode>" + checkCode[0] + "</checkCode><itemCode>" + itemCode + "</itemCode></msg>";
    res.getWriter().write(s);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 3083, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), itemCode);
    if(con != null) con.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean createTheStockCheckRec(Connection con, Statement stmt, ResultSet rs, String itemCode, String date, String unm, String[] checkCode) throws Exception
  {
    try
    {
      byte[] newCode = new byte[21];
      documentUtils.getNextCode(con, stmt, rs, "stockc", true, newCode);
      checkCode[0] = generalUtils.stringFromBytes(newCode, 0L);

      stmt = con.createStatement();

      String q = "INSERT INTO stockc ( CheckCode, ItemCode, StoreCode, Date, Level, Remark, Status, Reconciled, SignOn, Type, Location ) VALUES ('" + generalUtils.stringFromBytes(newCode, 0L) + "','" + generalUtils.sanitiseForSQL(itemCode) + "','',{d '" + date
               + "'},'999999','','L','N','" + unm + "','C','')";

      stmt.executeUpdate(q);

      if(stmt != null) stmt.close();

      return true;
    }
    catch(Exception e)
    {
      System.out.println("3083d: " + e);
      if(stmt != null) stmt.close();
    }

    return false;
  }

}
