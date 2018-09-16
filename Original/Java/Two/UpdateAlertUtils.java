// =======================================================================================================================================================================================================
// System: ZaraStar Update Alerts: Utilities
// Module: BlogsUtils.java
// Author: C.K.Harvey
// Copyright (c) 2006-13 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import java.sql.*;

public class UpdateAlertUtils
{
  GeneralUtils generalUtils = new GeneralUtils();

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void createTableUpdatealerts(Connection con, Statement stmt, boolean dropTable, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    try
    {
      if(dropTable)
      {
        stmt = con.createStatement();
        
        stmt.executeUpdate("DROP TABLE updatealerts");
      }
    }
    catch(Exception e) { }
    
    if(stmt != null) stmt.close();

    try
    {
      stmt = con.createStatement();
      
      stmt.executeUpdate("CREATE TABLE updatealerts ( Code integer not null, DNM char(40), DateSent date, Title char(255), Type char(1), DocCode char(20), DocType char(1), AlertTo char(100), CompanyCode char(2), CompanyType char(1), unique(Code))");
    }
    catch(Exception e) { }
 
    if(stmt != null) stmt.close();

    try
    {
      stmt = con.createStatement();

      stmt.executeUpdate("CREATE INDEX dnmInx on updatealerts(DNM)");
    }
    catch(Exception e) { }
 
    if(stmt != null) stmt.close();
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean add(Connection con, Statement stmt, ResultSet rs, String DNM, String code, String dateSent, String title, String type, String docCode, String docType, String alertTo, String companyCode, String companyType, String[] newCode)
                     throws Exception
  {
    boolean res = false;
    
    try
    {
      boolean isNew = false;
      
      if(code.length() == 0) // new rec
      {
        stmt = con.createStatement();

        stmt.setMaxRows(1);
        
        rs = stmt.executeQuery("SELECT Code FROM updatealerts ORDER BY Code DESC");
        
        if(rs.next())                  
        {
          int highestCode = generalUtils.strToInt(rs.getString(1));
          newCode[0]      = generalUtils.intToStr(highestCode + 1);
        }
        else newCode[0] = "1";
        
        if(rs   != null) rs.close();
        if(stmt != null) stmt.close();
        
        isNew = true;
      }

      String q;
      
      title = generalUtils.stripLeadingAndTrailingSpaces(title);
      docCode = generalUtils.stripLeadingAndTrailingSpaces(docCode);
      alertTo = generalUtils.stripLeadingAndTrailingSpaces(alertTo);
      companyCode = generalUtils.stripLeadingAndTrailingSpaces(companyCode);

      if(isNew)
      {
        q = "INSERT INTO updatealerts (Code, DNM, DateSent, Title, Type, DocCode, DocType, AlertTo, CompanyCode, CompanyType) VALUES ('" + newCode[0] + "','" + DNM + "', {d '" + dateSent + "'},'"
          + generalUtils.sanitiseForSQL(title) + "','" + type + "','" + generalUtils.sanitiseForSQL(docCode) + "','" + docType + "','" + generalUtils.sanitiseForSQL(alertTo) + "','" + companyCode + "','" + companyType + "')";
      }  
      else
      {
        newCode[0] = code;
        
        q = "UPDATE updatealerts SET DNM = '" + DNM + "', DateSent = {d '" + dateSent + "'}, Title = '" + generalUtils.sanitiseForSQL(title) + "', Type = '" + type + "', DocCode = '" + generalUtils.sanitiseForSQL(docCode)
          + "', DocType = '" + docType + "', AlertTo = '" + generalUtils.sanitiseForSQL(alertTo) + "', CompanyType = '" + generalUtils.sanitiseForSQL(companyType) + "' WHERE Code = '" + code + "'";
      }

      stmt = con.createStatement();

      stmt.executeUpdate(q);

      if(stmt != null) stmt.close();
      
      res = true;
    }
    catch(Exception e)
    {
      System.out.println("updateAlertUtils: " + e);
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
    
    return res;
  }

}
