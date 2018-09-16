// =======================================================================================================================================================================================================
// System: ZaraStar Analytic: Word search all purchase document lines - do it
// Module: PurchasesWordSearchExecute.java
// Author: C.K.Harvey
// Copyright (c) 2003-11 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;
import java.util.*;
import java.io.*;

public class PurchasesWordSearchExecute extends HttpServlet
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

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", phrase="", companyCode="", dateFrom="", dateTo="", gr="", po="", lp="", pi="";

    try
    {
      directoryUtils.setContentHeaders2(res);
      out = res.getWriter();

      byte[] lines    = new byte[1000]; lines[0]    = '\000';
      int[]  linesLen = new int[1];     linesLen[0] = 1000;

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
        if(name.equals("phrase"))
          phrase = value[0];
        else
        if(name.equals("companyCode"))
          companyCode = value[0];
        else
        if(name.equals("dateFrom"))   
          dateFrom = value[0];
        else
        if(name.equals("dateTo"))
          dateTo = value[0];
        else
        if(name.equals("GR"))
          gr = value[0];
        else
        if(name.equals("PO"))
          po = value[0];
        else
        if(name.equals("LP"))
          lp = value[0];
        else
        if(name.equals("PI"))
          pi = value[0];
      }
      
      if(companyCode == null) companyCode = "";
      if(dateFrom == null) dateFrom = "";
      if(dateTo == null) dateTo = "";

      doIt(out, req, phrase, companyCode, dateFrom, dateTo, gr, po, lp, pi, unm, sid, uty, men, den, dnm, bnm, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "PurchasesWordSearchExecute", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1028, bytesOut[0], 0, "ERR:" + phrase);
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String phrase, String companyCode, String dateFrom, String dateTo, String gr, String po,
                    String lp, String pi, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut)
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
      
    Statement stmt=null, stmt2 = null;
    ResultSet rs = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 1028, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "PurchasesWordSearch", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1028, bytesOut[0], 0, "ACC:" + phrase);
      
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "PurchasesWordSearch", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1028, bytesOut[0], 0, "SID:" + phrase);
      
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    process(con, stmt, stmt2, rs, out, req, phrase, companyCode, dateFrom, dateTo, gr, po, lp, pi, unm, sid, uty, men, den, dnm, bnm, 
            localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 1028, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), phrase);
      
      if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void process(Connection con, Statement stmt, Statement stmt2, ResultSet rs, PrintWriter out, HttpServletRequest req, String phrase,
                       String companyCode, String dateFrom, String dateTo, String gr, String po, String lp, String pi, String unm, String sid,
                       String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut)
                       throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Document Word Search</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    scoutln(out, bytesOut, "function viewPO(code){var p1=sanitise(code);");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/PurchaseOrderPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    
    scoutln(out, bytesOut, "function viewCompany(code){var p1=sanitise(code);");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/CustomerPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p1=\"+p1;}");

    scoutln(out, bytesOut, "function viewLP(code){var p1=sanitise(code);");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/LocalPurchasePage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");

    scoutln(out, bytesOut, "function viewGRN(code){var p1=sanitise(code);");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/GoodsReceivedPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");

    scoutln(out, bytesOut, "function viewPI(code){var p1=sanitise(code);");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/PurchaseInvoicePage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3001, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewItem(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/ProductStockRecord?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p1=\"+p1;}");
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
    
    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\"></head>");
  
    int[] hmenuCount = new int[1];
    
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "1028", "", "PurchasesWordSearch", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
                                
    scoutln(out, bytesOut, "<form>");
  
    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Purchases Document Word Search Results", "1028",  unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<p>For Search Phrase: &nbsp; &nbsp; " + phrase);
    
    if(companyCode.length() == 0)
      scoutln(out, bytesOut, "<br>for all suppliers");
    else scoutln(out, bytesOut, "<br>for supplier " + companyCode);
    
    if(dateFrom.length() == 0)
      scoutln(out, bytesOut, "<br>from start");
    else scoutln(out, bytesOut, "<br>from " + dateFrom);
    
    if(dateTo.length() == 0)
      scoutln(out, bytesOut, " to end");
    else scoutln(out, bytesOut, " and to " + dateTo);
    
    scoutln(out, bytesOut, "<br><table id=\"page\" width=100%>");

    boolean anError = false;
    
    if(phrase.length() == 0)
    {
      scoutln(out, bytesOut, "<br><br><p>No search phrase specified<br><br>");
      anError = true;
    }
    
    if(companyCode.length() > 0 && ! supplier.existsSupplierRecGivenCode(con, stmt, rs, companyCode, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "<br><br><p>Supplier Code does not exist<br><br>");
      anError = true;
    }

    if(dateFrom.length() == 0)
      dateFrom = "1970-01-01";
    else
    if(! generalUtils.validateDate(false, dateFrom, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "<br><br><p>From Date is not of the correct format<br><br>");
      anError = true;
    }
    else dateFrom = generalUtils.convertDateToSQLFormat(dateFrom);
    
    if(dateTo.length() == 0)
      dateTo = "2030-12-31";
    else
    if(! generalUtils.validateDate(false, dateTo, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "<br><br><p>To Date is not of the correct format<br><br>");
      anError = true;
    } 
    else dateTo = generalUtils.convertDateToSQLFormat(dateTo);
    
    if(! anError)
    {
      String tmpTable = unm + "_tmp";
      
      directoryUtils.createTmpTable(true, con, stmt2, "DocumentCode char(20), DocumentType char(1), ItemCode char(20), Description char(80), Quantity decimal(19,8), UnitPrice decimal(19,8), Line char(10), SupplierItemCode char(40), "
                                           + "Wanted char(1), Date date, ManufacturerCode char(60), Discount decimal(17,8), SupplierName char(60)", "docTypeInx ON " + tmpTable + " (DocumentType)", tmpTable);

      int matchesCount = 0;
      
      if(gr.equals("on"))
      {
        matchesCount += docSearch(out, false, con, stmt, stmt2, tmpTable, "GRCode", "Date", "Quantity", "Goods Received Note", "gr", "G", "viewGRN",
                                  phrase, companyCode, "CompanyName", dateFrom, dateTo, localDefnsDir, defnsDir, bytesOut);
      }  

      if(po.equals("on"))
      {
        matchesCount += docSearch(out, true, con, stmt, stmt2, tmpTable, "POCode", "Date", "Quantity", "Purchase Order", "po", "Y", "viewPO", phrase,
                                  companyCode, "CompanyName", dateFrom, dateTo, localDefnsDir, defnsDir, bytesOut);
      }  

      if(lp.equals("on"))
      {
        matchesCount += docSearch(out, true, con, stmt, stmt2, tmpTable, "LPCode", "Date", "Quantity", "Local Purchase", "lp", "Z", "viewLP", phrase,
                                  companyCode, "CompanyName", dateFrom, dateTo, localDefnsDir, defnsDir, bytesOut);
      }  

      if(pi.equals("on"))
      {
        matchesCount += docSearch(out, true, con, stmt, stmt2, tmpTable, "InvoiceCode", "Date", "Quantity", "Purchase Invoice", "pinvoice", "I",
                                  "viewPI", phrase, companyCode, "CompanyName", dateFrom, dateTo, localDefnsDir, defnsDir, bytesOut);
      }  

      if(matchesCount == 0)
        scoutln(out, bytesOut, "<br><br><p>No Matches<br><br>");
      
    }

    scoutln(out, bytesOut, "</table></form>");     scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private int docSearch(PrintWriter out, boolean searchLL, Connection con, Statement stmt, Statement stmt2, String tmpTable, String codeFieldName, String dateFieldName, String qtyFieldName, String docName, String tableName, String docType,
                        String jsCall, String phrase, String companyCode, String companyNameFieldName, String dateFrom, String dateTo, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    ResultSet rs=null;
   
    stmt = con.createStatement();
    
    switch(docType.charAt(0))
    {
      case 'Y' : 
      case 'Z' : if(companyCode.length() > 0)
                 {
                   rs = stmt.executeQuery("SELECT t2." + codeFieldName + ", t2.ItemCode, t2.Description, t2.Line, t2." + qtyFieldName
                                        + ", t2.UnitPrice, t1." + dateFieldName + ", t2.SupplierItemCode, t2.ManufacturerCode, t2.Discount, t1." + companyNameFieldName + " FROM "
                                        + tableName + "l AS t2 INNER JOIN " + tableName + " AS t1 ON t2." + codeFieldName + " = t1." + codeFieldName
                                        + " WHERE t2.Description LIKE '%" + phrase + "%' AND t1.CompanyCode = '" + companyCode + "' AND t1."
                                        + dateFieldName + " >= {d '" + dateFrom + "'} AND t1." + dateFieldName + " <= {d '" + dateTo
                                        + "'} ORDER BY t2." + codeFieldName + ", t2.Line");
                 }
                 else
                 {
                   rs = stmt.executeQuery("SELECT t2." + codeFieldName + ", t2.ItemCode, t2.Description, t2.Line, t2." + qtyFieldName
                                        + ", t2.UnitPrice, t1." + dateFieldName + ", t2.SupplierItemCode, t2.ManufacturerCode, t2.Discount, t1." + companyNameFieldName + " FROM "
                                        + tableName + "l AS t2 INNER JOIN " + tableName + " AS t1 ON t2." + codeFieldName + " = t1." + codeFieldName
                                        + " WHERE t2.Description LIKE '%" + phrase + "%' AND t1." + dateFieldName + " >= {d '" + dateFrom
                                        + "'} AND t1." + dateFieldName + " <= {d '" + dateTo + "'} ORDER BY t2." + codeFieldName + ", t2.Line");
                 }

                 while(rs.next())
                 {  
                   addToTmpTable(con, stmt2, rs.getString(1), docType, rs.getString(2), rs.getString(3), rs.getString(5), rs.getString(4),
                                 rs.getString(6), rs.getString(7), rs.getString(8), rs.getString(9), rs.getString(10), rs.getString(11), tmpTable);
                 }
                 break;
      case 'G' : if(companyCode.length() > 0)
                 {
                   rs = stmt.executeQuery("SELECT t2." + codeFieldName + ", t2.ItemCode, t2.Description, t2.Line, t2." + qtyFieldName
                                        + ", t2.UnitPrice, t1." + dateFieldName + ", t2.ManufacturerCode, t1." + companyNameFieldName + " FROM " + tableName + "l AS t2 INNER JOIN "
                                        + tableName + " AS t1 ON t2." + codeFieldName + " = t1." + codeFieldName + " WHERE t2.Description LIKE '%"
                                        + phrase + "%' AND t1.CompanyCode = '" + companyCode + "' AND t1." + dateFieldName + " >= {d '" + dateFrom
                                        + "'} AND t1." + dateFieldName + " <= {d '" + dateTo + "'} ORDER BY t2." + codeFieldName + ", t2.Line");
                 }
                 else
                 {
                   rs = stmt.executeQuery("SELECT t2." + codeFieldName + ", t2.ItemCode, t2.Description, t2.Line, t2." + qtyFieldName
                                        + ", t2.UnitPrice, t1." + dateFieldName + ", t2.ManufacturerCode, t1." + companyNameFieldName + " FROM " + tableName + "l AS t2 INNER JOIN "
                                        + tableName + " AS t1 ON t2." + codeFieldName + " = t1." + codeFieldName + " WHERE t2.Description LIKE '%"
                                        + phrase + "%' AND t1." + dateFieldName + " >= {d '" + dateFrom + "'} AND t1." + dateFieldName + " <= {d '"
                                        + dateTo + "'} ORDER BY t2." + codeFieldName + ", t2.Line");
                 }

                 while(rs.next())
                 {  
                  addToTmpTable(con, stmt2, rs.getString(1), docType, rs.getString(2), rs.getString(3), rs.getString(5), rs.getString(4),
                                 rs.getString(6), rs.getString(7), "", rs.getString(8), "0", rs.getString(9), tmpTable);
                 }
                 break;
      case 'I' : if(companyCode.length() > 0)
                 {
                   rs = stmt.executeQuery("SELECT t2." + codeFieldName + ", t2.ItemCode, t2.Description, t2.Line, t2." + qtyFieldName
                                        + ", t2.UnitPrice, t1." + dateFieldName + ", t2.ManufacturerCode, t2.Discount, t1." + companyNameFieldName + " FROM " + tableName
                                        + "l AS t2 INNER JOIN " + tableName + " AS t1 ON t2." + codeFieldName + " = t1." + codeFieldName
                                        + " WHERE t2.Description LIKE '%" + phrase + "%' AND t1.CompanyCode = '" + companyCode + "' AND t1."
                                        + dateFieldName + " >= {d '" + dateFrom + "'} AND t1." + dateFieldName + " <= {d '" + dateTo
                                        + "'} ORDER BY t2." + codeFieldName + ", t2.Line");
                 }
                 else
                 {
                   rs = stmt.executeQuery("SELECT t2." + codeFieldName + ", t2.ItemCode, t2.Description, t2.Line, t2." + qtyFieldName
                                        + ", t2.UnitPrice, t1." + dateFieldName + ", t2.ManufacturerCode, t2.Discount, t1." + companyNameFieldName + " FROM " + tableName
                                        + "l AS t2 INNER JOIN " + tableName + " AS t1 ON t2." + codeFieldName + " = t1." + codeFieldName
                                        + " WHERE t2.Description LIKE '%" + phrase + "%' AND t1." + dateFieldName + " >= {d '" + dateFrom
                                        + "'} AND t1." + dateFieldName + " <= {d '" + dateTo + "'} ORDER BY t2." + codeFieldName + ", t2.Line");
                 }

                 while(rs.next())
                 {  
                   addToTmpTable(con, stmt2, rs.getString(1), docType, rs.getString(2), rs.getString(3), rs.getString(5), rs.getString(4),
                                 rs.getString(6), rs.getString(7), "", rs.getString(8), rs.getString(9), rs.getString(10), tmpTable);
                 }
                 break;
    }
                 
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    if(searchLL)
    {
      stmt = con.createStatement();
        
      if(companyCode.length() > 0)
      {
        rs = stmt.executeQuery("SELECT t2." + codeFieldName + ", t2.Text, t2.Entry, t1." + dateFieldName + " FROM " + tableName + "ll AS t2 INNER JOIN "
                           + tableName + " AS t1 ON t2." + codeFieldName + " = t1." + codeFieldName + " WHERE t2.Text LIKE '%" + phrase
                           + "%' AND t1.CompanyCode = '" + companyCode + "' AND t1." + dateFieldName + " >= {d '" + dateFrom + "'} AND t1."
                           + dateFieldName + " <= {d '" + dateTo + "'} ORDER BY t1." + codeFieldName + ", t2.Line");
      }
      else
      {
        rs = stmt.executeQuery("SELECT t2." + codeFieldName + ", t2.Text, t2.Entry, t1." + dateFieldName + " FROM " + tableName + "ll AS t2 INNER JOIN "
                            + tableName + " AS t1 ON t2." + codeFieldName + " = t1." + codeFieldName + " WHERE t2.Text LIKE '%" + phrase + "%' AND t1."
                            + dateFieldName + " >= {d '" + dateFrom + "'} AND t1." + dateFieldName + " <= {d '" + dateTo + "'} ORDER BY t1."
                            + codeFieldName + ", t2.Line");
      }
    
      while(rs.next())
      {    
        addToTmpTable(con, stmt2, rs.getString(1), docType, "", rs.getString(2), "0", rs.getString(3), "0", rs.getString(4), "", "", "0", "", tmpTable);
      }

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }  

    // output results

    char dpOnQuantities = miscDefinitions.dpOnQuantities(con, stmt, rs, "5");

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT DocumentCode, ItemCode, Description, Quantity, Line, SupplierItemCode, UnitPrice, Date, ManufacturerCode, "
                          + "Discount, SupplierName FROM " + tmpTable + " WHERE DocumentType = '" + docType + "' AND Wanted = 'Y'");
    
    byte[] qtyB = new byte[20];
    String code, itemCode, line, desc, qty, unitPrice, date, cssFormat="", suppItemCode, mfrCode, discount, suppName;
    boolean first = true;
    int count=0;

    while(rs.next())
    {    
      code         = rs.getString(1);
      itemCode     = rs.getString(2);
      desc         = rs.getString(3);
      qty          = rs.getString(4);
      line         = rs.getString(5);
      suppItemCode = rs.getString(6);
      unitPrice    = rs.getString(7);
      date         = rs.getString(8);
      mfrCode      = rs.getString(9);
      discount     = rs.getString(10);
      suppName     = rs.getString(11);

      if(mfrCode == null)
        mfrCode = "";

      if(first)
      {
        scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
        scoutln(out, bytesOut, "<tr id=\"pageColumn\"><td colspan=3>" + docName + " Lines</td></tr>");
        scoutln(out, bytesOut, "<tr id=\"pageColumn\">");
        scoutln(out, bytesOut, "<td nowrap><p>Code</td>");
        scoutln(out, bytesOut, "<td nowrap><p>Line</td>");
        scoutln(out, bytesOut, "<td nowrap><p>Date</td>");
        scoutln(out, bytesOut, "<td nowrap><p>Item Code</td>");
        scoutln(out, bytesOut, "<td nowrap><p>Mfr Code</td>");
        scoutln(out, bytesOut, "<td nowrap><p>Supplier Item Code</td>");
        scoutln(out, bytesOut, "<td nowrap><p>Quantity</td>");
        scoutln(out, bytesOut, "<td nowrap><p>Unit Price</td>");
        scoutln(out, bytesOut, "<td nowrap><p>Discount %</td>");
        scoutln(out, bytesOut, "<td nowrap><p>Description</td>");
        scoutln(out, bytesOut, "<td nowrap><p>Supplier Name</td></tr>");
        first = false;
      }

      if(cssFormat.equals("line1"))
        cssFormat = "line2";
      else cssFormat = "line1";

      generalUtils.strToBytes(qtyB, qty);
      generalUtils.bytesDPsGivenSeparator(true, '\000', dpOnQuantities, qtyB, 20, 0);
      generalUtils.formatNumeric(qtyB, dpOnQuantities);

      scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\">");

      scoutln(out, bytesOut, "<td><a href=\"javascript:" + jsCall + "('" + code + "')\">" + code + "</a></td>");
      scoutln(out, bytesOut, "<td>" + line + "</td>");
      scoutln(out, bytesOut, "<td>" + generalUtils.convertFromYYYYMMDD(date) + "</td>");
      scoutln(out, bytesOut, "<td>" + itemCode + "</td>");
      scoutln(out, bytesOut, "<td>" + mfrCode + "</td>");
      scoutln(out, bytesOut, "<td>" + suppItemCode + "</td>");
      scoutln(out, bytesOut, "<td align=right>" + generalUtils.stringFromBytes(qtyB, 0L) + " </td>");
      scoutln(out, bytesOut, "<td align=right>" + generalUtils.doubleDPs(unitPrice, '2') + " </td>");
      scoutln(out, bytesOut, "<td align=right>" + generalUtils.doubleDPs(discount, '2') + " </td>");
      scoutln(out, bytesOut, "<td nowrap>" + desc + "</td>");
      scoutln(out, bytesOut, "<td nowrap>" + suppName + "</td></tr>");
        
      ++count;
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
      
    return count;
  } 
     
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void addToTmpTable(Connection con, Statement stmt, String docCode, String docType, String itemCode, String desc, String qty, String line,
                             String unitPrice, String date, String suppItemCode, String mfrCode, String discount, String companyName, String tmpTable) throws Exception
  {
    if(mfrCode == null)
      mfrCode = "";

    stmt = con.createStatement();
   
    String q = "INSERT INTO " + tmpTable + " ( DocumentCode, DocumentType, ItemCode, Description, Quantity, Line, SupplierItemCode, UnitPrice, "
             + "Wanted, Date, ManufacturerCode, Discount, SupplierName ) VALUES ('" + docCode + "', '" + docType + "', '" + generalUtils.sanitiseForSQL(itemCode) + "', '"
             + generalUtils.sanitiseForSQL(desc) + "', '" + qty + "', '" + line + "', '" + generalUtils.sanitiseForSQL(suppItemCode) + "', '" + unitPrice
             + "', 'Y', {d '" + date + "'}, '" + generalUtils.sanitiseForSQL(mfrCode) + "','" + discount + "', '" + generalUtils.sanitiseForSQL(companyName) + "')";

    stmt.executeUpdate(q);
  
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
