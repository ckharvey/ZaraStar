// =======================================================================================================================================================================================================
// System: ZaraStar: Sales Orders Processing - Workshop Manager
// Module: SalesOrderWorkshop.java
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
import java.sql.ResultSet;
import java.sql.Statement;
import java.io.*;

public class SalesOrderWorkshop extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  MiscDefinitions miscDefinitions = new MiscDefinitions();
  DrawingUtils drawingUtils = new DrawingUtils();
  
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="";

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
      
      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, bytesOut);
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
 
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "SalesOrderWorkshop", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2035, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm,
                    String bnm, int[] bytesOut) throws Exception
  {
    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 2035, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "SalesOrderWorkshop", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2035, bytesOut[0], 0, "ACC:");
      
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "SalesOrderWorkshop", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2035, bytesOut[0], 0, "SID:");
      
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    set(con, stmt, rs, out, req, unm, sid, uty, men, den, dnm, bnm, imagesDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.etotalBytes(req, unm, dnm, 2035, bytesOut[0], 0, "");
      
      if(con != null) con.close();
    if(out != null) out.flush(); 
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm,
                   String bnm, String imagesDir, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Sales Order Processing</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4031, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewSO(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/SalesOrderPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men
                           + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4001, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewCust(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/CustomerPage?unm=" + unm + "&sid=" + sid + "&uty="
                           + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p1=\"+p1;}");
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
                         + "general.css\">");

    int[] hmenuCount = new int[1];

    drawingUtils.outputPageFrame(con, stmt, rs, out, req, "2035", "", "SalesOrderWorkshop", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount, bytesOut);

    drawingUtils.drawTitle(out, false, false, "SalesOrderWorkshop", "", "Sales Order Processing - Workshop Manager", "2035", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<form><table id=\"page\" width=100% border=0>");

    scoutln(out, bytesOut, "<tr><td></td></tr><tr id=\"pageColumn\"><td></td>");

    scoutln(out, bytesOut, "<td><p><b> Scheduled for Production </b></td>");
    scoutln(out, bytesOut, "<td><p><b> Production Completed </b></td>");
    scoutln(out, bytesOut, "<td><p><b> Approved for Dispatch </b></td>");
    scoutln(out, bytesOut, "<td><p><b> Customer Code </b></td>");
    scoutln(out, bytesOut, "<td><p><b> Customer Name </b></td>");
    scoutln(out, bytesOut, "<td><p><b> Customer POCode </b></td>");
    scoutln(out, bytesOut, "<td><p><b> SO Code </b></td>");
    scoutln(out, bytesOut, "<td><p><b> WO Code </b></td>");
    scoutln(out, bytesOut, "<td><p><b> WO Date </b></td>");
    scoutln(out, bytesOut, "<td><p><b> Amount </b></td>");
    scoutln(out, bytesOut, "<td><p><b> Currency </b></td></tr>");

    String[] cssFormat = new String[1];  cssFormat[0] = "line1";
    int[] oCount = new int[1];  oCount[0] = 1;
    char dpOnQuantities = miscDefinitions.dpOnQuantities(con, stmt, rs, "5");
    
    forAllSalesOrderHeaders(con, stmt, rs, out, dpOnQuantities, cssFormat, oCount, dnm, localDefnsDir, defnsDir, bytesOut);
    
    scoutln(out, bytesOut, "</table></form>");     scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
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
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void forAllSalesOrderHeaders(Connection con, Statement stmt, ResultSet rs, PrintWriter out, char dpOnQuantities, String[] cssFormat, int[] oCount, String dnm,
                                       String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT SOCode, Date, CompanyCode, CustomerPOCode, TotalTotal, Currency2, CompanyName, ToEngineering, "
                         + "EngineeringApproved, ManagerApprovalRequested, ManagerApproved "
                         + "FROM so WHERE Status != 'C' AND AllSupplied != 'Y'     AND Date > {d '2007-07-01'} "); ////////////
      
    String soCode, date, customerCode, custPOCode, currency, companyName, toEngineering, engineeringApproved, managerApprovalRequested,
           managerApproved;
    double totalTotal;
    byte[] b = new byte[20];
    
    while(rs.next())
    {    
      soCode       = rs.getString(1);
      date         = rs.getString(2);
      customerCode = rs.getString(3);
      custPOCode   = rs.getString(4);
      totalTotal   = generalUtils.doubleFromStr(rs.getString(5));
      currency     = rs.getString(6);
      companyName  = rs.getString(7);
      toEngineering  = rs.getString(8);
      engineeringApproved  = rs.getString(9);
      managerApprovalRequested  = rs.getString(10);
      managerApproved  = rs.getString(11);
      
      generalUtils.doubleToBytesCharFormat(totalTotal, b, 0);
      generalUtils.bytesDPsGivenSeparator(true, '\000', '2', b, 20, 0);
      generalUtils.formatNumeric(b, '2');

      if(cssFormat[0].equals("line1"))
        cssFormat[0] = "line2";
      else cssFormat[0] = "line1";
 
      scoutln(out, bytesOut, "<tr  id=\"" + cssFormat[0] + "\">");

      scoutln(out, bytesOut, "<td><p>" + oCount[0]++ + "</td>");


      if(soCode.indexOf("3") == -1 && soCode.indexOf("4") == -1)
        scout(out, bytesOut, "<td align=center><p><input type=checkbox></td>");
      else scout(out, bytesOut, "<td align=center><p>Scheduled</td>");
      
      if(soCode.indexOf("1") == -1 && soCode.indexOf("2") == -1)
        scout(out, bytesOut, "<td align=center><p><input type=checkbox></td>");
      else scout(out, bytesOut, "<td align=center><p>Completed</td>");
      
     
      scout(out, bytesOut, "<td align=center><p><input type=checkbox></td>");
      
      
      scout(out, bytesOut, "<td><p><a href=\"javascript:viewCust('" + customerCode + "')\">" + customerCode + "</a></td>");
      scout(out, bytesOut, "<td nowrap><p>" + companyName + "</td>");
      scout(out, bytesOut, "<td nowrap><p>" + custPOCode + "</td>");
      scoutln(out, bytesOut, "<td><p><a href=\"javascript:viewSO('" + soCode + "')\">" + soCode + "</a></td>");
      scoutln(out, bytesOut, "<td><p><a href=\"javascript:viewSO('" + soCode + "')\">" + soCode + "</a></td>");
      scout(out, bytesOut, "<td><p>" + generalUtils.convertFromYYYYMMDD(date) + "</td>");
      scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.stringFromBytes(b, 0L) + "</td>");
      scout(out, bytesOut, "<td nowrap><p>" + currency + "</td></tr>");
    }
  
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

}
