// =======================================================================================================================================================================================================
// System: ZaraStar Document: Receipt listing page
// Module: ReceiptListing.java
// Author: C.K.Harvey
// Copyright (c) 2001-07 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
//
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.sql.Time;
import java.io.*;

public class ReceiptListing extends HttpServlet
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

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", inx="", codeArg="", codeArg2="", srchStr="", operation="", 
           firstRecNum="", lastRecNum="", callType="", maxRows="", numRecs="", companyCode="";
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

      callType    = req.getParameter("p8"); // 'G'roup (e.g., list of salespeople, or 'D'etail)
      if(callType == null || callType.length() == 0) callType = "G";
   
      maxRows = req.getParameter("p9");
      if(maxRows == null || maxRows.length() == 0) maxRows = "50";

      numRecs    = req.getParameter("p10");
      if(numRecs == null || numRecs.length() == 0) numRecs = "-1";

      companyCode = req.getParameter("p11");
      if(companyCode == null || companyCode.length() == 0 || companyCode.equals("All")) companyCode = "";
      else companyCode = companyCode.toUpperCase();

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, inx, operation, callType, codeArg, codeArg2, srchStr, firstRecNum,
           lastRecNum, generalUtils.strToInt(maxRows), generalUtils.strToInt(numRecs), companyCode, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "ReceiptListing", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 4213, bytesOut[0], 0, "ERR:" + codeArg);
      if(out != null) out.flush();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men,
                    String den, String dnm, String bnm, String inx,  String operation, String callType, String codeArg,
                    String codeArg2, String srchStr, String firstRecNum, String lastRecNum, int maxRows, int numRecs,
                    String companyCode, int[] bytesOut) throws Exception
  {
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');

    long startTime = new java.util.Date().getTime();

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 4213, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "ReceiptListing", imagesDir, localDefnsDir,
                         defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 4213, bytesOut[0], 0, "ACC:" + codeArg);
      
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "ReceiptListing", imagesDir, localDefnsDir,
                         defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 4213, bytesOut[0], 0, "SID:" + codeArg);
      
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(srchStr == null || srchStr.equalsIgnoreCase("null"))
      srchStr = "";

    generate(con, stmt, rs, out, req, unm, sid, uty, men, den, dnm, bnm, inx.charAt(0), operation.charAt(0), callType.charAt(0), codeArg,
             codeArg2, srchStr, firstRecNum, lastRecNum, maxRows, numRecs, companyCode, imagesDir, localDefnsDir, defnsDir,
             bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 4213, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), codeArg);
      
      if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void generate(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men,
                        String den, String dnm, String bnm, char inx, char operation, char callType, String codeArg,
                        String codeArg2, String srchStr, String firstRecNum, String lastRecNum, int maxRows, int numRecsIn,
                        String companyCode, String imagesDir, String localDefnsDir, String defnsDir, int[] bytesOut)
                        throws Exception
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
    createPage(html, operation2, callType, codeArg, codeArg2, inx, srchStr, numRecs, firstCodeOnPage, lastCodeOnPage, 
               firstEntryOnPage, lastEntryOnPage, firstRecNum, lastRecNum, newFirstRecNum, newLastRecNum, maxRows, companyCode,
               imagesDir, dnm, localDefnsDir, defnsDir);

    if(generalUtils.strToInt(newFirstRecNum[0]) <= 0) // prev call has gone back before rec 1
    {  
      newFirstRecNum[0] = "1";
      newLastRecNum[0] = "50";
    }

    int[] hmenuCount = new int[1];

    setHead(con, stmt, rs, out, req, inx, unm, sid, uty, men, den, dnm, bnm, newFirstRecNum[0], newLastRecNum[0], numRecs[0], companyCode,
            imagesDir, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    setNav(stmt, rs, out, true, inx, operation, callType, numRecs[0], firstCodeOnPage[0], lastCodeOnPage[0], firstEntryOnPage[0], 
           lastEntryOnPage[0], srchStr, imagesDir, newFirstRecNum[0], newLastRecNum[0], maxRows, unm, sid, uty, men, den, dnm,
           bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

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
    
    setNav(stmt, rs, out, false, inx, operation, callType, numRecs[0], firstCodeOnPage[0], lastCodeOnPage[0], firstEntryOnPage[0], 
           lastEntryOnPage[0], srchStr, imagesDir, newFirstRecNum[0], newLastRecNum[0], maxRows, unm, sid, uty, men, den, dnm,
           bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "</table></form>");     scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void createPage(String[] html, char operation, char callType, String arg, String arg2, char inx, String srchStr, 
                          int[] numRecs, String[] firstCodeOnPage, String[] lastCodeOnPage, String[] firstEntryOnPage, 
                          String[] lastEntryOnPage, String firstRecNum, String lastRecNum, String[] newFirstRecNum, 
                          String[] newLastRecNum, int maxRows, String companyCode, String imagesDir, String dnm,
                          String localDefnsDir, String defnsDir) throws Exception
  {
    String userName = directoryUtils.getMySQLUserName();
    String passWord = directoryUtils.getMySQLPassWord();
    
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + userName + "&password="
                                                 + passWord);
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
                   {
                     r = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM receipt WHERE Date = {d '"
                       + generalUtils.convertDateToSQLFormat(srchStr) + "'}" + companyCodeANDWHERE);
                   }
                   else
                   if(callType == 'G')
                     r = stmt.executeQuery("SELECT COUNT(DISTINCT Date) AS rowcount FROM receipt" + companyCodeWHERE);
                   else 
                   {
                     r = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM receipt WHERE Date = {d '"
                       + generalUtils.convertDateToSQLFormat(codeArg) + "'}" + companyCodeANDWHERE);
                   }
                   break;
        case '3' : if(callType == 'S')
                   {
                     r = stmt.executeQuery("SELECT COUNT(CompanyCode) AS rowcount FROM receipt WHERE CompanyCode LIKE '"
                       + srchStr + "%'");
                   }
                   else
                   if(callType == 'G')
                      r = stmt.executeQuery("SELECT COUNT(DISTINCT CompanyCode) AS rowcount FROM receipt" + companyCodeWHERE);  
                   else r = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM receipt WHERE CompanyCode = '" + codeArg + "'");
                   break;
        case '4' : if(callType == 'S')
                   {
                     r = stmt.executeQuery("SELECT COUNT(ReceiptReference) AS rowcount FROM receipt WHERE ReceiptReference LIKE '"
                       + srchStr + "%'");
                   }
                   else
                   if(callType == 'G')
                     r = stmt.executeQuery("SELECT COUNT(DISTINCT ReceiptReference) AS rowcount FROM receipt" + companyCodeWHERE);  
                   else r = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM receipt WHERE ReceiptReference = '" + codeArg + "'");
                   break;
        case '5' : if(callType == 'S')
                   {
                     r = stmt.executeQuery("SELECT COUNT(ARCode) AS rowcount FROM receipt WHERE ARCode LIKE '" + srchStr + "%'");
                   }
                   else
                   if(callType == 'G')
                     r = stmt.executeQuery("SELECT COUNT(DISTINCT ARCode) AS rowcount FROM receipt" + companyCodeWHERE);  
                   else r = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM receipt WHERE ARCode = '" + codeArg + "'");
                   break;
        default  : // case '1'
                   if(callType == 'S')
                   {
                     r = stmt.executeQuery("SELECT COUNT(DISTINCT ReceiptCode) AS rowcount FROM receipt WHERE ReceiptCode LIKE '"
                       + srchStr + "%'" + companyCodeANDWHERE);
                   }
                   else
                   if(operation == 'N' && callType=='2')
                   {
                     r = stmt.executeQuery("SELECT COUNT(DISTINCT ReceiptCode) AS rowcount FROM receipt WHERE ReceiptCode LIKE '"
                       + srchStr + "%'" + companyCodeANDWHERE); // always calc the total number even though we are on subsequent page
                   }
                   else r = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM receipt" + companyCodeWHERE);
                   break;
      }
      r.next();
      numRecs[0] = r.getInt("rowcount") ;
      r.close() ;
    }

    ResultSet rs = null;
    ResultSetMetaData rsmd = null;

    String receiptCode="", name="", customerCode="", date="", totalAmount="", arCode="", receiptReference="", status;
    int receiptCodePosn, datePosn, companyCodePosn, companyNamePosn, statusPosn, totalAmountPosn, arCodePosn,
        receiptReferencePosn;
     
    switch(operation)
    {
      case 'F' : // first page
                 if(inx == '1')
                 {
                   if(callType == 'S')
                   {
                     q = "SELECT ReceiptCode, Date, CompanyCode, TotalAmount, CompanyName, Status FROM receipt WHERE ReceiptCode LIKE '"
                       + srchStr + "%'" + companyCodeANDWHERE + " ORDER BY ReceiptCode";
                   }
                   else
                   {
                     q = "SELECT ReceiptCode, Date, CompanyCode, TotalAmount, CompanyName, Status FROM receipt " + companyCodeWHERE
                       + "ORDER BY ReceiptCode";
                   }

                   receiptCodePosn = 1; datePosn = 2; companyCodePosn = 3; companyNamePosn = 5; statusPosn = 6; totalAmountPosn = 4; 
                   arCodePosn = -1; receiptReferencePosn = -1;
                 }
                 else
                 if(inx == '2')
                 {
                   if(callType == 'S')
                   {
                     q = "SELECT Date, ReceiptCode, CompanyCode, TotalAmount, CompanyName, Status FROM receipt WHERE Date = {d '" 
                       + generalUtils.convertDateToSQLFormat(srchStr) + "'}" + companyCodeANDWHERE + " ORDER BY Date, ReceiptCode";
                     
                     receiptCodePosn = 2; datePosn = 1; companyCodePosn = 3; companyNamePosn = 5; statusPosn = 6; 
                     totalAmountPosn = 4; arCodePosn = -1; receiptReferencePosn = -1;
                   }
                   else
                   if(callType == 'G')
                   {
                     q = "SELECT DISTINCT Date FROM receipt ORDER BY Date" + companyCodeWHERE;
                     
                     receiptCodePosn = -1; datePosn = 1; companyCodePosn = -1; companyNamePosn = -1; statusPosn = -1;
                     totalAmountPosn = -1; arCodePosn = -1; receiptReferencePosn = -1;
                   }
                   else
                   {
                     q = "SELECT Date, ReceiptCode, CompanyCode, TotalAmount, CompanyName, Status FROM receipt WHERE Date = {d '"
                       + generalUtils.convertDateToSQLFormat(codeArg) + "'}" + companyCodeANDWHERE + " ORDER BY Date, ReceiptCode";
                     
                     receiptCodePosn = 2; datePosn = 1; companyCodePosn = 3; companyNamePosn = 5; statusPosn = 6;
                     totalAmountPosn = 4; arCodePosn = -1; receiptReferencePosn = -1;
                   }
                 }  
                 else
                 if(inx == '3')
                 {  
                   if(callType == 'S')
                   {
                     q = "SELECT CompanyCode, ReceiptCode, Date, TotalAmount, CompanyName, Status FROM receipt WHERE "
                      + "CompanyCode LIKE '" + srchStr + "%' ORDER BY CompanyCode, ReceiptCode";
                     
                     receiptCodePosn = 2; datePosn = 3; companyCodePosn = 1; companyNamePosn = 5; statusPosn = 6;
                     totalAmountPosn = 4; arCodePosn = -1; receiptReferencePosn = -1;
                   }
                   else
                   if(callType == 'G')
                   {
                     q = "SELECT DISTINCT CompanyCode FROM receipt " + companyCodeWHERE + "ORDER BY CompanyCode";
                     
                     receiptCodePosn = -1; datePosn = -1; companyCodePosn = 1; companyNamePosn = -1; statusPosn = -1;
                     totalAmountPosn = -1; arCodePosn = -1; receiptReferencePosn = -1;
                   }
                   else
                   {
                     q = "SELECT CompanyCode, ReceiptCode, Date, TotalAmount, CompanyName, Status FROM receipt WHERE "
                       + "CompanyCode = '" + codeArg + "' ORDER BY CompanyCode";
                     
                     receiptCodePosn = 2; datePosn = 3; companyCodePosn = 1; companyNamePosn = 5; statusPosn = 6;
                     totalAmountPosn = 4; arCodePosn = -1; receiptReferencePosn = -1;
                   }
                 }  
                 else
                 if(inx == '4')
                 {  
                   if(callType == 'S')
                   {
                     q = "SELECT ReceiptReference, ReceiptCode, Date, CompanyCode, CompanyName, Status FROM receipt WHERE "
                      + "ReceiptReference LIKE '" + srchStr + "%' ORDER BY ReceiptReference, ReceiptCode";
                     
                     receiptCodePosn = 2; datePosn = 3; companyCodePosn = 4; companyNamePosn = 5; statusPosn = 6;
                     totalAmountPosn = -1; arCodePosn = -1; receiptReferencePosn = 1;
                   }
                   else
                   if(callType == 'G')
                   {
                     q = "SELECT DISTINCT ReceiptReference FROM receipt " + companyCodeWHERE + "ORDER BY ReceiptReference";
                     
                     receiptCodePosn = -1; datePosn = -1; companyCodePosn = -1; companyNamePosn = -1; statusPosn = -1; 
                     totalAmountPosn = -1; arCodePosn = -1; receiptReferencePosn = 1;
                   }
                   else
                   {
                     q = "SELECT ReceiptReference, ReceiptCode, Date, CompanyCode, CompanyName, Status FROM receipt WHERE "
                       + "ReceiptReference = '" + codeArg + "' ORDER BY ReceiptReference";
                     
                     receiptCodePosn = 2; datePosn = 3; companyCodePosn = 4; companyNamePosn = 5; statusPosn = 6;
                     totalAmountPosn = -1; arCodePosn = -1; receiptReferencePosn = 1;
                   }
                 }  
                 else // inx == '5'
                 {  
                   if(callType == 'S')
                   {
                     q = "SELECT ARCode, ReceiptCode, Date, CompanyCode, CompanyName, Status FROM receipt WHERE ARCode LIKE '"
                       + srchStr + "%' ORDER BY ARCode, ReceiptCode";
                 
                     receiptCodePosn = 2; datePosn = 3; companyCodePosn = 4; companyNamePosn = 5; statusPosn = 6;
                     totalAmountPosn = -1; arCodePosn = 1; receiptReferencePosn = 1;
                   } 
                   else
                   if(callType == 'G')
                   {
                     q = "SELECT DISTINCT ARCode FROM receipt " + companyCodeWHERE + "ORDER BY ARCode";
                 
                     receiptCodePosn = -1; datePosn = -1; companyCodePosn = -1; companyNamePosn = -1; statusPosn = -1; 
                     totalAmountPosn = -1; arCodePosn = 1; receiptReferencePosn = -1;
                   }
                   else
                   {
                     q = "SELECT ARCode, ReceiptCode, Date, CompanyCode, CompanyName, Status FROM receipt WHERE ARCode = '"
                       + codeArg + "' ORDER BY ARCode";
                 
                     receiptCodePosn = 2; datePosn = 3; companyCodePosn = 4; companyNamePosn = 5; statusPosn = 6;
                     totalAmountPosn = -1; arCodePosn = 1; receiptReferencePosn = -1;
                   }
                 }  

                 stmt.setMaxRows(maxRows);
                 rs = stmt.executeQuery(q);
                 rsmd = rs.getMetaData();

                 while(rs.next())                  
                 {
                   receiptCode      = getValue(receiptCodePosn,          ' ', rs, rsmd);
                   date             = getValue(datePosn,                 'D', rs, rsmd);
                   customerCode     = getValue(companyCodePosn,          ' ', rs, rsmd);
                   name             = getValue(companyNamePosn,          ' ', rs, rsmd);
                   receiptReference = getValue(receiptReferencePosn, ' ', rs, rsmd);
                   arCode           = getValue(arCodePosn,               ' ', rs, rsmd);
                   totalAmount      = getValue(totalAmountPosn,          ' ', rs, rsmd);
                   status           = getValue(statusPosn,               ' ', rs, rsmd);

                   if(inx == '1')
                     appendBodyLine(html, inx, receiptCode, date, customerCode, totalAmount, name, receiptCode, status, imagesDir);
                   else
                   if(inx == '2')
                   {
                     if(callType == 'G')
                       appendBodyLine2(html, callType, operation, imagesDir, date);
                     else appendBodyLine(html, inx, date, receiptCode, customerCode, totalAmount, name, receiptCode, status, imagesDir);
                   }   
                   else
                   if(inx == '3')
                   {
                     if(callType == 'G')
                       appendBodyLine2(html, callType, operation, imagesDir, customerCode);
                     else
                     {
                       appendBodyLine(html, inx, customerCode, receiptCode, date, totalAmount, name, receiptCode, status, imagesDir);
                     }
                   }   
                   else
                   if(inx == '4')
                   {
                     if(callType == 'G')
                       appendBodyLine2(html, callType, operation, imagesDir, receiptReference);
                     else
                     {
                       appendBodyLine(html, inx, receiptReference, receiptCode, date, customerCode, name, receiptCode, status,
                                      imagesDir);
                     }
                   }   
                   else // inx == '5'
                   {
                     if(callType == 'G')
                       appendBodyLine2(html, callType, operation, imagesDir, arCode);
                     else
                     {
                       appendBodyLine(html, inx, arCode, receiptCode, date, customerCode, name, receiptCode, status, imagesDir);
                     }
                   }   
          
                   if(count++ == 1)
                   {  
                     firstCodeOnPage[0] = receiptCode;
                     
                     if(inx == '1')
                       firstEntryOnPage[0] = receiptCode;
                     else
                     if(inx == '2')
                       firstEntryOnPage[0] = date;
                     else
                     if(inx == '3')
                       firstEntryOnPage[0] = customerCode;
                     else 
                     if(inx == '4')
                       firstEntryOnPage[0] = receiptReference;
                     else // inx == '5'
                     firstEntryOnPage[0] = arCode;
                   }
                 }
                 
                 rs.close();
                                 
                 lastCodeOnPage[0] = receiptCode;

                 if(inx == '1')
                   lastEntryOnPage[0] = receiptCode;
                 else
                 if(inx == '2')
                   lastEntryOnPage[0] = date;
                 else 
                 if(inx == '3')
                   lastEntryOnPage[0] = customerCode;
                 else 
                 if(inx == '4')
                   lastEntryOnPage[0] = receiptReference;
                 else // inx == '5'
                   lastEntryOnPage[0] = arCode;
 
                 newFirstRecNum[0] = "1";
                 newLastRecNum[0]  = generalUtils.intToStr(count - 1);
                 break;
      case 'L' : // last page
                 if(inx == '1')
                 {
                   if(callType == 'S')
                   {
                     q = "SELECT ReceiptCode, Date, CompanyCode, TotalAmount, CompanyName, Status FROM receipt WHERE ReceiptCode LIKE '"
                       + srchStr + "%' AND ReceiptCode > '" + codeArg + "'" + companyCodeANDWHERE + " ORDER BY ReceiptCode DESC";
                   }
                   else
                   {  
                     q = "SELECT ReceiptCode, Date, CompanyCode, TotalAmount, CompanyName, Status FROM receipt WHERE ReceiptCode "
                       + "<= 'ZZZZZZZZZZZZZZZZZZZZ'" + companyCodeANDWHERE + " ORDER BY ReceiptCode DESC";
                   }
                   
                   receiptCodePosn = 1; datePosn = 2; companyCodePosn = 3; companyNamePosn = 5; statusPosn = 6;
                   totalAmountPosn = 4; arCodePosn = -1; receiptReferencePosn = -1;
                 }  
                 else
                 if(inx == '2')
                 {              
                   if(callType == 'G')
                   {
                     q = "SELECT DISTINCT Date FROM receipt WHERE Date <= {d '9999-12-31'}" + companyCodeANDWHERE
                       + " ORDER BY Date DESC";
                     
                     receiptCodePosn = -1; datePosn = 1; companyCodePosn = -1; companyNamePosn = -1; statusPosn = -1;
                     totalAmountPosn = -1; arCodePosn = -1; receiptReferencePosn = -1;
                   }
                   else
                   {
                     q = "SELECT Date, ReceiptCode, CompanyCode, TotalAmount, CompanyName, Status FROM receipt WHERE Date = {d '"
                       + generalUtils.convertDateToSQLFormat(codeArg) + "'}" + companyCodeANDWHERE + " ORDER BY Date, ReceiptCode DESC";
                     
                     receiptCodePosn = 2; datePosn = 1; companyCodePosn = 3; companyNamePosn = 5; statusPosn = 6;
                     totalAmountPosn = 4; arCodePosn = -1; receiptReferencePosn = -1;
                   }  
                 }
                 else
                 if(inx == '3')
                 {  
                   if(callType == 'G')
                   {
                     q = "SELECT DISTINCT CompanyCode FROM receipt WHERE CompanyCode <= 'ZZZZZZZZZZZZZZZZZZZ' ORDER BY CompanyCode DESC";
                     
                     receiptCodePosn = -1; datePosn = -1; companyCodePosn = 1; companyNamePosn = -1; statusPosn = -1;
                     totalAmountPosn = -1; arCodePosn = -1; receiptReferencePosn = -1;
                   }
                   else
                   {
                      q = "SELECT CompanyCode, ReceiptCode, Date, TotalAmount, CompanyName, Status FROM receipt WHERE "
                        + "CompanyCode = '" + codeArg + "' ORDER BY CompanyCode, ReceiptCode DESC";
                      
                      receiptCodePosn = 2; datePosn = 3; companyCodePosn = 1; companyNamePosn = 5; statusPosn = 6;
                      totalAmountPosn = 4; arCodePosn = -1; receiptReferencePosn = -1;
                   }
                 }
                 else
                 if(inx == '4')
                 {  
                   if(callType == 'G')
                   {
                     q = "SELECT DISTINCT ReceiptReference FROM receipt WHERE ReceiptReference <= 'zzzzzzzzzzzzzzzzzzz' ORDER BY "
                     + "ReceiptReference DESC";
                     
                     receiptCodePosn = -1; datePosn = -1; companyCodePosn = 1; companyNamePosn = -1; statusPosn = -1;
                     totalAmountPosn = -1; arCodePosn = -1; receiptReferencePosn = 1;
                   }
                   else
                   {
                      q = "SELECT ReceiptReference, ReceiptCode, Date, CompanyCode, CompanyName, Status FROM receipt WHERE "
                        + "ReceiptReference = '" + codeArg + "' ORDER BY ReceiptReference, ReceiptCode DESC";
                    
                      receiptCodePosn = 2; datePosn = 3; companyCodePosn = 4; companyNamePosn = 5; statusPosn = 6;
                      totalAmountPosn = -1; arCodePosn = -1; receiptReferencePosn = 1;
                   }
                 }
                 else // inx == '5'
                 {  
                   if(callType == 'G')
                   {
                     q = "SELECT DISTINCT ARCode FROM receipt WHERE ARCode <= 'ZZZZZZZZZZZZZZZZZZZ' ORDER BY ARCode DESC";
                     
                     receiptCodePosn = -1; datePosn = -1; companyCodePosn = 1; companyNamePosn = -1; statusPosn = -1;
                     totalAmountPosn = -1; arCodePosn = 1; receiptReferencePosn = -1;
                   }
                   else
                   {
                      q = "SELECT ARCode, ReceiptCode, Date, CompanyCode, CompanyName, Status FROM receipt WHERE ARCode = '"
                        + codeArg + "' ORDER BY ARCode, ReceiptCode DESC";
                    
                      receiptCodePosn = 2; datePosn = 3; companyCodePosn = 4; companyNamePosn = 5; statusPosn = 6;
                      totalAmountPosn = -1; arCodePosn = 1; receiptReferencePosn = -1;
                   }
                 }

                 stmt.setMaxRows(maxRows);
                 rs = stmt.executeQuery(q);
                 rsmd = rs.getMetaData();

                 while(rs.next())                  
                 {
                   receiptCode      = getValue(receiptCodePosn,          ' ', rs, rsmd);
                   date             = getValue(datePosn,                 'D', rs, rsmd);
                   customerCode     = getValue(companyCodePosn,          ' ', rs, rsmd);
                   name             = getValue(companyNamePosn,          ' ', rs, rsmd);
                   receiptReference = getValue(receiptReferencePosn, ' ', rs, rsmd);
                   arCode           = getValue(arCodePosn,               ' ', rs, rsmd);
                   totalAmount      = getValue(totalAmountPosn,          ' ', rs, rsmd);
                   status           = getValue(statusPosn,               ' ', rs, rsmd);

                   if(inx == '1')
                     prependBodyLine(html, inx, receiptCode, date, customerCode, totalAmount, name, receiptCode, status, imagesDir);
                   else
                   if(inx == '2')
                   {
                     if(callType == 'G')
                       prependBodyLine2(html, callType, date);
                     else prependBodyLine(html, inx, date, receiptCode, customerCode, totalAmount, name, receiptCode, status, imagesDir);
                   }  
                   else
                   if(inx == '3')
                   {
                     if(callType == 'G')
                       prependBodyLine2(html, callType, customerCode);
                     else 
                     {
                       prependBodyLine(html, inx, customerCode, receiptCode, date, totalAmount, name, receiptCode, status, imagesDir);
                     }
                   }  
                   else
                   if(inx == '4')
                   {
                     if(callType == 'G')
                       prependBodyLine2(html, callType, receiptReference);
                     else 
                     {
                       prependBodyLine(html, inx, receiptReference, receiptCode, date, customerCode, name, receiptCode, status, imagesDir);
                     }
                   }  
                   else // inx == '5'
                   {
                     if(callType == 'G')
                       prependBodyLine2(html, callType, arCode);
                     else 
                     {
                       prependBodyLine(html, inx, arCode, receiptCode, date, customerCode, name, receiptCode, status, imagesDir);
                     }
                   }  
          
                   if(count++ == 1)
                   {  
                     lastCodeOnPage[0] = receiptCode;

                     if(inx == '1')
                       lastEntryOnPage[0] = receiptCode;
                     else
                     if(inx == '2')
                       lastEntryOnPage[0] = date;
                     else
                     if(inx == '3')
                       lastEntryOnPage[0] = customerCode;
                     else
                     if(inx == '4')
                       lastEntryOnPage[0] = receiptReference;
                     else // inx == '5'
                       lastEntryOnPage[0] = arCode;
                   }
                 }
                 
                 rs.close();
                                 
                 firstCodeOnPage[0] = receiptCode;

                 if(inx == '1')
                   firstEntryOnPage[0] = receiptCode;
                 else
                 if(inx == '2')
                   firstEntryOnPage[0] = date;
                 else
                 if(inx == '3')
                   firstEntryOnPage[0] = customerCode;
                 else
                 if(inx == '4')
                   firstEntryOnPage[0] = receiptReference;
                 else // inx == '5'
                   firstEntryOnPage[0] = arCode;

                 newFirstRecNum[0] = generalUtils.intToStr(numRecs[0] - count + 2);
                 newLastRecNum[0]  = generalUtils.intToStr(numRecs[0]);
                 break;
      case 'N' : // next page
                 if(inx == '1')
                 {
                   if(callType == 'S')
                   {
                     q = "SELECT ReceiptCode, Date, CompanyCode, TotalAmount, CompanyName, Status FROM receipt WHERE ReceiptCode LIKE '"
                       + srchStr + "%' AND ReceiptCode > '" + codeArg + "'" + companyCodeANDWHERE + " ORDER BY ReceiptCode";
                     
                     receiptCodePosn = 1; datePosn = 2; companyCodePosn = 3; companyNamePosn = 5; statusPosn = 6;
                     totalAmountPosn = 4; arCodePosn = -1; receiptReferencePosn = -1;
                   }
                   else
                   {                   
                     q = "SELECT ReceiptCode, Date, CompanyCode, TotalAmount, CompanyName, Status FROM receipt WHERE ReceiptCode > '"
                       + codeArg + "'" + companyCodeANDWHERE + " ORDER BY ReceiptCode";
                     
                     receiptCodePosn = 1; datePosn = 2; companyCodePosn = 3; companyNamePosn = 5; statusPosn = 6;
                     totalAmountPosn = 4; arCodePosn = -1; receiptReferencePosn = -1;
                   }  
                 }
                 else
                 if(inx == '2')
                 {
                   if(callType == 'G')
                   {  
                     q = "SELECT DISTINCT Date FROM receipt WHERE Date > {d '" + generalUtils.convertDateToSQLFormat(codeArg) + "'}" 
                        + "" + companyCodeANDWHERE + " ORDER BY Date";
                     
                     receiptCodePosn = -1; datePosn = 1; companyCodePosn = -1; companyNamePosn = -1; statusPosn = -1;
                     totalAmountPosn = -1; arCodePosn = -1; receiptReferencePosn = -1;
                   }
                   else
                   {
                     q = "SELECT Date, ReceiptCode, CompanyCode, TotalAmount, CompanyName, Status FROM receipt WHERE Date = {d '" 
                        + generalUtils.convertDateToSQLFormat(codeArg) + "'} AND ReceiptCode > '" + codeArg2 + "'" + companyCodeANDWHERE
                        + " ORDER BY Date, ReceiptCode";
                     
                     receiptCodePosn = 2; datePosn = 1; companyCodePosn = 3; companyNamePosn = 5; statusPosn = 6;
                     totalAmountPosn = 4; arCodePosn = -1; receiptReferencePosn = -1;
                   }
                 }  
                 else
                 if(inx == '3')
                 {  
                   if(callType == 'G')
                   {
                     q = "SELECT DISTINCT CompanyCode FROM receipt WHERE CompanyCode >= '" + codeArg + "' ORDER BY CompanyCode";
                     
                     receiptCodePosn = -1; datePosn = -1; companyCodePosn = 1; companyNamePosn = -1; statusPosn = -1;
                     totalAmountPosn = -1; arCodePosn = -1; receiptReferencePosn = -1;
                   }
                   else
                   {
                     q = "SELECT CompanyCode, ReceiptCode, Date, TotalAmount, CompanyName, Status FROM receipt WHERE "
                       + "CompanyCode = '" + codeArg + "' AND ReceiptCode > '" + codeArg2 + "' ORDER BY CompanyCode, ReceiptCode";
                     
                     receiptCodePosn = 2; datePosn = 3; companyCodePosn = 1; companyNamePosn = 5; statusPosn = 6;
                     totalAmountPosn = 4; arCodePosn = -1; receiptReferencePosn = -1;
                   }
                 }  
                 else
                 if(inx == '4')
                 {  
                   if(callType == 'G')
                   {
                     q = "SELECT DISTINCT ReceiptReference FROM receipt WHERE ReceiptReference >= '" + codeArg
                       + "' ORDER BY ReceiptReference";
                     
                     receiptCodePosn = -1; datePosn = -1; companyCodePosn = -1; companyNamePosn = -1; statusPosn = -1;
                     totalAmountPosn = -1; arCodePosn = -1; receiptReferencePosn = 1;
                   }
                   else
                   {
                     q = "SELECT ReceiptReference, ReceiptCode, Date, CompanyCode, CompanyName, Status FROM receipt WHERE "
                       + "ReceiptReference = '" + codeArg + "' AND ReceiptCode > '" + codeArg2 + "' ORDER BY ReceiptReference, ReceiptCode";
                     
                     receiptCodePosn = 2; datePosn = 3; companyCodePosn = 4; companyNamePosn = 5; statusPosn = 6;
                     totalAmountPosn = -1; arCodePosn = -1; receiptReferencePosn = 1;
                   }
                 }  
                 else // inx == '5'
                 {  
                   if(callType == 'G')
                   {
                     q = "SELECT DISTINCT ARCode FROM receipt WHERE ARCode >= '" + codeArg + "' ORDER BY ARCode";
                     
                     receiptCodePosn = -1; datePosn = -1; companyCodePosn = -1; companyNamePosn = -1; statusPosn = -1;
                     totalAmountPosn = -1; arCodePosn = 1; receiptReferencePosn = -1;
                   }
                   else
                   {
                     q = "SELECT ARCode, ReceiptCode, Date, CompanyCode, CompanyName, Status FROM receipt WHERE ARCode = '" 
                       + codeArg + "' AND ReceiptCode > '" + codeArg2 + "' ORDER BY ARCode, ReceiptCode";
                     
                     receiptCodePosn = 2; datePosn = 3; companyCodePosn = 4; companyNamePosn = 5; statusPosn = 6;
                     totalAmountPosn = -1; arCodePosn = 1; receiptReferencePosn = -1;
                   }
                 }  

                 stmt.setMaxRows(maxRows);
                 rs = stmt.executeQuery(q);
                 rsmd = rs.getMetaData();

                 while(rs.next())                  
                 {
                   receiptCode      = getValue(receiptCodePosn,          ' ', rs, rsmd);
                   date             = getValue(datePosn,                 'D', rs, rsmd);
                   customerCode     = getValue(companyCodePosn,          ' ', rs, rsmd);
                   name             = getValue(companyNamePosn,          ' ', rs, rsmd);
                   receiptReference = getValue(receiptReferencePosn, ' ', rs, rsmd);
                   arCode           = getValue(arCodePosn,               ' ', rs, rsmd);
                   totalAmount      = getValue(totalAmountPosn,          ' ', rs, rsmd);
                   status           = getValue(statusPosn,               ' ', rs, rsmd);
                                                      
                   if(inx == '1')
                     appendBodyLine(html, inx, receiptCode, date, customerCode, totalAmount, name, receiptCode, status, imagesDir);
                   else
                   if(inx == '2')
                   {
                     if(callType == 'G')
                       appendBodyLine2(html, callType, operation, imagesDir, date);
                     else appendBodyLine(html, inx, date, receiptCode, customerCode, totalAmount, name, receiptCode, status, imagesDir);
                   }   
                   else
                   if(inx == '3')
                   {
                     if(callType == 'G')
                       appendBodyLine2(html, callType, operation, imagesDir, customerCode);
                     else
                     {
                       appendBodyLine(html, inx, customerCode, receiptCode, date, totalAmount, name, receiptCode, status, imagesDir);
                     }
                   }   
                   else
                   if(inx == '4')
                   {
                     if(callType == 'G')
                       appendBodyLine2(html, callType, operation, imagesDir, receiptReference);
                     else
                     {
                       appendBodyLine(html, inx, receiptReference, receiptCode, date, customerCode, name, receiptCode, status,
                                      imagesDir);
                     }
                   }   
                   else // inx == '5'
                   {
                     if(callType == 'G')
                       appendBodyLine2(html, callType, operation, imagesDir, arCode);
                     else
                     {
                       appendBodyLine(html, inx, arCode, receiptCode, date, customerCode, name, receiptCode, status, imagesDir);
                     }
                   }   
        
                   if(count++ == 1)
                   {  
                     firstCodeOnPage[0] = receiptCode;
                    
                     if(inx == '1')
                       firstEntryOnPage[0] = receiptCode;
                     else
                     if(inx == '2')
                       firstEntryOnPage[0] = date;
                     else
                     if(inx == '3')
                       firstEntryOnPage[0] = customerCode;
                     else 
                     if(inx == '4')
                       firstEntryOnPage[0] = receiptReference;
                     else // inx == '5'
                     firstEntryOnPage[0] = arCode;
                   }
                 }
               
                 rs.close();
                               
                 lastCodeOnPage[0] = receiptCode;

                 if(inx == '1')
                   lastEntryOnPage[0] = receiptCode;
                 else
                 if(inx == '2')
                   lastEntryOnPage[0] = date;
                 else 
                 if(inx == '3')
                   lastEntryOnPage[0] = customerCode;
                 else 
                 if(inx == '4')
                   lastEntryOnPage[0] = receiptReference;
                 else // inx == '5'
                   lastEntryOnPage[0] = arCode;

                 newFirstRecNum[0] = generalUtils.intToStr(generalUtils.intFromStr(lastRecNum) + 1);
                 newLastRecNum[0]  = generalUtils.intToStr(generalUtils.intFromStr(lastRecNum) + count - 1);
                 break;
      case 'P' : // prev page
                 if(inx == '1')
                 {
                   if(callType == 'S')
                   {
                     q = "SELECT ReceiptCode, Date, CompanyCode, TotalAmount, CompanyName, Status FROM receipt WHERE ReceiptCode LIKE '"
                       + srchStr + "%' AND ReceiptCode < '" + codeArg + "'" + companyCodeANDWHERE + " ORDER BY ReceiptCode DESC";
                   }
                   else
                   {
                     q = "SELECT ReceiptCode, Date, CompanyCode, TotalAmount, CompanyName, Status FROM receipt WHERE ReceiptCode < '"
                       + codeArg + "'" + companyCodeANDWHERE + " ORDER BY ReceiptCode DESC";
                   }
                   
                   receiptCodePosn = 1; datePosn = 2; companyCodePosn = 3; companyNamePosn = 5; statusPosn = 6;
                   totalAmountPosn = 4; arCodePosn = -1; receiptReferencePosn = -1;
                 }
                 else
                 if(inx == '2')
                 {
                   if(callType == 'G')
                   {  
                     q = "SELECT DISTINCT Date FROM receipt WHERE Date < {d '" + generalUtils.convertDateToSQLFormat(codeArg) 
                       + "'}" + companyCodeANDWHERE + " ORDER BY Date DESC";
                     
                     receiptCodePosn = -1; datePosn = 1; companyCodePosn = -1; companyNamePosn = -1; statusPosn = -1;
                     totalAmountPosn = -1; arCodePosn = -1; receiptReferencePosn = -1;
                   }
                   else
                   {
                     q = "SELECT Date, ReceiptCode, CompanyCode, TotalAmount, CompanyName, Status FROM receipt WHERE Date = {d '" 
                       + generalUtils.convertDateToSQLFormat(codeArg) + "'} AND ReceiptCode < '" + codeArg2 + "'" + companyCodeANDWHERE
                       + " ORDER BY Date, ReceiptCode DESC";
                     
                     receiptCodePosn = 2; datePosn = 1; companyCodePosn = 3; companyNamePosn = 5; statusPosn = 6;
                     totalAmountPosn = 4; arCodePosn = -1; receiptReferencePosn = -1;
                   }
                 }  
                 else
                 if(inx == '3')
                 {
                   if(callType == 'G')
                   {  
                     q = "SELECT DISTINCT CompanyCode FROM receipt WHERE CompanyCode < '" + codeArg + "' ORDER BY CompanyCode DESC";
                     
                     receiptCodePosn = -1; datePosn = -1; companyCodePosn = 1; companyNamePosn = -1; statusPosn = -1;
                     totalAmountPosn = -1; arCodePosn = -1; receiptReferencePosn = -1;
                   }
                   else
                   {
                     q = "SELECT CompanyCode, ReceiptCode, Date, TotalAmount, CompanyName, Status FROM receipt WHERE "
                       + "CompanyCode = '" + codeArg + "' AND ReceiptCode < '" + codeArg2 + "' ORDER BY CompanyCode, ReceiptCode DESC";
                     
                     receiptCodePosn = 2; datePosn = 3; companyCodePosn = 1; companyNamePosn = 5; statusPosn = 6;
                     totalAmountPosn = 4; arCodePosn = -1; receiptReferencePosn = -1;
                   }  
                 }  
                 else
                 if(inx == '4')
                 {
                   if(callType == 'G')
                   {  
                     q = "SELECT DISTINCT ReceiptReference FROM receipt WHERE ReceiptReference < '" + codeArg
                       + "' ORDER BY ReceiptReference DESC";
                     
                     receiptCodePosn = -1; datePosn = -1; companyCodePosn = -1; companyNamePosn = -1; statusPosn = -1;
                     totalAmountPosn = -1; arCodePosn = -1; receiptReferencePosn = 1;
                   }
                   else
                   {
                     q = "SELECT ReceiptReference, ReceiptCode, Date, CompanyCode, CompanyName, Status FROM receipt WHERE "
                       + "ReceiptReference = '" + codeArg + "' AND ReceiptCode < '" + codeArg2
                       + "' ORDER BY ReceiptReference, ReceiptCode DESC";
                     
                     receiptCodePosn = 2; datePosn = 3; companyCodePosn = 4; companyNamePosn = 5; statusPosn = 6;
                     totalAmountPosn = -1; arCodePosn = -1; receiptReferencePosn = 1;
                   }  
                 }  
                 else // inx == '5'
                 {
                   if(callType == 'G')
                   {  
                     q = "SELECT DISTINCT ARCode FROM receipt WHERE ARCode < '" + codeArg + "' ORDER BY ARCode DESC";
                     
                     receiptCodePosn = -1; datePosn = -1; companyCodePosn = -1; companyNamePosn = -1; statusPosn = -1;
                     totalAmountPosn = -1; arCodePosn = 1; receiptReferencePosn = -1;
                   }
                   else
                   {
                     q = "SELECT ARCode, ReceiptCode, Date, CompanyCode, CompanyName, Status FROM receipt WHERE ARCode = '"
                       + codeArg + "' AND ReceiptCode < '" + codeArg2 + "' ORDER BY ARCode, ReceiptCode DESC";
                     
                     receiptCodePosn = 2; datePosn = 3; companyCodePosn = 4; companyNamePosn = 5; statusPosn = 6;
                     totalAmountPosn = -1; arCodePosn = 1; receiptReferencePosn = -1;
                   }  
                 }  
                 
                 stmt.setMaxRows(maxRows);
                 rs = stmt.executeQuery(q);
                 rsmd = rs.getMetaData();

                 while(rs.next())                  
                 {
                   receiptCode      = getValue(receiptCodePosn,          ' ', rs, rsmd);
                   date             = getValue(datePosn,                 'D', rs, rsmd);
                   customerCode     = getValue(companyCodePosn,          ' ', rs, rsmd);
                   name             = getValue(companyNamePosn,          ' ', rs, rsmd);
                   receiptReference = getValue(receiptReferencePosn, ' ', rs, rsmd);
                   arCode           = getValue(arCodePosn,               ' ', rs, rsmd);
                   totalAmount      = getValue(totalAmountPosn,          ' ', rs, rsmd);
                   status           = getValue(statusPosn,               ' ', rs, rsmd);
                                                       
                   if(inx == '1')
                     prependBodyLine(html, inx, receiptCode, date, customerCode, totalAmount, name, receiptCode, status, imagesDir);
                   else
                   if(inx == '2')
                   {
                     if(callType == 'G')
                       prependBodyLine2(html, callType, date);
                     else prependBodyLine(html, inx, date, receiptCode, customerCode, totalAmount, name, receiptCode, status, imagesDir);
                   }  
                   else
                   if(inx == '3')
                   { 
                     if(callType == 'G')
                       prependBodyLine2(html, callType, customerCode);
                     else 
                     {
                       prependBodyLine(html, inx, customerCode, receiptCode, date, totalAmount, name, receiptCode, status, imagesDir);
                     }
                   }  
                   else
                   if(inx == '4')
                   {
                     if(callType == 'G')
                       prependBodyLine2(html, callType, receiptReference);
                     else 
                     {
                       prependBodyLine(html, inx, receiptReference, receiptCode, date, customerCode, name, receiptCode, status, imagesDir);
                     }
                   }  
                   else // inx == '5'
                   {
                     if(callType == 'G')
                       prependBodyLine2(html, callType, arCode);
                     else 
                     {
                       prependBodyLine(html, inx, arCode, receiptCode, date, customerCode, name, receiptCode, status, imagesDir);
                     }
                   }  
        
                   if(count++ == 1)
                   {  
                     lastCodeOnPage[0] = receiptCode;

                     if(inx == '1')
                       lastEntryOnPage[0] = receiptCode;
                     else
                     if(inx == '2')
                       lastEntryOnPage[0] = date;
                     else
                     if(inx == '3')
                       lastEntryOnPage[0] = customerCode;
                     else
                     if(inx == '4')
                       lastEntryOnPage[0] = receiptReference;
                     else // inx == '5'
                       lastEntryOnPage[0] = arCode;
                   }
                 }
               
                 rs.close();
                               
                 firstCodeOnPage[0] = receiptCode;

                 if(inx == '1')
                   firstEntryOnPage[0] = receiptCode;
                 else
                 if(inx == '2')
                   firstEntryOnPage[0] = date;
                 else
                 if(inx == '3')
                   firstEntryOnPage[0] = customerCode;
                 else
                 if(inx == '4')
                   firstEntryOnPage[0] = receiptReference;
                 else // inx == '5'
                   firstEntryOnPage[0] = arCode;

                 newFirstRecNum[0] = generalUtils.intToStr(generalUtils.intFromStr(firstRecNum) - count   + 1);
                 newLastRecNum[0]  = generalUtils.intToStr(generalUtils.intFromStr(firstRecNum) - 1);
                 break;
    }
  
    if(stmt != null) stmt.close();
    if(con  != null) con.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void setNav(Statement stmt, ResultSet rs, PrintWriter out, boolean first, char inx, char operation, char callType, int numRecs, String firstCodeOnPage,
                      String lastCodeOnPage, String firstEntryOnPage, String lastEntryOnPage, String srchStr, String imagesDir,
                      String newFirstRecNum, String newLastRecNum, int maxRows, String unm, String sid, String uty,
                      String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] hmenuCount, int[] bytesOut) throws Exception
  {
    if(first)
    {
      String title;  
      if(callType == 'S')
      {
        title = "Receipt Search Results for ";
        switch(inx)
        {
          case '1' : title += "Receipt Code";      break;
          case '2' : title += "Receipt Date";      break;
          case '3' : title += "Customer Code";     break;
          case '4' : title += "Receipt Reference"; break;
          case '5' : title += "AR Code";           break;
        }
        title += " starting with: " + srchStr;
      }
      else 
      if(operation == 'X')
      {
        title = "Receipt Listing by ";
        switch(inx)
        {
          case '1' : title += "Receipt Code";      break;
          case '2' : title += "Receipt Date";      break;
          case '3' : title += "Customer Code";     break;
          case '4' : title += "Receipt Reference"; break;
          case '5' : title += "AR Code";           break;
        }
      }
      else title = "Receipt Listing";

      drawingUtils.drawTitle(out, false, false, "ReceiptListing", "", title, "4213", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
    }

    scoutln(out, bytesOut, "<form><table id=\"pageColumn\" border=0 cellspacing=0 cellpadding=0 width=100%>");

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
            
        scoutln(out, bytesOut, "&nbsp;<a href=\"javascript:page('F','" + callType + "','" + arg + "','','" + topOrBottom
                              + "')\">First</a>");
        scoutln(out, bytesOut, "&nbsp;&nbsp;<a href=\"javascript:page('P','" + callType + "','" + arg + "','"
                              + generalUtils.sanitise(firstCodeOnPage) + "','" + topOrBottom + "')\">Previous</a>");
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

        scoutln(out, bytesOut, "&nbsp;&nbsp;<a href=\"javascript:page('N','" + callType + "','" + arg + "','"
                               + generalUtils.sanitise(lastCodeOnPage) + "','" + topOrBottom + "')\">Next</a>");
        scoutln(out, bytesOut, "&nbsp;&nbsp;<a href=\"javascript:page('L','" + callType + "','" + arg
                             + "','','"+topOrBottom+"')\">Last</a>");
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
      scoutln(out, bytesOut, "<a href=\"javascript:page('F','S','"+arg+"','','T')\">Search</a> on ");
    else
    {
      if(numRecs > 0)
        scoutln(out, bytesOut, "<a href=\"javascript:page('F','S','','','B')\">Search</a> on ");
    }
    
    if(numRecs > 0 || first)
    {
      switch(inx)
      {
        case '1' : scout(out, bytesOut, "Receipt Code");      break;
        case '2' : scout(out, bytesOut, "Receipt Date");      break;
        case '3' : scout(out, bytesOut, "Customer Code");     break;
        case '4' : scout(out, bytesOut, "Receipt Reference"); break;
        case '5' : scout(out, bytesOut, "AR Code");           break;
      }
    }

    if(first)
    {
      scoutln(out, bytesOut, "&nbsp;<input type=text size=20 maxlength=20 value=\"" + srchStr + "\" name=srchStr1>");
      scoutln(out, bytesOut, "&nbsp;&nbsp;&nbsp;PageSize&nbsp;&nbsp;<input type=text size=4 maxlength=4 value=\"" + maxRows
                           + "\" name=maxRows1><br><img src=\"" + imagesDir + "z402.gif\">");
    }
    else 
    {
      if(numRecs > 0)
      {
        scoutln(out, bytesOut, "&nbsp;<input type=text size=20 maxlength=20 value=\"" + srchStr + "\" name=srchStr2>");
        scoutln(out, bytesOut, "&nbsp;&nbsp;PageSize&nbsp;&nbsp;<input type=text size=4 maxlength=4 value=\"" + maxRows
                             + "\" name=maxRows2></td></tr><tr><td><img src=\""+imagesDir+"z402.gif\">");
      }
    }
    
    scoutln(out, bytesOut, "</td></tr></table><table id=\"page\" border=0 width=100% cellspacing=0 cellpadding=0>");

    if(first)
      scoutln(out, bytesOut, "<tr><td><img src=\"" + imagesDir + "z402.gif\"></td></tr>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void setHead(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, char inx, String unm, String sid, String uty,
                       String men, String den, String dnm, String bnm, String newFirstRecNum, String newLastRecNum, int numRecs,
                       String companyCode, String imagesDir, String localDefnsDir, String defnsDir, int[] hmenuCount, int[] bytesOut)
                       throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Receipt Listing</title>");
    scoutln(out, bytesOut, "<script javascript=\"JavaScript\">");

    scoutln(out, bytesOut, "function fetch(code){");
    scoutln(out, bytesOut, "var code2=sanitise(code);");
    scoutln(out, bytesOut, "this.location.href=\"http://" + men + "/central/servlet/ReceiptPage?unm=" + unm + "&sid="
                           + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                           + "&p2=A&p1=\"+code2;}");

    scoutln(out, bytesOut, "function page(operation,callType,argA,argB,topOrBottom){");
    scoutln(out, bytesOut, "var srchStr='';if(topOrBottom=='T')srchStr=document.forms[0].srchStr1.value;");
    scoutln(out, bytesOut, "else srchStr=document.forms[0].srchStr2.value;");
    scoutln(out, bytesOut, "var maxRows;if(topOrBottom=='T')maxRows=document.forms[0].maxRows1.value;");
    scoutln(out, bytesOut, "else maxRows=document.forms[0].maxRows2.value;");    
    scoutln(out, bytesOut, "var arg=sanitise(argA);var arg2=sanitise(argB);");
    scoutln(out, bytesOut, "if(callType=='G'||callType=='S')this.location.replace(\"/central/servlet/ReceiptListing?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                           + "&p1=" + inx + "&p2=\"+operation+\"&p3=\"+arg+\"&p4=\"+arg2+\"&p5=\"+srchStr+\"&p6=" + newFirstRecNum
                           + "&p10=" + numRecs + "&p11=" + companyCode + "&p8=\"+callType+\"&p9=\"+maxRows+\"&p7=" + newLastRecNum
                           + "\");");
    scoutln(out, bytesOut, "else this.location.replace(\"/central/servlet/ReceiptListing?unm=" + unm + "&sid=" + sid
                         + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + inx
                         + "&p2=\"+operation+\"&p3=\"+arg+\"&p4=\"+arg2+\"&p5=\"+srchStr+\"&p6=" + newFirstRecNum + "&p10="
                         + numRecs + "&p11=" + companyCode + "&p8=\"+callType+\"&p9=\"+maxRows+\"&p7=" + newLastRecNum + "\");}");

    scoutln(out, bytesOut, "function sanitise(code){");
    scoutln(out, bytesOut, "var code2='';var x;var len=code.length;");
    scoutln(out, bytesOut, "for(x=0;x<len;++x)");
    scoutln(out, bytesOut, "if(code.charAt(x)=='#')code2+='%23';");
    scoutln(out, bytesOut, "else if(code.charAt(x)==\"'\")code2+='%27';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='\"')code2+='%22';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='&')code2+='%26';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='%')code2+='%25';");
    scoutln(out, bytesOut, "else if(code.charAt(x)==' ')code2+='%20';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='?')code2+='%3F';");
    scoutln(out, bytesOut, "else code2+=code.charAt(x);");
    scoutln(out, bytesOut, "return code2};");

    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" 
                         + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    drawingUtils.outputPageFrame(con, stmt, rs, out, req, "4213", "", "ReceiptListing", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<form>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void startBody(boolean first, PrintWriter out, char inx, String imagesDir, int[] bytesOut) throws Exception
  {
    if(first)
      scoutln(out, bytesOut, "<table id=\"page\" border=0 cellspacing=2 cellpadding=0>");
    else scoutln(out, bytesOut, "<tr><td><img src=\"" + imagesDir + "z402.gif\"></td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap><p>Status &nbsp;</td>");

    switch(inx)
    {
      case '1' : scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Receipt Code &nbsp;</td>");
                 scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Receipt Date &nbsp;</td>");
                 scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Customer Code &nbsp;</td>");
                 scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Total Amount &nbsp;</td>");
                 scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Customer Name &nbsp;</td></tr>");
                 break;
      case '2' : scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Receipt Date &nbsp;</td>");
                 scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Receipt Code &nbsp;</td>");
                 scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Customer Code</td>");
                 scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Total Amount &nbsp;</td>");
                 scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Customer Name &nbsp;</td></tr>");
                 break;
      case '3' : scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Customer Code &nbsp;</td>");
                 scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Receipt Code &nbsp;</td>");
                 scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Receipt Date &nbsp;</td>");
                 scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Total Amount &nbsp;</td>");
                 scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Customer Name</td></tr>");
                 break;
      case '4' : scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Receipt Reference &nbsp;</td>");
                 scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Receipt Code &nbsp;</td>");
                 scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Receipt Date &nbsp;</td>");
                 scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Customer Code &nbsp;</td>");
                 scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Customer Name</td></tr>");
                 break;
      case '5' : scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>AR Code &nbsp;</td>");
                 scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Receipt Code &nbsp;</td>");
                 scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Receipt Date &nbsp;</td>");
                 scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Customer Code &nbsp;</td>");
                 scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Customer Name</td></tr>");
                 break;
    }

    scoutln(out, bytesOut, "<tr><td><img src=\"" + imagesDir + "z402.gif\"></td></tr>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void startBody2(boolean first, PrintWriter out, char inx, String imagesDir, int[] bytesOut) throws Exception
  {
    if(first)
      scoutln(out, bytesOut, "<table id=\"page\" border=0 cellspacing=0 cellpadding=0><tr>");

    scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>");

    switch(inx)
    {
      case '2' : scoutln(out, bytesOut, "Receipt Date");       break;
      case '3' : scoutln(out, bytesOut, "Customer Code");      break;
      case '4' : scoutln(out, bytesOut, "Receipt Referemce");  break;
      case '5' : scoutln(out, bytesOut, "AR Code");            break;
    }

    scoutln(out, bytesOut, " &nbsp;</b></td></tr>");
    scoutln(out, bytesOut, "<tr><td><img src=\"" + imagesDir + "z402.gif\"></td></tr>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void appendBodyLine(String[] html, char inx, String one, String two, String three, String four, String five, String code,
                              String status, String imagesDir) throws Exception
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
                 html[0] += "<td nowrap align=right><p>" + four + "&nbsp;</td>";
                 html[0] += "<td nowrap><p>" + five + "&nbsp;</td></tr>\n";
                 break;
      case '2' : 
      case '3' : html[0] += "<td nowrap><p>" + one + "&nbsp;</td>";
                 html[0] += "<td nowrap><a href=\"javascript:fetch('" + s + "')\">" + two + "</a>&nbsp;</td>";
                 html[0] += "<td nowrap><p>" + three + "&nbsp;</td>";
                 html[0] += "<td nowrap align=right><p>" + four + "&nbsp;</td>";
                 html[0] += "<td nowrap><p>" + five + "&nbsp;</td></tr>\n";
                 break;
      case '4' : 
      case '5' : html[0] += "<td nowrap><p>" + one + "&nbsp;</td>";
                 html[0] += "<td nowrap><a href=\"javascript:fetch('" + s + "')\">" + two + "</a>&nbsp;</td>";
                 html[0] += "<td nowrap><p>" + three + "&nbsp;</td>";
                 html[0] += "<td nowrap><p>" + four + "&nbsp;</td>";
                 html[0] += "<td nowrap><p>" + five + "&nbsp;</td></tr>\n";
                 break;
    }
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------
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

  // -------------------------------------------------------------------------------------------------------------------------------
  private void prependBodyLine(String[] html, char inx, String one, String two, String three, String four, String five, String code,
                               String status, String imagesDir) throws Exception
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
      case '2' : 
      case '3' : line += "<td nowrap><p>" + one + "&nbsp;</td>";
                 line += "<td nowrap><a href=\"javascript:fetch('" + s + "')\">" + two + "</a>&nbsp;</td>";
                 line += "<td nowrap><p>" + three + "&nbsp;</td>";
                 line += "<td nowrap align=right><p>" + four + "&nbsp;</td>";
                 line += "<td nowrap><p>" + five + "&nbsp;</td>";
                 html[0] = line + "</tr>\n" + html[0];
                 break;
      case '4' :
      case '5' : line += "<td nowrap><p>" + one + "&nbsp;</td>";
                 line += "<td nowrap><a href=\"javascript:fetch('" + s + "')\">" + two + "</a>&nbsp;</td>";
                 line += "<td nowrap><p>" + three + "&nbsp;</td>";
                 line += "<td nowrap><p>" + four + "&nbsp;</td>";
                 line += "<td nowrap><p>" + five + "&nbsp;</td>";
                 html[0] = line + "</tr>\n" + html[0];
                 break;
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------
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

  // -------------------------------------------------------------------------------------------------------------------------------
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
  
  // -------------------------------------------------------------------------------------------------------------------------------
  public String getValue(int colNum, char type, ResultSet rs, ResultSetMetaData rsmd)
  {
    if(colNum < 0)
      return "";
    
    try
    {
      Integer f;
      Double dd;
      java.sql.Date d;
      java.sql.Timestamp ts;
      Time t;

      String str="";

      switch(rsmd.getColumnType(colNum))
      {
        case java.sql.Types.CHAR    : str = rs.getString(colNum);
                                      str = generalUtils.stripTrailingSpacesStr(str);
                                      break;
        case java.sql.Types.INTEGER : f = rs.getInt(colNum);
                                      str = f.toString();
                                      break;
        case java.sql.Types.DECIMAL : dd = rs.getDouble(colNum);
                                      str = generalUtils.doubleDPs('2', dd);
                                   //   str = dd.toString();
                                      break;
        case 91                     : if(type == 'D')
                                      {
                                        d = rs.getDate(colNum);
                                        str = generalUtils.convertFromYYYYMMDD(d.toString());
                                      }  
                                      else 
                                      {
                                        t = rs.getTime(colNum);
                                        str = t.toString();
                                      }  
                                      break;
        case 93                     : ts = rs.getTimestamp(colNum);
                                      str = ts.toString();
                                      str = generalUtils.convertFromTimestamp(str);
                                      break;
        case -1                     : str = rs.getString(colNum);
                                      str = generalUtils.stripTrailingSpacesStr(str);
                                      break;
      }

      return str;
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }

    return "";
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void scout(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.print(str);
    bytesOut[0] += str.length();    
  }
  // -------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
