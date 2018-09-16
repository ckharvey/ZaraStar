// =======================================================================================================================================================================================================
// System: ZaraStar Document: DO to quote
// Module: DeliveryOrderToQuotation.java
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

public class DeliveryOrderToQuotation
{
  GeneralUtils  generalUtils  = new GeneralUtils();
  DocumentUtils  documentUtils  = new DocumentUtils();
  MiscDefinitions  miscDefinitions  = new MiscDefinitions();
  DocumentAttachmentsCopy documentAttachmentsCopy = new DocumentAttachmentsCopy();
  DeliveryOrder  deliveryOrder  = new DeliveryOrder();
  Quotation  quotation  = new Quotation();
  ServerUtils serverUtils = new ServerUtils();
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean create(Connection con, Statement stmt, ResultSet rs, byte[] code, char renumberLines, char renumberEntries, int[] lines, int numLines, String unm, String dnm, String localDefnsDir, String defnsDir, byte[] newDocCode,
                        int[] bytesOut) throws Exception
  {
    documentUtils.getNextCode(con, stmt, rs, "quote", true, newDocCode);
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
  
    if(quotation.getRecGivenCode(con, stmt, rs, newDocCode, '\000', data, dnm, localDefnsDir, defnsDir, bytesOut) != -1) // just-in-case
    {
      generalUtils.strToBytes(newDocCode, "h0008.htm"); // already exists
      return false;
    }

    if(deliveryOrder.getRecGivenCode(con, stmt, rs, code, '\000', data, dnm, localDefnsDir, defnsDir, bytesOut) == -1)
    {
      generalUtils.strToBytes(newDocCode, "h0031.htm"); // not found
      return false;
    }

    
    String customerCode = generalUtils.dfsAsStr(data, (short)2);

    
    doToQuote('H', newDocCode, data, dataTo, unm, localDefnsDir, defnsDir, b);

    if(quotation.putRecHeadGivenCode(con, stmt, rs, newDocCode, 'N', dataTo, dnm, localDefnsDir, defnsDir) != ' ')
    {
      generalUtils.strToBytes(newDocCode, "h0007.htm"); // not updated
      return false;
    }

    // fetch lines data in one go
    linesData = deliveryOrder.getLines(con, stmt, rs, code, '\000', linesData, listLen, linesCount, dnm, localDefnsDir, defnsDir);
    
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

          doToQuote('L', newDocCode, data, dataTo, unm, localDefnsDir, defnsDir, b);
          quotation.putRecLine(con, stmt, rs, newDocCode, null, 'N', dataTo, dnm, localDefnsDir, defnsDir);
        }
      }
    }  

    if(linesCount[0] > 0) // get all the multiple lines for this document
    {
      multipleLinesData = deliveryOrder.getMultipleLines(con, stmt, rs, code, multipleLinesData, multipleListLen, multipleLinesCount, dnm,
                                                 localDefnsDir, defnsDir);
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
          doToQuote('M', newDocCode, data, dataTo, unm, localDefnsDir, defnsDir, b);
          quotation.putMultipleLine(con, stmt, rs, newDocCode, newLines[z], dataTo, dnm, localDefnsDir, defnsDir);
        }
      }
    }

    documentAttachmentsCopy.copyAttachments(con, stmt, rs, "doa", code, "quotea", newDocCode, dnm, defnsDir, localDefnsDir);                 

    
              serverUtils.syncToIW(con, generalUtils.stringFromBytes(newDocCode, 0L), "", "", 'Q', generalUtils.todaySQLFormat(localDefnsDir, defnsDir), unm, customerCode, dnm);

    return true;
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void doToQuote(char which, byte[] newDocCode, byte[] fromBuf, byte[] toBuf, String unm, String localDefnsDir, String defnsDir, byte[] b) throws Exception
  {
    int x;

    generalUtils.zeroize(toBuf, 3000);

    generalUtils.repAlpha(toBuf, 3000, (short)0, newDocCode);
    
    switch(which)
    {
      case 'H' : // head
                 generalUtils.repAlpha(toBuf, 3000, (short)1, generalUtils.todaySQLFormat(localDefnsDir, defnsDir));

                 // stuff from DO
                 
                 for(x=2;x<=11;++x)
                 {
                   generalUtils.dfs(fromBuf, (short)x, b);
                   generalUtils.repAlpha(toBuf, 3000, (short)x, b);
                 }

                 generalUtils.dfs(fromBuf, (short)17, b); // gstTotal
                 generalUtils.repAlpha(toBuf, 3000, (short)17, b);

                 generalUtils.dfs(fromBuf, (short)18, b); // totalTotal
                 generalUtils.repAlpha(toBuf, 3000, (short)18, b);

                 generalUtils.dfs(fromBuf, (short)30, b); // salesPerson
                 generalUtils.repAlpha(toBuf, 3000, (short)19, b);

                 generalUtils.dfs(fromBuf, (short)21, b); // groupDiscount
                 generalUtils.repAlpha(toBuf, 3000, (short)33, b);

                 generalUtils.dfs(fromBuf, (short)24, b); // groupDiscountType
                 generalUtils.repAlpha(toBuf, 3000, (short)36, b);

                 generalUtils.dfs(fromBuf, (short)26, b); // projectCode
                 generalUtils.repAlpha(toBuf, 3000, (short)38, b);

                 generalUtils.dfs(fromBuf, (short)23, b); // locationCode
                 generalUtils.repAlpha(toBuf, 3000, (short)40, b);

                 generalUtils.dfs(fromBuf, (short)49, b); // buyeremail
                 generalUtils.repAlpha(toBuf, 3000, (short)41, b);                 

                 generalUtils.dfs(fromBuf, (short)39, b); // currency
                 generalUtils.repAlpha(toBuf, 3000, (short)42, b);

                 generalUtils.dfs(fromBuf, (short)41, b); // rate
                 generalUtils.repAlpha(toBuf, 3000, (short)43, b);

                 generalUtils.dfs(fromBuf, (short)44, b); // baseTotalTotal
                 generalUtils.repAlpha(toBuf, 3000, (short)44, b);

                 generalUtils.dfs(fromBuf, (short)45, b); // baseGSTTotal
                 generalUtils.repAlpha(toBuf, 3000, (short)45, b);

                 generalUtils.dfs(fromBuf, (short)46, b); // terms
                 generalUtils.repAlpha(toBuf, 3000, (short)46, b);

                 // other stuff
                 
                 generalUtils.repAlpha(toBuf, 3000, (short)12, ""); // status
                 
                 generalUtils.repAlpha(toBuf, 3000, (short)13, "1970-01-01"); // unused4
                 generalUtils.repAlpha(toBuf, 3000, (short)14, "1970-01-01"); // unused5

                 generalUtils.repAlpha(toBuf, 3000, (short)15, ""); // misc1
                 generalUtils.repAlpha(toBuf, 3000, (short)16, ""); // misc2

                 generalUtils.repAlpha(toBuf, 3000, (short)20, ""); // likelihoodrating
                 generalUtils.repAlpha(toBuf, 3000, (short)21, ""); // quoteStatus
                 generalUtils.repAlpha(toBuf, 3000, (short)22, ""); // wonOrLost

                 generalUtils.repAlpha(toBuf, 3000, (short)23, ""); // reason1
                 generalUtils.repAlpha(toBuf, 3000, (short)24, ""); // reason2
                 generalUtils.repAlpha(toBuf, 3000, (short)25, ""); // reason3
                 generalUtils.repAlpha(toBuf, 3000, (short)26, ""); // reason4
                 generalUtils.repAlpha(toBuf, 3000, (short)27, ""); // reason5
                 generalUtils.repAlpha(toBuf, 3000, (short)28, ""); // reason6
                 generalUtils.repAlpha(toBuf, 3000, (short)29, ""); // reason7
                 generalUtils.repAlpha(toBuf, 3000, (short)30, ""); // reason8
                 generalUtils.repAlpha(toBuf, 3000, (short)31, ""); // reason9
                 generalUtils.repAlpha(toBuf, 3000, (short)32, ""); // reason10

                 generalUtils.repAlpha(toBuf, 3000, (short)34, ""); // dlm
                 generalUtils.repAlpha(toBuf, 3000, (short)35, unm); // signon
                 
                 generalUtils.repAlpha(toBuf, 3000, (short)37, ""); // revisionOf
                 generalUtils.repAlpha(toBuf, 3000, (short)39, ""); // enquiryCode

                 generalUtils.repAlpha(toBuf, 3000, (short)47, ""); // validity
                 generalUtils.repAlpha(toBuf, 3000, (short)48, ""); // delivery
                 generalUtils.repAlpha(toBuf, 3000, (short)49, ""); // packaging
                 generalUtils.repAlpha(toBuf, 3000, (short)50, ""); // fax
                 generalUtils.repAlpha(toBuf, 3000, (short)51, ""); // country

                 generalUtils.repAlpha(toBuf, 3000, (short)52, "L"); // documentStatus
                 
                 generalUtils.repAlpha(toBuf, 3000, (short)53, ""); // manufacturer
                 generalUtils.repAlpha(toBuf, 3000, (short)54, ""); // remarkType
                 generalUtils.repAlpha(toBuf, 3000, (short)55, ""); // deliveryLeadTime
                 generalUtils.repAlpha(toBuf, 3000, (short)56, ""); // phone
                 
                 generalUtils.repAlpha(toBuf, 3000, (short)57, "N"); // ToEngineering
                 generalUtils.repAlpha(toBuf, 3000, (short)58, "1970-01-01");
                 generalUtils.repAlpha(toBuf, 3000, (short)59, ""); 
                 generalUtils.repAlpha(toBuf, 3000, (short)60, "N"); // ToProcurement
                 generalUtils.repAlpha(toBuf, 3000, (short)61, "1970-01-01");
                 generalUtils.repAlpha(toBuf, 3000, (short)62, ""); 
                 generalUtils.repAlpha(toBuf, 3000, (short)63, "N"); // ToScheduling
                 generalUtils.repAlpha(toBuf, 3000, (short)64, "1970-01-01");
                 generalUtils.repAlpha(toBuf, 3000, (short)65, ""); 
                 generalUtils.repAlpha(toBuf, 3000, (short)66, "N"); // ToManager
                 generalUtils.repAlpha(toBuf, 3000, (short)67, "1970-01-01");
                 generalUtils.repAlpha(toBuf, 3000, (short)68, ""); 
                 generalUtils.repAlpha(toBuf, 3000, (short)69, "N"); // EngineeringApproved
                 generalUtils.repAlpha(toBuf, 3000, (short)70, "1970-01-01");
                 generalUtils.repAlpha(toBuf, 3000, (short)71, ""); 
                 generalUtils.repAlpha(toBuf, 3000, (short)72, "N"); // ProcurementConfirmed
                 generalUtils.repAlpha(toBuf, 3000, (short)73, "1970-01-01");
                 generalUtils.repAlpha(toBuf, 3000, (short)74, ""); 
                 generalUtils.repAlpha(toBuf, 3000, (short)75, "N"); // SchedulingConfirmed
                 generalUtils.repAlpha(toBuf, 3000, (short)76, "1970-01-01");
                 generalUtils.repAlpha(toBuf, 3000, (short)77, ""); 
                 generalUtils.repAlpha(toBuf, 3000, (short)78, "N"); // ManagerApproved
                 generalUtils.repAlpha(toBuf, 3000, (short)79, "1970-01-01");
                 generalUtils.repAlpha(toBuf, 3000, (short)80, ""); 
                 generalUtils.repAlpha(toBuf, 3000, (short)81, "N"); // QuoteSent
                 generalUtils.repAlpha(toBuf, 3000, (short)82, "1970-01-01");
                 generalUtils.repAlpha(toBuf, 3000, (short)83, ""); 

                 break;
      case 'L' : // line
      
                 // stuff from DO
      
                 for(x=1;x<=7;++x)
                 {
                   generalUtils.dfs(fromBuf, (short)(x+1), b);
                   generalUtils.repAlpha(toBuf, 3000, (short)x, b);
                 }

                 generalUtils.dfs(fromBuf, (short)11, b); // amount2
                 generalUtils.repAlpha(toBuf, 3000, (short)9, b);

                 generalUtils.dfs(fromBuf, (short)19, b); // line
                 generalUtils.repAlpha(toBuf, 3000, (short)11, b);
                 
                 generalUtils.dfs(fromBuf, (short)20, b); // entry
                 generalUtils.repAlpha(toBuf, 3000, (short)12, b);

                 generalUtils.dfs(fromBuf, (short)13, b); // uom
                 generalUtils.repAlpha(toBuf, 3000, (short)13, b);

                 generalUtils.dfs(fromBuf, (short)18, b); // custItemCode
                 generalUtils.repAlpha(toBuf, 3000, (short)14, b);
                 
                 generalUtils.dfs(fromBuf, (short)25, b); // costPrice
                 if(b[0] == '\000')
                   generalUtils.strToBytes(b, "0.0");                   
                 generalUtils.repAlpha(toBuf, 3000, (short)18, b);
                 
                 // other stuff
                 
                 generalUtils.repAlpha(toBuf, 3000, (short)8, ""); // dlm

                 generalUtils.repAlpha(toBuf, 3000, (short)10, "1970-01-01"); // deliveryDate

                 generalUtils.repAlpha(toBuf, 3000, (short)15, ""); // remark
                 generalUtils.repAlpha(toBuf, 3000, (short)16, ""); // store
                 
                 generalUtils.repAlpha(toBuf, 3000, (short)17, unm); // signon

                 generalUtils.dfs(fromBuf, (short)26, b); // manufacturer
                 generalUtils.repAlpha(toBuf, 3000, (short)19, b);

                 generalUtils.dfs(fromBuf, (short)27, b); // manufacturerCode
                 generalUtils.repAlpha(toBuf, 3000, (short)20, b);

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
