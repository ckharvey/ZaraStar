// =======================================================================================================================================================================================================
// System: ZaraStar Analytic: Sales by Product Category
// Module: SalesByProductCategory.java
// Author: C.K.Harvey
// Copyright (c) 1999-2014 Christopher Harvey. All Rights Reserved.
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
import java.sql.ResultSet;
import java.sql.Statement;
import java.io.*;

public class SalesByProductCategory extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  AccountsUtils accountsUtils = new AccountsUtils();
  
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2 = "";

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
      p1  = req.getParameter("p1"); // dateFrom
      p2  = req.getParameter("p2"); // dateTo

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, bytesOut);
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
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "SalesByProductCategory", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 13056, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1, String p2, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String localDefnsDir  = directoryUtils.getLocalOverrideDir(dnm);
    String defnsDir          = directoryUtils.getSupportDirs('D');
    String imagesDir         = directoryUtils.getSupportDirs('I');
    
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null, stmt2 = null, stmt3 = null;
    ResultSet rs   = null, rs2 = null, rs3 = null;
    
    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "_13056", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 13056, bytesOut[0], 0, "SID:" + p1);
      if(con  != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    process(con, stmt, stmt2, stmt3, rs, rs2, rs3, out, req, p1, p2, unm, sid, uty, men, den, dnm, bnm, imagesDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 13056, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con  != null) con.close();
    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void process(Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, ResultSet rs3, PrintWriter out, HttpServletRequest req, String p1, String p2, String unm, String sid, String uty, String men, String den,
                       String dnm, String bnm, String imagesDir, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Sales by Product Category</title>");

    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\"></head>");
  
    int[] hmenuCount = new int[1];
    
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "13056", "", "_13056", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
                                
    scoutln(out, bytesOut, "<form>");
  
    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Sales by Product Category", "13056", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    scout(out, bytesOut, "<p>Date From: " + p1);
    scout(out, bytesOut, "<p>Date To: " + p2);
    scout(out, bytesOut, "<p>Consolidated into " + accountsUtils.getBaseCurrency(con, stmt, rs, dnm, localDefnsDir, defnsDir));

    StringBuilder sb = new StringBuilder(1024);

    
    processMfr(sb, con, stmt, stmt2, rs, rs2, out, p1, p2, localDefnsDir, defnsDir, bytesOut);

    processSegment(con, stmt, stmt2, stmt3, rs, rs2, rs3, out, p1, p2, localDefnsDir, defnsDir, bytesOut);

    
    
    processCustomers(con, stmt, rs, sb, out, bytesOut);
    
    
    
    scoutln(out, bytesOut, "</form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void processSegment(Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, ResultSet rs3, PrintWriter out, String p1, String p2, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<br><table id=\"page\" width=100%>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr id=\"pageColumn\">");
    scoutln(out, bytesOut, "<td></td>");
    scoutln(out, bytesOut, "<td align=center colspan=2><p> Hammer </td>");
    scoutln(out, bytesOut, "<td align=center colspan=2><p> Drill </td>");
    scoutln(out, bytesOut, "<td align=center colspan=2><p> Saw &amp; Cut </td>");
    scoutln(out, bytesOut, "<td align=center colspan=2><p> Grinder &amp; Sander </td>");
    scoutln(out, bytesOut, "<td align=center colspan=2><p> Fastening </td>");
    scoutln(out, bytesOut, "<td align=center colspan=2><p> Misc </td></tr>");

    scoutln(out, bytesOut, "<tr id=\"pageColumn\">");
    scoutln(out, bytesOut, "<td><p> Segment </td>");
    scoutln(out, bytesOut, "<td align=center><p>Value</td><td align=center><p>%</td>");
    scoutln(out, bytesOut, "<td align=center><p>Value</td><td align=center><p>%</td>");
    scoutln(out, bytesOut, "<td align=center><p>Value</td><td align=center><p>%</td>");
    scoutln(out, bytesOut, "<td align=center><p>Value</td><td align=center><p>%</td>");
    scoutln(out, bytesOut, "<td align=center><p>Value</td><td align=center><p>%</td>");
    scoutln(out, bytesOut, "<td align=center><p>Value</td><td align=center><p>%</td></tr>");

    String dateFrom;
    if(p1 == null || p1.length() == 0)
      dateFrom = generalUtils.todaySQLFormat(localDefnsDir, defnsDir);
    else dateFrom = generalUtils.convertDateToSQLFormat(p1);
    
    String dateTo;
    if(p2 == null || p2.length() == 0)
      dateTo = generalUtils.todaySQLFormat(localDefnsDir, defnsDir);
    else dateTo = generalUtils.convertDateToSQLFormat(p2);
    
    String[] segmentCodes = new String[1000];
    int[] numSegmentCodes   = new int[1]; numSegmentCodes[0]  = 0;
    
    double[][] totals = new double[8][3000]; 
    double[] totalValues = new double[3000];
    double percentage;
    
    int x, y, z;
    for(x=0;x<8;++x)
      for(y=0;y<3000;++y)
      totals[x][y] = 0.0;
    
    for(x=0;x<3000;++x)
      totalValues[x] = 0;
        
    calculateInvoicesSegment(con, stmt, stmt2, rs, rs2, dateFrom, dateTo, segmentCodes, numSegmentCodes, totals);

    for(y=0;y<8;++y)
    {
      for(x=0;x<numSegmentCodes[0];++x)
      {
        totalValues[y] += totals[y][x];
      }
    }
    
    for(x=0;x<numSegmentCodes[0];++x)
    {
      scoutln(out, bytesOut, "<tr><td><p>" + segmentCodes[x] + "</td>");
      
      for(y=0;y<6;++y)
      {
        scoutln(out, bytesOut, "<td align=right><p>" +generalUtils.formatNumeric(totals[y][x], '2') + "</td>");
        if(totalValues[y] == 0.0)
          percentage = 0.0;
        else percentage = ((totals[y][x] / totalValues[y]) * 100);
        scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.doubleDPs('2', percentage) + "%</td>");       
      }
      
      scoutln(out, bytesOut, "</tr>");
    }

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td><p><b>TOTAL</td>");

    for(y=0;y<6;++y)
    {  
      scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(totalValues[y], '2') + "</td>");
      scoutln(out, bytesOut, "<td align=right><p>100%</td>");       
    }

    scoutln(out, bytesOut, "</table>");
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void processMfr(StringBuilder sb, Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, String p1, String p2, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<br><table id=\"page\" width=100%>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr id=\"pageColumn\">");
    scoutln(out, bytesOut, "<td></td>");
    scoutln(out, bytesOut, "<td align=center colspan=2><p> Hammer </td>");
    scoutln(out, bytesOut, "<td align=center colspan=2><p> Drill </td>");
    scoutln(out, bytesOut, "<td align=center colspan=2><p> Saw &amp; Cut </td>");
    scoutln(out, bytesOut, "<td align=center colspan=2><p> Grinder &amp; Sander </td>");
    scoutln(out, bytesOut, "<td align=center colspan=2><p> Fastening </td>");
    scoutln(out, bytesOut, "<td align=center colspan=2><p> Misc </td></tr>");

    scoutln(out, bytesOut, "<tr id=\"pageColumn\">");
    scoutln(out, bytesOut, "<td><p> Manufacturer </td>");
    scoutln(out, bytesOut, "<td align=center><p>Value</td><td align=center><p>%</td>");
    scoutln(out, bytesOut, "<td align=center><p>Value</td><td align=center><p>%</td>");
    scoutln(out, bytesOut, "<td align=center><p>Value</td><td align=center><p>%</td>");
    scoutln(out, bytesOut, "<td align=center><p>Value</td><td align=center><p>%</td>");
    scoutln(out, bytesOut, "<td align=center><p>Value</td><td align=center><p>%</td>");
    scoutln(out, bytesOut, "<td align=center><p>Value</td><td align=center><p>%</td></tr>");

    String dateFrom;
    if(p1 == null || p1.length() == 0)
      dateFrom = generalUtils.todaySQLFormat(localDefnsDir, defnsDir);
    else dateFrom = generalUtils.convertDateToSQLFormat(p1);
    
    String dateTo;
    if(p2 == null || p2.length() == 0)
      dateTo = generalUtils.todaySQLFormat(localDefnsDir, defnsDir);
    else dateTo = generalUtils.convertDateToSQLFormat(p2);
    
    String[] mfrCodes = new String[1000];
    int[] numMfrCodes   = new int[1]; numMfrCodes[0]  = 0;
    
    double[][] totals = new double[8][3000]; 
    double[] totalValues = new double[3000];
    double percentage;
    
    int x, y, z;
    for(x=0;x<8;++x)
      for(y=0;y<3000;++y)
      totals[x][y] = 0.0;
    
    for(x=0;x<3000;++x)
      totalValues[x] = 0;
        
    calculateInvoicesMfr(sb, con, stmt, stmt2, rs, rs2, dateFrom, dateTo, "cf1", 0, mfrCodes, numMfrCodes, totals);
    calculateInvoicesMfr(sb, con, stmt, stmt2, rs, rs2, dateFrom, dateTo, "cf3", 1, mfrCodes, numMfrCodes, totals);
    calculateInvoicesMfr(sb, con, stmt, stmt2, rs, rs2, dateFrom, dateTo, "cf4", 2, mfrCodes, numMfrCodes, totals);
    calculateInvoicesMfr(sb, con, stmt, stmt2, rs, rs2, dateFrom, dateTo, "cf5", 3, mfrCodes, numMfrCodes, totals);
    calculateInvoicesMfr(sb, con, stmt, stmt2, rs, rs2, dateFrom, dateTo, "cf8", 4, mfrCodes, numMfrCodes, totals);
    calculateInvoicesMfr(sb, con, stmt, stmt2, rs, rs2, dateFrom, dateTo, "cf2", 5, mfrCodes, numMfrCodes, totals);

    for(y=0;y<8;++y)
    {
      for(x=0;x<numMfrCodes[0];++x)
      {
        totalValues[y] += totals[y][x];
      }
    }
    
    for(x=0;x<numMfrCodes[0];++x)
    {
      scoutln(out, bytesOut, "<tr><td><p>" + mfrCodes[x] + "</td>");
      
      for(y=0;y<6;++y)
      {
        scoutln(out, bytesOut, "<td align=right><p>" +generalUtils.formatNumeric(totals[y][x], '2') + "</td>");
        if(totalValues[y] == 0.0)
          percentage = 0.0;
        else percentage = ((totals[y][x] / totalValues[y]) * 100);
        scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.doubleDPs('2', percentage) + "%</td>");       
      }
      
      scoutln(out, bytesOut, "</tr>");
    }

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td><p><b>TOTAL</td>");

    for(y=0;y<6;++y)
    {  
      scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(totalValues[y], '2') + "</td>");
      scoutln(out, bytesOut, "<td align=right><p>100%</td>");       
    }

    scoutln(out, bytesOut, "</table>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void calculateInvoicesMfr(StringBuilder sb, Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String dateFrom, String dateTo, String prodCat, int prodCatCount, String[] mfrCodes, int[] numMfrCodes,
                                    double[][] totals) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.Manufacturer, t1.Date, t2.Amount, t2.ItemCode, t1.CompanyCode FROM invoice AS t1 INNER JOIN invoicel AS t2 ON t1.InvoiceCode = t2.InvoiceCode WHERE t1.Date >= {d '" + dateFrom
                         + "'} AND t1.Date <= {d '" + dateTo + "'} AND t1.Status != 'C'");

    double thisAmount;
    String thisMfrCode;

    while(rs.next())
    {
      thisMfrCode = rs.getString(1);
      
      if(isProdCat(con, stmt2, rs2, prodCat, rs.getString(4)))
      {
        processCompanyCode(sb, rs.getString(5));
        if(thisMfrCode == null || thisMfrCode.length() == 0 || thisMfrCode.equals("<none>"))
          thisMfrCode = "-";
      
      thisAmount      = generalUtils.doubleFromStr(generalUtils.doubleDPs(rs.getString(3), '2'));
   
      addToDataSoFarMfr(thisMfrCode, thisAmount, prodCatCount, mfrCodes, numMfrCodes, totals);
      }
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void addToDataSoFarMfr(String thisMfrCode, double thisAmount, int prodCatCount, String[] mfrCodes, int[] numMfrCodes, double[][] totals) throws Exception
  {
    int x=0;
    boolean found = false;
    while(x < numMfrCodes[0] && ! found)
    {
      if(mfrCodes[x].equals(thisMfrCode))
        found = true;
      else ++x;
    }
   
    if(! found)
    {
      x = numMfrCodes[0];
      ++numMfrCodes[0];
    }
    
    mfrCodes[x] = thisMfrCode;
        
    totals[prodCatCount][x] +=  thisAmount;
  }  
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void calculateInvoicesSegment(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String dateFrom, String dateTo, String[] industryCodes, int[] numIndustryCodes, double[][] totals) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.Manufacturer, t1.Date, t2.Amount, t2.ItemCode, t1.CompanyCode FROM invoice AS t1 INNER JOIN invoicel AS t2 ON t1.InvoiceCode = t2.InvoiceCode WHERE t1.Date >= {d '" + dateFrom
                         + "'} AND t1.Date <= {d '" + dateTo + "'} AND t1.Status != 'C'");

    double thisAmount;
    String industryType;
    int prodCatCount;

    while(rs.next())
    {
      if((prodCatCount = getProdCatCount(con, stmt2, rs2, rs.getString(4))) != -1)
      {
        industryType = getIndustryType(con, stmt2, rs2, rs.getString(5));
        thisAmount      = generalUtils.doubleFromStr(generalUtils.doubleDPs(rs.getString(3), '2'));
        
        addToDataSoFarSegment(prodCatCount, industryType, thisAmount, industryCodes, numIndustryCodes, totals);
      }
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void addToDataSoFarSegment(int prodCatCount, String thisSegmentCode, double thisAmount, String[] segmentCodes, int[] numSegmentCodes, double[][] totals) throws Exception
  {
    int x=0;
    boolean found = false;
    while(x < numSegmentCodes[0] && ! found)
    {
      if(segmentCodes[x].equals(thisSegmentCode))
        found = true;
      else ++x;
    }
   
    if(! found)
    {
      x = numSegmentCodes[0];
      ++numSegmentCodes[0];
    }
    
    segmentCodes[x] = thisSegmentCode;
        
    totals[prodCatCount][x] +=  thisAmount;
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void scout(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.print(str);
    bytesOut[0] += str.length();    
  }
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

  // ----------------------------------------------------------------
  private void processCompanyCode(StringBuilder sb, String code)
  {
    if(sb.indexOf(code + " ") == -1)
      sb.append(code + " ");
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean isProdCat(Connection con, Statement stmt, ResultSet rs, String prodCat, String itemCode) throws Exception
  {
    boolean res = false;
    
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT AltItemCode3 FROM stock WHERE ItemCode = '" + itemCode + "'");

    String alt;

    if(rs.next())
    {
      alt = rs.getString(1);
      
      if(alt.equals(prodCat))
        res = true;
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return res;
  }          

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private String getIndustryType(Connection con, Statement stmt, ResultSet rs, String companyCode) throws Exception
  {
    String industryType = "-";
    
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT IndustryType FROM company WHERE CompanyCode = '" + companyCode + "'");

    if(rs.next())
    {
      industryType = rs.getString(1);      
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return industryType;
  }          

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void processCustomers(Connection con, Statement stmt, ResultSet rs, StringBuilder sb, PrintWriter out, int[] bytesOut) throws Exception
  {
    String thisOne;
    int x = 0, len = sb.length();

    scoutln(out, bytesOut, "<br><table id=\"page\" width=100%>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    
    while(x < len)
    {
      thisOne = "";
      while(x < len && sb.charAt(x) != ' ')
        thisOne += sb.charAt(x++);
      ++x;
      
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT Name, IndustryType FROM company WHERE CompanyCode = '" + thisOne + "'");

      String type, name;

      if(rs.next())
      {
        type = rs.getString(1);
        name = rs.getString(2);
      
        scoutln(out, bytesOut, "<tr><td><p>" + thisOne + "</td><td><p>" + name + "</td><td><p>" + type + "</td></tr>");
      }
    
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
  }    

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private int getProdCatCount(Connection con, Statement stmt, ResultSet rs, String itemCode) throws Exception
  {
    int prodCatCount = -1;
    
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT AltItemCode3 FROM stock WHERE ItemCode = '" + itemCode + "'");

    String alt;

    if(rs.next())
    {
      alt = rs.getString(1);
      
      if(alt.startsWith("cf"))
      {
        prodCatCount = generalUtils.strToInt(alt.substring(2));
        
        if(prodCatCount == 1) prodCatCount = 0; else
        if(prodCatCount == 3) prodCatCount = 1; else
        if(prodCatCount == 4) prodCatCount = 2; else
        if(prodCatCount == 5) prodCatCount = 3; else
        if(prodCatCount == 8) prodCatCount = 4; else
        if(prodCatCount == 2) prodCatCount = 5;        
      }
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return prodCatCount;
  }          
  
  
  
}
