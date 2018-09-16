// =======================================================================================================================================================================================================
// System: ZaraStar Document: Quote Record Access
// Module: quotation.java
// Author: C.K.Harvey
// Copyright (c) 2001-08 Christopher Harvey. All Rights Reserved.
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

public class Quotation
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
  Customer customer = new Customer();
  LibraryUtils libraryUtils = new LibraryUtils();
  DashboardUtils dashboardUtils = new DashboardUtils();
  Inventory inventory = new Inventory();
  InventoryAdjustment inventoryAdjustment = new InventoryAdjustment();

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getTableCreateStringQuote() throws Exception
  {  
    return "quote ( QuoteCode char(20) not null,  QuoteDate date not null,       CompanyCode char(20) not null, "
                 + "CompanyName char(60),         Address1 char(40),             Address2 char(40), "
                 + "Address3 char(40),            Address4 char(40),             Address5 char(40), "
                 + "PostCode char(20),            FAO char(40),                  Notes char(250), "
                 + "Status char(1),               unused4 date,                  unused5 date, "
                 + "Misc1 char(80),               Misc2 char(80),                GSTTotal decimal(19,8), "
                 + "TotalTotal decimal(19,8),     SalesPerson char(40),          LikelihoodRating char(40), "
                 + "QuoteStatus char(40),         WonOrLost char(1),             Reason1 char(1), "
                 + "Reason2 char(1),              Reason3 char(1),               Reason4 char(1), "
                 + "Reason5 char(1),              Reason6 char(1),               Reason7 char(1), "
                 + "Reason8 char(1),              Reason9 char(1),               Reason10 char(1), " 
                 + "GroupDiscount decimal(19,8),  DateLastModified timestamp,    SignOn char(20), "
                 + "GroupDiscountType char(1),    RevisionOf char(20),           ProjectCode char(20), "
                 + "EnquiryCode char(40),         LocationCode char(20),         BuyerEMail char(60), "
                 + "Currency char(3),             Rate decimal(16,8),            BaseTotalTotal decimal(19,8), "
                 + "BaseGSTTotal decimal(19,8),   Terms char(20),                Validity char(40), "
                 + "Delivery char(60),            Packaging char(40),            Fax char(20), "
                 + "Country char(40),             DocumentStatus char(1),        Manufacturer char(40), "
                 + "RemarkType char(20),          DeliveryLeadTime char(20),     Phone char(20), "
            
                 + "ToEngineering char(1),        ToEngineeringDate date,        ToEngineeringSignon char(20), "
                 + "ToProcurement char(1),        ToProcurementDate date,        ToProcurementSignon char(20), "
                 + "ToScheduling char(1),         ToSchedulingDate date,         ToSchedulingSignon char(20), "
                 + "ToManager char(1),            ToManagerDate date,            ToManagerSignon char(20), "
                 + "EngineeringApproved char(1),  EngineeringApprovedDate date,  EngineeringApprovedSignon char(20), "
                 + "ProcurementConfirmed char(1), ProcurementConfirmedDate date, ProcurementConfirmedSignon char(20), "
                 + "SchedulingConfirmed char(1),  SchedulingConfirmedDate date,  SchedulingConfirmedSignon char(20), "
                 + "ManagerApproved char(1),      ManagerApprovedDate date,      ManagerApprovedSignon char(20), "
                 + "QuoteSent char(1),            QuoteSentDate date,            QuoteSentSignon char(20), "
            
            + "unique(QuoteCode))";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStringsQuote(String[] s) throws Exception
  {
    s[0] = "quoteQuoteDateInx on quote (QuoteDate)";
    s[1] = "quoteCompanyCodeInx on quote (CompanyCode)";
    s[2] = "quoteProjectCodeInx on quote (ProjectCode)";
    
    return 3;
  }
      
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNamesQuote() throws Exception
  {  
    return "QuoteCode, QuoteDate, CompanyCode, CompanyName, Address1, Address2, Address3, Address4, Address5, PostCode, FAO, Notes, Status, unused4, "
         + "unused5, Misc1, Misc2, GSTTotal, TotalTotal, SalesPerson, LikelihoodRating, QuoteStatus, WonOrLost, Reason1, Reason2, Reason3, Reason4, "
         + "Reason5, Reason6, Reason7, Reason8, Reason9, Reason10, GroupDiscount, DateLastModified, SignOn, GroupDiscountType, RevisionOf, "
         + "ProjectCode, EnquiryCode, LocationCode, BuyerEMail, Currency, Rate, BaseTotalTotal, BaseGSTTotal, Terms, Validity, Delivery, Packaging, "
         + "Fax, Country, DocumentStatus, Manufacturer, RemarkType, DeliveryLeadTime, Phone, ToEngineering, ToEngineeringDate, ToEngineeringSignon, "
         + "ToProcurement, ToProcurementDate, ToProcurementSignon, ToScheduling, ToSchedulingDate, ToSchedulingSignon, ToManager, ToManagerDate, "
         + "ToManagerSignon, EngineeringApproved, EngineeringApprovedDate, EngineeringApprovedSignon, ProcurementConfirmed, "
         + "ProcurementConfirmedDate, ProcurementConfirmedSignon, SchedulingConfirmed, SchedulingConfirmedDate, SchedulingConfirmedSignon, "
         + "ManagerApproved, ManagerApprovedDate, ManagerApprovedSignon, QuoteSent, QuoteSentDate, QuoteSentSignon";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypesQuote() throws Exception
  {
    return "CDCCCCCCCCCCCDDCCFFCCCCCCCCCCCCCCFSCCCCCCCCFFFCCCCCCCCCCCCDCCDCCDCCDCCDCCDCCDCCDCCDC";
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizesQuote(short[] sizes) throws Exception
  {
    sizes[0]  = 20;  sizes[1]  = 0;   sizes[2]  = 20;  sizes[3]  = 60;  sizes[4] = 40;    sizes[5] = 40;    sizes[6] = 40;
    sizes[7]  = 40;  sizes[8]  = 40;  sizes[9]  = 20;  sizes[10] = 40;  sizes[11] = 250;  sizes[12] = 1;    sizes[13] = 0;
    sizes[14] = 0;   sizes[15] = 80;  sizes[16] = 80;  sizes[17] = 0;   sizes[18] = 0;    sizes[19] = 40;   sizes[20] = 40;
    sizes[21] = 40;  sizes[22] = 1;   sizes[23] = 1;   sizes[24] = 1;   sizes[25] = 1;    sizes[26] = 1;    sizes[27] = 1;
    sizes[28] = 1;   sizes[29] = 1;   sizes[30] = 1;   sizes[31] = 1;   sizes[32] = 1;    sizes[33] = 0;    sizes[34] = -1;
    sizes[35] = 20;  sizes[36] = 1;   sizes[37] = 20;  sizes[38] = 20;  sizes[39] = 40;   sizes[40] = 20;   sizes[41] = 60;
    sizes[42] = 3;   sizes[43] = 0;   sizes[44] = 0;   sizes[45] = 0;   sizes[46] = 20;   sizes[47] = 40;   sizes[48] = 60;
    sizes[49] = 40;  sizes[50] = 20;  sizes[51] = 40;  sizes[52] = 1;   sizes[53] = 40;   sizes[54] = 20;   sizes[55] = 20;
    sizes[56] = 20;
    sizes[57] = 1;   sizes[58] = 0;   sizes[59] = 20;
    sizes[60] = 1;   sizes[61] = 0;   sizes[62] = 20;
    sizes[63] = 1;   sizes[64] = 0;   sizes[65] = 20;
    sizes[66] = 1;   sizes[67] = 0;   sizes[68] = 20;
    sizes[69] = 1;   sizes[70] = 0;   sizes[71] = 20;
    sizes[72] = 1;   sizes[73] = 0;   sizes[74] = 20;
    sizes[75] = 1;   sizes[76] = 0;   sizes[77] = 20;
    sizes[78] = 1;   sizes[79] = 0;   sizes[80] = 20;
    sizes[81] = 1;   sizes[82] = 0;   sizes[83] = 20;
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public String getFieldStylesQuote() throws Exception
  {
    return "MMMOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO";
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public String getTableCreateStringQuoteL() throws Exception
  {  
    return "quotel ( QuoteCode char(20) not null, ItemCode char(20) not null,  Description char(80), "
                  + "UnitPrice decimal(17,8),     Quantity decimal(19,8),      Amount decimal(17,8), "
                  + "GSTRate char(20),            Discount decimal(17,8),      DateLastModified timestamp, "
                  + "Amount2 decimal(17,8),       DeliveryDate date,           Line integer not null, "
                  + "Entry char(6),               UoM char(20),                CustomerItemCode char(40), "
                  + "Remark char(80),             Store char(20),              SignOn char(20), "
                  + "CostPrice decimal(17,8),     Manufacturer char(30),       ManufacturerCode char(60), "
                  + "BOMCode char(20), "
                  + "unique(QuoteCode, Line))";
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStringsQuoteL(String[] s) throws Exception
  {
    s[0] = "quotelItemCodeInx on quotel(ItemCode)";
    
    return 1;
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNamesQuoteL() throws Exception
  {  
    return "QuoteCode, ItemCode, Description, UnitPrice, Quantity, Amount, GSTRate, Discount, DateLastModified, Amount2, "
         + "DeliveryDate, Line, Entry, UoM, CustomerItemCode, Remark, Store, SignOn, CostPrice, Manufacturer, ManufacturerCode, BOMCode";
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypesQuoteL() throws Exception
  {
    return "CCCFFFCFSFDICCCCCCFCCC";
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizesQuoteL(short[] sizes) throws Exception
  {
    sizes[0] = 20;   sizes[1] = 20;   sizes[2] = 80;   sizes[3] = 0;    sizes[4] = 0;   sizes[5] = 0;   sizes[6] = 20;
    sizes[7] = 0;    sizes[8] = -1;   sizes[9] = 0;    sizes[10] = 0;   sizes[11] = 0;  sizes[12] = 6;  sizes[13] = 20;
    sizes[14] = 40;  sizes[15] = 80;  sizes[16] = 20;  sizes[17] = 20;  sizes[18] = 0;  sizes[19] = 30; sizes[20] = 60; sizes[21] = 20;
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public String getFieldStylesQuoteL() throws Exception
  {
    return "OOOOOOOOOOOMOOOOOOOOOO";
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public String getTableCreateStringQuoteLL() throws Exception
  {  
    return "quotell ( QuoteCode char(20) not null,   Entry char(6) not null,   Line integer not null,   Text char(80), "
                    + "unique(QuoteCode, Entry, Line))";
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStringsQuoteLL(String[] s) throws Exception
  {
    s[0]="\000"; // remove compiler warning
    return 0;
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNamesQuoteLL() throws Exception
  {  
    return "QuoteCode, Entry, Line, Text";
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypesQuoteLL() throws Exception
  {
    return "CCIC";
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizesQuoteLL(short[] sizes) throws Exception
  {
    sizes[0] = 20;   sizes[1] = 6;   sizes[2] = 0;   sizes[3] = 80;
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public String getFieldStylesQuoteLL() throws Exception
  {
    return "MMMO";
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public String getTableCreateStringQuoteA() throws Exception
  {  
    return "quotea ( Code char(20) not null, LibraryDocCode integer not null, unique(Code, LibraryDocCode))";
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStringsQuoteA(String[] s) throws Exception
  {
    s[0]="\0"; // remove compiler warning
    return 0;
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNamesQuoteA() throws Exception
  {  
    return "Code, LibraryDocCode";
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypesQuoteA() throws Exception
  {
    return "CI";
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizesQuoteA(short[] sizes) throws Exception
  {
    sizes[0] = 20;   sizes[1] = 0;
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public String getFieldStylesQuoteA() throws Exception
  {
    return "MM";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getRecToHTML(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, boolean plain, char dispOrEdit, byte[] code, String unm, String sid, String uty, String men, String den, String dnm,
                              String bnm, String localDefnsDir, String defnsDir, char cad, String errStr, byte[] dataAlready, HttpServletRequest req, int[] bytesOut) throws Exception
  {
    String s = "";

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
    byte[] prependCode = new byte[50000]; // plenty esp. mfrsDDL
    byte[] b           = new byte[300];

    byte[] javaScriptCode = new byte[20000]; // plenty ... esp for mfrsDDL entries
    javaScript(con, stmt, rs, req, generalUtils.stringFromBytes(code, 0L), unm, sid, uty, men, den, dnm, bnm, javaScriptCode, localDefnsDir, defnsDir, bytesOut);

    byte[] ddlData = new byte[1500];
    int[] ddlDataLen  = new int[1];

    ddlDataLen[0] = 1500;
    int[] ddlDataUpto = new int[1];
    ddlDataUpto[0] = 0;

    if(cad == 'A' && code[0] == '\000')
    {
      s += messagePage.msgScreen(false, out, req, 6, unm, sid, uty, men, den, dnm, bnm, "Quotation", imagesDir, localDefnsDir, defnsDir, bytesOut);
    }

    if(dataAlready[0] != '\000') // coming with an err msg - passing-in data already entered but the data's fields may (will) not be
    {                            // in the correct order
      dispOrEdit = 'E';
      sortFields(dataAlready, headData, "quote");
    }
    else // get header data
    {
      if(cad == 'A')
      {
        if(getRecGivenCode(con, stmt, rs, code, '\001', headData, dnm, localDefnsDir, defnsDir, bytesOut) == -1)
        {
          s += messagePage.msgScreen(false, out, req, 6, unm, sid, uty, men, den, dnm, bnm, "Quotation", imagesDir, localDefnsDir, defnsDir, bytesOut);
        }
      }
    }
   
    String date;
    byte[] dateB = new byte[20]; // lock
    if(dataAlready[0] != '\000')
    {
      generalUtils.dfsGivenSeparator(true, '\001', headData, (short)1, dateB);
      date = generalUtils.convertDateToSQLFormat(generalUtils.stringFromBytes(dateB, 0L));
    }
    else
    if(cad == 'A')
    {
      generalUtils.dfsGivenSeparator(true, '\001', headData, (short)1, dateB);
      date = generalUtils.stringFromBytes(dateB, 0L);
    }
    else date = generalUtils.todaySQLFormat(localDefnsDir, defnsDir); 

    byte[] javaScriptCallCode = new byte[1000];
    if(plain)
    {
      javaScriptCallCode[0]= '\000';
      scoutln(javaScriptCallCode, bytesOut, "_.lineFile=dol.line\001_.permitted=n\001");
    }
    else javaScriptCall(con, stmt, rs, javaScriptCallCode, req, date, unm, uty, dnm, localDefnsDir, defnsDir, bytesOut);

    short[] fieldSizesQuote = new short[90]; // plenty
    getFieldSizesQuote(fieldSizesQuote);

    if(cad == 'A') // not a new one
    {
      short[] fieldSizesQuoteL = new short[30]; // plenty
      getFieldSizesQuoteL(fieldSizesQuoteL);

      double amt, amt2, totalAmtLines, totalGSTAmtLines, totalGSTAmt2Lines, totalAmt2Lines, groupDiscount, gstRate;
      totalAmt2Lines = totalAmtLines = totalGSTAmtLines = totalGSTAmt2Lines = totalAmt2Lines = groupDiscount = 0.0;
      char groupDiscountType=' ';

      if(dispOrEdit == 'D')
      {  
        numFields = screenLayout.layoutToBuffer(buf1, buf2, size1, fieldNames, fieldTypes, fieldSizes, localDefnsDir, defnsDir, "227.htm", 2, getFieldNamesQuote(), fieldSizesQuote, getFieldNamesQuoteL(), fieldSizesQuoteL, null, null);
      }
      else
      {
        numFields = screenLayout.layoutToBuffer(buf1, buf2, size1, fieldNames, fieldTypes, fieldSizes, localDefnsDir, defnsDir, "227a.htm", 1, getFieldNamesQuote(), fieldSizesQuote, null, null, null, null);
      }
              
      if(dispOrEdit == 'D')
        prepend(con, stmt, rs, plain, unm, sid, uty, men, den, dnm, bnm, generalUtils.stringFromBytes(code, 0L), date, prependCode, req, localDefnsDir, defnsDir, bytesOut);
      else prependEdit(con, stmt, rs, unm, sid, uty, men, den, dnm, bnm, errStr, "A", generalUtils.stringFromBytes(code, 0L), date, "227a.htm", localDefnsDir, defnsDir, prependCode, req, bytesOut);

      screenLayout.bufferToBuffer(buf1, buf2, size1, size2, source, numFields, fieldNames, fieldTypes, fieldSizes, dispOrEdit, 'X', data, 0, ddlData, ddlDataUpto[0], javaScriptCode, prependCode);

      if(dispOrEdit == 'D') // display head *and* lines
      {
        // need to pickup groupDiscount

        generalUtils.dfsGivenSeparator(true, '\001', headData, (short)33, b);
        groupDiscount = generalUtils.doubleFromChars(b);

        generalUtils.dfsGivenSeparator(true, '\001', headData, (short)36, b);
        groupDiscountType = (char)b[0];

        char dpOnQuantities = miscDefinitions.dpOnQuantities(con, stmt, rs, "5");
        char dpOnUnitPrice  = miscDefinitions.dpOnUnitPrice(con, stmt, rs, "4");

        byte[] multipleLinesData = new byte[1000];
        int[]  multipleListLen = new int[1]; multipleListLen[0] = 1000;
        int[]  multipleLinesCount = new int[1];
        byte[] llData = new byte[200];
        multipleLinesData = getMultipleLines(con, stmt, rs, code, multipleLinesData, multipleListLen, multipleLinesCount);

        boolean isAtLeastOneLineWithGST = false;
        byte[] line      = new byte[20];
        byte[] linesData = new byte[5000];
        int[]  listLen = new int[1];  listLen[0] = 5000;
        int[]  linesCount = new int[1];
        linesData = getLines(con, stmt, rs, code, '\001', linesData, listLen, linesCount, dnm, localDefnsDir, defnsDir);

        String[][] allGSTRateNames = new String[1][10];
        double[][] allGSTRates     = new double[1][10];
        int[] allGSTLens = new int[1];  allGSTLens[0] = 10;

        getGSTRates(con, stmt, rs, allGSTRateNames, allGSTRates, allGSTLens);

        // if groupDiscount is a fixed amount, then need to make one pass in order to turn group discount into a % so that can calc GST correctly
        int x;
        if(groupDiscountType != '%' && groupDiscount != 0.0)
        {
          for(x=0;x<linesCount[0];++x)
          {
            if(generalUtils.getListEntryByNum(x, linesData, data)) // just-in-case
            {
              generalUtils.replaceTwosWithOnes(data);

              // get line amts
              generalUtils.dfsGivenSeparator(true, '\001', data, (short)10, b); // amt2, origin-1
              totalAmt2Lines += generalUtils.doubleDPs(generalUtils.doubleFromChars(b), '2');

              generalUtils.dfsGivenSeparator(true, '\001', data, (short)7, b); // gstrate
              if(generalUtils.lengthBytes(b, 0) != 0) // rate specified
              {
                 if(getGSTRate(allGSTRateNames[0], allGSTRates[0], allGSTLens[0], b) != 0)
                  isAtLeastOneLineWithGST = true;
              }
            }
          }

          // now can convert to a %
          if(totalAmt2Lines == 0)
            totalAmt2Lines = 1; // just-in-case

          if(isAtLeastOneLineWithGST)
            groupDiscount = (groupDiscount * 100.00) / totalAmt2Lines;
        }

        // now that we are sure that groupDiscount is a %...

        // GST calc: total-up all of each rate, then total the totals, then round-up (if nec)
        double[] gstTotalsBase    = new double[10]; for(x=0;x<10;++x) gstTotalsBase[x]    = 0.0;
        double[] gstTotalsNonBase = new double[10]; for(x=0;x<10;++x) gstTotalsNonBase[x] = 0.0;
        double[] gstRates         = new double[10]; for(x=0;x<10;++x) gstRates[x]         = -1;
        short z;

        totalAmt2Lines = 0.0;
        for(x=0;x<linesCount[0];++x)
        {
          if(generalUtils.getListEntryByNum(x, linesData, data)) // just-in-case
          {
            generalUtils.replaceTwosWithOnes(data);

            // get line amts

            generalUtils.dfsGivenSeparator(true, '\001', data, (short)10, b); // origin-1
            amt2 = generalUtils.doubleFromChars(b);

            generalUtils.dfsGivenSeparator(true, '\001', data, (short)6, b);
            amt = generalUtils.doubleFromChars(b);

            // apply the group discount to the two amts
            if(groupDiscountType == '%' || isAtLeastOneLineWithGST)
            {
              amt2 -= generalUtils.doubleDPs((groupDiscount * amt2 / 100.0), '2');
              amt  -= generalUtils.doubleDPs((groupDiscount * amt  / 100.0), '2');
            }
            totalAmt2Lines    += generalUtils.doubleDPs(amt2, '2');
            totalAmtLines     += generalUtils.doubleDPs(amt, '2');

            generalUtils.dfsGivenSeparator(true, '\001', data, (short)7, b); // gstRate

            if(generalUtils.lengthBytes(b, 0) != 0) // rate specified
              gstRate = getGSTRate(allGSTRateNames[0], allGSTRates[0], allGSTLens[0], b);
            else gstRate = 0.0;
            
            z=0;
            while(z < 10 && gstRates[z] != gstRate && gstRates[z] != -1)
              ++z;

            if(z < 10) // just-in-case
            {
              if(gstRates[z] == -1) // rate not found
              {
                gstRates[z]         = gstRate;
                gstTotalsBase[z]    = (amt  * gstRate);
                gstTotalsNonBase[z] = (amt2 * gstRate);
              }
              else
              {
                gstTotalsBase[z]    += (amt  * gstRates[z]);
                gstTotalsNonBase[z] += (amt2 * gstRates[z]);
              }
            }

            generalUtils.bytesDPsGivenSeparator(true, '\001', dpOnQuantities, data, 5000, 5);  // quantity (origin-1)
            generalUtils.bytesDPsGivenSeparator(true, '\001', '2', data, 5000, 6);  // amount
            generalUtils.bytesDPsGivenSeparator(true, '\001', '2', data, 5000, 10); // amount2
            generalUtils.bytesDPsGivenSeparator(true, '\001', dpOnUnitPrice,  data, 5000, 4);  // unitprice
            generalUtils.bytesDPsGivenSeparator(true, '\001', dpOnUnitPrice,  data, 5000, 8);  // discount

            generalUtils.dfsGivenSeparator(true, '\001', data, (short)12, line); // origin-1

            for(int xx=0;xx<multipleLinesCount[0];++xx)
            {
              if(generalUtils.getListEntryByNum(xx, multipleLinesData, llData)) // just-in-case
              {
                generalUtils.replaceTwosWithOnes(llData);

                generalUtils.dfsGivenBinary1(false, llData, (short)2, b); // entry

                if(generalUtils.matchIgnoreCase(line, 0, b, 0))
                {
                  generalUtils.dfsGivenBinary1(false, llData, (short)4, b); // text
                  generalUtils.appendAlphaGivenBinary1(data, 5000, 3, b, "<br>");
                }
              }
            }

            screenLayout.bufferToBuffer(buf1, buf2, size1, size2, source, numFields, fieldNames, fieldTypes, fieldSizes, 'D', 'L', data, 5000, ddlData, ddlDataUpto[0], javaScriptCallCode, null);
          }
        }

        totalGSTAmtLines  = 0.0;
        totalGSTAmt2Lines = 0.0;
        z=0;
        while(z < 10 && gstRates[z] != -1) // just-in-case
        {
          totalGSTAmt2Lines += gstTotalsNonBase[z];
          totalGSTAmtLines  += gstTotalsBase[z++];
        }

        if(groupDiscountType != '%' && ! isAtLeastOneLineWithGST)
        {
          totalAmt2Lines -= groupDiscount;
          totalAmtLines  = totalAmt2Lines * generalUtils.dfsAsDoubleGivenSeparator(true, '\001', headData, (short)43);
        }

        totalGSTAmt2Lines = generalUtils.doubleDPs(totalGSTAmt2Lines, '2');
        totalGSTAmtLines  = generalUtils.doubleDPs(totalGSTAmtLines, '2');

        stmt = con.createStatement();
        stmt.executeUpdate("UPDATE quote SET TotalTotal = " + (totalAmt2Lines + totalGSTAmt2Lines) + ", GSTTotal = " + totalGSTAmt2Lines
                         + ", BaseTotalTotal = " + (totalAmtLines + totalGSTAmtLines) + ", BaseGSTTotal = " + totalGSTAmtLines
                         + " WHERE QuoteCode = '" + generalUtils.stringFromBytes(code, 0L) + "'");
        if(stmt != null) stmt.close();

        generalUtils.repDoubleGivenSeparator('2', '\001', headData, 5000, (short)17, totalGSTAmt2Lines);
        generalUtils.repDoubleGivenSeparator('2', '\001', headData, 5000, (short)18, (totalAmt2Lines + totalGSTAmt2Lines));

        scoutln(headData, bytesOut, "quote.subtotalbeforegst=" + generalUtils.doubleToStr(totalAmt2Lines) + "\001"); // subtotal on screen

        generalUtils.repDoubleGivenSeparator('2', '\001', headData, 5000, (short)44, (totalAmtLines + totalGSTAmtLines));
        generalUtils.repDoubleGivenSeparator('2', '\001', headData, 5000, (short)45, totalGSTAmtLines);

        generalUtils.bytesDPsGivenSeparator(true, '\001', '2', headData, 5000, 33); // group discount

        scoutln(headData, bytesOut, "quote.basecurrency=" + accountsUtils.getBaseCurrency(con, stmt, rs, dnm, localDefnsDir, defnsDir) + "\001");

        generalUtils.bytesDPsGivenSeparator(true, '\001', '2',  headData, 5000, 43); // rate
        scoutln(headData, bytesOut, "quote.rateNoTrailing=" + generalUtils.dfsAsStrGivenBinary1(true, headData, (short)43) + "\001");

        String attachments = getAttachments(con, stmt, stmt2, rs, rs2, generalUtils.stringFromBytes(code, 0L), unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir);
        if(attachments.length() == 0)
          attachments= "none";
        scoutln(headData, bytesOut, "quote.Attachments=" + attachments + "\001");
      }
      else // edit
      {
        ddlData = accountsUtils.getCurrencyNamesDDLData(con, stmt, rs, "quote.Currency", dnm, ddlData, ddlDataUpto, ddlDataLen, localDefnsDir, defnsDir);
        ddlData = documentUtils.getSalesPersonDDLData(con, stmt, rs, "quote.SalesPerson", ddlData, ddlDataUpto, ddlDataLen);
        ddlData = documentUtils.getLikelihoodDDLData(con, stmt, rs, "quote.LikelihoodRating", ddlData, ddlDataUpto, ddlDataLen);
        ddlData = documentUtils.getQuoteStateDDLData(con, stmt, rs, "quote.QuoteStatus", ddlData, ddlDataUpto, ddlDataLen);
        ddlData = documentUtils.getCountryDDLData(con, stmt, rs, "quote.Country", ddlData, ddlDataUpto, ddlDataLen);

        String[] reasons = new String[10];
        documentUtils.getQuotationReasons(con, stmt, rs, reasons);
        scoutln(headData, bytesOut, "quote.Reason1Text="  + reasons[0] + "\001");
        scoutln(headData, bytesOut, "quote.Reason2Text="  + reasons[1] + "\001");
        scoutln(headData, bytesOut, "quote.Reason3Text="  + reasons[2] + "\001");
        scoutln(headData, bytesOut, "quote.Reason4Text="  + reasons[3] + "\001");
        scoutln(headData, bytesOut, "quote.Reason5Text="  + reasons[4] + "\001");
        scoutln(headData, bytesOut, "quote.Reason6Text="  + reasons[5] + "\001");
        scoutln(headData, bytesOut, "quote.Reason7Text="  + reasons[6] + "\001");
        scoutln(headData, bytesOut, "quote.Reason8Text="  + reasons[7] + "\001");
        scoutln(headData, bytesOut, "quote.Reason9Text="  + reasons[8] + "\001");
        scoutln(headData, bytesOut, "quote.Reason10Text=" + reasons[9] + "\001");
 
        // format notes field
        generalUtils.replaceThreesWithNewlines(generalUtils.dfsAsStrGivenBinary1(true, headData, (short)11), b);
        generalUtils.repAlphaGivenSeparator('\001', headData, 5000, (short)11, b);
      }

      if(dataAlready[0] == '\000') // NOT coming with an err msg
      {
        // convert date
        generalUtils.dfsGivenSeparator(true, '\001', headData, (short)1, b);
        generalUtils.convertFromYYYYMMDD(b); 
        generalUtils.repAlphaGivenSeparator('\001', headData, 5000, (short)1, b);
      }

      generalUtils.bytesDPsGivenSeparator(true, '\001', '2',  headData, 5000, 33); // groupDiscount
      generalUtils.bytesDPsGivenSeparator(true, '\001', '2',  headData, 5000, 43); // rate

      screenLayout.bufferToBuffer(buf1, buf2, size1, size2, source, numFields, fieldNames, fieldTypes, fieldSizes, dispOrEdit, 'H', headData, 5000, ddlData, ddlDataUpto[0], null, null);

      append(con, stmt, rs, buf1, buf2, source, size1, size2, unm, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

      s += screenLayout.bufferToOut(buf1, buf2, source, out);
    }
    else // a new layout
    {
      numFields = screenLayout.layoutToBuffer(buf1, buf2, size1, fieldNames, fieldTypes, fieldSizes, localDefnsDir, defnsDir, "227a.htm", 1, getFieldNamesQuote(), fieldSizesQuote, null, null, null, null);

      if(dataAlready[0] != '\000') // coming with an err msg - passing-in data already entered but the data's fields may (will) not
      {                            // be in the correct order
        sortFields(dataAlready, data, "quote");
      }  
      else
      {
        screenLayout.buildNewDetails(fieldNames, numFields, "quote", data);

        documentUtils.getNextCode(con, stmt, rs, "quote", true, code);

        generalUtils.repAlphaUsingOnes(data, 5000, "QuoteCode", generalUtils.stringFromBytes(code, 0L));
        generalUtils.repAlphaUsingOnes(data, 5000, "DocumentStatus", "L");
        generalUtils.repAlphaUsingOnes(data, 5000, "Currency", accountsUtils.getBaseCurrency(con, stmt, rs, dnm, localDefnsDir, defnsDir));
        generalUtils.repAlphaUsingOnes(data, 5000, "QuoteStatus", documentUtils.getDefaultQuoteStatus(con, stmt, rs));

        generalUtils.repAlphaUsingOnes(data, 5000, "SalesPerson", unm);

        generalUtils.repAlphaUsingOnes(data, 5000, "QuoteDate", generalUtils.today(localDefnsDir, defnsDir));        
      }

      ddlData = accountsUtils.getCurrencyNamesDDLData(con, stmt, rs, "quote.Currency", dnm, ddlData, ddlDataUpto, ddlDataLen, localDefnsDir, defnsDir);
      ddlData = documentUtils.getSalesPersonDDLData(con, stmt, rs, "quote.SalesPerson", ddlData, ddlDataUpto, ddlDataLen);
      ddlData = documentUtils.getLikelihoodDDLData(con, stmt, rs, "quote.LikelihoodRating", ddlData, ddlDataUpto, ddlDataLen);
      ddlData = documentUtils.getQuoteStateDDLData(con, stmt, rs, "quote.QuoteStatus", ddlData, ddlDataUpto, ddlDataLen);
      ddlData = documentUtils.getCountryDDLData(con, stmt, rs, "quote.Country", ddlData, ddlDataUpto, ddlDataLen);

      String[] reasons = new String[10];
      documentUtils.getQuotationReasons(con, stmt, rs, reasons);
      scoutln(data, bytesOut, "quote.Reason1Text="  + reasons[0] + "\001");
      scoutln(data, bytesOut, "quote.Reason2Text="  + reasons[1] + "\001");
      scoutln(data, bytesOut, "quote.Reason3Text="  + reasons[2] + "\001");
      scoutln(data, bytesOut, "quote.Reason4Text="  + reasons[3] + "\001");
      scoutln(data, bytesOut, "quote.Reason5Text="  + reasons[4] + "\001");
      scoutln(data, bytesOut, "quote.Reason6Text="  + reasons[5] + "\001");
      scoutln(data, bytesOut, "quote.Reason7Text="  + reasons[6] + "\001");
      scoutln(data, bytesOut, "quote.Reason8Text="  + reasons[7] + "\001");
      scoutln(data, bytesOut, "quote.Reason9Text="  + reasons[8] + "\001");
      scoutln(data, bytesOut, "quote.Reason10Text=" + reasons[9] + "\001");

      prependEdit(con, stmt, rs, unm, sid, uty, men, den, dnm, bnm, errStr, "C", generalUtils.stringFromBytes(code, 0L), date, "227a.htm", localDefnsDir, defnsDir, prependCode, req, bytesOut);

      screenLayout.bufferToBuffer(buf1, buf2, size1, size2, source, numFields, fieldNames, fieldTypes, fieldSizes, 'E', 'H', data, 5000, ddlData, ddlDataUpto[0], javaScriptCode, prependCode);

      append(con, stmt, rs, buf1, buf2, source, size1, size2, unm, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

      s += screenLayout.bufferToOut(buf1, buf2, source, out);
    }

    return s;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void javaScriptCall(Connection con, Statement stmt, ResultSet rs, byte[] b, HttpServletRequest req, String date, String unm, String uty, String dnm,
                              String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    b[0]= '\000';

    scoutln(b, bytesOut, "_.lineFile=quotel.line\001");
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4027, unm, uty, dnm, localDefnsDir, defnsDir) && serverUtils.passLockCheck(con, stmt, rs, "quote", date, unm))
      scoutln(b, bytesOut, "_.permitted=y\001");
    else scoutln(b, bytesOut, "_.permitted=n\001");

    scoutln(b, bytesOut, "_.stockRec=quotel.itemcode\001");
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3001, unm, uty, dnm, localDefnsDir, defnsDir))
      scoutln(b, bytesOut, "_.stockPermitted=y\001");
    else scoutln(b, bytesOut, "_.stockPermitted=n\001");
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void javaScript(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, String code, String unm, String sid, String uty, String men, String den, String dnm, String bnm, byte[] b, String localDefnsDir, String defnsDir,
                          int[] bytesOut) throws Exception
  {
    b[0] = '\000';

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4023, unm, uty, dnm, localDefnsDir, defnsDir)) //4025
    {
      scoutln(b, bytesOut, "<script language=\"JavaScript\">");

        scoutln(b,  bytesOut, "var thisLine;var topPosn=0;");

        scoutln(b,  bytesOut, "function doLine(option,line){");

        scoutln(b,  bytesOut, "if(option=='C'){document.getElementById('popup').style.visibility='hidden';}");
        scoutln(b,  bytesOut, "else { ");
        scoutln(b,  bytesOut, "updateLine();");
        scoutln(b,  bytesOut, "document.getElementById('popup').style.visibility='hidden';");
        scoutln(b,  bytesOut, "}}");

        // Update
        scoutln(b,  bytesOut, "var req2;");
        scoutln(b,  bytesOut, "function initRequest2(url)");
        scoutln(b,  bytesOut, "{if(window.XMLHttpRequest){req2=new XMLHttpRequest();}else if(window.ActiveXObject){req2=new ActiveXObject(\"Microsoft.XMLHTTP\");}}");

        scoutln(b,  bytesOut, "function updateLine(){data='';");
        scoutln(b,  bytesOut, "var line=document.getElementById('quotationline').innerHTML;");
        scoutln(b,  bytesOut, "var entry=document.getElementById('quotationentry').value;");
        scoutln(b,  bytesOut, "var manufacturer=document.getElementById('quotationmanufacturer').value;");
        scoutln(b,  bytesOut, "var manufacturerCode=document.getElementById('quotationmanufacturerCode').value;");
        scoutln(b,  bytesOut, "var customerItemCode=document.getElementById('quotationcustomerItemCode').value;");
        scoutln(b,  bytesOut, "var gstRate=document.getElementById('quotationgstRate').value;");
        scoutln(b,  bytesOut, "var quantity=document.getElementById('quotationquantity').value;");
        scoutln(b,  bytesOut, "var itemCode=document.getElementById('quotationitemCode').value;");
        scoutln(b,  bytesOut, "var uom=document.getElementById('quotationuom').value;");
        scoutln(b,  bytesOut, "var discount=document.getElementById('quotationdiscount').value;");
        scoutln(b,  bytesOut, "var remark=document.getElementById('quotationremark').value;");
        scoutln(b,  bytesOut, "var costPrice=document.getElementById('quotationcostPrice').value;");
        scoutln(b,  bytesOut, "var desc=document.getElementById('quotationdesc').value;");
        scoutln(b,  bytesOut, "var unitPrice=document.getElementById('quotationunitPrice').value;");

        scoutln(b,  bytesOut, "var url = \"http://" + men + "/central/servlet/QuotationLineUpdate?unm=\" + encodeURIComponent('" + unm + "') + \"&sid=\" + encodeURIComponent('" + sid + "') + \"&uty=\" + encodeURIComponent('"
                             + uty + "') + \"&men=\" + encodeURIComponent('" + men + "') + \"&den=\" + encodeURIComponent('" + den + "') + \"&bnm=\" + encodeURIComponent('" + bnm + "') + \"&p1=" + code
                             + "&p2=\" + line + \"&p3=\" + encodeURIComponent(entry) + \"&p4=\" + encodeURIComponent(manufacturer) + \"&p5=\" + encodeURIComponent(manufacturerCode) + \"&p6=\" + encodeURIComponent(customerItemCode) + \"&p7=\""
                             + " + encodeURIComponent(gstRate) + \"&p8=\" + encodeURIComponent(quantity) + \"&p9=\" + encodeURIComponent(itemCode) + \"&p10=\" + encodeURIComponent(uom) + \"&p11=\" + encodeURIComponent(discount) + \"&p12=\""
                             + " + encodeURIComponent(remark) + \"&p13=\" + encodeURIComponent(costPrice) + \"&p14=\" + encodeURIComponent(desc) + \"&p15=\" + encodeURIComponent(unitPrice) + \"&dnm=\" + encodeURIComponent('" + dnm + "');");
        scoutln(b,  bytesOut, "initRequest2(url);");
        scoutln(b,  bytesOut, "req2.onreadystatechange=processRequest2;");
        scoutln(b,  bytesOut, "req2.open(\"GET\",url,true);");
        scoutln(b,  bytesOut, "req2.send(null);}");

        scoutln(b, bytesOut, "function processRequest2(){");
        scoutln(b, bytesOut, "if(req2.readyState==4){");
        scoutln(b, bytesOut, "if(req2.status==200){");
        scoutln(b, bytesOut, "var res=req2.responseXML.getElementsByTagName(\"res\")[0].childNodes[0].nodeValue;");
        scoutln(b, bytesOut, "if(res.length>0){");
        scoutln(b, bytesOut, "if(res=='.') ; ");
        scoutln(b, bytesOut, "else{var messageElement=document.getElementById('msg');");
        scoutln(b, bytesOut, "messageElement.innerHTML='<span id=\"textErrorLarge\">'+res+'</span>';");
        scoutln(b, bytesOut, "}}}}}");

        scoutln(b, bytesOut, "function testResponseUpdate(){if(data.length>0){clearTimeout(res);processUpdate();}else res=setTimeout(\"testResponseUpdate()\",1000);}");

        scoutln(b, bytesOut, "function processUpdate(){if(data=='.')document.getElementById('popup').style.visibility='hidden';else alert(data);}");

        // edit

        scoutln(b, bytesOut, "var req3;");
        scoutln(b, bytesOut, "function initRequest3(url)");
        scoutln(b, bytesOut, "{if(window.XMLHttpRequest){req3=new XMLHttpRequest();}else ");
        scoutln(b, bytesOut, "if(window.ActiveXObject){req3=new ActiveXObject(\"Microsoft.XMLHTTP\");}}");

        scoutln(b, bytesOut, "function affect2(line){thisLine=line;");

        scoutln(b, bytesOut, "topPosn=((parseInt(thisLine)*50)+100);");

        scoutln(b, bytesOut, "var url = \"http://" + men + "/central/servlet/QuotationLineEdit?unm=\" + escape('" + unm + "') + \"&sid=\" + escape('" + sid + "') + \"&uty=\" + escape('" + uty + "') + \"&men=\" + escape('" + men + "') + \"&den=\" + escape('"
                             + den + "') + \"&bnm=\" + escape('" + bnm + "') + \"&p1=" + code + "&p2=\" + line + \"&dnm=\" + escape('" + dnm + "');");
        scoutln(b, bytesOut, "initRequest3(url);");
        scoutln(b, bytesOut, "req3.onreadystatechange=processRequest3;");
        scoutln(b, bytesOut, "req3.open(\"GET\",url,true);");
        scoutln(b, bytesOut, "req3.send(null);}");

        scoutln(b, bytesOut, "function processRequest3(){");
        scoutln(b, bytesOut, "if(req3.readyState==4){");
        scoutln(b, bytesOut, "if(req3.status==200){");
        scoutln(b, bytesOut, "var res=req3.responseXML.getElementsByTagName(\"res\")[0].childNodes[0].nodeValue;");
        scoutln(b, bytesOut, "if(res.length > 0){");
        scoutln(b, bytesOut, "if(res=='.'){");
        scoutln(b, bytesOut, "layoutHTML('line','line');");
        scoutln(b, bytesOut, "layoutValue('entry');");
        scoutln(b, bytesOut, "layoutValue('manufacturer');");
        scoutln(b, bytesOut, "layoutValue('manufacturerCode');");
        scoutln(b, bytesOut, "layoutValue('customerItemCode');");
        scoutln(b, bytesOut, "layoutValue('gstRate');");
        scoutln(b, bytesOut, "layoutValue('quantity');");
        scoutln(b, bytesOut, "layoutValue('itemCode');");
        scoutln(b, bytesOut, "layoutValue('uom');");
        scoutln(b, bytesOut, "layoutValue('discount');");
        scoutln(b, bytesOut, "layoutValue('remark');");
        scoutln(b, bytesOut, "layoutValue('costPrice');");
        scoutln(b, bytesOut, "layoutValue('unitPrice');");
        scoutln(b, bytesOut, "layoutValue('desc');");
        scoutln(b, bytesOut, "layoutHTML('currency','currency');");
        scoutln(b, bytesOut, "layoutHTML('currency','currency2');");
        scoutln(b, bytesOut, "layoutHTML('baseCurrency','baseCurrency');");
        scoutln(b, bytesOut, "layoutHTML('amount','amount');");
        scoutln(b, bytesOut, "layoutHTML('amount2','amount2');");

        scoutln(b, bytesOut, "if(!topPosn)topPosn=0;");
        scoutln(b, bytesOut, "document.getElementById('popup').style.top=topPosn+'px';");

        scoutln(b, bytesOut, "document.getElementById('popup').style.visibility='visible';");

        scoutln(b, bytesOut, "}}}}}");

        scoutln(b, bytesOut, "function thisLinePosn(e){");
        scoutln(b, bytesOut, "var x,y;if(e.pageX||e.pageY){x=e.pageX;y=e.pageY;} else {x=e.clientX+document.body.scrollLeft+document.documentElement.scrollLeft;y=e.clientY+document.body.scrollTop+document.documentElement.scrollTop;}");
        scoutln(b, bytesOut, "topPosn=y;");
        scoutln(b, bytesOut, "}");

        scoutln(b, bytesOut, "function layoutValue(name){");
        scoutln(b, bytesOut, "var entry = req3.responseXML.getElementsByTagName(name)[0].childNodes[0].nodeValue;");
        scoutln(b, bytesOut, "if(entry.length>0){if(entry=='.')document.getElementById('quotation'+name).value='';else document.getElementById('quotation'+name).value=entry;}");
        scoutln(b, bytesOut, "}");

        scoutln(b, bytesOut, "function layoutHTML(name,fld){");
        scoutln(b, bytesOut, "var entry = req3.responseXML.getElementsByTagName(name)[0].childNodes[0].nodeValue;");
        scoutln(b, bytesOut, "if(entry.length>0){if(entry=='.')document.getElementById('quotation'+fld).innerHTML='';else document.getElementById('quotation'+fld).innerHTML=entry;}");
        scoutln(b, bytesOut, "}");

        scoutln(b, bytesOut, "function affect1(line){");
        scoutln(b, bytesOut, "window.location.replace(\"/central/servlet/QuotationLine?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + code + "&p2=\"+line+\"&p3=&p4=\");}");

        scoutln(b, bytesOut, "function affect(line){");
        scoutln(b, bytesOut, "if(document.getElementById('editModeDiv2').style.visibility=='visible')affect2(line);else affect1(line);");
        scoutln(b, bytesOut, "}");

      scoutln(b, bytesOut, "</script>");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3001, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(b, bytesOut, "<script language=\"JavaScript\">");
      scoutln(b, bytesOut, "function stockRec(code){");
      scoutln(b, bytesOut, "window.location.href=\"/central/servlet/MainPageUtils1a?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+code;}</script>");
    }    
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void append(Connection con, Statement stmt, ResultSet rs, byte[][] buf1, byte[][] buf2, char[] source, int[] iSize1, int[] iSize2, String unm, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut)
                      throws Exception
  {
    byte[] b = new byte[1000];
    scoutln(b, bytesOut, "</form>");
    scoutln(b, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir));    
    screenLayout.appendBytesToBuffer(buf1, buf2, source, iSize1, iSize2, b);
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void prepend(Connection con, Statement stmt, ResultSet rs, boolean plain, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String code, String date, byte[] b, HttpServletRequest req,
                       String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    String code2 = code;//generalUtils.sanitise(code);

    b[0]= '\000';
    scoutln(b, bytesOut, "<html><head><title>" + code + "</title>");
    scoutln(b, bytesOut, "<script language=\"JavaScript\">\n");

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4023, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(b, bytesOut, "function fetch(){");
      scoutln(b, bytesOut, "window.location.replace(\"/central/servlet/QuotationHeaderEdit?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + code2 + "&p2=A&p3=&p4=\");}\n");

      scoutln(b, bytesOut, "function gst(){");
      scoutln(b, bytesOut, "window.location.href=\"/central/servlet/GSTRatesChange?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + code2 + "&p2=Q\";}");

      if(authenticationUtils.verifyAccess(con, stmt, rs, req, 6093, unm, uty, dnm, localDefnsDir, defnsDir))
      {
        scoutln(b, bytesOut, "function prices(){");
        scoutln(b, bytesOut, "window.location.href=\"/central/servlet/PricgrChange?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + code2 + "&p2=Q\";}");
      }

      if(authenticationUtils.verifyAccess(con, stmt, rs, req, 6098, unm, uty, dnm, localDefnsDir, defnsDir))
      {
        scoutln(b, bytesOut, "function discounts(){");
        scoutln(b, bytesOut, "window.location.href=\"/central/servlet/DocumentChangeDiscounts?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + code2 + "&p2=Q\";}");
      }
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4023, unm, uty, dnm, localDefnsDir, defnsDir)) // 4025
    {
      scoutln(b, bytesOut, "function add(){");
      scoutln(b, bytesOut, "window.location.replace(\"/central/servlet/QuotationLine?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + code2 + "&p2=&p3=&p4=\");}");
    }
    
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 1025, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(b, bytesOut, "function trace(){");
      scoutln(b, bytesOut, "window.location.replace(\"/central/servlet/DocumentTraceExecute?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + code2 + "&p2=Q\");}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 1025, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(b, bytesOut, "function trace2(){");
      scoutln(b, bytesOut, "window.location.replace(\"/central/servlet/DocumentTraceExecute?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + code2 + "&p2=Q&bta=Y\");}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 11800, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(b, bytesOut, "function trail(){");
      scoutln(b, bytesOut, "window.location.href=\"/central/servlet/TrailShow?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + code2 + "&p2=Q\";}");
    }

    scoutln(b, bytesOut, "function sortEntries(){");
    scoutln(b, bytesOut, "window.location.href=\"/central/servlet/QuotationPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + code2 + "&p7=Y\";}");

    scoutln(b, bytesOut, "function editMode(){document.getElementById('main').style.background='red';document.getElementById('editModeDiv').style.visibility='hidden';document.getElementById('editModeDiv2').style.visibility='visible';}");
    scoutln(b, bytesOut, "function editModeClose(){window.location.replace(\"/central/servlet/QuotationPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + code2 + "\");}\n");

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 6094, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(b, bytesOut, "function attach(){");
      scoutln(b, bytesOut, "window.location.replace(\"/central/servlet/DocumentAttachments?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + code2 + "&p2=quotea\");}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 8016, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      String projectCode = getAQuoteFieldGivenCode(con, stmt, rs, "ProjectCode", code);
      String companyCode = getAQuoteFieldGivenCode(con, stmt, rs, "CompanyCode", code);
      scoutln(b, bytesOut, "function mail(){");
      scoutln(b, bytesOut, "window.location.href=\"/central/servlet/MailExternalUserCreate?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p6=C&p4=" + companyCode + "&p5=" + projectCode + "&p1=QuotationRegisteredUser&p2=" + code2
                         + "&p3=Quotation&bnm=" + bnm + "\";}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4028, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(b, bytesOut, "function print(){");
      scoutln(b, bytesOut, "window.location.href=\"/central/servlet/AdminPrintControl?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=QuotationPrint&p2=" + code2 + "&p3=\";}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4028, unm, uty, dnm, localDefnsDir, defnsDir) && authenticationUtils.verifyAccess(con, stmt, rs, req, 4028, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(b, bytesOut, "function pdf(){");
      scoutln(b, bytesOut, "window.location.href=\"/central/servlet/PrintToPDFUtils?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p3=" + code2 + "&p1=0.000&p5=Quotation&p2=QuotationPrint\";}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4028, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(b, bytesOut, "function csv(){");
      scoutln(b, bytesOut, "window.location.href=\"/central/servlet/SendToCSVUtils?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + generalUtils.sanitise(code2) + "&p3=Quotation&p2=quotel\";}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 12708, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(b, bytesOut, "function direct(){");
      scoutln(b, bytesOut, "window.location.href=\"/central/servlet/ChannelSendDirect?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + generalUtils.sanitise(code2)
                         + "&p2=Quotation&p3=Requested%20Quotation%20Sent (" + generalUtils.sanitise(code2)+ ")\";}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4180, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(b, bytesOut, "function fax(){");
      scoutln(b, bytesOut, "window.location.href=\"/central/servlet/FaxCreate?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + code2 + "&p2=Q&p3=QuotationPrint&p4=&p5=Quotation\";}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4032, unm, uty, dnm, localDefnsDir, defnsDir)) // 4033
    {
      scoutln(b, bytesOut, "function so(){");
      scoutln(b, bytesOut, "var p1='CREATEBASEDONQUOTE:'+'" + code + "';");
      scoutln(b, bytesOut, "this.location.href=\"/central/servlet/SalesOrderPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty +"&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4055, unm, uty, dnm, localDefnsDir, defnsDir)) // 4056
    {
      scoutln(b, bytesOut, "function dorder(){");
      scoutln(b, bytesOut, "var p1='CREATEBASEDONQUOTE:'+'" + code + "';");
      scoutln(b, bytesOut, "this.location.href=\"/central/servlet/DeliveryOrderPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty +"&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4020, unm, uty, dnm, localDefnsDir, defnsDir)) // 4022
    {
      scoutln(b, bytesOut, "function quote(){");
      scoutln(b, bytesOut, "var p1='CREATEBASEDONQUOTE:'+'" + code + "';");
      scoutln(b, bytesOut, "this.location.href=\"/central/servlet/QuotationPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty +"&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    scoutln(b, bytesOut, "function plain(){");
    scoutln(b, bytesOut, "window.location.href=\"/central/servlet/QuotationPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + code2 + "&p2=A&p3=&p4=&p6=P\";}\n");

    String cssDirectory = directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir);
    String cssDocumentDirectory = directoryUtils.getCssDocumentDirectory(con, stmt, rs, dnm);
    scoutln(b, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + cssDirectory + "general.css\">\n");
    if(plain)
      scoutln(b, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"print\" href=\"" + cssDocumentDirectory + "documentPlain.css\">\n");
    else scoutln(b, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + cssDocumentDirectory + "document.css\">\n");

    outputPageFrame(con, stmt, rs, b, req, plain, ' ', ' ', "", "4019", "QuotationPage", date, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);
    
    scoutln(b, bytesOut, "<form id='quotation' name='quotation'>");

    scoutln(b,  bytesOut, "<div id=\"popup\" style=\"position:absolute;border:2px dotted red;top:100px;left:300px;z-index:1000;"
                         + "visibility:hidden;font-family: verdana, arial; font-size: 12px; background-color: #f0f0f0; color: #000000;padding: 5px;\">");

    scoutln(b,  bytesOut, "<table width=100%>");
    scoutln(b,  bytesOut, "<tr><td>");
    scoutln(b,  bytesOut, "<table cellpadding=2 border=0>");

    scoutln(b,  bytesOut, "<tr>");
    scoutln(b,  bytesOut, "  <td align=center><span class='code'>&nbsp; Line &nbsp;&nbsp;");
    scoutln(b,  bytesOut, "    <span id='quotationline'> &nbsp;</span></span>");
    scoutln(b,  bytesOut, "  </td>");
    scoutln(b,  bytesOut, "  <td><p>&nbsp;&nbsp;Entry &nbsp;&nbsp;<input type=text id=quotationentry size=6 maxchars=6></td>");
    scoutln(b,  bytesOut, "  <td>&nbsp;</td>");
    scoutln(b,  bytesOut, "  <td align=right><p>&nbsp;&nbsp;ItemCode&nbsp;&nbsp;<input type=text id=quotationitemCode></td>");
    scoutln(b,  bytesOut, "</tr>");

    scoutln(b,  bytesOut, "<tr>");
    scoutln(b,  bytesOut, "  <td></td>");
    scoutln(b,  bytesOut, "  <td style='font-family:courier;font-size:11px;'><p>&nbsp;.........1.........2.........3.........4.........5.........6.........7..</td>");
    scoutln(b,  bytesOut, "</tr>");

    scoutln(b,  bytesOut, "<tr>");
    scoutln(b,  bytesOut, "  <td valign=top><p>Description</td>");
    scoutln(b,  bytesOut, "  <td valign=top colspan=2><textarea id=quotationdesc rows=8 cols=70 wrap=hard style='font-family:courier;font-size:11px;'></textarea></td>");

    scoutln(b,  bytesOut, "  <td align=right>");
    scoutln(b,  bytesOut, "  <p>Manufacturer");
    scoutln(b,  bytesOut, "  &nbsp;&nbsp;" + inventoryAdjustment.getMfrsDDL(con, stmt, rs, "", "quotationmanufacturer", false));
    scoutln(b,  bytesOut, "  <br>");
    scoutln(b,  bytesOut, "  &nbsp;&nbsp;Mfr Code&nbsp;&nbsp;<input type=text id=quotationmanufacturerCode maxchars=30>");
    scoutln(b,  bytesOut, "  <br><br>");

    scoutln(b,  bytesOut, "  Customer Item Code<br><input type=text id=quotationcustomerItemCode maxchars=20>");
    scoutln(b,  bytesOut, "  <br><br>GST Rate");
    scoutln(b,  bytesOut, "    &nbsp;&nbsp;" + accountsUtils.getGSTDDL(con, stmt, rs, "", "quotationgstRate") + "</td>");
    scoutln(b,  bytesOut, "</tr>");
    scoutln(b,  bytesOut, "</table>");

    scoutln(b,  bytesOut, "<table border=0 cellpadding=2>");
    scoutln(b,  bytesOut, "<tr>");
    scoutln(b,  bytesOut, "  <td><p>Unit Price");
    scoutln(b,  bytesOut, "     <span id=quotationcurrency></span></td>");
    scoutln(b,  bytesOut, "  <td><input type=text id=quotationunitPrice></td>");

    scoutln(b,  bytesOut, "  <td><p>Quantity</td>");
    scoutln(b,  bytesOut, "  <td><input type=text id=quotationquantity maxchars=10 value='1'></td>");

    scoutln(b,  bytesOut, "  <td><p>Amount");
    scoutln(b,  bytesOut, "    <span id=quotationcurrency2></span></td>");
    scoutln(b,  bytesOut, "  <td><p>");
    scoutln(b,  bytesOut, "    <span id=quotationamount2></span></td>");
    scoutln(b,  bytesOut, "</tr>");

    scoutln(b,  bytesOut, "<tr>");
    scoutln(b,  bytesOut, "  <td><p>Unit of Measure</td>");
    scoutln(b,  bytesOut, "  <td><input type=text id=quotationuom value='Each' maxchars=10></td>");

    scoutln(b,  bytesOut, "  <td><p>Discount %</td>");
    scoutln(b,  bytesOut, "  <td><input type=text id=quotationdiscount maxchars=12></td>");

    scoutln(b,  bytesOut, "  <td><p>Amount");
    scoutln(b,  bytesOut, "     <span id=quotationbaseCurrency></span>&nbsp;</td>");
    scoutln(b,  bytesOut, "  <td><p><span id=quotationamount></span></td>");
    scoutln(b,  bytesOut, "</tr>");

    scoutln(b,  bytesOut, "<tr>");
    scoutln(b,  bytesOut, "  <td><p>Remark</td>");
    scoutln(b,  bytesOut, "  <td colspan=4><input type=text id=quotationremark maxchars=70 size=60></td>");
    scoutln(b,  bytesOut, "</tr>");

    scoutln(b,  bytesOut, "<tr>");
    scoutln(b,  bytesOut, "  <td><p>Cost Price</td>");
    scoutln(b,  bytesOut, "  <td>");
    scoutln(b,  bytesOut, "    <input type=text id=quotationcostPrice>");
    scoutln(b,  bytesOut, "  </td>");
    scoutln(b,  bytesOut, "</tr>");

    scoutln(b,  bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(b,  bytesOut, "</table></td></tr>");

    scoutln(b,  bytesOut, "</table>");

    scoutln(b,  bytesOut, "<br><a href=\"javascript:doLine('C','')\">Cancel</a> &#160;&#160;&#160; <a href=\"javascript:doLine('U',thisLine)\">Update</a>");

    scoutln(b,  bytesOut, "</div>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void prependEdit(Connection con, Statement stmt, ResultSet rs, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String errStr, String cad, String code, String date, String layoutFile,
                           String localDefnsDir, String defnsDir, byte[] b, HttpServletRequest req, int[] bytesOut) throws Exception
  {
    String code2 = generalUtils.sanitise(code);

    scoutln(b, bytesOut, "<html><head><title>" + code + "</title>");
    scoutln(b, bytesOut, "<script language=\"JavaScript\">\n");

    byte[] sourceBuf = new byte[5000];
    sourceBuf = htmlBuild.layoutToBuffer(sourceBuf, 5000, localDefnsDir, defnsDir, layoutFile);
    String saveStr = htmlBuild.buildSaveStringDelimited(sourceBuf, "document.forms[0]");

    scoutln(b, bytesOut, "function setCode(code){document.forms[0].CompanyCode.value=code;");
    scoutln(b, bytesOut, "main.style.visibility='visible';second.style.visibility='hidden';second.style.height='0';submenu.style.visibility='visible';window.location.hash='top';}");

    scoutln(b, bytesOut, "function save(thisOp){var saveStr=\"\";" + saveStr + "\n");
    scoutln(b, bytesOut, "var saveStr2=strip(saveStr);window.location.replace(\"/central/servlet/QuotationHeaderUpdate?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + cad + "&p2=" + code2
                       + "&p3=\"+saveStr2+\"&p4=\"+thisOp)}\n");
    
    scoutln(b, bytesOut, "function strip(saveStr){\n");
    scoutln(b, bytesOut, "var saveStr2='';var x;var len=saveStr.length;\n");
    scoutln(b, bytesOut, "for(x=0;x<len;++x)\n");
    scoutln(b, bytesOut, "if(saveStr.charAt(x)=='#')saveStr2+='%23';\n");
    scoutln(b, bytesOut, "else if(saveStr.charAt(x)=='\"')saveStr2+='%22';\n");
    scoutln(b, bytesOut, "else if(saveStr.charAt(x)=='&')saveStr2+='%26';\n");
    scoutln(b, bytesOut, "else if(saveStr.charAt(x)=='%')saveStr2+='%25';\n");
    scoutln(b, bytesOut, "else if(saveStr.charAt(x)==' ')saveStr2+='%20';\n");
    scoutln(b, bytesOut, "else if(saveStr.charAt(x)=='?')saveStr2+='%3F';\n");
    scoutln(b, bytesOut, "else if(saveStr.charAt(x)=='+')saveStr2+='%2B';\n");
    scoutln(b, bytesOut, "else if(escape(saveStr.charAt(x))=='%0A')saveStr2+='\003';\n");
    scoutln(b, bytesOut, "else if(escape(saveStr.charAt(x))=='%0D');");
    scoutln(b, bytesOut, "else saveStr2+=saveStr.charAt(x);return saveStr2;}\n");
    scoutln(b, bytesOut, "</script>\n");

    String cssDirectory = directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir);
    String cssDocumentDirectory = directoryUtils.getCssDocumentDirectory(con, stmt, rs, dnm);
    
    scoutln(b, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + cssDirectory + "general.css\">\n");
    scoutln(b, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + cssDocumentDirectory + "document.css\">\n");

    outputPageFrame(con, stmt, rs, b, req, false, ' ', cad.charAt(0), "", "4019", "QuotationHeaderEdit", date, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    if(errStr.length() > 0 && ! errStr.equalsIgnoreCase("null"))
      scoutln(b, bytesOut, errStr);

    scoutln(b, bytesOut, "<form>");
  }

  // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // rtns: 'X' if already exists
  //       ' ' if updated successfully
  //       'N' if no code specified
  //       'F' if not updated
  //       'I' if illegal code
  public char putLine(Connection con, Connection conp, Statement stmt, ResultSet rs, byte[] originalLine, byte[] code, String unm, String dnm, String localDefnsDir, String defnsDir, char cad, byte[] recData, int recDataLen, byte[] rtnLineBytes,
                      int[] bytesOut) throws Exception
  {
    byte[] lineBytes = new byte[20];
    byte[] b = new byte[100];
    generalUtils.catAsBytes("Line", 0, b, true);

    if(searchDataString(recData, recDataLen, "quotel", b, lineBytes) == -1)
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

    byte[] buf = new byte[4000];
    generalUtils.bytesToBytes(rtnLineBytes, 0, lineBytes, 0);

//     determines the number of fields and then processes them in order *but* assumes that fields in data are in no particular order

    sortFields(recData, buf, "quotel"); // sorts the data field (using buf in the process); results are put back into recData
    recDataLen = generalUtils.lengthBytes(recData, 0);
    generalUtils.zeroize(buf, 4000);
        
    String fieldNames = getFieldNamesQuoteL();
    byte[] value      = new byte[5000]; // plenty - to cover desc
    byte[] fieldName  = new byte[31];
    byte[] itemCode   = new byte[21];
    int x=0, y, fieldCount=0;
    int len = fieldNames.length();

    String itemCodeIn="", mfrIn="", mfrCodeIn=""; 
    
    while(x < len)
    {
      y=0;
      while(x < len && fieldNames.charAt(x) != ',')
        fieldName[y++] = (byte)fieldNames.charAt(x++);
      fieldName[y] = '\000';
      ++x;

      while(x < len && fieldNames.charAt(x) == ' ')
        ++x;

      if(searchDataString(recData, recDataLen, "quotel", fieldName, value) != -1) // entry exists
      {
        if(fieldCount == 1) // itemCode
          itemCodeIn = generalUtils.stringFromBytes(value, 0L);
        else
        if(fieldCount == 19) // mfr
          mfrIn = generalUtils.stringFromBytes(value, 0L);
        else
        if(fieldCount == 20)
          mfrCodeIn = generalUtils.stringFromBytes(value, 0L);
        else
        {
          if(fieldCount == 6) // gstrate
          {
            if(generalUtils.match(value, "<none>"))
              value[0] = '\000';
          }

          generalUtils.repAlpha(buf, 4000, (short)fieldCount, value);
        }
      }
      
      ++fieldCount;
    }
    
    String[] itemCodeOut = new String[1];
    String[] mfrOut      = new String[1];
    String[] mfrCodeOut  = new String[1];
    inventory.mapCodes(con, stmt, rs, itemCodeIn, mfrIn, mfrCodeIn, itemCodeOut, mfrOut, mfrCodeOut);
    generalUtils.strToBytes(itemCode, itemCodeOut[0]);
    generalUtils.repAlpha(buf, 4000, (short)1,  itemCodeOut[0]);
    generalUtils.repAlpha(buf, 4000, (short)19, mfrOut[0]);
    generalUtils.repAlpha(buf, 4000, (short)20, mfrCodeOut[0]);
    
    generalUtils.repAlpha(buf, 4000, (short)0,  code);
    generalUtils.repAlpha(buf, 4000, (short)11, lineBytes);

    // just-in-case no value then need to insert a default
    if((generalUtils.dfsAsStr(buf, (short)10)).length() == 0) // DeliveryDate 
      generalUtils.putAlpha(buf, 4000, (short)10, "1970-01-01");

    if((generalUtils.dfsAsStr(buf, (short)18)).length() == 0) // CostPrice 
      generalUtils.putAlpha(buf, 4000, (short)18, "0");

    byte[] rateB = new byte[30];
    getAQuoteFieldGivenCode(con, stmt, rs, "Rate", code, rateB);
    double rate = generalUtils.doubleFromBytesCharFormat(rateB, 0);
    
    if(rate == 0.0) rate = 1.0; // not sure if need this but some lines saved did not recalc
    double nonBaseSellPrice = 0.0;
    boolean unitPriceGiven=false;
    generalUtils.strToBytes(fieldName, "UnitPrice");
    if(   searchDataString(recData, recDataLen, "quotel", fieldName, value) != -1 // exists
       && value[0] != '\000')
    {
      nonBaseSellPrice = generalUtils.doubleFromBytesCharFormat(value, 0);
      unitPriceGiven = true;
    }

    // fetch item details
    generalUtils.strToBytes(fieldName, "Description");
    if(searchDataString(recData, recDataLen, "quotel", fieldName, value) != -1) // exists
    {
      if(value[0] == '\000') // description fld is blank
      {
        byte[] data = new byte[3000];

        if(inventory.getStockRecGivenCode(conp, stmt, rs, itemCode, '\000', data) != -1) // exists
        {
          generalUtils.dfs(data, (short)1, b);           // desc
          generalUtils.repAlpha(buf, 4000, (short)2, b);

          if(miscDefinitions.includeRemark(con, stmt, rs))
          {
            value[0]= '\000';
            scoutln(value, bytesOut, " \n" + generalUtils.dfsAsStr(data, (short)2));
          }

          if(! unitPriceGiven)
          {
            nonBaseSellPrice = generalUtils.dfsAsDouble(data, (short)25);  // rrp
            byte[] currency = new byte[50];
            getAQuoteFieldGivenCode(con, stmt, rs, "Currency", code, currency);
            generalUtils.dfs(data, (short)19, b);           // inventory currency
            if(! generalUtils.matchIgnoreCase(b, 0, currency, 0))
            {
              if(rate == 0.0) rate = 1.0;
              nonBaseSellPrice /= rate;
            }
          }

          generalUtils.dfs(data, (short)48, b);           // uom
          generalUtils.repAlpha(buf, 4000, (short)13, b);
        }

        //baseSellPrice = nonBaseSellPrice * rate;
        generalUtils.repDoubleGivenSeparator('8', '\000', buf, 4000, (short)3, nonBaseSellPrice);
      }
      else // strip only the first desc line for quoteL rec
      {
        if(! unitPriceGiven)
        {
          byte[] data = new byte[3000];

          if(inventory.getStockRecGivenCode(conp, stmt, rs, itemCode, '\000', data) != -1) // exists
          {
            nonBaseSellPrice = generalUtils.dfsAsDouble(data, (short)25);  // rrp
            byte[] currency = new byte[50];
            getAQuoteFieldGivenCode(con, stmt, rs, "Currency", code, currency);
            generalUtils.dfs(data, (short)19, b);           // inventory currency
            if(! generalUtils.matchIgnoreCase(b, 0, currency, 0))
            {
              if(rate == 0.0) rate = 1.0;
              nonBaseSellPrice /= rate;
            }
          }
          
          generalUtils.dfs(data, (short)48, b);           // uom
          generalUtils.repAlpha(buf, 4000, (short)13, b);
        }

        getDescriptionLine(0, value, b);
        generalUtils.repAlpha(buf, 4000, (short)2, b);
      }

      generalUtils.repDoubleGivenSeparator('8', '\000', buf, 4000, (short)3, nonBaseSellPrice);
    }

    reCalculate(buf, 4000, rate);

    if(putRecLine(con, stmt, rs, code, lineBytes, newOrEdit, buf, dnm, localDefnsDir, defnsDir) != 'F')
    {
      updateMultipleLines(con, stmt, rs, code, lineBytes, value, dnm, localDefnsDir, defnsDir);
      return ' ';
    }

    return 'F';
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void updateDLM(Connection con, Statement stmt, ResultSet rs, byte[] code, String unm, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    stmt = con.createStatement();
    stmt.executeUpdate("UPDATE quote SET SignOn = '" + unm + "', DateLastModified = NULL WHERE QuoteCode = '" + generalUtils.stringFromBytes(code, 0L) + "'");
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void reCalculate(byte[] buf, int dataBufLen, double rate) throws Exception
  {
    String s = generalUtils.dfsAsStr(buf, (short)4);

    double qty = generalUtils.doubleFromStr(s);
    if(s.length() == 0)
    {
      qty = 0.0;
      generalUtils.repDoubleGivenSeparator('2', '\000', buf, dataBufLen, (short)4, 0.0);
    }

    s = generalUtils.dfsAsStr(buf, (short)3);
    double unitPrice = generalUtils.doubleFromStr(s);
    if(s.length() == 0)
    {
      unitPrice = 0.0;
      generalUtils.repDoubleGivenSeparator('2', '\000', buf, dataBufLen, (short)3, 0.0);
    }

    s = generalUtils.dfsAsStr(buf, (short)7);
    double discountPercentage = generalUtils.doubleFromStr(s);
    if(s.length() == 0)
    {
      discountPercentage = 0.0;
      generalUtils.repDoubleGivenSeparator('2', '\000', buf, dataBufLen, (short)7, 0.0);
    }

    double discountValue = (qty * unitPrice) * discountPercentage / 100.0;

    double amt = ((qty * unitPrice) - discountValue);
    generalUtils.repDoubleGivenSeparator('2', '\000', buf, dataBufLen, (short)9, amt);

    amt = generalUtils.doubleDPs(amt, '2'); // added 5jul01
    generalUtils.repDoubleGivenSeparator('2', '\000', buf, dataBufLen, (short)5, (amt * rate));
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void updateMultipleLines(Connection con, Statement stmt, ResultSet rs, byte[] code, byte[] line, byte[] descData, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    try
    {
      stmt = con.createStatement();
    
      byte[] reqdLine = new byte[81];

      int numDescLines = detDescriptionLineInfo(descData);

      // determine how many lines there are for this code+line (entry) combination.
      rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM quotell WHERE QuoteCode = '" + generalUtils.stringFromBytes(code, 0L) + "' AND Entry = '" + generalUtils.stringFromBytes(line, 0L) + "'");
      rs.next();
      int numLinesAtPresent = rs.getInt("rowcount");
      rs.close() ;

      String sanitisedDesc, reqdLineStr;
      int lineCount=1;
      while(lineCount <= numDescLines && lineCount <= numLinesAtPresent) // overwrite those lines which exist
      {
        getDescriptionLine(lineCount, descData, reqdLine);
 
        reqdLineStr = generalUtils.stringFromBytes(reqdLine, 0L);
        sanitisedDesc = "";
        int len = reqdLineStr.length();
        for(int x=0;x<len;++x)
        {
          if(reqdLineStr.charAt(x) == '\'')
            sanitisedDesc += "''"; 
          else
          if(reqdLineStr.charAt(x) == '"')
            sanitisedDesc += "''''"; 
          else sanitisedDesc += reqdLineStr.charAt(x);        
        }
      
        stmt.executeUpdate("UPDATE quotell SET Text = '" + sanitisedDesc + "' WHERE QuoteCode = '" + generalUtils.stringFromBytes(code, 0L) + "' AND Entry = '" + generalUtils.stringFromBytes(line, 0L) + "' AND Line = '" + lineCount + "'");
        ++lineCount;
      }

      while(lineCount <= numDescLines) // add those lines which do not exist
      {
        getDescriptionLine(lineCount, descData, reqdLine);
 
        reqdLineStr = generalUtils.stringFromBytes(reqdLine, 0L);
        sanitisedDesc = "";
        int len = reqdLineStr.length();
        for(int x=0;x<len;++x)
        {
          if(reqdLineStr.charAt(x) == '\'')
            sanitisedDesc += "''"; 
          else
          if(reqdLineStr.charAt(x) == '"')
            sanitisedDesc += "''''"; 
          else sanitisedDesc += reqdLineStr.charAt(x);        
        }
      
        stmt.executeUpdate("INSERT INTO quotell (" + getFieldNamesQuoteLL() + ") VALUES ('" + generalUtils.stringFromBytes(code, 0L) + "','" + generalUtils.stringFromBytes(line, 0L) + "','" + lineCount + "','" + sanitisedDesc + "')");
        ++lineCount;
      }

      while(lineCount <= numLinesAtPresent) // remove any 'excess' lines
      {
        stmt.executeUpdate("DELETE FROM quotell WHERE QuoteCode = '" + generalUtils.stringFromBytes(code, 0L) + "' AND Entry = '" + generalUtils.stringFromBytes(line, 0L)  + "' AND Line = '" + lineCount + "'");
        ++lineCount;
      }
      
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println(e);  
      try
      {
        if(rs   != null) rs.close();
        if(stmt != null) stmt.close();
      }
      catch(Exception e2) { }
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getAQuoteFieldGivenCode(Connection con, Statement stmt, ResultSet rs, String fieldName, String quoteCode) throws Exception
  {
    byte[] value = new byte[300]; // plenty
    byte[] b     = new byte[21];
    generalUtils.strToBytes(b, quoteCode);
    
    getAQuoteFieldGivenCode(con, stmt, rs, fieldName, b, value);
    
    return generalUtils.stringFromBytes(value, 0L);
  }
  public void getAQuoteFieldGivenCode(Connection con, Statement stmt, ResultSet rs, String fieldName, byte[] quoteCode, byte[] value)
                                      throws Exception
  {
    if(quoteCode[0] == '\000') // just-in-case
      return;

    generalUtils.toUpper(quoteCode, 0);

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT " + fieldName + " FROM quote WHERE QuoteCode = '" + generalUtils.stringFromBytes(quoteCode, 0L) + "'");
    if(! rs.next())
      value[0] = '\000';
    else
    {    
      ResultSetMetaData rsmd = rs.getMetaData();
      generalUtils.strToBytes(value, getValue(1, ' ', rs, rsmd));
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private boolean getDescriptionLine(int whichLine, byte[] descData, byte[] reqdLine) throws Exception
  {
    int x=0;
    int count=0;

    while(count < whichLine)
    {
      while(descData[x] != '\003' && descData[x] != '\000')
        ++x;
      if(descData[x] == '\000')
        return false;
      ++x;
      ++count;
    }

    int y=0;
    while(descData[x] != (byte)'\003' && descData[x] != '\000')
    {
      if(descData[x] >= (byte)32 && descData[x] <= (byte)123)
        if(y < 80)
          reqdLine[y++] = descData[x];
      ++x;
    }
    reqdLine[y] = '\000';

    if(y == 0)
      return false;

    return true;
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private int detDescriptionLineInfo(byte[] descData) throws Exception
  {
    int x=0, y, count=0, blankCount=0;

    while(descData[x] != '\000')
    {
      y=0;
      while(descData[x] != '\003' && descData[x] != '\000')
      {
        ++x;
        ++y;
      }
      if(y == 0)
        ++blankCount;
      else blankCount = 0;
      ++count;
      if(descData[x] != '\000')
        ++x;
    }

    return count - 1 - blankCount;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean buildHTMLLayoutForLine(Connection con, Statement stmt, ResultSet rs, PrintWriter out, byte[] code, byte[] line, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir,
                                        String defnsDir, String errStr, byte[] dataAlready, HttpServletRequest req, int[] bytesOut) throws Exception
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

    String layoutFile="227b.htm";

    short[] fieldSizesQuoteL = new short[30]; // plenty
    getFieldSizesQuoteL(fieldSizesQuoteL);

    numFields = screenLayout.layoutToBuffer(buf1, buf2, size1, fieldNames, fieldTypes, fieldSizes, localDefnsDir, defnsDir, layoutFile, 1, getFieldNamesQuoteL(), fieldSizesQuoteL, null, null, null, null);

    byte[] b    = new byte[4000]; // plenty
    byte[] data = new byte[4000]; // plenty
    byte[] ddlData = new byte[1000];
    int[] ddlDataLen  = new int[1];
    ddlDataLen[0] = 1000;
    int[] ddlDataUpto = new int[1];
    ddlDataUpto[0] = 0;
    int dataLen = 4000;
    byte[] lineData = new byte[1000];
    int nextLine=0;
    char newOrEdit;

    if(line[0] != '\000') // existing line
    {
      if(code[0] != '\000') // just-in-case
      {
        if(dataAlready[0] != '\000') // coming with an err msg - passing-in data already entered but the data's fields may (will) not
        {                            // be in the correct order
          sortFields(dataAlready, data, "quotel");
        }
        else
        {
          getLine(con, stmt, rs, code, line, '\001', data, bytesOut);

          char dpOnQuantities = miscDefinitions.dpOnQuantities(con, stmt, rs, "5");
          generalUtils.bytesDPsGivenSeparator(true, '\001', dpOnQuantities, data, 4000, 4); // origin-0, quantity
          char dpOnUnitPrice  = miscDefinitions.dpOnUnitPrice(con, stmt, rs, "4");
          generalUtils.bytesDPsGivenSeparator(true, '\001', dpOnUnitPrice,  data, 4000, 3); // unitPrice 
          generalUtils.bytesDPsGivenSeparator(true, '\001', '2',  data, 4000, 5);  // amount
          generalUtils.bytesDPsGivenSeparator(true, '\001', '2',  data, 4000, 7);  // discount
          generalUtils.bytesDPsGivenSeparator(true, '\001', '2',  data, 4000, 9); // amount2
          generalUtils.bytesDPsGivenSeparator(true, '\001', '2',  data, 4000, 18); // costprice

          generalUtils.dfsGivenSeparator(true, '\001', data, (short)10, b); // deliveryDate
          if(! generalUtils.match(b,"1970-01-01"))
          {
            generalUtils.convertFromYYYYMMDD(b);
            generalUtils.repAlphaGivenSeparator('\001', data, 4000, (short)10, b);
          }

          try
          {
            generalUtils.dfsGivenSeparator(true, '\001', data, (short)6, b);
            if(b[0] == '\000')
              generalUtils.repAlphaGivenSeparator('\001', data, 4000, (short)6, "<none>");
          }
          catch(Exception e) { }

          byte[] multipleLinesData = new byte[1000];
          int[]  multipleListLen = new int[1]; multipleListLen[0] = 1000;
          int[]  multipleLinesCount = new int[1];
          byte[] llData = new byte[200];
          multipleLinesData = getMultipleLine(con, stmt, rs, code, line, multipleLinesData, multipleListLen, multipleLinesCount);

          for(int xx=0;xx<multipleLinesCount[0];++xx)
          {
            if(generalUtils.getListEntryByNum(xx, multipleLinesData, llData)) // just-in-case
            {
              generalUtils.replaceTwosWithOnes(llData);
              generalUtils.appendAlphaGivenBinary1(data, 4000, 2, llData, "\n");//<br>");
            }
          }
        }
      }

      newOrEdit = 'E';
    }
    else // new line
    {
      nextLine = getNextLine(con, stmt, rs, code, lineData, dnm, localDefnsDir, defnsDir);

      // when doing save & new, pre-fill app. fields
      if(dataAlready[0] != '\000') // passing-in data already entered but the data's fields may (will) not be in the correct order
      {
        sortFields(dataAlready, data, "quotel");
            
        generalUtils.dfsGivenSeparator(true, '\001', data, (short)2, b); // desc
        generalUtils.repAlphaGivenSeparator('\001', data, 4000, (short)2, b);
        generalUtils.repAlphaGivenSeparator('\001', data, 4000, (short)11, generalUtils.intToStr(nextLine)); // line
        generalUtils.repAlphaGivenSeparator('\001', data, 4000, (short)12, generalUtils.intToStr(nextLine)); // entry
      }
      else
      {
        data[0]= '\000';
        scoutln(data, bytesOut, "quotel.entry=" + generalUtils.intToStr(nextLine) + "\001");
      
        String gstRateDefault = accountsUtils.getDefaultGSTRate(con, stmt, rs, dnm, localDefnsDir, defnsDir);
        scoutln(data, bytesOut, "quotel.gstrate=" + gstRateDefault + "\001");
      }

      newOrEdit = 'N';
    }

    ddlData = documentUtils.getStoreDDLData(con, stmt, rs, "quotel.store",      ddlData, ddlDataUpto, ddlDataLen);
    ddlData = accountsUtils.getGSTRatesDDLData(con, stmt, rs, "quotel.gstrate", dnm, ddlData, ddlDataUpto, ddlDataLen, localDefnsDir, defnsDir);

    ddlData = inventory.getMfrsDDLData(con, stmt, rs, "quotel.manufacturer", ddlData, ddlDataUpto, ddlDataLen);
  
    getAQuoteFieldGivenCode(con, stmt, rs, "Currency", code, b);
    scoutln(data, bytesOut, "quotel.currency=" + generalUtils.stringFromBytes(b, 0L) + "\001");

    scoutln(data, bytesOut, "quotel.basecurrency=" + accountsUtils.getBaseCurrency(con, stmt, rs, dnm, localDefnsDir, defnsDir) + "\001");

    dataLen = generalUtils.lengthBytes(data, 0);

    String cad;
    if(newOrEdit == 'N')
      cad = "C";
    else cad = "A";
    
    byte[] prependCode = new byte[25000];
    prependEditLine(con, stmt, rs, unm, sid, uty, men, den, dnm, bnm, errStr, cad, generalUtils.stringFromBytes(code, 0L), generalUtils.stringFromBytes(line, 0L), "227b.htm", localDefnsDir, defnsDir, prependCode, newOrEdit, req, bytesOut);

    screenLayout.bufferToBuffer(buf1, buf2, size1, size2, source, numFields, fieldNames, fieldTypes, fieldSizes, 'E', 'H', data, dataLen, ddlData, ddlDataUpto[0], null, prependCode, false, false, "", nextLine);

    append(con, stmt, rs, buf1, buf2, source, size1, size2, unm, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    screenLayout.bufferToOut(buf1, buf2, source, out);

    return true;
  }
                                                                 
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void prependEditLine(Connection con, Statement stmt, ResultSet rs, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String errStr, String cad, String code, String line, String layoutFile,
                               String localDefnsDir, String defnsDir, byte[] b, char newOrEdit, HttpServletRequest req, int[] bytesOut) throws Exception
  {
    String code2 = generalUtils.sanitise(code);

    b[0] = '\000';
    scoutln(b, bytesOut, "<html><head><title>" + code + "</title>");
    scoutln(b, bytesOut, "<script language=\"JavaScript\">\n");

    byte[] sourceBuf = new byte[5000];
    sourceBuf = htmlBuild.layoutToBuffer(sourceBuf, 5000, localDefnsDir, defnsDir, layoutFile);
    String saveStr = htmlBuild.buildSaveStringDelimited(sourceBuf, "document.forms.doc");
    
    scoutln(b, bytesOut, "function setCode(code){document.forms.doc.ItemCode.value=code;main.style.visibility='visible';");
    scoutln(b, bytesOut, "second.style.visibility='hidden';third.style.visibility='hidden';second.style.height='0';third.style.height='0';submenu.style.visibility='visible';window.location.hash='top';}");

    scoutln(b, bytesOut, "function save(thisOp){var saveStr=\"\";" + saveStr + "\n");
    scoutln(b, bytesOut, "var saveStr2=strip(saveStr);window.location.replace(\"/central/servlet/QuotationLineUpdate?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + cad + "&p2=" + code2
                       + "&p3=" + line + "&p4=\"+thisOp+\"&p5=\"+saveStr2);}\n");

    scoutln(b, bytesOut, "function strip(saveStr){\n");
    scoutln(b, bytesOut, "var saveStr2='';var x;var len=saveStr.length;\n");
    scoutln(b, bytesOut, "for(x=0;x<len;++x)\n");
    scoutln(b, bytesOut, "if(saveStr.charAt(x)=='#')saveStr2+='%23';\n");
    scoutln(b, bytesOut, "else if(saveStr.charAt(x)=='\"')saveStr2+='%22';\n");
    scoutln(b, bytesOut, "else if(saveStr.charAt(x)=='&')saveStr2+='%26';\n");
    scoutln(b, bytesOut, "else if(saveStr.charAt(x)=='%')saveStr2+='%25';\n");
    scoutln(b, bytesOut, "else if(saveStr.charAt(x)==' ')saveStr2+='%20';\n");
    scoutln(b, bytesOut, "else if(saveStr.charAt(x)=='?')saveStr2+='%3F';\n");
    scoutln(b, bytesOut, "else if(saveStr.charAt(x)=='+')saveStr2+='%2B';\n");
    scoutln(b, bytesOut, "else if(escape(saveStr.charAt(x))=='%0A')saveStr2+='\003';\n");
    scoutln(b, bytesOut, "else if(escape(saveStr.charAt(x))=='%0D');");
    scoutln(b, bytesOut, "else saveStr2+=saveStr.charAt(x);return saveStr2;}\n");

    // fetch ietm details

    scoutln(b,  bytesOut, "var res31,req31;");
    scoutln(b,  bytesOut, "function initRequest31(url)");
    scoutln(b,  bytesOut, "{if(window.XMLHttpRequest){req31=new XMLHttpRequest();}else ");
    scoutln(b,  bytesOut, "if(window.ActiveXObject){req31=new ActiveXObject(\"Microsoft.XMLHTTP\");}}");

    scoutln(b,  bytesOut, "function getItemDetails(){");

    scoutln(b,  bytesOut, "var itemCode = document.getElementById('ItemCode').value;");
    scoutln(b,  bytesOut, "var mfr = document.getElementById('Manufacturer').value;");
    scoutln(b,  bytesOut, "var mfrCode = document.getElementById('ManufacturerCode').value;");

    scoutln(b,  bytesOut, "var url = \"http://" + men + "/central/servlet/QuotationItem?unm=\" + escape('" + unm + "') + \"&sid=\" + escape('" + sid + "') + \"&uty=\" + escape('" + uty + "') + \"&men=\" + escape('" + men + "') + \"&den=\" + escape('"
                         + den + "') + \"&bnm=\" + escape('" + bnm + "') + \"&p1=\" + encodeURIComponent(itemCode) + \"&p2=\" + encodeURIComponent(mfr) + \"&p3=\" + encodeURIComponent(mfrCode) + \"&dnm=\" + escape('" + dnm + "');");
    scoutln(b,  bytesOut, "initRequest31(url);");
    scoutln(b,  bytesOut, "req31.onreadystatechange=processRequest31;");
    scoutln(b,  bytesOut, "req31.open(\"GET\",url,true);");
    scoutln(b,  bytesOut, "req31.send(null);}");

    scoutln(b,  bytesOut, "function processRequest31(){");
    scoutln(b,  bytesOut, "if(req31.readyState==4){");
    scoutln(b,  bytesOut, "if(req31.status==200){");
    scoutln(b,  bytesOut, "var res31=req31.responseXML.getElementsByTagName(\"res\")[0].childNodes[0].nodeValue;");
    scoutln(b,  bytesOut, "if(res31.length > 0){");
    scoutln(b,  bytesOut, "if(res31=='.'){");
    scoutln(b,  bytesOut, "if(document.getElementById('Manufacturer').value.length==0)setField('manufacturer','Manufacturer');");
    scoutln(b,  bytesOut, "if(document.getElementById('ManufacturerCode').value.length==0)setField('manufacturerCode','ManufacturerCode');");
    scoutln(b,  bytesOut, "if(document.getElementById('ItemCode').value.length==0)setField('itemCode','ItemCode');");
    scoutln(b,  bytesOut, "if(document.getElementById('Description').value.length==0)setField('description','Description');");
    scoutln(b,  bytesOut, "}}}}}");

    scoutln(b,  bytesOut, "function setField(name,fld){");
    scoutln(b,  bytesOut, "var entry=req31.responseXML.getElementsByTagName(name)[0].childNodes[0].nodeValue;");
    scoutln(b,  bytesOut, "if(entry.length>0){if(entry=='.')document.getElementById(fld).value='';else document.getElementById(fld).value=entry;}");
    scoutln(b,  bytesOut, "}");

    scoutln(b, bytesOut, "</script>\n");

    String cssDirectory = directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir);
    String cssDocumentDirectory = directoryUtils.getCssDocumentDirectory(con, stmt, rs, dnm);
    scoutln(b, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + cssDirectory + "general.css\">\n");
    scoutln(b, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + cssDocumentDirectory + "document.css\">\n");

    outputPageFrame(con, stmt, rs, b, req, false, newOrEdit, ' ', "", "4019", "QuotationLine", "", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    scoutln(b, bytesOut, "<input type=hidden name=\"thisline\" VALUE=\"" + line + "\">");

    if(errStr.length() > 0 && ! errStr.equalsIgnoreCase("null"))
      scoutln(b, bytesOut, errStr);

    scoutln(b, bytesOut, "<form name=doc>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
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
        thisOne = generalUtils.strToInt(generalUtils.dfsAsStrGivenBinary1(true, data, (short)12)); // 'line': origin-0 + 1 (for leading count)
        if(thisOne > soFar)
          soFar = thisOne;
      }
    }

    return soFar + 1;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // rtns: 'X' if already exists
  //       ' ' if updated sucessfully
  //       'N' if no code specified
  //       'F' if not updated
  //       'I' if illegal code
  public char putHead(Connection con, Statement stmt, ResultSet rs, byte[] originalCode, String unm, String dnm, String localDefnsDir, String defnsDir, char cad, byte[] recData, int recDataLen, byte[] code, int[] bytesOut) throws Exception
  {
    byte[] str = new byte[21];  str[0] = '\000';
    generalUtils.catAsBytes("QuoteCode", 0, str, true);

    if(searchDataString(recData, recDataLen, "quote", str, code) == 0)
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

    // do multicurr bit
    
    // replace the currency on the header with the one from the cust rec
    generalUtils.strToBytes(fldName, "CompanyCode");
    if(searchDataString(recData, recDataLen, "quote", fldName, value) != -1) // just-in-case
    {
      String custCurrency = customer.getACompanyFieldGivenCode(con, stmt, rs, "Currency", generalUtils.stringFromBytes(value, 0L));
      if(custCurrency.length() > 0)
        generalUtils.repAlphaUsingOnes(recData, recDataLen, "Currency", custCurrency);
    }
    
    double rate = 1.0;
    generalUtils.strToBytes(fldName, "Rate");
    if(searchDataString(recData, recDataLen, "quote", fldName, value) != -1) // entry exists
    {
      if(value[0] == '\000' || generalUtils.doubleFromBytesCharFormat(value, 0) == 0.0)
      {
        byte[] currency = new byte[10];
        byte[] date     = new byte[20];
        generalUtils.strToBytes(fldName, "Currency");
        searchDataString(recData, recDataLen, "quote", fldName, currency); // assumes exists
        
        generalUtils.strToBytes(fldName, "QuoteDate");
        searchDataString(recData, recDataLen, "quote", fldName, date); // assumes exists

        rate = accountsUtils.getApplicableRate(con, stmt, rs, generalUtils.stringFromBytes(currency, 0L), generalUtils.stringFromBytes(date, 0L), value, dnm, localDefnsDir, defnsDir);
        if(rate == 0.0) rate = 1.0;
      }
      else rate = generalUtils.doubleFromBytesCharFormat(value, 0);
    }

    String fieldNamesQuote = getFieldNamesQuote();

    // chk if any of the compname or addr flds have values already
    int len;
    boolean atleastOneHasValue=false;
    for(short x=3;x<10;++x)
    {
      getFieldName(fieldNamesQuote, x, fldName);
      if(searchDataString(recData, recDataLen, "quote", fldName, value) != -1) // entry exists
      {
        if(value[0] != '\000')
          atleastOneHasValue = true;
      }
    }

    String companyCode = "";
    generalUtils.strToBytes(fldName, "CompanyCode");
    if(searchDataString(recData, recDataLen, "quote", fldName, value) != -1) // entry exists
      companyCode = generalUtils.stringFromBytes(value, 0L);

    len = fieldNamesQuote.length();
    int x=0, y, fieldCount=0;
    while(x < len)
    {
      y=0;
      while(x < len && fieldNamesQuote.charAt(x) != ',')
        fldName[y++] = (byte)fieldNamesQuote.charAt(x++);
      fldName[y] = '\000';
      ++x;
      
      while(x < len && fieldNamesQuote.charAt(x) == ' ')
        ++x;

      if(searchDataString(recData, recDataLen, "quote", fldName, value) != -1) // entry exists
      {  
        if(fieldCount == 0) // code
          ;
        else
        if(fieldCount == 1) // date
          generalUtils.repAlpha(buf, 2000, (short)1, generalUtils.convertDateToSQLFormat(generalUtils.stringFromBytes(value, 0L)));  
        else
        if(fieldCount == 2)
        {
          generalUtils.toUpper(value, 0);

          if(! atleastOneHasValue)
          {
            byte[] b    = new byte[100];
            byte[] data = new byte[3000];
            if(customer.getCompanyRecGivenCode(con, stmt, rs, value, '\000', dnm, data, localDefnsDir, defnsDir) != -1) // exists
            {
              generalUtils.dfs(data, (short)1, b);           // name
              generalUtils.repAlpha(buf, 2000, (short)3, b);
              generalUtils.dfs(data, (short)5, b);           // addr1
              generalUtils.repAlpha(buf, 2000, (short)4, b);
              generalUtils.dfs(data, (short)6, b);           // addr2
              generalUtils.repAlpha(buf, 2000, (short)5, b);
              generalUtils.dfs(data, (short)7, b);           // addr3
              generalUtils.repAlpha(buf, 2000, (short)6, b);
              generalUtils.dfs(data, (short)8, b);           // addr4
              generalUtils.repAlpha(buf, 2000, (short)7, b);
              generalUtils.dfs(data, (short)9, b);           // addr5
              generalUtils.repAlpha(buf, 2000, (short)8, b);
              generalUtils.dfs(data, (short)10, b);          // pc
              generalUtils.repAlpha(buf, 2000, (short)9, b);
            }
          }

          generalUtils.putAlpha(buf, 2000, (short)2, value);
        }
        else
        if(fieldCount == 35) // signon
        {
          if(value[0] == '\000')
            generalUtils.putAlpha(buf, 2000, (short)35, unm);
          else generalUtils.putAlpha(buf, 2000, (short)35, value);
        }
        else
        if(fieldCount == 11) // notes
        {
          byte[] b2 = new byte[300];
          int i2=0, valueLen = generalUtils.lengthBytes(value, 0);
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
        if(fieldCount == 46) // terms
        {
          if(value[0] == '\000')
          {
            String days;
            if((days = customer.getACompanyFieldGivenCode(con, stmt, rs, "CreditDays", companyCode)).length() == 0) // unknown cust
              days = "30";            
            generalUtils.repAlpha(buf, 2000, (short)46, days + " days");
          }
          else generalUtils.putAlpha(buf, 2000, (short)46, value);
        }  
        else
        if(fieldCount == 50) // fax
        {
          if(value[0] == '\000')
          {
            // fetch fax from customer file
            generalUtils.repAlpha(buf, 2000, (short)50, customer.getACompanyFieldGivenCode(con, stmt, rs, "Fax", companyCode));
          }
          else generalUtils.putAlpha(buf, 2000, (short)50, value);
        }  
        else
        if(fieldCount == 56) // phone
        {
          if(value[0] == '\000')
          {
            // fetch phone1 from customer file
            generalUtils.repAlpha(buf, 2000, (short)56, customer.getACompanyFieldGivenCode(con, stmt, rs, "Phone1", companyCode));
          }
          else generalUtils.putAlpha(buf, 2000, (short)56, value);
        }  
        else
        if(fieldCount == 43) // rate
        {
          if(rate == 0.0) rate = 1.0;

          generalUtils.repDoubleGivenSeparator('8', '\000', buf, 2000, (short)43, rate);
        }
        else if(value[0] != '\000') generalUtils.repAlpha(buf, 2000, (short)fieldCount, value);
      }
      
      ++fieldCount;
    }

    // just-in-case no value then need to insert a default
    if((generalUtils.dfsAsStr(buf, (short)17)).length() == 0) // GSTTotal 
      generalUtils.putAlpha(buf, 2000, (short)17, "0"); 

    if((generalUtils.dfsAsStr(buf, (short)18)).length() == 0) // TotalTotal 
      generalUtils.putAlpha(buf, 2000, (short)18, "0");
    
    if((generalUtils.dfsAsStr(buf, (short)44)).length() == 0) // baseTotalTotal 
      generalUtils.putAlpha(buf, 2000, (short)44, "0");
    
    if((generalUtils.dfsAsStr(buf, (short)45)).length() == 0) // BaseGSTTotal 
      generalUtils.putAlpha(buf, 2000, (short)45, "0");
    
    if((generalUtils.dfsAsStr(buf, (short)33)).length() == 0) // GroupDiscount 
      generalUtils.putAlpha(buf, 2000, (short)33, "0");

    generalUtils.repAlpha(buf, 2000, (short)13, "1970-01-01"); // unused4
    generalUtils.repAlpha(buf, 2000, (short)14, "1970-01-01"); // unused5

    if((generalUtils.dfsAsStr(buf, (short)35)).length() == 0) // signon 
      generalUtils.repAlpha(buf, 2000, (short)35, unm);

    if((generalUtils.dfsAsStr(buf, (short)58)).length() == 0) // 
      generalUtils.repAlpha(buf, 2000, (short)58, "1970-01-01"); // toEngineeringDate
    if((generalUtils.dfsAsStr(buf, (short)61)).length() == 0) // 
      generalUtils.repAlpha(buf, 2000, (short)61, "1970-01-01"); // ToProcurementDate
    if((generalUtils.dfsAsStr(buf, (short)64)).length() == 0) // 
      generalUtils.repAlpha(buf, 2000, (short)64, "1970-01-01"); // ToSchedulingDate
    if((generalUtils.dfsAsStr(buf, (short)67)).length() == 0) // 
      generalUtils.repAlpha(buf, 2000, (short)67, "1970-01-01"); // ToManagerDate
    if((generalUtils.dfsAsStr(buf, (short)70)).length() == 0) // 
      generalUtils.repAlpha(buf, 2000, (short)70, "1970-01-01"); // EngineeringApprovedDate
    if((generalUtils.dfsAsStr(buf, (short)73)).length() == 0) // 
      generalUtils.repAlpha(buf, 2000, (short)73, "1970-01-01"); // ProcurementConfirmedDate
    if((generalUtils.dfsAsStr(buf, (short)76)).length() == 0) // 
      generalUtils.repAlpha(buf, 2000, (short)76, "1970-01-01"); // SchedulingConfirmedDate
    if((generalUtils.dfsAsStr(buf, (short)79)).length() == 0) // 
      generalUtils.repAlpha(buf, 2000, (short)79, "1970-01-01"); // ManagerApprovedDate
    if((generalUtils.dfsAsStr(buf, (short)82)).length() == 0) // 
      generalUtils.repAlpha(buf, 2000, (short)82, "1970-01-01"); // QuoteSentDate

    // if rate has changed then recalc each line
    boolean rateChanged = false;
    if(cad == 'A')
    {
      byte[] data = new byte[2000];
      if(getRecGivenCode(con, stmt, rs, code, '\000', data, dnm, localDefnsDir, defnsDir, bytesOut) != -1) // just-in-case
      {
        if(rate != generalUtils.dfsAsDouble(data, (short)43)) // rate changed
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

            generalUtils.repAlpha(buf, 2000, (short)17, unm);

            generalUtils.dfs(buf, (short)12, str); // line

            rtn = putRecLine(con, stmt, rs, code, str, 'E', buf, dnm, localDefnsDir, defnsDir);
          }
        }
      }

      if(newOrEdit == 'N')
        serverUtils.syncToIW(con, generalUtils.stringFromBytes(code, 0L), "", "", 'Q', generalUtils.todaySQLFormat(localDefnsDir, defnsDir), unm, companyCode, dnm);
      else serverUtils.updateNewWavechannelsRec(con, stmt, rs, generalUtils.stringFromBytes(code, 0L), "Q", companyCode);
    }
    
    return rtn;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
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

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public int getRecGivenCode(Connection con, Statement stmt, ResultSet rs, byte[] code, char separator, byte[] data, String dnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    if(code[0] == '\000') // just-in-case
      return -1;

    generalUtils.toUpper(code, 0);

    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT * FROM quote WHERE QuoteCode = '" + generalUtils.stringFromBytes(code, 0L) + "'");
    if(! rs.next())
    {
      rs.close();
      stmt.close();
      return -1;
    }      
    
    ResultSetMetaData rsmd = rs.getMetaData();
    
    String fieldTypes = getFieldTypesQuote();
     
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
      String fieldNames = getFieldNamesQuote();
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
        scoutln(data, bytesOut, "quote." + thisFieldName + "=" + getValue(count, thisFieldType, rs, rsmd) + "\001");
      
        ++x;
        while(x < len && fieldNames.charAt(x) == ' ')
          ++x;
      }
    }  
    
    rs.close();
    stmt.close();
    
    return 0;   
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
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

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
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
      String fieldTypes = getFieldTypesQuote();
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
      
        q = "INSERT INTO quote (" + getFieldNamesQuote() + ") VALUES (" + opStr + ")";
      }
      else // newOrEdit == 'E'
      {
        String thisFieldName;
        char thisFieldType;

        // change: abc\0def\0... format into: CompanyCode='abc',Name='def',...

        String fieldNames = getFieldNamesQuote();
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

        q = "UPDATE quote SET " + opStr + " WHERE QuoteCode = '" + generalUtils.stringFromBytes(code, 0L) + "'";
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

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public char putRecLine(Connection con, Statement stmt, ResultSet rs, byte[] code, byte[] line, char newOrEdit, byte[] data, String dnm, String localDefnsDir, String defnsDir)
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
      String fieldTypes = getFieldTypesQuoteL();
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
            while(data[x] != '\000' )//&& data[x] != '"')
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
      
        q = "INSERT INTO quotel (" + getFieldNamesQuoteL() + ") VALUES (" + opStr + ")";
      }
      else // newOrEdit == 'E'
      {
        String thisFieldName;
        char thisFieldType;

        // change: abc\0def\0... format into: CompanyCode='abc',Name='def',...

        String fieldNames = getFieldNamesQuoteL();
        int len = fieldNames.length();
        int y=0;
            
        // cycle through each entry in fieldNames
            
        while(x < len)
        {
          if(! first)
            opStr += ",";
          else first = false;
//System.out.println(opStr);

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

        q = "UPDATE quotel SET " + opStr + " WHERE QuoteCode = '" + generalUtils.stringFromBytes(code, 0L) + "' AND Line = '"  + generalUtils.stringFromBytes(line, 0L) + "'";
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

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean deleteLine(Connection con, Statement stmt, ResultSet rs, byte[] code, byte[] line, int[] bytesOut)
  {
    try
    {
      if(code[0] == '\000') // just-in-case
        return false;

      if(line[0] == '\000')
        return false;

      // need to determine the 'entry' field
      byte[] data = new byte[2000];

      if(getLine(con, stmt, rs, code, line, '\000', data, bytesOut) == -1) // just-in-case
        return false;

      byte[] entry = new byte[7];
      generalUtils.dfs(data, (short)12, entry);

      stmt = con.createStatement();
    
      stmt.executeUpdate("DELETE FROM quotel WHERE QuoteCode = '" + generalUtils.stringFromBytes(code, 0L) + "' AND Line = '" + generalUtils.stringFromBytes(line, 0L) + "'");

      stmt.close();

      // delete multiple lines

      stmt = con.createStatement();
      stmt.executeUpdate("DELETE FROM quotell WHERE QuoteCode = '" + generalUtils.stringFromBytes(code, 0L) + "' AND Entry = '" + generalUtils.stringFromBytes(line, 0L) + "'");
   
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

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public byte[] getLines(Connection con, Statement stmt, ResultSet rs, byte[] code, char separator, byte[] linesData, int[] listLen, int[] linesCount, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    if(code[0] == '\000') // just-in-case
      return linesData;

    linesCount[0] = 0;

    generalUtils.toUpper(code, 0);

    byte[] data = new byte[5000];
    String s;
    int x, len;
    String fieldTypes = getFieldTypesQuoteL();
    String fieldNames = getFieldNamesQuoteL();    
    byte[] newItem = new byte[1000];
    String thisFieldName;
    char thisFieldType;
    int count;
    int lenFieldNames = fieldNames.length();
    int lenFieldTypes = fieldTypes.length();
    data[0] = '\000';
        
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT * FROM quotel WHERE QuoteCode = '" + generalUtils.stringFromBytes(code, 0L) + "'");
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
          s += "quotel." + thisFieldName + "=" + getValue(count, thisFieldType, rs, rsmd) + "\001";
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

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // if separator is '\001' then buf is: company.code=acme\001company.name=acme ltd\001
  // else if separator is '\000' (eg) then buf is acme\0acme ltd\0
  public int getLine(Connection con, Statement stmt, ResultSet rs, byte[] code, byte[] line, char separator, byte[] data, int[] bytesOut) throws Exception
  {
    if(code[0] == '\000') // just-in-case
      return -1;

    if(line[0] == '\000')
      return -1;

    generalUtils.toUpper(code, 0);

    int upto;
    String fieldTypes = getFieldTypesQuoteL();
    String fieldNames = getFieldNamesQuoteL();
    String thisFieldName;
    char thisFieldType;
    int x, count, len = fieldNames.length(), numFields = fieldTypes.length();
    data[0] = '\000';

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT * FROM quotel WHERE QuoteCode = '" + generalUtils.stringFromBytes(code, 0L) + "' AND Line = '" + generalUtils.stringFromBytes(line, 0L) + "'");
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
          scoutln(data, bytesOut, "quotel." + thisFieldName + "=" + getValue(count, thisFieldType, rs, rsmd) + "\001");
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

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public byte[] getMultipleLines(Connection con, Statement stmt, ResultSet rs, byte[] code, byte[] linesData, int[] listLen, int[] linesCount) throws Exception
  {
    linesCount[0] = 0;

    if(code[0] == '\000') // just-in-case
      return linesData;

    generalUtils.toUpper(code, 0);

    int len;
    String s;
    byte[] newItem = new byte[4000];

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT * FROM quotell WHERE QuoteCode = '" + generalUtils.stringFromBytes(code, 0L) + "'");
    ResultSetMetaData rsmd = rs.getMetaData();
    while(rs.next())
    {
      s  = getValue(1, ' ', rs, rsmd) + "\001";
      s += getValue(2, ' ', rs, rsmd) + "\001";
      s += getValue(3, ' ', rs, rsmd) + "\001";
      s += getValue(4, ' ', rs, rsmd) + "\001";
      
      ++linesCount[0];
      s = generalUtils.intToStr(linesCount[0]) + "\002" + s;
      len = generalUtils.replaceOnesWithTwos(s, newItem);
      newItem[len] = '\001';
      newItem[len + 1] = '\000';
      linesData = generalUtils.appendToList(true, newItem, linesData, listLen);
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    return linesData;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public byte[] getMultipleLine(Connection con, Statement stmt, ResultSet rs, byte[] code, byte[] line, byte[] linesData, int[] listLen, int[] linesCount) throws Exception
  {
    linesCount[0] = 0;

    if(code[0] == '\000') // just-in-case
      return linesData;

    generalUtils.toUpper(code, 0);

    int len;
    byte[] newItem = new byte[100];

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT Text FROM quotell WHERE QuoteCode = '" + generalUtils.stringFromBytes(code, 0L) + "' AND Entry = '"
                           + generalUtils.stringFromBytes(line, 0L) + "'");
    ResultSetMetaData rsmd = rs.getMetaData();
    while(rs.next())
    {    
      generalUtils.strToBytes(newItem, getValue(1, ' ', rs, rsmd));

      len = generalUtils.lengthBytes(newItem, 0);
      newItem[len++] = '\001';
      newItem[len] = '\000';
      linesData = generalUtils.appendToList(true, newItem, linesData, listLen);
      ++linesCount[0];
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    return linesData;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void prependDisplayOnly(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, String code2, String date, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir,
                                  String defnsDir, byte[] b, int[] bytesOut) throws Exception
  {
    b[0]= '\000';
    scoutln(b, bytesOut, "<html><head><title>" + code2 + "</title>");

    scoutln(b, bytesOut, "<script language=\"JavaScript\">\n");

    scoutln(b, bytesOut, "function trace(){");
    scoutln(b, bytesOut, "window.location.replace(\"/central/servlet/DocumentTraceExecute?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + code2 + "&p2=Q\");}");

    scoutln(b, bytesOut, "function print(){");
    scoutln(b, bytesOut, "window.location.href=\"/central/servlet/AdminPrintControl?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=QuotationPrint&p2=" + code2 + "&p3=\";}");

    scoutln(b, bytesOut, "function pdf(){");
    scoutln(b, bytesOut, "window.location.href=\"/central/servlet/PrintToPDFUtils?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p3=" + code2 + "&p1=0.000&p5=Quotation&p2=QuotationPrint\";}");

    scoutln(b, bytesOut, "function csv(){");
      scoutln(b, bytesOut, "window.location.href=\"/central/servlet/SendToCSVUtils?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + generalUtils.sanitise(code2) + "&p3=Quotation&p2=quotel\";}");

    scoutln(b, bytesOut, "function plain(){");
    scoutln(b, bytesOut, "window.location.href=\"/central/servlet/QuotationRegisteredUser?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + code2 + "&p2=A&p3=&p4=&p6=P\";}\n");

    String cssDirectory = directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir);
    String cssDocumentDirectory = directoryUtils.getCssDocumentDirectory(con, stmt, rs, dnm);
    scoutln(b, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + cssDirectory + "general.css\">\n");
    scoutln(b, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + cssDocumentDirectory + "document.css\">\n");

    outputPageFrame(con, stmt, rs, b, req, false, ' ',  ' ', "", "4122", "QuotationRegisteredUser", date, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void sortFields(byte[] ipBuf, byte[] opBuf, String which) throws Exception
  {
    String thisFieldName;
    String fieldNames;
    int x=0, y;
    byte[] thisFieldNameB = new byte[50];
    byte[] value          = new byte[3000];
    
    if(which.equals("quote"))
      fieldNames = getFieldNamesQuote();
    else // if(which.equals("quotel"))
      fieldNames = getFieldNamesQuoteL();
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
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void updateGSTRates(Connection con, Statement stmt, ResultSet rs, byte[] code, String gstRate, String unm, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    updateDLM(con, stmt, rs, code, unm, dnm, localDefnsDir, defnsDir);

    int y, z;
    byte[] buf        = new byte[4000];
    byte[] b          = new byte[20];
    byte[] line       = new byte[500];
    byte[] linesData  = new byte[4000];
    int[]  listLen    = new int[1];  listLen[0] = 4000;
    int[]  linesCount = new int[1];
    linesData = getLines(con, stmt, rs, code, '\000', linesData, listLen, linesCount, dnm, localDefnsDir, defnsDir);

    for(int x=0;x<linesCount[0];++x)
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

        generalUtils.repAlpha(buf, 4000, (short)6,  gstRate);
        generalUtils.repAlpha(buf, 4000, (short)17, unm);
        generalUtils.repAlpha(buf, 4000, (short)8, '\000'); // dlm

        generalUtils.dfs(buf, (short)11, b); // line

        putRecLine(con, stmt, rs, code, b, 'E', buf, dnm, localDefnsDir, defnsDir);
      }
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void updatePrices(Connection con, Statement stmt, ResultSet rs, byte[] code, String band, String clear, String unm, String dnm,
                           String localDefnsDir, String defnsDir) throws Exception
  {
    byte[] buf  = new byte[5000];
    byte[] data = new byte[5000];

    byte[] rateB = new byte[30];
    getAQuoteFieldGivenCode(con, stmt, rs, "Rate", code, rateB);
    double rate = generalUtils.doubleFromBytesCharFormat(rateB, 0);
 
    updateDLM(con, stmt, rs, code, unm, dnm, localDefnsDir, defnsDir);

    int y, z;
    byte[] b          = new byte[21];
    byte[] line       = new byte[500];
    byte[] linesData  = new byte[4000];
    int[]  listLen    = new int[1];  listLen[0] = 4000;
    int[]  linesCount = new int[1];
    linesData = getLines(con, stmt, rs, code, '\000', linesData, listLen, linesCount, dnm, localDefnsDir, defnsDir);

    for(int x=0;x<linesCount[0];++x)
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

        generalUtils.dfs(buf, (short)1, b); // itemCode
        if(inventory.getStockRecGivenCode(con, stmt, rs, b, '\000', data) != -1) // is an existant stock rec
        {
          switch(generalUtils.strToInt(band)) // pickup price dependent upon band
          {
            case  1 : generalUtils.dfs(data, (short)20, b); break;
            case  2 : generalUtils.dfs(data, (short)21, b); break;
            case  3 : generalUtils.dfs(data, (short)22, b); break;
            case  4 : generalUtils.dfs(data, (short)23, b); break;
            default : generalUtils.dfs(data, (short)25, b); break;
          }

          generalUtils.repAlpha(buf, 5000, (short)3,  b); // sellPrice
        }

        if(clear.equals("Y"))
          generalUtils.repAlpha(buf, 5000, (short)7, "0.0"); // discount
          
        reCalculate(buf, z, rate);

        generalUtils.repAlpha(buf, 4000, (short)17, unm);
        generalUtils.repAlpha(buf, 4000, (short)8, '\000');

        generalUtils.dfs(buf, (short)11, b); // line

        putRecLine(con, stmt, rs, code, b, 'E', buf, dnm, localDefnsDir, defnsDir);
      }
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // uses layout 227d.htm
  public boolean getRecToHTMLDisplayOnly(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, HttpServletRequest req, byte[] code, String unm, String sid, String uty, String men, String den, String dnm,
                                         String bnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
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

    String codeStr = generalUtils.stringFromBytes(code, 0L);
    
    byte[] javaScriptCode = new byte[1000];
    javaScript(con, stmt, rs, req, codeStr, unm, sid, uty, men,den, dnm, bnm, javaScriptCode, localDefnsDir, defnsDir, bytesOut);

    byte[] ddlData = new byte[1];
    int[] ddlDataUpto = new int[1];
    ddlDataUpto[0] = 0;

    if(getRecGivenCode(con, stmt, rs, code, '\001', data, dnm, localDefnsDir, defnsDir, bytesOut) == -1) // just-in-case
    {
      messagePage.msgScreen(false, out, req, 6, unm, sid, uty, men, den, dnm, bnm, "Quotation", imagesDir, localDefnsDir, defnsDir, bytesOut);
      return false;
    }

    short[] fieldSizesQuote = new short[90]; // plenty
    getFieldSizesQuote(fieldSizesQuote);
    short[] fieldSizesQuoteL = new short[30]; // plenty
    getFieldSizesQuoteL(fieldSizesQuoteL);

    numFields = screenLayout.layoutToBuffer(buf1, buf2, size1, fieldNames, fieldTypes, fieldSizes, localDefnsDir, defnsDir, "227d.htm", 2, getFieldNamesQuote(), fieldSizesQuote, getFieldNamesQuoteL(), fieldSizesQuoteL, null, null);

    int recDataLen = generalUtils.lengthBytes(data, 0);

    String fieldNamesQuote = getFieldNamesQuote();
    byte[] value     = new byte[1000]; // plenty - to cover notes
    byte[] fieldName = new byte[50];
    int x=0, y, fieldCount=0;
    int len = fieldNamesQuote.length();
    while(x < len)
    {
      y=0;
      while(x < len && fieldNamesQuote.charAt(x) != ',')
        fieldName[y++] = (byte)fieldNamesQuote.charAt(x++);
      fieldName[y] = '\000';
      ++x;
      
      while(x < len && fieldNamesQuote.charAt(x) == ' ')
        ++x;

      if(searchDataString(data, recDataLen, "quote", fieldName, value) != -1) // entry exists
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
    generalUtils.bytesDPsGivenSeparator(true, '\001', '2',  data, 3000, 33); // groupDiscount
    generalUtils.bytesDPsGivenSeparator(true, '\001', '2',  data, 3000, 44); // baseTotalTotal
    generalUtils.bytesDPsGivenSeparator(true, '\001', '2',  data, 3000, 45); // baseGSTTotal

    byte[] date = new byte[20]; // lock
    generalUtils.dfsGivenSeparator(true, '\001', data, (short)1, date);

    byte[] javaScriptCallCode = new byte[5000]; // lock
    javaScriptCall(con, stmt, rs, javaScriptCallCode, req, generalUtils.stringFromBytes(date, 0L), unm, uty, dnm, localDefnsDir, defnsDir, bytesOut);

    String attachments = getAttachments(con, stmt, stmt2, rs, rs2, generalUtils.stringFromBytes(code, 0L), unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir);
    if(attachments.length() == 0)
      attachments= "none";

    scoutln(data, bytesOut, "quote.Attachments=" + attachments + "\001");

    prependDisplayOnly(con, stmt, rs, req, codeStr, generalUtils.stringFromBytes(date, 0L), unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, prependCode, bytesOut);

    screenLayout.bufferToBuffer(buf1, buf2, size1, size2, source, numFields, fieldNames, fieldTypes, fieldSizes, 'D', 'H', data, 3000, ddlData, ddlDataUpto[0], javaScriptCode, prependCode);

    char dpOnQuantities = miscDefinitions.dpOnQuantities(con, stmt, rs, "5");
    char dpOnUnitPrice  = miscDefinitions.dpOnUnitPrice(con, stmt, rs, "4");

    byte[] linesData = new byte[4000];
    int[] listLen = new int[1];  listLen[0] = 4000;
    int[] linesCount = new int[1];
    linesData = getLines(con, stmt, rs, code, '\001', linesData, listLen, linesCount, dnm, localDefnsDir, defnsDir);
    for(x=0;x<linesCount[0];++x)
    {
      if(generalUtils.getListEntryByNum(x, linesData, data)) // just-in-case
      {
        generalUtils.replaceTwosWithOnes(data);
        generalUtils.bytesDPsGivenSeparator(true, '\001', dpOnUnitPrice,  data, 4000, 4); // origin-1
        generalUtils.bytesDPsGivenSeparator(true, '\001', dpOnQuantities, data, 4000, 5);
        generalUtils.bytesDPsGivenSeparator(true, '\001', '2',            data, 4000, 6);
        generalUtils.bytesDPsGivenSeparator(true, '\001', '2',            data, 4000, 8);
        generalUtils.bytesDPsGivenSeparator(true, '\001', '2',            data, 4000, 10);
        
        generalUtils.dfsGivenSeparator(true, '\001', data, (short)10, b); // deliveryDate, origin-0
        generalUtils.convertFromYYYYMMDD(b); 
        generalUtils.repAlphaGivenSeparator('\001', data, 4000, (short)10, b);
        
        screenLayout.bufferToBuffer(buf1, buf2, size1, size2, source, numFields, fieldNames, fieldTypes, fieldSizes, 'D', 'L', data, 4000, ddlData, ddlDataUpto[0], javaScriptCallCode, null);
      }
    }

    append(con, stmt, rs, buf1, buf2, source, size1, size2, unm, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    screenLayout.bufferToOut(buf1, buf2, source, out);

    return true;
  }
    
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getAttachments(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String code, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir)
                               throws Exception
  {
    String attachments="";

    try
    {
      stmt = con.createStatement();
      String docCode;
      
      rs = stmt.executeQuery("SELECT LibraryDocCode FROM quotea WHERE Code = '" + code + "'");
      while(rs.next())           
      {    
        docCode = rs.getString(1);
        attachments += "<a href=\"http://" + men + "/central/servlet/LibraryDownloaCasual?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + docCode + "\">"
                    + libraryUtils.getDocumentName(docCode, dnm, localDefnsDir, defnsDir) + "</a><br>";
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
    
    return attachments;
  }  

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void outputPageFrame(Connection con, Statement stmt, ResultSet rs, byte[] b, HttpServletRequest req, boolean plain, char newOrEdit, char cad, String bodyStr, String service, String callingServlet, String date, String unm, String sid,
                               String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    int[] hmenuCount = new int[1];

    String subMenuText = dashboardUtils.buildSubMenuText(con, stmt, rs, req, service, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount);

    subMenuText += "<table id='title' width=100%><tr><td colspan=2 nowrap><p>Quotation" + directoryUtils.buildHelp(service) + "</td></tr></table>";

    subMenuText += buildSubMenuText(con, stmt, rs, req, cad, newOrEdit, callingServlet, date, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount);

    if(plain)
      scoutln(b, bytesOut, authenticationUtils.buildPlainScreen(con, stmt, rs, req, unm, sid, uty, men, den, dnm, bnm, callingServlet, subMenuText, hmenuCount[0], bodyStr, "", localDefnsDir, defnsDir));
    else scoutln(b, bytesOut, authenticationUtils.buildScreen(con, stmt, rs, req, true, unm, sid, uty, men, den, dnm, bnm, callingServlet, subMenuText, hmenuCount[0], bodyStr, "", localDefnsDir, defnsDir));

    scoutln(b, bytesOut, "<table border=0><tr><td valign=top><div id='second' style=\"{margin-top:0px;}\"></div></td>");
    scoutln(b, bytesOut, "<td valign=top><div id='third' style=\"{margin-top:0px;margin-left:0px;}\"></div></td></tr></table>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  public String buildSubMenuText(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, char cad, char newOrEdit, String callingServlet, String date, String unm, String sid, String uty, String men, String den, String dnm,
                                 String bnm, String localDefnsDir, String defnsDir, int[] hmenuCount) throws Exception
  {
    String s = "<div id='submenu'>";

    ++hmenuCount[0];

    if(callingServlet.equals("QuotationPage"))
      s += drawOptions4019(con, stmt, rs, req, hmenuCount, date, unm, uty, dnm, localDefnsDir, defnsDir);
    else
    if(callingServlet.equals("QuotationHeaderEdit"))      
      s += drawOptions4023(hmenuCount, cad, unm, sid, uty, men, den, dnm, bnm);
    else 
    if(callingServlet.equals("QuotationLine"))      
      s += drawOptions4025(hmenuCount, newOrEdit, unm, sid, uty, men, den, dnm, bnm);
    else
    if(callingServlet.equals("QuotationRegisteredUser"))
      s += drawOptions4122(con, stmt, rs, req, hmenuCount, unm, uty, dnm, localDefnsDir, defnsDir);
    
    s += "</div>";
    
    --hmenuCount[0];
    
    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String drawOptions4019(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, int[] hmenuCount, String date, String unm, String uty, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {      
    String s = "";

    s += "<div id='editModeDiv' style=visibility:visible>";

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4023, unm, uty, dnm, localDefnsDir, defnsDir) && serverUtils.passLockCheck(con, stmt, rs, "quote", date, unm))
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\"><a href=\"javascript:fetch()\">Edit Details</a></dt></dl>\n";

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4023, unm, uty, dnm, localDefnsDir, defnsDir) && serverUtils.passLockCheck(con, stmt, rs, "quote", date, unm)) // 4025
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\"><a href=\"javascript:add()\">Add New Line</a></dt></dl>\n";

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 6094, unm, uty, dnm, localDefnsDir, defnsDir) && serverUtils.passLockCheck(con, stmt, rs, "quote", date, unm))
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\"><a href=\"javascript:attach()\">Attachments</a></dt></dl>\n";

    boolean  QuotationPrint = authenticationUtils.verifyAccess(con, stmt, rs, req, 4028,  unm, uty, dnm, localDefnsDir, defnsDir);
    boolean  _4180 = authenticationUtils.verifyAccess(con, stmt, rs, req, 4180,  unm, uty, dnm, localDefnsDir, defnsDir);
    boolean  MailExternalUserCreate = authenticationUtils.verifyAccess(con, stmt, rs, req, 8016,  unm, uty, dnm, localDefnsDir, defnsDir);
    boolean PrintToPDFUtils = authenticationUtils.verifyAccess(con, stmt, rs, req, 11200, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean SendToCSVUtils = authenticationUtils.verifyAccess(con, stmt, rs, req, 11300, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean ChannelSendDirect = authenticationUtils.verifyAccess(con, stmt, rs, req, 12708, unm, uty, dnm, localDefnsDir, defnsDir);

    s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0] + "');\">Send</dt><dd id='hmenu" + hmenuCount[0]++ + "'><ul>";
    if(QuotationPrint || _4180 || MailExternalUserCreate || PrintToPDFUtils || SendToCSVUtils || ChannelSendDirect)
    {
      if(QuotationPrint)
        s += "<li><a href=\"javascript:print()\">Print</a></li>\n";
      if(_4180)
        s += "<li><a href=\"javascript:fax()\">Fax</a></li>\n";
      if(MailExternalUserCreate)
        s += "<li><a href=\"javascript:mail()\">Mail</a></li>";
      if(QuotationPrint)
        s += "<li><a href=\"javascript:pdf()\">PDF</a></li>\n";
      if(QuotationPrint)
        s += "<li><a href=\"javascript:csv()\">CSV</a></li>";
      if(ChannelSendDirect)
        s += "<li><a href=\"javascript:direct()\">Direct</a></li>";
    }

    s += "<li><a href=\"javascript:plain()\">Friendly</a></li></ul></dd></dl>\n";

    if(serverUtils.passLockCheck(con, stmt, rs, "quote", date, unm))
    {
      boolean GSTRatesChange = authenticationUtils.verifyAccess(con, stmt, rs, req, 6092, unm, uty, dnm, localDefnsDir, defnsDir);
      boolean PricgrChange = authenticationUtils.verifyAccess(con, stmt, rs, req, 6093, unm, uty, dnm, localDefnsDir, defnsDir);
      boolean DocumentChangeDiscounts = authenticationUtils.verifyAccess(con, stmt, rs, req, 6098, unm, uty, dnm, localDefnsDir, defnsDir);
      if(GSTRatesChange || PricgrChange || DocumentChangeDiscounts)
      {
        s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0] + "');\">Change</dt><dd id='hmenu" + hmenuCount[0]++ + "'><ul>";
        if(GSTRatesChange)
          s += "<li><a href=\"javascript:gst()\">Change GST</a></li>";
        if(PricgrChange)
          s += "<li><a href=\"javascript:prices()\">Change Prices</a></li>";
        if(DocumentChangeDiscounts)
          s += "<li><a href=\"javascript:discounts()\">Change Discounts</a></li>";
        s += "</ul></dd></dl>\n";
      }
    }

    boolean DocLibEdit2 = authenticationUtils.verifyAccess(con, stmt, rs, req, 4032, unm, uty, dnm, localDefnsDir, defnsDir); // 4033
    boolean DataBaseTab0 = authenticationUtils.verifyAccess(con, stmt, rs, req, 4020, unm, uty, dnm, localDefnsDir, defnsDir); // 4022
    boolean DataBaseMain5 = authenticationUtils.verifyAccess(con, stmt, rs, req, 4055, unm, uty, dnm, localDefnsDir, defnsDir); // 4055
    if(DocLibEdit2 || DataBaseTab0 | DataBaseMain5)
    {
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0] + "');\">Convert</dt>\n";
      s += "<dd id='hmenu" + hmenuCount[0]++ + "'><ul>";

      if(DataBaseTab0)
        s += "<li><a href=\"javascript:quote()\">Quotation</a></li>";
    
      s += "</ul></dd></dl>\n";
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 1025, unm, uty, dnm, localDefnsDir, defnsDir))
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\"><a href=\"javascript:trace()\">Trace</a></dt></dl>\n";

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 1025, unm, uty, dnm, localDefnsDir, defnsDir))
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\"><a href=\"javascript:trace2()\">Long Trace</a></dt></dl>\n";

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 11800, unm, uty, dnm, localDefnsDir, defnsDir))
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\"><a href=\"javascript:trail()\">Trail</a></dt></dl>\n";
    
    s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\"><a href=\"javascript:sortEntries()\">Entry Sort</a></dt></dl>\n";
    s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\"><a href=\"javascript:editMode()\">Edit Mode</a></dt></dl>\n";

    s += "</div>";

    s += "<div id='editModeDiv2' style=visibility:hidden>";
    s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\"><a href=\"javascript:editModeClose()\">Close Edit Mode</a></dt></dl>\n";

    s += "</div>";

    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String drawOptions4023(int[] hmenuCount, char cad, String unm, String sid, String uty, String men, String mainDNM, String dnm, String bnm) throws Exception
  {
    String s = "";
    
    s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\"><a href=\"javascript:save(' ')\">Save Changes</a></dt></dl>\n";

    if(cad != 'C')
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\"><a href=\"javascript:save('X')\">Cancel Changes</a></dt></dl>\n";

    s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\">";
    s += "<script language='Javascript'>";
    s += "var alreadyOnce=false;function select(){main.style.visibility='hidden';second.style.visibility='visible';"
      + "submenu.style.visibility='hidden';if(!alreadyOnce){select4230('A');alreadyOnce=true;}}";

    s += "var req2;";
    s += "function initRequest2(url)";
    s += "{if(window.XMLHttpRequest){req2=new XMLHttpRequest();}else ";
    s += "if(window.ActiveXObject){req2=new ActiveXObject(\"Microsoft.XMLHTTP\");}}";

    s += "function select4230(searchChar)";
    s += "{var url = \"http://" + men + "/central/servlet/CustomerSelect?unm=\" + escape('" + unm + "') + \"&sid=\" + escape('" + sid + "') + \"&uty=\" + escape('" + uty + "') + \"&men=\" + escape('" + men + "') + \"&den=\" + escape('" + mainDNM
      + "') + \"&bnm=\" + escape('" + bnm + "') + \"&p1=\"+searchChar+\"&dnm=\" + escape('" + dnm + "');";
    s += "initRequest2(url);";
    
    s += "req2.onreadystatechange=processRequest2;";
    s += "req2.open(\"GET\",url,true);";
    s += "req2.send(null);}";

    s += "function processRequest2()";
    s += "{if(req2.readyState==4)";
    s += "{if(req2.status == 200)";
    s += "{var res=req2.responseText;";
    s += "if(res.length > 0)";
    s += "{document.getElementById('second').innerHTML=res;";
    s += "}}}}";
      
    s += "</script><a href=\"javascript:select()\">Select Customer</a></dt></dl>\n";
    
    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private String drawOptions4025(int[] hmenuCount, char newOrEdit, String unm, String sid, String uty, String men, String mainDNM, String dnm, String bnm) throws Exception
  {
    String s = "";
    
    s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\"><a href=\"javascript:select()\">Select Item</a></dt></dl>\n";

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

    s += "<script language='Javascript'>";
    s += "var alreadyOnce=false;function select(){main.style.visibility='hidden';second.style.visibility='visible';"
      + "third.style.visibility='visible';submenu.style.visibility='hidden';if(!alreadyOnce){select2008('A');alreadyOnce=true;}}\n";

    s += "var req2;";
    s += "function initRequest2(url)";
    s += "{if(window.XMLHttpRequest){req2=new XMLHttpRequest();}else ";
    s += "if(window.ActiveXObject){req2=new ActiveXObject(\"Microsoft.XMLHTTP\");}}\n";

    s += "function select2008(searchChar)";
    s += "{var url = \"http://" + men + "/central/servlet/CatalogStockPage?unm=\" + escape('" + unm + "') + \"&sid=\" + escape('" + sid + "') + \"&uty=\" + escape('" + uty + "') + \"&men=\" + escape('" + men + "') + \"&den=\" + escape('" + mainDNM
      + "') + \"&bnm=\" + escape('" + bnm + "') + \"&p1=\"+searchChar+\"&dnm=\" + escape('" + dnm + "');";
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

    s += "var req3;";
    s += "function initRequest3(url)";
    s += "{if(window.XMLHttpRequest){req3=new XMLHttpRequest();}else ";
    s += "if(window.ActiveXObject){req3=new ActiveXObject(\"Microsoft.XMLHTTP\");}}\n";

    s += "function select2008a(mfr,operation,searchType,srchStr,firstNum,lastNum,maxRows,numRecs,firstCode,lastCode)";
    s += "{var url = \"http://" + men + "/central/servlet/CatalogItemsSection?unm=\" + escape('" + unm + "') + \"&sid=\" + escape('" + sid + "') + \"&uty=\" + escape('" + uty + "') + \"&men=\" + escape('" + men + "') + \"&den=\" + escape('" + mainDNM
      + "') + \"&bnm=\" + escape('" + bnm + "') + \"&p1=\"+escape(mfr)+\"&p2=\"+operation+\"&p3=\"+srchStr+\"&p4=\"+firstNum+\"&p5=\"+lastNum+"
      + "\"&p6=\"+maxRows+\"&p7=\"+numRecs+\"&p8=\"+firstCode+\"&p9=\"+lastCode+\"&p10=\"+searchType+\"&dnm=\" + escape('" + dnm + "');";
    s += "initRequest3(url);\n";
    
    s += "req3.onreadystatechange=processRequest3;";
    s += "req3.open(\"GET\",url,true);";
    s += "req3.send(null);}\n";

    s += "function processRequest3()";
    s += "{if(req3.readyState==4)";
    s += "{if(req3.status == 200)";
    s += "{var res=req3.responseText;";
    s += "if(res.length>0)";
    s += "{document.getElementById('third').innerHTML=res;";
    s += "}}}}\n";
    
    s += "function page(mfr,operation,firstNum,lastNum,firstCode,lastCode,topOrBottom,numRecs){";
    s += "var srchStr='';if(topOrBottom=='T')srchStr=document.forms[0].srchStr1.value;";
    s += "else srchStr=document.forms[0].srchStr2.value;";
    s += "var maxRows;if(topOrBottom=='T')maxRows=document.forms[0].maxRows1.value;";
    s += "else maxRows=document.forms[0].maxRows2.value;";
    s += "var firstCode1=sanitise(firstCode);var lastCode1=sanitise(lastCode);";
    s += "var searchType='C';if(topOrBottom=='T'){if(document.forms[0].searchType1[0].checked)searchType='D';}";
    s += "else {if(document.forms[0].searchType2[0].checked)searchType='D';}";
    s += "select2008a(mfr,operation,searchType,srchStr,firstNum,lastNum,maxRows,numRecs,firstCode1,lastCode1);}\n";

    s += "function sanitise(code){";
    s += "var code2='';var x;var len=code.length;";
    s += "for(x=0;x<len;++x)";
    s += "if(code.charAt(x)=='#')code2+='%23';";
    s += "else if(code.charAt(x)==\"'\")code2+='%27';";
    s += "else if(code.charAt(x)=='\"')code2+='%22';";
    s += "else if(code.charAt(x)=='&')code2+='%26';";
    s += "else if(code.charAt(x)=='%')code2+='%25';";
    s += "else if(code.charAt(x)==' ')code2+='%20';";
    s += "else if(code.charAt(x)=='?')code2+='%3F';";
    s += "else code2+=code.charAt(x);";
    s += "return code2};\n";

    s += "</script>";
    
    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String drawOptions4122(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, int[] hmenuCount, String unm, String uty, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {      
    String s  = "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\"><a href=\"javascript:print()\">Print</a></dt></dl>";
           s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\"><a href=\"javascript:csv()\">CSV</a></dt></dl>";
           s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\"><a href=\"javascript:pdf()\">PDF</a></dt></dl>";
           s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\"><a href=\"javascript:plain()\">Friendly</a></dt></dl>";
           s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\"><a href=\"javascript:trace()\">Trace</a></dt></dl>";
    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void putMultipleLine(Connection con, Statement stmt, ResultSet rs, byte[] code, int line, byte[] descData, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    try
    {
      stmt = con.createStatement();
    
      String sanitisedDesc = generalUtils.dfsAsStr(descData, (short)3);
      String opStr = "";
      int len = sanitisedDesc.length();
      for(int x=0;x<len;++x)
      {
        if(sanitisedDesc.charAt(x) == '\'')
          opStr += "''"; 
        else
        if(sanitisedDesc.charAt(x) == '"')
          opStr += "''''"; 
        else opStr += sanitisedDesc.charAt(x);        
      }
      
      stmt.executeUpdate("INSERT INTO quotell (" + getFieldNamesQuoteLL() + ") VALUES ('" + generalUtils.stringFromBytes(code, 0L) + "','" + line + "','" + generalUtils.dfsAsStr(descData, (short)2)  + "','" + opStr + "')");
      
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println(e);  
      try
      {
        if(stmt != null) stmt.close();
      }
      catch(Exception e2) { }
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public int getGSTRates(Connection con, Statement stmt, ResultSet rs, String[][] gstRateNames, double[][] gstRates, int[] allGSTLens)
  {
    try
    {
      stmt = con.createStatement();
      
      rs = stmt.executeQuery("SELECT GSTRateName, GSTRate FROM gstrate");
      
      int x=0;
      
      while(rs.next())
      {
        if(x == allGSTLens[0])
        {
          int y;
          String[] tmp = new String[allGSTLens[0]];
          for(y=0;y<allGSTLens[0];++y)
            tmp[y] = gstRateNames[0][y];
          allGSTLens[0] += 10;
          gstRateNames[0] = new String[allGSTLens[0]];
          for(y=0;y<(allGSTLens[0]-10);++y)
            gstRateNames[0][y] = tmp[y];
  
          allGSTLens[0] -= 10;
          double[] tmpd = new double[allGSTLens[0]];
          for(y=0;y<allGSTLens[0];++y)
            tmpd[y] = gstRates[0][y];
          allGSTLens[0] += 10;
          gstRates[0] = new double[allGSTLens[0]];
          for(y=0;y<(allGSTLens[0]-10);++y)
            gstRates[0][y] = tmpd[y];
        }
        
        gstRateNames[0][x] = rs.getString(1);
        gstRates[0][x]     = generalUtils.doubleFromStr(rs.getString(2));
        ++x;
      }
      
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
            
      return x;
    }
    catch(Exception e)
    {
      System.out.println("Quotation: " + e);
      try
      {
        if(rs   != null) rs.close();     
        if(stmt != null) stmt.close();
      }
      catch(Exception e2) { }
    }
    
    return 0;
  }
 
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public double getGSTRate(String[] gstNames, double[] gstRates, int allGSTLens, byte[] b) throws Exception
  {
    int x=0;
    
    while(x < allGSTLens)
    {
      if(generalUtils.match(b, gstNames[x]))
        return gstRates[x] / 100;
      ++x;
    }
    
    return 0.0; // just-in-case
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(byte[] b, int bytesOut[], String str) throws Exception
  {      
    generalUtils.catAsBytes(str, 0, b, false);
    bytesOut[0] += (str.length() + 2);    
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void updateDiscounts(Connection con, Statement stmt, ResultSet rs, byte[] code, String discount, String which, String unm, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    byte[] buf  = new byte[5000];

    byte[] rateB = new byte[30];
    getAQuoteFieldGivenCode(con, stmt, rs, "Rate", code, rateB);
    double rate = generalUtils.doubleFromBytesCharFormat(rateB, 0);
 
    updateDLM(con, stmt, rs, code, unm, dnm, localDefnsDir, defnsDir);

    int y, z;
    byte[] b          = new byte[21];
    byte[] line       = new byte[500];
    byte[] linesData  = new byte[2000];
    int[]  listLen    = new int[1];  listLen[0] = 2000;
    int[]  linesCount = new int[1];
    boolean isAStockItem, wanted;

    linesData = getLines(con, stmt, rs, code, '\000', linesData, listLen, linesCount, dnm, localDefnsDir, defnsDir);

    for(int x=0;x<linesCount[0];++x)
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

        if(which.equals("A"))
          wanted = true;
        else
        {
          isAStockItem = inventory.isAStockItem(con, stmt, rs, generalUtils.dfsAsStr(buf, (short)1));
          if((isAStockItem && which.equals("S")) || (! isAStockItem && which.equals("N")))
            wanted = true;
          else wanted = false;
        }

        if(wanted)
        {
          generalUtils.repAlpha(buf, 5000, (short)7, discount);
          
          reCalculate(buf, z, rate);

          generalUtils.repAlpha(buf, 5000, (short)17, unm);
          generalUtils.repAlpha(buf, 5000, (short)8, '\000');

          generalUtils.dfs(buf, (short)11, b); // line

          putRecLine(con, stmt, rs, code, b, 'E', buf, dnm, localDefnsDir, defnsDir);
        }
      }
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // format of data: "do.docode=12345/DO\001 ..."
  public String searchDataString(byte[] data, int lenData, String tableName, String fieldName)
  {
      try{
    byte[] fieldNameB = new byte[50];
    generalUtils.strToBytes(fieldNameB, fieldName);
    
    byte[] valueB = new byte[300]; // plenty
    if(searchDataString(data, lenData, tableName, fieldNameB, valueB) == -1)
      return "";
    
    return generalUtils.stringFromBytes(valueB, 0L);
      }
      catch(Exception e) { return ""; }
  }
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

}
