// =======================================================================================================================================================================================================
// System: ZaraStar: Accounts: Stock Ledger
// Module: AccountsStockLedger.java
// Author: C.K.Harvey
// Copyright (c) 2000-09 Christopher Harvey. All Rights Reserved.
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

public class AccountsStockLedger extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  AccountsUtils accountsUtils = new AccountsUtils();
  MiscDefinitions miscDefinitions = new MiscDefinitions();
  DefinitionTables definitionTables = new DefinitionTables();
  Inventory inventory = new Inventory();

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
      p1  = req.getParameter("p1"); // year
      p2  = req.getParameter("p2"); // mfr

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

      System.out.println("6005: " + e);
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "AccountsStockLedger", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6005, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1, String p2, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 6028, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "AccountsStockLedger", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6005, bytesOut[0], 0, "ACC:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "AccountsStockLedger", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6005, bytesOut[0], 0, "SID:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    set(con, stmt, rs, out, req, p1, p2, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 6005, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String year, String mfr, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir,
                   String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Stock Ledger</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    scoutln(out, bytesOut, "function item(itemCode){var p2=sanitise(itemCode);");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/AccountsStockLedgerDisplay?p1=" + year + "&p2=\"+p2+\"&unm=" + unm + "&uty=" + uty + "&sid=" + sid + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\";}");

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

    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\"></head>");

    int[] hmenuCount = new int[1];

    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "6005", "", "AccountsStockLedger", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Stock Ledger", "6005",  unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<form><table id=\"page\" width=100% border=0>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    if(mfr.equals("___ALL___"))
      getAll(con, stmt, rs, out, bytesOut);
    else getItems(con, stmt, rs, out, mfr, bytesOut);

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir));
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void getItems(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String mfr, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT ItemCode, ManufacturerCode FROM stock WHERE Manufacturer = '" + generalUtils.sanitiseForSQL(mfr) + "' ORDER BY ItemCode");

    String itemCode;

    while(rs.next())
    {
      itemCode = rs.getString(1);
      scoutln(out, bytesOut, "<tr><td><p><a href=\"javascript:item('" + itemCode + "')\">" + itemCode + "</a></td><td><p>" + rs.getString(2) + "</td></tr>");
    }

    scoutln(out, bytesOut, "</select>");

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void getAll(Connection con, Statement stmt, ResultSet rs, PrintWriter out, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT ItemCode, Manufacturer, ManufacturerCode FROM stock ORDER BY ItemCode");

    String itemCode, mfr, mfrCode;

    while(rs.next())
    {
      itemCode = rs.getString(1);
      mfr      = rs.getString(2);
      mfrCode  = rs.getString(3);

      scoutln(out, bytesOut, "<tr><td><p><a href=\"javascript:item('" + itemCode + "')\">" + itemCode + "</a></td><td><p>" + rs.getString(2) + "</td></tr>");
    }

    scoutln(out, bytesOut, "</select>");

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {
    out.println(str);
    bytesOut[0] += (str.length() + 2);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public double getWAC(Connection con, Statement stmt, ResultSet rs, String itemCode, String dateFrom, String dateTo, String dnm, double[] numItemsPurchased) throws Exception
  {
    byte[] list = new byte[1000];
    int[] listLen = new int[1];  listLen[0] = 1000;

    double[] openingLevel = new double[1];
    double[] openingWAC   = new double[1];
    inventory.getWACDetailsGivenCode(con, stmt, rs, itemCode, openingLevel, openingWAC, dateFrom);

    String yearEndDate = accountsUtils.getAccountingYearEndDateForADate(con, stmt, rs, dateFrom, dnm);

    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT t2.Quantity, t1.Date, t1.GRCode, t2.Line FROM grl AS t2 INNER JOIN gr AS t1 ON t1.InvoiceCode = t2.InvoiceCode WHERE t2.ItemCode = '" + itemCode
                         + "' AND t1.Status != 'C' AND t2.PODate != {d '1970-01-01'} AND t2.PODate < {d '" + dateFrom + "'}  " +
                         "    AND t1.Date <= {d '" + dateTo + "'} AND t1.Date <= {d '" + yearEndDate + "'}");

      while(rs.next())
        list = addToTmp("PI", generalUtils.doubleFromStr(rs.getString(1)), generalUtils.doubleFromStr(rs.getString(2)), rs.getString(3), rs.getString(4), list, listLen);

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.Quantity, t2.Amount, t1.Date, t1.InvoiceCode FROM invoicel AS t2 INNER JOIN invoice AS t1 ON t1.InvoiceCode = t2.InvoiceCode WHERE t2.ItemCode = '" + itemCode + "' AND t1.Status != 'C' AND t1.Date >= {d '"
                         + dateFrom + "'} AND t1.Date <               {d '" + dateTo + "'}");

    while(rs.next())
      list = addToTmp("SI", generalUtils.doubleFromStr(rs.getString(1)), generalUtils.doubleFromStr(rs.getString(2)), rs.getString(3), rs.getString(4), list, listLen);

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println("getWAC: " + e);
      return 0;
    }


    String docType, qtyStr, amtStr, dateAndType, docCode;
    double qty, amt;
    double currentLevel = openingLevel[0];
    double currentWAC   = openingWAC[0];
    double currentValue = currentWAC * currentLevel;

    byte[] entry = new byte[1000]; // plenty
    int x, y, len, entryNum = 0;
    while(generalUtils.getListEntryByNum(entryNum++, list, entry))
    {
      x = 0;
      len = generalUtils.lengthBytes(entry, 0);
      if(len > 0) //  call seems to return empty entry... prob in getListEntryByNum??? FIXME
      {
        dateAndType ="";
        while(x < len && entry[x] != '\002') // just-in-case
          dateAndType += (char)entry[x++];

        if(dateAndType.length() > 0)
        {
        y = dateAndType.length();
        docType = "" + dateAndType.charAt(y - 2) + dateAndType.charAt(y - 1);
        ++x;
        qtyStr = "";
        while(x < len && entry[x] != '\002') // just-in-case
          qtyStr += (char)entry[x++];
        ++x;
        amtStr = "";
        while(x < len && entry[x] != '\002') // just-in-case
          amtStr += (char)entry[x++];
        ++x;
        docCode = "";
        while(x < len && entry[x] != '\002') // just-in-case
          docCode += (char)entry[x++];

        qty = generalUtils.doubleDPs(generalUtils.doubleFromStr(qtyStr), '2');
        amt = generalUtils.doubleDPs(generalUtils.doubleFromStr(amtStr), '2');

        if(docType.equals("GR"))
        {
          currentLevel += qty;
          currentValue += amt;
          if(currentLevel != 0)
            currentWAC = (currentValue / currentLevel);
        }
        else
        if(docType.equals("PL"))
        {
          currentLevel -= qty;
          currentValue -= (currentWAC * qty);
        }
        }}
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    numItemsPurchased[0] = currentLevel;

    try
    {
      currentWAC = generalUtils.doubleDPs(currentWAC, '2');
    }
    catch(Exception e)
    {
      currentWAC = 0;
      System.out.println(itemCode + ": " + e);
    }

    return currentWAC;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private byte[] addToTmp(String docType, double qty, double amt, String date, String docCode, byte[] list, int[] listLen) throws Exception
  {
    String s = date + "-" + docType + "\002" + qty + "\002" + amt + "\002" + docCode + "\002\001";

    byte[] newItem = new byte[s.length() + 1];
    generalUtils.strToBytes(newItem, s);

    list = generalUtils.addToList(true, newItem, list, listLen);

    return list;
  }

}
