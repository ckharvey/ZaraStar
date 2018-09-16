// =======================================================================================================================================================================================================
// System: ZaraStar AdminEngine: Edit appconfig
// Module: AdminAppconfigEdit.java
// Author: C.K.Harvey
// Copyright (c) 1999-2008 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

public class AdminAppconfigEdit extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  DashboardUtils dashboardUtils = new DashboardUtils();
  DefinitionTables definitionTables = new DefinitionTables();
  SignOnAdministrator  signOnAdministrator = new SignOnAdministrator();
  
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
 
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "AdminAppconfigEdit", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7062, bytesOut[0], 0, "ERR:");
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

    if((! serverUtils.isSysAdmin(con, stmt, rs, unm, dnm, sid, uty, localDefnsDir, defnsDir)) && (! serverUtils.isDBAdmin(con, stmt, rs, unm)))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "AdminAppconfigEdit", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7062, bytesOut[0], 0, "ACC:");
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "AdminAppconfigEdit", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7062, bytesOut[0], 0, "SID:");
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }
        
    set(con, stmt, rs, out, unm, sid, uty, men, den, dnm, bnm, imagesDir, localDefnsDir, defnsDir, bytesOut);
    
    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 7062, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String unm, String sid, String uty,
                   String men, String den, String dnm, String bnm, String imagesDir, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Definitions: Application Configuration</title>");
   
    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    scoutln(out, bytesOut, "function update(){document.forms[0].submit();}");

    String cssDirectory = directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir);
    
    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + cssDirectory + "general.css\">");

    signOnAdministrator.heading(out, true, unm, sid, uty, bnm, dnm, men, den, imagesDir, bytesOut);

    dashboardUtils.drawTitle(out, "Application Configuration", "7062", unm, sid, uty, men, den, dnm, bnm, bytesOut);

    scoutln(out, bytesOut, "<form action=\"AdminAppconfigProcess\" enctype=\"application/x-www-form-urlencoded\" method=POST>");
    scoutln(out, bytesOut, "<input type=hidden name=unm value="+unm+">");
    scoutln(out, bytesOut, "<input type=hidden name=sid value="+sid+">");
    scoutln(out, bytesOut, "<input type=hidden name=uty value="+uty+">");
    scoutln(out, bytesOut, "<input type=hidden name=men value=\""+men+"\">");
    scoutln(out, bytesOut, "<input type=hidden name=den value="+den+">");
    scoutln(out, bytesOut, "<input type=hidden name=dnm value="+dnm+">");
    scoutln(out, bytesOut, "<input type=hidden name=bnm value="+bnm+">");

    scoutln(out, bytesOut, "<table id=\"page\" cellspacing=2 cellpadding=2 width=100%>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

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

    definitionTables.getAppConfig(con, stmt, rs, dnm, companyName, applicationStartDate, financialYearStartMonth, financialYearEndMonth, effectiveStartDate, companyPhone, companyFax, dateStyle, dateSeparator, description, companyEMail, terseName, latitude,
                 longitude, googleMapsKey, currentStyle);

    dateStyle[0] = generalUtils.getFromDefnFile("FORMAT", "dates.dfn", localDefnsDir, defnsDir);

    dateSeparator[0] = generalUtils.getFromDefnFile("SEPARATOR", "dates.dfn", localDefnsDir, defnsDir);

    scoutln(out, bytesOut, "<tr><td nowrap><p>Company Name</td><td><input type=\"text\" name=\"companyName\" size=\"80\" maxlength=\"80\" value=\"" + companyName[0] + "\"></td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap><p>Terse Name</td><td><input type=\"text\" name=\"terseName\" size=\"20\" maxlength=\"20\" value=\"" + terseName[0] + "\"></td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap><p>Company Phone</td><td><input type=\"text\" name=\"companyPhone\" size=\"40\" maxlength=\"80\" value=\"" + companyPhone[0] + "\"></td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap><p>Company Fax</td><td><input type=\"text\" name=\"companyFax\" size=\"40\" maxlength=\"80\" value=\"" + companyFax[0] + "\"></td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap><p>Company eMail</td><td><input type=\"text\" name=\"companyEMail\" size=\"40\" maxlength=\"80\" value=\"" + companyEMail[0] + "\"></td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap><p>Site Description</td><td><input type=\"text\" name=\"description\" size=\"80\" maxlength=\"250\" value=\"" + description[0] + "\"></td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap><p>Initial Start Date</td><td><input type=\"text\" name=\"applicationStartDate\" size=\"10\" maxlength=\"10\" value=\"" + applicationStartDate[0] + "\"></td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap><p>Financial Year Start</td><td>");
    getMonths(out, "financialYearStartMonth", financialYearStartMonth[0], bytesOut);
    scoutln(out, bytesOut, "</td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap><p>Financial Year End</td><td>");
    getMonths(out, "financialYearEndMonth", financialYearEndMonth[0], bytesOut);
    scoutln(out, bytesOut, "</td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap><p>Effective Start Date</td><td><input type=\"text\" name=\"effectiveStartDate\" size=\"10\" maxlength=\"10\" value=\"" + effectiveStartDate[0] + "\"></td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap><p>Date Style</td><td>");
    getDateStyle(out, "dateStyle", dateStyle[0], bytesOut);
    scoutln(out, bytesOut, "</td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap><p>Date Separator</td><td>");
    getDateSeparator(out, "dateSeparator", dateSeparator[0], bytesOut);
    scoutln(out, bytesOut, "</td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap><p>Latitude</td><td><input type=\"text\" name=\"latitude\" size=\"40\" maxlength=\"40\" value=\"" + latitude[0] + "\"></td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap><p>Longitude</td><td><input type=\"text\" name=\"longitude\" size=\"40\" maxlength=\"40\" value=\"" + longitude[0] + "\"></td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap><p>Google Maps Key</td><td><input type=\"text\" name=\"googleMapsKey\" size=\"80\" maxlength=\"200\" value=\"" + googleMapsKey[0] + "\"></td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td><p><a href=\"javascript:update()\">Update</a></td></tr>");
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "</table></form></body></html>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void setCssDirectory(PrintWriter out, String current, String dnm, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<select name=\"currentStyle\">");
    
    String dir = "/Zara/Support/Css/";
    File path = new File(dir);
    String fs[] = path.list();
    
    int len = fs.length;
    
    generalUtils.insertionSort(fs, len);

    for(int x=0;x<len;++x)
    {
      if(generalUtils.isDirectory(dir + fs[x])) // just-in-case
      {
        scout(out, bytesOut, "<option value='" + fs[x] + "'");
        if(current.equals(fs[x]))
          scout(out, bytesOut, " selected");
        scoutln(out, bytesOut, ">" + fs[x] + "\n");
      }
    }   

    dir = "/Zara/" + dnm + "/Css/";
    path = new File(dir);
    fs = path.list();
    
    len = fs.length;
    
    generalUtils.insertionSort(fs, len);

    for(int x=0;x<len;++x)
    {
      if(generalUtils.isDirectory(dir + fs[x])) // just-in-case
      {
        scout(out, bytesOut, "<option value='" + fs[x] + "'");
        if(current.equals(fs[x]))
          scout(out, bytesOut, " selected");
        scoutln(out, bytesOut, ">" + fs[x] + "\n");
      }
    }   

    scoutln(out, bytesOut, "</select>");
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void getDateStyle(PrintWriter out, String name, String current, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<select name=\"" + name + "\">");

    scout(out, bytesOut, "<option value='DDMMYY'");
    if(current.equals("DDMMYY"))
      scout(out, bytesOut, " selected");
    scoutln(out, bytesOut, ">Day, Month, Year\n");    
    
    scout(out, bytesOut, "<option value='MMDDYY'");
    if(current.equals("MMDDYY"))
      scout(out, bytesOut, " selected");
    scoutln(out, bytesOut, ">Month, Day, Year\n");    
    
    scout(out, bytesOut, "<option value='YYYYMMDD'");
    if(current.equals("YYYYMMDD"))
      scout(out, bytesOut, " selected");
    scoutln(out, bytesOut, ">Year, Month, Day\n");    
    
    scoutln(out, bytesOut, "</select>");
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void getDateSeparator(PrintWriter out, String name, String current, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<select name=\"" + name + "\">");

    scout(out, bytesOut, "<option value='.'");
    if(current.equals("."))
      scout(out, bytesOut, " selected");
    scoutln(out, bytesOut, ">.\n");    
    
    scout(out, bytesOut, "<option value='/'");
    if(current.equals("/"))
      scout(out, bytesOut, " selected");
    scoutln(out, bytesOut, ">/\n");    
    
    scout(out, bytesOut, "<option value='-'");
    if(current.equals("-"))
      scout(out, bytesOut, " selected");
    scoutln(out, bytesOut, ">/\n");    
    
    scoutln(out, bytesOut, "</select>");
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void getMonths(PrintWriter out, String name, String current, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<select name=\"" + name + "\">");

    scout(out, bytesOut, "<option value='January'");
    if(current.equals("January"))
      scout(out, bytesOut, " selected");
    scoutln(out, bytesOut, ">January\n");    
    
    scout(out, bytesOut, "<option value='February'");
    if(current.equals("February"))
      scout(out, bytesOut, " selected");
    scoutln(out, bytesOut, ">February\n");    
    
    scout(out, bytesOut, "<option value='March'");
    if(current.equals("March"))
      scout(out, bytesOut, " selected");
    scoutln(out, bytesOut, ">March\n");    
    
    scout(out, bytesOut, "<option value='April'");
    if(current.equals("April"))
      scout(out, bytesOut, " selected");
    scoutln(out, bytesOut, ">April\n");    
    
    scout(out, bytesOut, "<option value='May'");
    if(current.equals("May"))
      scout(out, bytesOut, " selected");
    scoutln(out, bytesOut, ">May\n");    
    
    scout(out, bytesOut, "<option value='June'");
    if(current.equals("June"))
      scout(out, bytesOut, " selected");
    scoutln(out, bytesOut, ">June\n");    
    
    scout(out, bytesOut, "<option value='July'");
    if(current.equals("July"))
      scout(out, bytesOut, " selected");
    scoutln(out, bytesOut, ">July\n");    
    
    scout(out, bytesOut, "<option value='August'");
    if(current.equals("August"))
      scout(out, bytesOut, " selected");
    scoutln(out, bytesOut, ">August\n");    
    
    scout(out, bytesOut, "<option value='September'");
    if(current.equals("September"))
      scout(out, bytesOut, " selected");
    scoutln(out, bytesOut, ">September\n");    
    
    scout(out, bytesOut, "<option value='October'");
    if(current.equals("October"))
      scout(out, bytesOut, " selected");
    scoutln(out, bytesOut, ">October\n");    
    
    scout(out, bytesOut, "<option value='November'");
    if(current.equals("November"))
      scout(out, bytesOut, " selected");
    scoutln(out, bytesOut, ">November\n");    
    
    scout(out, bytesOut, "<option value='December'");
    if(current.equals("December"))
      scout(out, bytesOut, " selected");
    scoutln(out, bytesOut, ">December\n");    
    
    scoutln(out, bytesOut, "</select>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void scout(PrintWriter out, int[] bytesOut, String str) throws Exception
  {      
    out.print(str);
    bytesOut[0] += str.length();    
  }
  private void scoutln(PrintWriter out, int[] bytesOut, String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += str.length() + 2;    
  }

}
