// =======================================================================================================================================================================================================
// System: ZaraStar: DocumentEngine: Quotes fetch line data
// Module: QuotationLineEdit.java
// Author: C.K.Harvey
// Copyright (c) 2001-12 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.sql.*;
import java.util.Enumeration;

public class QuotationLineEdit extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  Quotation quotation = new Quotation();
  AccountsUtils accountsUtils = new AccountsUtils();
  MiscDefinitions miscDefinitions = new MiscDefinitions();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="";

    try
    {
      directoryUtils.setContentHeaders(res);
      res.setHeader("Header", "HTTP/1.0 200 OK");
      res.setStatus(200);
      
      Enumeration en = req.getParameterNames();
      while(en.hasMoreElements())
      {
        String elementName = (String)en.nextElement();
        String[] value = req.getParameterValues(elementName);
        if(elementName.equals("unm"))
          unm = value[0];
        else
        if(elementName.equals("sid"))
          sid = value[0];
        else
        if(elementName.equals("uty"))
          uty = value[0];
        else
        if(elementName.equals("dnm"))
          dnm = value[0];
        else
        if(elementName.equals("p1")) // code
          p1 = value[0];
        else
        if(elementName.equals("p2")) // line
          p2 = value[0];
      }
      
      doIt(res, req, unm, sid, uty, dnm, p1, p2, bytesOut);
    }
    catch(Exception e)
    {
      System.out.println(("Unexpected System Error: 4025a: " + e));
      res.getWriter().write("Unexpected System Error: 4025a");
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void doIt(HttpServletResponse res, HttpServletRequest req, String unm, String sid, String uty, String dnm, String p1, String p2, int[] bytesOut) throws Exception
  {
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    String[] rtnStr = new String[1];  rtnStr[0]="";

    String rtn="Unexpected System Error: 4025a";

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      rtn = "Access Denied - Duplicate SignOn or Session Timeout";
    }
    else
    {
      if(fetch(dnm, p1, p2, rtnStr, localDefnsDir, defnsDir))
        rtn = ".";
    }

    res.setContentType("text/xml");
    res.setHeader("Cache-Control", "no-cache");

    String s = "<msg><res>" + rtn + "</res>" + rtnStr[0] + "</msg>";
    res.getWriter().write(s);

    serverUtils.totalBytes(req, unm, dnm, 4025, bytesOut[0], 0, p1 + " " + p2);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // rtns: ' ' if rec added/updated ok
  //       'E' if adding a rec that already exists
  //       'X' other error
  private boolean fetch(String dnm, String code, String line, String[] res, String localDefnsDir, String defnsDir) throws Exception
  {
    res[0] = "";
    
    Connection con = null;
    Statement stmt = null;
    ResultSet rs   = null;

    try
    {
      String uName    = directoryUtils.getMySQLUserName();
      String passWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();

      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + uName + "&password=" + passWord);

      byte[] codeB = new byte[21];
      byte[] lineB = new byte[20];
      byte[] data  = new byte[2000];
      int[]  bytesOut = new int[1];

      generalUtils.strToBytes(codeB, code);
      generalUtils.strToBytes(lineB, line);

      char dpOnQuantities = miscDefinitions.dpOnQuantities(con, stmt, rs, "5");
      char dpOnUnitPrice  = miscDefinitions.dpOnUnitPrice(con, stmt, rs, "4");

      // if separator is '\001' then buf is: company.code=acme\001company.name=acme ltd\001
      // else if separator is '\000' (eg) then buf is acme\0acme ltd\0
      if(quotation.getLine(con, stmt, rs, codeB, lineB, '\001', data, bytesOut) == 0) // just-in-case
      {
        byte[] value   = new byte[501];
        byte[] fldName = new byte[50];

        generalUtils.strToBytes(fldName, "Line");
        if(quotation.searchDataString(data, generalUtils.lengthBytes(data, 0), "quotel", fldName, value) != -1) // just-in-case
        {
          if(value[0] == '\000')
            res[0] += "<line>.</line>";
          else res[0] += "<line>" + generalUtils.stringFromBytes(value, 0L) + "</line>";
        }

        generalUtils.strToBytes(fldName, "Entry");
        if(quotation.searchDataString(data, generalUtils.lengthBytes(data, 0), "quotel", fldName, value) != -1) // just-in-case
        {
          if(value[0] == '\000')
            res[0] += "<entry>.</entry>";
          else res[0] += "<entry>" + generalUtils.stringFromBytes(value, 0L) + "</entry>";
        }

        generalUtils.strToBytes(fldName, "Manufacturer");
        if(quotation.searchDataString(data, generalUtils.lengthBytes(data, 0), "quotel", fldName, value) != -1) // just-in-case
        {
          if(value[0] == '\000')
            res[0] += "<manufacturer>.</manufacturer>";
          else
          {
            if(generalUtils.stringFromBytes(value, 0L).equals("<none>"))
              res[0] += "<manufacturer>.</manufacturer>";
            else res[0] += "<manufacturer>" + generalUtils.stringFromBytes(value, 0L) + "</manufacturer>";
          }
        }

        generalUtils.strToBytes(fldName, "ManufacturerCode");
        if(quotation.searchDataString(data, generalUtils.lengthBytes(data, 0), "quotel", fldName, value) != -1) // just-in-case
        {
          if(value[0] == '\000')
            res[0] += "<manufacturerCode>.</manufacturerCode>";
          else res[0] += "<manufacturerCode>" + generalUtils.stringFromBytes(value, 0L) + "</manufacturerCode>";
        }

        generalUtils.strToBytes(fldName, "CustomerItemCode");
        if(quotation.searchDataString(data, generalUtils.lengthBytes(data, 0), "quotel", fldName, value) != -1) // just-in-case
        {
          if(value[0] == '\000')
            res[0] += "<customerItemCode>.</customerItemCode>";
          else res[0] += "<customerItemCode>" + generalUtils.stringFromBytes(value, 0L) + "</customerItemCode>";
        }

        generalUtils.strToBytes(fldName, "GSTRate");
        if(quotation.searchDataString(data, generalUtils.lengthBytes(data, 0), "quotel", fldName, value) != -1) // just-in-case
        {
          if(value[0] == '\000')
            res[0] += "<gstRate>.</gstRate>";
          else res[0] += "<gstRate>" + generalUtils.stringFromBytes(value, 0L) + "</gstRate>";
        }

        generalUtils.strToBytes(fldName, "Quantity");
        if(quotation.searchDataString(data, generalUtils.lengthBytes(data, 0), "quotel", fldName, value) != -1) // just-in-case
        {
          if(value[0] == '\000')
            res[0] += "<quantity>.</quantity>";
          else res[0] += "<quantity>" + generalUtils.formatNumeric(generalUtils.stringFromBytes(value, 0L), dpOnQuantities) + "</quantity>";
        }

        generalUtils.strToBytes(fldName, "ItemCode");
        if(quotation.searchDataString(data, generalUtils.lengthBytes(data, 0), "quotel", fldName, value) != -1) // just-in-case
        {
          if(value[0] == '\000')
            res[0] += "<itemCode>.</itemCode>";
          else res[0] += "<itemCode>" + generalUtils.stringFromBytes(value, 0L) + "</itemCode>";
        }

        generalUtils.strToBytes(fldName, "UoM");
        if(quotation.searchDataString(data, generalUtils.lengthBytes(data, 0), "quotel", fldName, value) != -1) // just-in-case
        {
          if(value[0] == '\000')
            res[0] += "<uom>.</uom>";
          else res[0] += "<uom>" + generalUtils.stringFromBytes(value, 0L) + "</uom>";
        }

        generalUtils.strToBytes(fldName, "Discount");
        if(quotation.searchDataString(data, generalUtils.lengthBytes(data, 0), "quotel", fldName, value) != -1) // just-in-case
        {
          if(value[0] == '\000')
            res[0] += "<discount>.</discount>";
          else res[0] += "<discount>" + generalUtils.formatNumeric(generalUtils.stringFromBytes(value, 0L), dpOnUnitPrice) + "</discount>";
        }

        generalUtils.strToBytes(fldName, "Remark");
        if(quotation.searchDataString(data, generalUtils.lengthBytes(data, 0), "quotel", fldName, value) != -1) // just-in-case
        {
          if(value[0] == '\000')
            res[0] += "<remark>.</remark>";
          else res[0] += "<remark>" + generalUtils.stringFromBytes(value, 0L) + "</remark>";
        }

        generalUtils.strToBytes(fldName, "CostPrice");
        if(quotation.searchDataString(data, generalUtils.lengthBytes(data, 0), "quotel", fldName, value) != -1) // just-in-case
        {
          if(value[0] == '\000')
            res[0] += "<costPrice>.</costPrice>";
          else res[0] += "<costPrice>" + generalUtils.formatNumeric(generalUtils.stringFromBytes(value, 0L), dpOnUnitPrice) + "</costPrice>";
        }

        generalUtils.strToBytes(fldName, "UnitPrice");
        if(quotation.searchDataString(data, generalUtils.lengthBytes(data, 0), "quotel", fldName, value) != -1) // just-in-case
        {
          if(value[0] == '\000')
            res[0] += "<unitPrice>.</unitPrice>";
          else res[0] += "<unitPrice>" + generalUtils.formatNumeric(generalUtils.stringFromBytes(value, 0L), dpOnUnitPrice) + "</unitPrice>";
        }

        generalUtils.strToBytes(fldName, "Description");
        if(quotation.searchDataString(data, generalUtils.lengthBytes(data, 0), "quotel", fldName, value) != -1) // just-in-case
        {
          byte[] multipleLinesData = new byte[1000];
          int[]  multipleListLen = new int[1]; multipleListLen[0] = 1000;
          int[]  multipleLinesCount = new int[1];
          byte[] llData = new byte[200];
          String m = "";
          
          multipleLinesData = quotation.getMultipleLine(con, stmt, rs, codeB, lineB, multipleLinesData, multipleListLen, multipleLinesCount);

          for(int xx=0;xx<multipleLinesCount[0];++xx)
          {
            if(generalUtils.getListEntryByNum(xx, multipleLinesData, llData)) // just-in-case
            {
              generalUtils.replaceTwosWithOnes(llData);
              m += (generalUtils.stringFromBytes(llData, 0L) + "\n");
            }
          }

          if(value[0] == '\000')
            res[0] += "<desc>.</desc>";
          else res[0] += "<desc>" + generalUtils.stringFromBytes(value, 0L) + "\n" + m + "</desc>";
        }

        String currency = quotation.getAQuoteFieldGivenCode(con, stmt, rs, "Currency", code);
        {
          if(currency.length() == 0)
          {
            res[0] += "<currency>.</currency>";
            res[0] += "<baseCurrency>.</baseCurrency>";
          }
          else
          {
            res[0] += "<currency>" + currency + "</currency>";
            res[0] += "<baseCurrency>" + accountsUtils.getBaseCurrency(con, stmt, rs, dnm, localDefnsDir, defnsDir) + "</baseCurrency>";
          }
        }

        generalUtils.strToBytes(fldName, "Amount");
        if(quotation.searchDataString(data, generalUtils.lengthBytes(data, 0), "quotel", fldName, value) != -1) // just-in-case
        {
          if(value[0] == '\000')
            res[0] += "<amount>0</amount>";
          else res[0] += "<amount>" + generalUtils.formatNumeric(generalUtils.stringFromBytes(value, 0L), dpOnUnitPrice) + "</amount>";
        }

        generalUtils.strToBytes(fldName, "Amount2");
        if(quotation.searchDataString(data, generalUtils.lengthBytes(data, 0), "quotel", fldName, value) != -1) // just-in-case
        {
          if(value[0] == '\000')
            res[0] += "<amount2>0</amount2>";
          else res[0] += "<amount2>" + generalUtils.formatNumeric(generalUtils.stringFromBytes(value, 0L), dpOnUnitPrice) + "</amount2>";
        }
      }

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
      if(con  != null) con.close();

      return true;
    }
    catch(Exception e)
    {
      System.out.println("4025a: " + e);
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }

    return false;
  }

}
