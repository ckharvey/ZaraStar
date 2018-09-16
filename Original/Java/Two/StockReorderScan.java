// =======================================================================================================================================================================================================
// System: ZaraStar Product: Stock ReOrder: Scan stock file
// Module: StockReorderScan.java
// Author: C.K.Harvey
// Copyright (c) 2002-08 Christopher Harvey. All Rights Reserved.
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
import java.util.*;
import java.io.*;
import java.net.*;
import java.sql.*;

public class StockReorderScan extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  MiscDefinitions miscDefinitions = new MiscDefinitions();
  ReportGeneration reportGeneration = new ReportGeneration();
  AccountsUtils accountsUtils = new AccountsUtils();
  Inventory inventory = new Inventory();
  GoodsReceivedNote goodsReceivedNote = new GoodsReceivedNote();
  PickingList pickingList = new PickingList();
  PurchaseOrder purchaseOrder = new PurchaseOrder();
  SalesOrder salesOrder = new SalesOrder();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", mfr1="", mfr2="", numDaysSold="", numDaysReqd="", includeOption="", includeOption2="", which="";
    
    try
    {
      directoryUtils.setContentHeaders(res);
      out = res.getWriter();

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
        if(name.equals("mfr1"))
          mfr1 = value[0];
        else
        if(name.equals("mfr2"))
          mfr2 = value[0];
        else
        if(name.equals("which"))
          which = value[0];
        else
        if(name.equals("numDaysSold"))
          numDaysSold = value[0];
        else
        if(name.equals("numDaysReqd"))
          numDaysReqd = value[0];
        else
        if(name.equals("includeOption"))
          includeOption = value[0];
        else
        if(name.equals("includeOption2"))
          includeOption2 = value[0];
        else
        if(name.length() > 2 && name.substring(1, 3).equals(".x")) ;
        else
        if(name.length() > 2 && name.substring(1, 3).equals(".y")) ;
      }
      
      doIt(out, req, which, mfr1, mfr2, numDaysSold, numDaysReqd, includeOption, includeOption2, unm, sid, uty, men, den, dnm, bnm, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "StockReorderScan", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1021, bytesOut[0], 0, "ERR:" + which);
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String which, String mfr1, String mfr2, String numDaysSold, String numDaysReqd,
          String includeOption, String includeOption2, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                    int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');
    String reportsDir    = directoryUtils.getUserDir('R', dnm, unm);
    String workingDir    = directoryUtils.getUserDir('W', dnm, dnm + "/" + unm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null, stmt2 = null, stmt3 = null;
    ResultSet rs   = null, rs2 = null,   rs3 = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 1021, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "StockReorderScan", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1021, bytesOut[0], 0, "ACC:" + which);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "StockReorderScan", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1021, bytesOut[0], 0, "SID:" + which);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(numDaysSold.length() == 0 || numDaysSold.equalsIgnoreCase("null"))
      numDaysSold = "0";

    RandomAccessFile[] fh = new RandomAccessFile[1];
        
    switch(setup(workingDir, reportsDir, localDefnsDir, defnsDir, fh))
    {
      case -2 : // cannot create report output file
                messagePage.msgScreen(false, out, req, 18, unm, sid, uty, men, den, dnm, bnm, "1021", "", "", "", imagesDir, localDefnsDir, defnsDir, bytesOut);
                serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 1021, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), which);
                serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 1021, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), which);
                if(con != null) con.close();
                if(out != null) out.flush(); 
                break;
      default : // submitted
                messagePage.msgScreen(false, out, req, 41, unm, sid, uty, men, den, dnm, bnm, "1021", "", "", "", imagesDir, localDefnsDir, defnsDir, bytesOut);
                if(out != null) out.flush(); 
                out.close();
                process(con, stmt, stmt2, stmt3, rs, rs2, rs3, fh[0], which, mfr1, mfr2, generalUtils.strToInt(numDaysSold), generalUtils.strToInt(numDaysReqd), includeOption, includeOption2, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir);
                serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 1021, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), which);
                if(con != null) con.close();
                break;
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int setup(String workingDir, String reportsDir, String localDefnsDir, String defnsDir, RandomAccessFile[] fh) throws Exception
  {
    fh[0] = reportGeneration.createNewFile((short)290, workingDir, localDefnsDir, defnsDir, reportsDir);
    if(fh[0] == null)
      return -2;
    return 0;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int process(Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, ResultSet rs3, RandomAccessFile fh, String which, String mfr1, String mfr2, int numDaysSold, int numDaysReqd,
                      String includeOption, String includeOption2, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir) throws Exception
  {
    int    dateBeforeTodayEncoded = generalUtils.todayEncoded(localDefnsDir, defnsDir) - numDaysSold;
    String dateBeforeToday        = generalUtils.decodeToYYYYMMDD(dateBeforeTodayEncoded);

    // next line used as title of report on report listing screen. DO NOT AMEND
    if(which.equals("1"))
      fh.writeBytes("<tr><td><p><b>" + mfr1 + ": Sales Order Needs</td></tr>"); // !!!
    else fh.writeBytes("<tr><td><p><b>" + mfr2 + ": Since " + generalUtils.decode(dateBeforeTodayEncoded, localDefnsDir, defnsDir) + "</td></tr>");
    
    fh.writeBytes("<tr><td><p><b>Stock Code</td>");
    fh.writeBytes("<td><p><b>Mfr Code</td>");
    fh.writeBytes("<td align=center><p><b>OnHand</td>");
    fh.writeBytes("<td align=center><p><b>On SO</td>");
    fh.writeBytes("<td align=center><p><b>On PO</td>");
    fh.writeBytes("<td align=center><p><b>Derived<br>Position</td>");
    
    if(! which.equals("1"))
      fh.writeBytes("<td align=center><p><b>On SO since<br>" + generalUtils.convertFromYYYYMMDD(dateBeforeToday) + "</td>");
    
    fh.writeBytes("<td align=center><p><b>ReOrder</td>");
    fh.writeBytes("<td align=center><p><b>Price</td>");
    fh.writeBytes("<td><p><b>Description</td></tr>");

    byte[] totalStockLevelB = new byte[20];
    byte[] outstandingOnSOB = new byte[20];
    byte[] outstandingOnPOB = new byte[20];
    byte[] totalReOrdersB   = new byte[20];
    byte[] posnB            = new byte[20];
    byte[] soldQtyB         = new byte[20];

    double outstandingOnSO, outstandingOnPO, posn;
    double[] totalStockLevel = new double[1];
    double[] soldQty         = new double[1];

    String bgColor;
    boolean green = false, wanted;

    char dpOnQuantities = miscDefinitions.dpOnQuantities(con, stmt, rs, "5");

    stmt = con.createStatement();

    if(which.equals("1"))
    {
      rs = stmt.executeQuery("SELECT ItemCode, PurchasePrice, Currency, Description, Description2, ManufacturerCode FROM stock WHERE Manufacturer = '" + mfr1 + "' AND Status != 'C' ORDER BY ManufacturerCode");
    }
    else
    {
      rs = stmt.executeQuery("SELECT ItemCode, PurchasePrice, Currency, Description, Description2, ManufacturerCode FROM stock WHERE Manufacturer = '" + mfr2 + "' AND Status != 'C' ORDER BY ManufacturerCode");
    }

    String itemCode, purchasePrice, purchaseCurrency, desc, desc2, mfrCode;
    double salesPerDay, projectedSales;
    int count = 1;
    
    while(rs.next())
    {    
      itemCode         = rs.getString(1);
      purchasePrice    = rs.getString(2);
      purchaseCurrency = rs.getString(3);
      desc             = rs.getString(4);
      desc2            = rs.getString(5);
      mfrCode          = rs.getString(6);
      
      totalStockLevel[0] = soldQty[0] = 0.0;
      
      stockLevels(itemCode, unm, uty, sid, men, den, dnm, bnm, localDefnsDir, totalStockLevel);
      
      generalUtils.doubleToBytesCharFormat(totalStockLevel[0], totalStockLevelB, 0);
      generalUtils.bytesDPsGivenSeparator(true, '\000', dpOnQuantities, totalStockLevelB, 20, 0);
      generalUtils.formatNumeric(totalStockLevelB, dpOnQuantities);

      outstandingOnSO = onSO(con, stmt2, stmt3, rs2, rs3, which, itemCode, dateBeforeTodayEncoded, soldQty);
      generalUtils.doubleToBytesCharFormat(outstandingOnSO, outstandingOnSOB, 0);
      generalUtils.bytesDPsGivenSeparator(true, '\000', dpOnQuantities, outstandingOnSOB, 20, 0);
      generalUtils.formatNumeric(outstandingOnSOB, dpOnQuantities);

      generalUtils.doubleToBytesCharFormat(soldQty[0], soldQtyB, 0);
      generalUtils.bytesDPsGivenSeparator(true, '\000', dpOnQuantities, soldQtyB, 20, 0);
      generalUtils.formatNumeric(soldQtyB, dpOnQuantities);

      outstandingOnPO = onPO(con, stmt2, rs2, which, itemCode) + onLP(con, stmt2, rs2, which, itemCode);
      generalUtils.doubleToBytesCharFormat(outstandingOnPO, outstandingOnPOB, 0);
      generalUtils.bytesDPsGivenSeparator(true, '\000', dpOnQuantities, outstandingOnPOB, 20, 0);
      generalUtils.formatNumeric(outstandingOnPOB, dpOnQuantities);

      posn = (outstandingOnPO + totalStockLevel[0]) - outstandingOnSO; // 12jun13
      generalUtils.doubleToBytesCharFormat(posn, posnB, 0);
      generalUtils.bytesDPsGivenSeparator(true, '\000', dpOnQuantities, posnB, 20, 0);
      generalUtils.formatNumeric(posnB, dpOnQuantities);

      if(! which.equals("1") && includeOption2.equals("on"))
      {
        wanted = true;
      }
      else
      if(! which.equals("1") && includeOption.equals("on"))
      {
        if(soldQty[0] > 0)
          wanted = true;
        else wanted = false;
      }
      else wanted = true;

      if(wanted)
      {
        if(which.equals("1")) // back-to-back
          projectedSales = posn;
        else // forecast
        {
          salesPerDay = soldQty[0] / numDaysSold;
          projectedSales = (salesPerDay * numDaysReqd) - posn;
        }

        if((! which.equals("1") && includeOption2.equals("on")) || projectedSales > 0)
        {          
          generalUtils.doubleToBytesCharFormat(projectedSales, totalReOrdersB, 0);
          generalUtils.bytesDPsGivenSeparator(true, '\000', dpOnQuantities, totalReOrdersB, 20, 0);
          generalUtils.formatNumeric(totalReOrdersB, dpOnQuantities);

          if(green) { bgColor = "#DDF0DD"; green = false; } else { bgColor = "#FFFFFF"; green = true; }

          fh.writeBytes("<tr bgcolor=" + bgColor + "><td><p>");
          fh.writeBytes("<a href=\"/central/servlet/MainPageUtils1a?unm=" + unm + "&sid=" + "\002" + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=" + generalUtils.sanitise(itemCode) + "&bnm=" + bnm + "\">" + itemCode + "</a></td>");

          fh.writeBytes("<td><p>" + mfrCode + "</td>");
          fh.writeBytes("<td align=center><p>" + generalUtils.stringFromBytes(totalStockLevelB, 0L) + "</td>");
          fh.writeBytes("<td align=center><p>" + generalUtils.stringFromBytes(outstandingOnSOB, 0L) + "</td>");
          fh.writeBytes("<td align=center><p>" + generalUtils.stringFromBytes(outstandingOnPOB, 0L) + "</td>");
          fh.writeBytes("<td align=center><p>" + generalUtils.stringFromBytes(posnB, 0L)            + "</td>");

          if(! which.equals("1"))
            fh.writeBytes("<td align=center><p>" + generalUtils.stringFromBytes(soldQtyB, 0L)       + "</td>");

          fh.writeBytes("<td align=center><p>");
          fh.writeBytes("<input type=text maxlength=6 size=6 name='" + count++ + ":" + itemCode + "' value='" + generalUtils.doubleDPs(generalUtils.stringFromBytes(totalReOrdersB, 0L), dpOnQuantities) + "'></td>");

          fh.writeBytes("<td align=right nowrap><p>" + purchaseCurrency + " " + generalUtils.doubleDPs(purchasePrice, '2') + "&nbsp;</td>");

          fh.writeBytes("<td><p>" + desc + " : "  + desc2 + "</td></tr>");
        }
      }
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    // next line used as EOF to determine if report generation is complete (on report listing screen)
    fh.writeBytes("<tr><td><p>*** END ***</td></tr>");

    generalUtils.fileClose(fh);

    System.out.println("Completed");

    return 0;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void stockLevels(String itemCode, String unm, String uty, String sid, String men, String den, String dnm, String bnm, String localDefnsDir, double[] totalStockLevel) throws Exception
  {
    String traceList = getStockLevelsViaTrace(itemCode, unm, uty, sid, men, den, dnm, bnm, localDefnsDir);
    // traceList = "SamLeong\001150000\001SyedAlwi\00150000\001\0"

    totalStoresLevels(traceList, totalStockLevel);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String getStockLevelsViaTrace(String itemCode, String unm, String uty, String sid, String men, String den, String dnm, String bnm, String localDefnsDir) throws Exception
  { 
    URL url = new URL("http://" + serverUtils.serverToCall("OFSA", localDefnsDir) + "/central/servlet/StockLevelsGenerate?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=" + itemCode + "&bnm=" + bnm);

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

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void totalStoresLevels(String traceList, double[] totalStockLevel) throws Exception
  {
    int y=0, len = traceList.length();
    String thisQty;

    while(y < len) // just-in-case
    {
      while(y < len && traceList.charAt(y) != '\001')
        y++;
      ++y;

      thisQty = "";
      while(y < len && traceList.charAt(y) != '\001')
        thisQty += traceList.charAt(y++);
      ++y;

      totalStockLevel[0] += generalUtils.doubleFromStr(thisQty);
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private double onPO(Connection con, Statement stmt, ResultSet rs, String which, String itemCode) throws Exception
  {
    stmt = con.createStatement();
    
    String where = "";
    where = " AND t1.AllReceived != 'Y' ";

    rs = stmt.executeQuery("SELECT t2.POCode, t2.Line, t2.Quantity FROM pol AS t2 INNER JOIN po AS t1 ON t2.POCode = t1.POCode WHERE t2.ItemCode = '" + generalUtils.sanitiseForSQL(itemCode) + "' AND t2.Received != 'R' AND t1.Status != 'C'" + where);

    String poCode, poLine;
    double qty, actualQty, outstanding = 0.0;
    
    while(rs.next())
    {    
      poCode = rs.getString(1);
      poLine = rs.getString(2);
      qty    = generalUtils.doubleFromStr(rs.getString(3));
     
      actualQty = goodsReceivedNote.getTotalReceivedForAPOLine(con, stmt, rs, poCode, poLine);

      if(actualQty < qty)
        outstanding += (qty - actualQty);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return outstanding;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private double onLP(Connection con, Statement stmt, ResultSet rs, String which, String itemCode) throws Exception
  {
    stmt = con.createStatement();
    
    String where = "";
    where = " AND t1.AllReceived != 'Y' ";

    rs = stmt.executeQuery("SELECT t2.LPCode, t2.Line, t2.Quantity FROM lpl AS t2 INNER JOIN lp AS t1 ON t2.LPCode = t1.LPCode WHERE t2.ItemCode = '" + generalUtils.sanitiseForSQL(itemCode) + "' AND t2.Received != 'R' AND t1.Status != 'C'" + where);

    String lpCode, lpLine;
    double qty, actualQty, outstanding = 0.0;
    
    while(rs.next())
    {    
      lpCode = rs.getString(1);
      lpLine = rs.getString(2);
      qty    = generalUtils.doubleFromStr(rs.getString(3));
     
      actualQty = goodsReceivedNote.getTotalReceivedForAPOLine(con, stmt, rs, lpCode, lpLine);

      if(actualQty < qty)
        outstanding += (qty - actualQty);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return outstanding;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private double onSO(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String which, String itemCode, int dateBeforeToday, double[] soldQty) throws Exception
  {
    stmt = con.createStatement();

    String where = "";
    where = " AND t1.AllSupplied != 'Y' ";

    rs = stmt.executeQuery("SELECT t2.SOCode, t2.Line, t2.Quantity, t1.Date, t1.AllSupplied FROM sol AS t2 INNER JOIN so AS t1 ON t2.SOCode = t1.SOCode WHERE t2.ItemCode = '" + generalUtils.sanitiseForSQL(itemCode) + "' AND t1.Status != 'C'" + where);

    int date;
    String soCode, soLine, allSupplied;
    double qty = 0, actualQty = 0, outstanding = 0.0;

    while(rs.next())
    {    
      soCode = rs.getString(1);
      soLine = rs.getString(2);
      qty    = generalUtils.doubleFromStr(rs.getString(3));
      date   = generalUtils.encodeFromYYYYMMDD(rs.getString(4));

      actualQty = pickingList.getTotalPickedForASOLine(con, stmt2, rs2, soCode, soLine);

      if(actualQty < qty)
      {
         outstanding += (qty - actualQty);
      }

      if(date >= dateBeforeToday)
      {
        if(which.equals("1"))
          soldQty[0] += actualQty;
        else soldQty[0] += qty;
      }
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return outstanding;
  }

}
