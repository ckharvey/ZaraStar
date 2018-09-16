// =======================================================================================================================================================================================================
// System: ZaraStar: ProductEngine: stock create first select page
// Module: CatalogStockPage.java
// Author: C.K.Harvey
// Copyright (c) 1999-2007 Christopher Harvey. All Rights Reserved.
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
import java.util.Enumeration;
import java.io.*;

public class CatalogStockPage extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", dnm="", p1="", p2="";

    try
    {
      directoryUtils.setContentHeaders(res);
      res.setHeader("Header", "HTTP/1.0 200 OK");
      res.setStatus(200);

      Enumeration en = req.getParameterNames();
      while(en.hasMoreElements())
      {
        String elementName = (String)en.nextElement();
        String[] value = req.getParameterValues(elementName);
        if(elementName.equals("unm"))
          unm = value[0];
        else
        if(elementName.equals("sid"))
          sid = value[0];
        else
        if(elementName.equals("uty"))
          uty = value[0];
        else
        if(elementName.equals("dnm"))
          dnm = value[0];
        else
        if(elementName.equals("p1"))
          p1 = value[0];
        else
        if(elementName.equals("p2"))
          p2 = value[0];
      }

      if(p2 == null) p2 = "";

      doIt(req, res, p1, p2, unm, sid, uty, dnm, bytesOut);
    }
    catch(Exception e)
    {
      System.out.println(("Unexpected System Error: 2008: " + e));
      serverUtils.etotalBytes(req, unm, dnm, 2008, bytesOut[0], 0, "ERR:" + p1);
      res.getWriter().write("Unexpected System Error: 2008");
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(HttpServletRequest req, HttpServletResponse res, String searchChar, String alsoName, String unm, String sid, String uty,
                    String dnm, int[] bytesOut) throws Exception
  {
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');
    
    long startTime = new java.util.Date().getTime();

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;    
    
    res.setContentType("text/xml");
    res.setHeader("Cache-Control", "no-cache");

    Writer r = res.getWriter();

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      r.write("Access Denied - Duplicate SignOn or Session Timeout");
      serverUtils.etotalBytes(req, unm, dnm, 2008, bytesOut[0], 0, "SID:" + searchChar);
      if(con  != null) con.close();
      return;
    }
    
    if(searchChar.charAt(0) == '-') // others
      searchOthers(con, stmt, rs, r, alsoName, dnm, imagesDir, localDefnsDir, defnsDir);
    else select2008(con, stmt, rs, r, alsoName, dnm, imagesDir, localDefnsDir, defnsDir, searchChar.charAt(0));
    
    r.write("</table>");

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 2008, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), searchChar);
    if(con  != null) con.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void select2008(Connection con, Statement stmt, ResultSet rs, Writer r, String alsoName, String dnm, String imagesDir, String localDefnsDir, String defnsDir, char searchChar)
                          throws Exception
  {
    setHead(r, imagesDir);

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT DISTINCT Manufacturer FROM stock WHERE Manufacturer LIKE '" + searchChar + "%' ORDER BY Manufacturer");

    while(rs.next())
    {    
      writeBodyLine(r, rs.getString(1));
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void searchOthers(Connection con, Statement stmt, ResultSet rs, Writer r, String alsoName, String dnm, String imagesDir, String localDefnsDir, String defnsDir)
                              throws Exception
  {
    setHead(r, imagesDir);

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT DISTINCT Manufacturer FROM stock WHERE Manufacturer NOT LIKE 'A%' "
                                   + "AND Manufacturer NOT LIKE 'B%' AND Manufacturer NOT LIKE 'C%' AND Manufacturer NOT LIKE 'D%' "
                                   + "AND Manufacturer NOT LIKE 'E%' AND Manufacturer NOT LIKE 'F%' AND Manufacturer NOT LIKE 'G%' "
                                   + "AND Manufacturer NOT LIKE 'H%' AND Manufacturer NOT LIKE 'I%' AND Manufacturer NOT LIKE 'J%' "
                                   + "AND Manufacturer NOT LIKE 'K%' AND Manufacturer NOT LIKE 'L%' AND Manufacturer NOT LIKE 'M%' "
                                   + "AND Manufacturer NOT LIKE 'N%' AND Manufacturer NOT LIKE 'O%' AND Manufacturer NOT LIKE 'P%' "
                                   + "AND Manufacturer NOT LIKE 'Q%' AND Manufacturer NOT LIKE 'R%' AND Manufacturer NOT LIKE 'S%' "
                                   + "AND Manufacturer NOT LIKE 'T%' AND Manufacturer NOT LIKE 'U%' AND Manufacturer NOT LIKE 'V%' "
                                   + "AND Manufacturer NOT LIKE 'W%' AND Manufacturer NOT LIKE 'X%' AND Manufacturer NOT LIKE 'Y%' "
                                   + "AND Manufacturer NOT LIKE 'Z%' ORDER BY Manufacturer");

    while(rs.next())
    {    
      writeBodyLine(r, rs.getString(1));
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void setHead(Writer r, String imagesDir) throws Exception
  {
    r.write("<table cellspacing=\"2\" cellpadding=\"2\">\n");
    r.write("<tr><td colspan=\"3\"><font face=\"Arial,Helvetica\" size=\"2\" color=\"#000000\"><b>Stock Item Select</b></font></td></tr>\n");
    r.write("<tr><td colspan=\"4\"><img src=\"" + imagesDir + "blm.gif\" width=\"100%\" height=\"18\" /></td></tr>\n");

    r.write("<tr><td colspan=\"4\"><font face=\"Arial,Helvetica\" size=\"2\">\n");
    r.write("<a href=\"javascript:select2008('A')\">A</a>&#160;&#160;\n");
    r.write("<a href=\"javascript:select2008('B')\">B</a>&#160;&#160;\n");
    r.write("<a href=\"javascript:select2008('C')\">C</a>&#160;&#160;\n");
    r.write("<a href=\"javascript:select2008('D')\">D</a>&#160;&#160;\n");
    r.write("<a href=\"javascript:select2008('E')\">E</a>&#160;&#160;\n");
    r.write("<a href=\"javascript:select2008('F')\">F</a>&#160;&#160;\n");
    r.write("<a href=\"javascript:select2008('G')\">G</a>&#160;&#160;\n");
    r.write("<a href=\"javascript:select2008('H')\">H</a>&#160;&#160;\n");
    r.write("<a href=\"javascript:select2008('I')\">I</a>&#160;&#160;\n");
    r.write("<a href=\"javascript:select2008('J')\">J</a>&#160;&#160;\n");

    r.write("</font></td></tr><tr><td colspan=\"4\"><font face=\"Arial,Helvetica\" size=\"2\">\n");

    r.write("<a href=\"javascript:select2008('K')\">K</a>&#160;&#160;\n");
    r.write("<a href=\"javascript:select2008('L')\">L</a>&#160;&#160;\n");
    r.write("<a href=\"javascript:select2008('M')\">M</a>&#160;&#160;\n");
    r.write("<a href=\"javascript:select2008('N')\">N</a>&#160;&#160;\n");
    r.write("<a href=\"javascript:select2008('O')\">O</a>&#160;&#160;\n");
    r.write("<a href=\"javascript:select2008('P')\">P</a>&#160;&#160;\n");
    r.write("<a href=\"javascript:select2008('Q')\">Q</a>&#160;&#160;\n");
    r.write("<a href=\"javascript:select2008('R')\">R</a>&#160;&#160;\n");
    r.write("<a href=\"javascript:select2008('S')\">S</a>&#160;&#160;\n");
    r.write("<a href=\"javascript:select2008('T')\">T</a>&#160;&#160;\n");

    r.write("</font></td></tr><tr><td colspan=\"4\"><font face=\"Arial,Helvetica\" size=\"2\">\n");
    
    r.write("<a href=\"javascript:select2008('U')\">U</a>&#160;&#160;\n");
    r.write("<a href=\"javascript:select2008('V')\">V</a>&#160;&#160;\n");
    r.write("<a href=\"javascript:select2008('W')\">W</a>&#160;&#160;\n");
    r.write("<a href=\"javascript:select2008('X')\">X</a>&#160;&#160;\n");
    r.write("<a href=\"javascript:select2008('Y')\">Y</a>&#160;&#160;\n");
    r.write("<a href=\"javascript:select2008('Z')\">Z</a>&#160;&#160;\n");
    r.write("<a href=\"javascript:select2008('-')\">Others</a></font></td></tr>\n");

    r.write("<tr><td>&#160;</td></tr>\n");
    r.write("<tr><td colspan=\"4\"><img src=\"" + imagesDir + "blm2.gif\" width=\"100%\" height=\"3\" /></td></tr>\n");

    r.write("<tr><td bgcolor=\"#C0C0C0\" height=\"18\"><font face=\"Arial, Helvetica\" color=\"#000000\" size=\"2\">Manufacturer/Brand&#160;</font></td></tr>\n");

  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void writeBodyLine(Writer r, String mfr) throws Exception
  {
    r.write("<tr><td nowrap='nowrap'><a href=\"javascript:select2008a('" + generalUtils.sanitise(mfr)
          + "','F','','','','','','')\"><font face=\"Arial, Helvetica\" color=\"#000000\" size=\"2\">" + mfr + "</font></a></td></tr>\n");
  }

}
