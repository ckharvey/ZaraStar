// =======================================================================================================================================================================================================
// System: ZaraStar: Principal: Dealer services page
// Module: PrincipalDealerServices.java
// Author: C.K.Harvey
// Copyright (c) 2002-08 Christopher Harvey. All Rights Reserved.
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

public class PrincipalDealerServices extends HttpServlet
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
 
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "PrincipalDealerServices", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 200, bytesOut[0], 0, "ERR:");
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

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
     
   Statement stmt = null;
    ResultSet rs   = null;
    
    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 200, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "PrincipalDealerServices", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 200, bytesOut[0], 0, "ACC:");
      
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "PrincipalDealerServices", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 200, bytesOut[0], 0, "SID:");
      
      if(con != null) con.close();
     if(out != null) out.flush(); 
      return;
    }

    set(con, stmt, rs, out, req, unm, sid, uty, men, den, dnm, bnm, imagesDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 200, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
      
      if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty,
                   String men, String den, String dnm, String bnm, String imagesDir, String localDefnsDir, String defnsDir, int[] bytesOut)
                   throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Principal Dealer Services</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">"); 
  
    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\""
                         + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");
    
    int[] hmenuCount = new int[1];
 
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "202", "", "PrincipalDealerServices", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount,
                          bytesOut);
    
    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Principal Services - Dealer", "202",  unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<form><table id=\"page\" width=100% border=0>");
      
    boolean _250 = authenticationUtils.verifyAccess(con, stmt, rs, req, 250, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean _251 = authenticationUtils.verifyAccess(con, stmt, rs, req, 251, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean _252 = authenticationUtils.verifyAccess(con, stmt, rs, req, 252, unm, uty, dnm, localDefnsDir, defnsDir);

    if(_250 || _251 || _252)
    {
      scoutln(out, bytesOut, "<tr><td nowrap colspan=3><h1>Stock Services</td></tr>");
    
      if(_250)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/_250?unm=" + unm + "&sid=" + sid + "&uty=" + uty
                               + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                               + "\">Stock: Catalog Items</a></td><td width=70%><span id=\"service\">"
                               + "&nbsp;&nbsp;&nbsp;<a href=\"javascript:help('250')\">(Service 250)</a></span></td></tr>");
      }

      if(_251)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/_251?unm=" + unm + "&sid=" + sid + "&uty=" + uty
                               + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                               + "\">Stock: New Products</a></td><td width=70%><span id=\"service\">"
                               + "&nbsp;&nbsp;&nbsp;<a href=\"javascript:help('251')\">(Service 251)</a></span></td></tr>");
      }

      if(_252)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/_252?unm=" + unm + "&sid=" + sid + "&uty=" + uty
                               + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                               + "\">Stock: Off-Catalog Items</a></td><td width=70%><span id=\"service\">"
                               + "&nbsp;&nbsp;&nbsp;<a href=\"javascript:help('252')\">(Service 252)</a></span></td></tr>");
      }
    }
    
    boolean _253 = authenticationUtils.verifyAccess(con, stmt, rs, req, 253, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean _254 = authenticationUtils.verifyAccess(con, stmt, rs, req, 254, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean _255 = authenticationUtils.verifyAccess(con, stmt, rs, req, 255, unm, uty, dnm, localDefnsDir, defnsDir);
    
    if(_253 || _254 || _255)
    {
      scoutln(out, bytesOut, "<tr><td nowrap colspan=3><h1>Quotation Services</td></tr>");
    
      if(_253)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/_253?unm=" + unm + "&sid=" + sid + "&uty=" + uty
                               + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                               + "\">Quotations: Catalog Items</a></td><td width=70%><span id=\"service\">"
                               + "&nbsp;&nbsp;&nbsp;<a href=\"javascript:help('253')\">(Service 253)</a></span></td></tr>");
      }

      if(_254)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/_254?unm=" + unm + "&sid=" + sid + "&uty=" + uty
                               + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                               + "\">Quotations: New Products</a></td><td width=70%><span id=\"service\">"
                               + "&nbsp;&nbsp;&nbsp;<a href=\"javascript:help('254')\">(Service 254)</a></span></td></tr>");
      }

      if(_255)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/_255?unm=" + unm + "&sid=" + sid + "&uty=" + uty
                               + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                               + "\">Quotations: Off-Catalog Items</a></td><td width=70%><span id=\"service\">"
                               + "&nbsp;&nbsp;&nbsp;<a href=\"javascript:help('255')\">(Service 255)</a></span></td></tr>");
      }
    }
    
    boolean _256 = authenticationUtils.verifyAccess(con, stmt, rs, req, 256, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean _257 = authenticationUtils.verifyAccess(con, stmt, rs, req, 257, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean _258 = authenticationUtils.verifyAccess(con, stmt, rs, req, 258, unm, uty, dnm, localDefnsDir, defnsDir);
    
    if(_256 || _257 || _258)
    {
      scoutln(out, bytesOut, "<tr><td nowrap colspan=3><h1>Sales Order Services</td></tr>");
    
      if(_256)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/_256?unm=" + unm + "&sid=" + sid + "&uty=" + uty
                               + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                               + "\">Sales Orders: Catalog Items</a></td><td width=70%><span id=\"service\">"
                               + "&nbsp;&nbsp;&nbsp;<a href=\"javascript:help('256')\">(Service 256)</a></span></td></tr>");
      }

      if(_257)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/_257?unm=" + unm + "&sid=" + sid + "&uty=" + uty
                               + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                               + "\">Sales Orders: New Products</a></td><td width=70%><span id=\"service\">"
                               + "&nbsp;&nbsp;&nbsp;<a href=\"javascript:help('257')\">(Service 257)</a></span></td></tr>");
      }

      if(_258)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/_258?unm=" + unm + "&sid=" + sid + "&uty=" + uty
                               + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                               + "\">Sales Orders: Off-Catalog Items</a></td><td width=70%><span id=\"service\">"
                               + "&nbsp;&nbsp;&nbsp;<a href=\"javascript:help('258')\">(Service 258)</a></span></td></tr>");
      }
    }
    
    boolean _259 = authenticationUtils.verifyAccess(con, stmt, rs, req, 259, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean _260 = authenticationUtils.verifyAccess(con, stmt, rs, req, 260, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean _261 = authenticationUtils.verifyAccess(con, stmt, rs, req, 261, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean _262 = authenticationUtils.verifyAccess(con, stmt, rs, req, 262, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean _263 = authenticationUtils.verifyAccess(con, stmt, rs, req, 263, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean _264 = authenticationUtils.verifyAccess(con, stmt, rs, req, 264, unm, uty, dnm, localDefnsDir, defnsDir);

    if(_259 || _260 || _261 || _262 || _263 || _264)
    {
      scoutln(out, bytesOut, "<tr><td nowrap colspan=3><h1>Purchase Order Services</td></tr>");
    
      if(_259)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/_259?unm=" + unm + "&sid=" + sid + "&uty=" + uty
                               + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                               + "\">Purchase Orders: Catalog Items - Delivered</a></td><td width=70%><span id=\"service\">"
                               + "&nbsp;&nbsp;&nbsp;<a href=\"javascript:help('259')\">(Service 259)</a></span></td></tr>");
      }

      if(_260)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/_260?unm=" + unm + "&sid=" + sid + "&uty=" + uty
                               + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                               + "\">Purchase Orders: Catalog Items - Outstanding</a></td><td width=70%><span id=\"service\">"
                               + "&nbsp;&nbsp;&nbsp;<a href=\"javascript:help('260')\">(Service 260)</a></span></td></tr>");
      }

      if(_261)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/_261?unm=" + unm + "&sid=" + sid + "&uty=" + uty
                               + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                               + "\">Purchase Orders: New Products - Delivered</a></td><td width=70%><span id=\"service\">"
                               + "&nbsp;&nbsp;&nbsp;<a href=\"javascript:help('261')\">(Service 261)</a></span></td></tr>");
      }

      if(_262)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/_262?unm=" + unm + "&sid=" + sid + "&uty=" + uty
                               + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                               + "\">Purchase Orders: New Products - Outstanding</a></td><td width=70%><span id=\"service\">"
                               + "&nbsp;&nbsp;&nbsp;<a href=\"javascript:help('262')\">(Service 262)</a></span></td></tr>");
      }

      if(_263)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/_263?unm=" + unm + "&sid=" + sid + "&uty=" + uty
                               + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                               + "\">Purchase Orders: Off-Catalog Items - Delivered</a></td><td width=70%><span id=\"service\">"
                               + "&nbsp;&nbsp;&nbsp;<a href=\"javascript:help('263')\">(Service 263)</a></span></td></tr>");
      }

      if(_264)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/_264?unm=" + unm + "&sid=" + sid + "&uty=" + uty
                               + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                               + "\">Purchase Orders: Off-Catalog Items - Outstanding</a></td><td width=70%><span id=\"service\">"
                               + "&nbsp;&nbsp;&nbsp;<a href=\"javascript:help('264')\">(Service 264)</a></span></td></tr>");
      }
    }

    scoutln(out, bytesOut, "</table></form>");     scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }
 
}
