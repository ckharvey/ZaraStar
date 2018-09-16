// =======================================================================================================================================================================================================
// System: ZaraStar: Generate packing list
// Module: GoodsReceivedPackingList.java
// Author: C.K.Harvey
// Copyright (c) 2001-10 Christopher Harvey. All Rights Reserved.
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

public class GoodsReceivedPackingList extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  DashboardUtils dashboardUtils = new DashboardUtils();
  MiscDefinitions miscDefinitions = new MiscDefinitions();
  DeliveryOrder deliveryOrder = new DeliveryOrder();
  SalesOrder salesOrder = new SalesOrder();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", men="", den="", uty="", dnm="", bnm="", p1="", p2="";

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
      p1  = req.getParameter("p1");
      p2  = req.getParameter("p2"); // plain or not

      if(p1 == null) p1 = "";
      if(p2 == null) p2 = "P"; // default to friendly
  
      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "_3041", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3041, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1, String p2, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');
    
    Connection con = null;
    Statement stmt = null;
    ResultSet rs   = null;
    
    try
    {
      String uName    = directoryUtils.getMySQLUserName();
      String passWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();
    
      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + uName + "&password=" + passWord);

      if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 4031, unm, uty, dnm, localDefnsDir, defnsDir)) // 4031 = SO
      {
        messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "3041", imagesDir, localDefnsDir, defnsDir, bytesOut);
        serverUtils.etotalBytes(req, unm, dnm, 3041, bytesOut[0], 0, "ACC:" + p1);
        if(con != null) con.close();
        if(out != null) out.flush(); 
        return;
      }
    
      if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
      {
        messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "3041", imagesDir, localDefnsDir, defnsDir, bytesOut);
        serverUtils.etotalBytes(req, unm, dnm, 3041, bytesOut[0], 0, "SID:" + p1);
      
        if(con != null) con.close();
        if(out != null) out.flush(); 
        return;
      }

      boolean plain = false;
      if(p2.equals("P"))
        plain = true;

      process(con, stmt, rs, out, req, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, p1, plain, bytesOut);
    }
    catch(Exception e) { }

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 3041, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
       
   if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void process(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir,
                       String soCode, boolean plain, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Generate Packing List</title>");

    if(plain)
      scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"print\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "documentPlain2.css\">");
    else scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    byte[] code = new byte[21];
    generalUtils.strToBytes(code, soCode);

    outputPageFrame(con, stmt, rs, out, req, plain, soCode, "", "Packing List", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, bytesOut);

    create(con, stmt, rs, out, soCode, code, dnm, localDefnsDir, defnsDir, bytesOut);

    if(! plain)
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void create(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String soCode, byte[] code, String dnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    generalUtils.toUpper(code, 0);

    byte[] data   = new byte[3000];

    byte[] linesData  = new byte[2000];
    int[]  listLen    = new int[1];  listLen[0] = 2000;
    int[]  linesCount = new int[1];

    if(salesOrder.getRecGivenCode(con, stmt, rs, code, '\000', data, dnm, localDefnsDir, defnsDir, bytesOut) == -1)
      return;

    char dpOnQuantities = miscDefinitions.dpOnQuantities(con, stmt, rs, "5");

    outputHeader(out, soCode, data, localDefnsDir, defnsDir, bytesOut);

    scoutln(out, bytesOut, "<table id=\"page\" width=100%>");

    scoutln(out, bytesOut, "<tr><td><p><b>Line</td><td><p><b>Qty</td><td><p><b>ItemCode</td><td><p><b>MfrCode</td><td><p><b>Description</td><td><p><b>Delivery</td><td><p><b>DeliveryDate</td><td><p><b>Qty Packed</td><td><p><b>Package No.</td><td><p><b>Picking List</td>"
                         + "<td><p><b>POCode</td><td><p><b>Supplier</td></tr>");

    // fetch lines data in one go
    linesData = salesOrder.getLines(con, stmt, rs, code, '\000', linesData, listLen, linesCount, dnm, localDefnsDir, defnsDir);

    double amtReqd, amtDeliveredAlready, quantityReqd;
    String thisLine;
    for(int xx=0;xx<linesCount[0];++xx)
    {
      if(generalUtils.getListEntryByNum(xx, linesData, data)) // just-in-case
      {
        generalUtils.replaceTwosWithNulls(data);
        thisLine = generalUtils.dfsAsStr(data, (short)22);
        amtReqd = generalUtils.dfsAsDouble(data, (short)5);
        amtDeliveredAlready = deliveryOrder.getTotalDeliveredForASOLine(con, stmt, rs, soCode, thisLine);//generalUtils.intToStr(xx));

        quantityReqd = (amtReqd - amtDeliveredAlready);

        if(quantityReqd > 0)
          outputLine(con, stmt, rs, out, soCode, thisLine, quantityReqd, data, dpOnQuantities, bytesOut);
      }
    }

    scoutln(out, bytesOut, "</table>");
    outputFooter(out, bytesOut);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void outputHeader(PrintWriter out, String soCode, byte[] data, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    String soDate      = generalUtils.dfsAsStr(data, (short)1);
    String companyCode = generalUtils.dfsAsStr(data, (short)2);
    String companyName = generalUtils.dfsAsStr(data, (short)3);
    String custPOCode  = generalUtils.dfsAsStr(data, (short)39);
    String salesPerson = generalUtils.dfsAsStr(data, (short)38);
    String addr1       = generalUtils.dfsAsStr(data, (short)4);
    String addr2       = generalUtils.dfsAsStr(data, (short)5);
    String addr3       = generalUtils.dfsAsStr(data, (short)6);
    String addr4       = generalUtils.dfsAsStr(data, (short)7);
    String addr5       = generalUtils.dfsAsStr(data, (short)8);
    String postCode    = generalUtils.dfsAsStr(data, (short)9);
    String shipName    = generalUtils.dfsAsStr(data, (short)32);
    String shipAddr1   = generalUtils.dfsAsStr(data, (short)33);
    String shipAddr2   = generalUtils.dfsAsStr(data, (short)34);
    String shipAddr3   = generalUtils.dfsAsStr(data, (short)35);
    String shipAddr4   = generalUtils.dfsAsStr(data, (short)36);
    String shipAddr5   = generalUtils.dfsAsStr(data, (short)37);

    scoutln(out, bytesOut, "<table id=header width=100%>");

    scoutln(out, bytesOut, "<tr><td colspan=2><p><b>PACKING LIST for Sales Order: " + soCode +"</b> (dated " + soDate + ")</td></tr>");
    scoutln(out, bytesOut, "<tr><td><p>Bill To:</td><td nowrap><p>" + companyName + " (" + companyCode + ")</td>");
    scoutln(out, bytesOut, "<td><p>Ship To:</td><td width=90% nowrap><p>" + shipName + "</td></tr>");

    scoutln(out, bytesOut, "<tr><td><p>Address:</td><td width=50% nowrap><p>" + addr1 + "</td>");
    scoutln(out, bytesOut, "<td><p>Address:</td><td width=90% nowrap><p>" + shipAddr1 + "</td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td><p>" + addr2 + "</td>");
    scoutln(out, bytesOut, "<td></td><td><p>" + shipAddr2 + "</td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td><p>" + addr3 + "</td>");
    scoutln(out, bytesOut, "<td></td><td><p>" + shipAddr3 + "</td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td><p>" + addr4 + "</td>");
    scoutln(out, bytesOut, "<td></td><td><p>" + shipAddr4 + "</td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td><p>" + addr5 + "</td>");
    scoutln(out, bytesOut, "<td></td><td><p>" + shipAddr5 + "</td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td><p>" + postCode + "</td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap><p>Customer PO Code: </td><td nowrap><p>" + custPOCode + "</td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap><p>SalesPerson: </td><td nowrap><p>" + salesPerson + "</td></tr>");
    scoutln(out, bytesOut, "<tr><td><p>Printed:</td><td nowrap><p>" + generalUtils.timeStamp(localDefnsDir, defnsDir) + "</td></tr>");

    scoutln(out, bytesOut, "</table>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void outputFooter(PrintWriter out, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<table id=footer width=100%>");

    scoutln(out, bytesOut, "<tr><td nowrap><p>Packed By: &nbsp; ___________________________________ &nbsp;&nbsp;&nbsp;&nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Date: &nbsp; ___________________________________</td>");
    scoutln(out, bytesOut, "<td nowrap width=90%><p>");
    scoutln(out, bytesOut, "<table><tr><td>Packing Required &nbsp;</td><td style='border-style:solid; height:20; width:20; border-width:1;'>&nbsp;</td></tr></table>");
    scoutln(out, bytesOut, "</td></tr>");

    scoutln(out, bytesOut, "</table>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void outputLine(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String soCode, String soLine, double qty, byte[] data, char dpOnQuantities, int[] bytesOut) throws Exception
  {
    String entry    = generalUtils.dfsAsStr(data, (short)23);
    String itemCode = generalUtils.dfsAsStr(data, (short)2);
    String mfrCode  = generalUtils.dfsAsStr(data, (short)31);
    String desc     = generalUtils.dfsAsStr(data, (short)3);
    String uom      = generalUtils.dfsAsStr(data, (short)21);
    String deliveryDate = generalUtils.convertFromYYYYMMDD(generalUtils.dfsAsStr(data, (short)25));

    String[] qtyOnPO        = new String[1];
    String[] deliveryMethod = new String[1];
    String[] supplierName   = new String[1];
    String[] poOrLPCode     = new String[1];

    qtyOnPO[0] = "0";
    deliveryMethod[0] = supplierName[0] = poOrLPCode[0] = "";

    calcDetailsPO(con, stmt, rs, soCode, soLine, qtyOnPO, deliveryMethod, supplierName, poOrLPCode);
    calcDetailsLP(con, stmt, rs, soCode, soLine, qtyOnPO, deliveryMethod, supplierName, poOrLPCode);

    String plCodes = detPL(con, stmt, rs, soCode, soLine);

    desc += addMulipleLines(con, stmt, rs, soCode, soLine);//entry);

    scoutln(out, bytesOut, "<tr><td valign=top><p>" + entry + "</td><td valign=top><p>" + generalUtils.formatNumeric(qty, dpOnQuantities) + " " + uom + "</td><td valign=top><p>" + itemCode + "</td><td valign=top><p> " + mfrCode + "</td><td><p> " + desc
                         + "</td><td valign=top><p> " + deliveryMethod[0] + "</td><td valign=top><p> " + deliveryDate + "</td><td valign=top><p>&nbsp;&nbsp;&nbsp;&nbsp;</td><td><p>&nbsp;&nbsp;&nbsp;&nbsp;</td>");
    scoutln(out, bytesOut, "<td valign=top><p>" + plCodes + "</td><td valign=top><p>" + poOrLPCode[0] + "</td><td valign=top><p>" + supplierName[0] + "</td></tr>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String addMulipleLines(Connection con, Statement stmt, ResultSet rs, String soCode, String entry) throws Exception
  {
    String s = "";

    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT Text FROM soll WHERE SOCode = '" + soCode + "' AND Entry = '" + entry + "' ORDER BY Line");

      while(rs.next())
        s += ("<br>" + rs.getString(1));
    }
    catch(Exception e)
    {
      System.out.println(e);
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    return s;
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void calcDetailsPO(Connection con, Statement stmt, ResultSet rs, String soCode, String soLine, String[] qty, String[] deliveryMethod, String[] supplierName, String[] poCode) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.Quantity, t2.DeliveryMethod, t1.CompanyName, t1.POCode FROM pol AS t2 INNER JOIN po AS t1 ON t2.POCode = t1.POCode WHERE t1.status != 'C' AND t2.SOCode = '" + soCode + "' AND t2.SOLine = '" + soLine + "'");

    while(rs.next())
    {
      if(poCode[0].length() > 0)
      {
        qty[0]            += "<br>" + rs.getString(1);
        deliveryMethod[0] += "<br>" + rs.getString(2);
        supplierName[0]   += "<br>" + rs.getString(3);
        poCode[0]         += "<br>" + rs.getString(4);
      }
      else
      {
        qty[0]            = rs.getString(1);
        deliveryMethod[0] = rs.getString(2);
        supplierName[0]   = rs.getString(3);
        poCode[0]         = rs.getString(4);
      }
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void calcDetailsLP(Connection con, Statement stmt, ResultSet rs, String soCode, String soLine, String[] qty, String[] deliveryMethod, String[] supplierName, String[] lpCode) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.Quantity, t2.DeliveryMethod, t1.CompanyName, t1.LPCode FROM lpl AS t2 INNER JOIN lp AS t1 ON t2.LPCode = t1.LPCode WHERE t1.status != 'C' AND t2.SOCode = '" + soCode + "' AND t2.SOLine = '" + soLine + "'");

    while(rs.next())
    {
      if(lpCode[0].length() > 0)
      {
        qty[0]            += "<br>" + rs.getString(1);
        deliveryMethod[0] += "<br>" + rs.getString(2);
        supplierName[0]   += "<br>" + rs.getString(3);
        lpCode[0]         += "<br>" + rs.getString(4);
      }
      else
      {
        qty[0]            = rs.getString(1);
        deliveryMethod[0] = rs.getString(2);
        supplierName[0]   = rs.getString(3);
        lpCode[0]         = rs.getString(4);
      }
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String detPL(Connection con, Statement stmt, ResultSet rs, String soCode, String soLine) throws Exception
  {
    String plCodes = "";

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t1.PLCode FROM pll AS t2 INNER JOIN pl AS t1 ON t2.PLCode = t1.PLCode WHERE t1.status != 'C' AND t2.SOCode = '" + soCode + "' AND t2.SOLine = '" + soLine + "'");

    while(rs.next())
    {
      if(plCodes.length() > 0)
        plCodes += "<br>" + rs.getString(1);
      else plCodes = rs.getString(1);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    return plCodes;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void outputPageFrame(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, boolean plain, String soCode, String bodyStr, String title, String unm, String sid, String uty, String men, String den,
                               String dnm, String bnm, String otherSetup, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    int[] hmenuCount = new int[1];

    String subMenuText = dashboardUtils.buildSubMenuText(con, stmt, rs, req, "3041", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount);

    subMenuText += "<table id='title' width=100%><tr><td colspan=2 nowrap><p>" + title + directoryUtils.buildHelp(3041) + "</td></tr></table>";

    subMenuText += buildSubMenuText(plain, soCode, unm, sid, uty, men, den, dnm, bnm, hmenuCount);

    if(plain)
      scoutln(out, bytesOut, authenticationUtils.buildPlainScreen(con, stmt, rs, req, unm, sid, uty, men, den, dnm, bnm, "_3041", subMenuText, hmenuCount[0], bodyStr, otherSetup, localDefnsDir, defnsDir));
    else
    {
      scoutln(out, bytesOut, authenticationUtils.buildScreen(con, stmt, rs, req, true, unm, sid, uty, men, den, dnm, bnm, "_3041", subMenuText, hmenuCount[0], bodyStr, otherSetup, localDefnsDir, defnsDir));

      scoutln(out, bytesOut, "<table border=0><tr><td valign=top><div id='second' style=\"{margin-top:0px;}\"></div></td>");
      scoutln(out, bytesOut, "<td valign=top><div id='third' style=\"{margin-top:0px;margin-left:0px;}\"></div></td></tr></table>");
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String buildSubMenuText(boolean plain, String soCode, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] hmenuCount) throws Exception
  {
    String s = "<div id='submenu'>";

    ++hmenuCount[0];

    if(! plain)
    {
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\">";
      s += "<a href=\"/central/servlet/_3041?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + soCode + "&p2=P\">Friendly</a></dt></dl>";
    }

    s += "</div>";

    --hmenuCount[0];

    return s;
  }

}
