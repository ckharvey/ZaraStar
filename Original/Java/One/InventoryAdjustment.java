// =======================================================================================================================================================================================================
// System: ZaraStar Product: Stock-related Record Access
// Module: inventoryAdjustment.java
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

public class InventoryAdjustment
{
  GeneralUtils generalUtils = new GeneralUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getTableCreateStringStockC() throws Exception
  {  
    return "stockc ( CheckCode char(20) not null, ItemCode char(20) not null, StoreCode char(20), "
                  + "Date date,                   Level decimal(19,8),       Remark char(80), "
                  + "Status char(1),              SignOn char(20),            DateLastModified timestamp, "
                  + "Location char(20),           unused2 char(10),           Reconciled char(1),         Type char(1), "
                  + "unique(CheckCode))";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStringsStockC(String[] s) throws Exception
  {
    s[0] = "stockcItemCodeInx on stockc (ItemCode)";
    s[1] = "stockcDateInx on stockc (Date)";

    return 2;
  }
      
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNamesStockC() throws Exception
  {  
    return "CheckCode, ItemCode, StoreCode, Date, Level, Remark, Status, SignOn, DateLastModified, Location, unused2, Reconciled, Type";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypesStockC() throws Exception
  {
    return "CCCDFCCCSCCCC";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizesStockC(short[] sizes) throws Exception
  {
    sizes[0] = 20;   sizes[1] = 20;   sizes[2] = 20;   sizes[3] = 0;    sizes[4] = 0;   sizes[5] = 80;   sizes[6] = 1;
    sizes[7] = 20;   sizes[8] = -1;   sizes[9] = 20;   sizes[10] = 10;  sizes[11] = 1;  sizes[12] = 1; 
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public String getFieldStylesStockC() throws Exception
  {
    return "MMOMMOOOOOOOO";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getTableCreateStringCycle() throws Exception
  {
    return "cycle ( Year char(4) not null, IgnoreMondays char(1), IgnoreTuesdays char(1), IgnoreWednesdays char(1), IgnoreThursdays char(1), IgnoreFridays char(1), IgnoreSaturdays char(1), IgnoreSundays char(1), IgnorePublicHolidays char(250), "
                 + "unique(Year))";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStringsCycle(String[] s) throws Exception
  {
    return 0;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNamesCycle() throws Exception
  {
    return "Year, IgnoreMondays, IgnoreTuesdays, IgnoreWednesdays, IgnoreThursdays, IgnoreFridays, IgnoreSaturdays, IgnoreSundays, IgnorePublicHolidays";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypesCycle() throws Exception
  {
    return "CCCCCCCCC";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizesCycle(short[] sizes) throws Exception
  {
    sizes[0] = 4;   sizes[1] = 1;   sizes[2] = 1;   sizes[3] = 1;   sizes[4] = 1;   sizes[5] = 1;   sizes[6] = 1;  sizes[7] = 1;   sizes[8] = 250;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldStylesCycle() throws Exception
  {
    return "MOOOOOOOO";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getTableCreateStringCyclel() throws Exception
  {
    return "cyclel ( Year char(4) not null, Manufacturer char(30), Priority char(1), Frequency integer, "
                 + "unique(Year,Manufacturer))";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStringsCyclel(String[] s) throws Exception
  {
    return 0;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNamesCyclel() throws Exception
  {
    return "Year, Manufacturer, Priority, Frequency";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypesCyclel() throws Exception
  {
    return "CCCI";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizesCyclel(short[] sizes) throws Exception
  {
    sizes[0] = 4;   sizes[1] = 30;   sizes[2] = 1;   sizes[3] = 0;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldStylesCyclel() throws Exception
  {
    return "MOOO";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getTableCreateStringCycled() throws Exception
  {
    return "cycled ( CountDate date, "
                 + "unique(CountDate))";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStringsCycled(String[] s) throws Exception
  {
    return 0;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNamesCycled() throws Exception
  {
    return "CountDate";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypesCycled() throws Exception
  {
    return "D";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizesCycled(short[] sizes) throws Exception
  {
    sizes[0] = 0;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldStylesCycled() throws Exception
  {
    return "M";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getTableCreateStringStockA() throws Exception
  {  
    return "stocka ( AdjustmentCode char(20) not null, ItemCode char(20) not null, Date date,        Quantity decimal(15,10), SignOn char(20),     Remark char(80), StoreFrom char(20), StoreTo char(20), DateLastModified timestamp, "
                  + "Status char(1),                   SOCode char(20),            POCode char(20),  LocationFrom char(20),   LocationTo char(20), unique(AdjustmentCode))";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStringsStockA(String[] s) throws Exception
  {
    s[0] = "stockaItemCodeInx on stocka (ItemCode)";
    s[1] = "stockaDateInx on stocka (Date)";

    return 2;
  }
      
  // -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNamesStockA() throws Exception
  {  
    return "AdjustmentCode, ItemCode, Date, Quantity, SignOn, Remark, StoreFrom, StoreTo, DateLastModified, Status, SOCode, POCode, LocationFrom, LocationTo";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypesStockA() throws Exception
  {
    return "CCDFCCCCSCCCCC";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizesStockA(short[] sizes) throws Exception
  {
    sizes[0] = 20;   sizes[1] = 20;   sizes[2] = 0;   sizes[3] = 0;    sizes[4] = 20;   sizes[5] = 80;   sizes[6] = 20;
    sizes[7] = 20;   sizes[8] = -1;   sizes[9] = 10;  sizes[10] = 20;  sizes[11] = 20;  sizes[12] = 20;  sizes[13] = 20;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldStylesStockA() throws Exception
  {
    return "MMMMOOMMOOOOOO";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getTableCreateStringStockOpen() throws Exception
  {  
    return "stockopen ( Code char(20) not null, ItemCode char(20) not null, Date date, Level decimal(15,10), Cost decimal(20,10), Status char(1), SignOn char(20), DateLastModified timestamp, "
                     + "unique(Code))";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStringsStockOpen(String[] s) throws Exception
  {
    s[0] = "stockopenItemCodeInx on stockc (ItemCode)";
    s[1] = "stockopenDateInx on stockc (Date)";

    return 2;
  }
      
  // -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNamesStockOpen() throws Exception
  {  
    return "Code, ItemCode, Date, Level, Cost, Status, SignOn, DateLastModified";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypesStockOpen() throws Exception
  {
    return "CCDFFCCS";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizesStockOpen(short[] sizes) throws Exception
  {
    sizes[0] = 20;   sizes[1] = 20;   sizes[2] = 0;    sizes[3] = 0;    sizes[4] = 0;   sizes[5] = 1;   sizes[6] = 20; sizes[7] = -1; 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldStylesStockOpen() throws Exception
  {
    return "MMMMOOOO";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getTableCreateStringsCatalog() throws Exception
  {  
    return "catalog ( Manufacturer char(30), Category integer,     Chapter char(80),    PageName char(80), Title char(80), "
                   + "ImageSmall char(30),   ImageSmall2 char(30), ImageLarge char(30), Download integer,  ExternalURL char(100), "
                   + "Heading1 char(30),     Heading2 char(30),    Heading3 char(30),   Heading4 char(30), Heading5 char(30), "
                   + "Heading6 char(30),     Heading7 char(30),    Heading8 char(30),   Heading9 char(30), Heading10 char(30), "
                   + "NewProduct char(1),    CategoryLink integer, Text mediumtext,     Text2 mediumtext,  Section char(80), "
                   + "OriginalPage char(10), "       
                   + "unique(Manufacturer, Category))";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStringsCatalog(String[] s) throws Exception
  {
    s[0] = "catalogManufacturerCategoryInx on catalog (Manufacturer, Category)";
    return 1;
  }
      
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNamesCatalog() throws Exception
  {  
    return "Manufacturer, Category, Chapter, PageName, Title, ImageSmall, ImageSmall2, ImageLarge, Download, ExternalURL, Heading1, Heading2, "
         + "Heading3, Heading4, Heading5, Heading6, Heading7, Heading8, Heading9, Heading10, NewProduct, CategoryLink, Text, Text2, Section, "
         + "OriginalPage";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypesCatalog() throws Exception
  {
    return "CICCCCCCICCCCCCCCCCCCIMMCC";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizesCatalog(short[] sizes) throws Exception
  {
    sizes[0]  = 30;  sizes[1]  = 0;  sizes[2]  = 80; sizes[3]  = 80; sizes[4]  = 80; sizes[5]  = 30; sizes[6]  = 30; sizes[7]  = 30; sizes[8] = 0;   
    sizes[9]  = 100; sizes[10] = 30; sizes[11] = 30; sizes[12] = 30; sizes[13] = 30; sizes[14] = 30; sizes[15] = 30; sizes[16] = 30; sizes[17] = 30; 
    sizes[18] = 30;  sizes[19] = 30; sizes[20] = 1;  sizes[21] = 0;  sizes[22] = -2; sizes[23] = -2; sizes[24] = 80; sizes[25] = 10;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldStylesCatalog() throws Exception
  {
    return "MMOOOOOOOOOOOOOOOOOOOOOOOO";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getTableCreateStringsCatalogL() throws Exception
  {  
    return "catalogl ( Manufacturer char(30), Category integer, ManufacturerCode char(60), Entry1 char(100),     Entry2 char(100), "
                    + "Entry3 char(100),      Entry4 char(100), Entry5 char(100),          Entry6 char(100),     Entry7 char(100), "
                    + "Entry8 char(100),      Entry9 char(100), Entry10 char(100),         Description char(80), Description2 char(80), "
                    + "Line integer, "
                    + "unique(Manufacturer, Category, ManufacturerCode))";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStringsCatalogL(String[] s) throws Exception
  {
    s[0] = "catalogManufacturerCategoryManufacturerCodeInx on catalogl (Manufacturer, Category, ManufacturerCode)";
    s[1] = "catalogManufacturerCodeInx on catalogl (ManufacturerCode)";
    return 2;
  }
      
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNamesCatalogL() throws Exception
  {  
    return "Manufacturer, Category, ManufacturerCode, Entry1, Entry2, Entry3, Entry4, Entry5, Entry6, Entry7, Entry8, Entry9, Entry10, Description, "
         + "Description2, Line";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypesCatalogL() throws Exception
  {
    return "CICCCCCCCCCCCCCI";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizesCatalogL(short[] sizes) throws Exception
  {
    sizes[0] = 30;  sizes[1] = 0;   sizes[2]  = 60;  sizes[3]  = 100; sizes[4]  = 100; sizes[5]  = 100; sizes[6]  = 100; sizes[7]  = 100;
    sizes[8] = 100; sizes[9] = 100; sizes[10] = 100; sizes[11] = 100; sizes[12] = 100; sizes[13] = 80;  sizes[14] = 80;  sizes[15] = 1;
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public String getFieldStylesCatalogL() throws Exception
  {
    return "MMMOOOOOOOOOOOOO";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getTableCreateStringsCatalogC() throws Exception
  {  
    return "catalogc ( Manufacturer char(30), Chapter char(80), Section char(80), Page char(80), Position integer )";
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStringsCatalogC(String[] s) throws Exception
  {
    return 0;
  }
      
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNamesCatalogC() throws Exception
  {  
    return "Manufacturer, Chapter, Section, Page, Position";
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypesCatalogC() throws Exception
  {
    return "CCCCI";
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizesCatalogC(short[] sizes) throws Exception
  {
    sizes[0] = 30;   sizes[1] = 80;    sizes[2] = 80;   sizes[3] = 80;   sizes[4] = 0;
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public String getFieldStylesCatalogC() throws Exception
  {
    return "MMMMM";
  }


  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getTableCreateStringsCatalogS() throws Exception
  {  
    return "catalogs ( Manufacturer char(30), CatalogManufacturerCode char(60), AlternativeManufacturerCode char(60) )";
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStringsCatalogS(String[] s) throws Exception
  {
    return 0;
  }
      
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNamesCatalogS() throws Exception
  {  
    return "Manufacturer, CatalogManufacturerCode, AlternativeManufacturerCode";
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypesCatalogS() throws Exception
  {
    return "CCC";
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizesCatalogS(short[] sizes) throws Exception
  {
    sizes[0] = 30;   sizes[1] = 60;   sizes[2] = 60;
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public String getFieldStylesCatalogS() throws Exception
  {
    return "MMM";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getTableCreateStringsBuyersCatalog() throws Exception
  {  
    return "buyerscatalog ( CustomerCode char(20), ItemCode char(20),     BuyerItemCode1 char(40), BuyerItemCode2 char(40), BuyerItemCode3 char(40), "
                         + "Section integer,       SectionName char(100), Band integer,            BelowSame char(1),       Entry integer, "       
                         + "unique(CustomerCode, ItemCode))";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStringsBuyersCatalog(String[] s) throws Exception
  {
    s[0] = "buyerItemCode1Inx on buyerscatalog (BuyerItemCode1)";
    s[1] = "sectionInx on buyerscatalog (Section)";

    return 2;
  }
      
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNamesBuyersCatalog() throws Exception
  {  
    return "CustomerCode, ItemCode, BuyerItemCode1, BuyerItemCode2, BuyerItemCode3, Section, SectionName, Band, BelowSame, Entry";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypesBuyersCatalog() throws Exception
  {
    return "CCCCCICICI";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizesBuyersCatalog(short[] sizes) throws Exception
  {
    sizes[0] = 20;  sizes[1] = 20;  sizes[2] = 40;  sizes[3] = 40;  sizes[4] = 40;  sizes[5] = 0;  sizes[6] = 100;  sizes[7] = 0;  sizes[8] = 1;
    sizes[9] = 0;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldStylesBuyersCatalog() throws Exception
  {
    return "MMOOOOOOOO";
  }


  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getTableCreateStringsCatalogList() throws Exception
  {  
    return "cataloglist ( Manufacturer char(30), Title char(100),        Image char(100),        Description mediumtext,      Type char(1), "
                        + "CatalogType char(1),  PublishCatalog char(1), PublishPricing char(1), PublishAvailability char(1), "
                        + "unique(Manufacturer, CatalogType))";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStringsCatalogList(String[] s) throws Exception
  {
    s[0] = "\000";
    return 0;
  }
      
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNamesCatalogList() throws Exception
  {  
    return "Manufacturer, Title, Image, Description, Type, CatalogType, PublishCatalog, PublishPricing, PublishAvailability";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypesCatalogList() throws Exception
  {
    return "CCCCCCCCC";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizesCatalogList(short[] sizes) throws Exception
  {
    sizes[0] = 30;  sizes[1] = 100;  sizes[2] = 100;  sizes[3] = 0;  sizes[4] = 1;  sizes[5] = 1;  sizes[6] = 1;  sizes[7] = 1;  sizes[8] = 1;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldStylesCatalogList() throws Exception
  {
    return "MOOOMOOOO";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void getSectionDetailsGivenService(Connection con, Statement stmt, ResultSet rs, String customerCode, String itemCode, String[] section,
                                            String[] sectionName) throws Exception
  {
    section[0]     = "0";
    sectionName[0] = "0";
    
    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT Section, SectionName FROM buyerscatalog WHERE CustomerCode = '" + customerCode + "' AND ItemCode = '" + itemCode
                           + "'"); 
      
      if(rs.next())
      {
        section[0]     = rs.getString(1);
        sectionName[0] = rs.getString(2);
      }
      
      if(rs   != null) rs.close();        
      if(stmt != null) stmt.close();        
    }
    catch(Exception e)
    {
      System.out.println("InventoryAdjustment: Desc: " + e);
      if(stmt != null) stmt.close();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void getCycleForYear(Connection con, Statement stmt, ResultSet rs, String year, String[] ignoreMondays, String[] ignoreTuesdays, String[] ignoreWednesdays, String[] ignoreThursdays, String[] ignoreFridays, String[] ignoreSaturdays,
                              String[] ignoreSundays, String[] ignorePublicHolidays) throws Exception
  {
    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT * FROM cycle WHERE Year = '" + year + "'");

      if(rs.next())
      {
        ignoreMondays[0]        = rs.getString(2);
        ignoreTuesdays[0]       = rs.getString(3);
        ignoreWednesdays[0]     = rs.getString(4);
        ignoreThursdays[0]      = rs.getString(5);
        ignoreFridays[0]        = rs.getString(6);
        ignoreSaturdays[0]      = rs.getString(7);
        ignoreSundays[0]        = rs.getString(8);
        ignorePublicHolidays[0] = rs.getString(9);
        
        if(ignorePublicHolidays[0] == null) ignorePublicHolidays[0] = "";
      }
      else
      {
        ignoreMondays[0]        = "N";
        ignoreTuesdays[0]       = "N";
        ignoreWednesdays[0]     = "N";
        ignoreThursdays[0]      = "N";
        ignoreFridays[0]        = "N";
        ignoreSaturdays[0]      = "Y";
        ignoreSundays[0]        = "Y";
        ignorePublicHolidays[0] = "";
      }
    }
    catch(Exception e)
    {
      System.out.println("InventoryAdjustment: " + e);
      ignoreMondays[0]        = "N";
      ignoreTuesdays[0]       = "N";
      ignoreWednesdays[0]     = "N";
      ignoreThursdays[0]      = "N";
      ignoreFridays[0]        = "N";
      ignoreSaturdays[0]      = "Y";
      ignoreSundays[0]        = "Y";
      ignorePublicHolidays[0] = "";
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean updateCycleForYear(Connection con, Statement stmt, ResultSet rs, String year, String ignoreMondays, String ignoreTuesdays, String ignoreWednesdays, String ignoreThursdays, String ignoreFridays, String ignoreSaturdays,
                                    String ignoreSundays, String ignorePublicHolidays, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    try
    {
      // check if rec already exists
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT COUNT(Year) AS rowcount FROM cycle WHERE Year = '" + year + "'");
      int rowCount = 0;
      if(rs.next())
        rowCount = rs.getInt("rowcount");
      if(rs != null) rs.close();

      if(stmt != null) stmt.close();

      String q;

      if(rowCount != 0) // already exists
      {
        q = "UPDATE cycle SET IgnoreMondays = '" + ignoreMondays + "', IgnoreTuesdays = '" + ignoreTuesdays + "', IgnoreWednesdays = '" + ignoreWednesdays + "', IgnoreThursdays = '" + ignoreThursdays + "', IgnoreFridays = '" + ignoreFridays
          + "', IgnoreSaturdays = '" + ignoreSaturdays + "', IgnoreSundays = '" + ignoreSundays + "', IgnorePublicHolidays = '" + generalUtils.sanitiseForSQL(ignorePublicHolidays) + "' WHERE Year = '" + year + "'";
      }
      else // create new rec
      {
        q = "INSERT INTO cycle (Year, IgnoreMondays, IgnoreTuesdays, IgnoreWednesdays, IgnoreThursdays, IgnoreFridays, IgnoreSaturdays, IgnoreSundays, IgnorePublicHolidays ) VALUES ('" + year + "','" + ignoreMondays + "','" + ignoreTuesdays
          + "','" + ignoreWednesdays + "','" + ignoreThursdays + "','" + ignoreFridays + "','" + ignoreSaturdays + "','" + ignoreSundays + "','" + generalUtils.sanitiseForSQL(ignorePublicHolidays) + "' )";
      }

      stmt = con.createStatement();

      stmt.executeUpdate(q);

      if(stmt != null) stmt.close();

      return true;
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(rs != null) rs.close();
      if(stmt != null) stmt.close();
    }

    return false;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getMfrsDDL(Connection con, Statement stmt, ResultSet rs, String currentMfr, String name, boolean addALL) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT DISTINCT Manufacturer FROM stock ORDER BY Manufacturer");

    String s = "<select name=\"" + name + "\" id=\"" + name + "\">";

    if(addALL)
      s += "<option value=\"___ALL\">ALL";

    String mfr;
    boolean oneFound = false;

    while(rs.next())
    {
      mfr = rs.getString(1).trim();
      if(mfr.length() > 0)
      {
        if(mfr.equals(currentMfr))
          s += "<option selected value=\"" + mfr + "\">" + mfr;
        else s += "<option value=\"" + mfr + "\">" + mfr;
      }

      oneFound = true;
    }

    if(oneFound == false)
      s += "<option selected value=\"-\">-";

    s += "</select>";

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    return s;
  }

}
