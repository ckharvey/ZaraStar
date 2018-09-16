// =======================================================================================================================================================================================================
// System: ZaraStar Document: Inbox to quote
// Module: InboxToQuote.java
// Author: C.K.Harvey
// Copyright (c) 2000-08 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
//
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import java.sql.*;

public class InboxToQuote
{
  GeneralUtils generalUtils = new GeneralUtils();
  DocumentUtils documentUtils = new DocumentUtils();
  MiscDefinitions miscDefinitions = new MiscDefinitions();
  Inbox inbox = new Inbox();
  Quotation quotation = new Quotation();
  AccountsUtils accountsUtils = new AccountsUtils();
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean create(Connection con, Statement stmt, ResultSet rs, byte[] code, char renumberLines, int[] lines, int numLines, String unm,
                        String dnm, String localDefnsDir, String defnsDir, byte[] newDocCode, int[] bytesOut) throws Exception
  {
    documentUtils.getNextCode(con, stmt, rs, "quote", true, newDocCode);
    generalUtils.toUpper(newDocCode, 0);

    byte[][] data   = new byte[1][3000];
    int[] dUpto = new int[1];  dUpto[0] = 0;
    int[] dSize = new int[1];  dSize[0] = 3000;

    byte[] dataTo = new byte[3000];
    byte[] b      = new byte[300];
    int z, thisLine;
    boolean found;
    
    byte[] linesData  = new byte[2000];
    int[]  listLen    = new int[1];  listLen[0] = 2000;
    int[]  linesCount = new int[1];
 
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
  
    if(quotation.getRecGivenCode(con, stmt, rs, newDocCode, '\000', data[0], dnm, localDefnsDir, defnsDir, bytesOut) != -1) // just-in-case
    {
      generalUtils.strToBytes(newDocCode, "h0008.htm"); // already exists
      return false;
    }

    if(inbox.getRecGivenCode(con, stmt, rs, code, '\000', data, dnm, localDefnsDir, defnsDir, dUpto, dSize, bytesOut) == -1)
    {
      generalUtils.strToBytes(newDocCode, "h0031.htm"); // not found
      return false;
    }

    inboxToQuote(con, stmt, rs, 'H', newDocCode, data[0], dataTo, unm, dnm, localDefnsDir, defnsDir, b);

    if(quotation.putRecHeadGivenCode(con, stmt, rs, newDocCode, 'N', dataTo, dnm, localDefnsDir, defnsDir) != ' ')
    {
      generalUtils.strToBytes(newDocCode, "h0007.htm"); // not updated
      return false;
    }

    // fetch lines data in one go
    linesData = inbox.getLines(con, stmt, rs, code, '\000', linesData, listLen, linesCount, dnm, localDefnsDir, defnsDir);
    
    for(int xx=0;xx<linesCount[0];++xx)
    {
      if(generalUtils.getListEntryByNum(xx, linesData, data[0])) // just-in-case
      {
        generalUtils.replaceTwosWithNulls(data[0]);
                     
        // check this line
        thisLine = generalUtils.dfsAsInt(data[0], (short)2); // origin-1
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
          generalUtils.repAlpha(data[0], dSize[0], (short)2, newLines[z]);
          inboxToQuote(con, stmt, rs, 'L', newDocCode, data[0], dataTo, unm, dnm, localDefnsDir, defnsDir, b);
          quotation.putRecLine(con, stmt, rs, newDocCode, null, 'N', dataTo, dnm, localDefnsDir, defnsDir);
        }
      }
    }  

    updateInbox(con, stmt, generalUtils.stringFromBytes(code, 0L));

