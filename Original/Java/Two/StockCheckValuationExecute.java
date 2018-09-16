// =======================================================================================================================================================================================================
// System: ZaraStar Accounts: Stock Check Valuation
// Module: StockCheckValuationExecute.java
// Author: C.K.Harvey
// Copyright (c) 2001-08 Christopher Harvey. All Rights Reserved.
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
import java.sql.*;
import java.io.*;

public class StockCheckValuationExecute extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  MiscDefinitions miscDefinitions = new MiscDefinitions();
  Inventory inventory = new Inventory();
  DefinitionTables definitionTables = new DefinitionTables();
  AccountsUtils accountsUtils = new AccountsUtils();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
     doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="";

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
      p1  = req.getParameter("p1"); // mfr
      p2  = req.getParameter("p2"); // date
      
      if(p2 == null) p2 = "";
      
      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, bytesOut);
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
 
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "StockCheckValuation", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3066, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush(); 
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String mfr,
                    String date, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    
    Connection con = null;
    Statement stmt = null, stmt2 = null;
    ResultSet rs = null;

    String uName = directoryUtils.getMySQLUserName();
    String pWord = directoryUtils.getMySQLPassWord();
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + uName + "&password=" + pWord);

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 3066, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "StockCheckValuation", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3066, bytesOut[0], 0, "ACC:" + mfr);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "StockCheckValuation", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3066, bytesOut[0], 0, "SID:" + mfr);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(date.length() == 0)
      date = "1970-01-01";
    else date = generalUtils.convertDateToSQLFormat(date);
    
    String[] financialYearStartMonth = new String[1];
    String[] financialYearEndMonth   = new String[1];
 
    definitionTables.getAppConfigFinancialYearMonths(con, stmt, rs, dnm, financialYearStartMonth, financialYearEndMonth);

    String s;
    int startMonth = generalUtils.detMonthNumFromMonthName(financialYearStartMonth[0]);
    if(startMonth < 10)
      s = "-0" + startMonth;
    else s = "-" + startMonth;

    s += "-01";
    
    String dateStartAccountingYear = accountsUtils.getAccountingYearForADate(con, stmt, rs, generalUtils.convertFromYYYYMMDD(date), dnm, localDefnsDir, defnsDir) + s;

    set(con, stmt, stmt2, rs, out, req, mfr, date, dateStartAccountingYear, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 3066, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), mfr);
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, Statement stmt2, ResultSet rs, PrintWriter out, HttpServletRequest req, String mfr, String date,
                   String dateStartAccountingYear, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir,
                   String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Stock Check Valuation</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3001, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewItem(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/ProductStockRecord?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm="
                           + dnm + "&bnm=" + bnm + "&p2=A&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3018, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function view(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/StockCheckForItem?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den
                           + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p1=\"+p1;}");
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
                         + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    int[] hmenuCount = new int[1];
 
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "3066", "", "StockCheckValuation", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Stock Check Valuation", "3066", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    String baseCurrency = accountsUtils.getBaseCurrency(con, stmt, rs, dnm, localDefnsDir, defnsDir);
    
    scoutln(out, bytesOut, "<form><table id=\"page\" border=0 cellspacing=2 cellpadding=3><tr><td colspan=10><p>Comparison of Stock Check Record levels against System Stock Trace levels; and valuation based on the Stock Check levels</td></tr>");

    scoutln(out, bytesOut, "<tr id=\"pageColumn\">");
    scoutln(out, bytesOut, "<td><p>Check Code</td>");
    scoutln(out, bytesOut, "<td><p>Item Code</td>");
    scoutln(out, bytesOut, "<td><p>Manufacturer</td>");
    scoutln(out, bytesOut, "<td><p>Manufacturer Code</td>");
    scoutln(out, bytesOut, "<td><p>Counted Level</td>");
    scoutln(out, bytesOut, "<td><p>Stock Trace Level</td>");
    scoutln(out, bytesOut, "<td><p>Value (" + baseCurrency + ")</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Store</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Remark</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Description;</td></tr>");
           
    double[] totalValuation = new double[1];  totalValuation[0] = 0.0;
    int[] skus = new int[1];  skus[0] = 0;
    double[] qtyItems = new double[1];  qtyItems[0] = 0.0;
            
    detSCRecs(con, stmt, stmt2, rs, out, mfr, date, dateStartAccountingYear, totalValuation, skus, qtyItems, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td colspan=6><p>Total Valuation in " + baseCurrency + ": " + generalUtils.formatNumeric(totalValuation[0], '2') + "</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    if(skus[0] > 0)
    {
      char dpOnQuantities = miscDefinitions.dpOnQuantities(con, stmt, rs, "5");
      scoutln(out, bytesOut, "<tr><td colspan=6><p>Warning: A total of " + generalUtils.formatNumeric(qtyItems[0], dpOnQuantities) + " items over " + skus[0] + " SKUs have no valuation</td></tr>");
    }

    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void detSCRecs(Connection con, Statement stmt, Statement stmt2, ResultSet rs, PrintWriter out, String mfr, String date, String dateStartAccountingYear,
                         double[] totalValuation, int[] skus, double[] qtyItems, String unm, String sid, String uty, String men, String den,
                       String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    char dpOnQuantities = miscDefinitions.dpOnQuantities(con, stmt, rs, "5");
    String[] cssFormat = new String[1];  cssFormat[0] = "";
        
    boolean useWAC = false;
    if(miscDefinitions.inventoryCostingMethod(con, stmt, rs).equals("WAC"))
      useWAC = true;

    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT CheckCode, ItemCode, StoreCode, Level, Remark FROM stockc WHERE Status != 'C' AND Date = {d '" + date + "'} AND Type = 'S' AND Level != '999999'");
      
      String checkCode, itemCode, storeCode, level, remark;
      while(rs.next())
      {
        checkCode = rs.getString(1);
        itemCode  = rs.getString(2);
        storeCode = rs.getString(3);
        level     = rs.getString(4);
        remark    = rs.getString(5);
        
        if(level  == null) level  = "0";
        if(remark == null) remark = "";

        process(con, stmt, stmt2, rs, out, useWAC, dateStartAccountingYear, date, mfr, checkCode, itemCode, storeCode, level, remark, dpOnQuantities, cssFormat, totalValuation, skus, qtyItems, unm, sid, uty, men, den, dnm, bnm, localDefnsDir,
                defnsDir, bytesOut);
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

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void process(Connection con, Statement stmt, Statement stmt2, ResultSet rs, PrintWriter out, boolean useWAC, String dateStartAccountingYear,
                       String date, String mfrReqd, String checkCode, String itemCode, String storeCode, String level, String remark, char dpOnQuantities,
                       String[] cssFormat, double[] totalValuation, int[] skus, double[] qtyItems, String unm, String sid, String uty, String men, String den,
                       String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut)
  {
    try
    {
      String[] mfr     = new String[1];
      String[] mfrCode = new String[1];
      String[] desc    = new String[1];
      getItemDetailsGivenCode(con, stmt, rs, itemCode, mfr, mfrCode, desc);

      if(mfrReqd.equals("___ALL___") || mfr[0].equals(mfrReqd))
      {
        double levelD = generalUtils.doubleFromStr(level);
      
        double value = 0.0;
        if(useWAC)
        {
          double wac = inventory.getWAC(con, stmt, rs, itemCode, dateStartAccountingYear, date, dnm);
          value = wac * levelD;
        }
      
        String yesterdaysDate = generalUtils.decode((generalUtils.encodeFromYYYYMMDD(date) - 1), localDefnsDir, defnsDir);

        double systemLevel = inventory.stockLevelForAStore(con, stmt, stmt2, rs, storeCode, itemCode, yesterdaysDate, unm, sid, uty, men, den, dnm, bnm);
      
        totalValuation[0] += generalUtils.doubleDPs(value, '2');
        
        if(cssFormat[0].equals("line1")) cssFormat[0] = "line2"; else cssFormat[0] = "line1";
     
        if(levelD != 0 && value == 0)
        {
          scoutln(out, bytesOut, "<tr bgcolor=tomato><td><p><a href=\"javascript:view('" + itemCode + "')\">" + checkCode + "</a></td>");
          qtyItems[0] += levelD;
          ++skus[0];
        }
        else scoutln(out, bytesOut, "<tr id='" + cssFormat[0] + "'><td><p><a href=\"javascript:view('" + itemCode + "')\">" + checkCode + "</a></td>");
        
        scoutln(out, bytesOut, "<td><p><a href=\"javascript:viewItem('" + itemCode + "')\">" + itemCode + "</a></td>");
        scoutln(out, bytesOut, "<td><p>" + mfr[0] + "</td>");
        scoutln(out, bytesOut, "<td><p>" + mfrCode[0] + "</td>");
        scoutln(out, bytesOut, "<td align=center><p>" + generalUtils.formatNumeric(levelD, dpOnQuantities) + "</td>");
        scoutln(out, bytesOut, "<td align=center><p>" + generalUtils.formatNumeric(systemLevel, dpOnQuantities) + "</td>");
 
        scoutln(out, bytesOut, "<td align=center><p>" + generalUtils.formatNumeric(value, '2') + "</td>");
        
        scoutln(out, bytesOut, "<td nowrap><p>" + storeCode + "</td>");
        scoutln(out, bytesOut, "<td><p>" + remark + "</td>");
        scoutln(out, bytesOut, "<td nowrap><p>" + desc[0] + "</td></tr>");
      }
    }
    catch(Exception e)
    {
      System.out.println(e);
    }
  }
  
  //------------------------------------------------------------------------------------------------------------------------------------------------------------
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

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int[] bytesOut, String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);
  }

}
