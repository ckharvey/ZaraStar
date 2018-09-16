// =======================================================================================================================================================================================================
// System: ZaraStar Product: Stock ReOrder
// Module: StockReorderGenerate.java
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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class StockReorderGenerate extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "StockReorderGenerate", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1021, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                    int[] bytesOut) throws Exception
   {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;
    
    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 1021, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "1021a", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1021, bytesOut[0], 0, "ACC:");
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "1021a", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1021, bytesOut[0], 0, "SID:");
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    set(con, stmt, rs, out, req, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 1021, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut)
                   throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Stock ReOrder</title></head>");

    scoutln(out, bytesOut, "<script javascript=\"JavaScript\">");

    scoutln(out, bytesOut, "function generate(option){document.forms[0].which.value=option;document.go.submit();}");

    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\""
                           + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\"></head>");
    
    scoutln(out, bytesOut, "<form name=\"go\" action=\"StockReorderScan\" enctype=\"application/x-www-form-urlencoded\" method=POST>");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=unm value=" + unm + ">");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=sid value=" + sid + ">");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=uty value=" + uty + ">");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=men value=\"" + men + "\">");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=den value=" + den + ">");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=dnm value=" + dnm + ">");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=bnm value=" + bnm + ">");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=which value=''>");

    int[] hmenuCount = new int[1];
    
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "1021", "", "StockReorderInput", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
                                
    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Stock ReOrder Generation", "1021", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<table id=\"page\" width=100%>");

    scoutln(out, bytesOut, "<tr><td colspan=2><p><br>Stock re-ordering can be performed from two perspectives:<br><br>");
    scoutln(out, bytesOut, "1. <b>Back-to-back ordering</b>; where you only place an order for items that are listed on existing, outstanding, sales orders.<br><br>");
    scoutln(out, bytesOut, "In this case, if the current stock level for an item is below sales order requirements, that item is selected."
                         + " Any outstanding purchase order levels are used to lower the suggested re-order level.</td><tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td colspan=2><p>2. <b>Forecast ordering</b>; where you order based upon what you have sold in the past.<br><br>");
    scoutln(out, bytesOut, "In this case, if the current stock level for an item is below a forecast stock level, that item is selected. "
                         + "Any current outstanding sales order requirements are added to the suggested re-order level. Any outstanding purchase order requirements are "
                         + "subtracted from the suggested re-order level.</td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=2><p><i><b>Use one of the following two options:</b></i></p></td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    
    scoutln(out, bytesOut, "<tr><td colspan=2><hr></td></tr>");
    scoutln(out, bytesOut, "<tr><td><p>1. Back-to-Back Ordering.</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap><p>Manufacturer &nbsp;</td><td width=99%>");
    getMfrsDDL(out, "1", dnm, localDefnsDir, defnsDir, bytesOut);
    scoutln(out, bytesOut, "</td></tr>");
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td><p><a href=\"javascript:generate(1)\">Scan Stock File</a> for Back-to-Back Ordering</td></tr>");

    scoutln(out, bytesOut, "<tr><td colspan=2><hr></td></tr>");
    
    scoutln(out, bytesOut, "<tr><td><p>2. Forecast Ordering.</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap><p>Manufacturer &nbsp;</td><td width=99%>");
    getMfrsDDL(out, "2", dnm, localDefnsDir, defnsDir, bytesOut);
    scoutln(out, bytesOut, "</td></tr>");
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap><p>Calculate quantity sold over the last &nbsp;");
    scoutln(out, bytesOut, "<input type=text maxlength=6 size=6 name=numDaysSold value='90'>&nbsp; days</td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap><p>Forecast quantity required over the next &nbsp;");
    scoutln(out, bytesOut, "<input type=text maxlength=6 size=6 name=numDaysReqd value='60'>&nbsp; days</td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap><p><input type=checkbox name=includeOption CHECKED> &nbsp;");
    scoutln(out, bytesOut, "Only include items where some have been ordered in the past</td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap><p><input type=checkbox name=includeOption2> &nbsp;");
    scoutln(out, bytesOut, "Include all items in the analysis</td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td><p><a href=\"javascript:generate(2)\">Scan Stock File</a> for Forecast Ordering</td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "</table></form>");     scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void getMfrsDDL(PrintWriter out, String which, String dnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    String userName = directoryUtils.getMySQLUserName();
    String passWord = directoryUtils.getMySQLPassWord();
    
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + userName + "&password=" + passWord);
    Statement stmt = con.createStatement();

    ResultSet rs = stmt.executeQuery("SELECT DISTINCT Manufacturer FROM stock ORDER BY Manufacturer");

    scoutln(out, bytesOut, "<select name=\"mfr" + which + "\">");
    
    String mfr;
    
    while(rs.next())
    {
      mfr = rs.getString(1);
      if(mfr.length() > 0)
        scoutln(out, bytesOut, "<option value=\"" + mfr + "\">" + mfr);
    }

    scoutln(out, bytesOut, "</select>");
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    if(con  != null) con.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
