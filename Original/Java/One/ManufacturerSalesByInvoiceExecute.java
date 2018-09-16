// =======================================================================================================================================================================================================
// System: ZaraStar Analytic: Manufacturer Sales from Invoice Lines
// Module: ManufacturerSalesByInvoiceExecute.java
// Author: C.K.Harvey
// Copyright (c) 1999-2007 Christopher Harvey. All Rights Reserved.
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
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.io.*;

public class ManufacturerSalesByInvoiceExecute extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  AccountsUtils accountsUtils = new AccountsUtils();
  
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="";

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
      p1  = req.getParameter("p1"); // effectiveDate

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, bytesOut);
    }    
    catch(Exception e)
    {
      StringBuffer url = req.getRequestURL();
      String urlBit="";
      int x=0;
      if(url.charAt(x) == 'h' || url.charAt(x) == 'H')
        x += 7;
      while(url.charAt(x) != '\000' && url.charAt(x) != ',' && url.charAt(x) != '/')
        urlBit += url.charAt(x++);

      System.out.println(e);
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "ManufacturerSalesByInvoiceExecute", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1038, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm,
                    String bnm, String p1, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String localDefnsDir  = directoryUtils.getLocalOverrideDir(dnm);
    String defnsDir          = directoryUtils.getSupportDirs('D');
    String imagesDir         = directoryUtils.getSupportDirs('I');
    
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;
    
    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 1038, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "ManufacturerSalesByInvoiceInput", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1038, bytesOut[0], 0, "ACC:" + p1);
    if(con  != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "ManufacturerSalesByInvoiceInput", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1038, bytesOut[0], 0, "SID:" + p1);
    if(con  != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    process(con, stmt, rs, out, req, p1, unm, sid, uty, men, den, dnm, bnm, imagesDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 1038, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con  != null) con.close();
    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void process(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String p1, String unm, String sid, String uty, String men, String den,
                       String dnm, String bnm, String imagesDir, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Manufacturer Sales from Invoice Lines</title>");

    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir)
                         + "general.css\"></head>");
  
    int[] hmenuCount = new int[1];
    
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "1038", "", "ManufacturerSalesByInvoiceInput", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount,
                          bytesOut);
                                
    scoutln(out, bytesOut, "<form>");
  
    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Manufacturer Sales from Invoice Lines", "1038", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    scout(out, bytesOut, "<p>For Upto Effective Date: " + p1);
    scout(out, bytesOut, "<p>Consolidated into " + accountsUtils.getBaseCurrency(con, stmt, rs, dnm, localDefnsDir, defnsDir));
    
    scoutln(out, bytesOut, "<br><table id=\"page\" width=100%>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr id=\"pageColumn\">");
    scoutln(out, bytesOut, "<td></td>");
    scoutln(out, bytesOut, "<td align=center colspan=2><p> Last 30 </td>");
    scoutln(out, bytesOut, "<td align=center colspan=2><p> Last 60 </td>");
    scoutln(out, bytesOut, "<td align=center colspan=2><p> Last 90 </td>");
    scoutln(out, bytesOut, "<td align=center colspan=2><p> Last 180 </td>");
    scoutln(out, bytesOut, "<td align=center colspan=2><p> One Year </td></tr>");

    scoutln(out, bytesOut, "<tr id=\"pageColumn\">");
    scoutln(out, bytesOut, "<td><p> Manufacturer </td>");
    scoutln(out, bytesOut, "<td align=center><p>Value</td><td align=center><p>%</td>");
    scoutln(out, bytesOut, "<td align=center><p>Value</td><td align=center><p>%</td>");
    scoutln(out, bytesOut, "<td align=center><p>Value</td><td align=center><p>%</td>");
    scoutln(out, bytesOut, "<td align=center><p>Value</td><td align=center><p>%</td>");
    scoutln(out, bytesOut, "<td align=center><p>Value</td><td align=center><p>%</td></tr>");

    String effectiveDate;
    if(p1 == null || p1.length() == 0)
      effectiveDate = generalUtils.todaySQLFormat(localDefnsDir, defnsDir);
    else effectiveDate = generalUtils.convertDateToSQLFormat(p1);
    
    int dateLess30  = generalUtils.encodeFromYYYYMMDD(effectiveDate) - 30;
    int dateLess60  = dateLess30 - 30;
    int dateLess90  = dateLess30 - 60;
    int dateLess180 = dateLess30 - 150;
    int dateLess365 = dateLess30 - 335;
    
    String effectiveDateLessOneYear = generalUtils.decodeToYYYYMMDD(dateLess365);

    String[][] mfrCodes = new String[1][10];
    int[] mfrCodesSize  = new int[1]; mfrCodesSize[0] = 10;
    int[] numMfrCodes   = new int[1]; numMfrCodes[0]  = 0;
    
    double[][] totals = new double[1][50]; 
    double[] totalValues = new double[5];
    double percentage;
    
    int x, y, z;
    for(x=0;x<50;++x)
      totals[0][x] = 0.0;
    
    for(x=0;x<5;++x)
    {
      totalValues[x] = 0;
    }
    
    calculateInvoices(con, stmt, rs, effectiveDate, effectiveDateLessOneYear, dateLess30, dateLess60, dateLess90, dateLess180, dateLess365,
                      mfrCodes, numMfrCodes, mfrCodesSize, totals, localDefnsDir, defnsDir);

    for(x=0;x<numMfrCodes[0];++x)
    {
      z = (x * 5);
      for(y=0;y<5;++y)
        totalValues[y] += totals[0][z + y];
    }

    for(x=0;x<numMfrCodes[0];++x)
    {
      scoutln(out, bytesOut, "<tr><td><p>" + mfrCodes[0][x] + "</td>");
      
      z = (x * 5);
      for(y=0;y<5;++y)
      {        
        scoutln(out, bytesOut, "<td align=right><p>" +generalUtils.formatNumeric(totals[0][z + y], '2') + "</td>");
        if(totalValues[y] == 0.0)
          percentage = 0.0;
        else percentage = ((totals[0][z + y] / totalValues[y]) * 100);
        scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.doubleDPs('2', percentage) + "%</td>");       
      }
      
      scoutln(out, bytesOut, "</tr>");
    }

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td><p><b>TOTAL</td>");

    for(y=0;y<5;++y)
    {  
      scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(totalValues[y], '2') + "</td>");
      scoutln(out, bytesOut, "<td align=right><p>100%</td>");       
    }

    scoutln(out, bytesOut, "</table></form>");     scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void calculateInvoices(Connection con, Statement stmt, ResultSet rs, String effectiveDate, String effectiveDateLessOneYear, int dateLess30,
                                 int dateLess60, int dateLess90, int dateLess180, int dateLess365, String[][] mfrCodes, int[] numMfrCodes,
                                 int[] mfrCodesSize, double[][] totals, String localDefnsDir, String defnsDir) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.Manufacturer, t1.Date, t2.Amount FROM invoice AS t1 INNER JOIN invoicel AS t2 "
                         + "ON t1.InvoiceCode = t2.InvoiceCode WHERE t1.Date <= {d '" + effectiveDate + "'} AND t1.Date > {d '"
                         + effectiveDateLessOneYear + "'} AND t1.Status != 'C'");

    double thisAmount;
    String thisMfrCode;
    int thisDateEncoded;

    while(rs.next())
    {
      thisMfrCode     = rs.getString(1);

      if(thisMfrCode == null || thisMfrCode.length() == 0 || thisMfrCode.equals("<none>"))
        thisMfrCode = "-";
      
      thisDateEncoded = generalUtils.encodeFromYYYYMMDD(rs.getString(2));
      thisAmount      = generalUtils.doubleFromStr(generalUtils.doubleDPs(rs.getString(3), '2'));
   
      addToDataSoFar(thisMfrCode, thisDateEncoded, thisAmount, dateLess30, dateLess60, dateLess90, dateLess180, dateLess365, mfrCodes, numMfrCodes,
                     mfrCodesSize, totals);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void addToDataSoFar(String thisMfrCode, int thisDateEncoded, double thisAmount, int dateLess30, int dateLess60, int dateLess90,
                              int dateLess180, int dateLess365, String[][] mfrCodes, int[] numMfrCodes, int[] mfrCodesSize, double[][] totals)
                              throws Exception
  {
    int x=0;
    boolean found = false;
    while(x < numMfrCodes[0] && ! found)
    {
      if(mfrCodes[0][x].equals(thisMfrCode))
        found = true;
      else ++x;
    }
   
    if(! found)
    {
      if((numMfrCodes[0] + 1) == mfrCodesSize[0])
      {
        int z;
       
        String[] buf = new String[mfrCodesSize[0] + 10]; // add room for 10 more mfrs
        for(z=0;z<mfrCodesSize[0];++z)
          buf[z] = mfrCodes[0][z];
        mfrCodes[0] = buf;

        int upto = (mfrCodesSize[0] * 5);

        double[] dbuf = new double[(mfrCodesSize[0] * 5) + 50];
        for(z=0;z<upto;++z)
          dbuf[z] = totals[0][z];
        for(z=upto;z<(upto+50);++z)
          dbuf[z] = 0.0;
        totals[0] = dbuf;

        mfrCodesSize[0] += 10;
      }

      x = numMfrCodes[0];

      ++numMfrCodes[0];
     
      mfrCodes[0][x] = thisMfrCode;
    }
    
    x *= 5;

    if(thisDateEncoded >= dateLess30)
      totals[0][x] +=  thisAmount;

    ++x;
    if(thisDateEncoded >= dateLess60)
      totals[0][x] += thisAmount;

    ++x;
    if(thisDateEncoded >= dateLess90)
      totals[0][x] += thisAmount;

    ++x;
    if(thisDateEncoded >= dateLess180)
      totals[0][x] += thisAmount;

    ++x;
    if(thisDateEncoded >= dateLess365)
      totals[0][x] += thisAmount;
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void scout(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.print(str);
    bytesOut[0] += str.length();    
  }
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
