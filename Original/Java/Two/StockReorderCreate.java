// =======================================================================================================================================================================================================
// System: ZaraStar: Product: Stock ReOrder: Create PO
// Module: StockReorderCreate.java
// Author: C.K.Harvey
// Copyright (c) 1999-2007 Christopher Harvey. All Rights Reserved.
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
import java.net.*;
import java.io.*;
import java.sql.*;

public class StockReorderCreate extends HttpServlet
{                          
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  AccountsUtils accountsUtils = new AccountsUtils();
  MiscDefinitions miscDefinitions = new MiscDefinitions();
  DocumentUtils documentUtils = new DocumentUtils();
  Inventory inventory = new Inventory();
  PurchaseOrder purchaseOrder = new PurchaseOrder();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", currency="", rate="", count;
    
    try
    {
      directoryUtils.setContentHeaders(res);
      out = res.getWriter();

      byte[] itemCodesEtc    = new byte[1000]; itemCodesEtc[0]    = '\000';
      int[]  itemCodesEtcLen = new int[1];     itemCodesEtcLen[0] = 1000;

      byte[] newItem = new byte[100];

      int x, len, justInCaseCount = 1;
      
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
        if(name.equals("currency"))
          currency = value[0];
        else
        if(name.equals("rate"))
          rate = value[0];
        else
        if(name.length() > 2 && name.substring(1, 3).equals(".x")) ;
        else
        if(name.length() > 2 && name.substring(1, 3).equals(".y")) ;
        else
        {
          // name is: <count> <colon> <itemCode>
          // value is: qty reqd

          x = 0;
          len = name.length();
          count = "";
          while(x < len && name.charAt(x) != ':') // just-in-case
            count += name.charAt(x++);
          ++x;

          if(x == len) // no ':' found; may be an old format report
            generalUtils.catAsBytes(justInCaseCount++ + "\002" + name + "\002" + value[0] + "\001", 0, newItem, true);
          else generalUtils.catAsBytes(count + "\002" + name.substring(x) + "\002" + value[0] + "\001", 0, newItem, true);

          itemCodesEtc = generalUtils.addToList(false, newItem, itemCodesEtc, itemCodesEtcLen, true);
        }
      }
      
