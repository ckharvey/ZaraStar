// =================================================================================================================================================================================================================================================
// System: ZaraStar Accounts: Invoice Line Verification
// Module: AccountsInvoiceLineVerification.java
// Author: C.K.Harvey
// Copyright (c) 2003-09 Christopher Harvey. All Rights Reserved.
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
import java.sql.*;

public class AccountsInvoiceLineVerification extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  Customer customer = new Customer();
  Supplier supplier = new Supplier();
  DefinitionTables definitionTables = new DefinitionTables();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="";

    try
    {
      directoryUtils.setContentHeaders(res);
      out = res.getWriter();

      unm = req.getParameter("unm");
      uty = req.getParameter("uty");
      sid = req.getParameter("sid");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");
      p1  = req.getParameter("p1");

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "AccountsInvoiceLineVerification", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6044, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush();
    }
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String year, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');

    String uName = directoryUtils.getMySQLUserName();
    String pWord = directoryUtils.getMySQLPassWord();
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null, stmt2 = null;
    ResultSet rs   = null, rs2   = null;

    Connection conAcc = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_accounts_" + year + "?user=" + uName + "&password=" + pWord);

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 6044, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "AccountsInvoiceLineVerification", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6044, bytesOut[0], 0, "ACC:" + year);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "AccountsInvoiceLineVerification", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6044, bytesOut[0], 0, "SID:" + year);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    create(con, conAcc, stmt, stmt2, rs, rs2, out, req, year, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 6044, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), year);
    if(conAcc != null) conAcc.close();
    if(con    != null) con.close();
    if(out != null) out.flush();
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void create(Connection con, Connection conAcc, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, HttpServletRequest req, String year, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                      String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Verification - Invoices</title>");

    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    scoutln(out, bytesOut, "function sinv(code){");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/SalesInvoicePage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=\"+escape(code)+\"&p2=A&p3=&p4=&bnm=" + bnm + "\";}");

    scoutln(out, bytesOut, "function pinv(code){");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/PurchaseInvoicePage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=\"+escape(code)+\"&p2=A&p3=&p4=&bnm=" + bnm + "\";}");

    scoutln(out, bytesOut, "</script>");

    int[] hmenuCount = new int[1];

    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "6044", "", "AccountsInvoiceLineVerification", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Verification - Invoices: " + year, "6044", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    String[] financialYearStartMonth = new String[1];
    String[] financialYearEndMonth   = new String[1];
    definitionTables.getAppConfigFinancialYearMonths(con, stmt, rs, dnm, financialYearStartMonth, financialYearEndMonth);

    String financialYearEndDate = generalUtils.lastDayOfMonthYYYYMMDD(year + "-" + generalUtils.detMonthNumFromMonthName(financialYearEndMonth[0]) + "-01");
    String financialYearStartDate = year + "-" + generalUtils.detMonthNumFromMonthName(financialYearStartMonth[0]) + "-01";

    int effectiveStartDate = generalUtils.encode(definitionTables.getAppConfigEffectiveStartDate(con, stmt, rs, dnm), localDefnsDir, defnsDir);

    if(effectiveStartDate > generalUtils.encodeFromYYYYMMDD(financialYearStartDate))
      financialYearStartDate = generalUtils.decodeToYYYYMMDD(effectiveStartDate);

    scoutln(out, bytesOut, "<form><table id=\"page\" border=0 width=100%>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td colspan=8><span id=\"msg\"></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr id=\"pageColumn\">");
    scoutln(out, bytesOut, "<td><p>Type &nbsp;</td>");
    scoutln(out, bytesOut, "<td><p>Invoice Code &nbsp;</td>");
    scoutln(out, bytesOut, "<td><p>Invoice Line &nbsp;</td>");
    scoutln(out, bytesOut, "<td><p>Date &nbsp;</td>");
    scoutln(out, bytesOut, "<td><p>Company Code &nbsp;</td>");
    scoutln(out, bytesOut, "<td><p>Company Name &nbsp;</td>");
    scoutln(out, bytesOut, "<td><p>InCorrect Category &nbsp;</td></tr>");

    sales(con, conAcc, stmt, stmt2, rs, rs2, out, financialYearStartDate, financialYearEndDate, bytesOut);
    purchase(con, conAcc, stmt, stmt2, rs, rs2, out, financialYearStartDate, financialYearEndDate, bytesOut);

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir));
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void sales(Connection con, Connection conAcc, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, String dateFrom, String dateTo, int[] bytesOut) throws Exception
  {
    try
    {

      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT t1.InvoiceCode, t1.Date, t1.CompanyCode, t1.CompanyName, t2.Category, t2.Line FROM invoicel AS t2 INNER JOIN invoice AS t1 ON t2.InvoiceCode = t1.InvoiceCode WHERE Status != 'C' AND Date >= {d '" + dateFrom
                           + "'} AND Date <= {d '" + dateTo + "'} ORDER BY InvoiceCode");

      String invoiceCode, date, companyCode, companyName, cssFormat= "", category, line;

      while(rs.next())
      {
        invoiceCode = rs.getString(1);
        date        = rs.getString(2);
        companyCode = rs.getString(3);
        companyName = rs.getString(4);
        category    = rs.getString(5);
        line        = rs.getString(6);

        if(! isAccCode(conAcc, stmt2, rs2, category))
        {
          if(cssFormat.equals("line2")) cssFormat = "line1"; else cssFormat = "line2";

          scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\">");

          scoutln(out, bytesOut, "<td><p>Sales Invoice</td>");
          scoutln(out, bytesOut, "<td nowrap><a href=\"javascript:sinv('" + invoiceCode + "')\">" + invoiceCode + "</a></td>");
          scoutln(out, bytesOut, "<td><p>" + line + "</td>");
          scoutln(out, bytesOut, "<td><p>" + generalUtils.convertFromYYYYMMDD(date) + "</td>");
          scoutln(out, bytesOut, "<td><p>" + companyCode + "</td>");
          scoutln(out, bytesOut, "<td><p>" + companyName + "</td>");
          scoutln(out, bytesOut, "<td><p>" + category + "</td></tr>");
        }
     }

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void purchase(Connection con, Connection conAcc, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, String dateFrom, String dateTo, int[] bytesOut) throws Exception
  {
    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT t1.InvoiceCode, t1.Date, t1.CompanyCode, t1.CompanyName, t2.Category, t2.Line FROM pinvoicel AS t2 INNER JOIN pinvoice AS t1 ON t2.InvoiceCode = t1.InvoiceCode WHERE Status != 'C' AND Date >= {d '" + dateFrom
                           + "'} AND Date <= {d '" + dateTo + "'} ORDER BY InvoiceCode");

      String invoiceCode, date, companyCode, companyName, cssFormat= "", category, line;

      while(rs.next())
      {
        invoiceCode = rs.getString(1);
        date        = rs.getString(2);
        companyCode = rs.getString(3);
        companyName = rs.getString(4);
        category    = rs.getString(5);
        line        = rs.getString(6);

        if(! isAccCode(conAcc, stmt2, rs2, category))
        {
          if(cssFormat.equals("line2")) cssFormat = "line1"; else cssFormat = "line2";

          scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\">");

          scoutln(out, bytesOut, "<td><p>Sales Invoice</td>");
          scoutln(out, bytesOut, "<td nowrap><a href=\"javascript:pinv('" + invoiceCode + "')\">" + invoiceCode + "</a></td>");
          scoutln(out, bytesOut, "<td><p>" + line + "</td>");
          scoutln(out, bytesOut, "<td><p>" + generalUtils.convertFromYYYYMMDD(date) + "</td>");
          scoutln(out, bytesOut, "<td><p>" + companyCode + "</td>");
          scoutln(out, bytesOut, "<td><p>" + companyName + "</td>");
          scoutln(out, bytesOut, "<td><p>" + category + "</td></tr>");
        }
     }

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {
    out.println(str);
    bytesOut[0] += (str.length() + 2);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean isAccCode(Connection con, Statement stmt, ResultSet rs, String accCode) throws Exception
  {
    boolean res = false;

    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT AccCode FROM acctdefn WHERE AccCode = '" + accCode + "'");

      if(rs.next())
        res = true;
    }
    catch(Exception e) { }

    return res;
  }

}
