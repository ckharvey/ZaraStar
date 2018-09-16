// =======================================================================================================================================================================================================
// System: ZaraStar: Quotation & Sales Order Processing - Scheduling
// Module: SalesOrderSchedulingExecute.java
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
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;
import java.io.*;

public class SalesOrderSchedulingExecute extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  MiscDefinitions miscDefinitions = new MiscDefinitions();
  DrawingUtils drawingUtils = new DrawingUtils();
  OrderConfirmation orderConfirmation = new OrderConfirmation();
  SalesOrder salesOrder = new SalesOrder();
  
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
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
      p1  = req.getParameter("p1");
      
      p1 = generalUtils.replaceSpacesWith20(p1) + "\001";              
      byte[] checkBoxes = new byte[p1.length()+1];
      generalUtils.strToBytes(checkBoxes, p1);
      
      doIt(out, req, checkBoxes, unm, sid, uty, men, den, dnm, bnm, bytesOut);
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
 
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "SalesOrderSchedulinga", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2033, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush(); 
    }
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

      byte[] checkBoxes    = new byte[1000]; checkBoxes[0]    = '\000';
      int[]  checkBoxesLen = new int[1];     checkBoxesLen[0] = 1000;

      int thisEntryLen, inc;

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
        else // must be checkbox value
        {
          thisEntryLen = name.length() + 2;
          if((generalUtils.lengthBytes(checkBoxes, 0) + thisEntryLen) >= checkBoxesLen[0])
          {
            byte[] tmp = new byte[checkBoxesLen[0]];
            System.arraycopy(checkBoxes, 0, tmp, 0, checkBoxesLen[0]);
            if(thisEntryLen > 1000)
              inc = thisEntryLen;
            else inc = 1000;
            checkBoxesLen[0] += inc;
            checkBoxes = new byte[checkBoxesLen[0]];
            System.arraycopy(tmp, 0, checkBoxes, 0, checkBoxesLen[0] - inc);
          }

          generalUtils.catAsBytes(name + "\001", 0, checkBoxes, false);
        }
      }

      doIt(out, req, checkBoxes, unm, sid, uty, men, den, dnm, bnm, bytesOut);
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
 
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "SalesOrderSchedulinga", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2033, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, byte[] checkBoxes, String unm, String sid, String uty, String men, String den,
                    String dnm, String bnm, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    
    Statement stmt = null;
    ResultSet rs   = null;
    Statement stmt2 = null, stmt3 = null;
    ResultSet rs2   = null, rs3 = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 2033, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "SalesOrderSchedulinga", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2033, bytesOut[0], 0, "ACC:");
      
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "SalesOrderSchedulinga", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2033, bytesOut[0], 0, "SID:");
      
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    set(con, stmt, stmt2, stmt3, rs, rs2, rs3, out, req, checkBoxes, unm, sid, uty, men, den, dnm, bnm, imagesDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 2033, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
      
      if(con != null) con.close();
    if(out != null) out.flush(); 
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, ResultSet rs3, PrintWriter out, HttpServletRequest req,
                   byte[] checkBoxes, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String imagesDir,
                   String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Sales Order Processing</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    scoutln(out, bytesOut, "function update(){document.go.submit();}");

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4019, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewQuote(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/QuotationPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men
                           + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4031, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewSO(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/SalesOrderPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men
                           + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4001, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewCust(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/CustomerPage?unm=" + unm + "&sid=" + sid + "&uty="
                           + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p1=\"+p1;}");
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
  
    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir)
                         + "general.css\">");

    int[] hmenuCount = new int[1];

    drawingUtils.outputPageFrame(con, stmt, rs, out, req, "2033", "", "SalesOrderSchedulinga", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount, bytesOut);

    drawingUtils.drawTitle(out, false, false, "SalesOrderSchedulinga", "", "Sales Order Processing - Scheduling", "2033", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<form name=\"go\" action=\"SalesOrderSchedulingUpdate\" enctype=\"application/x-www-form-urlencoded\" method=POST>");

    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=unm value=" + unm + ">");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=sid value=" + sid + ">");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=uty value=" + uty + ">");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=men value=\"" + men + "\">");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=den value=" + den + ">");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=dnm value=" + dnm + ">");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=bnm value=" + bnm + ">");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=salesPeople value=\"" + generalUtils.stringFromBytes(checkBoxes, 0L) + "\">");

    scoutln(out, bytesOut, "<table id=\"page\" width=100% border=0>");

    scoutln(out, bytesOut, "<tr><td></td></tr><tr id=\"pageColumn\"><td></td>");

    scoutln(out, bytesOut, "<td><p><b> Code </b></td>");
    scoutln(out, bytesOut, "<td><p><b> Date </b></td>");
    scoutln(out, bytesOut, "<td align=center><p><b> Engineering Approved </b></td>");
    scoutln(out, bytesOut, "<td align=center><p><b> Purchasing Confirmed </b></td>");
    scoutln(out, bytesOut, "<td align=center><p><b> Scheduling Confirms </b></td>");
    scoutln(out, bytesOut, "<td><p><b> Customer Code </b></td>");
    scoutln(out, bytesOut, "<td><p><b> Customer Name </b></td>");
    scoutln(out, bytesOut, "<td><p><b> Customer POCode </b></td>");
    scoutln(out, bytesOut, "<td><p><b> Amount </b></td>");
    scoutln(out, bytesOut, "<td><p><b> Currency </b></td></tr>");

    String[] cssFormat = new String[1];  cssFormat[0] = "line1";
    int[] oCount = new int[1];  oCount[0] = 1;
    char dpOnQuantities = miscDefinitions.dpOnQuantities(con, stmt, rs, "5");
    
    forAllSalesOrderHeaders(con, stmt, stmt2, stmt3, rs, rs2, rs3, out, checkBoxes, dpOnQuantities, cssFormat, oCount, dnm, localDefnsDir, defnsDir, bytesOut);
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td><img src=\"" + imagesDir + "upredarrow.png\" border=0></td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap colspan=8><p><font color=red>Red</font> if overdue confirmation</td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap colspan=4><p><a href=\"javascript:update()\">Update</a> Sales Orders</td></tr>");
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
                                       byte[] checkBoxes, char dpOnQuantities, String[] cssFormat, int[] oCount, String dnm, String localDefnsDir,
                                       String defnsDir, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT SalesPerson, SOCode, Date, CompanyCode, CustomerPOCode, TotalTotal, Currency2, CompanyName, EngineeringApproved, "
                         + "EngineeringApprovedDate, EngineeringApprovedSignOn, ProcurementConfirmed, ProcurementConfirmedDate, "
                         + "ProcurementConfirmedSignOn, SOType, ToEngineeringDate, ToEngineeringSignOn, ToProcurementDate, ToProcurementSignOn, "
                         + "ToEngineering, ToProcurement "
                         + "FROM so WHERE ToScheduling = 'Y' AND SchedulingConfirmed != 'Y' AND Status != 'C' AND AllSupplied != 'Y' AND SOType != 'T' ORDER BY Date, SOCode");
      
    String salesPerson, soCode, date, customerCode, custPOCode, currency, companyName, engineeringApproved, engineeringApprovedDate, soType,
           engineeringApprovedSignOn, procurementConfirmed, procurementConfirmedDate, procurementConfirmedSignOn, toEngineeringDate,
           toEngineeringSignOn, toProcurementDate, toProcurementSignOn, toEngineering, toProcurement;
    String[] ocDate   = new String[1];
    String[] ocSignOn = new String[1];
    double totalTotal;
    byte[] b = new byte[20];
    boolean oc, overdue, ocNotNeeded;

    int todayEncoded = generalUtils.todayEncoded(localDefnsDir, defnsDir);
    
    while(rs.next())
    {    
      salesPerson = generalUtils.deNull(rs.getString(1));
      
      if(salesPersonIsRequired(salesPerson, checkBoxes))
      {
        soCode                      = generalUtils.deNull(rs.getString(2));
        date                        = generalUtils.deNull(rs.getString(3));
        customerCode                = generalUtils.deNull(rs.getString(4));
        custPOCode                  = generalUtils.deNull(rs.getString(5));
        totalTotal                  = generalUtils.doubleFromStr(generalUtils.deNull(rs.getString(6)));
        currency                    = generalUtils.deNull(rs.getString(7));
        companyName                 = generalUtils.deNull(rs.getString(8));
        engineeringApproved         = generalUtils.deNull(rs.getString(9));
        engineeringApprovedDate     = generalUtils.deNull(rs.getString(10));
        engineeringApprovedSignOn   = generalUtils.deNull(rs.getString(11));
        procurementConfirmed        = generalUtils.deNull(rs.getString(12));
        procurementConfirmedDate    = generalUtils.deNull(rs.getString(13));
        procurementConfirmedSignOn  = generalUtils.deNull(rs.getString(14));
        soType                      = generalUtils.deNull(rs.getString(15));
        toEngineeringDate           = generalUtils.deNull(rs.getString(16));
        toEngineeringSignOn         = generalUtils.deNull(rs.getString(17));
        toProcurementDate           = generalUtils.deNull(rs.getString(18));
        toProcurementSignOn         = generalUtils.deNull(rs.getString(19));
        toEngineering               = generalUtils.deNull(rs.getString(20));
        toProcurement               = generalUtils.deNull(rs.getString(21));
      
        generalUtils.doubleToBytesCharFormat(totalTotal, b, 0);
        generalUtils.bytesDPsGivenSeparator(true, '\000', '2', b, 20, 0);
        generalUtils.formatNumeric(b, '2');

        if(cssFormat[0].equals("line1"))
          cssFormat[0] = "line2";
        else cssFormat[0] = "line1";
 
        scoutln(out, bytesOut, "<tr  id=\"" + cssFormat[0] + "\">");
      
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
        scout(out, bytesOut, "<td><p>" + generalUtils.convertFromYYYYMMDD(date) + "</td>");

        if(! toEngineering.equals("Y"))
          scout(out, bytesOut, "<td align=center><p><font color=red>Not Sent to Engineering</td>");
        else
        {
          if(! engineeringApproved.equals("Y"))
            scout(out, bytesOut, "<td><p><font color=orange>Sent to Engineering " + generalUtils.convertFromYYYYMMDD(toEngineeringDate)  + " by " + toEngineeringSignOn  + "</td>");
          else scout(out, bytesOut, "<td><p><font color=green>" + generalUtils.convertFromYYYYMMDD(engineeringApprovedDate)  + " by "
                                    + engineeringApprovedSignOn  + "</td>");
        }
        
        if(! toProcurement.equals("Y"))
         scout(out, bytesOut, "<td align=center><p><font color=red>Not Sent to Procurement</td>");
        else
        {
          if(! procurementConfirmed.equals("Y"))
            scout(out, bytesOut, "<td><p><font color=orange>Sent to Procurement " + generalUtils.convertFromYYYYMMDD(toProcurementDate)  + " by " + toProcurementSignOn  + "</td>");
          else scout(out, bytesOut, "<td><p><font color=green>" + generalUtils.convertFromYYYYMMDD(procurementConfirmedDate)  + " by "
                                    + procurementConfirmedSignOn  + "</td>");
        }
      
        scout(out, bytesOut, "<td align=center><p><input type=checkbox name=schCon" + generalUtils.sanitise(soCode) + "></td>");
            
        scout(out, bytesOut, "<td><p><a href=\"javascript:viewCust('" + customerCode + "')\">" + customerCode + "</a></td>");
        scout(out, bytesOut, "<td nowrap><p>" + companyName + "</td>");
        scout(out, bytesOut, "<td nowrap><p>" + custPOCode + "</td>");
        scoutln(out, bytesOut, "<td align=right><p>" + generalUtils.stringFromBytes(b, 0L) + "</td>");
        scout(out, bytesOut, "<td nowrap><p>" + currency + "</td></tr>");
      }
    }
  
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean salesPersonIsRequired(String salesPerson, byte[] checkBoxes) throws Exception
  {
    String s;

    int x = 0, len = generalUtils.lengthBytes(checkBoxes, 0);
    while(x < len)
    {
      s = "";
      while(checkBoxes[x] != '\001' && checkBoxes[x] != '\000')
        s += (char)checkBoxes[x++];

      s = generalUtils.deSanitise(s);

      if(s.equals(salesPerson))
        return true;

      ++x;
    }
    
    return false;
  }

}
