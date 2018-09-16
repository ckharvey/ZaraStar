// =======================================================================================================================================================================================================
// System: ZaraStar Project: Update project details
// Module: ProjectUpdateProject.java
// Author: C.K.Harvey
// Copyright (c) 2006-09 Christopher Harvey. All Rights Reserved.
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
import java.util.*;
import java.sql.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class ProjectUpdateProject extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  ProjectUtils projectUtils = new ProjectUtils();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", code="", title="", requestedDeliveryDate="", enquiryDate="", customerReference="", product="", note="", endUser="", contractor="", country="", currency="", quotedValue="",
           remark="", dateOfPO="", dateIssuedToContracts="", status="", dateOfReview="", reviewedBy="", statedDeliveryDate="", companyCode="", owner="", checkedBy="", dateCompleted="", dateIssuedToWorkshop="", newOrEdit="";
    
    try
    {
      directoryUtils.setContentHeaders(res);
      out = res.getWriter();

      Enumeration en = req.getParameterNames();
      while(en.hasMoreElements())
      {
        String name = (String)en.nextElement();
        String[] value = req.getParameterValues(name);
        if(name.equals("unm"))
          unm = value[0];
        else
        if(name.equals("sid"))
          sid = value[0];
        else
        if(name.equals("uty"))
          uty = value[0];
        else
        if(name.equals("men"))
          men = value[0];
        else
        if(name.equals("den"))
          den = value[0];
        else
        if(name.equals("dnm"))
          dnm = value[0];
        else
        if(name.equals("bnm"))
          bnm = value[0];
        else
        if(name.equals("code"))
          code = value[0];
        else
        if(name.equals("title"))
          title = value[0];
        else
        if(name.equals("requestedDeliveryDate"))
          requestedDeliveryDate = value[0];
        else
        if(name.equals("enquiryDate"))
          enquiryDate = value[0];
        else
        if(name.equals("customerReference"))
          customerReference = value[0];
        else
        if(name.equals("product"))
          product = value[0];
        else
        if(name.equals("note"))
          note = value[0];
        else
        if(name.equals("endUser"))
          endUser = value[0];
        else
        if(name.equals("contractor"))
          contractor = value[0];
        else
        if(name.equals("country"))
          country = value[0];
        else
        if(name.equals("currency"))
          currency = value[0];
        else
        if(name.equals("quotedValue"))
          quotedValue = value[0];
        else
        if(name.equals("remark"))
          remark = value[0];
        else
        if(name.equals("dateOfPO"))
          dateOfPO = value[0];
        else
        if(name.equals("dateIssuedToContracts"))
          dateIssuedToContracts = value[0];
        else
        if(name.equals("status"))
          status = value[0];
        else
        if(name.equals("dateOfReview"))
          dateOfReview = value[0];
        else
        if(name.equals("reviewedBy"))
          reviewedBy = value[0];
        else
        if(name.equals("statedDeliveryDate"))
          statedDeliveryDate = value[0];
        else
        if(name.equals("companyCode"))
          companyCode = value[0];
        else
        if(name.equals("checkedBy"))
          checkedBy = value[0];
        else
        if(name.equals("owner"))
          owner = value[0];
        else
        if(name.equals("dateCompleted"))
          dateCompleted = value[0];
        else
        if(name.equals("dateIssuedToWorkshop"))
          dateIssuedToWorkshop = value[0];
        else
        if(name.equals("newOrEdit"))
          newOrEdit = value[0];
      }

      doIt(out, req, newOrEdit, code, title, requestedDeliveryDate, enquiryDate, customerReference, product, note, endUser, contractor, country, currency, quotedValue, remark, dateOfPO, dateIssuedToContracts, status, dateOfReview, reviewedBy,
           statedDeliveryDate, companyCode, owner, checkedBy, dateCompleted, dateIssuedToWorkshop, unm, sid, uty, men, den, dnm, bnm, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "ProjectUpdateProject", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6801, bytesOut[0], 0, "ERR:" + code);
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String newOrEdit, String code, String title, String requestedDeliveryDate, String enquiryDate, String customerReference, String product, String note, String endUser, String contractor,
                    String country, String currency, String quotedValue, String remark, String dateOfPO, String dateIssuedToContracts, String status, String dateOfReview, String reviewedBy, String statedDeliveryDate, String companyCode,
                    String owner, String checkedBy, String dateCompleted, String dateIssuedToWorkshop, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 6801, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "ProjectUpdateProject", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6801, bytesOut[0], 0, "ACC:" + code);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "ProjectUpdateProject", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6801, bytesOut[0], 0, "SID:" + code);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }
    
    if(requestedDeliveryDate.length() == 0)
      requestedDeliveryDate = "1970-01-01";
    else requestedDeliveryDate = generalUtils.convertDateToSQLFormat(requestedDeliveryDate); 
    
    if(enquiryDate.length() == 0)
      enquiryDate = "1970-01-01";
    else enquiryDate = generalUtils.convertDateToSQLFormat(enquiryDate); 

    if(dateOfPO.length() == 0) 
      dateOfPO = "1970-01-01";
    else dateOfPO = generalUtils.convertDateToSQLFormat(dateOfPO); 
    
    if(dateIssuedToContracts.length() == 0)
      dateIssuedToContracts = "1970-01-01";
    else dateIssuedToContracts = generalUtils.convertDateToSQLFormat(dateIssuedToContracts); 
    
    if(dateOfReview.length() == 0)
      dateOfReview = "1970-01-01";
    else dateOfReview = generalUtils.convertDateToSQLFormat(dateOfReview); 
    
    if(statedDeliveryDate.length() == 0)
      statedDeliveryDate = "1970-01-01";
    else statedDeliveryDate = generalUtils.convertDateToSQLFormat(statedDeliveryDate); 
    
    if(dateCompleted.length() == 0) 
      dateCompleted = "1970-01-01";
    else dateCompleted = generalUtils.convertDateToSQLFormat(dateCompleted); 
    
    if(dateIssuedToWorkshop.length() == 0) 
      dateIssuedToWorkshop = "1970-01-01";
    else dateIssuedToWorkshop = generalUtils.convertDateToSQLFormat(dateIssuedToWorkshop); 
    
    if(quotedValue.length() == 0) 
      quotedValue = "0.0";

    String[] newCode = new String[1];
    
    projectUtils.updateProjects(newOrEdit, code, title, requestedDeliveryDate, enquiryDate, customerReference, product, note, endUser, contractor, country, currency, quotedValue, remark, dateOfPO, dateIssuedToContracts, status, dateOfReview, reviewedBy,
                         statedDeliveryDate, companyCode, owner, checkedBy, dateCompleted, dateIssuedToWorkshop, dnm, localDefnsDir, defnsDir, newCode);

    reGetRec(out, unm, sid, uty, men, den, dnm, bnm, newCode[0], localDefnsDir);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 6801, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), code);
    if(con != null) con.close();
    if(out != null) out.flush();
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void reGetRec(PrintWriter out, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String code, String localDefnsDir) throws Exception
  {
    URL url = new URL("http://" + serverUtils.serverToCall("OFSA", localDefnsDir) + "/central/servlet/ProjectMain?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + generalUtils.sanitise(code));

    HttpURLConnection uc = (HttpURLConnection)url.openConnection();
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
