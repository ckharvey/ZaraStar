// =======================================================================================================================================================================================================
// System: ZaraStar: Product: Remove Stock Record: do it
// Module: StockRecordRemoveExecute.java
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

public class StockRecordRemoveExecute extends HttpServlet implements SingleThreadModel
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
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
    String urlBit="";

    try
    {
      res.setContentType("text/html");
      out = res.getWriter();

      unm = req.getParameter("unm");
      sid = req.getParameter("sid");
      uty = req.getParameter("uty");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");
      p1  = req.getParameter("p1"); // itemCode
    
      if(p1 == null) p1 = "";
      
      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, bytesOut);
    }
    catch(Exception e)
    {
      StringBuffer url = req.getRequestURL();
      int x=0;
      if(url.charAt(x) == 'h' || url.charAt(x) == 'H')
        x += 7;
      urlBit="";
      while(url.charAt(x) != '\000' && url.charAt(x) != ',' && url.charAt(x) != '/')
        urlBit += url.charAt(x++);

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "StockRecordRemoveExecute", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3016, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush(); 
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1, int[] bytesOut) throws Exception
  {
    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    long startTime = new java.util.Date().getTime();

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 3016, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "StockRecordRemoveExecute", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3016, bytesOut[0], 0, "ACC:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "StockRecordRemoveExecute", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3016, bytesOut[0], 0, "SID:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }
    
    String itemCode = p1.toUpperCase();

    if(itemCode.length() == 0)
      messagePage.msgScreen(false, out, req, 0, unm, sid, uty, men, den, dnm, bnm, "3016", "", "", "No Item Code Specified", imagesDir, localDefnsDir, defnsDir, bytesOut);
    else
    {
      if(! inventory.existsItemRecGivenCode(con, stmt, rs, itemCode))
        messagePage.msgScreen(false, out, req, 0, unm, sid, uty, men, den, dnm, bnm, "3016", "", "", "No Item Code Specified", imagesDir, localDefnsDir, defnsDir, bytesOut);
      else
      {
        process(con, stmt, "quotel",    itemCode);
        process(con, stmt, "sol",       itemCode);
        process(con, stmt, "ocl",       itemCode);
        process(con, stmt, "pll",       itemCode);
        process(con, stmt, "dol",       itemCode);
        process(con, stmt, "invoicel",  itemCode);
        process(con, stmt, "debitl",    itemCode);
        process(con, stmt, "creditl",   itemCode);
        process(con, stmt, "proformal", itemCode);

        process(con, stmt, "pol",       itemCode);
        process(con, stmt, "lpl",       itemCode);
        process(con, stmt, "grl",       itemCode);
        process(con, stmt, "pinvoicel", itemCode);
        process(con, stmt, "pdebitl",   itemCode);
        process(con, stmt, "pcreditl",  itemCode);

        process(con, stmt, "stocka",    itemCode);
        process(con, stmt, "stockc",    itemCode);

        // now remove old rec from stock file (also removes any stockx recs)
        inventory.stockDeleteRec(con, stmt, rs, itemCode, dnm, localDefnsDir, defnsDir);

        messagePage.msgScreen(false, out, req, 21, unm, sid, uty, men, den, dnm, bnm, "StockRecordRemoveExecute", imagesDir, localDefnsDir, defnsDir, bytesOut);
      }
    }

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 3016, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), itemCode);
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void process(Connection con, Statement stmt, String tableName, String itemCode) throws Exception
  {
    stmt = con.createStatement();
    
    stmt.executeUpdate("UPDATE " + tableName + " SET ItemCode = '-' WHERE ItemCode = '" + itemCode + "'");

    if(stmt != null) stmt.close();
  }

}

