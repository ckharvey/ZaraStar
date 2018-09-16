// =======================================================================================================================================================================================================
// System: ZaraStar: AdminEngine - DataBase Builder access page
// Module: AdminDataBaseBuilder.java
// Author: C.K.Harvey
// Copyright (c) 2003-07 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
//
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

public class AdminDataBaseBuilder extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  DashboardUtils dashboardUtils = new DashboardUtils();
  SignOnAdministrator  signOnAdministrator  = new SignOnAdministrator();

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
      uty = req.getParameter("uty");
      sid = req.getParameter("sid");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");
      
      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "AdminDataBaseBuilder", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7200, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men,
                      String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');

    long startTime = new java.util.Date().getTime();

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! serverUtils.isSysAdmin(con, stmt, rs, unm, dnm, sid, uty, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "AdminDataBaseBuilder", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7200, bytesOut[0], 0, "ACC:");
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "AdminDataBaseBuilder", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7200, bytesOut[0], 0, "SID:");
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }
  
    create(con, stmt, rs, out, unm, sid, uty, men, den, dnm, bnm, imagesDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 7200, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void create(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String unm, String sid, String uty, String men,
                      String den, String dnm, String bnm, String imagesDir, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Admin - DataBase Builder</title>");
    
    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\""
                           + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    signOnAdministrator.heading(out, true, unm, sid, uty, bnm, dnm, men, den, imagesDir, bytesOut);

    dashboardUtils.drawTitle(out, "DataBase Builder", "7200", unm, sid, uty, men, den, dnm, bnm, bytesOut);
  
    scoutln(out, bytesOut, "<table id=\"page\" border=0 width=100%>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
    scoutln(out, bytesOut, "<a href=\"/central/servlet/_7201?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men="
                           + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Manage</a> Mapping Tables</td>");
    scoutln(out, bytesOut, "<td nowrap width=90%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                         + "<a href=\"javascript:help('7201')\">(Service 7201)</a></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
    scoutln(out, bytesOut, "<a href=\"/central/servlet/AdminDataBaseBuilderImport?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men="
                           + men + "&den=" + den + "&dnm=" + dnm + "&p1=po&bnm=" + bnm + "\">Import</a> PO Header</td>");
    scoutln(out, bytesOut, "<td nowrap width=90%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                         + "<a href=\"javascript:help('7203')\">(Service 7203)</a></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
    scoutln(out, bytesOut, "<a href=\"/central/servlet/AdminDataBaseBuilderImport?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men="
                           + men + "&den=" + den + "&dnm=" + dnm + "&p1=pol&bnm=" + bnm + "\">Import</a> PO Lines</td>");
    scoutln(out, bytesOut, "<td nowrap width=90%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                         + "<a href=\"javascript:help('7204')\">(Service 7204)</a></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
    scoutln(out, bytesOut, "<a href=\"/central/servlet/AdminDataBaseBuilderImport?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men="
                           + men + "&den=" + den + "&dnm=" + dnm + "&p1=poll&bnm=" + bnm + "\">Import</a> PO Multiple Lines</td>");
    scoutln(out, bytesOut, "<td nowrap width=90%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                         + "<a href=\"javascript:help('7206')\">(Service 7206)</a></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
    scoutln(out, bytesOut, "<a href=\"/central/servlet/AdminDataBaseBuilderImport?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men="
                           + men + "&den=" + den + "&dnm=" + dnm + "&p1=so&bnm=" + bnm + "\">Import</a> SO Header</td>");
    scoutln(out, bytesOut, "<td nowrap width=90%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                         + "<a href=\"javascript:help('7205')\">(Service 7205)</a></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
    scoutln(out, bytesOut, "<a href=\"/central/servlet/AdminDataBaseBuilderImport?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men="
                           + men + "&den=" + den + "&dnm=" + dnm + "&p1=sol&bnm=" + bnm + "\">Import</a> SO Lines</td>");
    scoutln(out, bytesOut, "<td nowrap width=90%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                         + "<a href=\"javascript:help('7206')\">(Service 7206)</a></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
    scoutln(out, bytesOut, "<a href=\"/central/servlet/AdminDataBaseBuilderImport?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men="
                           + men + "&den=" + den + "&dnm=" + dnm + "&p1=soll&bnm=" + bnm + "\">Import</a> SO Multiple Lines</td>");
    scoutln(out, bytesOut, "<td nowrap width=90%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                         + "<a href=\"javascript:help('7206')\">(Service 7206)</a></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
    scoutln(out, bytesOut, "<a href=\"/central/servlet/AdminDataBaseBuilderImport?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men="
                           + men + "&den=" + den + "&dnm=" + dnm + "&p1=company&bnm=" + bnm + "\">Import</a> Customer</td>");
    scoutln(out, bytesOut, "<td nowrap width=90%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                         + "<a href=\"javascript:help('7207')\">(Service 7207)</a></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
    scoutln(out, bytesOut, "<a href=\"/central/servlet/AdminDataBaseBuilderImport?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men="
                           + men + "&den=" + den + "&dnm=" + dnm + "&p1=stock&bnm=" + bnm + "\">Import</a> Stock</td>");
    scoutln(out, bytesOut, "<td nowrap width=90%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                         + "<a href=\"javascript:help('7207')\">(Service 7207)</a></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
    scoutln(out, bytesOut, "<a href=\"/central/servlet/AdminDataBaseBuilderImport?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men="
                           + men + "&den=" + den + "&dnm=" + dnm + "&p1=stockx&bnm=" + bnm + "\">Import</a> Stock Levels</td>");
    scoutln(out, bytesOut, "<td nowrap width=90%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                         + "<a href=\"javascript:help('7207')\">(Service 7207)</a></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
    scoutln(out, bytesOut, "<a href=\"/central/servlet/AdminDataBaseBuilderImport?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men="
                           + men + "&den=" + den + "&dnm=" + dnm + "&p1=quote&bnm=" + bnm + "\">Import</a> Quote Header</td>");
    scoutln(out, bytesOut, "<td nowrap width=90%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                         + "<a href=\"javascript:help('7207')\">(Service 7207)</a></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
    scoutln(out, bytesOut, "<a href=\"/central/servlet/AdminDataBaseBuilderImport?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men="
                           + men + "&den=" + den + "&dnm=" + dnm + "&p1=quotel&bnm=" + bnm + "\">Import</a> Quote Lines</td>");
    scoutln(out, bytesOut, "<td nowrap width=90%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                         + "<a href=\"javascript:help('7207')\">(Service 7207)</a></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
    scoutln(out, bytesOut, "<a href=\"/central/servlet/AdminDataBaseBuilderImport?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men="
                           + men + "&den=" + den + "&dnm=" + dnm + "&p1=quotell&bnm=" + bnm + "\">Import</a> Quote Multiple Lines</td>");
    scoutln(out, bytesOut, "<td nowrap width=90%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                         + "<a href=\"javascript:help('7207')\">(Service 7207)</a></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
    scoutln(out, bytesOut, "<a href=\"/central/servlet/AdminDataBaseBuilderImport?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men="
                           + men + "&den=" + den + "&dnm=" + dnm + "&p1=invoice&bnm=" + bnm + "\">Import</a> Invoice Header</td>");
    scoutln(out, bytesOut, "<td nowrap width=90%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                         + "<a href=\"javascript:help('7207')\">(Service 7207)</a></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
    scoutln(out, bytesOut, "<a href=\"/central/servlet/AdminDataBaseBuilderImport?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men="
                           + men + "&den=" + den + "&dnm=" + dnm + "&p1=invoicel&bnm=" + bnm + "\">Import</a> Invoice Lines</td>");
    scoutln(out, bytesOut, "<td nowrap width=90%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                         + "<a href=\"javascript:help('7207')\">(Service 7207)</a></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
    scoutln(out, bytesOut, "<a href=\"/central/servlet/AdminDataBaseBuilderImport?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men="
                           + men + "&den=" + den + "&dnm=" + dnm + "&p1=invoicell&bnm=" + bnm + "\">Import</a> Invoice Multiple Lines</td>");
    scoutln(out, bytesOut, "<td nowrap width=90%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                         + "<a href=\"javascript:help('7207')\">(Service 7207)</a></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
    scoutln(out, bytesOut, "<a href=\"/central/servlet/AdminDataBaseBuilderImport?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men="
                           + men + "&den=" + den + "&dnm=" + dnm + "&p1=itemmove&bnm=" + bnm + "\">Import</a> Stock Adjustment Records</td>");
    scoutln(out, bytesOut, "<td nowrap width=90%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                         + "<a href=\"javascript:help('7207')\">(Service 7207)</a></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
    scoutln(out, bytesOut, "<a href=\"/central/servlet/AdminDataBaseBuilderImport?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men="
                           + men + "&den=" + den + "&dnm=" + dnm + "&p1=stockc&bnm=" + bnm + "\">Import</a> Stock Check Records</td>");
    scoutln(out, bytesOut, "<td nowrap width=90%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                         + "<a href=\"javascript:help('7207')\">(Service 7207)</a></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
    scoutln(out, bytesOut, "<a href=\"/central/servlet/AdminDataBaseBuilderImport?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men="
                           + men + "&den=" + den + "&dnm=" + dnm + "&p1=proforma&bnm=" + bnm + "\">Import</a> Proforma Invoice Header</td>");
    scoutln(out, bytesOut, "<td nowrap width=90%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                         + "<a href=\"javascript:help('7207')\">(Service 7207)</a></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
    scoutln(out, bytesOut, "<a href=\"/central/servlet/AdminDataBaseBuilderImport?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men="
                           + men + "&den=" + den + "&dnm=" + dnm + "&p1=proformal&bnm=" + bnm + "\">Import</a> Proforma Invoice Lines</td>");
    scoutln(out, bytesOut, "<td nowrap width=90%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                         + "<a href=\"javascript:help('7207')\">(Service 7207)</a></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
    scoutln(out, bytesOut, "<a href=\"/central/servlet/AdminDataBaseBuilderImport?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men="
                           + men + "&den=" + den + "&dnm=" + dnm + "&p1=proformall&bnm=" + bnm + "\">Import</a> Proforma Invoice Multiple Lines</td>");
    scoutln(out, bytesOut, "<td nowrap width=90%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                         + "<a href=\"javascript:help('7207')\">(Service 7207)</a></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
    scoutln(out, bytesOut, "<a href=\"/central/servlet/AdminDataBaseBuilderImport?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men="
                           + men + "&den=" + den + "&dnm=" + dnm + "&p1=debit&bnm=" + bnm + "\">Import</a> Debit Note Header</td>");
    scoutln(out, bytesOut, "<td nowrap width=90%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                         + "<a href=\"javascript:help('7207')\">(Service 7207)</a></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
    scoutln(out, bytesOut, "<a href=\"/central/servlet/AdminDataBaseBuilderImport?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men="
                           + men + "&den=" + den + "&dnm=" + dnm + "&p1=debitl&bnm=" + bnm + "\">Import</a> Debit Notes Lines</td>");
    scoutln(out, bytesOut, "<td nowrap width=90%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                         + "<a href=\"javascript:help('7207')\">(Service 7207)</a></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
    scoutln(out, bytesOut, "<a href=\"/central/servlet/AdminDataBaseBuilderImport?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men="
                           + men + "&den=" + den + "&dnm=" + dnm + "&p1=debitll&bnm=" + bnm + "\">Import</a> Debit Note Multiple Lines</td>");
    scoutln(out, bytesOut, "<td nowrap width=90%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                         + "<a href=\"javascript:help('7207')\">(Service 7207)</a></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
    scoutln(out, bytesOut, "<a href=\"/central/servlet/AdminDataBaseBuilderImport?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men="
                           + men + "&den=" + den + "&dnm=" + dnm + "&p1=pdebit&bnm=" + bnm + "\">Import</a> Purchase Debit Note Header</td>");
    scoutln(out, bytesOut, "<td nowrap width=90%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                         + "<a href=\"javascript:help('7207')\">(Service 7207)</a></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
    scoutln(out, bytesOut, "<a href=\"/central/servlet/AdminDataBaseBuilderImport?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men="
                           + men + "&den=" + den + "&dnm=" + dnm + "&p1=pdebitl&bnm=" + bnm + "\">Import</a> Purchase Debit Note Lines</td>");
    scoutln(out, bytesOut, "<td nowrap width=90%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                         + "<a href=\"javascript:help('7207')\">(Service 7207)</a></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
    scoutln(out, bytesOut, "<a href=\"/central/servlet/AdminDataBaseBuilderImport?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men="
                           + men + "&den=" + den + "&dnm=" + dnm + "&p1=pdebitll&bnm=" + bnm + "\">Import</a> Purchase Debit Note Multiple Lines</td>");
    scoutln(out, bytesOut, "<td nowrap width=90%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                         + "<a href=\"javascript:help('7207')\">(Service 7207)</a></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
    scoutln(out, bytesOut, "<a href=\"/central/servlet/AdminDataBaseBuilderImport?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men="
                           + men + "&den=" + den + "&dnm=" + dnm + "&p1=pinvoice&bnm=" + bnm + "\">Import</a> Purchase Invoice Header</td>");
    scoutln(out, bytesOut, "<td nowrap width=90%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                         + "<a href=\"javascript:help('7207')\">(Service 7207)</a></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
    scoutln(out, bytesOut, "<a href=\"/central/servlet/AdminDataBaseBuilderImport?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men="
                           + men + "&den=" + den + "&dnm=" + dnm + "&p1=pinvoicel&bnm=" + bnm + "\">Import</a> Purchase Invoice Lines</td>");
    scoutln(out, bytesOut, "<td nowrap width=90%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                         + "<a href=\"javascript:help('7207')\">(Service 7207)</a></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
    scoutln(out, bytesOut, "<a href=\"/central/servlet/AdminDataBaseBuilderImport?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men="
                           + men + "&den=" + den + "&dnm=" + dnm + "&p1=pinvoicell&bnm=" + bnm + "\">Import</a> Purchase Invoice Multiple Lines</td>");
    scoutln(out, bytesOut, "<td nowrap width=90%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                         + "<a href=\"javascript:help('7207')\">(Service 7207)</a></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
    scoutln(out, bytesOut, "<a href=\"/central/servlet/AdminDataBaseBuilderImport?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men="
                           + men + "&den=" + den + "&dnm=" + dnm + "&p1=credit&bnm=" + bnm + "\">Import</a> Credit Note Header</td>");
    scoutln(out, bytesOut, "<td nowrap width=90%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                         + "<a href=\"javascript:help('7207')\">(Service 7207)</a></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
    scoutln(out, bytesOut, "<a href=\"/central/servlet/AdminDataBaseBuilderImport?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men="
                           + men + "&den=" + den + "&dnm=" + dnm + "&p1=creditl&bnm=" + bnm + "\">Import</a> Credit Note Lines</td>");
    scoutln(out, bytesOut, "<td nowrap width=90%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                         + "<a href=\"javascript:help('7207')\">(Service 7207)</a></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
    scoutln(out, bytesOut, "<a href=\"/central/servlet/AdminDataBaseBuilderImport?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men="
                           + men + "&den=" + den + "&dnm=" + dnm + "&p1=creditll&bnm=" + bnm + "\">Import</a> Credit Note Multiple Lines</td>");
    scoutln(out, bytesOut, "<td nowrap width=90%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                         + "<a href=\"javascript:help('7207')\">(Service 7207)</a></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
    scoutln(out, bytesOut, "<a href=\"/central/servlet/AdminDataBaseBuilderImport?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men="
                           + men + "&den=" + den + "&dnm=" + dnm + "&p1=pcredit&bnm=" + bnm + "\">Import</a> Purchase Credit Note Header</td>");
    scoutln(out, bytesOut, "<td nowrap width=90%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                         + "<a href=\"javascript:help('7207')\">(Service 7207)</a></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
    scoutln(out, bytesOut, "<a href=\"/central/servlet/AdminDataBaseBuilderImport?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men="
                           + men + "&den=" + den + "&dnm=" + dnm + "&p1=pcreditl&bnm=" + bnm + "\">Import</a> Purchase Credit Note Lines</td>");
    scoutln(out, bytesOut, "<td nowrap width=90%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                         + "<a href=\"javascript:help('7207')\">(Service 7207)</a></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
    scoutln(out, bytesOut, "<a href=\"/central/servlet/AdminDataBaseBuilderImport?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men="
                           + men + "&den=" + den + "&dnm=" + dnm + "&p1=pcreditll&bnm=" + bnm + "\">Import</a> Purchase Credit Note Multiple Lines</td>");
    scoutln(out, bytesOut, "<td nowrap width=90%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                         + "<a href=\"javascript:help('7207')\">(Service 7207)</a></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
    scoutln(out, bytesOut, "<a href=\"/central/servlet/AdminDataBaseBuilderImport?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men="
                           + men + "&den=" + den + "&dnm=" + dnm + "&p1=receipt&bnm=" + bnm + "\">Import</a> Receipt Header</td>");
    scoutln(out, bytesOut, "<td nowrap width=90%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                         + "<a href=\"javascript:help('7207')\">(Service 7207)</a></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
    scoutln(out, bytesOut, "<a href=\"/central/servlet/AdminDataBaseBuilderImport?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men="
                           + men + "&den=" + den + "&dnm=" + dnm + "&p1=receiptl&bnm=" + bnm + "\">Import</a> Receipt Lines</td>");
    scoutln(out, bytesOut, "<td nowrap width=90%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                         + "<a href=\"javascript:help('7207')\">(Service 7207)</a></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
    scoutln(out, bytesOut, "<a href=\"/central/servlet/AdminDataBaseBuilderImport?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men="
                           + men + "&den=" + den + "&dnm=" + dnm + "&p1=payment&bnm=" + bnm + "\">Import</a> Payment Header</td>");
    scoutln(out, bytesOut, "<td nowrap width=90%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                         + "<a href=\"javascript:help('7207')\">(Service 7207)</a></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
    scoutln(out, bytesOut, "<a href=\"/central/servlet/AdminDataBaseBuilderImport?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men="
                           + men + "&den=" + den + "&dnm=" + dnm + "&p1=paymentl&bnm=" + bnm + "\">Import</a> Payment Lines</td>");
    scoutln(out, bytesOut, "<td nowrap width=90%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                         + "<a href=\"javascript:help('7207')\">(Service 7207)</a></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
    scoutln(out, bytesOut, "<a href=\"/central/servlet/AdminDataBaseBuilderImport?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men="
                           + men + "&den=" + den + "&dnm=" + dnm + "&p1=do&bnm=" + bnm + "\">Import</a> DO Header</td>");
    scoutln(out, bytesOut, "<td nowrap width=90%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                         + "<a href=\"javascript:help('7207')\">(Service 7207)</a></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
    scoutln(out, bytesOut, "<a href=\"/central/servlet/AdminDataBaseBuilderImport?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men="
                           + men + "&den=" + den + "&dnm=" + dnm + "&p1=dol&bnm=" + bnm + "\">Import</a> DO Lines</td>");
    scoutln(out, bytesOut, "<td nowrap width=90%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                         + "<a href=\"javascript:help('7207')\">(Service 7207)</a></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
    scoutln(out, bytesOut, "<a href=\"/central/servlet/AdminDataBaseBuilderImport?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men="
                           + men + "&den=" + den + "&dnm=" + dnm + "&p1=doll&bnm=" + bnm + "\">Import</a> DO Multiple Lines</td>");
    scoutln(out, bytesOut, "<td nowrap width=90%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                         + "<a href=\"javascript:help('7207')\">(Service 7207)</a></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
    scoutln(out, bytesOut, "<a href=\"/central/servlet/AdminDataBaseBuilderImport?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men="
                           + men + "&den=" + den + "&dnm=" + dnm + "&p1=pl&bnm=" + bnm + "\">Import</a> Picking List Header</td>");
    scoutln(out, bytesOut, "<td nowrap width=90%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                         + "<a href=\"javascript:help('7207')\">(Service 7207)</a></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
    scoutln(out, bytesOut, "<a href=\"/central/servlet/AdminDataBaseBuilderImport?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men="
                           + men + "&den=" + den + "&dnm=" + dnm + "&p1=pll&bnm=" + bnm + "\">Import</a> Picking List Lines</td>");
    scoutln(out, bytesOut, "<td nowrap width=90%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                         + "<a href=\"javascript:help('7207')\">(Service 7207)</a></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
    scoutln(out, bytesOut, "<a href=\"/central/servlet/AdminDataBaseBuilderImport?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men="
                           + men + "&den=" + den + "&dnm=" + dnm + "&p1=plll&bnm=" + bnm + "\">Import</a> Picking List Multiple Lines</td>");
    scoutln(out, bytesOut, "<td nowrap width=90%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                         + "<a href=\"javascript:help('7207')\">(Service 7207)</a></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
    scoutln(out, bytesOut, "<a href=\"/central/servlet/AdminDataBaseBuilderImport?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men="
                           + men + "&den=" + den + "&dnm=" + dnm + "&p1=gr&bnm=" + bnm + "\">Import</a> GRN Header</td>");
    scoutln(out, bytesOut, "<td nowrap width=90%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                         + "<a href=\"javascript:help('7207')\">(Service 7207)</a></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
    scoutln(out, bytesOut, "<a href=\"/central/servlet/AdminDataBaseBuilderImport?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men="
                           + men + "&den=" + den + "&dnm=" + dnm + "&p1=grl&bnm=" + bnm + "\">Import</a> GRN Lines</td>");
    scoutln(out, bytesOut, "<td nowrap width=90%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                         + "<a href=\"javascript:help('7207')\">(Service 7207)</a></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
    scoutln(out, bytesOut, "<a href=\"/central/servlet/AdminDataBaseBuilderImport?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men="
                           + men + "&den=" + den + "&dnm=" + dnm + "&p1=lp&bnm=" + bnm + "\">Import</a> Local Requisition Header</td>");
    scoutln(out, bytesOut, "<td nowrap width=90%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                         + "<a href=\"javascript:help('7207')\">(Service 7207)</a></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
    scoutln(out, bytesOut, "<a href=\"/central/servlet/AdminDataBaseBuilderImport?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men="
                           + men + "&den=" + den + "&dnm=" + dnm + "&p1=lpl&bnm=" + bnm + "\">Import</a> Local Requisition Lines</td>");
    scoutln(out, bytesOut, "<td nowrap width=90%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                         + "<a href=\"javascript:help('7207')\">(Service 7207)</a></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
    scoutln(out, bytesOut, "<a href=\"/central/servlet/AdminDataBaseBuilderImport?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men="
                           + men + "&den=" + den + "&dnm=" + dnm + "&p1=lpll&bnm=" + bnm + "\">Import</a> Local Requisition Multiple Lines</td>");
    scoutln(out, bytesOut, "<td nowrap width=90%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                         + "<a href=\"javascript:help('7207')\">(Service 7207)</a></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
    scoutln(out, bytesOut, "<a href=\"/central/servlet/AdminDataBaseBuilderImport?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men="
                           + men + "&den=" + den + "&dnm=" + dnm + "&p1=oc&bnm=" + bnm + "\">Import</a> Order Confirmation Header</td>");
    scoutln(out, bytesOut, "<td nowrap width=90%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                         + "<a href=\"javascript:help('7207')\">(Service 7207)</a></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
    scoutln(out, bytesOut, "<a href=\"/central/servlet/AdminDataBaseBuilderImport?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men="
                           + men + "&den=" + den + "&dnm=" + dnm + "&p1=ocl&bnm=" + bnm + "\">Import</a> Order Confirmation Lines</td>");
    scoutln(out, bytesOut, "<td nowrap width=90%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                         + "<a href=\"javascript:help('7207')\">(Service 7207)</a></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
    scoutln(out, bytesOut, "<a href=\"/central/servlet/AdminDataBaseBuilderImport?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men="
                           + men + "&den=" + den + "&dnm=" + dnm + "&p1=ocll&bnm=" + bnm + "\">Import</a> Order Confirmation Multiple Lines</td>");
    scoutln(out, bytesOut, "<td nowrap width=90%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                         + "<a href=\"javascript:help('7207')\">(Service 7207)</a></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
    scoutln(out, bytesOut, "<a href=\"/central/servlet/AdminDataBaseBuilderImport?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men="
                           + men + "&den=" + den + "&dnm=" + dnm + "&p1=supplier&bnm=" + bnm + "\">Import</a> Supplier</td>");
    scoutln(out, bytesOut, "<td nowrap width=90%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                         + "<a href=\"javascript:help('7207')\">(Service 7207)</a></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
    scoutln(out, bytesOut, "<a href=\"/central/servlet/AdminDataBaseBuilderImport?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men="
                           + men + "&den=" + den + "&dnm=" + dnm + "&p1=ar&bnm=" + bnm + "\">Import</a> AR Records Header</td>");
    scoutln(out, bytesOut, "<td nowrap width=90%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                         + "<a href=\"javascript:help('7207')\">(Service 7207)</a></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
    scoutln(out, bytesOut, "<a href=\"/central/servlet/AdminDataBaseBuilderImport?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men="
                           + men + "&den=" + den + "&dnm=" + dnm + "&p1=arl&bnm=" + bnm + "\">Import</a> AR Records Lines</td>");
    scoutln(out, bytesOut, "<td nowrap width=90%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                         + "<a href=\"javascript:help('7207')\">(Service 7207)</a></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
    scoutln(out, bytesOut, "<a href=\"/central/servlet/AdminDataBaseBuilderImport?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men="
                           + men + "&den=" + den + "&dnm=" + dnm + "&p1=ap&bnm=" + bnm + "\">Import</a> AP Records Header</td>");
    scoutln(out, bytesOut, "<td nowrap width=90%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                         + "<a href=\"javascript:help('7207')\">(Service 7207)</a></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
    scoutln(out, bytesOut, "<a href=\"/central/servlet/AdminDataBaseBuilderImport?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men="
                           + men + "&den=" + den + "&dnm=" + dnm + "&p1=apl&bnm=" + bnm + "\">Import</a> AP Records Lines</td>");
    scoutln(out, bytesOut, "<td nowrap width=90%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                         + "<a href=\"javascript:help('7207')\">(Service 7207)</a></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
    scoutln(out, bytesOut, "<a href=\"/central/servlet/AdminDataBaseBuilderImport?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men="
                           + men + "&den=" + den + "&dnm=" + dnm + "&p1=voucher&bnm=" + bnm + "\">Import</a> Payment Voucher Header</td>");
    scoutln(out, bytesOut, "<td nowrap width=90%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                         + "<a href=\"javascript:help('7207')\">(Service 7207)</a></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
    scoutln(out, bytesOut, "<a href=\"/central/servlet/AdminDataBaseBuilderImport?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men="
                           + men + "&den=" + den + "&dnm=" + dnm + "&p1=voucherl&bnm=" + bnm + "\">Import</a> Payment Voucher Lines</td>");
    scoutln(out, bytesOut, "<td nowrap width=90%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                         + "<a href=\"javascript:help('7207')\">(Service 7207)</a></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
    scoutln(out, bytesOut, "<a href=\"/central/servlet/AdminDataBaseBuilderImport?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men="
                           + men + "&den=" + den + "&dnm=" + dnm + "&p1=voucherll&bnm=" + bnm + "\">Import</a> Payment Voucher Multiple Lines</td>");
    scoutln(out, bytesOut, "<td nowrap width=90%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                         + "<a href=\"javascript:help('7207')\">(Service 7207)</a></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
    scoutln(out, bytesOut, "<a href=\"/central/servlet/AdminDataBaseBuilderImport?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men="
                           + men + "&den=" + den + "&dnm=" + dnm + "&p1=rvoucher&bnm=" + bnm + "\">Import</a> Receipt Voucher Header</td>");
    scoutln(out, bytesOut, "<td nowrap width=90%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                         + "<a href=\"javascript:help('7207')\">(Service 7207)</a></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
    scoutln(out, bytesOut, "<a href=\"/central/servlet/AdminDataBaseBuilderImport?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men="
                           + men + "&den=" + den + "&dnm=" + dnm + "&p1=rvoucherl&bnm=" + bnm + "\">Import</a> Receipt Voucher Lines</td>");
    scoutln(out, bytesOut, "<td nowrap width=90%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                         + "<a href=\"javascript:help('7207')\">(Service 7207)</a></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
    scoutln(out, bytesOut, "<a href=\"/central/servlet/AdminDataBaseBuilderImport?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men="
                           + men + "&den=" + den + "&dnm=" + dnm + "&p1=rvoucherll&bnm=" + bnm + "\">Import</a> Receipt Voucher Multiple Lines</td>");
    scoutln(out, bytesOut, "<td nowrap width=90%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                         + "<a href=\"javascript:help('7207')\">(Service 7207)</a></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
    scoutln(out, bytesOut, "<a href=\"/central/servlet/AdminDataBaseBuilderImport?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men="
                           + men + "&den=" + den + "&dnm=" + dnm + "&p1=acctdefn&bnm=" + bnm + "\">Import</a> Accounts Definitions</td>");
    scoutln(out, bytesOut, "<td nowrap width=90%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                         + "<a href=\"javascript:help('7207')\">(Service 7207)</a></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
    scoutln(out, bytesOut, "<a href=\"/central/servlet/AdminDataBaseBuilderImport?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men="
                           + men + "&den=" + den + "&dnm=" + dnm + "&p1=extuser&bnm=" + bnm + "\">Import</a> Business Contacts</td>");
    scoutln(out, bytesOut, "<td nowrap width=90%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                         + "<a href=\"javascript:help('7207')\">(Service 7207)</a></span></td></tr>");
    
    scoutln(out, bytesOut, "</table></body></html>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
