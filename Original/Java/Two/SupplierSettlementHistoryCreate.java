// =======================================================================================================================================================================================================
// System: ZaraStar: Supplier: Settlement History: Create settlements page
// Module: SupplierSettlementHistoryCreate.java
// Author: C.K.Harvey
// Copyright (c) 1998-2008 Christopher Harvey. All Rights Reserved.
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

public class SupplierSettlementHistoryCreate extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  AccountsUtils accountsUtils = new AccountsUtils();
  Supplier supplier = new Supplier();
  DashboardUtils dashboardUtils = new DashboardUtils();

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
      directoryUtils.setContentHeaders(res);
      out = res.getWriter();

      unm = req.getParameter("unm");
      sid = req.getParameter("sid");
      uty = req.getParameter("uty");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");
      p1  = req.getParameter("p1"); // unpaidOnly
      p2  = req.getParameter("p2"); // supplierCode
      p3  = req.getParameter("p3"); // dateFrom
      p4  = req.getParameter("p4"); // dateTo
      p5  = req.getParameter("p5"); // dateCut
      p6  = req.getParameter("p6"); // cash or not
      p7  = req.getParameter("p7"); // plain or not
  
      if(p7 == null) p7 = "";
  
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

      System.out.println("5002a: " + e);
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "SupplierSettlementHistoryCreate", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 5002, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den,
                    String dnm, String bnm, String p1, String p2, String p3, String p4, String p5, String p6, String p7,
                    int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;
    Statement stmt2 = null;
    ResultSet rs2   = null;
    Statement stmt3 = null;
    ResultSet rs3   = null;
    
    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 5002, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "SupplierSettlementHistoryInput", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 5002, bytesOut[0], 0, "ACC:" + p2);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "SupplierSettlementHistoryInput", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 5002, bytesOut[0], 0, "SID:" + p2);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    char unpaidOnly = p1.charAt(0);

    String supplierCode;
    if(p2 == null || p2.length() == 0 || p2.equalsIgnoreCase("NULL"))
      supplierCode = "";
    else supplierCode = p2.toUpperCase();

    byte[] companyCode = new byte[21];
    byte[] b           = new byte[61];
    generalUtils.strToBytes(companyCode, supplierCode);
    supplier.getSupplierNameGivenCode(con, stmt, rs, companyCode, b);
    String companyName = generalUtils.stringFromBytes(b, 0L);

    String dateFrom, dateFromText;
    if(p3 == null || p3.length() == 0 || p3.equalsIgnoreCase("NULL"))
    {
      dateFrom = "1970-01-01";
      dateFromText = "Start";
    }  
    else
    {
      dateFromText = p3;
      dateFrom = generalUtils.convertDateToSQLFormat(p3);
    }

    String dateTo, dateToText;
    if(p4 == null || p4.length() == 0 || p4.equalsIgnoreCase("NULL"))
    {
      dateTo = "2099-12-31";
      dateToText = "Finish";
    }
    else
    {
      dateToText = p4;
      dateTo = generalUtils.convertDateToSQLFormat(p4);
    }

    String dateCut, dateCutText;
    if(p5.length() == 0 || p5.equalsIgnoreCase("NULL"))
    {
      dateCut = "2099-12-31";
      dateCutText = "Latest";
    }  
    else
    {
      dateCutText = p5;
      dateCut = generalUtils.convertDateToSQLFormat(p5);
    }

    String baseCurrency = accountsUtils.getBaseCurrency(con, stmt, rs, dnm, localDefnsDir, defnsDir);

    boolean plain = false;
    if(p7.equals("P"))
      plain = true;

    if(p6.charAt(0) == 'Y')
    {
      createPageForCash(con, stmt, stmt2, stmt3, rs, rs2, rs3, out, req, plain, p1, p2, p3, p4, p5, p6, p7, unm, sid, uty, men, den, dnm, bnm,
                        unpaidOnly, baseCurrency, dateFrom, dateTo, dateCut, dateFromText, dateToText, dateCutText, localDefnsDir, defnsDir,
                        companyName, bytesOut);
    }
    else
    {
      createPage(con, stmt, stmt2, stmt3, rs, rs2, rs3, out, req, plain, p1, p2, p3, p4, p5, p6, p7, unm, sid, uty, men, den, dnm, bnm, unpaidOnly,
                 supplierCode, baseCurrency, dateFrom, dateTo, dateCut, dateFromText, dateToText, dateCutText, localDefnsDir, defnsDir,
                 companyName, bytesOut);
    }

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 5002, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p2);
    if(con  != null) con.close();
    if(out != null) out.flush();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void createPage(Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, ResultSet rs3,
                          PrintWriter out, HttpServletRequest req, boolean plain, String p1, String p2, String p3,
                          String p4, String p5, String p6, String p7, String unm, String sid, String uty, String men,
                          String den, String dnm, String bnm, char unpaidOnly, String companyCode, String baseCurrency,
                          String dateFrom, String dateTo, String dateCut, String dateFromText, String dateToText,
                          String dateCutText, String localDefnsDir, String defnsDir, String companyName, int[] bytesOut)
                          throws Exception
  {
    setHead(con, stmt, rs, out, req, plain, p1, p2, p3, p4, p5, p6, p7, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    // do invoices

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT InvoiceCode, Date, TotalTotal, BaseTotalTotal, Currency, Notes, SalesPerson, Settled, "
                                   + "SupplierInvoiceCode FROM pinvoice WHERE CompanyCode = '" + generalUtils.sanitiseForSQL(companyCode)
                                   + "' AND Status != 'C' AND Date >= {d '" + dateFrom + "'} AND Date <= {d '" + dateTo
                                   + "'} ORDER BY Date, InvoiceCode");

    String code, date, amount, currency, baseAmount, notes, salesPerson, settled, suppInvoiceCode;
    String[] cssFormat = new String[1];  cssFormat[0] = "line1";
    String[] firstCurrency = new String[1];  firstCurrency[0] = ""; 
    boolean[] first = new boolean[1];  first[0] = true;
    boolean[] mixedCurrencies = new boolean[1];  mixedCurrencies[0] = false;
    double totalInvoices = 0.0, totalInvoicesBase = 0.0, total = 0.0, baseTotal = 0.0, amountF, baseAmountF, totalDNs = 0.0, totalDNsBase = 0.0;
    double[] outstanding       = new double[1];  
    double[] baseOutstanding   = new double[1];    
    double[] totalCNs          = new double[1];  totalCNs[0]          = 0.0;    
    double[] totalCNsBase      = new double[1];  totalCNsBase[0]      = 0.0;  
    double[] totalPayments     = new double[1];  totalPayments[0]     = 0.0;
    double[] totalPaymentsBase = new double[1];  totalPaymentsBase[0] = 0.0;
    boolean first2 = true;    
                  
    while(rs.next())
    {    
      code        = rs.getString(1);
      date        = rs.getString(2);
      amount      = rs.getString(3);
      baseAmount  = rs.getString(4);
      currency    = rs.getString(5);
      notes       = rs.getString(6);
      salesPerson = rs.getString(7);
      settled     = rs.getString(8);
      suppInvoiceCode = rs.getString(9);

      amountF     = generalUtils.doubleFromStr(amount);
      baseAmountF = generalUtils.doubleFromStr(baseAmount);
        
      outstanding[0]     = generalUtils.doubleDPs(amountF, '2');
      baseOutstanding[0] = generalUtils.doubleDPs(baseAmountF, '2');

      if(first2)
      {
        startBody(out, companyCode, companyName, dateFromText, dateToText, dateCutText, bytesOut);
        first2 = false;
      }

      totalInvoices     += outstanding[0];
      totalInvoicesBase += baseOutstanding[0];

      if(unpaidOnly == 'N')
      {
        writeBodyLinePurchaseInvoice(out, code, date, amount, baseAmount, currency, baseCurrency, firstCurrency, mixedCurrencies,
                                     cssFormat, notes, salesPerson, suppInvoiceCode, first, localDefnsDir, defnsDir, bytesOut);

        if(! settled.equals("Y"))
        {
          writePaymentLines(con, stmt2, rs2, true, out, unpaidOnly, true, code, dateCut, dnm, outstanding, baseOutstanding, totalPayments,
                            totalPaymentsBase, cssFormat, first, baseCurrency, firstCurrency, mixedCurrencies, localDefnsDir, defnsDir, bytesOut);

          writePurchaseCreditNoteLines(con, stmt2, stmt3, rs2, rs3, true, out, unpaidOnly, true, code, dateCut, dnm, false, outstanding,
                                       baseOutstanding, totalCNs, totalCNsBase, cssFormat, first, baseCurrency, firstCurrency, mixedCurrencies,
                                       localDefnsDir, defnsDir, bytesOut);

          writeOutstanding(out, cssFormat[0], outstanding[0], baseOutstanding[0], baseCurrency, localDefnsDir, defnsDir, bytesOut);

          total     += outstanding[0];
          baseTotal += baseOutstanding[0];
        }
        else writeSettled(out, cssFormat, bytesOut);
      }
      else // unpaid only, so need to determine (before printing) if paid
      {
        if(! settled.equals("Y"))
        {
          writePaymentLines(con, stmt2, rs2, false, out, unpaidOnly, false, code, dateCut, dnm, outstanding, baseOutstanding, totalPayments, totalPaymentsBase,
                            cssFormat, first, baseCurrency, firstCurrency, mixedCurrencies, localDefnsDir, defnsDir, bytesOut);

          writePurchaseCreditNoteLines(con, stmt2, stmt3, rs2, rs3, false, out, unpaidOnly, false, code, dateCut, dnm, false, outstanding, baseOutstanding, totalCNs,
                                       totalCNsBase, cssFormat, first, baseCurrency, firstCurrency, mixedCurrencies, localDefnsDir, defnsDir,
                                       bytesOut);
          
          if(outstanding[0] <= -0.00999999999999 || outstanding[0] >= 0.00999999999999)
          {
            writeBodyLinePurchaseInvoice(out, code, date, amount, baseAmount, currency, baseCurrency, firstCurrency, mixedCurrencies, cssFormat,
                                         notes, salesPerson, suppInvoiceCode, first, localDefnsDir, defnsDir, bytesOut);
            
            writePaymentLines(con, stmt2, rs2, true, out, unpaidOnly, true, code, dateCut, dnm, outstanding, baseOutstanding, totalPayments, totalPaymentsBase,
                              cssFormat, first, baseCurrency, firstCurrency, mixedCurrencies, localDefnsDir, defnsDir, bytesOut);

            writePurchaseCreditNoteLines(con, stmt2, stmt3, rs2, rs3, true, out, unpaidOnly, true, code, dateCut, dnm, false, outstanding, baseOutstanding, totalCNs,
                                         totalCNsBase, cssFormat, first, baseCurrency, firstCurrency, mixedCurrencies, localDefnsDir, defnsDir,
                                         bytesOut);
            
            writeOutstanding(out, cssFormat[0], outstanding[0], baseOutstanding[0], baseCurrency, localDefnsDir, defnsDir, bytesOut);
            total     += outstanding[0];
            baseTotal += baseOutstanding[0];
          }
        }
      }
    }
 
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
 
    // do pdebit notes
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT PDNCode, Date, TotalTotal, BaseTotalTotal, Currency, Notes, SalesPerson, Settled "
                         + "FROM pdebit WHERE CompanyCode = '" + generalUtils.sanitiseForSQL(companyCode) + "' AND Status != 'C' AND Date >= {d '"
                         + dateFrom + "'} AND Date <= {d '" + dateTo + "'} ORDER BY Date, PDNCode");

    while(rs.next())
    {    
      code        = rs.getString(1);
      date        = rs.getString(2);
      amount      = rs.getString(3);
      baseAmount  = rs.getString(4);
      currency    = rs.getString(5);
      notes       = rs.getString(6);
      salesPerson = rs.getString(7);
      settled     = rs.getString(8);

      amountF     = generalUtils.doubleFromStr(amount);
      baseAmountF = generalUtils.doubleFromStr(baseAmount);
        
      outstanding[0]     = generalUtils.doubleDPs(amountF, '2');
      baseOutstanding[0] = generalUtils.doubleDPs(baseAmountF, '2');

      if(first2)
      {
        startBody(out, companyCode, companyName, dateFromText, dateToText, dateCutText, bytesOut);
        first2 = false;
      }

      totalDNs     += outstanding[0];
      totalDNsBase += baseOutstanding[0];

      if(unpaidOnly == 'N')
      {
        writeBodyLinePurchaseDebitNote(out, code, date, amount, baseAmount, currency, baseCurrency, firstCurrency, mixedCurrencies,
                                       cssFormat, notes, salesPerson, first, localDefnsDir, defnsDir, bytesOut);

        if(! settled.equals("Y"))
        {
          writePaymentLines(con, stmt2, rs2, true, out, unpaidOnly, true, code, dateCut, dnm, outstanding, baseOutstanding, totalPayments,
                            totalPaymentsBase, cssFormat, first, baseCurrency, firstCurrency, mixedCurrencies, localDefnsDir,
                            defnsDir, bytesOut);

          writePurchaseCreditNoteLines(con, stmt2, stmt3, rs2, rs3, true, out, unpaidOnly, true, code, dateCut, dnm, false, outstanding, baseOutstanding,
                                       totalCNs, totalCNsBase, cssFormat, first, baseCurrency, firstCurrency, mixedCurrencies,
                                       localDefnsDir, defnsDir, bytesOut);

          writeOutstanding(out, cssFormat[0], outstanding[0], baseOutstanding[0], baseCurrency, localDefnsDir, defnsDir,
                           bytesOut);

          total     += outstanding[0];
          baseTotal += baseOutstanding[0];
        }
        else writeSettled(out, cssFormat, bytesOut);
      }
      else // unpaid only, so need to determine (before printing) if paid
      {
        if(! settled.equals("Y"))
        {
          writePaymentLines(con, stmt2, rs2, false, out, unpaidOnly, false, code, dateCut, dnm, outstanding, baseOutstanding, totalPayments,
                            totalPaymentsBase, cssFormat, first, baseCurrency, firstCurrency, mixedCurrencies, localDefnsDir,
                            defnsDir, bytesOut);

          writePurchaseCreditNoteLines(con, stmt2, stmt3, rs2, rs3, false, out, unpaidOnly, false, code, dateCut, dnm, false, outstanding, baseOutstanding,
                                       totalCNs, totalCNsBase, cssFormat, first, baseCurrency, firstCurrency, mixedCurrencies,
                                       localDefnsDir, defnsDir, bytesOut);
          
          if(outstanding[0] <= -0.00999999999999 || outstanding[0] >= 0.00999999999999)
          {
            writeBodyLinePurchaseInvoice(out, code, date, amount, baseAmount, currency, baseCurrency, firstCurrency,
                                         mixedCurrencies, cssFormat, notes, salesPerson, "", first, localDefnsDir,
                                         defnsDir, bytesOut);
            
            writePaymentLines(con, stmt2, rs2, true, out, unpaidOnly, true, code, dateCut, dnm, outstanding, baseOutstanding, totalPayments,
                              totalPaymentsBase, cssFormat, first, baseCurrency, firstCurrency, mixedCurrencies, localDefnsDir,
                              defnsDir, bytesOut);

            writePurchaseCreditNoteLines(con, stmt2, stmt3, rs2, rs3, true, out, unpaidOnly, true, code, dateCut, dnm, false, outstanding, baseOutstanding,
                                         totalCNs, totalCNsBase, cssFormat, first, baseCurrency, firstCurrency, mixedCurrencies,
                                         localDefnsDir, defnsDir, bytesOut);
            
            writeOutstanding(out, cssFormat[0], outstanding[0], baseOutstanding[0], baseCurrency, localDefnsDir, defnsDir,
                             bytesOut);
            total     += outstanding[0];
            baseTotal += baseOutstanding[0];
          }
        }
      }
    }
 
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    boolean cnFound = outputAnyCNsUnattached(con, stmt2, stmt3, rs2, rs3, out, companyCode, baseCurrency, firstCurrency, mixedCurrencies, cssFormat, first,
                                             dnm, localDefnsDir, defnsDir, bytesOut);

    if(first[0] && ! cnFound) // still, no lines output
    {
      startBody(out, companyCode, companyName, dateFromText, dateToText, dateCutText, bytesOut);
      writeBodyLine(out, bytesOut);
    }

    endBody(con, stmt, rs, out, mixedCurrencies[0], firstCurrency[0], baseCurrency, total, baseTotal, totalInvoices, totalInvoicesBase, totalDNs, totalDNsBase,
            totalCNs[0], totalCNsBase[0], totalPayments[0], totalPaymentsBase[0], unm, dnm, bnm, localDefnsDir, defnsDir, bytesOut);
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void createPageForCash(Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, ResultSet rs3, PrintWriter out, HttpServletRequest req, boolean plain, String p1, String p2,
                                 String p3, String p4, String p5, String p6, String p7, String unm, String sid,
                                 String uty, String men, String den, String dnm, String bnm, char unpaidOnly, String baseCurrency,
                                 String dateFrom, String dateTo, String dateCut, String dateFromText, String dateToText,
                                 String dateCutText, String localDefnsDir, String defnsDir, String companyName, int[] bytesOut)
                                 throws Exception
  {
    setHead(con, stmt, rs, out, req, plain, p1, p2, p3, p4, p5, p6, p7, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    // do invoices

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT InvoiceCode, Date, TotalTotal, BaseTotalTotal, Currency, Notes, SalesPerson, Settled, "
                                   + "SupplierInvoiceCode FROM pinvoice WHERE CashOrAccount = 'C' AND Status != 'C' AND Date >= {d '"
                                   + dateFrom + "'} AND Date <= {d '" + dateTo + "'} ORDER BY Date, InvoiceCode");

    String code, date, amount, currency, baseAmount, notes, salesPerson, settled, suppInvoiceCode;
    String[] cssFormat = new String[1];  cssFormat[0] = "line1";
    String[] firstCurrency = new String[1]; 
    boolean[] first = new boolean[1];  first[0] = true;
    boolean[] mixedCurrencies = new boolean[1];  mixedCurrencies[0] = false;
    double totalInvoices = 0.0, totalInvoicesBase = 0.0, total = 0.0, baseTotal = 0.0, amountF, baseAmountF, totalDNs = 0.0,
           totalDNsBase = 0.0;
    double[] outstanding       = new double[1];  
    double[] baseOutstanding   = new double[1];    
    double[] totalCNs          = new double[1];  totalCNs[0]          = 0.0;    
    double[] totalCNsBase      = new double[1];  totalCNsBase[0]      = 0.0;  
    double[] totalPayments     = new double[1];  totalPayments[0]     = 0.0;
    double[] totalPaymentsBase = new double[1];  totalPaymentsBase[0] = 0.0;
    boolean first2 = true;    
                  
    while(rs.next())
    {    
      code        = rs.getString(1);
      date        = rs.getString(2);
      amount      = rs.getString(3);
      baseAmount  = rs.getString(4);
      currency    = rs.getString(5);
      notes       = rs.getString(6);
      salesPerson = rs.getString(7);
      settled     = rs.getString(8);
      suppInvoiceCode = rs.getString(9);

      amountF     = generalUtils.doubleFromStr(amount);
      baseAmountF = generalUtils.doubleFromStr(baseAmount);
        
      outstanding[0]     = generalUtils.doubleDPs(amountF, '2');
      baseOutstanding[0] = generalUtils.doubleDPs(baseAmountF, '2');

      if(first2)
      {
        startBody(out, "Cash", "Flagged as 'CASH'", dateFromText, dateToText, dateCutText, bytesOut);
        first2 = false;
      }

      totalInvoices     += outstanding[0];
      totalInvoicesBase += baseOutstanding[0];

      if(unpaidOnly == 'N')
      {
        writeBodyLinePurchaseInvoice(out, code, date, amount, baseAmount, currency, baseCurrency, firstCurrency, mixedCurrencies,
                                     cssFormat, notes, salesPerson, suppInvoiceCode, first, localDefnsDir, defnsDir, bytesOut);

        if(settled.length() == 0 || settled.equals("0") || settled.equals("N"))
        {
          writePaymentLines(con, stmt2, rs2, true, out, unpaidOnly, true, code, dateCut, dnm, outstanding, baseOutstanding, totalPayments,
                            totalPaymentsBase, cssFormat, first, baseCurrency, firstCurrency, mixedCurrencies, localDefnsDir,
                            defnsDir, bytesOut);

          writePurchaseCreditNoteLines(con, stmt2, stmt3, rs2, rs3, true, out, unpaidOnly, true, code, dateCut, dnm, false, outstanding, baseOutstanding,
                                       totalCNs, totalCNsBase, cssFormat, first, baseCurrency, firstCurrency, mixedCurrencies,
                                       localDefnsDir, defnsDir, bytesOut);

          writeOutstanding(out, cssFormat[0], outstanding[0], baseOutstanding[0], baseCurrency, localDefnsDir, defnsDir,
                           bytesOut);

          total     += outstanding[0];
          baseTotal += baseOutstanding[0];
        }
        else writeSettled(out, cssFormat, bytesOut);
      }
      else // unpaid only, so need to determine (before printing) if paid
      {
        if(settled.length() == 0 || settled.equals("0") || settled.equals("N"))
        {
          writePaymentLines(con, stmt2, rs2, false, out, unpaidOnly, false, code, dateCut, dnm, outstanding, baseOutstanding, totalPayments,
                            totalPaymentsBase, cssFormat, first, baseCurrency, firstCurrency, mixedCurrencies, localDefnsDir,
                            defnsDir, bytesOut);

          writePurchaseCreditNoteLines(con, stmt2, stmt3, rs2, rs3, false, out, unpaidOnly, false, code, dateCut, dnm, false, outstanding, baseOutstanding,
                                       totalCNs, totalCNsBase, cssFormat, first, baseCurrency, firstCurrency, mixedCurrencies,
                                       localDefnsDir, defnsDir, bytesOut);
          
          if(outstanding[0] <= -0.00999999999999 || outstanding[0] >= 0.00999999999999)
          {
            writeBodyLinePurchaseInvoice(out, code, date, amount, baseAmount, currency, baseCurrency, firstCurrency,
                                         mixedCurrencies, cssFormat, notes, salesPerson, suppInvoiceCode, first, localDefnsDir,
                                         defnsDir, bytesOut);
            
            writePaymentLines(con, stmt2, rs2, true, out, unpaidOnly, true, code, dateCut, dnm, outstanding, baseOutstanding, totalPayments,
                              totalPaymentsBase, cssFormat, first, baseCurrency, firstCurrency, mixedCurrencies, localDefnsDir,
                              defnsDir, bytesOut);

            writePurchaseCreditNoteLines(con, stmt2, stmt3, rs2, rs3, true, out, unpaidOnly, true, code, dateCut, dnm, false, outstanding, baseOutstanding,
                                         totalCNs, totalCNsBase, cssFormat, first, baseCurrency, firstCurrency, mixedCurrencies,
                                         localDefnsDir, defnsDir, bytesOut);
            
            writeOutstanding(out, cssFormat[0], outstanding[0], baseOutstanding[0], baseCurrency, localDefnsDir, defnsDir,
                             bytesOut);
            total     += outstanding[0];
            baseTotal += baseOutstanding[0];
          }
        }
      }
    }
 
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
      
    // do pdebit notes
    
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT PDNCode, Date, TotalTotal, BaseTotalTotal, Currency, Notes, SalesPerson, Settled FROM pdebit "
                         + "WHERE CashOrAccount = 'C' AND Status != 'C' AND Date >= {d '" + dateFrom + "'} AND Date <= {d '"
                         + dateTo + "'} ORDER BY Date, PDNCode");

    while(rs.next())
    {    
      code        = rs.getString(1);
      date        = rs.getString(2);
      amount      = rs.getString(3);
      baseAmount  = rs.getString(4);
      currency    = rs.getString(5);
      notes       = rs.getString(6);
      salesPerson = rs.getString(7);
      settled     = rs.getString(8);

      amountF     = generalUtils.doubleFromStr(amount);
      baseAmountF = generalUtils.doubleFromStr(baseAmount);
        
      outstanding[0]     = generalUtils.doubleDPs(amountF, '2');
      baseOutstanding[0] = generalUtils.doubleDPs(baseAmountF, '2');

      if(first2)
      {
        startBody(out, "Cash", "Flagged as 'CASH'", dateFromText, dateToText, dateCutText, bytesOut);
        first2 = false;
      }


      totalDNs     += outstanding[0];
      totalDNsBase += baseOutstanding[0];

      if(unpaidOnly == 'N')
      {
        writeBodyLinePurchaseDebitNote(out, code, date, amount, baseAmount, currency, baseCurrency, firstCurrency, mixedCurrencies,
                                       cssFormat, notes, salesPerson, first, localDefnsDir, defnsDir, bytesOut);

        if(settled.length() == 0 || settled.equals("0") || settled.equals("N"))
        {
          writePaymentLines(con, stmt2, rs2, true, out, unpaidOnly, true, code, dateCut, dnm, outstanding, baseOutstanding, totalPayments,
                            totalPaymentsBase, cssFormat, first, baseCurrency, firstCurrency, mixedCurrencies, localDefnsDir,
                            defnsDir, bytesOut);

          writePurchaseCreditNoteLines(con, stmt2, stmt3, rs2, rs3, true, out, unpaidOnly, true, code, dateCut, dnm, false, outstanding, baseOutstanding,
                                       totalCNs, totalCNsBase, cssFormat, first, baseCurrency, firstCurrency, mixedCurrencies,
                                       localDefnsDir, defnsDir, bytesOut);

          writeOutstanding(out, cssFormat[0], outstanding[0], baseOutstanding[0], baseCurrency, localDefnsDir, defnsDir,
                           bytesOut);

          total     += outstanding[0];
          baseTotal += baseOutstanding[0];
        }
        else writeSettled(out, cssFormat, bytesOut);
      }
      else // unpaid only, so need to determine (before printing) if paid
      {
        if(settled.length() == 0 || settled.equals("0") || settled.equals("N"))
        {
          writePaymentLines(con, stmt2, rs2, false, out, unpaidOnly, false, code, dateCut, dnm, outstanding, baseOutstanding, totalPayments,
                            totalPaymentsBase, cssFormat, first, baseCurrency, firstCurrency, mixedCurrencies, localDefnsDir,
                            defnsDir, bytesOut);

          writePurchaseCreditNoteLines(con, stmt2, stmt3, rs2, rs3, false, out, unpaidOnly, false, code, dateCut, dnm, false, outstanding, baseOutstanding,
                                       totalCNs, totalCNsBase, cssFormat, first, baseCurrency, firstCurrency, mixedCurrencies,
                                       localDefnsDir, defnsDir, bytesOut);
          
          if(outstanding[0] <= -0.00999999999999 || outstanding[0] >= 0.00999999999999)
          {
            writeBodyLinePurchaseInvoice(out, code, date, amount, baseAmount, currency, baseCurrency, firstCurrency, mixedCurrencies,
                                         cssFormat, notes, salesPerson, "", first, localDefnsDir, defnsDir, bytesOut);
            
            writePaymentLines(con, stmt2, rs2, true, out, unpaidOnly, true, code, dateCut, dnm, outstanding, baseOutstanding, totalPayments,
                              totalPaymentsBase, cssFormat, first, baseCurrency, firstCurrency, mixedCurrencies, localDefnsDir,
                              defnsDir, bytesOut);

            writePurchaseCreditNoteLines(con, stmt2, stmt3, rs2, rs3, true, out, unpaidOnly, true, code, dateCut, dnm, false, outstanding, baseOutstanding,
                                         totalCNs, totalCNsBase, cssFormat, first, baseCurrency, firstCurrency, mixedCurrencies,
                                         localDefnsDir, defnsDir, bytesOut);
            
            writeOutstanding(out, cssFormat[0], outstanding[0], baseOutstanding[0], baseCurrency, localDefnsDir, defnsDir,
                             bytesOut);
            total     += outstanding[0];
            baseTotal += baseOutstanding[0];
          }
        }
      }
    }
 
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
      
    if(first[0]) // still, no lines output
    {
      startBody(out, "Cash", companyName, dateFromText, dateToText, dateCutText, bytesOut);
      writeBodyLine(out, bytesOut);
    }

    endBody(con, stmt, rs, out, mixedCurrencies[0], firstCurrency[0], baseCurrency, total, baseTotal, totalInvoices, totalInvoicesBase, totalDNs,
            totalDNsBase, totalCNs[0], totalCNsBase[0], totalPayments[0], totalPaymentsBase[0], unm, dnm, bnm, localDefnsDir, defnsDir,
            bytesOut);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void writePaymentLines(Connection con, Statement stmt, ResultSet rs, boolean incTotals, PrintWriter out, char unpaidOnly, boolean show,
                                 String invoiceCode, String dateCut, String dnm, double[] outstanding, double[] baseOutstanding, double[] totalPayments,
                                 double[] totalPaymentsBase, String[] cssFormat, boolean[] first, String baseCurrency, String[] firstCurrency,
                                 boolean[] mixedCurrencies, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.PaymentCode, t1.DatePaid, t2.AmountPaid, t2.BaseAmountPaid, "
                                   + "t1.Currency, t1.PaymentReference FROM paymentl AS t2 INNER JOIN payment AS t1 ON "
                                   + "t1.PaymentCode = t2.PaymentCode WHERE t2.InvoiceCode = '" + invoiceCode
                                   + "' AND t1.Status != 'C' AND t1.DatePaid <= {d '" + dateCut + "'} ORDER BY t1.DatePaid");

    String code, datePaid, amount, baseAmount, currency, paymentReference;
    double amountF, baseAmountF;

    while(rs.next())
    {    
      code             = rs.getString(1);
      datePaid         = rs.getString(2);
      amount           = rs.getString(3);
      baseAmount       = rs.getString(4);
      currency         = rs.getString(5);
      paymentReference = rs.getString(6);

      amountF     = generalUtils.doubleDPs(generalUtils.doubleFromStr(amount), '2');
      baseAmountF = generalUtils.doubleDPs(generalUtils.doubleFromStr(baseAmount), '2');
      
      if(incTotals)
      {
        totalPayments[0]     += amountF;
        totalPaymentsBase[0] += baseAmountF;
      }

      if(unpaidOnly == 'N')
      {
        outstanding[0]     -= amountF;
        baseOutstanding[0] -= baseAmountF;
        writeBodyLinePayment(out, code, datePaid, amount, baseAmount, currency, baseCurrency, firstCurrency, mixedCurrencies,
                             cssFormat, first, localDefnsDir, defnsDir, bytesOut);
      }
      else
      {
        if(! show)
        {
          outstanding[0]     -= amountF;
          baseOutstanding[0] -= baseAmountF;
        }
        else
        {
          writeBodyLinePayment(out, code, datePaid, amount, baseAmount, currency, baseCurrency, firstCurrency, mixedCurrencies,
                               cssFormat, first, localDefnsDir, defnsDir, bytesOut);
        }
      }
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void writePurchaseCreditNoteLines(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, boolean incTotals, PrintWriter out, char unpaidOnly, boolean show, String invoiceCode,
                                            String dateCut, String dnm, boolean inUnattached, double[] outstanding,
                                            double[] baseOutstanding, double[] totalCNs, double[] totalCNsBase, String[] cssFormat,
                                            boolean[] first, String baseCurrency, String[] firstCurrency, boolean[] mixedCurrencies,
                                            String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t1.PCNCode, t1.Date, t2.Amount2, t2.Amount, t2.GSTRate, t1.Currency "
                                   + "FROM pcreditl AS t2 INNER JOIN pcredit AS t1 ON t1.PCNCode = t2.PCNCode WHERE t2.InvoiceCode = '"
                                   + invoiceCode + "' AND t1.Status != 'C' AND t1.Date <= {d '" + dateCut + "'} ORDER BY t1.Date");

    String amount, baseAmount, code, date, currency, gstRate;
    double amtF, gstF, amountF, baseAmountF;

    while(rs.next())
    {    
      code       = rs.getString(1);
      date       = rs.getString(2);
      amount     = rs.getString(3);
      baseAmount = rs.getString(4);
      gstRate    = rs.getString(5);
      currency   = rs.getString(6);
      
      amountF     = generalUtils.doubleDPs(generalUtils.doubleFromStr(amount), '2');
      baseAmountF = generalUtils.doubleDPs(generalUtils.doubleFromStr(baseAmount), '2');

      if(gstRate.length() > 0)
      {
        amtF = generalUtils.doubleDPs(generalUtils.doubleFromStr(amount), '2');
        gstF = amtF * accountsUtils.getGSTRate(con, stmt2, rs2, gstRate);
        amtF += gstF;
        amount = generalUtils.doubleToStr(amtF);
        if(incTotals)
          totalCNs[0] += amtF;

        amtF = generalUtils.doubleDPs(generalUtils.doubleFromStr(baseAmount), '2');
        gstF = amtF * accountsUtils.getGSTRate(con, stmt2, rs2, gstRate);
        amtF += gstF;
        baseAmount = generalUtils.doubleToStr(amtF);
        if(incTotals)
          totalCNsBase[0] += amtF;
      }
      else
      {
        if(incTotals)
        {
          totalCNs[0]     += amountF;
          totalCNsBase[0] += baseAmountF;
        }
      }

      if(unpaidOnly == 'N')
      {
        outstanding[0]     -= generalUtils.doubleDPs(generalUtils.doubleFromStr(amount), '2');
        baseOutstanding[0] -= generalUtils.doubleDPs(generalUtils.doubleFromStr(baseAmount), '2');
        writeBodyLinePurchaseCreditNote(out, code, date, amount, baseAmount, currency, baseCurrency, firstCurrency, mixedCurrencies, cssFormat, first, inUnattached, localDefnsDir, defnsDir, bytesOut);
      }
      else
      {
        if(! show)
        {
          outstanding[0]     -= generalUtils.doubleDPs(generalUtils.doubleFromStr(amount), '2');
          baseOutstanding[0] -= generalUtils.doubleDPs(generalUtils.doubleFromStr(baseAmount), '2');
        }
        else
        {
          writeBodyLinePurchaseCreditNote(out, code, date, amount, baseAmount, currency, baseCurrency, firstCurrency, mixedCurrencies, cssFormat, first, inUnattached, localDefnsDir, defnsDir, bytesOut);
        }
      }
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void setHead(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, boolean plain, String p1, String p2, String p3, String p4,
                       String p5, String p6, String p7, String unm, String sid, String uty, String men, String den,
                       String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Purchase Invoice Settlement</title>");
    scoutln(out, bytesOut, "<SCRIPT LANGUAGE=\"JavaScript\">");

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 5080, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function invoice(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/PurchaseInvoicePage?unm=" + unm + "&sid=" + sid + "&uty="
                           + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 5036, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function pdebitNote(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/PurchaseDebitNotePage?unm=" + unm + "&sid=" + sid + "&uty="
                           + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 5026, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function pcredit(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/PurchaseCreditNotePage?unm=" + unm + "&sid=" + sid + "&uty="
                           + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 5049, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function payment(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/PaymentPage?unm=" + unm + "&sid=" + sid + "&uty="
                           + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 5058, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function ap(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/_5058?unm=" + unm + "&sid=" + sid + "&uty="
                           + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
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
  
    if(plain)
    {
      scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"print\" href=\""
                             + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "generalPlain.css\">");
    }
    else
    {
      scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\""
                             + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");
    }

    outputPageFrame(con, stmt, rs, out, req, plain, p1, p2, p3, p4, p5, p6, "Purchase Invoice Settlement", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);
    
    scoutln(out, bytesOut, "<form><table id=\"page\" width=100% border=0>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void startBody(PrintWriter out, String code, String companyName, String dateFrom, String dateTo, String dateCut, int[] bytesOut)
                         throws Exception
  {
    scoutln(out, bytesOut, "<table id='page' width=100%>");

    scoutln(out, bytesOut, "<tr><td colspan=6><p>Purchase Invoices for " + code + " (" + companyName + ") - Dates: " + dateFrom + " to " + dateTo
                         + "</td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=6><p>Cut-Off Date: " + dateCut + "</td></tr>");//</table>");

    scoutln(out, bytesOut, "<tr id='pageColumn'><td><p>Document</td><td nowrap><p>Invoice Code</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Supplier Invoice</td><td nowrap><p>Issue Currency</td><td><p>Date</td><td nowrap><p>Base Currency</td>");
    scoutln(out, bytesOut, "<td><p> Note </td></tr>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void writeBodyLinePurchaseInvoice(PrintWriter out, String code, String date, String amount, String baseAmount,
                                            String currency, String baseCurrency, String[] firstCurrency, boolean[] mixedCurrencies,
                                            String[] cssFormat, String notes, String salesPerson, String suppInvoiceCode,
                                            boolean[] first, String localDefnsDir, String defnsDir, int[] bytesOut)
                                            throws Exception
  {
    if(first[0])
    {
      first[0] = false;
      firstCurrency[0] = currency;
    }
    else
    {
      if(! currency.equals(firstCurrency[0]))
        mixedCurrencies[0] = true;
    }

    if(! cssFormat[0].equals("line1"))
      cssFormat[0] = "line1";
    else cssFormat[0] = "line2";

    scoutln(out, bytesOut, "<tr id=\"" + cssFormat[0] + "\"><td><span id=\"textInvoice\">Invoice&nbsp;</span></td><td nowrap>");
    scoutln(out, bytesOut, "<a href=\"javascript:invoice('" + code + "')\">" + code + "</a></td><td nowrap><span id=\"textInvoice\">");
    scoutln(out, bytesOut, suppInvoiceCode + "</span></td>");    
    scoutln(out, bytesOut, "<td nowrap align=right><span id=\"textInvoice\"> &nbsp; " + currency + " " + generalUtils.doubleDPs(amount, '2')
              + "</span></td>");
    scoutln(out, bytesOut, "<td nowrap><span id=\"textInvoice\"> &nbsp; Invoiced on " + generalUtils.convertFromYYYYMMDD(date) + "</span></td>");
    scoutln(out, bytesOut, "<td align=right nowrap><span id=\"textInvoice\"> &nbsp; (" + baseCurrency + " " + generalUtils.doubleDPs(baseAmount, '2')
              + ")</span></td>");
    scoutln(out, bytesOut, "<td nowrap><span id=\"textInvoice\"> &nbsp; " + salesPerson + " : " + notes + "</span></td></tr>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void writeBodyLinePurchaseDebitNote(PrintWriter out, String code, String date, String amount, String baseAmount,
                                              String currency, String baseCurrency, String[] firstCurrency,
                                              boolean[] mixedCurrencies, String[] cssFormat, String notes, String salesPerson,
                                              boolean[] first, String localDefnsDir, String defnsDir, int[] bytesOut)
                                              throws Exception
  {
    if(first[0])
    {
      first[0] = false;
      firstCurrency[0] = currency;
    }
    else
    {
      if(! currency.equals(firstCurrency[0]))
        mixedCurrencies[0] = true;
    }

    if(! cssFormat[0].equals("line1"))
      cssFormat[0] = "line1";
    else cssFormat[0] = "line2";

    scoutln(out, bytesOut, "<tr id=\"" + cssFormat[0] + "\"><td><span id=\"textInvoice\">Purchase Debit Note&nbsp;</span></td><td nowrap>");
    scoutln(out, bytesOut, "<a href=\"javascript:pdebitNote('" + code + "')\">" + code + "</a></td><td></td>");
    scoutln(out, bytesOut, "<td nowrap align=right><span id=\"textInvoice\"> &nbsp; " + currency + " " + generalUtils.doubleDPs(amount, '2')
              + "</span></td>");
    scoutln(out, bytesOut, "<td nowrap><span id=\"textInvoice\"> &nbsp; Issued on " + generalUtils.convertFromYYYYMMDD(date) + "</span></td>");
    scoutln(out, bytesOut, "<td align=right nowrap><span id=\"textInvoice\"> &nbsp; " + baseCurrency + " " + generalUtils.doubleDPs(baseAmount, '2')
              + "</span></td>");
    scoutln(out, bytesOut, "<td nowrap><span id=\"textInvoice\"> &nbsp; " + salesPerson + " : " + notes + "</span></td></tr>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void writeBodyLine(PrintWriter out, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<tr><td><span id=\"textInvoice\">No Invoices</span></td></tr>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void writeBodyLinePayment(PrintWriter out, String code, String date, String amount, String baseAmount, String currency,
                                    String baseCurrency, String[] firstCurrency, boolean[] mixedCurrencies, String[] cssFormat,
                                    boolean[] first, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    if(first[0])
    {
      first[0] = false;
      firstCurrency[0] = currency;
    }
    else
    {
      if(! currency.equals(firstCurrency[0]))
        mixedCurrencies[0] = true;
    }

    if(! cssFormat[0].equals("line1"))
      cssFormat[0] = "line1";
    else cssFormat[0] = "line2";

    scoutln(out, bytesOut, "<tr id=\"" + cssFormat[0] + "\"><td><span id=\"textReceipt\">Payment&nbsp;</td><td nowrap>"
                + "<a href=\"javascript:payment('" + code + "')\">" + code + "</a></td><td></td>");
    scoutln(out, bytesOut, "<td nowrap align=right><span id=\"textReceipt\"> &nbsp; " + currency + " " 
                + generalUtils.doubleDPs(amount, '2') + "</span></td>");
    scoutln(out, bytesOut, "<td nowrap><span id=\"textReceipt\"> &nbsp; Paid on " + generalUtils.convertFromYYYYMMDD(date) + "</span></td>");
    scoutln(out, bytesOut, "<td align=right nowrap><span id=\"textReceipt\">" + baseCurrency + " " 
                + generalUtils.doubleDPs(baseAmount, '2') + "</span></td>");
    scoutln(out, bytesOut, "<td></td></tr>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void writeBodyLinePurchaseCreditNote(PrintWriter out, String code, String date, String amount, String baseAmount,
                                               String currency, String baseCurrency, String[] firstCurrency,
                                               boolean[] mixedCurrencies, String[] cssFormat, boolean[] first,
                                               boolean inUnattached, String localDefnsDir, String defnsDir, int[] bytesOut)
                                               throws Exception
  {
    if(first[0])
    {
      first[0] = false;
      firstCurrency[0] = currency;
    }
    else
    {
      if(! currency.equals(firstCurrency[0]))
        mixedCurrencies[0] = true;
    }

    if(! cssFormat[0].equals("line1"))
      cssFormat[0] = "line1";
    else cssFormat[0] = "line2";

    if(inUnattached)
    {
      scoutln(out, bytesOut, "<tr id=\"" + cssFormat[0] + "\"><td><span id=\"textRedHighlighting\">Unattached Purchase Credit Note&nbsp;</span></td>");
      scoutln(out, bytesOut, "<td nowrap><a href=\"javascript:pcredit('" + code + "')\">" + code + "</a></td><td></td>");
      scoutln(out, bytesOut, "<td nowrap align=right><span id=\"textRedHighlighting\"> &nbsp; " + currency + " " 
                  + generalUtils.doubleDPs(amount, '2') + "</span></td>");
      scoutln(out, bytesOut, "<td nowrap><span id=\"textRedHighlighting\"> &nbsp; Issued on " + generalUtils.convertFromYYYYMMDD(date) + "</span></td>");
      scoutln(out, bytesOut, "<td nowrap align=right><span id=\"textRedHighlighting\">(" + baseCurrency + " " 
                  + generalUtils.doubleDPs(baseAmount, '2') + ")</span></td></tr>");
    }
    else
    {
      scoutln(out, bytesOut, "<tr id=\"" + cssFormat[0] + "\"><td><span id=\"textCreditNote\">Purchase Credit Note&nbsp;</span></td>");
      scoutln(out, bytesOut, "<td nowrap><a href=\"javascript:pcredit('" + code + "')\">" + code + "</a></td>");
      scoutln(out, bytesOut, "<td nowrap align=right> &nbsp; " + currency + " " + generalUtils.doubleDPs(amount, '2') + "</td>");
      scoutln(out, bytesOut, "<td nowrap> &nbsp; Issued on " + generalUtils.convertFromYYYYMMDD(date) + "</td>");
      scoutln(out, bytesOut, "<td nowrap align=right>(" + baseCurrency + " " + generalUtils.doubleDPs(baseAmount, '2') + ")</td>");
      scoutln(out, bytesOut, "<td></td></tr>");
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void writeOutstanding(PrintWriter out, String cssFormat, double outstanding, double baseOutstanding, String baseCurrency,
                                String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\"><td></td><td>");
    if(generalUtils.doubleDPs(outstanding, '2') != 0)
      scoutln(out, bytesOut, "<span id=\"textRedHighlighting\"> ");
    else scoutln(out, bytesOut, "<span id=\"textBlack\">");
    scoutln(out, bytesOut, "Outstanding&nbsp;</span></td><td></td>");

    scoutln(out, bytesOut, "<td nowrap align=right><span id=\"textBlack\">" + generalUtils.doubleDPs('2', outstanding) + "</span></td>");
    scoutln(out, bytesOut, "<td>&nbsp;</td>");
    scoutln(out, bytesOut, "<td colspan=2>&nbsp;</td></tr>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void writeSettled(PrintWriter out, String[] cssFormat, int[] bytesOut) throws Exception
  {
    if(! cssFormat[0].equals("line1"))
      cssFormat[0] = "line1";
    else cssFormat[0] = "line2";

    scoutln(out, bytesOut, "<tr id=\"" + cssFormat[0] + "\"><td></td><td colspan=6><span id=\"textBlack\">Settled</span></td></tr>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void endBody(Connection con, Statement stmt, ResultSet rs, PrintWriter out, boolean mixedCurrencies, String firstCurrency, String baseCurrency,
                       double total, double baseTotal, double totalInvoices, double totalInvoicesBase, double totalDNs, double totalDNsBase,  double totalCNs,
                       double totalCNsBase, double totalPayments, double totalPaymentsBase, String unm, String dnm, String bnm, String localDefnsDir,
                       String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<tr><td colspan=7><hr></td></tr><tr><td></td><td><span id=\"textBlack\"><b>Total Outstanding</b></span></td>");

    if(mixedCurrencies)
      scoutln(out, bytesOut, "<td><span id=\"textRedHighlighting\"><b>ERROR!</b></span></td>");
    else 
    {
      scoutln(out, bytesOut, "<td nowrap align=right><span id=\"textBlack\"><b>" + firstCurrency + " "
                     + generalUtils.doubleDPs('2', total) + "</b></span></td>");
    }

    scoutln(out, bytesOut, "<td></td><td nowrap align=right><span id=\"textBlack\"><b>"
                + baseCurrency + " " + generalUtils.doubleDPs('2', baseTotal) + "</b></span></td>");
    scoutln(out, bytesOut, "<td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><span id=\"textBlack\">Total Purchase Invoices:</span></td>");
    if(mixedCurrencies)
      scoutln(out, bytesOut, "<td><span id=\"textRedHighlighting\"><b>Mixed Currencies</b></span></td>");
    else
    {
      scoutln(out, bytesOut, "<td nowrap align=right><span id=\"textBlack\">" + firstCurrency + " "
                  + generalUtils.doubleDPs('2', totalInvoices) + "</span></td>");
    }
    scoutln(out, bytesOut, "<td></td><td nowrap align=right><span id=\"textBlack\">" + baseCurrency + " "
                + generalUtils.doubleDPs('2', totalInvoicesBase) + "</span></td>");
    scoutln(out, bytesOut, "<td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><span id=\"textBlack\">Total Purchase Debit Notes:</span></td>");
    if(mixedCurrencies)
      scoutln(out, bytesOut, "<td></td>");
    else 
    {
      scoutln(out, bytesOut, "<td nowrap align=right><span id=\"textBlack\">" + firstCurrency + " "
                  + generalUtils.doubleDPs('2', totalDNs) + "</span></td>");
    }
    scoutln(out, bytesOut, "<td></td><td nowrap align=right><span id=\"textBlack\">" + baseCurrency + " "
                + generalUtils.doubleDPs('2', totalDNsBase) + "</span></td>");
    scoutln(out, bytesOut, "<td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><span id=\"textBlack\">Total Purchase Credit Notes:</span></td>");
    if(mixedCurrencies)
      scoutln(out, bytesOut, "<td></td>");
    else 
    {
      scoutln(out, bytesOut, "<td nowrap align=right><span id=\"textBlack\">" + firstCurrency + " "
                  + generalUtils.doubleDPs('2', totalCNs) + "</span></td>");
    }
    scoutln(out, bytesOut, "<td></td><td nowrap align=right><span id=\"textBlack\">" + baseCurrency + " "
                + generalUtils.doubleDPs('2', totalCNsBase) + "</span></td>");
    scoutln(out, bytesOut, "<td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><span id=\"textBlack\">Total Payments:</td>");
    if(mixedCurrencies)
      scoutln(out, bytesOut, "<td></td>");
    else
    {
      scoutln(out, bytesOut, "<td nowrap align=right><span id=\"textBlack\">" + firstCurrency + " "
                  + generalUtils.doubleDPs('2', totalPayments) + "</span></td>");
    }
    scoutln(out, bytesOut, "<td></td><td nowrap align=right><span id=\"textBlack\">" + baseCurrency + " "
                + generalUtils.doubleDPs('2', totalPaymentsBase) + "</span></td>");
    scoutln(out, bytesOut, "<td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir));
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean outputAnyCNsUnattached(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out,
                                         String companyCode, String baseCurrency, String[] firstCurrency, boolean[] mixedCurrencies,
                                         String[] cssFormat, boolean[] first, String dnm, String localDefnsDir, String defnsDir, int[] bytesOut)
                                         throws Exception
  {
    stmt = con.createStatement();
 
    rs = stmt.executeQuery("SELECT COUNT(*) as rowcount FROM pcredit;");
    
    int rowCount = 0;
    
    if(rs.next())
      rowCount = rs.getInt("rowcount");
    
    if(rs  != null) rs.close();
    if(stmt != null) stmt.close();

    if(rowCount == 0)
      return false;
    
    stmt = con.createStatement();

    // chk CNs for entries without valid invoice codes (blank entries or non-existant invoices)
    rs = stmt.executeQuery("SELECT t2.PCNCode, t2.Amount2, t2.Amount, t2.GSTRate, t1.Date, t1.Currency "
                                   + "FROM pcredit AS t1 INNER JOIN pcreditl AS t2 ON t1.PCNCode = t2.PCNCode WHERE t1.CompanyCode = '"
                                   + generalUtils.sanitiseForSQL(companyCode) + "' AND t1.Status != 'C' AND ( t2.InvoiceCode NOT IN (SELECT InvoiceCode "
                                   + "FROM pinvoice) AND t2.InvoiceCode NOT IN (SELECT PDNCode FROM pdebit) ) ORDER BY t1.Date, t2.PCNCode");

    String cnCode, amount, baseAmount, gstRate, dateIssued, currency;
    double thisBaseAmt, thisNonBaseAmt, gst;
    
    boolean oneFound = false;

    while(rs.next())
    {    
      cnCode      = rs.getString(1);
      amount      = rs.getString(2);
      baseAmount  = rs.getString(3);
      gstRate     = rs.getString(4);
      dateIssued  = rs.getString(5);
      currency    = rs.getString(6);
     
      thisBaseAmt    = generalUtils.doubleFromStr(baseAmount);
      thisNonBaseAmt = generalUtils.doubleFromStr(amount);

      gst = accountsUtils.getGSTRate(con, stmt2, rs2, gstRate);
      
      thisBaseAmt    += (thisBaseAmt * gst);
      thisNonBaseAmt += (thisNonBaseAmt * gst);

      writeBodyLinePurchaseCreditNote(out, cnCode, dateIssued, generalUtils.doubleToStr('2', thisNonBaseAmt),
                                      generalUtils.doubleToStr('2', thisBaseAmt), currency, baseCurrency, firstCurrency, mixedCurrencies,
                                      cssFormat, first, true, localDefnsDir, defnsDir, bytesOut);
      oneFound = true;
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    return oneFound;
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void outputPageFrame(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, boolean plain, String p1, String p2, String p3, String p4, String p5, String p6, String title, String unm, String sid,
                               String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    int[] hmenuCount = new int[1];

    String subMenuText = dashboardUtils.buildSubMenuText(con, stmt, rs, req, "5002", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount);

    subMenuText += "<table id='title' width=100%><tr><td colspan=2 nowrap><p>" + title + directoryUtils.buildHelp(5002) + "</td></tr></table>";

    subMenuText += buildSubMenuText(p1, p2, p3, p4, p5, p6, unm, sid, uty, men, den, dnm, bnm, hmenuCount);

    if(plain)
      scoutln(out, bytesOut, authenticationUtils.buildPlainScreen(con, stmt, rs, req, unm, sid, uty, men, den, dnm, bnm, "SupplierSettlementHistoryCreate", subMenuText, hmenuCount[0], "", "", localDefnsDir, defnsDir));
    else scoutln(out, bytesOut, authenticationUtils.buildScreen(con, stmt, rs, req, true, unm, sid, uty, men, den, dnm, bnm, "SupplierSettlementHistoryCreate", subMenuText, hmenuCount[0], "", "", localDefnsDir, defnsDir));

    scoutln(out, bytesOut, "<table border=0><tr><td valign=top><div id='second' style=\"{margin-top:0px;}\"></div></td>");
    scoutln(out, bytesOut, "<td valign=top><div id='third' style=\"{margin-top:0px;margin-left:0px;}\"></div></td></tr></table>");
  }
    
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private String buildSubMenuText(String p1, String p2, String p3, String p4, String p5, String p6, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] hmenuCount) throws Exception
  {
    String s = "<div id='submenu'>";

    ++hmenuCount[0];

    s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\">";
    s += "<a href=\"/central/servlet/SupplierSettlementHistoryCreate?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1="
      + p1 + "&p2=" + p2 + "&p3=" + p3 + "&p4=" + p4 + "&p5=" + p5 + "&p6=" + p6 + "&p7=P\">Friendly</a></dt></dl>";
    
    s += "</div>";
    
    --hmenuCount[0];
    
    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
