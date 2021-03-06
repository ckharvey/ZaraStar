// =======================================================================================================================================================================================================
// System: ZaraStar Product: For a Zara-format (and non-SC format) catalog page - get availability info
// Module: CatalogsAvailabilityInfo.java
// Author: C.K.Harvey
// Copyright (c) 2002-08 Christopher Harvey. All Rights Reserved.
// On called server
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

public class CatalogsAvailabilityInfo extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  Profile profile = new Profile();
  Customer customer = new Customer();
   
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    Writer r = null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", uty="", bnm="", p1="", p9="", p12="", p13="", p14="", p15="", p16="";

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
          
        if(name.equals("unm")) // unm
          unm = value;
        else
        if(name.equals("uty")) // uty
          uty = value;
        else
        if(name.equals("bnm")) // bnm
          bnm = value;
        else
        if(name.equals("p1")) // itemCode
          p1 = value;
        else
        if(name.equals("p9")) // remoteSID
          p9 = value;
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
      }   
      
      doIt(r, req, unm, uty, bnm, p1, p9, p12, p13, p14, p15, p16, bytesOut);
    }
    catch(Exception e)
    {
      serverUtils.etotalBytes(req, p15, p13, 2000, bytesOut[0], 0, "ERR:");
      try {  scoutln(r, bytesOut, "ERR:"); } catch (Exception e2) { } 
      r.flush();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(Writer r, HttpServletRequest req, String unm, String uty, String bnm, String p1, String p9, String pricingURL, String pricingUpline, String userType, String userName, String passWord, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');//, pricingUpline);
    String localDefnsDir = directoryUtils.getLocalOverrideDir(pricingUpline);//, userName);

    String uName = directoryUtils.getMySQLUserName();
    String pWord = directoryUtils.getMySQLPassWord();
    
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + pricingUpline + "_ofsa?user=" + uName + "&password=" + pWord);
    Statement stmt = null;
    ResultSet rs   = null;

    if(userType.equals("R")) // from a reseller server
    {
      if(! authenticationUtils.validateSignOnExtUser(userName, passWord, pricingUpline, defnsDir))
      {
        serverUtils.etotalBytes(req, userName, pricingUpline, 2000, bytesOut[0], 0, "ACC:" + p1);
        if(con != null) con.close();
        scoutln(r, bytesOut, "");
        r.flush();
        return;
      }
    }
    else // assume internal user
    {
      if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 2000, unm, uty, pricingUpline, localDefnsDir, defnsDir))
      {
        serverUtils.etotalBytes(req, userName, pricingUpline, 2000, bytesOut[0], 0, "ACC:" + p1);
        if(con != null) con.close();
        scoutln(r, bytesOut, "");
        r.flush();
        return;
      }
    }
 
    scoutln(r, bytesOut, stockLevel(p1, userName, p9, userType, pricingURL, pricingUpline, pricingUpline, bnm, localDefnsDir));

    serverUtils.totalBytes(con, stmt, rs, req, userName, pricingUpline, 2000, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con != null) con.close();
    r.flush();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String stockLevel(String itemCode, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir)
                           throws Exception
  {
    String traceList = getStockLevelsViaTrace(itemCode, unm, uty, sid, men, den, dnm, bnm, localDefnsDir);
    // traceList = "SamLeong\001150000\001SyedAlwi\00150000\001\0"
    
    return generalUtils.doubleToStr(totalLevels(traceList));
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private String getStockLevelsViaTrace(String itemCode, String unm, String uty, String sid, String men, String den, String dnm, String bnm,
                                        String localDefnsDir) throws Exception
  { 
    URL url = new URL("http://" + serverUtils.serverToCall("OFSA", localDefnsDir) + "/central/servlet/StockLevelsGenerate?unm=" + unm + "&sid=" + sid + "&uty=" + uty
                    + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=" + itemCode + "&bnm=" + bnm);

    URLConnection uc = url.openConnection();
    uc.setDoInput(true);
    uc.setUseCaches(false);
    uc.setDefaultUseCaches(false);

    uc.setRequestProperty("Content-Type", "application/octet-stream");

    BufferedReader di = new BufferedReader(new InputStreamReader(uc.getInputStream()));

    String t="", s = di.readLine();
    while(s != null)
    {
      t += s;
      s = di.readLine();
    }

    di.close();

    return t;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private double totalLevels(String traceList) throws Exception
  {
    int y=0, len = traceList.length();
    String thisQty;
    double totalStockLevel = 0.0;

    while(y < len) // just-in-case
    {
      while(y < len && traceList.charAt(y) != '\001')
        ++y;
      ++y;

      thisQty = "";
      while(y < len && traceList.charAt(y) != '\001')
        thisQty += traceList.charAt(y++);
      ++y;

      totalStockLevel += generalUtils.doubleFromStr(thisQty);
    }

    return totalStockLevel;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(Writer r, int bytesOut[], String str) throws Exception
  {      
    r.write(str + "\n");
    bytesOut[0] += (str.length() + 1);    
  }

}
