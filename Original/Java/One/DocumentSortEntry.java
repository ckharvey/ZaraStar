// =======================================================================================================================================================================================================
// System: ZaraStar: DocumentEngine: sort by entry
// Module: DocumentSortEntry.java
// Author: C.K.Harvey
// Copyright (c) 2003-09 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import java.sql.*;

public class DocumentSortEntry
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void process(Connection con, Statement stmt, Statement stmt2, ResultSet rs, String docCode, String docType) throws Exception
  {
    try
    {
      byte[] dataAlready = new byte[1];  dataAlready[0] = '\000';
      byte[] docCodeB    = new byte[21];
      generalUtils.strToBytes(docCodeB, docCode);

      if(docType.equals("Q"))
      {    
          forADocType(con, stmt, stmt2, rs, docCode, "quotel", "QuoteCode");
      }
      else
      if(docType.equals("S"))
      {    
        forADocType(con, stmt, stmt2, rs, docCode, "sol", "SOCode");
      }
    }
    catch(Exception e)
    {
      System.out.println("6099: " + e);
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void forADocType(Connection con, Statement stmt, Statement stmt2, ResultSet rs, String docCode, String tableName, String docCodeFldName) throws Exception
  {
    try
    {
      // for each doc line
      //   note line and entry
      //   sort by entry
      //   renumber lines on disk (twice, in order to avoid line# clashes)

      int numLines = 0;

      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT COUNT(1) AS rowcount FROM " + tableName + " WHERE " + docCodeFldName + " = '" + docCode + "'");

      if(rs.next())
        numLines = rs.getInt("rowcount");

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();

      if(numLines > 0)
      {
        String[] entries  = new String[numLines];
        int[]    lineNums = new int[numLines];
        boolean atleastOneSwapped;

        stmt = con.createStatement();

        rs = stmt.executeQuery("SELECT Line, Entry FROM " + tableName + " WHERE " + docCodeFldName + " = '" + docCode + "' ORDER BY Line");

        int count = 0;

        while(rs.next())
        {
          lineNums[count] = generalUtils.strToInt(rs.getString(1));
          entries[count]  = rs.getString(2);

          ++count;
        }

        if(rs   != null) rs.close();
        if(stmt != null) stmt.close();

        atleastOneSwapped = generalUtils.entryInsertionSort(entries, numLines, lineNums);

        if(atleastOneSwapped)
        {
          // for each entry in sorted list, renumber line to (1,000,000 + the entry-count)

          int i, newLineNum;

          for(i=0;i<numLines;++i)
          {
            newLineNum = 100001 + i;

            stmt = con.createStatement();

            try
            {
              stmt.executeUpdate("UPDATE " + tableName + " SET Line = '" + newLineNum + "' WHERE " + docCodeFldName + " = '" + docCode + "' AND Entry = '" + entries[i] + "'");
            }
            catch(Exception e) { } // catches dup error where there are two lines with the same 'entry'

            if(stmt != null) stmt.close();
              
            moveMultipleLines(con, stmt, docCode, tableName + "l", docCodeFldName, lineNums[i], newLineNum);
          }

          stmt = con.createStatement();

          rs = stmt.executeQuery("SELECT Line FROM " + tableName + " WHERE " + docCodeFldName + " = '" + docCode + "' ORDER BY Line");

          int thisLineNum;

          while(rs.next()) // just-in-case
          {
            thisLineNum = generalUtils.strToInt(rs.getString(1));
            newLineNum  = thisLineNum - 100000;

            stmt2 = con.createStatement();

            stmt2.executeUpdate("UPDATE " + tableName + " SET Line = '" + newLineNum + "' WHERE " + docCodeFldName + " = '" + docCode + "' AND Line = '" + thisLineNum + "'");

            if(stmt2 != null) stmt2.close();

            moveMultipleLines(con, stmt2, docCode, tableName + "l", docCodeFldName, thisLineNum, newLineNum);
          }

          if(rs   != null) rs.close();
          if(stmt != null) stmt.close();
        }
      }
    }
    catch(Exception e)
    {
      System.out.println("6099: " + e);
      try
      {
        if(rs   != null) rs.close();
        if(stmt != null) stmt.close();
      }
      catch(Exception e2) { }
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void moveMultipleLines(Connection con, Statement stmt, String docCode, String tableName, String docCodeFldName, int oldLineNum, int newLineNum) throws Exception
  {
    try
    {
      stmt = con.createStatement();

      stmt.executeUpdate("UPDATE " + tableName + " SET Entry = '" + newLineNum + "' WHERE " + docCodeFldName + " = '" + docCode + "' AND Entry = '" + oldLineNum + "'");

      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println("6099: " + e);
      try
      {
        if(stmt != null) stmt.close();
      }
      catch(Exception e2) { }
    }
  }
  
}
