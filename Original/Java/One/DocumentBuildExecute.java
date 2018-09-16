// =======================================================================================================================================================================================================
// System: ZaraStar: DocumentEngine: Process build doc from doc
// Module: DocumentBuildExecute.java
// Author: C.K.Harvey
// Copyright (c) 2003-09 Christopher Harvey. All Rights Reserved.
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

public class DocumentBuildExecute extends HttpServlet
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

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="", p3="", p5="", p6="", renumberLines="N", renumberEntries="N";

    try
    {
      directoryUtils.setContentHeaders2(res);
      out = res.getWriter();

      byte[] lines    = new byte[1000]; lines[0]    = '\000';
      int[]  linesLen = new int[1];     linesLen[0] = 1000;

      int thisEntryLen, inc;
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
        if(name.equals("p1")) // sourceDocCode
          p1 = value[0];
        else
        if(name.equals("p2")) // serviceCode
          p2 = value[0];
        else
        if(name.equals("p3")) // quantities reqd (only for conversion from SO)   
          p3 = value[0];
        else
        if(name.equals("p5")) // numLines
          p5 = value[0];
        else
        if(name.equals("p6")) // cashOrNot
          p6 = value[0];
        else
        if(name.equals("all"))
          ; // ignore
        else
        if(name.equals("renumberLines"))
          renumberLines = "Y";
        else
        if(name.equals("renumberEntries"))
          renumberEntries = "Y";
        else // must be (other) checkbox value
        {
          thisEntryLen = name.length() + 2;
          if((generalUtils.lengthBytes(lines, 0) + thisEntryLen) >= linesLen[0])
          {
            byte[] tmp = new byte[linesLen[0]];
            System.arraycopy(lines, 0, tmp, 0, linesLen[0]);
            if(thisEntryLen > 1000)
              inc = thisEntryLen;
            else inc = 1000;
            linesLen[0] += inc;
            lines = new byte[linesLen[0]];
            System.arraycopy(tmp, 0, lines, 0, linesLen[0] - inc);
          }

          generalUtils.catAsBytes(name + "\001", 0, lines, false);
        }
      }

      doIt(out, req, renumberLines, renumberEntries, lines, p1, p2, p3, p5, p6, unm, sid, uty, men, den, dnm, bnm, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "DocumentBuildExecute", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6095, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String renumberLines, String renumberEntries, byte[] lines, String p1, String p2, String p3, String p5, String p6, String unm, String sid, String uty, String men, String den,
                    String dnm, String bnm, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String imagesDir     = directoryUtils.getSupportDirs('I');
    
    Connection con = null;
    Statement stmt = null, stmt2 = null;
    ResultSet rs   = null, rs2 = null;

    try
    {
      String uName = directoryUtils.getMySQLUserName();
      String pWord = directoryUtils.getMySQLPassWord();
      Class.forName("com.mysql.jdbc.Driver").newInstance();
      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + uName + "&password=" + pWord);

      if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
      {
        messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "DocumentBuild", imagesDir, localDefnsDir, defnsDir, bytesOut);
        serverUtils.etotalBytes(req, unm, dnm, 6095, bytesOut[0], 0, "SID:" + p1);
        if(con  != null) con.close();
        if(out != null) out.flush();
        return;
      }

      process(con, stmt, stmt2, rs, rs2, out, req, renumberLines, renumberEntries, p1, p2, lines, p5, p3, p6, unm, sid, uty, men, den, dnm, bnm, imagesDir, localDefnsDir, defnsDir, bytesOut);
    }
    catch(Exception e) { } 

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 6095, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con  != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void process(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, HttpServletRequest req, String renumberLines, String renumberEntries, String sourceDocCode, String serviceCode, byte[] lines,
                       String numLinesStr, String quantitiesReqd, String cashOrNot, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String imagesDir, String localDefnsDir, String defnsDir, int[] bytesOut)
                       throws Exception
  {
    // sort the lines (they are in random order)
    int numLines = generalUtils.intFromStr(numLinesStr);
    int x=0, checkedLines=0;
    int[] linesI = new int[numLines];
    String line;

    int len = generalUtils.lengthBytes(lines, 0);
    while(x < len)
    {
      line="";
      while(lines[x] != '\001' && lines[x] != '\000')
        line += (char)lines[x++];
      linesI[checkedLines++] = generalUtils.intFromStr(line);
      ++x;
    }

    generalUtils.insertionSort(linesI, checkedLines);      

    byte[] dataAlready = new byte[1];  dataAlready[0] = '\000';
    byte[] newDocCode  = new byte[21];
    byte[] codeB       = new byte[21];
    generalUtils.strToBytes(codeB, sourceDocCode);

    if(serviceCode.equals("4022"))
    {
      QuotationToQuotation quotationToQuotation = new QuotationToQuotation();
      if(! quotationToQuotation.create(con, stmt, rs, codeB, renumberLines.charAt(0), renumberEntries.charAt(0), linesI, checkedLines, unm, dnm, localDefnsDir, defnsDir, newDocCode, bytesOut))
      {
        messagePage.msgScreen(false, out, req, generalUtils.intFromBytes(newDocCode, 0L), unm, sid, uty, men, den, dnm, bnm, serviceCode, "", "", "", imagesDir, localDefnsDir, defnsDir, bytesOut);
       return;
      } 
    
      Quotation quotation = new Quotation();
      quotation.getRecToHTML(con, stmt, stmt2, rs, rs2, out, false, 'D', newDocCode, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, 'A', "", dataAlready, req, bytesOut);
    }
    else
    if(serviceCode.equals("4033"))
    {
      QuotationToSalesOrder quotationToSalesOrder = new QuotationToSalesOrder();
      if(! quotationToSalesOrder.create(con, stmt, rs, codeB, renumberLines.charAt(0), renumberEntries.charAt(0), linesI, checkedLines, unm, dnm, localDefnsDir, defnsDir, newDocCode, bytesOut))
      {
        messagePage.msgScreen(false, out, req, generalUtils.intFromBytes(newDocCode, 0L), unm, sid, uty, men, den, dnm, bnm, serviceCode, "", "", "", imagesDir, localDefnsDir, defnsDir, bytesOut);
        return;
      }
  
      SalesOrder salesOrder = new SalesOrder();
      salesOrder.getRecToHTML(con, stmt, stmt2, rs, rs2, out, false, 'D', newDocCode, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, 'A', "", dataAlready, req, bytesOut);
    }
    else  
    if(serviceCode.equals("4034"))
    {
      SalesOrderToSalesOrder salesOrderToSalesOrder = new SalesOrderToSalesOrder();
      if(! salesOrderToSalesOrder.create(con, stmt, rs, codeB, renumberLines.charAt(0), renumberEntries.charAt(0), linesI, checkedLines, unm, dnm, localDefnsDir, defnsDir, newDocCode, bytesOut))
      {
        messagePage.msgScreen(false, out, req, generalUtils.intFromBytes(newDocCode, 0L), unm, sid, uty, men, den, dnm, bnm, serviceCode, "", "", "", imagesDir, localDefnsDir, defnsDir, bytesOut);
        return;
      }
  
      SalesOrder salesOrder = new SalesOrder();
      salesOrder.getRecToHTML(con, stmt, stmt2, rs, rs2, out, false, 'D', newDocCode, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, 'A', "", dataAlready, req, bytesOut);
    }
    else    
    if(serviceCode.equals("4433"))
    {
      SalesOrderToWorksOrder salesOrderToWorksOrder = new SalesOrderToWorksOrder();
      if(! salesOrderToWorksOrder.create(con, stmt, rs, codeB, renumberLines.charAt(0), renumberEntries.charAt(0), linesI, checkedLines, unm, dnm, localDefnsDir, defnsDir, newDocCode, bytesOut))
      {
        messagePage.msgScreen(false, out, req, generalUtils.intFromBytes(newDocCode, 0L), unm, sid, uty, men, den, dnm, bnm, serviceCode, "", "", "", imagesDir, localDefnsDir, defnsDir, bytesOut);
        return;
      }
  
      SalesOrder salesOrder = new SalesOrder();
      salesOrder.getRecToHTML(con, stmt, stmt2, rs, rs2, out, false, 'D', newDocCode, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, 'A', "", dataAlready, req, bytesOut);
    }
    else
    if(serviceCode.equals("5072"))
    {
      SalesOrderToPurchaseOrder salesOrderToPurchaseOrder = new SalesOrderToPurchaseOrder();
      if(! salesOrderToPurchaseOrder.create(con, stmt, rs, codeB, renumberLines.charAt(0), renumberEntries.charAt(0), linesI, checkedLines, quantitiesReqd, unm, dnm, localDefnsDir, defnsDir, newDocCode, bytesOut))
      {
        messagePage.msgScreen(false, out, req, generalUtils.intFromBytes(newDocCode, 0L), unm, sid, uty, men, den, dnm, bnm, serviceCode, "", "", "", imagesDir, localDefnsDir, defnsDir, bytesOut);
        return;
      }

      PurchaseOrder purchaseOrder = new PurchaseOrder();
      purchaseOrder.getRecToHTML(con, stmt, stmt2, rs, rs2, out, 'D', newDocCode, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, 'A', "", dataAlready, req, bytesOut);
    }
    else
    if(serviceCode.equals("5024"))
    {
      SalesOrderToLocalPurchase salesOrderToLocalPurchase = new SalesOrderToLocalPurchase();
      if(! salesOrderToLocalPurchase.create(con, stmt, rs, codeB, renumberLines.charAt(0), renumberEntries.charAt(0), linesI, checkedLines, quantitiesReqd, unm, dnm, localDefnsDir, defnsDir, newDocCode, bytesOut))
      {
        messagePage.msgScreen(false, out, req, generalUtils.intFromBytes(newDocCode, 0L), unm, sid, uty, men, den, dnm, bnm, serviceCode, "", "", "", imagesDir, localDefnsDir, defnsDir, bytesOut);
        return;
      }

      LocalPurchase localPurchase = new LocalPurchase();
      localPurchase.getRecToHTML(con, stmt, stmt2, rs, rs2, out, 'D', newDocCode, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, 'A', "", dataAlready, req, bytesOut);
    }
    else
    if(serviceCode.equals("4045"))
    {
      SalesOrderToOrderConfirmation salesOrderToOrderConfirmation = new SalesOrderToOrderConfirmation();
      if(! salesOrderToOrderConfirmation.create(con, stmt, rs, codeB, renumberLines.charAt(0), renumberEntries.charAt(0), linesI, checkedLines, quantitiesReqd, unm, dnm, localDefnsDir, defnsDir, newDocCode, bytesOut))
      {
        messagePage.msgScreen(false, out, req, generalUtils.intFromBytes(newDocCode, 0L), unm, sid, uty, men, den, dnm, bnm, serviceCode, "", "", "", imagesDir, localDefnsDir, defnsDir, bytesOut);
        return;
      }
  
      OrderConfirmation orderConfirmation = new OrderConfirmation();
      orderConfirmation.getRecToHTML(con, stmt, stmt2, rs, rs2, out, 'D', newDocCode, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, 'A', "", dataAlready, req, bytesOut);
    }
    else
    if(serviceCode.equals("4132"))
    {
      SalesOrderToOrderAcknowledgement salesOrderToOrderAcknowledgement = new SalesOrderToOrderAcknowledgement();
      if(! salesOrderToOrderAcknowledgement.create(con, stmt, rs, codeB, renumberLines.charAt(0), renumberEntries.charAt(0), linesI, checkedLines, quantitiesReqd, unm, dnm, localDefnsDir, defnsDir, newDocCode, bytesOut))
      {
        messagePage.msgScreen(false, out, req, generalUtils.intFromBytes(newDocCode, 0L), unm, sid, uty, men, den, dnm, bnm, serviceCode, "", "", "", imagesDir, localDefnsDir, defnsDir, bytesOut);
        return;
      }
  
      OrderAcknowledgement orderAcknowledgement = new OrderAcknowledgement();
      orderAcknowledgement.getRecToHTML(con, stmt, stmt2, rs, rs2, out, 'D', newDocCode, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, 'A', "", dataAlready, req, bytesOut);
    }
    else
    if(serviceCode.equals("4082"))
    {
      SalesOrderProformaInvoice salesOrderProformaInvoice = new SalesOrderProformaInvoice();
      if(! salesOrderProformaInvoice.create(con, stmt, rs, codeB, renumberLines.charAt(0), renumberEntries.charAt(0), linesI, checkedLines, quantitiesReqd, unm, dnm, localDefnsDir, defnsDir, newDocCode, bytesOut))
      {
        messagePage.msgScreen(false, out, req, generalUtils.intFromBytes(newDocCode, 0L), unm, sid, uty, men, den, dnm, bnm, serviceCode, "", "", "", imagesDir, localDefnsDir, defnsDir, bytesOut);
        return;
      }
   
      ProformaInvoice proformaInvoice = new ProformaInvoice();
      proformaInvoice.getRecToHTML(con, stmt, stmt2, rs, rs2, out, 'D', newDocCode, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, 'A', "", dataAlready, req, bytesOut);
    }
    else
    if(serviceCode.equals("3042"))
    {
      SalesOrderPickingList salesOrderPickingList = new SalesOrderPickingList();
      if(! salesOrderPickingList.create(con, stmt, rs, codeB, renumberLines.charAt(0), renumberEntries.charAt(0), linesI, checkedLines, quantitiesReqd, unm, dnm, localDefnsDir, defnsDir, newDocCode, bytesOut))
      {
        messagePage.msgScreen(false, out, req, generalUtils.intFromBytes(newDocCode, 0L), unm, sid, uty, men, den, dnm, bnm, serviceCode, "", "", "", imagesDir, localDefnsDir, defnsDir, bytesOut);
        return;
      }
  
      PickingList pickingList = new PickingList();
      pickingList.getRecToHTML(con, stmt, stmt2, rs, rs2, out, 'D', newDocCode, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, 'A', "", dataAlready, req, bytesOut);
    }
    else
    if(serviceCode.equals("4056"))
    {
      QuotationToDeliveryOrder quotationToDeliveryOrder = new QuotationToDeliveryOrder();
      if(! quotationToDeliveryOrder.create(con, stmt, rs, codeB, renumberLines.charAt(0), renumberEntries.charAt(0), linesI, checkedLines, unm, dnm, localDefnsDir, defnsDir, newDocCode, bytesOut))
      {
        messagePage.msgScreen(false, out, req, generalUtils.intFromBytes(newDocCode, 0L), unm, sid, uty, men, den, dnm, bnm, serviceCode, "", "", "", imagesDir, localDefnsDir, defnsDir, bytesOut);
        return;
      }
  
      DeliveryOrder deliveryOrder = new DeliveryOrder();
      deliveryOrder.getRecToHTML(con, stmt, stmt2, rs, rs2, out, false, "D",  'D', newDocCode, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, 'A', "", dataAlready, req, bytesOut);
    } 
    else
    if(serviceCode.equals("4057"))
    {
      PickingListToDeliveryOrder pickingListToDeliveryOrder = new PickingListToDeliveryOrder();
      if(! pickingListToDeliveryOrder.create(con, stmt, rs, codeB, renumberLines.charAt(0), renumberEntries.charAt(0), linesI, checkedLines, unm, dnm, localDefnsDir, defnsDir, newDocCode, bytesOut))
      {
        messagePage.msgScreen(false, out, req, generalUtils.intFromBytes(newDocCode, 0L), unm, sid, uty, men, den, dnm, bnm, serviceCode, "", "", "", imagesDir, localDefnsDir, defnsDir, bytesOut);
        return;
      }

      DeliveryOrder deliveryOrder = new DeliveryOrder();
      deliveryOrder.getRecToHTML(con, stmt, stmt2, rs, rs2, out, false, "D", 'D', newDocCode, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, 'A', "", dataAlready, req, bytesOut);
    }
    else
    if(serviceCode.equals("4058"))
    {
      SalesOrderToDeliveryOrder salesOrderToDeliveryOrder = new SalesOrderToDeliveryOrder();
      if(! salesOrderToDeliveryOrder.create(con, stmt, rs, codeB, renumberLines.charAt(0), renumberEntries.charAt(0), linesI, checkedLines, quantitiesReqd, unm, dnm, localDefnsDir, defnsDir, newDocCode, bytesOut))
      {
        messagePage.msgScreen(false, out, req, generalUtils.intFromBytes(newDocCode, 0L), unm, sid, uty, men, den, dnm, bnm, serviceCode, "", "", "", imagesDir, localDefnsDir, defnsDir, bytesOut);
        return;
      }

      DeliveryOrder deliveryOrder = new DeliveryOrder();
      deliveryOrder.getRecToHTML(con, stmt, stmt2, rs, rs2, out, false, "D", 'D', newDocCode, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, 'A', "", dataAlready, req, bytesOut);
    }
    else    
    if(serviceCode.equals("4068"))
    {
      PickingListToSalesInvoiceCash pickingListToSalesInvoiceCash = new PickingListToSalesInvoiceCash();
      if(! pickingListToSalesInvoiceCash.create(con, stmt, rs, codeB, renumberLines.charAt(0), renumberEntries.charAt(0), linesI, checkedLines, unm, dnm, localDefnsDir, defnsDir, newDocCode, bytesOut))
      {
        messagePage.msgScreen(false, out, req, generalUtils.intFromBytes(newDocCode, 0L), unm, sid, uty, men, den, dnm, bnm, serviceCode, "", "", "", imagesDir, localDefnsDir, defnsDir, bytesOut);
        return;
      }
  
      SalesInvoice salesInvoice = new SalesInvoice();
      salesInvoice.getRecToHTML(con, stmt, stmt2, rs, rs2, out, false, 'D', newDocCode, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, 'A', "", "C", dataAlready, req, bytesOut);
    } 
    else    
    if(serviceCode.equals("4090"))
    {
      ProformaInvoiceToSalesInvoice proformaInvoiceToSalesInvoice = new ProformaInvoiceToSalesInvoice();
      if(! proformaInvoiceToSalesInvoice.create(con, stmt, rs, codeB, renumberLines.charAt(0), renumberEntries.charAt(0), linesI, checkedLines, unm, dnm, localDefnsDir, defnsDir, newDocCode, bytesOut))
      {
        messagePage.msgScreen(false, out, req, generalUtils.intFromBytes(newDocCode, 0L), unm, sid, uty, men, den, dnm, bnm, serviceCode, "", "", "", imagesDir, localDefnsDir, defnsDir, bytesOut);
        return;
      }
  
      SalesInvoice salesInvoice = new SalesInvoice();
      salesInvoice.getRecToHTML(con, stmt, stmt2, rs, rs2, out, false, 'D', newDocCode, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, 'A', "", "C", dataAlready, req, bytesOut);
    } 
    else
    if(serviceCode.equals("4069"))
    {
      PickingListToSalesInvoiceTerms pickingListToSalesInvoiceTerms = new PickingListToSalesInvoiceTerms();
      if(! pickingListToSalesInvoiceTerms.create(con, stmt, rs, codeB, renumberLines.charAt(0), renumberEntries.charAt(0), linesI, checkedLines, unm, dnm, localDefnsDir, defnsDir, newDocCode, bytesOut))
      {
        messagePage.msgScreen(false, out, req, generalUtils.intFromBytes(newDocCode, 0L), unm, sid, uty, men, den, dnm, bnm, serviceCode, "", "", "", imagesDir, localDefnsDir, defnsDir, bytesOut);
        return;
      }
  
      SalesInvoice salesInvoice = new SalesInvoice();
      salesInvoice.getRecToHTML(con, stmt, stmt2, rs, rs2, out, false, 'D', newDocCode, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, 'A', "", "N", dataAlready, req, bytesOut);
    } 
    else   
    if(serviceCode.equals("4021"))
    {
      DeliveryOrderToQuotation deliveryOrderToQuotation = new DeliveryOrderToQuotation();
      if(! deliveryOrderToQuotation.create(con, stmt, rs, codeB, renumberLines.charAt(0), renumberEntries.charAt(0), linesI, checkedLines, unm, dnm, localDefnsDir, defnsDir, newDocCode, bytesOut))
      {
        messagePage.msgScreen(false, out, req, generalUtils.intFromBytes(newDocCode, 0L), unm, sid, uty, men, den, dnm, bnm, serviceCode, "", "", "", imagesDir, localDefnsDir, defnsDir, bytesOut);
        return;
      }
  
      Quotation quotation = new Quotation();
      quotation.getRecToHTML(con, stmt, stmt2, rs, rs2, out, false, 'D', newDocCode, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, 'A', "", dataAlready, req, bytesOut);
    } 
    else    
    if(serviceCode.equals("4065"))
    {
      SalesOrderToQuotation salesOrderToQuotation = new SalesOrderToQuotation();
      if(! salesOrderToQuotation.create(con, stmt, rs, codeB, renumberLines.charAt(0), renumberEntries.charAt(0), linesI, checkedLines, unm, dnm, localDefnsDir, defnsDir, newDocCode, bytesOut))
      {
        messagePage.msgScreen(false, out, req, generalUtils.intFromBytes(newDocCode, 0L), unm, sid, uty, men, den, dnm, bnm, serviceCode, "", "", "", imagesDir, localDefnsDir, defnsDir, bytesOut);
        return;
      }
  
      Quotation quotation = new Quotation();
      quotation.getRecToHTML(con, stmt, stmt2, rs, rs2, out, false, 'D', newDocCode, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, 'A', "", dataAlready, req, bytesOut);
    } 
    else
    if(serviceCode.equals("4070"))
    {
      DeliveryOrderToSalesInvoice deliveryOrderToSalesInvoice = new DeliveryOrderToSalesInvoice();
      if(! deliveryOrderToSalesInvoice.create(con, stmt, rs, codeB, renumberLines.charAt(0), renumberEntries.charAt(0), linesI, checkedLines, unm, dnm, localDefnsDir, defnsDir, newDocCode, bytesOut))
      {
        messagePage.msgScreen(false, out, req, generalUtils.intFromBytes(newDocCode, 0L), unm, sid, uty, men, den, dnm, bnm, serviceCode, "", "", "", imagesDir, localDefnsDir, defnsDir, bytesOut);
        return;
      }

      SalesInvoice salesInvoice = new SalesInvoice();
      salesInvoice.getRecToHTML(con, stmt, stmt2, rs, rs2, out, false, 'D', newDocCode, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, 'A', "", "N", dataAlready, req, bytesOut);
    }
    else
    if(serviceCode.equals("4099"))
    {
      SalesOrderToSalesInvoice salesOrderToSalesInvoice = new SalesOrderToSalesInvoice();
      if(! salesOrderToSalesInvoice.create(con, stmt, rs, codeB, renumberLines.charAt(0), renumberEntries.charAt(0), linesI, checkedLines, cashOrNot, unm, dnm, localDefnsDir, defnsDir, newDocCode, bytesOut))
      {
        messagePage.msgScreen(false, out, req, generalUtils.intFromBytes(newDocCode, 0L), unm, sid, uty, men, den, dnm, bnm, serviceCode, "", "", "", imagesDir, localDefnsDir, defnsDir, bytesOut);
        return;
      }

      SalesInvoice salesInvoice = new SalesInvoice();
      salesInvoice.getRecToHTML(con, stmt, stmt2, rs, rs2, out, false, 'D', newDocCode, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, 'A', "", "N", dataAlready, req, bytesOut);
    }
    else
    if(serviceCode.equals("4083"))
    {
      SalesInvoiceToProformaInvoice salesInvoiceToProformaInvoice = new SalesInvoiceToProformaInvoice();
      if(! salesInvoiceToProformaInvoice.create(con, stmt, rs, codeB, renumberLines.charAt(0), renumberEntries.charAt(0), linesI, checkedLines, unm, dnm, localDefnsDir, defnsDir, newDocCode, bytesOut))
      {
        messagePage.msgScreen(false, out, req, generalUtils.intFromBytes(newDocCode, 0L), unm, sid, uty, men, den, dnm, bnm, serviceCode, "", "", "", imagesDir, localDefnsDir, defnsDir, bytesOut);
        return;
      }

      ProformaInvoice proformaInvoice = new ProformaInvoice();
      proformaInvoice.getRecToHTML(con, stmt, stmt2, rs, rs2, out, 'D', newDocCode, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, 'A', "", dataAlready, req, bytesOut);
    } 
    else
    if(serviceCode.equals("3140"))
    {
      InboxToQuote inboxToQuote = new InboxToQuote();
      if(! inboxToQuote.create(con, stmt, rs, codeB, renumberLines.charAt(0), linesI, checkedLines, unm, dnm, localDefnsDir, defnsDir, newDocCode, bytesOut))
      {
        messagePage.msgScreen(false, out, req, generalUtils.intFromBytes(newDocCode, 0L), unm, sid, uty, men, den, dnm, bnm, serviceCode, "", "", "", imagesDir, localDefnsDir, defnsDir, bytesOut);
        return;
      }

      Quotation quotation = new Quotation();
      quotation.getRecToHTML(con, stmt, stmt2, rs, rs2, out, false, 'D', newDocCode, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, 'A', "", dataAlready, req, bytesOut);
    } 
    else
    if(serviceCode.equals("3141"))
    {
      InboxToSalesOrder inboxToSalesOrder = new InboxToSalesOrder();
      if(! inboxToSalesOrder.create(con, stmt, rs, codeB, renumberLines.charAt(0), linesI, checkedLines, unm, dnm, localDefnsDir, defnsDir, newDocCode, bytesOut))
      {
        messagePage.msgScreen(false, out, req, generalUtils.intFromBytes(newDocCode, 0L), unm, sid, uty, men, den, dnm, bnm, serviceCode, "", "", "", imagesDir, localDefnsDir, defnsDir, bytesOut);
        return;
      }

      SalesOrder salesOrder = new SalesOrder();
      salesOrder.getRecToHTML(con, stmt, stmt2, rs, rs2, out, false, 'D', newDocCode, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, 'A', "", dataAlready, req, bytesOut);
    } 
    else
    if(serviceCode.equals("3035"))
    {
      GoodsReceivedInvoice goodsReceivedInvoice = new GoodsReceivedInvoice();
      if(! goodsReceivedInvoice.create(con, stmt, rs, codeB, renumberLines.charAt(0), renumberEntries.charAt(0), linesI, checkedLines, unm, dnm, localDefnsDir, defnsDir, newDocCode, bytesOut))
      {
        messagePage.msgScreen(false, out, req, generalUtils.intFromBytes(newDocCode, 0L), unm, sid, uty, men, den, dnm, bnm, serviceCode, "", "", "", imagesDir, localDefnsDir, defnsDir, bytesOut);
        return;
      }

      PurchaseInvoice purchaseInvoice = new PurchaseInvoice();
      purchaseInvoice.getRecToHTML(con, stmt, stmt2, rs, rs2, out, 'D', newDocCode, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, 'A', "", dataAlready, req, bytesOut);
    } 
  }

}
