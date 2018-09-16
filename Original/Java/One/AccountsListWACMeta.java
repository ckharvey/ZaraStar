// =======================================================================================================================================================================================================
// System: ZaraStar Accounts: List WAC meta
// Module: AccountsListWACMeta.java
// Author: C.K.Harvey
// Copyright (c) 1998-2009 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
//
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

public class AccountsListWACMeta extends HttpServlet
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
      directoryUtils.setContentHeaders2(res);
      out = res.getWriter();

      unm = req.getParameter("unm");
      sid = req.getParameter("sid");
      uty = req.getParameter("uty");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");
      p1  = req.getParameter("p1");

      doIt(out, req, p1, unm, sid, uty, men, den, dnm, bnm, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "AccountsListWACMeta", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7002, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String year, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');

    String uName = directoryUtils.getMySQLUserName();
    String pWord = directoryUtils.getMySQLPassWord();
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);

    Statement stmt = null;
    ResultSet rs   = null;

    Connection conAcc = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_accounts_" + year + "?user=" + uName + "&password=" + pWord);
    Statement stmtAcc = null;
    ResultSet rsAcc   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 7002, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "AccountsListWACMeta", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7002, bytesOut[0], 0, "ACC:" + year);

      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "AccountsListWACMeta", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7002, bytesOut[0], 0, "SID:" + year);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    display(con, conAcc, stmt, stmtAcc, rs, rsAcc, out, req, year, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 7002, bytesOut[0], 0, (new java.util.Date().getTime() - startTime),  year);

    if(conAcc != null) conAcc.close();
    if(con    != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void display(Connection con, Connection conAcc, Statement stmt, Statement stmtAcc, ResultSet rs, ResultSet rsAcc, PrintWriter out, HttpServletRequest req, String year, String unm, String sid, String uty, String men, String den,
                       String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>List WACs</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    scoutln(out, bytesOut, "function det(code){var p1=sanitise(code);");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/AccountsStockLedgerDisplay?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + year + "&p2=\"+p1;}");

    scoutln(out, bytesOut, "function csv(){");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/AccountsListWACMetaExecute?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + year + "\";}");

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

    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "7002", "", "AccountsListWACMeta", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Stock Weighted Average Costs: " + year, "7002",  unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    String[] yearStartDate = new String[1];
    String[] yearEndDate   = new String[1];

    accountsUtils.getAccountingYearStartAndEndDatesForAYear(con, stmt, rs, year, dnm, localDefnsDir, defnsDir, yearStartDate, yearEndDate);

    scoutln(out, bytesOut, "<p><a href=\"javascript:csv()\">Download As CSV</a></p><br>");

    scoutln(out, bytesOut, "<table border=1 id=\"page\">");

    scoutln(out, bytesOut, "<tr id=\"pageColumn\"><td><p>ItemCode</td><td><p>Opening</td>");

    int x, yearStartDateEncoded = generalUtils.encodeFromYYYYMMDD(yearStartDate[0]);

    for(x=0;x<366;++x)
      scout(out, bytesOut, "<td><p>" + generalUtils.decodeToYYYYMMDD(yearStartDateEncoded + x) + "</td>");

    scoutln(out, bytesOut, "</tr>");

    String cssFormat = "";

    try
    {
      stmtAcc = conAcc.createStatement();

      rsAcc = stmtAcc.executeQuery("SELECT * FROM wacmeta ORDER BY ItemCode");

      String itemCode, openingWAC;
      double low, high, openingWACD, lastWAC, wac;
      double[] wacs = new double[368];
      boolean atLeastOneValue;

      while(rsAcc.next())
      {
        itemCode    = rsAcc.getString(1);
        openingWAC  = generalUtils.deNull(rsAcc.getString(2));
        openingWACD = generalUtils.doubleFromStr(openingWAC);

        for(x=2;x<368;++x)
          wacs[x] = generalUtils.doubleFromStr(generalUtils.deNull(rsAcc.getString(x)));

        atLeastOneValue = false;
        for(x=2;x<368;++x)
          if(wacs[x] != 0.0)
            atLeastOneValue = true;

        if(atLeastOneValue)
        {
          if(cssFormat.equals("line1")) cssFormat = "line2"; else cssFormat = "line1";

          scout(out, bytesOut, "<tr id=\"" + cssFormat + "\"><td><p><a href=\"javascript:det('" + itemCode + "')\">" + itemCode + "</a></td>");
          scout(out, bytesOut, "<td><p>" + generalUtils.doubleDPs(openingWAC, '4') + "</td>");

          low  = openingWACD - (openingWACD * 0.2);
          high = openingWACD + (openingWACD * 0.2);

          lastWAC = openingWACD;

          for(x=2;x<368;++x)
          {
            wac = wacs[x];

            if(x == 367 && wac == 0.0)
              wac = lastWAC;
            else wac = wacs[x];

            if(wac == 0.0)
              scout(out, bytesOut, "<td>&nbsp;</td>");
            else
            {
              if((openingWACD != 0.0 && (wac < low || wac > high)) || wac < 0.0)
                scout(out, bytesOut, "<td><p><font color=red>" + generalUtils.doubleDPs(wac, '4') + "</td>");
              else scout(out, bytesOut, "<td><p>" + generalUtils.doubleDPs(wac, '4') + "</td>");

              lastWAC = wac;
            }
          }

          scoutln(out, bytesOut, "</tr>");
        }
      }

      if(rsAcc   != null) rsAcc.close();
      if(stmtAcc != null) stmtAcc.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(rsAcc   != null) rsAcc.close();
      if(stmtAcc != null) stmtAcc.close();
    }

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "</table>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir));
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scout(PrintWriter out, int bytesOut[], String str) throws Exception
  {
    out.print(str);
    bytesOut[0] += str.length();
  }
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {
    out.println(str);
    bytesOut[0] += (str.length() + 2);
  }

}