      doIt(out, req, itemCodesEtc, currency, rate, unm, sid, uty, men, den, dnm, bnm, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "StockReorderCreate", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1021, bytesOut[0], 0, "ERR:" + e);
      if(out != null) out.flush(); 
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, byte[] itemCodesEtc, String currency, String rate, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
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
      String uName = directoryUtils.getMySQLUserName();
      String pWord = directoryUtils.getMySQLPassWord();
      Class.forName("com.mysql.jdbc.Driver").newInstance();
      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + uName + "&password=" + pWord);

      if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 1021, unm, uty, dnm, localDefnsDir, defnsDir))
      {
        messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "StockReorderCreate", imagesDir, localDefnsDir,
                           defnsDir, bytesOut);
        serverUtils.etotalBytes(req, unm, dnm, 1021, bytesOut[0], 0, "ACC:");
        if(con != null) con.close();
        if(out != null) out.flush(); 
        return;
      }

      if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
      {
        messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "StockReorderCreate", imagesDir, localDefnsDir,
                           defnsDir, bytesOut);
        serverUtils.etotalBytes(req, unm, dnm, 1021, bytesOut[0], 0, "SID:");
        if(con != null) con.close();
        if(out != null) out.flush(); 
        return;
      }

      byte[] nextCode = new byte[21];

      if(process(con, stmt, rs, itemCodesEtc, currency, rate, unm, dnm, localDefnsDir, defnsDir, bytesOut, nextCode))
      {
        getRec(out, unm, sid, uty, men, den, dnm, bnm, generalUtils.stringFromBytes(nextCode, 0L), localDefnsDir);    
      }
      else messagePage.msgScreen(false, out, req, 23, unm, sid, uty, men, den, dnm, bnm, "1021", imagesDir, localDefnsDir, defnsDir, bytesOut);

    }
    catch(Exception e) { } 

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 1021, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con != null) con.close();

    if(out != null) out.flush(); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean process(Connection con, Statement stmt, ResultSet rs, byte[] itemCodesEtc, String currency, String rate, String unm, String dnm, String localDefnsDir, String defnsDir, int[] bytesOut, byte[] newDocCode) throws Exception
  {
    String itemCode, value;

    documentUtils.getNextCode(con, stmt, rs, "po", true, newDocCode);

    generalUtils.toUpper(newDocCode, 0);
    if(! putHeader(con, stmt, rs, newDocCode, unm, dnm, currency, rate, localDefnsDir, defnsDir, bytesOut))
      return false;

    double rateD = generalUtils.doubleFromStr(rate);

    // count + "\002" + itemCode + "\002" + qty + "\001"
    int x, entryNum = 0;
    byte[] entry = new byte[100];

    boolean quit = generalUtils.getListEntryByNum(entryNum, itemCodesEtc, entry);

    while(quit)
    {
      x = 0;
      while(entry[x] != '\002' && entry[x] != '\000') // just-in-case
        ++x;
      ++x;

      itemCode="";
      while(entry[x] != '\002' && entry[x] != '\000')
        itemCode += (char)entry[x++];
      ++x;

      value="";
      while(entry[x] != '\001' && entry[x] != '\000')
        value += (char)entry[x++];

      ++entryNum;
      quit = generalUtils.getListEntryByNum(entryNum, itemCodesEtc, entry);

      processAnItemCode(con, stmt, rs, entryNum, itemCode, value, newDocCode, rateD, unm, dnm, localDefnsDir, defnsDir, bytesOut);
    }
    
    return true;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void processAnItemCode(Connection con, Statement stmt, ResultSet rs, int count, String itemCode, String value, byte[] newDocCode, double rateD, String unm, String dnm, String localDefnsDir, String defnsDir, int[] bytesOut)
                                 throws Exception
  {
    if(generalUtils.doubleFromStr(value) == 0)
      return;

    addLine(con, stmt, rs, count, newDocCode, itemCode, value, rateD, unm, dnm, localDefnsDir, defnsDir, bytesOut);

    System.out.println("Adding to PO: " + itemCode+ " " + value);
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void addLine(Connection con, Statement stmt, ResultSet rs, int count, byte[] poCode, String itemCode, String qty, double rateD, String unm, String dnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    byte[] data         = new byte[3000];
    byte[] buf          = new byte[3000];
    byte[] itemCodeB    = new byte[21];
    byte[] originalLine = new byte[20];
    byte[] b            = new byte[60];

    generalUtils.strToBytes(itemCodeB, itemCode);

    if(inventory.getStockRecGivenCode(con, stmt, rs, itemCodeB, '\000', data) == -1) // still, jic
      return;

    generalUtils.catAsBytes("pol.line=" + count + "\001", 0, buf, true);
    generalUtils.catAsBytes("pol.entry=" + count + "\001", 0, buf, false);
    generalUtils.catAsBytes("pol.pocode=" + generalUtils.stringFromBytes(poCode, 0L) + "\001", 0, buf, false);

    generalUtils.catAsBytes("pol.itemcode=" + itemCode + "\001", 0, buf, false);

    generalUtils.catAsBytes("pol.description=" + generalUtils.dfsAsStr(data, (short)1) + "\001", 0, buf, false);

    generalUtils.catAsBytes("pol.SupplierItemCode=" + generalUtils.dfsAsStr(data, (short)4) + "\001", 0, buf, false);

    String unitPrice  = generalUtils.dfsAsStr(data, (short)18);
    double unitPriceD = generalUtils.doubleFromStr(unitPrice);

    generalUtils.catAsBytes("pol.unitprice=" + unitPrice + "\001", 0, buf, false);

    generalUtils.catAsBytes("pol.quantity=" + qty + "\001", 0, buf, false);

    double qtyD = generalUtils.doubleFromStr(qty);

    generalUtils.catAsBytes("pol.amount=" + (qtyD * unitPriceD * rateD)  + "\001", 0, buf, false);
    generalUtils.catAsBytes("pol.amount2=" + (qtyD * unitPriceD)  + "\001", 0, buf, false); // issue currency

    String gstRate = accountsUtils.getDefaultGSTRate(con, stmt, rs, dnm, localDefnsDir, defnsDir);
    generalUtils.catAsBytes("pol.gstrate=" + gstRate + "\001", 0, buf, false);

    generalUtils.catAsBytes("pol.requiredby=01.01.1970\001", 0, buf, false);

    generalUtils.catAsBytes("pol.uom=" + generalUtils.dfsAsStr(data, (short)48) + "\001", 0, buf, false);

    generalUtils.catAsBytes("pol.storecode=" + miscDefinitions.getDefaultStore(con, stmt, rs) + "\001", 0, buf, false);

    originalLine[0] = '\000';

    purchaseOrder.putLine(con, stmt, rs, originalLine, poCode, unm, dnm, localDefnsDir, defnsDir, 'C', buf, generalUtils.lengthBytes(buf, 0), b, bytesOut);
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean putHeader(Connection con, Statement stmt, ResultSet rs, byte[] newDocCode, String unm, String dnm, String currency, String rate, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    byte[] b    = new byte[50];
    byte[] data = new byte[3000];

    generalUtils.zeroize(data, 3000);

    generalUtils.repAlpha(data, 3000, (short)0, newDocCode);

    generalUtils.repAlpha(data, 3000, (short)1, generalUtils.todaySQLFormat(localDefnsDir, defnsDir));

    generalUtils.repAlpha(data, 3000, (short)2, "-"); // suppliercode

    generalUtils.stringToBytes(unm, 0, b);
    generalUtils.repAlpha(data, 3000, (short)22, b); // signon

    generalUtils.stringToBytes(currency, 0, b);
    generalUtils.repAlpha(data, 3000, (short)30, rate);

    generalUtils.stringToBytes(currency, 0, b);
    generalUtils.repAlpha(data, 3000, (short)31, currency);

    generalUtils.repAlpha(data, 3000, (short)16, "0.0");        // GSTTotal 
    generalUtils.repAlpha(data, 3000, (short)17, "0.0");        // TotalTotal 
    generalUtils.repAlpha(data, 3000, (short)39, "0");          // baseTotalTotal 
    generalUtils.repAlpha(data, 3000, (short)40, "0");          // BaseGSTTotal 
    generalUtils.repAlpha(data, 3000, (short)50, "0");          // GroupDiscount 
    generalUtils.repAlpha(data, 3000, (short)51, "0");          // GroupDiscountValue 
    generalUtils.repAlpha(data, 3000, (short)18, "1970-01-01"); // unused3
    generalUtils.repAlpha(data, 3000, (short)19, "1970-01-01"); // unused4     
    generalUtils.repAlpha(data, 3000, (short)53, "1970-01-01"); // requiredBy
    generalUtils.repAlpha(data, 3000, (short)56, "0");          // printCount
    generalUtils.repAlpha(data, 3000, (short)20, "0");          // unused5 
    generalUtils.repAlpha(data, 3000, (short)22, unm);

    if(purchaseOrder.putRecHeadGivenCode(con, stmt, rs, newDocCode, 'N', data, dnm, localDefnsDir, defnsDir) != ' ')
      return false;
    return true;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void getRec(PrintWriter out, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String code,
                      String localDefnsDir) throws Exception
  {
    URL url = new URL("http://" + serverUtils.serverToCall("OFSA", localDefnsDir) + "/central/servlet/PurchaseOrderPage");

    HttpURLConnection uc = (HttpURLConnection)url.openConnection();
    uc.setDoInput(true);
    uc.setDoOutput(true);
    uc.setUseCaches(false);
    uc.setDefaultUseCaches(false);
    uc.setRequestProperty("Content-Type", "application/x-www-form.urlencoded");

    String s2 = "&unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm
              + "&bnm=" + bnm + "&p1="  + generalUtils.sanitise(code) + "&p2=A&p3=&p4=";

    Integer len = new Integer(s2.length());
    uc.setRequestProperty("Content-Length", len.toString());
    
    uc.setRequestMethod("POST");

    PrintWriter p = new PrintWriter(uc.getOutputStream());
    p.print(s2);    
    p.flush();
    p.close();

    BufferedReader di = new BufferedReader(new InputStreamReader(uc.getInputStream()));
    String s = di.readLine();
    while(s != null)
    {
      out.println(s);
      s = di.readLine();
    }

    di.close();
  }

}
