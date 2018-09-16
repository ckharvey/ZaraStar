// =======================================================================================================================================================================================================
// System: ZaraStar Document: DO to invoice
// Module: DeliveryOrderToSalesInvoice.java
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

public class DeliveryOrderToSalesInvoice
{
  GeneralUtils  generalUtils  = new GeneralUtils();
  DocumentUtils  documentUtils  = new DocumentUtils();
  MiscDefinitions  miscDefinitions  = new MiscDefinitions();
  DocumentAttachmentsCopy documentAttachmentsCopy = new DocumentAttachmentsCopy();
  DeliveryOrder  deliveryOrder  = new DeliveryOrder();
  SalesInvoice  salesInvoice  = new SalesInvoice();
  AccountsUtils  accountsUtils = new AccountsUtils();
  
 // -------------------------------------------------------------------------------------------------------------------------------
 public boolean create(Connection con, Statement stmt, ResultSet rs, byte[] code, char renumberLines, char renumberEntries, int[] lines, int numLines, String unm, String dnm,
                       String localDefnsDir, String defnsDir, byte[] newDocCode, int[] bytesOut)// throws Exception
 {
    try
    {
      documentUtils.getNextCode(con, stmt, rs, "invoice", true, newDocCode);

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

      if(salesInvoice.getRecGivenCode(con, stmt, rs, newDocCode, '\000', data, dnm, localDefnsDir, defnsDir, bytesOut) != -1) // just-in-case
      {
        generalUtils.strToBytes(newDocCode, "h0008.htm"); // already exists
        return false;
      }

      if(deliveryOrder.getRecGivenCode(con, stmt, rs, code, '\000', data, dnm, localDefnsDir, defnsDir, bytesOut) == -1)
      {
        generalUtils.strToBytes(newDocCode, "h0031.htm"); // not found
        return false;
      }

      doToInvoice(con, stmt, rs, 'H', newDocCode, data, dataTo, unm, dnm, localDefnsDir, defnsDir, b);

      if(salesInvoice.putRecHeadGivenCode(con, stmt, rs, newDocCode, 'N', dataTo, dnm, localDefnsDir, defnsDir) != ' ')
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

            doToInvoice(con, stmt, rs, 'L', newDocCode, data, dataTo, unm, dnm, localDefnsDir, defnsDir, b);
            salesInvoice.putRecLine(con, stmt, rs, newDocCode, null, 'N', dataTo, dnm, localDefnsDir, defnsDir);
          }
        }
      }  

      if(linesCount[0] > 0) // get all the multiple lines for this document
      {
        multipleLinesData = deliveryOrder.getMultipleLines(con, stmt, rs, code, multipleLinesData, multipleListLen, multipleLinesCount, dnm, localDefnsDir, defnsDir);
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
            doToInvoice(con, stmt, rs, 'M', newDocCode, data, dataTo, unm, dnm, localDefnsDir, defnsDir, b);
            salesInvoice.putMultipleLine(con, stmt, rs, newDocCode, newLines[z], dataTo);
          }
        }
      }

      documentAttachmentsCopy.copyAttachments(con, stmt, rs, "doa", code, "invoicea", newDocCode, dnm, defnsDir, localDefnsDir);                 

      updateDO(con, stmt, rs, generalUtils.stringFromBytes(code, 0L), generalUtils.stringFromBytes(newDocCode, 0L), dnm, localDefnsDir, defnsDir);
   }
   catch(Exception e)
   {
     System.out.println("4070: " + e);
   }
   return true;
 }

 // --------------------------------------------------------------------------------------------------------------------------------------------------
 private void doToInvoice(Connection con, Statement stmt, ResultSet rs, char which, byte[] newDocCode, byte[] fromBuf, byte[] toBuf, String unm, String dnm, String localDefnsDir,
                          String defnsDir, byte[] b) throws Exception
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
                
                generalUtils.dfs(fromBuf, (short)21, b); // groupDiscount
                generalUtils.repAlpha(toBuf, 3000, (short)23, b);

                generalUtils.dfs(fromBuf, (short)0, b); // doRefNum
                generalUtils.repAlpha(toBuf, 3000, (short)24, b);

                generalUtils.dfs(fromBuf, (short)29, b); // poRefNum
                generalUtils.repAlpha(toBuf, 3000, (short)25, b);

                generalUtils.dfs(fromBuf, (short)24, b); // groupDiscountType
                generalUtils.repAlpha(toBuf, 3000, (short)26, b);
                
                generalUtils.dfs(fromBuf, (short)26, b); // projectCode
                generalUtils.repAlpha(toBuf, 3000, (short)29, b);
                
                generalUtils.dfs(fromBuf, (short)30, b); // salesPerson
                generalUtils.repAlpha(toBuf, 3000, (short)30, b);
                
                generalUtils.dfs(fromBuf, (short)23, b); // locationCode
                generalUtils.repAlpha(toBuf, 3000, (short)38, b);
                
                generalUtils.dfs(fromBuf, (short)27, b); // shipAddrCode
                generalUtils.repAlpha(toBuf, 3000, (short)40, b);
                
                generalUtils.dfs(fromBuf, (short)45, b); // baseGSTTotal
                generalUtils.repAlpha(toBuf, 3000, (short)47, b);

                generalUtils.dfs(fromBuf, (short)39, b); // currency
                generalUtils.repAlpha(toBuf, 3000, (short)48, b);

                generalUtils.dfs(fromBuf, (short)41, b); // rate
                generalUtils.repAlpha(toBuf, 3000, (short)49, b);
                
                generalUtils.dfs(fromBuf, (short)44, b); // baseTotalTotal
                generalUtils.repAlpha(toBuf, 3000, (short)56, b);
                
                generalUtils.dfs(fromBuf, (short)46, b); // terms
                generalUtils.repAlpha(toBuf, 3000, (short)60, b);
                
                generalUtils.dfs(fromBuf, (short)43, b); // plCode
                generalUtils.repAlpha(toBuf, 3000, (short)57, b);
                
                generalUtils.dfs(fromBuf, (short)46, b); // exportStatement
                generalUtils.repAlpha(toBuf, 3000, (short)61, b);

                // other stuff
                
                generalUtils.repAlpha(toBuf, 3000, (short)12, ""); // attention

                generalUtils.repAlpha(toBuf, 3000, (short)13, "1970-01-01"); // unused1
                generalUtils.repAlpha(toBuf, 3000, (short)14, "1970-01-01"); // dateprocessed

                generalUtils.repAlpha(toBuf, 3000, (short)15, ""); // misc1
                generalUtils.repAlpha(toBuf, 3000, (short)16, ""); // misc2

                generalUtils.repAlpha(toBuf, 3000, (short)19, "0.0"); // amountPaid

                generalUtils.repAlpha(toBuf, 3000, (short)20, ""); // settled
                generalUtils.repAlpha(toBuf, 3000, (short)21, ""); // processed
                
                generalUtils.repAlpha(toBuf, 3000, (short)22, unm); // signon

                generalUtils.repAlpha(toBuf, 3000, (short)27, "A"); // cashOrAccount
                
                generalUtils.repAlpha(toBuf, 3000, (short)28, ""); // dlm

                generalUtils.repAlpha(toBuf, 3000, (short)31, "0"); // unused3
                generalUtils.repAlpha(toBuf, 3000, (short)32, "0.0"); // unused4
                generalUtils.repAlpha(toBuf, 3000, (short)33, "0.0"); // unused5

                generalUtils.repAlpha(toBuf, 3000, (short)34, ""); // commissionPaid
                generalUtils.repAlpha(toBuf, 3000, (short)35, "0.0"); // commissionValue
                
                generalUtils.repAlpha(toBuf, 3000, (short)36, ""); // unused6
                generalUtils.repAlpha(toBuf, 3000, (short)37, ""); // paymentReference

                generalUtils.repAlpha(toBuf, 3000, (short)39, "1970-01-01"); // datePaid

                generalUtils.repAlpha(toBuf, 3000, (short)41, "0"); // printCount
                generalUtils.repAlpha(toBuf, 3000, (short)42, "L"); // status

                generalUtils.repAlpha(toBuf, 3000, (short)43, "1970-01-01"); // unused7
                generalUtils.repAlpha(toBuf, 3000, (short)44, "1970-01-01"); // unused8

                generalUtils.repAlpha(toBuf, 3000, (short)45, ""); // revisionOf

                generalUtils.repAlpha(toBuf, 3000, (short)46, ""); // unused9

                generalUtils.repAlpha(toBuf, 3000, (short)58, ""); // misc3
                generalUtils.repAlpha(toBuf, 3000, (short)59, ""); // misc4

                generalUtils.repAlpha(toBuf, 3000, (short)62, ""); // closed
                generalUtils.repAlpha(toBuf, 3000, (short)63, ""); // reference
                
                 break;
     case 'L' : // line
     
                // stuff from DO
     
                for(x=1;x<=7;++x)
                {
                  generalUtils.dfs(fromBuf, (short)(x+1), b);
                  generalUtils.repAlpha(toBuf, 3000, (short)x, b);
                }

                generalUtils.dfs(fromBuf, (short)11, b); // amount2
                generalUtils.repAlpha(toBuf, 3000, (short)10, b);

                generalUtils.dfs(fromBuf, (short)13, b); // uom
                generalUtils.repAlpha(toBuf, 3000, (short)18, b);

                generalUtils.dfs(fromBuf, (short)19, b); // line
                generalUtils.repAlpha(toBuf, 3000, (short)19, b);

                generalUtils.dfs(fromBuf, (short)20, b); // entry
                generalUtils.repAlpha(toBuf, 3000, (short)20, b);

                generalUtils.dfs(fromBuf, (short)12, b); // poRefNum
                generalUtils.repAlpha(toBuf, 3000, (short)25, b);
                
                generalUtils.dfs(fromBuf, (short)1, b); // doRefNum
                generalUtils.repAlpha(toBuf, 3000, (short)26, b);
                
                generalUtils.dfs(fromBuf, (short)21, b); // soCode
                generalUtils.repAlpha(toBuf, 3000, (short)11, b);

                generalUtils.dfs(fromBuf, (short)25, b); // costPrice
                if(b[0] == '\000')
                  generalUtils.strToBytes(b, "0.0");                   
                generalUtils.repAlpha(toBuf, 3000, (short)27, b);
                
                // other stuff
                
                generalUtils.repAlpha(toBuf, 3000, (short)8, ""); // dlm
                
                generalUtils.repAlpha(toBuf, 3000, (short)9, unm); // signon

                generalUtils.repAlpha(toBuf, 3000, (short)12, ""); // accCode
                generalUtils.repAlpha(toBuf, 3000, (short)13, "N"); // hide
                generalUtils.repAlpha(toBuf, 3000, (short)14, ""); // stockUpdated
                generalUtils.repAlpha(toBuf, 3000, (short)15, "1970-01-01"); // dateOfStockUpdate
                
                generalUtils.repAlpha(toBuf, 3000, (short)16, "0"); // qtyReqd
                generalUtils.repAlpha(toBuf, 3000, (short)17, ""); // custItemCode

                generalUtils.repAlpha(toBuf, 3000, (short)22, "0.0"); // receivedAmount
                generalUtils.repAlpha(toBuf, 3000, (short)23, "0.0"); // refundedAmount
                generalUtils.repAlpha(toBuf, 3000, (short)24, ""); // altItemCode

                generalUtils.dfs(fromBuf, (short)26, b); // manufacturer
                generalUtils.repAlpha(toBuf, 3000, (short)28, b);

                generalUtils.dfs(fromBuf, (short)27, b); // manufacturerCode
                generalUtils.repAlpha(toBuf, 3000, (short)29, b);

                generalUtils.repAlpha(toBuf, 3000, (short)21, accountsUtils.getSalesAccCode(accountsUtils.getAccountingYearForADate(con, stmt, rs, generalUtils.today(localDefnsDir, defnsDir),
                                                                                                             dnm, localDefnsDir, defnsDir),
                                                                             dnm, localDefnsDir, defnsDir));

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
 private void updateDO(Connection con, Statement stmt, ResultSet rs, String doCode, String newInvoiceCode, String dnm, String localDefnsDir, String defnsDir) throws Exception
 {
   stmt = con.createStatement();
   stmt.executeUpdate("UPDATE do SET InvoiceCode = '" + newInvoiceCode + "' WHERE DOCode = '" + doCode + "'");
   if(stmt != null) stmt.close();
 }  

}
