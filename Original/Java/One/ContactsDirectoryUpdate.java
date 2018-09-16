// =======================================================================================================================================================================================================
// System: ZaraStar Directory: Create/Update directory entry
// Module: ContactsDirectoryUpdate.java
// Author: C.K.Harvey
// Copyright (c) 2006-09 Christopher Harvey. All Rights Reserved.
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
import java.util.Enumeration;

public class ContactsDirectoryUpdate extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", dnm="", p1="", p2="", p3="", p4="", p5="", p6="";

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
        if(elementName.equals("p1"))
          p1 = value[0];
        else
        if(elementName.equals("p2"))
          p2 = value[0];
        else
        if(elementName.equals("p3"))
          p3 = value[0];
        else
        if(elementName.equals("p4"))
          p4 = value[0];
        else
        if(elementName.equals("p5"))
          p5 = value[0];
        else
        if(elementName.equals("p6"))
          p6 = value[0];
      }

      dnm = "Zaracloud";
      
      doIt(req, res, unm, dnm, p1, p2, p3, p4, p5, p6, bytesOut);
    }    
    catch(Exception e)
    {
      System.out.println(("Unexpected System Error: 8831c: " + e));
      res.getWriter().write("Unexpected System Error: 8831c");
    }
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(HttpServletRequest req, HttpServletResponse res, String unm, String dnm, String zpn, String host, String zcn, String showInDirectory, String state, String newOrEdit, int[] bytesOut) throws Exception
  {
    String rtn="Unexpected System Error: 8831c";

    switch(update(zpn, host, zcn, showInDirectory, state, newOrEdit))
    {
      case ' ' : rtn = "OK:";            break;
      default  : rtn = "Already Exists"; break; // just-in-case
    }

    res.setContentType("text/html");
    res.setHeader("Cache-Control", "no-cache");
    
    res.getWriter().write(rtn);
    
    serverUtils.totalBytes(req, unm, dnm, 8831, bytesOut[0], 0, zpn);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // rtns: ' ' if rec added/updated ok
  //       'E' if adding a rec that already exists
  //       'X' other error
  private char update(String zpn, String host, String zcn, String showInDirectory, String state, String newOrEdit) throws Exception
  {
    Connection con = null;
    Statement stmt = null;
    ResultSet rs   = null;

    try
    {
      String uName = directoryUtils.getMySQLUserName();
      String pWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();
    
      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/Zaracloud_ofsa?user=" + uName + "&password=" + pWord);

      stmt = con.createStatement();

      String q;

      if(newOrEdit.equals("N")) // assume that this servlet not called if new entry && showInDirectory is N
      {      
        q = "INSERT INTO directory ( ZPN, Host, ZCN, Status ) VALUES ('" + generalUtils.sanitiseForSQL(zpn) + "','" + generalUtils.sanitiseForSQL(host) + "','" + zcn + "','" + state + "')";
      }
      else
      {
        if(newOrEdit.equals("E")) // chks that it exists, jic
        {
          if(showInDirectory.equals("Y"))         
          {
            rs = stmt.executeQuery("SELECT ZPN FROM directory WHERE ZPN = '" + zpn + "'");
         
            if(! rs.next())
            {
              q = "INSERT INTO directory ( ZPN, Host, ZCN, Status ) VALUES ('" + generalUtils.sanitiseForSQL(zpn) + "','" + generalUtils.sanitiseForSQL(host) + "','" + zcn + "','" + state + "')";
           
              stmt.executeUpdate(q);
            }
          
            if(rs != null) rs.close();
          }
        }
        else
        {
          if(showInDirectory.equals("Y"))         
            q = "UPDATE directory SET ZPN = '" + generalUtils.sanitiseForSQL(zpn) + "', Host = '" + generalUtils.sanitiseForSQL(host) + "', ZCN = '" + zcn + "', Status = '" + state + "'";
          else // remove from directory
            q = "DELETE FROM directory WHERE ZPN = '" + generalUtils.sanitiseForSQL(zpn) + "'";

          stmt.executeUpdate(q);
        }
      }
        
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
        
      return ' ';
    }
    catch(Exception e)
    {
      System.out.println("8831c: " + e);
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }
    
    return 'X';
  }

}
