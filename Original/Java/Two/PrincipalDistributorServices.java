// =======================================================================================================================================================================================================
// System: ZaraStar: Principal: Distributor services page
// Module: PrincipalDistributorServices.java
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

public class PrincipalDistributorServices extends HttpServlet
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
 
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "PrincipalDistributorServices", bytesOut);
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
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "PrincipalDistributorServices", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 200, bytesOut[0], 0, "ACC:");
       
      if(con != null) con.close();
     if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "PrincipalDistributorServices", imagesDir, localDefnsDir, defnsDir, bytesOut);
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
    scoutln(out, bytesOut, "<html><head><title>Principal Distributor Services</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">"); 
  
    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\""
                         + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");
    
    int[] hmenuCount = new int[1];
 
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "201", "", "PrincipalDistributorServices", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount,
                          bytesOut);
    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Principal Services - Distributor", "201",  unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<form><table id=\"page\" width=100% border=0>");

    boolean _220 = authenticationUtils.verifyAccess(con, stmt, rs, req, 220, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean _221 = authenticationUtils.verifyAccess(con, stmt, rs, req, 221, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean _222 = authenticationUtils.verifyAccess(con, stmt, rs, req, 222, unm, uty, dnm, localDefnsDir, defnsDir);

    if(_220 || _221 || _222)
    {
      scoutln(out, bytesOut, "<tr><td nowrap colspan=3><h1>Stock Services</td></tr>");
    
      if(_220)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/_220?unm=" + unm + "&sid=" + sid + "&uty=" + uty
                               + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                               + "\">Stock: Catalog Items</a></td><td width=70%><span id=\"service\">"
                               + "&nbsp;&nbsp;&nbsp;<a href=\"javascript:help('220')\">(Service 220)</a></span></td></tr>");
      }

      if(_221)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/_221?unm=" + unm + "&sid=" + sid + "&uty=" + uty
                               + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                               + "\">Stock: New Products</a></td><td width=70%><span id=\"service\">"
                               + "&nbsp;&nbsp;&nbsp;<a href=\"javascript:help('221')\">(Service 221)</a></span></td></tr>");
      }

      if(_222)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/_222?unm=" + unm + "&sid=" + sid + "&uty=" + uty
                               + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                               + "\">Stock: Off-Catalog Items</a></td><td width=70%><span id=\"service\">"
                               + "&nbsp;&nbsp;&nbsp;<a href=\"javascript:help('222')\">(Service 222)</a></span></td></tr>");
      }
    }
    
    boolean _223 = authenticationUtils.verifyAccess(con, stmt, rs, req, 223, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean _224 = authenticationUtils.verifyAccess(con, stmt, rs, req, 224, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean _225 = authenticationUtils.verifyAccess(con, stmt, rs, req, 225, unm, uty, dnm, localDefnsDir, defnsDir);
    
    if(_223 || _224 || _225)
    {
      scoutln(out, bytesOut, "<tr><td nowrap colspan=3><h1>Quotation Services</td></tr>");
    
      if(_223)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/_223?unm=" + unm + "&sid=" + sid + "&uty=" + uty
                               + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                               + "\">Quotations: Catalog Items</a></td><td width=70%><span id=\"service\">"
                               + "&nbsp;&nbsp;&nbsp;<a href=\"javascript:help('223')\">(Service 223)</a></span></td></tr>");
      }

      if(_224)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/_224?unm=" + unm + "&sid=" + sid + "&uty=" + uty
                               + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                               + "\">Quotations: New Products</a></td><td width=70%><span id=\"service\">"
                               + "&nbsp;&nbsp;&nbsp;<a href=\"javascript:help('224')\">(Service 224)</a></span></td></tr>");
      }

      if(_225)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/_225?unm=" + unm + "&sid=" + sid + "&uty=" + uty
                               + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                               + "\">Quotations: Off-Catalog Items</a></td><td width=70%><span id=\"service\">"
                               + "&nbsp;&nbsp;&nbsp;<a href=\"javascript:help('225')\">(Service 225)</a></span></td></tr>");
      }
    }

    boolean _226 = authenticationUtils.verifyAccess(con, stmt, rs, req, 226, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean _227 = authenticationUtils.verifyAccess(con, stmt, rs, req, 227, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean _228 = authenticationUtils.verifyAccess(con, stmt, rs, req, 228, unm, uty, dnm, localDefnsDir, defnsDir);

    if(_226 || _227 || _228)
    {
      scoutln(out, bytesOut, "<tr><td nowrap colspan=3><h1>Sales Order Services</td></tr>");

      if(_226)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/_226?unm=" + unm + "&sid=" + sid + "&uty=" + uty
                               + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                               + "\">Sales Orders: Catalog Items</a></td><td width=70%><span id=\"service\">"
                               + "&nbsp;&nbsp;&nbsp;<a href=\"javascript:help('226')\">(Service 226)</a></span></td></tr>");
      }

      if(_227)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/_227?unm=" + unm + "&sid=" + sid + "&uty=" + uty
                               + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                               + "\">Sales Orders: New Products</a></td><td width=70%><span id=\"service\">"
                               + "&nbsp;&nbsp;&nbsp;<a href=\"javascript:help('227')\">(Service 227)</a></span></td></tr>");
      }

      if(_228)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/_228?unm=" + unm + "&sid=" + sid + "&uty=" + uty
                               + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                               + "\">Sales Orders: Off-Catalog Items</a></td><td width=70%><span id=\"service\">"
                               + "&nbsp;&nbsp;&nbsp;<a href=\"javascript:help('228')\">(Service 228)</a></span></td></tr>");
      }
    }

    boolean _229 = authenticationUtils.verifyAccess(con, stmt, rs, req, 229, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean _230 = authenticationUtils.verifyAccess(con, stmt, rs, req, 230, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean _231 = authenticationUtils.verifyAccess(con, stmt, rs, req, 231, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean _232 = authenticationUtils.verifyAccess(con, stmt, rs, req, 232, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean _233 = authenticationUtils.verifyAccess(con, stmt, rs, req, 233, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean _234 = authenticationUtils.verifyAccess(con, stmt, rs, req, 234, unm, uty, dnm, localDefnsDir, defnsDir);

    if(_229 || _230 || _231 || _232 || _233 || _234)
    {
      scoutln(out, bytesOut, "<tr><td nowrap colspan=3><h1>Purchase Order Services</td></tr>");

      if(_229)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/_229?unm=" + unm + "&sid=" + sid + "&uty=" + uty
                               + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                               + "\">Purchase Orders: Catalog Items - Delivered</a></td><td width=70%><span id=\"service\">"
                               + "&nbsp;&nbsp;&nbsp;<a href=\"javascript:help('229')\">(Service 229)</a></span></td></tr>");
      }

      if(_230)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/_230?unm=" + unm + "&sid=" + sid + "&uty=" + uty
                               + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                               + "\">Purchase Orders: Catalog Items - Outstanding</a></td><td width=70%><span id=\"service\">"
                               + "&nbsp;&nbsp;&nbsp;<a href=\"javascript:help('230')\">(Service 230)</a></span></td></tr>");
      }

      if(_231)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/_231?unm=" + unm + "&sid=" + sid + "&uty=" + uty
                               + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                               + "\">Purchase Orders: New Products - Delivered</a></td><td width=70%><span id=\"service\">"
                               + "&nbsp;&nbsp;&nbsp;<a href=\"javascript:help('231')\">(Service 231)</a></span></td></tr>");
      }

      if(_232)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/_232?unm=" + unm + "&sid=" + sid + "&uty=" + uty
                               + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                               + "\">Purchase Orders: New Products - Outstanding</a></td><td width=70%><span id=\"service\">"
                               + "&nbsp;&nbsp;&nbsp;<a href=\"javascript:help('232')\">(Service 232)</a></span></td></tr>");
      }

      if(_233)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/_233?unm=" + unm + "&sid=" + sid + "&uty=" + uty
                               + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                               + "\">Purchase Orders: Off-Catalog Items - Delivered</a></td><td width=70%><span id=\"service\">"
                               + "&nbsp;&nbsp;&nbsp;<a href=\"javascript:help('233')\">(Service 233)</a></span></td></tr>");
      }

      if(_234)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/_234?unm=" + unm + "&sid=" + sid + "&uty=" + uty
                               + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                               + "\">Purchase Orders: Off-Catalog Items - Outstanding</a></td><td width=70%><span id=\"service\">"
                               + "&nbsp;&nbsp;&nbsp;<a href=\"javascript:help('234')\">(Service 234)</a></span></td></tr>");
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
