// =======================================================================================================================================================================================================
// System: ZaraStar Product: For a Zara-format catalog page - get pricing info
// Module: CatalogDisplayZaraPagePricing.java
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

public class CatalogDisplayZaraPagePricing extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  Profile profile = new Profile();
  Customer customer = new Customer();
  AdminControlUtils adminControlUtils = new AdminControlUtils();
   
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    Writer r = null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", uty="", dnm="", p1="", p2="", p13="", p14="", p15="", p16="";

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
        if(name.equals("dnm")) // dnm
          dnm = value;
        else
        if(name.equals("p1")) // mfr
          p1 = value;
        else
        if(name.equals("p2")) // mfrCodes
          p2 = value;
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
      
      doIt(r, req, unm, uty, dnm, p1, p2, p13, p14, p15, p16, bytesOut);
    }
    catch(Exception e)
    {
      System.out.println("2004e: " + e);
      serverUtils.etotalBytes(req, p15, p13, 2004, bytesOut[0], 0, "ERR:");
      try {  scoutln(r, bytesOut, "ERR:"); } catch (Exception e2) { } 
      r.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(Writer r, HttpServletRequest req, String unm, String uty, String dnm, String p1, String p2, String pricingUpline, String userType, String userName, String passWord, int[] bytesOut) throws Exception
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
        serverUtils.etotalBytes(req, userName, pricingUpline, 2004, bytesOut[0], 0, "ACC:" + p1);
        if(con != null) con.close();
        scoutln(r, bytesOut, "");
        r.flush();
        return;
      }
    }
    else // assume internal user
    {
      if((uty.equals("R") && ! adminControlUtils.notDisabled(con, stmt, rs, 902)) || (uty.equals("A") && ! adminControlUtils.notDisabled(con, stmt, rs, 802)) || (uty.equals("I") && ! authenticationUtils.verifyAccess(con, stmt, rs, req, 2004, unm, uty, dnm, localDefnsDir, defnsDir)))
      {
        serverUtils.etotalBytes(req, userName, pricingUpline, 2004, bytesOut[0], 0, "ACC:" + p1);
        if(con != null) con.close();
        scoutln(r, bytesOut, "");
        r.flush();
        return;
      }
    }
 
    String suppliersBand = "0";
    if(userType.equals("R"))  // remote
    {
      // the band of the calling downline will eb pt at the end of each item pricing details returned (quick-and-dirty)
      String[] customerCode = new String[1];
      profile.getExternalAccessNameCustomerCode(con, stmt, rs, userName, pricingUpline, localDefnsDir, defnsDir, customerCode);
      suppliersBand = generalUtils.intToStr(customer.getPriceBand(con, stmt, rs, customerCode[0]));
    }
    
    String[] pricingDetails = new String[1];  pricingDetails[0] = "";
    forAllMfrCodes(con, stmt, rs, p1, p2, pricingDetails, suppliersBand);
    
    scoutln(r, bytesOut, pricingDetails[0]);

    serverUtils.totalBytes(con, stmt, rs, req, userName, pricingUpline, 2004, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con != null) con.close();
    r.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // sets a price (list) for each item (inc synonyms) - some prices may be blanks
  private void forAllMfrCodes(Connection con, Statement stmt, ResultSet rs, String mfr, String mfrCodes, String[] pricingDetails, String suppliersBand) throws Exception
  {
    boolean thisItemSuccess;
    String thisMfrCode="";
    int x = 0, len = mfrCodes.length();
    while(x < len)
    {
      thisItemSuccess = false;
      while(x < len && mfrCodes.charAt(x) != '\001') // for this (group of related) item(s)
      {
        thisMfrCode = "";
        while(x < len && mfrCodes.charAt(x) != '\002') // just-in-case
          thisMfrCode += mfrCodes.charAt(x++);
        ++x; // \002
         
        if(forAnItem(con, stmt, rs, mfr, thisMfrCode, pricingDetails, suppliersBand))
        {
          while(x < len && mfrCodes.charAt(x) != '\001') // skip any (remaining) synonyms
            ++x;
          thisItemSuccess = true;
        }
      }
      
      if(! thisItemSuccess)
        pricingDetails[0] += ("\001\001\001\001\001\001\001\001");

      ++x; // \001
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean forAnItem(Connection con, Statement stmt, ResultSet rs, String mfr, String mfrCode, String[] pricingDetails, String suppliersBand) throws Exception
  {
    boolean found = false;
      
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT ItemCode, SalesCurrency, RRP, SellPrice1, SellPrice2, SellPrice3, SellPrice4 FROM stock WHERE Manufacturer = '" + generalUtils.sanitiseForSQL(mfr) + "' AND ManufacturerCode = '" + generalUtils.sanitiseForSQL(mfrCode) + "'");
    
    if(rs.next())
    {
      pricingDetails[0] += (rs.getString(1) + "\001" + rs.getString(2) + "\001" + rs.getString(3) + "\001" + rs.getString(4) + "\001"
                        + rs.getString(5) + "\001" + rs.getString(6) + "\001" + rs.getString(7) + "\001" + suppliersBand + "\001");
      found = true;
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return found;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(Writer r, int bytesOut[], String str) throws Exception
  {      
    r.write(str + "\n");
    bytesOut[0] += (str.length() + 1);    
  }

}
