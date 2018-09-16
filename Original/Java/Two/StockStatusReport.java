// =======================================================================================================================================================================================================
// System: ZaraStar Product: Generate stock status report
// Module: StockStatusReport.java
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

public class StockStatusReport extends HttpServlet
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

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="", p3="", p4="", p5="";
    
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
      p1  = req.getParameter("p1");  // mfr
      p2  = req.getParameter("p2");  // dateFrom
      p3  = req.getParameter("p3");  // dateTo
      p4  = req.getParameter("p4");  // includeZero summary
      p5  = req.getParameter("p5");  // itemCode

      if(p1 == null) p1="";
      if(p2 == null) p2="";
      if(p3 == null) p3="";
      if(p4 == null) p4="Y";
      if(p5 == null) p5="";

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, p3, p4, p5, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlbt, men, den, uty, "StockStatusReport", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3062, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush(); 
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1, String p2, String p3, String p4, String p5, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');
    String workingDir    = directoryUtils.getUserDir('W', dnm, "/" + unm);
  
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
     
    Statement stmt = null, stmt2 = null, stmt3 = null;
    ResultSet rs = null, rs2 = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 3062, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "StockStatusReport", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3062, bytesOut[0], 0, "ACC:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "StockStatusReport", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3062, bytesOut[0], 0, "SID:" + p1);       
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    String fName = "Data_OutputStockStatusPage.csv";

    RandomAccessFile fh = generalUtils.create(workingDir + fName);
      
    boolean isCSV = false;///////////////////////////////true;

    boolean includeZeroLines;
    if(p4.equals("Y"))
      includeZeroLines = true;
    else includeZeroLines = false;
    
    String dateFrom, dateFromText;
    if(p2.length() == 0)
    {
      dateFrom = "1970-01-01";
      dateFromText = "Start";
    }  
    else
    {
      dateFromText = p2;
      dateFrom = generalUtils.convertDateToSQLFormat(p2);
    }

    String dateTo, dateToText;
    if(p3.length() == 0)
    {
      dateTo = "2099-12-31";
      dateToText = "Finish";
    }
    else
    {
      dateToText = p3;
      dateTo = generalUtils.convertDateToSQLFormat(p3);
    }

    generate(con, stmt, stmt2, stmt3, rs, rs2, req, out, fh, isCSV, p1, dateFrom, dateTo, p5, includeZeroLines, dateFromText, dateToText, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 3062, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);

    fh.close();
    
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void generate(Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, HttpServletRequest req, PrintWriter out, RandomAccessFile fh, boolean isCSV, String mfr,
                         String dateFrom, String dateTo, String itemCode, boolean includeZeroLines, String dateFromText, String dateToText, String unm,
                         String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir,
                         String defnsDir, int[] bytesOut) throws Exception
  {
    if(! isCSV)
    {
      scoutln(out, bytesOut, "<html><head><title>Stock Status</title>");

      scoutln(out, bytesOut, "<script language=\"JavaScript\">");
  
      scoutln(out, bytesOut, "function item(code){");
      scoutln(out, bytesOut, "window.location.href=\"/central/servlet/ProductStockRecord?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p1=\"+code;}");

      scoutln(out, bytesOut, "function wac(code){");
      scoutln(out, bytesOut, "window.location.href='/central/servlet/StockStatusReportDetail?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1='+code+'&p2="+dateFrom+"&p3="+dateTo+"';}");

      scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\"></head>");
    
      int[] hmenuCount = new int[1];
 
      pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "3062", "", "StockStatusReport", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
                                
      scoutln(out, bytesOut, "<form>");

      pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Stock Status", "3062", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

      scoutln(out, bytesOut, "<table id='page' width=100% border=0>");
    
      scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    }
    
    String baseCurrency = accountsUtils.getBaseCurrency(con, stmt, rs, dnm, localDefnsDir, defnsDir);
            
    char dpOnQuantities = miscDefinitions.dpOnQuantities(con, stmt, rs, "5");

    header(out, fh, isCSV, dpOnQuantities, baseCurrency, bytesOut) ;
    
    boolean useWAC = false;
    if(miscDefinitions.inventoryCostingMethod(con, stmt, rs).equals("WAC"))
      useWAC = true;
 
    double[] totalAmountBegin = new double[1]; totalAmountBegin[0] = 0.0;
    double[] totalAmountClose = new double[1]; totalAmountClose[0] = 0.0;
    double[] invoiceIssuedD   = new double[1]; invoiceIssuedD[0] = 0.0;
    double[] plIssuedD        = new double[1]; plIssuedD[0] = 0.0;

    if(itemCode.length() == 0)
    {
      forEachStockItem(useWAC, includeZeroLines, con, stmt, stmt2, stmt3, rs, rs2, out, isCSV, fh, mfr, dateFrom, dateTo, dpOnQuantities, totalAmountBegin, totalAmountClose, invoiceIssuedD, plIssuedD, unm, sid, uty, men, den, dnm, bnm, localDefnsDir,
                       defnsDir, bytesOut);
    }
    else
    {
      forOneStockItem(useWAC, includeZeroLines, con, stmt, stmt2, stmt3, rs, rs2, out, isCSV, fh, itemCode, dateFrom, dateTo, dpOnQuantities, totalAmountBegin, totalAmountClose, invoiceIssuedD, plIssuedD, unm, sid, uty, men, den, dnm, bnm, localDefnsDir,
                      defnsDir, bytesOut);
    }

    header(out, fh, isCSV, dpOnQuantities, baseCurrency, bytesOut) ;

    if(! isCSV)
    {
      scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
      scoutln(out, bytesOut, "<tr><td colspan=8><p>Total Valuation at Start: " + baseCurrency + " " + generalUtils.formatNumeric(totalAmountBegin[0], '2') + "</td></tr>");
      scoutln(out, bytesOut, "<tr><td colspan=8><p>Total Valuation at End: "  + baseCurrency + " " + generalUtils.formatNumeric(totalAmountClose[0], '2') + "</td></tr>");

      scoutln(out, bytesOut, "</table></form>");
      scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void forEachStockItem(boolean useWAC, boolean includeZeroLines, Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, PrintWriter out, boolean isCSV, RandomAccessFile fh, String mfr,
                                String dateFrom, String dateTo, char dpOnQuantities, double[] totalAmountBegin, double[] totalAmountClose, double[] invoiceIssuedD, double[] plIssuedD, String unm, String sid, String uty, String men, String den,
                                String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
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

    String tmpTable = unm + "_tmp";

    directoryUtils.createTmpTable(true, con, stmt, "DocumentType char(1), Date date, Level char(20), DocumentCode char(20), SOCode char(20), Description char(80) ", "", tmpTable);

    stmt = con.createStatement();

    if(mfr.equals("___ALL___"))
      rs = stmt.executeQuery("SELECT ItemCode, Description, UoM, Manufacturer, ManufacturerCode FROM stock     WHERE     WrittenOff = 'N'    ORDER BY Manufacturer, ManufacturerCode");
    else rs = stmt.executeQuery("SELECT ItemCode, Description, UoM, Manufacturer, ManufacturerCode FROM stock WHERE Manufacturer = '" + generalUtils.sanitise(mfr) + "'      AND WrittenOff = 'N'    ORDER BY ManufacturerCode");

    String itemCode, desc, uom, mfr2, mfrCode, cssFormat="";
    double wac, stockAmountClose, thisBegin, e;

    String[] purchasePrice = new String[1];
    String[] currency = new String[1];
    String[] exchangeRate = new String[1];
    String[] dateChanged = new String[1];

    while(rs.next())
    {
      itemCode = rs.getString(1);
      desc     = rs.getString(2);
      uom      = rs.getString(3);
      mfr2     = rs.getString(4);
      mfrCode  = rs.getString(5);

      if(desc    == null) desc = "";
      if(uom     == null) uom = "";
      if(mfr2    == null) mfr2 = "";
      if(mfrCode == null) mfrCode = "";

      directoryUtils.clearTmpTable(con, stmt, tmpTable);

      forAnItem(con, stmt2, stmt3, rs, itemCode, dateFrom, dateTo, begin, received, issued, adjusted, close, openingWAC, checkAdjusted, tmpTable, invoiceIssued, plIssued, totalAdjusted, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir);

      if(close[0] < 0) close[0] = 0;

      if(   begin[0] == 00 && issued[0] == 0.0 && received[0] == 0.0 && adjusted[0] == 0.0 && close[0] == 0.0 && checkAdjusted[0] == 0.0
         && ! includeZeroLines)
      {
        ;
      }
      else
      {
        if(useWAC)
        {
          wac = inventory.getWAC(con, stmt2, rs2, itemCode, dateFrom, dateTo, dnm);
        }
        else
        {
          inventory.getPurchaseDetailsGivenCode(con, stmt2, rs2, itemCode, purchasePrice, currency, exchangeRate, dateChanged);
          byte[] actualDate = new byte[20];
          e = accountsUtils.getApplicableRate(con, stmt2, rs2, currency[0], dateTo, actualDate, dnm, localDefnsDir, defnsDir);

          if(e == 0)
            e = 1;

          wac = generalUtils.doubleFromStr(purchasePrice[0]) * e;
        }
        openingWAC[0] = wac;
        totalAmountBegin[0] += generalUtils.doubleDPs(openingWAC[0] * begin[0], '2');
        thisBegin =  generalUtils.doubleDPs(openingWAC[0] * begin[0], '2');

        stockAmountClose = wac * close[0];

        if(stockAmountClose < 0)
        {
          System.out.println(itemCode + " " + mfr2 + " " + mfrCode + " " + stockAmountClose + " " + desc);
        }

        // invoice calc
        invoiceIssuedD[0] += (wac * invoiceIssued[0]);
       
        // pl calc
        plIssuedD[0] += (wac * plIssued[0]);
       
        totalAmountClose[0] += generalUtils.doubleDPs(stockAmountClose, '2');

        if(! isCSV)
        {
          if(cssFormat.equals("line1")) cssFormat = "line2"; else cssFormat = "line1";

          scoutln(out, bytesOut, "<tr id='" + cssFormat + "'><td><p><a href=\"javascript:item('" + itemCode + "')\">" + itemCode + "</td><td><p>" + mfr2 + "</td><td><p>" + mfrCode + "</td><td><p>" + uom + "</td><td align=right><p>"
                               + generalUtils.formatNumeric(begin[0], dpOnQuantities) + "</td><td align=right><p>" + generalUtils.formatNumeric(received[0], dpOnQuantities) + "</td><td align=right><p>" + generalUtils.formatNumeric(issued[0], dpOnQuantities)
                               + "</td><td align=right><p>" + generalUtils.formatNumeric(adjusted[0], dpOnQuantities) + "</td><td align=right><p>" + generalUtils.formatNumeric(close[0], dpOnQuantities) + "</td><td align=right><p><a href=\"javascript:wac('"
                               + itemCode + "')\">" + generalUtils.formatNumeric(wac, '2') + "</a></td><td align=right><p>" + generalUtils.formatNumeric(thisBegin, '2') + "</td><td align=right><p>" + generalUtils.formatNumeric(stockAmountClose, '2')
                               + "</td><td nowrap><p>" + desc + "</td></tr>");
        }
        else
        {
          writeEntry(fh, itemCode, 'S', dpOnQuantities, true, false);
          writeEntry(fh, mfr2,     'S', dpOnQuantities, true, false);
          writeEntry(fh, mfrCode,  'S', dpOnQuantities, true, false);
          writeEntry(fh, uom,      'S', dpOnQuantities, true, false);
          writeEntry(fh, generalUtils.doubleToStr(dpOnQuantities, begin[0]), 'Q', dpOnQuantities, true, false);
          writeEntry(fh, generalUtils.doubleToStr(dpOnQuantities, received[0]), 'Q', dpOnQuantities, true, false);
          writeEntry(fh, generalUtils.doubleToStr(dpOnQuantities, issued[0]), 'Q', dpOnQuantities, true, false);
          writeEntry(fh, generalUtils.doubleToStr(dpOnQuantities, adjusted[0]), 'Q', dpOnQuantities, true, false);
          writeEntry(fh, generalUtils.doubleToStr(dpOnQuantities, close[0]), 'Q', dpOnQuantities, true, false);
          writeEntry(fh, generalUtils.doubleToStr(dpOnQuantities, wac), 'Q', dpOnQuantities, true, false);
          writeEntry(fh, generalUtils.doubleToStr(dpOnQuantities, thisBegin), 'Q', dpOnQuantities, true, false);
          writeEntry(fh, generalUtils.doubleToStr(dpOnQuantities, stockAmountClose), 'Q', dpOnQuantities, true, false);
          writeEntry(fh, desc, 'S', dpOnQuantities, false, true);
        }
      }
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    directoryUtils.removeTmpTable(con, stmt, tmpTable);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void forOneStockItem(boolean useWAC, boolean includeZeroLines, Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, PrintWriter out, boolean isCSV, RandomAccessFile fh, String itemCode,
                               String dateFrom, String dateTo, char dpOnQuantities, double[] totalAmountBegin, double[] totalAmountClose, double[] invoiceIssuedD, double[] plIssuedD, String unm, String sid, String uty, String men,
                               String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
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

    String tmpTable = unm + "_tmp";

    directoryUtils.createTmpTable(true, con, stmt, "DocumentType char(1), Date date, Level char(20), DocumentCode char(20), SOCode char(20), Description char(80) ", "", tmpTable);

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT Description, UoM, Manufacturer, ManufacturerCode FROM stock WHERE ItemCode = '" + generalUtils.sanitise(itemCode) + "'                      AND WrittenOff = 'N'");

    String desc, uom, mfr2, mfrCode, cssFormat="";
    double wac, stockAmountClose, thisBegin, e;

    String[] purchasePrice = new String[1];
    String[] currency = new String[1];
    String[] exchangeRate = new String[1];
    String[] dateChanged = new String[1];

    if(rs.next())
    {
      desc     = rs.getString(1);
      uom      = rs.getString(2);
      mfr2     = rs.getString(3);
      mfrCode  = rs.getString(4);

      if(desc    == null) desc = "";
      if(uom     == null) uom = "";
      if(mfr2    == null) mfr2 = "";
      if(mfrCode == null) mfrCode = "";

      directoryUtils.clearTmpTable(con, stmt, tmpTable);

      forAnItem(con, stmt2, stmt3, rs, itemCode, dateFrom, dateTo, begin, received, issued, adjusted, close, openingWAC, checkAdjusted, tmpTable, invoiceIssued, plIssued, totalAdjusted, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir);


if(close[0] < 0) close[0] = 0;


      if(   begin[0] == 0.0 && issued[0] == 0.0 && received[0] == 0.0 && adjusted[0] == 0.0 && close[0] == 0.0 && checkAdjusted[0] == 0.0
         && ! includeZeroLines)
      {
        ;
      }
      else
      {
        if(useWAC)
        {
          wac = inventory.getWAC(con, stmt2, rs2, itemCode, dateFrom, dateTo, dnm);
        }
        else
        {
          //wac = 0.0;

          inventory.getPurchaseDetailsGivenCode(con, stmt2, rs2, itemCode, purchasePrice, currency, exchangeRate, dateChanged);
          byte[] actualDate = new byte[20];
          e = accountsUtils.getApplicableRate(con, stmt2, rs2, currency[0], dateTo, actualDate, dnm, localDefnsDir, defnsDir);
          System.out.println(dateTo + " " + purchasePrice[0] + " " + e);

          if(e == 0)
            e = 1;

          wac = generalUtils.doubleFromStr(purchasePrice[0]) * e;
        }

        stockAmountClose = wac * close[0];
        openingWAC[0] = wac;
        totalAmountBegin[0] += generalUtils.doubleDPs(openingWAC[0] * begin[0], '2');
        thisBegin =  generalUtils.doubleDPs(openingWAC[0] * begin[0], '2');
        
        // invoice calc
        invoiceIssuedD[0] += (wac * invoiceIssued[0]);

        // pl calc
        plIssuedD[0] += (wac * plIssued[0]);
        //

        totalAmountClose[0] += generalUtils.doubleDPs(stockAmountClose, '2');

        if(! isCSV)
        {
          if(cssFormat.equals("line1")) cssFormat = "line2"; else cssFormat = "line1";

          scoutln(out, bytesOut, "<tr id='" + cssFormat + "'><td><p><a href=\"javascript:item('" + itemCode + "')\">" + itemCode + "</td><td><p>" + mfr2 + "</td><td><p>" + mfrCode + "</td><td><p>" + uom + "</td><td align=right><p>"
                               + generalUtils.formatNumeric(begin[0], dpOnQuantities) + "</td><td align=right><p>" + generalUtils.formatNumeric(received[0], dpOnQuantities) + "</td><td align=right><p>" + generalUtils.formatNumeric(issued[0], dpOnQuantities)
                               + "</td><td align=right><p>" + generalUtils.formatNumeric(adjusted[0], dpOnQuantities) + "</td><td align=right><p>" + generalUtils.formatNumeric(close[0], dpOnQuantities) + "</td><td align=right><p><a href=\"javascript:wac('"
                               + itemCode + "')\">" + generalUtils.formatNumeric(wac, '2') + "</a></td><td align=right><p>" + generalUtils.formatNumeric(thisBegin, '2') + "</td><td align=right><p>" + generalUtils.formatNumeric(stockAmountClose, '2')
                               + "</td><td nowrap><p>" + desc + "</td></tr>");
        }
        else
        {
          writeEntry(fh, itemCode, 'S', dpOnQuantities, true, false);
          writeEntry(fh, mfr2,     'S', dpOnQuantities, true, false);
          writeEntry(fh, mfrCode,  'S', dpOnQuantities, true, false);
          writeEntry(fh, uom,      'S', dpOnQuantities, true, false);
          writeEntry(fh, generalUtils.doubleToStr(dpOnQuantities, begin[0]), 'Q', dpOnQuantities, true, false);
          writeEntry(fh, generalUtils.doubleToStr(dpOnQuantities, received[0]), 'Q', dpOnQuantities, true, false);
          writeEntry(fh, generalUtils.doubleToStr(dpOnQuantities, issued[0]), 'Q', dpOnQuantities, true, false);
          writeEntry(fh, generalUtils.doubleToStr(dpOnQuantities, adjusted[0]), 'Q', dpOnQuantities, true, false);
          writeEntry(fh, generalUtils.doubleToStr(dpOnQuantities, close[0]), 'Q', dpOnQuantities, true, false);
          writeEntry(fh, generalUtils.doubleToStr(dpOnQuantities, wac), 'Q', dpOnQuantities, true, false);
          writeEntry(fh, generalUtils.doubleToStr(dpOnQuantities, thisBegin), 'Q', dpOnQuantities, true, false);
          writeEntry(fh, generalUtils.doubleToStr(dpOnQuantities, stockAmountClose), 'Q', dpOnQuantities, true, false);
          writeEntry(fh, desc, 'S', dpOnQuantities, false, true);
        }
      }
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    directoryUtils.removeTmpTable(con, stmt, tmpTable);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void forAnItem(Connection con, Statement stmt, Statement stmt2, ResultSet rs, String itemCode, String dateFrom, String dateTo, double[] begin, double[] received, double[] issued, double[] adjusted, double[] close, double[] openingWAC,
                         double[] checkAdjusted, String tmpTable, double[] invoiceIssued, double[] plIssued, double[] totalAdjusted, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir,
                         String defnsDir) throws Exception
  {
    begin[0] = received[0] = issued[0] = adjusted[0] = close[0] = openingWAC[0] = checkAdjusted[0] = 0.0;
    invoiceIssued[0] = 0.0;totalAdjusted[0] = 0.0;
    plIssued[0] = 0.0;

    int encoded = generalUtils.encodeFromYYYYMMDD(dateFrom);
    --encoded;

    begin[0] = inventory.stockLevelForAStore(con, stmt, stmt2, rs, "", itemCode, generalUtils.decode(encoded, localDefnsDir, defnsDir), unm, sid, uty, men, den, dnm, bnm); // (dateFrom - 1) ???

    getPickingLists(con, stmt, stmt2, rs, itemCode, dateFrom, dateTo, tmpTable);

    getGoodsReceivedNotes(con, stmt, stmt2, rs, itemCode, dateFrom, dateTo, tmpTable);

    getStockAdjustmentRecords(con, stmt, rs, itemCode, dateFrom, dateTo, tmpTable);

    getStockCheckRecords(con, stmt, rs, itemCode, dateFrom, dateTo, tmpTable);
    
    getInvoices(con, stmt, stmt2, rs, itemCode, dateFrom, dateTo, tmpTable); 
    
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT DocumentType, Level, DocumentCode, SOCode, Date, Description FROM " + tmpTable + " ORDER BY Date");

    char type;
    String level, code, soCode, date, desc;
    double closeNow = 0.0, closeNow2, thisAdjusted, levelD;
    boolean first = true;

    while(rs.next())
    {
      type   = rs.getString(1).charAt(0);
      level  = rs.getString(2);
      code   = rs.getString(3);
      soCode = rs.getString(4);
      date   = rs.getString(5);
      desc   = rs.getString(6);

      // qty, either in In column or Out column
        
      if(type == 'P') // PL
      {
        levelD = generalUtils.doubleFromStr(level);
        issued[0] += levelD;
        plIssued[0] += generalUtils.doubleFromStr(level);
        
        closeNow = begin[0] + adjusted[0] - issued[0] + received[0] ;//   + checkAdjusted[0];
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
    rs = stmt.executeQuery("SELECT t1.Date, t2.QuantityPacked, t2.PLCode, t2.SOCode, t1.CompanyName FROM pl AS t1 INNER JOIN pll AS t2 ON t1.PLCode = t2.PLCode "
                         + "WHERE t2.ItemCode = '" + generalUtils.sanitiseForSQL(itemCode) + "' AND t1.Date >= {d '" + dateFrom + "'} AND t1.Date <= {d '" + dateTo + "'} AND t1.Status != 'C' AND t1.Completed = 'Y' ORDER BY t1.Date");
    
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
                         + "WHERE t2.ItemCode = '" + generalUtils.sanitiseForSQL(itemCode) + "'" + " AND t1.Date >= {d '" + dateFrom + "'}  AND t1.Date <= {d '" + dateTo
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
                         + "WHERE t2.ItemCode = '" + generalUtils.sanitiseForSQL(itemCode) + "' AND t1.Date >= {d '" + dateFrom + "'} AND t1.Date <= {d '" + dateTo
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
    rs = stmt.executeQuery("SELECT Date, StoreFrom, StoreTo, Quantity, AdjustmentCode, Remark FROM stocka WHERE ItemCode = '" + generalUtils.sanitiseForSQL(itemCode) + "' AND Date >= {d '"
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
  private void getStockCheckRecords(Connection con, Statement stmt, ResultSet rs, String itemCode, String dateFrom, String dateTo, String tmpTable) throws Exception
  {
    stmt = con.createStatement();
    rs = stmt.executeQuery("SELECT CheckCode, Date, Level, Remark FROM stockc WHERE ItemCode = '" + generalUtils.sanitiseForSQL(itemCode) + "' AND Date >= {d '" + dateFrom
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
    
    String q = "INSERT INTO " + tmpTable + " ( DocumentType, Date, Level, DocumentCode, SOCode, Description ) VALUES ('" + docType + "', {d '" + date + "'},'" + level + "','" + docCode + "','" + soCode + "','" + generalUtils.sanitiseForSQL(desc)
             + "' )";

    stmt.executeUpdate(q);
  
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void header(PrintWriter out, RandomAccessFile fh, boolean isCSV, char dpOnQuantities, String baseCurrency, int[] bytesOut) throws Exception
  {
    if(isCSV)
    {  
      writeEntry(fh, "Item Code", 'S', dpOnQuantities, true, false);
      writeEntry(fh, "Manufacturer", 'S', dpOnQuantities, true, false);
      writeEntry(fh, "Mfr Code", 'S', dpOnQuantities, true, false);
      writeEntry(fh, "UoM", 'S', dpOnQuantities, true, false);
      writeEntry(fh, "Begin", 'S', dpOnQuantities, true, false);
      writeEntry(fh, "Received", 'S', dpOnQuantities, true, false);
      writeEntry(fh, "Issued", 'S', dpOnQuantities, true, false);
      writeEntry(fh, "Adjusted", 'S', dpOnQuantities, true, false);
      writeEntry(fh, "Close", 'S', dpOnQuantities, true, false);
      writeEntry(fh, "Purchase Price " + baseCurrency, 'S', dpOnQuantities, true, false);
      writeEntry(fh, "Begin " + baseCurrency, 'S', dpOnQuantities, true, false);
      writeEntry(fh, "Close " + baseCurrency, 'S', dpOnQuantities, true, false);
      writeEntry(fh, "Description", 'S', dpOnQuantities, false, true);
    }
    else
    {
      scoutln(out, bytesOut, "<tr id='pageColumn'><td><p>Item Code</td>");
      scoutln(out, bytesOut, "<td><p>Manufacturer</td>");
      scoutln(out, bytesOut, "<td><p>Mfr Code</td>");
      scoutln(out, bytesOut, "<td><p>UoM</td>");
      scoutln(out, bytesOut, "<td align=right><p>Begin</td>");
      scoutln(out, bytesOut, "<td align=right><p>Received</td>");
      scoutln(out, bytesOut, "<td align=right><p>Issued</td>");
      scoutln(out, bytesOut, "<td align=right><p>Adjusted</td>");
      scoutln(out, bytesOut, "<td align=right><p>Close</td>");
      scoutln(out, bytesOut, "<td align=right><p>WAC " + baseCurrency + "</td>");
      scoutln(out, bytesOut, "<td align=right><p>Begin " + baseCurrency + "</td>");
      scoutln(out, bytesOut, "<td align=right><p>Close " + baseCurrency + "</td>");
      scoutln(out, bytesOut, "<td><p>Description</td></tr>");    
    }
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void writeEntry(RandomAccessFile fh, String entry, char type, char dpOnQuantities, boolean comma, boolean newLine) throws Exception
  {
    if(entry == null) entry = "";

    switch(type)
    {
      case 'D' : entry = generalUtils.convertFromYYYYMMDD(entry);       break;
      case 'Q' : entry = generalUtils.doubleDPs(entry, dpOnQuantities); break;
      case 'V' : entry = generalUtils.doubleDPs(entry, '2');            break;
    }

    fh.writeBytes("\"");
    for(int x=0;x<entry.length();++x)
    {
      if(entry.charAt(x) == '"')
        fh.writeBytes("''");
      else fh.writeBytes("" + entry.charAt(x));
    }

    fh.writeBytes("\"");

    if(comma)
      fh.writeBytes(",");

    if(newLine)
      fh.writeBytes("\n");
  }

}
