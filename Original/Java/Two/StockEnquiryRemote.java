// =======================================================================================================================================================================================================
// System: ZaraStar: Analytic: Stock Enquiry - remote site
// Module: StockEnquiryRemote.java
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
import java.net.*;
import java.sql.*;

public class StockEnquiryRemote extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  MiscDefinitions miscDefinitions = new MiscDefinitions();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  PickingList pickingList = new PickingList();
  SalesOrder salesOrder = new SalesOrder();
  Customer customer = new Customer();
  Profile profile = new Profile();
  
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="", p3="", p4="", p5="", p6="", p9="", p14="";

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
      p1  = req.getParameter("p1");  // mfr
      p2  = req.getParameter("p2");  // mfrCode
      p3  = req.getParameter("p3");  // itemCode
      p4  = req.getParameter("p4");  // category
      p5  = req.getParameter("p5");  // currency
      p6  = req.getParameter("p6");  // price
      p9  = req.getParameter("p9");  // remoteSID
      p14 = req.getParameter("p14"); // userType 
 
      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, p3, p4, p5, p6, p9, p14, bytesOut);
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

      System.out.println("2000: " + e);
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "StockEnquiryRemote", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2000, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                    String p1, String p2, String p3, String p4, String p5, String p6, String p9, String p14, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null, stmt2 = null;
    ResultSet rs = null, rs2 = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 2000, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "StockEnquiryRemote", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2000, bytesOut[0], 0, "ACC:" + p1);
      if(con != null) con.close();  
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "StockEnquiryRemote", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2000, bytesOut[0], 0, "SID:" + p1);
      if(con != null) con.close();  
      if(out != null) out.flush(); 
      return;
    }

    String[] pricingURL    = new String[1];
    String[] pricingUpline = new String[1];
    String[] catalogURL    = new String[1];
    String[] catalogUpline = new String[1];
    String[] userName      = new String[1];
    String[] passWord      = new String[1];

    getCatalogDetails(con, stmt, rs, p1, pricingUpline, pricingURL, catalogURL, catalogUpline, userName, passWord);
    
    set(con, stmt, stmt2, rs, rs2, out, req, p1, p2, p3, p4, p5, p6, p9, pricingURL[0], pricingUpline[0], catalogURL[0], catalogUpline[0], p14,
        userName[0], passWord[0], unm, sid, uty, men, den, dnm, bnm, imagesDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 2000, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con != null) con.close();  
    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, HttpServletRequest req,
                   String mfr, String mfrCode, String itemCode, String category, String currency, String price, String remoteSID, String pricingURL,
                   String pricingUpline, String catalogURL, String catalogUpline, String userType, String userName, String passWord, String unm,
                   String sid, String uty, String men, String den, String dnm, String bnm, String imagesDir, String localDefnsDir, String defnsDir,
                   int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Stock Enquiry</title>");

    scoutln(out, bytesOut, "<script language='JavaScript'>");

    String[] customerCode = new String[1];
    
    int i = unm.indexOf("_");

    if(uty.equals("R"))
    {

      if(! profile.getExternalAccessNameCustomerCode(con, stmt, rs, unm.substring(0, i), dnm, localDefnsDir, defnsDir, customerCode))
        customerCode[0] = "";
    
      String[] name         = new String[1];
      String[] companyName  = new String[1];
      String[] accessRights = new String[1];
  
      profile.getExternalAccessNameCompanyAndRights(con, stmt, rs, unm.substring(0, i), name, companyName, accessRights);

      if(accessRights[0].equals("sales") || accessRights[0].equals("accounts"))
      {
        scoutln(out, bytesOut, "function viewSO(code){var p1=sanitise(code);");
        scoutln(out, bytesOut, "this.location.href=\"/central/servlet/SalesOrderRegisteredUser?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den
                             + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
      }
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 121, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function addToCart(descs){var p5=sanitise(descs);");
      if(!uty.equals("I"))scoutln(out, bytesOut, "alert('Coming Soon');else ");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/ProductCartAddToCart?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den="
                          + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p7=" + generalUtils.sanitise(mfr) + "&p8=" + generalUtils.sanitise2(mfrCode) + "&p2=R&p3="
                          + price + "&p4=" + currency + "&p5=Each&p6=\"+descs+\"&p9=" + remoteSID + "&p1=-\";}");
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
 
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "2000", "", "StockEnquiryRemote", unm, sid, uty, men, den, dnm, bnm, localDefnsDir,
                          defnsDir, hmenuCount, bytesOut);
  
    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Stock Enquiry", "2000", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<form><table id='page' width=100% border=0>");

    String[] title = new String[1];
    String[] desc  = new String[1];
    String[] desc2 = new String[1];
    
    getTitleAndDescsFromCatalogServer(unm, uty, bnm, mfr, mfrCode, category, catalogURL, catalogUpline, title, desc, desc2);

    scoutln(out, bytesOut, "<tr><td nowrap><p>Manufacturer: </td><td nowrap width='90%'><p><b>" + mfr + "</b></td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap><p>Manufacturer Code: </td><td nowrap><p><b>" + mfrCode + "</b></td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap><p>Description: </td><td nowrap><p><b>" + title[0] + "</b></td></tr>");
    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><b>" + desc[0] + "</b></td></tr>");
    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><b>" + desc2[0] + "</b></td></tr>");

    char dpOnQuantities = miscDefinitions.dpOnQuantities(con, stmt, rs, "5");

    scoutln(out, bytesOut, "</td></tr>");
 
    String stockLevel;
    if(i != -1)
    {
      stockLevel = getAvailabilityFromPricingServer(unm.substring(0, i), uty, bnm, itemCode, remoteSID, pricingURL, pricingUpline, userType,
                                                    userName, passWord);
    }
    else
    {
      stockLevel = getAvailabilityFromPricingServer(unm, uty, bnm, itemCode, remoteSID, pricingURL, pricingUpline, userType, userName, passWord);
    }

    if(generalUtils.doubleFromStr(stockLevel) <= 0.0)
      scoutln(out, bytesOut, "<tr><td nowrap><p>Availability: </td><td nowrap><p><b>Call for availability</b></td></tr>");
    else scoutln(out, bytesOut, "<tr><td nowrap><p>Availability: </td><td nowrap><p><b>" + generalUtils.doubleDPs(stockLevel, dpOnQuantities)
                              + "</b></td></tr>");

    if(! uty.equals("A"))
    {
      // from this point on, store in string 'html' so that we can total and output totals before the details

      String[] html = new String[1];
      html[0] = "";

      double[] total = new double[1];  total[0] = 0.0;
      byte[] b = new byte[50];

      onSO(con, stmt, stmt2, rs, rs2, mfr, mfrCode, customerCode[0], dpOnQuantities, total, html);
      generalUtils.doubleToBytesCharFormat(total[0], b, 0);
      generalUtils.bytesDPsGivenSeparator(true, '\000', dpOnQuantities, b, 20, 0);
      generalUtils.formatNumeric(b, dpOnQuantities);
      scoutln(out, bytesOut, "<tr><td nowrap><p>Total you have on order from us:</td><td><span id='textNumericValue'>"
                             + generalUtils.stringFromBytes(b, 0L) + "</span></td></tr>\n");

      scoutln(out, bytesOut, "</table><table id=\"page\" width=100%>");
      scoutln(out, bytesOut, "<tr><td colspan=7><hr></td></tr>");

      scoutln(out, bytesOut, html[0]);

      if(authenticationUtils.verifyAccess(con, stmt, rs, req, 121, unm, uty, dnm, localDefnsDir, defnsDir))
      {
        String descs = desc[0] + " " + desc2[0];
        if(descs.length() == 1)
          descs = title[0];
        scoutln(out, bytesOut, "<tr><td nowrap><p><a href=\"javascript:addToCart('" + generalUtils.sanitise2(descs)
                             + "')\"><img border=0 src=\"" + imagesDir + "toCart.png\"></a></td></tr>\n");
      }      
    }
    else
    {
      scoutln(out, bytesOut, "</table><table id='page' width=100%>");
      scoutln(out, bytesOut, "<tr><td colspan=7><hr></td></tr>");
      if(authenticationUtils.verifyAccess(con, stmt, rs, req, 121, unm, uty, dnm, localDefnsDir, defnsDir))
      {
        scoutln(out, bytesOut, "<tr><td nowrap><p><a href=\"javascript:addToCart('" + generalUtils.sanitise2(desc[0] + " " + desc2[0])
                             + "')\"><img border=0 src=\"" + imagesDir + "toCart.png\"></a></td></tr>\n");
      }
    }

    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void onSO(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String mfr, String mfrCode, String customerCode,
                    char dpOnQuantities, double[] total, String[] html) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.SOCode, t2.Line, t2.Quantity, t2.DeliveryDate, t1.Date, t1.CustomerPOCode FROM sol AS t2 INNER JOIN so AS t1 "
                         + "ON t2.SOCode = t1.SOCode WHERE t1.CompanyCode = '" + customerCode + "' AND t2.Manufacturer = '"
                         + generalUtils.sanitiseForSQL(mfr) + "' AND ManufacturerCode = '" + mfrCode + "' AND t1.Status != 'C' "
                         + "ORDER BY t1.Date, t2.SOCode, t2.Line");

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
        html[0] += "<tr id=\"pageColumn\"><td colspan=7><p>Previous Sales Orders for: " + mfr + " " + mfrCode + "</td></tr>";
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
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // res: availability
  private String getAvailabilityFromPricingServer(String unm, String uty, String bnm, String itemCode, String remoteSID, String pricingURL,
                                                  String pricingUpline, String userType, String userName, String passWord) throws Exception
  { 
    if(! pricingURL.startsWith("http://"))
        pricingURL = "http://" + pricingURL;

    URL url = new URL(pricingURL + "/central/servlet/CatalogsAvailabilityInfo");

    HttpURLConnection uc = (HttpURLConnection)url.openConnection();
    uc.setDoInput(true);
    uc.setDoOutput(true);
    uc.setUseCaches(false);
    uc.setDefaultUseCaches(false);
    uc.setRequestProperty("Content-Type", "application/x-www-form.urlencoded");

    String s2 = "&unm=" + unm + "&uty=" + uty + "&bnm=" + bnm + "&p1=" + itemCode + "&p9=" + remoteSID + "&p12=" + pricingURL + "&p13="
              + pricingUpline + "&p14=" + userType + "&p15=" + userName + "&p16=" + passWord;

    Integer len = new Integer(s2.length());
    uc.setRequestProperty("Content-Length", len.toString());
    
    uc.setRequestMethod("POST");

    PrintWriter p = new PrintWriter(uc.getOutputStream());
    p.print(s2);    
    p.flush();
    p.close();

    BufferedReader di = new BufferedReader(new InputStreamReader(uc.getInputStream()));
    String s = di.readLine();

    if(s == null) // just-in-case
      s = "0";

    di.close();
    
    return s;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void getTitleAndDescsFromCatalogServer(String unm, String uty, String bnm, String mfr, String mfrCode, String category, String catalogURL,
                                                 String catalogUpline, String[] title, String[] desc, String[] desc2) throws Exception
  { 
    title[0] = desc[0] = desc2[0] = "";
    
    if(! catalogURL.startsWith("http://"))
        catalogURL = "http://" + catalogURL;

    URL url = new URL(catalogURL + "/central/servlet/CatalogSteelclawsTitle");
 
    HttpURLConnection uc = (HttpURLConnection)url.openConnection();
    uc.setDoInput(true);
    uc.setDoOutput(true);
    uc.setUseCaches(false);
    uc.setDefaultUseCaches(false);
    uc.setRequestProperty("Content-Type", "application/x-www-form.urlencoded");

    String s2 = "&unm=" + unm + "&uty=" + uty + "&bnm=" + bnm + "&p1=" + generalUtils.sanitise(mfr) + "&p2=" + generalUtils.sanitise(mfrCode) + "&p3=" + category
              + "&p11=" + catalogUpline;

    Integer len = new Integer(s2.length());
    uc.setRequestProperty("Content-Length", len.toString());
    
    uc.setRequestMethod("POST");

    PrintWriter p = new PrintWriter(uc.getOutputStream());
    p.print(s2);    
    p.flush();
    p.close();

    BufferedReader di = new BufferedReader(new InputStreamReader(uc.getInputStream()));
    String s = di.readLine();

    if(s != null) // just-in-case
    {
      int x = 0;
      len = s.length();
      while(x < len && s.charAt(x) != '\001') // just-in-case
        title[0] += s.charAt(x++);
      ++x;
      
      while(x < len && s.charAt(x) != '\001') // just-in-case
        desc[0] += s.charAt(x++);
      
      ++x;
      
      while(x < len && s.charAt(x) != '\001') // just-in-case
        desc2[0] += s.charAt(x++);
    }

    di.close();
  }

  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void getCatalogDetails(Connection con, Statement stmt, ResultSet rs, String mfr, String[] pricingUpline, String[] pricingURL,
                                 String[] catalogURL, String[] catalogUpline, String[] userName, String[] passWord) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT PricingURL, UserName, PassWord, PricingUpline, CatalogURL, CatalogUpline FROM linkedcat WHERE Manufacturer = '"
                         + generalUtils.sanitiseForSQL(mfr) + "'");
    
    if(rs.next())
    {
      pricingURL[0]    = rs.getString(1);
      userName[0]      = rs.getString(2);
      passWord[0]      = rs.getString(3);
      pricingUpline[0] = rs.getString(4);
      catalogURL[0]    = rs.getString(5);
      catalogUpline[0] = rs.getString(6);
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
