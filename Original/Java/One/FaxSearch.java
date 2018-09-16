// =======================================================================================================================================================================================================
// System: ZaraStar: Fax: search historical
// Module: FaxSearch.java
// Author: C.K.Harvey
// Copyright (c) 2000-08 Christopher Harvey. All Rights Reserved.
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
import java.io.*;
import java.sql.*;

public class FaxSearch extends HttpServlet
{
  GeneralUtils  generalUtils = new GeneralUtils();
  MessagePage  messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils  directoryUtils = new DirectoryUtils();
  FaxUtils faxUtils = new FaxUtils();
  
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="", p3="", p4="", p5="", p6="", p7="", p8="", p9="", p10="",
           p11="";
                         
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
      p1  = req.getParameter("p1"); // dateFrom
      p2  = req.getParameter("p2"); // dateTo
      p3  = req.getParameter("p3"); // number
      p4  = req.getParameter("p4"); // person
      p5  = req.getParameter("p5"); // company
      p6  = req.getParameter("p6"); // docType
      p7  = req.getParameter("p7"); // docCode
      p8  = req.getParameter("p8"); // subject
      p9  = req.getParameter("p9"); // companyCode
      p10 = req.getParameter("p10"); // companyType
      p11 = req.getParameter("p11"); // userOrAll
      
