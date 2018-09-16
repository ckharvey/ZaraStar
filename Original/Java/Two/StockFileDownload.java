// =======================================================================================================================================================================================================
// System: ZaraStar Product: Stock File Download
// Module: StockFileUploadUpdate.java
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

public class StockFileDownload extends HttpServlet
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
 
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "StockFileDownload", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3057, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm,
                    String bnm, int[] bytesOut) throws Exception
  {
    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    long startTime = new java.util.Date().getTime();

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 3057, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "StockFileDownload", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3057, bytesOut[0], 0, "ACC:");
      
      if(con != null) con.close();
     if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "StockFileDownload", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3057, bytesOut[0], 0, "SID:");
       
      if(con != null) con.close();
     if(out != null) out.flush(); 
      return;
    }

    set(con, stmt, rs, out, req, unm, sid, uty, men, den, dnm, bnm, imagesDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 3057, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
      
      if(con != null) con.close();
   if(out != null) out.flush(); 
  }
 
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty,
                   String men, String den, String dnm, String bnm, String imagesDir, String localDefnsDir, String defnsDir, int[] bytesOut)
                   throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Stock DataBase Download</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    scoutln(out, bytesOut, "function go(){document.forms[0].submit();}");

    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\""
                           + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    int[] hmenuCount = new int[1];
 
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "3057", "", "StockFileDownload", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
                              
    scoutln(out, bytesOut, "<form action=\"StockFileDownloadExecute\" enctype=\"application/x-www-form-urlencoded\" method=POST>");
    scoutln(out, bytesOut, "<input type=hidden name=unm value=" + unm + ">");
    scoutln(out, bytesOut, "<input type=hidden name=uty value=" + uty + ">");
    scoutln(out, bytesOut, "<input type=hidden name=sid value=" + sid + ">");
    scoutln(out, bytesOut, "<input type=hidden name=men value=" + men + ">");
    scoutln(out, bytesOut, "<input type=hidden name=den value=" + den + ">");
    scoutln(out, bytesOut, "<input type=hidden name=dnm value=" + dnm + ">");
    scoutln(out, bytesOut, "<input type=hidden name=bnm value=" + bnm + ">");

    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Stock DataBase Download", "3057", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<table id=\"page\" width=100% border=0>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td><p>Enter the Manufacturer to export: ");
    getMfrsDDL(out, dnm, localDefnsDir, defnsDir, bytesOut);
    scoutln(out, bytesOut, " (optional)</td></tr>");
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap<p>Select the Stock record fields to download:</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td>"); 
    getFields(out, localDefnsDir, defnsDir, bytesOut);
    scoutln(out, bytesOut, "</td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap><p><a href=\"javascript:go()\">Download</a> Stock File</td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void getFields(PrintWriter out, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<tr><td><p><input type=checkbox name=\"Description\">Description 1</td></tr>");
    scoutln(out, bytesOut, "<tr><td><p><input type=checkbox name=\"Description2\">Description 2</td></tr>");
    scoutln(out, bytesOut, "<tr><td><p><input type=checkbox name=\"Remark\">Remark</td></tr>");
    scoutln(out, bytesOut, "<tr><td><p><input type=checkbox name=\"Manufacturer\">Manufacturer</td></tr>");
    scoutln(out, bytesOut, "<tr><td><p><input type=checkbox name=\"ManufacturerCode\">ManufacturerCode</td></tr>");
    scoutln(out, bytesOut, "<tr><td><p><input type=checkbox name=\"AltItemCode1\">Alternative Item Code 1</td></tr>");
    scoutln(out, bytesOut, "<tr><td><p><input type=checkbox name=\"AltItemCode2\">Alternative Item Code 2</td></tr>");
    scoutln(out, bytesOut, "<tr><td><p><input type=checkbox name=\"AltItemCode3\">Alternative Item Code 3</td></tr>");
    scoutln(out, bytesOut, "<tr><td><p><input type=checkbox name=\"AltItemCode4\">Alternative Item Code 4</td></tr>");
    scoutln(out, bytesOut, "<tr><td><p><input type=checkbox name=\"Misc1\">Miscellaneous 1</td></tr>");
    scoutln(out, bytesOut, "<tr><td><p><input type=checkbox name=\"Misc2\">Miscellaneous 2</td></tr>");
    scoutln(out, bytesOut, "<tr><td><p><input type=checkbox name=\"Misc3\">Miscellaneous 3</td></tr>");
    scoutln(out, bytesOut, "<tr><td><p><input type=checkbox name=\"Misc4\">Miscellaneous 4</td></tr>");
    scoutln(out, bytesOut, "<tr><td><p><input type=checkbox name=\"Misc5\">Miscellaneous 5</td></tr>");
    scoutln(out, bytesOut, "<tr><td><p><input type=checkbox name=\"Misc6\">Miscellaneous 6</td></tr>");
    scoutln(out, bytesOut, "<tr><td><p><input type=checkbox name=\"Misc7\">Miscellaneous 7</td></tr>");
    scoutln(out, bytesOut, "<tr><td><p><input type=checkbox name=\"Misc8\">Miscellaneous 8</td></tr>");
    scoutln(out, bytesOut, "<tr><td><p><input type=checkbox name=\"WrittenOff\">Written-Off</td></tr>");
    scoutln(out, bytesOut, "<tr><td><p><input type=checkbox name=\"Misc10\">Miscellaneous 10</td></tr>");
    scoutln(out, bytesOut, "<tr><td><p><input type=checkbox name=\"Misc11\">Miscellaneous 11</td></tr>");
    scoutln(out, bytesOut, "<tr><td><p><input type=checkbox name=\"Misc12\">Miscellaneous 12</td></tr>");
    scoutln(out, bytesOut, "<tr><td><p><input type=checkbox name=\"Status\">Status</td></tr>");
    scoutln(out, bytesOut, "<tr><td><p><input type=checkbox name=\"Currency\">Purchase Currency</td></tr>");
    scoutln(out, bytesOut, "<tr><td><p><input type=checkbox name=\"PurchasePrice\">Purchase Price</td></tr>");
    scoutln(out, bytesOut, "<tr><td><p><input type=checkbox name=\"ExchangeRate\">Exchange Rate</td></tr>");
    scoutln(out, bytesOut, "<tr><td><p><input type=checkbox name=\"SalesCurrency\">Sales Currency</td></tr>");
    scoutln(out, bytesOut, "<tr><td><p><input type=checkbox name=\"RRP\">List Price</td></tr>");
    scoutln(out, bytesOut, "<tr><td><p><input type=checkbox name=\"SellPrice1\">Sell Price 1</td></tr>");
    scoutln(out, bytesOut, "<tr><td><p><input type=checkbox name=\"SellPrice2\">Sell Price 2</td></tr>");
    scoutln(out, bytesOut, "<tr><td><p><input type=checkbox name=\"SellPrice3\">Sell Price 3</td></tr>");
    scoutln(out, bytesOut, "<tr><td><p><input type=checkbox name=\"SellPrice4\">Sell Price 4</td></tr>");
    scoutln(out, bytesOut, "<tr><td><p><input type=checkbox name=\"PPToRRPFactor\">Factor</td></tr>");
    scoutln(out, bytesOut, "<tr><td><p><input type=checkbox name=\"Markup1\">Markdown 1</td></tr>");
    scoutln(out, bytesOut, "<tr><td><p><input type=checkbox name=\"Markup2\">Markdown 2</td></tr>");
    scoutln(out, bytesOut, "<tr><td><p><input type=checkbox name=\"Markup3\">Markdown 3</td></tr>");
    scoutln(out, bytesOut, "<tr><td><p><input type=checkbox name=\"Markup4\">Markdown 4</td></tr>");
    scoutln(out, bytesOut, "<tr><td><p><input type=checkbox name=\"Weight\">Weight</td></tr>");
    scoutln(out, bytesOut, "<tr><td><p><input type=checkbox name=\"WeightPer\">WeightPer</td></tr>");
    scoutln(out, bytesOut, "<tr><td><p><input type=checkbox name=\"UoM\">Unit of Measure</td></tr>");
    scoutln(out, bytesOut, "<tr><td><p><input type=checkbox name=\"Image1\">Image 1</td></tr>");
    scoutln(out, bytesOut, "<tr><td><p><input type=checkbox name=\"Image2\">Image 2</td></tr>");
    scoutln(out, bytesOut, "<tr><td><p><input type=checkbox name=\"CategoryCode\">Category Code</td></tr>");
    scoutln(out, bytesOut, "<tr><td><p><input type=checkbox name=\"DateChanged\">Date Changed</td></tr>");
    scoutln(out, bytesOut, "<tr><td><p><input type=checkbox name=\"Grouping\">Grouping</td></tr>");

    scoutln(out, bytesOut, "<tr><td><p>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td><p><input type=checkbox name=\"levels\">Include stock levels as of this date &nbsp;");
    scoutln(out, bytesOut, "<input type=text name=\"levelsDate\"></td></tr>");

    scoutln(out, bytesOut, "<tr><td><p>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td><p><input type=checkbox name=\"ignoreObsolete\" checked>Ignore stock items marked as obsolete</td></tr>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void getMfrsDDL(PrintWriter out, String dnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    String userName = directoryUtils.getMySQLUserName();
    String passWord = directoryUtils.getMySQLPassWord();
    
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + userName + "&password=" + passWord);
    Statement stmt = con.createStatement();

    ResultSet rs = stmt.executeQuery("SELECT DISTINCT Manufacturer FROM stock ORDER BY Manufacturer");

    scoutln(out, bytesOut, "<select name=\"mfr\"><option value=\"___ALL___\">ALL");
    
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
