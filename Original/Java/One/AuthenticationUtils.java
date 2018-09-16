// =======================================================================================================================================================================================================
// System: ZaraStar: Utils: Authentication, logging, misc
// Module: serverUtils.java
// Author: C.K.Harvey
// Copyright (c) 1998-2009 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import javax.servlet.http.*;
import java.sql.*;
import java.io.*;

public class AuthenticationUtils extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  ServerUtils serverUtils = new ServerUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  Trail trail = new Trail();
  DefinitionTables definitionTables = new DefinitionTables();
  Profile profile = new Profile();
  AdminControlUtils adminControlUtils = new AdminControlUtils();

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // userType[0] set to I (internal), R (registered), or A (anonymous)
  public boolean validateSignOn(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, String unm, String pwd, String dnm, char[] userType, String[] sid, String[] userName, String[] actualUNM) throws Exception
  {
    String userDir       = directoryUtils.getSessionsDir(dnm) + "/";
    String sessionsDir   = directoryUtils.getSessionsDir(dnm);
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    unm = generalUtils.stripLeadingAndTrailingSpaces(unm);

    unm = generalUtils.capitalize(unm);
    serverUtils.removeSID(unm, sessionsDir);
    
    // not anon
    unm = generalUtils.capitalize(unm);

    boolean res;

    res = newValidateSignOn(con, stmt, rs, true, unm, pwd, userName);

    if(! res) // not validated as an internal user
    {
      if(! validateSignOnExtUser(unm, pwd, dnm, defnsDir)) // not validated against an extuser rec
      {
        return false;
      }
      // else // validated as an external user
      {
        // check registered user dir to see if all registered users are allowed access
        if(newValidateSignOn(con, stmt, rs, false, "Registered", "", userName)) // validated as an internal user
          return false;

        userType[0] = 'R';
        unm = unm + "_";
        actualUNM[0] = unm;
      }
    }
    else
    {
      userType[0] = 'I';
      actualUNM[0] = unm;
    }

    if(generalUtils.createDir(userDir + unm)) // does not (did not) exist!
    {
      if(! generalUtils.createDir(userDir + unm + "/Working"))
        return false;
    }

    sid[0] = serverUtils.newSessionID(unm, "I", dnm, sessionsDir, localDefnsDir, defnsDir);

    serverUtils.removeExtinctSIDs(sessionsDir, localDefnsDir, defnsDir);
            
    return true;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean newValidateSignOn(Connection con, Statement stmt, ResultSet rs, boolean needToCheckPassWord, String unm, String pwd, String[] userName) throws Exception
  {
    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT UserName, PassWord, Status FROM profiles WHERE UserCode = '" + generalUtils.sanitiseForSQL(unm) + "'");

      String passWord="", status="";
      
      if(rs.next())                  
      {
        userName[0] = rs.getString(1);
        passWord    = rs.getString(2);
        status      = rs.getString(3);
      } 

      if(rs   != null) rs.close();        
      if(stmt != null) stmt.close();        
      
      if(status.equals("L")) // live
      {
        if(needToCheckPassWord)
        {
          if(pwd.equals(passWord))
            return true;

          return false;
        }

        return true;
      }
      
      return false;
    }
    catch(Exception e)
    {
      System.out.println("ServerUtils: " + e);
      if(rs   != null) rs.close();        
      if(stmt != null) stmt.close();
    }
    
    return false;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean validateSignOnExtUser(String unm, String pwd, String dnm, String defnsDir) throws Exception
  {
    int i;
    if((i = unm.indexOf("_")) != -1) // registered
      unm = unm.substring(0, i);

    return profile.validateExternalAccess(unm, pwd, dnm, "", defnsDir);
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getUserNameGivenUserCode(Connection con, Statement stmt, ResultSet rs, String unm) throws Exception
  {
    String userName = "";

    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT UserName FROM profiles WHERE UserCode = '" + unm + "'"); 
      
      if(rs.next())                  
        userName = rs.getString(1);
                 
      if(rs   != null) rs.close();        
      if(stmt != null) stmt.close();        
    }
    catch(Exception e)
    {
      System.out.println("ServerUtils: " + e);
      if(rs   != null) rs.close();        
      if(stmt != null) stmt.close();
    }
    
    return userName;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(byte[] b, int bytesOut[], String str) throws Exception
  {      
    generalUtils.catAsBytes(str, 0, b, false);
    bytesOut[0] += (str.length() + 2);    
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean unprocessedInboxTransactions(Connection con, Statement stmt, ResultSet rs) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM inbox WHERE Processed != 'Y'");
      
    int rowCount = 0;
    if(rs.next())
      rowCount = rs.getInt("rowcount");
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    if(rowCount != 0)
      return true;
    
    return false;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getRelease() throws Exception
  {
    String release = "8"; // just-in-case

    Connection con = null;
    Statement stmt = null;
    ResultSet rs   = null;

    try
    {
      String uName = directoryUtils.getMySQLUserName();
      String pWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();

      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/ZaraStar_admin?user=" + uName + "&password=" + pWord);

      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT ServerRelease FROM ServerAdmin");

      if(rs.next())
        release = rs.getString(1);
    }
    catch(Exception e) { }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    if(con  != null) con.close();

    return release;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void getStyling(String dnm, String[] headerLogo, String[] headerLogoRepeat, String[] usesFlash, String[] footerText, String[] pageHeaderImage1, String[] pageHeaderImage2, String[] pageHeaderImage3, String[] pageHeaderImage4,
                          String[] pageHeaderImage5, String[] watermark)
  {
    Connection con = null;
    Statement stmt = null;
    ResultSet rs   = null;

    try
    {
      String uName = directoryUtils.getMySQLUserName();
      String pWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();
    
      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_sitewiki?user=" + uName + "&password=" + pWord);

      stmt = con.createStatement();
      rs = stmt.executeQuery("SELECT * FROM styling");   
      if(rs.next())
      {
        headerLogo[0]       = rs.getString(1); 
        headerLogoRepeat[0] = rs.getString(2);
        usesFlash[0]        = rs.getString(3);
        footerText[0]       = rs.getString(4);
        pageHeaderImage1[0] = rs.getString(5);
        pageHeaderImage2[0] = rs.getString(6);
        pageHeaderImage3[0] = rs.getString(7);
        pageHeaderImage4[0] = rs.getString(8);
        pageHeaderImage5[0] = rs.getString(9);
        watermark[0]        = rs.getString(10);
      }
      else
      {
        headerLogo[0]       = "";
        headerLogoRepeat[0] = "";
        usesFlash[0]        = "N";
        footerText[0]       = "Copyright (c)";
        pageHeaderImage1[0] = "";
        pageHeaderImage2[0] = "";
        pageHeaderImage3[0] = "";
        pageHeaderImage4[0] = "";
        pageHeaderImage5[0] = "";
        watermark[0]        = "";
      }
      
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }
    catch(Exception e)
    {
      headerLogo[0]       = "";
      headerLogoRepeat[0] = "";
      usesFlash[0]        = "N";
      footerText[0]       = "Copyright (c)";
      pageHeaderImage1[0] = "";
      pageHeaderImage2[0] = "";
      pageHeaderImage3[0] = "";
      pageHeaderImage4[0] = "";
      pageHeaderImage5[0] = "";
      watermark[0]        = "";

      System.out.println("ServerUtils: getStyling: " + e);

      try
      {
        if(rs   != null) rs.close();     
        if(stmt != null) stmt.close();
        if(con  != null) con.close();
      }
      catch(Exception e2) { }
    }
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String buildMainMenu(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, int layoutType, boolean writeSetupNow, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String callingServlet,
                               int hmenuCount, String otherSetup, String localDefnsDir, String defnsDir) throws Exception
  {
    String s = "<div id='mainmenu'>\n";
   
    if(callingServlet.equals("MainPageUtilsc")) // on signon screen
    {  
      s += "</div>";
      return s;
    }
    
    int vmenuCount = 1;

    if(uty.equals("A")) // Casual
    {
      if(layoutType < 3) s += "<h1>Cloud</h1><br/>";

      s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
      s += "<a href=\"http://" + men + "/central/servlet/SiteDisplayPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=&bnm=" + bnm + "\">Home</a></dt></dl>\n";

      if(adminControlUtils.notDisabled(con, stmt, rs, 806))
      {
        if(layoutType < 3) s += "<br/>";

        s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
        s += "<a href=\"http://" + men + "/central/servlet/RegisteredUserProfile?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Register</a></dt></dl>\n";
      }

      if(adminControlUtils.notDisabled(con, stmt, rs, 809))
      {
        if(havePersonnel(con, stmt, rs))
        {
          if(layoutType < 3) s += "<br/>";

          s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
          s += "<a href=\"http://" + men + "/central/servlet/CompanyPersonnelDirectory?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">People</a></dt></dl>\n";
        }
      }

      if(countBloggers(dnm) > 0)
      {
        if(layoutType < 3) s += "<br/>";

        s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
        s += "<a href=\"http://" + men + "/central/servlet/BlogsBlogRollList?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Blogs</a></dt></dl>\n";
      }

      if(adminControlUtils.notDisabled(con, stmt, rs, 824))
      {
        if(layoutType < 3) s += "<br/>";

        s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
        s += "<a href=\"http://" + men + "/central/servlet/RATIssuesMain?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Forum</a></dt></dl>\n";
      }

      if(adminControlUtils.notDisabled(con, stmt, rs, 802))
      {
        if(layoutType < 3) s += "<br/>";

        s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
        s += "<a href=\"http://" + men + "/central/servlet/MailZaraAnonymousUserProducts?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Products</a></dt></dl>\n";
      }

      if(adminControlUtils.notDisabled(con, stmt, rs, 808))
      {
        if(layoutType < 3) s += "<br/>";

        s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
        s += "<a href=\"http://" + men + "/central/servlet/ProductCart?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Cart</a></dt></dl>\n";
      }

      if(generalUtils.getFromDefnFile("LATITUDE", "map.dfn", localDefnsDir, defnsDir).length() > 0)
      {
        if(layoutType < 3) s += "<br/>";

        s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
        s += "<a href=\"http://" + men + "/central/servlet/GoogleMapServices?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Location</a></dt></dl>\n";
      }

      if(verifyAccess(con, stmt, rs, req, 188, unm, uty, dnm, localDefnsDir, defnsDir))
      {
        if(layoutType < 3) s += "<br/>";

        s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
        s += "<a href=\"http://" + men + "/central/servlet/AboutZara?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">About</a></dt></dl>\n";
      }
    }
    
    if(uty.equals("R")) // Registered Users
    {
      if(layoutType < 3) s += "<h1>Cloud</h1><br/>";

      s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
      s += "<a href=\"http://" + men + "/central/servlet/SiteDisplayPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=&bnm=" + bnm + "\">Home</a></dt></dl>\n";

      String[] name         = new String[1]; name[0] = "";
      String[] companyName  = new String[1]; companyName[0] = "";
      String[] accessRights = new String[1]; accessRights[0] = "";
      int i = unm.indexOf("_");

      profile.getExternalAccessNameCompanyAndRights(con, stmt, rs, unm.substring(0, i), name, companyName, accessRights);
      String[] customerCode = new String[1];
      if(! profile.getExternalAccessNameCustomerCode(con, stmt, rs, unm.substring(0, i), dnm, localDefnsDir, defnsDir, customerCode))
        customerCode[0] = "";

      if(adminControlUtils.notDisabled(con, stmt, rs, 909))
      {
        if(havePersonnel(con, stmt, rs))
        {
          if(layoutType < 3) s += "<br/>";

          s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
          s += "<a href=\"http://" + men + "/central/servlet/CompanyPersonnelDirectory?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">People</a></dt></dl>\n";
        }
      }

      if(countBloggers(dnm) > 0)
      {
        if(layoutType < 3) s += "<br/>";

        s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
        s += "<a href=\"http://" + men + "/central/servlet/BlogsBlogRollList?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Blogs</a></dt></dl>\n";
      }

      if(adminControlUtils.notDisabled(con, stmt, rs, 924))
      {
        if(layoutType < 3) s += "<br/>";

        s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
        s += "<a href=\"http://" + men + "/central/servlet/RATIssuesMain?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Forum</a></dt></dl>\n";
      }

      if(adminControlUtils.notDisabled(con, stmt, rs, 908))
      {
        if(layoutType < 3) s += "<br/>";

        s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
        s += "<a href=\"http://" + men + "/central/servlet/ProductCart?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Cart</a></dt></dl>\n";
      }

      if(customerCode[0].length() > 0 && haveProjects(customerCode[0], dnm))
      {
        if(adminControlUtils.notDisabled(con, stmt, rs, 907))
        {
          if(layoutType < 3) s += "<br/>";

          s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
          s += "<a href=\"http://" + men + "/central/servlet/ExternalUserProjects?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=" + customerCode[0] + "&bnm=" + bnm + "\">Projects</a></dt></dl>\n";
        }
      }

      if(adminControlUtils.notDisabled(con, stmt, rs, 902))
      {
        if(layoutType < 3) s += "<br/>";

        s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
        s += "<a href=\"http://" + men + "/central/servlet/RegisteredUserProducts?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Products</a></dt></dl>\n";
      }

      if(customerCode[0].length() > 0)
      {
        if(adminControlUtils.notDisabled(con, stmt, rs, 903))
        {
          if(accessRights[0].equals("sales") || accessRights[0].equals("accounts"))
          {
            if(layoutType < 3) s += "<br/>";

            s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
            s += "<a href=\"http://" + men + "/central/servlet/ExternalUserTransactionServices?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Transactions</a></dt></dl>\n";
          }
        }

        if(adminControlUtils.notDisabled(con, stmt, rs, 904))
        {
          if(accessRights[0].equals("accounts"))
          {
            if(layoutType < 3) s += "<br/>";

            s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
            s += "<a href=\"http://" + men + "/central/servlet/RegisteredUserAccounts?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Accounts</a></dt></dl>\n";
          }
        }
      }

      if(adminControlUtils.notDisabled(con, stmt, rs, 906))
      {
        if(layoutType < 3) s += "<br/>";

        s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
        s += "<a href=\"http://" + men + "/central/servlet/RegisteredUserProfile?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=" + unm + "&bnm=" + bnm + "\">My Profile</a></dt></dl>\n";
      }

      if(verifyAccess(con, stmt, rs, req, 188, unm, uty, dnm, localDefnsDir, defnsDir))
      {
        if(layoutType < 3) s += "<br/>";

        s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
        s += "<a href=\"http://" + men + "/central/servlet/AboutZara?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">About</a></dt></dl>\n";
      }
    }

    if(uty.equals("I")) // Internal Users
    {
      if(layoutType < 3) s += "<h1>Cloud</h1><br/>";
        
      s += "<dl><dt onmouseover=\"setup('vmenu" + vmenuCount++ + "');\">";
      s += "<a id='oc' href=\"http://" + men + "/central/servlet/ContactsDashboardView?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Dashboard</a></dt></dl>";

      if(verifyAccess(con, stmt, rs, req, 168, unm, uty, dnm, localDefnsDir, defnsDir))
      {
        if(layoutType < 3) s += "<br/>";
        
        s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
        s += "<a href=\"http://" + men + "/central/servlet/InboxServices?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">B2B Inbox</a></dt></dl>\n";
      }

      if(verifyAccess(con, stmt, rs, req, 6500, unm, uty, dnm, localDefnsDir, defnsDir))
      {
        if(layoutType < 3) s += "<br/>";
       
        s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
        s += "<a href=\"http://" + men + "/central/servlet/ImagesMaintainDirectories?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Images</a></dt></dl>\n";
      }

      if(verifyAccess(con, stmt, rs, req, 6100, unm, uty, dnm, localDefnsDir, defnsDir))
      {
        if(layoutType < 3) s += "<br/>";
        
        s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
        s += "<a href=\"http://" + men + "/central/servlet/TextMaintainDirectories?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Text</a></dt></dl>\n";
      }

      boolean AccountsServices   = verifyAccess(con, stmt, rs, req, 109,   unm, uty, dnm, localDefnsDir, defnsDir);
      boolean AdminServices   = verifyAccess(con, stmt, rs, req, 111,   unm, uty, dnm, localDefnsDir, defnsDir);
      boolean ProductCart   = verifyAccess(con, stmt, rs, req, 121,   unm, uty, dnm, localDefnsDir, defnsDir);
      boolean ImagesMaintainDirectories  = verifyAccess(con, stmt, rs, req, 6500,  unm, uty, dnm, localDefnsDir, defnsDir);
      boolean LibraryListDirectory = verifyAccess(con, stmt, rs, req, 12000, unm, uty, dnm, localDefnsDir, defnsDir);
      boolean ProductServices = verifyAccess(con, stmt, rs, req, 102,   unm, uty, dnm, localDefnsDir, defnsDir);
      boolean AdminDataBaseFixedFilesCreate = verifyAccess(con, stmt, rs, req, 104,   unm, uty, dnm, localDefnsDir, defnsDir);
      boolean TextMaintainDirectories  = verifyAccess(con, stmt, rs, req, 6100,  unm, uty, dnm, localDefnsDir, defnsDir);
      boolean ProductStockRecord  = verifyAccess(con, stmt, rs, req, 3001,   unm, uty, dnm, localDefnsDir, defnsDir);
      boolean CustomerPage  = verifyAccess(con, stmt, rs, req, 4001,   unm, uty, dnm, localDefnsDir, defnsDir);
      boolean SupplierPage  = verifyAccess(con, stmt, rs, req, 5001,   unm, uty, dnm, localDefnsDir, defnsDir);
 
      if(AccountsServices || AdminServices || ProductCart || ImagesMaintainDirectories || LibraryListDirectory || ProductServices || ProductServices || TextMaintainDirectories || ProductStockRecord || CustomerPage || SupplierPage)
      {
        if(layoutType < 3) s += "<br/><h1>Applications</h1>";

        if(AccountsServices)
        {
          if(layoutType < 3) s += "<br/>";

          s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
          s += "<a href=\"http://" + men + "/central/servlet/AccountsYearSelection?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Accounts</a></dt></dl>\n";
        }
    
        if(AdminServices)
        {
          if(layoutType < 3) s += "<br/>";
        
          s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
          s += "<a href=\"http://" + men + "/central/servlet/AdminServices?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Admin</a></dt></dl>\n";
        }

        if(ProductCart)
        {
          if(layoutType < 3) s += "<br/>";

          s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
          s += "<a href=\"http://" + men + "/central/servlet/ProductCart?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Cart</a></dt></dl>\n";
        }

        if(CustomerPage)
        {
          if(layoutType < 3) s += "<br/>";
        
          s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
          s += "<a href=\"http://" + men + "/central/servlet/CustomerServices?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Customer</a></dt></dl>\n";
        }
    
        if(ProductStockRecord) // stock records
        {
          if(layoutType < 3) s += "<br/>";

          s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
          s += "<a href=\"http://" + men + "/central/servlet/ProductServices?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Product</a></dt></dl>\n";
        }
    
        if(SupplierPage)
        {
          if(layoutType < 3) s += "<br/>";
        
          s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
          s += "<a href=\"http://" + men + "/central/servlet/ProductServices?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Supplier</a></dt></dl>\n";
        }
      }

      boolean FaxServices = verifyAccess(con, stmt, rs, req, 11000, unm, uty, dnm, localDefnsDir, defnsDir);
      boolean OnlineUsers = verifyAccess(con, stmt, rs, req, 12601, unm, uty, dnm, localDefnsDir, defnsDir);
      boolean AccountsgeceivableServices   = verifyAccess(con, stmt, rs, req, 158, unm, uty, dnm, localDefnsDir, defnsDir);
      boolean AccountsPayableServices   = verifyAccess(con, stmt, rs, req, 165, unm, uty, dnm, localDefnsDir, defnsDir);
      boolean InboxServices   = verifyAccess(con, stmt, rs, req, 168, unm, uty, dnm, localDefnsDir, defnsDir);
      boolean StockControlServices   = verifyAccess(con, stmt, rs, req, 180, unm, uty, dnm, localDefnsDir, defnsDir);
      boolean WorksControlServices   = verifyAccess(con, stmt, rs, req, 181, unm, uty, dnm, localDefnsDir, defnsDir);
      boolean DataAnalytics   = verifyAccess(con, stmt, rs, req, 182, unm, uty, dnm, localDefnsDir, defnsDir);
      boolean SalesControlServices   = verifyAccess(con, stmt, rs, req, 183, unm, uty, dnm, localDefnsDir, defnsDir);
      boolean _8600  = verifyAccess(con, stmt, rs, req, 8600, unm, uty, dnm, localDefnsDir, defnsDir);
      boolean ProjectList  = verifyAccess(con, stmt, rs, req, 6800, unm, uty, dnm, localDefnsDir, defnsDir);
      boolean MailZaraSignOn  = verifyAccess(con, stmt, rs, req, 8014, unm, uty, dnm, localDefnsDir, defnsDir);
      boolean _6300  = verifyAccess(con, stmt, rs, req, 6300, unm, uty, dnm, localDefnsDir, defnsDir);
      boolean ContactsAddressBook  = verifyAccess(con, stmt, rs, req, 8801, unm, uty, dnm, localDefnsDir, defnsDir);
    
      if(FaxServices || OnlineUsers || AccountsgeceivableServices || AccountsPayableServices || InboxServices || StockControlServices || WorksControlServices || DataAnalytics || SalesControlServices || _8600 || ProjectList || MailZaraSignOn || _6300 || ContactsAddressBook)
      {
        if(FaxServices)
        {
          if(layoutType < 3) s += "<br/>";
        
          s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
          s += "<a href=\"http://" + men + "/central/servlet/FaxServices?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Faxes</a></dt></dl>\n";
        }

        if(OnlineUsers)
        {
          if(layoutType < 3) s += "<br/>";
        
          s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
          s += "<a href=\"http://" + men + "/central/servlet/OnlineUsers?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Online Users</a></dt></dl>\n";
        }

        if(ProjectList)
        {
          if(layoutType < 3) s += "<br/>";
        
          s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
          s += "<a href=\"http://" + men + "/central/servlet/ProjectList?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Project</a></dt></dl>\n";
        }

        if(SalesControlServices)
        {
          if(layoutType < 3) s += "<br/>";
        
          s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
          s += "<a href=\"http://" + men + "/central/servlet/SalesControlServices?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Sales Control</a></dt></dl>\n";
        }

        if(StockControlServices)
        {
          if(layoutType < 3) s += "<br/>";

          s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
          s += "<a href=\"http://" + men + "/central/servlet/StockControlServices?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Stock Control</a></dt></dl>\n";
        }

        if(WorksControlServices)
        {
          if(layoutType < 3) s += "<br/>";
        
          s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
          s += "<a href=\"http://" + men + "/central/servlet/WorksControlServices?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Works Control</a></dt></dl>\n";
        }
      }
    
      boolean AccountsAnalyticServices = verifyAccess(con, stmt, rs, req, 110, unm, uty, dnm, localDefnsDir, defnsDir);
      boolean SalesAnalytics = verifyAccess(con, stmt, rs, req, 120, unm, uty, dnm, localDefnsDir, defnsDir);
      boolean PurchasesAnalytics = verifyAccess(con, stmt, rs, req, 122, unm, uty, dnm, localDefnsDir, defnsDir);
      boolean CompanyPersonnelDirectory = verifyAccess(con, stmt, rs, req, 128, unm, uty, dnm, localDefnsDir, defnsDir);
      boolean AboutZara = verifyAccess(con, stmt, rs, req, 188, unm, uty, dnm, localDefnsDir, defnsDir);
      boolean _7530 = verifyAccess(con, stmt, rs, req, 7530, unm, uty, dnm, localDefnsDir, defnsDir);
    
      if(AccountsAnalyticServices || SalesAnalytics || PurchasesAnalytics || CompanyPersonnelDirectory || AboutZara || _7530  || unm.equals("Tempstaff1") || unm.equals("Tempstaff2"))
      {
        if(AccountsAnalyticServices)
        {
          if(layoutType < 3) s += "<br/>";
        
          s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
          s += "<a href=\"http://" + men + "/central/servlet/AccountsAnalyticServices?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Accounts BI</a></dt></dl>\n";
        }
    
        if(adminControlUtils.anyOfACategory(con, stmt, rs, 7513, unm, uty, dnm) && !  (unm.equals("Tempstaff1") || unm.equals("Tempstaff2")))
        {
          if(layoutType < 3) s += "<br/>";

          s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
          s += "<a href=\"http://" + men + "/central/servlet/PurchasesAnalytics?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Purchases BI</a></dt></dl>\n";
        }

        if(adminControlUtils.anyOfACategory(con, stmt, rs, 7513, unm, uty, dnm) && !  (unm.equals("Tempstaff1") || unm.equals("Tempstaff2")))
        {
          if(layoutType < 3) s += "<br/>";
        
          s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
          s += "<a href=\"http://" + men + "/central/servlet/SalesAnalytics?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Sales BI</a></dt></dl>\n";
        }
    
        if(layoutType < 3) s += "<br/>";
        
        s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
        s += "<a href=\"http://" + men + "/central/servlet/DataAnalytics?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Data Analytics</a></dt></dl>\n";

        if(layoutType < 3) s += "<br/><h1>Site</h1>";
    
        s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
        s += "<a href=\"http://" + men + "/central/servlet/SiteDisplayPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=&bnm=" + bnm + "\">Home</a></dt></dl>\n";

        if(CompanyPersonnelDirectory)
        {
          if(havePersonnel(con, stmt, rs))
          {
            if(layoutType < 3) s += "<br/>";

            s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
            s += "<a href=\"http://" + men + "/central/servlet/CompanyPersonnelDirectory?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">People</a></dt></dl>\n";
          }
        }
      
        if(AboutZara)
        {
          if(layoutType < 3) s += "<br/>";
        
          s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
          s += "<a href=\"http://" + men + "/central/servlet/AboutZara?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">About</a></dt></dl>\n";
        }

        if(serverUtils.isSysAdmin(con, stmt, rs, unm, dnm, sid, uty, localDefnsDir, defnsDir) || serverUtils.isDBAdmin(con, stmt, rs, unm) || (_7530 && adminControlUtils.notDisabled(con, stmt, rs, 5900)))
        {
          if(layoutType < 3) s += "<br/>";

          s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
          s += "<a href=\"http://" + men + "/central/servlet/RATIssuesMain?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Forum</a></dt></dl>\n";
        }

        if(serverUtils.isSysAdmin(con, stmt, rs, unm, dnm, sid, uty, localDefnsDir, defnsDir)) //////////////
        {
          if(layoutType < 3) s += "<br/>";

          s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
          s += "<a href=\"http://" + men + "/central/servlet/_950?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Event</a></dt></dl>\n";
        }

        if(layoutType < 3) s += "<br/>";

        s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
        s += "<a target=\"zarahelp\" href=\"http://" + men + "/central/servlet/HelpguideJump?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\">HelpGuide</a></dt></dl>\n";
    
        if(generalUtils.getFromDefnFile("LATITUDE", "map.dfn", localDefnsDir, defnsDir).length() > 0)
        {
          if(layoutType < 3) s += "<br/>";
        
          s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
          s += "<a href=\"http://" + men + "/central/servlet/GoogleMapServices?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Location</a></dt></dl>\n";
        }
      }
    }
  
    // display any blogpages
    if(layoutType < 3)
    {
      s += "<br/><h1>News</h1>";

      Connection con2 = null;
      Statement stmt2 = null;
      ResultSet rs2   = null;
    
      try
      {
        String uName = directoryUtils.getMySQLUserName();
        String pWord = directoryUtils.getMySQLPassWord();

        Class.forName("com.mysql.jdbc.Driver").newInstance();

        con2 = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_info?user=" + uName + "&password=" + pWord);

        String code, shortTitle, isTheHomePage;
        boolean wanted;
      
        stmt2 = con2.createStatement();

        rs2 = stmt2.executeQuery("SELECT Code, ShortTitle, IsTheHomePage FROM blogs WHERE IsAMenuItem='Y' AND Published = 'Y' ORDER BY ShortTitle");

        while(rs2.next())
        {
          code          = rs2.getString(1);
          shortTitle    = rs2.getString(2);
          isTheHomePage = rs2.getString(3);

          wanted = false;
          if(isTheHomePage.equals("Y"))
          {
            ; // home page is automatically listed
          }
          else wanted = true;
        
          if(wanted)
          {
            s += "<br/><dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
            s += "<a href=\"http://" + men + "/central/servlet/SiteDisplayPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=" + code + "&bnm=" + bnm + "\">" + shortTitle + "</a></dt></dl>\n";
          }
        }
        
        if(rs2   != null) rs2.close();
        if(stmt2 != null) stmt2.close();
        if(con2  != null) con2.close();
      }
      catch(Exception e)
      {
        System.out.println("10 " + e);
        if(rs2   != null) rs2.close();
        if(stmt2 != null) stmt2.close();
        if(con2  != null) con2.close();
      }
    }
    else // layout >= 3
    {
      Connection con2 = null;
      Statement stmt2 = null;
      ResultSet rs2   = null;
      Statement stmt3 = null;
      ResultSet rs3   = null;

      try
      {
        String uName = directoryUtils.getMySQLUserName();
        String pWord = directoryUtils.getMySQLPassWord();

        Class.forName("com.mysql.jdbc.Driver").newInstance();

        con2 = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_info?user=" + uName + "&password=" + pWord);

        String topicName, code, shortTitle;

        stmt2 = con2.createStatement();

        // each topic of a blog that has a menu item set

        rs2 = stmt2.executeQuery("SELECT DISTINCT TopicName FROM blogs WHERE IsAMenuItem='Y' AND Published = 'Y' ORDER BY TopicName");

        boolean first;
        
        while(rs2.next())
        {
          topicName = rs2.getString(1);

          // output DD menu

          stmt3 = con2.createStatement();

          rs3 = stmt3.executeQuery("SELECT Code, ShortTitle FROM blogs WHERE TopicName = '" + topicName + "' AND IsAMenuItem='Y' AND Published = 'Y' AND IsTheHomePage != 'Y' ORDER BY Date");

          first = true;

          while(rs3.next())
          {
            code       = rs3.getString(1);
            shortTitle = rs3.getString(2);
           
            if(first)
            {
              s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount + "');\">" + topicName + "</dt><dd id='vmenu" + vmenuCount++ + "'><ul>\n";
              first = false;
            }

            s += "<li><a href=\"http://" + men + "/central/servlet/SiteDisplayPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=" + code + "&bnm=" + bnm + "\">" + shortTitle + "</a></li>\n";
          }
  
          if(! first) // at least one written
            s += "</ul></dd></dl>\n";

          if(rs3   != null) rs3.close();
          if(stmt3 != null) stmt3.close();
        }

        if(rs2   != null) rs2.close();
        if(stmt2 != null) stmt2.close();
        if(con2  != null) con2.close();
      }
      catch(Exception e)
      {
        System.out.println("10 " + e);
        if(rs3   != null) rs3.close();
        if(stmt3 != null) stmt3.close();
        if(rs2   != null) rs2.close();
        if(stmt2 != null) stmt2.close();
        if(con2  != null) con2.close();
      }
    }
    s += "</div>"; // mainmenu
    
    s += createSetupString(writeSetupNow, hmenuCount, vmenuCount, otherSetup);
   
    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String createSetupString(boolean writeSetupNow, int hmenuCount, int vmenuCount, String otherSetup)
  {
    String s = "<script type=\"text/javascript\">var done=false;\n";
           s += "function setup(id){vsetup(id);hsetup(id);if(!done){done=true;" + otherSetup + "}}";
           s += "function vsetup(id){";
           s += "var i,d=document.getElementById(id);\n";
           s += "for(i=1;i<=" + --vmenuCount + ";++i){if(document.getElementById('vmenu'+i)){document.getElementById('vmenu'+i).style.display='none';}}\n";
           s += "if(d){d.style.display='block';}}</script>\n";
           
    if(writeSetupNow)
      s += createHorizSetupString(hmenuCount);
      
    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String createHorizSetupString(int hmenuCount)
  {
    String s = "<script type=\"text/javascript\">var done2=false;\n";
           s += "function hsetup(id){if(!done2){done2=true;setFooter();};";
           s += "var i,d=document.getElementById(id);for(i=1;i<=" + hmenuCount + ";++i){if(document.getElementById('hmenu'+i)){document.getElementById('hmenu'+i).style.display='none';}}\n";
           s += "if(d){d.style.display='block';}}</script>\n";

    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String createHorizSetupStringW()
  {
    String s = "<script type=\"text/javascript\">\n";
           s += "function setup(count,init){";
           s += "var d;for(var i=1;i<=count;++i){d=document.getElementById('hmenu'+i);if(d){d.style.visibility='hidden';}};";
           s += "d=document.getElementById('hmenu'+count);if(d){d.style.display='block';if(init=='Y')d.style.visibility='hidden';";
           s += "else d.style.visibility='visible';}}</script>\n";

    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void buildScreen(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, boolean writeSetupNow, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String callingServlet, String subMenuText,
                          int hmenuCount, String bodyStr, String otherSetup, String localDefnsDir, String defnsDir, byte[] b, int[] bytesOut) throws Exception
  {
    scoutln(b, bytesOut, buildScreen(con, stmt, rs, req, writeSetupNow, unm, sid, uty, men, den, dnm, bnm, callingServlet, subMenuText, hmenuCount, bodyStr, otherSetup, localDefnsDir, defnsDir));
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String buildScreen(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, boolean writeSetupNow, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String callingServlet,
                            String subMenuText, int hmenuCount, String bodyStr, String otherSetup, String localDefnsDir, String defnsDir) throws Exception
  {
    String[] headerLogo       = new String[1];
    String[] headerLogoRepeat = new String[1];
    String[] usesFlash        = new String[1];
    String[] footerText       = new String[1];
    String[] pageHeaderImage1 = new String[1];
    String[] pageHeaderImage2 = new String[1];
    String[] pageHeaderImage3 = new String[1];
    String[] pageHeaderImage4 = new String[1];
    String[] pageHeaderImage5 = new String[1];
    String[] watermark        = new String[1];

    getStyling(dnm, headerLogo, headerLogoRepeat, usesFlash, footerText, pageHeaderImage1, pageHeaderImage2, pageHeaderImage3, pageHeaderImage4, pageHeaderImage5, watermark);

    String imageLibraryDir = directoryUtils.getImagesDir(dnm);
    
    String scriptsDir = directoryUtils.getScriptsDirectory();
    
    String s = "";
    
    s += "<script type=\"text/javascript\" src=\"http://" + men + "/Zara/Support/Scripts/contact.js\"></script>\n";
    s += "<script type=\"text/javascript\">";
    
    s += "function help(which){var newWindow=window.open('','zarahelp');";
    s += "newWindow.location.href=\"http://" + men + "/central/servlet/MainPageUtilsk?dnm=Help&bnm=" + bnm + "&p1=\"+which;}";

    s += "function updateCheckConnect(p1){";
    s += "var url = \"http://" + men + "/central/servlet/MediaUpdateCheck?unm=\" + escape('" + unm + "') + \"&sid=\" + escape('" + sid + "') + \"&uty=\" + escape('" + uty + "') + \"&men=\" + escape('" + men + "') + \"&p2=\" + escape('" + callingServlet
      + "') + \"&den=\" + escape('" + den + "') + \"&bnm=\" + escape('" + bnm + "') + \"&p1=\" + p1 + \"&dnm=\" + escape('" + dnm + "');\n";
    s += "contactInitRequest(url);contactReq.onreadystatechange=contactProcessRequest;contactReq.open(\"GET\",url,true);contactReq.send(null);}";
    s += "</script>\n";

    if(directoryUtils.usesFlash(dnm))
      s += "<script type=\"text/javascript\" src=\"http://" + men + scriptsDir + "flashobject.js\"></script>\n";
    
    int layoutType = getLayoutType(con, stmt, rs, unm, dnm, defnsDir);

    s+="<style type='text/css'>a:link#indexLink,a:visited#indexLink,a:hover#indexLink,a:active#indexLink{text-decoration:none;}</style>"; // for link to index.zaracloud.com
    
    s += jsForHeight(layoutType);
    s += "</head><body " + bodyStr + " onLoad=\"" + otherSetup + "setup();contactInitializeTimer(100);setFooter();\" onResize=\"setFooter();\">\n";

    s += "<div id='bigouter'>";

    if(layoutType > 2)
    {
      if(bnm.equals("M")) // fix for left-margin when fixed-width
        s += "<div style='text-align: center;'><div style='text-align: left; margin: 1em auto; width: 50%;'>";
    }

    s += "<div id='outer'>";
    s += "<table border='0' cellpadding='0' cellspacing='0'      width=100%><tr>";

    if(layoutType < 3)
      s += "<td colspan='2'>";
    else s += "<td>";
    
    s += "<table border='0' width='100%' cellpadding='0' cellspacing='0'>\n";

    if(layoutType == 5)
    {
      if(uty.equals("A"))
      {
        s += "<tr id='topBar'><td align='right' valign='top'><div id='headerrepeat'>You are not signed-on. <a href=\"http://" + men + "/central/servlet/MainPageUtilsc?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm="
          + dnm + "&p1=" + callingServlet + "&p2=" + men + "&p3=" + dnm + "&bnm=" + bnm + "\">Sign-On Here</a>";

        s += "\n</div></td></tr>\n";
      }
      else
      {
        s += "<tr><td align='right' valign='top'><div id='headerrepeat'>";

        s += "User: " + unm;

        s += "\n</div></td></tr>\n";
      }

      if(headerLogo[0].length() > 0)
        s += "<tr><td align='left' valign='top'><div id='header'><img src=\"http://" + men + imageLibraryDir + headerLogo[0] + "\" border='0'></div></td>\n";
      else s += "<tr><td align='left' valign='top'><div id='header'></div></td>\n";

      s += "</tr>";
    }
    else
    if(layoutType == 1 || layoutType == 3)
    {
      if(headerLogo[0].length() > 0)
        s += "<tr><td align='left' valign='top'><div id='header'><img src=\"http://" + men + imageLibraryDir + headerLogo[0] + "\" border='0'></div></td>\n";
      else s += "<tr><td align='left' valign='top'><div id='header'></div></td>\n";

      s += "<td width='99%' valign='top' align='right'><div id='headerrepeat'>\n";

      if(uty.equals("A"))
      {
        s += "You are not signed-on. <a href=\"http://" + men + "/central/servlet/MainPageUtilsc?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=" + callingServlet + "&p2=" + men + "&p3=" + dnm + "&bnm="
          + bnm + "\">Sign-On Here</a>\n";
      }
      else
      {
        s += "User: " + unm;
      }

      s += "</div></td></tr>";
    }
    else
    {
      s += "<tr><td width='99%' valign='top' align='right'><div id='headerrepeat'>\n";
    
      if(uty.equals("A"))
      {
        s += "You are not signed-on. <a href=\"http://" + men + "/central/servlet/MainPageUtilsc?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=" + callingServlet + "&p2=" + men + "&p3=" + dnm + "&bnm="
          + bnm + "\">Sign-On Here</a>\n";
      }
      else
      {
 //       if(zcn.length() > 0)
   //       s += "ZCN: " + zcn + "<br>";
        s += "User: " + unm;
      }
    
      s += "</div></td>";
      s += "<td align='left' valign='top'><div id='header'><img src=\"http://" + men + imageLibraryDir + headerLogo[0] + "\" border='0'></div></td></tr>\n";
    }
    
    s += "</table></td></tr>\n";

    s += "<tr><td valign='top'>";
    
    s += buildMainMenu(con, stmt, rs, req, layoutType, writeSetupNow, unm, sid, uty, men, den, dnm, bnm, callingServlet, hmenuCount, otherSetup, localDefnsDir, defnsDir);

    s += "</td>";

    if(layoutType > 2)
      s += "</tr><tr>";
    else s += "<td valign=top width=99%><table border='0' cellpadding='0' cellspacing='0'        width=100%   ><tr>";
    
    s += "<td width='99%' valign='top' height='1%'>";
    s += subMenuText;    
    s += "</td>";

    s += "</tr><tr>";

    if(layoutType < 3)
      s += "<td valign='top' width='99%' xrowspan='2'>";
    else s += "<td valign='top' width='99%'>";
    
    s += "<div id='main'>";

    return s;
  }   

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String buildPlainScreen(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String callingServlet, String subMenuText, int hmenuCount,
                                 String bodyStr, String otherSetup, String localDefnsDir, String defnsDir) throws Exception
  {
    String[] headerLogo       = new String[1];
    String[] headerLogoRepeat = new String[1];
    String[] usesFlash        = new String[1];
    String[] footerText       = new String[1];
    String[] pageHeaderImage1 = new String[1];
    String[] pageHeaderImage2 = new String[1];
    String[] pageHeaderImage3 = new String[1];
    String[] pageHeaderImage4 = new String[1];
    String[] pageHeaderImage5 = new String[1];
    String[] watermark        = new String[1];
    
    getStyling(dnm, headerLogo, headerLogoRepeat, usesFlash, footerText, pageHeaderImage1, pageHeaderImage2, pageHeaderImage3, pageHeaderImage4, pageHeaderImage5, watermark);

    String imageLibraryDir  = directoryUtils.getImagesDir(dnm);
    
    String s = "";
    
    s += "<script type=\"text/javascript\" src=\"http://" + men + "/Zara/Support/Scripts/contact.js\"></script>\n";
    s += "<script type=\"text/javascript\">";
    s += "function updateCheckConnect(p1){";
    s += "var url = \"http://" + men + "/central/servlet/MediaUpdateCheck?unm=\" + escape('" + unm + "') + \"&sid=\" + escape('" + sid + "') + \"&uty=\" + escape('" + uty
      + "') + \"&men=\" + escape('" + men + "') + \"&p2=\" + escape('" + callingServlet + "') + \"&den=\" + escape('" + den + "') + \"&bnm=\" + escape('" + bnm
      + "') + \"&p1=\" + p1 + \"&dnm=\" + escape('" + dnm + "');\n";
    s += "contactInitRequest(url);";
    s += "contactReq.onreadystatechange=contactProcessRequest;\n";
    s += "contactReq.open(\"GET\",url,true);";
    s += "contactReq.send(null);}";
    s += "</script>\n";

    s += "</head><body " + bodyStr + " onLoad=\"" + otherSetup + " contactInitializeTimer(100);\">\n";

    s += "<table border='0' width='100%' cellpadding='0' cellspacing='0'>\n";
    s += "<tr><td align='left' valign='top'><img src=\"http://" + men + imageLibraryDir + headerLogo[0] + "\" border='0'></td></tr>\n";

    s += "<tr><td valign='top'>";
    
    return s;
  }   

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String buildFooter(Connection con, Statement stmt, ResultSet rs, String unm, String dnm, String bnm, String localDefnsDir, String defnsDir) throws Exception
  {
    String[] headerLogo       = new String[1];
    String[] headerLogoRepeat = new String[1];
    String[] usesFlash        = new String[1];
    String[] footerText       = new String[1];
    String[] pageHeaderImage1 = new String[1];
    String[] pageHeaderImage2 = new String[1];
    String[] pageHeaderImage3 = new String[1];
    String[] pageHeaderImage4 = new String[1];
    String[] pageHeaderImage5 = new String[1];
    String[] watermark        = new String[1];
    
    getStyling(dnm, headerLogo, headerLogoRepeat, usesFlash, footerText, pageHeaderImage1, pageHeaderImage2, pageHeaderImage3, pageHeaderImage4, pageHeaderImage5, watermark);

    int layoutType = getLayoutType(con, stmt, rs, unm, dnm, defnsDir);

    String s = "</div></td>";

    if(layoutType < 3)
      s += "</tr></table></td>";
    
    s += "</tr><tr><td colspan='2' valign='bottom'>";

    s += "</td></tr></table></div></div>";

    if(layoutType > 2)
    {
      if(bnm.equals("M")) // fix for left-margin when fixed-width
        s += "</div></div>";  
    }

    return s + "</body></html>";
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String markup(String text)
  {
    String t = "";
    try
    {
      int x=0, len = text.length();
      while(x < len)
      {
        if(text.charAt(x) == '\n')
          t += "<br>";
        else t += text.charAt(x);
  
        ++x;
      }  
    }
    catch(Exception e) { }
    
    return t;
  }      

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int getLayoutType(Connection con, Statement stmt, ResultSet rs, String unm, String dnm, String defnsDir) throws Exception
  {
    RandomAccessFile fh = generalUtils.fileOpen(directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css");

    int layoutType;

    if(fh == null) // just-in-case!
      layoutType = 1;
    else layoutType = directoryUtils.getLayoutType(fh);

    generalUtils.fileClose(fh);
    
    return layoutType;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String jsForHeight(int layoutType) throws Exception
  {
    String s = "<script type='text/javascript'>"
             + "function getWindowHeight(){var wHt=0;if(typeof(window.innerHeight)=='number'){wHt=window.innerHeight;}else"
             + "{if(document.documentElement&&document.documentElement.clientHeight){wHt=document.documentElement.clientHeight;}else"
             + "{if(document.body&&document.body.clientHeight){wHt=document.body.clientHeight;}}}return wHt;}\n";
    
    s += "function setFooter(){if(document.getElementById){var wHt=getWindowHeight();if(wHt>0){var footerE=document.getElementById('footer');"
      + "var footerH;if(footerE==null)footerH=0;"
      + "else footerH=footerE.offsetHeight;var headerE=document.getElementById('header');var headerH=headerE.offsetHeight;"
      + "var submenuE=document.getElementById('submenu');var submenuH;if(submenuE==null) submenuH=0;else submenuH=submenuE.offsetHeight;"
      + "var topBarE=document.getElementById('topBar');var topBarH;if(topBarE==null)topBarH=0;else topBarH=topBarE.offsetHeight;\n";


    s += "var mainmenuE=document.getElementById('mainmenu');";

    if(layoutType > 2)
      s += "var mainmenuH=mainmenuE.offsetHeight;";
    else s += "var mainmenuH=0;";
    
    s += "var mainE=document.getElementById('main');\n";
    
    s += "var mainH=mainE.offsetHeight;\n";

    s += "if((mainH+headerH+footerH+mainmenuH+submenuH+topBarH)<wHt){mainE.style.height=(wHt-(headerH+footerH+mainmenuH+submenuH+topBarH))+'px';if(footerE!=null)footerE.style.position='static';}}}}</script>\n";

    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public int countBloggers(String dnm) throws Exception
  {
    Connection conInfo = null;
    Statement stmtInfo = null;
    ResultSet rsInfo = null;

    int rowCount = 0;

    try
    {
      String uName = directoryUtils.getMySQLUserName();
      String pWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();

      conInfo = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_info?user=" + uName + "&password=" + pWord);

      stmtInfo = conInfo.createStatement();

      rsInfo = stmtInfo.executeQuery("SELECT COUNT(DISTINCT Owner) AS rowcount FROM blogs");

      if(rsInfo.next())
        rowCount = rsInfo.getInt("rowcount");

      if(rsInfo   != null) rsInfo.close();
      if(stmtInfo != null) stmtInfo.close();
      if(conInfo  != null) conInfo.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(rsInfo   != null) rsInfo.close();
      if(stmtInfo != null) stmtInfo.close();
      if(conInfo  != null) conInfo.close();
    }

    return rowCount;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean havePersonnel(Connection con, Statement stmt, ResultSet rs) throws Exception
  {
    int rowCount = 0;

    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM profiles WHERE Status = 'L'");

      if(rs.next())
        rowCount = rs.getInt("rowcount");

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }

    if(rowCount > 0)
      return true;

    return false;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean haveProjects(String companyCode, String dnm) throws Exception
  {
    int rowCount = 0;

    Connection con = null;
    Statement stmt = null;
    ResultSet rs   = null;

    try
    {
      String uName = directoryUtils.getMySQLUserName();
      String pWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();

      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_project?user=" + uName + "&password=" + pWord);

      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM projects WHERE Status != 'C' AND CompanyCode = '" + companyCode + "'");

      if(rs.next())
        rowCount = rs.getInt("rowcount");

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }

    if(rowCount > 0)
      return true;

    return false;
  }


  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean verifyAccess(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, int serviceCode, String unm, String uty, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    return verifyAccess(con, stmt, rs, req, serviceCode, true, unm, uty, dnm, localDefnsDir, defnsDir);
  }
  public boolean verifyAccess(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, int serviceCode, boolean checkForMaintenance, String unm, String uty, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    if(unm.equals("Sysadmin"))
      return true;

    if(checkForMaintenance)
    {
      String[] systemStatus = new String[1];
      definitionTables.getSystem(con, stmt, rs, dnm, systemStatus);
      if(! systemStatus[0].equals("L")) // ! live
        return false;

      if(definitionTables.isSuspended(con, stmt, rs, serviceCode))
        return false;
    }

    if(serviceCode < 0) // some calls may be < 0
      return false;

    if(uty.charAt(0) == 'R')
      unm = "___registered___";

    if(! adminControlUtils.notDisabled(con, stmt, rs, serviceCode))
    {
      return false;
    }

    boolean res = profile.verifyAccess(con, stmt, rs, unm, uty, dnm, serviceCode);

    if(res)
      return true;

    return false;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // checks unm and pwd, and whether specified operation or service may be accessed, and whether docCode belongs to the company
  public boolean verifyAccessForDocument(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, int serviceCode, String docCode, String unm, String sid, String uty, String dnm, String localDefnsDir, String defnsDir)
                                         throws Exception
  {
    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
      return false;

    if(serviceCode < 0) // some calls may be < 0
      return false;

    // check if this user has access

    if(uty.length() == 0 || uty.charAt(0) == 'A')
      return false;

    if(uty.charAt(0) == 'R')
    {
      int i = unm.indexOf("_");
      unm = unm.substring(0, i);

      String docCompanyCode;

      if(serviceCode == 4121) // SO
      {
        SalesOrder salesOrder = new SalesOrder();
        docCompanyCode = salesOrder.getASOFieldGivenCode(con, stmt, rs, "CompanyCode", docCode);
        if(profile.validateExternalAccessCompany(unm, 'C', docCompanyCode, dnm, defnsDir, ""))
          return true;
      }
      else
      if(serviceCode == 4122) // Quote
      {
        Quotation quotation = new Quotation();
        docCompanyCode = quotation.getAQuoteFieldGivenCode(con, stmt, rs, "CompanyCode", docCode);
        if(profile.validateExternalAccessCompany(unm, 'C', docCompanyCode, dnm, defnsDir, ""))
          return true;
      }
      else
      if(serviceCode == 4124) // Invoice
      {
        SalesInvoice salesInvoice = new SalesInvoice();
        docCompanyCode = salesInvoice.getAnInvoiceFieldGivenCode(con, stmt, rs, "CompanyCode", docCode);
        if(profile.validateExternalAccessCompany(unm, 'C', docCompanyCode, dnm, defnsDir, ""))
          return true;
      }
      else
      if(serviceCode == 4125) // Proforma
      {
        ProformaInvoice proformaInvoice = new ProformaInvoice();
        docCompanyCode = proformaInvoice.getAProformaFieldGivenCode(con, stmt, rs, "CompanyCode", docCode);
        if(profile.validateExternalAccessCompany(unm, 'C', docCompanyCode, dnm, defnsDir, ""))
          return true;
      }
      else
      if(serviceCode == 4123) // DO
      {
        DeliveryOrder deliveryOrder = new DeliveryOrder();
        docCompanyCode = deliveryOrder.getADOFieldGivenCode(con, stmt, rs, "CompanyCode", docCode);
        if(profile.validateExternalAccessCompany(unm, 'C', docCompanyCode, dnm, defnsDir, ""))
          return true;
      }
      else
      if(serviceCode == 4126) // OC
      {
        OrderConfirmation orderConfirmation = new OrderConfirmation();
        docCompanyCode = orderConfirmation.getAnOCFieldGivenCode(con, stmt, rs, "CompanyCode", docCode);
        if(profile.validateExternalAccessCompany(unm, 'C', docCompanyCode, dnm, defnsDir, ""))
          return true;
      }
      else
      if(serviceCode == 4127) // OA
      {
        OrderAcknowledgement orderAcknowledgement = new OrderAcknowledgement();
        docCompanyCode = orderAcknowledgement.getAnOAFieldGivenCode(con, stmt, rs, "CompanyCode", docCode);
        if(profile.validateExternalAccessCompany(unm, 'C', docCompanyCode, dnm, defnsDir, ""))
          return true;
      }

      return false;
    }

    return false;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // - internal user can access any doc
  // - anon users only those docs marked as external
  // - registered users only those docs marked as external; and also if a project code exists for the doc, then the user's company must match the project company
  public boolean verifyAccessForLibrary(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, String docCode, String unm, String uty, String dnm) throws Exception
  {
    String[] systemStatus = new String[1];
    definitionTables.getSystem(con, stmt, rs, dnm, systemStatus);
    if(! systemStatus[0].equals("L")) // ! live
      return false;

    // check if this user has access

    if(uty.length() == 0)
      return false;

    String unmSave = "";

    if(uty.charAt(0) == 'R')
    {
      unmSave = unm;
      unm = "Registered"; // needs to be for rights chk for extuser rec check
    }
    else
    if(uty.charAt(0) == 'A')
    {
      unm = "Anonymous"; // needs to be for rights chk for anon rec check
    }
    // else unm is as given (an internal user, or demo user)

    if(uty.charAt(0) == 'R')
      unm = unmSave;

    if(uty.charAt(0) == 'I')
    {
      String[] userName = new String[61];
      if(newValidateSignOn(con, stmt, rs, false, unm, "", userName)) // validated as an internal user
        return true;

      return false;
    }

    return false;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // checks whether the ofsa/system table exists; if not, attempts to create every table
  public void checkTables(Connection con, Statement stmt, String dnm, String localDefnsDir, String defnsDir)
  {
    try
    {
      if(! directoryUtils.tableExists(con, stmt, "ofsa", "system", dnm))
      {
        AdminDataBaseFixedFiles  adminDataBaseFixedFiles  = new AdminDataBaseFixedFiles();
        AdminDataBaseFixedFilesCreate adminDataBaseFixedFilesCreate = new AdminDataBaseFixedFilesCreate();

        String table, tableNames = adminDataBaseFixedFiles.names();
        int x = 0, len = tableNames.length();
        while(x < len)
        {
          table = "";
          while(tableNames.charAt(x) != '\001')
            table += tableNames.charAt(x++);
          ++x;

          if(! directoryUtils.tableExists(con, stmt, "ofsa", table, dnm))
            adminDataBaseFixedFilesCreate.processATable(con, stmt, false, 'C', table, dnm, null, null, null);
        }

        MailUtils mailUtils = new MailUtils();
        if(! directoryUtils.tableExists(con, stmt, "mail", "mail", dnm))
          mailUtils.createTableMail(false, dnm);

        if(! directoryUtils.tableExists(con, stmt, "mail", "mailaccounts", dnm))
          mailUtils.createTableMailAccounts(false, dnm);

        LibraryUtils libraryUtils = new LibraryUtils();
        if(! directoryUtils.tableExists(con, stmt, "library", "documents", dnm))
          libraryUtils.createTableDocuments(false, dnm, localDefnsDir, defnsDir);

        if(! directoryUtils.tableExists(con, stmt, "library", "permissions", dnm))
          libraryUtils.createTablePermissions(false, dnm, localDefnsDir, defnsDir);

        if(! directoryUtils.tableExists(con, stmt, "library", "docmove", dnm))
          libraryUtils.createTableDocmove(false, dnm, localDefnsDir, defnsDir);

        if(! directoryUtils.tableExists(con, stmt, "library", "properties", dnm))
          libraryUtils.createTableProperties(false, dnm, localDefnsDir, defnsDir);

        ProjectUtils projectUtils = new ProjectUtils();
        if(! directoryUtils.tableExists(con, stmt, "project", "projects", dnm))
          projectUtils.createTableProjects(false, dnm, localDefnsDir, defnsDir);

        if(! directoryUtils.tableExists(con, stmt, "project", "title", dnm))
          projectUtils.createTableTitle(false, dnm, localDefnsDir, defnsDir);

        if(! directoryUtils.tableExists(con, stmt, "project", "officetasks", dnm))
          projectUtils.createTableOfficeTasks(false, dnm, localDefnsDir, defnsDir);

        if(! directoryUtils.tableExists(con, stmt, "project", "workshoptasks", dnm))
          projectUtils.createTableWorkshopTasks(false, dnm, localDefnsDir, defnsDir);

        if(! directoryUtils.tableExists(con, stmt, "project", "taskallocation", dnm))
          projectUtils.createTableTaskAllocation(false, dnm, localDefnsDir, defnsDir);

        if(! directoryUtils.tableExists(con, stmt, "project", "tasktime", dnm))
          projectUtils.createTableTaskTime(false, dnm, localDefnsDir, defnsDir);

        if(! directoryUtils.tableExists(con, stmt, "project", "stockrequirements", dnm))
          projectUtils.createTableStockRequirements(false, dnm, localDefnsDir, defnsDir);

        if(! directoryUtils.tableExists(con, stmt, "project", "permissions", dnm))
          projectUtils.createTablePermissions(false, dnm, localDefnsDir, defnsDir);

        IssuesUtils issuesUtils = new IssuesUtils();
        if(! directoryUtils.tableExists(con, stmt, "info", "rat", dnm))
          issuesUtils.createTableRAT(false, dnm, localDefnsDir, defnsDir);

        if(! directoryUtils.tableExists(con, stmt, "info", "ratcat", dnm))
          issuesUtils.createTableRATCat(false, dnm, localDefnsDir, defnsDir);

        if(! directoryUtils.tableExists(con, stmt, "info", "ratprob", dnm))
          issuesUtils.createTableRATProblemTypes(false, dnm, localDefnsDir, defnsDir);

        FaxUtils faxUtils = new FaxUtils();
        if(! directoryUtils.tableExists(con, stmt, "fax", "faxed", dnm))
          faxUtils.createTableFaxed(false, dnm, localDefnsDir, defnsDir);

        BlogsUtils blogsUtils = new BlogsUtils();
        if(! directoryUtils.tableExists(con, stmt, "info", "blogs", dnm))
          blogsUtils.createTableBlogs(false, dnm, localDefnsDir, defnsDir);

        if(! directoryUtils.tableExists(con, stmt, "info", "blogtopics", dnm))
          blogsUtils.createTableBlogTopics(false, dnm);

        if(! directoryUtils.tableExists(con, stmt, "info", "styles", dnm))
          blogsUtils.createTableStyles(false, dnm, localDefnsDir, defnsDir);

        BlogGuideUtils blogGuideUtils = new BlogGuideUtils();
        if(! directoryUtils.tableExists(con, stmt, "info", "blogguide", dnm))
          blogGuideUtils.createTableBlogGuide(false, dnm);
      }
    }
    catch(Exception e) { System.out.println("checkTables: " + e); }
  }

}
