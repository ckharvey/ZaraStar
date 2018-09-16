// =======================================================================================================================================================================================================
// System: ZaraStar Analytic: Manufacturer Sales from Invoice Lines by Customer
// Module: ManufacturerSalesGenerate.java
// Author: C.K.Harvey
// Copyright (c) 1999-2008 Christopher Harvey. All Rights Reserved.
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
import java.io.*;

public class ManufacturerSalesGenerate extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  AccountsUtils accountsUtils = new AccountsUtils();
  DashboardUtils dashboardUtils = new DashboardUtils();
  Customer customer = new Customer();
  
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="", p3="";

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
      p2  = req.getParameter("p2"); // mfr
      p3  = req.getParameter("p3"); // plain or not
      
      if(p3 == null) p3 = "";

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, p3, bytesOut);
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
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "ManufacturerSalesGenerate", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1022, bytesOut[0], 0, "ERR:" + p1+":"+p2);
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm,
                    String bnm, String p1, String p2, String p3, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String localDefnsDir  = directoryUtils.getLocalOverrideDir(dnm);
    String defnsDir          = directoryUtils.getSupportDirs('D');
    String imagesDir         = directoryUtils.getSupportDirs('I');
    
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;
    
    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 1022, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "ManufacturerSalesInput", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1022, bytesOut[0], 0, "ACC:" + p1+":"+p2);
    if(con  != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "ManufacturerSalesInput", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1022, bytesOut[0], 0, "SID:" + p1+":"+p2);
    if(con  != null) con.close();
      if(out != null) out.flush(); 
      return;
    }
    
    boolean plain = false;
    if(p3.equals("P"))
      plain = true;

    process(con, stmt, rs, out, req, p1, p2, plain, unm, sid, uty, men, den, dnm, bnm, imagesDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 1022, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1+":"+p2);
    if(con  != null) con.close();
    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void process(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String p1, String p2, boolean plain, String unm, String sid, String uty, String men,
                       String den, String dnm, String bnm, String imagesDir, String localDefnsDir, String defnsDir, int[] bytesOut)
                       throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Manufacturer Sales from Invoice Lines by Customer</title>");

    if(plain)
    {
      scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"print\" href=\""
                             + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "generalPlain.css\">");
    }
    else
    {
      scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\""
                             + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");
    }
  
    outputPageFrame(con, stmt, rs, out, req, plain, p1, p2, "", "Manufacturer Sales from Invoice Lines by Customer", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, bytesOut);

    scoutln(out, bytesOut, "<form>");
  
    scout(out, bytesOut, "<p>For: " + p2);
    
    scout(out, bytesOut, "<p>For Upto Effective Date: " + p1);
    scout(out, bytesOut, "<p>Consolidated into " + accountsUtils.getBaseCurrency(con, stmt, rs, dnm, localDefnsDir, defnsDir));
    
    scoutln(out, bytesOut, "<br><table id=\"page\" width=100%>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr id=\"pageColumn\">");
    scoutln(out, bytesOut, "<td colspan=3></td>");
    scoutln(out, bytesOut, "<td align=center colspan=2><p> Last 30 </td>");
    scoutln(out, bytesOut, "<td align=center colspan=2><p> Last 60 </td>");
    scoutln(out, bytesOut, "<td align=center colspan=2><p> Last 90 </td>");
    scoutln(out, bytesOut, "<td align=center colspan=2><p> Last 180 </td>");
    scoutln(out, bytesOut, "<td align=center colspan=2><p> One Year </td></tr>");

    scoutln(out, bytesOut, "<tr id=\"pageColumn\">");
    scoutln(out, bytesOut, "<td colspan=2><p> Customer </td>");
    scoutln(out, bytesOut, "<td><p> Industry </td>");
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

    String[][] companyCodes = new String[1][10];
    int[] companyCodesSize  = new int[1]; companyCodesSize[0] = 10;
    int[] numCompanyCodes   = new int[1]; numCompanyCodes[0]  = 0;
    
    double[][] totals = new double[1][50]; 
    double[] totalValues = new double[5];
    double percentage;
    
    int x, y, z;
    for(x=0;x<50;++x)
      totals[0][x] = 0.0;
    
    for(x=0;x<5;++x)
      totalValues[x] = 0;
    
    calculateInvoices(con, stmt, rs, p2, effectiveDate, effectiveDateLessOneYear, dateLess30, dateLess60, dateLess90, dateLess180, dateLess365,
                      companyCodes, numCompanyCodes, companyCodesSize, totals, localDefnsDir, defnsDir);

    for(x=0;x<numCompanyCodes[0];++x)
    {
      z = (x * 5);
      for(y=0;y<5;++y)
        totalValues[y] += totals[0][z + y];
    }

    for(x=0;x<numCompanyCodes[0];++x)
    {
      scoutln(out, bytesOut, "<tr><td><p>" + companyCodes[0][x] + "</td>");
      scoutln(out, bytesOut, "<td><p>" + customer.getCompanyNameGivenCode(con, stmt, rs, companyCodes[0][x]) + "</td>");
      scoutln(out, bytesOut, "<td><p>" + customer.getACompanyFieldGivenCode(con, stmt, rs, "IndustryType", companyCodes[0][x]) + "</td>");

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
    scoutln(out, bytesOut, "<tr><td colspan=3><p><b>TOTAL (" + numCompanyCodes[0] + " customers)</td>");

    for(y=0;y<5;++y)
    {  
      scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.formatNumeric(totalValues[y], '2') + "</td>");
      scoutln(out, bytesOut, "<td align=right><p>100%</td>");       
    }

    scoutln(out, bytesOut, "</table></form>");     scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void calculateInvoices(Connection con, Statement stmt, ResultSet rs, String mfr, String effectiveDate, String effectiveDateLessOneYear,
                                 int dateLess30, int dateLess60, int dateLess90, int dateLess180, int dateLess365, String[][] companyCodes,
                                 int[] numCompanyCodes, int[] companyCodesSize, double[][] totals, String localDefnsDir, String defnsDir)
                                 throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t1.CompanyCode, t1.Date, t2.Amount FROM invoice AS t1 INNER JOIN invoicel AS t2 "
                         + "ON t1.InvoiceCode = t2.InvoiceCode WHERE t2.Manufacturer = '" + generalUtils.sanitiseForSQL(mfr) + "' AND t1.Date <= {d '"
