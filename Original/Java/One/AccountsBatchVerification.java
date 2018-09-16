// =======================================================================================================================================================================================================
// System: ZaraStar Accounts: Batch Verification
// Module: AccountsBatchVerification.java
// Author: C.K.Harvey
// Copyright (c) 2003-09 Christopher Harvey. All Rights Reserved.
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
 
public class AccountsBatchVerification extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  AccountsUtils accountsUtils = new AccountsUtils();
  
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="";

    try
    {
      directoryUtils.setContentHeaders(res);
      out = res.getWriter();

      unm = req.getParameter("unm");
      uty = req.getParameter("uty");
      sid = req.getParameter("sid");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");
      p1  = req.getParameter("p1");
      
      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, bytesOut);
    }
    catch(Exception e)
    {
      StringBuffer url = req.getRequestURL();
      String urlBit="";
      int x=0;
      if(url.charAt(x) == 'h' || url.charAt(x) == 'H')
        x += 7;
      while(url.charAt(x) != '\000' && url.charAt(x) != ',' && url.charAt(x) != '/')
        urlBit += url.charAt(x++);

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "AccountsBatchVerification", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6045, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String year, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 6045, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "AccountsBatchVerification", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6045, bytesOut[0], 0, "ACC:" + year);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "AccountsBatchVerification", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6045, bytesOut[0], 0, "SID:" + year);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }
  
    create(con, stmt, rs, out, req, year, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 6045, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), year);
    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void create(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String year, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir,
                      int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Verification - Batches</title>");
    
    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">"); 

    scoutln(out, bytesOut, "function entries(code){");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/AccountsBatchEntries?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=\"+escape(code)+\"&bnm=" + bnm + "\";}");
    
    scoutln(out, bytesOut, "</script>");

    int[] hmenuCount = new int[1];
 
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "6045", "", "AccountsBatchVerification", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount,
                          bytesOut);

    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Verification - Batches: " + year, "6045", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);
  
    scoutln(out, bytesOut, "<form><table id=\"page\" border=0 width=100%>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td colspan=8><span id=\"msg\"></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr id=\"pageColumn\">");
    scoutln(out, bytesOut, "<td><p>Batch Code &nbsp;</td>");
    scoutln(out, bytesOut, "<td><p>Date &nbsp;</td>");
    scoutln(out, bytesOut, "<td><p>Description &nbsp;</td>");
    scoutln(out, bytesOut, "<td><p>Debits &nbsp;</td>");
    scoutln(out, bytesOut, "<td><p>Credits &nbsp;</td>");
    scoutln(out, bytesOut, "<td><p>Type &nbsp;</td>");
    scoutln(out, bytesOut, "<td><p>Opening Balances &nbsp;</td>");
    scoutln(out, bytesOut, "<td><p>Last Modified &nbsp;</td><td></td></tr>");
        
    double[] totalDebits  = new double[1];
    double[] totalCredits = new double[1];

    list(out, year, dnm, localDefnsDir, defnsDir, totalDebits, totalCredits, bytesOut);
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td><p>Total Debits</td><td align=right><p>"  + generalUtils.formatNumeric(totalDebits[0],  '2') + "</td></tr>");
    scoutln(out, bytesOut, "<tr><td><p>Total Credits</td><td align=right><p>" + generalUtils.formatNumeric(totalCredits[0], '2') + "</td></tr>");
    scoutln(out, bytesOut, "<tr><td><p>Difference: </td><td align=right><p>" + generalUtils.formatNumeric((totalDebits[0] - totalCredits[0]), '2') + "</td></tr>");
        
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void list(PrintWriter out, String year, String dnm, String localDefnsDir, String defnsDir, double[] totalDebits, double[] totalCredits, int[] bytesOut) throws Exception
  {
    Connection con = null;
    Statement stmt = null, stmt2 = null;
    ResultSet rs   = null, rs2   = null;
            
    try
    {
      String uName = directoryUtils.getMySQLUserName();
      String pWord = directoryUtils.getMySQLPassWord();
      Class.forName("com.mysql.jdbc.Driver").newInstance();
      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_accounts_" + year + "?user=" + uName + "&password=" + pWord);
      
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT * FROM joubatch ORDER BY Code");

      String code, date, desc, signOn, dlm, openingBalances, type, cssFormat;
      double[] debits  = new double[1];
      double[] credits = new double[1];
      boolean line1 = true;

      while(rs.next())
      {
        code            = rs.getString(1);
        date            = rs.getString(2);
        desc            = rs.getString(3);
        signOn          = rs.getString(4);
        dlm             = rs.getString(5);
        openingBalances = rs.getString(6);
        type            = rs.getString(7);
             
        if(line1) { cssFormat = "line1"; line1 = false; } else { cssFormat = "line2"; line1 = true; }
                    
        scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\">");
    
        scoutln(out, bytesOut, "<td>" + code + "</td>");
        scoutln(out, bytesOut, "<td>" + generalUtils.convertFromYYYYMMDD(date) + "</td>");
        scoutln(out, bytesOut, "<td>" + desc + "</td>");

        calcTotalsForABatch(con, stmt2, rs2, code, debits, credits);
        scoutln(out, bytesOut, "<td align=right>" + generalUtils.formatNumeric(debits[0], '2') + "</td>");
        scoutln(out, bytesOut, "<td align=right>" + generalUtils.formatNumeric(credits[0], '2') + "</td>");
        
        totalDebits[0]  += debits[0];
        totalCredits[0] += credits[0];

        if(type.equals("D"))
          type = "Debtors";
        else
        if(type.equals("C"))
          type = "Creditors";
        else type = "General";
        scoutln(out, bytesOut, "<td>" + type + "</td>");

        if(openingBalances.equals("Y"))
          openingBalances = "Yes";
        else openingBalances = "No";
        scoutln(out, bytesOut, "<td>" + openingBalances + "</td>");

        int x=0;
        while(x < dlm.length() && dlm.charAt(x) != ' ') // just-in-case
          ++x;
        scoutln(out, bytesOut, "<td>" + signOn + " on " + dlm.substring(0, x) +  "</td>");

        scoutln(out, bytesOut, "<td nowrap><a href=\"javascript:entries('" + code + "')\">Entries</a></td></tr>");
     }
                 
      if(rs   != null) rs.close();                                 
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(rs   != null) rs.close();                                 
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }    
  }  

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void calcTotalsForABatch(Connection con, Statement stmt, ResultSet rs, String batchCode, double[] debits, double[] credits) throws Exception
  {
    debits[0] = credits[0] = 0.0;
    
    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT DrCr, BaseAmount FROM joubatchl WHERE Code = '" + batchCode + "'"); 

      String drCr, baseAmount;

      while(rs.next())
      {
        drCr       = rs.getString(1);
        baseAmount = rs.getString(2);
             
        if(drCr.equals("D"))
          debits[0] += generalUtils.doubleDPs(generalUtils.doubleFromStr(baseAmount), '2');
        else
          credits[0] += generalUtils.doubleDPs(generalUtils.doubleFromStr(baseAmount), '2');
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
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
