// =======================================================================================================================================================================================================
// System: ZaraStar Admin: Trail Record Access
// Module: trail.java
// Author: C.K.Harvey
// Copyright (c) 2001-07 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import java.sql.*;

public class Trail
{
  GeneralUtils generalUtils = new GeneralUtils();
    
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getTableCreateStringTrail() throws Exception
  {
    return "trail ( UserCode char(20),  Service integer, BytesOut integer, BytesIn integer, DateTime timestamp, Host char(40), Text char(80) )";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStringsTrail(String[] s) throws Exception
  {
    s[0] = "trailUserCodeInx on trail (UserCode)";   
    s[1] = "serviceInx on trail (Service)";   
    return 2;
  }
      
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNamesTrail() throws Exception
  {  
    return "UserCode, Service, BytesOut, BytesIn, DateTime, Host, Text";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypesTrail() throws Exception
  {
    return "CIIISCC";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizesTrail(short[] sizes) throws Exception
  {
    sizes[0] = 20;  sizes[1] = 0;  sizes[2] = 0;  sizes[3] = 0;  sizes[4] = -1;  sizes[5] = 40;  sizes[6] = 80;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldStylesTrail() throws Exception
  {
    return "OOOOOOOO";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getTableCreateStringLastTrail() throws Exception
  {
    return "lasttrail ( UserCode char(20), Service integer, DateTime timestamp, Host char(40), Text char(80) )";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStringsLastTrail(String[] s) throws Exception
  {
    return 0;
  }
      
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNamesLastTrail() throws Exception
  {  
    return "UserCode, Service, DateTime, Host, Text";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypesLastTrail() throws Exception
  {
    return "CISCC";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizesLastTrail(short[] sizes) throws Exception
  {
    sizes[0] = 20;  sizes[1] = 0;  sizes[2] = -1;  sizes[3] = 40;  sizes[4] = 80;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldStylesLastTrail() throws Exception
  {
    return "OOOOOO";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void addToTrail(Connection con, Statement stmt, ResultSet rs, String dnm, String userCode, String service, String bytesOut, String bytesIn, String host, String text)
  {
    if(service.equals("8612") || service.equals("11200") || service.equals("12600") || service.equals("12601"))
      return;
    
    try
    {
      stmt = con.createStatement();
      stmt.executeUpdate("INSERT INTO trail ( UserCode, Service, BytesOut, BytesIn, DateTime, Host, Text ) VALUES ('" + userCode + "','" + service + "','" + bytesOut + "','" + bytesIn + "',NULL,'" + host + "','" + generalUtils.sanitiseForSQL(text)
                       + "')");
      
      if(stmt != null) stmt.close();

      stmt = con.createStatement();
   
      rs = stmt.executeQuery("SELECT COUNT(0) AS rowcount FROM lasttrail WHERE UserCode = '" + userCode + "'");
      int rowCount = 0;
      if(rs.next())
        rowCount = rs.getInt("rowcount");
      if(rs != null) rs.close();

      if(rowCount > 0)
      {
        stmt.executeUpdate("UPDATE lasttrail SET Service = '" + service + "', DateTime = NULL, Host = '" + host + "', Text = '" + generalUtils.sanitiseForSQL(text) + "' WHERE UserCode = '" + userCode + "'");
      }
      else
      {
        stmt.executeUpdate("INSERT INTO lasttrail ( UserCode, Service, DateTime, Host, Text ) VALUES ('" + userCode + "','" + service + "',NULL,'" + host + "','" + generalUtils.sanitiseForSQL(text) + "')");
      }
      
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println("Trail: addToTrail: " + e);

      try
      {
        if(rs != null) rs.close();
        if(stmt != null) stmt.close();
      }
      catch(Exception e2) { }
    }
  }

}
