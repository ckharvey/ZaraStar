// =======================================================================================================================================================================================================
// System: ZaraStar: Accounts: Generate TB report
// Module: AccountsGenerateTBReport.java
// Author: C.K.Harvey
// Copyright (c) 1998-2009 Christopher Harvey. All Rights Reserved.
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

public class AccountsGenerateTBReport extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  AccountsUtils accountsUtils = new AccountsUtils();
  DashboardUtils dashboardUtils = new DashboardUtils();
  MiscDefinitions miscDefinitions = new MiscDefinitions();
  DefinitionTables definitionTables = new DefinitionTables();
  Inventory inventory = new Inventory();
  SalesInvoice salesInvoice = new SalesInvoice();
  Customer customer = new Customer();
  Supplier supplier = new Supplier();
  PickingList pickingList = new PickingList();
  WacMetaUtils wacMetaUtils = new WacMetaUtils();
  
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="", p3="", p4="", p5="";
    try
    {
      directoryUtils.setContentHeaders2(res);
      out = res.getWriter();

      unm = req.getParameter("unm");
      sid = req.getParameter("sid");
      uty = req.getParameter("uty");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");
      p1  = req.getParameter("p1"); // monthFrom
      p2  = req.getParameter("p2"); // monthTo
      p3  = req.getParameter("p3"); // plain
      p4  = req.getParameter("p4"); // combined or separate
      p5  = req.getParameter("p5"); // general, subaccounts

      if(p3 == null) p3 = "N";

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, p3, p4, p5, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "AccountsGenerateTBReport", bytesOut);
      System.out.println(e);
      serverUtils.etotalBytes(req, unm, dnm, 6053, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1, String p2, String p3, String p4, String p5, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');
    String workingDir    = directoryUtils.getUserDir('W', dnm, unm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 6053, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "6053a", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6053, bytesOut[0], 0, "ACC:");
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "6053a", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6053, bytesOut[0], 0, "SID:");
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    scoutln(out, bytesOut, "<html><head><title>Trial Balance</title></head>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    boolean debug = false;
    String debugDateFrom = "2008-11-19";
    String debugDateTo   = "2008-11-20";

    if(debug)
    {
      scoutln(out, bytesOut, "function gl(account,dateFrom,dateTo){");
      scoutln(out, bytesOut, "window.location.href=\"/central/servlet/AccountsViewGLAccountFromTB?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+account+\"&p2=" + debugDateFrom + "&p3="
                           + debugDateTo + "\";}");
      
      scoutln(out, bytesOut, "function glD(code,dateFrom,dateTo){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/AccountsGLDebtorsIndividualExecute?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=" + debugDateFrom + "&p3=" + debugDateTo
                           + "&p4=&p5=&p6=&p4=&p1=\"+p1;}");

      scoutln(out, bytesOut, "function glC(code,dateFrom,dateTo){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/AccountsGLCreditorsIndividualExecute?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=" + debugDateFrom + "&p3=" + debugDateTo
                           + "&p4=&p5=&p6=&p4=&p1=\"+p1;}");
    }
    else
    {
      scoutln(out, bytesOut, "function gl(account,dateFrom,dateTo){");
      scoutln(out, bytesOut, "window.location.href=\"/central/servlet/AccountsViewGLAccountFromTB?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+account+\"&p2=\"+dateFrom+\"&p3=\"+dateTo;}");

      scoutln(out, bytesOut, "function glD(code,dateFrom,dateTo){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/AccountsGLDebtorsIndividualExecute?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                           + "&p2=\"+dateFrom+\"&p3=\"+dateTo+\"&p4=&p5=&p6=&p4=&p1=\"+p1;}");

      scoutln(out, bytesOut, "function glC(code,dateFrom,dateTo){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/AccountsGLCreditorsIndividualExecute?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                          + "&p2=\"+dateFrom+\"&p3=\"+dateTo+\"&p4=&p5=&p6=&p4=&p1=\"+p1;}");
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
    
    boolean plain;
    if(p3.equals("P"))
      plain = true;
    else plain = false;
        
    if(plain)
      scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"print\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "generalPlain.css\">");
    else scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    String s = "Trail Balance";
    if(p5.equals("S"))
      s += " (Debtors and Creditors Detail)";

    outputPageFrame(con, stmt, rs, out, req, plain, p1, p2, p4, s, p5, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    scoutln(out, bytesOut, "<table id=\"page\" border=0 width=100%>");

    if(plain)
    {
      scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
      scoutln(out, bytesOut, "<tr><td colspan=4><p><b>Trial Balance: " + p1 + " to " + p2 + "</td></tr>");
      scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    }

    int[] month = new int[1];
    int[] year  = new int[1];

    generalUtils.monthYearStrToYearAndMonth2(p2, month, year);
    String dateTo = generalUtils.lastDayOfMonthYYYYMMDD(year[0] + "-" + month[0] + "-" + "1");

    generalUtils.monthYearStrToYearAndMonth2(p1, month, year);
    String dateFrom = year[0] + "-" + month[0] + "-" + "01";

    // All months for the year are considered for data (but NOT those before an effective start date, if applicable).
    // Months not required to be shown are aggregated into the Open colunm
    
    // At this point, dateFrom and dateTo are the user-specified fromMonth and toMonth

    String yearStartDate = dateFrom;
    String yearEndDate   = dateTo;

    // need to know financial year start date for WAC start-from
    String yyyy = generalUtils.getFromDefnFile(unm, "accounts.dfn", workingDir, "");
    String[] financialYearStartMonth = new String[1];
    String[] financialYearEndMonth   = new String[1];
    definitionTables.getAppConfigFinancialYearMonths(con, stmt, rs, dnm, financialYearStartMonth, financialYearEndMonth);
    String financialYearStartDate = yyyy + "-" + generalUtils.detMonthNumFromMonthName(financialYearStartMonth[0]) + "-01";

    String previousFinancialYearStartDate = (generalUtils.intFromStr(yyyy) - 1) + "-" + generalUtils.detMonthNumFromMonthName(financialYearStartMonth[0]) + "-01";

    // determine the posn of that month in the range of months
    // e.g., if yearStart is April, and reqd month is June, then reqd month is 3

    int firstReqdMonthInList = 0, x = generalUtils.getMonth(generalUtils.encodeFromYYYYMMDD(yearStartDate));
    while(x < month[0])
    {
      ++firstReqdMonthInList;
      ++x;
    }       
    
    firstReqdMonthInList=0; // TODO
 
    if(debug)
    {
      yearStartDate = debugDateFrom;
      yearEndDate   = debugDateTo;
    }
   
    generate(con, stmt, rs, out, plain, p5, p4, yearStartDate, yearEndDate, firstReqdMonthInList, financialYearStartDate, previousFinancialYearStartDate, dnm, unm, workingDir, localDefnsDir, defnsDir, bytesOut);
    
    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 6053, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void generate(Connection con, Statement stmt, ResultSet rs, PrintWriter out, boolean plain, String which, String separateOrCombined, String dateFrom, String dateTo, int firstReqdMonthInList, String financialYearStartDate,
                        String previousFinancialYearStartDate, String dnm, String unm, String workingDir, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    String yyyy = generalUtils.getFromDefnFile(unm, "accounts.dfn", workingDir, "");

    String[][] accCodes = new String[1][];
    double[][][] accValsDr      = new double[1][][];
    double[][][] accValsCr      = new double[1][][];
    int[] numAccs = new int[1];
    char[][] accTypes = new char[1][];
        
    int[] monthEnds = new int[13];
    int[] numMonths = new int[1];

    int numCustomers = 0, numSuppliers = 0;
    if(! which.equals("G"))
    {
      numCustomers = customer.countCustomers(con, stmt, rs);
      numSuppliers = supplier.countSuppliers(con, stmt, rs);
    }

    String[]   debtorAccCodes  = new String[numCustomers];
    double[][] debtorAccValsDr = new double[14][numCustomers];
    double[][] debtorAccValsCr = new double[14][numCustomers];

    String[]   creditorAccCodes  = new String[numSuppliers];
    double[][] creditorAccValsDr = new double[14][numSuppliers];
    double[][] creditorAccValsCr = new double[14][numSuppliers];

    if(! which.equals("G"))
    {
      primeCustomers(con, stmt, rs, debtorAccCodes);
      primeSuppliers(con, stmt, rs, creditorAccCodes);
    }
    
    allAccounts(con, stmt, rs, which, yyyy, dateFrom, dateTo, firstReqdMonthInList, financialYearStartDate, previousFinancialYearStartDate, dnm, localDefnsDir, defnsDir, accCodes, accValsDr, accValsCr, numAccs, accTypes, numCustomers,
                numSuppliers, debtorAccCodes, creditorAccCodes, debtorAccValsDr, debtorAccValsCr, creditorAccValsDr, creditorAccValsCr, monthEnds, numMonths,     unm);

    String bgColor = "#F0F0F0";
    int x, y;
    
    scoutln(out, bytesOut, "<tr><td><p><b>AccCode</td>");
    scoutln(out, bytesOut, "<td><p><b>Description</td>");
    
    if(separateOrCombined.equals("S"))
      scoutln(out, bytesOut, "<td align=center colspan=2><p><b>Opening</td>");
    else scoutln(out, bytesOut, "<td align=center><p><b>Opening</td>");
    
    for(y=0;y<numMonths[0];++y)
    {
      if(separateOrCombined.equals("S"))
        scoutln(out, bytesOut, "<td align=center colspan=2><p><b>" + generalUtils.decode(monthEnds[y], localDefnsDir, defnsDir) + "</td>");
      else scoutln(out, bytesOut, "<td align=center><p><b>" + generalUtils.decode(monthEnds[y], localDefnsDir, defnsDir) + "</td>");
    }

    scoutln(out, bytesOut, "<td align=center colspan=2><p><b>Closing</td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td></td>");
    
    if(separateOrCombined.equals("S"))
    {
      scoutln(out, bytesOut, "<td align=center><p><b>Dr</td>");
      scoutln(out, bytesOut, "<td align=center><p><b>Cr</td>");
    
      for(y=0;y<numMonths[0];++y)
      {
        scoutln(out, bytesOut, "<td align=center><p><b>Dr</td>");
        scoutln(out, bytesOut, "<td align=center><p><b>Cr</td>");
      }

      scoutln(out, bytesOut, "<td align=center><p><b>Dr</td>");
      scoutln(out, bytesOut, "<td align=center><p><b>Cr</td></tr>");
    }

    String from, to;

    String debtorsAccount   = accountsUtils.getTradeDebtorsAccCode(yyyy, dnm, localDefnsDir, defnsDir);
    String creditorsAccount = accountsUtils.getTradeCreditorsAccCode(yyyy, dnm, localDefnsDir, defnsDir);

    for(x=0;x<numAccs[0];++x)
    {
      scoutln(out, bytesOut, "<tr><td><p>" + accCodes[0][x] + "</td>");
      scoutln(out, bytesOut, "<td nowrap><p>" + accountsUtils.getAccountDescriptionGivenAccCode(accCodes[0][x], yyyy, dnm, localDefnsDir, defnsDir) + "</td>");

      if(separateOrCombined.equals("S"))
      {
        if(plain)
          scoutln(out, bytesOut, "<td align=right style=\"background:#D0D0D0;\"><p>" + generalUtils.formatNumeric(accValsDr[0][12][x], '2') + "</td>");
        else scoutln(out, bytesOut, "<td align=right style=\"background:#D0D0D0;\"><p><a href=\"javascript:gl('" + accCodes[0][x] + "','" + dateFrom + "','" + dateTo + "')\">" + generalUtils.formatNumeric(accValsDr[0][12][x], '2') + "</a></td>");

        if(plain)
          scoutln(out, bytesOut, "<td align=right style=\"background:#D0D0D0;\"><p>" + generalUtils.formatNumeric(accValsCr[0][12][x], '2') + "</td>");
        else scoutln(out, bytesOut, "<td align=right style=\"background:#D0D0D0;\"><p><a href=\"javascript:gl('" + accCodes[0][x] + "','" + dateFrom + "','" + dateTo + "')\">" + generalUtils.formatNumeric(accValsCr[0][12][x], '2') + "</a></td>");
      }
      else
      {
        if(plain)
          scoutln(out, bytesOut, "<td align=right style=\"background:#D0D0D0;\"><p>" + generalUtils.formatNumeric((accValsDr[0][12][x] - accValsCr[0][12][x]), '2') + "</td>");
        else
        {
          scoutln(out, bytesOut, "<td align=right style=\"background:#D0D0D0;\"><p><a href=\"javascript:gl('" + accCodes[0][x] + "','" + dateFrom + "','" + dateTo + "')\">" + generalUtils.formatNumeric((accValsDr[0][12][x] - accValsCr[0][12][x]), '2')
                               + "</a></td>");
        }
      }
        
      for(y=0;y<numMonths[0];++y)
      {
        if(y == 0 || y == 2 || y == 4 || y == 6 || y == 8 || y == 10 || y == 12) bgColor = "#F0F0F0"; else bgColor = "#D0D0D0";
        
        to   = generalUtils.decodeToYYYYMMDD(monthEnds[y]);
        from = generalUtils.firstDayOfMonthYYYYMMDD(to);

        if(separateOrCombined.equals("S"))
        {
          if(plain)
            scoutln(out, bytesOut, "<td align=right style=\"background:" + bgColor + ";\"><p>" + generalUtils.formatNumeric(accValsDr[0][y][x], '2') + "</td>");
          else scoutln(out, bytesOut, "<td align=right style=\"background:" + bgColor + ";\"><p><a href=\"javascript:gl('" + accCodes[0][x] + "','" + from + "','" + to + "')\">" + generalUtils.formatNumeric(accValsDr[0][y][x], '2') + "</a></td>");

          if(plain)
            scoutln(out, bytesOut, "<td align=right style=\"background:" + bgColor + ";\"><p>" + generalUtils.formatNumeric(accValsCr[0][y][x], '2') + "</td>");
          else scoutln(out, bytesOut, "<td align=right style=\"background:" + bgColor + ";\"><p><a href=\"javascript:gl('" + accCodes[0][x] + "','" + from + "','" + to + "')\">" + generalUtils.formatNumeric(accValsCr[0][y][x], '2') + "</a></td>");
        }
        else
        {
          if(plain)
            scoutln(out, bytesOut, "<td align=right style=\"background:" + bgColor + ";\"><p>" + generalUtils.formatNumeric((accValsDr[0][y][x] - accValsCr[0][y][x]), '2') + "</td>");
          else
          {
            scoutln(out, bytesOut, "<td align=right style=\"background:" + bgColor + ";\"><p><a href=\"javascript:gl('" + accCodes[0][x] + "','" + from + "','" + to + "')\">" + generalUtils.formatNumeric((accValsDr[0][y][x] - accValsCr[0][y][x]), '2')
                                 + "</a></td>");
          }
        }
      }
      
      if(separateOrCombined.equals("S"))
      {
        if(plain)
          scoutln(out, bytesOut, "<td align=right style=\"background:#D0D0D0;\"><p>" + generalUtils.formatNumeric(accValsDr[0][13][x], '2') + "</td>");
        else scoutln(out, bytesOut, "<td align=right style=\"background:#D0D0D0;\"><p><a href=\"javascript:gl('" + accCodes[0][x] + "','" + dateFrom + "','" + dateTo + "')\">" + generalUtils.formatNumeric(accValsDr[0][13][x], '2') + "</a></td>");

        if(plain)
          scoutln(out, bytesOut, "<td align=right style=\"background:#D0D0D0;\"><p>" + generalUtils.formatNumeric(accValsCr[0][13][x], '2') + "</td>");
        else scoutln(out, bytesOut, "<td align=right style=\"background:#D0D0D0;\"><p><a href=\"javascript:gl('" + accCodes[0][x] + "','" + dateFrom + "','" + dateTo + "')\">" + generalUtils.formatNumeric(accValsCr[0][13][x], '2') + "</a></td>");
      }
      else
      {
        if(plain)
          scoutln(out, bytesOut, "<td align=right style=\"background:#D0D0D0;\"><p>" + generalUtils.formatNumeric((accValsDr[0][13][x] - accValsCr[0][13][x]), '2') + "</td>");
        else
        {
          scoutln(out, bytesOut, "<td align=right style=\"background:#D0D0D0;\"><p><a href=\"javascript:gl('" + accCodes[0][x] + "','" + dateFrom + "','" + dateTo + "')\">" + generalUtils.formatNumeric((accValsDr[0][13][x] - accValsCr[0][13][x]), '2')
                               + "</a></td>");
        }
      }
      
      scoutln(out, bytesOut, "</tr>");

      if(accCodes[0][x].equals(debtorsAccount))
      {
        if(! which.equals("G"))
          showDebtorsAndCreditors(con, stmt, rs, out, plain, separateOrCombined, "D", debtorAccCodes, numCustomers, debtorAccValsDr, debtorAccValsCr, numMonths, dateFrom, dateTo, bytesOut);
      }
      else
      if(accCodes[0][x].equals(creditorsAccount))
      {
        if(! which.equals("G"))
          showDebtorsAndCreditors(con, stmt, rs, out, plain, separateOrCombined, "C", creditorAccCodes, numSuppliers, creditorAccValsDr, creditorAccValsCr, numMonths, dateFrom, dateTo, bytesOut);
      }
    }
    
    // balances

    scoutln(out, bytesOut, "<tr><td colspan=2></td>");

    double totalsDr = 0.0, totalsCr = 0.0;
    int z;

    for(x=0;x<numAccs[0];++x) // OBs
    {
      totalsDr += generalUtils.doubleDPs(accValsDr[0][12][x], '2');
      totalsCr += generalUtils.doubleDPs(accValsCr[0][12][x], '2');

      if(accCodes[0][x].equals(debtorsAccount))
      {
        if(! which.equals("G"))
        {
          for(z=0;z<numCustomers;++z)
          {
            totalsDr += generalUtils.doubleDPs(debtorAccValsDr[12][z], '2');
            totalsCr += generalUtils.doubleDPs(debtorAccValsCr[12][z], '2');
          }
        }
      }
      else
      if(accCodes[0][x].equals(creditorsAccount))
      {
        if(! which.equals("G"))
        {
          for(z=0;z<numSuppliers;++z)
          {
            totalsDr += generalUtils.doubleDPs(creditorAccValsDr[12][z], '2');
            totalsCr += generalUtils.doubleDPs(creditorAccValsCr[12][z], '2');
          }
        }
      }
    }
    
    if(separateOrCombined.equals("S"))
    {
      scoutln(out, bytesOut, "<td align=right style=\"background:#D0D0D0;\"><p>" + generalUtils.formatNumeric(totalsDr, '2') + "</td>");
      scoutln(out, bytesOut, "<td align=right style=\"background:#D0D0D0;\"><p>" + generalUtils.formatNumeric(totalsCr, '2') + "</td>");
    }
    else scoutln(out, bytesOut, "<td align=right style=\"background:#D0D0D0;\"><p>" + generalUtils.formatNumeric((totalsDr - totalsCr), '2') + "</td>");
   
    for(y=0;y<numMonths[0];++y)
    {
    totalsDr = totalsCr = 0.0;

      for(x=0;x<numAccs[0];++x)
      {
      totalsDr += generalUtils.doubleDPs(accValsDr[0][y][x], '2');
      totalsCr += generalUtils.doubleDPs(accValsCr[0][y][x], '2');
        if(accCodes[0][x].equals(debtorsAccount))
        {
          if(! which.equals("G"))
          {
            for(z=0;z<numCustomers;++z)
            {
              totalsDr += generalUtils.doubleDPs(debtorAccValsDr[y][z], '2');
              totalsCr += generalUtils.doubleDPs(debtorAccValsCr[y][z], '2');
            }
          }
        }
        else
        if(accCodes[0][x].equals(creditorsAccount))
        {
          if(! which.equals("G"))
          {
            for(z=0;z<numSuppliers;++z)
            {
              totalsDr += generalUtils.doubleDPs(creditorAccValsDr[y][z], '2');
              totalsCr += generalUtils.doubleDPs(creditorAccValsCr[y][z], '2');
            }
          }
        }
      }
      
      totalsDr = generalUtils.doubleDPs(totalsDr, '2');
      totalsCr = generalUtils.doubleDPs(totalsCr, '2');
      
      if(y == 0 || y == 2 || y == 4 || y == 6 || y == 8 || y == 10 || y == 12) bgColor = "#F0F0F0"; else bgColor = "#D0D0D0";
   
      if(separateOrCombined.equals("S"))
      {
        scoutln(out, bytesOut, "<td align=right style=\"background:" + bgColor + ";\"><p>" + generalUtils.formatNumeric(totalsDr, '2') + "</td>");
        scoutln(out, bytesOut, "<td align=right style=\"background:" + bgColor + ";\"><p>" + generalUtils.formatNumeric(totalsCr, '2') + "</td>");
      }
      else scoutln(out, bytesOut, "<td align=right style=\"background:" + bgColor + ";\"><p>" + generalUtils.formatNumeric((totalsDr - totalsCr), '2') + "</td>");
    }
        
    totalsDr = totalsCr = 0.0;

    for(x=0;x<numAccs[0];++x)
    {
      totalsDr += generalUtils.doubleDPs(accValsDr[0][13][x], '2');
      totalsCr += generalUtils.doubleDPs(accValsCr[0][13][x], '2');

      if(accCodes[0][x].equals(debtorsAccount))
      {
        if(! which.equals("G"))
        {
          for(z=0;z<numCustomers;++z)
          {
            totalsDr += generalUtils.doubleDPs(debtorAccValsDr[13][z], '2');
            totalsCr += generalUtils.doubleDPs(debtorAccValsCr[13][z], '2');
          }
        }
      }
      else
      if(accCodes[0][x].equals(creditorsAccount))
      {
        if(! which.equals("G"))
        {
          for(z=0;z<numSuppliers;++z)
          {
            totalsDr += generalUtils.doubleDPs(creditorAccValsDr[13][z], '2');
            totalsCr += generalUtils.doubleDPs(creditorAccValsCr[13][z], '2');
          }
        }
      }
    }  
    
    totalsDr = generalUtils.doubleDPs(totalsDr, '2');
    totalsCr = generalUtils.doubleDPs(totalsCr, '2');

    if(separateOrCombined.equals("S"))
    {
      String s = generalUtils.formatNumeric(totalsDr, '2');
      int xS = 0, lenS = s.length();
      while(xS < lenS && s.charAt(xS) != '.')
        ++xS;
      xS += 3;
      if(xS < lenS) // just-in-case
        s = s.substring(0, xS);
    
      String t = generalUtils.formatNumeric(totalsCr, '2');
      xS = 0; lenS = t.length();
      while(xS < lenS && t.charAt(xS) != '.')
        ++xS;
      xS += 3;
      if(xS < lenS) // just-in-case
        t = t.substring(0, xS);
    
      scoutln(out, bytesOut, "<td align=right style=\"background:#F0F0F0;\"><p>" + s + "</td>");
      scoutln(out, bytesOut, "<td align=right style=\"background:#F0F0F0;\"><p>" + t + "</td>");
    }
    else
    {
      String s = generalUtils.formatNumeric((totalsDr - totalsCr), '2');
      int xS = 0, lenS = s.length();
      while(xS < lenS && s.charAt(xS) != '.')
        ++xS;
      xS += 3;
      if(xS < lenS) // just-in-case
        s = s.substring(0, xS);
  
      scoutln(out, bytesOut, "<td align=right style=\"background:#F0F0F0;\"><p>" + s + "</td>");
    }
    
    scoutln(out, bytesOut, "</tr>");
    
    // balances (as Dr and Cr difference)
    if(separateOrCombined.equals("S"))
    {
      scoutln(out, bytesOut, "<tr><td colspan=2></td>");

      double totals = 0.0;
  
      for(x=0;x<numAccs[0];++x)
      {
        totals += generalUtils.doubleDPs((accValsDr[0][12][x] - accValsCr[0][12][x]), '2');

        if(accCodes[0][x].equals(debtorsAccount))
        {
          if(! which.equals("G"))
          {
            for(z=0;z<numCustomers;++z)
              totals += generalUtils.doubleDPs((debtorAccValsDr[12][z] - debtorAccValsCr[12][z]), '2');
          }
        }
        else
        if(accCodes[0][x].equals(creditorsAccount))
        {
          if(! which.equals("G"))
          {
            for(z=0;z<numSuppliers;++z)
              totals += generalUtils.doubleDPs((creditorAccValsDr[12][z] - creditorAccValsCr[12][z]), '2');
          }
        }
      }
      
      scoutln(out, bytesOut, "<td align=center colspan=2 style=\"background:#D0D0D0;\"><p>" + generalUtils.formatNumeric(totals, '2') + "</td>");
   
      for(y=0;y<numMonths[0];++y)
      {
        totals = 0.0;
      
        for(x=0;x<numAccs[0];++x)
        {
          totals += generalUtils.doubleDPs((accValsDr[0][y][x] - accValsCr[0][y][x]), '2');

          if(accCodes[0][x].equals(debtorsAccount))
          {
            if(! which.equals("G"))
            {
              for(z=0;z<numCustomers;++z)
                totals += generalUtils.doubleDPs((debtorAccValsDr[y][z] - debtorAccValsCr[y][z]), '2');
            }
          }
          else
          if(accCodes[0][x].equals(creditorsAccount))
          {
            if(! which.equals("G"))
            {
              for(z=0;z<numSuppliers;++z)
                totals += generalUtils.doubleDPs((creditorAccValsDr[y][z] - creditorAccValsCr[y][z]), '2');
            }
          }
        }

        if(y == 0 || y == 2 || y == 4 || y == 6 || y == 8 || y == 10 || y == 12) bgColor = "#F0F0F0"; else bgColor = "#D0D0D0";
   
        totals = generalUtils.doubleDPs(totals, '2');
        scoutln(out, bytesOut, "<td align=center colspan=2 style=\"background:" + bgColor + ";\"><p>" + generalUtils.formatNumeric(totals, '2') + "</td>");
      }
    
      totals = 0.0;

      for(x=0;x<numAccs[0];++x)
      {
        totals += generalUtils.doubleDPs((accValsDr[0][13][x] - accValsCr[0][13][x]), '2');

        if(accCodes[0][x].equals(debtorsAccount))
        {
          if(! which.equals("G"))
          {
            for(z=0;z<numCustomers;++z)
              totals += generalUtils.doubleDPs((debtorAccValsDr[13][z] - debtorAccValsCr[13][z]), '2');
          }
        }
        else
        if(accCodes[0][x].equals(creditorsAccount))
        {
          if(! which.equals("G"))
          {
            for(z=0;z<numSuppliers;++z)
              totals += generalUtils.doubleDPs((creditorAccValsDr[13][z] - creditorAccValsCr[13][z]), '2');
          }
        }
      }

      totals = generalUtils.doubleDPs(totals, '2');
    
      if(bgColor.equals("#F0F0F0")) bgColor = "#D0D0D0"; else bgColor = "#F0F0F0";

      scoutln(out, bytesOut, "<td align=center colspan=2 style=\"background:" + bgColor + ";\"><p>" + generalUtils.formatNumeric(totals, '2') + "</td>");
   
      scoutln(out, bytesOut, "</tr>");
    }
  }
      
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void allAccounts(Connection con, Statement stmt, ResultSet rs, String which, String year, String scanDateFrom, String scanDateTo, int firstReqdMonthInList, String financialYearStartDate, String previousFinancialYearStartDate, String dnm,
                           String localDefnsDir, String defnsDir, String[][] accCodes, double[][][] accValsDr, double[][][] accValsCr, int[] numAccs, char[][] accTypes, int numCustomers, int numSuppliers, String[] debtorAccCodes,
                           String[] creditorAccCodes, double[][] debtorAccValsDr, double[][] debtorAccValsCr, double[][] creditorAccValsDr, double[][] creditorAccValsCr, int[] monthEnds, int[] numMonths,   String unm) throws Exception
  {
    Connection accCon = null, ofsaCon  = null;
    Statement accStmt = null, ofsaStmt = null;
    ResultSet accRs   = null, ofsaRs   = null;
    
    numAccs[0] = 0;

    try
    {
      String uName = directoryUtils.getMySQLUserName();
      String pWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();

      accCon = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_accounts_" + year + "?user=" + uName + "&password=" + pWord);

      ofsaCon = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + uName + "&password=" + pWord);

      accStmt = accCon.createStatement();

      accRs = accStmt.executeQuery("SELECT COUNT(*) AS rowcount FROM acctdefn WHERE Active = 'Y'"); 

      accRs.next();
      numAccs[0] = accRs.getInt("rowcount");

      if(accRs   != null) accRs.close();                                 
      if(accStmt != null) accStmt.close();

      accCodes[0] = new String[numAccs[0]];
      accTypes[0] = new char[numAccs[0]];

      accValsDr[0] = new double[14][numAccs[0]];
      accValsCr[0] = new double[14][numAccs[0]];

      String debtorsAccount = "", creditorsAccount = "", otherDebtorsAccount = "", otherCreditorsAccount = "", salesAccount = "", purchasesAccount = "", gstOutputAccount = "", gstInputAccount = "", exchangeGainAccount = "",
             exchangeLossAccount = "", salesReturnedAccount = "", purchasesReturnedAccount = "", stockAccount = "", discountReceivedAccount = "", discountGivenAccount = "";
      
      accStmt = accCon.createStatement();

      accRs = accStmt.executeQuery("SELECT AccCode, Category, Type FROM acctdefn WHERE Active = 'Y' ORDER BY AccCode"); 

      int x, y, count = 0;
      String category;
      
      while(accRs.next())
      {
        accCodes[0][count] = accRs.getString(1);
        category           = accRs.getString(2);
        accTypes[0][count] = accRs.getString(3).charAt(0);

        if(category.equals("Current Assets - Trade Debtors"))
          debtorsAccount = accRs.getString(1);
        else
        if(category.equals("Current Assets - Other Debtors"))
          otherDebtorsAccount = accRs.getString(1);
        else
        if(category.equals("Liabilities - Trade Creditors"))
          creditorsAccount = accRs.getString(1);
        else
        if(category.equals("Liabilities - Other Creditors"))
          otherCreditorsAccount = accRs.getString(1);
        else
        if(category.equals("Income - Sales"))
          salesAccount = accRs.getString(1);
        else
        if(category.equals("Income - Sales Returned"))
          salesReturnedAccount = accRs.getString(1);
        else
        if(category.equals("Cost of Sales - Purchases"))
          purchasesAccount = accRs.getString(1);
        else
        if(category.equals("Cost of Sales - Purchases Returned"))
          purchasesReturnedAccount = accRs.getString(1);
        else
        if(category.equals("Liabilities - GST Output"))
          gstOutputAccount = accRs.getString(1);
        else
        if(category.equals("Liabilities - GST Input"))
          gstInputAccount = accRs.getString(1);
        else
        if(category.equals("Income - Exchange Gain"))
          exchangeGainAccount = accRs.getString(1);
        else
        if(category.equals("Cost of Sales - Exchange Loss"))
          exchangeLossAccount = accRs.getString(1);
        else
        if(category.equals("Current Assets - Stock"))
          stockAccount = accRs.getString(1);
        else
        if(category.equals("Cost of Sales - Discount Received"))
          discountReceivedAccount = accRs.getString(1);
        else
        if(category.equals("Income - Discount Given"))
          discountGivenAccount = accRs.getString(1);

        ++count;
      }

      if(accRs   != null) accRs.close();                                 
      if(accStmt != null) accStmt.close();

      String baseCurrency = accountsUtils.getBaseCurrency(con, stmt, rs, dnm, localDefnsDir, defnsDir);      

      boolean consolidate;
      if(! which.equals("G"))
        consolidate = false;
      else consolidate = true;

      scanDocuments(accCon, ofsaCon, accStmt, ofsaStmt, accRs, ofsaRs, consolidate, scanDateFrom, scanDateTo, financialYearStartDate, previousFinancialYearStartDate, accCodes[0], accValsDr[0], accValsCr[0], numAccs[0], monthEnds, numMonths,
                    debtorsAccount, creditorsAccount, otherDebtorsAccount, otherCreditorsAccount, salesAccount, purchasesAccount, gstOutputAccount, gstInputAccount, exchangeGainAccount, exchangeLossAccount, salesReturnedAccount,
                    purchasesReturnedAccount, stockAccount, discountReceivedAccount, discountGivenAccount, baseCurrency, debtorAccCodes, creditorAccCodes, debtorAccValsDr, debtorAccValsCr, creditorAccValsDr, creditorAccValsCr,
                    numCustomers, numSuppliers, null,       unm, dnm);

      // accValsDr and accValsCr have data for all months of the year (from the effectiveStartDate if appropriate)
      // [12] has the OB values from the OB batch(es)
      // Now, months prior (to the required first month to be shown) must be aggregated into [12], then, on display, those months may be ignored

      for(x=0;x<firstReqdMonthInList;++x)
      {
        for(y=0;y<numAccs[0];++y)
        {
          accValsDr[0][12][y] += generalUtils.doubleDPs(accValsDr[0][x][y], '2');
          accValsCr[0][12][y] += generalUtils.doubleDPs(accValsCr[0][x][y], '2');
        }
      }

      // the Closing column values are derived by totalling all months
      
      for(y=0;y<numAccs[0];++y)
      {
        accValsDr[0][13][y] = accValsDr[0][12][y];
        accValsCr[0][13][y] = accValsCr[0][12][y];
      }

      for(x=0;x<numMonths[0];++x)
      {
        for(y=0;y<numAccs[0];++y)
        {
          accValsDr[0][13][y] += generalUtils.doubleDPs(accValsDr[0][x][y], '2');
          accValsCr[0][13][y] += generalUtils.doubleDPs(accValsCr[0][x][y], '2');
        }
      }

      // debtors or creditor TB

      if(! which.equals("G"))
      {
        for(x=0;x<firstReqdMonthInList;++x)
        {
          for(y=0;y<numCustomers;++y)
          {
            debtorAccValsDr[12][y] += generalUtils.doubleDPs(debtorAccValsDr[x][y], '2');
            debtorAccValsCr[12][y] += generalUtils.doubleDPs(debtorAccValsCr[x][y], '2');
          }

          for(y=0;y<numSuppliers;++y)
          {
            creditorAccValsDr[12][y] += generalUtils.doubleDPs(creditorAccValsDr[x][y], '2');
            creditorAccValsCr[12][y] += generalUtils.doubleDPs(creditorAccValsCr[x][y], '2');
          }
        }

        // the Closing colunm values are derived by totalling all months

        for(y=0;y<numCustomers;++y)
        {
          debtorAccValsDr[13][y] = debtorAccValsDr[12][y];
          debtorAccValsCr[13][y] = debtorAccValsCr[12][y];
        }

        for(y=0;y<numSuppliers;++y)
        {
          creditorAccValsDr[13][y] = creditorAccValsDr[12][y];
          creditorAccValsCr[13][y] = creditorAccValsCr[12][y];
        }

        for(x=0;x<numMonths[0];++x)
        {
          for(y=0;y<numCustomers;++y)
          {
            debtorAccValsDr[13][y] += generalUtils.doubleDPs(debtorAccValsDr[x][y], '2');
            debtorAccValsCr[13][y] += generalUtils.doubleDPs(debtorAccValsCr[x][y], '2');
          }

          for(y=0;y<numSuppliers;++y)
          {
            creditorAccValsDr[13][y] += generalUtils.doubleDPs(creditorAccValsDr[x][y], '2');
            creditorAccValsCr[13][y] += generalUtils.doubleDPs(creditorAccValsCr[x][y], '2');
          }
        }
      }
     
      if(ofsaCon != null) ofsaCon.close();
      if(accCon  != null) accCon.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(ofsaCon != null) ofsaCon.close();
      if(accCon != null)  accCon.close();
    }
  }  

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void scanDocuments(Connection accCon, Connection ofsaCon, Statement accStmt, Statement ofsaStmt, ResultSet accRs, ResultSet ofsaRs, boolean consolidated, String dateFrom, String dateTo, String financialYearStartDate,
                            String previousFinancialYearStartDate, String[] accCodes, double[][] accValsDr, double[][] accValsCr, int numAccs, int[] monthEnds, int[] numMonths, String debtorsAccount, String creditorsAccount,
                            String otherDebtorsAccount, String otherCreditorsAccount, String salesAccount, String purchasesAccount, String gstOutputAccount, String gstInputAccount, String exchangeGainAccount, String exchangeLossAccount,
                            String salesReturnedAccount, String purchasesReturnedAccount, String stockAccount, String discountReceivedAccount, String discountGivenAccount, String baseCurrency, String[] debtorAccCodes, String[] creditorAccCodes,
                            double[][] debtorAccValsDr, double[][] debtorAccValsCr, double[][] creditorAccValsDr, double[][] creditorAccValsCr, int numCustomers, int numSuppliers, String stateFileName,     String unm, String dnm) throws Exception
  {
    // for each month, determine the last day of that month
    numMonths[0] = 0;
    String lastDay;
    int date = generalUtils.encodeFromYYYYMMDD(dateFrom);
    int dateToEncoded = generalUtils.encodeFromYYYYMMDD(dateTo);
    while(date < dateToEncoded)
    {
      lastDay = generalUtils.lastDayOfMonthYYYYMMDD(generalUtils.decodeToYYYYMMDD(date));
      monthEnds[numMonths[0]] = generalUtils.encodeFromYYYYMMDD(lastDay);

      date = monthEnds[numMonths[0]++] + 1; 
    }

    directoryUtils.updateState(stateFileName, "11");

    Statement stmt = null, stmt2 = null;
    ResultSet rs   = null, rs2   = null;

    try
    {
      accStmt = accCon.createStatement();
      
      accRs = accStmt.executeQuery("SELECT Code, OpeningBalances, Type FROM joubatch ORDER BY Code"); 

      String code, type, account;
      boolean wanted;

      while(accRs.next())
      {
        if(accRs.getString(2).equals("Y"))
        {
          code = accRs.getString(1);
          type = accRs.getString(3);

          account = "";
          wanted = true;
          if(type.equals("D"))
          {
            if(! consolidated)
            {
              forOBBatch(ofsaCon, accCon, stmt, stmt2, rs, rs2, type, baseCurrency, code, "", dateFrom, dateTo, debtorAccCodes, debtorAccValsDr, debtorAccValsCr, numCustomers);
              wanted = false;
            }
            else account = debtorsAccount;
          }
          else
          if(type.equals("C"))
          {
            if(! consolidated)
            {
              forOBBatch(ofsaCon, accCon, stmt, stmt2, rs, rs2, type, baseCurrency, code, "", dateFrom, dateTo, creditorAccCodes, creditorAccValsDr, creditorAccValsCr, numSuppliers);
              wanted = false;
            }
            else account = creditorsAccount;
          }

          if(wanted)
            forOBBatch(ofsaCon, accCon, stmt, stmt2, rs, rs2, type, baseCurrency, code, account, dateFrom, dateTo, accCodes, accValsDr, accValsCr, numAccs);
        }
      }
                 
      if(accRs   != null) accRs.close();                                 
      if(accStmt != null) accStmt.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(accRs   != null) accRs.close();                                 
      if(accStmt != null) accStmt.close();
      return;
    }    
       
    directoryUtils.updateState(stateFileName, "15");

    boolean useWAC = false;
    if(miscDefinitions.inventoryCostingMethod(ofsaCon, ofsaStmt, ofsaRs).equals("WAC"))
      useWAC = true;
 
    forInvoiceHeader(ofsaCon, ofsaStmt, ofsaRs, gstOutputAccount, dateFrom, dateTo, accCodes, accValsCr, numAccs, monthEnds);

    directoryUtils.updateState(stateFileName, "18");

    forInvoiceLines(accCon, accStmt, accRs, ofsaCon, ofsaStmt, stmt, ofsaRs, rs, consolidated, useWAC, salesAccount, purchasesAccount, stockAccount, debtorsAccount, otherDebtorsAccount, dateFrom, dateTo, financialYearStartDate, accCodes, accValsDr,
                    accValsCr, numAccs, monthEnds, debtorAccCodes, debtorAccValsDr, stateFileName, "18", numCustomers, dnm);

    directoryUtils.updateState(stateFileName, "20");

    forDebitNoteHeader(ofsaCon, ofsaStmt, ofsaRs, consolidated, salesAccount, debtorsAccount, otherDebtorsAccount, gstOutputAccount, dateFrom, dateTo, accCodes, accValsDr, accValsCr, numAccs, monthEnds, debtorAccCodes, debtorAccValsDr,
                       numCustomers);

    directoryUtils.updateState(stateFileName, "28");

    forSalesCreditNoteHeader(ofsaCon, ofsaStmt, ofsaRs, gstOutputAccount, dateFrom, dateTo, accCodes, accValsDr, numAccs, monthEnds);

    directoryUtils.updateState(stateFileName, "30");

    forSalesCreditNoteLines(ofsaCon, ofsaStmt, ofsaRs, consolidated, salesReturnedAccount, debtorsAccount, otherDebtorsAccount, dateFrom, dateTo, accCodes, accValsDr, accValsCr, numAccs, monthEnds, debtorAccCodes, debtorAccValsCr, numCustomers);

    directoryUtils.updateState(stateFileName, "33");

    forSalesCreditNoteLinesCoSAdjustments(accCon, accStmt, accRs, ofsaCon, ofsaStmt, ofsaRs, useWAC, previousFinancialYearStartDate, purchasesAccount, stockAccount, dateFrom, dateTo, accCodes, accValsDr, accValsCr, numAccs, monthEnds     ,unm, dnm);

    directoryUtils.updateState(stateFileName, "34");

    forPurchaseInvoiceHeader(ofsaCon, ofsaStmt, ofsaRs, gstInputAccount, dateFrom, dateTo, accCodes, accValsDr, numAccs, monthEnds);

    directoryUtils.updateState(stateFileName, "35");

    forPurchaseInvoiceLines(ofsaCon, ofsaStmt, stmt, ofsaRs, rs, consolidated, purchasesAccount, stockAccount, creditorsAccount, otherCreditorsAccount, dateFrom, dateTo, accCodes, accValsDr, accValsCr, numAccs, monthEnds, creditorAccCodes,
                            creditorAccValsCr, stateFileName, "35", numSuppliers);

    directoryUtils.updateState(stateFileName, "38");

    forPurchaseDebitNoteHeader(ofsaCon, ofsaStmt, ofsaRs, consolidated, purchasesAccount, creditorsAccount, otherCreditorsAccount, gstInputAccount, dateFrom, dateTo, accCodes, accValsDr, accValsCr, numAccs, monthEnds, creditorAccCodes,
                               creditorAccValsCr, numSuppliers);

    directoryUtils.updateState(stateFileName, "40");

    forPurchaseCreditNoteHeader(ofsaCon, ofsaStmt, ofsaRs, gstInputAccount, dateFrom, dateTo, accCodes, accValsCr, numAccs, monthEnds);

    directoryUtils.updateState(stateFileName, "43");

    forPurchaseCreditNoteLines(ofsaCon, ofsaStmt, ofsaRs, consolidated, purchasesReturnedAccount, creditorsAccount, otherCreditorsAccount, dateFrom, dateTo, accCodes, accValsDr, accValsCr, numAccs, monthEnds,
                               creditorAccCodes, creditorAccValsDr, numSuppliers);

    directoryUtils.updateState(stateFileName, "45");

    forPaymentVoucherHeader(accCon, ofsaCon, ofsaStmt, stmt2, ofsaRs, rs2, baseCurrency, gstInputAccount, dateFrom, dateTo, accCodes, accValsDr, accValsCr, numAccs, monthEnds);

    directoryUtils.updateState(stateFileName, "48");

    forPaymentVoucherLines(accCon, ofsaCon, ofsaStmt, stmt2, ofsaRs, rs2, baseCurrency, dateFrom, dateTo, accCodes, accValsDr, numAccs, monthEnds);

    directoryUtils.updateState(stateFileName, "50");

    forReceiptVoucherHeader(accCon, ofsaCon, ofsaStmt, stmt2, ofsaRs, rs2, baseCurrency, gstOutputAccount, dateFrom, dateTo, accCodes, accValsDr, accValsCr, numAccs, monthEnds);

    directoryUtils.updateState(stateFileName, "55");

    forReceiptVoucherLines(accCon, ofsaCon, ofsaStmt, stmt2, ofsaRs, rs2, baseCurrency, dateFrom, dateTo, accCodes, accValsCr, numAccs, monthEnds);

    directoryUtils.updateState(stateFileName, "60");
   
    forPaymentHeader(ofsaCon, accCon, ofsaStmt, ofsaRs, consolidated, creditorsAccount, otherCreditorsAccount, discountReceivedAccount, discountGivenAccount, baseCurrency, dateFrom, dateTo, accCodes, accValsDr, accValsCr, numAccs, monthEnds, creditorAccCodes, creditorAccValsDr, numSuppliers);

    directoryUtils.updateState(stateFileName, "65");
    
    forReceiptHeader(ofsaCon, accCon, ofsaStmt, ofsaRs, consolidated, debtorsAccount, otherDebtorsAccount, discountReceivedAccount, discountGivenAccount, baseCurrency, dateFrom, dateTo, accCodes, accValsDr, accValsCr, numAccs, monthEnds, debtorAccCodes, debtorAccValsCr, numCustomers);

    directoryUtils.updateState(stateFileName, "70");

    forPaymentExchangeGainLoss(ofsaCon, ofsaStmt, ofsaRs, consolidated, exchangeGainAccount, exchangeLossAccount, creditorsAccount, otherCreditorsAccount, dateFrom, dateTo, accCodes, accValsDr, accValsCr, numAccs,
                               monthEnds, creditorAccCodes, creditorAccValsDr, creditorAccValsCr, numSuppliers);

    directoryUtils.updateState(stateFileName, "77");

    forReceiptExchangeGainLoss(ofsaCon, ofsaStmt, ofsaRs, consolidated, exchangeGainAccount, exchangeLossAccount, debtorsAccount, otherDebtorsAccount, dateFrom, dateTo, accCodes, accValsDr, accValsCr, debtorAccCodes, debtorAccValsDr, debtorAccValsCr, numAccs, monthEnds, numCustomers);

    directoryUtils.updateState(stateFileName, "82");

    forIAT(ofsaCon, accCon, ofsaStmt, ofsaRs, accCodes, accValsDr, accValsCr, dateFrom, dateTo, baseCurrency, numAccs, monthEnds);

    directoryUtils.updateState(stateFileName, "85");

    try
    {
      accStmt = accCon.createStatement();
      
      accRs = accStmt.executeQuery("SELECT Code, OpeningBalances, Type FROM joubatch ORDER BY Code"); 

      String code, type, account;
      boolean wanted;

      while(accRs.next())
      {
        if(! accRs.getString(2).equals("Y"))
        {
          code = accRs.getString(1);
          type = accRs.getString(3);

          account = "";
          wanted = true;
          if(type.equals("D"))
          {
            if(! consolidated)
            {
              forBatch(ofsaCon, accCon, stmt, stmt2, rs, rs2, type, baseCurrency, code, "", dateFrom, dateTo, debtorAccCodes, debtorAccValsDr, debtorAccValsCr, numCustomers, monthEnds);
              wanted = false;
            }
            else account = debtorsAccount;
          }
          else
          if(type.equals("C"))
          {
            if(! consolidated)
            {
              forBatch(ofsaCon, accCon, stmt, stmt2, rs, rs2, type, baseCurrency, code, "", dateFrom, dateTo, creditorAccCodes, creditorAccValsDr, creditorAccValsCr, numSuppliers, monthEnds);
              wanted = false;
            }
            else account = creditorsAccount;
          }

          if(wanted)
          {
            forBatch(ofsaCon, accCon, stmt, stmt2, rs, rs2, type, baseCurrency, code, account, dateFrom, dateTo, accCodes, accValsDr, accValsCr, numAccs, monthEnds);
          }
        }
      }
     
      if(accRs   != null) accRs.close();                                 
      if(accStmt != null) accStmt.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(accRs   != null) accRs.close();                                 
      if(accStmt != null) accStmt.close();
    }
    directoryUtils.updateState(stateFileName, "89");


    if(rs   != null) rs.close();                                 
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void updateValueForAccount(String which, String date, String accCode, double val, String[] accCodes, double[][] accVals, int numAccs, int[] monthEnds) throws Exception
  {
    int x=0;

    accCode = generalUtils.deSanitise(accCode);

    while(x < numAccs && ! accCodes[x].equals(accCode))
      ++x;

    if(x == numAccs) // accCode not found!
      System.out.println(which + " Account Code Not Found: >" + accCode + "< " + date + " " + val);
    else
    {
      val = generalUtils.doubleDPs(val, '2');

      if(which.startsWith("RX"))    System.out.println(which + " " + date + " " + accCode +  " " + val);

      int dateEncoded = generalUtils.encodeFromYYYYMMDD(date);

      if(dateEncoded <= monthEnds[0])  accVals[0][x]  += val; else
      if(dateEncoded <= monthEnds[1])  accVals[1][x]  += val; else
      if(dateEncoded <= monthEnds[2])  accVals[2][x]  += val; else
      if(dateEncoded <= monthEnds[3])  accVals[3][x]  += val; else
      if(dateEncoded <= monthEnds[4])  accVals[4][x]  += val; else
      if(dateEncoded <= monthEnds[5])  accVals[5][x]  += val; else
      if(dateEncoded <= monthEnds[6])  accVals[6][x]  += val; else
      if(dateEncoded <= monthEnds[7])  accVals[7][x]  += val; else
      if(dateEncoded <= monthEnds[8])  accVals[8][x]  += val; else
      if(dateEncoded <= monthEnds[9])  accVals[9][x]  += val; else
      if(dateEncoded <= monthEnds[10]) accVals[10][x] += val; else accVals[11][x] += val;
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void updateValueForAccountIndividualDebtorOrCreditor(String which, String date, String accCode, double val, String[] debtorOrCreditorAccCodes, double[][] accVals, int numAccs, int[] monthEnds) throws Exception
  {
    int x=0;

    accCode = generalUtils.deSanitise(accCode);

    while(x < numAccs && ! debtorOrCreditorAccCodes[x].equals(accCode))
      ++x;

    if(x == numAccs) // accCode not found!
      System.out.println(which + " Account Code Not Found: >" + accCode + "< " + date + " " + val);
    else
    {
      val = generalUtils.doubleDPs(val, '2');

      int dateEncoded = generalUtils.encodeFromYYYYMMDD(date);
      if(dateEncoded <= monthEnds[0])  accVals[0][x]  += val; else
      if(dateEncoded <= monthEnds[1])  accVals[1][x]  += val; else
      if(dateEncoded <= monthEnds[2])  accVals[2][x]  += val; else
      if(dateEncoded <= monthEnds[3])  accVals[3][x]  += val; else
      if(dateEncoded <= monthEnds[4])  accVals[4][x]  += val; else
      if(dateEncoded <= monthEnds[5])  accVals[5][x]  += val; else
      if(dateEncoded <= monthEnds[6])  accVals[6][x]  += val; else
      if(dateEncoded <= monthEnds[7])  accVals[7][x]  += val; else
      if(dateEncoded <= monthEnds[8])  accVals[8][x]  += val; else
      if(dateEncoded <= monthEnds[9])  accVals[9][x]  += val; else
      if(dateEncoded <= monthEnds[10]) accVals[10][x] += val; else accVals[11][x] += val;
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void updateValueForAccountOB(String which, String accCode, double valBase, String[] accCodes, double[][] accValsBase, int numAccs) throws Exception
  {
    int x=0;

    accCode = generalUtils.deSanitise(accCode);

    while(x < numAccs && ! accCodes[x].equals(accCode))
      ++x;
    
    if(x == numAccs) // accCode not found!
      System.out.println(which + " Account Code Not Found: >" + accCode + "<");
    else
    {
      accValsBase[12][x] += generalUtils.doubleDPs(valBase, '2');
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void forOBBatch(Connection ofsaCon, Connection accCon, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String type, String baseCurrency, String code, String account, String dateFrom, String dateTo, String[] accCodes,
                          double[][] accValsDrBase, double[][] accValsCrBase, int numAccs) throws Exception
  {
    stmt = accCon.createStatement();

    rs = stmt.executeQuery("SELECT AccCode, BaseAmount, DrCr, Amount FROM joubatchl WHERE Code = '" + generalUtils.sanitiseForSQL(code) + "' AND TransactionDate >= {d '" + dateFrom + "'} AND TransactionDate <= {d '" + dateTo + "'}");
    String useAccount, accountCurrency;
  
    while(rs.next())
    {
      if(account.length() == 0)
        useAccount = rs.getString(1);
      else useAccount = account;
        
      if(rs.getString(3).equals("D")) // Dr
      {
        updateValueForAccountOB("B", useAccount, generalUtils.doubleFromStr(rs.getString(2)), accCodes, accValsDrBase, numAccs);
      }
      else
      {
        updateValueForAccountOB("B", useAccount, generalUtils.doubleFromStr(rs.getString(2)), accCodes, accValsCrBase, numAccs);
      }
    }

    if(rs   != null) rs.close();                                 
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void forPaymentVoucherHeader(Connection accCon, Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String baseCurrency, String gstInputAccount, String dateFrom, String dateTo, String[] accCodes,
                                       double[][] accValsDr, double[][] accValsCr, int numAccs, int[] monthEnds) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT AccountCr, BaseTotalTotal, BaseGSTTotal, Date, TotalTotal, GSTTotal, Currency FROM voucher WHERE Status != 'C' AND Date >= {d '" + dateFrom + "'} AND Date <= {d '" + dateTo + "'}");
    
    double baseGSTTotal;
    String accountCurrency;
    
    while(rs.next())
    {
      accountCurrency = currencyForAccount(accCon, stmt2, rs2, rs.getString(1));

      updateValueForAccount("PV", rs.getString(4), rs.getString(1), generalUtils.doubleFromStr(rs.getString(2)), accCodes, accValsCr, numAccs, monthEnds);
     
      baseGSTTotal = generalUtils.doubleFromStr(rs.getString(3));
      if(baseGSTTotal != 0.0)
        updateValueForAccount("PV", rs.getString(4), gstInputAccount, baseGSTTotal, accCodes, accValsDr, numAccs, monthEnds);
    }      

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void forPaymentVoucherLines(Connection accCon, Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String baseCurrency, String dateFrom, String dateTo, String[] accCodes,
                                      double[][] accValsDr, int numAccs, int[] monthEnds) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.AccountDr, t2.Amount, t1.Date, t2.Amount2 FROM voucherl AS t2 INNER JOIN voucher AS t1 ON t1.VoucherCode = t2.VoucherCode WHERE t1.Status != 'C' AND t1.Date >= {d '" + dateFrom + "'} AND t1.Date <= {d '"
                         + dateTo + "'}");

    String accountCurrency;
    
    while(rs.next())
    {
      accountCurrency = currencyForAccount(accCon, stmt2, rs2, rs.getString(1));

      updateValueForAccount("PVL", rs.getString(3), rs.getString(1), generalUtils.doubleFromStr(rs.getString(2)), accCodes, accValsDr, numAccs, monthEnds);
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void forReceiptVoucherHeader(Connection accCon, Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String baseCurrency, String gstOutputAccount, String dateFrom, String dateTo,
                                       String[] accCodes, double[][] accValsDr, double[][] accValsCr, int numAccs, int[] monthEnds) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT AccountDr, BaseTotalTotal, BaseGSTTotal, Date, TotalTotal, GSTTotal FROM rvoucher WHERE Status != 'C' AND Date >= {d '" + dateFrom
                         + "'} AND Date <= {d '" + dateTo + "'}");
    double baseGSTTotal;
    String accountCurrency;
    
    while(rs.next())
    {
      accountCurrency = currencyForAccount(accCon, stmt2, rs2, rs.getString(1));

      updateValueForAccount("RV", rs.getString(4), rs.getString(1), generalUtils.doubleFromStr(rs.getString(2)), accCodes, accValsDr, numAccs, monthEnds);
     
      baseGSTTotal = generalUtils.doubleFromStr(rs.getString(3));
      if(baseGSTTotal != 0.0)
        updateValueForAccount("RV", rs.getString(4), gstOutputAccount, baseGSTTotal, accCodes, accValsCr, numAccs, monthEnds);
    }      

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void forReceiptVoucherLines(Connection accCon, Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String baseCurrency, String dateFrom, String dateTo, String[] accCodes,
                                      double[][] accValsCr, int numAccs, int[] monthEnds) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.AccountCr, t2.Amount, t1.Date, t2.Amount2 FROM rvoucherl AS t2 INNER JOIN rvoucher AS t1 ON t1.VoucherCode = t2.VoucherCode "
                         + "WHERE t1.Status != 'C' AND t1.Date >= {d '" + dateFrom + "'} AND t1.Date <= {d '" + dateTo + "'}");
    String accountCurrency;
    
    while(rs.next())
    {
      accountCurrency = currencyForAccount(accCon, stmt2, rs2, rs.getString(1));
 
      updateValueForAccount("RVL", rs.getString(3), rs.getString(1), generalUtils.doubleFromStr(rs.getString(2)), accCodes, accValsCr, numAccs, monthEnds);
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void forPaymentHeader(Connection con, Connection accCon, Statement stmt, ResultSet rs, boolean consolidate, String creditorsAccount, String otherCreditorsAccount, String discountReceivedAccount,
                                String discountGivenAccount, String baseCurrency, String dateFrom, String dateTo, String[] accCodes, double[][] accValsDr, double[][] accValsCr, int numAccs,
                                int[] monthEnds, String[] creditorAccCodes, double[][] creditorAccValsDr, int numSuppliers)
                                throws Exception
  {
    Statement stmt2 = null;
    ResultSet rs2   = null;

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT AccCredited, CompanyCode, BankAmount, Charges, Rate, Currency, DatePaid, DiscountAllowed, PaymentCode FROM payment "
                         + "WHERE Status != 'C' AND DatePaid >= {d '" + dateFrom + "'} AND DatePaid <= {d '" + dateTo + "'}");
        
    String chargesAccount, bankAccountCurrency, datePaid, companyCode;
    double charges, chargesAsBaseAmount, discountAllowed, rate, bankAmount, bankAmountAsBaseAmountWithDiscountAllowed, bankAmountWithDiscountAllowed;

    while(rs.next())
    {
      bankAccountCurrency = currencyForAccount(accCon, stmt2, rs2, rs.getString(1));
      chargesAccount      = chargesAccountForCurrency(accCon, bankAccountCurrency);
      rate                = generalUtils.doubleFromStr(rs.getString(5));
      discountAllowed     = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(8)), '2');
      bankAmount          = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(3)), '2');
      datePaid            = rs.getString(7);
      companyCode         = rs.getString(2);

      // if the bankAccountCurrency is the baseCurrency then charges are already base; else must use the payment rate to make charges a base amount
      charges = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(4)), '2');
      if(! bankAccountCurrency.equals(baseCurrency))
      {
        chargesAsBaseAmount = charges * rate;
        chargesAsBaseAmount = generalUtils.doubleDPs(chargesAsBaseAmount, '2');
      }
      else chargesAsBaseAmount = charges;
      
      bankAmountAsBaseAmountWithDiscountAllowed = bankAmount + discountAllowed;
      
      bankAmount *= rate;
      
      bankAmount = generalUtils.doubleDPs(bankAmount, '2');
      bankAmountAsBaseAmountWithDiscountAllowed *= rate;
      bankAmountAsBaseAmountWithDiscountAllowed = generalUtils.doubleDPs(bankAmountAsBaseAmountWithDiscountAllowed, '2');

      bankAmountWithDiscountAllowed = bankAmount + discountAllowed;
      bankAmountWithDiscountAllowed = generalUtils.doubleDPs(bankAmountWithDiscountAllowed, '2');

      if(consolidate)
      {
        if(! existsSupplier(con, stmt2, rs2, companyCode))
          updateValueForAccount("P ",datePaid, otherCreditorsAccount, bankAmountAsBaseAmountWithDiscountAllowed, accCodes, accValsDr, numAccs, monthEnds);
        else updateValueForAccount("P ",datePaid, creditorsAccount, bankAmountAsBaseAmountWithDiscountAllowed, accCodes, accValsDr, numAccs, monthEnds);
      }
      else
      {
        if(! existsSupplier(con, stmt2, rs2, companyCode))
        {
          updateValueForAccount("P ",datePaid, otherCreditorsAccount, bankAmountAsBaseAmountWithDiscountAllowed, accCodes, accValsDr, numAccs, monthEnds);
        }
        else
        {
          updateValueForAccountIndividualDebtorOrCreditor("P ",datePaid, rs.getString(2), bankAmountAsBaseAmountWithDiscountAllowed, creditorAccCodes, creditorAccValsDr, numSuppliers, monthEnds);
        }
      }

      updateValueForAccount("P Ch" ,datePaid, chargesAccount, chargesAsBaseAmount, accCodes, accValsDr, numAccs, monthEnds);

      updateValueForAccount("P Ch 2", datePaid, rs.getString(1), chargesAsBaseAmount, accCodes, accValsCr, numAccs, monthEnds);
      
      // make adjustments to compensate for any errors in data entry
      discountAllowed = bankAmountAsBaseAmountWithDiscountAllowed - bankAmount;
      discountAllowed = generalUtils.doubleDPs(discountAllowed, '2');

      if(discountAllowed < 0) // overpayment
        updateValueForAccount("P 2",rs.getString(7), discountGivenAccount, (discountAllowed * -1), accCodes, accValsDr, numAccs, monthEnds);///
      else updateValueForAccount("P 3",rs.getString(7), discountReceivedAccount, discountAllowed, accCodes, accValsCr, numAccs, monthEnds);///

      updateValueForAccount("P 1",rs.getString(7), rs.getString(1), bankAmount, accCodes, accValsCr, numAccs, monthEnds);///
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void forPaymentExchangeGainLoss(Connection con, Statement stmt, ResultSet rs, boolean consolidate, String exchangeGainAccount, String exchangeLossAccount, String creditorsAccount, String otherCreditorsAccount,
                                          String dateFrom, String dateTo, String[] accCodes, double[][] accValsDr, double[][] accValsCr, int numAccs, int[] monthEnds, 
                                          String[] creditorAccCodes, double[][] creditorAccValsDr, double[][] creditorAccValsCr, int numSuppliers) throws Exception
  {
    Statement stmt2 = null;
    ResultSet rs2   = null;

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t1.Rate, t2.OriginalRate, t2.AmountPaid, t1.DatePaid, t1.CompanyCode, t2.PaymentCode, t1.Currency FROM paymentl AS t2 INNER JOIN payment AS t1 ON t1.PaymentCode = t2.PaymentCode "
                         + "WHERE t1.Status != 'C' AND t1.DatePaid >= {d '" + dateFrom + "'} AND t1.DatePaid <= {d '" + dateTo + "'} ORDER BY t2.PaymentCode");
    
    String companyCode="", paymentCode, lastPaymentCode = "", datePaid = "", currency, lastCurrency = "";
    double amount, rate, originalRate, diff, runningDiff = 0.0;
    boolean first = true;
            
    while(rs.next())
    { 
      paymentCode = rs.getString(6);
      currency    = rs.getString(7);
      
      if(! paymentCode.equals(lastPaymentCode))
      {
        if(! first)
        {
          if(runningDiff > 0) // exch loss
          {  
            updateValueForAccount("PX 2",datePaid, exchangeLossAccount, runningDiff, accCodes, accValsDr, numAccs, monthEnds);

            if(consolidate)
            {
              if(! existsSupplier(con, stmt2, rs2, companyCode))
                updateValueForAccount("PX 1a",datePaid, otherCreditorsAccount, runningDiff, accCodes, accValsCr, numAccs, monthEnds);
              else updateValueForAccount("PX 1c",datePaid, creditorsAccount, runningDiff, accCodes, accValsCr, numAccs, monthEnds);
            }
            else
            {
              if(! existsSupplier(con, stmt2, rs2, companyCode))
                updateValueForAccount("PX 1d",datePaid, otherCreditorsAccount, runningDiff, accCodes, accValsCr, numAccs, monthEnds);
              else updateValueForAccountIndividualDebtorOrCreditor("PX 1f",datePaid, companyCode, runningDiff, creditorAccCodes, creditorAccValsCr, numSuppliers, monthEnds);
            }
          }
          else
          if(runningDiff < 0) // exch gain
          {
            updateValueForAccount("PX 4",datePaid, exchangeGainAccount, (runningDiff * -1), accCodes, accValsCr, numAccs, monthEnds);

            if(consolidate)
            {
              if(! existsSupplier(con, stmt2, rs2, companyCode))
                updateValueForAccount("PX 3",datePaid, otherCreditorsAccount, (runningDiff * -1), accCodes, accValsDr, numAccs, monthEnds);
              else updateValueForAccount("PX 3",datePaid, creditorsAccount, (runningDiff * -1), accCodes, accValsDr, numAccs, monthEnds);
            }
            else
            {
              if(! existsSupplier(con, stmt2, rs2, companyCode))
                updateValueForAccount("PX 3",datePaid, otherCreditorsAccount, (runningDiff * -1), accCodes, accValsDr, numAccs, monthEnds);
              else updateValueForAccountIndividualDebtorOrCreditor("PX 3",datePaid, companyCode, (runningDiff * -1), creditorAccCodes, creditorAccValsDr, numSuppliers, monthEnds);
            }
          }

          runningDiff = 0.0;
        }
        else first = false;

        lastPaymentCode = paymentCode;
        lastCurrency    = currency;
      }
        
      companyCode  = rs.getString(5);
      datePaid     = rs.getString(4);
      rate         = generalUtils.doubleFromStr(rs.getString(1));
      originalRate = generalUtils.doubleFromStr(rs.getString(2));
      
      if(rate != originalRate)
      {
        amount = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(3)), '2');

         //     100 * 3.1         100 * 3.06
        diff = (amount * rate) - (amount * originalRate);
        runningDiff += generalUtils.doubleDPs(diff, '2');
      }
    }

    if(runningDiff != 0.0)
    {
      if(runningDiff > 0) // exch loss
      {  
        updateValueForAccount("PX 2",datePaid, exchangeLossAccount, runningDiff, accCodes, accValsDr, numAccs, monthEnds);

        if(consolidate)
        {
          if(! existsSupplier(con, stmt2, rs2, companyCode))
            updateValueForAccount("PX 1",datePaid, otherCreditorsAccount, runningDiff, accCodes, accValsCr, numAccs, monthEnds);
          else updateValueForAccount("PX 1",datePaid, creditorsAccount, runningDiff, accCodes, accValsCr, numAccs, monthEnds);
        }
        else
        {
          if(! existsSupplier(con, stmt2, rs2, companyCode))
            updateValueForAccount("PX 1",datePaid, otherCreditorsAccount, runningDiff, accCodes, accValsCr, numAccs, monthEnds);
          else updateValueForAccountIndividualDebtorOrCreditor("PX 1",datePaid, companyCode, runningDiff, creditorAccCodes, creditorAccValsCr, numSuppliers, monthEnds);
        }
      }
      else
      if(runningDiff < 0) // exch gain
      {
        updateValueForAccount("PX 4",datePaid, exchangeGainAccount, (runningDiff * -1), accCodes, accValsCr, numAccs, monthEnds);

        if(consolidate)
        {
          if(! existsSupplier(con, stmt2, rs2, companyCode))
            updateValueForAccount("PX 3",datePaid, otherCreditorsAccount, (runningDiff * -1), accCodes, accValsDr, numAccs, monthEnds);
          else updateValueForAccount("aPX 3",datePaid, creditorsAccount, (runningDiff * -1), accCodes, accValsDr, numAccs, monthEnds);
        }
        else
        {
          if(! existsSupplier(con, stmt2, rs2, companyCode))
            updateValueForAccount("PX 3",datePaid, otherCreditorsAccount, (runningDiff * -1), accCodes, accValsDr, numAccs, monthEnds);
          else updateValueForAccountIndividualDebtorOrCreditor("PX 3",datePaid, companyCode, (runningDiff * -1), creditorAccCodes, creditorAccValsDr, numSuppliers, monthEnds);
        }
      }
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void forReceiptExchangeGainLoss(Connection con, Statement stmt, ResultSet rs, boolean consolidate, String exchangeGainAccount, String exchangeLossAccount, String debtorsAccount, String otherDebtorsAccount,
                                          String dateFrom, String dateTo, String[] accCodes, double[][] accValsDr, double[][] accValsCr, String[] debtorAccCodes, double[][] debtorAccValsDr,
                                          double[][] debtorAccValsCr, int numAccs, int[] monthEnds, int numCustomers) throws Exception
  {
    Statement stmt2 = null;
    ResultSet rs2   = null;

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t1.Rate, t2.OriginalRate, t2.AmountReceived, t1.DateReceived, t1.CompanyCode, t2.ReceiptCode, t1.Currency FROM receiptl AS t2 INNER JOIN receipt AS t1 ON t1.ReceiptCode = t2.ReceiptCode "
                          +"WHERE t1.Status != 'C' AND t1.DateReceived >= {d '" + dateFrom + "'} AND t1.DateReceived <= {d '" + dateTo + "'} ORDER BY t2.ReceiptCode");
    
    String companyCode="", receiptCode, lastReceiptCode = "", dateReceived="";
    double amount, rate, originalRate, diff, runningDiff = 0.0;
    boolean first = true;

    while(rs.next())
    { 
      receiptCode = rs.getString(6);
      
      if(! receiptCode.equals(lastReceiptCode))
      {
        if(! first)
        {
          if(runningDiff < 0) // exch loss
          {
            updateValueForAccount("RX 1",dateReceived, exchangeLossAccount, (runningDiff * -1), accCodes, accValsDr, numAccs, monthEnds);

            if(consolidate)
            {
              if(existsCustomer(con, stmt2, rs2, companyCode))
                updateValueForAccount("RX 3",dateReceived, debtorsAccount, (runningDiff * -1), accCodes, accValsCr, numAccs, monthEnds);
              else updateValueForAccount("RX 5",dateReceived, otherDebtorsAccount, (runningDiff * -1), accCodes, accValsCr, numAccs, monthEnds);
            }
            else
            {
              if(existsCustomer(con, stmt2, rs2, companyCode))
                updateValueForAccountIndividualDebtorOrCreditor("RX 61",dateReceived, companyCode, (runningDiff * -1), debtorAccCodes, debtorAccValsCr, numCustomers, monthEnds);
              else updateValueForAccount("RX 51",dateReceived, otherDebtorsAccount, (runningDiff * -1), accCodes, accValsCr, numAccs, monthEnds);
            }
          }
    
          if(runningDiff > 0) // exch gain
          {
            updateValueForAccount("RX 2",dateReceived, exchangeGainAccount, runningDiff, accCodes, accValsCr, numAccs, monthEnds);

            if(consolidate)
            {
              if(existsCustomer(con, stmt2, rs2, companyCode))
                updateValueForAccount("RX 4",dateReceived, debtorsAccount, runningDiff, accCodes, accValsDr, numAccs, monthEnds);
              else updateValueForAccount("RX 6",dateReceived, otherDebtorsAccount, runningDiff, accCodes, accValsDr, numAccs, monthEnds);
            }
            else
            {
              if(existsCustomer(con, stmt2, rs2, companyCode))
                updateValueForAccountIndividualDebtorOrCreditor("RX 61",dateReceived, companyCode, runningDiff, debtorAccCodes, debtorAccValsDr, numCustomers, monthEnds);
              else updateValueForAccount("RX 51",dateReceived, otherDebtorsAccount, runningDiff, accCodes, accValsDr, numAccs, monthEnds);
            }
          }
          
          runningDiff = 0.0;
        }
        else first = false;

        lastReceiptCode = receiptCode;
      }
      
      companyCode  = rs.getString(5);
      dateReceived = rs.getString(4);
      rate         = generalUtils.doubleFromStr(rs.getString(1));
      originalRate = generalUtils.doubleFromStr(rs.getString(2));
            
      if(rate != originalRate)
      {
        amount = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(3)), '2');
        
        //      100 * 1.53        100 * 1.54   
        diff = (amount * rate) - (amount * originalRate);
        runningDiff += generalUtils.doubleDPs(diff, '2');
      }
    }

    if(runningDiff != 0.0)
    {
      if(runningDiff < 0) // exch loss
      {
        updateValueForAccount("RX 1",dateReceived, exchangeLossAccount, (runningDiff * -1), accCodes, accValsDr, numAccs, monthEnds);///

        if(consolidate)
        {
          if(existsCustomer(con, stmt2, rs2, companyCode))
            updateValueForAccount("RX 3",dateReceived, debtorsAccount, (runningDiff * -1), accCodes, accValsCr, numAccs, monthEnds);///
          else updateValueForAccount("RX 5",dateReceived, otherDebtorsAccount, (runningDiff * -1), accCodes, accValsCr, numAccs, monthEnds);///
        }
        else
        {
          if(existsCustomer(con, stmt2, rs2, companyCode))
            updateValueForAccountIndividualDebtorOrCreditor("RX 61",dateReceived, companyCode, (runningDiff * -1), debtorAccCodes, debtorAccValsCr, numCustomers, monthEnds);
          else updateValueForAccount("RX 51",dateReceived, otherDebtorsAccount, (runningDiff * -1), accCodes, accValsCr, numAccs, monthEnds);///
        }
      }
      else
      if(runningDiff > 0) // exch gain
      {
        updateValueForAccount("RX 2",dateReceived, exchangeGainAccount, runningDiff, accCodes, accValsCr, numAccs, monthEnds);///

        if(consolidate)
        {
          if(existsCustomer(con, stmt2, rs2, companyCode))
            updateValueForAccount("RX 4",dateReceived, debtorsAccount, runningDiff, accCodes, accValsDr, numAccs, monthEnds);///
          else updateValueForAccount("RX 6",dateReceived, otherDebtorsAccount, runningDiff, accCodes, accValsDr, numAccs, monthEnds);///
        }
        else
        {
          if(existsCustomer(con, stmt2, rs2, companyCode))
            updateValueForAccountIndividualDebtorOrCreditor("RX 61",dateReceived, companyCode, runningDiff, debtorAccCodes, debtorAccValsDr, numCustomers, monthEnds);
          else updateValueForAccount("RX 51",dateReceived, otherDebtorsAccount, runningDiff, accCodes, accValsDr, numAccs, monthEnds);///
        }
      }
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void forReceiptHeader(Connection con, Connection accCon, Statement stmt, ResultSet rs, boolean consolidate, String debtorsAccount, String otherDebtorsAccount, String discountReceivedAccount, String discountGivenAccount,
                                String baseCurrency, String dateFrom, String dateTo, String[] accCodes, double[][] accValsDr, double[][] accValsCr, int numAccs, int[] monthEnds,
                                String[] debtorAccCodes, double[][] debtorAccValsCr, int numCustomers) throws Exception
  {
    Statement stmt2 = null;
    ResultSet rs2   = null;

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT AccDebited, CompanyCode, BankedAmount, Charges, Rate, Currency, DateReceived, DiscountAllowed, ReceiptCode FROM receipt "
                         + "WHERE Status != 'C' AND DateReceived >= {d '" + dateFrom + "'} AND DateReceived <= {d '" + dateTo + "'}");
    
    String chargesAccount, bankAccountCurrency, dateReceived, companyCode;
    double chargesAsBaseAmount, bankedAmountWithCharges, discountAllowed, charges, rate, bankedAmount, bankedAmountAsBaseAmountWithDiscountAllowed, bankedAmountWithDiscountAllowed;
            
    while(rs.next())
    {
      bankAccountCurrency = currencyForAccount(accCon, stmt2, rs2, rs.getString(1));
      chargesAccount      = chargesAccountForCurrency(accCon, bankAccountCurrency);
      rate                = generalUtils.doubleFromStr(rs.getString(5));
      discountAllowed     = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(8)), '2');
      bankedAmount        = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(3)), '2');
      dateReceived        = rs.getString(7);
      companyCode         = rs.getString(2);
     
      // if the bankAccountCurrency is the baseCurrency then charges are already base; else must use the receipt rate to make charges a base amount
      charges = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(4)), '2');
      if(! bankAccountCurrency.equals(baseCurrency))
      {
        chargesAsBaseAmount = charges * rate;
        chargesAsBaseAmount = generalUtils.doubleDPs(chargesAsBaseAmount, '2');
      }
      else chargesAsBaseAmount = charges;

      bankedAmountWithCharges = bankedAmount + charges;
      bankedAmountAsBaseAmountWithDiscountAllowed = bankedAmountWithCharges + discountAllowed;

      bankedAmountWithCharges *= rate;

      bankedAmountWithCharges = generalUtils.doubleDPs(bankedAmountWithCharges, '2');
      
      bankedAmountAsBaseAmountWithDiscountAllowed *= rate;
      bankedAmountAsBaseAmountWithDiscountAllowed = generalUtils.doubleDPs(bankedAmountAsBaseAmountWithDiscountAllowed, '2');

      bankedAmountWithDiscountAllowed = bankedAmountWithCharges + discountAllowed;
      bankedAmountWithDiscountAllowed = generalUtils.doubleDPs(bankedAmountWithDiscountAllowed, '2');

      if(consolidate)
      {
        if(! existsCustomer(con, stmt2, rs2, companyCode))
          updateValueForAccount("R",dateReceived, otherDebtorsAccount, bankedAmountAsBaseAmountWithDiscountAllowed, accCodes, accValsCr, numAccs, monthEnds);
        else updateValueForAccount("R",dateReceived, debtorsAccount, bankedAmountAsBaseAmountWithDiscountAllowed, accCodes, accValsCr, numAccs, monthEnds);
      }
      else
      {
        if(! existsCustomer(con, stmt2, rs2, companyCode))
          updateValueForAccount("R",dateReceived, otherDebtorsAccount, bankedAmountAsBaseAmountWithDiscountAllowed, accCodes, accValsCr, numAccs, monthEnds);
        else updateValueForAccountIndividualDebtorOrCreditor("R",dateReceived, rs.getString(2), bankedAmountAsBaseAmountWithDiscountAllowed, debtorAccCodes, debtorAccValsCr, numCustomers, monthEnds);
      }
      
      updateValueForAccount("R Ch",dateReceived, chargesAccount, chargesAsBaseAmount, accCodes, accValsDr, numAccs, monthEnds);

      updateValueForAccount("R Ch 2",dateReceived, rs.getString(1), chargesAsBaseAmount, accCodes, accValsCr, numAccs, monthEnds);

      // make adjustments to compensate for any errors in data entry
      discountAllowed = bankedAmountAsBaseAmountWithDiscountAllowed - bankedAmountWithCharges;
      discountAllowed = generalUtils.doubleDPs(discountAllowed, '2');

      if(discountAllowed < 0) // overpayment
        updateValueForAccount("R 2",dateReceived, discountReceivedAccount, (discountAllowed * -1), accCodes, accValsCr, numAccs, monthEnds);///
      else updateValueForAccount("R 3",dateReceived, discountGivenAccount, discountAllowed, accCodes, accValsDr, numAccs, monthEnds);///

      updateValueForAccount("R 1",dateReceived, rs.getString(1), bankedAmountWithCharges, accCodes, accValsDr, numAccs, monthEnds);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String chargesAccountForCurrency(Connection accCon, String currency) throws Exception
  {
    Statement stmt = null;
    ResultSet rs   = null;

    stmt = accCon.createStatement();

    rs = stmt.executeQuery("SELECT AccCode FROM acctdefn WHERE Category = 'Expenses - Bank Charges' AND Active = 'Y' AND Currency = '" + currency
                         + "'"); 
    String accCode = "";
      
    if(rs.next())
      accCode = rs.getString(1);
    else System.out.println("no charges account for " + currency);   
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    return accCode;
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String currencyForAccount(Connection accCon, Statement stmt, ResultSet rs, String account) throws Exception
  {
    stmt = accCon.createStatement();

    rs = stmt.executeQuery("SELECT Currency FROM acctdefn WHERE AccCode = '" + account + "'");
    String currency = "";

    if(rs.next())
      currency = rs.getString(1);
    else System.out.println("no currency for " + account);

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    return currency;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void forInvoiceHeader(Connection con, Statement stmt, ResultSet rs, String gstOutputAccount, String dateFrom, String dateTo, String[] accCodes, double[][] accValsCr, int numAccs, int[] monthEnds) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT BaseGSTTotal, Date, GSTTotal FROM invoice WHERE Status != 'C' AND Date >= {d '" + dateFrom + "'} AND Date <= {d '" + dateTo + "'}");

    double baseGSTTotal;

    while(rs.next())
    {
      baseGSTTotal = generalUtils.doubleFromStr(rs.getString(1));

      if(baseGSTTotal != 0.0)
        updateValueForAccount("I 1",rs.getString(2), gstOutputAccount, baseGSTTotal, accCodes, accValsCr, numAccs, monthEnds);
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void forInvoiceLines(Connection conAcc, Statement stmtAcc, ResultSet rsAcc, Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, boolean consolidate, boolean useWAC, String salesAccount, String purchasesAccount,
                               String stockAccount, String debtorsAccount, String otherDebtorsAccount,
                               String dateFrom, String dateTo, String financialYearStartDate, String[] accCodes, double[][] accValsDr, double[][] accValsCr, int numAccs, int[] monthEnds,
                               String[] debtorAccCodes, double[][] debtorAccValsDr, String stateFileName, String percentageStart, int numCustomers, String dnm) throws Exception
  {
    stmt = con.createStatement();
    rs = stmt.executeQuery("SELECT COUNT(t2.Category) FROM invoicel AS t2 INNER JOIN invoice AS t1 ON t1.InvoiceCode = t2.InvoiceCode WHERE t1.Status != 'C' AND t1.Date >= {d '" + dateFrom + "'} AND t1.Date <= {d '" + dateTo + "'}");
    double numInvoices = 1.0;
    if(rs.next())
      numInvoices = rs.getInt(1);
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.Category, t2.Amount, t2.ItemCode, t2.Quantity, t1.CompanyCode, t1.InvoiceCode, t1.Date, t1.BaseGSTTotal, t1.GroupDiscount, t1.GroupDiscountType, t1.TotalTotal, t1.GSTTotal, t1.Currency, t2.Amount2, t2.SOCode "
                         + "FROM invoicel AS t2 INNER JOIN invoice AS t1 ON t1.InvoiceCode = t2.InvoiceCode WHERE t1.Status != 'C' AND t1.Date >= {d '" + dateFrom + "'} AND t1.Date <= {d '" + dateTo + "'} ORDER BY t2.InvoiceCode");
    
    String category="", itemCode, date = "", customerCode = "", lastCustomerCode="", invoiceCode, lastInvoiceCode = "", groupDiscountType;
    double amount, quantity, unitPrice, amounts = 0.0, amountIncGST, baseGSTTotal = 0.0, groupDiscount = 1, totalTotal, lastGroupDiscount = 0, salesSofar = 0, gstTotal = 0.0, amountsNonBase = 0.0, amountIncGSTNonBase, amountNonBase;
    boolean first = true;
    int count = 0;

    while(rs.next())
    {    
      directoryUtils.updateState(stateFileName, "" + generalUtils.strDPs('2', (percentageStart + "." + generalUtils.doubleToStr((count++ / numInvoices) * 100))));

      invoiceCode       = rs.getString(6);
      customerCode      = rs.getString(5);
      groupDiscount     = generalUtils.doubleFromStr(rs.getString(9));
      groupDiscountType = rs.getString(10);
      totalTotal        = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(11)), '2');
      gstTotal          = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(12)), '2');

      if(groupDiscount != 0.0) // groupDiscount exists
      {
        if(! groupDiscountType.equals("%")) // then must convert it into a percentage
          groupDiscount = groupDiscount / (totalTotal + groupDiscount      - gstTotal);///////////////////
        else groupDiscount /= 100.0;
      }

      if(! invoiceCode.equals(lastInvoiceCode))
      {          
        if(! first)
        {
          amounts        = generalUtils.doubleDPs(amounts,        '2') - generalUtils.doubleDPs((generalUtils.doubleDPs(amounts,        '2') * lastGroupDiscount), '2');
          amountsNonBase = generalUtils.doubleDPs(amountsNonBase, '2') - generalUtils.doubleDPs((generalUtils.doubleDPs(amountsNonBase, '2') * lastGroupDiscount), '2');
            
          amountIncGST        = generalUtils.doubleDPs(amounts,        '2') + baseGSTTotal;
          amountIncGSTNonBase = generalUtils.doubleDPs(amountsNonBase, '2') + gstTotal;

          if(consolidate)
          {
            if(existsCustomer(con, stmt2, rs2, lastCustomerCode))
              updateValueForAccount("IL 1", date, debtorsAccount, amountIncGST, accCodes, accValsDr, numAccs, monthEnds);
            else updateValueForAccount("IL 2", date, otherDebtorsAccount, amountIncGST, accCodes, accValsDr, numAccs, monthEnds);
          }
          else
          {
            if(existsCustomer(con, stmt2, rs2, lastCustomerCode))
              updateValueForAccountIndividualDebtorOrCreditor("IL 3",date, lastCustomerCode, amountIncGST, debtorAccCodes, debtorAccValsDr, numCustomers, monthEnds);
            else updateValueForAccount("IL 4",date, otherDebtorsAccount, amountIncGST, accCodes, accValsDr, numAccs, monthEnds);
          }
          
          if(amounts != salesSofar)
            updateValueForAccount("IL 55", date, category, generalUtils.doubleDPs((amounts - salesSofar), '2'), accCodes, accValsCr, numAccs, monthEnds);

          amounts = salesSofar = amountsNonBase = 0.0;
        }
        else first = false;

        lastInvoiceCode   = invoiceCode;
        lastCustomerCode  = customerCode;
        lastGroupDiscount = groupDiscount;
      }
      
      amount = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(2)), '2');
      amounts += amount;

      amountNonBase   = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(14)), '2');
      amountsNonBase += amountNonBase;
    
      category     = rs.getString(1);
      date         = rs.getString(7);
      baseGSTTotal = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(8)), '2'); // pickup each time but only use once
      gstTotal     = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(12)), '2'); // pickup each time but only use once
  
      amount = generalUtils.doubleDPs(amount - (amount * groupDiscount), '2');

      updateValueForAccount("IL 5", date, category, amount, accCodes, accValsCr, numAccs, monthEnds);
      salesSofar += amount;
      amountNonBase = generalUtils.doubleDPs(amountNonBase - (amountNonBase * groupDiscount), '2');

      itemCode = rs.getString(3);
  
      if(itemExists(con, stmt2, rs2, itemCode))
      {   
        if(useWAC)
        {
          unitPrice = inventory.getWAC(con, stmt2, rs2, itemCode, financialYearStartDate, date, dnm);

          quantity = generalUtils.doubleFromStr(rs.getString(4));
          amount = unitPrice * quantity;

          amount = generalUtils.doubleDPs(amount - (amount * groupDiscount), '2'); /////////////////////////////////////////////// was commented-out
          amount = generalUtils.doubleDPs(amount, '2');
        }
        // else use amount from rec

        updateValueForAccount("IL 6",date, purchasesAccount, amount, accCodes, accValsDr, numAccs, monthEnds);
        updateValueForAccount("IL 7",date, stockAccount, amount, accCodes, accValsCr, numAccs, monthEnds);
      }
    }

    if(lastInvoiceCode.length() > 0)
    {
      if(amounts != 0.0)
      {
        amounts             = generalUtils.doubleDPs(amounts,        '2') - generalUtils.doubleDPs((generalUtils.doubleDPs(amounts, '2') * lastGroupDiscount), '2');
        amountIncGST        = generalUtils.doubleDPs(amounts,        '2') + baseGSTTotal;
        amountsNonBase      = generalUtils.doubleDPs(amountsNonBase, '2') - generalUtils.doubleDPs((generalUtils.doubleDPs(amountsNonBase, '2') * lastGroupDiscount), '2');
        amountIncGSTNonBase = generalUtils.doubleDPs(amountsNonBase, '2') + gstTotal;
        
        if(consolidate)
        {
          if(existsCustomer(con, stmt2, rs2, lastCustomerCode))
            updateValueForAccount("IL 8", date, debtorsAccount, amountIncGST, accCodes, accValsDr, numAccs, monthEnds);
          else updateValueForAccount("IL 9", date, otherDebtorsAccount, amountIncGST, accCodes, accValsDr, numAccs, monthEnds);
        }
        else
        {
          if(existsCustomer(con, stmt2, rs2, lastCustomerCode))
            updateValueForAccountIndividualDebtorOrCreditor("IL 10",date, lastCustomerCode, amountIncGST, debtorAccCodes, debtorAccValsDr, numCustomers, monthEnds);
          else updateValueForAccount("IL 11",date, otherDebtorsAccount, amountIncGST, accCodes, accValsDr, numAccs, monthEnds);
        }
      }
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void forPurchaseInvoiceHeader(Connection con, Statement stmt, ResultSet rs, String gstInputAccount, String dateFrom, String dateTo, String[] accCodes, double[][] accValsDr, int numAccs, int[] monthEnds) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT BaseGSTTotal, Date, GSTTotal FROM pinvoice "
                         + "WHERE Status != 'C' AND Date >= {d '" + dateFrom + "'} AND Date <= {d '" + dateTo + "'}");//  AND Settled != 'Y'");

    double baseGSTTotal;
    
    while(rs.next())
    {
      baseGSTTotal = generalUtils.doubleFromStr(rs.getString(1));
      
      if(baseGSTTotal != 0.0)
        updateValueForAccount("PI", rs.getString(2), gstInputAccount, baseGSTTotal, accCodes, accValsDr, numAccs, monthEnds);
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void forPurchaseInvoiceLines(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, boolean consolidate, String purchasesAccount, String stockAccount, String creditorsAccount,
                                       String otherCreditorsAccount, String dateFrom, String dateTo, String[] accCodes, double[][] accValsDr, double[][] accValsCr, int numAccs, int[] monthEnds,
                                       String[] creditorAccCodes, double[][] creditorAccValsCr, String stateFileName, String percentageStart, int numSuppliers) throws Exception
  {
    stmt = con.createStatement();
    rs = stmt.executeQuery("SELECT COUNT(t2.Category) FROM pinvoicel AS t2 INNER JOIN pinvoice AS t1 ON t1.InvoiceCode = t2.InvoiceCode WHERE t1.Status != 'C' AND t1.Date >= {d '" + dateFrom + "'} AND t1.Date <= {d '" + dateTo
                         + "'}");
    double numInvoices = 1.0;
    if(rs.next())
      numInvoices = rs.getInt(1);
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.Category, t2.Amount, t2.ItemCode, t2.Quantity, t1.CompanyCode, t1.InvoiceCode, t1.Date, t1.BaseGSTTotal, t1.GroupDiscount, t1.GroupDiscountType, t1.TotalTotal, t1.GSTTotal, t1.Currency, t2.Amount2 "
                         + "FROM pinvoicel AS t2 INNER JOIN pinvoice AS t1 ON t1.InvoiceCode = t2.InvoiceCode WHERE t1.Status != 'C' AND t1.Date >= {d '" + dateFrom + "'} AND t1.Date <= {d '" + dateTo
                         + "'} ORDER BY t2.InvoiceCode");

    String category="", itemCode, date = "", supplierCode = "", lastSupplierCode="", invoiceCode, lastInvoiceCode = "", groupDiscountType, currency = "";
    double amount, amounts = 0.0, amountIncGST, baseGSTTotal = 0.0, groupDiscount = 1, totalTotal, lastGroupDiscount = 0, purchasesSofar = 0, gstTotal = 0.0, amountsNonBase = 0.0, amountIncGSTNonBase, amountNonBase;
    boolean first = true;
    int count = 0;

    while(rs.next())
    {
      directoryUtils.updateState(stateFileName, "" + generalUtils.strDPs('2', (percentageStart + "." + generalUtils.doubleToStr((count++ / numInvoices) * 100))));

      supplierCode      = rs.getString(5);
      invoiceCode       = rs.getString(6);
      groupDiscount     = generalUtils.doubleFromStr(rs.getString(9));
      groupDiscountType = rs.getString(10);
      totalTotal        = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(11)), '2');
      gstTotal          = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(12)), '2');
      currency          = rs.getString(13);
      
      if(groupDiscount != 0.0) // groupDiscount exists
      {
        if(! groupDiscountType.equals("%")) // then must convert it into a percentage
          groupDiscount = groupDiscount / (totalTotal + groupDiscount       - gstTotal);
        else groupDiscount /= 100.0;
      }

      if(! invoiceCode.equals(lastInvoiceCode))
      {          
        if(! first)
        {
          amounts        = generalUtils.doubleDPs(amounts,        '2') - generalUtils.doubleDPs((generalUtils.doubleDPs(amounts,        '2') * lastGroupDiscount), '2');
          amountsNonBase = generalUtils.doubleDPs(amountsNonBase, '2') - generalUtils.doubleDPs((generalUtils.doubleDPs(amountsNonBase, '2') * lastGroupDiscount), '2');
            
          amountIncGST        = generalUtils.doubleDPs(amounts,        '2') + baseGSTTotal;
          amountIncGSTNonBase = generalUtils.doubleDPs(amountsNonBase, '2') + gstTotal;

          if(consolidate)
          {
            if(existsSupplier(con, stmt2, rs2, lastSupplierCode))
              updateValueForAccount("PIL 1", date, creditorsAccount, amountIncGST, accCodes, accValsCr, numAccs, monthEnds);
            else updateValueForAccount("PIL 2", date, otherCreditorsAccount, amountIncGST, accCodes, accValsCr, numAccs, monthEnds);
          }
          else
          {
            if(existsSupplier(con, stmt2, rs2, lastSupplierCode))
              updateValueForAccountIndividualDebtorOrCreditor("PIL 3",date, lastSupplierCode, amountIncGST, creditorAccCodes, creditorAccValsCr, numSuppliers, monthEnds);
            else updateValueForAccount("PIL 4",date, otherCreditorsAccount, amountIncGST, accCodes, accValsCr, numAccs, monthEnds);
          }
          
          if(amounts != purchasesSofar)
            updateValueForAccount("PIL 55", date, category, generalUtils.doubleDPs((amounts - purchasesSofar), '2'), accCodes, accValsDr, numAccs, monthEnds);

          amounts = purchasesSofar = amountsNonBase = 0.0;
        }
        else first = false;

        lastInvoiceCode  = invoiceCode;
        lastSupplierCode = supplierCode;
//        lastCurrency      = currency;
      }

      lastGroupDiscount = groupDiscount;

      amount = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(2)), '2');
      amounts += amount;
      
      amountNonBase   = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(14)), '2');
      amountsNonBase += amountNonBase;

      category     = rs.getString(1);
      date         = rs.getString(7);
      baseGSTTotal = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(8)), '2'); // pickup each time but only use once
      gstTotal     = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(12)), '2'); // pickup each time but only use once

      amount = generalUtils.doubleDPs(amount - (amount * groupDiscount), '2');
      purchasesSofar += amount;
      amountNonBase = generalUtils.doubleDPs(amountNonBase - (amountNonBase * groupDiscount), '2');

      if(! category.equals(purchasesAccount))
      {
        updateValueForAccount("PIL 5", date, category, amount, accCodes, accValsDr, numAccs, monthEnds);
      }
      else
      {
        itemCode = rs.getString(3);
  
        if(itemExists(con, stmt2, rs2, itemCode))
          updateValueForAccount("PIL 6",date, stockAccount, amount, accCodes, accValsDr, numAccs, monthEnds);
        else updateValueForAccount("PIL 7",date, purchasesAccount, amount, accCodes, accValsDr, numAccs, monthEnds);
      }
    }

    if(lastInvoiceCode.length() > 0)
    {
      if(amounts != 0.0)
      {
        amounts             = generalUtils.doubleDPs(amounts,        '2') - generalUtils.doubleDPs((generalUtils.doubleDPs(amounts, '2') * lastGroupDiscount), '2');
        amountIncGST        = generalUtils.doubleDPs(amounts,        '2') + baseGSTTotal;
        amountsNonBase      = generalUtils.doubleDPs(amountsNonBase, '2') - generalUtils.doubleDPs((generalUtils.doubleDPs(amountsNonBase, '2') * lastGroupDiscount), '2');
        amountIncGSTNonBase = generalUtils.doubleDPs(amountsNonBase, '2') + gstTotal;

        if(consolidate)
        {
          if(existsSupplier(con, stmt2, rs2, lastSupplierCode))
            updateValueForAccount("PIL 8", date, creditorsAccount, amountIncGST, accCodes, accValsCr, numAccs, monthEnds);
          else updateValueForAccount("PIL 9", date, otherCreditorsAccount, amountIncGST, accCodes, accValsCr, numAccs, monthEnds);
        }
        else
        {
          if(existsSupplier(con, stmt2, rs2, lastSupplierCode))
            updateValueForAccountIndividualDebtorOrCreditor("PIL 10",date, lastSupplierCode, amountIncGST, creditorAccCodes, creditorAccValsCr, numSuppliers, monthEnds);
          else updateValueForAccount("PIL 11",date, otherCreditorsAccount, amountIncGST, accCodes, accValsCr, numAccs, monthEnds);
        }
      }
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void forSalesCreditNoteHeader(Connection con, Statement stmt, ResultSet rs, String gstOutputAccount, String dateFrom, String dateTo, String[] accCodes, double[][] accValsDr, int numAccs, int[] monthEnds) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT BaseGSTTotal, Date, GSTTotal FROM credit WHERE Status != 'C' AND Date >= {d '" + dateFrom + "'} AND Date <= {d '" + dateTo + "'}");

    double baseGSTTotal;
    
    while(rs.next())
    {
      baseGSTTotal = generalUtils.doubleFromStr(rs.getString(1));

      if(baseGSTTotal != 0.0)
        updateValueForAccount("C",rs.getString(2), gstOutputAccount, baseGSTTotal, accCodes, accValsDr, numAccs, monthEnds);
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void forSalesCreditNoteLines(Connection con, Statement stmt, ResultSet rs, boolean consolidate, String salesReturnedAccount, String debtorsAccount, String otherDebtorsAccount, String dateFrom, String dateTo, String[] accCodes,
                                  double[][] accValsDr, double[][] accValsCr, int numAccs, int[] monthEnds, String[] debtorAccCodes, double[][] debtorAccValsCr, int numCustomers) throws Exception
  {
    Statement stmt2 = null;
    ResultSet rs2   = null;

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.Category, t2.Amount, t2.ItemCode, t2.Quantity, t1.CompanyCode, t1.CNCode, t1.Date, t1.BaseGSTTotal, t1.GroupDiscount, t1.GroupDiscountType, t1.TotalTotal, t1.GSTTotal, t1.Currency, t2.Amount2 "
                         + "FROM creditl AS t2 INNER JOIN credit AS t1 ON t1.CNCode = t2.CNCode WHERE t1.Status != 'C' AND t1.Date >= {d '" + dateFrom + "'} AND t1.Date <= {d '" + dateTo + "'} ORDER BY CNCode");

    String category="", date = "", customerCode = "", lastCustomerCode="", cnCode, lastCNCode = "", groupDiscountType, lastCurrency = "", currency= "";
    double amount, amounts = 0.0, amountIncGST, baseGSTTotal = 0.0, groupDiscount = 1, totalTotal, lastGroupDiscount = 0, salesSofar = 0, gstTotal = 0.0, amountsNonBase = 0.0, amountIncGSTNonBase = 0.0, amountNonBase;
    boolean first = true;

    while(rs.next())
    {
      customerCode      = rs.getString(5);
      cnCode            = rs.getString(6);
      groupDiscount     = generalUtils.doubleFromStr(rs.getString(9));
      groupDiscountType = rs.getString(10);
      totalTotal        = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(11)), '2');
      gstTotal          = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(12)), '2');
      currency          = rs.getString(13);

      if(groupDiscount != 0.0) // groupDiscount exists
      {
        if(! groupDiscountType.equals("%")) // then must convert it into a percentage
          groupDiscount = groupDiscount / (totalTotal + groupDiscount - gstTotal);
        else groupDiscount /= 100.0;
      }

      if(! cnCode.equals(lastCNCode))
      {
        if(! first)
        {
          amounts             = generalUtils.doubleDPs(amounts,        '2') - generalUtils.doubleDPs((generalUtils.doubleDPs(amounts,        '2') * lastGroupDiscount), '2');
          amountIncGST        = generalUtils.doubleDPs(amounts,        '2') + baseGSTTotal;
          amountsNonBase      = generalUtils.doubleDPs(amountsNonBase, '2') - generalUtils.doubleDPs((generalUtils.doubleDPs(amountsNonBase, '2') * lastGroupDiscount), '2');
          amountIncGSTNonBase = generalUtils.doubleDPs(amountsNonBase, '2') + gstTotal;

          if(consolidate)
          {
            if(existsCustomer(con, stmt2, rs2, lastCustomerCode))
              updateValueForAccount("CL 1", date, debtorsAccount, amountIncGST, accCodes, accValsCr, numAccs, monthEnds);
            else updateValueForAccount("CL 2", date, otherDebtorsAccount, amountIncGST, accCodes, accValsCr, numAccs, monthEnds);
          }
          else
          {
            if(existsCustomer(con, stmt2, rs2, lastCustomerCode))
              updateValueForAccountIndividualDebtorOrCreditor("CL 3",date, lastCustomerCode, amountIncGST, debtorAccCodes, debtorAccValsCr, numCustomers, monthEnds);
            else updateValueForAccount("CL 4",date, otherDebtorsAccount, amountIncGST, accCodes, accValsCr, numAccs, monthEnds);
          }

          if(amounts != salesSofar)
            updateValueForAccount("CL 55", date, category, generalUtils.doubleDPs((amounts - salesSofar), '2'), accCodes, accValsDr, numAccs, monthEnds);

          amounts = salesSofar = amountsNonBase = 0.0;
        }
        else first = false;

        lastCNCode  = cnCode;
        lastCustomerCode = customerCode;
        lastCurrency = currency;
      }

      lastGroupDiscount = groupDiscount;

      amount          = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(2)), '2');
      amounts        += amount;
      amountNonBase   = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(14)), '2');
      amountsNonBase += amountNonBase;

      category     = rs.getString(1);
      date         = rs.getString(7);
      baseGSTTotal = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(8)),  '2'); // pickup each time but only use once
      gstTotal     = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(12)), '2'); // pickup each time but only use once

      amount = generalUtils.doubleDPs(amount - (amount * groupDiscount), '2');
      salesSofar += amount;
      amountNonBase = generalUtils.doubleDPs(amountNonBase - (amountNonBase * groupDiscount), '2');

      if(! category.equals(salesReturnedAccount))
        updateValueForAccount("CL 5", date, category, amount, accCodes, accValsDr, numAccs, monthEnds);
      else
      {
        updateValueForAccount("CL 7",date, salesReturnedAccount, amount, accCodes, accValsDr, numAccs, monthEnds);
        // should be smart like above, or dumb (always taking what is on the CN line?)
      }
    }

    if(lastCNCode.length() > 0)
    {
      if(amounts != 0.0)
      {
        amounts             = generalUtils.doubleDPs(amounts,        '2') - generalUtils.doubleDPs((generalUtils.doubleDPs(amounts,        '2') * lastGroupDiscount), '2');
        amountIncGST        = generalUtils.doubleDPs(amounts,        '2') + baseGSTTotal;
        amountsNonBase      = generalUtils.doubleDPs(amountsNonBase, '2') - generalUtils.doubleDPs((generalUtils.doubleDPs(amountsNonBase, '2') * lastGroupDiscount), '2');
        amountIncGSTNonBase = generalUtils.doubleDPs(amountsNonBase, '2') + gstTotal;

        if(consolidate)
        {
          if(existsCustomer(con, stmt2, rs2, lastCustomerCode))
            updateValueForAccount("CL 8", date, debtorsAccount, amountIncGST, accCodes, accValsCr, numAccs, monthEnds);
          else updateValueForAccount("CL 9", date, otherDebtorsAccount, amountIncGST, accCodes, accValsCr, numAccs, monthEnds);
        }
        else
        {
          if(existsCustomer(con, stmt2, rs2, lastCustomerCode))
            updateValueForAccountIndividualDebtorOrCreditor("CL 10",date, lastCustomerCode, amountIncGST, debtorAccCodes, debtorAccValsCr, numCustomers, monthEnds);
          else updateValueForAccount("CL 11",date, otherDebtorsAccount, amountIncGST, accCodes, accValsCr, numAccs, monthEnds);
        }
      }
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void forSalesCreditNoteLinesCoSAdjustments(Connection conAcc, Statement stmtAcc, ResultSet rsAcc, Connection con, Statement stmt, ResultSet rs, boolean useWAC, String previousFinancialYearStartDate, String purchasesAccount, String stockAccount, String dateFrom, String dateTo, String[] accCodes,
                                                double[][] accValsDr, double[][] accValsCr, int numAccs, int[] monthEnds        , String unm, String dnm) throws Exception
  {
    Statement stmt2 = null;
    ResultSet rs2   = null;

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.CNCode, t2.Amount, t1.Date, t2.ItemCode, t2.Quantity, t2.InvoiceCode,        t1.GroupDiscount, "
                         + "t1.GroupDiscountType, t1.TotalTotal, t1.GSTTotal       FROM creditl AS t2 INNER JOIN credit AS t1 ON t1.CNCode = t2.CNCode "
                         + "WHERE t2.CostOfSaleAdjustment = 'Y' AND t1.Status != 'C' AND t1.Date >= {d '" + dateFrom + "'} AND t1.Date <= {d '" + dateTo + "'} ORDER BY t2.CNCode");

    double bAmt = 0.0, baseAmount, quantity, groupDiscount = 1, totalTotal, gstTotal;
    String itemCode, cnCode, date="", lastDate = "", lastCNCode = "", dateOfInvoice, groupDiscountType, invoiceCode;
    boolean first = true;

    while(rs.next())
    {
      itemCode    = rs.getString(4);
      date        = rs.getString(3);
      quantity    = generalUtils.doubleFromStr(rs.getString(5));
      invoiceCode = rs.getString(6);
      groupDiscount     = generalUtils.doubleFromStr(rs.getString(7));
      groupDiscountType = rs.getString(8);
      totalTotal        = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(9)), '2');
      gstTotal          = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(10)), '2');

      if(groupDiscount != 0.0) // groupDiscount exists
      {
        if(! groupDiscountType.equals("%")) // then must convert it into a percentage
          groupDiscount = groupDiscount / (totalTotal + groupDiscount - gstTotal);
        else groupDiscount /= 100.0;
      }

      if(itemExists(con, stmt2, rs2, itemCode))
      {
        if(useWAC)
        {
          // date must be of the original invoice in order to get the correct WAC
          dateOfInvoice = salesInvoice.getAnInvoiceFieldGivenCode(con, stmt2, rs2, "Date", invoiceCode);

          // determine finanacial year start date for invoice date

          String financialYearStartDate = accountsUtils.getAccountingYearStartDateForADate(con, stmt2, rs2, dateOfInvoice, dnm);

          baseAmount = inventory.getWAC(con, stmt2, rs2, itemCode, financialYearStartDate, dateOfInvoice, dnm);

          baseAmount *= quantity;
        }
        else baseAmount = generalUtils.doubleFromStr(rs.getString(2));


         baseAmount -= generalUtils.doubleDPs((generalUtils.doubleDPs(baseAmount, '2') * groupDiscount), '2');/////////////


        baseAmount = generalUtils.doubleDPs(baseAmount, '2');

        if(baseAmount != 0.0)
        {
          cnCode = rs.getString(1);

          if(cnCode.equals(lastCNCode) || first)
          {
            bAmt += baseAmount;

            if(first)
            {
              lastCNCode = cnCode;
              lastDate   = date;
              first      = false;
            }
          }
          else
          {
            updateValueForAccount("CL 101", lastDate, stockAccount,     bAmt, accCodes, accValsDr, numAccs, monthEnds);
            updateValueForAccount("CL 102", lastDate, purchasesAccount, bAmt, accCodes, accValsCr, numAccs, monthEnds);
            bAmt       = baseAmount;

            lastCNCode = cnCode;
            lastDate = date;
          }
        }
      }
    }

    if(lastCNCode.length() > 0)
    {
      if(bAmt != 0.0)
      {
         updateValueForAccount("CL 103", lastDate, stockAccount,     bAmt, accCodes, accValsDr, numAccs, monthEnds);
         updateValueForAccount("CL 104", lastDate, purchasesAccount, bAmt, accCodes, accValsCr, numAccs, monthEnds);
      }
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void forPurchaseCreditNoteHeader(Connection con, Statement stmt, ResultSet rs, String gstInputAccount, String dateFrom, String dateTo, String[] accCodes, double[][] accValsCr, int numAccs, int[] monthEnds)
                                           throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT BaseGSTTotal, Date, GSTTotal FROM pcredit WHERE Status != 'C' AND Date >= {d '" + dateFrom + "'} AND Date <= {d '" + dateTo + "'}");

    double baseGSTTotal;
    
    while(rs.next())
    {
      baseGSTTotal = generalUtils.doubleFromStr(rs.getString(1));

      if(baseGSTTotal != 0.0)
        updateValueForAccount("PC",rs.getString(2), gstInputAccount, baseGSTTotal, accCodes, accValsCr, numAccs, monthEnds);
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void forPurchaseCreditNoteLines(Connection con, Statement stmt, ResultSet rs, boolean consolidate, String purchasesReturnedAccount, String creditorsAccount, String otherCreditorsAccount, 
                                          String dateFrom, String dateTo, String[] accCodes, double[][] accValsDr, double[][] accValsCr, int numAccs, int[] monthEnds,
                                          String[] creditorAccCodes, double[][] creditorAccValsDr, int numSuppliers) throws Exception
  {
    Statement stmt2 = null;
    ResultSet rs2   = null;

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.Category, t2.Amount, t2.ItemCode, t2.Quantity, t1.CompanyCode, t1.PCNCode, t1.Date, t1.BaseGSTTotal, "
                         + "t1.GroupDiscount, t1.GroupDiscountType, t1.TotalTotal, t1.GSTTotal, t1.Currency, t2.Amount2  FROM pcreditl AS t2 INNER JOIN pcredit AS t1 ON "
                         + "t1.PCNCode = t2.PCNCode WHERE t1.Status != 'C' AND t1.Date >= {d '" + dateFrom + "'} AND t1.Date <= {d '" + dateTo
                         + "'} ORDER BY PCNCode");

    String category="", date = "", supplierCode = "", lastSupplierCode = "", cnCode, lastCNCode = "", groupDiscountType, currency = "";
    double amount, amounts = 0.0, amountIncGST, baseGSTTotal = 0.0, groupDiscount = 1, totalTotal, lastGroupDiscount = 0, purchasesSofar = 0, gstTotal = 0.0, amountsNonBase = 0.0, amountIncGSTNonBase = 0.0, amountNonBase;
    boolean first = true;
    
    while(rs.next())
    {
      supplierCode      = rs.getString(5);
      cnCode            = rs.getString(6);
      groupDiscount     = generalUtils.doubleFromStr(rs.getString(9));
      groupDiscountType = rs.getString(10);
      totalTotal        = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(11)), '2');
      gstTotal          = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(12)), '2');
      currency          = rs.getString(13);
      
      if(groupDiscount != 0.0) // groupDiscount exists
      {
        if(! groupDiscountType.equals("%")) // then must convert it into a percentage
          groupDiscount = groupDiscount / (totalTotal + groupDiscount);
        else groupDiscount /= 100.0;
      }

      if(! cnCode.equals(lastCNCode))
      {          
        if(! first)
        {
          amounts             = generalUtils.doubleDPs(amounts,        '2') - generalUtils.doubleDPs((generalUtils.doubleDPs(amounts,        '2') * lastGroupDiscount), '2');
          amountIncGST        = generalUtils.doubleDPs(amounts,        '2') + baseGSTTotal;
          amountsNonBase      = generalUtils.doubleDPs(amountsNonBase, '2') - generalUtils.doubleDPs((generalUtils.doubleDPs(amountsNonBase, '2') * lastGroupDiscount), '2');
          amountIncGSTNonBase = generalUtils.doubleDPs(amountsNonBase, '2') + gstTotal;

          if(consolidate)
          {
            if(existsSupplier(con, stmt2, rs2, lastSupplierCode))
              updateValueForAccount("PCL 1", date, creditorsAccount, amountIncGST, accCodes, accValsDr, numAccs, monthEnds);
            else updateValueForAccount("PCL 2", date, otherCreditorsAccount, amountIncGST, accCodes, accValsDr, numAccs, monthEnds);
          }
          else
          {
            if(existsSupplier(con, stmt2, rs2, lastSupplierCode))
              updateValueForAccountIndividualDebtorOrCreditor("PCL 3",date, lastSupplierCode, amountIncGST, creditorAccCodes, creditorAccValsDr, numSuppliers, monthEnds);
            else updateValueForAccount("PCL 4",date, otherCreditorsAccount, amountIncGST, accCodes, accValsDr, numAccs, monthEnds);
          }

          if(amounts != purchasesSofar)
            updateValueForAccount("IL 55", date, category, generalUtils.doubleDPs((amounts - purchasesSofar), '2'), accCodes, accValsCr, numAccs, monthEnds);

          amounts = purchasesSofar = 0.0;
        }
        else first = false;

        lastCNCode  = cnCode;
        lastSupplierCode = supplierCode;
      }
      
      lastGroupDiscount = groupDiscount;

      amount          = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(2)), '2');
      amounts        += amount;
      amountNonBase   = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(14)), '2');
      amountsNonBase += amountNonBase;
      
      category     = rs.getString(1);
      date         = rs.getString(7);
      baseGSTTotal = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(8)), '2'); // pickup each time but only use once
      gstTotal     = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(12)), '2'); // pickup each time but only use once

      amount = generalUtils.doubleDPs(amount - (amount * groupDiscount), '2');
      purchasesSofar += amount;
      amountNonBase = generalUtils.doubleDPs(amountNonBase - (amountNonBase * groupDiscount), '2');

      if(! category.equals(purchasesReturnedAccount))
        updateValueForAccount("PCL 5", date, category, amount, accCodes, accValsCr, numAccs, monthEnds);
      else
      {
        updateValueForAccount("PCL 7",date, purchasesReturnedAccount, amount, accCodes, accValsCr, numAccs, monthEnds);
      }
    }

    if(lastCNCode.length() > 0)
    {
      if(amounts != 0.0)
      {
        amounts             = generalUtils.doubleDPs(amounts,        '2') - generalUtils.doubleDPs((generalUtils.doubleDPs(amounts,        '2') * lastGroupDiscount), '2');
        amountIncGST        = generalUtils.doubleDPs(amounts,        '2') + baseGSTTotal;
        amountsNonBase      = generalUtils.doubleDPs(amountsNonBase, '2') - generalUtils.doubleDPs((generalUtils.doubleDPs(amountsNonBase, '2') * lastGroupDiscount), '2');
        amountIncGSTNonBase = generalUtils.doubleDPs(amountsNonBase, '2') + gstTotal;

        if(consolidate)
        {
          if(existsSupplier(con, stmt2, rs2, lastSupplierCode))
            updateValueForAccount("PCL 8", date, creditorsAccount, amountIncGST, accCodes, accValsDr, numAccs, monthEnds);
          else updateValueForAccount("PCL 9", date, otherCreditorsAccount, amountIncGST, accCodes, accValsDr, numAccs, monthEnds);
        }
        else
        {
          if(existsSupplier(con, stmt2, rs2, lastSupplierCode))
            updateValueForAccountIndividualDebtorOrCreditor("PCL 10",date, lastSupplierCode, amountIncGST, creditorAccCodes, creditorAccValsDr, numSuppliers, monthEnds);
          else updateValueForAccount("PCL 11",date, otherCreditorsAccount, amountIncGST, accCodes, accValsDr, numAccs, monthEnds);
        }
      }
    }
        
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void forDebitNoteHeader(Connection con, Statement stmt, ResultSet rs, boolean consolidate, String salesAccount, String debtorsAccount, String otherDebtorsAccount, String gstOutputAccount, String dateFrom,
                                  String dateTo, String[] accCodes, double[][] accValsDr, double[][] accValsCr, int numAccs, int[] monthEnds,
                                  String[] debtorAccCodes, double[][] debtorAccValsDr, int numCustomers) throws Exception
  {
    Statement stmt2 = null;
    ResultSet rs2   = null;

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT CompanyCode, BaseTotalTotal, BaseGSTTotal, Date, Currency, TotalTotal, GSTTotal FROM debit WHERE Status != 'C' AND Date >= {d '" + dateFrom + "'} AND Date <= {d '" + dateTo + "'}");

    double baseGSTTotal, baseTotalTotal, totalTotal, gstTotal;
    
    while(rs.next())
    {
      baseTotalTotal = generalUtils.doubleFromStr(rs.getString(2));
      baseGSTTotal   = generalUtils.doubleFromStr(rs.getString(3));
      totalTotal     = generalUtils.doubleFromStr(rs.getString(6));
      gstTotal       = generalUtils.doubleFromStr(rs.getString(7));

      if(consolidate)
      {  
        if(! existsCustomer(con, stmt2, rs2, rs.getString(1)))
          updateValueForAccount("D1",rs.getString(4), otherDebtorsAccount, baseTotalTotal, accCodes, accValsDr, numAccs, monthEnds);
        else updateValueForAccount("D2",rs.getString(4), debtorsAccount, baseTotalTotal, accCodes, accValsDr, numAccs, monthEnds);

        updateValueForAccount("D3",rs.getString(4), salesAccount, (baseTotalTotal - baseGSTTotal), accCodes, accValsCr, numAccs, monthEnds);
      }
      else
      {
        if(! existsCustomer(con, stmt2, rs2, rs.getString(1)))
          updateValueForAccount("D4",rs.getString(4), otherDebtorsAccount, baseTotalTotal, accCodes, accValsDr, numAccs, monthEnds);
        else updateValueForAccountIndividualDebtorOrCreditor("D5",rs.getString(4), rs.getString(1), baseTotalTotal, debtorAccCodes, debtorAccValsDr, numCustomers, monthEnds);
        
        updateValueForAccount("D6",rs.getString(4), salesAccount, (baseTotalTotal - baseGSTTotal), accCodes, accValsCr, numAccs, monthEnds);
      }

      if(baseGSTTotal != 0.0)
        updateValueForAccount("D7",rs.getString(4), gstOutputAccount, baseGSTTotal, accCodes, accValsCr, numAccs, monthEnds);
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void forPurchaseDebitNoteHeader(Connection con, Statement stmt, ResultSet rs, boolean consolidate, String purchasesAccount, String creditorsAccount, String otherCreditorsAccount, String gstInputAccount,
                                          String dateFrom, String dateTo, String[] accCodes, double[][] accValsDr, double[][] accValsCr, int numAccs, int[] monthEnds,
                                          String[] debtorAccCodes, double[][] debtorAccValsCr, int numCustomers) throws Exception
  {
    Statement stmt2 = null;
    ResultSet rs2   = null;

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT CompanyCode, BaseTotalTotal, BaseGSTTotal, Date, Currency, TotalTotal, GSTTotal FROM pdebit WHERE Status != 'C' AND Date >= {d '" + dateFrom + "'} AND Date <= {d '" + dateTo + "'}");

    double baseGSTTotal, baseTotalTotal, totalTotal, gstTotal;
    
    while(rs.next())
    {
      baseTotalTotal = generalUtils.doubleFromStr(rs.getString(2));
      baseGSTTotal   = generalUtils.doubleFromStr(rs.getString(3));
      totalTotal     = generalUtils.doubleFromStr(rs.getString(6));
      gstTotal       = generalUtils.doubleFromStr(rs.getString(7));

      if(consolidate)
      {  
        if(! existsCustomer(con, stmt2, rs2, rs.getString(1)))
          updateValueForAccount("PD",rs.getString(4), otherCreditorsAccount, baseTotalTotal, accCodes, accValsCr, numAccs, monthEnds);
        else updateValueForAccount("PD",rs.getString(4), creditorsAccount, baseTotalTotal, accCodes, accValsCr, numAccs, monthEnds);

        updateValueForAccount("PD",rs.getString(4), purchasesAccount, (baseTotalTotal - baseGSTTotal), accCodes, accValsDr, numAccs, monthEnds);
      }
      else
      {
        if(! existsCustomer(con, stmt2, rs2, rs.getString(1)))
          updateValueForAccount("PD",rs.getString(4), otherCreditorsAccount, baseTotalTotal, accCodes, accValsCr, numAccs, monthEnds);
        else updateValueForAccountIndividualDebtorOrCreditor("PD",rs.getString(4), rs.getString(1), baseTotalTotal, debtorAccCodes, debtorAccValsCr, numCustomers, monthEnds);

        updateValueForAccount("PD",rs.getString(4), purchasesAccount, (baseTotalTotal - baseGSTTotal), accCodes, accValsDr, numAccs, monthEnds);
      }

      if(baseGSTTotal != 0.0)
        updateValueForAccount("PD",rs.getString(4), gstInputAccount, baseGSTTotal, accCodes, accValsDr, numAccs, monthEnds);
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void forIAT(Connection con, Connection accCon, Statement stmt, ResultSet rs, String[] accCodes, double[][] accValsDr, double[][] accValsCr, String dateFrom, String dateTo, String baseCurrency, int numAccs,
                      int[] monthEnds) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT AccountDr, AmountDr, RateDr, AccountCr, AmountCr, RateCr, TransactionDate, ChargesDr, ChargesCr, CurrencyDr, CurrencyCr, RateDrCharges, RateCrCharges, IATCode FROM iat WHERE Status != 'C' AND TransactionDate >= {d '"
                          + dateFrom + "'} AND TransactionDate <= {d '" + dateTo + "'}");
    
    double baseAmount, amount, rate, charges, chargesAsBaseAmount, diff;
    String chargesAccount, currency;
    
    while(rs.next())
    {    
      amount = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(2)), '2');

      charges = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(8)), '2');

      amount -= charges;

      rate   = generalUtils.doubleFromStr(rs.getString(3));

      baseAmount = generalUtils.doubleDPs((amount * rate), '2');

      updateValueForAccount("IAT 1",rs.getString(7), rs.getString(1), baseAmount, accCodes, accValsDr, numAccs, monthEnds);

      currency = rs.getString(10);

      chargesAsBaseAmount = charges * generalUtils.doubleFromStr(rs.getString(12));

      chargesAsBaseAmount = generalUtils.doubleDPs(chargesAsBaseAmount, '2');

      chargesAccount = chargesAccountForCurrency(accCon, currency);

      updateValueForAccount("IAT 3", rs.getString(7), chargesAccount, chargesAsBaseAmount, accCodes, accValsDr, numAccs, monthEnds);

      // Cr
 
      amount = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(5)), '2');

      charges = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(9)), '2');

      amount += charges;
      
      rate   = generalUtils.doubleFromStr(rs.getString(6));

      diff = (baseAmount + chargesAsBaseAmount) - (generalUtils.doubleDPs((amount * rate), '2') - generalUtils.doubleDPs(charges * generalUtils.doubleFromStr(rs.getString(13)), '2'));

      baseAmount = diff;

      baseAmount += generalUtils.doubleDPs((amount * rate), '2');

      updateValueForAccount("IAT 2",rs.getString(7), rs.getString(4), baseAmount, accCodes, accValsCr, numAccs, monthEnds);

      currency = rs.getString(11);
      
      chargesAsBaseAmount = charges * generalUtils.doubleFromStr(rs.getString(13));

      chargesAsBaseAmount = generalUtils.doubleDPs(chargesAsBaseAmount, '2');   
      
      chargesAccount = chargesAccountForCurrency(accCon, currency);

      updateValueForAccount("IAT 4",rs.getString(7), chargesAccount, chargesAsBaseAmount, accCodes, accValsDr, numAccs, monthEnds);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void forBatch(Connection ofsaCon, Connection accCon, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String type, String baseCurrency, String code, String account, String dateFrom, String dateTo,
                        String[] accCodes, double[][] accValsDr, double[][] accValsCr, int numAccs, int[] monthEnds) throws Exception
  {
    stmt = accCon.createStatement();

    rs = stmt.executeQuery("SELECT AccCode, BaseAmount, DrCr, TransactionDate, Amount FROM joubatchl WHERE Code = '" + generalUtils.sanitiseForSQL(code) + "' AND TransactionDate >= {d '" + dateFrom + "'} AND TransactionDate <= {d '" + dateTo + "'}");
    String useAccount, accountCurrency;

    while(rs.next())
    {
      if(account.length() == 0)
        useAccount = rs.getString(1);
      else useAccount = account;

      if(rs.getString(3).equals("D")) // Dr
      {
        if(type.equals("D"))
        {
          accountCurrency = customer.getCompanyCurrencyGivenCode(ofsaCon, stmt2, rs2, useAccount);
          updateValueForAccountIndividualDebtorOrCreditor("B2",rs.getString(4), useAccount, generalUtils.doubleFromStr(rs.getString(2)), accCodes, accValsDr, numAccs, monthEnds);
        }
        else
        if(type.equals("C"))
        {
          accountCurrency = supplier.getSupplierCurrencyGivenCode(ofsaCon, stmt2, rs2, useAccount);
          updateValueForAccountIndividualDebtorOrCreditor("B2",rs.getString(4), useAccount, generalUtils.doubleFromStr(rs.getString(2)), accCodes, accValsCr, numAccs, monthEnds);
        }
        else
        {
          accountCurrency = currencyForAccount(accCon, stmt2, rs2, useAccount);
          updateValueForAccount("B2",rs.getString(4), useAccount, generalUtils.doubleFromStr(rs.getString(2)), accCodes, accValsDr, numAccs, monthEnds);
        }
      }
      else // Cr
      {
        if(type.equals("D"))
        {
          accountCurrency = customer.getCompanyCurrencyGivenCode(ofsaCon, stmt2, rs2, useAccount);
          updateValueForAccountIndividualDebtorOrCreditor("B2",rs.getString(4), useAccount, generalUtils.doubleFromStr(rs.getString(2)), accCodes, accValsDr, numAccs, monthEnds);
        }
        else
        if(type.equals("C"))
        {
          accountCurrency = supplier.getSupplierCurrencyGivenCode(ofsaCon, stmt2, rs2, useAccount);
            updateValueForAccountIndividualDebtorOrCreditor("B2",rs.getString(4), useAccount, generalUtils.doubleFromStr(rs.getString(2)), accCodes, accValsCr, numAccs, monthEnds);
        }
        else
        {
          accountCurrency = currencyForAccount(accCon, stmt2, rs2, useAccount);
          updateValueForAccount("B2",rs.getString(4), useAccount, generalUtils.doubleFromStr(rs.getString(2)), accCodes, accValsCr, numAccs, monthEnds);
        }
      }
    }
  
    if(rs   != null) rs.close();                                 
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean itemExists(Connection con, Statement stmt, ResultSet rs, String itemCode) throws Exception
  {
    if(itemCode.length() == 0) // just-in-case
      return false;

    if(itemCode.equals("-")) // quick check
      return false;

    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM stock WHERE ItemCode = '" + itemCode + "'");

    int numRecs = 0;
    
    if(rs.next())
      numRecs = rs.getInt("rowcount") ;

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    if(numRecs == 1)
      return true;
    
    return false;
  }
 
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean existsCustomer(Connection con, Statement stmt, ResultSet rs, String companyCode) throws Exception
  {
    int numRecs = 0;
        
    try
    {
      if(companyCode.length() == 0)
        return false;

      if(companyCode.equals("-"))
        return false;

      stmt = con.createStatement();
    
      rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM company WHERE CompanyCode = '" + companyCode + "'");
      rs.next();
      numRecs = rs.getInt("rowcount") ;
    }
    catch(Exception e) { }
      
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    if(numRecs == 1)
      return true;
    
    return false;
  }

  // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean existsSupplier(Connection con, Statement stmt, ResultSet rs, String companyCode) throws Exception
  {
    int numRecs = 0;
        
    try
    {
      if(companyCode.length() == 0)
        return false;

      if(companyCode.equals("-"))
        return false;

      stmt = con.createStatement();
    
      rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM supplier WHERE SupplierCode = '" + companyCode + "'");
      rs.next();
      numRecs = rs.getInt("rowcount") ;
    }
    catch(Exception e) { }
      
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    if(numRecs == 1)
      return true;
    
    return false;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
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
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void outputPageFrame(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, boolean plain, String monthFrom, String monthTo, String which, String title, String option, String unm, String sid, String uty,
                               String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    int[] hmenuCount = new int[1];

    String subMenuText = dashboardUtils.buildSubMenuText(con, stmt, rs, req, "6053", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount);

    subMenuText += "<table id='title' width=100%><tr><td colspan=2 nowrap><p>" + title + directoryUtils.buildHelp(6053) + "</td></tr></table>";

    subMenuText += buildSubMenuText(plain, which, monthFrom, monthTo, option, unm, sid, uty, men, den, dnm, bnm, hmenuCount);

    if(plain)
      scoutln(out, bytesOut, authenticationUtils.buildPlainScreen(con, stmt, rs, req, unm, sid, uty, men, den, dnm, bnm, "AccountsGenerateTBReport", subMenuText, hmenuCount[0], "", "", localDefnsDir, defnsDir));
    else
    {
      scoutln(out, bytesOut, authenticationUtils.buildScreen(con, stmt, rs, req, true, unm, sid, uty, men, den, dnm, bnm, "AccountsGenerateTBReport", subMenuText, hmenuCount[0], "", "", localDefnsDir, defnsDir));

      scoutln(out, bytesOut, "<table border=0><tr><td valign=top><div id='second' style=\"{margin-top:0px;}\"></div></td><td valign=top><div id='third' style=\"{margin-top:0px;margin-left:0px;}\"></div></td></tr></table>");
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String buildSubMenuText(boolean plain, String which, String monthFrom, String monthTo, String option, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] hmenuCount) throws Exception
  {
    String s = "<div id='submenu'>";

    ++hmenuCount[0];

    if(! plain)
    {
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\">";
      s += "<a href=\"/central/servlet/AccountsGenerateTBReport?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p4=" + which  + "&p1=" + monthFrom + "&p2=" + monthTo + "&p5=" + option
        + "&p3=P\">Friendly</a></dt></dl>";
    }
    
    s += "</div>";
    
    --hmenuCount[0];
    
    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void primeCustomers(Connection con, Statement stmt, ResultSet rs, String[] accCodes) throws Exception
  {
    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT CompanyCode FROM company ORDER BY CompanyCode");

      int x = 0;

      while(rs.next())
        accCodes[x++] = rs.getString(1);

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void primeSuppliers(Connection con, Statement stmt, ResultSet rs, String[] accCodes) throws Exception
  {
    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT SupplierCode FROM supplier ORDER BY SupplierCode");

      int x = 0;

      while(rs.next())
        accCodes[x++] = rs.getString(1);

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void showDebtorsAndCreditors(Connection con, Statement stmt, ResultSet rs, PrintWriter out, boolean plain, String separateOrCombined, String which, String[] accCodes, int numAccs, double[][] accValsDr, double[][] accValsCr,
                                       int[] numMonths, String dateFrom, String dateTo, int[] bytesOut) throws Exception
  {
    int x, y;
    String bgColor;
    boolean wanted;

    for(x=0;x<numAccs;++x)
    {
      wanted = false;
      for(y=0;y<14;++y)
      {
        if(accValsDr[y][x] != 0.0)
          wanted = true;
      }

      if(wanted)
      {
        scoutln(out, bytesOut, "<tr><td><p>" + accCodes[x] + "</td>");
        scout(out, bytesOut, "<td nowrap><p>");
        if(which.equals("D"))
          scout(out, bytesOut, customer.getCompanyNameGivenCode(con, stmt, rs, accCodes[x]));
        else scout(out, bytesOut, supplier.getSupplierNameGivenCode(con, stmt, rs, accCodes[x]));
        scoutln(out, bytesOut, "</td>");

        if(separateOrCombined.equals("S"))
        {
          if(plain)
            scoutln(out, bytesOut, "<td align=right style=\"background:#D0D0D0;\"><p>" + generalUtils.formatNumeric(accValsDr[12][x], '2') + "</td>");
          else scoutln(out, bytesOut, "<td align=right style=\"background:#D0D0D0;\"><p><a href=\"javascript:gl" + which + "('" + accCodes[x] + "','" + dateFrom + "','" + dateTo + "')\">" + generalUtils.formatNumeric(accValsDr[12][x], '2') + "</a></td>");

          if(plain)
            scoutln(out, bytesOut, "<td align=right style=\"background:#D0D0D0;\"><p>" + generalUtils.formatNumeric(accValsCr[12][x], '2') + "</td>");
          else scoutln(out, bytesOut, "<td align=right style=\"background:#D0D0D0;\"><p><a href=\"javascript:gl" + which + "('" + accCodes[x] + "','" + dateFrom + "','" + dateTo + "')\">" + generalUtils.formatNumeric(accValsCr[12][x], '2') + "</a></td>");
        }
        else
        {
          if(plain)
            scoutln(out, bytesOut, "<td align=right style=\"background:#D0D0D0;\"><p>" + generalUtils.formatNumeric((accValsDr[12][x] - accValsCr[12][x]), '2') + "</a></td>");
          else
          {
            scoutln(out, bytesOut, "<td align=right style=\"background:#D0D0D0;\"><p><a href=\"javascript:gl" + which + "('" + accCodes[x] + "','" + dateFrom + "','" + dateTo + "')\">"
                                 + generalUtils.formatNumeric((accValsDr[12][x] - accValsCr[12][x]), '2') + "</a></td>");
          }
        }

        for(y=0;y<numMonths[0];++y)
        {
          if(y == 0 || y == 2 || y == 4 || y == 6 || y == 8 || y == 10 || y == 12) bgColor = "#F0F0F0"; else bgColor = "#D0D0D0";

          if(separateOrCombined.equals("S"))
          {
            if(plain)
              scoutln(out, bytesOut, "<td align=right style=\"background:" + bgColor + ";\"><p>" + generalUtils.formatNumeric(accValsDr[y][x], '2') + "</td>");
            else
            {
              scoutln(out, bytesOut, "<td align=right style=\"background:" + bgColor + ";\"><p><a href=\"javascript:gl" + which + "('" + accCodes[x] + "','" + dateFrom + "','" + dateTo + "')\">" + generalUtils.formatNumeric(accValsDr[y][x], '2')
                                   + "</a></td>");
            }

            if(plain)
              scoutln(out, bytesOut, "<td align=right style=\"background:" + bgColor + ";\"><p>" + generalUtils.formatNumeric(accValsCr[y][x], '2') + "</a></td>");
            else
            {
              scoutln(out, bytesOut, "<td align=right style=\"background:" + bgColor + ";\"><p><a href=\"javascript:gl" + which + "('" + accCodes[x] + "','" + dateFrom + "','" + dateTo + "')\">" + generalUtils.formatNumeric(accValsCr[y][x], '2')
                                   + "</a></td>");
            }
          }
          else
          {
            if(plain)
              scoutln(out, bytesOut, "<td align=right style=\"background:" + bgColor + ";\"><p>" + generalUtils.formatNumeric((accValsDr[y][x] - accValsCr[y][x]), '2') + "</a></td>");
            else
            {
              scoutln(out, bytesOut, "<td align=right style=\"background:" + bgColor + ";\"><p><a href=\"javascript:gl" + which + "('" + accCodes[x] + "','" + dateFrom + "','" + dateTo + "')\">"
                                   + generalUtils.formatNumeric((accValsDr[y][x] - accValsCr[y][x]), '2') + "</a></td>");
            }
          }
        }

        if(separateOrCombined.equals("S"))
        {
          if(plain)
            scoutln(out, bytesOut, "<td align=right style=\"background:#D0D0D0;\"><p>" + generalUtils.formatNumeric(accValsDr[13][x], '2') + "</td>");
          else scoutln(out, bytesOut, "<td align=right style=\"background:#D0D0D0;\"><p><a href=\"javascript:gl" + which + "('" + accCodes[x] + "','" + dateFrom + "','" + dateTo + "')\">" + generalUtils.formatNumeric(accValsDr[13][x], '2') + "</a></td>");

          if(plain)
            scoutln(out, bytesOut, "<td align=right style=\"background:#D0D0D0;\"><p>" + generalUtils.formatNumeric(accValsCr[13][x], '2') + "</td>");
          else scoutln(out, bytesOut, "<td align=right style=\"background:#D0D0D0;\"><p><a href=\"javascript:gl" + which + "('" + accCodes[x] + "','" + dateFrom + "','" + dateTo + "')\">" + generalUtils.formatNumeric(accValsCr[13][x], '2') + "</a></td>");
        }
        else
        {
          if(plain)
            scoutln(out, bytesOut, "<td align=right style=\"background:#D0D0D0;\"><p>" + generalUtils.formatNumeric((accValsDr[13][x] - accValsCr[13][x]), '2') + "</a></td>");
          else
          {
            scoutln(out, bytesOut, "<td align=right style=\"background:#D0D0D0;\"><p><a href=\"javascript:gl" + which + "('" + accCodes[x] + "','" + dateFrom + "','" + dateTo + "')\">"
                                 + generalUtils.formatNumeric((accValsDr[13][x] - accValsCr[13][x]), '2') + "</a></td>");
          }
        }

        scoutln(out, bytesOut, "</tr>");
      }
    }
  }

}
