// =======================================================================================================================================================================================================
// System: ZaraStar Google: Map 
// Module: StockControlServices00.java
// Author: C.K.Harvey
// Copyright (c) 1999-2009 Christopher Harvey. All Rights Reserved.
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

public class GoogleMapServices extends HttpServlet
{
  GeneralUtils  generalUtils = new GeneralUtils(); 
  MessagePage  messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils  pageFrameUtils = new PageFrameUtils();
  DirectoryUtils  directoryUtils = new DirectoryUtils();
  Customer customer = new Customer();
  Supplier  supplier = new Supplier();
  
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="";

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
      p1  = req.getParameter("p1"); // type // C, S, or null
      p2  = req.getParameter("p2"); // customerOrSupplierCode

      if(p1 == null || p1.length() == 0) p1 = " ";
      
      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, bytesOut);
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
 
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "StockControlServices00", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 18100, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush(); 
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1, String p2, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    Connection con = null;
    Statement stmt = null;
    ResultSet rs   = null;

    try
    {
      String uName = directoryUtils.getMySQLUserName();
      String pWord = directoryUtils.getMySQLPassWord();
      Class.forName("com.mysql.jdbc.Driver").newInstance();
      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + uName + "&password=" + pWord);

      set(con, stmt, rs, out, req, p1, p2, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);
    
    }
    catch(Exception e) { } 

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 18100, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);

    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String p1, String p2, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                   String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html xmlns=\"http://www.w3.org/1999/xhtml\">");
    scoutln(out, bytesOut, "<head>");
    scoutln(out, bytesOut, "<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"/>");
    scoutln(out, bytesOut, "<title>Location</title>");

    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    String googleKey = generalUtils.getFromDefnFile("KEY", "map.dfn", localDefnsDir, defnsDir);
    scoutln(out, bytesOut, "<script src=\"http://maps.google.com/maps?file=api&amp;v=2&amp;key=" + googleKey +"\" type=\"text/javascript\"></script>");

    String[] latitude  = new String[1];
    String[] longitude = new String[1];
    String[] address   = new String[1];
    
    switch(p1.charAt(0))
    {
      case 'C' : customer.getGoogleMapDataGivenCode(con, stmt, rs, p2, dnm, localDefnsDir, defnsDir, latitude, longitude, address);
                 break;
      case 'S' : supplier.getGoogleMapDataGivenCode(con, stmt, rs, p2, dnm, localDefnsDir, defnsDir, latitude, longitude, address);                 
                 break;
      default  : // this site
                 latitude[0]  = generalUtils.getFromDefnFile("LATITUDE",  "map.dfn", localDefnsDir, defnsDir);
                 longitude[0] = generalUtils.getFromDefnFile("LONGITUDE", "map.dfn", localDefnsDir, defnsDir);
                 address[0]   = generalUtils.getFromDefnFile("ADDRESS",   "map.dfn", localDefnsDir, defnsDir);
                 break;
    }
    
    if(latitude[0].length() != 0 && longitude[0].length() != 0)
    {
      scoutln(out, bytesOut, "<script language=\"JavaScript\">"); 
      scoutln(out, bytesOut, "var mapLatitude='" + latitude[0] + "';");
      scoutln(out, bytesOut, "var mapLongitude='" + longitude[0] + "';");
      scoutln(out, bytesOut, "var mapAddr = \"" + address[0] + "\"");
      scoutln(out, bytesOut, "</script>"); 

      scoutln(out, bytesOut, "<script language=\"JavaScript\" type=\"text/javascript\" src=\"" + directoryUtils.getGoogleScriptsDirectory() + "map.js\"></script>");
    }
    
    int[] hmenuCount = new int[1];
    
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "18100", " onunload=\"GUnload();\" ", false, false, "", "", "", "", "", "GoogleMapServices", unm, sid, uty, men, den, dnm, bnm, " load(); ", localDefnsDir, "", hmenuCount, bytesOut);

    pageFrameUtils.drawTitle(out, false, false, "GoogleMapServices", "", "", "", "", "", "Location", "18100", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    if(latitude[0].length() != 0 && longitude[0].length() != 0)
      scoutln(out, bytesOut, "<div id=\"map\" style=\"z-index:8888;width: 800px; height: 500px; margin-left:100px; margin-top:50px;\"></div>");
    
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir));
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += str.length() + 2;    
  }

}

