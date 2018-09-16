// =======================================================================================================================================================================================================
// System: ZaraStar Admin: Locking Utilities
// Module: AdminLockingUtilities.java
// Author: C.K.Harvey
// Copyright (c) 1998-2008 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
//
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import java.sql.*;

public class AdminLockingUtilities
{
  GeneralUtils generalUtils = new GeneralUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();

  // -------------------------------------------------------------------------------------------------------------------------------
  public void primeCodes(String dnm, String localDefnsDir, String defnsDir)
  {
    Connection con = null;
    Statement stmt = null;
    
    try
    {
      String uName    = directoryUtils.getMySQLUserName();
      String passWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();
    
      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + uName + "&password=" + passWord);

      primeADocument(con, stmt, "Sales Orders",                "so");
      primeADocument(con, stmt, "Works Orders",                "wo");
      primeADocument(con, stmt, "Receipts",                    "receipt");
      primeADocument(con, stmt, "Stock Adjustments",           "stocka");
      primeADocument(con, stmt, "Quotations",                  "quote");
      primeADocument(con, stmt, "Goods Received Notes",        "gr");
      primeADocument(con, stmt, "Purchase Orders",             "po");
      primeADocument(con, stmt, "Delivery Orders",             "do");
      primeADocument(con, stmt, "Payments",                    "payment");
      primeADocument(con, stmt, "Sales Invoices",              "invoice");
      primeADocument(con, stmt, "Sales Credit Notes",          "credit");
      primeADocument(con, stmt, "Journal Batches",             "journalbatch");
      primeADocument(con, stmt, "Purchase Invoices",           "pinvoice");
      primeADocument(con, stmt, "Purchase Credit Notes",       "pcredit");
      primeADocument(con, stmt, "Picking Lists",               "pl");
      primeADocument(con, stmt, "Payment Vouchers",            "voucher");
      primeADocument(con, stmt, "Receipt Vouchers",            "rvoucher");
      primeADocument(con, stmt, "Order Confirmations",         "oc");
      primeADocument(con, stmt, "Purchase Debit Notes",        "pdebit");
      primeADocument(con, stmt, "Sales Debit Notes",           "debit");
      primeADocument(con, stmt, "Proforma Invoices",           "proforma");
      primeADocument(con, stmt, "Inbox Records",               "inbox");
      primeADocument(con, stmt, "Local Purchase Requisitions", "lp");
      primeADocument(con, stmt, "Inter-Account Transfers",     "iat");
      primeADocument(con, stmt, "Stock Check Records",         "stockc");
      primeADocument(con, stmt, "Stock Value Records",         "stockopen");
      primeADocument(con, stmt, "Enquiries",                   "enquiry");

      if(con  != null) con.close();
    }
    catch(Exception e)
    {
      System.out.println("AdminLockingUtilities: " + e);
      try
      {
        if(stmt != null) stmt.close();
        if(con  != null) con.close();
      }
      catch(Exception e2) { }
    }
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void primeADocument(Connection con, Statement stmt, String docName, String docAbbrev) throws Exception
  {
    stmt = con.createStatement();
    stmt.executeUpdate("INSERT INTO locks ( DocumentName, DocumentAbbrev, LockedUpto, OpenTo ) VALUES ('" + docName + "','" + docAbbrev
                     + "', {d '1970-01-01'}, '')");
    if(stmt != null) stmt.close();
  }
 
}
