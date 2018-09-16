// =======================================================================================================================================================================================================
// System: ZaraStar: Products: update cycle count setup - save
// Module: ProductCycleCountSetupSave.java
// Author: C.K.Harvey
// Copyright (c) 1999-2009 Christopher Harvey. All Rights Reserved.
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
import java.net.*;
import java.sql.*;
import java.util.*;

public class ProductCycleCountSetupSave extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  InventoryAdjustment inventoryAdjustment = new InventoryAdjustment();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", year="", ignoreMondays="", ignoreTuesdays="", ignoreWednesdays="", ignoreThursdays="", ignoreFridays="", ignoreSaturdays="", ignoreSundays="", ignorePublicHolidays="";
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
        if(name.equals("year"))
          year = value[0];
        else
        if(name.equals("ignoreMondays"))
          ignoreMondays = value[0];
        else
        if(name.equals("ignoreTuesdays"))
          ignoreTuesdays = value[0];
        else
        if(name.equals("ignoreWednesdays"))
          ignoreWednesdays = value[0];
        else
        if(name.equals("ignoreThursdays"))
          ignoreThursdays = value[0];
        else
        if(name.equals("ignoreFridays"))
          ignoreFridays = value[0];
        else
        if(name.equals("ignoreSaturdays"))
          ignoreSaturdays = value[0];
        else
        if(name.equals("ignoreSundays"))
          ignoreSundays = value[0];
        else
        if(name.equals("ignorePublicHolidays"))
          ignorePublicHolidays = value[0];
      }

      ignorePublicHolidays = generalUtils.stripLeadingAndTrailingSpaces(ignorePublicHolidays);

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, year, ignoreMondays, ignoreTuesdays, ignoreWednesdays, ignoreThursdays, ignoreFridays, ignoreSaturdays, ignoreSundays, ignorePublicHolidays, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "ProductCycleCountSetupSave", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3081, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush(); 
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String year, String ignoreMondays, String ignoreTuesdays, String ignoreWednesdays,
                    String ignoreThursdays, String ignoreFridays, String ignoreSaturdays, String ignoreSundays, String ignorePublicHolidays, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 3081, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "3081b", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3081, bytesOut[0], 0, "ACC:");
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "3081b", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3081, bytesOut[0], 0, "SID:");
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(ignoreMondays.equals("on"))    ignoreMondays = "Y";    else ignoreMondays = "N";
    if(ignoreTuesdays.equals("on"))   ignoreTuesdays = "Y";   else ignoreTuesdays = "N";
    if(ignoreWednesdays.equals("on")) ignoreWednesdays = "Y"; else ignoreWednesdays = "N";
    if(ignoreThursdays.equals("on"))  ignoreThursdays = "Y";  else ignoreThursdays = "N";
    if(ignoreFridays.equals("on"))    ignoreFridays = "Y";    else ignoreFridays = "N";
    if(ignoreSaturdays.equals("on"))  ignoreSaturdays = "Y";  else ignoreSaturdays = "N";
    if(ignoreSundays.equals("on"))    ignoreSundays = "Y";    else ignoreSundays = "N";

    inventoryAdjustment.updateCycleForYear(con, stmt, rs, year, ignoreMondays, ignoreTuesdays, ignoreWednesdays, ignoreThursdays, ignoreFridays, ignoreSaturdays, ignoreSundays, ignorePublicHolidays, dnm, localDefnsDir, defnsDir);

    display(out, year, unm, sid, uty, men, den, dnm, bnm, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 3081, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void display(PrintWriter out, String year, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    URL url = new URL("http://" + men + "/central/servlet/ProductCycleCountingSetup?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&dnm=" + dnm + "&p1=" + year + "&bnm=" + bnm);

    URLConnection uc = url.openConnection();
    uc.setDoInput(true);
    uc.setUseCaches(false);
    uc.setDefaultUseCaches(false);

    uc.setRequestProperty("Content-Type", "application/octet-stream");

    BufferedReader di = new BufferedReader(new InputStreamReader(uc.getInputStream()));

    String s = di.readLine();

    while(s != null)
    {
      out.println(s);
      bytesOut[0] += s.length();
      s = di.readLine();
    }

    di.close();
  }

}
