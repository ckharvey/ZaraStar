// ===================================================================================================================================================
// System: ZaraStar Admin: Config Utilities
// Module: AdminUtilitiesConfig.java
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

public class AdminUtilitiesConfig
{
  GeneralUtils generalUtils = new GeneralUtils();
    DirectoryUtils directoryUtils = new DirectoryUtils();

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean primeAppConfig(Connection con, Statement stmt, String dnm)
  {
    try
    {
      stmt = con.createStatement();
      stmt.executeUpdate("INSERT INTO appconfig ( CompanyName, DNM, CurrentStyle, ApplicationStartDate, FinancialYearStartMonth, FinancialYearEndMonth, EffectiveStartDate, CompanyPhone, CompanyFax, CompanyEMail, Description, TerseName, "
                       + "DateStyle, DateSeparator, Latitude, Longitude, GoogleMapsKey ) "
                       + "VALUES ('" + dnm + "','" + dnm + "','xxx Modern', '2010-01-01', 'January', 'December', '2010-01-01', '','','','','" + dnm + "','Y','.','','','')");

      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println("7062e: " + e);
      try
      {
        if(stmt != null) stmt.close();
      }
      catch(Exception e2) { }
      return false;
    }

    return true;
  }

}
