// =======================================================================================================================================================================================================
// System: ZaraStar: DO Updating
// Module: DeliveryOrderUpdating.java
// Author: C.K.Harvey
// Copyright (c) 2001-09 Christopher Harvey. All Rights Reserved.
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

public class DeliveryOrderUpdating extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  MiscDefinitions miscDefinitions = new MiscDefinitions();
  DrawingUtils drawingUtils = new DrawingUtils();
  OrderConfirmation orderConfirmation = new OrderConfirmation();
  SalesOrder salesOrder = new SalesOrder();

  // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "DeliveryOrderUpdating", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2043, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush(); 
    }
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
      }

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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "DeliveryOrderUpdating", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2043, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush(); 
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
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
    Statement stmt = null, stmt2 = null;
    ResultSet rs   = null, rs2   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 2043, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "DeliveryOrderUpdating", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2043, bytesOut[0], 0, "ACC:");
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "DeliveryOrderUpdating", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2043, bytesOut[0], 0, "SID:");
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    set(con, stmt, stmt2, rs, rs2, out, req, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 2043, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");

    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm,
                   String bnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Delivery Order Processing</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    scoutln(out, bytesOut, "function update(){document.go.submit();}");

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 148, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewDO(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/DeliveryOrderPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
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

    drawingUtils.outputPageFrame(con, stmt, rs, out, req, "2043", "", "DeliveryOrderUpdating", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount, bytesOut);

    drawingUtils.drawTitle(out, false, false, "DeliveryOrderUpdating", "", "Delivery Order Processing - Returns", "2043", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<form name=\"go\" action=\"DeliveryOrdersReturned\" enctype=\"application/x-www-form-urlencoded\" method=POST>");

    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=unm value=" + unm + ">");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=sid value=" + sid + ">");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=uty value=" + uty + ">");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=men value=\"" + men + "\">");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=den value=" + den + ">");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=dnm value=" + dnm + ">");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=bnm value=" + bnm + ">");

    scoutln(out, bytesOut, "<table id=\"page\" width=100% border=0>");

    scoutln(out, bytesOut, "<tr><td></td></tr><tr id=\"pageColumn\"><td></td><td></td>");

    scoutln(out, bytesOut, "<td><p><b>DO Code </b></td>");
    scoutln(out, bytesOut, "<td><p><b>DO Date </b></td>");
    scoutln(out, bytesOut, "<td><p><b>Customer Code </b></td>");
    scoutln(out, bytesOut, "<td><p><b>PO Code </b></td>");
    scoutln(out, bytesOut, "<td><p><b>Delivery Driver </b></td>");
    scoutln(out, bytesOut, "<td><p><b>Notes </b></td>");
    scoutln(out, bytesOut, "<td><p><b>Customer Name </b></td></tr>");

    String[] cssFormat = new String[1];  cssFormat[0] = "line1";
    int[] oCount = new int[1];  oCount[0] = 1;

    forAllDeliveryOrderHeaders(con, stmt, stmt2, rs, rs2, out, cssFormat, oCount, bytesOut);

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap colspan=4><p><a href=\"javascript:update()\">Update</a> Delivery Orders</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir));
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
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

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void forAllDeliveryOrderHeaders(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, String[] cssFormat, int[] oCount, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT DOCode, Date, CompanyCode, CompanyName, PORefNum, Notes, DeliveryDriver FROM do WHERE Status != 'C' AND Returned != '1' ORDER BY DOCode");

    String doCode, date, customerCode, companyName, custPOCode, notes, deliveryDriver;

    while(rs.next())
    {
      doCode       = generalUtils.deNull(rs.getString(1));
      date         = generalUtils.deNull(rs.getString(2));
      customerCode = generalUtils.deNull(rs.getString(3));
      companyName  = generalUtils.deNull(rs.getString(4));
      custPOCode   = generalUtils.deNull(rs.getString(5));
      notes        = generalUtils.deNull(rs.getString(6));
      deliveryDriver = generalUtils.deNull(rs.getString(7));

      if(cssFormat[0].equals("line1")) cssFormat[0] = "line2"; else cssFormat[0] = "line1";

      scoutln(out, bytesOut, "<tr id=\"" + cssFormat[0] + "\">");

      scoutln(out, bytesOut, "<td><p>" + oCount[0]++ + "</td>");

      scout(out, bytesOut, "<td><p><input type=checkbox name=x" + generalUtils.sanitise(doCode) + "></td>");

      scoutln(out, bytesOut, "<td><p><a href=\"javascript:viewDO('" + doCode + "')\">" + doCode + "</a></td>");
      scoutln(out, bytesOut, "<td><p>" + generalUtils.convertFromYYYYMMDD(date) + "</td>");

      scout(out, bytesOut, "<td><p><a href=\"javascript:viewCust('" + customerCode + "')\">" + customerCode + "</a></td>");
      scout(out, bytesOut, "<td nowrap><p>" + custPOCode + "</td>");

      scoutln(out, bytesOut, "<td><p>" + getDeliveryDriverDDL(con, stmt2, rs2, deliveryDriver, doCode) + "</td>");

      scoutln(out, bytesOut, "<td><p><input type=text size=25 maxlength=250 name='n" + doCode + "' value='" + generalUtils.replaceThreesWithSpaces(notes) + "'></td>");

      scout(out, bytesOut, "<td nowrap><p>" + companyName + "</td></tr>");
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String getDeliveryDriverDDL(Connection con, Statement stmt, ResultSet rs, String deliveryDriver, String doCode) throws Exception
  {
    String s = "";

    s += "<select name='d" + doCode + "'>";

    try
    {
      String name;

      stmt = con.createStatement();
      rs = stmt.executeQuery("SELECT Name FROM driver WHERE Type = 'A' ORDER BY Name\n");
      while(rs.next())
      {
        name = rs.getString(1);
        s += "<option value=\"" + name + "\"";
        if(name.equals(deliveryDriver))
          s += " selected";
        s += ">" + name;
      }
    }
    catch(Exception e)
    {
      System.out.println("2043: " + e);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    s += "</select>";

    return s;
  }

}
