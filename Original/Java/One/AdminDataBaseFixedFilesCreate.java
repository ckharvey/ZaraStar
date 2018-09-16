// =======================================================================================================================================================================================================
// System: ZaraStar: Admin: process fixed file
// Module: AdminDataBaseFixedFilesCreate.java
// Author: C.K.Harvey
// Copyright (c) 2003-09 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
//
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.io.*;
import java.sql.*;
 
public class AdminDataBaseFixedFilesCreate extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  AdminUtils adminUtils = new AdminUtils();
 
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="";

    try
    {
      directoryUtils.setContentHeaders(res);
      out = res.getWriter();

      byte[] tableNames    = new byte[1000]; tableNames[0]    = '\000';
      int[]  tableNamesLen = new int[1];     tableNamesLen[0] = 1000;

      int thisEntryLen, inc;
      char option=' ';

      Enumeration en = req.getParameterNames();
      while(en.hasMoreElements())
      {
        String name = (String)en.nextElement();
        String[] value = req.getParameterValues(name);
        if(name.equals("unm"))
          unm = value[0];
        else
        if(name.equals("sid"))
          sid = value[0];
        else
        if(name.equals("uty"))
          uty = value[0];
        else
        if(name.equals("men"))
          men = value[0];
        else
        if(name.equals("den"))
          den = value[0];
        else
        if(name.equals("dnm"))
          dnm = value[0];
        else
        if(name.equals("bnm"))
          bnm = value[0];
        else
        if(name.length() > 2 && name.substring(1, 3).equals(".x"))
        {
          option = name.charAt(0);
        }
        else
        if(name.length() > 2 && name.substring(1, 3).equals(".y"))
        {
          ;
        }
        else // must be checkbox value
        {
          thisEntryLen = name.length() + 2;
          if((generalUtils.lengthBytes(tableNames, 0) + thisEntryLen) >= tableNamesLen[0])
          {
            byte[] tmp = new byte[tableNamesLen[0]];
            System.arraycopy(tableNames, 0, tmp, 0, tableNamesLen[0]);
            if(thisEntryLen > 1000)
              inc = thisEntryLen;
            else inc = 1000;
            tableNamesLen[0] += inc;
            tableNames = new byte[tableNamesLen[0]];
            System.arraycopy(tmp, 0, tableNames, 0, tableNamesLen[0] - inc);
          }

          generalUtils.catAsBytes(name + "\001", 0, tableNames, false);
        }
      }

      doIt(out, req, option, tableNames, unm, sid, uty, men, den, bnm, dnm, bytesOut);
    }    
    catch(Exception e)
    {
      StringBuffer url = req.getRequestURL();
      String urlBit="";
      int x=0;
      if(url.charAt(x) == 'h' || url.charAt(x) == 'H')
        x += 7;
      while(url.charAt(x) != '\000' && url.charAt(x) != ',' && url.charAt(x) != '/')
        urlBit += url.charAt(x++);

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "Admin:DataBaseFixedFilesCreate", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7052, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, char option, byte[] tableNames, String unm, String sid, String uty, String men, String den, String bnm, String dnm, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String localDefnsDir  = directoryUtils.getLocalOverrideDir(dnm);
    String defnsDir       = directoryUtils.getSupportDirs('D');
    String exportDir      = directoryUtils.getExportDir(dnm);
    String imagesDir      = directoryUtils.getSupportDirs('I');

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! serverUtils.isSysAdmin(con, stmt, rs, unm, dnm, sid, uty, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(true, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "AdminDataBaseFixedFiles", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7052, bytesOut[0], 0, "ACC:");
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(true, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "AdminDataBaseFixedFiles", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7052, bytesOut[0], 0, "SID:");
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }
        
    int target;
    if(process(con, stmt, option, tableNames, dnm, exportDir, localDefnsDir, defnsDir))
      target = 27;
    else target = 23;

    messagePage.msgScreen(true, out, req, target, unm, sid, uty, men, den, dnm, bnm, "AdminDataBaseFixedFiles", "", "", "", imagesDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 7052, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con  != null) con.close();
    if(out != null) out.flush();
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean process(Connection con, Statement stmt, char option, byte[] tableNames, String dnm, String exportDir, String localDefnsDir, String defnsDir) throws Exception
  {
    int x=0;
    String tableName;

    int len = generalUtils.lengthBytes(tableNames, 0);
    while(x < len)
    {
      tableName="";
      while(tableNames[x] != '\001' && tableNames[x] != '\000')
        tableName += (char)tableNames[x++];

      if(! processATable(con, stmt, true, option, tableName, dnm, exportDir, localDefnsDir, defnsDir))
        return false;

      ++x;
    }

    return true;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean processATable(Connection con, Statement stmt, boolean dropTable, char option, String tableName, String dnm, String exportDir, String localDefnsDir, String defnsDir) throws Exception
  {
    if(exportDir     == null) exportDir     = directoryUtils.getExportDir(dnm);
    if(localDefnsDir == null) localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    if(defnsDir      == null) defnsDir      = directoryUtils.getSupportDirs('D');

    boolean rtn = false;

    if(tableName.length() == 0) // just-in-case
      return rtn;
      
    String[] indexCreateStrings = new String[10]; // plenty
    String tableCreateString=null, fieldNames=null, fieldTypes=null, dataBase = "ofsa";
    int numIndexes=0;

    if(tableName.equalsIgnoreCase("appconfig"))
    {
      DefinitionTables definitionTables = new DefinitionTables();
      numIndexes        = definitionTables.getIndexCreateStringsAppConfig(indexCreateStrings);
      tableCreateString = definitionTables.getTableCreateStringAppConfig();
      fieldNames        = definitionTables.getFieldNamesAppConfig();
      fieldTypes        = definitionTables.getFieldTypesAppConfig();
    }
  
    if(tableName.equalsIgnoreCase("bankaccount"))
    {
      AccountsTables accountsTables = new AccountsTables();
      numIndexes        = accountsTables.getIndexCreateStringsBankAccount(indexCreateStrings);
      tableCreateString = accountsTables.getTableCreateStringBankAccount();
      fieldNames        = accountsTables.getFieldNamesBankAccount();
      fieldTypes        = accountsTables.getFieldTypesBankAccount();
    }

    if(tableName.equalsIgnoreCase("buyerscatalog"))
    {
      InventoryAdjustment inventoryAdjustment = new InventoryAdjustment();
      numIndexes        = inventoryAdjustment.getIndexCreateStringsBuyersCatalog(indexCreateStrings);
      tableCreateString = inventoryAdjustment.getTableCreateStringsBuyersCatalog();
      fieldNames        = inventoryAdjustment.getFieldNamesBuyersCatalog();
      fieldTypes        = inventoryAdjustment.getFieldTypesBuyersCatalog();
    }

    if(tableName.equalsIgnoreCase("catalog"))
    {
      InventoryAdjustment inventoryAdjustment = new InventoryAdjustment();
      numIndexes        = inventoryAdjustment.getIndexCreateStringsCatalog(indexCreateStrings);
      tableCreateString = inventoryAdjustment.getTableCreateStringsCatalog();
      fieldNames        = inventoryAdjustment.getFieldNamesCatalog();
      fieldTypes        = inventoryAdjustment.getFieldTypesCatalog();
    }

    if(tableName.equalsIgnoreCase("catalogc"))
    {
      InventoryAdjustment inventoryAdjustment = new InventoryAdjustment();
      numIndexes        = inventoryAdjustment.getIndexCreateStringsCatalogC(indexCreateStrings);
      tableCreateString = inventoryAdjustment.getTableCreateStringsCatalogC();
      fieldNames        = inventoryAdjustment.getFieldNamesCatalogC();
      fieldTypes        = inventoryAdjustment.getFieldTypesCatalogC();
    }

    if(tableName.equalsIgnoreCase("catalogl"))
    {
      InventoryAdjustment inventoryAdjustment = new InventoryAdjustment();
      numIndexes        = inventoryAdjustment.getIndexCreateStringsCatalogL(indexCreateStrings);
      tableCreateString = inventoryAdjustment.getTableCreateStringsCatalogL();
      fieldNames        = inventoryAdjustment.getFieldNamesCatalogL();
      fieldTypes        = inventoryAdjustment.getFieldTypesCatalogL();
    }

    if(tableName.equalsIgnoreCase("cataloglist"))
    {
      InventoryAdjustment inventoryAdjustment = new InventoryAdjustment();
      numIndexes        = inventoryAdjustment.getIndexCreateStringsCatalogList(indexCreateStrings);
      tableCreateString = inventoryAdjustment.getTableCreateStringsCatalogList();
      fieldNames        = inventoryAdjustment.getFieldNamesCatalogList();
      fieldTypes        = inventoryAdjustment.getFieldTypesCatalogList();
    }

    if(tableName.equalsIgnoreCase("catalogs"))
    {
      InventoryAdjustment inventoryAdjustment = new InventoryAdjustment();
      numIndexes        = inventoryAdjustment.getIndexCreateStringsCatalogS(indexCreateStrings);
      tableCreateString = inventoryAdjustment.getTableCreateStringsCatalogS();
      fieldNames        = inventoryAdjustment.getFieldNamesCatalogS();
      fieldTypes        = inventoryAdjustment.getFieldTypesCatalogS();
    }

    if(tableName.equalsIgnoreCase("chat"))
    {
      Chat chat = new Chat();
      numIndexes        = chat.getIndexCreateStringsChat(indexCreateStrings);
      tableCreateString = chat.getTableCreateStringChat();
      fieldNames        = chat.getFieldNamesChat();
      fieldTypes        = chat.getFieldTypesChat();
    }

    if(tableName.equalsIgnoreCase("chatunread"))
    {
      Chat chat = new Chat();
      numIndexes        = chat.getIndexCreateStringsChatUnRead(indexCreateStrings);
      tableCreateString = chat.getTableCreateStringChatUnRead();
      fieldNames        = chat.getFieldNamesChatUnRead();
      fieldTypes        = chat.getFieldTypesChatUnRead();
    }

    if(tableName.equalsIgnoreCase("channelalerts"))
    {
      Chat chat = new Chat();
      numIndexes        = chat.getIndexCreateStringsChannelAlerts(indexCreateStrings);
      tableCreateString = chat.getTableCreateStringChannelAlerts();
      fieldNames        = chat.getFieldNamesChannelAlerts();
      fieldTypes        = chat.getFieldTypesChannelAlerts();
    }

    if(tableName.equalsIgnoreCase("channels"))
    {
      Chat chat = new Chat();
      numIndexes        = chat.getIndexCreateStringsChannels(indexCreateStrings);
      tableCreateString = chat.getTableCreateStringChannels();
      fieldNames        = chat.getFieldNamesChannels();
      fieldTypes        = chat.getFieldTypesChannels();
    }

    if(tableName.equalsIgnoreCase("channelsmine"))
    {
      Chat chat = new Chat();
      numIndexes        = chat.getIndexCreateStringsChannelsMine(indexCreateStrings);
      tableCreateString = chat.getTableCreateStringChannelsMine();
      fieldNames        = chat.getFieldNamesChannelsMine();
      fieldTypes        = chat.getFieldTypesChannelsMine();
    }

    if(tableName.equalsIgnoreCase("channelsopen"))
    {
      Chat chat = new Chat();
      numIndexes        = chat.getIndexCreateStringsChannelsOpen(indexCreateStrings);
      tableCreateString = chat.getTableCreateStringChannelsOpen();
      fieldNames        = chat.getFieldNamesChannelsOpen();
      fieldTypes        = chat.getFieldTypesChannelsOpen();
    }

    if(tableName.equalsIgnoreCase("channellinks"))
    {
      Chat chat = new Chat();
      numIndexes        = chat.getIndexCreateStringsChannelLinks(indexCreateStrings);
      tableCreateString = chat.getTableCreateStringChannelLinks();
      fieldNames        = chat.getFieldNamesChannelLinks();
      fieldTypes        = chat.getFieldTypesChannelLinks();
    }

    if(tableName.equalsIgnoreCase("channelusers"))
    {
      Chat chat = new Chat();
      numIndexes        = chat.getIndexCreateStringsChannelusers(indexCreateStrings);
      tableCreateString = chat.getTableCreateStringChannelusers();
      fieldNames        = chat.getFieldNamesChannelusers();
      fieldTypes        = chat.getFieldTypesChannelusers();
    }

    if(tableName.equalsIgnoreCase("codes"))
    {
      DocumentUtils documentUtils = new DocumentUtils();
      numIndexes        = documentUtils.getIndexCreateStringsCodes(indexCreateStrings);
      tableCreateString = documentUtils.getTableCreateStringCodes();
      fieldNames        = documentUtils.getFieldNamesCodes();
      fieldTypes        = documentUtils.getFieldTypesCodes();
    }

    if(tableName.equalsIgnoreCase("company"))
    {
      Customer customer = new Customer();
      numIndexes        = customer.getIndexCreateStrings(indexCreateStrings);
      tableCreateString = customer.getTableCreateString();
      fieldNames        = customer.getFieldNames();
      fieldTypes        = customer.getFieldTypes();
    }
    
    if(tableName.equalsIgnoreCase("companytype"))
    {
      DocumentUtils documentUtils = new DocumentUtils();
      numIndexes        = documentUtils.getIndexCreateStringsCompanyType(indexCreateStrings);
      tableCreateString = documentUtils.getTableCreateStringCompanyType();
      fieldNames        = documentUtils.getFieldNamesCompanyType();
      fieldTypes        = documentUtils.getFieldTypesCompanyType();
    }
       
    if(tableName.equalsIgnoreCase("contacts"))
    {
      Profile profile = new Profile();
      numIndexes        = profile.getIndexCreateStringsContacts(indexCreateStrings);
      tableCreateString = profile.getTableCreateStringContacts();
      fieldNames        = profile.getFieldNamesContacts();
      fieldTypes        = profile.getFieldTypesContacts();
    }
       
    if(tableName.equalsIgnoreCase("contactgroups"))
    {
      Profile profile = new Profile();
      numIndexes        = profile.getIndexCreateStringsContactgroups(indexCreateStrings);
      tableCreateString = profile.getTableCreateStringContactgroups();
      fieldNames        = profile.getFieldNamesContactgroups();
      fieldTypes        = profile.getFieldTypesContactgroups();
    }
    
    if(tableName.equalsIgnoreCase("contactsharing"))
    {
      Profile profile = new Profile();
      numIndexes        = profile.getIndexCreateStringsContactsharing(indexCreateStrings);
      tableCreateString = profile.getTableCreateStringContactsharing();
      fieldNames        = profile.getFieldNamesContactsharing();
      fieldTypes        = profile.getFieldTypesContactsharing();
    }
       
    if(tableName.equalsIgnoreCase("country"))
    {
      DocumentUtils documentUtils = new DocumentUtils();
      numIndexes        = documentUtils.getIndexCreateStringsCountry(indexCreateStrings);
      tableCreateString = documentUtils.getTableCreateStringCountry();
      fieldNames        = documentUtils.getFieldNamesCountry();
      fieldTypes        = documentUtils.getFieldTypesCountry();
    }
    
    if(tableName.equalsIgnoreCase("credit"))
    {
      SalesCreditNote salesSalesCreditNote = new SalesCreditNote();
      numIndexes        = salesSalesCreditNote.getIndexCreateStringsSalesCreditNote(indexCreateStrings);
      tableCreateString = salesSalesCreditNote.getTableCreateStringSalesCreditNote();
      fieldNames        = salesSalesCreditNote.getFieldNamesSalesCreditNote();
      fieldTypes        = salesSalesCreditNote.getFieldTypesSalesCreditNote();
    } 
   
    if(tableName.equalsIgnoreCase("creditl"))
    { 
      SalesCreditNote salesSalesCreditNote = new SalesCreditNote();
      numIndexes        = salesSalesCreditNote.getIndexCreateStringsSalesCreditNoteL(indexCreateStrings);
      tableCreateString = salesSalesCreditNote.getTableCreateStringSalesCreditNoteL();
      fieldNames        = salesSalesCreditNote.getFieldNamesSalesCreditNoteL();
      fieldTypes        = salesSalesCreditNote.getFieldTypesSalesCreditNoteL();
    } 
   
    if(tableName.equalsIgnoreCase("creditll"))
    {
      SalesCreditNote salesSalesCreditNote = new SalesCreditNote();
      numIndexes        = salesSalesCreditNote.getIndexCreateStringsSalesCreditNoteLL(indexCreateStrings);
      tableCreateString = salesSalesCreditNote.getTableCreateStringSalesCreditNoteLL();
      fieldNames        = salesSalesCreditNote.getFieldNamesSalesCreditNoteLL();
      fieldTypes        = salesSalesCreditNote.getFieldTypesSalesCreditNoteLL();
    } 
   
    if(tableName.equalsIgnoreCase("credita"))
    {
      SalesCreditNote salesSalesCreditNote = new SalesCreditNote();
      numIndexes        = salesSalesCreditNote.getIndexCreateStringsSalesCreditNoteA(indexCreateStrings);
      tableCreateString = salesSalesCreditNote.getTableCreateStringSalesCreditNoteA();
      fieldNames        = salesSalesCreditNote.getFieldNamesSalesCreditNoteA();
      fieldTypes        = salesSalesCreditNote.getFieldTypesSalesCreditNoteA();
    }

    if(tableName.equalsIgnoreCase("cssstyles"))
    {
      DocumentUtils documentUtils = new DocumentUtils();
      numIndexes        = documentUtils.getIndexCreateStringsCSSstyles(indexCreateStrings);
      tableCreateString = documentUtils.getTableCreateStringCSSstyles();
      fieldNames        = documentUtils.getFieldNamesCSSstyles();
      fieldTypes        = documentUtils.getFieldTypesCSSstyles();
    }

    if(tableName.equalsIgnoreCase("currency"))
    {
      AccountsTables accountsTables = new AccountsTables();
      numIndexes        = accountsTables.getIndexCreateStringsCurrency(indexCreateStrings);
      tableCreateString = accountsTables.getTableCreateStringCurrency();
      fieldNames        = accountsTables.getFieldNamesCurrency();
      fieldTypes        = accountsTables.getFieldTypesCurrency();
    }
       
    if(tableName.equalsIgnoreCase("currrate"))
    {
      AccountsTables accountsTables = new AccountsTables();
      numIndexes        = accountsTables.getIndexCreateStringsCurrRate(indexCreateStrings);
      tableCreateString = accountsTables.getTableCreateStringCurrRate();
      fieldNames        = accountsTables.getFieldNamesCurrRate();
      fieldTypes        = accountsTables.getFieldTypesCurrRate();
    }

    if(tableName.equalsIgnoreCase("cycle"))
    {
      InventoryAdjustment inventoryAdjustment = new InventoryAdjustment();
      numIndexes        = inventoryAdjustment.getIndexCreateStringsCycle(indexCreateStrings);
      tableCreateString = inventoryAdjustment.getTableCreateStringCycle();
      fieldNames        = inventoryAdjustment.getFieldNamesCycle();
      fieldTypes        = inventoryAdjustment.getFieldTypesCycle();
    }

    if(tableName.equalsIgnoreCase("cycled"))
    {
      InventoryAdjustment inventoryAdjustment = new InventoryAdjustment();
      numIndexes        = inventoryAdjustment.getIndexCreateStringsCycled(indexCreateStrings);
      tableCreateString = inventoryAdjustment.getTableCreateStringCycled();
      fieldNames        = inventoryAdjustment.getFieldNamesCycled();
      fieldTypes        = inventoryAdjustment.getFieldTypesCycled();
    }

    if(tableName.equalsIgnoreCase("cyclel"))
    {
      InventoryAdjustment inventoryAdjustment = new InventoryAdjustment();
      numIndexes        = inventoryAdjustment.getIndexCreateStringsCyclel(indexCreateStrings);
      tableCreateString = inventoryAdjustment.getTableCreateStringCyclel();
      fieldNames        = inventoryAdjustment.getFieldNamesCyclel();
      fieldTypes        = inventoryAdjustment.getFieldTypesCyclel();
    }

    if(tableName.equalsIgnoreCase("debit"))
    {
      SalesDebitNote salesDebitNote = new SalesDebitNote();
      numIndexes        = salesDebitNote.getIndexCreateStringsDebitNote(indexCreateStrings);
      tableCreateString = salesDebitNote.getTableCreateStringDebitNote();
      fieldNames        = salesDebitNote.getFieldNamesDebitNote();
      fieldTypes        = salesDebitNote.getFieldTypesDebitNote();
    }

    if(tableName.equalsIgnoreCase("debita"))
    {
      SalesDebitNote salesDebitNote = new SalesDebitNote();
      numIndexes        = salesDebitNote.getIndexCreateStringsDebitNoteA(indexCreateStrings);
      tableCreateString = salesDebitNote.getTableCreateStringDebitNoteA();
      fieldNames        = salesDebitNote.getFieldNamesDebitNoteA();
      fieldTypes        = salesDebitNote.getFieldTypesDebitNoteA();
    } 
   
    if(tableName.equalsIgnoreCase("debitl"))
    { 
      SalesDebitNote salesDebitNote = new SalesDebitNote();
      numIndexes        = salesDebitNote.getIndexCreateStringsDebitNoteL(indexCreateStrings);
      tableCreateString = salesDebitNote.getTableCreateStringDebitNoteL();
      fieldNames        = salesDebitNote.getFieldNamesDebitNoteL();
      fieldTypes        = salesDebitNote.getFieldTypesDebitNoteL();
    } 
     
    if(tableName.equalsIgnoreCase("debitll"))
    {
      SalesDebitNote salesDebitNote = new SalesDebitNote();
      numIndexes        = salesDebitNote.getIndexCreateStringsDebitNoteLL(indexCreateStrings);
      tableCreateString = salesDebitNote.getTableCreateStringDebitNoteLL();
      fieldNames        = salesDebitNote.getFieldNamesDebitNoteLL();
      fieldTypes        = salesDebitNote.getFieldTypesDebitNoteLL();
    } 
     
    if(tableName.equalsIgnoreCase("demousers"))
    {
      Profile profile = new Profile();
      numIndexes        = profile.getIndexCreateStringsDemoUsers(indexCreateStrings);
      tableCreateString = profile.getTableCreateStringDemoUsers();
      fieldNames        = profile.getFieldNamesDemoUsers();
      fieldTypes        = profile.getFieldTypesDemoUsers();
    }

    if(tableName.equalsIgnoreCase("do"))
    {
      DeliveryOrder deliveryOrder = new DeliveryOrder();
      numIndexes        = deliveryOrder.getIndexCreateStringsDO(indexCreateStrings);
      tableCreateString = deliveryOrder.getTableCreateStringDO();
      fieldNames        = deliveryOrder.getFieldNamesDO();
      fieldTypes        = deliveryOrder.getFieldTypesDO();
    } 
       
    if(tableName.equalsIgnoreCase("dol"))
    { 
      DeliveryOrder deliveryOrder = new DeliveryOrder();
      numIndexes        = deliveryOrder.getIndexCreateStringsDOL(indexCreateStrings);
      tableCreateString = deliveryOrder.getTableCreateStringDOL();
      fieldNames        = deliveryOrder.getFieldNamesDOL();
      fieldTypes        = deliveryOrder.getFieldTypesDOL();
    } 
       
    if(tableName.equalsIgnoreCase("doll"))
    {
      DeliveryOrder deliveryOrder = new DeliveryOrder();
      numIndexes        = deliveryOrder.getIndexCreateStringsDOLL(indexCreateStrings);
      tableCreateString = deliveryOrder.getTableCreateStringDOLL();
      fieldNames        = deliveryOrder.getFieldNamesDOLL();
      fieldTypes        = deliveryOrder.getFieldTypesDOLL();
    } 
       
    if(tableName.equalsIgnoreCase("doa"))
    {
      DeliveryOrder deliveryOrder = new DeliveryOrder();
      numIndexes        = deliveryOrder.getIndexCreateStringsDOA(indexCreateStrings);
      tableCreateString = deliveryOrder.getTableCreateStringDOA();
      fieldNames        = deliveryOrder.getFieldNamesDOA();
      fieldTypes        = deliveryOrder.getFieldTypesDOA();
    } 
       
    if(tableName.equalsIgnoreCase("documentoptions"))
    {
      DefinitionTables definitionTables = new DefinitionTables();
      numIndexes        = definitionTables.getIndexCreateStringsDocumentOptions(indexCreateStrings);
      tableCreateString = definitionTables.getTableCreateStringDocumentOptions();
      fieldNames        = definitionTables.getFieldNamesDocumentOptions();
      fieldTypes        = definitionTables.getFieldTypesDocumentOptions();
    } 
     
    if(tableName.equalsIgnoreCase("documentsettings"))
    {
      DefinitionTables definitionTables = new DefinitionTables();
      numIndexes        = definitionTables.getIndexCreateStringsDocumentSettings(indexCreateStrings);
      tableCreateString = definitionTables.getTableCreateStringDocumentSettings();
      fieldNames        = definitionTables.getFieldNamesDocumentSettings();
      fieldTypes        = definitionTables.getFieldTypesDocumentSettings();
    } 

    if(tableName.equalsIgnoreCase("driver"))
    {
      DocumentUtils documentUtils = new DocumentUtils();
      numIndexes        = documentUtils.getIndexCreateStringsDriver(indexCreateStrings);
      tableCreateString = documentUtils.getTableCreateStringDriver();
      fieldNames        = documentUtils.getFieldNamesDriver();
      fieldTypes        = documentUtils.getFieldTypesDriver();
    }

    if(tableName.equalsIgnoreCase("enquiry"))
    {
      Enquiry enquiry = new Enquiry();
      numIndexes        = enquiry.getIndexCreateStringsEnquiry(indexCreateStrings);
      tableCreateString = enquiry.getTableCreateStringEnquiry();
      fieldNames        = enquiry.getFieldNamesEnquiry();
      fieldTypes        = enquiry.getFieldTypesEnquiry();
    } 
       
    if(tableName.equalsIgnoreCase("enquirya"))
    {
      Enquiry enquiry = new Enquiry();
      numIndexes        = enquiry.getIndexCreateStringsEnquiryA(indexCreateStrings);
      tableCreateString = enquiry.getTableCreateStringEnquiryA();
      fieldNames        = enquiry.getFieldNamesEnquiryA();
      fieldTypes        = enquiry.getFieldTypesEnquiryA();
    } 

    if(tableName.equalsIgnoreCase("enquiryl"))
    {
      Enquiry enquiry = new Enquiry();
      numIndexes        = enquiry.getIndexCreateStringsEnquiryL(indexCreateStrings);
      tableCreateString = enquiry.getTableCreateStringEnquiryL();
      fieldNames        = enquiry.getFieldNamesEnquiryL();
      fieldTypes        = enquiry.getFieldTypesEnquiryL();
    }
       
    if(tableName.equalsIgnoreCase("enquiryll"))
    {
      Enquiry enquiry = new Enquiry();
      numIndexes        = enquiry.getIndexCreateStringsEnquiryLL(indexCreateStrings);
      tableCreateString = enquiry.getTableCreateStringEnquiryLL();
      fieldNames        = enquiry.getFieldNamesEnquiryLL();
      fieldTypes        = enquiry.getFieldTypesEnquiryLL();
    }
    
    if(tableName.equalsIgnoreCase("externalaccess"))
    {
      Profile profile = new Profile();
      numIndexes        = profile.getIndexCreateStringsExternalAccess(indexCreateStrings);
      tableCreateString = profile.getTableCreateStringExternalAccess();
      fieldNames        = profile.getFieldNamesExternalAccess();
      fieldTypes        = profile.getFieldTypesExternalAccess();
    }
    
    if(tableName.equalsIgnoreCase("gr"))
    {
      GoodsReceivedNote goodsReceivedNote = new GoodsReceivedNote();
      numIndexes        = goodsReceivedNote.getIndexCreateStringsGR(indexCreateStrings);
      tableCreateString = goodsReceivedNote.getTableCreateStringGR();
      fieldNames        = goodsReceivedNote.getFieldNamesGR();
      fieldTypes        = goodsReceivedNote.getFieldTypesGR();
    } 
   
    if(tableName.equalsIgnoreCase("grl"))
    {
      GoodsReceivedNote goodsReceivedNote = new GoodsReceivedNote();
      numIndexes        = goodsReceivedNote.getIndexCreateStringsGRL(indexCreateStrings);
      tableCreateString = goodsReceivedNote.getTableCreateStringGRL();
      fieldNames        = goodsReceivedNote.getFieldNamesGRL();
      fieldTypes        = goodsReceivedNote.getFieldTypesGRL();
    }

    if(tableName.equalsIgnoreCase("gstrate"))
    {
      AccountsTables accountsTables = new AccountsTables();
      numIndexes        = accountsTables.getIndexCreateStringsGSTRate(indexCreateStrings);
      tableCreateString = accountsTables.getTableCreateStringGSTRate();
      fieldNames        = accountsTables.getFieldNamesGSTRate();
      fieldTypes        = accountsTables.getFieldTypesGSTRate();
    }

    if(tableName.equalsIgnoreCase("iat"))
    {
      InterAccountTransfer interAccountTransfer = new InterAccountTransfer();
      numIndexes        = interAccountTransfer.getIndexCreateStringsIAT(indexCreateStrings);
      tableCreateString = interAccountTransfer.getTableCreateStringIAT();
      fieldNames        = interAccountTransfer.getFieldNamesIAT();
      fieldTypes        = interAccountTransfer.getFieldTypesIAT();
    }

    if(tableName.equalsIgnoreCase("inbox"))
    {
      Inbox inbox = new Inbox();
      numIndexes        = inbox.getIndexCreateStringsInbox(indexCreateStrings);
      tableCreateString = inbox.getTableCreateStringInbox();
      fieldNames        = inbox.getFieldNamesInbox();
      fieldTypes        = inbox.getFieldTypesInbox();
    } 
   
    if(tableName.equalsIgnoreCase("inboxl"))
    { 
      Inbox inbox = new Inbox();
      numIndexes        = inbox.getIndexCreateStringsInboxL(indexCreateStrings);
      tableCreateString = inbox.getTableCreateStringInboxL();
      fieldNames        = inbox.getFieldNamesInboxL();
      fieldTypes        = inbox.getFieldTypesInboxL();
    } 
   
    if(tableName.equalsIgnoreCase("industrytype"))
    {
      DocumentUtils documentUtils = new DocumentUtils();
      numIndexes        = documentUtils.getIndexCreateStringsIndustryType(indexCreateStrings);
      tableCreateString = documentUtils.getTableCreateStringIndustryType();
      fieldNames        = documentUtils.getFieldNamesIndustryType();
      fieldTypes        = documentUtils.getFieldTypesIndustryType();
    }

    if(tableName.equalsIgnoreCase("invoice"))
    {
      SalesInvoice salesInvoice = new SalesInvoice();
      numIndexes        = salesInvoice.getIndexCreateStringsInvoice(indexCreateStrings);
      tableCreateString = salesInvoice.getTableCreateStringInvoice();
      fieldNames        = salesInvoice.getFieldNamesInvoice();
      fieldTypes        = salesInvoice.getFieldTypesInvoice();
    } 
     
    if(tableName.equalsIgnoreCase("invoicel"))
    { 
      SalesInvoice salesInvoice = new SalesInvoice();
      numIndexes        = salesInvoice.getIndexCreateStringsInvoiceL(indexCreateStrings);
      tableCreateString = salesInvoice.getTableCreateStringInvoiceL();
      fieldNames        = salesInvoice.getFieldNamesInvoiceL();
      fieldTypes        = salesInvoice.getFieldTypesInvoiceL();
    } 
     
    if(tableName.equalsIgnoreCase("invoicell"))
    {
      SalesInvoice salesInvoice = new SalesInvoice();
      numIndexes        = salesInvoice.getIndexCreateStringsInvoiceLL(indexCreateStrings);
      tableCreateString = salesInvoice.getTableCreateStringInvoiceLL();
      fieldNames        = salesInvoice.getFieldNamesInvoiceLL();
      fieldTypes        = salesInvoice.getFieldTypesInvoiceLL();
    } 
     
    if(tableName.equalsIgnoreCase("invoicea"))
    {
      SalesInvoice salesInvoice = new SalesInvoice();
      numIndexes        = salesInvoice.getIndexCreateStringsInvoiceA(indexCreateStrings);
      tableCreateString = salesInvoice.getTableCreateStringInvoiceA();
      fieldNames        = salesInvoice.getFieldNamesInvoiceA();
      fieldTypes        = salesInvoice.getFieldTypesInvoiceA();
    } 

    if(tableName.equalsIgnoreCase("lasttrail"))
    {
      Trail trail = new Trail();
      numIndexes        = trail.getIndexCreateStringsLastTrail(indexCreateStrings);
      tableCreateString = trail.getTableCreateStringLastTrail();
      fieldNames        = trail.getFieldNamesLastTrail();
      fieldTypes        = trail.getFieldTypesLastTrail();
    } 
    
    if(tableName.equalsIgnoreCase("linkedcat"))
    {
      DefinitionTables definitionTables = new DefinitionTables();
      numIndexes        = definitionTables.getIndexCreateStringsLinkedCat(indexCreateStrings);
      tableCreateString = definitionTables.getTableCreateStringLinkedCat();
      fieldNames        = definitionTables.getFieldNamesLinkedCat();
      fieldTypes        = definitionTables.getFieldTypesLinkedCat();
    } 
   
    if(tableName.equalsIgnoreCase("locks"))
    {
      DocumentUtils documentUtils = new DocumentUtils();
      numIndexes        = documentUtils.getIndexCreateStringsLocks(indexCreateStrings);
      tableCreateString = documentUtils.getTableCreateStringLocks();
      fieldNames        = documentUtils.getFieldNamesLocks();
      fieldTypes        = documentUtils.getFieldTypesLocks();
    }
  
    if(tableName.equalsIgnoreCase("lp"))
    {
      LocalPurchase localPurchase = new LocalPurchase();
      numIndexes        = localPurchase.getIndexCreateStringsLP(indexCreateStrings);
      tableCreateString = localPurchase.getTableCreateStringLP();
      fieldNames        = localPurchase.getFieldNamesLP();
      fieldTypes        = localPurchase.getFieldTypesLP();
    }

    if(tableName.equalsIgnoreCase("lpl"))
    {
      LocalPurchase localPurchase = new LocalPurchase();
      numIndexes        = localPurchase.getIndexCreateStringsLPL(indexCreateStrings);
      tableCreateString = localPurchase.getTableCreateStringLPL();
      fieldNames        = localPurchase.getFieldNamesLPL();
      fieldTypes        = localPurchase.getFieldTypesLPL();
    }

    if(tableName.equalsIgnoreCase("lpll"))
    {
      LocalPurchase localPurchase = new LocalPurchase();
      numIndexes        = localPurchase.getIndexCreateStringsLPLL(indexCreateStrings);
      tableCreateString = localPurchase.getTableCreateStringLPLL();
      fieldNames        = localPurchase.getFieldNamesLPLL();
      fieldTypes        = localPurchase.getFieldTypesLPLL();
    }

    if(tableName.equalsIgnoreCase("lpa"))
    {
      LocalPurchase localPurchase = new LocalPurchase();
      numIndexes        = localPurchase.getIndexCreateStringsLPA(indexCreateStrings);
      tableCreateString = localPurchase.getTableCreateStringLPA();
      fieldNames        = localPurchase.getFieldNamesLPA();
      fieldTypes        = localPurchase.getFieldTypesLPA();
    }

    if(tableName.equalsIgnoreCase("lr"))
    {
      LocalRequisition localRequisition = new LocalRequisition();
      numIndexes        = localRequisition.getIndexCreateStringsLR(indexCreateStrings);
      tableCreateString = localRequisition.getTableCreateStringLR();
      fieldNames        = localRequisition.getFieldNamesLR();
      fieldTypes        = localRequisition.getFieldTypesLR();
    }

    if(tableName.equalsIgnoreCase("lrl"))
    {
      LocalRequisition localRequisition = new LocalRequisition();
      numIndexes        = localRequisition.getIndexCreateStringsLRL(indexCreateStrings);
      tableCreateString = localRequisition.getTableCreateStringLRL();
      fieldNames        = localRequisition.getFieldNamesLRL();
      fieldTypes        = localRequisition.getFieldTypesLRL();
    }

    if(tableName.equalsIgnoreCase("lrll"))
    {
      LocalRequisition localRequisition = new LocalRequisition();
      numIndexes        = localRequisition.getIndexCreateStringsLRLL(indexCreateStrings);
      tableCreateString = localRequisition.getTableCreateStringLRLL();
      fieldNames        = localRequisition.getFieldNamesLRLL();
      fieldTypes        = localRequisition.getFieldTypesLRLL();
    }

    if(tableName.equalsIgnoreCase("lra"))
    {
      LocalRequisition localRequisition = new LocalRequisition();
      numIndexes        = localRequisition.getIndexCreateStringsLRA(indexCreateStrings);
      tableCreateString = localRequisition.getTableCreateStringLRA();
      fieldNames        = localRequisition.getFieldNamesLRA();
      fieldTypes        = localRequisition.getFieldTypesLRA();
    }

    if(tableName.equalsIgnoreCase("managepeople"))
    {
      DocumentUtils documentUtils = new DocumentUtils();
      numIndexes        = documentUtils.getIndexCreateStringsManagepeople(indexCreateStrings);
      tableCreateString = documentUtils.getTableCreateStringManagepeople();
      fieldNames        = documentUtils.getFieldNamesManagepeople();
      fieldTypes        = documentUtils.getFieldTypesManagepeople();
    }

    if(tableName.equalsIgnoreCase("moduleservices"))
    {
      DefinitionTables definitionTables = new DefinitionTables();
      numIndexes        = definitionTables.getIndexCreateStringsModuleServices(indexCreateStrings);
      tableCreateString = definitionTables.getTableCreateStringModuleServices();
      fieldNames        = definitionTables.getFieldNamesModuleServices();
      fieldTypes        = definitionTables.getFieldTypesModuleServices();
    } 
     
    if(tableName.equalsIgnoreCase("oc"))
    {
      OrderConfirmation orderConfirmation = new OrderConfirmation();
      numIndexes        = orderConfirmation.getIndexCreateStringsOC(indexCreateStrings);
      tableCreateString = orderConfirmation.getTableCreateStringOC();
      fieldNames        = orderConfirmation.getFieldNamesOC();
      fieldTypes        = orderConfirmation.getFieldTypesOC();
    } 
     
    if(tableName.equalsIgnoreCase("ocl"))
    { 
      OrderConfirmation orderConfirmation = new OrderConfirmation();
      numIndexes        = orderConfirmation.getIndexCreateStringsOCL(indexCreateStrings);
      tableCreateString = orderConfirmation.getTableCreateStringOCL();
      fieldNames        = orderConfirmation.getFieldNamesOCL();
      fieldTypes        = orderConfirmation.getFieldTypesOCL();
    } 
     
    if(tableName.equalsIgnoreCase("ocll"))
    {
      OrderConfirmation orderConfirmation = new OrderConfirmation();
      numIndexes        = orderConfirmation.getIndexCreateStringsOCLL(indexCreateStrings);
      tableCreateString = orderConfirmation.getTableCreateStringOCLL();
      fieldNames        = orderConfirmation.getFieldNamesOCLL();
      fieldTypes        = orderConfirmation.getFieldTypesOCLL();
    } 
     
    if(tableName.equalsIgnoreCase("oca"))
    {
      OrderConfirmation orderConfirmation = new OrderConfirmation();
      numIndexes        = orderConfirmation.getIndexCreateStringsOCA(indexCreateStrings);
      tableCreateString = orderConfirmation.getTableCreateStringOCA();
      fieldNames        = orderConfirmation.getFieldNamesOCA();
      fieldTypes        = orderConfirmation.getFieldTypesOCA();
    }
    
    if(tableName.equalsIgnoreCase("oa"))
    {
      OrderAcknowledgement orderAcknowledgement = new OrderAcknowledgement();
      numIndexes        = orderAcknowledgement.getIndexCreateStringsOA(indexCreateStrings);
      tableCreateString = orderAcknowledgement.getTableCreateStringOA();
      fieldNames        = orderAcknowledgement.getFieldNamesOA();
      fieldTypes        = orderAcknowledgement.getFieldTypesOA();
    } 
     
    if(tableName.equalsIgnoreCase("oal"))
    { 
      OrderAcknowledgement orderAcknowledgement = new OrderAcknowledgement();
      numIndexes        = orderAcknowledgement.getIndexCreateStringsOAL(indexCreateStrings);
      tableCreateString = orderAcknowledgement.getTableCreateStringOAL();
      fieldNames        = orderAcknowledgement.getFieldNamesOAL();
      fieldTypes        = orderAcknowledgement.getFieldTypesOAL();
    } 
     
    if(tableName.equalsIgnoreCase("oall"))
    {
      OrderAcknowledgement orderAcknowledgement = new OrderAcknowledgement();
      numIndexes        = orderAcknowledgement.getIndexCreateStringsOALL(indexCreateStrings);
      tableCreateString = orderAcknowledgement.getTableCreateStringOALL();
      fieldNames        = orderAcknowledgement.getFieldNamesOALL();
      fieldTypes        = orderAcknowledgement.getFieldTypesOALL();
    } 
     
    if(tableName.equalsIgnoreCase("oaa"))
    {
      OrderAcknowledgement orderAcknowledgement = new OrderAcknowledgement();
      numIndexes        = orderAcknowledgement.getIndexCreateStringsOAA(indexCreateStrings);
      tableCreateString = orderAcknowledgement.getTableCreateStringOAA();
      fieldNames        = orderAcknowledgement.getFieldNamesOAA();
      fieldTypes        = orderAcknowledgement.getFieldTypesOAA();    
    }
    
    if(tableName.equalsIgnoreCase("organizations"))
    { 
      Profile profile = new Profile();
      numIndexes        = profile.getIndexCreateStringsOrganizations(indexCreateStrings);
      tableCreateString = profile.getTableCreateStringOrganizations();
      fieldNames        = profile.getFieldNamesOrganizations();
      fieldTypes        = profile.getFieldTypesOrganizations();
    } 
   
    if(tableName.equalsIgnoreCase("payment"))
    { 
      Payment payment = new Payment();
      numIndexes        = payment.getIndexCreateStringsPayment(indexCreateStrings);
      tableCreateString = payment.getTableCreateStringPayment();
      fieldNames        = payment.getFieldNamesPayment();
      fieldTypes        = payment.getFieldTypesPayment();
    } 
   
    if(tableName.equalsIgnoreCase("paymentl"))
    { 
      Payment payment = new Payment();
      numIndexes        = payment.getIndexCreateStringsPaymentL(indexCreateStrings);
      tableCreateString = payment.getTableCreateStringPaymentL();
      fieldNames        = payment.getFieldNamesPaymentL();
      fieldTypes        = payment.getFieldTypesPaymentL();
    } 
    
    if(tableName.equalsIgnoreCase("pcredit"))
    {
      PurchaseCreditNote purchaseCreditNote = new PurchaseCreditNote();
      numIndexes        = purchaseCreditNote.getIndexCreateStringsPurchaseCreditNote(indexCreateStrings);
      tableCreateString = purchaseCreditNote.getTableCreateStringPurchaseCreditNote();
      fieldNames        = purchaseCreditNote.getFieldNamesPurchaseCreditNote();
      fieldTypes        = purchaseCreditNote.getFieldTypesPurchaseCreditNote();
    }  
 
    if(tableName.equalsIgnoreCase("pcreditl"))
    { 
      PurchaseCreditNote purchaseCreditNote = new PurchaseCreditNote();
      numIndexes        = purchaseCreditNote.getIndexCreateStringsPurchaseCreditNoteL(indexCreateStrings);
      tableCreateString = purchaseCreditNote.getTableCreateStringPurchaseCreditNoteL();
      fieldNames        = purchaseCreditNote.getFieldNamesPurchaseCreditNoteL();
      fieldTypes        = purchaseCreditNote.getFieldTypesPurchaseCreditNoteL();
    } 
 
    if(tableName.equalsIgnoreCase("pcreditll"))
    {
      PurchaseCreditNote purchaseCreditNote = new PurchaseCreditNote();
      numIndexes        = purchaseCreditNote.getIndexCreateStringsPurchaseCreditNoteLL(indexCreateStrings);
      tableCreateString = purchaseCreditNote.getTableCreateStringPurchaseCreditNoteLL();
      fieldNames        = purchaseCreditNote.getFieldNamesPurchaseCreditNoteLL();
      fieldTypes        = purchaseCreditNote.getFieldTypesPurchaseCreditNoteLL();
    } 
 
    if(tableName.equalsIgnoreCase("pcredita")) 
    {
      PurchaseCreditNote purchaseCreditNote = new PurchaseCreditNote();
      numIndexes        = purchaseCreditNote.getIndexCreateStringsPurchaseCreditNoteA(indexCreateStrings);
      tableCreateString = purchaseCreditNote.getTableCreateStringPurchaseCreditNoteA();
      fieldNames        = purchaseCreditNote.getFieldNamesPurchaseCreditNoteA();
      fieldTypes        = purchaseCreditNote.getFieldTypesPurchaseCreditNoteA();
    }

    if(tableName.equalsIgnoreCase("pdebit"))
    {
      PurchaseDebitNote purchaseDebitNote = new PurchaseDebitNote();
      numIndexes        = purchaseDebitNote.getIndexCreateStringsPurchaseDebitNote(indexCreateStrings);
      tableCreateString = purchaseDebitNote.getTableCreateStringPurchaseDebitNote();
      fieldNames        = purchaseDebitNote.getFieldNamesPurchaseDebitNote();
      fieldTypes        = purchaseDebitNote.getFieldTypesPurchaseDebitNote();
    } 
     
    if(tableName.equalsIgnoreCase("pdebitl"))
    { 
      PurchaseDebitNote purchaseDebitNote = new PurchaseDebitNote();
      numIndexes        = purchaseDebitNote.getIndexCreateStringsPurchaseDebitNoteL(indexCreateStrings);
      tableCreateString = purchaseDebitNote.getTableCreateStringPurchaseDebitNoteL();
      fieldNames        = purchaseDebitNote.getFieldNamesPurchaseDebitNoteL();
      fieldTypes        = purchaseDebitNote.getFieldTypesPurchaseDebitNoteL();
    } 
     
    if(tableName.equalsIgnoreCase("pdebitll"))
    {
      PurchaseDebitNote purchaseDebitNote = new PurchaseDebitNote();
      numIndexes        = purchaseDebitNote.getIndexCreateStringsPurchaseDebitNoteLL(indexCreateStrings);
      tableCreateString = purchaseDebitNote.getTableCreateStringPurchaseDebitNoteLL();
      fieldNames        = purchaseDebitNote.getFieldNamesPurchaseDebitNoteLL();
      fieldTypes        = purchaseDebitNote.getFieldTypesPurchaseDebitNoteLL();
    } 
     
    if(tableName.equalsIgnoreCase("pdebita"))
    {
      PurchaseDebitNote purchaseDebitNote = new PurchaseDebitNote();
      numIndexes        = purchaseDebitNote.getIndexCreateStringsPurchaseDebitNoteA(indexCreateStrings);
      tableCreateString = purchaseDebitNote.getTableCreateStringPurchaseDebitNoteA();
      fieldNames        = purchaseDebitNote.getFieldNamesPurchaseDebitNoteA();
      fieldTypes        = purchaseDebitNote.getFieldTypesPurchaseDebitNoteA();
    } 
   
    if(tableName.equalsIgnoreCase("personposition"))
    {
      Profile profile = new Profile();
      numIndexes        = profile.getIndexCreateStringsPersonposition(indexCreateStrings);
      tableCreateString = profile.getTableCreateStringPersonposition();
      fieldNames        = profile.getFieldNamesPersonposition();
      fieldTypes        = profile.getFieldTypesPersonposition();
    }

    if(tableName.equalsIgnoreCase("pinvoice"))
    {
      PurchaseInvoice purchaseInvoice = new PurchaseInvoice();
      numIndexes        = purchaseInvoice.getIndexCreateStringsPurchaseInvoice(indexCreateStrings);
      tableCreateString = purchaseInvoice.getTableCreateStringPurchaseInvoice();
      fieldNames        = purchaseInvoice.getFieldNamesPurchaseInvoice();
      fieldTypes        = purchaseInvoice.getFieldTypesPurchaseInvoice();
    } 
   
    if(tableName.equalsIgnoreCase("pinvoicel"))
    { 
      PurchaseInvoice purchaseInvoice = new PurchaseInvoice();
      numIndexes        = purchaseInvoice.getIndexCreateStringsPurchaseInvoiceL(indexCreateStrings);
      tableCreateString = purchaseInvoice.getTableCreateStringPurchaseInvoiceL();
      fieldNames        = purchaseInvoice.getFieldNamesPurchaseInvoiceL();
      fieldTypes        = purchaseInvoice.getFieldTypesPurchaseInvoiceL();
    } 
   
    if(tableName.equalsIgnoreCase("pinvoicell"))
    {
      PurchaseInvoice purchaseInvoice = new PurchaseInvoice();
      numIndexes        = purchaseInvoice.getIndexCreateStringsPurchaseInvoiceLL(indexCreateStrings);
      tableCreateString = purchaseInvoice.getTableCreateStringPurchaseInvoiceLL();
      fieldNames        = purchaseInvoice.getFieldNamesPurchaseInvoiceLL();
      fieldTypes        = purchaseInvoice.getFieldTypesPurchaseInvoiceLL();
    } 
   
    if(tableName.equalsIgnoreCase("pinvoicea"))
    {
      PurchaseInvoice purchaseInvoice = new PurchaseInvoice();
      numIndexes        = purchaseInvoice.getIndexCreateStringsPurchaseInvoiceA(indexCreateStrings);
      tableCreateString = purchaseInvoice.getTableCreateStringPurchaseInvoiceA();
      fieldNames        = purchaseInvoice.getFieldNamesPurchaseInvoiceA();
      fieldTypes        = purchaseInvoice.getFieldTypesPurchaseInvoiceA();
    } 

    if(tableName.equalsIgnoreCase("pl"))
    {
      PickingList pickingList = new PickingList();
      numIndexes        = pickingList.getIndexCreateStringsPL(indexCreateStrings);
      tableCreateString = pickingList.getTableCreateStringPL();
      fieldNames        = pickingList.getFieldNamesPL();
      fieldTypes        = pickingList.getFieldTypesPL();
    } 
       
    if(tableName.equalsIgnoreCase("pll"))
    {
      PickingList pickingList = new PickingList();
      numIndexes        = pickingList.getIndexCreateStringsPLL(indexCreateStrings);
      tableCreateString = pickingList.getTableCreateStringPLL();
      fieldNames        = pickingList.getFieldNamesPLL();
      fieldTypes        = pickingList.getFieldTypesPLL();
    } 
       
    if(tableName.equalsIgnoreCase("plll"))
    {
      PickingList pickingList = new PickingList();
      numIndexes        = pickingList.getIndexCreateStringsPLLL(indexCreateStrings);
      tableCreateString = pickingList.getTableCreateStringPLLL();
      fieldNames        = pickingList.getFieldNamesPLLL();
      fieldTypes        = pickingList.getFieldTypesPLLL();
    } 
       
    if(tableName.equalsIgnoreCase("pla"))
    {
      PickingList pickingList = new PickingList();
      numIndexes        = pickingList.getIndexCreateStringsPLA(indexCreateStrings);
      tableCreateString = pickingList.getTableCreateStringPLA();
      fieldNames        = pickingList.getFieldNamesPLA();
      fieldTypes        = pickingList.getFieldTypesPLA();
    } 

    if(tableName.equalsIgnoreCase("po"))
    {
      PurchaseOrder purchaseOrder = new PurchaseOrder();
      numIndexes        = purchaseOrder.getIndexCreateStringsPO(indexCreateStrings);
      tableCreateString = purchaseOrder.getTableCreateStringPO();
      fieldNames        = purchaseOrder.getFieldNamesPO();
      fieldTypes        = purchaseOrder.getFieldTypesPO();
    } 
     
    if(tableName.equalsIgnoreCase("poa"))
    {
      PurchaseOrder purchaseOrder = new PurchaseOrder();
      numIndexes        = purchaseOrder.getIndexCreateStringsPOA(indexCreateStrings);
      tableCreateString = purchaseOrder.getTableCreateStringPOA();
      fieldNames        = purchaseOrder.getFieldNamesPOA();
      fieldTypes        = purchaseOrder.getFieldTypesPOA();
    } 

    if(tableName.equalsIgnoreCase("pol"))
    {
      PurchaseOrder purchaseOrder = new PurchaseOrder();
      numIndexes        = purchaseOrder.getIndexCreateStringsPOL(indexCreateStrings);
      tableCreateString = purchaseOrder.getTableCreateStringPOL();
      fieldNames        = purchaseOrder.getFieldNamesPOL();
      fieldTypes        = purchaseOrder.getFieldTypesPOL();
    }
     
    if(tableName.equalsIgnoreCase("poll"))
    {
      PurchaseOrder purchaseOrder = new PurchaseOrder();
      numIndexes        = purchaseOrder.getIndexCreateStringsPOLL(indexCreateStrings);
      tableCreateString = purchaseOrder.getTableCreateStringPOLL();
      fieldNames        = purchaseOrder.getFieldNamesPOLL();
      fieldTypes        = purchaseOrder.getFieldTypesPOLL();
    }
  
    if(tableName.equalsIgnoreCase("profiles"))
    {
      Profile profile = new Profile();
      numIndexes        = profile.getIndexCreateStringsProfiles(indexCreateStrings);
      tableCreateString = profile.getTableCreateStringProfiles();
      fieldNames        = profile.getFieldNamesProfiles();
      fieldTypes        = profile.getFieldTypesProfiles();
    }
    
    if(tableName.equalsIgnoreCase("proforma"))
    {
      ProformaInvoice proformaInvoice = new ProformaInvoice();
      numIndexes        = proformaInvoice.getIndexCreateStringsProforma(indexCreateStrings);
      tableCreateString = proformaInvoice.getTableCreateStringProforma();
      fieldNames        = proformaInvoice.getFieldNamesProforma();
      fieldTypes        = proformaInvoice.getFieldTypesProforma();
    } 
     
    if(tableName.equalsIgnoreCase("proformal"))
    {
      ProformaInvoice proformaInvoice = new ProformaInvoice();
      numIndexes        = proformaInvoice.getIndexCreateStringsProformaL(indexCreateStrings);
      tableCreateString = proformaInvoice.getTableCreateStringProformaL();
      fieldNames        = proformaInvoice.getFieldNamesProformaL();
      fieldTypes        = proformaInvoice.getFieldTypesProformaL();
    } 
     
    if(tableName.equalsIgnoreCase("proformall"))
    {
      ProformaInvoice proformaInvoice = new ProformaInvoice();
      numIndexes        = proformaInvoice.getIndexCreateStringsProformaLL(indexCreateStrings);
      tableCreateString = proformaInvoice.getTableCreateStringProformaLL();
      fieldNames        = proformaInvoice.getFieldNamesProformaLL();
      fieldTypes        = proformaInvoice.getFieldTypesProformaLL();
    } 
     
    if(tableName.equalsIgnoreCase("proformaa"))
    {
      ProformaInvoice proformaInvoice = new ProformaInvoice();
      numIndexes        = proformaInvoice.getIndexCreateStringsProformaA(indexCreateStrings);
      tableCreateString = proformaInvoice.getTableCreateStringProformaA();
      fieldNames        = proformaInvoice.getFieldNamesProformaA();
      fieldTypes        = proformaInvoice.getFieldTypesProformaA();
    } 
   
    if(tableName.equalsIgnoreCase("quolike"))
    {
      DocumentUtils documentUtils = new DocumentUtils();
      numIndexes        = documentUtils.getIndexCreateStringsQuoLike(indexCreateStrings);
      tableCreateString = documentUtils.getTableCreateStringQuoLike();
      fieldNames        = documentUtils.getFieldNamesQuoLike();
      fieldTypes        = documentUtils.getFieldTypesQuoLike();
    }     
    
    if(tableName.equalsIgnoreCase("quoreasons"))
    {
      DocumentUtils documentUtils = new DocumentUtils();
      numIndexes        = documentUtils.getIndexCreateStringsQuoReasons(indexCreateStrings);
      tableCreateString = documentUtils.getTableCreateStringQuoReasons();
      fieldNames        = documentUtils.getFieldNamesQuoReasons();
      fieldTypes        = documentUtils.getFieldTypesQuoReasons();
    }     
        
    if(tableName.equalsIgnoreCase("quostate"))
    {
      DocumentUtils documentUtils = new DocumentUtils();
      numIndexes        = documentUtils.getIndexCreateStringsQuoState(indexCreateStrings);
      tableCreateString = documentUtils.getTableCreateStringQuoState();
      fieldNames        = documentUtils.getFieldNamesQuoState();
      fieldTypes        = documentUtils.getFieldTypesQuoState();
    }     

    if(tableName.equalsIgnoreCase("quote"))
    {
      Quotation quotation = new Quotation();
      numIndexes        = quotation.getIndexCreateStringsQuote(indexCreateStrings);
      tableCreateString = quotation.getTableCreateStringQuote();
      fieldNames        = quotation.getFieldNamesQuote();
      fieldTypes        = quotation.getFieldTypesQuote();
    } 
       
    if(tableName.equalsIgnoreCase("quotea"))
    {
      Quotation quotation = new Quotation();
      numIndexes        = quotation.getIndexCreateStringsQuoteA(indexCreateStrings);
      tableCreateString = quotation.getTableCreateStringQuoteA();
      fieldNames        = quotation.getFieldNamesQuoteA();
      fieldTypes        = quotation.getFieldTypesQuoteA();
    } 

    if(tableName.equalsIgnoreCase("quotel"))
    {
      Quotation quotation = new Quotation();
      numIndexes        = quotation.getIndexCreateStringsQuoteL(indexCreateStrings);
      tableCreateString = quotation.getTableCreateStringQuoteL();
      fieldNames        = quotation.getFieldNamesQuoteL();
      fieldTypes        = quotation.getFieldTypesQuoteL();
    }
       
    if(tableName.equalsIgnoreCase("quotell"))
    {
      Quotation quotation = new Quotation();
      numIndexes        = quotation.getIndexCreateStringsQuoteLL(indexCreateStrings);
      tableCreateString = quotation.getTableCreateStringQuoteLL();
      fieldNames        = quotation.getFieldNamesQuoteLL();
      fieldTypes        = quotation.getFieldTypesQuoteLL();
    }
    
    if(tableName.equalsIgnoreCase("receipt"))
    { 
      Receipt receipt = new Receipt();
      numIndexes        = receipt.getIndexCreateStringsReceipt(indexCreateStrings);
      tableCreateString = receipt.getTableCreateStringReceipt();
      fieldNames        = receipt.getFieldNamesReceipt();
      fieldTypes        = receipt.getFieldTypesReceipt();
    } 
     
    if(tableName.equalsIgnoreCase("receiptl"))
    { 
      Receipt receipt = new Receipt();
      numIndexes        = receipt.getIndexCreateStringsReceiptL(indexCreateStrings);
      tableCreateString = receipt.getTableCreateStringReceiptL();
      fieldNames        = receipt.getFieldNamesReceiptL();
      fieldTypes        = receipt.getFieldTypesReceiptL();
    } 
     
    if(tableName.equalsIgnoreCase("registrants"))
    {
      Profile profile = new Profile();
      numIndexes        = profile.getIndexCreateStringsRegistrants(indexCreateStrings);
      tableCreateString = profile.getTableCreateStringRegistrants();
      fieldNames        = profile.getFieldNamesRegistrants();
      fieldTypes        = profile.getFieldTypesRegistrants();
    }

    if(tableName.equalsIgnoreCase("robottext"))
    {
      DefinitionTables definitionTables = new DefinitionTables();
      numIndexes        = definitionTables.getIndexCreateStringsRobotText(indexCreateStrings);
      tableCreateString = definitionTables.getTableCreateStringRobotText();
      fieldNames        = definitionTables.getFieldNamesRobotText();
      fieldTypes        = definitionTables.getFieldTypesRobotText();
    } 

    if(tableName.equalsIgnoreCase("rvoucher"))
    {
      ReceiptVoucher receiptVoucher = new ReceiptVoucher();
      numIndexes        = receiptVoucher.getIndexCreateStringsReceiptVoucher(indexCreateStrings);
      tableCreateString = receiptVoucher.getTableCreateStringReceiptVoucher();
      fieldNames        = receiptVoucher.getFieldNamesReceiptVoucher();
      fieldTypes        = receiptVoucher.getFieldTypesReceiptVoucher();
    } 
   
    if(tableName.equalsIgnoreCase("rvouchera"))
    {
      ReceiptVoucher receiptVoucher = new ReceiptVoucher();
      numIndexes        = receiptVoucher.getIndexCreateStringsReceiptVoucherA(indexCreateStrings);
      tableCreateString = receiptVoucher.getTableCreateStringReceiptVoucherA();
      fieldNames        = receiptVoucher.getFieldNamesReceiptVoucherA();
      fieldTypes        = receiptVoucher.getFieldTypesReceiptVoucherA();
    } 

    if(tableName.equalsIgnoreCase("rvoucherl"))
    {
      ReceiptVoucher receiptVoucher = new ReceiptVoucher();
      numIndexes        = receiptVoucher.getIndexCreateStringsReceiptVoucherL(indexCreateStrings);
      tableCreateString = receiptVoucher.getTableCreateStringReceiptVoucherL();
      fieldNames        = receiptVoucher.getFieldNamesReceiptVoucherL();
      fieldTypes        = receiptVoucher.getFieldTypesReceiptVoucherL();
    }
   
    if(tableName.equalsIgnoreCase("rvoucherll"))
    {
      ReceiptVoucher receiptVoucher = new ReceiptVoucher();
      numIndexes        = receiptVoucher.getIndexCreateStringsReceiptVoucherLL(indexCreateStrings);
      tableCreateString = receiptVoucher.getTableCreateStringReceiptVoucherLL();
      fieldNames        = receiptVoucher.getFieldNamesReceiptVoucherLL();
      fieldTypes        = receiptVoucher.getFieldTypesReceiptVoucherLL();
    }

    if(tableName.equalsIgnoreCase("salesper"))
    {
      DocumentUtils documentUtils = new DocumentUtils();
      numIndexes        = documentUtils.getIndexCreateStringsSalesper(indexCreateStrings);
      tableCreateString = documentUtils.getTableCreateStringSalesper();
      fieldNames        = documentUtils.getFieldNamesSalesper();
      fieldTypes        = documentUtils.getFieldTypesSalesper();
    }

    if(tableName.equalsIgnoreCase("scbrands"))
    {
      DefinitionTables definitionTables = new DefinitionTables();
      numIndexes        = definitionTables.getIndexCreateStringsSCBrands(indexCreateStrings);
      tableCreateString = definitionTables.getTableCreateStringSCBrands();
      fieldNames        = definitionTables.getFieldNamesSCBrands();
      fieldTypes        = definitionTables.getFieldTypesSCBrands();
    } 

    if(tableName.equalsIgnoreCase("scsites"))
    {
      DefinitionTables definitionTables = new DefinitionTables();
      numIndexes        = definitionTables.getIndexCreateStringsSCSites(indexCreateStrings);
      tableCreateString = definitionTables.getTableCreateStringSCSites();
      fieldNames        = definitionTables.getFieldNamesSCSites();
      fieldTypes        = definitionTables.getFieldTypesSCSites();
    } 

    if(tableName.equalsIgnoreCase("servers"))
    {
      DefinitionTables definitionTables = new DefinitionTables();
      numIndexes        = definitionTables.getIndexCreateStringsServers(indexCreateStrings);
      tableCreateString = definitionTables.getTableCreateStringServers();
      fieldNames        = definitionTables.getFieldNamesServers();
      fieldTypes        = definitionTables.getFieldTypesServers();
    }

    if(tableName.equalsIgnoreCase("scsitesbrands"))
    {
      DefinitionTables definitionTables = new DefinitionTables();
      numIndexes        = definitionTables.getIndexCreateStringsSCSitesBrands(indexCreateStrings);
      tableCreateString = definitionTables.getTableCreateStringSCSitesBrands();
      fieldNames        = definitionTables.getFieldNamesSCSitesBrands();
      fieldTypes        = definitionTables.getFieldTypesSCSitesBrands();
    }

    if(tableName.equalsIgnoreCase("so"))
    {
      SalesOrder salesOrder = new SalesOrder();
      numIndexes        = salesOrder.getIndexCreateStringsSO(indexCreateStrings);
      tableCreateString = salesOrder.getTableCreateStringSO();
      fieldNames        = salesOrder.getFieldNamesSO();
      fieldTypes        = salesOrder.getFieldTypesSO();
    } 
       
    if(tableName.equalsIgnoreCase("soa"))
    {
      SalesOrder salesOrder = new SalesOrder();
      numIndexes        = salesOrder.getIndexCreateStringsSOA(indexCreateStrings);
      tableCreateString = salesOrder.getTableCreateStringSOA();
      fieldNames        = salesOrder.getFieldNamesSOA();
      fieldTypes        = salesOrder.getFieldTypesSOA();
    } 

    if(tableName.equalsIgnoreCase("sol"))
    {
      SalesOrder salesOrder = new SalesOrder();
      numIndexes        = salesOrder.getIndexCreateStringsSOL(indexCreateStrings);
      tableCreateString = salesOrder.getTableCreateStringSOL();
      fieldNames        = salesOrder.getFieldNamesSOL();
      fieldTypes        = salesOrder.getFieldTypesSOL();
    }
       
    if(tableName.equalsIgnoreCase("soll"))
    {
      SalesOrder salesOrder = new SalesOrder();
      numIndexes        = salesOrder.getIndexCreateStringsSOLL(indexCreateStrings);
      tableCreateString = salesOrder.getTableCreateStringSOLL();
      fieldNames        = salesOrder.getFieldNamesSOLL();
      fieldTypes        = salesOrder.getFieldTypesSOLL();
    }
    
    if(tableName.equalsIgnoreCase("stock"))
    {
      Inventory inventory = new Inventory();
      numIndexes        = inventory.getIndexCreateStringsStock(indexCreateStrings);
      tableCreateString = inventory.getTableCreateStringStock();
      fieldNames        = inventory.getFieldNamesStock();
      fieldTypes        = inventory.getFieldTypesStock();
    } 

    if(tableName.equalsIgnoreCase("stocka"))
    {
      InventoryAdjustment inventoryAdjustment = new InventoryAdjustment();
      numIndexes        = inventoryAdjustment.getIndexCreateStringsStockA(indexCreateStrings);
      tableCreateString = inventoryAdjustment.getTableCreateStringStockA();
      fieldNames        = inventoryAdjustment.getFieldNamesStockA();
      fieldTypes        = inventoryAdjustment.getFieldTypesStockA();
    }

    if(tableName.equalsIgnoreCase("stockc"))
    {
      InventoryAdjustment inventoryAdjustment = new InventoryAdjustment();
      numIndexes        = inventoryAdjustment.getIndexCreateStringsStockC(indexCreateStrings);
      tableCreateString = inventoryAdjustment.getTableCreateStringStockC();
      fieldNames        = inventoryAdjustment.getFieldNamesStockC();
      fieldTypes        = inventoryAdjustment.getFieldTypesStockC();
    }

    if(tableName.equalsIgnoreCase("stockopen"))
    {
      InventoryAdjustment inventoryAdjustment = new InventoryAdjustment();
      numIndexes        = inventoryAdjustment.getIndexCreateStringsStockOpen(indexCreateStrings);
      tableCreateString = inventoryAdjustment.getTableCreateStringStockOpen();
      fieldNames        = inventoryAdjustment.getFieldNamesStockOpen();
      fieldTypes        = inventoryAdjustment.getFieldTypesStockOpen();
    }

    if(tableName.equalsIgnoreCase("stockcat"))
    {
      DefinitionTables definitionTables = new DefinitionTables();
      numIndexes        = definitionTables.getIndexCreateStringsStockCat(indexCreateStrings);
      tableCreateString = definitionTables.getTableCreateStringStockCat();
      fieldNames        = definitionTables.getFieldNamesStockCat();
      fieldTypes        = definitionTables.getFieldTypesStockCat();
    } 
     
    if(tableName.equalsIgnoreCase("stockx"))
    {
      Inventory inventory = new Inventory();
      numIndexes        = inventory.getIndexCreateStringsStockx(indexCreateStrings);
      tableCreateString = inventory.getTableCreateStringStockx();
      fieldNames        = inventory.getFieldNamesStockx();
      fieldTypes        = inventory.getFieldTypesStockx();
    } 

    if(tableName.equalsIgnoreCase("store"))
    {
      DocumentUtils documentUtils = new DocumentUtils();
      numIndexes        = documentUtils.getIndexCreateStringsStore(indexCreateStrings);
      tableCreateString = documentUtils.getTableCreateStringStore();
      fieldNames        = documentUtils.getFieldNamesStore();
      fieldTypes        = documentUtils.getFieldTypesStore();
    }
   
    if(tableName.equalsIgnoreCase("storeman"))
    {
      DocumentUtils documentUtils = new DocumentUtils();
      numIndexes        = documentUtils.getIndexCreateStringsStoreman(indexCreateStrings);
      tableCreateString = documentUtils.getTableCreateStringStoreman();
      fieldNames        = documentUtils.getFieldNamesStoreman();
      fieldTypes        = documentUtils.getFieldTypesStoreman();
    }

    if(tableName.equalsIgnoreCase("styling"))
    {
      DefinitionTables definitionTables = new DefinitionTables();
      numIndexes        = definitionTables.getIndexCreateStringsStyling(indexCreateStrings);
      tableCreateString = definitionTables.getTableCreateStringStyling();
      fieldNames        = definitionTables.getFieldNamesStyling();
      fieldTypes        = definitionTables.getFieldTypesStyling();
      dataBase = "sitewiki";
    } 

    if(tableName.equalsIgnoreCase("supplier"))
    {
      Supplier supplier = new Supplier();
      numIndexes        = supplier.getIndexCreateStrings(indexCreateStrings);
      tableCreateString = supplier.getTableCreateString();
      fieldNames        = supplier.getFieldNames();
      fieldTypes        = supplier.getFieldTypes();
    }
    
    if(tableName.equalsIgnoreCase("system"))
    {
      DefinitionTables definitionTables = new DefinitionTables();
      numIndexes        = definitionTables.getIndexCreateStringsSystem(indexCreateStrings);
      tableCreateString = definitionTables.getTableCreateStringSystem();
      fieldNames        = definitionTables.getFieldNamesSystem();
      fieldTypes        = definitionTables.getFieldTypesSystem();
    }

    if(tableName.equalsIgnoreCase("systemmodules"))
    {
      DefinitionTables definitionTables = new DefinitionTables();
      numIndexes        = definitionTables.getIndexCreateStringsSystemModules(indexCreateStrings);
      tableCreateString = definitionTables.getTableCreateStringSystemModules();
      fieldNames        = definitionTables.getFieldNamesSystemModules();
      fieldTypes        = definitionTables.getFieldTypesSystemModules();
    } 

    if(tableName.equalsIgnoreCase("systemsuspend"))
    {
      DefinitionTables definitionTables = new DefinitionTables();
      numIndexes        = definitionTables.getIndexCreateStringsSystemSuspend(indexCreateStrings);
      tableCreateString = definitionTables.getTableCreateStringSystemSuspend();
      fieldNames        = definitionTables.getFieldNamesSystemSuspend();
      fieldTypes        = definitionTables.getFieldTypesSystemSuspend();
    }

    if(tableName.equalsIgnoreCase("trail"))
    {
      Trail trail = new Trail();
      numIndexes        = trail.getIndexCreateStringsTrail(indexCreateStrings);
      tableCreateString = trail.getTableCreateStringTrail();
      fieldNames        = trail.getFieldNamesTrail();
      fieldTypes        = trail.getFieldTypesTrail();
    } 
    
    if(tableName.equalsIgnoreCase("usergroups"))
    {
      DefinitionTables definitionTables = new DefinitionTables();
      numIndexes        = definitionTables.getIndexCreateStringsUserGroups(indexCreateStrings);
      tableCreateString = definitionTables.getTableCreateStringUserGroups();
      fieldNames        = definitionTables.getFieldNamesUserGroups();
      fieldTypes        = definitionTables.getFieldTypesUserGroups();
    } 
    
    if(tableName.equalsIgnoreCase("usermodules"))
    {
      DefinitionTables definitionTables = new DefinitionTables();
      numIndexes        = definitionTables.getIndexCreateStringsUserModules(indexCreateStrings);
      tableCreateString = definitionTables.getTableCreateStringUserModules();
      fieldNames        = definitionTables.getFieldNamesUserModules();
      fieldTypes        = definitionTables.getFieldTypesUserModules();
    } 

    if(tableName.equalsIgnoreCase("users"))
    {
      DefinitionTables definitionTables = new DefinitionTables();
      numIndexes        = definitionTables.getIndexCreateStringsUsers(indexCreateStrings);
      tableCreateString = definitionTables.getTableCreateStringUsers();
      fieldNames        = definitionTables.getFieldNamesUsers();
      fieldTypes        = definitionTables.getFieldTypesUsers();
    } 
    
    if(tableName.equalsIgnoreCase("userservices"))
    {
      DefinitionTables definitionTables = new DefinitionTables();
      numIndexes        = definitionTables.getIndexCreateStringsUserServices(indexCreateStrings);
      tableCreateString = definitionTables.getTableCreateStringUserServices();
      fieldNames        = definitionTables.getFieldNamesUserServices();
      fieldTypes        = definitionTables.getFieldTypesUserServices();
    } 
    
    if(tableName.equalsIgnoreCase("voucher"))
    {
      PaymentVoucher paymentVoucher = new PaymentVoucher();
      numIndexes        = paymentVoucher.getIndexCreateStringsVoucher(indexCreateStrings);
      tableCreateString = paymentVoucher.getTableCreateStringVoucher();
      fieldNames        = paymentVoucher.getFieldNamesVoucher();
      fieldTypes        = paymentVoucher.getFieldTypesVoucher();
    } 
      
    if(tableName.equalsIgnoreCase("vouchera"))
    {
      PaymentVoucher paymentVoucher = new PaymentVoucher();
      numIndexes        = paymentVoucher.getIndexCreateStringsVoucherA(indexCreateStrings);
      tableCreateString = paymentVoucher.getTableCreateStringVoucherA();
      fieldNames        = paymentVoucher.getFieldNamesVoucherA();
      fieldTypes        = paymentVoucher.getFieldTypesVoucherA();
    } 

    if(tableName.equalsIgnoreCase("voucherl"))
    {
      PaymentVoucher paymentVoucher = new PaymentVoucher();
      numIndexes        = paymentVoucher.getIndexCreateStringsVoucherL(indexCreateStrings);
      tableCreateString = paymentVoucher.getTableCreateStringVoucherL();
      fieldNames        = paymentVoucher.getFieldNamesVoucherL();
      fieldTypes        = paymentVoucher.getFieldTypesVoucherL();
    }
     
    if(tableName.equalsIgnoreCase("voucherll"))
    {
      PaymentVoucher paymentVoucher = new PaymentVoucher();
      numIndexes        = paymentVoucher.getIndexCreateStringsVoucherLL(indexCreateStrings);
      tableCreateString = paymentVoucher.getTableCreateStringVoucherLL();
      fieldNames        = paymentVoucher.getFieldNamesVoucherLL();
      fieldTypes        = paymentVoucher.getFieldTypesVoucherLL();
    }
  
    if(tableName.equalsIgnoreCase("wo"))
    {
      WorksOrder worksOrder = new WorksOrder();
      numIndexes        = worksOrder.getIndexCreateStringsWO(indexCreateStrings);
      tableCreateString = worksOrder.getTableCreateStringWO();
      fieldNames        = worksOrder.getFieldNamesWO();
      fieldTypes        = worksOrder.getFieldTypesWO();
    } 
       
    if(tableName.equalsIgnoreCase("woa"))
    {
      WorksOrder worksOrder = new WorksOrder();
      numIndexes        = worksOrder.getIndexCreateStringsWOA(indexCreateStrings);
      tableCreateString = worksOrder.getTableCreateStringWOA();
      fieldNames        = worksOrder.getFieldNamesWOA();
      fieldTypes        = worksOrder.getFieldTypesWOA();
    } 

    if(tableName.equalsIgnoreCase("wol"))
    {
      WorksOrder worksOrder = new WorksOrder();
      numIndexes        = worksOrder.getIndexCreateStringsWOL(indexCreateStrings);
      tableCreateString = worksOrder.getTableCreateStringWOL();
      fieldNames        = worksOrder.getFieldNamesWOL();
      fieldTypes        = worksOrder.getFieldTypesWOL();
    }
       
    if(tableName.equalsIgnoreCase("woll"))
    {
      WorksOrder worksOrder = new WorksOrder();
      numIndexes        = worksOrder.getIndexCreateStringsWOLL(indexCreateStrings);
      tableCreateString = worksOrder.getTableCreateStringWOLL();
      fieldNames        = worksOrder.getFieldNamesWOLL();
      fieldTypes        = worksOrder.getFieldTypesWOLL();
    }

    switch(option)
    {
      case 'C' : if(adminUtils.createTable(dataBase, dropTable, tableName, tableCreateString, numIndexes, indexCreateStrings, dnm, localDefnsDir, defnsDir))
                 { 
                   rtn = true;
    
                   if(tableName.equalsIgnoreCase("appconfig"))
                   {
                     AdminUtilitiesConfig adminUtilitiesConfig = new AdminUtilitiesConfig();
                     adminUtilitiesConfig.primeAppConfig(con, stmt, dnm);
                   }
                   else
                   if(tableName.equalsIgnoreCase("channels"))
                   {
                     Chat chat = new Chat();
                     chat.primeChannels(con, stmt);
                   }
                   else
                   if(tableName.equalsIgnoreCase("codes"))
                   {
                     AdminCodesDBUtilities adminCodesDBUtilities = new AdminCodesDBUtilities();
                     adminCodesDBUtilities.primeCodes(dnm, localDefnsDir, defnsDir);
                   }
                   else
                   if(tableName.equalsIgnoreCase("country"))
                   {
                     AdminCountryDBUtilities adminCountryDBUtilities = new AdminCountryDBUtilities();
                     adminCountryDBUtilities.primeCountries(dnm, localDefnsDir, defnsDir);
                   }
                   else
                   if(tableName.equalsIgnoreCase("industrytype"))
                   {
                     AdminIndustryTypeDBUtilities adminIndustryTypeDBUtilities = new AdminIndustryTypeDBUtilities();
                     adminIndustryTypeDBUtilities.primeCountries(dnm, localDefnsDir, defnsDir);
                   }
                   else
                   if(tableName.equalsIgnoreCase("locks"))
                   {
                     AdminLockingUtilities adminLockingUtilities = new AdminLockingUtilities();
                     adminLockingUtilities.primeCodes(dnm, localDefnsDir, defnsDir);
                   }
                   else
                   if(tableName.equalsIgnoreCase("profiles"))
                   {
                     ContactsProfilesPrime contactsProfilesPrime = new ContactsProfilesPrime();
                     contactsProfilesPrime.primeProfiles(dnm, localDefnsDir, defnsDir);
                   }
                   else
                   if(tableName.equalsIgnoreCase("quoreasons"))
                   {
                     AdminQuotationReasonsPrime adminQuotationReasonsPrime = new AdminQuotationReasonsPrime();
                     adminQuotationReasonsPrime.primeQuotationReasons(dnm, localDefnsDir, defnsDir);
                   }
                   else
                   if(tableName.equalsIgnoreCase("styling"))
                   {
                     primeStyling(dnm);
                   }
                   else
                   if(tableName.equalsIgnoreCase("documentoptions"))
                   {
                     primeDocumentOptions(dnm);
                   }
                   else
                   if(tableName.equalsIgnoreCase("documentsettings"))
                   {
                     primeDocumentSettings(dnm);
                   }
                   else
                   if(tableName.equalsIgnoreCase("system"))
                   {
                     AdminSystemUtilities adminSystemUtilities = new AdminSystemUtilities();
                     adminSystemUtilities.primeSystem(dnm, localDefnsDir, defnsDir);
                   }
                 }
                 break;
      case 'I' : if(adminUtils.importFile(tableName, fieldNames, fieldTypes, exportDir, dnm, localDefnsDir, defnsDir))
                   rtn = true;
                  break;
      case 'E' : if(adminUtils.exportTable(tableName, fieldNames, fieldTypes, exportDir, dnm, localDefnsDir, defnsDir))
                   rtn = true;
                  break;
      case 'X' : if(adminUtils.exportTableToXML(tableName, fieldNames, fieldTypes, exportDir, dnm, localDefnsDir, defnsDir))
                   rtn = true;
                  break;
    }
  
    return rtn;
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void primeStyling(String dnm) throws Exception
  {
    String uName = directoryUtils.getMySQLUserName();
    String pWord = directoryUtils.getMySQLPassWord();
    
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_sitewiki?user=" + uName + "&password=" + pWord);
    Statement stmt = con.createStatement();
    String q;
   
    try
    {
      stmt = con.createStatement();
      q = "INSERT INTO styling ( HeaderLogo, HeaderLogoRepeat, UsesFlash, FooterText, PageHeaderImage1, PageHeaderImage2, PageHeaderImage3, PageHeaderImage4, PageHeaderImage5, Watermark )"
        + "VALUES ('', '', 'N', 'Copyright (c)', '', '', '', '', '', '')";
          
      stmt.executeUpdate(q);
    }
    catch(Exception e)
    {
      System.out.println(e);
    }
    
    if(stmt != null) stmt.close();
    if(con  != null) con.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void primeDocumentOptions(String dnm) throws Exception
  {
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = con.createStatement();
    String q, s = "", v = "";
   
    for(int x=1;x<=100;++x)
    {
      if(x > 1)
      {
        s += ",";
        v += ",";
      }
      s += "Option" + x;
      v += "'N'";
    }
    
    try
    {
      stmt = con.createStatement();
      q = "INSERT INTO documentoptions ( " + s + ") VALUES (" + v + ")";
          
      stmt.executeUpdate(q);
    }
    catch(Exception e)
    {
      System.out.println(e);
    }
    
    if(stmt != null) stmt.close();
    if(con  != null) con.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void primeDocumentSettings(String dnm) throws Exception
  {
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = con.createStatement();
    String q, s = "", v = "";
   
    for(int x=1;x<=50;++x)
    {
      if(x > 1)
      {
        s += ",";
        v += ",";
      }
      s += "Option" + x;
      v += "''";
    }
    
    try
    {
      stmt = con.createStatement();
      q = "INSERT INTO documentsettings ( " + s + ") VALUES (" + v + ")";
          
      stmt.executeUpdate(q);
    }
    catch(Exception e)
    {
      System.out.println(e);
    }
    
    if(stmt != null) stmt.close();
    if(con  != null) con.close();
  }

}
