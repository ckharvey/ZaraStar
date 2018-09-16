// =======================================================================================================================================================================================================
// System: ZaraStar Document: AP: create initial AP builder page
// Module: AccountsPayableBuilderPage.java
// Author: C.K.Harvey
// Copyright (c) 1999-2008 Christopher Harvey. All Rights Reserved.
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

public class AccountsPayableBuilderPage extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  Supplier supplier = new Supplier();
  MiscDefinitions miscDefinitions = new MiscDefinitions();
  DocumentUtils documentUtils = new DocumentUtils();
  AccountsUtils accountsUtils = new AccountsUtils();
  
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="", p3="";
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
      p1  = req.getParameter("p1"); // suppCode
      p2  = req.getParameter("p2"); // type
      p3  = req.getParameter("p3"); // monthYear

      if(p1 == null) p1 = "";
      if(p2 == null) p2 = " ";
      
      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, p3, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "AccountsPayableBuilderPage", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 5047, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                    String p1, String p2, String p3, int[] bytesOut) throws Exception
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
    Statement stmt2 = null;
    ResultSet rs2   = null;
    Statement stmt3 = null;
    ResultSet rs3   = null;
    
    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 5047, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "5047", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 5047, bytesOut[0], 0, "ACC:");
    if(con  != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "5047", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 5047, bytesOut[0], 0, "SID:");
    if(con  != null) con.close();
      if(out != null) out.flush();
      return;
    }

    p1 = p1.toUpperCase();

    set(con, stmt, stmt2, stmt3, rs, rs2, rs3, out, req, p1, p2, p3, unm, sid, uty, men, den, dnm, bnm, workingDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 5047, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con  != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, ResultSet rs3, PrintWriter out,
                   HttpServletRequest req, String code, String type, String monthYear, String unm, String sid, String uty, String men, String den, String dnm,
                   String bnm, String workingDir, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>AP Builder</title>");
    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 5080, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewInvoice(code){");
      scoutln(out, bytesOut, "var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/PurchaseInvoicePage?unm=" + unm + "&sid=" + sid + "&uty="
                           + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 5036, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewPDN(code){");
      scoutln(out, bytesOut, "var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/PurchaseDebitNotePage?unm=" + unm + "&sid=" + sid + "&uty="
                           + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 5026, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewPCN(code){");
      scoutln(out, bytesOut, "var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/PurchaseCreditNotePage?unm=" + unm + "&sid=" + sid + "&uty="
                           + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 5049, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewPayment(code){");
      scoutln(out, bytesOut, "var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/PaymentPage?unm=" + unm + "&sid=" + sid + "&uty="
                           + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }
    
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 6066, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewVoucher(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/PaymentVoucherPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den
                           + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    scoutln(out, bytesOut, "function create(){document.lines.submit();}");
        
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

    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" 
                           + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    int[] hmenuCount = new int[1];
 
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "5047", "", "AccountsPayableBuilderPage", unm, sid, uty, men, den, dnm, bnm,
                          localDefnsDir, defnsDir, hmenuCount, bytesOut);

    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "AP Builder", "5047", unm, sid, uty, men, den,
                    dnm, bnm, hmenuCount, bytesOut);
      
    scoutln(out, bytesOut, "<form action=\"AccountsPayableBuilderCreate\" name=lines enctype=\"application/x-www-form-urlencoded\" method=POST>");
    scoutln(out, bytesOut, "<input type=hidden name=unm value=" + unm + ">");
    scoutln(out, bytesOut, "<input type=hidden name=sid value=" + sid + ">");
    scoutln(out, bytesOut, "<input type=hidden name=uty value=" + uty + ">");
    scoutln(out, bytesOut, "<input type=hidden name=men value=" + men + ">");
    scoutln(out, bytesOut, "<input type=hidden name=den value=" + den + ">");
    scoutln(out, bytesOut, "<input type=hidden name=dnm value=" + dnm + ">");
    scoutln(out, bytesOut, "<input type=hidden name=bnm value=" + bnm + ">");
    scoutln(out, bytesOut, "<input type=hidden name=companyCode value=" + code + ">");
    scoutln(out, bytesOut, "<input type=hidden name=cashOrNot value=" + type + ">");
    
    scoutln(out, bytesOut, "<table id=\"page\" border=0 cellspacing=2 cellpadding=0 width=100%>");

    String name;
    if(type.equals(" "))
      name = supplier.getASupplierFieldGivenCode(con, stmt, rs, "Name", code);
    else
    {
      code = "Cash Invoices";
      name = "Flagged as 'Cash'";
    }

    String[] dateFrom = new String[1];
    String[] dateTo   = new String[1];
  
    generalUtils.monthYearStrToYYYYMMDDDates(monthYear, dateFrom, dateTo);
    
    String today = generalUtils.today(localDefnsDir, defnsDir);

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td><p>Outstanding for: " + code + " (" + name + ") for "
                           + generalUtils.monthYearToMonthYearStr(monthYear) + "</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr></table>");

    scoutln(out, bytesOut, "<table id=\"page\" border=0 cellspacing=2 cellpadding=0>");

    scoutln(out, bytesOut, "<tr><td nowrap><p>&nbsp; AP Date &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p><input type=text name=apDate value=\"" + today + "\" size=10 maxlength=10></td>");

    scoutln(out, bytesOut, "<td><span id=\"optional\">The date that this data entry was done. No other relevance.</span></td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap><p>&nbsp; Date Paid &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p><input type=text name=datePaid value=\"" + today + "\" size=10 maxlength=10></td>");

    scoutln(out, bytesOut, "<td><span id=\"optional\">The date the payment was made. Determines the processing month.</span></td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap><p>&nbsp; Cheque # &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p><input type=text name=chequeNumber size=15 maxlength=20></td>");

    scoutln(out, bytesOut, "<td><span id=\"optional\">For later cross-reference checking.</span></td></tr>");

    scoutln(out, bytesOut, "<td nowrap><p>&nbsp; Debit Account &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>&nbsp; " + code + "&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap><p>&nbsp; Total Amount &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p><input type=text name=totalAmount size=15 maxlength=20></td>");

    scoutln(out, bytesOut, "<td><span id=\"optional\">The total amount being paid. Used to cross-check individual amounts entered below.</span></td></tr>");

    scoutln(out, bytesOut, "<tr><td valign=top nowrap><p>&nbsp; Adjustment &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p><input type=text name=discountAllowed size=10 maxlength=20>");
    scoutln(out, bytesOut, "<br><input type=radio name=adj checked value='D'>Discount");
    scoutln(out, bytesOut, "<br><input type=radio name=adj value='O'>Overpaid</td>");

    scoutln(out, bytesOut, "<td valign=top><span id=\"optional\">Used to account for minor discrepanies when total received is an over/under payment.</span></td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap><p>&nbsp; Charges &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p><input type=text name=charges size=10 maxlength=20></td>");

    scoutln(out, bytesOut, "<td><span id=\"optional\">The amount as appears on the bank advice.</span></td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap><p>&nbsp; Rate &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p><input type=text name=rate value=\"1.00\" size=10 maxlength=20></td>");

    scoutln(out, bytesOut, "<td><span id=\"optional\">The current exchange rate.</span></td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap><p>&nbsp; Bank Reference &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p><input type=text name=paymentReference size=10 maxlength=20></td>");

    scoutln(out, bytesOut, "<td><span id=\"optional\">The number allocated to the payment slip.</span></td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap><p>&nbsp; Bank Amount &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p><input type=text name=bankAmount size=10 maxlength=20></td>");

    scoutln(out, bytesOut, "<td><span id=\"optional\">The actual amount paid from the bank (minus any charges). This is the amount that will appear on the bank statement.</span></td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap><p>&nbsp; Credit Account &nbsp;</td><td><p>");
    scoutln(out, bytesOut, accountsUtils.getAROrAPAccountsDDL('P', "crAccount", "", unm, dnm, workingDir));
    scoutln(out, bytesOut, "</td><td><span id=\"optional\">The bank (or other) account from which the payment is being made.</span></td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap><p>&nbsp; Note &nbsp;</td>");
    scoutln(out, bytesOut, "<td colspan=5 nowrap><p><input type=text name=note size=70 maxlength=80></td>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr><tr>");

    scoutln(out, bytesOut, "</table><table id=\"page\" cellspacing=4 cellpadding=0><tr id=\"pageColumn\">");

    // draw column header
    scoutln(out, bytesOut, "<td align=center nowrap><p>&nbsp; Type &nbsp;</td>");
    scoutln(out, bytesOut, "<td align=center nowrap><p>&nbsp; Invoice &nbsp;<br>&nbsp; Date &nbsp;</td>");
    scoutln(out, bytesOut, "<td align=center nowrap><p>&nbsp; Invoice &nbsp;<br>&nbsp; Code &nbsp;</td>");
    scoutln(out, bytesOut, "<td align=center nowrap><p>&nbsp; Supplier &nbsp; <br> &nbsp;Invoice &nbsp;<br>&nbsp; Code &nbsp;</td>");
    scoutln(out, bytesOut, "<td align=center nowrap><p>&nbsp; Original &nbsp;<br>&nbsp; Amount &nbsp;</td>");
    scoutln(out, bytesOut, "<td align=center nowrap><p>&nbsp; Outstanding &nbsp;<br>&nbsp; Amount &nbsp;</td>");
    scoutln(out, bytesOut, "<td align=center nowrap><p>&nbsp; Currency &nbsp;</td>");
    scoutln(out, bytesOut, "<td align=center nowrap><p>&nbsp; Amount &nbsp;<br>&nbsp; Paid &nbsp;</td></tr>");

    String[] invoicesArg = new String[1];  invoicesArg[0] = "";
    String[] typesArg    = new String[1];  typesArg[0] = "";

    boolean[] firstUnattachedCN = new boolean[1];  firstUnattachedCN[0] = true;
    int[] count = new int[1];  count[0] = 0;
    
    if(type.equals(" "))
    {
      detInvoicesGivenSupplier(con, stmt, stmt2, stmt3, rs, rs2, rs3, out, code, dateFrom[0], dateTo[0], dnm, count, invoicesArg, typesArg, 
                               localDefnsDir, defnsDir, firstUnattachedCN, bytesOut);
      detDebitNotesGivenSupplier(con, stmt, stmt2, stmt3, rs, rs2, rs3, out, code, dateFrom[0], dateTo[0], dnm, count, invoicesArg, typesArg, 
                                 localDefnsDir, defnsDir, firstUnattachedCN, bytesOut);
    }
    else
    {
      detInvoicesCashOnly(con, stmt, stmt2, stmt2, rs, rs2, rs3, out, code, dateFrom[0], dateTo[0], dnm, count, invoicesArg, typesArg, 
                          localDefnsDir, defnsDir, firstUnattachedCN, bytesOut);
      detDebitNotesCashOnly(con, stmt, stmt2, stmt3, rs, rs2, rs3, out, code, dateFrom[0], dateTo[0], dnm, count, invoicesArg, typesArg, 
                          localDefnsDir, defnsDir, firstUnattachedCN, bytesOut);
    }

    unattachedCreditNoteLines(con, stmt, stmt2, rs, rs2, out, code, dnm, firstUnattachedCN, localDefnsDir, defnsDir, bytesOut);

    scoutln(out, bytesOut, "<input type=hidden name=invoices value=" + invoicesArg[0] + ">");
    scoutln(out, bytesOut, "<input type=hidden name=types value="    + typesArg[0] + ">");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");
    scoutln(out, bytesOut, "function runningTotal(){");
    scoutln(out, bytesOut, "var x,y=0;for(x=0;x<" + count[0] + ";++x){y+=((document.forms[0].elements['a'+x].value).toInt());}  ");    
    scoutln(out, bytesOut, "document.getElementById('msg').innerHTML='<span id=\"textErrorLarge\">'+roundNumber(y)+'</span>';}");

    scoutln(out, bytesOut, "function useInvoice(count,outstanding){;");
    scoutln(out, bytesOut, "document.forms[0].elements['a'+count].value=outstanding; runningTotal();}  ");

    scoutln(out, bytesOut, "</script>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=6 align=right><font color=red><span id=\"msg\"></span></td></tr>");

    scoutln(out, bytesOut, "</table><table id=\"page\" cellspacing=4 cellpadding=0><tr id=\"pageColumn\">");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=10><hr></td></tr>");
    String year = generalUtils.getFromDefnFile(unm, "accounts.dfn", workingDir, "");
    writePrePaymentLines(con, stmt, rs, out, code, year, dnm, localDefnsDir, defnsDir, bytesOut);

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap colspan=4><p><a href=\"javascript:create()\">Create</a> Payment</td></tr>");

    scoutln(out, bytesOut, "</table></form>");  
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void detInvoicesGivenSupplier(Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, ResultSet rs3,
                                        PrintWriter out, String supplierCode, String dateFrom, String dateTo, String dnm, int[] count, String[] invoicesArg,
                                        String[] typesArg, String localDefnsDir, String defnsDir, boolean[] firstUnattachedCN, int[] bytesOut)
                                        throws Exception
  {
    stmt = con.createStatement();
    rs = stmt.executeQuery("SELECT InvoiceCode, Date, Currency, TotalTotal, SupplierInvoiceCode FROM pinvoice "
                         + "WHERE CompanyCode = '" + supplierCode + "' AND Status != 'C' AND Settled != 'Y' AND Date >= {d ' "
                         + dateFrom + "'} AND Date <= {d '" + dateTo + "'}"); 

    String invoiceCode, date, currency, totalTotal, supplierInvoiceCode;
    double outstanding;
    
    while(rs.next())
    {
      invoiceCode         = rs.getString(1);
      date                = rs.getString(2);
      currency            = rs.getString(3);
      totalTotal          = rs.getString(4);
      supplierInvoiceCode = rs.getString(5);

      outstanding = calculateOutstanding(con, stmt2, stmt3, rs2, rs3, invoiceCode, generalUtils.doubleFromStr(totalTotal), dnm, localDefnsDir, defnsDir);

      if(outstanding > 0)
      {
        writeLine(out, 'I', date, invoiceCode, supplierInvoiceCode, totalTotal, currency, count[0], outstanding, invoicesArg, typesArg, bytesOut);

        salesCreditNoteLines(con, stmt2, stmt3, rs2, rs3, out, invoiceCode, dnm, localDefnsDir, defnsDir, firstUnattachedCN, bytesOut);

        writePaymentLines(con, stmt2, rs2, out, invoiceCode, bytesOut);

        ++count[0];
      }
    }  

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void writePaymentLines(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String invoiceCode, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.PaymentCode, t2.AmountPaid, t1.Currency, t1.DatePaid "
                         + "FROM paymentl AS t2 INNER JOIN payment AS t1 ON t1.PaymentCode = t2.PaymentCode WHERE t2.InvoiceCode = '" + invoiceCode
                         + "' AND t1.Status != 'C'");

    String amountPaid, code, currency, datePaid;

    while(rs.next())
    {    
      code       = rs.getString(1);
      amountPaid = rs.getString(2);
      currency   = rs.getString(3);
      datePaid   = rs.getString(4);
      
      writePaymentLine(out, code, datePaid, amountPaid, currency, bytesOut);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void writePaymentLine(PrintWriter out, String paymentCode, String paymentDate, String amt, String currency, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<tr><td nowrap><span id=\"textReceipt\">Payment</span></td>");
    scoutln(out, bytesOut, "<td><span id=\"textReceipt\">" + generalUtils.convertFromYYYYMMDD(paymentDate) + "</span></td>");
    scoutln(out, bytesOut, "<td><span id=\"textReceipt\">");
    scoutln(out, bytesOut, "<a href=\"javascript:viewPayment('" + paymentCode + "')\">" + paymentCode + "</a></td>");
    scoutln(out, bytesOut, "<td align=right><span id=\"textReceipt\">" + generalUtils.doubleDPs(amt, '2') + "</span></td>");
    scoutln(out, bytesOut, "<td></td>");
    scoutln(out, bytesOut, "<td align=center nowrap><span id=\"textReceipt\">" + currency + "</span></td></tr>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void writeLine(PrintWriter out, char type, String date, String invoiceCode, String supplierInvoiceCode, String amt, String currency,
                         int count, double outstanding, String[] invoicesArg, String[] typesArg, int[] bytesOut) throws Exception
  {
    scout(out, bytesOut, "<tr><td><p>");
    if(type == 'I')
      scout(out, bytesOut, "Invoice</td>");
    else scout(out, bytesOut, "Debit Note</td>");

    scout(out, bytesOut, "<td><p>" + generalUtils.convertFromYYYYMMDD(date) + "</td>");
    scout(out, bytesOut, "<td><p>");

    if(type == 'I')
      scoutln(out, bytesOut, "<a href=\"javascript:viewInvoice('" + invoiceCode + "')\">" + invoiceCode + "</a></td>");
    else scoutln(out, bytesOut, "<a href=\"javascript:viewDN('" + invoiceCode + "')\">" + invoiceCode + "</a></td>");

    scoutln(out, bytesOut, "<td nowrap><p>" + supplierInvoiceCode + "</td>");

    scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.doubleDPs(amt, '2') + "</td>");
    scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.doubleDPs('2', outstanding) + "</td>");
    scoutln(out, bytesOut, "<td align=center nowrap><p>" + currency + "</td>");

    scoutln(out, bytesOut, "<td nowrap><p><input type=text name=a" + count + " value=\"0\" size=10 maxlength=20  onkeyup=\"javascript:runningTotal()\"></td>");

    scoutln(out, bytesOut, "<td nowrap><p><a href=\"javascript:useInvoice(" + count + "," + generalUtils.doubleDPs(outstanding,'2') + ")\"><p>Use</a></td></tr>");//////////

    invoicesArg[0] += (invoiceCode + "\001");
    typesArg[0]    += (type + "\001");
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void detInvoicesCashOnly(Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, ResultSet rs3,
                                   PrintWriter out, String supplierCode, String dateFrom, String dateTo, String dnm, int[] count,
                                   String[] invoicesArg, String[] typesArg, String localDefnsDir, String defnsDir,
                                   boolean[] firstUnattachedCN, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();
    rs = stmt.executeQuery("SELECT InvoiceCode, Date, Currency, TotalTotal, SupplierInvoiceCode  FROM pinvoice "
                         + "WHERE CompanyCode = '" + supplierCode + "' AND Status != 'C' AND CashOrAccount = 'C' "
                         + "AND Settled != 'Y' AND Date >= {d ' " + dateFrom + "'} AND Date <= {d '" + dateTo + "'}"); 

    String invoiceCode, date, currency, totalTotal, supplierInvoiceCode;
    double outstanding;
    
    while(rs.next())
    {
      invoiceCode         = rs.getString(1);
      date                = rs.getString(2);
      currency            = rs.getString(3);
      totalTotal          = rs.getString(4);
      supplierInvoiceCode = rs.getString(5);

      outstanding = calculateOutstanding(con, stmt2, stmt3, rs2, rs3, invoiceCode, generalUtils.doubleFromStr(totalTotal), dnm, localDefnsDir, defnsDir);

      if(outstanding > 0)
      {
        writeLine(out, 'I', date, invoiceCode, supplierInvoiceCode, totalTotal, currency, count[0], outstanding, invoicesArg, typesArg, bytesOut);

        salesCreditNoteLines(con, stmt2, stmt3, rs2, rs3, out, invoiceCode, dnm, localDefnsDir, defnsDir, firstUnattachedCN, bytesOut);

        writePaymentLines(con, stmt2, rs2, out, invoiceCode, bytesOut);

        ++count[0];
      }
    }  

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------
  private void salesCreditNoteLines(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, String invoiceCode, String dnm,
                               String localDefnsDir, String defnsDir, boolean[] firstUnattachedCN, int[] bytesOut)
                               throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.PCNCode, t2.Amount2, t2.GSTRate, t1.Currency, t1.Date "
                         + "FROM pcreditl AS t2 INNER JOIN pcredit AS t1 ON t1.PCNCode = t2.PCNCode WHERE t2.InvoiceCode = '"
                         + invoiceCode + "' AND t1.Status != 'C'");

    String amount, gstRate, code, currency, date;
    double amtF, gstF;

    while(rs.next())
    {    
      code     = rs.getString(1);
      amount   = rs.getString(2);
      gstRate  = rs.getString(3);
      currency = rs.getString(4);
      date     = rs.getString(5);
      
      if(gstRate.length() > 0)
      {
        amtF = generalUtils.doubleFromStr(amount);
        gstF = amtF * accountsUtils.getGSTRate(con, stmt2, rs2, gstRate);
        amtF += gstF;
        amount = generalUtils.doubleToStr(amtF);
      }

      writeCNLine(out, ' ', date, code, amount, currency, firstUnattachedCN, bytesOut);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void writeCNLine(PrintWriter out, char type, String date, String salesCreditNoteCode, String amt, String currency,
                           boolean[] firstUnattachedCN, int[] bytesOut) throws Exception
  {
    if(type == 'U' && firstUnattachedCN[0])
    {
      scoutln(out, bytesOut, "<tr><td colspan=7><hr></td></tr>");
      firstUnattachedCN[0] = false;
    }
    
    scoutln(out, bytesOut, "<tr><td nowrap><span id=\"textCreditNote\">Credit Note</span></td>");
    scoutln(out, bytesOut, "<td><span id=\"textCreditNote\">" + generalUtils.convertFromYYYYMMDD(date) + "</span></td>");
    scoutln(out, bytesOut, "<td><span id=\"textCreditNote\">");
    scoutln(out, bytesOut, "<a href=\"javascript:viewPCN('" + salesCreditNoteCode + "')\">" + salesCreditNoteCode + "</a></span></td>");
    scoutln(out, bytesOut, "<td align=right><span id=\"textCreditNote\">" + generalUtils.doubleDPs(amt, '2') + "</span></td>");
    scoutln(out, bytesOut, "<td></td>");
    scoutln(out, bytesOut, "<td align=center nowrap><span id=\"textCreditNote\">" + currency + "</span></td></tr>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void detDebitNotesGivenSupplier(Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2,
                                          ResultSet rs3, PrintWriter out, String supplierCode, String dateFrom, String dateTo, String dnm,
                                          int[] count, String[] invoicesArg, String[] typesArg, String localDefnsDir,
                                          String defnsDir, boolean[] firstUnattachedCN, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();
    rs = stmt.executeQuery("SELECT PDNCode, Date, Currency, TotalTotal FROM pdebit "
                         + "WHERE CompanyCode = '" + supplierCode + "' AND Status != 'C' AND Settled != 'Y' AND Date >= {d ' "
                         + dateFrom + "'} AND Date <= {d '" + dateTo + "'}"); 

    String dnCode, date, currency, totalTotal;
    double outstanding;
    
    while(rs.next())
    {
      dnCode     = rs.getString(1);
      date       = rs.getString(2);
      currency   = rs.getString(3);
      totalTotal = rs.getString(4);

      outstanding = calculateOutstanding(con, stmt2, stmt3, rs2, rs3, dnCode, generalUtils.doubleFromStr(totalTotal), dnm, localDefnsDir, defnsDir);

      if(outstanding > 0)
      {
        writeLine(out, 'D', date, dnCode, "", totalTotal, currency, count[0], outstanding, invoicesArg, typesArg, bytesOut);

        salesCreditNoteLines(con, stmt2, stmt3, rs2, rs3, out, dnCode, dnm, localDefnsDir, defnsDir, firstUnattachedCN, bytesOut);

        writePaymentLines(con, stmt2, rs2, out, dnCode, bytesOut);

        ++count[0];
      }
    }  

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void detDebitNotesCashOnly(Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, ResultSet rs3, PrintWriter out,
                                     String supplierCode, String dateFrom, String dateTo, String dnm, int[] count,
                                     String[] invoicesArg, String[] typesArg, String localDefnsDir, String defnsDir,
                                     boolean[] firstUnattachedCN, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();
    rs = stmt.executeQuery("SELECT PDNCode, Date, Currency, TotalTotal FROM pdebit "
                         + "WHERE CompanyCode = '" + supplierCode + "' AND Status != 'C' AND CashOrAccount = 'C' "
                         + "AND Settled != 'Y' AND Date >= {d ' " + dateFrom + "'} AND Date <= {d '" + dateTo + "'}"); 

    String dnCode, date, currency, totalTotal;
    double outstanding;
    
    while(rs.next())
    {
      dnCode     = rs.getString(1);
      date       = rs.getString(2);
      currency   = rs.getString(3);
      totalTotal = rs.getString(4);

      outstanding = calculateOutstanding(con, stmt2, stmt3, rs2, rs3, dnCode, generalUtils.doubleFromStr(totalTotal), dnm, localDefnsDir, defnsDir);

      if(outstanding > 0)
      {
        writeLine(out, 'D', date, dnCode, "", totalTotal, currency, count[0], outstanding, invoicesArg, typesArg, bytesOut);

        salesCreditNoteLines(con, stmt2, stmt3, rs2, rs3, out, dnCode, dnm, localDefnsDir, defnsDir, firstUnattachedCN, bytesOut);

        writePaymentLines(con, stmt2, rs2, out, dnCode, bytesOut);

        ++count[0];
      }
    }  

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void unattachedCreditNoteLines(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, String companyCode, String dnm,
                                         boolean[] firstUnattachedCN, String localDefnsDir, String defnsDir, int[] bytesOut)
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
      return;
    
    stmt = con.createStatement();

    // chk CNs for entries without valid invoice codes (blank entries or non-existant invoices)
    rs = stmt.executeQuery("SELECT t2.PCNCode, t2.Amount, t2.Amount2, t2.GSTRate, t1.Date, t1.Currency "
                                   + "FROM pcredit AS t1 INNER JOIN pcreditl AS t2 ON t1.PCNCode = t2.PCNCode WHERE t1.CompanyCode = '"
                                   + companyCode + "' AND t1.Status != 'C' AND t2.InvoiceCode NOT IN (SELECT InvoiceCode "
                                   + "FROM pinvoice) ORDER BY t1.Date, t2.PCNCode");

    String cnCode, amount, baseAmount, gstRate, dateIssued, currency;
    double thisBaseAmt, thisNonBaseAmt, gst;

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

      writeCNLine(out, 'U', dateIssued, cnCode, amount, currency, firstUnattachedCN, bytesOut);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private double calculateOutstanding(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String invoiceCode,
                                      double invoiceAmount, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    invoiceAmount -= calcOutstandingCreditNoteLines(con, stmt, stmt2, rs, rs2, invoiceCode, dnm, localDefnsDir, defnsDir);

    invoiceAmount -= calcOutstandingPaymentLines(con, stmt, rs, invoiceCode);

    if(invoiceAmount >= -0.00999999999999 && invoiceAmount <= 0.00999999999999)
      invoiceAmount = 0.0;

    return invoiceAmount;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private double calcOutstandingCreditNoteLines(Connection con, Statement stmt, Statement stmt2, ResultSet rs2, ResultSet rs, String invoiceCode,
                                                String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.Amount2, t2.GSTRate "
                         + "FROM pcreditl AS t2 INNER JOIN pcredit AS t1 ON t1.PCNCode = t2.PCNCode WHERE t2.InvoiceCode = '" + invoiceCode
                         + "' AND t1.Status != 'C'");

    String amount, gstRate;
    double amtF, gstF, totalAmount = 0.0;

    while(rs.next())
    {    
      amount     = rs.getString(1);
      gstRate    = rs.getString(2);
      
      amtF = generalUtils.doubleDPs(generalUtils.doubleFromStr(amount), '2');

      if(gstRate.length() > 0)
      {
        gstF = generalUtils.doubleDPs((amtF * accountsUtils.getGSTRate(con, stmt2, rs2, gstRate)), '2');
        amtF += gstF;
      }
      
      totalAmount += generalUtils.doubleDPs(amtF, '2');
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    return totalAmount;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private double calcOutstandingPaymentLines(Connection con, Statement stmt, ResultSet rs, String invoiceCode) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.AmountPaid FROM paymentl AS t2 INNER JOIN payment AS t1 "
                         + "ON t1.PaymentCode = t2.PaymentCode WHERE t2.InvoiceCode = '" + invoiceCode + "' AND t1.Status != 'C'");

    double totalAmount = 0.0;

    while(rs.next())
    {    
      totalAmount += generalUtils.doubleDPs(generalUtils.doubleFromStr(rs.getString(1)), '2');
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    return totalAmount;
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void writePrePaymentLines(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String companyCode, String year, String dnm,
                                    String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    String prePaymentAccount = accountsUtils.getPrePaymentToSupplierAccCode(year, dnm, localDefnsDir, defnsDir);    
    
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.VoucherCode, t2.Amount2, t2.Description, t1.Currency, t1.Date "
                         + "FROM voucherl AS t2 INNER JOIN voucher AS t1 ON t1.VoucherCode = t2.VoucherCode WHERE t1.CompanyCode = '" + companyCode
                         + "' AND t2.AccountDr = '" + prePaymentAccount + "' AND t1.Status != 'C' AND t1.ClearedPrePayment != 'Y'");

    String amount, code, currency, date, desc;

    while(rs.next())
    {    
      code     = rs.getString(1);
      amount   = rs.getString(2);
      desc     = rs.getString(3);
      currency = rs.getString(4);
      date     = rs.getString(5);
      
      writePrePaymentLine(out, code, date, amount, currency, desc, bytesOut);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void writePrePaymentLine(PrintWriter out, String voucherCode, String date, String amt, String currency, String desc, int[] bytesOut)
                                   throws Exception
  {
    scoutln(out, bytesOut, "<tr><td nowrap><p><b>PrePayment:</td>");
    scoutln(out, bytesOut, "<td><p>" + generalUtils.convertFromYYYYMMDD(date) + "</td>");
    scoutln(out, bytesOut, "<td><p><a href=\"javascript:viewVoucher('" + voucherCode + "')\">" + voucherCode + "</a></td>");
    scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.doubleDPs(amt, '2') + "</td>");
    scoutln(out, bytesOut, "<td align=center nowrap><p>" + currency + "</td>");
    scoutln(out, bytesOut, "<td nowrap><p>" + desc + "</td></tr>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void scout(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.print(str);
    bytesOut[0] += (str.length());    
  }
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
