// =======================================================================================================================================================================================================
// System: ZaraStar AdminEngine: process appconfig
// Module: AdminAppconfigProcess.java
// Author: C.K.Harvey
// Copyright (c) 2003-08 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;
import java.util.*;
import java.io.*;

public class AdminAppconfigProcess extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  AdminUtils adminUtils = new AdminUtils();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", companyName="", companyPhone="", companyFax="", logo="", currentStyle="",
           applicationStartDate="", financialYearStartMonth="", financialYearEndMonth="", effectiveStartDate="", dateStyle="", dateSeparator="",
           publishSite="", description="", domainName="", zcn="";

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
        if(name.equals("companyName"))
          companyName = value[0];
        else
        if(name.equals("companyPhone"))
          companyPhone = value[0];
        else
        if(name.equals("companyFax"))
          companyFax = value[0];
        else
        if(name.equals("logo"))
          logo = value[0];
        else
        if(name.equals("currentStyle"))
          currentStyle = value[0];
        else
        if(name.equals("applicationStartDate"))
          applicationStartDate = value[0];
        else
        if(name.equals("financialYearStartMonth"))
          financialYearStartMonth = value[0];
        else
        if(name.equals("financialYearEndMonth"))
          financialYearEndMonth = value[0];
        else
        if(name.equals("effectiveStartDate"))
          effectiveStartDate = value[0];
        else
        if(name.equals("dateStyle"))
          dateStyle = value[0];
        else
        if(name.equals("dateSeparator"))
          dateSeparator = value[0];
        else
        if(name.equals("publishSite"))
          publishSite = value[0];
        else
        if(name.equals("description"))
          description = value[0];
        else
        if(name.equals("domainName"))
          domainName = value[0];
        else
        if(name.equals("zcn"))
          zcn = value[0];
      }

      doIt(out, req, companyName, companyPhone, companyFax, logo, currentStyle, applicationStartDate, financialYearStartMonth, financialYearEndMonth,
           effectiveStartDate, dateStyle, dateSeparator, publishSite, description, domainName, zcn, unm, sid, uty, men, den, dnm, bnm, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "AdminAppconfigProcess", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7062, bytesOut[0], 0, "ERR:" + companyName);
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String companyName, String companyPhone, String companyFax, String logo, String currentStyle,
                    String applicationStartDate, String financialYearStartMonth, String financialYearEndMonth, String effectiveStartDate, String dateStyle,
                    String dateSeparator, String publishSite, String description, String domainName, String zcn, String unm, String sid, String uty, String men,
                    String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    String imagesDir     = directoryUtils.getSupportDirs('I');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String defnsDir      = directoryUtils.getSupportDirs('D');

    long startTime = new java.util.Date().getTime();

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 7062, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "AdminAppconfigEdit", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7062, bytesOut[0], 0, "ACC:" + companyName);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "AdminAppconfigEdit", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7062, bytesOut[0], 0, "SID:" + companyName);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }
        
    applicationStartDate = generalUtils.convertDateToSQLFormat(applicationStartDate);
    effectiveStartDate   = generalUtils.convertDateToSQLFormat(effectiveStartDate);

    zcn = generalUtils.stripAllSpaces(zcn);
    
    if(publishSite != null && publishSite.equals("on"))
      publishSite = "Y";
    else publishSite = "N";
    
    int target;
    if(process(con, stmt, companyName, companyPhone, companyFax, logo, currentStyle, applicationStartDate, financialYearStartMonth, financialYearEndMonth, effectiveStartDate, publishSite, description, domainName, dnm))
    {
      if(process(dateStyle, dateSeparator, localDefnsDir))
        target = 27;
      else target = 23;
    }
    else target = 23;

    messagePage.msgScreen(false, out, req, target, unm, sid, uty, men, den, dnm, bnm, "AdminAppconfigProcess", "", "", "", imagesDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 7062, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), companyName);
    if(con  != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean process(Connection con, Statement stmt, String companyName, String companyPhone, String companyFax, String logo, String currentStyle,
                          String applicationStartDate, String financialYearStartMonth, String financialYearEndMonth, String effectiveStartDate,
                          String publishSite, String description, String domainName, String dnm) throws Exception
  {
    try
    {
      stmt = con.createStatement();
          
      String q = "UPDATE appconfig SET CompanyName = '" + generalUtils.sanitiseForSQL(companyName) + "', CompanyPhone = '" + generalUtils.sanitiseForSQL(companyPhone) + "', CompanyFax = '" + generalUtils.sanitiseForSQL(companyFax) + "', CompanyEMail = '"
               + generalUtils.sanitiseForSQL("") + "', TerseName = '" + generalUtils.sanitiseForSQL(companyName) + "', ApplicationStartDate = '" + applicationStartDate + "', FinancialYearStartMonth = '" + financialYearStartMonth
               + "', FinancialYearEndMonth = '" + financialYearEndMonth + "', EffectiveStartDate = '" + effectiveStartDate + "', Description = '" + generalUtils.sanitiseForSQL(description) + "', Latitude = '0', Longitude = '0', GoogleMapsKey = '"
               + generalUtils.sanitiseForSQL("") + "' WHERE DNM = '" + dnm + "'";

      stmt.executeUpdate(q);
        
      if(stmt != null) stmt.close();
      
      return true;
    }
    catch(Exception e)
    {
      System.out.println("7062a: " + e);
      if(stmt != null) stmt.close();
    }
    
    return false;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean process(String dateStyle, String dateSeparator, String localDefnsDir) throws Exception
  {
    if(! generalUtils.fileExists(localDefnsDir + "dates.dfn"))
    {
      if(generalUtils.createDefnFile("dates.dfn", localDefnsDir))
        return false;
    }
    
    if(generalUtils.repInDefnFile("FORMAT", dateStyle, "dates.dfn", localDefnsDir, ""))
    {
      if(generalUtils.repInDefnFile("SEPARATOR", dateSeparator, "dates.dfn", localDefnsDir, ""))
        return true;
    }
    
    return false;
  }

}
