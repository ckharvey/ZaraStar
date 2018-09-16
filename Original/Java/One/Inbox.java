// =======================================================================================================================================================================================================
// System: ZaraStar Document: Inbox Record Access
// Module: Inbox.java
// Author: C.K.Harvey
// Copyright (c) 2001-09 Christopher Harvey. All Rights Reserved.
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
 
public class Inbox
{
  GeneralUtils generalUtils = new GeneralUtils();
  HtmlBuild htmlBuild = new HtmlBuild();
  MessagePage messagePage = new MessagePage();
  ScreenLayout screenLayout = new ScreenLayout();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  MiscDefinitions miscDefinitions = new MiscDefinitions();
  AccountsUtils accountsUtils = new AccountsUtils();
  DocumentUtils documentUtils = new DocumentUtils();
  Customer customer = new Customer();
  DashboardUtils dashboardUtils = new DashboardUtils();
  Inventory inventory = new Inventory();  

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getTableCreateStringInbox() throws Exception
  {  
    return "inbox ( InboxCode char(20) not null, Date date not null,    CompanyName char(60),       FAO char(40),          Designation char(40), "
                 + "EMail char(60),              Phone char(30),        Fax char(30),               ShipAddress1 char(40), ShipAddress2 char(40), "
                 + "ShipAddress3 char(40),       ShipAddress4 char(40), ShipAddress5 char(40),      ShipCountry char(40),  BillAddress1 char(40), "
                 + "BillAddress2 char(40),       BillAddress3 char(40), BillAddress4 char(40),      BillAddress5 char(40), BillCountry char(40), "
                 + "Type char(1),                Notes char(250),       CustomerReference char(40), CompanyCode char(20),  Processed char(1), "
                 + "DateLastModified timestamp,  SignOn char(20),       Status char(1),             UserCodeFrom char(20), UserCodeTo char(20), "
                 + "unique(InboxCode))";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStringsInbox(String[] s) throws Exception
  {
    s[0] = "inboxDateInx on inbox (Date)";
    s[1] = "inboxCompanyCodeInx on inbox (CompanyCode)";
    s[2] = "inboxUserCodeToInx on inbox (UserCodeTo)";
    
    return 3;
  }
      
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNamesInbox() throws Exception
  {  
    return "InboxCode, Date, CompanyName, FAO, Designation, EMail,Phone, Fax, ShipAddress1, ShipAddress2, ShipAddress3, ShipAddress4, ShipAddress5, "
         + "ShipCountry, BillAddress1, BillAddress2, BillAddress3, BillAddress4, BillAddress5, BillCountry, Type, Notes, CustomerReference, "
         + "CompanyCode, Processed, DateLastModified, SignOn, Status, UserCodeFrom, UserCodeTo";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypesInbox() throws Exception
  {
    return "CDCCCCCCCCCCCCCCCCCCCCCCCSCCCC";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizesInbox(short[] sizes) throws Exception
  {
    sizes[0] = 20;   sizes[1] = 0;    sizes[2] = 60;   sizes[3] = 40;   sizes[4] = 40;   sizes[5] = 60;    sizes[6] = 30;    sizes[7] = 30;
    sizes[8] = 40;   sizes[9] = 40;   sizes[10] = 40;  sizes[11] = 40;  sizes[12] = 40;  sizes[13] = 40;   sizes[14] = 40;   sizes[15] = 40;
    sizes[16] = 40;  sizes[17] = 40;  sizes[18] = 40;  sizes[19] = 40;  sizes[20] = 1;   sizes[21] = 250;  sizes[22] = 40;   sizes[23] = 20;
    sizes[24] = 1;   sizes[25] = -1;  sizes[26] = 20;  sizes[27] = 1;   sizes[28] = 20;  sizes[29] = 20;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldStylesInbox() throws Exception
  {
    return "MMOOOOOOOOOOOOOOOOOOOOOOOOOOOO";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getTableCreateStringInboxL() throws Exception
  {  
    return "inboxl ( InboxCode char(20) not null, Line integer,        StockCode char(20),  Manufacturer char(40), Quantity decimal(19,8), "
                  + "Currency char(3),            Price decimal(19,8), UoM char(20),        Description char(80),  DateLastModified timestamp, "
                  + "SignOn char(20),             ManufacturerCode char(60), "
                  + "unique(InboxCode, Line))";
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStringsInboxL(String[] s) throws Exception
  {
    s[0] = "inboxlStockCodeInx on inboxl(StockCode)";
    
    return 1;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNamesInboxL() throws Exception
  {  
    return "InboxCode, Line, StockCode, Manufacturer, Quantity, Currency, Price, UoM, Description, DateLastModified, SignOn, ManufacturerCode";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypesInboxL() throws Exception
  {
    return "CICCFCFCCSCC";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizesInboxL(short[] sizes) throws Exception
  {
    sizes[0] = 20;  sizes[1] = 0;   sizes[2] = 20;  sizes[3] = 40;  sizes[4] = 0;  sizes[5] = 3;  sizes[6] = 0;  sizes[7] = 20;  sizes[8] = 80;
    sizes[9] = -1;  sizes[10] = 20; sizes[11] = 60;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldStylesInboxL() throws Exception
  {
    return "OMOOOOOOOOOO";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean getRecToHTML(Connection con, Statement stmt, ResultSet rs, PrintWriter out, char dispOrEdit, byte[] code, String unm, String sid, String uty, String men, String den,
                              String dnm, String bnm, String localDefnsDir, String defnsDir, char cad, String errStr, byte[] dataAlready,
                              HttpServletRequest req, int[] bytesOut) throws Exception
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

    byte[] data       = new byte[5000];

    byte[][] headData = new byte[1][5000];
    int[] dUpto = new int[1];  dUpto[0] = 0;
    int[] dSize = new int[1];  dSize[0] = 5000;

    byte[] prependCode = new byte[10000];
    int[] bUpto = new int[1];  bUpto[0] = 0;
    int[] bSize = new int[1];  bSize[0] = 10000;

    byte[] b           = new byte[300];

    boolean rtn;

    byte[] javaScriptCode = new byte[1000];
    int[] jsUpto = new int[1];  jsUpto[0] = 0;
    int[] jsSize = new int[1];  jsSize[0] = 1000;
    javaScriptCode = javaScript(con, stmt, rs, req, generalUtils.stringFromBytes(code, 0L), unm, sid, uty, men, den, dnm, bnm, javaScriptCode, localDefnsDir, defnsDir, jsUpto, jsSize, bytesOut);

    byte[] ddlData = new byte[1500];
    int[] ddlDataLen  = new int[1];

    ddlDataLen[0] = 1500;
    int[] ddlDataUpto = new int[1];
    ddlDataUpto[0] = 0;

    if(cad == 'A' && code[0] == '\000')
    {
      messagePage.msgScreen(false, out, req, 6, unm, sid, uty, men, den, dnm, bnm, "Inbox", imagesDir, localDefnsDir, defnsDir, bytesOut);
      return true;
    }

    if(dataAlready[0] != '\000') // coming with an err msg - passing-in data already entered but the data's fields may (will) not be 
    {                          // in the correct order
      dispOrEdit = 'E';
      sortFields(dataAlready, headData[0], "inbox");
    }
    else // get header data
    {
      if(cad == 'A')
      {
        if(getRecGivenCode(con, stmt, rs, code, '\001', headData, dnm, localDefnsDir, defnsDir, dUpto, dSize, bytesOut) == -1)
        {
          messagePage.msgScreen(false, out, req, 6, unm, sid, uty, men, den, dnm, bnm, "Inbox", imagesDir, localDefnsDir, defnsDir, bytesOut);
          return true;
        }
      }
    }

    String date;
    byte[] dateB = new byte[20]; // lock
    if(dataAlready[0] != '\000')
    {
      generalUtils.dfsGivenSeparator(true, '\001', headData[0], (short)1, dateB);
      date = generalUtils.convertDateToSQLFormat(generalUtils.stringFromBytes(dateB, 0L));
    }
    else
    if(cad == 'A')
    {
      generalUtils.dfsGivenSeparator(true, '\001', headData[0], (short)1, dateB);
      date = generalUtils.stringFromBytes(dateB, 0L);
    }
    else date = generalUtils.todaySQLFormat(localDefnsDir, defnsDir); 

    byte[] javaScriptCallCode = new byte[1000]; // lock
    javaScriptCallCode = javaScriptCall(con, stmt, rs, javaScriptCallCode, req, date, unm, uty, dnm, localDefnsDir, defnsDir, jsUpto, jsSize, bytesOut);

    short[] fieldSizesInbox = new short[60]; // plenty
    getFieldSizesInbox(fieldSizesInbox);

    if(cad == 'A') // not a new one
    {
      short[] fieldSizesInboxL = new short[30]; // plenty
      getFieldSizesInboxL(fieldSizesInboxL);

      if(dispOrEdit == 'D')
      {  
        numFields = screenLayout.layoutToBuffer(buf1, buf2, size1, fieldNames, fieldTypes, fieldSizes, localDefnsDir, defnsDir, "517.htm", 2,
                                         getFieldNamesInbox(), fieldSizesInbox, getFieldNamesInboxL(), fieldSizesInboxL, null, null);
      }
      else numFields = screenLayout.layoutToBuffer(buf1, buf2, size1, fieldNames, fieldTypes, fieldSizes, localDefnsDir, defnsDir, "517a.htm", 1,
                                            getFieldNamesInbox(), fieldSizesInbox, null, null, null, null);

      if(dispOrEdit == 'D')
        prependCode = prepend(con, stmt, rs, unm, sid, uty, men, den, dnm, bnm, generalUtils.stringFromBytes(code, 0L), date, prependCode, req, localDefnsDir, defnsDir, bUpto, bSize, bytesOut);
      else
      {
        prependCode = prependEdit(con, stmt, rs, unm, sid, uty, men, den, dnm, bnm, errStr, "A", generalUtils.stringFromBytes(code, 0L), date, "517a.htm", localDefnsDir, defnsDir, prependCode, req, bUpto,
                                  bSize, bytesOut);
      }

      screenLayout.bufferToBuffer(buf1, buf2, size1, size2, source, numFields, fieldNames, fieldTypes, fieldSizes, dispOrEdit, 'X', data, 0, ddlData,
                           ddlDataUpto[0], javaScriptCode, prependCode);

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

            generalUtils.bytesDPsGivenSeparator(true, '\001', '2', data, 5000, 5); // quantity (origin-1)
            generalUtils.bytesDPsGivenSeparator(true, '\001', '2', data, 5000, 7); // price

            generalUtils.dfsGivenSeparator(true, '\001', data, (short)2, line); // origin-1

            screenLayout.bufferToBuffer(buf1, buf2, size1, size2, source, numFields, fieldNames, fieldTypes, fieldSizes, 'D', 'L', data, 5000, ddlData,
                                 ddlDataUpto[0], javaScriptCallCode, null);
          }
        }
      }
      else // edit
      {
        ddlData = documentUtils.getSalesPersonDDLData(con, stmt, rs, "inbox.SalesPerson", ddlData, ddlDataUpto, ddlDataLen);

        // format notes field
        generalUtils.replaceThreesWithNewlines(generalUtils.dfsAsStrGivenBinary1(true, headData[0], (short)21), b);
        generalUtils.repAlphaGivenSeparator('\001', headData[0], dSize[0], (short)21, b);
      }

      if(dataAlready[0] == '\000') // NOT coming with an err msg
      {
        // convert date
        generalUtils.dfsGivenSeparator(true, '\001', headData[0], (short)1, b);
        generalUtils.convertFromYYYYMMDD(b); 
        generalUtils.repAlphaGivenSeparator('\001', headData[0], dSize[0], (short)1, b);
      }
      
      screenLayout.bufferToBuffer(buf1, buf2, size1, size2, source, numFields, fieldNames, fieldTypes, fieldSizes, dispOrEdit, 'H', headData[0], dSize[0], ddlData, ddlDataUpto[0], null, null);

      append(con, stmt, rs, buf1, buf2, source, size1, size2, unm, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

      screenLayout.bufferToOut(buf1, buf2, source, out);

      rtn = true;
    }
    else // a new layout
    {
      numFields = screenLayout.layoutToBuffer(buf1, buf2, size1, fieldNames, fieldTypes, fieldSizes, localDefnsDir, defnsDir, "517a.htm", 1,
                                       getFieldNamesInbox(), fieldSizesInbox, null, null, null, null);

      if(dataAlready[0] != '\000') // coming with an err msg - passing-in data already entered but the data's fields may (will) not be in the correct order
        sortFields(dataAlready, data, "inbox");
      else
      {
        screenLayout.buildNewDetails(fieldNames, numFields, "inbox", data);

        documentUtils.getNextCode(con, stmt, rs, "inbox", true, code);
        
        generalUtils.repAlphaUsingOnes(data, 5000, "InboxCode", generalUtils.stringFromBytes(code, 0L));
        generalUtils.repAlphaUsingOnes(data, 5000, "Status", "L");
        
        generalUtils.repAlphaUsingOnes(data, 5000, "Date", generalUtils.today(localDefnsDir, defnsDir));
      } 

      prependCode = prependEdit(con, stmt, rs, unm, sid, uty, men, den, dnm, bnm, errStr, "C", generalUtils.stringFromBytes(code, 0L), date, "517a.htm", localDefnsDir, defnsDir, prependCode, req, bUpto, bSize,
                                bytesOut);

      screenLayout.bufferToBuffer(buf1, buf2, size1, size2, source, numFields, fieldNames, fieldTypes, fieldSizes, 'E', 'H', data, 5000, ddlData, ddlDataUpto[0], javaScriptCode, prependCode);

      append(con, stmt, rs, buf1, buf2, source, size1, size2, unm, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

      screenLayout.bufferToOut(buf1, buf2, source, out);
      rtn = true;
    }

    return rtn;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private byte[] javaScriptCall(Connection con, Statement stmt, ResultSet rs, byte[] b, HttpServletRequest req, String date, String unm, String uty, String dnm, String localDefnsDir, String defnsDir,
                                int[] bUpto, int[] bSize, int[] bytesOut) throws Exception
  {
    b[0]= '\000';
    b = scoutln(b, bytesOut, "_.lineFile=inboxl.line\001", bUpto, bSize);
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3135, unm, uty, dnm, localDefnsDir, defnsDir) && serverUtils.passLockCheck(con, stmt, rs, "inbox", date, unm))
      b = scoutln(b, bytesOut, "_.permitted=y\001", bUpto, bSize);
    else b = scoutln(b, bytesOut, "_.permitted=n\001", bUpto, bSize);

    return b;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private byte[] javaScript(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, String code, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                            byte[] b, String localDefnsDir, String defnsDir, int[] bUpto, int[] bSize, int[] bytesOut) throws Exception
  {
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3135, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      b[0]= '\000';
      b = scoutln(b, bytesOut, "<script language=\"JavaScript\">function affect(line){", bUpto, bSize);
      b = scoutln(b, bytesOut, "window.location.replace(\"/central/servlet/InboxLinePage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1="
                               + code + "&p2=\"+line+\"&p3=&p4=\");}</script>", bUpto, bSize);
    }
    else b[0] = '\000';

    return b;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void append(Connection con, Statement stmt, ResultSet rs, byte[][] buf1, byte[][] buf2, char[] source, int[] iSize1, int[] iSize2, String unm, String dnm, String bnm, String localDefnsDir,
                      String defnsDir, int[] bytesOut) throws Exception
  {
    byte[] b = new byte[1000];
    int[] bUpto = new int[1];  bUpto[0] = 0;
    int[] bSize = new int[1];  bSize[0] = 10000;
    b = scoutln(b, bytesOut, "</form>", bUpto, bSize);
    b = scoutln(b, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir), bUpto, bSize);
    screenLayout.appendBytesToBuffer(buf1, buf2, source, iSize1, iSize2, b);
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private byte[] prepend(Connection con, Statement stmt, ResultSet rs, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                       String code, String date, byte[] b, HttpServletRequest req, String localDefnsDir, String defnsDir, int[] bUpto, int[] bSize, int[] bytesOut) throws Exception
  {
    String code2 = code;

    b[0]= '\000';
    b = scoutln(b, bytesOut, "<html><head><title>" + code + "</title>", bUpto, bSize);
    b = scoutln(b, bytesOut, "<script language=\"JavaScript\">\n", bUpto, bSize);

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3133, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      b = scoutln(b, bytesOut, "function fetch(){", bUpto, bSize);
      b = scoutln(b, bytesOut, "window.location.replace(\"/central/servlet/InboxHeaderEdit?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                             + "&p1=" + code2 + "&p2=A&p3=&p4=\");}\n", bUpto, bSize);
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3135, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      b = scoutln(b, bytesOut, "function add(){", bUpto, bSize);
      b = scoutln(b, bytesOut, "window.location.replace(\"/central/servlet/InboxLinePage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den="
                         + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + code2 + "&p2=&p3=&p4=\");}", bUpto, bSize);
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3138, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      b = scoutln(b, bytesOut, "function print(){", bUpto, bSize);
      b = scoutln(b, bytesOut, "window.location.href=\"/central/servlet/_3138?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den
                         + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + code2 + "&p2=&p3=&p4=\";}", bUpto, bSize);
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3140, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      b = scoutln(b, bytesOut, "function quote(){var p1='CREATEBASEDONINBOX:'+'" + code + "';", bUpto, bSize);
      b = scoutln(b, bytesOut, "this.location.href=\"/central/servlet/QuotationPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty +"&men=" + men + "&den=" + den
                         + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}", bUpto, bSize);
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3141, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      b = scoutln(b, bytesOut, "function so(){var p1='CREATEBASEDONINBOX:'+'" + code + "';", bUpto, bSize);
      b = scoutln(b, bytesOut, "this.location.href=\"/central/servlet/SalesOrderPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty +"&men=" + men + "&den=" + den
                         + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}", bUpto, bSize);
    }

    String cssDirectory = directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir);
    String cssDocumentDirectory = directoryUtils.getCssDocumentDirectory(con, stmt, rs, dnm);
    b = scoutln(b, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + cssDirectory + "general.css\">\n", bUpto, bSize);
    b = scoutln(b, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + cssDocumentDirectory + "document.css\">\n", bUpto, bSize);

    b = outputPageFrame(con, stmt, rs, b, req, ' ', ' ', "", "InboxPage", date, unm, sid, uty, men, dnm, dnm, bnm, localDefnsDir, defnsDir, bUpto, bSize, bytesOut);
    b = scoutln(b, bytesOut, "<form>", bUpto, bSize);

    return b;
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private byte[] prependEdit(Connection con, Statement stmt, ResultSet rs, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String errStr, String cad, String code,
                             String date, String layoutFile, String localDefnsDir, String defnsDir, byte[] b, HttpServletRequest req, int[] bUpto, int[] bSize, int[] bytesOut) throws Exception
  {
    String code2 = generalUtils.sanitise(code);

    b[0] = '\000';
    b = scoutln(b, bytesOut, "<html><head><title>" + code + "</title>", bUpto, bSize);
    b = scoutln(b, bytesOut, "<script language=\"JavaScript\">\n", bUpto, bSize);

    byte[] sourceBuf = new byte[5000];
    sourceBuf = htmlBuild.layoutToBuffer(sourceBuf, 5000, localDefnsDir, defnsDir, layoutFile);
    String saveStr = htmlBuild.buildSaveStringDelimited(sourceBuf, "document.forms[0]");

    b = scoutln(b, bytesOut, "function setCode(code){document.forms[0].CompanyCode.value=code;", bUpto, bSize);
    b = scoutln(b, bytesOut, "main.style.visibility='visible';second.style.visibility='hidden';second.style.height='0';submenu.style.visibility='visible';}", bUpto, bSize);
    
    b = scoutln(b, bytesOut, "function save(thisOp){var saveStr=\"\";" + saveStr + "\n", bUpto, bSize);
    b = scoutln(b, bytesOut, "var saveStr2=strip(saveStr);window.location.replace(\"/central/servlet/_3134?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1="
                       + cad + "&p2=" + code2 + "&p3=\"+saveStr2+\"&p4=\"+thisOp)}\n", bUpto, bSize);
    b = scoutln(b, bytesOut, "function strip(saveStr){\n", bUpto, bSize);
    b = scoutln(b, bytesOut, "var saveStr2='';var x;var len=saveStr.length;\n", bUpto, bSize);
    b = scoutln(b, bytesOut, "for(x=0;x<len;++x)\n", bUpto, bSize);
    b = scoutln(b, bytesOut, "if(saveStr.charAt(x)=='#')saveStr2+='%23';\n", bUpto, bSize);
    b = scoutln(b, bytesOut, "else if(saveStr.charAt(x)=='\"')saveStr2+='%22';\n", bUpto, bSize);
    b = scoutln(b, bytesOut, "else if(saveStr.charAt(x)=='&')saveStr2+='%26';\n", bUpto, bSize);
    b = scoutln(b, bytesOut, "else if(saveStr.charAt(x)=='%')saveStr2+='%25';\n", bUpto, bSize);
    b = scoutln(b, bytesOut, "else if(saveStr.charAt(x)==' ')saveStr2+='%20';\n", bUpto, bSize);
    b = scoutln(b, bytesOut, "else if(saveStr.charAt(x)=='?')saveStr2+='%2B';\n", bUpto, bSize);
    b = scoutln(b, bytesOut, "else if(saveStr.charAt(x)=='?')saveStr2+='%3F';\n", bUpto, bSize);
    b = scoutln(b, bytesOut, "else if(escape(saveStr.charAt(x))=='%0A')saveStr2+='\003';\n", bUpto, bSize);
    b = scoutln(b, bytesOut, "else if(escape(saveStr.charAt(x))=='%0D');", bUpto, bSize);
    b = scoutln(b, bytesOut, "else saveStr2+=saveStr.charAt(x);return saveStr2;}\n", bUpto, bSize);

    String cssDirectory = directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir);
    String cssDocumentDirectory = directoryUtils.getCssDocumentDirectory(con, stmt, rs, dnm);
    b = scoutln(b, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + cssDirectory + "general.css\">\n", bUpto, bSize);
    b = scoutln(b, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + cssDocumentDirectory + "document.css\">\n", bUpto, bSize);

    b = outputPageFrame(con, stmt, rs, b, req, ' ', cad.charAt(0), "", "InboxHeaderEdit", date, unm, sid, uty, men, dnm, dnm, bnm, localDefnsDir, defnsDir, bUpto, bSize, bytesOut);

    b = scoutln(b, bytesOut, "<form>", bUpto, bSize);
  
    if(errStr.length() > 0 && ! errStr.equalsIgnoreCase("null"))
      b = scoutln(b, bytesOut, errStr, bUpto, bSize);

    return b;
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  // rtns: 'X' if already exists
  //       ' ' if updated successfully
  //       'N' if no code specified
  //       'F' if not updated
  //       'I' if illegal code
  public char putLine(Connection con, Statement stmt, ResultSet rs, byte[] originalLine, byte[] code, String unm, String dnm, String localDefnsDir, String defnsDir, char cad,
                      byte[] recData, int recDataLen, byte[] rtnLineBytes, int[] bytesOut) throws Exception
  {
    byte[] lineBytes = new byte[20];
    byte[] b = new byte[100];  b[0]= '\000';
    generalUtils.catAsBytes("Line", 0, b, true);

    if(searchDataString(recData, recDataLen, "inboxl", b, lineBytes) == -1)
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

    sortFields(recData, buf, "inboxl"); // sorts the data field (using buf in the process); results are put back into recData
    recDataLen = generalUtils.lengthBytes(recData, 0);
    generalUtils.zeroize(buf, 2000);
        
    String fieldNames = getFieldNamesInboxL();

    byte[] value      = new byte[3000]; // plenty - to cover desc
   
    byte[] fieldName  = new byte[31];
    byte[] itemCode   = new byte[21];
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

      if(searchDataString(recData, recDataLen, "inboxl", fieldName, value) != -1) // entry exists
      {
        if(fieldCount == 2) // stockCode
        {
          if(value[0] == '\000')
            generalUtils.repAlpha(buf, 2000, (short)1, "-");
          else
          {
            generalUtils.bytesToBytes(itemCode, 0, value, 0);
            generalUtils.toUpper(itemCode, 0);
            generalUtils.repAlpha(buf, 2000, (short)2, itemCode);
          }
        }
        else
        {
          generalUtils.repAlpha(buf, 2000, (short)fieldCount, value);
        }
      }

      ++fieldCount;
    }

    generalUtils.repAlpha(buf, 2000, (short)0,  code);
    generalUtils.repAlpha(buf, 2000, (short)1, lineBytes);
   
    // just-in-case no value then need to insert a default
    if((generalUtils.dfsAsStr(buf, (short)4)).length() == 0) // quantity 
      generalUtils.putAlpha(buf, 2000, (short)4, "0");

    if((generalUtils.dfsAsStr(buf, (short)6)).length() == 0) // price 
      generalUtils.putAlpha(buf, 2000, (short)6, "0");

    generalUtils.repAlpha(buf, 2000, (short)10, unm);

    // fetch item details
    generalUtils.strToBytes(fieldName, "Description");
    if(searchDataString(recData, recDataLen, "inboxl", fieldName, value) != -1) // exists
    {
      if(value[0] == '\000') // description fld is blank
      {
        byte[] data = new byte[3000];

        if(inventory.getStockRecGivenCode(con, stmt, rs, itemCode, '\000', data) != -1) // exists
        {
          generalUtils.dfs(data, (short)1, b);           // desc
          generalUtils.repAlpha(buf, 1000, (short)8, b);

          if(miscDefinitions.includeRemark(con, stmt, rs))
          {    
            value[0]= '\000';
          }

          generalUtils.dfs(data, (short)48, b);           // uom
          generalUtils.repAlpha(buf, 2000, (short)7, b);
        }
      }
      else // strip only the first desc line for inboxL rec
      {
        getDescriptionLine(0, value, b);
        generalUtils.repAlpha(buf, 2000, (short)8, b);
      }
    }

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
    stmt.executeUpdate("UPDATE inbox SET SignOn = '" + unm + "', DateLastModified = NULL WHERE InboxCode = '" + generalUtils.stringFromBytes(code, 0L) + "'");
    stmt.close();
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void getAnInboxFieldGivenCode(Connection con, Statement stmt, ResultSet rs, String fieldName, byte[] inboxCode, byte[] value) throws Exception
  {
    if(inboxCode[0] == '\000') // just-in-case
      return;

    generalUtils.toUpper(inboxCode, 0);

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT " + fieldName + " FROM inbox WHERE InboxCode = '" + generalUtils.stringFromBytes(inboxCode, 0L) + "'");
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

    String layoutFile="517b.htm";

    short[] fieldSizesInboxL = new short[30]; // plenty
    getFieldSizesInboxL(fieldSizesInboxL);

    numFields = screenLayout.layoutToBuffer(buf1, buf2, size1, fieldNames, fieldTypes, fieldSizes, localDefnsDir, defnsDir, layoutFile, 1, getFieldNamesInboxL(), fieldSizesInboxL, null, null, null, null);

    byte[] b = new byte[21];

    byte[] data = new byte[2000];
    int[] dUpto = new int[1];  dUpto[0] = 0;
    int[] dSize = new int[1];  dSize[0] = 2000;

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
        {                          //  be in the correct order
          sortFields(dataAlready, data, "inboxl");
        }
        else
        {
          getLine(con, stmt, rs, code, line, '\001', data, dnm, localDefnsDir, defnsDir, dUpto, dSize, bytesOut);

          generalUtils.bytesDPsGivenSeparator(true, '\001', '2', data, 2000, 4); // origin-0, quantity
          generalUtils.bytesDPsGivenSeparator(true, '\001', '2', data, 2000, 6);  // price

          generalUtils.dfsGivenSeparator(true, '\001', data, (short)9, b);
          generalUtils.decodeTime(3, generalUtils.stringFromBytes(b, 0L), ":", b);
          generalUtils.repAlphaGivenSeparator('\001', data, 2000, (short)9, b);
        }
      }

      newOrEdit = 'E';
    }
    else /// new line
    {
      nextLine = getNextLine(con, stmt, rs, code, lineData, dnm, localDefnsDir, defnsDir);
      newOrEdit = 'N';
    }

    ddlData = accountsUtils.getCurrencyNamesDDLData(con, stmt, rs, "inboxl.currency", dnm, ddlData, ddlDataUpto, ddlDataLen, localDefnsDir, defnsDir);

    dataLen = generalUtils.lengthBytes(data, 0);

    byte[] prependCode = new byte[10000];
    int[] bUpto = new int[1];  bUpto[0] = 0;
    int[] bSize = new int[1];  bSize[0] = 10000;

    prependCode = prependEditLine(con, stmt, rs, unm, sid, uty, men, den, dnm, bnm, errStr, "A", generalUtils.stringFromBytes(code, 0L), generalUtils.stringFromBytes(line, 0L), "517b.htm", localDefnsDir, defnsDir,
                                  prependCode, newOrEdit, req, bUpto, bSize, bytesOut);

    screenLayout.bufferToBuffer(buf1, buf2, size1, size2, source, numFields, fieldNames, fieldTypes, fieldSizes, 'E', 'H', data, dataLen, ddlData, ddlDataUpto[0], null, prependCode, false, false, "", nextLine);

    append(con, stmt, rs, buf1, buf2, source, size1, size2, unm, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    screenLayout.bufferToOut(buf1, buf2, source, out);
    
    return true;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private byte[] prependEditLine(Connection con, Statement stmt, ResultSet rs, String unm, String sid, String uty, String men, String den, String dnm,
                               String bnm, String errStr, String cad, String code, String line, String layoutFile, String localDefnsDir,
                               String defnsDir, byte[] b, char newOrEdit, HttpServletRequest req, int[] bUpto, int[] bSize, int[] bytesOut) throws Exception
  {
    String code2 = generalUtils.sanitise(code);

    b[0] = '\000';
    b = scoutln(b, bytesOut, "<html><head><title>" + code + "</title>", bUpto, bSize);
    b = scoutln(b, bytesOut, "<script language=\"JavaScript\">\n", bUpto, bSize);

    byte[] sourceBuf = new byte[5000];
    sourceBuf = htmlBuild.layoutToBuffer(sourceBuf, 5000, localDefnsDir, defnsDir, layoutFile);
    String saveStr = htmlBuild.buildSaveStringDelimited(sourceBuf, "document.forms.doc");
    
    b = scoutln(b, bytesOut, "function setCode(code){document.forms.doc.ItemCode.value=code;main.style.visibility='visible';", bUpto, bSize);
    b = scoutln(b, bytesOut, "second.style.visibility='hidden';third.style.visibility='hidden';second.style.height='0';third.style.height='0';submenu.style.visibility='visible';}", bUpto, bSize);

    b = scoutln(b, bytesOut, "function save(thisOp){var saveStr=\"\";" + saveStr + "\n", bUpto, bSize);
    b = scoutln(b, bytesOut, "var saveStr2=strip(saveStr);window.location.replace(\"/central/servlet/InboxLineUpdate?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm 
                           + "&bnm=" + bnm + "&p1=" + cad + "&p2=" + code2 + "&p3=" + line + "&p4=\"+thisOp+\"&p5=\"+saveStr2);}\n", bUpto, bSize);
    b = scoutln(b, bytesOut, "function strip(saveStr){\n", bUpto, bSize);
    b = scoutln(b, bytesOut, "var saveStr2='';var x;var len=saveStr.length;\n", bUpto, bSize);
    b = scoutln(b, bytesOut, "for(x=0;x<len;++x)\n", bUpto, bSize);
    b = scoutln(b, bytesOut, "if(saveStr.charAt(x)=='#')saveStr2+='%23';\n", bUpto, bSize);
    b = scoutln(b, bytesOut, "else if(saveStr.charAt(x)=='\"')saveStr2+='%22';\n", bUpto, bSize);
    b = scoutln(b, bytesOut, "else if(saveStr.charAt(x)=='&')saveStr2+='%26';\n", bUpto, bSize);
    b = scoutln(b, bytesOut, "else if(saveStr.charAt(x)=='%')saveStr2+='%25';\n", bUpto, bSize);
    b = scoutln(b, bytesOut, "else if(saveStr.charAt(x)==' ')saveStr2+='%20';\n", bUpto, bSize);
    b = scoutln(b, bytesOut, "else if(saveStr.charAt(x)=='?')saveStr2+='%2B';\n", bUpto, bSize);
    b = scoutln(b, bytesOut, "else if(saveStr.charAt(x)=='?')saveStr2+='%3F';\n", bUpto, bSize);
    b = scoutln(b, bytesOut, "else if(escape(saveStr.charAt(x))=='%0A')saveStr2+='\003';\n", bUpto, bSize);
    b = scoutln(b, bytesOut, "else if(escape(saveStr.charAt(x))=='%0D');", bUpto, bSize);
    b = scoutln(b, bytesOut, "else saveStr2+=saveStr.charAt(x);return saveStr2;}\n", bUpto, bSize);

    String cssDirectory = directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir);
    String cssDocumentDirectory = directoryUtils.getCssDocumentDirectory(con, stmt, rs, dnm);
    b = scoutln(b, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + cssDirectory + "general.css\">\n", bUpto, bSize);
    b = scoutln(b, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + cssDocumentDirectory + "document.css\">\n", bUpto, bSize);

    b = outputPageFrame(con, stmt, rs, b, req, newOrEdit, ' ', "", "InboxLinePage", "", unm, sid, uty, men, dnm, dnm, bnm, localDefnsDir, defnsDir, bUpto, bSize, bytesOut);

    b = scoutln(b, bytesOut, "<input type=hidden name=\"thisline\" VALUE=\"" + line + "\">", bUpto, bSize);

    if(errStr.length() > 0 && ! errStr.equalsIgnoreCase("null"))
      b = scoutln(b, bytesOut, errStr, bUpto, bSize);

    b = scoutln(b, bytesOut, "<form name=doc>", bUpto, bSize);

    return b;
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
        thisOne = generalUtils.strToInt(generalUtils.dfsAsStrGivenBinary1(true, data, (short)2)); // line entry + 1 (for leading count)
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
    generalUtils.catAsBytes("InboxCode", 0, str, true);

    if(searchDataString(recData, recDataLen, "inbox", str, code) == -1)
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
 
    String fieldNamesInbox = getFieldNamesInbox();
    
    // chk if any of the compname or addr flds have values already
    int x, len;
    boolean atleastOneHasValue=false;
    for(x=8;x<15;++x)
    {
      getFieldName(fieldNamesInbox, x, fldName);
      if(searchDataString(recData, recDataLen, "inbox", fldName, value) != -1) // entry exists
      {
        if(value[0] != '\000')
          atleastOneHasValue = true;
      }
    }

    boolean atleastOneHasValue2=false;
    for(x=14;x<21;++x)
    {
      getFieldName(fieldNamesInbox, x, fldName);
      if(searchDataString(recData, recDataLen, "inbox", fldName, value) != -1) // entry exists
      {
        if(value[0] != '\000')
          atleastOneHasValue2 = true;
      }
    }

    len = fieldNamesInbox.length();
    int y, fieldCount=0;
    x=0;
    while(x < len)
    {
      y=0;
      while(x < len && fieldNamesInbox.charAt(x) != ',')
        fldName[y++] = (byte)fieldNamesInbox.charAt(x++);
      fldName[y] = '\000';
      ++x;
      
      while(x < len && fieldNamesInbox.charAt(x) == ' ')
        ++x;

      if(searchDataString(recData, recDataLen, "inbox", fldName, value) != -1) // entry exists
      {  
        if(fieldCount == 0) // code
          ;
        else
        if(fieldCount == 1) // date
        {
          generalUtils.putAlpha(buf, 2000, (short)1, generalUtils.convertDateToSQLFormat(generalUtils.stringFromBytes(value, 0L)));  
        }
        else
        if(fieldCount == 23)
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
                generalUtils.repAlpha(buf, 2000, (short)2, b);
                generalUtils.dfs(data, (short)5, b);           // shipaddr1
                generalUtils.repAlpha(buf, 2000, (short)8, b);
                generalUtils.dfs(data, (short)6, b);           // shipaddr2
                generalUtils.repAlpha(buf, 2000, (short)9, b);
                generalUtils.dfs(data, (short)7, b);           // shipaddr3
                generalUtils.repAlpha(buf, 2000, (short)10, b);
                generalUtils.dfs(data, (short)8, b);           // shipaddr4
                generalUtils.repAlpha(buf, 2000, (short)11, b);
                generalUtils.dfs(data, (short)9, b);           // shipaddr5
                generalUtils.repAlpha(buf, 2000, (short)12, b);
                generalUtils.dfs(data, (short)12, b);           // shipcountry
                generalUtils.repAlpha(buf, 2000, (short)13, b);
              }

              if(! atleastOneHasValue2)
              {
                generalUtils.dfs(data, (short)40, b);          // billaddr1
                generalUtils.repAlpha(buf, 2000, (short)14, b);
                generalUtils.dfs(data, (short)41, b);          // billpaddr2
                generalUtils.repAlpha(buf, 2000, (short)15, b);
                generalUtils.dfs(data, (short)42, b);          // billaddr3
                generalUtils.repAlpha(buf, 2000, (short)16, b);
                generalUtils.dfs(data, (short)43, b);          // billaddr4
                generalUtils.repAlpha(buf, 2000, (short)17, b);
                generalUtils.dfs(data, (short)44, b);          // billaddr5
                generalUtils.repAlpha(buf, 2000, (short)18, b);
                generalUtils.dfs(data, (short)12, b);          // billcountry
                generalUtils.repAlpha(buf, 2000, (short)19, b);
              }
            }
          }
          
          generalUtils.putAlpha(buf, 2000, (short)23, value);
        }
        else
        if(fieldCount == 26) // signon
        {
          if(value[0] == '\000')
            generalUtils.putAlpha(buf, 2000, (short)26, unm);
          else generalUtils.putAlpha(buf, 2000, (short)26, value);
        }
        else
        if(fieldCount == 21) // notes
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
          generalUtils.putAlpha(buf, 2000, (short)21, b2);
        }
        else generalUtils.putAlpha(buf, 2000, (short)fieldCount, value);
      }
      
      ++fieldCount;
    }

    generalUtils.repAlpha(buf, 2000, (short)26, unm);

    return putRecHeadGivenCode(con, stmt, rs, code, newOrEdit, buf, dnm, localDefnsDir, defnsDir);
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public int getRecGivenCode(Connection con, Statement stmt, ResultSet rs, byte[] code, char separator, byte[][] data, String dnm, String localDefnsDir, String defnsDir, int[] dUpto, int[] dSize,
                             int[] bytesOut) throws Exception
  {
    if(code[0] == '\000') // just-in-case
      return -1;

    generalUtils.toUpper(code, 0);

    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT * FROM inbox WHERE InboxCode = '" + generalUtils.stringFromBytes(code, 0L) + "'");
    if(! rs.next())
    {
      rs.close();
      stmt.close();
      return -1;
    }      
    
    ResultSetMetaData rsmd = rs.getMetaData();
    
    String fieldTypes = getFieldTypesInbox();
     
    if(separator == '\000')
    {
      int x, y=0;
      String s = getValue(1, ' ', rs, rsmd);
      int len = s.length();
      for(x=0;x<len;++x)
        data[0][y++] = (byte)s.charAt(x);
      data[0][y++] = '\000';
      
      for(int z=2;z<=fieldTypes.length();++z)
      {
        s = getValue(z, fieldTypes.charAt(z-1), rs, rsmd);
        len = s.length();
        for(x=0;x<len;++x)
          data[0][y++] = (byte)s.charAt(x);
        data[0][y++] = '\000';
      }
    }
    else // separator == \001
    {    
      String fieldNames = getFieldNamesInbox();
      String thisFieldName;
      char thisFieldType;
      data[0][0]= '\000';

      int x=0, count=0, len = fieldNames.length();
      while(x < len)
      {
        thisFieldName="";
        while(x < len && fieldNames.charAt(x) != ',')
          thisFieldName += fieldNames.charAt(x++);
        thisFieldType = fieldTypes.charAt(count++);
        data[0] = scoutln(data[0], bytesOut, "inbox." + thisFieldName + "=" + getValue(count, thisFieldType, rs, rsmd) + "\001", dUpto, dSize);
      
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
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
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
      String fieldTypes = getFieldTypesInbox();
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
            while(data[x] != '\000' && data[x] != '"')
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
      
        q = "INSERT INTO inbox (" + getFieldNamesInbox() + ") VALUES (" + opStr + ")";
      }
      else // newOrEdit == 'E'
      {
        String thisFieldName;
        char thisFieldType;

        // change: abc\0def\0... format into: CompanyCode='abc',Name='def',...

        String fieldNames = getFieldNamesInbox();
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

        q = "UPDATE inbox SET " + opStr + " WHERE InboxCode = '" + generalUtils.stringFromBytes(code, 0L) + "'";
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

  // -------------------------------------------------------------------------------------------------------------------------------------------------
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
      String fieldTypes = getFieldTypesInboxL();
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
      
        q = "INSERT INTO inboxl (" + getFieldNamesInboxL() + ") VALUES (" + opStr + ")";
      }
      else // newOrEdit == 'E'
      {
        String thisFieldName;
        char thisFieldType;

        // change: abc\0def\0... format into: CompanyCode='abc',Name='def',...

        String fieldNames = getFieldNamesInboxL();
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

        q = "UPDATE inboxl SET " + opStr + " WHERE InboxCode = '" + generalUtils.stringFromBytes(code, 0L) + "' AND Line = '" 
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

  // -------------------------------------------------------------------------------------------------------------------------------------------------
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
      int[] dUpto = new int[1];  dUpto[0] = 0;
      int[] dSize = new int[1];  dSize[0] = 2000;

      if(getLine(con, stmt, rs, code, line, '\000', data, dnm, localDefnsDir, defnsDir, dUpto, dSize, bytesOut) == -1) // just-in-case
        return false;

      stmt = con.createStatement();
    
      stmt.executeUpdate("DELETE FROM inboxl WHERE InboxCode = '" + generalUtils.stringFromBytes(code, 0L) + "' AND Line = '" + generalUtils.stringFromBytes(line, 0L) + "'");

      stmt.close();
    }
    catch(Exception e)
    {
      try
      {
        System.out.println(e);
        if(stmt != null) stmt.close();
      }
      catch(Exception e2) { }
      return false;
    }
    
    return true;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public byte[] getLines(Connection con, Statement stmt, ResultSet rs, byte[] code, char separator, byte[] linesData, int[] listLen, int[] linesCount, String dnm,
                         String localDefnsDir, String defnsDir) throws Exception
  {
    if(code[0] == '\000') // just-in-case
      return linesData;

    linesCount[0] = 0;

    generalUtils.toUpper(code, 0);

    byte[] data = new byte[5000];
    String s;
    int x, len;
    String fieldTypes = getFieldTypesInboxL();
    String fieldNames = getFieldNamesInboxL();    
    byte[] newItem = new byte[1000];
    String thisFieldName;
    char thisFieldType;
    int count;
    int lenFieldNames = fieldNames.length();
    int lenFieldTypes = fieldTypes.length();
    data[0] = '\000';
        
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT * FROM inboxl WHERE InboxCode = '" + generalUtils.stringFromBytes(code, 0L) + "'");
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
          s += "inboxl." + thisFieldName + "=" + getValue(count, thisFieldType, rs, rsmd) + "\001";
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
    
    rs.close();
    stmt.close();
    
    return linesData; 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // if separator is '\001' then buf is: company.code=acme\001company.name=acme ltd\001
  // else if separator is '\000' (eg) then buf is acme\0acme ltd\0
  private int getLine(Connection con, Statement stmt, ResultSet rs, byte[] code, byte[] line, char separator, byte[] data, String dnm, String localDefnsDir, String defnsDir, int[] dUpto, int[] dSize, int[] bytesOut) throws Exception
  {
    if(code[0] == '\000') // just-in-case
      return -1;

    if(line[0] == '\000')
      return -1;

    generalUtils.toUpper(code, 0);

    int upto;
    String fieldTypes = getFieldTypesInboxL();
    String fieldNames = getFieldNamesInboxL();
    String thisFieldName;
    char thisFieldType;
    int x, count, len = fieldNames.length(), numFields = fieldTypes.length();
    data[0] = '\000';
    
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT * FROM inboxl WHERE InboxCode = '" + generalUtils.stringFromBytes(code, 0L) + "' AND Line = '"
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
          data = scoutln(data, bytesOut, "inboxl." + thisFieldName + "=" + getValue(count, thisFieldType, rs, rsmd) + "\001", dUpto, dSize);
          ++x;
          while(x < len && fieldNames.charAt(x) == ' ')
            ++x;
        }
      }
    }
    
    rs.close();
    stmt.close();
  
    return 0;
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private byte[] prependDisplayOnly(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, String code, String date, String unm, String sid, String uty,
                                  String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, byte[] b, int[] bUpto, int[] bSize, int[] bytesOut)
                                  throws Exception
  {
    b[0]= '\000';

    b = scoutln(b, bytesOut, "<html><head><title>" + code + "</title>", bUpto, bSize);

    String cssDirectory = directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir);
    String cssDocumentDirectory = directoryUtils.getCssDocumentDirectory(con, stmt, rs, dnm);
    b = scoutln(b, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + cssDirectory + "general.css\">\n", bUpto, bSize);
    b = scoutln(b, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + cssDocumentDirectory + "document.css\">\n", bUpto, bSize);

    b = outputPageFrame(con, stmt, rs, b, req, ' ',  ' ', "", "ExternalUserCartInbox", date, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bUpto, bSize, bytesOut);

    return b;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // picks-up the list of fieldnames for the inbox or inboxL file, and fetches the data value for that field; and rebuilds the data 
  // string
  private void sortFields(byte[] ipBuf, byte[] opBuf, String which) throws Exception
  {
    String thisFieldName;
    String fieldNames;
    int x=0, y;
    byte[] thisFieldNameB = new byte[50];
    byte[] value          = new byte[1000];
    
    if(which.equals("inbox"))
      fieldNames = getFieldNamesInbox();
    else // if(which.equals("inboxl"))
      fieldNames = getFieldNamesInboxL();
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

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // uses layout 517d.htm
  public boolean getRecToHTMLDisplayOnly(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, byte[] code,
                                         String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir,
                                         String defnsDir, int[] bytesOut) throws Exception
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

    byte[][] data = new byte[1][3000];
    int[] dUpto = new int[1];  dUpto[0] = 0;
    int[] dSize = new int[1];  dSize[0] = 3000;

    byte[] b    = new byte[300];

    byte[] prependCode = new byte[10000];
    int[] bUpto = new int[1];  bUpto[0] = 0;
    int[] bSize = new int[1];  bSize[0] = 10000;

    byte[] javaScriptCode = new byte[1000];
    int[] jsUpto = new int[1];  jsUpto[0] = 0;
    int[] jsSize = new int[1];  jsSize[0] = 1000;

    javaScriptCode = javaScript(con, stmt, rs, req, generalUtils.stringFromBytes(code, 0L), unm, sid, uty, men, den, dnm, bnm, javaScriptCode, localDefnsDir, defnsDir, jsUpto, jsSize, bytesOut);

    byte[] ddlData = new byte[1];

    int[] ddlDataUpto = new int[1];
    ddlDataUpto[0] = 0;

    if(getRecGivenCode(con, stmt, rs, code, '\001', data, dnm, localDefnsDir, defnsDir, dUpto, dSize, bytesOut) == -1) // just-in-case
    {
      messagePage.msgScreen(false, out, req, 6, unm, sid, uty, men, den, dnm, bnm, "Inbox", imagesDir, localDefnsDir, defnsDir, bytesOut);
      return false;
    }

    short[] fieldSizesInbox = new short[60]; // plenty
    getFieldSizesInbox(fieldSizesInbox);
    short[] fieldSizesInboxL = new short[30]; // plenty
    getFieldSizesInboxL(fieldSizesInboxL);

    numFields = screenLayout.layoutToBuffer(buf1, buf2, size1, fieldNames, fieldTypes, fieldSizes, localDefnsDir, defnsDir, "517d.htm", 2,
                                     getFieldNamesInbox(), fieldSizesInbox, getFieldNamesInboxL(), fieldSizesInboxL, null, null);

    int recDataLen = generalUtils.lengthBytes(data[0], 0);

    String fieldNamesInbox = getFieldNamesInbox();
    byte[] value     = new byte[1000]; // plenty - to cover notes
    byte[] fieldName = new byte[50];
    int x=0, y, fieldCount=0;
    int len = fieldNamesInbox.length();
    while(x < len)
    {
      y=0;
      while(x < len && fieldNamesInbox.charAt(x) != ',')
        fieldName[y++] = (byte)fieldNamesInbox.charAt(x++);
      fieldName[y] = '\000';
      ++x;
      
      while(x < len && fieldNamesInbox.charAt(x) == ' ')
        ++x;

      if(searchDataString(data[0], recDataLen, "inbox", fieldName, value) != -1) // entry exists
      {
        generalUtils.repAlphaGivenSeparator('\001', data[0], dSize[0], (short)fieldCount, value);
      }
      
      ++fieldCount;
    }

    generalUtils.dfsGivenSeparator(true, '\001', data[0], (short)25, b);
    generalUtils.decodeTime(3, generalUtils.stringFromBytes(b, 0L), ":", b);
    generalUtils.repAlphaGivenSeparator('\001', data[0], 3000, (short)25, b);

    byte[] date = new byte[20]; // lock
    generalUtils.dfsGivenSeparator(true, '\001', data[0], (short)1, date);

    byte[] javaScriptCallCode = new byte[1000]; // lock
    jsUpto[0] = 0;
    jsSize[0] = 1000;

    javaScriptCallCode = javaScriptCall(con, stmt, rs, javaScriptCallCode, req, generalUtils.stringFromBytes(date, 0L), unm, uty, dnm, localDefnsDir, defnsDir, jsUpto, jsSize, bytesOut);

    prependCode = prependDisplayOnly(con, stmt, rs, req, generalUtils.stringFromBytes(code, 0L), generalUtils.stringFromBytes(date, 0L), unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, prependCode, bUpto,
                                     bSize, bytesOut);

    screenLayout.bufferToBuffer(buf1, buf2, size1, size2, source, numFields, fieldNames, fieldTypes, fieldSizes, 'D', 'H', data[0], dSize[0], ddlData, ddlDataUpto[0], javaScriptCode, prependCode);

    byte[] linesData = new byte[2000];
    int[] listLen = new int[1];  listLen[0] = 2000;
    int[] linesCount = new int[1];
    linesData = getLines(con, stmt, rs, code, '\001', linesData, listLen, linesCount, dnm, localDefnsDir, defnsDir);
    for(x=0;x<linesCount[0];++x)
    {
      if(generalUtils.getListEntryByNum(x, linesData, data[0])) // just-in-case
      {
        generalUtils.replaceTwosWithOnes(data[0]);
        generalUtils.bytesDPsGivenSeparator(true, '\001', '2', data[0], 2000, 5); // origin-1
        generalUtils.bytesDPsGivenSeparator(true, '\001', '2', data[0], 2000, 7);
        
        screenLayout.bufferToBuffer(buf1, buf2, size1, size2, source, numFields, fieldNames, fieldTypes, fieldSizes, 'D', 'L', data[0], 2000, ddlData, ddlDataUpto[0], javaScriptCallCode, null);
      }
    }

    append(con, stmt, rs, buf1, buf2, source, size1, size2, unm, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    screenLayout.bufferToOut(buf1, buf2, source, out);

    return true;
  }
    
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private byte[] outputPageFrame(Connection con, Statement stmt, ResultSet rs, byte[] b, HttpServletRequest req, char newOrEdit, char cad, String bodyStr,
                                 String callingServlet, String date, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                                 String localDefnsDir, String defnsDir, int[] bUpto, int[] bSize, int[] bytesOut) throws Exception
  {
    int[] hmenuCount = new int[1];

    String subMenuText = dashboardUtils.buildSubMenuText(con, stmt, rs, req, "3131", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount);

    subMenuText += "<table id='title' width=100%><tr><td colspan=2 nowrap><p>Inbox Record" + directoryUtils.buildHelp(3131) + "</td></tr></table>";

    subMenuText += buildSubMenuText(con, stmt, rs, req, cad, newOrEdit, callingServlet, date, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount);

    b = scoutln(b, bytesOut, authenticationUtils.buildScreen(con, stmt, rs, req, true, unm, sid, uty, men, den, dnm, bnm, callingServlet, subMenuText, hmenuCount[0], "", "", localDefnsDir, defnsDir), bUpto, bSize);
    
    b = scoutln(b, bytesOut, "<table border=0><tr><td valign=top><div id='second' style=\"{margin-top:0px;}\"></div></td>", bUpto, bSize);
    b = scoutln(b, bytesOut, "<td valign=top><div id='third' style=\"{margin-top:0px;margin-left:0px;}\"></div></td></tr></table>", bUpto, bSize);

    if(callingServlet.equals("ExternalUserCartInbox"))
      b = scoutln(b, bytesOut, "<p>Your request (shown below) has been submitted. Thank You.</p><br>", bUpto, bSize);

    return b;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private String buildSubMenuText(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, char cad, char newOrEdit, String callingServlet,
                                  String date, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir,
                                  int[] hmenuCount) throws Exception
  {
    String s = "<div id='submenu'>";

    ++hmenuCount[0];

    if(callingServlet.equals("InboxPage"))      
      s += drawOptions3131(con, stmt, rs, req, hmenuCount, date, unm, uty, dnm, localDefnsDir, defnsDir);
    else
    if(callingServlet.equals("InboxHeaderEdit"))      
      s += drawOptions3133(hmenuCount, cad, unm, sid, uty, men, den, dnm, bnm);
    else 
    if(callingServlet.equals("InboxLinePage"))      
      s += drawOptions3135(hmenuCount, newOrEdit, unm, sid, uty, men, den, dnm, bnm);

    s += "</div>";
    
    --hmenuCount[0];
    
    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private String drawOptions3131(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, int[] hmenuCount, String date, String unm, String uty, String dnm,
                                 String localDefnsDir, String defnsDir) throws Exception
  {      
    String s = "";
    
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3133, unm, uty, dnm, localDefnsDir, defnsDir) && serverUtils.passLockCheck(con, stmt, rs, "inbox", date, unm))
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\"><a href=\"javascript:fetch()\">Edit Details</a></dt></dl>\n";

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3135, unm, uty, dnm, localDefnsDir, defnsDir) && serverUtils.passLockCheck(con, stmt, rs, "inbox", date, unm))
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\"><a href=\"javascript:add()\">Add New Line</a></dt></dl>\n";

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3138, unm, uty, dnm, localDefnsDir, defnsDir))
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\"><a href=\"javascript:print()\">Print</a></dt></dl>\n";
 
    boolean InboxToQuote = authenticationUtils.verifyAccess(con, stmt, rs, req, 3140, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean InboxToSalesOrder = authenticationUtils.verifyAccess(con, stmt, rs, req, 3141, unm, uty, dnm, localDefnsDir, defnsDir);
    if(InboxToQuote || InboxToSalesOrder)
    {
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0] + "');\">Convert</dt><dd id='hmenu" + hmenuCount[0]++ + "'><ul>";
      if(InboxToQuote)
        s += "<li><a href=\"javascript:quote()\">Quotation</a></li>\n";
    
      s += "</ul></dd></dl>";
    }
    
    return s;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private String drawOptions3133(int[] hmenuCount, char cad, String unm, String sid, String uty, String men, String mainDNM, String dnm,
                                 String bnm) throws Exception
  {      
    String s = "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\"><a href=\"javascript:save(' ')\">Save Changes</a></dt></dl>\n";

    if(cad != 'C')
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\"><a href=\"javascript:save('X')\">Cancel Changes</a></dt></dl>\n";

    s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\"><script language='Javascript'>";
    s += "var alreadyOnce=false;function select(){main.style.visibility='hidden';second.style.visibility='visible';"
                       + "submenu.style.visibility='hidden';if(!alreadyOnce){select4230('A');alreadyOnce=true;}}";

    s += "var req2;";
    s += "function initRequest2(url)";
    s += "{if(window.XMLHttpRequest){req2=new XMLHttpRequest();}else ";
    s += "if(window.ActiveXObject){req2=new ActiveXObject(\"Microsoft.XMLHTTP\");}}";

    s += "function select4230(searchChar)";
    s += "{var url = \"http://" + men + "/central/servlet/CustomerSelect?unm=\" + escape('" + unm
                       + "') + \"&sid=\" + escape('" + sid + "') + \"&uty=\" + escape('" + uty
                       + "') + \"&men=\" + escape('" + men + "') + \"&den=\" + escape('" + mainDNM + "') + \"&bnm=\" + escape('"
                       + bnm + "') + \"&p1=\"+searchChar+\"&dnm=\" + escape('" + dnm + "');";
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

  // -------------------------------------------------------------------------------------------------------------------------------
  private String drawOptions3135(int[] hmenuCount, char newOrEdit, String unm, String sid, String uty,
                                 String men, String mainDNM, String dnm, String bnm) throws Exception
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
    s += "{if(req2.status==200)";
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

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private byte[] scoutln(byte[] b, int bytesOut[], String str, int[] bUpto, int[] bSize) throws Exception
  {
    int len = str.length();

    if((len + bUpto[0]) >= bSize[0])
    {
      int z;
      byte[] tmp = new byte[bSize[0]];
      for(z=0;z<bUpto[0];++z)
        tmp[z] = b[z];
      b = new byte[bSize[0] + len + 1000];
      for(z=0;z<bUpto[0];++z)
        b[z] = tmp[z];
      bSize[0] += (len + 1000);
    }

    generalUtils.catAsBytes(str, 0, b, false);
    bUpto[0] += len;

    bytesOut[0] += (str.length() + 2);

    return b;
  }

}
