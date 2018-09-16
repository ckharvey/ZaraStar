// =======================================================================================================================================================================================================
// System: ZaraStar Product: Stock Check Reconciliation - update
// Module: StockCheckReconciliationUpdate.java
// Author: C.K.Harvey
// Copyright (c) 1999-2009 Christopher Harvey. All Rights Reserved.
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

public class StockCheckReconciliationUpdate extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  DocumentUtils documentUtils = new DocumentUtils();
  DrawingUtils drawingUtils = new DrawingUtils();
  
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="";
    
    try
    {
      directoryUtils.setContentHeaders(res);
      out = res.getWriter();

      String[] remarks = new String[100];  int remarksLen = 100;
      for(int x=0;x<100;++x) remarks[x] = "";

      String[] checkBoxesOn = new String[100];  int checkBoxesOnLen = 100;
      for(int x=0;x<100;++x) checkBoxesOn[x] = "";

      int remarksCount = 0, checkBoxesOnCount = 0;
      
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
        if(name.length() > 2 && name.substring(1, 3).equals(".x")) ;
        else
        if(name.length() > 2 && name.substring(1, 3).equals(".y")) ;
        else
        {
          if(name.startsWith("i")) // input
          {
            if(remarksCount == remarksLen)
            {
              int z;
              String[] buf = new String[remarksLen + 100];
              for(z=0;z<remarksLen;++z)
                buf[z] = remarks[z];
              remarks = new String[remarksLen + 100];
              for(z=0;z<remarksLen;++z)
                remarks[z] = buf[z];
              remarksLen += 100;
            }
            
            remarks[remarksCount++] = (name.substring(1) + "\001" + value[0]);
          }
          else
          if(name.startsWith("c")) // checkbox
          {
            if(checkBoxesOnCount == checkBoxesOnLen)
            {
              int z;
              String[] buf = new String[checkBoxesOnLen + 100];
              for(z=0;z<checkBoxesOnLen;++z)
                buf[z] = checkBoxesOn[z];
              checkBoxesOn = new String[checkBoxesOnLen + 100];
              for(z=0;z<checkBoxesOnLen;++z)
                checkBoxesOn[z] = buf[z];
              checkBoxesOnLen += 100;
            }
            
            checkBoxesOn[checkBoxesOnCount++] = name.substring(1);
          }
        }
      }
      
      doIt(out, req, remarks, remarksCount, checkBoxesOn, checkBoxesOnCount, unm, sid, uty, men, den, dnm, bnm, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "StockCheckReconciliationUpdate", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3065, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush(); 
    }
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String[] remarks, int remarksCount, String[] checkBoxesOn, int checkBoxesOnCount, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut)
                    throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 3065, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "StockCheckReconciliationUpdate", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3065, bytesOut[0], 0, "ACC:");
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "StockCheckReconciliationUpdate", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.totalBytes(req, unm, dnm, 3065, bytesOut[0], 0, "SID:");
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    int[] hmenuCount = new int[1];

    drawingUtils.outputPageFrame(con, stmt, rs, out, req, "3065", "", "StockCheckReconciliationUpdate", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount, bytesOut);

    drawingUtils.drawTitle(out, false, false, "StockCheckReconciliationUpdate", "", "Stock Check Reconciliation Update", "3065", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
    
    update(con, stmt, remarks, remarksCount, checkBoxesOn, checkBoxesOnCount, unm);
    
    scoutln(out, bytesOut, "<table id='page' border=0 width=100%>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td><p>Completed</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "</table>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 3065, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void update(Connection con, Statement stmt, String[] remarks, int remarksCount, String[] checkBoxesOn, int checkBoxesOnCount, String unm) throws Exception
  {
    int x, y, len;
    String checkCode, remark;
    
    for(x=0;x<checkBoxesOnCount;++x)
    {
      updateStockCheckRecord(con, stmt, checkBoxesOn[x], unm);
    }
      
    for(x=0;x<remarksCount;++x)
    {
      y = 0;
      len = remarks[x].length();
      checkCode = "";
      while(y < len && remarks[x].charAt(y) != '\001') // just-in-case
        checkCode += remarks[x].charAt(y++);
      ++y;
      remark = "";
      while(y < len)
        remark += remarks[x].charAt(y++);
      
      updateStockCheckRecord(con, stmt, checkCode, remark, unm);
    } 
  }
  
  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void updateStockCheckRecord(Connection con, Statement stmt, String checkCode, String unm) throws Exception
  {
    stmt = con.createStatement();
    
    stmt.executeUpdate("UPDATE stockc SET Reconciled = 'Y', SignOn = '" + generalUtils.sanitiseForSQL(unm) + "' WHERE CheckCode = '" + generalUtils.sanitiseForSQL(checkCode) + "'");

    if(stmt != null) stmt.close();
  }
  
  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void updateStockCheckRecord(Connection con, Statement stmt, String checkCode, String remark, String unm) throws Exception
  {
    stmt = con.createStatement();
    
    stmt.executeUpdate("UPDATE stockc SET Remark = '" + generalUtils.sanitiseForSQL(remark) + "', SignOn = '" + generalUtils.sanitiseForSQL(unm) + "' WHERE CheckCode = '" + generalUtils.sanitiseForSQL(checkCode) + "'");

    if(stmt != null) stmt.close();
  }
  
  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
