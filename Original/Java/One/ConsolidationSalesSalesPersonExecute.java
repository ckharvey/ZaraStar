// =============================================================================================================================================================
// System: ZaraStar Consolidation: List companies
// Module: ConsolidationSalesSalesPersonExecute.java
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

public class ConsolidationSalesSalesPersonExecute extends HttpServlet
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

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", salesPerson="";

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
      
      doIt(out, req, salesPerson, unm, sid, uty, men, den, dnm, bnm, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "ConsolidationSalesSalesPerson", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6904, bytesOut[0], 0, "ERR:" + salesPerson);
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String salesPerson, String unm, String sid, String uty, String men, String den, String dnm,
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
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "ConsolidationSalesSalesPerson", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6904, bytesOut[0], 0, "SID:" + salesPerson);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    process(con, stmt, stmt2, rs, rs2, out, req, salesPerson, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 6904, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), salesPerson);
    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void process(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, HttpServletRequest req, String salesPerson,
                       String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir,
                       String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Detail</title>");

    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    int[] hmenuCount = new int[1];
    
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "6904", "", "ConsolidationSalesSalesPerson", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
    
    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "SalesPerson Detail", "6904", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<form>");

    scoutln(out, bytesOut, "<table id='page' cellspacing=0 cellpadding=2 width=100%>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap colspan='4'><p>SalesPerson: " + salesPerson + "</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap><p>Customer Code</td><td nowrap><p>Customer Name</td><td nowrap align=right><p>2014</td><td nowrap align=right><p>2013</td><td nowrap align=right><p>2012</td></tr>");

    int numEntries = 3000; // plenty
    
    String[] customerCodes = new String[numEntries];
    double[] CatalogBuyers         = new double[numEntries];
    double[] ProductSmartWordPhraseSearch         = new double[numEntries];
    double[] TrackTraceMainExternal         = new double[numEntries];

    int numCustomers = salesPersonCustomers(con, stmt, rs, salesPerson, customerCodes);
    
    for(int x=0;x<numCustomers;++x)
      consolidate(con, stmt, rs, salesPerson, customerCodes[x], x, TrackTraceMainExternal, ProductSmartWordPhraseSearch, CatalogBuyers);
    
    insertionSort(CatalogBuyers, numCustomers, customerCodes, ProductSmartWordPhraseSearch, TrackTraceMainExternal);
            
    output(con, stmt, rs, out, CatalogBuyers, numCustomers, customerCodes, ProductSmartWordPhraseSearch, TrackTraceMainExternal, bytesOut);
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    
    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir));
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void consolidate(Connection con, Statement stmt, ResultSet rs, String salesPerson, String companyCode, int entry, double[] TrackTraceMainExternal, double[] ProductSmartWordPhraseSearch, double[] CatalogBuyers) throws Exception
  {
    String dateFrom = "2012-01-01";
    String dateTo   = "2012-12-31";
    
    TrackTraceMainExternal[entry] = generateInvoices(con, stmt, rs, salesPerson, dateFrom, dateTo, companyCode);

    dateFrom = "2013-01-01";
    dateTo   = "2013-12-31";
    
    ProductSmartWordPhraseSearch[entry] = generateInvoices(con, stmt, rs, salesPerson, dateFrom, dateTo, companyCode);

    dateFrom = "2014-01-01";
    dateTo   = "2014-12-31";
    
    CatalogBuyers[entry] = generateInvoices(con, stmt, rs, salesPerson, dateFrom, dateTo, companyCode);
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }
  
  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private double generateInvoices(Connection con, Statement stmt, ResultSet rs, String salesPerson, String dateFrom, String dateTo, String companyCode) throws Exception
  {
    double total = 0.0;
    
    stmt = con.createStatement();
  
    rs = stmt.executeQuery("SELECT BaseTotalTotal FROM invoice WHERE Status != 'C' AND Date >= {d '" + dateFrom + "'} AND Date <= {d '" + dateTo + "'} AND CompanyCode = '" + companyCode + "' AND SalesPerson = '" + salesPerson + "'");

    String baseTotalTotal;
   
    while(rs.next())                  
    {
      baseTotalTotal = rs.getString(1);

      total += generalUtils.doubleFromStr(generalUtils.doubleDPs(baseTotalTotal, '2'));
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
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private int salesPersonCustomers(Connection con, Statement stmt, ResultSet rs, String salesPerson, String[] customerCodes) throws Exception
  {
    stmt = con.createStatement();
 
    rs = stmt.executeQuery("SELECT DISTINCT CompanyCode FROM invoice WHERE SalesPerson = '" + salesPerson + "' AND Date >= {d '2012-01-01'} AND Date <= {d '2014-12-31'} ");
    
    String code;
    int count = 0;
    
    while(rs.next())
    {  
      code = rs.getString(1);
      
      customerCodes[count++] = code;
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return count;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void output(Connection con, Statement stmt, ResultSet rs, PrintWriter out, double[] CatalogBuyers, int numCustomers, String[] customerCodes, double[] ProductSmartWordPhraseSearch, double[] TrackTraceMainExternal, int[] bytesOut) throws Exception
  {
    String customerName, cssFormat = "";
    double TrackTraceMainExternalTotal = 0.0,  ProductSmartWordPhraseSearchTotal = 0.0,  CatalogBuyersTotal = 0.0;
    
    for(int x=0;x<numCustomers;++x)
    {
      customerName = customer.getCompanyNameGivenCode(con, stmt, rs, customerCodes[x]);
              
      if(cssFormat.equals("line2")) cssFormat = "line1"; else cssFormat = "line2";

      scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\">");
      scoutln(out, bytesOut, "<td><p>" + customerCodes[x] + "</td><td><p>" + customerName + "</td>");    
      scoutln(out, bytesOut, "<td align='right'><p>" + generalUtils.formatNumeric(CatalogBuyers[x], '2') + "</td>");
      scoutln(out, bytesOut, "<td align='right'><p>" + generalUtils.formatNumeric(ProductSmartWordPhraseSearch[x], '2') + "</td>");
      scoutln(out, bytesOut, "<td align='right'><p>" + generalUtils.formatNumeric(TrackTraceMainExternal[x], '2') + "</td></tr>");              
      
      TrackTraceMainExternalTotal += TrackTraceMainExternal[x];
      ProductSmartWordPhraseSearchTotal += ProductSmartWordPhraseSearch[x];
      CatalogBuyersTotal += CatalogBuyers[x];
    }

    scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\">");
    scoutln(out, bytesOut, "<td></td><td></td>");    
    scoutln(out, bytesOut, "<td align='right'><p>" + generalUtils.formatNumeric(CatalogBuyersTotal, '2') + "</td>");
    scoutln(out, bytesOut, "<td align='right'><p>" + generalUtils.formatNumeric(ProductSmartWordPhraseSearchTotal, '2') + "</td>");
    scoutln(out, bytesOut, "<td align='right'><p>" + generalUtils.formatNumeric(TrackTraceMainExternalTotal, '2') + "</td></tr>");              
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void insertionSort(double[] CatalogBuyers, int numElements, String[] customerCodes, double[] ProductSmartWordPhraseSearch, double[] TrackTraceMainExternal) throws Exception
  {
    int j;
    for(int i=1;i<numElements;i++)
    {
      for(j=0;j<i;j++)
      {
        if(CatalogBuyers[i-j-1] < CatalogBuyers[i-j])
          swap(CatalogBuyers, i-j-1, i-j, customerCodes, ProductSmartWordPhraseSearch, TrackTraceMainExternal);
        else j=i;
      }
    }
  }

  private void swap(double[] CatalogBuyers, int x, int y, String[] customerCodes, double[] ProductSmartWordPhraseSearch, double[] TrackTraceMainExternal) throws Exception
  {
    double d = CatalogBuyers[x];
    CatalogBuyers[x] = CatalogBuyers[y];
    CatalogBuyers[y] = d;
    
    d = ProductSmartWordPhraseSearch[x];
    ProductSmartWordPhraseSearch[x] = ProductSmartWordPhraseSearch[y];
    ProductSmartWordPhraseSearch[y] = d;
    
    d = TrackTraceMainExternal[x];
    TrackTraceMainExternal[x] = TrackTraceMainExternal[y];
    TrackTraceMainExternal[y] = d;
    
    String customerCode = customerCodes[x];
    customerCodes[x] = customerCodes[y];
    customerCodes[y] = customerCode;
  }

}
