// =======================================================================================================================================================================================================
// System: ZaraStar cart: fetch line for edit
// Module: ProductCartFetchLineEdit.java
// Author: C.K.Harvey
// Copyright (c) 2006-07 Christopher Harvey. All Rights Reserved.
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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Enumeration;

public class ProductCartFetchLineEdit extends HttpServlet
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

    String unm="", sid="", uty="", dnm="", line="", cartTable="";

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
          line = value[0];
        else
        if(elementName.equals("p2"))
          cartTable = value[0];
      }

      doIt(req, res, unm, sid, uty, dnm, line, cartTable, bytesOut);
    }    
    catch(Exception e)
    {
      System.out.println(("Unexpected System Error: 121c: " + e));
      res.getWriter().write("Unexpected System Error: 121c");
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(HttpServletRequest req, HttpServletResponse res, String unm, String sid, String uty, String dnm, String line, String cartTable,
                    int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs = null;

    String[] entry    = new String[1]; entry[0]="";
    String[] itemCode = new String[1]; itemCode[0]="";
    String[] mfr      = new String[1]; mfr[0]="";
    String[] mfrCode  = new String[1]; mfrCode[0]="";
    String[] desc     = new String[1]; desc[0]="";
    String[] qty      = new String[1]; qty[0]="";
    String[] uom      = new String[1]; uom[0]="";
    String[] price    = new String[1]; price[0]="";
    String[] currency = new String[1]; currency[0]="";
    
    String rtn = "Unexpected System Error: 121c";
    
    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      rtn = "Access Denied - Duplicate SignOn or Session Timeout";
    }
    else
    {
      if(fetch(dnm, cartTable, line, entry, itemCode, mfr, mfrCode, desc, qty, uom, price, currency, localDefnsDir, defnsDir))
        rtn = ".";
    }
    
    res.setContentType("text/xml");
    res.setHeader("Cache-Control", "no-cache");

    if(entry[0].length() == 0)
      entry[0] = line;
    
    if(itemCode[0].length() == 0)
      itemCode[0] = "-";
    
    if(mfr[0].length() == 0)
      mfr[0] = "-";
   
    if(mfrCode[0].length() == 0)
      mfrCode[0] = "-";
   
    if(desc[0].length() == 0)
      desc[0] = "-";
   
    if(qty[0].length() == 0)
      qty[0] = "1";
    else qty[0] = generalUtils.doubleDPs(qty[0], miscDefinitions.dpOnQuantities(con, stmt, rs, "5"));
   
    if(uom[0].length() == 0)
      uom[0] = "Each";
   
    if(price[0].length() == 0)
      price[0] = "poa";
    else price[0] = generalUtils.doubleDPs(price[0], '2');
   
    if(currency[0].length() == 0)
      currency[0] = "-";
   
    String s = "<msg><res>" + rtn + "</res><line>" + line + "</line><entry>" + entry[0] + "</entry><itemCode>" + itemCode[0] + "</itemCode><mfr>"
             + mfr[0] + "</mfr><mfrCode>" + mfrCode[0] + "</mfrCode><desc>" + desc[0] + "</desc><qty>" + qty[0] + "</qty><uom>" + uom[0]
             + "</uom><price>" + price[0] + "</price><currency>" + currency[0] + "</currency></msg>";
    res.getWriter().write(s);
    
    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 121, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), line);
    if(con != null) con.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // rtns: ' ' if rec added/updated ok
  //       'E' if adding a rec that already exists
  //       'X' other error
  private boolean fetch(String dnm, String cartTable, String line, String[] entry, String[] itemCode, String[] mfr, String[] mfrCode, String[] desc,
                        String[] qty, String[] uom, String[] price, String[] currency, String localDefnsDir, String defnsDir) throws Exception
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
  
      rs = stmt.executeQuery("SELECT Entry, ItemCode, Mfr, MfrCode, Description, Quantity, UoM, Price, Currency FROM " + cartTable + " WHERE Line = '"
                             + line + "'"); 

      if(rs.next())                  
      {
        entry[0]    = rs.getString(1);
        itemCode[0] = rs.getString(2);
        mfr[0]      = rs.getString(3);
        mfrCode[0]  = rs.getString(4);
        desc[0]     = rs.getString(5);
        qty[0]      = rs.getString(6);
        uom[0]      = rs.getString(7);
        price[0]    = rs.getString(8);
        currency[0] = rs.getString(9);
      } 
                 
      if(rs   != null) rs.close();        
      if(stmt != null) stmt.close();        
      if(con  != null) con.close();
            
      return true;
    }
    catch(Exception e)
    {
      System.out.println("121c: " + e);
      if(rs   != null) rs.close();        
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }
    
    return false;
  }

}


