// =======================================================================================================================================================================================================
// System: ZaraStar Accounts: Invoice Cost-of-Sale Verification
// Module: InvoiceCostOfSaleVerificationExecute.java
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

public class InvoiceCostOfSaleVerificationExecute extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DrawingUtils drawingUtils = new DrawingUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  MiscDefinitions miscDefinitions = new MiscDefinitions();
  Inventory inventory = new Inventory();
  DefinitionTables definitionTables = new DefinitionTables();
  AccountsUtils accountsUtils = new AccountsUtils();
  PickingList pickingList = new PickingList();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
     doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="";

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
      p1  = req.getParameter("p1"); // dateFrom
      p2  = req.getParameter("p2"); // dateTo
      
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
 
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "InvoiceCostOfSaleVerification", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3070, bytesOut[0], 0, "ERR:" + p1 + ":" + p2);
      if(out != null) out.flush(); 
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String dateFrom, String dateTo, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String workingDir    = directoryUtils.getUserDir('W', dnm, unm);
    
    Statement stmt = null, stmt2 = null;
    ResultSet rs   = null, rs2   = null;

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 3070, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "InvoiceCostOfSaleVerification", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3070, bytesOut[0], 0, "ACC:" + dateTo);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "InvoiceCostOfSaleVerification", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3070, bytesOut[0], 0, "SID:" + dateTo);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(dateFrom.length() == 0)
      dateFrom = "1970-01-01";
    else dateFrom = generalUtils.convertDateToSQLFormat(dateFrom);

    if(dateTo.length() == 0)
      dateTo = "2099-12-31";
    else dateTo = generalUtils.convertDateToSQLFormat(dateTo);

    set(con, stmt, stmt2, rs, rs2, out, req, dateFrom, dateTo, unm, sid, uty, men, den, dnm, bnm, workingDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 3070, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), dateTo);
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String determineStartYearForInvoice(Connection con, Statement stmt, ResultSet rs, String dateTo, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    String[] financialYearStartMonth = new String[1];
    String[] financialYearEndMonth   = new String[1];
 
    definitionTables.getAppConfigFinancialYearMonths(con, stmt, rs, dnm, financialYearStartMonth, financialYearEndMonth);

    String s;
    int startMonth = generalUtils.detMonthNumFromMonthName(financialYearStartMonth[0]);
    if(startMonth < 10)
      s = "-0" + startMonth;
    else s = "-" + startMonth;

    s += "-01";
    
    return accountsUtils.getAccountingYearForADate(con, stmt, rs, generalUtils.convertFromYYYYMMDD(dateTo), dnm, localDefnsDir, defnsDir) + s;
  }
  
  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, HttpServletRequest req, String dateFrom, String dateTo, String unm, String sid, String uty, String men, String den, String dnm,
                   String bnm, String workingDir, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Invoice Cost-of-Sale Verification</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3001, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewItem(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/ProductStockRecord?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4067, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewInv(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/SalesInvoicePage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
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
    
    RandomAccessFile fhData  = generalUtils.create(workingDir + "3070.data");
    RandomAccessFile fhState = generalUtils.create(workingDir + "3070.state");
    generalUtils.fileClose(fhState);
    String stateFileName = workingDir + "3070.state";
    keepChecking(out, "3070", unm, sid, uty, men, den, dnm, bnm, bytesOut);

    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    int[] hmenuCount = new int[1];

    drawingUtils.outputPageFrame(con, stmt, rs, out, req, "3070", "", "InvoiceCostOfSaleVerificationExecute", unm, sid, uty, men, den, dnm, bnm, " chkTimer(); ", localDefnsDir, defnsDir, hmenuCount, bytesOut);

    drawingUtils.drawTitle(out, false, false, "InvoiceCostOfSaleVerificationExecute", "", "Invoice Cost-of-Sale Verification", "3070", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<span id='stuff'><font size=7><br><br><br><br>1% complete, please wait...</font></span>");
    if(out != null) out.flush(); 
    out.close();

    scoutln(fhData, "<form><table id='page' width=100% border=0>");

    scoutln(fhData, "<table id='page' border=0 cellspacing=2 cellpadding=3><tr id='pageColumn'>");
    scoutln(fhData, "<td><p>Invoice Code</td>");
    scoutln(fhData, "<td><p>Date</td>");
    scoutln(fhData, "<td><p>Item Code</td>");
    scoutln(fhData, "<td><p>Manufacturer</td>");
    scoutln(fhData, "<td><p>Manufacturer Code</td>");
    scoutln(fhData, "<td><p>Quantity</td>");
    scoutln(fhData, "<td><p>Value</td>");
    scoutln(fhData, "<td nowrap><p>Description</td></tr>");
           
    int numRecs = detRecs(con, stmt, stmt2, rs, rs2, fhData, stateFileName, dateFrom, dateTo, dnm, localDefnsDir, defnsDir);

    scoutln(fhData, "<tr><td>&nbsp;</td></tr>");

    scoutln(fhData, "<tr><td colspan=3><p>Number of records: " + numRecs + "</td></tr>");
    scoutln(fhData, "<tr><td>&nbsp;</td></tr>");

    scoutln(fhData, "</table></form>");
    scoutln(fhData, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
   
    generalUtils.fileClose(fhData);
    directoryUtils.updateState(stateFileName, "100");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int detRecs(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, RandomAccessFile fhData, String stateFileName, String dateFrom, String dateTo, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    int count = 0;
    char dpOnQuantities = miscDefinitions.dpOnQuantities(con, stmt, rs, "5");
    String[] cssFormat = new String[1];  cssFormat[0] = "";
    String dateStartAccountingYear;
        
    boolean useWAC = false;
    if(miscDefinitions.inventoryCostingMethod(con, stmt, rs).equals("WAC"))
      useWAC = true;

    try
    {
      stmt = con.createStatement();
      rs = stmt.executeQuery("SELECT COUNT(t2.InvoiceCode) FROM invoicel AS t2 INNER JOIN invoice AS t1 ON t1.InvoiceCode = t2.InvoiceCode WHERE t1.Status != 'C' AND t1.Date >= {d '" + dateFrom + "'} AND t1.Date <= {d '" + dateTo
                           + "'} ORDER BY t2.InvoiceCode");
      double numRecs = 1.0;
      if(rs.next())
        numRecs = rs.getInt(1);
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
      int countRecs = 0;
      
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT t2.InvoiceCode, t1.Date, t2.ItemCode, t2.Quantity, t2.SOCode FROM invoicel AS t2 INNER JOIN invoice AS t1 ON t1.InvoiceCode = t2.InvoiceCode WHERE t1.Status != 'C' AND t1.Date >= {d '" + dateFrom
                           + "'} AND t1.Date <= {d '" + dateTo + "'} ORDER BY t2.InvoiceCode");
      
      String invoiceCode, itemCode, qty, date, soCode;

      while(rs.next())
      {
        directoryUtils.updateState(stateFileName, "" + generalUtils.strDPs('0', ("" + generalUtils.doubleToStr((countRecs++ / numRecs) * 100))));

        itemCode    = rs.getString(3);
        
        if(itemExists(con, stmt2, rs2, itemCode))
        {      
          invoiceCode = rs.getString(1);
          date        = rs.getString(2);
          qty         = rs.getString(4);
          soCode      = rs.getString(5);

          if(qty == null) qty = "0";       
        
          dateStartAccountingYear = determineStartYearForInvoice(con, stmt2, rs2, date, dnm, localDefnsDir, defnsDir);

          count += process(con, stmt, rs, fhData, useWAC, dateStartAccountingYear, date, invoiceCode, itemCode, qty, dpOnQuantities, dnm, cssFormat);
        }
      }  
      
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();

    }
    catch(Exception e)
    {
    System.out.println(e);
      if(rs   != null) rs.close();     
      if(stmt != null) stmt.close();    
    }

    return count;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int process(Connection con, Statement stmt, ResultSet rs, RandomAccessFile fhData, boolean useWAC, String dateStartAccountingYear, String date, String invoiceCode, String itemCode, String qty, char dpOnQuantities, String dnm, String[] cssFormat)
  {
    int count = 0;

    try
    {
      String[] mfr     = new String[1];
      String[] mfrCode = new String[1];
      String[] desc    = new String[1];
      getItemDetailsGivenCode(con, stmt, rs, itemCode, mfr, mfrCode, desc);

      double qtyD = generalUtils.doubleFromStr(qty);
      
      double value = 0.0;
      if(useWAC)
      {
        double wac = inventory.getWAC(con, stmt, rs, itemCode, dateStartAccountingYear, date, dnm);

        value = wac * qtyD;
      }
      
      if(cssFormat[0].equals("line1")) cssFormat[0] = "line2"; else cssFormat[0] = "line1";
     
      if(qtyD != 0 && value <= 0)
      {
        scoutln(fhData, "<tr id='" + cssFormat[0] + "'><td><p><a href=\"javascript:viewInv('" + invoiceCode + "')\">" + invoiceCode + "</a></td>");
      
        scoutln(fhData, "<td><p>" + date + "</td>");
        scoutln(fhData, "<td><p><a href=\"javascript:viewItem('" + itemCode + "')\">" + itemCode + "</a></td>");
        scoutln(fhData, "<td><p>" + mfr[0] + "</td>");
        scoutln(fhData, "<td><p>" + mfrCode[0] + "</td>");
        scoutln(fhData, "<td align=center><p>" + generalUtils.formatNumeric(qtyD, dpOnQuantities) + "</td>");
        scoutln(fhData, "<td align=center><p>" + generalUtils.formatNumeric(value, '2') + "</td>");        
        scoutln(fhData, "<td nowrap><p>" + desc[0] + "</td></tr>");

        ++count;
      }
    }
    catch(Exception e)
    {
      System.out.println(e);
    }

    return count;
  }
    
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean itemExists(Connection con, Statement stmt, ResultSet rs, String itemCode) throws Exception
  {
    if(itemCode.length() == 0) // just-in-case
      return false;

    if(itemCode.equals("-")) // quick check
      return false;

    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM stock WHERE ItemCode = '" + generalUtils.sanitiseForSQL(itemCode) + "'");

    int numRecs = 0;
    
    if(rs.next())
      numRecs = rs.getInt("rowcount") ;

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    if(numRecs == 1)
      return true;
    
    return false;
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void getItemDetailsGivenCode(Connection con, Statement stmt, ResultSet rs, String itemCode, String[] mfr, String[] mfrCode, String[] desc) throws Exception
  {
    byte[] data = new byte[5000];
    
    if(inventory.getStockRecGivenCode(con, stmt, rs, itemCode, '\000', data) < 0) // just-in-case
    {
      mfr[0] = mfrCode[0] = desc[0] = "";
      return;
    }
 
    desc[0]    = generalUtils.dfsAsStr(data, (short)1);    
    mfr[0]     = generalUtils.dfsAsStr(data, (short)3);
    mfrCode[0] = generalUtils.dfsAsStr(data, (short)4);
    
    if(desc[0]    == null) desc[0] = "";
    if(mfr[0]     == null) mfr[0] = "";
    if(mfrCode[0] == null) mfrCode[0] = "";
    
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int[] bytesOut, String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);
  }
  private void scoutln(RandomAccessFile fh, String str) throws Exception
  {      
    fh.writeBytes(str + "\n");
  }
  
  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void keepChecking(PrintWriter out, String servlet, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "function chkTimer(){chkTimerID=self.setTimeout('chk()',4000);}");
      
    scoutln(out, bytesOut, "var chkreq2;");
    scoutln(out, bytesOut, "function initRequest2(url)");
    scoutln(out, bytesOut, "{if(window.XMLHttpRequest){chkreq2=new XMLHttpRequest();}else");
    scoutln(out, bytesOut, "if(window.ActiveXObject){chkreq2=new ActiveXObject(\"Microsoft.XMLHTTP\");}}");

    scoutln(out, bytesOut, "function chk(){");
    scoutln(out, bytesOut, "var url=\"http://" + men + "/central/servlet/FaxStatusDataFromReportTemp?p1=" + servlet + "&unm=\" + escape('" + unm + "') + \"&sid=\" + escape('" + sid + "') + \"&uty=\" + escape('" + uty + "') + \"&men=\" + escape('" + men
                         + "') + \"&den=\" + escape('" + den + "') + \"&bnm=\" + escape('" + bnm + "') + \"&dnm=\" + escape('" + dnm + "');");
    scoutln(out, bytesOut, "initRequest2(url);");
    scoutln(out, bytesOut, "chkreq2.onreadystatechange=processRequest2;");
    scoutln(out, bytesOut, "chkreq2.open(\"GET\",url,true);");
    scoutln(out, bytesOut, "chkreq2.send(null);}");
 
    scoutln(out, bytesOut, "function processRequest2(){");
    scoutln(out, bytesOut, "if(chkreq2.readyState==4){");
    scoutln(out, bytesOut, "if(chkreq2.status==200){");
    scoutln(out, bytesOut, "var res=chkreq2.responseXML.getElementsByTagName(\"res\")[0].childNodes[0].nodeValue;");
    scoutln(out, bytesOut, "if(res.length>0){");
    scoutln(out, bytesOut, "if(res=='.')clearTimeout(chkTimerID);else chkTimer();");
    scoutln(out, bytesOut, "var s=chkreq2.responseXML.getElementsByTagName(\"stuff\")[0].childNodes[0].nodeValue;");
    scoutln(out, bytesOut, "var messageElement=document.getElementById('stuff');");
    scoutln(out, bytesOut, "messageElement.innerHTML=s;");
    scoutln(out, bytesOut, "}}}}");
  }

}
