// =======================================================================================================================================================================================================
// System: ZaraStar DocumentEngine: Change GST Rates for a Document
// Module: GSTRatesChange.java
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

public class GSTRatesChangeWave extends HttpServlet
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

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="";

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
      p1  = req.getParameter("p1"); // docCode
      p2  = req.getParameter("p2"); // docType

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "GSTRatesChange", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6092, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1, String p2, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);

    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 6092, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreenW(out, req, 13, unm, sid, uty, men, den, dnm, bnm, "6092", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6092, bytesOut[0], 0, "ACC:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreenW(out, req, 12, unm, sid, uty, men, den, dnm, bnm, "6092", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6092, bytesOut[0], 0, "SID:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    set(con, stmt, rs, out, req, p1, p2, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 6092, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con  != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String docCode, String docType, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir,
                   String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    String s="", servlet="";
    if(docType.equals("SO"))
    {
      s = "Sales Order";
      servlet = "SalesOrderPagew";
    }
    else
    if(docType.equals("RFQ"))
      s = "Enquiry (RFQ)";
    else
    if(docType.equals("Q"))
    {
      s = "Quotation";
      servlet = "QuotationPagew";
    }
    else
    if(docType.equals("OC"))
    {
      s = "Confirmation";
      servlet = "OrderConfirmationPagew";
    }
    else
    if(docType.equals("OA"))
    {
      s = "Acknowledgement";
      servlet = "OrderAcknowledgementPagew";
    }
    else
    if(docType.equals("PL"))
    {
      s = "Picking List";
      servlet = "GoodsReceivedPickingListw";
    }
    else
    if(docType.equals("DO"))
    {
      s = "Delivery Order";
      servlet = "DeliveryOrderPagew";
    }
    else
    if(docType.equals("I"))
    {
      s = "Invoice";
      servlet = "SalesInvoicePagew";
    }
    else
    if(docType.equals("PRO"))
    {
      s = "Proforma Invoice";
      servlet = "ProformaInvoicePagew";
    }
    else
    if(docType.equals("PP"))
    {
      s = "PrePayment";
      servlet = "_4092w";
    }
    else
    if(docType.equals("DN"))
    {
      s = "Debit Note";
      servlet = "SalesDebitNotePagew";
    }
    else
    if(docType.equals("CN"))
    {
      s = "Credit Note";
      servlet = "SalesCreditNotePagew";
    }
    else
    if(docType.equals("PO"))
    {
      s = "Purchase Order";
      servlet = "PurchaseOrderPagew";
    }
    else
    if(docType.equals("LP"))
    {
      s = "Local Purchase";
      servlet = "LocalPurchasePagew";
    }
    else
    if(docType.equals("GRN"))
      s = "Goods Received Note";
    else
    if(docType.equals("PI"))
    {
       s = "Purchase Invoice";
       servlet = "PurchaseInvoicePagew";
    }
    else
    if(docType.equals("PDN"))
    {
      s = "Purchase Debit Note";
      servlet = "PurchaseDebitNotePagew";
    }
    else
    if(docType.equals("PCN"))
    {
      s = "Purchase Credit Note";
      servlet = "PurchaseCreditNotePagew";
    }
    else
    if(docType.equals("RV"))
    {
      s = "Receipt Voucher";
      servlet = "ReceiptVoucherPagew";
    }

    drawingUtils.drawTitleW(out, false, false, "GSTRatesChange", "", "Documents - GST Rate Change", "6092", "", "", bytesOut);

    scoutln(out, bytesOut, "<form><table id=\"page\" cellspacing=2 cellpadding=0 border=0 width=100%>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td colspan=2><p>&nbsp;Set all lines of <b>" + s + " " + docCode + " </b>to:</td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    try
    {
      String gstRateName;

      stmt = con.createStatement();
      rs = stmt.executeQuery("SELECT gstRateName FROM gstrate");
      while(rs.next())
      {
        gstRateName = rs.getString(1);
        scoutln(out, bytesOut, "<tr><td width=100></td><td width=90%><p>&nbsp;<a href=\"javascript:getHTML('GSTRatesChangeExecutew','&p1=" + generalUtils.sanitise(docCode) + "&p2=" + generalUtils.sanitise(docType) + "&p3=" + generalUtils.sanitise(gstRateName) + "')\">"
                               + gstRateName + "</a></td></tr>");
      }

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println("AccountsUtils: " + e);
      try
      {
        if(rs   != null) rs.close();
        if(stmt != null) stmt.close();
      }
      catch(Exception e2) { }
    }

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "</table></form>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {
    out.println(str);
    bytesOut[0] += (str.length() + 2);
  }

}
