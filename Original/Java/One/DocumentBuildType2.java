// =======================================================================================================================================================================================================
// System: ZaraStar DocumentEngine: Build doc from doc (type 2)
// Module: DocumentBuildType2.java
// Author: C.K.Harvey
// Copyright (c) 2001-08 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.sql.*;

public class DocumentBuildType2 extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  DrawingUtils drawingUtils = new DrawingUtils();
  PickingList pickingList = new PickingList();
  DeliveryOrder deliveryOrder = new DeliveryOrder();
  MiscDefinitions miscDefinitions = new MiscDefinitions();
  
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", men="", den="", uty="", dnm="", bnm="", p1="", p2="", p3="", p4="", p5="";

    try
    {
      directoryUtils.setContentHeaders2(res);
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
        if(name.equals("p1")) // sourceDocCode
          p1 = value;
        else
        if(name.equals("p2")) // serviceCode
          p2 = value;
        else
        if(name.equals("p3")) // targetDocName
          p3 = value;
        else
        if(name.equals("p4")) // sourceDocName
          p4 = value;
        else
        if(name.equals("p5")) // returnServlet
          p5 = value;
      }
  
      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, p3, p4, p5, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "DocumentBuildType2", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6097, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                    String p1, String p2, String p3, String p4, String p5, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');
    
    Connection con = null;
    Statement stmt = null;
    ResultSet rs   = null;
    
    try
    {
      String uName    = directoryUtils.getMySQLUserName();
      String passWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();
    
      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + uName + "&password=" + passWord);
 
      if(! authenticationUtils.verifyAccess(con, stmt, rs, req, generalUtils.strToInt(p2), unm, uty, dnm, localDefnsDir, defnsDir))
      {
        messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "6097", imagesDir, localDefnsDir, defnsDir, bytesOut);
        serverUtils.etotalBytes(req, unm, dnm, 6097, bytesOut[0], 0, "ACC:" + p1);
      
        if(con  != null) con.close();
        if(out != null) out.flush();
        return;
      }
    
      if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
      {
        messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "6097", imagesDir, localDefnsDir, defnsDir, bytesOut);
        serverUtils.etotalBytes(req, unm, dnm, 6097, bytesOut[0], 0, "SID:" + p1);
      
        if(con  != null) con.close();
        if(out != null) out.flush();
        return;
      }

      process(con, stmt, rs, out, req, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, p1, p2, p3, p4, p5, bytesOut);
    }
    catch(Exception e) { }

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 6097, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
        
  if(con  != null) con.close();

    if(out != null) out.flush();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void process(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm,
                       String bnm, String localDefnsDir, String defnsDir, String sourceDocCode, String serviceCode, String targetDocName,
                       String sourceDocName, String returnServlet, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Document Build</title>");

    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir)
                         + "general.css\">");

    scoutln(out, bytesOut, "<script javascript=\"JavaScript\">");

    scoutln(out, bytesOut, "function build(){document.lines.submit();}");

    scoutln(out, bytesOut, "</script>");

    scoutln(out, bytesOut, "<form action=\"DocumentBuildType2Execute\" name=lines enctype=\"application/x-www-form-urlencoded\" method=POST>");
    scoutln(out, bytesOut, "<input type=hidden name=unm value=" + unm + ">");
    scoutln(out, bytesOut, "<input type=hidden name=sid value=" + sid + ">");
    scoutln(out, bytesOut, "<input type=hidden name=uty value=" + uty + ">");
    scoutln(out, bytesOut, "<input type=hidden name=men value=" + men + ">");
    scoutln(out, bytesOut, "<input type=hidden name=den value=" + den + ">");
    scoutln(out, bytesOut, "<input type=hidden name=dnm value=" + dnm + ">");
    scoutln(out, bytesOut, "<input type=hidden name=bnm value=" + bnm + ">");
    scoutln(out, bytesOut, "<input type=hidden name=p1  value=" + sourceDocCode + ">");
    scoutln(out, bytesOut, "<input type=hidden name=p2  value=" + serviceCode + ">");

    byte[] linesData  = new byte[2000];
    int[]  listLen    = new int[1];  listLen[0] = 2000;
    int[]  linesCount = new int[1];

    byte[] b = new byte[21];
    generalUtils.strToBytes(b, sourceDocCode);

    short lineField=0, entryField=0, descField=0, itemCodeField=0, mfrField=0, mfrCodeField=0;
    boolean isASalesOrder = false;
    String soCode="";

    if(serviceCode.equals("4058"))
    { 
      SalesOrder salesOrder = new SalesOrder();
      linesData = salesOrder.getLines(con, stmt, rs, b, '\000', linesData, listLen, linesCount, dnm, localDefnsDir, defnsDir);
      lineField  = 22; // origin-1
      entryField = 23; // origin-1
      descField  = 3;  // origin-1
      itemCodeField = 2;  // origin-1
      mfrField      = 30;  // origin-1
      mfrCodeField  = 31;  // origin-1
      isASalesOrder = true;
      soCode = generalUtils.stringFromBytes(b, 0L);
    }
      
    scoutln(out, bytesOut, "<input type=hidden name=p5 value=" + linesCount[0] + ">");

    int[] hmenuCount = new int[1];

    drawingUtils.outputPageFrame(con, stmt, rs, out, req, "6097", "", "DocumentBuildType2", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount, bytesOut);

    drawingUtils.drawTitle(out, false, false, "DocumentBuildType2", "", "Build " + targetDocName + " from " + sourceDocName, "6097", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
      
    scoutln(out, bytesOut, "<table id=\"page\" width=100%>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap colspan=2><p><input type=checkbox name=renumberLines checked>Renumber All Lines</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    
    scoutln(out, bytesOut, "<tr><td nowrap colspan=2><p><input type=checkbox name=renumberEntries>Renumber All Entries</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    
    scoutln(out, bytesOut, "<tr><td colspan=4><p>Note: Only items with a <i>Picked</i> quantity greater-than 0 are converted.</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    
    drawListingHeader(out, targetDocName, bytesOut);
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "</table>");

    scoutln(out, bytesOut, "<table id=\"page\" cellspacing=2 cellpadding=0 width=100%>");
//    scoutln(out, bytesOut, "<tr id=\"pageColumn\"><td><p><b>Picked</td><td><p><b>Required</td>");
    scoutln(out, bytesOut, "<tr id=\"pageColumn\"><td><p><b>Delivered</td><td><p><b>Picked</td><td><p><b>Required</td>");
    scoutln(out, bytesOut, "<td><p><b>Line</td><td><p><b>Entry</td><td><p><b>ItemCode</td><td><p><b>Manufacturer</td>"
                         + "<td><p><b>Manufacturer Code</td><td><p><b>Description</td></tr>");

    char dpOnQuantities = miscDefinitions.dpOnQuantities(con, stmt, rs, "5");

    double amtReqd, amtPickedAlready, amtDeliveredAlready;
    String[] doCodes = new String[1];

    byte[] thisLine = new byte[1000]; // plenty
    String line, entry, desc, itemCode, mfr, mfrCode;//, quantitiesReqd="";
    for(int xx=0;xx<linesCount[0];++xx)
    {
      if(generalUtils.getListEntryByNum(xx, linesData, thisLine)) // just-in-case
      {
        generalUtils.replaceTwosWithNulls(thisLine);
        line     = generalUtils.dfsAsStr(thisLine, lineField);
        entry    = generalUtils.dfsAsStr(thisLine, entryField);
        desc     = generalUtils.dfsAsStr(thisLine, descField);
        itemCode = generalUtils.dfsAsStr(thisLine, itemCodeField);
        mfr      = generalUtils.dfsAsStr(thisLine, mfrField);
        mfrCode  = generalUtils.dfsAsStr(thisLine, mfrCodeField);

        if(isASalesOrder)
        {
          amtReqd = generalUtils.dfsAsDouble(thisLine, (short)5);
          
          amtPickedAlready = pickingList.getTotalPickedForASOLine(con, stmt, rs, soCode, line);

          amtDeliveredAlready = deliveryOrder.getTotalDeliveredForASOLine(con, stmt, rs, soCode, line);

          scoutln(out, bytesOut, "<tr><td><input type=text size=6 maxlength=20 name=" + line + " value='" + doubleToStr(amtDeliveredAlready, dpOnQuantities) + "'></td>");
          scoutln(out, bytesOut, "<td>" + doubleToStr(amtPickedAlready, dpOnQuantities) + "</td>");
          scoutln(out, bytesOut, "<td><p>" + doubleToStr(amtReqd, dpOnQuantities) + "</td><td><p>" + line + "</td><td><p>" + entry + "</td><td><p>" + itemCode + "</td>");
          scoutln(out, bytesOut, "<td><p>" + mfr + "</td><td nowrap><p> " + mfrCode + "</td><td nowrap><p> " + desc + "</td></tr>");          
        }
      }
    }  

    scoutln(out, bytesOut, "</table><table id=\"page\" width=100%>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    drawListingHeader(out, targetDocName, bytesOut);
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "</table></form>");     scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private String doubleToStr(double d, char dpOnQuantities) throws Exception
  {
    byte[] b = new byte[50];
    
    generalUtils.doubleToBytesCharFormat(d, b, 0);
    generalUtils.bytesDPsGivenSeparator(true, '\000', dpOnQuantities, b, 20, 0);
    
    return generalUtils.stringFromBytes(b, 0L);
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void drawListingHeader(PrintWriter out, String targetDocName, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<tr><td nowrap><p><p>&nbsp<a href=\"javascript:build()\">Build the " + targetDocName + "</a></td></tr>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }
  
}
