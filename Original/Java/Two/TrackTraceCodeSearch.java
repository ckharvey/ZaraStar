// =======================================================================================================================================================================================================
// System: ZaraStar DocumentEngine: TNT document code search
// Module: TrackTraceCodeSearch.java
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
import java.net.*;

public class TrackTraceCodeSearch extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  ReportGeneration reportGeneration = new ReportGeneration();
  Profile profile = new Profile();
  AdminControlUtils adminControlUtils = new AdminControlUtils();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="", p3="";
    try
    {
      out = res.getWriter();
      directoryUtils.setContentHeaders(res);

      unm = req.getParameter("unm");
      sid = req.getParameter("sid");
      uty = req.getParameter("uty");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");
      p1  = req.getParameter("p1"); // documentCode
      p2  = req.getParameter("p2"); // docType
      p3  = req.getParameter("p3"); // supplierCode

      if(p1 == null || p1.length() == 0)
        p1 = "-";

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, p3, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "TrackTraceCodeSearch", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2012, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                    String p1, String p2, String p3, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String imagesDir     = directoryUtils.getSupportDirs('I');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String workingDir       = directoryUtils.getUserDir('W', dnm, unm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! adminControlUtils.notDisabled(con, stmt, rs, 903))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "TrackTraceMainExternal", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2012, bytesOut[0], 0, "ACC:" + p1);
      if(con  != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "TrackTraceMainExternal", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2012, bytesOut[0], 0, "SID:" + p1);
      if(con  != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    // create tmp searchresults file here on web server
    RandomAccessFile fh = generalUtils.create(workingDir + "searchresults.txt");
    if((fh = generalUtils.fileOpenD("searchresults.txt", workingDir)) == null) // just-in-case
      return;

    RandomAccessFile fhi = generalUtils.create(workingDir + "resultsoffsets.inx");
    if((fhi = generalUtils.fileOpenD("resultsoffsets.inx", workingDir)) == null) // just-in-case
      return;

    int[]    count = new int[1];    count[0] = 0;
    double[] time  = new double[1]; time[0]  = 0.0;

    int[] numPages = new int[1]; numPages[0] = 1;
    fhi.writeInt(0);

    int i = unm.indexOf("_");
    String[] reqdCustomerCode = new String[1];

    if(! profile.getExternalAccessNameCustomerCode(con, stmt, rs, unm.substring(0, i), dnm, localDefnsDir, defnsDir, reqdCustomerCode))
      reqdCustomerCode[0] = "";
    
    if(reqdCustomerCode[0].length() == 0)
    {
      // output screen saying none found.... TODO
      serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 2012, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
      if(con  != null) con.close();
      if(out != null) out.flush(); 
      return;      
    }  
    
    switch(p2.charAt(0))
    {
      case 'Q' : // Quotes
                 searchQuote(con, stmt, rs, fh, fhi, p1, reqdCustomerCode[0], count, numPages);
                 break;
      case 'S' : // Sales Orders
                 searchSO(con, stmt, rs, fh, fhi, p1, reqdCustomerCode[0], count, numPages);
                 break;
      case 'P' : // Sales Orders (CPO)
                 searchSO4CustPOCode(con, stmt, rs, fh, fhi, p1, reqdCustomerCode[0], count, numPages);
                 break;
      case 'D' : // Delivery Orders
                 searchDO(con, stmt, rs, fh, fhi, p1, reqdCustomerCode[0], count, numPages);
                 break;
      case 'I' : // Invoices
                 searchInvoice(con, stmt, rs, fh, fhi, p1, reqdCustomerCode[0], count, numPages);
                 break;
      case 'R' : // Proforma Invoices
                 searchProformaInvoice(con, stmt, rs, fh, fhi, p1, reqdCustomerCode[0], count, numPages);
                 break;
    }

    time[0] += (new java.util.Date().getTime() - startTime);

    generalUtils.fileClose(fh);
    generalUtils.fileClose(fhi);

    displayFirstPage(out, count[0], generalUtils.doubleToStr('2', (time[0] / 1000.0)), numPages[0], unm, sid, uty, men, den, dnm, bnm, localDefnsDir, p1);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 2012, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void searchSO(Connection con, Statement stmt, ResultSet rs, RandomAccessFile fh, RandomAccessFile fhi, String soCode, String reqdCustomerCode, int[] count, int[] numPages) throws Exception
  {
    soCode = soCode.toUpperCase();

    int offset;
    String soDate;
    
    try
    {
      stmt = con.createStatement();
      
      rs = stmt.executeQuery("SELECT Date FROM so WHERE SOCode = '" + soCode + "' AND CompanyCode = '" + reqdCustomerCode + "'");
      while(rs.next())           
      {    
        ++count[0];
        soDate = rs.getString(1);
        
        offset = (int)fh.getFilePointer();

        fh.writeBytes("DOC:Sales Order:" + soCode + " " + soDate + "\n");

        if(count[0] == ((count[0] / 20) * 20))
        {  
          fhi.writeInt(offset);
          ++numPages[0];
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
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void searchSO4CustPOCode(Connection con, Statement stmt, ResultSet rs, RandomAccessFile fh, RandomAccessFile fhi, String code,
                                   String reqdCustomerCode, int[] count, int[] numPages) throws Exception
  {
    code = code.toUpperCase();

    int offset;
    String soCode, soDate;
    
    try
    {
      stmt = con.createStatement();
      
      rs = stmt.executeQuery("SELECT SOCode, Date FROM so WHERE CustomerPOCode = '" + code + "' AND CompanyCode = '" + reqdCustomerCode + "'");
      while(rs.next())           
      {    
        ++count[0];
        soCode = rs.getString(1);
        soDate = rs.getString(2);
        
        offset = (int)fh.getFilePointer();

        fh.writeBytes("DOC:Sales Order:" + soCode + " " + soDate + "\n");

        if(count[0] == ((count[0] / 20) * 20))
        {  
          fhi.writeInt(offset);
          ++numPages[0];
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
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void searchQuote(Connection con, Statement stmt, ResultSet rs, RandomAccessFile fh, RandomAccessFile fhi, String quoteCode,
                           String reqdCustomerCode, int[] count, int[] numPages) throws Exception
  {
    quoteCode = quoteCode.toUpperCase();

    int offset;
    String quoteDate;
    
    try
    {
      stmt = con.createStatement();
      
      rs = stmt.executeQuery("SELECT QuoteDate FROM quote WHERE QuoteCode = '" + quoteCode + "' AND CompanyCode = '" + reqdCustomerCode + "'");
      while(rs.next())           
      {    
        ++count[0];
        quoteDate = rs.getString(1);
        
        offset = (int)fh.getFilePointer();

        fh.writeBytes("DOC:Quotation:" + quoteCode + " " + quoteDate + "\n");

        if(count[0] == ((count[0] / 20) * 20))
        {  
          fhi.writeInt(offset);
          ++numPages[0];
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
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void searchDO(Connection con, Statement stmt, ResultSet rs, RandomAccessFile fh, RandomAccessFile fhi, String doCode,
                        String reqdCustomerCode, int[] count, int[] numPages) throws Exception
  {
    doCode = doCode.toUpperCase();

    int offset;
    String doDate;
    
    try
    {
      stmt = con.createStatement();
      
      rs = stmt.executeQuery("SELECT DOCode FROM do WHERE DOCode = '" + doCode + "' AND CompanyCode = '" + reqdCustomerCode + "'");
      while(rs.next())           
      {    
        ++count[0];
        doDate = rs.getString(1);
        
        offset = (int)fh.getFilePointer();

        fh.writeBytes("DOC:DO:" + doCode + " " + doDate + "\n");

        if(count[0] == ((count[0] / 20) * 20))
        {  
          fhi.writeInt(offset);
          ++numPages[0];
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
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void searchInvoice(Connection con, Statement stmt, ResultSet rs, RandomAccessFile fh, RandomAccessFile fhi, String invoiceCode,
                             String reqdCustomerCode, int[] count, int[] numPages) throws Exception
  {
    invoiceCode = invoiceCode.toUpperCase();

    int offset;
    String invoiceDate;
    
    try
    {
      stmt = con.createStatement();
      
      rs = stmt.executeQuery("SELECT Date FROM invoice WHERE InvoiceCode = '" + invoiceCode + "' AND CompanyCode = '" + reqdCustomerCode + "'");
      while(rs.next())           
      {    
        ++count[0];
        invoiceDate = rs.getString(1);
        
        offset = (int)fh.getFilePointer();

        fh.writeBytes("DOC:Invoice:" + invoiceCode + " " + invoiceDate + "\n");

        if(count[0] == ((count[0] / 20) * 20))
        {  
          fhi.writeInt(offset);
          ++numPages[0];
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
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void searchProformaInvoice(Connection con, Statement stmt, ResultSet rs, RandomAccessFile fh, RandomAccessFile fhi, String proformaCode,
                                     String reqdCustomerCode, int[] count, int[] numPages) throws Exception
  {
    proformaCode = proformaCode.toUpperCase();

    int offset;
    String proformaDate;
    
    try
    {
      stmt = con.createStatement();
      
      rs = stmt.executeQuery("SELECT Date FROM proforma WHERE ProformaCode = '" + proformaCode + "' AND CompanyCode = '" + reqdCustomerCode + "'");

      while(rs.next())
      {    
        ++count[0];
        proformaDate = rs.getString(1);

        offset = (int)fh.getFilePointer();

        fh.writeBytes("DOC:Proforma:" + proformaCode + " " + proformaDate + "\n");

        if(count[0] == ((count[0] / 20) * 20))
        {  
          fhi.writeInt(offset);
          ++numPages[0];
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
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void displayFirstPage(PrintWriter out, int count, String time, int numPages, String unm, String sid, String uty, String men, String den,
                                String dnm, String bnm, String localDefnsDir, String p1) throws Exception
  {
    URL url = new URL("http://" + serverUtils.serverToCall("OFSA", localDefnsDir) + "/central/servlet/TrackTraceSearchPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty
                    + "&men=" + men + "&dnm=" + dnm + "&den=" + den + "&p1=F&p3=" + generalUtils.intToStr(numPages) + "&p4=" + generalUtils.intToStr(count) + "&p6="
                    + time + "&p7=" + generalUtils.sanitise(p1) + "&bnm=" + bnm);

    URLConnection uc = url.openConnection();
    uc.setDoInput(true);
    uc.setUseCaches(false);
    uc.setDefaultUseCaches(false);

    uc.setRequestProperty("Content-Type", "application/octet-stream");

    BufferedReader di = new BufferedReader(new InputStreamReader(uc.getInputStream()));

    String s = di.readLine();
    while(s != null)
    {
      out.println(s);
      s = di.readLine();
    }
    
    di.close();
  }

}
