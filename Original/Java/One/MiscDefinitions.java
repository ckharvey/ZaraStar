// =======================================================================================================================================================================================================
// System: Zara: Definitions: Misc defns
// Module: MiscDefinitions.java
// Author: C.K.Harvey
// Copyright (c) 1999-2008 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import java.sql.*;

public class MiscDefinitions
{
  GeneralUtils generalUtils = new GeneralUtils();

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean includeRemark(Connection con, Statement stmt, ResultSet rs)
  {
    return getDocumentOption(con, stmt, rs, "10");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean soRemarkToPLInstruction(Connection con, Statement stmt, ResultSet rs)
  {
    return getDocumentOption(con, stmt, rs, "9");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  public char dpOnQuantities(Connection con, Statement stmt, ResultSet rs, String option)
  {
    String s = getDocumentSetting(con, stmt, rs, "5");
    if(s.length() == 0)
      return '0';
    return s.charAt(0);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  public char dpOnUnitPrice(Connection con, Statement stmt, ResultSet rs, String option)
  {
    String s = getDocumentSetting(con, stmt, rs, "4");
    if(s.length() == 0)
      return '2';
    return s.charAt(0);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  public int docSizeMax(Connection con, Statement stmt, ResultSet rs, String document)
  {
    String option = "";
    if(document.equals("enquiry"))  option = "6";  else
    if(document.equals("quote"))    option = "7";  else
    if(document.equals("quote1"))   option = "8";  else
    if(document.equals("quote2"))   option = "9";  else
    if(document.equals("quote3"))   option = "10";  else
    if(document.equals("so"))       option = "11";  else
    if(document.equals("oa"))       option = "12";  else
    if(document.equals("oc"))       option = "13"; else
    if(document.equals("pl"))       option = "14"; else
    if(document.equals("pack"))     option = "15"; else
    if(document.equals("do"))       option = "16"; else
    if(document.equals("invoice"))  option = "17"; else
    if(document.equals("credit"))   option = "18"; else
    if(document.equals("debit"))    option = "19"; else
    if(document.equals("proforma")) option = "20"; else
    if(document.equals("wo"))       option = "21"; else
    if(document.equals("po"))       option = "22"; else
    if(document.equals("lp"))       option = "23"; else
    if(document.equals("gr"))       option = "24"; else
    if(document.equals("san"))      option = "25"; else
    if(document.equals("receipt"))  option = "26"; else
    if(document.equals("payment"))  option = "27"; else
    if(document.equals("voucher"))  option = "28"; else
    if(document.equals("soa"))      option = "29";
    
    return generalUtils.intFromStr(getDocumentSetting(con, stmt, rs, option));
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean showDuplicateDescriptions(Connection con, Statement stmt, ResultSet rs)
  {
    return getDocumentOption(con, stmt, rs, "8");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean hideDuplicateEntries(Connection con, Statement stmt, ResultSet rs)
  {
    return getDocumentOption(con, stmt, rs, "7");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean showCustItemCodeAfterDescription(Connection con, Statement stmt, ResultSet rs)
  {
    return getDocumentOption(con, stmt, rs, "6");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean drawHorizontalLineOnPickingList(Connection con, Statement stmt, ResultSet rs)
  {
    return getDocumentOption(con, stmt, rs, "5");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean appendDeliveryDate(Connection con, Statement stmt, ResultSet rs)
  {
    return getDocumentOption(con, stmt, rs, "4");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean plPrependMfrCode(Connection con, Statement stmt, ResultSet rs)
  {
    return getDocumentOption(con, stmt, rs, "3");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean prependMfrCode(Connection con, Statement stmt, ResultSet rs)
  {
    return getDocumentOption(con, stmt, rs, "11");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  public String unitOfWeight(Connection con, Statement stmt, ResultSet rs)
  {
    return getDocumentSetting(con, stmt, rs, "2");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getPickingListQueueDefaultPrinter(Connection con, Statement stmt, ResultSet rs)
  {
    return getDocumentSetting(con, stmt, rs, "3");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean useStoreAndQtyNotTwoStores(Connection con, Statement stmt, ResultSet rs)
  {
    return getDocumentOption(con, stmt, rs, "2");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  public String inventoryCostingMethod(Connection con, Statement stmt, ResultSet rs)
  {
    return getDocumentSetting(con, stmt, rs, "1");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean ocBlockUnlessManagerApproved(Connection con, Statement stmt, ResultSet rs)
  {
    return getDocumentOption(con, stmt, rs, "1");
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean getDocumentOption(Connection con, Statement stmt, ResultSet rs, String option)
  {
    boolean b = false;
 
    try
    {
      stmt = con.createStatement();
      
      rs = stmt.executeQuery("SELECT Option" + option + " FROM documentoptions");
      
      if(rs.next())
      {
        if((rs.getString(1)).equals("Y"))
          b = true;
      }
      
      rs.close();
      stmt.close();
    }
    catch(Exception e)
    {
      System.out.println("DefinitionTables: DocOption: " + e);
      try
      {
        if(rs   != null) rs.close();     
        if(stmt != null) stmt.close();
      }
      catch(Exception e2) { }
    }
    
    return b;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private String getDocumentSetting(Connection con, Statement stmt, ResultSet rs, String option)
  {
    String s = "";
 
    try
    {
      stmt = con.createStatement();
      
      rs = stmt.executeQuery("SELECT Option" + option + " FROM documentsettings");
      
      if(rs.next())
        s = rs.getString(1);
      
      rs.close();
      stmt.close();
    }
    catch(Exception e)
    {
      System.out.println("DefinitionTables: DocSetting: " + e);
      try
      {
        if(rs   != null) rs.close();     
        if(stmt != null) stmt.close();
      }
      catch(Exception e2) { }
    }
    
    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getDefaultStore(Connection con, Statement stmt, ResultSet rs) throws Exception
  {
    String name = "'";

    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT * FROM store WHERE Type = 'D'");

      if(rs.next())
        name = rs.getString(1);

      rs.close();
      stmt.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }

    return name;
  }

}

