// =======================================================================================================================================================================================================
// System: ZaraStar: Product: Display mfr stcok check 'trust'
// Module: StockCheckTrust.java
// Author: C.K.Harvey
// Copyright (c) 2000-09 Christopher Harvey. All Rights Reserved.
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

public class StockCheckTrust extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  InventoryAdjustment inventoryAdjustment = new InventoryAdjustment();

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
      p1  = req.getParameter("p1"); // mfr

      if(p1 == null) p1 = "";

      doIt(out, req, p1, unm, sid, uty, men, den, dnm, bnm, bytesOut);
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

      System.out.println("3082: " + e);
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "StockCheckTrust", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3082, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush(); 
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String p1, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null, stmt2 = null, stmt3 = null;
    ResultSet rs   = null, rs2   = null, rs3   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 3082, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "StockCheckTrust", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3082, bytesOut[0], 0, "ACC:");
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "StockCheckTrust", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3082, bytesOut[0], 0, "SID:");
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    set(con, stmt, stmt2, stmt3, rs, rs2, rs3, out, req, p1, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 3082, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, ResultSet rs3, PrintWriter out, HttpServletRequest req, String mfr, String unm, String sid, String uty, String men, String den,
                   String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Manufacturer-Count Trust</title>");

    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    int[] hmenuCount = new int[1];
 
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "3082", "", "StockCheckTrust", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    if(mfr.length() > 0)
      pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Manufacturer-Count Trust for " + mfr, "3082", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);
    else pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Manufacturer-Count Trust", "3082", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<p>High: if > 80 checked within the last 90 days. Low: if > 50% not checked for 180 daya (at least). Medium: Otherwise.");

    scoutln(out, bytesOut, "<table id=\"page\" border=0 cellpadding=3 cellspacing=3>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr id=\"pageColumn\">");
    scoutln(out, bytesOut, "<td nowrap><p>Manufacturer &nbsp;</td>");
    scoutln(out, bytesOut, "<td><p>Trust Level &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap align=center><p>% checked within 30 days</td>");
    scoutln(out, bytesOut, "<td nowrap align=center><p>% checked between 30 and 90 days</td>");
    scoutln(out, bytesOut, "<td nowrap align=center><p>% checked between 90 and 180 days</td>");
    scoutln(out, bytesOut, "<td nowrap align=center><p>% not checked for more than 180 days</td>");
    scoutln(out, bytesOut, "<td nowrap align=center><p>% never checked</td></tr>");

    int todayEncoded = generalUtils.todayEncoded(localDefnsDir, defnsDir);

    if(mfr.length() > 0)
      forAMfr(con, stmt, stmt2, rs, rs2, out, mfr, todayEncoded, bytesOut);
    else forAllMfrs(con, stmt, stmt2, stmt3, rs, rs2, rs3, out, todayEncoded, bytesOut);

    scoutln(out, bytesOut, "</table>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir));
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void forAMfr(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, String mfr, int todayEncoded, int[] bytesOut) throws Exception
  {
    int[] within30  = new int[1];
    int[] within90  = new int[1];
    int[] within180 = new int[1];
    int[] after180  = new int[1];
    int[] never     = new int[1];
    int[] total     = new int[1];
    String level, colour;
    
    level = determineTrustLevel(con, stmt, stmt2, rs, rs2, mfr, todayEncoded, within30, within90, within180, after180, never, total);

    if(level.equals("High"))
      colour = "green";
    else
    if(level.equals("Medium"))
      colour = "orange";
    else colour = "red";

    scoutln(out, bytesOut, "<tr><td><p><font color='" + colour + "'>" + mfr + "</td><td><p><font color='" + colour + "'>" + level + "</td><td align=center><p><font color='" + colour + "'>" + ((within30[0] / total[0]) * 100)
                         + "</td><td align=center><p><font color='" + colour + "'>" + ((within90[0] / total[0]) * 100) + "</td><td align=center><p><font color='" + colour + "'>" + ((within180[0] / total[0]) * 100)
                         + "</td><td align=center><p><font color='" + colour + "'>" + ((after180[0] / total[0]) * 100) + "</td><td align=center><p><font color='" + colour + "'>" + ((never[0] / total[0]) * 100) + "</td></tr>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void forAllMfrs(Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, ResultSet rs3, PrintWriter out, int todayEncoded, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT DISTINCT Manufacturer FROM stock ORDER BY Manufacturer");

    String mfr, level, cssFormat = "", colour;
    int[] within30  = new int[1];
    int[] within90  = new int[1];
    int[] within180 = new int[1];
    int[] after180  = new int[1];
    int[] never     = new int[1];
    int[] total     = new int[1];

    while(rs.next())
    {
      mfr = rs.getString(1);

      level = determineTrustLevel(con, stmt2, stmt3, rs2, rs3, mfr, todayEncoded, within30, within90, within180, after180, never, total);

      if(cssFormat.equals("line2")) cssFormat = "line1"; else cssFormat = "line2";

      if(level.equals("High"))
        colour = "green";
      else
      if(level.equals("Medium"))
        colour = "orange";
      else colour = "red";

      scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\"><td><p><font color='" + colour + "'>" + mfr + "</td><td><p><font color='" + colour + "'>" + level + "</td><td align=center><p><font color='" + colour + "'>"
                           + ((within30[0] / total[0]) * 100) + "</td><td align=center><p><font color='" + colour + "'>" + ((within90[0] / total[0]) * 100) + "</td><td align=center><p><font color='" + colour + "'>"
                           + ((within180[0] / total[0]) * 100) + "</td><td align=center><p><font color='" + colour + "'>" + ((after180[0] / total[0]) * 100)
                           + "</td><td align=center><p><font color='" + colour + "'>" + ((never[0] / total[0]) * 100) + "</td></tr>");
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String determineTrustLevel(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String mfr, int todayEncoded, int[] within30, int[] within90, int[] within180, int[] after180, int[] never, int[] total)
                                     throws Exception
  {
    String level = "Unknown";
    total[0] = within30[0] = within90[0] = within180[0] = after180[0] = 0;
    
    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT ItemCode FROM stock WHERE Manufacturer = '" + generalUtils.sanitiseForSQL(mfr) + "'");

      String itemCode;
      int dateEncoded;
      boolean found;

      while(rs.next())
      {
        itemCode = rs.getString(1);

        stmt2 = con.createStatement();

        rs2 = stmt2.executeQuery("SELECT Date FROM stockc WHERE ItemCode = '" + generalUtils.sanitiseForSQL(itemCode) + "' AND Level != '999999'");

        found = false;

        while(rs2.next())
        {
          dateEncoded = generalUtils.encodeFromYYYYMMDD(rs2.getString(1));

          if(dateEncoded > (todayEncoded - 30)) // or greater than today!
            ++within30[0];
          else
          if(dateEncoded > (todayEncoded - 90))
            ++within90[0];
          else
          if(dateEncoded > (todayEncoded - 180))
            ++within180[0];
          else ++after180[0];

          found = true;
        }

        if(rs2   != null) rs2.close();
        if(stmt2 != null) stmt2.close();

        if(! found)
          ++never[0];
      }

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();

      total[0] = within30[0] + within90[0] + within180[0] + after180[0] + never[0];

      // low if > 50% after180
      // high if > 80% within90
      // medium otherwise

      if(total[0] == 0) total[0] = 1;
      
      if((after180[0] / total[0]) > 0.5)
        level = "Low";
      else
      if((within90[0] / total[0]) > 0.8)
        level = "High";
      else level = "Medium";
    }
    catch(Exception e)
    {
      System.out.println("3082: " + e);
      if(rs2   != null) rs2.close();
      if(stmt2 != null) stmt2.close();
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }

    return level;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {
    out.println(str);
    bytesOut[0] += (str.length() + 2);
  }

}