      if(p1 == null) p1 = "";
      if(p2 == null) p2 = "";
      if(p3 == null) p3 = "";
      if(p4 == null) p4 = "";
      if(p5 == null) p5 = "";
      if(p6 == null) p6 = "";
      if(p7 == null) p7 = "";
      if(p8 == null) p8 = "";
      if(p9 == null) p9 = "";
      if(p10 == null) p10 = "";
      if(p11 == null) p11 = "U";
      
      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, bytesOut);
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

      System.out.println("11003a: " + e);
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "FaxSearch", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 11003, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm,
                    String bnm, String p1, String p2, String p3, String p4, String p5, String p6, String p7, String p8, String p9, String p10,
                    String p11, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    
    Statement stmt = null;
    ResultSet rs   = null;
    
    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 11000, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "FaxHistory", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 11003, bytesOut[0], 0, "ACC:" + p1);
      
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "FaxHistory", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 11003, bytesOut[0], 0, "SID:" + p1);
      
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    String dateFrom;
    if(p1.length() == 0)
      dateFrom = "1970-01-01";
    else dateFrom = generalUtils.convertDateToSQLFormat(p1);
    
    String dateTo;
    if(p2.length() == 0)
      dateTo = "2099-12-31";
    else dateTo = generalUtils.convertDateToSQLFormat(p2);
    
    String number = generalUtils.stripAllNonNumeric(p3);
    
    String person = generalUtils.stripLeadingAndTrailingSpaces(p4);
    
    String company = generalUtils.stripLeadingAndTrailingSpaces(p5);
    String companyCode = generalUtils.stripLeadingAndTrailingSpaces(p9);
    
    String docCode = generalUtils.stripLeadingAndTrailingSpaces(p7);
    
    String subject = generalUtils.stripLeadingAndTrailingSpaces(p8);
                            
    set(con, stmt, rs, out, req, dateFrom, dateTo, number, person, company, companyCode, p10, p7, docCode, subject, p11, unm, sid, uty, men, den, dnm,
        bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 11003, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
      
      if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String dateFrom, String dateTo,
                   String number, String person, String company, String companyCode, String companyType, String docType, String docCode,
                   String subject, String signOn, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                   String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Historical Faxes</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    scoutln(out, bytesOut, "function view(type,code){if(type=='Q')s=4019;else if(type=='O')s=4053;else if(type=='P')s=3038;else if(type=='R')s=4080;");
    scoutln(out, bytesOut, "else if(type=='Y')s=5006;else if(type=='I')s=4067;else if(type=='A')s=5049;else if(type=='E')s=4205;else if(type=='C')s=4101;else if(type=='Z')s=5016;");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/_\"+s+\"?unm=" + unm + "&uty=" + uty + "&sid=" + sid + "&men=" + men + "&den="
                           + den + "&dnm=" + dnm + "&p1=\"+code+\"&bnm=" + bnm + "\";}");    
    
    scoutln(out, bytesOut, "function resend(faxCode){");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/FaxResend?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+faxCode;}");

    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    int[] hmenuCount = new int[1];

    faxUtils.outputPageFrame(con, stmt, rs, out, req, "", "FaxHistory", "11003", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount, bytesOut);

    faxUtils.drawTitle(con, stmt, rs, req, out, "Historical Faxes", "11003", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<form><table id=\"page\" width=100% border=0>");

    search(out, dateFrom, dateTo, number, person, company, companyCode, companyType, docType, docCode, subject, signOn, unm, dnm, defnsDir, bytesOut);

    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void search(PrintWriter out, String dateFrom, String dateTo, String number, String person, String company, String companyCode, String companyType, String docType, String docCode, String subject, String signOn, String unm, String dnm,
                      String localDefnsDir, int[] bytesOut) throws Exception
  {
    String uName = directoryUtils.getMySQLUserName();
    String pWord = directoryUtils.getMySQLPassWord();
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_fax?user=" + uName + "&password=" + pWord);
    Statement stmt = con.createStatement();

    String where = "DateTime >= {d '" + dateFrom + " 00:00:00'} AND DateTime <= {d '" + dateTo + " 23:59:59'}";
    
    if(number.length() > 0)
      where += " AND Number LIKE '%" + generalUtils.sanitiseForSQL(number) + "%'";
              
    if(person.length() > 0)
      where += " AND Person LIKE '%" + generalUtils.sanitiseForSQL(person) + "%'";
              
    if(company.length() > 0)
      where += " AND Company LIKE '%" + generalUtils.sanitiseForSQL(company) + "%'";
              
    if(companyCode.length() > 0)
      where += " AND CompanyCode = '" + companyCode + "' AND CompanyType = '" + companyType + "'";
              
    if(signOn.equals("U"))
      where += " AND SignOn = '" + generalUtils.sanitiseForSQL(unm) + "'";
              
    if(docType.length() > 0)
      where += " AND DocumentType = '" + docType + "'";
              
    if(docCode.length() > 0)
      where += " AND DocumentCode LIKE '%" + generalUtils.sanitiseForSQL(docCode) + "%'";
              
    if(subject.length() > 0)
      where += " AND Subject LIKE '%" + generalUtils.sanitiseForSQL(subject) + "%'";
              
    ResultSet rs = stmt.executeQuery("SELECT FaxCode, DateTime, Number, Person, Company, SignOn, DocumentType, DocumentCode, Subject, Status, CompanyCode FROM faxed WHERE " + where + " ORDER BY DateTime DESC");

    String faxCode, dateTime, status, cssFormat="";
    boolean first = true;
    
    while(rs.next())
    {    
      try
      {
      faxCode  = rs.getString(1);
      dateTime = rs.getString(2);
      number   = rs.getString(3);
      person   = rs.getString(4);
      company  = rs.getString(5);
      signOn   = rs.getString(6);
      docType  = rs.getString(7);
      docCode  = rs.getString(8);
      subject  = rs.getString(9);
      status   = rs.getString(10);
      companyCode = rs.getString(11);
      
      if(company     == null) company = "";
      if(companyCode == null) companyCode = "";
     
      dateTime = generalUtils.convertFromTimestamp(dateTime);
      int len = dateTime.length() - 3;
      dateTime = dateTime.substring(0, len);
      
      String docName = "";
      
      if(docType.equals("Q")) docName = "Quotation"; else
      if(docType.equals("O")) docName = "Order Confirmation"; else
      if(docType.equals("P")) docName = "Picking List"; else
      if(docType.equals("R")) docName = "Proforma Invoice"; else
      if(docType.equals("Y")) docName = "Purchase Order"; else
      if(docType.equals("I")) docName = "Invoice"; else
      if(docType.equals("E")) docName = "Receipt"; else
      if(docType.equals("C")) docName = "Credit Note"; else
      if(docType.equals("A")) docName = "Payment Advice";

      if(first)
      {
        scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
        scoutln(out, bytesOut, "<tr id=\"pageColumn\"><td></td><td><p>Fax Code</td>");
        scoutln(out, bytesOut, "<td><p>Sender</td>");
        scoutln(out, bytesOut, "<td><p>Code</td>");
        scoutln(out, bytesOut, "<td><p>To</td>");
        scoutln(out, bytesOut, "<td><p>Person</td>");
        scoutln(out, bytesOut, "<td><p>Fax #</td>");
        scoutln(out, bytesOut, "<td><p>Date</td>");
        scoutln(out, bytesOut, "<td><p>Type</td>");
        scoutln(out, bytesOut, "<td><p>Code</td>");
        scoutln(out, bytesOut, "<td><p>Subject</td>");
        scoutln(out, bytesOut, "<td><p>Status</td>");
        scoutln(out, bytesOut, "<td><p>Action</td></tr>");
        first = false;
      }

      if(cssFormat.equals("line1")) cssFormat = "line2"; else cssFormat = "line1";
      
      scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\"><td></td><td><p>" + faxCode + "</td>");
      scoutln(out, bytesOut, "<td nowrap><p>" + signOn + "</td>");
      scoutln(out, bytesOut, "<td nowrap><p>" + companyCode + "</td>");
      scoutln(out, bytesOut, "<td nowrap><p>" + company + "</td>");
      scoutln(out, bytesOut, "<td nowrap><p>" + person + "</td>");
      scoutln(out, bytesOut, "<td nowrap><p>" + number + "</td>");
      scoutln(out, bytesOut, "<td nowrap><p>" + dateTime + "</td>");
      scoutln(out, bytesOut, "<td nowrap><p>" + docName + "</td>");
      scoutln(out, bytesOut, "<td nowrap><p><a href=\"javascript:view('" + docType + "','" + docCode + "')\">" + docCode + "</td>");
      scoutln(out, bytesOut, "<td nowrap><p>" + subject + "</td>");
      scoutln(out, bytesOut, "<td nowrap><p>" + status + "</td>");
      scoutln(out, bytesOut, "<td><p><a href=\"javascript:resend('" + faxCode + "')\">ReSend</a></td></tr>");
      }
      catch(Exception e) { }
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    if(con  != null) con.close();
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

}

