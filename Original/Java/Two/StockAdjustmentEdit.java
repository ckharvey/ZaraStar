// =======================================================================================================================================================================================================
// System: ZaraStar Product: stock adjustment - fetch for edit
// Module: StockAdjustmentEdit.java
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
import java.io.*;
import java.sql.*;
import java.util.Enumeration;

public class StockAdjustmentEdit extends HttpServlet
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

    String unm="", sid="", uty="", dnm="", code="";

    try
    {
      directoryUtils.setContentHeaders(res);
      res.setHeader("Header", "HTTP/1.0 200 OK");
      res.setStatus(200);

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
        if(name.equals("dnm"))
          dnm = value[0];
        else
        if(name.equals("p1"))
          code = value[0];
      }

      doIt(req, res, unm, sid, uty, dnm, code, bytesOut);
    }    
    catch(Exception e)
    {
      serverUtils.etotalBytes(req, unm, dnm, 3011, bytesOut[0], 0, "ERR:" + code);
      System.out.println(("Unexpected System Error: 3011c: " + e));
      res.getWriter().write("Unexpected System Error: 3011c");
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void doIt(HttpServletRequest req, HttpServletResponse res, String unm, String sid, String uty, String dnm, String code, int[] bytesOut) throws Exception
  {
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
 
    String[] storeFrom = new String[1]; storeFrom[0]="";
    String[] storeTo   = new String[1]; storeTo[0]="";
    String[] date      = new String[1]; date[0]="";
    String[] qty       = new String[1]; qty[0]="";
    String[] soCode    = new String[1]; soCode[0]="";
    String[] poCode    = new String[1]; poCode[0]="";
    String[] remark    = new String[1]; remark[0]="";
    String[] status    = new String[1]; status[0]="";
    String[] signon    = new String[1]; signon[0]="";
    String[] locationFrom = new String[1]; locationFrom[0]="";
    String[] locationTo   = new String[1]; locationTo[0]="";
    
    String rtn="Unexpected System Error: 3011c";
    
    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      rtn = "Access Denied - Duplicate SignOn or Session Timeout";
    }
    else
    {
      if(fetch(dnm, code, storeFrom, storeTo, date, qty, soCode, poCode, remark, status, signon, locationFrom, locationTo))
        rtn = ".";
    }
    
    res.setContentType("text/xml");
    res.setHeader("Cache-Control", "no-cache");
    
    if(remark[0].length() == 0)
      remark[0] = ".";
    
    if(soCode[0] == null || soCode[0].length() == 0)
      soCode[0] = ".";
    
    if(poCode[0] == null || poCode[0].length() == 0)
      poCode[0] = ".";
    
    if(status[0].length() == 0)
      status[0] = "L";

    if(locationFrom[0].length() == 0)
      locationFrom[0] = "Unknown";

    if(locationTo[0].length() == 0)
      locationTo[0] = "Unknown";

    String s = "<msg><res>" + rtn + "</res><code>" + code + "</code><storeFrom><![CDATA[" + storeFrom[0] + "]]></storeFrom><storeTo>"
             + "<![CDATA[" + storeTo[0] + "]]></storeTo><date>" + generalUtils.convertFromYYYYMMDD(date[0]) + "</date><qty>" + qty[0] + "</qty><soCode>" + soCode[0]
             + "</soCode><poCode>" + poCode[0] + "</poCode><status>" + status[0] + "</status><signon><![CDATA[" + signon[0].substring(0, (signon[0].length() - 2)) + "]]></signon><remark><![CDATA[" + remark[0]
             + "]]></remark><locationFrom><![CDATA[" + locationFrom[0] + "]]></locationFrom><locationTo><![CDATA[" + locationTo[0] + "]]></locationTo></msg>";
    res.getWriter().write(s);

    serverUtils.totalBytes(req, unm, dnm, 3011, bytesOut[0], 0, code);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean fetch(String dnm, String code, String[] storeFrom, String[] storeTo, String[] date, String[] qty, String[] soCode, String[] poCode, String[] remark, String[] status, String[] signon, String[] locationFrom, String[] locationTo)
                        throws Exception
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
  
      rs = stmt.executeQuery("SELECT * FROM stocka WHERE AdjustmentCode = '" + code + "'"); 

      if(rs.next())                  
      {
        date[0]      = rs.getString(3);
        qty[0]       = generalUtils.stripTrailingZeroesStr(rs.getString(4));
        signon[0]    = rs.getString(5) + " @ " + rs.getString(9);
        remark[0]    = rs.getString(6);
        storeFrom[0] = rs.getString(7);
        storeTo[0]   = rs.getString(8);
        status[0]    = rs.getString(10);
        soCode[0]    = rs.getString(11);
        poCode[0]    = rs.getString(12);
        locationFrom[0] = rs.getString(13);
        locationTo[0] = rs.getString(14);

        if(locationFrom[0] == null) locationFrom[0] = "";
        if(locationTo[0]   == null) locationTo[0] = "";
      } 

      if(rs   != null) rs.close();        
      if(stmt != null) stmt.close();        
      if(con  != null) con.close();
            
      return true;
    }
    catch(Exception e)
    {
      System.out.println("3011c: " + e);
      if(rs   != null) rs.close();        
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }
    
    return false;
  }

}
