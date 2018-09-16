// =======================================================================================================================================================================================================
// System: ZaraStar Product: Catalogs from admin screen
// Module: ProductCatalogsAdmin.java
// Author: C.K.Harvey
// Copyright (c) 2002-08 Christopher Harvey. All Rights Reserved.
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

public class ProductCatalogsAdmin extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  Profile profile = new Profile();
  
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
 
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "ProductCatalogsAdmin", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2003, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                    int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    
    Statement stmt = null;
    ResultSet rs   = null;
    
    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 2003, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "ProductCatalogsAdmin", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2003, bytesOut[0], 0, "ACC:");
      
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "ProductCatalogsAdmin", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2003, bytesOut[0], 0, "SID:");
      
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    set(con, stmt, rs, out, req, unm, sid, uty, men, den, dnm, bnm, imagesDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 2003, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
        
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty,
                   String men, String den, String dnm, String bnm, String imagesDir, String localDefnsDir, String defnsDir, int[] bytesOut)
                   throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Catalogs Page</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    scoutln(out, bytesOut, "function mfr(which){");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/CatalogUtils?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+escape(which);}");

    scoutln(out, bytesOut, "function mfr2(which){");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/PrincipalServices4a?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+escape(which);}");

    scoutln(out, bytesOut, "function mfr3(which){");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/CatalogFetchLinked?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+escape(which);}");

    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\"></head>");
  
    int[] hmenuCount = new int[1];
 
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "2003", "", "ProductCatalogsAdmin", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount,
                          bytesOut);
   
    if(uty.equals("R"))
    {
      String[] name         = new String[1];
      String[] companyName  = new String[1];
      String[] accessRights = new String[1];
      int i = unm.indexOf("_");

      profile.getExternalAccessNameCompanyAndRights(con, stmt, rs, unm.substring(0, i), name, companyName, accessRights);

      pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Catalog Services for " + name[0] + " of " + companyName[0], "2003", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);
    }
    else
    if(uty.equals("I"))
      pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Catalog Services", "2003", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);
    else pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Catalog Services (Casual User)", "2003", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<form><table id=\"page\" width=100% border=0>");

    scoutln(out, bytesOut, "<tr><td nowrap colspan=3><h1>Select a Manufacturer</td></tr>");
    
    listMfrs(con, stmt, rs, out, unm, bytesOut);

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
  
    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir));
  }
    
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void listMfrs(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String unm, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT DISTINCT Manufacturer FROM stockcat ORDER BY Manufacturer");

    String mfr;
    int count = -1;
    scout(out, bytesOut, "<tr>");
    
    while(rs.next())
    {
      if(++count == 3)
      {
        scout(out, bytesOut, "</tr><tr>");
        count = 0;
      }
      
      mfr = rs.getString(1);
      
      scoutln(out, bytesOut, "<td nowrap width=33%><p><a href=\"javascript:mfr('" + mfr + "')\">" + mfr + "</a></td>");
    }
    
    scout(out, bytesOut, "</tr>");

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT DISTINCT Manufacturer FROM catalog ORDER BY Manufacturer");

    count = -1;
    scout(out, bytesOut, "<tr>");
    
    while(rs.next())
    {
      if(++count == 3)
      {
        scout(out, bytesOut, "</tr><tr>");
        count = 0;
      }
      
      mfr = rs.getString(1);
      
      scoutln(out, bytesOut, "<td nowrap width=33%><p><a href=\"javascript:mfr2('" + mfr + "')\">" + mfr + "</a></td>");
    }
    
    scout(out, bytesOut, "</tr>");
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    getAnyLinkedCatalogs(con, stmt, rs, out, bytesOut);
  }    

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void getAnyLinkedCatalogs(Connection con, Statement stmt, ResultSet rs, PrintWriter out, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT Manufacturer, Description FROM linkedcat ORDER BY Description");
    
    int count = -1;
    scout(out, bytesOut, "<tr>");
    
    while(rs.next())
    {
      if(++count == 3)
      {
        scout(out, bytesOut, "</tr><tr>");
        count = 0;
      }
      
      scoutln(out, bytesOut, "<td nowrap width=33%><p><a href=\"javascript:mfr3('" + rs.getString(1) + "')\">" + rs.getString(2) + "</a></td>");
    }
    
    scout(out, bytesOut, "</tr>");
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void scout(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.print(str);
    bytesOut[0] += str.length();    
  }
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }
  
}
