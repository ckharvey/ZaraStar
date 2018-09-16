// =======================================================================================================================================================================================================
// System: ZaraStar Document: Change Discounts for a Document - update
// Module: DocumentChangeDiscountsExecute.java
// Author: C.K.Harvey
// Copyright (c) 2002-08 Christopher Harvey. All Rights Reserved.
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
import java.net.*;
import java.sql.*;

public class DocumentChangeDiscountsExecute extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="", p3="", p4="";

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
      p3  = req.getParameter("p3"); // discount
      p4  = req.getParameter("p4"); // which: Stock, Non-stock, All
    
      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, p3, p4, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "DocumentChangeDiscountsExecute", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6098, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm,
                    String bnm, String p1, String p2, String p3, String p4, int[] bytesOut) throws Exception
  {
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');

    long startTime = new java.util.Date().getTime();

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 6098, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "6098a", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6098, bytesOut[0], 0, "ACC:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "6098a", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6098, bytesOut[0], 0, "SID:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    set(con, stmt, rs, out, p1, p2, p3, p4, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 6098, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con  != null) con.close();
    if(out != null) out.flush();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String docCode, String docType, String discount, String which, String unm,
                   String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir) throws Exception
  {
    byte[] code = new byte[21];
    generalUtils.strToBytes(code, docCode);

    String servletToCall=null;

    if(docType.equals("SO"))
    {
      SalesOrder salesOrder = new SalesOrder();
      salesOrder.updateDiscounts(con, stmt, rs, code, discount, which, unm, dnm, localDefnsDir, defnsDir);
      servletToCall = "SalesOrderPage";
    }
    else
    if(docType.equals("Q"))
    {
      Quotation quotation = new Quotation();
      quotation.updateDiscounts(con, stmt, rs, code, discount, which, unm, dnm, localDefnsDir, defnsDir);
      servletToCall = "QuotationPage";
    }
    else
    if(docType.equals("OC"))
    {
      OrderConfirmation orderConfirmation = new OrderConfirmation();
      orderConfirmation.updateDiscounts(con, stmt, rs, code, discount, which, unm, dnm, localDefnsDir, defnsDir);
      servletToCall = "OrderConfirmationPage";
    }  
    else
    if(docType.equals("OA"))
    {
      OrderAcknowledgement orderAcknowledgement = new OrderAcknowledgement();
      orderAcknowledgement.updateDiscounts(con, stmt, rs, code, discount, which, unm, dnm, localDefnsDir, defnsDir);
      servletToCall = "OrderAcknowledgementPage";
    }  
    else
    if(docType.equals("PL"))
    {
      PickingList pickingList = new PickingList();
      pickingList.updateDiscounts(con, stmt, rs, code, discount, which, unm, dnm, localDefnsDir, defnsDir);
      servletToCall = "GoodsReceivedPickingList";
    }
    else
    if(docType.equals("DO"))
    {
      DeliveryOrder deliveryOrder = new DeliveryOrder();
      deliveryOrder.updateDiscounts(con, stmt, rs, code, discount, which, unm, dnm, localDefnsDir, defnsDir);
      servletToCall = "DeliveryOrderPage";
    }
    else
    if(docType.equals("I"))
    {
      SalesInvoice salesInvoice = new SalesInvoice();
      salesInvoice.updateDiscounts(con, stmt, rs, code, discount, which, unm, dnm, localDefnsDir, defnsDir);
      servletToCall = "SalesInvoicePage";
    }
    else
    if(docType.equals("PRO"))
    {
      ProformaInvoice proformaInvoice = new ProformaInvoice();
      proformaInvoice.updateDiscounts(con, stmt, rs, code, discount, which, unm, dnm, localDefnsDir, defnsDir);
      servletToCall = "ProformaInvoicePage";
    }
    else
    if(docType.equals("DN"))
    {
      SalesDebitNote salesDebitNote = new SalesDebitNote();
      salesDebitNote.updateDiscounts(con, stmt, rs, code, discount, which, unm, dnm, localDefnsDir, defnsDir);
      servletToCall = "SalesDebitNotePage";
    }
    else
    if(docType.equals("CN"))
    {
      SalesCreditNote salesSalesCreditNote = new SalesCreditNote();
      salesSalesCreditNote.updateDiscounts(con, stmt, rs, code, discount, which, unm, dnm, localDefnsDir, defnsDir);
      servletToCall = "SalesCreditNotePage";
    }
    else
    if(docType.equals("PO"))
    {
      PurchaseOrder purchaseOrder = new PurchaseOrder();
      purchaseOrder.updateDiscounts(con, stmt, rs, code, discount, which, unm, dnm, localDefnsDir, defnsDir);
      servletToCall = "PurchaseOrderPage";
    }
   else
    if(docType.equals("LP"))
    {
      LocalPurchase localPurchase = new LocalPurchase();
      localPurchase.updateDiscounts(con, stmt, rs, code, discount, which, unm, dnm, localDefnsDir, defnsDir);
      servletToCall = "LocalPurchasePage";
    }
    else
    if(docType.equals("PI"))
    {
      PurchaseInvoice purchaseInvoice = new PurchaseInvoice();
      purchaseInvoice.updateDiscounts(con, stmt, rs, code, discount, which, unm, dnm, localDefnsDir, defnsDir);
      servletToCall = "PurchaseInvoicePage";
    }
    else
    if(docType.equals("PDN"))
    {
      PurchaseDebitNote purchaseDebitNote = new PurchaseDebitNote();
      purchaseDebitNote.updateDiscounts(con, stmt, rs, code, discount, which, unm, dnm, localDefnsDir, defnsDir);
      servletToCall = "PurchaseDebitNotePage";
    }
    else
    if(docType.equals("PCN"))
    {
      PurchaseCreditNote purchaseCreditNote = new PurchaseCreditNote();
      purchaseCreditNote.updateDiscounts(con, stmt, rs, code, discount, which, unm, dnm, localDefnsDir, defnsDir);
      servletToCall = "PurchaseCreditNotePage";
    }

    byte[] dataAlready = new byte[2000];

    dataAlready[0] = '\000';

    getRec(out, servletToCall, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, docCode, "", dataAlready, 'A');
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void getRec(PrintWriter out, String servletToCall, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                      String localDefnsDir, String code, String errStr, byte[] dataAlready, char cad)  throws Exception
  {
    URL url = new URL("http://" + serverUtils.serverToCall("OFSA", localDefnsDir) + "/central/servlet/" + servletToCall);

    HttpURLConnection uc = (HttpURLConnection)url.openConnection();
    uc.setDoInput(true);
    uc.setDoOutput(true);
    uc.setUseCaches(false);
    uc.setDefaultUseCaches(false);
    uc.setRequestProperty("Content-Type", "application/x-www-form.urlencoded");

    String s2 = "&unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1="
              + generalUtils.sanitise(code) + "&p2="  + cad + "&p3="  + generalUtils.sanitise(true, errStr) + "&p4="  + generalUtils.sanitise(dataAlready);

    Integer len = new Integer(s2.length());
    uc.setRequestProperty("Content-Length", len.toString());
    
    uc.setRequestMethod("POST");

    PrintWriter p = new PrintWriter(uc.getOutputStream());
    p.print(s2);    
    p.flush();
    p.close();

    BufferedReader di = new BufferedReader(new InputStreamReader(uc.getInputStream()));
    String s = di.readLine();
    while(s != null)
    {
      out.println(s);
      s = di.readLine();
    }

    di.close();
  }

}
