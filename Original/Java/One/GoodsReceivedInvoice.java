// =======================================================================================================================================================================================================
// System: ZaraStar Document: GRN to invoice
// Module: GoodsReceivedInvoice.java
// Author: C.K.Harvey
// Copyright (c) 2000-09 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
//
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;


import java.sql.*;

public class GoodsReceivedInvoice
{
  GeneralUtils generalUtils = new GeneralUtils();
  DocumentUtils documentUtils = new DocumentUtils();
  MiscDefinitions miscDefinitions = new MiscDefinitions();
  GoodsReceivedNote goodsReceivedNote = new GoodsReceivedNote();
  PurchaseInvoice purchaseInvoice = new PurchaseInvoice();
  Supplier supplier = new Supplier();
  AccountsUtils accountsUtils = new AccountsUtils();
  PurchaseOrder purchaseOrder = new PurchaseOrder();
  LocalPurchase localPurchase = new LocalPurchase();
  
 // --------------------------------------------------------------------------------------------------------------------------------------------------
 public boolean create(Connection con, Statement stmt, ResultSet rs, byte[] code, char renumberLines, char renumberEntries, int[] lines, int numLines, String unm, String dnm, String localDefnsDir, String defnsDir, byte[] newDocCode,
                       int[] bytesOut) throws Exception
 {
   documentUtils.getNextCode(con, stmt, rs, "pinvoice", true, newDocCode);

   generalUtils.toUpper(newDocCode, 0);

   byte[] data   = new byte[3000];
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
 
   if(purchaseInvoice.getRecGivenCode(con, stmt, rs, newDocCode, '\000', data, dnm, localDefnsDir, defnsDir, bytesOut) != -1) // just-in-case
   {
     generalUtils.strToBytes(newDocCode, "h0008.htm"); // already exists
     return false;
   }

   if(goodsReceivedNote.getRecGivenCode(con, stmt, rs, code, '\000', data, dnm, localDefnsDir, defnsDir, bytesOut) == -1)
   {
     generalUtils.strToBytes(newDocCode, "h0031.htm"); // not found
     return false;
   }

   grnToInvoice(con, stmt, rs, 'H', newDocCode, data, dataTo, unm, dnm, localDefnsDir, defnsDir, b);

   if(purchaseInvoice.putRecHeadGivenCode(con, stmt, rs, newDocCode, 'N', dataTo, dnm, localDefnsDir, defnsDir) != ' ')
   {
     generalUtils.strToBytes(newDocCode, "h0007.htm"); // not updated
     return false;
   }

   // fetch lines data in one go
   linesData = goodsReceivedNote.getLines(con, stmt, rs, code, '\000', linesData, listLen, linesCount, dnm, localDefnsDir, defnsDir);

   int entryCount = 0;
   for(int xx=0;xx<linesCount[0];++xx)
   {
     if(generalUtils.getListEntryByNum(xx, linesData, data)) // just-in-case
     {
       generalUtils.replaceTwosWithNulls(data);
                    
       // check this line
       thisLine = generalUtils.dfsAsInt(data, (short)11); // origin-1
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
         generalUtils.repAlpha(data, 3000, (short)11, newLines[z]);

         if(renumberEntries == 'Y')
           generalUtils.repAlpha(data, 3000, (short)12, ++entryCount);

         grnToInvoice(con, stmt, rs, 'L', newDocCode, data, dataTo, unm, dnm, localDefnsDir, defnsDir, b);
         purchaseInvoice.putRecLine(con, stmt, rs, newDocCode, null, 'N', dataTo, dnm, localDefnsDir, defnsDir);
       }
     }
   }  

   updateGRN(con, stmt, rs, generalUtils.stringFromBytes(code, 0L), generalUtils.stringFromBytes(newDocCode, 0L), dnm, localDefnsDir, defnsDir);
   
