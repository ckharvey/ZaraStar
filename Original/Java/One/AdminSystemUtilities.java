// =======================================================================================================================================================================================================
// System: ZaraStar Admin: System Utilities
// Module: AdminSystemUtilities.java
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

public class AdminSystemUtilities
{
  GeneralUtils generalUtils = new GeneralUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void primeSystem(String dnm, String localDefnsDir, String defnsDir)
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
      stmt.executeUpdate("INSERT INTO system ( SystemStatus ) "
                       + "VALUES ('L')");
      
      if(stmt != null) stmt.close();    
      if(con  != null) con.close();
    }
    catch(Exception e)
    {
      System.out.println("AdminSystemUtilities: " + e);
      try
      {
        if(stmt != null) stmt.close();
        if(con  != null) con.close();
      }
      catch(Exception e2) { }
    }
  }
  
}
