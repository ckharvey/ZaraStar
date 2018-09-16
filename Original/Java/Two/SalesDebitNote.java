// =======================================================================================================================================================================================================
// System: ZaraStar Document: DN Record Access
// Module: salesDebitNote.java
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

public class SalesDebitNote
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

  // -------------------------------------------------------------------------------------------------------------------------------
  public String getTableCreateStringDebitNote() throws Exception
  {  
    return "debit ( DNCode char(20) not null, Date date not null,         CompanyCode char(20) not null, "
                 + "CompanyName char(60),     Address1 char(40),          Address2 char(40), "
                 + "Address3 char(40),        Address4 char(40),          Address5 char(40), "
                 + "PostCode char(20),        FAO char(40),               Notes char(250), "
                 + "Attention char(1),        unused1 date,               DateProcessed date, "
                 + "Misc1 char(80),           Misc2 char(80),             GSTTotal decimal(19,8), "
                 + "TotalTotal decimal(19,8), AmountPaid decimal(19,8),   Settled char(1), "
                 + "Processed char(1),        SignOn char(20),            GroupDiscount decimal(19,8), "
                 + "InvoiceCode char(20),     PORefNum char(40),          GroupDiscountType char(1), "
                 + "CashOrAccount char(1),    DateLastModified timestamp, ProjectCode char(20), "
                 + "SalesPerson char(40),     unused3 integer,            unused4 decimal(19,8), "
                 + "unused5 decimal(19,8),    CommissionPaid char(1),     CommissionValue decimal(19,8), "
                 + "unused6 char(1),          PaymentReference char(20),  ProfitCentre char(20), "
                 + "DatePaid date,            ShipAddrCode char(20),      PrintCount integer, "
                 + "Status char(1),           unused7 date,               unused8 date, "
                 + "RevisionOf char(20),      LocationCode char(20),      BaseGSTTotal decimal(19,8), "
                 + "Currency char(3),         Rate decimal(16,8),         ShipName char(60), "
                 + "ShipAddress1 char(40),    ShipAddress2 char(40),      ShipAddress3 char(40),  "
                 + "ShipAddress4 char(40),    ShipAddress5 char(40),      BaseTotalTotal decimal(19,8), "
                 + "OCCode char(20),          Misc3 char(60),             Misc4 char(60), "
                 + "Terms char(20),           ExportStatement char(40),   Closed char(1), "
                 + "Reference char(40), "
                 + "unique(DNCode))";
  } 

  // -------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStringsDebitNote(String[] s) throws Exception
  {
    s[0] = "debitDateInx on debit (Date)";
    s[1] = "debitCompanyCodeInx on debit (CompanyCode)";
    
    return 2;
  }
      
  // -------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNamesDebitNote() throws Exception
  {  
    return "DNCode, Date, CompanyCode, CompanyName, Address1, Address2, Address3, Address4, Address5, PostCode, FAO, Notes, "
         + "Attention, unused1, DateProcessed, Misc1, Misc2, GSTTotal, TotalTotal, AmountPaid, Settled, Processed, SignOn, "
         + "GroupDiscount, InvoiceCode, PORefNum, GroupDiscountType, CashOrAccount, DateLastModified, ProjectCode, SalesPerson, "
         + "unused3, unused4, unused5, CommissionPaid, CommissionValue, unused6, PaymentReference, ProfitCentre, DatePaid , "
         + "ShipAddrCode, PrintCount, Status, unused7, unused8, RevisionOf, LocationCode, BaseGSTTotal, Currency, Rate, ShipName, "
         + "ShipAddress1, ShipAddress2, ShipAddress3, ShipAddress4, ShipAddress5, BaseTotalTotal, OCCode, Misc3, Misc4, Terms, "
         + "ExportStatement, Closed, Reference";
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypesDebitNote() throws Exception
  {
    return "CDCCCCCCCCCCCDDCCFFFCCCFCCCCSCCIFFCFCCCDCCCDDCCFCFCCCCCCFCCCCCCC";
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizesDebitNote(short[] sizes) throws Exception
  {
    sizes[0]  = 20;  sizes[1]  = 0;   sizes[2]  = 20;  sizes[3]  = 60;  sizes[4] = 40;   sizes[5] = 40;   sizes[6] = 40;
    sizes[7]  = 40;  sizes[8]  = 40;  sizes[9]  = 20;  sizes[10] = 40;  sizes[11] = 250; sizes[12] = 1;   sizes[13] = 0;
    sizes[14] = 0;   sizes[15] = 80;  sizes[16] = 80;  sizes[17] = 0;   sizes[18] = 0;   sizes[19] = 0;   sizes[20] = 1;
    sizes[21] = 1;   sizes[22] = 20;  sizes[23] = 0;   sizes[24] = 20;  sizes[25] = 40;  sizes[26] = 1;   sizes[27] = 1;
    sizes[28] = -1;  sizes[29] = 20;  sizes[30] = 40;  sizes[31] = 0;   sizes[32] = 0;   sizes[33] = 0;   sizes[34] = 1;
    sizes[35] = 0;   sizes[36] = 1;   sizes[37] = 20;  sizes[38] = 20;  sizes[39] = 0;   sizes[40] = 20;  sizes[41] = 0;
    sizes[42] = 1;   sizes[43] = 0;   sizes[44] = 0;   sizes[45] = 20;  sizes[46] = 20;  sizes[47] = 0;   sizes[48] = 3;
    sizes[49] = 0;   sizes[50] = 20;  sizes[51] = 40;  sizes[52] = 40;  sizes[53] = 40;  sizes[54] = 40;  sizes[55] = 40;
    sizes[56] = 0;   sizes[57] = 20;  sizes[58] = 60;  sizes[59] = 60;  sizes[60] = 20;  sizes[61] = 40;  sizes[62] = 1;
    sizes[63] = 40;
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public String getFieldStylesDebitNote() throws Exception
  {
    return "MMMOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO";
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public String getTableCreateStringDebitNoteL() throws Exception
  {  
    return "debitl ( DNCode char(20) not null, ItemCode char(20) not null,     Description char(80), "
                  + "UnitPrice decimal(17,8),  Quantity decimal(19,8),         Amount decimal(17,8), "
                  + "GSTRate char(20),         Discount decimal(17,8),         DateLastModified timestamp, "
                  + "SignOn char(20),          Amount2 decimal(17,8),          ProfitCentre char(20), "
                  + "AccCode char(20),         AppliedStatus char(1),          StockUpdated char(1), "
                  + "DateOfStockUpdate date,   QuantityRequired decimal(19,8), CustomerItemCode char(40), "
                  + "UoM char(20),             Line integer not null,          Entry char(6), "
                  + "Unused char(40),          ReceivedAmount decimal(17,8),   RefundedAmount decimal(17,8), "
                  + "AltItemCode char(20),     PORefNum char(40),              DORefNum char(20), "
                  + "CostPrice decimal(17,8),  Manufacturer char(30),          ManufacturerCode char(60), "
                  + "unique(DNCode, Line))";
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStringsDebitNoteL(String[] s) throws Exception
  {
    s[0] = "debitlItemCodeInx on debitl(ItemCode)";
    
    return 1;
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNamesDebitNoteL() throws Exception
  {  
    return "DNCode, ItemCode, Description, UnitPrice, Quantity, Amount, GSTRate, Discount, DateLastModified, SignOn, Amount2, "
         + "ProfitCentre, AccCode, AppliedStatus, StockUpdated, DateOfStockUpdate, QuantityRequired, CustomerItemCode, UoM, Line, "
         + "Entry, Unused, ReceivedAmount, RefundedAmount, AltItemCode, PORefNum, DORefNum, CostPrice, Manufacturer, ManufacturerCode";
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypesDebitNoteL() throws Exception
  {
    return "CCCFFFCFSCFCCCCDFCCICCFFCCCFCC";
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizesDebitNoteL(short[] sizes) throws Exception
  {
    sizes[0] = 20;   sizes[1] = 20;   sizes[2] = 80;   sizes[3] = 0;    sizes[4] = 0;    sizes[5] = 0;    sizes[6] = 20;
    sizes[7] = 0;    sizes[8] = -1;   sizes[9] = 20;   sizes[10] = 0;   sizes[11] = 20;  sizes[12] = 20;  sizes[13] = 1;
    sizes[14] = 1;   sizes[15] = 0;   sizes[16] = 0;   sizes[17] = 40;  sizes[18] = 20;  sizes[19] = 0;   sizes[20] = 6;
    sizes[21] = 40;  sizes[22] = 0;   sizes[23] = 0;   sizes[24] = 20;  sizes[25] = 40;  sizes[26] = 20;  sizes[27] = 0;
    sizes[28] = 30;  sizes[29] = 60;
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public String getFieldStylesDebitNoteL() throws Exception
  {
    return "OOOOOOOOOOOOOOOOOOOMOOOOOOOOOO";
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public String getTableCreateStringDebitNoteLL() throws Exception
  {  
    return "debitll ( DNCode char(20) not null,   Entry char(6) not null,   Line integer not null,   Text char(80), "
                    + "unique(DNCode, Entry, Line))";
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStringsDebitNoteLL(String[] s) throws Exception
  {
    s[0]="\0"; // remove compiler warning
    return 0;
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNamesDebitNoteLL() throws Exception
  {  
    return "DNCode, Entry, Line, Text";
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypesDebitNoteLL() throws Exception
  {
    return "CCIC";
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizesDebitNoteLL(short[] sizes) throws Exception
  {
    sizes[0] = 20;   sizes[1] = 6;   sizes[2] = 0;   sizes[3] = 80;
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public String getFieldStylesDebitNoteLL() throws Exception
  {
    return "MMMO";
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public String getTableCreateStringDebitNoteA() throws Exception
  {  
    return "debita ( Code char(20) not null, LibraryDocCode integer not null, unique(Code, LibraryDocCode))";
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStringsDebitNoteA(String[] s) throws Exception
  {
    s[0]="\0"; // remove compiler warning
    return 0;
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNamesDebitNoteA() throws Exception
  {  
    return "Code, LibraryDocCode";
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypesDebitNoteA() throws Exception
  {
    return "CI";
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizesDebitNoteA(short[] sizes) throws Exception
  {
    sizes[0] = 20;   sizes[1] = 0;
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public String getFieldStylesDebitNoteA() throws Exception
  {
    return "MM";
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public boolean getRecToHTML(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, char dispOrEdit, byte[] code, String unm, String sid, String uty,
                              String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, char cad,
                              String errStr, byte[] dataAlready, HttpServletRequest req, int[] bytesOut) throws Exception
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
    byte[] prependCode = new byte[20000];
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
      messagePage.msgScreen(false, out, req, 6, unm, sid, uty, men, den, dnm, bnm, "SalesDebitNote", imagesDir, localDefnsDir,
                         defnsDir, bytesOut);
      return true;
    }

    if(dataAlready[0] != '\000') // coming with an err msg - passing-in data already entered but the data's fields may (will) not be
    {                            // in the correct order
      dispOrEdit = 'E';
      sortFields(dataAlready, headData, "debit");
    }
    else // get header data
    {
      if(cad == 'A')
      {
        if(getRecGivenCode(con, stmt, rs, code, '\001', headData, dnm, localDefnsDir, defnsDir, bytesOut) == -1)
        {
          messagePage.msgScreen(false, out, req, 6, unm, sid, uty, men, den, dnm, bnm, "SalesDebitNote", imagesDir, localDefnsDir,
                             defnsDir, bytesOut);
          return true;
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

    byte[] javaScriptCallCode = new byte[1000]; // lock
    javaScriptCall(con, stmt, rs, javaScriptCallCode, req, date, unm, uty, dnm, localDefnsDir, defnsDir, bytesOut);

    short[] fieldSizesDebitNote = new short[70]; // plenty
    getFieldSizesDebitNote(fieldSizesDebitNote);

    if(cad == 'A') // not a new one
    {
      short[] fieldSizesDebitNoteL = new short[30]; // plenty
      getFieldSizesDebitNoteL(fieldSizesDebitNoteL);

      double amt, amt2, gstAmt, gstAmt2, totalAmtLines, totalGSTAmtLines, totalGSTAmt2Lines, totalAmt2Lines, groupDiscount, gstRate;
      totalAmt2Lines = totalAmtLines = totalGSTAmtLines = totalGSTAmt2Lines = totalAmt2Lines = groupDiscount = 0.0;
      char groupDiscountType;

      if(dispOrEdit == 'D')
      {  
        numFields = screenLayout.layoutToBuffer(buf1, buf2, size1, fieldNames, fieldTypes, fieldSizes, localDefnsDir, defnsDir, "228.htm", 2, getFieldNamesDebitNote(), fieldSizesDebitNote,
                             getFieldNamesDebitNoteL(), fieldSizesDebitNoteL, null, null);
      }
      else
      {
        numFields = screenLayout.layoutToBuffer(buf1, buf2, size1, fieldNames, fieldTypes, fieldSizes, localDefnsDir, defnsDir, "228a.htm", 1, getFieldNamesDebitNote(), fieldSizesDebitNote, null, null,
                             null, null);
      }
              
      if(dispOrEdit == 'D')
      {
        prepend(con, stmt, rs, unm, sid, uty, men, den, dnm, bnm, generalUtils.stringFromBytes(code, 0L), date, prependCode, req, localDefnsDir,
                defnsDir, bytesOut);
      }
      else
      {
        prependEdit(con, stmt, rs, unm, sid, uty, men, den, dnm, bnm, errStr, "A", generalUtils.stringFromBytes(code, 0L), date, "228a.htm", 
                    localDefnsDir, defnsDir, prependCode, req, bytesOut);
      }

      screenLayout.bufferToBuffer(buf1, buf2, size1, size2, source, numFields, fieldNames, fieldTypes, fieldSizes, dispOrEdit, 'X', data, 0, ddlData, ddlDataUpto[0], javaScriptCode, prependCode);

      if(dispOrEdit == 'D') // display head *and* lines
      {
        // need to pickup groupDiscount

        generalUtils.dfsGivenSeparator(true, '\001', headData, (short)23, b);
        groupDiscount = generalUtils.doubleFromChars(b);

        generalUtils.dfsGivenSeparator(true, '\001', headData, (short)27, b);
        groupDiscountType = (char)b[0];

        char dpOnQuantities = miscDefinitions.dpOnQuantities(con, stmt, rs, "5");
        char dpOnUnitPrice  = miscDefinitions.dpOnUnitPrice(con, stmt, rs, "4");

        byte[] multipleLinesData = new byte[1000];
        int[]  multipleListLen = new int[1]; multipleListLen[0] = 1000;
        int[]  multipleLinesCount = new int[1];
        byte[] llData = new byte[200];
        multipleLinesData = getMultipleLines(con, stmt, rs, code, multipleLinesData, multipleListLen, multipleLinesCount, dnm, localDefnsDir, defnsDir);

        boolean isAtLeastOneLineWithGST = false;
        byte[] line      = new byte[20];
        byte[] linesData = new byte[5000];
        int[]  listLen = new int[1];  listLen[0] = 5000;
        int[]  linesCount = new int[1];
        linesData = getLines(con, stmt, rs, code, '\001', linesData, listLen, linesCount, dnm, localDefnsDir, defnsDir);

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
              generalUtils.dfsGivenSeparator(true, '\001', data, (short)11, b); // amt2, origin-1
              totalAmt2Lines += generalUtils.doubleDPs(generalUtils.doubleFromChars(b), '2');

              generalUtils.dfsGivenSeparator(true, '\001', data, (short)7, b); // gstrate
              if(generalUtils.lengthBytes(b, 0) != 0) // rate specified
              {
                if(accountsUtils.getGSTRate(con, stmt, rs, b) != 0)
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

            generalUtils.dfsGivenSeparator(true, '\001', data, (short)11, b); // origin-1
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
              gstRate = accountsUtils.getGSTRate(con, stmt, rs, b);
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
            generalUtils.bytesDPsGivenSeparator(true, '\001', '2', data, 5000, 11); // amount2
            generalUtils.bytesDPsGivenSeparator(true, '\001', dpOnUnitPrice,  data, 5000, 4);  // unitprice
            generalUtils.bytesDPsGivenSeparator(true, '\001', '2',  data, 5000, 8);  // discount

            generalUtils.dfsGivenSeparator(true, '\001', data, (short)20, line); // origin-1

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
          totalAmtLines  = totalAmt2Lines * generalUtils.dfsAsDoubleGivenSeparator(true, '\001', headData, (short)49);
        }
  
        totalGSTAmt2Lines = generalUtils.doubleDPs(totalGSTAmt2Lines, '2');
        totalGSTAmtLines  = generalUtils.doubleDPs(totalGSTAmtLines, '2');

        stmt = con.createStatement();
        stmt.executeUpdate("UPDATE debit SET TotalTotal = " + (totalAmt2Lines + totalGSTAmt2Lines) + ", GSTTotal = " 
                         + totalGSTAmt2Lines + ", BaseTotalTotal = " + (totalAmtLines + totalGSTAmtLines) + ", BaseGSTTotal = "
                         + totalGSTAmtLines + " WHERE DNCode = '" + generalUtils.stringFromBytes(code, 0L) + "'");
        stmt.close();
        
        generalUtils.repDoubleGivenSeparator('2', '\001', headData, 5000, (short)17, totalGSTAmt2Lines);
        generalUtils.repDoubleGivenSeparator('2', '\001', headData, 5000, (short)18, (totalAmt2Lines + totalGSTAmt2Lines));

        scoutln(headData, bytesOut, "debit.subtotalbeforegst=" + generalUtils.doubleToStr(totalAmt2Lines) + "\001"); // subtotal on screen

        generalUtils.repDoubleGivenSeparator('2', '\001', headData, 5000, (short)56, (totalAmtLines + totalGSTAmtLines));
        generalUtils.repDoubleGivenSeparator('2', '\001', headData, 5000, (short)47, totalGSTAmtLines);

        generalUtils.bytesDPsGivenSeparator(true, '\001', '2', headData, 5000, 23); // group discount

        scoutln(headData, bytesOut, "debit.basecurrency=" + accountsUtils.getBaseCurrency(con, stmt, rs, dnm, localDefnsDir, defnsDir) + "\001");

        generalUtils.bytesDPsGivenSeparator(true, '\001', '2',  headData, 5000, 49); // rate
        scoutln(headData, bytesOut, "debit.rateNoTrailing=" + generalUtils.dfsAsStrGivenBinary1(true, headData, (short)49) + "\001");

        String attachments = getAttachments(con, stmt, stmt2, rs, rs2, generalUtils.stringFromBytes(code, 0L), unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir);
        if(attachments.length() == 0)
          attachments= "none";
        scoutln(headData, bytesOut, "debit.Attachments=" + attachments + "\001");
      }
      else // edit
      {
        ddlData = accountsUtils.getCurrencyNamesDDLData(con, stmt, rs, "debit.Currency", dnm, ddlData, ddlDataUpto, ddlDataLen, localDefnsDir, defnsDir);
        ddlData = documentUtils.getSalesPersonDDLData(con, stmt, rs, "debit.SalesPerson", ddlData, ddlDataUpto, ddlDataLen);
        ddlData = documentUtils.getCountryDDLData(con, stmt, rs, "debit.Country", ddlData, ddlDataUpto, ddlDataLen);

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

      generalUtils.bytesDPsGivenSeparator(true, '\001', '2',  headData, 5000, 23); // groupDiscount

      screenLayout.bufferToBuffer(buf1, buf2, size1, size2, source, numFields, fieldNames, fieldTypes, fieldSizes, dispOrEdit, 'H', headData, 5000, ddlData, ddlDataUpto[0], null, null);

      append(con, stmt, rs, buf1, buf2, source, size1, size2, unm, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

      screenLayout.bufferToOut(buf1, buf2, source, out);

      rtn = true;
    }
    else // a new layout
    {
      numFields = screenLayout.layoutToBuffer(buf1, buf2, size1, fieldNames, fieldTypes, fieldSizes, localDefnsDir, defnsDir, "228a.htm", 1, getFieldNamesDebitNote(), fieldSizesDebitNote, null, null,
                           null, null);

      if(dataAlready[0] != '\000') // coming with an err msg - passing-in data already entered but the data's fields may (will) not
      {                            // be in the correct order
        sortFields(dataAlready, data, "debit");
      }  
      else
      {
        screenLayout.buildNewDetails(fieldNames, numFields, "debit", data);

        documentUtils.getNextCode(con, stmt, rs, "dn", true, code);

        generalUtils.repAlphaUsingOnes(data, 5000, "DNCode", generalUtils.stringFromBytes(code, 0L));
        generalUtils.repAlphaUsingOnes(data, 5000, "Status", "L");
        generalUtils.repAlphaUsingOnes(data, 5000, "Currency", accountsUtils.getBaseCurrency(con, stmt, rs, dnm, localDefnsDir, defnsDir));

        generalUtils.repAlphaUsingOnes(data, 5000, "Date", generalUtils.today(localDefnsDir, defnsDir));
      }

      ddlData = accountsUtils.getCurrencyNamesDDLData(con, stmt, rs, "debit.Currency", dnm, ddlData, ddlDataUpto, ddlDataLen, localDefnsDir, defnsDir);
      ddlData = documentUtils.getSalesPersonDDLData(con, stmt, rs, "debit.SalesPerson", ddlData, ddlDataUpto, ddlDataLen);
      ddlData = documentUtils.getCountryDDLData(con, stmt, rs, "debit.Country", ddlData, ddlDataUpto, ddlDataLen);

      prependEdit(con, stmt, rs, unm, sid, uty, men, den, dnm, bnm, errStr, "C", generalUtils.stringFromBytes(code, 0L), date, "228a.htm",
                  localDefnsDir, defnsDir, prependCode, req, bytesOut);

      screenLayout.bufferToBuffer(buf1, buf2, size1, size2, source, numFields, fieldNames, fieldTypes, fieldSizes, 'E', 'H', data, 5000, ddlData, ddlDataUpto[0], javaScriptCode, prependCode);

      append(con, stmt, rs, buf1, buf2, source, size1, size2, unm, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

      screenLayout.bufferToOut(buf1, buf2, source, out);
      rtn = true;
    }

    return rtn;
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void javaScriptCall(Connection con, Statement stmt, ResultSet rs, byte[] b, HttpServletRequest req, String date, String unm, String uty, String dnm, String localDefnsDir, String defnsDir, int[] bytesOut)
                              throws Exception
  {
    b[0]= '\000';
    scoutln(b, bytesOut, "_.lineFile=debitl.line\001");
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4117, unm, uty, dnm, localDefnsDir, defnsDir) && serverUtils.passLockCheck(con, stmt, rs, "debit", date, unm))
      scoutln(b, bytesOut, "_.permitted=y\001");
    else scoutln(b, bytesOut, "_.permitted=n\001");
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void javaScript(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, String code, String unm, String sid, String uty, String men,
                          String den, String dnm, String bnm, byte[] b, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4115, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      b[0]= '\000';
      scoutln(b, bytesOut, "<script language=\"JavaScript\">");
      scoutln(b, bytesOut, "function affect(line){");
      scoutln(b, bytesOut, "window.location.replace(\"/central/servlet/SalesDebitNoteLine?unm=" + unm + "&sid=" + sid
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

  // -------------------------------------------------------------------------------------------------------------------------------
  private void prepend(Connection con, Statement stmt, ResultSet rs, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                       String code, String date, byte[] b, HttpServletRequest req, String localDefnsDir, String defnsDir, int[] bytesOut)
                       throws Exception
  {
    String code2 = code; 

    b[0]= '\000';
    scoutln(b, bytesOut, "<html><head><title>" + code + "</title>");
    scoutln(b, bytesOut, "<script language=\"JavaScript\">\n");

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4113, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(b, bytesOut, "function fetch(){");
      scoutln(b, bytesOut, "window.location.replace(\"/central/servlet/SalesDebitNoteHeaderEdit?unm=" + unm + "&sid=" + sid + "&uty="
                           + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + code2
                           + "&p2=A&p3=&p4=\");}\n");

      scoutln(b, bytesOut, "function gst(){");
      scoutln(b, bytesOut, "window.location.href=\"/central/servlet/GSTRatesChange?unm=" + unm + "&sid=" + sid + "&uty="
                       + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + code2 + "&p2=DN\";}");

      if(authenticationUtils.verifyAccess(con, stmt, rs, req, 6093, unm, uty, dnm, localDefnsDir, defnsDir))
      {
        scoutln(b, bytesOut, "function prices(){");
        scoutln(b, bytesOut, "window.location.href=\"/central/servlet/PricgrChange?unm=" + unm + "&sid=" + sid
                           + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + code2
                           + "&p2=DN\";}");
      }

      if(authenticationUtils.verifyAccess(con, stmt, rs, req, 6098, unm, uty, dnm, localDefnsDir, defnsDir))
      {
        scoutln(b, bytesOut, "function discounts(){");
        scoutln(b, bytesOut, "window.location.href=\"/central/servlet/DocumentChangeDiscounts?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den="
                           + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + code2 + "&p2=DN\";}");
      }
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4115, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(b, bytesOut, "function add(){");
      scoutln(b, bytesOut, "window.location.replace(\"/central/servlet/SalesDebitNoteLine?unm=" + unm + "&sid=" + sid
                         + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + code2
                         + "&p2=&p3=&p4=\");}");
    }
    
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 1025, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(b, bytesOut, "function trace(){");
      scoutln(b, bytesOut, "window.location.replace(\"/central/servlet/DocumentTraceExecute?unm=" + unm + "&sid=" + sid
                         + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + code2
                         + "&p2=N\");}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 1025, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(b, bytesOut, "function trace2(){");
      scoutln(b, bytesOut, "window.location.replace(\"/central/servlet/DocumentTraceExecute?unm=" + unm + "&sid=" + sid
                         + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + code2
                         + "&p2=N&bta=Y\");}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 6094, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(b, bytesOut, "function attach(){");
      scoutln(b, bytesOut, "window.location.replace(\"/central/servlet/DocumentAttachments?unm=" + unm + "&sid=" + sid
                         + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + code2
                         + "&p2=debita\");}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 8016, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(b, bytesOut, "function mail(){");
      scoutln(b, bytesOut, "window.location.replace(\"/central/servlet/MailExternalUserCreate?unm=" + unm + "&sid=" + sid
                         + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=SalesDebitNotePage&p2=" + code2
                         + "&p3=Debit%20Note&bnm=" + bnm + "\");}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 11800, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(b, bytesOut, "function trail(){");
      scoutln(b, bytesOut, "window.location.href=\"/central/servlet/TrailShow?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men="
                         + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + code2 + "&p2=N\";}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4118, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(b, bytesOut, "function print(){");
      scoutln(b, bytesOut, "window.location.href=\"/central/servlet/AdminPrintControl?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=SalesDebitNotePrint&p2=" + code2 + "&p3=\";}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4188, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(b, bytesOut, "function fax(){");
      scoutln(b, bytesOut, "window.location.href=\"/central/servlet/_4188?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + code2 + "&p2=Debit%20Note&p3=SalesDebitNotePage\";}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 11200, unm, uty, dnm, localDefnsDir, defnsDir) && authenticationUtils.verifyAccess(con, stmt, rs, req, 4118, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(b, bytesOut, "function pdf(){");
      scoutln(b, bytesOut, "window.location.href=\"/central/servlet/PrintToPDFUtils?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p3=" + code2 + "&p1=0.000&p5=DebitNote&p2=SalesDebitNotePrint\";}");
    }

    String cssDirectory = directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir);
    String cssDocumentDirectory = directoryUtils.getCssDocumentDirectory(con, stmt, rs, dnm);
    scoutln(b, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + cssDirectory + "general.css\">\n");
    scoutln(b, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + cssDocumentDirectory + "document.css\">\n");
    
    outputPageFrame(con, stmt, rs, b, req, ' ', ' ', "", "SalesDebitNotePage", date, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    scoutln(b, bytesOut, "<form>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void prependEdit(Connection con, Statement stmt, ResultSet rs, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                           String errStr, String cad, String code, String date, String layoutFile, String localDefnsDir, String defnsDir,
                           byte[] b, HttpServletRequest req, int[] bytesOut) throws Exception
  {
    String code2 = generalUtils.sanitise(code);

    scoutln(b, bytesOut, "<html><head><title>" + code + "</title>");
    scoutln(b, bytesOut, "<script language=\"JavaScript\">\n");

    byte[] sourceBuf = new byte[5000];
    sourceBuf = htmlBuild.layoutToBuffer(sourceBuf, 5000, localDefnsDir, defnsDir, layoutFile);
    String saveStr = htmlBuild.buildSaveStringDelimited(sourceBuf, "document.forms[0]");

    scoutln(b, bytesOut, "function setCode(code){document.forms[0].CompanyCode.value=code;");
    scoutln(b, bytesOut, "main.style.visibility='visible';second.style.visibility='hidden';second.style.height='0';submenu.style.visibility='visible';}");

    scoutln(b, bytesOut, "function save(thisOp){var saveStr=\"\";" + saveStr + "\n");
    scoutln(b, bytesOut, "var saveStr2=strip(saveStr);window.location.replace(\"/central/servlet/SalesDebitNoteHeaderUpdate?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1="
                       + cad + "&p2=" + code2 + "&p3=\"+saveStr2+\"&p4=\"+thisOp)}\n");
    scoutln(b, bytesOut, "function strip(saveStr){\n");
    scoutln(b, bytesOut, "var saveStr2='';var x;var len=saveStr.length;\n");
    scoutln(b, bytesOut, "for(x=0;x<len;++x)\n");
    scoutln(b, bytesOut, "if(saveStr.charAt(x)=='#')saveStr2+='%23';\n");
    scoutln(b, bytesOut, "else if(saveStr.charAt(x)=='\"')saveStr2+='%22';\n");
    scoutln(b, bytesOut, "else if(saveStr.charAt(x)=='&')saveStr2+='%26';\n");
    scoutln(b, bytesOut, "else if(saveStr.charAt(x)=='+')saveStr2+='%2B';\n");
    scoutln(b, bytesOut, "else if(saveStr.charAt(x)=='%')saveStr2+='%25';\n");
    scoutln(b, bytesOut, "else if(saveStr.charAt(x)==' ')saveStr2+='%20';\n");
    scoutln(b, bytesOut, "else if(saveStr.charAt(x)=='?')saveStr2+='%3F';\n");
    scoutln(b, bytesOut, "else if(escape(saveStr.charAt(x))=='%0A')saveStr2+='\003';\n");
    scoutln(b, bytesOut, "else if(escape(saveStr.charAt(x))=='%0D');");
    scoutln(b, bytesOut, "else saveStr2+=saveStr.charAt(x);return saveStr2;}\n");

    String cssDirectory = directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir);
    String cssDocumentDirectory = directoryUtils.getCssDocumentDirectory(con, stmt, rs, dnm);
    scoutln(b, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + cssDirectory + "general.css\">\n");
    scoutln(b, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + cssDocumentDirectory + "document.css\">\n");

    outputPageFrame(con, stmt, rs, b, req, ' ', cad.charAt(0), "", "SalesDebitNoteHeaderEdit", date, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

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

    if(searchDataString(recData, recDataLen, "debitl", b, lineBytes) == -1)
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

    sortFields(recData, buf, "debitl"); // sorts the data field (using buf in the process); results are put back into recData
    recDataLen = generalUtils.lengthBytes(recData, 0);
    generalUtils.zeroize(buf, 2000);
        
    String fieldNames = getFieldNamesDebitNoteL();
    byte[] value      = new byte[3000]; // plenty - to cover desc
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

      if(searchDataString(recData, recDataLen, "debitl", fieldName, value) != -1) // entry exists
      {
        if(fieldCount == 1) // itemCode
          itemCodeIn = generalUtils.stringFromBytes(value, 0L);
        else
        if(fieldCount == 28) // mfr
          mfrIn = generalUtils.stringFromBytes(value, 0L);
        else
        if(fieldCount == 29)
          mfrCodeIn = generalUtils.stringFromBytes(value, 0L);
        else
        {
          if(fieldCount == 6) // gstrate
          {
            if(generalUtils.match(value, "<none>"))
              value[0] = '\000';
          }

          generalUtils.repAlpha(buf, 2000, (short)fieldCount, value);
        }
      }
      
      ++fieldCount;
    }

    String[] itemCodeOut = new String[1];
    String[] mfrOut      = new String[1];
    String[] mfrCodeOut  = new String[1];
    inventory.mapCodes(con, stmt, rs, itemCodeIn, mfrIn, mfrCodeIn, itemCodeOut, mfrOut, mfrCodeOut);
    generalUtils.strToBytes(itemCode, itemCodeOut[0]);
    generalUtils.repAlpha(buf, 2000, (short)1,  itemCodeOut[0]);
    generalUtils.repAlpha(buf, 2000, (short)28, mfrOut[0]);
    generalUtils.repAlpha(buf, 2000, (short)29, mfrCodeOut[0]);
   
    generalUtils.repAlpha(buf, 2000, (short)0,  code);
    generalUtils.repAlpha(buf, 2000, (short)19, lineBytes);
   
    // just-in-case no value then need to insert a default
    if((generalUtils.dfsAsStr(buf, (short)15)).length() == 0) // dateofstockupdate 
      generalUtils.putAlpha(buf, 2000, (short)15, "1970-01-01");

    if((generalUtils.dfsAsStr(buf, (short)16)).length() == 0) // qtyreqd
      generalUtils.putAlpha(buf, 2000, (short)16, "0");

    if((generalUtils.dfsAsStr(buf, (short)27)).length() == 0) // costPrice 
      generalUtils.putAlpha(buf, 2000, (short)27, "0");

    if((generalUtils.dfsAsStr(buf, (short)22)).length() == 0) // receivedAmt 
      generalUtils.putAlpha(buf, 2000, (short)22, "0");

    if((generalUtils.dfsAsStr(buf, (short)23)).length() == 0) // refundedAmt 
      generalUtils.putAlpha(buf, 2000, (short)23, "0");

    byte[] rateB = new byte[30];
    getADebitNoteFieldGivenCode(con, stmt, rs, "Rate", code, rateB);
    double rate = generalUtils.doubleFromBytesCharFormat(rateB, 0);
    
    if(rate == 0.0) rate = 1.0; // not sure if need this but some lines saved did not recalc
    double nonBaseSellPrice = 0.0;
    boolean unitPriceGiven=false;
    generalUtils.strToBytes(fieldName, "UnitPrice");
    if(   searchDataString(recData, recDataLen, "debitl", fieldName, value) != -1 // exists
       && value[0] != '\000')
    {
      nonBaseSellPrice = generalUtils.doubleFromBytesCharFormat(value, 0);
      unitPriceGiven = true;
    }

    // fetch item details
    generalUtils.strToBytes(fieldName, "Description");
    if(searchDataString(recData, recDataLen, "debitl", fieldName, value) != -1) // exists
    {
      if(value[0] == '\000') // description fld is blank
      {
        byte[] data = new byte[3000];

        if(inventory.getStockRecGivenCode(con, stmt, rs, itemCode, '\000', data) != -1) // exists
        {
          generalUtils.dfs(data, (short)1, b);           // desc
          generalUtils.repAlpha(buf, 2000, (short)2, b);

          if(miscDefinitions.includeRemark(con, stmt, rs))
          {    
            value[0]= '\000';  
            scoutln(value, bytesOut, " \n" + generalUtils.dfsAsStr(data, (short)2));
          }
          
          if(! unitPriceGiven)
          {
            nonBaseSellPrice = generalUtils.dfsAsDouble(data, (short)25);  // rrp
            byte[] currency = new byte[50];
            getADebitNoteFieldGivenCode(con, stmt, rs, "Currency", code, currency); 
            generalUtils.dfs(data, (short)19, b);           // inventory currency
            if(! generalUtils.matchIgnoreCase(b, 0, currency, 0))
            {
              if(rate == 0.0) rate = 1.0;
              nonBaseSellPrice /= rate;
            }
          }

          generalUtils.dfs(data, (short)48, b);           // uom
          generalUtils.repAlpha(buf, 2000, (short)18, b);
        }

        generalUtils.repDoubleGivenSeparator('8', '\000', buf, 2000, (short)3, nonBaseSellPrice);
      }
      else // strip only the first desc line for debitL rec
      {
        getDescriptionLine(0, value, b);
        generalUtils.repAlpha(buf, 2000, (short)2, b);
      }
    }

    reCalculate(buf, 2000, rate);

    if(putRecLine(con, stmt, rs, code, lineBytes, newOrEdit, buf, dnm, localDefnsDir, defnsDir) != 'F')
    {
      updateMultipleLines(con, stmt, rs, code, lineBytes, value, dnm, localDefnsDir, defnsDir);
      updateDLM(con, stmt, rs, code, unm, dnm, localDefnsDir, defnsDir);
      return ' ';
    }

    return 'F';
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void updateDLM(Connection con, Statement stmt, ResultSet rs, byte[] code, String unm, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    stmt = con.createStatement();
    stmt.executeUpdate("UPDATE debit SET SignOn = '" + unm + "', DateLastModified = NULL WHERE DNCode = '"
                       + generalUtils.stringFromBytes(code, 0L) + "'");
    
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
    generalUtils.repDoubleGivenSeparator('2', '\000', buf, dataBufLen, (short)10, amt);

    amt = generalUtils.doubleDPs(amt, '2'); // added 5jul01
    generalUtils.repDoubleGivenSeparator('2', '\000', buf, dataBufLen, (short)5, (amt * rate));
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void updateMultipleLines(Connection con, Statement stmt, ResultSet rs, byte[] code, byte[] line, byte[] descData, String dnm, String localDefnsDir,
                                   String defnsDir) throws Exception
  {
    try
    {
      stmt = con.createStatement();
    
      byte[] reqdLine = new byte[81];

      int numDescLines = detDescriptionLineInfo(descData);

      // determine how many lines there are for this code+line (entry) combination.
      rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM debitll WHERE DNCode = '" + generalUtils.stringFromBytes(code, 0L) 
                             + "' AND Entry = '" + generalUtils.stringFromBytes(line, 0L) + "'");
      rs.next();
      int numLinesAtPresent = rs.getInt("rowcount");
      rs.close();

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
      
        stmt.executeUpdate("UPDATE debitll SET Text = '" + sanitisedDesc + "' WHERE DNCode = '"
                         + generalUtils.stringFromBytes(code, 0L) + "' AND Entry = '" + generalUtils.stringFromBytes(line, 0L) + "' AND Line = '"
                         + lineCount + "'");
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
      
        stmt.executeUpdate("INSERT INTO debitll (" + getFieldNamesDebitNoteLL() + ") VALUES ('" + generalUtils.stringFromBytes(code, 0L)
                         + "','" + generalUtils.stringFromBytes(line, 0L) + "','" + lineCount + "','" + sanitisedDesc + "')");
        ++lineCount;
      }

      while(lineCount <= numLinesAtPresent) // remove any 'excess' lines
      {
        stmt.executeUpdate("DELETE FROM debitll WHERE DNCode = '" + generalUtils.stringFromBytes(code, 0L) + "' AND Entry = '"
                           + generalUtils.stringFromBytes(line, 0L)  + "' AND Line = '" + lineCount + "'");
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

  // -------------------------------------------------------------------------------------------------------------------------------
  public String getADebitNoteFieldGivenCode(Connection con, Statement stmt, ResultSet rs, String fieldName, String dnCode) throws Exception
  {
    byte[] value = new byte[300]; // plenty
    byte[] dnCodeB = new byte[21];
    generalUtils.strToBytes(dnCodeB, dnCode);
    
    getADebitNoteFieldGivenCode(con, stmt, rs, fieldName, dnCodeB, value);
    
    return generalUtils.stringFromBytes(value, 0L);
  }
  public void getADebitNoteFieldGivenCode(Connection con, Statement stmt, ResultSet rs, String fieldName, byte[] dnCode, byte[] value)
                                          throws Exception
  {
    if(dnCode[0] == '\000') // just-in-case
      return;

    generalUtils.toUpper(dnCode, 0);

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT " + fieldName + " FROM debit WHERE DNCode = '" + generalUtils.stringFromBytes(dnCode, 0L)
                                   + "'");
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
     
    String layoutFile="228b.htm";

    short[] fieldSizesDebitNoteL = new short[30]; // plenty
    getFieldSizesDebitNoteL(fieldSizesDebitNoteL);

    numFields = screenLayout.layoutToBuffer(buf1, buf2, size1, fieldNames, fieldTypes, fieldSizes, localDefnsDir, defnsDir, layoutFile, 1, getFieldNamesDebitNoteL(), fieldSizesDebitNoteL, null, null,
                         null, null);

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
          sortFields(dataAlready, data, "debitl");
        }
        else
        {
          getLine(con, stmt, rs, code, line, '\001', data, dnm, localDefnsDir, defnsDir, bytesOut);

          char dpOnQuantities = miscDefinitions.dpOnQuantities(con, stmt, rs, "5");
          generalUtils.bytesDPsGivenSeparator(true, '\001', dpOnQuantities, data, 2000, 4); // origin-0, quantity
          char dpOnUnitPrice  = miscDefinitions.dpOnUnitPrice(con, stmt, rs, "4");
          generalUtils.bytesDPsGivenSeparator(true, '\001', dpOnUnitPrice,  data, 2000, 3); // unitPrice 
          generalUtils.bytesDPsGivenSeparator(true, '\001', '2',  data, 2000, 5);  // amount
          generalUtils.bytesDPsGivenSeparator(true, '\001', '2',  data, 2000, 7);  // discount
          generalUtils.bytesDPsGivenSeparator(true, '\001', '2',  data, 2000, 10); // amount2

          try
          {
            generalUtils.dfsGivenSeparator(true, '\001', data, (short)6, b);
            if(b[0] == '\000')
              generalUtils.repAlphaGivenSeparator('\001', data, 2000, (short)6, "<none>");
          }
          catch(Exception e) { }

          byte[] multipleLinesData = new byte[1000];
          int[]  multipleListLen = new int[1]; multipleListLen[0] = 1000;
          int[]  multipleLinesCount = new int[1];
          byte[] llData = new byte[200];
          multipleLinesData = getMultipleLine(con, stmt, rs, code, line, multipleLinesData, multipleListLen, multipleLinesCount, dnm, 
                                              localDefnsDir, defnsDir);

          for(int xx=0;xx<multipleLinesCount[0];++xx)
          {
            if(generalUtils.getListEntryByNum(xx, multipleLinesData, llData)) // just-in-case
            {
              generalUtils.replaceTwosWithOnes(llData);
              generalUtils.appendAlphaGivenBinary1(data, 2000, 2, llData, "\n");//<br>");
            }
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
        sortFields(dataAlready, data, "debitl");
            
        generalUtils.dfsGivenSeparator(true, '\001', data, (short)2, b); // desc
        generalUtils.repAlphaGivenSeparator('\001', data, 2000, (short)2, b);
        generalUtils.repAlphaGivenSeparator('\001', data, 2000, (short)19, generalUtils.intToStr(nextLine)); // line
        generalUtils.repAlphaGivenSeparator('\001', data, 2000, (short)20, generalUtils.intToStr(nextLine)); // entry
      }
      else
      {
        data[0]= '\000';
        scoutln(data, bytesOut, "debitl.entry=" + generalUtils.intToStr(nextLine) + "\001");

        String gstRateDefault = accountsUtils.getDefaultGSTRate(con, stmt, rs, dnm, localDefnsDir, defnsDir);
        scoutln(data, bytesOut, "debitl.gstrate=" + gstRateDefault + "\001");
      }
      
      newOrEdit = 'N';
   }

    ddlData = accountsUtils.getGSTRatesDDLData(con, stmt, rs, "debitl.gstrate", dnm, ddlData, ddlDataUpto, ddlDataLen, localDefnsDir, defnsDir);
    ddlData = inventory.getMfrsDDLData(con, stmt, rs, "debitl.manufacturer", ddlData, ddlDataUpto, ddlDataLen);

    getADebitNoteFieldGivenCode(con, stmt, rs, "Currency", code, b);
    scoutln(data, bytesOut, "debitl.currency=" + generalUtils.stringFromBytes(b, 0L) + "\001");

    scoutln(data, bytesOut, "debitl.basecurrency=" + accountsUtils.getBaseCurrency(con, stmt, rs, dnm, localDefnsDir, defnsDir) + "\001");

    dataLen = generalUtils.lengthBytes(data, 0);

    String cad;
    if(newOrEdit == 'N')
      cad = "C";
    else cad = "A";

    byte[] prependCode = new byte[20000];
    prependEditLine(con, stmt, rs, unm, sid, uty, men, den, dnm, bnm, errStr, cad, generalUtils.stringFromBytes(code, 0L),
                    generalUtils.stringFromBytes(line, 0L), "228b.htm", localDefnsDir, defnsDir, prependCode, newOrEdit, req,
                    bytesOut);

    screenLayout.bufferToBuffer(buf1, buf2, size1, size2, source, numFields, fieldNames, fieldTypes, fieldSizes, 'E', 'H', data, dataLen, ddlData, ddlDataUpto[0], null, prependCode, false, false, "", nextLine);

    append(con, stmt, rs, buf1, buf2, source, size1, size2, unm, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    screenLayout.bufferToOut(buf1, buf2, source, out);

    return true;
  }
                                                                 
  // -------------------------------------------------------------------------------------------------------------------------------
  private void prependEditLine(Connection con, Statement stmt, ResultSet rs, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                               String errStr, String cad, String code, String line, String layoutFile, String localDefnsDir,
                               String defnsDir, byte[] b, char newOrEdit, HttpServletRequest req, int[] bytesOut)
                               throws Exception
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
    scoutln(b, bytesOut, "var saveStr2=strip(saveStr);window.location.replace(\"/central/servlet/SalesDebitNoteLineUpdate?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1="
                       + cad + "&p2=" + code2 + "&p3=" + line + "&p4=\"+thisOp+\"&p5=\"+saveStr2);}\n");
    scoutln(b, bytesOut, "function strip(saveStr){\n");
    scoutln(b, bytesOut, "var saveStr2='';var x;var len=saveStr.length;\n");
    scoutln(b, bytesOut, "for(x=0;x<len;++x)\n");
    scoutln(b, bytesOut, "if(saveStr.charAt(x)=='#')saveStr2+='%23';\n");
    scoutln(b, bytesOut, "else if(saveStr.charAt(x)=='\"')saveStr2+='%22';\n");
    scoutln(b, bytesOut, "else if(saveStr.charAt(x)=='&')saveStr2+='%26';\n");
    scoutln(b, bytesOut, "else if(saveStr.charAt(x)=='%')saveStr2+='%25';\n");
    scoutln(b, bytesOut, "else if(saveStr.charAt(x)=='+')saveStr2+='%2B';\n");
    scoutln(b, bytesOut, "else if(saveStr.charAt(x)==' ')saveStr2+='%20';\n");
    scoutln(b, bytesOut, "else if(saveStr.charAt(x)=='?')saveStr2+='%3F';\n");
    scoutln(b, bytesOut, "else if(escape(saveStr.charAt(x))=='%0A')saveStr2+='\003';\n");
    scoutln(b, bytesOut, "else if(escape(saveStr.charAt(x))=='%0D');");
    scoutln(b, bytesOut, "else saveStr2+=saveStr.charAt(x);return saveStr2;}\n");

    String cssDirectory = directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir);
    String cssDocumentDirectory = directoryUtils.getCssDocumentDirectory(con, stmt, rs, dnm);
    scoutln(b, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + cssDirectory + "general.css\">\n");
    scoutln(b, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + cssDocumentDirectory + "document.css\">\n");

    outputPageFrame(con, stmt, rs, b, req, newOrEdit, ' ', "", "SalesDebitNoteLine", "", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

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
        thisOne = generalUtils.strToInt(generalUtils.dfsAsStrGivenBinary1(true, data, (short)20)); // 'line': origin-0 + 1 (for leading count)
        if(thisOne > soFar)
          soFar = thisOne;
      }
    }

    return soFar + 1;
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  // rtns: 'X' if already exists
  //       ' ' if updated sucessfully
  //       'N' if no code specified
  //       'F' if not updated
  //       'I' if illegal code
  public char putHead(Connection con, Statement stmt, ResultSet rs, byte[] originalCode, String unm, String dnm, String localDefnsDir, String defnsDir, char cad,
                      byte[] recData, int recDataLen, byte[] code, int[] bytesOut) throws Exception
  {
    byte[] str = new byte[21];  str[0] = '\000';
    generalUtils.catAsBytes("DNCode", 0, str, true);

    if(searchDataString(recData, recDataLen, "debit", str, code) == 0)
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
    double rate = 1.0;
    generalUtils.strToBytes(fldName, "Rate");
    if(searchDataString(recData, recDataLen, "debit", fldName, value) != -1) // entry exists
    {
      if(value[0] == '\000' || generalUtils.doubleFromBytesCharFormat(value, 0) == 0.0)
      {
        byte[] currency = new byte[10];
        byte[] date     = new byte[20];
        generalUtils.strToBytes(fldName, "Currency");
        searchDataString(recData, recDataLen, "debit", fldName, currency); // assumes exists

        generalUtils.strToBytes(fldName, "Date");
        searchDataString(recData, recDataLen, "debit", fldName, date); // assumes exists

        rate = accountsUtils.getApplicableRate(con, stmt, rs, generalUtils.stringFromBytes(currency, 0L), generalUtils.stringFromBytes(date, 0L), value, dnm,
                                       localDefnsDir, defnsDir);
        if(rate == 0.0) rate = 1.0;
      }
      else rate = generalUtils.doubleFromBytesCharFormat(value, 0);
    }

    String fieldNamesDebitNote = getFieldNamesDebitNote();

    // chk if any of the compname or addr flds have values already
    int len;
    boolean atleastOneHasValue=false;
    for(short x=3;x<10;++x)
    {
      getFieldName(fieldNamesDebitNote, x, fldName);
      if(searchDataString(recData, recDataLen, "debit", fldName, value) != -1) // entry exists
      {
        if(value[0] != '\000')
          atleastOneHasValue = true;
      }
    }

    boolean atleastOneHasValue2=false;
    for(short x=32;x<38;++x)
    {
      getFieldName(fieldNamesDebitNote, x, fldName);
      if(searchDataString(recData, recDataLen, "debit", fldName, value) != -1) // entry exists
      {
        if(value[0] != '\000')
          atleastOneHasValue2 = true;
      }
    }

    String companyCode = "";
    generalUtils.strToBytes(fldName, "CompanyCode");
    if(searchDataString(recData, recDataLen, "debit", fldName, value) != -1) // entry exists
      companyCode = generalUtils.stringFromBytes(value, 0L);

    len = fieldNamesDebitNote.length();
    int x=0, y, fieldCount=0;
    while(x < len)
    {
      y=0;
      while(x < len && fieldNamesDebitNote.charAt(x) != ',')
        fldName[y++] = (byte)fieldNamesDebitNote.charAt(x++);
      fldName[y] = '\000';
      ++x;
      
      while(x < len && fieldNamesDebitNote.charAt(x) == ' ')
        ++x;

      if(searchDataString(recData, recDataLen, "debit", fldName, value) != -1) // entry exists
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

          if(! atleastOneHasValue || ! atleastOneHasValue2) // companycode
          {
            byte[] b    = new byte[100];
            byte[] data = new byte[3000];
            if(customer.getCompanyRecGivenCode(con, stmt, rs, value, '\000', dnm, data, localDefnsDir, defnsDir) != -1) // exists
            {
              if(! atleastOneHasValue)
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
          
              if(! atleastOneHasValue2)
              {
                generalUtils.dfs(data, (short)39, b);          // shipname
                generalUtils.repAlpha(buf, 2000, (short)50, b);
                generalUtils.dfs(data, (short)40, b);          // shipaddr1
                generalUtils.repAlpha(buf, 2000, (short)51, b);
                generalUtils.dfs(data, (short)41, b);          // shipaddr2
                generalUtils.repAlpha(buf, 2000, (short)52, b);
                generalUtils.dfs(data, (short)42, b);          // shipaddr3
                generalUtils.repAlpha(buf, 2000, (short)53, b);
                generalUtils.dfs(data, (short)43, b);          // shipaddr4
                generalUtils.repAlpha(buf, 2000, (short)54, b);
                generalUtils.dfs(data, (short)44, b);          // shipaddr5
                generalUtils.repAlpha(buf, 2000, (short)55, b);
              }
            }  
          }

          generalUtils.putAlpha(buf, 2000, (short)2, value);
        }
        else
        if(fieldCount == 22) // signon
        {
          if(value[0] == '\000')
            generalUtils.putAlpha(buf, 2000, (short)22, unm);
          else generalUtils.putAlpha(buf, 2000, (short)22, value);
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
        if(fieldCount == 60) // terms
        {
          if(value[0] == '\000')
          {
            String days;
            if((days = customer.getACompanyFieldGivenCode(con, stmt, rs, "CreditDays", companyCode)).length() == 0) // unknown cust
              days = "30";            
            generalUtils.repAlpha(buf, 2000, (short)60, days + " days");
          }
          else generalUtils.putAlpha(buf, 2000, (short)60, value);
        }  
        else
        if(fieldCount == 49) // rate
        {
          if(rate == 0.0) rate = 1.0;

          generalUtils.repDoubleGivenSeparator('8', '\000', buf, 2000, (short)49, rate);
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
    
    if((generalUtils.dfsAsStr(buf, (short)19)).length() == 0) // amountPaid 
      generalUtils.putAlpha(buf, 2000, (short)19, "0"); 

    if((generalUtils.dfsAsStr(buf, (short)56)).length() == 0) // baseTotalTotal 
      generalUtils.putAlpha(buf, 2000, (short)56, "0");
    
    if((generalUtils.dfsAsStr(buf, (short)47)).length() == 0) // BaseGSTTotal 
      generalUtils.putAlpha(buf, 2000, (short)47, "0");
    
    if((generalUtils.dfsAsStr(buf, (short)23)).length() == 0) // GroupDiscount 
      generalUtils.putAlpha(buf, 2000, (short)23, "0");

    generalUtils.repAlpha(buf, 2000, (short)13, "1970-01-01");
    generalUtils.repAlpha(buf, 2000, (short)14, "1970-01-01");
    generalUtils.repAlpha(buf, 2000, (short)39, "1970-01-01");
    generalUtils.repAlpha(buf, 2000, (short)43, "1970-01-01");
    generalUtils.repAlpha(buf, 2000, (short)44, "1970-01-01");

    if((generalUtils.dfsAsStr(buf, (short)22)).length() == 0) // signon 
      generalUtils.repAlpha(buf, 2000, (short)22, unm);

    if((generalUtils.dfsAsStr(buf, (short)19)).length() == 0) // amountPaid 
      generalUtils.putAlpha(buf, 2000, (short)19, "0");

    generalUtils.putAlpha(buf, 2000, (short)31, "0");

    generalUtils.putAlpha(buf, 2000, (short)32, "0");

    generalUtils.putAlpha(buf, 2000, (short)33, "0");

    if((generalUtils.dfsAsStr(buf, (short)35)).length() == 0) // commissionvalue 
      generalUtils.putAlpha(buf, 2000, (short)35, "0");

    if((generalUtils.dfsAsStr(buf, (short)41)).length() == 0) // printCount 
      generalUtils.putAlpha(buf, 2000, (short)41, "0");

    // if rate has changed then recalc each line
    boolean rateChanged = false;
    if(cad == 'A')
    {
      byte[] data = new byte[2000];
      if(getRecGivenCode(con, stmt, rs, code, '\000', data, dnm, localDefnsDir, defnsDir, bytesOut) != -1) // just-in-case
      {
        if(rate != generalUtils.dfsAsDouble(data, (short)49)) // rate changed
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

            generalUtils.repAlpha(buf, 2000, (short)9, unm);

            generalUtils.dfs(buf, (short)19, str); // line

            rtn = putRecLine(con, stmt, rs, code, str, 'E', buf, dnm, localDefnsDir, defnsDir);
          }
        }
      }

      if(newOrEdit == 'N')
      {
        generalUtils.strToBytes(fldName, "InvoiceCode");
        searchDataString(recData, recDataLen, "debit", fldName, value);
        serverUtils.syncToIW(con, generalUtils.stringFromBytes(code, 0L), "", generalUtils.stringFromBytes(value, 0L), 'N', generalUtils.todaySQLFormat(localDefnsDir, defnsDir), unm, companyCode, dnm);
      }
      else serverUtils.updateNewWavechannelsRec(con, stmt, rs, generalUtils.stringFromBytes(code, 0L), "N", companyCode);

    }

    return rtn;
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
    
    rs = stmt.executeQuery("SELECT * FROM debit WHERE DNCode = '" + generalUtils.stringFromBytes(code, 0L) + "'");
    if(! rs.next())
    {
      rs.close();
      stmt.close();
      return -1;
    }      
    
    ResultSetMetaData rsmd = rs.getMetaData();
    
    String fieldTypes = getFieldTypesDebitNote();
     
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
      String fieldNames = getFieldNamesDebitNote();
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
        scoutln(data, bytesOut, "debit." + thisFieldName + "=" + getValue(count, thisFieldType, rs, rsmd) + "\001");
      
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
      String fieldTypes = getFieldTypesDebitNote();
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
      
        q = "INSERT INTO debit (" + getFieldNamesDebitNote() + ") VALUES (" + opStr + ")";
      }
      else // newOrEdit == 'E'
      {
        String thisFieldName;
        char thisFieldType;

        // change: abc\0def\0... format into: CompanyCode='abc',Name='def',...

        String fieldNames = getFieldNamesDebitNote();
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

        q = "UPDATE debit SET " + opStr + " WHERE DNCode = '" + generalUtils.stringFromBytes(code, 0L) + "'";
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
      String fieldTypes = getFieldTypesDebitNoteL();
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
      
        q = "INSERT INTO debitl (" + getFieldNamesDebitNoteL() + ") VALUES (" + opStr + ")";
      }
      else // newOrEdit == 'E'
      {
        String thisFieldName;
        char thisFieldType;

        // change: abc\0def\0... format into: CompanyCode='abc',Name='def',...

        String fieldNames = getFieldNamesDebitNoteL();
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

        q = "UPDATE debitl SET " + opStr + " WHERE DNCode = '" + generalUtils.stringFromBytes(code, 0L) + "' AND Line = '" 
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
      generalUtils.dfs(data, (short)20, entry);

      stmt = con.createStatement();
    
      stmt.executeUpdate("DELETE FROM debitl WHERE DNCode = '" + generalUtils.stringFromBytes(code, 0L) + "' AND Line = '"
                         + generalUtils.stringFromBytes(line, 0L) + "'");

      stmt.close();

      // delete multiple lines

      stmt = con.createStatement();
      stmt.executeUpdate("DELETE FROM debitll WHERE DNCode = '" + generalUtils.stringFromBytes(code, 0L) + "' AND Entry = '"
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

  // -------------------------------------------------------------------------------------------------------------------------------
  public byte[] getLines(Connection con, Statement stmt, ResultSet rs, byte[] code, char separator, byte[] linesData, int[] listLen, int[] linesCount, String dnm,
                         String localDefnsDir, String defnsDir) throws Exception
  {
    if(code[0] == '\000') // just-in-case
      return linesData;

    linesCount[0] = 0;

    generalUtils.toUpper(code, 0);

    String s;
    int x, len;
    String fieldTypes = getFieldTypesDebitNoteL();
    String fieldNames = getFieldNamesDebitNoteL();    
    byte[] newItem = new byte[1000];
    String thisFieldName;
    char thisFieldType;
    int count;
    int lenFieldNames = fieldNames.length();
    int lenFieldTypes = fieldTypes.length();
        
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT * FROM debitl WHERE DNCode = '" + generalUtils.stringFromBytes(code, 0L) + "'");
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
          s += "debitl." + thisFieldName + "=" + getValue(count, thisFieldType, rs, rsmd) + "\001";
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
  private int getLine(Connection con, Statement stmt, ResultSet rs, byte[] code, byte[] line, char separator, byte[] data, String dnm, String localDefnsDir,
                      String defnsDir, int[] bytesOut) throws Exception
  {
    if(code[0] == '\000') // just-in-case
      return -1;

    if(line[0] == '\000')
      return -1;

    generalUtils.toUpper(code, 0);

    int upto;
    String fieldTypes = getFieldTypesDebitNoteL();
    String fieldNames = getFieldNamesDebitNoteL();
    String thisFieldName;
    char thisFieldType;
    int x, count, len = fieldNames.length(), numFields = fieldTypes.length();
    data[0] = '\000';
    
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT * FROM debitl WHERE DNCode = '" + generalUtils.stringFromBytes(code, 0L)
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
          scoutln(data, bytesOut, "debitl." + thisFieldName + "=" + getValue(count, thisFieldType, rs, rsmd) + "\001");
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

  // -------------------------------------------------------------------------------------------------------------------------------
  public byte[] getMultipleLines(Connection con, Statement stmt, ResultSet rs, byte[] code, byte[] linesData, int[] listLen, int[] linesCount, String dnm,
                                 String localDefnsDir, String defnsDir) throws Exception
  {
    linesCount[0] = 0;

    if(code[0] == '\000') // just-in-case
      return linesData;

    generalUtils.toUpper(code, 0);

    int len;
    String s;
    byte[] newItem = new byte[2000];

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT * FROM debitll WHERE DNCode = '" + generalUtils.stringFromBytes(code, 0L) + "'");
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

  // -------------------------------------------------------------------------------------------------------------------------------
  public byte[] getMultipleLine(Connection con, Statement stmt, ResultSet rs, byte[] code, byte[] line, byte[] linesData, int[] listLen, int[] linesCount, String dnm,
                                String localDefnsDir, String defnsDir) throws Exception
  {
    linesCount[0] = 0;

    if(code[0] == '\000') // just-in-case
      return linesData;

    generalUtils.toUpper(code, 0);

    int len;
    byte[] newItem = new byte[100];

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT Text FROM debitll WHERE DNCode = '" + generalUtils.stringFromBytes(code, 0L)
                                   + "' AND Entry = '" + generalUtils.stringFromBytes(line, 0L) + "'");
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

  // -------------------------------------------------------------------------------------------------------------------------------
  private void prependDisplayOnly(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, String code, String date, String unm, String sid, String uty, String men, String den,
                                  String dnm, String bnm, String localDefnsDir, String defnsDir, byte[] b, int[] bytesOut)
                                  throws Exception
  {
    b[0]= '\000';
    scoutln(b, bytesOut, "<html><head><title>" + code + "</title>");

    String cssDirectory = directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir);
    String cssDocumentDirectory = directoryUtils.getCssDocumentDirectory(con, stmt, rs, dnm);
    scoutln(b, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + cssDirectory + "general.css\">\n");
    scoutln(b, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + cssDocumentDirectory + "document.css\">\n");

    outputPageFrame(con, stmt, rs, b, req, ' ',  ' ', "", "_4129", date, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void sortFields(byte[] ipBuf, byte[] opBuf, String which) throws Exception
  {
    String thisFieldName;
    String fieldNames;
    int x=0, y;
    byte[] thisFieldNameB = new byte[50];
    byte[] value          = new byte[1000];
    
    if(which.equals("debit"))
      fieldNames = getFieldNamesDebitNote();
    else // if(which.equals("debitl"))
      fieldNames = getFieldNamesDebitNoteL();
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
  
  // -------------------------------------------------------------------------------------------------------------------------------
  public void updateGSTRates(Connection con, Statement stmt, ResultSet rs, byte[] code, String gstRate, String unm, String dnm, String localDefnsDir, String defnsDir)
                               throws Exception
  {
    updateDLM(con, stmt, rs, code, unm, dnm, localDefnsDir, defnsDir);

    int y, z;
    byte[] buf        = new byte[2000];
    byte[] b          = new byte[20];
    byte[] line       = new byte[500];
    byte[] linesData  = new byte[2000];
    int[]  listLen    = new int[1];  listLen[0] = 2000;
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

        generalUtils.repAlpha(buf, 2000, (short)6,  gstRate);
        generalUtils.repAlpha(buf, 2000, (short)9, unm);
        generalUtils.repAlpha(buf, 2000, (short)8, '\000'); // dlm

        generalUtils.dfs(buf, (short)19, b); // line

        putRecLine(con, stmt, rs, code, b, 'E', buf, dnm, localDefnsDir, defnsDir);
      }
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void updatePrices(Connection con, Statement stmt, ResultSet rs, byte[] code, String band, String clear, String unm, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    byte[] buf  = new byte[5000];
    byte[] data = new byte[5000];

    byte[] rateB = new byte[30];
    getADebitNoteFieldGivenCode(con, stmt, rs, "Rate", code, rateB);
    double rate = generalUtils.doubleFromBytesCharFormat(rateB, 0);
 
    updateDLM(con, stmt, rs, code, unm, dnm, localDefnsDir, defnsDir);

    int y, z;
    byte[] b          = new byte[21];
    byte[] line       = new byte[500];
    byte[] linesData  = new byte[2000];
    int[]  listLen    = new int[1];  listLen[0] = 2000;
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

        generalUtils.repAlpha(buf, 2000, (short)9, unm);
        generalUtils.repAlpha(buf, 2000, (short)8, '\000');

        generalUtils.dfs(buf, (short)19, b); // line

        putRecLine(con, stmt, rs, code, b, 'E', buf, dnm, localDefnsDir, defnsDir);
      }
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  // uses layout 228d.htm
  public boolean getRecToHTMLDisplayOnly(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, HttpServletRequest req, byte[] code, String unm, String sid, String uty, String men, String den, String dnm,
                                         String bnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    String imagesDir = directoryUtils.getSupportDirs('I');

    byte[][] buf1 = new byte[1][5000];
    byte[][] buf2 = new byte[1][5000];
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
      messagePage.msgScreen(false, out, req, 6, unm, sid, uty, men, den, dnm, bnm, "SalesInvoice", imagesDir, localDefnsDir,
                         defnsDir, bytesOut);
      return false;
    }

    short[] fieldSizesDebitNote = new short[70]; // plenty
    getFieldSizesDebitNote(fieldSizesDebitNote);
    short[] fieldSizesDebitNoteL = new short[30]; // plenty
    getFieldSizesDebitNoteL(fieldSizesDebitNoteL);

    numFields = screenLayout.layoutToBuffer(buf1, buf2, size1, fieldNames, fieldTypes, fieldSizes, localDefnsDir, defnsDir, "228d.htm", 2, getFieldNamesDebitNote(), fieldSizesDebitNote, getFieldNamesDebitNoteL(), fieldSizesDebitNoteL, null, null);

    int recDataLen = generalUtils.lengthBytes(data, 0);

    String fieldNamesDN = getFieldNamesDebitNote();
    byte[] value     = new byte[1000]; // plenty - to cover notes
    byte[] fieldName = new byte[50];
    int x=0, y, fieldCount=0;
    int len = fieldNamesDN.length();
    while(x < len)
    {
      y=0;
      while(x < len && fieldNamesDN.charAt(x) != ',')
        fieldName[y++] = (byte)fieldNamesDN.charAt(x++);
      fieldName[y] = '\000';
      ++x;
      
      while(x < len && fieldNamesDN.charAt(x) == ' ')
        ++x;

      if(searchDataString(data, recDataLen, "debit", fieldName, value) != -1) // entry exists
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
    generalUtils.dfsGivenSeparator(true, '\001', data, (short)1, date);

    byte[] javaScriptCallCode = new byte[1000]; // lock
    javaScriptCall(con, stmt, rs, javaScriptCallCode, req, generalUtils.stringFromBytes(date, 0L), unm, uty, dnm, localDefnsDir, defnsDir, bytesOut);

    String attachments = getAttachments(con, stmt, stmt2, rs, rs2, generalUtils.stringFromBytes(code, 0L), unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir);
    if(attachments.length() == 0)
      attachments= "none";
    scoutln(data, bytesOut, "debit.Attachments=" + attachments + "\001");

    prependDisplayOnly(con, stmt, rs, req, generalUtils.stringFromBytes(code, 0L), generalUtils.stringFromBytes(date, 0L), unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, prependCode, bytesOut);
    screenLayout.bufferToBuffer(buf1, buf2, size1, size2, source, numFields, fieldNames, fieldTypes, fieldSizes, 'D', 'H', data, 3000, ddlData, ddlDataUpto[0], javaScriptCode, prependCode);

    char dpOnQuantities = miscDefinitions.dpOnQuantities(con, stmt, rs, "5");
    char dpOnUnitPrice  = miscDefinitions.dpOnUnitPrice(con, stmt, rs, "4");

    byte[] linesData = new byte[2000];
    int[] listLen = new int[1];  listLen[0] = 2000;
    int[] linesCount = new int[1];
    linesData = getLines(con, stmt, rs, code, '\001', linesData, listLen, linesCount, dnm, localDefnsDir, defnsDir);
    for(x=0;x<linesCount[0];++x)
    {
      if(generalUtils.getListEntryByNum(x, linesData, data)) // just-in-case
      {
        generalUtils.replaceTwosWithOnes(data);
        generalUtils.bytesDPsGivenSeparator(true, '\001', dpOnUnitPrice,  data, 2000, 4); // origin-1
        generalUtils.bytesDPsGivenSeparator(true, '\001', dpOnQuantities, data, 2000, 5);
        generalUtils.bytesDPsGivenSeparator(true, '\001', '2',            data, 2000, 6);
        generalUtils.bytesDPsGivenSeparator(true, '\001', '2',            data, 2000, 8);
        generalUtils.bytesDPsGivenSeparator(true, '\001', '2',            data, 2000, 11);
        
        screenLayout.bufferToBuffer(buf1, buf2, size1, size2, source, numFields, fieldNames, fieldTypes, fieldSizes, 'D', 'L', data, 2000, ddlData, ddlDataUpto[0], javaScriptCallCode, null);
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
      
      rs = stmt.executeQuery("SELECT LibraryDocCode FROM doa WHERE Code = '" + code + "'");
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
  private void outputPageFrame(Connection con, Statement stmt, ResultSet rs, byte[] b, HttpServletRequest req, char newOrEdit, char cad, String bodyStr, String callingServlet, String date, String unm, String sid, String uty, String men,
                               String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    int[] hmenuCount = new int[1];

    String subMenuText = dashboardUtils.buildSubMenuText(con, stmt, rs, req, "4111", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount);

    subMenuText += "<table id='title' width=100%><tr><td colspan=2 nowrap><p>Sales Debit Note" + directoryUtils.buildHelp(4111) + "</td></tr></table>";

    subMenuText += buildSubMenuText(con, stmt, rs, req, cad, newOrEdit, callingServlet, date, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount);

    scoutln(b, bytesOut, authenticationUtils.buildScreen(con, stmt, rs, req, true, unm, sid, uty, men, den, dnm, bnm, callingServlet, subMenuText, hmenuCount[0], bodyStr, "", localDefnsDir, defnsDir));
    
    scoutln(b, bytesOut, "<table border=0><tr><td valign=top><div id='second' style=\"{margin-top:0px;}\"></div></td>");
    scoutln(b, bytesOut, "<td valign=top><div id='third' style=\"{margin-top:0px;margin-left:0px;}\"></div></td></tr></table>");
  }
 
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private String buildSubMenuText(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, char cad, char newOrEdit, String callingServlet, String date, String unm, String sid, String uty, String men, String den, String dnm,
                                  String bnm, String localDefnsDir, String defnsDir, int[] hmenuCount) throws Exception
  {
    String s = "<div id='submenu'>";

    ++hmenuCount[0];

    if(callingServlet.equals("SalesDebitNotePage"))
      s += drawOptions4111(con, stmt, rs, req, hmenuCount, date, unm, uty, dnm, localDefnsDir, defnsDir);
    else
    if(callingServlet.equals("SalesDebitNoteHeaderEdit"))
      s += drawOptions4113(hmenuCount, cad, unm, sid, uty, men, den, dnm, bnm);
    else 
    if(callingServlet.equals("SalesDebitNoteLine"))
      s += drawOptions4115(hmenuCount, newOrEdit, unm, sid, uty, men, den, dnm, bnm);

    s += "</div>";
    
    --hmenuCount[0];
    
    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private String drawOptions4111(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, int[] hmenuCount, String date, String unm, String uty, String dnm, String localDefnsDir, String defnsDir) throws Exception
  { 
    String s = "";
    
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4113, unm, uty, dnm, localDefnsDir, defnsDir) && serverUtils.passLockCheck(con, stmt, rs, "debit", date, unm))
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\"><a href=\"javascript:fetch()\">Edit Details</a></dt></dl>\n";
  
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4115, unm, uty, dnm, localDefnsDir, defnsDir) && serverUtils.passLockCheck(con, stmt, rs, "debit", date, unm))
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\"><a href=\"javascript:add()\">Add New Line</a></dt></dl>\n";
   
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 6094, unm, uty, dnm, localDefnsDir, defnsDir) && serverUtils.passLockCheck(con, stmt, rs, "debit", date, unm))
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\"><a href=\"javascript:attach()\">Attachments</a></dt></dl>\n";
   
    boolean SalesDebitNotePrint = authenticationUtils.verifyAccess(con, stmt, rs, req, 4118, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean _4188 = authenticationUtils.verifyAccess(con, stmt, rs, req, 4188, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean MailExternalUserCreate = authenticationUtils.verifyAccess(con, stmt, rs, req, 8016, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean PrintToPDFUtils = authenticationUtils.verifyAccess(con, stmt, rs, req, 11200, unm, uty, dnm, localDefnsDir, defnsDir);

    if(SalesDebitNotePrint || _4188 || MailExternalUserCreate || PrintToPDFUtils)
    {
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0] + "');\">Send</dt><dd id='hmenu" + hmenuCount[0]++ + "'><ul>";
      if(SalesDebitNotePrint)
        s += "<li><a href=\"javascript:print()\">Print</a></li>\n";
      if(_4188)
        s += "<li><a href=\"javascript:fax()\">Fax</a></li>\n";
      if(PrintToPDFUtils)
        s += "<li><a href=\"javascript:pdf()\">PDF</a></li>";
      if(MailExternalUserCreate)
        s += "<li><a href=\"javascript:mail()\">Mail</a></li>";
    
      s += "</ul></dd></dl>\n";
    }

    if(serverUtils.passLockCheck(con, stmt, rs, "debit", date, unm))
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

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 1025, unm, uty, dnm, localDefnsDir, defnsDir))
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\"><a href=\"javascript:trace()\">Trace</a></dt></dl>\n";
   
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 1025, unm, uty, dnm, localDefnsDir, defnsDir))
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\"><a href=\"javascript:trace2()\">Long Trace</a></dt></dl>\n";

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 11800, unm, uty, dnm, localDefnsDir, defnsDir))
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\"><a href=\"javascript:trail()\">Trail</a></dt></dl>\n";
  
    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private String drawOptions4113(int[] hmenuCount, char cad, String unm, String sid, String uty, String men, String mainDNM, String dnm, String bnm)
                                 throws Exception
  {      
    String s = "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\"><a href=\"javascript:save(' ')\">Save Changes</a></dt></dl>\n";

    if(cad != 'C')
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\"><a href=\"javascript:save('X')\">Cancel Changes</a></dt></dl>\n";
 
    s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\"><script language='Javascript'>";
    s += "var alreadyOnce=false;function select(){main.style.visibility='hidden';second.style.visibility='visible';"
      + "submenu.style.visibility='hidden';if(!alreadyOnce){select4230('A');alreadyOnce=true;}}";

    s += "var req2;";
    s += "function initRequest2(url)";
    s += "{if(window.XMLHttpRequest){req2=new XMLHttpRequest();}else if(window.ActiveXObject){req2=new ActiveXObject(\"Microsoft.XMLHTTP\");}}";

    s += "function select4230(searchChar)";
    s += "{var url = \"http://" + men + "/central/servlet/CustomerSelect?unm=\" + escape('" + unm + "') + \"&sid=\" + escape('" + sid + "') + \"&uty=\" + escape('" + uty
      + "') + \"&men=\" + escape('" + men + "') + \"&den=\" + escape('" + mainDNM + "') + \"&bnm=\" + escape('" + bnm
      + "') + \"&p1=\"+searchChar+\"&dnm=\" + escape('" + dnm + "');";
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
  private String drawOptions4115(int[] hmenuCount, char newOrEdit, String unm, String sid, String uty, String men, String mainDNM, String dnm, String bnm)
                                 throws Exception
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

    s += "<script language='Javascript'>";
    s += "var alreadyOnce=false;function select(){main.style.visibility='hidden';second.style.visibility='visible';"
      + "third.style.visibility='visible';submenu.style.visibility='hidden';if(!alreadyOnce){select2008('A');alreadyOnce=true;}}\n";

    s += "var req2;";
    s += "function initRequest2(url)";
    s += "{if(window.XMLHttpRequest){req2=new XMLHttpRequest();}else if(window.ActiveXObject){req2=new ActiveXObject(\"Microsoft.XMLHTTP\");}}\n";

   s += "function select2008(searchChar)";
    s += "{var url = \"http://" + men + "/central/servlet/CatalogStockPage?unm=\" + escape('" + unm + "') + \"&sid=\" + escape('" + sid + "') + \"&uty=\" + escape('" + uty
      + "') + \"&men=\" + escape('" + men + "') + \"&den=\" + escape('" + mainDNM + "') + \"&bnm=\" + escape('" + bnm
      + "') + \"&p1=\"+searchChar+\"&dnm=\" + escape('" + dnm + "');";
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
    s += "{if(window.XMLHttpRequest){req3=new XMLHttpRequest();}else if(window.ActiveXObject){req3=new ActiveXObject(\"Microsoft.XMLHTTP\");}}\n";

    s += "function select2008a(mfr,operation,searchType,srchStr,firstNum,lastNum,maxRows,numRecs,firstCode,lastCode)";
    s += "{var url = \"http://" + men + "/central/servlet/CatalogItemsSection?unm=\" + escape('" + unm + "') + \"&sid=\" + escape('" + sid + "') + \"&uty=\" + escape('"
      + uty + "') + \"&men=\" + escape('" + men + "') + \"&den=\" + escape('" + mainDNM + "') + \"&bnm=\" + escape('" + bnm
      + "') + \"&p1=\"+escape(mfr)+\"&p2=\"+operation+\"&p3=\"+srchStr+\"&p4=\"+firstNum+\"&p5=\"+lastNum+"
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
    s +=  "else if(code.charAt(x)=='?')code2+='%3F';";
    s += "else code2+=code.charAt(x);";
    s += "return code2};\n";

    s += "</script>";
    
    return s;
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public void putMultipleLine(Connection con, Statement stmt, ResultSet rs, byte[] code, int line, byte[] descData, String dnm, String localDefnsDir, String defnsDir)
                                throws Exception
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
      
      stmt.executeUpdate("INSERT INTO debitll (" + getFieldNamesDebitNoteLL() + ") VALUES ('" + generalUtils.stringFromBytes(code, 0L)
                         + "','" + line + "','" + generalUtils.dfsAsStr(descData, (short)2)  + "','" + opStr + "')");
      
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

  // -------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(byte[] b, int bytesOut[], String str) throws Exception
  {      
    generalUtils.catAsBytes(str, 0, b, false);
    bytesOut[0] += (str.length() + 2);    
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void updateDiscounts(Connection con, Statement stmt, ResultSet rs, byte[] code, String discount, String which, String unm, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    byte[] buf  = new byte[5000];

    byte[] rateB = new byte[30];
    getADebitNoteFieldGivenCode(con, stmt, rs, "Rate", code, rateB);
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

          generalUtils.repAlpha(buf, 5000, (short)9, unm);
          generalUtils.repAlpha(buf, 5000, (short)8, '\000');

          generalUtils.dfs(buf, (short)19, b); // line

          putRecLine(con, stmt, rs, code, b, 'E', buf, dnm, localDefnsDir, defnsDir);
        }
      }
    }
  }

}
