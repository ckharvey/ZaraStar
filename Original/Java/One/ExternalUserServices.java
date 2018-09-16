// =======================================================================================================================================================================================================
// System: ZaraStar UtilsEngine: Ext User services access page
// Module: ExternalUserServices.java
// Author: C.K.Harvey
// Copyright (c) 2002-09 Christopher Harvey. All Rights Reserved.
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

public class ExternalUserServices extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  Customer customer = new Customer();
  Supplier supplier = new Supplier();
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

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="";

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
      
      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, bytesOut);
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
 
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "ExternalUserServices", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 901, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    long startTime = new java.util.Date().getTime();

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! adminControlUtils.notDisabled(con, stmt, rs, 901))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "ExternalUserServices", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 901, bytesOut[0], 0, "ACC:");
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "ExternalUserServices", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 901, bytesOut[0], 0, "SID:");
      if(con != null) con.close();
     if(out != null) out.flush();
      return;
    }

    set(con, stmt, rs, out, req, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 901, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut)
                   throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Registered User Services Access</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    boolean RegisteredUserProducts = adminControlUtils.notDisabled(con, stmt, rs, 902);

    if(RegisteredUserProducts)
    {
      scoutln(out, bytesOut, "function products(){");
      scoutln(out, bytesOut, "window.location.href=\"/central/servlet/RegisteredUserProducts?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\";}");
    }

    scoutln(out, bytesOut, "function doIt(clearTransaction){");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/_124?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=X&p2=\"+clearTransaction+\"&bnm=" + bnm + "\";}");
    
    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\"></head>");
  
    int[] hmenuCount = new int[1];
 
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "901", "", "ExternalUserServices", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    String[] name         = new String[1];
    String[] companyName  = new String[1];
    String[] accessRights = new String[1];
    int i = unm.indexOf("_");

    profile.getExternalAccessNameCompanyAndRights(con, stmt, rs, unm.substring(0, i), name, companyName, accessRights);

    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Registered User Services for " + name[0] + " of " + companyName[0], "901", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<form><table id=\"page\" width=100% border=0>");

    String[] customerCode = new String[1];
    if(! profile.getExternalAccessNameCustomerCode(con, stmt, rs, unm.substring(0, i), dnm, localDefnsDir, defnsDir, customerCode))
      customerCode[0] = "";

    // ----------------------------------------------------------------------------------

    if(RegisteredUserProducts)
    {
      if(accessRights[0].equals("sales") || accessRights[0].equals("accounts") || accessRights[0].equals("store"))
      {
        scoutln(out, bytesOut, "<tr><td nowrap colspan=3><h1>Orders and Enquiries</td></tr>");

        scoutln(out, bytesOut, "<td></td><td nowrap colspan=2><p><a href=\"javascript:products()\">Search</a> our Stock DataBase</td></tr>");
        scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
      }
    }
    
    // ----------------------------------------------------------------------------------

    if(customerCode[0].length() > 0)
    {    
      if(adminControlUtils.notDisabled(con, stmt, rs, 903))
      {
        if(accessRights[0].equals("sales") || accessRights[0].equals("accounts"))
        {
          scoutln(out, bytesOut, "<tr><td nowrap colspan=3><h1>Transaction Services</td></tr>");

          scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/TrackTraceMainExternal?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                               + "\">Track &amp; Trace</a> Existing Transactions</td><td width=70%><span id=\"service\">&nbsp;&nbsp;&nbsp;<a href=\"javascript:help('2012')\">(Service 2012)</a></span></td></tr>");

          scoutln(out, bytesOut, "<tr><td></td><td colspan=2><p>This service allows you to search documents relating to all previously closed, and currently outstanding, transactions.</td></tr>");
        }
      }
    }  
    
    // ----------------------------------------------------------------------------------

    if(customerCode[0].length() > 0)
    {
      if(adminControlUtils.notDisabled(con, stmt, rs, 904))
      {
        if(accessRights[0].equals("accounts"))
        {
          scoutln(out, bytesOut, "<tr><td nowrap colspan=3><h1>Accounts Services</td></tr>");

          scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/RegisteredUserAccounts?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                               + "\">View</a> Statement of Account</td><td width=70%><span id=\"service\">&nbsp;&nbsp;&nbsp;<a href=\"javascript:help('904')\">(Service 904)</a></span></td></tr>");

          scoutln(out, bytesOut, "<tr><td></td><td colspan=2><p>This service allows you to view a statement of account for the current, or a previous, month.</td></tr>");
        }
      }
    }

    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir));

  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }
  
}
