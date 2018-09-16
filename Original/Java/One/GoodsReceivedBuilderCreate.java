// =======================================================================================================================================================================================================
// System: ZaraStar Document: GR builder create GRN rec
// Module: GoodsReceivedBuilderCreate.java
// Author: C.K.Harvey
// Copyright (c) 2006-09 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.io.*;
import java.sql.*;
import java.net.*;

public class GoodsReceivedBuilderCreate extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  Supplier supplier = new Supplier();
  GoodsReceivedNote goodsReceivedNote = new GoodsReceivedNote();
  LocalPurchase localPurchase = new LocalPurchase();
  Inventory inventory = new Inventory();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", note="", supplierCode="", lpCodes="", lpLines="";

    try
    {
      directoryUtils.setContentHeaders(res);
      out = res.getWriter();

      String[] quantities       = new String[100];  int quantitiesLen       = 100;
      String[] suppDOCodes      = new String[100];  int suppDOCodesLen      = 100;
      String[] suppInvoiceCodes = new String[100];  int suppInvoiceCodesLen = 100;

      int x, len, thisEntry, numLines=0;

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
        if(name.equals("note"))
          note = value[0];
        else
   //     if(name.equals("store"))
      //    store = value[0];
     //   else
        if(name.equals("supplierCode"))
          supplierCode = value[0];
        else
        if(name.equals("lpCodes"))
          lpCodes = value[0];
        else
        if(name.equals("lpLines"))
          lpLines = value[0];
        else
        {
          if(name.startsWith("a"))
          {
            thisEntry = generalUtils.strToInt(name.substring(1));
            if(thisEntry >= quantitiesLen) // outside of the current size
            {
              String[] tmp = new String[quantitiesLen];
              for(x=0;x<quantitiesLen;++x)
                tmp[x] = quantities[x];
              len = quantitiesLen;
              quantitiesLen = thisEntry + 1;
              quantities = new String[quantitiesLen];
              for(x=0;x<len;++x)
                quantities[x] = tmp[x];
            }

            ++numLines;
            quantities[thisEntry] = value[0];
          }
          else
          if(name.startsWith("b"))
          {
            thisEntry = generalUtils.strToInt(name.substring(1));
            if(thisEntry >= suppDOCodesLen) // outside of the current size
            {
              String[] tmp = new String[suppDOCodesLen];
              for(x=0;x<suppDOCodesLen;++x)
                tmp[x] = suppDOCodes[x];
              len = suppDOCodesLen;
              suppDOCodesLen = thisEntry + 1;
              suppDOCodes = new String[suppDOCodesLen];
              for(x=0;x<len;++x)
                suppDOCodes[x] = tmp[x];
            }

            suppDOCodes[thisEntry] = value[0];
          }
          else
          if(name.startsWith("c"))
          {
            thisEntry = generalUtils.strToInt(name.substring(1));
            if(thisEntry >= suppInvoiceCodesLen) // outside of the current size
            {
              String[] tmp = new String[suppInvoiceCodesLen];
              for(x=0;x<suppInvoiceCodesLen;++x)
                tmp[x] = suppInvoiceCodes[x];
              len = suppInvoiceCodesLen;
              suppInvoiceCodesLen = thisEntry + 1;
              suppInvoiceCodes = new String[suppInvoiceCodesLen];
              for(x=0;x<len;++x)
                suppInvoiceCodes[x] = tmp[x];
            }

            suppInvoiceCodes[thisEntry] = value[0];
          }
        }
      }

      doIt(out, req, numLines, supplierCode, note,// store,
          lpCodes, lpLines, quantities, suppDOCodes, suppInvoiceCodes, unm, sid, uty, men, den, bnm, dnm, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "GoodsReceivedBuilderCreate", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3034, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush(); 
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, int numLines, String supplierCode, String note, String lpCodes, String lpLines, String[] quantities, String[] suppDOCodes, String[] suppInvoiceCodes, String unm, String sid, String uty,
                    String men, String den, String bnm, String dnm, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String imagesDir     = directoryUtils.getSupportDirs('I');

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 3033, unm, uty, dnm, localDefnsDir, defnsDir)) // temp change from 3034
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "GoodsReceivedBuilderPage", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3034, bytesOut[0], 0, "ACC:");
      if(con  != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "GoodsReceivedBuilderPage", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3034, bytesOut[0], 0, "SID:");
      if(con  != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    byte[] nextCode = new byte[21];  nextCode[0] = '\000';

    if(process(con, stmt, rs, numLines, supplierCode, note, lpCodes, lpLines, quantities, suppDOCodes, suppInvoiceCodes, unm, dnm, nextCode, localDefnsDir, defnsDir, bytesOut))
      getRec(out, unm, sid, uty, men, den, dnm, bnm, generalUtils.stringFromBytes(nextCode, 0L), localDefnsDir);
    else messagePage.msgScreen(false, out, req, 23, unm, sid, uty, men, den, dnm, bnm, "GoodsReceivedBuilderPage", imagesDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 3034, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), generalUtils.stringFromBytes(nextCode, 0L));
    if(con  != null) con.close();
    if(out != null) out.flush(); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean process(Connection con, Statement stmt, ResultSet rs, int numLines, String supplierCode, String note, String lpCodes, String lpLines, String[] quantities, String[] suppDOCodes, String[] suppInvoiceCodes, String unm, String dnm,
                          byte[] nextCode, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    // check that at least one line has value
    if(! analyzeLines(quantities, numLines))
      return false;

    if(createGRNRec(con, stmt, rs, supplierCode, unm, dnm, note, lpCodes, localDefnsDir, defnsDir, nextCode))
    {
      byte[] lpCode          = new byte[21];
      byte[] lpLine          = new byte[10];
      byte[] thisLine        = new byte[10];
      int count=0, lineCount=1;

      while(count < numLines)
      {
        try // just-in-case
        {
          if(generalUtils.doubleFromStr(quantities[count]) != 0.0)
          {
            generalUtils.dfsGivenSeparator(false, '\001', lpCodes, (short)count, lpCode);
            generalUtils.dfsGivenSeparator(false, '\001', lpLines, (short)count, lpLine);

            generalUtils.intToBytesCharFormat(lineCount, thisLine, (short)0);

            createGRNRecLine(con, stmt, rs, lpCode, lpLine, nextCode, thisLine, quantities[count], suppDOCodes[count], suppInvoiceCodes[count], unm, dnm, localDefnsDir, defnsDir, bytesOut);

            ++lineCount;
          }
        }
        catch(Exception e) { System.out.println("3034a qtys[count] " + count + " " + quantities[count]); }

        ++count;
      }
    }
    else // failed to create header
    {
      return false;
    }

    return true;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean analyzeLines(String[] quantities, int numLines) throws Exception
  {
    boolean atLeastOneHasQty = false;

    int count=0;

    while(! atLeastOneHasQty && count < numLines)
    {
      if(generalUtils.doubleFromStr(quantities[count]) != 0.0)
        atLeastOneHasQty = true;
      ++count;
    }

    return atLeastOneHasQty;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean createGRNRec(Connection con, Statement stmt, ResultSet rs, String supplierCode, String unm, String dnm, String note, String lpCodes, String localDefnsDir, String defnsDir, byte[] nextCode) throws Exception
  {
    byte[] supplierCodeB = new byte[21];
    generalUtils.strToBytes(supplierCodeB, supplierCode);

    byte[] supplierName = new byte[81];
    supplier.getASupplierFieldGivenCode(con, stmt, rs, "Name", supplierCodeB, supplierName);

    if(generalUtils.lengthBytes(supplierName, 0) == 0) // suppCode is a dash (or an unknown code)
    {
       byte[] lpCode = new byte[21];
       generalUtils.dfsGivenSeparator(false, '\001', lpCodes, (short)0, lpCode); // pickup from the first lp - the assumption being that all lps are from the same supplier
       localPurchase.getAnLPFieldGivenCode(con, stmt, rs, "CompanyName", lpCode, supplierName, dnm, localDefnsDir, defnsDir);
    }

    return goodsReceivedNote.saveNewGRRecHeader(con, stmt, rs, supplierCodeB, supplierName, unm, note, dnm, localDefnsDir, defnsDir, nextCode);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void createGRNRecLine(Connection con, Statement stmt, ResultSet rs, byte[] lpCode, byte[] lpLine, byte[] nextCode, byte[] thisLine, String qty, String suppDOCode, String suppInvoiceCode, String unm, String dnm, String localDefnsDir,
                                String defnsDir, int[] bytesOut) throws Exception
  {
    byte[] data = new byte[1000];

    if(localPurchase.getLine(con, stmt, rs, lpCode, lpLine, '\000', data, dnm, localDefnsDir, defnsDir, bytesOut) == -1) // just-in-case
      return;

    String itemCode  = generalUtils.dfsAsStr(data, (short)1);
    String desc      = generalUtils.dfsAsStr(data, (short)2);
    String unitPrice = generalUtils.dfsAsStr(data, (short)3);
    String store     = generalUtils.dfsAsStr(data, (short)24);
    String mfr       = generalUtils.dfsAsStr(data, (short)30);
    String mfrCode   = generalUtils.dfsAsStr(data, (short)31);

    byte[] b = new byte[20];
    double amt = 0;

    try
    {
      amt = generalUtils.doubleFromStr(unitPrice);
    }
    catch(Exception e) { System.out.println("3034a amt " + amt); }

    double quantity = 0;
    try
    {
      quantity = generalUtils.doubleFromStr(qty);
    }
    catch(Exception e) { System.out.println("3034a qty " + quantity); }

    amt *= quantity;
    generalUtils.doubleToBytesCharFormat(amt, b, 0);

    goodsReceivedNote.saveNewGRRecLine(con, stmt, rs, nextCode, thisLine, itemCode, desc, unitPrice, qty, b, lpCode, lpLine, store, suppDOCode, suppInvoiceCode, mfr, mfrCode, unm, dnm, localDefnsDir, defnsDir);

    // create stockx rec if none exists for this itemcode
    if(! inventory.existsStockxRecGivenCodes(con, stmt, rs, itemCode, store, dnm, localDefnsDir, defnsDir))
    {
      if(inventory.isAStockItem(con, stmt, rs, itemCode))
        inventory.createNewStockXRec(con, stmt, rs, itemCode, store, unm, dnm, localDefnsDir, defnsDir);
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void getRec(PrintWriter out, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String code, String localDefnsDir) throws Exception
  {
    URL url = new URL("http://" + serverUtils.serverToCall("OFSA", localDefnsDir) + "/central/servlet/GoodsReceivedPage");

    HttpURLConnection uc = (HttpURLConnection)url.openConnection();
    uc.setDoInput(true);
    uc.setDoOutput(true);
    uc.setUseCaches(false);
    uc.setDefaultUseCaches(false);
    uc.setRequestProperty("Content-Type", "application/x-www-form.urlencoded");

    String s2 = "&unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1="  + generalUtils.sanitise(code) + "&p2=A&p3=&p4=";

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
