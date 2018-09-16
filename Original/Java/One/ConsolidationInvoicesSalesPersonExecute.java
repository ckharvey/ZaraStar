 // =============================================================================================================================================================
// System: ZaraStar Consolidation: List companies
// Module: ConsolidationInvoicesSalesPersonExecute.java
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

public class ConsolidationInvoicesSalesPersonExecute extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  Customer customer = new Customer();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", salesPerson="", dateFrom="", dateTo="";

    try
    {
      directoryUtils.setContentHeaders2(res);
      req.setCharacterEncoding("UTF-8");
      out = res.getWriter();

      unm = req.getParameter("unm");
      sid = req.getParameter("sid");
      uty = req.getParameter("uty");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");
      salesPerson = req.getParameter("salesPerson");
      dateFrom = req.getParameter("dateFrom");
      dateTo = req.getParameter("dateTo");
      
      doIt(out, req, salesPerson, dateFrom, dateTo, unm, sid, uty, men, den, dnm, bnm, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "ConsolidationInvoicesSalesPerson", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6907, bytesOut[0], 0, "ERR:" + salesPerson);
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String salesPerson, String dateFrom, String dateTo, String unm, String sid, String uty, String men, String den, String dnm,
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
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "ConsolidationInvoicesSalesPerson", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6907, bytesOut[0], 0, "SID:" + salesPerson);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    process(con, stmt, rs, out, req, salesPerson, dateFrom, dateTo, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 6907, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), salesPerson);
    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void process(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String salesPerson, String dateFrom, String dateTo,
                       String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir,
                       String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Detail</title>");

    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    int[] hmenuCount = new int[1];
    
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "6907", "", "ConsolidationInvoicesSalesPerson", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
    
    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "SalesPerson Detail", "6907", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<form>");

    scoutln(out, bytesOut, "<table id='page' cellspacing=0 cellpadding=2 width=100%>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap colspan='4'><p>SalesPerson: " + salesPerson + "</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td><p>Date From: " + dateFrom + "</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td><p>Date To: " + dateTo + "</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    dateFrom = generalUtils.convertDateToSQLFormat(dateFrom);
    dateTo   = generalUtils.convertDateToSQLFormat(dateTo);
    
    scoutln(out, bytesOut, "<tr><td nowrap><p>InvoiceCode</td><td nowrap><p>Date</td><td nowrap><p>Customer Code</td><td nowrap><p>Customer Name</td><td nowrap><p>Item Code</td><td nowrap><p>Manufacturer</td><td nowrap><p>Mfr Code</td><td nowrap><p>Description</td>"
                         + "<td nowrap><p>Unit Price</td><td nowrap><p>Quantity</td></tr>");

    this.salesPersonInvoices(con, stmt, rs, out, salesPerson, dateFrom, dateTo, bytesOut);
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    
    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir));
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private int salesPersonInvoices(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String salesPerson, String dateFrom, String dateTo, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t1.InvoiceCode, t1.CompanyCode, t1.CompanyName, t2.ItemCode, t2.Description, t2.UnitPrice, t2.Quantity, t1.Date, t2.Manufacturer, t2.ManufacturerCode FROM invoice AS t1 INNER JOIN invoicel AS t2 ON t1.InvoiceCode = t2.InvoiceCode WHERE t1.SalesPerson = '" + salesPerson + "' AND t1.Date >= {d '" + dateFrom + "'}"
                        + " AND t1.Date <= {d '" + dateTo + "'} ORDER BY t2.ItemCode");
    
    String invoiceCode, compCode, compName, itemCode, desc, unitPrice, qty, date, mfr, mfrCode;
    int count = 0;
    String[] cssFormat = new String[1];  cssFormat[0] = "";

    while(rs.next())
    {  
      invoiceCode = rs.getString(1);
      compCode    = generalUtils.deNull(rs.getString(2));
      compName    = generalUtils.deNull(rs.getString(3));
      itemCode    = generalUtils.deNull(rs.getString(4));
      desc        = generalUtils.deNull(rs.getString(5));
      unitPrice   = generalUtils.deNull(rs.getString(6));
      qty         = generalUtils.deNull(rs.getString(7));
      date        = generalUtils.deNull(rs.getString(8));
      mfr         = generalUtils.deNull(rs.getString(9));
      mfrCode     = generalUtils.deNull(rs.getString(10));

      output(out, invoiceCode, compCode, compName, itemCode, desc, unitPrice, qty, date, mfr, mfrCode, bytesOut, cssFormat);
      
      ++count;
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return count;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void output(PrintWriter out, String invoiceCode, String compCode, String compName, String itemCode, String desc, String unitPrice, String qty, String date, String mfr, String mfrCode, int[] bytesOut, String[] cssFormat) throws Exception
  {    
    if(cssFormat[0].equals("line2")) cssFormat[0] = "line1"; else cssFormat[0] = "line2";

    scoutln(out, bytesOut, "<tr id=\"" + cssFormat[0] + "\">");
    scoutln(out, bytesOut, "<td><p>" + invoiceCode + "</td>");    
    scoutln(out, bytesOut, "<td><p>" + date + "</td>");    
    scoutln(out, bytesOut, "<td><p>" + compCode + "</td>");    
    scoutln(out, bytesOut, "<td><p>" + compName + "</td>");    
    scoutln(out, bytesOut, "<td><p>" + itemCode + "</td>");    
    scoutln(out, bytesOut, "<td><p>" + mfr + "</td>");    
    scoutln(out, bytesOut, "<td><p>" + mfrCode + "</td>");    
    scoutln(out, bytesOut, "<td><p>" + desc + "</td>");    
    scoutln(out, bytesOut, "<td align='right'><p>" + generalUtils.formatNumeric(unitPrice, '2') + "</td>");
    scoutln(out, bytesOut, "<td align='right'><p>" + generalUtils.formatNumeric(qty, '0') + "</td></tr>");              
  }

}
