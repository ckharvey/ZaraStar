// =======================================================================================================================================================================================================
// System: ZaraStar Product: Display linked catalogs contents page
// Module: CatalogFetchLinkedContents.java
// Author: C.K.Harvey
// Copyright (c) 2002-08 Christopher Harvey. All Rights Reserved. All Rights Reserved. All Rights Reserved;
// Remark: On called server
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
import java.net.*;
import java.sql.*;

public class CatalogFetchLinkedContents extends HttpServlet
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

    String unm="", uty="", sid="", dnm="", bnm="", p1="", p2="", p10="", p11="", p12="", p13="", p14="", p15="", p16="", p17="", p18="", p19="", p20="", p21="", p22="", p23="", p24="", p25="", p26="";

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
        if(name.equals("sid"))
          sid = value;
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
        if(name.equals("p2")) // catalogType
          p2 = value;
        else
        if(name.equals("p10")) // catalogURL
          p10 = value;
        else
        if(name.equals("p11")) // catalogUpline
          p11 = value;
        else
        if(name.equals("p12")) // pricingURL
          p12 = value;
        else
        if(name.equals("p13")) // pricingUpline
          p13 = value;
        else
        if(name.equals("p14")) // userType
          p14 = value;
        else
        if(name.equals("p15")) // userName
          p15 = value;
        else
        if(name.equals("p16")) // passWord
          p16 = value;
        else
        if(name.equals("p17")) // band
          p17 = value;
        else
        if(name.equals("p18")) // markup
          p18 = value;
        else
        if(name.equals("p19")) // discount1
          p19 = value;
        else
        if(name.equals("p20")) // discount2
          p20 = value;
        else
        if(name.equals("p21")) // discount3
          p21 = value;
        else
        if(name.equals("p22")) // discount4
          p22 = value;
        else
        if(name.equals("p23")) // catalogCurrency
          p23 = value;
        else
        if(name.equals("p24")) // priceBasis
          p24 = value;
        else
        if(name.equals("p25")) // canSeeCostPrice
          p25 = value;
        else
        if(name.equals("p26")) // canSeeRRPPrice
          p26 = value;
      }
      
      doIt(r, req, unm, uty, sid, dnm, bnm, p1, p2, p10, p11, p12, p13, p14, p15, p16, p17, p18, p19, p20, p21, p22, p23, p24, p25, p26, bytesOut);
    }
    catch(Exception e)
    {
      serverUtils.etotalBytes(req, unm, dnm, 2005, bytesOut[0], 0, "ERR:" + p1);
      try
      {
        scoutln(r, bytesOut, "ERR:2005a " + p1);
      }
      catch (Exception e2) { } 
      r.flush();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(Writer r, HttpServletRequest req, String unm, String uty, String sid, String dnm, String bnm, String p1, String p2, String p10, String p11, String p12, String p13, String p14, String p15, String p16, String p17, String p18,
                    String p19, String p20, String p21, String p22, String p23, String p24, String p25, String p26, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(p11);
    String sessionsDir      = directoryUtils.getSessionsDir(p11);

    String uName = directoryUtils.getMySQLUserName();
    String pWord = directoryUtils.getMySQLPassWord();
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + p11 + "_ofsa?user=" + uName + "&password=" + pWord);
    Statement stmt = null;
    ResultSet rs   = null;

    // only need to validate if this server is the pricing server (ie this is not SC)

    String newSID;
    if(p11.equals("Catalogs") || p14.equals("R")) // remote server
    {
      newSID = serverUtils.newSessionID(p15, "R", p11, sessionsDir, localDefnsDir, defnsDir);
    }
    else
    {        
      newSID = serverUtils.newSessionID(unm, uty, p11, sessionsDir, localDefnsDir, defnsDir);
    }
    
    if(newSID.length() == 0) // just-in-case
    {
      serverUtils.etotalBytes(req, unm, dnm, 2005, bytesOut[0], 0, "SID:" + p1);
      scoutln(r, bytesOut, "ERR:SID2005a");
      if(con != null) con.close();
      r.flush();
      return;
    }

    String men = "";
    String den = dnm;

    set(r, p1, p18, p10, p15, p16, p17, p19, p20, p21, p22, p11, newSID, p14, p12, p13, p2, p23, p24, p25, p26, unm, uty, sid, men, den, dnm, bnm, localDefnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 2005, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con != null) con.close();
    r.flush();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Writer r, String mfr, String markup, String catalogURL, String userName, String passWord, String band, String discount1, String discount2, String discount3, String discount4, String catalogUpline, String remoteSID,
                   String userType, String pricingURL, String pricingUpline, String catalogType, String catalogCurrency, String priceBasis, String canSeeCostPrice, String canSeeRRPPrice, String unm, String uty, String sid, String men, String den,
                   String dnm, String bnm, String localDefnsDir, int[] bytesOut) throws Exception
  {
    if(catalogURL.equals("catalogs.zaracloud.com") || catalogURL.equals("http://catalogs.zaracloud.com"))
    {
      getContentsPageSCFormat(r, mfr, remoteSID, catalogUpline, catalogURL, userType, userName, unm, uty, men, den, catalogUpline, bnm, localDefnsDir, bytesOut);
    }
    else
    if(catalogType.equals("C"))
    {
      getContentsPageInternalCatalog(r, mfr, remoteSID, catalogUpline, userType, userName, unm, localDefnsDir, bytesOut);
    }
    else
    {
      getFirstPageInternalListing(r, mfr, userName, catalogURL, band, markup, discount1, discount2, discount3, discount4, catalogUpline, remoteSID,
                                  userType, pricingURL, pricingUpline, catalogCurrency, priceBasis, canSeeCostPrice, canSeeRRPPrice, unm, uty, sid,
                                  men, den, dnm, bnm, localDefnsDir, bytesOut);
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void getContentsPageInternalCatalog(Writer r, String mfr, String remoteSID, String catalogUpline, String userType, String userName, String unm, String localDefnsDir, int[] bytesOut) throws Exception
  { 
    URL url = new URL("http://" + serverUtils.serverToCall("OFSA", localDefnsDir) + "/central/servlet/CatalogZaraLinkedInUser?unm=" + unm + "&p1=" + generalUtils.sanitise(mfr) + "&p9=" + remoteSID + "&p11=" + catalogUpline + "&p14=" + userType + "&p15=" + userName);
    
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
  private void getFirstPageInternalListing(Writer r, String mfr, String userName, String catalogURL, String band, String markup, String discount1,
                                           String discount2, String discount3, String discount4, String catalogUpline, String remoteSID,
                                           String userType, String pricingURL, String pricingUpline, String catalogCurrency, String priceBasis,
                                           String canSeeCostPrice, String canSeeRRPPrice, String unm, String uty, String sid, String men, String den,
                                           String dnm, String bnm, String localDefnsDir, int[] bytesOut) throws Exception
  { 
    URL url = new URL("http://" + serverUtils.serverToCall("OFSA", localDefnsDir) + "/central/servlet/CatalogZaraListingLinkedInUser?unm=" + unm + "&uty=" + uty + "&sid=" + sid
                    + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p32=F&p31=" + generalUtils.sanitise(mfr) + "&p9=" + remoteSID + "&p15=" + userName
                    + "&p14=" + userType + "&p10=" + generalUtils.sanitise(catalogURL) + "&p17=" + band + "&p18=" + markup + "&p19=" + discount1 + "&p20="
                    + discount2 + "&p21=" + discount3 + "&p22=" + discount4 + "&p11=" + catalogUpline + "&p12=" + generalUtils.sanitise(pricingURL) + "&p13="
                    + pricingUpline + "&p23=" + catalogCurrency + "&p24=" + priceBasis + "&p25=" + canSeeCostPrice + "&p26=" + canSeeRRPPrice + "&bnm=" + bnm);
    
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
  private void getContentsPageSCFormat(Writer r, String mfr, String remoteSID, String catalogUpline, String catalogURL, String userType, String userName, String unm, String uty, String men, String den, String dnm, String bnm,
                                       String localDefnsDir, int[] bytesOut) throws Exception
  { 
    URL url = new URL("http://" + serverUtils.serverToCall("OFSA", localDefnsDir) + "/central/servlet/CatalogSteelclawsLinkedUser?unm=" + unm //+ "&sid=" + sid
                    + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=" + mfr + "&p9=" + remoteSID + "&p10=" + generalUtils.sanitise(catalogURL) + "&p11=" + catalogUpline + "&p14=" + userType + "&p15=" + userName + "&bnm=" + bnm);
    
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
