// =======================================================================================================================================================================================================
// System: ZaraStar DocumentEngine: TNT create search results display page
// Module: TrackTraceSearchPage.java
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

public class TrackTraceSearchPage extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  DrawingUtils drawingUtils = new DrawingUtils();
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
      out = res.getWriter();
      directoryUtils.setContentHeaders(res);

      unm = req.getParameter("unm");
      sid = req.getParameter("sid");
      uty = req.getParameter("uty");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");
      p1  = req.getParameter("p1");  // operation
      p2  = req.getParameter("p2");  // gotoStr
      p3  = req.getParameter("p3");  // numPages
      p4  = req.getParameter("p4");  // numEntries
      p5  = req.getParameter("p5");  // currPage
      p6  = req.getParameter("p6");  // searchTime
      p7  = req.getParameter("p7");  // phrase

      if(p1  == null) p1  = "F";
      if(p2  == null) p2  = "";
      if(p3  == null) p3  = "1";
      if(p4  == null) p4  = "0";
      if(p5  == null) p5  = "1";
      if(p6  == null) p6  = "0";
      if(p7  == null) p7  = "";

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, p3, p4, p5, p6, p7, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "TrackTraceSearchPage", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2012, bytesOut[0], 0, "ERR:" + p7);
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                    String p1, String p2, String p3, String p4, String p5, String p6, String p7, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String workingDir       = directoryUtils.getUserDir('W', dnm, unm);
    String imagesDir     = directoryUtils.getSupportDirs('I');
    
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    
    Statement stmt = null;
    ResultSet rs   = null;

    if(! adminControlUtils.notDisabled(con, stmt, rs, 903))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "TrackTraceSearchPage", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2012, bytesOut[0], 0, "ACC:" + p7);
      
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "TrackTraceSearchPage", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2012, bytesOut[0], 0, "SID:" + p7);
      
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(p2 == null || p2.equalsIgnoreCase("null"))
      p2 = "";

    generate(con, stmt, rs, out, req, unm, sid, uty, men, den, dnm, bnm, p1.charAt(0), p2, p3, p4, p5, p6, p7, workingDir, localDefnsDir,
             defnsDir, imagesDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 2012, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p7);
      
      if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void generate(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty,
                        String men, String den, String dnm, String bnm, char operation, String gotoStr, String numPages, String numEntries,
                        String currPage, String searchTime, String phrase, String workingDir, String localDefnsDir, String defnsDir,
                        String imagesDir, int[] bytesOut) throws Exception
  {
    RandomAccessFile fh, fhi=null;
    boolean noResults = false;

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
          createPage(fh, fhi, html, operation, gotoStr, currPageI, numPagesI, unm, sid, uty, men, den, dnm, bnm);

          setHead(con, stmt, rs, out, req, unm, sid, uty, men, den, dnm, bnm, searchTime, phrase, numPages, numEntries, localDefnsDir,
                  defnsDir, bytesOut);

          setNav(out, true, currPageI[0], numPagesI, numPages, numEntriesI, numEntries, searchTime, imagesDir, bytesOut);

          startBody(true, out, bytesOut);

          scoutln(out, bytesOut, html[0]);

          startBody(false, out, bytesOut);

          setNav(out, false, currPageI[0], numPagesI, numPages, numEntriesI, numEntries, searchTime, imagesDir, bytesOut);

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
      setHead(con, stmt, rs, out, req, unm, sid, uty, men, den, dnm, bnm, "0.00", phrase, "0", "0", localDefnsDir, defnsDir, bytesOut);

      startBody(true, out, bytesOut);

      scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
      scoutln(out, bytesOut, "<tr><td><p><b>No Matches</i></td></tr>");
      scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr><tr><td>&nbsp;</td></tr>");

      startBody(false, out, bytesOut);

      scoutln(out, bytesOut, "</td></tr>");
    }

    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir));
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void createPage(RandomAccessFile fh, RandomAccessFile fhi, String[] html, char operation, String gotoStr, int[] currPage, int numPages,
                          String unm, String sid, String uty, String men, String den, String dnm, String bnm) throws Exception
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

    String type, docType, docCode, date, rest;
    int x, count=0;

    boolean quit = false;
    String s = fh.readLine();
    while(! quit && count < 20)
    {
      if(s == null)
        quit = true;
      else
      {
        x=0;
        type = "";
        while(x < s.length() && s.charAt(x) != ':')
          type += s.charAt(x++);

        ++x;
        docType = "";
        while(x < s.length() && s.charAt(x) != ':')
          docType += s.charAt(x++);

        ++x;
        docCode = "";
        while(x < s.length() && s.charAt(x) != ' ')
          docCode += s.charAt(x++);

        ++x;
        date = "";
        while(x < s.length() && s.charAt(x) != ':')
          date += s.charAt(x++);

        rest = "";
        if(x < s.length()) // just-in-case
        {
          ++x;
          while(x < s.length())
            rest += s.charAt(x++);
        }

        writeBodyLine(html, type, docType, docCode, date, rest, unm, sid, uty, men, den, dnm, bnm);

        ++count;
        if(count < 20)
          s = fh.readLine();
      }
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void setNav(PrintWriter out, boolean first, int currPageI, int numPagesI, String numPages, int numEntriesI, String numEntries,
                     String searchTime, String imagesDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<form><table id=\"pageColumn\" border=0 cellspacing=0 cellpadding=0 width=100%>");

    scoutln(out, bytesOut, "<tr><td height=20 nowrap><p>Results ");

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
      scoutln(out, bytesOut, "<a href=\"javascript:page('A','T')\">Goto Page</a>");
      scoutln(out, bytesOut, "&nbsp;<input type=text size=5 maxlength=5 value=\"" + nextJump + "\" name=gotoStr1>");
    }
    else
    {
      scoutln(out, bytesOut, "<a href=\"javascript:page('A','B')\">Goto Page</a>");
      scoutln(out, bytesOut, "&nbsp;<input type=text size=5 maxlength=5 value=\"" + nextJump + "\" name=gotoStr2>");
    }

    scoutln(out, bytesOut, "<img src=\"" + imagesDir + "d.gif\">");

    if(currPageI != 1)
    {
      scoutln(out, bytesOut, "&nbsp;&nbsp;<a href=\"javascript:page('F','1')\">First</a>");
      scoutln(out, bytesOut, "&nbsp;&nbsp;<a href=\"javascript:page('P','" + generalUtils.intToStr(currPageI) + "')\">Previous</a>");
    }
    
    if(currPageI < numPagesI)
    {
      scoutln(out, bytesOut, "&nbsp;&nbsp;<a href=\"javascript:page('N','" + generalUtils.intToStr(currPageI) + "')\">Next</a>");
      scoutln(out, bytesOut, "&nbsp;&nbsp;<a href=\"javascript:page('L','" + numPages + "')\">Last</a>");
    }

    scoutln(out, bytesOut, "</td></tr></table><table id=\"page\" border=0 width=100% cellspacing=0 cellpadding=0>");

    if(first)
      scoutln(out, bytesOut, "<tr><td><img src=\"" + imagesDir + "z402.gif\"></td></tr>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void setHead(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men,
                         String den, String dnm, String bnm, String searchTime, String phrase, String numPages, String numEntries,
                         String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Search Results</title>");
    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

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
    scoutln(out, bytesOut, "var gotoStr;if(operation=='A'){if(pageReqd=='T')gotoStr=document.forms[0].gotoStr1.value;");
    scoutln(out, bytesOut, "else gotoStr=document.forms[0].gotoStr2.value;}");

    scoutln(out, bytesOut, "this.location.replace(\"/central/servlet/TrackTraceSearchPage?unm=" + unm + "&sid=" + sid + "&uty="
                         + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                         + "&p1=\"+operation+\"&p2=\"+gotoStr+\"&p3=" + numPages + "&p4=" + numEntries + "&p5=\"+pageReqd+\"&p6="
                         + searchTime + "&p7=" + generalUtils.sanitise(phrase) + "\");");
    scoutln(out, bytesOut, "}");

    scoutln(out, bytesOut, "</script>");

    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" 
                         + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");
    
    int[] hmenuCount = new int[1];

    drawingUtils.outputPageFrame(con, stmt, rs, out, req, "2012", "", "TrackTraceSearchPage", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount, bytesOut);

    drawingUtils.drawTitle(out, false, false, "TrackTraceSearchPage", "", "Tranmsaction Track &amp; Trace Search Results", "2012", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void startBody(boolean first, PrintWriter out, int[] bytesOut) throws Exception
  {
    if(first)
      scoutln(out, bytesOut, "<table id=\"page\" border=0 cellspacing=0 cellpadding=0>");
    else
    {
      scoutln(out, bytesOut, "</table><table id=\"page\" border=0 cellspacing=0 cellpadding=0 width=100%>");
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void writeBodyLine(String[] html, String type, String docType, String docCode, String date, String rest, String unm, String sid, String uty,
                             String men, String den, String dnm, String bnm) throws Exception
  {
    if(type.equals("DOC"))
    {
      if(docType.equals("Sales Order"))
      {
        html[0] += "<tr><td height=18 nowrap><p><b>&nbsp;" + docType + "</b> ";
        html[0] += "<a href=\"/central/servlet/SalesOrderRegisteredUser?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm
                + "&bnm=" + bnm + "&p1=" + generalUtils.sanitise(docCode) + "\">";
        html[0] += docCode + "</a> dated " + generalUtils.convertFromYYYYMMDD(date);
        if(rest.length() > 0)
          html[0] += "<br>&nbsp;" + rest + "</td></tr>\n";
      }
      else
      if(docType.equals("Quotation"))
      {
        html[0] += "<tr><td height=18 nowrap><p><b>&nbsp;" + docType + "</b> ";
        html[0] += "<a href=\"/central/servlet/QuotationRegisteredUser?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm
                + "&bnm=" + bnm + "&p1=" + generalUtils.sanitise(docCode) + "\">";
        html[0] += docCode + "</a> dated " + generalUtils.convertFromYYYYMMDD(date);
        if(rest.length() > 0)
          html[0] += "<br>&nbsp;" + rest + "</td></tr>\n";
      }
      else
      if(docType.equals("Invoice"))
      {
        html[0] += "<tr><td height=18 nowrap><p><b>&nbsp;" + docType + "</b> ";
        html[0] += "<a href=\"/central/servlet/SalesInvoiceRegisteredUser?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm
                + "&bnm=" + bnm + "&p1=" + generalUtils.sanitise(docCode) + "\">";
        html[0] += docCode + "</a> dated " + generalUtils.convertFromYYYYMMDD(date);
        if(rest.length() > 0)
          html[0] += "<br>&nbsp;" + rest + "</td></tr>\n";
      }
      else
      if(docType.equals("DO"))
      {
        html[0] += "<tr><td height=18 nowrap><p><b>&nbsp;" + docType + "</b> ";
        html[0] += "<a href=\"/central/servlet/DeliveryOrdeRegisteredUser?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm
                + "&bnm=" + bnm + "&p1=" + generalUtils.sanitise(docCode) + "\">";
        html[0] += docCode + "</a> dated " + generalUtils.convertFromYYYYMMDD(date);
        if(rest.length() > 0)
          html[0] += "<br>&nbsp;" + rest + "</td></tr>\n";
      }
      else
      if(docType.equals("Proforma"))
      {
        html[0] += "<tr><td height=18 nowrap><p><b>&nbsp;" + docType + "</b> ";
        html[0] += "<a href=\"/central/servlet/ProformaInvoiceRegisteredUser?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm
                + "&bnm=" + bnm + "&p1=" + generalUtils.sanitise(docCode) + "\">";
        html[0] += docCode + "</a> dated " + generalUtils.convertFromYYYYMMDD(date);
        if(rest.length() > 0)
          html[0] += "<br>&nbsp;" + rest + "</td></tr>\n";
      }
    }

    html[0] += "<tr><td>&nbsp;</td></tr>\n";
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
