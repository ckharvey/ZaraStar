// =======================================================================================================================================================================================================
// System: ZaraStar: Users: view profile (not internal)
// Module: ContactsProfileView.java
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

public class ContactsProfileView extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  Profile profile = new Profile();
  DashboardUtils dashboardUtils = new DashboardUtils();
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
      p1  = req.getParameter("p1"); // unm

      if(p1 == null) p1 = unm;

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

      System.out.println("8832: " + e);
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "ContactsProfileView", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8832, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String p1, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir    = directoryUtils.getLocalOverrideDir(dnm);
    String imagesLibraryDir = directoryUtils.getImagesDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 128, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      if(! uty.equals("I") && ! ( uty.equals("R") && adminControlUtils.notDisabled(con, stmt, rs, 909)) && ( uty.equals("A") && adminControlUtils.notDisabled(con, stmt, rs, 809)))
      {
        messagePage.msgScreen(true, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "ContactsProfileView", imagesDir, localDefnsDir, defnsDir, bytesOut);
        serverUtils.etotalBytes(req, unm, dnm, 8832, bytesOut[0], 0, "ACC:");
        if(con != null) con.close();
        if(out != null) out.flush();
        return;
      }
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(true, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "ContactsProfileView", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8832, bytesOut[0], 0, "SID:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    set(con, stmt, rs, req, out, p1, unm, sid, uty, men, den, dnm, bnm, imagesLibraryDir, imagesDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 8832, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, PrintWriter out, String p1, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String imagesLibraryDir, String imagesDir,
                   String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>User Profile</title>");

    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    int[] hmenuCount = new int[1];
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "8832", "", "ContactsProfileView", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    if(p1.equals("Sysadmin"))
    {
      dashboardUtils.drawTitle(out, "User Profile: <i>System Administrator</i>", "8832", unm, sid, uty, men, den, dnm, bnm, bytesOut);

      scoutln(out, bytesOut, "<table id='page' width=100% border=0>");
      scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
      scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
      scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
      scoutln(out, bytesOut, "<tr><td><p>No Profile for the System Administrator</td></tr>");
      scoutln(out, bytesOut, "</table>");
      scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir));
      return;
    }

    String[] userName        = new String[1];
    String[] passWord        = new String[1];
    String[] jobTitle        = new String[1];
    String[] status          = new String[1];
    String[] dateJoined      = new String[1];
    String[] dateLeft        = new String[1];
    String[] showInDirectory = new String[1];
    String[] officePhone     = new String[1];
    String[] mobilePhone     = new String[1];
    String[] fax             = new String[1];
    String[] image           = new String[1];
    String[] eMail           = new String[1];
    String[] bio             = new String[1];
    String[] isDBAdmin       = new String[1];
    String[] externalAccess  = new String[1];
    String[] customerCode    = new String[1];
    String[] supplierCode    = new String[1];
    String[] facebookCode    = new String[1];
    String[] facebookAccessToken    = new String[1];
    String[] dateOfBirth       = new String[1];
    String[] nationality      = new String[1];
    String[] isSalesPerson          = new String[1];
    String[] isSeniorSalesPerson    = new String[1];
    String[] isEnquiriesSalesPerson = new String[1];
    String[] userBasis                  = new String[1];

      profile.getProfile(p1, dnm, userName, passWord, status, facebookCode, facebookAccessToken, dateOfBirth, nationality);
      
      profile.getProfiled(p1, dnm, jobTitle, dateJoined, dateLeft, showInDirectory, officePhone, mobilePhone, fax, userBasis, eMail, bio, isDBAdmin, externalAccess, isSalesPerson, isSeniorSalesPerson, isEnquiriesSalesPerson, customerCode,
                        supplierCode);

    scoutln(out, bytesOut, "<table id=\"page\" width=100% border=0>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    if(image[0].length() > 0 && generalUtils.fileExists(imagesLibraryDir + image[0]))
      scoutln(out, bytesOut, "<tr><td><img src=\"http://" + men + imagesLibraryDir + image[0] + "\" border=2></td></tr>");
   
    scoutln(out, bytesOut, "<tr><td><p>UserCode:</td><td width=99%><p>" + p1 + "</td></tr>");

    scoutln(out, bytesOut, "<tr><td><p>UserName:</td><td><p>" + userName[0] + "</td></tr>");

    scoutln(out, bytesOut, "<tr><td><p>Job Title:</td><td><p>" + jobTitle[0] + "</td></tr>");

    scoutln(out, bytesOut, "<tr><td><p>eMail:</td><td><p>" + eMail[0] + "</td></tr>");

    scoutln(out, bytesOut, "</td></tr><tr><td valign=top nowrap><p>Bio:</td><td><p>" + bio[0] + "</td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "</table>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir));
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {
    out.println(str);
    bytesOut[0] += (str.length() + 2);
  }

}
