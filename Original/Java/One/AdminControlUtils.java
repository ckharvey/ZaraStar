// =======================================================================================================================================================================================================
// System: ZaraStar Admin: Control
// Module: AdminControlUtils.java
// Author: C.K.Harvey
// Copyright (c) 2003-09 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import java.io.*;
import java.sql.*;

public class AdminControlUtils
{
  GeneralUtils generalUtils = new GeneralUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  Profile profile = new Profile();

  private String table =
          
      "7500,SubSystems,7519,Site;"
    + "7500,SubSystems,7501,Dashboard;"

    + "7500,SubSystems,7520,External User;"
    + "7500,SubSystems,7521,Catalogs;"
    + "7500,SubSystems,7518,OFSA;"
    + "7500,SubSystems,7540,Projects;"
    + "7500,SubSystems,7533,Utility;"
    + "7500,SubSystems,7512,Workshop;"
    + "7500,SubSystems,7534,NX;"
    + "7500,SubSystems,7535,Sun Ray;"

    + "7519,Site,8200,Show Dashboard as Home Page;"
    + "7519,Site,5912,Show Topics as Home Page;"
    + "7519,Site,8104,Show All Blogs as Home Page;"
    + "7519,Site,7528,Blogs;"
    + "7519,Site,7539,Forum;"
    + "7519,Site,6500,Image Library;"
    + "7519,Site,128,People;"
    + "7519,Site,188,About;"
   
    + "7539,Forum,5905,Create New Issue;"
    + "7539,Forum,5903,Edit Existing Issue;"

    + "7540,Projects,6800,View Projects;"
    + "7540,Projects,6801,Create New Project;"

    + "7528,Blogs,8114,Blogs Visible;"
    + "7528,Blogs,8102,Create Blog Entries;"
    + "7528,Blogs,8107,Create Blog Topics;"
    + "7528,Blogs,8108,Create Blog Guides;"
    + "7528,Blogs,8112,Create BlogGuide Contents;"
    + "7528,Blogs,8113,Create PDF BlogGuide;"

    + "7501,Dashboard,7522,Mail;"
    + "7501,Dashboard,7523,Document Library;"
    + "7501,Dashboard,7530,Forum;"
    + "7501,Dashboard,7531,Contacts;"
    + "7501,Dashboard,7532,Instant Messaging;"

    + "7531,Contacts,8801,Personal Contacts;"
    + "7531,Contacts,8819,Change External User Access Rights;"

    + "7523,Document Library,12000,Personal Library;"
    + "7523,Document Library,12014,View All Directories;"
    + "7523,Document Library,12009,Delete Directory;"
    + "7523,Document Library,12017,View Transaction History;"

    + "7520,External Users,7503,Casual Users;"
    + "7520,External Users,7537,Registered Users;"

    + "7503,Casual Users,809,Show People."
    + "7503,Casual Users,822,Show Blogs."
    + "7503,Casual Users,820,Allow Instant Messaging to People."
    + "7503,Casual Users,802,Access to Catalogs (Stock) and Availability."
    + "7503,Casual Users,808,Allow Cart."
    + "7503,Casual Users,806,Allow Registration."
    + "7503,Casual Users,824,Show Forum."
    + "7503,Casual Users,825,Allow Document Library Downloads."
    + "7503,Casual Users,5910,Show Topics as Home Page."

    + "7537,Registered Users,921,eMail Notifications."
    + "7537,Registered Users,902,Access to Catalogs (Stock) and Availability."
    + "7537,Registered Users,903,Access to Order and Fulfillment Transactions."
    + "7537,Registered Users,904,Access to Accounts."
    + "7537,Registered Users,907,Access to Projects."
    + "7537,Registered Users,908,Allow Cart."
    + "7537,Registered Users,924,Show Forum."
    + "7537,Registered Users,909,Show People."
    + "7537,Registered Users,906,Allow Changes to Profile."
    + "7537,Registered Users,922,Show Blogs."
    + "7537,Registered Users,920,Allow Instant Messaging to People."
    + "7537,Registered Users,923,Create New Issue."
    + "7537,Registered Users,5911,Show Topics as Home Page."
    + "7537,Registered Users,901,Show Menu as Home Page."
    + "7537,Registered Users,925,Allow Document Library Downloads."

