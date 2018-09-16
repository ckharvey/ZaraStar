// =======================================================================================================================================================================================================
// System: ZaraStar Admin: Linked catalog definition - update
// Module: CatalogLinkedDefinitionUpdate.java
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

public class CatalogLinkedDefinitionUpdate extends HttpServlet
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

    String unm="", sid="", uty="", dnm="", mfr="", desc="", pricingUpline="", pricingURL="", userName="", passWord="", markup="", showPrices="",
           showAvailability="", catalogURL="", catalogUpline="", disc1="", disc2="", disc3="", disc4="", catalogType="", currency="", priceBasis="",
           newOrEdit="";

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
          desc = value[0];
        else
        if(elementName.equals("p3"))
          pricingUpline = value[0];
        else
        if(elementName.equals("p4"))
          pricingURL = value[0];
        else
        if(elementName.equals("p5"))
          userName = value[0];
        else
        if(elementName.equals("p6"))
          passWord = value[0];
        else
        if(elementName.equals("p7"))
          markup = value[0];
        else
        if(elementName.equals("p8"))
          showPrices = value[0];
        else
        if(elementName.equals("p9"))
          showAvailability = value[0];
        else
        if(elementName.equals("p10"))
          catalogURL = value[0];
        else
        if(elementName.equals("p11"))
          catalogUpline = value[0];
        else
        if(elementName.equals("p12"))
          disc1 = value[0];
        else
        if(elementName.equals("p13"))
          disc2 = value[0];
        else
        if(elementName.equals("p14"))
          disc3 = value[0];
        else
        if(elementName.equals("p15"))
          disc4 = value[0];
        else
        if(elementName.equals("p16"))
          catalogType = value[0];
        else
        if(elementName.equals("p17"))
          currency = value[0];
        else
        if(elementName.equals("p18"))
          priceBasis = value[0];
        else
        if(elementName.equals("p19"))
          newOrEdit = value[0];
      }
                         
      doIt(req, res, unm, sid, uty, dnm, mfr, desc, pricingUpline, pricingURL, catalogUpline, catalogURL, userName, passWord, markup, showPrices,
           showAvailability, disc1, disc2, disc3, disc4, catalogType, currency, priceBasis, newOrEdit, bytesOut);
    }    
    catch(Exception e)
    {
      System.out.println(("Unexpected System Error: 2011a: " + e));
      res.getWriter().write("Unexpected System Error: 2011a");
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(HttpServletRequest req, HttpServletResponse res, String unm, String sid, String uty, String dnm, String mfr, String desc,
                    String pricingUpline, String pricingURL, String catalogUpline, String catalogURL, String userName, String passWord, String markup,
                    String showPrices, String showAvailability, String disc1, String disc2, String disc3, String disc4, String catalogType,
                    String currency, String priceBasis, String newOrEdit, int[] bytesOut) throws Exception
  {
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
 
    String rtn="Unexpected System Error: 2011a";
    
    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      rtn = "Access Denied - Duplicate SignOn or Session Timeout";
    }
    else
    {
      mfr           = generalUtils.stripLeadingAndTrailingSpaces(mfr);
      desc          = generalUtils.stripLeadingAndTrailingSpaces(desc);
      pricingUpline = generalUtils.stripLeadingAndTrailingSpaces(generalUtils.capitalize(pricingUpline));
      catalogUpline = generalUtils.stripLeadingAndTrailingSpaces(generalUtils.capitalize(catalogUpline));
      pricingURL    = generalUtils.stripAllSpaces(pricingURL);
      catalogURL    = generalUtils.stripAllSpaces(catalogURL);
      userName      = generalUtils.stripAllSpaces(userName);
      passWord      = generalUtils.stripAllSpaces(passWord);
      markup        = generalUtils.stripAllSpaces(markup);
      disc1         = generalUtils.stripAllSpaces(disc1);
      disc2         = generalUtils.stripAllSpaces(disc2);
      disc3         = generalUtils.stripAllSpaces(disc3);
      disc4         = generalUtils.stripAllSpaces(disc4);
      
      if(markup.length() == 0)
        markup = "0";

      if(disc1.length() == 0)
        disc1 = "0";

      if(disc2.length() == 0)
        disc2 = "0";

      if(disc3.length() == 0)
        disc3 = "0";

      if(disc4.length() == 0)
        disc4 = "0";

      if(mfr.length() == 0)
        rtn = "No Manufacturer Entered";
      else
      if(desc.length() == 0)
        rtn = "No Description Entered";
      else
      if(pricingUpline.length() == 0)
        rtn = "No Pricing Upline Entered";
      else
      if(catalogUpline.length() == 0)
        rtn = "No Catalog Upline Entered";
      else
      if(pricingURL.length() == 0)
        rtn = "No Pricing URL Entered";
      else
      if(catalogURL.length() == 0)
        rtn = "No Catalog URL Entered";
      else
      if(userName.length() == 0)
        rtn = "No UserName Entered";
      else
      if(passWord.length() == 0)
        rtn = "No PassWord Entered";     
      else
      if(! generalUtils.isNumeric(markup))
        rtn = "Invalid Markup Percentage Entered";
      else
      {
        boolean newRec;
        if(newOrEdit.equals("N"))
          newRec = true;
        else newRec = false;

        switch(update(newRec, dnm, mfr, desc, pricingUpline, pricingURL, catalogUpline, catalogURL, userName, passWord, markup, showPrices,
                      showAvailability, disc1, disc2, disc3, disc4, catalogType, currency, priceBasis, localDefnsDir, defnsDir))
        {
          case ' ' : rtn = ".";                        
                     break;
          case 'E' : if(catalogType.equals("C"))
                       rtn = "Manufacturer Catalog Already Exists";
                     else rtn = "Manufacturer Listing Already Exists";
                     break;
        }
      }
    }
    
    res.setContentType("text/xml");
    res.setHeader("Cache-Control", "no-cache");
    
    String s = "<msg><res>" + rtn + "</res></msg>";
    res.getWriter().write(s);
    
    serverUtils.totalBytes(req, unm, dnm, 2011, bytesOut[0], 0, mfr);
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // rtns: ' ' if rec added/updated ok
  //       'E' if adding a rec that already exists
  //       'X' other error
  private char update(boolean newRec, String dnm, String mfr, String desc, String pricingUpline, String pricingURL, String catalogUpline,
                      String catalogURL, String userName, String passWord, String markup, String showPrices, String showAvailability, String disc1,
                      String disc2, String disc3, String disc4, String catalogType, String currency, String priceBasis, String localDefnsDir,
                      String defnsDir) throws Exception
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

      if(newRec)
      {    
        // if adding a new rec, check if already exists
        boolean alreadyExists=false;
        stmt = con.createStatement();
  
        rs = stmt.executeQuery("SELECT Manufacturer FROM linkedcat WHERE Manufacturer = '" + generalUtils.sanitiseForSQL(mfr) + "' AND CatalogType = '"
                             + catalogType + "'"); 
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
           
        String q = "INSERT INTO linkedcat ( Manufacturer, PricingUpline, PricingURL, CatalogUpline, CatalogURL, UserName, PassWord, "
                 + "MarkupPercentage, ShowPrices, ShowAvailability, Description, DiscountPercentage1, DiscountPercentage2, DiscountPercentage3, "
                 + "DiscountPercentage4, CatalogType, Currency, PriceBasis ) VALUES ('" + mfr + "','" + generalUtils.sanitiseForSQL(pricingUpline) + "','"
                 + generalUtils.sanitiseForSQL(pricingURL) + "','" + generalUtils.sanitiseForSQL(catalogUpline) + "','" + generalUtils.sanitiseForSQL(catalogURL) + "','"
                 + generalUtils.sanitiseForSQL(userName) + "','" + generalUtils.sanitiseForSQL(passWord) + "','" + markup + "','" + showPrices + "','"
                 + showAvailability + "','" + generalUtils.sanitiseForSQL(desc) + "','" + disc1 + "','" + disc2 + "','" + disc3 + "','" + disc4 + "','"
                 + catalogType + "','" + currency + "','" + priceBasis + "' )";
        
        stmt.executeUpdate(q);
        
        if(stmt != null) stmt.close();
        if(con  != null) con.close();
        
        return ' ';
      }
  
      // else update existing rec  
      stmt = con.createStatement();
        
      String q = "UPDATE linkedcat SET PricingUpline = '" + generalUtils.sanitiseForSQL(pricingUpline) + "', CatalogUpline = '"
               + generalUtils.sanitiseForSQL(catalogUpline) + "', PricingURL = '" + generalUtils.sanitiseForSQL(pricingURL) + "', UserName = '"
               + generalUtils.sanitiseForSQL(userName) + "', PassWord = '" + generalUtils.sanitiseForSQL(passWord) + "', MarkupPercentage = '"
               + generalUtils.sanitiseForSQL(markup) + "', ShowPrices = '" + showPrices + "', ShowAvailability = '" + showAvailability
               + "', Description = '" + generalUtils.sanitiseForSQL(desc) + "', CatalogURL = '" + generalUtils.sanitiseForSQL(catalogURL)
               + "', DiscountPercentage1 = '" + generalUtils.sanitiseForSQL(disc1) + "', DiscountPercentage2 = '" + generalUtils.sanitiseForSQL(disc2)
               + "', DiscountPercentage3 = '" + generalUtils.sanitiseForSQL(disc3) + "', DiscountPercentage4 = '" + generalUtils.sanitiseForSQL(disc4)
               + "', CatalogType = '" + generalUtils.sanitiseForSQL(catalogType) + "', Currency = '" + currency + "', PriceBasis = '" + priceBasis
               + "' WHERE Manufacturer = '" + generalUtils.sanitiseForSQL(mfr) + "'";
      
      stmt.executeUpdate(q);
        
      if(stmt != null) stmt.close();        
      if(con  != null) con.close();
            
      return ' ';
    }
    catch(Exception e)
    {
      System.out.println("2011a: " + e);
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }
    
    return 'X';
  }

}


