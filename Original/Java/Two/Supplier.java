// =======================================================================================================================================================================================================
// System: ZaraStar: CompanyEngine: Supplier Record Access
// Module: Supplier.java
// Author: C.K.Harvey
// Copyright (c) 1998-2008 Christopher Harvey. All Rights Reserved.
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

public class Supplier
{
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ScreenLayout screenLayout = new ScreenLayout();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  AccountsUtils accountsUtils = new AccountsUtils();
  DocumentUtils documentUtils = new DocumentUtils();
  DashboardUtils dashboardUtils = new DashboardUtils();

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getTableCreateString() throws Exception
  {
    return "supplier( SupplierCode char(20) not null, Name char(60) not null,     Address1 char(40), "
                   + "Address2 char(40),              Address3 char(40),          Address4 char(40), "
                   + "Address5 char(40),              PostCode char(20),          OfficePhone char(20), "
                   + "Fax char(20),                   EMail char(40),             Notes char(250), "
                   + "NoStatements char(1),           Contact1 char(40),          Description1 char(40), "
                   + "OfficePhone1 char(20),          HomePhone1 char(20),        Pager1 char(20), "
                   + "MobilePhone1 char(20),          EMail1 char(40),            Contact2 char(40), "
                   + "Description2 char(40),          OfficePhone2 char(20),      HomePhone2 char(20), "
                   + "Pager2 char(20),                MobilePhone2 char(20),      EMail2 char(40), "
                   + "DateEarliestInvoice date,       DateLastModified timestamp, InternalExternalOrOther char(1), "
                   + "Currency char(3),               Country char(40),           ShipName char(60), "
                   + "ShipAddress1 char(40),          ShipAddress2 char(40),      ShipAddress3 char(40), "
                   + "ShipAddress4 char(40),          ShipAddress5 char(40),      WebSite char(60), "
                   + "PaymentTerms char(40),          BankName char(40),          BankCode char(20), "
                   + "BankAccount char(20),           BankBranchCode char(20),    Latitude char(20), "
                   + "Longitude char(20),             ZCN char(20), "
                   + "unique(SupplierCode))";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStrings(String[] s) throws Exception
  {
    s[0] = "supplierNameInx on supplier(Name)";
    s[1] = "supplierContact1Inx on supplier(Contact1)";
    s[2] = "supplierEmailInx on supplier(Email)";
    s[3] = "zcnlInx on supplier(ZCN)";

    return 3;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNames() throws Exception
  {
    return "SupplierCode, Name, Address1, Address2, Address3, Address4, Address5, PostCode, OfficePhone, Fax, EMail, Notes, "
          + "NoStatements, Contact1, Description1, OfficePhone1, HomePhone1, Pager1, MobilePhone1, EMail1, Contact2, Description2, "
          + "OfficePhone2, HomePhone2, Pager2, MobilePhone2, EMail2, DateEarliestInvoice, DateLastModified, "
          + "InternalExternalOrOther, Currency, Country, ShipName, ShipAddress1, ShipAddress2, ShipAddress3, ShipAddress4, "
          + "ShipAddress5, WebSite, PaymentTerms, BankName, BankCode, BankAccount, BankBranchCode, Latitude, Longitude, ZCN";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypes() throws Exception
  {
    return "CCCCCCCCCCCCCCCCCCCCCCCCCCCDSCCCCCCCCCCCCCCCCCC";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizes(short[] sizes) throws Exception
  {
    sizes[0]  = 20;  sizes[1]  = 60;  sizes[2]  = 40;  sizes[3]  = 40;  sizes[4]  = 40;  sizes[5]  = 40;  sizes[6]  = 40;
    sizes[7]  = 20;  sizes[8]  = 20;  sizes[9]  = 20;  sizes[10] = 40;  sizes[11] = 250; sizes[12] = 1;   sizes[13] = 40;
    sizes[14] = 40;  sizes[15] = 20;  sizes[16] = 20;  sizes[17] = 20;  sizes[18] = 20;  sizes[19] = 40;  sizes[20] = 40;
    sizes[21] = 40;  sizes[22] = 20;  sizes[23] = 20;  sizes[24] = 20;  sizes[25] = 20;  sizes[26] = 40;  sizes[27] = 0;
    sizes[28] = -1;  sizes[29] = 1;   sizes[30] = 3;   sizes[31] = 40;  sizes[32] = 60;  sizes[33] = 40;  sizes[34] = 40;
    sizes[35] = 40;  sizes[36] = 40;  sizes[37] = 40;  sizes[38] = 60;  sizes[39] = 40;  sizes[40] = 40;  sizes[41] = 20;
    sizes[42] = 20;  sizes[43] = 20;  sizes[44] = 20;  sizes[45] = 20;  sizes[46] = 20;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldStyles() throws Exception
  {
    return "MMOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean supplierGetRecToHTML(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, char dispOrEdit, char cad,
                                      byte[] supplierCode, String localDefnsDir, String defnsDir, String errStr, byte[] dataAlready, String imagesDir, int[] bytesOut) throws Exception
  {
    byte[] data = new byte[5000];
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

    byte[] prependCode = new byte[20000];

    boolean rtn=false;

    if(cad == 'A' && supplierCode[0] == '\000')
    {
      messagePage.msgScreen(false, out, req, 6, unm, sid, uty, men, den, dnm, bnm, "5001", "", "", "", imagesDir, localDefnsDir, defnsDir, bytesOut);
      return true;
    }

    getFieldSizes(fieldSizes);

    if(cad == 'A') // not a new one
    {
      boolean ok = false;
      if(dataAlready[0] != '\000') // coming with an err msg - passing-in data already entered but the data's fields may (will) not
      {                            // be in the correct order
        sortFieldData(dataAlready, data);
        ok = true;
      }
      else
      {
        if(getSupplierRecGivenCode(con, stmt, rs, supplierCode, '\001', dnm, data, localDefnsDir, defnsDir) == -1)
          messagePage.msgScreen(false, out, req, 6, unm, sid, uty, men, den, dnm, bnm, "5001", "", "", "", imagesDir, localDefnsDir, defnsDir, bytesOut);
        else ok = true;
      }

      if(ok)
      {
        numFields = screenLayout.layoutToBuffer(buf1, buf2, size1, fieldNames, fieldTypes, fieldSizes, localDefnsDir, defnsDir, "061a.htm", 1, getFieldNames(), fieldSizes, null, null, null, null);

        byte[] ddlData = new byte[1000];
        int[] ddlDataLen  = new int[1]; ddlDataLen[0] = 1000;
        int[] ddlDataUpto = new int[1]; ddlDataUpto[0] = 0;

        ddlData = documentUtils.getCountryDDLData(      con, stmt, rs, "supplier.Country",  ddlData, ddlDataUpto, ddlDataLen);
        ddlData = accountsUtils.getCurrencyNamesDDLData(con, stmt, rs, "supplier.Currency", dnm, ddlData, ddlDataUpto, ddlDataLen, localDefnsDir, defnsDir);

        if(dispOrEdit == 'D')
        {
          prepend(con, stmt, rs, req, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, generalUtils.stringFromBytes(supplierCode, 0L),
                  prependCode, bytesOut);
        }
        else
        {
          prependEdit(con, stmt, rs, errStr, req, unm, sid, uty, men, den, dnm, bnm, cad, generalUtils.stringFromBytes(supplierCode, 0L),
                      localDefnsDir, defnsDir, prependCode, bytesOut);
        }

        // replaces binary-3s in notes with newlines
        byte[] buf = new byte[300];
        generalUtils.replaceThreesWithNewlines(generalUtils.dfsAsStrGivenBinary1(true, data, (short)11), buf);
        generalUtils.repAlphaUsingOnes(data, 5000, "Notes", buf);

        screenLayout.bufferToBuffer(buf1, buf2, size1, size2, source, numFields, fieldNames, fieldTypes, fieldSizes, dispOrEdit, 'H', data, 5000, ddlData, ddlDataUpto[0], null, prependCode);

        append(con, stmt, rs, buf1, buf2, source, size1, size2, unm, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

        screenLayout.bufferToOut(buf1, buf2, source, out);
        rtn = true;
      }
    }
    else // a new layout
    {
      numFields = screenLayout.layoutToBuffer(buf1, buf2, size1, fieldNames, fieldTypes, fieldSizes, localDefnsDir, defnsDir, "061a.htm", 1, getFieldNames(), fieldSizes, null, null, null, null);
      if(dataAlready[0] != '\000') // coming with an err msg - passing-in data already entered (but not yet saved)
        generalUtils.bytesToBytes(data, 0, dataAlready, 0);
      else
      {
        screenLayout.buildNewDetails(fieldNames, numFields, "supplier", data);

        byte[] nextCode = new byte[21];
        documentUtils.getNextCode(con, stmt, rs, "supplier", true, nextCode);
        generalUtils.repAlphaUsingOnes(data, 5000, "SupplierCode", nextCode);

        generalUtils.repAlphaUsingOnes(data, 5000, "InternalOrExternalOrOther", "E");
      }

      byte[] ddlData = new byte[1000];
      int[] ddlDataLen  = new int[1]; ddlDataLen[0] = 1000;
      int[] ddlDataUpto = new int[1]; ddlDataUpto[0] = 0;
      ddlData = documentUtils.getCountryDDLData(      con, stmt, rs, "supplier.Country",      ddlData, ddlDataUpto, ddlDataLen);
      ddlData = accountsUtils.getCurrencyNamesDDLData(con, stmt, rs, "supplier.Currency",     dnm, ddlData, ddlDataUpto, ddlDataLen, localDefnsDir, defnsDir);

      prependEdit(con, stmt, rs, errStr, req, unm, sid, uty, men, den, dnm, bnm, 'C', generalUtils.stringFromBytes(supplierCode, 0L),
                  localDefnsDir, defnsDir, prependCode, bytesOut);

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
  public char supplierPutRec(Connection con, Statement stmt, ResultSet rs, byte[] originalSupplierCode, String dnm, String localDefnsDir, String defnsDir, char cad,
                             byte[] recData, int recDataLen) throws Exception
  {
    byte[] codeBytes = new byte[21];
    return supplierPutRec(con, stmt, rs, originalSupplierCode, dnm, localDefnsDir, defnsDir, cad, recData, recDataLen, codeBytes);
  }
  public char supplierPutRec(Connection con, Statement stmt, ResultSet rs, byte[] originalSupplierCode, String dnm, String localDefnsDir, String defnsDir, char cad,
                             byte[] recData, int recDataLen, byte[] codeBytes) throws Exception
  {
    byte[] b = new byte[21];
    generalUtils.catAsBytes("SupplierCode", 0, b, true);

    if(searchDataString(recData, recDataLen, "supplier", b, codeBytes) == -1)
      return 'N';

    char newOrEdit;

    if(cad == 'A')
      newOrEdit = 'E';
    else
    if(originalSupplierCode[0] == '\000')
      newOrEdit = 'N';
    else  // originalcode not blank
    {
      if(generalUtils.matchIgnoreCase(originalSupplierCode, 0, codeBytes, 0))
        newOrEdit = 'E';
      else // change in the code, or rec with no suppliercode supplied
        newOrEdit = 'N';
    }

    generalUtils.toUpper(codeBytes, 0);

    if(existsSupplierRecGivenCode(con, stmt, rs, codeBytes, dnm, localDefnsDir, defnsDir))
    {
      if(cad != 'A') // amending
        return 'X';
    }

    // get data values from recData and put into buf for updating
    byte[] buf = new byte[2000];

    generalUtils.putAlpha(buf, 2000, (short)0, codeBytes);

    // determines the number of fields and then processes them in order *but* makes no assumptions about order of fields in data

    String fieldNames = getFieldNames();
    byte[] value        = new byte[300];
    byte[] fieldName    = new byte[31];
    byte[] supplierName = new byte[61];  supplierName[0] = '\000';
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

      if(searchDataString(recData, recDataLen, "supplier", fieldName, value) != -1) // entry exists
      {
        if(fieldCount == 1)
        {
          generalUtils.bytesToBytes(supplierName, 0, value, 0);
          generalUtils.repAlpha(buf, 2000, (short)fieldCount, value);
        }
        else
        if(fieldCount == 27) // dateearliestinvoice
        {
          if(value[0] == '\000')
            generalUtils.strToBytes(value, "2099-12-31");

          generalUtils.repAlpha(buf, 2000, (short)27, value);
        }
        else
        if(fieldCount == 10) // notes
        {
          byte[] b2 = new byte[300];
          int i2=0;
          int i=0;
          while(value[i] != '\000')
          {
            if(value[i] == (byte)10 || value[i] == (byte)1)
              ; // ignore
            else b2[i2++] = value[i];
            ++i;
          }
          b2[i2] = '\000';
          generalUtils.putAlpha(buf, 2000, (short)10, b2);
        }
        else generalUtils.repAlpha(buf, 2000, (short)fieldCount, value);
      }
      else
      {
        if(fieldCount == 1)
        {
         generalUtils.repAlpha(buf, 2000, (short)fieldCount, "");
        }
        else
        if(fieldCount == 27) // dateearliestinvoice
          generalUtils.repAlpha(buf, 2000, (short)fieldCount, "2099-12-31");
      }

      ++fieldCount;
    }

    if(putSupplierRecGivenCode(con, stmt, rs, codeBytes, '0', newOrEdit, buf, dnm, localDefnsDir, defnsDir))
    {
    }
    else return 'F';

    return ' ';
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

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getSupplierNameGivenCode(Connection con, Statement stmt, ResultSet rs, String companyCode) throws Exception
  {
    byte[] name = new byte[100];
    byte[] codeB = new byte[21];
    generalUtils.strToBytes(codeB, companyCode);
    getSupplierNameGivenCode(con, stmt, rs, codeB, name);
    return generalUtils.stringFromBytes(name, 0L);
  }
  public void getSupplierNameGivenCode(Connection con, Statement stmt, ResultSet rs, byte[] companyCode, byte[] companyName) throws Exception
  {
    if(companyCode[0] == '\000') // just-in-case
    {
      companyName[0] = '\000';
      return;
    }

    getASupplierFieldGivenCode(con, stmt, rs, "Name", companyCode, companyName);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getASupplierFieldGivenCode(Connection con, Statement stmt, ResultSet rs, String fieldName, String supplierCode) throws Exception
  {
    byte[] value = new byte[300]; // plenty
    byte[] supplierCodeB = new byte[21];
    generalUtils.strToBytes(supplierCodeB, supplierCode);

    getASupplierFieldGivenCode(con, stmt, rs, fieldName, supplierCodeB, value);

    return generalUtils.stringFromBytes(value, 0L);
  }
  public void getASupplierFieldGivenCode(Connection con, Statement stmt, ResultSet rs, String fieldName, byte[] supplierCode, byte[] value)
                                         throws Exception
  {
    if(supplierCode[0] == '\000') // just-in-case
      return;

    generalUtils.toUpper(supplierCode, 0);

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT " + fieldName + " FROM supplier WHERE SupplierCode = '"
                                     + generalUtils.stringFromBytes(supplierCode, 0L) + "'");
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

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // sets : supplier.code=acme\001supplier.name=acme ltd\001...
  // returns: 0 or -1 if rec not found
  public int getSupplierRecGivenCode(Connection con, Statement stmt, ResultSet rs, String supplierCode, char separator, String dnm, byte[] data, String localDefnsDir, String defnsDir) throws Exception
  {
    byte[] supplierCodeB = new byte[21];
    generalUtils.strToBytes(supplierCodeB, supplierCode);

    return getSupplierRecGivenCode(con, stmt, rs, supplierCodeB, separator, dnm, data, localDefnsDir, defnsDir);
  }
  public int getSupplierRecGivenCode(Connection con, Statement stmt, ResultSet rs, byte[] supplierCode, char separator, String dnm, byte[] data, String localDefnsDir, String defnsDir)
                                     throws Exception
  {
    if(supplierCode[0] == '\000') // just-in-case
      return -1;

    generalUtils.toUpper(supplierCode, 0);

    stmt = con.createStatement();
    rs = null;

    rs = stmt.executeQuery("SELECT * FROM supplier WHERE SupplierCode = '" + generalUtils.stringFromBytes(supplierCode, 0L) + "'");
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
        generalUtils.catAsBytes("supplier." + thisFieldName + "=" + getValue(count, thisFieldType, rs, rsmd) + "\001", 0, data, terminate);

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
  public void determineSuppAccCodeFromCompCode(byte[] supplierCode, byte[] fullAccCode) throws Exception
  {
    generalUtils.bytesToBytes(fullAccCode, 0, supplierCode, 0);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean existsSupplierRecGivenCode(Connection con, Statement stmt, ResultSet rs, String supplierCode, String dnm, String localDefnsDir, String defnsDir)
                                            throws Exception
  {
    byte[] b = new byte[100]; // plenty
    generalUtils.strToBytes(b, supplierCode);
    return existsSupplierRecGivenCode(con, stmt, rs, b, dnm, localDefnsDir, defnsDir);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean existsSupplierRecGivenCode(Connection con, Statement stmt, ResultSet rs, byte[] supplierCode, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    if(supplierCode[0] == '\000') // just-in-case
      return false;

    generalUtils.toUpper(supplierCode, 0);

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM supplier WHERE SupplierCode = '" + generalUtils.stringFromBytes(supplierCode, 0L) + "'");
    rs.next();
    int numRecs = rs.getInt("rowcount") ;
    rs.close() ;

    stmt.close() ;

    if(numRecs == 1)
      return true;

    return false;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public int countSuppliers(Connection con, Statement stmt, ResultSet rs) throws Exception
  {
    int numRecs = 0;

    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM supplier");

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
  private void append(Connection con, Statement stmt, ResultSet rs, byte[][] buf1, byte[][] buf2, char[] source, int[] iSize1, int[] iSize2, String unm,
                      String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    byte[] b = new byte[1000];
    scoutln(b, bytesOut, "</form>");
    scoutln(b, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir));
    screenLayout.appendBytesToBuffer(buf1, buf2, source, iSize1, iSize2, b);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void prepend(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, String code, byte[] b,
                       int[] bytesOut) throws Exception
  {
    String code2 = code;

    b[0] = '\000';
    scoutln(b, bytesOut, "<html><head><title>" + code + "</title>");

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 5070, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(b, bytesOut, "<script language=\"JavaScript\">\n");
      scoutln(b, bytesOut, "function fetch(){");
      scoutln(b, bytesOut, "window.location.replace(\"http://" + men + "/central/servlet/SupplierPage?unm=" + unm + "&sid="
                           + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1="
                           + generalUtils.sanitise(code2) + "&p2=&p3=&p4=A\");}\n");

      scoutln(b, bytesOut, "</script>\n");
    }

    String cssDirectory = directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir);
    String cssDocumentDirectory = directoryUtils.getCssDocumentDirectory(con, stmt, rs, dnm);
    scoutln(b, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + cssDirectory + "general.css\">\n");
    scoutln(b, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + cssDocumentDirectory + "document.css\">\n");

    outputPageFrame(con, stmt, rs, b, req, ' ', code2, "", "SupplierPage", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, bytesOut);
    scoutln(b, bytesOut, "<form>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void prependEdit(Connection con, Statement stmt, ResultSet rs, String errStr, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, char cad, String code, String localDefnsDir,
                           String defnsDir, byte[] b, int[] bytesOut) throws Exception
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

    scoutln(b, bytesOut, "<form name=go action=\"SupplierUpdate\" enctype=\"application/x-www-form-urlencoded\" method=POST>");
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

    outputPageFrame(con, stmt, rs, b, req, cad, code2, "", "SupplierUpdate", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, bytesOut);

    if(errStr.length() > 0 && ! errStr.equalsIgnoreCase("null"))
      generalUtils.catAsBytes(errStr, 0, b, false);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean putSupplierRecGivenCode(Connection con, Statement stmt, ResultSet rs, byte[] code, char newOrEdit, byte[] data, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    return putSupplierRecGivenCode(con, stmt, rs, code, '0', newOrEdit, data, dnm, localDefnsDir, defnsDir);
  }
  public boolean putSupplierRecGivenCode(Connection con, Statement stmt, ResultSet rs, byte[] code, char separator, char newOrEdit, byte[] data, String dnm, String localDefnsDir, String defnsDir) throws Exception
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

        q = "INSERT INTO supplier (" + getFieldNames() + ") VALUES (" + opStr + ")";
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

        q = "UPDATE supplier SET " + opStr + " WHERE SupplierCode = '" + generalUtils.stringFromBytes(code, 0L) + "'";
      }

      stmt.executeUpdate(q);

      stmt.close();
    }
    catch(Exception e) { System.out.println(e); return false; }

    return true;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean supplierDeleteRec(Connection con, Statement stmt, ResultSet rs, String supplierCode, String dnm, String localDefnsDir, String defnsDir)
  {
    try
    {
      if(supplierCode.length() == 0) // just-in-case
        return false;

      supplierCode = supplierCode.toUpperCase();

      stmt = con.createStatement();

      stmt.executeUpdate("DELETE FROM supplier WHERE SupplierCode = '" + supplierCode + "'");

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
  // picks-up the list of fieldnames for the supplier file, and fetches the data value for that field; and rebuilds the data string
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

      generalUtils.catAsBytes("supplier." + thisFieldName + "=", 0, opBuf, false);

      if(searchDataString(ipBuf, recDataLen, "supplier", thisFieldNameB, value) != -1) // entry exists
        generalUtils.catAsBytes(generalUtils.stringFromBytes(value, 0L), 0, opBuf, false);

      generalUtils.catAsBytes("\001", 0, opBuf, false);
    }

    generalUtils.bytesToBytes(ipBuf, 0, opBuf, 0);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void outputPageFrame(Connection con, Statement stmt, ResultSet rs, byte[] b, HttpServletRequest req, char cad, String customerCode, String bodyStr, String callingServlet, String unm, String sid, String uty, String men, String den,
                               String dnm, String bnm, String otherSetup, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    int[] hmenuCount = new int[1];

    String subMenuText = dashboardUtils.buildSubMenuText(con, stmt, rs, req, "5001", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount);

    subMenuText += "<table id='title' width=100%><tr><td colspan=2 nowrap><p>Supplier Record" + directoryUtils.buildHelp(5001) + "</td></tr></table>";

    subMenuText += buildSubMenuText(con, stmt, rs, req, cad, callingServlet, customerCode, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount);

    scoutln(b, bytesOut, authenticationUtils.buildScreen(con, stmt, rs, req, true, unm, sid, uty, men, den, dnm, bnm, callingServlet, subMenuText, hmenuCount[0], bodyStr, otherSetup, localDefnsDir, defnsDir));

    scoutln(b, bytesOut, "<table border=0><tr><td valign=top><div id='second' style=\"{margin-top:0px;}\"></div></td>");
    scoutln(b, bytesOut, "<td valign=top><div id='third' style=\"{margin-top:0px;margin-left:0px;}\"></div></td></tr></table>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String buildSubMenuText(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, char cad, String callingServlet, String supplierCode, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                                  String localDefnsDir, String defnsDir, int[] hmenuCount) throws Exception
  {
    String s = "<div id='submenu'>";

    ++hmenuCount[0];

    if(callingServlet.equals("SupplierPage"))
      s += drawOptions5001(con, stmt, rs, req, hmenuCount, supplierCode, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir);
    else
    if(callingServlet.equals("SupplierUpdate"))
      s += drawOptions5071(hmenuCount, cad);

    s += "</div>";

    --hmenuCount[0];

    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String drawOptions5001(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, int[] hmenuCount, String supplierCode, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                                 String localDefnsDir, String defnsDir) throws Exception
  {
    String s = "";

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 5002, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\">";
      s += "<a href=\"/central/servlet/SupplierSettlementHistoryInput?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=" + supplierCode + " &p2=N&bnm=" + bnm + "\">Settlement</a></dt></dl>";
    }

    String[] latitude  = new String[1];
    String[] longitude = new String[1];
    String[] address   = new String[1];

    getGoogleMapDataGivenCode(con, stmt, rs, supplierCode, dnm, localDefnsDir, defnsDir, latitude, longitude, address);

    if(latitude[0].length() != 0 && longitude[0].length() != 0)
    {
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\">";
      s += "<a href=\"/central/servlet/GoogleMapServices?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p2=" + supplierCode + " &p1=S&bnm=" + bnm + "\">Location</a></dt></dl>";
    }

    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String drawOptions5071(int[] hmenuCount, char cad) throws Exception
  {
    String s = "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\"><a href=\"javascript:save(' ')\">Save Changes</a></dt></dl>";

    if(cad != 'C')
    {
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\">";
      s += "<a href=\"javascript:save('X')\">Cancel Changes</a></dt></dl>";
    }

    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void updatedDateInvoicePaid(Connection con, Statement stmt, ResultSet rs, String date, String companyCode, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    stmt = con.createStatement();

    stmt.executeUpdate("UPDATE supplier SET DateEarliestInvoice = {d '" + date + "'} WHERE SupplierCode = '" + companyCode + "'");

    stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getSupplierCurrencyGivenCode(Connection con, Statement stmt, ResultSet rs, String companyCode) throws Exception
  {
    byte[] currency = new byte[10];
    byte[] codeB = new byte[21];
    generalUtils.strToBytes(codeB, companyCode);
    getSupplierCurrencyGivenCode(con, stmt, rs, codeB, currency);
    return generalUtils.stringFromBytes(currency, 0L);
  }
  public void getSupplierCurrencyGivenCode(Connection con, Statement stmt, ResultSet rs, byte[] companyCode, byte[] currency) throws Exception
  {
    if(companyCode[0] == '\000') // just-in-case
    {
      currency[0] = '\000';
      return;
    }

    getASupplierFieldGivenCode(con, stmt, rs, "Currency", companyCode, currency);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(byte[] b, int bytesOut[], String str) throws Exception
  {
    generalUtils.catAsBytes(str, 0, b, false);
    bytesOut[0] += (str.length() + 2);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void getGoogleMapDataGivenCode(Connection con, Statement stmt, ResultSet rs, String companyCode, String dnm, String localDefnsDir, String defnsDir, String[] latitude, String[] longitude, String[] address)
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
                           + "FROM supplier WHERE SupplierCode = '" + companyCode + "'");
      if(rs.next())
      {
        latitude[0]  = rs.getString(1);
        longitude[0] = rs.getString(2);
        address[0]   = rs.getString(3) + "<br>" + rs.getString(4) + "<br>" + rs.getString(5) + "<br>" + rs.getString(6) + "<br>" + rs.getString(7)
                     + "<br>" + rs.getString(8)  + "<br>" + rs.getString(9);
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
