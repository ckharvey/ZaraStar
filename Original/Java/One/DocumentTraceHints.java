// =======================================================================================================================================================================================================
// System: ZaraStar Documents: fetch hint details
// Module: DocumentTraceHints.java
// Author: C.K.Harvey
// Copyright (c) 2006-07 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.sql.*;
import java.util.Enumeration;

public class DocumentTraceHints extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", dnm="", docType="", docCode="";

    try
    {
      directoryUtils.setContentHeaders(res);
      res.setHeader("Header", "HTTP/1.0 200 OK");
      res.setStatus(200);

      Enumeration en = req.getParameterNames();
      while(en.hasMoreElements())
      {
        String elementName = (String)en.nextElement();
        String[] value = req.getParameterValues(elementName);
        if(elementName.equals("unm"))
          unm = value[0];
        else
        if(elementName.equals("sid"))
          sid = value[0];
        else
        if(elementName.equals("uty"))
          uty = value[0];
        else
        if(elementName.equals("dnm"))
          dnm = value[0];
        else
        if(elementName.equals("p1"))
          docType = value[0];
        else
        if(elementName.equals("p2"))
          docCode = value[0];
      }

      doIt(req, res, unm, sid, uty, dnm, docType, docCode, bytesOut);
    }    
    catch(Exception e)
    {
      System.out.println(("Unexpected System Error: 1025b: " + e));
      res.getWriter().write("Unexpected System Error: 1025b");
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(HttpServletRequest req, HttpServletResponse res, String unm, String sid, String uty, String dnm, String docType, 
                    String docCode, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;
    
    String hint="";
    
    String rtn="Unexpected System Error: 1025b";
    
    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      rtn = "Access Denied - Duplicate SignOn or Session Timeout";
    }
    else
    {
      hint = fetch(con, stmt, rs, dnm, docType, docCode, localDefnsDir, defnsDir);
    }
    
    res.setContentType("text/xml");
    res.setHeader("Cache-Control", "no-cache");

    if(hint.length() == 0)
      hint = ".";
    else hint = generalUtils.sanitiseForXML(hint);
    
    String s = "<msg><res>.</res><hint>" + hint + "</hint></msg>";
    res.getWriter().write(s);
    
    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 1025, bytesOut[0], 0, (new java.util.Date().getTime() - startTime),  docType + " " + docCode);
    if(con  != null) con.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private String fetch(Connection con, Statement stmt, ResultSet rs, String dnm, String docType, String code, String localDefnsDir, String defnsDir) throws Exception
  {   
    if(docType.equals("quote"))    return getDetails(con, stmt, rs, dnm, code, docType, "QuoteCode",    "QuoteDate", "Currency", "TotalTotal", localDefnsDir, defnsDir); else
    if(docType.equals("so"))       return getDetails(con, stmt, rs, dnm, code, docType, "SOCode",       "Date",      "Currency2", "TotalTotal", localDefnsDir, defnsDir); else
    if(docType.equals("oa"))       return getDetails(con, stmt, rs, dnm, code, docType, "OACode",       "Date",      "Currency", "TotalTotal", localDefnsDir, defnsDir); else
    if(docType.equals("oc"))       return getDetails(con, stmt, rs, dnm, code, docType, "OCCode",       "Date",      "Currency", "TotalTotal", localDefnsDir, defnsDir); else
    if(docType.equals("pl"))       return getDetails(con, stmt, rs, dnm, code, docType, "PLCode",       "Date",      "Currency", "TotalTotal", localDefnsDir, defnsDir); else
    if(docType.equals("do"))       return getDetails(con, stmt, rs, dnm, code, docType, "DOCode",       "Date",      "Currency", "TotalTotal", localDefnsDir, defnsDir); else
    if(docType.equals("invoice"))  return getDetails(con, stmt, rs, dnm, code, docType, "InvoiceCode",  "Date",      "Currency", "TotalTotal", localDefnsDir, defnsDir); else
    if(docType.equals("proforma")) return getDetails(con, stmt, rs, dnm, code, docType, "ProformaCode", "Date",      "Currency", "TotalTotal", localDefnsDir, defnsDir); else
    if(docType.equals("credit"))   return getDetails(con, stmt, rs, dnm, code, docType, "CNCode",       "Date",      "Currency", "TotalTotal", localDefnsDir, defnsDir); else
    if(docType.equals("po"))       return getDetails(con, stmt, rs, dnm, code, docType, "POCode",       "Date",      "Currency", "TotalTotal", localDefnsDir, defnsDir); else
    if(docType.equals("lp"))       return getDetails(con, stmt, rs, dnm, code, docType, "LPCode",       "Date",      "Currency", "TotalTotal", localDefnsDir, defnsDir); else
    if(docType.equals("debit"))    return getDetails(con, stmt, rs, dnm, code, docType, "DNCode",       "Date",      "Currency", "TotalTotal", localDefnsDir, defnsDir); else
    if(docType.equals("gr"))       return getDetails2(con, stmt, rs, dnm, code, docType, "GRCode",       "Date",      localDefnsDir, defnsDir); else
    if(docType.equals("pinvoice")) return getDetails(con, stmt, rs, dnm, code, docType, "InvoiceCode",  "Date",      "Currency", "TotalTotal", localDefnsDir, defnsDir);
    
    return "Not Found";
  }      

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private String getDetails(Connection con, Statement stmt, ResultSet rs, String dnm, String docCode, String tableName, String docCodeName, String docDateName, String docCurrencyName,
                            String docTotalAmountName, String localDefnsDir, String defnsDir) throws Exception 
  {    
    try
    {
      stmt = con.createStatement();
      rs = stmt.executeQuery("SELECT " + docDateName + "," + docCurrencyName + "," + docTotalAmountName + ", CompanyCode, CompanyName FROM "
                           + tableName + " WHERE " + docCodeName + " = '" + docCode + "'");
    
      String date="", currency="", companyCode="", companyName="", totalAmount="";
     
      if(rs.next()) // just-in-case
      {
        date        = generalUtils.convertFromYYYYMMDD(rs.getString(1));
        currency    = rs.getString(2);
        totalAmount = generalUtils.doubleDPs(rs.getString(3), '2');
        companyCode = rs.getString(4);
        companyName = rs.getString(5);
      }

      if(rs   != null) rs.close();        
      if(stmt != null) stmt.close();        

      return docCode + "`" + date + "`" + companyCode + "`" + companyName + "`" + currency + " " + totalAmount;
    }
    catch(Exception e)
    {
      System.out.println("1025b: " + e);
      if(rs   != null) rs.close();        
      if(stmt != null) stmt.close();
    }
    
    return "Not Found";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private String getDetails2(Connection con, Statement stmt, ResultSet rs, String dnm, String docCode, String tableName, String docCodeName, String docDateName, String localDefnsDir,
                             String defnsDir) throws Exception 
  {    
    try
    {
      stmt = con.createStatement();
      rs = stmt.executeQuery("SELECT " + docDateName + ", CompanyCode, CompanyName FROM " + tableName + " WHERE " + docCodeName + " = '" + docCode
                           + "'");
    
      String date="", companyCode="", companyName="";
     
      if(rs.next()) // just-in-case
      {
        date        = generalUtils.convertFromYYYYMMDD(rs.getString(1));
        companyCode = rs.getString(2);
        companyName = rs.getString(3);
      }

      if(rs   != null) rs.close();        
      if(stmt != null) stmt.close();        

      return docCode + "`" + date + "`" + companyCode + "`" + companyName + "`";
    }
    catch(Exception e)
    {
      System.out.println("1025b: " + e);
      if(rs   != null) rs.close();        
      if(stmt != null) stmt.close();
    }
    
    return "Not Found";
  }
  
}
