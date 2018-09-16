// =============================================================================================================================================================
// System: ZaraStar Admin: Services Upload
// Module: ServicesUpload.java
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

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.sql.*;

public class ServicesUpload extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  AdminDataBaseFixedFilesCreate adminDataBaseFixedFilesCreate = new AdminDataBaseFixedFilesCreate();
  
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "ZaracloudDataBase", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 9015, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut)
                    throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir        = directoryUtils.getSupportDirs('D');
    String localDefnsDir   = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir       = directoryUtils.getSupportDirs('I');
    String supportDefnsDir = directoryUtils.getSupportDirs('D');
    
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 9015, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "ZaracloudDataBase", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 9015, bytesOut[0], 0, "ACC:");
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "ZaracloudDataBase", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 9015, bytesOut[0], 0, "SID:");
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }
   
    process(supportDefnsDir);
    
    messagePage.msgScreen(false, out, req, 27, unm, sid, uty, men, den, dnm, bnm, "ZaracloudDataBase", imagesDir, localDefnsDir, defnsDir, bytesOut);
    
    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 9015, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con  != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void process(String supportDefnsDir) throws Exception
  {
    String uName    = directoryUtils.getMySQLUserName();
    String passWord = directoryUtils.getMySQLPassWord();
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/Zaracloud_scbs?user=" + uName + "&password=" + passWord);
    Statement stmt = null;

    int x=1, y;
    String category, service, desc, erp, mrp, mail, library, site, channels, admin, hidden, casual, registered;
    
    String s=" ";
    while(s.length() != 0)
    {
      s = generalUtils.getFromDefnFileByEntry(true, "", x, "scbs.dfn", supportDefnsDir, "");

      ++x;
      if(s.length() != 0)
      {
        if(s.charAt(0) != '#') // not a comment line
        {
          category="";
          y=0;
          while(y < s.length() && s.charAt(y) != ' ' && s.charAt(y) != '\t')
            category += s.charAt(y++);

          ++y;
          while(y < s.length() && (s.charAt(y) == ' ' || s.charAt(y) == '\t'))
            ++y;
          service="";
          while(y < s.length() && s.charAt(y) != ' ')
            service += s.charAt(y++);
          
          ++y;
          while(y < s.length() && (s.charAt(y) == ' ' || s.charAt(y) == '\t'))
            ++y;
          hidden="";
          while(y < s.length() && s.charAt(y) != ' ')
            hidden += s.charAt(y++);

          ++y;
          while(y < s.length() && (s.charAt(y) == ' ' || s.charAt(y) == '\t'))
            ++y;
          erp="";
          while(y < s.length() && s.charAt(y) != ' ')
            erp += s.charAt(y++);

          ++y;
          while(y < s.length() && (s.charAt(y) == ' ' || s.charAt(y) == '\t'))
            ++y;
          mrp="";
          while(y < s.length() && s.charAt(y) != ' ')
            mrp += s.charAt(y++);
            
          ++y;
          while(y < s.length() && (s.charAt(y) == ' ' || s.charAt(y) == '\t'))
            ++y;
          mail="";
          while(y < s.length() && s.charAt(y) != ' ')
            mail += s.charAt(y++);
            
          ++y;
          while(y < s.length() && (s.charAt(y) == ' ' || s.charAt(y) == '\t'))
            ++y;
          library="";
          while(y < s.length() && s.charAt(y) != ' ')
            library += s.charAt(y++);
            
          ++y;
          while(y < s.length() && (s.charAt(y) == ' ' || s.charAt(y) == '\t'))
            ++y;
          site="";
          while(y < s.length() && s.charAt(y) != ' ')
            site += s.charAt(y++);
            
          ++y;
          while(y < s.length() && (s.charAt(y) == ' ' || s.charAt(y) == '\t'))
            ++y;
          channels="";
          while(y < s.length() && s.charAt(y) != ' ')
            channels += s.charAt(y++);
            
          ++y;
          while(y < s.length() && (s.charAt(y) == ' ' || s.charAt(y) == '\t'))
            ++y;
          admin="";
          while(y < s.length() && s.charAt(y) != ' ')
            admin += s.charAt(y++);
          
          ++y;
          while(y < s.length() && (s.charAt(y) == ' ' || s.charAt(y) == '\t'))
            ++y;
          casual="";
          while(y < s.length() && s.charAt(y) != ' ')
            casual += s.charAt(y++);
          
          ++y;
          while(y < s.length() && (s.charAt(y) == ' ' || s.charAt(y) == '\t'))
            ++y;
          registered="";
          while(y < s.length() && s.charAt(y) != ' ')
            registered += s.charAt(y++);
          
          ++y;
          desc="";
          while(y < s.length())
            desc += s.charAt(y++);

          update(con, stmt, category, service, desc, erp, mrp, mail, library, site, channels, admin, hidden, casual, registered);
        }
      }
    }
        
    if(con  != null) con.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void update(Connection con, Statement stmt, String category, String service, String desc, String erp, String mrp, String mail, String library,
                      String site, String channels, String admin, String hidden, String casual, String registered) throws Exception
  {    
    try
    {
      stmt = con.createStatement();
 
      stmt.executeUpdate("INSERT INTO scbs ( Service, Category, Description, erp, mrp, mail, library, site, channels, admin, hidden, casual, registered) "
                       + "VALUES ('" + service + "','" + category + "','" + generalUtils.sanitiseForSQL(desc) + "','" + erp + "','" + mrp + "','" + mail + "','"
                       + library + "','" + site + "','" + channels + "','" + admin + "','" + hidden + "','" + casual + "','" + registered + "')");
        
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println("9015: " + e);
      try
      {
        if(stmt != null) stmt.close();
      }
      catch(Exception e2) { }
    }
  }  

}
