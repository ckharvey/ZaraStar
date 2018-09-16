// =======================================================================================================================================================================================================
// System: ZaraStar Document: Contacts Record Access
// Module: Profile.java
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

public class Profile
{
  GeneralUtils generalUtils = new GeneralUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  DefinitionTables definitionTables = new DefinitionTables();

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getTableCreateStringRegistrants() throws Exception
  {
    return "registrants ( RegistrantCode char(20),  Name char(60) not null, CompanyName char(100), JobTitle char(80), EMail char(60), Phone1 char(30), MailingList char(1), Date timestamp, Received char(1), Confirmed char(1), Reminded char(1), "
                       + "unique(RegistrantCode))";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStringsRegistrants(String[] s) throws Exception
  {
    return 0;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNamesRegistrants() throws Exception
  {
    return "RegistrantCode, Name, CompanyName, JobTitle, EMail, Phone1, MailingList, Date, Received, Confirmed, Reminded";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypesRegistrants() throws Exception
  {
    return "CCCCCCCDCCC";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getTableCreateStringContacts() throws Exception
  {
    return "contacts ( ContactCode char(20),  Owner char(20) not null, Name char(60) not null, CompanyName char(100),     Domain char(60),         UserCode char(20),           JobTitle char(80),       Notes mediumtext, "
                    + "CustomerCode char(20), SupplierCode char(20),   EMail char(60),         OrganizationCode char(20), ExternalCode char(20),   ExternalPassWord char(20),   ExternalRights char(20), ExternalApproved char(1), "
                    + "Phone1 char(30),       Phone2 char(30),         Phone3 char(30),        Fax char(30),              ExternalCodeTo char(20), ExternalPassWordTo char(20), MailingList char(1),     ZPN char(40), " // ZPN not used
                    + "unique(ContactCode))";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStringsContacts(String[] s) throws Exception
  {
    return 0;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNamesContacts() throws Exception
  {
    return "ContactCode, Owner, Name, CompanyName, Domain, UserCode, JobTitle, Notes, CustomerCode, SupplierCode, EMail, OrganizationCode, ExternalCode, ExternalPassWord, ExternalRights, ExternalApproved, Phone1, Phone2, Phone3, Fax, "
         + "ExternalCodeTo, ExternalPassWordTo, MailingList, ZPN";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypesContacts() throws Exception
  {
    return "CCCCCCCMCCCCCCCCCCCCCCC";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizesContacts(short[] sizes) throws Exception
  {
    sizes[0] = 20;   sizes[1] = 20;   sizes[2] = 60;    sizes[3] = 100;   sizes[4] = 60;   sizes[5] = 20;   sizes[6] = 80;   sizes[7] = 0;
    sizes[8] = 20;   sizes[9] = 20;   sizes[10] = 60;   sizes[11] = 20;   sizes[12] = 20;  sizes[13] = 20;  sizes[14] = 20;  sizes[15] = 1;
    sizes[16] = 30;  sizes[17] = 30;  sizes[18] = 30;   sizes[19] = 30;   sizes[20] = 20;  sizes[21] = 20;  sizes[22] = 1;  sizes[23] = 40;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldStylesContacts() throws Exception
  {
    return "MOOOOOOOOOOOOOOOOOOOO0O";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getTableCreateStringContactgroups() throws Exception
  {
    return "contactgroups ( ContactCode char(20), GroupName char(20), unique(ContactCode, GroupName))";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStringsContactgroups(String[] s) throws Exception
  {
    return 0;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNamesContactgroups() throws Exception
  {
    return "ContactCode, GroupName";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypesContactgroups() throws Exception
  {
    return "CC";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizesContactgroups(short[] sizes) throws Exception
  {
    sizes[0] = 20;   sizes[1] = 20;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldStylesContactgroups() throws Exception
  {
    return "MO";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getTableCreateStringContactsharing() throws Exception
  {
    return "contactsharing ( Owner char(20) not null, UserCode char(20) not null, Mode char(1), unique(Owner, UserCode))";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStringsContactsharing(String[] s) throws Exception
  {
    return 0;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNamesContactsharing() throws Exception
  {
    return "Owner, UserCode, Mode";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypesContactsharing() throws Exception
  {
    return "CCC";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizesContactsharing(short[] sizes) throws Exception
  {
    sizes[0] = 20;   sizes[1] = 20;    sizes[2] = 1;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldStylesContactsharing() throws Exception
  {
    return "MMO";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getTableCreateStringOrganizations() throws Exception
  {
    return "organizations ( Code char(20) not null, Name char(80), unique(Code, Name))";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStringsOrganizations(String[] s) throws Exception
  {
    return 0;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNamesOrganizations() throws Exception
  {
    return "Code, Name";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypesOrganizations() throws Exception
  {
    return "CC";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizesOrganizations(short[] sizes) throws Exception
  {
    sizes[0] = 20;   sizes[1] = 80;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldStylesOrganizations() throws Exception
  {
    return "MO";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getTableCreateStringProfiles() throws Exception
  {
    return "profiles ( UserCode char(20) not null, UserName char(60),    PassWord char(40),    JobTitle char(80),       Status char(1), "
                    + "PhoneExtension char(10),    DateJoined date,      DateLeft date,        ShowInDirectory char(1), CustomerCode char(20), "
                    + "OfficePhone char(40),       MobilePhone char(40), Fax char(40),         IsEmployee char(1),      EMail char(60), "
                    + "Bio text,                   DNM char(40),         IsDBAdmin char(1),    ExternalAccess char(1),  SupplierCode char(20), "
                    + "FacebookCode char(100),     LinkedInCode char(100), GroupView char(60), IsSalesPerson char(1), IsSeniorSalesPerson char(1), isEnquiriesSalesPerson char(1), Owner char(20), CompanyName char(100), MailingList char(1), "
                    + "unique(UserCode,DNM))";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStringsProfiles(String[] s) throws Exception
  {
    return 0;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNamesProfiles() throws Exception
  {
    return "UserCode, UserName, PassWord, JobTitle, Status, PhoneExtension, DateJoined, DateLeft, ShowInDirectory, CustomerCode, OfficePhone, MobilePhone, Fax, IsEmployee, EMail, Bio, DNM, IsDBAdmin, ExternalAccess, SupplierCode, FacebookCode, "
         + "LinkedInCode, GroupView, IsSalesPerson, IsSeniorSalesPerson, isEnquiriesSalesPerson, Owner, CompanyName, MailingList";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypesProfiles() throws Exception
  {
    return "CCCCCCDDCCCCCCCMCCCCCCCCCCCCC";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizesProfiles(short[] sizes) throws Exception
  {
    sizes[0]  = 20;  sizes[1] = 60;  sizes[2] = 40;  sizes[3] = 80;  sizes[4] = 1;   sizes[5] = 10;  sizes[6]   = 0;  sizes[6] = 0;     sizes[7] = 0;  sizes[8] = 1;  sizes[9] = 20;  sizes[10] = 40;  sizes[11] = 40;  sizes[12] = 40;
    sizes[13] = 1;   sizes[14] = 60; sizes[15] = 0;  sizes[16] = 40; sizes[17] = 1;  sizes[18] = 1;  sizes[19] = 20;  sizes[20] = 100;  sizes[21] = 100;   sizes[22] = 60;  sizes[23] = 1;  sizes[24] = 1;  sizes[25] = 1;  sizes[26] = 20;
    sizes[27] = 100;  sizes[28] = 1;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldStylesProfiles() throws Exception
  {
    return "MOOOOOOOOOOOOOOOOOOOOOOOOOOOO";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getTableCreateStringPersonposition() throws Exception
  {
    return "personposition ( UserCode char(20) not null, Position integer, unique(UserCode))";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStringsPersonposition(String[] s) throws Exception
  {
    return 0;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNamesPersonposition() throws Exception
  {
    return "UserCode, Position";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypesPersonposition() throws Exception
  {
    return "CI";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizesPersonposition(short[] sizes) throws Exception
  {
    sizes[0] = 20;    sizes[1] = 0;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldStylesPersonposition() throws Exception
  {
    return "MM";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getTableCreateStringDemoUsers() throws Exception
  {
    return "demousers ( UserCode char(40) not null, Created date, "
                     + "unique(UserCode) )";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStringsDemoUsers(String[] s) throws Exception
  {
    s[0] = "\000"; // remove compiler warning
    return 0;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNamesDemoUsers() throws Exception
  {
    return "UserCode, Created";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypesDemoUsers() throws Exception
  {
    return "CD";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizesDemoUsers(short[] sizes) throws Exception
  {
    sizes[0] = 40;  sizes[1] = 0;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldStylesDemoUsers() throws Exception
  {
    return "MO";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getTableCreateStringExternalAccess() throws Exception
  {
    return "externalaccess ( Owner char(20) not null, Domain char(60), ExternalUserCode char(20), ExternalUserPassWord char(20), "
                          + "unique(Owner, Domain))";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStringsExternalAccess(String[] s) throws Exception
  {
    return 0;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNamesExternalAccess() throws Exception
  {
    return "Owner, Domain, ExternalUserCode, ExternalUserPassWord";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypesExternalAccess() throws Exception
  {
    return "CCCC";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizesExternalAccess(short[] sizes) throws Exception
  {
    sizes[0] = 20;   sizes[1] = 60;    sizes[2] = 20;   sizes[3] = 20;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldStylesExternalAccess() throws Exception
  {
    return "MMOO";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public char updateContacts(Connection con, Statement stmt, ResultSet rs, char newOrEdit, String contactCode, String name, String companyName, String jobTitle, String notes, String customerCode, String supplierCode, String organizationCode,
                             String externalCode, String externalPassWord, String externalRights, String externalApproved, String phone1, String phone2, String phone3, String fax, String eMailAddr, String xuserCode, String xdomain,
                             String userCode, String mailingList) throws Exception
  {
    char res = 'X';

    if(name.length() == 0)
        name = "-";
    if(companyName.length() == 0)
      companyName = "-";

    try
    {
      if(newOrEdit == 'N')
      {
        stmt = con.createStatement();

        stmt.setMaxRows(1);
        rs = stmt.executeQuery("SELECT ContactCode FROM contacts ORDER BY ContactCode DESC");

        int i;
        if(rs.next())
        {
          i = generalUtils.strToInt(rs.getString(1));
          ++i;
          if(i < 10)
            contactCode = "0000" + i;
          else
          if(i < 100)
            contactCode = "000" + i;
          else
          if(i < 1000)
            contactCode = "00" + i;
          else
          if(i < 10000)
            contactCode = "0" + i;
          else contactCode = "" + i;
        }
        else contactCode = "00001";

        if(rs   != null) rs.close();
        if(stmt != null) stmt.close();

        stmt = con.createStatement();

        String q = "INSERT INTO contacts (ContactCode, Owner, Name, CompanyName, Domain, UserCode, JobTitle, Notes, CustomerCode, SupplierCode, EMail, OrganizationCode, ExternalCode, ExternalPassWord, ExternalRights, ExternalApproved, Phone1, "
                 + "Phone2, Phone3, Fax, MailingList ) VALUES ('" + contactCode + "','" + userCode + "','" + generalUtils.sanitiseForSQL(name) + "','" + generalUtils.sanitiseForSQL(companyName) + "','', '', '" + generalUtils.sanitiseForSQL(jobTitle) + "','"
                 + generalUtils.sanitiseForSQL(notes) + "','" + customerCode + "','" + supplierCode + "','" + generalUtils.sanitiseForSQL(eMailAddr) + "','" + organizationCode + "','" + externalCode + "','" + generalUtils.sanitiseForSQL(externalPassWord) + "','"
                 + externalRights + "','" + externalApproved + "','" + generalUtils.sanitiseForSQL(phone1) + "','" + generalUtils.sanitiseForSQL(phone2) + "','" + generalUtils.sanitiseForSQL(phone3) + "','" + generalUtils.sanitiseForSQL(fax) + "','" + mailingList + "')";

        stmt.executeUpdate(q);
        res = ' ';

        if(stmt != null) stmt.close();
      }
      else // newOrEdit = 'E'
      {
        stmt = con.createStatement();

        String q = "UPDATE contacts SET Name = '" + generalUtils.sanitiseForSQL(name) + "', CompanyName = '" + generalUtils.sanitiseForSQL(companyName) + "', Domain = '" + xdomain + "', UserCode = '" + xuserCode + "', jobTitle = '"
                 + generalUtils.sanitiseForSQL(jobTitle) + "', Notes = '" + generalUtils.sanitiseForSQL(notes) + "', CustomerCode = '" + customerCode + "', SupplierCode = '" + supplierCode + "', EMail = '" + generalUtils.sanitiseForSQL(eMailAddr)
                 + "', OrganizationCode = '" + organizationCode + "', ExternalCode = '" + externalCode + "', ExternalPassWord = '" + generalUtils.sanitiseForSQL(externalPassWord) + "', ExternalRights = '" + externalRights + "', ExternalApproved = '"
                 + externalApproved + "', Phone1 = '" + generalUtils.sanitiseForSQL(phone1) + "', Phone2 = '" + generalUtils.sanitiseForSQL(phone2) + "', Phone3 = '" + generalUtils.sanitiseForSQL(phone3) + "', Fax = '" + generalUtils.sanitiseForSQL(fax)
                 + "', MailingList = '" + mailingList + "' WHERE ContactCode = '" + contactCode + "'";

        stmt.executeUpdate(q);

        if(stmt != null) stmt.close();

        res = ' ';
      }

      return res;
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(stmt != null) stmt.close();
    }

    return res;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public char updateContactsGivenExternalCode(Connection con, Statement stmt, ResultSet rs, char newOrEdit, String externalCode, String contactCode, String name, String companyName, String jobTitle, String notes, String externalPassWord,
                                              String externalApproved, String phone1, String phone2, String phone3, String fax, String eMailAddr, String owner, String mailingList, String[] newCode) throws Exception
  {
    char res = 'X';

    try
    {
      if(newOrEdit == 'N')
      {
        stmt = con.createStatement();

        stmt.setMaxRows(1);
        rs = stmt.executeQuery("SELECT ContactCode FROM contacts ORDER BY ContactCode DESC");

        int i;
        if(rs.next())
        {
          i = generalUtils.strToInt(rs.getString(1));
          ++i;
          if(i < 10)
            contactCode = "0000" + i;
          else
          if(i < 100)
            contactCode = "000" + i;
          else
          if(i < 1000)
            contactCode = "00" + i;
          else
          if(i < 10000)
            contactCode = "0" + i;
          else contactCode = "" + i;
        }
        else contactCode = "00001";

        if(rs   != null) rs.close();
        if(stmt != null) stmt.close();

        stmt = con.createStatement();

        stmt.setMaxRows(1);
        rs = stmt.executeQuery("SELECT ExternalCode FROM contacts ORDER BY ExternalCode DESC");

        if(rs.next())
        {
          externalCode = "" + (generalUtils.strToInt(rs.getString(1)) + 1);
          if(externalCode.equals("0"))
            externalCode = "10001";
        }
        else externalCode = "10001"; // just-in-case

        if(rs   != null) rs.close();
        if(stmt != null) stmt.close();

        stmt = con.createStatement();

        String q = "INSERT INTO contacts (ContactCode, Owner, Name, CompanyName, Domain, UserCode, JobTitle, Notes, CustomerCode, SupplierCode, EMail, OrganizationCode, ExternalCode, ExternalPassWord, ExternalRights, ExternalApproved, Phone1, "
                 + "Phone2, Phone3, Fax, MailingList ) VALUES ('" + contactCode + "','" + owner + "','" + generalUtils.sanitiseForSQL(name) + "','" + generalUtils.sanitiseForSQL(companyName) + "','', '', '" + generalUtils.sanitiseForSQL(jobTitle) + "','"
                 + generalUtils.sanitiseForSQL(notes) + "','','','" + generalUtils.sanitiseForSQL(eMailAddr) + "','','" + externalCode + "','" + generalUtils.sanitiseForSQL(externalPassWord) + "','','" + externalApproved + "','" + generalUtils.sanitiseForSQL(phone1) + "','"
                 + generalUtils.sanitiseForSQL(phone2) + "','" + generalUtils.sanitiseForSQL(phone3) + "','" + generalUtils.sanitiseForSQL(fax) + "','" + mailingList + "')";

        stmt.executeUpdate(q);

        if(stmt != null) stmt.close();

        res = ' ';
      }
      else // newOrEdit = 'E'
      {
        stmt = con.createStatement();

        String q = "UPDATE contacts SET Name = '" + generalUtils.sanitiseForSQL(name) + "', CompanyName = '" + generalUtils.sanitiseForSQL(companyName) + "', jobTitle = '" + generalUtils.sanitiseForSQL(jobTitle) + "', Notes = '" + generalUtils.sanitiseForSQL(notes)
                 + "', EMail = '" + generalUtils.sanitiseForSQL(eMailAddr) + "', ExternalPassWord = '" + generalUtils.sanitiseForSQL(externalPassWord) + "', Phone1 = '" + generalUtils.sanitiseForSQL(phone1) + "', Phone2 = '" + generalUtils.sanitiseForSQL(phone2)
                 + "', Phone3 = '" + generalUtils.sanitiseForSQL(phone3) + "', Fax = '" + generalUtils.sanitiseForSQL(fax) + "', Owner = '" + generalUtils.sanitiseForSQL(owner) + "', MailingList= '" + mailingList + "' WHERE ExternalCode = '" + externalCode + "'";

        stmt.executeUpdate(q);

        if(stmt != null) stmt.close();

        res = ' ';
      }

      newCode[0] = externalCode;

      return res;
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(stmt != null) stmt.close();
    }

    return res;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void updateContactGroups(Connection con, Statement stmt, byte[] groups, String contactCode) throws Exception
  {
    try
    {
      // remove all existing recs
      stmt = con.createStatement();

      stmt.executeUpdate("DELETE FROM contactgroups WHERE ContactCode = '" + contactCode + "'");

      if(stmt != null) stmt.close();

      String group;

      int x=0, len = generalUtils.lengthBytes(groups, 0);
      while(x < len)
      {
        group = "";
        while(groups[x] != '\001' && groups[x] != '\000')
          group += (char)groups[x++];

        stmt = con.createStatement();

        String q = "INSERT INTO contactgroups (ContactCode, GroupName ) "
                 + "VALUES ('" + contactCode + "','" + group + "')";

        stmt.executeUpdate(q);

        if(stmt != null) stmt.close();

        ++x;
      }
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(stmt != null) stmt.close();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void updateContactCompanyCode(Connection con, Statement stmt, String code, String type, String contactCode) throws Exception
  {
    try
    {
      stmt = con.createStatement();

      if(type.equals("C"))
        type = "CustomerCode";
      else type = "SupplierCode";

      String q = "UPDATE contacts SET " + type + " = '" + code + "' WHERE ContactCode = '" + contactCode + "'";
      stmt.executeUpdate(q);

      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(stmt != null) stmt.close();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void updateContactExternal(String contactCode, String externalCode, String externalPassWord, String externalRights, String externalApproved, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    Connection con = null;
    Statement stmt = null;

    try
    {
      String uName    = directoryUtils.getMySQLUserName();
      String passWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();

      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + uName + "&password=" + passWord);

      stmt = con.createStatement();

      String q = "UPDATE contacts SET ExternalCode = '" + externalCode + "', ExternalPassWord = '" + externalPassWord + "', ExternalRights = '" + externalRights + "', ExternalApproved = '" + externalApproved + "' WHERE ContactCode = '"
               + contactCode + "'";

      stmt.executeUpdate(q);

      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void getContact(String contactCode, String[] owner, String[] name, String[] companyName, String[] eMailAddr, String[] domain, String[] userCode, String[] jobTitle, String[] notes, String[] customerCode, String[] supplierCode,
                         String[] organizationCode, String[] externalCode, String[] externalPassWord, String[] externalRights, String[] externalApproved, String[] phone1, String[] phone2, String[] phone3, String[] fax, String[] mailingList,
                         String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    String uName = directoryUtils.getMySQLUserName();
    String pWord = directoryUtils.getMySQLPassWord();

    Class.forName("com.mysql.jdbc.Driver").newInstance();

    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = con.createStatement();
    ResultSet rs = null;

    try
    {
      rs = stmt.executeQuery("SELECT * FROM contacts WHERE ContactCode = '" + contactCode + "'");

      if(rs.next())
      {
        owner[0]            = rs.getString(2);
        name[0]             = rs.getString(3);
        companyName[0]      = rs.getString(4);
        domain[0]           = rs.getString(5);
        userCode[0]         = rs.getString(6);
        jobTitle[0]         = rs.getString(7);
        notes[0]            = rs.getString(8);
        customerCode[0]     = rs.getString(9);
        supplierCode[0]     = rs.getString(10);
        eMailAddr[0]        = rs.getString(11);
        organizationCode[0] = rs.getString(12);
        externalCode[0]     = rs.getString(13);
        externalPassWord[0] = rs.getString(14);
        externalRights[0]   = rs.getString(15);
        externalApproved[0] = rs.getString(16);
        phone1[0]           = rs.getString(17);
        phone2[0]           = rs.getString(18);
        phone3[0]           = rs.getString(19);
        fax[0]              = rs.getString(20);
        mailingList[0]      = rs.getString(23);

        if(name[0]        == null) name[0] = "";
        if(companyName[0] == null) companyName[0] = "";
        if(phone1[0]      == null) phone1[0] = "";
        if(phone2[0]      == null) phone2[0] = "";
        if(phone3[0]      == null) phone3[0] = "";
        if(fax[0]         == null) fax[0]    = "";
        if(mailingList[0] == null) mailingList[0] = "N";
      }
      else
      {
        owner[0]            = "";
        name[0]             = "";
        companyName[0]      = "";
        domain[0]           = "";
        userCode[0]         = "";
        jobTitle[0]         = "";
        notes[0]            = "";
        customerCode[0]     = "";
        supplierCode[0]     = "";
        eMailAddr[0]        = "";
        organizationCode[0] = "";
        externalCode[0]     = "";
        externalPassWord[0] = "";
        externalRights[0]   = "";
        externalApproved[0] = "";
        phone1[0]           = "";
        phone2[0]           = "";
        phone3[0]           = "";
        fax[0]              = "";
        mailingList[0]      = "N";
      }
    }
    catch(Exception e)
    {
      owner[0]            = "";
      name[0]             = "";
      companyName[0]      = "";
      domain[0]           = "";
      userCode[0]         = "";
      jobTitle[0]         = "";
      notes[0]            = "";
      customerCode[0]     = "";
      supplierCode[0]     = "";
      eMailAddr[0]        = "";
      organizationCode[0] = "";
      externalCode[0]     = "";
      externalPassWord[0] = "";
      externalRights[0]   = "";
      externalApproved[0] = "";
      phone1[0]           = "";
      phone2[0]           = "";
      phone3[0]           = "";
      fax[0]              = "";
      mailingList[0]      = "N";
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    if(con  != null) con.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void getContactGivenExternalCode(String externalCode, String[] contactCode, String[] owner, String[] name, String[] companyName, String[] eMailAddr, String[] domain, String[] userCode, String[] jobTitle, String[] notes,
                                          String[] customerCode, String[] supplierCode, String[] organizationCode, String[] externalPassWord, String[] externalRights, String[] externalApproved, String[] phone1, String[] phone2, String[] phone3,
                                          String[] fax, String[] mailingList, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    String uName = directoryUtils.getMySQLUserName();
    String pWord = directoryUtils.getMySQLPassWord();

    Class.forName("com.mysql.jdbc.Driver").newInstance();

    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = con.createStatement();
    ResultSet rs = null;

    try
    {
      rs = stmt.executeQuery("SELECT * FROM contacts WHERE ExternalCode = '" + externalCode + "'");

      if(rs.next())
      {
        contactCode[0]      = rs.getString(1);
        owner[0]            = rs.getString(2);
        name[0]             = rs.getString(3);
        companyName[0]      = rs.getString(4);
        domain[0]           = rs.getString(5);
        userCode[0]         = rs.getString(6);
        jobTitle[0]         = rs.getString(7);
        notes[0]            = rs.getString(8);
        customerCode[0]     = rs.getString(9);
        supplierCode[0]     = rs.getString(10);
        eMailAddr[0]        = rs.getString(11);
        organizationCode[0] = rs.getString(12);
        // externalCode[0]     = rs.getString(13);
        externalPassWord[0] = rs.getString(14);
        externalRights[0]   = rs.getString(15);
        externalApproved[0] = rs.getString(16);
        phone1[0]           = rs.getString(17);
        phone2[0]           = rs.getString(18);
        phone3[0]           = rs.getString(19);
        fax[0]              = rs.getString(20);
        mailingList[0]      = rs.getString(23);

        if(name[0]        == null) name[0] = "";
        if(companyName[0] == null) companyName[0] = "";
        if(phone1[0]      == null) phone1[0] = "";
        if(phone2[0]      == null) phone2[0] = "";
        if(phone3[0]      == null) phone3[0] = "";
        if(fax[0]         == null) fax[0]    = "";
        if(mailingList[0] == null) mailingList[0] = "N";
      }
      else
      {
        contactCode[0]      = "";
        owner[0]            = "";
        name[0]             = "";
        companyName[0]      = "";
        domain[0]           = "";
        userCode[0]         = "";
        jobTitle[0]         = "";
        notes[0]            = "";
        customerCode[0]     = "";
        supplierCode[0]     = "";
        eMailAddr[0]        = "";
        organizationCode[0] = "";
        externalPassWord[0] = "";
        externalRights[0]   = "";
        externalApproved[0] = "";
        phone1[0]           = "";
        phone2[0]           = "";
        phone3[0]           = "";
        fax[0]              = "";
        mailingList[0]      = "N";
      }
    }
    catch(Exception e)
    {
      contactCode[0]      = "";
      owner[0]            = "";
      name[0]             = "";
      companyName[0]      = "";
      domain[0]           = "";
      userCode[0]         = "";
      jobTitle[0]         = "";
      notes[0]            = "";
      customerCode[0]     = "";
      supplierCode[0]     = "";
      eMailAddr[0]        = "";
      organizationCode[0] = "";
      externalPassWord[0] = "";
      externalRights[0]   = "";
      externalApproved[0] = "";
      phone1[0]           = "";
      phone2[0]           = "";
      phone3[0]           = "";
      fax[0]              = "";
        mailingList[0]      = "N";
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    if(con  != null) con.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void deleteContact(String contactCode, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    Connection con = null;
    Statement stmt = null;

    try
    {
      String uName    = directoryUtils.getMySQLUserName();
      String passWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();

      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + uName + "&password=" + passWord);

      stmt = con.createStatement();

      stmt.executeUpdate("DELETE FROM contacts WHERE ContactCode = '" + contactCode + "'");

      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }
  }
/*
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void getContactDetails(String contactCode, String dnm, String localDefnsDir, String defnsDir, String[] notes) throws Exception
  {
    String userName = directoryUtils.getMySQLUserName();
    String passWord = directoryUtils.getMySQLPassWord();

    Class.forName("com.mysql.jdbc.Driver").newInstance();

    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + userName + "&password=" + passWord);
    Statement stmt = con.createStatement();

    ResultSet rs = stmt.executeQuery("SELECT UserCode, Domain, Notes FROM contacts WHERE ContactCode = '" + contactCode + "'");

    String userCode, domain;
    if(rs.next())
    {
      userCode = rs.getString(1);
      domain   = rs.getString(2);
      notes[0] = rs.getString(3);

      if(userCode != null && userCode.length() > 0 && domain != null && domain.length() > 0)
      {
//  private String getProfileFromRemoteServer(String unm, String pwd, String sid, String uty, String men, String den, String dnm, String bnm,
  //                                      String serverToCall, String userCode) throws Exception
      }
    }
    else notes[0] = "";

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    if(con  != null) con.close();
  }
*/

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean getExternalAccessCodeAndPassWord(Connection con, Statement stmt, ResultSet rs, String u, String[] userCode, String[] passWord) throws Exception
  {
    stmt = con.createStatement();

    boolean res = false;

    try
    {
      rs = stmt.executeQuery("SELECT ExternalCodeTo, ExternalPassWordTo FROM contacts WHERE ContactCode = '" + u + "'");

      if(rs.next())
      {
        userCode[0] = rs.getString(1);
        passWord[0] = rs.getString(2);

        res = true;
      }
    }
    catch(Exception e) { }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    return res;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getEMailGivenContactCode(Connection con, Statement stmt, ResultSet rs, String contactCode) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT EMail FROM contacts WHERE ContactCode = '" + contactCode + "'");

    String eMail = "";
    if(rs.next())
    {
      eMail = rs.getString(1);

      if(eMail == null)
        eMail = "";
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    return eMail;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getUserBasis(Connection con, String userCode, String DNM) throws Exception
  {
    String userBasis = "N";
    
    Statement stmt = con.createStatement();
    ResultSet rs = null;

    try
    {
      rs = stmt.executeQuery("SELECT UserBasis FROM profilesd WHERE userCode = '" + userCode + "'");

      if(rs.next())
        userBasis = generalUtils.deNull(rs.getString(1));
    }
    catch(Exception e)
    {
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return userBasis;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void getProfile(String userCode, String DNM, String[] userName, String[] passWord, String[] status, String[] facebookCode, String[] facebookAccessToken, String[] dateOfBirth, String[] nationality) throws Exception
  {
    Class.forName("com.mysql.jdbc.Driver").newInstance();

    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(DNM + "_ofsa", conp);
    Statement stmt = con.createStatement();
    ResultSet rs = null;

    try
    {
      rs = stmt.executeQuery("SELECT * FROM profiles WHERE userCode = '" + userCode + "'");

      if(rs.next())
      {
        userName[0]            = generalUtils.deNull(rs.getString(2));
        passWord[0]            = generalUtils.deNull(rs.getString(3));
        status[0]              = generalUtils.deNull(rs.getString(4));
        facebookCode[0]        = generalUtils.deNull(rs.getString(5));
        facebookAccessToken[0] = generalUtils.deNull(rs.getString(6));
        dateOfBirth[0]         = generalUtils.deNull(rs.getString(7));
        nationality[0]         = generalUtils.deNull(rs.getString(8));

        if(dateOfBirth[0].length() == 0) dateOfBirth[0]         = "1970-01-01";
      }
      else
      {
        userName[0]            = "";
        passWord[0]            = "";
        status[0]              = "L";
        facebookCode[0]        = "";
        facebookAccessToken[0] = "";
        dateOfBirth[0]         = "1970-01-01";
        nationality[0  ]       = "";
      }
    }
    catch(Exception e)
    {
        userName[0]            = "";
        passWord[0]            = "";
        status[0]              = "L";
        facebookCode[0]        = "";
        facebookAccessToken[0] = "";
        dateOfBirth[0]         = "1970-01-01";
        nationality[0  ]       = "";
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    if(con  != null) con.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void getProfiled(String userCode, String DNM, String[] jobTitle, String[] dateJoined, String[] dateLeft, String[] showInDirectory, String[] officePhone, String[] mobilePhone, String[] fax, String[] userBasis, String[] eMail,
                          String[] bio, String[] isDBAdmin, String[] externalAccess, String[] isSalesPerson, String[] isSeniorSalesPerson, String[] isEnquiriesSalesPerson, String[] customerCode, String[] supplierCode) throws Exception
  {
    Class.forName("com.mysql.jdbc.Driver").newInstance();

    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(DNM + "_ofsa", conp);
    Statement stmt = con.createStatement();
    ResultSet rs = null;

    try
    {
      rs = stmt.executeQuery("SELECT * FROM profilesd WHERE userCode = '" + userCode + "'");

      if(rs.next())
      {
        jobTitle[0]               = generalUtils.deNull(rs.getString(3));
        dateJoined[0]             = generalUtils.deNull(rs.getString(4));
        dateLeft[0]               = generalUtils.deNull(rs.getString(5));
        showInDirectory[0]        = generalUtils.deNull(rs.getString(6));
        officePhone[0]            = generalUtils.deNull(rs.getString(7));
        mobilePhone[0]            = generalUtils.deNull(rs.getString(8));
        fax[0]                    = generalUtils.deNull(rs.getString(9));
        userBasis[0]              = generalUtils.deNull(rs.getString(10));
        eMail[0]                  = generalUtils.deNull(rs.getString(11));
        bio[0]                    = generalUtils.deNull(rs.getString(12));
        isDBAdmin[0]              = generalUtils.deNull(rs.getString(13));
        externalAccess[0]         = generalUtils.deNull(rs.getString(14));
        isSalesPerson[0]          = generalUtils.deNull(rs.getString(15));
        isSeniorSalesPerson[0]    = generalUtils.deNull(rs.getString(16));
        isEnquiriesSalesPerson[0] = generalUtils.deNull(rs.getString(17));
//        accessMethod[0]           = generalUtils.deNull(rs.getString(18));
        customerCode[0]           = generalUtils.deNull(rs.getString(19));
        supplierCode[0]           = generalUtils.deNull(rs.getString(20));

        if(isDBAdmin[0].length() == 0) isDBAdmin[0] = "N";
        if(externalAccess[0].length() == 0) externalAccess[0] = "N";
      }
      else
      {
        jobTitle[0]        = "";
        dateJoined[0]      = "1970-01-01";
        dateLeft[0]        = "1970-01-01";
        showInDirectory[0] = "";
        officePhone[0]     = "";
        mobilePhone[0]     = "";
        fax[0]             = "";
        userBasis[0]      = "N";
        eMail[0]           = "";
        bio[0]             = "";
        isDBAdmin[0]       = "N";
        externalAccess[0]         = "N";
        isSalesPerson[0]          = "N";
        isSeniorSalesPerson[0]    = "N";
        isEnquiriesSalesPerson[0] = "N";
        customerCode[0]           = "";
        supplierCode[0]           = "N";
      }
    }
    catch(Exception e)
    {
        jobTitle[0]        = "";
        dateJoined[0]      = "1970-01-01";
        dateLeft[0]        = "1970-01-01";
        showInDirectory[0] = "";
        officePhone[0]     = "";
        mobilePhone[0]     = "";
        fax[0]             = "";
        userBasis[0]      = "N";
        eMail[0]           = "";
        bio[0]             = "";
        isDBAdmin[0]       = "";
        externalAccess[0]         = "N";
        isSalesPerson[0]          = "N";
        isSeniorSalesPerson[0]    = "N";
        isEnquiriesSalesPerson[0] = "N";
        customerCode[0]           = "";
        supplierCode[0]           = "N";
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    if(con  != null) con.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean updateProfile(String userCode, String userName, String passWord, String status, String facebookCode, String facebookAccessToken, String dateOfBirth, String nationality, String dnm) throws Exception
  {
    boolean res = false;
    
    Connection con = null;
    Statement stmt = null;

    try
    {
      String uName = directoryUtils.getMySQLUserName();
      String pWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();

      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + uName + "&password=" + pWord);

      // check if rec already exists
      stmt = con.createStatement();

      ResultSet rs = stmt.executeQuery("SELECT UserCode FROM profiles WHERE UserCode = '" + userCode + "'");
      String code = null;
      if(rs.next())
        code = rs.getString(1);
      if(rs != null) rs.close();

      if(stmt != null) stmt.close();

      if(status.length() > 0) // just-in-case
        status = status.substring(0, 1);
      else status = "S"; // suspended

      if(dateOfBirth.length() == 0) dateOfBirth = "1970-01-01";

      userCode = generalUtils.capitalize(userCode);

      if(code != null) // already exists
      {
        stmt = con.createStatement();

        String q = "UPDATE profiles SET UserName = '" + generalUtils.sanitiseForSQL(userName) + "', PassWord = '" + passWord + "', Status = '" + status + "', FacebookCode = '" + facebookCode + "', FacebookAccessToken = '" + facebookAccessToken
                 + "', DateOfBirth = '" + dateOfBirth + "', Nationality = '" + nationality + "' WHERE UserCode = '" + userCode + "'";

        stmt.executeUpdate(q);

        if(stmt != null) stmt.close();
      }
      else // create new rec
      {
        stmt = con.createStatement();

        String q = "INSERT INTO profiles (UserCode, UserName, PassWord, Status, FacebookCode, FacebookAccessToken, DateOfBirth, Nationality) VALUES ('" + userCode + "','" + generalUtils.sanitiseForSQL(userName) + "','"
                 + passWord + "','" + status + "','" + facebookCode + "','" + facebookAccessToken + "','" + dateOfBirth + "','" + nationality + "')";

        stmt.executeUpdate(q);
      }

      if(stmt != null) stmt.close();

      if(status.equals("L"))
        status = "A"; // active
      else status = "N";

      if(con  != null) con.close();
      
      res = true;
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }

    return res;
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean updateProfiled(String userCode, String jobTitle, String dateJoined, String dateLeft, String showInDirectory, String officePhone, String mobilePhone, String fax, String userBasis, String eMail, String bio, String isDBAdmin,
                               String externalAccess, String isSalesPerson, String isSeniorSalesPerson, String isEnquiriesSalesPerson, String customerCode, String supplierCode, String dnm) throws Exception
  {
    boolean res = false;

    Connection con = null;
    Statement stmt = null;

    try
    {
      String uName = directoryUtils.getMySQLUserName();
      String pWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();

      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + uName + "&password=" + pWord);

      // check if rec already exists
      stmt = con.createStatement();

      ResultSet rs = stmt.executeQuery("SELECT UserCode FROM profilesd WHERE UserCode = '" + userCode + "'");
      String code = null;
      if(rs.next())
        code = rs.getString(1);
      if(rs != null) rs.close();

      if(stmt != null) stmt.close();

      if(dateJoined.length() == 0) dateJoined = "1970-01-01";
      if(dateLeft.length() == 0)   dateLeft   = "1970-01-01";

      if(showInDirectory.equals("on"))
        showInDirectory = "Y";
      else showInDirectory = "N"; // no

      if(isDBAdmin.equals("on"))
        isDBAdmin = "Y";
      else isDBAdmin = "N"; // no

      if(externalAccess.equals("on"))
        externalAccess = "Y";
      else externalAccess = "N"; // no

      if(isSalesPerson.equals("on"))
        isSalesPerson = "Y";
      else isSalesPerson = "N"; // no

      if(isSeniorSalesPerson.equals("on"))
        isSeniorSalesPerson = "Y";
      else isSeniorSalesPerson = "N"; // no

      if(isEnquiriesSalesPerson.equals("on"))
        isEnquiriesSalesPerson = "Y";
      else isEnquiriesSalesPerson = "N"; // no

      userCode = generalUtils.capitalize(userCode);

      if(code != null) // already exists
      {
        stmt = con.createStatement();

        String q = "UPDATE profilesd SET JobTitle = '" + generalUtils.sanitiseForSQL(jobTitle) + "', DateJoined = {d '" + dateJoined + "' }, DateLeft = {d '" + dateLeft + "' }, ShowInDirectory = '" + showInDirectory + "', OfficePhone = '" + officePhone
                 + "', MobilePhone = '" + mobilePhone + "', Fax = '" + fax + "', UserBasis = '" + userBasis + "', EMail = '" + generalUtils.sanitiseForSQL(eMail) + "', Bio = '" + generalUtils.sanitiseForSQL(bio) + "', IsDBAdmin = '" + isDBAdmin
                 + "', ExternalAccess = '" + externalAccess + "', IsSalesPerson = '" + isSalesPerson + "', IsSeniorSalesPerson = '" + isSeniorSalesPerson + "', IsEnquiriesSalesPerson = '" + isEnquiriesSalesPerson + "', CustomerCode = '"
                 + customerCode + "', SupplierCode = '" + supplierCode + "', DNM = '" + dnm + "' WHERE UserCode = '" + userCode + "'";

        stmt.executeUpdate(q);

        if(stmt != null) stmt.close();
      }
      else // create new rec
      {
        stmt = con.createStatement();

        String q = "INSERT INTO profilesd (UserCode, JobTitle, DateJoined, DateLeft, ShowInDirectory, OfficePhone, MobilePhone, Fax, UserBasis, EMail, Bio, IsDBAdmin, ExternalAccess, IsSalesPerson, IsSeniorSalesPerson, IsEnquiriesSalesPerson,"
                 + "CustomerCode, SupplierCode, DNM) VALUES ('" + userCode + "','" + generalUtils.sanitiseForSQL(jobTitle) + "','" + dateJoined + "','" + dateLeft + "','" + showInDirectory + "','" + officePhone + "','" + mobilePhone + "','" + fax + "','"
                 + userBasis + "','" + generalUtils.sanitiseForSQL(eMail) + "','" + generalUtils.sanitiseForSQL(bio) + "','" + isDBAdmin + "','" + externalAccess + "','" + isSalesPerson + "','" + isSeniorSalesPerson + "','" + isEnquiriesSalesPerson + "','"
                 + customerCode + "','" + supplierCode + "','" + dnm + "')";

        stmt.executeUpdate(q);
      }

      if(stmt != null) stmt.close();

      if(con  != null) con.close();
      
      res = true;
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }

    return res;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // the IP address of the local machine
  private String serverToCall(String serverName, String defnsDir) throws Exception
  {
    String s = generalUtils.getFromDefnFile(serverName, "local.dfn", "", defnsDir);
    int len = s.length();
    if(len == 0) // just-in-case
      return "127.0.0.1";

    String localServer = "";
    int x = 0;
    while(x < len && s.charAt(x) != ' ') // just-in-case
      localServer += s.charAt(x++);

    return localServer;
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void getExternalAccess(String owner, String domain, String[] userCode, String[] passWord, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    Class.forName("com.mysql.jdbc.Driver").newInstance();

    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = con.createStatement();
    ResultSet rs = null;

    try
    {
      rs = stmt.executeQuery("SELECT ExternalUserCode, ExternalUserPassWord FROM externalaccess WHERE Owner = '" + owner + "' AND Domain = '" + domain + "'");

      if(rs.next())
      {
        userCode[0] = rs.getString(1);
        passWord[0] = rs.getString(2);
      }
      else
      {
        userCode[0] = "";
        passWord[0] = "";
      }
    }
    catch(Exception e)
    {
      userCode[0] = "";
      passWord[0] = "";
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    if(con  != null) con.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean validateExternalAccess(String u, String p, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    Class.forName("com.mysql.jdbc.Driver").newInstance();

    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = con.createStatement();
    ResultSet rs = null;

    boolean res = false;

    try
    {
      rs = stmt.executeQuery("SELECT ExternalPassWord FROM contacts WHERE ExternalCode = '" + u + "'");

      if(rs.next())
      {
        if(p.equals(rs.getString(1)))
          res = true;
      }
    }
    catch(Exception e) { }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    if(con  != null) con.close();

    return res;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean validateExternalAccessCompany(String u, char companyType, String companyCode, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    String uName = directoryUtils.getMySQLUserName();
    String pWord = directoryUtils.getMySQLPassWord();

    Class.forName("com.mysql.jdbc.Driver").newInstance();

    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = con.createStatement();
    ResultSet rs = null;

    boolean res = false;

    try
    {
      rs = stmt.executeQuery("SELECT CustomerCode, SupplierCode, OrganizationCode FROM contacts WHERE ExternalCode = '" + u + "'");

      if(rs.next())
      {
        switch(companyType)
        {
          case 'C' : if(companyCode.equals(rs.getString(1)))
                       res = true;
                     break;
          case 'S' : if(companyCode.equals(rs.getString(2)))
                       res = true;
                     break;
          case 'O' : if(companyCode.equals(rs.getString(3)))
                       res = true;
                     break;
        }
      }
    }
    catch(Exception e) { }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    if(con  != null) con.close();

    return res;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean getExternalAccessNameCompanyAndRights(Connection con, Statement stmt, ResultSet rs, String u, String[] name, String[] companyName, String[] accessRights) throws Exception
  {
    stmt = con.createStatement();

    boolean res = false;

    try
    {
      rs = stmt.executeQuery("SELECT Name, CompanyName, ExternalRights FROM contacts WHERE ExternalCode = '" + u + "'");

      if(rs.next())
      {
        name[0]         = rs.getString(1);
        companyName[0]  = rs.getString(2);
        accessRights[0] = rs.getString(3);

        res = true;
      }
    }
    catch(Exception e) { }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    return res;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean getExternalAccessDetails(String u, String dnm, String localDefnsDir, String defnsDir, String[] name, String[] companyName, String[] jobTitle, String[] customerCode, String[] eMail) throws Exception
  {
    String uName = directoryUtils.getMySQLUserName();
    String pWord = directoryUtils.getMySQLPassWord();

    Class.forName("com.mysql.jdbc.Driver").newInstance();

    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = con.createStatement();
    ResultSet rs = null;

    boolean res = false;

    try
    {
      rs = stmt.executeQuery("SELECT Name, CompanyName, JobTitle, CustomerCode, EMail FROM contacts WHERE ExternalCode = '" + u + "'");

      if(rs.next())
      {
        name[0]         = rs.getString(1);
        companyName[0]  = rs.getString(2);
        jobTitle[0]     = rs.getString(3);
        customerCode[0] = rs.getString(4);
        eMail[0]        = rs.getString(5);

        res = true;
      }
    }
    catch(Exception e) { System.out.println(e); }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    if(con  != null) con.close();

    return res;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean getExternalAccessNameCustomerCode(Connection con, Statement stmt, ResultSet rs, String u, String dnm, String localDefnsDir, String defnsDir, String[] customerCode) throws Exception
  {
    stmt = con.createStatement();

    boolean res = false;

    try
    {
      rs = stmt.executeQuery("SELECT CustomerCode FROM contacts WHERE ExternalCode = '" + u + "'");

      if(rs.next())
      {
        customerCode[0] = rs.getString(1);

        res = true;
      }
    }
    catch(Exception e) { System.out.println(e); }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    return res;
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean verifyAccess(Connection con, Statement stmt, ResultSet rs, String userCode, String uty, String dnm, int service) throws Exception
  {
    if(userCode.equals("Sysadmin"))
      return true;

    boolean live = false;

    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT Status FROM profiles WHERE UserCode = '" + userCode + "'");

      if(rs.next())
      {
        if(rs.getString(1).equals("L"))
          live = true;
      }

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }

    if(! live)
      return false;

    if(userCode.equals("___casual___") || userCode.equals("___registered___"))
    {
      int rowCount = 0;

      try
      {
        stmt = con.createStatement();

        rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM userservices WHERE UserCode = '" + userCode + "' AND Service = '" + service + "'");

        if(rs.next())
          rowCount = rs.getInt("rowcount");

        if(rs   != null) rs.close();
        if(stmt != null) stmt.close();
      }
      catch(Exception e)
      {
        if(rs   != null) rs.close();
        if(stmt != null) stmt.close();
      }

      if(rowCount == 0)
        return false;
      return true;
    }
    else
    {
      String usersDir = directoryUtils.getUserDir(dnm) + "/" + userCode + "/";

      byte[] rights = new byte[2500];

      RandomAccessFile fhRights = generalUtils.fileOpenD("rights", usersDir);
      if(fhRights == null) // just-in-case
      {
        //generalUtils.fileClose(fhRights);

        fhRights = reCreateRights(con, stmt, rs, usersDir, userCode, rights);
        //return false;
      }

      fhRights.read(rights, 0, 2500);

      int i = (int) Math.IEEEremainder(service, 8);
      if (i < 0) i = 8 + i;
        Double d = new Double(Math.pow(2, i));

      if((rights[service / 8] & (byte) (d.intValue())) != (byte) (d.intValue()))
      {
        generalUtils.fileClose(fhRights);
        return false;
      }

      generalUtils.fileClose(fhRights);
      return true;
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private RandomAccessFile reCreateRights(Connection con, Statement stmt, ResultSet rs, String usersDir, String userCode, byte[] rights) throws Exception
  {
    RandomAccessFile fh = generalUtils.create(usersDir + "rights");

    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT Service FROM userservices WHERE UserCode = '" + userCode + "'");

      int i, service;
      Double d;

      while(rs.next())
      {
        service = generalUtils.intFromStr(rs.getString(1));

        i = (int)Math.IEEEremainder(service, 8);
        if(i < 0)
          i += 8;
        d = new Double(Math.pow(2, i));

        rights[service / 8] |= (byte)(d.intValue());
      }

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }

    fh.seek(0L);
    fh.write(rights, 0, 2500);

    return fh;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void deleteRightsFile(String userCode, String dnm) throws Exception
  {
    String usersDir = directoryUtils.getUserDir(dnm) + "/" + userCode + "/";
    generalUtils.fileDelete(usersDir + "rights");
  }
/*
  // -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean xxxxgetDetailsForService(Statement stmtZC, ResultSet rsZC, String service, String[] erp, String[] mrp, String[] mail, String[] library, String[] site, String[] channels, String[] admin, String[] casual,
                                      String[] registered) throws Exception
  {
    boolean res = false;

    stmtZC = conZC.createStatement();
    rsZC = stmtZC.executeQuery("SELECT * FROM scbs WHERE Service = '" + service + "'");

    if(rsZC.next())
    {
      erp[0]        = rsZC.getString(4);
      mrp[0]        = rsZC.getString(5);
      mail[0]       = rsZC.getString(6);
      library[0]    = rsZC.getString(7);
      site[0]       = rsZC.getString(8);
      channels[0]   = rsZC.getString(9);
      admin[0]      = rsZC.getString(10);
// hidden
      casual[0]     = rsZC.getString(12);
      registered[0] = rsZC.getString(13);

      res = true;
    }

    if(rsZC   != null) rsZC.close();
    if(stmtZC != null) stmtZC.close();

    return res;
  }
*/
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean isContactGroupUsed(Connection con, Statement stmt, ResultSet rs, String contactCode, String group) throws Exception
  {
    stmt = con.createStatement();

    int rowCount = 0;

    try
    {
      rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM contactgroups WHERE ContactCode = '" + contactCode + "' AND GroupName = '" + group
                           + "'");

      if(rs.next())
        rowCount = rs.getInt("rowcount");

      if(rs != null) rs.close();
    }
    catch(Exception e) { }

    if(stmt != null) stmt.close();

    if(rowCount == 0)
      return false;
    return true;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void areContactsShared(Connection con, Statement stmt, ResultSet rs, String unm, String userCode, String[] mode) throws Exception
  {
    stmt = con.createStatement();

    try
    {
      rs = stmt.executeQuery("SELECT Mode FROM contactsharing WHERE Owner = '" + unm + "' AND UserCode = '" + generalUtils.sanitiseForSQL(userCode) + "'");

      if(rs.next())
        mode[0] = rs.getString(1);
      else mode[0] = "N";
    }
    catch(Exception e) { mode[0] = "N"; }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void updateContactSharing(Connection con, Statement stmt, byte[] userCodes, String modes, String unm) throws Exception
  {
    try
    {
      // remove all existing recs
      stmt = con.createStatement();

      stmt.executeUpdate("DELETE FROM contactsharing WHERE Owner = '" + unm + "'");

      if(stmt != null) stmt.close();

      String userCode;
      char mode;

      int x=0, y = 0, len = generalUtils.lengthBytes(userCodes, 0);
      while(x < len)
      {
        userCode = "";
        while(userCodes[x] != '\001' && userCodes[x] != '\000')
          userCode += (char)userCodes[x++];

        mode = modes.charAt(y++);

        stmt = con.createStatement();

        String q = "INSERT INTO contactsharing (Owner, UserCode, Mode ) VALUES ('" + unm + "','" + userCode + "','" + mode + "')";

        stmt.executeUpdate(q);
//System.out.println(q);
        if(stmt != null) stmt.close();

        ++x;
      }
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(stmt != null) stmt.close();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  public byte[] getContactDDLData(Connection con, Statement stmt, ResultSet rs, String customerCode, String fieldName, byte[] ddlData, int[] ddlDataUpto, int[] ddlDataLen) throws Exception
  {
    try
    {
      String name, contactCode;
//System.out.println("customerCode: " + customerCode);
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT ContactCode, Name FROM contacts WHERE CustomerCode = '" + customerCode + "' ORDER BY Name");

      while(rs.next())
      {
        contactCode = rs.getString(1);
        name        = rs.getString(2);

        if((ddlDataUpto[0] + 100) >= ddlDataLen[0])
        {
          byte[] tmp = new byte[ddlDataLen[0]];
          System.arraycopy(ddlData, 0, tmp, 0, ddlDataLen[0]);
          ddlDataLen[0] += 1000;
          ddlData = new byte[ddlDataLen[0]];
          System.arraycopy(tmp, 0, ddlData, 0, ddlDataLen[0] - 1000);
        }

        ddlDataUpto[0] = generalUtils.stringIntoBytes(fieldName + "=\"", 0, ddlData, ddlDataUpto[0]);
        ddlDataUpto[0] = generalUtils.stringIntoBytes(name + " (" + contactCode + ")", 0, ddlData, ddlDataUpto[0]);
        ddlDataUpto[0] = generalUtils.stringIntoBytes("\" ", 0, ddlData, ddlDataUpto[0]);
      }

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();

      return ddlData;
    }
    catch(Exception e)
    {
      System.out.println("Profile: " + e);
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
  public boolean canSignonFromOutside(Connection con, Statement stmt, ResultSet rs, String userCode) throws Exception
  {
    boolean can = false;

    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT ExternalAccess FROM profiles WHERE UserCode = '" + userCode + "'");

      if(rs.next())
      {
        if(rs.getString(1).equals("Y"))
          can = true;
      }

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }

    return can;
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getNameFromProfile(Connection con, Statement stmt, ResultSet rs, String userCode) throws Exception
  {
    String userName = "";

    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT UserName FROM profiles WHERE UserCode = '" + userCode + "'");

      if(rs.next())
        userName = rs.getString(1);

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }

    return userName;
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

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getEMailFromProfiled(Connection con, Statement stmt, ResultSet rs, String userCode) throws Exception
  {
    String eMail = "";

    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT EMail FROM profilesd WHERE UserCode = '" + userCode + "'");

      if(rs.next())
        eMail = rs.getString(1);

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }

    return eMail;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getProfileGivenCustomerCode(Connection con, Statement stmt, ResultSet rs, String customerCode) throws Exception
  {
    String userCode = "";
    
    try
    {
      stmt = con.createStatement();
      
      rs = stmt.executeQuery("SELECT UserCode FROM profilesd WHERE CustomerCode = '" + customerCode + "'");

      if(rs.next()) // only picks-up the first rec with the stated eMail
        userCode = rs.getString(1);
        
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println("profile: getProfileGivenCustomerCode(): " + e);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return userCode;
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getNewNumericUserCode(Connection con, Statement stmt, ResultSet rs) throws Exception
  {
    String userCode = "";
    
    try
    {
      stmt = con.createStatement();
      
      stmt.setMaxRows(1);
      
      rs = stmt.executeQuery("SELECT UserCode FROM profiles where UserCode >= '1' and UserCode < 'a' ORDER BY UserCode DESC");

      if(rs.next())
        userCode = rs.getString(1);
      else userCode = "0";
      
      userCode = "" + (generalUtils.strToInt(userCode) + 1);
        
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println("profile: getNewNumericUserCode(): " + e);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return userCode;
  }
  
}
