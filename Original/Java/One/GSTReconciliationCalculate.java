// =======================================================================================================================================================================================================
// System: ZaraStar Analytic: GST reconciliation - calculate  
// Module: GSTReconciliationCalculate.java
// Author: C.K.Harvey
// Copyright (c) 2003-11 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;
import java.io.*;

public class GSTReconciliationCalculate extends HttpServlet
{
  GeneralUtils  generalUtils = new GeneralUtils();
  MessagePage  messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils  pageFrameUtils = new PageFrameUtils();
  DirectoryUtils  directoryUtils = new DirectoryUtils();
  Customer customer = new Customer();
  AccountsUtils  accountsUtils = new AccountsUtils();
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
      directoryUtils.setContentHeaders2(res);
      out = res.getWriter();

      unm = req.getParameter("unm");
      sid = req.getParameter("sid");
      uty = req.getParameter("uty");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");
      p1  = req.getParameter("p1"); // which
      p2  = req.getParameter("p2"); // dateFrom
      p3  = req.getParameter("p3"); // dateTo
      p4  = req.getParameter("p4"); // plain or not

      if(p4 == null) p4 = "";

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, p3, p4, bytesOut);
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

      System.out.println(e);
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "GSTReconciliationCalculate", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6003, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1, String p2, String p3, String p4, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String imagesDir     = directoryUtils.getSupportDirs('I');
    
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 6003, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "GSTReconciliation", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6003, bytesOut[0], 0, "ACC:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "GSTReconciliation", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6003, bytesOut[0], 0, "SID:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    boolean plain = false;
    if(p4.equals("P"))
      plain = true;

    process(con, stmt, rs, out, req, p1, p2, p3, plain, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 6003, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con  != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void process(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String p1, String p2, String p3, boolean plain, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                       String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    // validate dates TODO
    
    String dateFrom = generalUtils.convertDateToSQLFormat(p2);
    String dateTo   = generalUtils.convertDateToSQLFormat(p3);
    
    scoutln(out, bytesOut, "<html><head><title>GST Reconciliation</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    scoutln(out, bytesOut, "function list(servlet,rate){");
    scoutln(out, bytesOut, "var p3=sanitise(rate);");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/_\"+servlet+\"?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + dateFrom + "&p2=" + dateTo + "&p3=\"+p3;}");

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

    if(plain)
      scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"print\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "generalPlain.css\">");
    else scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");
  
    String which;
    if(p1.equals("O"))
      which = "Output Tax";
    else which = "Input Tax";

    outputPageFrame(con, stmt, rs, out, req, plain, p1, p2, p3, "", "GST Reconciliation: " + which + " (" + dateFrom + " to " + dateTo + ")", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, bytesOut);
                                
    scoutln(out, bytesOut, "<form>");
  
    String[][] gstRateNames = new String[1][];  
    double[][] gstRates     = new double[1][];  
    int numRates = accountsUtils.getGSTNamesAndRates(con, stmt, rs, gstRateNames, gstRates, dnm, localDefnsDir, defnsDir);

    double[] gstTotals        = new double[numRates];  
    double[] totalBaseAmounts = new double[numRates];

    scoutln(out, bytesOut, "<br><table id=\"page\" width=100%>");

    writeHeader(out, bytesOut);
    
    if(p1.equals("O"))
    {    
      calculateInvoices(con, stmt, rs, "!= 'C'", dateFrom, dateTo, gstRateNames[0], gstRates[0], numRates, gstTotals, totalBaseAmounts,
                        localDefnsDir, defnsDir);    
      writeLine(out, "Invoices", "6003b", gstRateNames[0], numRates, gstTotals, totalBaseAmounts, localDefnsDir, defnsDir, bytesOut);
    
      calculateInvoices(con, stmt, rs, "= 'C'", dateFrom, dateTo, gstRateNames[0], gstRates[0], numRates, gstTotals, totalBaseAmounts,
                        localDefnsDir, defnsDir);    
      writeLine(out, "Cash Invoices", "6003c", gstRateNames[0], numRates, gstTotals, totalBaseAmounts, localDefnsDir, defnsDir, bytesOut);
    
      calculateDebitNotes(con, stmt, rs, "!= 'C'", dateFrom, dateTo, gstRateNames[0], gstRates[0], numRates, gstTotals, totalBaseAmounts,
                          localDefnsDir, defnsDir);    
      writeLine(out, "Debit Notes", "6003d", gstRateNames[0], numRates, gstTotals, totalBaseAmounts, localDefnsDir, defnsDir, bytesOut);
    
      calculateDebitNotes(con, stmt, rs, "= 'C'", dateFrom, dateTo, gstRateNames[0], gstRates[0], numRates, gstTotals, totalBaseAmounts,
                          localDefnsDir, defnsDir);    
      writeLine(out, "Cash Debit Notes", "6003e", gstRateNames[0], numRates, gstTotals, totalBaseAmounts, localDefnsDir, defnsDir, bytesOut);
    
      calculateSalesCreditNotes(con, stmt, rs,  dateFrom, dateTo, gstRateNames[0], gstRates[0], numRates, gstTotals, totalBaseAmounts, localDefnsDir,
                           defnsDir);    
      writeLine(out, "Credit Notes", "6003f", gstRateNames[0], numRates, gstTotals, totalBaseAmounts, localDefnsDir, defnsDir, bytesOut);
    
      calculateReceiptVouchers(con, stmt, rs,  dateFrom, dateTo, gstRateNames[0], gstRates[0], numRates, gstTotals, totalBaseAmounts, localDefnsDir, defnsDir);
      writeLine(out, "Receipt Vouchers", "6003g", gstRateNames[0], numRates, gstTotals, totalBaseAmounts, localDefnsDir, defnsDir,  bytesOut);
    }
    else
    {    
      calculatePurchaseInvoices(con, stmt, rs, "!= 'C'", dateFrom, dateTo, gstRateNames[0], gstRates[0], numRates, gstTotals, totalBaseAmounts, localDefnsDir, defnsDir);
      writeLine(out, "Purchase Invoices", "6003h", gstRateNames[0], numRates, gstTotals, totalBaseAmounts, localDefnsDir, defnsDir, bytesOut);
    
      calculatePurchaseInvoices(con, stmt, rs, "= 'C'", dateFrom, dateTo, gstRateNames[0], gstRates[0], numRates, gstTotals, totalBaseAmounts, localDefnsDir, defnsDir);
      writeLine(out, "Cash Purchase Invoices", "6003i", gstRateNames[0], numRates, gstTotals, totalBaseAmounts, localDefnsDir, defnsDir, bytesOut);
    
      calculatePurchaseDebitNotes(con, stmt, rs, "!= 'C'", dateFrom, dateTo, gstRateNames[0], gstRates[0], numRates, gstTotals, totalBaseAmounts,
                                  localDefnsDir, defnsDir);    
      writeLine(out, "Purchase Debit Notes", "6003j", gstRateNames[0], numRates, gstTotals, totalBaseAmounts, localDefnsDir, defnsDir, bytesOut);
    
      calculatePurchaseDebitNotes(con, stmt, rs, "= 'C'", dateFrom, dateTo, gstRateNames[0], gstRates[0], numRates, gstTotals, totalBaseAmounts,
                                  localDefnsDir, defnsDir);    
      writeLine(out, "Cash Purchase Debit Notes", "6003k", gstRateNames[0], numRates, gstTotals, totalBaseAmounts, localDefnsDir, defnsDir,
                bytesOut);
    
      calculatePurchaseCreditNotes(con, stmt, rs,  dateFrom, dateTo, gstRateNames[0], gstRates[0], numRates, gstTotals, totalBaseAmounts,
                                   localDefnsDir, defnsDir);    
      writeLine(out, "Purchase Credit Notes", "6003l", gstRateNames[0], numRates, gstTotals, totalBaseAmounts, localDefnsDir, defnsDir, bytesOut);
    
      calculatePaymentVouchers(con, stmt, rs,  dateFrom, dateTo, gstRateNames[0], gstRates[0], numRates, gstTotals, totalBaseAmounts, localDefnsDir, defnsDir);
      writeLine(out, "Payment Vouchers", "6003m", gstRateNames[0], numRates, gstTotals, totalBaseAmounts, localDefnsDir, defnsDir, bytesOut);
    }
    
    writeFooter(out, bytesOut);
    scoutln(out, bytesOut, "</table></form>");     scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void calculateInvoices(Connection con, Statement stmt, ResultSet rs, String cashOrAccount, String dateFrom, String dateTo, String[] gstRateNames, double[] gstRates, int numRates, double[] gstTotals, double[] totalBaseAmounts,
                                 String localDefnsDir, String defnsDir) throws Exception
  {
    for(int x=0;x<numRates;++x)
      gstTotals[x] = totalBaseAmounts[x] = 0.0;
    
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.Amount2, t2.GSTRate, t1.GroupDiscount, t1.GroupDiscountType, t1.TotalTotal, t1.GSTTotal, t2.Amount, t1.Rate "
                         + "FROM invoicel AS t2 INNER JOIN invoice AS t1 ON t2.InvoiceCode = t1.InvoiceCode WHERE t1.Date >= {d '"
                         + dateFrom + "'} AND t1.Date <= {d '" + dateTo + "'} AND t1.Status != 'C' AND t1.CashOrAccount " + cashOrAccount);

    String groupDiscountType;
    double groupDiscount, totalTotal, gstTotal, amount, rate, amount2;
    
    while(rs.next())
    {
      amount2           = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(1)), '2');
      groupDiscount     = generalUtils.doubleFromStr(rs.getString(3));
      groupDiscountType = rs.getString(4);
      totalTotal        = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(5)), '2');
      gstTotal          = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(6)), '2');
      rate              = generalUtils.doubleFromStr(rs.getString(8));
      
      totalTotal -= gstTotal;
      if(totalTotal == 0.0)
        totalTotal = 1.0;
      
      // if groupDiscount exists
      if(groupDiscount != 0.0)
      {
        if(! groupDiscountType.equals("%")) // then must convert it into a percentage
          groupDiscount = groupDiscount / (totalTotal + groupDiscount);
        else groupDiscount /= 100.0;

        // now reduce the amount by the percentage
        amount2 = generalUtils.doubleDPs(amount2, '2') - generalUtils.doubleDPs((generalUtils.doubleDPs(amount2, '2') * groupDiscount), '2');
        amount = generalUtils.doubleDPs(generalUtils.doubleDPs(amount2, '2') * rate, '2');
      }
      else amount = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(7)), '2');
      
      if(amount != 0.0)
        updateTotals(amount, rs.getString(2), gstRateNames, gstRates, numRates, gstTotals, totalBaseAmounts);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void calculateDebitNotes(Connection con, Statement stmt, ResultSet rs, String cashOrAccount, String dateFrom, String dateTo,
                                   String[] gstRateNames, double[] gstRates, int numRates, double[] gstTotals, double[] totalBaseAmounts,
                                   String localDefnsDir, String defnsDir) throws Exception
  {
    for(int x=0;x<numRates;++x)
      gstTotals[x] = totalBaseAmounts[x] = 0.0;
    
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.Amount2, t2.GSTRate, t1.GroupDiscount, t1.GroupDiscountType, t1.TotalTotal, t1.GSTTotal, t2.Amount, t1.Rate "
                         + " FROM debitl AS t2 INNER JOIN debit AS t1 ON t2.DNCode = t1.DNCode WHERE t1.Date >= {d '"
                         + dateFrom + "'} AND t1.Date <= {d '" + dateTo + "'} AND t1.Status != 'C' AND t1.CashOrAccount " + cashOrAccount);

    String groupDiscountType;
    double groupDiscount, totalTotal, gstTotal, amount, rate, amount2;
    
    while(rs.next())
    {
      amount2           = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(1)), '2');
      groupDiscount     = generalUtils.doubleFromStr(rs.getString(3));
      groupDiscountType = rs.getString(4);
      totalTotal        = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(5)), '2');
      gstTotal          = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(6)), '2');
      rate              = generalUtils.doubleFromStr(rs.getString(8));
      
      totalTotal -= gstTotal;
      if(totalTotal == 0.0)
        totalTotal = 1.0;
      
      // if groupDiscount exists
      if(groupDiscount != 0.0)
      {
        if(! groupDiscountType.equals("%")) // then must convert it into a percentage
          groupDiscount = groupDiscount / (totalTotal + groupDiscount);
//          groupDiscount = (1 / (groupDiscount / totalTotal)) / 100.0;
        else groupDiscount /= 100.0;

        // now reduce the amount by the percentage
        amount2 = generalUtils.doubleDPs(amount2, '2') - generalUtils.doubleDPs((generalUtils.doubleDPs(amount2, '2') * groupDiscount), '2');
        amount = generalUtils.doubleDPs(generalUtils.doubleDPs(amount2, '2') * rate, '2');
      }
      else amount = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(7)), '2');
      
      if(amount != 0.0)
        updateTotals(amount, rs.getString(2), gstRateNames, gstRates, numRates, gstTotals, totalBaseAmounts);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void calculateSalesCreditNotes(Connection con, Statement stmt, ResultSet rs, String dateFrom, String dateTo, String[] gstRateNames,
                                    double[] gstRates, int numRates, double[] gstTotals, double[] totalBaseAmounts, String localDefnsDir,
                                    String defnsDir) throws Exception
  {
    for(int x=0;x<numRates;++x)
      gstTotals[x] = totalBaseAmounts[x] = 0.0;
    
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.Amount2, t2.GSTRate, t1.GroupDiscount, t1.GroupDiscountType, t1.TotalTotal, t1.GSTTotal, t2.Amount, t1.Rate "
                         + " FROM creditl AS t2 INNER JOIN credit AS t1 ON t2.CNCode = t1.CNCode WHERE t1.Date >= {d '"
                         + dateFrom + "'} AND t1.Date <= {d '" + dateTo + "'} AND t1.Status != 'C'");

    String groupDiscountType;
    double groupDiscount, totalTotal, gstTotal, amount, rate, amount2;
    
    while(rs.next())
    {
      amount2           = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(1)), '2');
      groupDiscount     = generalUtils.doubleFromStr(rs.getString(3));
      groupDiscountType = rs.getString(4);
      totalTotal        = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(5)), '2');
      gstTotal          = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(6)), '2');
      rate              = generalUtils.doubleFromStr(rs.getString(8));
      
      totalTotal -= gstTotal;
      if(totalTotal == 0.0)
        totalTotal = 1.0;
      
      // if groupDiscount exists
      if(groupDiscount != 0.0)
      {
        if(! groupDiscountType.equals("%")) // then must convert it into a percentage
          groupDiscount = groupDiscount / (totalTotal + groupDiscount);
        else groupDiscount /= 100.0;

        // now reduce the amount by the percentage
        amount2 = generalUtils.doubleDPs(amount2, '2') - generalUtils.doubleDPs((generalUtils.doubleDPs(amount2, '2') * groupDiscount), '2');
        amount = generalUtils.doubleDPs(generalUtils.doubleDPs(amount2, '2') * rate, '2');
      }
      else amount = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(7)), '2');
      
      if(amount != 0.0)
        updateTotals(amount, rs.getString(2), gstRateNames, gstRates, numRates, gstTotals, totalBaseAmounts);
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void calculateReceiptVouchers(Connection con, Statement stmt, ResultSet rs, String dateFrom, String dateTo, String[] gstRateNames,
                                        double[] gstRates, int numRates, double[] gstTotals, double[] totalBaseAmounts, String localDefnsDir,
                                        String defnsDir) throws Exception
  {
    for(int x=0;x<numRates;++x)
      gstTotals[x] = totalBaseAmounts[x] = 0.0;
    double amount;
    
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.Amount, t2.GSTRate, t2.Amount2 FROM rvoucherl AS t2 INNER JOIN rvoucher AS t1 "
                         + "ON t2.VoucherCode = t1.VoucherCode WHERE t1.Date >= {d '" + dateFrom + "'} AND t1.Date <= {d '"
                         + dateTo + "'} AND t1.Status != 'C'");

    while(rs.next())
    {
      amount = generalUtils.doubleFromStr(rs.getString(1));

      updateTotals(amount, rs.getString(2), gstRateNames, gstRates, numRates, gstTotals, totalBaseAmounts);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void calculatePurchaseInvoices(Connection con, Statement stmt, ResultSet rs, String cashOrAccount, String dateFrom, String dateTo,
                                         String[] gstRateNames, double[] gstRates, int numRates, double[] gstTotals, double[] totalBaseAmounts,
                                         String localDefnsDir, String defnsDir) throws Exception
  {
    for(int x=0;x<numRates;++x)
      gstTotals[x] = totalBaseAmounts[x] = 0.0;
    
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.Amount2, t2.GSTRate, t1.GroupDiscount, t1.GroupDiscountType, t1.TotalTotal, t1.GSTTotal, t2.Amount, t1.Rate "
                         + "FROM pinvoicel AS t2 INNER JOIN pinvoice AS t1 ON t2.InvoiceCode = t1.InvoiceCode WHERE t1.Date >= {d '"
                         + dateFrom + "'} AND t1.Date <= {d '" + dateTo + "'} AND t1.Status != 'C' AND t1.CashOrAccount " + cashOrAccount);

    String groupDiscountType;
    double groupDiscount, totalTotal, gstTotal, amount, rate, amount2;
    
    while(rs.next())
    {
      amount2           = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(1)), '2');
      groupDiscount     = generalUtils.doubleFromStr(rs.getString(3));
      groupDiscountType = rs.getString(4);
      totalTotal        = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(5)), '2');
      gstTotal          = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(6)), '2');
      rate              = generalUtils.doubleFromStr(rs.getString(8));
      
      totalTotal -= gstTotal;
      if(totalTotal == 0.0)
        totalTotal = 1.0;
      
      // if groupDiscount exists
      if(groupDiscount != 0.0)
      {
        if(! groupDiscountType.equals("%")) // then must convert it into a percentage
          groupDiscount = groupDiscount / (totalTotal + groupDiscount);
        else groupDiscount /= 100.0;

        // now reduce the amount by the percentage
        amount2 = generalUtils.doubleDPs(amount2, '2') - generalUtils.doubleDPs((generalUtils.doubleDPs(amount2, '2') * groupDiscount), '2');
        amount = generalUtils.doubleDPs(generalUtils.doubleDPs(amount2, '2') * rate, '2');
      }
      else amount = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(7)), '2');
      
      if(amount != 0.0)
        updateTotals(amount, rs.getString(2), gstRateNames, gstRates, numRates, gstTotals, totalBaseAmounts);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void calculatePurchaseDebitNotes(Connection con, Statement stmt, ResultSet rs, String cashOrAccount, String dateFrom, String dateTo,
                                           String[] gstRateNames, double[] gstRates, int numRates, double[] gstTotals, double[] totalBaseAmounts,
                                           String localDefnsDir, String defnsDir) throws Exception
  {
    for(int x=0;x<numRates;++x)
      gstTotals[x] = totalBaseAmounts[x] = 0.0;
    
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.Amount2, t2.GSTRate, t1.GroupDiscount, t1.GroupDiscountType, t1.TotalTotal, t1.GSTTotal, t2.Amount, t1.Rate "
                         + " FROM pdebitl AS t2 INNER JOIN pdebit AS t1 ON t2.PDNCode = t1.PDNCode WHERE t1.Date >= {d '"
                         + dateFrom + "'} AND t1.Date <= {d '" + dateTo + "'} AND t1.Status != 'C' AND t1.CashOrAccount " + cashOrAccount);

    String groupDiscountType;
    double groupDiscount, totalTotal, gstTotal, amount, rate, amount2;
    
    while(rs.next())
    {
      amount2           = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(1)), '2');
      groupDiscount     = generalUtils.doubleFromStr(rs.getString(3));
      groupDiscountType = rs.getString(4);
      totalTotal        = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(5)), '2');
      gstTotal          = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(6)), '2');
      rate              = generalUtils.doubleFromStr(rs.getString(8));
      
      totalTotal -= gstTotal;
      if(totalTotal == 0.0)
        totalTotal = 1.0;
      
      // if groupDiscount exists
      if(groupDiscount != 0.0)
      {
        if(! groupDiscountType.equals("%")) // then must convert it into a percentage
          groupDiscount = groupDiscount / (totalTotal + groupDiscount);
        else groupDiscount /= 100.0;

        // now reduce the amount by the percentage
        amount2 = generalUtils.doubleDPs(amount2, '2') - generalUtils.doubleDPs((generalUtils.doubleDPs(amount2, '2') * groupDiscount), '2');
        amount = generalUtils.doubleDPs(generalUtils.doubleDPs(amount2, '2') * rate, '2');
      }
      else amount = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(7)), '2');
      
      if(amount != 0.0)
        updateTotals(amount, rs.getString(2), gstRateNames, gstRates, numRates, gstTotals, totalBaseAmounts);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void calculatePurchaseCreditNotes(Connection con, Statement stmt, ResultSet rs, String dateFrom, String dateTo, String[] gstRateNames,
                                            double[] gstRates, int numRates, double[] gstTotals, double[] totalBaseAmounts, String localDefnsDir,
                                            String defnsDir) throws Exception
  {
    for(int x=0;x<numRates;++x)
      gstTotals[x] = totalBaseAmounts[x] = 0.0;
    
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.Amount2, t2.GSTRate, t1.GroupDiscount, t1.GroupDiscountType, t1.TotalTotal, t1.GSTTotal, t2.Amount, t1.Rate "
                         + " FROM pcreditl AS t2 INNER JOIN pcredit AS t1 ON t2.PCNCode = t1.PCNCode WHERE t1.Date >= {d '"
                         + dateFrom + "'} AND t1.Date <= {d '" + dateTo + "'} AND t1.Status != 'C'");

    String groupDiscountType;
    double groupDiscount, totalTotal, gstTotal, amount, rate, amount2;
    
    while(rs.next())
    {
      amount2           = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(1)), '2');
      groupDiscount     = generalUtils.doubleFromStr(rs.getString(3));
      groupDiscountType = rs.getString(4);
      totalTotal        = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(5)), '2');
      gstTotal          = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(6)), '2');
      rate              = generalUtils.doubleFromStr(rs.getString(8));
      
      totalTotal -= gstTotal;
      if(totalTotal == 0.0)
        totalTotal = 1.0;
      
      // if groupDiscount exists
      if(groupDiscount != 0.0)
      {
        if(! groupDiscountType.equals("%")) // then must convert it into a percentage
          groupDiscount = groupDiscount / (totalTotal + groupDiscount);
        else groupDiscount /= 100.0;

        // now reduce the amount by the percentage
        amount2 = generalUtils.doubleDPs(amount2, '2') - generalUtils.doubleDPs((generalUtils.doubleDPs(amount2, '2') * groupDiscount), '2');
        amount = generalUtils.doubleDPs(generalUtils.doubleDPs(amount2, '2') * rate, '2');
      }
      else amount = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(7)), '2');
      
      if(amount != 0.0)
        updateTotals(amount, rs.getString(2), gstRateNames, gstRates, numRates, gstTotals, totalBaseAmounts);
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void calculatePaymentVouchers(Connection con, Statement stmt, ResultSet rs, String dateFrom, String dateTo, String[] gstRateNames, double[] gstRates, int numRates, double[] gstTotals, double[] totalBaseAmounts, String localDefnsDir,
                                        String defnsDir) throws Exception
  {
    for(int x=0;x<numRates;++x)
      gstTotals[x] = totalBaseAmounts[x] = 0.0;
    double amount;
    
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.Amount, t2.GSTRate, t2.Amount2 FROM voucherl AS t2 INNER JOIN voucher AS t1 "
                         + "ON t2.VoucherCode = t1.VoucherCode WHERE t1.Date >= {d '" + dateFrom + "'} AND t1.Date <= {d '" + dateTo + "'} AND t1.Status != 'C'");

    while(rs.next())
    {
      amount = generalUtils.doubleFromStr(rs.getString(1));

      updateTotals(amount, rs.getString(2), gstRateNames, gstRates, numRates, gstTotals, totalBaseAmounts);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void updateTotals(double amount, String gstRate, String[] gstRateNames, double[] gstRates, int numRates, double[] gstTotals,
                            double[] totalBaseAmounts) throws Exception
  {
    for(int x=0;x<numRates;++x)
    {
      if(gstRate.equals(gstRateNames[x]))
      {
        gstTotals[x]        += generalUtils.doubleDPs((amount * generalUtils.doubleDPs(gstRates[x], '2')), '2');
        totalBaseAmounts[x] += generalUtils.doubleDPs(amount, '2');
    
        return;
      }
    }
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void writeHeader(PrintWriter out, int[] bytesOut) throws Exception
  { 
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scout(out, bytesOut, "<tr id=\"pageColumn\"><td><p>Document</td>");
    scout(out, bytesOut, "<td><p>GST Rate Name</td><td align=right><p>GST Component</td>");
    scoutln(out, bytesOut, "<td align=right><p>Net Amount</td></tr>");
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void writeFooter(PrintWriter out, int[] bytesOut) throws Exception
  { 
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=5><p>Note: As GST is calculated on a document per-line basis, rounding issues apply.</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void writeLine(PrintWriter out, String document, String servlet, String[] gstRateNames, int numRates, double[] gstTotals,
                         double[] totalBaseAmounts, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    boolean first = true;
    double totalAmounts = 0.0, totalGST = 0.0;
    scoutln(out, bytesOut, "<tr><td><p>" + document + ": </td>");
    
    for(int x=0;x<numRates;++x)
    {
      if(totalBaseAmounts[x] != 0.0)
      {
        if(! first)
          scoutln(out, bytesOut, "<tr><td></td>");
        else first = false;
        
        scoutln(out, bytesOut, "<td><p>" + gstRateNames[x] + "</td><td align=right><p>" + generalUtils.formatNumeric(gstTotals[x], '2') + "</td>");
        scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(totalBaseAmounts[x], '2') + "</td>");
        
        totalGST     += gstTotals[x];
        totalAmounts += totalBaseAmounts[x];

        scout(out, bytesOut, "<td><td><p><a href=\"javascript:list('" + servlet + "','" + gstRateNames[x] + "')\">Documents</a></td>");
      }
    }
    
    scoutln(out, bytesOut, "</tr><tr><td>&nbsp;</td></tr><tr><td colspan=2></td><td align=right><p>" + generalUtils.formatNumeric(totalGST, '2')
                         + "</td><td align=right><p>" + generalUtils.formatNumeric(totalAmounts, '2') + "</td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=5><hr></td></tr>");    
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void outputPageFrame(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, boolean plain, String which, String dateFrom, String dateTo, String bodyStr, String title, String unm, String sid, String uty,
                               String men, String den, String dnm, String bnm, String otherSetup, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    int[] hmenuCount = new int[1];

    String subMenuText = dashboardUtils.buildSubMenuText(con, stmt, rs, req, "6003", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount);

    subMenuText += "<table id='title' width=100%><tr><td colspan=2 nowrap><p>" + title + directoryUtils.buildHelp(6003) + "</td></tr></table>";

    subMenuText += buildSubMenuText(plain, which, dateFrom, dateTo, unm, sid, uty, men, den, dnm, bnm, hmenuCount);

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
  private String buildSubMenuText(boolean plain, String which, String dateFrom, String dateTo, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] hmenuCount) throws Exception
  {
    String s = "<div id='submenu'>";

    ++hmenuCount[0];

    if(! plain)
    {
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\">";
      s += "<a href=\"/central/servlet/GSTReconciliationCalculate?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + which + "&p2=" + dateFrom + "&p3=" + dateTo + "&p4=P\">Friendly</a></dt></dl>";
    }

    s += "</div>";

    --hmenuCount[0];

    return s;
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
