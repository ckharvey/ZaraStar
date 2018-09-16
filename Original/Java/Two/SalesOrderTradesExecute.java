// =======================================================================================================================================================================================================
// System: ZaraStar: Sales Orders Processing - Sales - Trades
// Module: SalesOrderTradesExecute.java
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

public class SalesOrderTradesExecute extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  MiscDefinitions miscDefinitions = new MiscDefinitions();
  DocumentUtils documentUtils = new DocumentUtils();
  DrawingUtils drawingUtils = new DrawingUtils();
  SalesOrder salesOrder = new SalesOrder();
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="";

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
      p1  = req.getParameter("p1"); // unm
      p2  = req.getParameter("p2"); // soCode - if coming from 2042c and want to mark as AllSupplied

      if(p2 == null) p2 = "";

      String[] people = new String[1];
      
      documentUtils.getManagedPeople(p1, dnm, people);
      
      byte[] checkBoxes = new byte[people[0].length() + 1];

      generalUtils.strToBytes(checkBoxes, people[0]);

      doIt(out, req, checkBoxes, p2, unm, sid, uty, men, den, dnm, bnm, bytesOut);
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
 
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "SalesOrderTradesa", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2042, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush(); 
    }
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p2="";

    try
    {
      directoryUtils.setContentHeaders(res);
      out = res.getWriter();

      byte[] checkBoxes    = new byte[1000]; checkBoxes[0]    = '\000';
      int[]  checkBoxesLen = new int[1];     checkBoxesLen[0] = 1000;

      int thisEntryLen, inc;

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
        if(name.length() > 2 && name.substring(1, 3).equals(".x")) ;
        else
        if(name.length() > 2 && name.substring(1, 3).equals(".y")) ;
        else // must be checkbox value
        {
          thisEntryLen = name.length() + 2;
          if((generalUtils.lengthBytes(checkBoxes, 0) + thisEntryLen) >= checkBoxesLen[0])
          {
            byte[] tmp = new byte[checkBoxesLen[0]];
            System.arraycopy(checkBoxes, 0, tmp, 0, checkBoxesLen[0]);
            if(thisEntryLen > 1000)
              inc = thisEntryLen;
            else inc = 1000;
            checkBoxesLen[0] += inc;
            checkBoxes = new byte[checkBoxesLen[0]];
            System.arraycopy(tmp, 0, checkBoxes, 0, checkBoxesLen[0] - inc);
          }

          generalUtils.catAsBytes(name + "\001", 0, checkBoxes, false);
        }
      }

      if(p2 == null) p2 = "";

      doIt(out, req, checkBoxes, p2, unm, sid, uty, men, den, dnm, bnm, bytesOut);
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
 
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "SalesOrderTradesa", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2042, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush(); 
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, byte[] checkBoxes, String soCode, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
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
    ResultSet rs= null, rs2 = null;
    
    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 2042, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "SalesOrderTradesa", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2042, bytesOut[0], 0, "ACC:");
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "SalesOrderTradesa", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2042, bytesOut[0], 0, "SID:");
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(soCode.length() > 0)
      salesOrder.setAsAllSupplied(con, stmt, soCode, unm);

    set(con, stmt, stmt2, rs, rs2, out, req, checkBoxes, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 2042, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, HttpServletRequest req, byte[] checkBoxes, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                   String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Sales Order Processing - Trades</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    scoutln(out, bytesOut, "function details(code){var p1=sanitise(code);");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/SalesOrderTradesDetails?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+p1;}");

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4143, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewOC(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/OrderConfirmationPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4130, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewOA(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/OrderAcknowledgementPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4031, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewSO(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/SalesOrderPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3038, unm, uty, dnm, localDefnsDir, defnsDir))
    {
       scoutln(out, bytesOut, "function viewPL(code){var p1=sanitise(code);");
       scoutln(out, bytesOut, "this.location.href=\"/central/servlet/GoodsReceivedPickingList?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4067, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewInv(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/SalesInvoicePage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4054, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewDO(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/DeliveryOrderPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 5006, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewPO(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/PurchaseOrderPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }
  
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4080, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewPro(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/ProformaInvoicePage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }
    
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3025, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewGR(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/GoodsReceivedPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4001, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewCust(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/CustomerPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p1=\"+p1;}");
    }

    scoutln(out, bytesOut, "function del(x,code){update(code);var messageElement=document.getElementById('a'+x);messageElement.innerHTML='';");
    scoutln(out, bytesOut, "messageElement=document.getElementById('b'+x);messageElement.innerHTML='';");
    scoutln(out, bytesOut, "messageElement=document.getElementById('c'+x);messageElement.innerHTML='';");
    scoutln(out, bytesOut, "messageElement=document.getElementById('d'+x);messageElement.innerHTML='';");
    scoutln(out, bytesOut, "messageElement=document.getElementById('e'+x);messageElement.innerHTML='';");
    scoutln(out, bytesOut, "messageElement=document.getElementById('f'+x);messageElement.innerHTML='';");
    scoutln(out, bytesOut, "messageElement=document.getElementById('g'+x);messageElement.innerHTML='';");
    scoutln(out, bytesOut, "messageElement=document.getElementById('h'+x);messageElement.innerHTML='';");
    scoutln(out, bytesOut, "messageElement=document.getElementById('i'+x);messageElement.innerHTML='';");
    scoutln(out, bytesOut, "messageElement=document.getElementById('j'+x);messageElement.innerHTML='';");
    scoutln(out, bytesOut, "messageElement=document.getElementById('k'+x);messageElement.innerHTML='';");
    scoutln(out, bytesOut, "messageElement=document.getElementById('l'+x);messageElement.innerHTML='';");
    scoutln(out, bytesOut, "messageElement=document.getElementById('m'+x);messageElement.innerHTML='';");
    scoutln(out, bytesOut, "messageElement=document.getElementById('n'+x);messageElement.innerHTML='';");
    scoutln(out, bytesOut, "messageElement=document.getElementById('o'+x);messageElement.innerHTML='';");
    scoutln(out, bytesOut, "messageElement=document.getElementById('p'+x);messageElement.innerHTML='';");
    scoutln(out, bytesOut, "messageElement=document.getElementById('q'+x);messageElement.innerHTML='';}");

    // update
    scoutln(out, bytesOut, "var req3;");
    scoutln(out, bytesOut, "function initRequest3(url)");
    scoutln(out, bytesOut, "{if(window.XMLHttpRequest){req3=new XMLHttpRequest();}else");
    scoutln(out, bytesOut, "if(window.ActiveXObject){req3=new ActiveXObject(\"Microsoft.XMLHTTP\");}}");

    scoutln(out, bytesOut, "function update(code){");
    scoutln(out, bytesOut, "var url=\"http://" + men + "/central/servlet/SalesOrderAllSupplied?unm=\" + escape('" + unm + "') + \"&sid=\" + escape('" + sid + "') + \"&uty=\" + escape('" + uty + "') + \"&men=\" + escape('" + men + "') + \"&den=\" + escape('"
                         + den + "') + \"&bnm=\" + escape('" + bnm + "') + \"&p1=\" + code + \"&dnm=\" + escape('" + dnm + "');");
    scoutln(out, bytesOut, "initRequest3(url);");
    scoutln(out, bytesOut, "req3.onreadystatechange=processRequest3;");
    scoutln(out, bytesOut, "req3.open(\"GET\",url,true);");
    scoutln(out, bytesOut, "req3.send(null);}");

    scoutln(out, bytesOut, "function processRequest3(){");
    scoutln(out, bytesOut, "if(req3.readyState==4){");
    scoutln(out, bytesOut, "if(req3.status==200){");
    scoutln(out, bytesOut, "var res=req3.responseXML.getElementsByTagName(\"res\")[0].childNodes[0].nodeValue;");
    scoutln(out, bytesOut, "if(res.length>0){");
    scoutln(out, bytesOut, "}}}}");

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

    drawingUtils.outputPageFrame(con, stmt, rs, out, req, "2042", "", "SalesOrderTradesa", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount, bytesOut);

    drawingUtils.drawTitle(out, false, false, "SalesOrderTradesa", "", "Sales Order Processing - Sales: Trades", "2042", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<table id=\"page\" width=100% border=0>");

    scoutln(out, bytesOut, "<tr><td></td></tr><tr id=\"pageColumn\"><td></td><td></td>");

    scoutln(out, bytesOut, "<td><p>SO Code</td>");
    scoutln(out, bytesOut, "<td><p>SO Date</td>");
    scoutln(out, bytesOut, "<td align=center><p>Acknowledgements</td>");
    scoutln(out, bytesOut, "<td align=center><p>Picking Lists</td>");
    scoutln(out, bytesOut, "<td align=center><p>Delivery Orders</td>");
    scoutln(out, bytesOut, "<td align=center><p>Invoices</td>");
    scoutln(out, bytesOut, "<td align=center><p>Proforma Invoices</td>");
    scoutln(out, bytesOut, "<td align=center><p>Purchase Orders</td>");
    scoutln(out, bytesOut, "<td align=center><p>Goods Received Notes</td>");
    scoutln(out, bytesOut, "<td align=center><p>Confirmations</td>");
    scoutln(out, bytesOut, "<td><p>Customer Code</td>");
    scoutln(out, bytesOut, "<td><p>Customer Name</td>");
    scoutln(out, bytesOut, "<td><p>Customer POCode</td>");
    scoutln(out, bytesOut, "<td><p>Amount</td>");
    scoutln(out, bytesOut, "<td><p>Currency</td></tr>");

    String[] cssFormat = new String[1];  cssFormat[0] = "line1";
    int[] oCount = new int[1];  oCount[0] = 1;
    
    boolean[] atleastOneDONotReturned     = new boolean[1];  atleastOneDONotReturned[0]     = false;
    boolean[] atleastOneInvoiceNotCreated = new boolean[1];  atleastOneInvoiceNotCreated[0] = false;
    boolean[] atleastOnePLCompleted       = new boolean[1];  atleastOnePLCompleted[0]       = false;
    
    forAllSalesOrderHeaders(con, stmt, stmt2, rs, rs2, out, checkBoxes, cssFormat, oCount, atleastOneDONotReturned, atleastOneInvoiceNotCreated, atleastOnePLCompleted, bytesOut);
    
    scout(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    if(atleastOneDONotReturned[0])
      scout(out, bytesOut, "<tr><td colspan=6><font size=4 color=red>* Delivery Order <i>NOT</i> Returned</td></tr>");

    if(atleastOneInvoiceNotCreated[0])
      scout(out, bytesOut, "<tr><td bgcolor=tomato>&nbsp;</td><td colspan=5>Invoice <i>NOT</i> Created</td></tr>");

    if(atleastOnePLCompleted[0])
      scout(out, bytesOut, "<tr><td colspan=6><font size=4 color=green>* Picking List Completed</td></tr>");

    scoutln(out, bytesOut, "</table>");
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
  private void forAllSalesOrderHeaders(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, byte[] checkBoxes, String[] cssFormat, int[] oCount, boolean[] atleastOneDONotReturned,
                                       boolean[] atleastOneInvoiceNotCreated, boolean[] atleastOnePLCompleted, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT SalesPerson, SOCode, Date, CompanyCode, CustomerPOCode, TotalTotal, Currency2, CompanyName FROM so WHERE SOType = 'T' AND Status != 'C' AND AllSupplied != 'Y' ORDER BY Date, SOCode");
      
    String soCode, date, salesPerson, customerCode, custPOCode, currency, companyName;
    
    double totalTotal;
    byte[] b = new byte[20];
    String poCodes;
    boolean[] atLeastOneOutstandingInvoice = new boolean[1];

    while(rs.next())
    {    
      salesPerson = rs.getString(1);

      if(salesPersonIsRequired(salesPerson, checkBoxes))
      {
        soCode       = generalUtils.deNull(rs.getString(2));
        date         = generalUtils.deNull(rs.getString(3));
        customerCode = generalUtils.deNull(rs.getString(4));
        custPOCode   = generalUtils.deNull(rs.getString(5));
        totalTotal   = generalUtils.doubleFromStr(generalUtils.deNull(rs.getString(6)));
        currency     = generalUtils.deNull(rs.getString(7));
        companyName  = generalUtils.deNull(rs.getString(8));
       
        generalUtils.doubleToBytesCharFormat(totalTotal, b, 0);
        generalUtils.bytesDPsGivenSeparator(true, '\000', '2', b, 20, 0);
        generalUtils.formatNumeric(b, '2');

        if(! forASalesOrder(con, stmt2, rs2, soCode, atLeastOneOutstandingInvoice))
        {
          if(atLeastOneOutstandingInvoice[0])
          {
            scoutln(out, bytesOut, "<tr bgcolor=tomato>");
            atleastOneInvoiceNotCreated[0] = true;
          }
          else scoutln(out, bytesOut, "<tr bgcolor=lightgreen>");

        }
        else
        {
          if(cssFormat[0].equals("line1")) cssFormat[0] = "line2"; else cssFormat[0] = "line1";

          scoutln(out, bytesOut, "<tr id=\"" + cssFormat[0] + "\">");
        }

        scoutln(out, bytesOut, "<td valign=top><span id='a" + oCount[0] + "'><p>" + oCount[0] + "</span></td><td valign=top><span id='b" + oCount[0] + "'><p><a href=\"javascript:del('" + oCount[0] + "','" + soCode + "')\">Supplied</a></span></td>");

        scoutln(out, bytesOut, "<td valign=top><span id='c" + oCount[0] + "'><p><a href=\"javascript:viewSO('" + soCode + "')\">" + soCode + "</a>");
        scoutln(out, bytesOut, "<br><a href=\"javascript:details('" + soCode + "')\">Details</a></span></td>");

        scoutln(out, bytesOut, "<td valign=top><span id='d" + oCount[0] + "'><p>" + generalUtils.convertFromYYYYMMDD(date) + "</span></td>");

        getAcknowledgementsForAnSO(con, stmt2, rs2, out, soCode, oCount[0], bytesOut);
         
        getPickingListsForAnSO(con, stmt2, rs2, out, soCode, oCount[0], atleastOnePLCompleted, bytesOut);

        getDeliveryOrdersForAnSO(con, stmt2, rs2, out, soCode, oCount[0], atleastOneDONotReturned, bytesOut);
        
        getInvoicesForAnSO(con, stmt2, rs2, out, soCode, oCount[0], bytesOut);
         
        getProformaInvoicesForAnSO(con, stmt2, rs2, out, soCode, oCount[0], bytesOut);

        poCodes = getPurchaseOrdersForAnSO(con, stmt2, rs2, out, soCode, oCount[0], bytesOut);

        getGRNsForThePOs(con, stmt2, rs2, out, poCodes, oCount[0], bytesOut);

        getConfirmationsForAnSO(con, stmt2, rs2, out, soCode, oCount[0], bytesOut);
            
        scout(out, bytesOut, "<td valign=top><span id='m" + oCount[0] + "'><p><a href=\"javascript:viewCust('" + customerCode + "')\">" + customerCode + "</a></span></td>");
        scout(out, bytesOut, "<td nowrap valign=top><span id='n" + oCount[0] + "'><p>" + companyName + "</span></td>");
        scout(out, bytesOut, "<td nowrap valign=top><span id='o" + oCount[0] + "'><p>" + custPOCode + "</span></td>");
        scoutln(out, bytesOut, "<td align=right valign=top><span id='p" + oCount[0] + "'><p>" + generalUtils.stringFromBytes(b, 0L) + "</span></td>");
        scout(out, bytesOut, "<td nowrap valign=top><span id='q" + oCount[0] + "'><p>" + currency + "</td></tr></span>");

        ++oCount[0];
      }
    }
  
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean salesPersonIsRequired(String salesPerson, byte[] checkBoxes) throws Exception
  {
    String s;

    int x = 0, len = generalUtils.lengthBytes(checkBoxes, 0);
    while(x < len)
    {
      s = "";
      while(checkBoxes[x] != '\001' && checkBoxes[x] != '\000')
        s += (char)checkBoxes[x++];

      s = generalUtils.deSanitise(s);

      if(s.equals(salesPerson))
        return true;

      ++x;
    }
    
    return false;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void getPickingListsForAnSO(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String soCode, int oCount, boolean[] atleastOnePLCompleted, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.PLCode, t1.Completed FROM pll AS t2 INNER JOIN pl AS t1 ON t2.PLCode = t1.PLCode WHERE t2.SOCode = '" + soCode + "' AND t1.Status != 'C'");
   
    boolean first = true;
    String code, completed;
    String[] codes = new String[1];  codes[0] = "";
    
    while(rs.next())
    {
      completed = rs.getString(2);
      if(completed.equals("Y"))
        atleastOnePLCompleted[0] = true;
      else completed = "N";

      addToList(codes, completed + rs.getString(1));
    }

    scout(out, bytesOut, "<td valign=top><span id='f" + oCount + "'><p>");

    char hasBeenCompleted;
    int x = 0, len = codes[0].length();
    while(x < len)
    {
      hasBeenCompleted = codes[0].charAt(x++);
      code = "";
      while(codes[0].charAt(x) != '\001')
        code += codes[0].charAt(x++);
      ++x;

      if(! first)
        scout(out, bytesOut, " ");
      else first = false;
      
      scout(out, bytesOut, "<a href=\"javascript:viewPL('" + code + "')\">" + code + "</a>");

      if(hasBeenCompleted == 'Y')
        scout(out, bytesOut, "<font size=4 color=greeen><sup>*</sup></font>");
    }
    
    scoutln(out, bytesOut, "</td>");
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }            
      
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void getDeliveryOrdersForAnSO(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String soCode, int oCount, boolean[] atleastOneDONotReturned, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t1.DOCode, t1.Returned FROM dol AS t2 INNER JOIN do AS t1 ON t2.DOCode = t1.DOCode WHERE t2.SOCode != '' AND t2.SOCode = '" + soCode + "' AND t1.Status != 'C'");
   
    boolean first = true;
    String code, returned;
    String[] codes = new String[1];  codes[0] = "";
    
    while(rs.next())
    {
      returned = rs.getString(2);
      if(returned.equals("1")) // Y
        returned = "Y";
      else
      {
        returned = "N";
        atleastOneDONotReturned[0] = true;
      }

      addToList(codes, returned + rs.getString(1));
    }

    scout(out, bytesOut, "<td valign=top><span id='g" + oCount + "'><p>");

    char hasBeenReturned;
    int x = 0, len = codes[0].length();
    while(x < len)
    {
      hasBeenReturned = codes[0].charAt(x++);
      code = "";
      while(codes[0].charAt(x) != '\001')
        code += codes[0].charAt(x++);
      ++x;

      if(! first)
        scout(out, bytesOut, " ");
      else first = false;
      
      scout(out, bytesOut, "<a href=\"javascript:viewDO('" + code + "')\">" + code + "</a>");

      if(hasBeenReturned == 'N')
        scout(out, bytesOut, "<font size=4 color=red><sup>*</sup></font>");
    }
    
    scoutln(out, bytesOut, "</td>");
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }                 
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void getInvoicesForAnSO(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String soCode, int oCount, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t1.InvoiceCode FROM invoicel AS t2 INNER JOIN invoice AS t1 ON t2.InvoiceCode = t1.InvoiceCode WHERE t2.SOCode = '" + soCode + "' AND t1.Status != 'C'");
   
    boolean first = true;
    String code;
    String[] codes = new String[1];  codes[0] = "";
    
    while(rs.next())
      addToList(codes, rs.getString(1));

    scout(out, bytesOut, "<td valign=top><span id='h" + oCount + "'><p>");

    int x = 0, len = codes[0].length();
    while(x < len)
    {
      code = "";
      while(codes[0].charAt(x) != '\001')
        code += codes[0].charAt(x++);
      ++x;

      if(! first)
        scout(out, bytesOut, " ");
      else first = false;
      
      scout(out, bytesOut, "<a href=\"javascript:viewInv('" + code + "')\">" + code + "</a>");
    }
    
    scoutln(out, bytesOut, "</td>");
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }            
      
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void getProformaInvoicesForAnSO(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String soCode, int oCount, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.ProformaCode FROM proformal AS t2 INNER JOIN proforma AS t1 ON t2.ProformaCode = t1.ProformaCode WHERE t2.SOCode = '" + soCode + "' AND t1.Status != 'C'");
   
    boolean first = true;
    String code;
    String[] codes = new String[1];  codes[0] = "";
    
    while(rs.next())
      addToList(codes, rs.getString(1));

    scout(out, bytesOut, "<td valign=top><span id='i" + oCount + "'><p>");

    int x = 0, len = codes[0].length();
    while(x < len)
    {
      code = "";
      while(codes[0].charAt(x) != '\001')
        code += codes[0].charAt(x++);
      ++x;

      if(! first)
        scout(out, bytesOut, " ");
      else first = false;
      
      scout(out, bytesOut, "<a href=\"javascript:viewPro('" + code + "')\">" + code + "</a>");
    }
    
    scoutln(out, bytesOut, "</td>");
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }            
      
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String getPurchaseOrdersForAnSO(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String soCode, int oCount, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.POCode FROM pol AS t2 INNER JOIN po AS t1 ON t2.POCode = t1.POCode WHERE t2.SOCode = '" + soCode + "' AND t1.Status != 'C'");
   
    boolean first = true;
    String code;
    String[] codes = new String[1];  codes[0] = "";
    
    while(rs.next())
      addToList(codes, rs.getString(1));

    scout(out, bytesOut, "<td valign=top><span id='j" + oCount + "'><p>");

    int x = 0, len = codes[0].length();
    while(x < len)
    {
      code = "";
      while(codes[0].charAt(x) != '\001')
        code += codes[0].charAt(x++);
      ++x;

      if(! first)
        scout(out, bytesOut, " ");
      else first = false;
      
      scout(out, bytesOut, "<a href=\"javascript:viewPO('" + code + "')\">" + code + "</a>");
    }
    
    scoutln(out, bytesOut, "</td>");
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    return codes[0];
  }            

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void getGRNsForThePOs(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String poCodes, int oCount, int[] bytesOut) throws Exception
  {
    try
    {
      int x = 0, len = poCodes.length();
      String poCode, code;
      String[] codes = new String[1];  codes[0] = "";

      while(x < len)
      {
        poCode = "";
        while(x < len && poCodes.charAt(x) != '\001') // just-in-case
          poCode += poCodes.charAt(x++);
        ++x;

        stmt = con.createStatement();

        rs = stmt.executeQuery("SELECT t2.GRCode FROM grl AS t2 INNER JOIN gr AS t1 ON t2.GRCode = t1.GRCode WHERE t2.POCode = '" + poCode + "' AND t1.Status != 'C'");

        while(rs.next())
          addToList(codes, rs.getString(1));

        if(rs   != null) rs.close();
        if(stmt != null) stmt.close();
      }

      scout(out, bytesOut, "<td valign=top><span id='k" + oCount + "'><p>");

      boolean first = true;
      x = 0;
      len = codes[0].length();
      while(x < len)
      {
        code = "";
        while(codes[0].charAt(x) != '\001')
          code += codes[0].charAt(x++);
        ++x;

        if(! first)
          scout(out, bytesOut, " ");
        else first = false;

        scout(out, bytesOut, "<a href=\"javascript:viewGR('" + code + "')\">" + code + "</a>");
      }

      scoutln(out, bytesOut, "</td>");
    }
    catch(Exception e)
    {
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void getAcknowledgementsForAnSO(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String soCode, int oCount, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT OACode FROM oa WHERE SOCode = '" + soCode + "' AND Status != 'C'");
   
    boolean first = true;
    String code;
    String[] codes = new String[1];  codes[0] = "";
    
    while(rs.next())
      addToList(codes, rs.getString(1));

    scout(out, bytesOut, "<td valign=top><span id='e" + oCount + "'><p>");

    int x = 0, len = codes[0].length();
    while(x < len)
    {
      code = "";
      while(codes[0].charAt(x) != '\001')
        code += codes[0].charAt(x++);
      ++x;

      if(! first)
        scout(out, bytesOut, " ");
      else first = false;
      
      scout(out, bytesOut, "<a href=\"javascript:viewOA('" + code + "')\">" + code + "</a>");
    }
    
    scoutln(out, bytesOut, "</td>");
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }            

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void getConfirmationsForAnSO(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String soCode, int oCount, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT OCCode FROM oc WHERE SOCode = '" + soCode + "' AND Status != 'C'");
   
    boolean first = true;
    String code;
    String[] codes = new String[1];  codes[0] = "";
    
    while(rs.next())
      addToList(codes, rs.getString(1));

    scout(out, bytesOut, "<td valign=top><span id='l" + oCount + "'><p>");

    int x = 0, len = codes[0].length();
    while(x < len)
    {
      code = "";
      while(codes[0].charAt(x) != '\001')
        code += codes[0].charAt(x++);
      ++x;

      if(! first)
        scout(out, bytesOut, " ");
      else first = false;
      
      scout(out, bytesOut, "<a href=\"javascript:viewOC('" + code + "')\">" + code + "</a>");
    }
    
    scoutln(out, bytesOut, "</td>");
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }            

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void addToList(String[] codes, String newCode) throws Exception
  {
    String code;
    int x = 0, len = codes[0].length();
    while(x < len)
    {
      code = "";
      while(codes[0].charAt(x) != '\001')
        code += codes[0].charAt(x++);
      ++x;

      if(code.equals(newCode))
        return;
    }
    
    codes[0] += (newCode + "\001");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean forASalesOrder(Connection con, Statement stmt, ResultSet rs, String soCode, boolean[] atLeastOneOutstandingInvoice) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.ItemCode, t2.Quantity FROM sol AS t2 INNER JOIN so AS t1 ON t2.SOCode = t1.SOCode WHERE t1.Status != 'C' AND t2.SOCode = '" + soCode + "'");

    int soItemCodesCount = 0;
    String[][] soItemCodes = new String[1][100];
    double[][] soItemQtys  = new double[1][100];
    int[] soItemCodesSize  = new int[1];  soItemCodesSize[0] = 100;

    String[][] doItemCodes = new String[1][100];
    double[][] doItemQtys  = new double[1][100];
    int[] doItemCodesSize  = new int[1];  doItemCodesSize[0] = 100;

    String[][] invoiceItemCodes = new String[1][100];
    double[][] invoiceItemQtys  = new double[1][100];
    int[] invoiceItemCodesSize  = new int[1];  invoiceItemCodesSize[0] = 100;

    String itemCode, qty;

    while(rs.next())
    {
      itemCode = rs.getString(1);
      qty      = rs.getString(2);

      soItemCodesCount = addToList(itemCode, qty, soItemCodes, soItemCodesSize, soItemQtys, soItemCodesCount);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    return processSO(con, stmt, rs, soCode, soItemCodes[0], soItemQtys[0], soItemCodesCount, doItemCodes, doItemCodesSize, doItemQtys, invoiceItemCodes, invoiceItemCodesSize, invoiceItemQtys, atLeastOneOutstandingInvoice);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean processSO(Connection con, Statement stmt, ResultSet rs, String soCode, String[] soItemCodes, double[] soItemQtys, int soItemCodesCount, String[][] doItemCodes, int[] doItemCodesSize, double[][] doItemQtys,
                            String[][] invoiceItemCodes, int[] invoiceItemCodesSize, double[][] invoiceItemQtys, boolean[] atLeastOneOutstandingInvoice) throws Exception
  {
    boolean atLeastOneOutstanding = atLeastOneOutstandingInvoice[0] = false;

    int doItemCodesCount = scanDOs(con, stmt, rs, soCode, doItemCodes, doItemCodesSize, doItemQtys);

    int invoiceItemCodesCount = scanInvoices(con, stmt, rs, soCode, invoiceItemCodes, invoiceItemCodesSize, invoiceItemQtys);

    String itemCode;
    double doQty, invoiceQty;

    int x = 0;
    for(x=0;x<soItemCodesCount;++x)
    {
      itemCode = soItemCodes[x];

      doQty = qtyOnList(itemCode, doItemCodes[0], doItemQtys[0], doItemCodesCount);

      if(doQty != soItemQtys[x])
        atLeastOneOutstanding = true;

      invoiceQty = qtyOnList(itemCode, invoiceItemCodes[0], invoiceItemQtys[0], invoiceItemCodesCount);

      if(invoiceQty != soItemQtys[x])
        atLeastOneOutstandingInvoice[0] = true;
    }

    return atLeastOneOutstanding;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private double qtyOnList(String itemCode, String[] itemCodes, double[] itemQtys, int itemCodesCount) throws Exception
  {
    for(int x=0;x<itemCodesCount;++x)
    {
      if(itemCode.equals(itemCodes[x]))
        return itemQtys[x];
    }

    return 0;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int scanDOs(Connection con, Statement stmt, ResultSet rs, String soCode, String[][] doItemCodes, int[] doItemCodesSize, double[][] doItemQtys) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.ItemCode, t2.Quantity FROM dol AS t2 INNER JOIN do AS t1 ON t2.DOCode = t1.DOCode "
                         + "WHERE t1.Status != 'C' AND t2.SOCode = '" + soCode + "'");

    int itemCodesCount = 0;

    while(rs.next())
      itemCodesCount = addToList(rs.getString(1), rs.getString(2), doItemCodes, doItemCodesSize, doItemQtys, itemCodesCount);

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    return itemCodesCount;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int scanInvoices(Connection con, Statement stmt, ResultSet rs, String soCode, String[][] doItemCodes, int[] doItemCodesSize, double[][] doItemQtys) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.ItemCode, t2.Quantity FROM invoicel AS t2 INNER JOIN invoice AS t1 ON t2.InvoiceCode = t1.InvoiceCode "
                         + "WHERE t1.Status != 'C' AND t2.SOCode = '" + soCode + "'");

    int itemCodesCount = 0;

    while(rs.next())
      itemCodesCount = addToList(rs.getString(1), rs.getString(2), doItemCodes, doItemCodesSize, doItemQtys, itemCodesCount);

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    return itemCodesCount;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int addToList(String itemCode, String qty, String[][] itemCodes, int[] itemCodesSize, double[][] itemQtys, int itemCodesCount) throws Exception
  {
    int x = 0;
    while(x < itemCodesCount)
    {
      if(itemCodes[0][x].equals(itemCode))
      {
        itemQtys[0][x] += generalUtils.doubleFromStr(qty);
        return itemCodesCount;
      }

      ++x;
    }

    if(itemCodesCount == itemCodesSize[0])
    {
      String[] tmp = new String[itemCodesSize[0]];
      for(x=0;x<itemCodesSize[0];++x)
        tmp[x] = itemCodes[0][x];
      itemCodes[0] = new String[itemCodesSize[0] + 20];
      for(x=0;x<itemCodesSize[0];++x)
        itemCodes[0][x] = tmp[x];

      double[] tmp2 = new double[itemCodesSize[0]];
      for(x=0;x<itemCodesSize[0];++x)
        tmp2[x] = itemQtys[0][x];
      itemQtys[0] = new double[itemCodesSize[0] + 20];
      for(x=0;x<itemCodesSize[0];++x)
        itemQtys[0][x] = tmp2[x];

      itemCodesSize[0] += 20;
    }

    itemCodes[0][itemCodesCount] = itemCode;

    itemQtys[0][itemCodesCount] = generalUtils.doubleFromStr(qty);

    return ++itemCodesCount;
  }

}
