// =======================================================================================================================================================================================================
// System: ZaraStar: DocumentEngine: SO Print
// Module: SalesOrderPrint.java
// Author: C.K.Harvey
// Copyright (c) 1999-2006 Christopher Harvey. All Rights Reserved.
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
import java.net.*;
import java.sql.*;

public class SalesOrderPrint extends HttpServlet
{
  GeneralUtils  generalUtils = new GeneralUtils();
  MessagePage  messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils  directoryUtils = new DirectoryUtils();
  ReportGeneration  reportGeneration = new ReportGeneration();
  SalesOrder salesOrder = new SalesOrder();
  MiscDefinitions  miscDefinitions = new MiscDefinitions();

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
      p1  = req.getParameter("p1");
    
      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "SalesOrderPrint", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 4040, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush();
    }
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men,
                      String den, String dnm, String bnm, String p1, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String reportsDir    = directoryUtils.getUserDir('R', dnm, unm);
    String workingDir    = directoryUtils.getUserDir('W', dnm, dnm + "/" + unm);
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

      stmt = con.createStatement();

      if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 4040, unm, uty, dnm, localDefnsDir, defnsDir))
      {
        messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "4040", "", "", "", imagesDir, localDefnsDir, defnsDir, bytesOut);
        serverUtils.etotalBytes(req, unm, dnm, 4040, bytesOut[0], 0, "ACC:" + p1);
        if(con  != null) con.close();
        if(out != null) out.flush();
        return;
      }

      if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
      {
        messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "4040", "", "", "", imagesDir, localDefnsDir, defnsDir, bytesOut);
        serverUtils.etotalBytes(req, unm, dnm, 4040, bytesOut[0], 0, "SID:" + p1);
        if(con  != null) con.close();
        if(out != null) out.flush();
        return;
      }

      byte[] code = new byte[21];
      generalUtils.strToBytes(code, p1);

      switch(generate(con, stmt, rs, code, dnm, unm, workingDir, reportsDir, localDefnsDir, defnsDir, bytesOut))
      {
        case -1 : // Definition File Not Found
                  messagePage.msgScreen(false, out, req, 17, unm, sid, uty, men, den, dnm, bnm, "4040", "", "", "", imagesDir, localDefnsDir, defnsDir, bytesOut);
                  break;
        case -2 : // cannot create report output file
                  messagePage.msgScreen(false, out, req, 18, unm, sid, uty, men, den, dnm, bnm, "4040", "", "", "", imagesDir, localDefnsDir, defnsDir, bytesOut);
                  break;
        default : // generated ok
                  display(out, unm, sid, uty, men, den, dnm, bnm);
                  break;
      }
    }
    catch(Exception e) { }

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 4040, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con  != null) con.close();

    if(out != null) out.flush();
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private int generate(Connection con, Statement stmt, ResultSet rs, byte[] code, String dnm, String unm, String workingDir, String reportsDir, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    byte[] b   = new byte[300];
    byte[] b2  = new byte[300];
    byte[] tmp = new byte[3000];

    reportGeneration.currFont = 1;
    reportGeneration.currPage = 1;
    reportGeneration.currDown = reportGeneration.currAcross = 0.0;

    reportGeneration.oBufLen = 30000;
    reportGeneration.oBuf = new byte[30000];
    reportGeneration.oPtr = 0;

    if((reportGeneration.fhO = reportGeneration.createNewFile((short)0, workingDir, localDefnsDir, defnsDir, reportsDir)) == null)
      return -2;

    if((reportGeneration.fhPPR = generalUtils.fileOpenD("206.ppr", localDefnsDir)) == null)
    {
      if((reportGeneration.fhPPR = generalUtils.fileOpenD("206.ppr", defnsDir)) == null)
      {
        generalUtils.fileClose(reportGeneration.fhO);
        return -1;
      }
    }

    reportGeneration.lastOperationPF = false;

    byte[] fldNames = new byte[10000];
    byte[] data     = new byte[4000];

    generalUtils.putAlpha(fldNames, 4000, (short)0,  "SO.SOCode");
    generalUtils.putAlpha(fldNames, 4000, (short)1,  "SO.Date");
    generalUtils.putAlpha(fldNames, 4000, (short)2,  "SO.CompanyCode");
    generalUtils.putAlpha(fldNames, 4000, (short)3,  "SO.CompanyName");
    generalUtils.putAlpha(fldNames, 4000, (short)4,  "SO.Address1");
    generalUtils.putAlpha(fldNames, 4000, (short)5,  "SO.Address2");
    generalUtils.putAlpha(fldNames, 4000, (short)6,  "SO.Address3");
    generalUtils.putAlpha(fldNames, 4000, (short)7,  "SO.Address4");
    generalUtils.putAlpha(fldNames, 4000, (short)8,  "SO.Address5");
    generalUtils.putAlpha(fldNames, 4000, (short)9,  "SO.PostCode");
    generalUtils.putAlpha(fldNames, 4000, (short)10, "SO.FAO");
    generalUtils.putAlpha(fldNames, 4000, (short)11, "SO.Misc1");
    generalUtils.putAlpha(fldNames, 4000, (short)12, "SO.Misc2");
    generalUtils.putAlpha(fldNames, 4000, (short)13, "SO.DateRange");
    generalUtils.putAlpha(fldNames, 4000, (short)14, "SO.Attention");
    generalUtils.putAlpha(fldNames, 4000, (short)15, "SOL.LineNumber");
    generalUtils.putAlpha(fldNames, 4000, (short)16, "SOL.ItemCode");
    generalUtils.putAlpha(fldNames, 4000, (short)17, "SOL.Description");
    generalUtils.putAlpha(fldNames, 4000, (short)18, "SOL.UnitPrice");
    generalUtils.putAlpha(fldNames, 4000, (short)19, "SOL.Quantity");
    generalUtils.putAlpha(fldNames, 4000, (short)21, "SOL.Amount");
    generalUtils.putAlpha(fldNames, 4000, (short)22, "SOL.GSTRate");
    generalUtils.putAlpha(fldNames, 4000, (short)23, "Items.Misc3");
    generalUtils.putAlpha(fldNames, 4000, (short)24, "SOL.AltItemCode");
    generalUtils.putAlpha(fldNames, 4000, (short)25, "SO.GSTTotal");
    generalUtils.putAlpha(fldNames, 4000, (short)26, "SO.TotalTotal");
    generalUtils.putAlpha(fldNames, 4000, (short)29, "SO.TotalWithoutGST");
    generalUtils.putAlpha(fldNames, 4000, (short)33, "SO.SignOn");
    generalUtils.putAlpha(fldNames, 4000, (short)35, "SO.NoteLine1");
    generalUtils.putAlpha(fldNames, 4000, (short)36, "SO.NoteLines");

    byte[] accountCompany = new byte[21];

    if(salesOrder.getRecGivenCode(con, stmt, rs, code, '\000', tmp, dnm, localDefnsDir, defnsDir, bytesOut) != -1L) // just-in-case
    {
      generalUtils.putAlpha(data, 4000, (short)0, code);
      generalUtils.dfs(tmp, (short)1,  b); generalUtils.putAlpha(data, 4000, (short)1,  b); // date
      generalUtils.dfs(tmp, (short)2,  b); generalUtils.putAlpha(data, 4000, (short)2,  b); // suppcode
      generalUtils.dfs(tmp, (short)3,  b); generalUtils.putAlpha(data, 4000, (short)3,  b); // suppname
      generalUtils.dfs(tmp, (short)4,  b); generalUtils.putAlpha(data, 4000, (short)4,  b); // address1
      generalUtils.dfs(tmp, (short)5,  b); generalUtils.putAlpha(data, 4000, (short)5,  b); // address2
      generalUtils.dfs(tmp, (short)6,  b); generalUtils.putAlpha(data, 4000, (short)6,  b); // address3
      generalUtils.dfs(tmp, (short)7,  b); generalUtils.putAlpha(data, 4000, (short)7,  b); // address4
      generalUtils.dfs(tmp, (short)8,  b); generalUtils.putAlpha(data, 4000, (short)8,  b); // address5
      generalUtils.dfs(tmp, (short)9,  b); generalUtils.putAlpha(data, 4000, (short)9,  b); // postcode
      generalUtils.dfs(tmp, (short)10, b); generalUtils.putAlpha(data, 4000, (short)10, b); // FAO
      generalUtils.dfs(tmp, (short)14, b); generalUtils.putAlpha(data, 4000, (short)11, b); // misc1
      generalUtils.dfs(tmp, (short)15, b); generalUtils.putAlpha(data, 4000, (short)12, b); // misc2

      generalUtils.dfs(tmp, (short)12, b); generalUtils.putAlpha(data, 4000, (short)14, b); // attention
      generalUtils.dfs(tmp, (short)16, b); generalUtils.putAlpha(data, 4000, (short)25, b); // gsttotal
      generalUtils.dfs(tmp, (short)17, b); generalUtils.putAlpha(data, 4000, (short)26, b); // totaltotal

      double d1 = generalUtils.dfsAsDouble(tmp, (short)16);
      double d2 = generalUtils.dfsAsDouble(tmp, (short)17);
      double totalSansGST = d2 - d1;
      generalUtils.repDoubleGivenSeparator('2', '\000', data, 4000, (short)29, totalSansGST);

      generalUtils.dfs(tmp, (short)22, b); generalUtils.putAlpha(data, 4000, (short)33, b); // signon

      generalUtils.dfs(tmp, (short)27, accountCompany);
    }
    else accountCompany[0] = '\000';

    reportGeneration.processControl(dnm, unm, localDefnsDir, defnsDir);
    reportGeneration.processSection("RH", (byte[])null, (byte[])null, (short)0, dnm, unm, localDefnsDir, defnsDir);
    reportGeneration.processSection("PH", data, fldNames, (short)33, dnm, unm, localDefnsDir, defnsDir);

    char dpOnQuantities = miscDefinitions.dpOnQuantities(con, stmt, rs, "5");
    byte[] itemCode = new byte[21];
    byte[] lineNumB = new byte[20];
    int lineNum, lineCount;

    // fetch lines data in one go
    byte[] linesData  = new byte[2000];
    int[]  listLen    = new int[1];  listLen[0] = 2000;
    int[]  linesCount = new int[1];
    linesData = salesOrder.getLines(con, stmt, rs, code, '\001', linesData, listLen, linesCount, dnm, localDefnsDir, defnsDir);

    byte[] multipleLinesData  = new byte[2000];
    int[]  multipleListLen    = new int[1];  listLen[0] = 2000;
    int[]  multipleLinesCount = new int[1];
    int llCount=0;
    if(linesCount[0] > 0) // get all the multiple lines for this document
      multipleLinesData = salesOrder.getMultipleLines(con, stmt, rs, code, multipleLinesData, multipleListLen, multipleLinesCount, dnm, localDefnsDir, defnsDir);

    for(int xx=0;xx<linesCount[0];++xx)
    {
      if(generalUtils.getListEntryByNum(xx, linesData, tmp)) // just-in-case
      {
        generalUtils.replaceTwosWithOnes(tmp);

        lineNum=1; lineCount=0;
        {
          generalUtils.intToBytesCharFormat(lineNum, lineNumB, (short)0);      generalUtils.repAlpha(data, 4000, (short)15, lineNumB); // linenum

          generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)2, itemCode); generalUtils.repAlpha(data, 4000, (short)16, itemCode); // origin-1 ... origin-0

          generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)3, b);        generalUtils.repAlpha(data, 4000, (short)17, b); // desc

          generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)4, b); // unitprice
          generalUtils.bytesDPsGivenSeparator(true, '\000', '2', b, 20, 0);
          generalUtils.repAlpha(data, 4000, (short)18, b); // unitprice

          generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)5, b);
          generalUtils.bytesDPsGivenSeparator(true, '\000', dpOnQuantities, b, 20, 0);
          generalUtils.formatNumeric(b, dpOnQuantities);
          generalUtils.repAlpha(data, 4000, (short)19, b); // qty

          generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)6, b); // amt
          generalUtils.bytesDPsGivenSeparator(true, '\000', '2', b, 20, 0);
          generalUtils.repAlpha(data, 4000, (short)21, b); // amt

          generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)7, b); generalUtils.repAlpha(data, 4000, (short)22, b); // gstrate

          // get inventory rec
          generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)23, b); generalUtils.repAlpha(data, 4000, (short)23, b); // misc3

          reportGeneration.processSection("BL1", data, fldNames, (short)25, dnm, unm, localDefnsDir, defnsDir);

          // output any LL lines
          b[0] = '\000';
          generalUtils.repAlpha(data, 4000, (short)15, b); // linenum
          generalUtils.repAlpha(data, 4000, (short)16, b); // itemCode
          generalUtils.repAlpha(data, 4000, (short)17, b); // desc
          generalUtils.repAlpha(data, 4000, (short)18, b); // unitprice
          generalUtils.repAlpha(data, 4000, (short)19, b); // qty
          generalUtils.repAlpha(data, 4000, (short)21, b); // amt
          generalUtils.repAlpha(data, 4000, (short)22, b); // gstrate
          generalUtils.repAlpha(data, 4000, (short)23, b); // misc3

          // step through LL lines
          llCount = generalUtils.getListEntryByNumAndValue(llCount, multipleLinesCount[0], lineNumB, multipleLinesData, data);
          while(llCount != -1)
          {
            generalUtils.replaceTwosWithNulls(data);
            boolean quit1 = false;
            while(! quit1)
            {
              generalUtils.dfs(data, (short)3,  b);
              generalUtils.repAlpha(data, 4000, (short)17, b); // desc
              reportGeneration.processSection("BL1", data, fldNames, (short)25, dnm, unm, localDefnsDir, defnsDir);

              ++llCount;
              llCount = generalUtils.getListEntryByNumAndValue(llCount, multipleLinesCount[0], lineNumB, multipleLinesData, data);
              if(llCount == -1)
                quit1 = true; // on last rec
              else
              {
                if(! generalUtils.match(code, generalUtils.dfsAsStr(data, (short)0)) || ! generalUtils.match(lineNumB, generalUtils.dfsAsStr(data, (short)1)) ) // stepped on
                  quit1 = true;
                else
                {
	               ++lineCount;
	               if(lineCount > miscDefinitions.docSizeMax(con, stmt, rs, "so"))
	               {
                    b[0] = '\000';
                    reportGeneration.outputLine('E', b, data, fldNames, (short)33, ' ', dnm, unm, localDefnsDir, defnsDir);
	                 lineCount=1;
   	            }
                }
              }
            }
          }

          ++lineNum;
          ++lineCount;
          if(lineCount > miscDefinitions.docSizeMax(con, stmt, rs, "so"))
          {
            b[0] = '\000';
            reportGeneration.outputLine('E', b, data, fldNames, (short)33, ' ', dnm, unm, localDefnsDir, defnsDir);
            lineCount=1;
          }
        }
      }
    }

    if(salesOrder.getRecGivenCode(con, stmt, rs, code, '\000', tmp, dnm, localDefnsDir, defnsDir, bytesOut) != -1L) // just-in-case
    {  
      generalUtils.dfs(tmp, (short)11, b);
      int x=0,  y=0;
      while(b[x] != '\001' && b[x] != '\000')
        b2[y++] = b[x++];
      b2[y] = '\000';
      generalUtils.repAlpha(data, 4000, (short)35, b2);

      reportGeneration.processSection("BL2", data, fldNames, (short)36, dnm, unm, localDefnsDir, defnsDir);

      // any other note lines
      if(b[x] == '\001')
        ++x; // lf
      while(b[x] != '\000')
      {
        y=0;
        while(b[x] != '\001' && b[x] != '\000')
	  b2[y++] = b[x++];
        b2[y] = '\000';
        generalUtils.repAlpha(data, 4000, (short)36, b2);

        reportGeneration.processSection("BL3", data, fldNames, (short)37, dnm, unm, localDefnsDir, defnsDir);

        if(b[x] == '\001')
          ++x; // lf
      }
    }  

    reportGeneration.processSection("PF", data, fldNames, (short)36, dnm, unm, localDefnsDir, defnsDir);

    reportGeneration.fhO.write(reportGeneration.oBuf, 0, reportGeneration.oPtr);

    generalUtils.fileClose(reportGeneration.fhO);
    generalUtils.fileClose(reportGeneration.fhPPR);

    PrintingLayout printingLayout = new PrintingLayout();
    printingLayout.updateNumPages("0.000", reportsDir);

    return 0;
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void display(PrintWriter out, String unm, String sid, String uty, String men, String den, String dnm,
                         String bnm) throws Exception
  {
    URL url = new URL("http://" + men + "/central/servlet/PrintPreview?unm=" + unm + "&sid=" + sid + "&uty=" + uty
                    + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=0.000&bnm=" + bnm);

    URLConnection uc = url.openConnection();
    uc.setDoInput(true);
    uc.setUseCaches(false);
    uc.setDefaultUseCaches(false);

    uc.setRequestProperty("Content-Type", "application/octet-stream");

    BufferedReader di = new BufferedReader(new InputStreamReader(uc.getInputStream()));

    String s = di.readLine();
    while(s != null)
    {
      out.println(s);
      s = di.readLine();
    }

    di.close();
  }

}
