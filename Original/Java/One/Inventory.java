// =======================================================================================================================================================================================================
// System: ZaraStar Product: Stock Record Access
// Module: inventory.java
// Author: C.K.Harvey
// Copyright (c) 1999-08 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import java.sql.*;
import java.io.*;
import java.math.BigDecimal;
import javax.servlet.http.HttpServletRequest;

public class Inventory
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ScreenLayout screenLayout = new ScreenLayout();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  MiscDefinitions miscDefinitions = new MiscDefinitions();
  DocumentUtils documentUtils = new DocumentUtils();
  AccountsUtils accountsUtils = new AccountsUtils();
  DashboardUtils dashboardUtils = new DashboardUtils();
  StockLevelsGenerate stockLevelsGenerate = new StockLevelsGenerate();
  WacMetaUtils wacMetaUtils = new WacMetaUtils();

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getTableCreateStringStock() throws Exception
  {  
    return "stock( ItemCode char(20) not null,  Description char(80),       Description2 char(80), "
                + "Manufacturer char(30),       ManufacturerCode char(60),  Misc11 char(20), "
                + "AltItemCode1 char(30),       AltItemCode2 char(30),      GSTRate char(20), "
                + "Misc1 char(20),              Misc2 char(20),             Misc3 char(50), "
                + "Misc4 char(20),              Misc5 char(20),             Misc6 decimal(19,8), "
                + "Misc7 decimal(19,8),         Misc8 decimal(19,8),        Status char(1), "
                + "PurchasePrice decimal(17,8), Currency char(3),           SellPrice1 decimal(17,8), "
                + "SellPrice2 decimal(17,8),    SellPrice3 decimal(17,8),   SellPrice4 decimal(17,8), "
                + "ExchangeRate decimal(16,8),  RRP decimal(17,8),          PPToRRPFactor decimal(16,8), "
                + "Image1 char(100),            Image2 char(100),           AccCodeSales char(20), "
                + "Special char(1),             SpecialPrice decimal(17,8), SpecialPriceFrom date, "
                + "SpecialPriceTo date,         SignOn char(20),            DateLastModified timestamp, "
                + "Misc12 char(20),             WrittenOff char(1),         Markup1 decimal(16,8), "
                + "Markup2 decimal(16,8),       Markup3 decimal(16,8),      Markup4 decimal(16,8), "
                + "Misc10 char(40),             AltItemCode3 char(30),      NewPurchasePrice decimal(17,8), "
                + "NewPurchasePriceFrom date,   Weight decimal(16,8),       WeightPer integer, "
                + "UoM char(20),                AltItemCode4 char(30),      DateChanged date, "
                + "Remark char(80),             SalesCurrency char(3),      ShowToWeb char(1), "
                + "CycleCountIgnore char(1),    CategoryCode integer,       ClosingWAC decimal(19,8), "
                + "ClosingLevel decimal(17,8),  ClosingDate date,           Grouping char(20), "
                + "Origin char(60),             ProductWaveID integer,      NewDesc char(100), NewDesc2 char(250), NewCategory1 char(60), NewCategory2 char(60)"
                + "unique(ItemCode))";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStringsStock(String[] s) throws Exception
  {
    s[0] = "stockItemCodeInx on stock(Description)";
    s[1] = "stockManufacturerInx on stock(Manufacturer)";
    s[2] = "stockManufacturerCodeInx on stock(ManufacturerCode)";
    s[3] = "altItemCode1Inx on stock(AltItemCode1)";
    s[4] = "altItemCode2Inx on stock(AltItemCode2)";
    s[5] = "altItemCode3Inx on stock(AltItemCode3)";
    s[6] = "altItemCode4Inx on stock(AltItemCode4)";
    s[7] = "categoryCodeInx on stock(CategoryCode)";
    s[8] = "productWaveIDInx on stock(ProductWaveID)";
    
    return 9;
  }
      
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNamesStock() throws Exception
  {  
    return "ItemCode, Description, Description2, Manufacturer, ManufacturerCode, Misc11, AltItemCode1, AltItemCode2, GSTRate, "
         + "Misc1, Misc2, Misc3, Misc4, Misc5, Misc6, Misc7, Misc8, Status, PurchasePrice, Currency, SellPrice1, SellPrice2, "
         + "SellPrice3, SellPrice4, ExchangeRate, RRP, PPToRRPFactor, Image1, Image2, AccCodeSales, Special, "
         + "SpecialPrice, SpecialPriceFrom, SpecialPriceTo, SignOn, DateLastModified, Misc12, WrittenOff, Markup1, Markup2, Markup3, "
         + "Markup4, Misc10, AltItemCode3, NewPurchasePrice, NewPurchasePriceFrom, Weight, WeightPer, UoM, AltItemCode4, "
         + "DateChanged, Remark, SalesCurrency, ShowToWeb, CycleCountIgnore, CategoryCode, ClosingWAC, ClosingLevel, ClosingDate, Grouping, Origin, ProductWaveID, NewDesc, NewDesc2, NewCategory1, NewCategory2";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypesStock() throws Exception
  {
    return "CCCCCCCCCCCCCCFFFCFCFFFFFFFCCCCFDDCSCCFFFFCCFDFICCDCCCCIFFDCCICCCC";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizesStock(short[] sizes) throws Exception
  {
    sizes[0]  = 20; sizes[1]  = 80; sizes[2]  = 80;  sizes[3]  = 30;  sizes[4]  = 60;  sizes[5]  = 20;  sizes[6]  = 30;
    sizes[7]  = 30; sizes[8]  = 20; sizes[9]  = 20;  sizes[10] = 20;  sizes[11] = 50;  sizes[12] = 20;  sizes[13] = 20;
    sizes[14] = 0;  sizes[15] = 0;  sizes[16] = 0;   sizes[17] = 1;   sizes[18] = 0;   sizes[19] = 3;   sizes[20] = 0;
    sizes[21] = 0;  sizes[22] = 0;  sizes[23] = 0;   sizes[24] = 0;   sizes[25] = 0;   sizes[26] = 0;   sizes[27] = 40;
    sizes[28] = 40; sizes[29] = 20; sizes[30] = 1;   sizes[31] = 0;   sizes[32] = 0;   sizes[33] = 0;   sizes[34] = 20;
    sizes[35] = -1; sizes[36] = 20; sizes[37] = 1;  sizes[38] = 0;   sizes[39] = 0;   sizes[40] = 0;   sizes[41] = 0;
    sizes[42] = 40; sizes[43] = 30; sizes[44] = 0;   sizes[45] = 0;   sizes[46] = 0;   sizes[47] = 0;   sizes[48] = 20;
    sizes[49] = 30; sizes[50] = 0;  sizes[51] = 80;  sizes[52] = 3;   sizes[53] = 1;   sizes[54] = 1;   sizes[55] = 0;
    sizes[56] = 0;  sizes[57] = 0;  sizes[58] = 0;   sizes[59] = 20;  sizes[60] = 60;  sizes[61] = 0;   sizes[62] = 100;   sizes[63] = 250;   sizes[64] = 60;   sizes[65] = 60;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldStylesStock() throws Exception
  {
    return "MMOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO";
  }
    
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getTableCreateStringStockx() throws Exception
  {  
    return "stockx( ItemCode char(20) not null,      Store char(20) not null,       Location char(40), "
                 + "CurrentQty decimal(19,8),        MinimumQty decimal(19,8),      MaximumQty decimal(19,8), "
                 + "LastStockCheckQty decimal(19,8), OrderQty decimal(19,8),        SignOn char(20), "
                 + "OpenBalance decimal(19,8),       DateLastModified timestamp,    unused1 char(20), "
                 + "LastStockCheckDate date,         LastStockCheckSignOn char(20), "
                 + "unique(ItemCode, Store))";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStringsStockx(String[] s) throws Exception
  {
    s[0] = "";  
    return 0;
  }
      
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNamesStockx() throws Exception
  {  
    return "ItemCode, Store, Location, CurrentQty, MinimumQty, MaximumQty, LastStockCheckQty, OrderQty, SignOn, OpenBalance, "
         + "DateLastModified, unused1, LastStockCheckDate, LastStockCheckSignOn";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypesStockx() throws Exception
  {
    return "CCCFFFFFCFSCDC";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizesStockx(short[] sizes) throws Exception
  {
    sizes[0]  = 20; sizes[1]  = 20; sizes[2]  = 40;  sizes[3]  = 0;   sizes[4]  = 0;   sizes[5]  = 0;  sizes[6]  = 0;
    sizes[7]  = 0;  sizes[8]  = 20; sizes[9]  = 0;   sizes[10] = -1;  sizes[11] = 20;  sizes[12] = 0;  sizes[13] = 20;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldStylesStockx() throws Exception
  {
    return "MMOOOOOOOOOOOO";
  }  
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean stockGetRecToHTML(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den,
                                   String dnm, String bnm, char dispOrEdit, char cad, byte[] itemCode, String localDefnsDir, String defnsDir,
                                   String errStr, byte[] dataAlready, int[] bytesOut) throws Exception
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
    byte[] prependCode = new byte[20000];

    boolean rtn=false;

    if(cad == 'A' && itemCode[0] == '\000')
    {
      messagePage.msgScreen(false, out, req, 6, unm, sid, uty, men, den, dnm, bnm, "Inventory", imagesDir, localDefnsDir, defnsDir, bytesOut);
      return true;
    }
    
    short[] fieldSizesStock = new short[70]; // plenty
    getFieldSizesStock(fieldSizesStock);
    
    short[] fieldSizesStockx = new short[20]; // plenty
    getFieldSizesStockx(fieldSizesStockx);
    
    String layoutToUse;
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 7543, unm, uty, dnm, localDefnsDir, defnsDir))
      layoutToUse = "201.htm";
    else layoutToUse = "201a.htm";      
      
    if(cad == 'A') // not a new one
    {
      boolean ok = false;
      if(dataAlready[0] != '\000') // coming with an err msg - passing-in data already entered but 
      {                            // the data's fields may (will) not be in the correct order, so sort them
        sortFieldData("stock.", dataAlready, data);
        ok = true;
      }
      else
      {
        if(getStockRecGivenCode(con, stmt, rs, itemCode, '\001', data) == -1)
          messagePage.msgScreen(false, out, req, 6, unm, sid, uty, men, den, dnm, bnm, "Inventory", imagesDir, localDefnsDir, defnsDir, bytesOut);
        else ok = true;
      }

      if(ok)
      {
        numFields = screenLayout.layoutToBuffer(buf1, buf2, size1, fieldNames, fieldTypes, fieldSizes, localDefnsDir, defnsDir, layoutToUse, 2, getFieldNamesStock(), fieldSizesStock, getFieldNamesStockx(), fieldSizesStockx, null, null);

        byte[] ddlData    = new byte[1000];
        int[] ddlDataLen  = new int[1]; ddlDataLen[0] = 1000;
        int[] ddlDataUpto = new int[1]; ddlDataUpto[0] = 0;

        ddlData = accountsUtils.getCurrencyNamesDDLData(con, stmt, rs, "stock.Currency", dnm, ddlData, ddlDataUpto, ddlDataLen, localDefnsDir, defnsDir);
        ddlData = accountsUtils.getCurrencyNamesDDLData(con, stmt, rs, "stock.SalesCurrency", dnm, ddlData, ddlDataUpto, ddlDataLen, localDefnsDir, defnsDir);

        if(dispOrEdit == 'D')
          prepend(con, stmt, rs, req, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, generalUtils.stringFromBytes(itemCode, 0L), prependCode, bytesOut);
        else prependEdit(con, stmt, rs, errStr, req, unm, sid, uty, men, den, dnm, bnm, cad, generalUtils.stringFromBytes(itemCode, 0L), localDefnsDir, defnsDir, prependCode, bytesOut);

        if(dataAlready[0] == '\000') // NOT coming with an err msg
        {
          byte[] b = new byte[20];

          // convert dates
          generalUtils.dfsGivenSeparator(true, '\001', data, (short)32, b); // origin-0
          generalUtils.convertFromYYYYMMDD(b); 
          generalUtils.repAlphaGivenSeparator('\001', data, 5000, (short)32, b);

          generalUtils.dfsGivenSeparator(true, '\001', data, (short)33, b); // origin-0
          generalUtils.convertFromYYYYMMDD(b); 
          generalUtils.repAlphaGivenSeparator('\001', data, 5000, (short)33, b);

          generalUtils.dfsGivenSeparator(true, '\001', data, (short)45, b); // origin-0
          generalUtils.convertFromYYYYMMDD(b); 
          generalUtils.repAlphaGivenSeparator('\001', data, 5000, (short)45, b);

          generalUtils.dfsGivenSeparator(true, '\001', data, (short)50, b); // origin-0
          generalUtils.convertFromYYYYMMDD(b); 
          generalUtils.repAlphaGivenSeparator('\001', data, 5000, (short)50, b);
          
          generalUtils.dfsGivenSeparator(true, '\001', data, (short)58, b); // origin-0
          generalUtils.convertFromYYYYMMDD(b); 
          generalUtils.repAlphaGivenSeparator('\001', data, 5000, (short)58, b);
          
          generalUtils.bytesDPsGivenSeparator(true, '\001', '2',  data, 5000, 14); // origin-0 
          generalUtils.bytesDPsGivenSeparator(true, '\001', '2',  data, 5000, 15); 
          generalUtils.bytesDPsGivenSeparator(true, '\001', '2',  data, 5000, 16);  
          generalUtils.bytesDPsGivenSeparator(true, '\001', '2',  data, 5000, 18);  
          generalUtils.bytesDPsGivenSeparator(true, '\001', '2',  data, 5000, 20); 
          generalUtils.bytesDPsGivenSeparator(true, '\001', '2',  data, 5000, 21);  
          generalUtils.bytesDPsGivenSeparator(true, '\001', '2',  data, 5000, 22); 
          generalUtils.bytesDPsGivenSeparator(true, '\001', '2',  data, 5000, 23);  
          generalUtils.bytesDPsGivenSeparator(true, '\001', '2',  data, 5000, 24); 
          generalUtils.bytesDPsGivenSeparator(true, '\001', '2',  data, 5000, 25);  
          generalUtils.bytesDPsGivenSeparator(true, '\001', '2',  data, 5000, 26); 
          generalUtils.bytesDPsGivenSeparator(true, '\001', '2',  data, 5000, 31);  
          generalUtils.bytesDPsGivenSeparator(true, '\001', '2',  data, 5000, 38); 
          generalUtils.bytesDPsGivenSeparator(true, '\001', '2',  data, 5000, 39);  
          generalUtils.bytesDPsGivenSeparator(true, '\001', '2',  data, 5000, 40); 
          generalUtils.bytesDPsGivenSeparator(true, '\001', '2',  data, 5000, 41);  
          generalUtils.bytesDPsGivenSeparator(true, '\001', '2',  data, 5000, 44); 
          generalUtils.bytesDPsGivenSeparator(true, '\001', '2',  data, 5000, 46);  
          generalUtils.bytesDPsGivenSeparator(true, '\001', '2',  data, 5000, 56);  
          generalUtils.bytesDPsGivenSeparator(true, '\001', '2',  data, 5000, 57);  
        }
        
        screenLayout.bufferToBuffer(buf1, buf2, size1, size2, source, numFields, fieldNames, fieldTypes, fieldSizes, dispOrEdit, 'H', data, 5000, ddlData, ddlDataUpto[0], null, prependCode);
        
        if(dispOrEdit == 'D') // display head and lines
        {
          byte[] javaScriptPrependCode = new byte[1000];
          javaScriptForStockx(con, stmt, rs, req, generalUtils.stringFromBytes(itemCode, 0L), unm, sid, uty, men, den, dnm, bnm, javaScriptPrependCode, localDefnsDir, defnsDir, bytesOut);
          
          byte[] javaScriptCallCode = new byte[1000];
          javaScriptCallForStockx(con, stmt, rs, javaScriptCallCode, req, unm, uty, dnm, localDefnsDir, defnsDir, bytesOut);

          ddlDataUpto[0] = 0;

          char dpOnQuantities = miscDefinitions.dpOnQuantities(con, stmt, rs, "5");
          
          byte[] storeCode = new byte[50];

          byte[] list      = new byte[1000];
          int[] listLen    = new int[1];  listLen[0] = 1000;
          int[] numEntries = new int[1];

          list = documentUtils.getStoreList(con, stmt, rs, list, listLen, numEntries);
    
          for(short entry=0;entry<numEntries[0];++entry)
          {
            generalUtils.stringToBytes(generalUtils.dfsAsStrGivenBinary1(list, entry), 0, storeCode);

            if(getStockxRecGivenCodes(con, stmt, rs, itemCode, storeCode, '\001', dnm, data, localDefnsDir, defnsDir))
            {
              generalUtils.bytesDPsGivenSeparator(true, '\001', dpOnQuantities, data, 5000, 3);
              generalUtils.bytesDPsGivenSeparator(true, '\001', dpOnQuantities, data, 5000, 4);
              generalUtils.bytesDPsGivenSeparator(true, '\001', dpOnQuantities, data, 5000, 5);
              generalUtils.bytesDPsGivenSeparator(true, '\001', dpOnQuantities, data, 5000, 6);
              generalUtils.bytesDPsGivenSeparator(true, '\001', dpOnQuantities, data, 5000, 7);
              generalUtils.bytesDPsGivenSeparator(true, '\001', dpOnQuantities, data, 5000, 9);

              screenLayout.bufferToBuffer(buf1, buf2, size1, size2, source, numFields, fieldNames, fieldTypes, fieldSizes, 'D', 'L', data, 5000, ddlData, ddlDataUpto[0], javaScriptCallCode, javaScriptPrependCode);
            }
          }
        }  
        
        screenLayout.bufferToOut(buf1, buf2, source, out);
        rtn = true;
      }
    }
    else // a new layout
    {
      numFields = screenLayout.layoutToBuffer(buf1, buf2, size1, fieldNames, fieldTypes, fieldSizes, localDefnsDir, defnsDir, layoutToUse, 1, getFieldNamesStock(), fieldSizesStock, null, null, null, null);
      
      if(dataAlready[0] != '\000') // coming with an err msg - passing-in data already entered (but not yet saved)
        generalUtils.bytesToBytes(data, 0, dataAlready, 0);
      else
      {
        screenLayout.buildNewDetails(fieldNames, numFields, "stock", data);

        byte[] nextCode = new byte[21];
        documentUtils.getNextCode(con, stmt, rs, "stock", true, nextCode);
        generalUtils.repAlphaUsingOnes(data, 5000, "ItemCode", nextCode);

        generalUtils.repAlphaUsingOnes(data, 5000, "ShowToWeb", "N");
        generalUtils.repAlphaUsingOnes(data, 5000, "Special", "N");
        generalUtils.repAlphaUsingOnes(data, 5000, "CycleCountIgnore", "N");
        generalUtils.repAlphaUsingOnes(data, 5000, "Status", "L");
        generalUtils.repAlphaUsingOnes(data, 5000, "WrittenOff", "N");

        generalUtils.repAlphaUsingOnes(data, 5000, "DateChanged", generalUtils.today(localDefnsDir, defnsDir));
      }

      byte[] ddlData    = new byte[1000];
      int[] ddlDataLen  = new int[1]; ddlDataLen[0] = 1000;
      int[] ddlDataUpto = new int[1]; ddlDataUpto[0] = 0;

      ddlData = accountsUtils.getCurrencyNamesDDLData(con, stmt, rs, "stock.Currency", dnm, ddlData, ddlDataUpto, ddlDataLen, localDefnsDir, defnsDir);
      ddlData = accountsUtils.getCurrencyNamesDDLData(con, stmt, rs, "stock.SalesCurrency", dnm, ddlData, ddlDataUpto, ddlDataLen, localDefnsDir, defnsDir);

      prependEdit(con, stmt, rs, errStr, req, unm, sid, uty, men, den, dnm, bnm, 'C', generalUtils.stringFromBytes(itemCode, 0L), localDefnsDir, defnsDir, prependCode, bytesOut);

      screenLayout.bufferToBuffer(buf1, buf2, size1, size2, source, numFields, fieldNames, fieldTypes, fieldSizes, 'E', 'H', data, 5000, ddlData, ddlDataUpto[0], null, prependCode);

      screenLayout.bufferToOut(buf1, buf2, source, out);
      rtn = true;
    }

    return rtn;
  }
        
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void javaScriptCallForStockx(Connection con, Statement stmt, ResultSet rs, byte[] b, HttpServletRequest req, String unm, String uty, String dnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    b[0] = '\000';

    scoutln(b, bytesOut, "_.lineFile=stockx.store\001");
    
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3006, unm, uty, dnm, localDefnsDir, defnsDir))
      scoutln(b, bytesOut, "_.permitted=y\001");
    else scoutln(b, bytesOut, "_.permitted=n\001");
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void javaScriptForStockx(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, String code, String unm, String sid, String uty, String men, String den, String dnm, String bnm, byte[] b, String localDefnsDir,
                                   String defnsDir, int[] bytesOut) throws Exception
  {
    b[0] = '\000';
    
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3006, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(b, bytesOut, "<script language=\"JavaScript\">");
      scoutln(b, bytesOut, "function affect(storeCode){");
      scoutln(b, bytesOut, "window.location.replace(\"http://" + men + "/central/servlet/ProductStockxCreate?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1="
                           + generalUtils.sanitise(code) + "&p2=\"+storeCode+\"&p3=A&p4=\");");
      scoutln(b, bytesOut, "}</script>");
    }
  }
        
  // -------------------------------------------------------------------------------------------------------------------------------
  // picks-up the list of fieldnames for the stock file, and fetches the data value for that field; and rebuilds the data string
  private void sortFieldData(String stockOrStockx, byte[] ipBuf, byte[] opBuf) throws Exception
  {
    String thisFieldName, fieldNames = getFieldNamesStock();
    int x=0, y, len = fieldNames.length();
    byte[] thisFieldNameB = new byte[50];
    byte[] value          = new byte[1000];
    
    int recDataLen = generalUtils.lengthBytes(ipBuf, 0);
    
    opBuf[0] = '\000';

    while(x < len)
    {
      y=0;
      thisFieldName="";
      while(x < len && fieldNames.charAt(x) != ',')
      {
        thisFieldNameB[y++] = (byte)fieldNames.charAt(x);  
        thisFieldName += fieldNames.charAt(x++);
      }
      thisFieldNameB[y] = '\000';  
      ++x;
            
      while(x < len && fieldNames.charAt(x) == ' ')
        ++x;

      generalUtils.catAsBytes(stockOrStockx + thisFieldName + "=", 0, opBuf, false);

      if(searchDataString(ipBuf, recDataLen, "stock", thisFieldNameB, value) != -1) // entry exists
        generalUtils.catAsBytes(generalUtils.stringFromBytes(value, 0L), 0, opBuf, false);

      generalUtils.catAsBytes("\001", 0, opBuf, false);
    }

    generalUtils.bytesToBytes(ipBuf, 0, opBuf, 0);
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // sets : stock.code=acme\001stock.name=acme ltd\001...
  // returns: 0 or -1 if rec not found
  public int getStockRecGivenCode(Connection con, Statement stmt, ResultSet rs, String itemCode, char separator, byte[] data) throws Exception
  {
    byte[] b = new byte[21];
    generalUtils.strToBytes(b, itemCode);

    return getStockRecGivenCode(con, stmt, rs, b, separator, data);
  }
  public int getStockRecGivenCode(Connection con, Statement stmt, ResultSet rs, byte[] itemCode, char separator, byte[] data) throws Exception
  {
    return getStockRecGivenCode(con, stmt, rs, itemCode, separator, false, data);
  }
  public int getStockRecGivenCode(Connection con, Statement stmt, ResultSet rs, byte[] itemCode, char separator, boolean onlyShowToWeb, byte[] data) throws Exception
  {
    if(itemCode[0] == '\000') // just-in-case
      return -1;

    generalUtils.toUpper(itemCode, 0);

    stmt = con.createStatement();

    if(onlyShowToWeb)
      rs = stmt.executeQuery("SELECT * FROM stock WHERE ItemCode = '" + generalUtils.sanitiseForSQL(generalUtils.stringFromBytes(itemCode, 0L)) + "' AND ShowToWeb = 'Y'");
    else rs = stmt.executeQuery("SELECT * FROM stock WHERE ItemCode = '" + generalUtils.sanitiseForSQL(generalUtils.stringFromBytes(itemCode, 0L)) + "'");

    if(! rs.next())
    {
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
      return -1;
    }      
    
    ResultSetMetaData rsmd = rs.getMetaData();

    String fieldTypes = getFieldTypesStock();
     
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
      String fieldNames = getFieldNamesStock();
      String thisFieldName;
      char thisFieldType;
      boolean terminate = true;
    
      int x=0, count=0, len = fieldNames.length();
      while(x < len)
      {
        thisFieldName="";
        while(x < len && fieldNames.charAt(x) != ',')
          thisFieldName += fieldNames.charAt(x++);
        thisFieldType = fieldTypes.charAt(count++);
        generalUtils.catAsBytes("stock." + thisFieldName + "=" + getValue(count, thisFieldType, rs, rsmd) + "\001", 0, data, terminate);
      
        ++x;
        while(x < len && fieldNames.charAt(x) == ' ')
          ++x;
        terminate = false;
      }
    }  
  
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return 0;   
  }
    
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void getPurchaseDetailsGivenCode(Connection con, Statement stmt, ResultSet rs, String itemCode, String[] purchasePrice, String[] currency, String[] exchangeRate, String[] dateChanged) throws Exception
  {
    purchasePrice[0] = currency[0] = exchangeRate[0] = dateChanged[0] = "";

    if(itemCode.length() == 0) // just-in-case
      return;
    
    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT PurchasePrice, Currency, ExchangeRate, DateChanged FROM stock WHERE ItemCode = '" + generalUtils.sanitiseForSQL(itemCode) + "'");

    if(rs.next())
    {
      purchasePrice[0] = rs.getString(1);
      currency[0]      = rs.getString(2);
      exchangeRate[0]  = rs.getString(3);
      dateChanged[0]   = rs.getString(4);
    }
  
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------
  // sets : stock.code=acme\001stock.name=acme ltd\001...
  // returns: 0 or -1 if rec not found
  public int getStockRecGivenMfrAndMfrCode(Connection con, Statement stmt, ResultSet rs, String mfr, String mfrCode, char separator, String dnm, byte[] data, String localDefnsDir, String defnsDir) throws Exception
  {
    if(mfr.length() == 0 || mfrCode.length() == 0) // just-in-case
      return -1;

    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT * FROM stock WHERE Manufacturer = '" + generalUtils.sanitiseForSQL(mfr) + "' AND ManufacturerCode = '" + generalUtils.sanitiseForSQL(mfrCode) + "'");
    if(! rs.next())
    {
      rs.close();
      stmt.close();
      return -1;
    }      
    
    ResultSetMetaData rsmd = rs.getMetaData();

    String fieldTypes = getFieldTypesStock();
     
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
      String fieldNames = getFieldNamesStock();
      String thisFieldName;
      char thisFieldType;
      boolean terminate = true;
    
      int x=0, count=0, len = fieldNames.length();
      while(x < len)
      {
        thisFieldName="";
        while(x < len && fieldNames.charAt(x) != ',')
          thisFieldName += fieldNames.charAt(x++);
        thisFieldType = fieldTypes.charAt(count++);
        generalUtils.catAsBytes("stock." + thisFieldName + "=" + getValue(count, thisFieldType, rs, rsmd) + "\001", 0, data, terminate);
      
        ++x;
        while(x < len && fieldNames.charAt(x) == ' ')
          ++x;
        terminate = false;
      }
    }  
  
    rs.close();
    stmt.close();
    
    return 0;   
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
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
//                                      str = generalUtils.sanitiseForSQL(str);
                                      break;
      }

      return str;
    }
    catch(Exception e)
    {
      System.out.println(e);
    }

    return "";
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------
  private void prepend(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm,
                       String bnm, String localDefnsDir, String defnsDir, String code, byte[] b, int[] bytesOut)
                       throws Exception
  {
    b[0] = '\000';
    scoutln(b, bytesOut, "<html><head><title>" + code + "</title>");
    scoutln(b, bytesOut, "<script language=\"JavaScript\">\n");

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3003, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(b, bytesOut, "function fetch(){");
      scoutln(b, bytesOut, "window.location.replace(\"http://" + men + "/central/servlet/ProductStockEdit?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1="
                           + generalUtils.sanitise(code) + "&p2=&p3=&p4=A\");}\n");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3006, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(b, bytesOut, "function add(){");
      scoutln(b, bytesOut, "window.location.replace(\"http://" + men + "/central/servlet/ProductStockxCreate?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1="
                           + generalUtils.sanitise(code) + "&p2=&p3=C&p4=\");}\n");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 11800, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(b, bytesOut, "function trail(){");
      scoutln(b, bytesOut, "window.location.href=\"/central/servlet/TrailShow?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + generalUtils.sanitise(code) + "&p2=X\";}");
    }

    scoutln(b, bytesOut, "</script>\n");

    String cssDirectory = directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir);
    String cssDocumentDirectory = directoryUtils.getCssDocumentDirectory(con, stmt, rs, dnm);
    scoutln(b, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + cssDirectory + "general.css\">\n");
    scoutln(b, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + cssDocumentDirectory + "document.css\">\n");
   
    outputPageFrame(con, stmt, rs, b, req, ' ', "", "ProductStockRecord", code, unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, bytesOut);

    scoutln(b, bytesOut, "<form>");
  }

  // ------------------------------------------------------------------------------------------------------------------------------------------------------
  private void prependEdit(Connection con, Statement stmt, ResultSet rs, String errStr, HttpServletRequest req, String unm, String sid, String uty, String men,
                             String den, String dnm, String bnm, char cad, String code, String localDefnsDir, String defnsDir,
                             byte[] b, int[] bytesOut) throws Exception
  {
    String code2 = generalUtils.sanitise(code);
    scoutln(b, bytesOut, "<html><head><title>" + code + "</title>");
    
    scoutln(b, bytesOut, "<script language=\"JavaScript\">\n");
    scoutln(b, bytesOut, "function save(option){");
    scoutln(b, bytesOut, "document.forms[0].p4.value=option;");
    scoutln(b, bytesOut, "document.go.submit();}\n");
    scoutln(b, bytesOut, "</script>\n");

    String cssDirectory = directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir);
    String cssDocumentDirectory = directoryUtils.getCssDocumentDirectory(con, stmt, rs, dnm);
    scoutln(b, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + cssDirectory + "general.css\">\n");
    scoutln(b, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + cssDocumentDirectory + "document.css\">\n");

    scoutln(b, bytesOut, "<form name=go action=\"ProductStockUpdate\" enctype=\"application/x-www-form-urlencoded\" method=POST>");
    scoutln(b, bytesOut, "<input type=hidden name=unm value="+unm+">");
    scoutln(b, bytesOut, "<input type=hidden name=sid value="+sid+">");
    scoutln(b, bytesOut, "<input type=hidden name=uty value="+uty+">");
    scoutln(b, bytesOut, "<input type=hidden name=men value=\""+men+"\">");
    scoutln(b, bytesOut, "<input type=hidden name=den value="+den+">");
    scoutln(b, bytesOut, "<input type=hidden name=dnm value="+dnm+">");
    scoutln(b, bytesOut, "<input type=hidden name=bnm value="+bnm+">");

    scoutln(b, bytesOut, "<input type=hidden name=p1 value="+cad+">");
    scoutln(b, bytesOut, "<input type=hidden name=p2 value="+code2+">");
    scoutln(b, bytesOut, "<input type=hidden name=p4 value=''>");

    outputPageFrame(con, stmt, rs, b, req, cad, "", "ProductStockUpdate", code, unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, bytesOut);

    if(errStr.length() > 0 && ! errStr.equalsIgnoreCase("null"))
      generalUtils.catAsBytes(errStr, 0, b, false);
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------
  // rtns: 'X' if already exists
  //       ' ' if updated successfully
  //       'N' if no code specified
  //       'F' if not updated
  //       'I' if illegal code
  public char stockxPutRec(Connection con, Statement stmt, ResultSet rs, byte[] itemCode, String originalStoreCode, char cad, String unm, String dnm, String localDefnsDir, String defnsDir, byte[] recData, int recDataLen) throws Exception
  {
    if(itemCode[0] == '\000')
      return 'N';

    byte[] b = new byte[100];
    byte[] storeCode = new byte[21];
    generalUtils.catAsBytes("Store", 0, b, true);

    if(searchDataString(recData, recDataLen, "stockx", b, storeCode) == -1)
      return 'N';

    if(storeCode[0] == '\000') return ' ';

    byte[] location = new byte[50];
    generalUtils.catAsBytes("Location", 0, b, true);

    if(searchDataString(recData, recDataLen, "stockx", b, location) == -1)
      return 'N';

    char newOrEdit;
    if(cad == 'C')
      newOrEdit = 'N';
    else newOrEdit = 'E';

    generalUtils.toUpper(itemCode, 0);

    if(existsStockxRecGivenCodes(con, stmt, rs, itemCode, storeCode, dnm, localDefnsDir, defnsDir))
    {
      if(cad != 'A') // creating
        return 'X';
    }
            
    // get data values from recData and put into buf for updating
    byte[] buf = new byte[1000];

    generalUtils.putAlpha(buf, 1000, (short)0, itemCode);

    // determines the number of fields and then processes them in order *but* makes no assumptions about order of fields in data
    
    String fieldNames = getFieldNamesStockx();
    byte[] value     = new byte[100];
    byte[] fieldName = new byte[31];
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

      if(searchDataString(recData, recDataLen, "stockx", fieldName, value) != -1) // entry exists
      {
        if(fieldCount == 12) // laststockcheckdate
        {
          if(value[0] == '\000')
            generalUtils.putAlpha(buf, 1000, (short)12, "1970-01-01");
          else generalUtils.putAlpha(buf, 1000, (short)12, generalUtils.convertDateToSQLFormat(generalUtils.stringFromBytes(value, 0L)));  
        }
        else
        if(fieldCount == 8) // signOn
           generalUtils.putAlpha(buf, 1000, (short)8, unm);
        else generalUtils.repAlpha(buf, 1000, (short)fieldCount, value);
      }
      else
      {
        if(fieldCount == 12) // laststockcheckdate
          generalUtils.putAlpha(buf, 1000, (short)fieldCount, "1970-01-01");
        else
        if(fieldCount == 3 || fieldCount == 4 || fieldCount == 5 || fieldCount == 6 || fieldCount == 7 || fieldCount == 9)
          generalUtils.repAlpha(buf, 1000, (short)fieldCount, "0");
      }
      
      ++fieldCount;
    }

    if(putStockxRecGivenCodes(con, stmt, rs, itemCode, originalStoreCode, '0', newOrEdit, buf, dnm, localDefnsDir, defnsDir))
      return ' ';

    return 'F';
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean existsStockxRecGivenCodes(Connection con, Statement stmt, ResultSet rs, String itemCode, String storeCode, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    byte[] itemCodeB  = new byte[21];
    byte[] storeCodeB = new byte[50];
    
    generalUtils.strToBytes(itemCodeB,  itemCode);
    generalUtils.strToBytes(storeCodeB, storeCode);

    return existsStockxRecGivenCodes(con, stmt, rs, itemCodeB, storeCodeB, dnm, localDefnsDir, defnsDir);
  }
  public boolean existsStockxRecGivenCodes(Connection con, Statement stmt, ResultSet rs, byte[] itemCode, byte[] storeCode, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    byte[] data = new byte[2000];
      
    return getStockxRecGivenCodes(con, stmt, rs, itemCode, storeCode, '\000', dnm, data, localDefnsDir, defnsDir);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // format of data: "do.docode="12345/DO\001 ..."
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

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public double stockLevelForAStore(Connection con, Statement stmt, Statement stmt2, ResultSet rs, String storeWanted, String itemCode, String date, String unm, String sid, String uty, String men, String den, String dnm, String bnm)
  {
    try
    {
    byte[] itemCodeB = new byte[21];
    generalUtils.strToBytes(itemCodeB, itemCode);
    
    if(! existsItemRecGivenCode(con, stmt, rs, itemCode))
      return 0.0;

    // traceList = "SamLeong\001150000\001SyedAlwi\00150000\001\0"
    String traceList = stockLevelsGenerate.fetch(con, stmt, stmt2, rs, itemCode, date, "", unm, sid, uty, men, den, dnm, bnm);

    int y=0, len = traceList.length();
    String thisStore, thisQty;

    while(y < len) // just-in-case
    {
      thisStore = "";
      while(y < len && traceList.charAt(y) != '\001')
        thisStore += traceList.charAt(y++);
      ++y;

      thisQty = "";
      while(y < len && traceList.charAt(y) != '\001')
        thisQty += traceList.charAt(y++);
      ++y;

      if(storeWanted.length() == 0 || thisStore.equals(storeWanted))
        return generalUtils.doubleFromStr(thisQty);
    }

    return 0.0; // just-in-case
    }
    catch(Exception e) { return 0.0; }
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public void createNewStockXRec(Connection con, Statement stmt, ResultSet rs, String itemCode, String store, String unm, String dnm, String localDefnsDir, String defnsDir)
                                 throws Exception
  {
    byte[] itemCodeB = new byte[21];
    byte[] storeB    = new byte[50];
    
    generalUtils.strToBytes(itemCodeB, itemCode);
    generalUtils.strToBytes(storeB,    store);
   
    createNewStockXRec(con, stmt, rs, itemCodeB, storeB, unm, dnm, localDefnsDir, defnsDir);
  }
  public void createNewStockXRec(Connection con, Statement stmt, ResultSet rs, byte[] itemCode, byte[] store, String unm, String dnm, String localDefnsDir, String defnsDir)
                                 throws Exception
  {
    byte[] data = new byte[2000];

    generalUtils.catAsBytes("stockx.itemCode=\"" + generalUtils.stringFromBytes(itemCode, 0L) + "\" ", 0, data, true);
    generalUtils.catAsBytes("stockx.store=\""    + generalUtils.stringFromBytes(store, 0L)    + "\" ", 0, data, false);
    generalUtils.catAsBytes("stockx.signOn=\""   + unm + "\" ", 0, data, false);

    byte[] originalItemCode = new byte[1];  originalItemCode[0] = '\000';
    stockxPutRec(con, stmt, rs, originalItemCode, "", 'C', unm, dnm, localDefnsDir, defnsDir, data, 2000);
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean isAStockItem(Connection con, Statement stmt, ResultSet rs, String itemCode) throws Exception
  {
    byte[] itemCodeB = new byte[21];
    
    generalUtils.strToBytes(itemCodeB, itemCode);
    
    return isAStockItem(con, stmt, rs, itemCodeB);
  }
  public boolean isAStockItem(Connection con, Statement stmt, ResultSet rs, byte[] itemCode) throws Exception
  {
    if(itemCode[0] == '\000') // just-in-case
      return false;

    generalUtils.toUpper(itemCode, 0);

    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM stock WHERE ItemCode = '" + generalUtils.stringFromBytes(itemCode, 0L) + "'");
    rs.next();      
    int numRecs = rs.getInt("rowcount") ;
    rs.close() ;
   
    stmt.close();
    
    if(numRecs == 1)
      return true;
    
    return false;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void mergeStockLevels(Connection con, Statement stmt, ResultSet rs, byte[] oldItemCode, byte[] newItemCode, String unm, String dnm, String localDefnsDir, String defnsDir)
                               throws Exception
  {
    byte[] storeCode = new byte[21];
    byte[] data      = new byte[2000];
    String name;
    double currentQty, openBalance;

    byte[] list = new byte[1000];
    int[] listLen  = new int[1];  listLen[0] = 1000;
    int[] numEntries = new int[1];

    list = documentUtils.getStoreList(con, stmt, rs, list, listLen, numEntries);
    
    for(short entry=0;entry<numEntries[0];++entry)
    {
      name = generalUtils.dfsAsStrGivenBinary1(list, entry);

      generalUtils.stringToBytes(name, 0, storeCode);

      // if stockx rec exists for old rec
      //   fetch the stockx rec for the new rec
      //   if a new rec does not exist, then simply save the old stockx rec as the new stockx rec
      //   else add the old currentQty to the new currentQty, add the old openBalance to the new openBalance, and save new
      // else do nowt
      if(getStockxRecGivenCodes(con, stmt, rs, oldItemCode, storeCode, '\000', dnm, data, localDefnsDir, defnsDir)) // old exists
      {
        currentQty  = generalUtils.dfsAsDouble(data, (short)3);
        openBalance = generalUtils.dfsAsDouble(data, (short)9);

        if(! getStockxRecGivenCodes(con, stmt, rs, newItemCode, storeCode, '\000', dnm, data, localDefnsDir, defnsDir)) // new does NOT exist
        {
          generalUtils.zeroize(data, 2000);
          generalUtils.putAlpha(data, 2000, (short)0, newItemCode);
          generalUtils.putAlpha(data, 2000, (short)1, storeCode);
          generalUtils.putAlpha(data, 2000, (short)8, unm);
          generalUtils.putAlpha(data, 2000, (short)3, generalUtils.doubleToStr('8', currentQty));
          generalUtils.putAlpha(data, 2000, (short)9, generalUtils.doubleToStr('8', openBalance));

          putStockxRecGivenCodes(con, stmt, rs, newItemCode, "", '\000', 'N', data, dnm, localDefnsDir, defnsDir);
        }
        else
        {
          stmt = con.createStatement();
          stmt.executeUpdate("UPDATE stockx SET CurrentQty = " + currentQty + ", OpenBalance = " + openBalance
                           + " WHERE ItemCode = '" + generalUtils.stringFromBytes(oldItemCode, 0L) + "'");
        
          if(stmt != null) stmt.close();
        }
      }
    }
  }

  // ------------------------------------------------------------------------------------------------------------------------------------------------------
  // rtns: 'X' if already exists
  //       ' ' if updated sucessfully
  //       'N' if no code specified
  //       'F' if not updated
  //       'I' if illegal code
  public char stockPutRec(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, byte[] originalStockCode, String unm, String uty, String dnm, String localDefnsDir, String defnsDir, char cad, byte[] recData, int recDataLen,
                          byte[] codeBytes) throws Exception
  {
    byte[] b = new byte[21];
    generalUtils.catAsBytes("ItemCode", 0, b, true);

    if(searchDataString(recData, recDataLen, "stock", b, codeBytes) == -1)
      return 'N';

    char newOrEdit;

    if(cad == 'A')
      newOrEdit = 'E';
    else
    if(originalStockCode[0] == '\000')
      newOrEdit = 'N';
    else  // originalcode not blank
    {
      if(generalUtils.matchIgnoreCase(originalStockCode, 0, codeBytes, 0))
        newOrEdit = 'E';
      else // change in the code, or rec with no itemcode supplied
        newOrEdit = 'N';
    }

    generalUtils.toUpper(codeBytes, 0);

    // get data values from recData and put into buf for updating
    byte[] buf = new byte[2000];

    generalUtils.putAlpha(buf, 2000, (short)0, codeBytes);

    String[] purchasePrice = new String[1];
    String[] currency      = new String[1];
    String[] exchangeRate  = new String[1];
    String[] dateChanged   = new String[1];

    if((! authenticationUtils.verifyAccess(con, stmt, rs, req, 3002, unm, uty, dnm, localDefnsDir, defnsDir)) && newOrEdit == 'E')
      getPurchaseDetailsGivenCode(con, stmt, rs, generalUtils.stringFromBytes(codeBytes, 0L), purchasePrice, currency, exchangeRate, dateChanged);     

    // determines the number of fields and then processes them in order *but* makes no assumptions about order of fields in data
    
    String fieldNames = getFieldNamesStock();
    byte[] value     = new byte[100];
    byte[] fieldName = new byte[31];
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

      if(searchDataString(recData, recDataLen, "stock", fieldName, value) != -1) // entry exists
      {
        if(fieldCount == 14) // misc6
        {
          if(value[0] == '\000')
            generalUtils.putAlpha(buf, 2000, (short)14, "0");
          else generalUtils.putAlpha(buf, 2000, (short)14, value);
        }
        else
        if(fieldCount == 15) // misc7
        {
          if(value[0] == '\000')
            generalUtils.putAlpha(buf, 2000, (short)15, "0");
          else generalUtils.putAlpha(buf, 2000, (short)15, value);
        }
        else
        if(fieldCount == 16) // misc8
        {
          if(value[0] == '\000')
            generalUtils.putAlpha(buf, 2000, (short)16, "0");
          else generalUtils.putAlpha(buf, 2000, (short)16, value);
        }
        else
        if(fieldCount == 18) // purchaseprice
        {
          if(value[0] == '\000')
            generalUtils.putAlpha(buf, 2000, (short)18, "0");
          else generalUtils.putAlpha(buf, 2000, (short)18, value);
        }
        else
        if(fieldCount == 20) // sellPrice1
        {
          if(value[0] == '\000')
            generalUtils.putAlpha(buf, 2000, (short)20, "0");
          else generalUtils.putAlpha(buf, 2000, (short)20, value);
        }
        else
        if(fieldCount == 21) // sellPrice2
        {
          if(value[0] == '\000')
            generalUtils.putAlpha(buf, 2000, (short)21, "0");
          else generalUtils.putAlpha(buf, 2000, (short)21, value);
        }
        else
        if(fieldCount == 22) // sellPrice3
        {
          if(value[0] == '\000')
            generalUtils.putAlpha(buf, 2000, (short)22, "0");
          else generalUtils.putAlpha(buf, 2000, (short)22, value);
        }
        else
        if(fieldCount == 23) // sellPrice4
        {
          if(value[0] == '\000')
            generalUtils.putAlpha(buf, 2000, (short)23, "0");
          else generalUtils.putAlpha(buf, 2000, (short)23, value);
        }
        else
        if(fieldCount == 24) // exchRate
        {
          if(value[0] == '\000')
            generalUtils.putAlpha(buf, 2000, (short)24, "0");
          else generalUtils.putAlpha(buf, 2000, (short)24, value);
        }
        else
        if(fieldCount == 25) // rrp
        {
          if(value[0] == '\000')
            generalUtils.putAlpha(buf, 2000, (short)25, "0");
          else generalUtils.putAlpha(buf, 2000, (short)25, value);
        }
        else
        if(fieldCount == 26) // ppToRRPFactor
        {
          if(value[0] == '\000')
            generalUtils.putAlpha(buf, 2000, (short)26, "0");
          else generalUtils.putAlpha(buf, 2000, (short)26, value);
        }
        else
        if(fieldCount == 31) // specialPrice
        {
          if(value[0] == '\000')
            generalUtils.putAlpha(buf, 2000, (short)31, "0");
          else generalUtils.putAlpha(buf, 2000, (short)31, value);
        }
        else
        if(fieldCount == 38) // markup1
        {
          if(value[0] == '\000')
            generalUtils.putAlpha(buf, 2000, (short)38, "0");
          else generalUtils.putAlpha(buf, 2000, (short)38, value);
        }
        else
        if(fieldCount == 39) // markup2
        {
          if(value[0] == '\000')
            generalUtils.putAlpha(buf, 2000, (short)39, "0");
          else generalUtils.putAlpha(buf, 2000, (short)39, value);
        }
        else
        if(fieldCount == 40) // markup3
        {
          if(value[0] == '\000')
            generalUtils.putAlpha(buf, 2000, (short)40, "0");
          else generalUtils.putAlpha(buf, 2000, (short)40, value);
        }
        else
        if(fieldCount == 41) // markup4
        {
          if(value[0] == '\000')
            generalUtils.putAlpha(buf, 2000, (short)41, "0");
          else generalUtils.putAlpha(buf, 2000, (short)41, value);
        }
        else
        if(fieldCount == 44) // newPP
        {
          if(value[0] == '\000')
            generalUtils.putAlpha(buf, 2000, (short)44, "0");
          else generalUtils.putAlpha(buf, 2000, (short)44, value);
        }
        else
        if(fieldCount == 46) // weight
        {
          if(value[0] == '\000')
            generalUtils.putAlpha(buf, 2000, (short)46, "0");
          else generalUtils.putAlpha(buf, 2000, (short)46, value);
        }
        else
        if(fieldCount == 47) // weightper
        {
          if(value[0] == '\000')
            generalUtils.putAlpha(buf, 2000, (short)47, "0");
          else generalUtils.putAlpha(buf, 2000, (short)47, value);
        }
        else
        if(fieldCount == 55) // categoryCode
        {
          if(value[0] == '\000')
            generalUtils.putAlpha(buf, 2000, (short)55, "0");
          else generalUtils.putAlpha(buf, 2000, (short)55, value);
        }
        else
        if(fieldCount == 32) // specialPriceFromDate
        {
          if(value[0] == '\000')
            generalUtils.putAlpha(buf, 2000, (short)32, "1970-01-01");
          else generalUtils.putAlpha(buf, 2000, (short)32, generalUtils.convertDateToSQLFormat(generalUtils.stringFromBytes(value, 0L)));  
        }
        else
        if(fieldCount == 33) // specialPriceToDate
        {
          if(value[0] == '\000')
            generalUtils.putAlpha(buf, 2000, (short)33, "1970-01-01");
          else generalUtils.putAlpha(buf, 2000, (short)33, generalUtils.convertDateToSQLFormat(generalUtils.stringFromBytes(value, 0L)));  
        }
        else
        if(fieldCount == 45) // newPurchasePriceFromDate
        {
          if(value[0] == '\000')
            generalUtils.putAlpha(buf, 2000, (short)45, "1970-01-01");
          else generalUtils.putAlpha(buf, 2000, (short)45, generalUtils.convertDateToSQLFormat(generalUtils.stringFromBytes(value, 0L)));  
        }
        else
        if(fieldCount == 50) // dateChanged
        {
          if(value[0] == '\000')
            generalUtils.putAlpha(buf, 2000, (short)50, "1970-01-01");
          else generalUtils.putAlpha(buf, 2000, (short)50, generalUtils.convertDateToSQLFormat(generalUtils.stringFromBytes(value, 0L)));  
        }
        else
        if(fieldCount == 34) // signOn
           generalUtils.putAlpha(buf, 2000, (short)34, unm);
        else
        if(fieldCount == 53) // showToWeb
          generalUtils.repAlpha(buf, 2000, (short)fieldCount, "Y");
        else
        if(fieldCount == 37) // writtenOff
          generalUtils.repAlpha(buf, 2000, (short)fieldCount, "Y");
        else
        if(fieldCount == 30) // special
          generalUtils.repAlpha(buf, 2000, (short)fieldCount, "Y");
        else
        if(fieldCount == 54) // cyclecountignore
          generalUtils.repAlpha(buf, 2000, (short)fieldCount, "Y");
        else
        if(fieldCount == 56) // closingWAC
        {
          if(value[0] == '\000')
            generalUtils.putAlpha(buf, 2000, (short)56, "0");
          else generalUtils.putAlpha(buf, 2000, (short)56, value);
        }
        else
        if(fieldCount == 57) // closingLevel
        {
          if(value[0] == '\000')
            generalUtils.putAlpha(buf, 2000, (short)57, "0");
          else generalUtils.putAlpha(buf, 2000, (short)57, value);
        }
        else
        if(fieldCount == 58) // closingDate
        {
          if(value[0] == '\000')
            generalUtils.putAlpha(buf, 2000, (short)58, "1970-01-01");
          else generalUtils.putAlpha(buf, 2000, (short)58, generalUtils.convertDateToSQLFormat(generalUtils.stringFromBytes(value, 0L)));  
        }
        else
        if(fieldCount == 61) // productwaveid
        {
          if(value[0] == '\000')
            generalUtils.putAlpha(buf, 2000, (short)61, "0");
          else generalUtils.putAlpha(buf, 2000, (short)61, value);
        }
        else generalUtils.repAlpha(buf, 2000, (short)fieldCount, value);
      }
      else // no entry exists for this field
      {        
        if(fieldCount == 32 || fieldCount == 33 || fieldCount == 45 || fieldCount == 50 || fieldCount == 58) // specialPriceFromDate || specialPriceToDate ||
        {                                                                                                    // newPurchasePriceFromDate || dateChanged }} closingDate
          generalUtils.putAlpha(buf, 2000, (short)fieldCount, "1970-01-01");
        }
        else
        if(fieldCount == 53) // showToWeb
          generalUtils.repAlpha(buf, 2000, (short)fieldCount, "N");
        else
        if(fieldCount == 37) // writtenoff
          generalUtils.repAlpha(buf, 2000, (short)fieldCount, "N");
        else
        if(fieldCount == 30) // special
          generalUtils.repAlpha(buf, 2000, (short)fieldCount, "N");
        else
        if(fieldCount == 54) // CycleCountIgnore
        {
          generalUtils.repAlpha(buf, 2000, (short)fieldCount, "N");
        }
        else
        if(fieldCount == 61) // ProductWaveID
        {
          generalUtils.repAlpha(buf, 2000, (short)fieldCount, "0");
        }
        else
        if(   fieldCount == 14 || fieldCount == 15 || fieldCount == 16 || fieldCount == 18 || fieldCount == 20 || fieldCount == 21
           || fieldCount == 22 || fieldCount == 23 || fieldCount == 24 || fieldCount == 25 || fieldCount == 26 || fieldCount == 31
           || fieldCount == 38 || fieldCount == 39 || fieldCount == 40 || fieldCount == 41 || fieldCount == 44 || fieldCount == 46
           || fieldCount == 47 || fieldCount == 55 || fieldCount == 56 || fieldCount == 57)
        {
          generalUtils.repAlpha(buf, 2000, (short)fieldCount, "0");
        }

        // if the user does not have purchasePrice access, must use the existing values from the 
        if((! authenticationUtils.verifyAccess(con, stmt, rs, req, 3002, unm, uty, dnm, localDefnsDir, defnsDir)) && newOrEdit == 'E')
        {
          if(fieldCount == 18) // purchaseprice
            generalUtils.repAlpha(buf, 2000, (short)18, purchasePrice[0]);
          else
          if(fieldCount == 24) // exchRate
            generalUtils.repAlpha(buf, 2000, (short)24, exchangeRate[0]);
          else
          if(fieldCount == 50) // dateChanged
            generalUtils.repAlpha(buf, 2000, (short)50, dateChanged[0]);
          else
          if(fieldCount == 19) // currency
            generalUtils.repAlpha(buf, 2000, (short)19, currency[0]);
        }
      }
      
      ++fieldCount;
    }

   if(putStockRecGivenCode(con, stmt, rs, '0', originalStockCode, newOrEdit, buf, dnm, localDefnsDir, defnsDir))
      return ' ';

    return 'F';
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean putStockRecGivenCode(Connection con, Statement stmt, ResultSet rs, byte[] originalStockCode, char newOrEdit, byte[] data, String dnm, String localDefnsDir, String defnsDir)
                                      throws Exception
  {
    return putStockRecGivenCode(con, stmt, rs, '0', originalStockCode, newOrEdit, data, dnm, localDefnsDir, defnsDir);
  }
  public boolean putStockRecGivenCode(Connection con, Statement stmt, ResultSet rs, char separator, String originalStockCode, char newOrEdit, byte[] data, String dnm, String localDefnsDir,
                                      String defnsDir) throws Exception
  {
    byte[] originalStockCodeB = new byte[21];
    generalUtils.strToBytes(originalStockCodeB, originalStockCode);

    return putStockRecGivenCode(con, stmt, rs, '0', originalStockCodeB, newOrEdit, data, dnm, localDefnsDir, defnsDir);
  }
  public boolean putStockRecGivenCode(Connection con, Statement stmt, ResultSet rs, char separator, byte[] originalStockCode, char newOrEdit, byte[] data, String dnm, String localDefnsDir,
                                      String defnsDir) throws Exception
  {
    try
    {
      stmt = con.createStatement();

      // if saving new rec
      //   if separator == '\001'
      //   change: Company.CompanyCode="abc"\001Company.Name="def"\001... format into: 'abc','def',... (including e.g., {d '...'})
      //   else if separator == '\000'
      //   change: abc\0def\0... format into: 'abc','def',...
      // else if editing existing rec
      //   if separator == '\001'
      //   change: Company.CompanyCode="abc"\001Company.Name="def"\001... format into: CompanyCode='abc',Name='def',...
      //   else if separator == '\000'
      //   change: abc\0def\0... format into: CompanyCode='abc',Name='def',...

      String q;
      String opStr="";
      boolean first = true;
      String fieldTypes = getFieldTypesStock();
      int x=0, count=0;

      if(newOrEdit == 'N')
      {
        if(separator == '\001')
        {
          // change: Company.CompanyCode="abc"\001Company.Name="def"\001... format into: 'abc','def',... (including e.g., {d '...'})
          
          char thisFieldType;
          while(data[x] != '\000')
          {
            while(data[x] != '\000' && data[x] != '"')
              ++x;
            ++x;

            if(! first)
              opStr += ",";
            else first = false;
            
            thisFieldType = fieldTypes.charAt(count++);
            
            if(thisFieldType == 'S')
            {    
              opStr += "NULL";
              while(data[x] != '\000' && data[x] != '"')
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
              while(data[x] != '\000')
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
            
            while(data[x] != '\000' && data[x] == '\001')
              ++x;
            if(data[x] == '\001')
              ++x;
          }
        }
        else // separator == '\000';
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
              while(data[x] != '\000')
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
        }
      
        q = "INSERT INTO stock (" + getFieldNamesStock() + ") VALUES (" + opStr + ")";
      }
      else // newOrEdit == 'E'
      {
        String thisEntry, thisFieldName;
        char thisFieldType;

        if(separator == '\001')
        {
          // change: Company.CompanyCode="abc"\001Company.Name="def"\001... format into: CompanyCode='abc',Name='def',...
          
          while(data[x] != '\000')
          {
            while(data[x] != '\000' && data[x] != '.')
              ++x;
            if(data[x] == '.')
            {  
              ++x;
              thisFieldName = "";
              while(data[x] != '\000' && data[x] != '=')
                thisFieldName += (char)data[x];
 
              ++x;
              while(data[x] != '\000' && data[x] != '"')
                ++x;
              ++x;

              if(! first)
                opStr += ",";
              else first = false;
            
              opStr += (thisFieldName + "=");
              
              thisFieldType = fieldTypes.charAt(count++);
              if(thisFieldType == 'S')
              {    
                opStr += "NULL";
                while(data[x] != '\000' && data[x] != '"')
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
                thisEntry = "";
                while(data[x] != '\000' && data[x] != '"')
                {  
                  if(data[x] == '\'')
                    thisEntry += "''"; 
                  else
                  if(data[x] == '"')
                    thisEntry += "''''"; 
                  else thisEntry += (char)data[x];
                  ++x;
                }
                
                opStr += thisEntry;
                opStr += "'";
            
                if(thisFieldType == 'D' || thisFieldType == 'T')
                  opStr += "}";
              } 
 
              // pickup the companyCode (in case it has been changed)            
              
              while(data[x] != '\000' && data[x] == '\001')
                ++x;
              if(data[x] == '\001')
                ++x;
            }  
          }
        }
        else // separator == '\000';
        {
          // change: abc\0def\0... format into: CompanyCode='abc',Name='def',...

          String fieldNames = getFieldNamesStock();
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
              thisEntry = "";
              while(data[y] != '\000')
              {
                if(data[y] == '\'')
                  thisEntry += "''"; 
                else
                if(data[y] == '"')
                  thisEntry += "''''"; 
                else thisEntry += (char)data[y];
                ++y;
              }
              ++y;
              
              opStr += thisEntry;
              opStr += "'";

              if(thisFieldType == 'D' || thisFieldType == 'T')
                opStr += "}";
            }
            
            // pickup the companyCode (in case it has been changed)          
          
            while(x < len && fieldNames.charAt(x) == ' ')
              ++x;
          }
        }  

        q = "UPDATE stock SET " + opStr + " WHERE ItemCode = '" + generalUtils.stringFromBytes(originalStockCode, 0L) + "'";
      }

      stmt.executeUpdate(q);

      stmt.close();
    }
    catch(Exception e) { System.out.println("inventory: " + e); return false; }
    
    return true;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean putStockxRecGivenCodes(Connection con, Statement stmt, ResultSet rs, byte[] itemCode, String originalStoreCode, char separator, char newOrEdit, byte[] data, String dnm,
                                         String localDefnsDir, String defnsDir) throws Exception
  {
    generalUtils.toUpper(itemCode, 0);

    try
    {
      stmt = con.createStatement();

      // if saving new rec
      //   if separator == '\001'
      //   change: Company.CompanyCode="abc"\001Company.Name="def"\001... format into: 'abc','def',... (including e.g., {d '...'})
      //   else if separator == '\000'
      //   change: abc\0def\0... format into: 'abc','def',...
      // else if editing existing rec
      //   if separator == '\001'
      //   change: Company.CompanyCode="abc"\001Company.Name="def"\001... format into: CompanyCode='abc',Name='def',...
      //   else if separator == '\000'
      //   change: abc\0def\0... format into: CompanyCode='abc',Name='def',...

      String q;
      String opStr="";
      boolean first = true;
      String fieldTypes = getFieldTypesStockx();
      int x=0, count=0;

      if(newOrEdit == 'N')
      {
        if(separator == '\001')
        {
          // change: Company.CompanyCode="abc"\001Company.Name="def"\001... format into: 'abc','def',... (including e.g., {d '...'})
          
          char thisFieldType;
          while(data[x] != '\000')
          {
            while(data[x] != '\000' && data[x] != '"')
              ++x;
            ++x;

            if(! first)
              opStr += ",";
            else first = false;
            
            thisFieldType = fieldTypes.charAt(count++);
            
            if(thisFieldType == 'S')
            {    
              opStr += "NULL";
              while(data[x] != '\000' && data[x] != '"')
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
            
            while(data[x] != '\000' && data[x] == '\001')
              ++x;
            if(data[x] == '\001')
              ++x;
          }
        }
        else // separator == '\000';
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
        }
      
        q = "INSERT INTO stockx (" + getFieldNamesStockx() + ") VALUES (" + opStr + ")";
      }
      else // newOrEdit == 'E'
      {
        String thisEntry, thisFieldName;
        char thisFieldType;

        if(separator == '\001')
        {
          // change: Company.CompanyCode="abc"\001Company.Name="def"\001... format into: CompanyCode='abc',Name='def',...
          
          while(data[x] != '\000')
          {
            while(data[x] != '\000' && data[x] != '.')
              ++x;
            if(data[x] == '.')
            {  
              ++x;
              thisFieldName = "";
              while(data[x] != '\000' && data[x] != '=')
                thisFieldName += (char)data[x];
 
              ++x;
              while(data[x] != '\000' && data[x] != '"')
                ++x;
              ++x;

              if(! first)
                opStr += ",";
              else first = false;
            
              opStr += (thisFieldName + "=");
              
              thisFieldType = fieldTypes.charAt(count++);
              if(thisFieldType == 'S')
              {    
                opStr += "NULL";
                while(data[x] != '\000' && data[x] != '"')
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
                thisEntry = "";
                while(data[x] != '\000')// && data[x] != '"')
                {  
                  if(data[x] == '\'')
                    thisEntry += "''"; 
                  else
                  if(data[x] == '"')
                    thisEntry += "''''"; 
                  else thisEntry += (char)data[x];
                  ++x;
                }
                
                opStr += thisEntry;
                opStr += "'";
            
                if(thisFieldType == 'D' || thisFieldType == 'T')
                  opStr += "}";
              } 

              // pickup the companyCode (in case it has been changed)            

              while(data[x] != '\000' && data[x] == '\001')
                ++x;
              if(data[x] == '\001')
                ++x;
            }  
          }
        }
        else // separator == '\000';
        {
          // change: abc\0def\0... format into: CompanyCode='abc',Name='def',...

          String fieldNames = getFieldNamesStockx();
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
              thisEntry = "";
              while(data[y] != '\000')
              {
                if(data[y] == '\'')
                  thisEntry += "''"; 
                else
                if(data[y] == '"')
                  thisEntry += "''''"; 
                else thisEntry += (char)data[y];
                ++y;
              }
              ++y;
              
              opStr += thisEntry;
              opStr += "'";

              if(thisFieldType == 'D' || thisFieldType == 'T')
                opStr += "}";
            }
            
            // pickup the companyCode (in case it has been changed)          
          
            while(x < len && fieldNames.charAt(x) == ' ')
              ++x;
          }
        }  

        q = "UPDATE stockx SET " + opStr + " WHERE ItemCode = '" + generalUtils.stringFromBytes(itemCode, 0L) + "' AND Store = '" + originalStoreCode + "'";
      }

      stmt.executeUpdate(q);

      if(stmt != null) stmt.close();
    }
    catch(Exception e) { System.out.println(e); return false; }
    
    return true;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean stockxGetRecToHTML(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String unm, String sid,
                                    String uty, String men, String den, String dnm, String bnm, char cad, char dispOrEdit, byte[] itemCode,
                                    byte[] store, String localDefnsDir, String defnsDir, String errStr, byte[] dataAlready, int[] bytesOut)
                                    throws Exception
  {
    byte[] data = new byte[5000];

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

    boolean rtn=false;

    if(itemCode[0] != '\000') // just-in-case
    {
      short[] fieldSizesStockx = new short[20]; // plenty
      getFieldSizesStockx(fieldSizes);

      numFields = screenLayout.layoutToBuffer(buf1, buf2, size1, fieldNames, fieldTypes, fieldSizes, localDefnsDir, defnsDir, "201b.htm", 1, getFieldNamesStockx(), fieldSizesStockx, null, null, null, null);

      byte[] ddlData    = new byte[1000];
      int[] ddlDataLen  = new int[1]; ddlDataLen[0] = 1000;
      int[] ddlDataUpto = new int[1]; ddlDataUpto[0] = 0;

      ddlData = documentUtils.getStoreDDLData(con, stmt, rs, "stockx.store", ddlData, ddlDataUpto, ddlDataLen);

      if(dataAlready[0] != '\000') // coming with an err msg - passing-in data already entered but 
      {                            // the data's fields may (will) not be in the correct order, so sort them
        sortFieldData("stockx.", dataAlready, data);
      }
      else
      {
        if(! getStockxRecGivenCodes(con, stmt, rs, itemCode, store, '\001', dnm, data, localDefnsDir, defnsDir)) // no record for item/store
        {
          screenLayout.buildNewDetails(fieldNames, numFields, "stockx", data);
          generalUtils.repAlphaGivenSeparator('\001', data, 5000, (short)0, itemCode);
          generalUtils.repAlphaGivenSeparator('\001', data, 5000, (short)1, store);
          generalUtils.repAlphaGivenSeparator('\001', data, 5000, (short)2, "");
          generalUtils.repAlphaGivenSeparator('\001', data, 5000, (short)3, "0");
          generalUtils.repAlphaGivenSeparator('\001', data, 5000, (short)4, "0");
          generalUtils.repAlphaGivenSeparator('\001', data, 5000, (short)5, "0");
          generalUtils.repAlphaGivenSeparator('\001', data, 5000, (short)6, "0");
          generalUtils.repAlphaGivenSeparator('\001', data, 5000, (short)7, "0");
          generalUtils.repAlphaGivenSeparator('\001', data, 5000, (short)8, "");
          generalUtils.repAlphaGivenSeparator('\001', data, 5000, (short)9, "0");
        }

        byte[] b = new byte[20];

        // convert dates
        generalUtils.dfsGivenSeparator(true, '\001', data, (short)12, b); // origin-0
        generalUtils.convertFromYYYYMMDD(b); 
        generalUtils.repAlphaGivenSeparator('\001', data, 5000, (short)12, b);

        char dpOnQuantities = miscDefinitions.dpOnQuantities(con, stmt, rs, "5");
        generalUtils.bytesDPsGivenSeparator(true, '\001', dpOnQuantities, data, 5000, 3);
        generalUtils.bytesDPsGivenSeparator(true, '\001', dpOnQuantities, data, 5000, 4);
        generalUtils.bytesDPsGivenSeparator(true, '\001', dpOnQuantities, data, 5000, 5);
        generalUtils.bytesDPsGivenSeparator(true, '\001', dpOnQuantities, data, 5000, 6);
        generalUtils.bytesDPsGivenSeparator(true, '\001', dpOnQuantities, data, 5000, 7);
        generalUtils.bytesDPsGivenSeparator(true, '\001', dpOnQuantities, data, 5000, 9);

        generalUtils.catAsBytes("stockx.itemcode=" + generalUtils.stringFromBytes(itemCode, 0L) + "\001", 0, data, false);

        String[] mfr     = new String[1];
        String[] mfrCode = new String[1];
        getMfrAndMfrCodeGivenItemCode(con, stmt, rs, generalUtils.stringFromBytes(itemCode, 0L), mfr, mfrCode);
        generalUtils.catAsBytes("stockx.manufacturer="     + mfr[0]     + "\001", 0, data, false);
        generalUtils.catAsBytes("stockx.manufacturerCode=" + mfrCode[0] + "\001", 0, data, false);
      }

      byte[] prependCode = new byte[20000];
      prependEditStockx(con, stmt, rs, req, errStr, unm, sid, uty, men, den, dnm, bnm, cad, generalUtils.stringFromBytes(itemCode, 0L), 
                        generalUtils.stringFromBytes(store, 0L), localDefnsDir, defnsDir, prependCode, bytesOut);

      screenLayout.bufferToBuffer(buf1, buf2, size1, size2, source, numFields, fieldNames, fieldTypes, fieldSizes, dispOrEdit, 'H', data, 5000, ddlData,
                           ddlDataUpto[0], null, prependCode);

      screenLayout.bufferToOut(buf1, buf2, source, out);

      rtn = true;
    }

    return rtn;
  }

  // ------------------------------------------------------------------------------------------------------------------------------------------------------
  private void prependEditStockx(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, String errStr, String unm, String sid, String uty, String men,
                                   String den, String dnm, String bnm, char cad, String code, String store, String localDefnsDir,
                                   String defnsDir, byte[] b, int[] bytesOut) throws Exception
  {
    String code2 = generalUtils.sanitise(code);
    scoutln(b, bytesOut, "<html><head><title>" + code + "</title>");
    
    scoutln(b, bytesOut, "<SCRIPT LANGUAGE=\"JavaScript\">\n");
    scoutln(b, bytesOut, "function save(option){");
    scoutln(b, bytesOut, "document.forms[0].p4.value=option;");
    scoutln(b, bytesOut, "document.go.submit();}\n");
    scoutln(b, bytesOut, "</script>\n");

    String cssDirectory = directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir);
    String cssDocumentDirectory = directoryUtils.getCssDocumentDirectory(con, stmt, rs, dnm);
    scoutln(b, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + cssDirectory + "general.css\">\n");
    scoutln(b, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + cssDocumentDirectory + "document.css\">\n");

    scoutln(b, bytesOut, "<form name=go action=\"ProductStockxUpdate\" enctype=\"application/x-www-form-urlencoded\" method=POST>");

    scoutln(b, bytesOut, "<input type=hidden name=unm value=" + unm + ">");
    scoutln(b, bytesOut, "<input type=hidden name=sid value=" + sid + ">");
    scoutln(b, bytesOut, "<input type=hidden name=uty value=" + uty + ">");
    scoutln(b, bytesOut, "<input type=hidden name=men value=\"" + men + "\">");
    scoutln(b, bytesOut, "<input type=hidden name=den value=" + den + ">");
    scoutln(b, bytesOut, "<input type=hidden name=dnm value=" + dnm + ">");
    scoutln(b, bytesOut, "<input type=hidden name=bnm value=" + bnm + ">");

    scoutln(b, bytesOut, "<input type=hidden name=p1 value=" + cad + ">");
    scoutln(b, bytesOut, "<input type=hidden name=p2 value=" + code2 + ">");
    scoutln(b, bytesOut, "<input type=hidden name=p3 value=" + store + ">");
    scoutln(b, bytesOut, "<input type=hidden name=p4 value=''>");

    outputPageFrame(con, stmt, rs, b, req, cad, "", "ProductStockxUpdate", code, unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, bytesOut);

    if(errStr.length() > 0 && ! errStr.equalsIgnoreCase("null"))
      generalUtils.catAsBytes(errStr, 0, b, false);
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // sets : stock.code=acme\001stock.name=acme ltd\001...
  // returns: true or false if rec not found
  public boolean getStockxRecGivenCodes(Connection con, Statement stmt, ResultSet rs, String itemCode, String store, char separator, String dnm, byte[] data, String localDefnsDir, String defnsDir) throws Exception
  {
    byte[] itemCodeB = new byte[21];
    byte[] storeB    = new byte[50];
    generalUtils.strToBytes(itemCodeB, itemCode);
    generalUtils.strToBytes(storeB,    store);
    
    return getStockxRecGivenCodes(con, stmt, rs, itemCodeB, storeB, separator, dnm, data, localDefnsDir, defnsDir);
  }
  public boolean getStockxRecGivenCodes(Connection con, Statement stmt, ResultSet rs, byte[] itemCode, byte[] store, char separator, String dnm, byte[] data, String localDefnsDir, String defnsDir) throws Exception
  {
    if(itemCode[0] == '\000') // just-in-case
      return false;

    generalUtils.toUpper(itemCode, 0);

    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT * FROM stockx WHERE ItemCode = '" + generalUtils.sanitiseForSQL(generalUtils.stringFromBytes(itemCode, 0L)) + "' AND Store = '" + generalUtils.stringFromBytes(store, 0L) + "'");
    if(! rs.next())
    {
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
      return false;
    }      
    
    ResultSetMetaData rsmd = rs.getMetaData();

    String fieldTypes = getFieldTypesStockx();
     
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
      String fieldNames = getFieldNamesStockx();
      String thisFieldName;
      char thisFieldType;
      boolean terminate = true;
    
      int x=0, count=0, len = fieldNames.length();
      while(x < len)
      {
        thisFieldName="";
        while(x < len && fieldNames.charAt(x) != ',')
          thisFieldName += fieldNames.charAt(x++);
        thisFieldType = fieldTypes.charAt(count++);
        generalUtils.catAsBytes("stockx." + thisFieldName + "=" + getValue(count, thisFieldType, rs, rsmd) + "\001", 0, data, terminate);
      
        ++x;
        while(x < len && fieldNames.charAt(x) == ' ')
          ++x;
        terminate = false;
      }
    }  
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return true;   
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean stockxDeleteRec(Connection con, Statement stmt, ResultSet rs, byte[] itemCode, byte[] recData, int recDataLen, String dnm, String localDefnsDir, String defnsDir)
                                 throws Exception
  {
    try
    {
      if(itemCode[0] == '\000') // just-in-case
        return false;

      generalUtils.toUpper(itemCode, 0);

      byte[] b         = new byte[20];
      byte[] storeCode = new byte[21];
      generalUtils.catAsBytes("Store", 0, b, true);

      if(searchDataString(recData, recDataLen, "stockx", b, storeCode) == -1)
        return false;

      stmt = con.createStatement();
    
      stmt.executeUpdate("DELETE FROM stockx WHERE ItemCode = '" + generalUtils.sanitiseForSQL(generalUtils.stringFromBytes(itemCode, 0L)) + "' AND Store = '" 
                         + generalUtils.stringFromBytes(storeCode, 0L) + "'");
   
      if(stmt != null) stmt.close();
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
  public boolean stockDeleteRec(Connection con, Statement stmt, ResultSet rs, String itemCode, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    if(itemCode.length() == 0) // just-in-case
      return false;

    try
    {
      itemCode = itemCode.toUpperCase();

      stmt = con.createStatement();
    
      stmt.executeUpdate("DELETE FROM stock WHERE ItemCode = '" + generalUtils.sanitiseForSQL(itemCode) + "'");
   
      if(stmt != null) stmt.close();
      stmt = con.createStatement();

      stmt.executeUpdate("DELETE FROM stockx WHERE ItemCode = '" + generalUtils.sanitiseForSQL(itemCode) + "'");      
      
      if(stmt != null) stmt.close();
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
  private void outputPageFrame(Connection con, Statement stmt, ResultSet rs, byte[] b, HttpServletRequest req, char cad, String bodyStr, String callingServlet, String code, String unm, String sid, String uty, String men, String den, String dnm,
                               String bnm, String otherSetup, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    int[] hmenuCount = new int[1];

    String subMenuText = dashboardUtils.buildSubMenuText(con, stmt, rs, req, "3001", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount);

    subMenuText += "<table id='title' width=100%><tr><td colspan=2 nowrap><p>Stock Record" + directoryUtils.buildHelp(3001) + "</td></tr></table>";

    subMenuText += buildSubMenuText(con, stmt, rs, req, cad, code, callingServlet, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount);

    scoutln(b, bytesOut, authenticationUtils.buildScreen(con, stmt, rs, req, true, unm, sid, uty, men, den, dnm, bnm, callingServlet, subMenuText, hmenuCount[0], bodyStr, "", localDefnsDir, defnsDir));

    scoutln(b, bytesOut, "<table border=0><tr><td valign=top><div id='second' style=\"{margin-top:0px;}\"></div></td>");
    scoutln(b, bytesOut, "<td valign=top><div id='third' style=\"{margin-top:0px;margin-left:0px;}\"></div></td></tr></table>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String buildSubMenuText(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, char cad, String code, String callingServlet, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                                  String localDefnsDir, String defnsDir, int[] hmenuCount) throws Exception
  {
    String s = "<div id='submenu'>";

    ++hmenuCount[0];

    if(callingServlet.equals("ProductStockRecord"))      
      s += drawOptions3001(con, stmt, rs, req, hmenuCount, code, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir);
    else
    if(callingServlet.equals("ProductStockUpdate"))      
      s += drawOptions3005(hmenuCount, cad);
    else
    if(callingServlet.equals("ProductStockxUpdate"))      
      s += drawOptions3007(hmenuCount, cad);

    s += "</div>";
    
    --hmenuCount[0];
    
    return s;
  }

  // ------------------------------------------------------------------------------------------------------------------------------------------------
  private String drawOptions3001(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, int[] hmenuCount, String code, String unm, String sid,
                                 String uty,
                               String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir)
                               throws Exception
  {
    String s = "";
    
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 1001, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\">";
      s += "<a href=\"/central/servlet/MainPageUtils1a?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men="
                           + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + code + "\">Enquiry</a></dt></dl>";
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 1002, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\">";
      s += "<a href=\"/central/servlet/MainPageUtils2?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men="
                           + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + code + "\">History</a></dt></dl>";
    }
    
    String[] mfr  = new String[1];
    String[] page = new String[1];
            
    if(getCatalogPageAndMfrGivenCode(con, stmt, rs, code, dnm, localDefnsDir, defnsDir, mfr, page))
    {
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\">";
      s += "<a href=\"/central/servlet/CatalogPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=" + page[0] + "&p1=" + generalUtils.sanitise(mfr[0]) + "\">Catalog</a></dt></dl>";
    }
        
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3052, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\">";
      s += "<a href=\"/central/servlet/ProductStockTraceInput?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + code + "\">Trace</a></dt></dl>";
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3003, unm, uty, dnm, localDefnsDir, defnsDir))
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\"><a href=\"javascript:fetch()\">Edit Details</a></dt></dl>";

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3007, unm, uty, dnm, localDefnsDir, defnsDir))
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\"><a href=\"javascript:add()\">Add New Store</a></dt></dl>";

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 11800, unm, uty, dnm, localDefnsDir, defnsDir))
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\"><a href=\"javascript:trail()\">Trail</a></dt></dl>\n";

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3069, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\">";
      s += "<a href=\"/central/servlet/StockLevelValuesItem?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + code + "\">Openings</a></dt></dl>";
    }
    
    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private String drawOptions3005(int[] hmenuCount, char cad) throws Exception
  {      
    String s = "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\"><a href=\"javascript:save(' ')\">Save Changes</a></dt></dl>";

    if(cad != 'C')
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\"><a href=\"javascript:save('X')\">Cancel Changes</a></dt></dl>";
    
    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private String drawOptions3007(int[] hmenuCount, char cad) throws Exception
  {      
    String s = "";
    
    if(cad == 'C')
    {
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\"><a href=\"javascript:save('S')\">Save New Store</a></dt></dl>";

      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\"><a href=\"javascript:save('X')\">Cancel Changes</a></dt></dl>";
    }
    else 
    {
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\">";
      s += "<a href=\"javascript:save('S')\">Save Changes</a></dt></dl>";

      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\">";
      s += "<a href=\"javascript:save('X')\">Cancel Changes</a></dt></dl>";

      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\">";
      s += "<a href=\"javascript:save('D')\">Delete Store</a></dt></dl>";
    }
    
    return s;
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public void updateStockxField(Connection con, Statement stmt, ResultSet rs, byte[] itemCode, byte[] storeCode, String fieldName, String fieldValue, String unm, String dnm,
                                  String localDefnsDir, String defnsDir) throws Exception
  {
    stmt = con.createStatement();
    stmt.executeUpdate("UPDATE stockx SET " + fieldName + " = '" + fieldValue + "', SignOn='" + unm
                     + "', DateLastModified = NULL WHERE ItemCode = '" + generalUtils.sanitiseForSQL(generalUtils.stringFromBytes(itemCode, 0L)) + "' AND Store = '"
                     + generalUtils.stringFromBytes(storeCode, 0L) + "'");
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(byte[] b, int bytesOut[], String str) throws Exception
  {      
    generalUtils.catAsBytes(str, 0, b, false);
    bytesOut[0] += (str.length() + 2);    
  }

  //--------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean existsItemRecGivenCode(Connection con, Statement stmt, ResultSet rs, String code) throws Exception
  {
    if(code.length() == 0) // just-in-case
      return false;

    code = code.toUpperCase();

    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM stock WHERE ItemCode = '" + generalUtils.sanitiseForSQL(code) + "'");
    int numRecs = 0;
    if(rs.next())
      numRecs = rs.getInt("rowcount");
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    if(numRecs == 1)
      return true;
    
    return false;
  }

  //------------------ -------------------------------------------------------------------------------------------------------------------------------
  public boolean existsItemRecGivenMfrAndMfrCode(Connection con, Statement stmt, ResultSet rs, String mfr, String mfrCode) throws Exception
  {
    if(mfr.length() == 0 && mfrCode.length() == 0) // just-in-case
      return false;

    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM stock WHERE Manufacturer = '" + generalUtils.sanitiseForSQL(mfr)
                                   + "' AND ManufacturerCode = '" + generalUtils.sanitiseForSQL(mfrCode) + "'");
    rs.next();      
    int numRecs = rs.getInt("rowcount") ;
    if(rs != null) rs.close();
    if(stmt != null) stmt.close();

    if(numRecs == 1)
      return true;
    
    return false;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void mapCodes(Connection con, Statement stmt, ResultSet rs, String itemCodeIn, String mfrIn, String mfrCodeIn, String[] itemCodeOut, String[] mfrOut, String[] mfrCodeOut) throws Exception
  {
    if(itemCodeIn.length() > 0)
    {
      getMfrAndMfrCodeGivenItemCode(con, stmt, rs, itemCodeIn, mfrOut, mfrCodeOut);
      itemCodeOut[0] = itemCodeIn;
    }
    else
    if(mfrIn.length() > 0 && mfrCodeIn.length() > 0)
    {
      itemCodeOut[0] = getItemCodeGivenMfrAndMfrCode(con, stmt, rs, mfrIn, mfrCodeIn);
      if(itemCodeOut[0].length() == 0)
        itemCodeOut[0] = "-";
      mfrOut[0]     = mfrIn;
      mfrCodeOut[0] = mfrCodeIn;      
    }
    else
    {
      itemCodeOut[0] = "-";
      mfrOut[0]     = mfrIn;
      mfrCodeOut[0] = mfrCodeIn;
    }
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getItemCodeGivenMfrAndMfrCode(Connection con, Statement stmt, ResultSet rs, String mfr, String mfrCode) throws Exception
  {
    if(mfr.length() == 0 || mfrCode.length() == 0) // just-in-case
      return "";

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT ItemCode FROM stock WHERE Manufacturer = '" + generalUtils.sanitiseForSQL(mfr) + "' AND ManufacturerCode = '"
                                     + generalUtils.sanitiseForSQL(mfrCode) + "'");
    
    String s = "";
    
    if(rs.next())
      s = rs.getString(1);
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return s;
  }
      
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void getMfrAndMfrCodeGivenItemCode(Connection con, Statement stmt, ResultSet rs, String itemCode, String[] mfr, String[] mfrCode) throws Exception
  {
    mfr[0] = mfrCode[0] = "";

    if(itemCode.length() == 0) // just-in-case
      return;

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT Manufacturer, ManufacturerCode FROM stock WHERE ItemCode = '" + generalUtils.sanitiseForSQL(itemCode) + "'");
    
    if(rs.next())
    {
      mfr[0]     = rs.getString(1);
      mfrCode[0] = rs.getString(2);
    }
    
    if(mfr[0]     == null) mfr[0]     = "";
    if(mfrCode[0] == null) mfrCode[0] = "";

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }
      
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public byte[] getMfrsDDLData(Connection con, Statement stmt, ResultSet rs, String fieldName, byte[] ddlData, int[] ddlDataUpto, int[] ddlDataLen)
                               throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT DISTINCT Manufacturer FROM stock ORDER BY Manufacturer");

    String mfr;
    
    ddlDataUpto[0] = generalUtils.stringIntoBytes(fieldName + "=\"<none>\" ", 0, ddlData, ddlDataUpto[0]);

    while(rs.next())
    {
      mfr = rs.getString(1);
      if(mfr.length() > 0)
      {
        if((ddlDataUpto[0] + 100) >= ddlDataLen[0])
       {
          byte[] tmp = new byte[ddlDataLen[0]];
          System.arraycopy(ddlData, 0, tmp, 0, ddlDataLen[0]);
          ddlDataLen[0] += 1000;
          ddlData = new byte[ddlDataLen[0]];
          System.arraycopy(tmp, 0, ddlData, 0, ddlDataLen[0] - 1000);
        }

        ddlDataUpto[0] = generalUtils.stringIntoBytes(fieldName + "=\"" + mfr + "\" ", 0, ddlData, ddlDataUpto[0]);
      }
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return ddlData;
  }

  //--------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean getCatalogPageAndMfrGivenCode(Connection con, Statement stmt, ResultSet rs, String itemCode, String dnm, String localDefnsDir, String defnsDir, String[] mfr, String[] page) throws Exception
  {
    byte[] data = new byte[5000];
    
    if(getStockRecGivenCode(con, stmt, rs, itemCode, '\000', data) < 0) // just-in-case
      return false;
    
    mfr[0]  = generalUtils.dfsAsStr(data, (short)3);
    String categoryCode = generalUtils.dfsAsStr(data, (short)55);
    
    page[0] = getPageGivenCategoryCode(con, stmt, rs, categoryCode, dnm, localDefnsDir, defnsDir);
    
    if(page[0].length() > 0)
      return true;
    
    return false;
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private String getPageGivenCategoryCode(Connection con, Statement stmt, ResultSet rs, String categoryCode, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT Page FROM stockcat WHERE CategoryCode = '" + categoryCode + "'");

    String page="";
    
    while(rs.next())
    {
      page = rs.getString(1);
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return page;
  }

  //--------------------------------------------------------------------------------------------------------------------------------------------------
  public String getAStockFieldGivenCode(Connection con, Statement stmt, ResultSet rs, String fieldName, String itemCode) throws Exception
  {
    if(itemCode.length() == 0) // just-in-case
      return "";

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT " + fieldName + " FROM stock WHERE ItemCode = '" + generalUtils.sanitiseForSQL(itemCode) + "'");
    
    String s = "";
    
    if(rs.next())
      s = rs.getString(1);
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return s;
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void getWACDetailsGivenCode(Connection con, Statement stmt, ResultSet rs, String itemCode, double[] closingLevel, double[] closingWAC, String date) throws Exception
  {
    closingLevel[0] = closingWAC[0] = 0.0;

    if(itemCode.length() == 0) // just-in-case
      return;

    // check that there is a record that exists after the required date

    int rowCount = 0;
    
    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM stockopen WHERE ItemCode = '" + generalUtils.sanitiseForSQL(itemCode) + "' AND Date >= {d '" + date + "'}");

      if(rs.next()) // pickup the first one
        rowCount = rs.getInt("rowcount");

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
    catch(Exception e) { System.out.println(e); }
    {
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }

    if(rowCount == 0)
      return;

    // there is at least one record, so fetch the latest

    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT Level, Cost FROM stockopen WHERE ItemCode = '" + generalUtils.sanitiseForSQL(itemCode) + "' AND Date <= {d '" + date + "'} ORDER BY Date DESC");

      if(rs.next()) // pickup the first one
      {
        closingLevel[0] = generalUtils.doubleFromStr(rs.getString(1));
        closingWAC[0]   = generalUtils.doubleFromStr(rs.getString(2));
      }

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
    catch(Exception e) { System.out.println(e); }
    {
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
  }


 
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public double getWAC(Connection con, Statement stmt, ResultSet rs, String itemCode, String dateFrom, String dateTo, String dnm) throws Exception
  {
    double[] numItemsPurchased = new double[1];
    return getWAC(con, stmt, rs, itemCode, dateFrom, dateTo, dnm, numItemsPurchased);
  }
  public double getWAC(Connection con, Statement stmt, ResultSet rs, String itemCode, String dateFrom, String dateTo, String dnm, double[] numItemsPurchased) throws Exception
  {
    byte[] list = new byte[1000];
    int[] listLen = new int[1];  listLen[0] = 1000;

    double[] openingLevel = new double[1];
    double[] openingWAC   = new double[1];
    getWACDetailsGivenCode(con, stmt, rs, itemCode, openingLevel, openingWAC, dateFrom);

    String yearEndDate = accountsUtils.getAccountingYearEndDateForADate(con, stmt, rs, dateFrom, dnm);

    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT t2.Quantity, t2.Amount, t1.Date, t1.InvoiceCode, t1.Currency, t2.UnitPrice, t1.Rate FROM pinvoicel AS t2 INNER JOIN pinvoice AS t1 ON t1.InvoiceCode = t2.InvoiceCode WHERE t2.ItemCode = '" + generalUtils.sanitise(itemCode)
                         + "' AND t1.Status != 'C' AND t2.PODate != {d '1970-01-01'} AND t2.PODate < {d '" + dateFrom + "'}  " +
                         "    AND t1.Date >= {d '" + dateFrom + "'}  " +
                         "    AND t1.Date <= {d '" + dateTo + "'} AND t1.Date <= {d '" + yearEndDate + "'}");

      while(rs.next())
        list = addToTmp("PI", generalUtils.doubleFromStr(rs.getString(1)), generalUtils.doubleFromStr(rs.getString(2)), rs.getString(3), rs.getString(4), list, listLen);

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();

      stmt = con.createStatement();
                                                           
    rs = stmt.executeQuery("SELECT t2.Quantity, t2.Amount, t2.PODate, t1.InvoiceCode, t1.Currency, t2.UnitPrice, t1.Rate FROM pinvoicel AS t2 INNER JOIN pinvoice AS t1 ON t1.InvoiceCode = t2.InvoiceCode WHERE t2.ItemCode = '" + generalUtils.sanitise(itemCode)
                         + "' AND t1.Status != 'C' AND t2.PODate != {d '1970-01-01'} AND t2.PODate < {d '" + dateFrom + "'} AND t1.Date > {d '" + dateTo + "'} AND t1.Date <= {d '" + yearEndDate + "'}");

    while(rs.next())
      list = addToTmp("PI", generalUtils.doubleFromStr(rs.getString(1)), generalUtils.doubleFromStr(rs.getString(2)), rs.getString(3), rs.getString(4), list, listLen);

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.Quantity, t2.Amount, t2.PODate, t1.InvoiceCode FROM pinvoicel AS t2 INNER JOIN pinvoice AS t1 ON t1.InvoiceCode = t2.InvoiceCode WHERE t2.ItemCode = '" + generalUtils.sanitise(itemCode)
                         + "' AND t1.Status != 'C' AND t2.PODate != {d '1970-01-01'} AND t2.PODate >= {d '" + dateFrom + "'} AND t1.Date > {d '" + dateTo + "'} AND t1.Date <= {d '" + yearEndDate + "'}");

    while(rs.next())
      list = addToTmp("PI", generalUtils.doubleFromStr(rs.getString(1)), generalUtils.doubleFromStr(rs.getString(2)), rs.getString(3), rs.getString(4), list, listLen);

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.Quantity, t2.Amount, t1.Date, t1.InvoiceCode FROM pinvoicel AS t2 INNER JOIN pinvoice AS t1 ON t1.InvoiceCode = t2.InvoiceCode WHERE t2.ItemCode = '" + generalUtils.sanitise(itemCode)
                         + "' AND t1.Status != 'C' AND t2.PODate != {d '1970-01-01'} AND t2.PODate >= {d '" + dateFrom + "'} AND t1.Date <= {d '" + dateTo + "'} AND t1.Date <= {d '" + yearEndDate + "'}");

    while(rs.next())
      list = addToTmp("PI", generalUtils.doubleFromStr(rs.getString(1)), generalUtils.doubleFromStr(rs.getString(2)), rs.getString(3), rs.getString(4), list, listLen);

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.Quantity, t2.Amount, t1.Date, t1.InvoiceCode FROM pinvoicel AS t2 INNER JOIN pinvoice AS t1 ON t1.InvoiceCode = t2.InvoiceCode WHERE t2.ItemCode = '" + generalUtils.sanitise(itemCode)
                         + "' AND t1.Status != 'C' AND t2.PODate = {d '1970-01-01'} AND t1.Date >= {d '" + dateFrom + "'} AND t1.Date <= {d '" + dateTo + "'}");

    while(rs.next())
      list = addToTmp("PI", generalUtils.doubleFromStr(rs.getString(1)), generalUtils.doubleFromStr(rs.getString(2)), rs.getString(3), rs.getString(4), list, listLen);

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();


    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.Quantity, t2.Amount, t1.Date, t1.PCNCode FROM pcreditl AS t2 INNER JOIN pcredit AS t1 ON t1.PCNCode = t2.PCNCode WHERE t2.ItemCode = '" + generalUtils.sanitise(itemCode) + "' AND t1.Status != 'C' AND t1.Date >= {d '" + dateFrom
                         + "'} AND t1.Date <= {d '" + dateTo + "'}");

    while(rs.next())
      list = addToTmp("PC", generalUtils.doubleFromStr(rs.getString(1)), generalUtils.doubleFromStr(rs.getString(2)), rs.getString(3), rs.getString(4), list, listLen);

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.Quantity, t2.Amount, t1.Date, t1.InvoiceCode FROM invoicel AS t2 INNER JOIN invoice AS t1 ON t1.InvoiceCode = t2.InvoiceCode WHERE t2.ItemCode = '" + generalUtils.sanitise(itemCode) + "' AND t1.Status != 'C' AND t1.Date >= {d '"
                         + dateFrom + "'} AND t1.Date <               {d '" + dateTo + "'}");

    while(rs.next())
      list = addToTmp("SI", generalUtils.doubleFromStr(rs.getString(1)), generalUtils.doubleFromStr(rs.getString(2)), rs.getString(3), rs.getString(4), list, listLen);

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.Quantity, t2.Amount, t1.Date, t2.InvoiceCode FROM creditl AS t2 INNER JOIN credit AS t1 ON t1.CNCode = t2.CNCode WHERE t2.ItemCode = '" + generalUtils.sanitise(itemCode) + "' AND t1.Status != 'C' AND t1.Date >= {d '" + dateFrom
                         + "'} AND t1.Date <                  {d '" + dateTo + "'} AND t2.CostOfSaleAdjustment = 'Y'"); // note: if SCN is for a salesprice discount then it can be ignored

    while(rs.next())
      list = addToTmp("SC", generalUtils.doubleFromStr(rs.getString(1)), generalUtils.doubleFromStr(rs.getString(2)), rs.getString(3), rs.getString(4), list, listLen);

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    }
    catch(Exception e)
    {
      System.out.println("getWAC: " + e);
      return 0;
    }


    String docType, qtyStr, amtStr, dateAndType, docCode;
    double qty, amt;
    double currentLevel = openingLevel[0];
    double currentWAC   = openingWAC[0];
    double currentValue = currentWAC * currentLevel;

    byte[] entry = new byte[1000]; // plenty
    int x, y, len, entryNum = 0;

    while(generalUtils.getListEntryByNum(entryNum++, list, entry))
    {
      x = 0;
      len = generalUtils.lengthBytes(entry, 0);
      if(len > 0) //  call seems to return empty entry... prob in getListEntryByNum??? FIXME
      {
        dateAndType ="";
        while(x < len && entry[x] != '\002') // just-in-case
          dateAndType += (char)entry[x++];

        if(dateAndType.length() > 0)
        {
        y = dateAndType.length();
        docType = "" + dateAndType.charAt(y - 2) + dateAndType.charAt(y - 1);
        ++x;
        qtyStr = "";
        while(x < len && entry[x] != '\002') // just-in-case
          qtyStr += (char)entry[x++];
        ++x;
        amtStr = "";
        while(x < len && entry[x] != '\002') // just-in-case
          amtStr += (char)entry[x++];
        ++x;
        docCode = "";
        while(x < len && entry[x] != '\002') // just-in-case
          docCode += (char)entry[x++];

        qty = generalUtils.doubleDPs(generalUtils.doubleFromStr(qtyStr), '2');
        amt = generalUtils.doubleDPs(generalUtils.doubleFromStr(amtStr), '2');

        if(docType.equals("PI"))
        {
          currentLevel += qty;
          currentValue += amt;
          if(currentLevel != 0)
            currentWAC = (currentValue / currentLevel);
        }
        else
        if(docType.equals("PC"))
        {
          currentLevel -= qty; // need to know if it's a discount or goods returned... assumed goods returned  FIXME
          currentValue -= amt;
          if(currentLevel != 0)
            currentWAC = (currentValue / currentLevel);
        }
        else
        if(docType.equals("SI"))
        {
          currentLevel -= qty;
          currentValue -= (currentWAC * qty);

          // update the WAC used on the SI back into the results list (in case it should be needed by a future SCN)
          list = repInTmp((entryNum - 1), qty, currentWAC, dateAndType, docCode, list, listLen);
        }
        else
        if(docType.equals("SC"))
        {
          currentLevel += qty;
          currentValue += (wacAtInvoiceTime(docCode, list) * qty); // for SCN the docCode is the invoiceCode from the CN line
          if(currentLevel != 0)
            currentWAC = (currentValue / currentLevel);
        }
        }
      }
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    numItemsPurchased[0] = currentLevel;

    try
    {
      currentWAC = generalUtils.doubleDPs(currentWAC, '2');
    }
    catch(Exception e)
    {
      currentWAC = 0;
      System.out.println(itemCode + ": " + e);
    }
    
    return currentWAC;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private byte[] addToTmp(String docType, double qty, double amt, String date, String docCode, byte[] list, int[] listLen) throws Exception
  {
    String s = date + "-" + docType + "\002" + qty + "\002" + amt + "\002" + docCode + "\002\001";

    byte[] newItem = new byte[s.length() + 1];
    generalUtils.strToBytes(newItem, s);

    list = generalUtils.addToList(true, newItem, list, listLen);

    return list;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private byte[] repInTmp(int entryNum, double qty, double amt, String dateAndType, String docCode, byte[] list, int[] listLen) throws Exception
  {
    String s = dateAndType + "\002" + qty + "\002" + amt + "\002" + docCode + "\002\001";

    byte[] newEntry = new byte[s.length() + 1];
    generalUtils.strToBytes(newEntry, s);

    list = generalUtils.repListEntryByNum(entryNum, newEntry, list, listLen);

    return list;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private double wacAtInvoiceTime(String invoiceCode, byte[] list) throws Exception
  {
    String dateAndType, docType, amtStr, docCode;
    byte[] entry = new byte[1001]; // plenty
    int x, y, len, entryNum = 0;
    while(generalUtils.getListEntryByNum(entryNum++, list, entry))
    {
      x = 0;
      len = generalUtils.lengthBytes(entry, 0);

      dateAndType ="";
      while(x < len && entry[x] != '\002') // just-in-case
        dateAndType += (char)entry[x++];
      y = dateAndType.length();
      docType = "" + dateAndType.charAt(y - 2) + dateAndType.charAt(y - 1);

      if(docType.equals("SI")) // in case we match against the CN entry
      {
        ++x;
        while(x < len && entry[x] != '\002') // just-in-case
          ++x;
        ++x;
        amtStr = "";
        while(x < len && entry[x] != '\002') // just-in-case
         amtStr += (char)entry[x++];
        ++x;
        docCode = "";
        while(x < len && entry[x] != '\002') // just-in-case
          docCode += (char)entry[x++];

        if(docCode.equals(invoiceCode))
          return generalUtils.doubleDPs(generalUtils.doubleFromStr(amtStr), '2');
      }
    }

    return 0.0; // just-in-case
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void getPriceDetailsGivenCode(Connection con, Statement stmt, ResultSet rs, String itemCode, String band, String dnm, String localDefnsDir, String defnsDir, String[] descs, String[] sellPrice, String[] salesCurrency, String[] uom)
                                       throws Exception
  {
    sellPrice[0]     = "0";
    salesCurrency[0] = accountsUtils.getBaseCurrency(con, stmt, rs, dnm, localDefnsDir, defnsDir);
    uom[0]           = "Each";

    if(itemCode.length() == 0) // just-in-case
      return;
    
    stmt = con.createStatement();
    
    String price;
    switch(generalUtils.strToInt(band)) // pickup price dependent upon band
    {
      case  1 : price = "SellPrice1"; break;
      case  2 : price = "SellPrice2"; break;
      case  3 : price = "SellPrice3"; break;
      case  4 : price = "SellPrice4"; break;
      default : price = "RRP";        break;
    }

    rs = stmt.executeQuery("SELECT " + price + ", UoM, SalesCurrency, Description, Description2 FROM stock WHERE ItemCode = '" + generalUtils.sanitiseForSQL(itemCode) + "'");

    if(rs.next())
    {
      sellPrice[0]     = rs.getString(1);
      uom[0]           = rs.getString(2);
      salesCurrency[0] = rs.getString(3);
      descs[0]         = rs.getString(4);
      
      if((descs[0] + " " + rs.getString(5)).length() < 80)
        descs[0] += (" " + rs.getString(5));
    }
  
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getDescriptionGivenCode(Connection con, Statement stmt, ResultSet rs, String itemCode) throws Exception
  {
    if(itemCode.length() == 0) // just-in-case
      return "";
        
    String desc = null;
    
    try
    {    
      stmt = con.createStatement();
      
      rs = stmt.executeQuery("SELECT Description FROM stock WHERE ItemCode = '" + generalUtils.sanitiseForSQL(itemCode) + "'");

      if(rs.next())
        desc = rs.getString(1);
      
      if(desc == null)
        desc = "";      
  
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
    catch(Exception e) { }
    {
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
    
    return desc;
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public void updateStockPurchasePriceAndDate(Connection con, Statement stmt, ResultSet rs, String itemCode, String purchasePrice, String dateChanged) throws Exception
  {
    stmt = con.createStatement();
    stmt.executeUpdate("UPDATE stock SET PurchasePrice = '" + purchasePrice+ "', DateChanged = {d '" + dateChanged + "'} WHERE ItemCode = '"
            + generalUtils.sanitiseForSQL(itemCode) + "'");
    if(stmt != null) stmt.close();
  }

  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getLocationGivenItemCodeAndStore(Connection con, Statement stmt, ResultSet rs, String itemCode, String storeCode) throws Exception
  {
    if(itemCode.length() == 0 || storeCode.length() == 0)
      return "";

    String location = "";
    
    itemCode = itemCode.toUpperCase();

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT Location FROM stockx WHERE ItemCode = '" + generalUtils.sanitiseForSQL(itemCode) + "' AND Store = '" + storeCode + "'");

    if(rs.next())
      location = generalUtils.deNull(rs.getString(1));

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    return location;
  }

}
