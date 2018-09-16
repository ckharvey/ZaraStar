// ==========================================================================================================================================================================================================
// System: ZaraStar Product: Stock Check Reconciliation
// Module: StockCheckReconciliationExecute.java
// Author: C.K.Harvey
// Copyright (c) 1999-2009 Christopher Harvey. All Rights Reserved.
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

public class StockCheckReconciliationExecute extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  DrawingUtils drawingUtils = new DrawingUtils();
  Inventory inventory = new Inventory();
  MiscDefinitions miscDefinitions = new MiscDefinitions();
  
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p3="";
    try
    {
      directoryUtils.setContentHeaders2(res);
      out = res.getWriter();

      unm = req.getParameter("unm");
      sid = req.getParameter("sid");
      uty = req.getParameter("uty");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");
      p1  = req.getParameter("p1"); // store
      p3  = req.getParameter("p3"); // type

      if(p1 == null) p1 = "";
      
      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p3, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "StockCheckReconciliationExecute", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3065, bytesOut[0], 0, "ERR:" + p1 + ":" + p3);
      if(out != null) out.flush(); 
    }
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1, String p3, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');
    String workingDir    = directoryUtils.getUserDir('W', dnm, unm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null, stmt2 = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 3065, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "3065a", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3065, bytesOut[0], 0, "ACC:" + p1 + ":" + p3);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "3065a", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3065, bytesOut[0], 0, "SID:" + p1 + ":" + p3);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    set(con, stmt, stmt2, rs, out, req, p1, p3, unm, sid, uty, men, den, dnm, bnm, workingDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 3065, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1 + ":" + p3);
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, Statement stmt2, ResultSet rs, PrintWriter out, HttpServletRequest req, String store, String type, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                   String workingDir, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Stock Check Reconciliation</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3018, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function view(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/StockCheckForItem?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p1=\"+p1;}");
    }
    
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3001, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewItem(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/ProductStockRecord?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p1=\"+p1;}");
    }
    
    scoutln(out, bytesOut, "function level(code,date){var p1=sanitise(code);");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/StockCheckWIPReconciliation?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=\"+date+\"&p1=\"+p1;}");

    scoutln(out, bytesOut, "function update(){document.lines.submit();}");
        
    scoutln(out, bytesOut, "function sanitise(code){");
    scoutln(out, bytesOut, "var code2='';var x;var len=code.length;");
    scoutln(out, bytesOut, "for(x=0;x<len;++x)");
    scoutln(out, bytesOut, "if(code.charAt(x)=='#')code2+='%23';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='\"')code2+='%22';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='&')code2+='%26';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='%')code2+='%25';");
    scoutln(out, bytesOut, "else if(code.charAt(x)==' ')code2+='%20';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='?')code2+='%3F';");
    scoutln(out, bytesOut, "else code2+=code.charAt(x);");
    scoutln(out, bytesOut, "return code2};");

    RandomAccessFile fhData  = generalUtils.create(workingDir + "3065.data");
    RandomAccessFile fhState = generalUtils.create(workingDir + "3065.state");
    generalUtils.fileClose(fhState);
    String stateFileName = workingDir + "3065.state";
    keepChecking(out, "3065", unm, sid, uty, men, den, dnm, bnm, bytesOut);

    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    int[] hmenuCount = new int[1];

    drawingUtils.outputPageFrame(con, stmt, rs, out, req, "3065", "", "StockCheckReconciliationExecute", unm, sid, uty, men, den, dnm, bnm, " chkTimer() ", localDefnsDir, defnsDir, hmenuCount, bytesOut);

    String s;
    if(type.equals("S"))
      s = "Shelf Stock";
    else s = "WIP";
    
    drawingUtils.drawTitle(out, false, false, "StockCheckReconciliation", "", "Stock Check Reconciliation (" + s + ")", "3065", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<form action=\"StockCheckReconciliationUpdate\" name=lines enctype=\"application/x-www-form-urlencoded\" method=POST>");
    scoutln(out, bytesOut, "<input type=hidden name=unm value=" + unm + ">");
    scoutln(out, bytesOut, "<input type=hidden name=sid value=" + sid + ">");
    scoutln(out, bytesOut, "<input type=hidden name=uty value=" + uty + ">");
    scoutln(out, bytesOut, "<input type=hidden name=men value=" + men + ">");
    scoutln(out, bytesOut, "<input type=hidden name=den value=" + den + ">");
    scoutln(out, bytesOut, "<input type=hidden name=dnm value=" + dnm + ">");
    scoutln(out, bytesOut, "<input type=hidden name=bnm value=" + bnm + ">");
    
    scoutln(out, bytesOut, "<span id='stuff'><font size=7><br><br><br><br>1% complete, please wait...</font></span>");
    if(out != null) out.flush(); 
    out.close();

    scoutln(fhData, "<table id='page' border=0 cellspacing=2 cellpadding=3><tr id='pageColumn'><td></td>");
    scoutln(fhData, "<td><p>Check Code &nbsp;</td>");
    scoutln(fhData, "<td><p>Item Code &nbsp;</td>");
    scoutln(fhData, "<td><p>Manufacturer &nbsp;</td>");
    scoutln(fhData, "<td><p>Manufacturer Code &nbsp;</td>");
    
    if(type.equals("S"))
      scoutln(fhData, "<td><p>Stock Trace Level &nbsp;</td>");
    else scoutln(fhData, "<td><p>PL - DO &nbsp;</td>");

    scoutln(fhData, "<td><p>Counted Level &nbsp;</td>");
    scoutln(fhData, "<td nowrap><p>Difference &nbsp;</td>");
    scoutln(fhData, "<td nowrap><p>Store &nbsp;</td>");
    scoutln(fhData, "<td nowrap><p>Remark &nbsp;</td>");
    scoutln(fhData, "<td nowrap><p>Description &nbsp;</td></tr>");
    
    int[] count = new int[1];  count[0] = 0;
    String[] cssFormat = new String[1];  cssFormat[0] = "";

    detSCRecs(con, stmt, stmt2, rs, fhData, stateFileName, store, type, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, cssFormat, count);
    
    scoutln(fhData, "<tr><td>&nbsp;</td></tr>");
    scoutln(fhData, "<tr><td>&nbsp;</td></tr>");
    
    if(count[0] == 0)
      scoutln(fhData, "<tr><td nowrap colspan=6><p>No Unreconciled Records</td></tr>");
    else
    {
      scoutln(fhData, "<tr><td>&nbsp;</td></tr>");
      if(type.equals("S"))
        scoutln(fhData, "<tr><td nowrap colspan=6><p><font color=red size=3>There are currently " + count[0] + " Unreconciled Mismatched Records</font></td></tr>");
      else scoutln(fhData, "<tr><td nowrap colspan=6><p><font color=red size=3>There are currently " + count[0] + " Unreconciled Records</font></td></tr>");
      scoutln(fhData, "<tr><td>&nbsp;</td></tr>");

      scoutln(fhData, "<tr><td nowrap colspan=4><p><a href=\"javascript:update()\">Update</a> Stock Check Records</td></tr>");
    }

    scoutln(fhData, "</table></form>");
    scoutln(fhData, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
   
    generalUtils.fileClose(fhData);
    directoryUtils.updateState(stateFileName, "100");
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void detSCRecs(Connection con, Statement stmt, Statement stmt2, ResultSet rs, RandomAccessFile fhData, String stateFileName, String store, String type, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                         String localDefnsDir, String defnsDir, String[] cssFormat, int[] count) throws Exception
  {
    char dpOnQuantities = miscDefinitions.dpOnQuantities(con, stmt, rs, "5");

    try
    {
      stmt = con.createStatement();
      rs = stmt.executeQuery("SELECT COUNT(CheckCode) FROM stockc WHERE Reconciled != 'Y' AND Status != 'C' AND StoreCode = '" + generalUtils.sanitiseForSQL(store) + "' AND Type = '" + type + "' AND Level != '999999'");
      double numRecs = 1.0;
      if(rs.next())
        numRecs = rs.getInt(1);
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
      
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT CheckCode, ItemCode, StoreCode, Date, Level, Remark FROM stockc WHERE Reconciled != 'Y' AND Status != 'C' AND StoreCode = '" + generalUtils.sanitiseForSQL(store) + "' AND Type = '" + type + "' AND Level != '999999'");
      
      String checkCode, itemCode, storeCode, date, level, remark;
      int countRecs = 0;
      
      while(rs.next())
      {
        checkCode = rs.getString(1);
        itemCode  = rs.getString(2);
        storeCode = rs.getString(3);
        date      = rs.getString(4);
        level     = rs.getString(5);
        remark    = rs.getString(6);
        
        if(level  == null) level  = "0";
        if(remark == null) remark = "0";

        directoryUtils.updateState(stateFileName, "" + generalUtils.strDPs('0', ("" + generalUtils.doubleToStr((countRecs++ / numRecs) * 100))));

        if(type.equals("S"))
        {
          processShelfStock(con, stmt, stmt2, rs, fhData, checkCode, itemCode, storeCode, date, level, remark, dpOnQuantities, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, count, cssFormat);
        }
        else processWIP(con, stmt, rs, fhData, checkCode, itemCode, storeCode, date, level, remark, dpOnQuantities, count, cssFormat);
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

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void processShelfStock(Connection con, Statement stmt, Statement stmt2, ResultSet rs, RandomAccessFile fhData, String checkCode, String itemCode, String storeCode, String date, String level, String remark, char dpOnQuantities, String unm,
                                 String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] count, String[] cssFormat)
  {
    try
    {
      String[] mfr     = new String[1];
      String[] mfrCode = new String[1];
      String[] desc    = new String[1];
      getItemDetailsGivenCode(con, stmt, rs, itemCode, mfr, mfrCode, desc);
  
      String yesterdaysDate = generalUtils.decode((generalUtils.encodeFromYYYYMMDD(date) - 1), localDefnsDir, defnsDir);

      double systemLevel = inventory.stockLevelForAStore(con, stmt, stmt2, rs, storeCode, itemCode, yesterdaysDate, unm, sid, uty, men, den, dnm, bnm);
      
      double levelD = generalUtils.doubleFromStr(level);

      if(systemLevel != levelD) // needs reconciling
      {
        if(cssFormat[0].equals("line1")) cssFormat[0] = "line2"; else cssFormat[0] = "line1";
        
        scoutln(fhData, "<tr id='" + cssFormat[0] + "'><td><p><input type=checkbox name='c" + checkCode + "'></td>");

        scoutln(fhData, "<td><p><a href=\"javascript:view('" + itemCode + "')\">" + checkCode + "</a></td>");
        scoutln(fhData, "<td><p><a href=\"javascript:viewItem('" + itemCode + "')\">" + itemCode + "</a></td>");
        scoutln(fhData, "<td><p>" + mfr[0] + "</td>");
        scoutln(fhData, "<td><p>" + mfrCode[0] + "</td>");
        scoutln(fhData, "<td align=center><p>" + generalUtils.formatNumeric(systemLevel, dpOnQuantities) + "</td>");
        scoutln(fhData, "<td align=center><p>" + generalUtils.formatNumeric(levelD, dpOnQuantities) + "</td>");
        scoutln(fhData, "<td align=center><p><font color=red size=3>" + generalUtils.formatNumeric((levelD - systemLevel), dpOnQuantities) + "</td>");
        scoutln(fhData, "<td nowrap><p>" + storeCode + "</td>");
        scoutln(fhData, "<td><p><input type=text size=20 maxlength=80 name='i" + checkCode + "' value='" + remark + "'></td>");
        scoutln(fhData, "<td nowrap><p>" + desc[0] + "</td></tr>");

        ++count[0];
      }
    }
    catch(Exception e)
    {
      System.out.println(e);
    }
  }
  
  //-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void getItemDetailsGivenCode(Connection con, Statement stmt, ResultSet rs, String itemCode, String[] mfr, String[] mfrCode, String[] desc)
                                       throws Exception
  {
    byte[] data = new byte[5000];
    
    if(inventory.getStockRecGivenCode(con, stmt, rs, itemCode, '\000', data) < 0) // just-in-case
    {
      mfr[0] = mfrCode[0] = desc[0] = "";
      return;
   }
 
    desc[0]    = generalUtils.dfsAsStr(data, (short)1);    
    mfr[0]     = generalUtils.dfsAsStr(data, (short)3);
    mfrCode[0] = generalUtils.dfsAsStr(data, (short)4);
    
    if(desc[0]    == null) desc[0] = "";
    if(mfr[0]     == null) mfr[0] = "";
    if(mfrCode[0] == null) mfrCode[0] = "";
    
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }
  private void scoutln(RandomAccessFile fh, String str) throws Exception
  {      
    fh.writeBytes(str + "\n");
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void processWIP(Connection con, Statement stmt, ResultSet rs, RandomAccessFile fhData, String checkCode, String itemCode, String storeCode, String date, String level, String remark, char dpOnQuantities, int[] count, String[] cssFormat)
  {
    try
    {
      String[] mfr     = new String[1];
      String[] mfrCode = new String[1];
      String[] desc    = new String[1];
      getItemDetailsGivenCode(con, stmt, rs, itemCode, mfr, mfrCode, desc);
  
      double systemLevel = forAllSalesOrders(con, stmt, rs, itemCode, date, generalUtils.encodeFromYYYYMMDD(date));

      double levelD = generalUtils.doubleFromStr(level);

      String colour;
      // if systemLevel != levelD then needs reconciling

      if(systemLevel != levelD)
        colour = "red";
      else colour = "";            

      if(cssFormat[0].equals("line1")) cssFormat[0] = "line2"; else cssFormat[0] = "line1";

      scoutln(fhData, "<tr id='" + cssFormat[0] + "'><td><p><input type=checkbox name='c" + checkCode + "'></td>");

      scoutln(fhData, "<td><p><a href=\"javascript:view('" + itemCode + "')\">" + checkCode + "</a></td>");
      scoutln(fhData, "<td><p><a href=\"javascript:viewItem('" + itemCode + "')\">" + itemCode + "</a></td>");
      scoutln(fhData, "<td><p>" + mfr[0] + "</td>");
      scoutln(fhData, "<td><p>" + mfrCode[0] + "</td>");
      scoutln(fhData, "<td align=center><p><a href=\"javascript:level('" + itemCode + "','" + date + "')\">" + generalUtils.formatNumeric(systemLevel, dpOnQuantities) + "</a></td>");
      scoutln(fhData, "<td align=center><p>" + generalUtils.formatNumeric(levelD, dpOnQuantities) + "</td>");
      scoutln(fhData, "<td align=center><p><font color='" + colour + "' size=3>" + generalUtils.formatNumeric((levelD - systemLevel), dpOnQuantities) + "</td>");
      scoutln(fhData, "<td nowrap><p>" + storeCode + "</td>");
      scoutln(fhData, "<td><p><input type=text size=20 maxlength=80 name='i" + checkCode + "' value='" + remark + "'></td>");
      scoutln(fhData, "<td nowrap><p>" + desc[0] + "</td></tr>");

      ++count[0];
    }
    catch(Exception e)
    {
      System.out.println(e);
    }
  }
    
  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private double forAllSalesOrders(Connection con, Statement stmt, ResultSet rs, String itemCode, String dateTo, int dateToEncoded) throws Exception
  {
    char dpOnQuantities = miscDefinitions.dpOnQuantities(con, stmt, rs, "5");

    String soCode;
    double plQty, doQty, saQty, level = 0.0;
        
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT DISTINCT t2.SOCode FROM sol AS t2 INNER JOIN so AS t1 ON t2.SOCode = t1.SOCode WHERE t2.ItemCode = '" + generalUtils.sanitiseForSQL(itemCode) + "' AND t1.Status != 'C' AND t1.Date <= {d '" + dateTo + "'}");
      
    while(rs.next())
    {
      soCode = rs.getString(1);
      
      plQty = scanPickingLists(con, stmt, rs, soCode, itemCode, dateTo, dpOnQuantities);
      doQty = scanDOs(con, stmt, rs, soCode, itemCode, dateTo, dpOnQuantities);
      // type1: picked, no DO
      level += (plQty - doQty);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    return level;
  }
  
  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private double scanPickingLists(Connection con, Statement stmt, ResultSet rs, String soCode, String itemCode, String dateTo, char dpOnQuantities) throws Exception
  {
    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT t2.QuantityPacked,            t2.PLCode FROM pll AS t2 INNER JOIN pl AS t1 ON t2.PLCode = t1.PLCode WHERE t1.Status != 'C' AND t2.SOCode = '" + soCode + "' AND t2.ItemCode = '" + itemCode + "' AND t1.Date <= {d '" + dateTo + "'}");
      
    double qty = 0;
    while(rs.next())
    {
      qty += generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(1)), dpOnQuantities);
    }
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return qty;
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private double scanDOs(Connection con, Statement stmt, ResultSet rs, String soCode, String itemCode, String dateTo, char dpOnQuantities) throws Exception
  {
    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT t2.Quantity,       t2.DOCode    FROM dol AS t2 INNER JOIN do AS t1 ON t2.DOCode = t1.DOCode "
                         + "WHERE t1.Status != 'C' AND t2.SOCode = '" + soCode + "' AND t2.ItemCode = '" + itemCode + "' AND t1.Date <= {d '" + dateTo + "'}");
      
    double qty = 0;
    
    while(rs.next())
    {
        qty += generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(1)), dpOnQuantities);
    }
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return qty;
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private double scanForStockA(Connection con, Statement stmt, ResultSet rs, String soCode, String itemCode, String dateTo, char dpOnQuantities) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT Quantity FROM stocka WHERE SOCode = '" + soCode + "' AND ItemCode = '" + itemCode + "' AND Status != 'C' AND StoreFrom = 'None' AND StoreTo != 'None' AND Date <= {d '" + dateTo + "'}");

    double qty = 0.0;

    while(rs.next())
      qty += generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(1)), dpOnQuantities);
      
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return qty;
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String scanSOForWIPOverrides(Connection con, Statement stmt, ResultSet rs, String soCode, String itemCode) throws Exception
  {
    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT t2.WIPOverride FROM sol AS t2 INNER JOIN so AS t1 ON t2.SOCode = t1.SOCode WHERE t1.Status != 'C' AND t2.SOCode = '" + soCode + "' AND t2.ItemCode = '" + itemCode + "'");
      
    String date = "";

    while(rs.next())
    {
      if(rs.getString(1) != null && rs.getString(1).length() > 0)
        date = rs.getString(1);
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return date;
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void keepChecking(PrintWriter out, String servlet, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "function chkTimer(){chkTimerID=self.setTimeout('chk()',4000);}");
      
    scoutln(out, bytesOut, "var chkreq2;");
    scoutln(out, bytesOut, "function initRequest2(url)");
    scoutln(out, bytesOut, "{if(window.XMLHttpRequest){chkreq2=new XMLHttpRequest();}else");
    scoutln(out, bytesOut, "if(window.ActiveXObject){chkreq2=new ActiveXObject(\"Microsoft.XMLHTTP\");}}");

    scoutln(out, bytesOut, "function chk(){");
    scoutln(out, bytesOut, "var url=\"http://" + men + "/central/servlet/FaxStatusDataFromReportTemp?p1=" + servlet + "&unm=\" + escape('" + unm + "') + \"&sid=\" + escape('" + sid + "') + \"&uty=\" + escape('" + uty + "') + \"&men=\" + escape('" + men
                         + "') + \"&den=\" + escape('" + den + "') + \"&bnm=\" + escape('" + bnm + "') + \"&dnm=\" + escape('" + dnm + "');");
    scoutln(out, bytesOut, "initRequest2(url);");
    scoutln(out, bytesOut, "chkreq2.onreadystatechange=processRequest2;");
    scoutln(out, bytesOut, "chkreq2.open(\"GET\",url,true);");
    scoutln(out, bytesOut, "chkreq2.send(null);}");
 
    scoutln(out, bytesOut, "function processRequest2(){");
    scoutln(out, bytesOut, "if(chkreq2.readyState==4){");
    scoutln(out, bytesOut, "if(chkreq2.status==200){");
    scoutln(out, bytesOut, "var res=chkreq2.responseXML.getElementsByTagName(\"res\")[0].childNodes[0].nodeValue;");
    scoutln(out, bytesOut, "if(res.length>0){");
    scoutln(out, bytesOut, "if(res=='.')clearTimeout(chkTimerID);else chkTimer();");
    scoutln(out, bytesOut, "var s=chkreq2.responseXML.getElementsByTagName(\"stuff\")[0].childNodes[0].nodeValue;");
    scoutln(out, bytesOut, "var messageElement=document.getElementById('stuff');");
    scoutln(out, bytesOut, "messageElement.innerHTML=s;");
    scoutln(out, bytesOut, "}}}}");
  }

  
  
}
