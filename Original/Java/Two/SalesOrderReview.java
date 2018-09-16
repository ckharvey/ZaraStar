// =======================================================================================================================================================================================================
// System: ZaraStar: Sales Orders Processing - Contract Review
// Module: SalesOrderReview.java
// Author: C.K.Harvey
// Copyright (c) 2001-08 Christopher Harvey. All Rights Reserved.
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

public class SalesOrderReview extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  OrderConfirmation orderConfirmation = new OrderConfirmation();
  SalesOrder salesOrder = new SalesOrder();
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

      unm = req.getParameter("unm");
      sid = req.getParameter("sid");
      uty = req.getParameter("uty");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");
      
      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, bytesOut);
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
 
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "SalesOrderReview", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2039, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                    int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    
    String userName = directoryUtils.getMySQLUserName();
    String passWord = directoryUtils.getMySQLPassWord();
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + userName + "&password=" + passWord);

    Statement stmt = con.createStatement(), stmt2 = null, stmt3 = null;
    ResultSet rs = null, rs2 = null, rs3 = null;
    
    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 2039, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "SalesOrderReview", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2039, bytesOut[0], 0, "ACC:");
      
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "SalesOrderReview", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2039, bytesOut[0], 0, "SID:");
      
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    set(con, stmt, stmt2, stmt3, rs, rs2, rs3, out, req, unm, sid, uty, men, den, dnm, bnm, imagesDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 2039, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, ResultSet rs3, PrintWriter out, HttpServletRequest req, String unm,
                   String sid, String uty, String men, String den, String dnm, String bnm, String imagesDir, String localDefnsDir, String defnsDir,
                   int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Sales Order Processing</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4031, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewSO(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/SalesOrderPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den
                           + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

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
                         + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    int[] hmenuCount = new int[1];

    drawingUtils.outputPageFrame(con, stmt, rs, out, req, "2039", "", "SalesOrderReview", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount, bytesOut);

    drawingUtils.drawTitle(out, false, false, "SalesOrderReview", "", "Sales Order Processing - Contract Review Team", "2039", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<table id=\"page\" width=100% border=0>");

    scoutln(out, bytesOut, "<tr><td></td></tr><tr id=\"pageColumn\"><td></td>");

    scoutln(out, bytesOut, "<td><p><b> SO Code </b></td>");
    scoutln(out, bytesOut, "<td><p><b> SO Date </b></td>");
    scoutln(out, bytesOut, "<td><p><b> Acknowledgement Sent </b></td>");
    scoutln(out, bytesOut, "<td><p><b> Engineering </b></td>");
    scoutln(out, bytesOut, "<td><p><b> Procurement</b></td>");
    scoutln(out, bytesOut, "<td><p><b> Scheduling </b></td>");
    scoutln(out, bytesOut, "<td><p><b> Manager</b></td>");
    scoutln(out, bytesOut, "<td><p><b> Confirmation Sent </b></td>");
    scoutln(out, bytesOut, "<td><p><b> Customer Code</b></td>");
    scoutln(out, bytesOut, "<td><p><b> Name</b></td>");
    scoutln(out, bytesOut, "<td><p><b> Customer PO Code</b></td>");
    scoutln(out, bytesOut, "<td><p><b> Value</b></td>");
    scoutln(out, bytesOut, "<td><p><b> Currency</b></td></tr>");

    String[] cssFormat = new String[1];  cssFormat[0] = "line1";
    int[] oCount = new int[1];  oCount[0] = 1;
    
    forAllSalesOrderHeaders(con, stmt, stmt2, stmt3, rs, rs2, rs3, out, cssFormat, oCount, dnm, localDefnsDir, defnsDir, bytesOut);
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td><img src=\"" + imagesDir + "upredarrow.png\" border=0></td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap colspan=8><p><font color=red>Red</font> if overdue confirmation</td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "</table></form>");     scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
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
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void forAllSalesOrderHeaders(Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, ResultSet rs3, PrintWriter out,
                                       String[] cssFormat, int[] oCount, String dnm, String localDefnsDir, String defnsDir, int[] bytesOut)
                                       throws Exception
  {
    rs = stmt.executeQuery("SELECT SalesPerson, SOCode, Date, CompanyCode, CustomerPOCode, TotalTotal, Currency2, CompanyName, SOType, "
                         + "ToEngineering, EngineeringApproved, ToManager, ManagerApproved, ToProcurement, ProcurementConfirmed, ToScheduling, "
                         + "SchedulingConfirmed, EngineeringApprovedDate, EngineeringApprovedSignOn, ProcurementConfirmedDate, "
                         + "ProcurementConfirmedSignOn, SchedulingConfirmedDate, SchedulingConfirmedSignOn, ToEngineeringDate, ToEngineeringSignOn, "
                         + "ToProcurementDate, ToProcurementSignOn, ToSchedulingDate, ToSchedulingSignOn, ManagerApprovedDate, ManagerApprovedSignOn, "
                         + "ToManagerDate, ToManagerSignOn FROM so WHERE Status != 'C' AND AllSupplied != 'Y' ORDER BY Date, SOCode");
      
    String soCode, date, salesPerson, customerCode, custPOCode, currency, companyName, soType, toEngineering, engineeringApproved, toManager,
           managerApproved, toScheduling, schedulingConfirmed, toProcurement, procurementConfirmed, engineeringApprovedDate,
           engineeringApprovedSignOn, procurementConfirmedDate, procurementConfirmedSignOn, schedulingConfirmedDate, schedulingConfirmedSignOn,
           toEngineeringDate, toEngineeringSignOn, toProcurementDate, toProcurementSignOn, toSchedulingDate, toSchedulingSignOn, managerApprovedDate,
           managerApprovedSignOn, toManagerDate, toManagerSignOn;
    String[] ocDate   = new String[1];
    String[] ocSignOn = new String[1];
    double totalTotal;
    byte[] b = new byte[20];
    boolean oc, overdue, ocNotNeeded;

    int todayEncoded = generalUtils.todayEncoded(localDefnsDir, defnsDir);
    
    while(rs.next())
    {    
      salesPerson          = rs.getString(1);
      soCode               = rs.getString(2);
      date                 = rs.getString(3);
      customerCode         = rs.getString(4);
      custPOCode           = rs.getString(5);
      totalTotal           = generalUtils.doubleFromStr(rs.getString(6));
      currency             = rs.getString(7);
      companyName          = rs.getString(8);
      soType               = rs.getString(9);
      toEngineering        = rs.getString(10);
      engineeringApproved  = rs.getString(11);
      toManager            = rs.getString(12);
      managerApproved      = rs.getString(13);
      toProcurement        = rs.getString(14);
      procurementConfirmed = rs.getString(15);
      toScheduling         = rs.getString(16);
      schedulingConfirmed  = rs.getString(17);
      engineeringApprovedDate    = rs.getString(18);
      engineeringApprovedSignOn  = rs.getString(19);
      procurementConfirmedDate   = rs.getString(20);
      procurementConfirmedSignOn = rs.getString(21);
      schedulingConfirmedDate    = rs.getString(22);
      schedulingConfirmedSignOn  = rs.getString(23);
      toEngineeringDate          = rs.getString(24);
      toEngineeringSignOn        = rs.getString(25);
      toProcurementDate          = rs.getString(26);
      toProcurementSignOn        = rs.getString(27);
      toSchedulingDate           = rs.getString(28);
      toSchedulingSignOn         = rs.getString(29);
      managerApprovedDate        = rs.getString(30);
      managerApprovedSignOn      = rs.getString(31);
      toManagerDate              = rs.getString(32);
      toManagerSignOn            = rs.getString(33);
              
      generalUtils.doubleToBytesCharFormat(totalTotal, b, 0);
      generalUtils.bytesDPsGivenSeparator(true, '\000', '2', b, 20, 0);
      generalUtils.formatNumeric(b, '2');

      if(cssFormat[0].equals("line1")) cssFormat[0] = "line2"; else cssFormat[0] = "line1"; 
      
      scoutln(out, bytesOut, "<tr id=\"" + cssFormat[0] + "\">");

      oc = orderConfirmation.getOCDetailsGivenSOCode(con, stmt2, rs2, soCode, ocDate, ocSignOn);

      ocNotNeeded = false;
      if(! oc)
        ocNotNeeded = salesOrder.confirmationNotNeeded(con, stmt2, rs2, soCode, dnm, localDefnsDir, defnsDir);

      if(! oc && ! ocNotNeeded)
      {
        overdue = false;
        if(soType.equals("T"))
        {
          if((todayEncoded - generalUtils.encodeFromYYYYMMDD(date)) > 1)
            overdue = true;
        }
        else // project
        {
          if((todayEncoded - generalUtils.encodeFromYYYYMMDD(date)) > 14)
            overdue = true;
        }
        
        if(overdue)            
          scoutln(out, bytesOut, "<td style={background-color:red;}><p><font color=white>" + oCount[0]++ + "</td>");
        else scoutln(out, bytesOut, "<td><p>" + oCount[0]++ + "</td>");
      }
      else scoutln(out, bytesOut, "<td><p>" + oCount[0]++ + "</td>");

      scoutln(out, bytesOut, "<td><p><a href=\"javascript:viewSO('" + soCode + "')\">" + soCode + "</a></td>");
      scoutln(out, bytesOut, "<td><p>" + generalUtils.convertFromYYYYMMDD(date) + "</td>");

      if(oaExists(soCode, dnm, localDefnsDir, defnsDir))
        scout(out, bytesOut, "<td align=center><p>Yes</td>");
      else scout(out, bytesOut, "<td align=center><p>No</td>");
      
      if(soType.equals("T"))
        scout(out, bytesOut, "<td align=center><p>-</td>");
      else        
      if(! toEngineering.equals("Y"))
        scout(out, bytesOut, "<td align=center><p><font color=red>Not Sent to Engineering</td>");
      else
      {
        if(! engineeringApproved.equals("Y"))
          scout(out, bytesOut, "<td><p><font color=orange>Sent to Engineering " + generalUtils.convertFromYYYYMMDD(toEngineeringDate)  + " by " + toEngineeringSignOn  + "</td>");
        else scout(out, bytesOut, "<td><p><font color=green>" + generalUtils.convertFromYYYYMMDD(engineeringApprovedDate)  + " by " + engineeringApprovedSignOn  + "</td>");
      }
      
      if(! toProcurement.equals("Y"))
       scout(out, bytesOut, "<td align=center><p><font color=red>Not Sent to Procurement</td>");
      else
      {
        if(! procurementConfirmed.equals("Y"))
          scout(out, bytesOut, "<td><p><font color=orange>Sent to Procurement " + generalUtils.convertFromYYYYMMDD(toProcurementDate)  + " by " + toProcurementSignOn  + "</td>");
        else scout(out, bytesOut, "<td><p><font color=green>" + generalUtils.convertFromYYYYMMDD(procurementConfirmedDate)  + " by " + procurementConfirmedSignOn  + "</td>");
      }
      
      if(soType.equals("T"))
        scout(out, bytesOut, "<td align=center><p>-</td>");
      else        
      if(! toScheduling.equals("Y"))
        scout(out, bytesOut, "<td align=center><p><font color=red>Not Sent to Scheduling</td>");
      else
      {
        if(! schedulingConfirmed.equals("Y"))
          scout(out, bytesOut, "<td><p><font color=orange>Sent to Scheduling " + generalUtils.convertFromYYYYMMDD(toSchedulingDate)  + " by " + toSchedulingSignOn  + "</td>");
        else scout(out, bytesOut, "<td><p><font color=green>" + generalUtils.convertFromYYYYMMDD(schedulingConfirmedDate)  + " by " + schedulingConfirmedSignOn  + "</td>");
      }
      
      if(! toManager.equals("Y"))
        scout(out, bytesOut, "<td align=center><p><font color=red>Not Sent to Manager</td>");
      else
      {
        if(! managerApproved.equals("Y"))
          scout(out, bytesOut, "<td><p><font color=orange>Sent to Manager " + generalUtils.convertFromYYYYMMDD(toManagerDate)  + " by " + toManagerSignOn  + "</td>");
        else scout(out, bytesOut, "<td><p><font color=green>" + generalUtils.convertFromYYYYMMDD(managerApprovedDate)  + " by " + managerApprovedSignOn  + "</td>");
      }
            
      if(oc && ! ocNotNeeded)
        scout(out, bytesOut, "<td><p><font color=green>" + generalUtils.convertFromYYYYMMDD(ocDate[0])  + " by " + ocSignOn[0]  + "</td>");
      else scout(out, bytesOut, "<td align=center><p><font color=red>Not Sent</td>");
            
      scout(out, bytesOut, "<td><p><a href=\"javascript:viewCust('" + customerCode + "')\">" + customerCode + "</a></td>");
      scout(out, bytesOut, "<td nowrap><p>" + companyName + "</td>");
      scout(out, bytesOut, "<td nowrap><p>" + custPOCode + "</td>");
      scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.stringFromBytes(b, 0L) + "</td>");
      scout(out, bytesOut, "<td nowrap><p>" + currency + "</td></tr>");
    }
  
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean oaExists(String soCode, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    return false;
  }
  
}
