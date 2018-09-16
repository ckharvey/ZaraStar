// =======================================================================================================================================================================================================
// System: ZaraStar wacmeta: Utilities
// Module: WacMetaUtils.java
// Author: C.K.Harvey
// Copyright (c) 2006-09 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import java.sql.*; 

public class WacMetaUtils
{
  GeneralUtils generalUtils = new GeneralUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  AccountsUtils accountsUtils = new AccountsUtils();

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void createTableWacmeta(boolean dropTable, String dnm, String year) throws Exception
  {
    String userName = directoryUtils.getMySQLUserName();
    String passWord = directoryUtils.getMySQLPassWord();

    Class.forName("com.mysql.jdbc.Driver").newInstance();

    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_accounts_" + year + "?user=" + userName + "&password=" + passWord);
    Statement stmt = con.createStatement();
    String q;

    try
    {
      if(dropTable)
      {
        q = "DROP TABLE wacmeta";
        stmt.executeUpdate(q);
      }
    }
    catch(Exception e) { }
    if(stmt != null) stmt.close();

    try
    {
      stmt = con.createStatement();

      q = "CREATE TABLE wacmeta ( ItemCode char(20) not null";

      for(int x=0;x<367;++x)
        q += ", Date" + x + " decimal(19,8)";

      q += ")";

      stmt.executeUpdate(q);
    }
    catch(Exception e) { }

    if(stmt != null) stmt.close();

    try
    {
      stmt = con.createStatement();
      
      q = "CREATE INDEX itemCodeInx on wacmeta(ItemCode)";

      stmt.executeUpdate(q);
    }
    catch(Exception e) { }

    if(stmt != null) stmt.close();
    con.close();
  }


  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void getWACDetailsGivenCode(Connection con, Statement stmt, ResultSet rs, String itemCode, double[] closingLevel, double[] closingWAC, String date) throws Exception
  {
    closingLevel[0] = closingWAC[0] = 0.0;

    if(itemCode.length() == 0) // just-in-case
      return;

    // check that there is a record that exists after the required date

    int rowCount = 0;

    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM stockopen WHERE ItemCode = '" + generalUtils.sanitiseForSQL(itemCode) + "' AND Date >= {d '" + date + "'}");

      if(rs.next()) // pickup the first one
        rowCount = rs.getInt("rowcount");

      rs.close();
      stmt.close();
    }
    catch(Exception e) { System.out.println(e); }
    {
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }

    if(rowCount == 0)
      return;

    // there is at least one record, so fetch the latest

    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT Level, Cost FROM stockopen WHERE ItemCode = '" + generalUtils.sanitiseForSQL(itemCode) + "' AND Date <= {d '" + date + "'} ORDER BY Date DESC");

      if(rs.next()) // pickup the first one
      {
        closingLevel[0] = generalUtils.doubleFromStr(rs.getString(1));
        closingWAC[0]   = generalUtils.doubleFromStr(rs.getString(2));
      }

