// =======================================================================================================================================================================================================
// System: ZaraStar: Product: create listing page
// Module: CatalogListingPage.java
// Author: C.K.Harvey
// Copyright (c) 2001-08 Christopher Harvey. All Rights Reserved.
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
import java.io.*;
import java.sql.*;

public class CatalogListingPage extends HttpServlet
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
    PrintWriter out = null;
    int[] bytesOut = new int[1];
    bytesOut[0] = 0;

    String unm = "", sid = "", uty = "", men = "", den = "", dnm = "", bnm = "", inx = "", codeArg = "", codeArg2 = "", srchStr = "",
           operation = "", firstRecNum = "", lastRecNum = "", callType = "", maxRows = "", numRecs = "";

    try
    {
      req.setCharacterEncoding("UTF-8");
      directoryUtils.setContentHeaders(res);
      out = res.getWriter();

      unm = req.getParameter("unm");
      sid = req.getParameter("sid");
      uty = req.getParameter("uty");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");
      inx = req.getParameter("p1");
      operation = req.getParameter("p2");

      codeArg = req.getParameter("p3");
      if(codeArg == null) codeArg = "";
      codeArg = new String(codeArg.getBytes("ISO-8859-1"), "UTF-8");

      codeArg2 = req.getParameter("p4");
      if(codeArg2 == null) codeArg2 = "";
      codeArg2 = new String(codeArg2.getBytes("ISO-8859-1"), "UTF-8");

      srchStr = req.getParameter("p5");
      if(srchStr == null) srchStr = "";
      srchStr = new String(srchStr.getBytes("ISO-8859-1"), "UTF-8");

      firstRecNum = req.getParameter("p6");
      lastRecNum = req.getParameter("p7");

      callType = req.getParameter("p8"); // 'G'roup (e.g., list of salespeople, or 'D'etail)
      if(callType == null || callType.length() == 0) callType = "G";

      maxRows = req.getParameter("p9");
      if(maxRows == null || maxRows.length() == 0) maxRows = "50";

      numRecs = req.getParameter("p10");
      if(numRecs == null || numRecs.length() == 0) numRecs = "-1";

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, inx, operation, callType, codeArg, codeArg2, srchStr, firstRecNum, lastRecNum,
           generalUtils.strToInt(maxRows), generalUtils.strToInt(numRecs), bytesOut);
    }
    catch (Exception e)
    {
      StringBuffer url = req.getRequestURL();
      int x = 0;
      if(url.charAt(x) == 'h' || url.charAt(x) == 'H') x += 7;
      String urlBit = "";
      while (url.charAt(x) != '\000' && url.charAt(x) != ',' && url.charAt(x) != '/')
        urlBit += url.charAt(x++);

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "CatalogListingPage", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2006, bytesOut[0], 0, "ERR:" + codeArg);
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm,
                    String bnm, String inx, String operation, String callType, String codeArg,  String codeArg2, String srchStr, String firstRecNum,
                    String lastRecNum, int maxRows, int numRecs, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir = directoryUtils.getSupportDirs('I');

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;
    
    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 3001, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "CatalogListingPage", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2005, bytesOut[0], 0, "ACC:" + codeArg);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(srchStr == null || srchStr.equalsIgnoreCase("null")) srchStr = "";

    generate(con, stmt, rs, out, req, unm, sid, uty, men, den, dnm, bnm, inx.charAt(0), operation.charAt(0), callType.charAt(0), codeArg, codeArg2, srchStr,
             firstRecNum, lastRecNum, maxRows, numRecs, imagesDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 2006, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), codeArg);
      if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void generate(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm,
                        String bnm, char inx, char operation, char callType, String codeArg, String codeArg2, String srchStr, String firstRecNum,
                        String lastRecNum, int maxRows, int numRecsIn, String imagesDir, String localDefnsDir, String defnsDir, int[] bytesOut)
                        throws Exception
  {
    int[] numRecs = new int[1];
    numRecs[0] = numRecsIn;

    String[] firstCodeOnPage = new String[1];
    String[] lastCodeOnPage = new String[1];
    String[] firstEntryOnPage = new String[1];
    String[] lastEntryOnPage = new String[1];
    String[] newFirstRecNum = new String[1];
    String[] newLastRecNum = new String[1];

    char operation2;
    if(operation == 'X') // first time in
      operation2 = 'F';
    else operation2 = operation;

    String[] html = new String[1];
    html[0] = "";
    createPage(con, stmt, rs, html, operation2, callType, codeArg, codeArg2, inx, srchStr, numRecs, firstCodeOnPage, lastCodeOnPage, firstEntryOnPage,
               lastEntryOnPage, firstRecNum, lastRecNum, newFirstRecNum, newLastRecNum, maxRows, imagesDir, dnm, localDefnsDir, defnsDir);

    if(generalUtils.strToInt(newFirstRecNum[0]) <= 0) // prev call has gone back before rec 1
    {
      newFirstRecNum[0] = "1";
      newLastRecNum[0] = "50";
    }

    setHead(con, stmt, rs, out, req, inx, unm, sid, uty, men, den, dnm, bnm, newFirstRecNum[0], newLastRecNum[0], numRecs[0], localDefnsDir, defnsDir,
            bytesOut);

    setNav(out, true, inx, operation, callType, numRecs[0], firstCodeOnPage[0], lastCodeOnPage[0], firstEntryOnPage[0], lastEntryOnPage[0], srchStr,
           imagesDir, newFirstRecNum[0], newLastRecNum[0], maxRows, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    if(numRecs[0] > 0)
    {
      if(callType != 'G' || inx == '1') // || operation == 'S')
        startBody(true, out, inx, imagesDir, bytesOut);
      else startBody2(true, out, inx, imagesDir, bytesOut);
    }

    scoutln(out, bytesOut, html[0]);

    if(numRecs[0] > 0)
    {
      if(callType != 'G' || inx == '1') // || operation == 'S')
        startBody(false, out, inx, imagesDir, bytesOut);
      else startBody2(false, out, inx, imagesDir, bytesOut);
    }

    setNav(out, false, inx, operation, callType, numRecs[0], firstCodeOnPage[0], lastCodeOnPage[0], firstEntryOnPage[0], lastEntryOnPage[0], srchStr,
           imagesDir, newFirstRecNum[0], newLastRecNum[0], maxRows, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    scoutln(out, bytesOut, "</td></tr><tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "</table></form>");     scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void createPage(Connection con, Statement stmt, ResultSet rs, String[] html, char operation, char callType, String arg, String arg2, char inx, String srchStr, int[] numRecs,
                          String[] firstCodeOnPage, String[] lastCodeOnPage, String[] firstEntryOnPage, String[] lastEntryOnPage, String firstRecNum,
                          String lastRecNum, String[] newFirstRecNum, String[] newLastRecNum, int maxRows, String imagesDir, String dnm,
                          String localDefnsDir, String defnsDir) throws Exception
  {
    stmt = con.createStatement();
    String q;
    int count = 1;

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

    if(codeArg.equals("<none>")) codeArg = "";

    // determine num of recs if not already known, or if doing a search and hence must re-determine
    if(numRecs[0] == -1 || operation == 'F')
    {
      ResultSet r;
      switch (inx)
      {
        case '2': if(callType == 'S')
                    r = stmt.executeQuery("SELECT COUNT(Description) AS rowcount FROM stock WHERE Description LIKE '%" + srchStr + "%'");
                  else
                  if(callType == 'G')
                    r = stmt.executeQuery("SELECT COUNT(DISTINCT Description) AS rowcount FROM stock");
                  else r = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM stock WHERE Description = '" + codeArg + "'");
                  break;
        case '3': if(callType == 'S')
                  {
                    r = stmt.executeQuery("SELECT COUNT(Manufacturer) AS rowcount FROM stock WHERE Manufacturer LIKE '" + srchStr + "%'");
                  }
                  else
                  if(callType == 'G')
                    r = stmt.executeQuery("SELECT COUNT(DISTINCT Manufacturer) AS rowcount FROM stock");
                  else r = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM stock WHERE Manufacturer = '" + codeArg + "'");
                  break;
        case '4': if(callType == 'S')
                    r = stmt.executeQuery("SELECT COUNT(ManufacturerCode) AS rowcount FROM stock WHERE Manufacturer LIKE '" + srchStr + "%'");
                  else
                  if(callType == 'G')
                    r = stmt.executeQuery("SELECT COUNT(DISTINCT ManufacturerCode) AS rowcount FROM stock");
                  else r = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM stock WHERE ManufacturerCode = '" + codeArg + "'");
                  break;
        case '5': if(callType == 'S')
                    r = stmt.executeQuery("SELECT COUNT(AltItemCode1) AS rowcount FROM stock WHERE AltItemCode1 LIKE '" + srchStr + "%'");
                  else
                  if(callType == 'G')
                    r = stmt.executeQuery("SELECT COUNT(DISTINCT AltItemCode1) AS rowcount FROM stock");
                  else r = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM stock WHERE AltItemCode1 = '" + codeArg + "'");
                  break;
        case '6': if(callType == 'S')
                    r = stmt.executeQuery("SELECT COUNT(AltItemCode2) AS rowcount FROM stock WHERE AltItemCode2 LIKE '" + srchStr + "%'");
                  else
                  if(callType == 'G')
                    r = stmt.executeQuery("SELECT COUNT(DISTINCT AltItemCode2) AS rowcount FROM stock");
                  else r = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM stock WHERE AltItemCode2 = '" + codeArg + "'");
                  break;
        case '7': if(callType == 'S')
                    r = stmt.executeQuery("SELECT COUNT(AltItemCode3) AS rowcount FROM stock WHERE AltItemCode3 LIKE '" + srchStr + "%'");
                  else
                  if(callType == 'G')
                    r = stmt.executeQuery("SELECT COUNT(DISTINCT AltItemCode3) AS rowcount FROM stock");
                  else r = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM stock WHERE AltItemCode3 = '" + codeArg + "'");
                  break;
        case '8': if(callType == 'S')
                    r = stmt.executeQuery("SELECT COUNT(AltItemCode4) AS rowcount FROM stock WHERE AltItemCode4 LIKE '" + srchStr + "%'");
                  else
                  if(callType == 'G')
                    r = stmt.executeQuery("SELECT COUNT(DISTINCT AltItemCode4) AS rowcount FROM stock");
                  else r = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM stock WHERE AltItemCode4 = '" + codeArg + "'");
                  break;
        default: // case '1'
                  if(callType == 'S')
                    r = stmt.executeQuery("SELECT COUNT(DISTINCT ItemCode) AS rowcount FROM stock WHERE ItemCode LIKE '" + generalUtils.sanitise(srchStr) + "%'");
                  else
                  if(operation == 'N' && callType == '2')
                  {
                    r = stmt.executeQuery("SELECT COUNT(DISTINCT ItemCode) AS rowcount FROM stock WHERE ItemCode LIKE '" + (srchStr) + "%'"); // always calc the total number even though are on subsequent page
                  }
                  else r = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM stock");
                  break;
      }
      
      r.next();
      numRecs[0] = r.getInt("rowcount");
      r.close();
    }

    ResultSetMetaData rsmd = null;

    String itemCode = "", desc = "", mfr = "", mfrCode = "", altItemCode1 = "", altItemCode2 = "", altItemCode3 = "", altItemCode4 = "", status = "",
           categoryCode="", showToWeb="";
    int itemCodePosn, descPosn, mfrPosn, mfrCodePosn, altItemCode1Posn, altItemCode2Posn, altItemCode3Posn, altItemCode4Posn, statusPosn,
        categoryCodePosn, showToWebPosn;

    switch(operation)
    {
      case 'F': // first page
                if(inx == '1')
                {
                  if(callType == 'S')
                  {
                    q = "SELECT ItemCode, Description, Manufacturer, ManufacturerCode, Status, CategoryCode, ShowToWeb FROM stock WHERE ItemCode"
                      + " LIKE '" + srchStr + "%' ORDER BY ItemCode";
                  }
                  else q = "SELECT ItemCode, Description, Manufacturer, ManufacturerCode, Status, CategoryCode, ShowToWeb FROM stock ORDER BY ItemCode";

                  itemCodePosn = 1; descPosn = 2; mfrPosn = 3; mfrCodePosn = 4; statusPosn = 5; altItemCode1Posn = -1; altItemCode2Posn = -1;
                  altItemCode3Posn = -1; altItemCode4Posn = -1; categoryCodePosn = 6; showToWebPosn = 7;
                }
                else  
                if(inx == '2')
                {
                  if(callType == 'S')
                  {
                    q = "SELECT Description, ItemCode, Manufacturer, ManufacturerCode, Status, ShowToWeb, CategoryCode FROM stock WHERE Description "
                      + "LIKE '%" + srchStr + "%' ORDER BY Description";
                    
                    itemCodePosn = 2; descPosn = 1; mfrPosn = 3; mfrCodePosn = 4; statusPosn = 5; altItemCode1Posn = -1; altItemCode2Posn = -1;
                    altItemCode3Posn = -1; altItemCode4Posn = -1; categoryCodePosn = 7; showToWebPosn = 6;
                  }
                  else
                  if(callType == 'G')
                  {
                    q = "SELECT DISTINCT Description FROM stock ORDER BY Description";
                    
                    itemCodePosn = -1; descPosn = 1; mfrPosn = -1; mfrCodePosn = -1; statusPosn = -1; altItemCode1Posn = -1; altItemCode2Posn = -1;
                    altItemCode3Posn = -1; altItemCode4Posn = -1; categoryCodePosn = -1; showToWebPosn = -1;
                  }
                  else
                  {
                    q = "SELECT Description, ItemCode, Manufacturer, ManufacturerCode, Status, ShowToWeb, CategoryCode FROM stock WHERE Description = '"
                      + codeArg + "' ORDER BY Description";
                    
                    itemCodePosn = 2; descPosn = 1; mfrPosn = 3; mfrCodePosn = 4; statusPosn = 5; altItemCode1Posn = -1; altItemCode2Posn = -1;
                    altItemCode3Posn = -1; altItemCode4Posn = -1; categoryCodePosn = 7; showToWebPosn = 6;
                  }
                }
                else
                if(inx == '3')
                {
                  if(callType == 'S')
                  {
                    q = "SELECT Manufacturer, ManufacturerCode, ItemCode, Description, Status, ShowToWeb, CategoryCode FROM stock "
                      + "WHERE Manufacturer LIKE '" + srchStr + "%' ORDER BY Manufacturer, ManufacturerCode";
  
                    itemCodePosn = 3; descPosn = 4; mfrPosn = 1; mfrCodePosn = 2; statusPosn = 5; altItemCode1Posn = -1; altItemCode2Posn = -1;
                    altItemCode3Posn = -1; altItemCode4Posn = -1; categoryCodePosn = 7; showToWebPosn = 6;
                  }
                  else
                  if(callType == 'G')
                  {
                    q = "SELECT DISTINCT Manufacturer FROM stock ORDER BY Manufacturer";
            
                    itemCodePosn = -1; descPosn = -1; mfrPosn = 1; mfrCodePosn = -1; statusPosn = -1; altItemCode1Posn = -1; altItemCode2Posn = -1;
                    altItemCode3Posn = -1; altItemCode4Posn = -1; categoryCodePosn = -1; showToWebPosn = -1;
                  }
                  else
                  { 
                    q = "SELECT Manufacturer, ManufacturerCode, ItemCode, Description, Status, ShowToWeb, CategoryCode FROM stock WHERE Manufacturer = '"
                      + codeArg + "' ORDER BY Manufacturer, ManufacturerCode";
       
                    itemCodePosn = 3; descPosn = 4; mfrPosn = 1; mfrCodePosn = 2; statusPosn = 5; altItemCode1Posn = -1; altItemCode2Posn = -1;
                    altItemCode3Posn = -1; altItemCode4Posn = -1; categoryCodePosn = 7; showToWebPosn = 6;
                  }
                }
                else
                if(inx == '4')
                {
                  if(callType == 'S')
                  {
                    q = "SELECT ManufacturerCode, Manufacturer, ItemCode, Description, Status, ShowToWeb, CategoryCode FROM stock WHERE ManufacturerCode LIKE '"
                      + srchStr + "%' ORDER BY ManufacturerCode, Manufacturer";
                    
                    itemCodePosn = 3; descPosn = 4; mfrPosn = 2; mfrCodePosn = 1; statusPosn = 5; altItemCode1Posn = -1; altItemCode2Posn = -1;
                    altItemCode3Posn = -1; altItemCode4Posn = -1; categoryCodePosn = 7; showToWebPosn = 6;
                  }
                  else
                  if(callType == 'G')
                  {
                    q = "SELECT DISTINCT ManufacturerCode FROM stock ORDER BY ManufacturerCode";
                    
                    itemCodePosn = -1; descPosn = -1; mfrPosn = -1; mfrCodePosn = 1; statusPosn = -1; altItemCode1Posn = -1; altItemCode2Posn = -1;
                    altItemCode3Posn = -1; altItemCode4Posn = -1;  categoryCodePosn = -1; showToWebPosn = -1;
                  }
                  else
                  {
                    q = "SELECT ManufacturerCode, Manufacturer, ItemCode, Description, Status, ShowToWeb, CategoryCode FROM stock WHERE ManufacturerCode " + "= '" + codeArg
                      + "' ORDER BY ManufacturerCode";
                  
                    itemCodePosn = 3; descPosn = 4; mfrPosn = 2; mfrCodePosn = 1; statusPosn = 5; altItemCode1Posn = -1; altItemCode2Posn = -1;
                    altItemCode3Posn = -1; altItemCode4Posn = -1; categoryCodePosn = 7; showToWebPosn = 6;
                  }
                }
                else
                if(inx == '5')
                {
                  if(callType == 'S')
                  {
                    q = "SELECT AltItemCode1, ItemCode, Manufacturer, ManufacturerCode, Description, Status, ShowToWeb, CategoryCode FROM stock WHERE AltItemCode1 LIKE '"
                      + srchStr + "%' ORDER BY AltItemCode1";
                    
                    itemCodePosn = 2; descPosn = 5; mfrPosn = 3; mfrCodePosn = 4; statusPosn = 6; altItemCode1Posn = 1;
                    altItemCode2Posn = -1; altItemCode3Posn = -1; altItemCode4Posn = -1; categoryCodePosn = 8; showToWebPosn = 7;
                  }
                  else
                  if(callType == 'G')
                  {
                    q = "SELECT DISTINCT AltItemCode1 FROM stock ORDER BY AltItemCode1";

                    itemCodePosn = -1; descPosn = -1; mfrPosn = -1; mfrCodePosn = -1; statusPosn = -1; altItemCode1Posn = 1; altItemCode2Posn = -1;
                    altItemCode3Posn = -1; altItemCode4Posn = -1; categoryCodePosn = -1; showToWebPosn = -1;
                  }
                  else
                  {
                    q = "SELECT AltItemCode1, ItemCode, Manufacturer, ManufacturerCode, Description, Status, ShowToWeb, CategoryCode FROM stock WHERE AltItemCode1 = '"
                      + codeArg + "' ORDER BY AltItemCode1";

                    itemCodePosn = 2; descPosn = 5; mfrPosn = 3; mfrCodePosn = 4; statusPosn = 6; altItemCode1Posn = 1; altItemCode2Posn = -1;
                    altItemCode3Posn = -1; altItemCode4Posn = -1; categoryCodePosn = 8; showToWebPosn = 7;
                  }
                }
                else
                if(inx == '6')
                {
                  if(callType == 'S')
                  {
                    q = "SELECT AltItemCode2, ItemCode, Manufacturer, ManufacturerCode, Description, Status, ShowToWeb, CategoryCode FROM stock WHERE ltItemCode2 LIKE '"
                      + srchStr + "%' ORDER BY AltItemCode2";
 
                    itemCodePosn = 2; descPosn = 5; mfrPosn = 3; mfrCodePosn = 4; statusPosn = 6; altItemCode1Posn = -1; altItemCode2Posn = 1;
                    altItemCode3Posn = -1; altItemCode4Posn = -1; categoryCodePosn = 8; showToWebPosn = 7;
                  }
                  else
                  if(callType == 'G')
                  {
                    q = "SELECT DISTINCT AltItemCode2 FROM stock ORDER BY AltItemCode2";
                  
                    itemCodePosn = -1; descPosn = -1; mfrPosn = -1; mfrCodePosn = -1; statusPosn = -1; altItemCode1Posn = -1; altItemCode2Posn = 1;
                    altItemCode3Posn = -1; altItemCode4Posn = -1; categoryCodePosn = -1; showToWebPosn = -1;
                  }
                  else
                  {
                    q = "SELECT AltItemCode2, ItemCode, Manufacturer, ManufacturerCode, Description, Status, ShowToWeb, CategoryCode FROM stock WHERE AltItemCode2 = '"
                      + codeArg + "' ORDER BY AltItemCode2";
                  
                    itemCodePosn = 2; descPosn = 5; mfrPosn = 3; mfrCodePosn = 4; statusPosn = 6; altItemCode1Posn = -1; altItemCode2Posn = 1;
                    altItemCode3Posn = -1; altItemCode4Posn = -1; categoryCodePosn = 8; showToWebPosn = 7;
                  }
                }
                else
                if(inx == '7')
                {
                  if(callType == 'S')
                  {
                    q = "SELECT AltItemCode3, ItemCode, Manufacturer, ManufacturerCode, Description, Status, ShowToWeb, CategoryCode FROM stock WHERE AltItemCode3 LIKE '"
                      + srchStr + "%' ORDER BY AltItemCode3";
                
                    itemCodePosn = 2; descPosn = 5; mfrPosn = 3; mfrCodePosn = 4; statusPosn = 6; altItemCode1Posn = -1; altItemCode2Posn = -1;
                    altItemCode3Posn = 1; altItemCode4Posn = -1; categoryCodePosn = 8; showToWebPosn = 7;
                  }
                  else
                  if(callType == 'G')
                  {
                    q = "SELECT DISTINCT AltItemCode3 FROM stock ORDER BY AltItemCode3";
                  
                    itemCodePosn = -1; descPosn = -1; mfrPosn = -1; mfrCodePosn = -1; statusPosn = -1; altItemCode1Posn = -1; altItemCode2Posn = -1;
                    altItemCode3Posn = 1; altItemCode4Posn = -1; categoryCodePosn = -1; showToWebPosn = -1;
                  }
                  else
                  {
                    q = "SELECT AltItemCode3, ItemCode, Manufacturer, ManufacturerCode, Description, Status, ShowToWeb, CategoryCode FROM stock WHERE AltItemCode3 = '"
                      + codeArg + "' ORDER BY AltItemCode3";
                  
                    itemCodePosn = 2; descPosn = 5; mfrPosn = 3; mfrCodePosn = 4; statusPosn = 6; altItemCode1Posn = -1; altItemCode2Posn = -1;
                    altItemCode3Posn = 1; altItemCode4Posn = -1; categoryCodePosn = 8; showToWebPosn = 7;
                  }
                }
                else // inx == '8'
                {
                  if(callType == 'S')
                  {
                    q = "SELECT AltItemCode4, ItemCode, Manufacturer, ManufacturerCode, Description, Status, ShowToWeb, CategoryCode FROM stock WHERE AltItemCode4 LIKE '"
                      + srchStr + "%' ORDER BY AltItemCode4";
                
                    itemCodePosn = 2; descPosn = 5; mfrPosn = 3; mfrCodePosn = 4; statusPosn = 6; altItemCode1Posn = -1; altItemCode2Posn = -1;
                    altItemCode3Posn = -1; altItemCode4Posn = 1; categoryCodePosn = 8; showToWebPosn = 7;
                  }
                  else
                  if(callType == 'G')
                  {
                    q = "SELECT DISTINCT AltItemCode4 FROM stock ORDER BY AltItemCode4";
                  
                    itemCodePosn = -1; descPosn = -1; mfrPosn = -1; mfrCodePosn = -1; statusPosn = -1; altItemCode1Posn = -1; altItemCode2Posn = -1;
                    altItemCode3Posn = -1; altItemCode4Posn = 1; categoryCodePosn = -1; showToWebPosn = -1;
                  }
                  else
                  {
                    q = "SELECT AltItemCode4, ItemCode, Manufacturer, ManufacturerCode, Description, Status, ShowToWeb, CategoryCode FROM stock WHERE AltItemCode4 = '"
                      + codeArg + "' ORDER BY AltItemCode4";
                  
                    itemCodePosn = 2; descPosn = 5; mfrPosn = 3; mfrCodePosn = 4; statusPosn = 6; altItemCode1Posn = -1; altItemCode2Posn = -1;
                    altItemCode3Posn = -1; altItemCode4Posn = 1; categoryCodePosn = 8; showToWebPosn = 7;
                  }
                }

                stmt.setMaxRows(maxRows);
                rs = stmt.executeQuery(q);
                rsmd = rs.getMetaData();

                while (rs.next())
                {
                  itemCode     = getValue(itemCodePosn,     ' ', rs, rsmd);
                  desc         = getValue(descPosn,         ' ', rs, rsmd);
                  mfr          = getValue(mfrPosn,          ' ', rs, rsmd);
                  mfrCode      = getValue(mfrCodePosn,      ' ', rs, rsmd);
                  altItemCode1 = getValue(altItemCode1Posn, ' ', rs, rsmd);
                  altItemCode2 = getValue(altItemCode2Posn, ' ', rs, rsmd);
                  altItemCode3 = getValue(altItemCode3Posn, ' ', rs, rsmd);
                  altItemCode4 = getValue(altItemCode4Posn, ' ', rs, rsmd);
                  status       = getValue(statusPosn,       ' ', rs, rsmd);
                  categoryCode = getValue(categoryCodePosn, ' ', rs, rsmd);
                  showToWeb    = getValue(showToWebPosn,    ' ', rs, rsmd);

                  if(inx == '1')
                    appendBodyLine(html, inx, itemCode, desc, mfr, mfrCode, "", categoryCode, itemCode, status, showToWeb, imagesDir);
                  else
                  if(inx == '2')
                  {
                    if(callType == 'G')
                      appendBodyLine2(html, callType, operation, desc);
                    else appendBodyLine(html, inx, desc, itemCode, mfr, mfrCode, "", categoryCode, itemCode, status, showToWeb, imagesDir);
                  }
                  else
                  if(inx == '3')
                  {
                    if(callType == 'G')
                      appendBodyLine2(html, callType, operation, mfr);
                    else appendBodyLine(html, inx, mfr, mfrCode, itemCode, desc, "", categoryCode, itemCode, status, showToWeb, imagesDir);
                  }
                  else
                  if(inx == '4')
                  {
                    if(callType == 'G')
                      appendBodyLine2(html, callType, operation, mfrCode);
                    else appendBodyLine(html, inx, mfrCode, mfr, itemCode, desc, "", categoryCode, itemCode, status, showToWeb, imagesDir);
                  }
                  else
                  if(inx == '5')
                  {
                    if(callType == 'G')
                      appendBodyLine2(html, callType, operation, altItemCode1);
                    else appendBodyLine(html, inx, altItemCode1, itemCode, mfr, mfrCode, desc, categoryCode, itemCode, status, showToWeb, imagesDir);
                  }
                  else
                  if(inx == '6')
                  {
                    if(callType == 'G')
                      appendBodyLine2(html, callType, operation, altItemCode2);
                    else appendBodyLine(html, inx, altItemCode2, itemCode, mfr, mfrCode, desc, categoryCode, itemCode, status, showToWeb, imagesDir);
                  }
                  else
                  if(inx == '7')
                  {
                    if(callType == 'G')
                      appendBodyLine2(html, callType, operation, altItemCode3);
                    else appendBodyLine(html, inx, altItemCode3, itemCode, mfr, mfrCode, desc, categoryCode, itemCode, status, showToWeb, imagesDir);
                  }
                  else
                  if(inx == '8')
                  {
                    if(callType == 'G')
                      appendBodyLine2(html, callType, operation, altItemCode4);
                    else appendBodyLine(html, inx, altItemCode4, itemCode, mfr, mfrCode, desc, categoryCode, itemCode, status, showToWeb, imagesDir);
                  }

                  if(count++ == 1)
                  {
                    firstCodeOnPage[0] = itemCode;

                    if(inx == '1')
                      firstEntryOnPage[0] = itemCode;
                    else
                    if(inx == '2')
                      firstEntryOnPage[0] = desc;
                    else
                    if(inx == '3')
                      firstEntryOnPage[0] = mfr;
                    else
                    if(inx == '4')
                      firstEntryOnPage[0] = mfrCode;
                    else
                    if(inx == '5')
                      firstEntryOnPage[0] = altItemCode1;
                    else
                    if(inx == '6')
                      firstEntryOnPage[0] = altItemCode2;
                    else
                    if(inx == '7')
                      firstEntryOnPage[0] = altItemCode3;
                    else // inx == '8'
                      firstEntryOnPage[0] = altItemCode4;
                  }
                }

                rs.close();

                lastCodeOnPage[0] = itemCode;

                if(inx == '1')
                  lastEntryOnPage[0] = itemCode;
                else
                if(inx == '2')
                  lastEntryOnPage[0] = desc;
                else
                if(inx == '3')
                  lastEntryOnPage[0] = mfr;
                else
                if(inx == '4')
                  lastEntryOnPage[0] = mfrCode;
                else
                if(inx == '5')
                  lastEntryOnPage[0] = altItemCode1;
                else
                if(inx == '6')
                  lastEntryOnPage[0] = altItemCode2;
                else
                if(inx == '7')
                  lastEntryOnPage[0] = altItemCode3;
                else // inx == '8'
                  lastEntryOnPage[0] = altItemCode4;

                newFirstRecNum[0] = "1";
                newLastRecNum[0] = generalUtils.intToStr(count - 1);
                break;
      case 'L': // last page
                if(inx == '1')
                {
                  if(callType == 'S')
                  {
                    q = "SELECT ItemCode, Description, Manufacturer, ManufacturerCode, Status, CategoryCode, ShowToWeb FROM stock WHERE ItemCode" + " LIKE '"
                      + srchStr + "%' AND ItemCode > '" + codeArg + "' ORDER BY ItemCode DESC";
                  }
                  else
                  {
                    q = "SELECT ItemCode, Description, Manufacturer, ManufacturerCode, Status, CategoryCode, ShowToWeb FROM stock WHERE ItemCode <= 'ZZZZZZZZZZZZZZZZZZZZ' ORDER BY ItemCode DESC";
                  }

                  itemCodePosn = 1; descPosn = 2; mfrPosn = 3; mfrCodePosn = 4; statusPosn = 5; altItemCode1Posn = -1;
                  altItemCode2Posn = -1; altItemCode3Posn = -1; altItemCode4Posn = -1; categoryCodePosn = 6; showToWebPosn = 7;
               }
               else
               if(inx == '2')
               {
                 if(callType == 'G')
                 {
                   q = "SELECT DISTINCT Description FROM stock WHERE Description <= 'zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz' "
                     + " ORDER BY Description DESC";
                   
                   itemCodePosn = -1; descPosn = 1; mfrPosn = -1; mfrCodePosn = -1; statusPosn = -1; altItemCode1Posn = -1;
                   altItemCode2Posn = -1; altItemCode3Posn = -1; altItemCode4Posn = -1; categoryCodePosn = -1; showToWebPosn = -1;
                 }
                 else
                 {
                   q = "SELECT Description, ItemCode, Manufacturer, ManufacturerCode, Status, ShowToWeb, CategoryCode FROM stock WHERE Description = '"
                     + codeArg + "' ORDER BY Description DESC";
              
                   itemCodePosn = 2; descPosn = 1; mfrPosn = 3; mfrCodePosn = 4; statusPosn = 5; altItemCode1Posn = -1; altItemCode2Posn = -1;
                   altItemCode3Posn = -1; altItemCode4Posn = -1; categoryCodePosn = 7; showToWebPosn = 6;
            }
          }
          else
            if(inx == '3')
            {
              if(callType == 'G')
              {
                q = "SELECT DISTINCT Manufacturer FROM stock WHERE Manufacturer <= 'zzzzzzzzzzzzzzzzzzzzzzzzzzzzzz' ORDER BY Manufacturer DESC";
                
                itemCodePosn = -1; descPosn = -1; mfrPosn = 1; mfrCodePosn = -1; statusPosn = -1; altItemCode1Posn = -1; altItemCode2Posn = -1;
                altItemCode3Posn = -1; altItemCode4Posn = -1; categoryCodePosn = -1; showToWebPosn = -1;
              }
              else
              {
                q = "SELECT Manufacturer, ManufacturerCode, ItemCode, Description, Status, ShowToWeb, CategoryCode FROM stock WHERE Manufacturer = '" + codeArg
                  + "' ORDER BY Manufacturer, ManufacturerCode DESC";
                
                itemCodePosn = 3; descPosn = 4; mfrPosn = 1; mfrCodePosn = 2; statusPosn = 5; altItemCode1Posn = -1; altItemCode2Posn = -1;
                altItemCode3Posn = -1; altItemCode4Posn = -1; categoryCodePosn = 7; showToWebPosn = 6;
              }
            }
            else
              if(inx == '4')
              {
                if(callType == 'G')
                {
                  q = "SELECT DISTINCT ManufacturerCode FROM stock WHERE ManufacturerCode <= 'zzzzzzzzzzzzzzzzzzzzzzzzzzzzzz' "
                      + "ORDER BY ManufacturerCode DESC";
                  
                  itemCodePosn = -1; descPosn = -1; mfrPosn = -1; mfrCodePosn = 1; statusPosn = -1; altItemCode1Posn = -1; altItemCode2Posn = -1;
                  altItemCode3Posn = -1; altItemCode4Posn = -1; categoryCodePosn = -1; showToWebPosn = -1;
                }
                else
                {
                  q = "SELECT ManufacturerCode, Manufacturer, ItemCode, Description, Status, ShowToWeb, CategoryCode FROM stock WHERE ManufacturerCode = '" + codeArg
                    + "' ORDER BY ManufacturerCode, Manufacturer DESC";
                  
                  itemCodePosn = 3; descPosn = 4; mfrPosn = 2; mfrCodePosn = 1; statusPosn = 5; altItemCode1Posn = -1; altItemCode2Posn = -1;
                  altItemCode3Posn = -1; altItemCode4Posn = -1; categoryCodePosn = 7; showToWebPosn = 6;
                }
              }
              else
                if(inx == '5')
                {
                  if(callType == 'G')
                  {
                    q = "SELECT DISTINCT AltItemCode1 FROM stock WHERE AltItemCode1 <= 'zzzzzzzzzzzzzzzzzzzzzzzzzzzzzz' ORDER BY AltItemCode1 DESC";
                    
                    itemCodePosn = -1; descPosn = -1; mfrPosn = -1; mfrCodePosn = -1; statusPosn = -1; altItemCode1Posn = 1; altItemCode2Posn = -1;
                    altItemCode3Posn = -1; altItemCode4Posn = -1; categoryCodePosn = -1; showToWebPosn = -1;
                  }
                  else
                  {
                    q = "SELECT AltItemCode1, ItemCode, Manufacturer, ManufacturerCode, Description, Status, ShowToWeb, CategoryCode FROM stock WHERE AltItemCode1 = '"
                      + codeArg + "' ORDER BY AltItemCode1 DESC";
                    
                    itemCodePosn = 2; descPosn = 5; mfrPosn = 3; mfrCodePosn = 4; statusPosn = 6; altItemCode1Posn = 1; altItemCode2Posn = -1;
                    altItemCode3Posn = -1; altItemCode4Posn = -1; categoryCodePosn = 8; showToWebPosn = 7;
                  }
                }
                else
                  if(inx == '6')
                  {
                    if(callType == 'G')
                    {
                      q = "SELECT DISTINCT AltItemCode2 FROM stock WHERE AltItemCode2 <= 'zzzzzzzzzzzzzzzzzzzzzzzzzzzzzz' ORDER BY AltItemCode2 DESC";
                      
                      itemCodePosn = -1; descPosn = -1; mfrPosn = -1; mfrCodePosn = -1; statusPosn = -1; altItemCode1Posn = -1; altItemCode2Posn = 1;
                      altItemCode3Posn = -1; altItemCode4Posn = -1; categoryCodePosn = -1; showToWebPosn = -1;
                    }
                    else
                    {
                      q = "SELECT AltItemCode2, ItemCode, Manufacturer, ManufacturerCode, Description, Status, ShowToWeb, CategoryCode FROM stock WHERE AltItemCode2 = '"
                        + codeArg + "' ORDER BY AltItemCode2 DESC";
                      
                      itemCodePosn = 2; descPosn = 5; mfrPosn = 3; mfrCodePosn = 4; statusPosn = 6; altItemCode1Posn = -1; altItemCode2Posn = 1;
                      altItemCode3Posn = -1; altItemCode4Posn = -1; categoryCodePosn = 8; showToWebPosn = 7;
                    }
                  }
                  else
                    if(inx == '7')
                    {
                      if(callType == 'G')
                      {
                        q = "SELECT DISTINCT AltItemCode3 FROM stock WHERE AltItemCode3 <= 'zzzzzzzzzzzzzzzzzzzzzzzzzzzzzz' ORDER BY AltItemCode3 DESC";
                        
                        itemCodePosn = -1; descPosn = -1; mfrPosn = -1; mfrCodePosn = -1; statusPosn = -1; altItemCode1Posn = -1;
                        altItemCode2Posn = -1; altItemCode3Posn = 1; altItemCode4Posn = -1; categoryCodePosn = -1; showToWebPosn = -1;
                      }
                      else
                      {
                        q = "SELECT AltItemCode3, ItemCode, Manufacturer, ManufacturerCode, Description, Status, ShowToWeb, CategoryCode FROM stock WHERE AltItemCode3 = '"
                          + codeArg + "' ORDER BY AltItemCode3 DESC";
                        
                        itemCodePosn = 2; descPosn = 5; mfrPosn = 3; mfrCodePosn = 4; statusPosn = 6; altItemCode1Posn = 1; altItemCode2Posn = -1;
                        altItemCode3Posn = -1; altItemCode4Posn = -1; categoryCodePosn = 8; showToWebPosn = 7;
                      }
                    }
                    else
                    // inx == '8'
                    {
                      if(callType == 'G')
                      {
                        q = "SELECT DISTINCT AltItemCode4 FROM stock WHERE AltItemCode4 <= 'zzzzzzzzzzzzzzzzzzzzzzzzzzzzzz' ORDER BY"
                            + " AltItemCode4 DESC";
                        
                        itemCodePosn = -1; descPosn = -1; mfrPosn = -1; mfrCodePosn = -1; statusPosn = -1; altItemCode1Posn = -1;
                        altItemCode2Posn = -1; altItemCode3Posn = -1; altItemCode4Posn = 1; categoryCodePosn = -1; showToWebPosn = -1;
                      }
                      else
                      {
                        q = "SELECT AltItemCode4, ItemCode, Manufacturer, ManufacturerCode, Description, Status, ShowToWeb, CategoryCode FROM stock WHERE AltItemCode4 = '"
                          + codeArg + "' ORDER BY AltItemCode4 DESC";
                        
                        itemCodePosn = 2; descPosn = 5; mfrPosn = 3; mfrCodePosn = 4; statusPosn = 6; altItemCode1Posn = -1; altItemCode2Posn = -1;
                        altItemCode3Posn = -1; altItemCode4Posn = 1; categoryCodePosn = 8; showToWebPosn = 7;
                      }
                    }

        stmt.setMaxRows(maxRows);
        rs = stmt.executeQuery(q);
        rsmd = rs.getMetaData();

        while (rs.next())
        {
          itemCode = getValue(itemCodePosn, ' ', rs, rsmd);
          desc = getValue(descPosn, ' ', rs, rsmd);
          mfr = getValue(mfrPosn, ' ', rs, rsmd);
          mfrCode = getValue(mfrCodePosn, ' ', rs, rsmd);
          altItemCode1 = getValue(altItemCode1Posn, ' ', rs, rsmd);
          altItemCode2 = getValue(altItemCode2Posn, ' ', rs, rsmd);
          altItemCode3 = getValue(altItemCode3Posn, ' ', rs, rsmd);
          altItemCode4 = getValue(altItemCode4Posn, ' ', rs, rsmd);
          status = getValue(statusPosn, ' ', rs, rsmd);
                  categoryCode = getValue(categoryCodePosn, ' ', rs, rsmd);
                  showToWeb = getValue(showToWebPosn, ' ', rs, rsmd);

          if(inx == '1')
            prependBodyLine(html, inx, itemCode, desc, mfr, mfrCode, "", categoryCode, itemCode, status, showToWeb, imagesDir);
          else
            if(inx == '2')
            {
              if(callType == 'G')
                prependBodyLine2(html, callType, desc);
              else prependBodyLine(html, inx, desc, itemCode, mfr, mfrCode, "", categoryCode, itemCode, status, showToWeb, imagesDir);
            }
            else
              if(inx == '3')
              {
                if(callType == 'G')
                  prependBodyLine2(html, callType, mfr);
                else prependBodyLine(html, inx, mfr, mfrCode, itemCode, desc, "", categoryCode, itemCode, status, showToWeb, imagesDir);
              }
              else
                if(inx == '4')
                {
                  if(callType == 'G')
                    prependBodyLine2(html, callType, mfrCode);
                  else prependBodyLine(html, inx, mfrCode, mfr, itemCode, desc, "", categoryCode, itemCode, status, showToWeb, imagesDir);
                }
                else
                  if(inx == '5')
                  {
                    if(callType == 'G')
                      prependBodyLine2(html, callType, altItemCode1);
                    else prependBodyLine(html, inx, altItemCode1, itemCode, mfr, mfrCode, desc, categoryCode, itemCode, status, showToWeb, imagesDir);
                  }
                  else
                    if(inx == '6')
                    {
                      if(callType == 'G')
                        prependBodyLine2(html, callType, altItemCode2);
                      else prependBodyLine(html, inx, altItemCode2, itemCode, mfr, mfrCode, desc, categoryCode, itemCode, status, showToWeb, imagesDir);
                    }
                    else
                      if(inx == '7')
                      {
                        if(callType == 'G')
                          prependBodyLine2(html, callType, altItemCode3);
                        else prependBodyLine(html, inx, altItemCode3, itemCode, mfr, mfrCode, desc, categoryCode, itemCode, status, showToWeb, imagesDir);
                      }
                      else
                      // inx == '8'
                      {
                        if(callType == 'G')
                          prependBodyLine2(html, callType, altItemCode4);
                        else prependBodyLine(html, inx, altItemCode4, itemCode, mfr, mfrCode, desc, categoryCode, itemCode, status, showToWeb, imagesDir);
                      }

          if(count++ == 1)
          {
            lastCodeOnPage[0] = itemCode;

            if(inx == '1')
              lastEntryOnPage[0] = itemCode;
            else
              if(inx == '2')
                lastEntryOnPage[0] = desc;
              else
                if(inx == '3')
                  lastEntryOnPage[0] = mfr;
                else
                  if(inx == '4')
                    lastEntryOnPage[0] = mfrCode;
                  else
                    if(inx == '5')
                      lastEntryOnPage[0] = altItemCode1;
                    else
                      if(inx == '6')
                        lastEntryOnPage[0] = altItemCode2;
                      else
                        if(inx == '7')
                          lastEntryOnPage[0] = altItemCode3;
                        else // inx == '8'
                        lastEntryOnPage[0] = altItemCode4;
          }
        }

        rs.close();

        firstCodeOnPage[0] = itemCode;

        if(inx == '1')
          firstEntryOnPage[0] = itemCode;
        else
          if(inx == '2')
            firstEntryOnPage[0] = desc;
          else
            if(inx == '3')
              firstEntryOnPage[0] = mfr;
            else
              if(inx == '4')
                firstEntryOnPage[0] = mfrCode;
              else
                if(inx == '5')
                  firstEntryOnPage[0] = altItemCode1;
                else
                  if(inx == '6')
                    firstEntryOnPage[0] = altItemCode2;
                  else
                    if(inx == '7')
                      firstEntryOnPage[0] = altItemCode3;
                    else // inx == '8'
                    firstEntryOnPage[0] = altItemCode4;

        newFirstRecNum[0] = generalUtils.intToStr(numRecs[0] - count + 2);
        newLastRecNum[0] = generalUtils.intToStr(numRecs[0]);
        break;
      case 'N': // next page
                if(inx == '1')
                {
                  if(callType == 'S')
                  {
                    q = "SELECT ItemCode, Description, Manufacturer, ManufacturerCode, Status, CategoryCode, ShowToWeb FROM stock WHERE ItemCode" + " LIKE '"
                      + srchStr + "%' AND ItemCode > '" + codeArg + "' ORDER BY ItemCode";
                    
                    itemCodePosn = 1; descPosn = 2; mfrPosn = 3; mfrCodePosn = 4; statusPosn = 5; altItemCode1Posn = -1;
                    altItemCode2Posn = -1; altItemCode3Posn = -1; altItemCode4Posn = -1; categoryCodePosn = 6; showToWebPosn = 7;
                  }
                  else
                  {
                    q = "SELECT ItemCode, Description, Manufacturer, ManufacturerCode, Status, CategoryCode, ShowToWeb FROM stock WHERE ItemCode > "
                      + codeArg + " ORDER BY ItemCode";
            
                    itemCodePosn = 1; descPosn = 2; mfrPosn = 3; mfrCodePosn = 4; statusPosn = 5; altItemCode1Posn = -1;
                    altItemCode2Posn = -1; altItemCode3Posn = -1; altItemCode4Posn = -1; categoryCodePosn = 6; showToWebPosn = 7;
                  }
                }
                else
                if(inx == '2')
                {
                  if(callType == 'G')
                  {
                    q = "SELECT DISTINCT Description FROM stock WHERE Description > '" + codeArg + "' ORDER BY Description";
            
                    itemCodePosn = -1; descPosn = 1; mfrPosn = -1; mfrCodePosn = -1; statusPosn = -1; altItemCode1Posn = -1;
                    altItemCode2Posn = -1; altItemCode3Posn = -1; altItemCode4Posn = -1; categoryCodePosn = -1; showToWebPosn = -1;
                  }
                  else
                  {
                    q = "SELECT Description, ItemCode, Manufacturer, ManufacturerCode, Status, ShowToWeb, CategoryCode FROM stock WHERE Description > '"
                      + codeArg + "' AND ItemCode > '" + codeArg2 + "' ORDER BY Description, ItemCode";

                    itemCodePosn = 2; descPosn = 1; mfrPosn = 3; mfrCodePosn = 4; statusPosn = 5; altItemCode1Posn = -1;
                    altItemCode2Posn = -1; altItemCode3Posn = -1; altItemCode4Posn = -1; categoryCodePosn = 7; showToWebPosn = 6;
                  }
                }
                else
                if(inx == '3')
                {
                  if(callType == 'G')
                  {
                    q = "SELECT DISTINCT Manufacturer FROM stock WHERE Manufacturer >= '" + codeArg + "' ORDER BY Manufacturer";
                
                    itemCodePosn = -1; descPosn = -1; mfrPosn = 1; mfrCodePosn = -1; statusPosn = -1; altItemCode1Posn = -1;
                    altItemCode2Posn = -1; altItemCode3Posn = -1; altItemCode4Posn = -1; categoryCodePosn = -1; showToWebPosn = -1;
                  }
                  else
                  {
                    q = "SELECT Manufacturer, ManufacturerCode, ItemCode, Description, Status, ShowToWeb, CategoryCode FROM stock WHERE Manufacturer = '"
                      + codeArg + "' AND ItemCode > '" + codeArg2 + "' ORDER BY Manufacturer, ManufacturerCode";
                
                    itemCodePosn = 3; descPosn = 4; mfrPosn = 1; mfrCodePosn = 2; statusPosn = 5; altItemCode1Posn = -1;
                    altItemCode2Posn = -1; altItemCode3Posn = -1; altItemCode4Posn = -1; categoryCodePosn = 7; showToWebPosn = 6;
              }
            }
            else
              if(inx == '4')
              {
                if(callType == 'G')
                {
                  q = "SELECT DISTINCT ManufacturerCode FROM stock WHERE ManufacturerCode >= '" + codeArg + "' ORDER BY "
                      + "ManufacturerCode";
                  itemCodePosn = -1;
                  descPosn = -1;
                  mfrPosn = -1;
                  mfrCodePosn = 1;
                  statusPosn = -1;
                  altItemCode1Posn = -1;
                  altItemCode2Posn = -1;
                  altItemCode3Posn = -1;
                  altItemCode4Posn = -1; categoryCodePosn = -1; showToWebPosn = -1;
                }
                else
                {
                  q = "SELECT ManufacturerCode, Manufacturer, ItemCode, Description, Status, ShowToWeb, CategoryCode FROM stock WHERE ManufacturerCode = '"
                      + codeArg + "' AND ItemCode > '" + codeArg2 + "' ORDER BY ManufacturerCode, ItemCode";
                  itemCodePosn = 3;
                  descPosn = 4;
                  mfrPosn = 2;
                  mfrCodePosn = 1;
                  statusPosn = 5;
                  altItemCode1Posn = -1;
                  altItemCode2Posn = -1;
                  altItemCode3Posn = -1;
                  altItemCode4Posn = -1; categoryCodePosn = 7; showToWebPosn = 6;
                }
              }
              else
                if(inx == '5')
                {
                  if(callType == 'G')
                  {
                    q = "SELECT DISTINCT AltItemCode1 FROM stock WHERE AltItemCode1 >= '" + codeArg + "' ORDER BY AltItemCode1";
                    itemCodePosn = -1;
                    descPosn = -1;
                    mfrPosn = -1;
                    mfrCodePosn = -1;
                    statusPosn = -1;
                    altItemCode1Posn = 1;
                    altItemCode2Posn = -1;
                    altItemCode3Posn = -1;
                    altItemCode4Posn = -1; categoryCodePosn = -1; showToWebPosn = -1;
                  }
                  else
                  {
                    q = "SELECT AltItemCode1, ItemCode, Manufacturer, ManufacturerCode, Description, Status, ShowToWeb, CategoryCode FROM stock WHERE "
                        + "AltItemCode1 = '" + codeArg + "' AND ItemCode > '" + codeArg2 + "' ORDER BY AltItemCode1, ItemCode";
                    itemCodePosn = 2;
                    descPosn = 5;
                    mfrPosn = 3;
                    mfrCodePosn = 4;
                    statusPosn = 6;
                    altItemCode1Posn = 1;
                    altItemCode2Posn = -1;
                    altItemCode3Posn = -1;
                    altItemCode4Posn = -1; categoryCodePosn = 8; showToWebPosn = 7;
                  }
                }
                else
                  if(inx == '6')
                  {
                    if(callType == 'G')
                    {
                      q = "SELECT DISTINCT AltItemCode2 FROM stock WHERE AltItemCode2 >= '" + codeArg + "' ORDER BY AltItemCode2";
                      itemCodePosn = -1;
                      descPosn = -1;
                      mfrPosn = -1;
                      mfrCodePosn = -1;
                      statusPosn = -1;
                      altItemCode1Posn = -1;
                      altItemCode2Posn = 1;
                      altItemCode3Posn = -1;
                      altItemCode4Posn = -1; categoryCodePosn = -1; showToWebPosn = -1;
                    }
                    else
                    {
                      q = "SELECT AltItemCode2, ItemCode, Manufacturer, ManufacturerCode, Description, Status, ShowToWeb, CategoryCode FROM stock WHERE "
                          + "AltItemCode2 = '" + codeArg + "' AND ItemCode > '" + codeArg2 + "' ORDER BY AltItemCode2, ItemCode";
                      itemCodePosn = 2;
                      descPosn = 5;
                      mfrPosn = 3;
                      mfrCodePosn = 4;
                      statusPosn = 6;
                      altItemCode1Posn = -1;
                      altItemCode2Posn = 1;
                      altItemCode3Posn = -1;
                      altItemCode4Posn = -1; categoryCodePosn = 8; showToWebPosn = 7;
                    }
                  }
                  else
                    if(inx == '7')
                    {
                      if(callType == 'G')
                      {
                        q = "SELECT DISTINCT AltItemCode3 FROM stock WHERE AltItemCode3 >= '" + codeArg + "' ORDER BY AltItemCode3";
                        itemCodePosn = -1;
                        descPosn = -1;
                        mfrPosn = -1;
                        mfrCodePosn = -1;
                        statusPosn = -1;
                        altItemCode1Posn = -1;
                        altItemCode2Posn = -1;
                        altItemCode3Posn = 1;
                        altItemCode4Posn = -1; categoryCodePosn = -1; showToWebPosn = -1;
                      }
                      else
                      {
                        q = "SELECT AltItemCode3, ItemCode, Manufacturer, ManufacturerCode, Description, Status, ShowToWeb, CategoryCode FROM stock WHERE "
                            + "AltItemCode3 = '" + codeArg + "' AND ItemCode > '" + codeArg2 + "' ORDER BY AltItemCode3, ItemCode";
                        itemCodePosn = 2;
                        descPosn = 5;
                        mfrPosn = 3;
                        mfrCodePosn = 4;
                        statusPosn = 6;
                        altItemCode1Posn = -1;
                        altItemCode2Posn = -1;
                        altItemCode3Posn = 1;
                        altItemCode4Posn = -1; categoryCodePosn = 8; showToWebPosn = 7;
                      }
                    }
                    else
                    // inx == '8'
                    {
                      if(callType == 'G')
                      {
                        q = "SELECT DISTINCT AltItemCode4 FROM stock WHERE AltItemCode4 >= '" + codeArg + "' ORDER BY AltItemCode4";
                        itemCodePosn = -1;
                        descPosn = -1;
                        mfrPosn = -1;
                        mfrCodePosn = -1;
                        statusPosn = -1;
                        altItemCode1Posn = -1;
                        altItemCode2Posn = -1;
                        altItemCode3Posn = -1;
                        altItemCode4Posn = 1; categoryCodePosn = -1; showToWebPosn = -1;
                      }
                      else
                      {
                        q = "SELECT AltItemCode4, ItemCode, Manufacturer, ManufacturerCode, Description, Status, ShowToWeb, CategoryCode FROM stock WHERE "
                            + "AltItemCode4 = '" + codeArg + "' AND ItemCode > '" + codeArg2 + "' ORDER BY AltItemCode4, ItemCode";
                        itemCodePosn = 2;
                        descPosn = 5;
                        mfrPosn = 3;
                        mfrCodePosn = 4;
                        statusPosn = 6;
                        altItemCode1Posn = -1;
                        altItemCode2Posn = -1;
                        altItemCode3Posn = -1;
                        altItemCode4Posn = 1; categoryCodePosn = 8; showToWebPosn = 7;
                      }
                    }

        stmt.setMaxRows(maxRows);
        rs = stmt.executeQuery(q);
        rsmd = rs.getMetaData();

        while (rs.next())
        {
          itemCode = getValue(itemCodePosn, ' ', rs, rsmd);
          desc = getValue(descPosn, ' ', rs, rsmd);
          mfr = getValue(mfrPosn, ' ', rs, rsmd);
          mfrCode = getValue(mfrCodePosn, ' ', rs, rsmd);
          altItemCode1 = getValue(altItemCode1Posn, ' ', rs, rsmd);
          altItemCode2 = getValue(altItemCode2Posn, ' ', rs, rsmd);
          altItemCode3 = getValue(altItemCode3Posn, ' ', rs, rsmd);
          altItemCode4 = getValue(altItemCode4Posn, ' ', rs, rsmd);
          status = getValue(statusPosn, ' ', rs, rsmd);
                  categoryCode = getValue(categoryCodePosn, ' ', rs, rsmd);
                  showToWeb = getValue(showToWebPosn, ' ', rs, rsmd);

          if(inx == '1')
            appendBodyLine(html, inx, itemCode, desc, mfr, mfrCode, "", categoryCode, itemCode, status, showToWeb, imagesDir);
          else
            if(inx == '2')
            {
              if(callType == 'G')
                appendBodyLine2(html, callType, operation, desc);
              else appendBodyLine(html, inx, desc, itemCode, mfr, mfrCode, "", categoryCode, itemCode, status, showToWeb, imagesDir);
            }
            else
              if(inx == '3')
              {
                if(callType == 'G')
                  appendBodyLine2(html, callType, operation, mfr);
                else appendBodyLine(html, inx, mfr, mfrCode, itemCode, desc, "", categoryCode, itemCode, status, showToWeb, imagesDir);
              }
              else
                if(inx == '4')
                {
                  if(callType == 'G')
                    appendBodyLine2(html, callType, operation, mfrCode);
                  else appendBodyLine(html, inx, mfrCode, mfr, itemCode, desc, "", categoryCode, itemCode, status, showToWeb, imagesDir);
                }
                else
                  if(inx == '5')
                  {
                    if(callType == 'G')
                      appendBodyLine2(html, callType, operation, altItemCode1);
                    else appendBodyLine(html, inx, altItemCode1, itemCode, mfr, mfrCode, desc, categoryCode, itemCode, status, showToWeb, imagesDir);
                  }
                  else
                    if(inx == '6')
                    {
                      if(callType == 'G')
                        appendBodyLine2(html, callType, operation, altItemCode2);
                      else appendBodyLine(html, inx, altItemCode2, itemCode, mfr, mfrCode, desc, categoryCode, itemCode, status, showToWeb, imagesDir);
                    }
                    else
                      if(inx == '7')
                      {
                        if(callType == 'G')
                          appendBodyLine2(html, callType, operation, altItemCode3);
                        else appendBodyLine(html, inx, altItemCode3, itemCode, mfr, mfrCode, desc, categoryCode, itemCode, status, showToWeb, imagesDir);
                      }
                      else
                      // inx == '8'
                      {
                        if(callType == 'G')
                          appendBodyLine2(html, callType, operation, altItemCode4);
                        else appendBodyLine(html, inx, altItemCode4, itemCode, mfr, mfrCode, desc, categoryCode, itemCode, status, showToWeb, imagesDir);
                      }

          if(count++ == 1)
          {
            firstCodeOnPage[0] = itemCode;

            if(inx == '1')
              firstEntryOnPage[0] = itemCode;
            else
              if(inx == '2')
                firstEntryOnPage[0] = desc;
              else
                if(inx == '3')
                  firstEntryOnPage[0] = mfr;
                else
                  if(inx == '4')
                    firstEntryOnPage[0] = mfrCode;
                  else
                    if(inx == '5')
                      firstEntryOnPage[0] = altItemCode1;
                    else
                      if(inx == '6')
                        firstEntryOnPage[0] = altItemCode2;
                      else
                        if(inx == '7')
                          firstEntryOnPage[0] = altItemCode3;
                        else // inx == '8'
                        firstEntryOnPage[0] = altItemCode4;
          }
        }

        rs.close();

        lastCodeOnPage[0] = itemCode;

        if(inx == '1')
          lastEntryOnPage[0] = itemCode;
        else
          if(inx == '2')
            lastEntryOnPage[0] = desc;
          else
            if(inx == '3')
              lastEntryOnPage[0] = mfr;
            else
              if(inx == '4')
                lastEntryOnPage[0] = mfrCode;
              else
                if(inx == '5')
                  lastEntryOnPage[0] = altItemCode1;
                else
                  if(inx == '6')
                    lastEntryOnPage[0] = altItemCode2;
                  else
                    if(inx == '7')
                      lastEntryOnPage[0] = altItemCode3;
                    else // inx == '8'
                    lastEntryOnPage[0] = altItemCode4;

        newFirstRecNum[0] = generalUtils.intToStr(generalUtils.intFromStr(lastRecNum) + 1);
        newLastRecNum[0] = generalUtils.intToStr(generalUtils.intFromStr(lastRecNum) + count - 1);
                 break;
      case 'P': // prev page
                if(inx == '1')
                {
                  if(callType == 'S')
                  {
                    q = "SELECT ItemCode, Description, Manufacturer, ManufacturerCode, Status, CategoryCode, ShowToWeb FROM stock WHERE ItemCode LIKE '"
                      + srchStr + "%' AND ItemCode < '" + codeArg + "' ORDER BY ItemCode DESC";
                  }
                  else
                  {
                    q = "SELECT ItemCode, Description, Manufacturer, ManufacturerCode, Status, CategoryCode, ShowToWeb FROM stock WHERE ItemCode < "
                      + codeArg + " ORDER BY ItemCode DESC";
                  }

                  itemCodePosn = 1; descPosn = 2; mfrPosn = 3; mfrCodePosn = 4; statusPosn = 5; altItemCode1Posn = -1;
                  altItemCode2Posn = -1; altItemCode3Posn = -1; altItemCode4Posn = -1; categoryCodePosn = 6; showToWebPosn = 7;
                }
                else
                if(inx == '2')
                {
                  if(callType == 'G')
                  {
                    q = "SELECT DISTINCT Description FROM stock WHERE Description < '" + codeArg + "' ORDER BY Description DESC";
                    
                    itemCodePosn = -1; descPosn = 1; mfrPosn = -1; mfrCodePosn = -1; statusPosn = -1; altItemCode1Posn = -1;
                    altItemCode2Posn = -1; altItemCode3Posn = -1; altItemCode4Posn = -1; categoryCodePosn = -1; showToWebPosn = -1;
                  }
                  else
                  {
                    q = "SELECT Description, ItemCode, Manufacturer, ManufacturerCode, Status, ShowToWeb, CategoryCode FROM stock WHERE Description = '"
                      + codeArg + "' AND ItemCode < '" + codeArg2 + "' ORDER BY Description, ItemCode DESC";

                    itemCodePosn = 2; descPosn = 1; mfrPosn = 3; mfrCodePosn = 4; statusPosn = 5; altItemCode1Posn = -1;
                    altItemCode2Posn = -1; altItemCode3Posn = -1; altItemCode4Posn = -1; categoryCodePosn = 7; showToWebPosn = 6;
                  }
                }
                else
                if(inx == '3')
                {
                  if(callType == 'G')
                  {
                    q = "SELECT DISTINCT Manufacturer FROM stock WHERE Manufacturer < '" + codeArg + "' ORDER BY Manufacturer DESC";
                    
                    itemCodePosn = -1; descPosn = -1; mfrPosn = 1; mfrCodePosn = -1; statusPosn = -1; altItemCode1Posn = -1;
                    altItemCode2Posn = -1; altItemCode3Posn = -1; altItemCode4Posn = -1; categoryCodePosn = -1; showToWebPosn = -1;
                  }
                  else
                  {
                    q = "SELECT Manufacturer, ManufacturerCode, ItemCode, Description, Status, ShowToWeb, CategoryCode FROM stock WHERE Manufacturer = '"
                      + codeArg + "' AND ItemCode < '" + codeArg2 + "' ORDER BY Manufacturer, ManufacturerCode DESC";
                    
                    itemCodePosn = 3; descPosn = 4; mfrPosn = 1; mfrCodePosn = 2; statusPosn = 5; altItemCode1Posn = -1;
                    altItemCode2Posn = -1; altItemCode3Posn = -1; altItemCode4Posn = -1; categoryCodePosn = 7; showToWebPosn = 6;
                  }
                }
                else
                if(inx == '4')
                {
                  if(callType == 'G')
                  {
                    q = "SELECT DISTINCT ManufacturerCode FROM stock WHERE ManufacturerCode < '" + codeArg + "' ORDER BY "
                      + "ManufacturerCode DESC";
                    
                    itemCodePosn = -1; descPosn = -1; mfrPosn = -1; mfrCodePosn = 1; statusPosn = -1; altItemCode1Posn = -1;
                    altItemCode2Posn = -1; altItemCode3Posn = -1; altItemCode4Posn = -1; categoryCodePosn = -1; showToWebPosn = -1;
                  }
                  else
                  {
                    q = "SELECT ManufacturerCode, Manufacturer, ItemCode, Description, Status, ShowToWeb, CategoryCode FROM stock WHERE ManufacturerCode = '"
                      + codeArg + "' AND ItemCode < '" + codeArg2 + "' ORDER BY ManufacturerCode, ItemCode DESC";
                  
                    itemCodePosn = 3; descPosn = 4; mfrPosn = 2; mfrCodePosn = 1; statusPosn = 5; altItemCode1Posn = -1; altItemCode2Posn = -1;
                    altItemCode3Posn = -1; altItemCode4Posn = -1; categoryCodePosn = 7; showToWebPosn = 6;
                  }
                }
                else
                if(inx == '5')
                {
                  if(callType == 'G')
                  {
                    q = "SELECT DISTINCT AltItemCode1 FROM stock WHERE AltItemCode1 < '" + codeArg + "' ORDER BY " + "AltItemCode1 DESC";
                    itemCodePosn = -1;
                    descPosn = -1;
                    mfrPosn = -1;
                    mfrCodePosn = -1;
                    statusPosn = -1;
                    altItemCode1Posn = 1;
                    altItemCode2Posn = -1;
                    altItemCode3Posn = -1;
                    altItemCode4Posn = -1; categoryCodePosn = -1; showToWebPosn = -1;
                  }
                  else
                  {
                    q = "SELECT AltItemCode1, ItemCode, Manufacturer, ManufacturerCode, Description, Status, ShowToWeb, CategoryCode FROM stock "
                        + "WHERE AltItemCode1 = '" + codeArg + "' AND ItemCode < '" + codeArg2 + "' ORDER BY AltItemCode1, ItemCode " + "DESC";
                    itemCodePosn = 2;
                    descPosn = 5;
                    mfrPosn = 3;
                    mfrCodePosn = 4;
                    statusPosn = 6;
                    altItemCode1Posn = 1;
                    altItemCode2Posn = -1;
                    altItemCode3Posn = -1;
                    altItemCode4Posn = -1; categoryCodePosn = 8; showToWebPosn = 7;
                  }
                }
                else
                if(inx == '6')
                {
                  if(callType == 'G')
                  {
                    q = "SELECT DISTINCT AltItemCode2 FROM stock WHERE AltItemCode2 < '" + codeArg + "' ORDER BY " + "AltItemCode2 DESC";
                
                    itemCodePosn = -1; descPosn = -1; mfrPosn = -1; mfrCodePosn = -1; statusPosn = -1; altItemCode1Posn = -1; altItemCode2Posn = 1;
                    altItemCode3Posn = -1; altItemCode4Posn = -1; categoryCodePosn = -1; showToWebPosn = -1;
                  }
                  else
                  {
                    q = "SELECT AltItemCode2, ItemCode, Manufacturer, ManufacturerCode, Description, Status, ShowToWeb, CategoryCode FROM stock "
                      + "WHERE AltItemCode2 = '" + codeArg + "' AND ItemCode < '" + codeArg2 + "' ORDER BY AltItemCode2, ItemCode DESC";
                      
                    itemCodePosn = 2;
                    descPosn = 5;
                    mfrPosn = 3;
                    mfrCodePosn = 4;
                    statusPosn = 6;
                      altItemCode1Posn = -1;
                      altItemCode2Posn = 1;
                      altItemCode3Posn = -1;
                      altItemCode4Posn = -1; categoryCodePosn = 8; showToWebPosn = 7;
                    }
                  }
                  else
                    if(inx == '7')
                    {
                      if(callType == 'G')
                      {
                        q = "SELECT DISTINCT AltItemCode3 FROM stock WHERE AltItemCode3 < '" + codeArg + "' ORDER BY "
                            + "AltItemCode3 DESC";
                        itemCodePosn = -1;
                        descPosn = -1;
                        mfrPosn = -1;
                        mfrCodePosn = -1;
                        statusPosn = -1;
                        altItemCode1Posn = -1;
                        altItemCode2Posn = -1;
                        altItemCode3Posn = 1;
                        altItemCode4Posn = -1; categoryCodePosn = -1; showToWebPosn = -1;
                      }
                      else
                      {
                        q = "SELECT AltItemCode3, ItemCode, Manufacturer, ManufacturerCode, Description, Status, ShowToWeb, CategoryCode FROM stock "
                            + "WHERE AltItemCode3 = '" + codeArg + "' AND ItemCode < '" + codeArg2 + "' ORDER BY AltItemCode3, ItemCode DESC";
                        itemCodePosn = 2;
                        descPosn = 5;
                        mfrPosn = 3;
                        mfrCodePosn = 4;
                        statusPosn = 6;
                        altItemCode1Posn = -1;
                        altItemCode2Posn = -1;
                        altItemCode3Posn = 1;
                        altItemCode4Posn = -1; categoryCodePosn = 8; showToWebPosn = 7;
                      }
                    }
                    else
                    // inx == '8'
                    {
                      if(callType == 'G')
                      {
                        q = "SELECT DISTINCT AltItemCode4 FROM stock WHERE AltItemCode4 < '" + codeArg + "' ORDER BY "
                            + "AltItemCode4 DESC";
                        itemCodePosn = -1;
                        descPosn = -1;
                        mfrPosn = -1;
                        mfrCodePosn = -1;
                        statusPosn = -1;
                        altItemCode1Posn = -1;
                        altItemCode2Posn = -1;
                        altItemCode3Posn = -1;
                        altItemCode4Posn = 1; categoryCodePosn = -1; showToWebPosn = -1;
                      }
                      else
                      {
                        q = "SELECT AltItemCode4, ItemCode, Manufacturer, ManufacturerCode, Description, Status, ShowToWeb, CategoryCode FROM stock "
                            + "WHERE AltItemCode4 = '"
                            + codeArg
                            + "' AND ItemCode < '"
                            + codeArg2
                            + "' ORDER BY AltItemCode4, ItemCode " + "DESC";
                        itemCodePosn = 2;
                        descPosn = 5;
                        mfrPosn = 3;
                        mfrCodePosn = 4;
                        statusPosn = 6;
                        altItemCode1Posn = -1;
                        altItemCode2Posn = -1;
                        altItemCode3Posn = -1;
                        altItemCode4Posn = 1; categoryCodePosn = 8; showToWebPosn = 7;
                      }
                    }

        stmt.setMaxRows(maxRows);
        rs = stmt.executeQuery(q);
        rsmd = rs.getMetaData();

        while(rs.next())
        {
          itemCode     = getValue(itemCodePosn, ' ', rs, rsmd);
          desc         = getValue(descPosn, ' ', rs, rsmd);
          mfr          = getValue(mfrPosn, ' ', rs, rsmd);
          mfrCode      = getValue(mfrCodePosn, ' ', rs, rsmd);
          altItemCode1 = getValue(altItemCode1Posn, ' ', rs, rsmd);
          altItemCode2 = getValue(altItemCode2Posn, ' ', rs, rsmd);
          altItemCode3 = getValue(altItemCode3Posn, ' ', rs, rsmd);
          altItemCode4 = getValue(altItemCode4Posn, ' ', rs, rsmd);
          status       = getValue(statusPosn, ' ', rs, rsmd);
                  categoryCode = getValue(categoryCodePosn, ' ', rs, rsmd);
                  showToWeb = getValue(showToWebPosn, ' ', rs, rsmd);

          if(inx == '1')
            prependBodyLine(html, inx, itemCode, desc, mfr, mfrCode, "", categoryCode, itemCode, status, showToWeb, imagesDir);
          else
            if(inx == '2')
            {
              if(callType == 'G')
                prependBodyLine2(html, callType, desc);
              else prependBodyLine(html, inx, desc, itemCode, mfr, mfrCode, "", categoryCode, itemCode, status, showToWeb, imagesDir);
            }
            else
              if(inx == '3')
              {
                if(callType == 'G')
                  prependBodyLine2(html, callType, mfr);
                else prependBodyLine(html, inx, mfr, mfrCode, itemCode, desc, "", categoryCode, itemCode, status, showToWeb, imagesDir);
              }
              else
                if(inx == '4')
                {
                  if(callType == 'G')
                    prependBodyLine2(html, callType, mfrCode);
                  else prependBodyLine(html, inx, mfrCode, mfr, itemCode, desc, "", categoryCode, itemCode, status, showToWeb, imagesDir);
                }
                else
                  if(inx == '5')
                  {
                    if(callType == 'G')
                      prependBodyLine2(html, callType, altItemCode1);
                    else prependBodyLine(html, inx, altItemCode1, itemCode, mfr, mfrCode, desc, categoryCode, itemCode, status, showToWeb, imagesDir);
                  }
                  else
                    if(inx == '6')
                    {
                      if(callType == 'G')
                        prependBodyLine2(html, callType, altItemCode2);
                      else prependBodyLine(html, inx, altItemCode2, itemCode, mfr, mfrCode, desc, categoryCode, itemCode, status, showToWeb, imagesDir);
                    }
                    else
                      if(inx == '7')
                      {
                        if(callType == 'G')
                          prependBodyLine2(html, callType, altItemCode3);
                        else prependBodyLine(html, inx, altItemCode3, itemCode, mfr, mfrCode, desc, categoryCode, itemCode, status, showToWeb, imagesDir);
                      }
                      else
                      // inx == '8'
                      {
                        if(callType == 'G')
                          prependBodyLine2(html, callType, altItemCode4);
                        else prependBodyLine(html, inx, altItemCode4, itemCode, mfr, mfrCode, desc, categoryCode, itemCode, status, showToWeb, imagesDir);
                      }

          if(count++ == 1)
          {
            lastCodeOnPage[0] = itemCode;

            if(inx == '1')
              lastEntryOnPage[0] = itemCode;
            else
              if(inx == '2')
                lastEntryOnPage[0] = desc;
              else
                if(inx == '3')
                  lastEntryOnPage[0] = mfr;
                else
                  if(inx == '4')
                    lastEntryOnPage[0] = mfrCode;
                  else
                    if(inx == '5')
                      lastEntryOnPage[0] = altItemCode1;
                    else
                      if(inx == '6')
                        lastEntryOnPage[0] = altItemCode2;
                      else
                        if(inx == '7')
                          lastEntryOnPage[0] = altItemCode3;
                        else // inx == '8'
                        lastEntryOnPage[0] = altItemCode4;
          }
        }

        rs.close();

        firstCodeOnPage[0] = itemCode;

        if(inx == '1')
          firstEntryOnPage[0] = itemCode;
        else
          if(inx == '2')
            firstEntryOnPage[0] = desc;
          else
            if(inx == '3')
              firstEntryOnPage[0] = mfr;
            else
              if(inx == '4')
                firstEntryOnPage[0] = mfrCode;
              else
                if(inx == '5')
                  firstEntryOnPage[0] = altItemCode1;
                else
                  if(inx == '6')
                    firstEntryOnPage[0] = altItemCode2;
                  else
                    if(inx == '7')
                      firstEntryOnPage[0] = altItemCode3;
                    else // inx == '8'
                    firstEntryOnPage[0] = altItemCode4;

        newFirstRecNum[0] = generalUtils.intToStr(generalUtils.intFromStr(firstRecNum) - count + 1);
        newLastRecNum[0] = generalUtils.intToStr(generalUtils.intFromStr(firstRecNum) - 1);
        break;
    }

    stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void setNav(PrintWriter out, boolean first, char inx, char operation, char callType, int numRecs, String firstCodeOnPage,
                      String lastCodeOnPage, String firstEntryOnPage, String lastEntryOnPage, String srchStr, String imagesDir, String newFirstRecNum,
                      String newLastRecNum, int maxRows, String unm, String sid, String uty, String men, String den, String dnm,
                      String bnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {

    if(first)
    {
      String title;
      if(callType == 'S')
      {
        title = "Stock Search Results for ";
        switch (inx)
        {
          case '1': title += "Item Code";             break;
          case '2': title += "Description";           break;
          case '3': title += "Manufacturer";          break;
          case '4': title += "Manufacturer Code";     break;
          case '5': title += "Alternate Item Code 1"; break;
          case '6': title += "Alternate Item Code 2"; break;
          case '7': title += "Alternate Item Code 3"; break;
          case '8': title += "Alternate Item Code 4"; break;
        }
        
        title += " starting with: " + srchStr;
      }
      else
      if(operation == 'X')
      {
        title = "Stock Listing via ";
        switch (inx)
        {
          case '1': title += "Item Code";             break;
          case '2': title += "Description";           break;
          case '3': title += "Manufacturer";          break;
          case '4': title += "Manufacturer Code";     break;
          case '5': title += "Alternate Item Code 1"; break;
          case '6': title += "Alternate Item Code 2"; break;
          case '7': title += "Alternate Item Code 3"; break;
          case '8': title += "Alternate Item Code 4"; break;
        }
      }
      else title = "Stock Listing";

      int[] hmenuCount = new int[1];

      drawingUtils.drawTitle(out, false, false, "CatalogListingPage", "", title, "2006", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
    }

    scoutln(out, bytesOut, "<form><table id=\"pageColumn\" border=0 cellspacing=0 cellpadding=0 width=100%>");

    scoutln(out, bytesOut, "<tr><td height=20 nowrap><p>");

    int newFirstRecNumI = generalUtils.strToInt(newFirstRecNum);
    int newLastRecNumI = generalUtils.strToInt(newLastRecNum);

    if(numRecs == 0)
    {
      if(first) scoutln(out, bytesOut, "No Records");
    }
    else
    {
      scoutln(out, bytesOut, "Records " + newFirstRecNum + " to " + newLastRecNum + " of " + numRecs);
      if(inx == '1' || callType != 'S')
      {
        if(newFirstRecNumI > 1 || newLastRecNumI < numRecs) scoutln(out, bytesOut, "<img src=\"" + imagesDir + "d.gif\">");
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

        for(int y = 0; y < len; ++y)
        {
          if(firstEntryOnPage.charAt(y) == '\'')
            arg += "\\'";
          else arg += firstEntryOnPage.charAt(y);
        }

        scoutln(out, bytesOut, "&nbsp;<a href=\"javascript:page('F','" + callType + "','" + arg + "','','" + topOrBottom + "')\">First</a>");
        scoutln(out, bytesOut, "&nbsp;&nbsp;<a href=\"javascript:page('P','" + callType + "','" + arg + "','" + generalUtils.sanitise(firstCodeOnPage)
                             + "','" + topOrBottom + "')\">Previous</a>");
      }

      if(newLastRecNumI < numRecs)
      {
        String arg = "";
        int len = lastEntryOnPage.length();
        for(int y = 0; y < len; ++y)
        {
          if(lastEntryOnPage.charAt(y) == '\'')
            arg += "\\'";
          else arg += lastEntryOnPage.charAt(y);
        }

        scoutln(out, bytesOut, "&nbsp;&nbsp;<a href=\"javascript:page('N','" + callType + "','" + arg + "','" + generalUtils.sanitise(lastCodeOnPage) + "','"
                               + topOrBottom + "')\">Next</a>");
        scoutln(out, bytesOut, "&nbsp;&nbsp;<a href=\"javascript:page('L','" + callType + "','" + arg + "','','" + topOrBottom + "')\">Last</a>");
      }
    }

    if(numRecs > 0) scoutln(out, bytesOut, "</td></tr><tr><td>");

    scoutln(out, bytesOut, "</td></tr><tr><td nowrap><p>");

    String arg = "";
    int len = lastEntryOnPage.length();
    for(int y = 0; y < len; ++y)
    {
      if(lastEntryOnPage.charAt(y) == '\'')
        arg += "\\'";
      else arg += lastEntryOnPage.charAt(y);
    }

    if(first)
      scoutln(out, bytesOut, "<a href=\"javascript:page('F','S','" + arg + "','','T')\">Search</a> on ");
    else
    {
      if(numRecs > 0) scoutln(out, bytesOut, "<a href=\"javascript:page('F','S','','','B')\">Search</a> on ");
    }

    if(numRecs > 0 || first)
    {
      switch (inx)
      {
        case '1': scoutln(out, bytesOut, "Item Code");              break;
        case '2': scoutln(out, bytesOut, "Description");            break;
        case '3': scoutln(out, bytesOut, "Manufacturer");           break;
        case '4': scoutln(out, bytesOut, "Manufacturer Code");      break;
        case '5': scoutln(out, bytesOut, "Alternate Item Code 1");  break;
        case '6': scoutln(out, bytesOut, "Alternate Item Code 2");  break;
        case '7': scoutln(out, bytesOut, "Alternate Item Code 3");  break;
        case '8': scoutln(out, bytesOut, "Alternate Item Code 4");  break;
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
                             + "\" name=maxRows2></td></tr><tr><td><img src=\"" + imagesDir + "z402.gif\">");
      }
    }

    scoutln(out, bytesOut, "</td></tr></table><table id=\"page\" border=0 width=100% cellspacing=0 cellpadding=0>");

    if(first) scoutln(out, bytesOut, "<tr><td><img src=\"" + imagesDir + "z402.gif\"></td></tr>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void setHead(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, char inx, String unm, String sid, String uty,
      String men, String den, String dnm, String bnm, String newFirstRecNum, String newLastRecNum, int numRecs,
      String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {

    scoutln(out, bytesOut, "<html><head><title>Stock Listing</title>");
    scoutln(out, bytesOut, "<SCRIPT LANGUAGE=\"JavaScript\">");

    scoutln(out, bytesOut, "function fetch(code){");
    scoutln(out, bytesOut, "var code2=sanitise(code);");
    scoutln(out, bytesOut, "this.location.href=\"http://" + men + "/central/servlet/ProductStockRecord?unm=" + unm + "&sid="
                           + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p1=\"+code2;}");

    scoutln(out, bytesOut, "function qty(code){");
    scoutln(out, bytesOut, "var code2=sanitise(code);");
    scoutln(out, bytesOut, "this.location.href=\"http://" + men + "/central/servlet/MainPageUtils1a?unm=" + unm + "&sid="
                           + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+code2;}");

    scoutln(out, bytesOut, "function page(operation,callType,argA,argB,topOrBottom){");
    scoutln(out, bytesOut, "var srchStr='';/*if(operation=='S'){*/  if(topOrBottom=='T')srchStr=document.forms[0].srchStr1.value;");
    scoutln(out, bytesOut, "else srchStr=document.forms[0].srchStr2.value;  /*}///*/    ");
    scoutln(out, bytesOut, "var maxRows;if(topOrBottom=='T')maxRows=document.forms[0].maxRows1.value;");
    scoutln(out, bytesOut, "else maxRows=document.forms[0].maxRows2.value;");
    scoutln(out, bytesOut, "var arg=sanitise(argA);var arg2=sanitise(argB);");
    scoutln(out, bytesOut, "if(callType=='G'||callType=='S')this.location.replace(\"/central/servlet/CatalogListingPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + inx
        + "&p2=\"+operation+\"&p3=\"+arg+\"&p4=\"+arg2+\"&p5=\"+srchStr+\"&p6=" + newFirstRecNum + "&p10=" + numRecs
        + "&p8=\"+callType+\"&p9=\"+maxRows+\"&p7=" + newLastRecNum + "\");");
    scoutln(out, bytesOut, "else this.location.href=\"/central/servlet/CatalogListingPage?unm=" + unm + "&sid=" + sid + "&uty="
        + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + inx
        + "&p2=\"+operation+\"&p3=\"+arg+\"&p4=\"+arg2+\"&p5=\"+srchStr+\"&p6=" + newFirstRecNum + "&p10=" + numRecs
        + "&p8=\"+callType+\"&p9=\"+maxRows+\"&p7=" + newLastRecNum + "\";}");

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

    int[] hmenuCount = new int[1];

    drawingUtils.outputPageFrame(con, stmt, rs, out, req, "2006", "", "CatalogListingPage", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<form>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void startBody(boolean first, PrintWriter out, char inx, String imagesDir, int[] bytesOut) throws Exception
  {
    if(first)
      scoutln(out, bytesOut, "<table id=\"page\" border=0 cellspacing=2 cellpadding=0>");
    else scoutln(out, bytesOut, "<tr><td><img src=\"" + imagesDir + "z402.gif\"></td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap><p>Status &nbsp;</td><td nowrap><p>Web &nbsp;</td><td nowrap><p>Quantity&nbsp;</td>");

    switch (inx)
    {
      case '1' : scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Item Code &nbsp;</td>");
                 scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Description &nbsp;</td>");
                 scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Manufacturer &nbsp;</td>");
                 scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Manufacturer Code</td>");
                 scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Category Code</td></tr>");
                 break;
      case '2' : scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Description &nbsp;</td>");
                 scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Item Code &nbsp;</td>");
                 scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Manufacturer &nbsp;</td>");
                 scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Manufacturer Code</td>");
                 scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Category Code</td></tr>");
                 break;
      case '3' : scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Manufacturer &nbsp;</td>");
                 scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Manufacturer Code &nbsp;</td>");
                 scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Item Code &nbsp;</td>");
                 scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Description</td>");
                 scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Category Code</td></tr>");
                 break;
      case '4' : scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Manufacturer Code &nbsp;</td>");
                 scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Manufacturer &nbsp;</td>");
                 scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Item Code &nbsp;</td>");
                 scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Description</td>");
                 scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Category Code</td></tr>");
                 break;
      case '5' : scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Alternate Item Code 1&nbsp;</td>");
                 scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Item Code &nbsp;</td>");
                 scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Manufacturer &nbsp;</td>");
                 scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Manufacturer Code &nbsp;</td>");
                 scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Description</td>");
                 scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Category Code</td></tr>");
                 break;
      case '6' : scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Alternate Item Code 2&nbsp;</td>");
                 scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Item Code &nbsp;</td>");
                 scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Manufacturer &nbsp;</td>");
                 scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Manufacturer Code &nbsp;</td>");
                 scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Description</td>");
                 scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Category Code</td></tr>");
                 break;
      case '7' : scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Alternate Item Code 3&nbsp;</td>");
                 scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Item Code &nbsp;</td>");
                 scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Manufacturer &nbsp;</td>");
                 scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Manufacturer Code &nbsp;</td>");
                 scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Description</td>");
                 scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Category Code</td></tr>");
                 break;
      case '8' : scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Alternate Item Code 4&nbsp;</td>");
                 scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Item Code &nbsp;</td>");
                 scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Manufacturer &nbsp;</td>");
                 scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Manufacturer Code &nbsp;</td>");
                 scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Description</td>");
                 scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>Category Code</td></tr>");
                 break;
    }

    scoutln(out, bytesOut, "<tr><td><img src=\"" + imagesDir + "z402.gif\"></td></tr>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void startBody2(boolean first, PrintWriter out, char inx, String imagesDir, int[] bytesOut) throws Exception
  {
    if(first) scoutln(out, bytesOut, "<table id=\"page\" border=0 cellspacing=0 cellpadding=0><tr>");

    scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap>");

    switch (inx)
    {
      case '2' : scoutln(out, bytesOut, "Description");            break;
      case '3' : scoutln(out, bytesOut, "Manufacturer");           break;
      case '4' : scoutln(out, bytesOut, "Manufacturer Code");      break;
      case '5' : scoutln(out, bytesOut, "Alternate Item Code 1");  break;
      case '6' : scoutln(out, bytesOut, "Alternate Item Code 2");  break;
      case '7' : scoutln(out, bytesOut, "Alternate Item Code 3");  break;
      case '8' : scoutln(out, bytesOut, "Alternate Item Code 4");  break;
    }

    scoutln(out, bytesOut, " &nbsp;</b></td></tr>");
    scoutln(out, bytesOut, "<tr><td><img src=\"" + imagesDir + "z402.gif\"></td></tr>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void appendBodyLine(String[] html, char inx, String one, String two, String three, String four, String five, String six, String code,
                              String status, String showToWeb, String imagesDir) throws Exception
  {
    String s = checkCode(code);

    html[0] += "<tr>";

    if(status.equals("C"))
    {
      html[0] += "<td align=center><img src=\"" + imagesDir + "z0212.gif\" border=0></td>";
      if(showToWeb.equals("Y"))
        html[0] += "<td align=center><img src=\"" + imagesDir + "z0030.gif\" border=0></td>";
      else html[0] += "<td></td>";
    }
    else
    {
      html[0] += "<td></td>";
      if(showToWeb.equals("Y"))
        html[0] += "<td align=center><img src=\"" + imagesDir + "z0030.gif\" border=0></td>";
      else html[0] += "<td></td>";
    }

    html[0] += "<td align=center><p><font size=\"3\"><a href=\"javascript:qty('" + s + "')\"><b>Q</b></a></font></td>";

    switch (inx)
    {
      case '1' : html[0] += "<td height=18 nowrap><p><a href=\"javascript:fetch('" + s + "')\">" + one + "</a>&nbsp;</td>";
                 html[0] += "<td nowrap><p>" + two + "&nbsp;</td>";
                 html[0] += "<td nowrap><p>" + three + "&nbsp;</td>";
                 html[0] += "<td nowrap><p>" + four + "&nbsp;</td>";
                 html[0] += "<td nowrap align=center><p>" + six + "&nbsp;</td></tr>\n";
                 break;
      case '2' : html[0] += "<td height=18 nowrap><p>" + one + "&nbsp;</td>";
                 html[0] += "<td nowrap><p><a href=\"javascript:fetch('" + s + "')\">" + two + "</a>&nbsp;</td>";
                 html[0] += "<td nowrap><p>" + three + "&nbsp;</td>";
                 html[0] += "<td nowrap><p>" + four + "&nbsp;</td>";
                 html[0] += "<td nowrap align=center><p>" + six + "&nbsp;</td></tr>\n";
                 break;
      case '3' :
      case '4' : html[0] += "<td height=18 nowrap><p>" + one + "&nbsp;</td>";
                 html[0] += "<td nowrap><p>" + two + "&nbsp;</td>";
                 html[0] += "<td nowrap><p><a href=\"javascript:fetch('" + s + "')\">" + three + "</a>&nbsp;</td>";
                 html[0] += "<td nowrap><p>" + four + "&nbsp;</td>";
                 html[0] += "<td nowrap align=center><p>" + six + "&nbsp;</td></tr>\n";
                 break;
      case '5' :
      case '6' :
      case '7' :
      case '8' : html[0] += "<td height=18 nowrap><p>" + one + "&nbsp;</td>";
                 html[0] += "<td nowrap><p><a href=\"javascript:fetch('" + s + "')\">" + two + "</a>&nbsp;</td>";
                 html[0] += "<td nowrap><p>" + three + "&nbsp;</td>";
                 html[0] += "<td nowrap><p>" + four + "&nbsp;</td>";
                 html[0] += "<td nowrap><p>" + five + "&nbsp;</td>";
                 html[0] += "<td nowrap align=center><p>" + six + "&nbsp;</td></tr>\n";
                 break;
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void appendBodyLine2(String[] html, char callType, char operation, String one) throws Exception
  {
    String s;
    if(one == null || one.length() == 0)
    {
      one = "&lt;none&gt;";
      s = "<none>";
    }
    else s = checkCode(one);

    html[0] += "<tr><td height=18 nowrap>";
    if(callType == 'G' || operation == 'S')
      html[0] += "<a href=\"javascript:page('X','D','" + s + "','')\">";
    else html[0] += "<a href=\"javascript:fetch('" + s + "')\">";

    html[0] += "<font face=\"Arial, Helvetica\" size=2>" + one + "</a>&nbsp;</td></tr>\r\n";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void prependBodyLine(String[] html, char inx, String one, String two, String three, String four, String five, String six, String code,
                               String status, String showToWeb, String imagesDir) throws Exception
  {
    String s = checkCode(code);

    String line = "<tr>";
    
    if(status.equals("C"))
    {
      line += "<td align=center><img src=\"" + imagesDir + "z0212.gif\" border=0></td>";
      if(showToWeb.equals("Y"))
        line += "<td align=center><img src=\"" + imagesDir + "z0030.gif\" border=0></td>";
      else line += "<td></td>";
    }
    else
    {
      line = "<td></td>";
      if(showToWeb.equals("Y"))
        line += "<td align=center><img src=\"" + imagesDir + "z0030.gif\" border=0></td>";
      else line += "<td></td>";
    }

    line += "<td align=center><p><font size=\"3\"><a href=\"javascript:qty('" + s + "')\"><b>Q</b></a></font></td>";

    switch (inx)
    {
      case '1' : line += "<td height=18 nowrap><a href=\"javascript:fetch('" + s + "')\"><p>" + one + "</a>&nbsp;</td>";
                 line += "<td nowrap><p>" + two + "&nbsp;</td>";
                 line += "<td nowrap><p>" + three + "&nbsp;</td>";
                 line += "<td nowrap><p>" + four + "&nbsp;</td>";
                 line += "<td nowrap align=center><p>" + six + "&nbsp;</td>";
                 html[0] = line + "</tr>\n" + html[0];
                 break;
      case '2' : line += "<td height=18 nowrap><p>" + one + "&nbsp;</td>";
                 line += "<td nowrap><a href=\"javascript:fetch('" + s + "')\"><p>" + two + "</a>&nbsp;</td>";
                 line += "<td nowrap><p>" + three + "&nbsp;</td>";
                 line += "<td nowrap><p>" + four + "&nbsp;</td>";
                 line += "<td nowrap align=center><p>" + six + "&nbsp;</td>";
                 html[0] = line + "</tr>\n" + html[0];
                 break;
      case '3' :
      case '4' : line += "<td height=18 nowrap><p>" + one + "&nbsp;</td>";
                 line += "<td nowrap><p>" + two + "&nbsp;</td>";
                 line += "<td nowrap><a href=\"javascript:fetch('" + s + "')\"><p>" + three + "</a>&nbsp;</td>";
                 line += "<td nowrap><p>" + four + "&nbsp;</td>";
                 line += "<td nowrap align=center><p>" + six + "&nbsp;</td>";
                 html[0] = line + "</tr>\n" + html[0];
                 break;
      case '5' :
      case '6' :
      case '7' :
      case '8' : line += "<td height=18 nowrap><p>" + one + "&nbsp;</td>";
                 line += "<td nowrap><a href=\"javascript:fetch('" + s + "')\"><p>" + two + "</a>&nbsp;</td>";
                 line += "<td nowrap><p>" + three + "&nbsp;</td>";
                 line += "<td nowrap><p>" + four + "&nbsp;</td>";
                 line += "<td nowrap><p>" + five + "&nbsp;</td>";
                 line += "<td nowrap align=center><p>" + six + "&nbsp;</td>";
                 html[0] = line + "</tr>\n" + html[0];
                 break;
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void prependBodyLine2(String[] html, char callType, String one) throws Exception
  {
    String s;
    if(one == null || one.length() == 0)
    {
      one = "&lt;none&gt;";
      s = "<none>";
    }
    else s = checkCode(one);

    String line = "<tr><td height=18 nowrap>";
    if(callType == 'G')
      line += "<a href=\"javascript:page('X','D','" + s + "','')\">";
    else line += "<a href=\"javascript:fetch('" + s + "')\">";

    line += "<font face=\"Arial, Helvetica\" size=2>" + one + "</a>&nbsp;</td></tr>\r\n";

    html[0] = line + html[0];
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private String checkCode(String code) throws Exception
  {
    String s = "";
    if(code.indexOf('\'') != -1)
    {
      int len = code.length();
      for(int x = 0; x < len; ++x)
      {
        if(code.charAt(x) == '\'')
          s += "\\'";
        else s += code.charAt(x);
      }
    }
    else s = code;
    return s;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private String getValue(int colNum, char type, ResultSet rs, ResultSetMetaData rsmd)
  {
    if(colNum < 0) return "";

    try
    {
      Integer f;
      java.sql.Date d;
      Time t;

      String str = "";

      switch (rsmd.getColumnType(colNum))
      {
        case java.sql.Types.CHAR :    str = rs.getString(colNum);
                                      str = generalUtils.stripTrailingSpacesStr(str);
                                      break;
        case java.sql.Types.INTEGER : f = rs.getInt(colNum);
                                      str = f.toString();
                                      break;
        case 91                     : if(type == 'D')
                                      {
                                        d = rs.getDate(colNum);
                                        str = d.toString();
                                      }
                                      else
                                      {
                                        t = rs.getTime(colNum);
                                        str = t.toString();
                                      }
                                      break;
        case 93                     : d = rs.getDate(colNum);
                                      str = d.toString();
                                      break;
        case -1                     : str = rs.getString(colNum);
                                      str = generalUtils.stripTrailingSpacesStr(str);
                                      break;
      }

      return str;
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }

    return "";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {
    out.println(str);
    bytesOut[0] += (str.length() + 2);
  }

}
