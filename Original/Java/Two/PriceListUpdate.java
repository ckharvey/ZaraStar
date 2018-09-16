// =======================================================================================================================================================================================================
// System: ZaraStar Product: Price List Update
// Module: PriceListUpdate.java
// Author: C.K.Harvey
// Copyright (c) 2003-08 Christopher Harvey. All Rights Reserved.
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

public class PriceListUpdate extends HttpServlet
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
 
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "PriceListUpdate", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3053, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                    int[] bytesOut) throws Exception
  {
    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String libDir           = directoryUtils.getUserDir('L', dnm, unm);

    long startTime = new java.util.Date().getTime();

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
     
   Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 3053, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "PriceListUpdate", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3053, bytesOut[0], 0, "ACC:");
       
      if(con != null) con.close();
     if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "PriceListUpdate", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3053, bytesOut[0], 0, "SID:");
       
      if(con != null) con.close();
     if(out != null) out.flush(); 
      return;
    }

    set(con, stmt, rs, out, req, unm, sid, uty, men, den, dnm, bnm, libDir, imagesDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 3053, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
      
      if(con != null) con.close();
   if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty,
                   String men, String den, String dnm, String bnm, String libDir, String imagesDir, String localDefnsDir, String defnsDir,
                   int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Price List Update</title>");

    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\""
                           + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\"></head>");

    int[] hmenuCount = new int[1];
 
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "3053", "", "PriceListUpdate", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount,
                          bytesOut);
         
    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Price List Update", "3053", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<form action=\"PriceListUpdatePerform\" enctype=\"application/x-www-form-urlencoded\" method=POST>");
    scoutln(out, bytesOut, "<input type=hidden name=unm value=" + unm + ">");
    scoutln(out, bytesOut, "<input type=hidden name=sid value=" + sid + ">");
    scoutln(out, bytesOut, "<input type=hidden name=uty value=" + uty + ">");
    scoutln(out, bytesOut, "<input type=hidden name=men value=" + men + ">");
    scoutln(out, bytesOut, "<input type=hidden name=den value=" + den + ">");
    scoutln(out, bytesOut, "<input type=hidden name=dnm value=" + dnm + ">");
    scoutln(out, bytesOut, "<input type=hidden name=bnm value=" + bnm + ">");

    scoutln(out, bytesOut, "<table id=\"page\" width=100% border=0>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    
    scoutln(out, bytesOut, "<tr><td><p>Library File Number:</td>");
    scoutln(out, bytesOut, "<td><p><input type=text size=8 maxlength=10 name=fileNumber></td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap><p>Manufacturer</td><td>");
    getMfrsDDL(con, stmt, rs, out, bytesOut);
    scoutln(out, bytesOut, "</td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap><p>The purchase currency is</td><td>");
    getCurrencies(con, stmt, rs, out, "purchaseCurrency", bytesOut);
    scoutln(out, bytesOut, "</td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap><p>The sales currency is</td><td>");
    getCurrencies(con, stmt, rs, out, "salesCurrency", bytesOut);
    scoutln(out, bytesOut, "</td></tr>");

    scoutln(out, bytesOut, "<tr><td colspan=3><hr></td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=3>");
      
    scoutln(out, bytesOut, "<table id=\"page\" width=100% border=0>");
    scoutln(out, bytesOut, "<tr><td colspan=3><p>You have two ways of providing the List Price:</td></tr><tr><td><p>1.</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Enter it in the Import File, or</td></tr><tr><td><p>2.</td>"
                         + "<td colspan=2><p>Calculate it from the Purchase price; in which case you must supply the following:</td></tr>");      
    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>Exchange Rate</td>");
    scoutln(out, bytesOut, "<td width=90% colspan=2><p><input type=text size=10 maxlength=10 name=exchangeRate></td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>Our Markup</td>");
    scoutln(out, bytesOut, "<td><p><input type=text size=10 maxlength=10 name=ourMarkup>&nbsp;%</td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>Dealer Discount &nbsp;</td>");
    scoutln(out, bytesOut, "<td><p><input type=text size=10 maxlength=10 name=dealerDiscount>&nbsp;%</td></tr>");

    scoutln(out, bytesOut, "</table></td></tr>");
      
    scoutln(out, bytesOut, "<tr><td colspan=3><hr></td></tr>");
      
    scoutln(out, bytesOut, "<tr><td nowrap><p>The markdown from the List Price to SellPrice1 is</td>");
    scoutln(out, bytesOut, "<td width=99%><p><input type=text size=10 maxlength=10 name=markdown1>&nbsp;%</td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap><p>The markdown from the List Price to SellPrice2 is</td>");
    scoutln(out, bytesOut, "<td><p><input type=text size=10 maxlength=10 name=markdown2>&nbsp;%</td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap><p>The markdown from the List Price to SellPrice3 is</td>");
    scoutln(out, bytesOut, "<td><p><input type=text size=10 maxlength=10 name=markdown3>&nbsp;%</td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap><p>The markdown from the List Price to SellPrice4 is</td>");
    scoutln(out, bytesOut, "<td><p><input type=text size=10 maxlength=10 name=markdown4>&nbsp;%</td></tr>");

    scoutln(out, bytesOut, "<tr><td colspan=3><p><input type=checkbox name=addNew>"
                           + "Add as new stock records any items in the price list that are not already in the stock database.</tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=4><p><input type=image src=\"" + imagesDir + "go.gif\" name=V>");
    scoutln(out, bytesOut, "View Import File&nbsp;&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td colspan=4 nowrap><p><input type=image src=\"" + imagesDir + "go.gif\" name=T>");
    scoutln(out, bytesOut, "Test Update Stock File&nbsp;&nbsp;");
    scoutln(out, bytesOut, "<span id=\"optional\">Process the import file but do NOT actually change the stock file</span></td></tr>");

    scoutln(out, bytesOut, "<tr><td colspan=4><p><input type=image src=\"" + imagesDir + "go.gif\" name=U>");
    scoutln(out, bytesOut, "Update Stock File&nbsp;&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "</table></form>");     scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void getMfrsDDL(Connection con, Statement stmt, ResultSet rs, PrintWriter out, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT DISTINCT Manufacturer FROM stock ORDER BY Manufacturer");

    scoutln(out, bytesOut, "<select name=\"mfr\">");
    
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
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void getCurrencies(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String selectName, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT CurrencyCode FROM currency ORDER BY CurrencyCode");

    scoutln(out, bytesOut, "<p><select name=\"" + selectName + "\">");
    
    String currency;
    
    while(rs.next())
    {
      currency = rs.getString(1);
      scoutln(out, bytesOut, "<option value=\"" + currency + "\">" + currency);
    }

    scoutln(out, bytesOut, "</select>");
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