    + "7521,Catalogs,0000,xxxxxxx;"

    + "7522,Mail,8014,WebMail;"
    + "7522,Mail,8022,Manage Own Accounts;"
    + "7522,Mail,8013,Remote Mail Sending;"

    + "7518,OFSA,7507,Orders;"
    + "7518,OFSA,7509,Fulfillment;"
    + "7518,OFSA,7510,Settlement;"
    + "7518,OFSA,7511,Accounts;"

    + "7533,Utility,7505,Fax;"
    + "7533,Utility,7541,Mail Send;"

    + "7505,Fax,11000,Faxes;"

    + "7541,Mail Send,8016,Send Mails with Links;"

    + "7516,Fulfillment (Inventory),3001,Stock Records,3003,Change;"
    + "7516,Fulfillment (Inventory),166,Stock Check Records,3019,Change;"
    + "7516,Fulfillment (Inventory),3065,Stock Reconciliation;"
    + "7516,Fulfillment (Inventory),3081,Cycle Counting (Setup);"
    + "7516,Fulfillment (Inventory),3083,Cycle Counting (Perform);"
    + "7516,Fulfillment (Inventory),167,Stock Adjustment Records,3012,Change;"
    + "7516,Fulfillment (Inventory),3053,Stock Prices Update;"
    + "7516,Fulfillment (Inventory),3061,List Stock Prices Out-of-Date;"
    + "7516,Fulfillment (Inventory),3057,Stock File Download;"
    + "7516,Fulfillment (Inventory),3058,Stock File Upload Update;"
    + "7516,Fulfillment (Inventory),3023,Renumber Stock Record;"
    + "7516,Fulfillment (Inventory),3009,Replace Stock Records;"
    + "7516,Fulfillment (Inventory),3022,Add Stock Records by Upload;"
    + "7516,Fulfillment (Inventory),3016,Remove Stock Records;"
    + "7516,Fulfillment (Inventory),3071,Move from Store to Store;"

    + "7538,User-Level Options,3049,Send Picking List to Queue;"
    + "7538,User-Level Options,3051,Print Picking Lists from Queue;"

    + "7509,Fulfillment,7514,Analytics;"
    + "7509,Fulfillment,7516,Inventory;"
    + "7509,Fulfillment,7524,Documents;"
    + "7509,Fulfillment,7538,User-Level Options;"

    + "7512,Workshop,169,Works Orders,4432,Create,4435,Change,4439,Cancel/Delete,4440,Print;"

    + "7517,Orders (Documents),142,Quotations,4020,Create,4023,Change,4027,Cancel/Delete,4028,Print;"
    + "7517,Orders (Documents),140,Sales Orders,4032,Create,4035,Change,4037,Cancel/Delete,4040,Print;"
    + "7517,Orders (Documents),146,Sales Order Confirmations,4044,Create,4046,Change,4050,Cancel/Delete,4051,Print;"
    + "7517,Orders (Documents),170,Sales Order Acknowledgements,4131,Create,4133,Change,4137,Cancel/Delete,4138,Print;"
    + "7517,Orders (Documents),139,Purchase Orders,5007,Create,5008,Change,5012,Cancel/Delete,5013,Print;"
    + "7517,Orders (Documents),152,Local Requisitions,5017,Create,5018,Change,5022,Cancel/Delete,5023,Print;"

    + "7507,Orders,7513,Analytics;"
    + "7507,Orders,7517,Documents;"
    + "7507,Orders,7536,Companies;"
    + "7507,Orders,7900,Tasks;"
    + "7507,Orders,7542,User-Level Options;"

    + "7542,User-Level Options,7543,Display Purchase Pricing;"

    + "7536,Companies,4001,Customers,4003,Change;"
    + "7536,Companies,5001,Suppliers,5003,Change;"
    + "7536,Companies,4005,Customer Listing Report;"
    + "7536,Companies,5005,Supplier Listing Report;"

