// =======================================================================================================================================================================================================
// System: ZaraStar AdminEngine: Codes DB Utilities
// Module: AdminCodesDBUtilities.java
// Author: C.K.Harvey
// Copyright (c) 1998-2006 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
//
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import java.sql.*;

public class AdminCodesDBUtilities
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

      primeADocument(con, stmt, "Sales Order",                "so",           "?????/SO");
      primeADocument(con, stmt, "Works Order",                "wo",           "?????/WO");
      primeADocument(con, stmt, "Receipt Record",             "receipt",      "?????/R");
      primeADocument(con, stmt, "Stock Adjustment",           "stocka",       "?????/SA");
      primeADocument(con, stmt, "Quotation",                  "quote",        "?????/Q");
      primeADocument(con, stmt, "Goods Received Note",        "goodsr",       "?????/GRN");
      primeADocument(con, stmt, "Purchase Order",             "po",           "?????/PO");
      primeADocument(con, stmt, "Delivery Order",             "do",           "?????/DO");
      primeADocument(con, stmt, "Payment Record",             "payment",      "?????/P");
      primeADocument(con, stmt, "Invoice",                    "invoice",      "?????/I");
      primeADocument(con, stmt, "Cash Invoice",               "cashinvoice",  "?????/CI");
      primeADocument(con, stmt, "Credit Note",                "cn",           "?????/CN");
      primeADocument(con, stmt, "Journal Batch",              "journalbatch", "?????/JB");
      primeADocument(con, stmt, "Purchase Invoice",           "pinvoice",     "?????/PI");
      primeADocument(con, stmt, "Purchase Credit Note",       "pcn",          "?????/PCN");
      primeADocument(con, stmt, "Picking List",               "pl",           "?????/PL");
      primeADocument(con, stmt, "Voucher",                    "voucher",      "?????/V");
      primeADocument(con, stmt, "Receipt Voucher",            "rvoucher",     "?????/RV");
      primeADocument(con, stmt, "Order Confirmation",         "oc",           "?????/OC");
      primeADocument(con, stmt, "Purchase Debit Note",        "pdn",          "?????/PDN");
      primeADocument(con, stmt, "Debit Note",                 "dn",           "?????/DN");
      primeADocument(con, stmt, "Stock",                      "stock",        "?????");
      primeADocument(con, stmt, "Proforma Invoice",           "proforma",     "?????/PRO");
      primeADocument(con, stmt, "Inbox Record",               "inbox",        "?????/IN");
      primeADocument(con, stmt, "Local Purchase Requisition", "lp",           "?????/LP");
      primeADocument(con, stmt, "Inter-Account Transfer",     "iat",          "?????/IAT");
      primeADocument(con, stmt, "Customer",                   "company",      "?????");
      primeADocument(con, stmt, "Supplier",                   "supplier",     "?????");
      primeADocument(con, stmt, "Stock Check",                "stockc",       "?????/SC");
      primeADocument(con, stmt, "Enquiry",                    "enquiry",      "?????/E");

      if(con  != null) con.close();
    }
    catch(Exception e)
    {
      System.out.println("AdminCodesDBUtilities: " + e);
      try
      {
        if(stmt != null) stmt.close();
        if(con  != null) con.close();
      }
      catch(Exception e2) { }
    }
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------
  private void primeADocument(Connection con, Statement stmt, String name, String shortName, String format) throws Exception
  {
    stmt = con.createStatement();
    stmt.executeUpdate("INSERT INTO codes ( Name, ShortName, NextCode, Format ) VALUES ('" + name + "','" + shortName + "','1','"
                       + format + "')");
    if(stmt != null) stmt.close();
  }
  

}
