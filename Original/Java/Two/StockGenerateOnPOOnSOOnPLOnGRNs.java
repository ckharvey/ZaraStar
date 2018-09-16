// =======================================================================================================================================================================================================
// System: ZaraStar: Stock: Generate OnPO, OnSO, OnPL, OnGRNs 
// Module: StockGenerateOnPOOnSOOnPLOnGRNs.java
// Author: C.K.Harvey
// Copyright (c) 2000-07 Christopher Harvey. All Rights Reserved.
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
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class StockGenerateOnPOOnSOOnPLOnGRNs extends HttpServlet
{
  GeneralUtils  generalUtils = new GeneralUtils();
  MessagePage  messagePage = new MessagePage();
  MiscDefinitions  miscDefinitions = new MiscDefinitions();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils  pageFrameUtils = new PageFrameUtils();
  DirectoryUtils  directoryUtils = new DirectoryUtils();
  Inventory  inventory = new Inventory();
  GoodsReceivedNote  goodsReceivedNote = new GoodsReceivedNote();
  PickingList  pickingList = new PickingList();
  PurchaseOrder  purchaseOrder = new PurchaseOrder();
  SalesOrder salesOrder = new SalesOrder();
  
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
      p1  = req.getParameter("p1");
      
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

      System.out.println("3059: " + e);
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "StockGenerateOnPOOnSOOnPLOnGRNs", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3059, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den,
                    String dnm, String bnm, String p1, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    Statement stmt2 = null;
    ResultSet rs   = null;
    ResultSet rs2   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 3059, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      out.println("0\0010\0010\0010\001");
      serverUtils.etotalBytes(req, unm, dnm, 3059, bytesOut[0], 0, "ACC:" + p1);
    if(con  != null) con.close();  
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      out.println("0\0010\0010\0010\001");
      serverUtils.etotalBytes(req, unm, dnm, 3059, bytesOut[0], 0, "SID:" + p1);
    if(con  != null) con.close();  
      if(out != null) out.flush(); 
      return;
    }

    set(con, stmt, stmt2, rs, rs2, out, p1.toUpperCase(), dnm, localDefnsDir, defnsDir);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 3059, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con  != null) con.close();  
    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, String itemCode, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    String rtnStr = onPO(con, stmt, stmt2, rs, rs2, itemCode, dnm, localDefnsDir, defnsDir) + "\001";

    rtnStr += onSO(con, stmt, stmt2, rs, rs2, itemCode, dnm, localDefnsDir, defnsDir) + "\001";

    rtnStr += onPLNotCompleted(con, stmt, rs, itemCode, dnm, localDefnsDir, defnsDir) + "\001";

    rtnStr += onGRNInTransit(con, stmt, rs, itemCode, dnm, localDefnsDir, defnsDir) + "\001";

    out.println(rtnStr);
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private double onPO(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String itemCode, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT t2.POCode, t2.Line, t2.Quantity FROM pol AS t2 INNER JOIN po AS t1 "
                                   + "ON t2.POCode = t1.POCode WHERE t2.ItemCode = '" + itemCode + "' AND t2.Received != 'R' "
                                   + "AND t1.Status != 'C' AND t1.AllReceived != 'Y' ORDER BY t1.Date, t2.POCode, t2.Line");

    String poCode, poLine;
    double qty, actualQty, total = 0.0;
    
    while(rs.next())
    {    
      poCode = rs.getString(1);
      poLine = rs.getString(2);
      qty    = generalUtils.doubleFromStr(rs.getString(3));
     
      actualQty = goodsReceivedNote.getTotalReceivedForAPOLine(con, stmt2, rs2, poCode, poLine);

      if(actualQty < qty)
      {            
        total += (qty - actualQty);
      }
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return total;
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private double onSO(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String itemCode, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.SOCode, t2.Line, t2.Quantity FROM sol AS t2 INNER JOIN so AS t1 "
                                   + "ON t2.SOCode = t1.SOCode WHERE t2.ItemCode = '" + itemCode + "' AND t1.Status != 'C' "
                                   + "AND t1.AllSupplied != 'Y' ORDER BY t1.Date, t2.SOCode, t2.Line");

    String soCode, soLine;
    double qty, actualQty, total = 0.0;;

    while(rs.next())
    {    
      soCode = rs.getString(1);
      soLine = rs.getString(2);
      qty    = generalUtils.doubleFromStr(rs.getString(3));

      actualQty = pickingList.getTotalPickedForASOLine(con, stmt2, rs2, soCode, soLine);

      if(actualQty < qty)
      {
        total += (qty - actualQty);
      }
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return total;
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private double onPLNotCompleted(Connection con, Statement stmt, ResultSet rs, String itemCode, String dnm, String localDefnsDir, String defnsDir)
                                  throws Exception
  {
    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT t2.QuantityPacked FROM pll AS t2 INNER JOIN pl AS t1 ON t2.PLCode = t1.PLCode"
                                   + " WHERE t2.ItemCode = '" + itemCode + "' AND t1.Status != 'C' AND t1.Completed != 'Y'");

    double total = 0.0;
    
    while(rs.next())
    {    
      total += generalUtils.doubleFromStr(rs.getString(1));
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return total;
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private double onGRNInTransit(Connection con, Statement stmt, ResultSet rs, String itemCode, String dnm, String localDefnsDir, String defnsDir)
                                throws Exception
  {
    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT t2.Quantity FROM grl AS t2 INNER JOIN gr AS t1 ON t2.GRCode = t1.GRCode "
                                   + "WHERE t2.ItemCode = '" + itemCode + "' AND t1.Status != 'C' AND t1.StockProcessed != 'Y'");

    double total = 0.0;
    
    while(rs.next())
    {    
      total += generalUtils.doubleFromStr(rs.getString(1));
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return total;
  }

}
