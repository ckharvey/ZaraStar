// =======================================================================================================================================================================================================
// System: ZaraStar Product: Catalogs list page
// Module: CatalogList.java
// Author: C.K.Harvey
// Copyright (c) 2002-08 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.sql.*;
import java.net.*;

public class CatalogList extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  Profile profile = new Profile();
  AdminControlUtils adminControlUtils = new AdminControlUtils();
  
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
      sid = req.getParameter("sid");
      uty = req.getParameter("uty");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");
      
      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, bytesOut);
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
 
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "CatalogList", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2003, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                    int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesLibDir  = directoryUtils.getImagesDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    
    Statement stmt = null, stmt2 = null, stmt3 = null;
    ResultSet rs   = null, rs2   = null, rs3   = null;
    
    if((uty.equals("R") && ! adminControlUtils.notDisabled(con, stmt, rs, 902)) || (uty.equals("A") && ! adminControlUtils.notDisabled(con, stmt, rs, 802)) || (uty.equals("I") && ! authenticationUtils.verifyAccess(con, stmt, rs, req, 2003, unm, uty, dnm, localDefnsDir, defnsDir)))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "CatalogList", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2003, bytesOut[0], 0, "ACC:");
      
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      if(uty.equals("A"))
      {
        String sessionsDir = directoryUtils.getSessionsDir(dnm);
        sid = serverUtils.newSessionID(unm, "A", dnm, sessionsDir, localDefnsDir, defnsDir);
        den = dnm;
        unm = "_" + sid;
        StringBuffer url = req.getRequestURL();
        int x=0;
        if(url.charAt(x) == 'h' || url.charAt(x) == 'H')
          x += 7;
        men="";
        while(url.charAt(x) != '\000' && url.charAt(x) != ',' && url.charAt(x) != '/')
          men += url.charAt(x++);
      }
      else
      {
        messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "CatalogList", imagesDir, localDefnsDir, defnsDir, bytesOut);
        serverUtils.etotalBytes(req, unm, dnm, 2003, bytesOut[0], 0, "SID:");
        
        if(con != null) con.close();
        if(out != null) out.flush(); 
        return;
      }
    }

    set(con, stmt, stmt2, stmt3, rs, rs2, rs3, out, req, unm, sid, uty, men, den, dnm, bnm, imagesLibDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 2003, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, ResultSet rs3,
                   PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                   String imagesLibDir, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Catalogs List</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    scoutln(out, bytesOut, "function mfr(which){");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/CatalogUtils?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+escape(which);}");

    scoutln(out, bytesOut, "function mfr2(which){");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/ProductManufacturerItems?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=F&p1=\"+escape(which);}");

    scoutln(out, bytesOut, "function mfr3(which,catType){");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/CatalogFetchLinked?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=\"+catType+\"&p1=\"+escape(which);}");

    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\"></head>");
  
    int[] hmenuCount = new int[1];
 
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "2003", "", "ProductCatalogsAdmin", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount,
                          bytesOut);
   
    if(uty.equals("R"))
    {
      String[] name         = new String[1];
      String[] companyName  = new String[1];
      String[] accessRights = new String[1];
      int i = unm.indexOf("_");

      profile.getExternalAccessNameCompanyAndRights(con, stmt, rs, unm.substring(0, i), name, companyName, accessRights);

      pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Catalog Services for " + name[0] + " of " + companyName[0], "2003", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);
    }
    else
    if(uty.equals("A"))
     pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Catalog Services (Casual User)", "2003", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);
    else pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Catalogs", "2003", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<form><table id=\"page\" width=100% border=0 cellpadding=4 cellspacing=0>");

    String mfrList = listMfrs(con, stmt, stmt2, stmt3, rs, rs2, rs3, out, uty, imagesLibDir, bytesOut,       unm);
    
    scoutln(out, bytesOut, "</table><hr><table id=\"page\" width=100% border=0 cellpadding=2 cellspacing=0>");

    listMfrsWithNoEntry(con, stmt, stmt2, rs, rs2, out, mfrList, uty, bytesOut);
        
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
  
    scoutln(out, bytesOut, "</table></form>");     scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }
    
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private String listMfrs(Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, ResultSet rs3,
                          PrintWriter out, String uty, String imagesLibDir, int[] bytesOut,    String unm) throws Exception
  {
    String mfrList = "";
    
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT * FROM cataloglist ORDER BY Manufacturer");

    String mfr, title, image, desc, type, catalogType, lastMfr = "", cssFormat="line2";
    int itemCount;
    boolean catalogWanted, listingWanted;
    
    while(rs.next())
    {
      mfr         = rs.getString(1);
      title       = rs.getString(2);
      image       = rs.getString(3);
      desc        = rs.getString(4);
      type        = rs.getString(5);
      catalogType = rs.getString(6);

      catalogWanted = listingWanted = false;

      if(catalogType.equals("C"))
        catalogWanted = true;
      else
      if(catalogType.equals("L"))
        listingWanted = true;
      else
      {
        catalogWanted = true;
        listingWanted = true;
      }

      if(! mfr.equals(lastMfr))
      {
        if(lastMfr.length() > 0)
        {
          scoutln(out, bytesOut, "</td></tr><tr id='" + cssFormat + "'><td colspan=2>&nbsp;</td></tr>");
          if(cssFormat.equals("line1")) cssFormat = "line2"; else cssFormat = "line1";
          scoutln(out, bytesOut, "<tr id='" + cssFormat + "'><td colspan=2>&nbsp;</td></tr>");
        }
        else scoutln(out, bytesOut, "<tr id='" + cssFormat + "'><td colspan=2>&nbsp;</td></tr>");

        scoutln(out, bytesOut, "<tr id='" + cssFormat + "'><td valign=top><img src=\"" + imagesLibDir + image + "\" border=0></td>");
        scoutln(out, bytesOut, "<td valign=top><p>" + desc);
        
        lastMfr = mfr;
      }
      
      if(catalogWanted)
      {
        if(type.equals("O")) // local
          itemCount = getSizesForAMfrCatalogLocal(con, stmt2, stmt3, rs2, rs3, mfr);
        else // linked
        {
          itemCount = generalUtils.strToInt(getSizesForAMfrRemote(con, stmt2, rs2, mfr, "C"));
        }

        if(itemCount > 0)
        {
          if(type.equals("O"))
            scoutln(out, bytesOut, "<br><a href=\"javascript:mfr('" + mfr + "')\">" + title + " (Illustrated: " + itemCount + " Items)</a>");
          else scoutln(out, bytesOut, "<br><a href=\"javascript:mfr3('" + mfr + "','C')\">" + title + " (Illustrated: " + itemCount + " Items)</a>");
        }
      }
      
      if(listingWanted)
      {
        if(type.equals("O")) // local
          itemCount = getSizesForAMfrListingLocal(con, stmt2, rs2, mfr, uty);
        else // linked
        {
          itemCount = generalUtils.strToInt(getSizesForAMfrRemote(con, stmt2, rs2, mfr, "L"));
        }

        if(itemCount > 0)
        {
          if(type.equals("O"))
            scoutln(out, bytesOut, "<br><a href=\"javascript:mfr2('" + mfr + "')\">" + title + " (Listing: " + itemCount + " Items)</a>");
          else scoutln(out, bytesOut, "<br><a href=\"javascript:mfr3('" + mfr + "','L')\">" + title + " (Listing: " + itemCount + " Items)</a>");
        }
      }

      mfrList += (mfr + "\001");
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return mfrList;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void listMfrsWithNoEntry(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, String mfrList,
                                   String uty, int[] bytesOut) throws Exception
  {
    int mfrListLen = mfrList.length();
    
    stmt = con.createStatement();

    if(uty.equals("I"))
      rs = stmt.executeQuery("SELECT DISTINCT Manufacturer FROM stock ORDER BY Manufacturer");
    else rs = stmt.executeQuery("SELECT DISTINCT Manufacturer FROM stock WHERE ShowToWeb = 'Y' ORDER BY Manufacturer");

    String mfr, cssFormat="";
    int itemCount;
    
    while(rs.next())
    {
      mfr = rs.getString(1);
      
      if(! mfrIsOnList(mfr, mfrList, mfrListLen))
      {
        itemCount = getSizesForAMfrListingLocal(con, stmt2, rs2, mfr, uty);
             
        if(cssFormat.equals("line1")) cssFormat = "line2"; else cssFormat = "line1";
        scoutln(out, bytesOut, "<tr id='" + cssFormat + "'><td nowrap><p>" + mfr + "</td>");
       
        if(itemCount > 0)
          scoutln(out, bytesOut, "<td width=90% nowrap><p><a href=\"javascript:mfr2('" + mfr + "')\">Listing: " + itemCount + " Items</a></td>");

        scout(out, bytesOut, "</tr>");
      }
    }    

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean mfrIsOnList(String mfr, String mfrList, int mfrListLen) throws Exception
  {
    String thisMfr;
    int x=0;
    while(x < mfrListLen)
    {
      thisMfr = "";
      while(x < mfrListLen && mfrList.charAt(x) != '\001') // just-in-case
        thisMfr += mfrList.charAt(x++);
      if(mfr.equals(thisMfr))
        return true;      
      ++x;          
    }
    
    return false;
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private int getSizesForAMfrCatalogLocal(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String mfr) throws Exception
  {
    int itemCount;
    
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT CategoryCode FROM stockcat WHERE Manufacturer = '" + generalUtils.sanitiseForSQL(mfr) + "'");

    itemCount = 0;
    
    while(rs.next())
      itemCount += getSizesForACategory(con, stmt2, rs2, rs.getString(1));
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return itemCount;
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private int getSizesForAMfrListingLocal(Connection con, Statement stmt, ResultSet rs, String mfr, String uty) throws Exception
  {
    int itemCount = 0;
        
    stmt = con.createStatement();

    if(uty.equals("I"))
      rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM stock WHERE Manufacturer = '" + generalUtils.sanitiseForSQL(mfr) + "'");
    else rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM stock WHERE Manufacturer = '" + generalUtils.sanitiseForSQL(mfr) + "' AND ShowToWeb = 'Y'");

    if(rs.next())
      itemCount = rs.getInt("rowcount");
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return itemCount;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private int getSizesForACategory(Connection con, Statement stmt, ResultSet rs, String categoryCode) throws Exception
  {
    int itemCount = 0;
    
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM stock WHERE CategoryCode = '" + categoryCode + "'");

    if(rs.next())
      itemCount += rs.getInt("rowcount");
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return itemCount;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private String getSizesForAMfrRemote(Connection con, Statement stmt, ResultSet rs, String mfr, String catalogType) throws Exception
  { 
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT CatalogURL, CatalogUpline FROM linkedcat WHERE Manufacturer = '" + generalUtils.sanitiseForSQL(mfr) + "'");
    
    String catalogURL = "", dnmForCatalog = "";

    if(rs.next())
    {
      catalogURL    = rs.getString(1);
      dnmForCatalog = rs.getString(2);
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
                  
    URL url;
    if(catalogURL.equals("catalogs.zaracloud.com"))
      url = new URL("http://" + catalogURL + "/central/servlet/CatalogFetchLinesCountSteelclaws?p1=" + mfr + "&p2=" + dnmForCatalog);
    else url = new URL("http://" + catalogURL + "/central/servlet/CatalogItemsCountZara?p1=" + mfr + "&p2=" + dnmForCatalog + "&p3=" + catalogType);
    
    URLConnection uc = url.openConnection();
    uc.setDoInput(true);
    uc.setUseCaches(false);
    uc.setDefaultUseCaches(false);

    uc.setRequestProperty("Content-Type", "application/octet-stream");

    BufferedReader di = new BufferedReader(new InputStreamReader(uc.getInputStream()));

    String s = di.readLine();
    if(s.startsWith("ERR:"))
      s = "0";  
      
    di.close();
    
    return s;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void scout(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.print(str);
    bytesOut[0] += str.length();    
  }
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }
  
}
