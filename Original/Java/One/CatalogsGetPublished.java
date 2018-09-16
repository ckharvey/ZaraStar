// =======================================================================================================================================================================================================
// System: ZaraStar Catalogs: Get Published Catalogs
// Module: CatalogsListDefinition.java
// Author: C.K.Harvey
// Copyright (c) 2003-08 Christopher Harvey. All Rights Reserved.
// Note: Checks all domains on this server
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

public class CatalogsGetPublished extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  DefinitionTables definitionTables = new DefinitionTables();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", scDNM="", oneOrAll="", reqdMfr="", reqdCatalogType="";

    try
    {
      directoryUtils.setContentHeaders(res);
      out = res.getWriter();

      unm             = req.getParameter("unm");
      scDNM           = req.getParameter("p1");
      oneOrAll        = req.getParameter("p2");
      reqdMfr         = req.getParameter("p3");
      reqdCatalogType = req.getParameter("p4");
      
      doIt(out, req, unm, scDNM, oneOrAll, reqdMfr, reqdCatalogType, bytesOut);
    }
    catch(Exception e)
    {
      out.print("ERR:7032d:" + e);
      serverUtils.etotalBytes(req, unm, "", 7032, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String scDNM, String oneOrAll, String reqdMfr, String reqdCatalogType,
                    int[] bytesOut) throws Exception
  {
    forADomain(out, req, unm, scDNM, oneOrAll, reqdMfr, reqdCatalogType, bytesOut);

    if(out != null) out.flush();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void forADomain(PrintWriter out, HttpServletRequest req, String unm, String scDNM, String oneOrAll, String reqdMfr, String reqdCatalogType,
                          int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String[] companyName             = new String[1];
    String[] applicationStartDate    = new String[1];
    String[] financialYearStartMonth = new String[1];
    String[] financialYearEndMonth   = new String[1];
    String[] effectiveStartDate      = new String[1];
    String[] companyPhone            = new String[1];
    String[] companyFax              = new String[1];
    String[] dateStyle               = new String[1];
    String[] dateSeparator           = new String[1];
    String[] description             = new String[1];
    String[] companyEMail            = new String[1];
    String[] terseName               = new String[1];
    String[] latitude                = new String[1];
    String[] longitude               = new String[1];
    String[] googleMapsKey           = new String[1];
    String[] currentStyle            = new String[1];


    Connection con = null;
    Statement stmt = null, stmt2 = null;
    ResultSet rs   = null, rs2   = null;

    try
    {
      String uName = directoryUtils.getMySQLUserName();
      String pWord = directoryUtils.getMySQLPassWord();
      Class.forName("com.mysql.jdbc.Driver").newInstance();
      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + scDNM + "_ofsa?user=" + uName + "&password=" + pWord);

    definitionTables.getAppConfig(con, stmt, rs, scDNM, companyName, applicationStartDate, financialYearStartMonth, financialYearEndMonth, effectiveStartDate, companyPhone, companyFax, dateStyle, dateSeparator, description, companyEMail, terseName, latitude,
                 longitude, googleMapsKey, currentStyle);
    }
    catch(Exception e) { }

    serverUtils.totalBytes(con, stmt, rs, req, unm, scDNM, 7032, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con  != null) con.close();
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void list(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, String oneOrAll,
                    String companyName, String domainName, String pricingUpline, String reqdMfr, String reqdCatalogType, String imagesDir)
                    throws Exception
  {
    try
    {
      stmt = con.createStatement();
  
      rs = stmt.executeQuery("SELECT Manufacturer, CatalogType, Title, Image, PublishPricing, PublishAvailability FROM cataloglist "
                           + "WHERE PublishCatalog = 'Y' ORDER BY Manufacturer");

      String mfr, title, image, catalogType, publishPricing, publishAvailability;
      String[] catalogURL    = new String[1];
      String[] catalogUpline = new String[1];
      boolean wanted;

      while(rs.next())                  
      {        
        mfr                 = rs.getString(1);
        catalogType         = rs.getString(2);

        if(oneOrAll.equals("O"))
        {
          if(mfr.equals(reqdMfr) && (catalogType.equals("B") || catalogType.equals(reqdCatalogType)))
            wanted = true;
          else wanted = false;
        }
        else wanted = true;
        
        if(wanted)
        {     
          title               = rs.getString(3);
          image               = rs.getString(4);
          publishPricing      = rs.getString(5);
          publishAvailability = rs.getString(6);
        
          if(! getCatalogSource(con, stmt2, rs2, mfr, catalogType, catalogURL, catalogUpline))
            catalogURL[0] = catalogUpline[0] = "___SAME___";
          
          out.println(companyName + "\001" + mfr + "\001" + title + "\001" + domainName + "\001" + catalogType + "\001" + pricingUpline + "\001"
                      + "http://" + domainName + imagesDir + image + "\001" + publishPricing + "\001" + publishAvailability + "\001"
                      + catalogURL[0] + "\001" + catalogUpline[0] + "\001");
        }
      }

      if(rs   != null) rs.close();                                 
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(rs   != null) rs.close();                                 
      if(stmt != null) stmt.close();
    }    
  }  

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean getCatalogSource(Connection con, Statement stmt, ResultSet rs, String mfr, String reqdCatalogType, String[] catalogURL,
                                   String[] catalogUpline) throws Exception
  {
    catalogURL[0] = catalogUpline[0] = "";
    boolean foundInLinked = false;
    
    try
    {
      stmt = con.createStatement();
  
      rs = stmt.executeQuery("SELECT CatalogType, CatalogURL, CatalogUpline FROM linkedcat WHERE Manufacturer = '" + generalUtils.sanitiseForSQL(mfr) + "'");

      String catalogType;
      
      while(rs.next())
      {        
        catalogType = rs.getString(1);
        
        if(catalogType.equals("B") || catalogType.equals(reqdCatalogType))
        {
          catalogURL[0]    = rs.getString(2);
          catalogUpline[0] = rs.getString(3);
          foundInLinked = true;
        }
      }

      if(rs   != null) rs.close();                                 
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(rs   != null) rs.close();                                 
      if(stmt != null) stmt.close();
    }
    
    return foundInLinked;
  }  

}
