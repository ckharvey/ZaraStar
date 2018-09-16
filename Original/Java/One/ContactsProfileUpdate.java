// =======================================================================================================================================================================================================
// System: ZaraStar: Contacts: update profile - save
// Module: ContactsProfileUpdate.java
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

public class ContactsProfileUpdate extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
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

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", userCode="", userName="", passWord="", jobTitle="", status="", dateJoined="", dateLeft="", showInDirectory="", officePhone="", mobilePhone="", fax="", customerCode="",
           supplierCode="", facebookCode="", facebookAccessToken="", userBasis="", eMail="", bio="", isDBAdmin="", externalAccess="", isSalesPerson="", isSeniorSalesPerson="", isEnquiriesSalesPerson="", dateOfBirth="", nationality = "";
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
        if(name.equals("passWord")) 
          passWord = value[0];
        else
        if(name.equals("userCode")) 
          userCode = value[0];
        else
        if(name.equals("userName")) 
          userName = value[0];
        else
        if(name.equals("jobTitle")) 
          jobTitle = value[0];
        else
        if(name.equals("status"))
          status = value[0];
        else
        if(name.equals("dateJoined"))
          dateJoined = value[0];
        else
        if(name.equals("dateLeft"))
          dateLeft = value[0];
        else
        if(name.equals("showInDirectory"))
          showInDirectory = value[0];
        else
        if(name.equals("officePhone"))
          officePhone = value[0];
        else
        if(name.equals("mobilePhone"))
          mobilePhone = value[0];
        else
        if(name.equals("fax"))
          fax = value[0];
        else
        if(name.equals("eMail"))
          eMail = value[0];
        else
        if(name.equals("bio"))
          bio = value[0];
        else
        if(name.equals("isDBAdmin"))
          isDBAdmin = value[0];
        else
        if(name.equals("externalAccess"))
          externalAccess = value[0];
        else
        if(name.equals("customerCode"))
          customerCode = value[0];
        else
        if(name.equals("supplierCode"))
          supplierCode = value[0];
        else
        if(name.equals("facebookCode"))
          facebookCode = value[0];
        else
        if(name.equals("userBasis"))
          userBasis = value[0];
        else
        if(name.equals("facebookAccessToken"))
          facebookAccessToken = value[0];
        else
        if(name.equals("dateOfBirth"))
          dateOfBirth = value[0];
        else
        if(name.equals("nationality"))
          nationality = value[0];
        else
        if(name.equals("isSalesPerson"))
          isSalesPerson = value[0];
        else
        if(name.equals("isSeniorSalesPerson"))
          isSeniorSalesPerson = value[0];
        else
        if(name.equals("isEnquiriesSalesPerson"))
          isEnquiriesSalesPerson = value[0];
      }

      userCode        = generalUtils.capitalize(generalUtils.stripAllSpaces(userCode));
      userName        = generalUtils.stripLeadingAndTrailingSpaces(userName);
      passWord        = generalUtils.stripLeadingAndTrailingSpaces(passWord);
      jobTitle        = generalUtils.stripLeadingAndTrailingSpaces(jobTitle);
      dateJoined      = generalUtils.stripLeadingAndTrailingSpaces(dateJoined);
      dateLeft        = generalUtils.stripLeadingAndTrailingSpaces(dateLeft);
      officePhone     = generalUtils.stripLeadingAndTrailingSpaces(officePhone);
      mobilePhone     = generalUtils.stripLeadingAndTrailingSpaces(mobilePhone);
      fax             = generalUtils.stripLeadingAndTrailingSpaces(fax);
      eMail           = generalUtils.stripLeadingAndTrailingSpaces(eMail);
      bio             = generalUtils.stripLeadingAndTrailingSpaces(bio);
      customerCode           = generalUtils.stripLeadingAndTrailingSpaces(customerCode);
      supplierCode           = generalUtils.stripLeadingAndTrailingSpaces(supplierCode);
      facebookCode           = generalUtils.stripLeadingAndTrailingSpaces(facebookCode);
  
      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, userCode, userName, passWord, jobTitle, status, dateJoined, dateLeft, showInDirectory, officePhone, mobilePhone, fax, eMail, bio, isDBAdmin, externalAccess, customerCode, supplierCode,
           facebookCode, facebookAccessToken, userBasis, isSalesPerson, isSeniorSalesPerson, isEnquiriesSalesPerson, dateOfBirth, nationality, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "ContactsProfileUpdate", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8831, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush();
    }
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String userCode, String userName, String passWord, String jobTitle, String status,
                    String dateJoined, String dateLeft, String showInDirectory, String officePhone, String mobilePhone, String fax, String eMail, String bio, String isDBAdmin, String externalAccess, String customerCode, String supplierCode,
                    String facebookCode, String facebookAccessToken, String userBasis, String isSalesPerson, String isSeniorSalesPerson, String isEnquiriesSalesPerson, String dateOfBirth, String nationality, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 8831, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(true, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "8831b", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8831, bytesOut[0], 0, "ACC:");
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(true, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "8831b", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8831, bytesOut[0], 0, "SID:");
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    dateJoined = generalUtils.convertDateToSQLFormat(dateJoined);
    dateLeft   = generalUtils.convertDateToSQLFormat(dateLeft);
    
    String usersDir = directoryUtils.getUserDir(dnm) + "/" + userCode + "/";
    generalUtils.createDir(usersDir, true);

    generalUtils.createDir(usersDir + userCode + "/", true);
    generalUtils.createDir(usersDir + userCode + "/Working/", true);
    generalUtils.createDir(usersDir + userCode + "/Library/", true);
    generalUtils.createDir(usersDir + userCode + "/Logs/", true);
    generalUtils.createDir(usersDir + userCode + "/" + dnm + "/Reports/", true);
    
    profile.updateProfile(userCode, userName, passWord, status, facebookCode, facebookAccessToken, dateOfBirth, nationality, dnm);

    profile.updateProfiled(userCode, jobTitle, dateJoined, dateLeft, showInDirectory, officePhone, mobilePhone, fax, userBasis, eMail, bio, isDBAdmin, externalAccess, isSalesPerson, isSeniorSalesPerson, isEnquiriesSalesPerson, customerCode,
                        supplierCode, dnm);
    
    display(out, userCode, unm, sid, uty, men, den, dnm, bnm, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 8831, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con  != null) con.close();
    if(out != null) out.flush();
  }
   
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void display(PrintWriter out, String userCode, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    URL url = new URL("http://" + men + "/central/servlet/ContactsProfileView?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&dnm=" + dnm + "&p1=" + userCode + "&bnm=" + bnm);

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
