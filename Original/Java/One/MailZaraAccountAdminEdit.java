// =======================================================================================================================================================================================================
// System: ZaraStar MailEngine: Account Admin - Edit Account Details
// Module: MailZaraAccountAdminEdit.java
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

public class MailZaraAccountAdminEdit extends HttpServlet
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

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="", p3="";

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
      p1  = req.getParameter("p1");
      p2  = req.getParameter("p2"); // caller
      p3  = req.getParameter("p3"); // userCode

      if(p2 == null) p2 = "";
      if(p3 == null) p3 = unm;

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, p3, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "MailZaraAccountAdminEdit", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8022, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1, String p2, String p3, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 8022, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "MailZaraAccountAdmin", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8022, bytesOut[0], 0, "ACC:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }
   
    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "MailZaraAccountAdmin", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8022, bytesOut[0], 0, "SID:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }    

    boolean fromZaraAdmin = false;
    if(p2.equals("ZA"))
      fromZaraAdmin = true;

    set(con, stmt, rs, out, req, p1, fromZaraAdmin, p2, p3, unm, sid, uty, men, den, dnm, bnm, imagesDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 8022, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String account, boolean fromZaraAdmin, String p2, String userCode, String unm, String sid, String uty, String men, String den, String dnm,
                   String bnm, String imagesDir, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Zara Mail: Account Maintenance</title>");

    String cssDirectory = directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir);
            
    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + cssDirectory + "general.css\">");

    scoutln(out, bytesOut, "<FORM ACTION=\"MailZaraAccountAdminUpdate\" ENCTYPE=\"application/x-www-form-urlencoded\" METHOD=POST>");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=unm value=" + unm + ">");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=uty value=" + uty + ">");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=sid value=" + sid + ">");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=men value=\"" + men + "\">");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=den value=" + den + ">");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=dnm value=" + dnm + ">");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=bnm value=" + bnm + ">");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=fromZaraAdmin value=" + p2 + ">");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=userCode value=" + userCode + ">");
    if(account.length() > 0)
      scoutln(out, bytesOut, "<INPUT TYPE=hidden name=account value=" + generalUtils.sanitise(account) + ">");

    if(fromZaraAdmin)
    {
      scoutln(out, bytesOut, "</head><body>");
      signOnAdministrator.heading(out, true, unm, sid, uty, bnm, dnm, men, den, imagesDir, bytesOut);
      if(account.length() == 0)
        dashboardUtils.drawTitle(out, "New Account", "8022", unm, sid, uty, men, den, dnm, bnm, bytesOut);
      else dashboardUtils.drawTitle(out, "Account Maintenance for <i><b>" + account + "</b></i>", "8022", unm, sid, uty, men, den, dnm, bnm, bytesOut);
    }
    else
    {
      int[] hmenuCount = new int[1];

      mailUtils.outputPageFrame(con, stmt, rs, out, req, false, "", "", "MailZaraAccountAdminEdit", "8022", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount, bytesOut);
    
      if(account.length() == 0)
        mailUtils.drawTitle(con, stmt, rs, req, out, false, "", "New Account", "8022", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
      else mailUtils.drawTitle(con, stmt, rs, req, out, false, "", "Account Maintenance for <i><b>" + account + "</b></i>", "8022", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
    }

    scoutln(out, bytesOut, "<form><table id='page' cellspacing=2 cellpadding=0 width=100%>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    String[] server   = new String[1]; server[0]   = "";
    String[] address  = new String[1]; address[0]  = "";
    String[] userName = new String[1]; userName[0] = "";
    String[] passWord = new String[1]; passWord[0] = "";
    String[] desc     = new String[1]; desc[0]     = "";
    String[] type     = new String[1]; type[0]     = "";

    if(account.length() == 0 || mailUtils.getAccountDetails(account, userCode, dnm, server, address, userName, passWord, desc, type)) // just-in-case
    {
      if(account.length() == 0)
      {
        scoutln(out, bytesOut, "<tr><td><p>Account Display Name &nbsp;</td>");
        scoutln(out, bytesOut, "<td><input type=text size=50 name=account></td>");
        scoutln(out, bytesOut, "<td><p>Example: <i>CyberNet ISP</i></td></tr>");
      }

      scoutln(out, bytesOut, "<tr><td><p>Account Name&nbsp;</td>");
      scoutln(out, bytesOut, "<td><input type=text size=50 name=userName value='" + userName[0] + "'></td>");
      scoutln(out, bytesOut, "<td><p>Example: <i>petertan</i></td></tr>");

      scoutln(out, bytesOut, "<tr><td><p>Pass Word &nbsp;</td>");
      scoutln(out, bytesOut, "<td><input type=password size=50 name=passWord value='" + passWord[0] + "'></td>");
      scoutln(out, bytesOut, "<td><p>Example: <i>mypassword</i></span></td></tr>");

      scoutln(out, bytesOut, "<tr><td><p>Server &nbsp;</td>");
      scoutln(out, bytesOut, "<td><input type=text size=50 name=server value='" + server[0] + "'></td>");
      scoutln(out, bytesOut, "<td><p>Example: <i>mail.cybernetisp.com</i></td></tr>");

      scoutln(out, bytesOut, "<tr><td><p>Address &nbsp;</td>");
      scoutln(out, bytesOut, "<td><input type=text size=50 name=address value='" + address[0] + "'}></td>");
      scoutln(out, bytesOut, "<td><p>Example: <i>petertan@cybernetisp.com</i></td></tr>");

      scoutln(out, bytesOut, "<tr><td><p>Mail Display Name &nbsp;</td>");
      scoutln(out, bytesOut, "<td><input type=text size=50 name=desc value='" + desc[0] + "'></td>");
      scoutln(out, bytesOut, "<td><p>Example: <i>Peter Tan</i></td></tr>");

      scoutln(out, bytesOut, "<tr><td><p>Account Type &nbsp;</td><td><p>");
      buildTypeList(out, type[0], bytesOut);
      scoutln(out, bytesOut, "</td></tr>");
    }

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    if(account.length() == 0)
      scoutln(out, bytesOut, "<tr><td colspan=3><p><input type=image src=\"" + imagesDir + "go.gif\" name=U> Save New Account</td></tr>");
    else
    {
      scoutln(out, bytesOut, "<tr><td colspan=3><p><input type=image src=\"" + imagesDir + "go.gif\" name=U> Update Changes</td></tr>");
      scoutln(out, bytesOut, "<tr><td><p>or</td></tr>");
      scoutln(out, bytesOut, "<tr><td colspan=3><p><input type=image src=\"" + imagesDir + "go.gif\" name=D> Delete this Account</td></tr>");
    }

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "</table></form>");

    if(fromZaraAdmin)
      scoutln(out, bytesOut, "</body></html>");
    else scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir));
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void buildTypeList(PrintWriter out, String currentType, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<select name='type'>");
    
    scout(out, bytesOut, "<option value='POP3'");
    if(currentType.equals("POP3"))
      scout(out, bytesOut, " selected");
    scoutln(out, bytesOut, ">POP3");

    scout(out, bytesOut, "<option value='GMail'");
    if(currentType.equals("GMail"))
      scout(out, bytesOut, " selected");
    scoutln(out, bytesOut, ">GMail");

    scoutln(out, bytesOut, "</select>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
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
