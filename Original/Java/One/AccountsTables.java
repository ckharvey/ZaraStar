// =======================================================================================================================================================================================================
// System: ZaraStar Accounts: Record Access
// Module: accountsTables.java
// Author: C.K.Harvey
// Copyright (c) 2001-09 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

public class AccountsTables
{
  GeneralUtils generalUtils = new GeneralUtils();
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getTableCreateStringCurrency() throws Exception
  {  
    return "currency ( CurrencyCode char(3) not null, CurrencyType char(1) not null, CurrencyDesc char(40) not null,"
          + " unique(CurrencyCode, CurrencyType, CurrencyDesc))";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStringsCurrency(String[] s) throws Exception
  {
    s[0] = "\000"; // remove compiler warning
    return 0;
  }
      
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNamesCurrency() throws Exception
  {  
    return "CurrencyCode, CurrencyType, CurrencyDesc";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypesCurrency() throws Exception
  {
    return "CCC";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizesCurrency(short[] sizes) throws Exception
  {
    sizes[0] = 3;   sizes[1] = 1;    sizes[2] = 40; 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldStylesCurrency() throws Exception
  {
    return "MMM";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getTableCreateStringCurrRate() throws Exception
  {  
    return "currrate ( CurrencyCode char(3) not null, RateDate date not null, RateValue decimal(16,8) not null, Note char(40),"
                   + " unique(CurrencyCode, RateDate))";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStringsCurrRate(String[] s) throws Exception
  {
    return 0;
  }
      
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNamesCurrRate() throws Exception
  {  
    return "CurrencyCode, RateDate, RateValue, Note";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypesCurrRate() throws Exception
  {
    return "CDFC";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizesCurrRate(short[] sizes) throws Exception
  {
    sizes[0] = 3;   sizes[1] = -1;    sizes[2] = 40;   sizes[3] = 40; 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldStylesCurrRate() throws Exception
  {
    return "MMMO";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getTableCreateStringGSTRate() throws Exception
  {  
    return "gstrate ( GSTRateName char(20) not null, Type char(1) not null, GSTRate decimal(16,8),"
                  + " unique(GSTRateName, Type, GSTRate))";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStringsGSTRate(String[] s) throws Exception
  {
    return 0;
  }
      
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNamesGSTRate() throws Exception
  {  
    return "GSTRateName, Type, GSTRate";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypesGSTRate() throws Exception
  {
    return "CCF";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizesGSTRate(short[] sizes) throws Exception
  {
    sizes[0] = 20;   sizes[1] = 1;    sizes[2] = -1; 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldStylesGSTRate() throws Exception
  {
    return "MMM";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getTableCreateStringBankAccount() throws Exception
  {  
    return "bankaccount ( BankAccountName char(20) not null, Type char(1) not null, Account char(20),"
        + " unique(BankAccountName, Type, Account))";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStringsBankAccount(String[] s) throws Exception
  {
    return 0;
  }
      
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNamesBankAccount() throws Exception
  {  
    return "BankAccountName, Type, Account";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypesBankAccount() throws Exception
  {
    return "CCC";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizesBankAccount(short[] sizes) throws Exception
  {
    sizes[0] = 20;   sizes[1] = 1;    sizes[2] = 20; 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldStylesBankAccount() throws Exception
  {
    return "MMM";
  }

}
