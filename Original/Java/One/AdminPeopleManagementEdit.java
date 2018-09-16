// =======================================================================================================================================================================================================
// System: ZaraStar Admin: Edit manage people
// Module: AdminPeopleManagementEdit.java
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

public class AdminPeopleManagementEdit extends HttpServlet
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

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="";

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
      p1  = req.getParameter("p1"); // userCode

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "AdminPeopleManagementEdit", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7053, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null, stmt2 = null;
    ResultSet rs   = null, rs2   = null;

    if((! serverUtils.isSysAdmin(con, stmt, rs, unm, dnm, sid, uty, localDefnsDir, defnsDir)) && (! serverUtils.isDBAdmin(con, stmt, rs, unm)))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "AdminPeopleManagementCreate", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7053, bytesOut[0], 0, "ACC:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "AdminPeopleManagementCreate", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7053, bytesOut[0], 0, "SID:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    set(con, stmt, stmt2, rs, rs2, out, p1, unm, sid, uty, men, den, dnm, bnm, defnsDir, imagesDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 7053, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, String userCode, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String defnsDir,
                   String imagesDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Manage People</title>");

    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    scoutln(out, bytesOut, "function update(){document.users.submit();}");

    scoutln(out, bytesOut, "function checkAll(){");
    scoutln(out, bytesOut, "var e,x;for(x=0;x<document.forms[0].length;++x)");
    scoutln(out, bytesOut, "{e=document.forms[0].elements[x];");
    scoutln(out, bytesOut, "if(e.type=='checkbox' && e.name != 'all')");
    scoutln(out, bytesOut, "if(e.checked)");
    scoutln(out, bytesOut, "e.checked=false;");
    scoutln(out, bytesOut, "else e.checked=true;}}");

    scoutln(out, bytesOut, "</script>");
    scoutln(out, bytesOut, "</head><body>");

    scoutln(out, bytesOut, "<form action=\"AdminPeopleManagementUpdate\" name=users ENCTYPE=\"application/x-www-form-urlencoded\" method=POST>");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=unm value=" + unm + ">");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=sid value=" + sid + ">");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=uty value=" + uty + ">");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=men value=" + men + ">");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=den value=" + den + ">");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=dnm value=" + dnm + ">");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=bnm value=" + bnm + ">");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=userCode value=" + userCode + ">");

    signOnAdministrator.heading(out, true, unm, sid, uty, bnm, dnm, men, den, imagesDir, bytesOut);

    dashboardUtils.drawTitle(out, "Manage SalesPeople for: " + userCode, "7053", unm, sid, uty, men, den, dnm, bnm, bytesOut);

    scoutln(out, bytesOut, "<table id=\"page\" border=0 width=100% cellpadding=3 cellspacing=3>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr id=\"pageColumn\"><td><p><input type=checkbox name=all onClick=\"checkAll()\"></td>");
    scoutln(out, bytesOut, "<td nowrap width=99%><p>User Name &nbsp;</td></tr>");

    list(con, stmt, stmt2, rs, rs2, out, userCode, bytesOut);

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=3><hr></td></tr>");

    scoutln(out, bytesOut, "<tr><td colspan=3><span id=\"option\"><p><a href=\"javascript:update()\">Update</a> Records for " + userCode + "</span></td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "</table></form></body></html>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void list(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, String reqdUserCode, int[] bytesOut) throws Exception
  {
    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT Name FROM salesper ORDER BY Name");

      String name, cssFormat = "";

      while(rs.next())
      {
        name = rs.getString(1);
       
        if(cssFormat.equals("line2")) cssFormat = "line1"; else cssFormat = "line2";

        scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\">");

        scoutln(out, bytesOut, "<td><input type=checkbox name='" + name + "'");
        if(already(con, stmt2, rs2, reqdUserCode, name))
          scoutln(out, bytesOut, " checked");
        scoutln(out, bytesOut, "></td><td><p>" + name + "</td></tr>");
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
  private boolean already(Connection con, Statement stmt, ResultSet rs, String reqdUserCode, String userCode) throws Exception
  {
    int rowcount = 0;

    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM managepeople WHERE UserCode = '" + generalUtils.sanitiseForSQL(reqdUserCode) + "' AND SalesPerson = '" + generalUtils.sanitiseForSQL(userCode) + "'");

      if(rs.next())
        rowcount = rs.getInt(1);

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
