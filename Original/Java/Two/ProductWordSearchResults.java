// =======================================================================================================================================================================================================
// System: ZaraStar: Product: create search results display page
// Module: ProductWordSearchResults.java
// Author: C.K.Harvey
// Copyright (c) 2003-08 Christopher Harvey. All Rights Reserved.
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

public class ProductWordSearchResults extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  Inventory inventory = new Inventory();
  DrawingUtils drawingUtils = new DrawingUtils();
  InventoryAdjustment inventoryAdjustment = new InventoryAdjustment();
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

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="", p3="", p4="", p5="", p6="", p7="";

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
      p1  = req.getParameter("p1");  // operation
      p2  = req.getParameter("p2");  // gotoStr
      p3  = req.getParameter("p3");  // numPages
      p4  = req.getParameter("p4");  // numEntries
      p5  = req.getParameter("p5");  // currPage
      p6  = req.getParameter("p6");  // searchTime
      p7  = req.getParameter("p7");  // phrase

      if(p2 == null || p2.equalsIgnoreCase("null")) p2 = "";

      if(p5 == null || p5.equalsIgnoreCase("null")) p5 = "1";

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, p3, p4, p5, p6, p7, bytesOut);
    }
    catch(Exception e)
    {
      StringBuffer url = req.getRequestURL();
      int x=0;
      if(url.charAt(x) == 'h' || url.charAt(x) == 'H')
        x += 7;
      String urlBit="";
      while(x < url.length() && url.charAt(x) != ',' && url.charAt(x) != '/')
        urlBit += url.charAt(x++);

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "ProductWordSearchResults", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2002, bytesOut[0], 0, "ERR:" + p7);
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1, String p2, String p3, String p4, String p5, String p6, String p7, int[] bytesOut)
                    throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String workingDir       = directoryUtils.getUserDir('W', dnm, unm);
    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    Connection con = null;
    Statement stmt = null;
    ResultSet rs   = null;

    String uName = directoryUtils.getMySQLUserName();
    String pWord = directoryUtils.getMySQLPassWord();
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + uName + "&password=" + pWord);
    

    if((uty.equals("R") && ! adminControlUtils.notDisabled(con, stmt, rs, 902)) || (uty.equals("A") && ! adminControlUtils.notDisabled(con, stmt, rs, 802)) || (uty.equals("I") && ! authenticationUtils.verifyAccess(con, stmt, rs, req, 1001, unm, uty, dnm, localDefnsDir, defnsDir)))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "ProductWordSearchResults", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2002, bytesOut[0], 0, "ACC:" + p7);
      
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "ProductWordSearchResults", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2002, bytesOut[0], 0, "SID:" + p7);
      
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    String[] customerCode = new String[1];
    
    if(uty.equals("R"))
    {
      int i = unm.indexOf("_");
      
      profile.getExternalAccessNameCustomerCode(con, stmt, rs, unm.substring(0, i), dnm, localDefnsDir, defnsDir, customerCode);
    }
    
    generate(con, stmt, rs, out, req, unm, sid, uty, men, den, dnm, bnm, p1.charAt(0), p2, p3, p4, p5, p6, p7, customerCode[0], workingDir,
             localDefnsDir, defnsDir, imagesDir, bytesOut);
    
    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 2002, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p7);
      
      if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void generate(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty,
                        String men, String den, String dnm, String bnm, char operation, String gotoStr, String numPages, String numEntries,
                        String currPage, String searchTime, String phrase, String customerCode, String workingDir, String localDefnsDir,
                        String defnsDir, String imagesDir, int[] bytesOut) throws Exception
  {
    byte[][] sectionsList = new byte[1][1000]; sectionsList[0][0] = '\000';
    int[] sectionsListLen = new int[1];  sectionsListLen[0] = 1000;
              
    RandomAccessFile fh, fhi;
    boolean noResults = false;

    if(generalUtils.strToInt(numEntries) == 0)
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
          String[] html = new String[1]; html[0] = "";

          int numPagesI   = generalUtils.strToInt(numPages);

          int[] currPageI = new int[1];
          if(operation != 'A')
             currPageI[0] = generalUtils.strToInt(currPage);
          else
          {
            if(generalUtils.isInteger(gotoStr))
            {
              if(generalUtils.strToInt(gotoStr) > numPagesI)
              {
                currPageI[0] = numPagesI;
                gotoStr = numPages;
              }
              else currPageI[0] = generalUtils.strToInt(gotoStr);
            }
            else
            {
              currPageI[0] = 1;
              gotoStr = "1";
            }
          }

          int numEntriesI = generalUtils.strToInt(numEntries);
          createPage(con, stmt, rs, fh, fhi, html, operation, gotoStr, currPageI, numPagesI, customerCode, dnm, localDefnsDir, defnsDir,
                     sectionsList, sectionsListLen);

          setHead(con, stmt, rs, out, req, customerCode, unm, sid, uty, men, den, dnm, bnm, searchTime, phrase, numPages, numEntries, 
                  localDefnsDir, defnsDir, bytesOut);

          setNav(out, true, currPageI[0], numPagesI, numPages, phrase, numEntriesI, numEntries, searchTime, imagesDir, bytesOut);

          startBody(true, out, imagesDir, bytesOut);

          scoutln(out, bytesOut, html[0]);

          startBody(false, out, imagesDir, bytesOut);

          setNav(out, false, currPageI[0], numPagesI, numPages, phrase, numEntriesI, numEntries, searchTime, imagesDir, bytesOut);

          scoutln(out, bytesOut, "</td></tr>");

          generalUtils.fileClose(fhi);
        }
        else noResults = true;
      }

      generalUtils.fileClose(fh);
    }
    else noResults = true;

    if(noResults)
    {
      setHead(con, stmt, rs, out, req, customerCode, unm, sid, uty, men, den, dnm, bnm, "0.00", phrase, "0", "0", localDefnsDir, defnsDir, bytesOut);

      startBody(true, out, imagesDir, bytesOut);

      scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
      scoutln(out, bytesOut, "<tr><td><p><b>No Matches for <i>" + phrase + "</i></td></tr>");
      scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr><tr><td>&nbsp;</td></tr>");

      startBody(false, out, imagesDir, bytesOut);

      scoutln(out, bytesOut, "</td></tr>");
    }

    scoutln(out, bytesOut, "</table>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void createPage(Connection con, Statement stmt, ResultSet rs, RandomAccessFile fh, RandomAccessFile fhi, String[] html, char operation,
                          String gotoStr, int[] currPage, int numPages, String customerCode, String dnm, String localDefnsDir, String defnsDir,
                          byte[][] sectionsList, int[] sectionsListLen) throws Exception
  {
    switch(operation)
    {
      case 'F' : currPage[0] = 1;                       break;
      case 'L' : currPage[0] = numPages;                break;
      case 'N' : ++currPage[0];                         break;
      case 'P' : --currPage[0];                         break;
      case 'A' : currPage[0] = generalUtils.strToInt(gotoStr); break;
    }

    fhi.seek((currPage[0] - 1) * 4);
    fh.seek(fhi.readInt());

    String docType, docCode;
    int x, count=0;

    boolean quit = false;
    String s = fh.readLine();
    while(! quit && count < 20)
    {
      if(s == null)
        quit = true;
      else
      {
        docType = "";
        x=0;
        while(x < s.length() && s.charAt(x) != ' ')
          docType += s.charAt(x++);

        docCode = "";
        if(s.charAt(x) == ' ') // just-in-case
        {
          ++x;
          while(x < s.length())
            docCode += s.charAt(x++);
        }

        writeBodyLine(con, stmt, rs, html, docType, docCode, customerCode, dnm, localDefnsDir, defnsDir, sectionsList, sectionsListLen);

        ++count;
        if(count < 20)
          s = fh.readLine();
      }
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void setNav(PrintWriter out, boolean first, int currPageI, int numPagesI, String numPages, String phrase, int numEntriesI, String numEntries,
                     String searchTime, String imagesDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<table id=\"page\" border=0 cellspacing=0 cellpadding=0 width=100%>");

    if(first)
    {
      scoutln(out, bytesOut, "<tr><td><p><b>Search Results for <i>" + phrase + "</i></td></tr>");
      scoutln(out, bytesOut, "<tr><td colspan=2><img src=\"" + imagesDir + "blm2.gif\" width=100% height=3></td></tr>");
    }

    scoutln(out, bytesOut, "<tr><td height=20 nowrap bgcolor=\"#FFFFDD\"><p>Results ");

    int to = (currPageI * 20);
    if(to > numEntriesI)
      to = numEntriesI;

    scoutln(out, bytesOut, generalUtils.intToStr((currPageI * 20) - 19) + " to " + generalUtils.intToStr(to));
    scoutln(out, bytesOut, " of " + numEntries + " (Page " + currPageI + " of " + numPagesI + ")");
    scoutln(out, bytesOut, "<img src=\"" + imagesDir + "d.gif\">Search took " + searchTime + " seconds<br>");

    long nextJump = currPageI + ((numPagesI - currPageI) / 2);
    if(nextJump <= 0)
      nextJump = 1;

    if(first)
    {
      scoutln(out, bytesOut, "<a href=\"javascript:page('A','T')\">Goto Page</a><font color=\"#880000\">");
      scoutln(out, bytesOut, "&nbsp;<input type=text size=5 maxlength=5 value=\""+nextJump+"\" name=gotoStr1>");
    }
    else
    {
      scoutln(out, bytesOut, "<a href=\"javascript:page('A','B')\">Goto Page</a><font color=\"#880000\">");
      scoutln(out, bytesOut, "&nbsp;<input type=text size=5 maxlength=5 value=\""+nextJump+"\" name=gotoStr2>");
    }

    scoutln(out, bytesOut, "<img src=\"" + imagesDir + "d.gif\">");

    if(currPageI != 1)
    {
      scoutln(out, bytesOut, "&nbsp;&nbsp;<a href=\"javascript:page('F','1')\">First</a>");
      scoutln(out, bytesOut, "&nbsp;&nbsp;<a href=\"javascript:page('P','"+generalUtils.intToStr(currPageI)+"')\">Previous</a>");
    }
    
    if(currPageI < numPagesI)
    {
      scoutln(out, bytesOut, "&nbsp;&nbsp;<a href=\"javascript:page('N','"+generalUtils.intToStr(currPageI)+"')\">Next</a>");
      scoutln(out, bytesOut, "&nbsp;&nbsp;<a href=\"javascript:page('L','"+numPages+"')\">Last</a>");
    }

    scoutln(out, bytesOut, "</td></tr>");

    scoutln(out, bytesOut, "<table id=\"page\" border=0 width=100% cellspacing=0 cellpadding=0>");

    scoutln(out, bytesOut, "<tr><td colspan=9><img src=\"" + imagesDir + "blm2.gif\" width=100% height=3></td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr></table>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void setHead(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String customerCode, String unm,
                       String sid, String uty, String men, String den, String dnm, String bnm, String searchTime, String phrase, String numPages,
                       String numEntries, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Search Results</title>");
    
        scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\""
                         + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    
    scoutln(out, bytesOut, "<SCRIPT LANGUAGE=\"JavaScript\">");

    boolean regUser = false;
    if(uty.equals("R") && unm.endsWith("_"))
      regUser = true;
    boolean anonUser = false;
    if(uty.equals("A") && unm.startsWith("_"))
      anonUser = true;

    if(! regUser && ! anonUser)
    {
      scoutln(out, bytesOut, "function fetch(docType,code){");
      scoutln(out, bytesOut, "var code2=sanitise(code);");
      scoutln(out, bytesOut, "if(docType=='Stock')");
      scoutln(out, bytesOut, "window.location.href='/central/servlet/MainPageUtils1a?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&dnm=" + dnm + "&men="
                         + men + "&den=" + den + "&p1='+code2+'&bnm=" + bnm + "';");

      scoutln(out, bytesOut, "}");
    }
    else // regUser, or anonUser
    {
      scoutln(out, bytesOut, "function fetch(docType,code){");
      scoutln(out, bytesOut, "var code2=sanitise(code);");
      scoutln(out, bytesOut, "if(docType=='Stock')");
      scoutln(out, bytesOut, "window.location.href='/central/servlet/StockEnquiryExternal?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&dnm=" + dnm + "&men=" + men
                           + "&den=" + den + "&p1='+code2+'&bnm=" + bnm + "';");

      scoutln(out, bytesOut, "}");
    }

    scoutln(out, bytesOut, "function section(which){");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/CatalogBuyersPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den="
                         + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + customerCode + "&p2=\"+which;}");

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

    scoutln(out, bytesOut, "function page(operation,pageReqd){");
    scoutln(out, bytesOut, "var gotoStr=' ';if(operation=='A'){if(pageReqd=='T')gotoStr=document.forms[0].gotoStr1.value;");
    scoutln(out, bytesOut, "else gotoStr=document.forms[0].gotoStr2.value;}");

    scoutln(out, bytesOut, "this.location.replace(\"/central/servlet/ProductWordSearchResults?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den="
                         + den + "&dnm=" + dnm + "&bnm=" + bnm +"&p1=\"+operation+\"&p2=\"+gotoStr+\"&p3=" + numPages + "&p4=" + numEntries
                         + "&p5=\"+pageReqd+\"&p6=" + searchTime + "&p7=" + generalUtils.sanitise(phrase) + "\");}");

    scoutln(out, bytesOut, "</script>");

    int[] hmenuCount = new int[1];

    drawingUtils.outputPageFrame(con, stmt, rs, out, req, "2002", "", "ProductWordPhraseSearch", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount, bytesOut);

    drawingUtils.drawTitle(out, false, false, "ProductWordPhraseSearch", "", "Search Results", "2002", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void startBody(boolean first, PrintWriter out, String imagesDir, int[] bytesOut) throws Exception
  {
    if(first)
      scoutln(out, bytesOut, "<table id=\"page\" border=0 cellspacing=0 cellpadding=0>");
    else
    {
      scoutln(out, bytesOut, "</table><table id=\"page\" border=0 cellspacing=0 cellpadding=0 width=100%>");
      scoutln(out, bytesOut, "<tr><td colspan=9><img src=\"" + imagesDir + "blm2.gif\" width=100% height=3></td></tr>");
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // for docs and stock the format is:
  //   docType eg: Stock
  //   docCode eg: PRO1212
  // but for catalogs:
  //   docType eg: Catalog-Proto-1212
  //   docCode eg: itemDesc \003 catalogName \003 catalogDesc1
  private void writeBodyLine(Connection con, Statement stmt, ResultSet rs, String[] html, String docType, String docCode, String customerCode,
                             String dnm, String localDefnsDir, String defnsDir, byte[][] sectionsList, int[] sectionsListLen) throws Exception
  {
    String s = checkCode(docCode);
    byte[] docCodeB = new byte[101];
    generalUtils.strToBytes(docCodeB, docCode);

    byte[] dets = new byte[1000]; // plenty
    byte[] data = new byte[5000];

    if(docType.equals("Stock"))
    {
      getStockDetsGivenCode(con, stmt, rs, docCodeB, dets, dnm, localDefnsDir, defnsDir, data);

      html[0] += "<tr><td height=18 nowrap><p><b>Stock Item</b> - <a href=\"javascript:fetch('" + docType + "','" + s + "')\">" + docCode + "</a>&nbsp;</td></tr>\n";
      html[0] += "<tr><td height=18><p>" + generalUtils.stringFromBytes(dets, 0L) + "</td></tr>\n";
      html[0] += "<tr><td>&nbsp;</td></tr>\n";
    }
    else
    if(docType.equals("Buyers"))
    {
      String[] section     = new String[1];
      String[] sectionName = new String[1];
      getBuyersDetsGivenCode(con, stmt, rs, docCodeB, customerCode, section, sectionName);

      // chk section not already displayed
      byte[] sectionB = new byte[100];
      generalUtils.strToBytes(sectionB, section[0]);

      if(! generalUtils.chkList(sectionB, sectionsList[0]))
      {
        generalUtils.catAsBytes("\001\000", '\000', sectionB);

        sectionsList[0] = generalUtils.appendToList(false, sectionB, sectionsList[0], sectionsListLen);

        html[0] += "<tr><td height=18 nowrap><p><b>Section " + section[0] + "</b> - <a href=\"javascript:section('" + section[0] + "')\">"
                + sectionName[0] + "</a>&nbsp;</td></tr>\n";
        html[0] += "<tr><td>&nbsp;</td></tr>\n";
      }
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private String checkCode(String code) throws Exception
  {
    String s="";
    if(code.indexOf('\'') != -1)
    {
      int len = code.length();
      for(int x=0;x<len;++x)
      {
        if(code.charAt(x) == '\'')
          s += "\\'";
        else s += code.charAt(x);
      }
    }
    else s = code;
    return s;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void getStockDetsGivenCode(Connection con, Statement stmt, ResultSet rs, byte[] itemCode, byte[] dets, String dnm, String localDefnsDir, String defnsDir, byte[] data) throws Exception
  {
    if(itemCode[0] == '\000') // just-in-case
    {
      generalUtils.catAsBytes("Record Not Found", 0, dets, true);
      return;
    }

    if(inventory.getStockRecGivenCode(con, stmt, rs, itemCode, '\000', data) < 0)
    {
      generalUtils.catAsBytes("Record Not Found", 0, dets, true);
      return;
    }

    generalUtils.catAsBytes(generalUtils.dfsAsStr(data, (short)1) + " - ", 0, dets, true);
    generalUtils.catAsBytes(generalUtils.dfsAsStr(data, (short)2) + " (Brand: <i>", 0, dets, false);
    generalUtils.catAsBytes(generalUtils.dfsAsStr(data, (short)3) + "</i>, Manufacturer Code: <i>", 0, dets, false);
    
    String mfrCode = generalUtils.dfsAsStr(data, (short)4);
    if(mfrCode.length() == 0)
      mfrCode = "-";    
    generalUtils.catAsBytes(mfrCode + "</i>)", 0, dets, false);
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void getBuyersDetsGivenCode(Connection con, Statement stmt, ResultSet rs, byte[] itemCode, String customerCode, String [] section,
                                      String[] sectionName) throws Exception
  {
    if(itemCode[0] == '\000') // just-in-case
    {
      section[0]     = "0";
      sectionName[0] = "Not Found";
      return;
    }

    inventoryAdjustment.getSectionDetailsGivenService(con, stmt, rs, customerCode, generalUtils.stringFromBytes(itemCode, 0L), section, sectionName);
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }
  
}
