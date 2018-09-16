// =======================================================================================================================================================================================================
// System: ZaraStar: Prodcut: Cycle count - Generate
// Module: ProductCycleCountGenerate.java
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

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.sql.*;

public class ProductCycleCountGenerate extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  DrawingUtils drawingUtils = new DrawingUtils();
  Inventory inventory = new Inventory();
  InventoryAdjustment inventoryAdjustment = new InventoryAdjustment();
  DocumentUtils documentUtils = new DocumentUtils();
  DefinitionTables definitionTables = new DefinitionTables();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="";

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
      p1  = req.getParameter("p1"); // dateTo
      p2  = req.getParameter("p2"); // generateNew

      if(p1 == null) p1 = "";
      if(p2 == null) p2 = "N";

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "ProductCycleCountGenerate", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3083, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush(); 
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1, String p2, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');
    String workingDir    = directoryUtils.getUserDir('W', dnm, unm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null, stmt2 = null;
    ResultSet rs   = null, rs2   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 3083, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "3083a", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3083, bytesOut[0], 0, "ACC:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "3083a", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3083, bytesOut[0], 0, "SID:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    set(con, stmt, stmt2, rs, rs2, out, req, p1, p2, unm, sid, uty, men, den, dnm, bnm, workingDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 3083, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, HttpServletRequest req, String dateTo, String generateNew, String unm, String sid, String uty, String men, String den, String dnm,
                   String bnm, String workingDir, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Cycle Count Generate</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    if(generateNew.equals("N"))
    {
      // insert line
      scoutln(out, bytesOut, "var req3;");
      scoutln(out, bytesOut, "function initRequest3(url)");
      scoutln(out, bytesOut, "{if(window.XMLHttpRequest){req3=new XMLHttpRequest();}else");
      scoutln(out, bytesOut, "if(window.ActiveXObject){req3=new ActiveXObject(\"Microsoft.XMLHTTP\");}}");

      scoutln(out, bytesOut, "function affect(line,itemCode){var p1=sanitise(itemCode);");
      scoutln(out, bytesOut, "var url=\"http://" + men + "/central/servlet/ProductCycleCountInsert?unm=\" + escape('" + unm + "') + \"&sid=\" + escape('" + sid + "') + \"&uty=\" + escape('" + uty + "') + \"&men=\" + escape('" + men + "') + \"&den=\" + escape('"
                           + den + "') + \"&bnm=\" + escape('" + bnm + "') + \"&p1=\" + p1 + \"&dnm=\" + escape('" + dnm + "');");
      scoutln(out, bytesOut, "initRequest3(url);");
      scoutln(out, bytesOut, "req3.onreadystatechange=processRequest3;");
      scoutln(out, bytesOut, "req3.open(\"GET\",url,true);");
      scoutln(out, bytesOut, "req3.send(null);}");

      scoutln(out, bytesOut, "function processRequest3(){");
      scoutln(out, bytesOut, "if(req3.readyState==4){");
      scoutln(out, bytesOut, "if(req3.status == 200){");
      scoutln(out, bytesOut, "var res=req3.responseXML.getElementsByTagName(\"res\")[0].childNodes[0].nodeValue;");
      scoutln(out, bytesOut, "if(res.length > 0){");
      scoutln(out, bytesOut, "if(res=='.'){");
      scoutln(out, bytesOut, "var checkCode=req3.responseXML.getElementsByTagName(\"checkCode\")[0].childNodes[0].nodeValue;");
      scoutln(out, bytesOut, "var itemCode=req3.responseXML.getElementsByTagName(\"itemCode\")[0].childNodes[0].nodeValue;");

      scoutln(out, bytesOut, "var messageElement=document.getElementById('lc'+itemCode);messageElement.innerHTML+='<p>'+checkCode;");
      scoutln(out, bytesOut, "messageElement=document.getElementById('lq'+itemCode);messageElement.innerHTML+='<p><input type=text size=10 maxlength=10 name=q'+checkCode+'>';");
      scoutln(out, bytesOut, "messageElement=document.getElementById('ll'+itemCode);messageElement.innerHTML+='<p><input type=text size=20 maxlength=20 name=l'+checkCode+'>';");
      scoutln(out, bytesOut, "messageElement=document.getElementById('ld'+itemCode);messageElement.innerHTML+='<p><input type=text size=10 maxlength=10 name=d'+checkCode+'>';");

      scoutln(out, bytesOut, "}}}}}");

      if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3001, unm, uty, dnm, localDefnsDir, defnsDir))
      {
        scoutln(out, bytesOut, "function viewItem(code){var p1=sanitise(code);");
        scoutln(out, bytesOut, "this.location.href=\"/central/servlet/ProductStockRecord?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p1=\"+p1;}");
      }

      scoutln(out, bytesOut, "function update(){document.lines.submit();}");

      scoutln(out, bytesOut, "function sanitise(code){");
      scoutln(out, bytesOut, "var code2='';var x;var len=code.length;");
      scoutln(out, bytesOut, "for(x=0;x<len;++x)");
      scoutln(out, bytesOut, "if(code.charAt(x)=='#')code2+='%23';");
      scoutln(out, bytesOut, "else if(code.charAt(x)=='\"')code2+='%22';");
      scoutln(out, bytesOut, "else if(code.charAt(x)=='&')code2+='%26';");
      scoutln(out, bytesOut, "else if(code.charAt(x)=='%')code2+='%25';");
      scoutln(out, bytesOut, "else if(code.charAt(x)==' ')code2+='%20';");
      scoutln(out, bytesOut, "else if(code.charAt(x)=='?')code2+='%3F';");
      scoutln(out, bytesOut, "else code2+=code.charAt(x);");
      scoutln(out, bytesOut, "return code2};");
    }
    
    RandomAccessFile fhData  = generalUtils.create(workingDir + "3083.data");
    RandomAccessFile fhState = generalUtils.create(workingDir + "3083.state");
    generalUtils.fileClose(fhState);
    String stateFileName = workingDir + "3083.state";
    keepChecking(out, "3083", unm, sid, uty, men, den, dnm, bnm, bytesOut);

    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    int[] hmenuCount = new int[1];

    drawingUtils.outputPageFrame(con, stmt, rs, out, req, "3083", "", "ProductCycleCountGenerate", unm, sid, uty, men, den, dnm, bnm, " chkTimer(); ", localDefnsDir, defnsDir, hmenuCount, bytesOut);

    // determine year from today's date
    String today = generalUtils.todaySQLFormat(localDefnsDir, defnsDir);

    if(dateTo.length() == 0)
      dateTo = today;

    if(generateNew.equals("Y"))
      drawingUtils.drawTitle(out, false, false, "ProductStockLevelListing", "", "Cycle Count Generate up to " + dateTo, "3083", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
    else drawingUtils.drawTitle(out, false, false, "ProductStockLevelListing", "", "Cycle Count Items Already Generated up to " + dateTo, "3083", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<form action=\"ProductCycleCountInputb\" name=lines enctype=\"application/x-www-form-urlencoded\" method=POST>");
    scoutln(out, bytesOut, "<input type=hidden name=unm value=" + unm + ">");
    scoutln(out, bytesOut, "<input type=hidden name=sid value=" + sid + ">");
    scoutln(out, bytesOut, "<input type=hidden name=uty value=" + uty + ">");
    scoutln(out, bytesOut, "<input type=hidden name=men value=" + men + ">");
    scoutln(out, bytesOut, "<input type=hidden name=den value=" + den + ">");
    scoutln(out, bytesOut, "<input type=hidden name=dnm value=" + dnm + ">");
    scoutln(out, bytesOut, "<input type=hidden name=bnm value=" + bnm + ">");
    scoutln(out, bytesOut, "<input type=hidden name=dateTo value=" + dateTo + ">");

    scoutln(out, bytesOut, "<span id='stuff'><font size=7><br><br><br><br>1% complete, please wait...</font></span>");
    if(out != null) out.flush(); 
    out.close();

    scoutln(fhData, "<table id='page' border=0 cellspacing=0 cellpadding=2><tr id='pageColumn'><td><p>Date</td>");
    scoutln(fhData, "<td><p>Check Code &nbsp;</td>");
    scoutln(fhData, "<td><p>Item Code &nbsp;</td>");
    scoutln(fhData, "<td><p>Manufacturer &nbsp;</td>");
    scoutln(fhData, "<td><p>Manufacturer Code &nbsp;</td>");

    if(generateNew.equals("N"))
    {
      scoutln(fhData, "<td><p>Counted Level &nbsp;</td>");
      scoutln(fhData, "<td nowrap><p>Location &nbsp;</td>");
      scoutln(fhData, "<td nowrap><p>Date Counted &nbsp;</td>");
    }

    scoutln(fhData, "<td nowrap><p>Description &nbsp;</td></tr>");

    directoryUtils.updateState(stateFileName, "2");

    if(generateNew.equals("Y"))
    {
      int[] count = new int[1];  count[0] = 0;
      String[] cssFormat = new String[1];  cssFormat[0] = "";

      String yyyy = today.substring(0, 4);
      int year = generalUtils.strToInt(yyyy);

      String[] ignoreMondays        = new String[1];
      String[] ignoreTuesdays       = new String[1];
      String[] ignoreWednesdays     = new String[1];
      String[] ignoreThursdays      = new String[1];
      String[] ignoreFridays        = new String[1];
      String[] ignoreSaturdays      = new String[1];
      String[] ignoreSundays        = new String[1];
      String[] ignorePublicHolidays = new String[1];

      inventoryAdjustment.getCycleForYear(con, stmt, rs, yyyy, ignoreMondays, ignoreTuesdays, ignoreWednesdays, ignoreThursdays, ignoreFridays, ignoreSaturdays, ignoreSundays, ignorePublicHolidays);

      String[] financialYearStartMonth = new String[1];
      String[] financialYearEndMonth   = new String[1];

      definitionTables.getAppConfigFinancialYearMonths(con, stmt, rs, dnm, financialYearStartMonth, financialYearEndMonth);

      int startMonth = generalUtils.detMonthNumFromMonthName(financialYearStartMonth[0]);

      int startDateEncoded = generalUtils.encode(("01." + startMonth + "." + year), localDefnsDir, defnsDir);

      int dateToEncoded = generalUtils.encodeFromYYYYMMDD(dateTo);

      int totalNumItemsToCountPerDay = numItemsToCountPerDay(con, stmt, stmt2, rs, rs2, startMonth, year, startDateEncoded, ignoreMondays[0], ignoreTuesdays[0], ignoreWednesdays[0], ignoreThursdays[0], ignoreFridays[0], ignoreSaturdays[0],
                                                             ignoreSundays[0], ignorePublicHolidays[0], localDefnsDir, defnsDir);

      String[][] mfrs = new String[1][];

      int numMfrs = listOfMfrs(con, stmt, rs, year, mfrs);

      int theDate = startDateEncoded;

      short thisDay, thisMonth, thisYear;
      String dateInQuestion;

      int[] c1 = new int[1];  c1[0] = 1;
      int c2 = totalNumItemsToCountPerDay * (dateToEncoded - startDateEncoded);

      while(theDate <= dateToEncoded)
      {
        thisDay   = generalUtils.getDay(theDate);
        thisMonth = generalUtils.getMonth(theDate);
        thisYear  = generalUtils.getYear(theDate);

        if(dayIsNotIgnored(theDate, ignoreMondays[0], ignoreTuesdays[0], ignoreWednesdays[0], ignoreThursdays[0], ignoreFridays[0], ignoreSaturdays[0], ignoreSundays[0], ignorePublicHolidays[0], localDefnsDir, defnsDir))
        {
          dateInQuestion = (thisYear + "-" + thisMonth + "-" + thisDay);

          generateForADay(con, stmt, stmt2, rs, rs2, stateFileName, totalNumItemsToCountPerDay, mfrs[0], numMfrs, dateInQuestion, theDate, year, startDateEncoded, dateToEncoded, unm, c1, c2);

          updateDayCounted(con, stmt, dateInQuestion);
        }

        ++theDate;
      }
    }

    directoryUtils.updateState(stateFileName, "99");

    // output all stock check recs still set to 999999

    int numrecs = showRecs(con, stmt, rs, fhData, generateNew, dateTo, stateFileName);
    scoutln(fhData, "<tr><td colspan=5><p>Total of " + numrecs + " Records</td></tr>");

    scoutln(fhData, "<tr><td>&nbsp;</td></tr>");
    scoutln(fhData, "<tr><td>&nbsp;</td></tr>");

    if(generateNew.equals("N"))
      scoutln(fhData, "<tr><td nowrap colspan=4><p><a href=\"javascript:update()\">Update</a> Stock Check Records</td></tr>");

    scoutln(fhData, "</table></form>");
    scoutln(fhData, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir));

    generalUtils.fileClose(fhData);
    directoryUtils.updateState(stateFileName, "100");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean dayIsNotIgnored(int theDate, String ignoreMondays, String ignoreTuesdays, String ignoreWednesdays, String ignoreThursdays, String ignoreFridays, String ignoreSaturdays, String ignoreSundays, String ignorePublicHolidays,
                                  String localDefnsDir, String defnsDir)
  {
    try
    {
      String dow, aPH;
      int[] dayOfWeek = new int[1];
      int x, len = ignorePublicHolidays.length();

      dow = generalUtils.getDayOfWeek(generalUtils.decode(theDate, localDefnsDir, defnsDir), dayOfWeek, localDefnsDir, defnsDir);

      if(dow.equals("Monday") && ignoreMondays.equals("Y"))
        return false;

      if(dow.equals("Tuesday") && ignoreTuesdays.equals("Y"))
        return false;

      if(dow.equals("Wednesday") && ignoreWednesdays.equals("Y"))
        return false;

      if(dow.equals("Thursday") && ignoreThursdays.equals("Y"))
        return false;

      if(dow.equals("Friday") && ignoreFridays.equals("Y"))
        return false;

      if(dow.equals("Saturday") && ignoreSaturdays.equals("Y"))
        return false;

      if(dow.equals("Sunday") && ignoreSundays.equals("Y"))
        return false;

      x = 0;
      while(x < len)
      {
        aPH = "";
        while(x < len && ignorePublicHolidays.charAt(x) != ' ' && ignorePublicHolidays.charAt(x) != ',')
          aPH += ignorePublicHolidays.charAt(x++);

        while(x < len && (ignorePublicHolidays.charAt(x) == ' ' || ignorePublicHolidays.charAt(x) == ','))
          ++x;

        if(theDate == generalUtils.encode(aPH, localDefnsDir, defnsDir))
          return false;
      }
    }
    catch(Exception e) { return false; }

    return true;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void generateForADay(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String stateFileName, int totalNumItemsToCountPerDay, String[] mfrs, int numMfrs, String dateInQuestion, int dateInQuestionEncoded,
                               int year, int yearStartDate, int yearFinishDate, String unm, int[] c1, int c2) throws Exception
  {
    try
    {
      // skip this day if recs already exist

      stmt = con.createStatement();
      rs = stmt.executeQuery("SELECT COUNT(CheckCode) FROM stockc WHERE Level = '999999' AND Status != 'C' AND Date = {d '" + dateInQuestion + "'}");
      int numRecs = 0;
      if(rs.next())
        numRecs = rs.getInt(1);
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();

      if(numRecs > 0) // already generated
      {
        directoryUtils.updateState(stateFileName, "" + generalUtils.strDPs('0', ("" + generalUtils.doubleToStr((c1[0]++ / c2) * 100))));
        return;
      }
      // otherwise generate for this day

      int itemCount = 0, mfrCount = 0, freq;
      int[] mfrStartDate  = new int[1];
      int[] mfrFinishDate = new int[1];
      String thisMfr, itemCode;
      boolean quit = false;
      
      while(! quit) // itemCount < totalNumItemsToCountPerDay
      {
        directoryUtils.updateState(stateFileName, "" + generalUtils.strDPs('1', ("" + generalUtils.doubleToStr((c1[0]++ / c2) * 100))));

        thisMfr = mfrs[mfrCount];

        freq = startAndFinishDatesForAMfr(con, stmt, rs, thisMfr, year, dateInQuestionEncoded, yearStartDate, yearFinishDate, mfrStartDate, mfrFinishDate);

        if(freq > 0)
        {
          stmt = con.createStatement();

          rs = stmt.executeQuery("SELECT ItemCode FROM stock WHERE Manufacturer = '" + generalUtils.sanitiseForSQL(thisMfr) + "' AND CycleCountIgnore != 'Y' ORDER BY ManufacturerCode");

          while(! quit && rs.next())
          {
            itemCode = rs.getString(1);
          
            if(! stockCheckAlreadyInDateRange(con, stmt, rs, itemCode, mfrStartDate[0], mfrFinishDate[0]))
            {
              createTheStockCheckRec(con, stmt2, rs2, itemCode, dateInQuestion, unm);
              ++itemCount;
            }
          
            if(itemCount == totalNumItemsToCountPerDay)
            {
              if(rs   != null) rs.close();
              if(stmt != null) stmt.close();
            
              quit = true;
            }
          }

          if(! quit)
          {
            ++mfrCount;
            
            if(mfrCount > numMfrs) // all tried
              quit = true;
          }

          if(rs   != null) rs.close();
          if(stmt != null) stmt.close();
        }
        else
        {
          ++mfrCount;

          if(mfrCount > numMfrs) // all tried
            quit = true;
        }
      }
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int showRecs(Connection con, Statement stmt, ResultSet rs, RandomAccessFile fhData, String generateNew, String dateTo, String stateFileName) throws Exception
  {
    int count = 0;
    String[] mfr     = new String[1];
    String[] mfrCode = new String[1];
    String[] desc    = new String[1];

    try
    {
      String where = "";
      where = " AND Date <= {d '" + dateTo + "'}";

      stmt = con.createStatement();
      rs = stmt.executeQuery("SELECT COUNT(CheckCode) FROM stockc WHERE Status != 'C' AND Level = '999999' " + where + " ORDER BY Date, CheckCode");
      int numRecs = 0;
      if(rs.next())
        numRecs = rs.getInt(1);
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();

      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT CheckCode, ItemCode, Date FROM stockc WHERE Status != 'C' AND Level = '999999' " + where + " ORDER BY Date, CheckCode");

      String checkCode, itemCode, date, cssFormat = "";

      while(rs.next())
      {
        directoryUtils.updateState(stateFileName, "" + generalUtils.strDPs('1', ("" + generalUtils.doubleToStr((count / numRecs) * 100))));

        checkCode = rs.getString(1);
        itemCode  = rs.getString(2);
        date      = rs.getString(3);

        getItemDetailsGivenCode(con, stmt, rs, itemCode, mfr, mfrCode, desc);

        if(cssFormat.equals("line1")) cssFormat = "line2"; else cssFormat = "line1";

        scoutln(fhData, "<tr id='" + cssFormat + "'>");

        scoutln(fhData, "<td><p>" + generalUtils.convertFromYYYYMMDD(date) + "</td>");
        scoutln(fhData, "<td><p>" + checkCode + "</td>");
        scoutln(fhData, "<td><p><a href=\"javascript:viewItem('" + itemCode + "')\">" + itemCode + "</a></td>");
        scoutln(fhData, "<td><p>" + mfr[0] + "</td>");
        scoutln(fhData, "<td><p>" + mfrCode[0] + "</td>");

        if(generateNew.equals("N"))
        {
          scoutln(fhData, "<td><p><input type=text size=10 maxlength=10 name='q" + checkCode + "'></td>");
          scoutln(fhData, "<td><p><input type=text size=20 maxlength=20 name='l" + checkCode + "'></td>");
          scoutln(fhData, "<td><p><input type=text size=10 maxlength=10 name='d" + checkCode + "'></td>");
        }

        scoutln(fhData, "<td nowrap><p>" + desc[0] + "</td>");

        if(generateNew.equals("N"))
          scoutln(fhData, "<td><p><a href=\"javascript:affect('" + count + "','" + itemCode + "')\">Insert another Location</a></td></tr>");

        // insert hidden item after each line

        scoutln(fhData, "<tr><td></td><td><span id='lc" + itemCode + "'></span></td>");
        scoutln(fhData, "<td colspan=3></td><td><span id='lq" + itemCode + "'></span></td>");
        scoutln(fhData, "<td><span id='ll" + itemCode + "'></span></td>");
        scoutln(fhData, "<td><span id='ld" + itemCode + "'></span></td></tr>");

        ++count;
      }
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }

    return count;
  }

  //--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void getItemDetailsGivenCode(Connection con, Statement stmt, ResultSet rs, String itemCode, String[] mfr, String[] mfrCode, String[] desc) throws Exception
  {
    byte[] data = new byte[5000];

    if(inventory.getStockRecGivenCode(con, stmt, rs, itemCode, '\000', data) < 0) // just-in-case
    {
      mfr[0] = mfrCode[0] = desc[0] = "";
      return;
   }

    desc[0]    = generalUtils.dfsAsStr(data, (short)1);
    mfr[0]     = generalUtils.dfsAsStr(data, (short)3);
    mfrCode[0] = generalUtils.dfsAsStr(data, (short)4);

    if(desc[0]    == null) desc[0] = "";
    if(mfr[0]     == null) mfr[0] = "";
    if(mfrCode[0] == null) mfrCode[0] = "";

  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {
    out.println(str);
    bytesOut[0] += (str.length() + 2);
  }
  private void scoutln(RandomAccessFile fh, String str) throws Exception
  {
    fh.writeBytes(str + "\n");
  //  bytesOut[0] += (str.length() + 2);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void keepChecking(PrintWriter out, String servlet, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "function chkTimer(){chkTimerID=self.setTimeout('chk()',4000);}");

    scoutln(out, bytesOut, "var chkreq2;");
    scoutln(out, bytesOut, "function initRequest2(url)");
    scoutln(out, bytesOut, "{if(window.XMLHttpRequest){chkreq2=new XMLHttpRequest();}else");
    scoutln(out, bytesOut, "if(window.ActiveXObject){chkreq2=new ActiveXObject(\"Microsoft.XMLHTTP\");}}");

    scoutln(out, bytesOut, "function chk(){");
    scoutln(out, bytesOut, "var url=\"http://" + men + "/central/servlet/FaxStatusDataFromReportTemp?p1=" + servlet + "&unm=\" + escape('" + unm + "') + \"&sid=\" + escape('" + sid + "') + \"&uty=\" + escape('" + uty + "') + \"&men=\" + escape('" + men
                         + "') + \"&den=\" + escape('" + den + "') + \"&bnm=\" + escape('" + bnm + "') + \"&dnm=\" + escape('" + dnm + "');");
    scoutln(out, bytesOut, "initRequest2(url);");
    scoutln(out, bytesOut, "chkreq2.onreadystatechange=processRequest2;");
    scoutln(out, bytesOut, "chkreq2.open(\"GET\",url,true);");
    scoutln(out, bytesOut, "chkreq2.send(null);}");

    scoutln(out, bytesOut, "function processRequest2(){");
    scoutln(out, bytesOut, "if(chkreq2.readyState==4){");
    scoutln(out, bytesOut, "if(chkreq2.status==200){");
    scoutln(out, bytesOut, "var res=chkreq2.responseXML.getElementsByTagName(\"res\")[0].childNodes[0].nodeValue;");
    scoutln(out, bytesOut, "if(res.length>0){");
    scoutln(out, bytesOut, "if(res=='.')clearTimeout(chkTimerID);else chkTimer();");
    scoutln(out, bytesOut, "var s=chkreq2.responseXML.getElementsByTagName(\"stuff\")[0].childNodes[0].nodeValue;");
    scoutln(out, bytesOut, "var messageElement=document.getElementById('stuff');");
    scoutln(out, bytesOut, "messageElement.innerHTML=s;");
    scoutln(out, bytesOut, "}}}}");
  }
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int numItemsToCountPerDay(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, int startMonth, int year, int startDateEncoded, String ignoreMondays, String ignoreTuesdays, String ignoreWednesdays,
                                    String ignoreThursdays, String ignoreFridays, String ignoreSaturdays, String ignoreSundays, String ignorePublicHolidays, String localDefnsDir, String defnsDir) throws Exception
  {
    // determine num of countable days in the year

    int totalNumCountableDaysInTheYear = 0, numDaysInYear;

    if(startMonth > 2)
      numDaysInYear = generalUtils.numOfDaysInYear((short)++year);
    else numDaysInYear = generalUtils.numOfDaysInYear((short)year);

    int lastDayEncoded = startDateEncoded + numDaysInYear;

    int theDate = startDateEncoded;

    while(theDate <= lastDayEncoded)
    {
      if(dayIsNotIgnored(theDate, ignoreMondays, ignoreTuesdays, ignoreWednesdays, ignoreThursdays, ignoreFridays, ignoreSaturdays, ignoreSundays, ignorePublicHolidays, localDefnsDir, defnsDir))
        ++totalNumCountableDaysInTheYear;

      ++theDate;
    }

    // determine total num items to count

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT DISTINCT Manufacturer FROM stock ORDER BY Manufacturer");

    String mfr;
    int numItems = 0;

    while(rs.next())
    {
      mfr = rs.getString(1);

      numItems += (numItemsForAMfr(con, stmt2, rs2, mfr) * numTimesToCountForAMfr(con, stmt2, rs2, mfr, year));
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    // calc num items to count per day

    if(totalNumCountableDaysInTheYear == 0) // just-in-case
      return 0;

    return numItems / totalNumCountableDaysInTheYear;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int numItemsForAMfr(Connection con, Statement stmt, ResultSet rs, String mfr) throws Exception
  {
    int rowCount = 0;

    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT COUNT(ItemCode) AS rowcount FROM stock WHERE Manufacturer = '" + generalUtils.sanitiseForSQL(mfr) + "' AND CycleCountIgnore != 'Y'");

      if(rs.next())
        rowCount = rs.getInt("rowcount");
    }
    catch(Exception e)
    {
      System.out.println(e);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    return rowCount;
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int numTimesToCountForAMfr(Connection con, Statement stmt, ResultSet rs, String mfr, int year) throws Exception
  {
    String freq = "1";

    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT Frequency FROM cyclel WHERE Manufacturer = '" + generalUtils.sanitiseForSQL(mfr) + "' AND Year = '" + year + "'");

      if(rs.next())
        freq = rs.getString(1);
    }
    catch(Exception e)
    {
      System.out.println(e);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    return generalUtils.strToInt(freq);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int listOfMfrs(Connection con, Statement stmt, ResultSet rs, int year, String[][] mfrs) throws Exception
  {
    int mfrsSoFar = 0;

    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT COUNT(DISTINCT Manufacturer) FROM stock");

      int numMfrs = 0;

      if(rs.next())
        numMfrs = rs.getInt(1);

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();

      if(numMfrs == 0)
        return 0;

      mfrs[0] = new String[numMfrs];

      // check on the cycle definitions for the year

      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT Manufacturer FROM cyclel WHERE Year = '" + year + "' ORDER BY Priority, Manufacturer"); // priority = 'H' or 'L'

      while(rs.next())
        mfrsSoFar = addToMfrsList(rs.getString(1), mfrs[0], mfrsSoFar);

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();

      // check mfrs not on the cycle definitions

      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT DISTINCT Manufacturer FROM stock ORDER BY Manufacturer");

      while(rs.next())
        mfrsSoFar = addToMfrsList(rs.getString(1), mfrs[0], mfrsSoFar);

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }

    return mfrsSoFar;
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int addToMfrsList(String mfr, String[] mfrs, int mfrsSoFar)
  {
    int x = 0;
    while(x < mfrsSoFar)
    {
      if(mfrs[x].equals(mfr)) // already on list
        return mfrsSoFar;
      ++x;
    }

    // not on list
    mfrs[mfrsSoFar] = mfr;

    return ++mfrsSoFar;
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // deternmine begin and end days for this mfr based on frequency
  private int startAndFinishDatesForAMfr(Connection con, Statement stmt, ResultSet rs, String mfr, int year, int dateInQuestion, int yearStartDate, int yearFinishDate, int[] mfrStartDate, int[] mfrFinishDate) throws Exception
  {
    int freqI = 1;

    mfrStartDate[0]  = yearStartDate;
    mfrFinishDate[0] = yearFinishDate;

    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT Frequency FROM cyclel WHERE Year = '" + year + "' AND Manufacturer = '" + generalUtils.sanitiseForSQL(mfr) + "'");

      String freq = "1";

      if(rs.next())
        freq = rs.getString(1);

      // divide the counting-year into periods based on frequency
      // determine in which period the required date is

      int x;
      freqI = generalUtils.intFromStr(freq);

      if(freqI > 0)
      {
        int[] dateFrom = new int[freqI];
        int[] dateTo   = new int[freqI];

        int daysInCountPeriod = (yearFinishDate- yearStartDate) / freqI;

        for(x=0;x<freqI;++x)
        {
          dateFrom[x] = yearStartDate  + (daysInCountPeriod * x);
          dateTo[x]   = (yearStartDate + (daysInCountPeriod * x)) - 1;
        }

        for(x=0;x<freqI;++x)
        {
          if(dateInQuestion >= dateFrom[x] && dateInQuestion <= dateTo[x])
          {
            mfrStartDate[0]  = dateFrom[x];
            mfrFinishDate[0] = dateTo[x];
          }
        }
      }

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }

    return freqI;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean stockCheckAlreadyInDateRange(Connection con, Statement stmt, ResultSet rs, String itemCode, int mfrStartDate, int mfrFinishDate) throws Exception
  {
    int rowCount = 0;

    try
    {
      stmt = con.createStatement();
      rs = stmt.executeQuery("SELECT COUNT(CheckCode) FROM stockc WHERE ItemCode = '" + generalUtils.sanitiseForSQL(itemCode) + "' AND Status != 'C' AND Date >= {d '" + generalUtils.decodeToYYYYMMDD(mfrStartDate) + "'} AND Date <= {d '"
                           + generalUtils.decodeToYYYYMMDD(mfrFinishDate) + "'}");
      if(rs.next())
        rowCount = rs.getInt(1);

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }

    if(rowCount == 0)
      return false;

    return true;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void createTheStockCheckRec(Connection con, Statement stmt, ResultSet rs, String itemCode, String date, String unm) throws Exception
  {
    try
    {
      byte[] newCode = new byte[21];
      documentUtils.getNextCode(con, stmt, rs, "stockc", true, newCode);

      stmt = con.createStatement();

      String q = "INSERT INTO stockc ( CheckCode, ItemCode, StoreCode, Date, Level, Remark, Status, Reconciled, SignOn, Type, Location ) VALUES ('" + generalUtils.stringFromBytes(newCode, 0L) + "','" + generalUtils.sanitiseForSQL(itemCode) + "','',{d '" + date
               + "'},'999999','','L','N','" + unm + "','C','')";

      stmt.executeUpdate(q);

      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println("3083a: " + e);
      if(stmt != null) stmt.close();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void updateDayCounted(Connection con, Statement stmt, String date) throws Exception
  {
    try
    {
      stmt = con.createStatement();

      String q = "INSERT INTO cycled ( CountDate ) VALUES ( {d '" + date + "'} )";

      stmt.executeUpdate(q);

      if(stmt != null) stmt.close();
    }
    catch(Exception e) // if day already exists
    {
      if(stmt != null) stmt.close();
    }
  }
  
}
