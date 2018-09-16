// ===================================================================================================================================================
// System: ZaraStar: Utils: show document map page
// Module: DocumentMap.java
// Author: C.K.Harvey
// Copyright (c) 2001-07 Christopher Harvey. All Rights Reserved.
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

public class DocumentMap extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  DrawingUtils drawingUtils = new DrawingUtils();
  
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "DocumentMap", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 131, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den,
                    String dnm, String bnm, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    
    Statement stmt = null;
    ResultSet rs   = null;
    
    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 131, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "131", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 131, bytesOut[0], 0, "ACC:");
      
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }
 
    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "131", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 131, bytesOut[0], 0, "SID:");
      
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    set(con, stmt, rs, out, req, unm, sid, uty, men, den, dnm, bnm, imagesDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 131, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm,
                   String bnm, String imagesDir, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Document Map</title>");
    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    scoutln(out, bytesOut, "function go(servlet){");
    scoutln(out, bytesOut, "window.location.href='/central/servlet/'+servlet+'?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men
                         + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "';}");

    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\""  + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir)
                         + "general.css\">");

    int[] hmenuCount = new int[1];

    drawingUtils.outputPageFrame(con, stmt, rs, out, req, "131", "", "DocumentMap", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount, bytesOut);

    drawingUtils.drawTitle(out, false, false, "DocumentMap", "", "Document Map", "131", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<form><table id=\"page\" border=0 cellspacing=2 cellpadding=0 width=100%>");

    scoutln(out, bytesOut, "<tr><td colspan=2 align=left valign=top><img src=\"" + imagesDir + "z535.gif\" usemap=\"#map\" border=0>");

    scoutln(out, bytesOut, "<map name=\"map\">");

    // Orders/Fulfillment
    scoutln(out, bytesOut, "<area name=\"rect\" coords=\"0,66,99,85\"      href=\"javascript:go('ProductServices')\">"); // suppliers
    scoutln(out, bytesOut, "<area name=\"rect\" coords=\"0,146,99,161\"    href=\"javascript:go('PurchaseOrderServices')\">"); // POs
    scoutln(out, bytesOut, "<area name=\"rect\" coords=\"0,172,99,187\"    href=\"javascript:go('LocalPurchaesServices')\">"); // LPRs
    scoutln(out, bytesOut, "<area name=\"rect\" coords=\"0,207,99,222\"    href=\"javascript:go('GoodsReceivedServices')\">"); // GRNs
    scoutln(out, bytesOut, "<area name=\"rect\" coords=\"0,237,99,252\"    href=\"javascript:go('PurchaseInvoiceServices')\">"); // PIs
    scoutln(out, bytesOut, "<area name=\"rect\" coords=\"141,52,221,98\"   href=\"javascript:go('ProductServices')\">"); // supplierManagement
    scoutln(out, bytesOut, "<area name=\"rect\" coords=\"141,176,221,222\" href=\"javascript:go('ProductServices')\">"); // purchaseDocumentProcessing
    scoutln(out, bytesOut, "<area name=\"rect\" coords=\"141,255,221,302\" href=\"javascript:go('ProductServices')\">"); // poTracking
    scoutln(out, bytesOut, "<area name=\"rect\" coords=\"266,52,346,98\"   href=\"javascript:go('ProductServices')\">"); // stockRecords
    scoutln(out, bytesOut, "<area name=\"rect\" coords=\"266,119,346,165\" href=\"javascript:go('ProductServices')\">"); // adjustmentRecords
    scoutln(out, bytesOut, "<area name=\"rect\" coords=\"266,188,346,234\" href=\"javascript:go('ProductServices')\">"); // goodsReceived
    scoutln(out, bytesOut, "<area name=\"rect\" coords=\"266,255,346,301\" href=\"javascript:go('ProductServices')\">"); // goodsOut
    scoutln(out, bytesOut, "<area name=\"rect\" coords=\"275,325,340,371\" href=\"javascript:go('ProductCatalogsAdmin')\">"); // stockAndCatalogs
    scoutln(out, bytesOut, "<area name=\"rect\" coords=\"392,52,472,98\"   href=\"javascript:go('CustomerServices')\">"); // customerManagement
    scoutln(out, bytesOut, "<area name=\"rect\" coords=\"392,175,472,221\" href=\"javascript:go('CustomerServices')\">"); // salesDocumentProcesing
    scoutln(out, bytesOut, "<area name=\"rect\" coords=\"392,255,472,301\" href=\"javascript:go('TrackTraceSalesOrders')\">"); // soTracking
    scoutln(out, bytesOut, "<area name=\"rect\" coords=\"511,65,595,85\"   href=\"javascript:go('CustomerServices')\">"); // customers
    scoutln(out, bytesOut, "<area name=\"rect\" coords=\"511,110,595,125\" href=\"javascript:go()\">"); // enquiries
    scoutln(out, bytesOut, "<area name=\"rect\" coords=\"511,140,595,155\" href=\"javascript:go('QuotationServices')\">"); // quotations
    scoutln(out, bytesOut, "<area name=\"rect\" coords=\"511,170,595,185\" href=\"javascript:go('SalesOrderServices')\">"); // SOs
    scoutln(out, bytesOut, "<area name=\"rect\" coords=\"511,210,595,225\" href=\"javascript:go('OrderConfirmationServices')\">"); // OCs
    scoutln(out, bytesOut, "<area name=\"rect\" coords=\"511,241,595,256\" href=\"javascript:go('PickingListServices')\">"); // PLs
    scoutln(out, bytesOut, "<area name=\"rect\" coords=\"511,271,595,286\" href=\"javascript:go('DeliveryOrderServices')\">"); // DOs
    scoutln(out, bytesOut, "<area name=\"rect\" coords=\"511,301,595,316\" href=\"javascript:go('SalesInvoiceServices')\">"); // Invoices

    // Settlement
    scoutln(out, bytesOut, "<area name=\"rect\" coords=\"0,401,113,416\"   href=\"javascript:go('PurchaseCreditNoteServices')\">"); // PCNs
    scoutln(out, bytesOut, "<area name=\"rect\" coords=\"0,431,113,446\"   href=\"javascript:go('PurchaseDebitNoteServices')\">"); // PDNs
    scoutln(out, bytesOut, "<area name=\"rect\" coords=\"0,461,113,476\"   href=\"javascript:go('PaymentServices')\">"); // payments
    scoutln(out, bytesOut, "<area name=\"rect\" coords=\"141,418,221,464\" href=\"javascript:go('ProductServices')\">"); // creditorSettlement
    scoutln(out, bytesOut, "<area name=\"rect\" coords=\"237,444,348,463\" href=\"javascript:go('CAAReport')\">"); // CAA
    scoutln(out, bytesOut, "<area name=\"rect\" coords=\"255,418,372,436\" href=\"javascript:go('MainPageUtils4')\">"); // DAA
    scoutln(out, bytesOut, "<area name=\"rect\" coords=\"392,417,472,463\" href=\"javascript:go('CustomerServices')\">"); // debtorSettlement
    scoutln(out, bytesOut, "<area name=\"rect\" coords=\"372,490,491,509\" href=\"javascript:go('Statements')\">"); // soa
    scoutln(out, bytesOut, "<area name=\"rect\" coords=\"501,402,595,417\" href=\"javascript:go('SalesCreditNoteServices')\">"); // CNs
    scoutln(out, bytesOut, "<area name=\"rect\" coords=\"501,432,595,448\" href=\"javascript:go('SalesDebitNoteServices')\">"); // DNs
    scoutln(out, bytesOut, "<area name=\"rect\" coords=\"501,462,595,477\" href=\"javascript:go('ReceiptServices')\">"); // receipts

    // Accounts
    scoutln(out, bytesOut, "<area name=\"rect\" coords=\"0,546,61,576\"    href=\"javascript:go()\">"); // paymentVouchers
    scoutln(out, bytesOut, "<area name=\"rect\" coords=\"0,624,54,656\"    href=\"javascript:go()\">"); // creditorsLedger
    scoutln(out, bytesOut, "<area name=\"rect\" coords=\"114,662,234,681\" href=\"javascript:go()\">"); // coa
    scoutln(out, bytesOut, "<area name=\"rect\" coords=\"114,691,234,710\" href=\"javascript:go()\">"); // currency
    scoutln(out, bytesOut, "<area name=\"rect\" coords=\"246,546,366,565\" href=\"javascript:go()\">"); // document
    scoutln(out, bytesOut, "<area name=\"rect\" coords=\"246,574,366,593\" href=\"javascript:go()\">"); // reconciliation
    scoutln(out, bytesOut, "<area name=\"rect\" coords=\"246,603,366,622\" href=\"javascript:go()\">"); // procedures
    scoutln(out, bytesOut, "<area name=\"rect\" coords=\"246,631,366,650\" href=\"javascript:go()\">"); // ledger
    scoutln(out, bytesOut, "<area name=\"rect\" coords=\"246,662,366,681\" href=\"javascript:go()\">"); // journal
    scoutln(out, bytesOut, "<area name=\"rect\" coords=\"376,662,496,681\" href=\"javascript:go()\">"); // statReporting
    scoutln(out, bytesOut, "<area name=\"rect\" coords=\"376,691,496,710\" href=\"javascript:go()\">"); // definitions
    scoutln(out, bytesOut, "<area name=\"rect\" coords=\"534,546,595,576\" href=\"javascript:go()\">"); // receiptVouchers
    scoutln(out, bytesOut, "<area name=\"rect\" coords=\"542,625,595,657\" href=\"javascript:go()\">"); // debtorsledeger

    scoutln(out, bytesOut, "</map></td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir));
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }
  
}
