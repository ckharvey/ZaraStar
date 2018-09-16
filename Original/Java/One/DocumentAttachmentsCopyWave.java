// =======================================================================================================================================================================================================
// System: ZaraStar DocumentEngine: Copy attachments between documents
// Module: DocumentAttachmentsCopyWave.java
// Author: C.K.Harvey
// Copyright (c) 2006-09 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import java.sql.*;

public class DocumentAttachmentsCopyWave
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void copyAttachments(Connection con, Statement stmt, ResultSet rs, String sourceFileName, byte[] sourceCodeB, String targetFileName, byte[] targetCodeB, String dnm, String defnsDir, String localDefnsDir) throws Exception
  {
    String sourceCode = generalUtils.stringFromBytes(sourceCodeB, 0);
    String targetCode = generalUtils.stringFromBytes(targetCodeB, 0);

    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM " + sourceFileName + " WHERE Code = '" + sourceCode + "'");
      rs.next();
      int numRecs = rs.getInt("rowcount") ;
      if(rs != null) rs.close();

      if(numRecs > 0) // attachments exist
      {
        String[] libraryDocCodes = new String[numRecs];
        int count=0;

        rs = stmt.executeQuery("SELECT LibraryDocCode FROM " + sourceFileName + " WHERE Code = '" + sourceCode + "'");
        while(count < numRecs && rs.next()) // just-in-case
          libraryDocCodes[count++] = rs.getString(1);

        if(rs   != null) rs.close();
        if(stmt != null) stmt.close();

        stmt = con.createStatement();

        for(int x=0;x<count;++x)
          stmt.executeUpdate("INSERT INTO " + targetFileName + " ( Code, LibraryDocCode ) VALUES ('" + targetCode + "','" + libraryDocCodes[x] + "')");

        if(stmt != null) stmt.close();
      }
    }
    catch(Exception e)
    {
      System.out.println("6094c: " + e);
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
  }

}
