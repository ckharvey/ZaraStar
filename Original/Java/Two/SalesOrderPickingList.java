// =======================================================================================================================================================================================================
// System: ZaraStar Document: SO to PL
// Module: SalesOrderPickingList.java
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

public class SalesOrderPickingList
{
  GeneralUtils  generalUtils  = new GeneralUtils();
  ServerUtils  serverUtils  = new ServerUtils();
  DocumentUtils  documentUtils  = new DocumentUtils();
  MiscDefinitions  miscDefinitions  = new MiscDefinitions();
  DocumentAttachmentsCopy documentAttachmentsCopy = new DocumentAttachmentsCopy();
  SalesOrder salesOrder  = new SalesOrder();
  PickingList  pickingList  = new PickingList();
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean create(Connection con, Statement stmt, ResultSet rs, byte[] code, char renumberLines, char renumberEntries, int[] lines, int numLines, String quantitiesReqd, String unm,
                        String dnm, String localDefnsDir, String defnsDir, byte[] newDocCode, int[] bytesOut) throws Exception
  {
    documentUtils.getNextCode(con, stmt, rs, "pl", true, newDocCode);
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
  
    if(pickingList.getRecGivenCode(con, stmt, rs, newDocCode, '\000', data, dnm, localDefnsDir, defnsDir, bytesOut) != -1) // just-in-case
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

    soToPL(con, stmt, rs, 'H', newDocCode, data, dataTo, null, null, "", unm, localDefnsDir, defnsDir, b);

    if(pickingList.putRecHeadGivenCode(con, stmt, rs, newDocCode, 'N', dataTo, dnm, localDefnsDir, defnsDir) != ' ')
    {
      generalUtils.strToBytes(newDocCode, "h0007.htm"); // not updated
      return false;
    }

    // fetch lines data in one go
    linesData = salesOrder.getLines(con, stmt, rs, code, '\000', linesData, listLen, linesCount, dnm, localDefnsDir, defnsDir);
    
    int entryCount = 0;
    String qtyReqd;
    byte[] originalLine = new byte[10];
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
          // get the qtyRead entry
          qtyReqd = getEntry(xx, quantitiesReqd);
                
          generalUtils.dfs(data, (short)22, originalLine);
          generalUtils.repAlpha(data, 3000, (short)22, newLines[z]); // origin-1

          if(renumberEntries == 'Y')
            generalUtils.repAlpha(data, 3000, (short)23, ++entryCount);

          soToPL(con, stmt, rs, 'L', newDocCode, data, dataTo, code, originalLine, qtyReqd, unm, localDefnsDir, defnsDir, b);
          pickingList.putRecLine(con, stmt, rs, newDocCode, null, 'N', dataTo, dnm, localDefnsDir, defnsDir);
        }
      }
    }  

    if(linesCount[0] > 0) // get all the multiple lines for this document
    {
      multipleLinesData = salesOrder.getMultipleLines(con, stmt, rs, code, multipleLinesData, multipleListLen, multipleLinesCount, dnm, localDefnsDir,
                                                 defnsDir);
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
          soToPL(con, stmt, rs, 'M', newDocCode, data, dataTo, null, originalLine, "", unm, localDefnsDir, defnsDir, b);
          pickingList.putMultipleLine(con, stmt, rs, newDocCode, newLines[z], dataTo, dnm, localDefnsDir, defnsDir);
        }
      }
    }

    documentAttachmentsCopy.copyAttachments(con, stmt, rs, "soa", code, "pla", newDocCode, dnm, defnsDir, localDefnsDir);                 

    serverUtils.syncToIW(con, generalUtils.stringFromBytes(newDocCode, 0L), "", "", 'P', generalUtils.todaySQLFormat(localDefnsDir, defnsDir), unm, customerCode, dnm);

    return true;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void soToPL(Connection con, Statement stmt, ResultSet rs, char which, byte[] newDocCode, byte[] fromBuf, byte[] toBuf, byte[] code, byte[] originalLine, String qtyReqd,
                      String unm, String localDefnsDir, String defnsDir, byte[] b) throws Exception
  {
    int x;

    generalUtils.zeroize(toBuf, 3000);

    generalUtils.repAlpha(toBuf, 3000, (short)0, newDocCode);
    
    switch(which)
    {
      case 'H' : // head
                 generalUtils.repAlpha(toBuf, 3000, (short)1, generalUtils.todaySQLFormat(localDefnsDir, defnsDir));

                 // stuff from SO
                 
                 for(x=2;x<=11;++x)
                 {
                   generalUtils.dfs(fromBuf, (short)x, b);
                   generalUtils.repAlpha(toBuf, 3000, (short)x, b);
                 }

                 generalUtils.repAlpha(toBuf, 3000, (short)17, "0.0");
                 
                 generalUtils.repAlpha(toBuf, 3000, (short)18, "0.0");
                 
                 generalUtils.dfs(fromBuf, (short)43, b); // groupDiscount
                 generalUtils.repAlpha(toBuf, 3000, (short)21, b);
                 
                 generalUtils.dfs(fromBuf, (short)24, b); // locationCode
                 generalUtils.repAlpha(toBuf, 3000, (short)23, b);
                 
                 generalUtils.dfs(fromBuf, (short)44, b); // groupDiscountType
                 generalUtils.repAlpha(toBuf, 3000, (short)24, b);
                 
                 generalUtils.dfs(fromBuf, (short)26, b); // shipAddrCode
                 generalUtils.repAlpha(toBuf, 3000, (short)26, b);
                 
                 generalUtils.dfs(fromBuf, (short)39, b); // custPOCode
                 generalUtils.repAlpha(toBuf, 3000, (short)28, b);
                 
                 generalUtils.dfs(fromBuf, (short)38, b); // salesPerson
                 generalUtils.repAlpha(toBuf, 3000, (short)29, b);
                 
                 generalUtils.dfs(fromBuf, (short)29, b); // projectCode
                 generalUtils.repAlpha(toBuf, 3000, (short)30, b);

                 for(x=32;x<=37;++x)
                 {
                   generalUtils.dfs(fromBuf, (short)x, b);
                   generalUtils.repAlpha(toBuf, 3000, (short)x, b);
                 }

                 generalUtils.dfs(fromBuf, (short)31, b); // currency
                 generalUtils.repAlpha(toBuf, 3000, (short)38, b);

                 generalUtils.dfs(fromBuf, (short)30, b); // rate
                 generalUtils.repAlpha(toBuf, 3000, (short)40, b);
                 
                 generalUtils.dfs(fromBuf, (short)45, b); // terms
                 generalUtils.repAlpha(toBuf, 3000, (short)44, b);
                 
                 generalUtils.repAlpha(toBuf, 3000, (short)45, "0.0");
                 
                 generalUtils.repAlpha(toBuf, 3000, (short)46, "0.0");
                 
                 generalUtils.dfs(fromBuf, (short)42, b); // buyerEMail
                 generalUtils.repAlpha(toBuf, 3000, (short)55, b);
                 
                 // other stuff
                 
                 generalUtils.repAlpha(toBuf, 3000, (short)12, "N"); // attention

                 generalUtils.repAlpha(toBuf, 3000, (short)13, "1970-01-01"); // dateOfStockUpdate
                 generalUtils.repAlpha(toBuf, 3000, (short)14, ""); // signOnForUpdated
                 
                 generalUtils.repAlpha(toBuf, 3000, (short)15, ""); // misc1
                 generalUtils.repAlpha(toBuf, 3000, (short)16, ""); // misc2

                 generalUtils.repAlpha(toBuf, 3000, (short)19, "N"); // completed
                 
                 generalUtils.repAlpha(toBuf, 3000, (short)20, unm); // signon

                 generalUtils.repAlpha(toBuf, 3000, (short)22, ""); // dlm
                 
                 generalUtils.repAlpha(toBuf, 3000, (short)25, ""); // type

                 generalUtils.repAlpha(toBuf, 3000, (short)27, ""); // purchasedBy

                 generalUtils.repAlpha(toBuf, 3000, (short)31, ""); // revisionOf
                 
                 generalUtils.repAlpha(toBuf, 3000, (short)39, "1"); // numCartons
                 
                 generalUtils.repAlpha(toBuf, 3000, (short)41, "N"); // stockprocessed

                 generalUtils.repAlpha(toBuf, 3000, (short)42, ""); // time
                 
                 generalUtils.repAlpha(toBuf, 3000, (short)43, "1970-01-01"); // expectedDate
                 
                 generalUtils.repAlpha(toBuf, 3000, (short)47, ""); // store
                 
                 generalUtils.repAlpha(toBuf, 3000, (short)48, "L"); // status
                
                 generalUtils.repAlpha(toBuf, 3000, (short)49, "0"); // totalGrossWeight
                 generalUtils.repAlpha(toBuf, 3000, (short)50, "");  // dimension
                 generalUtils.repAlpha(toBuf, 3000, (short)51, "");  // exportStatement
                 generalUtils.repAlpha(toBuf, 3000, (short)52, "");  // invoiceCode
                 generalUtils.repAlpha(toBuf, 3000, (short)53, "0"); // ocCode
                 generalUtils.repAlpha(toBuf, 3000, (short)54, "0"); // totalNetWeight

                 generalUtils.repAlpha(toBuf, 3000, (short)56, ""); // assignedStoreman
                 generalUtils.repAlpha(toBuf, 3000, (short)57, "1970-01-01 12:15:00"); // dateTimeAssigned
                 generalUtils.repAlpha(toBuf, 3000, (short)58, ""); // assignedBy
                 generalUtils.repAlpha(toBuf, 3000, (short)59, "1970-01-01 12:15:00"); // dateTimeReturned
                 generalUtils.repAlpha(toBuf, 3000, (short)60, ""); // returnedBy
                 generalUtils.repAlpha(toBuf, 3000, (short)61, "N"); // releastToStore
                 generalUtils.repAlpha(toBuf, 3000, (short)62, "0"); // timesPrinted
                 generalUtils.repAlpha(toBuf, 3000, (short)63, "1970-01-01 12:15:00"); // dateTimeLastPrinted
                 generalUtils.repAlpha(toBuf, 3000, (short)64, "1970-01-01 12:15:00"); // dateTimeQueued

                 break;
      case 'L' : // line
      
                 // stuff from SO
      
                 for(x=1;x<=6;++x)
                 {
                   generalUtils.dfs(fromBuf, (short)(x+1), b);
                   generalUtils.repAlpha(toBuf, 3000, (short)x, b);
                 }

                 generalUtils.repAlpha(toBuf, 3000, (short)5, "0.0"); // amount
                 
                 generalUtils.dfs(fromBuf, (short)24, b); // discount
                 generalUtils.repAlpha(toBuf, 3000, (short)7, b);
                 
                 generalUtils.repAlpha(toBuf, 3000, (short)10, "0.0");

                 generalUtils.dfs(fromBuf, (short)21, b); // uom
                 generalUtils.repAlpha(toBuf, 3000, (short)12, b);

                 //generalUtils.dfs(fromBuf, (short)5, b); // qtyReqd
                 generalUtils.repAlpha(toBuf, 3000, (short)16, qtyReqd);

                 generalUtils.dfs(fromBuf, (short)27, b); // custItemCode
                 generalUtils.repAlpha(toBuf, 3000, (short)17, b);

                 generalUtils.dfs(fromBuf, (short)22, b); // line
                 generalUtils.repAlpha(toBuf, 3000, (short)18, b);

                 generalUtils.dfs(fromBuf, (short)23, b); // entry
                 generalUtils.repAlpha(toBuf, 3000, (short)19, b);
                 
                 generalUtils.repAlpha(toBuf, 3000, (short)20, code); // soCode

                 generalUtils.dfs(fromBuf, (short)28, b); // store
                 generalUtils.repAlpha(toBuf, 3000, (short)29, b);

                 // the soLine must be the line before any renumbering is (maybe) done
                 generalUtils.repAlpha(toBuf, 3000, (short)30, originalLine);
                  
                 if(miscDefinitions.soRemarkToPLInstruction(con, stmt, rs))
                 {
                   generalUtils.dfs(fromBuf, (short)26, b);          // remark
                   generalUtils.repAlpha(toBuf, 3000, (short)24, b); // instruction
                 }
                 else generalUtils.repAlpha(toBuf, 3000, (short)24, ""); // instruction                 

                 generalUtils.dfs(fromBuf, (short)29, b); // costPrice
                 if(b[0] == '\000')
                   generalUtils.strToBytes(b, "0.0");                   
                 generalUtils.repAlpha(toBuf, 3000, (short)31, b);

                 // other stuff
                 
                 generalUtils.repAlpha(toBuf, 3000, (short)8, ""); // dlm

                 generalUtils.repAlpha(toBuf, 3000, (short)9, unm); // signon

                 generalUtils.repAlpha(toBuf, 3000, (short)11, ""); // custPOCode

                 generalUtils.repAlpha(toBuf, 3000, (short)13, ""); // serialNumber
                 
                 generalUtils.repAlpha(toBuf, 3000, (short)14, ""); // unused1
                 generalUtils.repAlpha(toBuf, 3000, (short)15, ""); // unused2
                 
                 generalUtils.repAlpha(toBuf, 3000, (short)21, "0"); // grossWeight
                 generalUtils.repAlpha(toBuf, 3000, (short)22, "0"); // weightPer
                 generalUtils.repAlpha(toBuf, 3000, (short)23, "0"); // qtyPacked
                 generalUtils.repAlpha(toBuf, 3000, (short)25, "");  // lotNum
                 generalUtils.repAlpha(toBuf, 3000, (short)26, "");  // invoiceCode
                 generalUtils.repAlpha(toBuf, 3000, (short)27, "0"); // netWeight
                 generalUtils.repAlpha(toBuf, 3000, (short)28, "0"); // numCartons

                 generalUtils.dfs(fromBuf, (short)30, b); // manufacturer
                 generalUtils.repAlpha(toBuf, 3000, (short)32, b);

                 generalUtils.dfs(fromBuf, (short)31, b); // manufacturerCode
                 generalUtils.repAlpha(toBuf, 3000, (short)33, b);
                 
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

  // -------------------------------------------------------------------------------------------------------------------------------
  private String getEntry(int entry, String quantitiesReqd)
  {
    String s="";

    try
    {
      int x=0, count=0, len=quantitiesReqd.length();
      while(count < entry)
      {
        while(x < len && quantitiesReqd.charAt(x) != '\001') // just-in-case
          ++x;
        ++x;
        ++count;
      }
      
      while(x < len && quantitiesReqd.charAt(x) != '\001') // just-in-case
        s += quantitiesReqd.charAt(x++);
    }
    catch(Exception e) { } 
    
    return s;
  }

}
