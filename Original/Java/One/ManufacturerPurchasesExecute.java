// =======================================================================================================================================================================================================
// System: ZaraStar Analytic: Manufacturer Purchases from PO Lines
// Module: ManufacturerPurchasesExecute.java
// Author: C.K.Harvey
// Copyright (c) 1999-2007 Christopher Harvey. All Rights Reserved.
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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.io.*;

public class ManufacturerPurchasesExecute extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  MiscDefinitions miscDefinitions = new MiscDefinitions();
  Supplier supplier = new Supplier();
  
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
      directoryUtils.setContentHeaders(res);
      out = res.getWriter();

      unm = req.getParameter("unm");
      sid = req.getParameter("sid");
      uty = req.getParameter("uty");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");
      p1  = req.getParameter("p1"); // suppCode
      p2  = req.getParameter("p2"); // mfr
      p3  = req.getParameter("p3"); // effectiveDate
      p4  = req.getParameter("p4"); // period

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, p3, p4, bytesOut);
    }    
    catch(Exception e)
    {
      StringBuffer url = req.getRequestURL();
      String urlBit="";
      int x=0;
      if(url.charAt(x) == 'h' || url.charAt(x) == 'H')
        x += 7;
      while(url.charAt(x) != '\000' && url.charAt(x) != ',' && url.charAt(x) != '/')
        urlBit += url.charAt(x++);

      System.out.println(e);
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "ManufacturerPurchasesExecute", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1035, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm,
                    String bnm, String p1, String p2, String p3, String p4, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String localDefnsDir  = directoryUtils.getLocalOverrideDir(dnm);
    String defnsDir          = directoryUtils.getSupportDirs('D');
    String imagesDir         = directoryUtils.getSupportDirs('I');
    
    String userName = directoryUtils.getMySQLUserName();
    String passWord = directoryUtils.getMySQLPassWord();
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + userName + "&password=" + passWord);
    Statement stmt=null;
    ResultSet rs=null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 1035, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "ManufacturerPurchases", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1035, bytesOut[0], 0, "ACC:" + p1);
    if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "ManufacturerPurchases", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1035, bytesOut[0], 0, "SID:" + p1);
    if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    process(con, stmt, rs, out, req, p1, p2, p3, p4, unm, sid, uty, men, den, dnm, bnm, imagesDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 1035, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void process(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String p1, String p2, String p3, String p4, String unm, String sid,
                       String uty, String men, String den, String dnm, String bnm, String imagesDir, String localDefnsDir, String defnsDir,
                       int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Manufacturer Purchases from PO Lines</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    scoutln(out, bytesOut, "function view(poCode){var p1=sanitise(poCode);");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/PurchaseOrderPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men
                         + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+p1;}");

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
                         + "general.css\"></head>");
  
    int[] hmenuCount = new int[1];
    
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "1035", "", "ManufacturerPurchases", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount,
                          bytesOut);
                                
    scoutln(out, bytesOut, "<form>");
  
    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Manufacturer Purchases from PO Lines", "1035", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    int dateFromEncoded = generalUtils.encode(p3, localDefnsDir, defnsDir);
    String dateTo = generalUtils.convertDateToSQLFormat(p3);
        
    if(p4.equals("0"))
      dateFromEncoded -= 30;
    else
    if(p4.equals("1"))
      dateFromEncoded -= 60;
    else
    if(p4.equals("2"))
      dateFromEncoded -= 90;
    else
    if(p4.equals("3"))
      dateFromEncoded -= 180;
    else dateFromEncoded -= 365;

    String dateFrom = generalUtils.decodeToYYYYMMDD(dateFromEncoded);
    
    scoutln(out, bytesOut, "<p>For Supplier: " + p1 + ": " + supplier.getSupplierNameGivenCode(con, stmt, rs, p1));
    scoutln(out, bytesOut, "<br><p>" + supplier.getSupplierCurrencyGivenCode(con, stmt, rs, p1));
    scoutln(out, bytesOut, "<br><p>From " + generalUtils.convertFromYYYYMMDD(dateFrom) + " to " + generalUtils.decode(generalUtils.encode(p3, localDefnsDir, defnsDir), localDefnsDir, defnsDir));
  
    scoutln(out, bytesOut, "<br><table id=\"page\" width=100%>");

    scoutln(out, bytesOut, "<tr id=\"pageColumn\">");
    scoutln(out, bytesOut, "<td><p> Code </td>");
    scoutln(out, bytesOut, "<td><p> Date </td>");
    scoutln(out, bytesOut, "<td><p> ItemCode</td>");
    scoutln(out, bytesOut, "<td><p> Manufacturer Code </td>");
    scoutln(out, bytesOut, "<td align=right><p> Quantity </td>");
    scoutln(out, bytesOut, "<td align=right><p> UnitPrice </td>");
    scoutln(out, bytesOut, "<td align=right><p> Amount </td>");
    scoutln(out, bytesOut, "<td align=right><p> Discount </td></tr>");
    
    char dpOnQuantities = miscDefinitions.dpOnQuantities(con, stmt, rs, "5");

    calculatePOs(con, stmt, rs, out, p1, p2, dateFrom, dateTo, dpOnQuantities, bytesOut);

    calculateLPs(con, stmt, rs, out, p1, p2, dateFrom, dateTo, dpOnQuantities, bytesOut);

    
    scoutln(out, bytesOut, "</table></form>");     scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void calculatePOs(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String supplierCode, String mfr, String dateFrom,
                            String dateTo, char dpOnQuantities, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t1.Date, t2.Amount2, t2.ItemCode, t2.ManufacturerCode, t2.Quantity, t2.Discount, t2.POCode, t2.UnitPrice FROM po "
                         + "AS t1 INNER JOIN pol AS t2 ON t1.POCode = t2.POCode WHERE t1.CompanyCode = '" + supplierCode + "' AND t2.Manufacturer = '"
                         + generalUtils.sanitiseForSQL(mfr) + "' AND t1.Status != 'C' AND t1.Date >= {d '" + dateFrom + "'} AND t1.Date <= {d '" + dateTo
                         + "'} ORDER BY Date");

    String date, amount2, itemCode, mfrCode, qty, discount, code, unitPrice;
    
    while(rs.next())
    {
      date      = rs.getString(1);
      amount2   = rs.getString(2);
      itemCode  = rs.getString(3);
      mfrCode   = rs.getString(4);
      qty       = rs.getString(5);
      discount  = rs.getString(6);
      code      = rs.getString(7);
      unitPrice = rs.getString(8);
   
      output(out, date, amount2, itemCode, mfrCode, qty, discount, code, unitPrice, dpOnQuantities, bytesOut);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void calculateLPs(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String supplierCode, String mfr, String dateFrom,
                            String dateTo, char dpOnQuantities, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t1.Date, t2.Amount2, t2.ItemCode, t2.ManufacturerCode, t2.Quantity, t2.Discount, t2.LPCode, t2.UnitPrice FROM lp "
                         + "AS t1 INNER JOIN lpl AS t2 ON t1.LPCode = t2.LPCode WHERE t1.CompanyCode = '" + supplierCode + "' AND t2.Manufacturer = '"
                         + generalUtils.sanitiseForSQL(mfr) + "' AND t1.Status != 'C' AND t1.Date >= {d '" + dateFrom + "'} AND t1.Date <= {d '" + dateTo
                         + "'} ORDER BY Date");

    String date, amount2, itemCode, mfrCode, qty, discount, code, unitPrice;
    
    while(rs.next())
    {
      date      = rs.getString(1);
      amount2   = rs.getString(2);
      itemCode  = rs.getString(3);
      mfrCode   = rs.getString(4);
      qty       = rs.getString(5);
      discount  = rs.getString(6);
      code      = rs.getString(7);
      unitPrice = rs.getString(8);
   
      output(out, date, amount2, itemCode, mfrCode, qty, discount, code, unitPrice, dpOnQuantities, bytesOut);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void output(PrintWriter out, String date, String amount2, String itemCode, String mfrCode, String qty, String discount, String code,
                      String unitPrice, char dpOnQuantities, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<tr><td><p>" + "<a href=\"javascript:view('" + code + "')\">" + code + "</a></td>");       
    scoutln(out, bytesOut, "<td><p>" + generalUtils.convertFromYYYYMMDD(date) + "</td>");
    scoutln(out, bytesOut, "<td><p>" + itemCode + "</td>");
    scoutln(out, bytesOut, "<td><p>" + mfrCode + "</td>");
    scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.doubleDPs(qty, dpOnQuantities) + "</td>");
    scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.doubleDPs(unitPrice, '2') + "</td>");
    scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.doubleDPs(amount2, '2') + "</td>");
    scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.doubleDPs(discount, '2') + "</td></tr>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}


