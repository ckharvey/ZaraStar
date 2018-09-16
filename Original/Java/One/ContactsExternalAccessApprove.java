// =======================================================================================================================================================================================================
// System: ZaraStar: Contacts: Update ext user access approved
// Module: ContactsExternalAccessApprove.java
// Author: C.K.Harvey
// Copyright (c) 2002-07 Christopher Harvey. All Rights Reserved.
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

public class ContactsExternalAccessApprove extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="";

    try
    {
      directoryUtils.setContentHeaders(res);
      out = res.getWriter();

      byte[] extNamesAndCompanyNames    = new byte[1000]; extNamesAndCompanyNames[0]    = '\000';
      int[]  extNamesAndCompanyNamesLen = new int[1];     extNamesAndCompanyNamesLen[0] = 1000;

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
        if(name.length() > 2 && name.substring(1, 3).equals(".x")) ;
        else
        if(name.length() > 2 && name.substring(1, 3).equals(".y")) ;
        else // must be checkbox value
        {
          thisEntryLen = name.length() + 2;
          if((generalUtils.lengthBytes(extNamesAndCompanyNames, 0) + thisEntryLen) >= extNamesAndCompanyNamesLen[0])
          {
            byte[] tmp = new byte[extNamesAndCompanyNamesLen[0]];
            System.arraycopy(extNamesAndCompanyNames, 0, tmp, 0, extNamesAndCompanyNamesLen[0]);
            if(thisEntryLen > 1000)
              inc = thisEntryLen;
            else inc = 1000;
            extNamesAndCompanyNamesLen[0] += inc;
            extNamesAndCompanyNames = new byte[extNamesAndCompanyNamesLen[0]];
            System.arraycopy(tmp, 0, extNamesAndCompanyNames, 0, extNamesAndCompanyNamesLen[0] - inc);
          }

          generalUtils.catAsBytes(name + "\001", 0, extNamesAndCompanyNames, false);
        }
      }
      
      doIt(out, req, res, unm, sid, uty, men, den, dnm, bnm, extNamesAndCompanyNames, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "ContactsExternalAccessApprove", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8819, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, HttpServletResponse res, String unm, String sid, String uty, String men,
                    String den, String dnm, String bnm, byte[] extNamesAndCompanyNames, int[] bytesOut) throws Exception
  {
    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    
    String userName = directoryUtils.getMySQLUserName();
    String passWord = directoryUtils.getMySQLPassWord();
    
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + userName + "&password=" + passWord);

    Statement stmt = con.createStatement(), stmt2 = null;
    ResultSet rs = null;

    rs = stmt.executeQuery("SELECT Name, CompanyName FROM contacts");

    String name, companyName, approved;
    
    while(rs.next())
    {
      name        = rs.getString(1);
      companyName = rs.getString(2);

      stmt2 = con.createStatement();

      if(isOnList(name, companyName, extNamesAndCompanyNames))
        approved = "Y";
      else approved = "N";

      stmt2.executeUpdate("UPDATE contacts SET ExternalApproved = '" + approved + "' WHERE Name = '" + name + "' AND CompanyName = '" + companyName + "'");

      if(stmt2 != null) stmt2.close();
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    if(con  != null) con.close();

    messagePage.msgScreen(false, out, req, 21, unm, sid, uty, men, den, dnm, bnm, "8819", imagesDir, localDefnsDir, defnsDir, bytesOut);
    if(out != null) out.flush();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean isOnList(String reqdName, String reqdCompanyName, byte[] extNamesAndCompanyNames) throws Exception
  {
    String thisName, thisCompanyName;
 
    int x=0, len = generalUtils.lengthBytes(extNamesAndCompanyNames, 0);
    while(x < len)
    {
      thisName = "";
      while(x < len && extNamesAndCompanyNames[x] != '\002')
        thisName += (char)extNamesAndCompanyNames[x++];
      thisName = generalUtils.deSanitise(thisName);

      ++x;
      thisCompanyName = "";
      while(x < len && extNamesAndCompanyNames[x] != '\001')
        thisCompanyName += (char)extNamesAndCompanyNames[x++];
      thisCompanyName = generalUtils.deSanitise(thisCompanyName);
      
      if(thisName.equals(reqdName) && thisCompanyName.equals(reqdCompanyName))
        return true;
      
      ++x;
    }
    
    return false;  
  }

}
