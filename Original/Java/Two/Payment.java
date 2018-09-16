// =======================================================================================================================================================================================================
// System: ZaraStar DocumentEngine: Payment Record Access
// Module: payment.java
// Author: C.K.Harvey
// Copyright (c) 2001-07 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import java.io.*;
import java.sql.*;
import java.math.BigDecimal;
import javax.servlet.http.HttpServletRequest;

public class Payment
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ScreenLayout screenLayout = new ScreenLayout();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  MiscDefinitions miscDefinitions = new MiscDefinitions();
  HtmlBuild htmlBuild = new HtmlBuild();
  AccountsUtils accountsUtils = new AccountsUtils();
  DocumentUtils documentUtils = new DocumentUtils();
  Supplier supplier = new Supplier();
  Wiki wiki = new Wiki();
  DashboardUtils dashboardUtils = new DashboardUtils();

  // -------------------------------------------------------------------------------------------------------------------------------
  public String getTableCreateStringPayment() throws Exception
  {  
    return "payment ( PaymentCode char(20) not null, Date date not null,               CompanyCode char(20) not null, "
                   + "CompanyName char(60),          Address1 char(40),                Address2 char(40), "
                   + "Address3 char(40),             Address4 char(40),                Address5 char(40), "
                   + "PostCode char(20),             FAO char(40),                     Notes char(250), "
                   + "Attention char(1),             Misc1 char(80),                   Misc2 char(80), "
                   + "TotalAmount decimal(19,8),     DatePaid date,                    AccCredited char(20), "
                   + "AccDebited char(20),           unused1 char(20),                 DateLastModified timestamp, "
                   + "SignOn char(20),               unused2 char(20),                 ProjectCode char(20), "
                   + "GSTComponent decimal(19,8),    ChequeNumber char(20),            PrintCount integer, "
                   + "APCode char(20),               Currency char(3),                 Rate decimal(16,8), "
                   + "BaseTotalAmount decimal(19,8), Status char(1),                   BaseGSTComponent decimal(19,8), "
                   + "CashOrNot char(1),             PaymentReference char(20),        Reconciled char(1), "
                   + "BankAccount char(40),          Charges decimal(19,8),            DiscountAllowed decimal(19,8), "
                   + "Processed char(1),             DateProcessed date,               BankAmount decimal(19,8), "
                   + "RateBankToBase decimal(16,8),  ExchangeAdjustment decimal(16,8), "
                   + "unique(PaymentCode))";
  } 

  // -------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStringsPayment(String[] s) throws Exception
  {
    s[0] = "paymentDateInx on payment (Date)";
    s[1] = "paymentCompanyCodeInx on payment (CompanyCode)";
    
    return 2;
  }
      
  // -------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNamesPayment() throws Exception
  {  
    return "PaymentCode, Date, CompanyCode, CompanyName, Address1, Address2, Address3, Address4, Address5, PostCode, FAO, Notes, "
         + "Attention, Misc1, Misc2, TotalAmount, DatePaid, AccCredited, AccDebited, unused1, DateLastModified, SignOn, "
         + "unused2, ProjectCode, GSTComponent, ChequeNumber, PrintCount, APCode, Currency, Rate, BaseTotalAmount, Status, "
         + "BaseGSTComponent, CashOrNot, PaymentReference, Reconciled, BankAccount, Charges, DiscountAllowed, Processed, "
         + "DateProcessed, BankAmount, RateBankToBase, ExchangeAdjustment";
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypesPayment() throws Exception
  {
    return "CDCCCCCCCCCCCCCFDCCCSCCCFCICCFFCFCCCCFFCDFFF";
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizesPayment(short[] sizes) throws Exception
  {
    sizes[0]  = 20;  sizes[1]  = 0;  sizes[2]  = 20;  sizes[3]  = 60;  sizes[4] = 40;   sizes[5] = 40;   sizes[6] = 40;
    sizes[7]  = 40;  sizes[8]  = 40; sizes[9]  = 20;  sizes[10] = 40;  sizes[11] = 250; sizes[12] = 1;   sizes[13] = 80;
    sizes[14] = 80;  sizes[15] = 0;  sizes[16] = 0;   sizes[17] = 20;  sizes[18] = 20;  sizes[19] = 20;  sizes[20] = -1;
    sizes[21] = 20;  sizes[22] = 20; sizes[23] = 20;  sizes[24] = 0;   sizes[25] = 20;  sizes[26] = 0;   sizes[27] = 20;
    sizes[28] = 3;   sizes[29] = 0;  sizes[30] = 0;   sizes[31] = 1;   sizes[32] = 0;   sizes[33] = 1;   sizes[34] = 20;
    sizes[35] = 1;   sizes[36] = 40; sizes[37] = 0;   sizes[38] = 0;   sizes[39] = 1;   sizes[40] = 0;   sizes[41] = 0;
    sizes[42] = 0;   sizes[43] = 0;
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public String getFieldStylesPayment() throws Exception
  {
    return "MMMOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO";
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public String getTableCreateStringPaymentL() throws Exception
  {  
    return "paymentl ( PaymentCode char(20) not null,  InvoiceCode char(20) not null,  InvoiceDate date, "
                    + "InvoiceAmount decimal(19,8),    Description char(80),           AmountPaid decimal(19,8), "
                    + "DateLastModified timestamp,     SignOn char(20),                unused1 char(20), "
                    + "unused2 char(20),               OriginalRate decimal(16,8),     unused3 decimal(19,8), "
                    + "GSTComponent decimal(19,8),     AccountCr char(20),             InvoiceLine integer, "
                    + "Line integer not null,          Entry char(6),                  BaseAmountPaid  decimal(19,8), "
                    + "BaseGSTComponent decimal(19,8), "
                    + "unique(PaymentCode, Line))";
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStringsPaymentL(String[] s) throws Exception
  {
    s[0] = "paymentlInvoiceCodeInx on paymentl(InvoiceCode)";
    
    return 1;
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNamesPaymentL() throws Exception
  {  
    return "PaymentCode, InvoiceCode, InvoiceDate, InvoiceAmount, Description, AmountPaid, DateLastModified, SignOn, unused1, "
         + "unused2, OriginalRate, unused3, GSTComponent, AccountCr, InvoiceLine, Line, Entry, BaseAmountPaid, BaseGSTComponent";
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypesPaymentL() throws Exception
  {
    return "CCDFCFSCCCFFFCIICFF";
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizesPaymentL(short[] sizes) throws Exception
  {
    sizes[0] = 20;   sizes[1] = 20;   sizes[2] = 0;   sizes[3] = 0;   sizes[4] = 80;   sizes[5] = 0;   sizes[6] = -1;
    sizes[7] = 20;   sizes[8] = 20;   sizes[9] = 20;  sizes[10] = 0;  sizes[11] = 0;   sizes[12] = 0;  sizes[13] = 20;
    sizes[14] = 0;   sizes[15] = 0;   sizes[16] = 6;  sizes[17] = 0;  sizes[18] = 0;
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public String getFieldStylesPaymentL() throws Exception
  {
    return "OMOOOOOOOOOOOOOOMOO";
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public boolean getRecToHTML(Connection con, Statement stmt, ResultSet rs, PrintWriter out, char dispOrEdit, byte[] code, String unm, String sid, String uty,
                              String men, String den, String dnm, String bnm, String workingDir, String localDefnsDir, String defnsDir, char cad,
                              String errStr, String cashOrNot, byte[] dataAlready, HttpServletRequest req, int[] bytesOut)
                              throws Exception
  {
    String imagesDir = directoryUtils.getSupportDirs('I');
    byte[][] buf1      = new byte[1][5000];
    byte[][] buf2      = new byte[1][5000];
    char[] source = new char[1];
    source[0] = screenLayout.resetBuffer(buf1, buf2);
    short numFields;
    int[] size1 = new int[1];  size1[0] = 5000;
    int[] size2 = new int[1];  size2[0] = 5000;
    byte[]  fieldNames  = new byte[2000]; // plenty
    byte[]  fieldTypes  = new byte[300];
    short[] fieldSizes = new short[200];

    byte[] data        = new byte[5000];
    byte[] headData    = new byte[5000];
    byte[] prependCode = new byte[25000];
    byte[] b           = new byte[300];

    boolean rtn=false;

    byte[] javaScriptCode = new byte[1000];
    javaScript(con, stmt, rs, req, generalUtils.stringFromBytes(code, 0L), unm, sid, uty, men, den, dnm, bnm, javaScriptCode, localDefnsDir, defnsDir, bytesOut);

    byte[] ddlData = new byte[1500];
    int[] ddlDataLen  = new int[1];

    ddlDataLen[0] = 1500;
    int[] ddlDataUpto = new int[1];
    ddlDataUpto[0] = 0;

    if(cad == 'A' && code[0] == '\000')
    {
      messagePage.msgScreen(false, out, req, 6, unm, sid, uty, men, den, dnm, bnm, "Payment", imagesDir, localDefnsDir,
                         defnsDir, bytesOut);
      return true;
    }

    if(dataAlready[0] != '\000') // coming with an err msg - passing-in data already entered but the data's fields may (will) not be
    {                          // in the correct order
      dispOrEdit = 'E';
      sortFields(dataAlready, headData, "payment");
    }
    else // get header data
    {
      if(cad == 'A')
      {
        if(getRecGivenCode(con, stmt, rs, code, '\001', headData, dnm, localDefnsDir, defnsDir, bytesOut) == -1)
        {
          messagePage.msgScreen(false, out, req, 6, unm, sid, uty, men, den, dnm, bnm, "Payment", imagesDir, localDefnsDir,
                             defnsDir, bytesOut);
          return true;
        }
      }
    }
   
    String date;
    byte[] dateB = new byte[20]; // lock
    if(dataAlready[0] != '\000')
    {
      generalUtils.dfsGivenSeparator(true, '\001', headData, (short)16, dateB);
      date = generalUtils.convertDateToSQLFormat(generalUtils.stringFromBytes(dateB, 0L));
    }
    else
    if(cad == 'A')
    {
      generalUtils.dfsGivenSeparator(true, '\001', headData, (short)16, dateB);
      date = generalUtils.stringFromBytes(dateB, 0L);
    }
    else date = generalUtils.todaySQLFormat(localDefnsDir, defnsDir); 

    byte[] javaScriptCallCode = new byte[1000]; // lock
    javaScriptCall(con, stmt, rs, javaScriptCallCode, req, date, unm, uty, dnm, localDefnsDir, defnsDir, bytesOut);

    short[] fieldSizesPayment = new short[70]; // plenty
    getFieldSizesPayment(fieldSizesPayment);

    if(cad == 'A') // not a new one
    {
      short[] fieldSizesPaymentL = new short[30]; // plenty
      getFieldSizesPaymentL(fieldSizesPaymentL);
   
      // calculate totals from lines
      if(dispOrEdit == 'D') // display head *and* lines
      {
        byte[] line = new byte[20];
        byte[] linesData = new byte[5000];
        int[] listLen = new int[1];
        listLen[0] = 5000;
        int[] linesCount = new int[1];
        linesData = getLines(con, stmt, rs, code, '\001', linesData, listLen, linesCount, dnm, localDefnsDir, defnsDir);
        double totalAmtLines = 0.0, totalBaseAmtLines = 0.0;
      
        for(int x=0;x<linesCount[0];++x)
        {
          if(generalUtils.getListEntryByNum(x, linesData, data)) // just-in-case
          {
            generalUtils.replaceTwosWithOnes(data);

            generalUtils.dfsGivenSeparator(true, '\001', data, (short)6, b);
            totalAmtLines += generalUtils.doubleFromChars(b);
            generalUtils.dfsGivenSeparator(true, '\001', data, (short)18, b);
            totalBaseAmtLines += generalUtils.doubleFromChars(b);
          }
        }
        
        generalUtils.repDoubleGivenSeparator('2', '\001', headData, 5000, (short)15, totalAmtLines);
        generalUtils.repDoubleGivenSeparator('2', '\001', headData, 5000, (short)30, totalBaseAmtLines);
    
        stmt = con.createStatement();
        stmt.executeUpdate("UPDATE payment SET TotalAmount = " + totalAmtLines + ", BaseTotalAmount = " + totalBaseAmtLines
                         + " WHERE PaymentCode = '" + generalUtils.stringFromBytes(code, 0L) + "'");
        stmt.close();
      }
 
      if(dispOrEdit == 'D')
      {  
        numFields = screenLayout.layoutToBuffer(buf1, buf2, size1, fieldNames, fieldTypes, fieldSizes, localDefnsDir, defnsDir, "253.htm", 2, getFieldNamesPayment(), fieldSizesPayment,
                             getFieldNamesPaymentL(), fieldSizesPaymentL, null, null);
      }
      else
      {
        numFields = screenLayout.layoutToBuffer(buf1, buf2, size1, fieldNames, fieldTypes, fieldSizes, localDefnsDir, defnsDir, "253a.htm", 1, getFieldNamesPayment(), fieldSizesPayment, null, null, null,
                             null);
      }
              
      if(dispOrEdit == 'D')
      {
        prepend(con, stmt, rs, unm, sid, uty, men, den, dnm, bnm, generalUtils.stringFromBytes(code, 0L), date, prependCode, req, localDefnsDir, defnsDir,
                bytesOut);
      }
      else
      {
        prependEdit(con, stmt, rs, unm, sid, uty, men, den, dnm, bnm, errStr, "A", generalUtils.stringFromBytes(code, 0L), date, "253a.htm",  localDefnsDir,
                    defnsDir, prependCode, req, bytesOut);
      }

      screenLayout.bufferToBuffer(buf1, buf2, size1, size2, source, numFields, fieldNames, fieldTypes, fieldSizes, dispOrEdit, 'X', data, 0, ddlData, ddlDataUpto[0], javaScriptCode, prependCode);

      if(dispOrEdit == 'D') // display head *and* lines
      {
        byte[] line      = new byte[20];
        byte[] linesData = new byte[5000];
        int[]  listLen = new int[1];  listLen[0] = 5000;
        int[]  linesCount = new int[1];
        linesData = getLines(con, stmt, rs, code, '\001', linesData, listLen, linesCount, dnm, localDefnsDir, defnsDir);

        for(int x=0;x<linesCount[0];++x)
        {
          if(generalUtils.getListEntryByNum(x, linesData, data)) // just-in-case
          {
            generalUtils.replaceTwosWithOnes(data);

            generalUtils.bytesDPsGivenSeparator(true, '\001', '2', data, 5000, 4);  // invoiceAmount
            generalUtils.bytesDPsGivenSeparator(true, '\001', '2', data, 5000, 6);  // amountPaid
            generalUtils.bytesDPsGivenSeparator(true, '\001', '2', data, 5000, 11); // originalRate
            generalUtils.bytesDPsGivenSeparator(true, '\001', '2', data, 5000, 13); // gstComponent
            generalUtils.bytesDPsGivenSeparator(true, '\001', '2', data, 5000, 18); // baseAmountPaid
            generalUtils.bytesDPsGivenSeparator(true, '\001', '2', data, 5000, 19); // baseGSTComponent

            generalUtils.dfsGivenSeparator(true, '\001', data, (short)3, b); // invoicedate
            generalUtils.convertFromYYYYMMDD(b); 
            generalUtils.repAlphaGivenSeparator('\001', data, 5000, (short)3, b);

            generalUtils.dfsGivenSeparator(true, '\001', data, (short)16, line); // origin-1

            screenLayout.bufferToBuffer(buf1, buf2, size1, size2, source, numFields, fieldNames, fieldTypes, fieldSizes, 'D', 'L', data, 5000, ddlData, ddlDataUpto[0], javaScriptCallCode, null);
          }
        }

        scoutln(headData, bytesOut, "payment.basecurrency=" + accountsUtils.getBaseCurrency(con, stmt, rs, dnm, localDefnsDir, defnsDir) + "\001");

        generalUtils.bytesDPsGivenSeparator(true, '\001', '2',  headData, 5000, 29); // rate
        scoutln(headData, bytesOut, "payment.rateNoTrailing=" + generalUtils.dfsAsStrGivenBinary1(true, headData, (short)29) + "\001");
      }
      else // edit
      {
        ddlData = accountsUtils.getCurrencyNamesDDLData(con, stmt, rs, "payment.Currency",  dnm, ddlData, ddlDataUpto, ddlDataLen, localDefnsDir, defnsDir);
        ddlData = accountsUtils.getAROrAPAccountsDDLData('P', "payment.AccCredited", unm, dnm, ddlData, ddlDataUpto, ddlDataLen, workingDir);

        generalUtils.dfsGivenSeparator(true, '\001', headData, (short)17, b); // accountCr
        if(b[0] != '\000')
        {
          generalUtils.repAlphaGivenSeparator('\001', headData, 2000, (short)17, accountsUtils.getAccountPlusDescriptionGivenAccCode(b, unm, dnm, workingDir, localDefnsDir,
                                                                                                                    defnsDir));
        }

        // format notes field
        generalUtils.replaceThreesWithNewlines(generalUtils.dfsAsStrGivenBinary1(true, headData, (short)11), b);
        generalUtils.repAlphaGivenSeparator('\001', headData, 5000, (short)11, b);
      }

      if(dataAlready[0] == '\000') // NOT coming with an err msg
      {
        generalUtils.bytesDPsGivenSeparator(true, '\001', '2', headData, 5000, 15); // TotalAmount
        generalUtils.bytesDPsGivenSeparator(true, '\001', '2', headData, 5000, 29); // Rate
        generalUtils.bytesDPsGivenSeparator(true, '\001', '2', headData, 5000, 30); // BaseTotalAmount
        generalUtils.bytesDPsGivenSeparator(true, '\001', '2', headData, 5000, 37); // bankCharges
        generalUtils.bytesDPsGivenSeparator(true, '\001', '2', headData, 5000, 41); // bankAmount
        
        generalUtils.bytesDPsGivenSeparator(true, '\001', '2', headData, 5000, 38); // discountAllowed
        generalUtils.bytesDPsGivenSeparator(true, '\001', '2', headData, 5000, 43); // exchangeAdjustment
  
        // convert date
        generalUtils.dfsGivenSeparator(true, '\001', headData, (short)1, b);
        generalUtils.convertFromYYYYMMDD(b); 
        generalUtils.repAlphaGivenSeparator('\001', headData, 5000, (short)1, b);

        generalUtils.dfsGivenSeparator(true, '\001', headData, (short)16, b); // datePaid
        generalUtils.convertFromYYYYMMDD(b); 
        generalUtils.repAlphaGivenSeparator('\001', headData, 5000, (short)16, b);

        generalUtils.dfsGivenSeparator(true, '\001', headData, (short)40, b); // dateProcessed
        generalUtils.convertFromYYYYMMDD(b); 
        generalUtils.repAlphaGivenSeparator('\001', headData, 5000, (short)40, b);
      }

      screenLayout.bufferToBuffer(buf1, buf2, size1, size2, source, numFields, fieldNames, fieldTypes, fieldSizes, dispOrEdit, 'H', headData, 5000, ddlData, ddlDataUpto[0], null, null);

      append(con, stmt, rs, buf1, buf2, source, size1, size2, unm, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

      screenLayout.bufferToOut(buf1, buf2, source, out);

      rtn = true;
    }
    else // a new layout
    {
      numFields = screenLayout.layoutToBuffer(buf1, buf2, size1, fieldNames, fieldTypes, fieldSizes, localDefnsDir, defnsDir, "253a.htm", 1, getFieldNamesPayment(), fieldSizesPayment, null, null, null, null);

      if(dataAlready[0] != '\000') // coming with an err msg - passing-in data already entered but the data's fields may (will) not
      {                            // be in the correct order
        sortFields(dataAlready, data, "payment");
      }  
      else
      {
        screenLayout.buildNewDetails(fieldNames, numFields, "payment", data);

        documentUtils.getNextCode(con, stmt, rs, "payment", true, code);

        generalUtils.repAlphaUsingOnes(data, 5000, "PaymentCode", generalUtils.stringFromBytes(code, 0L));
        generalUtils.repAlphaUsingOnes(data, 5000, "Status", "L");
        generalUtils.repAlphaUsingOnes(data, 5000, "Currency", accountsUtils.getBaseCurrency(con, stmt, rs, dnm, localDefnsDir, defnsDir));

        generalUtils.repAlphaUsingOnes(data, 5000, "Date", generalUtils.today(localDefnsDir, defnsDir));
      }

      ddlData = accountsUtils.getCurrencyNamesDDLData(con, stmt, rs, "payment.Currency",  dnm, ddlData, ddlDataUpto, ddlDataLen, localDefnsDir, defnsDir);
      ddlData = accountsUtils.getAROrAPAccountsDDLData('P', "payment.AccCredited", unm, dnm, ddlData, ddlDataUpto, ddlDataLen, workingDir);

      prependEdit(con, stmt, rs, unm, sid, uty, men, den, dnm, bnm, errStr, "C", generalUtils.stringFromBytes(code, 0L), date, "253a.htm", localDefnsDir, defnsDir,
                  prependCode, req, bytesOut);

      screenLayout.bufferToBuffer(buf1, buf2, size1, size2, source, numFields, fieldNames, fieldTypes, fieldSizes, 'E', 'H', data, 5000, ddlData, ddlDataUpto[0], javaScriptCode, prependCode);

      append(con, stmt, rs, buf1, buf2, source, size1, size2, unm, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

      screenLayout.bufferToOut(buf1, buf2, source, out);
      rtn = true;
    }

    return rtn;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void javaScriptCall(Connection con, Statement stmt, ResultSet rs, byte[] b, HttpServletRequest req, String date, String unm, String uty, String dnm,
                              String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    b[0]= '\000';
    scoutln(b, bytesOut, "_.lineFile=paymentl.line\001");
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 5055, unm, uty, dnm, localDefnsDir, defnsDir) && serverUtils.passLockCheck(con, stmt, rs, "payment", date, unm))
      scoutln(b, bytesOut, "_.permitted=y\001");
    else scoutln(b, bytesOut, "_.permitted=n\001");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void javaScript(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, String code, String unm, String sid, String uty, String men,
                          String den, String dnm, String bnm, byte[] b, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 5053, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      b[0]= '\000';
      scoutln(b, bytesOut, "<script language=\"JavaScript\">");
      scoutln(b, bytesOut, "function affect(line){");
      scoutln(b, bytesOut, "window.location.replace(\"/central/servlet/PaymentLine?unm=" + unm + "&sid=" + sid
                         + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + code
                         + "&p2=\"+line+\"&p3=&p4=\");}</script>");
    }
    else b[0] = '\000';
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void append(Connection con, Statement stmt, ResultSet rs, byte[][] buf1, byte[][] buf2, char[] source, int[] iSize1, int[] iSize2, String unm,
                      String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    byte[] b = new byte[1000];
    scoutln(b, bytesOut, "</form>");
    scoutln(b, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir));    
    screenLayout.appendBytesToBuffer(buf1, buf2, source, iSize1, iSize2, b);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void prepend(Connection con, Statement stmt, ResultSet rs, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                       String code, String date, byte[] b, HttpServletRequest req, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    String code2 = code;

    b[0]= '\000';
    scoutln(b, bytesOut, "<html><head><title>" + code + "</title>");
    scoutln(b, bytesOut, "<script language=\"JavaScript\">\n");

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 5051, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(b, bytesOut, "function fetch(){");
      scoutln(b, bytesOut, "window.location.replace(\"/central/servlet/PaymentHeaderEdit?unm=" + unm + "&sid=" + sid + "&uty="
                           + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + code2
                           + "&p2=A&p3=&p4=\");}\n");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 5053, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(b, bytesOut, "function add(){");
      scoutln(b, bytesOut, "window.location.replace(\"/central/servlet/PaymentLine?unm=" + unm + "&sid=" + sid
                         + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + code2
                         + "&p2=&p3=&p4=\");}");
    }
    
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 8016, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(b, bytesOut, "function mail(){");
      scoutln(b, bytesOut, "window.location.replace(\"/central/servlet/MailExternalUserCreate?unm=" + unm + "&sid=" + sid
                         + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=SalesInvoicePage&p2=" + code2
                         + "&p3=Payment&bnm=" + bnm + "\");}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 11800, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(b, bytesOut, "function trail(){");
      scoutln(b, bytesOut, "window.location.href=\"/central/servlet/TrailShow?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men="
                         + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + code2 + "&p2=A\";}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 5056, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(b, bytesOut, "function print(){");
      scoutln(b, bytesOut, "window.location.href=\"/central/servlet/AdminPrintControl?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men="
                          + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=PaymentAdvicePrint&p2=" + code2 + "&p3=\";}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4192, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(b, bytesOut, "function fax(){");
      scoutln(b, bytesOut, "window.location.href=\"/central/servlet/FaxCreate?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men
                         + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + code2 + "&p2=A&p3=PaymentPage&p4=&p5=Payment\";}");
    }
    
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 11200, unm, uty, dnm, localDefnsDir, defnsDir) && authenticationUtils.verifyAccess(con, stmt, rs, req, 4051, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(b, bytesOut, "function pdf(){");
      scoutln(b, bytesOut, "window.location.href=\"/central/servlet/PrintToPDFUtils?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men="
                         + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p3=" + code2 + "&p1=0.000&p5=Payment&p2=PaymentAdvicePrint\";}");
    }

    String cssDirectory = directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir);
    String cssDocumentDirectory = directoryUtils.getCssDocumentDirectory(con, stmt, rs, dnm);
    scoutln(b, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + cssDirectory + "general.css\">\n");
    scoutln(b, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + cssDocumentDirectory + "document.css\">\n");
    
    outputPageFrame(con, stmt, rs, b, req, ' ', ' ', "", "PaymentPage", date, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    scoutln(b, bytesOut, "<form>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void prependEdit(Connection con, Statement stmt, ResultSet rs, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                           String errStr, String cad, String code, String date, String layoutFile, String localDefnsDir, String defnsDir, byte[] b,
                           HttpServletRequest req, int[] bytesOut) throws Exception
  {
    String code2 = generalUtils.sanitise(code);
    scoutln(b, bytesOut, "<html><head><title>" + code + "</title>");

    scoutln(b, bytesOut, "<script language=\"JavaScript\">\n");

    byte[] sourceBuf = new byte[5000];
    sourceBuf = htmlBuild.layoutToBuffer(sourceBuf, 5000, localDefnsDir, defnsDir, layoutFile);
    String saveStr = htmlBuild.buildSaveStringDelimited(sourceBuf, "document.forms[0]");

    scoutln(b, bytesOut, "function setCodeS(code){document.forms[0].CompanyCode.value=code;");
    scoutln(b, bytesOut, "main.style.visibility='visible';second.style.visibility='hidden';second.style.height='0';submenu.style.visibility='visible';}");

    scoutln(b, bytesOut, "function save(thisOp){var saveStr=\"\";" + saveStr + "\n");
    scoutln(b, bytesOut, "var saveStr2=strip(saveStr);window.location.replace(\"/central/servlet/PaymentHeaderUpdate?unm=" + unm + "&sid=" + sid + "&uty=" + uty
                       + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + cad + "&p2=" + code2
                       + "&p3=\"+saveStr2+\"&p4=\"+thisOp)}\n");
    scoutln(b, bytesOut, "function strip(saveStr){\n");
    scoutln(b, bytesOut, "var saveStr2='';var x;var len=saveStr.length;\n");
    scoutln(b, bytesOut, "for(x=0;x<len;++x)\n");
    scoutln(b, bytesOut, "if(saveStr.charAt(x)=='#')saveStr2+='%23';\n");
    scoutln(b, bytesOut, "else if(saveStr.charAt(x)=='\"')saveStr2+='%22';\n");
    scoutln(b, bytesOut, "else if(saveStr.charAt(x)=='&')saveStr2+='%26';\n");
    scoutln(b, bytesOut, "else if(saveStr.charAt(x)=='%')saveStr2+='%25';\n");
    scoutln(b, bytesOut, "else if(saveStr.charAt(x)==' ')saveStr2+='%20';\n");
    scoutln(b, bytesOut, "else if(saveStr.charAt(x)=='+')saveStr2+='%2B';\n");
    scoutln(b, bytesOut, "else if(saveStr.charAt(x)=='?')saveStr2+='%3F';\n");
    scoutln(b, bytesOut, "else if(escape(saveStr.charAt(x))=='%0A')saveStr2+='\003';\n");
    scoutln(b, bytesOut, "else if(escape(saveStr.charAt(x))=='%0D');");
    scoutln(b, bytesOut, "else saveStr2+=saveStr.charAt(x);return saveStr2;}\n");

    String cssDirectory = directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir);
    String cssDocumentDirectory = directoryUtils.getCssDocumentDirectory(con, stmt, rs, dnm);
    scoutln(b, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + cssDirectory + "general.css\">\n");
    scoutln(b, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + cssDocumentDirectory + "document.css\">\n");

    outputPageFrame(con, stmt, rs, b, req, ' ', cad.charAt(0), "", "PaymentHeaderEdit", date, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    if(errStr.length() > 0 && ! errStr.equalsIgnoreCase("null"))
      scoutln(b, bytesOut, errStr);

    scoutln(b, bytesOut, "<form>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  // rtns: 'X' if already exists
  //       ' ' if updated successfully
  //       'N' if no code specified
  //       'F' if not updated
  //       'I' if illegal code
  public char putLine(Connection con, Statement stmt, ResultSet rs, byte[] originalLine, byte[] code, String unm, String dnm, String localDefnsDir, String defnsDir,
                      char cad, byte[] recData, int recDataLen, byte[] rtnLineBytes, int[] bytesOut) throws Exception
  {
    byte[] lineBytes = new byte[20];
    byte[] b = new byte[100];
    generalUtils.catAsBytes("Line", 0, b, true);

    if(searchDataString(recData, recDataLen, "paymentl", b, lineBytes) == -1)
      return 'N';

    if(lineBytes[0] == '\000') return ' ';

    if(! generalUtils.validNumeric(lineBytes, 0))
      return 'I';

    char newOrEdit;

    if(originalLine[0] == '\000')
      newOrEdit = 'N';
    else  // originalline not blank
    {
      if(generalUtils.matchIgnoreCase(originalLine, 0, lineBytes, 0))
        newOrEdit = 'E';
      else // change in the line
      {
        if(cad == 'A')
          newOrEdit = 'E';
        else newOrEdit = 'N';
      }
    }

    byte[] buf = new byte[2000];
    generalUtils.bytesToBytes(rtnLineBytes, 0, lineBytes, 0);

    // determines the number of fields and then processes them in order *but* assumes that fields in data are in no particular order

    sortFields(recData, buf, "paymentl"); // sorts the data field (using buf in the process); results are put back into recData
    recDataLen = generalUtils.lengthBytes(recData, 0);
    generalUtils.zeroize(buf, 2000);
        
    String fieldNames = getFieldNamesPaymentL();
    byte[] value      = new byte[3000]; // plenty - to cover desc
    byte[] fieldName  = new byte[31];
    int x=0, y, fieldCount=0;
    int len = fieldNames.length();
    
    while(x < len)
    {
      y=0;
      while(x < len && fieldNames.charAt(x) != ',')
        fieldName[y++] = (byte)fieldNames.charAt(x++);
      fieldName[y] = '\000';
      ++x;

      while(x < len && fieldNames.charAt(x) == ' ')
        ++x;

      if(searchDataString(recData, recDataLen, "paymentl", fieldName, value) != -1) // entry exists
      {
        if(fieldCount == 2) // date
        {
          generalUtils.putAlpha(buf, 2000, (short)2, generalUtils.convertDateToSQLFormat(generalUtils.stringFromBytes(value, 0L)));  
        }
        else
        if(fieldCount == 10) // originalrate
        {
          if(value[0] == '\000')
            generalUtils.repAlpha(buf, 2000, (short)10, "1.00");
          else generalUtils.repAlpha(buf, 2000, (short)10, value);
        }
        else generalUtils.repAlpha(buf, 2000, (short)fieldCount, value);
      }
      
      ++fieldCount;
    }
    
    generalUtils.repAlpha(buf, 2000, (short)0,  code);
    generalUtils.repAlpha(buf, 2000, (short)15, lineBytes);
   
    // just-in-case no value then need to insert a default
    if((generalUtils.dfsAsStr(buf, (short)2)).length() == 0) // invoiceDate 
      generalUtils.putAlpha(buf, 2000, (short)2, "1970-01-01");

    if((generalUtils.dfsAsStr(buf, (short)8)).length() == 0) // unused1
      generalUtils.putAlpha(buf, 2000, (short)8, "0");

    if((generalUtils.dfsAsStr(buf, (short)9)).length() == 0) // unused2
      generalUtils.putAlpha(buf, 2000, (short)9, "0");

    if((generalUtils.dfsAsStr(buf, (short)11)).length() == 0) // unused3
      generalUtils.putAlpha(buf, 2000, (short)11, "0");

    if((generalUtils.dfsAsStr(buf, (short)12)).length() == 0) // GSTComponent
      generalUtils.putAlpha(buf, 2000, (short)12, "0.0");

    if((generalUtils.dfsAsStr(buf, (short) 14)).length() == 0) // invoiceLine
      generalUtils.putAlpha(buf, 2000, (short) 14, "0");

    if((generalUtils.dfsAsStr(buf, (short)18)).length() == 0) // baseGSTComponent
      generalUtils.putAlpha(buf, 2000, (short)18, "0.0");

    if(putRecLine(con, stmt, rs, code, lineBytes, newOrEdit, buf, dnm, localDefnsDir, defnsDir) != 'F')
    {
      updateDLM(con, stmt, rs, code, unm, dnm, localDefnsDir, defnsDir);
      return ' ';
    }

    return 'F';
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void updateDLM(Connection con, Statement stmt, ResultSet rs, byte[] code, String unm, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    stmt = con.createStatement();
    stmt.executeUpdate("UPDATE payment SET SignOn = '" + unm + "', DateLastModified = NULL WHERE PaymentCode = '"
                       + generalUtils.stringFromBytes(code, 0L) + "'");
    if(stmt != null) stmt.close();
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getAPaymentFieldGivenCode(Connection con, Statement stmt, ResultSet rs, String fieldName, String ocCode, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    byte[] value = new byte[300]; // plenty
    byte[] b     = new byte[21];
    generalUtils.strToBytes(b, ocCode);
    
    getAPaymentFieldGivenCode(con, stmt, rs, fieldName, b, value, dnm, localDefnsDir, defnsDir);
    
    return generalUtils.stringFromBytes(value, 0L);
  }
  public void getAPaymentFieldGivenCode(Connection con, Statement stmt, ResultSet rs, String fieldName, byte[] paymentCode, byte[] value, String dnm, String localDefnsDir,
                                        String defnsDir) throws Exception
  {
    if(paymentCode[0] == '\000') // just-in-case
      return;

    generalUtils.toUpper(paymentCode, 0);

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT " + fieldName + " FROM payment WHERE PaymentCode = '"
                                     + generalUtils.stringFromBytes(paymentCode, 0L) + "'");
    if(! rs.next())
      value[0] = '\000';
    else
    {    
      ResultSetMetaData rsmd = rs.getMetaData();
      generalUtils.strToBytes(value, getValue(1, ' ', rs, rsmd));
    }
    
    rs.close();
    stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  // format of data: "do.docode=12345/DO\001 ..."
  public int searchDataString(byte[] data, int lenData, String tableName, byte[] fieldName, byte[] value)
  {
    int x=0;

    byte[] tfName = new byte[50];
    byte[] tableAndFieldName = new byte[50];

    int len = tableName.length();
    while(x < len)
    {
      tableAndFieldName[x] = (byte)tableName.charAt(x);
      ++x;
    }
    tableAndFieldName[x++] = '.';

    int y=0;
    len = generalUtils.lengthBytes(fieldName, 0);
    while(y < len)
      tableAndFieldName[x++] = fieldName[y++];
    tableAndFieldName[x] = '\000';

    int ptr=0;
    while(ptr < lenData)
    {
      x=0;
      while(data[ptr] != '\000' && data[ptr] != '=')
        tfName[x++] = data[ptr++];
      tfName[x] = '\000';

      if(generalUtils.matchIgnoreCase(tfName, 0, tableAndFieldName, 0))
      {
        ++ptr; // '='
        int valuePtr=0;
        while(ptr < lenData && data[ptr] != '\001')
          value[valuePtr++] = data[ptr++];
        value[valuePtr] = '\000';
        return valuePtr;
      }
      
      // else not the reqd table/field entry
      ++ptr; // '='
      while(data[ptr] != '\000' && data[ptr] != '\001') // e o data entry
        ++ptr;
      if(data[ptr] == '\001')
        ++ptr;
    }

    return -1; // data not found
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public boolean buildHTMLLayoutForLine(Connection con, Statement stmt, ResultSet rs, PrintWriter out, byte[] code, byte[] line, String unm, String sid, String uty,
                                        String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir,
                                        String errStr, byte[] dataAlready, HttpServletRequest req, int[] bytesOut)
                                        throws Exception
  {
    byte[][] buf1      = new byte[1][5000];
    byte[][] buf2      = new byte[1][5000];
    char[] source = new char[1];
    source[0] = screenLayout.resetBuffer(buf1, buf2);
    short numFields;
    int[] size1 = new int[1];  size1[0] = 5000;
    int[] size2 = new int[1];  size2[0] = 5000;
    byte[]  fieldNames  = new byte[2000]; // plenty
    byte[]  fieldTypes  = new byte[300];
    short[] fieldSizes = new short[200];
    
    String layoutFile="253b.htm";

    short[] fieldSizesPaymentL = new short[30]; // plenty
    getFieldSizesPaymentL(fieldSizesPaymentL);

    numFields = screenLayout.layoutToBuffer(buf1, buf2, size1, fieldNames, fieldTypes, fieldSizes, localDefnsDir, defnsDir, layoutFile, 1,
                                     getFieldNamesPaymentL(), fieldSizesPaymentL, null, null, null, null);

    byte[] b    = new byte[2000]; // plenty
    byte[] data = new byte[2000]; // plenty
    byte[] ddlData = new byte[1000];
    int[] ddlDataLen  = new int[1];
    ddlDataLen[0] = 1000;
    int[] ddlDataUpto = new int[1];
    ddlDataUpto[0] = 0;
    int dataLen = 2000;
    byte[] lineData = new byte[1000];
    int nextLine=0;
    char newOrEdit;

    if(line[0] != '\000') // existing line
    {
      if(code[0] != '\000') // just-in-case
      {
        if(dataAlready[0] != '\000') // coming with an err msg - passing-in data already entered but the data's fields may (will) not
        {                            // be in the correct order
          sortFields(dataAlready, data, "paymentl");
        }
        else
        {
          getLine(con, stmt, rs, code, line, '\001', data, dnm, localDefnsDir, defnsDir, bytesOut);

          generalUtils.bytesDPsGivenSeparator(true, '\001', '2', data, 2000, 3);  // invoiceAmount
          generalUtils.bytesDPsGivenSeparator(true, '\001', '2', data, 2000, 5);  // amountPaid
          generalUtils.bytesDPsGivenSeparator(true, '\001', '2', data, 2000, 10); // originalRate
          generalUtils.bytesDPsGivenSeparator(true, '\001', '2', data, 2000, 12); // gstComponent
          generalUtils.bytesDPsGivenSeparator(true, '\001', '2', data, 2000, 17); // baseAmountPaid
          generalUtils.bytesDPsGivenSeparator(true, '\001', '2', data, 2000, 18); // baseGSTComponent

          generalUtils.dfsGivenSeparator(true, '\001', data, (short)2, b); // invoiceDate
          if(! generalUtils.match(b,"1970-01-01"))
          {
            generalUtils.convertFromYYYYMMDD(b);
            generalUtils.repAlphaGivenSeparator('\001', data, 2000, (short)2, b);
          }
        }
      }

      newOrEdit = 'E';
    }
    else /// new line
    {
      nextLine = getNextLine(con, stmt, rs, code, lineData, dnm, localDefnsDir, defnsDir);

      // when doing save & new, pre-fill app. fields
      if(dataAlready[0] != '\000') // passing-in data already entered but the data's fields may (will) not be in the correct order
      {
        sortFields(dataAlready, data, "paymentl");
            
        generalUtils.dfsGivenSeparator(true, '\001', data, (short)2, b); // desc
        generalUtils.repAlphaGivenSeparator('\001', data, 2000, (short)4, b);
        generalUtils.repAlphaGivenSeparator('\001', data, 2000, (short)15, generalUtils.intToStr(nextLine)); // line
        generalUtils.repAlphaGivenSeparator('\001', data, 2000, (short)16, generalUtils.intToStr(nextLine)); // entry
      }
      else
      {
        data[0]= '\000';
        scoutln(data, bytesOut, "paymentl.entry=" + generalUtils.intToStr(nextLine) + "\001");

        String gstRateDefault = accountsUtils.getDefaultGSTRate(con, stmt, rs, dnm, localDefnsDir, defnsDir);
        scoutln(data, bytesOut, "paymentl.gstrate=" + gstRateDefault + "\001");
      }

      newOrEdit = 'N';
    }

    ddlData = accountsUtils.getGSTRatesDDLData(con, stmt, rs, "paymentl.gstrate", dnm, ddlData, ddlDataUpto, ddlDataLen, localDefnsDir, defnsDir);

    getAPaymentFieldGivenCode(con, stmt, rs, "Currency", code, b, dnm, localDefnsDir, defnsDir);
    scoutln(data, bytesOut, "paymentl.currency=" + generalUtils.stringFromBytes(b, 0L) + "\001");

    scoutln(data, bytesOut, "paymentl.basecurrency=" + accountsUtils.getBaseCurrency(con, stmt, rs, dnm, localDefnsDir, defnsDir) + "\001");

    dataLen = generalUtils.lengthBytes(data, 0);

    String cad;
    if(newOrEdit == 'N')
      cad = "C";
    else cad = "A";

    byte[] prependCode = new byte[25000];
    prependEditLine(con, stmt, rs, unm, sid, uty, men, den, dnm, bnm, errStr, cad, generalUtils.stringFromBytes(code, 0L),
                    generalUtils.stringFromBytes(line, 0L), "253b.htm", localDefnsDir, defnsDir, prependCode, newOrEdit, req,
                    bytesOut);

    screenLayout.bufferToBuffer(buf1, buf2, size1, size2, source, numFields, fieldNames, fieldTypes, fieldSizes, 'E', 'H', data, dataLen, ddlData, ddlDataUpto[0], null, prependCode, false, false, "", nextLine);

    append(con, stmt, rs, buf1, buf2, source, size1, size2, unm, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    screenLayout.bufferToOut(buf1, buf2, source, out);

    return true;
  }
                                                                 
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void prependEditLine(Connection con, Statement stmt, ResultSet rs, String unm, String sid, String uty, String men, String den, String dnm,
                               String bnm, String errStr, String cad, String code, String line, String layoutFile, String localDefnsDir,
                               String defnsDir, byte[] b, char newOrEdit, HttpServletRequest req, int[] bytesOut) throws Exception
  {
    String code2 = generalUtils.sanitise(code);

    b[0] = '\000';
    scoutln(b, bytesOut, "<html><head><title>" + code + "</title>");
    scoutln(b, bytesOut, "<script language=\"JavaScript\">\n");

    byte[] sourceBuf = new byte[5000];
    sourceBuf = htmlBuild.layoutToBuffer(sourceBuf, 5000, localDefnsDir, defnsDir, layoutFile);
    String saveStr = htmlBuild.buildSaveStringDelimited(sourceBuf, "document.forms.doc");
    
    scoutln(b, bytesOut, "function setCode(code){document.forms.doc.ItemCode.value=code;main.style.visibility='visible';");
    scoutln(b, bytesOut, "second.style.visibility='hidden';third.style.visibility='hidden';second.style.height='0';third.style.height='0';submenu.style.visibility='visible';}");
       
    scoutln(b, bytesOut, "function save(thisOp){var saveStr=\"\";" + saveStr + "\n");
    scoutln(b, bytesOut, "var saveStr2=strip(saveStr);window.location.replace(\"/central/servlet/PaymentLineUpdate?unm=" + unm + "&sid=" + sid + "&uty=" + uty
                       + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + cad + "&p2=" + code2 + "&p3=" + line
                       + "&p4=\"+thisOp+\"&p5=\"+saveStr2);}\n");
    scoutln(b, bytesOut, "function strip(saveStr){\n");
    scoutln(b, bytesOut, "var saveStr2='';var x;var len=saveStr.length;\n");
    scoutln(b, bytesOut, "for(x=0;x<len;++x)\n");
    scoutln(b, bytesOut, "if(saveStr.charAt(x)=='#')saveStr2+='%23';\n");
    scoutln(b, bytesOut, "else if(saveStr.charAt(x)=='\"')saveStr2+='%22';\n");
    scoutln(b, bytesOut, "else if(saveStr.charAt(x)=='&')saveStr2+='%26';\n");
    scoutln(b, bytesOut, "else if(saveStr.charAt(x)=='%')saveStr2+='%25';\n");
    scoutln(b, bytesOut, "else if(saveStr.charAt(x)==' ')saveStr2+='%20';\n");
    scoutln(b, bytesOut, "else if(saveStr.charAt(x)=='+')saveStr2+='%2B';\n");
    scoutln(b, bytesOut, "else if(saveStr.charAt(x)=='?')saveStr2+='%3F';\n");
    scoutln(b, bytesOut, "else if(escape(saveStr.charAt(x))=='%0A')saveStr2+='\003';\n");
    scoutln(b, bytesOut, "else if(escape(saveStr.charAt(x))=='%0D');");
    scoutln(b, bytesOut, "else saveStr2+=saveStr.charAt(x);return saveStr2;}\n");

    String cssDirectory = directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir);
    String cssDocumentDirectory = directoryUtils.getCssDocumentDirectory(con, stmt, rs, dnm);
    scoutln(b, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + cssDirectory + "general.css\">\n");
    scoutln(b, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + cssDocumentDirectory + "document.css\">\n");

    outputPageFrame(con, stmt, rs, b, req, newOrEdit, ' ', "", "PaymentLine", "", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    scoutln(b, bytesOut, "<input type=hidden name=\"thisline\" VALUE=\"" + line + "\">");

    if(errStr.length() > 0 && ! errStr.equalsIgnoreCase("null"))
      scoutln(b, bytesOut, errStr);

    scoutln(b, bytesOut, "<form name=doc>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private int getNextLine(Connection con, Statement stmt, ResultSet rs, byte[] code, byte[] data, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    byte[] linesData = new byte[5000];
    int[] listLen = new int[1];  listLen[0] = 5000;
    int[] linesCount = new int[1];

    linesData = getLines(con, stmt, rs, code, '\001', linesData, listLen, linesCount, dnm, localDefnsDir, defnsDir);

    int thisOne, soFar=0;
    for(int x=0;x<linesCount[0];++x)
    {
      if(generalUtils.getListEntryByNum(x, linesData, data)) // just-in-case
      {
        generalUtils.replaceTwosWithOnes(data);
        thisOne = generalUtils.strToInt(generalUtils.dfsAsStrGivenBinary1(true, data, (short)16)); // 'line': origin-0 + 1 (for leading count)
        if(thisOne > soFar)
          soFar = thisOne;
      }
    }

    return soFar + 1;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // rtns: 'X' if already exists
  //       ' ' if updated sucessfully
  //       'N' if no code specified
  //       'F' if not updated
  //       'I' if illegal code
  public char putHead(Connection con, Statement stmt, ResultSet rs, byte[] originalCode, String unm, String dnm, String localDefnsDir, String defnsDir, char cad, byte[] recData, int recDataLen,
                      byte[] code, int[] bytesOut) throws Exception
  {
    byte[] str = new byte[21];  str[0] = '\000';
    generalUtils.catAsBytes("PaymentCode", 0, str, true);

    if(searchDataString(recData, recDataLen, "payment", str, code) == 0)
      return 'N';

    char newOrEdit;

    if(cad == 'C')
      newOrEdit = 'N';
    else // cad == 'A'
    if(generalUtils.matchIgnoreCase(originalCode, 0, code, 0))
      newOrEdit = 'E';
    else // change in the code
      newOrEdit = 'N';

    generalUtils.toUpper(code, 0);

    // get data values from recData and put into buf for updating

    byte[] buf = new byte[2000];
    generalUtils.putAlpha(buf, 2000, (short)0, code);

    byte[] value   = new byte[501];
    byte[] fldName = new byte[50];

    String fieldNamesPayment = getFieldNamesPayment();

    // chk if any of the compname or addr flds have values already
    int len;
    boolean atleastOneHasValue=false;
    for(short x=3;x<10;++x)
    {
      getFieldName(fieldNamesPayment, x, fldName);
      if(searchDataString(recData, recDataLen, "payment", fldName, value) != -1) // entry exists
      {
        if(value[0] != '\000')
          atleastOneHasValue = true;
      }
    }

    double rate = 1.0;
    len = fieldNamesPayment.length();
    int x=0, y, fieldCount=0;
    while(x < len)
    {
      y=0;
      while(x < len && fieldNamesPayment.charAt(x) != ',')
        fldName[y++] = (byte)fieldNamesPayment.charAt(x++);
      fldName[y] = '\000';
      ++x;
      
      while(x < len && fieldNamesPayment.charAt(x) == ' ')
        ++x;

      if(searchDataString(recData, recDataLen, "payment", fldName, value) != -1) // entry exists
      {  
        if(fieldCount == 0) // code
          ;
        else
        if(fieldCount == 1) // date
        {
          generalUtils.repAlpha(buf, 2000, (short)1, generalUtils.convertDateToSQLFormat(generalUtils.stringFromBytes(value, 0L)));  
        }
        else
        if(fieldCount == 2)
        {
          generalUtils.toUpper(value, 0);

          if(! atleastOneHasValue) // companycode
          {
            byte[] b    = new byte[100];
            byte[] data = new byte[3000];
            if(supplier.getSupplierRecGivenCode(con, stmt, rs, value, '\000', dnm, data, localDefnsDir, defnsDir) != -1) // exists
            {
              generalUtils.dfs(data, (short)1, b);           // name
              generalUtils.repAlpha(buf, 2000, (short)3, b);
              generalUtils.dfs(data, (short)2, b);           // addr1
              generalUtils.repAlpha(buf, 2000, (short)4, b);
              generalUtils.dfs(data, (short)3, b);           // addr2
              generalUtils.repAlpha(buf, 2000, (short)5, b);
              generalUtils.dfs(data, (short)4, b);           // addr3
              generalUtils.repAlpha(buf, 2000, (short)6, b);
              generalUtils.dfs(data, (short)5, b);           // addr4
              generalUtils.repAlpha(buf, 2000, (short)7, b);
              generalUtils.dfs(data, (short)6, b);           // addr5
              generalUtils.repAlpha(buf, 2000, (short)8, b);
              generalUtils.dfs(data, (short)7, b);           // pc
              generalUtils.repAlpha(buf, 2000, (short)9, b);
            }  
          }

          generalUtils.putAlpha(buf, 2000, (short)2, value);
        }
        else
        if(fieldCount == 17) // accCredited
        {
          int z=0, lenz=generalUtils.lengthBytes(value, 0);
          while(z < lenz && value[z] != ' ') // just-in-case
            ++z;
          value[z] = '\000';
          generalUtils.repAlpha(buf, 2000, (short) 17, value);
        }
        else
        if(fieldCount == 21) // signon
        {
          if(value[0] == '\000')
            generalUtils.putAlpha(buf, 2000, (short)21, unm);
          else generalUtils.putAlpha(buf, 2000, (short)21, value);
        }
        else
        if(fieldCount == 11) // notes
        {
          byte[] b2 = new byte[300];
          int i2=0, valueLen=generalUtils.lengthBytes(value, 0);
          for(int i=0;i<valueLen;++i)
          {
            if(value[i] == (byte)10)
              ; // ignore
            if(value[i] == (byte)1) // autoConvert old data that uses '\001' as linebreak
              b2[i2++] = '\003';
            else b2[i2++] = value[i];
          }
          
          b2[i2] = '\000';
          generalUtils.putAlpha(buf, 2000, (short)11, b2);
        }
        else
        if(fieldCount == 16) // datePaid
        {
          generalUtils.repAlpha(buf, 2000, (short)16, generalUtils.convertDateToSQLFormat(generalUtils.stringFromBytes(value, 0L)));  
        }
        else
        if(fieldCount == 40) // dateProcessed
        {
          generalUtils.repAlpha(buf, 2000, (short)40, generalUtils.convertDateToSQLFormat(generalUtils.stringFromBytes(value, 0L)));  
        }
        else
        if(fieldCount == 29) // rate
        {
          if(value[0] == '\000')
          {  
            generalUtils.repAlpha(buf, 2000, (short)29, "1.00");
            rate = 1.00;
          }
          else
          {
            generalUtils.repAlpha(buf, 2000, (short)29, value);
            rate = generalUtils.doubleFromBytesCharFormat(value, 0);
          }
        }
        else
        if(fieldCount == 42) // ratebanktobase
        {
          if(value[0] == '\000')
            generalUtils.repAlpha(buf, 2000, (short)42, "1.00");
          else generalUtils.repAlpha(buf, 2000, (short)42, value);
        }
        else if(value[0] != '\000') generalUtils.repAlpha(buf, 2000, (short)fieldCount, value);
      }
      
      ++fieldCount;
    }

    // just-in-case no value then need to insert a default
    if((generalUtils.dfsAsStr(buf, (short)15)).length() == 0) // TotalAmount 
      generalUtils.putAlpha(buf, 2000, (short)15, "0"); 

    if((generalUtils.dfsAsStr(buf, (short)24)).length() == 0) // GSTComponent
      generalUtils.putAlpha(buf, 2000, (short)24, "0");
    
    if((generalUtils.dfsAsStr(buf, (short)26)).length() == 0) // printCount
      generalUtils.putAlpha(buf, 2000, (short)26, "0");
    
    if((generalUtils.dfsAsStr(buf, (short)30)).length() == 0) // baseTotalAmount 
      generalUtils.putAlpha(buf, 2000, (short)30, "0");
    
    if((generalUtils.dfsAsStr(buf, (short)32)).length() == 0) // BaseGSTComponent
      generalUtils.putAlpha(buf, 2000, (short)32, "0");
    
    if((generalUtils.dfsAsStr(buf, (short)37)).length() == 0) // Charges 
        generalUtils.putAlpha(buf, 2000, (short)37, "0");

    if((generalUtils.dfsAsStr(buf, (short)38)).length() == 0) // DiscountAllowed 
        generalUtils.putAlpha(buf, 2000, (short)38, "0");

    if((generalUtils.dfsAsStr(buf, (short)41)).length() == 0) // BankAmount 
        generalUtils.putAlpha(buf, 2000, (short)41, "0");

    if((generalUtils.dfsAsStr(buf, (short)42)).length() == 0) // RateBankToBase 
        generalUtils.putAlpha(buf, 2000, (short)42, "0");

    if((generalUtils.dfsAsStr(buf, (short)43)).length() == 0) // ExchangeAdjustment 
        generalUtils.putAlpha(buf, 2000, (short)43, "0");

    if((generalUtils.dfsAsStr(buf, (short)16)).length() == 0) // datepaid 
      generalUtils.repAlpha(buf, 2000, (short)16, "1970-01-01");
  
    if((generalUtils.dfsAsStr(buf, (short)40)).length() == 0) // dateprocessed 
      generalUtils.repAlpha(buf, 2000, (short)40, "1970-01-01"); 

    if((generalUtils.dfsAsStr(buf, (short)21)).length() == 0) // signon 
      generalUtils.repAlpha(buf, 2000, (short)21, unm);
    
    // if rate has changed then recalc each line
    boolean rateChanged = false;
    if(cad == 'A')
    {
      byte[] data = new byte[2000];
      if(getRecGivenCode(con, stmt, rs, code, '\000', data, dnm, localDefnsDir, defnsDir, bytesOut) != -1) // just-in-case
      {
        if(rate != generalUtils.dfsAsDouble(data, (short)29)) // rate changed
          rateChanged = true;
      }
    }

    char rtn = putRecHeadGivenCode(con, stmt, rs, code, newOrEdit, buf, dnm, localDefnsDir, defnsDir);

    if(rtn == ' ') // head saved ok
    {
      if(rateChanged)
      {
        int z;
        byte[] line       = new byte[500];
        byte[] linesData  = new byte[2000];
        int[]  listLen    = new int[1];  listLen[0] = 2000;
        int[]  linesCount = new int[1];
        linesData = getLines(con, stmt, rs, code, '\000', linesData, listLen, linesCount, dnm, localDefnsDir, defnsDir);
        for(x=0;x<linesCount[0];++x)
        {
          if(generalUtils.getListEntryByNum(x, linesData, line)) // just-in-case
          {
            // shunt-down to remove lines-count (first entry)
            y=0;
            while(y < 500 && line[y] != '\002')
              ++y;
            ++y;
            z=0;
            while(y < 500 && line[y] != '\000')
            {
              if(line[y] == '\002')
                buf[z] = '\000';
              else buf[z] = line[y];

              ++y;
              ++z;
            }
            buf[z] = '\000';

            reCalculate(buf, z, rate);

            generalUtils.repAlpha(buf, 2000, (short)7, unm);

            generalUtils.dfs(buf, (short)15, str); // line

            rtn = putRecLine(con, stmt, rs, code, str, 'E', buf, dnm, localDefnsDir, defnsDir);
          }
        }
      }
    }

    return rtn;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void reCalculate(byte[] buf, int dataBufLen, double rate) throws Exception
  {
    double amt = generalUtils.dfsAsDouble(buf, (short)5); // amountPaid

    amt = generalUtils.doubleDPs(amt, '2');
    generalUtils.repDoubleGivenSeparator('2', '\000', buf, dataBufLen, (short)17, (amt * rate));
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void getFieldName(String fieldNames, int fieldReqd, byte[] fieldName) throws Exception
  {
    int x=0, y, fieldCount=0;
    int len = fieldNames.length();

    while(fieldCount < fieldReqd)
    {  
      while(x < len && fieldNames.charAt(x) != ',')
        ++x;
      ++x;
      while(x < len && fieldNames.charAt(x) == ' ')
        ++x;
      ++fieldCount;
    }
      
    if(x >= len) // just-in-case
      fieldName[0] = '\000';
    else
    {
      y=0;
      while(x < len && fieldNames.charAt(x) != ',')
        fieldName[y++] = (byte)fieldNames.charAt(x++);
      fieldName[y] = '\000';
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public int getRecGivenCode(Connection con, Statement stmt, ResultSet rs, byte[] code, char separator, byte[] data, String dnm, String localDefnsDir, String defnsDir,
                             int[] bytesOut) throws Exception
  {
    if(code[0] == '\000') // just-in-case
      return -1;

    generalUtils.toUpper(code, 0);

    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT * FROM payment WHERE PaymentCode = '" + generalUtils.stringFromBytes(code, 0L) + "'");
    if(! rs.next())
    {
      rs.close();
      stmt.close();
      return -1;
    }      
    
    ResultSetMetaData rsmd = rs.getMetaData();
    
    String fieldTypes = getFieldTypesPayment();
     
    if(separator == '\000')
    {
      int x, y=0;
      String s = getValue(1, ' ', rs, rsmd);
      int len = s.length();
      for(x=0;x<len;++x)
        data[y++] = (byte)s.charAt(x);
      data[y++] = '\000';
      
      for(int z=2;z<=fieldTypes.length();++z)
      {
        s = getValue(z, fieldTypes.charAt(z-1), rs, rsmd);
        len = s.length();
        for(x=0;x<len;++x)
          data[y++] = (byte)s.charAt(x);
        data[y++] = '\000';
      }
    }
    else // separator == \001
    {    
      String fieldNames = getFieldNamesPayment();
      String thisFieldName;
      char thisFieldType;
      data[0]= '\000';

      int x=0, count=0, len = fieldNames.length();
      while(x < len)
      {
        thisFieldName="";
        while(x < len && fieldNames.charAt(x) != ',')
          thisFieldName += fieldNames.charAt(x++);
        thisFieldType = fieldTypes.charAt(count++);
        scoutln(data, bytesOut, "payment." + thisFieldName + "=" + getValue(count, thisFieldType, rs, rsmd) + "\001");
      
        ++x;
        while(x < len && fieldNames.charAt(x) == ' ')
          ++x;
      }
    }  
    
    rs.close();
    stmt.close();
    
    return 0;   
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------
  public String getValue(int colNum, char type, ResultSet rs, ResultSetMetaData rsmd)
  {
    if(colNum < 0)
      return "";
    
    try
    {
      Integer f;
      BigDecimal bd;
      java.sql.Date d;
      java.sql.Timestamp ts;
      Time t;

      String str="";

      switch(rsmd.getColumnType(colNum))
      {
        case java.sql.Types.CHAR    : str = rs.getString(colNum);
                                      str = generalUtils.stripTrailingSpacesStr(str);
                                      break;
        case java.sql.Types.INTEGER : f = rs.getInt(colNum);
                                      str = f.toString();
                                      break;
        case java.sql.Types.DECIMAL : bd = rs.getBigDecimal(colNum);
                                      str = bd.toPlainString();
                                      break;
        case 91                     : if(type == 'D')
                                      {
                                        d = rs.getDate(colNum);
                                        str = d.toString();
                                      }  
                                      else 
                                      {
                                        t = rs.getTime(colNum);
                                        str = t.toString();
                                      }  
                                      break;
        case 93                     : ts = rs.getTimestamp(colNum);
                                      str = ts.toString();
                                      str = generalUtils.convertFromTimestamp(str);
                                      break;
        case -1                     : str = rs.getString(colNum);
                                      str = generalUtils.stripTrailingSpacesStr(str);
                                      break;
      }

      return generalUtils.stripNonDisplayable(str);
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }

    return "";
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public char putRecHeadGivenCode(Connection con, Statement stmt, ResultSet rs, byte[] code, char newOrEdit, byte[] data, String dnm, String localDefnsDir, String defnsDir)
  {
    try
    {
      stmt = con.createStatement();

      // if saving new rec
      //   change: Company.CompanyCode="abc"\001Company.Name="def"\001... format into: 'abc','def',... (including e.g., {d '...'})
      //   separator == '\000'
      //   change: abc\0def\0... format into: 'abc','def',...
      // else if editing existing rec
      //   separator == '\000'
      //   change: abc\0def\0... format into: CompanyCode='abc',Name='def',...
      
      String q;
      String opStr="";
      boolean first = true;
      String fieldTypes = getFieldTypesPayment();
      int x=0, count=0;

      if(newOrEdit == 'N')
      {
        // change: abc\0def\0... format into: 'abc','def',...

        char thisFieldType;
        int numFields = fieldTypes.length();
        while(count < numFields)
        {
          if(! first)
            opStr += ",";
          else first = false;
            
          thisFieldType = fieldTypes.charAt(count++);
          if(thisFieldType == 'S')
          {    
            opStr += "NULL";
            while(data[x] != '\000')
              ++x;              
          }
          else
          {
            if(thisFieldType == 'D')
              opStr += "{d ";
            else
            if(thisFieldType == 'T')
              opStr += "{t ";

            opStr += "'";
            while(data[x] != '\000')// && data[x] != '"')
            {  
              if(data[x] == '\'')
                opStr += "''";
              else
              if(data[x] == '"')
                opStr += "''''";
              else opStr += (char)data[x];
              ++x;
            }
            opStr += "'";

            if(thisFieldType == 'D' || thisFieldType == 'T')
              opStr += "}";
          }
           
          ++x;
        }
      
        q = "INSERT INTO payment (" + getFieldNamesPayment() + ") VALUES (" + opStr + ")";
      }
      else // newOrEdit == 'E'
      {
        String thisFieldName;
        char thisFieldType;

        // change: abc\0def\0... format into: CompanyCode='abc',Name='def',...

        String fieldNames = getFieldNamesPayment();
        int len = fieldNames.length();
        int y=0;
            
        // cycle through each entry in fieldNames
            
        while(x < len)
        {
          if(! first)
            opStr += ",";
          else first = false;

          thisFieldName="";
          while(x < len && fieldNames.charAt(x) != ',')
            thisFieldName += fieldNames.charAt(x++);
           ++x;
          thisFieldType = fieldTypes.charAt(count++);
          opStr += thisFieldName;
            
          opStr += "=";

          // now pickup the corresponding entry in the input buf
          if(thisFieldType == 'S')
          {    
            opStr += "NULL";
            while(data[y] != '\000')
              ++y;             
            ++y;
          }
          else
          {
            if(thisFieldType == 'D')
              opStr += "{d ";
            else
            if(thisFieldType == 'T')
              opStr += "{t ";

            opStr += "'";
            while(data[y] != '\000')
            {
              if(data[y] == '\'')
                opStr += "''"; 
              else
              if(data[y] == '"')
                opStr += "''''"; 
              else opStr += (char)data[y];
              ++y;
            }
            ++y;
              
            opStr += "'";

            if(thisFieldType == 'D' || thisFieldType == 'T')
              opStr += "}";
          }
            
          while(x < len && fieldNames.charAt(x) == ' ')
            ++x;
        }  

        q = "UPDATE payment SET " + opStr + " WHERE PaymentCode = '" + generalUtils.stringFromBytes(code, 0L) + "'";
      }

      stmt.executeUpdate(q);

      stmt.close();
    }
    catch(Exception e) 
    { 
      System.out.println(e);
      try
      {
        if(stmt != null) stmt.close();
      }
      catch(Exception e2) { }        
      return 'F'; 
    }
    
    return ' ';    
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public char putRecLine(Connection con, Statement stmt, ResultSet rs, byte[] code, byte[] line, char newOrEdit, byte[] data, String dnm, String localDefnsDir,
                         String defnsDir)
  {
    if(code[0] == '\000') // just-in-case
      return 'F';

    try
    {
      stmt = con.createStatement();

      // if saving new rec
      //   change: Company.CompanyCode="abc"\001Company.Name="def"\001... format into: 'abc','def',... (including e.g., {d '...'})
      //   separator == '\000'
      //   change: abc\0def\0... format into: 'abc','def',...
      // else if editing existing rec
      //   separator == '\000'
      //   change: abc\0def\0... format into: CompanyCode='abc',Name='def',...
      
      String q;
      String opStr="";
      boolean first = true;
      String fieldTypes = getFieldTypesPaymentL();
      int x=0, count=0;

      if(newOrEdit == 'N')
      {
        // change: abc\0def\0... format into: 'abc','def',...

        char thisFieldType;
        int numFields = fieldTypes.length();
        while(count < numFields)
        {
          if(! first)
            opStr += ",";
          else first = false;
            
          thisFieldType = fieldTypes.charAt(count++);
          if(thisFieldType == 'S')
          {    
            opStr += "NULL";
            while(data[x] != '\000')
              ++x;              
          }
          else
          {
            if(thisFieldType == 'D')
              opStr += "{d ";
            else
            if(thisFieldType == 'T')
              opStr += "{t ";

            opStr += "'";
            while(data[x] != '\000')// && data[x] != '"')
            {  
              if(data[x] == '\'')
                opStr += "''";
              else
              if(data[x] == '"')
                opStr += "''''";
              else opStr += (char)data[x];
              ++x;
            }
            opStr += "'";

            if(thisFieldType == 'D' || thisFieldType == 'T')
              opStr += "}";
          }
           
          ++x;
        }
      
        q = "INSERT INTO paymentl (" + getFieldNamesPaymentL() + ") VALUES (" + opStr + ")";
      }
      else // newOrEdit == 'E'
      {
        String thisFieldName;
        char thisFieldType;

        // change: abc\0def\0... format into: CompanyCode='abc',Name='def',...

        String fieldNames = getFieldNamesPaymentL();
        int len = fieldNames.length();
        int y=0;
            
        // cycle through each entry in fieldNames
            
        while(x < len)
        {
          if(! first)
            opStr += ",";
          else first = false;

          thisFieldName="";
          while(x < len && fieldNames.charAt(x) != ',')
            thisFieldName += fieldNames.charAt(x++);
           ++x;
          thisFieldType = fieldTypes.charAt(count++);
          opStr += thisFieldName;
            
          opStr += "=";

          // now pickup the corresponding entry in the input buf
          if(thisFieldType == 'S')
          {    
            opStr += "NULL";
            while(data[y] != '\000')
              ++y;             
            ++y;
          }
          else
          {
            if(thisFieldType == 'D')
              opStr += "{d ";
            else
            if(thisFieldType == 'T')
              opStr += "{t ";

            opStr += "'";
            while(data[y] != '\000')
            {
              if(data[y] == '\'')
                opStr += "''"; 
              else
              if(data[y] == '"')
                opStr += "''''"; 
              else opStr += (char)data[y];
              ++y;
            }
            ++y;
              
            opStr += "'";

            if(thisFieldType == 'D' || thisFieldType == 'T')
              opStr += "}";
          }
            
          while(x < len && fieldNames.charAt(x) == ' ')
            ++x;
        }  

        q = "UPDATE paymentl SET " + opStr + " WHERE PaymentCode = '" + generalUtils.stringFromBytes(code, 0L) + "' AND Line = '" 
          + generalUtils.stringFromBytes(line, 0L) + "'";
      }

      stmt.executeUpdate(q);

      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    { 
      try
      {
        System.out.println(e);
        if(stmt != null) stmt.close();
      }
      catch(Exception e2) { }      
      return 'F';
    }
    
    return ' ';  
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public boolean deleteLine(Connection con, Statement stmt, ResultSet rs, byte[] code, byte[] line, String dnm, String localDefnsDir, String defnsDir, int[] bytesOut)
  {
    try
    {
      if(code[0] == '\000') // just-in-case
        return false;

      if(line[0] == '\000')
        return false;

      // need to determine the 'entry' field
      byte[] data = new byte[2000];

      if(getLine(con, stmt, rs, code, line, '\000', data, dnm, localDefnsDir, defnsDir, bytesOut) == -1) // just-in-case
        return false;

      byte[] entry = new byte[7];
      generalUtils.dfs(data, (short)16, entry);

      stmt = con.createStatement();
    
      stmt.executeUpdate("DELETE FROM paymentl WHERE PaymentCode = '" + generalUtils.stringFromBytes(code, 0L) + "' AND Line = '"
                         + generalUtils.stringFromBytes(line, 0L) + "'");

      stmt.close();

      // delete multiple lines

      stmt = con.createStatement();
      stmt.executeUpdate("DELETE FROM paymentll WHERE PaymentCode = '" + generalUtils.stringFromBytes(code, 0L) + "' AND Entry = '"
                         + generalUtils.stringFromBytes(line, 0L) + "'");
   
      stmt.close();
    }
    catch(Exception e)
    {
      try
      {
        if(stmt != null) stmt.close();
      }
      catch(Exception e2) { }
      return false;
    }
    
    return true;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public byte[] getLines(Connection con, Statement stmt, ResultSet rs, byte[] code, char separator, byte[] linesData, int[] listLen, int[] linesCount, String dnm, String localDefnsDir,
                         String defnsDir) throws Exception
  {
    if(code[0] == '\000') // just-in-case
      return linesData;

    linesCount[0] = 0;

    generalUtils.toUpper(code, 0);

    byte[] data = new byte[5000];
    String s;
    int x, len;
    String fieldTypes = getFieldTypesPaymentL();
    String fieldNames = getFieldNamesPaymentL();    
    byte[] newItem = new byte[1000];
    String thisFieldName;
    char thisFieldType;
    int count;
    int lenFieldNames = fieldNames.length();
    int lenFieldTypes = fieldTypes.length();
    data[0] = '\000';
        
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT * FROM paymentl WHERE PaymentCode = '" + generalUtils.stringFromBytes(code, 0L) + "'");
    ResultSetMetaData rsmd = rs.getMetaData();
    while(rs.next())
    {
      if(separator == '\000')
      {
        s="";
        for(x=1;x<=lenFieldTypes;++x)
          s += getValue(x, fieldTypes.charAt(x-1), rs, rsmd) + "\002";

        ++linesCount[0];
        s = generalUtils.intToStr(linesCount[0]) + "\002" + s;
        //len = generalUtils.replaceOnesWithTwos(s, newItem);
        len = generalUtils.strToBytes(newItem, s);
        newItem[len] = '\001';
        newItem[len + 1] = '\000';
        linesData = generalUtils.appendToList(true, newItem, linesData, listLen);
      } 
      else // separator == '\001'
      {
        s="";
        x=count=0;
        while(x < lenFieldNames)
        {
          thisFieldName="";
          while(x < lenFieldNames && fieldNames.charAt(x) != ',')
            thisFieldName += fieldNames.charAt(x++);
          thisFieldType = fieldTypes.charAt(count++);
          s += "paymentl." + thisFieldName + "=" + getValue(count, thisFieldType, rs, rsmd) + "\001";
          ++x;
          while(x < lenFieldNames && fieldNames.charAt(x) == ' ')
            ++x;
        }

        ++linesCount[0];
        s = generalUtils.intToStr(linesCount[0]) + "\002" + s;
        len = generalUtils.replaceOnesWithTwos(s, newItem);
        newItem[len] = '\001';
        newItem[len + 1] = '\000';
        linesData = generalUtils.appendToList(true, newItem, linesData, listLen);
      }      
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return linesData; 
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  // if separator is '\001' then buf is: company.code=acme\001company.name=acme ltd\001
  // else if separator is '\000' (eg) then buf is acme\0acme ltd\0
  private int getLine(Connection con, Statement stmt, ResultSet rs, byte[] code, byte[] line, char separator, byte[] data, String dnm, String localDefnsDir, String defnsDir,
                      int[] bytesOut) throws Exception
  {
    if(code[0] == '\000') // just-in-case
      return -1;

    if(line[0] == '\000')
      return -1;

    generalUtils.toUpper(code, 0);

    int upto;
    String fieldTypes = getFieldTypesPaymentL();
    String fieldNames = getFieldNamesPaymentL();
    String thisFieldName;
    char thisFieldType;
    int x, count, len = fieldNames.length(), numFields = fieldTypes.length();
    data[0] = '\000';
    
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT * FROM paymentl WHERE PaymentCode = '" + generalUtils.stringFromBytes(code, 0L)
                                   + "' AND Line = '" + generalUtils.stringFromBytes(line, 0L) + "'");
    ResultSetMetaData rsmd = rs.getMetaData();
    while(rs.next())
    {
      if(separator == '\000')
      {         
        upto=0;
        for(x=0;x<numFields;++x)
        {
          upto = generalUtils.stringIntoBytes(getValue((x + 1), fieldTypes.charAt(x), rs, rsmd), 0, data, upto);
          data[upto++] = '\000';
        }
      }
      else // separator == '\001'
      {
        x=count=0;
        while(x < len)
        {
          thisFieldName="";
          while(x < len && fieldNames.charAt(x) != ',')
            thisFieldName += fieldNames.charAt(x++);
          thisFieldType = fieldTypes.charAt(count++);
          scoutln(data, bytesOut, "paymentl." + thisFieldName + "=" + getValue(count, thisFieldType, rs, rsmd) + "\001");
          ++x;
          while(x < len && fieldNames.charAt(x) == ' ')
            ++x;
        }
      }
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return 0;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void prependDisplayOnly(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, String code, String date, String unm, String sid,
                                  String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, byte[] b,
                                  int[] bytesOut) throws Exception
  {
    b[0]= '\000';
    scoutln(b, bytesOut, "<html><head><title>" + code + "</title>");

    String cssDirectory = directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir);
    String cssDocumentDirectory = directoryUtils.getCssDocumentDirectory(con, stmt, rs, dnm);
    scoutln(b, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + cssDirectory + "general.css\">\n");
    scoutln(b, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + cssDocumentDirectory + "document.css\">\n");

    outputPageFrame(con, stmt, rs, b, req, ' ',  ' ', "", "_4137", date, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void sortFields(byte[] ipBuf, byte[] opBuf, String which) throws Exception
  {
    String thisFieldName;
    String fieldNames;
    int x=0, y;
    byte[] thisFieldNameB = new byte[50];
    byte[] value          = new byte[1000];
    
    if(which.equals("payment"))
      fieldNames = getFieldNamesPayment();
    else // if(which.equals("paymentl"))
      fieldNames = getFieldNamesPaymentL();
    int len = fieldNames.length();
    
    int recDataLen = generalUtils.lengthBytes(ipBuf, 0);
    
    opBuf[0] = '\000';

    while(x < len)
    {
      y=0;
      thisFieldName="";
      while(x < len && fieldNames.charAt(x) != ',')
      {
        thisFieldNameB[y++] = (byte)fieldNames.charAt(x);  
        thisFieldName      += fieldNames.charAt(x++);
      }
      thisFieldNameB[y] = (byte)'\000';        
      ++x;
            
      while(x < len && fieldNames.charAt(x) == ' ')
        ++x;

      generalUtils.catAsBytes(which + "." + thisFieldName + "=", 0, opBuf, false);

      if(searchDataString(ipBuf, recDataLen, which, thisFieldNameB, value) != -1) // entry exists
        generalUtils.catAsBytes(generalUtils.stringFromBytes(value, 0L), 0, opBuf, false);

      generalUtils.catAsBytes("\001", 0, opBuf, false);
    }
    
    generalUtils.bytesToBytes(ipBuf, 0, opBuf, 0); // ipBuf now also in correct order
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  // uses layout 253d.htm
  public boolean getRecToHTMLDisplayOnly(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, byte[] code, String unm,
                                         String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir,
                                         int[] bytesOut) throws Exception
  {
    String imagesDir = directoryUtils.getSupportDirs('I');

    byte[][] buf1      = new byte[1][5000];
    byte[][] buf2      = new byte[1][5000];
    char[] source = new char[1];
    source[0] = screenLayout.resetBuffer(buf1, buf2);
    short numFields;
    int[] size1 = new int[1];  size1[0] = 5000;
    int[] size2 = new int[1];  size2[0] = 5000;
    byte[]  fieldNames  = new byte[2000]; // plenty
    byte[]  fieldTypes  = new byte[300];
    short[] fieldSizes = new short[200];
    
    byte[] data = new byte[5000];
    byte[] b    = new byte[300];

    byte[] prependCode = new byte[15000];

    byte[] javaScriptCode = new byte[1000];
    javaScript(con, stmt, rs, req, generalUtils.stringFromBytes(code, 0L), unm, sid, uty, men,den, dnm, bnm, javaScriptCode, localDefnsDir, defnsDir, bytesOut);

    byte[] ddlData = new byte[1];
    int[] ddlDataLen  = new int[1];

    ddlDataLen[0] = 1;
    int[] ddlDataUpto = new int[1];
    ddlDataUpto[0] = 0;

    if(getRecGivenCode(con, stmt, rs, code, '\001', data, dnm, localDefnsDir, defnsDir, bytesOut) == -1) // just-in-case
    {
      messagePage.msgScreen(false, out, req, 6, unm, sid, uty, men, den, dnm, bnm, "Payment", imagesDir, localDefnsDir,
                         defnsDir, bytesOut);
      return false;
    }

    short[] fieldSizesPayment = new short[70]; // plenty
    getFieldSizesPayment(fieldSizesPayment);
    short[] fieldSizesPaymentL = new short[30]; // plenty
    getFieldSizesPaymentL(fieldSizesPaymentL);

    numFields = screenLayout.layoutToBuffer(buf1, buf2, size1, fieldNames, fieldTypes, fieldSizes, localDefnsDir, defnsDir, "253d.htm", 2, getFieldNamesPayment(), fieldSizesPayment,
                         getFieldNamesPaymentL(), fieldSizesPaymentL, null, null);

    int recDataLen = generalUtils.lengthBytes(data, 0);

    String fieldNamesPayment = getFieldNamesPayment();
    byte[] value     = new byte[1000]; // plenty - to cover notes
    byte[] fieldName = new byte[50];
    int x=0, y, fieldCount=0;
    int len = fieldNamesPayment.length();
    while(x < len)
    {
      y=0;
      while(x < len && fieldNamesPayment.charAt(x) != ',')
        fieldName[y++] = (byte)fieldNamesPayment.charAt(x++);
      fieldName[y] = '\000';
      ++x;
      
      while(x < len && fieldNamesPayment.charAt(x) == ' ')
        ++x;

      if(searchDataString(data, recDataLen, "payment", fieldName, value) != -1) // entry exists
      {
        generalUtils.repAlphaGivenSeparator('\001', data, 3000, (short)fieldCount, value);
      }
      
      ++fieldCount;
    }

    generalUtils.dfsGivenSeparator(true, '\001', data, (short)34, b);
    generalUtils.decodeTime(3, generalUtils.stringFromBytes(b, 0L), ":", b);
    generalUtils.repAlphaGivenSeparator('\001', data, 3000, (short)34, b);

    generalUtils.bytesDPsGivenSeparator(true, '\001', '2',  data, 3000, 17);
    generalUtils.bytesDPsGivenSeparator(true, '\001', '2',  data, 3000, 18);
    generalUtils.bytesDPsGivenSeparator(true, '\001', '2',  data, 3000, 23); // groupDiscount

    byte[] date = new byte[20]; // lock
    generalUtils.dfsGivenSeparator(true, '\001', data, (short)16, date);

    byte[] javaScriptCallCode = new byte[1000]; // lock
    javaScriptCall(con, stmt, rs, javaScriptCallCode, req, generalUtils.stringFromBytes(date, 0L), unm, uty, dnm, localDefnsDir, defnsDir, bytesOut);

    prependDisplayOnly(con, stmt, rs, req, generalUtils.stringFromBytes(code, 0L), generalUtils.stringFromBytes(date, 0L), unm, sid, uty, men, den, dnm, bnm,
                       localDefnsDir, defnsDir, prependCode, bytesOut);
    screenLayout.bufferToBuffer(buf1, buf2, size1, size2, source, numFields, fieldNames, fieldTypes, fieldSizes, 'D', 'H', data, 3000, ddlData, ddlDataUpto[0],
                         javaScriptCode, prependCode);

    byte[] linesData = new byte[2000];
    int[] listLen = new int[1];  listLen[0] = 2000;
    int[] linesCount = new int[1];
    linesData = getLines(con, stmt, rs, code, '\001', linesData, listLen, linesCount, dnm, localDefnsDir, defnsDir);
    for(x=0;x<linesCount[0];++x)
    {
      if(generalUtils.getListEntryByNum(x, linesData, data)) // just-in-case
      {
        generalUtils.replaceTwosWithOnes(data);
        
        generalUtils.bytesDPsGivenSeparator(true, '\001', '2', data, 2000, 4);  // invoiceAmount
        generalUtils.bytesDPsGivenSeparator(true, '\001', '2', data, 2000, 6);  // amountPaid
        generalUtils.bytesDPsGivenSeparator(true, '\001', '2', data, 2000, 11); // originalRate
        generalUtils.bytesDPsGivenSeparator(true, '\001', '2', data, 2000, 13); // gstComponent
        generalUtils.bytesDPsGivenSeparator(true, '\001', '2', data, 2000, 18); // baseAmountPaid
        generalUtils.bytesDPsGivenSeparator(true, '\001', '2', data, 2000, 19); // baseGSTComponent
        
        generalUtils.dfsGivenSeparator(true, '\001', data, (short)4, b); // invoiceDate, origin-0 or 1 ???
        generalUtils.convertFromYYYYMMDD(b); 
        generalUtils.repAlphaGivenSeparator('\001', data, 2000, (short)4, b);
        
        screenLayout.bufferToBuffer(buf1, buf2, size1, size2, source, numFields, fieldNames, fieldTypes, fieldSizes, 'D', 'L', data, 2000, ddlData, ddlDataUpto[0], javaScriptCallCode, null);
      }
    }

    append(con, stmt, rs, buf1, buf2, source, size1, size2, unm, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    screenLayout.bufferToOut(buf1, buf2, source, out);

    return true;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void outputPageFrame(Connection con, Statement stmt, ResultSet rs, byte[] b, HttpServletRequest req, char newOrEdit, char cad, String bodyStr,
                               String callingServlet, String date, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                               String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    int[] hmenuCount = new int[1];

    String subMenuText = dashboardUtils.buildSubMenuText(con, stmt, rs, req, "5049", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount);

    subMenuText += "<table id='title' width=100%><tr><td colspan=2 nowrap><p>Payments" + directoryUtils.buildHelp(5049) + "</td></tr></table>";

    subMenuText += buildSubMenuText(con, stmt, rs, req, cad, newOrEdit, callingServlet, date, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount);

    scoutln(b, bytesOut, authenticationUtils.buildScreen(con, stmt, rs, req, true, unm, sid, uty, men, den, dnm, bnm, callingServlet, subMenuText, hmenuCount[0], bodyStr, "", localDefnsDir, defnsDir));

    scoutln(b, bytesOut, "<table border=0><tr><td valign=top><div id='second' style=\"{margin-top:0px;}\"></div></td>");
    scoutln(b, bytesOut, "<td valign=top><div id='third' style=\"{margin-top:0px;margin-left:0px;}\"></div></td></tr></table>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private String buildSubMenuText(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, char cad, char newOrEdit, String callingServlet,
                                  String date, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir,
                                  String defnsDir, int[] hmenuCount) throws Exception
  {
    String s = "<div id='submenu'>";

    ++hmenuCount[0];

    if(callingServlet.equals("PaymentPage"))
      s += drawOptions5049(con, stmt, rs, req, hmenuCount, date, unm, uty, dnm, localDefnsDir, defnsDir);
    else
    if(callingServlet.equals("PaymentHeaderEdit"))
      s += drawOptions5051(hmenuCount, cad, unm, sid, uty, men, den, dnm, bnm);
    else 
    if(callingServlet.equals("PaymentLine"))
      s += drawOptions5053(hmenuCount, newOrEdit);

    s += "</div>";
    
    --hmenuCount[0];
    
    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private String drawOptions5049(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, int[] hmenuCount, String date, String unm, String uty,
                                 String dnm, String localDefnsDir, String defnsDir) throws Exception
  {      
    String s = "";
    
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 5051, unm, uty, dnm, localDefnsDir, defnsDir) && serverUtils.passLockCheck(con, stmt, rs, "payment", date, unm))
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\"><a href=\"javascript:fetch()\">Edit Details</a></dt></dl>\n";
   
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 5053, unm, uty, dnm, localDefnsDir, defnsDir) && serverUtils.passLockCheck(con, stmt, rs, "payment", date, unm))
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\"><a href=\"javascript:add()\">Add New Line</a></dt></dl>\n";
  
    boolean PaymentAdvicePrint = authenticationUtils.verifyAccess(con, stmt, rs, req, 5056, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean _4192 = authenticationUtils.verifyAccess(con, stmt, rs, req, 4192, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean MailExternalUserCreate = authenticationUtils.verifyAccess(con, stmt, rs, req, 8016, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean PrintToPDFUtils = authenticationUtils.verifyAccess(con, stmt, rs, req, 11200, unm, uty, dnm, localDefnsDir, defnsDir);
    if(PaymentAdvicePrint || _4192 || MailExternalUserCreate || PrintToPDFUtils)
    {
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0] + "');\">Send</dt><dd id='hmenu" + hmenuCount[0]++ + "'><ul>";
      if(PaymentAdvicePrint)
        s += "<li><a href=\"javascript:print()\">Print</a></li>\n";
      if(_4192)
        s += "<li><a href=\"javascript:fax()\">Fax</a></li>\n";
      if(MailExternalUserCreate)
        s += "<li><a href=\"javascript:mail()\">Mail</a></li>";
      if(PrintToPDFUtils)
        s += "<li><a href=\"javascript:pdf()\">PDF</a></li>";
    
      s += "</ul></dd></dl>";
    }
    
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 11800, unm, uty, dnm, localDefnsDir, defnsDir))
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\"><a href=\"javascript:trail()\">Trail</a></dt></dl>\n";
    
    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private String drawOptions5051(int[] hmenuCount, char cad, String unm, String sid, String uty, String men, String mainDNM, String dnm, String bnm)
                                 throws Exception
  {      
    String s = "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\"><a href=\"javascript:save(' ')\">Save Changes</a></dt></dl>\n";

    if(cad != 'C')
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\"><a href=\"javascript:save('X')\">Cancel Changes</a></dt></dl>\n";
  
    s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\"><script language='Javascript'>";
    s += "var alreadyOnce=false;function select(){main.style.visibility='hidden';second.style.visibility='visible';"
      + "submenu.style.visibility='hidden';if(!alreadyOnce){select5069('A');alreadyOnce=true;}}\n";

    s += "var req2;";
    s += "function initRequest2(url)";
    s += "{if(window.XMLHttpRequest){req2=new XMLHttpRequest();}else if(window.ActiveXObject){req2=new ActiveXObject(\"Microsoft.XMLHTTP\");}}";

    s += "function select5069(searchChar)";
    s += "{var url = \"http://" + men + "/central/servlet/SupplierSelect?unm=\" + escape('" + unm + "') + \"&sid=\" + escape('" + sid + "') + \"&uty=\" + escape('" + uty
      + "') + \"&men=\" + escape('" + men + "') + \"&den=\" + escape('" + mainDNM + "') + \"&bnm=\" + escape('" + bnm
      + "') + \"&p1=\"+searchChar+\"&dnm=\" + escape('" + dnm + "');\n";
    s += "initRequest2(url);\n";
    
    s += "req2.onreadystatechange=processRequest2;";
    s += "req2.open(\"GET\",url,true);";
    s += "req2.send(null);}\n";
    
    s += "function processRequest2()";
    s += "{if(req2.readyState==4)";
    s += "{if(req2.status == 200)";
    s += "{var res=req2.responseText;";
    s += "if(res.length > 0)";
    s += "{document.getElementById('second').innerHTML=res;";
    s += "}}}}\n";
      
    s += "</script><a href=\"javascript:select()\">Select Supplier</a></dt></dl>\n";
    
    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private String drawOptions5053(int[] hmenuCount, char newOrEdit) throws Exception
  {      
    String s = "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\"><a href=\"javascript:select()\">Select Item</a></dt></dl>\n";

    if(newOrEdit == 'N')
    {
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\"><a href=\"javascript:save(' ')\">Save New Line</a></dt></dl>\n";

      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\"><a href=\"javascript:save('S')\">Save & New</a></dt></dl>\n";

      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\"><a href=\"javascript:save('X')\">Cancel Changes</a></dt></dl>\n";
    }
    else
    {
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\"><a href=\"javascript:save(' ')\">Save Line</a></dt></dl>\n";

      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\"><a href=\"javascript:save('X')\">Cancel Changes</a></dt></dl>\n";

      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\"><a href=\"javascript:save('D')\">Delete Line</a></dt></dl>\n";
    } 
    
    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(byte[] b, int bytesOut[], String str) throws Exception
  {      
    generalUtils.catAsBytes(str, 0, b, false);
    bytesOut[0] += (str.length() + 2);    
  }

}
