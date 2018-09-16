// =======================================================================================================================================================================================================
// System: ZaraStar: Product: Replace Stock Records: do it
// Module: ProductStockReplaceExecute.java
// Author: C.K.Harvey
// Copyright (c) 2002-08 Christopher Harvey. All Rights Reserved.
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

public class ProductStockReplaceExecute extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  Inventory inventory = new Inventory();  
  
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="";
    String urlBit="";

    try
    {
      res.setContentType("text/html");
      out = res.getWriter();

      unm = req.getParameter("unm");
      sid = req.getParameter("sid");
      uty = req.getParameter("uty");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");
      p1  = req.getParameter("p1"); // oldItemCode
      p2  = req.getParameter("p2"); // newItemCode
    
      if(p1 == null) p1 = "";
      if(p2 == null) p2 = "";
      
      StringBuffer url = req.getRequestURL();
      int x=0;
      if(url.charAt(x) == 'h' || url.charAt(x) == 'H')
        x += 7;
      urlBit="";
      while(url.charAt(x) != '\000' && url.charAt(x) != ',' && url.charAt(x) != '/')
        urlBit += url.charAt(x++);

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, urlBit, bytesOut);
    }
    catch(Exception e)
    {
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "ProductStockReplaceExecute", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3009, bytesOut[0], 0, "ERR:" + p1 + " " + p2);
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm,
                    String bnm, String p1, String p2, String urlBit, int[] bytesOut) throws Exception
  {
    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    long startTime = new java.util.Date().getTime();

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
      
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 3009, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "ProductStockReplaceExecute", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3009, bytesOut[0], 0, "ACC:" + p1 + " " + p2);
      
      if(con != null) con.close();
     if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "ProductStockReplaceExecute", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3009, bytesOut[0], 0, "SID:" + p1 + " " + p2);
       
      if(con != null) con.close();
     if(out != null) out.flush(); 
      return;
    }
    
    scoutln(out, bytesOut, "<html><head><title>Replace Stock Records</title>");

    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\""
                         + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\"></head>");

    int[] hmenuCount = new int[1];
 
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "3009", "", "ProductStockReplace", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount,
                          bytesOut);
                                
    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Replace Stock Records", "3009", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    String oldItemCode = p1.toUpperCase();
    String newItemCode = p2.toUpperCase();

    if(oldItemCode.length() == 0 || newItemCode.length() == 0)
    {
       messagePage.msgScreen(false, out, req, 0, unm, sid, uty, men, den, dnm, bnm, "3009", "", "", "Please supply both an existing code and a new code.", imagesDir, localDefnsDir, defnsDir, bytesOut);
    }
    else
    {
      boolean ok;
      if(newItemCode.equals("-"))
        ok = true;
      else
      if(! inventory.existsItemRecGivenCode(con, stmt, rs, newItemCode))
      {
        messagePage.msgScreen(false, out, req, 0, unm, sid, uty, men, den, dnm, bnm, "3009", "", "", "The new item must exist on the stock database first. Please create it.", imagesDir, localDefnsDir, defnsDir, bytesOut);
        ok = false;
      }
      else ok = true;

      if(ok)
      {
        process(con, stmt, "quotel",    oldItemCode, newItemCode);
        process(con, stmt, "sol",       oldItemCode, newItemCode);
        process(con, stmt, "wol",       oldItemCode, newItemCode);
        process(con, stmt, "ocl",       oldItemCode, newItemCode);
        process(con, stmt, "pll",       oldItemCode, newItemCode);
        process(con, stmt, "dol",       oldItemCode, newItemCode);
        process(con, stmt, "invoicel",  oldItemCode, newItemCode);
        process(con, stmt, "debitl",    oldItemCode, newItemCode);
        process(con, stmt, "creditl",   oldItemCode, newItemCode);
        process(con, stmt, "proformal", oldItemCode, newItemCode);

        process(con, stmt, "pol",       oldItemCode, newItemCode);
        process(con, stmt, "lpl",       oldItemCode, newItemCode);
        process(con, stmt, "grl",       oldItemCode, newItemCode);
        process(con, stmt, "pinvoicel", oldItemCode, newItemCode);
        process(con, stmt, "pdebitl",   oldItemCode, newItemCode);
        process(con, stmt, "pcreditl",  oldItemCode, newItemCode);

        process(con, stmt, "stocka",    oldItemCode, newItemCode);
        process(con, stmt, "stockc",    oldItemCode, newItemCode);

        try
        {
          process(con, stmt, "buyerscatalog", oldItemCode, newItemCode);
        }
        catch(Exception e) { } // for the case where there are two entries in a catalog (one for the old item and one for the new, and renumbering
                               // would 'create' two sams itemcodes in that catalog

        // now remove old rec from stock file (also removes any stockx recs)
        inventory.stockDeleteRec(con, stmt, rs, oldItemCode, dnm, localDefnsDir, defnsDir);

        messagePage.msgScreen(false, out, req, 21, unm, sid, uty, men, den, dnm, bnm, "ProductStockReplaceExecute", imagesDir, localDefnsDir, defnsDir, bytesOut);
      }
    }

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "</table></form>");     scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
    
    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 3009, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), oldItemCode +  " " + newItemCode);
      
      if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void process(Connection con, Statement stmt, String tableName, String oldItemCode, String newItemCode) throws Exception
  {
    stmt = con.createStatement();
    
    stmt.executeUpdate("UPDATE " + tableName + " SET ItemCode = '" + newItemCode + "' WHERE ItemCode = '" + generalUtils.sanitiseForSQL(oldItemCode) + "'");

    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
