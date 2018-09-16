// =======================================================================================================================================================================================================
// System: ZaraStar: product: Move Store to Store
// Module: StoreStoreMoveExecute.java
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
import java.sql.*;

public class StoreStoreMoveExecute extends HttpServlet
{
  GeneralUtils  generalUtils = new GeneralUtils();
  MessagePage  messagePage = new MessagePage();
  MiscDefinitions  miscDefinitions = new MiscDefinitions();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils  pageFrameUtils = new PageFrameUtils();
  DirectoryUtils  directoryUtils = new DirectoryUtils();
  Inventory  inventory = new Inventory();
  StockLevelsGenerate stockLevelsGenerate = new StockLevelsGenerate();
  
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="", p3="", p4="";

    try
    {
      directoryUtils.setContentHeaders2(res);
      out = res.getWriter();

      unm = req.getParameter("unm");
      sid = req.getParameter("sid");
      uty = req.getParameter("uty");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");
      p1  = req.getParameter("p1"); // date
      p2  = req.getParameter("p2"); // remark
      p3  = req.getParameter("p3"); // storeFrom
      p4  = req.getParameter("p4"); // storeTo
      
      if(p1 == null) p1 = "";
      if(p2 == null) p2 = "";
      
      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, p3, p4, bytesOut);
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

      System.out.println("3071a: " + e);
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "StoreStoreMoveExecute", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3071, bytesOut[0], 0, "ERR:" + p3 + " " + p4);
      if(out != null) out.flush(); 
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1,
                    String p2, String p3, String p4, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null, stmt2 = null, stmt3 = null;
    ResultSet rs   = null, rs2   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 3071, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "StoreStoreMove", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3071, bytesOut[0], 0, "ACC:" + p3 + " " + p4);
      if(con  != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "StoreStoreMove", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3071, bytesOut[0], 0, "SID:" + p3 + " " + p4);
      if(con  != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    String date;
    if(p1.length() == 0)
      date = generalUtils.todaySQLFormat(localDefnsDir, defnsDir);
    else date = generalUtils.convertDateToSQLFormat(p1);
    
    set(con, stmt, stmt2, stmt3, rs, rs2, out, req, date, p2, p3, p4, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 3071, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p3 + " " + p4);
    if(con  != null) con.close();
    if(out != null) out.flush(); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, PrintWriter out, HttpServletRequest req,
                   String date, String remark, String storeFrom,
                   String storeTo, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir,
                   String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Move Store to Store</title>");

    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\""
                         + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    int[] hmenuCount = new int[1];
 
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "3071", "", "StoreStoreMove", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
  
    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Move Store to Store", "3071", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);


    generate(con, stmt, stmt2, stmt3, rs, rs2, date, remark, storeFrom, storeTo, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);
    
    
    scoutln(out, bytesOut, "<tr><td colspan=10><hr></td></tr>");

    
    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void generate(Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, String date, String remark,
                        String storeFrom, String storeTo, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                        String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    String today = generalUtils.today(localDefnsDir, defnsDir);
    
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT ItemCode FROM stockx WHERE Store = '" + generalUtils.sanitiseForSQL(storeFrom) + "'");

    String itemCode, list, qty;
    int x, len;
    
    while(rs.next())
    {    
      itemCode = rs.getString(1);
      
      // Return stocklevel for each store: "SamLeong\001150000\001\0"
      list = stockLevelsGenerate.fetch(con, stmt2, stmt3, rs2, itemCode, today, storeFrom, unm, sid, uty, men, den, dnm, bnm);
      
      x=0;
      len = list.length();
      while(x < len && list.charAt(x) != '\001')
        ++x;
      if(x < len)
        ++x;
      qty = "";
      while(x < len && list.charAt(x) != '\001')
        qty += list.charAt(x++);
      
      if(generalUtils.doubleFromStr(qty) != 0)
      System.out.println(itemCode = " " + qty);


    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
