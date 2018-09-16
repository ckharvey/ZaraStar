// =======================================================================================================================================================================================================
// System: ZaraStar: Catalogs: Word Phrase Search of SC Catalog
// Module: CatalogWordPhraseSearchSteelclaws.java
// Author: C.K.Harvey
// Copyright (c) 2001-08 Christopher Harvey. All Rights Reserved.
// Where: Caller
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

public class CatalogWordPhraseSearchSteelclaws extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  Profile profile = new Profile();
  CatalogSteelclawsLinkedUser catalogSteelclawsLinkedUser = new CatalogSteelclawsLinkedUser();
  AdminControlUtils adminControlUtils = new AdminControlUtils();
  
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="", p3="", p4="", p5="", p6="", p7="", p8="", p9="", p10="", p11="",
           p12="", p13="", p14="", p15="", p16="", p17="", p18="", p19="", p20="", p21="", p22="", p23="", p24="", p70="";

    try
    {
      directoryUtils.setContentHeaders2(res);
      out = res.getWriter();

      unm = req.getParameter("unm");
      sid = req.getParameter("sid");      
      uty = req.getParameter("uty");
      dnm = req.getParameter("dnm");
      men = req.getParameter("men");
      den = req.getParameter("den");
      bnm = req.getParameter("bnm");
      p1  = req.getParameter("p1");  // mfr
      p2  = req.getParameter("p2");  // phrase
      p3  = req.getParameter("p3");  // search or display
      p4  = req.getParameter("p4");  // display page
      p5  = req.getParameter("p5");  // numPages
      p6  = req.getParameter("p6");  // numEntries
      p7  = req.getParameter("p7");  // searchTime
      p8  = req.getParameter("p8");  // operation

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
      p23 = req.getParameter("p23"); // catalogCurrency
      p24 = req.getParameter("p24"); // priceBasis
      
      p70 = req.getParameter("p70"); // currPage
      
      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18, p19, p20,
           p21, p22, p23, p24, p70, bytesOut);
    }
    catch(Exception e)
    {
      StringBuffer url = req.getRequestURL();
      String urlBit="";
      int x=0;
      if(url.charAt(x) == 'h' || url.charAt(x) == 'H')
        x += 7;
      while(url.charAt(x) != '\000' && url.charAt(x) != ',' && url.charAt(x) != '/')
        urlBit += url.charAt(x++);

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "CatalogWordPhraseSearchSteelclaws", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2003, bytesOut[0], 0, "ERR:" + p1);
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                    String p1, String p2, String p3, String p4, String p5, String p6, String p7, String p8, String p9, String p10, String p11,
                    String p12, String p13, String p14, String p15, String p16, String p17, String p18, String p19, String p20, String p21,
                    String p22, String p23, String p24, String p70, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String workingDir       = directoryUtils.getUserDir('W', dnm, unm);
    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesLibDir     = directoryUtils.getImagesDir(p11);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if((uty.equals("R") && ! adminControlUtils.notDisabled(con, stmt, rs, 902)) || (uty.equals("A") && ! adminControlUtils.notDisabled(con, stmt, rs, 802)) || (uty.equals("I") && ! authenticationUtils.verifyAccess(con, stmt, rs, req, 2003, unm, uty, dnm, localDefnsDir, defnsDir)))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "CatalogWordPhraseSearchSteelclaws", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2003, bytesOut[0], 0, "ACC:" + p1);
      if(con  != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "CatalogWordPhraseSearchSteelclaws", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2003, bytesOut[0], 0, "SID:" + p1);
      if(con  != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    int numPages;

    String canSeeCostPrice, canSeeRRPPrice;
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 2007, unm, uty, dnm, localDefnsDir, defnsDir))
      canSeeCostPrice = "Y";
    else canSeeCostPrice = "N";

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 2009, unm, uty, dnm, localDefnsDir, defnsDir))
      canSeeRRPPrice = "Y";
    else canSeeRRPPrice = "N";

    if(p3.equals("S")) // first time in... search
    {
      // create tmp searchresults file here on calling server
      RandomAccessFile fh = generalUtils.create(workingDir + "searchresults.txt");
      if((fh = generalUtils.fileOpenD("searchresults.txt", workingDir)) == null) // just-in-case
        return;

      RandomAccessFile fhi = generalUtils.create(workingDir + "resultsoffsets.inx");
      if((fhi = generalUtils.fileOpenD("resultsoffsets.inx", workingDir)) == null) // just-in-case
        return;

      int[]    numEntries = new int[1];    numEntries[0] = 0;
      double[] searchTime = new double[1]; searchTime[0]  = 0.0;
     
      numPages = search(fh, fhi, p1, p2, p10, p11, p15, unm, sid, uty, men, den, dnm, bnm, numEntries, searchTime);
       
      generalUtils.fileClose(fh);
      generalUtils.fileClose(fhi);

      String toEntryNum;
      if(numEntries[0] < 20)
        toEntryNum = generalUtils.intToStr(numEntries[0]);
      else toEntryNum = "20";
          
      displayPage(con, stmt, rs, out, req, p1, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18, p19, p20, p21, p22, p23, p24, canSeeCostPrice,
                  canSeeRRPPrice, unm, sid, uty, men, den, dnm, bnm, p2, "1", "1", toEntryNum, numPages, numEntries[0], searchTime[0], imagesDir,
                  imagesLibDir, workingDir, localDefnsDir, defnsDir, bytesOut);
    }
    else // display subsequent page
    {
      numPages          = generalUtils.intFromStr(p5);
      double searchTime = generalUtils.doubleFromStr(p7);
      int numEntries    = generalUtils.intFromStr(p6);
      
      int i = ((generalUtils.intFromStr(p70) - 1) * 20) + 1;
      int j = i + 20;
      if(j > numEntries)
        j = numEntries;      
      
      String fromEntryNum = generalUtils.intToStr(i);
      String toEntryNum   = generalUtils.intToStr(j);

      displayPage(con, stmt, rs, out, req, p1, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18, p19, p20, p21, p22, p23, p24, canSeeCostPrice,
                  canSeeRRPPrice, unm, sid, uty, men, den, dnm, bnm, p2, p70, fromEntryNum, toEntryNum, numPages, numEntries, searchTime, imagesDir,
                  imagesLibDir, workingDir, localDefnsDir, defnsDir, bytesOut);
    }

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 2003, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private int search(RandomAccessFile fh, RandomAccessFile fhi, String mfr, String phrase, String catalogURL, String catalogUpline, String userName,
                     String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] count, double[] time) throws Exception
  {
    if(! catalogURL.startsWith("http://"))
      catalogURL = "http://" + catalogURL;
    
    URL url = new URL(catalogURL + "/central/servlet/CatalogsSearchSteelclawsCatalog?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm="
                      + dnm + "&p1=" + generalUtils.sanitise(mfr) + "&p2=" + generalUtils.sanitise(phrase) + "&p11=" + catalogUpline + "&p15=" + userName + "&bnm="
                      + bnm);
                      
    URLConnection uc = url.openConnection();
    uc.setDoInput(true);
    uc.setUseCaches(false);
    uc.setDefaultUseCaches(false);

    uc.setRequestProperty("Content-Type", "application/octet-stream");

    BufferedReader di = new BufferedReader(new InputStreamReader(uc.getInputStream()));

    int c=0,/*count[0],*/ offset, numPages=0;
    String s = di.readLine();
    while(s != null)
    {
      if(s.startsWith("COUNTANDTIME"))
      {
        int x=13;
        int len = s.length();
        String t="";
        while(x < len && s.charAt(x) != ' ')
          t += s.charAt(x++);
        count[0] += generalUtils.strToInt(t);

        t="";
        ++x;
        while(x < len)
          t += s.charAt(x++);
        time[0] += generalUtils.doubleFromStr(t);
      }
      else
      {
        offset = (int)fh.getFilePointer();
        fh.writeBytes(s + "\n");
        fhi.writeInt(offset);

        ++numPages;

        ++c;
      }

      s = di.readLine();
    }

    di.close();

    return numPages;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void displayPage(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String mfr, String remoteSID,
                           String catalogURL, String catalogUpline, String pricingURL, String pricingUpline, String userType, String userName,
                           String passWord, String userBand, String markup, String discount1, String discount2, String discount3, String discount4,      
                           String catalogCurrency, String priceBasis, String canSeeCostPrice, String canSeeRRPPrice, String unm, String sid,
                           String uty, String men, String den, String dnm, String bnm, String phrase, String currPage, String fromEntryNum,
                           String toEntryNum, int numPages, int numEntries, double searchTime, String imagesDir, String imagesLibDir,
                           String workingDir, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    setHead(con, stmt, rs, out, req, mfr, remoteSID, catalogURL, catalogUpline, pricingURL, pricingUpline, userType, userName, passWord, userBand,
            markup, discount1, discount2, discount3, discount4, catalogCurrency, priceBasis, unm, sid, uty, men, den, dnm, bnm, phrase,
            localDefnsDir, defnsDir, bytesOut);

    scoutln(out, bytesOut, "<table id='catalogPage'>");
    String[] title = new String[1];
    String[] image = new String[1];
    getTitleAndImageNameFromCatalogServer(mfr, catalogURL, catalogUpline, title, image);
              
    scoutln(out, bytesOut, "<tr><td>");
    catalogSteelclawsLinkedUser.outputOptionsBar(out, mfr, remoteSID, catalogURL, title[0], image[0], imagesLibDir, bytesOut);
    scoutln(out, bytesOut, "</td></tr>");
    
    setNav(con, stmt, rs, out, true, mfr, remoteSID, catalogURL, catalogUpline, currPage, fromEntryNum, toEntryNum, numPages, numEntries, searchTime,
           imagesDir, imagesLibDir, defnsDir, bytesOut);
        
    try
    {
      RandomAccessFile fh, fhi;
      boolean noResults = false;
      String categories = "";

      if(numEntries == 0)
        noResults = true;
      else   
      if((fh = generalUtils.fileOpenD("searchresults.txt", workingDir)) != null)
      {
        if(fh.length() == 0)
          noResults = true;
        else
        {
          if((fhi = generalUtils.fileOpenD("resultsoffsets.inx", workingDir)) != null)
          {
            int currPageI = generalUtils.strToInt(currPage);
          
            fhi.seek((currPageI - 1) * 4);
            fh.seek(fhi.readInt());

            categories = fh.readLine();

            generalUtils.fileClose(fhi);
          }
        }
        
        generalUtils.fileClose(fh);
    
        displayCategories(out, mfr, remoteSID, categories, catalogURL, catalogUpline, pricingURL, pricingUpline, userType, userName, passWord,
                          userBand, markup, discount1, discount2, discount3, discount4, catalogCurrency, priceBasis, canSeeCostPrice, canSeeRRPPrice,
                          unm, uty, sid, men, den, dnm, bnm, bytesOut);
      }
    }
    catch(Exception e) { System.out.println(e); }
      
    setNav(con, stmt, rs, out, false, mfr, remoteSID, catalogURL, catalogUpline, currPage, fromEntryNum, toEntryNum, numPages, numEntries, searchTime, imagesDir, imagesLibDir, defnsDir, bytesOut);
 
    scoutln(out, bytesOut, "</table>");

    scoutln(out, bytesOut, "</div>");
    scoutln(out, bytesOut, "</form></div></body></html>");
  }
      
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void setNav(Connection con, Statement stmt, ResultSet rs, PrintWriter out, boolean first, String mfr, String remoteSID, String catalogURL,
                      String catalogUpline, String currPage, String fromEntryNum, String toEntryNum, int numPages, int numEntries, double searchTime,
                      String imagesDir, String imagesLibDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<tr><td colspan='2'><img src=\"" + imagesDir + "blm2.gif\" width='100%' height='3'/></td></tr>");
    
    scoutln(out, bytesOut, "<tr><td nowrap='nowrap'><p>Results ");

    scoutln(out, bytesOut, fromEntryNum + " to " + toEntryNum);
    scoutln(out, bytesOut, " of " + numEntries + " (Page " + currPage + " of " + numPages + ")");
    scoutln(out, bytesOut, "<img src=\"" + imagesDir + "d.gif\"/>Search took " + searchTime + " seconds<br>");

    if(! currPage.equals("1"))
    {
      scoutln(out, bytesOut, "&nbsp;&nbsp;<a href=\"javascript:more('F','" + numPages + "','1','" + numEntries + "','" + searchTime + "')\">First</a>");
      scoutln(out, bytesOut, "&nbsp;&nbsp;<a href=\"javascript:more('P','" + numPages + "','" + (generalUtils.intFromStr(currPage) - 1) + "','" + numEntries
                           + "','" + searchTime + "')\">Previous</a>");
    }
    
    if(generalUtils.intFromStr(currPage) < numPages)
    {
      scoutln(out, bytesOut, "&nbsp;&nbsp;<a href=\"javascript:more('N','" + numPages + "','" + (generalUtils.intFromStr(currPage) + 1) + "','" + numEntries
                           + "','" + searchTime + "')\">Next</a>");
      scoutln(out, bytesOut, "&nbsp;&nbsp;<a href=\"javascript:more('L','" + numPages + "','" + numPages + "','" + numEntries + "','" + searchTime
                           + "')\">Last</a>");
    }

    scoutln(out, bytesOut, "</td></tr>");

    scoutln(out, bytesOut, "<tr><td colspan='9'><img src=\"" + imagesDir + "blm2.gif\" width='100%' height='3'/></td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void setHead(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String p1, String p9, String p10,
                       String p11, String p12, String p13, String p14, String p15, String p16, String p17, String p18, String p19, String p20,
                       String p21, String p22, String p23, String p24, String unm, String sid, String uty, String men, String den, String dnm,
                       String bnm, String phrase, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Search Results</title>");
    scoutln(out, bytesOut, "<script language='JavaScript'>");

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
                         + "&p19=" + p19 + "&p20=" + p20 + "&p21=" + p21 + "&p22=" + p22 + "&p23=" + p23 + "&p24=" + p24 + "&p9=\"+remoteSID}");
    
    scoutln(out, bytesOut, "function search(remoteSID){");
    scoutln(out, bytesOut, "var p2=sanitise(document.forms[0].searchPhrase.value);");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/CatalogWordPhraseSearchSteelclaws?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den
                         + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + generalUtils.sanitise(p1) + "&p10=" + generalUtils.sanitise(p10) + "&p11=" + p11 + "&p12="
                         + generalUtils.sanitise(p12) + "&p13=" + p13 + "&p14=" + p14 + "&p15=" + p15 + "&p16=" + p16 + "&p17=" + p17 + "&p18=" + p18
                         + "&p19=" + p19 + "&p20=" + p20 + "&p21=" + p21 + "&p22=" + p22 + "&p23=" + p23 + "&p24=" + p24 + "&p3=S&p4=0&p5=0&p6=0&p2=\"+p2+\"&p9=\"+remoteSID}");

    scoutln(out, bytesOut, "function indecatalogSteelclawsDisplayIndex(which,remoteSID){");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/CatalogSteelclawsDisplayIndex?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den
                         + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + generalUtils.sanitise(p1) + "&p9=\"+remoteSID+\"&p10=" + generalUtils.sanitise(p10) + "&p11="
                         + p11 + "&p12=" + generalUtils.sanitise(p12) + "&p13=" + p13 + "&p14=" + p14 + "&p15=" + p15 + "&p16=" + p16 + "&p17=" + p17
                         + "&p18=" + p18 + "&p19=" + p19 + "&p20=" + p20 + "&p21=" + p21 + "&p22=" + p22 + "&p23=" + p23 + "&p24=" + p24
                         + "&p90=\"+which;}");
    
    scoutln(out, bytesOut, "function page2005e(category,remoteSID,chapter,section,page){");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/CatalogDisplayLinkedPage?unm=" + unm + "&uty=" + uty + "&sid=" + sid + "&men=" + men + "&den=" + den
                         + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + generalUtils.sanitise(p1) + "&p17=" + p17 + "&p14=" + p14 //+ "&p13=" + pricingUpline[0]
                         + "&p2=\"+escape(category)+\"&p80=\"+escape(chapter)+\"&p81=\"+escape(section)+\"&p82=\"+escape(page)+\"&p9=\"+remoteSID;}");

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

    scoutln(out, bytesOut, "function more(operation,numPages,currPage,numEntries,searchTime){");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/CatalogWordPhraseSearchSteelclaws?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den="
                         + den + "&dnm=" + dnm + "&bnm=" + bnm +"&p1=" + generalUtils.sanitise(p1) + "&p2=" + generalUtils.sanitise(phrase)
                         + "&p3=D&p4=0&p5=\"+numPages+\"&p6=\"+numEntries+\"&p7=\"+searchTime+\"&p8=\"+operation+\"&p70=\"+currPage+\"&p15=" + p15
                         + "&p9=" + p9 + "&p11=" + p11 + "&p11=" + p11 + "&p12=" + generalUtils.sanitise(p12) + "&p13=" + p13 + "&p14=" + p14 + "&p15=" + p15
                         + "&p16=" + p16 + "&p17=" + p17 + "&p18=" + p18 + "&p19=" + p19 + "&p20=" + p20 + "&p21=" + p21 + "&p22=" + p22 + "&p23="
                         + p23 + "&p24=" + p24 + "&p10=" + generalUtils.sanitise(p10) + "\";}");

    scoutln(out, bytesOut, "</script>");

    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\""
                         + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");
    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCatalogCssDirectory(p1) + "\">");

    int[] hmenuCount = new int[1];
 
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "2003", "", "ProductCatalogsAdmin", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount,
                          bytesOut);

    scoutln(out, bytesOut, "<form>");

    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Search Results for: " + phrase, "2003", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);
    
    scoutln(out, bytesOut, "<div id='catalogPageFrame'>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void displayCategories(PrintWriter out, String mfr, String remoteSID, String categories, String catalogURL, String catalogUpline,
                                 String pricingURL, String pricingUpline, String userType, String userName, String passWord, String userBand,
                                 String markup, String discount1, String discount2, String discount3, String discount4, String catalogCurrency,
                                 String priceBasis, String canSeeCostPrice, String canSeeRRPPrice, String unm, String uty, String sid, String men,
                                 String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    if(! catalogURL.startsWith("http://"))
      catalogURL = "http://" + catalogURL;

    URL url = new URL(catalogURL + "/central/servlet/CatalogDisplaySearchSteelcaws?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm="
                    + dnm + "&p1=" + generalUtils.sanitise(mfr) + "&p9=" + remoteSID + "&p10=" + generalUtils.sanitise(catalogURL) + "&p11=" + catalogUpline
                    + "&p12=" + generalUtils.sanitise(pricingURL) + "&p13=" + pricingUpline + "&p14=" + userType + "&p15=" + userName + "&p16=" + passWord
                    + "&p17=" + userBand + "&p18=" + markup + "&p19=" + discount1 + "&p20=" + discount2 + "&p21=" + discount3 + "&p22=" + discount4
                    + "&p23=" + catalogCurrency + "&p24=" + priceBasis + "&p25=" + canSeeCostPrice + "&p26=" + canSeeRRPPrice + "&p60="
                    + generalUtils.sanitise(categories) + "&bnm=" + bnm);
   
    URLConnection uc = url.openConnection();
    uc.setDoInput(true);
    uc.setUseCaches(false);
    uc.setDefaultUseCaches(false);

    uc.setRequestProperty("Content-Type", "application/octet-stream");

    BufferedReader di = new BufferedReader(new InputStreamReader(uc.getInputStream()));

    String s = di.readLine();

    while(s != null)
    {
      scoutln(out, bytesOut, s);
      s = di.readLine();
    }
    di.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void getTitleAndImageNameFromCatalogServer(String mfr, String catalogURL, String catalogUpline, String[] title, String[] image)
                                                     throws Exception
  {
    if(! catalogURL.startsWith("http://"))
      catalogURL = "http://" + catalogURL;

    URL url = new URL(catalogURL + "/central/servlet/CatalogFetchLinkedTitle?p1=" + generalUtils.sanitise(mfr) + "&p11=" + catalogUpline);
   
    URLConnection uc = url.openConnection();
    uc.setDoInput(true);
    uc.setUseCaches(false);
    uc.setDefaultUseCaches(false);

    uc.setRequestProperty("Content-Type", "application/octet-stream");

    BufferedReader di = new BufferedReader(new InputStreamReader(uc.getInputStream()));

    String s = di.readLine();

    title[0] = image[0] = "";
    
    if(s != null)
    {
      int x = 0, len = s.length();
      while(x < len && s.charAt(x) != '\001') // just-in-case
        title[0] += s.charAt(x++);
      ++x;
      while(x < len)
        image[0] += s.charAt(x++);
    }

    di.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
