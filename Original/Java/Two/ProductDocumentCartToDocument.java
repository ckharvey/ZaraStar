// =======================================================================================================================================================================================================
// System: ZaraStar Document: Cart to doc
// Module: ProductDocumentCartToDocument.java
// Author: C.K.Harvey
// Copyright (c) 2000-08 Christopher Harvey. All Rights Reserved.
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

public class ProductDocumentCartToDocument extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  DocumentUtils documentUtils = new DocumentUtils();
  AccountsUtils accountsUtils = new AccountsUtils();
  AdminControlUtils adminControlUtils = new AdminControlUtils();
  Quotation quotation = null;
  SalesOrder salesOrder = null;
  PickingList pickingList = null;
  DeliveryOrder deliveryOrder = null;
  SalesInvoice salesInvoice = null;
  ProformaInvoice proformaInvoice = null;
  PurchaseOrder purchaseOrder = null;
  LocalPurchase localPurchase = null;

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", men="", den="", uty="", dnm="", bnm="", p1="", p2="";

    try
    {
      directoryUtils.setContentHeaders2(res);
      out = res.getWriter();
      
      int len = req.getContentLength();
      ServletInputStream in  = req.getInputStream();
      byte[] b = new byte[len];
      
      in.readLine(b, 0, len);          
          
      String name, value;
      int x=0;
      while(x < len)
      {
        ++x; // & or ?
        name="";
        while(x < len && b[x] != '=')
          name += (char)b[x++];
        
        ++x; // =
        value="";
        while(x < len && b[x] != '&')
          value += (char)b[x++];
        value = generalUtils.deSanitise(value);
          
        if(name.equals("unm"))
          unm = value;
        else
        if(name.equals("sid"))
          sid = value;
        else
        if(name.equals("uty"))
          uty = value;
        else
        if(name.equals("men"))
          men = value;
        else
        if(name.equals("den"))
          den = value;
        else
        if(name.equals("dnm"))
          dnm = value;
        else
        if(name.equals("bnm"))
          bnm = value;
        else
        if(name.equals("p1")) // which
          p1 = value;
        else
        if(name.equals("p2")) // docName (used to allocate new doccode)
          p2 = value;
      }

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1.charAt(0), p2, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "ProductDocumentCartToDocument", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 121, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                    char which, String docName, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');
    
    Connection con = null;
    Statement stmt = null, stmt2 = null;
    ResultSet rs   = null, rs2   = null;
    
    String uName    = directoryUtils.getMySQLUserName();
    String passWord = directoryUtils.getMySQLPassWord();

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    
    con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + uName + "&password=" + passWord);

    if((uty.equals("R") && ! adminControlUtils.notDisabled(con, stmt, rs, 908)) || (uty.equals("A") && ! adminControlUtils.notDisabled(con, stmt, rs, 808)) || (uty.equals("I") && ! authenticationUtils.verifyAccess(con, stmt, rs, req, 121, unm, uty, dnm, localDefnsDir, defnsDir)))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "121g", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 121, bytesOut[0], 0, "ACC:" + docName);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }
    
    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "121g", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 121, bytesOut[0], 0, "SID:" + docName);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    String baseCurrency = accountsUtils.getBaseCurrency(con, stmt, rs, dnm, localDefnsDir, defnsDir);

    String cartTable = cartTableName(unm);

    byte[] newDocCode  = new byte[21];
    create(con, stmt, rs, which, docName, cartTable, baseCurrency, unm, dnm, localDefnsDir, defnsDir, newDocCode);

    byte[] dataAlready = new byte[1];  dataAlready[0] = '\000';
  
    switch(which)
    {
      case 'Q' : quotation.getRecToHTML(con, stmt, stmt2, rs, rs2, out, false, 'D', newDocCode, unm, sid, uty, men, den, dnm, bnm, localDefnsDir,
                                    defnsDir, 'A', "", dataAlready, req, bytesOut);
                 break;
      case 'S' : salesOrder.getRecToHTML(con, stmt, stmt2, rs, rs2, out, false, 'D', newDocCode, unm, sid, uty, men, den, dnm, bnm, localDefnsDir,
                                    defnsDir, 'A', "", dataAlready, req, bytesOut);
                 break;
      case 'Y' : purchaseOrder.getRecToHTML(con, stmt, stmt2, rs, rs2, out, 'D', newDocCode, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir,
                                    'A', "", dataAlready, req, bytesOut);
                 break;
      case 'Z' : localPurchase.getRecToHTML(con, stmt, stmt2, rs, rs2, out, 'D', newDocCode, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir,
                                    'A', "", dataAlready, req, bytesOut);
                 break;
      case 'R' : proformaInvoice.getRecToHTML(con, stmt, stmt2, rs, rs2, out, 'D', newDocCode, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir,
                                    'A', "", dataAlready, req, bytesOut);
                 break;
      case 'P' : pickingList.getRecToHTML(con, stmt, stmt2, rs, rs2, out, 'D', newDocCode, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, 'A', "",
                                    dataAlready, req, bytesOut);
                 break;
      case 'D' : deliveryOrder.getRecToHTML(con, stmt, stmt2, rs, rs2, out, false, "D", 'D', newDocCode, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, 'A', "",
                                    dataAlready, req, bytesOut);
                 break;
      case 'I' : salesInvoice.getRecToHTML(con, stmt, stmt2, rs, rs2, out, false, 'D', newDocCode, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, 'A', "",
                                    "C", dataAlready, req, bytesOut);
                 break;
    } 

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 121, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), docName);
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean create(Connection con, Statement stmt, ResultSet rs, char which, String docName, String cartTable, String baseCurrency, String unm,
                         String dnm, String localDefnsDir, String defnsDir, byte[] newDocCode) throws Exception
  {
    documentUtils.getNextCode(con, stmt, rs, docName, true, newDocCode);
    generalUtils.toUpper(newDocCode, 0);

    byte[] toBuf = new byte[3000];

    int lineFld = 0, entryFld = 0, itemCodeFld = 0, mfrFld = 0, mfrCodeFld = 0, descFld = 0, qtyFld = 0, uomFld = 0, unitPriceFld = 0, amountFld = 0,
         amount2Fld = 0, currencyFld = 0, rateFld = 0, signOnFld = 0, gstRateFld = 0, numOtherDateFlds = 0, numOtherNumericFlds = 0, dateFld = 0,
         companyCodeFld = 0;
    int[] otherDateFlds    = new int[50]; // plenty
    int[] otherNumericFlds = new int[50]; // plenty
    String fieldNames = "", fieldTypes = "";
  
    switch(which)
    {
      case 'Q' : quotation = new Quotation();
                 fieldTypes = quotation.getFieldTypesQuote();
                 numOtherDateFlds    = getDateFieldPositions(fieldTypes,    otherDateFlds);
                 numOtherNumericFlds = getNumericFieldPositions(fieldTypes, otherNumericFlds);
                 fieldNames  = quotation.getFieldNamesQuote();
                 dateFld        = getFieldPositionGivenName("QuoteDate",   fieldNames);
                 companyCodeFld = getFieldPositionGivenName("CompanyCode", fieldNames);
                 currencyFld    = getFieldPositionGivenName("Currency",    fieldNames);
                 rateFld        = getFieldPositionGivenName("Rate",        fieldNames);
                 break;
      case 'S' : salesOrder = new SalesOrder();
                 fieldTypes = salesOrder.getFieldTypesSO();
                 numOtherDateFlds    = getDateFieldPositions(fieldTypes,    otherDateFlds);
                 numOtherNumericFlds = getNumericFieldPositions(fieldTypes, otherNumericFlds);
                 fieldNames  = salesOrder.getFieldNamesSO();
                 dateFld        = getFieldPositionGivenName("Date",        fieldNames);
                 companyCodeFld = getFieldPositionGivenName("CompanyCode", fieldNames);
                 currencyFld    = getFieldPositionGivenName("Currency2",   fieldNames);
                 rateFld        = getFieldPositionGivenName("Rate",        fieldNames);
                 break;
      case 'P' : pickingList = new PickingList();
                 fieldTypes = pickingList.getFieldTypesPL();
                 numOtherDateFlds    = getDateFieldPositions(fieldTypes,    otherDateFlds);
                 numOtherNumericFlds = getNumericFieldPositions(fieldTypes, otherNumericFlds);
                 fieldNames  = pickingList.getFieldNamesPL();
                 dateFld        = getFieldPositionGivenName("Date",        fieldNames);
                 companyCodeFld = getFieldPositionGivenName("CompanyCode", fieldNames);
                 currencyFld    = getFieldPositionGivenName("Currency",    fieldNames);
                 rateFld        = getFieldPositionGivenName("Rate",        fieldNames);
                 break;
      case 'D' : deliveryOrder = new DeliveryOrder();
                 fieldTypes = deliveryOrder.getFieldTypesDO();
                 numOtherDateFlds    = getDateFieldPositions(fieldTypes,    otherDateFlds);
                 numOtherNumericFlds = getNumericFieldPositions(fieldTypes, otherNumericFlds);
                 fieldNames  = deliveryOrder.getFieldNamesDO();
                 dateFld        = getFieldPositionGivenName("Date",        fieldNames);
                 companyCodeFld = getFieldPositionGivenName("CompanyCode", fieldNames);
                 currencyFld    = getFieldPositionGivenName("Currency",    fieldNames);
                 rateFld        = getFieldPositionGivenName("Rate",        fieldNames);
                 break;
      case 'R' : proformaInvoice = new ProformaInvoice();
                 fieldTypes = proformaInvoice.getFieldTypesProforma();
                 numOtherDateFlds    = getDateFieldPositions(fieldTypes,    otherDateFlds);
                 numOtherNumericFlds = getNumericFieldPositions(fieldTypes, otherNumericFlds);
                 fieldNames  = proformaInvoice.getFieldNamesProforma();
                 dateFld        = getFieldPositionGivenName("Date",        fieldNames);
                 companyCodeFld = getFieldPositionGivenName("CompanyCode", fieldNames);
                 currencyFld    = getFieldPositionGivenName("Currency",    fieldNames);
                 rateFld        = getFieldPositionGivenName("Rate",        fieldNames);
                 break;
      case 'I' : salesInvoice = new SalesInvoice();
                 fieldTypes = salesInvoice.getFieldTypesInvoice();
                 numOtherDateFlds    = getDateFieldPositions(fieldTypes,    otherDateFlds);
                 numOtherNumericFlds = getNumericFieldPositions(fieldTypes, otherNumericFlds);
                 fieldNames  = salesInvoice.getFieldNamesInvoice();
                 dateFld        = getFieldPositionGivenName("Date",        fieldNames);
                 companyCodeFld = getFieldPositionGivenName("CompanyCode", fieldNames);
                 currencyFld    = getFieldPositionGivenName("Currency",    fieldNames);
                 rateFld        = getFieldPositionGivenName("Rate",        fieldNames);
                 break;
      case 'Y' : purchaseOrder = new PurchaseOrder();
                 fieldTypes = purchaseOrder.getFieldTypesPO();
                 numOtherDateFlds    = getDateFieldPositions(fieldTypes,    otherDateFlds);
                 numOtherNumericFlds = getNumericFieldPositions(fieldTypes, otherNumericFlds);
                 fieldNames  = purchaseOrder.getFieldNamesPO();
                 dateFld        = getFieldPositionGivenName("Date",        fieldNames);
                 companyCodeFld = getFieldPositionGivenName("CompanyCode", fieldNames);
                 currencyFld    = getFieldPositionGivenName("Currency",    fieldNames);
                 rateFld        = getFieldPositionGivenName("Rate",        fieldNames);
                 break;
      case 'Z' : localPurchase = new LocalPurchase();
                 fieldTypes = localPurchase.getFieldTypesLP();
                 numOtherDateFlds    = getDateFieldPositions(fieldTypes,    otherDateFlds);
                 numOtherNumericFlds = getNumericFieldPositions(fieldTypes, otherNumericFlds);
                 fieldNames  = localPurchase.getFieldNamesLP();
                 dateFld        = getFieldPositionGivenName("Date",        fieldNames);
                 companyCodeFld = getFieldPositionGivenName("CompanyCode", fieldNames);
                 currencyFld    = getFieldPositionGivenName("Currency",    fieldNames);
                 rateFld        = getFieldPositionGivenName("Rate",        fieldNames);
                 break;
    }
    
    buildHeader(newDocCode, baseCurrency, dateFld, companyCodeFld, currencyFld, rateFld, numOtherDateFlds, otherDateFlds, numOtherNumericFlds, otherNumericFlds, toBuf, localDefnsDir, defnsDir);
//generalUtils.pb("",toBuf,0,500);    
    switch(which)
    {
      case 'Q' : quotation.putRecHeadGivenCode(con, stmt, rs, newDocCode, 'N', toBuf, dnm, localDefnsDir, defnsDir);
                 serverUtils.syncToIW(con, generalUtils.stringFromBytes(newDocCode, 0L), "", "", 'Q', generalUtils.todaySQLFormat(localDefnsDir, defnsDir), unm, "-", dnm);
                 break;
      case 'S' : salesOrder.putRecHeadGivenCode(con, stmt, rs, newDocCode, 'N', toBuf, dnm, localDefnsDir, defnsDir);
                 serverUtils.syncToIW(con, generalUtils.stringFromBytes(newDocCode, 0L), "", "", 'S', generalUtils.todaySQLFormat(localDefnsDir, defnsDir), unm, "-", dnm);
                 break;
      case 'P' : pickingList.putRecHeadGivenCode(con, stmt, rs, newDocCode, 'N', toBuf, dnm, localDefnsDir, defnsDir);
                 serverUtils.syncToIW(con, generalUtils.stringFromBytes(newDocCode, 0L), "", "", 'P', generalUtils.todaySQLFormat(localDefnsDir, defnsDir), unm, "-", dnm);
                 break;
      case 'D' : deliveryOrder.putRecHeadGivenCode(con, stmt, rs, newDocCode, 'N', toBuf, dnm, localDefnsDir, defnsDir);
                 serverUtils.syncToIW(con, generalUtils.stringFromBytes(newDocCode, 0L), "", "", 'D', generalUtils.todaySQLFormat(localDefnsDir, defnsDir), unm, "-", dnm);
                 break;
      case 'R' : proformaInvoice.putRecHeadGivenCode(con, stmt, rs, newDocCode, 'N', toBuf, dnm, localDefnsDir, defnsDir);
                 serverUtils.syncToIW(con, generalUtils.stringFromBytes(newDocCode, 0L), "", "", 'R', generalUtils.todaySQLFormat(localDefnsDir, defnsDir), unm, "-", dnm);
                 break;
      case 'I' : salesInvoice.putRecHeadGivenCode(con, stmt, rs, newDocCode, 'N', toBuf, dnm, localDefnsDir, defnsDir);
                 serverUtils.syncToIW(con, generalUtils.stringFromBytes(newDocCode, 0L), "", "", 'I', generalUtils.todaySQLFormat(localDefnsDir, defnsDir), unm, "-", dnm);
                 break;
      case 'Y' : purchaseOrder.putRecHeadGivenCode(con, stmt, rs, newDocCode, 'N', toBuf, dnm, localDefnsDir, defnsDir);
                 serverUtils.syncToIW(con, generalUtils.stringFromBytes(newDocCode, 0L), "", "", 'Y', generalUtils.todaySQLFormat(localDefnsDir, defnsDir), unm, "-", dnm);
                 break;
      case 'Z' : localPurchase.putRecHeadGivenCode(con, stmt, rs, newDocCode, 'N', toBuf, dnm, localDefnsDir, defnsDir);
                 serverUtils.syncToIW(con, generalUtils.stringFromBytes(newDocCode, 0L), "", "", 'Z', generalUtils.todaySQLFormat(localDefnsDir, defnsDir), unm, "-", dnm);
                 break;
    }
    

    switch(which)
    {
      case 'Q' : fieldTypes = quotation.getFieldTypesQuoteL();              
                 fieldNames = quotation.getFieldNamesQuoteL();
                 qtyFld     = getFieldPositionGivenName("Quantity", fieldNames);
                 break;
      case 'S' : fieldTypes = salesOrder.getFieldTypesSOL();              
                 fieldNames = salesOrder.getFieldNamesSOL();
                 qtyFld     = getFieldPositionGivenName("Quantity", fieldNames);
                 break;
      case 'P' : fieldTypes = pickingList.getFieldTypesPLL();              
                 fieldNames = pickingList.getFieldNamesPLL();
                 qtyFld     = getFieldPositionGivenName("QuantityRequired", fieldNames);
                 break;
      case 'D' : fieldTypes = deliveryOrder.getFieldTypesDOL();              
                 fieldNames = deliveryOrder.getFieldNamesDOL();
                 qtyFld     = getFieldPositionGivenName("Quantity", fieldNames);
                 break;
      case 'R' : fieldTypes = proformaInvoice.getFieldTypesProformaL();              
                 fieldNames = proformaInvoice.getFieldNamesProformaL();
                 qtyFld     = getFieldPositionGivenName("Quantity", fieldNames);
                 break;
      case 'I' : fieldTypes = salesInvoice.getFieldTypesInvoiceL();              
                 fieldNames = salesInvoice.getFieldNamesInvoiceL();
                 qtyFld     = getFieldPositionGivenName("Quantity", fieldNames);
                 break;
      case 'Y' : fieldTypes = purchaseOrder.getFieldTypesPOL();              
                 fieldNames = purchaseOrder.getFieldNamesPOL();
                 qtyFld     = getFieldPositionGivenName("Quantity", fieldNames);
                 break;
      case 'Z' : fieldTypes = localPurchase.getFieldTypesLPL();              
                 fieldNames = localPurchase.getFieldNamesLPL();
                 qtyFld     = getFieldPositionGivenName("Quantity", fieldNames);
                 break;
    }
    
    numOtherDateFlds    = getDateFieldPositions(fieldTypes,    otherDateFlds);
    numOtherNumericFlds = getNumericFieldPositions(fieldTypes, otherNumericFlds);
    lineFld      = getFieldPositionGivenName("Line",             fieldNames);
    entryFld     = getFieldPositionGivenName("Entry",            fieldNames);
    itemCodeFld  = getFieldPositionGivenName("ItemCode",         fieldNames);
    mfrFld       = getFieldPositionGivenName("Manufacturer",     fieldNames);
    mfrCodeFld   = getFieldPositionGivenName("ManufacturerCode", fieldNames);
    descFld      = getFieldPositionGivenName("Description",      fieldNames);
    uomFld       = getFieldPositionGivenName("UoM",              fieldNames);
    unitPriceFld = getFieldPositionGivenName("UnitPrice",        fieldNames);
    amountFld    = getFieldPositionGivenName("Amount",           fieldNames);
    amount2Fld   = getFieldPositionGivenName("Amount2",          fieldNames);
    signOnFld    = getFieldPositionGivenName("SignOn",           fieldNames);
    gstRateFld   = getFieldPositionGivenName("GSTRate",          fieldNames);
    
    forAllCartLines(con, stmt, rs, cartTable, which, newDocCode, lineFld, entryFld, itemCodeFld, mfrFld, mfrCodeFld, descFld, qtyFld, uomFld,
                    unitPriceFld, amountFld, amount2Fld, signOnFld, gstRateFld, numOtherDateFlds, otherDateFlds, numOtherNumericFlds,
                    otherNumericFlds, toBuf, unm, dnm, localDefnsDir, defnsDir);

    return true;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void buildHeader(byte[] newDocCode, String baseCurrency, int dateFld, int companyCodeFld, int currencyFld, int rateFld, int numOtherDateFlds, int[] otherDateFlds, int numOtherNumericFlds, int[] otherNumericFlds, byte[] toBuf,
                           String localDefnsDir, String defnsDir) throws Exception
  {
    generalUtils.zeroize(toBuf, 3000);

    int x;
    
    for(x=0;x<numOtherDateFlds;++x)
      generalUtils.repAlpha(toBuf, 3000, (short)otherDateFlds[x], "1970-01-01");
                
    for(x=0;x<numOtherNumericFlds;++x)
      generalUtils.repAlpha(toBuf, 3000, (short)otherNumericFlds[x], "0");

    generalUtils.repAlpha(toBuf, 3000, (short)0, newDocCode);

    generalUtils.repAlpha(toBuf, 3000, (short)dateFld,        generalUtils.todaySQLFormat(localDefnsDir, defnsDir));
    generalUtils.repAlpha(toBuf, 3000, (short)companyCodeFld, "-");
    generalUtils.repAlpha(toBuf, 3000, (short)currencyFld,    baseCurrency);
    generalUtils.repAlpha(toBuf, 3000, (short)rateFld,        "1");
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void forAllCartLines(Connection con, Statement stmt, ResultSet rs, String cartTable, char which, byte[] newDocCode, int lineFld,
                               int entryFld, int itemCodeFld, int mfrFld, int mfrCodeFld, int descFld, int qtyFld, int uomFld, int unitPriceFld,
                               int amountFld, int amount2Fld, int signOnFld, int gstRateFld, int numOtherDateFlds, int[] otherDateFlds,
                               int numOtherNumericFlds, int[] otherNumericFlds, byte[] toBuf, String unm, String dnm, String localDefnsDir,
                               String defnsDir) throws Exception
  {
    String gstRate = accountsUtils.getDefaultGSTRate(con, stmt, rs, dnm, localDefnsDir, defnsDir);

    try
    {
      stmt = con.createStatement();
  
      rs = stmt.executeQuery("SELECT Line, Entry, ItemCode, Mfr, MfrCode, Description, Quantity, UoM, Price FROM " + cartTable
                           + " ORDER BY Line");

      String line, entry, itemCode, mfr, mfrCode, desc, qty, uom, price;

      while(rs.next())                  
      {
        line     = rs.getString(1);
        entry    = rs.getString(2);
        itemCode = rs.getString(3);
        mfr      = rs.getString(4);
        mfrCode  = rs.getString(5);
        desc     = rs.getString(6);
        qty      = rs.getString(7);
        uom      = rs.getString(8);
        price    = rs.getString(9);
        
        if(uom      == null) uom = "";
        if(price    == null) price = "";

        buildLine(newDocCode, line, entry, itemCode, mfr, mfrCode, desc, qty, uom, price, gstRate, lineFld, entryFld, itemCodeFld, mfrFld,
                  mfrCodeFld, descFld, qtyFld, uomFld, unitPriceFld, amountFld, amount2Fld, signOnFld, gstRateFld, numOtherDateFlds, otherDateFlds,
                  numOtherNumericFlds, otherNumericFlds, toBuf, unm);
        
        switch(which)
        {
          case 'Q' : quotation.putRecLine(con, stmt, rs, newDocCode, null, 'N', toBuf, dnm, localDefnsDir, defnsDir); break;
          case 'S' : salesOrder.putRecLine(con, stmt, newDocCode, null, 'N', toBuf, dnm, localDefnsDir, defnsDir); break;
          case 'P' : pickingList.putRecLine(con, stmt, rs, newDocCode, null, 'N', toBuf, dnm, localDefnsDir, defnsDir); break;
          case 'D' : deliveryOrder.putRecLine(con, stmt, rs, newDocCode, null, 'N', toBuf, dnm, localDefnsDir, defnsDir); break;
          case 'R' : proformaInvoice.putRecLine(con, stmt, rs, newDocCode, null, 'N', toBuf, dnm, localDefnsDir, defnsDir); break;
          case 'I' : salesInvoice.putRecLine(con, stmt, rs, newDocCode, null, 'N', toBuf, dnm, localDefnsDir, defnsDir); break;
          case 'Y' : purchaseOrder.putRecLine(con, stmt, rs, newDocCode, null, 'N', toBuf, dnm, localDefnsDir, defnsDir); break;
          case 'Z' : localPurchase.putRecLine(con, stmt, rs, newDocCode, null, 'N', toBuf, dnm, localDefnsDir, defnsDir); break;
        }
      }
                 
      if(rs   != null) rs.close();                                 
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(rs   != null) rs.close();                                 
      if(stmt != null) stmt.close();
    }    
  }  

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void buildLine(byte[] newDocCode, String line, String entry, String itemCode, String mfr, String mfrCode, String desc, String qty,
                         String uom, String price, String gstRate, int lineFld, int entryFld, int itemCodeFld, int mfrFld, int mfrCodeFld,
                         int descFld, int qtyFld, int uomFld, int unitPriceFld, int amountFld, int amount2Fld, int signOnFld, int gstRateFld,
                         int numOtherDateFlds, int[] otherDateFlds, int numOtherNumericFlds, int[] otherNumericFlds, byte[] toBuf, String unm)
                         throws Exception
  {
    generalUtils.zeroize(toBuf, 3000);
    
    int x;
    
    for(x=0;x<numOtherDateFlds;++x)
      generalUtils.repAlpha(toBuf, 3000, (short)otherDateFlds[x], "1970-01-01");
                
    for(x=0;x<numOtherNumericFlds;++x)
      generalUtils.repAlpha(toBuf, 3000, (short)otherNumericFlds[x], "0");

    generalUtils.repAlpha(toBuf, 3000, (short)0,            newDocCode);
    generalUtils.repAlpha(toBuf, 3000, (short)lineFld,      line);
    
    if(entry.length() == 0) entry = line;
    generalUtils.repAlpha(toBuf, 3000, (short)entryFld,     entry);
    
    generalUtils.repAlpha(toBuf, 3000, (short)itemCodeFld,  itemCode);
    generalUtils.repAlpha(toBuf, 3000, (short)mfrFld,       mfr);
    generalUtils.repAlpha(toBuf, 3000, (short)mfrCodeFld,   mfrCode);
    generalUtils.repAlpha(toBuf, 3000, (short)descFld,      desc);
    generalUtils.repAlpha(toBuf, 3000, (short)gstRateFld,   gstRate);
    generalUtils.repAlpha(toBuf, 3000, (short)qtyFld,       qty);
    generalUtils.repAlpha(toBuf, 3000, (short)uomFld,       uom);
    generalUtils.repAlpha(toBuf, 3000, (short)unitPriceFld, price);
    
    String amt = generalUtils.doubleDPs('2', generalUtils.doubleFromStr(price) * generalUtils.doubleFromStr(qty));
    generalUtils.repAlpha(toBuf, 3000, (short)amountFld,    amt);
    generalUtils.repAlpha(toBuf, 3000, (short)amount2Fld,   amt);
    generalUtils.repAlpha(toBuf, 3000, (short)signOnFld,    unm);
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private int getFieldPositionGivenName(String reqdFieldName, String fieldNames) throws Exception
  {     
    String fieldName; 
    int posn = 0, x = 0, len = fieldNames.length();
    while(x < len)
    {
      fieldName = "";
      while(x < len && fieldNames.charAt(x) != ',')
        fieldName += fieldNames.charAt(x++);

      if(fieldName.equals(reqdFieldName))
        return posn;
      
      while(x < len && (fieldNames.charAt(x) == ',' || fieldNames.charAt(x) == ' '))
        ++x;

      ++posn;
    }
    
    return -1; // just-in-case
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // "CCCFFFCFSFDICCCCCCFCCC";
  private int getDateFieldPositions(String fieldTypes, int[] posns) throws Exception
  {     
    int count = 0, x = 0, len = fieldTypes.length();
    while(x < len)
    {
      if(fieldTypes.charAt(x) == 'D')
        posns[count++] = x;
      ++x;
    }
    
    return count;
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // "CCCFFFCFSFDICCCCCCFCCC";
  private int getNumericFieldPositions(String fieldTypes, int[] posns) throws Exception
  {     
    int count = 0, x = 0, len = fieldTypes.length();
    while(x < len)
    {
      if(fieldTypes.charAt(x) == 'I' || fieldTypes.charAt(x) == 'F')
        posns[count++] = x;
      ++x;
    }
    
    return count;
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private String cartTableName(String unm) throws Exception
  {
    String tmpTable = "";

    if(unm.startsWith("_")) // anon user
      tmpTable = unm.substring(1) + "_cart_tmp";
    else
    {
      int i = unm.indexOf("_");
      if(i != -1) // registered user
        tmpTable = unm.substring(0, i) + "_cart_tmp";
      else tmpTable = unm + "_cart_tmp";
    }

    return tmpTable;
  }

}
