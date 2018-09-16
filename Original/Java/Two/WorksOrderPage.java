// =======================================================================================================================================================================================================
// System: ZaraStar: DocumentEngine: WO Fetch rec page
// Module: WorksOrderPage.java
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
import java.io.*;
import java.sql.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class WorksOrderPage extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  WorksOrder worksOrder = new WorksOrder();
  MiscDefinitions  miscDefinitions = new MiscDefinitions();
  AccountsUtils  accountsUtils = new AccountsUtils();
  DashboardUtils dashboardUtils = new DashboardUtils();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="", p3="", p4="", p6="";

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
      p1  = req.getParameter("p1"); // code
      p2  = req.getParameter("p2"); // cad
      p3  = req.getParameter("p3"); // errStr
      p4  = req.getParameter("p4"); // dataAlready
      p6  = req.getParameter("p6"); // plain or not

      if(p1 == null) p1 = "";
      if(p2 == null) p2 = "A";
      if(p3 == null) p3 = "";
      if(p4 == null) p4 = "";
      if(p6 == null) p6 = "N";

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, p3, p4, p6, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "WorksOrderPage", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 4431, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush();
    }
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="", p3="", p4="", p6="";
    try
    {
      directoryUtils.setContentHeaders(res);
      out = res.getWriter();
      
      int len = req.getContentLength();
      ServletInputStream in  = req.getInputStream();
      byte[] b = new byte[len];
      
      in.readLine(b, 0, len);          
          
      String name, value;
      int x=0;
      while(x < len)
      {
        ++x; // & or ?
        name="";
        while(x < len && b[x] != '=')
          name += (char)b[x++];
        
        ++x; // =
        value="";
        while(x < len && b[x] != '&')
          value += (char)b[x++];
        value = generalUtils.deSanitise(value);
          
        if(name.equals("unm"))
          unm = value;
        else
        if(name.equals("sid"))
          sid = value;
        else
        if(name.equals("uty"))
          uty = value;
        else
        if(name.equals("men"))
          men = value;
        else
        if(name.equals("den"))
          den = value;
        else
        if(name.equals("dnm"))
          dnm = value;
        else
        if(name.equals("bnm"))
          bnm = value;
        else
        if(name.equals("p1")) // code
          p1 = value;
        else
        if(name.equals("p2")) // cad
          p2 = value;
        else
        if(name.equals("p3")) // errStr
          p3 = value;
        else
        if(name.equals("p4")) // dataAlready
          p4 = value;
        else
        if(name.equals("p6")) // plain or not
          p6 = value;
      }
      
      if(p1 == null) p1 = "";
      if(p2 == null) p2 = "A";
      if(p3 == null) p3 = "";
      if(p4 == null) p4 = "";
      if(p6 == null) p6 = "N";

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, p3, p4, p6, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "WorksOrderPage", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 4431, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush();
    }
  }
 
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1, String p2, String p3, String p4, String p6, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;
    Statement stmt2 = null;
    ResultSet rs2   = null;
    
    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 4431, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "4431", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 4431, bytesOut[0], 0, "ACC:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "4431", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 4431, bytesOut[0], 0, "SID:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    char dispOrEdit;
    if(p1.length() == 0)
      dispOrEdit = 'E';
    else dispOrEdit = 'D';

    byte[] dataAlready = new byte[5000];
    generalUtils.stringToBytes(p4, 0, dataAlready);

    byte[] codeB = new byte[21];

    p1 = generalUtils.stripLeadingAndTrailingSpaces(p1);
    p1 = p1.toUpperCase();
    
    // see if we have to create the record first
    boolean basedOn = false;
    String serviceCode="", sourceDocName="", returnServlet=""; 

    if(p1.startsWith("CREATEBASEDONSO:"))
    {
      p1 = p1.substring(16);
      serviceCode = "4034";
      returnServlet = "WorksOrderPage";
      sourceDocName = "Works Order";
      basedOn = true;
    }
    else generalUtils.strToBytes(codeB, p1);
        
    if(basedOn)
      buildRec(out, unm, sid, uty, men, den, dnm, bnm, p1, serviceCode, sourceDocName, returnServlet, localDefnsDir);
    else 
    {
      boolean plain = false;
      if(p6.equals("P"))
        plain = true;

      getRecToHTML(con, stmt, stmt2, rs, rs2, out, plain, dispOrEdit, codeB, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, p2.charAt(0), p3, dataAlready, req, bytesOut);
      scoutln(out, bytesOut, "</form>");
      scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir));
    }

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 4431, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con  != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void buildRec(PrintWriter out, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String code, String serviceCode, String sourceDocName, String returnServlet, String localDefnsDir) throws Exception
  {
    URL url = new URL("http://" + serverUtils.serverToCall("OFSA", localDefnsDir) + "/central/servlet/DocumentBuild");

    HttpURLConnection uc = (HttpURLConnection)url.openConnection();
    uc.setDoInput(true);
    uc.setDoOutput(true);
    uc.setUseCaches(false);
    uc.setDefaultUseCaches(false);
    uc.setRequestProperty("Content-Type", "application/x-www-form.urlencoded");

    String s2 = "&unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + generalUtils.sanitise(code) + "&p2=" + serviceCode + "&p5=" + returnServlet + "&p4="
              + generalUtils.sanitise(sourceDocName) + "&p3=Works%20Order";

    Integer len = new Integer(s2.length());
    uc.setRequestProperty("Content-Length", len.toString());
    
    uc.setRequestMethod("POST");

    PrintWriter p = new PrintWriter(uc.getOutputStream());
    p.print(s2);    
    p.flush();
    p.close();

    BufferedReader di = new BufferedReader(new InputStreamReader(uc.getInputStream()));
    String s = di.readLine();
    while(s != null)
    {
      out.println(s);
      s = di.readLine();
    }

    di.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean getRecToHTML(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, boolean plain, char dispOrEdit, byte[] code, String unm, String sid, String uty, String men, String den, String dnm,
                               String bnm, String localDefnsDir, String defnsDir, char cad, String errStr, byte[] dataAlready, HttpServletRequest req, int[] bytesOut) throws Exception
  {
    String imagesDir = directoryUtils.getSupportDirs('I');

    byte[] data        = new byte[5000];
    int dataLen = 5000;
    byte[] headData    = new byte[5000];
    int headDataLen = 5000;
    byte[] b           = new byte[300];

    boolean rtn=false;

    if(worksOrder.getRecGivenCode(con, stmt, rs, code, '\001', headData, dnm, localDefnsDir, defnsDir, bytesOut) == -1)
    {
      messagePage.msgScreen(false, out, req, 6, unm, sid, uty, men, den, dnm, bnm, "4431", imagesDir, localDefnsDir, defnsDir, bytesOut);
      return true;
    }

    String date;
    byte[] dateB = new byte[20]; // lock

    generalUtils.dfsGivenSeparator(true, '\001', headData, (short)1, dateB);
    date = generalUtils.stringFromBytes(dateB, 0L);

    prepend(con, stmt, rs, out, plain, unm, sid, uty, men, den, dnm, bnm, generalUtils.stringFromBytes(code, 0L), date, req, localDefnsDir, defnsDir, bytesOut);

    scoutln(out, bytesOut, "<table id='wopage' width=100%>");
    scoutln(out, bytesOut, "<tr><td>");

    displayHead(out, headData,headDataLen, bytesOut);

    char dpOnQuantities = miscDefinitions.dpOnQuantities(con, stmt, rs, "5");

    byte[] multipleLinesData = new byte[1000];
    int[]  multipleListLen = new int[1]; multipleListLen[0] = 1000;
    int[]  multipleLinesCount = new int[1];
    byte[] llData = new byte[200];
    multipleLinesData = worksOrder.getMultipleLines(con, stmt, rs, code, multipleLinesData, multipleListLen, multipleLinesCount);

    byte[] line      = new byte[20];
    byte[] linesData = new byte[5000];
    int[]  listLen = new int[1];  listLen[0] = 5000;
    int[]  linesCount = new int[1];
    linesData = worksOrder.getLines(con, stmt, rs, code, '\001', linesData, listLen, linesCount, dnm, localDefnsDir, defnsDir);

    scoutln(out, bytesOut, "<table border=0 width=100%>");
    displayLineHeading(out, bytesOut);

    for(int x=0;x<linesCount[0];++x)
    {
      if(generalUtils.getListEntryByNum(x, linesData, data)) // just-in-case
      {
        generalUtils.replaceTwosWithOnes(data);

        generalUtils.bytesDPsGivenSeparator(true, '\001', dpOnQuantities, data, dataLen, 5);  // quantity (origin-1)

        generalUtils.dfsGivenSeparator(true, '\001', data, (short)12, line); // origin-1

        for(int xx=0;xx<multipleLinesCount[0];++xx)
        {
          if(generalUtils.getListEntryByNum(xx, multipleLinesData, llData)) // just-in-case
          {
            generalUtils.replaceTwosWithOnes(llData);

            generalUtils.dfsGivenBinary1(false, llData, (short)2, b); // entry

            if(generalUtils.matchIgnoreCase(line, 0, b, 0))
            {
              generalUtils.dfsGivenBinary1(false, llData, (short)4, b); // text
              generalUtils.appendAlphaGivenBinary1(data, dataLen, 3, b, "<br>");
            }
          }
        }

        displayLine(out, data, dataLen, bytesOut);
      }
    }
    displayLineHeading(out, bytesOut);

    String attachments = worksOrder.getAttachments(con, stmt, stmt2, rs, rs2, generalUtils.stringFromBytes(code, 0L), unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir);
    if(attachments.length() == 0)
      attachments= "none";
    scoutln(headData, bytesOut, "wo.Attachments=" + attachments + "\001");

    // convert date
    generalUtils.dfsGivenSeparator(true, '\001', headData, (short)1, b);
    generalUtils.convertFromYYYYMMDD(b);
    generalUtils.repAlphaGivenSeparator('\001', headData, headDataLen, (short)1, b);

    scoutln(out, bytesOut, "<table border=0 cellpadding=0>");
    displaySummary(out, headData, headDataLen, bytesOut);
    scoutln(out, bytesOut, "</table>");

    scoutln(out, bytesOut, "</table></td></tr>");

    scoutln(out, bytesOut, "</td></tr></table>");

    rtn = true;

    return rtn;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void displayHead(PrintWriter out, byte[] data, int dataLen, int[] bytesOut) throws Exception
  {
    String companyCode = worksOrder.searchDataString(data, dataLen, "wo", "CompanyCode");
    String companyName = worksOrder.searchDataString(data, dataLen, "wo", "CompanyName");
    String date        = worksOrder.searchDataString(data, dataLen, "wo", "woDate");
    String woCode      = worksOrder.searchDataString(data, dataLen, "wo", "WOCode");
    String soCode      = worksOrder.searchDataString(data, dataLen, "wo", "SOCode");
    String fao         = worksOrder.searchDataString(data, dataLen, "wo", "FAO");
    String projectCode = worksOrder.searchDataString(data, dataLen, "wo", "ProjectCode");
    String salesPerson = worksOrder.searchDataString(data, dataLen, "wo", "SalesPerson");
    String status      = worksOrder.searchDataString(data, dataLen, "wo", "Status");

    scoutln(out, bytesOut, "<table width=100% border=0>");
    scoutln(out, bytesOut, "<tr>");
    scoutln(out, bytesOut, "<td><span class='name'>&nbsp; WORKS ORDER: &nbsp; " + woCode + " &nbsp;</td>");
    scoutln(out, bytesOut, "<td align=right>");

    if(status.equals("C"))
      scout(out, bytesOut, "<span class='textRed'>CANCELLED</span>");

    scoutln(out, bytesOut, "</td></tr>");

    scoutln(out, bytesOut, "</table>");

    scoutln(out, bytesOut, "<table border=0 cellpadding=0>");
    scoutln(out, bytesOut, "<tr>");
    scoutln(out, bytesOut, "<td><span class='name'>&nbsp; Customer: " + companyCode + " - " + companyName + "</span></td>");
    scoutln(out, bytesOut, "</table>");

    scoutln(out, bytesOut, "<table border=0 width=100%>");
    scoutln(out, bytesOut, "<tr><td><font size=1>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr class='heading'>");
    scoutln(out, bytesOut, "<td align=center><span class='heading'>DATE</span></td>");
    scoutln(out, bytesOut, "<td align=center><span class='heading'>ATTENTION</span></td>");
    scoutln(out, bytesOut, "<td align=center><span class='heading'>SO CODE</span></td>");
    scoutln(out, bytesOut, "<td align=center><span class='heading'>PROJECT CODE</span></td>");
    scoutln(out, bytesOut, "<td align=center><span class='heading'>SALES PERSON</span></td>");
    scoutln(out, bytesOut, "</tr>");

    scoutln(out, bytesOut, "<tr><td></td></tr>");

    scoutln(out, bytesOut, "<tr class='value'>");
    scoutln(out, bytesOut, "<td align=center><span class='value'>" + date + "</span></td>");
    scoutln(out, bytesOut, "<td align=center><span class='value'>" + fao + "</span></td>");
    scoutln(out, bytesOut, "<td align=center><span class='value'>" + soCode + "</span></td>");
    scoutln(out, bytesOut, "<td align=center><span class='value'>" + projectCode + "</span></td>");
    scoutln(out, bytesOut, "<td align=center><span class='value'>" + salesPerson + "</span></td>");
    scoutln(out, bytesOut, "</tr></table>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void displayLineHeading(PrintWriter out, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<tr class='heading'>");
    scoutln(out, bytesOut, "<td align=center><span class='heading'>LINE</span></td>");
    scoutln(out, bytesOut, "<td align=center><span class='heading'>ENTRY</span></td>");
    scoutln(out, bytesOut, "<td align=center><span class='heading'>QUANTITY</span></td>");
    scoutln(out, bytesOut, "<td align=center><span class='heading'>ITEM CODE</span></td>");
    scoutln(out, bytesOut, "<td align=center><span class='heading'>DESCRIPTION</span></td>");
    scoutln(out, bytesOut, "<td align=center><span class='heading'>MFR</span></td>");
    scoutln(out, bytesOut, "<td align=center><span class='heading'>MFR CODE</span></td>");
    scoutln(out, bytesOut, "</tr>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void displayLine(PrintWriter out, byte[] dataIn, int dataLen, int[] bytesOut) throws Exception
  {
    int x=0;
    while(x < dataLen && dataIn[x] != (byte)'\001')
      ++x;
    ++x;
    int y = 0;
    byte[] data = new byte[dataLen];
    while(x < dataLen)
      data[y++] = dataIn[x++];

    String line      = worksOrder.searchDataString(data, dataLen, "wol", "Line");
    String entry     = worksOrder.searchDataString(data, dataLen, "wol", "Entry");
    String qty       = worksOrder.searchDataString(data, dataLen, "wol", "Quantity");
    String uom       = worksOrder.searchDataString(data, dataLen, "wol", "UoM");
    String itemCode  = worksOrder.searchDataString(data, dataLen, "wol", "ItemCode");
    String desc      = worksOrder.searchDataString(data, dataLen, "wol", "Description");
    String mfr       = worksOrder.searchDataString(data, dataLen, "wol", "Manufacturer");
    String mfrCode   = worksOrder.searchDataString(data, dataLen, "wol", "ManufacturerCode");

    scoutln(out, bytesOut, "<tr style='height:1;'><td colspan=10><span id='l" + line + "'></td></tr>");

    scoutln(out, bytesOut, "<tr id='x" + line + "'>");
    scoutln(out, bytesOut, "<td valign=top align=center><span class='line'><a href=\"javascript:affect('" + line + "')\">" + line + "</a></td>");
    scoutln(out, bytesOut, "<td valign=top align=center><span class='line'><span id='x" + line + "entry'>" + entry + "</span></span></td>");
    scoutln(out, bytesOut, "<td valign=top align=center><span class='line'><span id='x" + line + "qty'>" + qty + " " + uom + "</span></span></td>");
    scoutln(out, bytesOut, "<td valign=top><span class='line'><span id='x" + line + "itemCode'>" + itemCode + "</span></span></td>");
    scoutln(out, bytesOut, "<td valign=top nowrap><span class='line'><span id='x" + line + "desc'>" + desc + "</span></span></td>");
    scoutln(out, bytesOut, "<td valign=top><span class='line'><span id='x" + line + "mfr'>" + mfr + "</span></span></td>");
    scoutln(out, bytesOut, "<td valign=top><span class='line'><span id='x" + line + "mfrCode'>" + mfrCode + "</span></span></td>");
    scoutln(out, bytesOut, "</tr>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void displaySummary(PrintWriter out, byte[] data, int dataLen, int[] bytesOut) throws Exception
  {
    String misc1       = worksOrder.searchDataString(data, dataLen, "wo", "Misc1");
    String misc2       = worksOrder.searchDataString(data, dataLen, "wo", "Misc2");
    String attachments = worksOrder.searchDataString(data, dataLen, "wo", "Attachments");
    String signOn      = worksOrder.searchDataString(data, dataLen, "wo", "SignOn");
    String dlm         = worksOrder.searchDataString(data, dataLen, "wo", "DateLastModified");

    scoutln(out, bytesOut, "<tr class='summary'><td><td colspan=2>" + misc1 + "</td></tr>");

    scoutln(out, bytesOut, "<tr class='summary'><td><td colspan=2>" + misc2 + "</td></tr>");

    scoutln(out, bytesOut, "<tr>");
    scoutln(out, bytesOut, "<td valign=top><span class='attachments'>Attachments:</span></td>");
    scoutln(out, bytesOut, "<td colspan=6><span class='attachments'>" + attachments + "</span></td>");
    scoutln(out, bytesOut, "</tr>");

    scoutln(out, bytesOut, "<tr class='summary'><td colspan=7><span class='modified'>Last Modified:" + signOn + " " + dlm + "</span></td></tr>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void javaScript(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, String code, String unm, String sid, String uty, String men, String den, String dnm, String bnm, byte[] b, String localDefnsDir, String defnsDir,
                          int[] bytesOut) throws Exception
  {
    b[0] = '\000';

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4025, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(b, bytesOut, "<script language=\"JavaScript\">");
      scoutln(b, bytesOut, "function affect(line){");

      scoutln(b, bytesOut, "window.location.replace(\"/central/servlet/QuotationLine?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + code + "&p2=\"+line+\"&p3=&p4=\");}</script>");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3001, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(b, bytesOut, "<script language=\"JavaScript\">");
      scoutln(b, bytesOut, "function stockRec(code){");
      scoutln(b, bytesOut, "window.location.href=\"/central/servlet/MainPageUtils1a?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+code;}</script>");
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void javaScriptCall(Connection con, Statement stmt, ResultSet rs, byte[] b, HttpServletRequest req, String date, String unm, String uty, String dnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    b[0]= '\000';

    scoutln(b, bytesOut, "_.lineFile=wol.line\001");
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4027, unm, uty, dnm, localDefnsDir, defnsDir) && serverUtils.passLockCheck(con, stmt, rs, "wo", date, unm))
      scoutln(b, bytesOut, "_.permitted=y\001");
    else scoutln(b, bytesOut, "_.permitted=n\001");

    scoutln(b, bytesOut, "_.stockRec=wol.itemcode\001");
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3001, unm, uty, dnm, localDefnsDir, defnsDir))
      scoutln(b, bytesOut, "_.stockPermitted=y\001");
    else scoutln(b, bytesOut, "_.stockPermitted=n\001");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void prepend(Connection con, Statement stmt, ResultSet rs, PrintWriter out, boolean plain, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String code, String date, HttpServletRequest req,
                       String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    String code2 = code;//generalUtils.sanitise(code);

//    b[0]= '\000';
    scoutln(out, bytesOut, "<html><head><title>" + code + "</title>");
    scoutln(out, bytesOut, "<script language=\"JavaScript\">\n");

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4025, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      // fetch line for edit
      scoutln(out, bytesOut, "var req2;");
      scoutln(out, bytesOut, "function initRequest2(url)");
      scoutln(out, bytesOut, "{if(window.XMLHttpRequest){req2=new XMLHttpRequest();}else");
      scoutln(out, bytesOut, "if(window.ActiveXObject){req2=new ActiveXObject(\"Microsoft.XMLHTTP\");}}");

      scoutln(out, bytesOut, "function affect(line){");
      scoutln(out, bytesOut, "var url=\"http://" + men + "/central/servlet/WorksOrderLineEdit?unm=\" + escape('" + unm + "') + \"&sid=\" + escape('" + sid + "') + \"&uty=\" + escape('" + uty + "') + \"&men=\" + escape('" + men + "') + \"&den=\" + escape('"
                           + den + "') + \"&bnm=\" + escape('" + bnm + "') + \"&p1=\" + escape('" + code + "') + \"&p2=\" + line + \"&dnm=\" + escape('" + dnm + "');");
      scoutln(out, bytesOut, "initRequest2(url);");
      scoutln(out, bytesOut, "req2.onreadystatechange=processRequest2;");
      scoutln(out, bytesOut, "req2.open(\"GET\",url,true);");
      scoutln(out, bytesOut, "req2.send(null);}");

      scoutln(out, bytesOut, "function processRequest2(){");
      scoutln(out, bytesOut, "if(req2.readyState==4){");
      scoutln(out, bytesOut, "if(req2.status == 200){");
      scoutln(out, bytesOut, "var res=req2.responseXML.getElementsByTagName(\"res\")[0].childNodes[0].nodeValue;");
      scoutln(out, bytesOut, "if(res.length > 0){");
      scoutln(out, bytesOut, "if(res=='.'){");
      scoutln(out, bytesOut, "var line=req2.responseXML.getElementsByTagName(\"line\")[0].childNodes[0].nodeValue;");
      scoutln(out, bytesOut, "var entry=req2.responseXML.getElementsByTagName(\"entry\")[0].childNodes[0].nodeValue;");
      scoutln(out, bytesOut, "var itemCode=req2.responseXML.getElementsByTagName(\"itemCode\")[0].childNodes[0].nodeValue;");
      scoutln(out, bytesOut, "var desc=req2.responseXML.getElementsByTagName(\"desc\")[0].childNodes[0].nodeValue;");
      scoutln(out, bytesOut, "var mfr=req2.responseXML.getElementsByTagName(\"mfr\")[0].childNodes[0].nodeValue;");
      scoutln(out, bytesOut, "var mfrCode=req2.responseXML.getElementsByTagName(\"mfrCode\")[0].childNodes[0].nodeValue;");
      scoutln(out, bytesOut, "var custItemCode=req2.responseXML.getElementsByTagName(\"custItemCode\")[0].childNodes[0].nodeValue;");
      scoutln(out, bytesOut, "var qty=req2.responseXML.getElementsByTagName(\"qty\")[0].childNodes[0].nodeValue;");
      scoutln(out, bytesOut, "var uom=req2.responseXML.getElementsByTagName(\"uom\")[0].childNodes[0].nodeValue;");
      scoutln(out, bytesOut, "var remark=req2.responseXML.getElementsByTagName(\"remark\")[0].childNodes[0].nodeValue;");

      scoutln(out, bytesOut, "var s='<td><table bgcolor=lightblue cellpadding=2 border=0><tr>"
                           + "<td><span class=name>&nbsp;&nbsp; Line &nbsp;&nbsp;' + line + ' &nbsp;&nbsp;</td>"
                           + "<td colspan=2><span class=summary><p>&nbsp;&nbsp;Entry &nbsp;&nbsp;<input type=text name=entry'+line+' size=6 maxlength=6 value=' + entry + '>"
                           + "&nbsp;&nbsp;ItemCode&nbsp;&nbsp;<input type=text name=itemCode'+line+' size=7 maxlength=20 value='+itemCode+'>"

                           + "&nbsp;&nbsp;Manufacturer &nbsp;&nbsp;<span id=mfr'+line+'></span>"

                           + "&nbsp;&nbsp;Mfr Code&nbsp;&nbsp;<input type=text name=mfrCode'+line+' size=30 maxlength=60 value=' + mfrCode + '></span></td></tr>"
                           
                           + "<tr><td valign=top><span class=summary>Description</td><td colspan=2><input name=desc'+line+' size=60 maxlength=170 value=' + desc + '></span></td></tr>"

                           + "<tr><td><span class=summary>Customer Item Code&nbsp;&nbsp;</td><td><span class=summary><input type=text name=custItemCode'+line+' value=' + custItemCode + '>&nbsp;&nbsp;Quantity &nbsp;&nbsp;"
                           + "<input type=text name=qty'+line+' size=6 maxlength=10 value=' + qty + '>&nbsp;&nbsp;Unit of Measure &nbsp;&nbsp;<input type=text name=uom'+line+' size=8 maxlength=10 value=' + uom + '></td></span></tr>"

                           + "<tr><td><span class=summary>Remark</td><td><p><input type=text name=remark'+line+' size=60 maxlength=80 value=' + remark + '></span></td></tr>"

                           + "<tr><td colspan=2 align=right nowrap><span class=summary><a href=\"javascript:doLine(\'+line+\',\\\'C\\\')\">Cancel</a> &nbsp;&nbsp;&nbsp; <a href=\"javascript:doLine(\'+line+\',\\\'U\\\')\">Update</a></span></td></tr>"

                           + "</table></td>';");

      scoutln(out, bytesOut, "getMfrsDDL(mfr,line);");

      scoutln(out, bytesOut, "var messageElement=document.getElementById('l'+line);");
      scoutln(out, bytesOut, "messageElement.style.visibility='visible';");
      scoutln(out, bytesOut, "messageElement.innerHTML=s;");
      scoutln(out, bytesOut, "messageElement=document.getElementById('x'+line);");
      scoutln(out, bytesOut, "messageElement.style.visibility='hidden';");
      scoutln(out, bytesOut, "messageElement.innerHTML='';");

      scoutln(out, bytesOut, "}}}}}");

      // update line to DB
      scoutln(out, bytesOut, "var req4;");
      scoutln(out, bytesOut, "function initRequest4(url)");
      scoutln(out, bytesOut, "{if(window.XMLHttpRequest){req4=new XMLHttpRequest();}else");
      scoutln(out, bytesOut, "if(window.ActiveXObject){req4=new ActiveXObject(\"Microsoft.XMLHTTP\");}}");

      scoutln(out, bytesOut, "function doLine(line,mode){");
      scoutln(out, bytesOut, "var x='entry=sanitise(document.forms[0].entry'+line+'.value)';eval(x);");
      scoutln(out, bytesOut, "x='itemCode=sanitise(document.forms[0].itemCode'+line+'.value)';eval(x);");
      scoutln(out, bytesOut, "x='desc=sanitise(document.forms[0].desc'+line+'.value)';eval(x);");
      scoutln(out, bytesOut, "x='mfr=sanitise(document.forms[0].mfr'+line+'.value)';eval(x);");
      scoutln(out, bytesOut, "x='mfrCode=sanitise(document.forms[0].mfrCode'+line+'.value)';eval(x);");
      scoutln(out, bytesOut, "x='custItemCode=sanitise(document.forms[0].custItemCode'+line+'.value)';eval(x);");
      scoutln(out, bytesOut, "x='qty=sanitise(document.forms[0].qty'+line+'.value)';eval(x);");
      scoutln(out, bytesOut, "x='uom=sanitise(document.forms[0].uom'+line+'.value)';eval(x);");
      scoutln(out, bytesOut, "x='remark=sanitise(document.forms[0].remark'+line+'.value)';eval(x);");
      scoutln(out, bytesOut, "var url=\"http://" + men + "/central/servlet/WorksOrderLineUpdate?unm=\" + escape('" + unm + "') + \"&sid=\" + escape('" + sid + "') + \"&uty=\" + escape('" + uty + "') + \"&men=\" + escape('" + men + "') + \"&den=\" + escape('"
                           + den + "') + \"&bnm=\" + escape('" + bnm + "') + \"&p1=\" + escape('" + code + "') + \"&p2=\" + line + \"&p3=\" + escape(entry) + \"&p4=\" + escape(itemCode) + \"&p5=\" + escape(desc) + \"&p6=\" + "
                           + "escape(mfr) + \"&p7=\" + escape(mfrCode) + \"&p8=\" + escape(custItemCode) + \"&p9=\" + escape(qty) + \"&p10=\" + escape(uom) + \"&p11=\" + escape(remark) + \"&p12=\" + mode + \"&dnm=\" + escape('" + dnm + "');");
      scoutln(out, bytesOut, "initRequest4(url);");
      scoutln(out, bytesOut, "req4.onreadystatechange = processRequest4;");
      scoutln(out, bytesOut, "req4.open(\"GET\",url,true);");
      scoutln(out, bytesOut, "req4.send(null);}");

      scoutln(out, bytesOut, "function processRequest4(){");
      scoutln(out, bytesOut, "if(req4.readyState==4){");
      scoutln(out, bytesOut, "if(req4.status==200){");
      scoutln(out, bytesOut, "var res=req4.responseXML.getElementsByTagName(\"res\")[0].childNodes[0].nodeValue;");
      scoutln(out, bytesOut, "if(res.length>0){");
      scoutln(out, bytesOut, "if(res=='.')");
      scoutln(out, bytesOut, "var line=req4.responseXML.getElementsByTagName(\"line\")[0].childNodes[0].nodeValue;");
      scoutln(out, bytesOut, "var entry=req4.responseXML.getElementsByTagName(\"entry\")[0].childNodes[0].nodeValue;");
      scoutln(out, bytesOut, "var itemCode=req4.responseXML.getElementsByTagName(\"itemCode\")[0].childNodes[0].nodeValue;");
      scoutln(out, bytesOut, "var desc=req4.responseXML.getElementsByTagName(\"desc\")[0].childNodes[0].nodeValue;");
      scoutln(out, bytesOut, "var mfrCode=req4.responseXML.getElementsByTagName(\"mfrCode\")[0].childNodes[0].nodeValue;");
      scoutln(out, bytesOut, "var qty=req4.responseXML.getElementsByTagName(\"qty\")[0].childNodes[0].nodeValue;");
      scoutln(out, bytesOut, "var uom=req4.responseXML.getElementsByTagName(\"uom\")[0].childNodes[0].nodeValue;");

      scoutln(out, bytesOut, "var messageElement=document.getElementById('l'+line);");
      scoutln(out, bytesOut, "messageElement.style.visibility='hidden';");
      scoutln(out, bytesOut, "messageElement.innerHTML='';");
      scoutln(out, bytesOut, "messageElement=document.getElementById('x'+line);");
      scoutln(out, bytesOut, "messageElement.style.visibility='visible';");

      scoutln(out, bytesOut, "var s='<td valign=top align=center><a href=\"javascript:affect(' + line + ')\">' + line + '</a></td>"
                           + "<td valign=top align=center><span class=line><span id=\\\'x\\\' + line + \\\'entry\\\'>' + entry + '</span></span></td>"
                           + "<td valign=top align=center><span class=line><span id=\\\'x\\\' + line + \\\'qty\\\'>' + qty + \' \' + uom + '</span></span></td>"
                           + "<td valign=top><span class=line><span id=\\\'x\\\' + line + \\\'itemCode\\\'>' + itemCode + '</span></span></td>"
                           + "<td valign=top nowrap><span class=line><span id=\\\'x\\\' + line + \\\'desc\\\'>' + desc + '</span></span></td>"
                           + "<td valign=top nowrap><span class=line><span id=\\\'x\\\' + line + \\\'mfr\\\'>' + mfr + '</span></span></td>"
                           + "<td valign=top><span class=line><span id=\\\'x\\\' + line + \\\'mfrCode\\\'>' + mfrCode + '</span></span></td>';");
      scoutln(out, bytesOut, "messageElement.innerHTML=s;");

      scoutln(out, bytesOut, "}else{var messageElement=document.getElementById('msg');");
      scoutln(out, bytesOut, "messageElement.innerHTML='<span id=\"textErrorLarge\">'+res+'</span>';");
      scoutln(out, bytesOut, "}}}}");

       // get mfrs DDL
      scoutln(out, bytesOut, "var req5;");
      scoutln(out, bytesOut, "function initRequest5(url)");
      scoutln(out, bytesOut, "{if(window.XMLHttpRequest){req5=new XMLHttpRequest();}else");
      scoutln(out, bytesOut, "if(window.ActiveXObject){req5=new ActiveXObject(\"Microsoft.XMLHTTP\");}}");

      scoutln(out, bytesOut, "function getMfrsDDL(mfr,line){var p1=sanitise(mfr);");
      scoutln(out, bytesOut, "var url=\"http://" + men + "/central/servlet/WorksOrderManufacturers?unm=\" + escape('" + unm + "') + \"&sid=\" + escape('" + sid + "') + \"&uty=\" + escape('" + uty + "') + \"&men=\" + escape('" + men + "') + \"&den=\" + escape('"
                           + den + "') + \"&bnm=\" + escape('" + bnm + "') + \"&p1=\" + escape(p1) + \"&p2=\" + escape(line) + \"&dnm=\" + escape('" + dnm + "');");
      scoutln(out, bytesOut, "initRequest5(url);");
      scoutln(out, bytesOut, "req5.onreadystatechange=processRequest5;");
      scoutln(out, bytesOut, "req5.open(\"GET\",url,true);");
      scoutln(out, bytesOut, "req5.send(null);}");

      scoutln(out, bytesOut, "function processRequest5(){");
      scoutln(out, bytesOut, "if(req5.readyState==4){");
      scoutln(out, bytesOut, "if(req5.status==200){");
      scoutln(out, bytesOut, "var res=req5.responseXML.getElementsByTagName(\"res\")[0].childNodes[0].nodeValue;");
      scoutln(out, bytesOut, "if(res.length>0){");
      scoutln(out, bytesOut, "if(res=='.')");
      scoutln(out, bytesOut, "var ddl=req5.responseXML.getElementsByTagName(\"ddl\")[0].childNodes[0].nodeValue;");
      scoutln(out, bytesOut, "var line=req5.responseXML.getElementsByTagName(\"line\")[0].childNodes[0].nodeValue;");

      scoutln(out, bytesOut, "var messageElement=document.getElementById('mfr'+line);");
      scoutln(out, bytesOut, "messageElement.innerHTML=ddl;");

      scoutln(out, bytesOut, "}else{var messageElement=document.getElementById('msg');");
      scoutln(out, bytesOut, "messageElement.innerHTML='<span id=\"textErrorLarge\">'+res+'</span>';");
      scoutln(out, bytesOut, "}}}}");
    }

    scoutln(out, bytesOut, "function sanitise(code){");
    scoutln(out, bytesOut, "var code2='';var x;var len=code.length;");
    scoutln(out, bytesOut, "for(x=0;x<len;++x)");
    scoutln(out, bytesOut, "if(code.charAt(x)=='#')code2+='%23';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='\"')code2+='%22';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='&')code2+='%26';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='%')code2+='%25';");
    scoutln(out, bytesOut, "else if(code.charAt(x)==' ')code2+='%20';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='+')code2+='%2b';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='?')code2+='%3F';");
    scoutln(out, bytesOut, "else code2+=code.charAt(x);");
    scoutln(out, bytesOut, "return code2};");

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4435, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function fetch(){");
      scoutln(out, bytesOut, "window.location.replace(\"/central/servlet/WorksOrderHeaderEdit?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + code2 + "&p2=A&p3=&p4=\");}\n");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4437, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function add(){");
      scoutln(out, bytesOut, "window.location.replace(\"/central/servlet/WorksOrderLine?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + code2 + "&p2=&p3=&p4=\");}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 1025, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function trace(){");
      scoutln(out, bytesOut, "window.location.replace(\"/central/servlet/DocumentTraceExecute?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + code2 + "&p2=Q\");}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 11800, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function trail(){");
      scoutln(out, bytesOut, "window.location.href=\"/central/servlet/TrailShow?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + code2 + "&p2=Q\";}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 6094, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function attach(){");
      scoutln(out, bytesOut, "window.location.replace(\"/central/servlet/DocumentAttachments?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + code2 + "&p2=woa\");}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 8016, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function mail(){");
      scoutln(out, bytesOut, "window.location.replace(\"/central/servlet/MailExternalUserCreate?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=WorksOrderPage&p2=" + code2 + "&p3=Quotation&bnm=" + bnm + "\");}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4028, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function print(){");
      scoutln(out, bytesOut, "window.location.href=\"/central/servlet/AdminPrintControl?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=QuotationPrint&p2=" + code2 + "&p3=\";}");
    }

    if(   authenticationUtils.verifyAccess(con, stmt, rs, req, 11200, unm, uty, dnm, localDefnsDir, defnsDir) && authenticationUtils.verifyAccess(con, stmt, rs, req, 4028, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function pdf(){");
      scoutln(out, bytesOut, "window.location.href=\"/central/servlet/PrintToPDFUtils?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p3=" + code2 + "&p1=0.000&p5=Quotation&p2=QuotationPrint\";}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 11300, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function csv(){");
      scoutln(out, bytesOut, "window.location.href=\"/central/servlet/SendToCSVUtils?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + generalUtils.sanitise(code2)
                           + "&p3=Quotation&p2=wol\";}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 12708, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function direct(){");
      scoutln(out, bytesOut, "window.location.href=\"/central/servlet/ChannelSendDirect?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + generalUtils.sanitise(code2)
                           + "&p2=Quotation&p3=Requested%20Quotation%20Sent (" + generalUtils.sanitise(code2)+ ")\";}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4180, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function fax(){");
      scoutln(out, bytesOut, "window.location.href=\"/central/servlet/FaxCreate?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + code2 + "&p2=Q&p3=QuotationPrint&p4=&p5=Quotation\";}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4033, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function so(){");
      scoutln(out, bytesOut, "var p1='CREATEBASEDONwo:'+'" + code + "';");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/SalesOrderPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty +"&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4056, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function dorder(){");
      scoutln(out, bytesOut, "var p1='CREATEBASEDONWO:'+'" + code + "';");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/DeliveryOrderPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty +"&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4022, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function wo(){");
      scoutln(out, bytesOut, "var p1='CREATEBASEDONWO:'+'" + code + "';");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/WorksOrderPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty +"&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    scoutln(out, bytesOut, "function plain(){");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/WorksOrderPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + code2 + "&p2=A&p3=&p4=&p6=P\";}\n");

    String cssDirectory = directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir);
    String cssDocumentDirectory = directoryUtils.getCssDocumentDirectory(con, stmt, rs, dnm);
    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + cssDirectory + "general.css\">\n");
    if(plain)
      scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"print\" href=\"" + cssDocumentDirectory + "documentPlain.css\">\n");
    else scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + cssDocumentDirectory + "document.css\">\n");

    outputPageFrame(con, stmt, rs, out, req, plain, ' ', ' ', "xid=woPage", "WorksOrderPage", code, date, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    scoutln(out, bytesOut, "<form>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void outputPageFrame(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, boolean plain, char newOrEdit, char cad, String bodyStr, String callingServlet, String customerCode, String date, String unm,
                               String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    int[] hmenuCount = new int[1];

    String subMenuText = dashboardUtils.buildSubMenuText(con, stmt, rs, req, "4431", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount);

    subMenuText += worksOrder.buildSubMenuText(con, stmt, rs, req, cad, newOrEdit, callingServlet, customerCode, date, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount);

    if(plain)
      scoutln(out, bytesOut, authenticationUtils.buildPlainScreen(con, stmt, rs, req, unm, sid, uty, men, den, dnm, bnm, "WorksOrderPage", subMenuText, hmenuCount[0], bodyStr, "", localDefnsDir, defnsDir));
    else
    {
      scoutln(out, bytesOut, authenticationUtils.buildScreen(con, stmt, rs, req, true, unm, sid, uty, men, den, dnm, bnm, "WorksOrderPage", subMenuText, hmenuCount[0], bodyStr, "", localDefnsDir, defnsDir));

      scoutln(out, bytesOut, "<table border=0><tr><td valign=top><div id='second' style=\"{margin-top:0px;}\"></div></td>");
      scoutln(out, bytesOut, "<td valign=top><div id='third' style=\"{margin-top:0px;margin-left:0px;}\"></div></td></tr></table>");
    }
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
  private void scoutln(byte[] b, int bytesOut[], String str) throws Exception
  {
    generalUtils.catAsBytes(str, 0, b, false);
    bytesOut[0] += (str.length() + 2);
  }

}
