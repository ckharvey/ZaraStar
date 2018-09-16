// =======================================================================================================================================================================================================
// System: ZaraStar: Companies: Customer services access page
// Module: CustomerServicesWave.java
// Author: C.K.Harvey
// Copyright (c) 2002-09 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.sql.*;

public class CustomerServicesWave extends HttpServlet
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

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="";

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

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "CustomerServices", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 103, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush(); 
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 4001, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreenW(out, req, 13, unm, sid, uty, men, den, dnm, bnm, "CustomerServices", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 103, bytesOut[0], 0, "ACC:");
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreenW(out, req, 12, unm, sid, uty, men, den, dnm, bnm, "CustomerServices", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 103, bytesOut[0], 0, "SID:");
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    scoutln(out, bytesOut, "103\001Customers\001Customer Focus\001javascript:getHTML('CustomerServicesWave','')\001\001Y\001\001\003");

    set(con, stmt, rs, out, req, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 103, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut)
                   throws Exception
  {
    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\"></head>");

    scoutln(out, bytesOut, "<form>");

    pageFrameUtils.drawTitleW(out, false, false, "", "", "", "", "", "", "Customer Services", "103", unm, sid, uty, men, den, dnm, bnm, bytesOut);

    scoutln(out, bytesOut, "<table id='page' width=100% border=0>");

    scoutln(out, bytesOut, "<tr><td nowrap><h1>Customer Management Services</td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"javascript:getHTML('AccountsgeceivableServicesw','')\">Customer Focus</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(141) + "</td></tr>");

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4225, unm, uty, dnm, localDefnsDir, defnsDir))
      scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"javascript:getHTML('CustomerChangeOccurrencesw','')\">Replace Customer Code</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(4225) + "</td></tr>");

    boolean SalesOrderServices = authenticationUtils.verifyAccess(con, stmt, rs, req, 140, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean QuotationServices = authenticationUtils.verifyAccess(con, stmt, rs, req, 142, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean EnquiryServices = authenticationUtils.verifyAccess(con, stmt, rs, req, 143, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean OrderConfirmationServices = authenticationUtils.verifyAccess(con, stmt, rs, req, 146, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean PickingListServices = authenticationUtils.verifyAccess(con, stmt, rs, req, 147, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean DeliveryOrderServices = authenticationUtils.verifyAccess(con, stmt, rs, req, 148, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean SalesInvoiceServices = authenticationUtils.verifyAccess(con, stmt, rs, req, 149, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean ProformaInvoiceServices = authenticationUtils.verifyAccess(con, stmt, rs, req, 150, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean _151 = authenticationUtils.verifyAccess(con, stmt, rs, req, 151, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean WorksOrderServices = authenticationUtils.verifyAccess(con, stmt, rs, req, 169, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean OrderAcknowledgementServices = authenticationUtils.verifyAccess(con, stmt, rs, req, 170, unm, uty, dnm, localDefnsDir, defnsDir);

    if(SalesOrderServices || QuotationServices || EnquiryServices || OrderConfirmationServices || PickingListServices || DeliveryOrderServices || SalesInvoiceServices || ProformaInvoiceServices || _151 || WorksOrderServices || OrderAcknowledgementServices)
    {
      scoutln(out, bytesOut, "<tr><td nowrap><h1>Sales Document Processing Services</td></tr>");

      if(EnquiryServices)
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"javascript:getHTML('EnquiryServicesw','')\">Enquiry Focus</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(143) + "</td></tr>");

      if(QuotationServices)
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"javascript:getHTML('QuotationServicesw','')\">Quotation Focus</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(142) + "</td></tr>");

      if(SalesOrderServices)
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"javascript:getHTML('SalesOrderServicesw','')\">Sales Order Focus</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(140) + "</td></tr>");

      if(OrderAcknowledgementServices)
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"javascript:getHTML('OrderAcknowledgementServicesw','')\">Order Acknowledgement Focus</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(170) + "</td></tr>");

      if(OrderConfirmationServices)
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"javascript:getHTML('OrderConfirmationServicesw','')\">Order Confirmation Focus</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(146) + "</td></tr>");

      if(WorksOrderServices)
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"javascript:getHTML('WorksOrderServicesw','')\">Works Order Focus</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(169) + "</td></tr>");

      if(PickingListServices)
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"javascript:getHTML('PickingListServicesw','')\">Picking List Focus</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(147) + "</td></tr>");

      if(DeliveryOrderServices)
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"javascript:getHTML('DeliveryOrderServicesw','')\">Delivery Order Focus</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(148) + "</td></tr>");

      if(SalesInvoiceServices)
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"javascript:getHTML('SalesInvoiceServicesw','')\">Invoice Focus</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(149) + "</td></tr>");

      if(ProformaInvoiceServices)
        scoutln(out, bytesOut, "<tr><td></td><td><p nowrap><a href=\"javascript:getHTML('ProformaInvoiceServicesw','')\">Proforma Invoice Focus</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(150) + "</td></tr>");

      if(_151)
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"javascript:getHTML('_151w','')\">PrePayment Focus</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(151) + "</td></tr>");
    }

    boolean MainPageUtils8 = authenticationUtils.verifyAccess(con, stmt, rs, req, 1008, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean SalesInvoiceListingInput = authenticationUtils.verifyAccess(con, stmt, rs, req, 1023, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean DocumentTrace = authenticationUtils.verifyAccess(con, stmt, rs, req, 1025, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean SalesInvoiceEnquiryInput = authenticationUtils.verifyAccess(con, stmt, rs, req, 1027, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean SalesLinesEnquiry = authenticationUtils.verifyAccess(con, stmt, rs, req, 1030, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean SalesInvoiceListing = authenticationUtils.verifyAccess(con, stmt, rs, req, 1033, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean TrackTraceSalesOrders = authenticationUtils.verifyAccess(con, stmt, rs, req, 2021, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean TrackTraceQuotations = authenticationUtils.verifyAccess(con, stmt, rs, req, 2024, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean TrackTracePickingLists = authenticationUtils.verifyAccess(con, stmt, rs, req, 2025, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean TrackTraceDeliveryOrders = authenticationUtils.verifyAccess(con, stmt, rs, req, 2026, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean TrackTraceInvoices = authenticationUtils.verifyAccess(con, stmt, rs, req, 2028, unm, uty, dnm, localDefnsDir, defnsDir);

    if(MainPageUtils8 || SalesInvoiceListingInput || DocumentTrace || SalesLinesEnquiry || TrackTraceSalesOrders || TrackTraceQuotations || TrackTracePickingLists || TrackTraceDeliveryOrders || TrackTraceInvoices)
    {
      scoutln(out, bytesOut, "<tr><td nowrap><h1>Sales Tracking Services</td></tr>");

      if(MainPageUtils8)
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"javascript:getHTML('MainPageUtils8w','')\">Word Search Document Lines</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(1008) + "</td></tr>");

      if(DocumentTrace)
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"javascript:getHTML('DocumentTracew','')\">Document Trace</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(1025) + "</td></tr>");

      if(TrackTraceQuotations)
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"javascript:getHTML('TrackTraceQuotationsw','')\">Quotation Track and Trace</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(2024) + "</td></tr>");

      if(TrackTraceSalesOrders)
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"javascript:getHTML('TrackTraceSalesOrdersw','')\">Sales Order Track and Trace</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(2021) + "</td></tr>");

      if(TrackTracePickingLists)
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"javascript:getHTML('TrackTracePickingListsw','')\">Picking List Track and Trace</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(2025) + "</td></tr>");

      if(TrackTraceDeliveryOrders)
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"javascript:getHTML('TrackTraceDeliveryOrdersw','')\">Delivery Order Track and Trace</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(2026) + "</td></tr>");

      if(TrackTraceInvoices)
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"javascript:getHTML('TrackTraceInvoicesw','')\">Invoice Track and Trace</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(2028) + "</td></tr>");

      if(SalesInvoiceListingInput)
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"javascript:getHTML('SalesInvoiceListingInputw','')\">Invoice Listing Enquiry by SalesPerson</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(1023) + "</td></tr>");

      if(SalesInvoiceListing)
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"javascript:getHTML('SalesInvoiceListingw','')\">Invoice Listing Enquiry by Date</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(1033) + "</td></tr>");

      if(SalesInvoiceEnquiryInput)
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"javascript:getHTML('SalesInvoiceEnquiryInputw','')\">Invoice Enquiry for GST</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(1027) + "</td></tr>");

      if(SalesLinesEnquiry)
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"javascript:getHTML('SalesLinesEnquiryw','')\">Document Lines Enquiry</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(1030) + "</td></tr>");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 106, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "<tr><td nowrap><h1>Debtor Settlement Services</td></tr>");

      scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"javascript:getHTML('DebtorSettlementServicesWave','')\">Debtor Settlement</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(106) + "</td></tr>");
    }

    scoutln(out, bytesOut, "</table></form>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {
    out.println(str);
    bytesOut[0] += (str.length() + 2);
  }

}
