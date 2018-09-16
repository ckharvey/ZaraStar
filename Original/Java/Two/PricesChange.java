// =======================================================================================================================================================================================================
// System: ZaraStar DocumentEngine: Change Prices for a Document
// Module: PricesChange.java
// Author: C.K.Harvey
// Copyright (c) 2002-08 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

public class PricesChange extends HttpServlet
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "PricesChangeExecute", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6093, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                    String p1, String p2, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 6093, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "6093", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6093, bytesOut[0], 0, "ACC:" + p1);
      
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "6093", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6093, bytesOut[0], 0, "SID:" + p1);
      
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }
  
    set(con, stmt, rs, out, req, p1, p2, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 6093, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
        
  if(con  != null) con.close();
    if(out != null) out.flush();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String docCode, String docType, String unm,
                   String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut)
                   throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Change Prices for a Document</title></head>");
    
    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir)
                         + "general.css\">");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">"); 

    scoutln(out, bytesOut, "function go(band){var clear;if(document.forms[0].clear.checked)clear='Y';else clear='N';");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/PricesChangeExecute?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men
                         + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + generalUtils.sanitise(docCode) + "&p2=" + generalUtils.sanitise(docType)
                         + "&p3=\"+band+\"&p4=\"+clear;}");
    
    
    scoutln(out, bytesOut, "</script>");

    String s="", servlet="";
    if(docType.equals("SO"))
    {
      s = "Sales Order";
      servlet = "SalesOrderPage";
    }
    else
    if(docType.equals("RFQ"))
      s = "Enquiry (RFQ)";
    else
    if(docType.equals("Q"))
    {
      s = "Quotation";
      servlet = "QuotationPage";
    }
    else
    if(docType.equals("OC"))
    {
      s = "Order Confirmation";
      servlet = "OrderConfirmationPage";
    }
    else
    if(docType.equals("OA"))
    {
      s = "Order Acknowledgement";
      servlet = "OrderAcknowledgementPage";
    }
    else
    if(docType.equals("PL"))
    {  
      s = "Picking List";
      servlet = "GoodsReceivedPickingList";
    }
    else
    if(docType.equals("DO"))
    {
      s = "Delivery Order";
      servlet = "DeliveryOrderPage";
    }
    else
    if(docType.equals("I"))
    {
      s = "Invoice";
      servlet = "SalesInvoicePage";
    }
    else
    if(docType.equals("PRO"))
    {
      s = "Proforma Invoice";
      servlet = "ProformaInvoicePage";
    }
    else
    if(docType.equals("PP"))
    {  
      s = "PrePayment";
      servlet = "_4092";
    }
    else
    if(docType.equals("DN"))
    {
      s = "Debit Note";
      servlet = "SalesDebitNotePage";
    }
    else
    if(docType.equals("CN"))
    {
      s = "Credit Note";
      servlet = "SalesCreditNotePage";
    }
    else
    if(docType.equals("PO"))
    {
      s = "Purchase Order";
      servlet = "PurchaseOrderPage";
    }
    else
    if(docType.equals("LP"))
    {
      s = "Local Purchase";
      servlet = "LocalPurchasePage";
    }
    else
    if(docType.equals("GRN"))
      s = "Goods Received Note";
    else
    if(docType.equals("PI"))
    {
      s = "Purchase Invoice";
      servlet = "PurchaseInvoicePage";
    }
    else
    if(docType.equals("PDN"))
    {
      s = "Purchase Debit Note";
     servlet = "PurchaseDebitNotePage";
    } 
    else
    if(docType.equals("PCN"))
    {
      s = "Purchase Credit Note";
      servlet = "PurchaseCreditNotePage";
    }

    int[] hmenuCount = new int[1];

    drawingUtils.outputPageFrame(con, stmt, rs, out, req, "6093", "", "PricgrChange", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount, bytesOut);

    drawingUtils.drawTitle(out, false, false, "PricgrChange", "", "Documents - Prices Change", "6093", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
   
    scoutln(out, bytesOut, "<form><table id=\"page\" cellspacing=2 cellpadding=0 border=0 width=100%>");
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td colspan=2><p>&nbsp;ReCalculate all lines of <b>" + s + " " + docCode + " </b>using:</td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td width=100></td><td nowrap width=90%><p>&nbsp;<a href=\"javascript:go('0')\">List Prices</a></td></tr>");
    scoutln(out, bytesOut, "<tr><td></td><td nowrap width=90%><p>&nbsp;<a href=\"javascript:go('1')\">Band 1 Prices</a></td></tr>");
    scoutln(out, bytesOut, "<tr><td></td><td nowrap width=90%><p>&nbsp;<a href=\"javascript:go('2')\">Band 2 Prices</a></td></tr>");
    scoutln(out, bytesOut, "<tr><td></td><td nowrap width=90%><p>&nbsp;<a href=\"javascript:go('3')\">Band 3 Prices</a></td></tr>");
    scoutln(out, bytesOut, "<tr><td></td><td nowrap width=90%><p>&nbsp;<a href=\"javascript:go('4')\">Band 4 Prices</a></td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap colspan=2><p><input type=checkbox name=clear> &nbsp;&nbsp; Clear all the Line Discounts</td></tr>");
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "</table></form>");     scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}

