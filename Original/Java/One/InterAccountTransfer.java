// =======================================================================================================================================================================================================
// System: ZaraStar DocumentEngine: IAT Record Access
// Module: interAccountTransfer.java
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

public class InterAccountTransfer
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
  DashboardUtils dashboardUtils = new DashboardUtils();

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getTableCreateStringIAT() throws Exception
  {  
    return "iat ( IATCode char(20) not null, Date date not null,         TransactionDate date not null, "
               + "AccountCr char(20),        BankCr char(40),            AmountCr decimal(15,2), "
               + "ChargesCr decimal(15,2),   CurrencyCr char(3),         RateCr decimal(15,10), "
               + "AccountDr char(20),        BankDr char(40),            AmountDr decimal(15,2), "
               + "ChargesDr decimal(15,2),   DateLastModified timestamp, RateCrCharges decimal(15,10), "
               + "SignOn char(20),           Remark char(100),           RateDrCharges decimal(15,10), "
               + "Status char(1),            CurrencyDr char(3),         RateDr decimal(15,10), "
               + "ReconciledCr char(1),      ReconciledDr char(1), "
               + "unique(IATCode))";
  } 

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStringsIAT(String[] s) throws Exception
  {
    s[0] = "iatDateInx on iat (Date)";
    s[1] = "iatTransactionDateInx on iat (TransactionDate)";
    s[2] = "iatAccountCrInx on iat (AccountCr)";
    s[3] = "iatAccountDrInx on iat (AccountDr)";
    
    return 4;
  }
      
  // ------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNamesIAT() throws Exception
  {  
    return "IATCode, Date, TransactionDate, AccountCr, BankCr, AmountCr, ChargesCr, CurrencyCr, RateCr, AccountDr, BankDr, "
         + "AmountDr, ChargesDr, DateLastModified, RateCrCharges, SignOn, Remark, RateDrCharges, Status, CurrencyDr, RateDr, ReconciledCr, "
         + "ReconciledDr";
  }

  // ------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypesIAT() throws Exception
  {
    return "CDDCCFFCFCCFFSFCCFCCFCC";
  }

  // ------------------------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizesIAT(short[] sizes) throws Exception
  {
    sizes[0]  = 20;  sizes[1]  = 0;   sizes[2]  = 0;    sizes[3]  = 20;  sizes[4] = 40;  sizes[5] = 0;   sizes[6] = 0;
    sizes[7]  = 3;   sizes[8]  = 0;   sizes[9]  = 20;   sizes[10] = 40;  sizes[11] = 0;  sizes[12] = 0;  sizes[13] = -1;
    sizes[14] = 0;  sizes[15] = 20;  sizes[16] = 100;  sizes[17] = 0;   sizes[18] = 1;  sizes[19] = 3;  sizes[20] = 0;
    sizes[21] = 1;   sizes[22] = 1; 
  }

  // ------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldStylesIAT() throws Exception
  {
    return "MMMOOMOMOOOMOOOOOOOMOOO";
  }

  // ------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean getRecToHTML(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, char dispOrEdit, byte[] code,
                              String unm, String sid, String uty, String men, String den, String dnm, String bnm, String workingDir, String localDefnsDir,
                              String defnsDir, char cad, String errStr, byte[] dataAlready, HttpServletRequest req, int[] bytesOut) throws Exception
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

    boolean rtn;

    byte[] ddlData = new byte[1500];
    int[] ddlDataLen  = new int[1];

    ddlDataLen[0] = 1500;
    int[] ddlDataUpto = new int[1];
    ddlDataUpto[0] = 0;

    if(cad == 'A' && code[0] == '\000')
    {
      messagePage.msgScreen(false, out, req, 6, unm, sid, uty, men, den, dnm, bnm, "InterAccountTransfer", imagesDir, localDefnsDir, defnsDir, bytesOut);
      return true;
    }

    if(dataAlready[0] != '\000') // coming with an err msg - passing-in data already entered but the data's fields may (will) not be in the correct order
    {
      dispOrEdit = 'E';
      sortFields(dataAlready, headData, "iat");
    }
    else // get header data
    {
      if(cad == 'A')
      {
        if(getRecGivenCode(con, stmt, rs, code, '\001', headData, dnm, localDefnsDir, defnsDir, bytesOut) == -1)
        {
          messagePage.msgScreen(false, out, req, 6, unm, sid, uty, men, den, dnm, bnm, "InterAccountTransfer", imagesDir, localDefnsDir, defnsDir, bytesOut);
          return true;
        }
      }
    }
   
    
    String date;
    byte[] dateB = new byte[20]; // lock
    if(dataAlready[0] != '\000')
    {
      generalUtils.dfsGivenSeparator(true, '\001', headData, (short)2, dateB);
      date = generalUtils.convertDateToSQLFormat(generalUtils.stringFromBytes(dateB, 0L));
    }
    else
    if(cad == 'A')
    {
      generalUtils.dfsGivenSeparator(true, '\001', headData, (short)2, dateB);
      date = generalUtils.stringFromBytes(dateB, 0L);
    }
    else date = generalUtils.todaySQLFormat(localDefnsDir, defnsDir);     
    
    short[] fieldSizesIAT = new short[70]; // plenty
    getFieldSizesIAT(fieldSizesIAT);

    if(cad == 'A') // not a new one
    {
      if(dispOrEdit == 'D')
        numFields = screenLayout.layoutToBuffer(buf1, buf2, size1, fieldNames, fieldTypes, fieldSizes, localDefnsDir, defnsDir, "110.htm", 1, getFieldNamesIAT(), fieldSizesIAT, null, null, null, null);
      else numFields = screenLayout.layoutToBuffer(buf1, buf2, size1, fieldNames, fieldTypes, fieldSizes, localDefnsDir, defnsDir, "110a.htm", 1, getFieldNamesIAT(), fieldSizesIAT, null, null, null, null);
              
      if(dispOrEdit == 'D')
        prepend(con, stmt, rs, unm, sid, uty, men, den, dnm, bnm, generalUtils.stringFromBytes(code, 0L), date, prependCode, req, localDefnsDir, defnsDir, bytesOut);
      else prependEdit(con, stmt, rs, unm, sid, uty, men, den, dnm, bnm, errStr, "A", generalUtils.stringFromBytes(code, 0L), date, "110a.htm", localDefnsDir, defnsDir, prependCode, req, bytesOut);

      screenLayout.bufferToBuffer(buf1, buf2, size1, size2, source, numFields, fieldNames, fieldTypes, fieldSizes, dispOrEdit, 'X', data, 0, ddlData, ddlDataUpto[0], null, prependCode);

      if(dispOrEdit == 'D') // display head
      {
      ;
      }
      else // edit
      {
        ddlData = accountsUtils.getGLAccountsDDLData("iat.AccountCr", unm, dnm, ddlData, ddlDataUpto, ddlDataLen, workingDir);
        ddlData = accountsUtils.getGLAccountsDDLData("iat.AccountDr", unm, dnm, ddlData, ddlDataUpto, ddlDataLen, workingDir);
        ddlData = accountsUtils.getCurrencyNamesDDLData(con, stmt, rs, "iat.CurrencyCr",  dnm, ddlData, ddlDataUpto, ddlDataLen, localDefnsDir, defnsDir);
        ddlData = accountsUtils.getCurrencyNamesDDLData(con, stmt, rs, "iat.CurrencyDr",  dnm, ddlData, ddlDataUpto, ddlDataLen, localDefnsDir, defnsDir);
    
        generalUtils.dfsGivenSeparator(true, '\001', headData, (short)3, b); // accountCr
        if(b[0] != '\000')
        {
          generalUtils.repAlphaGivenSeparator('\001', headData, 2000, (short)3, accountsUtils.getAccountPlusDescriptionGivenAccCode(b, unm, dnm, workingDir, localDefnsDir, defnsDir));
        }

        generalUtils.dfsGivenSeparator(true, '\001', headData, (short)9, b); // accountDr
        if(b[0] != '\000')
        {
          generalUtils.repAlphaGivenSeparator('\001', headData, 2000, (short)9, accountsUtils.getAccountPlusDescriptionGivenAccCode(b, unm, dnm, workingDir, localDefnsDir, defnsDir));
        }
      }

      if(dataAlready[0] == '\000') // NOT coming with an err msg
      {
        generalUtils.bytesDPsGivenSeparator(true, '\001', '2', headData, 5000, 8);  // RateCr
        generalUtils.bytesDPsGivenSeparator(true, '\001', '2', headData, 5000, 20); // RateDr
        generalUtils.bytesDPsGivenSeparator(true, '\001', '2', headData, 5000, 14); // RateCrCharges
        generalUtils.bytesDPsGivenSeparator(true, '\001', '2', headData, 5000, 17); // RateDrCharges

        // convert date
        generalUtils.dfsGivenSeparator(true, '\001', headData, (short)1, b);
        generalUtils.convertFromYYYYMMDD(b); 
        generalUtils.repAlphaGivenSeparator('\001', headData, 5000, (short)1, b);

        generalUtils.dfsGivenSeparator(true, '\001', headData, (short)2, b); // transactionDate
        generalUtils.convertFromYYYYMMDD(b); 
        generalUtils.repAlphaGivenSeparator('\001', headData, 5000, (short)2, b);
      }
      
      generalUtils.dfsGivenSeparator(true, '\001', headData, (short)8, b);
      double rateCr = generalUtils.doubleFromBytesCharFormat(b, 0);
      generalUtils.dfsGivenSeparator(true, '\001', headData, (short)20, b);
      double rateDr = generalUtils.doubleFromBytesCharFormat(b, 0);
      generalUtils.dfsGivenSeparator(true, '\001', headData, (short)5, b);
      double amountCr = generalUtils.doubleDPs(generalUtils.doubleFromBytesCharFormat(b, 0), '2');
      generalUtils.dfsGivenSeparator(true, '\001', headData, (short)11, b);
      double amountDr = generalUtils.doubleDPs(generalUtils.doubleFromBytesCharFormat(b, 0), '2');
      
      screenLayout.bufferToBuffer(buf1, buf2, size1, size2, source, numFields, fieldNames, fieldTypes, fieldSizes, dispOrEdit, 'H', headData, 5000, ddlData, ddlDataUpto[0], null, null);

      append(con, stmt, rs, buf1, buf2, source, size1, size2, unm, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

      screenLayout.bufferToOut(buf1, buf2, source, out);

      rtn = true;
    }
    else // a new layout
    {
      numFields = screenLayout.layoutToBuffer(buf1, buf2, size1, fieldNames, fieldTypes, fieldSizes, localDefnsDir, defnsDir, "110a.htm", 1, getFieldNamesIAT(), fieldSizesIAT, null, null, null, null);

      if(dataAlready[0] != '\000') // coming with an err msg - passing-in data already entered but the data's fields may (will) not
      {                            // be in the correct order
        sortFields(dataAlready, data, "iat");
      }  
      else
      {
        screenLayout.buildNewDetails(fieldNames, numFields, "iat", data);

        documentUtils.getNextCode(con, stmt, rs, "iat", true, code);

        generalUtils.repAlphaUsingOnes(data, 5000, "IATCode", generalUtils.stringFromBytes(code, 0L));
        generalUtils.repAlphaUsingOnes(data, 5000, "Status", "L");

        generalUtils.repAlphaUsingOnes(data, 5000, "Date", generalUtils.today(localDefnsDir, defnsDir));
      }

      ddlData = accountsUtils.getGLAccountsDDLData("iat.AccountCr", unm, dnm, ddlData, ddlDataUpto, ddlDataLen, workingDir);
      ddlData = accountsUtils.getGLAccountsDDLData("iat.AccountDr", unm, dnm, ddlData, ddlDataUpto, ddlDataLen, workingDir);
      ddlData = accountsUtils.getCurrencyNamesDDLData(con, stmt, rs, "iat.CurrencyCr",  dnm, ddlData, ddlDataUpto, ddlDataLen, localDefnsDir, defnsDir);
      ddlData = accountsUtils.getCurrencyNamesDDLData(con, stmt, rs, "iat.CurrencyDr",  dnm, ddlData, ddlDataUpto, ddlDataLen, localDefnsDir, defnsDir);

      prependEdit(con, stmt, rs, unm, sid, uty, men, den, dnm, bnm, errStr, "C", generalUtils.stringFromBytes(code, 0L), date, "110a.htm", localDefnsDir, defnsDir, prependCode, req, bytesOut);

      screenLayout.bufferToBuffer(buf1, buf2, size1, size2, source, numFields, fieldNames, fieldTypes, fieldSizes, 'E', 'H', data, 5000, ddlData, ddlDataUpto[0], null, prependCode);

      append(con, stmt, rs, buf1, buf2, source, size1, size2, unm, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

      screenLayout.bufferToOut(buf1, buf2, source, out);
      rtn = true;
    }

    return rtn;
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

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void prepend(Connection con, Statement stmt, ResultSet rs, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String code, String date, byte[] b, HttpServletRequest req, String localDefnsDir,
                       String defnsDir, int[] bytesOut) throws Exception
  {
    String code2 = code;

    b[0]= '\000';
    scoutln(b, bytesOut, "<html><head><title>" + code + "</title>");
    scoutln(b, bytesOut, "<script language=\"JavaScript\">\n");

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 6079, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(b, bytesOut, "function edit(){");
      scoutln(b, bytesOut, "window.location.replace(\"/central/servlet/InterAccountTransferHeaderEdit?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + code2 + "&p2=A&p3=&p4=\");}\n");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 6084, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(b, bytesOut, "function print(){");
      scoutln(b, bytesOut, "window.location.href=\"/central/servlet/_6084?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + code2 + "&p2=&p3=&p4=\";}");
    }

    String cssDirectory = directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir);
    String cssDocumentDirectory = directoryUtils.getCssDocumentDirectory(con, stmt, rs, dnm);
    scoutln(b, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + cssDirectory + "general.css\">\n");
    scoutln(b, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + cssDocumentDirectory + "document.css\">\n");
    
    outputPageFrame(con, stmt, rs, b, req, ' ', ' ', "", "InterAccountTransferPage", date, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    scoutln(b, bytesOut, "<form>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void prependEdit(Connection con, Statement stmt, ResultSet rs, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String errStr, String cad, String code, String date, String layoutFile,
                           String localDefnsDir, String defnsDir, byte[] b, HttpServletRequest req, int[] bytesOut) throws Exception
  {
    String code2 = generalUtils.sanitise(code);

    scoutln(b, bytesOut, "<html><head><title>" + code + "</title>");
    scoutln(b, bytesOut, "<script language=\"JavaScript\">\n");

    byte[] sourceBuf = new byte[5000];
    sourceBuf = htmlBuild.layoutToBuffer(sourceBuf, 5000, localDefnsDir, defnsDir, layoutFile);
    String saveStr = htmlBuild.buildSaveStringDelimited(sourceBuf, "document.forms[0]");

    scoutln(b, bytesOut, "function save(thisOp){var saveStr=\"\";" + saveStr + "\n");
    scoutln(b, bytesOut, "var saveStr2=strip(saveStr);window.location.replace(\"/central/servlet/InterAccountTransferHeaderUpdate?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + cad + "&p2=" + code2
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

    outputPageFrame(con, stmt, rs, b, req, ' ', cad.charAt(0), "", "InterAccountTransferHeaderEdit", date, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    if(errStr.length() > 0 && ! errStr.equalsIgnoreCase("null"))
      scoutln(b, bytesOut, errStr);

    scoutln(b, bytesOut, "<form>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void getAnIATFieldGivenCode(Connection con, Statement stmt, ResultSet rs, String fieldName, byte[] iatCode, byte[] value, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    if(iatCode[0] == '\000') // just-in-case
      return;

    generalUtils.toUpper(iatCode, 0);

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT " + fieldName + " FROM iat WHERE IATCode = '" + generalUtils.stringFromBytes(iatCode, 0L) + "'");
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

  // -------------------------------------------------------------------------------------------------------------------------------------------------
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
        while(valuePtr < 499 && data[ptr] != '\001')
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
  // rtns: 'X' if already exists
  //       ' ' if updated sucessfully
  //       'N' if no code specified
  //       'F' if not updated
  //       'I' if illegal code
  public char putHead(Connection con, Statement stmt, ResultSet rs, byte[] originalCode, String unm, String dnm, String localDefnsDir, String defnsDir, char cad, byte[] recData, int recDataLen, byte[] code, int[] bytesOut) throws Exception
  {
    byte[] str = new byte[21];  str[0] = '\000';
    generalUtils.catAsBytes("IATCode", 0, str, true);

    if(searchDataString(recData, recDataLen, "iat", str, code) == 0)
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
    
    String currencyCr = "", currencyDr = "", transactionDate = "1.1.1970";
    double amountCr = 0.0, amountDr = 0.0, chargesCr = 0.0, chargesDr = 0.0;

    String fieldNamesIAT = getFieldNamesIAT();

    int len = fieldNamesIAT.length();
    int x=0, y, fieldCount=0;
    while(x < len)
    {
      y=0;
      while(x < len && fieldNamesIAT.charAt(x) != ',')
        fldName[y++] = (byte)fieldNamesIAT.charAt(x++);
      fldName[y] = '\000';
      ++x;
      
      while(x < len && fieldNamesIAT.charAt(x) == ' ')
        ++x;

      if(searchDataString(recData, recDataLen, "iat", fldName, value) != -1) // entry exists
      {  
        if(fieldCount == 0) // code
          ;
        else
        if(fieldCount == 1) // date
        {
          generalUtils.repAlpha(buf, 2000, (short)1, generalUtils.convertDateToSQLFormat(generalUtils.stringFromBytes(value, 0L)));  
        }
        else
        if(fieldCount == 2) // transactionDate
        {
          transactionDate = generalUtils.stringFromBytes(value, 0L);
          generalUtils.repAlpha(buf, 2000, (short)2, generalUtils.convertDateToSQLFormat(transactionDate));
        }
        else
        if(fieldCount == 15) // signon
        {
          if(value[0] == '\000')
            generalUtils.putAlpha(buf, 2000, (short)15, unm);
          else generalUtils.putAlpha(buf, 2000, (short)15, value);
        }
        else
        if(fieldCount == 8) // rateCr
        {
          if(value[0] == '\000')
            generalUtils.repAlpha(buf, 2000, (short)8, "1.00");
          else generalUtils.repAlpha(buf, 2000, (short)8, value);
        }
        else
        if(fieldCount == 14) // rateCrCharges
        {
          if(value[0] == '\000')
            generalUtils.repAlpha(buf, 2000, (short)14, "1.00");
          else generalUtils.repAlpha(buf, 2000, (short)14, value);
        }
        else
        if(fieldCount == 3) // accountCr
        {
          int xx=0;
          int valueLen = generalUtils.lengthBytes(value, 0);
          while(xx < valueLen && value[xx] != ' ')
            ++xx;
          value[xx] = '\000'; // only use the accCode
          
          generalUtils.repAlpha(buf, 2000, (short)3, value);
        }
        else
        if(fieldCount == 9) // accountDr
        {
          int xx=0;
          int valueLen = generalUtils.lengthBytes(value, 0);
          while(xx < valueLen && value[xx] != ' ')
            ++xx;
          value[xx] = '\000'; // only use the accCode
          
          generalUtils.repAlpha(buf, 2000, (short)9, value);
        }
        else
        if(fieldCount == 17) // rateDrCharges
        {
          if(value[0] == '\000')
            generalUtils.repAlpha(buf, 2000, (short)17, "1.00");
          else generalUtils.repAlpha(buf, 2000, (short)17, value);
        }
        else
        if(fieldCount == 20) // rateDr
        {
          if(value[0] == '\000')
            generalUtils.repAlpha(buf, 2000, (short)20, "1.00");
          else generalUtils.repAlpha(buf, 2000, (short)20, value);
        }
        else
        if(fieldCount == 6) // chargesCr
        {
          chargesCr = generalUtils.doubleDPs(generalUtils.doubleFromBytesCharFormat(value, 0), '2');
          generalUtils.repAlpha(buf, 2000, (short)6, value);
        }
        else
        if(fieldCount == 12) // chargesDr
        {
          chargesDr = generalUtils.doubleDPs(generalUtils.doubleFromBytesCharFormat(value, 0), '2');
          generalUtils.repAlpha(buf, 2000, (short)12, value);
        }
        else
        if(fieldCount == 7) // CurrencyCr
        {
          currencyCr = generalUtils.stringFromBytes(value, 0L);
          generalUtils.repAlpha(buf, 2000, (short)7, value);
        }
        else
        if(fieldCount == 19) // CurrencyDr
        {
          currencyDr = generalUtils.stringFromBytes(value, 0L);
          generalUtils.repAlpha(buf, 2000, (short)19, value);
        }
        else
        if(fieldCount == 5) // amountCr
        {
          amountCr = generalUtils.doubleDPs(generalUtils.doubleFromBytesCharFormat(value, 0), '2');
          generalUtils.repAlpha(buf, 2000, (short)5, value);
        }
        else
        if(fieldCount == 11) // amountDr
        {
          amountDr = generalUtils.doubleDPs(generalUtils.doubleFromBytesCharFormat(value, 0), '2');
          generalUtils.repAlpha(buf, 2000, (short)11, value);
        }
        else generalUtils.repAlpha(buf, 2000, (short)fieldCount, value);
      }
      
      ++fieldCount;
    }

    // just-in-case no value then need to insert a default
    if((generalUtils.dfsAsStr(buf, (short)5)).length() == 0) // amountCr 
      generalUtils.putAlpha(buf, 2000, (short)5, "0"); 

    if((generalUtils.dfsAsStr(buf, (short)11)).length() == 0) // amountDr
      generalUtils.putAlpha(buf, 2000, (short)11, "0");
    
    if((generalUtils.dfsAsStr(buf, (short)6)).length() == 0) // chargesCr 
      generalUtils.putAlpha(buf, 2000, (short)6, "0");
    
    if((generalUtils.dfsAsStr(buf, (short)12)).length() == 0) // chargesDr
      generalUtils.putAlpha(buf, 2000, (short)12, "0");
    
    if((generalUtils.dfsAsStr(buf, (short)8)).length() == 0) // rateCr
        generalUtils.putAlpha(buf, 2000, (short)8, "1.00");

    if((generalUtils.dfsAsStr(buf, (short)20)).length() == 0) // rateDr
        generalUtils.putAlpha(buf, 2000, (short)20, "1.00");

    if((generalUtils.dfsAsStr(buf, (short)14)).length() == 0) // rateCrCharges
        generalUtils.putAlpha(buf, 2000, (short)14, "1.00");

    if((generalUtils.dfsAsStr(buf, (short)17)).length() == 0) // rateDrCharges
        generalUtils.putAlpha(buf, 2000, (short)17, "1.00");

    if((generalUtils.dfsAsStr(buf, (short)2)).length() == 0) // transactiondate 
      generalUtils.repAlpha(buf, 2000, (short)2, "1970-01-01"); 

    if((generalUtils.dfsAsStr(buf, (short)15)).length() == 0) // signon 
      generalUtils.repAlpha(buf, 2000, (short)15, unm);
    
    // calc the appropriate rate in order that to-base calculations balance
    
    boolean crNotBase = false, drNotBase = false;
    
    String baseCurrency = accountsUtils.getBaseCurrency(con, stmt, rs, dnm, localDefnsDir, defnsDir);
    
    if(currencyCr.equals(baseCurrency))
    {
      generalUtils.repAlpha(buf, 2000, (short)8,  "1.00");
      generalUtils.repAlpha(buf, 2000, (short)14, "1.00");
    }
    else crNotBase = true;
    
    if(currencyDr.equals(baseCurrency))
    {
      generalUtils.repAlpha(buf, 2000, (short)20, "1.00");
      generalUtils.repAlpha(buf, 2000, (short)17, "1.00");
    }
    else drNotBase = true;
 
    double rate, baseAmount, nonBaseAmount;
    byte[] actualDate = new byte[20];

    if(crNotBase)
    {
      if(drNotBase)
      {
        rate = accountsUtils.getApplicableRate(con, stmt, rs, currencyCr, transactionDate, actualDate, dnm, localDefnsDir, defnsDir);
        generalUtils.repAlpha(buf, 2000, (short)8,  generalUtils.doubleToStr(rate));
        generalUtils.repAlpha(buf, 2000, (short)14, generalUtils.doubleToStr(rate));

        baseAmount    = generalUtils.doubleDPs((amountCr * rate), '2');
        nonBaseAmount = amountDr - chargesDr;

        if(nonBaseAmount == 0.0) nonBaseAmount = 1.0;
        rate = baseAmount / nonBaseAmount;
        generalUtils.repAlpha(buf, 2000, (short)20, generalUtils.doubleToStr('8', rate));

        if(chargesDr == 0.0)
          rate = 0.0;
        else rate = chargesCr / chargesDr;
        generalUtils.repAlpha(buf, 2000, (short)17, generalUtils.doubleToStr('8', rate));
      }
      else // dr is base
      {
        baseAmount    = generalUtils.doubleDPs(amountDr, '2');
        nonBaseAmount = generalUtils.doubleDPs((amountCr + chargesCr), '2');
        if(nonBaseAmount == 0.0) nonBaseAmount = 1.0;
        rate = baseAmount / nonBaseAmount;
        generalUtils.repAlpha(buf, 2000, (short)8, generalUtils.doubleToStr('8', rate));

        if(chargesCr == 0.0)
          rate = 0.0;
        else rate = chargesDr / chargesCr;
        generalUtils.repAlpha(buf, 2000, (short)14, generalUtils.doubleToStr('8', rate));
      }
    }
    else // cr is Base
    {
      if(drNotBase)
      {
        baseAmount    = generalUtils.doubleDPs(amountCr, '2');
        nonBaseAmount = generalUtils.doubleDPs((amountDr - chargesDr), '2');
        if(nonBaseAmount == 0.0) nonBaseAmount = 1.0;
        rate = baseAmount / nonBaseAmount;
        generalUtils.repAlpha(buf, 2000, (short)20, generalUtils.doubleToStr('8', rate));

        if(chargesDr == 0.0)
          rate = 0.0;
        else rate = chargesCr / chargesDr;
        generalUtils.repAlpha(buf, 2000, (short)17, generalUtils.doubleToStr('8', rate));
      }
      // else both are base
    }

    return putRecHeadGivenCode(con, stmt, rs, code, newOrEdit, buf, dnm, localDefnsDir, defnsDir);
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public int getRecGivenCode(Connection con, Statement stmt, ResultSet rs, byte[] code, char separator, byte[] data, String dnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    if(code[0] == '\000') // just-in-case
      return -1;

    generalUtils.toUpper(code, 0);

    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT * FROM iat WHERE IATCode = '" + generalUtils.stringFromBytes(code, 0L) + "'");
    if(! rs.next())
    {
      rs.close();
      stmt.close();
      return -1;
    }      
    
    ResultSetMetaData rsmd = rs.getMetaData();
    
    String fieldTypes = getFieldTypesIAT();
     
    if(separator == '\000')
    {
      int x, y=0;
      String s = getValue(1, ' ', rs, rsmd);
      int len = s.length();
      for(x=0;x<len;++x)
        data[y++] = (byte)s.charAt(x);
      data[y++] = '\000';
      
      for(int z=2;z<fieldTypes.length();++z)
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
      String fieldNames = getFieldNamesIAT();
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
        scoutln(data, bytesOut, "iat." + thisFieldName + "=" + getValue(count, thisFieldType, rs, rsmd) + "\001");
      
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

      return str;
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
      String fieldTypes = getFieldTypesIAT();
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
              else opStr += (char)data[x];
              ++x;
            }
            opStr += "'";

            if(thisFieldType == 'D' || thisFieldType == 'T')
              opStr += "}";
          }
           
          ++x;
        }
      
        q = "INSERT INTO iat (" + getFieldNamesIAT() + ") VALUES (" + opStr + ")";
      }
      else // newOrEdit == 'E'
      {
        String thisFieldName;
        char thisFieldType;

        // change: abc\0def\0... format into: CompanyCode='abc',Name='def',...

        String fieldNames = getFieldNamesIAT();
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

        q = "UPDATE iat SET " + opStr + " WHERE IATCode = '" + generalUtils.stringFromBytes(code, 0L) + "'";
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
  private void sortFields(byte[] ipBuf, byte[] opBuf, String which) throws Exception
  {
    String thisFieldName;
    String fieldNames;
    int x=0, y;
    byte[] thisFieldNameB = new byte[50];
    byte[] value          = new byte[1000];
    
    fieldNames = getFieldNamesIAT();
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
  private void outputPageFrame(Connection con, Statement stmt, ResultSet rs, byte[] b, HttpServletRequest req, char newOrEdit, char cad, String bodyStr, String callingServlet, String date, String unm, String sid, String uty, String men,
                               String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    int[] hmenuCount = new int[1];

    String subMenuText = dashboardUtils.buildSubMenuText(con, stmt, rs, req, "6077", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount);

    subMenuText += "<table id='title' width=100%><tr><td colspan=2 nowrap><p>Inter-Account Transfer" + directoryUtils.buildHelp(6077) + "</td></tr></table>";

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

    if(callingServlet.equals("InterAccountTransferPage"))      
      s += drawOptions6077(con, stmt, rs, req, hmenuCount, date, unm, uty, dnm, localDefnsDir, defnsDir);
    else
    if(callingServlet.equals("InterAccountTransferHeaderEdit"))      
      s += drawOptions6079(hmenuCount, cad, unm, sid, uty, men, den, dnm, bnm);
    
    s += "</div>";
    
    --hmenuCount[0];
    
    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private String drawOptions6077(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, int[] hmenuCount, String date, String unm, String uty, String dnm, String overrideDefsnDir, String defnsDir) throws Exception
  {      
    String s = "";
    
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 6079, unm, uty, dnm, overrideDefsnDir, defnsDir) && serverUtils.passLockCheck(con, stmt, rs, "iat", date, unm))
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\"><a href=\"javascript:edit()\">Edit Details</a></dt></dl>\n";

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 6084, unm, uty, dnm, overrideDefsnDir, defnsDir))
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\"><a href=\"javascript:print()\">Print</a></dt></dl>\n";
    
    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private String drawOptions6079(int[] hmenuCount, char cad, String unm, String sid, String uty, String men, String mainDNM, String dnm, String bnm)
                                 throws Exception
  {      
    String s = "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\"><a href=\"javascript:save(' ')\">Save Changes</a></dt></dl>\n";

    if(cad != 'C')
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\"><a href=\"javascript:save('X')\">Cancel Changes</a></dt></dl>\n";
    
    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(byte[] b, int bytesOut[], String str) throws Exception
  {      
    generalUtils.catAsBytes(str, 0, b, false);
    bytesOut[0] += (str.length() + 2);    
  }

}
