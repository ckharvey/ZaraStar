// =======================================================================================================================================================================================================
// System: ZaraStar: Document trace (cost-of-sale) - do it
// Module: DocumentTraceCostOfSalea.java
// Author: C.K.Harvey
// Copyright (c) 2003-09 Christopher Harvey. All Rights Reserved.
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
import java.util.*;
import java.io.*;

public class DocumentTraceCostOfSale extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  AccountsUtils accountsUtils = new AccountsUtils();
  Inventory inventory = new Inventory();
  DefinitionTables definitionTables = new DefinitionTables();
  MiscDefinitions miscDefinitions = new MiscDefinitions();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
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
      p1  = req.getParameter("p1"); // doCode
      
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
 
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "DocumentTraceCostOfSale", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1039, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush(); 
    }
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", doCode="";

    try
    {
      directoryUtils.setContentHeaders2(res);
      out = res.getWriter();

      Enumeration en = req.getParameterNames();
      while(en.hasMoreElements())
      {
        String name = (String)en.nextElement();
        String[] value = req.getParameterValues(name);
        if(name.equals("unm"))
          unm = value[0];
        else
        if(name.equals("sid"))
          sid = value[0];
        else
        if(name.equals("uty"))
          uty = value[0];
        else
        if(name.equals("men"))
          men = value[0];
        else
        if(name.equals("den"))
          den = value[0];
        else
        if(name.equals("dnm"))
          dnm = value[0];
        else
        if(name.equals("bnm"))
          bnm = value[0];
        else
        if(name.equals("p1"))
          doCode = value[0];
      }
      
      if(doCode == null) doCode = "";
      
      doIt(out, req, doCode, unm, sid, uty, men, den, dnm, bnm, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "DocumentTraceCostOfSalea", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1039, bytesOut[0], 0, "ERR:" + doCode);
      if(out != null) out.flush(); 
    }
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String doCode, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String localDefnsDir  = directoryUtils.getLocalOverrideDir(dnm);
    String defnsDir       = directoryUtils.getSupportDirs('D');
    String imagesDir      = directoryUtils.getSupportDirs('I');
    
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    ResultSet rs = null, rs2 = null, rs3 = null, rs4 = null;
    Statement stmt = null, stmt2 = null, stmt3 = null, stmt4 = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 1039, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "DocumentTraceCostOfSale", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1039, bytesOut[0], 0, "ACC:" + doCode);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "DocumentTraceCostOfSale", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1039, bytesOut[0], 0, "SID:" + doCode);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    doCode = generalUtils.stripLeadingAndTrailingSpaces(doCode);
    
    process(con, stmt, stmt2, stmt3, stmt4, rs, rs2, rs3, rs4, out, req, doCode, unm, sid, uty, men, den, dnm, bnm, imagesDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 1039, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), doCode);
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void process(Connection con, Statement stmt, Statement stmt2, Statement stmt3, Statement stmt4, ResultSet rs, ResultSet rs2, ResultSet rs3, ResultSet rs4, PrintWriter out, HttpServletRequest req, String doCode,
                       String unm, String sid, String uty, String men, String den, String dnm, String bnm, String imagesDir, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Cost Trace</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 148, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewDO(code){");
      scoutln(out, bytesOut, "var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/DeliveryOrderPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 139, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewPO(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/PurchaseOrderPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }
    
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 152, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewLP(code){");
      scoutln(out, bytesOut, "var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/LocalPurchasePage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
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
  
    int[] hmenuCount = new int[1];
    
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "1039", "", "DocumentTraceCostOfSale", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<form>");
  
    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Cost Trace", "1039",  unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    scout(out, bytesOut, "Delivery Order");

    scoutln(out, bytesOut, ": &nbsp; &nbsp; <a href=\"javascript:viewDO('" + doCode + "')\">" + doCode + "</a>");
    
    scoutln(out, bytesOut, "<br><br><table id=\"page\" width=100%>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    forEachDOLine(con, stmt, stmt2, rs, rs2, out, doCode, bytesOut);
    
    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }   
        
  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
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

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void forEachDOLine(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, String doCode, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.Line, t2.SOCode, t2.SOLine, t2.UnitPrice, t2.ItemCode, t2.Quantity, t2.Description, t1.Currency FROM dol AS t2 INNER JOIN do AS t1 ON t1.DOCode = t2.DOCode "
                         + "WHERE t2.DOCode = '" + doCode + "'");

    double unitPrice, diff, pc;
    boolean first = true, found, isPO, isLP;
    String itemCode, soCode, soLine, qty, desc, currency, doLine, cssFormat = "";
    double[] unitPricePO = new double[1];
    String[] currencyPO  = new String[1];
    String[] itemCodePO  = new String[1];
    String[] qtyPO       = new String[1];
    String[] poCodePO    = new String[1];
    String[] poLinePO    = new String[1];
    String[] poDatePO    = new String[1];

    while(rs.next())
    {
      doLine    = rs.getString(1);
      soCode    = rs.getString(2);
      soLine    = rs.getString(3);
      unitPrice = generalUtils.doubleFromStr(generalUtils.doubleDPs(rs.getString(4), '2'));
      itemCode  = rs.getString(5);
      qty       = rs.getString(6);
      desc      = rs.getString(7);
      currency  = rs.getString(8);

      found = isPO = isLP = false;
      if(getFromPOGivenSOCode(con, stmt2, rs2, soCode, soLine, unitPricePO, currencyPO, itemCodePO, qtyPO, poCodePO, poLinePO, poDatePO))
        isPO = found = true;
      else
      if(getFromLPGivenSOCode(con, stmt2, rs2, soCode, soLine, unitPricePO, currencyPO, itemCodePO, qtyPO, poCodePO, poLinePO, poDatePO))
        isLP = found = true;

      if(first)
      {
        scoutln(out, bytesOut, "<tr id=\"pageColumn\"><td align=center><p>DO Line</td>");
        scoutln(out, bytesOut, "<td align=center><p>Markup</td>");
        scoutln(out, bytesOut, "<td><p>DO ItemCode</td>");
        scoutln(out, bytesOut, "<td><p>DO Quantity</td>");
        scoutln(out, bytesOut, "<td><p>DO Description</td>");
        scoutln(out, bytesOut, "<td><p>DO Currency</td>");
        scoutln(out, bytesOut, "<td><p>DO UnitPrice</td>");
        if(isPO) scoutln(out, bytesOut, "<td><p>PO Code</td>"); else scoutln(out, bytesOut, "<td><p>LP Code</td>");
        if(isPO) scoutln(out, bytesOut, "<td><p>PO Line</td>"); else scoutln(out, bytesOut, "<td><p>LP Line</td>");
        if(isPO) scoutln(out, bytesOut, "<td><p>PO Currency</td>"); else scoutln(out, bytesOut, "<td><p>LP Currency</td>");
        if(isPO) scoutln(out, bytesOut, "<td><p>PO UnitPrice</td>"); else scoutln(out, bytesOut, "<td><p>LP UnitPrice</td>");
        if(isPO) scoutln(out, bytesOut, "<td><p>PO Quantity</td>"); else scoutln(out, bytesOut, "<td><p>LP Quantity</td>");
        if(isPO) scoutln(out, bytesOut, "<td><p>PO ItemCode</td>"); else scoutln(out, bytesOut, "<td><p>LP ItemCode</td>");
        scoutln(out, bytesOut, "</tr>");
        first = false;
      }

      if(cssFormat.equals("line1")) cssFormat = "line2"; else cssFormat = "line1";

      scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\"><td align=center><p>" + doLine + "</td>");

      if(currency.equals(currencyPO[0]))
      {
        diff = unitPrice - unitPricePO[0];
        if(unitPricePO[0] != 0.0)
          pc = (diff / unitPricePO[0]) * 100;
        else pc = 0;
        scoutln(out, bytesOut, "<td align=center><p>" + generalUtils.doubleDPs('2', diff) + " (" + generalUtils.doubleDPs('2', pc) + "%)</td>");
      }
      else scoutln(out, bytesOut, "<td align=center><p>-</td>");

      scoutln(out, bytesOut, "<td><p>" + itemCode + "</td>");
      scoutln(out, bytesOut, "<td><p>" + generalUtils.doubleDPs(qty, '0') + "</td>");
      scoutln(out, bytesOut, "<td><p>" + desc + "</td>");
      scoutln(out, bytesOut, "<td><p>" + currency + "</td>");
      scoutln(out, bytesOut, "<td><p>" +  generalUtils.doubleDPs('2', unitPrice) + "</td>");

      if(found)
      {
        if(isPO)
          scoutln(out, bytesOut, "<td><p><a href=\"javascript:viewPO('" + poCodePO[0] + "')\">" + poCodePO[0] + "</td>");
        else scoutln(out, bytesOut, "<td><p><a href=\"javascript:viewLP('" + poCodePO[0] + "')\">" + poCodePO[0] + "</td>");
        scoutln(out, bytesOut, "<td><p>" + poLinePO[0] + "</td>");
        scoutln(out, bytesOut, "<td><p>" + currencyPO[0] + "</td>");
        scoutln(out, bytesOut, "<td><p>" + generalUtils.doubleDPs('2', unitPricePO[0]) + "</td>");
        scoutln(out, bytesOut, "<td><p>" + generalUtils.doubleDPs(qtyPO[0], '0') + "</td>");
        scoutln(out, bytesOut, "<td><p>" + itemCodePO[0] + "</td>");
      }

      scoutln(out, bytesOut, "</tr>");
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean getFromPOGivenSOCode(Connection con, Statement stmt, ResultSet rs, String soCode, String soLine, double[] unitPrice, String[] currency, String[] itemCode, String[] qty, String[] poCode, String[] poLine, String[] poDate)
                                       throws Exception
  {
    boolean found = false;

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.UnitPrice, t1.Currency, t2.ItemCode, t2.Quantity, t2.POCode, t2.Line, t1.Date FROM pol AS t2 INNER JOIN po AS t1 ON t1.POCode = t2.POCode "
                         + "WHERE t2.SOCode = '" + soCode + "' AND t2.SOLine = '" + soLine + "' AND t1.Status != 'C'");

    if(rs.next())
    {
      unitPrice[0] = generalUtils.doubleFromStr(generalUtils.doubleDPs(rs.getString(1), '2'));
      currency[0]  = rs.getString(2);
      itemCode[0]  = rs.getString(3);
      qty[0]       = rs.getString(4);
      poCode[0]    = rs.getString(5);
      poLine[0]    = rs.getString(6);
      poDate[0]    = rs.getString(7);

      found = true;
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    return found;
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean getFromLPGivenSOCode(Connection con, Statement stmt, ResultSet rs, String soCode, String soLine, double[] unitPrice, String[] currency, String[] itemCode, String[] qty, String[] poCode, String[] poLine, String[] poDate)
                                       throws Exception
  {
    boolean found = false;

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.UnitPrice, t1.Currency, t2.ItemCode, t2.Quantity, t2.LPCode, t2.Line, t1.Date FROM lpl AS t2 INNER JOIN lp AS t1 ON t1.LPCode = t2.LPCode "
                         + "WHERE t2.SOCode = '" + soCode + "' AND t2.SOLine = '" + soLine + "' AND t1.Status != 'C'");

    if(rs.next())
    {
      unitPrice[0] = generalUtils.doubleFromStr(generalUtils.doubleDPs(rs.getString(1), '2'));
      currency[0]  = rs.getString(2);
      itemCode[0]  = rs.getString(3);
      qty[0]       = rs.getString(4);
      poCode[0]    = rs.getString(5);
      poLine[0]    = rs.getString(6);
      poDate[0]    = rs.getString(7);

      found = true;
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    return found;
  }

}
