// =======================================================================================================================================================================================================
// System: ZaraStar Catalogs: Catalog List definition - fetch for edit
// Module: CatalogsListDefinitionEdit.java
// Author: C.K.Harvey
// Copyright (c) 2006-08 Christopher Harvey. All Rights Reserved.
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
import java.util.Enumeration;

public class CatalogsListDefinitionEdit extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", dnm="", mfr="", type="";

    try
    {
      directoryUtils.setContentHeaders(res);
      res.setHeader("Header", "HTTP/1.0 200 OK");
      res.setStatus(200);

      Enumeration en = req.getParameterNames();
      while(en.hasMoreElements())
      {
        String elementName = (String)en.nextElement();
        String[] value = req.getParameterValues(elementName);
        if(elementName.equals("unm"))
          unm = value[0];
        else
        if(elementName.equals("sid"))
          sid = value[0];
        else
        if(elementName.equals("uty"))
          uty = value[0];
        else
        if(elementName.equals("dnm"))
          dnm = value[0];
        else
        if(elementName.equals("p1"))
          mfr = value[0];
        else
        if(elementName.equals("p2"))
          type = value[0];
      }

      doIt(req, res, unm, sid, uty, dnm, mfr, type, bytesOut);
    }    
    catch(Exception e)
    {
      System.out.println(("Unexpected System Error: 7032c: " + e));
      res.getWriter().write("Unexpected System Error: 7032c");
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(HttpServletRequest req, HttpServletResponse res, String unm, String sid, String uty, String dnm, String mfr, String type,
                    int[] bytesOut) throws Exception
  {
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
 
    String[] title               = new String[1]; title[0]="";
    String[] image               = new String[1]; image[0]="";
    String[] desc                = new String[1]; desc[0]="";
    String[] catalogType         = new String[1]; catalogType[0]="";
    String[] publishCatalog      = new String[1]; publishCatalog[0]="";
    String[] publishPricing      = new String[1]; publishPricing[0]="";
    String[] publishAvailability = new String[1]; publishAvailability[0]="";
    
    String rtn="Unexpected System Error: 7032c";
    
    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      rtn = "Access Denied - Duplicate SignOn or Session Timeout";
    }
    else
    {
      if(fetch(dnm, mfr, type, title, image, desc, catalogType, publishCatalog, publishPricing, publishAvailability, localDefnsDir, defnsDir))
        rtn = ".";
      if(image[0].length() == 0)
        image[0] = ".";
      if(desc[0].length() == 0)
        desc[0] = ".";
      if(title[0].length() == 0)
        title[0] = ".";
    }
    
    res.setContentType("text/xml");
    res.setHeader("Cache-Control", "no-cache");
    
    String s = "<msg><res>" + rtn + "</res><mfr><![CDATA[" + mfr + "]]></mfr><title><![CDATA[" + title[0] + "]]></title><image><![CDATA[" + image[0]
             + "]]></image><desc><![CDATA[" + desc[0] + "]]></desc><type>" + type + "</type><catalogType>" + catalogType[0]
             + "</catalogType><publishCatalog>" + publishCatalog[0] + "</publishCatalog><publishPricing>" + publishPricing[0]
             + "</publishPricing><publishAvailability>" + publishAvailability[0] + "</publishAvailability></msg>";
    res.getWriter().write(s);
    
    serverUtils.totalBytes(req, unm, dnm, 7032, bytesOut[0], 0, mfr);
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // rtns: ' ' if rec added/updated ok
  //       'E' if adding a rec that already exists
  //       'X' other error
  private boolean fetch(String dnm, String mfr, String type, String[] title, String[] image, String[] desc, String[] catalogType,
                        String[] publishCatalog, String[] publishPricing, String[] publishAvailability, String localDefnsDir, String defnsDir)
                        throws Exception
  {
    Connection con = null;
    Statement stmt = null;
    ResultSet rs   = null;

    try
    {
      String uName    = directoryUtils.getMySQLUserName();
      String passWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();
    
      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + uName + "&password=" + passWord);

      stmt = con.createStatement();
  
      rs = stmt.executeQuery("SELECT Title, Image, Description, CatalogType, PublishCatalog, PublishPricing, PublishAvailability "
                           + "FROM cataloglist WHERE Manufacturer = '" + generalUtils.sanitiseForSQL(mfr) + "' AND Type = '" + type + "'"); 

      if(rs.next())                  
      {
        title[0]               = rs.getString(1);
        image[0]               = rs.getString(2);
        desc[0]                = rs.getString(3);
        catalogType[0]         = rs.getString(4);
        publishCatalog[0]      = rs.getString(5);
        publishPricing[0]      = rs.getString(6);
        publishAvailability[0] = rs.getString(7);
      } 
                 
      if(rs   != null) rs.close();        
      if(stmt != null) stmt.close();        
      if(con  != null) con.close();
            
      return true;
    }
    catch(Exception e)
    {
      System.out.println("7032c: " + e);
      if(rs   != null) rs.close();        
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }
    
    return false;
  }

}
