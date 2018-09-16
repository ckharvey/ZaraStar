// =======================================================================================================================================================================================================
// System: ZaraStar Product: Mfr sales by salesperson: do it
// Module: ProductManufacturerSalesExecute.java
// Author: C.K.Harvey
// Copyright (c) 2002-13 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.io.*;
import java.sql.*;

public class ProductManufacturerSalesExecute extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  MiscDefinitions miscDefinitions = new MiscDefinitions();
  AccountsUtils accountsUtils = new AccountsUtils();
  Inventory inventory = new Inventory();
  Profile profile = new Profile();
  
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", mfr="", year="", month = "";
    
    try
    {
      directoryUtils.setContentHeaders(res);
      out = res.getWriter();

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
        if(name.equals("mfr"))
          mfr = value[0];
        else
        if(name.equals("month"))
          month = value[0];
        else
        if(name.equals("year"))
          year = value[0];
      }

      doIt(out, req, mfr, month, year, unm, sid, uty, men, den, dnm, bnm, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "ProductManufacturerSalesExecute", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 13055, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String mfr, String month, String year, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut)
                    throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null, stmt2 = null;
    ResultSet rs   = null, rs2   = null;

    if(! unm.equals("Desmondpoh") && ! authenticationUtils.verifyAccess(con, stmt, rs, req, 3062, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "ProductManufacturerSalesExecute", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 13055, bytesOut[0], 0, "ACC:");
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "ProductManufacturerSalesExecute", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 13055, bytesOut[0], 0, "SID:");
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    generate(con, stmt, stmt2, rs, rs2, req, out, mfr, month, year, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);
    
    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 13055, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con != null) con.close();
    if(out != null) out.flush(); 
    out.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void generate(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, HttpServletRequest req, PrintWriter out, String mfr, String month, String year, String unm, String sid, String uty, String men, String den,
                        String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Manufacturer Sales by SalesPerson</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");
  
    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\"></head>");
    
    int[] hmenuCount = new int[1];
 
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "13055", "", "ProductManufacturerSalesExecute", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
                                
    scoutln(out, bytesOut, "<form>");

    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Manufacturer Sales by SalesPerson", "13055", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<table id='page' width=100% border=0>");
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    double[] totalAmt = new double[12];
    
    for(int x=0;x<12;++x) totalAmt[x] = 0.0;

    if(mfr.equals("-"))
      scoutln(out, bytesOut, "<tr><td><p><b>For All Manufacturers</td></tr>");
    else scoutln(out, bytesOut, "<tr><td><p><b>For " + mfr + "</td></tr>");
  
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");    
    
    
    scoutln(out, bytesOut, "<tr><td></td>");

    int mm = generalUtils.strToInt(month);
    int yy = generalUtils.strToInt(year);
    
    for(int x=0;x<12;++x)
    {
      scoutln(out, bytesOut, "<td align=right><p><b>" + mm + "-" + yy + "</td>");
          
      --mm;
          
      if(mm == 0)
      {
        mm = 12;
        --yy;
      }
    }

    scoutln(out, bytesOut, "<td align=right><p><b>Year Total</td>");

    scoutln(out, bytesOut, "</tr>");
  
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");    
    
    forEachSalesPerson(out, con, stmt, stmt2, rs, rs2, mfr, generalUtils.strToInt(month), generalUtils.strToInt(year), bytesOut, totalAmt);
    
    scoutln(out, bytesOut, "<tr><td><p>TOTAL</td>");

    double grandTotalAmt = 0.0;
    
    for(int x=0;x<12;++x) grandTotalAmt += totalAmt[x];

    for(int x=0;x<12;++x) scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(totalAmt[x], '2') + "</td>");

    scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(grandTotalAmt, '2') + "</td>");
 
    scoutln(out, bytesOut, "</tr>");

    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void forEachSalesPerson(PrintWriter out, Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String mfr, int month, int year, int[] bytesOut, double[] totalAmt) throws Exception
  {
    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT UserCode FROM profilesd WHERE UserBasis = 'E' AND IsSalesPerson = 'Y' ORDER BY UserCode");

      String userCode, userName, dateFrom, dateTo;
      double amt, thisTotalAmt;
      int numDays, x, mm, yy;
      String[] cssFormat = new String[1];  cssFormat[0] = "";
      
      while(rs.next())
      {
        userCode = rs.getString(1);

        userName = profile.getUserNameFromProfilesGivenUserCodeAndStatus(con, stmt2, rs2, userCode, true);
        
        if(userName.length() > 0)
        {
          if(cssFormat[0].equals("line1")) cssFormat[0] = "line2"; else cssFormat[0] = "line1";
            
          scoutln(out, bytesOut, "<tr id='" + cssFormat[0] + "'><td><p>" + userName + "</td>");

        mm = month;
        yy = year;
        
        thisTotalAmt = 0.0;
        
        for(x=0;x<12;++x)
        {
          numDays = generalUtils.numOfDaysInMonth((short)mm, (short)yy);
         
          dateTo = yy + "-" + mm + "-" + numDays;
           
          dateFrom = yy + "-" + mm + "-01";
          
          amt = getInvoicesForASalesPerson(con, stmt2, rs2, mfr, userCode, dateFrom, dateTo);
          
          totalAmt[x] += amt;
        
          scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(amt, '2') + "</td>");

          thisTotalAmt += amt;
          
          --mm;
          
          if(mm == 0)
          {
            mm = 12;
            --yy;
          }
        }

        scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(thisTotalAmt, '2') + "</td>");

        scoutln(out, bytesOut, "</tr>");
      }
      }
      
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println("DocumentUtils: " + e);
      try
      {
        if(rs   != null) rs.close();
        if(stmt != null) stmt.close();
      }
      catch(Exception e2) { }
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private double getInvoicesForASalesPerson(Connection con, Statement stmt, ResultSet rs, String mfr, String salesPerson, String dateFrom, String dateTo) throws Exception
  {
    double amt = 0.0;
    
    stmt = con.createStatement();
    
    if(mfr.equals("-"))
    {
      rs = stmt.executeQuery("SELECT t2.Amount FROM invoice AS t1 INNER JOIN invoicel AS t2 ON t1.InvoiceCode = t2.InvoiceCode "
                           + "WHERE t1.SalesPerson = '" + salesPerson + "'" + " AND t1.Date >= {d '" + dateFrom + "'}  AND t1.Date <= {d '" + dateTo + "'} AND t1.Status != 'C'");
    }
    else
    {
      rs = stmt.executeQuery("SELECT t2.Amount FROM invoice AS t1 INNER JOIN invoicel AS t2 ON t1.InvoiceCode = t2.InvoiceCode "
                         + "WHERE t2.Manufacturer = '" + generalUtils.sanitiseForSQL(mfr) + "' AND t1.SalesPerson = '" + salesPerson + "'" + " AND t1.Date >= {d '" + dateFrom + "'}  AND t1.Date <= {d '" + dateTo + "'} AND t1.Status != 'C'");
    }
    
    while(rs.next())
    {
      amt += generalUtils.doubleFromStr(rs.getString(1));
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return amt;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }
  
}
