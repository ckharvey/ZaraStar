// =======================================================================================================================================================================================================
// System: ZaraStar DocumentEngine: Build doc from doc
// Module: DocumentBuild.java
// Author: C.K.Harvey
// Copyright (c) 2001-08 Christopher Harvey. All Rights Reserved.
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
import java.net.*;

public class DocumentBuild extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  MiscDefinitions miscDefinitions = new MiscDefinitions();  
  DrawingUtils drawingUtils = new DrawingUtils();
  DeliveryOrder deliveryOrder;
  PickingList pickingList;
  Inventory inventory = new Inventory();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", men="", den="", uty="", dnm="", bnm="", p1="", p2="", p3="", p4="", p5="", p6="";

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
        if(name.equals("p1")) // sourceDocCode
          p1 = value;
        else
        if(name.equals("p2")) // serviceCode
          p2 = value;
        else
        if(name.equals("p3")) // targetDocName
          p3 = value;
        else
        if(name.equals("p4")) // sourceDocName
          p4 = value;
        else
        if(name.equals("p5")) // returnServlet
          p5 = value;
        else
        if(name.equals("p6")) // cashOrNot
          p6 = value;
      }
  
      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, p3, p4, p5, p6, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "DocumentBuild", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6095, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                    String p1, String p2, String p3, String p4, String p5, String p6, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');
    
    Connection con = null;
    Statement stmt = null;
    ResultSet rs   = null;
    
    try
    {
      String uName    = directoryUtils.getMySQLUserName();
      String passWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();
    
      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + uName + "&password=" + passWord);
    
      if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
      {
        messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "6095", imagesDir, localDefnsDir, defnsDir, bytesOut);
        serverUtils.etotalBytes(req, unm, dnm, 6095, bytesOut[0], 0, "SID:" + p1);
      
        if(con != null) con.close();
        if(out != null) out.flush();
        return;
      }

      process(con, stmt, rs, out, req, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, p1, p2, p3, p4, p5, p6, bytesOut);
    }
    catch(Exception e) { }

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 6095, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
       
   if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void process(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty,
                       String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, String sourceDocCode,
                       String serviceCode, String targetDocName, String sourceDocName, String returnServlet, String cashOrNot, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Document Build</title>");

    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\""
                         + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    scoutln(out, bytesOut, "<script javascript=\"JavaScript\">");

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3001, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewItem(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/MainPageUtils1a?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den
                           + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+p1;}");
    }

    scoutln(out, bytesOut, "function build(){document.lines.submit();}");

    scoutln(out, bytesOut, "function checkAll(){");
    scoutln(out, bytesOut, "var e,x;for(x=0;x<document.forms[0].length;++x)");
    scoutln(out, bytesOut, "{e=document.forms[0].elements[x];");
    scoutln(out, bytesOut, "if(e.type=='checkbox' && e.name != 'all' && e.name != 'renumberLines' && e.name != 'renumberEntries')");
    scoutln(out, bytesOut, "if(e.checked)");
    scoutln(out, bytesOut, "e.checked=false;");
    scoutln(out, bytesOut, "else e.checked=true;}}");

    scoutln(out, bytesOut, "</script>");

    scoutln(out, bytesOut, "<form action=\"DocumentBuildExecute\" name=lines enctype=\"application/x-www-form-urlencoded\" method=POST>");
    scoutln(out, bytesOut, "<input type=hidden name=unm value=" + unm + ">");
    scoutln(out, bytesOut, "<input type=hidden name=sid value=" + sid + ">");
    scoutln(out, bytesOut, "<input type=hidden name=uty value=" + uty + ">");
    scoutln(out, bytesOut, "<input type=hidden name=men value=" + men + ">");
    scoutln(out, bytesOut, "<input type=hidden name=den value=" + den + ">");
    scoutln(out, bytesOut, "<input type=hidden name=dnm value=" + dnm + ">");
    scoutln(out, bytesOut, "<input type=hidden name=bnm value=" + bnm + ">");
    scoutln(out, bytesOut, "<input type=hidden name=p1  value=" + sourceDocCode + ">");
    scoutln(out, bytesOut, "<input type=hidden name=p2  value=" + serviceCode + ">");
    scoutln(out, bytesOut, "<input type=hidden name=p6  value=" + cashOrNot + ">");

    byte[] linesData  = new byte[2000];
    int[]  listLen    = new int[1];  listLen[0] = 2000;
    int[]  linesCount = new int[1];

    byte[] b = new byte[21];
    generalUtils.strToBytes(b, sourceDocCode);

    short lineField=0, entryField=0, descField=0, itemCodeField=0, mfrField=0, mfrCodeField=0;
    boolean isASalesOrder = false, isASalesOrder2 = false;
    String soCode="";

    if(serviceCode.equals("4022") || serviceCode.equals("4033") || serviceCode.equals("4056"))
    { 
      Quotation quotation = new Quotation();
      linesData = quotation.getLines(con, stmt, rs, b, '\000', linesData, listLen, linesCount, dnm, localDefnsDir, defnsDir);
      lineField  = 12; // origin-1
      entryField = 13; // origin-1
      descField  = 3;  // origin-1
      itemCodeField = 2;  // origin-1
      mfrField      = 20;  // origin-1
      mfrCodeField  = 21;  // origin-1
    }
    else
    if(serviceCode.equals("4034") || serviceCode.equals("5024") || serviceCode.equals("4045") || serviceCode.equals("4082") || serviceCode.equals("3042") || serviceCode.equals("5072") || serviceCode.equals("4132"))
    {
      SalesOrder salesOrder = new SalesOrder();
      linesData = salesOrder.getLines(con, stmt, rs, b, '\000', linesData, listLen, linesCount, dnm, localDefnsDir, defnsDir);
      lineField  = 22; // origin-1
      entryField = 23; // origin-1
      descField  = 3;  // origin-1
      itemCodeField = 2;  // origin-1
      mfrField      = 30;  // origin-1
      mfrCodeField  = 31;  // origin-1

      isASalesOrder = true;

      soCode = generalUtils.stringFromBytes(b, 0L);
      pickingList = new PickingList();
    }
    else
    if(serviceCode.equals("4058"))
    {
      SalesOrder salesOrder = new SalesOrder();
      linesData = salesOrder.getLines(con, stmt, rs, b, '\000', linesData, listLen, linesCount, dnm, localDefnsDir, defnsDir);
      lineField  = 22; // origin-1
      entryField = 23; // origin-1
      descField  = 3;  // origin-1
      itemCodeField = 2;  // origin-1
      mfrField      = 30;  // origin-1
      mfrCodeField  = 31;  // origin-1

      isASalesOrder2 = true;

      soCode = generalUtils.stringFromBytes(b, 0L);
      deliveryOrder = new DeliveryOrder();
    }
    else
    if(serviceCode.equals("4099"))
    {
      SalesOrder salesOrder = new SalesOrder();
      linesData = salesOrder.getLines(con, stmt, rs, b, '\000', linesData, listLen, linesCount, dnm, localDefnsDir, defnsDir);
      lineField  = 22; // origin-1
      entryField = 23; // origin-1
      descField  = 3;  // origin-1
      itemCodeField = 2;  // origin-1
      mfrField      = 30;  // origin-1
      mfrCodeField  = 31;  // origin-1
    }
    else
    if(serviceCode.equals("4433") || serviceCode.equals("4065"))
    { 
      SalesOrder salesOrder = new SalesOrder();
      linesData = salesOrder.getLines(con, stmt, rs, b, '\000', linesData, listLen, linesCount, dnm, localDefnsDir, defnsDir);
      lineField  = 22; // origin-1
      entryField = 23; // origin-1
      descField  = 3;  // origin-1
      itemCodeField = 2;  // origin-1
      mfrField      = 30;  // origin-1
      mfrCodeField  = 31;  // origin-1
      isASalesOrder = false;
      soCode = generalUtils.stringFromBytes(b, 0L);
    }
    else
    if(serviceCode.equals("4057") || serviceCode.equals("4068") || serviceCode.equals("4069"))
    { 
      pickingList = new PickingList();
      linesData = pickingList.getLines(con, stmt, rs, b, '\000', linesData, listLen, linesCount, dnm, localDefnsDir, defnsDir);
      lineField  = 19; // origin-1
      entryField = 20; // origin-1
      descField  = 3;  // origin-1
      itemCodeField = 2;  // origin-1
      mfrField      = 33;  // origin-1
      mfrCodeField  = 34;  // origin-1
    }
    else
    if(serviceCode.equals("4021") || serviceCode.equals("4070"))
    { 
      deliveryOrder = new DeliveryOrder();
      linesData = deliveryOrder.getLines(con, stmt, rs, b, '\000', linesData, listLen, linesCount, dnm, localDefnsDir, defnsDir);
      lineField  = 19; // origin-1
      entryField = 20; // origin-1
      descField  = 3;  // origin-1
      itemCodeField = 2;  // origin-1
      mfrField      = 27;  // origin-1
      mfrCodeField  = 28;  // origin-1
    }
    else
    if(serviceCode.equals("4083"))
    { 
      SalesInvoice salesInvoice = new SalesInvoice();
      linesData = salesInvoice.getLines(con, stmt, rs, b, '\000', linesData, listLen, linesCount, dnm, localDefnsDir, defnsDir);
      lineField  = 20; // origin-1
      entryField = 21; // origin-1
      descField  = 3;  // origin-1
      itemCodeField = 2;  // origin-1
      mfrField      = 29;  // origin-1
      mfrCodeField  = 30;  // origin-1
    }
    else
    if(serviceCode.equals("4090"))
    { 
      ProformaInvoice proformaInvoice = new ProformaInvoice();
      linesData = proformaInvoice.getLines(con, stmt, rs, b, '\000', linesData, listLen, linesCount, dnm, localDefnsDir, defnsDir);
      lineField  = 20; // origin-1
      entryField = 21; // origin-1
      descField  = 3;  // origin-1
      itemCodeField = 2;  // origin-1
      mfrField      = 28;  // origin-1
      mfrCodeField  = 29;  // origin-1
    }
    else
    if(serviceCode.equals("3140") || serviceCode.equals("3141"))
    { 
      Inbox inbox = new Inbox();
      linesData = inbox.getLines(con, stmt, rs, b, '\000', linesData, listLen, linesCount, dnm, localDefnsDir, defnsDir);
      lineField  = 2; // origin-1
      entryField = 2; // origin-1 
      descField  = 9; // origin-1
      itemCodeField = 3;  // origin-1
      mfrField      = 4;  // origin-1
      mfrCodeField  = 3;  // origin-1
    }
    else
    if(serviceCode.equals("3035"))
    { 
      GoodsReceivedNote goodsReceivedNote = new GoodsReceivedNote();
      linesData = goodsReceivedNote.getLines(con, stmt, rs, b, '\000', linesData, listLen, linesCount, dnm, localDefnsDir, defnsDir);
      lineField  = 11; // origin-1
      entryField = 12; // origin-1
      descField  = 3;  // origin-1
      itemCodeField = 2;  // origin-1
      mfrField      = 18;  // origin-1
      mfrCodeField  = 19;  // origin-1
    }
      
    scoutln(out, bytesOut, "<input type=hidden name=p5 value=" + linesCount[0] + ">");

    int[] hmenuCount = new int[1];

    drawingUtils.outputPageFrame(con, stmt, rs, out, req, "6095", "", "DocumentBuild", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount, bytesOut);

    drawingUtils.drawTitle(out, false, false, "DocumentBuild", "", "Build " + targetDocName + " from " + sourceDocName, "6095", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
      
    scoutln(out, bytesOut, "<table id=\"page\" width=100%>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap colspan=2><p><input type=checkbox name=renumberLines checked>Renumber All Lines</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    
    scoutln(out, bytesOut, "<tr><td nowrap colspan=2><p><input type=checkbox name=renumberEntries>Renumber All Entries</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    
    if(isASalesOrder)
    {
      scoutln(out, bytesOut, "<tr><td colspan=4><p>Note: Only items not completely supplied are checked.</td></tr>");
      scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    }
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "</table>");

    scoutln(out, bytesOut, "<table id=\"page\" cellspacing=2 cellpadding=0 width=100%>");
    scoutln(out, bytesOut, "<tr id=\"pageColumn\"><td><p><input type=checkbox name=all onClick=\"checkAll()\"></td>");
    scoutln(out, bytesOut, "<td><p><b>Line</td><td><p><b>Entry</td><td><p><b>ItemCode</td><td><p><b>Manufacturer</td><td><p><b>Manufacturer Code</td>");

    if(serviceCode.equals("3140") || serviceCode.equals("3141"))
      scoutln(out, bytesOut, "<td><p><b>OnHand</td>");
  
    scoutln(out, bytesOut, "<td><p><b>Description</td></tr>");

    char dpOnQuantities = miscDefinitions.dpOnQuantities(con, stmt, rs, "5");

    double amtReqd, amtPickedAlready, amtDeliveredAlready, onHand = 0.0;
    
    byte[] thisLine = new byte[1000]; // plenty
    String line, entry, desc, itemCode, mfr, mfrCode, quantitiesReqd="";
    for(int xx=0;xx<linesCount[0];++xx)
    {
      if(generalUtils.getListEntryByNum(xx, linesData, thisLine)) // just-in-case
      {
        generalUtils.replaceTwosWithNulls(thisLine);
        line     = generalUtils.dfsAsStr(thisLine, lineField);
        entry    = generalUtils.dfsAsStr(thisLine, entryField);
        desc     = generalUtils.dfsAsStr(thisLine, descField);
        itemCode = generalUtils.dfsAsStr(thisLine, itemCodeField);
        mfr      = generalUtils.dfsAsStr(thisLine, mfrField);
        mfrCode  = generalUtils.dfsAsStr(thisLine, mfrCodeField);

        if(serviceCode.equals("3140") || serviceCode.equals("3141"))
          onHand = stockLevel(itemCode, unm, sid, uty, men, den, dnm, bnm, localDefnsDir);        
        
        if(isASalesOrder)
        {
          amtReqd = generalUtils.dfsAsDouble(thisLine, (short)5);
          amtPickedAlready = pickingList.getTotalPickedForASOLine(con, stmt, rs, soCode, line);

          if(dnm.equals("xxx"))
          {
            if(serviceCode.equals("3042")) // so to pl
            {
              if(inventory.existsItemRecGivenCode(con, stmt, rs, itemCode))
              {
                scoutln(out, bytesOut, "<tr>");
                scoutln(out, bytesOut, "<td><input type=checkbox name=" + line);
                if(amtPickedAlready < amtReqd)
                  scoutln(out, bytesOut, " checked");
                scoutln(out, bytesOut, "></td><td><p>" + line + "</td><td><p>" + entry + "</td><td><p><a href=\"javascript:viewItem('" + itemCode + "')\">" + itemCode + "</a></td>");
              }
              else
              {
                scoutln(out, bytesOut, "<tr style='background-color:tomato;'>");
                scoutln(out, bytesOut, "<td></td>");
                scoutln(out, bytesOut, "<td><p>" + line + "</td><td><p>" + entry + "</td><td><p><a href=\"javascript:viewItem('" + itemCode + "')\">" + itemCode + "</a></td>");
              }
            }
            else
            {
              if(inventory.existsItemRecGivenCode(con, stmt, rs, itemCode))
              {
                scoutln(out, bytesOut, "<tr>");
              }
              else
              {
                scoutln(out, bytesOut, "<tr style='background-color:tomato;'>");
              }
              scoutln(out, bytesOut, "<td><input type=checkbox name=" + line);
              if(amtPickedAlready < amtReqd)
                scoutln(out, bytesOut, " checked");
              scoutln(out, bytesOut, "></td><td><p>" + line + "</td><td><p>" + entry + "</td><td><p><a href=\"javascript:viewItem('" + itemCode + "')\">" + itemCode + "</a></td>");
            }
          }
          else
          {
            scoutln(out, bytesOut, "<tr>");
            scoutln(out, bytesOut, "<td><input type=checkbox name=" + line);
            if(amtPickedAlready < amtReqd)
              scoutln(out, bytesOut, " checked");
            scoutln(out, bytesOut, "></td><td><p>" + line + "</td><td><p>" + entry + "</td><td><p><a href=\"javascript:viewItem('" + itemCode + "')\">" + itemCode + "</a></td>");
          }

          scoutln(out, bytesOut, "<td><p>" + mfr + "</td><td nowrap><p> " + mfrCode + "</td>");

          if(serviceCode.equals("3140") || serviceCode.equals("3141"))
            scoutln(out, bytesOut, "<td align=center><p>" + generalUtils.formatNumeric(onHand, dpOnQuantities) + "</td>");

          scoutln(out, bytesOut, "<td nowrap><p> " + desc + "</td></tr>");

          quantitiesReqd += ((amtReqd - amtPickedAlready) + "\001");
        }
        else
        if(isASalesOrder2)
        {
          amtReqd = generalUtils.dfsAsDouble(thisLine, (short)5);
          amtDeliveredAlready = deliveryOrder.getTotalDeliveredForASOLine(con, stmt, rs, soCode, line);

          scoutln(out, bytesOut, "<tr>");

          scoutln(out, bytesOut, "<td><input type=checkbox name=" + line);

          if(amtDeliveredAlready < amtReqd)
            scoutln(out, bytesOut, " checked");
          scoutln(out, bytesOut, "></td><td><p>" + line + "</td><td><p>" + entry + "</td><td><p><a href=\"javascript:viewItem('" + itemCode + "')\">" + itemCode + "</a></td>");
          scoutln(out, bytesOut, "<td><p>" + mfr + "</td><td nowrap><p> " + mfrCode + "</td>");

          scoutln(out, bytesOut, "<td nowrap><p> " + desc + "</td></tr>");

          quantitiesReqd += ((amtReqd - amtDeliveredAlready) + "\001");
        }
        else
        {
          scoutln(out, bytesOut, "<tr><td><input type=checkbox name=" + line + " checked></td>");
          scoutln(out, bytesOut, "<td><p>" + line + "</td><td><p>" + entry + "</td><td><p><a href=\"javascript:viewItem('" + itemCode + "')\">" + itemCode + "</a></td>");
          scoutln(out, bytesOut, "<td><p>" + mfr + "</td><td nowrap><p> " + mfrCode + "</td>");

          if(serviceCode.equals("3140") || serviceCode.equals("3141"))
            scoutln(out, bytesOut, "<td align=center><p>" + generalUtils.formatNumeric(onHand, dpOnQuantities) + "</td>");    

          scoutln(out, bytesOut, "<td nowrap><p> " + desc + "</td></tr>");
        }  
      }
    }  

    scoutln(out, bytesOut, "<input type=hidden name=p3 value=" + quantitiesReqd + ">");

    scoutln(out, bytesOut, "</table><table id=\"page\" width=100%>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    drawListingHeader(out, targetDocName, bytesOut);
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void drawListingHeader(PrintWriter out, String targetDocName, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<tr><td nowrap><p>&nbsp<a href=\"javascript:build()\">Build the " + targetDocName + "</a></td></tr>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private double stockLevel(String itemCodeStr, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir)
                            throws Exception
  {
    String traceList = getStockLevelsViaTrace(itemCodeStr, unm, uty, sid, men, den, dnm, bnm, localDefnsDir);
    // traceList = "SamLeong\001150000\001SyedAlwi\00150000\001\0"

    double[] totalStockLevel = new double[1];
    
    totalLevels(traceList, totalStockLevel);

    return totalStockLevel[0];
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private String getStockLevelsViaTrace(String itemCode, String unm, String uty, String sid, String men, String den, String dnm, String bnm,
                                        String localDefnsDir) throws Exception
  { 
    URL url = new URL("http://" + serverUtils.serverToCall("OFSA", localDefnsDir) + "/central/servlet/StockLevelsGenerate?unm=" + unm + "&sid=" + sid + "&uty=" + uty
                    + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=" + itemCode + "&bnm=" + bnm);

    URLConnection uc = url.openConnection();
    uc.setDoInput(true);
    uc.setUseCaches(false);
    uc.setDefaultUseCaches(false);

    uc.setRequestProperty("Content-Type", "application/octet-stream");

    BufferedReader di = new BufferedReader(new InputStreamReader(uc.getInputStream()));

    String t="", s = di.readLine();
    while(s != null)
    {
      t += s;
      s = di.readLine();
    }

    di.close();

    return t;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void totalLevels(String traceList, double[] totalStockLevel) throws Exception
  {
    int y=0, len = traceList.length();
    String thisQty;
    totalStockLevel[0] = 0.0;

    while(y < len) // just-in-case
    {
      while(y < len && traceList.charAt(y) != '\001')
        ++y;
      ++y;

      thisQty = "";
      while(y < len && traceList.charAt(y) != '\001')
        thisQty += traceList.charAt(y++);
      ++y;

      totalStockLevel[0] += generalUtils.doubleFromStr(thisQty);
    }
  }

}
