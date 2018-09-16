// =======================================================================================================================================================================================================
// System: ZaraStar: CompanyEngine: customer select page
// Module: CustomerSelect.java
// Author: C.K.Harvey
// Copyright (c) 1999-2008 Christopher Harvey. All Rights Reserved.
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

public class CustomerSelect extends HttpServlet
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
      StringBuffer url = req.getRequestURL();
      int x=0;
      if(url.charAt(x) == 'h' || url.charAt(x) == 'H')
        x += 7;
      String urlBit="";
      while(url.charAt(x) != '\000' && url.charAt(x) != ',' && url.charAt(x) != '/')
        urlBit += url.charAt(x++);
      
      System.out.println(("Unexpected System Error: 4230: " + e));
      res.getWriter().write("Unexpected System Error: 4230");
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(HttpServletRequest req, HttpServletResponse res, String searchChar, String alsoName, String unm, String sid, String uty, String dnm,
                    int[] bytesOut) throws Exception
  {
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');

    res.setContentType("text/xml");
    res.setHeader("Cache-Control", "no-cache");

    Writer r = res.getWriter();

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      r.write("Access Denied - Duplicate SignOn or Session Timeout");
      serverUtils.etotalBytes(req, unm, dnm, 4230, bytesOut[0], 0, "SID:" + searchChar);
      return;
    }
    
    if(searchChar.charAt(0) == '-') // others
      searchOthers(r, alsoName, dnm, imagesDir, localDefnsDir, defnsDir);
    else select4230(r, alsoName, dnm, imagesDir, localDefnsDir, defnsDir, searchChar.charAt(0));
    
    r.write("</table>");

    serverUtils.totalBytes(req, unm, dnm, 4230, bytesOut[0], 0, searchChar);
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void select4230(Writer r, String alsoName, String dnm, String imagesDir, String localDefnsDir, String defnsDir,
                             char searchChar) throws Exception
  {
    setHead(r, imagesDir);

    String userName = directoryUtils.getMySQLUserName();
    String passWord = directoryUtils.getMySQLPassWord();
    
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + userName + "&password="
                                                 + passWord);
    Statement stmt = con.createStatement();

    String code, name, addr1, quoteOnly, currency;

    ResultSet rs = stmt.executeQuery("SELECT CompanyCode, Name, Address1, QuotationOnly, Currency FROM company WHERE Name LIKE '"
                                     + searchChar + "%' ORDER BY Name");

    while(rs.next())
    {    
      code      = rs.getString(1);
      name      = rs.getString(2);
      addr1     = rs.getString(3);
      quoteOnly = rs.getString(4);
      currency  = rs.getString(5);

      writeBodyLine(r, alsoName, code, name, addr1, quoteOnly, currency);
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    if(con  != null) con.close(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void searchOthers(Writer r, String alsoName, String dnm, String imagesDir, String localDefnsDir, String defnsDir)
                              throws Exception
  {
    setHead(r, imagesDir);

    String userName = directoryUtils.getMySQLUserName();
    String passWord = directoryUtils.getMySQLPassWord();
    
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + userName + "&password="
                                                 + passWord);
    Statement stmt = con.createStatement();

    String code, name, addr1, quoteOnly, currency;

    ResultSet rs = stmt.executeQuery("SELECT CompanyCode, Name, Address1, QuotationOnly, Currency FROM company WHERE Name NOT LIKE 'A%' AND"
                                   + " Name NOT LIKE 'B%' AND Name NOT LIKE 'C%' AND Name NOT LIKE 'D%' AND Name NOT LIKE 'E%' AND"
                                   + " Name NOT LIKE 'F%' AND Name NOT LIKE 'G%' AND Name NOT LIKE 'H%' AND Name NOT LIKE 'I%' AND"
                                   + " Name NOT LIKE 'J%' AND Name NOT LIKE 'K%' AND Name NOT LIKE 'L%' AND Name NOT LIKE 'M%' AND"
                                   + " Name NOT LIKE 'N%' AND Name NOT LIKE 'O%' AND Name NOT LIKE 'P%' AND Name NOT LIKE 'Q%' AND"
                                   + " Name NOT LIKE 'R%' AND Name NOT LIKE 'S%' AND Name NOT LIKE 'T%' AND Name NOT LIKE 'U%' AND"
                                   + " Name NOT LIKE 'V%' AND Name NOT LIKE 'W%' AND Name NOT LIKE 'X%' AND Name NOT LIKE 'Y%' AND"
                                   + " Name NOT LIKE 'Z%' ORDER BY Name");

    while(rs.next())
    {    
      code      = rs.getString(1);
      name      = rs.getString(2);
      addr1     = rs.getString(3);
      quoteOnly = rs.getString(4);
      currency  = rs.getString(5);

      writeBodyLine(r, alsoName, code, name, addr1, quoteOnly, currency);
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    if(con  != null) con.close(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void setHead(Writer r, String imagesDir) throws Exception
  {
    r.write("<table border='0' cellspacing='2' cellpadding='2'>");
    r.write("<tr><td colspan='3'><font face='Arial,Helvetica' size='2' color='#000000'><b>Customer Select</b></font></td></tr>");
    r.write("<tr><td colspan='4'><img src='" + imagesDir + "blm.gif' width='100%' height='18' /></td></tr>");

    r.write("<tr><td colspan='4'><font face='Arial,Helvetica' size='2'>\n");
    r.write("<a href=\"javascript:select4230('A')\">A</a>&#160;&#160;\n");
    r.write("<a href=\"javascript:select4230('B')\">B</a>&#160;&#160;\n");
    r.write("<a href=\"javascript:select4230('C')\">C</a>&#160;&#160;\n");
    r.write("<a href=\"javascript:select4230('D')\">D</a>&#160;&#160;\n");
    r.write("<a href=\"javascript:select4230('E')\">E</a>&#160;&#160;\n");
    r.write("<a href=\"javascript:select4230('F')\">F</a>&#160;&#160;\n");
    r.write("<a href=\"javascript:select4230('G')\">G</a>&#160;&#160;\n");
    r.write("<a href=\"javascript:select4230('H')\">H</a>&#160;&#160;\n");
    r.write("<a href=\"javascript:select4230('I')\">I</a>&#160;&#160;\n");
    r.write("<a href=\"javascript:select4230('J')\">J</a>&#160;&#160;\n");
    r.write("<a href=\"javascript:select4230('K')\">K</a>&#160;&#160;\n");
    r.write("<a href=\"javascript:select4230('L')\">L</a>&#160;&#160;\n");
    r.write("<a href=\"javascript:select4230('M')\">M</a>&#160;&#160;\n");
    r.write("<a href=\"javascript:select4230('N')\">N</a>&#160;&#160;\n");
    r.write("<a href=\"javascript:select4230('O')\">O</a>&#160;&#160;\n");
    r.write("<a href=\"javascript:select4230('P')\">P</a>&#160;&#160;\n");
    r.write("<a href=\"javascript:select4230('Q')\">Q</a>&#160;&#160;\n");
    r.write("<a href=\"javascript:select4230('R')\">R</a>&#160;&#160;\n");
    r.write("<a href=\"javascript:select4230('S')\">S</a>&#160;&#160;\n");
    r.write("<a href=\"javascript:select4230('T')\">T</a>&#160;&#160;\n");
    r.write("<a href=\"javascript:select4230('U')\">U</a>&#160;&#160;\n");
    r.write("<a href=\"javascript:select4230('V')\">V</a>&#160;&#160;\n");
    r.write("<a href=\"javascript:select4230('W')\">W</a>&#160;&#160;\n");
    r.write("<a href=\"javascript:select4230('X')\">X</a>&#160;&#160;\n");
    r.write("<a href=\"javascript:select4230('Y')\">Y</a>&#160;&#160;\n");
    r.write("<a href=\"javascript:select4230('Z')\">Z</a>&#160;&#160;\n");
    r.write("<a href=\"javascript:select4230('-')\">Others</a></font></td></tr>\n");

    r.write("<tr><td>&#160;</td></tr>");
    r.write("<tr><td colspan='4'><img src='" + imagesDir + "blm2.gif' width='100%' height='3' /></td></tr>\n");

    r.write("<tr><td bgcolor='#C0C0C0' height='18'><font face='Arial,Helvetica' color='#000000' size='2'>Type&#160;</font></td>\n");
    r.write("<td bgcolor='#C0C0C0'><font face='Arial,Helvetica' size='2' color='#000000'>Code&#160;</font></td>\n");
    r.write("<td bgcolor='#C0C0C0'><font face='Arial,Helvetica' size='2' color='#000000'>Name&#160;</font></td>\n");
    r.write("<td bgcolor='#C0C0C0'><font face='Arial,Helvetica' size='2' color='#000000'>Currency&#160;</font></td>\n");
    r.write("<td bgcolor='#C0C0C0'><font face='Arial,Helvetica' size='2' color='#000000'>Address 1</font></td></tr>\n");
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void writeBodyLine(Writer r, String alsoName, String code, String name, String addr1, String quoteOnly, String currency)
                               throws Exception
  {
    if(quoteOnly.length() > 0 && quoteOnly.charAt(0) == 'Y')
      r.write("<tr><td><font face='Arial, Helvetica' color='#FF0000' size='2'>QuoteOnly&#160;</font></td>");
    else r.write("<tr><td></td>");

    if(alsoName.equals("Y"))
      r.write("<td nowrap='nowrap'><a href=\"javascript:setCode('" + code + "','" + name + "')\">");
    else r.write("<td nowrap='nowrap'><font face='Arial, Helvetica' size='2'><a href=\"javascript:setCode('" + code + "')\">");

    r.write(code + "</a>&#160;</font></td>\n");

    r.write("<td nowrap='nowrap'><font face='Arial, Helvetica' size='2' color='#880000'>" + generalUtils.sanitiseForXML(name) + "</font></td>\n");
    r.write("<td nowrap='nowrap'><font face='Arial, Helvetica' size='2' color='#880000'>" + currency + "</font></td>\n");
    r.write("<td nowrap='nowrap'><font face='Arial, Helvetica' size='2' color='#880000'>" + generalUtils.sanitiseForXML(addr1) + "</font></td></tr>\n");
  }
  
}
