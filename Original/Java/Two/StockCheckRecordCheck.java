// =======================================================================================================================================================================================================
// System: ZaraStar Accounts: Stock Check Record Check
// Module: StockCheckRecordCheck.java
// Author: C.K.Harvey
// Copyright (c) 2001-08 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;
import java.io.*;

public class StockCheckRecordCheck extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  MiscDefinitions miscDefinitions = new MiscDefinitions();
  Inventory inventory = new Inventory();
  DefinitionTables definitionTables = new DefinitionTables();
  AccountsUtils accountsUtils = new AccountsUtils();

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
      directoryUtils.setContentHeaders2(res);
      out = res.getWriter();

      unm = req.getParameter("unm");
      sid = req.getParameter("sid");
      uty = req.getParameter("uty");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");
      p1  = req.getParameter("p1"); // mfr
      p2  = req.getParameter("p2"); // date
      
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
 
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "StockCheckCheck", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 13066, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush(); 
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String mfr,
                    String date, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    
    Connection con = null;
    Statement stmt = null, stmt2 = null, stmt3 = null;
    ResultSet rs = null, rs2 = null, rs3 = null;

    String uName = directoryUtils.getMySQLUserName();
    String pWord = directoryUtils.getMySQLPassWord();
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + uName + "&password=" + pWord);

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 13066, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "StockCheckCheck", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 13066, bytesOut[0], 0, "ACC:" + mfr);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "StockCheckCheck", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 13066, bytesOut[0], 0, "SID:" + mfr);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(date.length() == 0)
      date = "1970-01-01";
    else date = generalUtils.convertDateToSQLFormat(date);
    
    set(con, stmt, stmt2, stmt3, rs, rs2, rs3, out, req, mfr, date, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 13066, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), mfr);
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, ResultSet rs3, PrintWriter out, HttpServletRequest req, String mfr, String date, String unm, String sid, String uty, String men,
                   String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Stock Check Record Check</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3001, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewItem(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/ProductStockRecord?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3018, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function view(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/StockCheckForItem?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p1=\"+p1;}");
    }
    
    scoutln(out, bytesOut, "function sanitise(code){");
    scoutln(out, bytesOut, "var code2='';var x;var len=code.length;");
    scoutln(out, bytesOut, "for(x=0;x<len;++x)");
    scoutln(out, bytesOut, "if(code.charAt(x)=='#')code2+='%23';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='\"')code2+='%22';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='&')code2+='%26';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='%')code2+='%25';");
    scoutln(out, bytesOut, "else if(code.charAt(x)==' ')code2+='%20';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='?')code+='%3F';");
    scoutln(out, bytesOut, "else code2+=code.charAt(x);");
    scoutln(out, bytesOut, "return code2};");
  
    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    int[] hmenuCount = new int[1];
 
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "13066", "", "StockCheckCheck", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Stock Check Record Check", "13066", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);
    
    scoutln(out, bytesOut, "<form><table id=\"page\" border=0 cellspacing=2 cellpadding=3>");

    scoutln(out, bytesOut, "<tr id=\"pageColumn\">");
    scoutln(out, bytesOut, "<td><p>Item Code</td>");
    scoutln(out, bytesOut, "<td><p>Manufacturer</td>");
    scoutln(out, bytesOut, "<td><p>Manufacturer Code</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Description</td></tr>");
            
    int[] numRecsOutput = new int[1];  numRecsOutput[0] = 0;
    
    int numRecs = checkRecs(con, stmt, stmt2, stmt3, rs, rs2, rs3, out, mfr, date, bytesOut, numRecsOutput);

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td colspan=6><p>There are " + generalUtils.formatNumeric(numRecsOutput[0], '0') + " items that have no stock check record on, or after, " + date + "</td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=6><p>There are " + generalUtils.formatNumeric(numRecs, '0') + " stock items</td></tr>");

    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private int checkRecs(Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, ResultSet rs3, PrintWriter out, String mfrReqd, String dateFrom, int[] bytesOut, int[] numRecsOutput) throws Exception
  {
    int count = 0;

    String[] cssFormat = new String[1];  cssFormat[0] = "";
        
    try
    {
      stmt = con.createStatement();

      if(mfrReqd.equals("___ALL___"))
        rs = stmt.executeQuery("SELECT ItemCode FROM stock ORDER BY Manufacturer");
      else rs = stmt.executeQuery("SELECT ItemCode FROM stock WHERE Manufacturer = '" + generalUtils.sanitiseForSQL(mfrReqd) + "' ORDER BY ManufacturerCode");
      
      while(rs.next())
      {
        processItemCode(con, stmt2, stmt3, rs2, rs3, out, rs.getString(1), dateFrom, cssFormat, bytesOut, numRecsOutput);
        ++count;
      }  
      
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();

    }
    catch(Exception e)
    {
    System.out.println(e);
      if(rs   != null) rs.close();     
      if(stmt != null) stmt.close();
    }
    
    return count;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void processItemCode(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, String itemCode, String date, String[] cssFormat, int[] bytesOut, int[] numRecsOutput) throws Exception
  {
    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT COUNT(0) FROM stockc WHERE Status != 'C' AND Date >= {d '" + date + "'} AND ItemCode = '" + generalUtils.sanitiseForSQL(itemCode) + "' AND Type = 'S' AND Level != '999999'");
      
      int numStockCheckRecs;
      String latestDate;
      
      if(rs.next())
      {
         if(rs.getInt(1) == 0)
        {           
          numStockCheckRecs = countStockCheckRecs(con, stmt, rs, itemCode);
     
          latestDate = latestStockCheckRec(con, stmt, rs, itemCode);
     
          output(out, con, stmt2, rs2, itemCode, numStockCheckRecs, latestDate, cssFormat, bytesOut);
          ++numRecsOutput[0];
        }
      }
      
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();

    }
    catch(Exception e)
    {
    System.out.println(e);
      if(rs   != null) rs.close();     
      if(stmt != null) stmt.close();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private int countStockCheckRecs(Connection con, Statement stmt, ResultSet rs, String itemCode) throws Exception
  {
    int count = 0;
    
    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT COUNT(0) FROM stockc WHERE Status != 'C' AND ItemCode = '" + generalUtils.sanitiseForSQL(itemCode) + "' AND Type = 'S' AND Level != '999999'");
      
      if(rs.next())
        count = rs.getInt(1);
      
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();

    }
    catch(Exception e)
    {
    System.out.println(e);
      if(rs   != null) rs.close();     
      if(stmt != null) stmt.close();
    }
    
    return count;
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private String latestStockCheckRec(Connection con, Statement stmt, ResultSet rs, String itemCode) throws Exception
  {
    String date = "";
    
    try
    {
      stmt = con.createStatement();

      stmt.setMaxRows(1);
      
      rs = stmt.executeQuery("SELECT Date FROM stockc WHERE Status != 'C' AND ItemCode = '" + generalUtils.sanitiseForSQL(itemCode) + "' AND Type = 'S' AND Level != '999999' ORDER BY Date DESC");
      
      if(rs.next())
        date = rs.getString(1);
      
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();

    }
    catch(Exception e)
    {
    System.out.println(e);
      if(rs   != null) rs.close();     
      if(stmt != null) stmt.close();
    }
    
    return date;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void output(PrintWriter out, Connection con, Statement stmt, ResultSet rs, String itemCode, int numStockCheckRecs, String latestDate, String[] cssFormat, int[] bytesOut)
  {
    try
    {
      String[] mfr     = new String[1];
      String[] mfrCode = new String[1];
      String[] desc    = new String[1];
      getItemDetailsGivenCode(con, stmt, rs, itemCode, mfr, mfrCode, desc);

      if(cssFormat[0].equals("line1")) cssFormat[0] = "line2"; else cssFormat[0] = "line1";
     
      scoutln(out, bytesOut, "<tr id='" + cssFormat[0] + "'>");    
      scoutln(out, bytesOut, "<td><p><a href=\"javascript:viewItem('" + itemCode + "')\">" + itemCode + "</a></td>");

      if(numStockCheckRecs == 0)
        scoutln(out, bytesOut, "<td></td>");
      else
      if(numStockCheckRecs == 1)
        scoutln(out, bytesOut, "<td><p><a href=\"javascript:view('" + itemCode + "')\">" + numStockCheckRecs + " Record up to " + latestDate + "</a></td>");
      else scoutln(out, bytesOut, "<td><p><a href=\"javascript:view('" + itemCode + "')\">" + numStockCheckRecs + " Records up to " + latestDate + "</a></td>");
      
      scoutln(out, bytesOut, "<td><p>" + mfr[0] + "</td>");
      scoutln(out, bytesOut, "<td><p>" + mfrCode[0] + "</td>");
      scoutln(out, bytesOut, "<td nowrap><p>" + desc[0] + "</td></tr>");
    }
    catch(Exception e)
    {
      System.out.println(e);
    }
  }
  
  //------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void getItemDetailsGivenCode(Connection con, Statement stmt, ResultSet rs, String itemCode, String[] mfr, String[] mfrCode, String[] desc)
                                       throws Exception
  {
    byte[] data = new byte[5000];
    
    if(inventory.getStockRecGivenCode(con, stmt, rs, itemCode, '\000', data) < 0) // just-in-case
    {
      mfr[0] = mfrCode[0] = desc[0] = "";
      return;
    }
 
    desc[0]    = generalUtils.dfsAsStr(data, (short)1);    
    mfr[0]     = generalUtils.dfsAsStr(data, (short)3);
    mfrCode[0] = generalUtils.dfsAsStr(data, (short)4);
    
    if(desc[0]    == null) desc[0] = "";
    if(mfr[0]     == null) mfr[0] = "";
    if(mfrCode[0] == null) mfrCode[0] = "";
    
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int[] bytesOut, String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);
  }

}
