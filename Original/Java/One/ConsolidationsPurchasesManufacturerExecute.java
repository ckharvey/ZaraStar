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

public class ConsolidationsPurchasesManufacturerExecute extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  AccountsUtils accountsUtils = new AccountsUtils();
  DashboardUtils dashboardUtils = new DashboardUtils();
  Supplier supplier = new Supplier();
  
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="", p3="";

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
      p1  = req.getParameter("p1"); // which
      p2  = req.getParameter("p2"); // mfr
      p3  = req.getParameter("p3"); // include items
      
      if(p3 == null) p3 = "Y";

      if(p1.equals("S"))
        out = res.getWriter();

      doIt(out, req, res, unm, sid, uty, men, den, dnm, bnm, p1, p2, p3, bytesOut);
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
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "ConsolidationsPurchasesManufacturerExecute", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6913, bytesOut[0], 0, "ERR:" + p1+":"+p2);
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, HttpServletResponse res, String unm, String sid, String uty, String men, String den, String dnm,
                    String bnm, String p1, String p2, String p3, int[] bytesOut) throws Exception
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
 
     Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/xxx_ofsa?user=" + uName + "&password=" + pWord + "&autoReconnect=true");
   
     
    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 6913, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "ConsolidationsPurchasesManufacturer", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6913, bytesOut[0], 0, "ACC:" + p1+":"+p2);
      if(con  != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "ConsolidationsPurchasesManufacturer", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6913, bytesOut[0], 0, "SID:" + p1+":"+p2);
      if(con  != null) con.close();
      if(out != null) out.flush(); 
      return;
    }
    
    boolean includeItems;
    if(p3.equals("Y"))
      includeItems = true;
    else includeItems = false;

    boolean isCSV = false;
    
    if(p1.equals("C"))
      isCSV = true;

    RandomAccessFile fh = null;

    if(isCSV)
    {
      String fName;
      if(p2.equals("-")) fName = "6913.csv"; else fName = p2 + "6913.csv";
      
      fh = generalUtils.create(workingDir + fName);

      processCSV(fh, con, stmt, stmt2, stmt3, rs, rs2, rs3, out, includeItems, p2, bytesOut);
      
      generalUtils.fileClose(fh);

      download(res, workingDir, fName, bytesOut);
    }
    else processScreen(fh, con, stmt, stmt2, stmt3, rs, rs2, rs3, out, req, p1, p2, includeItems, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 6913, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1+":"+p2+":"+p3);
    
    if(con  != null) con.close();
    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void processScreen(RandomAccessFile fh, Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, ResultSet rs3, PrintWriter out, HttpServletRequest req, String p1, String p2, boolean includeItems,
                             String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Purchases by Manufacturer</title>");

    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");
   
    outputPageFrame(con, stmt, rs, out, req, false, p1, p2, "", "Purchases by Manufacturer", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, bytesOut);

    scoutln(out, bytesOut, "<form>");
  
    scout(out, bytesOut, "<p>For: " + p2);
    
    scout(out, bytesOut, "<p>Consolidated into " + accountsUtils.getBaseCurrency(con, stmt, rs, dnm, localDefnsDir, defnsDir));
    
    scoutln(out, bytesOut, "<br><table id=\"page\" width=100%>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    outputTitleLine(fh, out, false, includeItems, bytesOut);

    if(p2.equals("-"))
      forEachMfr(fh, out, false, con, stmt, stmt2, stmt3, rs, rs2, rs3, includeItems, bytesOut);
    else forAMfr(fh, out, false, con, stmt, stmt2, rs, rs2, includeItems, p2, bytesOut);    
    
    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void processCSV(RandomAccessFile fh, Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, ResultSet rs3, PrintWriter out, boolean includeItems, String mfr, int[] bytesOut) throws Exception
  {
    outputTitleLine(fh, out, true, includeItems, bytesOut);

    if(mfr.equals("-"))
      forEachMfr(fh, out, true, con, stmt, stmt2, stmt3, rs, rs2, rs3, includeItems, bytesOut);
    else forAMfr(fh, out, true, con, stmt, stmt2, rs, rs2, includeItems, mfr, bytesOut);    
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void forEachMfr(RandomAccessFile fh, PrintWriter out, boolean isCSV, Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, ResultSet rs3, boolean includeItems, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT DISTINCT Manufacturer FROM stock ORDER BY Manufacturer");

    String mfr;

    while(rs.next())
    {
      mfr = rs.getString(1);

      forAMfr(fh, out, isCSV, con, stmt2, stmt3, rs2, rs3, includeItems, mfr, bytesOut);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void forAMfr(RandomAccessFile fh, PrintWriter out, boolean isCSV, Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, boolean includeItems, String mfr, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT SupplierCode FROM supplier ORDER BY Name");

    String suppCode, suppName;
    double amt1, amt2, amt3, amt4;
    boolean first = true;
    
    while(rs.next())
    {
      suppCode = rs.getString(1);

      suppName = supplier.getSupplierNameGivenCode(con, stmt2, rs2, suppCode);
      
      amt1 = forASupplier(con, stmt2, rs2, suppCode, "2014-01-01", "2014-12-31", mfr);
      amt2 = forASupplier(con, stmt2, rs2, suppCode, "2013-01-01", "2013-12-31", mfr);
      amt3 = forASupplier(con, stmt2, rs2, suppCode, "2012-01-01", "2012-12-31", mfr);
      amt4 = forASupplier(con, stmt2, rs2, suppCode, "2011-01-01", "2011-12-31", mfr);
    
      if(amt1 != 0.0 || amt2 != 0.0 || amt3 != 0.0 || amt4 != 0.0)
      {
        outputLine(fh, out, isCSV, first, mfr, suppCode, suppName, amt1, amt2, amt3, amt4, bytesOut);
        
        if(includeItems)
          forASupplierAllItems(fh, out, isCSV, con, stmt2, rs2, suppCode, "2011-01-01", "2014-12-31", mfr, bytesOut);
        
        first = false;
      }
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private double forASupplier(Connection con, Statement stmt, ResultSet rs, String suppCode, String dateFrom, String dateTo, String mfr) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.Amount FROM po AS t1 INNER JOIN pol AS t2 ON t1.POCode = t2.POCode WHERE t1.CompanyCode = '" + suppCode + "' AND t2.Manufacturer = '"
                         + generalUtils.sanitiseForSQL(mfr) + "' AND t1.Date >= {d '" + dateFrom + "'} AND t1.Date <= {d '" + dateTo + "'} AND t1.Status != 'C'");

    double amount = 0.0;

    while(rs.next())
      amount += generalUtils.doubleFromStr(generalUtils.doubleDPs(rs.getString(1), '2'));

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return amount;
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private double forASupplierAllItems(RandomAccessFile fh, PrintWriter out, boolean isCSV, Connection con, Statement stmt, ResultSet rs, String suppCode, String dateFrom, String dateTo, String mfr, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.Amount, t2.ItemCode, t2.Description, t1.Currency, t2.Quantity, t1.POCode, t1.Date, t2.Amount2, t2.Line FROM po AS t1 INNER JOIN pol AS t2 ON t1.POCode = t2.POCode WHERE t1.CompanyCode = '"
                         + suppCode + "' AND t2.Manufacturer = '" + generalUtils.sanitiseForSQL(mfr) + "' AND t1.Date >= {d '" + dateFrom + "'} AND t1.Date <= {d '" + dateTo + "'} AND t1.Status != 'C'");

    double amount = 0.0;
    String amt, itemCode, desc, currency, qty, poCode, poDate, amt2, poLine;
    String[] invoiceCode = new String[1];
    String[] invoiceDate = new String[1];

    while(rs.next())
    {
      amt      = rs.getString(1);
      itemCode = rs.getString(2);
      desc     = generalUtils.deNull(rs.getString(3));
      currency = generalUtils.deNull(rs.getString(4));
      qty      = generalUtils.deNull(rs.getString(5));
      poCode = generalUtils.deNull(rs.getString(6));
      poDate = generalUtils.deNull(rs.getString(7));
      amt2        = rs.getString(8);
      poLine       = rs.getString(9);
      
      amount += generalUtils.doubleFromStr(generalUtils.doubleDPs(amt, '2'));

      getInvoiceCodeAndDateGivenPOCodeAndLine(con, stmt, rs, poCode, poLine, invoiceCode, invoiceDate);      

      outputLine2(fh, out, isCSV, itemCode, desc, amt2, currency, qty, poCode, poDate, invoiceCode[0], invoiceDate[0], bytesOut);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return amount;
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void outputLine(RandomAccessFile fh, PrintWriter out, boolean isCSV, boolean first, String mfr, String suppCode, String suppName, double purchases2014, double purchases2013, double purchases2012, double purchases2011, int[] bytesOut) 
                          throws Exception
  {
    if(isCSV)
    {  
      if(first)
        writeEntry(fh, mfr, true, false);
      else writeEntry(fh, "", true, false);
      
      writeEntry(fh, suppCode, true, false);
      writeEntry(fh, suppName, true, false);

      writeEntry(fh, generalUtils.doubleToStr('2', purchases2014), true, false);
      writeEntry(fh, generalUtils.doubleToStr('2', purchases2013), true, false);
      writeEntry(fh, generalUtils.doubleToStr('2', purchases2012), true, false);
      writeEntry(fh, generalUtils.doubleToStr('2', purchases2011), false, true);
    }
    else
    {
      scoutln(out, bytesOut, "<tr>");
    
      if(first)
        scoutln(out, bytesOut, "<td><p>" + mfr + "</td>");
      else scoutln(out, bytesOut, "<td></td>");

      scoutln(out, bytesOut, "<td><p>" + suppCode + "</td>");
      scoutln(out, bytesOut, "<td><p>" + suppName + "</td>");

      scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(purchases2014, '2') + "</td>");
      scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(purchases2013, '2') + "</td>");
      scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(purchases2012, '2') + "</td>");
      scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(purchases2011, '2') + "</td>");
    
      scoutln(out, bytesOut, "</tr>");
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void outputLine2(RandomAccessFile fh, PrintWriter out, boolean isCSV, String itemCode, String desc, String amt, String currency, String qty, String poCode, String poDate, String invoiceCode, String invoiceDate, int[] bytesOut)
                           throws Exception
  {
    if(isCSV)
    {  
      writeEntry(fh, "", true, false);
      writeEntry(fh, "", true, false);
      writeEntry(fh, "", true, false);
      writeEntry(fh, "", true, false);
      writeEntry(fh, "", true, false);
      writeEntry(fh, "", true, false);
      writeEntry(fh, "", true, false);

      writeEntry(fh, itemCode, true, false);
      writeEntry(fh, desc,     true, false);

      writeEntry(fh, currency + " " + generalUtils.formatNumeric(amt, '2'), true, false);

      writeEntry(fh, generalUtils.formatNumeric(qty, '0'), true, false);

      writeEntry(fh, poCode, true, false);
      writeEntry(fh, poDate, true, false);

      writeEntry(fh, invoiceCode, true, false);
      writeEntry(fh, invoiceDate, false, true);
    }
    else
    {
      scoutln(out, bytesOut, "<tr>");
    
      scoutln(out, bytesOut, "<td></td>");
      scoutln(out, bytesOut, "<td></td>");
      scoutln(out, bytesOut, "<td></td>");
      scoutln(out, bytesOut, "<td></td>");
      scoutln(out, bytesOut, "<td></td>");
      scoutln(out, bytesOut, "<td></td>");
      scoutln(out, bytesOut, "<td></td>");

      scoutln(out, bytesOut, "<td><p>" + itemCode + "</td>");
      scoutln(out, bytesOut, "<td><p>" + desc + "</td>");

      scoutln(out, bytesOut, "<td align=right><p>" + currency + " " + generalUtils.formatNumeric(amt, '2') + "</td>");
    
      scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(qty, '0') + "</td>");
 
      scoutln(out, bytesOut, "<td><p>" + poCode + "</td>");
      scoutln(out, bytesOut, "<td><p>" + poDate + "</td>");
      scoutln(out, bytesOut, "<td><p>" + invoiceCode + "</td>");
      scoutln(out, bytesOut, "<td><p>" + invoiceDate + "</td>");
    
      scoutln(out, bytesOut, "</tr>");
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void outputTitleLine(RandomAccessFile fh, PrintWriter out, boolean isCSV, boolean includeItems, int[] bytesOut) throws Exception
  {
    if(isCSV)
    {  
      writeEntry(fh, "Manufacturer", true, false);
      writeEntry(fh, "Supplier Code", true, false);
      writeEntry(fh, "Supplier Name", true, false);
      writeEntry(fh, "2014 Purchases", true, false);
      writeEntry(fh, "2013 Purchases", true, false);
      writeEntry(fh, "2012 Purchases", true, false);
      
      if(! includeItems)
        writeEntry(fh, "2011 Purchases", false, true);
      else
      {
        writeEntry(fh, "Item Code", true, false);
        writeEntry(fh, "Description", true, false);
        writeEntry(fh, "Purchase Amount", true, false);    
        writeEntry(fh, "Quantity",    true, false);
        writeEntry(fh, "PO Code", true, false);
        writeEntry(fh, "PO Date", true, false);
        writeEntry(fh, "Invoice Code", true, false);
        writeEntry(fh, "Invoice Date", false, true);
      }
    }
    else
    {
      scoutln(out, bytesOut, "<tr id=\"pageColumn\">");
      if(! includeItems)
        scoutln(out, bytesOut, "<td><p>Manufacturer</td><td><p>Supplier Code</td><td><p>Supplier Name</td><td><p>2014 Purchases</td><td><p>2013 Purchases</td><td><p>2012 Purchases</td><td><p>2011 Purchases</td></tr>");
      else
        scoutln(out, bytesOut, "<td><p>Manufacturer</td><td><p>Supplier Code</td><td><p>Supplier Name</td><td><p>2014 Purchases</td><td><p>2013 Purchases</td><td><p>2012 Purchases</td><td><p>2011 Purchases</td><td><p>Item Code</td><td><p>Description</td>"
                             + "<td><p>Purchase Amount</td><td><p>Quantity</td><td><p>PO Code</td><td><p>PO Date</td><td><p>Invoice Code</td><td><p>Invoice Date</td></tr>");
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void outputPageFrame(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, boolean plain, String effectiveDate, String mfr, String bodyStr, String title, String unm, String sid, String uty, String men,
                               String den, String dnm, String bnm, String otherSetup, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    int[] hmenuCount = new int[1];  hmenuCount[0] = 0;

    String subMenuText = "";

    scoutln(out, bytesOut, authenticationUtils.buildScreen(con, stmt, rs, req, true, unm, sid, uty, men, den, dnm, bnm, "ConsolidationsPurchasesManufacturer", subMenuText, hmenuCount[0], bodyStr, otherSetup, localDefnsDir, defnsDir));
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

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void getInvoiceCodeAndDateGivenPOCodeAndLine(Connection con, Statement stmt, ResultSet rs, String poCode, String poLine, String[] invoiceCode, String[] invoiceDate) throws Exception
  {
    invoiceCode[0] = "";
    invoiceDate[0] = "";
    
    stmt = con.createStatement();

    stmt.setMaxRows(1);
    
    rs = stmt.executeQuery("SELECT SupplierInvoiceCode FROM grl WHERE POCode = '" + generalUtils.sanitiseForSQL(poCode) + "' AND POLine = '" + generalUtils.sanitiseForSQL(poLine) + "'");

    if(rs.next())
      invoiceCode[0] = rs.getString(1);

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    if(invoiceCode[0].length() == 0) return;
    
    stmt = con.createStatement();

    stmt.setMaxRows(1);
    
    rs = stmt.executeQuery("SELECT Date FROM pinvoice WHERE InvoiceCode = '" + generalUtils.sanitiseForSQL(poCode) + "'");

    if(rs.next())
      invoiceDate[0] = rs.getString(1);

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

}
