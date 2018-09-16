// =======================================================================================================================================================================================================
// System: ZaraStar Admin: Control
// Module: AdminControl.java
// Author: C.K.Harvey
// Copyright (c) 2003-09 Christopher Harvey. All Rights Reserved.
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

public class AdminControlWave extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  DashboardUtils dashboardUtils = new DashboardUtils();

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
      directoryUtils.setContentHeaders2(res);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "AdminControl", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7500, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');

    String uName = directoryUtils.getMySQLUserName();
    String pWord = directoryUtils.getMySQLPassWord();
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Connection conAdmin = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_admin?user=" + uName + "&password=" + pWord);
    Statement stmt = null;
    ResultSet rs   = null;

    if((! serverUtils.isSysAdmin(con, stmt, rs, unm, dnm, sid, uty, localDefnsDir, defnsDir)) && (! serverUtils.isDBAdmin(con, stmt, rs, unm)))
    {
      messagePage.msgScreenW(out, req, 13, unm, sid, uty, men, den, dnm, bnm, "AdminControl", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7500, bytesOut[0], 0, "ACC:");
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreenW(out, req, 12, unm, sid, uty, men, den, dnm, bnm, "AdminControl", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7500, bytesOut[0], 0, "SID:");
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    scoutln(out, bytesOut, "7500\001Admin\001System Types\001javascript:getHTML('AdminControlWave','')\001\001\001\001\003");

    create(con, conAdmin, stmt, rs, out, req, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 7500, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(conAdmin != null) conAdmin.close();
    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void create(Connection con, Connection conAdmin, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir,
                      int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    scoutln(out, bytesOut, "function modify(svc,desc){var p2=sanitise(desc);getHTML('AdminAccessUpdateWave','&p3=7500&p2='+p2+'&p1='+svc);}");

    scoutln(out, bytesOut, "function suspend(svc){getHTML('AdminSuspendUpdateWave','&p2=7500&p1='+svc);}");

    scoutln(out, bytesOut, "function setup(type,mode){if(type=='Operations')getHTML('AdminSetupOperations','&p1='+mode);");
    scoutln(out, bytesOut, "else if(type=='Documents')getHTML('AdminSetupDocuments','&p1='+mode);");
    scoutln(out, bytesOut, "else if(type=='Mail')getHTML('AdminSetupMail','&p1='+mode);");
    scoutln(out, bytesOut, "else if(type=='Blogs')getHTML('AdminSetupBlogs','&p1='+mode);");
    scoutln(out, bytesOut, "else if(type=='Catalogs')getHTML('AdminSetupCatalogs','&p1='+mode);");
    scoutln(out, bytesOut, "else if(type=='Waves')getHTML('AdminSetupWaves','&p1='+mode);");
    scoutln(out, bytesOut, "else if(type=='Manager')getHTML('AdminSetupManager','&p1='+mode);}");

    scoutln(out, bytesOut, "function select(type,mode){getHTML('AdminSelectDeselectSysType','&p1='+type+'&p2='+mode);}");

    scoutln(out, bytesOut, "</script>");

    dashboardUtils.drawTitleW(out, "Administration - System Types", "7500", unm, sid, uty, men, den, dnm, bnm, bytesOut);

    String[] active = new String[1];
    String[] setup  = new String[1];

    scoutln(out, bytesOut, "<table id=\"page\" border=0>");

    scoutln(out, bytesOut, "<tr><td nowrap colspan=4><h1>Operations</td></tr>");
    scoutln(out, bytesOut, "<tr><td></td><td colspan=3><p>Comprising: orders, fulfillment, settlement, accounts. Documents: Quotations, Sales Orders, Sales Order Confirmations, Picking Lists, Packing Lists, Delivery Orders, Proforma Invoices, "
                         + "Sales Invoices, Purchase Orders, Local Requisitions, Goods Received Notes, Purchase Invoices, Stock Adjustments, Sales Credit Notes, Sales Debit Notes, Purchase Credit Notes, Purchase Debit Notes, Receipts, Payments, "
                         + "Receipt Vouchers, Payment Vouchers, Works Orders.</td></tr>");

    getStateForSysType(conAdmin, stmt, rs, "Operations", active, setup);
    showStateForSysType(out, "Operations", active[0], setup[0], bytesOut);

    scoutln(out, bytesOut, "<tr><td nowrap colspan=4><h1>Documents</td></tr>");
    scoutln(out, bytesOut, "<tr><td></td><td><p></td></tr>");

    getStateForSysType(conAdmin, stmt, rs, "Documents", active, setup);
    showStateForSysType(out, "Documents", active[0], setup[0], bytesOut);

    scoutln(out, bytesOut, "<tr><td nowrap colspan=4><h1>Mail</td></tr>");
    scoutln(out, bytesOut, "<tr><td></td><td><p></td></tr>");

    getStateForSysType(conAdmin, stmt, rs, "Mail", active, setup);
    showStateForSysType(out, "Mail", active[0], setup[0], bytesOut);

    scoutln(out, bytesOut, "<tr><td nowrap colspan=4><h1>Blogs</td></tr>");
    scoutln(out, bytesOut, "<tr><td></td><td><p></td></tr>");

    getStateForSysType(conAdmin, stmt, rs, "Blogs", active, setup);
    showStateForSysType(out, "Blogs", active[0], setup[0], bytesOut);

    scoutln(out, bytesOut, "<tr><td nowrap colspan=4><h1>Catalogs</td></tr>");
    scoutln(out, bytesOut, "<tr><td></td><td><p></td></tr>");

    getStateForSysType(conAdmin, stmt, rs, "Catalogs", active, setup);
    showStateForSysType(out, "Catalogs", active[0], setup[0], bytesOut);

    scoutln(out, bytesOut, "<tr><td nowrap colspan=4><h1>Waves</td></tr>");
    scoutln(out, bytesOut, "<tr><td></td><td><p></td></tr>");

    getStateForSysType(conAdmin, stmt, rs, "Waves", active, setup);
    showStateForSysType(out, "Waves", active[0], setup[0], bytesOut);

    scoutln(out, bytesOut, "<tr><td nowrap colspan=4><h1>Manager</td></tr>");
    scoutln(out, bytesOut, "<tr><td></td><td><p></td></tr>");

    getStateForSysType(conAdmin, stmt, rs, "Manager", active, setup);
    showStateForSysType(out, "Manager", active[0], setup[0], bytesOut);

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "</table>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void getStateForSysType(Connection con, Statement stmt, ResultSet rs, String sysType, String[] active, String[] setup) throws Exception
  {
    active[0] = "N";
    setup[0]  = "N";

    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT Active, SetupDone FROM SysTypes WHERE Type = '" + sysType + "'");

      if(rs.next())
      {
        active[0] = rs.getString(1);
        setup[0]  = rs.getString(2);
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

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void showStateForSysType(PrintWriter out, String sysType, String active, String setup, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<tr><td></td><td><p>");
    if(active.equals("Y"))
    {
      scoutln(out, bytesOut, "Currently Active</td><td><p><a href=\"javascript:select('" + sysType + "','D')\">DeSelect</a></td>");

      if(setup.equals("Y"))
        scoutln(out, bytesOut, "<td><p><a href=\"javascript:setup('" + sysType + "','M')\">Modify Setup</a></td>");
      else scoutln(out, bytesOut, "<td><p><a href=\"javascript:setup('" + sysType + "','R')\">Run Setup</a></td>");
    }
    else
    {
      scoutln(out, bytesOut, "Not Active</td><td><p><a href=\"javascript:select('" + sysType + "','S')\">Select</a></td>");

      if(setup.equals("Y"))
        scoutln(out, bytesOut, "<td><p><a href=\"javascript:setup('" + sysType + "','M')\">Modify Setup</a></td>");
      else scoutln(out, bytesOut, "<td><p><a href=\"javascript:setup('" + sysType + "','R')\">Run Setup</a></td>");
    }

    scoutln(out, bytesOut, "</tr>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {
    out.println(str);
    bytesOut[0] += (str.length() + 2);
  }

}
