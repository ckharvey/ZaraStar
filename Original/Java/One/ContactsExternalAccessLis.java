// =======================================================================================================================================================================================================
// System: ZaraStar: Contacts: List Ext Access
// Module: ContactsExternalAccessLis.java
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

public class ContactsExternalAccessLis extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  ContactsUtils contactsUtils = new ContactsUtils();
  
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
      p1  = req.getParameter("p1"); // N if no link required
      
      if(p1 == null) p1 = "Y";
      
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

      System.out.println("8817: " + e);
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "ContactsExternalAccessLis", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8817, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1, int[] bytesOut) throws Exception
  {
    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    long startTime = new java.util.Date().getTime();

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 8801, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "ContactsExternalAccessLis", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8817, bytesOut[0], 0, "ACC:");
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "ContactsExternalAccessLis", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8817, bytesOut[0], 0, "SID:");
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    set(con, stmt, rs, out, req, p1, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 8817, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String link, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir,
                   int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>External Access</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    contactsUtils.outputJS(con, stmt, rs, out, req, unm, uty, sid, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    if(link.equals("Y"))
    {      
      // write to clipboard
      scoutln(out, bytesOut, "var req3;");    
      scoutln(out, bytesOut, "function initRequest3(url)");
      scoutln(out, bytesOut, "{if(window.XMLHttpRequest){req3=new XMLHttpRequest();}else");
      scoutln(out, bytesOut, "if(window.ActiveXObject){req3=new ActiveXObject(\"Microsoft.XMLHTTP\");}}");

      scoutln(out, bytesOut, "function select(extCode,extPwd,extRights,extApp){var ext=extCode+'%20'+extPwd+'%20'+extRights+'%20'+extApp+'%20';");
      scoutln(out, bytesOut, "var url=\"http://" + men + "/central/servlet/ClipboardWrite?unm=\" + escape('" + unm + "') + \"&sid=\" + escape('" + sid + "') + \"&uty=\" + escape('" + uty + "') + \"&men=\" + escape('" + men + "') + \"&den=\" + escape('"
                           + den + "') + \"&bnm=\" + escape('" + bnm + "') + \"&p1=\" + ext + \"&dnm=\" + escape('" + dnm + "');");
      scoutln(out, bytesOut, "initRequest3(url);");
      scoutln(out, bytesOut, "req3.onreadystatechange=processRequest3;");
      scoutln(out, bytesOut, "req3.open(\"GET\", url, true);");
      scoutln(out, bytesOut, "req3.send(null);}");

      scoutln(out, bytesOut, "function processRequest3(){");
      scoutln(out, bytesOut, "if(req3.readyState==4){");
      scoutln(out, bytesOut, "if(req3.status==200){");
      scoutln(out, bytesOut, "var res=req3.responseXML.getElementsByTagName(\"res\")[0].childNodes[0].nodeValue;");
      scoutln(out, bytesOut, "if(res.length>0){");
      scoutln(out, bytesOut, "if(res=='.')history.back();");
      scoutln(out, bytesOut, "}}}}");   
    }
      
    scoutln(out, bytesOut, "function go(){document.forms[0].submit();}");

    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\""  + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    int[] hmenuCount = new int[1];
    
    contactsUtils.outputPageFrame(con, stmt, rs, out, req, "", "ContactsExternalAccessLis", "8817", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
  
    contactsUtils.drawTitle(con, stmt, rs, req, out, "External Access", "8817", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<br><table id=\"page\" width=100% border=0 cellspacing=\"2\" cellpadding=\"2\">");

    scoutln(out, bytesOut, "<form action=\"ContactsExternalAccessApprove\" enctype=\"application/x-www-form-urlencoded\" method=POST>");
    scoutln(out, bytesOut, "<tr><td><p><input type=hidden name=unm value=" + unm + ">");
    scoutln(out, bytesOut, "<tr><td><p><input type=hidden name=uty value=" + uty + ">");
    scoutln(out, bytesOut, "<tr><td><p><input type=hidden name=sid value=" + sid + ">");
    scoutln(out, bytesOut, "<tr><td><p><input type=hidden name=men value=" + men + ">");
    scoutln(out, bytesOut, "<tr><td><p><input type=hidden name=den value=" + den + ">");
    scoutln(out, bytesOut, "<tr><td><p><input type=hidden name=dnm value=" + dnm + ">");
    scoutln(out, bytesOut, "<tr><td><p><input type=hidden name=bnm value=" + bnm + ">");

    scoutln(out, bytesOut, "<table id=\"page\" width=100% border=0>");
    scoutln(out, bytesOut, "<tr id=\"pageColumn\"><td><p>Company</td><td><p>Name</td>");
    scoutln(out, bytesOut, "<td><p>Job Title</td><td><p>eMail</td>");
    scoutln(out, bytesOut, "<td><p>Owner</td><td><p>External Code</td>");
    scoutln(out, bytesOut, "<td><p>Access Rights</td><td><p>Access Approved</td></tr>");

    fetch(con, stmt, rs, out, req, link, unm, uty, dnm, localDefnsDir, defnsDir, bytesOut);
    
    if(! link.equals("Y"))
    {  
      if(authenticationUtils.verifyAccess(con, stmt, rs, req, 8819, unm, uty, dnm, localDefnsDir, defnsDir))
      {
        scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr><tr><td>&nbsp;</td></tr>");   
        scoutln(out, bytesOut, "<tr><td><p><a href=\"javascript:go()\">Update Access Rights</a></td></tr>");
      }
    }
      
    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void fetch(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String link, String unm, String uty, String dnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT Owner, Name, CompanyName, EMail, JobTitle, ExternalCode, ExternalPassWord, ExternalRights, ExternalApproved FROM contacts WHERE ExternalCode != '' ORDER BY CompanyName");

    String owner, name, companyName, eMail, jobTitle, externalCode, externalPassWord, externalRights, externalApproved, cssFormat="";

    while(rs.next())
    {    
      owner            = rs.getString(1);
      name             = rs.getString(2);
      companyName      = rs.getString(3);
      eMail            = rs.getString(4);
      jobTitle         = rs.getString(5);
      externalCode     = rs.getString(6);
      externalPassWord = rs.getString(7);
      externalRights   = rs.getString(8);
      externalApproved = rs.getString(9);

      if(externalApproved.equals("Y"))
        externalApproved = "Yes";
      else externalApproved = "No";
      
      if(cssFormat.equals("line1")) cssFormat = "line2"; else cssFormat = "line1";

      writeBodyLine(con, stmt, rs, out, req, unm, uty, dnm, owner, name, companyName, eMail, jobTitle, externalCode, externalPassWord, externalRights, externalApproved, link, cssFormat, localDefnsDir, defnsDir, bytesOut);
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void writeBodyLine(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String unm, String uty, String dnm, String owner, String name, String companyName, String eMail, String jobTitle,
                             String externalCode, String externalPassWord, String externalRights, String externalApproved, String link, String cssFormat, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<tr id='" + cssFormat + "'><td><p>" + companyName + "</td><td nowrap><p>" + name + "</td>");
    scoutln(out, bytesOut, "<td><p>" + jobTitle + "</td><td nowrap><p>" + eMail + "</td>");
    
    scout(out, bytesOut, "<td><p>" + owner + "</td><td nowrap><p>");
    if(link.equals("Y"))
      scout(out, bytesOut, "<a href=\"javascript:select('" + externalCode + "','" + externalPassWord + "','" + externalRights + "','" + externalApproved + "')\">");

    scout(out, bytesOut, externalCode);
    if(link.equals("Y"))
      scout(out, bytesOut, "</a>");
    scoutln(out, bytesOut, "</td>");

    scoutln(out, bytesOut, "<td><p>" + externalRights + "</td>");

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 8819, unm, uty, dnm, localDefnsDir, defnsDir))
      scoutln(out, bytesOut, "<td><p>" + externalApproved + "</td></tr>");
    else
    {
      scout(out, bytesOut, "<td><p><input type=checkbox name='" + generalUtils.sanitise(name) + "\002" + generalUtils.sanitise(companyName) + "'");
      
      if(externalApproved.equals("Yes"))
        scout(out, bytesOut, " checked");
      
      scoutln(out, bytesOut, "></td></tr>"); 
    }   
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scout(PrintWriter out, int[] bytesOut, String str) throws Exception
  {      
    out.print(str);
    bytesOut[0] += str.length();
  }
  private void scoutln(PrintWriter out, int[] bytesOut, String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);
  }

}
