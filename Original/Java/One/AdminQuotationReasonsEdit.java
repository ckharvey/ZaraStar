// =======================================================================================================================================================================================================
// System: ZaraStar AdminEngine: Edit quotation reasons
// Module: AdminQuotationReasonsEdit.java
// Author: C.K.Harvey
// Copyright (c) 1999-2006 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
//
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

public class AdminQuotationReasonsEdit extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  DashboardUtils dashboardUtils = new DashboardUtils();
  DocumentUtils documentUtils = new DocumentUtils();
  SignOnAdministrator  signOnAdministrator  = new SignOnAdministrator();

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
 
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "AdminQuotationReasonsEdit", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7065, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    long startTime = new java.util.Date().getTime();

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if((! serverUtils.isSysAdmin(con, stmt, rs, unm, dnm, sid, uty, localDefnsDir, defnsDir)) && (! serverUtils.isDBAdmin(con, stmt, rs, unm)))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "AdminQuotationReasonsEdit", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7065, bytesOut[0], 0, "ACC:");
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "AdminQuotationReasonsEdit", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7065, bytesOut[0], 0, "SID:");
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }
        
    set(con, stmt, rs, out, unm, sid, uty, men, den, dnm, bnm, imagesDir, defnsDir, bytesOut);
    
    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 7065, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String unm, String sid, String uty,  String men, String den, String dnm, String bnm, String imagesDir, String defnsDir, int[] bytesOut)
                   throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Definitions: Quotation Reasons</title>");
   
    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    scoutln(out, bytesOut, "function update(){document.forms[0].submit();}");

    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    signOnAdministrator.heading(out, true, unm, sid, uty, bnm, dnm, men, den, imagesDir, bytesOut);

    dashboardUtils.drawTitle(out, "Quotation Reasons", "7065", unm, sid, uty, men, den, dnm, bnm, bytesOut);

    scoutln(out, bytesOut, "<form action=\"AdminQuotationReasonsUpdate\" enctype=\"application/x-www-form-urlencoded\" method=POST>");
    scoutln(out, bytesOut, "<input type=hidden name=unm value="+unm+">");
    scoutln(out, bytesOut, "<input type=hidden name=sid value="+sid+">");
    scoutln(out, bytesOut, "<input type=hidden name=uty value="+uty+">");
    scoutln(out, bytesOut, "<input type=hidden name=men value=\""+men+"\">");
    scoutln(out, bytesOut, "<input type=hidden name=den value="+den+">");
    scoutln(out, bytesOut, "<input type=hidden name=dnm value="+dnm+">");
    scoutln(out, bytesOut, "<input type=hidden name=bnm value="+bnm+">");

    scoutln(out, bytesOut, "<table id=\"page\" cellspacing=2 cellpadding=2 width=100%>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    String[] reasons = new String[10];

    documentUtils.getQuotationReasons(con, stmt, rs, reasons);
    
    scoutln(out, bytesOut, "<tr><td nowrap><p>Reason 1</td><td><input type=\"text\" name=\"reason1\" size=\"40\" maxlength=\"40\""
                         + " value=\"" + reasons[0] + "\"></td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap><p>Reason 2</td><td><input type=\"text\" name=\"reason2\" size=\"40\" maxlength=\"40\""
                         + " value=\"" + reasons[1] + "\"></td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap><p>Reason 3</td><td><input type=\"text\" name=\"reason3\" size=\"40\" maxlength=\"40\""
                         + " value=\"" + reasons[2] + "\"></td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap><p>Reason 4</td><td><input type=\"text\" name=\"reason4\" size=\"40\" maxlength=\"40\""
                         + " value=\"" + reasons[3] + "\"></td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap><p>Reason 5</td><td><input type=\"text\" name=\"reason5\" size=\"40\" maxlength=\"40\""
                         + " value=\"" + reasons[4] + "\"></td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap><p>Reason 6</td><td><input type=\"text\" name=\"reason6\" size=\"40\" maxlength=\"40\""
                         + " value=\"" + reasons[5] + "\"></td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap><p>Reason 7</td><td><input type=\"text\" name=\"reason7\" size=\"40\" maxlength=\"40\""
                         + " value=\"" + reasons[6] + "\"></td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap><p>Reason 8</td><td><input type=\"text\" name=\"reason8\" size=\"40\" maxlength=\"40\""
                         + " value=\"" + reasons[7] + "\"></td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap><p>Reason 9</td><td><input type=\"text\" name=\"reason9\" size=\"40\" maxlength=\"40\""
                         + " value=\"" + reasons[8] + "\"></td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap><p>Reason 10</td><td><input type=\"text\" name=\"reason10\" size=\"40\" maxlength=\"40\""
                         + " value=\"" + reasons[9] + "\"></td></tr>");

    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td><p><a href=\"javascript:update()\">Update</a></td></tr>");
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "</table></form></body></html>");
  }


  // -------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += str.length() + 2;    
  }

}
