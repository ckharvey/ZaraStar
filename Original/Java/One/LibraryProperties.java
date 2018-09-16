// =======================================================================================================================================================================================================
// System: ZaraStar Library: show properties
// Module: LibraryProperties.java
// Author: C.K.Harvey
// Copyright (c) 2002-07 Christopher Harvey. All Rights Reserved.
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

public class LibraryProperties extends HttpServlet
{ 
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  LibraryUtils libraryUtils = new LibraryUtils();

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
      directoryUtils.setContentHeaders(res);
      out = res.getWriter();

      unm = req.getParameter("unm");
      sid = req.getParameter("sid");
      uty = req.getParameter("uty");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");
      p1  = req.getParameter("p1"); // rootDir
      p2  = req.getParameter("p2"); // option

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "LibraryProperties", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 12015, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm,
                    String bnm, String p1, String p2, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String libraryDir       = directoryUtils.getUserDir('L', dnm, unm);
    String referenceDir     = directoryUtils.getReferenceLibraryDir(dnm); 
  
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;
    
    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "LibraryProperties", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 12015, bytesOut[0], 0, "SID:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    String lib;
    if(p2.equals("R"))
      lib = referenceDir;
    else lib = libraryDir;
    
    set(con, stmt, rs, out, req, p1, p2, unm, sid, uty, men, den, dnm, bnm, imagesDir, lib, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 12015, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
      if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String p1, String p2, String unm, String sid, String uty, String men,
                   String den, String dnm, String bnm, String imagesDir, String libraryDir, String localDefnsDir, String defnsDir,
                   int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Library: Show Properties</title>");

    String cssDirectory = directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir);
            
    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + cssDirectory + "general.css\">");

    int[] hmenuCount = new int[1];
    libraryUtils.outputPageFrame(con, stmt, rs, out, req, "LibraryProperties", "12015", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount, bytesOut);

    libraryUtils.drawTitle(con, stmt, rs, req, out, "Directory Properties", "12015", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<form action=\"LibraryPropertiesa\" enctype=\"application/x-www-form-urlencoded\" method=POST>");

    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"unm\" value='" + unm + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"uty\" value='" + uty + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"sid\" value='" + sid + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"men\" value='" + men + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"den\" value='" + den + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"dnm\" value='" + dnm + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"bnm\" value='" + bnm + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"p1\" value='" + generalUtils.sanitise(p1) + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"p2\" value='" + p2 + "'>");
    
    scoutln(out, bytesOut, "<table border=0 cellpadding=5 id=\"directory\" width=100%>");

    int len = libraryDir.length();

    --len; // trailing '/'
    while(len > 0 && libraryDir.charAt(len) != '/') // just-in-case
      --len;    

    String[] companyCode   = new String[1];
    String[] companyType   = new String[1];
    String[] documentsType = new String[1];

    libraryUtils.getProperties(p1, unm, dnm, localDefnsDir, defnsDir, companyCode, companyType, documentsType);
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    
    scoutln(out, bytesOut, "<tr><td nowrap><p>Directory &nbsp;</td><td width=99%><p>" + p1.substring(len) + "</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    
    scoutln(out, bytesOut, "<tr><td nowrap><p>Company Code &nbsp;</td><td><p><input type=text name=companyCode size=20 maxsize=20 value='"
                          + companyCode[0] + "'></td></tr>");

    scout(out, bytesOut, "<tr><td nowrap><p>Company Type &nbsp;</td><td><p><input type=radio name=companyType value=C");
    if(companyType[0].equals("C"))
      scout(out, bytesOut, " checked");
    scoutln(out, bytesOut, ">&nbsp; Customer</td></tr>");

    scout(out, bytesOut, "<tr><td></td><td><p><input type=radio name=companyType value=S");
    if(companyType[0].equals("S"))
      scout(out, bytesOut, " checked");
    scoutln(out, bytesOut, ">&nbsp; Supplier</td></tr>");

    scout(out, bytesOut, "<tr><td></td><td><p><input type=radio name=companyType value=O");
    if(companyType[0].equals("O"))
      scout(out, bytesOut, " checked");
    scoutln(out, bytesOut, ">&nbsp; Organization</td></tr>");
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap><p>Document Types &nbsp;</td><td>");
    getTypes(out, documentsType[0], bytesOut);
    scoutln(out, bytesOut, "</td></tr>");
        
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    
    scoutln(out, bytesOut, "<tr><td colspan=2><p><input type=image src=\"" + imagesDir + "go.gif\"> &nbsp; &nbsp;Update Properties</td></tr>");
    
    scoutln(out, bytesOut, "</table></form>");     scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void getTypes(PrintWriter out, String currentDocumentType, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<select name=\"documentsType\">");

    setAType(out, "Any",                "Any",                 currentDocumentType, bytesOut);
    setAType(out, "Quotations",         "Quotations",          currentDocumentType, bytesOut);
    setAType(out, "SalesOrders",        "Sales Orders",        currentDocumentType, bytesOut);
    setAType(out, "PickingLists",       "Picking Lists",       currentDocumentType, bytesOut);
    setAType(out, "SalesInvoices",      "Sales Invoices",      currentDocumentType, bytesOut);
    setAType(out, "DeliveryOrders",     "Delivery Orders",     currentDocumentType, bytesOut);
    setAType(out, "ProformaInvoices",   "Proforma Invoices",   currentDocumentType, bytesOut);
    setAType(out, "OrderConfirmations", "Order Confirmations", currentDocumentType, bytesOut);
    setAType(out, "PurchaseOrders",     "Purchase Orders",     currentDocumentType, bytesOut);
    setAType(out, "LocalRequisitions",  "Local Requisitions",  currentDocumentType, bytesOut);
    setAType(out, "GoodsReceived",      "Goods Received",      currentDocumentType, bytesOut);
    setAType(out, "PurchaseInvoices",   "Purchase Invoices",   currentDocumentType, bytesOut);
    setAType(out, "Mail",               "Mail",                currentDocumentType, bytesOut);
    setAType(out, "Documents",          "Documents",           currentDocumentType, bytesOut);
    setAType(out, "Faxes",              "Faxes",               currentDocumentType, bytesOut);

    scoutln(out, bytesOut, "</select>");
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void setAType(PrintWriter out, String documentsType, String documentsTypeText, String currentDocumentType, int[] bytesOut) throws Exception
  {
    scout(out, bytesOut, "<option value='" + documentsType + "'");
    if(documentsType.equals(currentDocumentType))
      scout(out, bytesOut, " selected");
    scoutln(out, bytesOut, ">" + documentsTypeText);
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
