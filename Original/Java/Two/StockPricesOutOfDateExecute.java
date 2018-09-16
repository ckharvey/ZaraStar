// =======================================================================================================================================================================================================
// System: ZaraStar: List Stock Prices Out-of-Date
// Module: StockPricesOutOfDateExecute.java
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
import java.sql.*;

public class StockPricesOutOfDateExecute extends HttpServlet
{
  GeneralUtils  generalUtils = new GeneralUtils();
  MessagePage  messagePage = new MessagePage();
  MiscDefinitions  miscDefinitions = new MiscDefinitions();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils  pageFrameUtils = new PageFrameUtils();
  DirectoryUtils  directoryUtils = new DirectoryUtils();
  Inventory  inventory = new Inventory();
  
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="";

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
      p2  = req.getParameter("p2"); // mfr
      
      if(p1 == null) p1 = "";
      if(p2 == null) p2 = "";
      
      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, bytesOut);
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

      System.out.println("3061a: " + e);
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "StockPricesOutOfDateExecute", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3061, bytesOut[0], 0, "ERR:" + p1 + " " + p2);
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm,
                    String bnm, String p1, String p2, int[] bytesOut) throws Exception
  {
    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    long startTime = new java.util.Date().getTime();

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 3061, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "StockPricesOutOfDate", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3061, bytesOut[0], 0, "ACC:" + p1 + " " + p2);
      if(con  != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "StockPricesOutOfDate", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3061, bytesOut[0], 0, "SID:" + p1 + " " + p2);
      if(con  != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    String date;
    if(p1.length() == 0)
      date = generalUtils.todaySQLFormat(localDefnsDir, defnsDir);
    else date = generalUtils.convertDateToSQLFormat(p1);
    
    set(con, stmt, rs, out, req, date, p2, unm, sid, uty, men, den, dnm, bnm, imagesDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 3061, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1 + " " + p2);
    if(con  != null) con.close();
    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String date, String mfr, String unm, String sid, String uty, String men,
                   String den, String dnm, String bnm, String imagesDir, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>List Stock Prices Out-of-Date</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3001, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewItem(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/ProductStockRecord?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men
                           + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p1=\"+p1;}");
    }

    scoutln(out, bytesOut, "function sanitise(code){");
    scoutln(out, bytesOut, "var code2='';var x;var len=code.length;");
    scoutln(out, bytesOut, "for(x=0;x<len;++x)");
    scoutln(out, bytesOut, "if(code.charAt(x)=='#')code2+='%23';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='\"')code2+='%22';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='&')code2+='%26';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='%')code2+='%25';");
    scoutln(out, bytesOut, "else if(code.charAt(x)==' ')code2+='%20';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='?')code+='%3F';");
    scoutln(out, bytesOut, "else code2+=code.charAt(x);");
    scoutln(out, bytesOut, "return code2};");
    
    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir)
                         + "general.css\">");

    int[] hmenuCount = new int[1];
 
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "3061", "", "StockPricesOutOfDate", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount,
                          bytesOut);
  
    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "List Stock Prices Out-of-Date", "3061", unm, sid, uty, men, den, dnm, bnm,
                    hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<form><table id=\"page\" width=100% border=0>");
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr id=\"pageColumn\"><td><p>Item Code</td>");
    scoutln(out, bytesOut, "<td><p>Manufacturer Code</td>");
    scoutln(out, bytesOut, "<td><p>Date Changed</td>");
    scoutln(out, bytesOut, "<td><p>Description</td>");
    scoutln(out, bytesOut, "<td><p>Description2</td>");
    scoutln(out, bytesOut, "<td><p>List Price</td>");
    scoutln(out, bytesOut, "<td><p>Band 1</td>");
    scoutln(out, bytesOut, "<td><p>Band 2</td>");
    scoutln(out, bytesOut, "<td><p>Band 3</td>");
    scoutln(out, bytesOut, "<td><p>Band 4</td>");
    scoutln(out, bytesOut, "<td><p>Purchase Price</td></tr>");

    search(out, date, mfr, dnm, localDefnsDir, defnsDir, bytesOut);

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=10><hr></td></tr>");

    scoutln(out, bytesOut, "</table></form>");     scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void search(PrintWriter out, String date, String mfr, String dnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    String userName = directoryUtils.getMySQLUserName();
    String passWord = directoryUtils.getMySQLPassWord();
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + userName + "&password=" + passWord);

    Statement stmt = con.createStatement();
    
    ResultSet rs = stmt.executeQuery("SELECT ItemCode, DateChanged, Description, Description2, RRP, SellPrice1, SellPrice2, SellPrice3, SellPrice4, "
                                   + "PurchasePrice, Currency, SalesCurrency, ManufacturerCode FROM stock WHERE Manufacturer = '"
                                   + generalUtils.sanitiseForSQL(mfr) + "' AND DateChanged < {d '" + date + "'} AND Status != 'C' " + "ORDER BY ItemCode");

    String itemCode, cssFormat="";
    
    while(rs.next())
    {    
      itemCode = rs.getString(1);

      if(cssFormat.equals("line1"))
        cssFormat = "line2";
      else cssFormat = "line1";

      scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\"><td><p><a href=\"javascript:viewItem('" + itemCode + "')\">" + itemCode + "</a></td>");
      scoutln(out, bytesOut, "<td><p>" + rs.getString(13) + "</td>");
      scoutln(out, bytesOut, "<td><p>" + generalUtils.convertFromYYYYMMDD(rs.getString(2)) + "</td>");
      scoutln(out, bytesOut, "<td><p>" + rs.getString(3) + "</td>");
      scoutln(out, bytesOut, "<td><p>" + rs.getString(4) + "</td>");
      scoutln(out, bytesOut, "<td><p>" + rs.getString(12) + " " + generalUtils.doubleDPs(rs.getString(5), '2') + "</td>");
      scoutln(out, bytesOut, "<td><p>" + generalUtils.doubleDPs(rs.getString(6), '2') + "</td>");
      scoutln(out, bytesOut, "<td><p>" + generalUtils.doubleDPs(rs.getString(7), '2') + "</td>");
      scoutln(out, bytesOut, "<td><p>" + generalUtils.doubleDPs(rs.getString(8), '2') + "</td>");
      scoutln(out, bytesOut, "<td><p>" + generalUtils.doubleDPs(rs.getString(9), '2') + "</td>");
      scoutln(out, bytesOut, "<td><p>" + rs.getString(11) + " " + generalUtils.doubleDPs(rs.getString(10), '2') + "</td></tr>");
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    if(con  != null) con.close();  
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
