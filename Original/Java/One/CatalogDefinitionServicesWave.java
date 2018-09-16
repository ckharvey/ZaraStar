// =======================================================================================================================================================================================================
// System: ZaraStar Admin - Catalog Definition Services
// Module: CatalogDefinitionServicesWave.java
// Author: C.K.Harvey
// Copyright (c) 2003-09 Christopher Harvey. All Rights Reserved.
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

public class CatalogDefinitionServicesWave extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  AdminUtils adminUtils = new AdminUtils();

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

      unm = req.getParameter("unm");
      uty = req.getParameter("uty");
      sid = req.getParameter("sid");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");
      
      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "CatalogDefinitionServices", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 114, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush(); 
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    
    Statement stmt = null;
    ResultSet rs   = null;
    
    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 114, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "114", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 114, bytesOut[0], 0, "ACC:");
      
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "114", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 114, bytesOut[0], 0, "SID:");
      
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }
  
    create(con, stmt, rs, out, req, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 114, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void create(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir,
                      int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Admin - Catalog Definition Services</title>");
    
    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir)
                         + "general.css\">");

    int[] hmenuCount = new int[1];

    adminUtils.outputPageFrame(con, stmt, rs, out, req, "CatalogDefinitionServices", "", "114", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount, bytesOut);

    adminUtils.drawTitle(con, stmt, rs, req, out, "Administration - Catalog Definition Services", "114", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
  
    scoutln(out, bytesOut, "<table id=\"page\" border=0 xwidth=100%>");

    boolean ProductCatalogsAdmin = authenticationUtils.verifyAccess(con, stmt, rs, req, 2003, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean CatalogPDFCreate = authenticationUtils.verifyAccess(con, stmt, rs, req, 2038, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean StockServices  = authenticationUtils.verifyAccess(con, stmt, rs, req, 144,  unm, uty, dnm, localDefnsDir, defnsDir);
    boolean CatalogsListDefinition = authenticationUtils.verifyAccess(con, stmt, rs, req, 7032, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean AdminSetShowToWeb = authenticationUtils.verifyAccess(con, stmt, rs, req, 7079, unm, uty, dnm, localDefnsDir, defnsDir);
        
    if(ProductCatalogsAdmin || StockServices || CatalogPDFCreate || CatalogsListDefinition)
    {
      scoutln(out, bytesOut, "<tr><td nowrap><h1>Content</td></tr>");
    
      if(ProductCatalogsAdmin)
      {
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
        scoutln(out, bytesOut, "<a href=\"/central/servlet/ProductCatalogsAdmin?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">View</a> the Catalogs</td><td nowrap width=90% nowrap>"
                               + directoryUtils.buildHelp(2003) + "</td></tr>");
      }
      
      if(CatalogPDFCreate)
      {
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
        scoutln(out, bytesOut, "<a href=\"/central/servlet/CatalogPDFCreate?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">PDF</a> the Catalogs</td><td nowrap width=90% nowrap>"
                               + directoryUtils.buildHelp(2038) + "</td></tr>");
      }
      
      if(CatalogsListDefinition)
      {
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
        scoutln(out, bytesOut, "<a href=\"/central/servlet/CatalogsListDefinition?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Modify</a> the Catalog List</td><td nowrap width=90% nowrap>"
                               + directoryUtils.buildHelp(7032) + "</td></tr>");
      }
      
      if(StockServices)
      {
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
        scoutln(out, bytesOut, "<a href=\"/central/servlet/StockServices?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Stock Records</a> Focus</td><td nowrap width=90% nowrap>"
                               + directoryUtils.buildHelp(144) + "</td></tr>");
      }
      
      if(AdminSetShowToWeb)
      {
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
        scoutln(out, bytesOut, "<a href=\"/central/servlet/AdminSetShowToWeb?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Set</a> Show-to-Web on Stock Records</td><td nowrap width=90% nowrap>"
                               + directoryUtils.buildHelp(7079) + "</td></tr>");
      }
    }
    
    boolean TextMaintainDirectories = authenticationUtils.verifyAccess(con, stmt, rs, req, 6100, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean ImagesMaintainDirectories = authenticationUtils.verifyAccess(con, stmt, rs, req, 6500, unm, uty, dnm, localDefnsDir, defnsDir);
        
    if(TextMaintainDirectories || ImagesMaintainDirectories)
    {
      scoutln(out, bytesOut, "<tr><td nowrap><h1>Supporting Images and Text</td></tr>");
    
      if(TextMaintainDirectories)
      {
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
        scoutln(out, bytesOut, "<a href=\"/central/servlet/TextMaintainDirectories?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Modify</a> Text</td><td nowrap width=90% nowrap>"
                               + directoryUtils.buildHelp(6100) + "</td></tr>");
      }

      if(ImagesMaintainDirectories)
      {
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
        scoutln(out, bytesOut, "<a href=\"/central/servlet/ImagesMaintainDirectories?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Modify</a> Images</td><td nowrap width=90% nowrap>"
                               + directoryUtils.buildHelp(6500) + "</td></tr>");
      }
    }
    
    boolean AdminStockCategoriesDefinition = authenticationUtils.verifyAccess(con, stmt, rs, req, 7071, unm, uty, dnm, localDefnsDir, defnsDir);        
    boolean AdminCategoriseStock = authenticationUtils.verifyAccess(con, stmt, rs, req, 7072, unm, uty, dnm, localDefnsDir, defnsDir);        
        
    if(AdminStockCategoriesDefinition || AdminCategoriseStock)
    {
      scoutln(out, bytesOut, "<tr><td nowrap><h1>Categories</td></tr>");
    
      if(AdminStockCategoriesDefinition)
      {
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"/central/servlet/AdminStockCategoryDefinitionManufacturer?unm=" + unm + "&sid=" + sid + "&uty="
                           + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Modify</a> Stock Categories</td>"
                           + "<td nowrap width=90% nowrap>" + directoryUtils.buildHelp(7071) + "</td></tr>");
      }
        
      if(AdminCategoriseStock)
      {
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"/central/servlet/AdminCategoriseStock?unm=" + unm + "&sid=" + sid + "&uty="
                           + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Categorize</a> Stock</td>"
                           + "<td nowrap width=90% nowrap>" + directoryUtils.buildHelp(7072) + "</td></tr>");
      }
    }
    
    boolean CatalogZaraImport = authenticationUtils.verifyAccess(con, stmt, rs, req, 2010, unm, uty, dnm, localDefnsDir, defnsDir);        
    boolean CatalogSteelclawsExport = authenticationUtils.verifyAccess(con, stmt, rs, req, 2015, unm, uty, dnm, localDefnsDir, defnsDir);        
    boolean CatalogsRecreateGoogleIndexes = authenticationUtils.verifyAccess(con, stmt, rs, req, 7011, unm, uty, dnm, localDefnsDir, defnsDir);        
        
    if(CatalogZaraImport || CatalogSteelclawsExport || CatalogsRecreateGoogleIndexes)
    {
      scoutln(out, bytesOut, "<tr><td nowrap><h1>Zara-format Catalog</td></tr>");
    
      if(CatalogSteelclawsExport)
      {
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"/central/servlet/CatalogSteelclawsExport?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Export</a> Catalog</td>"
                             + "<td nowrap width=90% nowrap>" + directoryUtils.buildHelp(2015) + "</td></tr>");
      }

      if(CatalogZaraImport)
      {
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"/central/servlet/CatalogZaraImport?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Import</a> Catalog</td>"
                             + "<td nowrap width=90% nowrap>" + directoryUtils.buildHelp(2010) + "</td></tr>");
      }

      if(CatalogsRecreateGoogleIndexes)
      {
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"/central/servlet/CatalogsRecreateGoogleIndexes?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Re-Create</a> Google Pages</td>"
                             + "<td nowrap width=90% nowrap>" + directoryUtils.buildHelp(7011) + "</td></tr>");
      }
    }
    
    boolean CatalogLinkedDefinition = authenticationUtils.verifyAccess(con, stmt, rs, req, 2011, unm, uty, dnm, localDefnsDir, defnsDir);        
    boolean _7006 = authenticationUtils.verifyAccess(con, stmt, rs, req, 7006, unm, uty, dnm, localDefnsDir, defnsDir);
      
    if(CatalogLinkedDefinition || _7006)
    {
      scoutln(out, bytesOut, "<tr><td nowrap><h1>Linked Catalog</td></tr>");
      
      if(_7006)
      {
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
        scoutln(out, bytesOut, "<a href=\"/central/servlet/_7006?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Scan</a> for Published Catalogs</td><td nowrap width=90% nowrap>"
                               + directoryUtils.buildHelp(7006) + "</td></tr>");
      }     
  
      if(CatalogLinkedDefinition)
      {
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
        scoutln(out, bytesOut, "<a href=\"/central/servlet/CatalogLinkedDefinition?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Manage</a> Linked Catalogs and Listings</td>"
                               + "<td nowrap width=90% nowrap>" + directoryUtils.buildHelp(2011) + "</td></tr>");
      }
    }
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "</table>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir));
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
