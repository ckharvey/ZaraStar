// =======================================================================================================================================================================================================
// System: ZaraStar: Product: List Incomplete Picking Lists and GRNs
// Module: IncompletePickingListExecute.java
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
import java.sql.*;
import java.io.*;

public class IncompletePickingListExecute extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  MiscDefinitions miscDefinitions = new MiscDefinitions();
  PickingList pickingList = new PickingList();

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
      p1  = req.getParameter("p1"); // mfr
      
      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, bytesOut);
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
 
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "IncompletePickingLists", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3064, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush(); 
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String mfr,
                    int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    
    Connection con = null;
    Statement stmt = null;
    ResultSet rs = null;

    String uName = directoryUtils.getMySQLUserName();
    String pWord = directoryUtils.getMySQLPassWord();
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + uName + "&password=" + pWord);

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 3064, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "IncompletePickingLists", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3064, bytesOut[0], 0, "ACC:" + mfr);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "IncompletePickingLists", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3064, bytesOut[0], 0, "SID:" + mfr);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    set(con, stmt, rs, out, req, mfr, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 3064, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), mfr);
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String mfr, String unm, String sid, String uty,
                   String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>List Incomplete Picking Lists and GRNs</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3038, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewPL(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/GoodsReceivedPickingList?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm="
                           + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }
  
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3025, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewGR(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/GoodsReceivedPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm="
                           + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }
  
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3001, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewItem(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/ProductStockRecord?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm="
                           + dnm + "&bnm=" + bnm + "&p2=A&p1=\"+p1;}");
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
  
    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\""
                         + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    int[] hmenuCount = new int[1];
 
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "3064", "", "IncompletePickingLists", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "List Incomplete Picking Lists and GRNs", "3064", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<form><table id=\"page\" width=100% border=0>");

    scoutln(out, bytesOut, "<tr><td></td></tr><tr id=\"pageColumn\"><td></td>");

    scoutln(out, bytesOut, "<td><p><b> PL Code </b></td>");
    scoutln(out, bytesOut, "<td><p><b> PL Line </b></td>");
    scoutln(out, bytesOut, "<td><p><b> PL Date </b></td>");
    scoutln(out, bytesOut, "<td><p><b> Item Code </b></td>");
    scoutln(out, bytesOut, "<td><p><b> Mfr Code </b></td>");
    scoutln(out, bytesOut, "<td><p><b> Quantity<br>Required </b></td>");
    scoutln(out, bytesOut, "<td><p><b> Quantity<br>Packed </b></td>");
    scoutln(out, bytesOut, "<td nowrap><p><b> Description </b></td></tr>");
       
    String[] cssFormat = new String[1];  cssFormat[0] = "line1";
    int[] oCount = new int[1];  oCount[0] = 1;
    char dpOnQuantities = miscDefinitions.dpOnQuantities(con, stmt, rs, "5");
            
    forPLsForAMfr(con, stmt, rs, out, mfr, dpOnQuantities, cssFormat, oCount, localDefnsDir, defnsDir, bytesOut);

    scoutln(out, bytesOut, "</table><table id=\"page\" width=100% border=0>");

    scoutln(out, bytesOut, "<tr><td></td></tr><tr id=\"pageColumn\"><td></td>");

    scoutln(out, bytesOut, "<td><p><b> GRN Code </b></td>");
    scoutln(out, bytesOut, "<td><p><b> GRN Line </b></td>");
    scoutln(out, bytesOut, "<td><p><b> GRN Date </b></td>");
    scoutln(out, bytesOut, "<td><p><b> Item Code </b></td>");
    scoutln(out, bytesOut, "<td><p><b> Mfr Code </b></td>");
    scoutln(out, bytesOut, "<td><p><b> Quantity </b></td>");
    scoutln(out, bytesOut, "<td nowrap><p><b> Description </b></td></tr>");
       
    cssFormat[0] = "line1";
    oCount[0] = 1;
            
    forGRNsForAMfr(con, stmt, rs, out, mfr, dpOnQuantities, cssFormat, oCount, localDefnsDir, defnsDir, bytesOut);

    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void forPLsForAMfr(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String mfr, char dpOnQuantities, String[] cssFormat, int[] oCount,
                             String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t1.PLCode, t1.Date, t2.Description, t2.QuantityRequired, t2.QuantityPacked, t2.Line, t2.ItemCode, "
                         + "t2.ManufacturerCode FROM pll AS t2 INNER JOIN pl AS t1 ON t2.plCode = t1.plCode WHERE t2.Manufacturer = '"
                         + generalUtils.sanitiseForSQL(mfr) + "' AND t1.Status != 'C' AND Completed != 'Y' ORDER BY t1.PLCode");

    String plCode, date, desc, quantityRequired, quantityPacked, line, itemCode, mfrCode;

    while(rs.next())
    {    
      plCode           = rs.getString(1);
      date             = rs.getString(2);
      desc             = rs.getString(3);
      quantityRequired = rs.getString(4);
      quantityPacked   = rs.getString(5);
      line             = rs.getString(6);
      itemCode         = rs.getString(7);
      mfrCode          = rs.getString(8);
      
      processPLLine(out, plCode, line, date, itemCode, quantityRequired, quantityPacked, desc, mfrCode, dpOnQuantities, cssFormat, oCount, bytesOut);
    }
  
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void processPLLine(PrintWriter out, String plCode, String plLine, String plDate, String itemCode, String quantityRequired, String quantityPacked,
                             String desc, String mfrCode, char dpOnQuantities, String[] cssFormat, int[] oCount, int[] bytesOut) throws Exception
  {
    byte[] b  = new byte[20];
    byte[] b2 = new byte[20];
    double qtyReqd = generalUtils.doubleFromStr(quantityRequired);
    double qtyPkd  = generalUtils.doubleFromStr(quantityPacked);
    
    generalUtils.doubleToBytesCharFormat(qtyReqd, b, 0);
    generalUtils.bytesDPsGivenSeparator(true, '\000', dpOnQuantities, b, 20, 0);
    generalUtils.formatNumeric(b, dpOnQuantities);

    generalUtils.doubleToBytesCharFormat(qtyPkd, b2, 0);
    generalUtils.bytesDPsGivenSeparator(true, '\000', dpOnQuantities, b2, 20, 0);
    generalUtils.formatNumeric(b2, dpOnQuantities);
 
    if(cssFormat[0].equals("line1"))
      cssFormat[0] = "line2";
    else cssFormat[0] = "line1";
 
    scoutln(out, bytesOut, "<tr  id=\"" + cssFormat[0] + "\">");

    scoutln(out, bytesOut, "<td><p>" + oCount[0]++ + "</td>");
    scoutln(out, bytesOut, "<td><p><a href=\"javascript:viewPL('" + plCode + "')\">" + plCode + "</a></td>");
    scout(out, bytesOut,   "<td nowrap><p>" + plLine + "</td>");
    scout(out, bytesOut,   "<td><p>" + generalUtils.convertFromYYYYMMDD(plDate) + "</td>");
    scout(out, bytesOut,   "<td><p><a href=\"javascript:viewItem('" + itemCode + "')\">" + itemCode + "</a></td>");
    scout(out, bytesOut,   "<td><p>" + mfrCode + "</td>");
    scoutln(out, bytesOut, "<td align=center><p>" + generalUtils.stringFromBytes(b,  0L) + "</td>");
    scoutln(out, bytesOut, "<td align=center><p>" + generalUtils.stringFromBytes(b2, 0L) + "</td>");
    scoutln(out, bytesOut, "<td nowrap><p>" + desc + "</td></tr>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void forGRNsForAMfr(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String mfr, char dpOnQuantities, String[] cssFormat, int[] oCount,
                              String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t1.GRCode, t1.Date, t2.Description, t2.Quantity, t2.Line, t2.ItemCode, t2.ManufacturerCode "
                         + "FROM grl AS t2 INNER JOIN gr AS t1 ON t2.grCode = t1.grCode WHERE t2.Manufacturer = '"
                         + generalUtils.sanitiseForSQL(mfr) + "' AND t1.Status != 'C' AND t1.StockProcessed != 'Y' ORDER BY t1.GRCode");

    String grCode, date, desc, quantity, line, itemCode, mfrCode;

    while(rs.next())
    {    
      grCode   = rs.getString(1);
      date     = rs.getString(2);
      desc     = rs.getString(3);
      quantity = rs.getString(4);
      line     = rs.getString(5);
      itemCode = rs.getString(6);
      mfrCode  = rs.getString(7);
      
      processGRNLine(out, grCode, line, date, itemCode, quantity, desc, mfrCode, dpOnQuantities, cssFormat, oCount, bytesOut);
    }
  
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void processGRNLine(PrintWriter out, String grCode, String grLine, String grDate, String itemCode, String quantity, String desc, String mfrCode,
                              char dpOnQuantities, String[] cssFormat, int[] oCount, int[] bytesOut) throws Exception
  {
    byte[] b  = new byte[20];
    double qty = generalUtils.doubleFromStr(quantity);
    
    generalUtils.doubleToBytesCharFormat(qty, b, 0);
    generalUtils.bytesDPsGivenSeparator(true, '\000', dpOnQuantities, b, 20, 0);
    generalUtils.formatNumeric(b, dpOnQuantities);
 
    if(cssFormat[0].equals("line1"))
      cssFormat[0] = "line2";
    else cssFormat[0] = "line1";
 
    scoutln(out, bytesOut, "<tr  id=\"" + cssFormat[0] + "\">");

    scoutln(out, bytesOut, "<td><p>" + oCount[0]++ + "</td>");
    scoutln(out, bytesOut, "<td><p><a href=\"javascript:viewGR('" + grCode + "')\">" + grCode + "</a></td>");
    scout(out, bytesOut,   "<td nowrap><p>" + grLine + "</td>");
    scout(out, bytesOut,   "<td><p>" + generalUtils.convertFromYYYYMMDD(grDate) + "</td>");
    scout(out, bytesOut,   "<td><p><a href=\"javascript:viewItem('" + itemCode + "')\">" + itemCode + "</a></td>");
    scout(out, bytesOut,   "<td><p>" + mfrCode + "</td>");
    scoutln(out, bytesOut, "<td align=center><p>" + generalUtils.stringFromBytes(b,  0L) + "</td>");
    scoutln(out, bytesOut, "<td nowrap><p>" + desc + "</td></tr>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
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
