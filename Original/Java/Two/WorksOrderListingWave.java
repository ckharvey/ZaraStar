// =======================================================================================================================================================================================================
// System: ZaraStar: Document: WO create listing page
// Module: WorksOrderListing.java
// Author: C.K.Harvey
// Copyright (c) 1998-2009 Christopher Harvey. All Rights Reserved.
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

public class WorksOrderListingWave extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  DrawingUtils drawingUtils = new DrawingUtils();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", inx="", codeArg="", codeArg2="", srchStr="", operation="",  firstRecNum="", lastRecNum="", callType="", maxRows="", numRecs="", companyCode="";
    try
    {
      req.setCharacterEncoding("UTF-8");
      directoryUtils.setContentHeaders(res);
      out = res.getWriter();

      unm         = req.getParameter("unm");
      sid         = req.getParameter("sid");
      uty         = req.getParameter("uty");
      men         = req.getParameter("men");
      den         = req.getParameter("den");
      dnm         = req.getParameter("dnm");
      bnm         = req.getParameter("bnm");
      inx         = req.getParameter("p1");
      operation   = req.getParameter("p2");

      codeArg     = req.getParameter("p3");
      if(codeArg  == null) codeArg = "";
      codeArg     = new String(codeArg.getBytes("ISO-8859-1"), "UTF-8");

      codeArg2    = req.getParameter("p4");
      if(codeArg2 == null) codeArg2 = "";
      codeArg2    = new String(codeArg2.getBytes("ISO-8859-1"), "UTF-8");

      srchStr     = req.getParameter("p5");
      if(srchStr  == null) srchStr = "";
      srchStr     = new String(srchStr.getBytes("ISO-8859-1"), "UTF-8");

      firstRecNum = req.getParameter("p6");
      lastRecNum  = req.getParameter("p7");

      callType    = req.getParameter("p8"); // 'G'roup (e.g., list of salespeople), or 'D'etail
      if(callType == null || callType.length() == 0) callType = "G";

      maxRows = req.getParameter("p9");
      if(maxRows == null || maxRows.length() == 0) maxRows = "50";

      numRecs    = req.getParameter("p10");
      if(numRecs == null || numRecs.length() == 0) numRecs = "-1";

      companyCode = req.getParameter("p11");
      if(companyCode == null || companyCode.length() == 0 || companyCode.equals("All")) companyCode = "";
      else companyCode = companyCode.toUpperCase();

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, inx, operation, callType, codeArg, codeArg2, srchStr, firstRecNum, lastRecNum, generalUtils.strToInt(maxRows), generalUtils.strToInt(numRecs), companyCode, bytesOut);
    }
    catch(Exception e)
    {
      try
      {
        scoutln(out, bytesOut, "4442\001WOs\001WO Listing\001javascript:getHTML('WorksOrderListingWave','')\001\001\001" + e + "\001\003");
      } catch(Exception e2){}
      serverUtils.etotalBytes(req, unm, dnm, 4442, bytesOut[0], 0, "ERR:" + codeArg);
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String inx,  String operation, String callType, String codeArg, String codeArg2, String srchStr,
                    String firstRecNum, String lastRecNum, int maxRows, int numRecs, String companyCode, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);

    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 4442, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreenW(out, req, 13, unm, sid, uty, men, den, dnm, bnm, "WorksOrderListing", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 4442, bytesOut[0], 0, "ACC:" + codeArg);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreenW(out, req, 12, unm, sid, uty, men, den, dnm, bnm, "WorksOrderListing", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 4442, bytesOut[0], 0, "SID:" + codeArg);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(srchStr == null || srchStr.equalsIgnoreCase("null"))
      srchStr = "";

    scoutln(out, bytesOut, "4442\001WOs\001WO Listing\001javascript:getHTML('WorksOrderListingWave','')\001\001\001\001\003");

    generate(con, stmt, rs, out, unm, dnm, inx.charAt(0), operation.charAt(0), callType.charAt(0), codeArg, codeArg2, srchStr, firstRecNum, lastRecNum, maxRows, numRecs, companyCode, imagesDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 4442, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), codeArg);

    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void generate(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String unm, String dnm, char inx, char operation, char callType, String codeArg, String codeArg2, String srchStr, String firstRecNum, String lastRecNum,
                        int maxRows, int numRecsIn, String companyCode, String imagesDir, String defnsDir, int[] bytesOut) throws Exception
  {
    int[] numRecs = new int[1];  numRecs[0] = numRecsIn;

    String[] firstCodeOnPage  = new String[1];
    String[] lastCodeOnPage   = new String[1];
    String[] firstEntryOnPage = new String[1];
    String[] lastEntryOnPage  = new String[1];
    String[] newFirstRecNum   = new String[1];
    String[] newLastRecNum    = new String[1];

    char operation2;
    if(operation == 'X') // first time in
      operation2 = 'F';
    else operation2 = operation;

    String[] html = new String[1]; html[0] = "";
    createPage(html, operation2, callType, codeArg, codeArg2, inx, srchStr, numRecs, firstCodeOnPage, lastCodeOnPage, firstEntryOnPage, lastEntryOnPage, firstRecNum, lastRecNum, newFirstRecNum, newLastRecNum, maxRows, companyCode, imagesDir, dnm);

    if(generalUtils.strToInt(newFirstRecNum[0]) <= 0) // prev call has gone back before rec 1
    {
      newFirstRecNum[0] = "1";
      newLastRecNum[0] = "50";
    }

    setHead(con, stmt, rs, out, inx, unm, dnm, newFirstRecNum[0], newLastRecNum[0], numRecs[0], companyCode, defnsDir, bytesOut);

    setNav(out, true, inx, operation, callType, numRecs[0], firstCodeOnPage[0], lastCodeOnPage[0], firstEntryOnPage[0],  lastEntryOnPage[0], srchStr, imagesDir, newFirstRecNum[0], newLastRecNum[0], maxRows, bytesOut);

    if(numRecs[0] > 0)
    {
      if(callType != 'G' || inx == '1')
        startBody(true, out, inx, imagesDir, bytesOut);
      else startBody2(true, out, inx, imagesDir, bytesOut);
    }

    scoutln(out, bytesOut, html[0]);

    if(numRecs[0] > 0)
    {
      if(callType != 'G' || inx == '1')
        startBody(false, out, inx, imagesDir, bytesOut);
      else startBody2(false, out, inx, imagesDir, bytesOut);
    }

    setNav(out, false, inx, operation, callType, numRecs[0], firstCodeOnPage[0], lastCodeOnPage[0], firstEntryOnPage[0], lastEntryOnPage[0], srchStr, imagesDir, newFirstRecNum[0], newLastRecNum[0], maxRows, bytesOut);

    scoutln(out, bytesOut, "</table></form>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void createPage(String[] html, char operation, char callType, String arg, String arg2, char inx, String srchStr, int[] numRecs, String[] firstCodeOnPage, String[] lastCodeOnPage, String[] firstEntryOnPage,  String[] lastEntryOnPage,
                          String firstRecNum, String lastRecNum, String[] newFirstRecNum, String[] newLastRecNum, int maxRows, String companyCode, String imagesDir, String dnm) throws Exception
  {
    String userName = directoryUtils.getMySQLUserName();
    String passWord = directoryUtils.getMySQLPassWord();

    Class.forName("com.mysql.jdbc.Driver").newInstance();

    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + userName + "&password=" + passWord);
    Statement stmt = con.createStatement();
    String q;
    int count=1;

    String codeArg = "";
    int y, len = arg.length();
    for(y=0;y<len;++y)
    {
      if(arg.charAt(y) == '\'')
        codeArg += "''";
      else codeArg += arg.charAt(y);
    }

    String codeArg2 = "";
    len = arg2.length();
    for(y=0;y<len;++y)
    {
      if(arg2.charAt(y) == '\'')
        codeArg2 += "''";
      else codeArg2 += arg2.charAt(y);
    }

    String companyCodeWHERE="", companyCodeANDWHERE="";
    if(companyCode.length() > 0)
    {
      companyCodeWHERE    = " WHERE CompanyCode = '" + companyCode + "'";
      companyCodeANDWHERE = " AND CompanyCode = '" + companyCode + "'";
    }

    // determine num of recs if not already known, or if doing a search and hence must re-determine
    if(numRecs[0] == -1 || operation == 'F')
    {
      ResultSet r;
      switch(inx)
      {
        case '2' : if(callType == 'S')
                     r = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM wo WHERE Date = {d '" + generalUtils.convertDateToSQLFormat(srchStr) + "'}" + companyCodeANDWHERE);
                   else
                   if(callType == 'G')
                     r = stmt.executeQuery("SELECT COUNT(DISTINCT Date) AS rowcount FROM wo" + companyCodeWHERE);
                   else r = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM wo WHERE Date = {d '" + generalUtils.convertDateToSQLFormat(codeArg) + "'}" + companyCodeANDWHERE);
                   break;
        case '3' : if(callType == 'S')
                     r = stmt.executeQuery("SELECT COUNT(CompanyCode) AS rowcount FROM wo WHERE CompanyCode LIKE '" + srchStr + "%'");
                   else
                   if(callType == 'G')
                      r = stmt.executeQuery("SELECT COUNT(DISTINCT CompanyCode) AS rowcount FROM wo" + companyCodeWHERE);
                   else r = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM wo WHERE CompanyCode = '" + codeArg + "'");
                   break;
        case '4' : if(callType == 'S')
                     r = stmt.executeQuery("SELECT COUNT(SalesPerson) AS rowcount FROM wo WHERE SalesPerson LIKE '" + srchStr + "%'" + companyCodeANDWHERE);
                   else
                   if(callType == 'G')
                     r = stmt.executeQuery("SELECT COUNT(DISTINCT SalesPerson) AS rowcount FROM wo");
                   else r = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM wo WHERE SalesPerson = '" + codeArg + "'" + companyCodeANDWHERE);
                   break;
        case '5' : if(callType == 'S')
                     r = stmt.executeQuery("SELECT COUNT(CustomerPOCode) AS rowcount FROM wo WHERE CustomerPOCode LIKE '" + srchStr + "%'"
                       + companyCodeANDWHERE);
                   else
                   if(callType == 'G')
                     r = stmt.executeQuery("SELECT COUNT(DISTINCT CustomerPOCode) AS rowcount FROM wo" + companyCodeWHERE);
                   else r = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM wo WHERE CustomerPOCode = '" + codeArg + "'" + companyCodeANDWHERE);
                   break;
        case '6' : if(callType == 'S')
                     r = stmt.executeQuery("SELECT COUNT(SOCode) AS rowcount FROM wo WHERE SOCode LIKE '" + srchStr + "%'"
                       + companyCodeANDWHERE);
                   else
                   if(callType == 'G')
                     r = stmt.executeQuery("SELECT COUNT(DISTINCT SOCode) AS rowcount FROM wo" + companyCodeWHERE);
                   else r = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM wo WHERE SOCode = '" + codeArg + "'" + companyCodeANDWHERE);
                   break;
        default  : // case '1'
                   if(callType == 'S')
                     r = stmt.executeQuery("SELECT COUNT(DISTINCT WOCode) AS rowcount FROM wo WHERE WOCode LIKE '" + srchStr + "%'" + companyCodeANDWHERE);
                   else
                   if(operation == 'N' && callType=='2')
                     r = stmt.executeQuery("SELECT COUNT(DISTINCT WOCode) AS rowcount FROM wo WHERE WOCode LIKE '" + srchStr + "%'" + companyCodeANDWHERE); // always calc the total number even though we are on subsequent page
                   else r = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM wo" + companyCodeWHERE);
                   break;
      }
      r.next();
      numRecs[0] = r.getInt("rowcount") ;
      if(r != null) r.close();
    }

    ResultSet rs = null;

    String woCode="", name="", customerCode="", date="", customerPOCode="", soCode="", status, salesPerson="";
    int woCodePosn, datePosn, companyCodePosn, companyNamePosn, statusPosn, soCodePosn, customerPOCodePosn, salesPersonPosn;

    switch(operation)
    {
      case 'F' : // first page
                 if(inx == '1')
                 {
                   if(callType == 'S')
                     q = "SELECT WOCode, Date, CompanyCode, CustomerPOCode, CompanyName, Status FROM wo WHERE WOCode LIKE '" + srchStr + "%'" + companyCodeANDWHERE + " ORDER BY WOCode";
                   else q = "SELECT WOCode, Date, CompanyCode, CustomerPOCode, CompanyName, Status FROM wo" + companyCodeWHERE + " ORDER BY WOCode";

                   woCodePosn = 1; datePosn = 2; companyCodePosn = 3; companyNamePosn = 5; customerPOCodePosn = 4; statusPosn = 6; salesPersonPosn = -1; soCodePosn = -1;
                 }
                 else
                 if(inx == '2')
                 {
                   if(callType == 'S')
                   {
                     q = "SELECT Date, WOCode, CompanyCode, CustomerPOCode, CompanyName, Status FROM wo WHERE Date = {d '" + generalUtils.convertDateToSQLFormat(srchStr) + "'}" + companyCodeANDWHERE + " ORDER BY Date, WOCode";

                     woCodePosn = 2; datePosn = 1; companyCodePosn = 3; companyNamePosn = 5; customerPOCodePosn = 4; statusPosn = 6; salesPersonPosn = -1; soCodePosn = -1;
                   }
                   else
                   if(callType == 'G')
                   {
                     q = "SELECT DISTINCT Date FROM wo" + companyCodeWHERE + " ORDER BY Date";

                     woCodePosn = -1; datePosn = 1; companyCodePosn = -1; companyNamePosn = -1; customerPOCodePosn = -1; statusPosn = -1; salesPersonPosn = -1; soCodePosn = -1;
                   }
                   else
                   {
                     q = "SELECT Date, WOCode, CompanyCode, CustomerPOCode, CompanyName, Status FROM wo WHERE Date = {d '" + generalUtils.convertDateToSQLFormat(codeArg) + "'}" + companyCodeANDWHERE + " ORDER BY Date, WOCode";

                     woCodePosn = 2; datePosn = 1; companyCodePosn = 3; companyNamePosn = 5; customerPOCodePosn = 4; statusPosn = 6; salesPersonPosn = -1; soCodePosn = -1;
                   }
                 }
                 else
                 if(inx == '3')
                 {
                   if(callType == 'S')
                   {
                     q = "SELECT CompanyCode, WOCode, CompanyName, CustomerPOCode, Status FROM wo WHERE CompanyCode LIKE '" + srchStr + "%' ORDER BY CompanyCode, WOCode";

                     woCodePosn = 2; datePosn = -1; companyCodePosn = 1; companyNamePosn = 3; customerPOCodePosn = 4; statusPosn = 5; salesPersonPosn = -1; soCodePosn = -1;
                   }
                   else
                   if(callType == 'G')
                   {
                     q = "SELECT DISTINCT CompanyCode FROM wo" + companyCodeWHERE + " ORDER BY CompanyCode";

                     woCodePosn = -1; datePosn = -1; companyCodePosn = 1; companyNamePosn = -1; customerPOCodePosn = -1; statusPosn = -1; salesPersonPosn = -1; soCodePosn = -1;
                   }
                   else
                   {
                     q = "SELECT CompanyCode, WOCode, CompanyName, CustomerPOCode, Status FROM wo WHERE CompanyCode = '" + codeArg + "' ORDER BY CompanyCode";
                     woCodePosn = 2; datePosn = -1; companyCodePosn = 1; companyNamePosn = 3; customerPOCodePosn = 4; statusPosn = 5; salesPersonPosn = -1; soCodePosn = -1;
                   }
                 }
                 else
                 if(inx == '4')
                 {
                   if(callType == 'S')
                   {
                     q = "SELECT SalesPerson, WOCode, Date, CompanyCode, CustomerPOCode, CompanyName, Status FROM wo WHERE SalesPerson LIKE '" + srchStr + "%'" + companyCodeANDWHERE + " ORDER BY SalesPerson, Date";
                     woCodePosn = 2; datePosn = 3; companyCodePosn = 4; companyNamePosn = 6; customerPOCodePosn = 5; statusPosn = 7; salesPersonPosn = 1; soCodePosn = -1;
                   }
                   else
                   if(callType == 'G')
                   {
                     q = "SELECT DISTINCT SalesPerson FROM wo" + companyCodeWHERE + " ORDER BY SalesPerson";
                     woCodePosn = -1; datePosn = -1; companyCodePosn = -1; companyNamePosn = -1; customerPOCodePosn = -1; statusPosn = -1; salesPersonPosn = 1; soCodePosn = -1;
                   }
                   else
                   {
                     q = "SELECT SalesPerson, WOCode, Date, CompanyCode, CustomerPOCode, CompanyName, Status FROM wo WHERE SalesPerson = '" + codeArg + "'" + companyCodeANDWHERE + " ORDER BY SalesPerson";
                     woCodePosn = 2; datePosn = 3; companyCodePosn = 4; companyNamePosn = 6; customerPOCodePosn = 5; statusPosn = 7; salesPersonPosn = 1; soCodePosn = -1;
                   }
                 }
                 else
                 if(inx == '5')
                 {
                   if(callType == 'S')
                   {
                     q = "SELECT CustomerPOCode, WOCode, CompanyCode, CompanyName, Status FROM wo WHERE CustomerPOCode LIKE '" + srchStr + "%'" + companyCodeANDWHERE + " ORDER BY CustomerPOCode, WOCode";

                     woCodePosn = 2; datePosn = -1; companyCodePosn = 3; companyNamePosn = 4; customerPOCodePosn = 1; statusPosn = 5; salesPersonPosn = -1; soCodePosn = -1;
                   }
                   else
                   if(callType == 'G')
                   {
                     q = "SELECT DISTINCT CustomerPOCode FROM wo" + companyCodeWHERE + " ORDER BY CustomerPOCode";

                     woCodePosn = -1; datePosn = -1; companyCodePosn = -1; companyNamePosn = -1; customerPOCodePosn = 1; statusPosn = -1; salesPersonPosn = -1; soCodePosn = -1;
                   }
                   else
                   {
                     q = "SELECT CustomerPOCode, WOCode, CompanyCode, CompanyName, Status FROM wo WHERE CustomerPOCode = '" + codeArg + "'" + companyCodeANDWHERE + " ORDER BY CustomerPOCode";

                     woCodePosn = 2; datePosn = -1; companyCodePosn = 3; companyNamePosn = 4; customerPOCodePosn = 1; statusPosn = 5; salesPersonPosn = -1; soCodePosn = -1;
                   }
                 }
                 else // inx == '6'
                 {
                   if(callType == 'S')
                   {
                     q = "SELECT SOCode, WOCode, CompanyCode, CompanyName, Status FROM wo WHERE SOCode LIKE '" + srchStr + "%'" + companyCodeANDWHERE + " ORDER BY SOCode, WOCode";

                     woCodePosn = 2; datePosn = -1; companyCodePosn = 3; companyNamePosn = 4; soCodePosn = 1; statusPosn = 5; salesPersonPosn = -1; customerPOCodePosn = -1;
                   }
                   else
                   if(callType == 'G')
                   {
                     q = "SELECT DISTINCT SOCode FROM wo" + companyCodeWHERE + " ORDER BY SOCode";

                     woCodePosn = -1; datePosn = -1; companyCodePosn = -1; companyNamePosn = -1; soCodePosn = 1; statusPosn = -1; salesPersonPosn = -1; customerPOCodePosn = -1;
                   }
                   else
                   {
                     q = "SELECT SOCode, WOCode, CompanyCode, CompanyName, Status FROM wo WHERE SOCode = '" + codeArg + "'" + companyCodeANDWHERE + " ORDER BY SOCode";

                     woCodePosn = 2; datePosn = -1; companyCodePosn = 3; companyNamePosn = 4; soCodePosn = 1; statusPosn = 5; salesPersonPosn = -1; customerPOCodePosn = -1;
                   }
                 }

                 stmt.setMaxRows(maxRows);
                 rs = stmt.executeQuery(q);

                 while(rs.next())
                 {
                   woCode         = getString(rs, woCodePosn);
                   date           = getString(rs, datePosn);
                   customerCode   = getString(rs, companyCodePosn);
                   name           = getString(rs, companyNamePosn);
                   customerPOCode = getString(rs, customerPOCodePosn);
                   soCode         = getString(rs, soCodePosn);
                   salesPerson    = getString(rs, salesPersonPosn);
                   status         = getString(rs, statusPosn);

                   if(inx == '1')
                     appendBodyLine(html, inx, woCode, date, customerCode, customerPOCode, name, null, woCode, status, imagesDir);
                   else
                   if(inx == '2')
                   {
                     if(callType == 'G')
                       appendBodyLine2(html, callType, operation, imagesDir, date);
                     else appendBodyLine(html, inx, date, woCode, customerCode, customerPOCode, name, null, woCode, status, imagesDir);
                   }
                   else
                   if(inx == '3')
                   {
                     if(callType == 'G')
                       appendBodyLine2(html, callType, operation, imagesDir, customerCode);
                     else appendBodyLine(html, inx, customerCode, woCode, name, customerPOCode, null, null, woCode, status, imagesDir);
                   }
                   else
                   if(inx == '4')
                   {
                     if(callType == 'G')
                       appendBodyLine2(html, callType, operation, imagesDir, salesPerson);
                     else appendBodyLine(html, inx, salesPerson, woCode, date, customerCode, customerPOCode, name, woCode, status, imagesDir);
                   }
                   else
                   if(inx == '5')
                   {
                     if(callType == 'G')
                       appendBodyLine2(html, callType, operation, imagesDir, customerPOCode);
                     else appendBodyLine(html, inx, customerPOCode, woCode, customerCode, name, null, null, woCode, status, imagesDir);
                   }
                   else // inx == '6'
                   {
                     if(callType == 'G')
                       appendBodyLine2(html, callType, operation, imagesDir, soCode);
                     else appendBodyLine(html, inx, soCode, woCode, customerCode, name, null, null, woCode, status, imagesDir);
                   }

                   if(count++ == 1)
                   {
                     firstCodeOnPage[0] = woCode;

                     if(inx == '1')
                       firstEntryOnPage[0] = woCode;
                     else
                     if(inx == '2')
                       firstEntryOnPage[0] = date;
                     else
                     if(inx == '3')
                       firstEntryOnPage[0] = customerCode;
                     else
                     if(inx == '4')
                       firstEntryOnPage[0] = salesPerson;
                     else
                     if(inx == '5')
                       firstEntryOnPage[0] = customerPOCode;
                     else // inx == '6'
                       firstEntryOnPage[0] = soCode;
                   }
                 }

                 rs.close();

                 lastCodeOnPage[0] = woCode;

                 if(inx == '1')
                   lastEntryOnPage[0] = woCode;
                 else
                 if(inx == '2')
                   lastEntryOnPage[0] = date;
                 else
                 if(inx == '3')
                   lastEntryOnPage[0] = customerCode;
                 else
                 if(inx == '4')
                   lastEntryOnPage[0] = salesPerson;
                 else
                 if(inx == '5')
                   lastEntryOnPage[0] = customerPOCode;
                 else // inx == '6'
                   lastEntryOnPage[0] = soCode;

                 newFirstRecNum[0] = "1";
                 newLastRecNum[0]  = generalUtils.intToStr(count - 1);
                 break;
      case 'L' : // last page
                 if(inx == '1')
                 {
                   if(callType == 'S')
                     q = "SELECT WOCode, Date, CompanyCode, CustomerPOCode, CompanyName, Status FROM wo WHERE WOCode LIKE '" + srchStr + "%' AND WOCode > '" + codeArg + "'" + companyCodeANDWHERE + " ORDER BY WOCode DESC";
                   else q = "SELECT WOCode, Date, CompanyCode, CustomerPOCode, CompanyName, Status FROM wo WHERE WOCode <= 'ZZZZZZZZZZZZZZZZZZZZ'" + companyCodeANDWHERE + " ORDER BY WOCode DESC";

                   woCodePosn = 1; datePosn = 2; companyCodePosn = 3; companyNamePosn = 5; customerPOCodePosn = 4; statusPosn = 6; salesPersonPosn = -1; soCodePosn = -1;
                 }
                 else
                 if(inx == '2')
                 {
                   if(callType == 'G')
                   {
                     q = "SELECT DISTINCT Date FROM wo WHERE Date <= {d '9999-12-31'}" + companyCodeANDWHERE + " ORDER BY Date DESC";

                     woCodePosn = -1; datePosn = 1; companyCodePosn = -1; companyNamePosn = -1; customerPOCodePosn = -1; statusPosn = -1; salesPersonPosn = -1; soCodePosn = -1;
                   }
                   else
                   {
                     q = "SELECT Date, WOCode, CompanyCode, CustomerPOCode, CompanyName, Status FROM wo" + " WHERE Date = {d '" + generalUtils.convertDateToSQLFormat(codeArg) + "'}" + companyCodeANDWHERE + " ORDER BY Date, WOCode DESC";

                     woCodePosn = 2; datePosn = 1; companyCodePosn = 3; companyNamePosn = 5; customerPOCodePosn = 4; statusPosn = 6; salesPersonPosn = -1; soCodePosn = -1;
                   }
                 }
                 else
                 if(inx == '3')
                 {
                   if(callType == 'G')
                   {
                     q = "SELECT DISTINCT CompanyCode FROM wo WHERE CompanyCode <= 'ZZZZZZZZZZZZZZZZZZZ' ORDER BY CompanyCode DESC";

                     woCodePosn = -1; datePosn = -1; companyCodePosn = 1; companyNamePosn = -1; customerPOCodePosn = -1; statusPosn = -1; salesPersonPosn = -1; soCodePosn = -1;
                   }
                   else
                   {
                      q = "SELECT CompanyCode, WOCode, CompanyName, CustomerPOCode, Status FROM wo WHERE CompanyCode = '" + codeArg + "' ORDER BY CompanyCode, WOCode DESC";

                     woCodePosn = 2; datePosn = -1; companyCodePosn = 1; companyNamePosn = 3; customerPOCodePosn = 4; statusPosn = 5; salesPersonPosn = -1; soCodePosn = -1;
                   }
                 }
                 else
                 if(inx == '4')
                 {
                   if(callType == 'G')
                   {
                     q = "SELECT DISTINCT SalesPerson FROM wo WHERE SalesPerson <= 'zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz'" + companyCodeANDWHERE + " ORDER BY SalesPerson DESC";

                     woCodePosn = -1; datePosn = -1; companyCodePosn = -1; companyNamePosn = -1; customerPOCodePosn = -1; statusPosn = -1; salesPersonPosn = 1; soCodePosn = -1;
                   }
                   else
                   {
                     q = "SELECT SalesPerson, WOCode, Date, CompanyCode, CustomerPOCode, CompanyName, Status FROM wo WHERE SalesPerson = '" + codeArg + "'" + companyCodeANDWHERE + " ORDER BY SalesPerson, WOCode DESC";

                     woCodePosn = 2; datePosn = 3; companyCodePosn = 4; companyNamePosn = 6; customerPOCodePosn = 5; statusPosn = 7; salesPersonPosn = 1; soCodePosn = -1;
                   }
                 }
                 else
                 if(inx == '5')
                 {
                   if(callType == 'G')
                   {
                     q = "SELECT DISTINCT CustomerPOCode FROM wo WHERE CustomerPOCode <= 'zzzzzzzzzzzzzzzzzzzzzzzzzzzzzz'" + companyCodeANDWHERE + " ORDER BY CustomerPOCode DESC";

                     woCodePosn = -1; datePosn = -1; companyCodePosn = -1; companyNamePosn = -1; customerPOCodePosn = 1; statusPosn = -1; salesPersonPosn = -1; soCodePosn = -1;
                   }
                   else
                   {
                     q = "SELECT CustomerPOCode, WOCode, CompanyCode, CompanyName, Status FROM wo WHERE CustomerPOCode = '" + codeArg + companyCodeANDWHERE + "' ORDER BY CustomerPOCode, WOCode DESC";

                     woCodePosn = 2; datePosn = -1; companyCodePosn = 3; companyNamePosn = 4; customerPOCodePosn = 1; statusPosn = 5; salesPersonPosn = -1; soCodePosn = -1;
                   }
                 }
                 else // inx == '6'
                 {
                   if(callType == 'G')
                   {
                     q = "SELECT DISTINCT SOCode FROM wo WHERE SOCode <= 'zzzzzzzzzzzzzzzzzzzzzzzzzzzzzz'" + companyCodeANDWHERE + " ORDER BY SOCode DESC";

                     woCodePosn = -1; datePosn = -1; companyCodePosn = -1; companyNamePosn = -1; customerPOCodePosn = -1; statusPosn = -1; salesPersonPosn = -1; soCodePosn = 1;
                   }
                   else
                   {
                     q = "SELECT SOCode, WOCode, CompanyCode, CompanyName, Status FROM wo WHERE SOCode = '" + codeArg + companyCodeANDWHERE + "' ORDER BY CustomerPOCode, WOCode DESC";

                     woCodePosn = 2; datePosn = -1; companyCodePosn = 3; companyNamePosn = 4; customerPOCodePosn = -1; statusPosn = 5; salesPersonPosn = -1; soCodePosn = 1;
                   }
                 }

                 stmt.setMaxRows(maxRows);
                 rs = stmt.executeQuery(q);

                 while(rs.next())
                 {
                   woCode         = getString(rs, woCodePosn);
                   date           = getString(rs, datePosn);
                   customerCode   = getString(rs, companyCodePosn);
                   name           = getString(rs, companyNamePosn);
                   customerPOCode = getString(rs, customerPOCodePosn);
                   soCode         = getString(rs, soCodePosn);
                   salesPerson    = getString(rs, salesPersonPosn);
                   status         = getString(rs, statusPosn);

                   if(inx == '1')
                     prependBodyLine(html, inx, woCode, date, customerCode, customerPOCode, name, null, woCode, status, imagesDir);
                   else
                   if(inx == '2')
                   {
                     if(callType == 'G')
                       prependBodyLine2(html, callType, date);
                     else prependBodyLine(html, inx, date, woCode, customerCode, customerPOCode, name, null, woCode, status, imagesDir);
                   }
                   else
                   if(inx == '3')
                   {
                     if(callType == 'G')
                       prependBodyLine2(html, callType, customerCode);
                     else prependBodyLine(html, inx, customerCode, woCode, name, customerPOCode, null, null, woCode, status, imagesDir);
                   }
                   else
                   if(inx == '4')
                   {
                     if(callType == 'G')
                       prependBodyLine2(html, callType, salesPerson);
                     else prependBodyLine(html, inx, salesPerson, woCode, date, customerCode, customerPOCode, name, woCode, status, imagesDir);
                   }
                   else
                   if(inx == '5')
                   {
                     if(callType == 'G')
                       prependBodyLine2(html, callType, customerPOCode);
                     else prependBodyLine(html, inx, customerPOCode, woCode, customerCode, name, null, null, woCode, status, imagesDir);
                   }
                   else // inx == '6'
                   {
                     if(callType == 'G')
                       prependBodyLine2(html, callType, soCode);
                     else prependBodyLine(html, inx, soCode, woCode, customerCode, name, null, null, woCode, status, imagesDir);
                   }

                   if(count++ == 1)
                   {
                     lastCodeOnPage[0] = woCode;

                     if(inx == '1')
                       lastEntryOnPage[0] = woCode;
                     else
                     if(inx == '2')
                       lastEntryOnPage[0] = date;
                     else
                     if(inx == '3')
                       lastEntryOnPage[0] = customerCode;
                     else
                     if(inx == '4')
                       lastEntryOnPage[0] = salesPerson;
                     else
                     if(inx == '5')
                       lastEntryOnPage[0] = customerPOCode;
                     else // inx == '6'
                       lastEntryOnPage[0] = soCode;
                   }
                 }

                 rs.close();

                 firstCodeOnPage[0] = woCode;

                 if(inx == '1')
                   firstEntryOnPage[0] = woCode;
                 else
                 if(inx == '2')
                   firstEntryOnPage[0] = date;
                 else
                 if(inx == '3')
                   firstEntryOnPage[0] = customerCode;
                 else
                 if(inx == '4')
                   firstEntryOnPage[0] = salesPerson;
                 else
                 if(inx == '5')
                   firstEntryOnPage[0] = customerPOCode;
                 else // inx == '6'
                   firstEntryOnPage[0] = soCode;

                 newFirstRecNum[0] = generalUtils.intToStr(numRecs[0] - count + 2);
                 newLastRecNum[0]  = generalUtils.intToStr(numRecs[0]);
                 break;
      case 'N' : // next page
                 if(inx == '1')
                 {
                   if(callType == 'S')
                   {
                     q = "SELECT WOCode, Date, CompanyCode, CustomerPOCode, CompanyName, Status FROM wo WHERE WOCode LIKE '" + srchStr + "%' AND WOCode > '" + codeArg + "'" + companyCodeANDWHERE + " ORDER BY WOCode";

                     woCodePosn = 1; datePosn = 2; companyCodePosn = 3; companyNamePosn = 5; customerPOCodePosn = 4; statusPosn = 6; salesPersonPosn = -1; soCodePosn = -1;
                   }
                   else
                   {
                     q = "SELECT WOCode, Date, CompanyCode, CustomerPOCode, CompanyName, Status FROM wo WHERE WOCode > '" + codeArg + "'" + companyCodeANDWHERE + " ORDER BY WOCode";

                     woCodePosn = 1; datePosn = 2; companyCodePosn = 3; companyNamePosn = 5; customerPOCodePosn = 4; statusPosn = 6; salesPersonPosn = -1; soCodePosn = -1;
                   }
                 }
                 else
                 if(inx == '2')
                 {
                   if(callType == 'G')
                   {
                     q = "SELECT DISTINCT Date FROM wo WHERE Date > {d '" + generalUtils.convertDateToSQLFormat(codeArg) + "'}" + companyCodeANDWHERE + " ORDER BY Date";

                     woCodePosn = -1; datePosn = 1; companyCodePosn = -1; companyNamePosn = -1; customerPOCodePosn = -1; statusPosn = -1; salesPersonPosn = -1; soCodePosn = -1;
                   }
                   else
                   {
                     q = "SELECT Date, WOCode, CompanyCode, CustomerPOCode, CompanyName, Status FROM wo WHERE Date = {d '" + generalUtils.convertDateToSQLFormat(codeArg) + "'} AND WOCode > '" + codeArg2 + "'" + companyCodeANDWHERE
                       + " ORDER BY Date, WOCode";

                     woCodePosn = 2; datePosn = 1; companyCodePosn = 3; companyNamePosn = 5; customerPOCodePosn = 4; statusPosn = 6; salesPersonPosn = -1; soCodePosn = -1;
                   }
                 }
                 else
                 if(inx == '3')
                 {
                   if(callType == 'G')
                   {
                     q = "SELECT DISTINCT CompanyCode FROM wo WHERE CompanyCode >= '" + codeArg + "' ORDER BY CompanyCode";

                     woCodePosn = -1; datePosn = -1; companyCodePosn = 1; companyNamePosn = -1; customerPOCodePosn = -1; statusPosn = -1; salesPersonPosn = -1; soCodePosn = -1;
                   }
                   else
                   {
                     q = "SELECT CompanyCode, WOCode, CompanyName, CustomerPOCode, Status FROM wo WHERE CompanyCode = '" + codeArg + "' AND WOCode > '" + codeArg2 + "' ORDER BY CompanyCode, WOCode";

                     woCodePosn = 2; datePosn = -1; companyCodePosn = 1; companyNamePosn = 3; customerPOCodePosn = 4; statusPosn = 5; salesPersonPosn = -1; soCodePosn = -1;
                   }
                 }
                 else
                 if(inx == '4')
                 {
                   if(callType == 'G')
                   {
                     q = "SELECT DISTINCT SalesPerson FROM wo WHERE SalesPerson >= '" + codeArg + "'" + companyCodeANDWHERE + " ORDER BY SalesPerson";

                     woCodePosn = -1; datePosn = -1; companyCodePosn = -1; companyNamePosn = -1; customerPOCodePosn = -1; statusPosn = -1; salesPersonPosn = 1; soCodePosn = -1;
                   }
                   else
                   {
                     q = "SELECT SalesPerson, WOCode, Date, CompanyCode, CustomerPOCode, CompanyName, Status FROM wo WHERE SalesPerson = '" + codeArg + "' AND WOCode > '" + codeArg2 + "'" + companyCodeANDWHERE + " ORDER BY SalesPerson, WOCode";

                     woCodePosn = 2; datePosn = 3; companyCodePosn = 4; companyNamePosn = 6; customerPOCodePosn = 5; statusPosn = 7; salesPersonPosn = 1; soCodePosn = -1;
                   }
                 }
                 else
                 if(inx == '5')
                 {
                   if(callType == 'G')
                   {
                     q = "SELECT DISTINCT CustomerPOCode FROM wo WHERE CustomerPOCode > '" + codeArg + "'" + companyCodeANDWHERE + " ORDER BY CustomerPOCode";

                     woCodePosn = -1; datePosn = -1; companyCodePosn = -1; companyNamePosn = -1; customerPOCodePosn = 1; statusPosn = -1; salesPersonPosn = -1; soCodePosn = -1;
                   }
                   else
                   {
                     q = "SELECT CustomerPOCode, WOCode, CompanyCode, CompanyName, Status FROM wo WHERE CustomerPOCode = '" + codeArg + "' AND WOCode > '" + codeArg2 + "'" + companyCodeANDWHERE + " ORDER BY CustomerPOCode, WOCode";

                     woCodePosn = 2; datePosn = -1; companyCodePosn = 3; companyNamePosn = 4; customerPOCodePosn = 1; statusPosn = 5; salesPersonPosn = -1; soCodePosn = -1;
                   }
                 }
                 else // inx == '6'
                 {
                   if(callType == 'G')
                   {
                     q = "SELECT DISTINCT SOCode FROM wo WHERE SOCode > '" + codeArg + "'" + companyCodeANDWHERE + " ORDER BY SOCode";

                     woCodePosn = -1; datePosn = -1; companyCodePosn = -1; companyNamePosn = -1; customerPOCodePosn = -1; statusPosn = -1; salesPersonPosn = -1; soCodePosn = 1;
                   }
                   else
                   {
                     q = "SELECT CustomerPOCode, WOCode, CompanyCode, CompanyName, Status FROM wo WHERE CustomerPOCode = '" + codeArg + "' AND WOCode > '" + codeArg2 + "'" + companyCodeANDWHERE + " ORDER BY CustomerPOCode, WOCode";

                     woCodePosn = 2; datePosn = -1; companyCodePosn = 3; companyNamePosn = 4; customerPOCodePosn = -1; statusPosn = 5; salesPersonPosn = -1; soCodePosn = 1;
                   }
                 }

                 stmt.setMaxRows(maxRows);
                 rs = stmt.executeQuery(q);

                 while(rs.next())
                 {
                   woCode         = getString(rs, woCodePosn);
                   date           = getString(rs, datePosn);
                   customerCode   = getString(rs, companyCodePosn);
                   name           = getString(rs, companyNamePosn);
                   customerPOCode = getString(rs, customerPOCodePosn);
                   soCode         = getString(rs, soCodePosn);
                   salesPerson    = getString(rs, salesPersonPosn);
                   status         = getString(rs, statusPosn);

                   if(inx == '1')
                     appendBodyLine(html, inx, woCode, date, customerCode, customerPOCode, name, null, woCode, status, imagesDir);
                   else
                   if(inx == '2')
                   {
                     if(callType == 'G')
                       appendBodyLine2(html, callType, operation, imagesDir, date);
                     else appendBodyLine(html, inx, date, woCode, customerCode, customerPOCode, name, null, woCode, status, imagesDir);
                   }
                   else
                   if(inx == '3')
                   {
                     if(callType == 'G')
                       appendBodyLine2(html, callType, operation, imagesDir, customerCode);
                     else appendBodyLine(html, inx, customerCode, woCode, name, customerPOCode, null, null, woCode, status, imagesDir);
                   }
                   else
                   if(inx == '4')
                   {
                     if(callType == 'G')
                       appendBodyLine2(html, callType, operation, imagesDir, salesPerson);
                     else appendBodyLine(html, inx, salesPerson, woCode, date, customerCode, customerPOCode, name, woCode, status, imagesDir);
                   }
                   else
                   if(inx == '5')
                   {
                     if(callType == 'G')
                       appendBodyLine2(html, callType, operation, imagesDir, customerPOCode);
                     else appendBodyLine(html, inx, customerPOCode, woCode, customerCode, name, null, null, woCode, status, imagesDir);
                   }
                   else // inx == '6'
                   {
                     if(callType == 'G')
                       appendBodyLine2(html, callType, operation, imagesDir, soCode);
                     else appendBodyLine(html, inx, soCode, woCode, customerCode, name, null, null, woCode, status, imagesDir);
                   }

                   if(count++ == 1)
                   {
                     firstCodeOnPage[0] = woCode;

                     if(inx == '1')
                       firstEntryOnPage[0] = woCode;
                     else
                     if(inx == '2')
                       firstEntryOnPage[0] = date;
                     else
                     if(inx == '3')
                       firstEntryOnPage[0] = customerCode;
                     else
                     if(inx == '4')
                       firstEntryOnPage[0] = salesPerson;
                     else
                     if(inx == '5')
                       firstEntryOnPage[0] = customerPOCode;
                     else // inx == '6'
                       firstEntryOnPage[0] = soCode;
                   }
                 }

                 rs.close();

                 lastCodeOnPage[0] = woCode;

                 if(inx == '1')
                   lastEntryOnPage[0] = woCode;
                 else
                 if(inx == '2')
                   lastEntryOnPage[0] = date;
                 else
                 if(inx == '3')
                   lastEntryOnPage[0] = customerCode;
                 else
                 if(inx == '4')
                   lastEntryOnPage[0] = salesPerson;
                 else
                 if(inx == '5')
                   lastEntryOnPage[0] = customerPOCode;
                 else // inx == '6'
                   lastEntryOnPage[0] = soCode;

                 newFirstRecNum[0] = generalUtils.intToStr(generalUtils.intFromStr(lastRecNum) + 1);
                 newLastRecNum[0]  = generalUtils.intToStr(generalUtils.intFromStr(lastRecNum) + count - 1);
                 break;
      case 'P' : // prev page
                 if(inx == '1')
                 {
                   if(callType == 'S')
                     q = "SELECT WOCode, Date, CompanyCode, CustomerPOCode, CompanyName, Status FROM wo WHERE WOCode LIKE '" + srchStr + "%' AND WOCode < '" + codeArg + "'" + companyCodeANDWHERE + " ORDER BY WOCode DESC";
                   else q = "SELECT WOCode, Date, CompanyCode, CustomerPOCode, CompanyName, Status FROM wo WHERE WOCode < '" + codeArg + "'" + companyCodeANDWHERE + " ORDER BY WOCode DESC";

                   woCodePosn = 1; datePosn = 2; companyCodePosn = 3; companyNamePosn = 5; customerPOCodePosn = 4; statusPosn = 6; salesPersonPosn = -1; soCodePosn = -1;
                 }
                 else
                 if(inx == '2')
                 {
                   if(callType == 'G')
                   {
                     q = "SELECT DISTINCT Date FROM wo WHERE Date < {d '" + generalUtils.convertDateToSQLFormat(codeArg) + "'}" + companyCodeANDWHERE + " ORDER BY Date DESC";

                     woCodePosn = -1; datePosn = 1; companyCodePosn = -1; companyNamePosn = -1; customerPOCodePosn = -1; statusPosn = -1; salesPersonPosn = -1; soCodePosn = -1;
                   }
                   else
                   {
                     q = "SELECT Date, WOCode, CompanyCode, CustomerPOCode, CompanyName, Status FROM wo WHERE Date = {d '" + generalUtils.convertDateToSQLFormat(codeArg) + "'} AND WOCode < '" + codeArg2 + "'" + companyCodeANDWHERE
                       + " ORDER BY Date, WOCode DESC";

                     woCodePosn = 2; datePosn = 1; companyCodePosn = 3; companyNamePosn = 5; customerPOCodePosn = 4; statusPosn = 6; salesPersonPosn = -1; soCodePosn = -1;
                   }
                 }
                 else
                 if(inx == '3')
                 {
                   if(callType == 'G')
                   {
                     q = "SELECT DISTINCT CompanyCode FROM wo WHERE CompanyCode < '" + codeArg + "' ORDER BY CompanyCode DESC";

                     woCodePosn = -1; datePosn = -1; companyCodePosn = 1; companyNamePosn = -1; customerPOCodePosn = -1; statusPosn = -1; salesPersonPosn = -1; soCodePosn = -1;
                   }
                   else
                   {
                     q = "SELECT CompanyCode, WOCode, CompanyName, CustomerPOCode, Status FROM wo WHERE CompanyCode = '" + codeArg + "' AND WOCode < '" + codeArg2 + "' ORDER BY CompanyCode, WOCode DESC";

                     woCodePosn = 2; datePosn = -1; companyCodePosn = 1; companyNamePosn = 3; customerPOCodePosn = 4; statusPosn = 5; salesPersonPosn = -1; soCodePosn = -1;
                   }
                 }
                 else
                 if(inx == '4')
                 {
                   if(callType == 'G')
                   {
                     q = "SELECT DISTINCT SalesPerson FROM wo WHERE SalesPerson < '" + codeArg + "'" + companyCodeANDWHERE + " ORDER BY SalesPerson DESC";

                     woCodePosn = -1; datePosn = -1; companyCodePosn = -1; companyNamePosn = -1; customerPOCodePosn = -1; statusPosn = -1; salesPersonPosn = 1; soCodePosn = -1;
                   }
                   else
                   {
                     q = "SELECT SalesPerson, WOCode, Date, CompanyCode, CustomerPOCode, CompanyName, Status FROM wo WHERE SalesPerson = '" + codeArg + "' AND WOCode < '" + codeArg2 + "'" + companyCodeANDWHERE
                       + " ORDER BY SalesPerson, WOCode DESC";

                     woCodePosn = 2; datePosn = 3; companyCodePosn = 4; companyNamePosn = 6; customerPOCodePosn = 5; statusPosn = 7; salesPersonPosn = 1; soCodePosn = -1;
                   }
                 }
                 else
                 if(inx == '5')
                 {
                   if(callType == 'G')
                   {
                     q = "SELECT DISTINCT CustomerPOCode FROM wo WHERE CustomerPOCode < '" + codeArg + "'" + companyCodeANDWHERE + " ORDER BY CustomerPOCode DESC";

                     woCodePosn = -1; datePosn = -1; companyCodePosn = -1; companyNamePosn = -1; customerPOCodePosn = 1; statusPosn = -1; salesPersonPosn = -1; soCodePosn = -1;
                   }
                   else
                   {
                     q = "SELECT CustomerPOCode, WOCode, CompanyCode, CompanyName, Status FROM wo WHERE CustomerPOCode = '" + codeArg + "' AND WOCode < '" + codeArg2 + "'" + companyCodeANDWHERE + " ORDER BY CustomerPOCode, WOCode DESC";

                     woCodePosn = 2; datePosn = -1; companyCodePosn = 3; companyNamePosn = 4; customerPOCodePosn = 1; statusPosn = 5; salesPersonPosn = -1; soCodePosn = -1;
                   }
                 }
                 else // inx == '6'
                 {
                   if(callType == 'G')
                   {
                     q = "SELECT DISTINCT SOCode FROM wo WHERE SOCode < '" + codeArg + "'" + companyCodeANDWHERE + " ORDER BY SOCode DESC";

                     woCodePosn = -1; datePosn = -1; companyCodePosn = -1; companyNamePosn = -1; customerPOCodePosn = -1; statusPosn = -1; salesPersonPosn = -1; soCodePosn = 1;
                   }
                   else
                   {
                     q = "SELECT SOCode, WOCode, CompanyCode, CompanyName, Status FROM wo WHERE SOCode = '" + codeArg + "' AND WOCode < '" + codeArg2 + "'" + companyCodeANDWHERE + " ORDER BY SOCode, WOCode DESC";

                     woCodePosn = 2; datePosn = -1; companyCodePosn = 3; companyNamePosn = 4; customerPOCodePosn = -1; statusPosn = 5; salesPersonPosn = -1; soCodePosn = 1;
                   }
                 }

                 stmt.setMaxRows(maxRows);
                 rs = stmt.executeQuery(q);

                 while(rs.next())
                 {
                   woCode         = getString(rs, woCodePosn);
                   date           = getString(rs, datePosn);
                   customerCode   = getString(rs, companyCodePosn);
                   name           = getString(rs, companyNamePosn);
                   customerPOCode = getString(rs, customerPOCodePosn);
                   soCode         = getString(rs, soCodePosn);
                   salesPerson    = getString(rs, salesPersonPosn);
                   status         = getString(rs, statusPosn);

                   if(inx == '1')
                     prependBodyLine(html, inx, woCode, date, customerCode, customerPOCode, name, null, woCode, status, imagesDir);
                   else
                   if(inx == '2')
                   {
                     if(callType == 'G')
                       prependBodyLine2(html, callType, date);
                     else prependBodyLine(html, inx, date, woCode, customerCode, customerPOCode, name, null, woCode, status, imagesDir);
                   }
                   else
                   if(inx == '3')
                   {
                     if(callType == 'G')
                       prependBodyLine2(html, callType, customerCode);
                     else prependBodyLine(html, inx, customerCode, woCode, name, customerPOCode, null, null, woCode, status, imagesDir);
                   }
                   else
                   if(inx == '4')
                   {
                     if(callType == 'G')
                       prependBodyLine2(html, callType, salesPerson);
                     else prependBodyLine(html, inx, salesPerson, woCode, date, customerCode, customerPOCode, name, woCode, status, imagesDir);
                   }
                   else
                   if(inx == '5')
                   {
                     if(callType == 'G')
                       prependBodyLine2(html, callType, soCode);
                     else prependBodyLine(html, inx, soCode, woCode, customerCode, name, null, null, woCode, status, imagesDir);
                   }
                   else // inx == '6'
                   {
                     if(callType == 'G')
                       prependBodyLine2(html, callType, soCode);
                     else prependBodyLine(html, inx, soCode, woCode, customerCode, name, null, null, woCode, status, imagesDir);
                   }

                   if(count++ == 1)
                   {
                     lastCodeOnPage[0] = woCode;

                     if(inx == '1')
                       lastEntryOnPage[0] = woCode;
                     else
                     if(inx == '2')
                       lastEntryOnPage[0] = date;
                     else
                     if(inx == '3')
                       lastEntryOnPage[0] = customerCode;
                     else
                     if(inx == '4')
                       lastEntryOnPage[0] = salesPerson;
                     else
                     if(inx == '5')
                       lastEntryOnPage[0] = customerPOCode;
                     else // inx == '6'
                       lastEntryOnPage[0] = soCode;
                   }
                 }

                 rs.close();

                 firstCodeOnPage[0] = woCode;

                 if(inx == '1')
                   firstEntryOnPage[0] = woCode;
                 else
                 if(inx == '2')
                   firstEntryOnPage[0] = date;
                 else
                 if(inx == '3')
                   firstEntryOnPage[0] = customerCode;
                 else
                 if(inx == '4')
                   firstEntryOnPage[0] = salesPerson;
                 else
                 if(inx == '5')
                   firstEntryOnPage[0] = customerPOCode;
                 else // inx == '6'
                   firstEntryOnPage[0] = soCode;

                 newFirstRecNum[0] = generalUtils.intToStr(generalUtils.intFromStr(firstRecNum) - count   + 1);
                 newLastRecNum[0]  = generalUtils.intToStr(generalUtils.intFromStr(firstRecNum) - 1);
                 break;
    }

    if(stmt != null) stmt.close();
    if(con  != null) con.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void setNav(PrintWriter out, boolean first, char inx, char operation, char callType, int numRecs, String firstCodeOnPage, String lastCodeOnPage, String firstEntryOnPage, String lastEntryOnPage, String srchStr, String imagesDir,
                      String newFirstRecNum, String newLastRecNum, int maxRows, int[] bytesOut) throws Exception
  {
    if(first)
    {
      String title;
      if(callType == 'S')
      {
        title = "Works Order Search Results for ";
        switch(inx)
        {
          case '1' : title += "Works Order Code";  break;
          case '2' : title += "Works Order Date";  break;
          case '3' : title += "Customer Code";     break;
          case '4' : title += "Sales Person";      break;
          case '5' : title += "Customer PO Code";  break;
          case '6' : title += "Sales Order Code";  break;
        }
        title += " starting with: " + srchStr;
      }
      else
      if(operation == 'X')
      {
        title = "Works Order Listing by ";
        switch(inx)
        {
          case '1' : title += "Works Order Code";  break;
          case '2' : title += "Works Order Date";  break;
          case '3' : title += "Customer Code";     break;
          case '4' : title += "Sales Person";      break;
          case '5' : title += "Customer PO Code";  break;
          case '6' : title += "Sales Order Code";  break;
        }
      }
      else title = "Works Order Listing";

      drawingUtils.drawTitleW(out, false, false, "WorksOrderListing", "", title, "4442", "", "", bytesOut);
    }

    scoutln(out, bytesOut, "<form><table id=\"page\" border=0 cellspacing=0 cellpadding=0 width=100%>");

    scoutln(out, bytesOut, "<tr><td height=20 nowrap><p>");

    int newFirstRecNumI = generalUtils.strToInt(newFirstRecNum);
    int newLastRecNumI  = generalUtils.strToInt(newLastRecNum);

    if(numRecs == 0)
    {
      if(first)
        scoutln(out, bytesOut, "No Records");
    }
    else
    {
      scoutln(out, bytesOut, "Records " + newFirstRecNum + " to " + newLastRecNum + " of " + numRecs);
      if(inx == '1' || callType != 'S')
      {
        if(newFirstRecNumI > 1 || newLastRecNumI < numRecs)
          scoutln(out, bytesOut, "<img src=\"" + imagesDir + "d.gif\">");
      }
    }

    if(inx == '1' || callType != 'S')
    {
      char topOrBottom;
      if(first)
        topOrBottom = 'T';
      else topOrBottom = 'B';

      if(newFirstRecNumI > 1)
      {
        String arg = "";

        int len = firstEntryOnPage.length();

        for(int y=0;y<len;++y)
        {
          if(firstEntryOnPage.charAt(y) == '\'')
            arg += "\\'";
          else arg += firstEntryOnPage.charAt(y);
        }

        scoutln(out, bytesOut, "&nbsp;<a href=\"javascript:page('F','" + callType + "','" + arg + "','','" + topOrBottom + "')\">First</a>");
        scoutln(out, bytesOut, "&nbsp;&nbsp;<a href=\"javascript:page('P','" + callType + "','" + arg + "','" + generalUtils.sanitise(firstCodeOnPage) + "','" + topOrBottom + "')\">Previous</a>");
      }

      if(newLastRecNumI < numRecs)
      {
        String arg = "";
        int len = lastEntryOnPage.length();
        for(int y=0;y<len;++y)
        {
          if(lastEntryOnPage.charAt(y) == '\'')
            arg += "\\'";
          else arg += lastEntryOnPage.charAt(y);
        }

        scoutln(out, bytesOut, "&nbsp;&nbsp;<a href=\"javascript:page('N','" + callType + "','" + arg + "','" + generalUtils.sanitise(lastCodeOnPage) + "','" + topOrBottom + "')\">Next</a>");
        scoutln(out, bytesOut, "&nbsp;&nbsp;<a href=\"javascript:page('L','" + callType + "','" + arg + "','','"+topOrBottom+"')\">Last</a>");
      }
    }

    if(numRecs > 0)
      scoutln(out, bytesOut, "</td></tr><tr><td>");

    scoutln(out, bytesOut, "</td></tr><tr><td nowrap><p>");

    String arg = "";
    int len = lastEntryOnPage.length();
    for(int y=0;y<len;++y)
    {
      if(lastEntryOnPage.charAt(y) == '\'')
        arg += "\\'";
      else arg += lastEntryOnPage.charAt(y);
    }

    if(first)
      scoutln(out, bytesOut, "<a href=\"javascript:page('F','S','" + arg + "','','T')\">Search</a> on ");
    else
    {
      if(numRecs > 0)
        scoutln(out, bytesOut, "<a href=\"javascript:page('F','S','','','B')\">Search</a> on ");
    }

    if(numRecs > 0 || first)
    {
      switch(inx)
      {
        case '1' : scout(out, bytesOut, "Works Order Code");  break;
        case '2' : scout(out, bytesOut, "Works Order Date");  break;
        case '3' : scout(out, bytesOut, "Customer Code");     break;
        case '4' : scout(out, bytesOut, "Sales Person");      break;
        case '5' : scout(out, bytesOut, "Customer PO Code");  break;
        case '6' : scout(out, bytesOut, "Sales Order Code");  break;
      }
    }

    if(first)
    {
      scoutln(out, bytesOut, "&nbsp;<input type=text size=20 maxlength=20 value=\"" + srchStr + "\" name=srchStr1>");
      scoutln(out, bytesOut, "&nbsp;&nbsp;&nbsp;PageSize&nbsp;&nbsp;<input type=text size=4 maxlength=4 value=\"" + maxRows + "\" name=maxRows1><br><img src=\"" + imagesDir + "z402.gif\">");
    }
    else
    {
      if(numRecs > 0)
      {
        scoutln(out, bytesOut, "&nbsp;<input type=text size=20 maxlength=20 value=\"" + srchStr + "\" name=srchStr2>");
        scoutln(out, bytesOut, "&nbsp;&nbsp;PageSize&nbsp;&nbsp;<input type=text size=4 maxlength=4 value=\"" + maxRows + "\" name=maxRows2></td></tr><tr><td><img src=\""+imagesDir+"z402.gif\">");
      }
    }

    scoutln(out, bytesOut, "</td></tr></table><table id=\"page\" border=0 width=100% cellspacing=0 cellpadding=0>");

    if(first)
      scoutln(out, bytesOut, "<tr><td><img src=\"" + imagesDir + "z402.gif\"></td></tr>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void setHead(Connection con, Statement stmt, ResultSet rs, PrintWriter out, char inx, String unm, String dnm, String newFirstRecNum, String newLastRecNum, int numRecs, String companyCode, String defnsDir, int[] bytesOut)
                       throws Exception
  {
    scoutln(out, bytesOut, "<script javascript=\"JavaScript\">");

    scoutln(out, bytesOut, "function fetch(code){var code2=sanitise(code);getHTML('WorksOrderPagew','&p2=A&p1='+code2);}");

    scoutln(out, bytesOut, "function page(operation,callType,argA,argB,topOrBottom){");
    scoutln(out, bytesOut, "var srchStr='';if(topOrBottom=='T')srchStr=document.forms[0].srchStr1.value;");
    scoutln(out, bytesOut, "else srchStr=document.forms[0].srchStr2.value;");
    scoutln(out, bytesOut, "var maxRows;if(topOrBottom=='T')maxRows=document.forms[0].maxRows1.value;");
    scoutln(out, bytesOut, "else maxRows=document.forms[0].maxRows2.value;");
    scoutln(out, bytesOut, "var arg=sanitise(argA);var arg2=sanitise(argB);");
    scoutln(out, bytesOut, "if(callType=='G'||callType=='S')getHTML('WorksOrderListingWave','&p1=" + inx + "&p2='+operation+'&p3='+arg+'&p4='+arg2+'&p5='+srchStr+'&p6=" + newFirstRecNum + "&p10=" + numRecs + "&p11=" + companyCode
                          + "&p8='+callType+'&p9='+maxRows+'&p7=" + newLastRecNum + "');");
    scoutln(out, bytesOut, "else getHTML('WorksOrderListingWave','&p1=" + inx + "&p2='+operation+'&p3='+arg+'&p4='+arg2+'&p5='+srchStr+'&p6=" + newFirstRecNum + "&p10=" + numRecs + "&p11=" + companyCode + "&p8='+callType+'&p9='+maxRows+'&p7=" + newLastRecNum
                          + "');}");

    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    scoutln(out, bytesOut, "<form>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void startBody(boolean first, PrintWriter out, char inx, String imagesDir, int[] bytesOut) throws Exception
  {
    if(first)
      scoutln(out, bytesOut, "<table id=\"page\" border=0 cellspacing=2 cellpadding=0>");
    else scoutln(out, bytesOut, "<tr><td><img src=\"" + imagesDir + "z402.gif\"></td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap><p>Status &nbsp;</td>");

    switch(inx)
    {
      case '1' : scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Works Order Code &nbsp;</td>");
                 scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Date &nbsp;</td>");
                 scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Customer Code &nbsp;</td>");
                 scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Customer PO Code</td>");
                 scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Customer Name &nbsp;</td></tr>");
                 break;
      case '2' : scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Works Order Date &nbsp;</td>");
                 scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Works Order Code &nbsp;</td>");
                 scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Customer Code &nbsp;</td>");
                 scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Customer PO Code</td>");
                 scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Customer Name &nbsp;</td></tr>");
                 break;
      case '3' : scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Customer Code &nbsp;</td>");
                 scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Customer Name &nbsp;</td>");
                 scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Works Order Code &nbsp;</td>");
                 scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Customer PO Code</td></tr>");
                 break;
      case '4' : scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Sales Person &nbsp;</td>");
                 scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Works Order Code &nbsp;</td>");
                 scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Works Order Date &nbsp;</td>");
                 scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Customer Code &nbsp;</td>");
                 scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Customer PO Code</td>");
                 scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Customer Name &nbsp;</td></tr>");
                 break;
      case '5' : scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Customer PO Code &nbsp;</td>");
                 scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Works Order Code &nbsp;</td>");
                 scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Customer Code</td>");
                 scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Customer Name &nbsp;</td></tr>");
                 break;
      case '6' : scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Sales Order Code &nbsp;</td>");
                 scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Works Order Code &nbsp;</td>");
                 scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Customer Code</td>");
                 scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Customer Name &nbsp;</td></tr>");
                 break;
    }

    scoutln(out, bytesOut, "<tr><td><img src=\"" + imagesDir + "z402.gif\"></td></tr>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void startBody2(boolean first, PrintWriter out, char inx, String imagesDir, int[] bytesOut) throws Exception
  {
    if(first)
      scoutln(out, bytesOut, "<table id=\"page\" border=0 cellspacing=0 cellpadding=0><tr>");

    scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>");

    switch(inx)
    {
      case '2' : scoutln(out, bytesOut, "Works Order Date");  break;
      case '3' : scoutln(out, bytesOut, "Customer Code");     break;
      case '4' : scoutln(out, bytesOut, "Sales Person");      break;
      case '5' : scoutln(out, bytesOut, "Customer PO Code");  break;
      case '6' : scoutln(out, bytesOut, "Sales Order Code");  break;
    }

    scoutln(out, bytesOut, " &nbsp;</b></td></tr>");
    scoutln(out, bytesOut, "<tr><td><img src=\"" + imagesDir + "z402.gif\"></td></tr>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void appendBodyLine(String[] html, char inx, String one, String two, String three, String four, String five, String six, String code, String status, String imagesDir) throws Exception
  {
    String s = checkCode(code);

    if(status.equals("C"))
      html[0] += "<tr><td align=center><img src=\"" + imagesDir + "z0212.gif\" border=0></td>";
    else html[0] += "<tr><td></td>";

    switch(inx)
    {
      case '1' : html[0] += "<td nowrap><p><a href=\"javascript:fetch('" + s + "')\">" + one + "</a>&nbsp;</td>";
                 html[0] += "<td nowrap><p>" + two + "&nbsp;</td>";
                 html[0] += "<td nowrap><p>" + three + "&nbsp;</td>";
                 html[0] += "<td nowrap><p>" + four + "&nbsp;</td>";
                 html[0] += "<td nowrap><p>" + five + "&nbsp;</td></tr>\n";
                 break;
      case '2' : html[0] += "<td nowrap><p>" + one + "&nbsp;</td>";
                 html[0] += "<td nowrap><a href=\"javascript:fetch('" + s + "')\">" + two + "</a>&nbsp;</td>";
                 html[0] += "<td nowrap><p>" + three + "&nbsp;</td>";
                 html[0] += "<td nowrap><p>" + four + "&nbsp;</td>";
                 html[0] += "<td nowrap><p>" + five + "&nbsp;</td></tr>\n";
                 break;
      case '3' : html[0] += "<td nowrap><p>" + one + "&nbsp;</td>";
                 html[0] += "<td nowrap><p>" + three + "&nbsp;</td>";
                 html[0] += "<td nowrap><a href=\"javascript:fetch('" + s + "')\">" + two + "</a>&nbsp;</td>";
                 html[0] += "<td nowrap><p>" + four + "&nbsp;</td></tr>\n";
                 break;
      case '4' : html[0] += "<td nowrap><p>" + one + "&nbsp;</td>";
                 html[0] += "<td nowrap><a href=\"javascript:fetch('" + s + "')\">" + two + "</a>&nbsp;</td>";
                 html[0] += "<td nowrap><p>" + three + "&nbsp;</td>";
                 html[0] += "<td nowrap><p>" + four + "&nbsp;</td>";
                 html[0] += "<td nowrap><p>" + five + "&nbsp;</td>";
                 html[0] += "<td nowrap><p>" + six + "&nbsp;</td></tr>\n";
                 break;
      case '5' :
      case '6' : html[0] += "<td nowrap><p>" + one + "&nbsp;</td>";
                 html[0] += "<td nowrap><a href=\"javascript:fetch('" + s + "')\">" + two + "</a>&nbsp;</td>";
                 html[0] += "<td nowrap><p>" + three + "&nbsp;</td>";
                 html[0] += "<td nowrap><p>" + four + "&nbsp;</td></tr>\n";
                 break;
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void appendBodyLine2(String[] html, char callType, char operation, String imagesDir, String one) throws Exception
  {
    String s;
    if(one == null || one.length() == 0)
    {
      one = "&lt;none&gt;";
      s   = "<none>";
    }
    else s = checkCode(one);

    html[0] += "<tr><td nowrap>";
    if(callType == 'G' || operation == 'S')
      html[0] += "<a href=\"javascript:page('X','D','" + s + "','')\">";
    else html[0] += "<a href=\"javascript:fetch('" + s + "')\">";

    html[0] +=  one + "</a>&nbsp;</td></tr>\n";
    html[0] += "<tr><td><img src=\"" + imagesDir + "z402.gif\"></td></tr>\n";

  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void prependBodyLine(String[] html, char inx, String one, String two, String three, String four, String five, String six, String code, String status, String imagesDir) throws Exception
  {
    String s = checkCode(code);

    String line;
    if(status.equals("C"))
      line = "<tr><td align=center><img src=\"" + imagesDir + "z0212.gif\" border=0></td>";
    else line = "<tr><td></td>";

    switch(inx)
    {
      case '1' : line += "<td nowrap><a href=\"javascript:fetch('" + s + "')\">" + one + "</a>&nbsp;</td>";
                 line += "<td nowrap><p>" + two + "&nbsp;</td>";
                 line += "<td nowrap><p>" + three + "&nbsp;</td>";
                 line += "<td nowrap><p>" + four + "&nbsp;</td>";
                 line += "<td nowrap><p>" + five + "&nbsp;</td>";
                 html[0] = line + "</tr>\n" + html[0];
                 break;
      case '2' : line += "<td nowrap><p>" + one + "&nbsp;</td>";
                 line += "<td nowrap><a href=\"javascript:fetch('" + s + "')\">" + two + "</a>&nbsp;</td>";
                 line += "<td nowrap><p>" + three + "&nbsp;</td>";
                 line += "<td nowrap><p>" + four + "&nbsp;</td>";
                 line += "<td nowrap><p>" + five + "&nbsp;</td>";
                 html[0] = line + "</tr>\n" + html[0];
                 break;
      case '3' : line += "<td nowrap><p>" + one + "&nbsp;</td>";
                 line += "<td nowrap><a href=\"javascript:fetch('" + s + "')\">" + two + "</a>&nbsp;</td>";
                 line += "<td nowrap><p>" + three + "&nbsp;</td>";
                 line += "<td nowrap><p>" + four + "&nbsp;</td>";
                 html[0] = line + "</tr>\n" + html[0];
                 break;
      case '4' : line += "<td nowrap><p>" + one + "&nbsp;</td>";
                 line += "<td nowrap><a href=\"javascript:fetch('" + s + "')\">" + two + "</a>&nbsp;</td>";
                 line += "<td nowrap><p>" + three + "&nbsp;</td>";
                 line += "<td nowrap><p>" + four + "&nbsp;</td>";
                 line += "<td nowrap><p>" + five  + "&nbsp;</td>";
                 line += "<td nowrap><p>" + six + "&nbsp;</td>";
                 html[0] = line + "</tr>\n" + html[0];
                 break;
      case '5' :
      case '6' : line += "<td nowrap><p>" + one + "&nbsp;</td>";
                 line += "<td nowrap><a href=\"javascript:fetch('" + s + "')\"><p>" + two + "</a>&nbsp;</td>";
                 line += "<td nowrap><p>" + three + "&nbsp;</td>";
                 line += "<td nowrap><p>" + four + "&nbsp;</td>";
                 html[0] = line + "</tr>\n" + html[0];
                 break;
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void prependBodyLine2(String[] html, char callType, String one) throws Exception
  {
    String s;
    if(one == null || one.length() == 0)
    {
      one = "&lt;none&gt;";
      s   = "<none>";
    }
    else s = checkCode(one);

    String line = "<tr><td nowrap>";
    if(callType == 'G')
      line += "<a href=\"javascript:page('X','D','" + s + "','')\">";
    else line += "<a href=\"javascript:fetch('" + s + "')\">";

    line += one + "</a>&nbsp;</td></tr>\n";

    html[0] = line + html[0];
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String checkCode(String code) throws Exception
  {
    String s="";
    if(code.indexOf('\'') != -1)
    {
      int len = code.length();
      for(int x=0;x<len;++x)
      {
        if(code.charAt(x) == '\'')
          s += "\\'";
        else s += code.charAt(x);
      }
    }
    else s = code;
    return s;
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

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String getString(ResultSet rs, int posn)
  {
    try
    {
      if(posn <= 0)
        return "";

      String s = rs.getString(posn);

      if(s == null)
        s = "";
      return s;
    }
    catch(Exception e)
    {
      System.out.println("4442: " + e);
    }

    return "";
  }

}
