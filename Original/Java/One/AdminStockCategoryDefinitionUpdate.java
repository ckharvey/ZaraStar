// =======================================================================================================================================================================================================
// System: ZaraStar AdminEngine: Stock category definition - update
// Module: AdminStockCategoryDefinitionUpdate.java
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

public class AdminStockCategoryDefinitionUpdate extends HttpServlet
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

    String unm="", sid="", uty="", dnm="", code="", desc="", image="", mfr="", page="", download="", text="", categoryLink="", text2="",
           noPrices="", noAvailability="", style="", URL="", wikiPage="", order="", newOrEdit="";

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
        else
        if(elementName.equals("p2"))
          desc = value[0];
        else
        if(elementName.equals("p3"))
          image = value[0];
        else
        if(elementName.equals("p4"))
          mfr = value[0];
        else
        if(elementName.equals("p5"))
          page = value[0];
        else
        if(elementName.equals("p6"))
          download = value[0];
        else
        if(elementName.equals("p7"))
          text = value[0];
        else
        if(elementName.equals("p8"))
          categoryLink = value[0];
        else
        if(elementName.equals("p9"))
          text2 = value[0];
        else
        if(elementName.equals("p10"))
          noPrices = value[0];
        else
        if(elementName.equals("p11"))
          noAvailability = value[0];
        else
        if(elementName.equals("p12"))
          style = value[0];
        else
        if(elementName.equals("p13"))
          URL = value[0];
        else
        if(elementName.equals("p14"))
          wikiPage = value[0];
        else
        if(elementName.equals("p15"))
          order = value[0];
        else
        if(elementName.equals("p16"))
          newOrEdit = value[0];
      }
                         
      doIt(req, res, unm, sid, uty, dnm, code, desc, image, mfr, page, download, text, categoryLink, text2, noPrices, noAvailability, style, URL,
           wikiPage, order, newOrEdit, bytesOut);
    }    
    catch(Exception e)
    {
      StringBuffer url = req.getRequestURL();
      int x=0;
      if(url.charAt(x) == 'h' || url.charAt(x) == 'H')
        x += 7;
      String urlBit="";
      while(url.charAt(x) != '\000' && url.charAt(x) != ',' && url.charAt(x) != '/')
        urlBit += url.charAt(x++);
      
      System.out.println(("Unexpected System Error: 7071a: " + e));
      res.getWriter().write("Unexpected System Error: 7071a");
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(HttpServletRequest req, HttpServletResponse res, String unm, String sid, String uty, String dnm, String code,
                    String desc, String image, String mfr, String page, String download, String text, String categoryLink, String text2,
                    String noPrices, String noAvailability, String style, String url, String wikiPage, String order, String newOrEdit,
                    int[] bytesOut) throws Exception
  {
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
 
    String rtn="Unexpected System Error: 7071a";
    
    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      rtn = "Access Denied - Duplicate SignOn or Session Timeout";
    }
    else
    {
      code         = generalUtils.stripLeadingAndTrailingSpaces(code);
      desc         = generalUtils.stripLeadingAndTrailingSpaces(desc);
      image        = generalUtils.stripLeadingAndTrailingSpaces(image);
      page         = generalUtils.stripLeadingAndTrailingSpaces(page);
      download     = generalUtils.stripLeadingAndTrailingSpaces(download);
      categoryLink = generalUtils.stripLeadingAndTrailingSpaces(categoryLink);
      text2        = generalUtils.stripLeadingAndTrailingSpaces(text2);
      url          = generalUtils.stripLeadingAndTrailingSpaces(url);
      wikiPage     = generalUtils.stripLeadingAndTrailingSpaces(wikiPage);
      
      if(code.length() == 0)
        rtn = "No Category Code Entered";
      else
      if(desc.length() == 0)
        rtn = "No Description Entered";
      else
      if(page.length() == 0)
        rtn = "No Page Entered";
      else
      if(! generalUtils.isInteger(code))
        rtn = "Invalid Category Code Entered";
      else
      if(! generalUtils.isInteger(categoryLink))
        rtn = "Invalid Category Link Code Entered";
      else
      if(! generalUtils.isInteger(wikiPage))
        rtn = "Invalid Wiki PageCode Entered";
      else
      if(! generalUtils.isInteger(download))
        rtn = "Invalid Download Library Code Entered";
      else
      if(! generalUtils.isInteger(page))
        rtn = "Invalid Page Entered";
      else
      {
        boolean newRec;
        if(newOrEdit.equals("N"))
          newRec = true;
        else newRec = false;

        if(download.length() == 0)
          download = "0";

        if(categoryLink.length() == 0)
          categoryLink = "0";
   
        if(wikiPage.length() == 0)
          wikiPage = "0";
   
        switch(update(newRec, dnm, code, desc, image, mfr, page, download, text, categoryLink, text2, noPrices, noAvailability, style, url, wikiPage, 
                      order, localDefnsDir, defnsDir))
        {
          case ' ' : rtn = ".";                             break;
          case 'E' : rtn = "Stock Category Already Exists"; break;
        }
      }
    }
    
    res.setContentType("text/xml");
    res.setHeader("Cache-Control", "no-cache");
    
    String s = "<msg><res>" + rtn + "</res></msg>";
    res.getWriter().write(s);
    
    serverUtils.totalBytes(req, unm, dnm, 7071, bytesOut[0], 0, code);
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // rtns: ' ' if rec added/updated ok
  //       'E' if adding a rec that already exists
  //       'X' other error
  private char update(boolean newRec, String dnm, String code, String desc, String image, String mfr, String page, String download, String text,
                      String categoryLink, String text2, String noPrices, String noAvailability, String style, String url, String wikiPage,
                      String order, String localDefnsDir, String defnsDir) throws Exception
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

      if(newRec)
      {    
        // if adding a new rec, check if already exists
        boolean alreadyExists=false;
        stmt = con.createStatement();
  
        rs = stmt.executeQuery("SELECT CategoryCode FROM stockcat WHERE CategoryCode = '" + code + "'"); 
        if(rs.next())
          alreadyExists = true;
        rs.close() ;
  
        if(stmt != null) stmt.close();
      
        if(alreadyExists)
        {
          if(con != null) con.close();
          return 'E';
        }
  
        stmt = con.createStatement();
           
        String q = "INSERT INTO stockcat ( CategoryCode, Description, Image, Manufacturer, Page, Download, Text, CategoryLink, Text2, NoPrices, "
                 + "NoAvailability, Style, URL, WikiPage, OrderByDescription ) VALUES ('" + code + "','" + generalUtils.sanitiseForSQL(desc) + "','" + image
                 + "','" + mfr + "','" + page + "','" + download + "','" + generalUtils.sanitiseForSQL(text) + "','" + categoryLink + "','"
                 + generalUtils.sanitiseForSQL(text2) + "','" + noPrices + "','" + noAvailability + "','" + style + "','" + url + "','" + wikiPage + "','"
                 + order + "' )";
        stmt.executeUpdate(q);
        
        if(stmt != null) stmt.close();
        if(con  != null) con.close();
        
        return ' ';
      }
  
      // else update existing rec  
      stmt = con.createStatement();
        
      String q = "UPDATE stockcat SET Description = '" + generalUtils.sanitiseForSQL(desc) + "', Image = '" + image + "', Manufacturer = '" + mfr
               + "', Page = '" + page + "', Download = '" + download + "', Text = '" + generalUtils.sanitiseForSQL(text) + "', CategoryLink = '"
               + categoryLink + "', Text2 = '" + generalUtils.sanitiseForSQL(text2) + "', NoPrices = '" + noPrices + "', NoAvailability = '" + noAvailability
               + "', Style = '" + style + "', URL = '" + url + "', WikiPage = '" + wikiPage + "', OrderByDescription = '" + order
               + "' WHERE CategoryCode = '" + code + "'";
      stmt.executeUpdate(q);
        
      if(stmt != null) stmt.close();        
      if(con  != null) con.close();
            
      return ' ';
    }
    catch(Exception e)
    {
      System.out.println("7071a: " + e);
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }
    
    return 'X';
  }

}

