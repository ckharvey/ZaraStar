// =============================================================================================================================================================
// System: ZaraStar Admin: Create Zaracloud_scbs DataBase
// Module: ZaracloudDataBase.java
// Author: C.K.Harvey
// Copyright (c) 2006-08 Christopher Harvey. All Rights Reserved.
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
import java.util.*;
import java.io.*;

public class ZaracloudDataBase extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();

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
      
      Enumeration en = req.getParameterNames();
      while(en.hasMoreElements())
      {
        String name = (String)en.nextElement();
        String[] value = req.getParameterValues(name);
        if(name.equals("unm"))
          unm = value[0];
        else
        if(name.equals("sid"))
          sid = value[0];
        else
        if(name.equals("uty"))
          uty = value[0];
        else
        if(name.equals("men"))
          men = value[0];
        else
        if(name.equals("den"))
          den = value[0];
        else
        if(name.equals("dnm"))
          dnm = value[0];
        else
        if(name.equals("bnm"))
          bnm = value[0];
      }
      
      doIt(out, req, unm, uty, sid, men, den, dnm, bnm, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "ZaracloudDataBase", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 9015, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String uty, String sid, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null, stmt2 = null;
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

    if(create(con, stmt, stmt2, rs))
    {
      messagePage.msgScreen(false, out, req, 0, unm, sid, uty, men, den, dnm, bnm, "9015", "", "", "Created", imagesDir, localDefnsDir, defnsDir, bytesOut);
    }
    else
    {
      messagePage.msgScreen(false, out, req, 0, unm, sid, uty, men, den, dnm, bnm, "9015", "", "", "Already Exists", imagesDir, localDefnsDir, defnsDir, bytesOut);
    }
    
    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 9015, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean create(Connection con, Statement stmt, Statement stmt2, ResultSet rs) throws Exception
  {
    try
    {
      stmt = con.createStatement();
      
      rs = stmt.executeQuery("SELECT Commodity, CommodityTitle FROM unspsc WHERE Segment LIKE '27%' ORDER BY Commodity");
  
      String commodity, commodityTitle, descStr, thisWord;
      int x, len;

      while(rs.next())
      {
        commodity      = rs.getString(1);
        commodityTitle = rs.getString(2);
        
        x = 0;
        len = commodityTitle.length();
        descStr = "";
        
        while(x < len)
        {
          if(x > 0)
            descStr += " AND ";
            
          descStr += "Description LIKE '%";

          while(x < len && commodityTitle.charAt(x) == ' ')
            ++x;
        
          thisWord = "";
          
          while(x < len && commodityTitle.charAt(x) != ' ')
            thisWord += commodityTitle.charAt(x++);
        
          descStr += (generalUtils.sanitiseForSQL(thisWord) + "%'");
        
          while(x < len && commodityTitle.charAt(x) == ' ')
            ++x;
        }
        
        if(descStr.length() > 0)         
          update(con, stmt2, commodity, descStr);         
      }
    }
    catch(Exception e)
    {
      System.out.println("ERROR: " + e);
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
      return false;
    }
   
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return true;
  }  
  
  // ----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void update(Connection con, Statement stmt, String commodity, String descStr) throws Exception
  {
    try
    {
      stmt = con.createStatement();
      
      stmt.executeUpdate("UPDATE stock SET AltItemCode3 = '" + commodity + "' WHERE " + descStr);  
    }
    catch(Exception e)
    {
      System.out.println("ERROR: " + e + ": " + commodity +  " " + descStr);
    }
   
    if(stmt != null) stmt.close();
  }  
  
}
