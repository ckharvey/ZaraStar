// =======================================================================================================================================================================================================
// System: ZaraStar Product: Generate stock trace output
// Module: ProductStockTraceGenerate.java
// Author: C.K.Harvey
// Copyright (c) 2002-07 Christopher Harvey. All Rights Reserved.
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

public class ProductStockTraceGenerate extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  PickingList pickingList = new PickingList();
  GoodsReceivedNote goodsReceivedNote = new GoodsReceivedNote();
  Inventory inventory = new Inventory();
  MiscDefinitions miscDefinitions = new MiscDefinitions();
  DocumentUtils documentUtils = new DocumentUtils();

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
      directoryUtils.setContentHeaders(res);
      out = res.getWriter();

      unm = req.getParameter("unm");
      sid = req.getParameter("sid");
      uty = req.getParameter("uty");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");
      p1  = req.getParameter("p1"); // itemCode
      p2  = req.getParameter("p2"); // stores
      p3  = req.getParameter("p3"); // storesChecked
      p4  = req.getParameter("p4"); // numStores
      p5  = req.getParameter("p5"); // all data wanted 

      if(p1 == null) p1 = "";
      
      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, p3, p4, p5, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "ProductStockTraceInput", bytesOut);

      serverUtils.etotalBytes(req, unm, dnm, 3052, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm,
                    String bnm, String p1, String p2, String p3, String p4, String p5, int[] bytesOut) throws Exception
   {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 3052, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "3052", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3052, bytesOut[0], 0, "ACC:" + p1);
      if(con  != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "3052", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3052, bytesOut[0], 0, "SID:" + p1);
      if(con  != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    int x, numStores;
    String stores = ""; 
    
    p1 = p1.toUpperCase();

    if(p2 == null || p2.length() == 0) // not a call from 3052
    {
      String[][] storesList = new String[1][1];
      numStores = documentUtils.getStoresList(con, stmt, rs, storesList);

      for(x=0;x<numStores;++x)
        stores += (storesList[0][x] + "\001");

      p3 = ""; // storesChecked
      for(x=0;x<numStores;++x)
        p3 += "Y"; // assume want all stores

      p5 = "N"; // all data wanted
    }
    else
    {
      numStores = generalUtils.strToInt(p4);      
    }

    set(con, stmt, rs, out, req, p1, stores, p3, numStores, p5, unm, sid, uty, men, den, dnm, bnm, imagesDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 3052, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String itemCode, String storesList,
                   String storesChecked, int numStores, String allData, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                   String imagesDir, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Stock Trace</title>");
    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3038, unm, uty, dnm, localDefnsDir, defnsDir))
    {
       scoutln(out, bytesOut, "function pl(code){");
       scoutln(out, bytesOut, "var p1=sanitise(code);");
       scoutln(out, bytesOut, "this.location.href=\"/central/servlet/GoodsReceivedPickingList?unm=" + unm + "&sid=" + sid + "&uty="
                            + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3025, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function grn(code){");
      scoutln(out, bytesOut, "var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/GoodsReceivedPage?unm=" + unm + "&sid=" + sid + "&uty="
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
    
    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    int[] hmenuCount = new int[1];
 
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "3052", "", "ProductStockTraceInput", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Stock Trace Results", "3052", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<form><p>");

    String[] stores = new String[numStores]; // the maximum number of stores that can be selected

    String[] mfr     = new String[1];
    String[] mfrCode = new String[1];
    inventory.getMfrAndMfrCodeGivenItemCode(con, stmt, rs, itemCode, mfr, mfrCode);
    scoutln(out, bytesOut, "For <b>" + itemCode + " (" + mfr[0] + " " + mfrCode[0] + ")</b><br>In stores: ");

    boolean first = true;
    int x, y=0, z=0;
    for(x=0;x<numStores;++x)
    {
      if(storesChecked.charAt(x) == 'Y')
      {
        stores[z] = ""; 
        while(storesList.charAt(y) != '\001')
          stores[z] += storesList.charAt(y++);

        if(first)
          first = false;
        else scout(out, bytesOut, ", ");
        
        scout(out, bytesOut, stores[z++]);
      }
      else
      {
        while(storesList.charAt(y) != '\001')
          ++y;
      }
    
      ++y;      
    }

    if(allData.equals("Y"))
      scoutln(out, bytesOut, "<br>Showing ALL historical data.");
    else scoutln(out, bytesOut, "<br>Showing data from last Stock Check.");
  
    numStores = z; // actual number of stores required

    char dpOnQuantities = miscDefinitions.dpOnQuantities(con, stmt, rs, "5");

    boolean useWAC = false;
    
    scoutln(out, bytesOut, "<br><table border=0 cellpadding=3 cellspacing=0><tr id=\"pageColumn\">");

    generate(out, useWAC, itemCode, stores, numStores, allData, dpOnQuantities, unm, dnm, imagesDir, bytesOut);

    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void generate(PrintWriter out, boolean useWAC, String itemCode, String[] stores, int numStores, String allData, char dpOnQuantities, String unm,
                        String dnm, String imagesDir, int[] bytesOut) throws Exception
  {
    String userName = directoryUtils.getMySQLUserName();
    String passWord = directoryUtils.getMySQLPassWord();
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + userName + "&password=" + passWord);

    Statement stmt = null, stmt2 = null;
    ResultSet rs = null;
    
    int x;
     
    String[][] checkCodes = new String[1][numStores];
    String[][] dates      = new String[1][numStores];
    String[][] levels     = new String[1][numStores];
    String[][] remarks    = new String[1][numStores];

    String[] tmpTables = new String[numStores];
     
    int[] startDatesEncoded = new int[numStores];
     
    double[][] balances = new double[1][numStores];
     
    for(x=0;x<numStores;++x)
    {
      tmpTables[x] = unm + "_tmp" + x;
      directoryUtils.createTmpTable(true, con, stmt2, "DocumentCode char(20), DocumentType char(1), Date date, Status char(1), StockProcessed char(1), "
                                           + "Level char(20), Remark char(150)", "", tmpTables[x]);

      if(allData.equals("N"))
        getFirstStockCheckRecords(con, stmt, rs, itemCode, numStores, stores, checkCodes, dates, levels, remarks);
      else
      {
        checkCodes[0][x] = "";
        dates[0][x]      = "1970-01-01";
        levels[0][x]     = "0";
        remarks[0][x]    = "";
      }
    
      balances[0][x] = generalUtils.doubleFromStr(levels[0][x]);
      startDatesEncoded[x] = generalUtils.encodeSQLFormat(dates[0][x]);
    }

    // output column headers
     
    scoutln(out, bytesOut, "<tr id=\"pageColumn\">");

    for(x=0;x<numStores;++x)
      scoutln(out, bytesOut, "<td colspan=8><p><b>" + stores[x] + "</b></td>");

    scoutln(out, bytesOut, "</tr><tr id=\"pageColumn\">");

    for(x=0;x<numStores;++x)
    {
      scoutln(out, bytesOut, "<td nowrap><p>Date</td>");
      scoutln(out, bytesOut, "<td nowrap><p>In</td>");
      scoutln(out, bytesOut, "<td nowrap><p>Out</td>");
      scoutln(out, bytesOut, "<td nowrap><p>Balance</td>");
      
      if(useWAC)
        scoutln(out, bytesOut, "<td nowrap><p>WAC</td>");

      scoutln(out, bytesOut, "<td nowrap><p>Code</td>");
      scoutln(out, bytesOut, "<td nowrap><p>Status</td>");
      scoutln(out, bytesOut, "<td nowrap><p>Remark</td><td width=30></td>");
    }  

    String cssFormat = "line1";

    scoutln(out, bytesOut, "</tr><tr id=\"" + cssFormat + "\">");

    for(x=0;x<numStores;++x)
    {
      writeLine(out, useWAC, dates[0][x], levels[0][x], balances[0], x, 'C', checkCodes[0][x], "", "Y", remarks[0][x], dpOnQuantities, imagesDir, bytesOut);
    }
     
    scoutln(out, bytesOut, "</tr>");

    cssFormat = "line2";
     
    getPickingLists(con, stmt, stmt2, rs, itemCode, stores, numStores, tmpTables);

    getGoodsReceivedNotes(con, stmt, stmt2, rs, itemCode, stores, numStores, tmpTables);
     
    getStockAdjustmentRecords(con, stmt, rs, itemCode, numStores, stores, tmpTables);

    if(allData.equals("Y"))
      getStockCheckRecords(con, stmt, stmt2, rs, itemCode, numStores, stores, tmpTables);

    // now read all store tmp files and output in chronological order
     
    Statement[] stmts = new Statement[numStores];
    ResultSet[] rss   = new ResultSet[numStores];
    int[] resultDatesEncoded = new int[numStores];

    for(x=0;x<numStores;++x)
    {
      stmts[x] = con.createStatement();
       
      rss[x] = stmts[x].executeQuery("SELECT * FROM " + tmpTables[x] + " ORDER BY Date, DocumentType"); // ensures SC appears first on a day
     
      if(rss[x].next())
        resultDatesEncoded[x] = generalUtils.encodeSQLFormat(rss[x].getString(3)); // encode first rec in the tmp file
      else resultDatesEncoded[x] = -1; // no more recs for this store 
    }  

    String documentCode, documentType, date, status, stockProcessed, level, remark;

    scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\">");
    cssFormat = "line1";

    int[] earliest = new int[1];
    boolean allDone = findEarliest(resultDatesEncoded, numStores, earliest); // earliest indicates which of the stores is first
    x = 0;
    while(x < earliest[0])
    {
      scoutln(out, bytesOut, "<td colspan=8></td>");
      ++x;
    }
     
    boolean oneOutput;
    while(! allDone)
    {
      oneOutput = false;
      if(resultDatesEncoded[earliest[0]] >= startDatesEncoded[earliest[0]])
      {        
        documentCode   = rss[earliest[0]].getString(1);
        documentType   = rss[earliest[0]].getString(2);
        date           = rss[earliest[0]].getString(3);
        status         = rss[earliest[0]].getString(4);
        stockProcessed = rss[earliest[0]].getString(5);
        level          = rss[earliest[0]].getString(6);
        remark         = rss[earliest[0]].getString(7);

        while(x < earliest[0])
        {
          scoutln(out, bytesOut, "<td colspan=8></td>");
          ++x;
        }
  
        writeLine(out, useWAC, date, level, balances[0], earliest[0], documentType.charAt(0), documentCode, status, stockProcessed, remark, dpOnQuantities,
                  imagesDir, bytesOut);

        oneOutput = true;
      }
       
      if(rss[earliest[0]].next())
        resultDatesEncoded[earliest[0]] = generalUtils.encodeSQLFormat(rss[earliest[0]].getString(3));
      else resultDatesEncoded[earliest[0]] = -1;
          
      allDone = findEarliest(resultDatesEncoded, numStores, earliest);

      if(! allDone && oneOutput)
      {  
        scoutln(out, bytesOut, "</tr><tr id=\"" + cssFormat + "\">");

        if(cssFormat.equals("line1")) cssFormat = "line2"; else cssFormat = "line1";

        x = 0;
      }
    }     
   
    scoutln(out, bytesOut, "</tr>"); 
     
    // output column headers again
     
    scoutln(out, bytesOut, "<tr id=\"pageColumn\">");

    for(x=0;x<numStores;++x)
      scoutln(out, bytesOut, "<td colspan=8><p><b>" + stores[x] + "</b></td>");

    scoutln(out, bytesOut, "</tr><tr id=\"pageColumn\">");

    for(x=0;x<numStores;++x)
    {
      scoutln(out, bytesOut, "<td nowrap><p>Date</td>");
      scoutln(out, bytesOut, "<td nowrap><p>In</td>");
      scoutln(out, bytesOut, "<td nowrap><p>Out</td>");
      scoutln(out, bytesOut, "<td nowrap><p>Balance</td>");
      scoutln(out, bytesOut, "<td nowrap><p>Code</td>");
      scoutln(out, bytesOut, "<td nowrap><p>Status</td>");
      scoutln(out, bytesOut, "<td nowrap><p>Remark</td><td></td>");
    }  

    scoutln(out, bytesOut, "</tr>");
     
    double total = 0.0;
    for(x=0;x<numStores;++x)
    {
      scoutln(out, bytesOut, "<tr><td colspan=3><p>" + stores[x] + "</td><td align=right><p>"
              + generalUtils.formatNumeric(balances[0][x], dpOnQuantities) + "</td></tr>");
      total += balances[0][x];
    }
   
    scoutln(out, bytesOut, "<tr><td colspan=3><p>TOTAL IN-STOCK</td><td align=right><p>"
            + generalUtils.formatNumeric(total, dpOnQuantities) + "</td></tr>");
     
    for(x=0;x<numStores;++x)
    {
      if(rss[x]   != null) rss[x].close();
      if(stmts[x] != null) stmts[x].close();
    }
     
    // remove tmp tables
    for(x=0;x<numStores;++x)
      directoryUtils.removeTmpTable(con, stmt2, tmpTables[x]);
    
    if(con != null) con.close();  
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void getFirstStockCheckRecords(Connection con, Statement stmt, ResultSet rs, String itemCode, int numStores, String[] stores, String[][] checkCodes,
                                         String[][] dates, String[][] levels, String[][] remarks) throws Exception
  {
    for(int x=0;x<numStores;++x)
    {
      stmt = con.createStatement();
      stmt.setMaxRows(1);

      rs = stmt.executeQuery("SELECT CheckCode, Date, Level, Remark FROM stockc WHERE ItemCode = '" + itemCode + "' AND StoreCode = '" + stores[x] + "' AND Status != 'C' AND Type = 'S' AND Level != '999999' ORDER BY Date DESC");
    
      if(rs.next())
      {
        checkCodes[0][x] = rs.getString(1);
        dates[0][x]      = rs.getString(2);
        levels[0][x]     = rs.getString(3);
        remarks[0][x]    = rs.getString(4);
      }
      else
      {
        checkCodes[0][x] = "";
        dates[0][x]      = "1970-01-01";
        levels[0][x]     = "0";
        remarks[0][x]    = "";
      }

      if(rs != null) rs.close();
      if(stmt != null) stmt.close();
    }
  }
    
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void getPickingLists(Connection con, Statement stmt, Statement stmt2, ResultSet rs, String itemCode, String[] stores, int numStores,
                               String[] tmpTables) throws Exception
  {
    int x;
    boolean found;
    
    stmt = con.createStatement();
    rs = stmt.executeQuery("SELECT t1.PLCode, t1.Date, t1.Status, t1.Completed, t2.QuantityPacked, t2.Store, t2.Instruction FROM "
                         + "pl AS t1 INNER JOIN pll AS t2 ON t1.PLCode = t2.PLCode WHERE t2.ItemCode = '" + itemCode + "'");
    
    while(rs.next())
    {
      x = 0;
      found = false;
      while(x < numStores && ! found) // just-in-case
      {
        if(rs.getString(6).equals(stores[x]))
        {
          addToTmpTable(con, stmt2, rs.getString(1), "P", rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(7), 
                        tmpTables[x]);
          found = true;
        }
        
        ++x;
      }  
    }
      
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void getGoodsReceivedNotes(Connection con, Statement stmt, Statement stmt2, ResultSet rs, String itemCode, String[] stores, int numStores,
                                     String[] tmpTables) throws Exception
  {
    int x;
    boolean found;
    
    stmt = con.createStatement();
    rs = stmt.executeQuery("SELECT t1.GRCode, t1.Date, t1.Status, t1.StockProcessed, t2.Quantity, t2.StoreCode, t2.QCComplete, t2.QCCompleteDate, "
                         + "t2.Remark FROM gr AS t1 INNER JOIN grl AS t2 ON t1.GRCode = t2.GRCode WHERE t2.ItemCode = '" + itemCode + "'");
    
    String qcComplete, qcCompleteDate, remark, s;

    while(rs.next())
    {
      x = 0;
      found = false;
      while(x < numStores && ! found) // just-in-case
      {
        if(rs.getString(6).equals(stores[x]))
        {
          qcComplete     = rs.getString(7);
          qcCompleteDate = rs.getString(8);
          remark         = rs.getString(9);

          if(remark == null) remark = "";
          
          if(qcComplete != null && qcComplete.equals("Y"))
          {
            s = "QC Completed on ";
            if(qcCompleteDate.equals("1970-01-01"))              
              s += "(no date)";
            else s += generalUtils.convertFromYYYYMMDD(qcCompleteDate);
                    
            if(remark.length() > 0)
              s += ": " + remark;
          }
          else s = remark;              
          
          addToTmpTable(con, stmt2, rs.getString(1), "G", rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), s, tmpTables[x]);
          found = true;
        }
      
        ++x;
      }  
    }
      
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void getStockAdjustmentRecords(Connection con, Statement stmt, ResultSet rs, String itemCode, int numStores, String[] stores,
                                         String[] tmpTables) throws Exception
  {
    int x;
                                      
    stmt = con.createStatement();
    rs = stmt.executeQuery("SELECT AdjustmentCode, Date, Status, StoreFrom, StoreTo, Quantity, Remark FROM stocka WHERE ItemCode = '" + itemCode
                         + "'");
                                      
    while(rs.next())
    {
      for(x=0;x<numStores;++x)
      {
        // if StoreFrom has value and it matches this store mark as 'O' (transfer out)
        if(rs.getString(4).equals(stores[x]))
        {
          addToTmpTable(con, stmt, rs.getString(1), "o", rs.getString(2), rs.getString(3), "Y", rs.getString(6), rs.getString(7), tmpTables[x]);
        }

        
        // if StoreTo has value and it matches this store mark as 'I' (transfer in)
        if(rs.getString(5).equals(stores[x]))
        {
          addToTmpTable(con, stmt, rs.getString(1), "I", rs.getString(2), rs.getString(3), "Y", rs.getString(6), rs.getString(7), tmpTables[x]);
        }
      }
    }
                                        
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }
    
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void getStockCheckRecords(Connection con, Statement stmt, Statement stmt2, ResultSet rs, String itemCode, int numStores, String[] stores,
                                    String[] tmpTables) throws Exception
  {
    int x;
    boolean found;
    
    stmt = con.createStatement();
    rs = stmt.executeQuery("SELECT CheckCode, Date, Status, StoreCode, Level, Remark FROM stockc WHERE ItemCode = '" + itemCode + "' AND Type = 'S' AND Level != '999999'");
                                      
    while(rs.next())
    {
      x = 0;
      found = false;
      while(x < numStores && ! found) // just-in-case
      {
        if(rs.getString(4).equals(stores[x]))
        {
          addToTmpTable(con, stmt2, rs.getString(1), "C", rs.getString(2), rs.getString(3), "Y", rs.getString(5), rs.getString(6), tmpTables[x]);
          found = true;
        }
      
        ++x;
      }  
    }
                                        
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }
    
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void writeLine(PrintWriter out, boolean useWAC, String date, String qty, double[] balances, int whichStore, char type, String code, String status,
                         String stockProcessed, String remark, char dpOnQuantities, String imagesDir, int[] bytesOut) throws Exception
  {
    byte[] b = new byte[20];

    scout(out, bytesOut, "<td>");

    // date
    
    if(date.length() == '\000' || date.equals("1970-01-01"))
      scoutln(out, bytesOut, "&nbsp;</td>");
    else scoutln(out, bytesOut, generalUtils.convertFromYYYYMMDD(date) + "</td>");

    // qty, either in In column or Out column
    
    if(type == 'O') // opening balance
    {
      scoutln(out, bytesOut, "<td>&nbsp;</td><td>&nbsp;</td>"); // blank In and Out
      balances[whichStore] = generalUtils.doubleFromStr(qty);
    }
    else
    if(type == 'C') // stock check
    {
      if(date.equals("1970-01-01"))
        scoutln(out, bytesOut, "<td>&nbsp;</td><td>&nbsp;</td>"); // blank In and Out
      else
      {
        if(! status.equals("C") && stockProcessed.equals("Y"))
          balances[whichStore] = generalUtils.doubleFromStr(qty);
        scoutln(out, bytesOut, "<td>&nbsp;</td><td>&nbsp;</td>"); // blank In and Out
      }  
    }
    else
    if(type == 'P') // PL
    {
      if(! status.equals("C") && stockProcessed.equals("Y"))
        balances[whichStore] -= generalUtils.doubleFromStr(qty);
      scout(out, bytesOut, "<td>&nbsp;</td><td align=right>"); // blank Out
      scoutln(out, bytesOut, generalUtils.formatNumeric(qty, dpOnQuantities) + "</td>"); // entry in In
    }
    else
    if(type == 'G') // GRN
    {
      if(! status.equals("C") && stockProcessed.equals("Y"))
        balances[whichStore] += generalUtils.doubleFromStr(qty);
      scout(out, bytesOut, "<td align=right>");      
      scoutln(out, bytesOut, generalUtils.formatNumeric(qty, dpOnQuantities) + "</td><td>&nbsp;</td>"); // entry in In, blank Out
    }  
    else // adjustment
    {
      if(type == 'I') // is an In to this store
      {
        if(! status.equals("C") && stockProcessed.equals("Y"))
          balances[whichStore] += generalUtils.doubleFromStr(qty);
        scout(out, bytesOut, "<td align=right>");
        scoutln(out, bytesOut, generalUtils.formatNumeric(qty, dpOnQuantities) + "</td><td>&nbsp;</td>"); // entry in In, blank Out      
      }
      else // is an Out from this store
      {
        if(! status.equals("C") && stockProcessed.equals("Y"))
          balances[whichStore] -= generalUtils.doubleFromStr(qty);
        scout(out, bytesOut, "<td>&nbsp;</td><td align=right>"); // blank Out
        scoutln(out, bytesOut, generalUtils.formatNumeric(qty, dpOnQuantities) + "</td>"); // entry in In
      }      
    }
    
    // balance
    
    if(! status.equals("C"))
    {
      if(stockProcessed.equals("Y"))
      {
        generalUtils.doubleToBytesCharFormat(balances[whichStore], b, 0);
        generalUtils.formatNumeric(b, dpOnQuantities);
        scout(out, bytesOut, "<td align=right>");
    
        if(balances[whichStore] < 0)
          scout(out, bytesOut, "<span id=\"textRedHighlighting\">");

        if(type == 'S') // stock check
          scout(out, bytesOut, "<b>");

        scout(out, bytesOut, generalUtils.stringFromBytes(b, 0L));
    
        if(balances[whichStore] < 0)
          scout(out, bytesOut, "</span>");
        scoutln(out, bytesOut, "</td>");
      }  
      else scoutln(out, bytesOut, "<td>&nbsp;</td>");
    }
    else scoutln(out, bytesOut, "<td>&nbsp;</td>");

    // output weight-average cost
    
    if(useWAC)
    {
      
    }
    
    // document code
    
    scout(out, bytesOut, "<td>");

    if(code.length() == 0)
      scout(out, bytesOut, "&nbsp;</td>");
    else
    {
      switch(type)
      {
        case 'P' : scoutln(out, bytesOut, "<a href=\"javascript:pl('"  + code + "')\">" + code + "</a></td>"); break;
        case 'G' : scoutln(out, bytesOut, "<a href=\"javascript:grn('" + code + "')\">" + code + "</a></td>"); break;
        case 'C' : scoutln(out, bytesOut, "<a href=\"javascript:chk('" + code + "')\">" + code + "</a></td>"); break;
        case 'o' : 
        case 'I' : scoutln(out, bytesOut, "<a href=\"javascript:adj('" + code + "')\">" + code + "</a></td>"); break;
      }
    }

    // status
    
    scout(out, bytesOut, "<td>");
    if(status.equals("C"))
      scout(out, bytesOut, "<img src=\"" + imagesDir + "z0212.gif\" border=0>");
    else
    {
      if(stockProcessed.equals("Y"))
        ; 
      else scout(out, bytesOut, "?");
    }  
    
    scoutln(out, bytesOut, "</td><td>");
  
    if(remark == null) remark = "";
    scoutln(out, bytesOut, remark);

    scoutln(out, bytesOut, "</td><td></td>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void addToTmpTable(Connection con, Statement stmt, String docCode, String docType, String date, String status, String stockProcessed, String level,
                             String remark, String tmpTable) throws Exception
  {
    stmt = con.createStatement();
   
    if(remark == null) remark = "";
    
    String q = "INSERT INTO " + tmpTable + " ( DocumentCode, DocumentType, Date, Status, StockProcessed, Level, Remark ) "
             + "VALUES ('" + docCode + "','" + docType + "', {d '" + date + "'},'" + status + "','" + stockProcessed + "','" + level + "','"
             + generalUtils.sanitiseForSQL(remark) + "' )";

    stmt.executeUpdate(q);
  
    if(stmt != null) stmt.close();
  }

  //--------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean findEarliest(int[] resultDatesEncoded, int numStores, int[] earliest) throws Exception
  {
    boolean oneNotNull = false;
    int x;
    for(x=0;x<numStores;++x)
    {
      if(resultDatesEncoded[x] != -1)
        oneNotNull = true;
    }
    
    if(! oneNotNull)
      return true; // all done
    
    earliest[0] = -1;
    int earliestDateSoFar = 2000000000;
    for(x=0;x<numStores;++x)
    {
      if(resultDatesEncoded[x] != -1 && resultDatesEncoded[x] < earliestDateSoFar)
      {
        earliestDateSoFar = resultDatesEncoded[x];
        earliest[0] = x;
      }
    }
  
    return false;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void scout(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.print(str);
    bytesOut[0] += str.length();    
  }
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
