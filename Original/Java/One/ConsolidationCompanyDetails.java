// =============================================================================================================================================================
// System: ZaraStar Consolidation: List companies
// Module: ConsolidationCompanyDetails.java
// Author: C.K.Harvey
// Copyright (c) 2006-14 Christopher Harvey. All Rights Reserved.
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

public class ConsolidationCompanyDetails extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();

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
      p1  = req.getParameter("p1"); // Customer or Supplier
      
      if(p1 == null) p1 = "";
      
      doIt(out, req, p1, unm, sid, uty, men, den, dnm, bnm, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "ConsolidationCompanyDetails", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6903, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String p1, String unm, String sid, String uty, String men, String den, String dnm,
                    String bnm, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null, stmt2 = null;
    ResultSet rs   = null, rs2   = null;
    
    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "ConsolidationCompanyDetails", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6903, bytesOut[0], 0, "SID:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    process(con, stmt, stmt2, rs, rs2, out, req, p1, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 6903, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void process(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, HttpServletRequest req, String option,
                       String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir,
                       String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Detail</title>");

    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    int[] hmenuCount = new int[1];
    
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "6903", "", "ConsolidationCompanyDetails", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
     
    String s;

    if(option.equals("C"))
      s = "Customers";
    else
        s = "Suppliers";
    
    if(option.equals("C"))
      pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Customer Detail", "6903", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);
    else pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Supplier Detail", "6903", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<form>");

    scoutln(out, bytesOut, "<table id='page' cellspacing=0 cellpadding=2 width=100%>");

    if(option.equals("C"))
    {
      scoutln(out, bytesOut, "<tr><td nowrap><p>Company Code</td><td nowrap><p>Company Name</td><td nowrap><p>SalesPerson</td><td nowrap><p>Address</td><td nowrap><p>Contact Number 1</td><td nowrap><p>Contact Number 2</td><td nowrap><p>Fax Number</td><td nowrap><p>Email Address</td><td nowrap><p>Last Transaction Date</td>");

      String currencies = customerCurrencies(con, stmt, rs, out, bytesOut);

      scoutln(out, bytesOut, "</tr>");
    
      customers(con, stmt, stmt2, rs, rs2, out, currencies, bytesOut);
    }
    else
    if(option.equals("S"))
    {
      scoutln(out, bytesOut, "<tr><td nowrap><p>Company Code</td><td nowrap><p>Company Name</td><td nowrap><p>Address</td><td nowrap><p>Contact Number</td><td nowrap><p>Fax Number</td><td nowrap><p>Email Address</td><td nowrap><p>Last Transaction Date</td>");

      String currencies = supplierCurrencies(con, stmt, rs, out, bytesOut);

      scoutln(out, bytesOut, "</tr>");
    
      suppliers(con, stmt, stmt2, rs, rs2, out, currencies, bytesOut);
    }
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    
    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir));
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void customers(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, String currencies, int[] bytesOut) throws Exception
  {
    String companyCode = "", name, cssFormat = "line1";

    stmt = con.createStatement();
 
    rs = stmt.executeQuery("SELECT CompanyCode, Name, SalesPerson, Address1, Address2, Address3, Address4, Address5, Phone1, Phone2, Fax, EMail FROM company ORDER BY Name");
   
    String salesPerson, addr1, addr2, addr3, addr4, addr5, phone1, phone2, fax, eMail, address, lastTransactionDate;
    String[] currency = new String[1];  currency[0] = "";
    
    while(rs.next())
    {  
      companyCode = rs.getString(1);
      name        = generalUtils.deNull(rs.getString(2));
      salesPerson = generalUtils.deNull(rs.getString(3));
      addr1       = generalUtils.deNull(rs.getString(4));
      addr2       = generalUtils.deNull(rs.getString(5));
      addr3       = generalUtils.deNull(rs.getString(6));
      addr4       = generalUtils.deNull(rs.getString(7));
      addr5       = generalUtils.deNull(rs.getString(8));
      phone1      = generalUtils.deNull(rs.getString(9));
      phone2      = generalUtils.deNull(rs.getString(10));
      fax         = generalUtils.deNull(rs.getString(11));
      eMail       = generalUtils.deNull(rs.getString(12));

      address = addr1 + ", " + addr2 + ", " + addr3 + ", " + addr4 + ", " + addr5;

      if(cssFormat.equals("line1")) cssFormat = "line2"; else cssFormat = "line1";
      
      lastTransactionDate = lastTransactionDateCustomer(con, stmt2, rs2, companyCode);

      scoutln(out, bytesOut, "<tr id='" + cssFormat + "'><td><p>" + companyCode + "</td><td><p>" + name + "</td><td><p>" + salesPerson + "</td><td><p>" + address + "</td><td><p>" + phone1 + "</td><td><p>" + phone2 + "</td><td><p>"
                           + fax + "</td><td><p>" + eMail + "</td><td><p>" + lastTransactionDate + "</td>");

      consolidateCustomer(con, stmt2, rs2, out, companyCode, currencies, bytesOut, currency);

      scoutln(out, bytesOut, "</tr>");
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();    
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void suppliers(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, String currencies, int[] bytesOut) throws Exception
  {
    String companyCode = "", name, cssFormat = "line1";

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT SupplierCode, Name, Address1, Address2, Address3, Address4, Address5, OfficePhone, Fax, EMail FROM supplier ORDER BY Name");
    
    String addr1, addr2, addr3, addr4, addr5, officePhone, fax, eMail, address, lastTransactionDate;
    String[] currency = new String[1];  currency[0] = "";
    
    while(rs.next())
    {  
      companyCode = rs.getString(1);
      name        = generalUtils.deNull(rs.getString(2));
      addr1       = generalUtils.deNull(rs.getString(3));
      addr2       = generalUtils.deNull(rs.getString(4));
      addr3       = generalUtils.deNull(rs.getString(5));
      addr4       = generalUtils.deNull(rs.getString(6));
      addr5       = generalUtils.deNull(rs.getString(7));
      officePhone = generalUtils.deNull(rs.getString(8));
      fax         = generalUtils.deNull(rs.getString(9));
      eMail       = generalUtils.deNull(rs.getString(10));

      address = addr1 + ", " + addr2 + ", " + addr3 + ", " + addr4 + ", " + addr5;

      if(cssFormat.equals("line1")) cssFormat = "line2"; else cssFormat = "line1";
      
      lastTransactionDate = lastTransactionDateSupplier(con, stmt2, rs2, companyCode);

      scoutln(out, bytesOut, "<tr id='" + cssFormat + "'><td><p>" + companyCode + "</td><td><p>" + name + "</td><td><p>" + address + "</td><td><p>" + officePhone + "</td><td><p>"
                           + fax + "</td><td><p>" + eMail + "</td><td><p>" + lastTransactionDate + "</td>");

      consolidateSupplier(con, stmt2, rs2, out, companyCode, currencies, bytesOut, currency);

      scoutln(out, bytesOut, "</tr>");
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();    
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void consolidateCustomer(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String companyCode, String currencies, int[] bytesOut, String[] currency) throws Exception
  {
    String dateFrom = "2011-01-01";
    String dateTo   = "2011-12-31";
    
    double totalInvoices2011 = generateInvoices(con, stmt, rs, dateFrom, dateTo, companyCode, currency);

    double totalCNs2011      = generateCNs(con, stmt, rs, dateFrom, dateTo, companyCode);
    
    dateFrom = "2012-01-01";
    dateTo   = "2012-12-31";
    
    double totalInvoices2012 = generateInvoices(con, stmt, rs, dateFrom, dateTo, companyCode, currency);

    double totalCNs2012      = generateCNs(con, stmt, rs, dateFrom, dateTo, companyCode);
    
    dateFrom = "2013-01-01";
    dateTo   = "2013-12-31";
    
    double totalInvoices2013 = generateInvoices(con, stmt, rs, dateFrom, dateTo, companyCode, currency);

    double totalCNs2013      = generateCNs(con, stmt, rs, dateFrom, dateTo, companyCode);
    
    outputTotals(out, totalInvoices2011, totalCNs2011, totalInvoices2012, totalCNs2012, totalInvoices2013, totalCNs2013, currency[0], currencies, bytesOut);
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void consolidateSupplier(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String companyCode, String currencies, int[] bytesOut, String[] currency) throws Exception
  {
    String dateFrom = "2011-01-01";
    String dateTo   = "2011-12-31";
    
    double totalInvoices2011 = generatePurchaseInvoices(con, stmt, rs, dateFrom, dateTo, companyCode, currency);

    double totalCNs2011      = generatePCNs(con, stmt, rs, dateFrom, dateTo, companyCode);
    
    dateFrom = "2012-01-01";
    dateTo   = "2012-12-31";
    
    double totalInvoices2012 = generatePurchaseInvoices(con, stmt, rs, dateFrom, dateTo, companyCode, currency);

    double totalCNs2012      = generatePCNs(con, stmt, rs, dateFrom, dateTo, companyCode);
    
    dateFrom = "2013-01-01";
    dateTo   = "2013-12-31";
    
    double totalInvoices2013 = generatePurchaseInvoices(con, stmt, rs, dateFrom, dateTo, companyCode, currency);

    double totalCNs2013      = generatePCNs(con, stmt, rs, dateFrom, dateTo, companyCode);
    
    outputTotals(out, totalInvoices2011, totalCNs2011, totalInvoices2012, totalCNs2012, totalInvoices2013, totalCNs2013, currency[0], currencies, bytesOut);
  }    
    
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }
  
  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private double generateInvoices(Connection con, Statement stmt, ResultSet rs, String dateFrom, String dateTo, String companyCode, String[] currency) throws Exception
  {
    double total = 0.0;
    
    stmt = con.createStatement();
  
    rs = stmt.executeQuery("SELECT TotalTotal, Currency FROM invoice WHERE Status != 'C' AND Date >= {d '" + dateFrom + "'} AND Date <= {d '" + dateTo + "'} AND CompanyCode = '" + companyCode + "'");

    String totalTotal;
   
    while(rs.next())                  
    {
      totalTotal     = rs.getString(1);
      currency[0]    = rs.getString(2);

      total += generalUtils.doubleFromStr(generalUtils.doubleDPs(totalTotal, '2'));
    }
  
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return total;
  }
  
  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private double generateCNs(Connection con, Statement stmt, ResultSet rs, String dateFrom, String dateTo, String companyCode) throws Exception
  {
    double total = 0.0;
    
    stmt = con.createStatement();
  
    rs = stmt.executeQuery("SELECT TotalTotal FROM credit WHERE Status != 'C' AND Date >= {d '" + dateFrom + "'} AND Date <= {d '" + dateTo + "'} AND CompanyCode = '" + companyCode + "'");

    String totalTotal;
   
    while(rs.next())                  
    {
      totalTotal = rs.getString(1);

      total += generalUtils.doubleFromStr(generalUtils.doubleDPs(totalTotal, '2'));
    }
  
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return total;
  }
  
  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private double generatePurchaseInvoices(Connection con, Statement stmt, ResultSet rs, String dateFrom, String dateTo, String companyCode, String[] currency) throws Exception
  {
    double total = 0.0;
    
    stmt = con.createStatement();
  
    rs = stmt.executeQuery("SELECT TotalTotal, Currency FROM pinvoice WHERE Status != 'C' AND Date >= {d '" + dateFrom + "'} AND Date <= {d '" + dateTo + "'} AND CompanyCode = '" + companyCode + "'");

    String totalTotal;
   
    while(rs.next())                  
    {
      totalTotal  = rs.getString(1);
      currency[0] = rs.getString(2);

      total += generalUtils.doubleFromStr(generalUtils.doubleDPs(totalTotal, '2'));
    }
  
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return total;
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private double generatePCNs(Connection con, Statement stmt, ResultSet rs, String dateFrom, String dateTo, String companyCode) throws Exception
  {
    double total = 0.0;
    
    stmt = con.createStatement();
  
    rs = stmt.executeQuery("SELECT TotalTotal FROM pcredit WHERE Status != 'C' AND Date >= {d '" + dateFrom + "'} AND Date <= {d '" + dateTo + "'} AND CompanyCode = '" + companyCode + "'");

    String totalTotal;
   
    while(rs.next())                  
    {
      totalTotal = rs.getString(1);

      total += generalUtils.doubleFromStr(generalUtils.doubleDPs(totalTotal, '2'));
    }
  
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return total;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private String customerCurrencies(Connection con, Statement stmt, ResultSet rs, PrintWriter out, int[] bytesOut) throws Exception
  {
    String currencies = "";

    stmt = con.createStatement();
 
    rs = stmt.executeQuery("SELECT DISTINCT Currency FROM invoice ORDER BY Currency");
    
    String curr;
    
    while(rs.next())
    {  
      curr = rs.getString(1);
      
      scoutln(out, bytesOut, "<td><p>2011 Sales " + curr + "</td><td><p>2011 Credit Note " + curr + "</td>");
      scoutln(out, bytesOut, "<td><p>2012 Sales " + curr + "</td><td><p>2012 Credit Note " + curr + "</td>");
      scoutln(out, bytesOut, "<td><p>2013 Sales " + curr + "</td><td><p>2013 Credit Note " + curr + "</td>");

      currencies += (curr + "\001");
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return currencies;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private String supplierCurrencies(Connection con, Statement stmt, ResultSet rs, PrintWriter out, int[] bytesOut) throws Exception
  {
    String currencies = "";

    stmt = con.createStatement();
 
    rs = stmt.executeQuery("SELECT DISTINCT Currency FROM pinvoice ORDER BY Currency");
    
    String curr;
    
    while(rs.next())
    {  
      curr = rs.getString(1);
      
      scoutln(out, bytesOut, "<td><p>2011 Purchases " + curr + "</td><td><p>2011 Credit Note " + curr + "</td>");
      scoutln(out, bytesOut, "<td><p>2012 Purchases " + curr + "</td><td><p>2012 Credit Note " + curr + "</td>");
      scoutln(out, bytesOut, "<td><p>2013 Purchases " + curr + "</td><td><p>2013 Credit Note " + curr + "</td>");

      currencies += (curr + "\001");
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return currencies;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void outputTotals(PrintWriter out, double totalInvoices2011, double totalCNs2011, double totalInvoices2012, double totalCNs2012, double totalInvoices2013, double totalCNs2013, String currency, String currencies, int[] bytesOut)
                            throws Exception
  {
    int posn = 0, x = 0, len = currencies.length();
    boolean found = false;
    String thisCurr;
    
    while(x < len && ! found)
    {
      thisCurr = "";
      while(x < len && currencies.charAt(x) != '\001')
        thisCurr += currencies.charAt(x++);
      ++x;
      
      if(currency.equals(thisCurr))
        found = true;
      else ++posn;
    }

    for(x=0;x<posn;++x)
      scoutln(out, bytesOut, "<td></td><td></td><td></td><td></td><td></td><td></td>");

    if(totalInvoices2011 == 0.0) scoutln(out, bytesOut, "<td></td>"); else scoutln(out, bytesOut, "<td align='right'><p>" + generalUtils.formatNumeric(totalInvoices2011, '2') + "</td>");
    if(totalCNs2011      == 0.0) scoutln(out, bytesOut, "<td></td>"); else scoutln(out, bytesOut, "<td align='right'><p>" + generalUtils.formatNumeric(totalCNs2011, '2') + "</td>");
    if(totalInvoices2012 == 0.0) scoutln(out, bytesOut, "<td></td>"); else scoutln(out, bytesOut, "<td align='right'><p>" + generalUtils.formatNumeric(totalInvoices2012, '2') + "</td>");
    if(totalCNs2012      == 0.0) scoutln(out, bytesOut, "<td></td>"); else scoutln(out, bytesOut, "<td align='right'><p>" + generalUtils.formatNumeric(totalCNs2012, '2') + "</td>");
    if(totalInvoices2013 == 0.0) scoutln(out, bytesOut, "<td></td>"); else scoutln(out, bytesOut, "<td align='right'><p>" + generalUtils.formatNumeric(totalInvoices2013, '2') + "</td>");
    if(totalCNs2013      == 0.0) scoutln(out, bytesOut, "<td></td>"); else scoutln(out, bytesOut, "<td align='right'><p>" + generalUtils.formatNumeric(totalCNs2013, '2') + "</td>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private String lastTransactionDateCustomer(Connection con, Statement stmt, ResultSet rs, String companyCode) throws Exception
  {
    String lastTransactionDate = "";

    stmt = con.createStatement();
 
    stmt.setMaxRows(1);
    
    rs = stmt.executeQuery("SELECT Date FROM invoice WHERE CompanyCode = '" + companyCode + "' ORDER BY Date DESC");
    
    String invoiceDate = "1970-01-01";
    
    if(rs.next())
      invoiceDate = rs.getString(1);
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    stmt = con.createStatement();
 
    stmt.setMaxRows(1);
    
    rs = stmt.executeQuery("SELECT Date FROM credit WHERE CompanyCode = '" + companyCode + "' ORDER BY Date DESC");
    
    String cnDate = "1970-01-01";
    
    if(rs.next())
      cnDate = rs.getString(1);
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    if(generalUtils.encodeFromYYYYMMDD(invoiceDate) > generalUtils.encodeFromYYYYMMDD(cnDate))
      lastTransactionDate = invoiceDate;
    else lastTransactionDate = cnDate;
    
    if(lastTransactionDate.equals("1970-01-01")) lastTransactionDate = "";

    return lastTransactionDate;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private String lastTransactionDateSupplier(Connection con, Statement stmt, ResultSet rs, String companyCode) throws Exception
  {
    String lastTransactionDate = "";

    stmt = con.createStatement();
 
    stmt.setMaxRows(1);
    
    rs = stmt.executeQuery("SELECT Date FROM pinvoice WHERE CompanyCode = '" + companyCode + "' ORDER BY Date DESC");
    
    String invoiceDate = "1970-01-01";
    
    if(rs.next())
      invoiceDate = rs.getString(1);
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    stmt = con.createStatement();
 
    stmt.setMaxRows(1);
    
    rs = stmt.executeQuery("SELECT Date FROM pcredit WHERE CompanyCode = '" + companyCode + "' ORDER BY Date DESC");
    
    String cnDate = "1970-01-01";
    
    if(rs.next())
      cnDate = rs.getString(1);
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    if(generalUtils.encodeFromYYYYMMDD(invoiceDate) > generalUtils.encodeFromYYYYMMDD(cnDate))
      lastTransactionDate = invoiceDate;
    else lastTransactionDate = cnDate;

    if(lastTransactionDate.equals("1970-01-01")) lastTransactionDate = "";
    
    return lastTransactionDate;
  }

}
