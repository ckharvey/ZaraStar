// =======================================================================================================================================================================================================
// System: ZaraStar ProductEngine: Create storex edit page
// Module: ProductStockxCreate.java
// Author: C.K.Harvey
// Copyright (c) 2001-06 Christopher Harvey. All Rights Reserved.
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

public class ProductStockxCreate extends HttpServlet
{
  Inventory inventory = new Inventory();
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
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="", p3="", p4="", p5="";

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
      p1  = req.getParameter("p1"); // code
      p2  = req.getParameter("p2"); // store
      p3  = req.getParameter("p3"); // cad
      p4  = req.getParameter("p4"); // errStr
      p5  = req.getParameter("p5"); // dataAlready

      if(p4 == null) p4 = "";
      if(p5 == null) p5 = "";

      doIt(out, req, p1, p2, p3, p4, p5, unm, sid, uty, men, den, dnm, bnm, bytesOut);
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
 
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "ProductStockxCreate", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3006, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String code, String store, String cad, String errStr,
                      String dataAlready, String unm, String sid, String uty, String men, String den, String dnm,
                      String bnm, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 3006, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "ProductStockxCreate", imagesDir, localDefnsDir,
                         defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3006, bytesOut[0], 0, "ACC:" + code);
    if(con  != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "ProductStockxCreate", imagesDir, localDefnsDir,
                         defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3006, bytesOut[0], 0, "SID:" + code);
    if(con  != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    byte[] codeB = new byte[21];
    generalUtils.stringToBytes(code, 0, codeB);

    byte[] storeB = new byte[21];
    generalUtils.stringToBytes(store, 0, storeB);

    byte[] dataAlreadyB = new byte[2000];
    generalUtils.stringToBytes(dataAlready, 0, dataAlreadyB);

   inventory.stockxGetRecToHTML(con, stmt, rs, out, req, unm, sid, uty, men, den, dnm, bnm, cad.charAt(0), 'E', codeB, storeB, localDefnsDir, defnsDir, errStr, dataAlreadyB, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 3036, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), code);
    if(con  != null) con.close();
    if(out != null) out.flush(); 
  }

}
