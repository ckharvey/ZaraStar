// =======================================================================================================================================================================================================
// System: ZaraStar Product: Display linked listing subsequent page
// Module: CatalogDisplayLinkedListingPage.java
// Author: C.K.Harvey
// Copyright (c) 2002-08 Christopher Harvey. All Rights Reserved.
// Remark: On caller server
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

public class CatalogDisplayLinkedListingPage extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  Profile profile = new Profile();
  
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out = null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", uty="", sid="", men="", den="", dnm="", bnm="", p31="", p32="", p33="", p34="", p35="", p36="", p37="", p38="", p39="", p40="",
           p9="", p10="", p11="", p12="", p13="", p14="", p15="", p16="", p17="", p18="", p19="", p20="", p21="", p22="", p23="", p24="";

    try
    {
      directoryUtils.setContentHeaders(res);
      out = res.getWriter();

      unm = req.getParameter("unm");
      uty = req.getParameter("uty");
      sid = req.getParameter("sid");
      dnm = req.getParameter("dnm");
      men = req.getParameter("men");
      den = req.getParameter("den");
      bnm = req.getParameter("bnm");
      p31 = req.getParameter("p31"); // mfr
      p32 = req.getParameter("p32"); // operation
      p33 = req.getParameter("p33"); // srchStr
      p34 = req.getParameter("p34"); // firstNum
      p35 = req.getParameter("p35"); // lastNum
      p36 = req.getParameter("p36"); // maxRows
      p37 = req.getParameter("p37"); // numRecs
      p38 = req.getParameter("p38"); // firstCode
      p39 = req.getParameter("p39"); // lastCode
      p40 = req.getParameter("p40"); // searchType
      
      p9  = req.getParameter("p9");  // remoteSID
      p10 = req.getParameter("p10"); // catalogURL
      p11 = req.getParameter("p11"); // catalogUpline
      p12 = req.getParameter("p12"); // pricingURL
      p13 = req.getParameter("p13"); // pricingUpline
      p14 = req.getParameter("p14"); // userType
      p15 = req.getParameter("p15"); // userName
      p16 = req.getParameter("p16"); // passWord
      p17 = req.getParameter("p17"); // band
      p18 = req.getParameter("p18"); // markup
      p19 = req.getParameter("p19"); // discount1
      p20 = req.getParameter("p20"); // discount2
      p21 = req.getParameter("p21"); // discount3
      p22 = req.getParameter("p22"); // discount4
      p23 = req.getParameter("p23"); // catalogCurrency
      p24 = req.getParameter("p24"); // priceBasis

      doIt(out, req, unm, uty, sid, men, den, dnm, bnm, p31, p32, p33, p34, p35, p36, p37, p38, p39, p40, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18, p19, p20, p21, p22, p23, p24, bytesOut);
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
 
      messagePage.errorPage(out, req, e, "Communications Error", unm, "", dnm, "", urlBit, "", "", "", "CatalogDisplayLinkedListingPage", bytesOut);
      try
      {
        scoutln(out, bytesOut, "ERR:2005i     " + e);
      }
      catch (Exception e2) { } 
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String uty, String sid, String men, String den, String dnm, String bnm,
                    String p31, String p32, String p33, String p34, String p35, String p36, String p37, String p38, String p39, String p40, String p9,
                    String p10, String p11, String p12, String p13, String p14, String p15, String p16, String p17, String p18, String p19,
                    String p20, String p21, String p22, String p23, String p24, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;
    
    String canSeeCostPrice, canSeeRRPPrice;
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 2007, unm, uty, dnm, localDefnsDir, defnsDir))
      canSeeCostPrice = "Y";
    else canSeeCostPrice = "N";

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 2009, unm, uty, dnm, localDefnsDir, defnsDir))
      canSeeRRPPrice = "Y";
    else canSeeRRPPrice = "N";

    showPage(con, stmt, rs, out, req, p31, p32, p33, p34, p35, p36, p37, p38, p39, p40, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18, p19, p20,
             p21, p22, p23, p24, canSeeCostPrice, canSeeRRPPrice, unm, uty, sid, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 2005, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p31);
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void showPage(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String p31, String p32, String p33,
                        String p34, String p35, String p36, String p37, String p38, String p39, String p40, String p9, String p10, String p11,
                        String p12, String p13, String p14, String p15, String p16, String p17, String p18, String p19, String p20, String p21,
                        String p22, String p23, String p24, String p25, String p26, String unm, String uty, String sid, String men, String den,
                        String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Catalog Page</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    scoutln(out, bytesOut, "function page2005i(mfr,operation,firstNum,lastNum,firstCode,lastCode,topOrBottom,numRecs,sid){");
    scoutln(out, bytesOut, "var srchStr='';if(topOrBottom=='T')srchStr=document.forms[0].srchStr1.value;");
    scoutln(out, bytesOut, "else srchStr=document.forms[0].srchStr2.value;");
//    scoutln(out, bytesOut, "var maxRows;if(topOrBottom=='T')maxRows=document.forms[0].maxRows1.value;");
//    scoutln(out, bytesOut, "else maxRows=document.forms[0].maxRows2.value;");    
    scoutln(out, bytesOut, "var firstCode1=sanitise(firstCode);var lastCode1=sanitise(lastCode);");
    scoutln(out, bytesOut, "var searchType='C';if(topOrBottom=='T'){if(document.forms[0].searchType1[0].checked)searchType='D';}");
    scoutln(out, bytesOut, "else {if(exists())if(document.forms[0].searchType2[0].checked)searchType='D';}");
    scoutln(out, bytesOut, "function exists(){var e=document.getElementsByTagName('input');");
    scoutln(out, bytesOut, "for(var i=0;i<e.length;i++){if(e[i].type==\"radio\")if(e[i].name==\"searchType2\")return true;}return false;}");
    scoutln(out, bytesOut, "window.location.replace(\"/central/servlet/CatalogDisplayLinkedListingPage?unm=" + unm + "&sid="+sid+"&uty=" + uty + "&men=" + men + "&den="
                          + den + "&dnm=" + dnm + "&p9=" + p9 + "&p10=" + p10 + "&p14=" + p14 + "&p15=" + p15 + "&p16=" + p16 + "&p17=" + p17
                          + "&p18=" + p18 + "&p19=" + p19 + "&p20=" + p20 + "&p21=" + p21 + "&p22=" + p22 + "&p23=" + p23 + "&p24=" + p24 + "&dnm="
                          + dnm + "&p11=" + p11 + "&bnm=" + bnm
                          + "&p31=\"+mfr+\"&p32=\"+operation+\"&p33=\"+srchStr+\"&p34=\"+firstNum+\"&p35=\"+lastNum+"
                          + "\"&p36=50&p37=\"+numRecs+\"&p38=\"+firstCode+\"&p40=\"+searchType+\"&p39=\"+lastCode);}");
    
    scoutln(out, bytesOut, "function s2000b(mfrCode,itemCode,currency,price,remoteSID){");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/StockEnquiryRemote?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den
                         + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + generalUtils.sanitise(p31) + "&p2=\"+mfrCode+\"&p4=\"+currency+\"&p5=\"+price+\"&p9="
                         + p9 + "&p14=" + p14 + "&p3=\"+itemCode;}");

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

    scoutln(out, bytesOut, "function addToCart(code,price,curr,mfrCode,uom,descs,remoteSID){var p1=sanitise(code),p5=sanitise(descs);");
    if(!uty.equals("I"))scoutln(out, bytesOut, "alert('Coming Soon');else ");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/ProductCartAddToCart?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den
                         + "&dnm=" + dnm + "&bnm=" + bnm + "&p7=" + generalUtils.sanitise(p31)
                         + "&p8=\"+mfrCode+\"&p2=R&p3=\"+price+\"&p4=\"+curr+\"&p5=\"+uom+\"&p6=\"+descs+\"&p9=\"+remoteSID+\"&p1=\"+p1;}");

    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\""
                         + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\"></head>");
  
    int[] hmenuCount = new int[1];
 
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "2005", "", "CatalogFetchLinked", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount,
                          bytesOut);

    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", p31 + " Catalog", "2005", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    set(out, p31, p32, p33, p34, p35, p36, p37, p38, p39, p40, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18, p19, p20, p21, p22, p23, p24, p25,
        p26, unm, uty, sid, men, den, dnm, bnm, bytesOut);
    
    scoutln(out, bytesOut, "</div></body></html>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(PrintWriter out, String p31, String p32, String p33, String p34, String p35, String p36, String p37, String p38, String p39,
                   String p40, String p9, String p10, String p11, String p12, String p13, String p14, String p15, String p16, String p17, String p18,
                   String p19, String p20, String p21, String p22, String p23, String p24, String p25, String p26, String unm, String uty, String sid,
                   String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
  URL url = new URL(p10 + "/central/servlet/CatalogZaraListingLinkedInUser?unm=" + unm + "&sid=" + sid + "&uty=" + uty
                    + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p31=" + p31 + "&p32=" + p32 + "&p33=" + generalUtils.sanitise(p33) + "&p34=" + p34
                    + "&p35=" + p35 + "&p36=" + p36 + "&p37=" + p37 + "&p38=" + generalUtils.sanitise(p38) + "&p39=" + generalUtils.sanitise(p39) + "&p40=" + p40
                    + "&p9=" + p9 + "&p10=" + generalUtils.sanitise(p10) + "&p11=" + p11 + "&p12=" + p12 + "&p13=" + p13 + "&p14=" + p14 + "&p15=" + p15
                    + "&p16=" + p16 + "&p17=" + p17 + "&p18=" + p18 + "&p19=" + p19 + "&p20=" + p20 + "&p21=" + p21 + "&p22=" + p22 + "&p23=" + p23
                    + "&p24=" + p24 + "&p25=" + p25 + "&p26=" + p26 + "&bnm=" + bnm);
    
    URLConnection uc = url.openConnection();
    uc.setDoInput(true);
    uc.setUseCaches(false);
    uc.setDefaultUseCaches(false);

    uc.setRequestProperty("Content-Type", "application/octet-stream");

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
    bytesOut[0] += (str.length() + 1);    
  }

}
