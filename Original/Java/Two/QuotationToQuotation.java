// =======================================================================================================================================================================================================
// System: ZaraStar DocumentEngine: quote to quote
// Module: QuotationToQuotation.java
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

public class QuotationToQuotation
{
  GeneralUtils  generalUtils  = new GeneralUtils();
  DocumentUtils  documentUtils  = new DocumentUtils();
  MiscDefinitions  miscDefinitions  = new MiscDefinitions();
  Customer customer   = new Customer();
  Quotation  quotation  = new Quotation();
  DocumentAttachmentsCopy documentAttachmentsCopy = new DocumentAttachmentsCopy();
  ServerUtils serverUtils   = new ServerUtils();
  
  // -------------------------------------------------------------------------------------------------------------------------------
  public boolean create(Connection con, Statement stmt, ResultSet rs, byte[] code, char renumberLines, char renumberEntries, int[] lines, int numLines, String unm, String dnm,
                        String localDefnsDir, String defnsDir, byte[] newDocCode, int[] bytesOut) throws Exception
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

    if(quotation.getRecGivenCode(con, stmt, rs, code, '\000', data, dnm, localDefnsDir, defnsDir, bytesOut) == -1)
    {
      generalUtils.strToBytes(newDocCode, "h0031.htm"); // not found
      return false;
    }

    String customerCode = generalUtils.dfsAsStr(data, (short)2);

    quoteToQuote('H', newDocCode, data, dataTo, unm, localDefnsDir, defnsDir, b);

    if(quotation.putRecHeadGivenCode(con, stmt, rs, newDocCode, 'N', dataTo, dnm, localDefnsDir, defnsDir) != ' ')
    {
      generalUtils.strToBytes(newDocCode, "h0007.htm"); // not created
      return false;
    }

    // fetch lines data in one go
    linesData = quotation.getLines(con, stmt, rs, code, '\000', linesData, listLen, linesCount, dnm, localDefnsDir, defnsDir);

    int entryCount = 0;
    for(int xx=0;xx<linesCount[0];++xx)
    {
      if(generalUtils.getListEntryByNum(xx, linesData, data)) // just-in-case
      {
        generalUtils.replaceTwosWithNulls(data);
                   
        // check this line
        thisLine = generalUtils.dfsAsInt(data, (short)12); // origin-1
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
          generalUtils.repAlpha(data, 3000, (short)12, newLines[z]);

          if(renumberEntries == 'Y')
            generalUtils.repAlpha(data, 3000, (short)13, ++entryCount);

          quoteToQuote('L', newDocCode, data, dataTo, unm, localDefnsDir, defnsDir, b);
          quotation.putRecLine(con, stmt, rs, newDocCode, null, 'N', dataTo, dnm, localDefnsDir, defnsDir);
        }
      }
    }  

    if(linesCount[0] > 0) // get all the multiple lines for this document
      multipleLinesData = quotation.getMultipleLines(con, stmt, rs, code, multipleLinesData, multipleListLen, multipleLinesCount);

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
          quoteToQuote('M', newDocCode, data, dataTo, unm, localDefnsDir, defnsDir, b);
          quotation.putMultipleLine(con, stmt, rs, newDocCode, newLines[z], dataTo, dnm, localDefnsDir, defnsDir);
        }
      }
    }

    documentAttachmentsCopy.copyAttachments(con, stmt, rs, "quotea", code, "quotea", newDocCode, dnm, defnsDir, localDefnsDir);                 

        serverUtils.syncToIW(con, generalUtils.stringFromBytes(newDocCode, 0L), "", "", 'Q', generalUtils.todaySQLFormat(localDefnsDir, defnsDir), unm, customerCode, dnm);

   return true;
 }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void quoteToQuote(char which, byte[] newDocCode, byte[] fromBuf, byte[] toBuf, String unm, String localDefnsDir, String defnsDir,
                            byte[] b) throws Exception
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

                 generalUtils.repAlpha(toBuf, 3000, (short)12, "\0"); // status
                 generalUtils.repAlpha(toBuf, 3000, (short)13, "1970-01-01");
                 generalUtils.repAlpha(toBuf, 3000, (short)14, "1970-01-01");

                 for(x=15;x<=19;++x)
                 {
                   generalUtils.dfs(fromBuf, (short)x, b);
                   generalUtils.repAlpha(toBuf, 3000, (short)x, b);
                 }

                 for(x=20;x<=32;++x)
                 {
                   generalUtils.repAlpha(toBuf, 3000, (short)x, "");
                 }

                 generalUtils.dfs(fromBuf, (short)33, b); // groupDiscount
                 generalUtils.repAlpha(toBuf, 3000, (short)33, b);

                 generalUtils.repAlpha(toBuf, 3000, (short)34, ""); // dlm

                 generalUtils.repAlpha(toBuf, 3000, (short)35, unm); // signon

                 generalUtils.dfs(fromBuf, (short)36, b); // groupDiscountType
                 generalUtils.repAlpha(toBuf, 3000, (short)36, b);

                 generalUtils.repAlpha(toBuf, 3000, (short)37, ""); // revisionOf

                 for(x=38;x<=41;++x)
                 {
                   generalUtils.dfs(fromBuf, (short)x, b);
                   generalUtils.repAlpha(toBuf, 3000, (short)x, b);
                 }

                 for(x=42;x<=51;++x)
                 {
                   generalUtils.dfs(fromBuf, (short)x, b);
                   generalUtils.repAlpha(toBuf, 3000, (short)x, b);
                 }

                 generalUtils.repAlpha(toBuf, 3000, (short)52, " "); // documentStatus

                 for(x=53;x<=83;++x)
                 {
                   generalUtils.dfs(fromBuf, (short)x, b);
                   generalUtils.repAlpha(toBuf, 3000, (short)x, b);
                 }

                 break;
      case 'L' : // line
                 for(x=1;x<=7;++x)
                 {
                   generalUtils.dfs(fromBuf, (short)(x+1), b);
                   generalUtils.repAlpha(toBuf, 3000, (short)x, b);
                 }

                 generalUtils.repAlpha(toBuf, 3000, (short)8, "");

                 for(x=9;x<=16;++x)
                 {
                   generalUtils.dfs(fromBuf, (short)(x+1), b);
                   generalUtils.repAlpha(toBuf, 3000, (short)x, b);
                 }

                 generalUtils.repAlpha(toBuf, 3000, (short)17, unm); // signon

                 generalUtils.dfs(fromBuf, (short)19, b); // costPrice
                 if(b[0] == '\000')
                   generalUtils.strToBytes(b, "0");
                 generalUtils.repAlpha(toBuf, 3000, (short)18, b);

                 generalUtils.dfs(fromBuf, (short)20, b); // manufacturer
                 generalUtils.repAlpha(toBuf, 3000, (short)19, b);

                 generalUtils.dfs(fromBuf, (short)21, b); // manufacturerCode
                 generalUtils.repAlpha(toBuf, 3000, (short)20, b);

                 break;
      case 'M' : // LL line
                 for(x=1;x<=3;++x)
                 {
                   generalUtils.dfs(fromBuf, (short)(x+1), b);
                   generalUtils.repAlpha(toBuf, 3000, (short)x, b);
                 }
                 break;
    }
  }

}

