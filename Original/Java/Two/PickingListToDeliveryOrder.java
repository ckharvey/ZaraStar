// =======================================================================================================================================================================================================
// System: ZaraStar Document: PL to DO
// Module: PickingListToDeliveryOrder.java
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

public class PickingListToDeliveryOrder
{
  GeneralUtils  generalUtils  = new GeneralUtils();
  DocumentUtils  documentUtils  = new DocumentUtils();
  MiscDefinitions  miscDefinitions  = new MiscDefinitions();
  DocumentAttachmentsCopy documentAttachmentsCopy = new DocumentAttachmentsCopy();
  PickingList  pickingList  = new PickingList();
  DeliveryOrder  deliveryOrder  = new DeliveryOrder();
  ServerUtils serverUtils = new ServerUtils();
  
  // -------------------------------------------------------------------------------------------------------------------------------
  public boolean create(Connection con, Statement stmt, ResultSet rs, byte[] code, char renumberLines, char renumberEntries, int[] lines, int numLines, String unm, String dnm,
                        String localDefnsDir, String defnsDir, byte[] newDocCode, int[] bytesOut) throws Exception
  {
    documentUtils.getNextCode(con, stmt, rs, "do", true, newDocCode);
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
  
    if(deliveryOrder.getRecGivenCode(con, stmt, rs, newDocCode, '\000', data, dnm, localDefnsDir, defnsDir, bytesOut) != -1) // just-in-case
    {
      generalUtils.strToBytes(newDocCode, "h0008.htm"); // already exists
      return false;
    }

    if(pickingList.getRecGivenCode(con, stmt, rs, code, '\000', data, dnm, localDefnsDir, defnsDir, bytesOut) == -1)
    {
      generalUtils.strToBytes(newDocCode, "h0031.htm"); // not found
      return false;
    }

    String customerCode = generalUtils.dfsAsStr(data, (short)2);
    plToDO('H', newDocCode, data, dataTo, code, unm, localDefnsDir, defnsDir, b);

    if(deliveryOrder.putRecHeadGivenCode(con, stmt, rs, newDocCode, 'N', dataTo, dnm, localDefnsDir, defnsDir) != ' ')
    {
      generalUtils.strToBytes(newDocCode, "h0007.htm"); // not updated
      return false;
    }

    // fetch lines data in one go
    linesData = pickingList.getLines(con, stmt, rs, code, '\000', linesData, listLen, linesCount, dnm, localDefnsDir, defnsDir);
    
    int entryCount = 0;
    for(int xx=0;xx<linesCount[0];++xx)
    {
      if(generalUtils.getListEntryByNum(xx, linesData, data)) // just-in-case
      {
        generalUtils.replaceTwosWithNulls(data);
                     
        // check this line
        thisLine = generalUtils.dfsAsInt(data, (short)19); // origin-1
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
          generalUtils.repAlpha(data, 3000, (short)19, newLines[z]);

          if(renumberEntries == 'Y')
            generalUtils.repAlpha(data, 3000, (short)20, ++entryCount);

          plToDO('L', newDocCode, data, dataTo, code, unm, localDefnsDir, defnsDir, b);
          deliveryOrder.putRecLine(con, stmt, rs, newDocCode, null, 'N', dataTo, dnm, localDefnsDir, defnsDir);
        }
      }
    }  

