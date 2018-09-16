// =======================================================================================================================================================================================================
// System: ZaraStar AdminEngine: Misc Record Access
// Module: definitionTables.java
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

public class DefinitionTables
{
  GeneralUtils generalUtils = new GeneralUtils();

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getTableCreateStringAppConfig() throws Exception
  {
    return "appconfig ( CompanyName char(80) not null, DNM char(40),                     CurrentStyle char(40), " // CurrentStyle not used
                     + "ApplicationStartDate date,     FinancialYearStartMonth char(20), FinancialYearEndMonth char(20), "
                     + "EffectiveStartDate date,       CompanyPhone char(80),            CompanyFax char(80), "
                     + "CompanyEMail char(80),         Description char(250),            TerseName char(20), "
                     + "DateStyle char(1),             DateSeparator char(1),            Latitude char(20),  Longitude char(20), GoogleMapsKey char(200), "
                     + "unique (DNM) )";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStringsAppConfig(String[] s) throws Exception
  {
    return 0;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNamesAppConfig() throws Exception
  {
    return "CompanyName, DNM, CurrentStyle, ApplicationStartDate, FinancialYearStartMonth, FinancialYearEndMonth, EffectiveStartDate, CompanyPhone, CompanyFax, CompanyEMail, Description, TerseName, DateStyle, DateSeparator, Latitude,"
         + "Longitude";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypesAppConfig() throws Exception
  {
    return "CCCDCCDCCCCCCCCC";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizesAppConfig(short[] sizes) throws Exception
  {
    sizes[0] = 80;  sizes[1] = 40;  sizes[2] = 40;  sizes[3] = 0;  sizes[4] = 20;  sizes[5] = 20;  sizes[6] = 0;  sizes[7] = 80;  sizes[8] = 80; sizes[9] = 80;  sizes[10] = 250;  sizes[11] = 20;  sizes[12] =1;  sizes[13] = 1;  sizes[14] = 20;
    sizes[15] = 20;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldStylesAppConfig() throws Exception
  {
    return "MOOOOOOOOOOOOOOO";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getTableCreateStringServers() throws Exception
  {
    return "servers ( Name char(40) not null, Type char(1), InternalIP char(60), ExternalIP char(60), unique (Name) )";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStringsServers(String[] s) throws Exception
  {
    return 0;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNamesServers() throws Exception
  {
    return "Name, Type, InternalIP, ExternalIP";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypesServers() throws Exception
  {
    return "CCCC";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizesServers(short[] sizes) throws Exception
  {
    sizes[0] = 40;  sizes[1] = 1;  sizes[2] = 60;  sizes[3] = 60;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldStylesServers() throws Exception
  {
    return "MOOO";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getTableCreateStringSCSites() throws Exception
  {
    return "scsites ( DNM char(40) not null, URL char(80) not null, eMail char(80), Type char(1), Active char(1), CompanyName char(80), "
                   + "Address1 char(60), Address2 char(60), Address3 char(60), Address4 char(60), Address5 char(60), Country char(60), "
                   + "Phone1 char(30), Phone2 char(30), Fax1 char(30), Fax2 char(30), Description char(250), PassWord char(20), "
                   + "unique (DNM) )";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStringsSCSites(String[] s) throws Exception
  {
    return 0;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNamesSCSites() throws Exception
  {
    return "DNM, URL, eMail, Type, Active, CompanyName, Address1, Address2, Address3, Address4, Address5, Country, Phone1, Phone2, Fax1, Fax2, Description, PassWord";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypesSCSites() throws Exception
  {
    return "CCCCCCCCCCCCCCCCCC";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizesSCSites(short[] sizes) throws Exception
  {
    sizes[0] = 40;  sizes[1] = 80;  sizes[2] = 80;  sizes[3] = 1;  sizes[4] = 1;  sizes[5] = 80;  sizes[6] = 60;  sizes[7] = 60;  sizes[8] = 60;
    sizes[9] = 60;  sizes[10] = 60;  sizes[11] = 60;  sizes[12] = 30;  sizes[13] = 30;  sizes[14] = 30;  sizes[15] = 30;  sizes[16] = 250;
    sizes[18] = 20;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldStylesSCSites() throws Exception
  {
    return "MOOOOOOOOOOOOOOOOO";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getTableCreateStringSCSitesBrands() throws Exception
  {
    return "scsitesbrands ( DNM char(40) not null, Brand char(40) not null, unique (DNM, Brand) )";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStringsSCSitesBrands(String[] s) throws Exception
  {
    s[0] = "brandInx on scsitesbrands (Brand)";
    return 1;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNamesSCSitesBrands() throws Exception
  {
    return "DNM, Brand";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypesSCSitesBrands() throws Exception
  {
    return "CC";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizesSCSitesBrands(short[] sizes) throws Exception
  {
    sizes[0] = 40;  sizes[1] = 40;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldStylesSCSitesBrands() throws Exception
  {
    return "MM";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getTableCreateStringSCBrands() throws Exception
  {
    return "scbrands ( Brand char(40) not null, unique (Brand) )";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStringsSCBrands(String[] s) throws Exception
  {
    return 0;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNamesSCBrands() throws Exception
  {
    return "Brand";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypesSCBrands() throws Exception
  {
    return "C";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizesSCBrands(short[] sizes) throws Exception
  {
    sizes[0] = 40;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldStylesSCBrands() throws Exception
  {
    return "M";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getTableCreateStringRobotText() throws Exception
  {
    return "robottext ( PageURL char(100) not null, DNM char(40), TextBit mediumtext, Title char(80) )";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStringsRobotText(String[] s) throws Exception
  {
    s[0]="\000"; // remove compiler warning
    return 0;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNamesRobotText() throws Exception
  {
    return "URL, DNM, TextBit, Title";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypesRobotText() throws Exception
  {
    return "CCCC";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizesRobotText(short[] sizes) throws Exception
  {
    sizes[0] = 100;  sizes[1] = 40;  sizes[2] = 0;  sizes[3] = 80;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldStylesRobotText() throws Exception
  {
    return "MOOO";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getTableCreateStringStockCat() throws Exception
  {
    return "stockcat ( CategoryCode integer not null, Description char(80),  Image char(100),            Manufacturer char(40), Page integer, "
                    + "Download integer,              Text char(100),        WikiPage integer,           URL char(100),         NoPrices char(1), "
                    + "NoAvailability char(1),        CategoryLink integer,  OrderByDescription char(1), Style char(20),        Text2 char(100), "
                    + "unique (CategoryCode) )";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStringsStockCat(String[] s) throws Exception
  {
    s[0]="\000"; // remove compiler warning
    return 0;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNamesStockCat() throws Exception
  {
    return "CategoryCode, Description, Image, Manufacturer, Page, Download, Text, WikiPage, URL, NoPrices, NoAvailability, CategoryLink, "
          + "OrderByDescription, Style, Text2";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypesStockCat() throws Exception
  {
    return "ICCCIICICCCICCC";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizesStockCat(short[] sizes) throws Exception
  {
    sizes[0] = 0;    sizes[1] = 80;   sizes[2]  = 100;   sizes[3]  = 40;   sizes[4] = 0;   sizes[5] = 0;      sizes[6] = 100;   sizes[7] = 0;
    sizes[8] = 100;  sizes[9] = 1;    sizes[10] = 1;     sizes[11] = 0;    sizes[12] = 1;  sizes[13] = 100;   sizes[14] = 20;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldStylesStockCat() throws Exception
  {
    return "MOOOOOOOOOOOOOO";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getTableCreateStringLinkedCat() throws Exception
  {
    return "linkedcat ( Manufacturer char(30) not null,    PricingUpline char(40),            PricingURL char(80),    UserName char(20), "
                     + "PassWord char(20),                 MarkupPercentage decimal(17,8),    ShowPrices char(1),     ShowAvailability char(1), "
                     + "Description char(100),             CatalogURL char(80),               CatalogUpline char(40), DiscountPercentage1 decimal(17,8), "
                     + "DiscountPercentage2 decimal(17,8), DiscountPercentage3 decimal(17,8), DiscountPercentage4 decimal(17,8), CatalogType char(1), "
                     + "Currency char(3), PriceBasis char(1), "
                     + "unique (Manufacturer, CatalogType) )";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStringsLinkedCat(String[] s) throws Exception
  {
    s[0]="\000"; // remove compiler warning
    return 0;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNamesLinkedCat() throws Exception
  {
    return "Manufacturer, PricingUpline, PricingURL, UserName, PassWord, MarkupPercentage, ShowPrices, ShowAvailability, Description, CatalogURL, "
         + "CatalogUpline, DiscountPercentage1, DiscountPercentage2, DiscountPercentage3, DiscountPercentage4, CatalogType, Currency, PriceBasis";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypesLinkedCat() throws Exception
  {
    return "CCCCCFCCCCCFFFFCCC";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizesLinkedCat(short[] sizes) throws Exception
  {
    sizes[0] = 30;  sizes[1] = 40;   sizes[2] = 80;  sizes[3] = 20;  sizes[4] = 20;  sizes[5] = 0;   sizes[6] = 1;  sizes[7] = 1;   sizes[8] = 100;
    sizes[9] = 80;  sizes[10] = 40;  sizes[11] = 0;  sizes[12] = 0;  sizes[13] = 0;  sizes[14] = 0;  sizes[15] = 1; sizes[16] = 3;  sizes[17] = 1;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldStylesLinkedCat() throws Exception
  {
    return "MOOOOOOOOOOOOOOOOO";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getTableCreateStringDocumentOptions() throws Exception
  {
    return "documentoptions ( Option1 char(1),  Option2 char(1),  Option3 char(1),  Option4 char(1),  Option5 char(1), "
                           + "Option6 char(1),  Option7 char(1),  Option8 char(1),  Option9 char(1),  Option10 char(1), "
                           + "Option11 char(1), Option12 char(1), Option13 char(1), Option14 char(1), Option15 char(1), "
                           + "Option16 char(1), Option17 char(1), Option18 char(1), Option19 char(1), Option20 char(1), "
                           + "Option21 char(1), Option22 char(1), Option23 char(1), Option24 char(1), Option25 char(1), "
                           + "Option26 char(1), Option27 char(1), Option28 char(1), Option29 char(1), Option30 char(1), "
                           + "Option31 char(1), Option32 char(1), Option33 char(1), Option34 char(1), Option35 char(1), "
                           + "Option36 char(1), Option37 char(1), Option38 char(1), Option39 char(1), Option40 char(1), "
                           + "Option41 char(1), Option42 char(1), Option43 char(1), Option44 char(1), Option45 char(1), "
                           + "Option46 char(1), Option47 char(1), Option48 char(1), Option49 char(1), Option50 char(1), "
                           + "Option51 char(1), Option52 char(1), Option53 char(1), Option54 char(1), Option55 char(1), "
                           + "Option56 char(1), Option57 char(1), Option58 char(1), Option59 char(1), Option60 char(1), "
                           + "Option61 char(1), Option62 char(1), Option63 char(1), Option64 char(1), Option65 char(1), "
                           + "Option66 char(1), Option67 char(1), Option68 char(1), Option69 char(1), Option70 char(1), "
                           + "Option71 char(1), Option72 char(1), Option73 char(1), Option74 char(1), Option75 char(1), "
                           + "Option76 char(1), Option77 char(1), Option78 char(1), Option79 char(1), Option80 char(1), "
                           + "Option81 char(1), Option82 char(1), Option83 char(1), Option84 char(1), Option85 char(1), "
                           + "Option86 char(1), Option87 char(1), Option88 char(1), Option89 char(1), Option90 char(1), "
                           + "Option91 char(1), Option92 char(1), Option93 char(1), Option94 char(1), Option95 char(1), "
                           + "Option96 char(1), Option97 char(1), Option98 char(1), Option99 char(1), Option100 char(1) ) ";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStringsDocumentOptions(String[] s) throws Exception
  {
    return 0;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNamesDocumentOptions() throws Exception
  {
    return "Option1,  Option2,  Option3,  Option4,  Option5,  Option6,  Option7,  Option8,  Option9,  Option10, "
         + "Option11, Option12, Option13, Option14, Option15, Option16, Option17, Option18, Option19, Option20"
         + "Option21, Option22, Option23, Option24, Option25, Option26, Option27, Option28, Option29, Option30"
         + "Option31, Option32, Option33, Option34, Option35, Option36, Option37, Option38, Option39, Option40"
         + "Option41, Option42, Option43, Option44, Option45, Option46, Option47, Option48, Option49, Option50"
         + "Option51, Option52, Option53, Option54, Option55, Option56, Option57, Option58, Option59, Option60"
         + "Option61, Option62, Option63, Option64, Option65, Option66, Option67, Option68, Option69, Option70"
         + "Option71, Option72, Option73, Option74, Option75, Option76, Option77, Option78, Option79, Option80"
         + "Option81, Option82, Option83, Option84, Option85, Option86, Option87, Option88, Option89, Option90"
         + "Option91, Option92, Option93, Option94, Option95, Option96, Option97, Option98, Option99, Option100";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypesDocumentOptions() throws Exception
  {
    return "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizesDocumentOptions(short[] sizes) throws Exception
  {
    sizes[0]  = 1; sizes[1]  = 1; sizes[2]  = 1;  sizes[3]  = 1;  sizes[4]  = 1;  sizes[5]  = 1;  sizes[6]  = 1;  sizes[7]  = 1;  sizes[8]  = 1;  sizes[9]  = 1;
    sizes[10] = 1; sizes[11] = 1; sizes[12] = 1;  sizes[13] = 1;  sizes[14] = 1;  sizes[15] = 1;  sizes[16] = 1;  sizes[17] = 1;  sizes[18] = 1;  sizes[19] = 1;
    sizes[20] = 1; sizes[21] = 1; sizes[22] = 1;  sizes[23] = 1;  sizes[24] = 1;  sizes[25] = 1;  sizes[26] = 1;  sizes[27] = 1;  sizes[28] = 1;  sizes[29] = 1;
    sizes[30] = 1; sizes[31] = 1; sizes[32] = 1;  sizes[33] = 1;  sizes[34] = 1;  sizes[35] = 1;  sizes[36] = 1;  sizes[37] = 1;  sizes[38] = 1;  sizes[39] = 1;
    sizes[40] = 1; sizes[41] = 1; sizes[42] = 1;  sizes[43] = 1;  sizes[44] = 1;  sizes[45] = 1;  sizes[46] = 1;  sizes[47] = 1;  sizes[48] = 1;  sizes[49] = 1;
    sizes[50] = 1; sizes[51] = 1; sizes[52] = 1;  sizes[53] = 1;  sizes[54] = 1;  sizes[55] = 1;  sizes[56] = 1;  sizes[57] = 1;  sizes[58] = 1;  sizes[59] = 1;
    sizes[60] = 1; sizes[61] = 1; sizes[62] = 1;  sizes[63] = 1;  sizes[64] = 1;  sizes[65] = 1;  sizes[66] = 1;  sizes[67] = 1;  sizes[68] = 1;  sizes[69] = 1;
    sizes[70] = 1; sizes[71] = 1; sizes[72] = 1;  sizes[73] = 1;  sizes[74] = 1;  sizes[75] = 1;  sizes[76] = 1;  sizes[77] = 1;  sizes[78] = 1;  sizes[79] = 1;
    sizes[80] = 1; sizes[81] = 1; sizes[82] = 1;  sizes[83] = 1;  sizes[84] = 1;  sizes[85] = 1;  sizes[86] = 1;  sizes[87] = 1;  sizes[88] = 1;  sizes[89] = 1;
    sizes[90] = 1; sizes[91] = 1; sizes[92] = 1;  sizes[93] = 1;  sizes[94] = 1;  sizes[95] = 1;  sizes[96] = 1;  sizes[97] = 1;  sizes[98]  = 1; sizes[99] = 1;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldStylesDocumentOptions() throws Exception
  {
    return "OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getTableCreateStringDocumentSettings() throws Exception
  {
    return "documentsettings ( Option1 char(10),  Option2 char(100),  Option3 char(10),  Option4 char(10),  Option5 char(10), "
                            + "Option6 char(10),  Option7 char(10),  Option8 char(10),  Option9 char(10),  Option10 char(10), "
                            + "Option11 char(10), Option12 char(10), Option13 char(10), Option14 char(10), Option15 char(10), "
                            + "Option16 char(10), Option17 char(10), Option18 char(10), Option19 char(10), Option20 char(10), "
                            + "Option21 char(10), Option22 char(10), Option23 char(10), Option24 char(10), Option25 char(10), "
                            + "Option26 char(10), Option27 char(10), Option28 char(10), Option29 char(10), Option30 char(10), "
                            + "Option31 char(10), Option32 char(10), Option33 char(10), Option34 char(10), Option35 char(10), "
                            + "Option36 char(10), Option37 char(10), Option38 char(10), Option39 char(10), Option40 char(10), "
                            + "Option41 char(10), Option42 char(10), Option43 char(10), Option44 char(10), Option45 char(10), "
                            + "Option46 char(10), Option47 char(10), Option48 char(10), Option49 char(10), Option50 char(10) )";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStringsDocumentSettings(String[] s) throws Exception
  {
    return 0;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNamesDocumentSettings() throws Exception
  {
    return "Option1,  Option2,  Option3,  Option4,  Option5,  Option6,  Option7,  Option8,  Option9,  Option10, "
         + "Option11, Option12, Option13, Option14, Option15, Option16, Option17, Option18, Option19, Option20"
         + "Option21, Option22, Option23, Option24, Option25, Option26, Option27, Option28, Option29, Option30"
         + "Option31, Option32, Option33, Option34, Option35, Option36, Option37, Option38, Option39, Option40"
         + "Option41, Option42, Option43, Option44, Option45, Option46, Option47, Option48, Option49, Option50";
   }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypesDocumentSettings() throws Exception
  {
    return "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizesDocumentSettings(short[] sizes) throws Exception
  {
    sizes[0]  = 10; sizes[1]  = 10; sizes[2]  = 100; sizes[3]  = 10; sizes[4]  = 10; sizes[5]  = 10; sizes[6]  = 10; sizes[7]  = 10; sizes[8]  = 10; sizes[9]  = 10;
    sizes[10] = 10; sizes[11] = 10; sizes[12] = 10; sizes[13] = 10; sizes[14] = 10; sizes[15] = 10; sizes[16] = 10; sizes[17] = 10; sizes[18] = 10; sizes[19] = 10;
    sizes[20] = 10; sizes[21] = 10; sizes[22] = 10; sizes[23] = 10; sizes[24] = 10; sizes[25] = 10; sizes[26] = 10; sizes[27] = 10; sizes[28] = 10; sizes[29] = 10;
    sizes[30] = 10; sizes[31] = 10; sizes[32] = 10; sizes[33] = 10; sizes[34] = 10; sizes[35] = 10; sizes[36] = 10; sizes[37] = 10; sizes[38] = 10; sizes[39] = 10;
    sizes[40] = 10; sizes[41] = 10; sizes[42] = 10; sizes[43] = 10; sizes[44] = 10; sizes[45] = 10; sizes[46] = 10; sizes[47] = 10; sizes[48] = 10; sizes[49] = 10;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldStylesDocumentSettings() throws Exception
  {
    return "OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getTableCreateStringUsers() throws Exception
  {
    return "users ( UserCode char(20) not null, UserName char(60), PassWord char(40), ExternalAccess char(1), Status char(1), "
          + "PhoneExtension char(10), DateJoined date, DateLeft date, ShowInDirectory char(1), BioPage integer, "
          + "Designation char(40), Misc1 char(20), Misc2 char(20), Misc3 char(40), Misc4 char(1), Misc5 char(1), "
          + "unique (UserCode) )";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStringsUsers(String[] s) throws Exception
  {
    s[0]="\000"; // remove compiler warning
    return 0;
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNamesUsers() throws Exception
  {
    return "UserCode, UserName, PassWord, ExternalAccess, Status, PhoneExtension, DateJoined, DateLeft, ShowInDirectory, BioPage, "
          + "Designation, Misc1, Misc2, Misc3, Misc4, Misc5";
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypesUsers() throws Exception
  {
    return "CCCCCCDDCICCCCCC";
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizesUsers(short[] sizes) throws Exception
  {
    sizes[0] = 20;  sizes[1] = 60;  sizes[2] = 40;   sizes[3] = 1;    sizes[4] = 1;    sizes[5] = 10;  sizes[6] = 0;  sizes[7] = 0;
    sizes[8] = 1;   sizes[9] = 0 ;  sizes[10] = 40;  sizes[11] = 20;  sizes[12] = 20;  sizes[13] = 40; sizes[14] = 1; sizes[15] = 1;
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public String getFieldStylesUsers() throws Exception
  {
    return "MMMOOOOOOOOOOOOO";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getTableCreateStringUserModules() throws Exception
  {
    return "usermodules ( UserCode char(20) not null, Module char(30) not null, "
                       + "unique (UserCode, Module) )";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStringsUserModules(String[] s) throws Exception
  {
    s[0]="\000"; // remove compiler warning
    return 0;
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNamesUserModules() throws Exception
  {
    return "UserCode, Module";
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypesUserModules() throws Exception
  {
    return "CC";
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizesUserModules(short[] sizes) throws Exception
  {
    sizes[0] = 20;  sizes[1] = 30;
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public String getFieldStylesUserModules() throws Exception
  {
    return "MM";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getTableCreateStringUserServices() throws Exception
  {
    return "userservices ( UserCode char(20) not null, Service int, "
                       + "unique (UserCode, Service) )";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStringsUserServices(String[] s) throws Exception
  {
    s[0]="\000"; // remove compiler warning
    return 0;
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNamesUserServices() throws Exception
  {
    return "UserCode, Service";
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypesUserServices() throws Exception
  {
    return "CC";
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizesUserServices(short[] sizes) throws Exception
  {
    sizes[0] = 20;  sizes[1] = 0;
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public String getFieldStylesUserServices() throws Exception
  {
    return "MO";
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public String getTableCreateStringUserGroups() throws Exception
  {
    return "usergroups ( GroupName char(40) not null, User char(20), unique (GroupName, User) )";
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStringsUserGroups(String[] s) throws Exception
  {
    s[0]="\000"; // remove compiler warning
    return 0;
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNamesUserGroups() throws Exception
  {
    return "Group, User";
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypesUserGroups() throws Exception
  {
    return "CC";
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizesUserGroups(short[] sizes) throws Exception
  {
    sizes[0] = 40;   sizes[1] = 20;
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public String getFieldStylesUserGroups() throws Exception
  {
    return "MM";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getTableCreateStringSystem() throws Exception
  {
    return "system ( SystemStatus char(1) )";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStringsSystem(String[] s) throws Exception
  {
    s[0]="\000"; // remove compiler warning
    return 0;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNamesSystem() throws Exception
  {
    return "SystemStatus";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypesSystem() throws Exception
  {
    return "C";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizesSystem(short[] sizes) throws Exception
  {
    sizes[0] = 1;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldStylesSystem() throws Exception
  {
    return "M";
  }


  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getTableCreateStringSystemSuspend() throws Exception
  {
    return "systemsuspend ( Service char(20) )";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStringsSystemSuspend(String[] s) throws Exception
  {
    s[0]="\000"; // remove compiler warning
    return 0;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNamesSystemSuspend() throws Exception
  {
    return "Service";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypesSystemSuspend() throws Exception
  {
    return "C";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizesSystemSuspend(short[] sizes) throws Exception
  {
    sizes[0] = 20;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldStylesSystemSuspend() throws Exception
  {
    return "M";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getTableCreateStringSystemModules() throws Exception
  {
    return "systemmodules ( Module char(30) )";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStringsSystemModules(String[] s) throws Exception
  {
    s[0]="\000"; // remove compiler warning
    return 0;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNamesSystemModules() throws Exception
  {
    return "Module";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypesSystemModules() throws Exception
  {
    return "C";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizesSystemModules(short[] sizes) throws Exception
  {
    sizes[0] = 30;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldStylesSystemModules() throws Exception
  {
    return "M";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getTableCreateStringModuleServices() throws Exception
  {
    return "moduleservices ( Module char(30) not null, Service integer, "
                          + "unique (Module, Service) )";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStringsModuleServices(String[] s) throws Exception
  {
    s[0]="\000"; // remove compiler warning
    return 0;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNamesModuleServices() throws Exception
  {
    return "Module, Service";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypesModuleServices() throws Exception
  {
    return "CC";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizesModuleServices(short[] sizes) throws Exception
  {
    sizes[0] = 30;  sizes[1] = 0;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldStylesModuleServices() throws Exception
  {
    return "MO";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // HeaderLogoRepeat now used for PlainLogo
  // Watermark not used
  public String getTableCreateStringStyling() throws Exception
  {
    return "styling ( HeaderLogo char (100),      HeaderLogoRepeat char(100), UsesFlash char(1), "
                   + "FooterText char(200),       PageHeaderImage1 char(100), PageHeaderImage2 char(100), "
                   + "PageHeaderImage3 char(100), PageHeaderImage4 char(100), PageHeaderImage5 char(100), "
                   + "Watermark char(100) )";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStringsStyling(String[] s) throws Exception
  {
    return 0;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNamesStyling() throws Exception
  {
    return "HeaderLogo, HeaderLogoRepeat, UsesFlash, FooterText, PageHeaderImage1, PageHeaderImage2, PageHeaderImage3, PageHeaderImage4, PageHeaderImage5, Watermark";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypesStyling() throws Exception
  {
    return "CCCCCCCCCC";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizesStyling(short[] sizes) throws Exception
  {
    sizes[0] = 100;  sizes[1] = 100;  sizes[2] = 1;  sizes[3] = 200;   sizes[4] = 100;   sizes[5] = 100;   sizes[6] = 100;  sizes[7] = 100;  sizes[8] = 100;  sizes[9] = 100;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getAppConfigApplicationStartDate(Connection con, Statement stmt, ResultSet rs, String dnm) throws Exception
  {
    String[] companyName             = new String[1];
    String[] applicationStartDate    = new String[1];
    String[] financialYearStartMonth = new String[1];
    String[] financialYearEndMonth   = new String[1];
    String[] effectiveStartDate      = new String[1];
    String[] companyPhone            = new String[1];
    String[] companyFax              = new String[1];
    String[] dateStyle               = new String[1];
    String[] dateSeparator           = new String[1];
    String[] description             = new String[1];
    String[] companyEMail            = new String[1];
    String[] terseName               = new String[1];
    String[] latitude                = new String[1];
    String[] longitude               = new String[1];
    String[] googleMapsKey           = new String[1];
    String[] currentStyle            = new String[1];

    getAppConfig(con, stmt, rs, dnm, companyName, applicationStartDate, financialYearStartMonth, financialYearEndMonth, effectiveStartDate, companyPhone, companyFax, dateStyle, dateSeparator, description, companyEMail, terseName, latitude,
                 longitude, googleMapsKey, currentStyle);

    return applicationStartDate[0];
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getAppConfigApplicationCompanyName(Connection con, Statement stmt, ResultSet rs, String dnm) throws Exception
  {
    String[] companyName             = new String[1];
    String[] applicationStartDate    = new String[1];
    String[] financialYearStartMonth = new String[1];
    String[] financialYearEndMonth   = new String[1];
    String[] effectiveStartDate      = new String[1];
    String[] companyPhone            = new String[1];
    String[] companyFax              = new String[1];
    String[] dateStyle               = new String[1];
    String[] dateSeparator           = new String[1];
    String[] description             = new String[1];
    String[] companyEMail            = new String[1];
    String[] terseName               = new String[1];
    String[] latitude                = new String[1];
    String[] longitude               = new String[1];
    String[] googleMapsKey           = new String[1];
    String[] currentStyle            = new String[1];

    getAppConfig(con, stmt, rs, dnm, companyName, applicationStartDate, financialYearStartMonth, financialYearEndMonth, effectiveStartDate, companyPhone, companyFax, dateStyle, dateSeparator, description, companyEMail, terseName, latitude,
                 longitude, googleMapsKey, currentStyle);

    return companyName[0];
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void getAppConfigNamePhoneAndFax(Connection con, Statement stmt, ResultSet rs, String dnm, String[] companyName, String[] companyPhone, String[] companyFax) throws Exception
  {
    String[] applicationStartDate    = new String[1];
    String[] financialYearStartMonth = new String[1];
    String[] financialYearEndMonth   = new String[1];
    String[] effectiveStartDate      = new String[1];
    String[] dateStyle               = new String[1];
    String[] dateSeparator           = new String[1];
    String[] description             = new String[1];
    String[] companyEMail            = new String[1];
    String[] terseName               = new String[1];
    String[] latitude                = new String[1];
    String[] longitude               = new String[1];
    String[] googleMapsKey           = new String[1];
    String[] currentStyle            = new String[1];

    getAppConfig(con, stmt, rs, dnm, companyName, applicationStartDate, financialYearStartMonth, financialYearEndMonth, effectiveStartDate, companyPhone, companyFax, dateStyle, dateSeparator, description, companyEMail, terseName, latitude,
                 longitude, googleMapsKey, currentStyle);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void getLocationInfo(Connection con, Statement stmt, ResultSet rs, String dnm, String[] latitude, String[] longitude, String[] googleMapsKey) throws Exception
  {
    String[] companyName             = new String[1];
    String[] applicationStartDate    = new String[1];
    String[] companyPhone            = new String[1];
    String[] companyFax              = new String[1];
    String[] financialYearStartMonth = new String[1];
    String[] financialYearEndMonth   = new String[1];
    String[] effectiveStartDate      = new String[1];
    String[] dateStyle               = new String[1];
    String[] dateSeparator           = new String[1];
    String[] description             = new String[1];
    String[] companyEMail            = new String[1];
    String[] terseName               = new String[1];
    String[] currentStyle            = new String[1];

    getAppConfig(con, stmt, rs, dnm, companyName, applicationStartDate, financialYearStartMonth, financialYearEndMonth, effectiveStartDate, companyPhone, companyFax, dateStyle, dateSeparator, description, companyEMail, terseName, latitude,
                 longitude, googleMapsKey, currentStyle);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getAppConfigEffectiveStartDate(Connection con, Statement stmt, ResultSet rs, String dnm) throws Exception
  {
    String[] companyName             = new String[1];
    String[] applicationStartDate    = new String[1];
    String[] financialYearStartMonth = new String[1];
    String[] financialYearEndMonth   = new String[1];
    String[] effectiveStartDate      = new String[1];
    String[] companyPhone            = new String[1];
    String[] companyFax              = new String[1];
    String[] dateStyle               = new String[1];
    String[] dateSeparator           = new String[1];
    String[] description             = new String[1];
    String[] companyEMail            = new String[1];
    String[] terseName               = new String[1];
    String[] latitude                = new String[1];
    String[] longitude               = new String[1];
    String[] googleMapsKey           = new String[1];
    String[] currentStyle            = new String[1];

    getAppConfig(con, stmt, rs, dnm, companyName, applicationStartDate, financialYearStartMonth, financialYearEndMonth, effectiveStartDate, companyPhone, companyFax, dateStyle, dateSeparator, description, companyEMail, terseName, latitude,
                 longitude, googleMapsKey, currentStyle);

    return effectiveStartDate[0];
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void getAppConfigFinancialYearMonths(Connection con, Statement stmt, ResultSet rs, String dnm, String[] financialYearStartMonth, String[] financialYearEndMonth) throws Exception
  {
    String[] companyName             = new String[1];
    String[] applicationStartDate    = new String[1];
    String[] effectiveStartDate      = new String[1];
    String[] companyPhone            = new String[1];
    String[] companyFax              = new String[1];
    String[] dateStyle               = new String[1];
    String[] dateSeparator           = new String[1];
    String[] description             = new String[1];
    String[] companyEMail            = new String[1];
    String[] terseName               = new String[1];
    String[] latitude                = new String[1];
    String[] longitude               = new String[1];
    String[] googleMapsKey           = new String[1];
    String[] currentStyle            = new String[1];

    getAppConfig(con, stmt, rs, dnm, companyName, applicationStartDate, financialYearStartMonth, financialYearEndMonth, effectiveStartDate, companyPhone, companyFax, dateStyle, dateSeparator, description, companyEMail, terseName, latitude,
                 longitude, googleMapsKey, currentStyle);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void getAppConfig(Connection con, Statement stmt, ResultSet rs, String dnm, String[] companyName, String[] applicationStartDate, String[] financialYearStartMonth, String[] financialYearEndMonth, String[] effectiveStartDate,
                           String[] companyPhone, String[] companyFax, String[] dateStyle, String[] dateSeparator, String[] description, String[] companyEMail, String[] terseName, String[] latitude, String[] longitude, String[] googleMapsKey,
                           String[] currentStyle)
  {
    try
    {
      boolean done = false;
      while(! done)
      {
        stmt = con.createStatement();
        rs = stmt.executeQuery("SELECT * FROM appconfig WHERE DNM = '" + dnm + "'");
        if(rs.next())
        {
          companyName[0]             = rs.getString(1);
          currentStyle[0]             = rs.getString(3);
          applicationStartDate[0]    = generalUtils.convertFromYYYYMMDD(rs.getString(4));
          financialYearStartMonth[0] = rs.getString(5);
          financialYearEndMonth[0]   = rs.getString(6);
          effectiveStartDate[0]      = generalUtils.convertFromYYYYMMDD(rs.getString(7));
          companyPhone[0]            = rs.getString(8);
          companyFax[0]              = rs.getString(9);
          companyEMail[0]            = rs.getString(10);
          description[0]             = rs.getString(11);
          terseName[0]               = rs.getString(12);
          dateStyle[0]               = rs.getString(13);
          dateSeparator[0]           = rs.getString(14);
          latitude[0]                = rs.getString(15);
          longitude[0]               = rs.getString(16);
          googleMapsKey[0]           = rs.getString(17);

          if(dateStyle[0]     == null) dateStyle[0]     = "D";
          if(dateSeparator[0] == null) dateSeparator[0] = ".";
          if(description[0]   == null) description[0] = "";
          if(terseName[0 ]    == null) terseName[0] = "";
          if(latitude[0 ]     == null) latitude[0] = "1.30452"; // Singapore
          if(longitude[0 ]    == null) longitude[0] = "103.7377";
          if(googleMapsKey[0] == null) googleMapsKey[0] = "";

          done = true;
        }
        else
        {
          if(rs   != null) rs.close();
          if(stmt != null) stmt.close();

          AdminUtilitiesConfig AdminUtilitiesConfig = new AdminUtilitiesConfig();
          if(! AdminUtilitiesConfig.primeAppConfig(con, stmt, dnm))
          {
            companyName[0]             = "";
            applicationStartDate[0]    = "1.1.2010";
            financialYearStartMonth[0] = "January";
            financialYearEndMonth[0]   = "December";
            effectiveStartDate[0]      = "1.1.2010";
            companyPhone[0]            = "";
            companyFax[0]              = "";
            companyEMail[0]            = "";
            dateStyle[0]               = "D";
            dateSeparator[0]           = ".";
            description[0]             = "";
            terseName[0]               = "";
            latitude[0]                = "1.30452"; // Singapore
            longitude[0]               = "103.7377";
            googleMapsKey[0]           = "";

            done = true;
          }
        }
      }

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      companyName[0]             = "";
      applicationStartDate[0]    = "1.1.2010";
      financialYearStartMonth[0] = "January";
      financialYearEndMonth[0]   = "December";
      effectiveStartDate[0]      = "1.1.2010";
      companyPhone[0]            = "";
      companyFax[0]              = "";
      companyEMail[0]            = "";
      dateStyle[0]               = "D";
      dateSeparator[0]           = ".";
      description[0]             = "";
      terseName[0]               = "";
      latitude[0]                = "1.30452"; // Singapore
      longitude[0]               = "103.7377";
      googleMapsKey[0]           = "";

      System.out.println("DefinitionTables: AppConfig: " + e);

      try
      {
        if(rs   != null) rs.close();
        if(stmt != null) stmt.close();
      }
      catch(Exception e2) { }
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void getDocumentOptions(Connection con, Statement stmt, ResultSet rs, String[][] options)
  {
    try
    {
      stmt = con.createStatement();
      rs = stmt.executeQuery("SELECT * FROM documentoptions");

      String s;

      if(rs.next())
      {
        for(int x=0;x<100;++x)
        {
          s = rs.getString(x + 1);
          if(s == null || s.length() == 0)
            s = "N";
          options[0][x] = s;
        }
      }
      else
      {
        for(int x=0;x<100;++x)
          options[0][x] = "";
      }

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      for(int x=0;x<100;++x)
        options[0][x] = "N";

      System.out.println("DefinitionTables: DocOptions: " + e);
      try
      {
        if(rs   != null) rs.close();
        if(stmt != null) stmt.close();
      }
      catch(Exception e2) { }
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  public void getDocumentSettings(Connection con, Statement stmt, ResultSet rs, String[][] options)
  {
    try
    {
      stmt = con.createStatement();
      rs = stmt.executeQuery("SELECT * FROM documentsettings");

      String s;

      if(rs.next())
      {
        for(int x=0;x<50;++x)
        {
          s = rs.getString(x + 1);
          if(s == null || s.length() == 0)
            s = "";
          options[0][x] = s;
        }
      }
      else
      {
        for(int x=0;x<50;++x)
          options[0][x] = "";
      }

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      for(int x=0;x<50;++x)
        options[0][x] = "";

      System.out.println("DefinitionTables: DocSettings: " + e);
      try
      {
        if(rs   != null) rs.close();
        if(stmt != null) stmt.close();
      }
      catch(Exception e2) { }
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getDescriptionGivenService(Connection con, Statement stmt, ResultSet rs, String service) throws Exception
  {
    String desc = "";

    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT Description FROM scbs WHERE Service = '" + service + "'");

      if(rs.next())
        desc = rs.getString(1);

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println("DefinitionTables: Desc: " + e);
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }

    return desc;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void getSystem(Connection con, Statement stmt, ResultSet rs, String dnm, String[] systemStatus)
  {
    try
    {
      stmt = con.createStatement();
      
      rs = stmt.executeQuery("SELECT * FROM system");
      
      if(rs.next())
        systemStatus[0] = rs.getString(1);
      else systemStatus[0] = "M"; // maintenance

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      systemStatus[0] = "M";

      System.out.println("DefinitionTables: System: " + e);

      try
      {
        if(rs   != null) rs.close();
        if(stmt != null) stmt.close();
      }
      catch(Exception e3) { }
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean isSuspended(Connection con, Statement stmt, ResultSet rs, int service)
  {
    return isSuspended(con, stmt, rs, "" + service);
  }
  public boolean isSuspended(Connection con, Statement stmt, ResultSet rs, String service)
  {
    int rowCount = 0;

    try
    {
      stmt = con.createStatement();
      rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM systemsuspend WHERE Service = '" + service + "'");

      if(rs.next())
        rowCount = rs.getInt("rowcount");

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println("DefinitionTables: SystemSuspend: " + e);
      try
      {
        if(rs   != null) rs.close();
        if(stmt != null) stmt.close();
      }
      catch(Exception e2) { }
    }

    if(rowCount == 1)
      return true;
    return false;
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getUserNameFromProfilesGivenUserCodeAndStatus(Connection con, Statement stmt, ResultSet rs, String userCode, boolean wantLive) throws Exception
  {
    String userName = "";

    try
    {
      stmt = con.createStatement();

      if(wantLive)
        rs = stmt.executeQuery("SELECT UserName FROM profiles WHERE UserCode = '" + userCode + "' AND Status = 'L'");
      else rs = stmt.executeQuery("SELECT UserName FROM profiles WHERE UserCode = '" + userCode + "' AND Status != 'L'");

      if(rs.next())
        userName = generalUtils.deNull(rs.getString(1));
    }
    catch(Exception e)
    {
      System.out.println("Profile: " + e);
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    return userName;
  }

}
