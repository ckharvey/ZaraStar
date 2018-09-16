// =======================================================================================================================================================================================================
// System: ZaraStar Product: Re-Index SC catalog
// Module: CatalogsReIndexSteelclaws.java
// Author: C.K.Harvey
// Copyright (c) 2006-09 Christopher Harvey. All Rights Reserved.
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
import java.sql.*;
import java.util.*;
import java.io.*;

public class CatalogsReIndexSteelclaws extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
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

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", mfr="";

    try
    {
      directoryUtils.setContentHeaders(res);
      out = res.getWriter();

      Enumeration en = req.getParameterNames();
      while(en.hasMoreElements())
      {
        String name = (String)en.nextElement();
        String[] value = req.getParameterValues(name);
        if(name.equals("unm"))
          unm = value[0];
        else
        if(name.equals("sid"))
          sid = value[0];
        else
        if(name.equals("uty"))
          uty = value[0];
        else
        if(name.equals("men"))
          men = value[0];
        else
        if(name.equals("den"))
          den = value[0];
        else
        if(name.equals("dnm"))
          dnm = value[0];
        else
        if(name.equals("bnm"))
          bnm = value[0];
        else
        if(name.equals("mfr"))
          mfr = value[0];
      }
      
      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, mfr, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "CatalogsRecreateGoogleIndexes", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7011, bytesOut[0], 0, "ERR:" + mfr);
      if(out != null) out.flush();
    }
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String mfr, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');
    
    String uName = directoryUtils.getMySQLUserName();
    String pWord = directoryUtils.getMySQLPassWord();
    
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    
    Connection con    = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + uName + "&password=" + pWord);
    Connection conCat = DriverManager.getConnection("jdbc:mysql://localhost:3306/Catalogs_ofsa?user=" + uName + "&password=" + pWord);

    Statement stmt = null, stmt2 = null;
    ResultSet rs   = null, rs2 = null;

    mfr = generalUtils.deSanitise(mfr);

    try
    {
      generalUtils.directoryHierarchyDelete2("/Zara/Index/" + mfr + "/");   
    }
    catch(Exception e) { }

    try
    {
      generalUtils.createDir("/Zara/Index/" + mfr + "/", true);
    }
    catch(Exception e) { }
    
    String fileName = "/Zara/Index/index.html";

    RandomAccessFile fh = generalUtils.create(fileName);
    
    fh.writeBytes("<html><head><title>Catalogs</title></head><body>");

    stmt = conCat.createStatement();
    rs = stmt.executeQuery("SELECT Category, Title, Text, Text2 FROM catalog WHERE Manufacturer = '" + mfr + "' ORDER BY Category");

    String category;
    
    while(rs.next())
    {
      category = rs.getString(1);
      
      outputLines(conCat, stmt2, rs2, mfr, category, rs.getString(2), rs.getString(3), rs.getString(4), localDefnsDir);
      fh.writeBytes("<a href=\"/Zara/Index/" + mfr + "/" + category + ".html\">" + category + "</a>\n");
    }

    fh.writeBytes("</body></html>\n");

    generalUtils.fileClose(fh);    

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    messagePage.msgScreen(false, out, req, 27, unm, sid, uty, men, den, dnm, bnm, "CatalogsReIndexSteelclaws", imagesDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 7011, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), mfr);
    if(con    != null) con.close();
    if(conCat != null) conCat.close();
    if(out != null) out.flush();
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void outputLines(Connection con, Statement stmt, ResultSet rs, String mfr, String category, String title, String text, String text2, String localDefnsDir) throws Exception
  {
    String fileName = "/Zara/Index/" + mfr + "/" + category + ".html";

    RandomAccessFile fh = generalUtils.create(fileName);
    
    fh.writeBytes("<html><head><title>" + mfr + "</title>");
    fh.writeBytes("<script language='JavaScript'>");
    fh.writeBytes("function go(){window.location.href=\"http://" + serverUtils.serverToCall("ZC", localDefnsDir) + "/central/servlet/CatalogZaraGoogleSearch?p1=" + mfr + "&p2=" + category + "\";}");
    fh.writeBytes("</script></head>");
    fh.writeBytes("<body onload='go()'>");
    fh.writeBytes("<table>");
    fh.writeBytes("<tr><td>" + mfr + "</td></tr>");

    if(title != null && title.length() > 0) fh.writeBytes("<tr><td>" + title + "</td></tr>");
    if(text  != null && text.length()  > 0) fh.writeBytes("<tr><td>" + text + "</td></tr>");
    if(text2 != null && text2.length() > 0) fh.writeBytes("<tr><td>" + text2 + "</td></tr>");

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT ManufacturerCode, Description, Description2 FROM catalogl WHERE Manufacturer = '" + mfr + "' AND Category = '" + category + "' ORDER BY ManufacturerCode");

    String mfrCode, desc, desc2;
    
    while(rs.next())
    {
      mfrCode = rs.getString(1);
      desc    = rs.getString(2);
      desc2   = rs.getString(3);
    
      if(mfrCode == null) mfrCode = "";
      if(desc    == null) desc    = "";
      if(desc2   == null) desc2   = "";
      
      if(mfrCode.length() > 0)        fh.writeBytes("<tr><td>" + mfrCode + "</td></tr>");
      if((desc + desc2).length() > 0) fh.writeBytes("<tr><td>" + desc + " " + desc2 + "</td></tr>");
      
      fh.writeBytes("\n");
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    fh.writeBytes("</table></body></html>\n");

    generalUtils.fileClose(fh);    
  }

}
