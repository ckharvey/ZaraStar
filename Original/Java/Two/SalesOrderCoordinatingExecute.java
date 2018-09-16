// =======================================================================================================================================================================================================
// System: ZaraStar: Sales Orders Processing - Coordinating
// Module: SalesOrderCoordinatingExecute.java
// Author: C.K.Harvey
// Copyright (c) 2001-09 Christopher Harvey. All Rights Reserved.
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

public class SalesOrderCoordinatingExecute extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  WorksOrder worksOrder = new WorksOrder();
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
      directoryUtils.setContentHeaders2(res);
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
 
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "SalesOrderCoordinatingExecute", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2034, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, byte[] checkBoxes, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    
    long startTime = new java.util.Date().getTime();

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null, stmt2 = null;
    ResultSet rs   = null, rs2 = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 2034, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "SalesOrderCoordinatingExecute", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2034, bytesOut[0], 0, "ACC:");
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "SalesOrderCoordinatingExecute", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2034, bytesOut[0], 0, "SID:");
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    set(con, stmt, stmt2, rs, rs2, out, req, checkBoxes, unm, sid, uty, men, den, dnm, bnm, imagesDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 2034, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, HttpServletRequest req, byte[] checkBoxes, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                   String imagesDir, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Sales Order Processing</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    scoutln(out, bytesOut, "function update(){document.go.submit();}");

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4031, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewSO(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/SalesOrderPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4431, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewWO(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/WorksOrderPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4001, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewCust(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/CustomerPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p1=\"+p1;}");
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
  
    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    int[] hmenuCount = new int[1];

    drawingUtils.outputPageFrame(con, stmt, rs, out, req, "2034", "", "SalesOrderCoordinatingExecute", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount, bytesOut);

    drawingUtils.drawTitle(out, false, false, "SalesOrderCoordinatingExecute", "", "Sales Order Processing - Coordinating", "2034", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<form name=\"go\" action=\"SalesOrderCoordinatorUpdate\" enctype=\"application/x-www-form-urlencoded\" method=POST>");

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

    scoutln(out, bytesOut, "<td><p><b> SO Code </b></td>");
    scoutln(out, bytesOut, "<td><p><b> SO Date </b></td>");
    scoutln(out, bytesOut, "<td align=center><p><b> Works Orders Created </b></td>");
    scoutln(out, bytesOut, "<td align=center><p><b> Sent to Procurement </b></td>");
    scoutln(out, bytesOut, "<td align=center><p><b> All Available </b></td>");
    scoutln(out, bytesOut, "<td align=center><p><b> Ready for Workshop </b></td>");
    scoutln(out, bytesOut, "<td><p><b> Customer Code </b></td>");
    scoutln(out, bytesOut, "<td><p><b> Customer Name </b></td>");
    scoutln(out, bytesOut, "<td><p><b> Customer POCode </b></td></tr>");

    String[] cssFormat = new String[1];  cssFormat[0] = "line1";
    int[] oCount = new int[1];  oCount[0] = 1;
    
    forAllSalesOrderHeaders(con, stmt, stmt2, rs, rs2, out, checkBoxes, cssFormat, oCount, bytesOut);
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap colspan=4><p><a href=\"javascript:update()\">Update</a> Sales Orders</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
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
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void forAllSalesOrderHeaders(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, byte[] checkBoxes, String[] cssFormat, int[] oCount, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();
        
    rs = stmt.executeQuery("SELECT t1.SalesPerson, t1.SOCode, t1.Date, t1.CompanyCode, t1.CustomerPOCode, t1.CompanyName, t2.ToProcurement, t2.ToProcurementDate, t2.ToProcurementSignOn, t2.ReadyForWorkshop, t2.ReadyForWorkshopDate, "
                         + "t2.ReadyForWorkshopSignOn, t2.Line FROM sol AS t2 INNER JOIN so AS t1 ON t2.SOCode = t1.SOCode "
                         + "WHERE t1.Status != 'C' AND t1.AllSupplied != 'Y' AND t2.WOOverride != 'Y' ORDER BY t1.Date, t2.SOCode, t2.Line");
      
    String salesPerson, soCode="", date="", customerCode, custPOCode, companyName, toProcurement, toProcurementDate, toProcurementSignOn, readyForWorkshop, readyForWorkshopDate, readyForWorkshopSignOn, soLine, lastSOCode = "", s, lastDate="";
    String[] woCode = new String[1];
    byte[] woCodeB = new byte[21];
    int notConvertedCount = 0;

    int[] listLen = new int[1];  listLen[0] = 100;
    byte[] list = new byte[listLen[0]];

    while(rs.next())
    {    
      salesPerson = generalUtils.deNull(rs.getString(1));
      
      if(salesPersonIsRequired(salesPerson, checkBoxes))
      {
        soCode       = generalUtils.deNull(rs.getString(2));
        date         = generalUtils.deNull(rs.getString(3));
        customerCode = generalUtils.deNull(rs.getString(4));
        custPOCode   = generalUtils.deNull(rs.getString(5));
        companyName  = generalUtils.deNull(rs.getString(6));

        if(! soCode.equals(lastSOCode))
        {
          if(notConvertedCount > 0)
          {
            if(cssFormat[0].equals("line1")) cssFormat[0] = "line2"; else cssFormat[0] = "line1";

            scoutln(out, bytesOut, "<tr  id=\"" + cssFormat[0] + "\">");

            scoutln(out, bytesOut, "<td><p>" + oCount[0]++ + "</td>");

            scoutln(out, bytesOut, "<td><p><a href=\"javascript:viewSO('" + lastSOCode + "')\">" + lastSOCode + "</a></td>");

            scout(out, bytesOut, "<td><p>" + generalUtils.convertFromYYYYMMDD(lastDate) + "</td>");

            s = "line";
            if(notConvertedCount > 1)
              s += "s";
            scout(out, bytesOut, "<td align=center><p><font color=orange>No Works Order Yet (" + notConvertedCount + " " + s + ")</td>");
            scout(out, bytesOut, "<td></td>");
            scout(out, bytesOut, "<td></td>");
            scout(out, bytesOut, "<td></td>");
            scout(out, bytesOut, "<td></td>");
            scout(out, bytesOut, "<td></td>");
            scoutln(out, bytesOut, "<td></td></tr>");
            
            notConvertedCount = 0;
          }

          lastSOCode = soCode;
          lastDate   = date;
        }

        soLine = generalUtils.deNull(rs.getString(13));

        if(worksOrder.getWOCodeGivenSOCodeAndSOLine(con, stmt2, rs2, soCode, soLine, woCode))
        {
          generalUtils.strToBytes(woCodeB, woCode[0] + "\001");

          if(! generalUtils.chkList(woCodeB, list))
          {
            list = generalUtils.appendToList(false, woCodeB, list, listLen);

            toProcurement          = generalUtils.deNull(rs.getString(7));
            toProcurementDate      = generalUtils.deNull(rs.getString(8));
            toProcurementSignOn    = generalUtils.deNull(rs.getString(9));
            readyForWorkshop       = generalUtils.deNull(rs.getString(10));
            readyForWorkshopDate   = generalUtils.deNull(rs.getString(11));
            readyForWorkshopSignOn = generalUtils.deNull(rs.getString(12));
      
            if(cssFormat[0].equals("line1")) cssFormat[0] = "line2"; else cssFormat[0] = "line1";
 
            scoutln(out, bytesOut, "<tr  id=\"" + cssFormat[0] + "\">");

            scoutln(out, bytesOut, "<td><p>" + oCount[0]++ + "</td>");

            scoutln(out, bytesOut, "<td><p><a href=\"javascript:viewSO('" + soCode + "')\">" + soCode + "</a></td>");

            scout(out, bytesOut, "<td><p>" + generalUtils.convertFromYYYYMMDD(date) + "</td>");

            scoutln(out, bytesOut, "<td align=center><p><a href=\"javascript:viewWO('" + woCode[0] + "')\">" + woCode[0] + "</a></td>");

            if(toProcurement.equals("Y"))
              scoutln(out, bytesOut, "<td><p><font color=green>" + generalUtils.convertFromYYYYMMDD(toProcurementDate) +  " by " + toProcurementSignOn + "</td>");
            else scout(out, bytesOut, "<td align=center><p><input type=checkbox name=toPro" + generalUtils.sanitise(soCode) + "></td>");




            scoutln(out, bytesOut, "<td align=center><p><font color=red>No</td>");
        
        
        
            if(readyForWorkshop.equals("Y"))
              scoutln(out, bytesOut, "<td><p>" + generalUtils.convertFromYYYYMMDD(readyForWorkshopDate) +  " by " + readyForWorkshopSignOn + "</td>");
            else scout(out, bytesOut, "<td align=center><p><input type=checkbox name=ready" + generalUtils.sanitise(soCode) + "></td>");
      
            scout(out, bytesOut, "<td><p><a href=\"javascript:viewCust('" + customerCode + "')\">" + customerCode + "</a></td>");
            scout(out, bytesOut, "<td nowrap><p>" + companyName + "</td>");
            scoutln(out, bytesOut, "<td nowrap><p>" + custPOCode + "</td></tr>");
          }
        }
        else // line not converted to WO, yet
        {
          ++notConvertedCount;
        }
      }
    }
  
    if(notConvertedCount > 0)
    {
      if(cssFormat[0].equals("line1")) cssFormat[0] = "line2"; else cssFormat[0] = "line1";

      scoutln(out, bytesOut, "<tr  id=\"" + cssFormat[0] + "\">");

      scoutln(out, bytesOut, "<td><p>" + oCount[0]++ + "</td>");

      scoutln(out, bytesOut, "<td><p><a href=\"javascript:viewSO('" + lastSOCode + "')\">" + lastSOCode + "</a></td>");

      scout(out, bytesOut, "<td><p>" + generalUtils.convertFromYYYYMMDD(lastDate) + "</td>");

      s = "line";
      if(notConvertedCount > 1)
        s += "s";
      scout(out, bytesOut, "<td align=center><p><font color=orange>No Works Order Yet (" + notConvertedCount + " " + s + ")</td>");
      scout(out, bytesOut, "<td></td>");
      scout(out, bytesOut, "<td></td>");
      scout(out, bytesOut, "<td></td>");
      scout(out, bytesOut, "<td></td>");
      scout(out, bytesOut, "<td></td>");
      scoutln(out, bytesOut, "<td></td></tr>");
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
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
