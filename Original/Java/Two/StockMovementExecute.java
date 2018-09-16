// =======================================================================================================================================================================================================
// System: ZaraStar Product: Stock Movement: do it
// Module: StockMovementExecute.java
// Author: C.K.Harvey
// Copyright (c) 2002-13 Christopher Harvey. All Rights Reserved.
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

public class StockMovementExecute extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  MiscDefinitions miscDefinitions = new MiscDefinitions();
  AccountsUtils accountsUtils = new AccountsUtils();
  Inventory inventory = new Inventory();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", mfr="";
    
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
        if(name.equals("mfr"))
          mfr = value[0];
        else
        if(name.length() > 2 && name.substring(1, 3).equals(".x")) ;
        else
        if(name.length() > 2 && name.substring(1, 3).equals(".y")) ;
      }
      
      doIt(out, req, mfr, unm, sid, uty, men, den, dnm, bnm, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "StockMovementExecute", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3055, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String mfr, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut)
                    throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');
    String reportsDir    = directoryUtils.getUserDir('R', dnm, unm);

    String uName = directoryUtils.getMySQLUserName();
    String pWord = directoryUtils.getMySQLPassWord();
    Class.forName("com.mysql.jdbc.Driver").newInstance();
      
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/xxx?user=" + uName + "&password=" + pWord + "&autoReconnect=true");
    
    Statement stmt = null, stmt2 = null, stmt3 = null;
    ResultSet rs   = null, rs2   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 3062, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "StockMovementExecute", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3055, bytesOut[0], 0, "ACC:");
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "StockMovementExecute", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3055, bytesOut[0], 0, "SID:");
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    boolean isCSV = false;

    RandomAccessFile fh = null;

    if(isCSV)
    {
      fh = generalUtils.create(reportsDir + "3055.csv");
      
      if(fh == null) // cannot create report output file
      {
        messagePage.msgScreen(false, out, req, 18, unm, sid, uty, men, den, dnm, bnm, "3055", "", "", "", imagesDir, localDefnsDir, defnsDir, bytesOut);
        serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 3055, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
        serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 3055, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
        if(con != null) con.close();
        if(out != null) out.flush(); 
      }
      else
      {
        generate(con, stmt, stmt2, stmt3, rs, rs2, req, out, fh, isCSV, mfr, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);
        serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 3055, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
        if(con != null) con.close();
        if(out != null) out.flush(); 
        out.close();
        fh.close();
      }
    }
    else
    {
      generate(con, stmt, stmt2, stmt3, rs, rs2, req, out, fh, isCSV, mfr, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);
      serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 3055, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
      if(con != null) con.close();
      if(out != null) out.flush(); 
      out.close();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void generate(Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, HttpServletRequest req, PrintWriter out, RandomAccessFile fh, boolean isCSV,
                        String mfrIn, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    if(! isCSV)
    {
      scoutln(out, bytesOut, "<html><head><title>Stock Movement</title>");

      scoutln(out, bytesOut, "<script language=\"JavaScript\">");
  
      scoutln(out, bytesOut, "function item(code){");
      scoutln(out, bytesOut, "window.location.href=\"/central/servlet/ProductStockRecord?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p1=\"+code;}");

      scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\"></head>");
    
      int[] hmenuCount = new int[1];
 
      pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "3055", "", "StockMovementExecute", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
                                
      scoutln(out, bytesOut, "<form>");

      pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Stock Movement", "3055", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

      scoutln(out, bytesOut, "<table id='page' width=100% border=0>");
    
      scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    }
    
    double[] totalstockValue2011 = new double[1];  totalstockValue2011[0] = 0.0;
    double[] totalstockValue2012 = new double[1];  totalstockValue2012[0] = 0.0;
    double[] totalstockValue2013First6 = new double[1];  totalstockValue2013First6[0] = 0.0;
    double[] totalstockValue2013 = new double[1];  totalstockValue2013[0] = 0.0;
            
    header(out, fh, isCSV, bytesOut);
    
    // 2013

    StringBuilder data2013First6 = new StringBuilder(8192);
    
    String dateFrom = "2013-01-01";
    String dateTo   = "2013-06-30";

    forEachStockItem(data2013First6, con, stmt, stmt2, stmt3, rs, rs2, out, "2013First6", isCSV, fh, mfrIn, dateFrom, dateTo, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut, totalstockValue2013First6);

    // 2013

    StringBuilder data2013 = new StringBuilder(8192);
    
    dateFrom = "2013-01-01";
    dateTo   = "2013-12-31";

    forEachStockItem(data2013, con, stmt, stmt2, stmt3, rs, rs2, out, "2013", isCSV, fh, mfrIn, dateFrom, dateTo, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut, totalstockValue2013);

    // 2012
    
    StringBuilder data2012 = new StringBuilder(8192);

    dateFrom = "2012-01-01";
    dateTo   = "2012-12-31";

    forEachStockItem(data2012, con, stmt, stmt2, stmt3, rs, rs2, out, "2012", isCSV, fh, mfrIn, dateFrom, dateTo, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut, totalstockValue2012);

    // 2011
   
    StringBuilder data2011 = new StringBuilder(8192);

    dateFrom = "2011-01-01";
    dateTo   = "2011-12-31";

    forEachStockItem(data2011, con, stmt, stmt2, stmt3, rs, rs2, out, "2011", isCSV, fh, mfrIn, dateFrom, dateTo, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut, totalstockValue2011);

    double totalstockValue2013First6D = 0;
    double totalstockValue2012D = 0;
    double totalstockValue2011D = 0;
    
    int catalogLinkedDefinition = 0, trackTraceMainExternal = 0, productSmartWordPhraseSearch = 0, productSmartWordPhraseSearchFirst6 = 0, len2012 = data2012.length(), len2013 = data2013.length();
    String mfr, mfrCode, itemCode, endStockLevel2012, purchasePrice2012, stockValue2012, endStockLevel2013, purchasePrice2013, stockValue2013, endStockLevel2013First6, purchasePrice2013First6, stockValue2013First6, endStockLevel2011,
           purchasePrice2011, stockValue2011;

    double[] ob2013 = new double[1];
    double[] obQty2013 = new double[1];
    double obQty2011, obVal2011Item, obQty2012, obVal2012Item, stockValue2012D, stockValue2011D;
    
    while(trackTraceMainExternal < len2012)
    {
      mfr = "";
      while(data2011.charAt(catalogLinkedDefinition) != '`')
        mfr += data2011.charAt(catalogLinkedDefinition++);
      ++catalogLinkedDefinition;
        
      mfrCode = "";
      while(data2011.charAt(catalogLinkedDefinition) != '`')
        mfrCode += data2011.charAt(catalogLinkedDefinition++);
      ++catalogLinkedDefinition;
        
      itemCode = "";
      while(data2011.charAt(catalogLinkedDefinition) != '`')
        itemCode += data2011.charAt(catalogLinkedDefinition++);
      ++catalogLinkedDefinition;
                
      endStockLevel2011 = "";
      while(data2011.charAt(catalogLinkedDefinition) != '`')
        endStockLevel2011 += data2011.charAt(catalogLinkedDefinition++);
      ++catalogLinkedDefinition;
        
      purchasePrice2011 = "";
      while(data2011.charAt(catalogLinkedDefinition) != '`')
        purchasePrice2011 += data2011.charAt(catalogLinkedDefinition++);
      ++catalogLinkedDefinition;
        
      stockValue2011 = "";
      while(data2011.charAt(catalogLinkedDefinition) != '`')
        stockValue2011 += data2011.charAt(catalogLinkedDefinition++);
      ++catalogLinkedDefinition;

      endStockLevel2012 = "";
      while(data2012.charAt(trackTraceMainExternal) != '`')
        endStockLevel2012 += data2012.charAt(trackTraceMainExternal++);
      ++trackTraceMainExternal;
        
      purchasePrice2012 = "";
      while(data2012.charAt(trackTraceMainExternal) != '`')
        purchasePrice2012 += data2012.charAt(trackTraceMainExternal++);
      ++trackTraceMainExternal;
        
      stockValue2012 = "";
      while(data2012.charAt(trackTraceMainExternal) != '`')
        stockValue2012 += data2012.charAt(trackTraceMainExternal++);
      ++trackTraceMainExternal;
        
      endStockLevel2013First6 = "";
      while(data2013First6.charAt(productSmartWordPhraseSearchFirst6) != '`')
        endStockLevel2013First6 += data2013First6.charAt(productSmartWordPhraseSearchFirst6++);
      ++productSmartWordPhraseSearchFirst6;
        
      purchasePrice2013First6 = "";
      while(data2013First6.charAt(productSmartWordPhraseSearchFirst6) != '`')
        purchasePrice2013First6 += data2013First6.charAt(productSmartWordPhraseSearchFirst6++);
      ++productSmartWordPhraseSearchFirst6;
        
      stockValue2013First6 = "";
      while(data2013First6.charAt(productSmartWordPhraseSearchFirst6) != '`')
        stockValue2013First6 += data2013First6.charAt(productSmartWordPhraseSearchFirst6++);
      ++productSmartWordPhraseSearchFirst6;

      endStockLevel2013 = "";
      while(data2013.charAt(productSmartWordPhraseSearch) != '`')
        endStockLevel2013 += data2013.charAt(productSmartWordPhraseSearch++);
      ++productSmartWordPhraseSearch;
        
      purchasePrice2013 = "";
      while(data2013.charAt(productSmartWordPhraseSearch) != '`')
        purchasePrice2013 += data2013.charAt(productSmartWordPhraseSearch++);
      ++productSmartWordPhraseSearch;
        
      stockValue2013 = "";
      while(data2013.charAt(productSmartWordPhraseSearch) != '`')
        stockValue2013 += data2013.charAt(productSmartWordPhraseSearch++);
      ++productSmartWordPhraseSearch;

      // 2013First6
      
      getOB2013(con, stmt, rs, itemCode, ob2013, obQty2013);      

      if(obQty2013[0] < 0) obQty2013[0] = 0;
      
      // adjustments
      
      totalstockValue2013First6D += generalUtils.doubleFromStr(stockValue2013First6);

      // 2012 adjustments
      
      obQty2012 = obQty2013[0] - generalUtils.doubleFromStr(endStockLevel2012);

      if(obQty2012 == 0)         
        obVal2012Item = ((ob2013[0] * obQty2013[0]) - (generalUtils.doubleFromStr(endStockLevel2012) * generalUtils.doubleFromStr(purchasePrice2012)));
      else obVal2012Item = ((ob2013[0] * obQty2013[0]) - (generalUtils.doubleFromStr(endStockLevel2012) * generalUtils.doubleFromStr(purchasePrice2012))) / obQty2012;

      if(obQty2012 < 0) obVal2012Item = 0;
      
      endStockLevel2012 = generalUtils.doubleToStr('8', (generalUtils.doubleFromStr(endStockLevel2012) + obQty2012)   );

      stockValue2012D = ((obQty2012 * obVal2012Item) + (generalUtils.doubleFromStr(endStockLevel2012) * generalUtils.doubleFromStr(purchasePrice2012))   );
      
      totalstockValue2012D += stockValue2012D;
      
      // 2011 adjustments
      if(obQty2012 <= 0)
        obQty2011 = obQty2012 + generalUtils.doubleFromStr(endStockLevel2011);
      else obQty2011 = obQty2012 - generalUtils.doubleFromStr(endStockLevel2011);

      if(obQty2011 == 0)         
        obVal2011Item = ((obVal2012Item * obQty2012) - (generalUtils.doubleFromStr(endStockLevel2011) * generalUtils.doubleFromStr(purchasePrice2011)));
      else obVal2011Item = ((obVal2012Item * obQty2012) - (generalUtils.doubleFromStr(endStockLevel2011) * generalUtils.doubleFromStr(purchasePrice2011))) / obQty2011;

      if(obQty2011 < 0) obVal2011Item = 0;
      
      endStockLevel2011 = generalUtils.doubleToStr('8', (generalUtils.doubleFromStr(endStockLevel2011) + obQty2011)   );

      stockValue2011D = ((obQty2011 * obVal2011Item) + (generalUtils.doubleFromStr(endStockLevel2011) * generalUtils.doubleFromStr(purchasePrice2011)) );
      if(stockValue2011D < 0) stockValue2011D = 0;
      
      totalstockValue2011D += stockValue2011D;
      
      if(! isCSV)
      {
        scoutln(out, bytesOut, "<tr><td><p>" + mfr + "</td><td><p>" + mfrCode + "</td><td><p><a href=\"javascript:item('" + itemCode + "')\">" + itemCode
                             + "</td><td align=right><p>" + generalUtils.doubleDPs(endStockLevel2011, '0') + "</td><td align=right><p>" + purchasePrice2011 + "</td><td align=right><p>" + generalUtils.doubleDPs(stockValue2011D, '2')
                             + "</td><td align=right><p>" + generalUtils.doubleDPs(endStockLevel2012, '0') + "</td><td align=right><p>" + purchasePrice2012 + "</td><td align=right><p>" + generalUtils.doubleDPs(stockValue2012D, '2')
                             + "</td><td align=right><p>" + generalUtils.doubleDPs(endStockLevel2013First6, '0') + "</td><td align=right><p>" + purchasePrice2013First6 + "</td><td align=right><p>" + generalUtils.doubleDPs(stockValue2013First6, '2') + "</td>"
                             + "</td><td align=right><p>" + generalUtils.doubleDPs(endStockLevel2013, '0') + "</td><td align=right><p>" + purchasePrice2013 + "</td><td align=right><p>" + stockValue2013 + "</td></tr>");
      }
      else
      {
        writeEntry(fh, mfr,               true, false);
        writeEntry(fh, mfrCode,           true, false);
        writeEntry(fh, itemCode,          true, false);
        writeEntry(fh, endStockLevel2011, true, false);
        writeEntry(fh, purchasePrice2011, true, false);
        writeEntry(fh, stockValue2011,    true, false);
        writeEntry(fh, endStockLevel2012, true, false);
        writeEntry(fh, purchasePrice2012, true, false);
        writeEntry(fh, stockValue2012,    true, false);
        writeEntry(fh, endStockLevel2013First6, true, false);
        writeEntry(fh, purchasePrice2013First6, true, false);
        writeEntry(fh, stockValue2013First6,    true, false);
        writeEntry(fh, endStockLevel2013, true, false);
        writeEntry(fh, purchasePrice2013, true, false);
        writeEntry(fh, stockValue2013,    false, true);
      }
    }
    
    footer(out, fh, isCSV, totalstockValue2011D, totalstockValue2012D, totalstockValue2013First6D, totalstockValue2013[0], bytesOut);
    
    if(! isCSV)
    {
      scoutln(out, bytesOut, "</table></form>");
      scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void forEachStockItem(StringBuilder data, Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, PrintWriter out, String year, boolean isCSV, RandomAccessFile fh,
                                String mfr, String dateFrom, String dateTo, String unm, String sid, String uty, String men, String den, String dnm,
                                String bnm, String localDefnsDir, String defnsDir, int[] bytesOut, double[] totalstockValue) throws Exception
  {
    stmt = con.createStatement();

    if(mfr.equals("-"))
      rs = stmt.executeQuery("SELECT ItemCode FROM stock WHERE WrittenOff != 'Y' ORDER BY Manufacturer, ManufacturerCode");
    else rs = stmt.executeQuery("SELECT ItemCode FROM stock WHERE Manufacturer = '" + generalUtils.sanitise(mfr) + "' AND WrittenOff != 'Y' ORDER BY ManufacturerCode");

    String itemCode;
    String[] cssFormat = new String[1];  cssFormat[0] = "";
    double[] mfrStockValue = new double[1]; mfrStockValue[0] = 0.0;

    while(rs.next())
    {
      itemCode   = rs.getString(1);
      
      forAnItem(data, con, stmt2, stmt3, rs2, year, isCSV, itemCode, dateFrom, dateTo, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, cssFormat, bytesOut, mfrStockValue);
    }

    totalstockValue[0] += mfrStockValue[0];
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private double stockLevelForAStore(Connection con, Statement stmt, ResultSet rs, String fld, String itemCode) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT " + fld + " FROM newstock WHERE ItemCode = '" + itemCode + "'");

    double level = 0.0;

    if(rs.next())
      level = generalUtils.doubleFromStr(rs.getString(1));

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    return level;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void forAnItem(StringBuilder data, Connection con, Statement stmt, Statement stmt2, ResultSet rs, String year, boolean isCSV, String itemCode, String dateFrom, String dateTo, String unm, String sid, String uty, String men,
                         String den, String dnm, String bnm, String localDefnsDir, String defnsDir, String[] cssFormat, int[] bytesOut, double[] mfrStockValue) throws Exception
  {
    String[] mfr     = new String[1];
    String[] mfrCode = new String[1];
    inventory.getMfrAndMfrCodeGivenItemCode(con, stmt, rs, itemCode, mfr, mfrCode);

    String fld = "";
    
    if(year.equals("2011"))
      fld = "EndLevel2011";
    else
    if(year.equals("2012"))
      fld = "EndLevel2012";
    else
    if(year.equals("2013"))
      fld = "EndLevel2013";
    else
    if(year.equals("2013First6"))
      fld = "EndLevel2013After6";
    
    double endStockLevel = this.stockLevelForAStore(con, stmt, rs, fld, itemCode);
    
    if(endStockLevel < 0) endStockLevel = 0;    
    
    double purchasePrice = determinePurchasePrice(con, stmt, rs, year, false, itemCode, dateFrom, dateTo, dnm, localDefnsDir, defnsDir);
    
    double stockValue = purchasePrice * endStockLevel;
    
    mfrStockValue[0] += stockValue;

    if(year.equals("2011"))
    {
      data.append(mfr[0] + "`");
      data.append(mfrCode[0] + "`");
      data.append(itemCode + "`");
    }
    
    data.append(generalUtils.doubleToStr('0', endStockLevel) + "`");
    data.append(generalUtils.doubleToStr('8', purchasePrice) + "`");
    data.append(generalUtils.doubleToStr('2', stockValue) + "`");
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void header(PrintWriter out, RandomAccessFile fh, boolean isCSV, int[] bytesOut) throws Exception
  {
    if(isCSV)
    {  
      writeEntry(fh, "Manufacturer",       true, false);
      writeEntry(fh, "Mfr Code",           true, false);
      writeEntry(fh, "Item Code",          true, false);
      writeEntry(fh, "2011 Stock On-Hand", true, false);
      writeEntry(fh, "2011 Purchase Price", true, false);
      writeEntry(fh, "2011 Stock Value",  true, false);
      writeEntry(fh, "2012 Stock On-Hand", true, false);
      writeEntry(fh, "2012 Purchase Price", true, false);
      writeEntry(fh, "2012 Stock Value", true, false);
      writeEntry(fh, "2013 (1-6) Stock On-Hand", true, false);
      writeEntry(fh, "2013 (1-6) Purchase Price", true, false);
      writeEntry(fh, "2013 (1-6) Stock Value",  true, false);
      writeEntry(fh, "2013 (7-12) Stock On-Hand", true, false);
      writeEntry(fh, "2013 (7-12) Purchase Price", true, false);
      writeEntry(fh, "2013 (7-12) Stock Value", false, true);
    }
    else
    {
      scoutln(out, bytesOut, "<tr id='pageColumn'><td><p>Manufacturer</td>");
      scoutln(out, bytesOut, "<td><p>Mfr Code</td>");
      scoutln(out, bytesOut, "<td><p>Item Code</td>");
      scoutln(out, bytesOut, "<td align=right><p>2011 Stock On-Hand</td>");
      scoutln(out, bytesOut, "<td align=right><p>2011 Purchase Price</td>");
      scoutln(out, bytesOut, "<td align=right><p>2011 Stock Value</td>");
      scoutln(out, bytesOut, "<td align=right><p>2012 Stock On-Hand</td>");
      scoutln(out, bytesOut, "<td align=right><p>2012 Purchase Price</td>");
      scoutln(out, bytesOut, "<td align=right><p>2012 Stock Value</td>");
      scoutln(out, bytesOut, "<td align=right><p>2013 (1-6) Stock On-Hand</td>");
      scoutln(out, bytesOut, "<td align=right><p>2013 (1-6) Purchase Price</td>");
      scoutln(out, bytesOut, "<td align=right><p>2013 (1-6) Stock Value</td>");
      scoutln(out, bytesOut, "<td align=right><p>2013 (7-12) Stock On-Hand</td>");
      scoutln(out, bytesOut, "<td align=right><p>2013 (7-12)Purchase Price</td>");
      scoutln(out, bytesOut, "<td align=right><p>2013 (7-12)Stock Value</td>    </tr>");
    }
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void footer(PrintWriter out, RandomAccessFile fh, boolean isCSV, double totalstockValue2011, double totalstockValue2012, double totalstockValue2013First6, double totalstockValue2013, int[] bytesOut) throws Exception
  {
    if(isCSV)
    {  
      writeEntry(fh, "", true, false);
      writeEntry(fh, "", true, false);
      writeEntry(fh, "", true, false);
      writeEntry(fh, "", true, false);
      writeEntry(fh, "", true, false);      
      writeEntry(fh, generalUtils.doubleToStr('0', totalstockValue2011), true, false);
      writeEntry(fh, "", true, false);
      writeEntry(fh, "", true, false);      
      writeEntry(fh, generalUtils.doubleToStr('0', totalstockValue2012), true, false);
      writeEntry(fh, "", true, false);
      writeEntry(fh, "", true, false);      
      writeEntry(fh, generalUtils.doubleToStr('0', totalstockValue2013First6), true, false);
      writeEntry(fh, "", true, false);
      writeEntry(fh, "", true, false);      
      writeEntry(fh, generalUtils.doubleToStr('0', totalstockValue2013), false, true);
    }
    else
    {
      scoutln(out, bytesOut, "<tr id='pageColumn'><td colspan=5></td><td align=right><p>" + generalUtils.formatNumeric(totalstockValue2011, '2') + "</td><td colspan=2></td><td align=right><p>" + generalUtils.formatNumeric(totalstockValue2012, '2')
                       + "</td><td colspan=2></td><td align=right><p>" + generalUtils.formatNumeric(totalstockValue2013First6, '2') + "</td><td colspan=2></td><td align=right><p>" + generalUtils.formatNumeric(totalstockValue2013, '2') + "</td></tr>");
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void writeEntry(RandomAccessFile fh, String entry, boolean comma, boolean newLine) throws Exception
  {
    if(entry == null) entry = "";

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

  // -------------------------------------------------------------------------------------------------------------------------------
  private double determinePurchasePrice(Connection con, Statement stmt, ResultSet rs, String year, boolean useStockRecordPurchasePrice, String itemCode, String dateFrom, String dateTo, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    double purchasePrice = 0.0;

    if(! useStockRecordPurchasePrice)
    {
      int count = 0;
      double[] up  = new double[1200];
      double[] qty = new double[1200];

      String currency = "";
      byte[] actualDate = new byte[20];
    
      stmt = con.createStatement();

      stmt.setMaxRows(1000);

//System.out.println("SELECT t2.UnitPrice, t1.Currency, t2.Quantity, t2.POCode, t1.Rate FROM pol AS t2 INNER JOIN po AS t1 ON t1.POCode = t2.POCode WHERE t2.ItemCode = '" + generalUtils.sanitiseForSQL(itemCode)
//                             + "' AND t1.Date >= {d '" + yearEndFrom + "'} AND t1.Date <= {d '" + yearEndTo + "'}"
//                             + " AND t1.Status != 'C' ORDER BY t2.POCode");
      rs = stmt.executeQuery("SELECT t2.UnitPrice, t1.Currency, t2.Quantity, t2.POCode, t1.Rate FROM pol AS t2 INNER JOIN po AS t1 ON t1.POCode = t2.POCode WHERE t2.ItemCode = '" + generalUtils.sanitiseForSQL(itemCode)
//                             + "' AND t1.Date >= {d '" + yearEndFrom + "'} AND t1.Date <= {d '" + yearEndTo + "'}"
                           + "' AND t1.Date <= {d '" + dateTo + "'} AND t1.Date >= {d '" + dateFrom  + "'}"
                           + " AND t1.Status != 'C' ORDER BY t2.POCode");
 
      double totalUPs = 0.0;
      double totalQtys = 0.0, quantity, unitPrice;
      String poCode = "", lastPOCode = "", date;
      boolean first = true;
      double upModified, endYearExchRate = 1.0;
      double rate = 1.0;
  
      while(rs.next())
      {
        unitPrice = generalUtils.doubleFromStr(rs.getString(1));
        quantity  = generalUtils.doubleFromStr(rs.getString(3));
        poCode   = rs.getString(4);
        currency = rs.getString(2);

        rate = accountsUtils.getApplicableRate(con, stmt, rs, currency, dateTo, actualDate, dnm, localDefnsDir, defnsDir);
      if(rate == 0)
        rate = 1;

        if(quantity > 0)
        {
          up[count] = unitPrice * rate;
          qty[count++] = quantity;
        }
      }

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();

      // LP
      
      stmt = con.createStatement();

      stmt.setMaxRows(1000);
      
      rs = stmt.executeQuery("SELECT t2.UnitPrice, t1.Currency, t2.Quantity, t2.LPCode, t1.Rate FROM lpl AS t2 INNER JOIN lp AS t1 ON t1.LPCode = t2.LPCode WHERE t2.ItemCode = '" + generalUtils.sanitiseForSQL(itemCode)
                           + "' AND t1.Date <= {d '" + dateTo + "'} AND t1.Date >= {d '" + dateFrom + "'}"
                           + " AND t1.Status != 'C' ORDER BY t2.LPCode");
      
      totalUPs = 0.0;
      totalQtys = 0.0;
      lastPOCode = "";
      first = true;
      endYearExchRate = 1.0;
      rate = 1.0;
  
      while(rs.next())
      {
        unitPrice = generalUtils.doubleFromStr(rs.getString(1));
        quantity  = generalUtils.doubleFromStr(rs.getString(3));
        poCode   = rs.getString(4);
        currency = rs.getString(2);

        rate = accountsUtils.getApplicableRate(con, stmt, rs, currency, dateTo, actualDate, dnm, localDefnsDir, defnsDir);
        if(rate == 0)
          rate = 1;

        if(quantity > 0)
        {
          up[count] = unitPrice * rate;
          qty[count++] = quantity;
        }
      }
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();

      // OB
      
      if(year.equals("2013") || year.equals("2013First6"))
      {
        stmt = con.createStatement();

        rs = stmt.executeQuery("SELECT ClosingWac, ClosingLevel FROM stock WHERE ItemCode = '" + generalUtils.sanitiseForSQL(itemCode) + "'");
      
        if(rs.next())
        {
          double d = generalUtils.doubleFromStr(rs.getString(2));
          if(d > 0)
          {
            up[count] = generalUtils.doubleFromStr(rs.getString(1));////////// / d;
            qty[count++] = d;
          }
        }
      }  
      
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
      
      double totalUP = 0.0, totalQty = 0.0;
      
      if(count > 0)
      {
        for(int x=0;x<count;++x)
        {
          totalUP += (up[x] * qty[x]);
          totalQty += qty[x];
        }
        
        purchasePrice = totalUP / totalQty;
      }
    }

    if(useStockRecordPurchasePrice || purchasePrice == 0.0)
    {
      String[] purchasePrice2 = new String[1];
      String[] currency       = new String[1];
      String[] exchangeRate   = new String[1];
      String[] dateChanged    = new String[1];
    
      inventory.getPurchaseDetailsGivenCode(con, stmt, rs, itemCode, purchasePrice2, currency, exchangeRate, dateChanged);
    
      byte[] actualDate = new byte[20];
      double endYearExchRate = accountsUtils.getApplicableRate(con, stmt, rs, currency[0], dateTo, actualDate, dnm, localDefnsDir, defnsDir);
      if(endYearExchRate == 0)
        endYearExchRate = 1;
    
      purchasePrice = generalUtils.doubleFromStr(purchasePrice2[0]) * endYearExchRate;
    }
            
    return purchasePrice;
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void getOB2013(Connection con, Statement stmt, ResultSet rs, String itemCode, double[] ob2013, double[] obQty2013) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT ClosingWac, ClosingLevel FROM stock WHERE ItemCode = '" + generalUtils.sanitiseForSQL(itemCode) + "'");
      
    if(rs.next())
    {
      ob2013[0]    = generalUtils.doubleFromStr(rs.getString(1));
      obQty2013[0] = generalUtils.doubleFromStr(rs.getString(2));
    }
      
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

}
