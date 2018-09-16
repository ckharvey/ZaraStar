// =======================================================================================================================================================================================================
// System: ZaraStar: Analytic: Ext user Stock Enquiry
// Module: StockEnquiryExternal.java
// Author: C.K.Harvey
// Copyright (c) 2000-08 Christopher Harvey. All Rights Reserved.
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
import java.net.*;
import java.sql.*;

public class StockEnquiryExternal extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  MiscDefinitions miscDefinitions = new MiscDefinitions();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  Inventory inventory = new Inventory();
  PickingList pickingList = new PickingList();
  SalesOrder salesOrder = new SalesOrder();
  Customer customer = new Customer();
  Profile profile = new Profile();
  AdminControlUtils adminControlUtils = new AdminControlUtils();
  
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
      p1  = req.getParameter("p1"); // itemcode
      p2  = req.getParameter("p2"); // mfr
      p3  = req.getParameter("p3"); // mfrCode
      
      if(p1 == null) p1 = "";
      if(p2 == null) p2 = "";
      if(p3 == null) p3 = "";
      
      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, p3, bytesOut);
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

      System.out.println("2001: " + e);
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "StockEnquiryExternal", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2001, bytesOut[0], 0, "ERR:" + p1 + ":" + p2 + ":" + p3);
      if(out != null) out.flush(); 
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1, String p2, String p3, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    String userName = directoryUtils.getMySQLUserName();
    String passWord = directoryUtils.getMySQLPassWord();
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + userName + "&password=" + passWord);
    Statement stmt = null, stmt2 = null;
    ResultSet rs = null, rs2 = null;

    if((uty.equals("R") && ! adminControlUtils.notDisabled(con, stmt, rs, 902)) || (uty.equals("A") && ! adminControlUtils.notDisabled(con, stmt, rs, 802)))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "StockEnquiryExternal", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2001, bytesOut[0], 0, "ACC:" + p1 + ":" + p2 + ":" + p3);
      if(con != null) con.close();  
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "StockEnquiryExternal", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2001, bytesOut[0], 0, "SID:" + p1 + ":" + p2 + ":" + p3);
      if(con  != null) con.close();  
      if(out != null) out.flush(); 
      return;
    }

    byte[] itemCode = new byte[21];
    generalUtils.strToBytes(itemCode, p1);
    generalUtils.toUpper(itemCode, 0);

    set(con, stmt, stmt2, rs, rs2, out, req, itemCode, p2, p3, unm, sid, uty, men, den, dnm, bnm, imagesDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 2001, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1 + ":" + p2 + ":" + p3);
    if(con  != null) con.close();  
    if(out != null) out.flush(); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, HttpServletRequest req, byte[] itemCode, String mfr, String mfrCode, String unm, String sid, String uty, String men, String den,
                   String dnm, String bnm, String imagesDir, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    String[] customerCode = new String[1];
    
    int priceBand = 0;

    if(uty.equals("R"))
    {
      int i = unm.indexOf("_");
      if(! profile.getExternalAccessNameCustomerCode(con, stmt, rs, unm.substring(0, i), dnm, localDefnsDir, defnsDir, customerCode))
        customerCode[0] = "";
      else priceBand = customer.getPriceBand(con, stmt, rs, customerCode[0]);
    }
    
    scoutln(out, bytesOut, "<html><head><title>Stock Enquiry</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    String[] name         = new String[1];
    String[] companyName  = new String[1];
    String[] accessRights = new String[1];
    int i = unm.indexOf("_");

    profile.getExternalAccessNameCompanyAndRights(con, stmt, rs, unm.substring(0, i), name, companyName, accessRights);

    if(accessRights[0].equals("sales") || accessRights[0].equals("accounts"))
    {
      scoutln(out, bytesOut, "function viewSO(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/SalesOrderRegisteredUser?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    scoutln(out, bytesOut, "function add(code){");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/ProductCart?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+code;}");
    
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
 
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "2001", "", "StockEnquiryExternal", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
  
    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Stock Enquiry Results", "2001", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<form><table id=\"page\" width=100% border=0>");

    byte[] data   = new byte[5000];

    String itemCodeStr = generalUtils.stringFromBytes(itemCode, 0L);

    if(mfrCode.length() > 0)
    {
      itemCodeStr = inventory.getItemCodeGivenMfrAndMfrCode(con, stmt, rs, mfr, mfrCode);
      if(itemCodeStr.length() == 0)
      {
        scoutln(out, bytesOut, "<br><br><p>Manufaturer: " + mfr + ", and Manufacturer Code: " + mfrCode + " Not Found<br><br>");
        scoutln(out, bytesOut, "</table></form>");
        scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir));
        return;
      }
      
      generalUtils.strToBytes(itemCode, itemCodeStr);
    }

    boolean onlyShowToWeb = false;
    if(! uty.equals("I"))
      onlyShowToWeb = true;

    if(itemCode[0] == '\000')
      scoutln(out, bytesOut, "<br><br><p>No Item Code Specified<br><br>");
    else
    if(inventory.getStockRecGivenCode(con, stmt, rs, itemCode, '\000', onlyShowToWeb, data) == -1) // rec not found
      scoutln(out, bytesOut, "<br><br><p>Item Code &nbsp; &nbsp; " + itemCodeStr + "&nbsp; &nbsp; <span id=\"enquiryMessage\">Not Found</span><br><br>");
    else // found
    {
      String desc  = generalUtils.dfsAsStr(data, (short)1);
      String desc2 = generalUtils.dfsAsStr(data, (short)2);
      mfr          = generalUtils.dfsAsStr(data, (short)3);
      mfrCode      = generalUtils.dfsAsStr(data, (short)4);

      scoutln(out, bytesOut, "<tr><td nowrap><p>Our Stock Code: </td><td width=90% nowrap><p><b>" + itemCodeStr + "</b></td></tr>");
      scoutln(out, bytesOut, "<tr><td nowrap><p>Description: </td><td nowrap><p><b>" + desc + "</b></td></tr>");
      scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><b>" + desc2 + "</b></td></tr>");
      scoutln(out, bytesOut, "<tr><td nowrap><p>Manufacturer: </td><td nowrap><p><b>" + mfr + "</b></td></tr>");
      scoutln(out, bytesOut, "<tr><td nowrap><p>Manufacturer Code: </td><td nowrap><p><b>" + mfrCode + "</b></td></tr>");

      byte[] b = new byte[100];
   
      switch(priceBand)
      {
        case 0 : generalUtils.dfs(data, (short)25, b); break;
        case 1 : generalUtils.dfs(data, (short)20, b); break;
        case 2 : generalUtils.dfs(data, (short)21, b); break;
        case 3 : generalUtils.dfs(data, (short)22, b); break;
        case 4 : generalUtils.dfs(data, (short)23, b); break;
      }
      
      generalUtils.bytesDPsGivenSeparator(true, '\000', '2', b, 20, 0);
      
      char dpOnQuantities = miscDefinitions.dpOnQuantities(con, stmt, rs, "5");

      scoutln(out, bytesOut, "</td></tr>");
 
      stockLevel(out, itemCodeStr, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, dpOnQuantities, bytesOut);

      if(! uty.equals("A"))
      {
        // from this point on, store in string 'html' so that we can total and output totals before the details

        String[] html = new String[1];
        html[0] = "";

        double[] total = new double[1]; total[0] = 0.0;

        total[0] = 0.0;
        onSO(con, stmt, stmt2, rs, rs2, itemCodeStr, customerCode[0], dpOnQuantities, total, html);
        generalUtils.doubleToBytesCharFormat(total[0], b, 0);
        generalUtils.bytesDPsGivenSeparator(true, '\000', dpOnQuantities, b, 20, 0);
        generalUtils.formatNumeric(b, dpOnQuantities);
        scoutln(out, bytesOut, "<tr><td nowrap><p>Total you have on order from us:</td><td><span id=\"textNumericValue\">" + generalUtils.stringFromBytes(b, 0L) + "</span></td></tr>\n");

        scoutln(out, bytesOut, "</table><table id=\"page\" width=100%>");
        scoutln(out, bytesOut, "<tr><td colspan=7><hr></td></tr>");

        scoutln(out, bytesOut, html[0]);

        if(! adminControlUtils.notDisabled(con, stmt, rs, 908))
          scoutln(out, bytesOut, "<tr><td nowrap><p><a href=\"javascript:add('" + itemCodeStr + "')\"><img border=0 src=\"" + imagesDir + "toCart.png\"></a></td></tr>\n");
      }
      else
      {
        scoutln(out, bytesOut, "</table><table id=\"page\" width=100%>");
        scoutln(out, bytesOut, "<tr><td colspan=7><hr></td></tr>");
        if(! adminControlUtils.notDisabled(con, stmt, rs, 808))
          scoutln(out, bytesOut, "<tr><td nowrap><p><a href=\"javascript:add('" + itemCodeStr + "')\"><img border=0 src=\"" + imagesDir + "toCart.png\"></a></td></tr>\n");
      }
    }

    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void stockLevel(PrintWriter out, String itemCodeStr, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, char dpOnQuantities, int[] bytesOut) throws Exception
  {
    String traceList = getStockLevelsViaTrace(itemCodeStr, unm, uty, sid, men, den, dnm, bnm, localDefnsDir);
    // traceList = "SamLeong\001150000\001SyedAlwi\00150000\001\0"

    String pendingsList = getPendings(itemCodeStr, unm, uty, sid, men, den, dnm, bnm, localDefnsDir);
    // pendings = <onPO> \001 <onSO> \001 <onPLNotCompleted> \001 <onGRNInTransit> \001 
    
    double[] totalStockLevel = new double[1];
    double[] totalPending    = new double[1];
    
    totalLevels(traceList, pendingsList, totalStockLevel, totalPending);
    
    byte[] b = new byte[20];
    generalUtils.doubleToBytesCharFormat(totalStockLevel[0], b, 0);
    generalUtils.bytesDPsGivenSeparator(true, '\000', dpOnQuantities, b, 20, 0);
    generalUtils.formatNumeric(b, dpOnQuantities);
    
    scoutln(out, bytesOut, "<tr><td nowrap><p>Availability: </td><td nowrap><p><b>" + generalUtils.stringFromBytes(b, 0L));

    generalUtils.doubleToBytesCharFormat(totalPending[0], b, 0);
    generalUtils.bytesDPsGivenSeparator(true, '\000', dpOnQuantities, b, 20, 0);
    generalUtils.formatNumeric(b, dpOnQuantities);

    scoutln(out, bytesOut, " (" + generalUtils.stringFromBytes(b, 0L) + " of which are reserved)</b></td></tr>");
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
  private String getPendings(String itemCode, String unm, String uty, String sid, String men, String den, String dnm, String bnm, String localDefnsDir) throws Exception
  { 
    URL url = new URL("http://" + serverUtils.serverToCall("OFSA", localDefnsDir) + "/central/servlet/StockGenerateOnPOOnSOOnPLOnGRNs?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=" + itemCode + "&bnm=" + bnm);

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
  private void totalLevels(String traceList, String pendingsList, double[] totalStockLevel, double[] totalPending) throws Exception
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

    y=0;
    len = pendingsList.length();
    totalPending[0] = 0.0;

    while(y < len && pendingsList.charAt(y) != '\001') // step over PO
      ++y;
    ++y;

    thisQty = "";
    while(y < len && pendingsList.charAt(y) != '\001') // want SO
      thisQty += pendingsList.charAt(y++);
    ++y;

    totalPending[0] += generalUtils.doubleFromStr(thisQty);

    thisQty = "";
    while(y < len && pendingsList.charAt(y) != '\001') // want PL
      thisQty += pendingsList.charAt(y++);
    ++y;

    totalPending[0] += generalUtils.doubleFromStr(thisQty);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void onSO(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String itemCode, String customerCode, char dpOnQuantities, double[] total, String[] html) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.SOCode, t2.Line, t2.Quantity, t2.DeliveryDate, t1.Date, t1.CustomerPOCode FROM sol AS t2 INNER JOIN so AS t1 ON t2.SOCode = t1.SOCode WHERE t1.CompanyCode = '" + customerCode + "' AND t2.ItemCode = '"
                         + itemCode + "' AND t1.Status != 'C' ORDER BY t1.Date, t2.SOCode, t2.Line");

    String soCode, soLine, date, deliveryDate, customerPOCode, cssFormat="";
    double qty, actualQty;
    byte[] b = new byte[20];
    byte[] b2 = new byte[20];
    boolean first = true;

    while(rs.next())
    {    
      soCode         = rs.getString(1);
      soLine         = rs.getString(2);
      qty            = generalUtils.doubleFromStr(rs.getString(3));
      deliveryDate   = rs.getString(4);
      date           = rs.getString(5);
      customerPOCode = rs.getString(6);

      actualQty = pickingList.getTotalPickedForASOLine(con, stmt2, rs2, soCode, soLine);

      if(first)
      {
        html[0] += "<tr><td><p>&nbsp;</td></tr>";
        html[0] += "<tr id=\"pageColumn\"><td colspan=7><p>Previous Sales Orders for: " + itemCode + "</td></tr>";
        html[0] += "<tr id=\"pageColumn\"><td><p>Ordered</td><td><p>Outstanding</td>";
        html[0] += "<td nowrap><p>SO Code</td>";
        html[0] += "<td nowrap><p>SO Date</td>";
        html[0] += "<td nowrap><p>Your PO Code</td>";
        html[0] += "<td nowrap><p>Delivery Date</td>";
        first = false;
      }

      total[0] += (qty - actualQty);

      generalUtils.doubleToBytesCharFormat((qty - actualQty), b, 0);
      generalUtils.bytesDPsGivenSeparator(true, '\000', dpOnQuantities, b, 20, 0);
      generalUtils.formatNumeric(b, dpOnQuantities);

      generalUtils.doubleToBytesCharFormat(qty, b2, 0);
      generalUtils.bytesDPsGivenSeparator(true, '\000', dpOnQuantities, b2, 20, 0);
      generalUtils.formatNumeric(b2, dpOnQuantities);

      if(cssFormat.equals("line1")) cssFormat = "line2"; else cssFormat = "line1";

      html[0] += "<tr id=\"" + cssFormat + "\"><td nowrap align=center><p>" + generalUtils.stringFromBytes(b2, 0L)
              + "</td><td nowrap align=center><p>" + generalUtils.stringFromBytes(b, 0L) + "</td>";
      html[0] += "<td nowrap><p><a href=\"javascript:viewSO('" + soCode + "')\">" + soCode + "</a></td>";
      html[0] += "<td nowrap><p>" + generalUtils.convertFromYYYYMMDD(date) + "</td>";
      html[0] += "<td nowrap><p>" + customerPOCode + "</td>";
      html[0] += "<td nowrap><p>" + generalUtils.convertFromYYYYMMDD(deliveryDate) + "</td></tr>";
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
