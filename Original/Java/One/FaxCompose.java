// =======================================================================================================================================================================================================
// System: ZaraStar: Fax: compose
// Module: FaxCompose.java
// Author: C.K.Harvey
// Copyright (c) 2000-07 Christopher Harvey. All Rights Reserved.
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
import java.sql.ResultSet;
import java.sql.Statement;

public class FaxCompose extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  DefinitionTables definitionTables = new DefinitionTables();
  FaxUtils faxUtils = new FaxUtils();
  
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

      System.out.println("11006: " + e);
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "FaxCompose", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 11006, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm,
                    String bnm, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    
    Statement stmt = null;
    ResultSet rs   = null;
    
    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 11000, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "FaxCompose", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 11006, bytesOut[0], 0, "ACC:");
      
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "FaxCompose", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 11006, bytesOut[0], 0, "SID:");
      
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    set(con, stmt, rs, out, req, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 11006, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
      
      if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm,
                   String bnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Compose Fax</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    scoutln(out, bytesOut, "function send(){document.forms[0].submit()}");
    
    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir)
                         + "general.css\">");

    int[] hmenuCount = new int[1];

    faxUtils.outputPageFrame(con, stmt, rs, out, req, "", "FaxCompose", "11006", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount, bytesOut);

    faxUtils.drawTitle(con, stmt, rs, req, out, "Compose Fax", "11006", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
    
    scoutln(out, bytesOut, "<form action=\"FaxSend\" enctype=\"application/x-www-form-urlencoded\" method=post>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"unm\" value='" + unm + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"sid\" value='" + sid + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"uty\" value='" + uty + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"men\" value='" + men + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"den\" value='" + den + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"dnm\" value='" + dnm + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"bnm\" value='" + bnm + "'>");

    scoutln(out, bytesOut, "<table id=\"page\" width=100% border=0>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    compose(con, stmt, rs, out, unm, dnm, localDefnsDir, defnsDir, bytesOut);

    scoutln(out, bytesOut, "</table></form>");     scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void compose(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String unm, String dnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    // senderName from user file
    // senderCompany, senderPhone, senderFax from appconfig

    String senderName = authenticationUtils.getUserNameGivenUserCode(con, stmt, rs, unm);
    
    String[] senderCompany = new String[1];
    String[] senderPhone   = new String[1];
    String[] senderFax     = new String[1];
    
    definitionTables.getAppConfigNamePhoneAndFax(con, stmt, rs, dnm, senderCompany, senderPhone, senderFax);
    
    scoutln(out, bytesOut, "<tr><td><p>To Fax Number:</td><td><p><input type=text name=number size=30 maxlength=60></td></tr>");

    scoutln(out, bytesOut, "<tr><td><p>To Person:</td><td><p><input type=text name=person size=60></td></tr>");

    scoutln(out, bytesOut, "<tr><td><p>To Company:</td><td><p><input type=text name=company size=60></td></tr>");

    scoutln(out, bytesOut, "<tr><td><p>Company Code:</td><td><p><input type=text name=companyCode size=20>");
    scoutln(out, bytesOut, "&nbsp;&nbsp;<input type=radio name=companyType value='C'>Customer");
    scoutln(out, bytesOut, "&nbsp;&nbsp;<input type=radio name=companyType value='S'>Supplier</td></tr>");

    scoutln(out, bytesOut, "<tr><td><p>Subject:</td><td><p><input type=text name=subject size=60></td></tr>");

    scoutln(out, bytesOut, "<tr><td><p>Include Cover Sheet:</td><td><p><input type=checkbox name=coverSheet></td></tr>");

    scoutln(out, bytesOut, "<tr><td valign=top nowrap><p>Text:</td><td><p><textarea name=text cols=90 rows=20></textarea></td></tr>");

    scoutln(out, bytesOut, "<tr><td><p>Your Phone Number:</td><td><p><input type=text name=senderPhone size=30 maxlength=60 value=\"" + senderPhone[0]
                         + "\"></td></tr>");
    scoutln(out, bytesOut, "<tr><td><p>Your Fax Number:</td><td><p><input type=text name=senderFax size=30 maxlength=60 value=\"" + senderFax[0]
                         + "\"></td></tr>");
    scoutln(out, bytesOut, "<tr><td><p>Your Name:</td><td><p><input type=text name=senderName size=60 value=\"" + senderName + "\"></td></tr>");
    scoutln(out, bytesOut, "<tr><td><p>Your Company:</td><td><p><input type=text name=senderCompany size=60 value=\"" + senderCompany[0]
                         + "\"></td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td><p><a href=\"javascript:send()\">Send</a></td></tr>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
