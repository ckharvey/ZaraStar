// =======================================================================================================================================================================================================
// System: ZaraStar Product: Generate stock status report - detail for an item
// Module: StockStatusReportDetail.java
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

public class StockStatusReportDetail extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  Inventory inventory = new Inventory();
  MiscDefinitions miscDefinitions = new MiscDefinitions();
  AccountsUtils accountsUtils = new AccountsUtils();
  
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="", p3="";
    
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
      p1  = req.getParameter("p1");  // itemCode
      p2  = req.getParameter("p2");  // dateFrom
      p3  = req.getParameter("p3");  // dateTo

      if(p1 == null) p1="";
      if(p2 == null) p2="";
      if(p3 == null) p3="";
      
      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, p3, bytesOut);
    }
    catch(Exception e)
    {
      StringBuffer url = req.getRequestURL();
      int x=0;
      if(url.charAt(x) == 'h' || url.charAt(x) == 'H')
        x += 7;
      String urlbt="";
      while(url.charAt(x) != '\000' && url.charAt(x) != ',' && url.charAt(x) != '/')
        urlbt += url.charAt(x++);

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlbt, men, den, uty, "StockStatusReportDetail", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3062, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush(); 
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1,
                    String p2, String p3, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');
  
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    
    Statement stmt = null, stmt2 = null, stmt3 = null;
    ResultSet rs = null, rs2 = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 3062, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "StockStatusReportDetail", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3062, bytesOut[0], 0, "ACC:" + p1);
      
      if(con != null) con.close();
     if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "StockStatusReportDetail", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3062, bytesOut[0], 0, "SID:" + p1);
       
      if(con != null) con.close();
     if(out != null) out.flush(); 
      return;
    }

    String dateFromText;
    if(p2.equals("1970-01-01"))
      dateFromText = "Start";
    else dateFromText = p2;

    String dateToText;
    if(p3.equals("2099-12-31"))
      dateToText = "Finish";
    else dateToText = p3;
  
    generate(con, stmt, stmt2, stmt3, rs, rs2, req, out, p1, p2, p3, dateFromText, dateToText, unm, sid, uty, men, den, dnm, bnm, 
             localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 3062, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
      
      if(con != null) con.close();
   if(out != null) out.flush(); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void generate(Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, HttpServletRequest req, PrintWriter out,
                        String itemCode, String dateFrom, String dateTo, String dateFromText, String dateToText, String unm,
                         String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir,
                         String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Stock Status</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");
  
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3025, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function gr(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/GoodsReceivedPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den
                          + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3038, unm, uty, dnm, localDefnsDir, defnsDir))
    {
       scoutln(out, bytesOut, "function pl(code){");
       scoutln(out, bytesOut, "var p1=sanitise(code);");
       scoutln(out, bytesOut, "this.location.href=\"/central/servlet/GoodsReceivedPickingList?unm=" + unm + "&sid=" + sid + "&uty="
                            + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3011, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function adj(code){");
      scoutln(out, bytesOut, "var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/StockAdjustmentItem?unm=" + unm + "&sid=" + sid + "&uty="
                           + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + itemCode + "\";}");
    }
  
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3018, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function chk(code){");
      scoutln(out, bytesOut, "var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/StockCheckForItem?unm=" + unm + "&sid=" + sid + "&uty="
                           + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + itemCode + "\";}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3001, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function item(code){");
      scoutln(out, bytesOut, "var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/ProductStockRecord?unm=" + unm + "&sid=" + sid + "&uty="
                           + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4031, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function so(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/SalesOrderPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm="
                           + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    scoutln(out, bytesOut, "function sanitise(code){");
    scoutln(out, bytesOut, "var code2='';var x;var len=code.length;");
    scoutln(out, bytesOut, "for(x=0;x<len;++x)");
    scoutln(out, bytesOut, "if(code.charAt(x)=='#')code2+='%23';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='\"')code2+='%22';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='&')code2+='%26';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='%')code2+='%25';");
    scoutln(out, bytesOut, "else if(code.charAt(x)==' ')code2+='%20';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='?')code+='%3F';");
    scoutln(out, bytesOut, "else code2+=code.charAt(x);");
    scoutln(out, bytesOut, "return code2};");
  
    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\""
                           + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\"></head>");
  
    int[] hmenuCount = new int[1];
 
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "3062", "", "StockStatusReportDetail", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount,
                          bytesOut);
                                
    scoutln(out, bytesOut, "<form>");

    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Stock Status", "3062", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    String baseCurrency = accountsUtils.getBaseCurrency(con, stmt, rs, dnm, localDefnsDir, defnsDir);
            
    scoutln(out, bytesOut, "<table id='page' width=100% border=0>");
    
    char dpOnQuantities = miscDefinitions.dpOnQuantities(con, stmt, rs, "5");
    
    boolean useWAC = false;
    if(miscDefinitions.inventoryCostingMethod(con, stmt, rs).equals("WAC"))
      useWAC = true;
 
    double[] totalAmountBegin = new double[1]; totalAmountBegin[0] = 0.0;
    double[] totalAmountClose = new double[1]; totalAmountClose[0] = 0.0;
    double[] invoiceIssuedD   = new double[1]; invoiceIssuedD[0] = 0.0;
    double[] plIssuedD        = new double[1]; plIssuedD[0] = 0.0;
           
    forAStockItem(useWAC, con, stmt, stmt2, stmt3, rs, rs2, out, itemCode, dateFrom, dateTo, dpOnQuantities, baseCurrency, unm, dnm, totalAmountBegin,
                     totalAmountClose, invoiceIssuedD, plIssuedD, bytesOut);

    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void forAStockItem(boolean useWAC, Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, PrintWriter out,
                             String itemCode, String dateFrom, String dateTo, char dpOnQuantities, String baseCurrency, String unm, String dnm, double[] totalAmountBegin,
                             double[] totalAmountClose, double[] invoiceIssuedD, double[] plIssuedD, int[] bytesOut) throws Exception
  {
    double[] begin         = new double[1];
    double[] received      = new double[1];
    double[] issued        = new double[1];
    double[] adjusted      = new double[1];
    double[] close         = new double[1];
    double[] openingWAC    = new double[1];
    double[] checkAdjusted = new double[1];
    double[] totalAdjusted = new double[1]; 
    
    double[] invoiceIssued = new double[1];
    double[] plIssued      = new double[1];
       
    String tmpTable = unm + "2_tmp";
      
    directoryUtils.createTmpTable(true, con, stmt, "DocumentType char(1), Date date, Level char(20), DocumentCode char(20), SOCode char(20), Description char(80) ", "",
                         tmpTable);

    stmt = con.createStatement();
 
    rs = stmt.executeQuery("SELECT Description, UoM, Manufacturer, ManufacturerCode FROM stock WHERE ItemCode = '" + itemCode + "'");

    String desc, uom, mfr, mfrCode, cssFormat="";
    double wac, stockAmountClose;

    if(rs.next())
    {
      desc    = rs.getString(1);
      uom     = rs.getString(2);
      mfr     = rs.getString(3);
      mfrCode = rs.getString(4);

      if(desc    == null) desc = "";
      if(uom     == null) uom = "";
      if(mfr    == null) mfr = "";
      if(mfrCode == null) mfrCode = "";

      directoryUtils.clearTmpTable(con, stmt, tmpTable);

      forAnItem(con, stmt2, stmt3, rs, out, itemCode, mfr, uom, dateFrom, dateTo, begin, received, issued, adjusted, close, openingWAC,
                checkAdjusted, tmpTable, invoiceIssued, plIssued, dpOnQuantities, totalAdjusted, bytesOut);
          
      if(begin[0] == 00 && issued[0] == 0.0 && received[0] == 0.0 && adjusted[0] == 0.0 && close[0] == 0.0 && checkAdjusted[0] == 0.0)
      {
        ;
      }
      else
      {
        totalAmountBegin[0] += generalUtils.doubleDPs(openingWAC[0] * begin[0], '2');

        if(useWAC)
          wac = inventory.getWAC(con, stmt2, rs2, itemCode, dateFrom, dateTo, dnm);
        else wac = 0.0;
        stockAmountClose = wac * close[0];

        if(stockAmountClose < 0)
        {
          System.out.println(itemCode + " " + mfr + " " + mfrCode + " " + stockAmountClose + " " + desc);    
        }

        // invoice calc
        invoiceIssuedD[0] += (wac * invoiceIssued[0]);
        //
        // pl calc
        plIssuedD[0] += (wac * plIssued[0]);
        //
        
        totalAmountClose[0] += generalUtils.doubleDPs(stockAmountClose, '2');
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr id='pageColumn'><td><p>Item Code</td>");
    scoutln(out, bytesOut, "<td><p>Manufacturer</td>");
    scoutln(out, bytesOut, "<td><p>Mfr Code</td>");
    scoutln(out, bytesOut, "<td><p>UoM</td>");
    scoutln(out, bytesOut, "<td align=center><p>Begin</td>");
    scoutln(out, bytesOut, "<td align=center><p>Received</td>");
    scoutln(out, bytesOut, "<td align=center><p>Issued</td>");
    scoutln(out, bytesOut, "<td align=center><p>Adjusted</td>");
    scoutln(out, bytesOut, "<td align=center><p>Close</td>");
    scoutln(out, bytesOut, "<td align=center><p>WAC " + baseCurrency + "</td>");
    scoutln(out, bytesOut, "<td align=center><p>Begin Valuation " + baseCurrency + "</td>");
    scoutln(out, bytesOut, "<td align=center><p>Close Valuation " + baseCurrency + "</td>");
    scoutln(out, bytesOut, "<td><p>Description</td></tr>");    


        if(cssFormat.equals("line1")) cssFormat = "line2"; else cssFormat = "line1";

        scoutln(out, bytesOut, "<tr id='" + cssFormat + "'><td><p><a href=\"javascript:item('" + itemCode + "')\">" + itemCode + "</a></td><td><p>" + mfr
                             + "</td><td><p>" + mfrCode + "</td><td><p>" + uom + "</td><td align=center><p>" + generalUtils.formatNumeric(begin[0], dpOnQuantities)
                             + "</td><td align=center><p>" + generalUtils.formatNumeric(received[0], dpOnQuantities) + "</td><td align=center><p>"
                             + generalUtils.formatNumeric(issued[0], dpOnQuantities) + "</td><td align=center><p>" + generalUtils.formatNumeric(adjusted[0], dpOnQuantities)
                             + "</td><td align=center><p>" + generalUtils.formatNumeric(close[0], dpOnQuantities) + "</td><td align=center><p>" 
                             + generalUtils.formatNumeric(wac, '2') + "</td><td align=center><p>" + generalUtils.formatNumeric(totalAmountBegin[0], '2')
                             + "</td><td align=center><p>" + generalUtils.formatNumeric(stockAmountClose, '2') + "</td><td nowrap><p>" + desc +
                             "</td></tr>");
      }
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    directoryUtils.removeTmpTable(con, stmt, tmpTable);
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void forAnItem(Connection con, Statement stmt, Statement stmt2, ResultSet rs, PrintWriter out, String itemCode, String mfr, String uom,
                        String dateFrom, String dateTo, double[] begin, double[] received, double[] issued, double[] adjusted,
                         double[] close, double[] openingWAC, double[] checkAdjusted, String tmpTable, double[] invoiceIssued, double[] plIssued,
                         char dpOnQuantities, double[] totalAdjusted, int[] bytesOut) throws Exception
  {
    begin[0] = received[0] = issued[0] = adjusted[0] = close[0] = openingWAC[0] = checkAdjusted[0] = 0.0;
    invoiceIssued[0] = 0.0;totalAdjusted[0] = 0.0;
    plIssued[0] = 0.0;

    double[] openingLevel = new double[1];

    inventory.getWACDetailsGivenCode(con, stmt, rs, itemCode, openingLevel, openingWAC, dateFrom);
    begin[0] = openingLevel[0];

    getPickingLists(con, stmt, stmt2, rs, itemCode, dateFrom, dateTo, tmpTable);

    getGoodsReceivedNotes(con, stmt, stmt2, rs, itemCode, dateFrom, dateTo, tmpTable);

    getStockAdjustmentRecords(con, stmt, rs, itemCode, dateFrom, dateTo, tmpTable);

    getStockCheckRecords(con, stmt, rs, itemCode, dateFrom, dateTo, tmpTable);

    
    getInvoices(con, stmt, stmt2, rs, itemCode, dateFrom, dateTo, tmpTable);

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr id='pageColumn'><td><p>Type</td>");
    scoutln(out, bytesOut, "<td><p>Date</td>");
    scoutln(out, bytesOut, "<td><p>Code</td>");
    scoutln(out, bytesOut, "<td><p>Reference</td>");
    scoutln(out, bytesOut, "<td align=center><p>Begin</td>");
    scoutln(out, bytesOut, "<td align=center><p>Received</td>");
    scoutln(out, bytesOut, "<td align=center><p>Issued</td>");
    scoutln(out, bytesOut, "<td align=center><p>Adjusted</td>");
    scoutln(out, bytesOut, "<td align=center><p>Close</td>");
   scoutln(out, bytesOut, "</tr>");    
      
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT DocumentType, Level, DocumentCode, SOCode, Date, Description FROM " + tmpTable + " ORDER BY Date");

    char type;
    String level, code, soCode, date, cssFormat= "";
    double closeNow = 0.0, closeNow2, thisAdjusted, levelD;
    boolean first = true;

    while(rs.next())
    {
      type   = rs.getString(1).charAt(0);
      level  = rs.getString(2);
      code   = rs.getString(3);
      soCode = rs.getString(4);
      date   = rs.getString(5);

      // qty, either in In column or Out column
        
      if(type == 'P') // PL
      {
        levelD = generalUtils.doubleFromStr(level);
        issued[0] += levelD;
        plIssued[0] += generalUtils.doubleFromStr(level);
        
        closeNow = begin[0] + adjusted[0] - issued[0] + received[0] ;//   + checkAdjusted[0];

        if(levelD != 0.0)
        {
          if(cssFormat.equals("line1")) cssFormat = "line2"; else cssFormat = "line1";
          scoutln(out, bytesOut, "<tr id='" + cssFormat + "'><td><p>Picking List</td><td><p>" + date + "</td><td><p><a href=\"javascript:pl('" + code
                                 + "')\">" + code + "</a></td><td><p><a href=\"javascript:so('" + soCode + "')\">" + soCode + "</a></td><td>"
                                 + "</td><td></td><td align=center><p>" + generalUtils.formatNumeric(levelD,  dpOnQuantities) + "</td><td></td><td align=center><p>"
                                 + generalUtils.formatNumeric(closeNow, dpOnQuantities) + "</td></tr>");
        }
      }
      else
      if(type == 'V') // invoices
      {
        levelD = generalUtils.doubleFromStr(level);
        invoiceIssued[0] += levelD;
      }
      else
      if(type == 'G') // GRN
      {
        levelD = generalUtils.doubleFromStr(level);
        received[0] += levelD;
        
        closeNow = begin[0] + adjusted[0] - issued[0] + received[0]  ;//  + checkAdjusted[0];
        if(levelD != 0.0)
        {
          if(cssFormat.equals("line1")) cssFormat = "line2"; else cssFormat = "line1";
         
          scoutln(out, bytesOut, "<tr id='" + cssFormat + "'><td><p>Goods Received Note</td><td><p>" + date + "</td><td><p><a href=\"javascript:gr('" + code
                               + "')\">" + code + "</a></td><td><p>" + soCode + "</td><td></td><td align=center><p>"
                               + generalUtils.formatNumeric(levelD, dpOnQuantities) + "</td><td></td><td></td><td align=center><p>"
                               + generalUtils.formatNumeric(closeNow, dpOnQuantities) + "</td></tr>");
        }
      }  
      else
      if(type == 'C') // check
      {          
        // calc to adjust by how many
        closeNow2 = begin[0] + adjusted[0] - issued[0] + received[0] ;//+ totalAdjusted[0];//   + checkAdjusted[0];
          
        levelD = generalUtils.doubleFromStr(level);
        
        closeNow = begin[0] + adjusted[0] - issued[0] + received[0]    + levelD;//+totalAdjusted[0];//   + checkAdjusted[0];
        if(first)
        {
          thisAdjusted = 0;
          closeNow = levelD;
        }
        else
        {
          thisAdjusted = levelD - closeNow2;
          adjusted[0] += thisAdjusted;
          closeNow = levelD;
        }

          if(levelD != 0.0)
          {
        if(cssFormat.equals("line1")) cssFormat = "line2"; else cssFormat = "line1";
          scoutln(out, bytesOut, "<tr id='" + cssFormat + "'><td><p>Check</td><td><p>" + date + "</td><td><p><a href=\"javascript:chk('" + code + "')\">" + code + "</a></td><td></td><td></td><td></td><td align=center><p>"
                                   + generalUtils.formatNumeric(thisAdjusted, dpOnQuantities) + "</td><td align=center><p>" + generalUtils.formatNumeric(closeNow, dpOnQuantities)
                                   + "</td></tr>");
          }
      }
      else // adjustment
      {
        levelD = generalUtils.doubleFromStr(level);
        if(type == 'I') // is an In to this store
        {
          adjusted[0] += levelD;
          totalAdjusted[0] += levelD;
        }
        else // is an Out from this store
        {
          adjusted[0] -= levelD;
     totalAdjusted[0] -= levelD;
        }      
        
        closeNow = begin[0] + adjusted[0] - issued[0] + received[0]    + checkAdjusted[0];
        if(levelD != 0.0)
        {
          if(cssFormat.equals("line1")) cssFormat = "line2"; else cssFormat = "line1";
          scoutln(out, bytesOut, "<tr id='" + cssFormat + "'><td><p>Check</td><td><p>" + date + "</td><td><p><a href=\"javascript:adj('" + code + "')\">" + code + "</a></td><td></td><td></td><td></td><td align=center><p>"
                                   + generalUtils.formatNumeric(adjusted[0], dpOnQuantities) + "</td><td align=center><p>" + generalUtils.formatNumeric(levelD, dpOnQuantities)
                                   + "</td></tr>");
          }
      }
      first = false;
    }

    close[0] = closeNow;
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void getPickingLists(Connection con, Statement stmt, Statement stmt2, ResultSet rs, String itemCode, String dateFrom, String dateTo, String tmpTable)
                               throws Exception
  {
    stmt = con.createStatement();
    rs = stmt.executeQuery("SELECT t1.Date, t2.QuantityPacked, t2.PLCode, t2.SOCode, t1.CompanyName FROM pl AS t1 INNER JOIN pll AS t2 ON t1.PLCode = t2.PLCode"
                         + " WHERE t2.ItemCode = '" + itemCode + "'" + " AND t1.Date >= {d '" + dateFrom + "'}  AND t1.Date <= {d '" + dateTo
                         + "'} AND t1.Status != 'C' " + "AND t1.Completed = 'Y' ORDER BY t1.Date");
    
    while(rs.next())
      addToTmpTable(con, stmt2, "P", rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), tmpTable);
      
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void getInvoices(Connection con, Statement stmt, Statement stmt2, ResultSet rs, String itemCode, String dateFrom, String dateTo, String tmpTable)
                           throws Exception
  {
    stmt = con.createStatement();
    rs = stmt.executeQuery("SELECT t1.Date, t2.Quantity, t2.InvoiceCode FROM invoice AS t1 INNER JOIN invoicel AS t2 ON t1.InvoiceCode = t2.InvoiceCode "
                         + "WHERE t2.ItemCode = '" + itemCode + "'" + " AND t1.Date >= {d '" + dateFrom + "'}  AND t1.Date <= {d '" + dateTo
                         + "'} AND t1.Status != 'C' ORDER BY t1.Date");
    
    while(rs.next())
      addToTmpTable(con, stmt2, "V", rs.getString(1), rs.getString(2), rs.getString(3), "", "", tmpTable);
      
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void getGoodsReceivedNotes(Connection con, Statement stmt, Statement stmt2, ResultSet rs, String itemCode, String dateFrom, String dateTo,
                                     String tmpTable) throws Exception
  {
    stmt = con.createStatement();
    rs = stmt.executeQuery("SELECT t1.Date, t2.Quantity, t2.GRCode, t1.CompanyName FROM gr AS t1 INNER JOIN grl AS t2 ON t1.GRCode = t2.GRCode "
                         + "WHERE t2.ItemCode = '" + itemCode + "'" + " AND t1.Date >= {d '" + dateFrom + "'} AND t1.Date <= {d '" + dateTo
                         + "'}  AND t1.Status != 'C' AND t1.StockProcessed = 'Y' ORDER BY t1.Date");
    
    while(rs.next())
      addToTmpTable(con, stmt2, "G", rs.getString(1), rs.getString(2), rs.getString(3), "", rs.getString(4), tmpTable);
      
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void getStockAdjustmentRecords(Connection con, Statement stmt, ResultSet rs, String itemCode, String dateFrom, String dateTo, String tmpTable)
                                         throws Exception
  {
    stmt = con.createStatement();
    rs = stmt.executeQuery("SELECT Date, StoreFrom, StoreTo, Quantity, AdjustmentCode, Remark FROM stocka WHERE ItemCode = '" + itemCode + "' AND Date >= {d '"
                         + dateFrom + "'} AND Date <= {d '" + dateTo + "'}  AND Status != 'C' ORDER BY Date");
                                      
    while(rs.next())
    {
      // if StoreFrom has value mark as 'O' (transfer out)

      if(! rs.getString(2).equals("None"))
        addToTmpTable(con, stmt, "O", rs.getString(1), rs.getString(4), rs.getString(5), "", rs.getString(6), tmpTable);
        
      // if StoreTo has value mark as 'I' (transfer in)
      if(! rs.getString(3).equals("None"))
        addToTmpTable(con, stmt, "I", rs.getString(1), rs.getString(4), rs.getString(5), "", rs.getString(6), tmpTable);
    }
                                        
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }
    
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void getStockCheckRecords(Connection con, Statement stmt, ResultSet rs, String itemCode, String dateFrom, String dateTo, String tmpTable)
                                    throws Exception
  {
    stmt = con.createStatement();
    rs = stmt.executeQuery("SELECT CheckCode, Date, Level, Remark FROM stockc WHERE ItemCode = '" + itemCode + "' AND Date >= {d '" + dateFrom
                         + "'} AND Date <= {d '" + dateTo + "'}  AND Status != 'C' AND Type = 'S' AND Level != '999999' ORDER BY Date");
                                      
    while(rs.next())
      addToTmpTable(con, stmt, "C", rs.getString(2), rs.getString(3), rs.getString(1), "", rs.getString(4), tmpTable);
                                        
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }
    
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void addToTmpTable(Connection con, Statement stmt, String docType, String date, String level, String docCode, String soCode, String desc,
                             String tmpTable) throws Exception
  {
    stmt = con.createStatement();
    
   if(desc.length() > 80)
     desc = desc.substring(0, 80);
    
    String q = "INSERT INTO " + tmpTable + " ( DocumentType, Date, Level, DocumentCode, SOCode, Description ) VALUES ('" + docType + "', {d '" + date
             + "'},'" + level + "','" + docCode + "','" + soCode + "','" + generalUtils.sanitiseForSQL(desc) + "' )";

    stmt.executeUpdate(q);
  
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
