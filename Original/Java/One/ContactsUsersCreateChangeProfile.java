// =======================================================================================================================================================================================================
// System: ZaraStar: Users: create/change profile
// Module: ContactsUsersCreateChangeProfile.java
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

public class ContactsUsersCreateChangeProfile extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  Profile profile = new Profile();
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

      System.out.println("8831: " + e);
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "ContactsUsersCreateChangeProfile", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8831, bytesOut[0], 0, "ERR:");
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

    if((! serverUtils.isSysAdmin(con, stmt, rs, unm, dnm, sid, uty, localDefnsDir, defnsDir)) && (! serverUtils.isDBAdmin(con, stmt, rs, unm)))
    {
      messagePage.msgScreen(true, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "ContactsUsersCreateChangeProfile", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8831, bytesOut[0], 0, "ACC:");
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(true, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "ContactsUsersCreateChangeProfile", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8831, bytesOut[0], 0, "SID:");
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    set(con, stmt, rs, out, p1, unm, sid, uty, men, den, dnm, bnm, imagesDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 8831, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String p1, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String imagesDir, String localDefnsDir, String defnsDir,
                   int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>User Profile</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    if(serverUtils.isSysAdmin(con, stmt, rs, unm, dnm, sid, uty, localDefnsDir, defnsDir) || serverUtils.isDBAdmin(con, stmt, rs, unm))
    {
      if((p1.equals("___registered___") || p1.equals("___casual___")))
      {
        scoutln(out, bytesOut, "function rights(){");
        scoutln(out, bytesOut, "this.location.href=\"/central/servlet/_7009?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=" + p1 + "&bnm=" + bnm + "\";}");
      }
      else
      {
        scoutln(out, bytesOut, "function rights(){");
        scoutln(out, bytesOut, "this.location.href=\"/central/servlet/AdminUserModules?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=" + p1 + "&bnm=" + bnm + "\";}");
      }
    }
    
    scoutln(out, bytesOut, "function update(){document.forms[0].submit()}");
    
    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    scoutln(out, bytesOut, "</head><body>");

    signOnAdministrator.heading(out, true, unm, sid, uty, bnm, dnm, men, den, imagesDir, bytesOut);

    if(p1.length() == 0)
      dashboardUtils.drawTitle(out, "New User Profile", "8831", unm, sid, uty, men, den, dnm, bnm, bytesOut);
    else
    {
      if(p1.equals("___registered___"))
        dashboardUtils.drawTitle(out, "User Profile: <i>All Registered Users</i>", "8831", unm, sid, uty, men, den, dnm, bnm, bytesOut);
      else
      if(p1.equals("___casual___"))
        dashboardUtils.drawTitle(out, "User Profile: <i>All Casual Users</i>", "8831", unm, sid, uty, men, den, dnm, bnm, bytesOut);
      else dashboardUtils.drawTitle(out, "User Profile: " + p1, "8831", unm, sid, uty, men, den, dnm, bnm, bytesOut);
    }

    scoutln(out, bytesOut, "<form action=\"ContactsProfileUpdate\" enctype=\"application/x-www-form-urlencoded\" method=post>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"unm\" value='" + unm + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"sid\" value='" + sid + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"uty\" value='" + uty + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"men\" value='" + men + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"den\" value='" + den + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"dnm\" value='" + dnm + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"bnm\" value='" + bnm + "'>");

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
    String[] eMail           = new String[1];
    String[] bio             = new String[1];
    String[] isDBAdmin       = new String[1];
    String[] externalAccess  = new String[1];
    String[] customerCode    = new String[1];
    String[] supplierCode    = new String[1];
    String[] facebookCode    = new String[1];
    String[] facebookAccessToken = new String[1];
    String[] isSeniorSalesPerson = new String[1];
    String[] isEnquiriesSalesPerson = new String[1];
    String[] isSalesPerson          = new String[1];
    String[] dateOfBirth            = new String[1];
    String[] nationality            = new String[1];
    String[] userBasis            = new String[1];

    if(p1.length() == 0)
    {
      userName[0]        = "";
      passWord[0]        = ""; 
      jobTitle[0]        = "";
      status[0]          = "S";
      dateJoined [0]     = generalUtils.today(localDefnsDir, defnsDir);
      dateLeft[0]        = "";
      showInDirectory[0] = "N";
      officePhone[0]     = "";
      mobilePhone[0]     = "";
      fax[0]             = "";
      eMail[0]           = "";
      bio[0]             = "";
      isDBAdmin[0]       = "";
      externalAccess[0]  = "N";
      customerCode[0]           = "";
      supplierCode[0]           = "";
      facebookCode[0]           = "";
      facebookAccessToken[0]    = "";
      userBasis[0]           = "N";
      isSalesPerson[0]       = "N";
      isSeniorSalesPerson[0] = "N";
      isEnquiriesSalesPerson[0]   = "N";
      dateOfBirth[0]   = "";
      nationality[0]   = "";
    }
    else
    {
      profile.getProfile(p1, dnm, userName, passWord, status, facebookCode, facebookAccessToken, dateOfBirth, nationality);
      
      profile.getProfiled(p1, dnm, jobTitle, dateJoined, dateLeft, showInDirectory, officePhone, mobilePhone, fax, userBasis, eMail, bio, isDBAdmin, externalAccess, isSalesPerson, isSeniorSalesPerson, isEnquiriesSalesPerson, customerCode,
                        supplierCode);
    }

    scoutln(out, bytesOut, "<table id=\"page\" width=100% border=0>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    if(p1.length() == 0)
      scoutln(out, bytesOut, "<tr><td><p>UserCode:</td><td><p><input type=text name=userCode size=20 maxlength=20\"></td></tr>");
    else
    {
      String userCode;
      if(p1.equals("___registered___"))
        userCode = "<i>All Registered Users</i>";
      else
      if(p1.equals("___casual___"))
        userCode = "<i>All Casual Users</i>";
      else userCode = p1;
      scoutln(out, bytesOut, "<tr><td><p>UserCode:</td><td><p>" + userCode);
      
      if(serverUtils.isSysAdmin(con, stmt, rs, unm, dnm, sid, uty, localDefnsDir, defnsDir) || serverUtils.isDBAdmin(con, stmt, rs, unm))
      {
        scoutln(out, bytesOut, "&nbsp;&nbsp;&nbsp;&nbsp;<a href=\"javascript:rights()\">Set <i>access rights</i> like another user</a>");
      }

      scoutln(out, bytesOut, "<input type=\"hidden\" name=\"userCode\" value='" + p1 + "'></td></tr>");
    }

    if(! p1.equals("___registered___") && ! p1.equals("___casual___"))
    {
      scoutln(out, bytesOut, "<tr><td><p>UserName:</td><td><p><input type=text name=userName size=60 maxlength=60 value=\"" + userName[0] + "\"></td></tr>");
    
      scoutln(out, bytesOut, "<tr><td><p>PassWord:</td><td><p><input type=password name=passWord size=20 maxlength=40 value=\"" + passWord[0] + "\"></td></tr>");

      scoutln(out, bytesOut, "<tr><td><p>Job Title:</td><td><p><input type=text name=jobTitle size=60 maxlength=80 value=\"" + jobTitle[0] + "\"></td></tr>");
    }
    else
    {
      if(p1.equals("___registered___"))
        scoutln(out, bytesOut, "<tr><td><input type=hidden name=userName value=\"All Registered Users\"></td></tr>");
      else scoutln(out, bytesOut, "<tr><td><input type=hidden name=userName value=\"All Casual Users\"></td></tr>");
      
      scoutln(out, bytesOut, "<tr><td><input type=hidden name=passWord value=\"\"></td></tr>");
      scoutln(out, bytesOut, "<tr><td><input type=hidden name=jobTitle value=\"\"></td></tr>");
    }
    
    if(serverUtils.isSysAdmin(con, stmt, rs, unm, dnm, sid, uty, localDefnsDir, defnsDir) || serverUtils.isDBAdmin(con, stmt, rs, unm))
    {
      scoutln(out, bytesOut, "<tr><td valign=top><p>Status:</td>");
      scout(out, bytesOut, "<td nowrap><p><input type=radio name=status value=L");
      if(status[0].equals("L"))
        scout(out, bytesOut, " checked");
      scout(out, bytesOut, ">&nbsp;&nbsp;Live");
      scoutln(out, bytesOut, "<br><input type=radio name=status value=S");
      if(status[0].equals("S"))
        scout(out, bytesOut, " checked");
      scout(out, bytesOut, ">&nbsp;&nbsp;Suspended");
      scoutln(out, bytesOut, "<br><input type=radio name=status value=T");
      if(status[0].equals("T"))
        scout(out, bytesOut, " checked");
      scout(out, bytesOut, ">&nbsp;&nbsp;Terminated</td></tr>");
    }
    else
    {
      String s= "";
      if(status[0].equals("L"))
        s = "Live";
      else
      if(status[0].equals("S"))
        s = "Suspended";
      else
      if(status[0].equals("D"))
        s +=  "Terminated";
      
      scoutln(out, bytesOut, "<tr><td valign=top><p>Status:</td><td><p>" + s + "</td>");
      scoutln(out, bytesOut, "<td><input type=hidden name=status value=\'" + status[0] + "'></td></tr>");
    }

    if(! p1.equals("___registered___") && ! p1.equals("___casual___"))
    {
      scoutln(out, bytesOut, "<tr><td><p>Office Phone:</td><td><p><input type=text name=officePhone size=40 maxlength=40 value=\"" + officePhone[0] + "\"></td></tr>");
    
      scoutln(out, bytesOut, "<tr><td><p>Mobile Phone:</td><td><p><input type=text name=mobilePhone size=40 maxlength=40 value=\"" + mobilePhone[0] + "\"></td></tr>");
    
      scoutln(out, bytesOut, "<tr><td><p>Fax:</td><td><p><input type=text name=fax size=40 maxlength=40 value=\"" + fax[0] + "\"></td></tr>");

      if(serverUtils.isSysAdmin(con, stmt, rs, unm, dnm, sid, uty, localDefnsDir, defnsDir) || serverUtils.isDBAdmin(con, stmt, rs, unm))
      {
        scoutln(out, bytesOut, "<tr><td><p>Date Joined:</td><td><p><input type=text name=dateJoined size=10 maxlength=10 value=\"" + generalUtils.convertFromYYYYMMDD(dateJoined[0]) + "\"></td></tr>");
    
        scoutln(out, bytesOut, "<tr><td><p>Date Left:</td><td><p><input type=text name=dateLeft size=10 maxlength=10 value=\"" + generalUtils.convertFromYYYYMMDD(dateLeft[0]) + "\"></td></tr>");
    
        scout(out, bytesOut, "<tr><td nowrap><p>Show In Directory</td><td><p><input type=checkbox name=showInDirectory");
        if(showInDirectory[0].equals("Y"))
          scout(out, bytesOut, " checked");
        scoutln(out, bytesOut, "></td></tr>");

        scout(out, bytesOut, "<tr><td nowrap><p>Is DB Admin</td><td><p><input type=checkbox name=isDBAdmin");
        if(isDBAdmin[0].equals("Y"))
          scout(out, bytesOut, " checked");
        scoutln(out, bytesOut, "></td></tr>");

        scoutln(out, bytesOut, "<td><input type=hidden name=userBasis value='E'></td></tr>");

        scout(out, bytesOut, "<tr><td nowrap><p>Is SalesPerson</td><td><p><input type=checkbox name=isSalesPerson");
        if(isSalesPerson[0].equals("Y"))
          scout(out, bytesOut, " checked");
        scoutln(out, bytesOut, "></td></tr>");

        scout(out, bytesOut, "<tr><td nowrap><p>Is Senior SalesPerson</td><td><p><input type=checkbox name=isSeniorSalesPerson");
        if(isSeniorSalesPerson[0].equals("Y"))
          scout(out, bytesOut, " checked");
        scoutln(out, bytesOut, "></td></tr>");

        scout(out, bytesOut, "<tr><td nowrap><p>Is Enquiries SalesPerson</td><td><p><input type=checkbox name=isEnquiriesSalesPerson");
        if(isEnquiriesSalesPerson[0].equals("Y"))
          scout(out, bytesOut, " checked");
        scoutln(out, bytesOut, "></td></tr>");


        scout(out, bytesOut, "<tr><td nowrap><p>Is Allowed to Access Externally</td><td><p><input type=checkbox name=externalAccess");
        if(externalAccess[0].equals("Y"))
          scout(out, bytesOut, " checked");
        scoutln(out, bytesOut, "></td></tr>");
      }
      else
      {
        scoutln(out, bytesOut, "<tr><td><p>Date Joined:</td><td><p>" + generalUtils.convertFromYYYYMMDD(dateJoined[0]) + "</td></tr>");
    
        scoutln(out, bytesOut, "<tr><td><p>Date Left:</td><td><p>" + generalUtils.convertFromYYYYMMDD(dateLeft[0]) + "</td></tr>");
    
        String s;
        scout(out, bytesOut, "<tr><td nowrap><p>Show In Directory</td><td><p>");
        if(showInDirectory[0].equals("Y"))
        {
          s = "Yes";
          showInDirectory[0] = "on";
        }
        else
        {
          s = "No";
          showInDirectory[0] = "off";
        }
        scoutln(out, bytesOut, s + "</td></tr>");

        scout(out, bytesOut, "<tr><td nowrap><p>Is DB Admin</td><td><p>");
        if(isDBAdmin[0].equals("Y"))
        {
          s = "Yes";
          isDBAdmin[0] = "on";
        }
        else
        {
          s = "No";
          isDBAdmin[0] = "off";
        }
        scoutln(out, bytesOut, s + "</td></tr>");

        scoutln(out, bytesOut, "<tr><td><input type=hidden name=dateJoined value=\"" + generalUtils.convertFromYYYYMMDD(dateJoined[0]) + "\"></td></tr>");
        scoutln(out, bytesOut, "<tr><td><input type=hidden name=dateLeft value=\"" + generalUtils.convertFromYYYYMMDD(dateLeft[0]) + "\"></td></tr>");
        scoutln(out, bytesOut, "<tr><td><input type=hidden name=showInDirectory value=\"" + showInDirectory[0] + "\"></td></tr>");
        scoutln(out, bytesOut, "<tr><td><input type=hidden name=externalAccess value=\"" + externalAccess[0] + "\"></td></tr>");
      }
    
      scoutln(out, bytesOut, "<tr><td><p>eMail:</td><td><p><input type=text name=eMail size=60 maxlength=60 value=\"" + eMail[0] + "\"></td></tr>");

      scoutln(out, bytesOut, "<tr><td><p>Customer Code:</td><td><p><input type=text name=customerCode size=20 maxlength=20 value=\"" + customerCode[0] + "\"></td></tr>");

      scoutln(out, bytesOut, "<tr><td><p>Supplier Code:</td><td><p><input type=text name=supplierCode size=20 maxlength=20 value=\"" + supplierCode[0] + "\"></td></tr>");

//      scoutln(out, bytesOut, "<tr><td><p>Facebook:</td><td><p><input type=text name=facebookCode size=60 maxlength=100 value=\"" + facebookCode[0] + "\"></td></tr>");

//      scoutln(out, bytesOut, "<tr><td><p>LinkedIn:</td><td><p><input type=text name=linkedInCode size=60 maxlength=100 value=\"" + linkedInCode[0] + "\"></td></tr>");
        scoutln(out, bytesOut, "<td><input type=hidden name=userBasis value='E'></td></tr>");
    }
    else
    {
      scoutln(out, bytesOut, "<tr><td><input type=hidden name=officePhone value=\"\"></td></tr>");
      scoutln(out, bytesOut, "<tr><td><input type=hidden name=mobilePhone value=\"\"></td></tr>");
      scoutln(out, bytesOut, "<tr><td><input type=hidden name=fax size=40 maxlength=40 value=\"\"></td></tr>");
      scoutln(out, bytesOut, "<tr><td><input type=hidden name=dateJoined value=\"01.01.1970\"></td></tr>");
      scoutln(out, bytesOut, "<tr><td><input type=hidden name=dateLeft value=\"01.01.1970\"></td></tr>");
      scoutln(out, bytesOut, "<tr><td><input type=hidden name=eMail value=\"\"></td></tr>");
        scoutln(out, bytesOut, "<td><input type=hidden name=userBasis value='E'></td></tr>");
    }
    
    if(! p1.equals("___registered___") && ! p1.equals("___casual___"))
      scoutln(out, bytesOut, "<tr><td valign=top nowrap><p>Bio:</td><td><p><textarea name=bio cols=90 rows=15>" + bio[0] + "</textarea></td></tr>");
    else scoutln(out, bytesOut, "<tr><td><input type=hidden name=bio value=''></td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td><p><a href=\"javascript:update()\">Update Profile</a></td></tr>");

    scoutln(out, bytesOut, "</table></form></body></html>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
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
