// =======================================================================================================================================================================================================
// System: ZaraStar Admin: process import file
// Module: AdminDataBaseBuilderImportProcess.java
// Author: C.K.Harvey
// Copyright (c) 2006-07 Christopher Harvey. All Rights Reserved.
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
import java.util.*;
import java.io.*;

public class AdminDataBaseBuilderImportProcess extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  DocumentUtils documentUtils = new DocumentUtils();
  LibraryUtils libraryUtils = new LibraryUtils();
  AdminUtils adminUtils = new AdminUtils();
  
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", docLibNumber="", importOrValidate="", createNewFile="",
           tableName="", skipLines="";
    
    try
    {
      directoryUtils.setContentHeaders(res);
      out = res.getWriter();

      int[] csvFields = new int[100];
      for(int x=0;x<100;++x) csvFields[x] = 0;

      String[] rules = new String[100];
      for(int x=0;x<100;++x) rules[x] = "";

      String[] defaults = new String[100];
      for(int x=0;x<100;++x) defaults[x] = "";

      String[] values = new String[100];
      for(int x=0;x<100;++x) values[x] = "";

      int num;

      Enumeration en = req.getParameterNames();
      while(en.hasMoreElements())
      {
        String name = (String)en.nextElement();
        String[] value = req.getParameterValues(name);
        if(name.equals("unm"))
          unm = value[0];
        else
        if(name.equals("sid"))
          sid = value[0];
        else
        if(name.equals("uty"))
          uty = value[0];
        else
        if(name.equals("men"))
          men = value[0];
        else
        if(name.equals("den"))
          den = value[0];
        else
        if(name.equals("dnm"))
          dnm = value[0];
        else
        if(name.equals("bnm"))
          bnm = value[0];
        else
        if(name.length() > 2 && name.substring(1, 3).equals(".x")) ;
        else
        if(name.length() > 2 && name.substring(1, 3).equals(".y")) ;
        else
        if(name.equals("docLibNumber"))
          docLibNumber = value[0];
        else
        if(name.equals("p1"))
          tableName = value[0];
        else
        if(name.equals("importOrValidate"))
          importOrValidate = value[0];
        else
        if(name.equals("createNewFile"))
          createNewFile = value[0];
        else
        if(name.equals("skipLines"))
          skipLines = value[0];
        else
        {
          if(name.startsWith("csv"))
          {
            num = generalUtils.strToInt(name.substring(3));
            csvFields[num] = generalUtils.strToInt(value[0]);
          }
          else
          if(name.startsWith("rule"))
          {
            num = generalUtils.strToInt(name.substring(4));
            rules[num] = value[0];
          }
          else
          if(name.startsWith("default"))
          {
            num = generalUtils.strToInt(name.substring(7));
            defaults[num] = value[0];
          }
          else
          if(name.startsWith("value"))
          {
            num = generalUtils.strToInt(name.substring(5));
            values[num] = value[0];
          }
        }        
      }
      
      if(skipLines == null || skipLines.length() == 0) skipLines = "0";
      
      doIt(out, req, tableName, docLibNumber, importOrValidate, generalUtils.strToInt(skipLines), createNewFile, csvFields, rules,
           defaults, values, unm, sid, uty, men, den, dnm, bnm, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "AdminDataBaseBuilderImportProcess", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7202, bytesOut[0], 0, "ERR:" + docLibNumber);
      if(out != null) out.flush();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String tableName, String docLibNumber, String importOrValidate,
                    int skipLines, String createNewFile, int[] csvFields, String[] rules, String[] defaults, String[] values,
                    String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut)
                    throws Exception
  {
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');
    String exportDir      = directoryUtils.getExportDir(dnm);

    long startTime = new java.util.Date().getTime();

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! serverUtils.isSysAdmin(con, stmt, rs, unm, dnm, sid, uty, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "AdminDataBaseBuilderImportProcess", imagesDir, localDefnsDir,
                         defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7202, bytesOut[0], 0, "ACC:" + docLibNumber);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "AdminDataBaseBuilderImportProcess", imagesDir, localDefnsDir,
                         defnsDir, bytesOut);
      serverUtils.totalBytes(req, unm, dnm, 7202, bytesOut[0], 0, "SID:" + docLibNumber);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\""
                           + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    int[] hmenuCount = new int[1];

    adminUtils.outputPageFrame(con, stmt, rs, out, req, "AdminDataBaseBuilderImport", "", "7202", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount, bytesOut);

    adminUtils.drawTitle(con, stmt, rs, req, out, "Administration - DataBase Builder: Import (" + tableName + ")", "7202", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<table id=\"page\" border=0 width=100%>");

    String fullPathName = libraryUtils.getDocumentNameGivenCode(docLibNumber, unm, dnm, localDefnsDir, defnsDir);

    RandomAccessFile fh;
    if((fh = generalUtils.fileOpen(fullPathName)) == null)
    {
      scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
      scoutln(out, bytesOut, "<tr><td><p>Unknown document: " + docLibNumber + "</td></tr>");
      scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
        
      scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
      scoutln(out, bytesOut, "</table>");     scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 

      serverUtils.totalBytes(req, unm, dnm, 7202, bytesOut[0], 0, docLibNumber);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    String outputFile = exportDir + tableName + ".asc";
    RandomAccessFile fho;
    if(createNewFile.equals("on"))
    {
      if((fho = generalUtils.create(outputFile)) == null)
      {
        scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
        scoutln(out, bytesOut, "<tr><td><p>Cannot create output file: " + outputFile + "</td></tr>");
        scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
        generalUtils.fileClose(fh);

        scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
        scoutln(out, bytesOut, "</table>");     scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 

        serverUtils.totalBytes(req, unm, dnm, 7202, bytesOut[0], 0, docLibNumber);

        if(con != null) con.close();
        if(out != null) out.flush();
        return;
      }
    }
    else
    {
      if((fho = generalUtils.fileOpen(outputFile)) == null)
      {
        if((fho = generalUtils.create(outputFile)) == null)
        {
          scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
          scoutln(out, bytesOut, "<tr><td><p>Cannot open (or create) output file: " + outputFile + "</td></tr>");
          scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
          generalUtils.fileClose(fh);

          scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
          scoutln(out, bytesOut, "</table>");     scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 

          serverUtils.totalBytes(req, unm, dnm, 7202, bytesOut[0], 0, docLibNumber);

          if(con != null) con.close();
          if(out != null) out.flush();
          return;
        }
      }
    }
    
    fho.seek(fho.length());
    
    String fieldNames="", fieldTypes="";
    if(tableName.equals("company"))
    {
      Customer customer = new Customer();
      fieldNames = customer.getFieldNames();
      fieldTypes = customer.getFieldTypes();
    }  
    else
    if(tableName.equals("po"))
    {
      PurchaseOrder purchaseOrder = new PurchaseOrder();
      fieldNames = purchaseOrder.getFieldNamesPO();
      fieldTypes = purchaseOrder.getFieldTypesPO();
    }  
    else
    if(tableName.equals("pol"))
    {
      PurchaseOrder purchaseOrder = new PurchaseOrder();
      fieldNames = purchaseOrder.getFieldNamesPOL();
      fieldTypes = purchaseOrder.getFieldTypesPOL();
    }  
    else
    if(tableName.equals("poll"))
    {
      PurchaseOrder purchaseOrder = new PurchaseOrder();
      fieldNames = purchaseOrder.getFieldNamesPOLL();
      fieldTypes = purchaseOrder.getFieldTypesPOLL();
    }  
    else
    if(tableName.equals("so"))
    {
      SalesOrder salesOrder = new SalesOrder();
      fieldNames = salesOrder.getFieldNamesSO();
    fieldTypes = salesOrder.getFieldTypesSO();
    }  
    else
    if(tableName.equals("sol"))
    {
      SalesOrder salesOrder = new SalesOrder();
      fieldNames = salesOrder.getFieldNamesSOL();
      fieldTypes = salesOrder.getFieldTypesSOL();
    }  
    else
    if(tableName.equals("soll"))
    {
      SalesOrder salesOrder = new SalesOrder();
      fieldNames = salesOrder.getFieldNamesSOLL();
      fieldTypes = salesOrder.getFieldTypesSOLL();
    }  
    else
    if(tableName.equals("stock"))
    {
      Inventory inventory = new Inventory();
      fieldNames = inventory.getFieldNamesStock();
      fieldTypes = inventory.getFieldTypesStock();
    }  
    else
    if(tableName.equals("stockx"))
    {
      Inventory inventory = new Inventory();
      fieldNames = inventory.getFieldNamesStockx();
      fieldTypes = inventory.getFieldTypesStockx();
    }  
    else
    if(tableName.equals("quote"))
    {
      Quotation quotation = new Quotation();
      fieldNames = quotation.getFieldNamesQuote();
      fieldTypes = quotation.getFieldTypesQuote();
    }  
    else
    if(tableName.equals("quotel"))
    {
      Quotation quotation = new Quotation();
      fieldNames = quotation.getFieldNamesQuoteL();
      fieldTypes = quotation.getFieldTypesQuoteL();
    }  
    else
    if(tableName.equals("quotell"))
    {
      Quotation quotation = new Quotation();
      fieldNames = quotation.getFieldNamesQuoteLL();
      fieldTypes = quotation.getFieldTypesQuoteLL();
    }  
    else
    if(tableName.equals("invoice"))
    {
      SalesInvoice salesInvoice = new SalesInvoice();
      fieldNames = salesInvoice.getFieldNamesInvoice();
      fieldTypes = salesInvoice.getFieldTypesInvoice();
    }  
    else
    if(tableName.equals("invoicel"))
    {
      SalesInvoice salesInvoice = new SalesInvoice();
      fieldNames = salesInvoice.getFieldNamesInvoiceL();
      fieldTypes = salesInvoice.getFieldTypesInvoiceL();
    }  
    else
    if(tableName.equals("invoicell"))
    {
      SalesInvoice salesInvoice = new SalesInvoice();
      fieldNames = salesInvoice.getFieldNamesInvoiceLL();
      fieldTypes = salesInvoice.getFieldTypesInvoiceLL();
    }  
    else
    if(tableName.equals("itemmove"))
    {
      InventoryAdjustment inventoryAdjustment = new InventoryAdjustment();
      fieldNames = inventoryAdjustment.getFieldNamesStockA();
      fieldTypes = inventoryAdjustment.getFieldTypesStockA();
    }  
    else
    if(tableName.equals("proforma"))
    {
      ProformaInvoice proformaInvoice = new ProformaInvoice();
      fieldNames = proformaInvoice.getFieldNamesProforma();
      fieldTypes = proformaInvoice.getFieldTypesProforma();
    }  
    else
    if(tableName.equals("proformal"))
    {
      ProformaInvoice proformaInvoice = new ProformaInvoice();
      fieldNames = proformaInvoice.getFieldNamesProformaL();
      fieldTypes = proformaInvoice.getFieldTypesProformaL();
    }  
    else
    if(tableName.equals("proformall"))
    {
      ProformaInvoice proformaInvoice = new ProformaInvoice();
      fieldNames = proformaInvoice.getFieldNamesProformaLL();
      fieldTypes = proformaInvoice.getFieldTypesProformaLL();
    }  
    else
    if(tableName.equals("debit"))
    {
      SalesDebitNote salesDebitNote = new SalesDebitNote();
      fieldNames = salesDebitNote.getFieldNamesDebitNote();
      fieldTypes = salesDebitNote.getFieldTypesDebitNote();
    }  
    else
    if(tableName.equals("debitl"))
    {
      SalesDebitNote salesDebitNote = new SalesDebitNote();
      fieldNames = salesDebitNote.getFieldNamesDebitNoteL();
      fieldTypes = salesDebitNote.getFieldTypesDebitNoteL();
    }  
    else
    if(tableName.equals("debitll"))
    {
      SalesDebitNote salesDebitNote = new SalesDebitNote();
      fieldNames = salesDebitNote.getFieldNamesDebitNoteLL();
      fieldTypes = salesDebitNote.getFieldTypesDebitNoteLL();
    }  
    else
    if(tableName.equals("pdebit"))
    {
      PurchaseDebitNote purchaseDebitNote = new PurchaseDebitNote();
      fieldNames = purchaseDebitNote.getFieldNamesPurchaseDebitNote();
      fieldTypes = purchaseDebitNote.getFieldTypesPurchaseDebitNote();
    }  
    else
    if(tableName.equals("pdebitl"))
    {
      PurchaseDebitNote purchaseDebitNote = new PurchaseDebitNote();
      fieldNames = purchaseDebitNote.getFieldNamesPurchaseDebitNoteL();
      fieldTypes = purchaseDebitNote.getFieldTypesPurchaseDebitNoteL();
    }  
    else
    if(tableName.equals("pdebitll"))
    {
      PurchaseDebitNote purchaseDebitNote = new PurchaseDebitNote();
      fieldNames = purchaseDebitNote.getFieldNamesPurchaseDebitNoteLL();
      fieldTypes = purchaseDebitNote.getFieldTypesPurchaseDebitNoteLL();
    }  
    else
    if(tableName.equals("pinvoice"))
    {
      PurchaseInvoice purchaseInvoice = new PurchaseInvoice();
      fieldNames = purchaseInvoice.getFieldNamesPurchaseInvoice();
      fieldTypes = purchaseInvoice.getFieldTypesPurchaseInvoice();
    }  
    else
    if(tableName.equals("pinvoicel"))
    {
      PurchaseInvoice purchaseInvoice = new PurchaseInvoice();
      fieldNames = purchaseInvoice.getFieldNamesPurchaseInvoiceL();
      fieldTypes = purchaseInvoice.getFieldTypesPurchaseInvoiceL();
    }  
    else
    if(tableName.equals("pinvoicell"))
    {
      PurchaseInvoice purchaseInvoice = new PurchaseInvoice();
      fieldNames = purchaseInvoice.getFieldNamesPurchaseInvoiceLL();
      fieldTypes = purchaseInvoice.getFieldTypesPurchaseInvoiceLL();
    }  
    else
    if(tableName.equals("credit"))
    {
      SalesCreditNote salesSalesCreditNote = new SalesCreditNote();
      fieldNames = salesSalesCreditNote.getFieldNamesSalesCreditNote();
      fieldTypes = salesSalesCreditNote.getFieldTypesSalesCreditNote();
    }  
    else
    if(tableName.equals("creditl"))
    {
      SalesCreditNote salesSalesCreditNote = new SalesCreditNote();
      fieldNames = salesSalesCreditNote.getFieldNamesSalesCreditNoteL();
      fieldTypes = salesSalesCreditNote.getFieldTypesSalesCreditNoteL();
    }  
    else
    if(tableName.equals("creditll"))
    {
      SalesCreditNote salesSalesCreditNote = new SalesCreditNote();
      fieldNames = salesSalesCreditNote.getFieldNamesSalesCreditNoteLL();
      fieldTypes = salesSalesCreditNote.getFieldTypesSalesCreditNoteLL();
    }  
    else
    if(tableName.equals("pcredit"))
    {
      PurchaseCreditNote purchaseCreditNote = new PurchaseCreditNote();
      fieldNames = purchaseCreditNote.getFieldNamesPurchaseCreditNote();
      fieldTypes = purchaseCreditNote.getFieldTypesPurchaseCreditNote();
    }  
    else
    if(tableName.equals("pcreditl"))
    {
      PurchaseCreditNote purchaseCreditNote = new PurchaseCreditNote();
      fieldNames = purchaseCreditNote.getFieldNamesPurchaseCreditNoteL();
      fieldTypes = purchaseCreditNote.getFieldTypesPurchaseCreditNoteL();
    }  
    else
    if(tableName.equals("pcreditll"))
    {
      PurchaseCreditNote purchaseCreditNote = new PurchaseCreditNote();
      fieldNames = purchaseCreditNote.getFieldNamesPurchaseCreditNoteLL();
      fieldTypes = purchaseCreditNote.getFieldTypesPurchaseCreditNoteLL();
    }  
    else
    if(tableName.equals("receipt"))
    {
      Receipt receipt = new Receipt();
      fieldNames = receipt.getFieldNamesReceipt();
      fieldTypes = receipt.getFieldTypesReceipt();
    }  
    else
    if(tableName.equals("receiptl"))
    {
      Receipt receipt = new Receipt();
      fieldNames = receipt.getFieldNamesReceiptL();
      fieldTypes = receipt.getFieldTypesReceiptL();
    }  
    else
    if(tableName.equals("payment"))
    {
      Payment payment = new Payment();
      fieldNames = payment.getFieldNamesPayment();
      fieldTypes = payment.getFieldTypesPayment();
    }  
    else
    if(tableName.equals("paymentl"))
    {
      Payment payment = new Payment();
      fieldNames = payment.getFieldNamesPaymentL();
      fieldTypes = payment.getFieldTypesPaymentL();
    }  
    else
    if(tableName.equals("do"))
    {
      DeliveryOrder deliveryOrder = new DeliveryOrder();
      fieldNames = deliveryOrder.getFieldNamesDO();
      fieldTypes = deliveryOrder.getFieldTypesDO();
    }  
    else
    if(tableName.equals("dol"))
    {
      DeliveryOrder deliveryOrder = new DeliveryOrder();
      fieldNames = deliveryOrder.getFieldNamesDOL();
      fieldTypes = deliveryOrder.getFieldTypesDOL();
    }  
    else
    if(tableName.equals("doll"))
    {
      DeliveryOrder deliveryOrder = new DeliveryOrder();
      fieldNames = deliveryOrder.getFieldNamesDOLL();
      fieldTypes = deliveryOrder.getFieldTypesDOLL();
    }  
    else
    if(tableName.equals("pl"))
    {
      PickingList pickingList = new PickingList();
      fieldNames = pickingList.getFieldNamesPL();
      fieldTypes = pickingList.getFieldTypesPL();
    }  
    else
    if(tableName.equals("pll"))
    {
      PickingList pickingList = new PickingList();
      fieldNames = pickingList.getFieldNamesPLL();
      fieldTypes = pickingList.getFieldTypesPLL();
    }  
    else
    if(tableName.equals("plll"))
    {
      PickingList pickingList = new PickingList();
      fieldNames = pickingList.getFieldNamesPLLL();
      fieldTypes = pickingList.getFieldTypesPLLL();
    }  
    else
    if(tableName.equals("gr"))
    {
      GoodsReceivedNote goodsReceivedNote = new GoodsReceivedNote();
      fieldNames = goodsReceivedNote.getFieldNamesGR();
      fieldTypes = goodsReceivedNote.getFieldTypesGR();
    }  
    else
    if(tableName.equals("grl"))
    {
      GoodsReceivedNote goodsReceivedNote = new GoodsReceivedNote();
      fieldNames = goodsReceivedNote.getFieldNamesGRL();
      fieldTypes = goodsReceivedNote.getFieldTypesGRL();
    }  
    else
    if(tableName.equals("lp"))
    {
      LocalPurchase localPurchase = new LocalPurchase();
      fieldNames = localPurchase.getFieldNamesLP();
      fieldTypes = localPurchase.getFieldTypesLP();
    }  
    else
    if(tableName.equals("lpl"))
    {
      LocalPurchase localPurchase = new LocalPurchase();
      fieldNames = localPurchase.getFieldNamesLPL();
      fieldTypes = localPurchase.getFieldTypesLPL();
    }  
    else
    if(tableName.equals("lpll"))
    {
      LocalPurchase localPurchase = new LocalPurchase();
      fieldNames = localPurchase.getFieldNamesLPLL();
      fieldTypes = localPurchase.getFieldTypesLPLL();
    }  
    else
    if(tableName.equals("oc"))
    {
      OrderConfirmation orderConfirmation = new OrderConfirmation();
      fieldNames = orderConfirmation.getFieldNamesOC();
      fieldTypes = orderConfirmation.getFieldTypesOC();
    }  
    else
    if(tableName.equals("ocl"))
    {
      OrderConfirmation orderConfirmation = new OrderConfirmation();
      fieldNames = orderConfirmation.getFieldNamesOCL();
      fieldTypes = orderConfirmation.getFieldTypesOCL();
    }  
    else
    if(tableName.equals("ocll"))
    {
      OrderConfirmation orderConfirmation = new OrderConfirmation();
      fieldNames = orderConfirmation.getFieldNamesOCLL();
      fieldTypes = orderConfirmation.getFieldTypesOCLL();
    }  
    else
    if(tableName.equals("supplier"))
    {
      Supplier supplier = new Supplier();
      fieldNames = supplier.getFieldNames();
      fieldTypes = supplier.getFieldTypes();
    }  
    else
    if(tableName.equals("stockc"))
    {
      InventoryAdjustment inventoryAdjustment = new InventoryAdjustment();
      fieldNames = inventoryAdjustment.getFieldNamesStockC();
      fieldTypes = inventoryAdjustment.getFieldTypesStockC();
    }  
    else
    if(tableName.equals("voucher"))
    {
      PaymentVoucher paymentVoucher = new PaymentVoucher();
      fieldNames = paymentVoucher.getFieldNamesVoucher();
      fieldTypes = paymentVoucher.getFieldTypesVoucher();
    }  
    else
    if(tableName.equals("voucherl"))
    {
      PaymentVoucher paymentVoucher = new PaymentVoucher();
      fieldNames = paymentVoucher.getFieldNamesVoucherL();
      fieldTypes = paymentVoucher.getFieldTypesVoucherL();
    }  
    else
    if(tableName.equals("voucherll"))
    {
      PaymentVoucher paymentVoucher = new PaymentVoucher();
      fieldNames = paymentVoucher.getFieldNamesVoucherLL();
      fieldTypes = paymentVoucher.getFieldTypesVoucherLL();
    }  
    else
    if(tableName.equals("rvoucher"))
    {
      ReceiptVoucher receiptVoucher = new ReceiptVoucher();
      fieldNames = receiptVoucher.getFieldNamesReceiptVoucher();
      fieldTypes = receiptVoucher.getFieldTypesReceiptVoucher();
    }  
    else
    if(tableName.equals("rvoucherl"))
    {
      ReceiptVoucher receiptVoucher = new ReceiptVoucher();
      fieldNames = receiptVoucher.getFieldNamesReceiptVoucherL();
      fieldTypes = receiptVoucher.getFieldTypesReceiptVoucherL();
    }  
    else
    if(tableName.equals("rvoucherll"))
    {
      ReceiptVoucher receiptVoucher = new ReceiptVoucher();
      fieldNames = receiptVoucher.getFieldNamesReceiptVoucherLL();
      fieldTypes = receiptVoucher.getFieldTypesReceiptVoucherLL();
    }  
    
    // for each line in the file
    byte[] tmp       = new byte[10000]; // plenty - max rec size    
    int line=0, x, fieldCount, len = fieldNames.length();
    String[] format = new String[1];  format[0] = "";
    
//    int numFlds = fieldTypes.length();
//    String s, firstField="";
    String field, entry, thisCode, lastCode="";
  
    for(x=0;x<skipLines;++x)
      getNextLine(fh, tmp, 10000);

    int count=skipLines, outputCount=0;
    
    while(getNextLine(fh, tmp, 10000))
    {    
      ++count;
      ++outputCount;

      System.out.print(" " + count);

      // increment 'line'
      thisCode = getCSVField(csvFields[1], tmp);  // code field assumed to be specified in the first fld of any steelclaws record
      if(! thisCode.equals(lastCode))
      {
        line = 0;
        lastCode = thisCode;
      }  
      ++line;

      // for each of the required fields
      x=0; fieldCount=1;
      while(x < len)
      {
        field="";
        while(x < len && fieldNames.charAt(x) != ',')
          field += fieldNames.charAt(x++);
        ++x;
        while(x < len && fieldNames.charAt(x) == ' ')
          ++x;
            
        if(csvFields[fieldCount] == 0) // a CSV field has NOT been specified
        {
          if(defaults[fieldCount].length() != 0)
          {
            fho.writeBytes("\""); 
            writeOutput(fho, defaults[fieldCount]); 
            fho.writeBytes("\","); 
          }
          else
          if(! rules[fieldCount].equals("None"))
          {
            entry = applyRule(rules[fieldCount], values[fieldCount], line, "", outputCount, tableName, format, dnm, tmp,
                              localDefnsDir, defnsDir);
            fho.writeBytes("\""); 
            writeOutput(fho, entry); 
            fho.writeBytes("\","); 
          }
          else
          {
            switch(fieldTypes.charAt(fieldCount-1))
            {
              case 'D' : fho.writeBytes("\"1970-01-01\","); break;
              case 'I' : 
              case 'F' : fho.writeBytes("\"0\",");          break;
              default  : fho.writeBytes("\"\",");           break;
            }  
          }
        }
        else // a CSV field has been specified
        {
          entry = getCSVField(csvFields[fieldCount], tmp);
          if(entry.length() == 0)
            entry = defaults[fieldCount];
          
          if(! rules[fieldCount].equals("None"))
          {
            entry = applyRule(rules[fieldCount], values[fieldCount], line, entry, outputCount, tableName, format, dnm, tmp,
                              localDefnsDir, defnsDir);
          }
          
          fho.writeBytes("\""); 
          writeOutput(fho, entry); 
          fho.writeBytes("\","); 
        }

        ++fieldCount;
      }    

      fho.writeBytes("\n"); 
    }
    
    generalUtils.fileClose(fh);
    generalUtils.fileClose(fho);
      
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td><p>Completed</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "</table>");     scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 7202, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), docLibNumber);
    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void writeOutput(RandomAccessFile fho, String s) 
  {
    try
    {
      int x=0, len = s.length();
      String t="";
      while(x < len)
      {
        if(s.charAt(x) == '"')
          t += "\"\"";
        else t += s.charAt(x);
        ++x;
      }
        
      fho.writeBytes(t);
    }
    catch(Exception e) { System.out.println(e); } 
  }
 
  // -------------------------------------------------------------------------------------------------------------------------------
  private String applyRule(String rule, String value, int line, String entry, int outputCount, String document, String[] format,
                           String dnm, byte[] tmp, String localDefnsDir, String defnsDir) throws Exception
  {
    String s="";
    
    if(rule.equals("RC5"))
    {
      if(outputCount < 10)
        s = "0000" + outputCount;   
      else
      if(outputCount < 100)
        s = "000" + outputCount;
      else
      if(outputCount < 1000)
        s = "00" + outputCount;
      else
      if(outputCount < 10000)
        s = "0" + outputCount;
      else s = "" + outputCount;
    }
    else
    if(rule.equals("RC6"))
    {
      if(outputCount < 10)
        s = "00000" + outputCount;   
      else
      if(outputCount < 100)
        s = "0000" + outputCount;
      else
      if(outputCount < 1000)
        s = "000" + outputCount;
      else
      if(outputCount < 10000)
        s = "00" + outputCount;
      else
      if(outputCount < 100000)
        s = "0" + outputCount;
      else s = "" + outputCount;
    }
    else
    if(rule.equals("asLine"))
    {
      s = "" + line;
    }
    else
    if(rule.equals("seqForCode"))
    {
      s = formatCode(document, line, format, dnm, localDefnsDir, defnsDir);
    }
    else
    if(rule.equals("fromDDMMYY"))
    {
      int len = entry.length();
      if(len == 0)
        return "1970-01-01";

      int x;
      String dd="", mm="", yy="";
      char separator='.';
      for(x=0;x<len;++x)
      {
        if(entry.charAt(x) < '0' || entry.charAt(x) > '9')
          separator = entry.charAt(x);
        ++x;
      }
      
      x=0;
      while(x < len && entry.charAt(x) != separator)
        dd += entry.charAt(x++);

      ++x;
      while(x < len && entry.charAt(x) != separator)
        mm += entry.charAt(x++);

      ++x;
      while(x < len && entry.charAt(x) != separator)
        yy += entry.charAt(x++);

      // if year is only two chars, prepend century
      if(yy.length() == 2)
      {
        if(generalUtils.strToInt(yy) < 50)
          s = "20";
        else s = "19";
      }

      s += (yy + "-" + mm + "-" + dd);
    }
    else
    if(rule.equals("fromMMDDYYYY"))
    {
      int len = entry.length();
      int x=0;
      while(x < len && entry.charAt(x) != ' ') // catch M$-Access timestamp dates
        ++x;
      len = x;      
      
      if(len == 0)
        return "1970-01-01";

      String dd="", mm="", yyyy="";
      char separator='/';
      for(x=0;x<len;++x)
      {
        if(entry.charAt(x) < '0' || entry.charAt(x) > '9')
          separator = entry.charAt(x);
        ++x;
      }
      
      x=0;
      while(x < len && entry.charAt(x) != separator)
        mm += entry.charAt(x++);

      ++x;
      while(x < len && entry.charAt(x) != separator)
        dd += entry.charAt(x++);

      ++x;
      while(x < len && entry.charAt(x) != ' ') // catch M$-Access timestamp dates
        yyyy += entry.charAt(x++);

      // if year is only two chars, prepend century
      if(yyyy.length() == 2)
      {
        if(generalUtils.strToInt(yyyy) < 50)
          s = "20";
        else s = "19";
      }

      s += (yyyy + "-" + mm + "-" + dd);
    }
    else
    if(rule.equals("fromDDMMYYYY"))
    {
      int len = entry.length();
      if(len == 0)
        return "1970-01-01";

      int x;
      String dd="", mm="", yyyy="";
      char separator='/';
      for(x=0;x<len;++x)
      {
        if(entry.charAt(x) < '0' || entry.charAt(x) > '9')
          separator = entry.charAt(x);
        ++x;
      }
      
      x=0;
      while(x < len && entry.charAt(x) != separator)
        dd += entry.charAt(x++);

      ++x;
      while(x < len && entry.charAt(x) != separator)
        mm += entry.charAt(x++);

      ++x;
      while(x < len && entry.charAt(x) != separator)
        yyyy += entry.charAt(x++);

      // if year is only two chars, prepend century
      if(yyyy.length() == 2)
      {
        if(generalUtils.strToInt(yyyy) < 50)
          s = "20";
        else s = "19";
      }

      s += (yyyy + "-" + mm + "-" + dd);
    }
    else
    if(rule.equals("fromYYYYMMDD"))
    {
      if(entry.length() == 0)
        return "1970-01-01";

      s = ("" + entry.charAt(0) + entry.charAt(1) + entry.charAt(2) + entry.charAt(3) + "-" + entry.charAt(4) + entry.charAt(5)
        + "-" + entry.charAt(6) + entry.charAt(7));
    }
    else
    if(rule.equals("appendField"))
    {
      String entry2 = getCSVField(generalUtils.strToInt(value), tmp);

      if(entry2.length() > 0) // just-in-case
    {
      return entry + entry2;
    }
      
      return entry;
    }
    else
    if(rule.equals("useXRef"))
    {

    
    }
    else s = entry;
    
    return s;  
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private boolean getNextLine(RandomAccessFile fh, byte[] buf, int bufSize) throws Exception
  {
    int x=0, red=0;
    boolean inQuote = false;

    long curr = fh.getFilePointer();
    long high = fh.length();

    fh.seek(curr);

    if(curr == high)
      return false;

    fh.read(buf, 0, 1);
    while(curr < high && x < (bufSize - 1))
    {
      if(buf[x] == '"')
      {
        if(inQuote)
          inQuote = false;
        else inQuote = true;
      }
      
      if((buf[x] == 10 || buf[x] == 13 || buf[x] == 26) && ! inQuote)
      {
        while(buf[x] == 10 || buf[x] == 13 || buf[x] == 26)
        {
          red = fh.read(buf, x, 1);
          if(red < 0)
            break;
        }

        if(buf[x] == 26)
          ;
        else
          if(red > 0)
            fh.seek(fh.getFilePointer() -1);

        buf[x] = '\000';

        return true;
      }

      ++x;
      fh.read(buf, x, 1);
      ++curr;
    }

    // remove trailing spaces
    x = bufSize - 1;
    while(buf[x] == 32)
      --x;
    buf[++x] = '\000';

    return true;
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------
  private int getEntry(byte[] line, int num, String[] entry)
  {
    boolean quit = false, inQuotes, complete;
    int z, y, count = 0, x = 0;

    while(true)
    {
      entry[0] = "";

      // loop until sat on first char of entry text contents
      quit = complete = inQuotes = false;
      while(! quit)
      {
        if(line[x] == '\000')
          return -888; // legal EOL
        
        if(line[x] == '"') // on opening quote, maybe
        {  
          if(inQuotes)
          {
            if(line[x + 1] == '"') // then we have "" == "
              ++x;
            else inQuotes = false; // on closing quote; end of entry
          }
          else inQuotes = true;
          
          ++x;
        }
        else
        if(line[x] == ',') // on separator of next entry; i.e., this entry is empty
        {  
          if(! inQuotes)
          {
            quit = true;          
            complete = true;
          }
          
          ++x;
        }
        else
        if(line[x] == ' ' || line[x] == '\t') // on whitespace between entries
        {  
          ++x;
          while(line[x] == ' ' || line[x] == '\t')
            ++x;
        }
        else // sat on entry contents
          quit = true;
      }
      
      y = 0;
      while(! complete)
      {
        if(line[x] == '\000')
        {  
          if(inQuotes)
            return -1; // illegal EOL
          return -888; // legal EOL
        }
        
        if(line[x] == '"') // hit closing quote, maybe
        {
          if(line[x + 1] == '"') // then we have "" == "
          {
            entry[0] += "\"";
            ++x; ++x;
          }
          else
          {
            complete = true;
            ++x;
            while(line[x] == ' ' || line[x] == '\t')
              ++x;
            if(line[x] == ',') // just-in-case: may be EOL
              ++x;
          }
        }
        else
        if(line[x] == ',')
        {
          if(inQuotes)
          {  
            entry[0] += ",";
            ++x;
          }
          else // on next separator, so entry contents complete
          {
            complete = true;
            ++x;
            while(line[x] == ' ' || line[x] == '\t')
              ++x;
          }
        }
        else entry[0] += (char)line[x++];
      }        
        
      if(count == num)
      {
        if(y > 0)
        {  
          --y;
          while(y > 0 && entry[0].charAt(y) == ' ') // remove trailing spaces
            --y;
          entry[0] = entry[0].substring(0, y+1);
          
          z = 0;
          while(z < y && entry[0].charAt(z) == ' ') // remove leading spaces
            ++z;
          entry[0] = entry[0].substring(z);
        }
        
        return 0;
      }
      
      ++count;
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private String getCSVField(int csvFieldNum, byte[] line) throws Exception
  {
    String[] entry = new String[1];
    
    if(getEntry(line, (csvFieldNum - 1), entry) == 0)
      return generalUtils.stripLeadingAndTrailingSpaces(entry[0]);
    
    return "";
  }

  //-------------------------------------------------------------------------------------------------------------------------------
  private String formatCode(String document, int value, String[] format, String dnm, String localDefnsDir, String defnsDir)
                            throws Exception
  {
    if(format[0].length() == 0) // first call
      format[0] = getFormat(document, dnm, localDefnsDir, defnsDir);

    byte[] valueB = new byte[20];
    byte[] formattedCode = new byte[21];

    documentUtils.formatValue(generalUtils.intToStr(value), format[0], valueB, formattedCode);
    
    return generalUtils.stringFromBytes(formattedCode, 0L);
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------
  private String getFormat(String document, String dnm, String localDefnsDir, String defnsDir)
  {
    Connection con = null;
    Statement stmt = null;
    ResultSet rs   = null;
      
    String format = "";
    
    try
    {
      String uName    = directoryUtils.getMySQLUserName();
      String passWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();
    
      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + uName + "&password=" + passWord);

      stmt = con.createStatement();
      rs = stmt.executeQuery("SELECT Format FROM codes WHERE shortName = '" + document + "'"); 
      if(rs.next()) // just-in-case
        format = rs.getString(1);
      else format = "???????";
      
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
      if(con  != null) con.close();            
    }
    catch(Exception e)
    {
      System.out.println("DocumentUtils: " + e);
      try
      {
        if(rs   != null) rs.close();     
        if(stmt != null) stmt.close();
        if(con  != null) con.close();
      }
      catch(Exception e2) { }
    }
    
    return format;
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
