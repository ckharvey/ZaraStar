// =======================================================================================================================================================================================================
// System: ZaraStar Accounts: Gross profit calculations
// Module: AccountsCalculations.java
// Author: C.K.Harvey
// Copyright (c) 2007-09 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import java.sql.*;

public class AccountsCalculations 
{
  GeneralUtils generalUtils = new GeneralUtils();
  Inventory inventory = new Inventory();
  DefinitionTables definitionTables = new DefinitionTables();
  AccountsUtils accountsUtils = new AccountsUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void calcCostOfSale(String soCode, double[] cos, double[] invoiced, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {  
    String userName = directoryUtils.getMySQLUserName();
    String passWord = directoryUtils.getMySQLPassWord();
    
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + userName + "&password=" + passWord);
    
    ResultSet rs = null, rs2 = null, rs3 = null, rs4 = null;
    Statement stmt = null, stmt2 = null, stmt3 = null, stmt4 = null;

    String[][]  codes      = new String [1][100];
    String[][]  sourceDocs = new String [1][100];
    char[][]    types      = new char[1][100];
    boolean[][] searched   = new boolean[1][100];
    
    int[] codesLen      = new int[1];  codesLen[0]      = 100;
    int[] sourceDocsLen = new int[1];  sourceDocsLen[0] = 100;

    int x, numEntries = 0;
    
    numEntries = searchSOHeadersGivenSOCode(con, stmt, rs, soCode, codes, types, sourceDocs, searched, codesLen, sourceDocsLen, numEntries, localDefnsDir, defnsDir);
    numEntries = searchPLLinesGivenSOCode(con, stmt, rs, soCode, codes, types, sourceDocs, searched, codesLen, sourceDocsLen, numEntries, localDefnsDir, defnsDir);
    numEntries = searchPOLinesGivenSOCode(con, stmt, rs, soCode, codes, types, sourceDocs, searched, codesLen, sourceDocsLen, numEntries, localDefnsDir, defnsDir);
    numEntries = searchLPLinesGivenSOCode(con, stmt, rs, soCode, codes, types, sourceDocs, searched, codesLen, sourceDocsLen, numEntries, localDefnsDir, defnsDir);
    numEntries = searchDOLinesGivenSOCode(con, stmt, rs, soCode, codes, types, sourceDocs, searched, codesLen, sourceDocsLen, numEntries, localDefnsDir, defnsDir);
    numEntries = searchInvoiceLinesGivenSOCode(con, stmt, rs, soCode, codes, types, sourceDocs, searched, codesLen, sourceDocsLen, numEntries, localDefnsDir, defnsDir);
    numEntries = searchOCHeadersGivenSOCode(con, stmt, rs, soCode, codes, types, sourceDocs, searched, codesLen, sourceDocsLen, numEntries, localDefnsDir, defnsDir);
    numEntries = searchProformaLinesGivenSOCode(con, stmt, rs, soCode, codes, types, sourceDocs, searched, codesLen, sourceDocsLen, numEntries, localDefnsDir, defnsDir);

    numEntries = addToList(soCode, 'S', soCode, numEntries, codes, types, sourceDocs, searched, codesLen, sourceDocsLen);
    
    boolean atLeastOneRemaining = true;
    while(atLeastOneRemaining)
    {
      for(x=0;x<numEntries;++x)
      {
        if(! searched[0][x])
        {
          numEntries = allSearches(con, stmt, rs, codes[0][x], types[0][x], codes, types, sourceDocs, searched, codesLen, sourceDocsLen, numEntries, localDefnsDir, defnsDir);
          searched[0][x] = true;
        }
      }

      atLeastOneRemaining = false;
      for(x=0;x<numEntries;++x)
      {
        if(! searched[0][x])
          atLeastOneRemaining = true;
      }
    }

    calc(con, stmt, stmt2, stmt3, stmt4, rs, rs2, rs3, rs4, numEntries, codes[0], types[0], soCode, dnm, localDefnsDir, defnsDir, cos, invoiced);
    
    if(rs   != null) rs.close();
    if(con  != null) con.close();
  }   
        
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int allSearches(Connection con, Statement stmt, ResultSet rs, String thisCode, char thisType, String[][] codes, char[][] types, String[][] sourceDocs, boolean[][] searched, int[] codesLen,
                          int[] sourceDocsLen, int numEntries, String localDefnsDir, String defnsDir) throws Exception
  {
    if(thisType == 'Y')
    {
      numEntries = searchGRNLinesGivenPOCode(con, stmt, rs, thisCode, codes, types, sourceDocs, searched, codesLen, sourceDocsLen, numEntries, localDefnsDir, defnsDir);
    }
    else
    if(thisType == 'G')
    {
      numEntries = searchPILinesGivenGRCode(con, stmt, rs, thisCode, codes, types, sourceDocs, searched, codesLen, sourceDocsLen, numEntries, localDefnsDir, defnsDir);
    }

    return numEntries;
  }
    
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int addToList(String newCode, char newType, String newSourceDoc, int numEntries, String[][] codes, char[][] types, String[][] sourceDocs, boolean[][] searched, int[] codesLen, int[] sourceDocsLen) throws Exception
  {
    if(newCode == null || newCode.length() == 0)
      return numEntries;
    
    for(int x=0;x<numEntries;++x)
    {
      if(codes[0][x].equalsIgnoreCase(newCode))
      {
        if(types[0][x] == newType)
        {
          return numEntries; // already on list
        }     
      }
    }
    
    if(numEntries >= codesLen[0])
    {
      int y, len = codesLen[0];
      
      String[] buf = new String[len];
      for(y=0;y<len;++y)
        buf[y] = codes[0][y];
      codesLen[0] += 100;
      codes[0] = new String[codesLen[0]];
      for(y=0;y<len;++y)
        codes[0][y] = buf[y];

      char[] cbuf = new char[len];
      for(y=0;y<len;++y)
        cbuf[y] = types[0][y];
      types[0] = new char[codesLen[0]];
      for(y=0;y<len;++y)
        types[0][y] = cbuf[y];
      
      String[] buf2 = new String[len];
      for(y=0;y<len;++y)
        buf2[y] = sourceDocs[0][y];
      sourceDocsLen[0] += 100;
      sourceDocs[0] = new String[sourceDocsLen[0]];
      for(y=0;y<len;++y)
        sourceDocs[0][y] = buf2[y];

      boolean[] bbuf = new boolean[len];
      for(y=0;y<len;++y)
        bbuf[y] = searched[0][y];
      searched[0] = new boolean[codesLen[0]];
      for(y=0;y<len;++y)
        searched[0][y] = bbuf[y];
    }
    
    codes[0][numEntries] = newCode;
    types[0][numEntries] = newType;
    sourceDocs[0][numEntries] = newSourceDoc;
    searched[0][numEntries] = false;

    return (numEntries + 1);
  }
    
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void calc(Connection con, Statement stmt, Statement stmt2, Statement stmt3, Statement stmt4, ResultSet rs, ResultSet rs2, ResultSet rs3, ResultSet rs4, int numEntries, String[] codes, 
                    char[] types, String soCode, String dnm, String localDefnsDir, String defnsDir, double[] cos, double[] invoiced) throws Exception
  {
    cos[0] = invoiced[0] = 0.0;
    
    for(int x=0;x<numEntries;++x)
    {
      switch(types[x])
      {
        case 'S' : invoiced[0] += chkInvoiceLinesForInvoicedAmount(con, stmt, rs, soCode);
                   break;
        case 'I' : cos[0]      += chkInvoiceLines(true, con, stmt, stmt2, rs, rs2, codes[x], dnm, localDefnsDir, defnsDir);
                   break;
        case 'Y' : cos[0] += chkPurchaseInvoiceLines(con, stmt, stmt2, stmt3, stmt4, rs, rs2, rs3, rs4, soCode, dnm, localDefnsDir, defnsDir);
                   break;
      }
    }
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean calcDetailsSO(Connection con, Statement stmt, ResultSet rs, String soCode, String[] currency, String[] date, double[] amount, double[] amount2) throws Exception
  {    
    stmt = con.createStatement();
     
    rs = stmt.executeQuery("SELECT TotalTotal, BaseTotalTotal, Date, Currency2, Status FROM so WHERE SOCode = '" + soCode + "'");

    String status = "L";
    amount[0] = amount2[0] = 0.0;
     
    if(rs.next()) // just-in-case
    {
      amount[0]   = generalUtils.doubleFromStr(generalUtils.doubleDPs(rs.getString(1), '2'));
      amount2[0]  = generalUtils.doubleFromStr(generalUtils.doubleDPs(rs.getString(2), '2'));
      date[0]     = generalUtils.convertFromYYYYMMDD(rs.getString(3));
      currency[0] = rs.getString(4);
      status      = rs.getString(5);
    }
         
    rs.close();
    stmt.close();
    
    if(status.equals("C"))
      return false;
    
    return true;
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean calcDetailsPL(Connection con, Statement stmt, ResultSet rs, String soCode, String docCode, String[] currency, String[] date, double[] amount, double[] amount2) throws Exception
  {    
    stmt = con.createStatement();
     
    rs = stmt.executeQuery("SELECT t2.Amount2, t2.Amount, t1.Date, t1.Currency, t1.Status FROM pll AS t2 INNER JOIN pl AS t1 "
                         + "ON t2.PLCode = t1.PLCode WHERE t2.SOCode = '" + soCode + "' AND t2.PLCode = '" + docCode + "'");

    String status = "L";
    amount[0] = amount2[0] = 0.0;
     
    while(rs.next())
    {
      amount[0]  += generalUtils.doubleFromStr(generalUtils.doubleDPs(rs.getString(1), '2'));
      amount2[0] += generalUtils.doubleFromStr(generalUtils.doubleDPs(rs.getString(2), '2'));
      date[0]     = generalUtils.convertFromYYYYMMDD(rs.getString(3));
      currency[0] = rs.getString(4);
      status      = rs.getString(5);
    }
         
    rs.close();
    stmt.close();
    
    if(status.equals("C"))
      return false;
    
    return true;
  }
  
  // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean calcDetailsDO(Connection con, Statement stmt, ResultSet rs, String soCode, String docCode, String[] currency, String[] date, double[] amount, double[] amount2) throws Exception 
  {    
    stmt = con.createStatement();
     
    rs = stmt.executeQuery("SELECT t2.Amount2, t2.Amount, t1.Date, t1.Currency, t1.Status FROM dol AS t2 INNER JOIN do AS t1 "
                         + "ON t2.DOCode = t1.DOCode WHERE t2.SOCode = '" + soCode + "' AND t2.DOCode = '" + docCode + "'");

    String status = "L";
    amount[0] = amount2[0] = 0.0;
     
    while(rs.next())
    {
      amount[0]  += generalUtils.doubleFromStr(generalUtils.doubleDPs(rs.getString(1), '2'));
      amount2[0] += generalUtils.doubleFromStr(generalUtils.doubleDPs(rs.getString(2), '2'));
      date[0]     = generalUtils.convertFromYYYYMMDD(rs.getString(3));
      currency[0] = rs.getString(4);
      status      = rs.getString(5);
    }
         
    rs.close();
    stmt.close();
    
    if(status.equals("C"))
      return false;
    
    return true;
  }
  
  // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean calcDetailsInvoice(Connection con, Statement stmt, ResultSet rs, String soCode, String docCode, String[] currency, String[] date, double[] amount, double[] amount2) throws Exception 
  {    
    stmt = con.createStatement();
     
    rs = stmt.executeQuery("SELECT t2.Amount2, t2.Amount, t1.Date, t1.Currency, t1.Status FROM invoicel AS t2 INNER JOIN invoice AS t1 "
                         + "ON t2.InvoiceCode = t1.InvoiceCode WHERE t2.SOCode = '" + soCode + "' AND t2.InvoiceCode = '" + docCode + "'");

    String status = "L";
    amount[0] = amount2[0] = 0.0;
     
    while(rs.next())
    {
      amount[0]  += generalUtils.doubleFromStr(generalUtils.doubleDPs(rs.getString(1), '2'));
      amount2[0] += generalUtils.doubleFromStr(generalUtils.doubleDPs(rs.getString(2), '2'));
      date[0]     = generalUtils.convertFromYYYYMMDD(rs.getString(3));
      currency[0] = rs.getString(4);
      status      = rs.getString(5);
    }
         
    rs.close();
    stmt.close();
    
    if(status.equals("C"))
      return false;
    
    return true;
  }
  
  // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean calcDetailsPInvoice(Connection con, Statement stmt, ResultSet rs, String grCode, String docCode, String[] currency, String[] date, double[] amount, double[] amount2) throws Exception 
  {    
    stmt = con.createStatement();
     
    rs = stmt.executeQuery("SELECT t2.Amount2, t2.Amount, t1.Date, t1.Currency, t1.Status FROM pinvoicel AS t2 INNER JOIN pinvoice AS t1 "
                         + "ON t2.InvoiceCode = t1.InvoiceCode WHERE t2.GRCode = '" + grCode + "' AND t2.InvoiceCode = '" + docCode + "'");

    String status = "L";
    amount[0] = amount2[0] = 0.0;
     
    while(rs.next())
    {
      amount[0]  += generalUtils.doubleFromStr(generalUtils.doubleDPs(rs.getString(1), '2'));
      amount2[0] += generalUtils.doubleFromStr(generalUtils.doubleDPs(rs.getString(2), '2'));
      date[0]     = generalUtils.convertFromYYYYMMDD(rs.getString(3));
      currency[0] = rs.getString(4);
      status      = rs.getString(5);
    }
         
    rs.close();
    stmt.close();
    
    if(status.equals("C"))
      return false;
    
    return true;
  }
  
  // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean calcDetailsGR(Connection con, Statement stmt, ResultSet rs, String poCode, String docCode, String[] currency, String[] date, double[] amount, double[] amount2) throws Exception 
  {    
    stmt = con.createStatement();
     
    rs = stmt.executeQuery("SELECT t2.Amount, t1.Date, t1.Status FROM grl AS t2 INNER JOIN gr AS t1 "
                         + "ON t2.GRCode = t1.GRCode WHERE t2.POCode = '" + poCode + "' AND t2.GRCode = '" + docCode + "'");

    String status = "L";
    amount[0] = amount2[0] = 0.0;
     
    while(rs.next())
    {
      amount[0]  += generalUtils.doubleFromStr(generalUtils.doubleDPs(rs.getString(1), '2'));
      date[0]     = generalUtils.convertFromYYYYMMDD(rs.getString(2));
      status      = rs.getString(3);
    }
         
    rs.close();
    stmt.close();
    
    if(status.equals("C"))
      return false;
    
    return true;
  }
  
  // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean calcDetailsPO(Connection con, Statement stmt, ResultSet rs, String soCode, String docCode, String[] currency, String[] date, double[] amount, double[] amount2) throws Exception 
  {    
    stmt = con.createStatement();
     
    rs = stmt.executeQuery("SELECT t2.Amount2, t2.Amount, t1.Date, t1.Currency, t1.Status FROM pol AS t2 INNER JOIN po AS t1 "
                         + "ON t2.POCode = t1.POCode WHERE t2.SOCode = '" + soCode + "' AND t2.POCode = '" + docCode + "'");

    String status = "L";
    amount[0] = amount2[0] = 0.0;
     
    while(rs.next())
    {
      amount[0]  += generalUtils.doubleFromStr(generalUtils.doubleDPs(rs.getString(1), '2'));
      amount2[0] += generalUtils.doubleFromStr(generalUtils.doubleDPs(rs.getString(2), '2'));
      date[0]     = generalUtils.convertFromYYYYMMDD(rs.getString(3));
      currency[0] = rs.getString(4);
      status      = rs.getString(5);
    }
         
    rs.close();
    stmt.close();
    
    if(status.equals("C"))
      return false;
    
    return true;
  }

  // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean calcDetailsLP(Connection con, Statement stmt, ResultSet rs, String soCode, String docCode, String[] currency, String[] date, double[] amount, double[] amount2) throws Exception 
  {    
    stmt = con.createStatement();
     
    rs = stmt.executeQuery("SELECT t2.Amount2, t2.Amount, t1.Date, t1.Currency, t1.Status FROM LPl AS t2 INNER JOIN LP AS t1 "
                         + "ON t2.LPCode = t1.LPCode WHERE t2.SOCode = '" + soCode + "' AND t2.LPCode = '" + docCode + "'");

    String status = "L";
    amount[0] = amount2[0] = 0.0;
     
    while(rs.next())
    {
      amount[0]  += generalUtils.doubleFromStr(generalUtils.doubleDPs(rs.getString(1), '2'));
      amount2[0] += generalUtils.doubleFromStr(generalUtils.doubleDPs(rs.getString(2), '2'));
      date[0]     = generalUtils.convertFromYYYYMMDD(rs.getString(3));
      currency[0] = rs.getString(4);
      status      = rs.getString(5);
    }
   
    rs.close();
    stmt.close();
    
    if(status.equals("C"))
      return false;
    
    return true;
  }

  // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean checkCodeExists(String callerCode, Connection con, Statement stmt, ResultSet rs, String code, char type) throws Exception
  {   
    if(code.length() == 0)
      return false;
    
    boolean res = false;
    
    switch(type)
    {
      case 'Q' : res = existsGivenCode(con, stmt, rs, code, "quote",    "QuoteCode");    break;
      case 'S' : res = existsGivenCode(con, stmt, rs, code, "so",       "SOCode");       break;
      case 'O' : res = existsGivenCode(con, stmt, rs, code, "oc",       "OCCode");       break;
      case 'P' : res = existsGivenCode(con, stmt, rs, code, "pl",       "PLCode");       break;
      case 'D' : res = existsGivenCode(con, stmt, rs, code, "do",       "DOCode");       break;
      case 'I' : res = existsGivenCode(con, stmt, rs, code, "invoice",  "InvoiceCode");  break;
      case 'J' : res = existsGivenCode(con, stmt, rs, code, "pinvoice", "InvoiceCode");  break;
      case 'R' : res = existsGivenCode(con, stmt, rs, code, "proforma", "ProformaCode"); break;
      case 'C' : res = existsGivenCode(con, stmt, rs, code, "credit",   "CNCode");       break;
      case 'Y' : res = existsGivenCode(con, stmt, rs, code, "po",       "POCode");       break;
      case 'Z' : res = existsGivenCode(con, stmt, rs, code, "lp",       "LPCode");       break;
      case 'G' : res = existsGivenCode(con, stmt, rs, code, "gr",       "GRCode");       break;
      case 'N' : res = existsGivenCode(con, stmt, rs, code, "debit",    "DNCode");       break;
    } 

    return res;
  }      
      
  // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean existsGivenCode(Connection con, Statement stmt, ResultSet rs, String code, String tableName, String codeName) throws Exception
  {
    if(code.length() == 0) // just-in-case
      return false;
    
    if(code.contains("'"))
      return false;

    code = code.toUpperCase();

    stmt = con.createStatement();
    
    int numRecs = 0;
    
    rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM " + tableName + " WHERE " + codeName + " = '" + code + "'");
    if(rs.next()) // just-in-case      
      numRecs = rs.getInt("rowcount");

    rs.close();
    stmt.close();

    if(numRecs == 1)
      return true;
    
    return false;
  }
    
  // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int searchSOHeadersGivenSOCode(Connection con, Statement stmt, ResultSet rs, String soCode, String[][] codes, char[][] types, String[][] sourceDocs, boolean[][] searched, int[] codesLen,
                                         int[] sourceDocsLen, int numEntries, String localDefnsDir, String defnsDir) throws Exception
  {
    if(! checkCodeExists(soCode, con, stmt, rs, soCode, 'S')) return numEntries;   

    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT QuoteCode FROM so WHERE SOCode = '" + soCode + "'");

    while(rs.next())
    {    
      if(checkCodeExists(soCode, con, stmt, rs, rs.getString(1), 'Q'))
        numEntries = addToList(rs.getString(1), 'Q', soCode, numEntries, codes, types, sourceDocs, searched, codesLen, sourceDocsLen);
    }

    rs.close();
    stmt.close();
    
    return numEntries;
  }

  //-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int searchOCHeadersGivenSOCode(Connection con, Statement stmt, ResultSet rs, String soCode, String[][] codes, char[][] types, String[][] sourceDocs, boolean[][] searched, int[] codesLen,
                                         int[] sourceDocsLen, int numEntries, String localDefnsDir, String defnsDir) throws Exception
  {
    if(! checkCodeExists(soCode, con, stmt, rs, soCode, 'S')) return numEntries;   

    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT OCCode FROM oc WHERE SOCode = '" + soCode  + "'");

    while(rs.next())
    {    
      if(checkCodeExists(soCode, con, stmt, rs, rs.getString(1), 'O'))
        numEntries = addToList(rs.getString(1), 'O', soCode, numEntries, codes, types, sourceDocs, searched, codesLen, sourceDocsLen);
    }

    rs.close();
    stmt.close();
    
    return numEntries;
  }
  
  // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int searchProformaLinesGivenSOCode(Connection con, Statement stmt, ResultSet rs, String soCode, String[][] codes, char[][] types, String[][] sourceDocs, boolean[][] searched, int[] codesLen,
                                             int[] sourceDocsLen, int numEntries, String localDefnsDir, String defnsDir) throws Exception
  {
    if(! checkCodeExists(soCode, con, stmt, rs, soCode, 'S')) return numEntries;   

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT ProformaCode FROM proformal WHERE SOCode = '" + soCode + "'");

    while(rs.next())
    {    
      if(checkCodeExists(soCode, con, stmt, rs, rs.getString(1), 'R'))
        numEntries = addToList(rs.getString(1), 'R', soCode, numEntries, codes, types, sourceDocs, searched, codesLen, sourceDocsLen);
    }

    rs.close();
    stmt.close();
    
    return numEntries;
  }
    
  // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int searchPLLinesGivenSOCode(Connection con, Statement stmt, ResultSet rs, String soCode, String[][] codes, char[][] types, String[][] sourceDocs, boolean[][] searched, int[] codesLen,
                                       int[] sourceDocsLen, int numEntries, String localDefnsDir, String defnsDir) throws Exception
  {
    if(! checkCodeExists(soCode, con, stmt, rs, soCode, 'S')) return numEntries;   

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT PLCode FROM pll WHERE SOCode = '" + soCode + "'");

    while(rs.next())
    {    
      if(checkCodeExists(soCode, con, stmt, rs, rs.getString(1), 'P'))
        numEntries = addToList(rs.getString(1), 'P', soCode, numEntries, codes, types, sourceDocs, searched, codesLen, sourceDocsLen);
    }
    
    rs.close();
    stmt.close();
    
    return numEntries;
  }

  // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int searchDOLinesGivenSOCode(Connection con, Statement stmt, ResultSet rs, String soCode, String[][] codes, char[][] types, String[][] sourceDocs, boolean[][] searched, int[] codesLen, 
                                       int[] sourceDocsLen, int numEntries, String localDefnsDir, String defnsDir) throws Exception
  {
    if(! checkCodeExists(soCode, con, stmt, rs, soCode, 'S')) return numEntries;   

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT DOCode FROM dol WHERE SOCode = '" + soCode + "'");

    while(rs.next())
    {    
      if(checkCodeExists(soCode, con, stmt, rs, rs.getString(1), 'D'))
        numEntries = addToList(rs.getString(1), 'D', soCode, numEntries, codes, types, sourceDocs, searched, codesLen, sourceDocsLen);
    }
    
    rs.close();
    stmt.close();
    
    return numEntries;
  }

  // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int searchInvoiceLinesGivenSOCode(Connection con, Statement stmt, ResultSet rs, String soCode, String[][] codes, char[][] types, String[][] sourceDocs, boolean[][] searched, int[] codesLen,
                                            int[] sourceDocsLen, int numEntries, String localDefnsDir, String defnsDir) throws Exception
  {
    if(! checkCodeExists(soCode, con, stmt, rs, soCode, 'S')) return numEntries;   

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT InvoiceCode FROM invoicel WHERE SOCode = '" + soCode + "'");

    while(rs.next())
    {    
      if(checkCodeExists(soCode, con, stmt, rs, rs.getString(1), 'I'))
        numEntries = addToList(rs.getString(1), 'I', soCode, numEntries, codes, types, sourceDocs, searched, codesLen, sourceDocsLen);
    }
    
    rs.close();
    stmt.close();
    
    return numEntries;
  }

  // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int searchPOLinesGivenSOCode(Connection con, Statement stmt, ResultSet rs, String soCode, String[][] codes, char[][] types, String[][] sourceDocs, boolean[][] searched, int[] codesLen,
                                       int[] sourceDocsLen, int numEntries, String localDefnsDir, String defnsDir) throws Exception
  {
    if(! checkCodeExists(soCode, con, stmt, rs, soCode, 'S')) return numEntries;   
 
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT POCode FROM pol WHERE SOCode = '" + soCode  + "'");

    while(rs.next())
    {    
      if(checkCodeExists(soCode, con, stmt, rs, rs.getString(1), 'Y'))
        numEntries = addToList(rs.getString(1), 'Y', soCode, numEntries, codes, types, sourceDocs, searched, codesLen, sourceDocsLen);
    }

    rs.close();
    stmt.close();
    
    return numEntries;
  }
  
  // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int searchLPLinesGivenSOCode(Connection con, Statement stmt, ResultSet rs, String soCode, String[][] codes, char[][] types, String[][] sourceDocs, boolean[][] searched, int[] codesLen,
                                       int[] sourceDocsLen, int numEntries, String localDefnsDir, String defnsDir) throws Exception
  {
    if(! checkCodeExists(soCode, con, stmt, rs, soCode, 'S')) return numEntries;   

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT LPCode FROM lpl WHERE SOCode = '" + soCode  + "'");

    while(rs.next())
    {    
      if(checkCodeExists(soCode, con, stmt, rs, rs.getString(1), 'Z'))
        numEntries = addToList(rs.getString(1), 'Z', soCode, numEntries, codes, types, sourceDocs, searched, codesLen, sourceDocsLen);
    }

    rs.close();
    stmt.close();
    
    return numEntries;
  }

  // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int searchGRNLinesGivenPOCode(Connection con, Statement stmt, ResultSet rs, String poCode, String[][] codes, char[][] types, String[][] sourceDocs, boolean[][] searched, int[] codesLen,
                                        int[] sourceDocsLen, int numEntries, String localDefnsDir, String defnsDir) throws Exception
  {
    if(! checkCodeExists(poCode, con, stmt, rs, poCode, 'Y')) return numEntries;   

    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT GRCode FROM grl WHERE POCode = '" + poCode + "'");
    
    while(rs.next())
    {    
      if(checkCodeExists(poCode, con, stmt, rs, rs.getString(1), 'G'))
        numEntries = addToList(rs.getString(1), 'G', poCode, numEntries, codes, types, sourceDocs, searched, codesLen, sourceDocsLen);
    }
    
    rs.close();
    stmt.close();
    
    return numEntries;
  }
  
  // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int searchPILinesGivenGRCode(Connection con, Statement stmt, ResultSet rs, String grCode, String[][] codes, char[][] types, String[][] sourceDocs, boolean[][] searched, int[] codesLen,
                                       int[] sourceDocsLen, int numEntries, String localDefnsDir, String defnsDir) throws Exception
  {
    if(! checkCodeExists(grCode, con, stmt, rs, grCode, 'G')) return numEntries;

    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT InvoiceCode FROM pinvoicel WHERE GRCode = '" + grCode + "'");
    
    while(rs.next())
    {    
      if(checkCodeExists(grCode, con, stmt, rs, rs.getString(1), 'J'))
        numEntries = addToList(rs.getString(1), 'J', grCode, numEntries, codes, types, sourceDocs, searched, codesLen, sourceDocsLen);
    }
    
    rs.close();
    stmt.close();
    
    return numEntries;
  }
  
  // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private double chkInvoiceLines(boolean useWAC, Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String invoiceCode, String dnm, String localDefnsDir, String defnsDir)
                                 throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.Amount, t2.ItemCode, t2.Quantity, t1.Date FROM invoicel AS t2 INNER JOIN invoice AS t1 "
                         + "ON t1.InvoiceCode = t2.InvoiceCode WHERE t2.InvoiceCode = '" + invoiceCode + "'");

    double baseTotal = 0.0, baseAmount, quantity;
    String itemCode, date, dateFrom, s;
    String[] financialYearStartMonth = new String[1];
    String[] financialYearEndMonth   = new String[1];
    int startMonth;
    
    while(rs.next())
    {    
      itemCode = rs.getString(2);

      if(itemExists(con, stmt2, rs2, itemCode))
      {      
        if(useWAC)
        {
          quantity = generalUtils.doubleFromStr(rs.getString(3));
          date     = rs.getString(4);
        
          definitionTables.getAppConfigFinancialYearMonths(con, stmt2, rs2, dnm, financialYearStartMonth, financialYearEndMonth);

          startMonth = generalUtils.detMonthNumFromMonthName(financialYearStartMonth[0]);
          if(startMonth < 10)
            s = "-0" + startMonth;
          else s = "-" + startMonth;

          dateFrom = accountsUtils.getAccountingYearForADate(con, stmt2, rs2, generalUtils.convertFromYYYYMMDD(date), dnm, localDefnsDir, defnsDir) + s + "-01";

          baseAmount = inventory.getWAC(con, stmt2, rs2, itemCode, dateFrom, date, dnm);

          baseAmount *= quantity;
        }
        else baseAmount = generalUtils.doubleFromStr(rs.getString(1));

        baseTotal += baseAmount;
      }        
    }

    rs.close();
    stmt.close();
    
    return baseTotal;
  }

  // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean itemExists(Connection con, Statement stmt, ResultSet rs, String itemCode) throws Exception
  {
    if(itemCode.length() == 0) // just-in-case
      return false;

    if(itemCode.equals("-")) // quick check
      return false;

    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM stock WHERE ItemCode = '" + itemCode + "'");

    int numRecs = 0;
    
    if(rs.next())
      numRecs = rs.getInt("rowcount") ;

    rs.close();
    stmt.close();

    if(numRecs == 1)
      return true;
    
    return false;
  }
  
  // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private double chkPurchaseInvoiceLines(Connection con, Statement stmt, Statement stmt2, Statement stmt3, Statement stmt4, ResultSet rs, ResultSet rs2, ResultSet rs3, ResultSet rs4, String soCode,
                                         String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    double baseTotal = 0.0;

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT poCode FROM pol WHERE SOCode = '" + soCode + "'");

    String poCode, grCode, itemCode;
    
    while(rs.next())
    {    
      poCode = rs.getString(1);

      stmt2 = con.createStatement();

      rs2 = stmt2.executeQuery("SELECT grCode FROM grl WHERE POCode = '" + poCode + "'");
    
      while(rs2.next())
      {    
        grCode = rs2.getString(1);

        stmt3 = con.createStatement();

        rs3 = stmt3.executeQuery("SELECT t2.Amount, t2.ItemCode FROM pinvoicel AS t2 INNER JOIN pinvoice AS t1 "
                               + "ON t1.InvoiceCode = t2.InvoiceCode WHERE t2.GRCode = '" + grCode + "'");

        while(rs3.next())
        {    
          itemCode = rs3.getString(2);

          if(! itemExists(con, stmt4, rs4, itemCode))
          {
            baseTotal += generalUtils.doubleFromStr(rs3.getString(1));
          }
        }

        rs3.close();
        stmt3.close();
      }

      rs2.close();
      stmt2.close();
    }

    rs.close();
    stmt.close();
    
    return baseTotal;
  }

  // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private double chkInvoiceLinesForInvoicedAmount(Connection con, Statement stmt, ResultSet rs, String soCode) throws Exception
  {
    double baseTotal = 0.0;

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT Amount FROM invoicel WHERE SOCode = '" + soCode + "'");

    while(rs.next())
      baseTotal += generalUtils.doubleFromStr(rs.getString(1));

    rs.close();
    stmt.close();
    
    return baseTotal;
  }

}