    + "7524,Fulfillment (Documents),147,Picking Lists,3039,Create,3043,Change,3047,Cancel/Delete,3048,Print;"
    + "7524,Fulfillment (Documents),148,Delivery Orders,4055,Create,4059,Change,4063,Cancel/Delete,4064,Print;"
    + "7524,Fulfillment (Documents),150,Proforma Invoices,4081,Create,4084,Change,4088,Cancel/Delete,4089,Print;"
    + "7524,Fulfillment (Documents),149,Sales Invoices,4224,Create,4072,Change,4076,Cancel/Delete,4077,Print;"
    + "7524,Fulfillment (Documents),153,Goods Received Notes,3026,Create,3027,Change,3031,Cancel/Delete;"
    + "7524,Fulfillment (Documents),154,Purchase Invoices,5081,Create,5082,Change,5086,Cancel/Delete;"

    + "7525,Settlement (Documents),155,Sales Credit Notes,4102,Create,4103,Change,4107,Cancel/Delete,4108,Print;"
    + "7525,Settlement (Documents),156,Sales Debit Notes,4112,Create,4113,Change,4117,Cancel/Delete,4118,Print;"
    + "7525,Settlement (Documents),162,Purchase Credit Notes,5027,Create,5028,Change,5032,Cancel/Delete;"
    + "7525,Settlement (Documents),163,Purchase Debit Notes,5037,Create,5038,Change,5042,Cancel/Delete;"
    + "7525,Settlement (Documents),157,Receipts,4206,Create,4207,Change,4211,Cancel/Delete,4212,Print;"
    + "7525,Settlement (Documents),164,Payments,5050,Create,5051,Change,5055,Cancel/Delete,5056,Print;"

    + "7510,Settlement,7515,Analytics;"
    + "7510,Settlement,7525,Documents;"

    + "7511,Accounts,7526,Procedures;"
    + "7511,Accounts,7527,Documents;"

    + "7527,Accounts (Documents),160,Payment Vouchers,6067,Create,6068,Change,6072,Cancel/Delete,6073,Print;"
    + "7527,Accounts (Documents),159,Receipt Vouchers,6057,Create,6058,Change,6062,Cancel/Delete;"
    + "7527,Accounts (Documents),161,InterAccount Transfers,6078,Create,6079,Change;"

    + "7526,Accounts (Procedures),109,General Access;"
    + "7526,Accounts (Procedures),7054,Currency and Rates;"
    + "7526,Accounts (Procedures),6002,Bank Reconciliation;"
    + "7526,Accounts (Procedures),6003,GST Reconciliation;"
    + "7526,Accounts (Procedures),108,Self-Audit Validation;"
    + "7526,Accounts (Procedures),6051,Year Closing and Opening;"
    + "7526,Accounts (Procedures),7033,Document Locking;"
    + "7526,Accounts (Procedures),3067,Stock Opening Positions and Valuations;"
    + "7526,Accounts (Procedures),6016,Chart of Accounts;"
    + "7526,Accounts (Procedures),6028,General Ledger;"
    + "7526,Accounts (Procedures),6029,Debtors Ledger;"
    + "7526,Accounts (Procedures),6030,Creditors Ledger;"
    + "7526,Accounts (Procedures),6005,Stock Ledger;"
    + "7526,Accounts (Procedures),6053,Trial Balance;"
    + "7526,Accounts (Procedures),6054,Balance Sheet;"
    + "7526,Accounts (Procedures),6055,Profit and Loss Statement;"
    + "7526,Accounts (Procedures),1101,Chart: Profit and Loss;"
    + "7526,Accounts (Procedures),1102,Chart: Balance Sheet;"
    + "7526,Accounts (Procedures),6032,Verification;"
    + "7526,Accounts (Procedures),6006,Weighted-Average Cost Derivation;"

    + "108,Self-Audit Validation,1202,Sales: Closure Analysis;"
    + "108,Self-Audit Validation,1203,Purchases: Closure Analysis;"
    + "108,Self-Audit Validation,6033,Purchases Reconciliation Analysis;"
    + "108,Self-Audit Validation,3070,Invoice Cost-of-Sale Verification;"
    + "108,Self-Audit Validation,3076,Invoice Picking List Verification;"
    + "108,Self-Audit Validation,6036,Work-in-Progress Analysis;"
    + "108,Self-Audit Validation,6037,Stock-in-Transit Analysis;"
    + "108,Self-Audit Validation,3062,Stock Status Analysis;"
    + "108,Self-Audit Validation,3065,Stock Check Reconciliation;"
    + "108,Self-Audit Validation,3066,Stock Check Valuation;"
    + "108,Self-Audit Validation,6034,Debtor Opening Balance Verification;"
    + "108,Self-Audit Validation,6035,Creditor Opening Balance Verification;"
    + "108,Self-Audit Validation,6038,Debtor and Creditor Analysis;"
    + "108,Self-Audit Validation,3072,Sales Invoice Receivables Verification;"
    + "108,Self-Audit Validation,3073,Purchase Invoice Receivables Verification;"
    + "108,Self-Audit Validation,7002,List Stock Weighted Average Costs;"

