// =======================================================================================================================================================================================================
// System: ZaraStar RAT: Create/Edit Issue
// Module: RATCreateEditIssue.java
// Author: C.K.Harvey
// Copyright (c) 2002-09 Christopher Harvey. All Rights Reserved.
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

public class RATCreateEditIssue extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  IssuesUtils issuesUtils = new IssuesUtils();
  AdminControlUtils adminControlUtils = new AdminControlUtils();

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
      p1  = req.getParameter("p1"); // code
      p2  = req.getParameter("p2"); // category
      
      if(p1 == null) p1 = "";
      
      doIt(out, req, p1, p2, unm, sid, uty, men, den, dnm, bnm, bytesOut);
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
 
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "RATCreateEditIssue", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 5905, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String p1, String p2, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;
    
    if(! (uty.equals("R") && adminControlUtils.notDisabled(con, stmt, rs, 923) || ((uty.equals("I") && authenticationUtils.verifyAccess(con, stmt, rs, req, 5905, unm, uty, dnm, localDefnsDir, defnsDir)))))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "RATCreateEditIssue", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 5905, bytesOut[0], 0, "ACC:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "RATCreateEditIssue", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 5905, bytesOut[0], 0, "SID:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }
        
    set(con, stmt, rs, out, req, p1, p2, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);
    
    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 5905, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String p1, String p2, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir,
                   String defnsDir, int[] bytesOut) throws Exception
  {
    if(p1.length() == 0)
      scoutln(out, bytesOut, "<html><head><title>Create Issue</title>");
    else scoutln(out, bytesOut, "<html><head><title>Edit Issue</title>");
   
    String cssDirectory = directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir);
            
    scoutln(out, bytesOut, "<script language=\"JavaScript\" type=\"text/javascript\" src=\"" + directoryUtils.getEditorDirectory() + "editor.js\"></script>"); 

    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + cssDirectory + "general.css\">");
    
    int[] hmenuCount = new int[1];

    issuesUtils.outputPageFrame(con, stmt, rs, out, req, "RATCreateEditIssue", "5905", "", "document.getElementById('wikiview').contentWindow.document.designMode='on';", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    if(p1.length() == 0)
      issuesUtils.drawTitle(con, stmt, rs, req, out, "New Issue in: " + p2, "5905", p1, true, true, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
    else issuesUtils.drawTitle(con, stmt, rs, req, out, "Edit Issue " + p1 + ", in: " + p2, "5905", p1, true, true, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    boolean canEditStuff = false;
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 5903, unm, uty, dnm, localDefnsDir, defnsDir))
      canEditStuff = true;

    scoutln(out, bytesOut, "<table id='page' cellspacing=2 cellpadding=0 width=100% border=0>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    String categoryType = issuesUtils.getTypeGivenName(p2, dnm);

    String[] ratCode      = new String[1];
    String[] title        = new String[1];
    String[] date         = new String[1];
    String[] desc         = new String[1];
    String[] userCode     = new String[1];
    String[] userCompany  = new String[1];
    String[] release      = new String[1];
    String[] category     = new String[1];
    String[] type         = new String[1];
    String[] status       = new String[1];
    String[] fixRelease   = new String[1];
    String[] dateClosed   = new String[1];
    String[] dateTargeted = new String[1];
    String[] duplicateOf  = new String[1];
    String[] priority     = new String[1];
    String[] hoursSpent   = new String[1];
    String[] closedByUser = new String[1];
    String[] isPrivate    = new String[1];
    String[] service      = new String[1];

    if(p1.length() == 0) // new
    {
      ratCode[0] = title[0] = desc[0] = userCompany[0] = release[0] = type[0] = status[0] = fixRelease[0] = dateClosed[0] = dateTargeted[0] = duplicateOf[0] = priority[0] = hoursSpent[0] = closedByUser[0] = "";
      date[0] = generalUtils.todaySQLFormat(localDefnsDir, defnsDir);
      isPrivate[0] = "N";
      userCode[0] = unm;
      category[0] = p2;
    }
    else 
    {
      ratCode[0] = p1;
      issuesUtils.getRATGivenRATCode(p1, dnm, title, desc, date, userCode, userCompany, release, category, type, status, fixRelease, dateClosed, dateTargeted, duplicateOf, priority, hoursSpent, closedByUser, isPrivate, service);
    }

    if(uty.equals("R"))
      userCode[0] = userCode[0].substring(0, (userCode[0].length() - 1));

    scoutln(out, bytesOut, "<input type=hidden name=callingServlet value='5905'>");

    if(p1.length() > 0) // edit
    {
      scoutln(out, bytesOut, "<input type=hidden name=ratCode value=\"" + ratCode[0] + "\">");
      scoutln(out, bytesOut, "<tr><td nowrap><p>Issue Code:</p></td><td><p>" + ratCode[0] + "</p></td></tr>");
    }

    if(categoryType.equals("P") || categoryType.equals("R")) // Problem || Request
    {
      scoutln(out, bytesOut, "<tr><td><p>Title:</p></td><td colspan=3><p><input type=text size=40 maxlength=100 name=title value=\"" + title[0] + "\"></p></td>");
      if(canEditStuff)
        scoutln(out, bytesOut, "<td><p>Status: </p></td><td><p>" + showStatusDDL(status[0]) + "</p></td></tr>");
      else
      {
        scoutln(out, bytesOut, "<td><p>Status: </p></td><td><p>Open</p></td></tr>");
        scoutln(out, bytesOut, "<input name='status' type=hidden value='O'>");
      }

      if(canEditStuff)
        scoutln(out, bytesOut, "<tr><td><p>Category: </p></td><td colspan=3><p>" + showCategoryDDL(category[0], dnm) + "</p></td>");
      else
      {
        scoutln(out, bytesOut, "<td><p>Category: </p></td><td><p>" + p2 + "</p></td>");
        scoutln(out, bytesOut, "<input name='category' type=hidden value='O'>");
      }

      if(categoryType.equals("P")) // Problem
        scoutln(out, bytesOut, "<td><p>Type: </p></td><td><p>" + showTypeDDL(type[0], dnm) + "</p></td>");
      scoutln(out, bytesOut, "</tr>");

      if(canEditStuff)
      {
        scoutln(out, bytesOut, "<tr><td nowrap><p>Closed By: </p></td><td colspan=3><p><input type=text size=15 maxlength=20 name=closedByUser value=\"" + closedByUser[0] + "\"></p></td>");
        if(dateClosed[0].equals("1970-01-01"))
          dateClosed[0] = "";
        scoutln(out, bytesOut, "<td nowrap><p>Date Closed: </p></td><td><p><input type=text size=10 maxlength=10 name=dateClosed value=\"" + dateClosed[0] + "\"></p></td></tr>");
      }

      if(canEditStuff)
      {
        scoutln(out, bytesOut, "<tr><td nowrap><p>Submitted By: </p></td><td colspan=3><p><input type=text size=15 maxlength=20 name=userCode value=\"" + userCode[0]
                             + "\"></td><td><p>Company: </td><td></p><input type=text size=30 maxlength=40 name=userCompany value=\"" + userCompany[0] + "\"></p></td></tr>");
      }
      else
      {
        scoutln(out, bytesOut, "<tr><td nowrap><p>Submitted By: </p></td><td colspan=3><p>" + userCode[0] + "</td><td><p>Company: </td><td></p><input type=text size=20 maxlength=40 name=userCompany value=\"" + userCompany[0] + "\"></p></td></tr>");
        scoutln(out, bytesOut, "<input name='userCode' type=hidden value='" + userCode[0] + "''>");
      }

      if(canEditStuff)
        scoutln(out, bytesOut, "<tr><td><p>Date Submitted: </p></td><td><p><input type=text size=10 maxlength=10 name=dateSubmitted value=\"" + date[0] + "\"></p></td></tr>");
      else
      {
        scoutln(out, bytesOut, "<tr><td><p>Date Submitted: </p></td><td><p>" + date[0] + "</p></td></tr>");
        scoutln(out, bytesOut, "<input name='dateSubmitted' type=hidden value='" + date[0] + "''>");
      }

      if(canEditStuff)
      {
        scoutln(out, bytesOut, "<tr><td><p>Release: </p></td><td colspan=3><p><input type=text size=20 maxlength=20 name=release value=\"" + release[0] + "\"></p></td>");
        if(duplicateOf[0].equals("0"))
          duplicateOf[0] = "";
        scoutln(out, bytesOut, "<td nowrap><p>Duplicate Of:</p></td><td><p><input type=text size=10 maxlength=10 name=duplicateOf value=\"" + duplicateOf[0] + "\"></p></td></tr>");
      
        scoutln(out, bytesOut, "<tr><td><p>Priority: </p></td><td colspan=3><p>" + showPriorityDDL(priority[0]) + "</p></td>");
        if(dateTargeted[0].equals("1970-01-01"))
          dateTargeted[0] = "";
        scoutln(out, bytesOut, "<td nowrap><p>Date Targeted: </p></td><td><p><input type=text size=10 maxlength=10 name=dateTargeted value=\"" + dateTargeted[0] + "\"></p></td></tr>");

        if(dateClosed[0].equals("1970-01-01"))
          dateClosed[0] = "";

        scoutln(out, bytesOut, "<tr><td nowrap><p>Fixed in Release: </p></td><td colspan=3><p><input type=text size=10 maxlength=20 name=fixRelease value=\"" + fixRelease[0] + "\"></p></td>");
        scoutln(out, bytesOut, "<td nowrap><p>Hours Spent: </p></td><td><p><input type=text size=5 maxlength=10 name=hoursSpent value=\"" + hoursSpent[0] + "\"></p></td></tr>");
      }
    }
    else // Discussion
    {
      scoutln(out, bytesOut, "<tr><td><p>Title:</p></td><td colspan=3><p><input type=text size=40 maxlength=100 name=title value=\"" + title[0] + "\"></p></td>");
      if(canEditStuff)
        scoutln(out, bytesOut, "<td><p>Status: </p></td><td><p>" + showStatusDDL(status[0]) + "</p></td></tr>");
      else
      {
        scoutln(out, bytesOut, "<td><p>Status: </p></td><td><p>Open</p></td></tr>");
        scoutln(out, bytesOut, "<input name='status' type=hidden value='O'>");
      }

      if(canEditStuff)
        scoutln(out, bytesOut, "<tr><td><p>Category: </p></td><td colspan=3><p>" + showCategoryDDL(category[0], dnm) + "</p></td>");
      else
      {
        scoutln(out, bytesOut, "<td><p>Category: </p></td><td><p>" + p2 + "</p></td></tr>");
        scoutln(out, bytesOut, "<input name='category' type=hidden value='O'>");
      }

      if(canEditStuff)
      {
        scoutln(out, bytesOut, "<tr><td nowrap><p>Closed By: </p></td><td colspan=3><p><input type=text size=15 maxlength=20 name=closedByUser value=\"" + closedByUser[0] + "\"></p></td>");
        if(dateClosed[0].equals("1970-01-01"))
          dateClosed[0] = "";
        scoutln(out, bytesOut, "<td nowrap><p>Date Closed: </p></td><td><p><input type=text size=10 maxlength=10 name=dateClosed value=\"" + dateClosed[0] + "\"></p></td></tr>");
      }

      if(canEditStuff)
      {
        scoutln(out, bytesOut, "<tr><td nowrap><p>Submitted By: </p></td><td colspan=3><p><input type=text size=15 maxlength=20 name=userCode value=\"" + userCode[0]
                             + "\"></td><td><p>Company: </td><td></p><input type=text size=30 maxlength=40 name=userCompany value=\"" + userCompany[0] + "\"></p></td></tr>");
      }
      else
      {
        scoutln(out, bytesOut, "<tr><td nowrap><p>Submitted By: </p></td><td colspan=3><p>" + userCode[0] + "</td><td><p>Company: </td><td></p><input type=text size=20 maxlength=40 name=userCompany value=\"" + userCompany[0] + "\"></p></td></tr>");
        scoutln(out, bytesOut, "<input name='userCode' type=hidden value='" + userCode[0] + "''>");
      }
    }

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td colspan=6><textarea id='text1' name='text1' cols='100' rows='20'>" + desc[0] + "</textarea>");
    scoutln(out, bytesOut, "<script language='JavaScript'>drawEditor('text1', 'wikiview', '100%', '400', '#FF8888', '#F0F0F0', '#880000','" + unm + "','" + sid + "','" + uty + "','" + men + "','" + den + "','" + dnm + "','" + bnm
                         + "');</script></td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String showStatusDDL(String current) throws Exception
  {
    String s = "<select name='status'>";

    if(current.equals("O"))
      s += "<option selected value=\"O\">Open";
    else s += "<option value=\"O\">Open";

    if(current.equals("C"))
      s += "<option selected value=\"C\">Closed";
    else s += "<option value=\"C\">Closed";

    if(current.equals("A"))
      s += "<option selected value=\"F\">Awaiting Feedback";
    else s += "<option value=\"F\">Awaiting Feedback";

    s += "</select>";

    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String showTypeDDL(String current, String dnm) throws Exception
  {
    String s = "<select name='type'>";

    Connection con = null;
    Statement stmt = null;
    ResultSet rs = null;

    try
    {
      String uName = directoryUtils.getMySQLUserName();
      String pWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();

      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_info?user=" + uName + "&password=" + pWord);

      stmt = con.createStatement();

      String q = "SELECT Name FROM ratprob ORDER BY Position";

      rs = stmt.executeQuery(q);

      String name;

      while(rs.next())
      {
        name = rs.getString(1);

        if(current.equals(name))
          s += "<option value=\"" + name + "\" selected>" + name;
        else s += "<option value=\"" + name + "\">" + name;
      }

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }

    s += "</select>";

    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String showPriorityDDL(String current) throws Exception
  {
    String s = "<select name='priority'>";

    if(current.equals("U"))
      s += "<option selected value=\"U\">Unassigned";
    else s += "<option value=\"U\">Unassigned";

    if(current.equals("L"))
      s += "<option selected value=\"L\">Low";
    else s += "<option value=\"L\">Low";

    if(current.equals("M"))
      s += "<option selected value=\"M\">Medium";
    else s += "<option value=\"M\">Medium";

    if(current.equals("H"))
      s += "<option selected value=\"H\">High";
    else s += "<option value=\"H\">High";

    if(current.equals("C"))
      s += "<option selected value=\"C\">Critical";
    else s += "<option value=\"C\">Critical";

    s += "</select>";

    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String showCategoryDDL(String current, String dnm) throws Exception
  {
    String s = "<select name='category'>";

    Connection con = null;
    Statement stmt = null;
    ResultSet rs = null;

    try
    {
      String uName = directoryUtils.getMySQLUserName();
      String pWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();

      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_info?user=" + uName + "&password=" + pWord);

      stmt = con.createStatement();

      String q = "SELECT Name FROM ratcat ORDER BY Position";

      rs = stmt.executeQuery(q);

      String name;

      while(rs.next())
      {
        name = rs.getString(1);

        if(current.equals(name))
          s += "<option value=\"" + name + "\" selected>" + name;
        else s += "<option value=\"" + name + "\">" + name;
      }

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }

    s += "</select>";

    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {
    out.println(str);
    bytesOut[0] += str.length() + 2;
  }

}
