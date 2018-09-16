// =======================================================================================================================================================================================================
// System: ZaraStar Document: SO to LP
// Module: SalesOrderToLocalPurchase.java
// Author: C.K.Harvey
// Copyright (c) 2000-07 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import java.sql.*;

public class SalesOrderToLocalPurchase
{
  GeneralUtils  generalUtils  = new GeneralUtils();
  DocumentUtils  documentUtils  = new DocumentUtils();
  MiscDefinitions  miscDefinitions  = new MiscDefinitions();
  DocumentAttachmentsCopy documentAttachmentsCopy = new DocumentAttachmentsCopy();
  LocalPurchase  localPurchase  = new LocalPurchase();
  SalesOrder  salesOrder  = new SalesOrder();
  AccountsUtils  accountsUtils  = new AccountsUtils();
  Supplier  supplier  = new Supplier();
  Inventory  inventory  = new Inventory();

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean create(Connection con, Statement stmt, ResultSet rs, byte[] code, char renumberLines, char renumberEntries, int[] lines, int numLines, String quantitiesReqd, String unm, String dnm, String localDefnsDir, String defnsDir,
                        byte[] newDocCode, int[] bytesOut) throws Exception
  {
    documentUtils.getNextCode(con, stmt, rs, "lp", true, newDocCode);
    generalUtils.toUpper(newDocCode, 0);

    byte[] itemCode = new byte[21];
    byte[] data     = new byte[3000];
    byte[] data2    = new byte[3000];
    byte[] dataTo   = new byte[3000];
    byte[] b        = new byte[300];
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

    if(localPurchase.getRecGivenCode(con, stmt, rs, newDocCode, '\000', data, dnm, localDefnsDir, defnsDir, bytesOut) != -1) // just-in-case
    {
      generalUtils.strToBytes(newDocCode, "h0008.htm"); // already exists
      return false;
    }

    if(salesOrder.getRecGivenCode(con, stmt, rs, code, '\000', data, dnm, localDefnsDir, defnsDir, bytesOut) == -1)
    {
      generalUtils.strToBytes(newDocCode, "h0031.htm"); // not found
      return false;
    }

    double rate = generalUtils.dfsAsDouble(data, (short)30);

    soToLP(con, stmt, rs, 'H', newDocCode, data, dataTo, null, 0, "", rate, unm, dnm, localDefnsDir, defnsDir, b, itemCode, data2);

    if(localPurchase.putRecHeadGivenCode(con, stmt, rs, newDocCode, 'N', dataTo, dnm, localDefnsDir, defnsDir) != ' ')
    {
      generalUtils.strToBytes(newDocCode, "h0007.htm"); // not updated
      return false;
    }

    // fetch lines data in one go
    linesData = salesOrder.getLines(con, stmt, rs, code, '\000', linesData, listLen, linesCount, dnm, localDefnsDir, defnsDir);

    int entryCount = 0;
    String qtyReqd;
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

          generalUtils.repAlpha(data, 3000, (short)22, newLines[z]);

          if(renumberEntries == 'Y')
            generalUtils.repAlpha(data, 3000, (short)23, ++entryCount);

          soToLP(con, stmt, rs, 'L', newDocCode, data, dataTo, code, thisLine, qtyReqd, rate, unm, dnm, localDefnsDir, defnsDir, b, itemCode, data2);
          localPurchase.putRecLine(con, stmt, rs, newDocCode, null, 'N', dataTo, dnm, localDefnsDir, defnsDir);
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
          soToLP(con, stmt, rs, 'M', newDocCode, data, dataTo, null, 0, "", rate, unm, dnm, localDefnsDir, defnsDir, b, itemCode, data2);
          localPurchase.putMultipleLine(con, stmt, rs, newDocCode, newLines[z], dataTo, dnm, localDefnsDir, defnsDir);
        }
      }
    }

    documentAttachmentsCopy.copyAttachments(con, stmt, rs, "soa", code, "lpa", newDocCode, dnm, defnsDir, localDefnsDir);

    return true;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void soToLP(Connection con, Statement stmt, ResultSet rs, char which, byte[] newDocCode, byte[] fromBuf, byte[] toBuf, byte[] soCode,
          int soLine, String qtyReqd, double rate, String unm, String dnm, String localDefnsDir, String defnsDir, byte[] b,
                      byte[] itemCode, byte[] data2) throws Exception
  {
    int x;
    double d;
    double unitPrice, discount;
    String[] amount  = new String[1];
    String[] amount2 = new String[1];

    generalUtils.zeroize(toBuf, 3000);

    generalUtils.repAlpha(toBuf, 3000, (short)0, newDocCode);

    switch(which)
    {
      case 'H' : // head
                 generalUtils.repAlpha(toBuf, 3000, (short)1, generalUtils.todaySQLFormat(localDefnsDir, defnsDir));

                 generalUtils.repAlpha(toBuf, 3000, (short)2, "-"); // suppcode

                 // stuff from SO

                 generalUtils.dfs(fromBuf, (short)29, b); // projectCode
                 generalUtils.repAlpha(toBuf, 3000, (short)29, b);

                 generalUtils.dfs(fromBuf, (short)24, b); // locationCode
                 generalUtils.repAlpha(toBuf, 3000, (short)27, b);

                 generalUtils.dfs(fromBuf, (short)38, b); // salesPerson
                 generalUtils.repAlpha(toBuf, 3000, (short)41, b);

                 // other PO stuff

                 generalUtils.repAlpha(toBuf, 3000, (short)12, ""); // unused1
                 generalUtils.repAlpha(toBuf, 3000, (short)13, ""); // unused2

                 generalUtils.repAlpha(toBuf, 3000, (short)14, ""); // misc1
                 generalUtils.repAlpha(toBuf, 3000, (short)15, ""); // misc2

                 generalUtils.repAlpha(toBuf, 3000, (short)16, "0.0"); // gstTotal
                 generalUtils.repAlpha(toBuf, 3000, (short)17, "0.0"); // totalTotal

                 generalUtils.repAlpha(toBuf, 3000, (short)18, "1970-01-01"); // unused3
                 generalUtils.repAlpha(toBuf, 3000, (short)19, "1970-01-01"); // unused4

                 generalUtils.repAlpha(toBuf, 3000, (short)20, "0.0"); // unused5

                 generalUtils.repAlpha(toBuf, 3000, (short)21, "N"); // allsupplied

                 generalUtils.repAlpha(toBuf, 3000, (short)22, unm); // signon

                 generalUtils.repAlpha(toBuf, 3000, (short)23, ""); // dlm

                 generalUtils.repAlpha(toBuf, 3000, (short)24, ""); // unused8

                 generalUtils.repAlpha(toBuf, 3000, (short)25, "L"); // status

                 generalUtils.repAlpha(toBuf, 3000, (short)26, ""); // shipAddrCode

                 generalUtils.repAlpha(toBuf, 3000, (short)28, ""); // storeCode

                 generalUtils.repAlpha(toBuf, 3000, (short)30, "1.0"); // rate

                 generalUtils.repAlpha(toBuf, 3000, (short)31, accountsUtils.getBaseCurrency(con, stmt, rs, dnm, localDefnsDir, defnsDir)); // currency

                 generalUtils.repAlpha(toBuf, 3000, (short)32, ""); // shipName
                 generalUtils.repAlpha(toBuf, 3000, (short)33, ""); // shipAddr1
                 generalUtils.repAlpha(toBuf, 3000, (short)34, ""); // shipAddr2
                 generalUtils.repAlpha(toBuf, 3000, (short)35, ""); // shipAddr3
                 generalUtils.repAlpha(toBuf, 3000, (short)36, ""); // shipAddr4
                 generalUtils.repAlpha(toBuf, 3000, (short)37, ""); // shipAddr5

                 generalUtils.repAlpha(toBuf, 3000, (short)38, ""); // revisionOf

                 generalUtils.repAlpha(toBuf, 3000, (short)39, "0.0"); // baseTotalTotal
                 generalUtils.repAlpha(toBuf, 3000, (short)40, "0.0"); // baseGSTTotal

                 generalUtils.repAlpha(toBuf, 3000, (short)42, "0.0"); // remarkType

                 generalUtils.repAlpha(toBuf, 3000, (short)43, ""); // revision

                 generalUtils.repAlpha(toBuf, 3000, (short)44, ""); // deliveryMethod

                 generalUtils.repAlpha(toBuf, 3000, (short)45, ""); // shipTo

                 generalUtils.repAlpha(toBuf, 3000, (short)46, ""); // invoiceTo

                 generalUtils.repAlpha(toBuf, 3000, (short)47, ""); // customer

                 generalUtils.repAlpha(toBuf, 3000, (short)48, ""); // shippingTerms

                 generalUtils.repAlpha(toBuf, 3000, (short)49, ""); // groupDiscountType
                 generalUtils.repAlpha(toBuf, 3000, (short)50, "0.0"); // groupDiscountAmount
                 generalUtils.repAlpha(toBuf, 3000, (short)51, "0.0"); // groupDiscountValue

                 generalUtils.repAlpha(toBuf, 3000, (short)52, ""); // misc3

                 generalUtils.repAlpha(toBuf, 3000, (short)53, "1970-01-01"); // requiredBy

                 generalUtils.repAlpha(toBuf, 3000, (short)54, ""); // overseas
                 generalUtils.repAlpha(toBuf, 3000, (short)55, ""); // closed
                 generalUtils.repAlpha(toBuf, 3000, (short)56, "0"); // printCount

                 break;
      case 'L' : // line

                 // stuff from SO

                 generalUtils.dfs(fromBuf, (short)2, itemCode); // itemCode
                 generalUtils.repAlpha(toBuf, 3000, (short)1, itemCode);

                 generalUtils.dfs(fromBuf, (short)3, b); // description
                 generalUtils.repAlpha(toBuf, 3000, (short)2, b);

                 unitPrice = generalUtils.doubleFromStr(generalUtils.dfsAsStr(fromBuf, (short)29)); // unitPrice (SO costPrice)
                 if(unitPrice == 0.0) // no SO costPrice specified
                 {
                   unitPrice = generalUtils.doubleFromStr(inventory.getAStockFieldGivenCode(con, stmt, rs, "PurchasePrice", generalUtils.stringFromBytes(itemCode, 0L)));
                 }

                 generalUtils.repAlpha(toBuf, 3000, (short)3, generalUtils.doubleToStr('8', unitPrice));

                 generalUtils.repAlpha(toBuf, 3000, (short)4, qtyReqd); // qty

                 discount = 0.0;
                 reCalculate(qtyReqd, unitPrice, discount, rate, amount, amount2);
                 generalUtils.repAlpha(toBuf, 3000, (short)5,  amount[0]);
                 generalUtils.repAlpha(toBuf, 3000, (short)19, amount2[0]);

                 generalUtils.repAlpha(toBuf, 3000, (short)15, soLine);

                 generalUtils.dfs(fromBuf, (short)21, b); // UoM
                 generalUtils.repAlpha(toBuf, 3000, (short)20, b);

                 generalUtils.dfs(fromBuf, (short)22, b); // line
                 generalUtils.repAlpha(toBuf, 3000, (short)21, b);

                 generalUtils.dfs(fromBuf, (short)23, b); // entry
                 generalUtils.repAlpha(toBuf, 3000, (short)22, b);

                 generalUtils.dfs(fromBuf, (short)25, b); // requiredBy
                 generalUtils.repAlpha(toBuf, 3000, (short)25, b);

                 generalUtils.repAlpha(toBuf, 3000, (short)26, soCode);

                 generalUtils.dfs(fromBuf, (short)26, b); // remark
                 generalUtils.repAlpha(toBuf, 3000, (short)29, b);

                 generalUtils.dfs(fromBuf, (short)30, b); // manufacturer
                 generalUtils.repAlpha(toBuf, 3000, (short)30, b);

                 generalUtils.dfs(fromBuf, (short)31, b); // manufacturerCode
                 generalUtils.repAlpha(toBuf, 3000, (short)31, b);

                 // other PO sutff

                 generalUtils.dfs(fromBuf, (short)7, b); // gstRate
                 generalUtils.repAlpha(toBuf, 3000, (short)6, accountsUtils.getDefaultGSTRate(con, stmt, rs, dnm, localDefnsDir, defnsDir));

                 generalUtils.repAlpha(toBuf, 3000, (short)7, "0.0"); // ActualUnitPrice
                 generalUtils.repAlpha(toBuf, 3000, (short)8, "0.0"); // ActualQuantity
                 generalUtils.repAlpha(toBuf, 3000, (short)9, "0.0"); // ActualQuantity

                 generalUtils.repAlpha(toBuf, 3000, (short)10, ""); // received

                 generalUtils.repAlpha(toBuf, 3000, (short)11, unm); // signon

                 generalUtils.repAlpha(toBuf, 3000, (short)12, ""); // dlm
                 generalUtils.repAlpha(toBuf, 3000, (short)13, ""); // unused4
                 generalUtils.repAlpha(toBuf, 3000, (short)14, ""); // deliveryMethod

                 generalUtils.repAlpha(toBuf, 3000, (short)16, ""); // unused2

                 generalUtils.repAlpha(toBuf, 3000, (short)17, ""); // applied

                 generalUtils.repAlpha(toBuf, 3000, (short)18, ""); // unused3

                 generalUtils.repAlpha(toBuf, 3000, (short)23, "0.0"); // discount

                 generalUtils.repAlpha(toBuf, 3000, (short)24, ""); // storeCode

                 generalUtils.repAlpha(toBuf, 3000, (short)26, soCode); // soCode

                 generalUtils.repAlpha(toBuf, 3000, (short)28, "1970-01-01"); // dateConfirmed

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

  //--------------------------------------------------------------------------------------------------------------------------------------------------
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

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void reCalculate(String qty, double unitPrice, double discount, double rate, String[] amount, String[] amount2) throws Exception
  {
    double qtyD;

    if(qty.length() == 0)
      qtyD = 0.0;
    else qtyD = generalUtils.doubleFromStr(qty);

    double discountValue = (qtyD * unitPrice) * discount / 100.0;

    double amt = generalUtils.doubleDPs(((qtyD * unitPrice) - discountValue), '2');

    amount2[0]  = generalUtils.doubleToStr(generalUtils.doubleDPs(amt, '2'));
    amount[0] = generalUtils.doubleToStr(generalUtils.doubleDPs(amt, '2') * rate);
  }

}
