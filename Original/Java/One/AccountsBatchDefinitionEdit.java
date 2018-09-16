// =======================================================================================================================================================================================================
// System: ZaraStar Accounts: Batch Definition: fetch for edit
// Module: AccountsBatchDefinitionEdit.java
// Author: C.K.Harvey
// Copyright (c) 2006-07 Christopher Harvey. All Rights Reserved.
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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Enumeration;

public class AccountsBatchDefinitionEdit extends HttpServlet
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
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", dnm="", code="", year="";

    try
    {
      directoryUtils.setContentHeaders(res);
      res.setHeader("Header", "HTTP/1.0 200 OK");
      res.setStatus(200);

      Enumeration en = req.getParameterNames();
      while(en.hasMoreElements())
      {
        String elementName = (String)en.nextElement();
        String[] value = req.getParameterValues(elementName);
        if(elementName.equals("unm"))
          unm = value[0];
        else
        if(elementName.equals("sid"))
          sid = value[0];
        else
        if(elementName.equals("uty"))
          uty = value[0];
        else
        if(elementName.equals("dnm"))
          dnm = value[0];
        else
        if(elementName.equals("p1"))
          code = value[0];
        else
        if(elementName.equals("p2"))
          year = value[0];
      }

      doIt(req, res, year, unm, sid, uty, dnm, code, bytesOut);
    }    
    catch(Exception e)
    {
      System.out.println(("Unexpected System Error: 6022c: " + e));
      res.getWriter().write("Unexpected System Error: 6022c");
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(HttpServletRequest req, HttpServletResponse res, String year, String unm, String sid, String uty, String dnm,
                    String code, int[] bytesOut) throws Exception
  {
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
 
    String[] date = new String[1]; date[0]="";
    String[] desc = new String[1]; desc[0]="";
    String[] ob   = new String[1]; ob[0]="";
    String[] type = new String[1]; type[0]="";
    String[] dlm  = new String[1]; dlm[0]="";
    
    String rtn = "Unexpected System Error: 6022c";
    
    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      rtn = "Access Denied - Duplicate SignOn or Session Timeout";
    }
    else
    {
      if(fetch(dnm, year, code, date, desc, dlm, ob, type, localDefnsDir, defnsDir))
      {
        rtn = ".";
      }
    }
    
    res.setContentType("text/xml");
    res.setHeader("Cache-Control", "no-cache");
    
    if(date[0].length() == 0)
      date[0] = ".";
    
    desc[0] = generalUtils.sanitiseForXML(desc[0]);
    if(desc[0].length() == 0)
      desc[0] = ".";
    
    if(ob[0].length() == 0)
      ob[0] = "N";
    
    if(dlm[0].length() == 0)
      dlm[0] = ".";

    String s = "<msg><res>" + rtn + "</res><code>" + code + "</code><date>" + date[0] + "</date><desc>" + desc[0] + "</desc><dlm>" + dlm[0]
             + "</dlm><ob>" + ob[0] + "</ob><type>" + type[0] + "</type></msg>";

    res.getWriter().write(s);

    serverUtils.totalBytes(req, unm, dnm, 6022, bytesOut[0], 0, code);
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean fetch(String dnm, String year, String code, String[] date, String[] desc, String[] dlm, String[] ob, String[] type,
                        String localDefnsDir, String defnsDir) throws Exception
  {
    Connection con = null;
    Statement stmt = null;
    ResultSet rs   = null;

    try
    {
      String uName = directoryUtils.getMySQLUserName();
      String pWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();
    
      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_accounts_" + year + "?user=" + uName + "&password=" + pWord);

      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT Date, Description, DateLastModified, OpeningBalances, Type, SignOn FROM joubatch WHERE Code = '" + code + "'"); 

      String signOn="";
      int x;
      
      if(rs.next())                  
      {
        date[0] = generalUtils.convertFromYYYYMMDD(rs.getString(1));
        desc[0] = rs.getString(2);
        dlm[0]  = rs.getString(3);
        ob[0]   = rs.getString(4);
        type[0] = rs.getString(5);
        signOn  = rs.getString(6);
      }  
      
      x=0;
      while(x < dlm[0].length() && dlm[0].charAt(x) != ' ') // just-in-case
        ++x;
      dlm[0] = signOn + " on " + dlm[0].substring(0, x);
            
      if(rs   != null) rs.close();        
      if(stmt != null) stmt.close();        
      if(con  != null) con.close();
            
      return true;
    }
    catch(Exception e)
    {
      System.out.println("6022c: " + e);
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }
    
    return false;
  }

}
