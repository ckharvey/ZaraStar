// =======================================================================================================================================================================================================
// System: ZaraStar Analytic: generate consolidated creditors  
// Module: ConsolidatedCreditorsExecute.java
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
import java.sql.*;
import java.io.*;

public class ConsolidatedCreditorsExecute extends HttpServlet
{
  GeneralUtils  generalUtils = new GeneralUtils();
  MessagePage  messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils  pageFrameUtils = new PageFrameUtils();
  DirectoryUtils  directoryUtils = new DirectoryUtils();
  Customer customer = new Customer();
  AccountsUtils  accountsUtils = new AccountsUtils();
  DocumentUtils  documentUtils = new DocumentUtils();
  
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="";

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
      p1  = req.getParameter("p1"); // order
      p2  = req.getParameter("p2"); // date

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, bytesOut);
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
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "ConsolidatedCreditorsExecute", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1031, bytesOut[0], 0, "ERR:" + p1 + ":" + p2);
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm,
                    String bnm, String p1, String p2, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String localDefnsDir  = directoryUtils.getLocalOverrideDir(dnm);
    String defnsDir          = directoryUtils.getSupportDirs('D');
    String imagesDir         = directoryUtils.getSupportDirs('I');
    
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;
    Statement stmt2 = null;
    ResultSet rs2   = null;
    Statement stmt3 = null;
    ResultSet rs3   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 1031, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "ConsolidatedCreditorsInput", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1031, bytesOut[0], 0, "ACC:" + p1 + ":" + p2);
    if(con  != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "ConsolidatedCreditorsInput", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1031, bytesOut[0], 0, "SID:" + p1 + ":" + p2);
    if(con  != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    String effectiveDate = generalUtils.convertDateToSQLFormat(p2);
    
    process(con, stmt, stmt2, stmt3, rs, rs2, rs3, out, req, p1, effectiveDate, p2, unm, sid, uty, men, den, dnm, bnm, imagesDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 1031, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1 + ":" + p2);
    if(con  != null) con.close();
    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void process(Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, ResultSet rs3, PrintWriter out, HttpServletRequest req, String p1, String effectiveDate, String p2, String unm, String sid, String uty,
                       String men, String den, String dnm, String bnm, String imagesDir, String localDefnsDir, String defnsDir, int[] bytesOut)
                       throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Consolidated Creditors</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 5002, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function view(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/SupplierSettlementHistoryCreate?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den
                           + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=Y&p3=&p4="+p2+"&p5="+p2+"&p6=N&p7=N&p2=\"+p1;}");
    }
    else
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 5001, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function view(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/SupplierPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den
                            + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p1=\"+p1;}");
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
                           + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\"></head>");
  
    int[] hmenuCount = new int[1];
    
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "1031", "", "ConsolidatedCreditorsInput", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount,
                          bytesOut);
                                
    scoutln(out, bytesOut, "<form>");
  
    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Consolidated Creditors", "1031", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<p>Effective Date: " + effectiveDate);

    scout(out, bytesOut, "<p>Ordered by ");
    
    String order="";
    switch(p1.charAt(0))
    {
      case 'C' : scoutln(out, bytesOut, "Supplier Code"); order = "SupplierCode" ; break;
      default  : scoutln(out, bytesOut, "Supplier Name"); order = "Name";          break;
    }
      
    scoutln(out, bytesOut, "<br><table id=\"page\" width=100%>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr id=\"pageColumn\">");
    scoutln(out, bytesOut, "<td><p> Company Code </a></td>");
    scoutln(out, bytesOut, "<td align=right><p> Upto 30 </td>");
    scoutln(out, bytesOut, "<td align=right><p> Upto 60 </td>");
    scoutln(out, bytesOut, "<td align=right><p> Upto 90 </td>");
    scoutln(out, bytesOut, "<td align=right><p> Over 90 </td>");
    scoutln(out, bytesOut, "<td align=right><p> Total </td>");
    scoutln(out, bytesOut, "<td><p> Currency </td>");
    scoutln(out, bytesOut, "<td><p> Company Name</td></tr>");

    int dateLess30 = generalUtils.encodeFromYYYYMMDD(effectiveDate) - 30;  
    int dateLess60 = dateLess30 - 30;  
    int dateLess90 = dateLess30 - 60;  

    double[] upto30  = new double[1]; upto30[0]  = 0.0;
    double[] upto60  = new double[1]; upto60[0]  = 0.0;
    double[] upto90  = new double[1]; upto90[0]  = 0.0;
    double[] over    = new double[1]; over[0]    = 0.0;

    int x;
    
    String[][] currencies = new String[1][];  
    int numCurrencies = accountsUtils.getCurrencyNames(con, stmt, rs, currencies, dnm, localDefnsDir, defnsDir);
    double[] grandTotal30    = new double[numCurrencies];
    double[] grandTotal60    = new double[numCurrencies];
    double[] grandTotal90    = new double[numCurrencies];
    double[] grandTotalOver  = new double[numCurrencies];
    double[] grandTotalTotal = new double[numCurrencies];
    for(x=0;x<numCurrencies;++x)
      grandTotal30[x] = grandTotal60[x] = grandTotal90[x] = grandTotalTotal[x] = grandTotalOver[x] = 0.0;
    
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT SupplierCode, Name, DateEarliestInvoice, Currency FROM supplier ORDER BY " + order);

    String companyCode, companyName, currency, earliestDate, cssFormat = "";
    double total;
    boolean[] currencyMismatch = new boolean[1]; currencyMismatch[0] = false;

    while(rs.next())
    {    
      companyCode  = rs.getString(1);
      companyName  = rs.getString(2);
      earliestDate = rs.getString(3);
      currency     = rs.getString(4);

      upto30[0] = upto60[0] = upto90[0] = over[0] = 0.0;

      total = calculate(con, stmt, stmt2, stmt3, rs, rs2, rs3, companyCode, currencyMismatch, earliestDate, effectiveDate, currency, numCurrencies, currencies,
                        grandTotal30, grandTotal60, grandTotal90, grandTotalOver, dateLess30, dateLess60, dateLess90, upto30, upto60, upto90, over,
                        dnm, localDefnsDir, defnsDir);

      if(total != 0.0)
      {
        if(cssFormat.equals("line1")) cssFormat = "line2"; else cssFormat = "line1";

        scoutln(out, bytesOut, "<tr id='" + cssFormat + "'>");
        scoutln(out, bytesOut, "<td><p><a href=\"javascript:view('" + companyCode + "')\">" + companyCode + "</a></td>");
        scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.doubleDPs('2', upto30[0]) + "</td>");
        scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.doubleDPs('2', upto60[0]) + "</td>");
        scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.doubleDPs('2', upto90[0]) + "</td>");
        scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.doubleDPs('2', over[0]) + "</td>");
        scoutln(out, bytesOut, "<td align=right><p><b>" + generalUtils.doubleDPs('2', total) + "</b></td>");
        scoutln(out, bytesOut, "<td><p>" + currency + "</td>");
        scoutln(out, bytesOut, "<td nowrap><p>" + companyName + "</td></tr>");
      
        for(x=0;x<numCurrencies;++x)
        {
          if(currency.equals(currencies[0][x]))
          {
            grandTotalTotal[x] += total;
            x = numCurrencies;
          }
        }
      }
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    scoutln(out, bytesOut, "<tr><td colspan=9><hr></td></tr>");

    for(x=0;x<numCurrencies;++x)
    {
      if(   generalUtils.doubleDPs(grandTotal30[x], '2') != 0.0 || generalUtils.doubleDPs(grandTotal60[x], '2') != 0.0
         || generalUtils.doubleDPs(grandTotal90[x], '2') != 0.0 || generalUtils.doubleDPs(grandTotalOver[x], '2') != 0.0
         || generalUtils.doubleDPs(grandTotalTotal[x], '2') != 0.0)
      {
        scoutln(out, bytesOut, "<tr><td align=right><p>" + currencies[0][x] + "</td><td align=right><p>"
                + generalUtils.formatNumeric(grandTotal30[x], '2') + "</td><td align=right><p>" 
                + generalUtils.formatNumeric(grandTotal60[x], '2') + "</td><td align=right><p>" 
                + generalUtils.formatNumeric(grandTotal90[x], '2') + "</td><td align=right><p>" 
                + generalUtils.formatNumeric(grandTotalOver[x], '2') + "</td><td align=right><p>" 
                + generalUtils.formatNumeric(grandTotalTotal[x], '2') + "</td></tr>");
      }
    }    

    scoutln(out, bytesOut, "</table></form>");     scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private double calculate(Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, ResultSet rs3, String supplierCode,
                           boolean[] currencyMismatch, String earliestDate, String effectiveDate, String supplierCurrency, int numCurrencies,
                           String[][] currencies, double[] grandTotal30, double[] grandTotal60, double[] grandTotal90, double[] grandTotalOver, int dateLess30,
                           int dateLess60, int dateLess90, double[] upto30, double[] upto60, double[] upto90, double[] over, String dnm,
                           String localDefnsDir, String defnsDir) throws Exception
  {
    double total = 0.0, amt;
    double[] outstanding = new double[1];  outstanding[0] = 0.0;
    String invoiceCode, currency, dnCode;
    int x, thisDate;
    
    stmt = con.createStatement();

    // invoices

    rs = stmt.executeQuery("SELECT InvoiceCode, Date, TotalTotal, Currency FROM pinvoice WHERE CompanyCode = '" + supplierCode
                         + "' AND Status != 'C' AND Settled != 'Y' AND Date <= {d '" + effectiveDate + "'}");

    while(rs.next())
    {
      invoiceCode    = rs.getString(1);
      thisDate       = generalUtils.encodeFromYYYYMMDD(rs.getString(2));
      outstanding[0] = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(3)), '2');
      currency       = rs.getString(4);

      detPaymentLines(con, stmt2, rs2, invoiceCode, effectiveDate, outstanding);
      detPurchaseCreditNoteLines(con, stmt2, stmt3, rs2, rs3, invoiceCode, effectiveDate, dnm, localDefnsDir, defnsDir, outstanding);
      
      amt = generalUtils.doubleDPs(outstanding[0], '2');
      if(amt != 0.0)
      {
        total += amt;

        if(thisDate > dateLess30)
        {
          upto30[0] += amt;
          for(x=0;x<numCurrencies;++x)
          {
            if(currency.equals(currencies[0][x]))
            {
              grandTotal30[x] += amt;
              x = numCurrencies;
            }
          }
        }
        else
        if(thisDate > dateLess60)
        {
          upto60[0] += amt;
          for(x=0;x<numCurrencies;++x)
          {
            if(currency.equals(currencies[0][x]))
            {
              grandTotal60[x] += amt;
              x = numCurrencies;
            }
          }
        }
        else
        if(thisDate > dateLess90)
        {
          upto90[0] += amt;
          for(x=0;x<numCurrencies;++x)
          {
            if(currency.equals(currencies[0][x]))
            {
              grandTotal90[x] += amt;
              x = numCurrencies;
            }
          }
        }
        else
        {
          over[0] += amt;
          for(x=0;x<numCurrencies;++x)
          {
            if(currency.equals(currencies[0][x]))
            {
              grandTotalOver[x] += amt;
              x = numCurrencies;
            }
          }
        }
        
        if(currency.length() > 0 && ! currency.equals(supplierCurrency))
          currencyMismatch[0] = true;
      }
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
 
    // debit notes

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT PDNCode, Date, TotalTotal, Currency FROM pdebit WHERE CompanyCode = '" + supplierCode
                         + "' AND Status != 'C' AND Settled != 'Y' AND Date <= {d '" + effectiveDate + "'}");

    while(rs.next())
    {
      dnCode         = rs.getString(1);
      thisDate       = generalUtils.encodeFromYYYYMMDD(rs.getString(2));
      outstanding[0] = generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(3)), '2');
      currency       = rs.getString(4);

      detPaymentLines(con, stmt2, rs2, dnCode, effectiveDate, outstanding);
      detPurchaseCreditNoteLines(con, stmt2, stmt3, rs2, rs3, dnCode, effectiveDate, dnm, localDefnsDir, defnsDir, outstanding);
      
      amt = generalUtils.doubleDPs(outstanding[0], '2');
      if(amt != 0.0)
      {
        total += amt;

        if(thisDate > dateLess30)
        {
          upto30[0] += amt;
          for(x=0;x<numCurrencies;++x)
          {
            if(currency.equals(currencies[0][x]))
            {
              grandTotal30[x] += amt;
              x = numCurrencies;
            }
          }
        }
        else
        if(thisDate > dateLess60)
        {
          upto60[0] += amt;
          for(x=0;x<numCurrencies;++x)
          {
            if(currency.equals(currencies[0][x]))
            {
              grandTotal60[x] += amt;
              x = numCurrencies;
            }
          }
        }
        else
        if(thisDate > dateLess90)
        {
          upto90[0] += amt;
          for(x=0;x<numCurrencies;++x)
          {
            if(currency.equals(currencies[0][x]))
            {
              grandTotal90[x] += amt;
              x = numCurrencies;
            }
          }
        }
        else
        {
          over[0] += amt;
          for(x=0;x<numCurrencies;++x)
          {
            if(currency.equals(currencies[0][x]))
            {
              grandTotalOver[x] += amt;
              x = numCurrencies;
            }
          }
        }
        
        if(currency.length() > 0 && ! currency.equals(supplierCurrency))
          currencyMismatch[0] = true;
      }
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    return total; 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void detPaymentLines(Connection con, Statement stmt, ResultSet rs, String invoiceCode, String effectiveDate, double[] outstanding) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.AmountPaid FROM paymentl AS t2 INNER JOIN payment AS t1 ON t1.PaymentCode = t2.PaymentCode "
                         + "WHERE t2.InvoiceCode = '" + invoiceCode + "' AND t1.DatePaid <= {d '" + effectiveDate + "'} AND t1.Status != 'C'");

    double amount;

    while(rs.next())
    {    
      amount = generalUtils.doubleFromStr(rs.getString(1));

      outstanding[0] -= generalUtils.doubleDPs(amount, '2');
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void detPurchaseCreditNoteLines(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String invoiceCode,
                                          String effectiveDate, String dnm, String localDefnsDir, String defnsDir, double[] outstanding) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.Amount2, t2.GSTRate FROM pcreditl AS t2 INNER JOIN pcredit AS t1 ON t1.PCNCode = t2.PCNCode "
                         + "WHERE t2.InvoiceCode = '" + invoiceCode + "' AND Date <= {d '" + effectiveDate + "'} AND t1.Status != 'C'");

    String gstRate;
    double amount, gst;

    while(rs.next())
    {    
      amount  = generalUtils.doubleFromStr(rs.getString(1));
      gstRate = rs.getString(2);

      if(gstRate.length() > 0)
      {
        gst = amount * accountsUtils.getGSTRate(con, stmt2, rs2, gstRate);
        amount += gst;
      }

      outstanding[0] -= generalUtils.doubleDPs(amount, '2');
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
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
