// =======================================================================================================================================================================================================
// System: ZaraStar Documents: GRN Record Access
// Module: GoodsReceivedNote.java
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

public class GoodsReceivedNote
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
  DashboardUtils dashboardUtils = new DashboardUtils();
  Inventory inventory = new Inventory();

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getTableCreateStringGR() throws Exception
  {  
    return "gr ( GRCode char(20) not null,      Date date not null,               CompanyCode char(20) not null, "
              + "CompanyName char(60),          POCode char(20),                  SignOn char(20), "
              + "Notes char(250),               Attention char(1),                DateLastModified timestamp, "
              + "unused1 char(20),              StoreCode char(40),               StockProcessed char(1), "
              + "AccountsProcessed char(1),     DateStockProcessed date,          DateAccountsProcessed date, "
              + "SignOnStockProcessed char(20), SignOnAccountsProcessed char(20), Status char(1), "
              + "Closed char(1),                InTransit char(1), "
              + "unique(GRCode))";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStringsGR(String[] s) throws Exception
  {
    s[0] = "grDateInx on gr (Date)";
    s[1] = "grCompanyCodeInx on gr (CompanyCode)";
    s[2] = "grPOCodeInx on gr (POCode)";
    
    return 3;
  }
      
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNamesGR() throws Exception
  {  
    return "GRCode, Date, CompanyCode, CompanyName, POCode, SignOn, Notes, Attention, DateLastModified, unused1, StoreCode, "
         + "StockProcessed, AccountsProcessed, DateStockProcessed, DateAccountsProcessed, SignOnStockProcessed, "
         + "SignOnAccountsProcessed, Status, Closed, InTransit";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypesGR() throws Exception
  {
    return "CDCCCCCCSCCCCDDCCCCC";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizesGR(short[] sizes) throws Exception
  {
    sizes[0]  = 20;  sizes[1]  = 0;   sizes[2]  = 20;  sizes[3]  = 60;  sizes[4] = 20;   sizes[5] = 20;   sizes[6] = 250;
    sizes[7]  = 1;   sizes[8]  = -1;  sizes[9]  = 20;  sizes[10] = 40;  sizes[11] = 1;   sizes[12] = 1;   sizes[13] = 0;
    sizes[14] = 0;   sizes[15] = 20;  sizes[16] = 20;  sizes[17] = 1;   sizes[18] = 1;   sizes[19] = 1;   
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldStylesGR() throws Exception
  {
    return "MMMOOOOOOOOOOOOOOOOO";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getTableCreateStringGRL() throws Exception
  {  
    return "grl ( GRCode char(20) not null,           ItemCode char(20) not null, Description char(80), "
               + "UnitPrice decimal(17,8),            Quantity decimal(19,8),     Amount decimal(17,8), "
               + "DateLastModified timestamp,         SignOn char(20),            POCode char(20), "
               + "ProcessedToPurchaseInvoice char(1), Line integer,               Entry char(6), "
               + "StoreCode char(20),                 SupplierDOCode char(40),    SupplierInvoiceCode char(40), "
               + "POLine integer,                     StockProcessed char(1),     Manufacturer char(30), "
               + "ManufacturerCode char(60),          QCComplete char(1),         QCCompleteDate date, "
               + "Remark char(100),                   Location char(20), "
               + "unique(GRCode, Line))";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStringsGRL(String[] s) throws Exception
  {
    s[0] = "grlItemCodeInx on grl(ItemCode)";
    s[1] = "grlPOCodeInx on grl(POCode)";
    
    return 2;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNamesGRL() throws Exception
  {  
    return "GRCode, ItemCode, Description, UnitPrice, Quantity, Amount, DateLastModified, SignOn, POCode, "
         + "ProcessedToPurchaseInvoice, Line, Entry, StoreCode, SupplierDOCode, SupplierInvoiceCode, POLine, StockProcessed, "
         + "Manufacturer, ManufacturerCode, QCComplete, QCCompleteDate, Remark, Location";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypesGRL() throws Exception
  {
    return "CCCFFFSCCCICCCCICCCCDCC";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizesGRL(short[] sizes) throws Exception
  {
    sizes[0] = 20;    sizes[1] = 20;  sizes[2] = 80;  sizes[3] = 0;    sizes[4] = 0;   sizes[5] = 0;    sizes[6] = -1;
    sizes[7] = 20;    sizes[8] = 20;  sizes[9] = 1;   sizes[10] = 0;   sizes[11] = 6;  sizes[12] = 20;  sizes[13] = 40;
    sizes[14] = 40;   sizes[15] = 0;  sizes[16] = 1;  sizes[17] = 30;  sizes[18] = 60; sizes[19] = 1;   sizes[20] = 0;
    sizes[21] = 100;  sizes[22] = 20;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldStylesGRL() throws Exception
  {
    return "OOOOOOOOOOMOOOOO00O0OOO";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean getRecToHTML(Connection con, Statement stmt, ResultSet rs, PrintWriter out, char dispOrEdit, byte[] code, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir,
                              char cad, String errStr, byte[] dataAlready, HttpServletRequest req, int[] bytesOut) throws Exception
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
    byte[] prependCode = new byte[22000];
    byte[] b           = new byte[300];

    boolean rtn;

    byte[] javaScriptCode = new byte[1000];
    javaScript(con, stmt, rs, req, generalUtils.stringFromBytes(code, 0L), unm, sid, uty, men, den, dnm, bnm, javaScriptCode, localDefnsDir, defnsDir, bytesOut);

    byte[] ddlData = new byte[1500];
    int[] ddlDataLen  = new int[1];

    ddlDataLen[0] = 1500;
    int[] ddlDataUpto = new int[1];
    ddlDataUpto[0] = 0;

    if(cad == 'A' && code[0] == '\000')
    {
      messagePage.msgScreen(false, out, req, 6, unm, sid, uty, men, den, dnm, bnm, "GoodsReceivedNote", imagesDir, localDefnsDir,
                         defnsDir, bytesOut);
      return true;
    }
 
    if(dataAlready[0] != '\000') // coming with an err msg - passing-in data already entered but the data's fields may (will) not be
    {                            // in the correct order
      dispOrEdit = 'E';
      sortFields(dataAlready, headData, "gr");
    }
    else // get header data
    {
      if(cad == 'A')
      {
        if(getRecGivenCode(con, stmt, rs, code, '\001', headData, dnm, localDefnsDir, defnsDir, bytesOut) == -1)
        {
          messagePage.msgScreen(false, out, req, 6, unm, sid, uty, men, den, dnm, bnm, "GoodsReceivedNote", imagesDir, localDefnsDir,
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

    short[] fieldSizesGR = new short[60]; // plenty
    getFieldSizesGR(fieldSizesGR);

    if(cad == 'A') // not a new one
    {
      short[] fieldSizesGRL = new short[40]; // plenty
      getFieldSizesGRL(fieldSizesGRL);

      if(dispOrEdit == 'D')
      {  
        numFields = screenLayout.layoutToBuffer(buf1, buf2, size1, fieldNames, fieldTypes, fieldSizes, localDefnsDir, defnsDir, "208.htm", 2, getFieldNamesGR(), fieldSizesGR, getFieldNamesGRL(), fieldSizesGRL,
                                         null, null);
      }
      else
      {
        numFields = screenLayout.layoutToBuffer(buf1, buf2, size1, fieldNames, fieldTypes, fieldSizes, localDefnsDir, defnsDir, "208a.htm", 1, getFieldNamesGR(), fieldSizesGR, null, null, null, null);
      }

      if(dispOrEdit == 'D')
      {
        prepend(con, stmt, rs, unm, sid, uty, men, den, dnm, bnm, generalUtils.stringFromBytes(code, 0L), date, prependCode, req, localDefnsDir, defnsDir, bytesOut);
      }
      else
      {
        prependEdit(con, stmt, rs, unm, sid, uty, men, den, dnm, bnm, errStr, "A", generalUtils.stringFromBytes(code, 0L), date, "208a.htm", localDefnsDir, defnsDir, prependCode, req, bytesOut);
      }

      screenLayout.bufferToBuffer(buf1, buf2, size1, size2, source, numFields, fieldNames, fieldTypes, fieldSizes, dispOrEdit, 'X', data, 0, ddlData, ddlDataUpto[0], javaScriptCode, prependCode);

      if(dispOrEdit == 'D') // display head *and* lines
      {
        char dpOnQuantities = miscDefinitions.dpOnQuantities(con,stmt, rs, "5");
        char dpOnUnitPrice  = miscDefinitions.dpOnUnitPrice(con, stmt, rs, "4");

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

            generalUtils.bytesDPsGivenSeparator(true, '\001', dpOnUnitPrice,  data, 5000,  4); // unitPrice origin-1
            generalUtils.bytesDPsGivenSeparator(true, '\001', dpOnQuantities, data, 5000,  5); // qty
            generalUtils.bytesDPsGivenSeparator(true, '\001', '2', data, 5000,  6); // amt 

            generalUtils.dfsGivenSeparator(true, '\001', data, (short)11, line); // origin-1

            screenLayout.bufferToBuffer(buf1, buf2, size1, size2, source, numFields, fieldNames, fieldTypes, fieldSizes, 'D', 'L', data, 5000, ddlData, ddlDataUpto[0], javaScriptCallCode, null);
          }
        }
      }
      else // edit
      {
        ddlData = documentUtils.getStoreDDLData(con, stmt, rs, "gr.storecode", ddlData, ddlDataUpto, ddlDataLen);

        // format notes field
        generalUtils.replaceThreesWithNewlines(generalUtils.dfsAsStrGivenBinary1(true, headData, (short)11), b); // origin-0
        generalUtils.repAlphaGivenSeparator('\001', headData, 5000, (short)11, b);
      }

      if(dataAlready[0] == '\000') // NOT coming with an err msg
      {
        // convert date
        generalUtils.dfsGivenSeparator(true, '\001', headData, (short)1, b);
        generalUtils.convertFromYYYYMMDD(b); 
        generalUtils.repAlphaGivenSeparator('\001', headData, 5000, (short)1, b);

        generalUtils.dfsGivenSeparator(true, '\001', headData, (short)13, b); // origin-0
        generalUtils.convertFromYYYYMMDD(b); 
        generalUtils.repAlphaGivenSeparator('\001', headData, 5000, (short)13, b);

        generalUtils.dfsGivenSeparator(true, '\001', headData, (short)14, b); // origin-0
        generalUtils.convertFromYYYYMMDD(b); 
        generalUtils.repAlphaGivenSeparator('\001', headData, 5000, (short)14, b);
      }

      screenLayout.bufferToBuffer(buf1, buf2, size1, size2, source, numFields, fieldNames, fieldTypes, fieldSizes, dispOrEdit, 'H', headData, 5000, ddlData,
                           ddlDataUpto[0], null, null);

      append(con, stmt, rs, buf1, buf2, source, size1, size2, unm, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

      screenLayout.bufferToOut(buf1, buf2, source, out);

      rtn = true;
    }
    else // a new layout
    {
      numFields = screenLayout.layoutToBuffer(buf1, buf2, size1, fieldNames, fieldTypes, fieldSizes, localDefnsDir, defnsDir,
                                       "208a.htm", 1, getFieldNamesGR(), fieldSizesGR, null, null, null, null);

      if(dataAlready[0] != '\000') // coming with an err msg - passing-in data already entered but the data's fields may (will) not
      {                            // be in the correct order
        sortFields(dataAlready, data, "gr");
      }  
      else
      {
        screenLayout.buildNewDetails(fieldNames, numFields, "gr", data);
        
        documentUtils.getNextCode(con, stmt, rs, "goodsr", true, code);

        generalUtils.repAlphaUsingOnes(data, 5000, "GRCode", generalUtils.stringFromBytes(code, 0L));
        generalUtils.repAlphaUsingOnes(data, 5000, "Status", "L");
      }

      generalUtils.repAlphaUsingOnes(data, 5000, "Date", generalUtils.today(localDefnsDir, defnsDir));

      ddlData = documentUtils.getStoreDDLData(con, stmt, rs, "gr.storecode", ddlData, ddlDataUpto, ddlDataLen);

      prependEdit(con, stmt, rs, unm, sid, uty, men, den, dnm, bnm, errStr, "C", generalUtils.stringFromBytes(code, 0L), date, "208a.htm",
                  localDefnsDir, defnsDir, prependCode, req, bytesOut);

      screenLayout.bufferToBuffer(buf1, buf2, size1, size2, source, numFields, fieldNames, fieldTypes, fieldSizes, 'E', 'H', data, 5000,
                           ddlData, ddlDataUpto[0], javaScriptCode, prependCode);

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
    scoutln(b, bytesOut, "_.lineFile=grl.line\001");
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3031, unm, uty, dnm, localDefnsDir, defnsDir) && serverUtils.passLockCheck(con, stmt, rs, "gr", date, unm))
      scoutln(b, bytesOut, "_.permitted=y\001");
    else scoutln(b, bytesOut, "_.permitted=n\001");

    scoutln(b, bytesOut, "_.stockRec=grl.itemcode\001");
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3001, unm, uty, dnm, localDefnsDir, defnsDir) && serverUtils.passLockCheck(con, stmt, rs, "gr", date, unm))
      scoutln(b, bytesOut, "_.stockPermitted=y\001");
    else scoutln(b, bytesOut, "_.stockPermitted=n\001");
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void javaScript(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, String code, String unm, String sid, String uty,
                          String men, String den, String dnm, String bnm, byte[] b, String localDefnsDir, String defnsDir, int[] bytesOut)
                          throws Exception
  {
    b[0]= '\000';

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3027, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(b, bytesOut, "<script language=\"JavaScript\">");
      scoutln(b, bytesOut, "function affect(line){");
      scoutln(b, bytesOut, "window.location.replace(\"/central/servlet/GoodsReceivedLine?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + code + "&p2=\"+line+\"&p3=&p4=\");}</script>");
    }
    
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3001, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(b, bytesOut, "<script language=\"JavaScript\">");
      scoutln(b, bytesOut, "function stockRec(code){");
      scoutln(b, bytesOut, "window.location.href=\"/central/servlet/MainPageUtils1a?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+code;}</script>");
    }    
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
                       String code, String date, byte[] b, HttpServletRequest req, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    String code2 = code; 

    b[0]= '\000';
    scoutln(b, bytesOut, "<html><head><title>" + code + "</title>");
    scoutln(b, bytesOut, "<script language=\"JavaScript\">\n");

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 153, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(b, bytesOut, "function fetch(){");
      scoutln(b, bytesOut, "window.location.replace(\"/central/servlet/GoodsReceivedEdit?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + code2
                           + "&p2=A&p3=&p4=\");}\n");
    }  

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3027, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(b, bytesOut, "function update(option){");
      scoutln(b, bytesOut, "window.location.href=\"/central/servlet/GoodsReceivedUpdateStock?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=\" + option + \"&p1=" + code2 + "\";}");
    }   
    
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3026, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(b, bytesOut, "function add(){");
      scoutln(b, bytesOut, "window.location.replace(\"/central/servlet/GoodsReceivedLine?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + code2 + "&p2=&p3=&p4=\");}");
    }
      
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3035, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(b, bytesOut, "function invoice(){");
      scoutln(b, bytesOut, "var p1='CREATEBASEDONGR:'+'" + code + "';");
      scoutln(b, bytesOut, "window.location.href=\"/central/servlet/PurchaseInvoicePage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 6096, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(b, bytesOut, "function stores(){");
      scoutln(b, bytesOut, "window.location.href=\"/central/servlet/DocumentStores?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + code2 + "&p2=GRN\";}");
    }
    
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 1025, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(b, bytesOut, "function trace(){");
      scoutln(b, bytesOut, "window.location.replace(\"/central/servlet/DocumentTraceExecute?unm=" + unm + "&sid=" + sid
                         + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + code2
                         + "&p2=G\");}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 1025, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(b, bytesOut, "function trace2(){");
      scoutln(b, bytesOut, "window.location.replace(\"/central/servlet/DocumentTraceExecute?unm=" + unm + "&sid=" + sid
                         + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + code2
                         + "&p2=G&bta=Y\");}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3024, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(b, bytesOut, "function waiting(){");
      scoutln(b, bytesOut, "window.location.href=\"/central/servlet/ProductCheckWaiting?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men="
                         + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + code2 + "\";}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 11800, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(b, bytesOut, "function trail(){");
      scoutln(b, bytesOut, "window.location.href=\"/central/servlet/TrailShow?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men="
                         + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + code2 + "&p2=G\";}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3033, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(b, bytesOut, "function fromPOs(){");
      scoutln(b, bytesOut, "this.location.href=\"/central/servlet/_3033?unm=" + unm + "&sid=" + sid + "&uty=" + uty
                          + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3034, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(b, bytesOut, "function fromLPs(){");
      scoutln(b, bytesOut, "this.location.href=\"/central/servlet/GoodsReceivedBuilderPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty
                          + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
  }

    String cssDirectory = directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir);
    String cssDocumentDirectory = directoryUtils.getCssDocumentDirectory(con, stmt, rs, dnm);
    scoutln(b, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + cssDirectory + "general.css\">\n");
    scoutln(b, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + cssDocumentDirectory + "document.css\">\n");
    
    outputPageFrame(con, stmt, rs, b, req, ' ', ' ', "", "GoodsReceivedServices", date, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

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

    scoutln(b, bytesOut, "function setCodeS(code){document.forms[0].CompanyCode.value=code;");
    scoutln(b, bytesOut, "main.style.visibility='visible';second.style.visibility='hidden';second.style.height='0';submenu.style.visibility='visible';}");

    scoutln(b, bytesOut, "function save(thisOp){var saveStr=\"\";" + saveStr + "\n");
    scoutln(b, bytesOut, "var saveStr2=strip(saveStr);window.location.replace(\"/central/servlet/GoodsReceivedUpdate?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1="
                       + cad + "&p2=" + code2 + "&p3=\"+saveStr2+\"&p4=\"+thisOp)}\n");
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

    outputPageFrame(con, stmt, rs, b, req, ' ', cad.charAt(0), "", "GoodsReceivedEdit", date, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    if(errStr.length() > 0 && ! errStr.equalsIgnoreCase("null"))
      scoutln(b, bytesOut, errStr);

    scoutln(b, bytesOut, "<form>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // rtns: 'X' if already exists
  //       ' ' if updated successfully
  //       'N' if no code specified
  //       'F' if not updated
  //       'I' if illegal code
  public char putLine(Connection con, Statement stmt, ResultSet rs, byte[] originalLine, byte[] code, String unm, String dnm, String localDefnsDir, String defnsDir, char cad, byte[] recData, int recDataLen, byte[] rtnLineBytes, int[] bytesOut)
                      throws Exception
  {
    byte[] lineBytes = new byte[20];
    byte[] b = new byte[100];
    generalUtils.catAsBytes("Line", 0, b, true);

    if(searchDataString(recData, recDataLen, "grl", b, lineBytes) == -1)
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

    sortFields(recData, buf, "grl"); // sorts the data field (using buf in the process); results are put back into recData
    recDataLen = generalUtils.lengthBytes(recData, 0);
    generalUtils.zeroize(buf, 2000);
        
    String fieldNames = getFieldNamesGRL();
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

      if(searchDataString(recData, recDataLen, "grl", fieldName, value) != -1) // entry exists
      {
        if(fieldCount == 1) // itemCode
          itemCodeIn = generalUtils.stringFromBytes(value, 0L);
        else
        if(fieldCount == 17) // mfr
          mfrIn = generalUtils.stringFromBytes(value, 0L);
        else
        if(fieldCount == 18)
          mfrCodeIn = generalUtils.stringFromBytes(value, 0L);
        else
        if(fieldCount == 20) // qccompleteDate
        {
          generalUtils.putAlpha(buf, 2000, (short)20, generalUtils.convertDateToSQLFormat(generalUtils.stringFromBytes(value, 0L)));  
        }
        else generalUtils.repAlpha(buf, 2000, (short)fieldCount, value);
      }
      
      ++fieldCount;
    }
    
    String[] itemCodeOut = new String[1];
    String[] mfrOut      = new String[1];
    String[] mfrCodeOut  = new String[1];
    inventory.mapCodes(con, stmt, rs, itemCodeIn, mfrIn, mfrCodeIn, itemCodeOut, mfrOut, mfrCodeOut);
    generalUtils.strToBytes(itemCode, itemCodeOut[0]);
    generalUtils.repAlpha(buf, 2000, (short)1,  itemCodeOut[0]);
    generalUtils.repAlpha(buf, 2000, (short)17, mfrOut[0]);
    generalUtils.repAlpha(buf, 2000, (short)18, mfrCodeOut[0]);

    generalUtils.repAlpha(buf, 2000, (short)0,  code);
    generalUtils.repAlpha(buf, 2000, (short)10, lineBytes);
   
    // just-in-case no value then need to insert a default
    if((generalUtils.dfsAsStr(buf, (short)4)).length() == 0) // quantity 
      generalUtils.putAlpha(buf, 2000, (short)4, "0");

    if((generalUtils.dfsAsStr(buf, (short)15)).length() == 0) // poLine 
      generalUtils.putAlpha(buf, 2000, (short)15, "0");

    if((generalUtils.dfsAsStr(buf, (short)20)).length() == 0) // qccompletedate
      generalUtils.putAlpha(buf, 2000, (short)20, "1970-01-01");

    if((generalUtils.dfsAsStr(buf, (short)22)).length() == 0) // location
      generalUtils.putAlpha(buf, 2000, (short)22, "Unknown");

    // fetch item details
    generalUtils.strToBytes(fieldName, "Description");
    if(searchDataString(recData, recDataLen, "grl", fieldName, value) != -1) // exists
    {
      if(value[0] == '\000') // description fld is blank
      {
        byte[] data = new byte[3000];

        if(inventory.getStockRecGivenCode(con, stmt, rs, itemCodeOut[0], '\000', data) != -1) // exists
        {
          generalUtils.dfs(data, (short)1, b);           // desc
          generalUtils.repAlpha(buf, 2000, (short)2, b);

          if(miscDefinitions.includeRemark(con, stmt, rs))
          {    
            value[0]= '\000';  
            scoutln(value, bytesOut, " \n" + generalUtils.dfsAsStr(data, (short)2));
          }
        }
      }
      else // strip only the first desc line for glL rec
      {
        getDescriptionLine(0, value, b);
        generalUtils.repAlpha(buf, 2000, (short)2, b);
      }
    }

    reCalculate(buf, 2000);

    if(putRecLine(con, stmt, rs, code, lineBytes, newOrEdit, buf, dnm, localDefnsDir, defnsDir) != 'F')
    {
      updateDLM(con, stmt, rs, code, unm, dnm, localDefnsDir, defnsDir);
      return ' ';
    }

    return 'F';
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public void updateStores(Connection con, Statement stmt, ResultSet rs, byte[] code, String store, String unm, String dnm, String localDefnsDir, String defnsDir)
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

        generalUtils.repAlpha(buf, 2000, (short)12, store);
        generalUtils.repAlpha(buf, 2000, (short)7, unm);
        generalUtils.repAlpha(buf, 2000, (short)6, '\000');

        generalUtils.dfs(buf, (short)10, b); // line

        putRecLine(con, stmt, rs, code, b, 'E', buf, dnm, localDefnsDir, defnsDir);
      }
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void updateDLM(Connection con, Statement stmt, ResultSet rs, byte[] code, String unm, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    stmt = con.createStatement();
    stmt.executeUpdate("UPDATE gr SET SignOn = '" + unm + "', DateLastModified = NULL WHERE GRCode = '" + generalUtils.stringFromBytes(code, 0L) + "'");
    if(stmt != null) stmt.close();
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------
  public void reCalculate(byte[] buf, int dataBufLen) throws Exception
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

    double amt = (qty * unitPrice);
    generalUtils.repDoubleGivenSeparator('2', '\000', buf, dataBufLen, (short)5, amt); // origin-0
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public String getAGRFieldGivenCode(Connection con, Statement stmt, ResultSet rs, String fieldName, String code) throws Exception
  {
    byte[] value = new byte[300];
    byte[] codeB = new byte[21];
    generalUtils.strToBytes(codeB, code);

    getAGRFieldGivenCode(con, stmt, rs, fieldName, codeB, value);

    return generalUtils.stringFromBytes(value, 0L);
  }
  public void getAGRFieldGivenCode(Connection con, Statement stmt, ResultSet rs, String fieldName, byte[] grCode, byte[] value) throws Exception
  {
    if(grCode[0] == '\000') // just-in-case
      return;

    generalUtils.toUpper(grCode, 0);

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT " + fieldName + " FROM gr WHERE GRCode = '" + generalUtils.stringFromBytes(grCode, 0L) + "'");
    if(! rs.next())
      value[0] = '\000';
    else generalUtils.strToBytes(value, rs.getString(1));
    
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

    String layoutFile="208b.htm";

    short[] fieldSizesGRL = new short[40]; // plenty
    getFieldSizesGRL(fieldSizesGRL);

    numFields = screenLayout.layoutToBuffer(buf1, buf2, size1, fieldNames, fieldTypes, fieldSizes, localDefnsDir, defnsDir, layoutFile,
                                     1, getFieldNamesGRL(), fieldSizesGRL, null, null, null, null);

    byte[] data = new byte[4000]; // plenty
    byte[] ddlData = new byte[1000];
    int[] ddlDataLen  = new int[1];
    ddlDataLen[0] = 1000;
    int[] ddlDataUpto = new int[1];
    ddlDataUpto[0] = 0;
    int dataLen = 2000;
    byte[] lineData = new byte[1000];
    int nextLine=0;
    char newOrEdit;
    byte[] b = new byte[4000];

    if(line[0] != '\000') // existing line
    {
      if(code[0] != '\000') // just-in-case
      {
        if(dataAlready[0] != '\000') // coming with an err msg - passing-in data already entered but the data's fields may (will) not
        {                            // be in the correct order
          sortFields(dataAlready, data, "grl");
        }
        else
        {
          getLine(con, stmt, rs, code, line, '\001', data, dnm, localDefnsDir, defnsDir, bytesOut);

          char dpOnQuantities = miscDefinitions.dpOnQuantities(con, stmt, rs, "5");
          generalUtils.bytesDPsGivenSeparator(true, '\001', dpOnQuantities, data, 4000, 4); // origin-0, quantity
          char dpOnUnitPrice  = miscDefinitions.dpOnUnitPrice(con, stmt, rs, "4");
          generalUtils.bytesDPsGivenSeparator(true, '\001', dpOnUnitPrice,  data, 4000, 3); // unitPrice 
          generalUtils.bytesDPsGivenSeparator(true, '\001', '2',  data, 4000, 5);  // amount

          generalUtils.dfsGivenSeparator(true, '\001', data, (short) 20, b); // qccheckedDate
          generalUtils.convertFromYYYYMMDD(b);
          generalUtils.repAlphaGivenSeparator('\001', data, 4000, (short) 20, b);
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
        sortFields(dataAlready, data, "grl");
            
        generalUtils.dfsGivenSeparator(true, '\001', data, (short)2, b); // desc
        generalUtils.repAlphaGivenSeparator('\001', data, 4000, (short)2, b);
        generalUtils.repAlphaGivenSeparator('\001', data, 4000, (short)10, generalUtils.intToStr(nextLine)); // line
        generalUtils.repAlphaGivenSeparator('\001', data, 4000, (short)11, generalUtils.intToStr(nextLine)); // entry
      }
      else
      {
        data[0]= '\000';
        scoutln(data, bytesOut, "grl.entry=" + generalUtils.intToStr(nextLine) + "\001");
      }

      newOrEdit = 'N';
    }

    ddlData = documentUtils.getStoreDDLData(con, stmt, rs, "grl.storecode", ddlData, ddlDataUpto, ddlDataLen);
    ddlData = inventory.getMfrsDDLData(con, stmt, rs, "grl.manufacturer", ddlData, ddlDataUpto, ddlDataLen);

    dataLen = generalUtils.lengthBytes(data, 0);

    String cad;
    if(newOrEdit == 'N')
      cad = "C";
    else cad = "A";

    byte[] prependCode = new byte[25000];
    prependEditLine(con, stmt, rs, unm, sid, uty, men, den, dnm, bnm, errStr, cad, generalUtils.stringFromBytes(code, 0L),
                    generalUtils.stringFromBytes(line, 0L), "208b.htm", localDefnsDir, defnsDir, prependCode, newOrEdit, req,
                    bytesOut);

    screenLayout.bufferToBuffer(buf1, buf2, size1, size2, source, numFields, fieldNames, fieldTypes, fieldSizes, 'E', 'H', data, dataLen,
                         ddlData, ddlDataUpto[0], null, prependCode, false, false, "", nextLine);

    append(con, stmt, rs, buf1, buf2, source, size1, size2, unm, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    screenLayout.bufferToOut(buf1, buf2, source, out);

    return true;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void prependEditLine(Connection con, Statement stmt, ResultSet rs, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String errStr,
                               String cad, String code, String line, String layoutFile, String localDefnsDir, String defnsDir, byte[] b,
                               char newOrEdit, HttpServletRequest req, int[] bytesOut) throws Exception
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
    scoutln(b, bytesOut, "var saveStr2=strip(saveStr);window.location.replace(\"/central/servlet/_3030?unm=" + unm + "&sid=" + sid
                       + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + cad + "&p2=" + code2 + "&p3=" + line
                       + "&p4=\"+thisOp+\"&p5=\"+saveStr2);}\n");
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

    outputPageFrame(con, stmt, rs, b, req, newOrEdit, ' ', "", "_3026", "", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

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
        thisOne = generalUtils.strToInt(generalUtils.dfsAsStrGivenBinary1(true, data, (short)11)); // 'line': origin-0 + 1 (for leading count)
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
    generalUtils.catAsBytes("GRCode", 0, str, true);

    if(searchDataString(recData, recDataLen, "gr", str, code) == 0)
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
    generalUtils.repAlpha(buf, 2000, (short)0, code);

    byte[] value   = new byte[501];
    byte[] fldName = new byte[50];

    String fieldNamesGR = getFieldNamesGR();

    byte[] companyCode = new byte[21];
    int len = fieldNamesGR.length();
    int x=0, y, fieldCount=0;
    while(x < len)
    {
      y=0;
      while(x < len && fieldNamesGR.charAt(x) != ',')
        fldName[y++] = (byte)fieldNamesGR.charAt(x++);
      fldName[y] = '\000';
      ++x;
      
      while(x < len && fieldNamesGR.charAt(x) == ' ')
        ++x;

      if(searchDataString(recData, recDataLen, "gr", fldName, value) != -1) // entry exists
      {  
        if(fieldCount == 1) // date
          generalUtils.repAlpha(buf, 2000, (short)1, generalUtils.convertDateToSQLFormat(generalUtils.stringFromBytes(value, 0L)));  
        else
        if(fieldCount == 2) // companycode
        {
          generalUtils.toUpper(value, 0);
          generalUtils.bytesToBytes(companyCode, 0, value, 0);
          generalUtils.putAlpha(buf, 2000, (short)2, value);
        }  
        else
        if(fieldCount == 3) // companyname
        {
          if(value[0] == '\000')
          {
            byte[] b    = new byte[100];
            byte[] data = new byte[3000];
            if(supplier.getSupplierRecGivenCode(con, stmt, rs, companyCode, '\000', dnm, data, localDefnsDir, defnsDir) != -1) // exists
            {
              generalUtils.dfs(data, (short)1, b);           // name
              generalUtils.repAlpha(buf, 2000, (short)3, b);
            }
          }
          else generalUtils.putAlpha(buf, 2000, (short)3, value);
        }
        else
        if(fieldCount == 5) // signon
        {
          if(value[0] == '\000')
            generalUtils.putAlpha(buf, 2000, (short)5, unm);
          else generalUtils.putAlpha(buf, 2000, (short)5, value);
        }
        else
        if(fieldCount == 6) // notes
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
          generalUtils.putAlpha(buf, 2000, (short)6, b2);
        }
        else
        if(fieldCount == 13) // dateStockProcessed
        {
          if(value[0] == '\000')
            generalUtils.putAlpha(buf, 1000, (short)13, "1970-01-01");
          else generalUtils.putAlpha(buf, 1000, (short)13, generalUtils.convertDateToSQLFormat(generalUtils.stringFromBytes(value, 0L)));  
        }
        else
        if(fieldCount == 14) // dateAccountsProcessed
        {
          if(value[0] == '\000')
            generalUtils.putAlpha(buf, 1000, (short)14, "1970-01-01");
          else generalUtils.putAlpha(buf, 1000, (short)14, generalUtils.convertDateToSQLFormat(generalUtils.stringFromBytes(value, 0L)));  
        }
        else if(value[0] != '\000') generalUtils.repAlpha(buf, 2000, (short)fieldCount, value);
      }
      
      ++fieldCount;
    }

    // get existing stockProcessed value and re-use
    byte[] stockProcessed = new byte[2];    
    getAGRFieldGivenCode(con, stmt, rs, "StockProcessed", code, stockProcessed);
    generalUtils.repAlpha(buf, 2000, (short)11, stockProcessed);    
    
    if((generalUtils.dfsAsStr(buf, (short)13)).length() == 0) // dateStockProcessed 
      generalUtils.repAlpha(buf, 2000, (short)13, "1970-01-01"); 
    
    if((generalUtils.dfsAsStr(buf, (short)14)).length() == 0) // dateAccountsProcessed 
      generalUtils.repAlpha(buf, 2000, (short)14, "1970-01-01"); 

    return putRecHeadGivenCode(con, stmt, rs, code, newOrEdit, buf, dnm, localDefnsDir, defnsDir);
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public int getRecGivenCode(Connection con, Statement stmt, ResultSet rs, byte[] code, char separator, byte[] data, String dnm, String localDefnsDir, String defnsDir,
                             int[] bytesOut) throws Exception
  {
    if(code[0] == '\000') // just-in-case
      return -1;

    generalUtils.toUpper(code, 0);

    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT * FROM gr WHERE GRCode = '" + generalUtils.stringFromBytes(code, 0L) + "'");
    if(! rs.next())
    {
      rs.close();
      stmt.close();
      return -1;
    }      
    
    ResultSetMetaData rsmd = rs.getMetaData();
    
    String fieldTypes = getFieldTypesGR();
     
    if(separator == '\000')
    {
      int x, y=0;
      String s = rs.getString(1);
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
      String fieldNames = getFieldNamesGR();
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
        scoutln(data, bytesOut, "gr." + thisFieldName + "=" + getValue(count, thisFieldType, rs, rsmd) + "\001");
      
        ++x;
        while(x < len && fieldNames.charAt(x) == ' ')
          ++x;
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
      String fieldTypes = getFieldTypesGR();
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
      
        q = "INSERT INTO gr (" + getFieldNamesGR() + ") VALUES (" + opStr + ")";
      }
      else // newOrEdit == 'E'
      {
        String thisFieldName;
        char thisFieldType;

        // change: abc\0def\0... format into: CompanyCode='abc',Name='def',...

        String fieldNames = getFieldNamesGR();
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

        q = "UPDATE gr SET " + opStr + " WHERE GRCode = '" + generalUtils.stringFromBytes(code, 0L) + "'";
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
      String fieldTypes = getFieldTypesGRL();
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
      
        q = "INSERT INTO grl (" + getFieldNamesGRL() + ") VALUES (" + opStr + ")";
      }
      else // newOrEdit == 'E'
      {
        String thisFieldName;
        char thisFieldType;

        // change: abc\0def\0... format into: CompanyCode='abc',Name='def',...

        String fieldNames = getFieldNamesGRL();
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

        q = "UPDATE grl SET " + opStr + " WHERE GRCode = '" + generalUtils.stringFromBytes(code, 0L) + "' AND Line = '"  + generalUtils.stringFromBytes(line, 0L) + "'";
      }

      stmt.executeUpdate(q);

      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    { 
      try
      {
        System.out.println("GoodsReceivedNote: " + e);
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
      generalUtils.dfs(data, (short)11, entry);

      stmt = con.createStatement();
    
      stmt.executeUpdate("DELETE FROM grl WHERE GRCode = '" + generalUtils.stringFromBytes(code, 0L) + "' AND Line = '" + generalUtils.stringFromBytes(line, 0L) + "'");

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
    String fieldTypes = getFieldTypesGRL();
    String fieldNames = getFieldNamesGRL();    
    byte[] newItem = new byte[1000];
    String thisFieldName;
    char thisFieldType;
    int count;
    int lenFieldNames = fieldNames.length();
    int lenFieldTypes = fieldTypes.length();
    data[0] = '\000';
        
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT * FROM grl WHERE GRCode = '" + generalUtils.stringFromBytes(code, 0L) + "'");
    ResultSetMetaData rsmd = rs.getMetaData();
    while(rs.next())
    {
      if(separator == '\000')
      {
        s="";
        for(x=1;x<=lenFieldTypes;++x) // numLines
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
          s += "grl." + thisFieldName + "=" + getValue(count, thisFieldType, rs, rsmd) + "\001";
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
    String fieldTypes = getFieldTypesGRL();
    String fieldNames = getFieldNamesGRL();
    String thisFieldName;
    char thisFieldType;
    int x, count, len = fieldNames.length(), numFields = fieldTypes.length();
    data[0] = '\000';
    
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT * FROM grl WHERE GRCode = '" + generalUtils.stringFromBytes(code, 0L) + "' AND Line = '"
                                     + generalUtils.stringFromBytes(line, 0L) + "'");
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
          scoutln(data, bytesOut, "grl." + thisFieldName + "=" + getValue(count, thisFieldType, rs, rsmd) + "\001");
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
  public boolean updateAGRField(Connection con, Statement stmt, ResultSet rs, byte[] code, String fieldName, String fieldValue, String dnm, String localDefnsDir,
                                String defnsDir) throws Exception
  {
    try
    {
      stmt = con.createStatement();
      stmt.executeUpdate("UPDATE gr SET " + fieldName + " = '" + fieldValue + "', DateLastModified = NULL WHERE GRCode = '"
                         + generalUtils.stringFromBytes(code, 0L) + "'");
      if(stmt != null) stmt.close();
      return true;
    }
    catch(Exception e)
    {
      System.out.println("GoodsReceivedNote: " + e);
      try
      {
        if(stmt != null) stmt.close();
      }
      catch(Exception e2) {}
    }
    return false;
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void sortFields(byte[] ipBuf, byte[] opBuf, String which) throws Exception
  {
    String thisFieldName;
    String fieldNames;
    int x=0, y;
    byte[] thisFieldNameB = new byte[50];
    byte[] value          = new byte[1000];
    
    if(which.equals("gr"))
      fieldNames = getFieldNamesGR();
    else // if(which.equals("pll"))
      fieldNames = getFieldNamesGRL();
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
  private void outputPageFrame(Connection con, Statement stmt, ResultSet rs, byte[] b, HttpServletRequest req, char newOrEdit, char cad, String bodyStr, String callingServlet, String date, String unm, String sid, String uty, String men,
                               String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    int[] hmenuCount = new int[1];

    String subMenuText = dashboardUtils.buildSubMenuText(con, stmt, rs, req, "153", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount);

    subMenuText += "<table id='title' width=100%><tr><td colspan=2 nowrap><p>Goods Received Note" + directoryUtils.buildHelp(153) + "</td></tr></table>";

    subMenuText += buildSubMenuText(con, stmt, rs, req, cad, newOrEdit, callingServlet, date, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount);

    scoutln(b, bytesOut, authenticationUtils.buildScreen(con, stmt, rs, req, true, unm, sid, uty, men, den, dnm, bnm, callingServlet, subMenuText, hmenuCount[0], bodyStr, "", localDefnsDir, defnsDir));

    scoutln(b, bytesOut, "<table border=0><tr><td valign=top><div id='second' style=\"{margin-top:0px;}\"></div></td>");
    scoutln(b, bytesOut, "<td valign=top><div id='third' style=\"{margin-top:0px;margin-left:0px;}\"></div></td></tr></table>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String buildSubMenuText(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, char cad, char newOrEdit, String callingServlet, String date, String unm, String sid, String uty, String men, String den, String dnm,
                                  String bnm, String localDefnsDir, String defnsDir, int[] hmenuCount) throws Exception
  {
    String s = "<div id='submenu'>";

    ++hmenuCount[0];

    if(callingServlet.equals("GoodsReceivedServices")) // display doc
      s += drawOptions3025(con, stmt, rs, req, hmenuCount, date, unm, uty, dnm, localDefnsDir, defnsDir);
    else
    if(callingServlet.equals("GoodsReceivedEdit")) // editing header
      s += drawOptions3027(hmenuCount, cad, unm, sid, uty, men, den, dnm, bnm);
    else 
    if(callingServlet.equals("_3026")) // editing line
      s += drawOptions3029(hmenuCount, newOrEdit, unm, sid, uty, men, den, dnm, bnm);
    
    s += "</div>";
    
    --hmenuCount[0];
    
    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String drawOptions3025(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, int[] hmenuCount, String date, String unm, String uty, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {      
    String s = "";
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3027, unm, uty, dnm, localDefnsDir, defnsDir) && serverUtils.passLockCheck(con, stmt, rs, "gr", date, unm))
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\"><a href=\"javascript:fetch()\">Edit Details</a></dt></dl>\n";

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3026, unm, uty, dnm, localDefnsDir, defnsDir) && serverUtils.passLockCheck(con, stmt, rs, "gr", date, unm))
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\"><a href=\"javascript:add()\">Add New Line</a></dt></dl>\n";
    if(serverUtils.passLockCheck(con, stmt, rs, "gr", date, unm))
    {
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0] + "');\">Change</dt><dd id='hmenu" + hmenuCount[0]++ + "'><ul>";
      s += "<li><a href=\"javascript:stores()\">Change Stores</a></li></ul></dd></dl>\n";
    }
    
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3037, unm, uty, dnm, localDefnsDir, defnsDir) && serverUtils.passLockCheck(con, stmt, rs, "gr", date, unm))
    {
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0] + "');\">Update</dt><dd id='hmenu" + hmenuCount[0]++ + "'><ul>";
      s += "<li><a href=\"javascript:update('S')\">Set In-Stock</a></li><li><a href=\"javascript:update('T')\">Set In-Transit</a></li>";
      s += "</ul></dd></dl>\n";
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3035, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0] + "');\">Convert</dt><dd id='hmenu" + hmenuCount[0]++ + "'><ul>";
      s += "<li><a href=\"javascript:invoice()\">Invoice</a></li></ul></dd></dl>";
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 1025, unm, uty, dnm, localDefnsDir, defnsDir))
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\"><a href=\"javascript:trace()\">Trace</a></dt></dl>\n";

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 1025, unm, uty, dnm, localDefnsDir, defnsDir))
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\"><a href=\"javascript:trace2()\">Long Trace</a></dt></dl>\n";

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 11800, unm, uty, dnm, localDefnsDir, defnsDir))
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\"><a href=\"javascript:trail()\">Trail</a></dt></dl>\n";

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3024, unm, uty, dnm, localDefnsDir, defnsDir))
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\"><a href=\"javascript:waiting()\">Waiting</a></dt></dl>\n";
      
    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String drawOptions3027(int[] hmenuCount, char cad, String unm, String sid, String uty, String men, String mainDNM, String dnm, String bnm) throws Exception
  {      
    String s = "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\"><a href=\"javascript:save(' ')\">Save Changes</a></dt></dl>\n";

    if(cad != 'C')
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\"><a href=\"javascript:save('X')\">Cancel Changes</a></dt></dl>\n";

    s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\"><script language='Javascript'>";
    s += "var alreadyOnce=false;function select(){main.style.visibility='hidden';second.style.visibility='visible';submenu.style.visibility='hidden';if(!alreadyOnce){select5069('A');alreadyOnce=true;}}";

    s += "var req2;"; 
    s += "function initRequest2(url)";
    s += "{if(window.XMLHttpRequest){req2=new XMLHttpRequest();}else if(window.ActiveXObject){req2=new ActiveXObject(\"Microsoft.XMLHTTP\");}}";

    s += "function select5069(searchChar)";
    s += "{var url = \"http://" + men + "/central/servlet/SupplierSelect?unm=\" + escape('" + unm + "') + \"&sid=\" + escape('" + sid + "') + \"&uty=\" + escape('" + uty + "') + \"&men=\" + escape('" + men + "') + \"&den=\" + escape('" + mainDNM
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
      
    s += "</script><a href=\"javascript:select()\">Select Supplier</a></dt></dl>\n";
    
    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String drawOptions3029(int[] hmenuCount, char newOrEdit, String unm, String sid, String uty, String men, String mainDNM, String dnm, String bnm) throws Exception
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
    s += "var alreadyOnce=false;function select(){main.style.visibility='hidden';second.style.visibility='visible';third.style.visibility='visible';submenu.style.visibility='hidden';if(!alreadyOnce){select2008('A');alreadyOnce=true;}}\n";

    s += "var req2;";    
    s += "function initRequest2(url)";
    s += "{if(window.XMLHttpRequest){req2=new XMLHttpRequest();}else if(window.ActiveXObject){req2=new ActiveXObject(\"Microsoft.XMLHTTP\");}}\n";

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
    s += "{if(window.XMLHttpRequest){req3=new XMLHttpRequest();}else if(window.ActiveXObject){req3=new ActiveXObject(\"Microsoft.XMLHTTP\");}}\n";

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

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public double getTotalReceivedForAPOLine(Connection con, Statement stmt, ResultSet rs, String poCode, String poLine) throws Exception
  {
    String[] grnCodes = new String[1];

    return getTotalReceivedForAPOLine(con, stmt, rs, poCode, poLine, grnCodes);
  }
  public double getTotalReceivedForAPOLine(Connection con, Statement stmt, ResultSet rs, String poCode, String poLine, String[] grnCodes) throws Exception
  {
    double actualQty = 0.0;

    grnCodes[0] = "";

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.GRCode, t2.Quantity FROM grl AS t2 INNER JOIN gr AS t1 ON t2.GRCode = t1.GRCode "
                         + "WHERE t2.POCode = '" + poCode + "' AND t2.POLine = '" + poLine + "' AND t1.Status != 'C' AND t1.StockProcessed = 'Y'");

    String grCode, qty;
    
    while(rs.next())
    {
       grCode = rs.getString(1);
       qty    = rs.getString(2);

       grnCodes[0] += (grCode + " ");
       actualQty += generalUtils.doubleFromStr(qty);
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return actualQty;
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public boolean isGRNCancelled(Connection con, Statement stmt, ResultSet rs, byte[] grnCode, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    if(grnCode[0] != '\000')
    {
      byte[] b    = new byte[2];

      getAGRFieldGivenCode(con, stmt, rs, "Status", grnCode, b);
      
      if(b[0] == '\000') // no value or rec not found
        return true;
      
      if(b[0] == 'C') // not live
        return true;
      
      return false;
    }
    
    return true;
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public boolean saveNewGRRecHeader(Connection con, Statement stmt, ResultSet rs, byte[] supplierCode, byte[] supplierName, String signOn, String notes, String dnm,
                                    String localDefnsDir, String defnsDir, byte[] nextCode) throws Exception
  {
    byte[] buf = new byte[1000];

    documentUtils.getNextCode(con, stmt, rs, "goodsr", true, nextCode);

    generalUtils.putAlpha(buf, 1000, (short)0, nextCode);
    generalUtils.putAlpha(buf, 1000, (short)1, generalUtils.todaySQLFormat(localDefnsDir, defnsDir));
    generalUtils.putAlpha(buf, 1000, (short)2, supplierCode);
    generalUtils.putAlpha(buf, 1000, (short)3, supplierName);
    // poCode - leave blank on header
    generalUtils.putAlpha(buf, 1000, (short)5, signOn);
    generalUtils.putAlpha(buf, 1000, (short)6, notes);
    generalUtils.putAlpha(buf, 1000, (short)7, "0"); // attention
    // dlm
    // tlm
    // storeCode - leave blank on the header
    generalUtils.putAlpha(buf, 1000, (short)11, "N"); // StockProcessed
    generalUtils.putAlpha(buf, 1000, (short)13, "1970-01-01"); // DateStockProcessed
    generalUtils.putAlpha(buf, 1000, (short)14, "1970-01-01"); // DateAccountsProcessed

    if(putRecHeadGivenCode(con, stmt, rs, nextCode, 'N', buf, dnm, localDefnsDir, defnsDir) == ' ')
      return true;
    
    return false;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean saveNewGRRecLine(Connection con, Statement stmt, ResultSet rs, byte[] grCode, byte[] grLine, String itemCode, String desc, String unitPrice, String quantity, byte[] amount,
                                  byte[] poCode, byte[] poLine, String storeCode, String doCode, String invoiceCode, String mfr, String mfrCode,
                                  String signOn, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    byte[] buf = new byte[1000];

    generalUtils.putAlpha(buf, 1000, (short)0, grCode);
    generalUtils.putAlpha(buf, 1000, (short)1, itemCode);
    generalUtils.putAlpha(buf, 1000, (short)2, desc);
    generalUtils.putAlpha(buf, 1000, (short)3, unitPrice);
    generalUtils.putAlpha(buf, 1000, (short)4, quantity);
    generalUtils.putAlpha(buf, 1000, (short)5, amount);
    // dlm
    generalUtils.putAlpha(buf, 1000, (short)7, signOn);
    generalUtils.putAlpha(buf, 1000, (short)8, poCode);
    generalUtils.putAlpha(buf, 1000, (short)9, "N"); // not processed to PI
    generalUtils.putAlpha(buf, 1000, (short)10, grLine);
    generalUtils.putAlpha(buf, 1000, (short)11, grLine); // entry
    generalUtils.putAlpha(buf, 1000, (short)12, storeCode);
    generalUtils.putAlpha(buf, 1000, (short)13, doCode);
    generalUtils.putAlpha(buf, 1000, (short)14, invoiceCode);
    generalUtils.putAlpha(buf, 1000, (short)15, poLine);
    generalUtils.putAlpha(buf, 1000, (short)16, "N"); // stockprocessed
    generalUtils.putAlpha(buf, 1000, (short)17, mfr);
    generalUtils.putAlpha(buf, 1000, (short)18, mfrCode);
    generalUtils.putAlpha(buf, 1000, (short)19, "N"); // qcComplete
    generalUtils.putAlpha(buf, 1000, (short)20, "1970-01-01"); // qcCompleteDate
    generalUtils.putAlpha(buf, 1000, (short)21, ""); // remark
    
    if(putRecLine(con, stmt, rs, grCode, grLine, 'N', buf, dnm, localDefnsDir, defnsDir) == ' ')
      return true;

    return false;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getGRNCodesForAPOLine(Connection con, Statement stmt, ResultSet rs, String poCode, String poLine, String dnm, String localDefnsDir,
                                      String defnsDir) throws Exception
  {
    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT GRCode FROM grl WHERE POCode = '" + poCode + "' AND POLine = '" + poLine + "' ");// + "AND StockProcessed = 'Y'");

    String grnCodes = "";
    while(rs.next())
    {
      grnCodes += (rs.getString(1) + " ");
    }

    if(stmt != null) stmt.close();
    if(rs   != null) rs.close();
    
    return grnCodes;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(byte[] b, int bytesOut[], String str) throws Exception
  {      
    generalUtils.catAsBytes(str, 0, b, false);
    bytesOut[0] += (str.length() + 2);    
  }

}
