// =======================================================================================================================================================================================================
// System: ZaraStar Accounts: Utilities code
// Module: AccountsUtils.java
// Author: C.K.Harvey
// Copyright (c) 1998-2007 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>. 
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import java.sql.*;

public class AccountsUtils
{
  GeneralUtils generalUtils = new GeneralUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  DefinitionTables definitionTables = new DefinitionTables();
 
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getBaseCurrency(Connection con, Statement stmt, ResultSet rs, String dnm, String localDefnsDir, String defnsDir)
  {
    String currencyCode="";
    try
    {
      stmt = con.createStatement();
      rs = stmt.executeQuery("SELECT CurrencyCode FROM currency WHERE CurrencyType = 'B'"); 
      if(rs.next()) // just-in-case
        currencyCode = rs.getString(1);
      
      rs.close();
      stmt.close();
            
      return currencyCode;
    }
    catch(Exception e)
    {
      System.out.println("AccountsUtils: " + e);
      try
      {
        if(rs   != null) rs.close();     
        if(stmt != null) stmt.close();
      }
      catch(Exception e2) { }
    }
    
    return "";
  }

  // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public double getApplicableRate(Connection con, Statement stmt, ResultSet rs, byte[] currencyCode, byte[] dateReqd, byte[] actualDate, String dnm, String localDefnsDir, String defnsDir)
  {
    return getApplicableRate(con, stmt, rs, generalUtils.stringFromBytes(currencyCode, 0L), generalUtils.stringFromBytes(dateReqd, 0L), actualDate, dnm, localDefnsDir, defnsDir);
  }
  public double getApplicableRate(Connection con, Statement stmt, ResultSet rs, String currencyCode, String dateReqd, byte[] actualDate, String dnm, String localDefnsDir, String defnsDir)
  {
    double rate = 1.0; // just-in-case no rates for this curr

    try
    {
      int reqdDate = 0;
      if(dateReqd.contains("-"))
        reqdDate = generalUtils.encodeFromYYYYMMDD(dateReqd);
      else reqdDate = generalUtils.encode(dateReqd, localDefnsDir, defnsDir);

      int thisDate;
        
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT RateDate, RateValue FROM currrate WHERE CurrencyCode = '" + currencyCode + "' ORDER BY RateDate"); 
      
      while(rs.next())
      {
        thisDate = generalUtils.encodeFromYYYYMMDD(rs.getString(1));

        if(thisDate <= reqdDate)
        {
          rate = generalUtils.doubleFromStr(rs.getString(2));
          generalUtils.strToBytes(actualDate, rs.getString(1));
        }
      }

      rs.close();
      stmt.close();
            
      return rate;
    }
    catch(Exception e)
    {
      System.out.println("AccountsUtils: " + e);
      try
      {
        if(rs   != null) rs.close();     
        if(stmt != null) stmt.close();
      }
      catch(Exception e2) { }
    }
    
    return rate;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getDefaultGSTRate(Connection con, Statement stmt, ResultSet rs, String dnm, String localDefnsDir, String defnsDir)
  {
    String gstRate="";
    
    try
    {
      stmt = con.createStatement();
      rs = stmt.executeQuery("SELECT GSTRateName FROM gstrate WHERE Type = 'D'"); 
      if(rs.next()) // just-in-case
      {
        gstRate = rs.getString(1);
      }
      
      rs.close();
      stmt.close();
            
      return gstRate;
    }
    catch(Exception e)
    {
      System.out.println("AccountsUtils: " + e);
      try
      {
        if(rs   != null) rs.close();     
        if(stmt != null) stmt.close();
      }
      catch(Exception e2) { }
    }
    
    return "";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public byte[] getCurrencyNamesDDLData(Connection con, Statement stmt, ResultSet rs, String fieldName, String dnm, byte[] ddlData, int[] ddlDataUpto, int[] ddlDataLen, String localDefnsDir, 
                                        String defnsDir) throws Exception
  {
    try
    {
      String currencyCode;
      
      stmt = con.createStatement();
      
      rs = stmt.executeQuery("SELECT CurrencyCode FROM currency"); 
      
      while(rs.next())
      {
        currencyCode = rs.getString(1);
        if((ddlDataUpto[0] + 100) >= ddlDataLen[0])
        {
          byte[] tmp = new byte[ddlDataLen[0]];
          System.arraycopy(ddlData, 0, tmp, 0, ddlDataLen[0]);
          ddlDataLen[0] += 1000;
          ddlData = new byte[ddlDataLen[0]];
          System.arraycopy(tmp, 0, ddlData, 0, ddlDataLen[0] - 1000);
        }

        ddlDataUpto[0] = generalUtils.stringIntoBytes(fieldName + "=\"", 0, ddlData, ddlDataUpto[0]);
        ddlDataUpto[0] = generalUtils.stringIntoBytes(currencyCode, 0, ddlData, ddlDataUpto[0]);
        ddlDataUpto[0] = generalUtils.stringIntoBytes("\" ", 0, ddlData, ddlDataUpto[0]);
      }
      
      rs.close();
      stmt.close();
            
      return ddlData;
    }
    catch(Exception e)
    {
      System.out.println("AccountsUtils: " + e);
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
  public String getCurrencyNamesDDL(Connection con, Statement stmt, ResultSet rs, String selectName, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    String baseCurrency = getBaseCurrency(con, stmt, rs, dnm, localDefnsDir, defnsDir);
    return getCurrencyNamesDDL(con, stmt, rs, selectName, baseCurrency, dnm, localDefnsDir, defnsDir);
  }
  public String getCurrencyNamesDDL(Connection con, Statement stmt, ResultSet rs, String selectName, String thisOption, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    String s = "<select name=\"" + selectName + "\">";
      
    try
    {
      String currencyCode, currencyDesc;
      stmt = con.createStatement();
      rs = stmt.executeQuery("SELECT CurrencyCode, CurrencyDesc FROM currency"); 
      while(rs.next())
      {
        currencyCode = rs.getString(1);
        currencyDesc = rs.getString(2);

        s += "<option value=\"" + currencyCode + "\"";
        if(currencyCode.equals(thisOption))
          s += " selected";      
        s += ">" + currencyCode +  " - " + currencyDesc + "\n";      
      }
      
      rs.close();
      stmt.close();
            
      s += "</select>";
      return s;
    }
    catch(Exception e)
    {
      System.out.println("AccountsUtils: " + e);
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

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public int getCurrencyNames(Connection con, Statement stmt, ResultSet rs, String[][] currencies, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    try
    {
      int count = 0;
      stmt = con.createStatement();
      rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM currency");

      rs.next();
      int numCurrencies = rs.getInt("rowcount") ;
      rs.close();
        
      currencies[0] = new String[numCurrencies];
      
      stmt = con.createStatement();
      rs = stmt.executeQuery("SELECT CurrencyCode FROM currency"); 
      while(rs.next())
        currencies[0][count++] = rs.getString(1);
      
      rs.close();
      stmt.close();

      return count;
    }
    catch(Exception e)
    {
      System.out.println("AccountsUtils: " + e);
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
  public int getGSTNamesAndRates(Connection con, Statement stmt, ResultSet rs, String[][] names, double[][] rates, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    try
    {
      int count = 0;
      stmt = con.createStatement();
      rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM gstrate");

      rs.next();
      int numRates = rs.getInt("rowcount") ;
      rs.close();
        
      names[0] = new String[numRates];
      rates[0] = new double[numRates];
      
      stmt = con.createStatement();
      rs = stmt.executeQuery("SELECT GSTRateName, GSTRate FROM gstrate"); 
      while(rs.next())
      {
        names[0][count]   = rs.getString(1);
        rates[0][count++] = (generalUtils.doubleFromStr(rs.getString(2)) / 100);
      }
      
      rs.close();
      stmt.close();

      return count;
    }
    catch(Exception e)
    {
      System.out.println("AccountsUtils: " + e);
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
  public byte[] getGSTRatesDDLData(Connection con, Statement stmt, ResultSet rs, String fieldName, String dnm, byte[] ddlData, int[] ddlDataUpto, int[] ddlDataLen, String localDefnsDir, String defnsDir)
                                   throws Exception
  {
    try
    {
      String gstRateName;
      stmt = con.createStatement();
      rs = stmt.executeQuery("SELECT GSTRateName FROM gstrate"); 
      while(rs.next())
      {
        gstRateName = rs.getString(1);
        if((ddlDataUpto[0] + 100) >= ddlDataLen[0])
        {
          byte[] tmp = new byte[ddlDataLen[0]];
          System.arraycopy(ddlData, 0, tmp, 0, ddlDataLen[0]);
          ddlDataLen[0] += 1000;
          ddlData = new byte[ddlDataLen[0]];
          System.arraycopy(tmp, 0, ddlData, 0, ddlDataLen[0] - 1000);
        }

        ddlDataUpto[0] = generalUtils.stringIntoBytes(fieldName + "=\"", 0, ddlData, ddlDataUpto[0]);
        ddlDataUpto[0] = generalUtils.stringIntoBytes(gstRateName, 0, ddlData, ddlDataUpto[0]);
        ddlDataUpto[0] = generalUtils.stringIntoBytes("\" ", 0, ddlData, ddlDataUpto[0]);
      }
      
      rs.close();
      stmt.close();
            
      return ddlData;
    }
    catch(Exception e)
    {
      System.out.println("AccountsUtils: " + e);
      try
      {
        if(rs   != null) rs.close();     
        if(stmt != null) stmt.close();
      }
      catch(Exception e2) { }
    }
    
    return ddlData;
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public double getGSTRate(Connection con, Statement stmt, ResultSet rs, byte[] gstRateNameB)
  {
    return getGSTRate(con, stmt, rs, generalUtils.stringFromBytes(gstRateNameB, 0L));
  }
  public double getGSTRate(Connection con, Statement stmt, ResultSet rs, String gstRateName)
  {
    try
    {
      String gstRate="0.0";
      stmt = con.createStatement();
      rs = stmt.executeQuery("SELECT GSTRate FROM gstrate WHERE GSTRateName = '" + gstRateName + "'"); 
      if(rs.next())
      {
        gstRate = rs.getString(1);
      }
      
      rs.close();
      stmt.close();
            
      return (generalUtils.doubleFromStr(gstRate) / 100);
    }
    catch(Exception e)
    {
      System.out.println("AccountsUtils: " + e);
      try
      {
        if(rs   != null) rs.close();     
        if(stmt != null) stmt.close();
      }
      catch(Exception e2) { }
    }
    
    return 0.0;
  }
 
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public byte[] getGLAccountsDDLData(String fieldName, String unm, String dnm, byte[] ddlData, int[] ddlDataUpto, int[] ddlDataLen, String workingDir) throws Exception
  {
    Connection con = null;
    ResultSet rs = null;
    Statement stmt = null;
    
    try
    {
      String year = generalUtils.getFromDefnFile(unm, "accounts.dfn", workingDir, "");

      String uName = directoryUtils.getMySQLUserName();
      String pWord = directoryUtils.getMySQLPassWord();
      Class.forName("com.mysql.jdbc.Driver").newInstance();
      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_accounts_" + year + "?user=" + uName + "&password=" + pWord);
            
      stmt = con.createStatement();
      rs = stmt.executeQuery("SELECT AccCode, Description FROM acctdefn ORDER BY Description"); 

      String accCode, desc;

      while(rs.next())
      {
        accCode = rs.getString(1);
        desc    = rs.getString(2);
        
        if((ddlDataUpto[0] + 100) >= ddlDataLen[0])
        {
          byte[] tmp = new byte[ddlDataLen[0]];
          System.arraycopy(ddlData, 0, tmp, 0, ddlDataLen[0]);
          ddlDataLen[0] += 1000;
          ddlData = new byte[ddlDataLen[0]];
          System.arraycopy(tmp, 0, ddlData, 0, ddlDataLen[0] - 1000);
        }

        ddlDataUpto[0] = generalUtils.stringIntoBytes(fieldName + "=\"", 0, ddlData, ddlDataUpto[0]);
        ddlDataUpto[0] = generalUtils.stringIntoBytes(accCode + " - " + desc, 0, ddlData, ddlDataUpto[0]);
        ddlDataUpto[0] = generalUtils.stringIntoBytes("\" ", 0, ddlData, ddlDataUpto[0]);
      }
      
      rs.close();
      stmt.close();
      con.close();
            
      return ddlData;
    }
    catch(Exception e)
    {
      System.out.println("AccountsUtils: " + e);
      try
      {
        if(rs   != null) rs.close();     
        if(stmt != null) stmt.close();
        if(con  != null) con.close();
      }
      catch(Exception e2) { }
    }
    
    return ddlData;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public byte[] getBankAccountsDDLData(Connection con, Statement stmt, ResultSet rs, String fieldName, String dnm, byte[] ddlData, int[] ddlDataUpto, int[] ddlDataLen, String localDefnsDir,
                                       String defnsDir) throws Exception
  {
    try
    {
      String bankAccountName;

      ddlDataUpto[0] = generalUtils.stringIntoBytes(fieldName + "=\"<none>\" ", 0, ddlData, ddlDataUpto[0]);      
      
      stmt = con.createStatement();
      rs = stmt.executeQuery("SELECT BankAccountName FROM bankaccount"); 
      while(rs.next())
      {
        bankAccountName = rs.getString(1);
        if((ddlDataUpto[0] + 100) >= ddlDataLen[0])
        {
          byte[] tmp = new byte[ddlDataLen[0]];
          System.arraycopy(ddlData, 0, tmp, 0, ddlDataLen[0]);
          ddlDataLen[0] += 1000;
          ddlData = new byte[ddlDataLen[0]];
          System.arraycopy(tmp, 0, ddlData, 0, ddlDataLen[0] - 1000);
        }

        ddlDataUpto[0] = generalUtils.stringIntoBytes(fieldName + "=\"", 0, ddlData, ddlDataUpto[0]);
        ddlDataUpto[0] = generalUtils.stringIntoBytes(bankAccountName, 0, ddlData, ddlDataUpto[0]);
        ddlDataUpto[0] = generalUtils.stringIntoBytes("\" ", 0, ddlData, ddlDataUpto[0]);
      }
      
      rs.close();
      stmt.close();
            
      return ddlData;
    }
    catch(Exception e)
    {
      System.out.println("AccountsUtils: " + e);
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
  public String getCurrencyDescription(Connection con, Statement stmt, ResultSet rs, String currency, String dnm, String localDefnsDir, String defnsDir)
  {
    try
    {
      String currencyName="";
      stmt = con.createStatement();
      rs = stmt.executeQuery("SELECT CurrencyDesc FROM currency WHERE CurrencyCode = '" + currency + "'"); 
      while(rs.next())
      {
        currencyName = rs.getString(1);
      }
      
      rs.close();
      stmt.close();
            
      return currencyName;
    }
    catch(Exception e)
    {
      System.out.println("AccountsUtils: " + e);
      try
      {
        if(rs   != null) rs.close();     
        if(stmt != null) stmt.close();
      }
      catch(Exception e2) { }
    }
    
    return "";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getAccountPlusDescriptionGivenAccCode(byte[] accCode, String unm, String dnm, String workingDir, String localDefnsDir, String defnsDir)
  {
    String year = generalUtils.getFromDefnFile(unm, "accounts.dfn", workingDir, "");
    String accCodeStr = generalUtils.stringFromBytes(accCode, 0L);
    return accCodeStr + " - " + getAccountDescriptionGivenAccCode(accCodeStr, year, dnm, localDefnsDir, defnsDir);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getAccountDescriptionGivenAccCode(String accCode, String year, String dnm, String localDefnsDir, String defnsDir)
  {
    Connection con = null;
    Statement stmt = null;
    ResultSet rs   = null;
    
    try
    {
      String uName = directoryUtils.getMySQLUserName();
      String pWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();
    
      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_accounts_" + year + "?user=" + uName + "&password=" + pWord);

      stmt = con.createStatement();
      rs = stmt.executeQuery("SELECT Description FROM acctdefn WHERE AccCode = '" + accCode + "'");
      
      String desc="";
      if(rs.next())
        desc = rs.getString(1);
      
      rs.close();
      stmt.close();
      con.close();
            
      return desc;
    }
    catch(Exception e)
    {
      System.out.println("AccountsUtils: " + e);
      try
      {
        if(rs   != null) rs.close();     
        if(stmt != null) stmt.close();
        if(con  != null) con.close();
      }
      catch(Exception e2) { }
    }
    
    return "";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getAccountCurrencyGivenAccCode(String accCode, String year, String dnm, String localDefnsDir, String defnsDir)
  {
    Connection con = null;
    Statement stmt = null;
    ResultSet rs   = null;
    
    try
    {
      String uName = directoryUtils.getMySQLUserName();
      String pWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();
    
      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_accounts_" + year + "?user=" + uName + "&password=" + pWord);

      stmt = con.createStatement();
      rs = stmt.executeQuery("SELECT Currency FROM acctdefn WHERE AccCode = '" + accCode + "'");
      
      String desc="";
      if(rs.next())
        desc = rs.getString(1);
      
      rs.close();
      stmt.close();
      con.close();
            
      return desc;
    }
    catch(Exception e)
    {
      System.out.println("AccountsUtils: " + e);
      try
      {
        if(rs   != null) rs.close();     
        if(stmt != null) stmt.close();
        if(con  != null) con.close();
      }
      catch(Exception e2) { }
    }
    
    return "";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getAccountDrCrGivenAccCode(String accCode, String year, String dnm, String localDefnsDir, String defnsDir)
  {
    Connection con = null;
    Statement stmt = null;
    ResultSet rs   = null;
    
    try
    {
      String uName = directoryUtils.getMySQLUserName();
      String pWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();
    
      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_accounts_" + year + "?user=" + uName + "&password=" + pWord);

      stmt = con.createStatement();
      rs = stmt.executeQuery("SELECT DrCr FROM acctdefn WHERE AccCode = '" + accCode + "'");
      
      String drCr="";
      if(rs.next())
      {
        drCr = rs.getString(1);
        if(drCr.equals("D"))
          drCr = "Debit";
        else drCr = "Credit";
      }
      
      rs.close();
      stmt.close();
      con.close();
            
      return drCr;
    }
    catch(Exception e)
    {
      System.out.println("AccountsUtils: " + e);
      try
      {
        if(rs   != null) rs.close();     
        if(stmt != null) stmt.close();
        if(con  != null) con.close();
      }
      catch(Exception e2) { }
    }
    
    return "";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getBankAccountDDL2(String selectName, String unm, String dnm, String workingDir) throws Exception
  {
    Connection con = null;
    Statement stmt = null;
    ResultSet rs   = null;

    String s = "<select name=\"" + selectName + "\">";
      
    try
    {
      String year = generalUtils.getFromDefnFile(unm, "accounts.dfn", workingDir, "");

      String uName    = directoryUtils.getMySQLUserName();
      String passWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();
    
      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_accounts_" + year + "?user=" + uName + "&password=" + passWord);

      stmt = con.createStatement();
      rs = stmt.executeQuery("SELECT AccCode, Description FROM acctdefn WHERE Category = 'Current Assets - Bank' ORDER BY Description");

      String accCode, desc;
      
      while(rs.next())
      {
        accCode = rs.getString(1);
        desc    = rs.getString(2);

        s += ("<option value=\"" + accCode + "\">" + accCode + " - " + desc + "\n"); 
      }
      
      rs.close();
      stmt.close();
      con.close();
            
      s += "</select>";
      return s;
    }
    catch(Exception e)
    {
      System.out.println("AccountsUtils: " + e);
      try
      {
        if(rs   != null) rs.close();     
        if(stmt != null) stmt.close();
        if(con  != null) con.close();
      }
      catch(Exception e2) { }
    }
    
    s += "</select>";
    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getBankAccountDDL(Connection con, Statement stmt, ResultSet rs, String selectName, String thisOption, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    String s = "<select name=\"" + selectName + "\">";
      
    try
    {
      String bankAccountName, type;
      stmt = con.createStatement();
      rs = stmt.executeQuery("SELECT BankAccountName, Type FROM bankaccount"); 
      while(rs.next())
      {
        bankAccountName = rs.getString(1);
        type            = rs.getString(2);

        s += ("<option value=\"" + bankAccountName + "\"");
        if(thisOption.length() == 0)
        {
          if(type.equals("D"))
            s += " selected";
        }
        else
        if(bankAccountName.equals(thisOption))
          s += " selected";
        
        s += ">" + bankAccountName + "\n";      
      }
      
      rs.close();
      stmt.close();
            
      s += "</select>";
      return s;
    }
    catch(Exception e)
    {
      System.out.println("AccountsUtils: " + e);
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

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getAROrAPAccountsDDL(char arOrAP, String selectName, String thisOption, String unm, String dnm, String workingDir) throws Exception
  {
    Connection con = null;
    Statement stmt = null;
    ResultSet rs   = null;
    String s = "<select name=\"" + selectName + "\">";
      
    try
    {
      String year = generalUtils.getFromDefnFile(unm, "accounts.dfn", workingDir, "");

      String uName    = directoryUtils.getMySQLUserName();
      String passWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();
    
      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_accounts_" + year + "?user=" + uName + "&password=" + passWord);

      stmt = con.createStatement();
      if(arOrAP == 'R')
      {
        rs = stmt.executeQuery("SELECT AccCode, Description FROM acctdefn "
                             + "WHERE Category = 'Current Assets - Bank' "
                             + "OR Category = 'Current Assets - Cash' "
                             + "OR Category = 'Current Assets - Contra' "
                             + "OR Category = 'Current Assets - OverPayments' "
                             + "OR Category = 'Current Assets - PrePayments' "
                             + "OR Category = 'Liabilities - PrePayments' ORDER BY AccCode");
      }
      else
      {
        rs = stmt.executeQuery("SELECT AccCode, Description FROM acctdefn "
                             + "WHERE Category = 'Current Assets - Bank' "
                             + "OR Category = 'Current Assets - Cash' "
                             + "OR Category = 'Current Assets - Contra' "
                             + "OR Category = 'Current Assets - Currency Exchange' "
                             + "OR Category = 'Current Assets - PrePayments' "
                             + "OR Category = 'Income (Operating) - Agents Commission' "
                             + "OR Category = 'Current Assets - OverPayments' OR Category = 'Liabilities - PrePayments' "
                             + "OR Category = 'Current Assets - Recoverable Expenses' ORDER BY AccCode");
      }

      String accCode, desc;

      while(rs.next())
      {
        accCode = rs.getString(1);
        desc    = rs.getString(2);

        s += ("<option value=\"" + accCode + "\"");
        if(accCode.equals(thisOption))
          s += " selected";
        
        s += ">" + accCode + " (" + desc + ")\n";      
      }
      
      rs.close();
      stmt.close();
      con.close();
            
      s += "</select>";
      return s;
    }
    catch(Exception e)
    {
      System.out.println("AccountsUtils: " + e);
      try
      {
        if(rs   != null) rs.close();     
        if(stmt != null) stmt.close();
        if(con  != null) con.close();
      }
      catch(Exception e2) { }
    }
    
    s += "</select>";
    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public byte[] getAROrAPAccountsDDLData(char arOrAP, String fieldName, String unm, String dnm, byte[] ddlData, int[] ddlDataUpto, int[] ddlDataLen, String workingDir) throws Exception
  {
    Connection con = null;
    Statement stmt = null;
    ResultSet rs   = null;
    
    try
    {
      String year = generalUtils.getFromDefnFile(unm, "accounts.dfn", workingDir, "");

      String uName = directoryUtils.getMySQLUserName();
      String pWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();
    
      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_accounts_" + year + "?user=" + uName + "&password=" + pWord);

      stmt = con.createStatement();
      if(arOrAP == 'R')
      {
        rs = stmt.executeQuery("SELECT AccCode, Description FROM acctdefn "
                             + "WHERE Category = 'Current Assets - Bank' "
                             + "OR Category = 'Current Assets - Cash' "
                             + "OR Category = 'Current Assets - Contra' "
                             + "OR Category = 'Current Assets - OverPayments' "
                             + "OR Category = 'Liabilities - PrePayments' "
                             + "ORDER BY AccCode");
      }
      else
      {
        rs = stmt.executeQuery("SELECT AccCode, Description FROM acctdefn "
                             + "WHERE Category = 'Current Assets - Bank' "
                             + "OR Category = 'Current Assets - Cash' "
                             + "OR Category = 'Current Assets - Contra' "
                             + "OR Category = 'Current Assets - Currency Exchange' "
                             + "OR Category = 'Current Assets - OverPayments' "
                             + "OR Category = 'Income (Operating) - Agents Commission' "
                             + "OR Category = 'Current Assets - PrePayments' "
                             + "OR Category = 'Liabilities - PrePayments' "
                             + "OR Category = 'Current Assets - Recoverable Expenses' "
                             + "ORDER BY AccCode");
      }

      String accCode, desc;

      while(rs.next())
      {
        accCode = rs.getString(1);
        desc    = rs.getString(2);
        
        if((ddlDataUpto[0] + 100) >= ddlDataLen[0])
        {
          byte[] tmp = new byte[ddlDataLen[0]];
          System.arraycopy(ddlData, 0, tmp, 0, ddlDataLen[0]);
          ddlDataLen[0] += 1000;
          ddlData = new byte[ddlDataLen[0]];
          System.arraycopy(tmp, 0, ddlData, 0, ddlDataLen[0] - 1000);
        }

        ddlDataUpto[0] = generalUtils.stringIntoBytes(fieldName + "=\"", 0, ddlData, ddlDataUpto[0]);
        ddlDataUpto[0] = generalUtils.stringIntoBytes(accCode + " - " + desc, 0, ddlData, ddlDataUpto[0]);
        ddlDataUpto[0] = generalUtils.stringIntoBytes("\" ", 0, ddlData, ddlDataUpto[0]);
      }
      
      rs.close();
      stmt.close();
      con.close();
            
      return ddlData;
    }
    catch(Exception e)
    {
      System.out.println("AccountsUtils: " + e);
      try
      {
        if(rs   != null) rs.close();     
        if(stmt != null) stmt.close();
        if(con  != null) con.close();
      }
      catch(Exception e2) { }
    }
    
    return ddlData;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getCategoryNamesDDL(String selectName) throws Exception
  {
    String s = "<select name=\"" + selectName + "\">";
    
    s += "<option value=\"Fixed Assets\">Fixed Assets\n";
    s += "<option value=\"Fixed Assets - Accumulated Depreciation\">Fixed Assets - Accumulated Depreciation\n";
    s += "<option value=\"Fixed Assets - Investment\">Fixed Assets - Investment\n";
    s += "<option value=\"Current Assets\">Current Assets\n";
    s += "<option value=\"Current Assets - Bank\">Current Assets - Bank\n";
    s += "<option value=\"Current Assets - Cash\">Current Assets - Cash\n";
    s += "<option value=\"Current Assets - Contra\">Current Assets - Contra\n";
    s += "<option value=\"Current Assets - Currency Exchange\">Current Assets - Currency Exchange\n";
    s += "<option value=\"Current Assets - GST Clearing\">Current Assets - GST Clearing\n";
    s += "<option value=\"Current Assets - Other Debtors\">Current Assets - Other Debtors\n";
    s += "<option value=\"Current Assets - OverPayments\">Current Assets - OverPayments\n";
    s += "<option value=\"Current Assets - Recoverable Expenses\">Current Assets - Recoverable Expenses\n";
    s += "<option value=\"Current Assets - PrePayments\">Current Assets - PrePayments\n";
    s += "<option value=\"Current Assets - Stock\">Current Assets - Stock\n";
    s += "<option value=\"Current Assets - WIP\">Current Assets - WIP\n";
    s += "<option value=\"Current Assets - Trade Debtors\">Current Assets - Trade Debtors\n";
    s += "<option value=\"Liabilities\">Liabilities\n";
    s += "<option value=\"Liabilities - Accruals\">Liabilities - Accruals\n";
    s += "<option value=\"Liabilities - GST Input\">Liabilities - GST Input\n";
    s += "<option value=\"Liabilities - GST Output\">Liabilities - GST Output\n";
    s += "<option value=\"Liabilities - Other Creditors\">Liabilities - Other Creditors\n";
    s += "<option value=\"Liabilities - PrePayments\">Liabilities - PrePayments\n";
    s += "<option value=\"Liabilities - Proposed Dividend\">Liabilities - Proposed Dividend\n";
    s += "<option value=\"Liabilities - Provision for Income Tax\">Liabilities - Provision for Income Tax\n";
    s += "<option value=\"Liabilities - Suspense\">Liabilities - Suspense\n";
    s += "<option value=\"Liabilities - Trade Creditors\">Liabilities - Trade Creditors\n";
    s += "<option value=\"Equity\">Equity\n";
    s += "<option value=\"Equity - Current Earnings\">Equity - Current Earnings\n";
    s += "<option value=\"Equity - Retained Earnings\">Equity - Retained Earnings\n";
    s += "<option value=\"Income\">Income\n";
    s += "<option value=\"Income - Discount Given\">Income - Discount Given\n";
    s += "<option value=\"Income - Exchange Gain\">Income - Exchange Gain\n";
    s += "<option value=\"Income - Sales\">Income - Sales\n";
    s += "<option value=\"Income - Sales Category\">Income - Sales Category\n";
    s += "<option value=\"Income - Sales Returned\">Income - Sales Returned\n";
    s += "<option value=\"Income (Operating) - Agents Commission\">Income (Operating) - Agents Commission\n";
    s += "<option value=\"Cost of Sales\">Cost of Sales\n";
    s += "<option value=\"Cost of Sales - Carriage Inward\">Cost of Sales - Carriage Inward\n";
    s += "<option value=\"Cost of Sales - Carriage Outward\">Cost of Sales - Carriage Outward\n";
    s += "<option value=\"Cost of Sales - Delivery and Distribution\">Cost of Sales - Delivery and Distribution\n";
    s += "<option value=\"Cost of Sales - Discount Received\">Cost of Sales - Discount Received\n";
    s += "<option value=\"Cost of Sales - Exchange Loss\">Cost of Sales - Exchange Loss\n";
    s += "<option value=\"Cost of Sales - Purchases\">Cost of Sales - Purchases\n";
    s += "<option value=\"Cost of Sales - Purchases Returned\">Cost of Sales - Purchases Returned\n";
    s += "<option value=\"Cost of Sales - Purchases Category\">Cost of Sales - Purchases Category\n";
    s += "<option value=\"Expenses - Bank Charges\">Expenses - Bank Charges\n";
    s += "<option value=\"Expenses - Finance\">Expenses - Finance\n";
    s += "<option value=\"Expenses - General\">Expenses - General\n";
    s += "<option value=\"Expenses - Premises\">Expenses - Premises\n";
    s += "<option value=\"Expenses - Professional\">Expenses - Professional\n";
    s += "<option value=\"Expenses - Promotional\">Expenses - Promotional\n";
    s += "<option value=\"Expenses - Maintenance\">Expenses - Maintenance\n";
    s += "<option value=\"Expenses - Staff\">Expenses - Staff\n";
    s += "<option value=\"Expenses - Travel\">Expenses - Travel\n";
    s += "<option value=\"Expenses - Travel\">Expenses - Travel\n";
    s += "<option value=\"PL Provision - Provision for Income Tax\">PL Provision - Provision for Income Tax\n";
    s += "<option value=\"PL Provision - Proposed Dividend\">PL Provision - Proposed Dividend\n";
    s += "<option value=\"PL Provision - Deferred Tax\">PL Provision - Deferred Tax\n";

    s += "</select>";
    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getPrePaymentToSupplierAccCode(String year, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    return getAccCodeGivenCategory("Current Assets - PrePayments", year, dnm, localDefnsDir, defnsDir);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getPrePaymentFromCustomerAccCode(String year, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    return getAccCodeGivenCategory("Liabilities - PrePayments", year, dnm, localDefnsDir, defnsDir);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getPLAccCode(String year, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    return getAccCodeGivenCategory("Equity - Current Earnings", year, dnm, localDefnsDir, defnsDir);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getStockAccCode(String year, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    return getAccCodeGivenCategory("Current Assets - Stock", year, dnm, localDefnsDir, defnsDir);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getPurchasesAccCode(String year, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    return getAccCodeGivenCategory("Cost of Sales - Purchases", year, dnm, localDefnsDir, defnsDir);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getPurchasesReturnedAccCode(String year, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    return getAccCodeGivenCategory("Cost of Sales - Purchases Returned", year, dnm, localDefnsDir, defnsDir);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getSalesAccCode(String year, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    return getAccCodeGivenCategory("Income - Sales", year, dnm, localDefnsDir, defnsDir);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getSalesReturnedAccCode(String year, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    return getAccCodeGivenCategory("Income - Sales Returned", year, dnm, localDefnsDir, defnsDir);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getTradeDebtorsAccCode(String year, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    return getAccCodeGivenCategory("Current Assets - Trade Debtors", year, dnm, localDefnsDir, defnsDir);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getOtherDebtorsAccCode(String year, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    return getAccCodeGivenCategory("Current Assets - Other Debtors", year, dnm, localDefnsDir, defnsDir);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getTradeCreditorsAccCode(String year, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    return getAccCodeGivenCategory("Liabilities - Trade Creditors", year, dnm, localDefnsDir, defnsDir);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getOtherCreditorsAccCode(String year, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    return getAccCodeGivenCategory("Liabilities - Other Creditors", year, dnm, localDefnsDir, defnsDir);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getAccCodeGivenCategory(String category, String year, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    Connection con = null;
    Statement stmt = null;
    ResultSet rs   = null;
    
    try
    {
      String uName = directoryUtils.getMySQLUserName();
      String pWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();
    
      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_accounts_" + year + "?user=" + uName + "&password=" + pWord);

      stmt = con.createStatement();
      rs = stmt.executeQuery("SELECT AccCode FROM acctdefn WHERE Category = '" + category + "'");
      
      String accCode="";
      if(rs.next())
        accCode = rs.getString(1);
      
      rs.close();
      stmt.close();
      con.close();
            
      return accCode;
    }
    catch(Exception e)
    {
      System.out.println("AccountsUtils: " + e);
      try
      {
        if(rs   != null) rs.close();     
        if(stmt != null) stmt.close();
        if(con  != null) con.close();
      }
      catch(Exception e2) { }
    }
    
    return "";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public byte[] getCategoryDDLData(Connection ofsaCon, Statement ofsaStmt, ResultSet ofsaRs, char salesOrPurchases, String documentDate, String thisCategory, String fieldName, String dnm,
                                   byte[] ddlData, int[] ddlDataUpto, int[] ddlDataLen, String localDefnsDir, String defnsDir, String[] thisAccCodeAndDesc) throws Exception
  {
    boolean atLeastOneFound = false;

    thisAccCodeAndDesc[0] = "";
    
    Connection con = null;
    Statement stmt = null;
    ResultSet rs   = null;

    try
    {
      // determine year from the given document date
      int encoded = generalUtils.encodeFromYYYYMMDD(documentDate);
      int yyyy = generalUtils.getYear(encoded);
      int mm   = generalUtils.getMonth(encoded);

      String[] financialYearStartMonth = new String[1];
      String[] financialYearEndMonth   = new String[1];
      
      definitionTables.getAppConfigFinancialYearMonths(ofsaCon, ofsaStmt, ofsaRs, dnm, financialYearStartMonth, financialYearEndMonth);

      int startMonth = generalUtils.detMonthNumFromMonthName(financialYearStartMonth[0]);
      int endMonth   = generalUtils.detMonthNumFromMonthName(financialYearEndMonth[0]);
      
      if(endMonth < startMonth && mm <= endMonth)
        --yyyy;
      
      String year = generalUtils.intToStr(yyyy);      
 
      String uName = directoryUtils.getMySQLUserName();
      String pWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();
    
      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_accounts_" + year + "?user=" + uName + "&password=" + pWord);

      stmt = con.createStatement();
      if(salesOrPurchases == 'S')
        rs = stmt.executeQuery("SELECT AccCode, Description FROM acctdefn WHERE Category = 'Income - Sales' ORDER BY Description");
      else
      if(salesOrPurchases == 'C')
        rs = stmt.executeQuery("SELECT AccCode, Description FROM acctdefn WHERE Category = 'Income - Sales Returned' ORDER BY Description");
      else
      if(salesOrPurchases == 'D')
        rs = stmt.executeQuery("SELECT AccCode, Description FROM acctdefn WHERE Category = 'Cost of Sales - Purchases Returned' ORDER BY Description");
      else
        rs = stmt.executeQuery("SELECT AccCode, Description FROM acctdefn WHERE Category = 'Cost of Sales - Purchases' ORDER BY Description");

      String accCode, desc;

      while(rs.next())
      {
        accCode = rs.getString(1);
        desc    = rs.getString(2);
        
        if((ddlDataUpto[0] + 100) >= ddlDataLen[0])
        {
          byte[] tmp = new byte[ddlDataLen[0]];
          System.arraycopy(ddlData, 0, tmp, 0, ddlDataLen[0]);
          ddlDataLen[0] += 1000;
          ddlData = new byte[ddlDataLen[0]];
          System.arraycopy(tmp, 0, ddlData, 0, ddlDataLen[0] - 1000);
        }

        ddlDataUpto[0] = generalUtils.stringIntoBytes(fieldName + "=\"", 0, ddlData, ddlDataUpto[0]);
        ddlDataUpto[0] = generalUtils.stringIntoBytes(accCode + " - " + desc, 0, ddlData, ddlDataUpto[0]);
        ddlDataUpto[0] = generalUtils.stringIntoBytes("\" ", 0, ddlData, ddlDataUpto[0]);
        
        if(accCode.equals(thisCategory))
          thisAccCodeAndDesc[0] = accCode +  " - " + desc;
        
        atLeastOneFound = true;
      }
     
      rs.close();
      stmt.close();

      // done twice to ensure that main sales/purchases account appears at top of list
      
      stmt = con.createStatement();
      
      if(salesOrPurchases == 'S' || salesOrPurchases == 'C') // invoice or CN
      {
        rs = stmt.executeQuery("SELECT AccCode, Description FROM acctdefn "
                             + "WHERE Category = 'Income - Sales Category' "
                             + "OR Category = 'Liabilities - PrePayments' "
                             + "OR Category = 'Current Assets - Recoverable Expenses' "
                             + "ORDER BY Description");
      }
      else
      {
        rs = stmt.executeQuery("SELECT AccCode, Description FROM acctdefn "
                             + "WHERE (Category = 'Cost of Sales' "
                             + "OR Category = 'Cost of Sales - Purchases Category' "
                             + "OR Category = 'Cost of Sales - Carriage Outward' "
                             + "OR Category = 'Cost of Sales - Carriage Inward' "
                             + "OR Category = 'Cost of Sales - Delivery and Distribution' "
                             + "OR Category = 'Income (Operating) - Agents Commission' "
                             + "OR Category LIKE 'Expenses - %' "
                             + "OR Category = 'Current Assets - Recoverable Expenses' "
                             + "OR Category = 'Current Assets' "
                             + "OR Category = 'Current Assets - PrePayments' "
                             + "OR Category = 'Liabilities - Accruals' "
                             + "OR Category = 'Fixed Assets' "
                             + "OR Category = 'Fixed Assets - Investment' ) "
                             + "ORDER BY Description");
      }

      while(rs.next())
      {
        accCode = rs.getString(1);
        desc    = rs.getString(2);
        
        if((ddlDataUpto[0] + 100) >= ddlDataLen[0])
        {
          byte[] tmp = new byte[ddlDataLen[0]];
          System.arraycopy(ddlData, 0, tmp, 0, ddlDataLen[0]);
          ddlDataLen[0] += 1000;
          ddlData = new byte[ddlDataLen[0]];
          System.arraycopy(tmp, 0, ddlData, 0, ddlDataLen[0] - 1000);
        }

        ddlDataUpto[0] = generalUtils.stringIntoBytes(fieldName + "=\"", 0, ddlData, ddlDataUpto[0]);
        ddlDataUpto[0] = generalUtils.stringIntoBytes(accCode + " - " + desc, 0, ddlData, ddlDataUpto[0]);
        ddlDataUpto[0] = generalUtils.stringIntoBytes("\" ", 0, ddlData, ddlDataUpto[0]);
        
        if(accCode.equals(thisCategory))
          thisAccCodeAndDesc[0] = accCode +  " - " + desc;

        atLeastOneFound = true;
      }
      
      rs.close();
      stmt.close();

      con.close();
    }
    catch(Exception e)
    {
      System.out.println("AccountsUtils: " + e);
      try
      {
        if(rs   != null) rs.close();     
        if(stmt != null) stmt.close();
        if(con  != null) con.close();
      }
      catch(Exception e2) { }
    }

    if(! atLeastOneFound)
    {
      if((ddlDataUpto[0] + 100) >= ddlDataLen[0])
      {
        byte[] tmp = new byte[ddlDataLen[0]];
        System.arraycopy(ddlData, 0, tmp, 0, ddlDataLen[0]);
        ddlDataLen[0] += 1000;
        ddlData = new byte[ddlDataLen[0]];
        System.arraycopy(tmp, 0, ddlData, 0, ddlDataLen[0] - 1000);
      }

      ddlDataUpto[0] = generalUtils.stringIntoBytes(fieldName + "=\"0 - No Accounts\"", 0, ddlData, ddlDataUpto[0]);
    }
    
    return ddlData;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean isNonBaseAccountGivenAccCode(Connection con, Statement stmt, ResultSet rs, String accCode, String year, String dnm, String localDefnsDir, String defnsDir, String[] currency)
                                              throws Exception
  {
    currency[0] = getAccountCurrencyGivenAccCode(accCode, year, dnm, localDefnsDir, defnsDir);

    if(currency[0].equals(getBaseCurrency(con, stmt, rs, dnm, localDefnsDir, defnsDir)))
      return false;
    
    return true;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getAccountingYearForADate(Connection con, Statement stmt, ResultSet rs, String date, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    String[] financialYearStartMonth = new String[1];
    String[] financialYearEndMonth   = new String[1];

    definitionTables.getAppConfigFinancialYearMonths(con, stmt, rs, dnm, financialYearStartMonth, financialYearEndMonth);

    int startMonth = generalUtils.detMonthNumFromMonthName(financialYearStartMonth[0]);

    int d = generalUtils.encode(date, localDefnsDir, defnsDir);

    int month = generalUtils.getMonth(d);
    int year  = generalUtils.getYear(d);

    if(month < startMonth)
      --year;

    return generalUtils.intToStr(year);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // YYYYMMDD in and out
  public String getAccountingYearStartDateForADate(Connection con, Statement stmt, ResultSet rs, String date, String dnm) throws Exception
  {
    String[] financialYearStartMonth = new String[1];
    String[] financialYearEndMonth   = new String[1];

    definitionTables.getAppConfigFinancialYearMonths(con, stmt, rs, dnm, financialYearStartMonth, financialYearEndMonth);

    int startMonth = generalUtils.detMonthNumFromMonthName(financialYearStartMonth[0]);

    int d = generalUtils.encodeFromYYYYMMDD(date);

    int month = generalUtils.getMonth(d);
    int year  = generalUtils.getYear(d);

    if(month < startMonth)
      --year;

    return year + "-" + startMonth + "-01";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // YYYYMMDD in and out
  public String getAccountingYearEndDateForADate(Connection con, Statement stmt, ResultSet rs, String date, String dnm) throws Exception
  {
    String[] financialYearStartMonth = new String[1];
    String[] financialYearEndMonth   = new String[1];

    definitionTables.getAppConfigFinancialYearMonths(con, stmt, rs, dnm, financialYearStartMonth, financialYearEndMonth);

    int endMonth = generalUtils.detMonthNumFromMonthName(financialYearEndMonth[0]);

    int d = generalUtils.encodeFromYYYYMMDD(date);

    int month = generalUtils.getMonth(d);
    int year  = generalUtils.getYear(d);

    if(month > endMonth)
      ++year;

    return year + "-" + endMonth + "-" + generalUtils.numOfDaysInMonth((short)endMonth, (short)year);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getCategoryGivenAccCode(String accCode, String year, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    String category = "";
    
    Connection con = null;
    Statement stmt = null;
    ResultSet rs   = null;

    try
    {
      // determine year from the given document date
      String uName = directoryUtils.getMySQLUserName();
      String pWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();
    
      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_accounts_" + year + "?user=" + uName + "&password=" + pWord);

      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT Category FROM acctdefn WHERE AccCode = '" + accCode + "'");

      if(rs.next())
        category = rs.getString(1);
      
      rs.close();
      stmt.close();

      con.close();
    }
    catch(Exception e)
    {
      System.out.println("AccountsUtils: " + e);
      try
      {
        if(rs   != null) rs.close();     
        if(stmt != null) stmt.close();
        if(con  != null) con.close();
      }
      catch(Exception e2) { }
    }

    return category;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // sets YYYYMMDD
  public void getAccountingYearStartAndEndDatesForAYear(Connection con, Statement stmt, ResultSet rs, String year, String dnm, String localDefnsDir, String defnsDir, String[] yearStartDate,
                                                        String[] yearEndDate) throws Exception
  {
    String[] financialYearStartMonth = new String[1];
    String[] financialYearEndMonth   = new String[1];
    definitionTables.getAppConfigFinancialYearMonths(con, stmt, rs, dnm, financialYearStartMonth, financialYearEndMonth);

    String financialYearEndDate = generalUtils.lastDayOfMonthYYYYMMDD(year + "-" + generalUtils.detMonthNumFromMonthName(financialYearEndMonth[0]) + "-01");
    String financialYearStartDate = year + "-" + generalUtils.detMonthNumFromMonthName(financialYearStartMonth[0]) + "-01";

    yearStartDate[0] = financialYearStartDate;
    yearEndDate[0]   = financialYearEndDate;

    int effectiveStartDate = generalUtils.encode(definitionTables.getAppConfigEffectiveStartDate(con, stmt, rs, dnm), localDefnsDir, defnsDir);

    if(effectiveStartDate > generalUtils.encodeFromYYYYMMDD(yearStartDate[0]))
      yearStartDate[0] = generalUtils.decodeToYYYYMMDD(effectiveStartDate);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getAccountsDDL(String unm, String dnm, String workingDir, String selectName, boolean addALL) throws Exception
  {
    Connection con = null;
    Statement stmt = null;
    ResultSet rs   = null;

    String s = "<select name=\"" + selectName + "\">";

    if(addALL) s += "<option value=\"___ALL\">ALL\n";

    try
    {
      String year = generalUtils.getFromDefnFile(unm, "accounts.dfn", workingDir, "");

      String uName    = directoryUtils.getMySQLUserName();
      String passWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();

      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_accounts_" + year + "?user=" + uName + "&password=" + passWord);

      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT AccCode, Description FROM acctdefn ORDER BY Description");

      String accCode, desc;

      while(rs.next())
      {
        accCode = rs.getString(1);
        desc    = rs.getString(2);

        s += "<option value=\"" + accCode + "\">" + desc + "\n";
      }

      rs.close();
      stmt.close();
      con.close();

      s += "</select>";
      return s;
    }
    catch(Exception e)
    {
      System.out.println("AccountsUtils: " + e);
      try
      {
        if(rs   != null) rs.close();
        if(stmt != null) stmt.close();
        if(con  != null) con.close();
      }
      catch(Exception e2) { }
    }

    s += "</select>";
    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getGSTDDL(Connection con, Statement stmt, ResultSet rs, String currentRateName, String name) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT GSTRateName FROM gstrate");

    String s = "<select name=\"" + name + "\" id=\"" + name + "\">";

    String gstRateName;

    while(rs.next())
    {
      gstRateName = rs.getString(1);

      if(gstRateName.length() > 0)
      {
        if(gstRateName.equals(currentRateName))
          s += "<option selected value=\"" + gstRateName + "\">" + gstRateName;
        else s += "<option value=\"" + gstRateName + "\">" + gstRateName;
      }
    }

    s += "</select>";

    rs.close();
    stmt.close();

    return s;
  }

}
