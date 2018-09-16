// =======================================================================================================================================================================================================
// System: ZaraStar Analytic: Stock Usage Spread for a Manufacturer - doit
// Module: StockUsageSpreadExecute.java
// Author: C.K.Harvey
// Copyright (c) 2003-07 Christopher Harvey. All Rights Reserved.
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
import java.net.URL;
import java.net.URLConnection;

public class StockUsageSpreadExecute extends HttpServlet
{
  GeneralUtils  generalUtils = new GeneralUtils();
  MessagePage  messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils  pageFrameUtils = new PageFrameUtils();
  DirectoryUtils  directoryUtils = new DirectoryUtils();
  Customer customer = new Customer();
  AccountsUtils  accountsUtils = new AccountsUtils();
  MiscDefinitions  miscDefinitions = new MiscDefinitions();
  DocumentUtils  documentUtils = new DocumentUtils();
  
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
      directoryUtils.setContentHeaders2(res);
      out = res.getWriter();

      unm = req.getParameter("unm");
      sid = req.getParameter("sid");
      uty = req.getParameter("uty");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");
      p1  = req.getParameter("p1"); // order

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
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "StockUsageSpreadExecute", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3014, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den,
                    String dnm, String bnm, String p1, int[] bytesOut) throws Exception
  {
    String localDefnsDir  = directoryUtils.getLocalOverrideDir(dnm);
    String defnsDir          = directoryUtils.getSupportDirs('D');
    String imagesDir         = directoryUtils.getSupportDirs('I');
    
    long startTime = new java.util.Date().getTime();

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 3014, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "StockUsageSpread", imagesDir, localDefnsDir,
                         defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3014, bytesOut[0], 0, "ACC:" + p1);
    if(con  != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "StockUsageSpread", imagesDir, localDefnsDir,
                         defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3014, bytesOut[0], 0, "SID:" + p1);
    if(con  != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    process(con, stmt, rs, out, req, p1, unm, sid, uty, men, den, dnm, bnm, imagesDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 3014, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con  != null) con.close();
    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void process(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String p1, String unm, String sid, String uty,
                       String men, String den, String dnm, String bnm, String imagesDir, String localDefnsDir, String defnsDir,
                       int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Stock Usage Spread for a Manufacturer</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    scoutln(out, bytesOut, "function enquiry(code,from,to){var p1=sanitise(code);");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/MainPageUtils2a?unm=" + unm + "&sid=" + sid + "&uty="
                           + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                           + "&p1=\"+p1+\"&p3=\"+from+\"&p4=\"+to+\"&p2=P%20G%20\";}");

    scoutln(out, bytesOut, "function enquiry2(code){var p1=sanitise(code);");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/MainPageUtils1a?unm=" + unm + "&sid=" + sid + "&uty="
                           + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+p1;}");

    scoutln(out, bytesOut, "function viewItem(code){var p1=sanitise(code);");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/ProductStockRecord?unm=" + unm + "&sid=" + sid + "&uty="
                           + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p1=\"+p1;}");
    
    scoutln(out, bytesOut, "function sanitise(code){");
    scoutln(out, bytesOut, "var code2='';var x;var len=code.length;");
    scoutln(out, bytesOut, "for(x=0;x<len;++x)");
    scoutln(out, bytesOut, "if(code.charAt(x)=='#')code2+='%23';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='\"')code2+='%22';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='&')code2+='%26';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='%')code2+='%25';");
    scoutln(out, bytesOut, "else if(code.charAt(x)==' ')code2+='%20';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='?')code+='%3F';");
    scoutln(out, bytesOut, "else code2+=code.charAt(x);");
    scoutln(out, bytesOut, "return code2};");
    
    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\""
                           + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\"></head>");
  
    int[] hmenuCount = new int[1];
 
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "3014", "", "StockUsageSpread", unm, sid, uty, men, den, dnm, bnm, localDefnsDir,
                          defnsDir, hmenuCount, bytesOut);
                                
    scoutln(out, bytesOut, "<form>");
  
    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Stock Usage Spread for a Manufacturer", "3014", unm, sid, uty,
                    men, den, dnm, bnm, hmenuCount, bytesOut);

    scout(out, bytesOut, "<p>For Manufacturer: " + p1);
    
    scoutln(out, bytesOut, "<br><table id=\"page\" width=100%>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr id=\"pageColumn\">");
    scoutln(out, bytesOut, "<td><p> Item Code </td>");
    scoutln(out, bytesOut, "<td><p> Mfr Code </td>");
    scoutln(out, bytesOut, "<td align=center><p> Last 30 </td>");
    scoutln(out, bytesOut, "<td align=center><p> Last 60 </td>");
    scoutln(out, bytesOut, "<td align=center><p> Last 90 </td>");
    scoutln(out, bytesOut, "<td align=center><p> One Year </td>");
    scoutln(out, bytesOut, "<td align=center><p> Effective Quantity OnHand </td>");
    scoutln(out, bytesOut, "<td><p> Description </td></tr>");

    char dpOnQuantities = miscDefinitions.dpOnQuantities(con, stmt, rs, "5");

    int dateLess30  = generalUtils.todayEncoded(localDefnsDir, defnsDir) - 30;  
    int dateLess60  = dateLess30 - 30;  
    int dateLess90  = dateLess30 - 60;  
    int dateLess365 = dateLess30 - 335;  

    double[] upto30  = new double[1]; upto30[0]  = 0.0;
    double[] upto60  = new double[1]; upto60[0]  = 0.0;
    double[] upto90  = new double[1]; upto90[0]  = 0.0;
    double[] upto365 = new double[1]; upto365[0] = 0.0;

    String date0   = generalUtils.today(localDefnsDir, defnsDir);
    String date29  = generalUtils.decode((dateLess30 - 1), localDefnsDir, defnsDir);
    String date59  = generalUtils.decode((dateLess60 - 1), localDefnsDir, defnsDir);
    String date89  = generalUtils.decode((dateLess90 - 1), localDefnsDir, defnsDir);
    String date364 = generalUtils.decode((dateLess30 - 334) , localDefnsDir, defnsDir);
                    
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT ItemCode, Description, ManufacturerCode FROM stock WHERE Manufacturer = '" + p1 + "' "
                                   + "ORDER BY ManufacturerCode");

    String itemCode, desc, mfrCode;
    double onHand;

    while(rs.next())
    {    
      itemCode = rs.getString(1);
      desc     = rs.getString(2);
      mfrCode  = rs.getString(3);

      upto30[0] = upto60[0] = upto90[0] = upto365[0] = 0.0;

      onHand = calculate(con, stmt, rs, itemCode, dateLess30, dateLess60, dateLess90, dateLess365, upto30, upto60, upto90, upto365,
                         unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir);

      scoutln(out, bytesOut, "<tr>");
      scoutln(out, bytesOut, "<td><p><a href=\"javascript:viewItem('" + itemCode + "')\">" + itemCode + "</a></td>");
      scoutln(out, bytesOut, "<td><p>" + mfrCode + "</td>");

      if(upto30[0] == 0)
        scoutln(out, bytesOut, "<td align=center><p>0</td>");
      else
      if(upto30[0] < 0)
      {
        scoutln(out, bytesOut, "<td align=center><p><font color=red><a href=\"javascript:enquiry('" + itemCode + "','" + date29
                             + "','" + date0 + "')\">" + generalUtils.doubleDPs(dpOnQuantities, upto30[0]) + "</a></td>");
      }
      else
      {
        scoutln(out, bytesOut, "<td align=center><p><a href=\"javascript:enquiry('" + itemCode + "','" + date29 + "','" + date0
                             + "')\">" + generalUtils.doubleDPs(dpOnQuantities, upto30[0]) + "</a></td>");
      }

      if(upto60[0] == 0)
        scoutln(out, bytesOut, "<td align=center><p>0</td>");
      else
      {
        scoutln(out, bytesOut, "<td align=center><p><a href=\"javascript:enquiry('" + itemCode + "','" + date59 + "','" + date0
                             + "')\">" + generalUtils.doubleDPs(dpOnQuantities, upto60[0]) + "</a></td>");
      }

      if(upto90[0] == 0)
        scoutln(out, bytesOut, "<td align=center><p>0</td>");
      else
      {
        scoutln(out, bytesOut, "<td align=center><p><a href=\"javascript:enquiry('" + itemCode + "','" + date89 + "','" + date0
                             + "')\">" + generalUtils.doubleDPs(dpOnQuantities, upto90[0]) + "</a></td>");
      }

      if(upto365[0] == 0)
        scoutln(out, bytesOut, "<td align=center><p>0</td>");
      else
      {
        scoutln(out, bytesOut, "<td align=center><p><a href=\"javascript:enquiry('" + itemCode + "','" + date364 + "','" + date0
                             + "')\">" + generalUtils.doubleDPs(dpOnQuantities, upto365[0]) + "</a></td>");
      }
      
      if(onHand == 0)
        scoutln(out, bytesOut, "<td align=center><p>0</td>");
      else
      {
        scoutln(out, bytesOut, "<td align=center><p><a href=\"javascript:enquiry2('" + itemCode + "')\">"
                               + generalUtils.doubleDPs(dpOnQuantities, onHand) + "</a></td>");
      }
      
      scoutln(out, bytesOut, "<td nowrap><p>" + desc + "</td></tr>");
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    scoutln(out, bytesOut, "</table></form>");     scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private double calculate(Connection con, Statement stmt, ResultSet rs, String itemCode, int dateLess30, int dateLess60,
                           int dateLess90, int dateLess365, double[] upto30, double[] upto60, double[] upto90, double[] upto365,
                           String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                           String localDefnsDir, String defnsDir) throws Exception
  {
    double thisQty;
    int thisDate;
    
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t1.Date, t2.QuantityPacked FROM pl AS t1 INNER JOIN pll AS t2 ON t1.PLCode = t2.PLCode "
                         + "WHERE t2.ItemCode = '" + generalUtils.sanitiseForSQL(itemCode) + "' AND t1.Status != 'C'");

    while(rs.next())
    {
      thisDate  = generalUtils.encodeFromYYYYMMDD(rs.getString(1));
      thisQty   = generalUtils.doubleFromStr(rs.getString(2));
   
      if(thisDate >= dateLess30)
        upto30[0] += thisQty;

      if(thisDate >= dateLess60)
        upto60[0] += thisQty;

      if(thisDate >= dateLess90)
        upto90[0] += thisQty;

      if(thisDate >= dateLess365)
        upto365[0] += thisQty;
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return stockLevel(itemCode, unm, sid, uty, men, den, dnm, bnm, localDefnsDir);

 }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private double stockLevel(String itemCodeStr, String unm, String sid, String uty, String men, String den, String dnm,
                            String bnm, String localDefnsDir) throws Exception
  {
    String traceList = getStockLevelsViaTrace(itemCodeStr, unm, uty, sid, men, den, dnm, bnm, localDefnsDir);
    // traceList = "SamLeong\001150000\001SyedAlwi\00150000\001\0"

    String pendingsList = getPendings(itemCodeStr, unm, uty, sid, men, den, dnm, bnm, localDefnsDir);
    // pendings = <onPO> \001 <onSO> \001 <onPLNotCompleted> \001 <onGRNInTransit> \001 
    
    double[] totalStockLevel = new double[1];
    double[] totalPending    = new double[1];
    
    totalLevels(traceList, pendingsList, totalStockLevel, totalPending);

    return totalStockLevel[0] - totalPending[0];
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private String getStockLevelsViaTrace(String itemCode, String unm, String uty, String sid, String men, String den, String dnm, String bnm,
                                        String localDefnsDir) throws Exception
  { 
    URL url = new URL("http://" + serverUtils.serverToCall("OFSA", localDefnsDir) + "/central/servlet/StockLevelsGenerate?unm=" + unm + "&sid=" + sid + "&uty=" + uty
                    + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=" + itemCode + "&bnm=" + bnm);

    URLConnection uc = url.openConnection();
    uc.setDoInput(true);
    uc.setUseCaches(false);
    uc.setDefaultUseCaches(false);

    uc.setRequestProperty("Content-Type", "application/octet-stream");

    BufferedReader di = new BufferedReader(new InputStreamReader(uc.getInputStream()));

    String t="", s = di.readLine();
    while(s != null)
    {
      t += s;
      s = di.readLine();
    }

    di.close();

    return t;
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private String getPendings(String itemCode, String unm, String uty, String sid, String men, String den, String dnm,
                             String bnm, String localDefnsDir) throws Exception
  { 
    URL url = new URL("http://" + serverUtils.serverToCall("OFSA", localDefnsDir) + "/central/servlet/StockGenerateOnPOOnSOOnPLOnGRNs?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=" + itemCode
                    + "&bnm=" + bnm);

    URLConnection uc = url.openConnection();
    uc.setDoInput(true);
    uc.setUseCaches(false);
    uc.setDefaultUseCaches(false);

    uc.setRequestProperty("Content-Type", "application/octet-stream");

    BufferedReader di = new BufferedReader(new InputStreamReader(uc.getInputStream()));

    String t="", s = di.readLine();
    while(s != null)
    {
      t += s;
      s = di.readLine();
    }

    di.close();

    return t;
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void totalLevels(String traceList, String pendingsList, double[] totalStockLevel, double[] totalPending) throws Exception
  {
    int y=0, len = traceList.length();
    String thisQty;
    totalStockLevel[0] = 0.0;

    while(y < len) // just-in-case
    {
      while(y < len && traceList.charAt(y) != '\001')
        ++y;
      ++y;

      thisQty = "";
      while(y < len && traceList.charAt(y) != '\001')
        thisQty += traceList.charAt(y++);
      ++y;

      totalStockLevel[0] += generalUtils.doubleFromStr(thisQty);
    }

    y=0;
    len = pendingsList.length();
    totalPending[0] = 0.0;

    while(y < len && pendingsList.charAt(y) != '\001') // step over PO
      ++y;
    ++y;

    thisQty = "";
    while(y < len && pendingsList.charAt(y) != '\001') // want SO
      thisQty += pendingsList.charAt(y++);
    ++y;

    totalPending[0] += generalUtils.doubleFromStr(thisQty);

    thisQty = "";
    while(y < len && pendingsList.charAt(y) != '\001') // want PL
      thisQty += pendingsList.charAt(y++);
    ++y;

    totalPending[0] += generalUtils.doubleFromStr(thisQty);
  }

  // -------------------------------------------------------------------------------------------------------------------------------
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
