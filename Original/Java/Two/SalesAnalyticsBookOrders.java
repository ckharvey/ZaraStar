// =======================================================================================================================================================================================================
// System: ZaraStar: Sales Analytics - Book Orders
// Module: SalesAnalyticsBookOrders.java
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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.io.*;

public class SalesAnalyticsBookOrders extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  OrderConfirmation orderConfirmation = new OrderConfirmation();
  SalesOrder salesOrder = new SalesOrder();
  DrawingUtils drawingUtils = new DrawingUtils();
  AccountsUtils accountsUtils = new AccountsUtils();
  
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
 
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "SalesAnalyticsBookOrders", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1201, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    
    String userName = directoryUtils.getMySQLUserName();
    String passWord = directoryUtils.getMySQLPassWord();
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + userName + "&password=" + passWord);

    Statement stmt = null, stmt2 = null, stmt3 = null;
    ResultSet rs = null, rs2 = null, rs3 = null;
    
    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 1201, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "SalesAnalyticsBookOrders", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1201, bytesOut[0], 0, "ACC:");      
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "SalesAnalyticsBookOrders", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1201, bytesOut[0], 0, "SID:");      
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    set(con, stmt, stmt2, stmt3, rs, rs2, rs3, out, req, unm, sid, uty, men, den, dnm, bnm, imagesDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 1201, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, ResultSet rs3, PrintWriter out, HttpServletRequest req, String unm,
                   String sid, String uty, String men, String den, String dnm, String bnm, String imagesDir, String localDefnsDir, String defnsDir,
                   int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Sales: Book Orders</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4031, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewSO(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/SalesOrderPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den
                           + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
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
  
    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");
    
    scoutln(out, bytesOut, "<script language=\"JavaScript\">"); 
    
    // text/HTML of the hints
    scoutln(out, bytesOut, "var HINTS_ITEMS = [ 'details' ];");
    scoutln(out, bytesOut, "var myHint;");
    
    // fetch rec for edit
    scoutln(out, bytesOut, "var req2;");
    scoutln(out, bytesOut, "function initRequest2(url)");
    scoutln(out, bytesOut, "{if(window.XMLHttpRequest){req2=new XMLHttpRequest();}else");
    scoutln(out, bytesOut, "if(window.ActiveXObject){req2=new ActiveXObject(\"Microsoft.XMLHTTP\");}}");

    scoutln(out, bytesOut, "function details(soCode){");
    scoutln(out, bytesOut, "var url = \"http://" + men + "/central/servlet/BackOrdersHintDetails?unm=\" + escape('" + unm + "') + \"&sid=\" + escape('" + sid
                         + "') + \"&uty=\" + escape('" + uty + "') + \"&men=\" + escape('" + men + "') + \"&den=\" + escape('" + den
                         + "') + \"&bnm=\" + escape('" + bnm + "') + \"&p1=\" + soCode + \"&dnm=\" + escape('" + dnm + "');");
    scoutln(out, bytesOut, "initRequest2(url);");
    scoutln(out, bytesOut, "req2.onreadystatechange=processRequest2;");
    scoutln(out, bytesOut, "req2.open(\"GET\",url,true);");
    scoutln(out, bytesOut, "req2.send(null);}");

    scoutln(out, bytesOut, "function processRequest2(){");
    scoutln(out, bytesOut, "if(req2.readyState==4){");
    scoutln(out, bytesOut, "if(req2.status == 200){");
    scoutln(out, bytesOut, "var res=req2.responseXML.getElementsByTagName(\"res\")[0].childNodes[0].nodeValue;");
    scoutln(out, bytesOut, "if(res.length > 0){");
    scoutln(out, bytesOut, "if(res=='.'){");
    scoutln(out, bytesOut, "var hint = req2.responseXML.getElementsByTagName(\"hint\")[0].childNodes[0].nodeValue;");
    scoutln(out, bytesOut, "var messageElement = document.getElementById('hints');");
    scoutln(out, bytesOut, "messageElement.innerHTML=layout(hint);");
    scoutln(out, bytesOut, "document.getElementById('hints').style.height = (15 * numRows(hint));");
    scoutln(out, bytesOut, "document.getElementById('hints').style.width = (7 * numCols(hint));");
    scoutln(out, bytesOut, "}}}}}");

    scoutln(out, bytesOut, "function layout(s){");
    scoutln(out, bytesOut, "var x=0,len=s.length,t='';");
    scoutln(out, bytesOut, "while(x < len)");
    scoutln(out, bytesOut, "{");
    scoutln(out, bytesOut, "if(s.charAt(x) == '`')");
    scoutln(out, bytesOut, "  t += '<br />';");
    scoutln(out, bytesOut, "else t += s.charAt(x);");
    scoutln(out, bytesOut, "++x; }");
    scoutln(out, bytesOut, "return t;");
    scoutln(out, bytesOut, "}");
    scoutln(out, bytesOut, "");
    scoutln(out, bytesOut, "");
    scoutln(out, bytesOut, "");
    scoutln(out, bytesOut, "");
    
    scoutln(out, bytesOut, "function numRows(s){");
    scoutln(out, bytesOut, "var x=0,len=s.length,n=1;");
    scoutln(out, bytesOut, "while(x < len)");
    scoutln(out, bytesOut, "{");
    scoutln(out, bytesOut, "if(s.charAt(x) == '`')");
    scoutln(out, bytesOut, "  ++n;");
    scoutln(out, bytesOut, "++x; }");
    scoutln(out, bytesOut, "return n;");
    scoutln(out, bytesOut, "}");
    
    scoutln(out, bytesOut, "function numCols(s){");
    scoutln(out, bytesOut, "var x=0,len=s.length,n=0,y=0;");
    scoutln(out, bytesOut, "while(x < len)");
    scoutln(out, bytesOut, "{");
    scoutln(out, bytesOut, "if(s.charAt(x) == '`')");
    scoutln(out, bytesOut, "{ if(y > n) n = y; y = 0;}else ++y;");
    scoutln(out, bytesOut, "++x; }");
    scoutln(out, bytesOut, "if(y > n)n=y;return n;");
    scoutln(out, bytesOut, "}");
 
    scoutln(out, bytesOut, "</script>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\" src=\"" + directoryUtils.getSupportDirs('S') + "tigra_hints.js\"></script></head>");
    
    int[] hmenuCount = new int[1];

    drawingUtils.outputPageFrame(con, stmt, rs, out, req, "1201", "", "SalesAnalyticsBookOrders", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount, bytesOut);
    
    scoutln(out, bytesOut, "    <script language=\"JavaScript\">");

    // configuration variable for the hint object, these setting will be shared among all hints created by this object
    scoutln(out, bytesOut, "var HINTS_CFG = { 'wise'       : true," +
                "	'margin'     : 10," +
                "	'gap'        : 0," +
                "	'align'      : 'tcbc'," +
                "	'css'        : 'hintsClass'," +
                "	'show_delay' : 100," +
                "	'hide_delay' : 100," +
                "   'follow'     : false," +
                "	'z-index'    : 100," +
                "	'IEfix'      : false," +
                "	'IEtrans'    : ['blendTrans(DURATION=.3)', 'blendTrans(DURATION=.3)']," +
                "	'opacity'    : 100 " +
                "};");
    scoutln(out, bytesOut, "</script><script language=\"JavaScript\">myHint = new THints (HINTS_ITEMS, HINTS_CFG);</script>");

    scoutln(out, bytesOut, "<div id=\"hints\" style=\"position:absolute;border:2px dotted red;top:100px;left:100px;z-index:1000;"
               + "visibility:hidden;width:10px;height:10px; font-family: tahoma, verdana, arial; font-size: 12px; background-color: #f0f0f0; color: #000000;"
                + "padding: 5px;\"></div>");

    drawingUtils.drawTitle(out, false, false, "SalesAnalyticsBookOrders", "", "Sales: Book Orders", "1201", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<table id=\"page\" width=100% border=0>");

    scoutln(out, bytesOut, "<tr><td></td></tr><tr id=\"pageColumn\"><td></td>");

    scoutln(out, bytesOut, "<td><p><b> SO Code </b></td>");
    scoutln(out, bytesOut, "<td><p><b> SO Date </b></td>");
    scoutln(out, bytesOut, "<td><p><b> En</b></td>");
    scoutln(out, bytesOut, "<td><p><b> Pr</b></td>");
    scoutln(out, bytesOut, "<td><p><b> Sc</b></td>");
    scoutln(out, bytesOut, "<td><p><b> Ma</b></td>");
    scoutln(out, bytesOut, "<td><p><b> Co</b></td>");
    scoutln(out, bytesOut, "<td><p><b> Customer Code</b></td>");
    scoutln(out, bytesOut, "<td><p><b> Name</b></td>");
    scoutln(out, bytesOut, "<td><p><b> Sales Person</b></td>");
    scoutln(out, bytesOut, "<td><p><b> Value</b></td>");
    scoutln(out, bytesOut, "<td><p><b> Currency</b></td></tr>");

    String[] cssFormat = new String[1];  cssFormat[0] = "line1";
    int[] oCount = new int[1];  oCount[0] = 1;

    String[][] currencies = new String[1][];  
    int numCurrencies = accountsUtils.getCurrencyNames(con, stmt, rs, currencies, dnm, localDefnsDir, defnsDir);
    double[] grandTotalTotals = new double[numCurrencies];
    
    forAllSalesOrderHeaders(con, stmt, stmt2, stmt3, rs, rs2, rs3, out, cssFormat, oCount, dnm, localDefnsDir, defnsDir, currencies, numCurrencies,
                            grandTotalTotals, bytesOut);
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "</table><table id=\"page\" border=0>");

    for(int x=0;x<numCurrencies;++x)
    {
      if(generalUtils.doubleDPs(grandTotalTotals[x], '2') != 0.0)
      {
        if(x == 0)
          scout(out, bytesOut, "<tr><td><p>Book Value: &nbsp;&nbsp;</td>");
        else scout(out, bytesOut, "<tr><td></td>");
        scoutln(out, bytesOut, "<td align=right><p>" + currencies[0][x] + "</td><td align=right width=90%><p>"
                + generalUtils.formatNumeric(grandTotalTotals[x], '2') + "</td></tr>");
      }
    }    

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

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
  private void forAllSalesOrderHeaders(Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, ResultSet rs3, PrintWriter out,
                                       String[] cssFormat, int[] oCount, String dnm, String localDefnsDir, String defnsDir, String[][] currencies,
                                       int numCurrencies, double[] grandTotalTotals, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT SalesPerson, SOCode, Date, CompanyCode, TotalTotal, Currency2, CompanyName, SOType, "
                         + "ToEngineering, EngineeringApproved, ToManager, ManagerApproved, ToProcurement, ProcurementConfirmed, ToScheduling, "
                         + "SchedulingConfirmed FROM so WHERE Status != 'C' AND AllSupplied != 'Y' ");
      
    String soCode, date, salesPerson, customerCode, currency, companyName, soType, toEngineering, engineeringApproved, toManager, managerApproved,
           toScheduling, schedulingConfirmed, toProcurement, procurementConfirmed;
    String[] ocDate   = new String[1];
    String[] ocSignOn = new String[1];
    double totalTotal;
    byte[] b = new byte[20];
    int x;

    int todayEncoded = generalUtils.todayEncoded(localDefnsDir, defnsDir);
    
    while(rs.next())
    {    
      salesPerson          = rs.getString(1);
      soCode               = rs.getString(2);
      date                 = rs.getString(3);
      customerCode         = rs.getString(4);
      totalTotal           = generalUtils.doubleFromStr(rs.getString(5));
      currency             = rs.getString(6);
      companyName          = rs.getString(7);
      soType               = rs.getString(8);
      toEngineering        = rs.getString(9);
      engineeringApproved  = rs.getString(10);
      toManager            = rs.getString(11);
      managerApproved      = rs.getString(12);
      toProcurement        = rs.getString(13);
      procurementConfirmed = rs.getString(14);
      toScheduling         = rs.getString(15);
      schedulingConfirmed  = rs.getString(16);
              
      generalUtils.doubleToBytesCharFormat(totalTotal, b, 0);
      generalUtils.bytesDPsGivenSeparator(true, '\000', '2', b, 20, 0);
      generalUtils.formatNumeric(b, '2');

      if(cssFormat[0].equals("line1"))
        cssFormat[0] = "line2";
      else cssFormat[0] = "line1";
 
      scoutln(out, bytesOut, "<tr  id=\"" + cssFormat[0] + "\">");
      scoutln(out, bytesOut, "<td><p>" + oCount[0]++ + "</td>");
      
      scoutln(out, bytesOut, "<td><p><a href=\"javascript:viewSO('" + soCode + "')\" onMouseOver=\"details('" + soCode + "');myHint.show('hints', this)\" onMouseOut=\"myHint.hide()\">" + soCode + "</a></td>");
      
      scoutln(out, bytesOut, "<td><p>" + generalUtils.convertFromYYYYMMDD(date) + "</td>");

      if(soType.equals("T"))
        scout(out, bytesOut, "<td align=center><p>-</td>");
      else        
      if(! toEngineering.equals("Y"))
        scout(out, bytesOut, "<td style={background-color:red;}>&nbsp;</td>");
      else
      {
        if(! engineeringApproved.equals("Y"))
          scout(out, bytesOut, "<td style={background-color:orange;}>&nbsp;</td>");
        else scout(out, bytesOut, "<td style={background-color:green;}>&nbsp;</td>");
      }
      
      if(! toProcurement.equals("Y"))
        scout(out, bytesOut, "<td style={background-color:red;}>&nbsp;</td>");
      else
      {
        if(! procurementConfirmed.equals("Y"))
          scout(out, bytesOut, "<td style={background-color:orange;}>&nbsp;</td>");
        else scout(out, bytesOut, "<td style={background-color:green;}>&nbsp;</td>");
      }
      
      if(soType.equals("T"))
        scout(out, bytesOut, "<td align=center><p>-</td>");
      else        
      if(! toScheduling.equals("Y"))
        scout(out, bytesOut, "<td style={background-color:red;}>&nbsp;</td>");
      else
      {
        if(! schedulingConfirmed.equals("Y"))
          scout(out, bytesOut, "<td style={background-color:orange;}>&nbsp;</td>");
        else scout(out, bytesOut, "<td style={background-color:green;}>&nbsp;</td>");
      }
      
      if(! toManager.equals("Y"))
        scout(out, bytesOut, "<td style={background-color:red;}>&nbsp;</td>");
      else
      {
        if(! managerApproved.equals("Y"))
          scout(out, bytesOut, "<td style={background-color:orange;}>&nbsp;</td>");
        else scout(out, bytesOut, "<td style={background-color:green;}>&nbsp;</td>");
      }
            
      if(orderConfirmation.getOCDetailsGivenSOCode(con, stmt2, rs2, soCode, ocDate, ocSignOn))
        scout(out, bytesOut, "<td style={background-color:green;}>&nbsp;</td>");
      else
      if(salesOrder.confirmationNotNeeded(con, stmt2, rs2, soCode, dnm, localDefnsDir, defnsDir))
        scout(out, bytesOut, "<td align=center><p>-</td>");
      else
      {
        if((todayEncoded - generalUtils.encodeFromYYYYMMDD(date)) > 1)
          scout(out, bytesOut, "<td style={background-color:red;}>&nbsp;</td>");
        else scout(out, bytesOut, "<td style={background-color:black;}>&nbsp;</td>");
      }
            
      scout(out, bytesOut, "<td><p><a href=\"javascript:viewCust('" + customerCode + "')\">" + customerCode + "</a></td>");
      scout(out, bytesOut, "<td nowrap><p>" + companyName + "</td>");
      scout(out, bytesOut, "<td nowrap><p>" + salesPerson + "</td>");
      scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.stringFromBytes(b, 0L) + "</td>");
      scout(out, bytesOut, "<td nowrap><p>" + currency + "</td></tr>");

      if(totalTotal != 0.0)
      {
        for(x=0;x<numCurrencies;++x)
        {
          if(currency.equals(currencies[0][x]))
          {
            grandTotalTotals[x] += totalTotal;
            x = numCurrencies;
          }
        }
      }
    }
  
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean oaExists(String soCode, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    return false;
  }
  
}
