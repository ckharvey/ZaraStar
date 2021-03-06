// =======================================================================================================================================================================================================
// System: ZaraStar Product: Display linked SC Index
// Module: CatalogSteelclawsDisplayIndex.java
// Author: C.K.Harvey
// Copyright (c) 2002-08 Christopher Harvey. All Rights Reserved.
// Remark: On calling server
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

public class CatalogSteelclawsDisplayIndex extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  ServerUtils serverUtils = new ServerUtils();
  MessagePage messagePage = new MessagePage();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p9="", p10="", p11="", p12="", p13="", p14="", p15="", p16="", p17="",
           p18="", p19="", p20="", p21="", p22="", p90="";

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
      p9  = req.getParameter("p9");  // remoteSID
      p10 = req.getParameter("p10"); // catalogURL
      p11 = req.getParameter("p11"); // catalogUpline
      p12 = req.getParameter("p12"); // pricingURL
      p13 = req.getParameter("p13"); // pricingUpline
      p14 = req.getParameter("p14"); // userType 
      p15 = req.getParameter("p15"); // userName
      p16 = req.getParameter("p16"); // passWord
      p17 = req.getParameter("p17"); // userBand
      p18 = req.getParameter("p18"); // markup
      p19 = req.getParameter("p19"); // discount1
      p20 = req.getParameter("p20"); // discount2
      p21 = req.getParameter("p21"); // discount3
      p22 = req.getParameter("p22"); // discount4

      p90 = req.getParameter("p90"); // whichIndex

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18, p19, p20, p21, p22, p90, bytesOut);
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

      System.out.println("2005o: " + e);
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "CatalogFetchLinked", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2005, bytesOut[0], 0, "ERR:2005o" + p1);
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                    String p1, String p9, String p10, String p11, String p12, String p13, String p14, String p15, String p16, String p17, String p18,
                    String p19, String p20, String p21, String p22, String p90, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;
    
    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "CatalogFetchLinked", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2005, bytesOut[0], 0, "ERRSID2005o:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }
    
    String[] catalogUpline = new String[1];
    String[] pricingUpline = new String[1];
    String[] catalogURL = new String[1];
    String[] pricingURL = new String[1];
    String[] userName   = new String[1];
    String[] passWord   = new String[1];
    String[] markup     = new String[1];
    String[] discount1  = new String[1];
    String[] discount2  = new String[1];
    String[] discount3  = new String[1];
    String[] discount4  = new String[1];
    String[] catalogCurrency = new String[1];
    String[] priceBasis      = new String[1];

    getCatalogDetails(con, stmt, rs, p1, catalogUpline, pricingUpline, catalogURL, pricingURL, userName, passWord, markup, discount1, discount2,
                      discount3, discount4, catalogCurrency, priceBasis);
    if(! catalogURL[0].startsWith("http://"))
      catalogURL[0] = "http://" + catalogURL[0];

    scoutln(out, bytesOut, "<html><head><title>Index Page</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

      scoutln(out, bytesOut, "function addToCart(code,price,curr,mfrCode,uom,descs,remoteSID){var p1=sanitise(code),p5=sanitise(descs);");
       if(!uty.equals("I"))scoutln(out, bytesOut, "alert('Coming Soon');else ");
     scoutln(out, bytesOut, "this.location.href=\"/central/servlet/ProductCartAddToCart?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den
                          + "&dnm=" + dnm + "&bnm=" + bnm + "&p7=" + generalUtils.sanitise(p1)
                          + "&p8=\"+mfrCode+\"&p2=R&p3=\"+price+\"&p4=\"+curr+\"&p5=\"+uom+\"&p6=\"+descs+\"&p9=\"+remoteSID+\"&p1=\"+p1;}");

    if(dnm.equals("Catalogs") && unm.equals("Sysadmin"))
    {
      scoutln(out, bytesOut, "function edit(which){");
      scoutln(out, bytesOut, "window.location.href=\"/central/servlet/CatalogsSteelclawsCategoryEdit?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den
                           + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + generalUtils.sanitise(p1) + "&p2=\"+which;}");
    }

    scoutln(out, bytesOut, "function contents(){");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/CatalogFetchLinked?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den
                         + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + generalUtils.sanitise(p1) + "&p2=C;\"}");
    
    scoutln(out, bytesOut, "function view(itemCode){");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/MainPageUtils1a?unm=" + unm + "&uty=" + uty + "&sid=" + sid + "&men=" + men + "&den=" + den
                         + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+sanitise(itemCode);}");
    
    scoutln(out, bytesOut, "function newProducts(remoteSID){");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/CatalogNewProducts?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den
                         + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + generalUtils.sanitise(p1) + "&p10=" + generalUtils.sanitise(p10) + "&p11=" + p11 + "&p12="
                         + generalUtils.sanitise(p12) + "&p13=" + p13 + "&p14=" + p14 + "&p15=" + p15 + "&p16=" + p16 + "&p17=" + p17 + "&p18=" + p18
                         + "&p19=" + p19 + "&p20=" + p20 + "&p21=" + p21 + "&p22=" + p22 + "&p23=" + catalogCurrency + "&p24=" + priceBasis
                         + "&p9=\"+remoteSID}");
    
    scoutln(out, bytesOut, "function search(remoteSID){");
    scoutln(out, bytesOut, "var p2=sanitise(document.forms[0].searchPhrase.value);");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/CatalogWordPhraseSearchSteelclaws?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den
                         + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + generalUtils.sanitise(p1) + "&p10=" + generalUtils.sanitise(p10) + "&p11=" + p11 + "&p12="
                         + generalUtils.sanitise(p12) + "&p13=" + p13 + "&p14=" + p14 + "&p15=" + p15 + "&p16=" + p16 + "&p17=" + p17 + "&p18=" + p18
                         + "&p19=" + p19 + "&p20=" + p20 + "&p21=" + p21 + "&p22=" + p22 + "&p23=" + catalogCurrency + "&p24=" + priceBasis
                         + "&p3=S&p4=0&p5=0&p6=0&p2=\"+p2+\"&p9=\"+remoteSID}");

    scoutln(out, bytesOut, "function page2005e(category,remoteSID,chapter,section,page){");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/CatalogDisplayLinkedPage?unm=" + unm + "&uty=" + uty + "&sid=" + sid + "&men=" + men + "&den=" + den
                         + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + generalUtils.sanitise(p1) + "&p17=" + p17 + "&p14=" + p14 //+ "&p13=" + pricingUpline[0]
                         + "&p2=\"+escape(category)+\"&p80=\"+escape(chapter)+\"&p81=\"+escape(section)+\"&p82=\"+escape(page)+\"&p9=\"+remoteSID;}");

    scoutln(out, bytesOut, "function indecatalogSteelclawsDisplayIndex(which,remoteSID){");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/CatalogSteelclawsDisplayIndex?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den
                         + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + generalUtils.sanitise(p1) + "&p9=\"+remoteSID+\"&p10=" + generalUtils.sanitise(p10) + "&p11="
                         + p11 + "&p12=" + generalUtils.sanitise(p12) + "&p13=" + p13 + "&p14=" + p14 + "&p15=" + p15 + "&p16=" + p16 + "&p17=" + p17
                         + "&p18=" + p18 + "&p19=" + p19 + "&p20=" + p20 + "&p21=" + p21 + "&p22=" + p22 + "&p23=" + catalogCurrency + "&p24="
                         + priceBasis + "&p90=\"+which;}");

    scoutln(out, bytesOut, "function s2000(mfrCode,itemCode,category,currency,price,remoteSID){");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/StockEnquiryRemote?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den
                         + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + p1 + "&p2=\"+mfrCode+\"&p4=\"+category+\"&p5=\"+currency+\"&p6=\"+price+\"&p9=\"+remoteSID+\"&p14=" + p14
                         + "&p3=\"+itemCode;}");


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

    scoutln(out, bytesOut, "function view(category,remoteSID){");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/CatalogDisplayLinkedPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den
                         + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + generalUtils.sanitise(p1) + "&p14=" + p14 + "&p17=" + p17 + "&p2=\"+category;}");

    scoutln(out, bytesOut, "function page2005n(which,mfr,operation,firstNum,lastNum,firstCode,lastCode,numRecs,remoteSID){");
    scoutln(out, bytesOut, "var firstCode1=sanitise(firstCode);var lastCode1=sanitise(lastCode);");
    scoutln(out, bytesOut, "window.location.replace(\"/central/servlet/CatalogSteelclawsSubsequent?unm=" + unm + "&sid="+sid+"&uty=" + uty + "&men=" + men
                          + "&den=" + den + "&dnm=" + dnm + "&p1=\"+which+\"&p9=\"+remoteSID+\"&p10=" + generalUtils.sanitise(catalogURL[0]) + "&p11="
                          + catalogUpline[0] + "&p12=" + generalUtils.sanitise(pricingURL[0]) + "&p13=" + pricingUpline[0] + "&p14=" + p14 + "&p15="
                          + userName[0] + "&p16=" + passWord[0] + "&p17=" + p17 + "&p18=" + markup[0] + "&p19=" + discount1[0] + "&p20="
                          + discount2[0] + "&p21=" + discount3[0] + "&p22=" + discount4[0] + "&p23=" + catalogCurrency[0] + "&p24=" + priceBasis[0]
                         + "&bnm=" + bnm + "&p31=\"+mfr+\"&p32=\"+operation+\"&p34=\"+firstNum+\"&p35=\"+lastNum+"
                          + "\"&p37=\"+numRecs+\"&p38=\"+firstCode+\"&p39=\"+lastCode);}");
    
    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\""
                         + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");
    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCatalogCssDirectory(p1) + "\">");
  
    int[] hmenuCount = new int[1];
 
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "2005", "", "CatalogSteelclawsDisplayIndex", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount,
                          bytesOut);

    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", p1 + " Index", "2005", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<div id='catalogPageFrame'>");

    getPage(out, p1, p90, p9, catalogUpline[0], catalogURL[0], p14, userName[0], unm, uty, bnm, bytesOut);    
  
    scoutln(out, bytesOut, "</div>");

    scoutln(out, bytesOut, "</div></body></html>");

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 2005, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con  != null) con.close();
    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void getCatalogDetails(Connection con, Statement stmt, ResultSet rs, String mfr, String[] catalogUpline, String[] pricingUpline,
                                 String[] catalogURL, String[] pricingURL, String[] userName, String[] passWord, String[] markup, String[] discount1,
                                 String[] discount2, String[] discount3, String[] discount4, String[] catalogCurrency, String[] priceBasis)
                                 throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT CatalogUpline, CatalogURL, PricingURL, UserName, PassWord, MarkupPercentage, DiscountPercentage1, "
                         + "DiscountPercentage2, DiscountPercentage3, DiscountPercentage4, PricingUpline, Currency, PriceBasis FROM linkedcat "
                         + " WHERE Manufacturer = '" + generalUtils.sanitiseForSQL(mfr) + "'");
    
    if(rs.next())
    {
      catalogUpline[0] = rs.getString(1);
      catalogURL[0]    = rs.getString(2);
      pricingURL[0]    = rs.getString(3);
      userName[0]      = rs.getString(4);
      passWord[0]      = rs.getString(5);
      markup[0]        = rs.getString(6);
      discount1[0]     = rs.getString(7);
      discount2[0]     = rs.getString(8);
      discount3[0]     = rs.getString(9);
      discount4[0]     = rs.getString(10);
      pricingUpline[0] = rs.getString(11);
      catalogCurrency[0] = rs.getString(12);
      priceBasis[0]      = rs.getString(13);
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void getPage(PrintWriter out, String mfr, String whichIndex, String remoteSID, String catalogUpline, String catalogURL, String userType,
                       String userName, String unm, String uty, String bnm, int[] bytesOut) throws Exception
  { 
    URL url = new URL(catalogURL + "/central/servlet/CatalogSteelclawsIndex");

    HttpURLConnection uc = (HttpURLConnection)url.openConnection();
    uc.setDoInput(true);
    uc.setDoOutput(true);
    uc.setUseCaches(false);
    uc.setDefaultUseCaches(false);
    uc.setRequestProperty("Content-Type", "application/x-www-form.urlencoded");

    String s2 = "&unm=" + unm + "&dnm=" + catalogUpline + "&uty=" + uty + "&bnm=" + bnm + "&p32=F&p31=" + mfr + "&p9=" + remoteSID + "&p10="
              + generalUtils.sanitise(catalogURL) + "&p11=" + catalogUpline + "&p14=" + userType + "&p15=" + userName + "&p1=" + whichIndex;

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
      scout(out, bytesOut, s);
      s = di.readLine();
    }

    di.close();
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
