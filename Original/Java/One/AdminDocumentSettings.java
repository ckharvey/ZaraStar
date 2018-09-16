// =======================================================================================================================================================================================================
// System: ZaraStar Admin: Edit document settings
// Module: AdminDocumentSettings.java
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

import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

public class AdminDocumentSettings extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  DashboardUtils dashboardUtils = new DashboardUtils();
  DefinitionTables definitionTables = new DefinitionTables();
  SignOnAdministrator  signOnAdministrator  = new SignOnAdministrator();

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
 
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "AdminDocumentSettings", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7019, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if((! serverUtils.isSysAdmin(con, stmt, rs, unm, dnm, sid, uty, localDefnsDir, defnsDir)) && (! serverUtils.isDBAdmin(con, stmt, rs, unm)))
    {
      messagePage.msgScreen(true, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "AdminDocumentSettings", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7019, bytesOut[0], 0, "ACC:");
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(true, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "AdminDocumentSettings", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7019, bytesOut[0], 0, "SID:");
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }
        
    set(con, stmt, rs, out, unm, sid, uty, men, den, dnm, bnm, imagesDir, defnsDir, bytesOut);
    
    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 7071, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String imagesDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Definitions: Document Settings</title>");
   
    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    scoutln(out, bytesOut, "function update(){document.forms[0].submit();}");

    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    signOnAdministrator.heading(out, true, unm, sid, uty, bnm, dnm, men, den, imagesDir, bytesOut);

    dashboardUtils.drawTitle(out, "Document Settings", "7019", unm, sid, uty, men, den, dnm, bnm, bytesOut);

    scoutln(out, bytesOut, "<form action='AdminDocumentSettingsUpdate' enctype=\"application/x-www-form-urlencoded\" method=POST>");
    scoutln(out, bytesOut, "<input type=hidden name=unm value="+unm+">");
    scoutln(out, bytesOut, "<input type=hidden name=sid value="+sid+">");
    scoutln(out, bytesOut, "<input type=hidden name=uty value="+uty+">");
    scoutln(out, bytesOut, "<input type=hidden name=men value=\""+men+"\">");
    scoutln(out, bytesOut, "<input type=hidden name=den value="+den+">");
    scoutln(out, bytesOut, "<input type=hidden name=dnm value="+dnm+">");
    scoutln(out, bytesOut, "<input type=hidden name=bnm value="+bnm+">");

    scoutln(out, bytesOut, "<table id='page' cellspacing=2 cellpadding=2 width=100%>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    String[][] options = new String[1][50];

    definitionTables.getDocumentSettings(con, stmt, rs, options);

    drawCostingMethod(out, "Inventory Costing Method",                                    options[0][0],            bytesOut);
    
    draw(out, "Unit of Weight",                                                      "1", options[0][1], "Kgs", 10, bytesOut);

    drawPrinters(out, "Picking List Queue Default Printer",                               options[0][2],            bytesOut);

    draw(out, "Decimal places on unit prices",                                       "3", options[0][3],  "2",   2, bytesOut);
    draw(out, "Decimal places on stock quantities",                                  "4", options[0][4],  "0",   2, bytesOut);
    draw(out, "Enquiry: Number of lines on printout",                                "5", options[0][5],  "24",  3, bytesOut);
    draw(out, "Quotation: Number of lines on one-page printout",                     "6", options[0][6],  "24",  3, bytesOut);
    draw(out, "Quotation: Number of lines on first page of multipage printout",      "7", options[0][7],  "28",  3, bytesOut);
    draw(out, "Quotation: Number of lines on subsequent page of multipage printout", "8", options[0][8],  "30",  3, bytesOut);
    draw(out, "Quotation: Number of lines on last page of multipage printout",       "9", options[0][9],  "24",  3, bytesOut);
    draw(out, "Sales Order: Number of lines on printout",                           "10", options[0][10], "24",  3, bytesOut);
    draw(out, "Sales Order Acknowledgement: Number of lines on printout",           "11", options[0][11], "24",  3, bytesOut);
    draw(out, "Sales Order Confirmation: Number of lines on printout",              "12", options[0][12], "24",  3, bytesOut);
    draw(out, "Picking List: Number of lines on printout",                          "13", options[0][13], "24",  3, bytesOut);
    draw(out, "Packing List: Number of lines on printout",                          "14", options[0][14], "24",  3, bytesOut);
    draw(out, "Delivery Order: Number of lines on printout",                        "15", options[0][15], "24",  3, bytesOut);
    draw(out, "Sales Invoice: Number of lines on printout",                         "16", options[0][16], "24",  3, bytesOut);
    draw(out, "Sales Credit Note: Number of lines on printout",                     "17", options[0][17], "24",  3, bytesOut);
    draw(out, "Sales Debit Note: Number of lines on printout",                      "18", options[0][18], "24",  3, bytesOut);
    draw(out, "Proforma Invoice: Number of lines on printout",                      "19", options[0][19], "24",  3, bytesOut);
    draw(out, "Works Order: Number of lines on printout",                           "20", options[0][20], "24",  3 ,bytesOut);
    draw(out, "Purchase Order: Number of lines on printout",                        "21", options[0][21], "24",  3, bytesOut);
    draw(out, "Local Purchase Requisition: Number of lines on printout",            "22", options[0][22], "24",  3, bytesOut);
    draw(out, "Goods Received Note: Number of lines on printout",                   "23", options[0][23], "24",  3, bytesOut);
    draw(out, "Stock Authorization Note: Number of lines on printout",              "24", options[0][24], "24",  3, bytesOut);
    draw(out, "Receipt: Number of lines on printout",                               "25", options[0][25], "24",  3, bytesOut);
    draw(out, "Payment: Number of lines on printout",                               "26", options[0][26], "24",  3, bytesOut);
    draw(out, "Payment Voucher: Number of lines on printout",                       "27", options[0][27], "24",  3, bytesOut);
    draw(out, "Statement of Account: Number of lines on printout",                  "28", options[0][28], "24",  3, bytesOut);

    
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td><p><a href=\"javascript:update()\">Update</a></td></tr>");
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "</table></form></body></html>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void drawCostingMethod(PrintWriter out, String desc, String value, int[] bytesOut) throws Exception
  {
    if(value.length() == 0)
      value = "WAC";

    scout(out, bytesOut, "<tr><td><select name='option0'>");

    scout(out, bytesOut, "<option value='none'");
    if(value.equals("none"))
      scout(out, bytesOut, " selected");
    scoutln(out, bytesOut, ">None");

    scout(out, bytesOut, "<option value='WAC'");
    if(value.equals("WAC"))
      scout(out, bytesOut, " selected");
    scoutln(out, bytesOut, ">Weighted Average Cost");

    scoutln(out, bytesOut, "</select></td>");

    scoutln(out, bytesOut, "<td nowrap><p>&nbsp;&nbsp;" + desc + "</td></tr>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void drawPrinters(PrintWriter out, String desc, String value, int[] bytesOut) throws Exception
  {
    scout(out, bytesOut, "<tr><td><select name='option2'>");

    try
    {
      Process p;
      BufferedReader reader;

      Runtime r = Runtime.getRuntime();

      String commandArray = "/opt/csw/bin/lpstat -a";
      p = r.exec(commandArray);

      p.waitFor();

      InputStreamReader isr = new InputStreamReader(p.getInputStream());
      reader = new BufferedReader(isr);

      int x, len;
      String ptr, name;
      while((ptr = reader.readLine()) != null)
      {
        if(ptr.charAt(0) == ' '|| ptr.charAt(0) == '\t')
          ;
        else
        {
          x=0;
          len = ptr.length();
          while(x < len && ptr.charAt(x) != ' ') // just-in-case
            ++x;

          name = ptr.substring(0, x);
          scout(out, bytesOut, "<option value='" + name + "'");
          if(value.equals(name))
            scout(out, bytesOut, " selected");
          scoutln(out, bytesOut, ">" + name);
        }
      }
      reader.close();
      isr.close();
    }
    catch(Exception e)
    {
      System.out.println("7019: " + e);
    }

    scoutln(out, bytesOut, "</select></td>");

    scoutln(out, bytesOut, "<td nowrap><p>&nbsp;&nbsp;" + desc + "</td></tr>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void draw(PrintWriter out, String desc, String option, String value, String defaultValue, int size, int[] bytesOut) throws Exception
  {
    if(value.length() == 0)
      value = defaultValue;
    
    scout(out, bytesOut, "<tr><td><input type=text name='option" + option + "' value='" + value + "' size='" + size + "'></td>");
    
    scoutln(out, bytesOut, "<td nowrap><p>&nbsp;&nbsp;" + desc + "</td></tr>");
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
    bytesOut[0] += str.length() + 2;    
  }

}
