// =======================================================================================================================================================================================================
// System: ZaraStar Admin: Document Locks definition - update
// Module: AdminDocumentLocksUpdate.java
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
import java.io.*;
import java.sql.*;
import java.util.Enumeration;

public class AdminDocumentLocksUpdate extends HttpServlet
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

    String unm="", sid="", uty="", dnm="", docAbbrev="", lockedUpto="", openTo="";

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
          docAbbrev = value[0];
        else
        if(elementName.equals("p2"))
          lockedUpto = value[0];
        else
        if(elementName.equals("p3"))
          openTo = value[0];
      }

      doIt(req, res, unm, sid, uty, dnm, docAbbrev, lockedUpto, openTo, bytesOut);
    }    
    catch(Exception e)
    {
      System.out.println(("Unexpected System Error: 7033a: " + e));
      res.getWriter().write("Unexpected System Error: 7033a");
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(HttpServletRequest req, HttpServletResponse res, String unm, String sid, String uty, String dnm, String docAbbrev, String lockedUpto,
                    String openTo, int[] bytesOut) throws Exception
  {
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
 
    String rtn="Unexpected System Error: 7033a";
    
    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      rtn = "Access Denied - Duplicate SignOn or Session Timeout";
    }
    else
    {
      lockedUpto = generalUtils.stripLeadingAndTrailingSpaces(lockedUpto);
      
      if(! generalUtils.validateDate(false, lockedUpto, localDefnsDir, defnsDir))
        rtn = "Invalid Date Specified";
      else
      if(lockedUpto.length() == 0)
        rtn = "No Date Specified";
      else
      {
        lockedUpto = generalUtils.convertDateToSQLFormat(lockedUpto);
        
        openTo = generalUtils.deSanitise(openTo);
        openTo = generalUtils.stripLeadingAndTrailingSpaces(openTo);
        
        if(update(dnm, docAbbrev, lockedUpto, openTo, localDefnsDir, defnsDir))
          rtn = ".";
      }
    }
    
    res.setContentType("text/xml");
    res.setHeader("Cache-Control", "no-cache");
    
    String s = "<msg><res>" + rtn + "</res></msg>";
    res.getWriter().write(s);
    
    serverUtils.totalBytes(req, unm, dnm, 7033, bytesOut[0], 0, docAbbrev);
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private boolean update(String dnm, String docAbbrev, String lockedUpto, String openTo, String localDefnsDir, String defnsDir)
                           throws Exception
  {
    Connection con = null;
    Statement stmt = null;
   
    try
    {
      String uName    = directoryUtils.getMySQLUserName();
      String passWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();
    
      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + uName + "&password=" + passWord);

      stmt = con.createStatement();
        
      String q = "UPDATE locks SET LockedUpto = '" + lockedUpto + "', OpenTo = '" + openTo + "' WHERE DocumentAbbrev = '" + docAbbrev + "'";
      stmt.executeUpdate(q);
        
      if(stmt != null) stmt.close();        
      if(con  != null) con.close();
            
      return true;
    }
    catch(Exception e)
    {
      System.out.println("7033a: " + e);
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }
    
    return false;
  }

}
