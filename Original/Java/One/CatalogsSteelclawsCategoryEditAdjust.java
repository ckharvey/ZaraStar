// =======================================================================================================================================================================================================
// System: ZaraStar Catalogs: SC category edit - adjust table data
// Module: CatalogsSteelclawsCategoryEditAdjust.java
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
import java.util.Enumeration;
import java.sql.*;

public class CatalogsSteelclawsCategoryEditAdjust extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  AdminUtils adminUtils = new AdminUtils();
  DefinitionTables definitionTables = new DefinitionTables();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", mfr="", category="", rows="", cols="";

    try
    {
      directoryUtils.setContentHeaders(res);
      res.setHeader("Header", "HTTP/1.0 200 OK");
      res.setStatus(200);
  
      res.setContentType("text/xml");
      res.setHeader("Cache-Control", "no-cache");

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
        if(name.equals("p1"))
          mfr = value[0];
        else
        if(name.equals("p2"))
          category = value[0];
        else
        if(name.equals("p3"))
          rows = value[0];
        else
        if(name.equals("p4"))
          cols = value[0];
      }

      doIt(req, res, mfr, category, rows, cols, unm, sid, uty, men, den, dnm, bnm, bytesOut);
    }    
    catch(Exception e)
    {
      System.out.println(("Unexpected System Error: 6402c: " + e));
      res.getWriter().write("Unexpected System Error: 6402c");
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(HttpServletRequest req, HttpServletResponse res, String mfr, String category, String rows, String cols, String unm, String sid,
                    String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
 
    String rtn = "", dot = "Unexpected System Error: 6402c";
    
    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      dot = "Access Denied - Duplicate SignOn or Session Timeout";
    }
    else
    {
      if(! generalUtils.isInteger(rows))
        dot = "Rows not integer";
      else
      if(! generalUtils.isInteger(cols))
        dot = "Columns not integer";
      else
      {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs   = null;
    
        try
        {
          String userName = directoryUtils.getMySQLUserName();
          String passWord = directoryUtils.getMySQLPassWord();
    
          Class.forName("com.mysql.jdbc.Driver").newInstance();
    
          con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + userName + "&password=" + passWord);

          int numRows = generalUtils.strToInt(rows);
          int numCols = generalUtils.strToInt(cols);
          
          String headData = getHeaderData(con, stmt, rs, mfr, category, numCols);
          
          rtn = getLinesData(con, stmt, rs, mfr, category, numRows, numCols, headData);

          dot = ".";
          
          if(con  != null) con.close(); 
        }
        catch(Exception e) { }
      }
    }
    
    String s = "<msg><res>" + dot + "</res><stuff><![CDATA[" + rtn + "]]></stuff></msg>";

    res.getWriter().write(s);
    
    serverUtils.totalBytes(req, unm, dnm, 6402, bytesOut[0], 0, "");
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private String getLinesData(Connection con, Statement stmt, ResultSet rs, String mfr, String category, int numRows, int numCols, String headData)
                              throws Exception
  {
    String rtn = "<table id=\"page\" border=0 cellpadding=0 cellspacing=0>";
    
    rtn += headData;

    try
    {
      stmt = con.createStatement();
  
      rs = stmt.executeQuery("SELECT * FROM catalogl WHERE Manufacturer = '" + generalUtils.sanitiseForSQL(mfr) + "' AND Category = '" + category + "'");

      String mfrCode, entry1, entry2, entry3, entry4, entry5, entry6, entry7, entry8, entry9, entry10, desc, desc2, line;
      int count = 0, x = 0;
      
      while(rs.next())
      {
        if(count < numRows)
        {
          mfrCode = rs.getString(3);
          entry1  = rs.getString(4);
          entry2  = rs.getString(5);
          entry3  = rs.getString(6);
          entry4  = rs.getString(7);
          entry5  = rs.getString(8);
          entry6  = rs.getString(9);
          entry7  = rs.getString(10);
          entry8  = rs.getString(11);
          entry9  = rs.getString(12);
          entry10 = rs.getString(13);
          desc    = rs.getString(14);
          desc2   = rs.getString(15);
          line    = rs.getString(16);

          rtn += "<tr><td><p><input type=text name=mfrCode" + count + " size=30 maxlength=30 tabindex=" + (count + (0 * numRows)) + " value=\"" + mfrCode + "\"></td>";
          rtn += "<td><p><input type=text name=line" + count + " size=2 maxlength=10 tabindex=" + (count + (1 * numRows)) + " value=\"" + line  + "\"></td>";
          rtn += "<td><p><input type=text name=desca" + count + " size=20 maxlength=80 tabindex=" + (count + (1 * numRows)) + " value=\"" + desc  + "\"></td>";
          rtn += "<td><p><input type=text name=descb" + count + " size=20 maxlength=80 tabindex=" + (count + (2 * numRows)) + " value=\"" + desc2 + "\"></td>";
        
          if(numCols > 0)
            rtn += "<td><p><input type=text name=entry1" + count + " size=12 maxlength=100 tabindex=" + (count + (3 * numRows)) + " value=\"" + entry1  + "\"></td>";
          if(numCols > 1)
            rtn += "<td><p><input type=text name=entry2" + count + " size=12 maxlength=100 tabindex=" + (count + (4 * numRows)) + " value=\"" + entry2  + "\"></td>";
          if(numCols > 2)
            rtn += "<td><p><input type=text name=entry3" + count + " size=12 maxlength=100 tabindex=" + (count + (5 * numRows)) + " value=\"" + entry3  + "\"></td>";
          if(numCols > 3)
            rtn += "<td><p><input type=text name=entry4" + count + " size=12 maxlength=100 tabindex=" + (count + (6 * numRows)) + " value=\"" + entry4  + "\"></td>";
          if(numCols > 4)
            rtn += "<td><p><input type=text name=entry5" + count + " size=12 maxlength=100 tabindex=" + (count + (7 * numRows)) + " value=\"" + entry5  + "\"></td>";
          if(numCols > 5)
            rtn += "<td><p><input type=text name=entry6" + count + " size=12 maxlength=100 tabindex=" + (count + (8 * numRows)) + " value=\"" + entry6  + "\"></td>";
          if(numCols > 6)
            rtn += "<td><p><input type=text name=entry7" + count + " size=12 maxlength=100 tabindex=" + (count + (9 * numRows)) + " value=\"" + entry7  + "\"></td>";
          if(numCols > 7)
            rtn += "<td><p><input type=text name=entry8" + count + " size=12 maxlength=100 tabindex=" + (count + (10 * numRows)) + " value=\"" + entry8  + "\"></td>";
          if(numCols > 8)
            rtn += "<td><p><input type=text name=entry9" + count + " size=12 maxlength=100 tabindex=" + (count + (11 * numRows)) + " value=\"" + entry9  + "\"></td>";
          if(numCols > 9)
            rtn += "<td><p><input type=text name=entry0" + count + " size=12 maxlength=100 tabindex=" + (count + (12 * numRows)) + " value=\"" + entry10 + "\"></td>";

          rtn += "</tr>";
        }
        
        ++count;
      }
    
      // add 'extra' rows
      while(count < numRows)
      {
        rtn += "<tr><td><p><input type=text name=mfrCode" + count + " size=30 maxlength=30 tabindex=" + (count + (0 * numRows)) + "></td>";
        rtn += "<td><p><input type=text name=line" + count + " size=2 maxlength=10 tabindex=" + (count + (1 * numRows)) + "></td>";
        rtn += "<td><p><input type=text name=desca" + count + " size=20 maxlength=80 tabindex=" + (count + (1 * numRows)) + "></td>";
        rtn += "<td><p><input type=text name=descb" + count + " size=20 maxlength=80 tabindex=" + (count + (2 * numRows)) + "></td>";
        
        if(numCols > 0)
          rtn += "<td><p><input type=text name=entry1" + count + " size=12 maxlength=100 tabindex=" + (count + (3 * numRows)) + "></td>";
        if(numCols > 1)
          rtn += "<td><p><input type=text name=entry2" + count + " size=12 maxlength=100 tabindex=" + (count + (4 * numRows)) + "></td>";
        if(numCols > 2)
          rtn += "<td><p><input type=text name=entry3" + count + " size=12 maxlength=100 tabindex=" + (count + (5 * numRows)) + "></td>";
        if(numCols > 3)
          rtn += "<td><p><input type=text name=entry4" + count + " size=12 maxlength=100 tabindex=" + (count + (6 * numRows)) + "></td>";
        if(numCols > 4)
          rtn += "<td><p><input type=text name=entry5" + count + " size=12 maxlength=100 tabindex=" + (count + (7 * numRows)) + "></td>";
        if(numCols > 5)
          rtn += "<td><p><input type=text name=entry6" + count + " size=12 maxlength=100 tabindex=" + (count + (8 * numRows)) + "></td>";
        if(numCols > 6)
          rtn += "<td><p><input type=text name=entry7" + count + " size=12 maxlength=100 tabindex=" + (count + (9 * numRows)) + "></td>";
        if(numCols > 7)
          rtn += "<td><p><input type=text name=entry8" + count + " size=12 maxlength=100 tabindex=" + (count + (10 * numRows)) + "></td>";
        if(numCols > 8)
          rtn += "<td><p><input type=text name=entry9" + count + " size=12 maxlength=100 tabindex=" + (count + (11 * numRows)) + "></td>";
        if(numCols > 9)
          rtn += "<td><p><input type=text name=entry0" + count + " size=12 maxlength=100 tabindex=" + (count + (12 * numRows)) + "></td>";
        
        rtn += "</tr>";

        ++count;
      }
      
      rtn += "<tr><td>&nbsp;</td></tr></table>";
            
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
    
    return rtn;
  }  

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private String getHeaderData(Connection con, Statement stmt, ResultSet rs, String mfr, String category, int cols) throws Exception
  {
    String rtn = "<tr><td><p>Mfr Code</td><td><p>Line</td><td><p>Desc</td><td><p>Desc 2</td>";

    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT Heading1, Heading2, Heading3, Heading4, Heading5, Heading6, Heading7, Heading8, Heading9, Heading10 "
                           + "FROM catalog WHERE Manufacturer = '" + generalUtils.sanitiseForSQL(mfr) + "' AND Category = '" + category + "'");

      if(rs.next())
      {
        if(cols > 0)
          rtn += "<td><p><input type=text name=heading1  size=12 maxlength=30 tabindex=1000 value=\"" + rs.getString(1) + "\"></td>";
        if(cols > 1)
          rtn += "<td><p><input type=text name=heading2  size=12 maxlength=30 tabindex=1000 value=\"" + rs.getString(2) + "\"></td>";
        if(cols > 2)
          rtn += "<td><p><input type=text name=heading3  size=12 maxlength=30 tabindex=1000 value=\"" + rs.getString(3) + "\"></td>";
        if(cols > 3)
          rtn += "<td><p><input type=text name=heading4  size=12 maxlength=30 tabindex=1000 value=\"" + rs.getString(4) + "\"></td>";
        if(cols > 4)
          rtn += "<td><p><input type=text name=heading5  size=12 maxlength=30 tabindex=1000 value=\"" + rs.getString(5) + "\"></td>";
        if(cols > 5)
          rtn += "<td><p><input type=text name=heading6  size=12 maxlength=30 tabindex=1000 value=\"" + rs.getString(6) + "\"></td>";
        if(cols > 6)
          rtn += "<td><p><input type=text name=heading7  size=12 maxlength=30 tabindex=1000 value=\"" + rs.getString(7) + "\"></td>";
        if(cols > 7)
          rtn += "<td><p><input type=text name=heading8  size=12 maxlength=30 tabindex=1000 value=\"" + rs.getString(8) + "\"></td>";
        if(cols > 8)
          rtn += "<td><p><input type=text name=heading9  size=12 maxlength=30 tabindex=1000 value=\"" + rs.getString(9) + "\"></td>";
        if(cols > 9)
          rtn += "<td><p><input type=text name=heading10 size=12 maxlength=30 tabindex=1000 value=\"" + rs.getString(10) + "\"></td>";
      }
       
      rtn += "</tr>";
      
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
    
    return rtn;
  }
  
}
