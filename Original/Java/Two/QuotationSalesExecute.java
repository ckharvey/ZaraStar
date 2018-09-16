// =======================================================================================================================================================================================================
// System: ZaraStar: Quotation Processing - Sales
// Module: QuotationSalesExecute.java
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
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;
import java.io.*;

public class QuotationSalesExecute extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  MiscDefinitions miscDefinitions = new MiscDefinitions();
  DrawingUtils drawingUtils = new DrawingUtils();
  
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="";

    try
    {
      directoryUtils.setContentHeaders(res);
      out = res.getWriter();

      byte[] checkBoxes    = new byte[1000]; checkBoxes[0]    = '\000';
      int[]  checkBoxesLen = new int[1];     checkBoxesLen[0] = 1000;

      int thisEntryLen, inc;

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
        if(name.length() > 2 && name.substring(1, 3).equals(".x")) ;
        else
        if(name.length() > 2 && name.substring(1, 3).equals(".y")) ;
        else // must be checkbox value
        {
          thisEntryLen = name.length() + 2;
          if((generalUtils.lengthBytes(checkBoxes, 0) + thisEntryLen) >= checkBoxesLen[0])
          {
            byte[] tmp = new byte[checkBoxesLen[0]];
            System.arraycopy(checkBoxes, 0, tmp, 0, checkBoxesLen[0]);
            if(thisEntryLen > 1000)
              inc = thisEntryLen;
            else inc = 1000;
            checkBoxesLen[0] += inc;
            checkBoxes = new byte[checkBoxesLen[0]];
            System.arraycopy(tmp, 0, checkBoxes, 0, checkBoxesLen[0] - inc);
          }

          generalUtils.catAsBytes(name + "\001", 0, checkBoxes, false);
        }
      }

      doIt(out, req, checkBoxes, unm, sid, uty, men, den, dnm, bnm, bytesOut);
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
 
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "QuotationSales", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2029, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, byte[] checkBoxes, String unm, String sid, String uty, String men, String den,
                    String dnm, String bnm, int[] bytesOut) throws Exception
  {
    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    
    long startTime = new java.util.Date().getTime();

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 2029, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "QuotationSales", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2029, bytesOut[0], 0, "ACC:");
      
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "QuotationSales", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2029, bytesOut[0], 0, "SID:");
      
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    set(con, stmt, rs, out, req, checkBoxes, unm, sid, uty, men, den, dnm, bnm, imagesDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 2029, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
      
      if(con != null) con.close();
    if(out != null) out.flush(); 
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, byte[] checkBoxes, String unm, String sid, String uty, String men, String den, String dnm,
                   String bnm, String imagesDir, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Quotation Processing</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4019, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewQuote(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/QuotationPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men
                           + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4001, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewCust(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/CustomerPage?unm=" + unm + "&sid=" + sid + "&uty="
                           + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p1=\"+p1;}");
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

    drawingUtils.outputPageFrame(con, stmt, rs, out, req, "2029", "", "QuotationSalesa", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount, bytesOut);

    drawingUtils.drawTitle(out, false, false, "QuotationSalesa", "", "Quotation Processing - Sales", "2029", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<form><table id=\"page\" width=100% border=0>");

    scoutln(out, bytesOut, "<tr><td></td></tr><tr id=\"pageColumn\"><td></td>");

    scoutln(out, bytesOut, "<td><p><b> Quote Code </b></td>");
    scoutln(out, bytesOut, "<td><p><b> Quote Date </b></td>");
    scoutln(out, bytesOut, "<td align=center><p><b> Sent to Engineering </b></td>");
    scoutln(out, bytesOut, "<td align=center><p><b> Sent to Procurement</b></td>");
    scoutln(out, bytesOut, "<td align=center><p><b> Sent to Scheduling </b></td>");
    scoutln(out, bytesOut, "<td align=center><p><b> Manager Approval Requested </b></td>");
    scoutln(out, bytesOut, "<td align=center><p><b> Quotation Sent </b></td>");
    scoutln(out, bytesOut, "<td align=center><p><b> Quotation Status </b></td>");
    scoutln(out, bytesOut, "<td><p><b> Customer Code </b></td>");
    scoutln(out, bytesOut, "<td><p><b> Customer Name </b></td>");
    scoutln(out, bytesOut, "<td><p><b> Customer POCode </b></td>");
    scoutln(out, bytesOut, "<td><p><b> Amount </b></td>");
    scoutln(out, bytesOut, "<td><p><b> Currency </b></td></tr>");

    String[] cssFormat = new String[1];  cssFormat[0] = "line1";
    int[] oCount = new int[1];  oCount[0] = 1;
    char dpOnQuantities = miscDefinitions.dpOnQuantities(con, stmt, rs, "5");
    
    forAllQuotationHeaders(con, stmt, rs, out, checkBoxes, dpOnQuantities, cssFormat, oCount, dnm, localDefnsDir, defnsDir, bytesOut);
    
    scoutln(out, bytesOut, "</table></form>");     scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
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
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void forAllQuotationHeaders(Connection con, Statement stmt, ResultSet rs, PrintWriter out, byte[] checkBoxes, char dpOnQuantities, String[] cssFormat, int[] oCount,
                                      String dnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT SalesPerson, QuoteCode, QuoteDate, CompanyCode, EnquiryCode, TotalTotal, Currency, CompanyName, QuoteStatus "
                         + "FROM quote WHERE Status != 'C'"); ////////////
      
    String quoteCode, date, customerCode, currency, companyName, enquiryCode, quoteStatus, salesPerson;
    double totalTotal;
    byte[] b = new byte[20];
    
    while(rs.next())
    {    
      salesPerson = rs.getString(1);
      
      if(salesPersonIsRequired(salesPerson, checkBoxes))
      {
        quoteCode    = rs.getString(2);
        date         = rs.getString(3);
        customerCode = rs.getString(4);
        enquiryCode  = rs.getString(5);
        totalTotal   = generalUtils.doubleFromStr(rs.getString(6));
        currency     = rs.getString(7);
        companyName  = rs.getString(8);
        quoteStatus  = rs.getString(9);
      
        generalUtils.doubleToBytesCharFormat(totalTotal, b, 0);
        generalUtils.bytesDPsGivenSeparator(true, '\000', '2', b, 20, 0);
        generalUtils.formatNumeric(b, '2');

        if(cssFormat[0].equals("line1"))
          cssFormat[0] = "line2";
        else cssFormat[0] = "line1";
 
        scoutln(out, bytesOut, "<tr id=\"" + cssFormat[0] + "\">");

        scoutln(out, bytesOut, "<td><p>" + oCount[0]++ + "</td>");

        scoutln(out, bytesOut, "<td><p><a href=\"javascript:viewQuote('" + quoteCode + "')\">" + quoteCode + "</a></td>");
        scout(out, bytesOut, "<td><p>" + generalUtils.convertFromYYYYMMDD(date) + "</td>");

        if(quoteCode.indexOf("1") == -1 && quoteCode.indexOf("2") == -1)
          scout(out, bytesOut, "<td align=center><p><input type=checkbox></td>");
        else 
        if(quoteCode.indexOf("1") == -1 && quoteCode.indexOf("5") == -1)
          scout(out, bytesOut, "<td align=center><p>Sent</td>");
        else scout(out, bytesOut, "<td align=center><p>Approved</td>");

        if(quoteCode.indexOf("1") == -1 && quoteCode.indexOf("2") == -1)
          scout(out, bytesOut, "<td align=center><p><input type=checkbox></td>");
        else 
        if(quoteCode.indexOf("0") == -1 && quoteCode.indexOf("4") == -1)
          scout(out, bytesOut, "<td align=center><p>Sent</td>");
        else scout(out, bytesOut, "<td align=center><p>Confirmed</td>");

        if(quoteCode.indexOf("4") == -1 && quoteCode.indexOf("5") == -1)
          scout(out, bytesOut, "<td align=center><p><input type=checkbox></td>");
        else 
        if(quoteCode.indexOf("0") == -1 && quoteCode.indexOf("7") == -1)
          scout(out, bytesOut, "<td align=center><p>Sent</td>");
        else scout(out, bytesOut, "<td align=center><p>Confirmed</td>");

        if(quoteCode.indexOf("1") == -1 && quoteCode.indexOf("2") == -1)
          scout(out, bytesOut, "<td align=center><p><input type=checkbox></td>");
        else 
        if(quoteCode.indexOf("0") == -1 && quoteCode.indexOf("6") == -1)
          scout(out, bytesOut, "<td align=center><p>Requested</td>");
        else scout(out, bytesOut, "<td align=center><p>Approved</td>");

        scout(out, bytesOut, "<td align=center><p><input type=checkbox></td>");
      
        scout(out, bytesOut, "<td nowrap><p>" + quoteStatus + "</td>");
      
        scout(out, bytesOut, "<td><p><a href=\"javascript:viewCust('" + customerCode + "')\">" + customerCode + "</a></td>");
        scout(out, bytesOut, "<td nowrap><p>" + companyName + "</td>");
        scout(out, bytesOut, "<td nowrap><p>" + enquiryCode + "</td>");
        scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.stringFromBytes(b, 0L) + "</td>");
        scout(out, bytesOut, "<td nowrap><p>" + currency + "</td></tr>");
      }
    }
  
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean salesPersonIsRequired(String salesPerson, byte[] checkBoxes) throws Exception
  {
    String s;

    int x = 0, len = generalUtils.lengthBytes(checkBoxes, 0);
    while(x < len)
    {
      s = "";
      while(checkBoxes[x] != '\001' && checkBoxes[x] != '\000')
        s += (char)checkBoxes[x++];

      s = generalUtils.deSanitise(s);

      if(s.equals(salesPerson))
        return true;

      ++x;
    }
    
    return false;
  }

}

