// =======================================================================================================================================================================================================
// System: ZaraStar Fax: Create fax for document
// Module: FaxCreate.java
// Author: C.K.Harvey
// Copyright (c) 2002-08 Christopher Harvey. All Rights Reserved.
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

public class FaxCreate extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  DefinitionTables definitionTables = new DefinitionTables();
  FaxUtils faxUtils = new FaxUtils();
  
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="", p3="", p4="", p5="";

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
      p1  = req.getParameter("p1"); // docCode
      p2  = req.getParameter("p2"); // docType
      p3  = req.getParameter("p3"); // servlet
      p4  = req.getParameter("p4"); // option
      p5  = req.getParameter("p5"); // docName

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, p3, p4, p5, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "FaxCreate", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 11002, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                   String p1, String p2, String p3, String p4, String p5, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    Connection con = null;
    Statement stmt = null;
    ResultSet rs   = null;

    try
    {
      String uName = directoryUtils.getMySQLUserName();
      String pWord = directoryUtils.getMySQLPassWord();
      Class.forName("com.mysql.jdbc.Driver").newInstance();
      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + uName + "&password=" + pWord);

      if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 11000, unm, uty, dnm, localDefnsDir, defnsDir))
      {
        messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "FaxCreate", imagesDir, localDefnsDir, defnsDir, bytesOut);
        serverUtils.etotalBytes(req, unm, dnm, 11002, bytesOut[0], 0, "ACC:" + p1);
      
      if(con != null) con.close();
        if(out != null) out.flush(); 
        return;
      }

      if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
      {
        messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "FaxCreate", imagesDir, localDefnsDir, defnsDir, bytesOut);
        serverUtils.etotalBytes(req, unm, dnm, 11002, bytesOut[0], 0, "SID:" + p1);
      
      if(con != null) con.close();
        if(out != null) out.flush(); 
        return;
      }
  
      set(con, stmt, rs, out, req, p1, p2, p3, p4, p5, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);
    }
    catch(Exception e) { } 

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 11002, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p2);
      
      if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String docCode, String docType,
                   String servlet, String option, String docName, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                   String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Send Fax</title>");

    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\""
                         + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">"); 

    scoutln(out, bytesOut, "function send(){document.forms[0].submit()}");

     scoutln(out, bytesOut, "</script>");

    int[] hmenuCount = new int[1];

    faxUtils.outputPageFrame(con, stmt, rs, out, req, "", "FaxCreate", "11002", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount, bytesOut);

    faxUtils.drawTitle(con, stmt, rs, req, out, "Send Fax", "11002", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<form action=\"FaxSend\" enctype=\"application/x-www-form-urlencoded\" method=post>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"unm\" value='" + unm + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"sid\" value='" + sid + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"uty\" value='" + uty + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"men\" value='" + men + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"den\" value='" + den + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"dnm\" value='" + dnm + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"bnm\" value='" + bnm + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"p1\"  value='" + docCode + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"p2\"  value='" + docType + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"p3\"  value='" + servlet + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"p4\"  value='" + option + "'>");

    scoutln(out, bytesOut, "<table id=\"page\" cellspacing=2 cellpadding=0 border=0 width=100%>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    get(con, stmt, rs, out, docCode, docType, docName, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    scoutln(out, bytesOut, "</table><tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "</table></form>");     scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void get(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String docCode, String docType, String docName, String unm,
                   String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut)
                   throws Exception
  {
    scoutln(out, bytesOut, "<tr><td><p><b>Fax " + docName + ": " + docCode + "</td></tr>");

    // get faxNumber, companyName from customer/supplier using companyCode on the document to be faxed
    // subject = docType + docCode
    // senderName from user file
    // senderCompany, senderPhone, senderFax from appconfig

    String subject = "", number = "", companyName = "", companyCode = "", companyType = "";
    if(docType.equals("E"))
    {
      Enquiry enquiry = new Enquiry();
      companyCode = enquiry.getAnEnquiryFieldGivenCode(con, stmt, rs, "CompanyCode", docCode);
      Supplier supplier = new Supplier();
      number = supplier.getASupplierFieldGivenCode(con, stmt, rs, "Fax", companyCode);
      companyName = supplier.getASupplierFieldGivenCode(con, stmt, rs, "Name", companyCode);
      companyType = "S";
 
      subject = "Enquiry: " + docCode;
    }
    else
    if(docType.equals("Q"))
    {
      Quotation quotation = new Quotation();
      companyCode = quotation.getAQuoteFieldGivenCode(con, stmt, rs, "CompanyCode", docCode);
      Customer customer = new Customer();
      number = customer.getACompanyFieldGivenCode(con, stmt, rs, "Fax", companyCode);
      companyName = customer.getACompanyFieldGivenCode(con, stmt, rs, "Name", companyCode);
      companyType = "C";
 
      subject = "Quotation: " + docCode;
    }
    else
    if(docType.equals("O"))
    {
      OrderConfirmation orderConfirmation = new OrderConfirmation();
      companyCode = orderConfirmation.getAnOCFieldGivenCode(con, stmt, rs, "CompanyCode", docCode);
      Customer customer = new Customer();
      number = customer.getACompanyFieldGivenCode(con, stmt, rs, "Fax", companyCode);
      companyName = customer.getACompanyFieldGivenCode(con, stmt, rs, "Name", companyCode);
      companyType = "C";
 
      subject = "Order Confirmation: " + docCode;
    }
    else
    if(docType.equals("B"))
    {
      OrderAcknowledgement orderAcknowledgement = new OrderAcknowledgement();
      companyCode = orderAcknowledgement.getAnOAFieldGivenCode(con, stmt, rs, "CompanyCode", docCode);
      Customer customer = new Customer();
      number = customer.getACompanyFieldGivenCode(con, stmt, rs, "Fax", companyCode);
      companyName = customer.getACompanyFieldGivenCode(con, stmt, rs, "Name", companyCode);
      companyType = "C";
 
      subject = "Order Acknowledgement: " + docCode;
    }
    else
    if(docType.equals("P"))
    {
      PickingList pickingList = new PickingList();
      companyCode = pickingList.getAPLFieldGivenCode(con, stmt, rs, "CompanyCode", docCode);
      Customer customer = new Customer();
      number = customer.getACompanyFieldGivenCode(con, stmt, rs, "Fax", companyCode);
      companyName = customer.getACompanyFieldGivenCode(con, stmt, rs, "Name", companyCode);
      companyType = "C";
 
      subject = "Picking List: " + docCode;
    }
    else
    if(docType.equals("I"))
    {
      SalesInvoice salesInvoice = new SalesInvoice();
      companyCode = salesInvoice.getAnInvoiceFieldGivenCode(con, stmt, rs, "CompanyCode", docCode);
      Customer customer = new Customer();
      number = customer.getACompanyFieldGivenCode(con, stmt, rs, "Fax", companyCode);
      companyName = customer.getACompanyFieldGivenCode(con, stmt, rs, "Name", companyCode);
      companyType = "C";
 
      subject = "Invoice: " + docCode;
    }
    else
    if(docType.equals("R"))
    {
      ProformaInvoice proformaInvoice = new ProformaInvoice();
      companyCode = proformaInvoice.getAProformaFieldGivenCode(con, stmt, rs, "CompanyCode", docCode);
      Customer customer = new Customer();
      number = customer.getACompanyFieldGivenCode(con, stmt, rs, "Fax", companyCode);
      companyName = customer.getACompanyFieldGivenCode(con, stmt, rs, "Name", companyCode);
      companyType = "C";
 
      subject = "Proforma Invoice: " + docCode;
    }
    else
    if(docType.equals("A"))
    {
      Payment payment = new Payment();
      companyCode = payment.getAPaymentFieldGivenCode(con, stmt, rs, "CompanyCode", docCode, dnm, localDefnsDir, defnsDir);
      Supplier supplier = new Supplier();
      number = supplier.getASupplierFieldGivenCode(con, stmt, rs, "Fax", companyCode);
      companyName = supplier.getASupplierFieldGivenCode(con, stmt, rs, "Name", companyCode);
      companyType = "S";
 
      subject = "Payment Advice: " + docCode;
    }
    else
    if(docType.equals("E"))
    {
      Receipt receipt = new Receipt();
      companyCode = receipt.getAReceiptFieldGivenCode(con, stmt, rs, "CompanyCode", docCode, dnm, localDefnsDir, defnsDir);
      Customer customer = new Customer();
      number = customer.getACompanyFieldGivenCode(con, stmt, rs, "Fax", companyCode);
      companyName = customer.getACompanyFieldGivenCode(con, stmt, rs, "Name", companyCode);
      companyType = "C";
 
      subject = "Receipt: " + docCode;
    }
    else
    if(docType.equals("C"))
    {
      SalesCreditNote salesSalesCreditNote = new SalesCreditNote();
      companyCode = salesSalesCreditNote.getASalesCreditNoteFieldGivenCode(con, stmt, rs, "CompanyCode", docCode, dnm, localDefnsDir, defnsDir);
      Customer customer = new Customer();
      number = customer.getACompanyFieldGivenCode(con, stmt, rs, "Fax", companyCode);
      companyName = customer.getACompanyFieldGivenCode(con, stmt, rs, "Name", companyCode);
      companyType = "C";
 
      subject = "Credit Note: " + docCode;
    }
    else
    if(docType.equals("Y"))
    {
      PurchaseOrder purchaseOrder = new PurchaseOrder();
      companyCode = purchaseOrder.getAPOFieldGivenCode(con, stmt, rs, "CompanyCode", docCode);
      Supplier supplier = new Supplier();
      number = supplier.getASupplierFieldGivenCode(con, stmt, rs, "Fax", companyCode);
      companyName = supplier.getASupplierFieldGivenCode(con, stmt, rs, "Name", companyCode);
      companyType = "S";

      subject = "Purchase Order: " + docCode;
    }
    else
    if(docType.equals("Z"))
    {
      LocalPurchase localPurchase = new LocalPurchase();
      companyCode = localPurchase.getAnLPFieldGivenCode(con, stmt, rs, "CompanyCode", docCode, dnm, localDefnsDir, defnsDir);
      Supplier supplier = new Supplier();
      number = supplier.getASupplierFieldGivenCode(con, stmt, rs, "Fax", companyCode);
      companyName = supplier.getASupplierFieldGivenCode(con, stmt, rs, "Name", companyCode);
      companyType = "S";

      subject = "Local Purchase: " + docCode;
    }

    String senderName = authenticationUtils.getUserNameGivenUserCode(con, stmt, rs, unm);
    
    String[] senderCompany = new String[1];
    String[] senderPhone   = new String[1];
    String[] senderFax     = new String[1];
    
    definitionTables.getAppConfigNamePhoneAndFax(con, stmt, rs, dnm, senderCompany, senderPhone, senderFax);

    scoutln(out, bytesOut, "<tr><td><p>To Fax Number:</td><td><p><input type=text name=number size=30 maxlength=60 value=\"" + number + "\"></td></tr>");

    scoutln(out, bytesOut, "<tr><td><p>To Person:</td><td><p><input type=text name=person size=60></td></tr>");

    scoutln(out, bytesOut, "<tr><td><p>To Company:</td><td><p><input type=text name=company size=60 value=\"" + companyName + "\"></td></tr>");

    scoutln(out, bytesOut, "<tr><td><p>Company Code:</td><td><p><input type=text name=companyCode size=20 value=\"" + companyCode + "\">");
    scout(out, bytesOut, "&nbsp;&nbsp;<input type=radio name=companyType value='C'");
    if(companyType.equals("C"))
      scout(out, bytesOut, " selected");
    scoutln(out, bytesOut, ">Customer");
    scout(out, bytesOut, "&nbsp;&nbsp;<input type=radio name=companyType value='S'");
    if(companyType.equals("S"))
      scout(out, bytesOut, " selected");
    scoutln(out, bytesOut, ">Supplier</td><tr>");

    scoutln(out, bytesOut, "<tr><td><p>Subject:</td><td><p><input type=text name=subject size=60 value=\"" + subject + "\"></td></tr>");

    scoutln(out, bytesOut, "<tr><td><p>Include Cover Sheet:</td><td><p><input type=checkbox name=coverSheet></td></tr>");

    scoutln(out, bytesOut, "<tr><td valign=top nowrap><p>Comments:<br>(Max 500 characters;<br>new lines are ignored)</td>"
                         + "<td><p><textarea name=comments cols=80 rows=16></textarea></td></tr>");

    scoutln(out, bytesOut, "<tr><td><p>Your Phone Number:</td><td><p><input type=text name=senderPhone size=30 maxlength=60 value=\"" + senderPhone[0]
                         + "\"></td></tr>");
    scoutln(out, bytesOut, "<tr><td><p>Your Fax Number:</td><td><p><input type=text name=senderFax size=30 maxlength=60 value=\"" + senderFax[0]
                         + "\"></td></tr>");
    scoutln(out, bytesOut, "<tr><td><p>Your Name:</td><td><p><input type=text name=senderName size=60 value=\"" + senderName + "\"></td></tr>");
    scoutln(out, bytesOut, "<tr><td><p>Your Company:</td><td><p><input type=text name=senderCompany size=60 value=\"" + senderCompany[0]
                         + "\"></td></tr>");
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

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
