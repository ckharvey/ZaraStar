// =======================================================================================================================================================================================================
// System: ZaraStar Product: Stock ReOrder: display report
// Module: StockReorderDisplay.java
// Author: C.K.Harvey
// Copyright (c) 2002-08 Christopher Harvey. All Rights Reserved.
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

public class StockReorderDisplay extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  MiscDefinitions miscDefinitions = new MiscDefinitions();
  ReportGeneration reportGeneration = new ReportGeneration();
  AccountsUtils accountsUtils = new AccountsUtils();
  Inventory  inventory = new Inventory();

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
      directoryUtils.setContentHeaders2(res);
      out = res.getWriter();

      unm = req.getParameter("unm");
      sid = req.getParameter("sid");
      uty = req.getParameter("uty");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");
      p1  = req.getParameter("p1"); // fileName
       
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "StockReorderDisplay", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1021, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                    String fileName, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');
    String reportsDir       = directoryUtils.getUserDir('R', dnm, unm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    
    Statement stmt = null;
    ResultSet rs   = null;
    
    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 1021, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "StockReorderDisplay", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1021, bytesOut[0], 0, "ACC:" + fileName);
      
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "StockReorderDisplay", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1021, bytesOut[0], 0, "SID:" + fileName);
      
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    process(con, stmt, rs, out, req, fileName, unm, sid, uty, men, den, dnm, bnm, reportsDir, imagesDir, localDefnsDir, defnsDir, bytesOut);
    
    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 1021, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), fileName);
      
      if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void process(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String fileName, String unm, String sid,
                       String uty, String men, String den, String dnm, String bnm, String reportsDir, String imagesDir, String localDefnsDir,
                       String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Stock ReOrder</title></head>");

    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\""
                           + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\"></head>");
  
    int[] hmenuCount = new int[1];
    
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "1021", "", "StockReorderInput", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
                                
    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Stock ReOrder Report", "1021", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<FORM action=\"StockReorderCreate\" enctype=\"application/x-www-form-urlencoded\" method=POST>");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=unm value="+unm+">");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=sid value="+sid+">");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=uty value="+uty+">");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=men value=\""+men+"\">");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=den value="+den+">");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=dnm value="+dnm+">");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=bnm value="+bnm+">");

    scoutln(out, bytesOut, "<table id=\"page\" width=100%>");

    RandomAccessFile fh;
    char ch;

    if((fh = generalUtils.fileOpen(reportsDir + fileName)) != null)
    {
      try
      {
        while(true)
        {
          ch = (char)fh.readByte();
          if(ch == '\002') // sid
            scout(out, bytesOut, sid);
          else scout(out, bytesOut, "" + ch);
        }
      }
      catch(Exception e) { }
    }
    else System.out.println("Error displaying: " + fileName);

    generalUtils.fileClose(fh);

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    setCurrencies(con, stmt, rs, out, dnm, localDefnsDir, defnsDir, bytesOut);
    scoutln(out, bytesOut, "&nbsp; at a rate of &nbsp; <input type=text maxlength=10 size=6 name=rate value=1.00></td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=10><p>The <i>order currency</i> must match the currency of the purchase price currency stored on the stock record.");
    scoutln(out, bytesOut, "The <i>rate</i> is used to calculate the issue currency prices to base currency prices for accounting purposes.</td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=4><p><input type=image src=\"" + imagesDir + "go.gif\" name=X>&nbsp;&nbsp;Create Purchase Order</td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void setCurrencies(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String dnm, String localDefnsDir, String defnsDir,
                             int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<tr><td colspan=6><p>Order currency ");
     
    scoutln(out, bytesOut, accountsUtils.getCurrencyNamesDDL(con, stmt, rs, "currency", dnm, localDefnsDir, defnsDir));
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
