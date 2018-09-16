// =======================================================================================================================================================================================================
// System: ZaraStar Utils: fetch main menu
// Module: AdminControlUtilsMain.java
// Author: C.K.Harvey
// Copyright (c) 2002-09 Christopher Harvey. All Rights Reserved.
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

public class AdminControlUtilsMain extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  AdminUtils adminUtils = new AdminUtils();
  DefinitionTables definitionTables = new DefinitionTables();
  Profile profile = new Profile();
  AdminControlUtils adminControlUtils = new AdminControlUtils();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="";

    try
    {
      directoryUtils.setContentHeaders(res);
      res.setHeader("Header", "HTTP/1.0 200 OK");
      res.setStatus(200);
      res.setContentType("text/html");
      res.setHeader("Cache-Control", "no-cache");

      unm = req.getParameter("unm");
      sid = req.getParameter("sid");
      uty = req.getParameter("uty");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");

      doIt(req, res, unm, sid, uty, men, den, dnm, bnm, bytesOut);
    }
    catch(Exception e)
    {
      System.out.println(("Unexpected System Error: 7599a: " + e));
      res.getWriter().write("Unexpected System Error: 7599a");
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(HttpServletRequest req, HttpServletResponse res, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    res.getWriter().write( mergeData(req, unm, sid, uty, men, den, dnm, bnm, bytesOut));

    serverUtils.totalBytes(req, unm, dnm, 7599, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private String mergeData(HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();

    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    
    Statement stmt = null;
    ResultSet rs   = null;

    int[] count = new int[1];  count[0] = 1;

    String s = getMainMenuItems(con, stmt, rs, req, unm, sid, uty, dnm, localDefnsDir, defnsDir, count);

    --count[0];

    if(con != null) con.close();

    return "{ res: [{ \"msg\":\"" + count[0] + "\"," + s + "}]}";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String addOption(String label, String servlet, String params, int[] count)
  {
    String s = "";

    try
    {
      if(count[0] > 1)
        s += ",";
      s += "\"x" + count[0] + "1\":\"" + label + "\",\"x" + count[0] + "2\":\"" + servlet + "\",\"x" + count[0] + "3\":\"" + params + "\"";
      ++count[0];
    }
    catch(Exception e) { }

    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String getMainMenuItems(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, String unm, String sid, String uty, String dnm, String localDefnsDir, String defnsDir, int[] count)
  {
    String s = "";

    try
    {
      if(uty.equals("A")) // Casual ------------------------------------------------------------------------------------------------------------------------------------------------------------------------
      {
        s += addOption("Home", "SiteDisplayPageWave", "&p1=", count);

        if(generalUtils.getFromDefnFile("LATITUDE", "map.dfn", localDefnsDir, defnsDir).length() > 0)
          s += addOption("Location", "GoogleMapServicesw", "", count);

        if(authenticationUtils.verifyAccess(con, stmt, rs, req, 188, unm, uty, dnm, localDefnsDir, defnsDir))
          s += addOption("About", "AboutZaraw", "", count);
      }

      if(uty.equals("R")) // Registered Users --------------------------------------------------------------------------------------------------------------------------------------------------------------
      {
        s += addOption("Home", "SiteDisplayPageWave", "&p1=", count);

        String[] name         = new String[1]; name[0] = "";
        String[] companyName  = new String[1]; companyName[0] = "";
        String[] accessRights = new String[1]; accessRights[0] = "";
        int i = unm.indexOf("_");

        profile.getExternalAccessNameCompanyAndRights(con, stmt, rs, unm.substring(0, i), name, companyName, accessRights);
        String[] customerCode = new String[1];
        if(! profile.getExternalAccessNameCustomerCode(con, stmt, rs, unm.substring(0, i), dnm, localDefnsDir, defnsDir, customerCode))
          customerCode[0] = "";

        if(adminControlUtils.notDisabled(con, stmt, rs, 908))
          s += addOption("Cart", "ProductCartw", "", count);

        if(customerCode[0].length() > 0 && authenticationUtils.haveProjects(customerCode[0], dnm))
        {
          if(adminControlUtils.notDisabled(con, stmt, rs, 907))
            s += addOption("Projects", "ExternalUserProjectsw", "&p1=" + customerCode[0], count);
        }

        if(adminControlUtils.notDisabled(con, stmt, rs, 902))
          s += addOption("Products", "RegisteredUserProductsw", "", count);

        if(customerCode[0].length() > 0)
        {
          if(adminControlUtils.notDisabled(con, stmt, rs, 903))
          {
            if(accessRights[0].equals("sales") || accessRights[0].equals("accounts"))
              s += addOption("Transactions", "ExternalUserTransactionServicesw", "", count);
          }

          if(adminControlUtils.notDisabled(con, stmt, rs, 904))
          {
            if(accessRights[0].equals("accounts"))
              s += addOption("Accounts", "RegisteredUserAccountsw", "", count);
          }
        }

        if(adminControlUtils.notDisabled(con, stmt, rs, 906))
          s += addOption("My Profile", "RegisteredUserProfilew", "", count);

        if(authenticationUtils.verifyAccess(con, stmt, rs, req, 188, unm, uty, dnm, localDefnsDir, defnsDir))
          s += addOption("About", "AboutZaraw", "", count);
      }

      if(uty.equals("I")) // Internal Users -----------------------------------------------------------------------------------------------------------------------------------------------------------------------
      {
        boolean AccountsServices  = authenticationUtils.verifyAccess(con, stmt, rs, req, 109,  unm, uty, dnm, localDefnsDir, defnsDir);
        boolean ProductStockRecord = authenticationUtils.verifyAccess(con, stmt, rs, req, 3001, unm, uty, dnm, localDefnsDir, defnsDir);
        boolean CustomerPage = authenticationUtils.verifyAccess(con, stmt, rs, req, 4001, unm, uty, dnm, localDefnsDir, defnsDir);
        boolean SupplierPage = authenticationUtils.verifyAccess(con, stmt, rs, req, 5001, unm, uty, dnm, localDefnsDir, defnsDir);

        if(AccountsServices || ProductStockRecord || CustomerPage || SupplierPage)
        {
          if(AccountsServices)
            s += addOption("Accounts", "AccountsYearSelectionWave", "", count);

          if(CustomerPage)
            s += addOption("Customer", "CustomerServicesWave", "", count);

          if(ProductStockRecord) // stock records
            s += addOption("Product", "ProductServicesw", "", count);

          if(SupplierPage)
            s += addOption("Supplier", "ProductServicesw", "", count);
        }

        boolean FaxServices = authenticationUtils.verifyAccess(con, stmt, rs, req, 11000, unm, uty, dnm, localDefnsDir, defnsDir);
        boolean InboxServices   = authenticationUtils.verifyAccess(con, stmt, rs, req, 168,   unm, uty, dnm, localDefnsDir, defnsDir);
        boolean ProductCart   = authenticationUtils.verifyAccess(con, stmt, rs, req, 121,   unm, uty, dnm, localDefnsDir, defnsDir);
        boolean ProjectList  = authenticationUtils.verifyAccess(con, stmt, rs, req, 6800,  unm, uty, dnm, localDefnsDir, defnsDir);

        if(FaxServices || ProductCart || InboxServices || ProjectList)
        {
          s += addOption("-", "", "", count);

          if(FaxServices)
            s += addOption("Faxes", "FaxServicesw", "", count);

          if(ProductCart)
            s += addOption("Cart", "ProductCartw", "", count);

          if(InboxServices)
            s += addOption("B2B Inbox", "InboxServicesw", "", count);

          if(ProjectList)
            s += addOption("Projects", "ProjectListw", "", count);
        }
        
        boolean StockControlServices   = authenticationUtils.verifyAccess(con, stmt, rs, req, 180, unm, uty, dnm, localDefnsDir, defnsDir);
        boolean WorksControlServices   = authenticationUtils.verifyAccess(con, stmt, rs, req, 181, unm, uty, dnm, localDefnsDir, defnsDir);
        boolean DataAnalytics   = authenticationUtils.verifyAccess(con, stmt, rs, req, 182, unm, uty, dnm, localDefnsDir, defnsDir);
        boolean SalesControlServices   = authenticationUtils.verifyAccess(con, stmt, rs, req, 183, unm, uty, dnm, localDefnsDir, defnsDir);

        if(StockControlServices || WorksControlServices || DataAnalytics || SalesControlServices)
        {
          s += addOption("-", "", "", count);

          if(SalesControlServices)
            s += addOption("Sales Control", "SalesControlServicesw", "", count);

          if(DataAnalytics)
            s += addOption("Purchases Control", "DataAnalyticsw", "", count);

          if(StockControlServices)
            s += addOption("Stock Control", "StockControlServicesw", "", count);
       
          if(WorksControlServices)
            s += addOption("Works Control", "WorksControlServicesw", "", count);
        }

        boolean AccountsAnalyticServices = authenticationUtils.verifyAccess(con, stmt, rs, req, 110, unm, uty, dnm, localDefnsDir, defnsDir);
        boolean _7513 = adminControlUtils.anyOfACategory(con, stmt, rs, 7513, unm, uty, dnm);

        if(AccountsAnalyticServices || _7513)
        {
          s += addOption("-", "", "", count);

          if(AccountsAnalyticServices)
            s += addOption("Accounts Analytics", "AccountsAnalyticServicesw", "", count);

          if(_7513)
            s += addOption("Sales Analytics", "SalesAnalyticsw", "", count);

          if(_7513)
            s += addOption("Purchases Analytics", "PurchasesAnalyticsw", "", count);
        }

        boolean AboutZara  = authenticationUtils.verifyAccess(con, stmt, rs, req, 188,  unm, uty, dnm, localDefnsDir, defnsDir);
        boolean _7530 = authenticationUtils.verifyAccess(con, stmt, rs, req, 7530, unm, uty, dnm, localDefnsDir, defnsDir);
        boolean ImagesMaintainDirectories = authenticationUtils.verifyAccess(con, stmt, rs, req, 6500, unm, uty, dnm, localDefnsDir, defnsDir);
        boolean TextMaintainDirectories = authenticationUtils.verifyAccess(con, stmt, rs, req, 6100, unm, uty, dnm, localDefnsDir, defnsDir);

        s += addOption("-", "", "", count);

        s += addOption("Blogs", "BlogsOptionsWave", "", count);

        if(AboutZara)
          s += addOption("About", "AboutZaraw", "", count);

        if(serverUtils.isSysAdmin(con, stmt, rs, unm, dnm, sid, uty, localDefnsDir, defnsDir) || serverUtils.isDBAdmin(con, stmt, rs, unm) || (_7530 && adminControlUtils.notDisabled(con, stmt, rs, 5900)))
          s += addOption("Issues", "RATIssuesMainWave", "", count);

        if(generalUtils.getFromDefnFile("LATITUDE", "map.dfn", localDefnsDir, defnsDir).length() > 0)
          s += addOption("Location", "GoogleMapServices", "", count);

        if(ImagesMaintainDirectories)
          s += addOption("Images", "ImagesMaintainDirectoriesWave", "", count);

        if(TextMaintainDirectories)
          s += addOption("Text", "TextMaintainDirectoriesWave", "", count);
      }
    }
    catch(Exception e) { System.out.println("7599a main menu: " + e); }

    return s;
  }
  
}

