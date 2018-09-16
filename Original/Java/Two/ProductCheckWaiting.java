// =======================================================================================================================================================================================================
// System: ZaraStar: Product: Check waiting orders for an item
// Module: ProductCheckWaiting.java
// Author: C.K.Harvey
// Copyright (c) 2000-08 Christopher Harvey. All Rights Reserved.
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

public class ProductCheckWaiting extends HttpServlet
{
  GeneralUtils  generalUtils = new GeneralUtils();
  MessagePage  messagePage = new MessagePage();
  MiscDefinitions  miscDefinitions = new MiscDefinitions();
  ServerUtils  serverUtils = new ServerUtils();
  AuthenticationUtils  authenticationUtils = new AuthenticationUtils();
  PageFrameUtils  pageFrameUtils = new PageFrameUtils();
  DirectoryUtils  directoryUtils = new DirectoryUtils();
  PickingList  pickingList = new PickingList();
  Inventory  inventory = new Inventory();
  
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
      sid = req.getParameter("sid");
      uty = req.getParameter("uty");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");
      p1  = req.getParameter("p1"); // grCode
      
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

      System.out.println("3024: " + e);
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "ProductCheckWaiting", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3024, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                    String p1, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);

    Statement stmt = null, stmt2 = null, stmt3 = null;
    ResultSet rs = null, rs2 = null, rs3 = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 3024, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "ProductCheckWaiting", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3024, bytesOut[0], 0, "ACC:" + p1);
      if(con  != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "ProductCheckWaiting", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3024, bytesOut[0], 0, "SID:" + p1);
      if(con  != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    set(con, stmt, stmt2, stmt3, rs, rs2, rs3, out, req, p1, unm, sid, uty, men, den, dnm, bnm, imagesDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 3024, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con  != null) con.close();
    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, ResultSet rs3, PrintWriter out,
                   HttpServletRequest req, String grnCode, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                   String imagesDir, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Check Waiting Orders for an Item</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4031, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewSO(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/SalesOrderPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den
                           + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 5006, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewPO(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/PurchaseOrderPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den
                           + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3001, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewItem(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/ProductStockRecord?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den
                           + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p1=\"+p1;}");
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
 
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "3024", "", "ProductCheckWaiting", unm, sid, uty, men, den, dnm, bnm, localDefnsDir,
                          defnsDir, hmenuCount, bytesOut);
  
    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Check Waiting Orders for an Item", "3024", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<p>For Goods Received Note: &nbsp; &nbsp; " + grnCode);

    scoutln(out, bytesOut, "<br><table id=\"page\" width=100% border=0>");

    scoutln(out, bytesOut, "<tr id=\"pageColumn\">");
    scoutln(out, bytesOut, "<td><p>GRN Line &nbsp;</td>");
    scoutln(out, bytesOut, "<td><p>Item Code &nbsp;</td>");
    scoutln(out, bytesOut, "<td><p>Mfr &nbsp;</td>");
    scoutln(out, bytesOut, "<td><p>Mfr Code &nbsp;</td>");
    scoutln(out, bytesOut, "<td><p>GRN Quantity &nbsp;</td>");
    scoutln(out, bytesOut, "<td><p>PO Code &nbsp;</td>");
    scoutln(out, bytesOut, "<td><p>&nbsp;</td>");
    scoutln(out, bytesOut, "<td><p>SO Code &nbsp;</td>");
    scoutln(out, bytesOut, "<td><p>SO Date &nbsp;</td>");
    scoutln(out, bytesOut, "<td><p>SO Quantity O/S &nbsp;</td>");
    scoutln(out, bytesOut, "<td><p>Delivery Date &nbsp;</td>");
    scoutln(out, bytesOut, "<td><p>SalesPerson &nbsp;</td>");
    scoutln(out, bytesOut, "<td><p>Company Name &nbsp;</td>");
    scoutln(out, bytesOut, "<td><p>Description &nbsp;</td></tr>");

    char dpOnQuantities = miscDefinitions.dpOnQuantities(con, stmt, rs, "5");

    eachGRNLine(con, stmt, stmt2, stmt3, rs, rs2, rs3, out, grnCode, dnm, localDefnsDir, defnsDir, dpOnQuantities, bytesOut);

    scoutln(out, bytesOut, "</table></form>");     scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void eachGRNLine(Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, ResultSet rs3,
                           PrintWriter out, String grnCode, String dnm, String localDefnsDir, String defnsDir, char dpOnQuantities, int[] bytesOut)
                           throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT Line, ItemCode, Quantity, POCode, POLine FROM grl WHERE GRCode = '" + grnCode + "' ORDER BY Line");

    String grnLine, itemCode, qty, poCode, poLine, soCode;
    
    String[] cssFormat = new String[1];  cssFormat[0] = "line2";

    while(rs.next())
    {    
      grnLine  = rs.getString(1);
      itemCode = rs.getString(2);
      qty      = rs.getString(3);
      poCode   = rs.getString(4);
      poLine   = rs.getString(5);

      if(inventory.existsItemRecGivenCode(con, stmt2, rs2, itemCode))
      {
        soCode = getSOCodeFromPOLine(con, stmt2, rs2, poCode, poLine);
        onSO(con, stmt2, stmt3, rs2, rs3, out, grnLine, itemCode, qty, poCode, soCode, dnm, localDefnsDir, defnsDir, dpOnQuantities, cssFormat, bytesOut);
      }
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void onSO(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, String grnLine, String itemCode,
                    String grnQty, String poCode, String soCodeFromPOLine, String dnm, String localDefnsDir, String defnsDir, char dpOnQuantities,
                    String[] cssFormat, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.SOCode, t2.Line, t2.Quantity, t2.DeliveryDate, t2.Description, t1.Date, t1.SalesPerson, t1.CompanyName, "
                         + "t2.Manufacturer, t2.ManufacturerCode FROM sol AS t2 INNER JOIN so AS t1 ON t2.SOCode = t1.SOCode WHERE t2.ItemCode = '"
                         + itemCode + "' AND t1.Status != 'C' AND t1.AllSupplied != 'Y' ORDER BY t1.Date, t2.SOCode, t2.Line");

    String soCode, soLine, date, deliveryDate, desc, salesPerson, companyName, mfr, mfrCode, lastGRNLine = "";
    double qty, actualQty;
    byte[] b = new byte[20];

    while(rs.next())
    {    
      soCode       = rs.getString(1);
      soLine       = rs.getString(2);
      qty          = generalUtils.doubleFromStr(rs.getString(3));
      deliveryDate = rs.getString(4);
      desc         = rs.getString(5);
      date         = rs.getString(6);
      salesPerson  = rs.getString(7);
      companyName  = rs.getString(8);
      mfr          = rs.getString(9);
      mfrCode      = rs.getString(10);

      actualQty = pickingList.getTotalPickedForASOLine(con, stmt2, rs2, soCode, soLine);

      if(actualQty < qty)
      {
        generalUtils.doubleToBytesCharFormat((qty - actualQty), b, 0);
        generalUtils.bytesDPsGivenSeparator(true, '\000', dpOnQuantities, b, 20, 0);
        generalUtils.formatNumeric(b, dpOnQuantities);

        if(cssFormat[0].equals("line1"))
          cssFormat[0] = "line2";
        else cssFormat[0] = "line1";

        if(! grnLine.equals(lastGRNLine))
        {
          scoutln(out, bytesOut, "<tr id=\"" + cssFormat[0] + "\"><td nowrap align=center><p>" + grnLine
                               + "</td><td><p><a href=\"javascript:viewItem('" + itemCode + "')\">" + itemCode + "</a></td>");

          scoutln(out, bytesOut, "<td nowrap><p>" + mfr + "</td>");
          scoutln(out, bytesOut, "<td nowrap><p>" + mfrCode + "</td>");
          
          scoutln(out, bytesOut, "<td align=center><p>" + generalUtils.doubleDPs(grnQty, dpOnQuantities) + "</td>");
          scoutln(out, bytesOut, "<td><p><a href=\"javascript:viewPO('" + poCode + "')\">" + poCode + "</a></td>");
          lastGRNLine = grnLine;
        }
        else scoutln(out, bytesOut, "<tr id=\"" + cssFormat[0] + "\"><td colspan=6>&nbsp;</td>");
        
        if(soCode.length() > 0 && soCode.equals(soCodeFromPOLine))
          scoutln(out, bytesOut, "<td align=center><p>*</td>");
        else scoutln(out, bytesOut, "<td>&nbsp;</td>");          
                
        scoutln(out, bytesOut, "<td><p><a href=\"javascript:viewSO('" + soCode + "')\">" + soCode + "</a></td>");
        scoutln(out, bytesOut, "<td><p>" + generalUtils.convertFromYYYYMMDD(date) + "</td>");
        scoutln(out, bytesOut, "<td align=center><p>" + generalUtils.stringFromBytes(b, 0L) + "</td>");
        scoutln(out, bytesOut, "<td><p>" + generalUtils.convertFromYYYYMMDD(deliveryDate) + "</td>");
        scoutln(out, bytesOut, "<td nowrap><p>" + salesPerson +"</td>");
        scoutln(out, bytesOut, "<td nowrap><p>" + companyName +"</td>");
        scoutln(out, bytesOut, "<td nowrap><p>" + desc +"</td></tr>");
      }
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private String getSOCodeFromPOLine(Connection con, Statement stmt, ResultSet rs, String poCode, String poLine) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT SOCode FROM pol WHERE POCode = '" + poCode + "' AND Line = '" + poLine + "'");

    String soCode = "";
    
    if(rs.next())
      soCode = rs.getString(1);
        
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return soCode;
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}

