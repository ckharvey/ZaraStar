// =======================================================================================================================================================================================================
// System: ZaraStar Analytic: Purchase Order Delivery Performance
// Module: PurchaseOrderDeliveryExecute.java
// Author: C.K.Harvey
// Copyright (c) 1999-2007 Christopher Harvey. All Rights Reserved.
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
import java.sql.Statement;
import java.io.*;

public class PurchaseOrderDeliveryExecute extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  Supplier supplier = new Supplier();
  MiscDefinitions miscDefinitions = new MiscDefinitions();
  
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="", p3="";

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
      p1  = req.getParameter("p1"); // suppCode
      p2  = req.getParameter("p2"); // dateFrom
      p3  = req.getParameter("p3"); // dateTo

      if(p1 == null) p1 = "";
      if(p2 == null) p2 = "";
      if(p3 == null) p3 = "";

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, p3, bytesOut);
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

      System.out.println(e);
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "PurchaseOrderDeliveryExecute", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1037, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm,
                    String bnm, String p1, String p2, String p3, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String localDefnsDir  = directoryUtils.getLocalOverrideDir(dnm);
    String defnsDir          = directoryUtils.getSupportDirs('D');
    String imagesDir         = directoryUtils.getSupportDirs('I');
    
    String userName = directoryUtils.getMySQLUserName();
    String passWord = directoryUtils.getMySQLPassWord();
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + userName + "&password=" + passWord);
    Statement stmt=null, stmt2=null, stmt3=null;
    ResultSet rs=null, rs2=null, rs3=null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 1037, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "PurchaseOrderDeliveryInput", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1037, bytesOut[0], 0, "ACC:" + p1);
    if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "PurchaseOrderDeliveryInput", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1037, bytesOut[0], 0, "SID:" + p1);
    if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    process(con, stmt, stmt2, stmt3, rs, rs2, rs3, out, req, p1, p2, p3, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 1037, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void process(Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, ResultSet rs3, PrintWriter out, HttpServletRequest req, String p1, String p2, String p3, String unm, String sid, String uty,
                       String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Purchase Order Delivery Performance</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 5006, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function view(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/PurchaseOrderPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men
                           + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
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
    
    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir)
                         + "general.css\"></head>");
  
    int[] hmenuCount = new int[1];
    
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "1037", "", "PurchaseOrderDeliveryInput", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount,
                          bytesOut);
                                
    scoutln(out, bytesOut, "<form>");
  
    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Purchase Order Delivery Performance", "1037", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<p>For Supplier: " + p1 + " - " + supplier.getSupplierNameGivenCode(con, stmt, rs, p1));

    scoutln(out, bytesOut, "<br><table id=\"page\" width=100% border=0>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr id=\"pageColumn\">");
    scoutln(out, bytesOut, "<td><p> PO Code </td>");
    scoutln(out, bytesOut, "<td><p> PO Date </td>");
    scoutln(out, bytesOut, "<td align=center><p> # Items Ordered </td>");
    scoutln(out, bytesOut, "<td align=center><p> Within 30 </td>");
    scoutln(out, bytesOut, "<td align=center><p> Within 60 </td>");
    scoutln(out, bytesOut, "<td align=center><p> Within 90 </td>");
    scoutln(out, bytesOut, "<td align=center><p> Within 120 </td>");
    scoutln(out, bytesOut, "<td align=center><p> Within 150 </td>");
    scoutln(out, bytesOut, "<td align=center><p> Within 180 </td>");
    scoutln(out, bytesOut, "<td align=center><p> Within 210 </td>");
    scoutln(out, bytesOut, "<td align=center><p> Within 240 </td>");
    scoutln(out, bytesOut, "<td align=center><p> Within 270 </td>");
    scoutln(out, bytesOut, "<td align=center><p> Within 300 </td>");
    scoutln(out, bytesOut, "<td align=center><p> Within 330 </td>");
    scoutln(out, bytesOut, "<td align=center><p> Within 360 </td>");
    scoutln(out, bytesOut, "<td align=center><p> Over 360 </td></tr>");

    if(p2.length() != 0 && p3.length() != 0)
    {
      if(p2.length() == 0)
        p2 = "1970-01-01";
      else p2 = generalUtils.convertDateToSQLFormat(p2);
    
      if(p3.length() == 0)
        p3 = "2099-12-31";
      else p3 = generalUtils.convertDateToSQLFormat(p3);
    }

    String tmpTable;

    if(uty.equals("I"))
      tmpTable = unm + "_tmp";
    else
    {
      int i = unm.indexOf("_");
      tmpTable = unm.substring(0, i) + "_tmp";
    }
      
    directoryUtils.createTmpTable(true, con, stmt, "POCode char(20), PODate date, ItemCount integer, Upto30 decimal(19,8), Upto60 decimal(19,8), "
                                  + "Upto90 decimal(19,8), Upto120 decimal(19,8), Upto150 decimal(19,8), Upto180 decimal(19,8), "
                                  + "Upto210 decimal(19,8), Upto240 decimal(19,8), Upto270 decimal(19,8), Upto300 decimal(19,8), "
                                  + "Upto330 decimal(19,8), Upto360 decimal(19,8), Over decimal(19,8)", "", tmpTable);
    
    if(p2.length() != 0 && p3.length() != 0)
    {
      process(con, stmt, stmt2, stmt3, rs, rs2, rs3, p1, p2, p3, localDefnsDir, defnsDir, tmpTable);
      
      processTmpTable(con, stmt, rs, out, tmpTable, localDefnsDir, defnsDir, bytesOut);
    }

    
    scoutln(out, bytesOut, "</table></form>");     scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void process(Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, ResultSet rs3,
                       String supplierCode, String dateFrom, String dateTo, String localDefnsDir, String defnsDir, String tmpTable)
                       throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT POCode, Date FROM po WHERE CompanyCode= '" + supplierCode + "' AND Status != 'C' AND Date >= {d '" + dateFrom 
                         + "'} AND Date <= {d '" + dateTo + "'}");
    
    String poCode, poDate;
    double itemCount;
    
    while(rs.next())
    {
      poCode = rs.getString(1);
      poDate = rs.getString(2);

      itemCount = calculateNumOfItems(con, stmt2, rs2, poCode, localDefnsDir, defnsDir);
      
      addToTmpTable(con, stmt2, poCode, poDate, itemCount, tmpTable);      

      calculateQtyReceived(con, stmt2, stmt3, rs2, rs3, poCode, generalUtils.encodeFromYYYYMMDD(poDate), localDefnsDir, defnsDir, tmpTable);      
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private double calculateNumOfItems(Connection con, Statement stmt, ResultSet rs, String poCode, String localDefnsDir, String defnsDir)
                                     throws Exception
  {
    double itemCount = 0.0;
    
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT sum(Quantity) FROM pol WHERE POCode = '" + poCode + "'");
    
    String s;

    if(rs.next())
    {
      s = rs.getString(1);
      if(s == null)
        s = "0";
      itemCount += generalUtils.doubleFromStr(s);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return itemCount;
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void calculateQtyReceived(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String poCode, int poDateEncoded,
                                    String localDefnsDir, String defnsDir, String tmpTable) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t1.Date, t2.Quantity FROM grl AS t2 INNER JOIN gr AS t1 ON t1.GRCode = t2.GRCode " 
                         + "WHERE t2.POCode = '" + poCode + "' AND t1.Status != 'C'");

    double quantity;
    int grDateEncoded;
    
    while(rs.next())
    {
      grDateEncoded = generalUtils.encodeFromYYYYMMDD(rs.getString(1));
      quantity      = generalUtils.doubleFromStr(rs.getString(2));
     
      updateTmpTable(con, stmt2, rs2, poCode, poDateEncoded, grDateEncoded, quantity, tmpTable);      
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void addToTmpTable(Connection con, Statement stmt, String poCode, String poDate, double itemCount, String tmpTable) throws Exception
  {
    stmt = con.createStatement();
   
    String q = "INSERT INTO " + tmpTable + " ( POCode, PODate, ItemCount, Upto30, Upto60, Upto90, Upto120, Upto150, Upto180, Upto210, Upto240, "
             + "Upto270, Upto300, Upto330, Upto360, Over) "
             + "VALUES ('" + poCode + "', {d '" + poDate + "'}, '" + itemCount + "', '0.0', '0.0', '0.0', '0.0', '0.0', '0.0', '0.0', '0.0', '0.0', "
             + "'0.0', '0.0', '0.0', '0.0' )";

    stmt.executeUpdate(q);
  
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void updateTmpTable(Connection con, Statement stmt, ResultSet rs, String poCode, int poDateEncoded, int grDateEncoded, double quantity,
                              String tmpTable) throws Exception
  {
    stmt = con.createStatement();

    String fld;
    
    if(grDateEncoded <= (poDateEncoded + 30))
      fld = "Upto30";
    else
    if(grDateEncoded <= (poDateEncoded + 60))
      fld = "Upto60";
    else
    if(grDateEncoded <= (poDateEncoded + 90))
      fld = "Upto90";
    else
    if(grDateEncoded <= (poDateEncoded + 120))
      fld = "Upto120";
    else
    if(grDateEncoded <= (poDateEncoded + 150))
      fld = "Upto150";
    else
    if(grDateEncoded <= (poDateEncoded + 180))
      fld = "Upto180";
    else
    if(grDateEncoded <= (poDateEncoded + 210))
      fld = "Upto210";
    else
    if(grDateEncoded <= (poDateEncoded + 240))
      fld = "Upto240";
    else
    if(grDateEncoded <= (poDateEncoded + 270))
      fld = "Upto270";
    else
    if(grDateEncoded <= (poDateEncoded + 300))
      fld = "Upto300";
    else
    if(grDateEncoded <= (poDateEncoded + 330))
      fld = "Upto330";
    else
    if(grDateEncoded <= (poDateEncoded + 360))
      fld = "Upto360";
    else fld = "Over";
    
    rs = stmt.executeQuery("SELECT " + fld + " FROM " + tmpTable + " WHERE POCode = '" + poCode + "'");
    
    double quantitySoFar = 0.0; 
    
    if(rs.next()) // just-in-case
    {
      quantitySoFar = generalUtils.doubleFromStr(rs.getString(1));
      
      quantitySoFar += quantity;
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    stmt = con.createStatement();
    
    String q = "UPDATE " + tmpTable + " SET " + fld + " = " + quantitySoFar + " WHERE POCode = '" + poCode + "'";

    stmt.executeUpdate(q);
  
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void processTmpTable(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String tmpTable, String localDefnsDir,
                               String defnsDir, int[] bytesOut) throws Exception 
  {
    char dpOnQuantities = miscDefinitions.dpOnQuantities(con, stmt, rs, "5");

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT POCode, PODate, ItemCount, Upto30, Upto60, Upto90, Upto120, Upto150, Upto180, Upto210, Upto240, Upto270, Upto300, "
                         + "Upto330, Upto360, Over FROM " + tmpTable + " ORDER BY PODate");

    boolean isGreen = false;
    String poCode, poDate, bgColor;
    double itemCount, upto30, upto60, upto90, upto120, upto150, upto180, upto210, upto240, upto270, upto300, upto330, upto360, over;
    
    while(rs.next())
    {    
      poCode    = rs.getString(1);
      poDate    = rs.getString(2);
      itemCount = generalUtils.doubleFromStr(rs.getString(3));
      upto30    = generalUtils.doubleFromStr(rs.getString(4));
      upto60    = generalUtils.doubleFromStr(rs.getString(5));
      upto90    = generalUtils.doubleFromStr(rs.getString(6));
      upto120   = generalUtils.doubleFromStr(rs.getString(7));
      upto150   = generalUtils.doubleFromStr(rs.getString(8));
      upto180   = generalUtils.doubleFromStr(rs.getString(9));
      upto210   = generalUtils.doubleFromStr(rs.getString(10));
      upto240   = generalUtils.doubleFromStr(rs.getString(11));
      upto270   = generalUtils.doubleFromStr(rs.getString(12));
      upto300   = generalUtils.doubleFromStr(rs.getString(13));
      upto330   = generalUtils.doubleFromStr(rs.getString(14));
      upto360   = generalUtils.doubleFromStr(rs.getString(15));
      over      = generalUtils.doubleFromStr(rs.getString(16));
      
      if(isGreen)
      {
        bgColor = "#FFFFFF";
        isGreen = false;
      }
      else
      {
        bgColor = "#DDF0DD";
        isGreen = true;
      }
      
      if(itemCount == 0) 
        itemCount = 1;
      
      scoutln(out, bytesOut, "<tr bgcolor=" + bgColor + "><td><p><a href=\"javascript:view('" + poCode + "')\">" + poCode +"</a></td>");
      scoutln(out, bytesOut, "<td><p>" + generalUtils.convertFromYYYYMMDD(poDate) + "</td>");

      if((upto30 + upto60 + upto90 + upto120 + upto150 + upto180 + upto210 + upto240 + upto270 + upto300 + upto330 + upto360 + over) < itemCount)
        scoutln(out, bytesOut, "<td align=center><p><font color=red>" + generalUtils.doubleDPs(dpOnQuantities, itemCount) + "</font></td>");
      else scoutln(out, bytesOut, "<td align=center><p>" + generalUtils.doubleDPs(dpOnQuantities, itemCount) + "</td>");

      writeEntry(out, upto30,  itemCount, dpOnQuantities, bytesOut);
      writeEntry(out, upto60,  itemCount, dpOnQuantities, bytesOut);
      writeEntry(out, upto90,  itemCount, dpOnQuantities, bytesOut);
      writeEntry(out, upto120, itemCount, dpOnQuantities, bytesOut);
      writeEntry(out, upto150, itemCount, dpOnQuantities, bytesOut);
      writeEntry(out, upto180, itemCount, dpOnQuantities, bytesOut);
      writeEntry(out, upto210, itemCount, dpOnQuantities, bytesOut);
      writeEntry(out, upto240, itemCount, dpOnQuantities, bytesOut);
      writeEntry(out, upto270, itemCount, dpOnQuantities, bytesOut);
      writeEntry(out, upto300, itemCount, dpOnQuantities, bytesOut);
      writeEntry(out, upto330, itemCount, dpOnQuantities, bytesOut);
      writeEntry(out, upto360, itemCount, dpOnQuantities, bytesOut);
      writeEntry(out, over,    itemCount, dpOnQuantities, bytesOut);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void writeEntry(PrintWriter out, double upto, double itemCount, char dpOnQuantities, int[] bytesOut) throws Exception
  {      
    if(upto == 0.0)
      scoutln(out, bytesOut, "<td align=center><p>-</td>");
    else
    {
      scoutln(out, bytesOut, "<td align=center><p>" + generalUtils.doubleDPs(dpOnQuantities, upto) + " ("
                             + generalUtils.doubleDPs('1', ((upto / itemCount) * 100)) + "%)</td>");
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int[] bytesOut, String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
