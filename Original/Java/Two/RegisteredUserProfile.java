// =======================================================================================================================================================================================================
// System: ZaraStar: Registered Users: create/change profile
// Module: RegisteredUserProfile.java
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

public class RegisteredUserProfile extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  Profile profile = new Profile();
  AdminControlUtils adminControlUtils = new AdminControlUtils();

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
      p1  = req.getParameter("p1");

      if(p1 == null) p1 = "";

      doIt(out, req, p1, unm, sid, uty, men, den, dnm, bnm, bytesOut);
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

      System.out.println("906: " + e);
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "RegisteredUserProfile", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 906, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String p1, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    long startTime = new java.util.Date().getTime();

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! adminControlUtils.notDisabled(con, stmt, rs, 906))
    {
      messagePage.msgScreen(true, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "RegisteredUserProfile", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 906, bytesOut[0], 0, "ACC:");
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(true, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "RegisteredUserProfile", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 906, bytesOut[0], 0, "SID:");
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    set(con, stmt, rs, req, out, p1, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 906, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, PrintWriter out, String p1, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir,
                   int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>User Profile</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    scoutln(out, bytesOut, "function update(){document.forms[0].submit()}");

    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    int[] hmenuCount = new int[1];

    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "906", "", "RegisteredUserProfile", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    String[] name         = new String[1];
    String[] companyName  = new String[1];
    String[] accessRights = new String[1];

    int i = 0;

    if(p1.length() == 0)
      pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Create Profile Information", "906", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);
    else
    {
      i = p1.indexOf("_");
      profile.getExternalAccessNameCompanyAndRights(con, stmt, rs, unm.substring(0, i), name, companyName, accessRights);
      pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Change Profile Information for " + name[0] + " of " + companyName[0], "906", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);
    }

    scoutln(out, bytesOut, "<form action=\"ExternalContacts\" enctype=\"application/x-www-form-urlencoded\" method=post>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"unm\" value='" + unm + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"sid\" value='" + sid + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"uty\" value='" + uty + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"men\" value='" + men + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"den\" value='" + den + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"dnm\" value='" + dnm + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"bnm\" value='" + bnm + "'>");

    if(p1.length() > 0)
      p1 = p1.substring(0, i);

    String[] contactCode      = new String[1];
    String[] owner            = new String[1];
    String[] eMail            = new String[1];
    String[] userCode         = new String[1];
    String[] domain           = new String[1];
    String[] jobTitle         = new String[1];
    String[] notes            = new String[1];
    String[] customerCode     = new String[1];
    String[] supplierCode     = new String[1];
    String[] organizationCode = new String[1];
    String[] externalPassWord = new String[1];
    String[] externalRights   = new String[1];
    String[] externalApproved = new String[1];
    String[] phone1           = new String[1];
    String[] phone2           = new String[1];
    String[] phone3           = new String[1];
    String[] fax              = new String[1];
    String[] mailingList      = new String[1];

    String newOrEdit;

    if(p1.length() == 0)
    {
      companyName[0]      = "";
      name[0]             = "";
      owner[0]            = "";
      externalPassWord[0] = generalUtils.generatePassWord();
      jobTitle[0]         = "";
      notes[0]            = "";
      phone1[0]           = "";
      phone2[0]           = "";
      phone3[0]           = "";
      fax[0]              = "";
      eMail[0]            = "";
      externalApproved[0] = "N";
      newOrEdit           = "N";
      mailingList[0]      = "Y";
    }
    else
    {
      profile.getContactGivenExternalCode(p1, contactCode, owner, name, companyName, eMail, domain, userCode, jobTitle, notes, customerCode, supplierCode, organizationCode, externalPassWord, externalRights, externalApproved, phone1, phone2,
                                        phone3, fax, mailingList, dnm, localDefnsDir, defnsDir);
      newOrEdit = "E";
    }

    scoutln(out, bytesOut, "<table id=\"page\" width=100% border=0>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"hiddenExternalCode\" value='" + p1 + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"hiddenExternalApproved\" value='" + externalApproved[0] + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"hiddenContactCode\" value='" + contactCode[0] + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"newOrEdit\" value='" + newOrEdit + "'>");

    scoutln(out, bytesOut, "<tr><td nowrap><p>External Access Code:</td><td><p>" + p1 + "</td></tr>");

    scoutln(out, bytesOut, "<tr><td><p>Person Name:</td><td colspan=3><p><input type=text name=name size=60 maxlength=60 value=\"" + name[0] + "\"></td></tr>");

    scoutln(out, bytesOut, "<tr><td><p>PassWord:</td><td colspan=3><p><input type=text name=passWord size=20 maxlength=20 value=\"" + externalPassWord[0] + "\"></td></tr>");

    scoutln(out, bytesOut, "<tr><td><p>Company Name:</td><td colspan=3><p><input type=text name=companyName size=60 maxlength=100 value=\"" + companyName[0] + "\"></td></tr>");

    scoutln(out, bytesOut, "<tr><td><p>Job Title:</td><td colspan=3><p><input type=text name=jobTitle size=60 maxlength=80 value=\"" + jobTitle[0] + "\"></td></tr>");

    scoutln(out, bytesOut, "<tr><td><p>eMail:</td><td colspan=3><p><input type=text name=eMail size=60 maxlength=60 value=\"" + eMail[0] + "\"></td></tr>");

    scoutln(out, bytesOut, "<tr><td><p>Phones:</td><td colspan=3><p><input type=text name=phone1 size=17 maxlength=30 value=\"" + phone1[0] + "\">");
    scoutln(out, bytesOut, "&nbsp;&nbsp;<input type=text name=phone2 size=17 maxlength=30 value=\"" + phone2[0] + "\">");
    scoutln(out, bytesOut, "&nbsp;&nbsp;<input type=text name=phone3 size=17 maxlength=30 value=\"" + phone3[0] + "\"></td></tr>");

    scoutln(out, bytesOut, "<tr><td><p>Fax:</td><td colspan=3><p><input type=text name=fax size=17 maxlength=30 value=\"" + fax[0] + "\"></td></tr>");

    scoutln(out, bytesOut, "<tr><td><p>Contact:</td><td colspan=3><p>");
    ourUsers(con, stmt, rs, out, owner[0], bytesOut);
    scoutln(out, bytesOut, "</td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap><p>Mailing List:</td><td><p><input type=checkbox name=mailingList ");
    if(mailingList[0].equals("Y"))
      scoutln(out, bytesOut, " checked");
    scoutln(out, bytesOut, "></td></tr>");

    scoutln(out, bytesOut, "<tr><td valign=top nowrap><p>Notes:</td><td colspan=2><p><textarea name=notes cols=60 rows=10>" + notes[0] + "</textarea></td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td><p><a href=\"javascript:update()\">Update Profile</a></td></tr>");

    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir));
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {
    out.println(str);
    bytesOut[0] += (str.length() + 2);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void ourUsers(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String current, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<select name='owner'>");

    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT UserCode, UserName FROM profiles WHERE ( Status = 'L' OR Status = 'S' ) AND ShowInDirectory = 'Y' ORDER BY UserName");

      String userCode, userName;

      while(rs.next())
      {
        userCode = rs.getString(1);

        if(! userCode.equals("___registered___") && ! userCode.equals("___casual___") && ! userCode.equals("Sysadmin"))
        {
          userName = rs.getString(2);
          scoutln(out, bytesOut, "<option value=\"" + userCode + "\"");
          if(userCode.equals(current))
            scoutln(out, bytesOut, " selected");
          scoutln(out, bytesOut, ">" + userName);
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
