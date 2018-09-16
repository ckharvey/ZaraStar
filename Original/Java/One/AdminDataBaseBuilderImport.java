// =======================================================================================================================================================================================================
// System: ZaraStar: AdminEngine - DataBase Builder: Import
// Module: AdminDataBaseBuilderImport.java
// Author: C.K.Harvey
// Copyright (c) 2003-07 Christopher Harvey. All Rights Reserved.
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

public class AdminDataBaseBuilderImport extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  AdminUtils adminUtils = new AdminUtils();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="";

    try
    {
      directoryUtils.setContentHeaders(res);
      out = res.getWriter();

      unm = req.getParameter("unm");
      uty = req.getParameter("uty");
      sid = req.getParameter("sid");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");
      p1  = req.getParameter("p1");
      
      doIt(out, req, p1, unm, sid, uty, men, den, dnm, bnm, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "AdminDataBaseBuilderImport", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7202, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String p1, String unm, String sid, String uty, String men,
                    String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');

    long startTime = new java.util.Date().getTime();

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! serverUtils.isSysAdmin(con, stmt, rs, unm, dnm, sid, uty, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "AdminDataBaseBuilderImport", imagesDir, localDefnsDir,
                         defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7202, bytesOut[0], 0, "ACC:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "AdminDataBaseBuilderImport", imagesDir, localDefnsDir,
                         defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7202, bytesOut[0], 0, "SID:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }
  
    create(con, stmt, rs, out, req, p1, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 7202, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void create(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String tableName, String unm, String sid, String uty,
                      String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut)
                      throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Admin - DataBase Builder: Import</title>");
    
    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\""
                           + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">"); 

    scoutln(out, bytesOut, "function validate(){importOrValidate='V';document.forms[0].submit()}");
    scoutln(out, bytesOut, "function importIt(){importOrValidate='I';document.forms[0].submit()}");

    scoutln(out, bytesOut, "</script>");

    scoutln(out, bytesOut, "<form action=\"AdminDataBaseBuilderImportProcess\" enctype=\"application/x-www-form-urlencoded\" method=POST>");

    int[] hmenuCount = new int[1];

    adminUtils.outputPageFrame(con, stmt, rs, out, req, "AdminDataBaseBuilderImport", "", "7202", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount, bytesOut);

    adminUtils.drawTitle(con, stmt, rs, req, out, "Administration - DataBase Builder: Import (" + tableName + ")", "7202", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
  
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"unm\" value='" + unm + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"sid\" value='" + sid + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"uty\" value='" + uty + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"men\" value='" + men + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"den\" value='" + den + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"dnm\" value='" + dnm + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"bnm\" value='" + bnm + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"importOrValidate\">");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"p1\"  value='" + tableName + "'>");

    scoutln(out, bytesOut, "<table id=\"page\" border=0 width=100%>");

    scoutln(out, bytesOut, "<tr id=\"pageColumn\">");

    scoutln(out, bytesOut, "<td><p>Field</td><td><p>CSV Field</td><td><p>Rule</td><td><p>Default</td><td><p>Value</td></tr>");

    String fieldNames="";

    if(tableName.equals("company"))
    {
      Customer customer = new Customer();
      fieldNames = customer.getFieldNames();
    }  
    else
    if(tableName.equals("po"))
    {
      PurchaseOrder purchaseOrder = new PurchaseOrder();
      fieldNames = purchaseOrder.getFieldNamesPO();
    }  
    else
    if(tableName.equals("pol"))
    {
      PurchaseOrder purchaseOrder = new PurchaseOrder();
      fieldNames = purchaseOrder.getFieldNamesPOL();
    }  
    else
    if(tableName.equals("poll"))
    {
      PurchaseOrder purchaseOrder = new PurchaseOrder();
      fieldNames = purchaseOrder.getFieldNamesPOLL();
    }  
    else
    if(tableName.equals("so"))
    {
      SalesOrder salesOrder = new SalesOrder();
      fieldNames = salesOrder.getFieldNamesSO();
    }  
    else
    if(tableName.equals("sol"))
    {
      SalesOrder salesOrder = new SalesOrder();
      fieldNames = salesOrder.getFieldNamesSOL();
    }  
    else
    if(tableName.equals("soll"))
    {
      SalesOrder salesOrder = new SalesOrder();
      fieldNames = salesOrder.getFieldNamesSOLL();
    }  
    else
    if(tableName.equals("stock"))
    {
      Inventory inventory = new Inventory();
      fieldNames = inventory.getFieldNamesStock();
    }  
    else
    if(tableName.equals("stockx"))
    {
      Inventory inventory = new Inventory();
      fieldNames = inventory.getFieldNamesStockx();
    }  
    else
    if(tableName.equals("quote"))
    {
      Quotation quotation = new Quotation();
      fieldNames = quotation.getFieldNamesQuote();
    }  
    else
    if(tableName.equals("quotel"))
    {
      Quotation quotation = new Quotation();
      fieldNames = quotation.getFieldNamesQuoteL();
    }  
    else
    if(tableName.equals("quotell"))
    {
      Quotation quotation = new Quotation();
      fieldNames = quotation.getFieldNamesQuoteLL();
    }  
    else
    if(tableName.equals("invoice"))
    {
      SalesInvoice salesInvoice = new SalesInvoice();
      fieldNames = salesInvoice.getFieldNamesInvoice();
    }  
    else
    if(tableName.equals("invoicel"))
    {
      SalesInvoice salesInvoice = new SalesInvoice();
      fieldNames = salesInvoice.getFieldNamesInvoiceL();
    }  
    else
    if(tableName.equals("invoicell"))
    {
      SalesInvoice salesInvoice = new SalesInvoice();
      fieldNames = salesInvoice.getFieldNamesInvoiceLL();
    }  
    else
    if(tableName.equals("itemmove"))
    {
      InventoryAdjustment inventoryAdjustment = new InventoryAdjustment();
      fieldNames = inventoryAdjustment.getFieldNamesStockA();
    }  
    else
    if(tableName.equals("stockc"))
    {
      InventoryAdjustment inventoryAdjustment = new InventoryAdjustment();
      fieldNames = inventoryAdjustment.getFieldNamesStockC();
    }  
    else
    if(tableName.equals("proforma"))
    {
      ProformaInvoice proformaInvoice = new ProformaInvoice();
      fieldNames = proformaInvoice.getFieldNamesProforma();
    }  
    else
    if(tableName.equals("proformal"))
    {
      ProformaInvoice proformaInvoice = new ProformaInvoice();
      fieldNames = proformaInvoice.getFieldNamesProformaL();
    }  
    else
    if(tableName.equals("proformall"))
    {
      ProformaInvoice proformaInvoice = new ProformaInvoice();
      fieldNames = proformaInvoice.getFieldNamesProformaLL();
    }  
    else
    if(tableName.equals("debit"))
    {
      SalesDebitNote salesDebitNote = new SalesDebitNote();
      fieldNames = salesDebitNote.getFieldNamesDebitNote();
    }  
    else
    if(tableName.equals("debitl"))
    {
      SalesDebitNote salesDebitNote = new SalesDebitNote();
      fieldNames = salesDebitNote.getFieldNamesDebitNoteL();
    }  
    else
    if(tableName.equals("debitll"))
    {
      SalesDebitNote salesDebitNote = new SalesDebitNote();
      fieldNames = salesDebitNote.getFieldNamesDebitNoteLL();
    }  
    else
    if(tableName.equals("pdebit"))
    {
      PurchaseDebitNote purchaseDebitNote = new PurchaseDebitNote();
      fieldNames = purchaseDebitNote.getFieldNamesPurchaseDebitNote();
    }  
    else
    if(tableName.equals("pdebitl"))
    {
      PurchaseDebitNote purchaseDebitNote = new PurchaseDebitNote();
      fieldNames = purchaseDebitNote.getFieldNamesPurchaseDebitNoteL();
    }  
    else
    if(tableName.equals("pdebitll"))
    {
      PurchaseDebitNote purchaseDebitNote = new PurchaseDebitNote();
      fieldNames = purchaseDebitNote.getFieldNamesPurchaseDebitNoteLL();
    }  
    else
    if(tableName.equals("pinvoice"))
    {
      PurchaseInvoice purchaseInvoice = new PurchaseInvoice();
      fieldNames = purchaseInvoice.getFieldNamesPurchaseInvoice();
    }  
    else
    if(tableName.equals("pinvoicel"))
    {
      PurchaseInvoice purchaseInvoice = new PurchaseInvoice();
      fieldNames = purchaseInvoice.getFieldNamesPurchaseInvoiceL();
    }  
    else
    if(tableName.equals("pinvoicell"))
    {
      PurchaseInvoice purchaseInvoice = new PurchaseInvoice();
      fieldNames = purchaseInvoice.getFieldNamesPurchaseInvoiceLL();
    }  
    else
    if(tableName.equals("credit"))
    {
      SalesCreditNote salesSalesCreditNote = new SalesCreditNote();
      fieldNames = salesSalesCreditNote.getFieldNamesSalesCreditNote();
    }  
    else
    if(tableName.equals("creditl"))
    {
      SalesCreditNote salesSalesCreditNote = new SalesCreditNote();
      fieldNames = salesSalesCreditNote.getFieldNamesSalesCreditNoteL();
    }  
    else
    if(tableName.equals("creditll"))
    {
      SalesCreditNote salesSalesCreditNote = new SalesCreditNote();
      fieldNames = salesSalesCreditNote.getFieldNamesSalesCreditNoteLL();
    }  
    else
    if(tableName.equals("pcredit"))
    {
      PurchaseCreditNote purchaseCreditNote = new PurchaseCreditNote();
      fieldNames = purchaseCreditNote.getFieldNamesPurchaseCreditNote();
    }  
    else
    if(tableName.equals("pcreditl"))
    {
      PurchaseCreditNote purchaseCreditNote = new PurchaseCreditNote();
      fieldNames = purchaseCreditNote.getFieldNamesPurchaseCreditNoteL();
    }  
    else
    if(tableName.equals("pcreditll"))
    {
      PurchaseCreditNote purchaseCreditNote = new PurchaseCreditNote();
      fieldNames = purchaseCreditNote.getFieldNamesPurchaseCreditNoteLL();
    }  
    else
    if(tableName.equals("receipt"))
    {
      Receipt receipt = new Receipt();
      fieldNames = receipt.getFieldNamesReceipt();
    }  
    else
    if(tableName.equals("receiptl"))
    {
      Receipt receipt = new Receipt();
      fieldNames = receipt.getFieldNamesReceiptL();
    }  
    else
    if(tableName.equals("payment"))
    {
      Payment payment = new Payment();
      fieldNames = payment.getFieldNamesPayment();
    }  
    else
    if(tableName.equals("paymentl"))
    {
      Payment payment = new Payment();
      fieldNames = payment.getFieldNamesPaymentL();
    }  
    else
    if(tableName.equals("do"))
    {
      DeliveryOrder deliveryOrder = new DeliveryOrder();
      fieldNames = deliveryOrder.getFieldNamesDO();
    }  
    else
    if(tableName.equals("dol"))
    {
      DeliveryOrder deliveryOrder = new DeliveryOrder();
      fieldNames = deliveryOrder.getFieldNamesDOL();
    }  
    else
    if(tableName.equals("doll"))
    {
      DeliveryOrder deliveryOrder = new DeliveryOrder();
      fieldNames = deliveryOrder.getFieldNamesDOLL();
    }  
    else
    if(tableName.equals("pl"))
    {
      PickingList pickingList = new PickingList();
      fieldNames = pickingList.getFieldNamesPL();
    }  
    else
    if(tableName.equals("pll"))
    {
      PickingList pickingList = new PickingList();
      fieldNames = pickingList.getFieldNamesPLL();
    }  
    else
    if(tableName.equals("plll"))
    {
      PickingList pickingList = new PickingList();
      fieldNames = pickingList.getFieldNamesPLLL();
    }  
    else
    if(tableName.equals("gr"))
    {
      GoodsReceivedNote goodsReceivedNote = new GoodsReceivedNote();
      fieldNames = goodsReceivedNote.getFieldNamesGR();
    }  
    else
    if(tableName.equals("grl"))
    {
      GoodsReceivedNote goodsReceivedNote = new GoodsReceivedNote();
      fieldNames = goodsReceivedNote.getFieldNamesGRL();
    }  
    else
    if(tableName.equals("lp"))
    {
      LocalPurchase localPurchase = new LocalPurchase();
      fieldNames = localPurchase.getFieldNamesLP();
    }  
    else
    if(tableName.equals("lpl"))
    {
      LocalPurchase localPurchase = new LocalPurchase();
      fieldNames = localPurchase.getFieldNamesLPL();
    }  
    else
    if(tableName.equals("lpll"))
    {
      LocalPurchase localPurchase = new LocalPurchase();
      fieldNames = localPurchase.getFieldNamesLPLL();
    }  
    else
    if(tableName.equals("oc"))
    {
      OrderConfirmation orderConfirmation = new OrderConfirmation();
      fieldNames = orderConfirmation.getFieldNamesOC();
    }  
    else
    if(tableName.equals("ocl"))
    {
      OrderConfirmation orderConfirmation = new OrderConfirmation();
      fieldNames = orderConfirmation.getFieldNamesOCL();
    }  
    else
    if(tableName.equals("ocll"))
    {
      OrderConfirmation orderConfirmation = new OrderConfirmation();
      fieldNames = orderConfirmation.getFieldNamesOCLL();
    }  
    else
    if(tableName.equals("supplier"))
    {
      Supplier supplier = new Supplier();
      fieldNames = supplier.getFieldNames();
    }  
    else
    if(tableName.equals("voucher"))
    {
      PaymentVoucher paymentVoucher = new PaymentVoucher();
      fieldNames = paymentVoucher.getFieldNamesVoucher();
    }  
    else
    if(tableName.equals("voucherl"))
    {
      PaymentVoucher paymentVoucher = new PaymentVoucher();
      fieldNames = paymentVoucher.getFieldNamesVoucherL();
    }  
    else
    if(tableName.equals("voucherll"))
    {
      PaymentVoucher paymentVoucher = new PaymentVoucher();
      fieldNames = paymentVoucher.getFieldNamesVoucherLL();
    }  
    else
    if(tableName.equals("rvoucher"))
    {
      ReceiptVoucher receiptVoucher = new ReceiptVoucher();
      fieldNames = receiptVoucher.getFieldNamesReceiptVoucher();
    }  
    else
    if(tableName.equals("rvoucherl"))
    {
      ReceiptVoucher receiptVoucher = new ReceiptVoucher();
      fieldNames = receiptVoucher.getFieldNamesReceiptVoucherL();
    }  
    else
    if(tableName.equals("rvoucherll"))
    {
      ReceiptVoucher receiptVoucher = new ReceiptVoucher();
      fieldNames = receiptVoucher.getFieldNamesReceiptVoucherLL();
    }  

    int x=0, fieldCount=1, len = fieldNames.length();
    String field;
    
    while(x < len)
    {
      field="";
      while(x < len && fieldNames.charAt(x) != ',')
        field += fieldNames.charAt(x++);
      ++x;
      while(x < len && fieldNames.charAt(x) == ' ')
        ++x;
      
      scoutln(out, bytesOut, "<tr><td><p>" + field + "</td>");

      scoutln(out, bytesOut, "<td><p><input type=text name=csv" + fieldCount + " size=3></td>");

      scoutln(out, bytesOut, "<td><p>" + buildRuleDDL(fieldCount) + "</td>");

      scoutln(out, bytesOut, "<td><p><input type=text name=default" + fieldCount + " size=10></td>");
      scoutln(out, bytesOut, "<td><p><input type=text name=value" + fieldCount + " size=10></td>");

      scoutln(out, bytesOut, "</tr>");
      ++fieldCount;
    }    
        
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan2=><p>CSV File in Library <input type=text name=docLibNumber size=6></td></tr>");
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan2=><p>Skip first <input type=text name=skipLines size=6 value='0'> lines</td></tr>");
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap colspan=2><p><a href=\"javascript:importIt()\">Import</a>");
    scoutln(out, bytesOut, "&nbsp; &nbsp; New File<input type=checkbox name=createNewFile></td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "</table>");     scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private String buildRuleDDL(int fieldCount) throws Exception
  {
    String s = "<select name=\"rule" + fieldCount + "\">";

    s += "<option value=\"None\">None\n";      
    s += "<option value=\"RC5\">Running Code (5 chars)\n";      
    s += "<option value=\"RC6\">Running Code (6 chars)\n";      
    s += "<option value=\"fromDDMMYY\">Convert From dd.mm.yy\n";      
    s += "<option value=\"fromMMDDYYYY\">Convert From mm/dd/yyyy\n";      
    s += "<option value=\"fromDDMMYYYY\">Convert From dd/mm/yyyy\n";      
    s += "<option value=\"fromYYYYMMDD\">Convert From yyyymmdd\n";      
    s += "<option value=\"seqForCode\">Sequential for Code\n";      
    s += "<option value=\"asLine\">As Line\n";      
    s += "<option value=\"useXRef\">Use XRef Table\n";      
    s += "<option value=\"appendField\">Append Field\n";      

    s += "</select>";
    return s;
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
