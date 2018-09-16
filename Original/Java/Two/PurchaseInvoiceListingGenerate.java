// =======================================================================================================================================================================================================
// System: ZaraStar Analytic: Purchase invoice listing enquiry by date - search
// Module: PurchaseInvoiceListingGenerate.java
// Author: C.K.Harvey
// Copyright (c) 2002-09 Christopher Harvey. All Rights Reserved.
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
import java.util.Enumeration;

public class PurchaseInvoiceListingGenerate extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  AccountsUtils accountsUtils = new AccountsUtils();
  Supplier supplier = new Supplier();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", dateFrom="", dateTo="";

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
      dateFrom = req.getParameter("dateFrom");
      dateTo   = req.getParameter("dateTo");

      doIt(out, req, dateFrom, dateTo, "", "on", "on", "S", unm, sid, uty, men, den, dnm, bnm, bytesOut);
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
 
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "PurchaseInvoiceListingGenerate", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1034, bytesOut[0], 0, "ERR:" + dateFrom + " " + dateTo);
      if(out != null) out.flush(); 
    }
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", dateFrom="", dateTo="", suppCode="", summaryOrDetail="", invoiceTypeCash="", invoiceTypeAccounts="";

    try
    {
      directoryUtils.setContentHeaders(res);
      out = res.getWriter();

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
        else
        if(name.equals("dateFrom"))
          dateFrom = value[0];
        else
        if(name.equals("dateTo"))
          dateTo = value[0];
        else
        if(name.equals("suppCode"))
          suppCode = value[0];
        else
        if(name.equals("summaryOrDetail"))
          summaryOrDetail = value[0];
        else
        if(name.equals("invoiceTypeAccounts"))
          invoiceTypeAccounts = value[0];
        else
        if(name.equals("invoiceTypeCash"))
          invoiceTypeCash = value[0];
      }

      if(dateFrom == null || dateFrom.equalsIgnoreCase("null"))
        dateFrom = "";

      if(dateTo == null || dateTo.equalsIgnoreCase("null"))
        dateTo = "";

      if(suppCode == null || suppCode.equalsIgnoreCase("null"))
        suppCode = "";

      doIt(out, req, dateFrom, dateTo, suppCode, invoiceTypeAccounts, invoiceTypeCash, summaryOrDetail, unm, sid, uty, men, den, dnm, bnm, bytesOut);
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
 
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "PurchaseInvoiceListingGenerate", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1034, bytesOut[0], 0, "ERR:" + dateFrom + " " + dateTo);
      if(out != null) out.flush(); 
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String dateFrom, String dateTo, String suppCode, String invoiceTypeAccounts, String invoiceTypeCash, String summaryOrDetail, String unm, String sid, String uty, String men, String den,
                    String dnm, String bnm, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    
    Statement stmt = null;
    ResultSet rs   = null;
    
    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 1034, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "PurchaseInvoiceListingGenerate", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1034, bytesOut[0], 0, "ACC:" + dateFrom + " " + dateTo);
      
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "PurchaseInvoiceListingGenerate", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1034, bytesOut[0], 0, "SID:" + dateFrom + " " + dateTo);
      
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    set(con, stmt, rs, out, req, suppCode, invoiceTypeAccounts, invoiceTypeCash, dateFrom, dateTo, summaryOrDetail, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 1034, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), dateFrom + " " + dateTo);
    
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String suppCode, String invoiceTypeAccounts, String invoiceTypeCash, String dateFrom, String dateTo,
                   String summaryOrDetail, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Purchase Invoice Listing Enquiry by Date</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");
    
    scoutln(out, bytesOut, "function view(code){");
    scoutln(out, bytesOut, "var code2=sanitise(code);");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/PurchaseInvoicePage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p1=\"+code2;}");

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

    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\"></head>");

    int[] hmenuCount = new int[1];
    
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "1034", "", "PurchaseInvoiceListing", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
                                
    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Purchase Invoice Listing Enquiry by Date", "1034",  unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    suppCode = suppCode.toUpperCase();
    
    String dateFromText;
    if(dateFrom.length() == 0)
    {
      dateFromText = "Start";
      dateFrom = "1970-01-01";
    }
    else
    {
      dateFromText = dateFrom;
      dateFrom = generalUtils.convertDateToSQLFormat(dateFrom);
    }
    
    String dateToText;
    if(dateTo.length() == 0)
    {
      dateToText = "Finish";
      dateTo = "2099-12-31";
    }
    else
    {
      dateToText = dateTo;
      dateTo = generalUtils.convertDateToSQLFormat(dateTo);
    }
    
    char type=' ';
    if(invoiceTypeCash.equals("on"))
    {
      if(invoiceTypeAccounts.equals("on"))
        type = 'B';
      else type = 'C';
    }
    else    
    if(invoiceTypeAccounts.equals("on"))
    {
      type = 'A';
    }

    scoutln(out, bytesOut, "<table id=\"page\" width=100% border=0>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    if(suppCode.length() > 0)
      scoutln(out, bytesOut, "<tr><td><p>For Supplier: " + suppCode);
    else scoutln(out, bytesOut, "<tr><td><p>For ALL Suppliers");

    scoutln(out, bytesOut, "</td></tr><tr><td><p>For Date Range: " + dateFromText + " to " + dateToText);

    scoutln(out, bytesOut, "</td></tr><tr><td><p>Invoice Category: ");
    switch(type)
    {
      case 'C' : scoutln(out, bytesOut, "Cash");                  break;
      case 'A' : scoutln(out, bytesOut, "Account");               break;
      default  : scoutln(out, bytesOut, "Both Cash and Account"); break;
    }
    scoutln(out, bytesOut, "</td></tr><tr><td>&nbsp;</td></tr></table>");

    boolean accounts = false;
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 109, unm, uty, dnm, localDefnsDir, defnsDir))
      accounts = true;

    String baseCurrency = accountsUtils.getBaseCurrency(con, stmt, rs, dnm, localDefnsDir, defnsDir);
    
    generate(con, stmt, rs, out, type, dnm, suppCode, dateFrom, dateTo, summaryOrDetail, accounts, baseCurrency, localDefnsDir, defnsDir, bytesOut);

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void generate(Connection con, Statement stmt, ResultSet rs, PrintWriter out, char type, String dnm, String suppCode, String dateFrom, String dateTo, String summaryOrDetail, boolean accounts, String baseCurrency, String localDefnsDir,
                        String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<table id=\"page\" width=100% cellspacing=2 cellpadding=2 border=0>");
    scoutln(out, bytesOut, "<tr id=\"pageColumn\">");
    
    if(summaryOrDetail.equals("D"))
    {
      scoutln(out, bytesOut, "<td><p>Invoice</td>");
      scoutln(out, bytesOut, "<td><p>Supplier Invoice</td>");
      scoutln(out, bytesOut, "<td><p>Supplier</td>");
      scoutln(out, bytesOut, "<td><p>Date</td>");
      scoutln(out, bytesOut, "<td align=right><p>Total</td>");
      scoutln(out, bytesOut, "<td align=right><p>GST Total</td>");
      scoutln(out, bytesOut, "<td><p>Currency</td>");
      scoutln(out, bytesOut, "<td align=right><p>Base (" + baseCurrency + ") Total</td></tr>");
    }
    else // summary
    {    
      scoutln(out, bytesOut, "<td></td>");
      scoutln(out, bytesOut, "<td><p>Supplier</td>");
      scoutln(out, bytesOut, "<td></td>");
      scoutln(out, bytesOut, "<td align=right><p>Total</td>");
      scoutln(out, bytesOut, "<td><p>Currency</td>");
      scoutln(out, bytesOut, "<td align=right><p>Base Total</td></tr>");
    }
    
    String[][] currencies = new String[1][];  
    int x, numCurrencies = accountsUtils.getCurrencyNames(con, stmt, rs, currencies, dnm, localDefnsDir, defnsDir);
    double[] grandTotals = new double[numCurrencies];
    for(x=0;x<numCurrencies;++x)
      grandTotals[0] = 0.0;
    double[] companyTotals = new double[numCurrencies];
    
    double companyBaseTotal = 0.0, baseGrandTotal = 0.0;

    int numInvoices = 0;

    stmt = con.createStatement();
  
    String suppStr = "";
    if(suppCode.length() > 0)
      suppStr = "CompanyCode = '" + suppCode + "' AND ";
    
    String cashOrAccountStr = ""; // type = both
    if(type == 'C')
      cashOrAccountStr = " CashOrAccount = 'C' AND ";
    else
    if(type == 'A')
      cashOrAccountStr = " CashOrAccount != 'C' AND ";
    
    rs = stmt.executeQuery("SELECT InvoiceCode, Date, CompanyCode, CompanyName, TotalTotal, CashOrAccount, Currency, SupplierInvoiceCode, GSTTotal, BaseTotalTotal FROM pinvoice WHERE " + suppStr + cashOrAccountStr + "Status != 'C' && Date >= {d '"
                         + dateFrom + "'} AND Date <= {d '" + dateTo + "'} ORDER BY CompanyCode, Date");

    String s, code="", date="", companyCode=" ", lastCompanyCode="", name="", totalTotal, cashOrAccount, currency, suppInvoiceCode, gstTotal, baseTotalTotal, cssFormat="";

    double amt;

    while(rs.next())                  
    {
      cashOrAccount = rs.getString(6);
      currency      = rs.getString(7);
      totalTotal    = rs.getString(5);       
      companyCode   = rs.getString(3);
      name          = rs.getString(4);
      suppInvoiceCode = rs.getString(8);
      gstTotal        = rs.getString(9);
      baseTotalTotal  = rs.getString(10);

      if(! lastCompanyCode.equals(companyCode))
      {
        if(lastCompanyCode.length() > 0)
        {
          for(x=0;x<numCurrencies;++x)
          {
            if(generalUtils.doubleDPs(companyTotals[x], '2') != 0.0)
            {
              s = supplier.getSupplierNameGivenCode(con, stmt, rs, lastCompanyCode);

              if(summaryOrDetail.equals("D"))
                scoutln(out, bytesOut, "<tr><td colspan=5><p>");
              else scoutln(out, bytesOut, "<tr><td colspan=4><p>");

              scoutln(out, bytesOut, lastCompanyCode + " (" + s + ")</td><td align=right><p>" + generalUtils.formatNumeric(companyTotals[x], '2') + "</td><td><p>" + currencies[0][x] + "</td><td align=right><p>"
                                    + generalUtils.formatNumeric(companyBaseTotal, '2') + "</td></tr>");
            }
          }
        }

        for(x=0;x<numCurrencies;++x)
          companyTotals[x] = 0.0;

        companyBaseTotal = 0.0;

        lastCompanyCode = companyCode;
      }
      
      if(summaryOrDetail.equals("D"))
      {
        code = rs.getString(1);
        date = rs.getString(2);
      }

      if(type == 'B' || (type == 'C' && cashOrAccount.equals("C")) || (type == 'A' && cashOrAccount.equals("A")))
      {
        ++numInvoices;
            
        for(x=0;x<numCurrencies;++x)
        {
          if(currency.equals(currencies[0][x]))
          {
            amt = generalUtils.doubleFromStr(generalUtils.doubleDPs(totalTotal, '2'));          
            grandTotals[x]   += amt;
            companyTotals[x] += amt;
            x = numCurrencies;
          }
        }
        
        amt = generalUtils.doubleFromStr(generalUtils.doubleDPs(baseTotalTotal, '2'));
        companyBaseTotal += amt;
        baseGrandTotal += amt;

        if(summaryOrDetail.equals("D"))
        {
          if(cssFormat.equals("line2")) cssFormat = "line1"; else cssFormat = "line2";

          scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\">");
          scoutln(out, bytesOut, "<td nowrap><p><a href=\"javascript:view('" + code + "')\">" + code + "</a></td>");
          scoutln(out, bytesOut, "<td nowrap><p>" + suppInvoiceCode + " (" + name + ")</td>");
          scoutln(out, bytesOut, "<td nowrap><p>" + companyCode + " (" + name + ")</td>");
          scoutln(out, bytesOut, "<td nowrap><p>" + generalUtils.convertFromYYYYMMDD(date) + "</td>");
          scoutln(out, bytesOut, "<td nowrap align=right><p>" + generalUtils.doubleDPs(gstTotal, '2') + "&nbsp; </td>");
          scoutln(out, bytesOut, "<td nowrap align=right><p>" + generalUtils.doubleDPs(totalTotal, '2') + "&nbsp; </td>");
          scoutln(out, bytesOut, "<td nowrap><p>" + currency + "</td>");
          scoutln(out, bytesOut, "<td nowrap align=right><p>" + generalUtils.doubleDPs(baseTotalTotal, '2') + "&nbsp; </td></tr>");
        }
      }
    }
  
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  
    if(lastCompanyCode.length() > 0)
    {
      for(x=0;x<numCurrencies;++x)
      {
        if(generalUtils.doubleDPs(companyTotals[x], '2') != 0.0)
        {
          s = supplier.getSupplierNameGivenCode(con, stmt, rs, lastCompanyCode);
   
          if(summaryOrDetail.equals("D"))
            scoutln(out, bytesOut, "<tr><td colspan=4><p>");
          else scoutln(out, bytesOut, "<tr><td colspan=3><p>");

          scoutln(out, bytesOut, companyCode + " (" + s + ")</td><td align=right><p>" + generalUtils.formatNumeric(companyTotals[x], '2') + "</td><td><p>" + currencies[0][x] + "</td><td align=right><p>"
                                 + generalUtils.formatNumeric(companyBaseTotal, '2') + "</td></tr>");
        }
      }
    }

    if(numInvoices == 1)
      scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><b>1 invoice</td></tr>");
    else scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><b>" + generalUtils.formatNumeric(numInvoices, '0') + " invoices</td></tr>");
    
    scoutln(out, bytesOut, "<tr><td colspan=9><hr></td></tr>");

    for(x=0;x<numCurrencies;++x)
    {
      if(generalUtils.doubleDPs(grandTotals[x], '2') != 0.0)
      {
        scoutln(out, bytesOut, "<tr><td colspan=4 align=right><p>" + currencies[0][x] + "</td><td><p>" + generalUtils.formatNumeric(grandTotals[x], '2') + "</td></tr>");
      }
    }    

    if(accounts)
      scoutln(out, bytesOut, "<tr><td colspan=4 align=right><p>" + baseCurrency + " at Issue Date:</td><td><p>" + generalUtils.formatNumeric(baseGrandTotal, '2') + "</td></tr>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
