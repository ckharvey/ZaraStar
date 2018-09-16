// =======================================================================================================================================================================================================
// System: ZaraStar: Stock: Provide stock item details
// Module: StockProvideStockItemDetails.java
// Author: C.K.Harvey
// Copyright (c) 2000-08 Christopher Harvey. All Rights Reserved.
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

public class StockProvideStockItemDetails extends HttpServlet
{
  GeneralUtils  generalUtils = new GeneralUtils();
  MessagePage  messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils  directoryUtils = new DirectoryUtils();
  Inventory  inventory = new Inventory();
  Profile  profile = new Profile();
  Customer customer = new Customer();
  ProductCart   productCart  = new ProductCart();
  AdminControlUtils adminControlUtils = new AdminControlUtils();
  
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p9="", p14="", p15="";

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
      p1  = req.getParameter("p1"); // itemCode
      p9  = req.getParameter("p9"); // remoteSID
      p14  = req.getParameter("p14"); // userType
      p15  = req.getParameter("p15"); // userName
    
      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p9, p14, p15, bytesOut);
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

      System.out.println("121f: " + e);
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "StockProvideStockItemDetails", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 121, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                    String p1, String p9, String p14, String p15, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;
    
    if((uty.equals("R") && ! adminControlUtils.notDisabled(con, stmt, rs, 908)) || (uty.equals("A") && ! adminControlUtils.notDisabled(con, stmt, rs, 808)) || (uty.equals("I") && ! authenticationUtils.verifyAccess(con, stmt, rs, req, 121, unm, uty, dnm, localDefnsDir, defnsDir)))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "ProductCart", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 121, bytesOut[0], 0, "ACC:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(p14.equals("R"))
    {
    }
    else          
    {
      if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
      {
        messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "ProductCart", imagesDir, localDefnsDir, defnsDir, bytesOut);
        serverUtils.etotalBytes(req, unm, dnm, 121, bytesOut[0], 0, "SID:" + p1);
        if(con != null) con.close();
        if(out != null) out.flush(); 
        return;
      }
    }

    String[] mfr     = new String[1];
    String[] mfrCode = new String[1];
    String[] descs   = new String[1];
    String[] uom     = new String[1];

    getDetails(con, stmt, rs, p1, descs, mfr, mfrCode, uom);
    
    out.println(descs[0] + "\001" + mfr[0] + "\001" + mfrCode[0] + "\001" + uom[0] + "\001");
    
    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 121, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void getDetails(Connection con, Statement stmt, ResultSet rs, String itemCode, String[] descs, String[] mfr, String[] mfrCode, String[] uom)
                          throws Exception
  {
    descs[0] = mfr[0] = mfrCode[0] = "";
    uom[0] = "Each";

    if(itemCode.length() == 0) // just-in-case
      return;
    
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT Description, Description2, Manufacturer, ManufacturerCode, UoM FROM stock WHERE ItemCode = '"
                           + generalUtils.sanitiseForSQL(itemCode) + "'");
    
    if(rs.next())
    {
      descs[0]   = rs.getString(1) + ": " + rs.getString(2);
      mfr[0]     = rs.getString(3);
      mfrCode[0] = rs.getString(4);
      uom[0]     = rs.getString(5);
    }

    if(descs[0].length() == 0)
      descs[0] = "-";
    else
    if(descs[0].length() > 80)
      descs[0] = descs[0].substring(0, 80);

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

}
