// =======================================================================================================================================================================================================
// System: ZaraStar Analytic: Word search all sales document lines - do it
// Module: SalesWordSearchExecute.java
// Author: C.K.Harvey
// Copyright (c) 2003-08 Christopher Harvey. All Rights Reserved.
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

public class SalesWordSearchExecute extends HttpServlet
{
  GeneralUtils  generalUtils = new GeneralUtils();
  MessagePage  messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils  pageFrameUtils = new PageFrameUtils();
  DirectoryUtils  directoryUtils = new DirectoryUtils();
  Customer customer = new Customer();
  MiscDefinitions  miscDefinitions = new MiscDefinitions();
  AdminControlUtils adminControlUtils = new AdminControlUtils();
  
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", phrase="", companyCode="", dateFrom="", dateTo="", q="", so="", pl="", dorder="",
           i="", pr="";

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
        if(name.equals("Q"))
          q = value[0];
        else
        if(name.equals("SO"))
          so = value[0];
        else
        if(name.equals("PL"))
          pl = value[0];
        else
        if(name.equals("DO"))
          dorder = value[0];
        else
        if(name.equals("I"))
          i = value[0];
        else
        if(name.equals("PR"))
          pr = value[0];
      }
      
      if(companyCode == null) companyCode = "";
      if(dateFrom == null) dateFrom = "";
      if(dateTo == null) dateTo = "";

      doIt(out, req, phrase, companyCode, dateFrom, dateTo, q, so, pl, dorder, i, pr, unm, sid, uty, men, den, dnm, bnm, bytesOut);
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
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "MainPageUtils8a", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1008, bytesOut[0], 0, "ERR:" + phrase + " " + companyCode + " " + dateFrom + " " + dateTo);
      if(out != null) out.flush(); 
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String phrase, String companyCode, String dateFrom, String dateTo, String q, String so, String pl, String dorder, String i, String pr, String unm, String sid, String uty, String men,
                    String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String localDefnsDir  = directoryUtils.getLocalOverrideDir(dnm);
    String defnsDir       = directoryUtils.getSupportDirs('D');
    String imagesDir      = directoryUtils.getSupportDirs('I');
    
    String userName = directoryUtils.getMySQLUserName();
    String passWord = directoryUtils.getMySQLPassWord();
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + userName + "&password=" + passWord);
      
    Statement stmt=null, stmt2 = null;
    ResultSet rs = null;

    boolean isRegistered = false;

    int j = unm.indexOf("_");
    if(j != -1)
    {
      if(! adminControlUtils.notDisabled(con, stmt, rs, 903))
      {
        messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "_909", imagesDir, localDefnsDir, defnsDir, bytesOut);
        serverUtils.etotalBytes(req, unm, dnm, 909, bytesOut[0], 0, "ACC:");
        if(con != null) con.close();
        if(out != null) out.flush(); 
        return;
      }
      else isRegistered = true;
    }
    else
    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 1008, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "MainPageUtils8", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1008, bytesOut[0], 0, "ACC:" + phrase + " " + companyCode + " " + dateFrom + " " + dateTo);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "MainPageUtils8", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1008, bytesOut[0], 0, "SID:" + phrase + " " + companyCode + " " + dateFrom + " " + dateTo);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    process(con, stmt, stmt2, rs, out, req, isRegistered, j, phrase, companyCode, dateFrom, dateTo, q, so, pl, dorder, i, pr, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    String docs = "";
    if(q.equals("on")) docs += "Q";
    if(so.equals("on")) docs += "S";
    if(pl.equals("on")) docs += "P";
    if(dorder.equals("on")) docs += "D";
    if(i.equals("on")) docs += "I";
    if(pr.equals("on")) docs += "R";

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 1008, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), phrase + " " + companyCode + " " + dateFrom + " " + dateTo + " " + docs);
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void process(Connection con, Statement stmt, Statement stmt2, ResultSet rs, PrintWriter out, HttpServletRequest req, boolean isRegistered, int j, String phrase, String companyCode, String dateFrom, String dateTo, String q, String so,
                       String pl, String dorder, String i, String pr, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Document Word Search</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    if(isRegistered)
    {
      scoutln(out, bytesOut, "function viewInvoice(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/SalesInvoiceRegisteredUser?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+p1;}");

      scoutln(out, bytesOut, "function viewQuote(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/QuotationRegisteredUser?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+p1;}");

      scoutln(out, bytesOut, "function viewSO(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/SalesOrderRegisteredUser?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+p1;}");

      scoutln(out, bytesOut, "function viewDO(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/DeliveryOrdeRegisteredUser?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+p1;}");

      scoutln(out, bytesOut, "function viewItem(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/StockEnquiryExternal?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p1=\"+p1;}");
    }
    else
    {
      scoutln(out, bytesOut, "function viewCompany(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/CustomerPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p1=\"+p1;}");

      scoutln(out, bytesOut, "function viewProforma(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/ProformaInvoicePage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");

      scoutln(out, bytesOut, "function viewInvoice(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/SalesInvoicePage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");

      scoutln(out, bytesOut, "function viewQuote(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/QuotationPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");

      scoutln(out, bytesOut, "function viewSO(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/SalesOrderPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");

      scoutln(out, bytesOut, "function viewDO(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/DeliveryOrderPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");

      scoutln(out, bytesOut, "function viewOC(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/OrderConfirmationPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");

      scoutln(out, bytesOut, "function viewPL(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/GoodsReceivedPickingList?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");

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
    
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "1008", "", "MainPageUtils8", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir,
                          hmenuCount, bytesOut);
                                
    scoutln(out, bytesOut, "<form>");
  
    if(isRegistered)
    {
      String[] name         = new String[1];
      String[] companyName  = new String[1];
      String[] accessRights = new String[1];
      Profile profile = new Profile();    
      profile.getExternalAccessNameCompanyAndRights(con, stmt, rs, unm.substring(0, j), name, companyName, accessRights);

      pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Sales Document Word Search for " + name[0] + " of " + companyName[0], "909", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);
    }
    else
    {
      pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Sales Document Word Search Results", "1008",  unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);
    }

    scoutln(out, bytesOut, "<p>For Search Phrase: &nbsp; &nbsp; " + phrase);
    
    phrase = generalUtils.sanitiseForSQL(phrase);
        
    if(! isRegistered)
    {
      if(companyCode.length() == 0)
        scoutln(out, bytesOut, "<br>for all customers");
      else scoutln(out, bytesOut, "<br>for customer " + companyCode);
    }
    
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
    
    if(companyCode.length() > 0 && ! customer.existsCompanyRecGivenCode(con, stmt, rs, companyCode, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "<br><br><p>Customer Code does not exist<br><br>");
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
      String tmpTable;
      if(isRegistered)
         tmpTable = unm.substring(0, j) + "_tmp";
      else tmpTable = unm + "_tmp";

      directoryUtils.createTmpTable(true, con, stmt2, "DocumentCode char(20), DocumentType char(1), ItemCode char(20), Description char(80), "
                                           + "Quantity decimal(19,8), UnitPrice decimal(19,8), Line char(10), Wanted char(1), Date date, "
                                           + "ManufacturerCode char(60), Discount decimal(17,8) ", "docTypeInx ON " + tmpTable + " (DocumentType)",
                                           tmpTable);

      int matchesCount = 0;
      
      if(q.equals("on"))
      {
        matchesCount += docSearch(out, con, stmt, stmt2, tmpTable, "QuoteCode", "QuoteDate", "Quantity", "Quotation", "quote", "Q", "viewQuote",
                                  phrase, companyCode, dateFrom, dateTo, localDefnsDir, defnsDir, bytesOut);
      }  

      if(so.equals("on"))
      {
        matchesCount += docSearch(out, con, stmt, stmt2, tmpTable, "SOCode", "Date", "Quantity", "Sales Order", "so", "S", "viewSO", phrase,
                                  companyCode, dateFrom, dateTo, localDefnsDir, defnsDir, bytesOut);
      }  

      if(pl.equals("on"))
      {
        matchesCount += docSearch(out, con, stmt, stmt2, tmpTable, "PLCode", "Date", "QuantityPacked", "Picking List", "pl", "P", "viewPL", phrase,
                                  companyCode, dateFrom, dateTo, localDefnsDir, defnsDir, bytesOut);
      }  

      if(dorder.equals("on"))
      {
        matchesCount += docSearch(out, con, stmt, stmt2, tmpTable, "DOCode", "Date", "Quantity", "Delivery Order", "do", "D", "viewDO", phrase,
                                  companyCode, dateFrom, dateTo, localDefnsDir, defnsDir, bytesOut);
      }  

      if(i.equals("on"))
      {
        matchesCount += docSearch(out, con, stmt, stmt2, tmpTable, "InvoiceCode", "Date", "Quantity", "Invoice", "invoice", "I", "viewInvoice",
                                  phrase, companyCode, dateFrom, dateTo, localDefnsDir, defnsDir, bytesOut);
      }  

      if(pr.equals("on"))
      {
        matchesCount += docSearch(out, con, stmt, stmt2, tmpTable, "ProformaCode", "Date", "Quantity", "Proforma Invoice", "proforma", "R",
                                  "viewProforma", phrase, companyCode, dateFrom, dateTo, localDefnsDir, defnsDir, bytesOut);
      }  

      if(matchesCount == 0)
        scoutln(out, bytesOut, "<br><br><p>No Matches<br><br>");
      
    }

    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir));
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int docSearch(PrintWriter out, Connection con, Statement stmt, Statement stmt2, String tmpTable, String codeFieldName, String dateFieldName,
                        String qtyFieldName, String docName, String tableName, String docType, String jsCall, String phrase, String companyCode,
                        String dateFrom, String dateTo, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    ResultSet rs;
   
    stmt = con.createStatement();
    
    if(companyCode.length() > 0)
    {
      rs = stmt.executeQuery("SELECT t2." + codeFieldName + ", t2.ItemCode, t2.Description, t2.Line, t2." + qtyFieldName + ", t2.UnitPrice, "
                           + " t1." + dateFieldName + ", t2.ManufacturerCode, t2.Discount FROM " + tableName + "l AS t2 INNER JOIN " + tableName
                           + " AS t1 ON t2." + codeFieldName + " = t1." + codeFieldName + " WHERE t2.Description LIKE '%" + phrase
                           + "%' AND t1.CompanyCode = '" + companyCode + "' AND t1." + dateFieldName + " >= {d '" + dateFrom + "'} AND t1."
                           + dateFieldName + " <= {d '" + dateTo + "'} ORDER BY t2." + codeFieldName + ", t2.Line");
    }
    else
    {
      rs = stmt.executeQuery("SELECT t2." + codeFieldName + ", t2.ItemCode, t2.Description, t2.Line, t2." + qtyFieldName + ", t2.UnitPrice, "
                           + "t1." + dateFieldName + ", t2.ManufacturerCode, t2.Discount FROM " + tableName + "l AS t2 INNER JOIN " + tableName
                           + " AS t1 ON t2." + codeFieldName + " = t1." + codeFieldName + " WHERE t2.Description LIKE '%" + phrase + "%' AND t1."
                           + dateFieldName + " >= {d '" + dateFrom + "'} AND t1." + dateFieldName + " <= {d '" + dateTo + "'} ORDER BY t2."
                           + codeFieldName + ", t2.Line");
    }

    while(rs.next())
    {    
      addToTmpTable(con, stmt2, rs.getString(1), docType, rs.getString(2), rs.getString(3), rs.getString(5), rs.getString(4), rs.getString(6),
                    rs.getString(7), rs.getString(8), rs.getString(9), tmpTable);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    stmt = con.createStatement();
    
    if(companyCode.length() > 0)
    {
      rs = stmt.executeQuery("SELECT t2." + codeFieldName + ", t2.Text, t2.Entry, t1." + dateFieldName + " FROM " + tableName
                           + "ll AS t2 INNER JOIN " + tableName + " AS t1 ON t2." + codeFieldName + " = t1." + codeFieldName
                           + " WHERE t2.Text LIKE '%" + phrase + "%' AND t1.CompanyCode = '" + companyCode + "' AND t1." + dateFieldName + " >= {d '"
                           + dateFrom + "'} AND t1." + dateFieldName + " <= {d '" + dateTo + "'} ORDER BY t2." + codeFieldName + ", t2.Line");
    }
    else
    {
      rs = stmt.executeQuery("SELECT t2." + codeFieldName + ", t2.Text, t2.Entry, t1." + dateFieldName + " FROM " + tableName
                          + "ll AS t2 INNER JOIN " + tableName + " AS t1 ON t2." + codeFieldName + " = t1." + codeFieldName + " WHERE t2.Text LIKE '%"
                          + phrase + "%' AND t1." + dateFieldName + " >= {d '" + dateFrom + "'} AND t1." + dateFieldName + " <= {d '" + dateTo
                          + "'} ORDER BY t2." + codeFieldName + ", t2.Line");
    }
    
    while(rs.next())
    {    
      addToTmpTable(con, stmt2, rs.getString(1), docType, "", rs.getString(2), "0", rs.getString(3), "0", rs.getString(4), "", "0", tmpTable);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    // output results

    char dpOnQuantities = miscDefinitions.dpOnQuantities(con, stmt, rs, "5");

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT DocumentCode, ItemCode, Description, Quantity, Line, UnitPrice, Date, ManufacturerCode, Discount FROM " + tmpTable
                          + " WHERE DocumentType = '" + docType + "' AND Wanted = 'Y' ORDER BY DocumentCode");
    
    byte[] qtyB = new byte[20];
    String code, itemCode, line, desc, qty, unitPrice, date, mfrCode, discount, cssFormat="";
    boolean first = true;
    int count=0;

    while(rs.next())
    {    
      code      = rs.getString(1);
      itemCode  = rs.getString(2);
      desc      = rs.getString(3);
      qty       = rs.getString(4);
      line      = rs.getString(5);
      unitPrice = rs.getString(6);
      date      = rs.getString(7);
      mfrCode   = rs.getString(8);
      discount  = rs.getString(9);

      if(mfrCode == null)
        mfrCode = "";
      
      if(first)
      {
        scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
        scoutln(out, bytesOut, "<tr id=\"pageColumn\"><td colspan=3><p>" + docName + " Lines</td></tr>");
        scoutln(out, bytesOut, "<tr id=\"pageColumn\">");
//        if(companyCode.length() == 0)
  //        scoutln(out, bytesOut, "<td nowrap>Company Code</td>");
        scoutln(out, bytesOut, "<td nowrap><p>Code</td>");
        scoutln(out, bytesOut, "<td nowrap><p>Line</td>");
        scoutln(out, bytesOut, "<td nowrap><p>Date</td>");
        scoutln(out, bytesOut, "<td nowrap><p>Item Code</td>");
        scoutln(out, bytesOut, "<td nowrap><p>Mfr Code</td>");
        scoutln(out, bytesOut, "<td nowrap><p>Quantity</td>");
        scoutln(out, bytesOut, "<td nowrap><p>Unit Price</td>");
        scoutln(out, bytesOut, "<td nowrap><p>Discount %</td>");
        scoutln(out, bytesOut, "<td nowrap><p>Description</td></tr>");
        first = false;
      }

      if(cssFormat.equals("line1"))
        cssFormat = "line2";
      else cssFormat = "line1";

      generalUtils.strToBytes(qtyB, qty);
      generalUtils.bytesDPsGivenSeparator(true, '\000', dpOnQuantities, qtyB, 20, 0);
      generalUtils.formatNumeric(qtyB, dpOnQuantities);
      
      scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\">");
    //  if(companyCode.length() == 0)
      //  scoutln(out, bytesOut, "<td><a href=\"javascript:viewCompany('" + companyCode + "')\">" + companyCode + "</a></td>");
      scoutln(out, bytesOut, "<td><a href=\"javascript:" + jsCall + "('" + code + "')\">" + code + "</a></td>");
      scoutln(out, bytesOut, "<td>" + line + "</td>");
      scoutln(out, bytesOut, "<td>" + generalUtils.convertFromYYYYMMDD(date) + "</td>");
      scoutln(out, bytesOut, "<td><a href=\"javascript:viewItem('" + itemCode + "')\">" + itemCode + "</a></td>");
      scoutln(out, bytesOut, "<td>" + mfrCode + "</td>");
      scoutln(out, bytesOut, "<td align=right>" + generalUtils.stringFromBytes(qtyB, 0L) + " </td>");
      scoutln(out, bytesOut, "<td align=right>" + generalUtils.doubleDPs(unitPrice, '2') + " </td>");
      scoutln(out, bytesOut, "<td align=right>" + generalUtils.doubleDPs(discount, '2') + " </td>");
      scoutln(out, bytesOut, "<td nowrap>" + desc + "</td></tr>");
        
      ++count;
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
      
    return count;
  } 

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void addToTmpTable(Connection con, Statement stmt, String docCode, String docType, String itemCode, String desc, String qty, String line,
                             String unitPrice, String date, String mfrCode, String discount, String tmpTable) throws Exception
  {
    if(mfrCode == null)
      mfrCode = "";

    stmt = con.createStatement();
   
    String q = "INSERT INTO " + tmpTable + " ( DocumentCode, DocumentType, ItemCode, Description, Quantity, Line, UnitPrice, Wanted, Date, "
             + "ManufacturerCode, Discount ) VALUES ('" + docCode + "', '" + docType + "', '" + generalUtils.sanitiseForSQL(itemCode) + "', '"
             + generalUtils.sanitiseForSQL(desc) + "', '" + qty + "', '" + line + "', '" + unitPrice + "', 'Y', {d '" + date + "'}, '"
             + generalUtils.sanitiseForSQL(mfrCode) + "','" + discount + "')";

    stmt.executeUpdate(q);
  
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
