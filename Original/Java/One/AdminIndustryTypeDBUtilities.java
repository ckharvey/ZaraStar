// =================================================================================================================================
// System: ZaraStar AdminEngine: Industry Type DB Utilities
// Module: AdminIndustryTypeDBUtilities.java
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

public class AdminIndustryTypeDBUtilities
{
  GeneralUtils generalUtils = new GeneralUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();

  int count;
  
  // -------------------------------------------------------------------------------------------------------------------------------
  public void primeCountries(String dnm, String localDefnsDir, String defnsDir)
  {
    Connection con = null;
    Statement stmt = null;
    
    try
    {
      String uName    = directoryUtils.getMySQLUserName();
      String passWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();
    
      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + uName + "&password=" + passWord);

      primeAType(con, stmt, "Agriculture");
      primeAType(con, stmt, "Aviation & Aerospace");
      primeAType(con, stmt, "Biotechnology");
      primeAType(con, stmt, "Chemicals");
      primeAType(con, stmt, "Construction");
      primeAType(con, stmt, "Consulting");
      primeAType(con, stmt, "Defence");
      primeAType(con, stmt, "Diversified");
      primeAType(con, stmt, "Education");
      primeAType(con, stmt, "Electronics");
      primeAType(con, stmt, "Energy");
      primeAType(con, stmt, "Engineering");
      primeAType(con, stmt, "Environmental");
      primeAType(con, stmt, "Fashion & Textiles");
      primeAType(con, stmt, "Finance & Insurance");
      primeAType(con, stmt, "Food & Beverage");
      primeAType(con, stmt, "Furnishing & Decor");
      primeAType(con, stmt, "Government");
      primeAType(con, stmt, "Healthcare");
      primeAType(con, stmt, "Industrial Supply");
      primeAType(con, stmt, "Manufacturing");
      primeAType(con, stmt, "Media, Printing & Publishing");
      primeAType(con, stmt, "Not-for-Profit");
      primeAType(con, stmt, "Office Equipment & Supplies");
      primeAType(con, stmt, "Pharmaceuticals");
      primeAType(con, stmt, "Real Estate");
      primeAType(con, stmt, "Retail");
      primeAType(con, stmt, "Safety & Security");
      primeAType(con, stmt, "Shipping & Logistics");
      primeAType(con, stmt, "Sport & Leisure");
      primeAType(con, stmt, "Technology");
      primeAType(con, stmt, "Telecommunications");
      primeAType(con, stmt, "Transportation");
      primeAType(con, stmt, "Travel, Hospitality & Tourism");
      primeAType(con, stmt, "Utilities");
      primeAType(con, stmt, "Other");

      if(con  != null) con.close();
    }
    catch(Exception e)
    {
      System.out.println("AdminIndustryTypeDBUtilities: " + e);
      try
      {
        if(stmt != null) stmt.close();
        if(con  != null) con.close();
      }
      catch(Exception e2) { }
    }
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------
  private void primeAType(Connection con, Statement stmt, String name) throws Exception
  {
    stmt = con.createStatement();
    stmt.executeUpdate("INSERT INTO industrytype ( Name ) VALUES ('" + name + "')");
    if(stmt != null) stmt.close();
  }
  
}
