// =======================================================================================================================================================================================================
// System: ZaraStar Admin: Stock category definition - fetch for edit
// Module: AdminStockCategoryDefinitionEdit.java
// Author: C.K.Harvey
// Copyright (c) 2006-07 Christopher Harvey. All Rights Reserved.
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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Enumeration;

public class AdminStockCategoryDefinitionEdit extends HttpServlet
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

    String unm="", sid="", uty="", dnm="", code="";

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
          code = value[0];
      }

      doIt(req, res, unm, sid, uty, dnm, code, bytesOut);
    }    
    catch(Exception e)
    {      
      System.out.println(("Unexpected System Error: 7071c: " + e));
      res.getWriter().write("Unexpected System Error: 7071c");
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(HttpServletRequest req, HttpServletResponse res, String unm, String sid, String uty, String dnm, String code,
                    int[] bytesOut) throws Exception
  {
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
 
    String[] desc           = new String[1]; desc[0]="";
    String[] image          = new String[1]; image[0]="";
    String[] mfr            = new String[1]; mfr[0]="";
    String[] page           = new String[1]; page[0]="";
    String[] download       = new String[1]; download[0]="";
    String[] text           = new String[1]; text[0]="";
    String[] categoryLink   = new String[1]; categoryLink[0]="";
    String[] text2          = new String[1]; text2[0]="";
    String[] noPrices       = new String[1]; noPrices[0]="";
    String[] noAvailability = new String[1]; noAvailability[0]="";
    String[] style          = new String[1]; style[0]="";
    String[] url            = new String[1]; url[0]="";
    String[] wikiPage       = new String[1]; wikiPage[0]="";
    String[] order          = new String[1]; order[0]="";
    
    String rtn="Unexpected System Error: 7071c";
    
    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      rtn = "Access Denied - Duplicate SignOn or Session Timeout";
    }
    else
    {
      if(fetch(dnm, code, desc, image, mfr, page, download, text, categoryLink, text2, noPrices, noAvailability, style, url, wikiPage, order,
               localDefnsDir, defnsDir))
      {
        rtn = ".";
      }
    }
    
    res.setContentType("text/xml");
    res.setHeader("Cache-Control", "no-cache");

    if(image[0].length() == 0)
      image[0] = ".";
    
    if(text[0].length() == 0)
      text[0] = ".";

    if(text2[0].length() == 0)
      text2[0] = ".";
    
    if(download[0].length() == 0)
      download[0] = ".";
   
    if(url[0].length() == 0)
      url[0] = ".";
   
    String s = "<msg><res>" + rtn + "</res><code>" + code + "</code><desc>" + generalUtils.sanitiseForXML(desc[0]) + "</desc><image>"
             + generalUtils.sanitiseForXML(image[0]) + "</image><mfr>" + generalUtils.sanitiseForXML(mfr[0]) + "</mfr><page>" + page[0] + "</page><download>"
             + generalUtils.sanitiseForXML(download[0]) + "</download><text>" + generalUtils.sanitiseForXML(text[0]) + "</text><categoryLink>"
             + categoryLink[0] + "</categoryLink><text2>" + generalUtils.sanitiseForXML(text2[0]) + "</text2><noPrices>" + noPrices[0]
             + "</noPrices><noAvailability>" + noAvailability[0] + "</noAvailability><style>" + style[0] + "</style><url>"
             + generalUtils.sanitiseForXML(url[0]) + "</url><wikiPage>" + wikiPage[0] + "</wikiPage><order>" + order[0] + "</order></msg>";
    res.getWriter().write(s);
    
    serverUtils.totalBytes(req, unm, dnm, 7071, bytesOut[0], 0, code);
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // rtns: ' ' if rec added/updated ok
  //       'E' if adding a rec that already exists
  //       'X' other error
  private boolean fetch(String dnm, String code, String[] desc, String[] image, String[] mfr, String[] page, String[] download, String[] text,
                        String[] categoryLink, String[] text2, String[] noPrices, String[] noAvailability, String[] style, String[] url,
                        String[] wikiPage, String[] order, String localDefnsDir, String defnsDir) throws Exception
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
  
      rs = stmt.executeQuery("SELECT Description, Image, Manufacturer, Page, Download, Text, CategoryLink, Text2, NoPrices, NoAvailability, "
                           + "Style, URL, WikiPage, OrderByDescription FROM stockcat WHERE CategoryCode = '" + code + "'"); 

      if(rs.next())                  
      {
        desc[0]           = rs.getString(1);
        image[0]          = rs.getString(2);
        mfr[0]            = rs.getString(3);
        page[0]           = rs.getString(4);
        download[0]       = rs.getString(5);
        text[0]           = rs.getString(6);
        categoryLink[0]   = rs.getString(7);
        text2[0]          = rs.getString(8);
        noPrices[0]       = rs.getString(9);
        noAvailability[0] = rs.getString(10);
        style[0]          = rs.getString(11);
        url[0]            = rs.getString(12);
        wikiPage[0]       = rs.getString(13);
        order[0]          = rs.getString(14);
      } 
                 
      rs.close();
        
      if(stmt != null) stmt.close();        
      if(con  != null) con.close();
            
      return true;
    }
    catch(Exception e)
    {
      System.out.println("7071c: " + e);
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }
    
    return false;
  }

}

