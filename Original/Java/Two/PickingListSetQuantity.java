// =======================================================================================================================================================================================================
// System: ZaraStar: DocumentEngine: PL Set Quantity Packed & Completed
// Module: PickingListSetQuantity.java
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
import java.net.*;
import java.sql.*;

public class PickingListSetQuantity extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  PickingList pickingList = new PickingList();
  
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="";

    int service = 3056;
    
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
      p1  = req.getParameter("p1"); // code
      p2  = req.getParameter("p2"); // mode: Q for Set Quantities, Y for Set Completed, N for Set Not Completed

      service = 3054;

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, service, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "PickingListSetQuantity", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, service, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush(); 
    }
  }

  // ------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm,
                    String bnm, String p1, String p2, int service, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');
    
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, service, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "" + service, imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, service, bytesOut[0], 0, "ACC:" + p1);
    if(con  != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "" + service, imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, service, bytesOut[0], 0, "SID:" + p1);
    if(con  != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    set(con, stmt, rs, out, p1, p2, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, service, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con  != null) con.close();
    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String docCode, String mode, String unm, String sid, String uty, String men, String den, String dnm,
                   String bnm, String localDefnsDir, String defnsDir) throws Exception
  {
    byte[] code = new byte[21];
    generalUtils.strToBytes(code, docCode);

    switch(mode.charAt(0))
    {
      case 'Q' : pickingList.setQuantities(con, stmt, rs, code, unm, dnm, localDefnsDir, defnsDir);
                 break;
      case 'Y' : pickingList.updatePLField(con, stmt, rs, code, "Completed", "Y", dnm, localDefnsDir, defnsDir);
      
                 String salesPerson = pickingList.getAPLFieldGivenCode(con, stmt, rs, "SalesPerson", docCode);
                 String companyName = pickingList.getAPLFieldGivenCode(con, stmt, rs, "CompanyName", docCode);
                 sendNotifications(con, stmt, rs, salesPerson, docCode, companyName);

                 pickingList.updatePLField(con, stmt, rs, code, "DateOfStockUpdate", generalUtils.todaySQLFormat(localDefnsDir, defnsDir), dnm, localDefnsDir, defnsDir);
                 break;
      case 'N' : pickingList.updatePLField(con, stmt, rs, code, "Completed", "N", dnm, localDefnsDir, defnsDir);
                 pickingList.updatePLField(con, stmt, rs, code, "DateOfStockUpdate", "1970-01-01", dnm, localDefnsDir, defnsDir);
                 break;
    }

    byte[] dataAlready = new byte[2000];
    dataAlready[0] = '\000';

    getRec(out, unm, sid, uty, men, den, dnm, bnm, docCode, "", dataAlready, 'A', localDefnsDir);
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void getRec(PrintWriter out, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String code,
                      String errStr, byte[] dataAlready, char cad, String localDefnsDir) throws Exception
  {
    URL url = new URL("http://" + serverUtils.serverToCall("OFSA", localDefnsDir) + "/central/servlet/GoodsReceivedPickingList");

    HttpURLConnection uc = (HttpURLConnection)url.openConnection();
    uc.setDoInput(true);
    uc.setDoOutput(true);
    uc.setUseCaches(false);
    uc.setDefaultUseCaches(false);
    uc.setRequestProperty("Content-Type", "application/x-www-form.urlencoded");

    String s2 = "&unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm
              + "&bnm=" + bnm + "&p1="  + generalUtils.sanitise(code) + "&p2="  + cad + "&p3="  + generalUtils.sanitise(true, errStr)
              + "&p4="  + generalUtils.sanitise(dataAlready);

    Integer len = new Integer(s2.length());
    uc.setRequestProperty("Content-Length", len.toString());
    
    uc.setRequestMethod("POST");

    PrintWriter p = new PrintWriter(uc.getOutputStream());
    p.print(s2);    
    p.flush();
    p.close();

    BufferedReader di = new BufferedReader(new InputStreamReader(uc.getInputStream()));
    String s = di.readLine();
    while(s != null)
    {
      out.println(s);
      s = di.readLine();
    }

    di.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void sendNotifications(Connection con, Statement stmt, ResultSet rs, String salesPerson, String docCode, String companyName) throws Exception
  {
      if(1==1)return;
    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT userCode FROM managepeople WHERE SalesPerson = '" + generalUtils.sanitiseForSQL(salesPerson) + "'");

      String userCode;

      while(rs.next())
      {
        userCode = rs.getString(1);
        // send notification here
      }
    }
    catch(Exception e)
    {
      System.out.println(e);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

}

