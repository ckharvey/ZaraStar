// =======================================================================================================================================================================================================
// System: ZaraStar UtilsEngine: Ext User change own details
// Module: AdminExternalUserDetailsChange.java
// Author: C.K.Harvey
// Copyright (c) 2001-07 Christopher Harvey. All Rights Reserved.
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

public class AdminExternalUserDetailsChange extends HttpServlet
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
 
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "AdminExternalUserDetailsChange", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7066, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men,
                      String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    long startTime = new java.util.Date().getTime();

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
     
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 7066, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "AdminExternalUserDetailsChange", imagesDir, localDefnsDir,
                         defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7066, bytesOut[0], 0, "ACC:");
      
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "AdminExternalUserDetailsChange", imagesDir, localDefnsDir,
                         defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7066, bytesOut[0], 0, "SID:");
      
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    set(con, stmt, rs, out, req, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 7066, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
       
      if(con != null) con.close();
   if(out != null) out.flush();
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den,
                     String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Contact Enquiry</title>");

    scoutln(out,bytesOut, "<script language=\"JavaScript\">");

    scoutln(out,bytesOut, "function sanitise(code){");
    scoutln(out,bytesOut, "var code2='';var x;var len=code.length;");
    scoutln(out,bytesOut, "for(x=0;x<len;++x)");
    scoutln(out,bytesOut, "if(code.charAt(x)=='#')code2+='%23';");
    scoutln(out,bytesOut, "else if(code.charAt(x)=='\"')code2+='%22';");
    scoutln(out,bytesOut, "else if(code.charAt(x)=='&')code2+='%26';");
    scoutln(out,bytesOut, "else if(code.charAt(x)=='%')code2+='%25';");
    scoutln(out,bytesOut, "else if(code.charAt(x)==' ')code2+='%20';");
    scoutln(out,bytesOut, "else if(code.charAt(x)=='?')code2+='%3F';");
    scoutln(out,bytesOut, "else if(escape(code.charAt(x))=='%0A')code2+='\003';");
    scoutln(out,bytesOut, "else if(escape(code.charAt(x))=='%0D');");
    scoutln(out,bytesOut, "else code2+=code.charAt(x);");
    scoutln(out,bytesOut, "return code2};");

    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\""
                           + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\"></head>");
  
    int[] hmenuCount = new int[1];
 
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "7066", "", "AdminExternalUserDetailsChange", unm, sid, uty, men, den, dnm, bnm, localDefnsDir,
                          defnsDir, hmenuCount, bytesOut);

    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Change Details", "7066", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<form><table id=\"page\" width=100% border=0>");

    scoutln(out,bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out,bytesOut, "<tr><td colspan=4><p>Please amend your personal details as appropriate.");
    scoutln(out,bytesOut, "</td></tr><tr><td><br></td></tr></table>");

    scoutln(out,bytesOut, "<table  id=\"page\" border=0 cellspacing=1 cellpadding=1>");

    byte[] data = new byte[5000];
    
    byte[] extUserCode = new byte[21];
    int i = unm.indexOf("_");
    generalUtils.strToBytes(extUserCode, unm.substring(0, i));
    
    String personName    = generalUtils.dfsAsStr(data, (short)2);
    String title         = generalUtils.dfsAsStr(data, (short)4);
    String designation   = generalUtils.dfsAsStr(data, (short)6);
    String department    = generalUtils.dfsAsStr(data, (short)7);
    String officePhone   = generalUtils.dfsAsStr(data, (short)8);
    String fax           = generalUtils.dfsAsStr(data, (short)9);
    String mobilePhone   = generalUtils.dfsAsStr(data, (short)10);
    String pager         = generalUtils.dfsAsStr(data, (short)11);
    String eMail         = generalUtils.dfsAsStr(data, (short)13);
    String onMailingList = generalUtils.dfsAsStr(data, (short)16);

    scoutln(out,bytesOut, "<tr><td nowrap><p>Your Name: </td><td><p><input type=text name=personName maxlength=40 size=25 value=\""
                        + personName + "\"></td></tr>");

    scoutln(out,bytesOut, "<tr><td nowrap><p>Title: &nbsp;</td><td><p><input type=text name=title maxlength=20 size=6 "
                        + "value=\"" + title + "\"></td></tr>");

    scoutln(out,bytesOut, "<tr><td nowrap><p>Designation: &nbsp;</td><td><p><input type=text name=designation maxlength=40 size=25 "
                        + "value=\"" + designation + "\"></td></tr>");

    scoutln(out,bytesOut, "<tr><td nowrap><p>Department: </td><td><p><input type=text name=department maxlength=40 size=25 value=\""
                        + department + "\"></td></tr>");

    scoutln(out,bytesOut, "<tr><td nowrap><p>eMail: </td><td colspan=3><p><input type=text name=email maxlength=40 size=40 value=\""
                        + eMail + "\"></td></tr>");

    scoutln(out,bytesOut, "<tr><td nowrap><p>Office Phone: </td><td><p><input type=text name=officePhone maxlength=40 size=25 value=\""
                        + officePhone + "\"></td></tr>");

    scoutln(out,bytesOut, "<tr><td nowrap><p>Mobile Phone: </td><td><p><input type=text name=mobilePhone maxlength=40 size=25 value=\""
                        + mobilePhone + "\"></td></tr>");

    scoutln(out,bytesOut, "<tr><td nowrap><p>Pager: </td><td><p><input type=text name=pager maxlength=40 size=25 value=\"" + pager
                        + "\"></td></tr>");

    scoutln(out,bytesOut, "<tr><td nowrap><p>Fax: </td><td><p><input type=text name=fax maxlength=40 size=25 value=\"" + fax
                        + "\"></td></tr>");

    scoutln(out,bytesOut, "<tr><td nowrap><p>Account PassWord: </td><td><p><input type=text name=passWord1 maxlength=20 size=20></td></tr>");

    scoutln(out,bytesOut, "<tr><td nowrap><p>Re-Enter PassWord: </td><td><p><input type=text name=passWord2 maxlength=20 size=20</td></tr>");

    scoutln(out,bytesOut, "<tr><td nowrap><p>On Mailing List: </td><td><p><input type=checkbox name=onMailingList");
    if(onMailingList.equals("Y"))
      scoutln(out,bytesOut, " CHECKED");
     scoutln(out,bytesOut, "></td></tr>");

    scoutln(out,bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out,bytesOut, "<tr><td></td><td nowrap><p><a href=\"javascript:submit()\">Update</a> our DataBase</td></tr>");

    scoutln(out,bytesOut, "</table></form></div></body></html>");
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}

