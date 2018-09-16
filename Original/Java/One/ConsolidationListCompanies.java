// =============================================================================================================================================================
// System: ZaraStar Consolidation: List companies
// Module: ConsolidationListCompanies.java
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

public class ConsolidationListCompanies extends HttpServlet
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
      p1  = req.getParameter("p1"); // Customer or Supplier or Org
      p2  = req.getParameter("p2"); // customer or supplier code (optional)
      
      if(p1 == null) p1 = "";
      if(p2 == null) p2 = "";
      
      doIt(out, req, p1, p2, unm, sid, uty, men, den, dnm, bnm, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "ConsolidationListCompanies", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6901, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String p1, String p2, String unm, String sid, String uty, String men, String den, String dnm,
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
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "ConsolidationListCompanies", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6901, bytesOut[0], 0, "SID:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    process(con, stmt, stmt2, rs, rs2, out, req, p1, p2, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 6901, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void process(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, HttpServletRequest req, String option,
                       String code, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir,
                       String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Detail</title>");

    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    scoutln(out, bytesOut, "<script javascript=\"JavaScript\">");

    scoutln(out, bytesOut, "function listquote(customerCode){var code=sanitise(customerCode);");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/QuotationListing?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men
                          + "&den=" + den + "&dnm=" + dnm + "&p1=3&p2=F&p3=\"+code+\"&p4=&p5=&p6=0&p7=0&p8=D&p9=&p10=&p11=\"+code+\"&bnm=" + bnm
                          + "\";}");

    scoutln(out, bytesOut, "function listso(customerCode){var code=sanitise(customerCode);");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/SalesOrderListing?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men
                          + "&den=" + den + "&dnm=" + dnm + "&p1=3&p2=F&p3=\"+code+\"&p4=&p5=&p6=0&p7=0&p8=D&p9=&p10=&p11=\"+code+\"&bnm=" + bnm
                          + "\";}");

    scoutln(out, bytesOut, "function listpl(customerCode){var code=sanitise(customerCode);");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/PickingListListing?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men
                          + "&den=" + den + "&dnm=" + dnm + "&p1=3&p2=F&p3=\"+code+\"&p4=&p5=&p6=0&p7=0&p8=D&p9=&p10=&p11=\"+code+\"&bnm=" + bnm
                          + "\";}");

    scoutln(out, bytesOut, "function listinvoice(customerCode){var code=sanitise(customerCode);");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/SalesInvoiceListing?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men
                          + "&den=" + den + "&dnm=" + dnm + "&p1=3&p2=F&p3=\"+code+\"&p4=&p5=&p6=0&p7=0&p8=D&p9=&p10=&p11=\"+code+\"&bnm=" + bnm
                          + "\";}");

    scoutln(out, bytesOut, "function listdo(customerCode){var code=sanitise(customerCode);");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/DeliveryOrderListing?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men
                          + "&den=" + den + "&dnm=" + dnm + "&p1=3&p2=F&p3=\"+code+\"&p4=&p5=&p6=0&p7=0&p8=D&p9=&p10=&p11=\"+code+\"&bnm=" + bnm
                          + "\";}");

    scoutln(out, bytesOut, "function listproforma(customerCode){var code=sanitise(customerCode);");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/ProformaInvoiceListing?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men
                          + "&den=" + den + "&dnm=" + dnm + "&p1=3&p2=F&p3=\"+code+\"&p4=&p5=&p6=0&p7=0&p8=D&p9=&p10=&p11=\"+code+\"&bnm=" + bnm
                          + "\";}");

    scoutln(out, bytesOut, "function listoc(customerCode){var code=sanitise(customerCode);");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/OrderConfirmationListing?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men
                          + "&den=" + den + "&dnm=" + dnm + "&p1=3&p2=F&p3=\"+code+\"&p4=&p5=&p6=0&p7=0&p8=D&p9=&p10=&p11=\"+code+\"&bnm=" + bnm
                          + "\";}");

    scoutln(out, bytesOut, "function listcredit(customerCode){var code=sanitise(customerCode);");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/SalesCreditNoteListing?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men
                          + "&den=" + den + "&dnm=" + dnm + "&p1=3&p2=F&p3=\"+code+\"&p4=&p5=&p6=0&p7=0&p8=D&p9=&p10=&p11=\"+code+\"&bnm=" + bnm
                          + "\";}");

    scoutln(out, bytesOut, "function listdebit(customerCode){var code=sanitise(customerCode);");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/SalesDebitNoteListing?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men
                          + "&den=" + den + "&dnm=" + dnm + "&p1=3&p2=F&p3=\"+code+\"&p4=&p5=&p6=0&p7=0&p8=D&p9=&p10=&p11=\"+code+\"&bnm=" + bnm
                          + "\";}");

    scoutln(out, bytesOut, "function listpcredit(customerCode){var code=sanitise(customerCode);");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/PurchaseCreditNoteListing?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men
                          + "&den=" + den + "&dnm=" + dnm + "&p1=3&p2=F&p3=\"+code+\"&p4=&p5=&p6=0&p7=0&p8=D&p9=&p10=&p11=\"+code+\"&bnm=" + bnm
                          + "\";}");

    scoutln(out, bytesOut, "function listpdebit(customerCode){var code=sanitise(customerCode);");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/PurchaseDebitNoteListing?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men
                          + "&den=" + den + "&dnm=" + dnm + "&p1=3&p2=F&p3=\"+code+\"&p4=&p5=&p6=0&p7=0&p8=D&p9=&p10=&p11=\"+code+\"&bnm=" + bnm
                          + "\";}");

    scoutln(out, bytesOut, "function listpo(customerCode){var code=sanitise(customerCode);");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/PurchaseOrderListing?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men
                          + "&den=" + den + "&dnm=" + dnm + "&p1=3&p2=F&p3=\"+code+\"&p4=&p5=&p6=0&p7=0&p8=D&p9=&p10=&p11=\"+code+\"&bnm=" + bnm
                          + "\";}");

    scoutln(out, bytesOut, "function listlp(customerCode){var code=sanitise(customerCode);");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/LocalPurchaseListing?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men
                          + "&den=" + den + "&dnm=" + dnm + "&p1=3&p2=F&p3=\"+code+\"&p4=&p5=&p6=0&p7=0&p8=D&p9=&p10=&p11=\"+code+\"&bnm=" + bnm
                          + "\";}");

    scoutln(out, bytesOut, "function listgr(customerCode){var code=sanitise(customerCode);");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/GoodsReceivedListing?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men
                          + "&den=" + den + "&dnm=" + dnm + "&p1=3&p2=F&p3=\"+code+\"&p4=&p5=&p6=0&p7=0&p8=D&p9=&p10=&p11=\"+code+\"&bnm=" + bnm
                          + "\";}");

    scoutln(out, bytesOut, "function listpinvoice(customerCode){var code=sanitise(customerCode);");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/PurchaseInvoiceListing?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men
                          + "&den=" + den + "&dnm=" + dnm + "&p1=3&p2=F&p3=\"+code+\"&p4=&p5=&p6=0&p7=0&p8=D&p9=&p10=&p11=\"+code+\"&bnm=" + bnm
                          + "\";}");

    scoutln(out, bytesOut, "function listMail(companyCode,companyType){var code=sanitise(companyCode);");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/MailZaraViewSearchLibrary?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men
                          + "&den=" + den + "&dnm=" + dnm + "&p1=&p2=&p3=&p4=&p5=&p6=&p7=\"+code+\"&p8=\"+companyType+\"&p9=&p10=&p11=&p12=&p13=A&bnm=" + bnm
                          + "\";}");

    scoutln(out, bytesOut, "function listFaxes(companyCode,companyType){var code=sanitise(companyCode);");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/FaxSearch?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den
                         + "&dnm=" + dnm + "&p1=&p2=&p3=&p4=&p5=&p6&p7&p8=&p9=\"+code+\"&p10=\"+companyType+\"&p11=A&bnm=" + bnm + "\";}");

    scoutln(out, bytesOut, "function sanitise(code){");
    scoutln(out, bytesOut, "var code2='';var x;var len=code.length;");
    scoutln(out, bytesOut, "for(x=0;x<len;++x)");
    scoutln(out, bytesOut, "if(code.charAt(x)=='#')code2+='%23';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='\"')code2+='%22';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='&')code2+='%26';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='%')code2+='%25';");
    scoutln(out, bytesOut, "else if(code.charAt(x)==' ')code2+='%20';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='+')code2+='%2b';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='?')code2+='%3F';");
    scoutln(out, bytesOut, "else code2+=code.charAt(x);");
    scoutln(out, bytesOut, "return code2};");
    
    scoutln(out, bytesOut, "</script>");

    int[] hmenuCount = new int[1];
    
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "182", "", "DataAnalytics", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
     
    String s;
    if(code.length() == 0)
    {
      if(option.equals("C"))
        s = "Customers";
      else
      if(option.equals("S"))
        s = "Suppliers";
    }
    else s = code;
    
    if(option.equals("C"))
      pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Customer Detail", "6901", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);
    else pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Supplier Detail", "6901", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<form>");

    scoutln(out, bytesOut, "<table id='page' cellspacing=0 cellpadding=2 width=100%>");

    scoutln(out, bytesOut, "<tr><td nowrap><p>Company Code</td><td nowrap><p>Company Name</td><td nowrap><p>2011 Sales</td><td nowrap><p>2012 Sales</td><td nowrap><p>2013 Sales</td><td nowrap><p>2014 Sales</td></tr>");

    if(option.equals("C"))
      customers(con, stmt, stmt2, rs, rs2, out, code, dnm, localDefnsDir, defnsDir, bytesOut);
    else
    if(option.equals("S"))
      suppliers(con, stmt, stmt2, rs, rs2, out, code, dnm, localDefnsDir, defnsDir, bytesOut);
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    
    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir));
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void customers(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, String code, String dnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    String companyCode = "", name, cssFormat = "line1";

    stmt = con.createStatement();
 
    if(code.length() > 0)
    {
      rs = stmt.executeQuery("SELECT Name FROM company WHERE CompanyCode = '" + code + "'");
      companyCode = code;
    }
    else rs = stmt.executeQuery("SELECT CompanyCode, Name FROM company ORDER BY Name");
    
    boolean[] first = new boolean[1];
    double[] CatalogLinkedDefinition = new double[1];  CatalogLinkedDefinition[0] = 0.0;
    double[] TrackTraceMainExternal = new double[1];  TrackTraceMainExternal[0] = 0.0;
    double[] ProductSmartWordPhraseSearch = new double[1];  ProductSmartWordPhraseSearch[0] = 0.0;
    double[] CatalogBuyers = new double[1];  CatalogBuyers[0] = 0.0;
    String[] currency = new String[1];  currency[0] = "";
    
    while(rs.next())
    {  
      if(code.length() > 0)
        name = rs.getString(1);
      else
      {
        companyCode = rs.getString(1);
        name        = rs.getString(2);
      }
      
      first[0] = true;

      if(cssFormat.equals("line1")) cssFormat = "line2"; else cssFormat = "line1";
      
      scoutln(out, bytesOut, "<tr id='" + cssFormat + "'><td><p>" + companyCode + "</td><td><p>" + name + "</td></tr><tr id='" + cssFormat + "'><td><p>");

      CatalogLinkedDefinition[0] = TrackTraceMainExternal[0] = ProductSmartWordPhraseSearch[0] = CatalogBuyers[0] = 0.0;
      
      consolidateCustomer(con, stmt2, rs2, out, companyCode, dnm, localDefnsDir, defnsDir, first, bytesOut, CatalogLinkedDefinition, TrackTraceMainExternal, ProductSmartWordPhraseSearch, CatalogBuyers, currency);

      scoutln(out, bytesOut, "</td></tr>");

      scoutln(out, bytesOut, "<tr id='" + cssFormat + "'><td></td><td align='right'>" + currency[0] + "</td><td align='right'><p>" + generalUtils.formatNumeric(CatalogLinkedDefinition[0], '2') + "</td>");
      scoutln(out, bytesOut, "<td align='right'><p>" + generalUtils.formatNumeric(TrackTraceMainExternal[0], '2') + "</td>");
      scoutln(out, bytesOut, "<td align='right'><p>" + generalUtils.formatNumeric(ProductSmartWordPhraseSearch[0], '2') + "</td>");
      scoutln(out, bytesOut, "<td align='right'><p>" + generalUtils.formatNumeric(CatalogBuyers[0], '2') + "</td></tr>");
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();    
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void suppliers(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, String code, String dnm, String localDefnsDir, String defnsDir,
                         int[] bytesOut) throws Exception
  {
    String companyCode = "", name, cssFormat = "line1";

    stmt = con.createStatement();

    if(code.length() > 0)
    {
      rs = stmt.executeQuery("SELECT Name FROM supplier WHERE SupplierCode = '" + companyCode + "'");
      companyCode = code;
    }
    else rs = stmt.executeQuery("SELECT SupplierCode, Name FROM supplier ORDER BY Name");
    
    boolean[] first = new boolean[1];
    double[] CatalogLinkedDefinition = new double[1];  CatalogLinkedDefinition[0] = 0.0;
    double[] TrackTraceMainExternal = new double[1];  TrackTraceMainExternal[0] = 0.0;
    double[] ProductSmartWordPhraseSearch = new double[1];  ProductSmartWordPhraseSearch[0] = 0.0;
    double[] CatalogBuyers = new double[1];  CatalogBuyers[0] = 0.0;
    String[] currency = new String[1];  currency[0] = "";
    
    while(rs.next())
    {  
      if(code.length() > 0)
        name = rs.getString(1);
      else
      {
        companyCode = rs.getString(1);
        name        = rs.getString(2);
      }
      
      first[0] = true;
      
      if(cssFormat.equals("line1")) cssFormat = "line2"; else cssFormat = "line1";

      scoutln(out, bytesOut, "<tr id='" + cssFormat + "'><td><p>" + companyCode + "</td><td><p>" + name + "</td></tr><tr id='" + cssFormat + "'><td><p>");

      CatalogLinkedDefinition[0] = TrackTraceMainExternal[0] = ProductSmartWordPhraseSearch[0] = CatalogBuyers[0] = 0.0;
      
      consolidateSupplier(con, stmt2, rs2, out, companyCode, dnm, localDefnsDir, defnsDir, first, bytesOut, CatalogLinkedDefinition, TrackTraceMainExternal, ProductSmartWordPhraseSearch, CatalogBuyers, currency);
      
      scoutln(out, bytesOut, "</td></tr>");

      scoutln(out, bytesOut, "<tr id='" + cssFormat + "'><td></td><td align='right'>" + currency[0] + "</td><td align='right'><p>" + generalUtils.formatNumeric(CatalogLinkedDefinition[0], '2') + "</td>");
      scoutln(out, bytesOut, "<td align='right'><p>" + generalUtils.formatNumeric(TrackTraceMainExternal[0], '2') + "</td>");
      scoutln(out, bytesOut, "<td align='right'><p>" + generalUtils.formatNumeric(ProductSmartWordPhraseSearch[0], '2') + "</td>");
      scoutln(out, bytesOut, "<td align='right'><p>" + generalUtils.formatNumeric(CatalogBuyers[0], '2') + "</td></tr>");
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();    
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void consolidateCustomer(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String companyCode, String dnm, String localDefnsDir, String defnsDir, boolean[] first, int[] bytesOut, double[] CatalogLinkedDefinition,
                                   double[] TrackTraceMainExternal, double[] ProductSmartWordPhraseSearch, double[] CatalogBuyers, String[] currency) throws Exception
  {
    listFaxes(out, companyCode, "C", dnm, localDefnsDir, defnsDir, first, bytesOut);
        
    listDocs(con, stmt, rs, out, companyCode, "quotation", "quote", first, bytesOut);

    listDocs(con, stmt, rs, out, companyCode, "sales order", "so", first, bytesOut);

    listDocs(con, stmt, rs, out, companyCode, "works order", "wo", first, bytesOut);

    listDocs(con, stmt, rs, out, companyCode, "picking list", "pl", first, bytesOut);

    listDocs(con, stmt, rs, out, companyCode, "delivery order", "do", first, bytesOut);

    listDocs(con, stmt, rs, out, companyCode, "invoice", "invoice", first, bytesOut);

    listDocs(con, stmt, rs, out, companyCode, "credit note", "credit", first, bytesOut);

    listDocs(con, stmt, rs, out, companyCode, "debit note", "debit", first, bytesOut);

    listDocs(con, stmt, rs, out, companyCode, "proforma invoice", "proforma", first, bytesOut);

    listDocs(con, stmt, rs, out, companyCode, "order confirmation", "oc", first, bytesOut);

    listDocs(con, stmt, rs, out, companyCode, "order acknowledgement", "oa", first, bytesOut);
  
    String dateFrom = "2011-01-01";
    String dateTo   = "2011-12-31";
    
    double total = generateInvoices(con, stmt, rs, dateFrom, dateTo, companyCode, CatalogLinkedDefinition, currency);
    
    scoutln(out, bytesOut, "<td align='right'><p>SGD</td><td align='right'><p>" + generalUtils.formatNumeric(total, '2') + "</td>");

    dateFrom = "2012-01-01";
    dateTo   = "2012-12-31";
    
    total = generateInvoices(con, stmt, rs, dateFrom, dateTo, companyCode, TrackTraceMainExternal, currency);
    
    scoutln(out, bytesOut, "<td align='right'><p>" + generalUtils.formatNumeric(total, '2') + "</td>");
    
    dateFrom = "2013-01-01";
    dateTo   = "2013-12-31";
    
    total = generateInvoices(con, stmt, rs, dateFrom, dateTo, companyCode, ProductSmartWordPhraseSearch, currency);
    
    scoutln(out, bytesOut, "<td align='right'><p>" + generalUtils.formatNumeric(total, '2') + "</td>");
    
    dateFrom = "2014-01-01";
    dateTo   = "2014-12-31";
    
    total = generateInvoices(con, stmt, rs, dateFrom, dateTo, companyCode, CatalogBuyers, currency);
    
    scoutln(out, bytesOut, "<td align='right'><p>" + generalUtils.formatNumeric(total, '2') + "</td>");
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void consolidateSupplier(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String companyCode, String dnm, String localDefnsDir, String defnsDir, boolean[] first, int[] bytesOut, double[] CatalogLinkedDefinition,
                                   double[] TrackTraceMainExternal, double[] ProductSmartWordPhraseSearch, double[] CatalogBuyers, String[] currency) throws Exception
  {
    listFaxes(out, companyCode, "S", dnm, localDefnsDir, defnsDir, first, bytesOut);
        
    listDocs(con, stmt, rs, out, companyCode, "purchase order", "po", first, bytesOut);

    listDocs(con, stmt, rs, out, companyCode, "local requisition", "lp", first, bytesOut);

    listDocs(con, stmt, rs, out, companyCode, "goods received note", "gr", first, bytesOut);

    listDocs(con, stmt, rs, out, companyCode, "purchase invoice", "pinvoice", first, bytesOut);

    listDocs(con, stmt, rs, out, companyCode, "purchase credit note", "pcredit", first, bytesOut);

    listDocs(con, stmt, rs, out, companyCode, "purchase debit note", "pdebit", first, bytesOut);

    String dateFrom = "2011-01-01";
    String dateTo   = "2011-12-31";
    
    double total = generatePurchaseInvoices(con, stmt, rs, dateFrom, dateTo, companyCode, CatalogLinkedDefinition, currency);
    
    scoutln(out, bytesOut, "<td align='right'><p>SGD</td><td align='right'><p>" + generalUtils.formatNumeric(total, '2') + "</td>");

    dateFrom = "2012-01-01";
    dateTo   = "2012-12-31";
    
    total = generatePurchaseInvoices(con, stmt, rs, dateFrom, dateTo, companyCode, TrackTraceMainExternal, currency);
    
    scoutln(out, bytesOut, "<td align='right'><p>" + generalUtils.formatNumeric(total, '2') + "</td>");
    
    dateFrom = "2013-01-01";
    dateTo   = "2013-12-31";
    
    total = generatePurchaseInvoices(con, stmt, rs, dateFrom, dateTo, companyCode, ProductSmartWordPhraseSearch, currency);
    
    scoutln(out, bytesOut, "<td align='right'><p>" + generalUtils.formatNumeric(total, '2') + "</td>");
    
    dateFrom = "2014-01-01";
    dateTo   = "2014-12-31";
    
    total = generatePurchaseInvoices(con, stmt, rs, dateFrom, dateTo, companyCode, CatalogBuyers, currency);
    
    scoutln(out, bytesOut, "<td align='right'><p>" + generalUtils.formatNumeric(total, '2') + "</td>");
  }    
    
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void listDocs(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String companyCode, String docName, String tableName, boolean[] first,
                        int[] bytesOut) throws Exception
  {
    if(companyCode.length() == 0) // just-in-case
      return;

    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM " + tableName + " WHERE CompanyCode= '" + companyCode + "'");

    int numRecs;
    if(! rs.next())
      numRecs = 0;
    else numRecs = rs.getInt("rowcount");
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    if(numRecs > 0)
    {  
      if(! first[0])
        scout(out, bytesOut, ", ");
      else first[0] = false;
        
      scoutln(out, bytesOut, "<a href=\"javascript:list" + tableName + "('" + companyCode + "')\">");
          
      scout(out, bytesOut, numRecs + " " + docName);
      if(numRecs != 1)
        scout(out, bytesOut, "s");

      scout(out, bytesOut, "</a>");
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void listFaxes(PrintWriter out, String companyCode, String companyType, String dnm, String localDefnsDir, String defnsDir, boolean[] first,
                         int[] bytesOut) throws Exception
  {
    Connection con = null;
    Statement stmt = null;
    ResultSet rs   = null;

    try
    {
      if(companyCode.length() == 0) // just-in-case
        return;

      String userName = directoryUtils.getMySQLUserName();
      String passWord = directoryUtils.getMySQLPassWord();
    
      Class.forName("com.mysql.jdbc.Driver").newInstance();
    
      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_fax?user=" + userName + "&password=" + passWord);
      stmt = con.createStatement();
    
      rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM faxed WHERE CompanyCode = '" + companyCode + "' AND CompanyType = '" + companyType + "'");

      int numRecs;
      if(! rs.next())
        numRecs = 0;
      else numRecs = rs.getInt("rowcount");
    
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
      if(con  != null) con.close();

      if(numRecs > 0)
      {  
        if(! first[0])
          scout(out, bytesOut, ", ");
        else first[0] = false;

        scoutln(out, bytesOut, "<a href=\"javascript:listFaxes('" + companyCode + "','" + companyType + "')\">");

        scout(out, bytesOut, numRecs + " fax"); 
        if(numRecs > 1)
          scout(out, bytesOut, "es");
      }      
    }  
    catch(Exception e)
    {
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }
  private void scout(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.print(str);
    bytesOut[0] += str.length();    
  }
  
  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private double generateInvoices(Connection con, Statement stmt, ResultSet rs, String dateFrom, String dateTo, String companyCode, double[] totalIssue, String[] currency) throws Exception
  {
    double total = 0.0;
    
    stmt = con.createStatement();
  
    rs = stmt.executeQuery("SELECT BaseTotalTotal, TotalTotal, Currency FROM invoice WHERE Status != 'C' AND Date >= {d '" + dateFrom + "'} AND Date <= {d '" + dateTo + "'} AND CompanyCode = '" + companyCode + "'");

    String baseTotalTotal, totalTotal;
   
    while(rs.next())                  
    {
      baseTotalTotal = rs.getString(1);
      totalTotal     = rs.getString(2);
      currency[0]    = rs.getString(3);

      total += generalUtils.doubleFromStr(generalUtils.doubleDPs(baseTotalTotal, '2'));
      totalIssue[0] += generalUtils.doubleFromStr(generalUtils.doubleDPs(totalTotal, '2'));
    }
  
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return total;
  }
  
  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private double generatePurchaseInvoices(Connection con, Statement stmt, ResultSet rs, String dateFrom, String dateTo, String companyCode, double[] totalIssue, String[] currency) throws Exception
  {
    double total = 0.0;
    
    stmt = con.createStatement();
  
    rs = stmt.executeQuery("SELECT BaseTotalTotal, TotalTotal, Currency FROM pinvoice WHERE Status != 'C' AND Date >= {d '" + dateFrom + "'} AND Date <= {d '" + dateTo + "'} AND CompanyCode = '" + companyCode + "'");

    String baseTotalTotal, totalTotal;
   
    while(rs.next())                  
    {
      baseTotalTotal = rs.getString(1);
      totalTotal     = rs.getString(2);
      currency[0]    = rs.getString(3);

      total += generalUtils.doubleFromStr(generalUtils.doubleDPs(baseTotalTotal, '2'));
      totalIssue[0] += generalUtils.doubleFromStr(generalUtils.doubleDPs(totalTotal, '2'));
    }
  
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return total;
  }

}
