// =======================================================================================================================================================================================================
// System: ZaraStar: Contacts: add/update contact
// Module: ContactsDetailsUpdate.java
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
import java.util.*;
import java.sql.*;

public class ContactsDetailsUpdate extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  Profile profile = new Profile();
  Customer customer = new Customer();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", personName="", companyName="", jobTitle="", notes="", customerCode="", supplierCode="", eMail="", newOrEditOrDelete="", contactCode="", organizationCode="", externalCode="",
           externalPassWord="", externalRights="", externalApproved="", phone1="", phone2="", phone3="", fax="", returnToWhere = "", userCode = "", mailingList = "";

    try
    {
      directoryUtils.setContentHeaders(res);
      out = res.getWriter();

      byte[] groups    = new byte[1000]; groups[0] = '\000';
      int[]  groupsLen = new int[1];     groupsLen[0] = 1000;
      int thisEntryLen, inc;

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
        if(name.equals("name")) 
          personName = value[0];
        else
        if(name.equals("companyName"))
          companyName = value[0];
        else
        if(name.equals("jobTitle")) 
          jobTitle = value[0];
        else
        if(name.equals("eMail")) 
          eMail = value[0];
        else
        if(name.equals("notes"))
          notes = value[0];
        else
        if(name.equals("customerCode"))
          customerCode = value[0];
        else
        if(name.equals("supplierCode"))
          supplierCode = value[0];
        else
        if(name.equals("newOrEditOrDelete"))
          newOrEditOrDelete = value[0];
        else
        if(name.equals("contactCode"))
          contactCode = value[0];
        else
        if(name.equals("organizationCode"))
          organizationCode = value[0];
        else
        if(name.equals("hiddenExternalCode"))
          externalCode = value[0];
        else
        if(name.equals("hiddenExternalPassWord"))
          externalPassWord = value[0];
        else
        if(name.equals("externalRights"))
          externalRights = value[0];
        else
        if(name.equals("externalApproved"))
          externalApproved = value[0];
        else
        if(name.equals("phone1"))
          phone1 = value[0];
        else
        if(name.equals("phone2"))
          phone2 = value[0];
        else
        if(name.equals("phone3"))
          phone3 = value[0];
        else
        if(name.equals("fax"))
          fax = value[0];
        else
        if(name.equals("returnToWhere"))
          returnToWhere = value[0];
        else
        if(name.equals("userCode"))
          userCode = value[0];
        else
        if(name.equals("mailingList"))
          mailingList = value[0];
        else
        if(name.length() > 2 && name.substring(1, 3).equals(".x")) ;
        else
        if(name.length() > 2 && name.substring(1, 3).equals(".y")) ;
        else // must be checkbox value
        {
          thisEntryLen = name.length() + 2;

          if((generalUtils.lengthBytes(groups, 0) + thisEntryLen) >= groupsLen[0])
          {
            byte[] tmp = new byte[groupsLen[0]];
            System.arraycopy(groups, 0, tmp, 0, groupsLen[0]);
            if(thisEntryLen > 1000)
              inc = thisEntryLen;
            else inc = 1000;
            groupsLen[0] += inc;
            groups = new byte[groupsLen[0]];
            System.arraycopy(tmp, 0, groups, 0, groupsLen[0] - inc);
          }
            
          generalUtils.catAsBytes(name + "\001", 0, groups, false);
        }
      }  

      personName          = generalUtils.stripLeadingAndTrailingSpaces(personName);
      companyName         = generalUtils.stripLeadingAndTrailingSpaces(companyName);
      jobTitle            = generalUtils.stripLeadingAndTrailingSpaces(jobTitle);
      eMail               = generalUtils.stripLeadingAndTrailingSpaces(eMail);
      notes               = generalUtils.stripLeadingAndTrailingSpaces(notes);
      customerCode        = generalUtils.stripLeadingAndTrailingSpaces(customerCode);
      supplierCode        = generalUtils.stripLeadingAndTrailingSpaces(supplierCode);
      organizationCode    = generalUtils.stripLeadingAndTrailingSpaces(organizationCode);
      externalCode        = generalUtils.stripLeadingAndTrailingSpaces(externalCode);
      externalPassWord    = generalUtils.stripLeadingAndTrailingSpaces(externalPassWord);
      phone1              = generalUtils.stripLeadingAndTrailingSpaces(phone1);
      phone2              = generalUtils.stripLeadingAndTrailingSpaces(phone2);
      phone3              = generalUtils.stripLeadingAndTrailingSpaces(phone3);
      fax                 = generalUtils.stripLeadingAndTrailingSpaces(fax);
  
      if(returnToWhere == null) returnToWhere = "";
              
      doIt(out, req, groups, userCode, unm, sid, uty, men, den, dnm, bnm, personName, companyName, jobTitle, eMail, notes, customerCode, supplierCode, organizationCode, externalCode, externalPassWord, externalRights, externalApproved, phone1,
           phone2, phone3, fax, newOrEditOrDelete, contactCode, returnToWhere, mailingList, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "ContactsDetailsUpdate", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8811, bytesOut[0], 0, "ERR:" + personName);
      if(out != null) out.flush();
    }
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, byte[] groups, String userCode, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String personName, String companyName, String jobTitle, String eMail,
                    String notes, String customerCode, String supplierCode, String organizationCode, String externalCode, String externalPassWord, String externalRights, String externalApproved, String phone1, String phone2, String phone3,
                    String fax, String newOrEditOrDelete, String contactCode, String returnToWhere, String mailingList, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 8801, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "8811", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8811, bytesOut[0], 0, "ACC:" + personName);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "8811a", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8811, bytesOut[0], 0, "SID:" + personName);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }
    
    if(newOrEditOrDelete.equals("D"))
    {
      profile.deleteContact(contactCode, dnm, localDefnsDir, defnsDir);
      displayC(out, personName.toUpperCase(), userCode, unm, sid, uty, men, den, dnm, bnm, bytesOut);
    }
    else
    {
      if(personName.length() == 0)
        personName = "-";
    
      if(companyName.length() == 0)
        companyName = "-";

      if(mailingList.equals("on"))
        mailingList = "Y";

      if(profile.updateContacts(con, stmt, rs, newOrEditOrDelete.charAt(0), contactCode, personName, companyName, jobTitle, notes, customerCode, supplierCode, organizationCode, externalCode, externalPassWord, externalRights, externalApproved,
                              phone1, phone2, phone3, fax, eMail, userCode, "", unm, mailingList) == ' ')
      {
        //personName = personName.toUpperCase();
        if(personName.length() > 0)
        {
          if(personName.toUpperCase().charAt(0) < 'A' || personName.toUpperCase().charAt(0) > 'Z')
            personName = "-"; // ensures will display the "others" page
        }   
   
        if(returnToWhere.length() == 0) // contacts
          displayC(out, personName.toUpperCase(), userCode, unm, sid, uty, men, den, dnm, bnm, bytesOut);
        else displayGroup(out, returnToWhere, unm, sid, uty, men, den, dnm, bnm, bytesOut);       
      }
      else messagePage.msgScreen(false, out, req, 23, unm, sid, uty, men, den, dnm, bnm, "8811", "", "", "", imagesDir, localDefnsDir, defnsDir, bytesOut);
    }

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 8811, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), personName);
    if(con != null) con.close();
    if(out != null) out.flush();
  }
   
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void displayC(PrintWriter out, String personName, String userCode, String unm, String sid, String uty, String men, String den, String dnm,
                        String bnm, int[] bytesOut) throws Exception
  {
    URL url = new URL("http://" + men + "/central/servlet/ContactsAddressBook?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=" + personName.charAt(0) + "&p3=" + userCode + "&bnm=" + bnm);

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

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void displayGroup(PrintWriter out, String group, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    URL url = new URL("http://" + men + "/central/servlet/SignaturesListDirectorya?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&dnm=" + dnm + "&p1=" + generalUtils.sanitise(group) + "&bnm=" + bnm);

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
