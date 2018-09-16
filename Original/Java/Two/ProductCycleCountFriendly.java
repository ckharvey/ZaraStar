// =======================================================================================================================================================================================================
// System: ZaraStar: Product: Friendly print of cycle count outstanding items
// Module: ProductCycleCountFriendly.java
// Author: C.K.Harvey
// Copyright (c) 1999-2009 Christopher Harvey. All Rights Reserved.
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

public class ProductCycleCountFriendly extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  DashboardUtils dashboardUtils = new DashboardUtils();
  Inventory inventory = new Inventory();
  InventoryAdjustment inventoryAdjustment = new InventoryAdjustment();
  DocumentUtils documentUtils = new DocumentUtils();
  DefinitionTables definitionTables = new DefinitionTables();

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
      sid = req.getParameter("sid");
      uty = req.getParameter("uty");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");
      p1  = req.getParameter("p1"); // dateTo

      if(p1 == null) p1 = "";

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "ProductCycleCountFriendly", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3083, bytesOut[0], 0, "ERR:" + p1);
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
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 3083, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "3083e", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3083, bytesOut[0], 0, "ACC:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "3083e", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3083, bytesOut[0], 0, "SID:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    set(con, stmt, rs, out, req, p1, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 3083, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String dateTo, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir,
                   int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Cycle Count Print</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    outputPageFrame(con, stmt, rs, out, req, "", "", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, bytesOut);

    // determine year from today's date
    String today = generalUtils.todaySQLFormat(localDefnsDir, defnsDir);

    if(dateTo.length() == 0)
      dateTo = today;

    scoutln(out, bytesOut, "<table id='page' border=1 cellspacing=0 cellpadding=2><tr id='pageColumn'><td><p>Date</td>");
    scoutln(out, bytesOut, "<td><p>Check Code &nbsp;</td>");
    scoutln(out, bytesOut, "<td><p>Item Code &nbsp;</td>");
    scoutln(out, bytesOut, "<td><p>Manufacturer &nbsp;</td>");
    scoutln(out, bytesOut, "<td><p>Manufacturer Code &nbsp;</td>");

    scoutln(out, bytesOut, "<td><p>Counted Level &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Location &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Date Counted &nbsp;</td>");

    scoutln(out, bytesOut, "<td nowrap><p>Description &nbsp;</td></tr>");

    int numrecs = showRecs(con, stmt, rs, out, dateTo, bytesOut);

    scoutln(out, bytesOut, "</table>");

    scoutln(out, bytesOut, "<table id='page' border=0 cellspacing=0 cellpadding=2>");
    scoutln(out, bytesOut, "<tr><td><p>Total of " + numrecs + " Records</td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir));
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int showRecs(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String dateTo, int[] bytesOut) throws Exception
  {
    int count = 0;
    String[] mfr     = new String[1];
    String[] mfrCode = new String[1];
    String[] desc    = new String[1];

    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT CheckCode, ItemCode, Date FROM stockc WHERE Status != 'C' AND Level = '999999' AND Date <= {d '" + dateTo + "'} ORDER BY Date, CheckCode");

      String checkCode, itemCode, date;

      while(rs.next())
      {
        checkCode = rs.getString(1);
        itemCode  = rs.getString(2);
        date      = rs.getString(3);

        getItemDetailsGivenCode(con, stmt, rs, itemCode, mfr, mfrCode, desc);

        scoutln(out, bytesOut, "<tr>");

        scoutln(out, bytesOut, "<td><p>" + generalUtils.convertFromYYYYMMDD(date) + "</td>");
        scoutln(out, bytesOut, "<td><p>" + checkCode + "</td>");
        scoutln(out, bytesOut, "<td><p>" + itemCode + "</td>");
        scoutln(out, bytesOut, "<td><p>" + mfr[0] + "</td>");

        if(mfrCode[0].length() > 0)
          scoutln(out, bytesOut, "<td><p>" + mfrCode[0] + "</td>");
        else scoutln(out, bytesOut, "<td><p>&nbsp;</td>");

        scoutln(out, bytesOut, "<td><p>&nbsp;</td>");
        scoutln(out, bytesOut, "<td><p>&nbsp;</td>");
        scoutln(out, bytesOut, "<td><p>&nbsp;</td>");

        if(desc[0].length() > 0)
          scoutln(out, bytesOut, "<td nowrap><p>" + desc[0] + "</td>");
        else scoutln(out, bytesOut, "<td><p>&nbsp;</td>");

        ++count;
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

    return count;
  }

  //--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void getItemDetailsGivenCode(Connection con, Statement stmt, ResultSet rs, String itemCode, String[] mfr, String[] mfrCode, String[] desc) throws Exception
  {
    byte[] data = new byte[5000];

    if(inventory.getStockRecGivenCode(con, stmt, rs, itemCode, '\000', data) < 0) // just-in-case
    {
      mfr[0] = mfrCode[0] = desc[0] = "";
      return;
   }

    desc[0]    = generalUtils.dfsAsStr(data, (short)1);
    mfr[0]     = generalUtils.dfsAsStr(data, (short)3);
    mfrCode[0] = generalUtils.dfsAsStr(data, (short)4);

    if(desc[0]    == null) desc[0] = "";
    if(mfr[0]     == null) mfr[0] = "";
    if(mfrCode[0] == null) mfrCode[0] = "";

  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {
    out.println(str);
    bytesOut[0] += (str.length() + 2);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void outputPageFrame(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String bodyStr, String title, String unm, String sid, String uty, String men, String den,
                               String dnm, String bnm, String otherSetup, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    int[] hmenuCount = new int[1];

    String subMenuText = dashboardUtils.buildSubMenuText(con, stmt, rs, req, "3083", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount);

    subMenuText += "<table id='title' width=100%><tr><td colspan=2 nowrap><p>" + title + directoryUtils.buildHelp(3083) + "</td></tr></table>";

    scoutln(out, bytesOut, authenticationUtils.buildPlainScreen(con, stmt, rs, req, unm, sid, uty, men, den, dnm, bnm, "ProductCycleCountFriendly", subMenuText, hmenuCount[0], bodyStr, otherSetup, localDefnsDir, defnsDir));
  }

}
