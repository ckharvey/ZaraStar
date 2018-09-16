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

public class ConsolidationsSalesInvoiceExecute extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  AccountsUtils accountsUtils = new AccountsUtils();
  DashboardUtils dashboardUtils = new DashboardUtils();
  Customer customer = new Customer();
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
      p1  = req.getParameter("p1");

      if(p1.equals("S"))
        out = res.getWriter();

      doIt(out, req, res, unm, sid, uty, men, den, dnm, bnm, p1, bytesOut);
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
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "ConsolidationsSalesInvoiceExecute", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6906, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, HttpServletResponse res, String unm, String sid, String uty, String men, String den, String dnm,
                    String bnm, String p1, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String localDefnsDir  = directoryUtils.getLocalOverrideDir(dnm);
    String defnsDir          = directoryUtils.getSupportDirs('D');
    String imagesDir         = directoryUtils.getSupportDirs('I');
    String workingDir       = directoryUtils.getUserDir('W', dnm, "/" + unm);
    
    Statement stmt = null, stmt2 = null, stmt3 = null;
    ResultSet rs   = null, rs2 = null, rs3 = null;
    
    String uName = directoryUtils.getMySQLUserName();
    String pWord = directoryUtils.getMySQLPassWord();
    Class.forName("com.mysql.jdbc.Driver").newInstance();
 
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/xxx?user=" + uName + "&password=" + pWord + "&autoReconnect=true");
    
    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 6906, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "ConsolidationsSalesInvoice", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6906, bytesOut[0], 0, "ACC:" + p1);
      if(con  != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "ConsolidationsSalesInvoice", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6906, bytesOut[0], 0, "SID:" + p1);
      if(con  != null) con.close();
      if(out != null) out.flush(); 
      return;
    }
    
    boolean isCSV = false;
    
    if(p1.equals("C"))
      isCSV = true;

    RandomAccessFile fh = null;

    if(isCSV)
    {
      String fName = "ConsolidationsSalesInvoice.csv";
      
      fh = generalUtils.create(workingDir + fName);

      processCSV(fh, con, stmt, stmt2, rs, rs2, out, dnm, localDefnsDir, defnsDir, bytesOut);
      
      generalUtils.fileClose(fh);

      download(res, workingDir, fName, bytesOut);
    }
    else processScreen(fh, con, stmt, stmt2, rs, rs2, out, req, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 6906, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    
    if(con  != null) con.close();
    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void processScreen(RandomAccessFile fh, Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, HttpServletRequest req, 
                             String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Sales by Invoice</title>");

    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");
   
    outputPageFrame(con, stmt, rs, out, req, false, "", "", "", "Sales by Invoice", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, bytesOut);

    scoutln(out, bytesOut, "<form>");
  
    scoutln(out, bytesOut, "<br><table id=\"page\" width=100%>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    outputTitleLine(fh, out, false, bytesOut);

    forEachInvoice(fh, out, false, con, stmt, stmt2, rs, rs2, "2011-01-01", "2014-06-30", dnm, localDefnsDir, defnsDir, bytesOut);
    
    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void processCSV(RandomAccessFile fh, Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, String dnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    outputTitleLine(fh, out, true, bytesOut);

    forEachInvoice(fh, out, true, con, stmt, stmt2, rs, rs2, "2011-01-01", "2014-06-30", dnm, localDefnsDir, defnsDir, bytesOut);
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private double forEachInvoice(RandomAccessFile fh, PrintWriter out, boolean isCSV, Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String dateFrom, String dateTo, String dnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.Amount2, t2.ItemCode, t2.Description, t1.Currency, t2.Quantity, t1.InvoiceCode, t1.Date, t2.UnitPrice, t1.CompanyCode, t1.CompanyName, t2.Manufacturer, t2.GSTRate, t2.SOCode, t2.Amount"
                         + " FROM invoice AS t1 INNER JOIN invoicel AS t2 ON t1.InvoiceCode = t2.InvoiceCode WHERE "
                         + "t1.Date >= {d '" + dateFrom + "'} AND t1.Date <= {d '" + dateTo + "'} AND t1.Status != 'C'");

    double amount = 0.0;
    String amt2, itemCode, desc, currency, qty, invoiceCode, invoiceDate, unitPrice, custCode, custName, mfr, gstRate, soCode, industryType, industryType2, amt, sgdAmt, sgdUnitPrice;
    String[] cosAmt = new String[1];
    String[] cosUnitPrice = new String[1];
    String[] cosQty     = new String[1];
    String[] cnCurrency = new String[1];
    String[] cnCode     = new String[1];
    String[] cnAmt      = new String[1];
    String[] cat1       = new String[1];
    String[] cat2       = new String[1];

    while(rs.next())
    {
      amt2        = rs.getString(1);
      itemCode    = rs.getString(2);
      desc        = generalUtils.deNull(rs.getString(3));
      currency    = generalUtils.deNull(rs.getString(4));
      qty         = generalUtils.deNull(rs.getString(5));
      invoiceCode = generalUtils.deNull(rs.getString(6));
      invoiceDate = generalUtils.deNull(rs.getString(7));
      unitPrice   = generalUtils.deNull(rs.getString(8));
      custCode    = generalUtils.deNull(rs.getString(9));
      custName    = generalUtils.deNull(rs.getString(10));
      mfr         = generalUtils.deNull(rs.getString(11));
      gstRate     = generalUtils.deNull(rs.getString(12));
      soCode      = generalUtils.deNull(rs.getString(13));
      sgdAmt      = generalUtils.deNull(rs.getString(14));

      industryType  = customer.getACompanyFieldGivenCode(con, stmt2, rs2, "IndustryType", custCode);
      industryType2 = customer.getACompanyFieldGivenCode(con, stmt2, rs2, "State", custCode);
    
      getCOS(con, stmt2, rs2, soCode, itemCode, cosAmt, cosUnitPrice, cosQty, invoiceDate, dnm, localDefnsDir, defnsDir);
      
      cosUnitPrice[0] = generalUtils.doubleDPs(cosUnitPrice[0], '2');
      cosAmt[0]       = generalUtils.doubleDPs(cosAmt[0], '2');

      if(generalUtils.doubleFromStr(amt2) == 0)
        sgdUnitPrice = "0";
      else sgdUnitPrice = generalUtils.doubleToStr((generalUtils.doubleFromStr(sgdAmt) / generalUtils.doubleFromStr(amt2)) * generalUtils.doubleFromStr(unitPrice));

      if(! generateCNs(con, stmt2, rs2, invoiceCode, itemCode, cnCode, cnCurrency, cnAmt))
        cnCode[0] = cnCurrency[0] = cnAmt[0] = "";
      else cnAmt[0] = generalUtils.doubleDPs(cnAmt[0], '2');

      getCategories(con, stmt2, rs2, itemCode, cat1, cat2);

      outputLine(fh, out, isCSV, itemCode, desc, amt2, currency, qty, gstRate, invoiceCode, invoiceDate, unitPrice, mfr, custCode, custName, industryType, industryType2, cosAmt[0], cosUnitPrice[0], cosQty[0], cnCode[0], cnCurrency[0], 
              cnAmt[0],   bytesOut, sgdAmt, sgdUnitPrice, cat1[0], cat2[0]);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return amount;
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void outputLine(RandomAccessFile fh, PrintWriter out, boolean isCSV, String itemCode, String desc, String amt, String currency, 
                          String qty, String gstRate, String invoiceCode, String invoiceDate, String unitPrice, String mfr, String custCode,
                          String custName, String industryType, String industryType2, String cosAmt, String cosUnitPrice, String cosQty,
                          String cnCode, String cnCurrency, String cnAmt, int[] bytesOut, String sgdAmt, String sgdUnitPrice, String cat1, String cat2) throws Exception
  {
    if(isCSV)
    {  
      writeEntry(fh, invoiceCode, true, false);
      writeEntry(fh, invoiceDate, true, false);
      writeEntry(fh, currency, true, false);
      writeEntry(fh, amt, true, false);
      writeEntry(fh, sgdAmt, true, false);
      writeEntry(fh, currency, true, false);
      writeEntry(fh, unitPrice, true, false);
      writeEntry(fh, sgdUnitPrice, true, false);
      writeEntry(fh, qty, true, false);
      writeEntry(fh, gstRate, true, false);
      writeEntry(fh, cnCode, true, false);
      writeEntry(fh, cnCurrency, true, false);
      writeEntry(fh, cnAmt, true, false);
      writeEntry(fh, mfr, true, false);
      writeEntry(fh, custCode, true, false);
      writeEntry(fh, custName, true, false);
      writeEntry(fh, industryType, true, false);
      writeEntry(fh, industryType2, true, false);
      writeEntry(fh, itemCode, true, false);
      writeEntry(fh, desc, true, false);
      writeEntry(fh, cat1, true, false);
      writeEntry(fh, cat2, true, false);
      writeEntry(fh, cosAmt, true, false);
      writeEntry(fh, cosUnitPrice, true, false);
      writeEntry(fh, cosQty, false, true);
    }
    else
    {
      scoutln(out, bytesOut, "<tr>");
    
      scoutln(out, bytesOut, "<td><p>" + invoiceCode + "</td>");
      scoutln(out, bytesOut, "<td><p>" + invoiceDate + "</td>");

      scoutln(out, bytesOut, "<td><p>" + currency + "</td>");
      scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(amt, '2') + "</td>");
      scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(sgdAmt, '2') + "</td>");
      scoutln(out, bytesOut, "<td><p>" + currency + "</td>");
      scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(unitPrice, '2') + "</td>");
      scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(sgdUnitPrice, '2') + "</td>");

      scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(qty, '0') + "</td>");
      scoutln(out, bytesOut, "<td><p>" + gstRate + "</td>");

      scoutln(out, bytesOut, "<td><p>" + cnCode + "</td>");
      scoutln(out, bytesOut, "<td><p>" + cnCurrency + "</td>");
      scoutln(out, bytesOut, "<td><p>" + cnAmt + "</td>");

      scoutln(out, bytesOut, "<td><p>" + mfr + "</td>");
      scoutln(out, bytesOut, "<td><p>" + custCode + "</td>");
      scoutln(out, bytesOut, "<td><p>" + custName + "</td>");
      scoutln(out, bytesOut, "<td><p>" + industryType + "</td>");
      scoutln(out, bytesOut, "<td><p>" + industryType2 + "</td>");

      scoutln(out, bytesOut, "<td><p>" + itemCode + "</td>");
      scoutln(out, bytesOut, "<td><p>" + desc + "</td>");
      scoutln(out, bytesOut, "<td><p>" + cat1 + "</td>");
      scoutln(out, bytesOut, "<td><p>" + cat2 + "</td>");

      scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(cosAmt, '2') + "</td>");
      scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(cosUnitPrice, '2') + "</td>");
      scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(cosQty, '0') + "</td>");
    
    
      scoutln(out, bytesOut, "</tr>");
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void outputTitleLine(RandomAccessFile fh, PrintWriter out, boolean isCSV, int[] bytesOut) throws Exception
  {
    if(isCSV)
    {  
      writeEntry(fh, "Invoice Code", true, false);
      writeEntry(fh, "Invoice Date", true, false);
      writeEntry(fh, "Issue Currency", true, false);
      writeEntry(fh, "Issue Amount", true, false);
      writeEntry(fh, "SGD Amount", true, false);
      writeEntry(fh, "Issue Currency", true, false);
      writeEntry(fh, "Issue Unit Price", true, false);
      writeEntry(fh, "SGD Unit Price", true, false);
      writeEntry(fh, "Quantity", true, false);
      writeEntry(fh, "GST Rate", true, false);
      writeEntry(fh, "Credit Note Code", true, false);
      writeEntry(fh, "Credit Note Currency", true, false);
      writeEntry(fh, "Credit Note Amount", true, false);
      writeEntry(fh, "Manufacturer", true, false);
      writeEntry(fh, "Customer Code", true, false);
      writeEntry(fh, "Customer Name", true, false);
      writeEntry(fh, "Industry Type", true, false);
      writeEntry(fh, "Sub-Industry Type", true, false);
      writeEntry(fh, "Item Code", true, false);
      writeEntry(fh, "Description", true, false);
      writeEntry(fh, "Category", true, false);
      writeEntry(fh, "SubCategory", true, false);
      writeEntry(fh, "COS Amount", true, false);
      writeEntry(fh, "COS Unit Price", true, false);
      writeEntry(fh, "Quantity", false, true);     
    }
    else
    {
      scoutln(out, bytesOut, "<tr id=\"pageColumn\">");
      
      scoutln(out, bytesOut, "<td><p>Invoice Code</td><td><p>Invoice Date</td><td><p>Issue Currency</td><td><p>Issue Amount</td><td><p>SGD Amount</td><td><p>Issue Currency</td>"
                           + "<td><p>Issue Unit Price</td><td><p>SGD Unit Price</td><td><p>Quantity</td><td><p>GST Rate</td><td><p>CN Code</td><td><p>CN Currency</td><td><p>CN AMount</td><td><p>Manufacturer</td><td><p>Customer Code</td>");
      scoutln(out, bytesOut, "<td><p>Customer Name</td><td><p>IndustryType</td><td><p>IndustryType2</td><td><p>Item Code</td><td><p>Description</td><td><p>COS Amount</td><td><p>Category</td><td><p>SubCategory</td><td><p>COS UnitPrice</td>"
                           + "<td><p>Quantity</td></tr>");
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void outputPageFrame(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, boolean plain, String effectiveDate, String mfr, String bodyStr, String title, String unm, String sid, String uty, String men,
                               String den, String dnm, String bnm, String otherSetup, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    int[] hmenuCount = new int[1];  hmenuCount[0] = 0;

    String subMenuText = "";

    scoutln(out, bytesOut, authenticationUtils.buildScreen(con, stmt, rs, req, true, unm, sid, uty, men, den, dnm, bnm, "ConsolidationsSalesInvoice", subMenuText, hmenuCount[0], bodyStr, otherSetup, localDefnsDir, defnsDir));
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void writeEntry(RandomAccessFile fh, String entry, boolean comma, boolean newLine) throws Exception
  {
    if(entry == null) entry = "";

    fh.writeBytes("\"");
    for(int x=0;x<entry.length();++x)
    {
      if(entry.charAt(x) == '"')
        fh.writeBytes("''");
      else fh.writeBytes("" + entry.charAt(x));
    }

    fh.writeBytes("\"");

    if(comma)
      fh.writeBytes(",");

    if(newLine)
      fh.writeBytes("\n");
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void download(HttpServletResponse res, String dirName, String fileName, int[] bytesOut) throws Exception
  {
    res.setContentType("application/x-download");
    res.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

    OutputStream out = res.getOutputStream();

    InputStream in = null;
    try
    {
      in = new BufferedInputStream(new FileInputStream(dirName + fileName));
      byte[] buf = new byte[4096];
      int bytesRead;
      while((bytesRead = in.read(buf)) != -1)
        out.write(buf, 0, bytesRead);
    }
    catch(Exception e) //finally
    {
      if(in != null)
        in.close();
    }

    File file = new File(dirName + fileName);
    long fileSize = file.length();

    bytesOut[0] += (int)fileSize;
  }
    
  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean generateCNs(Connection con, Statement stmt, ResultSet rs, String invoiceCode, String itemCode, String[] cnCode, String[] currency, String[] amt) throws Exception
  {
    boolean res = false;
    
    stmt = con.createStatement();
  
    stmt.setMaxRows(1);
    
    rs = stmt.executeQuery("SELECT t1.CNCode, t1.Currency, t2.Amount FROM credit AS t1 INNER JOIN creditl AS t2 ON t1.CNCode = t2.CNCode WHERE t1.Status != 'C' AND t2.InvoiceCode = '" + invoiceCode + "' AND t2.ItemCode = '" + itemCode + "'");

    while(rs.next())                  
    {
      cnCode[0]   = rs.getString(1);
      currency[0] = rs.getString(2);
      amt[0]      = rs.getString(3);
      
      res = true;
    }
  
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return res;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void getCOS(Connection con, Statement stmt, ResultSet rs, String soCode, String itemCode, String[] cosAmt, String[] cosUnitPrice, String[] cosQty, String invoiceDate, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    cosAmt[0] = cosUnitPrice[0] = cosQty[0] = "0";
    
    if(soCode.length() == 0) return;

    // invoicel has SOCode (but not SOLine)
    
    stmt = con.createStatement();

    stmt.setMaxRows(1);
    
    rs = stmt.executeQuery("SELECT Line FROM sol WHERE SOCode = '" + generalUtils.sanitiseForSQL(soCode) + "' AND ItemCode = '" + generalUtils.sanitiseForSQL(itemCode) + "'");

    String soLine = "";
    
    if(rs.next())
      soLine = rs.getString(1);

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    if(soLine.length() == 0) return;
    
    // use SO details to get PO details
    
    stmt = con.createStatement();

    stmt.setMaxRows(1);
    
    rs = stmt.executeQuery("SELECT Amount, UnitPrice, Quantity FROM pol WHERE SOCode = '" + generalUtils.sanitiseForSQL(soCode) + "' AND SOLine = '" + generalUtils.sanitiseForSQL(soLine) + "'");

    boolean found = false;
    
    if(rs.next())
    {
      cosAmt[0]       = rs.getString(1);
      cosUnitPrice[0] = rs.getString(2);
      cosQty[0]       = rs.getString(3);
      
      found = true;
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    if(found) return;
    
    // use SO details to get LP details
    
    stmt = con.createStatement();

    stmt.setMaxRows(1);
    
    rs = stmt.executeQuery("SELECT Amount, UnitPrice, Quantity FROM lpl WHERE SOCode = '" + generalUtils.sanitiseForSQL(soCode) + "' AND SOLine = '" + generalUtils.sanitiseForSQL(soLine) + "'");

    if(rs.next())
    {
      cosAmt[0]       = rs.getString(1);
      cosUnitPrice[0] = rs.getString(2);
      cosQty[0]       = rs.getString(3);

      found = true;
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();    

    if(found) return;

    cosUnitPrice[0] = "" + determinePurchasePrice(con, stmt, rs, itemCode, invoiceDate, dnm, localDefnsDir, defnsDir);    
    cosAmt[0]       = "0";
    cosQty[0]       = "1";
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private double determinePurchasePrice(Connection con, Statement stmt, ResultSet rs, String itemCode, String invoiceDate, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    double purchasePrice = 0.0;

    {
      int count = 0;
      double[] up  = new double[1200];
      double[] qty = new double[1200];

      String currency = "";
      byte[] actualDate = new byte[20];
  
      String yearStart = "2009-01-01";
      
      stmt = con.createStatement();

      stmt.setMaxRows(500);

      rs = stmt.executeQuery("SELECT t2.UnitPrice, t1.Currency, t2.Quantity FROM pol AS t2 INNER JOIN po AS t1 ON t1.POCode = t2.POCode WHERE t2.ItemCode = '" + generalUtils.sanitiseForSQL(itemCode)
                           + "' AND t1.Date <= {d '" + invoiceDate + "'} AND t1.Date >= {d '" + yearStart + "'} AND t1.Status != 'C' ORDER BY t2.POCode");
 
      double  quantity, unitPrice;
      double rate = 1.0;
  
      while(rs.next())
      {
        unitPrice = generalUtils.doubleFromStr(rs.getString(1));
        currency = rs.getString(2);
        quantity  = generalUtils.doubleFromStr(rs.getString(3));

        rate = accountsUtils.getApplicableRate(con, stmt, rs, currency, invoiceDate, actualDate, dnm, localDefnsDir, defnsDir);
        
        if(rate == 0) rate = 1;

        if(quantity > 0)
        {
          up[count] = unitPrice * rate;
          qty[count++] = quantity;
        }
      }

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();

      // LP
      
      stmt = con.createStatement();

      stmt.setMaxRows(500);
      
      rs = stmt.executeQuery("SELECT t2.UnitPrice, t1.Currency, t2.Quantity FROM lpl AS t2 INNER JOIN lp AS t1 ON t1.LPCode = t2.LPCode WHERE t2.ItemCode = '" + generalUtils.sanitiseForSQL(itemCode)
                           + "' AND t1.Date <= {d '" + invoiceDate + "'} AND t1.Date >= {d '" + yearStart + "'} AND t1.Status != 'C' ORDER BY t2.LPCode");
      
      rate = 1.0;
  
      while(rs.next())
      {
        unitPrice = generalUtils.doubleFromStr(rs.getString(1));
        currency = rs.getString(2);
        quantity  = generalUtils.doubleFromStr(rs.getString(3));

        rate = accountsUtils.getApplicableRate(con, stmt, rs, currency, invoiceDate, actualDate, dnm, localDefnsDir, defnsDir);

        if(rate == 0) rate = 1;

        if(quantity > 0)
        {
          up[count] = unitPrice * rate;
          qty[count++] = quantity;
        }
      }

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();

      // OB
      
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT ClosingWac, ClosingLevel FROM stock WHERE ItemCode = '" + generalUtils.sanitiseForSQL(itemCode) + "'");
      
      if(rs.next())
      {
        double d = generalUtils.doubleFromStr(rs.getString(2));
        if(d > 0)
        {
          up[count] = generalUtils.doubleFromStr(rs.getString(1));////////// / d;
          qty[count++] = d;
        }
      }
      
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
      
      double totalUP = 0.0, totalQty = 0.0;
      
      if(count > 0)
      {
        for(int x=0;x<count;++x)
        {
          totalUP += (up[x] * qty[x]);
          totalQty += qty[x];
        }
        
        purchasePrice = totalUP / totalQty;
      }
    }

    if(purchasePrice == 0.0)
    {
      String[] purchasePrice2 = new String[1];
      String[] currency       = new String[1];
      String[] exchangeRate   = new String[1];
      String[] dateChanged    = new String[1];
    
      inventory.getPurchaseDetailsGivenCode(con, stmt, rs, itemCode, purchasePrice2, currency, exchangeRate, dateChanged);
    
      byte[] actualDate = new byte[20];
      double endYearExchRate = accountsUtils.getApplicableRate(con, stmt, rs, currency[0], invoiceDate, actualDate, dnm, localDefnsDir, defnsDir);
      if(endYearExchRate == 0)
        endYearExchRate = 1;
    
      purchasePrice = generalUtils.doubleFromStr(purchasePrice2[0]) * endYearExchRate;
    }
            
    if(purchasePrice == 0) purchasePrice = 1.24;

    return purchasePrice;
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void getCategories(Connection con, Statement stmt, ResultSet rs, String itemCode, String[] cat1, String[] cat2) throws Exception
  {
    cat1[0] = cat2[0] = "";
    
    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT NewCategory1, NewCategory2 FROM stock WHERE ItemCode = '" + generalUtils.sanitiseForSQL(itemCode) + "'");
      
      if(rs.next())
      {
        cat1[0] = generalUtils.deNull(rs.getString(1));
        cat2[0] = generalUtils.deNull(rs.getString(2));
      }
    }
    catch(Exception e)
    {
      System.out.println("getCategories(): " + e);
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

}

