// =======================================================================================================================================================================================================
// System: ZaraStar: Fax: resend historical
// Module: FaxResend.java
// Author: C.K.Harvey
// Copyright (c) 2000-07 Christopher Harvey. All Rights Reserved.
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

public class FaxResend extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  FaxUtils faxUtils = new FaxUtils();
  
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
      sid = req.getParameter("sid");
      uty = req.getParameter("uty");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");
      p1  = req.getParameter("p1"); // faxCode
      
      if(p1 == null) p1 = "";
      
      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, bytesOut);
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

      System.out.println("11005: " + e);
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "FaxResend", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 11005, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm,
                    String bnm, String p1, int[] bytesOut) throws Exception
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
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "FaxResend", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 11005, bytesOut[0], 0, "ACC:" + p1);
      
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "FaxResend", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 11005, bytesOut[0], 0, "SID:" + p1);
      
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    set(con, stmt, rs, out, req, p1, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 11005, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
      
      if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String faxCode, String unm, String sid, String uty, String men, String den,
                   String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>ReSend Fax</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    scoutln(out, bytesOut, "function send(){document.forms[0].submit()}");
    
    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir)
                         + "general.css\">");

    int[] hmenuCount = new int[1];

    faxUtils.outputPageFrame(con, stmt, rs, out, req, "", "FaxResend", "11005", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount, bytesOut);

    faxUtils.drawTitle(con, stmt, rs, req, out, "ReSend Fax", "11005", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
    
    scoutln(out, bytesOut, "<form action=\"FaxSend\" enctype=\"application/x-www-form-urlencoded\" method=post>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"unm\" value='" + unm + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"sid\" value='" + sid + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"uty\" value='" + uty + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"men\" value='" + men + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"den\" value='" + den + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"dnm\" value='" + dnm + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"bnm\" value='" + bnm + "'>");

    scoutln(out, bytesOut, "<table id=\"page\" width=100% border=0>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    resend(out, faxCode, dnm, localDefnsDir, defnsDir, bytesOut);

    scoutln(out, bytesOut, "</table></form>");     scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void resend(PrintWriter out, String faxCode, String dnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    String uName = directoryUtils.getMySQLUserName();
    String pWord = directoryUtils.getMySQLPassWord();
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_fax?user=" + uName + "&password=" + pWord);
    Statement stmt = null;
    ResultSet rs   = null;
              
    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT Number, Person, Company, Subject, CoverSheet, Comments, SenderPhone, SenderFax, SenderName, "
                         + "SenderCompany, DocumentCode, DocumentType, Text FROM faxed WHERE FaxCode = '" + faxCode + "'");

    String number, person, company, subject, coverSheet, comments, senderPhone, senderFax, senderName, senderCompany, docCode, docType, text;
    
    if(rs.next())
    {    
      number        = rs.getString(1);
      person        = rs.getString(2);
      company       = rs.getString(3);
      subject       = rs.getString(4);
      coverSheet    = rs.getString(5);
      comments      = rs.getString(6);
      senderPhone   = rs.getString(7);
      senderFax     = rs.getString(8);
      senderName    = rs.getString(9);
      senderCompany = rs.getString(10);
      docCode       = rs.getString(11);
      docType       = rs.getString(12);
      text          = rs.getString(13);
    }
    else
    {
      number = "";
      person = "";
      company = "";
      subject = "";
      coverSheet = "";
      comments = "";
      senderPhone = "";
      senderFax = "";
      senderName = "";
      senderCompany = "";
      docCode = "";
      docType = "";
      text = "";
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    if(con  != null) con.close();
    
    scoutln(out, bytesOut, "<tr><td><p>To Fax Number:</td><td><p><input type=text name=number size=30 maxlength=60 value=\"" + number + "\"></td></tr>");

    scoutln(out, bytesOut, "<tr><td><p>To Person:</td><td><p><input type=text name=person size=60 value=\"" + person + "\"></td></tr>");

    scoutln(out, bytesOut, "<tr><td><p>To Company:</td><td><p><input type=text name=company size=60 value=\"" + company + "\"></td></tr>");

    scoutln(out, bytesOut, "<tr><td><p>Subject:</td><td><p><input type=text name=subject size=60 value=\"" + subject + "\"></td></tr>");

    scout(out, bytesOut, "<tr><td><p>Include Cover Sheet:</td><td><p><input type=checkbox name=coverSheet");
    if(coverSheet.equals("Y"))
      scout(out, bytesOut, " checked");
    scoutln(out, bytesOut, "></td></tr>");

    if(docType.length() > 0)
    {  
      scoutln(out, bytesOut, "<tr><td valign=top nowrap><p>Comments:<br>(Max 500 characters;<br>new lines are ignored)</td>"
                           + "<td><p><textarea name=comments cols=80 rows=16>" + comments + "</textarea></td></tr>");
    }
    else scoutln(out, bytesOut, "<tr><td valign=top nowrap><p>Text:</td><td><p><textarea name=text cols=90 rows=20>" + text + "</textarea></td></tr>");

    scoutln(out, bytesOut, "<tr><td><p>Your Phone Number:</td><td><p><input type=text name=senderPhone size=30 maxlength=60 value=\"" + senderPhone
                         + "\"></td></tr>");
    scoutln(out, bytesOut, "<tr><td><p>Your Fax Number:</td><td><p><input type=text name=senderFax size=30 maxlength=60 value=\"" + senderFax
                         + "\"></td></tr>");
    scoutln(out, bytesOut, "<tr><td><p>Your Name:</td><td><p><input type=text name=senderName size=60 value=\"" + senderName + "\"></td></tr>");
    scoutln(out, bytesOut, "<tr><td><p>Your Company:</td><td><p><input type=text name=senderCompany size=60 value=\"" + senderCompany
                         + "\"></td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    String servlet = "";
    
    if(docType.equals("Q")) // quotation
      servlet = "QuotationPrint";
    else
    if(docType.equals("Y")) // purchase order
      servlet = "PurchaseOrderPrint";
    else
    if(docType.equals("P")) // picking list
      servlet = "PickingListPrint";
    else
    if(docType.equals("O")) // order confirmation
      servlet = "OrderConfirmationPrint";
    else
    if(docType.equals("R")) // proforma invoice
      servlet = "ProformaInvoicesPrint";
    else
    if(docType.equals("I")) // invoice
      servlet = "SalesInvoicePage";
    else
    if(docType.equals("E")) // receipt
      servlet = "ReceiptPage";
    else
    if(docType.equals("C")) // CN
      servlet = "SalesCreditNotePage";
    
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"p1\"  value='" + docCode + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"p2\"  value='" + docType + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"p3\"  value='" + servlet + "'>");

    scoutln(out, bytesOut, "<tr><td></td><td><p><a href=\"javascript:send()\">Send</a></td></tr>");
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
