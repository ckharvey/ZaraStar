// =======================================================================================================================================================================================================
// System: ZaraStar AdminEngine: process quotation reasons
// Module: AdminQuotationReasonsUpdate.java
// Author: C.K.Harvey
// Copyright (c) 2003-06 Christopher Harvey. All Rights Reserved.
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

public class AdminQuotationReasonsUpdate extends HttpServlet
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

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", reason1="", reason2="", reason3="", reason4="",
           reason5="", reason6="", reason7="", reason8="", reason9="", reason10="";

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
        else
        if(name.equals("reason1"))
          reason1 = value[0];
        else
        if(name.equals("reason2"))
          reason2 = value[0];
        else
        if(name.equals("reason3"))
          reason3 = value[0];
        else
        if(name.equals("reason4"))
          reason4 = value[0];
        else
        if(name.equals("reason5"))
          reason5 = value[0];
        else
        if(name.equals("reason6"))
          reason6 = value[0];
        else
        if(name.equals("reason7"))
          reason7 = value[0];
        else
        if(name.equals("reason8"))
          reason8 = value[0];
        else
        if(name.equals("reason9"))
          reason9 = value[0];
        else
        if(name.equals("reason10"))
          reason10 = value[0];
      }

      doIt(out, req, reason1, reason2, reason3, reason4, reason5, reason6, reason7, reason8, reason9, reason10, unm, sid, uty,
           men, den, dnm, bnm, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "AdminQuotationReasonsUpdate", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7065, bytesOut[0], 0, "ERR:" + reason1);
      if(out != null) out.flush();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String reason1, String reason2, String reason3, String reason4,
                      String reason5, String reason6, String reason7, String reason8, String reason9, String reason10, String unm, 
                      String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut)
                      throws Exception
  {
    String imagesDir     = directoryUtils.getSupportDirs('I');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String defnsDir      = directoryUtils.getSupportDirs('D');

    long startTime = new java.util.Date().getTime();

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 7065, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "AdminQuotationReasonsUpdate", imagesDir, localDefnsDir,
                         defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7062, bytesOut[0], 0, "ACC:" + reason1);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "AdminQuotationReasonsUpdate", imagesDir, localDefnsDir,
                         defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7065, bytesOut[0], 0, "SID:" + reason1);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }
        
    int target;
    if(process(reason1, reason2, reason3, reason4, reason5, reason6, reason7, reason8, reason9, reason10, dnm, localDefnsDir,
               defnsDir))
    {
      target = 27;
    }
    else target = 23;

    messagePage.msgScreen(false, out, req, target, unm, sid, uty, men, den, dnm, bnm, "AdminQuotationReasonsUpdate", "", "", "", imagesDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 7065, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con  != null) con.close();
    if(out != null) out.flush();
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private boolean process(String reason1, String reason2, String reason3, String reason4, String reason5, String reason6,
                            String reason7, String reason8, String reason9, String reason10, String dnm, String localDefnsDir,
                            String defnsDir) throws Exception
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
           
      String q = "UPDATE quoreasons SET Reason1 = '" + generalUtils.sanitise2(reason1) + "', Reason2 = '" + generalUtils.sanitise2(reason2)
               + "', Reason3 = '" + generalUtils.sanitise2(reason3) + "', Reason4 = '" + generalUtils.sanitise2(reason4) + "', Reason5 = '"
               + generalUtils.sanitise2(reason5) + "', Reason6 = '" + generalUtils.sanitise2(reason6) + "', Reason7 = '"
               + generalUtils.sanitise2(reason7) + "', Reason8 = '" + generalUtils.sanitise2(reason8) + "', Reason9 = '"
               + generalUtils.sanitise2(reason9) + "', Reason10 = '" + generalUtils.sanitise2(reason10) + "'";

      stmt.executeUpdate(q);
        
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
      
      return true;
    }
    catch(Exception e)
    {
      System.out.println("7065a: " + e);
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }
    
    return false;
  }

  
}
