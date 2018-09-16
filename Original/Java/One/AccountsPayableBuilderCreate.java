// =======================================================================================================================================================================================================
// System: ZaraStar Document: AP builder create AP rec
// Module: AccountsPayableBuilderCreate.java
// Author: C.K.Harvey
// Copyright (c) 2000-07 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.io.*;
import java.sql.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class AccountsPayableBuilderCreate extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  Supplier supplier = new Supplier();
  PurchaseInvoice purchaseInvoice = new PurchaseInvoice();
  PurchaseDebitNote purchaseDebitNote = new PurchaseDebitNote();
  AccountsUtils accountsUtils = new AccountsUtils();
  Payment payment = new Payment();
  DocumentUtils documentUtils = new DocumentUtils();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", companyCode="", paymentDate="", datePaid="", chequeNumber="", 
           totalAmount="", discountAllowed="", adj="", charges="", rate="", paymentReference="", bankAmount="", note="", cashOrNot="", crAccount="",
           invoices="", types="";
    
    try
    {
      directoryUtils.setContentHeaders(res);
      out = res.getWriter();

      String[] amounts = new String[100];  int amountsLen = 100;
      
      int x, len, thisEntry, numLines=0;

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
        if(name.equals("companyCode"))
          companyCode = value[0];
        else
        if(name.equals("apDate"))
          paymentDate = value[0];
        else
        if(name.equals("datePaid"))
          datePaid = value[0];
        else
        if(name.equals("chequeNumber"))
          chequeNumber = value[0];
        else
        if(name.equals("totalAmount"))
          totalAmount = value[0];
        else
        if(name.equals("discountAllowed"))
          discountAllowed = value[0];
        else
        if(name.equals("adj"))
          adj = value[0];
        else
        if(name.equals("charges"))
          charges = value[0];
        else
        if(name.equals("rate"))
          rate = value[0];
        else
        if(name.equals("paymentReference"))
          paymentReference = value[0];
        else
        if(name.equals("bankAmount"))
          bankAmount = value[0];
        else
        if(name.equals("note"))
          note = value[0];
        else
        if(name.equals("crAccount"))
          crAccount = value[0];
        else
        if(name.equals("cashOrNot"))
          cashOrNot = value[0];
        else
        if(name.equals("invoices"))
          invoices = value[0];
        else
        if(name.equals("types"))
          types = value[0];
        else
        { 
          if(name.startsWith("a"))
          {
            thisEntry = generalUtils.strToInt(name.substring(1));
            if(thisEntry >= amountsLen) // outside of the current size
            {
              String[] tmp = new String[amountsLen];
              for(x=0;x<amountsLen;++x)
                tmp[x] = amounts[x];
              len = amountsLen;
              amountsLen = thisEntry + 1;
              amounts = new String[amountsLen];
              for(x=0;x<len;++x)
                amounts[x] = tmp[x];
            }
            
            ++numLines;
            amounts[thisEntry] = value[0]; 
          }
        }
      }  

      if(paymentDate == null)      paymentDate = "";
      if(rate == null)             rate = "";
      if(datePaid == null)         datePaid = "";
      if(companyCode == null)      companyCode = "";
      if(chequeNumber == null)     chequeNumber = "";
      if(totalAmount == null)      totalAmount = "0";
      if(discountAllowed == null)  discountAllowed = "0";
      if(adj == null)              adj = "";
      if(charges == null)          charges = "0";
      if(paymentReference == null) paymentReference = "";
      if(bankAmount == null)       bankAmount = "0";
      if(note == null)             note = "";
      if(cashOrNot == null)        cashOrNot = " ";
      if(crAccount == null)        crAccount = "";
      
      doIt(out, req, numLines, companyCode, paymentDate, datePaid, chequeNumber, totalAmount, discountAllowed, adj, charges, rate,
           paymentReference, bankAmount, note, crAccount, cashOrNot, invoices, types, amounts, unm, sid, uty, men, den, bnm, dnm, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "AccountsPayableBuilderCreate", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 5047, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, int numLines, String companyCode, String paymentDate, String datePaid, 
                    String chequeNumber, String totalAmount, String discountAllowed, String adj, String charges, String rate, String paymentReference,
                    String bankAmount, String note, String crAccount, String cashOrNot, String invoices, String types, String[] amounts, String unm,
                    String sid, String uty, String men, String den, String bnm, String dnm, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String localDefnsDir  = directoryUtils.getLocalOverrideDir(dnm);
    String defnsDir          = directoryUtils.getSupportDirs('D');
    String imagesDir         = directoryUtils.getSupportDirs('I');

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;
    
    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 5047, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "AccountsPayableBuilderPage", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 5047, bytesOut[0], 0, "ACC:");
      if(con  != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "AccountsPayableBuilderPage", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 5047, bytesOut[0], 0, "SID:");
      if(con  != null) con.close();
      if(out != null) out.flush();
      return;
    }
        
    byte[] nextCode = new byte[21];  nextCode[0] = '\000';

    if(paymentDate.length() == 0)     paymentDate = generalUtils.today(localDefnsDir, defnsDir);
    if(rate.length() == 0)            rate = "0"; // correct
    if(datePaid.length() == 0)        datePaid = generalUtils.today(localDefnsDir, defnsDir);
    if(totalAmount.length() == 0)     totalAmount = "0";
    if(discountAllowed.length() == 0) discountAllowed = "0";
    if(charges.length() == 0)         charges = "0";
    if(bankAmount.length() == 0)      bankAmount = "0";

    switch(process(con, stmt, rs, out, numLines, companyCode, paymentDate, datePaid, chequeNumber, totalAmount, discountAllowed, adj, charges, rate,
                   paymentReference, bankAmount, note, crAccount, cashOrNot, invoices, types, amounts, unm, sid, uty, men, den, bnm, dnm,
                   nextCode, localDefnsDir, defnsDir, bytesOut))
    {
      case ' ' : getRec(out, unm, sid, uty, men, den, dnm, bnm, generalUtils.stringFromBytes(nextCode, 0L), localDefnsDir);
                 break;
      case 'E' : errorMsg(out, checkTotal(numLines, amounts), generalUtils.doubleDPs(generalUtils.doubleFromStr(totalAmount), '2'), bytesOut);
                 break;      
      case 'A' : messagePage.msgScreen(false, out, req, 33, unm, sid, uty, men, den, dnm, bnm, "AccountsPayableBuilderPage", imagesDir, localDefnsDir, defnsDir,
                                    bytesOut);
                 break;
      default  : messagePage.msgScreen(false, out, req, 23, unm, sid, uty, men, den, dnm, bnm, "AccountsPayableBuilderPage", imagesDir, localDefnsDir, defnsDir,
                                    bytesOut);
                 break;
    }  

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 5047, bytesOut[0], 0, (new java.util.Date().getTime() - startTime),
                     generalUtils.stringFromBytes(nextCode, 0L));
    if(con  != null) con.close();
    if(out != null) out.flush();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private char process(Connection con, Statement stmt, ResultSet rs, PrintWriter out, int numLines, String companyCode, String paymentDate,
                       String datePaid, String chequeNumber, String totalAmount, String discountAllowed, String discountFlag, String charges,
                       String rate, String paymentReference, String bankAmount, String note, String crAccount, String cashOrNot, String invoices,
                       String types, String[] amounts, String unm, String sid, String uty, String men, String den, String bnm, String dnm,
                       byte[] nextCode, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    // check that at least one line has value
    if(! analyzeLines(amounts, numLines, localDefnsDir, defnsDir)) 
      return 'A';
      
    // check that the total amt stated is indeed the total of all
    if(checkTotal(numLines, amounts) != generalUtils.doubleDPs(generalUtils.doubleFromStr(totalAmount), '2'))
      return 'E';

    String[] currency = new String[1];
    
    if(createPaymentHeader(con, stmt, rs, companyCode, invoices, types, rate, paymentDate, datePaid, totalAmount, cashOrNot, discountAllowed, charges, crAccount,
                           paymentReference, note, chequeNumber, bankAmount, discountFlag, dnm, unm, localDefnsDir, defnsDir, nextCode, currency))
    {
      byte[] invoice  = new byte[21];
      byte[] type     = new byte[10];
      byte[] thisLine = new byte[10];
      String originalRate, invoiceStr, typeStr;
      int count=0, lineCount=1;
      
      while(count < numLines)
      {
        if(generalUtils.doubleFromStr(amounts[count]) != 0.0)
        {
          generalUtils.dfsGivenSeparator(false, '\001', invoices, (short)count, invoice);
          generalUtils.dfsGivenSeparator(false, '\001', types, (short)count, type);
          
          invoiceStr = generalUtils.stringFromBytes(invoice, 0L);
          typeStr    = generalUtils.stringFromBytes(type, 0L);

          generalUtils.intToBytesCharFormat(lineCount, thisLine, (short)0);
          if(typeStr.equals("I"))
            originalRate = purchaseInvoice.getAnInvoiceFieldGivenCode(con, stmt, rs, "Rate", invoiceStr, dnm, localDefnsDir, defnsDir); 
          else originalRate = purchaseDebitNote.getADebitNoteFieldGivenCode(con, stmt, rs, "Rate", invoiceStr, dnm, localDefnsDir, defnsDir); 
          
          createPaymentLines(con, stmt, rs, nextCode, invoiceStr, typeStr, thisLine, amounts[count], rate, originalRate, unm, currency[0], datePaid, dnm, localDefnsDir, defnsDir, bytesOut);

          ++lineCount;
        }
        
        ++count;
      }
    }
    else // failed to create header
    {
      return 'X';
    }
    
    return ' ';
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean analyzeLines(String[] amounts, int numLines, String localDefnsDir, String defnsDir) throws Exception
  {
    boolean atLeastOneHasQty = false;

    int count=0;

    while(! atLeastOneHasQty && count < numLines)
    {
      if(generalUtils.doubleFromStr(amounts[count]) != 0.0)
        atLeastOneHasQty = true;
      
      ++count;
    }

    return atLeastOneHasQty;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private double checkTotal(int numLines, String[] amounts) throws Exception
  {
    int count = 0;
    double f, allAmounts = 0;

    while(count < numLines)
    {
      f = generalUtils.doubleFromStr(amounts[count]);
      if(f != 0.0)
        allAmounts += f;
      
      ++count;
    }

    return generalUtils.doubleDPs(allAmounts, '2');
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void errorMsg(PrintWriter out, double individualAmounts, double totalAmount, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<table border=0 cellspacing=2 cellpadding=0 width=100%>");

    scoutln(out, bytesOut, "<tr><td colspan=2 nowrap><p><b>AP Entry Error</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap><b>Unable to Update</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td><p>The <i>Total Amount</i> specified is &nbsp;");
    scoutln(out, bytesOut, generalUtils.doubleToStr(totalAmount) + " &nbsp; but the individual amounts add up to &nbsp;&nbsp;");
    scoutln(out, bytesOut, generalUtils.doubleToStr(individualAmounts) + "</td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td><p>That's a difference of &nbsp;");
    scoutln(out, bytesOut, generalUtils.doubleToStr(totalAmount - individualAmounts) + "</td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void getRec(PrintWriter out, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String code,
                      String localDefnsDir) throws Exception
  {
    URL url = new URL("http://" + serverUtils.serverToCall("OFSA", localDefnsDir) + "/central/servlet/PaymentPage");

    HttpURLConnection uc = (HttpURLConnection)url.openConnection();
    uc.setDoInput(true);
    uc.setDoOutput(true);
    uc.setUseCaches(false);
    uc.setDefaultUseCaches(false);
    uc.setRequestProperty("Content-Type", "application/x-www-form.urlencoded");

    String s2 = "&unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1="
              + generalUtils.sanitise(code) + "&p2=A&p3=&p4=";

    Integer len = new Integer(s2.length());
    uc.setRequestProperty("Content-Length", len.toString());
    
    uc.setRequestMethod("POST");

    PrintWriter p = new PrintWriter(uc.getOutputStream());
    p.print(s2);    
    p.flush();
    p.close();

    BufferedReader di = new BufferedReader(new InputStreamReader(uc.getInputStream()));
    String s = di.readLine();
    while(s != null)
    {
      out.println(s);
      s = di.readLine();
    }

    di.close();
  }
 
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean createPaymentHeader(Connection con, Statement stmt, ResultSet rs, String companyCode, String invoices, String types, String rate, String paymentDate, String datePaid, String totalAmount, String cashOrNot,
                                      String discountAllowed, String charges, String accCr, String paymentReference, String note, String chequeNumber,
                                      String bankAmount, String discountFlag, String dnm, String unm, String localDefnsDir, String defnsDir,
                                      byte[] paymentCode, String[] currency) throws Exception
  {   
    byte[] b       = new byte[100];
    byte[] data    = new byte[3000];
    byte[] recData = new byte[3000];

    documentUtils.getNextCode(con, stmt, rs, "payment", true, paymentCode);

    generalUtils.zeroize(recData, 3000);

    generalUtils.repAlpha(recData, 3000, (short)0, paymentCode);
    generalUtils.repAlpha(recData, 3000, (short)1, generalUtils.convertDateToSQLFormat(paymentDate));

    generalUtils.repAlpha(recData, 3000, (short)2, companyCode);

    currency[0]="";
    if(supplier.getSupplierRecGivenCode(con, stmt, rs, companyCode, '\000', dnm, data, localDefnsDir, defnsDir) != -1) // exists
    {
      generalUtils.dfs(data, (short)1, b);           // name
      generalUtils.repAlpha(recData, 3000, (short)3, b);
      generalUtils.dfs(data, (short)2, b);           // addr1
      generalUtils.repAlpha(recData, 3000, (short)4, b);
      generalUtils.dfs(data, (short)3, b);           // addr2
      generalUtils.repAlpha(recData, 3000, (short)5, b);
      generalUtils.dfs(data, (short)4, b);           // addr3
      generalUtils.repAlpha(recData, 3000, (short)6, b);
      generalUtils.dfs(data, (short)5, b);           // addr4
      generalUtils.repAlpha(recData, 3000, (short)7, b);
      generalUtils.dfs(data, (short)6, b);           // addr5
      generalUtils.repAlpha(recData, 3000, (short)8, b);
      generalUtils.dfs(data, (short)7, b);          // pc
      generalUtils.repAlpha(recData, 3000, (short)9, b);

      currency[0] = generalUtils.dfsAsStr(data, (short)30);
    }
    else // sundry
    {
       // get info from first invoice

       byte[] invoice  = new byte[21];
       byte[] type     = new byte[10];

       generalUtils.dfsGivenSeparator(false, '\001', invoices, (short)0, invoice);
       generalUtils.dfsGivenSeparator(false, '\001', types, (short)0, type);

       String invoiceStr = generalUtils.stringFromBytes(invoice, 0L);
       String typeStr    = generalUtils.stringFromBytes(type, 0L);

       String companyName;
       if(typeStr.equals("I"))
       {
         companyName = purchaseInvoice.getAnInvoiceFieldGivenCode(con, stmt, rs, "CompanyName", invoiceStr, dnm, localDefnsDir, defnsDir);
         currency[0] = purchaseInvoice.getAnInvoiceFieldGivenCode(con, stmt, rs, "Currency", invoiceStr, dnm, localDefnsDir, defnsDir);
       }
       else
       {
         companyName = purchaseDebitNote.getADebitNoteFieldGivenCode(con, stmt, rs, "CompanyName", invoiceStr, dnm, localDefnsDir, defnsDir);
         currency[0] = purchaseDebitNote.getADebitNoteFieldGivenCode(con, stmt, rs, "Currency", invoiceStr, dnm, localDefnsDir, defnsDir);
       }

       generalUtils.repAlpha(recData, 3000, (short)3, companyName.getBytes());
    }

    generalUtils.repAlpha(recData, 3000, (short)10, ""); // fao
    generalUtils.repAlpha(recData, 3000, (short)11, note);
    generalUtils.repAlpha(recData, 3000, (short)12, ""); // attn
    generalUtils.repAlpha(recData, 3000, (short)13, ""); // misc1
    generalUtils.repAlpha(recData, 3000, (short)14, ""); // misc2

    generalUtils.repAlpha(recData, 3000, (short)15, totalAmount); // nonBaseAmount

    generalUtils.repAlpha(recData, 3000, (short)16, generalUtils.convertDateToSQLFormat(datePaid));

    generalUtils.repAlpha(recData, 3000, (short)17, accCr);

    generalUtils.repAlpha(recData, 3000, (short)18, companyCode);//accDr);

    generalUtils.repAlpha(recData, 3000, (short)19, ""); // unused1

    // dlm
    
    generalUtils.repAlpha(recData, 3000, (short)21, unm);
    generalUtils.repAlpha(recData, 3000, (short)22, "");    // unused2
    generalUtils.repAlpha(recData, 3000, (short)23, "");    // projectCode
    generalUtils.repAlpha(recData, 3000, (short)24, "0.0"); // gstComponent
    
    generalUtils.repAlpha(recData, 3000, (short)25, chequeNumber);
    generalUtils.repAlpha(recData, 3000, (short)26, "0"); // printCount

    generalUtils.repAlpha(recData, 3000, (short)27, "");    // apcode

    generalUtils.repAlpha(recData, 3000, (short)28, currency[0]);

    double rateD;
    if(rate.length() == 0 || generalUtils.doubleFromStr(rate) == 0.0) // no rate specified
      rateD = accountsUtils.getApplicableRate(con, stmt, rs, currency[0], datePaid, b, dnm, localDefnsDir, defnsDir);
    else rateD = generalUtils.doubleFromStr(rate);

    double baseAmount = generalUtils.doubleFromStr(totalAmount);

    baseAmount *= rateD;

    String baseAmountStr = generalUtils.doubleToStr(baseAmount);

    generalUtils.repAlpha(recData, 3000, (short)29, generalUtils.doubleToStr(rateD));

    generalUtils.repAlpha(recData, 3000, (short)30, baseAmountStr);
    generalUtils.repAlpha(recData, 3000, (short)31, "L");   // status
    generalUtils.repAlpha(recData, 3000, (short)32, "0.0"); // baseGSTComponent

    generalUtils.repAlpha(recData, 3000, (short)33, cashOrNot);

    generalUtils.repAlpha(recData, 3000, (short)34, paymentReference);
    generalUtils.repAlpha(recData, 3000, (short)35, "N");   // reconciled
    generalUtils.repAlpha(recData, 3000, (short)36, ""); //bankAccount); // no longer used

    generalUtils.repAlpha(recData, 3000, (short)37, charges);

    double d = generalUtils.doubleFromStr(discountAllowed);
    if(discountFlag.equals("O")) // overpaid
    {
      d *= -1; // store as negative
      discountAllowed = generalUtils.doubleToStr(d);
    }
    generalUtils.repAlpha(recData, 3000, (short)38, discountAllowed);

    generalUtils.repAlpha(recData, 3000, (short)39, "N"); // processed
    generalUtils.repAlpha(recData, 3000, (short)40, "1970-01-01"); // dateProcessed

    generalUtils.repAlpha(recData, 3000, (short)41, bankAmount);

    generalUtils.repAlpha(recData, 3000, (short)42, "0.0"); // rateBaseToBank no longer used

    generalUtils.repAlpha(recData, 3000, (short)43, "0.0"); // exchangeAdjustment no longer used

    if(payment.putRecHeadGivenCode(con, stmt, rs, paymentCode, 'N', recData, dnm, localDefnsDir, defnsDir) != ' ')
      return false;
    
    return true;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void createPaymentLines(Connection con, Statement stmt, ResultSet rs, byte[] paymentCode, String documentCode, String type, byte[] thisLine, String nonBaseAmountPaid, String rate,
                                  String originalRate, String signOn, String currency, String datePaid, String dnm, String localDefnsDir, String defnsDir, int[] bytesOut)
                                  throws Exception
  {
    byte[] b       = new byte[100];
    byte[] data    = new byte[3000];
    byte[] recData = new byte[3000];
    double d;    

    generalUtils.zeroize(recData, 3000);

    generalUtils.repAlpha(recData, 3000, (short)0, paymentCode);
    generalUtils.repAlpha(recData, 3000, (short)1, documentCode); // invoiceCode

    generalUtils.strToBytes(b, documentCode);
    if(type.equals("I"))
    {
      if(purchaseInvoice.getRecGivenCode(con, stmt, rs, b, '\000', data, dnm, localDefnsDir, defnsDir, bytesOut) != -1)
      {
        generalUtils.dfs(data, (short)1, b);           // invoicedate
        generalUtils.repAlpha(recData, 3000, (short)2, b);

        generalUtils.dfs(data, (short)18, b);           // totaltotal
        generalUtils.repAlpha(recData, 3000, (short)3, b);
      }
    }
    else // not an invoice, perh a PDN
    if(purchaseDebitNote.getRecGivenCode(con, stmt, rs, b, '\000', data, dnm, localDefnsDir, defnsDir, bytesOut) != -1)
    {
      generalUtils.dfs(data, (short)1, b);           // invoicedate
      generalUtils.repAlpha(recData, 3000, (short)2, b);

      generalUtils.dfs(data, (short)18, b);           // totaltotal
      generalUtils.repAlpha(recData, 3000, (short)3, b);
    }
    else // just-in-case
    {
      generalUtils.repAlpha(recData, 3000, (short)2, "1970-01-01");
      generalUtils.repAlpha(recData, 3000, (short)3, "0.0");
    }

    generalUtils.repAlpha(recData, 3000, (short)4, ""); // desc
    generalUtils.repAlpha(recData, 3000, (short)5, nonBaseAmountPaid);
    //dlm
    generalUtils.repAlpha(recData, 3000, (short)7, signOn);
    generalUtils.repAlpha(recData, 3000, (short)8, ""); // unused1
    generalUtils.repAlpha(recData, 3000, (short)9, ""); // unused2

    generalUtils.repAlpha(recData, 3000, (short)10, originalRate); // originalrate
    generalUtils.repAlpha(recData, 3000, (short)11, "0.0"); // unused3

    generalUtils.repAlpha(recData, 3000, (short)12, "0.0"); // gstcomponent
    generalUtils.repAlpha(recData, 3000, (short)13, ""); // accountDr

    generalUtils.repAlpha(recData, 3000, (short)14, "0");// no longer used??? documentLine); // invoiceline

    generalUtils.repAlpha(recData, 3000, (short)15, thisLine); // line
    generalUtils.repAlpha(recData, 3000, (short)16, thisLine); // entry

    double rateD;
    if(rate.length() == 0 || generalUtils.doubleFromStr(rate) == 0.0) // no rate specified
    {
      rateD = accountsUtils.getApplicableRate(con, stmt, rs, currency, datePaid, b, dnm, localDefnsDir, defnsDir);
    }
    else rateD = generalUtils.doubleFromStr(rate);

    d = generalUtils.doubleFromStr(nonBaseAmountPaid);
    d *= rateD;
    generalUtils.repAlpha(recData, 3000, (short)17, d); // basetotalamount

    generalUtils.repAlpha(recData, 3000, (short)18, "0.0"); // baseGSTComponent

    payment.putRecLine(con, stmt, rs, paymentCode, b, 'N', recData, dnm, localDefnsDir, defnsDir);
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
