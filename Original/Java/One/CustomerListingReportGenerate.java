// =======================================================================================================================================================================================================
// System: ZaraStar CompanyEngine: Generate customer listing report
// Module: CustomerListingReportGenerate.java
// Author: C.K.Harvey
// Copyright (c) 2000-06 Christopher Harvey. All Rights Reserved.
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

public class CustomerListingReportGenerate extends HttpServlet
{
  GeneralUtils generalUtils  = new GeneralUtils();
  MessagePage messagePage  = new MessagePage();
  ServerUtils serverUtils  = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  ReportGeneration reportGeneration  = new ReportGeneration();
  Customer customer = new Customer();
  PrintingLayout printingLayout  = new PrintingLayout();
  DirectoryUtils directoryUtils = new DirectoryUtils();

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
      p1  = req.getParameter("p1");  // which
      p2  = req.getParameter("p2");  // customer

      if(p1  == null) p1 = "L"; // listing
      if(p2  == null) p2 = "";

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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "CustomerListingReportGenerate", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 4005, bytesOut[0], 0, "ERR:" + p2);
      if(out != null) out.flush();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men,
                      String den, String dnm, String bnm, String p1, String p2, int[] bytesOut) throws Exception
  {
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String reportsDir       = directoryUtils.getUserDir('R', dnm, unm);
    String workingDir       = directoryUtils.getUserDir('W', dnm, unm);
    String imagesDir     = directoryUtils.getSupportDirs('I');
  
    long startTime = new java.util.Date().getTime();

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 4005, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "4005", "", "", "", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 4005, bytesOut[0], 0, "ACC:" + p2);
      if(con  != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "4005", "", "", "", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 4005, bytesOut[0], 0, "SID:" + p2);
      if(con  != null) con.close();
      if(out != null) out.flush();
      return;
    }

    short rtn=0;
    if(p2.length() > 0)
      rtn = r010(true, p2, p1.charAt(0), dnm, unm, bnm, reportsDir, workingDir, localDefnsDir, defnsDir);
    else rtn = r010(false, "", p1.charAt(0), dnm, unm, bnm, reportsDir, workingDir, localDefnsDir, defnsDir);

    int target;
    switch(rtn)
    {
      case -1 : // Definition File Not Found
                target = 17;
                break;
      case -2 : // cannot create report output file
                target = 18;
                break;
      case -3 : // customer not found
                target = 6;
                break;
      default : // generated ok
                target = 16;
                break;
    }

    messagePage.msgScreen(false, out, req, target, unm, sid, uty, men, den, dnm, bnm, "CustomerListingReportGenerate", "", "", "", imagesDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 4005, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p2);
    if(con  != null) con.close();
    if(out != null) out.flush();
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private short r010(boolean oneCustomer, String customerCode, char which, String dnm, String unm, String bnm, String reportsDir,
                     String workingDir, String localDefnsDir, String defnsDir) throws Exception
  {
    reportGeneration.currFont = 1;
    reportGeneration.currPage = 1;
    reportGeneration.currDown = reportGeneration.currAcross = 0.0;

    reportGeneration.oBufLen = 30000;
    reportGeneration.oBuf = new byte[30000];
    reportGeneration.oPtr = 0;

    String[] newName = new String[1];
    if((reportGeneration.fhO = reportGeneration.createNewFile((short)10, workingDir, localDefnsDir, defnsDir, reportsDir, newName)) == null)
      return -2;

    if(which == 'L') // listing report
    {
      if((reportGeneration.fhPPR = generalUtils.fileOpenD("010.ppr", localDefnsDir)) == null)
      {
        if((reportGeneration.fhPPR = generalUtils.fileOpenD("010.ppr", defnsDir)) == null)
        {
          generalUtils.fileClose(reportGeneration.fhO);
          return -1;
        }
      }
    }
    else // addresses report
    {
      if((reportGeneration.fhPPR = generalUtils.fileOpenD("010b.ppr", localDefnsDir)) == null)
      {
        if((reportGeneration.fhPPR = generalUtils.fileOpenD("010b.ppr", defnsDir)) == null)
        {
          generalUtils.fileClose(reportGeneration.fhO);
          return -1;
        }
      }
    }

    reportGeneration.lastOperationPF = false;

    reportGeneration.processControl(dnm, unm, localDefnsDir, defnsDir);
    reportGeneration.processSection("RH", null, null, (short)0, dnm, unm, localDefnsDir, defnsDir);
    reportGeneration.processSection("PH", null, null, (short)0, dnm, unm, localDefnsDir, defnsDir);

    byte[] fldNames = new byte[3000];
    byte[] fldData  = new byte[2000];

    String fieldTypes = customer.getFieldTypes();
    int numFlds = generalUtils.buildFieldNamesInBuf(customer.getFieldNames(), "Company", fldNames);

    byte[] companyCode = new byte[21];

    String userName = directoryUtils.getMySQLUserName();
    String passWord = directoryUtils.getMySQLPassWord();
    
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + userName + "&password="
                                                 + passWord);
    Statement stmt = con.createStatement();
    ResultSet rs = null;

    if(oneCustomer)
    {
      System.out.print(customerCode);

      rs = stmt.executeQuery("SELECT * FROM company WHERE CompanyCode = '" + customerCode + "'");
      if(! rs.next())
      {
        if(rs   != null) rs.close();
        if(stmt != null) stmt.close();
        if(con  != null) con.close();       
        return -3;        
      }
      
      ResultSetMetaData rsmd = rs.getMetaData();

      generalUtils.strToBytes(companyCode, customerCode);

      generalUtils.repAlpha(fldData, 2000, (short)0, companyCode);

      for(int x=1;x<numFlds;++x)
        generalUtils.repAlpha(fldData, 2000, (short)x, customer.getValue((x + 1), fieldTypes.charAt(x), rs, rsmd));

      reportGeneration.processSection("BL1", fldData, fldNames, (short)numFlds, dnm, unm, localDefnsDir, defnsDir);
    }
    else
    {
      rs = stmt.executeQuery("SELECT * FROM company");
      ResultSetMetaData rsmd = rs.getMetaData();

      while(rs.next())
      {
        customerCode = customer.getValue(1, ' ', rs, rsmd);
        generalUtils.strToBytes(companyCode, customerCode);

        if(companyCode[0] != '\000') // just-in-case
        {
          System.out.print(" " + customerCode);
      
          generalUtils.repAlpha(fldData, 2000, (short)0, companyCode);

          for(int x=1;x<numFlds;++x)
            generalUtils.repAlpha(fldData, 2000, (short)x, customer.getValue((x + 1), fieldTypes.charAt(x), rs, rsmd));

          reportGeneration.processSection("BL1", fldData, fldNames, (short)numFlds, dnm, unm, localDefnsDir, defnsDir);
        }
      }
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    if(con  != null) con.close();       
    
    if(! reportGeneration.lastOperationPF)
      reportGeneration.processSection("PF", null, null, (short)0, dnm, unm, localDefnsDir, defnsDir);
    reportGeneration.processSection("RF", null, null, (short)0, dnm, unm, localDefnsDir, defnsDir);

    reportGeneration.fhO.write(reportGeneration.oBuf, 0, reportGeneration.oPtr);

    reportGeneration.fhO.close();
    reportGeneration.fhPPR.close();

    printingLayout.updateNumPages(0, newName[0], reportsDir);

    return 0;
  }

}
