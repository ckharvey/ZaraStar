// =======================================================================================================================================================================================================
// System: ZaraStar Admin: List Detailed Access Rights
// Module: AdminListDetailedAccessRights.java
// Author: C.K.Harvey
// Copyright (c) 1998-2008 Christopher Harvey. All Rights Reserved.
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

public class AdminListDetailedAccessRights extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  AdminUtils adminUtils = new AdminUtils();
  
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "AdminListDetailedAccessRights", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7004, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                    int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');
    String supportDefnsDir  = directoryUtils.getSupportDirs('D');
    
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 7004, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "AdminListDetailedAccessRights", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7004, bytesOut[0], 0, "ACC:");
      
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "AdminListDetailedAccessRights", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7004, bytesOut[0], 0, "SID:");
      
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    display(con, stmt, rs, out, req, unm, sid, uty, men, den, dnm, bnm, supportDefnsDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 7004, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    
    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void display(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty,
                       String men, String den, String dnm, String bnm, String supportDefnsDir, String localDefnsDir, String defnsDir, int[] bytesOut)
                       throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>List Detailed Access Rights</title>");

    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" 
                           + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");
    
    int[] hmenuCount = new int[1];

    adminUtils.outputPageFrame(con, stmt, rs, out, req, "AdminListDetailedAccessRights", "", "7004", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount, bytesOut);

    adminUtils.drawTitle(con, stmt, rs, req, out, "list Detailed Access Rights", "7004", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<table border=0 id=\"page\">");
 
    scoutln(out, bytesOut, "<tr id=\"pageColumn\"><td><p>Engine</td><td><p>Service</td><td nowrap><p>Description</td>");

    String[] users = new String[100]; // plenty
    int numUsers = getUsers(con, stmt, rs, users);
    
    String cssFormat = "line2";
    int x;
    for(x=0;x<numUsers;++x)
    {
      if(cssFormat.equals("line1"))
        cssFormat = "line2";
      else cssFormat = "line1";

      scout(out, bytesOut, "<td id=\"" + cssFormat + "\">" + users[x] + "</td>");
    }
    scoutln(out, bytesOut, "</tr>");
        
    byte[] rightsThisUser = new byte[2500];

    int y, z;
    x=1;
    String engine, service, desc, userDir, userCode;

    String s=" ";
    while(s.length() != 0)
    {
      s = generalUtils.getFromDefnFileByEntry(true, "", x, "scbs.dfn", supportDefnsDir, "");
      ++x;
      if(s.length() != 0)
      {
        if(s.charAt(0) != '#') // not a comment line
        {
          engine="";
          y=0;
          while(y < s.length() && s.charAt(y) != ' ' && s.charAt(y) != '\t')
            engine += s.charAt(y++);
          ++y;
          while(y < s.length() && (s.charAt(y) == ' ' || s.charAt(y) == '\t'))
            ++y;
          service="";
          while(y < s.length() && s.charAt(y) != ' ')
            service += s.charAt(y++);
          ++y;
          while(y < s.length() && (s.charAt(y) == ' ' || s.charAt(y) == '\t'))
            ++y;

          for(z=0;z<25;++z)
          {
            while(y < s.length() && s.charAt(y) != ' ')
              ++y;
            ++y;
            while(y < s.length() && (s.charAt(y) == ' ' || s.charAt(y) == '\t'))
              ++y;
          }

          desc="";
          while(y < s.length())
            desc += s.charAt(y++);
          
          if(cssFormat.equals("line1"))
            cssFormat = "line2";
          else cssFormat = "line1";

          scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\"><td><p>" + engine + "</td><td><p>" + service + "</td><td nowrap><p>" + desc + "</td>");

          for(z=0;z<numUsers;++z)
          {
            userCode = users[z];
            userDir = directoryUtils.getUserDir(dnm) + "/" + userCode + "/";
            aUserForAService(out, service, userDir, rightsThisUser, bytesOut);
          }

          scoutln(out, bytesOut, "</tr>");
        }
      }
    }
        
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "</table>");     scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private int getUsers(Connection con, Statement stmt, ResultSet rs, String[] users) throws Exception
  {
    int x=0;
    
    try
    {
      stmt = con.createStatement();
  
      rs = stmt.executeQuery("SELECT UserCode FROM profiles ORDER BY UserCode"); 

      String userCode;

      while(rs.next())
      {
        userCode = rs.getString(1);
        if(userCode.equals("Sysadmin") || userCode.startsWith("___"))//("Registered") || userCode.equals("Anonymous"))
          ;
        else users[x++] = userCode;
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
    
    return x;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void aUserForAService(PrintWriter out, String service, String userDir, byte[] rightsThisUser, int[] bytesOut) throws Exception
  {
    RandomAccessFile fhRightsThisUser = generalUtils.fileOpenD("z3u2.nfs", userDir);
    if(fhRightsThisUser == null) // create if exists not
      return;

    fhRightsThisUser.read(rightsThisUser, 0, 2500);
 
    scoutln(out, bytesOut, "<td align=center><p>");

    int serviceI = (generalUtils.strToInt(service) - 1);
    int i = (int)Math.IEEEremainder(serviceI, 8);
    if(i < 0)
      i = 8 + i;
    Double d = new Double(Math.pow(2, i));

    if((rightsThisUser[serviceI / 8] & (byte)(d.intValue())) == (byte)(d.intValue()))
      scout(out, bytesOut, "Y");
    scout(out, bytesOut, "</td>");
    
    generalUtils.fileClose(fhRightsThisUser);
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

