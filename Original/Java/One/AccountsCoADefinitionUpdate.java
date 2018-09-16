// =======================================================================================================================================================================================================
// System: ZaraStar Accouts: CoA Definition: Update rec
// Module: AccountsCoADefinitionUpdate.java
// Author: C.K.Harvey
// Copyright (c) 1998-2007 Christopher Harvey. All Rights Reserved.
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

public class AccountsCoADefinitionUpdate extends HttpServlet
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

    String unm="", sid="", uty="", dnm="", accCode="", category="", drcr="", desc="", type="", currency="", active="", newOrEdit="", year="";

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
          accCode = value[0];
        else
        if(elementName.equals("p2"))
          category = value[0];
        else
        if(elementName.equals("p3"))
          drcr = value[0];
        else
        if(elementName.equals("p4"))
          desc = value[0];
        else
        if(elementName.equals("p5"))
          type = value[0];
        else
        if(elementName.equals("p7"))
          currency = value[0];
        else
        if(elementName.equals("p8"))
          active = value[0];
        else
        if(elementName.equals("p9"))
          year = value[0];
        else
        if(elementName.equals("p12"))
          newOrEdit = value[0];
      }

      doIt(req, res, year, unm, sid, uty, dnm, accCode, category, drcr, desc, type, currency, active, newOrEdit, bytesOut);
    }    
    catch(Exception e)
    {
      System.out.println(("Unexpected System Error: 6016a: " + e));
      res.getWriter().write("Unexpected System Error: 6016a");
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(HttpServletRequest req, HttpServletResponse res, String year, String unm, String sid, String uty, String dnm,
                    String accCode, String category, String drcr, String desc, String type, String currency, String active, String newOrEdit,
                    int[] bytesOut) throws Exception
  {
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
 
    String rtn="Unexpected System Error: 6016a";
    
    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      rtn = "Access Denied - Duplicate SignOn or Session Timeout";
    }
    else
    {
      accCode = generalUtils.capitalize(generalUtils.stripAllSpaces(accCode));
      desc    = generalUtils.stripLeadingAndTrailingSpaces(desc);

      if(accCode.length() == 0)
        rtn = "No Account Code Entered";
      else
      if(desc.length() == 0)
        rtn = "No Description Entered";
      else
      if(! generalUtils.isInteger(accCode))
        rtn = "Invalid Account Code Entered";
      else
      {
        boolean newRec;
        if(newOrEdit.equals("N"))
          newRec = true;
        else newRec = false;
   
        switch(update(newRec, year, unm, dnm, accCode, category, drcr, desc, type, currency, active, localDefnsDir, defnsDir))
        {
          case ' ' : rtn = ".";                            break;
          case 'E' : rtn = "Account Already Exists";       break;
          case 'X' : rtn = "Failed to Initialize Account"; break;
        }
      }
    }
    
    res.setContentType("text/xml");
    res.setHeader("Cache-Control", "no-cache");
    
    String s = "<msg><res>" + rtn + "</res></msg>";
    res.getWriter().write(s);
    
    serverUtils.totalBytes(req, unm, dnm, 6016, bytesOut[0], 0, accCode);
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  // rtns: ' ' if rec added/updated ok
  //       'E' if adding a rec that already exists
  //       'X' other error
  private char update(boolean newRec, String year, String unm, String dnm, String accCode, String category, String drcr,
                      String desc, String type, String currency, String active, String localDefnsDir,
                      String defnsDir) throws Exception
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

      if(newRec)
      {    
        // if adding a new rec, check if accCode already exists
        boolean alreadyExists=false;
        stmt = con.createStatement();
  
        rs = stmt.executeQuery("SELECT AccCode FROM acctdefn WHERE AccCode = '" + accCode + "'"); 
        if(rs.next())
          alreadyExists = true;
        rs.close();
  
        if(stmt != null) stmt.close();
      
        if(alreadyExists)
        {
          if(con != null) con.close();
          return 'E';
        }
  
        stmt = con.createStatement();
           
        String q = "INSERT INTO acctdefn ( AccCode, DrCr, Description, Type, DateLastModified, Currency, Active, Category, SignOn ) VALUES ( '" + accCode + "','" + drcr + "','" + desc + "','" + type + "',NULL,'" + currency + "','" + active
                 + "','" + category + "','" + unm + "')";
        stmt.executeUpdate(q);
 
        if(stmt != null) stmt.close();
        if(con  != null) con.close();
        
        return ' ';
      }
  
      // else update existing rec
      stmt = con.createStatement();
        
      String q = "UPDATE acctdefn SET Category = '" + category + "', DrCr = '" + drcr + "', Description = '" + desc + "', Type = '" + type + "', DateLastModified = NULL, Currency = '" + currency + "', Active = '" + active + "', SignOn = '" + unm
               + "' WHERE AccCode = '" + accCode + "'";
      stmt.executeUpdate(q);
        
      if(stmt != null) stmt.close();        
      if(con  != null) con.close();
            
      return ' ';
    }
    catch(Exception e)
    {
      System.out.println("6016a: " + e);
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }
    
    return 'X';
  }

}
