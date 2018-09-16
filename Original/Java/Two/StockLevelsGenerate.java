// =======================================================================================================================================================================================================
// System: ZaraStar: Stock: Generate Stock Levels
// Module: StockLevelsGenerate.java
// Author: C.K.Harvey
// Copyright (c) 2002-08 Christopher Harvey. All Rights Reserved.
// Returns stocklevel for each store: "SamLeong\001150000\001SyedAlwi\00150000\001\0"
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

public class StockLevelsGenerate extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  DocumentUtils documentUtils = new DocumentUtils();
  MiscDefinitions miscDefinitions = new MiscDefinitions();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="";

    try
    {
      directoryUtils.setContentHeaders(res);
      out = res.getWriter();

      unm = req.getParameter("unm");
      sid = req.getParameter("sid");
      uty = req.getParameter("uty");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");
      p1  = req.getParameter("p1"); // itemCode
      p2  = req.getParameter("p2"); // uptoDate (not YYYYMMDD format)
      
      if(p1 == null) p1 = "";
      if(p2 == null) p2 = "";
      
      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, bytesOut);
    }
    catch(Exception e)
    {
      StringBuffer url = req.getRequestURL();
      int x=0;
      if(url.charAt(x) == 'h' || url.charAt(x) == 'H')
        x += 7;
      String urlBit="";
      while(url.charAt(x) != '\000' && url.charAt(x) != ',' && url.charAt(x) != '/')
        urlBit += url.charAt(x++);

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "StockLevelsGenerate", bytesOut);

      serverUtils.etotalBytes(req, unm, dnm, 3052, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush(); 
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1, String p2, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir     = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(uty.equals("R")) // from a reseller server
    {
      ;
    }
    else // assume internal user
    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      serverUtils.etotalBytes(req, unm, dnm, 3052, bytesOut[0], 0, "SID:" + p1);
      out.println("");
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }
    
    p1 = p1.toUpperCase();
    
    if(! existsItemRecGivenCode(con, stmt, rs, p1)) // cannot link from inventory (recursive servlet load)
      out.println("-\0010");
    else
    {    
      if(p2.length() == 0)
        p2 = "2099-12-31";
      else p2 = generalUtils.convertDateToSQLFormat(p2);

      if(! generalUtils.validateDateSQL(true, p2, localDefnsDir, defnsDir))
        out.println("-\0010");
      else go(con, stmt, rs, out, req, p1, p2, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir);
    }
    
    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 3052, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con  != null) con.close();
    if(out != null) out.flush(); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String fetch(Connection con, Statement stmt, Statement stmt2, ResultSet rs, String itemCode, String dateTo, String thisOneStoreWanted, String unm, String sid, String uty, String men, String den, String dnm, String bnm) throws Exception
  {
    itemCode = itemCode.toUpperCase();

    String dateToYYYYMMDD;
    if(dateTo.length() == 0)
      dateToYYYYMMDD = "2099-12-31";
    else dateToYYYYMMDD = generalUtils.convertDateToSQLFormat(dateTo);

    String[][] stores = new String[1][1];
    int numStores;

    if(thisOneStoreWanted.length() > 0)
    {
      stores[0][0] = thisOneStoreWanted;
      numStores = 1;
    }
    else numStores = documentUtils.getStoresList(con, stmt, rs, stores);

    // Return stocklevel for each store: "SamLeong\001150000\001SyedAlwi\00150000\001\0"
    String rtnList = generate2(con, stmt, stmt2, rs, itemCode, dateToYYYYMMDD, stores[0], numStores, unm);

    return rtnList;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String generate2(Connection con, Statement stmt, Statement stmt2, ResultSet rs, String itemCode, String dateTo, String[] stores, int numStores, String unm) throws Exception
  {
    int x;

    String[][] dates  = new String[1][numStores];
    String[][] levels = new String[1][numStores];

    String[] tmpTables = new String[numStores];

    double[] balances = new double[numStores];
    
    String tmpID = generalUtils.newRandom();

    for(x=0;x<numStores;++x)
    {
      int i;
      i = unm.indexOf("_");
      if(i != -1) // registered user
        tmpTables[x] = unm.substring(0, i) + "_tmp" + tmpID + x;
      else tmpTables[x] = unm + "_tmp" + tmpID + x;

      directoryUtils.createTmpTable(true, con, stmt2, "DocumentType char(1), Date date, Level char(20)", "", tmpTables[x]);

      getFirstStockCheckRecords(con, stmt, rs, itemCode, dateTo, numStores, stores, dates, levels);

      balances[x] = generalUtils.doubleFromStr(levels[0][x]);
    }

    getPickingLists(con, stmt, stmt2, rs, itemCode, dateTo, stores, numStores, tmpTables);

    getGoodsReceivedNotes(con, stmt, stmt2, rs, itemCode, dateTo, stores, numStores, tmpTables);

    getStockAdjustmentRecords(con, stmt, rs, itemCode, dateTo, numStores, stores, tmpTables);

    // now read all store tmp files and total

    Statement[] stmts = new Statement[numStores];
    ResultSet[] rss   = new ResultSet[numStores];

    char type;
    String level, rtnList = "";

    for(x=0;x<numStores;++x)
    {
      stmts[x] = con.createStatement();

      rss[x] = stmts[x].executeQuery("SELECT DocumentType, Level FROM " + tmpTables[x] + " WHERE Date >= {d '" + dates[0][x] + "'}");

      while(rss[x].next())
      {
        type  = rss[x].getString(1).charAt(0);
        level = rss[x].getString(2);

        // qty, either in In column or Out column
        
        if(type == 'P') // PL
        {
          balances[x] -= generalUtils.doubleFromStr(level);
        }
        else
        if(type == 'G') // GRN
        {
          balances[x] += generalUtils.doubleFromStr(level);
        }  
        else // adjustment
        {
          if(type == 'I') // is an In to this store
          {
            balances[x] += generalUtils.doubleFromStr(level);
          }
          else // is an Out from this store
          {
            balances[x] -= generalUtils.doubleFromStr(level);
          }      
        }
      }
     
      rtnList += (stores[x] + "\001" + generalUtils.doubleToStr(balances[x]) + "\001");   
    }
    
    for(x=0;x<numStores;++x)
    {
      if(rss[x]   != null) rss[x].close();
      if(stmts[x] != null) stmts[x].close();
    }

    // remove tmp tables
    for(x=0;x<numStores;++x)
      directoryUtils.removeTmpTable(con, stmt2, tmpTables[x]);

    return rtnList;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void go(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String itemCode, String dateTo, String unm, String sid, String uty, String men, String den,
                  String dnm, String bnm, String localDefnsDir, String defnsDir) throws Exception
  {
    String[][] stores = new String[1][1];
    int numStores = documentUtils.getStoresList(con, stmt, rs, stores);

    // Return stocklevel for each store: "SamLeong\001150000\001SyedAlwi\00150000\001\0"
    String rtnList = generate(itemCode, dateTo, stores[0], numStores, unm, dnm);

    out.println(rtnList);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private String generate(String itemCode, String dateTo, String[] stores, int numStores, String unm, String dnm) throws Exception
  {
    String userName = directoryUtils.getMySQLUserName();
    String passWord = directoryUtils.getMySQLPassWord();
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + userName + "&password=" + passWord);

    String rtnList = "";

    try
    {
    Statement stmt = null, stmt2 = null;
    ResultSet rs = null;

    int x;

    String[][] dates  = new String[1][numStores];
    String[][] levels = new String[1][numStores];

    String[] tmpTables = new String[numStores];

    double[] balances = new double[numStores];

    String tmpID = generalUtils.newRandom();

    for(x=0;x<numStores;++x)
    {
      int i;
      i = unm.indexOf("_");
      if(i != -1) // registered user
        tmpTables[x] = unm.substring(0, i) + "_tmp" + tmpID + x;
      else tmpTables[x] = unm + "_tmp" + tmpID + x;
      
      directoryUtils.createTmpTable(true, con, stmt2, "DocumentType char(1), Date date, Level char(20)", "", tmpTables[x]);

      getFirstStockCheckRecords(con, stmt, rs, itemCode, dateTo, numStores, stores, dates, levels);

      balances[x] = generalUtils.doubleFromStr(levels[0][x]);
    }

    getPickingLists(con, stmt, stmt2, rs, itemCode, dateTo, stores, numStores, tmpTables);

    getGoodsReceivedNotes(con, stmt, stmt2, rs, itemCode, dateTo, stores, numStores, tmpTables);

    getStockAdjustmentRecords(con, stmt, rs, itemCode, dateTo, numStores, stores, tmpTables);

    // now read all store tmp files and total

    Statement[] stmts = new Statement[numStores];
    ResultSet[] rss   = new ResultSet[numStores];

    char type;
    String level;

    for(x=0;x<numStores;++x)
    {
      stmts[x] = con.createStatement();

      rss[x] = stmts[x].executeQuery("SELECT DocumentType, Level FROM " + tmpTables[x] + " WHERE Date >= {d '" + dates[0][x] + "'}");

      while(rss[x].next())
      {
        type  = rss[x].getString(1).charAt(0);
        level = rss[x].getString(2);

        // qty, either in In column or Out column
        
        if(type == 'P') // PL
        {
          balances[x] -= generalUtils.doubleFromStr(level);
        }
        else
        if(type == 'G') // GRN
        {
          balances[x] += generalUtils.doubleFromStr(level);
        }  
        else // adjustment
        {
          if(type == 'I') // is an In to this store
          {
            balances[x] += generalUtils.doubleFromStr(level);
          }
          else // is an Out from this store
          {
            balances[x] -= generalUtils.doubleFromStr(level);
          }      
        }
      }
      
      rtnList += (stores[x] + "\001" + generalUtils.doubleToStr(balances[x]) + "\001");   
    }
    
    for(x=0;x<numStores;++x)
    {
      if(rss[x]   != null) rss[x].close();
      if(stmts[x] != null) stmts[x].close();
    }

    // remove tmp tables
    for(x=0;x<numStores;++x)
      directoryUtils.removeTmpTable(con, stmt2, tmpTables[x]);
    }
    catch(Exception e) { System.out.println(e); }

    if(con != null) con.close();  

    return rtnList;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void getFirstStockCheckRecords(Connection con, Statement stmt, ResultSet rs, String itemCode, String dateTo, int numStores, String[] stores, String[][] dates, String[][] levels) throws Exception
  {
    for(int x=0;x<numStores;++x)
    {
      stmt = con.createStatement();
      stmt.setMaxRows(1);

      rs = stmt.executeQuery("SELECT Date, Level FROM stockc WHERE ItemCode = '" + generalUtils.sanitiseForSQL(itemCode) + "' AND StoreCode = '" + stores[x] + "' AND Status != 'C' AND Date <= {d '" + dateTo
                           + "'} AND Type = 'S' AND Level != '999999' ORDER BY Date DESC");
    
      if(rs.next())
      {
        dates[0][x]  = rs.getString(1);
        levels[0][x] = rs.getString(2);
      }
      else
      {
        dates[0][x]  = "1970-01-01";
        levels[0][x] = "0";
      }

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
  }
    
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void getPickingLists(Connection con, Statement stmt, Statement stmt2, ResultSet rs, String itemCode, String dateTo, String[] stores, int numStores,
                               String[] tmpTables) throws Exception
  {
    int x;
    boolean found;
    
    stmt = con.createStatement();
    rs = stmt.executeQuery("SELECT t1.Date, t2.QuantityPacked, t2.Store FROM pl AS t1 INNER JOIN pll AS t2 ON t1.PLCode = t2.PLCode "
                         + "WHERE t2.ItemCode = '" + generalUtils.sanitiseForSQL(itemCode) + "'" + " AND t1.Date <= {d '" + dateTo + "'} AND t1.Status != 'C' AND t1.Completed = 'Y'");
    
    while(rs.next())
    {
      x = 0;
      found = false;
      while(x < numStores && ! found) // just-in-case
      {
        if(rs.getString(3).equals(stores[x]))
        {
          addToTmpTable(con, stmt2, "P", rs.getString(1), rs.getString(2), tmpTables[x]);
          found = true;
        }
        
        ++x;
      }  
    }
      
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void getGoodsReceivedNotes(Connection con, Statement stmt, Statement stmt2, ResultSet rs, String itemCode, String dateTo, String[] stores,
                                     int numStores, String[] tmpTables) throws Exception
  {
    int x;
    boolean found;
    
    stmt = con.createStatement();
    rs = stmt.executeQuery("SELECT t1.Date, t2.Quantity, t2.StoreCode FROM gr AS t1 INNER JOIN grl AS t2 ON t1.GRCode = t2.GRCode "
                         + "WHERE t2.ItemCode = '" + generalUtils.sanitiseForSQL(itemCode) + "'" + " AND t1.Date <= {d '" + dateTo + "'}  AND t1.Status != 'C' AND t1.StockProcessed = 'Y'");
    
    while(rs.next())
    {
      x = 0;
      found = false;
      while(x < numStores && ! found) // just-in-case
      {
        if(rs.getString(3).equals(stores[x]))
        {
          addToTmpTable(con, stmt2, "G", rs.getString(1), rs.getString(2), tmpTables[x]);
          found = true;
        }
      
        ++x;
      }  
    }
      
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void getStockAdjustmentRecords(Connection con, Statement stmt, ResultSet rs, String itemCode, String dateTo, int numStores, String[] stores,
                                         String[] tmpTables) throws Exception
  {
    int x;
                                      
    stmt = con.createStatement();
    rs = stmt.executeQuery("SELECT Date, StoreFrom, StoreTo, Quantity FROM stocka WHERE ItemCode = '" + generalUtils.sanitiseForSQL(itemCode) + "' AND Date <= {d '" + dateTo
                         + "'}  AND Status != 'C'");
                                      
    while(rs.next())
    {
      for(x=0;x<numStores;++x)
      {
        // if StoreFrom has value and it matches this store mark as 'O' (transfer out)
        if(rs.getString(2).equals(stores[x]))
          addToTmpTable(con, stmt, "O", rs.getString(1), rs.getString(4), tmpTables[x]);
        
        // if StoreTo has value and it matches this store mark as 'I' (transfer in)
        if(rs.getString(3).equals(stores[x]))
          addToTmpTable(con, stmt, "I", rs.getString(1), rs.getString(4), tmpTables[x]);
      }
    }
                                        
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }
    
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void addToTmpTable(Connection con, Statement stmt, String docType, String date, String level, String tmpTable) throws Exception
  {
    stmt = con.createStatement();
   
    String q = "INSERT INTO " + tmpTable + " ( DocumentType, Date, Level ) VALUES ('" + docType + "', {d '" + date + "'},'" + level + "' )";

    stmt.executeUpdate(q);
  
    if(stmt != null) stmt.close();
  }

  //--------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean existsItemRecGivenCode(Connection con, Statement stmt, ResultSet rs, String code) throws Exception
  {
    if(code.length() == 0) // just-in-case
      return false;

    code = code.toUpperCase();

    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT COUNT(1) AS rowcount FROM stock WHERE ItemCode = '" + generalUtils.sanitiseForSQL(code) + "'");
    int numRecs = 0;
    if(rs.next())
      numRecs = rs.getInt("rowcount") ;
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    if(numRecs == 1)
      return true;
    
    return false;
  }
  
}
