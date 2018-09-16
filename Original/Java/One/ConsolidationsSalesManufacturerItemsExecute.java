// =======================================================================================================================================================================================================
// System: ZaraStar Info: Consolidations
// Module: ConsolidationInfo.java
// Author: C.K.Harvey
// Copyright (c) 2002-09 Christopher Harvey. All Rights Reserved.
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
import java.sql.*;
import java.io.*;

public class ConsolidationsSalesManufacturerItemsExecute extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  AccountsUtils accountsUtils = new AccountsUtils();
  DashboardUtils dashboardUtils = new DashboardUtils();
  Inventory inventory = new Inventory();
  StockLevelsGenerate StockLevelsGenerate = new StockLevelsGenerate();
  
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
      req.setCharacterEncoding("UTF-8");
      directoryUtils.setContentHeaders2(res);

      unm = req.getParameter("unm");
      sid = req.getParameter("sid");
      uty = req.getParameter("uty");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");
      p1  = req.getParameter("p1"); // mfr
      p2  = req.getParameter("p2");
      
        out = res.getWriter();

      doIt(out, req, res, unm, sid, uty, men, den, dnm, bnm, p1, p2, bytesOut);
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
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "ConsolidationsSalesManufacturerItemsExecute", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6908, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, HttpServletResponse res, String unm, String sid, String uty, String men, String den, String dnm,
                    String bnm, String p1, String p2, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String localDefnsDir  = directoryUtils.getLocalOverrideDir(dnm);
    String defnsDir          = directoryUtils.getSupportDirs('D');
    String imagesDir         = directoryUtils.getSupportDirs('I');

    Statement stmt = null, stmt2 = null, stmt3 = null;
    ResultSet rs   = null, rs2 = null, rs3 = null;
     
    String uName = directoryUtils.getMySQLUserName();
    String pWord = directoryUtils.getMySQLPassWord();
    Class.forName("com.mysql.jdbc.Driver").newInstance();
 
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/xxx_ofsa?user=" + uName + "&password=" + pWord + "&autoReconnect=true");

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "ConsolidationsSalesManufacturerItems", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6908, bytesOut[0], 0, "SID:" + p1);
      if(con  != null) con.close();
      if(out != null) out.flush(); 
      return;
    }
    
    processScreen(con, stmt, stmt2, stmt3, rs, rs2, rs3, out, req, p1, p2, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 6908, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    
    if(con  != null) con.close();
    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void processScreen(Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, ResultSet rs3, PrintWriter out, HttpServletRequest req, String p1, 
                             String p2, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Sales for Manufacturer Items</title>");

    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");
   
    outputPageFrame(con, stmt, rs, out, req, "", "Sales for Manufacturer Items", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, bytesOut);

    scoutln(out, bytesOut, "<form>");
  
    scout(out, bytesOut, "<p>For: " + p1);
    
    scout(out, bytesOut, "<p>For: " + p2);
    
    scout(out, bytesOut, "<p>Consolidated into " + accountsUtils.getBaseCurrency(con, stmt, rs, dnm, localDefnsDir, defnsDir));
    
    scoutln(out, bytesOut, "<br><table id=\"page\" width=100%>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    outputTitleLine(out, bytesOut);

    String[] itemCodes  = new String[8000]; // plenty
    double[] qtys2012   = new double[8000]; // plenty
    double[] qtys2013   = new double[8000]; // plenty
    double[] unitPrices = new double[8000]; // plenty
    int[]    numUPs     = new int[8000]; // plenty
    
    int count = forAMfr(con, stmt, rs, p1, p2, itemCodes, qtys2012, qtys2013, unitPrices, numUPs);    
    
    insertionSort(qtys2013, count, unitPrices, qtys2012, numUPs, itemCodes);    
    
    output(out, con, stmt, stmt2, rs, count, unm, sid, uty, men, dnm, bnm, itemCodes, unitPrices, qtys2013, qtys2012, numUPs, bytesOut);

    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private int forAMfr(Connection con, Statement stmt, ResultSet rs, String mfr, String salesPerson, String[] itemCodes, double[] qtys2012, double[] qtys2013, double[] unitPrices, int[] numUPs) throws Exception
  {
    int count = 0;
    
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT ItemCode FROM stock WHERE Manufacturer = '" + generalUtils.sanitiseForSQL(mfr) + "'");

    String itemCode;
    
    while(rs.next())
    {
      itemCode = rs.getString(1);

      itemCodes[count] = itemCode;

      if(forAnItem(con, stmt, rs, salesPerson, itemCode, count, qtys2012, qtys2013, unitPrices, numUPs))     
        ++count;
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return count;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean forAnItem(Connection con, Statement stmt, ResultSet rs, String salesPerson, String itemCode, int count, double[] qtys2012, double[] qtys2013, double[] unitPrices, int[] numUPs) throws Exception
  {
    boolean oneFound = false;
    
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.UnitPrice, t1.Date, t2.Quantity, t1.Rate FROM invoice AS t1 INNER JOIN invoicel AS t2 ON t1.InvoiceCode = t2.InvoiceCode WHERE t2.ItemCode = '"
                         + generalUtils.sanitiseForSQL(itemCode) + "' AND t1.SalesPerson = '" + generalUtils.sanitiseForSQL(salesPerson) + "' AND t1.Date >= {d '2012-01-01'} AND t1.Date <= {d '2013-12-31'} AND t1.Status != 'C'");

    String date, unitPrice, qty, rate;
    int dateE;
    
    while(rs.next())
    {
      unitPrice = rs.getString(1);
      date      = rs.getString(2);
      qty       = rs.getString(3);
      rate      = rs.getString(4);
      
      dateE = generalUtils.encodeFromYYYYMMDD(date);
      
      if(dateE >= generalUtils.encodeFromYYYYMMDD("2013-01-01"))
      {
        unitPrices[count] += (generalUtils.doubleFromStr(unitPrice) * generalUtils.doubleFromStr(rate));
        ++numUPs[count];
        qtys2013[count] += generalUtils.doubleFromStr(qty);
      }
      else
      {
        qtys2012[count] += generalUtils.doubleFromStr(qty);
      }
      
      oneFound = true;
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return oneFound;
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void output(PrintWriter out, Connection con, Statement stmt, Statement stmt2, ResultSet rs, int count, String unm, String ses, String uty, String men, String dnm, String bnm, String[] itemCodes, double[] unitPrices,
          double[] qtys2013, double[] qtys2012, int[] numUPs, int[] bytesOut) throws Exception
  {
    String itemCode, desc;
    String[] mfr = new String[1];
    String[] mfrCode = new String[1];
    double avg2013;
    String stockOnHand;
    int y, len;
    
    for(int x=0;x<count;++x)
    {
      itemCode = itemCodes[x];
    
      inventory.getMfrAndMfrCodeGivenItemCode(con, stmt, rs, itemCode, mfr, mfrCode);
      
      desc = inventory.getDescriptionGivenCode(con, stmt, rs, itemCode);
        
      stockOnHand = StockLevelsGenerate.fetch(con, stmt, stmt2, rs, itemCode, "", "", unm, ses, uty, men, dnm, dnm, bnm);

      y = 0;
      len = stockOnHand.length();
      
      while(y < len && stockOnHand.charAt(y)!= '\001')
        ++y;
      ++y;
      
     stockOnHand = stockOnHand.substring(y); 
         
      if(numUPs[x] != 0)
        avg2013 = unitPrices[x] / numUPs[x];
      else avg2013 = unitPrices[x];
              
      scoutln(out, bytesOut, "<tr>");
    
      scoutln(out, bytesOut, "<td><p>" + mfrCode[0] + "</td>");

      scoutln(out, bytesOut, "<td><p>" + itemCode + "</td>");
      scoutln(out, bytesOut, "<td><p>" + desc + "</td>");

      scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(avg2013, '2') + "</td>");
      scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(qtys2013[x], '0') + "</td>");
      scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(qtys2012[x], '0') + "</td>");
      scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(stockOnHand, '0') + "</td>");
    
      scoutln(out, bytesOut, "</tr>");
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void outputTitleLine(PrintWriter out, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<tr id=\"pageColumn\">");
    scoutln(out, bytesOut, "<td><p>Mfr Code</td><td><p>Item Code</td><td><p>Description</td><td align=right><p>2013 Average Sell Price</td><td align=right><p>2013 Quantity Sold</td><td align=right><p>2012 Quantity Sold</td><td align=right><p>Stock OnHand</td></tr>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void outputPageFrame(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String bodyStr, String title, String unm, String sid, String uty, String men,
                               String den, String dnm, String bnm, String otherSetup, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    int[] hmenuCount = new int[1];  hmenuCount[0] = 0;

    String subMenuText = "";

    scoutln(out, bytesOut, authenticationUtils.buildScreen(con, stmt, rs, req, true, unm, sid, uty, men, den, dnm, bnm, "ConsolidationsSalesManufacturerItems", subMenuText, hmenuCount[0], bodyStr, otherSetup, localDefnsDir, defnsDir));
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
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
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void insertionSort(double[] qtys2013, int numElements, double[] unitPrices, double[] qtys2012, int[] numUPs, String[] itemCodes) throws Exception
  {
    int j;
    for(int i=1;i<numElements;i++)
    {
      for(j=0;j<i;j++)
      {
        if(qtys2013[i-j-1] < qtys2013[i-j])
          swap(qtys2013, i-j-1, i-j, unitPrices, qtys2012, numUPs, itemCodes);
        else j=i;
      }
    }
  }

  private void swap(double[] qtys2013, int x, int y, double[] unitPrices, double[] qtys2012, int[] numUPs, String[] itemCodes) throws Exception
  {
    double d = qtys2013[x];
    qtys2013[x] = qtys2013[y];
    qtys2013[y] = d;
    
    d = qtys2012[x];
    qtys2012[x] = qtys2012[y];
    qtys2012[y] = d;
    
    int i = numUPs[x];
    numUPs[x] = numUPs[y];
    numUPs[y] = i;
    
    d = unitPrices[x];
    unitPrices[x] = unitPrices[y];
    unitPrices[y] = d;

    String s = itemCodes[x];
    itemCodes[x] = itemCodes[y];
    itemCodes[y] = s;
  }

}
