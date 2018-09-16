// =======================================================================================================================================================================================================
// System: ZaraStar WO: fetch line for inline edit
// Module: WorksOrderLineEdit.java
// Author: C.K.Harvey
// Copyright (c) 2006-09 Christopher Harvey. All Rights Reserved.
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
import java.util.Enumeration;

public class WorksOrderLineEdit extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  MiscDefinitions miscDefinitions = new MiscDefinitions();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", dnm="", code="", line="";

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
          code = value[0];
        else
        if(elementName.equals("p2"))
          line = value[0];
      }

      doIt(req, res, unm, sid, uty, dnm, code, line, bytesOut);
    }
    catch(Exception e)
    {
      System.out.println(("Unexpected System Error: 4431a: " + e));
      res.getWriter().write("Unexpected System Error: 4431a");
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(HttpServletRequest req, HttpServletResponse res, String unm, String sid, String uty, String dnm, String code, String line, int[] bytesOut) throws Exception
  {
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    String[] entry        = new String[1]; entry[0]="";
    String[] itemCode     = new String[1]; itemCode[0]="";
    String[] desc         = new String[1]; desc[0]="";
    String[] mfr          = new String[1]; mfr[0]="";
    String[] mfrCode      = new String[1]; mfrCode[0]="";
    String[] custItemCode = new String[1]; custItemCode[0]="";
    String[] qty          = new String[1]; qty[0]="";
    String[] uom          = new String[1]; uom[0]="";
    String[] remark       = new String[1]; remark[0]="";

    String rtn="Unexpected System Error: 4431a";

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
      rtn = "Access Denied - Duplicate SignOn or Session Timeout";
    else
    {
      if(fetch(dnm, code, line, entry, itemCode, desc, mfr, mfrCode, custItemCode, qty, uom, remark))
        rtn = ".";
    }

    res.setContentType("text/xml");
    res.setHeader("Cache-Control", "no-cache");

    String s = "<msg><res>" + rtn + "</res><line><![CDATA[" + line + "]]></line><entry><![CDATA[" + entry[0] + "]]></entry><itemCode><![CDATA[" + itemCode[0] + "]]></itemCode><desc><![CDATA[" + desc[0] + "]]></desc><mfr><![CDATA[" + mfr[0]
             + "]]></mfr><mfrCode><![CDATA[" + mfrCode[0] + "]]></mfrCode><custItemCode><![CDATA[" + custItemCode[0] + "]]></custItemCode><qty><![CDATA[" + qty[0] + "]]></qty><uom><![CDATA[" + uom[0] + "]]></uom><remark><![CDATA[" + remark[0]
             + "]]></remark></msg>";
    
    res.getWriter().write(s);

    serverUtils.totalBytes(req, unm, dnm, 4431, bytesOut[0], 0, code + ":" + line);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // rtns: ' ' if rec added/updated ok
  //       'E' if adding a rec that already exists
  //       'X' other error
  private boolean fetch(String dnm, String code, String line, String[] entry, String[] itemCode, String[] desc, String[] mfr, String[] mfrCode, String[] custItemCode, String[] qty, String[] uom, String[] remark) throws Exception
  {
    Connection con = null;
    Statement stmt = null;
    ResultSet rs   = null;

    try
    {
      String uName    = directoryUtils.getMySQLUserName();
      String passWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();

      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + uName + "&password=" + passWord);

      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT Entry, ItemCode, Description, Manufacturer, ManufacturerCode, CustomerItemCode, Quantity, UoM, Remark FROM wol WHERE WOCode = '" + generalUtils.sanitiseForSQL(code) + "' AND Line = '" + line + "'");

      if(rs.next())
      {
        entry[0]        = rs.getString(1);
        itemCode[0]     = rs.getString(2);
        desc[0]         = rs.getString(3);
        mfr[0]          = rs.getString(4);
        mfrCode[0]      = rs.getString(5);
        custItemCode[0] = rs.getString(6);
        qty[0]          = rs.getString(7);
        uom[0]          = rs.getString(8);
        remark[0]       = rs.getString(9);
      }

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();

      qty[0] = generalUtils.doubleDPs(qty[0], miscDefinitions.dpOnQuantities(con, stmt, rs, "5"));

      if(con  != null) con.close();
      return true;
    }
    catch(Exception e)
    {
      System.out.println("4431a: " + e);
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }

    return false;
  }

}