   return true;
 }

 // --------------------------------------------------------------------------------------------------------------------------------------------------
 private void grnToInvoice(Connection con, Statement stmt, ResultSet rs, char which, byte[] newDocCode, byte[] fromBuf, byte[] toBuf, String unm, String dnm, String localDefnsDir, String defnsDir, byte[] b) throws Exception
 {
   generalUtils.zeroize(toBuf, 3000);

   generalUtils.repAlpha(toBuf, 3000, (short)0, newDocCode);
   
   switch(which)
   {
     case 'H' : // head
                generalUtils.repAlpha(toBuf, 3000, (short)1, generalUtils.todaySQLFormat(localDefnsDir, defnsDir));

                // stuff from GRN
                
                String companyCode = generalUtils.dfsAsStr(fromBuf, (short)2);
                generalUtils.repAlpha(toBuf, 3000, (short)2, companyCode);
                
                generalUtils.dfs(fromBuf, (short)3, b); // companyName
                generalUtils.repAlpha(toBuf, 3000, (short)3, b);
                
                generalUtils.dfs(fromBuf, (short)4, b); // POCode
                generalUtils.repAlpha(toBuf, 3000, (short)25, b);
                
                // other stuff
                       
                // fetch address
                byte[] data = new byte[3000];
                if(supplier.getSupplierRecGivenCode(con, stmt, rs, companyCode, '\000', dnm, data, localDefnsDir, defnsDir) != -1) // just-in-case
                {
                  generalUtils.dfs(data, (short)2, b); // addr1
                  generalUtils.repAlpha(toBuf, 3000, (short)4, b);
                  generalUtils.dfs(data, (short)3, b); // addr2
                  generalUtils.repAlpha(toBuf, 3000, (short)5, b);
                  generalUtils.dfs(data, (short)4, b); // addr3
                  generalUtils.repAlpha(toBuf, 3000, (short)6, b);
                  generalUtils.dfs(data, (short)5, b); // addr4
                  generalUtils.repAlpha(toBuf, 3000, (short)7, b);
                  generalUtils.dfs(data, (short)6, b); // addr5
                  generalUtils.repAlpha(toBuf, 3000, (short)8, b);
                  generalUtils.dfs(data, (short)7, b); // postCode
                  generalUtils.repAlpha(toBuf, 3000, (short)9, b);
                }

                generalUtils.repAlpha(toBuf, 3000, (short)10, ""); // fao
                generalUtils.repAlpha(toBuf, 3000, (short)11, ""); // notes
                generalUtils.repAlpha(toBuf, 3000, (short)12, ""); // attention
                generalUtils.repAlpha(toBuf, 3000, (short)13, "1970-01-01"); // unused1
                generalUtils.repAlpha(toBuf, 3000, (short)14, "1970-01-01"); // dateprocessed
                generalUtils.repAlpha(toBuf, 3000, (short)15, ""); // misc1
                generalUtils.repAlpha(toBuf, 3000, (short)16, ""); // misc2
                generalUtils.repAlpha(toBuf, 3000, (short)17, "0.0"); // gstTotal
                generalUtils.repAlpha(toBuf, 3000, (short)18, "0.0"); // totalTotal
                generalUtils.repAlpha(toBuf, 3000, (short)19, "0.0"); // amountPaid
                generalUtils.repAlpha(toBuf, 3000, (short)20, ""); // settled
                generalUtils.repAlpha(toBuf, 3000, (short)21, ""); // processed
                generalUtils.repAlpha(toBuf, 3000, (short)22, unm); // signon
                generalUtils.repAlpha(toBuf, 3000, (short)23, "0.0"); // groupDiscount
                generalUtils.repAlpha(toBuf, 3000, (short)24, ""); // DORefNum

                generalUtils.repAlpha(toBuf, 3000, (short)26, "V"); // groupDiscountType
                generalUtils.repAlpha(toBuf, 3000, (short)27, "A"); // cashOrAccount
                generalUtils.repAlpha(toBuf, 3000, (short)28, ""); // dlm
                generalUtils.repAlpha(toBuf, 3000, (short)29, ""); // projectCode
                generalUtils.repAlpha(toBuf, 3000, (short)30, ""); // salesPerson
                generalUtils.repAlpha(toBuf, 3000, (short)31, "0"); // unused3
                generalUtils.repAlpha(toBuf, 3000, (short)32, "0.0"); // unused4
                generalUtils.repAlpha(toBuf, 3000, (short)33, "0.0"); // unused5
                generalUtils.repAlpha(toBuf, 3000, (short)34, ""); // commissionPaid
                generalUtils.repAlpha(toBuf, 3000, (short)35, "0.0"); // commissionValue
                generalUtils.repAlpha(toBuf, 3000, (short)36, ""); // unused6
                generalUtils.repAlpha(toBuf, 3000, (short)37, ""); // paymentReference
                generalUtils.repAlpha(toBuf, 3000, (short)38, ""); // locationCode
                generalUtils.repAlpha(toBuf, 3000, (short)39, "1970-01-01"); // datePaid
                generalUtils.repAlpha(toBuf, 3000, (short)40, ""); // shipAddrCode
                generalUtils.repAlpha(toBuf, 3000, (short)41, "0"); // printCount
                generalUtils.repAlpha(toBuf, 3000, (short)42, "L"); // status
                generalUtils.repAlpha(toBuf, 3000, (short)43, "1970-01-01"); // unused7
                generalUtils.repAlpha(toBuf, 3000, (short)44, "1970-01-01"); // unused8
                generalUtils.repAlpha(toBuf, 3000, (short)45, ""); // revisionOf
                generalUtils.repAlpha(toBuf, 3000, (short)46, ""); // suppInvoiceCode
                generalUtils.repAlpha(toBuf, 3000, (short)47, "0.0"); // basegstTotal
  
                generalUtils.repAlpha(toBuf, 3000, (short)48, supplier.getSupplierCurrencyGivenCode(con, stmt, rs, companyCode));
  
                generalUtils.repAlpha(toBuf, 3000, (short)49, "1.0"); // rate
                generalUtils.repAlpha(toBuf, 3000, (short)50, ""); // shipname
                generalUtils.repAlpha(toBuf, 3000, (short)51, ""); // shipaddr1
                generalUtils.repAlpha(toBuf, 3000, (short)52, ""); // shipaddr2
                generalUtils.repAlpha(toBuf, 3000, (short)53, ""); // shipaddr3
                generalUtils.repAlpha(toBuf, 3000, (short)54, ""); // shipaddr4
                generalUtils.repAlpha(toBuf, 3000, (short)55, ""); // shipaddr5
                generalUtils.repAlpha(toBuf, 3000, (short)56, "0.0"); // baseTotalTotal
                generalUtils.repAlpha(toBuf, 3000, (short)57, ""); // ocCode
                generalUtils.repAlpha(toBuf, 3000, (short)58, ""); // misc3
                generalUtils.repAlpha(toBuf, 3000, (short)59, ""); // misc4
                generalUtils.repAlpha(toBuf, 3000, (short)60, ""); // terms
                generalUtils.repAlpha(toBuf, 3000, (short)61, ""); // exportStatement
                generalUtils.repAlpha(toBuf, 3000, (short)62, ""); // closed
                generalUtils.repAlpha(toBuf, 3000, (short)63, ""); // reference
                
                break;
     case 'L' : // line
     
                // stuff from GRN
     
                generalUtils.dfs(fromBuf, (short)2, b); // itemCode
                generalUtils.repAlpha(toBuf, 3000, (short)1, b);

                generalUtils.dfs(fromBuf, (short)3, b); // desc
                generalUtils.repAlpha(toBuf, 3000, (short)2, b);

                generalUtils.dfs(fromBuf, (short)4, b); // unitPrice
                generalUtils.repAlpha(toBuf, 3000, (short)3, b);

                generalUtils.dfs(fromBuf, (short)5, b); // qty
                generalUtils.repAlpha(toBuf, 3000, (short)4, b);

                generalUtils.dfs(fromBuf, (short)6, b); // amount
                generalUtils.repAlpha(toBuf, 3000, (short)10, b);
                generalUtils.repAlpha(toBuf, 3000, (short)5, b);

                byte[] poCode = new byte[21];

                generalUtils.dfs(fromBuf, (short)9, poCode); // poRefNum
                generalUtils.repAlpha(toBuf, 3000, (short)25, poCode);

                generalUtils.dfs(fromBuf, (short)11, b); // line
                generalUtils.repAlpha(toBuf, 3000, (short)19, b);

                generalUtils.dfs(fromBuf, (short)12, b); // entry
                generalUtils.repAlpha(toBuf, 3000, (short)20, b);

                generalUtils.dfs(fromBuf, (short)18, b); // mfr
                generalUtils.repAlpha(toBuf, 3000, (short)27, b);

                generalUtils.dfs(fromBuf, (short)19, b); // mfrCode
                generalUtils.repAlpha(toBuf, 3000, (short)28, b);

                generalUtils.dfs(fromBuf, (short)1, b); // grCode
                generalUtils.repAlpha(toBuf, 3000, (short)29, b);

                generalUtils.dfs(fromBuf, (short)11, b); // grLine
                generalUtils.repAlpha(toBuf, 3000, (short)30, b);

                // other stuff

                generalUtils.repAlpha(toBuf, 3000, (short)6, accountsUtils.getDefaultGSTRate(con, stmt, rs, dnm, localDefnsDir, defnsDir));
                generalUtils.repAlpha(toBuf, 3000, (short)7, "0.0"); // discount
                
                generalUtils.repAlpha(toBuf, 3000, (short)8, ""); // dlm
                generalUtils.repAlpha(toBuf, 3000, (short)9, unm); // signon

                generalUtils.repAlpha(toBuf, 3000, (short)11, ""); // profitCentre
                generalUtils.repAlpha(toBuf, 3000, (short)12, ""); // accCode
                generalUtils.repAlpha(toBuf, 3000, (short)13, ""); // appliedStatus
                generalUtils.repAlpha(toBuf, 3000, (short)14, ""); // stockUpdated
                generalUtils.repAlpha(toBuf, 3000, (short)15, "1970-01-01"); // dateOfStockUpdate
                
                generalUtils.repAlpha(toBuf, 3000, (short)16, "0"); // qtyReqd
                generalUtils.repAlpha(toBuf, 3000, (short)17, ""); // custItemCode
                generalUtils.repAlpha(toBuf, 3000, (short)18, ""); // uom

                String poDate;
                poDate = purchaseOrder.getAPOFieldGivenCode(con, stmt, rs, "Date", generalUtils.stringFromBytes(poCode, 0L));
                if(poDate.length() == 0)
                  poDate = localPurchase.getAnLPFieldGivenCode(con, stmt, rs, "Date", generalUtils.stringFromBytes(poCode, 0L), dnm, localDefnsDir, defnsDir);

                if(poDate.length() == 0)
                  generalUtils.repAlpha(toBuf, 3000, (short)21, "1970-01-01");

                generalUtils.repAlpha(toBuf, 3000, (short)21, poDate); // poDate

                generalUtils.repAlpha(toBuf, 3000, (short)22, "0.0"); // receivedAmount
                generalUtils.repAlpha(toBuf, 3000, (short)23, "0.0"); // refundedAmount
                generalUtils.repAlpha(toBuf, 3000, (short)24, ""); // altItemCode
                
                generalUtils.repAlpha(toBuf, 3000, (short)26, ""); // doRefNum
                
                generalUtils.repAlpha(toBuf, 3000, (short)31, accountsUtils.getPurchasesAccCode(accountsUtils.getAccountingYearForADate(con, stmt, rs, generalUtils.today(localDefnsDir, defnsDir), dnm, localDefnsDir, defnsDir), dnm, localDefnsDir, defnsDir));
                break;
   }
 }

 // --------------------------------------------------------------------------------------------------------------------------------------------------  
 private void updateGRN(Connection con, Statement stmt, ResultSet rs, String grCode, String newInvoiceCode, String dnm, String localDefnsDir, String defnsDir) throws Exception
 {
   stmt = con.createStatement();
   stmt.executeUpdate("UPDATE grl SET ProcessedToPurchaseInvoice = 'Y' WHERE GRCode = '" + grCode + "'");
   if(stmt != null) stmt.close();
 }  

}