//                         + "ON t1.InvoiceCode = t2.InvoiceCode WHERE t1.Date <= {d '"
                         + effectiveDate + "'} AND t1.Date > {d '" + effectiveDateLessOneYear + "'} AND t1.Status != 'C'");

    double thisAmount;
    String thisCompanyCode;
    int thisDateEncoded;

    while(rs.next())
    {
      thisCompanyCode = rs.getString(1);

      if(thisCompanyCode == null || thisCompanyCode.length() == 0 || thisCompanyCode.equals("<none>"))
        thisCompanyCode = "-";
      
      thisDateEncoded = generalUtils.encodeFromYYYYMMDD(rs.getString(2));
      thisAmount      = generalUtils.doubleFromStr(generalUtils.doubleDPs(rs.getString(3), '2'));
   
      addToDataSoFar(thisCompanyCode, thisDateEncoded, thisAmount, dateLess30, dateLess60, dateLess90, dateLess180, dateLess365, companyCodes,
                     numCompanyCodes, companyCodesSize, totals);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void addToDataSoFar(String thisCompanyCode, int thisDateEncoded, double thisAmount, int dateLess30, int dateLess60, int dateLess90,
                              int dateLess180, int dateLess365, String[][] companyCodes, int[] numCompanyCodes, int[] companyCodesSize,
                              double[][] totals) throws Exception
  {
    int x=0;
    boolean found = false;
    while(x < numCompanyCodes[0] && ! found)
    {
      if(companyCodes[0][x].equals(thisCompanyCode))
        found = true;
      else ++x;
    }
   
    if(! found)
    {
      if((numCompanyCodes[0] + 1) == companyCodesSize[0])
      {
        int z;
       
        String[] buf = new String[companyCodesSize[0] + 10]; // add room for 10 more companies
        for(z=0;z<companyCodesSize[0];++z)
          buf[z] = companyCodes[0][z];
        companyCodes[0] = buf;

        int upto = (companyCodesSize[0] * 5);

        double[] dbuf = new double[(companyCodesSize[0] * 5) + 50];
        for(z=0;z<upto;++z)
          dbuf[z] = totals[0][z];
        for(z=upto;z<(upto+50);++z)
          dbuf[z] = 0.0;
        totals[0] = dbuf;

        companyCodesSize[0] += 10;
      }

      x = numCompanyCodes[0];

      ++numCompanyCodes[0];
     
      companyCodes[0][x] = thisCompanyCode;
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
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void outputPageFrame(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, boolean plain, String effectiveDate, String mfr, String bodyStr, String title, String unm, String sid, String uty, String men,
                               String den, String dnm, String bnm, String otherSetup, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    int[] hmenuCount = new int[1];

    String subMenuText = dashboardUtils.buildSubMenuText(con, stmt, rs, req, "1022", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount);

    subMenuText += "<table id='title' width=100%><tr><td colspan=2 nowrap><p>" + title + directoryUtils.buildHelp(1022) + "</td></tr></table>";

    subMenuText += buildSubMenuText(plain, mfr, effectiveDate, unm, sid, uty, men, den, dnm, bnm, hmenuCount);

    if(plain)
      scoutln(out, bytesOut, authenticationUtils.buildPlainScreen(con, stmt, rs, req, unm, sid, uty, men, den, dnm, bnm, "ManufacturerSalesInput", subMenuText, hmenuCount[0], bodyStr, otherSetup, localDefnsDir, defnsDir));
    else scoutln(out, bytesOut, authenticationUtils.buildScreen(con, stmt, rs, req, true, unm, sid, uty, men, den, dnm, bnm, "ManufacturerSalesInput", subMenuText, hmenuCount[0], bodyStr, otherSetup, localDefnsDir, defnsDir));

    scoutln(out, bytesOut, "<table border=0><tr><td valign=top><div id='second' style=\"{margin-top:0px;}\"></div></td>");
    scoutln(out, bytesOut, "<td valign=top><div id='third' style=\"{margin-top:0px;margin-left:0px;}\"></div></td></tr></table>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String buildSubMenuText(boolean plain, String mfr, String effectiveDate, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] hmenuCount) throws Exception
  {
    String s = "<div id='submenu'>";

    ++hmenuCount[0];

    if(! plain)
    {
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\">";
      s += "<a href=\"/central/servlet/ManufacturerSalesGenerate?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + effectiveDate + "&p2=" + mfr + "&p3=P\">Friendly</a></dt></dl>";
    }
    
    s += "</div>";
    
    --hmenuCount[0];
    
    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
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
