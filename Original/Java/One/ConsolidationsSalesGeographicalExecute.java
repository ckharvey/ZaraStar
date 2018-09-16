// =======================================================================================================================================================================================================
// System: ZaraStar Info: Consolidations
// Module: ConsolidationsSalesGeographicalExecute.java
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

public class ConsolidationsSalesGeographicalExecute extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  AccountsUtils accountsUtils = new AccountsUtils();
  DashboardUtils dashboardUtils = new DashboardUtils();
  Customer customer = new Customer();
  
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
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "ConsolidationsSalesGeographicalExecute", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6911, bytesOut[0], 0, "ERR:" + p1);
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
 
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/xxx_ofsa?user=" + uName + "&password=" + pWord + "&autoReconnect=true");
    
    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 6911, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "ConsolidationsSalesGeographical", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6911, bytesOut[0], 0, "ACC:" + p1);
      if(con  != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "ConsolidationsSalesGeographical", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6911, bytesOut[0], 0, "SID:" + p1);
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
      String fName = "6911.csv";
      
      fh = generalUtils.create(workingDir + fName);

      processCSV(fh, con, stmt, rs, out, bytesOut);
      
      generalUtils.fileClose(fh);

      download(res, workingDir, fName, bytesOut);
    }
    else processScreen(fh, con, stmt, rs, out, req, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 6911, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    
    if(con  != null) con.close();
    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void processScreen(RandomAccessFile fh, Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, 
                             String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Sales by Geographical Location</title>");

    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");
   
    outputPageFrame(con, stmt, rs, out, req, false, "", "", "", "Sales by Geographical Location", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, bytesOut);

    scoutln(out, bytesOut, "<form>");
  
    scoutln(out, bytesOut, "<br><table id=\"page\" width=100%>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    outputTitleLine(fh, out, false, bytesOut);

    forEachCountry(fh, out, false, con, stmt, rs, "2011-01-01", "2014-12-31", bytesOut);
    
    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void processCSV(RandomAccessFile fh, Connection con, Statement stmt, ResultSet rs, PrintWriter out, int[] bytesOut) throws Exception
  {
    outputTitleLine(fh, out, true, bytesOut);

    forEachCountry(fh, out, true, con, stmt, rs, "2011-01-01", "2014-12-31", bytesOut);
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void forEachCountry(RandomAccessFile fh, PrintWriter out, boolean isCSV, Connection con, Statement stmt, ResultSet rs, String dateFrom, String dateTo, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT DISTINCT Country FROM company ORDER BY Country");

    String country;

    while(rs.next())
    {
      country = generalUtils.deNull(rs.getString(1));

      forEachCustomer(fh, out, isCSV, con, stmt, rs, country, dateFrom, dateTo, bytesOut);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void forEachCustomer(RandomAccessFile fh, PrintWriter out, boolean isCSV, Connection con, Statement stmt, ResultSet rs, String country, String dateFrom, String dateTo, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT CompanyCode, Name, IndustryType FROM company WHERE Country = '" + generalUtils.sanitiseForSQL(country) + "' ORDER BY Name");

    String code, name, industryType;

    while(rs.next())
    {
      code         = rs.getString(1);
      name         = generalUtils.deNull(rs.getString(2));
      industryType = generalUtils.deNull(rs.getString(3));
      
      forEachInvoice(fh, out, isCSV, con, stmt, rs, code, dateFrom, dateTo, name, country, industryType, bytesOut);      
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private double forEachInvoice(RandomAccessFile fh, PrintWriter out, boolean isCSV, Connection con, Statement stmt, ResultSet rs, String custCode, String dateFrom, String dateTo, String name, String country, String industryType, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.Amount2, t2.ItemCode, t2.Description, t1.Currency, t2.Quantity, t1.InvoiceCode, t1.Date, "
                         + "t2.UnitPrice, t2.Manufacturer FROM invoice AS t1 INNER JOIN invoicel AS t2 ON t1.InvoiceCode = t2.InvoiceCode WHERE "
                         + "t1.CompanyCode = '" + generalUtils.sanitiseForSQL(custCode) + "' AND t1.Date >= {d '" + dateFrom + "'} AND t1.Date <= {d '" + dateTo + "'} AND t1.Status != 'C' ORDER BY t1.Date");

    double amount = 0.0;
    String amt, itemCode, desc, currency, qty, invoiceCode, invoiceDate, unitPrice, mfr;

    while(rs.next())
    {
      amt      = rs.getString(1);
      itemCode = rs.getString(2);
      desc     = generalUtils.deNull(rs.getString(3));
      currency = generalUtils.deNull(rs.getString(4));
      qty      = generalUtils.deNull(rs.getString(5));
      invoiceCode = generalUtils.deNull(rs.getString(6));
      invoiceDate = generalUtils.deNull(rs.getString(7));
      unitPrice = generalUtils.deNull(rs.getString(8));
      mfr = generalUtils.deNull(rs.getString(9));
    
      outputLine(fh, out, isCSV, itemCode, desc, amt, currency, qty, invoiceCode, invoiceDate, unitPrice, mfr, custCode, name, industryType, country, bytesOut);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return amount;
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void outputLine(RandomAccessFile fh, PrintWriter out, boolean isCSV, String itemCode, String desc, String amt, String currency, String qty, String invoiceCode, String invoiceDate, String unitPrice, String mfr, String custCode,
                          String custName, String industryType, String country, int[] bytesOut) throws Exception
  {
    if(isCSV)
    {  
      writeEntry(fh, country, true, false);
      writeEntry(fh, custName, true, false);
      writeEntry(fh, custCode, true, false);
      writeEntry(fh, industryType, true, false);
      writeEntry(fh, itemCode, true, false);
      writeEntry(fh, desc, true, false);
      writeEntry(fh, mfr, true, false);
      writeEntry(fh, amt, true, false);
      writeEntry(fh, currency, true, false);
      writeEntry(fh, unitPrice, true, false);
      writeEntry(fh, qty, true, false);
      writeEntry(fh, invoiceCode, true, false);
      writeEntry(fh, invoiceDate, false, true);
    }
    else
    {
      scoutln(out, bytesOut, "<tr>");
    
      scoutln(out, bytesOut, "<td><p>" + country + "</td>");

      scoutln(out, bytesOut, "<td><p>" + custName + "</td>");
      scoutln(out, bytesOut, "<td><p>" + custCode + "</td>");

      scoutln(out, bytesOut, "<td><p>" + industryType + "</td>");

      scoutln(out, bytesOut, "<td><p>" + itemCode + "</td>");
      scoutln(out, bytesOut, "<td><p>" + desc + "</td>");

      scoutln(out, bytesOut, "<td><p>" + mfr + "</td>");

      scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(amt, '2') + "</td>");
      scoutln(out, bytesOut, "<td><p>" + currency + "</td>");
      scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(unitPrice, '2') + "</td>");

      scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(qty, '0') + "</td>");

      scoutln(out, bytesOut, "<td><p>" + invoiceCode + "</td>");
      scoutln(out, bytesOut, "<td><p>" + invoiceDate + "</td>");
    
      scoutln(out, bytesOut, "</tr>");
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void outputTitleLine(RandomAccessFile fh, PrintWriter out, boolean isCSV, int[] bytesOut) throws Exception
  {
    if(isCSV)
    {  
      writeEntry(fh, "Country", true, false);
      writeEntry(fh, "Customer Name", true, false);
      writeEntry(fh, "Customer Code", true, false);
      writeEntry(fh, "IndustryType", true, false);
      writeEntry(fh, "Item Code", true, false);
      writeEntry(fh, "Description", true, false);
      writeEntry(fh, "Manufacturer", true, false);
      writeEntry(fh, "Amount", true, false);
      writeEntry(fh, "Currency", true, false);
      writeEntry(fh, "Unit Price", true, false);
      writeEntry(fh, "Quantity", true, false);     
      writeEntry(fh, "Invoice Code", true, false);
      writeEntry(fh, "Invoice Date", false, true);
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void outputPageFrame(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, boolean plain, String effectiveDate, String mfr, String bodyStr, String title, String unm, String sid, String uty, String men,
                               String den, String dnm, String bnm, String otherSetup, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    int[] hmenuCount = new int[1];  hmenuCount[0] = 0;

    String subMenuText = "";

    scoutln(out, bytesOut, authenticationUtils.buildScreen(con, stmt, rs, req, true, unm, sid, uty, men, den, dnm, bnm, "ConsolidationsSalesGeographical", subMenuText, hmenuCount[0], bodyStr, otherSetup, localDefnsDir, defnsDir));
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

}
