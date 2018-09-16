// =======================================================================================================================================================================================================
// System: ZaraStar Analytic: Supplier Purchases from PO Lines
// Module: ManufacturerPurchasesGenerate.java
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
import java.sql.*;
import java.io.*;

public class ManufacturerPurchasesGenerate extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  Supplier supplier = new Supplier();
  
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="", p3="", p4="", p5="", p6="", p7="", p8="", p9="";

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
      p2  = req.getParameter("p2"); // dateFrom1
      p3  = req.getParameter("p3"); // dateTo1
      p4  = req.getParameter("p4"); // dateFrom2
      p5  = req.getParameter("p5"); // dateTo2
      p6  = req.getParameter("p6"); // dateFrom3
      p7  = req.getParameter("p7"); // dateTo3
      p8  = req.getParameter("p8"); // dateFrom4
      p9  = req.getParameter("p9"); // dateTo4

      if(p1 == null) p1 = "";
      if(p2 == null) p2 = "";
      if(p3 == null) p3 = "";
      if(p4 == null) p4 = "";
      if(p5 == null) p5 = "";
      if(p6 == null) p6 = "";
      if(p7 == null) p7 = "";
      if(p8 == null) p8 = "";
      if(p9 == null) p9 = "";

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, p3, p4, p5, p6, p7, p8, p9, bytesOut);
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
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "ManufacturerPurchasesGenerate", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1036, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm,
                    String bnm, String p1, String p2, String p3, String p4, String p5, String p6, String p7, String p8, String p9, int[] bytesOut)
                    throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String localDefnsDir  = directoryUtils.getLocalOverrideDir(dnm);
    String defnsDir          = directoryUtils.getSupportDirs('D');
    String imagesDir         = directoryUtils.getSupportDirs('I');
    
    String userName = directoryUtils.getMySQLUserName();
    String passWord = directoryUtils.getMySQLPassWord();
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + userName + "&password=" + passWord);
    Statement stmt=null;
    ResultSet rs=null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 1036, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "ManufacturerPurchasesInput", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1036, bytesOut[0], 0, "ACC:" + p1);
    if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "ManufacturerPurchasesInput", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1036, bytesOut[0], 0, "SID:" + p1);
    if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    process(con, stmt, rs, out, req, p1, p2, p3, p4, p5, p6, p7, p8, p9, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 1036, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void process(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String p1, String p2, String p3, String p4, String p5, String p6, String p7,
                       String p8, String p9, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                       String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Supplier Purchases from PO Lines</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    scoutln(out, bytesOut, "function view(dateFrom,dateTo){");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/ManufacturerPurchasesPrint?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men
                         + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + p1 + "&p2=\"+dateFrom+\"&p3=\"+dateTo;}");

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
    
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "1036", "", "ManufacturerPurchasesInput", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount,
                          bytesOut);
                                
    scoutln(out, bytesOut, "<form>");
  
    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Supplier Purchases from PO Lines", "1036", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<p>For Supplier: " + p1 + " - " + supplier.getSupplierNameGivenCode(con, stmt, rs, p1));
    scoutln(out, bytesOut, "<p>" + supplier.getSupplierCurrencyGivenCode(con, stmt, rs, p1));

    scoutln(out, bytesOut, "<br><table id=\"page\" width=100% border=0>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr id=\"pageColumn\">");
    scoutln(out, bytesOut, "<td colspan=2></td>");
    scoutln(out, bytesOut, "<td align=center><p> Gross Total </td>");
    scoutln(out, bytesOut, "<td align=center><p> GST </td>");
    scoutln(out, bytesOut, "<td align=center><p> Net Total </td>");
    scoutln(out, bytesOut, "<td align=center><p> Lines </td></tr>");

    if(p2.length() != 0 && p3.length() != 0)
    {
      if(p2.length() == 0)
        p2 = "1970-01-01";
      else p2 = generalUtils.convertDateToSQLFormat(p2);
    
      if(p3.length() == 0)
        p3 = "2099-12-31";
      else p3 = generalUtils.convertDateToSQLFormat(p3);
    }
    
    if(p4.length() != 0 && p5.length() != 0)
    {
      if(p4.length() == 0)
        p4 = "1970-01-01";
      else p4 = generalUtils.convertDateToSQLFormat(p4);
    
      if(p5.length() == 0)
        p5 = "2099-12-31";
      else p5 = generalUtils.convertDateToSQLFormat(p5);
    }
    
    if(p6.length() != 0 && p7.length() != 0)
    {
      if(p6.length() == 0)
        p6 = "1970-01-01";
      else p6 = generalUtils.convertDateToSQLFormat(p6);
    
      if(p7.length() == 0)
        p7 = "2099-12-31";
      else p7 = generalUtils.convertDateToSQLFormat(p7);
    }
    
    if(p8.length() != 0 && p9.length() != 0)
    {
      if(p8.length() == 0)
        p8 = "1970-01-01";
      else p8 = generalUtils.convertDateToSQLFormat(p8);
    
      if(p9.length() == 0)
        p9 = "2099-12-31";
      else p9 = generalUtils.convertDateToSQLFormat(p9);
    }
    
    double[] totalTotal = new double[1]; 
    double[] gstTotal   = new double[1]; 
    

    int count;
    
    if(p2.length() != 0 && p3.length() != 0)
    {
      count  = calculatePOs(con, stmt, rs, p1, p2, p3, localDefnsDir, defnsDir, totalTotal, gstTotal);
      count += calculateLPs(con, stmt, rs, p1, p2, p3, localDefnsDir, defnsDir, totalTotal, gstTotal);
      
      writeForARange(out, p2, p3, count, totalTotal[0], gstTotal[0], bytesOut);
    }

    if(p4.length() != 0 && p5.length() != 0)
    {
      count  = calculatePOs(con, stmt, rs, p1, p4, p5, localDefnsDir, defnsDir, totalTotal, gstTotal);
      count += calculateLPs(con, stmt, rs, p1, p4, p5, localDefnsDir, defnsDir, totalTotal, gstTotal);
      
      writeForARange(out, p4, p5, count, totalTotal[0], gstTotal[0], bytesOut);
    }

    if(p6.length() != 0 && p7.length() != 0)
    {
      count  = calculatePOs(con, stmt, rs, p1, p6, p7, localDefnsDir, defnsDir, totalTotal, gstTotal);
      count += calculateLPs(con, stmt, rs, p1, p6, p7, localDefnsDir, defnsDir, totalTotal, gstTotal);
      
      writeForARange(out, p6, p7, count, totalTotal[0], gstTotal[0], bytesOut);
    }

    if(p8.length() != 0 && p9.length() != 0)
    {
      count  = calculatePOs(con, stmt, rs, p1, p8, p9, localDefnsDir, defnsDir, totalTotal, gstTotal);
      count += calculateLPs(con, stmt, rs, p1, p8, p9, localDefnsDir, defnsDir, totalTotal, gstTotal);
      
      writeForARange(out, p8, p9, count, totalTotal[0], gstTotal[0], bytesOut);
    }

    
    scoutln(out, bytesOut, "</table></form>");     scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private int calculatePOs(Connection con, Statement stmt, ResultSet rs, String supplierCode, String dateFrom, String dateTo,
                           String localDefnsDir, String defnsDir, double[] totalTotal, double[] gstTotal) throws Exception
  {
    int count = 0;
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT TotalTotal, GSTTotal FROM po WHERE CompanyCode= '" + supplierCode
                         + "' AND Status != 'C' AND Date >= {d '" + dateFrom + "'} AND Date <= {d '" + dateTo + "'}");
    while(rs.next())
    {
      totalTotal[0] += generalUtils.doubleFromStr(rs.getString(1));
      gstTotal[0]   += generalUtils.doubleFromStr(rs.getString(2));
      ++count;
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return count;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private int calculateLPs(Connection con, Statement stmt, ResultSet rs, String supplierCode, String dateFrom, String dateTo,
                           String localDefnsDir, String defnsDir, double[] totalTotal, double[] gstTotal) throws Exception
  {
    int count = 0;
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT TotalTotal, GSTTotal FROM lp WHERE CompanyCode= '" + supplierCode
                         + "' AND Status != 'C' AND Date >= {d '" + dateFrom + "'} AND Date <= {d '" + dateTo + "'}");
    while(rs.next())
    {
      totalTotal[0] += generalUtils.doubleFromStr(rs.getString(1));
      gstTotal[0]   += generalUtils.doubleFromStr(rs.getString(2));
      ++count;
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    return count;
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void writeForARange(PrintWriter out, String dateFrom, String dateTo, int count, double totalTotal, double gstTotal, int[] bytesOut)
                              throws Exception
  {
    scoutln(out, bytesOut, "<tr><td><p>From " + generalUtils.convertFromYYYYMMDD(dateFrom) + "</td>");
    scoutln(out, bytesOut, "<td><p> to " + generalUtils.convertFromYYYYMMDD(dateTo) + "</td>");
    
    scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.doubleDPs('2', totalTotal) + "</td>");
    scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.doubleDPs('2', gstTotal) + "</td>");
    scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.doubleDPs('2', (totalTotal - gstTotal)) + "</td>");

    scoutln(out, bytesOut, "<td align=right><p><a href=\"javascript:view('" + dateFrom + "','" + dateTo + "')\">View " + count + " POs/LPs</a></td></tr>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