    if(linesCount[0] > 0) // get all the multiple lines for this document
      multipleLinesData = pickingList.getMultipleLines(con, stmt, rs, code, multipleLinesData, multipleListLen, multipleLinesCount, dnm, localDefnsDir, defnsDir);

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
          plToDO('M', newDocCode, data, dataTo, null, unm, localDefnsDir, defnsDir, b);
          deliveryOrder.putMultipleLine(con, stmt, rs, newDocCode, newLines[z], dataTo, dnm, localDefnsDir, defnsDir);
        }
      }
    }

    documentAttachmentsCopy.copyAttachments(con, stmt, rs, "pla", code, "doa", newDocCode, dnm, defnsDir, localDefnsDir);                 

    
            serverUtils.syncToIW(con, generalUtils.stringFromBytes(newDocCode, 0L), "", "", 'D', generalUtils.todaySQLFormat(localDefnsDir, defnsDir), unm, customerCode, dnm);

    
    return true;
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void plToDO(char which, byte[] newDocCode, byte[] fromBuf, byte[] toBuf, byte[] code, String unm, String localDefnsDir,
                      String defnsDir, byte[] b) throws Exception
  {
    int x;

    generalUtils.zeroize(toBuf, 3000);

    generalUtils.repAlpha(toBuf, 3000, (short)0, newDocCode);
    
    switch(which)
    {
      case 'H' : // head
                 generalUtils.repAlpha(toBuf, 3000, (short)1, generalUtils.todaySQLFormat(localDefnsDir, defnsDir));

                 // stuff from PL
                 
                 for(x=2;x<=11;++x)
                 {
                   generalUtils.dfs(fromBuf, (short)x, b);
                   generalUtils.repAlpha(toBuf, 3000, (short)x, b);
                 }

                 generalUtils.dfs(fromBuf, (short)17, b); // gstTotal
                 generalUtils.repAlpha(toBuf, 3000, (short)17, b);
                 
                 generalUtils.dfs(fromBuf, (short)18, b); // totalTotal
                 generalUtils.repAlpha(toBuf, 3000, (short)18, b);
                 
                 generalUtils.dfs(fromBuf, (short)21, b); // groupDiscount
                 generalUtils.repAlpha(toBuf, 3000, (short)21, b);
                 
                 generalUtils.dfs(fromBuf, (short)24, b); // groupDiscountType
                 generalUtils.repAlpha(toBuf, 3000, (short)24, b);
                 
                 generalUtils.dfs(fromBuf, (short)23, b); // locationCode
                 generalUtils.repAlpha(toBuf, 3000, (short)23, b);
                 
                 generalUtils.dfs(fromBuf, (short)30, b); // projectCode
                 generalUtils.repAlpha(toBuf, 3000, (short)26, b);
                 
                 generalUtils.dfs(fromBuf, (short)26, b); // shipAddrCode
                 generalUtils.repAlpha(toBuf, 3000, (short)27, b);
                 
                 generalUtils.dfs(fromBuf, (short)27, b); // purchasedBy
                 generalUtils.repAlpha(toBuf, 3000, (short)28, b);
                 
                 generalUtils.dfs(fromBuf, (short)28, b); // PORefNum
                 generalUtils.repAlpha(toBuf, 3000, (short)29, b);
                 
                 generalUtils.dfs(fromBuf, (short)29, b); // salesPerson
                 generalUtils.repAlpha(toBuf, 3000, (short)30, b);
                 
                 generalUtils.dfs(fromBuf, (short)32, b); // shipName
                 generalUtils.repAlpha(toBuf, 3000, (short)33, b);
                 
                 generalUtils.dfs(fromBuf, (short)33, b); // shipAddr1
                 generalUtils.repAlpha(toBuf, 3000, (short)34, b);
                 
                 generalUtils.dfs(fromBuf, (short)34, b); // shipAddr2
                 generalUtils.repAlpha(toBuf, 3000, (short)35, b);
                 
                 generalUtils.dfs(fromBuf, (short)35, b); // shipAddr3
                 generalUtils.repAlpha(toBuf, 3000, (short)36, b);
                 
                 generalUtils.dfs(fromBuf, (short)36, b); // shipAddr4
                 generalUtils.repAlpha(toBuf, 3000, (short)37, b);
                 
                 generalUtils.dfs(fromBuf, (short)37, b); // shipAddr5
                 generalUtils.repAlpha(toBuf, 3000, (short)38, b);
                 
                 generalUtils.dfs(fromBuf, (short)38, b); // currency
                 generalUtils.repAlpha(toBuf, 3000, (short)39, b);
                 
                 generalUtils.dfs(fromBuf, (short)39, b); // numcartons
                 generalUtils.repAlpha(toBuf, 3000, (short)40, b);
                 
                 generalUtils.dfs(fromBuf, (short)40, b); // rate
                 generalUtils.repAlpha(toBuf, 3000, (short)41, b);
                 
                 generalUtils.dfs(fromBuf, (short)45, b); // baseTotalTotal
                 generalUtils.repAlpha(toBuf, 3000, (short)44, b);
                 
                 generalUtils.dfs(fromBuf, (short)46, b); // baseGSTTotal
                 generalUtils.repAlpha(toBuf, 3000, (short)45, b);
                 
                 generalUtils.dfs(fromBuf, (short)44, b); // terms
                 generalUtils.repAlpha(toBuf, 3000, (short)46, b);
                 
                 generalUtils.dfs(fromBuf, (short)51, b); // exportStatement
                 generalUtils.repAlpha(toBuf, 3000, (short)48, b);
                 
                 generalUtils.dfs(fromBuf, (short)55, b); // buyeremail
                 generalUtils.repAlpha(toBuf, 3000, (short)49, b);
                 
                 // other stuff
                 
                 generalUtils.repAlpha(toBuf, 3000, (short)12, ""); // unused1
                 generalUtils.repAlpha(toBuf, 3000, (short)13, "1970-01-01"); // unused2
                 generalUtils.repAlpha(toBuf, 3000, (short)14, ""); // invoiceCode
                 generalUtils.repAlpha(toBuf, 3000, (short)15, ""); // misc1
                 generalUtils.repAlpha(toBuf, 3000, (short)16, ""); // misc2
                 
                 generalUtils.repAlpha(toBuf, 3000, (short)19, "N"); // returned

                 generalUtils.repAlpha(toBuf, 3000, (short)20, unm); // signon

                 generalUtils.repAlpha(toBuf, 3000, (short)22, ""); // dlm

                 generalUtils.repAlpha(toBuf, 3000, (short)31, ""); // deliveryDriver
                 generalUtils.repAlpha(toBuf, 3000, (short)32, ""); // revisionOf

                 generalUtils.repAlpha(toBuf, 3000, (short)42, ""); // unused5

                 generalUtils.repAlpha(toBuf, 3000, (short)43, code); // plCode

                 generalUtils.repAlpha(toBuf, 3000, (short)47, "L"); // status

                 generalUtils.repAlpha(toBuf, 3000, (short)50, "1970-01-01"); // dateReturned
                 break;
      case 'L' : // line
      
                 // stuff from PL      
      
                 for(x=1;x<=3;++x)
                 {
                   generalUtils.dfs(fromBuf, (short)(x+1), b);
                   generalUtils.repAlpha(toBuf, 3000, (short)x, b);
                 }

                 generalUtils.dfs(fromBuf, (short)24, b); // qtyPacked
                 generalUtils.repAlpha(toBuf, 3000, (short)4, b); // qty
               
                 for(x=5;x<=7;++x)
                 {
                   generalUtils.dfs(fromBuf, (short)(x+1), b);
                   generalUtils.repAlpha(toBuf, 3000, (short)x, b);
                 }

                 generalUtils.dfs(fromBuf, (short)11, b); // amount2
                 generalUtils.repAlpha(toBuf, 3000, (short)10, b);
               
                 generalUtils.dfs(fromBuf, (short)12, b); // poRefNum
                 generalUtils.repAlpha(toBuf, 3000, (short)11, b);
               
                 generalUtils.dfs(fromBuf, (short)13, b); // UoM
                 generalUtils.repAlpha(toBuf, 3000, (short)12, b);
               
                 generalUtils.dfs(fromBuf, (short)14, b); // serialNumber
                 generalUtils.repAlpha(toBuf, 3000, (short)13, b);

                 generalUtils.dfs(fromBuf, (short)31, b);
                 generalUtils.repAlpha(toBuf, 3000, (short)14, b); // soLine

                 for(x=16;x<=22;++x)
                 {
                   generalUtils.dfs(fromBuf, (short)(x+1), b);
                   generalUtils.repAlpha(toBuf, 3000, (short)x, b);
                 }  
               
                 generalUtils.dfs(fromBuf, (short)32, b); // costPrice
                 if(b[0] == '\000')
                   generalUtils.strToBytes(b, "0.0");                   
                 generalUtils.repAlpha(toBuf, 3000, (short)24, b);

                 // other stuff

                 generalUtils.repAlpha(toBuf, 3000, (short)8, ""); // dlm
                 
                 generalUtils.repAlpha(toBuf, 3000, (short)9, ""); // unused1

                 generalUtils.repAlpha(toBuf, 3000, (short)15, "1970-01-01"); // unused3
                 
                 generalUtils.repAlpha(toBuf, 3000, (short)23, unm); // signon

                 generalUtils.dfs(fromBuf, (short)33, b); // manufacturer
                 generalUtils.repAlpha(toBuf, 3000, (short)25, b);

                 generalUtils.dfs(fromBuf, (short)34, b); // manufacturerCode
                 generalUtils.repAlpha(toBuf, 3000, (short)26, b);

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