    + "7515,Settlement (Analytics),4203,Accounts Receivable;"
    + "7515,Settlement (Analytics),5047,Accounts Payable;"
    + "7515,Settlement (Analytics),1004,Debtors Ageing Analysis;"
    + "7515,Settlement (Analytics),1013,Creditors Ageing Analysis;"
    + "7515,Settlement (Analytics),1012,Statements of Account;"
    + "7515,Settlement (Analytics),4002,View Settlement History (Debtors);"
    + "7515,Settlement (Analytics),5002,View Settlement History (Creditors);"
    + "7515,Settlement (Analytics),1029,Consolidated Debtors;"
    + "7515,Settlement (Analytics),1031,Consolidated Creditors;"
    + "7515,Settlement (Analytics),6900,View Document Consolidations;"

    + "7513,Orders (Analytics),1201,Sales: Book Orders;"
    + "7513,Orders (Analytics),1202,Sales: Closure Analysis;"
    + "7513,Orders (Analytics),1204,Stock Sales: Gross Margin;"
    + "7513,Orders (Analytics),2024,Quotation Track and Trace;"
    + "7513,Orders (Analytics),2021,Sales Order Track and Trace;"
    + "7513,Orders (Analytics),2022,Purchase Order Track and Trace;"
    + "7513,Orders (Analytics),1200,Sales Intake;"
    + "7513,Orders (Analytics),1100,Chart: Sales Orders Past Due;"
    + "7513,Orders (Analytics),1008,Word Search Document Lines;"
    + "7513,Orders (Analytics),1030,Document Lines Enquiry;"
    + "7513,Orders (Analytics),1025,Document Trace (Cyclical);"
    + "7513,Orders (Analytics),1026,Document Trace (Financial);"
    + "7513,Orders (Analytics),2039,Sales Control: Contract Review Team;"
    + "7513,Orders (Analytics),2027,Sales Control: Sales;"
    + "7513,Orders (Analytics),2042,Sales Control: Sales - Trades;"
    + "7513,Orders (Analytics),2030,Sales Control: Sales Manager;"
    + "7513,Orders (Analytics),2031,Sales Control: Engineering;"
    + "7513,Orders (Analytics),2032,Sales Control: Purchasing;"
    + "7513,Orders (Analytics),2033,Sales Control: Scheduling;"

