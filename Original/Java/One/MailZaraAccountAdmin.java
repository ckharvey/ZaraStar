// =======================================================================================================================================================================================================
// System: ZaraStar Mail: Account Admin
// Module: MailZaraAccountAdmin.java
// Author: C.K.Harvey
// Copyright (c) 2001-08 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
//
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

public class MailZaraAccountAdmin extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  MailUtils mailUtils = new MailUtils();
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

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="";

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
      p1  = req.getParameter("p1"); // caller
      p2  = req.getParameter("p2"); // userCode

      if(p1 == null) p1 = "";
      if(p2 == null) p2 = unm;

      doIt(out, req, p1, p2, unm, sid, uty, men, den, dnm, bnm, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "MailZaraAccountAdmin", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8022, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String p1, String p2, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    long startTime = new java.util.Date().getTime();

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 8022, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "MailZaraAccountAdmin", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8022, bytesOut[0], 0, "ACC:");
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }
   
    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "MailZaraAccountAdmin", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8022, bytesOut[0], 0, "SID:");
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }   

    boolean fromZaraAdmin = false;
    if(p1.equals("ZA"))
      fromZaraAdmin = true;

    set(con, stmt, rs, out, req, fromZaraAdmin, p1, p2, unm, sid, uty, men, den, dnm, bnm, imagesDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 8022, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, boolean fromZaraAdmin, String p1, String p2, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                   String imagesDir, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Mail: Account Administration</title>");

    String cssDirectory = directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir);
            
    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + cssDirectory + "general.css\">");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    scoutln(out, bytesOut, "function access(account){var p1=sanitise(account);");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/MailZaraAccountAdminEdit?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=" + p1 + "&p3=" + p2 + "&p1=\"+p1;}");

    if(fromZaraAdmin)
    {
      scoutln(out, bytesOut, "function list(){");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/MailAccounts?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\";}");
    }

    scoutln(out, bytesOut, "function sanitise(code){");
    scoutln(out, bytesOut, "var code2='';var x;var len=code.length;");
    scoutln(out, bytesOut, "for(x=0;x<len;++x)");
    scoutln(out, bytesOut, "if(code.charAt(x)=='#')code2+='%23';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='\"')code2+='%22';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='&')code2+='%26';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='%')code2+='%25';");
    scoutln(out, bytesOut, "else if(code.charAt(x)==' ')code2+='%20';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='?')code2+='%3F';");
    scoutln(out, bytesOut, "else code2+=code.charAt(x);");
    scoutln(out, bytesOut, "return code2};");

    scoutln(out, bytesOut, "</script>");

    if(fromZaraAdmin)
    {
      scoutln(out, bytesOut, "</head><body>");
      signOnAdministrator.heading(out, true, unm, sid, uty, bnm, dnm, men, den, imagesDir, bytesOut);
      dashboardUtils.drawTitle(out, "Mail Operations: " + p2, "402", unm, sid, uty, men, den, dnm, bnm, bytesOut);
    }
    else
    {
      int[] hmenuCount = new int[1];
      mailUtils.outputPageFrame(con, stmt, rs, out, req, false, "", "", "MailZaraAccountAdmin", "8022", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount, bytesOut);
      mailUtils.drawTitle(con, stmt, rs, req, out, false, "", "Account Administration: " + p2, "8022", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
    }

    scoutln(out, bytesOut, "<form><table id=\"page\" cellspacing=2 cellpadding=0 width=100%>");
 
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    boolean atLeastOneAccount = getAccounts(out, p2, dnm, bytesOut);
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    if(atLeastOneAccount)
      scoutln(out, bytesOut, "<tr><td nowrap><p>Or, <a href=\"javascript:access('')\">Create a New Account</a><br></td></tr>");
    else scoutln(out, bytesOut, "<tr><td nowrap><p>No Accounts Defined<br><br><a href=\"javascript:access('')\">Create a New Account</a><br></td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    if(fromZaraAdmin)
      scoutln(out, bytesOut, "<tr><td nowrap><p><a href=\"javascript:list()\">Return to List of Users</a></td></tr>");

    scoutln(out, bytesOut, "</table></form>");

    if(fromZaraAdmin)
      scoutln(out, bytesOut, "</body></html>");
    else scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir));
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean getAccounts(PrintWriter out, String unm, String dnm, int[] bytesOut)
  {
    boolean first = true;

    String account, server, userName, address, userFullName, type;

    Connection con  = null;
    Statement  stmt = null;
    ResultSet  rs   = null;

    try
    {
      String uName = directoryUtils.getMySQLUserName();
      String pWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();
    
      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_mail?user=" + uName + "&password=" + pWord);

      stmt = con.createStatement();
  
      rs = stmt.executeQuery("SELECT * FROM mailaccounts WHERE Owner = '" + unm + "'");

      while(rs.next())                  
      {
        account      = rs.getString(1);
        server       = rs.getString(3);
        address      = rs.getString(4);
        userName     = rs.getString(5);
        userFullName = rs.getString(7);
        type         = rs.getString(8);

        if(first)
        {
          scoutln(out, bytesOut, "<tr><td nowrap><p>Select an Account to change:</td></tr>");
          scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

          scoutln(out, bytesOut, "</table><table id=\"page\" border=\"0\" cellspacing=\"3\" cellpadding=\"3\" width=100%>");

          scoutln(out, bytesOut, "<tr id=\"pageColumn\"><td><p><b>Account</b></td>");
          scoutln(out, bytesOut, "<td nowrap><p><b>User Name</b></td>");
          scoutln(out, bytesOut, "<td nowrap><p><b>Address</b></td>");
          scoutln(out, bytesOut, "<td nowrap><p><b>Mail Display Name</b></td>");
          scoutln(out, bytesOut, "<td nowrap><p><b>Server</b></td>");
          scoutln(out, bytesOut, "<td nowrap width=90%><p><b>Type</b></td></tr>");

          first = false;
        }

        scoutln(out, bytesOut, "<tr><td nowrap><p><a href=\"javascript:access('" + account + "')\">" + account + "</a></td>");
        scoutln(out, bytesOut, "<td nowrap><p>" + userName + "</td><td nowrap><p>" + address + "</td><td nowrap><p>" + userFullName + "</td><td nowrap><p>" + server + "</td><td nowrap><p>" + type + "</td></tr>");
      }  
      
      if(rs != null) rs.close();
      if(stmt != null) stmt.close();        
      if(con  != null) con.close();
    }
    catch(Exception e)
    {
      System.out.println("mailUtils: " + e);
      try
      {
        if(rs   != null) rs.close();
        if(stmt != null) stmt.close();
        if(con  != null) con.close();
      }
      catch(Exception e2) { }
    }

    return ! first;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }
  
}