      rs.close();
      stmt.close();
    }
    catch(Exception e) { System.out.println(e); }
    {
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void primeWAC(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String startDate, String year, String dnm) throws Exception
  {
    createTableWacmeta(true, dnm, year);

    String uName = directoryUtils.getMySQLUserName();
    String pWord = directoryUtils.getMySQLPassWord();

    Class.forName("com.mysql.jdbc.Driver").newInstance();

    Connection conAcc = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_accounts_" + year + "?user=" + uName + "&password=" + pWord);
    Statement stmtAcc = null, stmt3 = null, stmt4 = null;
    ResultSet rsAcc   = null, rs3   = null, rs4   = null;

    String yearEndDate = accountsUtils.getAccountingYearEndDateForADate(con, stmt, rs, startDate, dnm);

    int startDateEncoded = generalUtils.encodeFromYYYYMMDD(startDate);

    double[] openingLevel = new double[1];
    double[] openingWAC   = new double[1];

    String itemCode;

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT ItemCode FROM stock ORDER BY ItemCode");

    while(rs.next())
    {
      itemCode = rs.getString(1);

      getWACDetailsGivenCode(con, stmt, rs, itemCode, openingLevel, openingWAC, startDate);

      getWACForADate(con, conAcc, stmt2, stmt3, stmt4, stmtAcc, rs2, rs3, rs4, rsAcc, itemCode, startDate, startDateEncoded, yearEndDate, yearEndDate, openingLevel[0], openingWAC[0]);
    }

    rs.close();
    stmt.close();

    if(conAcc != null) conAcc.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private double getWACForADate(Connection con, Connection conAcc, Statement stmt, Statement stmt2, Statement stmt3, Statement stmtAcc, ResultSet rs, ResultSet rs2, ResultSet rs3, ResultSet rsAcc,
                                String itemCode, String dateFrom, int startDateEncoded, String dateTo, String yearEndDate, double openingLevel, double openingWAC) throws Exception
  {
    byte[] list = new byte[1000];
    int[] listLen = new int[1];  listLen[0] = 1000;

    try
    {
      // POs

      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT t2.Quantity, t2.Amount, t1.Date, t1.POCode "
                           + "FROM pol AS t2 INNER JOIN po AS t1 ON t1.POCode = t2.POCode "
                           + "WHERE t2.ItemCode = '" + itemCode + "' "
                           + "AND t1.Status != 'C' "
                           + "AND t1.Date >= {d '" + dateFrom + "'} "
                           + "AND t1.Date <= {d '" + dateTo + "'} "
                           + "ORDER BY t1.POCode");

      String poCode = "", lastPOCode = "", lastDate = "";
      double qtyForThisItem = 0.0, amtForThisItem = 0.0;
      boolean[] piFound = new boolean[1];
      boolean first = true;

      while(rs.next())
      {
        poCode = rs.getString(4);

        if(! poCode.equals(lastPOCode))
        {
          if(! first)
          {
            list = checkForPIs(con, stmt2, stmt3, rs2, rs3, lastPOCode, itemCode, lastDate, dateTo, list, listLen, piFound);

            if(! piFound[0]) // use PO data
            {
              list = addToTmp("PI", qtyForThisItem, amtForThisItem, lastDate, lastPOCode, list, listLen);

              qtyForThisItem = amtForThisItem = 0.0;
            }
          }
          else first = false;

          lastPOCode = poCode;
        }

        qtyForThisItem += generalUtils.doubleFromStr(rs.getString(1));
        amtForThisItem += generalUtils.doubleFromStr(rs.getString(2));
      
        lastDate   = rs.getString(3);
      }

      if(lastPOCode.length() > 0)// && ! poCode.equals(lastPOCode))
      {
        list = checkForPIs(con, stmt2, stmt3, rs2, rs3, lastPOCode, itemCode, lastDate, dateTo, list, listLen, piFound);
        if(! piFound[0]) // use PO data
          list = addToTmp("PI", qtyForThisItem, amtForThisItem, lastDate, lastPOCode, list, listLen);
      }

      rs.close();
      stmt.close();

      // SIs

      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT t2.Quantity, t2.Amount, t1.Date, t1.InvoiceCode FROM invoicel AS t2 INNER JOIN invoice AS t1 ON t1.InvoiceCode = t2.InvoiceCode WHERE t2.ItemCode = '" + itemCode + "' AND t1.Status != 'C' AND t1.Date >= {d '"
                           + dateFrom + "'} AND t1.Date < {d '" + dateTo + "'}");

      while(rs.next())
        list = addToTmp("SI", generalUtils.doubleFromStr(rs.getString(1)), 0, rs.getString(3), rs.getString(4), list, listLen);

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();

      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT t2.Quantity, t2.Amount, t1.Date, t2.InvoiceCode "
                           + "FROM creditl AS t2 INNER JOIN credit AS t1 ON t1.CNCode = t2.CNCode "
                           + "WHERE t2.ItemCode = '" + itemCode + "' "
                           + "AND t1.Status != 'C' "
                           + "AND t1.Date >= {d '" + dateFrom + "'} "
                           + "AND t1.Date < {d '" + dateTo + "'} "
                           + "AND t2.CostOfSaleAdjustment = 'Y'"); // note: if SCN is for a salesprice discount then it can be ignored

      while(rs.next())
        list = addToTmp("SC", generalUtils.doubleFromStr(rs.getString(1)), 0, rs.getString(3), rs.getString(4), list, listLen);

      rs.close();
      stmt.close();
    }
    catch(Exception e)
    {
      System.out.println("primeWAC: " + itemCode + " " + dateFrom + " " + dateTo + " " + yearEndDate + " " + e);
      return 0;
    }

    String docType, qtyStr, amtStr, dateAndType, docCode, dateStr;
    double qty, amt;
    double currentLevel = openingLevel;
    double currentWAC   = openingWAC;
    double currentValue = currentWAC * currentLevel;

    updateWAC(conAcc, stmtAcc, rsAcc, itemCode, -1, 0, openingWAC);

    byte[] entry = new byte[1000]; // plenty
    int x, y, z, len, entryNum = 0;
    while(generalUtils.getListEntryByNum(entryNum++, list, entry))
    {
      x = 0;
      len = generalUtils.lengthBytes(entry, 0);

      dateAndType = "";
      while(x < len && entry[x] != '\002') // just-in-case
        dateAndType += (char)entry[x++];
      ++x;

      y = dateAndType.length();
      dateStr = "";
      z = 0;
      while(z < (y - 3)) // just-in-case
        dateStr += dateAndType.charAt(z++);

      docType = "" + dateAndType.charAt(y - 2) + dateAndType.charAt(y - 1);

      qtyStr = "";
      while(x < len && entry[x] != '\002') // just-in-case
        qtyStr += (char)entry[x++];
      ++x;

      amtStr = "";
      while(x < len && entry[x] != '\002') // just-in-case
        amtStr += (char)entry[x++];
      ++x;

      docCode = "";
      while(x < len && entry[x] != '\002') // just-in-case
        docCode += (char)entry[x++];

      qty = generalUtils.doubleDPs(generalUtils.doubleFromStr(qtyStr), '2');
      amt = generalUtils.doubleDPs(generalUtils.doubleFromStr(amtStr), '2');

      if(docType.equals("PI"))
      {
        currentLevel += qty;
        currentValue += amt;
        if(currentLevel != 0)
          currentWAC = (currentValue / currentLevel); 

        updateWAC(conAcc, stmtAcc, rsAcc, itemCode, startDateEncoded,         generalUtils.encodeFromYYYYMMDD(dateStr),             currentWAC);
      }
      else
      if(docType.equals("PC"))
      {
        currentLevel -= qty; // need to know if it's a discount or goods returned... assumed goods returned  FIXME
        currentValue -= amt;
        if(currentLevel != 0)
          currentWAC = (currentValue / currentLevel);
      }
      else
      if(docType.equals("SI"))
      {
        currentLevel -= qty;
        currentValue -= (currentWAC * qty);

        // update the WAC used on the SI back into the results list (in case it should be needed by a future SCN)
        list = repInTmp((entryNum - 1), qty, currentWAC, dateAndType, docCode, list, listLen);
      }
      else
      if(docType.equals("SC"))
      {
        currentLevel += qty;
        currentValue += (wacAtInvoiceTime(docCode, list) * qty); // for SCN the docCode is the invoiceCode from the CN line
        if(currentLevel != 0)
          currentWAC = (currentValue / currentLevel);
      }  
    }

    rs.close();
    stmt.close();

    try
    {
      currentWAC = generalUtils.doubleDPs(currentWAC, '2');
    }
    catch(Exception e)
    {
      currentWAC = 0;
      System.out.println(itemCode + ": " + e);
    }

    return currentWAC;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private byte[] checkForPIs(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String poCode, String itemCode, String poDate, String dateTo, byte[] list, int[] listLen,
                             boolean[] piFound) throws Exception
  {
    piFound[0] = false;

    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT t2.Quantity, t2.Amount, t1.Date, t1.InvoiceCode "
                           + "FROM pinvoicel AS t2 INNER JOIN pinvoice AS t1 ON t1.InvoiceCode = t2.InvoiceCode "
                           + "WHERE t2.ItemCode = '" + itemCode + "' "
                           + "AND t1.Status != 'C' "
                           + "AND t2.PORefNum= '" + poCode + "' "
                           + "AND t1.Date >= {d '" + poDate + "'} "
                           + "AND t1.Date <= {d '" + dateTo + "'}");

      String piCode, lastPICode = "", lastDate = "";
      double qtyForThisItem = 0.0, amtForThisItem = 0.0;
      boolean first = true;

      while(rs.next())
      {
        piCode = rs.getString(4);

        if(! piCode.equals(lastPICode))
        {
          if(! first)
          {
            list = addToTmp("PI", qtyForThisItem, amtForThisItem, lastDate, lastPICode, list, listLen);
            qtyForThisItem = amtForThisItem = 0.0;

            list = checkForPCNs(con, stmt2, rs2, lastPICode, itemCode, poDate, dateTo, list, listLen);
          }
          else first = false;

          lastPICode = piCode;
        }

        qtyForThisItem += generalUtils.doubleFromStr(rs.getString(1));
        amtForThisItem += generalUtils.doubleFromStr(rs.getString(2));
      
        lastDate = rs.getString(3);
      }

      if(lastPICode.length() > 0)
      {
        list = addToTmp("PI", qtyForThisItem, amtForThisItem, lastDate, lastPICode, list, listLen);

        list = checkForPCNs(con, stmt2, rs2, lastPICode, itemCode, poDate, dateTo, list, listLen);
      }

      piFound[0] = true;
    }
    catch(Exception e)
    {
      System.out.println("PI chk : " + poCode + " " + e);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    return list;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private byte[] checkForPCNs(Connection con, Statement stmt, ResultSet rs, String piCode, String itemCode, String dateFrom, String dateTo, byte[] list, int[] listLen) throws Exception
  {
    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT t2.Quantity, t2.Amount, t1.Date, t1.PCNCode FROM pcreditl AS t2 INNER JOIN pcredit AS t1 ON t1.PCNCode = t2.PCNCode WHERE t2.InvoiceCode = '" + piCode + "' AND t2.ItemCode = '" + itemCode
                           + "' AND t1.Status != 'C' AND t1.Date >= {d '" + dateFrom + "'} AND t1.Date <= {d '" + dateTo + "'}");

      String pcnCode = "", lastPCNCode = "", lastDate = "";
      double qtyForThisItem = 0.0, amtForThisItem = 0.0;
      boolean first = true;

      while(rs.next())
      {
        pcnCode = rs.getString(4);

        if(! pcnCode.equals(lastPCNCode))
        {
          if(! first)
          {
            list = addToTmp("PC", qtyForThisItem, amtForThisItem, lastDate, lastPCNCode, list, listLen);
            qtyForThisItem = amtForThisItem = 0.0;
          }
          else first = false;

          lastPCNCode = pcnCode;
        }

        qtyForThisItem += generalUtils.doubleFromStr(rs.getString(1));
        amtForThisItem += generalUtils.doubleFromStr(rs.getString(2));
        
        lastDate    = rs.getString(3);
      }

      if(lastPCNCode.length() > 0 && ! pcnCode.equals(lastPCNCode))
        list = addToTmp("PC", qtyForThisItem, amtForThisItem, lastDate, lastPCNCode, list, listLen);
    }
    catch(Exception e)
    {
      System.out.println("PCN chk : " + piCode + " " + e);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    return list;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private byte[] addToTmp(String docType, double qty, double amt, String date, String docCode, byte[] list, int[] listLen) throws Exception
  {
    String s = date + "-" + docType + "\002" + qty + "\002" + amt + "\002" + docCode + "\002\001";

    byte[] newItem = new byte[s.length() + 1];
    generalUtils.strToBytes(newItem, s);

    list = generalUtils.addToList(true, newItem, list, listLen);

    return list;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private byte[] repInTmp(int entryNum, double qty, double amt, String dateAndType, String docCode, byte[] list, int[] listLen) throws Exception
  {
    String s = dateAndType + "\002" + qty + "\002" + amt + "\002" + docCode + "\002\001";

    byte[] newEntry = new byte[s.length() + 1];
    generalUtils.strToBytes(newEntry, s);

    list = generalUtils.repListEntryByNum(entryNum, newEntry, list, listLen);

    return list;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private double wacAtInvoiceTime(String invoiceCode, byte[] list) throws Exception
  {
    String dateAndType, docType, amtStr, docCode;
    byte[] entry = new byte[1001]; // plenty
    int x, y, len, entryNum = 0;
    while(generalUtils.getListEntryByNum(entryNum++, list, entry))
    {
      x = 0;
      len = generalUtils.lengthBytes(entry, 0);

      dateAndType ="";
      while(x < len && entry[x] != '\002') // just-in-case
        dateAndType += (char)entry[x++];
      y = dateAndType.length();
      docType = "" + dateAndType.charAt(y - 2) + dateAndType.charAt(y - 1);

      if(docType.equals("SI")) // in case we match against the CN entry
      {
        ++x;
        while(x < len && entry[x] != '\002') // just-in-case
          ++x;
        ++x;
        amtStr = "";
        while(x < len && entry[x] != '\002') // just-in-case
         amtStr += (char)entry[x++];
        ++x;
        docCode = "";
        while(x < len && entry[x] != '\002') // just-in-case
          docCode += (char)entry[x++];

        if(docCode.equals(invoiceCode))
          return generalUtils.doubleDPs(generalUtils.doubleFromStr(amtStr), '2');
      }
    }

    return 0.0; // just-in-case
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void updateWAC(Connection con, Statement stmt, ResultSet rs, String itemCode, int startDateEncoded, int lastDateEncoded, double wac) throws Exception
  {
    try
    {
      int dayNum;
      if(startDateEncoded == -1) // OB
        dayNum = -1;
      else dayNum = lastDateEncoded - startDateEncoded;

      // check if rec already exists
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT * FROM wacmeta WHERE ItemCode = '" + generalUtils.sanitiseForSQL(itemCode) + "'");

      double[] wacs = new double[368]; // [0] not used, [1] is OB, [2] is first day of year
      boolean exists = false;
      int x;

      if(rs.next())
      {
        for(x=1;x<368;++x)
          wacs[x] = generalUtils.doubleFromStr(generalUtils.deNull(rs.getString(x)));
        exists = true;
      }
      else
      {
        for(x=1;x<368;++x)
          wacs[x] = 0.0;
      }

      rs.close();
      stmt.close();

      for(x=(dayNum + 2);x<368;++x)
        wacs[x] = wac;

      if(exists)
      {
        stmt = con.createStatement();

        String q = "UPDATE wacmeta SET ";

        for(x=2;x<368;++x)
        {
          if(x > 2)
            q += ",";

          q += "Date" + (x - 2) + "='" + wacs[x] + "'";
        }

        q += " WHERE ItemCode = '" + generalUtils.sanitiseForSQL(itemCode) + "'";

        stmt.executeUpdate(q);

        stmt.close();
      }
      else // create new rec
      {
        stmt = con.createStatement();

        String q = "INSERT INTO wacmeta ( ItemCode";

        for(x=2;x<368;++x)
          q += ", Date" + (x - 2);

        q += ") VALUES ('" + generalUtils.sanitiseForSQL(itemCode);

        for(x=2;x<368;++x)
          q += "','" + wac;

        q += "')";

        stmt.executeUpdate(q);

        stmt.close();
      }
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public double getWAC(Connection con, Statement stmt, ResultSet rs, String itemCode, String startDate, String dateReqd) throws Exception
  {
    double[] numItemsPurchased = new double[1];
    return getWAC(con, stmt, rs, itemCode, startDate, dateReqd, numItemsPurchased);
  }
  public double getWAC(Connection con, Statement stmt, ResultSet rs, String itemCode, String startDate, String dateReqd, double[] numItemsPurchased) throws Exception
  {
    double wac = 0.0;
    int dayNum=0;
    
    try
    {
      dayNum = generalUtils.encodeFromYYYYMMDD(dateReqd) - generalUtils.encodeFromYYYYMMDD(startDate);

      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT Date" + dayNum + " FROM wacmeta WHERE ItemCode = '" + generalUtils.sanitiseForSQL(itemCode) + "'");

      if(rs.next())
        wac = generalUtils.doubleFromStr(rs.getString(1));

      rs.close();
      stmt.close();
    }
    catch(Exception e)
    {
      System.out.println(e + " " + dayNum + " " + startDate + " " + dateReqd);
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }

    return wac;
  }

}