    return true;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void inboxToQuote(Connection con, Statement stmt, ResultSet rs, char which, byte[] newDocCode, byte[] fromBuf, byte[] toBuf, String unm,
                            String dnm, String localDefnsDir, String defnsDir, byte[] b) throws Exception
  {
    int x;
    double d;

    generalUtils.zeroize(toBuf, 3000);

    generalUtils.repAlpha(toBuf, 3000, (short)0, newDocCode);
    
    switch(which)
    {
      case 'H' : // head
                 generalUtils.repAlpha(toBuf, 3000, (short)1, generalUtils.todaySQLFormat(localDefnsDir, defnsDir));

                 // stuff from Inbox
          
                 generalUtils.dfs(fromBuf, (short)23, b); // companyCode
                 generalUtils.repAlpha(toBuf, 3000, (short)2, b);
                 
                 generalUtils.dfs(fromBuf, (short)2, b); // companyName
                 generalUtils.repAlpha(toBuf, 3000, (short)3, b);
                                 
                 for(x=14;x<=18;++x)
                 {
                   generalUtils.dfs(fromBuf, (short)x, b);
                   generalUtils.repAlpha(toBuf, 3000, (short)(x-10), b);
                 }

                 generalUtils.dfs(fromBuf, (short)3, b); // fao
                 generalUtils.repAlpha(toBuf, 3000, (short)10, b);

                 generalUtils.dfs(fromBuf, (short)21, b); // notes
                 generalUtils.repAlpha(toBuf, 3000, (short)11, b);

                 generalUtils.dfs(fromBuf, (short)7, b); // fax
                 generalUtils.repAlpha(toBuf, 3000, (short)50, b);

                 generalUtils.dfs(fromBuf, (short)13, b); // country
                 generalUtils.repAlpha(toBuf, 3000, (short)51, b);

                 generalUtils.dfs(fromBuf, (short)6, b); // phone
                 generalUtils.repAlpha(toBuf, 3000, (short)56, b);
                 
                 // other stuff
                 
                 generalUtils.repAlpha(toBuf, 3000, (short)12, ""); // status
                 
                 generalUtils.repAlpha(toBuf, 3000, (short)13, "1970-01-01"); // unused4
                 generalUtils.repAlpha(toBuf, 3000, (short)14, "1970-01-01"); // unused5

                 generalUtils.repAlpha(toBuf, 3000, (short)15, ""); // misc1
                 generalUtils.repAlpha(toBuf, 3000, (short)16, ""); // misc2

                 generalUtils.repAlpha(toBuf, 3000, (short)17, "0.0"); // gstTotal
                 generalUtils.repAlpha(toBuf, 3000, (short)18, "0.0"); // totalTotal
                 
                 generalUtils.repAlpha(toBuf, 3000, (short)19, ""); // salesPerson

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

                 generalUtils.repAlpha(toBuf, 3000, (short)33, "0.0"); // groupDiscount

                 generalUtils.repAlpha(toBuf, 3000, (short)34, ""); // dlm
                 generalUtils.repAlpha(toBuf, 3000, (short)35, unm); // signon
                 
                 generalUtils.repAlpha(toBuf, 3000, (short)36, ""); // groupDiscountType

                 generalUtils.repAlpha(toBuf, 3000, (short)37, ""); // revisionOf
                 generalUtils.repAlpha(toBuf, 3000, (short)38, ""); // projectCode
                 
                 generalUtils.dfs(fromBuf, (short)0, b); // inboxCode
                 generalUtils.repAlpha(toBuf, 3000, (short)39, b); // enquiryCode

                 generalUtils.repAlpha(toBuf, 3000, (short)41, ""); // unused3

                 generalUtils.repAlpha(toBuf, 3000, (short)42, ""); // currency
                 generalUtils.repAlpha(toBuf, 3000, (short)43, "1.0"); // rate
                 generalUtils.repAlpha(toBuf, 3000, (short)44, "0.0"); // baseTotalTotal
                 generalUtils.repAlpha(toBuf, 3000, (short)45, "0.0"); // baseGSTTotal
                 generalUtils.repAlpha(toBuf, 3000, (short)46, ""); // terms

                 generalUtils.repAlpha(toBuf, 3000, (short)47, ""); // validity
                 generalUtils.repAlpha(toBuf, 3000, (short)48, ""); // delivery
                 generalUtils.repAlpha(toBuf, 3000, (short)49, ""); // packaging

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
      
                 // stuff from Inbox
      
                 generalUtils.dfs(fromBuf, (short)3, b); // itemCode
                 generalUtils.repAlpha(toBuf, 3000, (short)1, b);

                 generalUtils.dfs(fromBuf, (short)9, b); // desc
                 generalUtils.repAlpha(toBuf, 3000, (short)2, b);
                 
                 generalUtils.dfs(fromBuf, (short)7, b); // unitPrice
                 generalUtils.repAlpha(toBuf, 3000, (short)3, b);

                 generalUtils.dfs(fromBuf, (short)5, b); // qty
                 generalUtils.repAlpha(toBuf, 3000, (short)4, b);
                 
                 d = generalUtils.dfsAsDouble(fromBuf, (short)7) * generalUtils.dfsAsDouble(fromBuf, (short)5);
                 generalUtils.doubleToChars('8', d, b, 0);
                 generalUtils.repAlpha(toBuf, 3000, (short)5, b); // amount
                 generalUtils.repAlpha(toBuf, 3000, (short)9, b); // amount2
                 
                 generalUtils.repAlpha(toBuf, 3000, (short)6, accountsUtils.getDefaultGSTRate(con, stmt, rs, dnm, localDefnsDir, defnsDir));

                 generalUtils.dfs(fromBuf, (short)2, b); // line
                 generalUtils.repAlpha(toBuf, 3000, (short)11, b);
                 generalUtils.repAlpha(toBuf, 3000, (short)12, b);
                 
                 generalUtils.dfs(fromBuf, (short)8, b); // uom
                 generalUtils.repAlpha(toBuf, 3000, (short)13, b);

                 generalUtils.dfs(fromBuf, (short)4, b); // mfr
                 generalUtils.repAlpha(toBuf, 3000, (short)19, b);

                 generalUtils.dfs(fromBuf, (short)12, b); // mfrCode
                 generalUtils.repAlpha(toBuf, 3000, (short)20, b);

                 // other stuff
                
                 generalUtils.repAlpha(toBuf, 3000, (short)7, "0.0"); // discount                 
                 generalUtils.repAlpha(toBuf, 3000, (short)8, ""); // dlm

                 generalUtils.repAlpha(toBuf, 3000, (short)10, "1970-01-01"); // deliveryDate

                 generalUtils.repAlpha(toBuf, 3000, (short)15, ""); // remark
                 generalUtils.repAlpha(toBuf, 3000, (short)16, ""); // store
                 
                 generalUtils.repAlpha(toBuf, 3000, (short)17, unm); // signon
                 generalUtils.repAlpha(toBuf, 3000, (short)18, "0.0"); // costPrice

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

  // -------------------------------------------------------------------------------------------------------------------------------------------------  
  private void updateInbox(Connection con, Statement stmt, String inboxCode) throws Exception
  {
    stmt = con.createStatement();
    stmt.executeUpdate("UPDATE inbox SET Processed = 'Y' WHERE InboxCode = '" + inboxCode + "'");
    if(stmt != null) stmt.close();
  }  

}
