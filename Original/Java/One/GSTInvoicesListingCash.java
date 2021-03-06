// =======================================================================================================================================================================================================
// System: ZaraStar Document: GST: Invoices listing (cash)
// Module: GSTInvoicesListingCash.java
// Author: C.K.Harvey
// Copyright (c) 2001-11 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.io.*;

public class GSTInvoicesListingCash extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  DashboardUtils dashboardUtils = new DashboardUtils();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="", p3="", p4="";
    try
    {
      req.setCharacterEncoding("UTF-8");
      directoryUtils.setContentHeaders2(res);

      unm = req.getParameter("unm");
      sid = req.getParameter("sid");
      uty = req.getParameter("uty");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");
      p1  = req.getParameter("p1"); // dateFrom
      p2  = req.getParameter("p2"); // dateTo
      p3  = req.getParameter("p3"); // gstRate
      p4  = req.getParameter("p4"); // plain or not

      if(p4 == null) p4 = "";
      
      if(! p4.equals("X"))
        out = res.getWriter();

      doIt(out, req, res, unm, sid, uty, men, den, dnm, bnm, p1, p2, p3, p4, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "GSTInvoicesListingCash", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6003, bytesOut[0], 0, "ERR:" + p1 + " " + p2 + " " + p3);
      if(out != null) out.flush();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, HttpServletResponse res, String unm, String sid, String uty, String men, String den,
                    String dnm, String bnm, String p1, String p2, String p3, String p4, int[] bytesOut) throws Exception
  {
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');
    String workingDir       = directoryUtils.getUserDir('W', dnm, "/" + unm);

    long startTime = new java.util.Date().getTime();

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 6003, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "GSTInvoicesListingCash", imagesDir, localDefnsDir,
                         defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6003, bytesOut[0], 0, "ACC:" + p1 + " " + p2 + " " + p3);
    if(con  != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "GSTInvoicesListingCash", imagesDir, localDefnsDir,
                         defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6003, bytesOut[0], 0,  "SID:" + p1 + " " + p2 + " " + p3);
    if(con  != null) con.close();
      if(out != null) out.flush();
      return;
    }

    boolean plain = false;
    if(p4.equals("P"))
      plain = true;

    boolean isCSV = false;
    RandomAccessFile fh = null;
    String fName = "Data_OutputGST_Cash.csv";
    double[] baseGrandTotal    = new double[1];  baseGrandTotal[0] = 0.0;
    double[] baseGSTGrandTotal = new double[1];  baseGSTGrandTotal[0] = 0.0;
    double[] baseExclusiveGrandTotal = new double[1];  baseExclusiveGrandTotal[0] = 0.0;

    if(p4.equals("X"))
    {
      fh = generalUtils.create(workingDir + fName);
      isCSV = true;

      writeEntry(fh, "InvoiceCode",         ' ', ' ', true, false);
      writeEntry(fh, "Invoice Date",        ' ', ' ', true, false);
      writeEntry(fh, "Customer Code",       ' ', ' ', true, false);
      writeEntry(fh, "Customer Name",       ' ', ' ', true, false);
      writeEntry(fh, "Currency",            ' ', ' ', true, false);
      writeEntry(fh, "BaseGSTTotal",        ' ', ' ', true, false);
      writeEntry(fh, "BaseExclusive",       ' ', ' ', true, false);
      writeEntry(fh, "BaseTotal",           ' ', ' ', true, false);
      writeEntry(fh, "IssueTotal",          ' ', ' ', true, false);
      writeEntry(fh, "IssueExclusive",      ' ', ' ', true, false);
      writeEntry(fh, "IssueGSTTotal",       ' ', ' ', false, true);
    }

    generate(con, stmt, rs, out, req, isCSV, fh, unm, sid, uty, men, den, dnm, bnm, p1, p2, p3, plain, imagesDir, localDefnsDir, defnsDir, bytesOut, baseGrandTotal, baseGSTGrandTotal, baseExclusiveGrandTotal);

    if(p4.equals("X"))
    {
      writeEntry(fh, "",                   ' ', ' ', true, false);
      writeEntry(fh, "",                   ' ', ' ', true, false);
      writeEntry(fh, "",                   ' ', ' ', true, false);
      writeEntry(fh, "",                   ' ', ' ', true, false);
      writeEntry(fh, "",                   ' ', ' ', true, false);
      writeEntry(fh, generalUtils.doubleToStr('2', baseGSTGrandTotal[0]), 'V', ' ', true, false);
      writeEntry(fh, generalUtils.doubleToStr('2', baseExclusiveGrandTotal[0]),    'V', ' ', true, false);
      writeEntry(fh, generalUtils.doubleToStr('2', baseGrandTotal[0]),    'V', ' ', true, false);
      writeEntry(fh, "",                   ' ', ' ', true, false);
      writeEntry(fh, "",                   ' ', ' ', true, false);
      writeEntry(fh, "",                   ' ', ' ', false, true);
      generalUtils.fileClose(fh);
      download(res, workingDir, fName, bytesOut);
    }

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 6003, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1 + " " + p2 + " " + p3);
    if(con  != null) con.close();
    if(! p4.equals("X"))
      if(out != null) out.flush();
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void generate(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, boolean isCSV, RandomAccessFile fh, String unm, String sid, String uty, String men,
                        String den, String dnm, String bnm, String dateFrom, String dateTo, String gstRate, boolean plain, String imagesDir,
                        String localDefnsDir, String defnsDir, int[] bytesOut, double[] baseGrandTotal, double[] baseGSTGrandTotal, double[] baseExclusiveGrandTotal) throws Exception
  {
    if(! isCSV)
    {
      setHead(con, stmt, rs, out, req, plain, dateFrom, dateTo, gstRate, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);
      startBody(true, out, imagesDir, bytesOut);
    }

    stmt = con.createStatement();
  
    String invoiceCode, date, companyCode, baseGSTTotal, baseTotalTotal, totalTotal, gstTotal, currency, name, baseExclusiveTotal, issueExclusiveTotal;
     
    rs = stmt.executeQuery("SELECT DISTINCT t1.InvoiceCode, t1.Date, t1.CompanyCode, t1.BaseGSTTotal, t1.BaseTotalTotal, t1.TotalTotal, t1.GSTTotal, t1.Currency, t1.CompanyName  FROM invoicel AS t2 INNER JOIN invoice AS t1 "
                                   + "ON t2.InvoiceCode = t1.InvoiceCode WHERE t1.Date >= {d '" + dateFrom + "'} AND t1.Date <= {d '" + dateTo + "'} AND t1.Status != 'C' AND t1.CashOrAccount = 'C' AND t2.GSTRate = '" + gstRate + "'");

    String cssFormat = "";
    
    while(rs.next())                  
    {
      invoiceCode    = rs.getString(1);
      date           = rs.getString(2);
      companyCode    = rs.getString(3);
      baseGSTTotal   = rs.getString(4);
      baseTotalTotal = rs.getString(5);
      totalTotal     = rs.getString(6);
      gstTotal       = rs.getString(7);
      currency       = rs.getString(8);
      name           = rs.getString(9);

      if(isCSV)
      {
        baseExclusiveTotal = generalUtils.doubleToStr(generalUtils.doubleFromStr(baseTotalTotal) - generalUtils.doubleFromStr(baseGSTTotal));
        issueExclusiveTotal = generalUtils.doubleToStr(generalUtils.doubleFromStr(totalTotal) - generalUtils.doubleFromStr(gstTotal));

        writeEntry(fh, invoiceCode,     ' ', '2', true, false);
        writeEntry(fh, date,            'D', '2', true, false);
        writeEntry(fh, companyCode,     ' ', '2', true, false);
        writeEntry(fh, name,            ' ', '2', true, false);
        writeEntry(fh, currency,        ' ', '2', true, false);
        writeEntry(fh, baseGSTTotal,    'V', '2', true, false);
        writeEntry(fh, baseExclusiveTotal, 'V', '2', true, false);
        writeEntry(fh, baseTotalTotal,  'V', '2', true, false);
        writeEntry(fh, totalTotal,      'V', '2', true, false);
        writeEntry(fh, issueExclusiveTotal, 'V', '2', true, false);
        writeEntry(fh, gstTotal,        'V', '2', false, true);

        baseGrandTotal[0]          += generalUtils.doubleFromStr(baseTotalTotal);
        baseGSTGrandTotal[0]       += generalUtils.doubleFromStr(baseGSTTotal);
        baseExclusiveGrandTotal[0] += generalUtils.doubleFromStr(baseExclusiveTotal);
      }
      else
      {
        if(cssFormat.equals("line1")) cssFormat = "line2"; else cssFormat = "line1";
        appendBodyLine(out, invoiceCode, date, companyCode, baseGSTTotal, baseTotalTotal, totalTotal, gstTotal, currency, cssFormat, bytesOut);
      }
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
   
    if(! isCSV)
    {
      startBody(false, out, imagesDir, bytesOut);
      scoutln(out, bytesOut, "</table></form>");
      scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir));
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void setHead(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, boolean plain, String dateFrom, String dateTo, String gstRate, String unm, String sid, String uty, String men,
                       String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut)
                       throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>GST Invoice Listing</title>");
    scoutln(out, bytesOut, "<script javascript=\"JavaScript\">");

    scoutln(out, bytesOut, "function fetch(code){");
    scoutln(out, bytesOut, "var code2=sanitise(code);");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/SalesInvoicePage?unm=" + unm + "&sid="
                           + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                           + "&p2=A&p1=\"+code2;}");

    scoutln(out, bytesOut, "function sanitise(code){");
    scoutln(out, bytesOut, "var code2='';var x;var len=code.length;");
    scoutln(out, bytesOut, "for(x=0;x<len;++x)");
    scoutln(out, bytesOut, "if(code.charAt(x)=='#')code2+='%23';");
    scoutln(out, bytesOut, "else if(code.charAt(x)==\"'\")code2+='%27';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='\"')code2+='%22';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='&')code2+='%26';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='%')code2+='%25';");
    scoutln(out, bytesOut, "else if(code.charAt(x)==' ')code2+='%20';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='?')code2+='%3F';");
    scoutln(out, bytesOut, "else code2+=code.charAt(x);");
    scoutln(out, bytesOut, "return code2};");

    if(plain)
      scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"print\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "generalPlain.css\">");
    else scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    outputPageFrame(con, stmt, rs, out, req, plain, dateFrom, dateTo, gstRate, "", "GST Reconciliation: Invoice Listing", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, bytesOut);

    scoutln(out, bytesOut, "<form><br>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void startBody(boolean first, PrintWriter out, String imagesDir, int[] bytesOut) throws Exception
  {
    if(first)
      scoutln(out, bytesOut, "<table id=\"page\" border=0 cellspacing=2 cellpadding=0>");
    else scoutln(out, bytesOut, "<tr><td><img src=\"" + imagesDir + "z402.gif\"></td></tr>");

    scoutln(out, bytesOut, "<tr id=\"pageColumn\"><td nowrap><p>Invoice Code &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Date &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Customer Code &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Currency &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Base GST Total &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Base Total &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Issue Total &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Issue GST Total &nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td><img src=\"" + imagesDir + "z402.gif\"></td></tr>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void appendBodyLine(PrintWriter out, String invoiceCode, String date, String companyCode, String baseGSTTotal,
                              String baseTotalTotal, String totalTotal, String gstTotal, String currency, String cssFormat,
                              int[] bytesOut) throws Exception
  {
    String s = checkCode(invoiceCode);

    scoutln(out, bytesOut, "<tr  id=\"" + cssFormat + "\">");

    scoutln(out, bytesOut, "<td nowrap><p><a href=\"javascript:fetch('" + s + "')\">" + invoiceCode + "</a>&nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>" + generalUtils.convertFromYYYYMMDD(date) + "&nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>" + companyCode + "&nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>" + currency + "&nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap align=right><p>" + generalUtils.doubleDPs(baseGSTTotal, '2') + "&nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap align=right><p>" + generalUtils.doubleDPs(baseTotalTotal, '2') + "&nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap align=right><p>" + generalUtils.doubleDPs(totalTotal, '2') + "&nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap align=right><p>" + generalUtils.doubleDPs(gstTotal, '2') + "&nbsp;</td></tr>");
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------
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
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void outputPageFrame(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, boolean plain, String dateFrom, String dateTo, String gstRate, String bodyStr, String title, String unm, String sid, String uty,
                               String men, String den, String dnm, String bnm, String otherSetup, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    int[] hmenuCount = new int[1];

    String subMenuText = dashboardUtils.buildSubMenuText(con, stmt, rs, req, "6003", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount);

    subMenuText += "<table id='title' width=100%><tr><td colspan=2 nowrap><p>" + title + directoryUtils.buildHelp(6003) + "</td></tr></table>";

    subMenuText += buildSubMenuText(plain, dateFrom, dateTo, gstRate, unm, sid, uty, men, den, dnm, bnm, hmenuCount);

    if(plain)
      scoutln(out, bytesOut, authenticationUtils.buildPlainScreen(con, stmt, rs, req, unm, sid, uty, men, den, dnm, bnm, "GSTReconciliation", subMenuText, hmenuCount[0], bodyStr, otherSetup, localDefnsDir, defnsDir));
    else
    {
      scoutln(out, bytesOut, authenticationUtils.buildScreen(con, stmt, rs, req, true, unm, sid, uty, men, den, dnm, bnm, "GSTReconciliation", subMenuText, hmenuCount[0], bodyStr, otherSetup, localDefnsDir, defnsDir));

      scoutln(out, bytesOut, "<table border=0><tr><td valign=top><div id='second' style=\"{margin-top:0px;}\"></div></td>");
      scoutln(out, bytesOut, "<td valign=top><div id='third' style=\"{margin-top:0px;margin-left:0px;}\"></div></td></tr></table>");
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String buildSubMenuText(boolean plain, String dateFrom, String dateTo, String gstRate, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] hmenuCount) throws Exception
  {
    String s = "<div id='submenu'>";

    ++hmenuCount[0];

    if(! plain)
    {
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\">";
      s += "<a href=\"/central/servlet/GSTInvoicesListingCash?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + dateFrom + "&p2=" + dateTo + "&p3=" + generalUtils.sanitise(gstRate)
        + "&p4=P\">Friendly</a></dt></dl>";
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\">";
      s += "<a href=\"/central/servlet/GSTInvoicesListingCash?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + dateFrom + "&p2=" + dateTo + "&p3=" + generalUtils.sanitise(gstRate)
        + "&p4=X\">Export</a></dt></dl>";
    }

    s += "</div>";

    --hmenuCount[0];

    return s;
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }


  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void download(HttpServletResponse res, String dirName, String fileName, int[] bytesOut) throws Exception
  {
    res.setContentType("application/x-download");
    res.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

    OutputStream out = res.getOutputStream();

    InputStream in = null;
    try
    {
      in = new BufferedInputStream(new FileInputStream(dirName + fileName));
      byte[] buf = new byte[4096];
      int bytesRead;
      while((bytesRead = in.read(buf)) != -1)
        out.write(buf, 0, bytesRead);
    }
    catch(Exception e) //finally
    {
      if(in != null)
        in.close();
    }

    File file = new File(dirName + fileName);
    long fileSize = file.length();

    bytesOut[0] += (int)fileSize;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void writeEntry(RandomAccessFile fh, String entry, char type, char dpOnQuantities, boolean comma, boolean newLine) throws Exception
  {
    if(entry == null) entry = "";

    switch(type)
    {
      case 'D' : entry = generalUtils.convertFromYYYYMMDD(entry);       break;
      case 'Q' : entry = generalUtils.doubleDPs(entry, dpOnQuantities); break;
      case 'V' : entry = generalUtils.doubleDPs(entry, '2');            break;
    }

    fh.writeBytes("\"");
    for(int x=0;x<entry.length();++x)
    {
      if(entry.charAt(x) == '"')
        fh.writeBytes("''");
      else fh.writeBytes("" + entry.charAt(x));
    }

    fh.writeBytes("\"");

    if(comma)
      fh.writeBytes(",");

    if(newLine)
      fh.writeBytes("\n");
  }

}
