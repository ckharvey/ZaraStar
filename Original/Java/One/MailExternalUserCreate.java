// =======================================================================================================================================================================================================
// System: ZaraStar Mailengine: Create mail for ext user
// Module: MailExternalUserCreate.java
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
import java.sql.*;
import java.io.*;

public class MailExternalUserCreate extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  ProjectUtils projectUtils = new ProjectUtils();
  DrawingUtils drawingUtils = new DrawingUtils();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="", p3="", p4="", p5="", p6="";

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
      p1  = req.getParameter("p1"); // callBackServlet
      p2  = req.getParameter("p2"); // docCode
      p3  = req.getParameter("p3"); // docType
      p4  = req.getParameter("p4"); // companyCode
      p5  = req.getParameter("p5"); // projectCode
      p6  = req.getParameter("p6"); // Customer or Supplier

      if(p2 == null) p2 = "";
      if(p3 == null) p3 = "";
      if(p4 == null) p4 = "";
      if(p5 == null) p5 = "";
      if(p6 == null) p6 = "";

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, p3, p4, p5, p6, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "MailExternalUserCreate", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8016, bytesOut[0], 0, "ERR:" + p2);
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1, String p2, String p3, String p4, String p5, String p6, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 8014, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "MailExternalUserCreate", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8016, bytesOut[0], 0, "ACC:" + p2);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "MailExternalUserCreate", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8016, bytesOut[0], 0, "SID:" + p2);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }
  
    set(con, stmt, rs, out, req, p1, p2, p3, p4, p5, p6, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 8016, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p2);
    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String callBackServlet, String docCode, String docType, String companyCode, String projectCode, String companyType, String unm, String sid,
                   String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Mail</title>");

    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">"); 

    scoutln(out, bytesOut, "function send(){document.forms[0].submit()}");

    scoutln(out, bytesOut, "</script>");

    int[] hmenuCount = new int[1];

    drawingUtils.outputPageFrame(con, stmt, rs, out, req, "8016", "", "MailExternalUserCreate", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount, bytesOut);

    drawingUtils.drawTitle(out, false, false, "MailExternalUserCreate", "", "Mail to Business Partner", "8016", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<form action=\"MailZaraConfirmation\" enctype=\"application/x-www-form-urlencoded\" method=post>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"unm\" value='" + unm + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"sid\" value='" + sid + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"uty\" value='" + uty + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"men\" value='" + men + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"den\" value='" + den + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"dnm\" value='" + dnm + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"bnm\" value='" + bnm + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"callBackServlet\"  value='" + callBackServlet + "'>");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=sendType value=\"" + docType + "\">");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=companyCode value=\"" + companyCode + "\">");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=companyType value=\"" + companyType + "\">");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=projectCode value=\"" + projectCode + "\">");

    scoutln(out, bytesOut, "<table id=\"page\" cellspacing=2 cellpadding=0 border=0 width=100%>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    outputMail(con, stmt, rs, req, out, callBackServlet, docType, docCode, companyCode, projectCode, companyType, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    scoutln(out, bytesOut, "</table><tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir));
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void outputMail(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, PrintWriter out, String callBackServlet, String docType, String docCode, String companyCode, String projectCode, String companyType, String unm,
                          String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    if(companyType.equals("C"))
      scoutln(out, bytesOut, "<tr><td nowrap><p>Customer Code:</td>");
    else scoutln(out, bytesOut, "<tr><td nowrap><p>Supplier Code:</td>");
    scoutln(out, bytesOut, "<td><p>" + companyCode + "</td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap><p>Project Code:</td><td><p>" + projectCode + "</td></tr>");

    scoutln(out, bytesOut, "<tr><td><p>To:</td><td><p>");
    companyContacts(con, stmt, rs, out, companyType, companyCode, bytesOut);
    scoutln(out, bytesOut, "</td></tr>");

    scoutln(out, bytesOut, "<tr><td><p>From:</td><td><p>");
    ourPeople(con, stmt, rs, out, unm, bytesOut);
    scoutln(out, bytesOut, "</td></tr>");

    scoutln(out, bytesOut, "<tr><td valign=top><p>CC List:</td><td><p><textarea name=ccList cols=80 rows=3 style={white-space:nowrap;overflow:auto;}></textarea>");

    scoutln(out, bytesOut, "<tr><td valign=top><p>BCC List:</td><td><p><textarea name=bccList cols=80 rows=2 style={white-space:nowrap;overflow:auto;}></textarea>");

    scoutln(out, bytesOut, "<tr><td><p>Subject:</td><td><p><input type=text name=subject size=80 value=\"" + docType + " " + docCode);
    if(projectCode.length() > 0)
      scoutln(out, bytesOut, " (Project " + projectCode + ": " + projectUtils.fetchProjectTitleGivenCode(projectCode, dnm, localDefnsDir, defnsDir) + ")");
    scoutln(out, bytesOut, "\"></td></tr>");

    // text lines...
    scoutln(out, bytesOut, "<tr><td valign=top nowrap><p>Text:</td><td><p><textarea name=text cols=80 rows=16 style={white-space:nowrap;overflow:auto;}>");

    scoutln(out, bytesOut, "\n\n\n\nThe following link gives you web access to " + docType + " " + docCode + ".\n\n");

    String server = generalUtils.getFromDefnFile("EXTERNALACCESSSERVER", "local.dfn", localDefnsDir, defnsDir);
    if(! server.startsWith("http://"))
       server = "http://" + server;

    scoutln(out, bytesOut, server + "/central/servlet/MainPageUtils?unm=___ExtUserCode___&uty=R&dnm=" + dnm + "&p1=" + callBackServlet + "&p2=" + docCode);

    scoutln(out, bytesOut, "</textarea></td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td><p><a href=\"javascript:send()\">Preview</a></td></tr>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void companyContacts(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String companyType, String companyCode, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<select name='contactCode'>");

    try
    {
      String company;
      if(companyType.equals("C"))
        company = "CustomerCode";
      else company = "SupplierCode";
     
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT ContactCode, Name, EMail FROM contacts WHERE " + company + " = '" + companyCode + "' ORDER BY Name");

      String contactCode, name, eMail;

      while(rs.next())
      {
        contactCode = rs.getString(1);
        name        = rs.getString(2);
        eMail       = rs.getString(3);

        if(eMail != null && eMail.length() > 0 && eMail.indexOf("@") != -1)
          scoutln(out, bytesOut, "<option value=\"" + contactCode + "\">" + name + " - " + eMail);
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

    scoutln(out, bytesOut, "</select>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void ourPeople(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String unm, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<select name='from'>");

    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT UserCode, UserName, EMail FROM profiles WHERE ( Status = 'L' OR Status = 'S') ORDER BY UserName");

      String userCode, userName, eMail;

      while(rs.next())
      {
        userCode = rs.getString(1);

        if(! userCode.equals("___registered___") && ! userCode.equals("___casual___") && ! userCode.equals("Sysadmin"))
        {
          userName = rs.getString(2);
          eMail    = rs.getString(3);
    
          scoutln(out, bytesOut, "<option value=\"" + userCode + "\"");
          if(unm.equals(userCode))
            scoutln(out, bytesOut, " selected");
          scoutln(out, bytesOut, ">" + userName + " - " + eMail);
        }
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

    scoutln(out, bytesOut, "</select>");
  }

}
