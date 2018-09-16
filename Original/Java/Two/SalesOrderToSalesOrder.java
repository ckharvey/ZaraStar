// =======================================================================================================================================================================================================
// System: ZaraStar DocumentEngine: SO to SO
// Module: SalesOrderToSalesOrder.java
// Author: C.K.Harvey
// Copyright (c) 2000-07 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
//
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import java.sql.*;

public class SalesOrderToSalesOrder
{
  GeneralUtils  generalUtils  = new GeneralUtils();
  ServerUtils  serverUtils  = new ServerUtils();
  DocumentUtils  documentUtils  = new DocumentUtils();
  MiscDefinitions  miscDefinitions  = new MiscDefinitions();
  DocumentAttachmentsCopy documentAttachmentsCopy = new DocumentAttachmentsCopy();
  Customer customer  = new Customer();
  SalesOrder salesOrder  = new SalesOrder();

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean create(Connection con, Statement stmt, ResultSet rs, byte[] code, char renumberLines, char renumberEntries, int[] lines, int numLines, String unm, String dnm, String localDefnsDir, String defnsDir, byte[] newDocCode,
                        int[] bytesOut) throws Exception
  {
    documentUtils.getNextCode(con, stmt, rs, "so", true, newDocCode);
    generalUtils.toUpper(newDocCode, 0);

    byte[] data   = new byte[3000];
    byte[] dataTo = new byte[3000];
    byte[] b      = new byte[300];
    int z, thisLine;
    boolean found;
    
    byte[] linesData  = new byte[2000];
    int[]  listLen    = new int[1];  listLen[0] = 2000;
    int[]  linesCount = new int[1];
 
    byte[] multipleLinesData  = new byte[2000];
    int[]  multipleListLen    = new int[1];  multipleListLen[0] = 2000;
    int[]  multipleLinesCount = new int[1];

    // create old line numbers to new line numbers mapping
    int[] newLines = new int[numLines];
    if(renumberLines == 'Y')
    {
      for(z=0;z<numLines;++z)
        newLines[z] = (z + 1);
    }
    else
    {
      for(z=0;z<numLines;++z)
        newLines[z] = lines[z];
    }
  
    if(salesOrder.getRecGivenCode(con, stmt, rs, newDocCode, '\000', data, dnm, localDefnsDir, defnsDir, bytesOut) != -1) // just-in-case
    {
      generalUtils.strToBytes(newDocCode, "h0008.htm"); // already exists
      return false;
    }

    if(salesOrder.getRecGivenCode(con, stmt, rs, code, '\000', data, dnm, localDefnsDir, defnsDir, bytesOut) == -1)
    {
      generalUtils.strToBytes(newDocCode, "h0031.htm"); // not found
      return false;
    }

    String customerCode = generalUtils.dfsAsStr(data, (short)2);

    soToSO('H', newDocCode, data, dataTo, unm, localDefnsDir, defnsDir, b);

    if(salesOrder.putRecHeadGivenCode(con, stmt, rs, newDocCode, 'N', dataTo, dnm, localDefnsDir, defnsDir) != ' ')
    {
      generalUtils.strToBytes(newDocCode, "h0007.htm"); // not updated
      return false;
    }

    // fetch lines data in one go
    linesData = salesOrder.getLines(con, stmt, rs, code, '\000', linesData, listLen, linesCount, dnm, localDefnsDir, defnsDir);
    
    int entryCount = 0;
    for(int xx=0;xx<linesCount[0];++xx)
    {
      if(generalUtils.getListEntryByNum(xx, linesData, data)) // just-in-case
      {
        generalUtils.replaceTwosWithNulls(data);
                     
        // check this line
        thisLine = generalUtils.dfsAsInt(data, (short)22); // origin-1
        found = false;
        z=0;
        while(! found && z < numLines)
        {
          if(lines[z] == thisLine)
            found = true;
          else ++z;
        }
                     
        if(found)
        {
          generalUtils.repAlpha(data, 3000, (short)22, newLines[z]);

          if(renumberEntries == 'Y')
            generalUtils.repAlpha(data, 3000, (short)23, ++entryCount);

          soToSO('L', newDocCode, data, dataTo, unm, localDefnsDir, defnsDir, b);
          salesOrder.putRecLine(con, stmt, newDocCode, null, 'N', dataTo, dnm, localDefnsDir, defnsDir);
        }
      }
    }  

    if(linesCount[0] > 0) // get all the multiple lines for this document
    {
      multipleLinesData = salesOrder.getMultipleLines(con, stmt, rs, code, multipleLinesData, multipleListLen, multipleLinesCount, dnm, localDefnsDir, defnsDir);
    }

    for(int xx=0;xx<multipleLinesCount[0];++xx)
    {
      if(generalUtils.getListEntryByNum(xx, multipleLinesData, data)) // just-in-case
      {
        generalUtils.replaceTwosWithNulls(data);
        thisLine = generalUtils.dfsAsInt(data, (short)2); // line (entry)

        // check this line
        found = false;
        z=0;
        while(! found && z < numLines)
        {
          if(lines[z] == thisLine)
            found = true;
          else ++z;
        }
                     
        if(found)
        {
          soToSO('M', newDocCode, data, dataTo, unm, localDefnsDir, defnsDir, b);
          salesOrder.putMultipleLine(con, stmt, rs, newDocCode, newLines[z], dataTo, dnm, localDefnsDir, defnsDir);
        }
      }
    }

    documentAttachmentsCopy.copyAttachments(con, stmt, rs, "soa", code, "soa", newDocCode, dnm, defnsDir, localDefnsDir);                 

    serverUtils.syncToIW(con, generalUtils.stringFromBytes(newDocCode, 0L), "", "", 'S', generalUtils.todaySQLFormat(localDefnsDir, defnsDir), unm, customerCode, dnm);

    return true;
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void soToSO(char which, byte[] newDocCode, byte[] fromBuf, byte[] toBuf, String unm, String localDefnsDir,
                      String defnsDir, byte[] b) throws Exception
  {
    int x;

    generalUtils.zeroize(toBuf, 3000);

    generalUtils.repAlpha(toBuf, 3000, (short)0, newDocCode);
    
    switch(which)
    {
      case 'H' : // head
                 generalUtils.repAlpha(toBuf, 3000, (short)1, generalUtils.todaySQLFormat(localDefnsDir, defnsDir));

                 for(x=2;x<=11;++x)
                 {
                   generalUtils.dfs(fromBuf, (short)x, b);
                   generalUtils.repAlpha(toBuf, 3000, (short)x, b);
                 }
                 
                 generalUtils.repAlpha(toBuf, 3000, (short)12, ""); // unused1
                 generalUtils.repAlpha(toBuf, 3000, (short)13, ""); // unused2

                 for(x=14;x<=18;++x)
                 {
                   generalUtils.dfs(fromBuf, (short)x, b);
                   generalUtils.repAlpha(toBuf, 3000, (short)x, b);
                 }

                 generalUtils.repAlpha(toBuf, 3000, (short)19, "1970-01-01"); // unused4
                 generalUtils.repAlpha(toBuf, 3000, (short)20, "0.0"); // unused5

                 generalUtils.repAlpha(toBuf, 3000, (short)21, "N"); // allsupplied

                 generalUtils.repAlpha(toBuf, 3000, (short)22, unm); // signon

                 generalUtils.repAlpha(toBuf, 3000, (short)23, ""); // dlm

                 generalUtils.repAlpha(toBuf, 3000, (short)24, ""); // unused8

                 generalUtils.repAlpha(toBuf, 3000, (short)25, "L"); // status

                 for(x=26;x<=41;++x)
                 {
                   generalUtils.dfs(fromBuf, (short)x, b);
                   generalUtils.repAlpha(toBuf, 3000, (short)x, b);
                 }

                 generalUtils.repAlpha(toBuf, 3000, (short)27, ""); // quotecode
                 generalUtils.repAlpha(toBuf, 3000, (short)39, ""); // custpocode

                 for(x=42;x<=45;++x)
                 {
                   generalUtils.dfs(fromBuf, (short)x, b);
                   generalUtils.repAlpha(toBuf, 3000, (short)x, b);
                 }
                 
                 generalUtils.repAlpha(toBuf, 3000, (short)46, "N"); 
                 generalUtils.repAlpha(toBuf, 3000, (short)47, "1970-01-01"); // toEngineeringDate
                 generalUtils.repAlpha(toBuf, 3000, (short)48, ""); 
                 generalUtils.repAlpha(toBuf, 3000, (short)49, "N"); 
                 generalUtils.repAlpha(toBuf, 3000, (short)50, "1970-01-01"); // engineeringApprovedDate
                 generalUtils.repAlpha(toBuf, 3000, (short)51, ""); 
                 generalUtils.repAlpha(toBuf, 3000, (short)52, "N"); 
                 generalUtils.repAlpha(toBuf, 3000, (short)53, "1970-01-01"); // toManagerDate
                 generalUtils.repAlpha(toBuf, 3000, (short)54, ""); 
                 generalUtils.repAlpha(toBuf, 3000, (short)55, "N"); 
                 generalUtils.repAlpha(toBuf, 3000, (short)56, "1970-01-01"); // managerApprovedDate
                 generalUtils.repAlpha(toBuf, 3000, (short)57, ""); 
                 generalUtils.repAlpha(toBuf, 3000, (short)58, "N"); 
                 generalUtils.repAlpha(toBuf, 3000, (short)59, "1970-01-01"); // ToProcurementDate
                 generalUtils.repAlpha(toBuf, 3000, (short)60, ""); 
                 generalUtils.repAlpha(toBuf, 3000, (short)61, "N"); 
                 generalUtils.repAlpha(toBuf, 3000, (short)62, "1970-01-01"); // ToSchedulingDate
                 generalUtils.repAlpha(toBuf, 3000, (short)63, ""); 
                 generalUtils.repAlpha(toBuf, 3000, (short)64, "N"); 
                 generalUtils.repAlpha(toBuf, 3000, (short)65, "1970-01-01"); // DrawingsFiledDate
                 generalUtils.repAlpha(toBuf, 3000, (short)66, ""); 
                 generalUtils.repAlpha(toBuf, 3000, (short)67, "N"); 
                 generalUtils.repAlpha(toBuf, 3000, (short)68, "1970-01-01"); // ProcurementConfirmedDate
                 generalUtils.repAlpha(toBuf, 3000, (short)69, ""); 
                 generalUtils.repAlpha(toBuf, 3000, (short)70, "N"); 
                 generalUtils.repAlpha(toBuf, 3000, (short)71, "1970-01-01"); // SchedulingConfirmedDate
                 generalUtils.repAlpha(toBuf, 3000, (short)72, ""); 

                 break;
      case 'L' : // line
                 for(x=1;x<=6;++x)
                 {
                   generalUtils.dfs(fromBuf, (short)(x+1), b);
                   generalUtils.repAlpha(toBuf, 3000, (short)x, b);
                 }

                 generalUtils.repAlpha(toBuf, 3000, (short)7, "0"); // unused1
                 generalUtils.repAlpha(toBuf, 3000, (short)8, "0"); // unused2
                 generalUtils.repAlpha(toBuf, 3000, (short)9, "1970-01-01"); // WIPOverride
                 
                 generalUtils.repAlpha(toBuf, 3000, (short)10, " "); // status

                 generalUtils.repAlpha(toBuf, 3000, (short)11, unm); // signon

                 generalUtils.repAlpha(toBuf, 3000, (short)12, ""); // dlm
                 
                 generalUtils.repAlpha(toBuf, 3000, (short)13, ""); // unuse9
                 generalUtils.repAlpha(toBuf, 3000, (short)14, ""); // unused4
                 generalUtils.repAlpha(toBuf, 3000, (short)15, ""); // unused5
                 generalUtils.repAlpha(toBuf, 3000, (short)16, ""); // unused6
                 generalUtils.repAlpha(toBuf, 3000, (short)17, ""); // unused7
                 generalUtils.repAlpha(toBuf, 3000, (short)18, ""); // unused8

                 for(x=19;x<=27;++x)
                 {
                   generalUtils.dfs(fromBuf, (short)(x+1), b);
                   generalUtils.repAlpha(toBuf, 3000, (short)x, b);
                 }

                 generalUtils.dfs(fromBuf, (short)29, b); // costPrice
                 if(b[0] == '\000')
                   generalUtils.strToBytes(b, "0");
                 generalUtils.repAlpha(toBuf, 3000, (short)28, b);
                 
                 generalUtils.dfs(fromBuf, (short)30, b); // manufacturer
                 generalUtils.repAlpha(toBuf, 3000, (short)29, b);

                 generalUtils.dfs(fromBuf, (short)31, b); // manufacturerCode
                 generalUtils.repAlpha(toBuf, 3000, (short)30, b);

                 generalUtils.dfs(fromBuf, (short)32, b); // woCode
                 generalUtils.repAlpha(toBuf, 3000, (short)31, b);

                 generalUtils.dfs(fromBuf, (short)33, b); // woOverride
                 generalUtils.repAlpha(toBuf, 3000, (short)32, b);
                 
                 generalUtils.repAlpha(toBuf, 3000, (short)33, "N"); // toProcurement
                 generalUtils.repAlpha(toBuf, 3000, (short)34, "1970-01-01");
                 generalUtils.repAlpha(toBuf, 3000, (short)35, ""); 
                 generalUtils.repAlpha(toBuf, 3000, (short)36, "N"); // ReadyForWorkshop
                 generalUtils.repAlpha(toBuf, 3000, (short)37, "1970-01-01");
                 generalUtils.repAlpha(toBuf, 3000, (short)38, ""); 
                 generalUtils.repAlpha(toBuf, 3000, (short)39, "N"); // ReadyForDispatch
                 generalUtils.repAlpha(toBuf, 3000, (short)40, "1970-01-01");
                 generalUtils.repAlpha(toBuf, 3000, (short)41, ""); 
                 generalUtils.repAlpha(toBuf, 3000, (short)42, "N"); // DocumentsVerified
                 generalUtils.repAlpha(toBuf, 3000, (short)43, "1970-01-01");
                 generalUtils.repAlpha(toBuf, 3000, (short)44, ""); 
                 generalUtils.repAlpha(toBuf, 3000, (short)45, "N"); // Dispatched
                 generalUtils.repAlpha(toBuf, 3000, (short)46, "1970-01-01");
                 generalUtils.repAlpha(toBuf, 3000, (short)47, ""); 
                 generalUtils.repAlpha(toBuf, 3000, (short)48, "N"); // Invoiced
                 generalUtils.repAlpha(toBuf, 3000, (short)49, "1970-01-01");
                 generalUtils.repAlpha(toBuf, 3000, (short)50, ""); 

                 break;
      case 'M' : // LL line
                 for(x=1;x<4;++x)
                 {
                   generalUtils.dfs(fromBuf, (short)(x+1), b);
                   generalUtils.repAlpha(toBuf, 3000, (short)x, b);
                 }
                 break;
    }
  }

}
