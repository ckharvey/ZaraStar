// =======================================================================================================================================================================================================
// System: ZaraStar: Supplier: Change occurrences of supp code: Do it
// Module: SupplierChangeOccurrencesExecution.java
// Author: C.K.Harvey
// Copyright (c) 2002-07 Christopher Harvey. All Rights Reserved.
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

public class SupplierChangeOccurrencesExecution extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  Supplier supplier = new Supplier();

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
      p1  = req.getParameter("p1");
      p2  = req.getParameter("p2");
      
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
 
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "SupplierChangeOccurrencesExecution", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 5068, bytesOut[0], 0, "ERR:" + p1+":"+p2);
      if(out != null) out.flush();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm,
                    String bnm, String oldCode, String newCode, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;
    
    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 5068, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "SupplierChangeOccurrencesExecution", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 5068, bytesOut[0], 0, "ACC:" + oldCode+":"+newCode);
      if(con  != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "SupplierChangeOccurrencesExecution", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 5068, bytesOut[0], 0, "SID:" + oldCode+":"+newCode);
      if(con  != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(oldCode == null || oldCode.length() == 0 || newCode == null || newCode.length() == 0)
    {
      messagePage.msgScreen(false, out, req, 0, unm, sid, uty, men, den, dnm, bnm, "5068", "", "", "Please supply both an existing and a new code.", imagesDir, localDefnsDir, defnsDir, bytesOut);
    }
    else
    {
      process(con, stmt, "po",       oldCode, newCode);
      process(con, stmt, "lp",       oldCode, newCode);
      process(con, stmt, "gr",       oldCode, newCode);
      process(con, stmt, "pinvoice", oldCode, newCode);
      process(con, stmt, "payment",  oldCode, newCode);
      process(con, stmt, "voucher",  oldCode, newCode);
      process(con, stmt, "pcredit",  oldCode, newCode);
      process(con, stmt, "pdebit",   oldCode, newCode);

      if(supplier.supplierDeleteRec(con, stmt, rs, oldCode, dnm, localDefnsDir, defnsDir))
        System.out.println("Deleting Supplier record: " + oldCode);
      else System.out.println("Error deleting Supplier record: " + oldCode);

      messagePage.msgScreen(false, out, req, 21, unm, sid, uty, men, den, dnm, bnm, "5068", imagesDir, localDefnsDir, defnsDir, bytesOut);
    }

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 5068, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), oldCode+":"+newCode);
    if(con  != null) con.close();
    if(out != null) out.flush();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void process(Connection con, Statement stmt, String table, String oldCode, String newCode) throws Exception
  {
    stmt = con.createStatement();
    
    stmt.executeUpdate("UPDATE " + table + " SET CompanyCode = '" + newCode + "' WHERE CompanyCode = '" + oldCode + "'");

    if(stmt != null) stmt.close();   
  }

}
