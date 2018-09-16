// =======================================================================================================================================================================================================
// System: ZaraStar: Product: Stock updating services access page
// Module: StockUpdatingServices.java
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
import java.io.*;
import java.sql.*;

public class StockUpdatingServices extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
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
 
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "StockUpdatingServices", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 125, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush(); 
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;
    
    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 125, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "StockUpdatingServices", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 125, bytesOut[0], 0, "ACC:");
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "StockUpdatingServices", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 125, bytesOut[0], 0, "SID:");
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    set(con, stmt, rs, out, req, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 125, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut)
                   throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Stock Updating Services Access</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");
    
    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");
    
    int[] hmenuCount = new int[1];
    
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "125", "", "StockUpdatingServices", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
    
    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Stock Updating Services", "125", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<form><table id=\"page\" width=100% border=0>");

    boolean ProductStockReplace = authenticationUtils.verifyAccess(con, stmt, rs, req, 3009, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean PriceListUpdate = authenticationUtils.verifyAccess(con, stmt, rs, req, 3053, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean StockFileDownload = authenticationUtils.verifyAccess(con, stmt, rs, req, 3057, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean StockFileUploadUpdate = authenticationUtils.verifyAccess(con, stmt, rs, req, 3058, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean StockRecordsUpload = authenticationUtils.verifyAccess(con, stmt, rs, req, 3015, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean StockRecordRemove = authenticationUtils.verifyAccess(con, stmt, rs, req, 3016, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean StockRecordsUploadRemove = authenticationUtils.verifyAccess(con, stmt, rs, req, 3017, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean ProductStockRecordsUploadAdd = authenticationUtils.verifyAccess(con, stmt, rs, req, 3022, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean ProductStockRenumber = authenticationUtils.verifyAccess(con, stmt, rs, req, 3023, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean StockPricesOutOfDate = authenticationUtils.verifyAccess(con, stmt, rs, req, 3061, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean StoreStoreMove = authenticationUtils.verifyAccess(con, stmt, rs, req, 3071, unm, uty, dnm, localDefnsDir, defnsDir);

    if(ProductStockReplace || PriceListUpdate || StockFileDownload || StockFileUploadUpdate || StockRecordsUpload || StockRecordRemove || StockRecordsUploadRemove || ProductStockRecordsUploadAdd || ProductStockRenumber || StockPricesOutOfDate || StoreStoreMove)
    {
      scoutln(out, bytesOut, "<tr><td nowrap colspan=3><h1>Stock Record Services</td></tr>");
    
      if(PriceListUpdate)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/PriceListUpdate?unm="
                               + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm
                               + "&bnm=" + bnm + "\">Update Stock Prices</a></td><td width=70%><span id=\"service\">"
                               + "&nbsp;&nbsp;&nbsp;<a href=\"javascript:help('3053')\">(Service 3053)</a></span></td></tr>");
      }

      if(StockPricesOutOfDate)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/StockPricesOutOfDate?unm=" + unm + "&sid="
                               + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                               + "\">List Stock Prices Out-of-Date</a></td><td width=70%><span id=\"service\">"
                               + "&nbsp;&nbsp;&nbsp;<a href=\"javascript:help('3061')\">(Service 3061)</a></span></td></tr>");
      }

      if(StockFileDownload)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/StockFileDownload?unm="
                               + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm
                               + "&bnm=" + bnm + "\">Stock File Download</a></td><td width=70%>"
                               + "<span id=\"service\">&nbsp;&nbsp;&nbsp;"
                               + "<a href=\"javascript:help('3057')\">(Service 3057)</a></span></td></tr>");
      }

      if(StockFileUploadUpdate)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/StockFileUploadUpdate?unm="
                               + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm
                               + "&bnm=" + bnm + "\">Stock File Upload Update</a></td><td width=70%>"
                               + "<span id=\"service\">&nbsp;&nbsp;&nbsp;"
                               + "<a href=\"javascript:help('3058')\">(Service 3058)</a></span></td></tr>");
      }

      if(StockFileUploadUpdate)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/ProductStockUploadLocation?unm="
                               + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm
                               + "&bnm=" + bnm + "\">Store Location Upload Update</a></td><td width=70%>"
                               + "<span id=\"service\">&nbsp;&nbsp;&nbsp;"
                               + "<a href=\"javascript:help('3088')\">(Service 3088)</a></span></td></tr>");
      }

      if(ProductStockRenumber)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/ProductStockRenumber?unm=" + unm + "&sid="
                               + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                               + "\">Renumber Stock Record</a></td><td width=70%><span id=\"service\">"
                               + "&nbsp;&nbsp;&nbsp;<a href=\"javascript:help('3023')\">(Service 3023)</a></span></td></tr>");
      }

      if(ProductStockReplace)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/ProductStockReplace?unm="
                               + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm
                               + "&bnm=" + bnm + "\">Replace Stock Record</a></td><td width=70%><span id=\"service\">"
                               + "&nbsp;&nbsp;&nbsp;<a href=\"javascript:help('3009')\">(Service 3009)</a></span></td></tr>");
      }

      if(StockRecordsUpload)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/StockRecordsUpload?unm="
                               + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm
                               + "&bnm=" + bnm + "\">Replace Stock Records by Upload</a></td><td width=70%><span id=\"service\">"
                               + "&nbsp;&nbsp;&nbsp;<a href=\"javascript:help('3015')\">(Service 3015)</a></span></td></tr>");
      }

      if(ProductStockRecordsUploadAdd)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/ProductStockRecordsUploadAdd?unm="
                               + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm
                               + "&bnm=" + bnm + "\">Add Stock Records by Upload</a></td><td width=70%>"
                               + "<span id=\"service\">&nbsp;&nbsp;&nbsp;"
                               + "<a href=\"javascript:help('3022')\">(Service 3022)</a></span></td></tr>");
      }

      if(StockRecordRemove)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/StockRecordRemove?unm="
                               + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm
                               + "&bnm=" + bnm + "\">Remove Stock Record</a></td><td width=70%><span id=\"service\">"
                               + "&nbsp;&nbsp;&nbsp;<a href=\"javascript:help('3016')\">(Service 3016)</a></span></td></tr>");
      }

      if(StockRecordsUploadRemove)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/StockRecordsUploadRemove?unm="
                               + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm
                               + "&bnm=" + bnm + "\">Remove Stock Records by Upload</a></td><td width=70%><span id=\"service\">"
                               + "&nbsp;&nbsp;&nbsp;<a href=\"javascript:help('3017')\">(Service 3017)</a></span></td></tr>");
      }

      if(StoreStoreMove)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/StoreStoreMove?unm=" + unm + "&sid=" + sid + "&uty=" + uty
                             + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Move From Store to Store</a></td><td width=70%><span id=\"service\">"
                             + "&nbsp;&nbsp;&nbsp;<a href=\"javascript:help('3071')\">(Service 3071)</a></span></td></tr>");
      }

    }
    
    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }
 
}
