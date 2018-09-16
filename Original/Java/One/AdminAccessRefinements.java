// =======================================================================================================================================================================================================
// System: ZaraStar Admin: Access refinements
// Module: AdminAccessRefinements.java
// Author: C.K.Harvey
// Copyright (c) 2003-09 Christopher Harvey. All Rights Reserved.
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

public class AdminAccessRefinements extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
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

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="", p3="", p4="";

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
      p1  = req.getParameter("p1"); // allServices
      p2  = req.getParameter("p2"); // desc
      p3  = req.getParameter("p3"); // callingService
      p4  = req.getParameter("p4"); // userCode

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, p3, p4, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "AdminAccessRefinements", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7508, bytesOut[0], 0, "ERR:" + p1 + ":" + p4);
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1, String p2, String p3, String p4, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if((! serverUtils.isSysAdmin(con, stmt, rs, unm, dnm, sid, uty, localDefnsDir, defnsDir)) && (! serverUtils.isDBAdmin(con, stmt, rs, unm)))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "AdminAccessRefinements", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7508, bytesOut[0], 0, "ACC:" + p1 + ":" + p4);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "AdminAccessRefinements", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7508, bytesOut[0], 0, "SID:" + p1 + ":" + p4);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    create(con, stmt, rs, out, p1, p2, p3, p4, unm, sid, uty, men, den, dnm, bnm, imagesDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 7508, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1 + ":" + p4);
    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void create(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String allServices, String desc, String callingService, String userCode, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                      String imagesDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Admin - Access Refinement</title>");

    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    scoutln(out, bytesOut, "function checkAll(){");
    scoutln(out, bytesOut, "var e,x;for(x=0;x<document.forms[0].length;++x)");
    scoutln(out, bytesOut, "{e=document.forms[0].elements[x];");
    scoutln(out, bytesOut, "if(e.type=='checkbox' && e.name != 'all')");
    scoutln(out, bytesOut, "if(e.checked)");
    scoutln(out, bytesOut, "e.checked=false;");
    scoutln(out, bytesOut, "else e.checked=true;}}");

    scoutln(out, bytesOut, "function update(){document.users.submit();}");

    scoutln(out, bytesOut, "</script>");

    scoutln(out, bytesOut, "</head><body>");

    signOnAdministrator.heading(out, true, unm, sid, uty, bnm, dnm, men, den, imagesDir, bytesOut);

    dashboardUtils.drawTitle(out, "Access Refinement for " + userCode + ": " + desc, "7508", unm, sid, uty, men, den, dnm, bnm, bytesOut);

    scoutln(out, bytesOut, "<form action=\"AdminServicesRefinementUpdate\" name=users ENCTYPE=\"application/x-www-form-urlencoded\" method=POST>");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=unm value=" + unm + ">");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=sid value=" + sid + ">");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=uty value=" + uty + ">");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=men value=" + men + ">");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=den value=" + den + ">");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=dnm value=" + dnm + ">");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=bnm value=" + bnm + ">");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=userCode value=" + userCode + ">");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=callingService value=" + callingService + ">");

    scoutln(out, bytesOut, "<table id=\"page\" border=0 width=100% cellpadding=3 cellspacing=3>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr id=\"pageColumn\"><td><p><input type=checkbox name=all onClick=\"checkAll()\"></td>");
    scoutln(out, bytesOut, "<td nowrap width=99%><p>Option &nbsp;</td></tr>");

    String[] cssFormat = new String[1];  cssFormat[0] = "";

    String svc, name, svcs = "";
    boolean first = true;
    int x = 0, len = allServices.length();
    while(x < len)
    {
      svc = "";
      while(allServices.charAt(x) != ',')
        svc += allServices.charAt(x++);
      ++x;
      name = "";
      while(x < len && allServices.charAt(x) != ',')
        name += allServices.charAt(x++);
      ++x;

      refine(con, stmt, rs, out, userCode, svc, name, cssFormat, bytesOut);

      if(! first)
        svcs += ",";
      else first = false;

      svcs += svc;
    }

    scoutln(out, bytesOut, "<tr><td><INPUT TYPE=hidden name=services value='" + svcs + "'></td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=3><hr></td></tr>");

    scoutln(out, bytesOut, "<tr><td colspan=3><span id=\"option\"><p><a href=\"javascript:update()\">Update</a> User Records for " + userCode + "</span></td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "</table></form></body></html>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void refine(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String userCode, String service, String option, String[] cssFormat, int[] bytesOut) throws Exception
  {
    if(cssFormat[0].equals("line2")) cssFormat[0] = "line1"; else cssFormat[0] = "line2";

    scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\">");

    scoutln(out, bytesOut, "<td><input type=checkbox name='" + service + "'");
    if(alreadyHasAccess(con, stmt, rs, service, userCode))
      scoutln(out, bytesOut, " checked");
    scoutln(out, bytesOut, "></td>");
    scoutln(out, bytesOut, "<td><p>" + option + "</td></tr>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean alreadyHasAccess(Connection con, Statement stmt, ResultSet rs, String service, String userCode) throws Exception
  {
    int rowcount = 0;

    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM userservices WHERE Service = '" + service + "' AND UserCode = '" + userCode + "'");

      if(rs.next())
        rowcount = rs.getInt("rowcount");

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }

    if(rowcount == 0)
      return false;
    return true;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {
    out.println(str);
    bytesOut[0] += (str.length() + 2);
  }

}
