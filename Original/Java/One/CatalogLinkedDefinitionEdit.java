// =======================================================================================================================================================================================================
// System: ZaraStar Admin: linked catalog definition - fetch for edit
// Module: CatalogLinkedDefinitionEdit.java
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

public class CatalogLinkedDefinitionEdit extends HttpServlet
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

    String unm="", sid="", uty="", dnm="", mfr="";

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
      }

      doIt(req, res, unm, sid, uty, dnm, mfr, bytesOut);
    }    
    catch(Exception e)
    {      
      System.out.println(("Unexpected System Error: 2011c: " + e));
      res.getWriter().write("Unexpected System Error: 2011c");
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(HttpServletRequest req, HttpServletResponse res, String unm, String sid, String uty, String dnm, String mfr, int[] bytesOut)
                    throws Exception
  {
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
 
    String[] pricingUpline    = new String[1]; pricingUpline[0]="";
    String[] catalogUpline    = new String[1]; catalogUpline[0]="";
    String[] pricingURL       = new String[1]; pricingURL[0]="";
    String[] catalogURL       = new String[1]; catalogURL[0]="";
    String[] userName         = new String[1]; userName[0]="";
    String[] passWord         = new String[1]; passWord[0]="";
    String[] markup           = new String[1]; markup[0]="";
    String[] showPrices       = new String[1]; showPrices[0]="";
    String[] showAvailability = new String[1]; showAvailability[0]="";
    String[] desc             = new String[1]; desc[0]="";
    String[] disc1            = new String[1]; disc1[0]="";
    String[] disc2            = new String[1]; disc2[0]="";
    String[] disc3            = new String[1]; disc3[0]="";
    String[] disc4            = new String[1]; disc4[0]="";
    String[] catalogType      = new String[1]; catalogType[0]="";
    String[] currency         = new String[1]; currency[0]="";
    String[] priceBasis       = new String[1]; priceBasis[0]="";
    
    String rtn="Unexpected System Error: 2011c";
    
    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      rtn = "Access Denied - Duplicate SignOn or Session Timeout";
    }
    else
    {
      if(fetch(dnm, mfr, pricingUpline, catalogUpline, pricingURL, catalogURL, userName, passWord, markup, showPrices, showAvailability, desc, disc1,
               disc2, disc3, disc4, catalogType, currency, priceBasis, localDefnsDir, defnsDir))
      {
        rtn = ".";
      }
    }
    
    res.setContentType("text/xml");
    res.setHeader("Cache-Control", "no-cache");

    if(pricingUpline[0].length() == 0)
      pricingUpline[0] = ".";
    
    if(catalogUpline[0].length() == 0)
      catalogUpline[0] = ".";
    
    if(pricingURL[0].length() == 0)
      pricingURL[0] = ".";

    if(catalogURL[0].length() == 0)
      catalogURL[0] = ".";

    if(userName[0].length() == 0)
      userName[0] = ".";
    
    if(passWord[0].length() == 0)
      passWord[0] = ".";
   
    if(markup[0].length() == 0)
      markup[0] = "0";
   
    if(disc1[0].length() == 0)
      disc1[0] = "0";
   
    if(disc2[0].length() == 0)
      disc2[0] = "0";
   
    if(disc3[0].length() == 0)
      disc3[0] = "0";
   
    if(disc4[0].length() == 0)
      disc4[0] = "0";
   
    if(desc[0].length() == 0)
      desc[0] = ".";
   
    if(catalogType[0].length() == 0)
      catalogType[0] = "B";
   
    if(priceBasis[0].length() == 0)
      priceBasis[0] = "L";
   
    if(currency[0].length() == 0)
      currency[0] = ".";
   
    String s = "<msg><res>" + rtn + "</res><mfr>" + mfr + "</mfr><desc>" + generalUtils.sanitiseForXML(desc[0]) + "</desc><pricingUpline>"
             + generalUtils.sanitiseForXML(pricingUpline[0]) + "</pricingUpline><pricingURL>" + generalUtils.sanitiseForXML(pricingURL[0])
             + "</pricingURL><userName>" + userName[0] + "</userName><passWord>" + generalUtils.sanitiseForXML(passWord[0]) + "</passWord><markup>"
             + generalUtils.sanitiseForXML(markup[0]) + "</markup><showPrices>" + showPrices[0] + "</showPrices><showAvailability>" + showAvailability[0]
             + "</showAvailability><catalogURL>" + generalUtils.sanitiseForXML(catalogURL[0]) + "</catalogURL><catalogUpline>"
             + generalUtils.sanitiseForXML(catalogUpline[0]) + "</catalogUpline><disc1>" + generalUtils.sanitiseForXML(disc1[0]) + "</disc1><disc2>"
             + generalUtils.sanitiseForXML(disc2[0]) + "</disc2><disc3>" + generalUtils.sanitiseForXML(disc3[0]) + "</disc3><disc4>"
             + generalUtils.sanitiseForXML(disc4[0]) + "</disc4><catalogType>" + generalUtils.sanitiseForXML(catalogType[0]) + "</catalogType><currency>"
             + generalUtils.sanitiseForXML(currency[0]) + "</currency><priceBasis>" + generalUtils.sanitiseForXML(priceBasis[0]) + "</priceBasis></msg>";
    
    res.getWriter().write(s);
    
    serverUtils.totalBytes(req, unm, dnm, 2011, bytesOut[0], 0, mfr);
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // rtns: ' ' if rec added/updated ok
  //       'E' if adding a rec that already exists
  //       'X' other error
  private boolean fetch(String dnm, String mfr, String[] pricingUpline, String[] catalogUpline, String[] pricingURL, String[] catalogURL,
                        String[] userName, String[] passWord, String[] markup, String[] showPrices, String[] showAvailability, String[] desc,
                        String[] disc1, String[] disc2, String[] disc3, String[] disc4, String[] catalogType, String[] currency, String[] priceBasis,
                        String localDefnsDir, String defnsDir) throws Exception
  {
    Connection con = null;
    Statement stmt = null;
    ResultSet rs   = null;

    try
    {
      String uName = directoryUtils.getMySQLUserName();
      String pWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();
    
      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + uName + "&password=" + pWord);

      stmt = con.createStatement();
  
      rs = stmt.executeQuery("SELECT PricingUpline, PricingURL, UserName, PassWord, MarkupPercentage, ShowPrices, ShowAvailability, Description, "
                           + "CatalogUpline, CatalogURL, DiscountPercentage1, DiscountPercentage2, DiscountPercentage3, DiscountPercentage4, "
                           + "CatalogType, Currency, PriceBasis FROM linkedcat WHERE Manufacturer = '" + generalUtils.sanitiseForSQL(mfr) + "'"); 

      if(rs.next())                  
      {
        pricingUpline[0]    = rs.getString(1);
        pricingURL[0]       = rs.getString(2);
        userName[0]         = rs.getString(3);
        passWord[0]         = rs.getString(4);
        markup[0]           = rs.getString(5);
        showPrices[0]       = rs.getString(6);
        showAvailability[0] = rs.getString(7);
        desc[0]             = rs.getString(8);
        catalogUpline[0]    = rs.getString(9);
        catalogURL[0]       = rs.getString(10);
        disc1[0]            = rs.getString(11);
        disc2[0]            = rs.getString(12);
        disc3[0]            = rs.getString(13);
        disc4[0]            = rs.getString(14);
        catalogType[0]      = rs.getString(15);
        currency[0]         = rs.getString(16);
        priceBasis[0]       = rs.getString(17);
      } 
                 
      if(rs  != null) rs.close();        
      if(stmt != null) stmt.close();        
      if(con  != null) con.close();
            
      return true;
    }
    catch(Exception e)
    {
      System.out.println("2011c: " + e);
      if(rs  != null) rs.close();        
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }
    
    return false;
  }

}
