// =======================================================================================================================================================================================================
// System: ZaraStar Document: Misc Record Access
// Module: DocumentUtils.java
// Author: C.K.Harvey
// Copyright (c) 2001-09 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
// =======================================================================================================================================================================================================
 
package org.zarastar.zarastar;

import java.sql.*;

public class DocumentUtils
{
  GeneralUtils generalUtils = new GeneralUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  Profile profile = new Profile();

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getTableCreateStringSalesper() throws Exception
  {
    return "salesper ( Name char(40) not null, Type char(1) not null, Note char(80), unique(Name))";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStringsSalesper(String[] s) throws Exception
  {
    s[0]="\0"; // remove compiler warning
    return 0;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNamesSalesper() throws Exception
  {
    return "Name, Type, Note";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypesSalesper() throws Exception
  {
    return "CCC";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizesSalesper(short[] sizes) throws Exception
  {
    sizes[0] = 40;   sizes[1] = 1;    sizes[2] = 80;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldStylesSalesper() throws Exception
  {
    return "MMO";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getTableCreateStringManagepeople() throws Exception
  {
    return "managepeople ( UserCode char(20) not null, SalesPerson char(40))";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStringsManagepeople(String[] s) throws Exception
  {
    return 0;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNamesManagepeople() throws Exception
  {
    return "userCode, SalesPerson";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypesManagepeople() throws Exception
  {
    return "CC";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizesManagepeople(short[] sizes) throws Exception
  {
    sizes[0] = 20;   sizes[1] = 40;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldStylesManagepeople() throws Exception
  {
    return "MM";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getTableCreateStringDriver() throws Exception
  {
    return "driver ( Name char(40) not null, Type char(1) not null, Note char(80), unique(Name))";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStringsDriver(String[] s) throws Exception
  {
    return 0;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNamesDriver() throws Exception
  {
    return "Name, Type, Note";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypesDriver() throws Exception
  {
    return "CCC";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizesDriver(short[] sizes) throws Exception
  {
    sizes[0] = 40;   sizes[1] = 1;    sizes[2] = 80;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldStylesDriver() throws Exception
  {
    return "MMO";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getTableCreateStringStoreman() throws Exception
  {
    return "storeman ( Name char(40) not null, Type char(1) not null, Note char(80), unique(Name))";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStringsStoreman(String[] s) throws Exception
  {
    return 0;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNamesStoreman() throws Exception
  {
    return "Name, Type, Note";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypesStoreman() throws Exception
  {
    return "CCC";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizesStoreman(short[] sizes) throws Exception
  {
    sizes[0] = 40;   sizes[1] = 1;    sizes[2] = 80;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldStylesStoreman() throws Exception
  {
    return "MMO";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getTableCreateStringCodes() throws Exception
  {  
    return "codes ( Name char(40) not null, ShortName char(20) not null, NextCode integer not null, Format char(20) not null, unique(Name))";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStringsCodes(String[] s) throws Exception
  {
    s[0]="\0"; // remove compiler warning
    return 0;
  }
      
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNamesCodes() throws Exception
  {  
    return "Name, ShortName, NextCode, Format";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypesCodes() throws Exception
  {
    return "CCNC";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizesCodes(short[] sizes) throws Exception
  {
    sizes[0] = 40;   sizes[1] = 20;    sizes[2] = 0;    sizes[3] = 20; 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldStylesCodes() throws Exception
  {
    return "MMMM";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getTableCreateStringLocks() throws Exception
  {  
    return "locks ( DocumentName char(40) not null, DocumentAbbrev char(20) not null, LockedUpto date, OpenTo char(250),"
          + " unique(DocumentName))";
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStringsLocks(String[] s) throws Exception
  {
    return 0;
  }
      
  // -------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNamesLocks() throws Exception
  {  
    return "DocumentName, DocumentAbbrev, LockedUpto, OpenTo";
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypesLocks() throws Exception
  {
    return "CCDC";
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizesLocks(short[] sizes) throws Exception
  {
    sizes[0] = 40;   sizes[1] = 20;    sizes[2] = 0;    sizes[3] = 250; 
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public String getFieldStylesLocks() throws Exception
  {
    return "MMMM";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getTableCreateStringCSSstyles() throws Exception
  { 
    return "cssstyles ( UserCode char(20), Stylename char(40),"
          + " unique(UserCode))";
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStringsCSSstyles(String[] s) throws Exception
  {
    return 0;
  }
      
  // -------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNamesCSSstyles() throws Exception
  {  
    return "UserCode, StyleName";
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypesCSSstyles() throws Exception
  {
    return "CC";
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizesCSSstyles(short[] sizes) throws Exception
  {
    sizes[0] = 20;   sizes[1] = 40; 
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public String getFieldStylesCSSstyles() throws Exception
  {
    return "MM";
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public String getTableCreateStringCountry() throws Exception
  {  
    return "country ( Name char(60) not null, Position integer, unique(Name) )";
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStringsCountry(String[] s) throws Exception
  {
    s[0]="\0"; // remove compiler warning
    return 0;
  }
      
  // -------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNamesCountry() throws Exception
  {  
    return "Name, Position";
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypesCountry() throws Exception
  {
    return "CI";
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizesCountry(short[] sizes) throws Exception
  {
    sizes[0] = 60;  sizes[1] = 0; 
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public String getFieldStylesCountry() throws Exception
  {
    return "MM";
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public String getTableCreateStringCompanyType() throws Exception
  {  
    return "companytype ( Name char(60) not null, unique(Name) )";
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStringsCompanyType(String[] s) throws Exception
  {
    s[0]="\0"; // remove compiler warning
    return 0;
  }
      
  // -------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNamesCompanyType() throws Exception
  {  
    return "Name";
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypesCompanyType() throws Exception
  {
    return "C";
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizesCompanyType(short[] sizes) throws Exception
  {
    sizes[0] = 60; 
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public String getFieldStylesCompanyType() throws Exception
  {
    return "M";
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public String getTableCreateStringIndustryType() throws Exception
  {  
    return "industrytype ( Name char(60) not null, unique(Name) )";
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStringsIndustryType(String[] s) throws Exception
  {
    s[0]="\0"; // remove compiler warning
    return 0;
  }
      
  // -------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNamesIndustryType() throws Exception
  {  
    return "Name";
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypesIndustryType() throws Exception
  {
    return "C";
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizesIndustryType(short[] sizes) throws Exception
  {
    sizes[0] = 60; 
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public String getFieldStylesIndustryType() throws Exception
  {
    return "M";
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public String getTableCreateStringStore() throws Exception
  {  
    return "store ( Name char(20) not null, Type char(1) not null, unique(Name) )";
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStringsStore(String[] s) throws Exception
  {
    s[0]="\0"; // remove compiler warning
    return 0;
  }
      
  // -------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNamesStore() throws Exception
  {  
    return "Name, Type";
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypesStore() throws Exception
  {
    return "CC";
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizesStore(short[] sizes) throws Exception
  {
    sizes[0] = 20;  sizes[1] = 1; 
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public String getFieldStylesStore() throws Exception
  {
    return "MM";
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public String getTableCreateStringQuoLike() throws Exception
  {  
    return "quolike ( Name char(40) not null, unique(Name) )";
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStringsQuoLike(String[] s) throws Exception
  {
    s[0]="\0"; // remove compiler warning
    return 0;
  }
      
  // -------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNamesQuoLike() throws Exception
  {  
    return "Name";
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypesQuoLike() throws Exception
  {
    return "C";
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizesQuoLike(short[] sizes) throws Exception
  {
    sizes[0] = 40; 
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public String getFieldStylesQuoLike() throws Exception
  {
    return "M";
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public String getTableCreateStringQuoState() throws Exception
  {  
    return "quostate ( Name char(40) not null, Type char(1), unique(Name) )";
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStringsQuoState(String[] s) throws Exception
  {
    s[0]="\000"; // remove compiler warning
    return 0;
  }
      
  // -------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNamesQuoState() throws Exception
  {  
    return "Name, Type";
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypesQuoState() throws Exception
  {
    return "CC";
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizesQuoState(short[] sizes) throws Exception
  {
    sizes[0] = 40;  sizes[1] = 1; 
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public String getFieldStylesQuoState() throws Exception
  {
    return "MO";
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public String getTableCreateStringQuoReasons() throws Exception
  {  
    return "quoreasons ( Reason1 char(40), Reason2 char(40), Reason3 char(40), Reason4 char(40), Reason5 char(40), "
          + "Reason6 char(40), Reason7 char(40), Reason8 char(40), Reason9 char(40), Reason10 char(40) )";
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStringsQuoReasons(String[] s) throws Exception
  {
    s[0]="\000"; // remove compiler warning
    return 0;
  }
      
  // -------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNamesQuoReasons() throws Exception
  {  
    return "Reason1, Reason2, Reason3, Reason4, Reason5, Reason6, Reason7, Reason8, Reason9, Reason10";
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypesQuoReasons() throws Exception
  {
    return "CCCCCCCCCC";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizesQuoReasons(short[] sizes) throws Exception
  {
    sizes[0] = 40;  sizes[1] = 40;  sizes[2] = 40;  sizes[3] = 40;  sizes[4] = 40;  sizes[5] = 40;  sizes[6] = 40;  sizes[7] = 40;
    sizes[8] = 40;  sizes[9] = 40; 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldStylesQuoReasons() throws Exception
  {
    return "OOOOOOOOOO";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  public void getNextCode(Connection con, Statement stmt, ResultSet rs, String document, boolean incrementNow, byte[] nextCode) throws Exception
  {
    try
    {
      String value, format;
      stmt = con.createStatement();
      rs = stmt.executeQuery("SELECT NextCode, Format FROM codes WHERE shortName = '" + document + "'"); 
      if(rs.next()) // just-in-case
      {
        value = rs.getString(1);
        format= rs.getString(2);

        rs.close();
        stmt.close();
      }
      else
      {
        rs.close();
        stmt.close();
    
        stmt = con.createStatement();
        
        stmt.executeUpdate("INSERT INTO codes ( Name, ShortName, NextCode, Format) VALUES ('" + document + "','" + document + "','1','??????')");

        stmt.close();

        value  = "1";
        format = "??????";
      }
      
      byte[] valueB = new byte[20];

      formatValue(value, format, valueB, nextCode);

      if(incrementNow)
      {
        int intValue = generalUtils.intFromBytesCharFormat(valueB, (short)0);
        ++intValue;
        String newValue = generalUtils.intToStr(intValue);
        
        stmt = con.createStatement();
        stmt.executeUpdate("UPDATE codes SET NextCode = '" + newValue + "' WHERE shortName = '" + document + "'");

        stmt.close();
      }
    }
    catch(Exception e)
    {
      System.out.println("DocumentUtils: " + e);
      try
      {
        if(rs   != null) rs.close();     
        if(stmt != null) stmt.close();
      }
      catch(Exception e2) { }
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  public void formatValue(String v, String f, byte[] value, byte[] nextCode) throws Exception
  {
    generalUtils.stringToBytes(v, 0, value);

    byte[] format = new byte[21];
    generalUtils.stringToBytes(f, 0, format);

    if(value[0] == '\000') // no value either (!)
    {
      value[0] = '1';
      value[1] = '\000';
    }

    int x;
    for(x=0;x<20;++x)
      nextCode[x] = '\000';
    if(format[0] == '\000') // no format specified
      System.arraycopy(value, 0, nextCode, 0, (generalUtils.lengthBytes(value, 0) + 1));
    else // format specified
    {
      byte[] codeBit = new byte[20];
      x=0; int z=0, y, i;
      int lenValue, lenCodeBit;
      int lenFormat = generalUtils.lengthBytes(format, 0);
      while(x < lenFormat)
      {
        switch(format[x])
        {
          case '*' :
           case '?' : y=0;
                      while(x < lenFormat && ( format[x] == '*' || format[x] == '?' ) )
                        codeBit[y++] += format[x++];
                      --x;
                      lenValue   = generalUtils.lengthBytes(value, 0);
                     lenCodeBit = y;
                     if(lenValue > lenCodeBit)
                     {
                       value[0] = '1';
                       value[1] = '\000';
                       lenValue = 1;
                     }
                     System.arraycopy(value, 0, codeBit, (lenCodeBit - lenValue), (generalUtils.lengthBytes(value, 0) + 1));
                     i=0;
                     while(codeBit[i] == '*')
                       ++i;
                     y=0;
                     while(codeBit[y] == '?')
                       codeBit[y++] = '0'; // leading zeros
                     System.arraycopy(codeBit, i, nextCode, z, (generalUtils.lengthBytes(codeBit, 0) - i));
                     z = generalUtils.lengthBytes(nextCode, 0);
                     break;
          default : // other fixed char
                     nextCode[z++] = format[x];
                     break;
        }

        ++x;
      }

      nextCode[z] = '\000';
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public byte[] getShippersDDLData(Connection con, Statement stmt, ResultSet rs, String fieldName, byte[] ddlData, int[] ddlDataUpto,
                                   int[] ddlDataLen) throws Exception
  {
    String name;
      
    name = "Company";
 
    if((ddlDataUpto[0] + 100) >= ddlDataLen[0])
    {
      byte[] tmp = new byte[ddlDataLen[0]];
      System.arraycopy(ddlData, 0, tmp, 0, ddlDataLen[0]);
      ddlDataLen[0] += 1000;
      ddlData = new byte[ddlDataLen[0]];
      System.arraycopy(tmp, 0, ddlData, 0, ddlDataLen[0] - 1000);
    }

    ddlDataUpto[0] = generalUtils.stringIntoBytes(fieldName + "=\"", 0, ddlData, ddlDataUpto[0]);
    ddlDataUpto[0] = generalUtils.stringIntoBytes(name, 0, ddlData, ddlDataUpto[0]);
    ddlDataUpto[0] = generalUtils.stringIntoBytes("\" ", 0, ddlData, ddlDataUpto[0]);
            
    name = "DHL";
 
    if((ddlDataUpto[0] + 100) >= ddlDataLen[0])
    {
      byte[] tmp = new byte[ddlDataLen[0]];
      System.arraycopy(ddlData, 0, tmp, 0, ddlDataLen[0]);
      ddlDataLen[0] += 1000;
      ddlData = new byte[ddlDataLen[0]];
      System.arraycopy(tmp, 0, ddlData, 0, ddlDataLen[0] - 1000);
    }

    ddlDataUpto[0] = generalUtils.stringIntoBytes(fieldName + "=\"", 0, ddlData, ddlDataUpto[0]);
    ddlDataUpto[0] = generalUtils.stringIntoBytes(name, 0, ddlData, ddlDataUpto[0]);
    ddlDataUpto[0] = generalUtils.stringIntoBytes("\" ", 0, ddlData, ddlDataUpto[0]);
    
    name = "FedEx";
 
    if((ddlDataUpto[0] + 100) >= ddlDataLen[0])
    {
      byte[] tmp = new byte[ddlDataLen[0]];
      System.arraycopy(ddlData, 0, tmp, 0, ddlDataLen[0]);
      ddlDataLen[0] += 1000;
      ddlData = new byte[ddlDataLen[0]];
      System.arraycopy(tmp, 0, ddlData, 0, ddlDataLen[0] - 1000);
    }

    ddlDataUpto[0] = generalUtils.stringIntoBytes(fieldName + "=\"", 0, ddlData, ddlDataUpto[0]);
    ddlDataUpto[0] = generalUtils.stringIntoBytes(name, 0, ddlData, ddlDataUpto[0]);
    ddlDataUpto[0] = generalUtils.stringIntoBytes("\" ", 0, ddlData, ddlDataUpto[0]);
    
    name = "UPS";
 
    if((ddlDataUpto[0] + 100) >= ddlDataLen[0])
    {
      byte[] tmp = new byte[ddlDataLen[0]];
      System.arraycopy(ddlData, 0, tmp, 0, ddlDataLen[0]);
      ddlDataLen[0] += 1000;
      ddlData = new byte[ddlDataLen[0]];
      System.arraycopy(tmp, 0, ddlData, 0, ddlDataLen[0] - 1000);
    }

    ddlDataUpto[0] = generalUtils.stringIntoBytes(fieldName + "=\"", 0, ddlData, ddlDataUpto[0]);
    ddlDataUpto[0] = generalUtils.stringIntoBytes(name, 0, ddlData, ddlDataUpto[0]);
    ddlDataUpto[0] = generalUtils.stringIntoBytes("\" ", 0, ddlData, ddlDataUpto[0]);
    
    name = "Other";
 
    if((ddlDataUpto[0] + 100) >= ddlDataLen[0])
    {
      byte[] tmp = new byte[ddlDataLen[0]];
      System.arraycopy(ddlData, 0, tmp, 0, ddlDataLen[0]);
      ddlDataLen[0] += 1000;
      ddlData = new byte[ddlDataLen[0]];
      System.arraycopy(tmp, 0, ddlData, 0, ddlDataLen[0] - 1000);
    }

    ddlDataUpto[0] = generalUtils.stringIntoBytes(fieldName + "=\"", 0, ddlData, ddlDataUpto[0]);
    ddlDataUpto[0] = generalUtils.stringIntoBytes(name, 0, ddlData, ddlDataUpto[0]);
    ddlDataUpto[0] = generalUtils.stringIntoBytes("\" ", 0, ddlData, ddlDataUpto[0]);
    
    return ddlData;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public byte[] getSalesPersonDDLData(Connection con, Statement stmt, ResultSet rs, String fieldName, byte[] ddlData, int[] ddlDataUpto, int[] ddlDataLen) throws Exception
  {
    try
    {
      String name;

      stmt = con.createStatement();
 
      rs = stmt.executeQuery("SELECT UserCode FROM profilesd WHERE UserBasis = 'E' AND IsSalesPerson = 'Y' ORDER BY UserCode");

      String userName;
  
      while(rs.next())
      {
        name = rs.getString(1);

        userName = profile.getUserNameFromProfilesGivenUserCodeAndStatus(con, stmt, rs, rs.getString(1), true);
        
        if(userName.length() > 0)
        {
          if((ddlDataUpto[0] + 100) >= ddlDataLen[0])
          {
            byte[] tmp = new byte[ddlDataLen[0]];
            System.arraycopy(ddlData, 0, tmp, 0, ddlDataLen[0]);
            ddlDataLen[0] += 1000;
            ddlData = new byte[ddlDataLen[0]];
            System.arraycopy(tmp, 0, ddlData, 0, ddlDataLen[0] - 1000);
          }

          ddlDataUpto[0] = generalUtils.stringIntoBytes(fieldName + "=\"", 0, ddlData, ddlDataUpto[0]);
          ddlDataUpto[0] = generalUtils.stringIntoBytes(name, 0, ddlData, ddlDataUpto[0]);
          ddlDataUpto[0] = generalUtils.stringIntoBytes("\" ", 0, ddlData, ddlDataUpto[0]);
        }
      }

      rs.close();
      stmt.close();

      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT UserCode FROM profilesd WHERE UserBasis = 'E' AND IsSalesPerson = 'Y' ORDER BY UserCode");

      while(rs.next())
      {
        name = rs.getString(1);

        userName = profile.getUserNameFromProfilesGivenUserCodeAndStatus(con, stmt, rs, rs.getString(1), false);
        
        if(userName.length() > 0)
        {
          if((ddlDataUpto[0] + 100) >= ddlDataLen[0])
          {
            byte[] tmp = new byte[ddlDataLen[0]];
            System.arraycopy(ddlData, 0, tmp, 0, ddlDataLen[0]);
            ddlDataLen[0] += 1000;
            ddlData = new byte[ddlDataLen[0]];
            System.arraycopy(tmp, 0, ddlData, 0, ddlDataLen[0] - 1000);
          }

          ddlDataUpto[0] = generalUtils.stringIntoBytes(fieldName + "=\"", 0, ddlData, ddlDataUpto[0]);
          ddlDataUpto[0] = generalUtils.stringIntoBytes(name, 0, ddlData, ddlDataUpto[0]);
          ddlDataUpto[0] = generalUtils.stringIntoBytes("\" ", 0, ddlData, ddlDataUpto[0]);
        }
      }

      rs.close();
      stmt.close();

      return ddlData;
    }
    catch(Exception e)
    {
      System.out.println("DocumentUtils: " + e);
      try
      {
        if(rs   != null) rs.close();
        if(stmt != null) stmt.close();
      }
      catch(Exception e2) { }
    }

    return ddlData;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public int getManagedPeople(String userCode, String dnm, String[] names) throws Exception
  {
    names[0] = "";
    int numNames = 0;

    Connection con  = null;
    Statement  stmt = null;
    ResultSet  rs   = null;

    try
    {
      String userName = directoryUtils.getMySQLUserName();
      String passWord = directoryUtils.getMySQLPassWord();
      Class.forName("com.mysql.jdbc.Driver").newInstance();
      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + userName + "&password=" + passWord);

      stmt = con.createStatement();
      rs = stmt.executeQuery("SELECT SalesPerson FROM managepeople WHERE UserCode = '" + userCode + "'");

      while(rs.next())
      {
        names[0] += (rs.getString(1) + "\001");
        ++numNames;
      }

      rs.close();
      stmt.close();
    }
    catch(Exception e)
    {
      System.out.println("DocumentUtils: " + e);
      try
      {
        if(rs   != null) rs.close();
        if(stmt != null) stmt.close();
      }
      catch(Exception e2) { }
    }

    if(con != null) con.close();

    return numNames;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public byte[] getDeliveryDriverDDLData(Connection con, Statement stmt, ResultSet rs, String fieldName, byte[] ddlData, int[] ddlDataUpto, int[] ddlDataLen) throws Exception
  {
    try
    {
      String name;

      stmt = con.createStatement();
      rs = stmt.executeQuery("SELECT Name FROM driver WHERE Type = 'A' ORDER BY Name");
      while(rs.next())
      {
        name = rs.getString(1);

       if((ddlDataUpto[0] + 100) >= ddlDataLen[0])
        {
          byte[] tmp = new byte[ddlDataLen[0]];
          System.arraycopy(ddlData, 0, tmp, 0, ddlDataLen[0]);
          ddlDataLen[0] += 1000;
          ddlData = new byte[ddlDataLen[0]];
          System.arraycopy(tmp, 0, ddlData, 0, ddlDataLen[0] - 1000);
        }

        ddlDataUpto[0] = generalUtils.stringIntoBytes(fieldName + "=\"", 0, ddlData, ddlDataUpto[0]);
        ddlDataUpto[0] = generalUtils.stringIntoBytes(name, 0, ddlData, ddlDataUpto[0]);
        ddlDataUpto[0] = generalUtils.stringIntoBytes("\" ", 0, ddlData, ddlDataUpto[0]);
      }

      rs.close();
      stmt.close();

      stmt = con.createStatement();
      rs = stmt.executeQuery("SELECT Name FROM driver WHERE Type != 'A' ORDER BY Name");
      while(rs.next())
      {
        name = rs.getString(1);

        if((ddlDataUpto[0] + 100) >= ddlDataLen[0])
        {
          byte[] tmp = new byte[ddlDataLen[0]];
          System.arraycopy(ddlData, 0, tmp, 0, ddlDataLen[0]);
          ddlDataLen[0] += 1000;
          ddlData = new byte[ddlDataLen[0]];
          System.arraycopy(tmp, 0, ddlData, 0, ddlDataLen[0] - 1000);
        }

        ddlDataUpto[0] = generalUtils.stringIntoBytes(fieldName + "=\"", 0, ddlData, ddlDataUpto[0]);
        ddlDataUpto[0] = generalUtils.stringIntoBytes(name, 0, ddlData, ddlDataUpto[0]);
        ddlDataUpto[0] = generalUtils.stringIntoBytes("\" ", 0, ddlData, ddlDataUpto[0]);
      }

      rs.close();
      stmt.close();

      return ddlData;
    }
    catch(Exception e)
    {
      System.out.println("DocumentUtils: " + e);
      try
      {
        if(rs   != null) rs.close();
        if(stmt != null) stmt.close();
      }
      catch(Exception e2) { }
    }

    return ddlData;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getStoremanDDL(Connection con, Statement stmt, ResultSet rs, boolean includeSelect) throws Exception
  {
    String s = "";

    if(includeSelect)
      s += "<select name='storeman'>";

    try
    {
      String name;

      stmt = con.createStatement();
      rs = stmt.executeQuery("SELECT Name FROM storeman WHERE Type = 'A' ORDER BY Name\n");
      while(rs.next())
      {
        name = rs.getString(1);
        s += "<option value=\"" + name + "\">" + name;
      }
    }
    catch(Exception e)
    {
      System.out.println("DocumentUtils: " + e);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    if(includeSelect)
      s += "</select>";

    return s;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public int getSalesPersonNames(Connection con, Statement stmt, ResultSet rs, String[][] names) throws Exception
  {
    try
    {
      int count = 0;
      
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT COUNT(1) AS rowcount FROM profilesd WHERE UserBasis = 'E' AND IsSalesPerson = 'Y'");

      rs.next();

      int numNames = rs.getInt("rowcount") ;

      rs.close();
      stmt.close();
        
      names[0] = new String[numNames];
      
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT UserCode FROM profilesd WHERE UserBasis = 'E' AND IsSalesPerson = 'Y'");
      while(rs.next())
        names[0][count++] = rs.getString(1);
      
      rs.close();
      stmt.close();

      return count;
    }
    catch(Exception e)
    {
      System.out.println("DocumentUtils: " + e);
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
  public byte[] getStoreDDLData(Connection con, Statement stmt, ResultSet rs, String fieldName, byte[] ddlData, int[] ddlDataUpto, int[] ddlDataLen) throws Exception
  {
    try
    {
      String name;
      
      stmt = con.createStatement();
      rs = stmt.executeQuery("SELECT Name FROM store ORDER BY Type");
      while(rs.next())
      {
        name = rs.getString(1);
        if((ddlDataUpto[0] + 100) >= ddlDataLen[0])
        {
          byte[] tmp = new byte[ddlDataLen[0]];
          System.arraycopy(ddlData, 0, tmp, 0, ddlDataLen[0]);
          ddlDataLen[0] += 1000;
          ddlData = new byte[ddlDataLen[0]];
          System.arraycopy(tmp, 0, ddlData, 0, ddlDataLen[0] - 1000);
        }

        ddlDataUpto[0] = generalUtils.stringIntoBytes(fieldName + "=\"", 0, ddlData, ddlDataUpto[0]);
        ddlDataUpto[0] = generalUtils.stringIntoBytes(name, 0, ddlData, ddlDataUpto[0]);
        ddlDataUpto[0] = generalUtils.stringIntoBytes("\" ", 0, ddlData, ddlDataUpto[0]);
      }
      
      rs.close();
      stmt.close();
            
      return ddlData;
    }
    catch(Exception e)
    {
      System.out.println("DocumentUtils: " + e);
      try
      {
        if(rs   != null) rs.close();     
        if(stmt != null) stmt.close();
      }
      catch(Exception e2) { }
    }
    
    return ddlData;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public byte[] getStoreList(Connection con, Statement stmt, ResultSet rs, byte[] list, int[] listLen, int[] numEntries) throws Exception
  {
    try
    {
      String name;
      int listUpto = 0;
      numEntries[0] = 0;
      
      stmt = con.createStatement();
      rs = stmt.executeQuery("SELECT Name FROM store ORDER BY Type");
      while(rs.next())
      {
        name = rs.getString(1);
        if((listUpto + 100) >= listLen[0])
        {
          byte[] tmp = new byte[listLen[0]];
          System.arraycopy(list, 0, tmp, 0, listLen[0]);
          listLen[0] += 1000;
          list = new byte[listLen[0]];
          System.arraycopy(tmp, 0, list, 0, listLen[0] - 1000);
        }

        listUpto = generalUtils.stringIntoBytes(name + "\001", 0, list, listUpto);
        ++numEntries[0];
      }
      
      rs.close();
      stmt.close();
            
      return list;
    }
    catch(Exception e)
    {
      System.out.println("DocumentUtils: " + e);
      try
      {
        if(rs   != null) rs.close();     
        if(stmt != null) stmt.close();
      }
      catch(Exception e2) { }
    }
    
    return list;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getStoresDDL(Connection con, Statement stmt, ResultSet rs, String selectName) throws Exception
  {
    return getStoresDDL(con, stmt, rs, selectName, "");
  }
  public String getStoresDDL(Connection con, Statement stmt, ResultSet rs, String selectName, String insertOption) throws Exception
  {
    String s = "<select name=\"" + selectName + "\">";

    s += (insertOption + "\n");      

    try
    {
      String name;
      stmt = con.createStatement();
      rs = stmt.executeQuery("SELECT Name FROM store ORDER BY Type");
      while(rs.next())
      {
        name = rs.getString(1);

        s += "<option value=\"" + name + "\">" + name + "\n";      
      }
      
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
            
      s += "</select>";
      return s;
    }
    catch(Exception e)
    {
      System.out.println("DocumentUtils: " + e);
      try
      {
        if(rs   != null) rs.close();     
        if(stmt != null) stmt.close();
      }
      catch(Exception e2) { }
    }
    
    s += "</select>";
    return s;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public byte[] getCompanyTypeDDLData(Connection con, Statement stmt, ResultSet rs, String fieldName, byte[] ddlData, int[] ddlDataUpto,
                                      int[] ddlDataLen) throws Exception
  {
    try
    {
      String name;
      
      stmt = con.createStatement();
      rs = stmt.executeQuery("SELECT Name FROM companytype"); 
      while(rs.next())
      {
        name = rs.getString(1);
        if((ddlDataUpto[0] + 100) >= ddlDataLen[0])
        {
          byte[] tmp = new byte[ddlDataLen[0]];
          System.arraycopy(ddlData, 0, tmp, 0, ddlDataLen[0]);
          ddlDataLen[0] += 1000;
          ddlData = new byte[ddlDataLen[0]];
          System.arraycopy(tmp, 0, ddlData, 0, ddlDataLen[0] - 1000);
        }

        ddlDataUpto[0] = generalUtils.stringIntoBytes(fieldName + "=\"", 0, ddlData, ddlDataUpto[0]);
        ddlDataUpto[0] = generalUtils.stringIntoBytes(name, 0, ddlData, ddlDataUpto[0]);
        ddlDataUpto[0] = generalUtils.stringIntoBytes("\" ", 0, ddlData, ddlDataUpto[0]);
      }
      
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
            
      return ddlData;
    }
    catch(Exception e)
    {
      System.out.println("DocumentUtils: " + e);
      try
      {
        if(rs   != null) rs.close();     
        if(stmt != null) stmt.close();
      }
      catch(Exception e2) { }
    }
    
    return ddlData;
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public byte[] getIndustryTypeDDLData(Connection con, Statement stmt, ResultSet rs, String fieldName, byte[] ddlData, int[] ddlDataUpto,
                                       int[] ddlDataLen) throws Exception
  {
    try
    {
      String name;
      
      stmt = con.createStatement();
      rs = stmt.executeQuery("SELECT Name FROM industrytype"); 
      while(rs.next())
      {
        name = rs.getString(1);
        if((ddlDataUpto[0] + 100) >= ddlDataLen[0])
        {
          byte[] tmp = new byte[ddlDataLen[0]];
          System.arraycopy(ddlData, 0, tmp, 0, ddlDataLen[0]);
          ddlDataLen[0] += 1000;
          ddlData = new byte[ddlDataLen[0]];
          System.arraycopy(tmp, 0, ddlData, 0, ddlDataLen[0] - 1000);
        }

        ddlDataUpto[0] = generalUtils.stringIntoBytes(fieldName + "=\"", 0, ddlData, ddlDataUpto[0]);
        ddlDataUpto[0] = generalUtils.stringIntoBytes(name, 0, ddlData, ddlDataUpto[0]);
        ddlDataUpto[0] = generalUtils.stringIntoBytes("\" ", 0, ddlData, ddlDataUpto[0]);
      }
      
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
            
      return ddlData;
    }
    catch(Exception e)
    {
      System.out.println("DocumentUtils: " + e);
      try
      {
        if(rs   != null) rs.close();     
        if(stmt != null) stmt.close();
      }
      catch(Exception e2) { }
    }
    
    return ddlData;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  public byte[] getCountryDDLData(Connection con, Statement stmt, ResultSet rs, String fieldName, byte[] ddlData, int[] ddlDataUpto, int[] ddlDataLen)
                                  throws Exception
  {
    try
    {
      String name;
      
      stmt = con.createStatement();
      rs = stmt.executeQuery("SELECT Name FROM country ORDER BY Position"); 
      while(rs.next())
      {
        name = rs.getString(1);
        if((ddlDataUpto[0] + 100) >= ddlDataLen[0])
        {
          byte[] tmp = new byte[ddlDataLen[0]];
          System.arraycopy(ddlData, 0, tmp, 0, ddlDataLen[0]);
          ddlDataLen[0] += 1000;
          ddlData = new byte[ddlDataLen[0]];
          System.arraycopy(tmp, 0, ddlData, 0, ddlDataLen[0] - 1000);
        }

        ddlDataUpto[0] = generalUtils.stringIntoBytes(fieldName + "=\"", 0, ddlData, ddlDataUpto[0]);
        ddlDataUpto[0] = generalUtils.stringIntoBytes(name, 0, ddlData, ddlDataUpto[0]);
        ddlDataUpto[0] = generalUtils.stringIntoBytes("\" ", 0, ddlData, ddlDataUpto[0]);
      }
      
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
            
      return ddlData;
    }
    catch(Exception e)
    {
      System.out.println("DocumentUtils: " + e);
      try
      {
        if(rs   != null) rs.close();     
        if(stmt != null) stmt.close();
      }
      catch(Exception e2) { }
    }
    
    return ddlData;
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getCountryDDL(Connection con, Statement stmt, ResultSet rs, String selectName, String thisOption) throws Exception
  {
    String s = "<select name=\"" + selectName + "\">";

    try
    {
      String name;
      
      stmt = con.createStatement();
      rs = stmt.executeQuery("SELECT Name FROM country ORDER BY Position"); 
      while(rs.next())
      {
        name = rs.getString(1);

        s += "<option value=\"" + name + "\"";
        if(name.equals(thisOption))
          s += " selected";      
        s += ">" + name + "\n";      
      }
      
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
            
      s += "</select>";
      return s;
    }
    catch(Exception e)
    {
      System.out.println("DocumentUtils: " + e);
      try
      {
        if(rs   != null) rs.close();     
        if(stmt != null) stmt.close();
      }
      catch(Exception e2) { }
    }
    
    s += "</select>";
    return s;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public byte[] getLikelihoodDDLData(Connection con, Statement stmt, ResultSet rs, String fieldName, byte[] ddlData, int[] ddlDataUpto,
                                     int[] ddlDataLen) throws Exception
  {
    try
    {
      String name;
      
      stmt = con.createStatement();
      rs = stmt.executeQuery("SELECT Name FROM quolike"); 
      while(rs.next())
      {
        name = rs.getString(1);
        if((ddlDataUpto[0] + 100) >= ddlDataLen[0])
        {
          byte[] tmp = new byte[ddlDataLen[0]];
          System.arraycopy(ddlData, 0, tmp, 0, ddlDataLen[0]);
          ddlDataLen[0] += 1000;
          ddlData = new byte[ddlDataLen[0]];
          System.arraycopy(tmp, 0, ddlData, 0, ddlDataLen[0] - 1000);
        }

        ddlDataUpto[0] = generalUtils.stringIntoBytes(fieldName + "=\"", 0, ddlData, ddlDataUpto[0]);
        ddlDataUpto[0] = generalUtils.stringIntoBytes(name, 0, ddlData, ddlDataUpto[0]);
        ddlDataUpto[0] = generalUtils.stringIntoBytes("\" ", 0, ddlData, ddlDataUpto[0]);
      }
      
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
            
      return ddlData;
    }
    catch(Exception e)
    {
      System.out.println("DocumentUtils: " + e);
      try
      {
        if(rs   != null) rs.close();     
        if(stmt != null) stmt.close();
      }
      catch(Exception e2) { }
    }
    
    return ddlData;
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public byte[] getQuoteStateDDLData(Connection con, Statement stmt, ResultSet rs, String fieldName, byte[] ddlData, int[] ddlDataUpto,
                                     int[] ddlDataLen) throws Exception
  {
    try
    {
      String name;
      stmt = con.createStatement();
      rs = stmt.executeQuery("SELECT Name FROM quostate"); 
      while(rs.next())
      {
        name = rs.getString(1);
        if((ddlDataUpto[0] + 100) >= ddlDataLen[0])
        {
          byte[] tmp = new byte[ddlDataLen[0]];
          System.arraycopy(ddlData, 0, tmp, 0, ddlDataLen[0]);
          ddlDataLen[0] += 1000;
          ddlData = new byte[ddlDataLen[0]];
          System.arraycopy(tmp, 0, ddlData, 0, ddlDataLen[0] - 1000);
        }

        ddlDataUpto[0] = generalUtils.stringIntoBytes(fieldName + "=\"", 0, ddlData, ddlDataUpto[0]);
        ddlDataUpto[0] = generalUtils.stringIntoBytes(name, 0, ddlData, ddlDataUpto[0]);
        ddlDataUpto[0] = generalUtils.stringIntoBytes("\" ", 0, ddlData, ddlDataUpto[0]);
      }
      
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
            
      return ddlData;
    }
    catch(Exception e)
    {
      System.out.println("DocumentUtils: " + e);
      try
      {
        if(rs   != null) rs.close();     
        if(stmt != null) stmt.close();
      }
      catch(Exception e2) { }
    }
    
    return ddlData;
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getQuoteStateDDL(Connection con, Statement stmt, ResultSet rs, String selectName, String insertOption) throws Exception
  {
    String s = "<select name=\"" + selectName + "\">";

    s += (insertOption + "\n");      

    try
    {
      String name;
      stmt = con.createStatement();
      rs = stmt.executeQuery("SELECT Name FROM quostate"); 
      while(rs.next())
      {
        name = rs.getString(1);

        s += "<option value=\"" + name + "\">" + name + "\n";      
      }
      
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
            
      s += "</select>";
      return s;
    }
    catch(Exception e)
    {
      System.out.println("DocumentUtils: " + e);
      try
      {
        if(rs   != null) rs.close();     
        if(stmt != null) stmt.close();
      }
      catch(Exception e2) { }
    }
    
    s += "</select>";
    return s;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getDefaultQuoteStatus(Connection con, Statement stmt, ResultSet rs)
  {
    String name="";
    
    try
    {
      stmt = con.createStatement();
      rs = stmt.executeQuery("SELECT Name FROM quostate WHERE Type = 'D'"); 
      if(rs.next()) // just-in-case
        name = rs.getString(1);
      
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
            
      return name;
    }
    catch(Exception e)
    {
      System.out.println("DocumentUtils: " + e);
      try
      {
        if(rs   != null) rs.close();     
        if(stmt != null) stmt.close();
      }
      catch(Exception e2) { }
    }
    
    return "";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void getQuotationReasons(Connection con, Statement stmt, ResultSet rs, String[] reasons)
  {
    for(int x=0;x<10;++x)
      reasons[x] = "";

    try
    {
      stmt = con.createStatement();
      rs = stmt.executeQuery("SELECT * FROM quoreasons");   
      if(rs.next())
      {
        reasons[0] = rs.getString(1);
        reasons[1] = rs.getString(2);
        reasons[2] = rs.getString(3);
        reasons[3] = rs.getString(4);
        reasons[4] = rs.getString(5);
        reasons[5] = rs.getString(6);
        reasons[6] = rs.getString(7);
        reasons[7] = rs.getString(8);
        reasons[8] = rs.getString(9);
        reasons[9] = rs.getString(10);
      }
      
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println("DefinitionTables: " + e);
      try
      {
        if(rs   != null) rs.close();     
        if(stmt != null) stmt.close();
      }
      catch(Exception e2) { }
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public String getDefaultStore(Connection con, Statement stmt, ResultSet rs, String dnm, String localDefnsDir, String defnsDir)
  {
    String gstRate="";
    
    try
    {
      stmt = con.createStatement();
      rs = stmt.executeQuery("SELECT Name FROM store WHERE Type = 'D'"); 
      if(rs.next()) // just-in-case
      {
        gstRate = rs.getString(1);
      }
      
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
            
      return gstRate;
    }
    catch(Exception e)
    {
      System.out.println("DocumentUtils: " + e);
      try
      {
        if(rs   != null) rs.close();     
        if(stmt != null) stmt.close();
      }
      catch(Exception e2) { }
    }
    
    return "";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  public int getStoresList(Connection con, Statement stmt, ResultSet rs, String[][] stores) throws Exception
  {
    return getStoresList(con, stmt, rs, true, stores);
  }
  public int getStoresList(Connection con, Statement stmt, ResultSet rs, boolean ignoreNotInUse, String[][] stores) throws Exception
  {
    try
    {
      int numStores;
      stmt = con.createStatement();
      
      if(ignoreNotInUse)
        rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM store WHERE Type != 'U'");
      else rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM store");
      
      if(rs.next())
        numStores = rs.getInt("rowcount");
      else numStores = 0;
      
      rs.close();
      stmt.close();

      if(numStores == 0)
        return 0;
      
      stores[0] = new String[numStores];
      
      int x=0;
      stmt = con.createStatement();

      if(ignoreNotInUse)
        rs = stmt.executeQuery("SELECT Name FROM store WHERE Type != 'U'"); 
      else rs = stmt.executeQuery("SELECT Name FROM store"); 

      while(rs.next())
        stores[0][x++] = rs.getString(1);

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
      
      return numStores;
    }
    catch(Exception e)
    {
      System.out.println("DocumentUtils: " + e);
      try
      {
        if(rs   != null) rs.close();     
        if(stmt != null) stmt.close();
      }
      catch(Exception e2) { }
    }
    
    return 0;
  }

}
