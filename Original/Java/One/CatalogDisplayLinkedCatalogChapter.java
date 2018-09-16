// =======================================================================================================================================================================================================
// System: ZaraStar Product: Display linked catalog given chapter
// Module: CatalogDisplayLinkedCatalogChapter.java
// Author: C.K.Harvey
// Copyright (c) 2002-08 Christopher Harvey. All Rights Reserved.
// Remark: On called server
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

public class CatalogDisplayLinkedCatalogChapter extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  Profile profile = new Profile();
  
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    Writer r = null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", uty="", dnm="", bnm="", sid="", men="", p1="", p9="", p10="", p11="", p14="", p15="", p80="", p81="";

    try
    {
      res.setContentType("text/xml");
      res.setHeader("Cache-Control", "no-cache");
      r = res.getWriter();

      int len = req.getContentLength();
      ServletInputStream in  = req.getInputStream();
      byte[] b = new byte[len];
      
      in.readLine(b, 0, len);          
          
      String name, value;
      int x=0;
      while(x < len)
      {
        ++x; // &
        name="";
        while(x < len && b[x] != '=')
          name += (char)b[x++];
        
        ++x; // =
        value="";
        while(x < len && b[x] != '&')
          value += (char)b[x++];
        value = generalUtils.deSanitise(value);
          
        if(name.equals("unm"))
          unm = value;
        else
        if(name.equals("uty"))
          uty = value;
        else
        if(name.equals("dnm"))
          dnm = value;
        else
        if(name.equals("bnm"))
          bnm = value;
        else
        if(name.equals("p1")) // mfr
          p1 = value;
        else
        if(name.equals("p9")) // remote sid
          p9 = value; 
        else
        if(name.equals("p10")) // catalogURL
          p10 = value; 
        else
        if(name.equals("p11")) // catalogUpline
          p11 = value; 
        else
        if(name.equals("p14")) // userType
          p14 = value; 
        else
        if(name.equals("p15")) // userName
          p15 = value; 
        else
        if(name.equals("p80")) // chapter
          p80 = value; 
        else
        if(name.equals("p81")) // section
          p81 = value; 
      }
    
      doIt(r, req, unm, uty, sid, men, dnm, bnm, p1, p9, p10, p11, p14, p15, p80, p81, bytesOut);
    }
    catch(Exception e)
    {
      serverUtils.etotalBytes(req, unm, dnm, 2005, bytesOut[0], 0, "ERR:" + p1);
      try
      {
        System.out.println("2005l: " + e);
        scoutln(r, bytesOut, "ERR:2005l");
      }
      catch (Exception e2) { } 
      r.flush();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(Writer r, HttpServletRequest req, String unm, String uty, String sid, String men, String dnm, String bnm, String p1, String p9,
                    String p10, String p11, String p14, String p15, String p80, String p81, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();
    String defnsDir      = directoryUtils.getSupportDirs('D');//, p11);
    String localDefnsDir = directoryUtils.getLocalOverrideDir(p11);//, p15);
    
    String uName = directoryUtils.getMySQLUserName();
    String pWord = directoryUtils.getMySQLPassWord();
    
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + p11 + "_ofsa?user=" + uName + "&password=" + pWord);
    
    Statement stmt = null;
    ResultSet rs   = null;

    if(p9.length() == 0) // should exist from previous call to catalog contents page
    {
      serverUtils.etotalBytes(req, p15, p11, 2005, bytesOut[0], 0, "SID:" + p1);
      scoutln(r, bytesOut, "ERR:SID2005l");
      r.flush();
      if(con != null) con.close();
      return;
    }

    String den = dnm;

    getPageSCFormat(r, p1, p80, p81, p9, p10, p11, p14, p15, unm, uty, sid, men, den, dnm, bnm, localDefnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, p15, p11, 2005, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con != null) con.close();
    r.flush();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void getPageSCFormat(Writer r, String mfr, String chapter, String section, String remoteSID, String catalogURL, String catalogUpline,
                               String userType, String userName, String unm, String uty, String sid, String men, String den, String dnm, String bnm,
                               String localDefnsDir, int[] bytesOut) throws Exception
  { 
    URL url = new URL("http://" + serverUtils.serverToCall("OFSA", localDefnsDir) + "/central/servlet/CatalogSteelclawsLinkedUser?unm=" + unm + "&sid=" + sid + "&uty=" + uty
                    + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=" + mfr + "&p9=" + remoteSID + "&p10=" + generalUtils.sanitise(catalogURL)
                    + "&p11=" + catalogUpline + "&p14=" + userType + "&p15=" + userName + "&p80=" + generalUtils.sanitise(chapter) + "&p81="
                    + generalUtils.sanitise(section) + "&bnm=" + bnm);
    
    URLConnection uc = url.openConnection();
    uc.setDoInput(true);
    uc.setUseCaches(false);
    uc.setDefaultUseCaches(false);

    uc.setRequestProperty("Content-Type", "application/octet-stream");

    BufferedReader di = new BufferedReader(new InputStreamReader(uc.getInputStream()));

    String s = di.readLine();
    while(s != null)
    {
      scout(r, bytesOut, s);
      s = di.readLine();
    }

    di.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void scout(Writer r, int bytesOut[], String str) throws Exception
  {      
    r.write(str);
    bytesOut[0] += str.length();    
  }
  private void scoutln(Writer r, int bytesOut[], String str) throws Exception
  {      
    r.write(str + "\n");
    bytesOut[0] += (str.length() + 1);    
  }
  
}
