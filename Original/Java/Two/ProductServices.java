// =======================================================================================================================================================================================================
// System: ZaraStar: Utils: Supplier services access page
// Module: ProductServices.java
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
import java.io.*;
import java.sql.*;

public class ProductServices extends HttpServlet
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
 
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "ProductServices", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 104, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush(); 
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
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
    
    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 5001, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "ProductServices", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 104, bytesOut[0], 0, "ACC:");
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "ProductServices", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 104, bytesOut[0], 0, "SID:");
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    set(con, stmt, rs, out, req, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 104, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut)
                   throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Supplier Services Access</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");
  
    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\"></head>");
  
    int[] hmenuCount = new int[1];
    
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "104", "", "ProductServices", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
                                
    scoutln(out, bytesOut, "<form>");
  
    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Supplier Services", "104", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<table id=\"page\" width=100% border=0>");

    boolean SupplierChangeOccurrences = authenticationUtils.verifyAccess(con, stmt, rs, req, 5068, true, unm, uty, dnm, localDefnsDir, defnsDir);        
        
    scoutln(out, bytesOut, "<tr><td nowrap><h1>Supplier Management Services</td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"/central/servlet/SupplierServices?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                         + "\">Supplier Focus</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(138) + "</td></tr>");
      
    if(SupplierChangeOccurrences)
    {
      scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"/central/servlet/SupplierChangeOccurrences?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                           + "\">Replace Supplier Code</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(5068) + "</td></tr>");
    }

    boolean PurchaseOrderServices = authenticationUtils.verifyAccess(con, stmt, rs, req, 139, true, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean LocalPurchaesServices = authenticationUtils.verifyAccess(con, stmt, rs, req, 152, true, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean GoodsReceivedServices = authenticationUtils.verifyAccess(con, stmt, rs, req, 153, true, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean PurchaseInvoiceServices = authenticationUtils.verifyAccess(con, stmt, rs, req, 154, true, unm, uty, dnm, localDefnsDir, defnsDir);
        
    if(PurchaseOrderServices || LocalPurchaesServices || GoodsReceivedServices || PurchaseInvoiceServices)
    {
      scoutln(out, bytesOut, "<tr><td nowrap><h1>Purchase Document Processing Services</td></tr>");

      if(PurchaseOrderServices)
      {
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"/central/servlet/PurchaseOrderServices?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                             + "\">Purchase Order Focus</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(139) + "</td></tr");
      }
 
      if(LocalPurchaesServices)
      {  
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"/central/servlet/LocalPurchaesServices?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                             + "\">Local Requisition Focus</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(152) + "</td></tr>");
      }

      if(GoodsReceivedServices)
      {
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"/central/servlet/GoodsReceivedServices?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                             + "\">Goods Received Note Focus</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(153) + "</td></tr>");
      }

      if(PurchaseInvoiceServices)
      {  
        scoutln(out, bytesOut, "<tr><td></td><td><p nowrap><a href=\"/central/servlet/PurchaseInvoiceServices?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                             + "\">Purchase Invoice Focus</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(154) + "</td></tr>");
      }
    }
    
    boolean PurchasesWordSearch = authenticationUtils.verifyAccess(con, stmt, rs, req, 1028, true, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean PurchaseInvoiceListingInput = authenticationUtils.verifyAccess(con, stmt, rs, req, 1024, true, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean DocumentTrace = authenticationUtils.verifyAccess(con, stmt, rs, req, 1025, true, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean PurchasesLinesEnquiry = authenticationUtils.verifyAccess(con, stmt, rs, req, 1032, true, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean PurchaseInvoiceListing = authenticationUtils.verifyAccess(con, stmt, rs, req, 1034, true, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean TrackTracePurchaseOrders = authenticationUtils.verifyAccess(con, stmt, rs, req, 2022, true, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean TrackTracePurchaseInvoices = authenticationUtils.verifyAccess(con, stmt, rs, req, 2040, true, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean TrackTraceGoodsReceived = authenticationUtils.verifyAccess(con, stmt, rs, req, 2041, true, unm, uty, dnm, localDefnsDir, defnsDir);
    
    if(PurchasesWordSearch || DocumentTrace || PurchaseInvoiceListingInput || TrackTracePurchaseOrders || TrackTracePurchaseInvoices || TrackTraceGoodsReceived)
    {
      scoutln(out, bytesOut, "<tr><td nowrap><h1>Purchases Tracking Services</td></tr>");

      if(PurchasesWordSearch)
      {
        scoutln(out, bytesOut, "<tr><td>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/PurchasesWordSearch?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                               + "\">Word Search Document Lines</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(1028) + "</td></tr>");
      }
      
      if(DocumentTrace)
      {
        scoutln(out, bytesOut, "<tr><td>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/DocumentTrace?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                               + "\">Document Trace</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(1025) + "</td></tr>");
      }
      
      if(TrackTraceGoodsReceived)
      {
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"/central/servlet/TrackTraceGoodsReceived?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                             + "\">Goods Received Note Track and Trace</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(2041) + "</td></tr>");
      }  
      
      
      if(TrackTracePurchaseOrders)
      {
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"/central/servlet/TrackTracePurchaseOrders?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                             + "\">Purchase Order Track and Trace</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(2022) + "</td></tr>");
      }  
      
      if(TrackTracePurchaseInvoices)
      {
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"/central/servlet/TrackTracePurchaseInvoices?unm=" + unm + "&sid=" + sid + "&uty=" + uty
                             + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                             + "\">Purchase Invoice Track and Trace</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(2040) + "</td></tr>");
      }  
      
      if(PurchaseInvoiceListingInput)
      {
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"/central/servlet/PurchaseInvoiceListingInput?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                               + "\">Purchase Invoice Listing Enquiry by SalesPerson</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(1024) + "</td></tr>");
      }

      if(PurchaseInvoiceListing)
      {
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"/central/servlet/PurchaseInvoiceListing?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                             + "\">Invoice Lines Enquiry by Date</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(1034) + "</td></tr>");
      }  

      if(PurchasesLinesEnquiry)
      {
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"/central/servlet/PurchasesLinesEnquiry?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                             + "\">Document Lines Enquiry</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(1032) + "</td></tr>");
      }  
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 106, true, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "<tr><td nowrap><h1>Creditor Settlement Services</td></tr>");
      
      scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"/central/servlet/CreditorSettlementServices?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                             + "\">Creditor Settlement</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(133) + "</td></tr>");
    }

    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir));
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }
 
}
