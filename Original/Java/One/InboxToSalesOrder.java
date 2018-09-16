// =======================================================================================================================================================================================================
// System: ZaraStar Document: inbox to SO
// Module: InboxToSalesOrder.java
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

public class InboxToSalesOrder
{
  GeneralUtils  generalUtils = new GeneralUtils();
  DocumentUtils  documentUtils = new DocumentUtils();
  MiscDefinitions  miscDefinitions = new MiscDefinitions();
  Inbox  inbox = new Inbox();
  SalesOrder salesOrder = new SalesOrder();
  AccountsUtils  accountsUtils = new AccountsUtils();

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean create(Connection con, Statement stmt, ResultSet rs, byte[] code, char renumberLines, int[] lines, int numLines, String unm,
                        String dnm, String localDefnsDir, String defnsDir, byte[] newDocCode, int[] bytesOut) throws Exception
  {
    documentUtils.getNextCode(con, stmt, rs, "so", true, newDocCode);
    generalUtils.toUpper(newDocCode, 0);

    byte[][] data = new byte[1][3000];
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
      
    if(salesOrder.getRecGivenCode(con, stmt, rs, newDocCode, '\000', data[0], dnm, localDefnsDir, defnsDir, bytesOut) != -1) // just-in-case
    {
      generalUtils.strToBytes(newDocCode, "h0008.htm"); // already exists
      return false;
    }

    if(inbox.getRecGivenCode(con, stmt, rs, code, '\000', data, dnm, localDefnsDir, defnsDir, dUpto, dSize, bytesOut) == -1)
    {
      generalUtils.strToBytes(newDocCode, "h0031.htm"); // not found
      return false;
    }

    inboxToSO(con, stmt, rs, 'H', newDocCode, data[0], dataTo, unm, dnm, localDefnsDir, defnsDir, b);

    if(salesOrder.putRecHeadGivenCode(con, stmt, rs, newDocCode, 'N', dataTo, dnm, localDefnsDir, defnsDir) != ' ')
    {
      generalUtils.strToBytes(newDocCode, "h0007.htm"); // not created
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
          inboxToSO(con, stmt, rs, 'L', newDocCode, data[0], dataTo, unm, dnm, localDefnsDir, defnsDir, b);
          salesOrder.putRecLine(con, stmt, newDocCode, null, 'N', dataTo, dnm, localDefnsDir, defnsDir);
        }
      }
    }  

    updateInbox(con, stmt, generalUtils.stringFromBytes(code, 0L));

    return true;
 }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void inboxToSO(Connection con, Statement stmt, ResultSet rs, char which, byte[] newDocCode, byte[] fromBuf, byte[] toBuf, String unm, String dnm, String localDefnsDir, String defnsDir, byte[] b) throws Exception
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
                 
                 for(x=8;x<=12;++x)
                 {
                   generalUtils.dfs(fromBuf, (short)x, b);
                   generalUtils.repAlpha(toBuf, 3000, (short)(x+25), b);
                 }
                
                 // other Inbox stuff

                 generalUtils.repAlpha(toBuf, 3000, (short)9, ""); // postCode
                                  
                 generalUtils.repAlpha(toBuf, 3000, (short)12, ""); // unused1
                 generalUtils.repAlpha(toBuf, 3000, (short)13, ""); // unused2

                 generalUtils.repAlpha(toBuf, 3000, (short)14, ""); // misc1
                 generalUtils.repAlpha(toBuf, 3000, (short)15, ""); // misc2

                 generalUtils.repAlpha(toBuf, 3000, (short)16, "0.0"); // gstTotal
                 generalUtils.repAlpha(toBuf, 3000, (short)17, "0.0"); // totalTotal

                 generalUtils.repAlpha(toBuf, 3000, (short)18, "N"); // confirmationNotNeeded
                 generalUtils.repAlpha(toBuf, 3000, (short)19, "1970-01-01"); // unused4

                 generalUtils.repAlpha(toBuf, 3000, (short)20, "0.0"); // unused5
                 
                 generalUtils.repAlpha(toBuf, 3000, (short)21, "N"); // allSupplied
                 generalUtils.repAlpha(toBuf, 3000, (short)22, unm); // signon
                 generalUtils.repAlpha(toBuf, 3000, (short)23, ""); // dlm
                 generalUtils.repAlpha(toBuf, 3000, (short)24, ""); // locationCode
                 generalUtils.repAlpha(toBuf, 3000, (short)25, "L"); // status
                 generalUtils.repAlpha(toBuf, 3000, (short)26, ""); // shipaddrcode
                 
                 generalUtils.repAlpha(toBuf, 3000, (short)28, ""); // revisionOf
                 generalUtils.repAlpha(toBuf, 3000, (short)29, ""); // projectCode

                 generalUtils.repAlpha(toBuf, 3000, (short)30, "1.0"); // rate
                 generalUtils.repAlpha(toBuf, 3000, (short)31, ""); // currency

                 generalUtils.repAlpha(toBuf, 3000, (short)32, ""); // shipName
                 
                 generalUtils.repAlpha(toBuf, 3000, (short)38, ""); // salesPerson
                 
                 generalUtils.repAlpha(toBuf, 3000, (short)40, "0.0"); // baseTotalTotal
                 generalUtils.repAlpha(toBuf, 3000, (short)41, "0.0"); // baseGSTTotal
                 
                 generalUtils.repAlpha(toBuf, 3000, (short)42, ""); // unused7

                 generalUtils.repAlpha(toBuf, 3000, (short)43, "0.0"); // groupDiscount
                 generalUtils.repAlpha(toBuf, 3000, (short)44, ""); // groupDiscountType

                 generalUtils.repAlpha(toBuf, 3000, (short)44, ""); // terms
  
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
                 generalUtils.repAlpha(toBuf, 3000, (short)19, b); // amount2
                 
                 generalUtils.repAlpha(toBuf, 3000, (short)6, accountsUtils.getDefaultGSTRate(con, stmt, rs, dnm, localDefnsDir, defnsDir));
                 
                 generalUtils.dfs(fromBuf, (short)8, b); // uom
                 generalUtils.repAlpha(toBuf, 3000, (short)20, b);
                 
                 generalUtils.dfs(fromBuf, (short)2, b); // line
                 generalUtils.repAlpha(toBuf, 3000, (short)21, b);
                 generalUtils.repAlpha(toBuf, 3000, (short)22, b);
                 
                 generalUtils.dfs(fromBuf, (short)4, b); // mfr
                 generalUtils.repAlpha(toBuf, 3000, (short)29, b);

                 generalUtils.dfs(fromBuf, (short)12, b); // mfrCode
                 generalUtils.repAlpha(toBuf, 3000, (short)30, b);

                 // other stuff

                 generalUtils.repAlpha(toBuf, 3000, (short)7, "0.0"); // unused1                 
                 generalUtils.repAlpha(toBuf, 3000, (short)8, "0.0"); // unused2               
                 generalUtils.repAlpha(toBuf, 3000, (short)9, "1970-01-01"); // WIPOverride
                 
                 generalUtils.repAlpha(toBuf, 3000, (short)10, ""); // status

                 generalUtils.repAlpha(toBuf, 3000, (short)11, unm); // signon

                 generalUtils.repAlpha(toBuf, 3000, (short)12, ""); // dlm

                 generalUtils.repAlpha(toBuf, 3000, (short)13, ""); // unused9                 
                 generalUtils.repAlpha(toBuf, 3000, (short)14, ""); // unused4               
                 generalUtils.repAlpha(toBuf, 3000, (short)15, ""); // unused5                 
                 generalUtils.repAlpha(toBuf, 3000, (short)16, ""); // unused6                 
                 generalUtils.repAlpha(toBuf, 3000, (short)17, ""); // unused7               
                 generalUtils.repAlpha(toBuf, 3000, (short)18, ""); // unused8                 

                 generalUtils.repAlpha(toBuf, 3000, (short)23, "0.0"); // discount                 
                 generalUtils.repAlpha(toBuf, 3000, (short)24, "1970-01-01"); // deliveryDate                
                 generalUtils.repAlpha(toBuf, 3000, (short)25, ""); // remark            
                 generalUtils.repAlpha(toBuf, 3000, (short)26, ""); // custItemCode                 
                 generalUtils.repAlpha(toBuf, 3000, (short)27, ""); // store                 
                 generalUtils.repAlpha(toBuf, 3000, (short)28, "0.0"); // costPrice                 

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

  // -------------------------------------------------------------------------------------------------------------------------------------------------  
  private void updateInbox(Connection con, Statement stmt, String inboxCode) throws Exception
  {
    stmt = con.createStatement();
    stmt.executeUpdate("UPDATE inbox SET Processed = 'Y' WHERE InboxCode = '" + inboxCode + "'");
    if(stmt != null) stmt.close();
  }  

}
