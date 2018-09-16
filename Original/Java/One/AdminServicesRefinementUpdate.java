// =======================================================================================================================================================================================================
// System: ZaraStar Admin: Services refinement access - update
// Module: AdminServicesRefinementUpdate.java
// Author: C.K.Harvey
// Copyright (c) 2003-09 Christopher Harvey. All Rights Reserved.
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
import java.net.*;
import java.util.*;

public class AdminServicesRefinementUpdate extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  Profile profile = new Profile();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", userCode="", callingService="", allServices="";

    try
    {
      directoryUtils.setContentHeaders(res);
      out = res.getWriter();

      String[] services = new String[100];
      int numServices = 0, servicesLen = 100;

      int x;
      String value[];
      String name;

      Enumeration en = req.getParameterNames();
      while(en.hasMoreElements())
      {
        name = (String)en.nextElement();
        value = req.getParameterValues(name);
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
        if(name.equals("services"))
          allServices = value[0];
        else
        if(name.equals("userCode"))
          userCode = value[0];
        else
        if(name.equals("callingService"))
          callingService = value[0];
        else
        {
          if((numServices + 1) == servicesLen)
          {
            String[] tmp = new String[servicesLen];
            for(x=0;x<servicesLen;++x)
              tmp[x] = services[x];
            servicesLen += 100;
            services = new String[servicesLen];
            for(x=0;x<(servicesLen - 100);++x)
              services[x] = tmp[x];
          }

          services[numServices++] = name;
        }
      }

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, allServices, numServices, services, userCode, callingService, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "AdminServicesRefinementUpdate", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7508, bytesOut[0], 0, "ERR:" + userCode);
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String allServices, int numServices, String[] services, String userCode, String callingService,
                    int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if((! serverUtils.isSysAdmin(con, stmt, rs, unm, dnm, sid, uty, localDefnsDir, defnsDir)) && (! serverUtils.isDBAdmin(con, stmt, rs, unm)))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "AdminServicesRefinementUpdate", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7508, bytesOut[0], 0, "ACC:" + userCode);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "AdminServicesRefinementUpdate", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7508, bytesOut[0], 0, "SID:" + userCode);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    process(con, stmt, userCode, allServices, services, numServices, dnm);

    refetch(out, unm, sid, uty, dnm, men, den, bnm, localDefnsDir, callingService, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 7508, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), userCode);
    if(con  != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void process(Connection con, Statement stmt, String userCode, String allServices, String[] services, int numServices, String dnm) throws Exception
  {
    int x = 0, len = allServices.length();
    String service;

    while(x < len)
    {
      service = "";
      while(x < len && allServices.charAt(x) != ',')
        service += allServices.charAt(x++);
      ++x;
      
      if(isOnList(service,  services, numServices))
        addService(con, stmt, service, userCode);
      else removeService(con, stmt, service, userCode);

    }

    profile.deleteRightsFile(userCode, dnm);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean isOnList(String service, String[] services, int numServices) throws Exception
  {
    String s;
    int y, len;

    for(int x=0;x<numServices;++x)
    {
      y = 0;
      len = services[x].length();
      s = "";
      while(y < len && services[x].charAt(y) != '\001') // just-in-case
        s += services[x].charAt(y++);

      if(s.equals(service))
        return true;
    }

    return false;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void addService(Connection con, Statement stmt, String service, String userCode) throws Exception
  {
    try
    {
      stmt = con.createStatement();

      stmt.executeUpdate("INSERT INTO userservices (UserCode, Service) VALUES ('" + userCode + "','" + service + "')");

      if(stmt != null) stmt.close();
    }
    catch(Exception e) // already exists
    {
      if(stmt != null) stmt.close();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void removeService(Connection con, Statement stmt, String service, String userCode) throws Exception
  {
    try
    {
      stmt = con.createStatement();

      stmt.executeUpdate("DELETE FROM userservices WHERE Service = '" + service + "' AND UserCode = '" + userCode + "'");

      if(stmt != null) stmt.close();
    }
    catch(Exception e) // already exists
    {
      if(stmt != null) stmt.close();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void refetch(PrintWriter out, String unm, String sid, String uty, String dnm, String men, String den, String bnm, String localDefnsDir, String callingService, int[] bytesOut) throws Exception
  {
    URL url = new URL("http://" + serverUtils.serverToCall("OFSA", localDefnsDir) + "/central/servlet/OptionTabs?p1=A&p2=" + callingService + "&unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm);

    URLConnection uc = url.openConnection();
    uc.setDoInput(true);
    uc.setUseCaches(false);
    uc.setDefaultUseCaches(false);

    uc.setRequestProperty("Content-Type", "application/octet-stream");

    BufferedReader di = new BufferedReader(new InputStreamReader(uc.getInputStream()));

    String s = di.readLine();
    while(s != null)
    {
      scoutln(out, bytesOut, s);
      s = di.readLine();
    }

    di.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {
    out.println(str);
    bytesOut[0] += (str.length() + 2);
  }

}
