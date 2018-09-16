// =======================================================================================================================================================================================================
// System: ZaraStar Analytic: Document Trace
// Module: DocumentTrace.java
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

public class DocumentTrace extends HttpServlet
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
 
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "DocumentTrace", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1025, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
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
    
    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 1025, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "DocumentTrace", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1025, bytesOut[0], 0, "ACC:");
      
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "DocumentTrace", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1025, bytesOut[0], 0, "SID:");
      
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    set(con, stmt, rs, out, req, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 1025, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
      
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut)
                   throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Document Trace</title>");

    scoutln(out, bytesOut, "<script javascript=\"JavaScript\">");

    scoutln(out, bytesOut, "function search(){document.go.submit();}");

    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\"></head>");

    scoutln(out, bytesOut, "<form action=\"DocumentTraceExecute\" name=go enctype=\"application/x-www-form-urlencoded\" method=POST>");
    scoutln(out, bytesOut, "<input type=hidden name=unm value=" + unm + ">");
    scoutln(out, bytesOut, "<input type=hidden name=sid value=" + sid + ">");
    scoutln(out, bytesOut, "<input type=hidden name=uty value=" + uty + ">");
    scoutln(out, bytesOut, "<input type=hidden name=men value=" + men + ">");
    scoutln(out, bytesOut, "<input type=hidden name=den value=" + den + ">");
    scoutln(out, bytesOut, "<input type=hidden name=dnm value=" + dnm + ">");
    scoutln(out, bytesOut, "<input type=hidden name=bnm value=" + bnm + ">");
  
    int[] hmenuCount = new int[1];
    
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "1025", "", "DocumentTrace", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
         
    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Document Trace", "1025", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<table id=\"page\" width=100% border=0>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=1><p>Enter any ONE code:</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap><p>Customer Enquiry Code</td><td><p><input type=text maxlength=40 size=40 name=ce></td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap><p>Customer PO Code</td><td><p><input type=text maxlength=40 size=40 name=cpo></td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap><p>Quotation Code</td><td><p><input type=text size=20 name=q></td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap><p>Sales Order Code</td><td><p><input type=text size=20 name=so></td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap><p>Order Acknowledgement Code</td><td><p><input type=text size=20 name=oa></td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap><p>Order Confirmation Code</td><td><p><input type=text size=20 name=oc></td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap><p>Picking List Code</td><td><p><input type=text size=20 name=pl></td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap><p>Delivery Order Code</td><td><p><input type=text size=20 name=do></td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap><p>Invoice Code</td><td><p><input type=text size=20 name=i></td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap><p>Proforma Invoice Code</td><td><p><input type=text size=20 name=pro></td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap><p>Debit Note Code</td><td><p><input type=text size=20 name=dn></td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap><p>Credit Note Code</td><td><p><input type=text size=20 name=cn></td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap><p>Purchase Order Code</td><td><p><input type=text size=20 name=po></td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap><p>Local Purchase Code</td><td><p><input type=text size=20 name=lp></td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap><p>Goods Received Note Code&nbsp;&nbsp;</td><td><p><input type=text size=20 name=grn></td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    
    scoutln(out, bytesOut, "<tr><td><a href=\"javascript:search()\">Search</a></td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "</table></form>");     scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