    + "7514,Fulfillment (Analytics),2025,Picking List Track and Trace;"
    + "7514,Fulfillment (Analytics),2026,Delivery Order Track and Trace;"
    + "7514,Fulfillment (Analytics),2028,Sales Invoice Track and Trace;"
    + "7514,Fulfillment (Analytics),2041,Goods Received Notes Track and Trace;"
    + "7514,Fulfillment (Analytics),2040,Purchase Invoice Track and Trace;"
    + "7514,Fulfillment (Analytics),1023,Sales Invoice Enquiry by SalesPerson;"
    + "7514,Fulfillment (Analytics),1033,Sales Invoice Enquiry by Date;"
    + "7514,Fulfillment (Analytics),1027,Sales Invoice Enquiry for GST;"
    + "7514,Fulfillment (Analytics),1032,Document Lines Enquiry;"
    + "7514,Fulfillment (Analytics),1034,Purchase Invoice Lines Enquiry by Date;"
    + "7514,Fulfillment (Analytics),1028,Word Search Document Lines;"
    + "7514,Fulfillment (Analytics),1025,Document Trace (Cyclical);"
    + "7514,Fulfillment (Analytics),1026,Document Trace (Financial);"
    + "7514,Fulfillment (Analytics),2034,Fulfillment Control: Coordinator;"
    + "7514,Fulfillment (Analytics),2037,Fulfillment Control: Sales;"
    + "7514,Fulfillment (Analytics),2042,Chart: Inventory Sales and Purchases;"
    + "7514,Fulfillment (Analytics),1103,Chart: Sales Invoice Turnover;"
    + "7514,Fulfillment (Analytics),1014,Stock Usage Enquiry for Customer;"
    + "7514,Fulfillment (Analytics),1017,Stock Usgae Enquiry for Supplier;"
    + "7514,Fulfillment (Analytics),1021,Stock ReOrder;"
    + "7514,Fulfillment (Analytics),3062,Stock Status Report;"
    + "7514,Fulfillment (Analytics),1022,Manufacturer Sales by Customer;"
    + "7514,Fulfillment (Analytics),1035,Manufacturer Purchases from PO Lines;"
    + "7514,Fulfillment (Analytics),1038,Manufacturer Sales from Invoice Lines;"
    + "7514,Fulfillment (Analytics),1036,Supplier Purchases from PO Lines;"
    + "7514,Fulfillment (Analytics),3014,Stock Usage Spread for a Manufacturer;"
    + "7514,Fulfillment (Analytics),3064,List Incomplete Picking Lists and GRNs;"
    + "7514,Fulfillment (Analytics),1037,Purchase Order Delivery Performance;"
    + "7514,Fulfillment (Analytics),1001,Stock Enquiry;"
    + "7514,Fulfillment (Analytics),1002,Stock History Enquiry;"
    + "7514,Fulfillment (Analytics),3052,Stock Trace;"
    + "7514,Fulfillment (Analytics),2043,Delivery Orders: Update Returns;"
    + "7514,Fulfillment (Analytics),1019,Delivery Orders Returned for a Date;"
    + "7514,Fulfillment (Analytics),1020,Delivery Orders Not Invoiced;";

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean notDisabled(Connection con, Statement stmt, ResultSet rs, int service) throws Exception
  {
    String reqdSvc = generalUtils.intToStr(service);
    String svc, svc2;

    int tableLen = table.length();

    int x = 0;
    while(x < tableLen)
    {
      svc = "";
      while(table.charAt(x) != ',')
        svc += table.charAt(x++);
      ++x;
      while(table.charAt(x) != ',')
        ++x;

      ++x;
      svc2 = "";
      while(table.charAt(x) != ',')
        svc2 += table.charAt(x++);
      ++x;
      while(table.charAt(x) != ',' && table.charAt(x) != ';' && table.charAt(x) != '.')
        ++x;

      if(svc2.equals(reqdSvc))
      {
        if(isSuspended(con, stmt, rs, svc2))
          return false;

        if(isSuspended(con, stmt, rs, svc))
          return false;

        if(isAParentSuspended(con, stmt, rs, reqdSvc, tableLen))
          return false;

        return true;
      }

      while(table.charAt(x) != ';' && table.charAt(x) != '.')
        ++x;

      ++x;
    }

    return true; // none found 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean isAParentSuspended(Connection con, Statement stmt, ResultSet rs, String reqdSvc, int tableLen) throws Exception
  {
    String svc, svc2;

    int x = 0;
    while(x < tableLen)
    {
      svc = "";
      while(table.charAt(x) != ',')
        svc += table.charAt(x++);
      ++x;
      while(table.charAt(x) != ',')
        ++x;

      ++x;
      svc2 = "";
      while(table.charAt(x) != ',')
        svc2 += table.charAt(x++);
      ++x;
      while(table.charAt(x) != ',' && table.charAt(x) != ';' && table.charAt(x) != '.')
        ++x;

      if(svc2.equals(reqdSvc))
      {
        if(isSuspended(con, stmt, rs, svc2))
          return true;

        if(isSuspended(con, stmt, rs, svc))
          return true;

        if(isAParentSuspended(con, stmt, rs, svc, tableLen))
          return true;
      }

      while(table.charAt(x) != ';' && table.charAt(x) != '.')
        ++x;

      ++x;
    }

    return false; // none found
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean anyFurtherLevels(String service, int tableLen) throws Exception
  {
    String svc;
    int x = 0;
    while(x < tableLen)
    {
      svc = "";
      while(table.charAt(x) != ',')
        svc += table.charAt(x++);
      ++x;

      if(svc.equals(service))
        return true;

      while(table.charAt(x) != ';' && table.charAt(x) != '.')
        ++x;
      ++x;
    }

    return false;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean anyOfACategory(Connection con, Statement stmt, ResultSet rs, int service, String unm, String uty, String dnm) throws Exception
  {
    String reqdSvc = generalUtils.intToStr(service);
    String svc, svc2;
    boolean atLeastOneTrue = false;

    int tableLen = table.length();

    int x = 0;
    while(x < tableLen)
    {
      svc = "";
      while(table.charAt(x) != ',')
        svc += table.charAt(x++);
      ++x;
      while(table.charAt(x) != ',')
        ++x;

      ++x;
      svc2 = "";
      while(table.charAt(x) != ',')
        svc2 += table.charAt(x++);
      ++x;
      while(table.charAt(x) != ',' && table.charAt(x) != ';' && table.charAt(x) != '.')
        ++x;

      if(svc.equals(reqdSvc))
      {
        if(profile.verifyAccess(con, stmt, rs, unm, uty, dnm, generalUtils.strToInt(svc2)))
          atLeastOneTrue = true;
      }

      while(table.charAt(x) != ';' && table.charAt(x) != '.')
        ++x;

      ++x;
    }

    return atLeastOneTrue;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void createScreen(PrintWriter out, String reqdSvc, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    Connection con = null;
    Statement stmt = null, stmt2 = null;
    ResultSet rs   = null, rs2   = null;

    int tableLen = table.length();

    try
    {
      String uName = directoryUtils.getMySQLUserName();
      String pWord = directoryUtils.getMySQLPassWord();
      Class.forName("com.mysql.jdbc.Driver").newInstance();
      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + uName + "&password=" + pWord);

      String svc, svc2, name, name2, allServices;
      boolean first = true;

      int x = 0;
      while(x < tableLen)
      {
        svc = "";
        while(table.charAt(x) != ',')
          svc += table.charAt(x++);
        ++x;
        name = "";
        while(table.charAt(x) != ',')
          name += table.charAt(x++);

        ++x;
        svc2 = "";
        while(table.charAt(x) != ',')
          svc2 += table.charAt(x++);
        ++x;
        name2 = "";
        while(table.charAt(x) != ',' && table.charAt(x) != ';' && table.charAt(x) != '.')
          name2 += table.charAt(x++);

        if(table.charAt(x) == ',') // more services
        {
          ++x;
          allServices = "";
          while(table.charAt(x) != ';')
            allServices += table.charAt(x++);
        }
        else allServices = "";

        if(svc.equals(reqdSvc))
        {
          if(first)
          {
            drawTitle(out, "Access: " + name, reqdSvc, bytesOut);

            scoutln(out, bytesOut, "<table id='page' border=0 width=100%>");
            first = false;
          }

          if(table.charAt(x) == '.') // more services
            aModule(con, stmt, rs, out, svc2, name2, bytesOut);
          else
          if(anyFurtherLevels(svc2, tableLen))
            aLevel(con, stmt, rs, out, svc2, name2, bytesOut);
          else aService(con, stmt, stmt2, rs, rs2, out, svc2, name2, allServices, bytesOut);
        }

        ++x;
      }

      scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
      scoutln(out, bytesOut, "</table>");
    }
    catch(Exception e)
    {
      System.out.println(e);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    if(con != null) con.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void aLevel(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String service, String desc, int[] bytesOut) throws Exception
  {
    String status, action;

    if(isSuspended(con, stmt, rs, service)) { status = "Disabled"; action = "Enable"; } else { status = "Live"; action = "Disable"; }

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap><p><a href=\"javascript:help('" + service + "')\">" + service + "</a>: " + desc + " &nbsp;&nbsp;&nbsp;(Status is: " + status + "... <a href=\"javascript:disable('" + service + "')\">" + action
                         + "</a>)</td></tr>");
    scoutln(out, bytesOut, "<tr id=\"pageColumn\"><td></td>");

    scoutln(out, bytesOut, "<td width=99% nowrap><p><a href=\"javascript:again('" + service + "')\">Further Options</a></td></tr>");

    list(con, stmt, null, rs, null, out, service, desc, "", bytesOut);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void aService(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, String service, String desc, String allServices, int[] bytesOut) throws Exception
  {
    String status, action;

    if(isSuspended(con, stmt, rs, service)) { status = "Disabled"; action = "Enable"; } else { status = "Live"; action = "Disable"; }

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap><p><a href=\"javascript:help('" + service + "')\">" + service + "</a>: " + desc + " &nbsp;&nbsp;&nbsp;(Status is: " + status + "... <a href=\"javascript:disable('" + service + "')\">" + action
                         + "</a>)</td></tr>");
    scoutln(out, bytesOut, "<tr id=\"pageColumn\"><td></td>");
    scoutln(out, bytesOut, "<td width=80% nowrap colspan=2><p>User Code</td>");
    scoutln(out, bytesOut, "<td nowrap align=right><p><a href=\"javascript:modify('" + service + "','Enable " + desc + "')\">Modify</a></td></tr>");

    list(con, stmt, stmt2, rs, rs2, out, service, desc, allServices, bytesOut);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void aModule(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String service, String desc, int[] bytesOut) throws Exception
  {
    String status, action;

    if(isSuspended(con, stmt, rs, service)) { status = "Disabled"; action = "Enable"; } else { status = "Live"; action = "Disable"; }

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap><p><a href=\"javascript:help('" + service + "')\">" + service + "</a>: " + desc + " &nbsp;&nbsp;&nbsp;(Status is: " + status + "... <a href=\"javascript:disable('" + service + "')\">" + action
                         + "</a>)</td></tr>");
    scoutln(out, bytesOut, "<tr id=\"pageColumn\"><td></td>");
    scoutln(out, bytesOut, "<td width=80% nowrap colspan=2></td></tr>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean isSuspended(Connection con, Statement stmt, ResultSet rs, String service)
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

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void list(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, String service, String desc, String allServices, int[] bytesOut) throws Exception
  {
    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT UserCode FROM userservices WHERE Service = '" + service + "' ORDER BY UserCode");

      String userCode, cssFormat = "", subServices;

      while(rs.next())
      {
        userCode = rs.getString(1);

        if(userCode.equals("Sysadmin") || userCode.startsWith("___"))
          ;
        else
        {
          if(cssFormat.equals("line2")) cssFormat = "line1"; else cssFormat = "line2";

          scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\"><td></td>");

          if(allServices.length() > 0)
          {
            subServices = getSubServicesForThisUser(con, stmt2, rs2, allServices, userCode);
            scoutln(out, bytesOut, "<td><p>" + userCode + "</td><td width=80% nowrap><p>View" + subServices + "</td>");
            scoutln(out, bytesOut, "<td nowrap align=right><p><a href=\"javascript:refine('" + allServices + "','" + desc + "','" + userCode + "')\">Refine</a></td>");
          }
          else scoutln(out, bytesOut, "<td colspan=2><p>" + userCode + "</td>");

          scoutln(out, bytesOut, "</tr>");
        }
      }

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String getSubServicesForThisUser(Connection con, Statement stmt, ResultSet rs, String allServices, String userCode) throws Exception
  {
    int x = 0, len = allServices.length();
    String s = "", service, op;

    while(x < len)
    {
      service = "";
      while(x < len && allServices.charAt(x) != ',')
        service += allServices.charAt(x++);
      ++x;

      op = "";
      while(x < len && allServices.charAt(x) != ',')
        op += allServices.charAt(x++);
      ++x;

      if(alreadyHasAccess(con, stmt, rs, service, userCode))
        s += (", " + op);
    }

    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean alreadyHasAccess(Connection con, Statement stmt, ResultSet rs, String service, String userCode) throws Exception
  {
    int rowcount = 0;

    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM userservices WHERE Service = '" + service + "' AND UserCode = '" + userCode + "'");

      if(rs.next())
        rowcount = rs.getInt("rowcount");

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }

    if(rowcount == 0)
      return false;
    return true;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void drawTitle(PrintWriter out, String title, String service, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<table id='title' width=100%>");
    scoutln(out, bytesOut, "<tr><td colspan=2 nowrap><p>" + title + directoryUtils.buildHelp(service) + "</td></tr></table>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {
    out.println(str);
    bytesOut[0] += (str.length() + 2);
  }

}
