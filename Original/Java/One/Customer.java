// =======================================================================================================================================================================================================
// System: ZaraStar: CompanyEngine: Company Record Access
// Module: Customer.java
// Author: C.K.Harvey
// Copyright (c) 1998-2008 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import java.io.*;
import java.math.BigDecimal;
import java.sql.*;
import javax.servlet.http.HttpServletRequest;

public class Customer
{
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ScreenLayout screenLayout = new ScreenLayout();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  AccountsUtils accountsUtils = new AccountsUtils();
  DocumentUtils documentUtils = new DocumentUtils();
  DashboardUtils dashboardUtils = new DashboardUtils();

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // DateLastContacted: used for date of known invoice paid
  public String getTableCreateString() throws Exception
  {
    return "company( CompanyCode char(20) not null, Name char(60) not null, SessionID char(20), "
                  + "CompanyType char(20),          IndustryType char(40),  Address1 char(40), "
                  + "Address2 char(40),             Address3 char(40),      Address4 char(40), "
                  + "Address5 char(40),             PostCode char(20),      State char(40), "
                  + "Country char(40),              Phone1 char(20),        Phone2 char(20), "
                  + "Fax char(20),                  EMail char(40),         SalesPerson char(40), "
                  + "DateFirstContacted date,       DateLastContacted date, Tag1 char(1), "
                  + "Tag2 char(1),                  Tag3 char(1),           Tag4 char(1), "
                  + "Tag5 char(1),                  Tag6 char(1),           Tag7 char(1), "
                  + "Tag8 char(1),                  Tag9 char(1),           Tag10 char(1), "
                  + "Notes char(250),               QuotationOnly char(1),  CreditLimit decimal(19,8), "
                  + "DateLastModified timestamp,    PaymentMode char(40),   InternalExternalOrOther char(1), "
                  + "WebSite char(40),              CreditDays integer,     Currency char(3), "
                  + "ShipName char(60),             ShipAddress1 char(40),  ShipAddress2 char(40), "
                  + "ShipAddress3 char(40),         ShipAddress4 char(40),  ShipAddress5 char(40), "
                  + "BillingStyle char(1),          Password char(8),       PriceBand integer, "
                  + "Status char(1),                PaymentTerms char(40),  NoStatements char(1), "
                  + "Latitude char(20),             Longitude char(20),     OnlyShowBuyersCatalog char(1), "
                  + "unique(CompanyCode))";
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStrings(String[] s) throws Exception
  {
    s[0] = "companyNameInx on company(Name)";
    s[1] = "companySalesPersonInx on company(SalesPerson)";
    s[2] = "companyIndustryTypeInx on company(IndustryType)";
    s[3] = "companyEmailInx on company(Email)";

    return 3;
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNames() throws Exception
  {
    return "CompanyCode, Name, SessionID, CompanyType, IndustryType, Address1, Address2, Address3, Address4, Address5, PostCode, "
          + "State, Country, Phone1, Phone2, Fax, EMail, SalesPerson, DateFirstContacted, DateLastContacted, Tag1, Tag2, Tag3, "
          + "Tag4, Tag5, Tag6, Tag7, Tag8, Tag9, Tag10, Notes, QuotationOnly, CreditLimit, DateLastModified, PaymentMode, "
          + "InternalExternalOrOther, WebSite, CreditDays, Currency, ShipName, ShipAddress1, ShipAddress2, ShipAddress3, "
          + "ShipAddress4, ShipAddress5, BillingStyle, Password, PriceBand, Status, PaymentTerms, NoStatements, Latitude, Longitude, OnlyShowBuyersCatalog";
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypes() throws Exception
  {
    return "CCCCCCCCCCCCCCCCCCDDCCCCCCCCCCCCFSCCCICCCCCCCCCICCCCCC";
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizes(short[] sizes) throws Exception
  {
    sizes[0]  = 20;  sizes[1]  = 60;  sizes[2]  = 20;  sizes[3]  = 20;  sizes[4]  = 40;  sizes[5]  = 40;  sizes[6]  = 40;
    sizes[7]  = 40;  sizes[8]  = 40;  sizes[9]  = 40;  sizes[10] = 20;  sizes[11] = 40;  sizes[12] = 40;  sizes[13] = 20;
    sizes[14] = 20;  sizes[15] = 20;  sizes[16] = 40;  sizes[17] = 40;  sizes[18] = 0;   sizes[19] = 0;   sizes[20] = 1;
    sizes[21] = 1;   sizes[22] = 1;   sizes[23] = 1;   sizes[24] = 1;   sizes[25] = 1;   sizes[26] = 1;   sizes[27] = 1;
    sizes[28] = 1;   sizes[29] = 1;   sizes[30] = 250; sizes[31] = 1;   sizes[32] = 0;   sizes[33] = -1;  sizes[34] = 40;
    sizes[35] = 1;   sizes[36] = 40;  sizes[37] = 0;   sizes[38] = 3;   sizes[39] = 60;  sizes[40] = 40;  sizes[41] = 40;
    sizes[42] = 40;  sizes[43] = 40;  sizes[44] = 40;  sizes[45] = 1;   sizes[46] = 8;   sizes[47] = 0;   sizes[48] = 1;
    sizes[49] = 40;  sizes[50] = 1;   sizes[51] = 20;  sizes[52] = 20;  sizes[53] = 1;
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public String getFieldStyles() throws Exception
  {
    return "MMOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO";
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public boolean companyGetRecToHTML(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty,
                                     String men, String den, String dnm, String bnm, char dispOrEdit, char cad, byte[] companyCode,
                                     String localDefnsDir, String defnsDir, String errStr, byte[] dataAlready, String imagesDir, int[] bytesOut)
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

    byte[] prependCode = new byte[10000];
    int[] bUpto = new int[1];  bUpto[0] = 0;
    int[] bSize = new int[1];  bSize[0] = 10000;

    boolean rtn=false;

    if(cad == 'A' && companyCode[0] == '\000')
    {
      messagePage.msgScreen(false, out, req, 6, unm, sid, uty, men, den, dnm, bnm, "4001", "", "", "", imagesDir, localDefnsDir, defnsDir, bytesOut);
      return true;
    }

    getFieldSizes(fieldSizes);

    if(cad == 'A') // not a new one
    {
      boolean ok = false;
      if(dataAlready[0] != '\000') // coming with an err msg - passing-in data already entered but
      {                            // the data's fields may (will) not be in the correct order, so sort them
        sortFieldData(dataAlready, data);
        ok = true;
      }
      else
      {
        if(getCompanyRecGivenCode(con, stmt, rs, companyCode, '\001', dnm, data, localDefnsDir, defnsDir) == -1)
          messagePage.msgScreen(false, out, req, 6, unm, sid, uty, men, den, dnm, bnm, "4001", "", "", "", imagesDir, localDefnsDir, defnsDir, bytesOut);
        else ok = true;
      }

      if(ok)
      {
        numFields = screenLayout.layoutToBuffer(buf1, buf2, size1, fieldNames, fieldTypes, fieldSizes, localDefnsDir, defnsDir, "010ai.htm", 1, getFieldNames(), fieldSizes, null, null, null, null);

        byte[] ddlData = new byte[1000];
        int[] ddlDataLen  = new int[1]; ddlDataLen[0] = 1000;
        int[] ddlDataUpto = new int[1]; ddlDataUpto[0] = 0;

        ddlData = documentUtils.getSalesPersonDDLData(  con, stmt, rs, "company.SalesPerson",  ddlData, ddlDataUpto, ddlDataLen);
        ddlData = documentUtils.getCountryDDLData(      con, stmt, rs, "company.Country",      ddlData, ddlDataUpto, ddlDataLen);
        ddlData = documentUtils.getCompanyTypeDDLData(  con, stmt, rs, "company.CompanyType",  ddlData, ddlDataUpto, ddlDataLen);
        ddlData = documentUtils.getIndustryTypeDDLData( con, stmt, rs, "company.IndustryType", ddlData, ddlDataUpto, ddlDataLen);
        ddlData = accountsUtils.getCurrencyNamesDDLData(con, stmt, rs, "company.Currency",     dnm, ddlData, ddlDataUpto, ddlDataLen, localDefnsDir, defnsDir);

        if(dispOrEdit == 'D')
          prependCode = prepend(con, stmt, rs, req, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, generalUtils.stringFromBytes(companyCode, 0L), prependCode, bUpto, bSize, bytesOut);
        else prependCode = prependEdit(con, stmt, rs, errStr, req, unm, sid, uty, men, den, dnm, bnm, cad, generalUtils.stringFromBytes(companyCode, 0L), localDefnsDir, defnsDir, prependCode, bUpto, bSize, bytesOut);

        if(dataAlready[0] == '\000') // NOT coming with an err msg
        {
          byte[] b = new byte[20];
          // convert dates
          generalUtils.dfsGivenSeparator(true, '\001', data, (short)18, b);
          generalUtils.convertFromYYYYMMDD(b);
          generalUtils.repAlphaGivenSeparator('\001', data, 5000, (short)18, b);
        }

        generalUtils.bytesDPsGivenSeparator(true, '\001', '2', data, 5000, 32); // creditlimit

        // replaces binary-3s in notes with newlines
        byte[] buf = new byte[300];
        generalUtils.replaceThreesWithNewlines(generalUtils.dfsAsStrGivenBinary1(true, data, (short)30), buf);
        generalUtils.repAlphaUsingOnes(data, 5000, "Notes", buf);

        screenLayout.bufferToBuffer(buf1, buf2, size1, size2, source, numFields, fieldNames, fieldTypes, fieldSizes, dispOrEdit, 'H', data, 5000, ddlData, ddlDataUpto[0], null, prependCode);

        append(con, stmt, rs, buf1, buf2, source, size1, size2, unm, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

        screenLayout.bufferToOut(buf1, buf2, source, out);
        rtn = true;
      }
    }
    else // a new layout
    {
      numFields = screenLayout.layoutToBuffer(buf1, buf2, size1, fieldNames, fieldTypes, fieldSizes, localDefnsDir, defnsDir, "010ai.htm", 1, getFieldNames(), fieldSizes, null, null, null, null);
      if(dataAlready[0] != '\000') // coming with an err msg - passing-in data already entered (but not yet saved)
        generalUtils.bytesToBytes(data, 0, dataAlready, 0);
      else
      {
        screenLayout.buildNewDetails(fieldNames, numFields, "company", data);

        byte[] nextCode = new byte[21];
        documentUtils.getNextCode(con, stmt, rs, "company", true, nextCode);
        generalUtils.repAlphaUsingOnes(data, 5000, "CompanyCode", nextCode);

        generalUtils.repAlphaUsingOnes(data, 5000, "InternalExternalOrOther", "E");
        generalUtils.repAlphaUsingOnes(data, 5000, "BillingStyle", "B");
      }

      byte[] ddlData = new byte[1000];
      int[] ddlDataLen  = new int[1]; ddlDataLen[0] = 1000;
      int[] ddlDataUpto = new int[1]; ddlDataUpto[0] = 0;
      ddlData = documentUtils.getSalesPersonDDLData(  con, stmt, rs, "company.SalesPerson",  ddlData, ddlDataUpto, ddlDataLen);
      ddlData = documentUtils.getCountryDDLData(      con, stmt, rs, "company.Country",      ddlData, ddlDataUpto, ddlDataLen);
      ddlData = documentUtils.getCompanyTypeDDLData(  con, stmt, rs, "company.CompanyType",  ddlData, ddlDataUpto, ddlDataLen);
      ddlData = documentUtils.getIndustryTypeDDLData( con, stmt, rs, "company.IndustryType", ddlData, ddlDataUpto, ddlDataLen);
      ddlData = accountsUtils.getCurrencyNamesDDLData(con, stmt, rs, "company.Currency",     dnm, ddlData, ddlDataUpto, ddlDataLen, localDefnsDir, defnsDir);

      prependCode = prependEdit(con, stmt, rs, errStr, req, unm, sid, uty, men, den, dnm, bnm, 'C', generalUtils.stringFromBytes(companyCode, 0L), localDefnsDir, defnsDir, prependCode, bUpto, bSize, bytesOut);

      screenLayout.bufferToBuffer(buf1, buf2, size1, size2, source, numFields, fieldNames, fieldTypes, fieldSizes, 'E', 'H', data, 5000, ddlData, ddlDataUpto[0], null, prependCode);

      append(con, stmt, rs, buf1, buf2, source, size1, size2, unm, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

      screenLayout.bufferToOut(buf1, buf2, source, out);
      rtn = true;
    }

    return rtn;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // rtns: 'X' if already exists
  //       ' ' if updated sucessfully
  //       'N' if no code specified
  //       'F' if not updated
  //       'I' if illegal code
  public char companyPutRec(Connection con, Statement stmt, ResultSet rs, byte[] originalCompanyCode, String dnm, String localDefnsDir, String defnsDir, char cad, byte[] recData, int recDataLen) throws Exception
  {
    byte[] codeBytes = new byte[21];
    return companyPutRec(con, stmt, rs, originalCompanyCode, dnm, localDefnsDir, defnsDir, cad, recData, recDataLen, codeBytes);
  }
  public char companyPutRec(Connection con, Statement stmt, ResultSet rs, byte[] originalCompanyCode, String dnm, String localDefnsDir, String defnsDir, char cad, byte[] recData, int recDataLen, byte[] codeBytes) throws Exception
  {
    byte[] b = new byte[21];
    generalUtils.catAsBytes("CompanyCode", 0, b, true);

    if(searchDataString(recData, recDataLen, "company", b, codeBytes) == -1)
      return 'N';

    char newOrEdit;

    if(cad == 'A')
      newOrEdit = 'E';
    else
    if(originalCompanyCode[0] == '\000')
      newOrEdit = 'N';
    else  // originalcode not blank
    {
      if(generalUtils.matchIgnoreCase(originalCompanyCode, 0, codeBytes, 0))
        newOrEdit = 'E';
      else // change in the code, or rec with no companycode supplied
        newOrEdit = 'N';
    }

    generalUtils.toUpper(codeBytes, 0);

    if(existsCompanyRecGivenCode(con, stmt, rs, codeBytes, dnm, localDefnsDir, defnsDir))
    {
      if(cad != 'A') // amending
        return 'X';
    }

    // get data values from recData and put into buf for updating
    byte[] buf = new byte[2000];

    generalUtils.putAlpha(buf, 2000, (short)0, codeBytes);

    // determines the number of fields and then processes them in order *but* makes no assumptions about order of fields in data

    String fieldNames = getFieldNames();
    byte[] value       = new byte[300];
    byte[] fieldName   = new byte[31];
    byte[] companyName = new byte[61];
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

      if(searchDataString(recData, recDataLen, "company", fieldName, value) != -1) // entry exists
      {
        if(fieldCount == 1)
          generalUtils.bytesToBytes(companyName, 0, value, 0);

        if(fieldCount == 35) // InternalOrExternalOrOther
        {
          if(value[0] == '\000')
            generalUtils.strToBytes(value, "E");
          generalUtils.putAlpha(buf, 2000, (short)35, value);
        }
        else
        if(fieldCount == 18) // dateFirstContacted
        {
          if(value[0] == '\000')
            generalUtils.putAlpha(buf, 2000, (short)18, "1970-01-01");
          else generalUtils.putAlpha(buf, 2000, (short)18, generalUtils.convertDateToSQLFormat(generalUtils.stringFromBytes(value, 0L)));
        }
        else
        if(fieldCount == 19) // dateLastContacted
        {
          if(value[0] == '\000')
            generalUtils.putAlpha(buf, 2000, (short)19, "1970-01-01");
          else generalUtils.putAlpha(buf, 2000, (short)19, generalUtils.convertDateToSQLFormat(generalUtils.stringFromBytes(value, 0L)));
        }
        else
        if(fieldCount == 32) // creditLimit
        {
          if(value[0] == '\000')
            generalUtils.repAlpha(buf, 2000, (short)32, "0");
          else generalUtils.repAlpha(buf, 2000, (short)32, value);
        }
        else
        if(fieldCount == 37) // creditDays
        {
          if(value[0] == '\000')
            generalUtils.putAlpha(buf, 2000, (short)37, "0");
          else generalUtils.putAlpha(buf, 2000, (short)37, value);
        }
        else
        if(fieldCount == 46) // password
        {
          generalUtils.putAlpha(buf, 2000, (short)46, value);
        }
        else
        if(fieldCount == 47) // priceband
        {
          if(value[0] == '\000')
            generalUtils.putAlpha(buf, 2000, (short)47, "0"); // band 0 (list price)
          else generalUtils.putAlpha(buf, 2000, (short)47, value);
        }
        else
        if(fieldCount == 30) // notes
        {
          byte[] b2 = new byte[300];
          int i2=0;
          int i=0;
          while(value[i] != '\000')
          {
            // else
            if(value[i] == (byte)10 || value[i] == (byte)1)
              ; // ignore
            else b2[i2++] = value[i];
            ++i;
          }
          b2[i2] = '\000';
          generalUtils.putAlpha(buf, 2000, (short)30, b2);
        }
        else generalUtils.repAlpha(buf, 2000, (short)fieldCount, value);
      }
      else
      {
        if(fieldCount == 18 || fieldCount == 19) // dateFirstContacted || dateLastContacted
          generalUtils.putAlpha(buf, 2000, (short)fieldCount, "1970-01-01");

        if(fieldCount == 32 || fieldCount == 37) // creditLimit || creditDays
          generalUtils.putAlpha(buf, 2000, (short)fieldCount, "0");

        if(fieldCount == 35) // InternalOrExternalOrOther
          generalUtils.putAlpha(buf, 2000, (short)35, "E");

        if(fieldCount == 45) // billingstyle
          generalUtils.putAlpha(buf, 2000, (short)45, "B");

        if(fieldCount == 52) // onlyshowbuyerscatalog
          generalUtils.putAlpha(buf, 2000, (short)45, "B");

        if(fieldCount == 47) // priceBand
          generalUtils.putAlpha(buf, 2000, (short)47, "0"); // list price
      }

      ++fieldCount;
    }

//    generalUtils.putAlpha(buf, 2000, (short)33, "NULL"); // dlm

    if(putCompanyRecGivenCode(con, stmt, rs, codeBytes, '0', newOrEdit, buf, dnm, localDefnsDir, defnsDir))
    {
    }
    else return 'F';

    return ' ';
  }

  // -------------------------------------------------------------------------------------------------------------------------------
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

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getACompanyFieldGivenCode(Connection con, Statement stmt, ResultSet rs, String fieldName, String companyCode) throws Exception
  {
    byte[] value = new byte[300]; // plenty
    byte[] companyCodeB = new byte[21];
    generalUtils.strToBytes(companyCodeB, companyCode);

    getACompanyFieldGivenCode(con, stmt, rs, fieldName, companyCodeB, value);

    return generalUtils.stringFromBytes(value, 0L);
  }
  public void getACompanyFieldGivenCode(Connection con, Statement stmt, ResultSet rs, String fieldName, byte[] companyCode, byte[] value) throws Exception
  {
    if(companyCode[0] == '\000') // just-in-case
      return;

    generalUtils.toUpper(companyCode, 0);

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT " + fieldName + " FROM company WHERE CompanyCode = '" + generalUtils.stringFromBytes(companyCode, 0L) + "'");
    if(! rs.next())
      value[0] = '\000';
    else
    {
      ResultSetMetaData rsmd = rs.getMetaData();
      generalUtils.strToBytes(value, getValue(1, ' ', rs, rsmd));
    }

    if(fieldName.equalsIgnoreCase("Currency") && value[0] == '\000')
       value[0] = '\000';

    rs.close();
    stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // sets : company.code=acme\001company.name=acme ltd\001...
  // returns: 0 or -1 if rec not found
  public int getCompanyRecGivenCode(Connection con, Statement stmt, ResultSet rs, byte[] companyCode, char separator, String dnm, byte[] data, String localDefnsDir, String defnsDir)
                                    throws Exception
  {
    return getCompanyRecGivenCode(con, stmt, rs, generalUtils.stringFromBytes(companyCode, 0L), separator, dnm, data, localDefnsDir, defnsDir);
  }
  public int getCompanyRecGivenCode(Connection con, Statement stmt, ResultSet rs, String companyCode, char separator, String dnm, byte[] data, String localDefnsDir, String defnsDir)
                                    throws Exception
  {
    if(companyCode.length() == 0) // just-in-case
      return -1;

    companyCode = companyCode.toUpperCase();

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT * FROM company WHERE CompanyCode = '" + companyCode + "'");
    if(! rs.next())
    {
      rs.close();
      stmt.close();
      return -1;
    }

    ResultSetMetaData rsmd = rs.getMetaData();

    String fieldTypes = getFieldTypes();

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
      String fieldNames = getFieldNames();
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
        generalUtils.catAsBytes("company." + thisFieldName + "=" + getValue(count, thisFieldType, rs, rsmd) + "\001", 0, data, terminate);

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
  public void determineCompAccCodeFromCompCode(byte[] companyCode, byte[] fullAccCode) throws Exception
  {
    generalUtils.bytesToBytes(fullAccCode, 0, companyCode, 0);
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean existsCompanyRecGivenCode(Connection con, Statement stmt, ResultSet rs, String companyCode, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    byte[] b = new byte[100]; // plenty
    generalUtils.strToBytes(b, companyCode);
    return existsCompanyRecGivenCode(con, stmt, rs, b, dnm, localDefnsDir, defnsDir);
  }
  public boolean existsCompanyRecGivenCode(Connection con, Statement stmt, ResultSet rs, byte[] companyCode, String dnm, String localDefnsDir, String defnsDir)
                                           throws Exception
  {
    if(companyCode[0] == '\000') // just-in-case
      return false;

    generalUtils.toUpper(companyCode, 0);

    int numRecs = 0;

    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM company WHERE CompanyCode = '" + generalUtils.stringFromBytes(companyCode, 0L) + "'");
      if(rs.next())
        numRecs = rs.getInt("rowcount");

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }

    if(numRecs == 1)
      return true;

    return false;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public int countCustomers(Connection con, Statement stmt, ResultSet rs) throws Exception
  {
    int numRecs = 0;

    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM company");

      if(rs.next())
        numRecs = rs.getInt("rowcount");

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }

    return numRecs;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getCompanyCurrencyGivenCode(Connection con, Statement stmt, ResultSet rs, String companyCode) throws Exception
  {
    byte[] currency = new byte[10];
    byte[] codeB = new byte[21];
    generalUtils.strToBytes(codeB, companyCode);
    getCompanyCurrencyGivenCode(con, stmt, rs, codeB, currency);
    return generalUtils.stringFromBytes(currency, 0L);
  }
  public void getCompanyCurrencyGivenCode(Connection con, Statement stmt, ResultSet rs, byte[] companyCode, byte[] currency) throws Exception
  {
    if(companyCode[0] == '\000') // just-in-case
    {
      currency[0] = '\000';
      return;
    }

    getACompanyFieldGivenCode(con, stmt, rs, "Currency", companyCode, currency);
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getCompanyNameGivenCode(Connection con, Statement stmt, ResultSet rs, String companyCode) throws Exception
  {
    byte[] name = new byte[100];
    byte[] codeB = new byte[21];
    generalUtils.strToBytes(codeB, companyCode);
    getCompanyNameGivenCode(con, stmt, rs, codeB, name);
    return generalUtils.stringFromBytes(name, 0L);
  }
  public void getCompanyNameGivenCode(Connection con, Statement stmt, ResultSet rs, byte[] companyCode, byte[] companyName) throws Exception
  {
    if(companyCode[0] == '\000') // just-in-case
    {
      companyName[0] = '\000';
      return;
    }

    getACompanyFieldGivenCode(con, stmt, rs, "Name", companyCode, companyName);
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public int getCompanyTermsGivenCode(Connection con, Statement stmt, ResultSet rs, byte[] companyCode) throws Exception
  {
    if(companyCode[0] == '\000') // just-in-case
      return 0;

    byte[] value = new byte[50];
    getACompanyFieldGivenCode(con, stmt, rs, "CreditDays", companyCode, value);
    return generalUtils.intFromBytesCharFormat(value, (short)0);
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public char getCompanyBillingStyleGivenCode(Connection con, Statement stmt, ResultSet rs, byte[] companyCode) throws Exception
  {
    if(companyCode[0] == '\000') // just-in-case
      return ' ';

    byte[] style = new byte[2];
    getACompanyFieldGivenCode(con, stmt, rs, "BillingStyle", companyCode, style);
    return (char)style[0];
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public char getCompanyInternalExternalOrOther(Connection con, Statement stmt, ResultSet rs, byte[] companyCode) throws Exception
  {
    if(companyCode[0] == '\000') // just-in-case
      return 'E'; // external

    byte[] internalExternalOrOther = new byte[2];
    getACompanyFieldGivenCode(con, stmt, rs, "InternalExternalOrOther", companyCode, internalExternalOrOther);
    return (char)internalExternalOrOther[0];
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void append(Connection con, Statement stmt, ResultSet rs, byte[][] buf1, byte[][] buf2, char[] source, int[] iSize1, int[] iSize2, String unm, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut)
                      throws Exception
  {
    byte[] b = new byte[1000];
    int[] bUpto = new int[1];  bUpto[0] = 0;
    int[] bSize = new int[1];  bSize[0] = 1000;

    b = scoutln(b, bytesOut, "</form>", bUpto, bSize);
    b = scoutln(b, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir), bUpto, bSize);
    screenLayout.appendBytesToBuffer(buf1, buf2, source, iSize1, iSize2, b);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private byte[] prepend(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, String code, byte[] b,
                         int[] bUpto, int[] bSize, int[] bytesOut) throws Exception
  {
    String code2 = code;

    b[0] = '\000';
    b = scoutln(b, bytesOut, "<html><head><title>" + code + "</title>", bUpto, bSize);

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4226, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      b = scoutln(b, bytesOut, "<SCRIPT LANGUAGE=\"JavaScript\">\n", bUpto, bSize);
      b = scoutln(b, bytesOut, "function fetch(){", bUpto, bSize);
      b = scoutln(b, bytesOut, "window.location.replace(\"http://" + men + "/central/servlet/CustomerEdit?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + generalUtils.sanitise(code2)
                             + "&p2=&p3=&p4=A\");}\n", bUpto, bSize);
      b = scoutln(b, bytesOut, "</script>\n", bUpto, bSize);
    }

    String cssDirectory = directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir);
    String cssDocumentDirectory = directoryUtils.getCssDocumentDirectory(con, stmt, rs, dnm);
    b = scoutln(b, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + cssDirectory + "general.css\">\n", bUpto, bSize);
    b = scoutln(b, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + cssDocumentDirectory + "document.css\">\n", bUpto, bSize);

    b = outputPageFrame(con, stmt, rs, b, req, ' ', code2, "", "CustomerPage", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, bUpto, bSize, bytesOut);

    b = scoutln(b, bytesOut, "<form>", bUpto, bSize);

    return b;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private byte[] prependEdit(Connection con, Statement stmt, ResultSet rs, String errStr, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, char cad, String code, String localDefnsDir,
                             String defnsDir, byte[] b, int[] bUpto, int[] bSize, int[] bytesOut) throws Exception
  {
    String code2 = generalUtils.sanitise(code);
    b = scoutln(b, bytesOut, "<html><head><title>" + code + "</title>", bUpto, bSize);

    b = scoutln(b, bytesOut, "<SCRIPT LANGUAGE=\"JavaScript\">\n", bUpto, bSize);
    b = scoutln(b, bytesOut, "function save(option){", bUpto, bSize);
    b = scoutln(b, bytesOut, "document.forms[0].p4.value=option;", bUpto, bSize);
    b = scoutln(b, bytesOut, "document.go.submit();}\n", bUpto, bSize);
    b = scoutln(b, bytesOut, "</script>\n", bUpto, bSize);

    String cssDirectory = directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir);
    String cssDocumentDirectory = directoryUtils.getCssDocumentDirectory(con, stmt, rs, dnm);
    b = scoutln(b, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + cssDirectory + "general.css\">\n", bUpto, bSize);
    b = scoutln(b, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + cssDocumentDirectory + "document.css\">\n", bUpto, bSize);

    b = scoutln(b, bytesOut, "<form name=go action=\"CustomerUpdate\" enctype=\"application/x-www-form-urlencoded\" method=POST>", bUpto, bSize);
    b = scoutln(b, bytesOut, "<input type=hidden name=unm value="+unm+">", bUpto, bSize);
    b = scoutln(b, bytesOut, "<input type=hidden name=sid value="+sid+">", bUpto, bSize);
    b = scoutln(b, bytesOut, "<input type=hidden name=uty value="+uty+">", bUpto, bSize);
    b = scoutln(b, bytesOut, "<input type=hidden name=men value=\""+men+"\">", bUpto, bSize);
    b = scoutln(b, bytesOut, "<input type=hidden name=den value="+den+">", bUpto, bSize);
    b = scoutln(b, bytesOut, "<input type=hidden name=dnm value="+dnm+">", bUpto, bSize);
    b = scoutln(b, bytesOut, "<input type=hidden name=bnm value="+bnm+">", bUpto, bSize);

    b = scoutln(b, bytesOut, "<input type=hidden name=p1 value="+cad+">", bUpto, bSize);
    b = scoutln(b, bytesOut, "<input type=hidden name=p2 value="+code2+">", bUpto, bSize);
    b = scoutln(b, bytesOut, "<input type=hidden name=p4 value=''>", bUpto, bSize);

    b = outputPageFrame(con, stmt, rs, b, req, cad, code2, "", "CustomerUpdate", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, bUpto, bSize, bytesOut);

    if(errStr.length() > 0 && ! errStr.equalsIgnoreCase("null"))
      generalUtils.catAsBytes(errStr, 0, b, false);

    return b;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean putCompanyRecGivenCode(Connection con, Statement stmt, ResultSet rs, byte[] code, char newOrEdit, byte[] data, String dnm, String localDefnsDir, String defnsDir)
  {
    return putCompanyRecGivenCode(con, stmt, rs, code, '0', newOrEdit, data, dnm, localDefnsDir, defnsDir);
  }
  public boolean putCompanyRecGivenCode(Connection con, Statement stmt, ResultSet rs, byte[] code, char separator, char newOrEdit, byte[] data, String dnm, String localDefnsDir, String defnsDir)
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
      String fieldTypes = getFieldTypes();
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

        q = "INSERT INTO company (" + getFieldNames() + ") VALUES (" + opStr + ")";
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

          String fieldNames = getFieldNames();
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

        q = "UPDATE company SET " + opStr + " WHERE CompanyCode = '" + generalUtils.stringFromBytes(code, 0L) + "'";
      }

      stmt.executeUpdate(q);

      stmt.close();
    }
    catch(Exception e) { System.out.println(e); return false; }

    return true;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean companyDeleteRec(Connection con, Statement stmt, ResultSet rs, String companyCode, String dnm, String localDefnsDir, String defnsDir)
  {
    try
    {
      if(companyCode.length() == '\000') // just-in-case
        return false;

      companyCode = companyCode.toUpperCase();

      stmt = con.createStatement();

      stmt.executeUpdate("DELETE FROM company WHERE CompanyCode = '" + companyCode + "'");

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
  public void updateStatus(Connection con, Statement stmt, ResultSet rs, byte[] status, byte[] eMail, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    stmt = con.createStatement();

    stmt.executeUpdate("UPDATE company SET Status = '" + generalUtils.stringFromBytes(status, 0L) + "' WHERE EMail = '"
                       + generalUtils.stringFromBytes(eMail, 0L) + "'");

    stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public void updatedDateInvoicePaid(Connection con, Statement stmt, ResultSet rs, String date, String companyCode, String dnm, String localDefnsDir, String defnsDir)
                                     throws Exception
  {
    stmt = con.createStatement();

    stmt.executeUpdate("UPDATE company SET DateLastContacted = {d '" + date + "'} WHERE CompanyCode = '" + companyCode + "'");

    stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public boolean getRecGivenEMail(Connection con, Statement stmt, ResultSet rs, byte[] eMail, byte[] data, String dnm, String localDefnsDir, String defnsDir)
  {
    try
    {
      if(eMail[0] == '\000') // just-in-case
        return false;

      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT * FROM company WHERE EMail = '" + generalUtils.stringFromBytes(eMail, 0L) + "'");
      if(! rs.next())
      {
        rs.close();
        stmt.close();

        return false;
      }

      ResultSetMetaData rsmd = rs.getMetaData();

      generalUtils.catAsBytes(getValue(1, ' ', rs, rsmd), 0, data, true);

      char type;
      for(int x=2;x<52;++x)
      {
        if(x == 19 || x == 20 || x == 34)
          type = 'D';
        else
        if(x == 35)
          type = 'T';
        else type = ' ';

        generalUtils.catAsBytes(getValue(x, type, rs, rsmd), 0, data, false);
      }

      rs.close();
      stmt.close();

      return true;
    }
    catch(Exception e)
    {
      try
      {
        if(rs   != null) rs.close();
        if(stmt != null) stmt.close();
      }
      catch(Exception e2) { }

      return false;
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  // assumes company table is on same server as call
  public boolean validateSignOnCompany(Connection con, Statement stmt, ResultSet rs, String uName, String passWord, String[] storedSessionID, byte[] companyName, String dnm,
                                         String localDefnsDir, String defnsDir) throws Exception
  {
    byte[] companyCode = new byte[50];
    generalUtils.strToBytes(companyCode, uName.toUpperCase());

    try
    {
      if(companyCode[0] == '\000') // just-in-case
        return false;

      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT Password, SessionID, Name FROM company WHERE CompanyCode = '"
                             + generalUtils.stringFromBytes(companyCode, 0L) + "'");
      if(! rs.next())
      {
        rs.close();
        stmt.close();

        return false;
      }

      ResultSetMetaData rsmd = rs.getMetaData();
      String pwd = getValue(1, ' ', rs, rsmd);

      if(pwd.length() == 0) // no pwd
        return false;

      if(pwd.equals(passWord))
      {
        storedSessionID[0] = getValue(2, ' ', rs, rsmd);
        generalUtils.strToBytes(companyName, getValue(3, ' ', rs, rsmd));

        return true;
      }

      return false; // pwd mismatch
    }
    catch(Exception e)
    {
       if(rs   != null) rs.close();
       if(stmt != null) stmt.close();

       return false;
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  // assumes company table is on same server as call
  public boolean updateSessionIDCompany(Connection con, Statement stmt, ResultSet rs, String uName, String sessionID, String dnm, String localDefnsDir, String defnsDir)
  {
    try
    {
      byte[] companyCode = new byte[21];
      generalUtils.strToBytes(companyCode, uName.toUpperCase());

      stmt = con.createStatement();

      stmt.executeUpdate("UPDATE company SET SessionID = '" + sessionID + "' WHERE CompanyCode = '"
                         + generalUtils.stringFromBytes(companyCode, 0L) + "'");

      if(stmt != null) stmt.close();

      return true;
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
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  // picks-up the list of fieldnames for the company file, and fetches the data value for that field; and rebuilds the data string
  private void sortFieldData(byte[] ipBuf, byte[] opBuf) throws Exception
  {
    String thisFieldName, fieldNames = getFieldNames();
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

      generalUtils.catAsBytes("company." + thisFieldName + "=", 0, opBuf, false);

      if(searchDataString(ipBuf, recDataLen, "company", thisFieldNameB, value) != -1) // entry exists
        generalUtils.catAsBytes(generalUtils.stringFromBytes(value, 0L), 0, opBuf, false);

      generalUtils.catAsBytes("\001", 0, opBuf, false);
    }

    generalUtils.bytesToBytes(ipBuf, 0, opBuf, 0);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private byte[] outputPageFrame(Connection con, Statement stmt, ResultSet rs, byte[] b, HttpServletRequest req, char cad, String customerCode, String bodyStr, String callingServlet, String unm, String sid, String uty, String men, String den,
                                 String dnm, String bnm, String otherSetup, String localDefnsDir, String defnsDir, int[] bUpto, int[] bSize, int[] bytesOut) throws Exception
  {
    int[] hmenuCount = new int[1];

    String subMenuText = dashboardUtils.buildSubMenuText(con, stmt, rs, req, "4001", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount);

    subMenuText += "<table id='title' width=100%><tr><td colspan=2 nowrap><p>Customer Record" + directoryUtils.buildHelp(4001) + "</td></tr></table>";

    subMenuText += buildSubMenuText(con, stmt, rs, req, cad, callingServlet, customerCode, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount);

    b = scoutln(b, bytesOut, authenticationUtils.buildScreen(con, stmt, rs, req, true, unm, sid, uty, men, den, dnm, bnm, callingServlet, subMenuText, hmenuCount[0], "", otherSetup, localDefnsDir, defnsDir), bUpto, bSize);

    b = scoutln(b, bytesOut, "<table border=0><tr><td valign=top><div id='second' style=\"{margin-top:0px;}\"></div></td>", bUpto, bSize);
    b = scoutln(b, bytesOut, "<td valign=top><div id='third' style=\"{margin-top:0px;margin-left:0px;}\"></div></td></tr></table>", bUpto, bSize);

    return b;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private String buildSubMenuText(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, char cad, String callingServlet, String customerCode,
                                  String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir,
                                  int[] hmenuCount) throws Exception
  {
    String s = "<div id='submenu'>";

//    hmenuCount[0] = 1;
    ++hmenuCount[0];

    if(callingServlet.equals("CustomerPage"))
      s += drawOptions4001(con, stmt, rs, req, hmenuCount, customerCode, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir);
    else
    if(callingServlet.equals("CustomerUpdate"))
      s += drawOptions4227(hmenuCount, cad);

    s += "</div>";

    --hmenuCount[0];

    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private String drawOptions4001(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, int[] hmenuCount, String customerCode, String unm,
                                 String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir)
                                 throws Exception
  {
    String s = "";

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4002, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\">";
      s += "<a href=\"/central/servlet/CustomerSettlementHistoryInput?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=" + customerCode
        + " &p2=N&bnm=" + bnm + "\">Settlement</a></dt></dl>";
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 8804, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\">";
      s += "<a href=\"/central/servlet/ContactsShowCompany?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=" + customerCode
        + " &p2=C&bnm=" + bnm + "\">Contacts</a></dt></dl>";
    }

    String[] latitude  = new String[1];
    String[] longitude = new String[1];
    String[] address   = new String[1];

    getGoogleMapDataGivenCode(con, stmt, rs, customerCode, dnm, localDefnsDir, defnsDir, latitude, longitude, address);
    if(latitude[0].length() != 0 && longitude[0].length() != 0)
    {
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\">";
      s += "<a href=\"/central/servlet/GoogleMapServices?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p2="
        + customerCode + " &p1=C&bnm=" + bnm + "\">Location</a></dt></dl>";
    }

    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private String drawOptions4227(int[] hmenuCount, char cad) throws Exception
  {
    String s = "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\">";
    s += "<a href=\"javascript:save(' ')\">Save Changes</a></dt></dl>";

    if(cad != 'C')
    {
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\">";
      s += "<a href=\"javascript:save('X')\">Cancel Changes</a></dt></dl>";
    }

    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public int getPriceBand(Connection con, Statement stmt, ResultSet rs, String companyCode) throws Exception
  {
    int x = generalUtils.intFromStr(getACompanyFieldGivenCode(con, stmt, rs, "PriceBand", companyCode));
    if(x < 0 || x > 4) // just-in-case
      x = 0; // list price
    return x;
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

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void getGoogleMapDataGivenCode(Connection con, Statement stmt, ResultSet rs, String companyCode, String dnm, String localDefnsDir, String defnsDir, String[] latitude, String[] longitude, String[] address) throws Exception
  {
    latitude[0] = "";
    longitude[0] = "";
    address[0] = "";

    if(companyCode.length() == 0) // just-in-case
      return;

    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT Latitude, Longitude, Name, Address1, Address2, Address3, Address4, Address5, PostCode "
                           + "FROM company WHERE CompanyCode = '" + companyCode + "'");
       if(rs.next())
      {
        latitude[0]  = rs.getString(1);
        longitude[0] = rs.getString(2);
        address[0]   = rs.getString(3) + "<br>" + rs.getString(4) + "<br>" + rs.getString(5) + "<br>" + rs.getString(6) + "<br>" + rs.getString(7) + "<br>" + rs.getString(8)  + "<br>" + rs.getString(9);
      }

      if(latitude[0]  == null) latitude[0] = "";
      if(longitude[0] == null) longitude[0] = "";
      if(address[0]   == null) address[0] = "";

      if(rs   != null) rs.close();
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

}
