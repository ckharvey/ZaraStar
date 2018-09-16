// =======================================================================================================================================================================================================
// System: ZaraStar Document: SO to DO
// Module: SalesOrderToDeliveryOrder.java
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

public class SalesOrderToDeliveryOrder
{
  GeneralUtils  generalUtils  = new GeneralUtils();
  DocumentUtils  documentUtils  = new DocumentUtils();
  MiscDefinitions  miscDefinitions  = new MiscDefinitions();
  DocumentAttachmentsCopy documentAttachmentsCopy = new DocumentAttachmentsCopy();
  SalesOrder  salesOrder  = new SalesOrder();
  DeliveryOrder  deliveryOrder  = new DeliveryOrder();
  ServerUtils serverUtils = new ServerUtils();
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean create(Connection con, Statement stmt, ResultSet rs, byte[] code, char renumberLines, char renumberEntries, int[] lines, int numLines, String quantitiesReqd, String unm, String dnm, String localDefnsDir, String defnsDir,
                        byte[] newDocCode, int[] bytesOut) throws Exception
  {
    try
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

    if(salesOrder.getRecGivenCode(con, stmt, rs, code, '\000', data, dnm, localDefnsDir, defnsDir, bytesOut) == -1)
    {
      generalUtils.strToBytes(newDocCode, "h0031.htm"); // not found
      return false;
    }

    double rate = generalUtils.dfsAsDouble(data, (short)30);
    
    String customerCode = generalUtils.dfsAsStr(data, (short)2);
    
    soToDO(con, stmt, rs, 'H', rate, newDocCode, data, dataTo, code, null, "", unm, localDefnsDir, defnsDir, b);

    if(deliveryOrder.putRecHeadGivenCode(con, stmt, rs, newDocCode, 'N', dataTo, dnm, localDefnsDir, defnsDir) != ' ')
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
            generalUtils.repAlpha(data, 3000, (short)20, ++entryCount);
 
          soToDO(con, stmt, rs, 'L', rate, newDocCode, data, dataTo, code, originalLine, qtyReqd, unm, localDefnsDir, defnsDir, b);
          deliveryOrder.putRecLine(con, stmt, rs, newDocCode, null, 'N', dataTo, dnm, localDefnsDir, defnsDir);
        }
      }
    }  

    if(linesCount[0] > 0) // get all the multiple lines for this document
      multipleLinesData = salesOrder.getMultipleLines(con, stmt, rs, code, multipleLinesData, multipleListLen, multipleLinesCount, dnm, localDefnsDir, defnsDir);

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
          soToDO(con, stmt, rs, 'M', rate, newDocCode, data, dataTo, null, originalLine, "", unm, localDefnsDir, defnsDir, b);
          deliveryOrder.putMultipleLine(con, stmt, rs, newDocCode, newLines[z], dataTo, dnm, localDefnsDir, defnsDir);
        }
      }
    }

    documentAttachmentsCopy.copyAttachments(con, stmt, rs, "soa", code, "doa", newDocCode, dnm, defnsDir, localDefnsDir);                 
    
    serverUtils.syncToIW(con, generalUtils.stringFromBytes(newDocCode, 0L), "", "", 'D', generalUtils.todaySQLFormat(localDefnsDir, defnsDir), unm, customerCode, dnm);
    }
    catch(Exception e) { System.out.println(e); }

    return true;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void soToDO(Connection con, Statement stmt, ResultSet rs, char which, double rate, byte[] newDocCode, byte[] fromBuf, byte[] toBuf, byte[] code, byte[] originalLine, String qtyReqd, String unm, String localDefnsDir, String defnsDir, byte[] b)
                      throws Exception
  {
    int x;
    double unitPrice, discount;
    String[] amount  = new String[1];
    String[] amount2 = new String[1];

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
                                  
                 generalUtils.dfs(fromBuf, (short)29, b); // projectCode
                 generalUtils.repAlpha(toBuf, 3000, (short)26, b);

                 generalUtils.dfs(fromBuf, (short)26, b); // shipAddrCode
                 generalUtils.repAlpha(toBuf, 3000, (short)27, b);
                 
                 generalUtils.dfs(fromBuf, (short)39, b); // custPOCode
                 generalUtils.repAlpha(toBuf, 3000, (short)29, b);
                 
                 generalUtils.dfs(fromBuf, (short)38, b); // salesPerson
                 generalUtils.repAlpha(toBuf, 3000, (short)30, b);

                 for(x=32;x<=37;++x)
                 {
                   generalUtils.dfs(fromBuf, (short)x, b);
                   generalUtils.repAlpha(toBuf, 3000, (short)(x + 1), b);
                 }

                 generalUtils.dfs(fromBuf, (short)31, b); // currency
                 generalUtils.repAlpha(toBuf, 3000, (short)39, b);

                 generalUtils.dfs(fromBuf, (short)30, b); // rate
                 generalUtils.repAlpha(toBuf, 3000, (short)41, b);
                 
                 generalUtils.repAlpha(toBuf, 3000, (short)44, "0.0");
                 
                 generalUtils.repAlpha(toBuf, 3000, (short)45, "0.0");
                 
                 generalUtils.dfs(fromBuf, (short)42, b); // buyeremail
                 generalUtils.repAlpha(toBuf, 3000, (short)49, b);
                 
                 generalUtils.dfs(fromBuf, (short)45, b); // terms
                 generalUtils.repAlpha(toBuf, 3000, (short)46, b);
                 
                 // other stuff
                 
                 generalUtils.repAlpha(toBuf, 3000, (short)11, ""); // notes
                 generalUtils.repAlpha(toBuf, 3000, (short)12, ""); // waybill
                 generalUtils.repAlpha(toBuf, 3000, (short)13, "1970-01-01"); // dateShipped
                 
                 generalUtils.repAlpha(toBuf, 3000, (short)14, ""); // invoiceCode

                 generalUtils.repAlpha(toBuf, 3000, (short)15, ""); // misc1
                 generalUtils.repAlpha(toBuf, 3000, (short)16, ""); // misc2

                 generalUtils.repAlpha(toBuf, 3000, (short)19, "N"); // returned
                 
                 generalUtils.repAlpha(toBuf, 3000, (short)20, unm); // signon

                 generalUtils.repAlpha(toBuf, 3000, (short)22, ""); // dlm
                 
                 generalUtils.repAlpha(toBuf, 3000, (short)25, ""); // type

                 generalUtils.repAlpha(toBuf, 3000, (short)28, ""); // purchasedBy

                 generalUtils.repAlpha(toBuf, 3000, (short)31, ""); // deliveryDriver
                 
                 generalUtils.repAlpha(toBuf, 3000, (short)32, ""); // revisionOf
                 
                 generalUtils.repAlpha(toBuf, 3000, (short)40, "0"); // numCartons

                 generalUtils.repAlpha(toBuf, 3000, (short)42, ""); // shipper

                 generalUtils.repAlpha(toBuf, 3000, (short)43, setPLCodes(con, stmt, rs, generalUtils.stringFromBytes(code, 0L))); // plCode
                 
                 generalUtils.repAlpha(toBuf, 3000, (short)47, "L"); // status
                
                 generalUtils.repAlpha(toBuf, 3000, (short)48, "");  // exportStatement
                 
                 generalUtils.repAlpha(toBuf, 3000, (short)50, "1970-01-01"); // dateReturned
                 break;
      case 'L' : // line
      
                 // stuff from SO
      
                 for(x=1;x<=6;++x)
                 {
                   generalUtils.dfs(fromBuf, (short)(x+1), b);
                   generalUtils.repAlpha(toBuf, 3000, (short)x, b);
                 }

                 generalUtils.repAlpha(toBuf, 3000, (short)4, qtyReqd);

                 generalUtils.dfs(fromBuf, (short)24, b); // discount
                 generalUtils.repAlpha(toBuf, 3000, (short)7, b);
                 
                 generalUtils.dfs(fromBuf, (short)21, b); // uom
                 generalUtils.repAlpha(toBuf, 3000, (short)12, b);
                 
                 // the soLine must be the line before any renumbering is (maybe) done
                 generalUtils.repAlpha(toBuf, 3000, (short)14, originalLine);

                 generalUtils.dfs(fromBuf, (short)27, b); // custItemCode
                 generalUtils.repAlpha(toBuf, 3000, (short)17, b);

                 generalUtils.dfs(fromBuf, (short)22, b); // line
                 generalUtils.repAlpha(toBuf, 3000, (short)18, b);

                 generalUtils.dfs(fromBuf, (short)23, b); // entry
                 generalUtils.repAlpha(toBuf, 3000, (short)19, b);
                 
                 generalUtils.repAlpha(toBuf, 3000, (short)20, code); // soCode

                 generalUtils.dfs(fromBuf, (short)29, b); // costPrice
                 if(b[0] == '\000')
                   generalUtils.strToBytes(b, "0.0");                   
                 generalUtils.repAlpha(toBuf, 3000, (short)24, b);

                 generalUtils.dfs(fromBuf, (short)30, b); // manufacturer
                 generalUtils.repAlpha(toBuf, 3000, (short)25, b);

                 generalUtils.dfs(fromBuf, (short)31, b); // manufacturerCode
                 generalUtils.repAlpha(toBuf, 3000, (short)26, b);

                 unitPrice = generalUtils.dfsAsDouble(fromBuf, (short)4);
                 discount  = generalUtils.dfsAsDouble(fromBuf, (short)24);

                 reCalculate(qtyReqd, unitPrice, discount, rate, amount, amount2);
                 generalUtils.repAlpha(toBuf, 3000, (short)5,  amount[0]);

                 generalUtils.repAlpha(toBuf, 3000, (short)10, amount2[0]);

                 // other stuff
                 
                 generalUtils.repAlpha(toBuf, 3000, (short)8, ""); // dlm
                 generalUtils.repAlpha(toBuf, 3000, (short)9, ""); // unused1

                 generalUtils.repAlpha(toBuf, 3000, (short)11, ""); // poRefNum

                 generalUtils.repAlpha(toBuf, 3000, (short)13, ""); // serialNumber
                 generalUtils.repAlpha(toBuf, 3000, (short)15, "1970-01-01"); // unused3
                 generalUtils.repAlpha(toBuf, 3000, (short)16, "0"); // qtyRequired

                 generalUtils.repAlpha(toBuf, 3000, (short)21, "0"); // weight
                 generalUtils.repAlpha(toBuf, 3000, (short)22, "0"); // weightPer
                 
                 generalUtils.repAlpha(toBuf, 3000, (short)23, unm); // signon

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

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // finds *all* PLs for the SO (whether or not items from the PL are actually on this DO) !!!
  // AND truncates after 20 chars (size of PLCode field on DO header) !!!
  private String setPLCodes(Connection con, Statement stmt, ResultSet rs, String soCode) throws Exception
  {
    String plCodes = "";

    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT DISTINCT t1.PLCode FROM pll AS t2 INNER JOIN pl AS t1 ON t2.PLCode = t1.PLCode WHERE t1.status != 'C' AND t2.SOCode = '" + soCode + "'");

      while(rs.next())
      {
        if(plCodes.length() > 0)
          plCodes += "," + rs.getString(1);
        else plCodes = rs.getString(1);
      }
    }
    catch(Exception e) { System.out.println(e); }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    if(plCodes.length() > 20)
      plCodes = plCodes.substring(0, 20);

    return plCodes;
  }

}
