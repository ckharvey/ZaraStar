// =======================================================================================================================================================================================================
// System: ZaraStar Project: Documents
// Module: ProjectDocuments.java
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
import java.io.*;
import java.sql.*;

public class ProjectDocuments extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  ProjectUtils projectUtils = new ProjectUtils();

 AdminControlUtils adminControlUtils = new AdminControlUtils();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="";

    try
    {
      directoryUtils.setContentHeaders(res);
      out = res.getWriter();

      unm = req.getParameter("unm");
      uty = req.getParameter("uty");
      sid = req.getParameter("sid");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");
      p1  = req.getParameter("p1"); // projectCode
      
      doIt(out, req, p1, unm, sid, uty, men, den, dnm, bnm, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "ProjectDocuments", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6808, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String p1, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! adminControlUtils.notDisabled(con, stmt, rs, 907) && ! authenticationUtils.verifyAccess(con, stmt, rs, req, 6800, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "6808", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6808, bytesOut[0], 0, "ACC:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "6808", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6808, bytesOut[0], 0, "SID:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }
  
    create(con, stmt, rs, out, req, p1, unm, sid, uty, men, den, dnm, bnm, imagesDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 6808, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void create(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String p1, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String imagesDir, String localDefnsDir,
                      String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Project: Documents</title>");
    
    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">"); 

    if(uty.equals("R"))
    {
      scoutln(out, bytesOut, "function viewQuote(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/QuotationRegisteredUser?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+p1;}");
    }
    else
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4019, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewQuote(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/QuotationPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    if(uty.equals("R"))
    {
      scoutln(out, bytesOut, "function viewSO(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/SalesOrderRegisteredUser?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+p1;}");
    }
    else
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4031, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewSO(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/SalesOrderPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 5006, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewPO(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/PurchaseOrderPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3038, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewPL(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/GoodsReceivedPickingList?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    if(uty.equals("R"))
    {
      scoutln(out, bytesOut, "function viewOC(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/OrderConfirmationRegisteredUser?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+p1;}");
    }
    else
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4043, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewOC(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/OrderConfirmationPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    if(uty.equals("R"))
    {
      scoutln(out, bytesOut, "function viewOA(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/OrderAcknowledgementRegisteredUser?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+p1;}");
    }
    else
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4130, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewOA(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/OrderAcknowledgementPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    if(uty.equals("R"))
    {
      scoutln(out, bytesOut, "function viewDO(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/DeliveryOrdeRegisteredUser?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+p1;}");
    }
    else
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4054, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewDO(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/DeliveryOrderPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    if(uty.equals("R"))
    {
      scoutln(out, bytesOut, "function viewInvoice(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/SalesInvoiceRegisteredUser?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+p1;}");
    }
    else
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4067, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewInvoice(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/SalesInvoicePage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    if(uty.equals("R")) // TODO
    {
    }
    else
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4101, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewCN(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/SalesCreditNotePage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    if(uty.equals("R"))
    {
      scoutln(out, bytesOut, "function viewProforma(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/ProformaInvoiceRegisteredUser?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+p1;}");
    }
    else
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4080, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewProforma(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/ProformaInvoicePage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    if(uty.equals("R"))
    {
      scoutln(out, bytesOut, "function read(docCode){");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/LibraryDownloaCasual?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+docCode;}");
    }

    if(uty.equals("I"))
    {
      scoutln(out, bytesOut, "function read(docCode){");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/LibraryDownloadFile?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+docCode;}");

      scoutln(out, bytesOut, "function details(){");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/ProjectCreateEdit?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=E&p1=" + p1 + "\";}");

      scoutln(out, bytesOut, "function officetasks(){");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/ProjectOfficeTasks?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + p1 + "\";}");

      scoutln(out, bytesOut, "function workshoptasks(){");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/_6805?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + p1 + "\";}");

      scoutln(out, bytesOut, "function schedule(){");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/_6807?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + p1 + "\";}");

      scoutln(out, bytesOut, "function documents(){");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/ProjectDocuments?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + p1 + "\";}");

      scoutln(out, bytesOut, "function materials(){");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/_6809?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + p1 + "\";}");

      scoutln(out, bytesOut, "function tests(){");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/_6813?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + p1 + "\";}");

      scoutln(out, bytesOut, "function reports(){");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/_6810?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + p1 + "\";}");
    }

    scoutln(out, bytesOut, "function sanitise(code){");
    scoutln(out, bytesOut, "var code2='';var x;var len=code.length;");
    scoutln(out, bytesOut, "for(x=0;x<len;++x)");
    scoutln(out, bytesOut, "if(code.charAt(x)=='#')code2+='%23';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='\"')code2+='%22';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='&')code2+='%26';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='%')code2+='%25';");
    scoutln(out, bytesOut, "else if(code.charAt(x)==' ')code2+='%20';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='+')code2+='%2b';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='?')code2+='%3F';");
    scoutln(out, bytesOut, "else code2+=code.charAt(x);");
    scoutln(out, bytesOut, "return code2};");

    scoutln(out, bytesOut, "</script>");

    int[] hmenuCount = new int[1];
    projectUtils.outputPageFrame(con, stmt, rs, out, req, "6808", "ProjectDocuments", "", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount, bytesOut);

    String title = projectUtils.fetchProjectTitleGivenCode(p1, dnm, localDefnsDir, defnsDir);

    projectUtils.drawTitle(con, stmt, rs, req, out, "ProjectDocuments", "Documents for Project " + p1 + ": " + title, "6808", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
  
    scoutln(out, bytesOut, "<form><table id=\"page\" border=0 width=100%>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td colspan=3><span id=\"msg\"></span></td></tr>");

    String[] cssFormat = new String[1];  cssFormat[0] = "line1";

    boolean[] first = new boolean[1];  first[0] = true;

    onQuote(   con, stmt, rs, out, p1, uty, imagesDir, cssFormat, first, bytesOut);
    onSO(      con, stmt, rs, out, p1, uty, imagesDir, cssFormat, first, bytesOut);
    onOA(      con, stmt, rs, out, p1, uty, imagesDir, cssFormat, first, bytesOut);
    onOC(      con, stmt, rs, out, p1, uty, imagesDir, cssFormat, first, bytesOut);

    if(uty.equals("I"))
      onPL(    con, stmt, rs, out, p1, uty, imagesDir, cssFormat, first, bytesOut);

    onDO(      con, stmt, rs, out, p1, uty, imagesDir, cssFormat, first, bytesOut);
    onProforma(con, stmt, rs, out, p1, uty, imagesDir, cssFormat, first, bytesOut);
    onInvoice( con, stmt, rs, out, p1, uty, imagesDir, cssFormat, first, bytesOut);
    onCN(      con, stmt, rs, out, p1, uty, imagesDir, cssFormat, first, bytesOut);

    if(uty.equals("I"))
      onPO(    con, stmt, rs, out, p1, uty, imagesDir, cssFormat, first, bytesOut);
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "</table><table id=\"page\" border=0 width=100%>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    listLibraryDocuments(out, p1, uty, dnm, bytesOut);

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void onQuote(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String projectCode, String uty, String imagesDir, String[] cssFormat, boolean[] first, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT QuoteCode, QuoteDate, CompanyCode, CompanyName, EnquiryCode, DocumentStatus FROM quote WHERE ProjectCode = '" + projectCode + "' ORDER BY QuoteDate, QuoteCode");

    String quoteCode, status, date, customerCode, customerName, enquiryCode;

    while(rs.next())
    {    
      quoteCode    = rs.getString(1);
      date         = rs.getString(2);
      customerCode = rs.getString(3);
      customerName = rs.getString(4);
      enquiryCode  = rs.getString(5);
      status       = rs.getString(6);

      if(first[0])
      {
        header(out, bytesOut);
        first[0] = false;
      }

      if(cssFormat[0].equals("line1")) cssFormat[0] = "line2"; else cssFormat[0] = "line1";

      scoutln(out, bytesOut, "<tr id=\"" + cssFormat[0] + "\">");
      
      if(status.equals("C") && uty.equals("I"))
        scoutln(out, bytesOut, "<td align=center><img src=\"" + imagesDir + "z0212.gif\" border=0></td>");
      else scoutln(out, bytesOut, "<td></td>");
      
      scoutln(out, bytesOut, "<td nowrap><p>Quotation</td>");
      scoutln(out, bytesOut, "<td nowrap><p><a href=\"javascript:viewQuote('" + quoteCode + "')\">" + quoteCode + "</a></td>");
      scoutln(out, bytesOut, "<td nowrap><p>" + generalUtils.convertFromYYYYMMDD(date) + "</td>");
      scoutln(out, bytesOut, "<td nowrap><p>" + enquiryCode + "</td>");
      scoutln(out, bytesOut, "<td nowrap><p>" + customerCode + " (" + customerName +")</td></tr>");
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void onSO(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String projectCode, String uty, String imagesDir, String[] cssFormat, boolean[] first, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT SOCode, Date, CompanyCode, CompanyName, CustomerPOCode, Status FROM so WHERE ProjectCode = '" + projectCode + "' ORDER BY Date, SOCode");

    String soCode, status, date, customerCode, customerName, customerPOCode;

    while(rs.next())
    {    
      soCode         = rs.getString(1);
      date           = rs.getString(2);
      customerCode   = rs.getString(3);
      customerName   = rs.getString(4);
      customerPOCode = rs.getString(5);
      status         = rs.getString(6);

      if(first[0])
      {
        header(out, bytesOut);
        first[0] = false;
      }

      if(cssFormat[0].equals("line1")) cssFormat[0] = "line2"; else cssFormat[0] = "line1";

      scoutln(out, bytesOut, "<tr id=\"" + cssFormat[0] + "\">");
      
      if(status.equals("C") && uty.equals("I"))
        scoutln(out, bytesOut, "<td align=center><img src=\"" + imagesDir + "z0212.gif\" border=0></td>");
      else scoutln(out, bytesOut, "<td></td>");
      
      scoutln(out, bytesOut, "<td nowrap><p>Sales Order</td>");
      scoutln(out, bytesOut, "<td nowrap><p><a href=\"javascript:viewSO('" + soCode + "')\">" + soCode + "</a></td>");
      scoutln(out, bytesOut, "<td nowrap><p>" + generalUtils.convertFromYYYYMMDD(date) + "</td>");
      scoutln(out, bytesOut, "<td nowrap><p>" + customerPOCode + "</td>");
      scoutln(out, bytesOut, "<td nowrap><p>" + customerCode + " (" + customerName +")</td></tr>");
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void onOC(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String projectCode, String uty, String imagesDir, String[] cssFormat, boolean[] first, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT OCCode, Date, CompanyCode, CompanyName, SOCode, Status FROM oc WHERE ProjectCode = '" + projectCode + "' ORDER BY Date, OCCode");

    String ocCode, status, date, customerCode, customerName, soCode;

    while(rs.next())
    {
      ocCode       = rs.getString(1);
      date         = rs.getString(2);
      customerCode = rs.getString(3);
      customerName = rs.getString(4);
      soCode       = rs.getString(5);
      status       = rs.getString(6);

      if(first[0])
      {
        header(out, bytesOut);
        first[0] = false;
      }

      if(cssFormat[0].equals("line1")) cssFormat[0] = "line2"; else cssFormat[0] = "line1";

      scoutln(out, bytesOut, "<tr id=\"" + cssFormat[0] + "\">");

      if(status.equals("C") && uty.equals("I"))
        scoutln(out, bytesOut, "<td align=center><img src=\"" + imagesDir + "z0212.gif\" border=0></td>");
      else scoutln(out, bytesOut, "<td></td>");

      scoutln(out, bytesOut, "<td nowrap><p>Order Confirmation</td>");
      scoutln(out, bytesOut, "<td nowrap><p><a href=\"javascript:viewOC('" + ocCode + "')\">" + ocCode + "</a></td>");
      scoutln(out, bytesOut, "<td nowrap><p>" + generalUtils.convertFromYYYYMMDD(date) + "</td>");
      scoutln(out, bytesOut, "<td nowrap><p>" + soCode + "</td>");
      scoutln(out, bytesOut, "<td nowrap><p>" + customerCode + " (" + customerName +")</td></tr>");
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void onOA(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String projectCode, String uty, String imagesDir, String[] cssFormat, boolean[] first, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT OACode, Date, CompanyCode, CompanyName, SOCode, Status FROM oa WHERE ProjectCode = '" + projectCode + "' ORDER BY Date, OACode");

    String oaCode, status, date, customerCode, customerName, soCode;

    while(rs.next())
    {
      oaCode       = rs.getString(1);
      date         = rs.getString(2);
      customerCode = rs.getString(3);
      customerName = rs.getString(4);
      soCode       = rs.getString(5);
      status       = rs.getString(6);

      if(first[0])
      {
        header(out, bytesOut);
        first[0] = false;
      }

      if(cssFormat[0].equals("line1")) cssFormat[0] = "line2"; else cssFormat[0] = "line1";

      scoutln(out, bytesOut, "<tr id=\"" + cssFormat[0] + "\">");

      if(status.equals("C") && uty.equals("I"))
        scoutln(out, bytesOut, "<td align=center><img src=\"" + imagesDir + "z0212.gif\" border=0></td>");
      else scoutln(out, bytesOut, "<td></td>");

      scoutln(out, bytesOut, "<td nowrap><p>Order Acknowledgement</td>");
      scoutln(out, bytesOut, "<td nowrap><p><a href=\"javascript:viewOA('" + oaCode + "')\">" + oaCode + "</a></td>");
      scoutln(out, bytesOut, "<td nowrap><p>" + generalUtils.convertFromYYYYMMDD(date) + "</td>");
      scoutln(out, bytesOut, "<td nowrap><p>" + soCode + "</td>");
      scoutln(out, bytesOut, "<td nowrap><p>" + customerCode + " (" + customerName +")</td></tr>");
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void onPL(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String projectCode, String uty, String imagesDir, String[] cssFormat, boolean[] first, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT PLCode, Date, CompanyCode, CompanyName, Status FROM pl WHERE ProjectCode = '" + projectCode + "' ORDER BY Date, PLCode");

    String plCode, status, date, customerCode, customerName;

    while(rs.next())
    {    
      plCode       = rs.getString(1);
      date         = rs.getString(2);
      customerCode = rs.getString(3);
      customerName = rs.getString(4);
      status       = rs.getString(5);

      if(first[0])
      {
        header(out, bytesOut);
        first[0] = false;
      }

      if(cssFormat[0].equals("line1")) cssFormat[0] = "line2"; else cssFormat[0] = "line1";

      scoutln(out, bytesOut, "<tr id=\"" + cssFormat[0] + "\">");
      
      if(status.equals("C") && uty.equals("I"))
        scoutln(out, bytesOut, "<td align=center><img src=\"" + imagesDir + "z0212.gif\" border=0></td>");
      else scoutln(out, bytesOut, "<td></td>");
      
      scoutln(out, bytesOut, "<td nowrap><p>Picking List</td>");
      scoutln(out, bytesOut, "<td nowrap><p><a href=\"javascript:viewPL('" + plCode + "')\">" + plCode + "</a></td>");
      scoutln(out, bytesOut, "<td nowrap><p>" + generalUtils.convertFromYYYYMMDD(date) + "</td>");
      scoutln(out, bytesOut, "<td></td>");
      scoutln(out, bytesOut, "<td nowrap><p>" + customerCode + " (" + customerName +")</td></tr>");
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void onDO(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String projectCode, String uty, String imagesDir, String[] cssFormat, boolean[] first, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT DOCode, Date, CompanyCode, CompanyName, PLCode, Status FROM do WHERE ProjectCode = '" + projectCode + "' ORDER BY Date, DOCode");

    String doCode, status, date, customerCode, customerName, plCode;

    while(rs.next())
    {    
      doCode       = rs.getString(1);
      date         = rs.getString(2);
      customerCode = rs.getString(3);
      customerName = rs.getString(4);
      plCode       = rs.getString(5);
      status       = rs.getString(6);

      if(first[0])
      {
        header(out, bytesOut);
        first[0] = false;
      }

      if(cssFormat[0].equals("line1")) cssFormat[0] = "line2"; else cssFormat[0] = "line1";

      scoutln(out, bytesOut, "<tr id=\"" + cssFormat[0] + "\">");
      
      if(status.equals("C") && uty.equals("I"))
        scoutln(out, bytesOut, "<td align=center><img src=\"" + imagesDir + "z0212.gif\" border=0></td>");
      else scoutln(out, bytesOut, "<td></td>");
      
      scoutln(out, bytesOut, "<td nowrap><p>Delivery Order</td>");
      scoutln(out, bytesOut, "<td nowrap><p><a href=\"javascript:viewDO('" + doCode + "')\">" + doCode + "</a></td>");
      scoutln(out, bytesOut, "<td nowrap><p>" + generalUtils.convertFromYYYYMMDD(date) + "</td>");
      scoutln(out, bytesOut, "<td nowrap><p>" + plCode + "</td>");
      scoutln(out, bytesOut, "<td nowrap><p>" + customerCode + " (" + customerName +")</td></tr>");
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void onInvoice(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String projectCode, String uty, String imagesDir, String[] cssFormat, boolean[] first, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT InvoiceCode, Date, CompanyCode, CompanyName, DORefNum, Status FROM invoice WHERE ProjectCode = '"
                           + projectCode + "' ORDER BY Date, InvoiceCode");

    String invoiceCode, status, date, customerCode, customerName, doCode;

    while(rs.next())
    {    
      invoiceCode  = rs.getString(1);
      date         = rs.getString(2);
      customerCode = rs.getString(3);
      customerName = rs.getString(4);
      doCode       = rs.getString(5);
      status       = rs.getString(6);

      if(first[0])
      {
        header(out, bytesOut);
        first[0] = false;
      }

      if(cssFormat[0].equals("line1")) cssFormat[0] = "line2"; else cssFormat[0] = "line1";

      scoutln(out, bytesOut, "<tr id=\"" + cssFormat[0] + "\">");
      
      if(status.equals("C") && uty.equals("I"))
        scoutln(out, bytesOut, "<td align=center><img src=\"" + imagesDir + "z0212.gif\" border=0></td>");
      else scoutln(out, bytesOut, "<td></td>");
      
      scoutln(out, bytesOut, "<td nowrap><p>Invoice</td>");
      scoutln(out, bytesOut, "<td nowrap><p><a href=\"javascript:viewInvoice('" + invoiceCode + "')\">" + invoiceCode + "</a></td>");
      scoutln(out, bytesOut, "<td nowrap><p>" + generalUtils.convertFromYYYYMMDD(date) + "</td>");
      scoutln(out, bytesOut, "<td nowrap><p>" + doCode + "</td>");
      scoutln(out, bytesOut, "<td nowrap><p>" + customerCode + " (" + customerName +")</td></tr>");
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void onProforma(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String projectCode, String uty, String imagesDir, String[] cssFormat, boolean[] first, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT ProformaCode, Date, CompanyCode, CompanyName, PORefNum, Status FROM proforma WHERE ProjectCode = '" + projectCode + "' ORDER BY Date, ProformaCode");

    String proformaCode, status, date, customerCode, customerName, poRefNum;

    while(rs.next())
    {    
      proformaCode = rs.getString(1);
      date         = rs.getString(2);
      customerCode = rs.getString(3);
      customerName = rs.getString(4);
      poRefNum     = rs.getString(5);
      status       = rs.getString(6);

      if(first[0])
      {
        header(out, bytesOut);
        first[0] = false;
      }

      if(cssFormat[0].equals("line1")) cssFormat[0] = "line2"; else cssFormat[0] = "line1";

      scoutln(out, bytesOut, "<tr id=\"" + cssFormat[0] + "\">");
      
      if(status.equals("C") && uty.equals("I"))
        scoutln(out, bytesOut, "<td align=center><img src=\"" + imagesDir + "z0212.gif\" border=0></td>");
      else scoutln(out, bytesOut, "<td></td>");
      
      scoutln(out, bytesOut, "<td nowrap><p>Proforma Invoice</td>");
      scoutln(out, bytesOut, "<td nowrap><p><a href=\"javascript:viewProforma('" + proformaCode + "')\">" + proformaCode + "</a></td>");
      scoutln(out, bytesOut, "<td nowrap><p>" + generalUtils.convertFromYYYYMMDD(date) + "</td>");
      scoutln(out, bytesOut, "<td nowrap><p>" + poRefNum + "</td>");
      scoutln(out, bytesOut, "<td nowrap><p>" + customerCode + " (" + customerName +")</td></tr>");
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void onCN(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String projectCode, String uty, String imagesDir, String[] cssFormat, boolean[] first, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT CNCode, Date, CompanyCode, CompanyName, Status FROM credit WHERE ProjectCode = '" + projectCode + "' ORDER BY Date, CNCode");

    String cnCode, status, date, customerCode, customerName;

    while(rs.next())
    {    
      cnCode       = rs.getString(1);
      date         = rs.getString(2);
      customerCode = rs.getString(3);
      customerName = rs.getString(4);
      status       = rs.getString(5);

      if(first[0])
      {
        header(out, bytesOut);
        first[0] = false;
      }

      if(cssFormat[0].equals("line1")) cssFormat[0] = "line2"; else cssFormat[0] = "line1";

      scoutln(out, bytesOut, "<tr id=\"" + cssFormat[0] + "\">");
      
      if(status.equals("C") && uty.equals("I"))
        scoutln(out, bytesOut, "<td align=center><img src=\"" + imagesDir + "z0212.gif\" border=0></td>");
      else scoutln(out, bytesOut, "<td></td>");
      
      scoutln(out, bytesOut, "<td nowrap><p>Credit Note</td>");
      scoutln(out, bytesOut, "<td nowrap><p><a href=\"javascript:viewCN('" + cnCode + "')\">" + cnCode + "</a></td>");
      scoutln(out, bytesOut, "<td nowrap><p>" + generalUtils.convertFromYYYYMMDD(date) + "</td>");
      scoutln(out, bytesOut, "<td></td>");
      scoutln(out, bytesOut, "<td nowrap><p>" + customerCode + " (" + customerName +")</td></tr>");
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void onPO(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String projectCode, String uty, String imagesDir, String[] cssFormat, boolean[] first, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT POCode, Date, CompanyCode, CompanyName, Status FROM po WHERE ProjectCode = '" + projectCode + "' ORDER BY Date, POCode");

    String poCode, status, date, supplierCode, supplierName;

    while(rs.next())
    {    
      poCode         = rs.getString(1);
      date           = rs.getString(2);
      supplierCode   = rs.getString(3);
      supplierName   = rs.getString(4);
      status         = rs.getString(5);

      if(first[0])
      {
        header(out, bytesOut);
        first[0] = false;
      }

      if(cssFormat[0].equals("line1")) cssFormat[0] = "line2"; else cssFormat[0] = "line1";

      scoutln(out, bytesOut, "<tr id=\"" + cssFormat[0] + "\">");
      
      if(status.equals("C") && uty.equals("I"))
        scoutln(out, bytesOut, "<td align=center><img src=\"" + imagesDir + "z0212.gif\" border=0></td>");
      else scoutln(out, bytesOut, "<td></td>");
      
      scoutln(out, bytesOut, "<td nowrap><p>Purchase Order</td>");
      scoutln(out, bytesOut, "<td nowrap><p><a href=\"javascript:viewPO('" + poCode + "')\">" + poCode + "</a></td>");
      scoutln(out, bytesOut, "<td nowrap><p>" + generalUtils.convertFromYYYYMMDD(date) + "</td>");
      scoutln(out, bytesOut, "<td></td>");
      scoutln(out, bytesOut, "<td nowrap><p>" + supplierCode + " (" + supplierName +")</td></tr>");
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void listLibraryDocuments(PrintWriter out, String projectCode, String uty, String dnm, int[] bytesOut) throws Exception
  {
    Connection con = null;
    Statement stmt = null;
    ResultSet rs = null;
    
    try
    {
      String uName    = directoryUtils.getMySQLUserName();
      String passWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();
    
      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + uName + "&password=" + passWord);

      stmt = con.createStatement();
  
      String q = "SELECT DocCode, DocName, DocType, InOrOut, LastDateIn, InternalOrExternalOrAnonymous, FileSize FROM documents WHERE ProjectCode = '" + projectCode + "' ORDER BY DocCode";

      rs = stmt.executeQuery(q);
  
      String docCode, docName, docType, inOrOut, lastDateIn, internalOrExternalOrAnonymous, fileSize, cssFormat = "";
      boolean first = true, wanted;

      while(rs.next())                  
      {
        docCode                       = rs.getString(1);
        docName                       = rs.getString(2);
        docType                       = rs.getString(3);
        inOrOut                       = rs.getString(4);
        lastDateIn                    = rs.getString(5);
        internalOrExternalOrAnonymous = rs.getString(6);
        fileSize                      = rs.getString(7);
        
        wanted = false;
        if(internalOrExternalOrAnonymous.equals("E"))
          wanted = true;
        else
        if(uty.equals("I"))
          wanted = true;

        if(wanted)
        {
          if(first)
          {
            scoutln(out, bytesOut, "<tr id=\"pageColumn\"><td></td>");
            scoutln(out, bytesOut, "<td nowrap><p>Document &nbsp;</td>");
            scoutln(out, bytesOut, "<td nowrap align=center><p>Code &nbsp;</td>");
            scoutln(out, bytesOut, "<td nowrap align=center><p>Type &nbsp;</td>");
            scoutln(out, bytesOut, "<td nowrap align=center><p>Date &nbsp;</td>");
            scoutln(out, bytesOut, "<td nowrap align=right><p>FileSize</td></tr>");

            first = false;
          }

          if(cssFormat.equals("line1")) cssFormat = "line2"; else cssFormat= "line1";
                    
          scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\">");

          if(inOrOut.equals("O"))
            scoutln(out, bytesOut, "<td><p>(Under edit)</td><td><p>" + docName + "</td>");
          else scoutln(out, bytesOut, "<td></td><td><p><a href=\"javascript:read('" + docCode + "')\">" + docName + "</a></td>");
        
          scoutln(out, bytesOut, "<td align=center><p>" + docCode + "</td>");
          scoutln(out, bytesOut, "<td align=center><p>" + docType + "</td>");
          scoutln(out, bytesOut, "<td align=center><p>" + lastDateIn + "</td>");
          scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(fileSize, '0') + "&nbsp;</td></tr>");
        }
      }
                 
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }    
  }  

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int[] bytesOut, String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void header(PrintWriter out, int bytesOut[]) throws Exception
  {
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr id=\"pageColumn\"><td></td>");
    scoutln(out, bytesOut, "<td nowrap><p>Document &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Code &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Date &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Reference &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Customer &nbsp;</td></tr>");
  }

}
