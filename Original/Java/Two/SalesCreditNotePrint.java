// =======================================================================================================================================================================================================
// System: ZaraStar Document: Credit Note: Print
// Module: SalesCreditNotePrint.java
// Author: C.K.Harvey
// Copyright (c) 1999-2007 Christopher Harvey. All Rights Reserved.
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

public class SalesCreditNotePrint extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  ReportGenDetails reportGenDetails = new ReportGenDetails();
  SalesCreditNote salesSalesCreditNote = new SalesCreditNote();
  MiscDefinitions miscDefinitions = new MiscDefinitions();
  AccountsUtils accountsUtils = new AccountsUtils();
  FiguresUtils figuresUtils = new FiguresUtils();
  
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p3="";

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
      p3  = req.getParameter("p3"); // printOrFax
      
      if(p3 == null) p3 = "P";

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p3, bytesOut);
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
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "SalesCreditNotePrint", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 4108, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush();
    }
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1, String p3, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');
    String reportsDir       = directoryUtils.getUserDir('R', dnm, unm);
    String workingDir       = directoryUtils.getUserDir('W', dnm, dnm + "/" + unm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;
    
    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 4108, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "4108", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 4108, bytesOut[0], 0, "ACC:" + p1);
      if(con  != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "4108", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 4108, bytesOut[0], 0, "SID:" + p1);
      if(con  != null) con.close();
      if(out != null) out.flush();
      return;
    }

    byte[] code = new byte[21];
    generalUtils.strToBytes(code, p1);

    int numPages = generate(con, stmt, rs, code, p3, dnm, unm, workingDir, reportsDir, localDefnsDir, defnsDir, bytesOut);
    switch(numPages)
    {
      case -1 : // Definition File Not Found
                messagePage.msgScreen(false, out, req, 17, unm, sid, uty, men, den, dnm, bnm, "4108", imagesDir, localDefnsDir, defnsDir, bytesOut);
                break;
      case -2 : // cannot create report output file
                messagePage.msgScreen(false, out, req, 18, unm, sid, uty, men, den, dnm, bnm, "4108", imagesDir, localDefnsDir, defnsDir, bytesOut);
                break;
      default : // generated ok
                out.println(generalUtils.intToStr(numPages));
                break;
    }

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 4108, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con  != null) con.close();
    if(out != null) out.flush();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private int generate(Connection con, Statement stmt, ResultSet rs, byte[] code, String printOrFax, String dnm, String unm, String workingDir, String reportsDir, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    byte[] b   = new byte[300];
    byte[] b2  = new byte[300];
    byte[] tmp = new byte[3000];

    RandomAccessFile fhPPR, fhO;
    char[] negStyle  = new char[1];
    char[] zeroStyle = new char[1];
    double[] tm             = new double[1];
    double[] bm             = new double[1];
    double[] lm             = new double[1];
    double[] rm             = new double[1];
    double[] pageSizeWidth  = new double[1];
    double[] pageSizeLength = new double[1];
    double[] currDown       = new double[1];
    double[] currAcross     = new double[1];
    short[] dp       = new short[1];
    short[] currFont = new short[1];
    short[] currPage = new short[1];
    short[] oPtr     = new short[1];
    short oBufLen;
    boolean[] lastOperationPF = new boolean[1];
    String[] userFontName   = new String[1];
    String[] userFontStyle  = new String[1];
    String[] userFontItalic = new String[1];
    String[] userFontSize   = new String[1];
    char[] fontDefType   = new char[21];
    char[] fontDefStyle  = new char[21];
    char[] fontDefItalic = new char[21];
    int[]  fontDefSize   = new int[21];
    double[] down2Coord   = new double[1];
    double[] across2Coord = new double[1];
    byte[] oBuf;
    String[] gstRates = new String[1];

    currFont[0] = 1;
    currPage[0] = 1;
    currDown[0] = currAcross[0] = 0.0;

    oBufLen = 30000;
    oBuf = new byte[30000];
    oPtr[0] = 0;

    if((fhO = reportGenDetails.createNewFile((short)0, workingDir, localDefnsDir, defnsDir, reportsDir)) == null)
      return -2;

    // fetch lines data in one go
    byte[] linesData  = new byte[2000];
    int[]  listLen    = new int[1];  listLen[0] = 2000;
    int[]  linesCount = new int[1];
    linesData = salesSalesCreditNote.getLines(con, stmt, rs, code, '\001', linesData, listLen, linesCount, dnm, localDefnsDir, defnsDir);
    
    reportGenDetails.getGSTRatesFromDocument(linesData, linesCount[0], 7, gstRates);

    byte[] multipleLinesData  = new byte[2000];
    int[]  multipleListLen    = new int[1];  multipleListLen[0] = 2000;
    int[]  multipleLinesCount = new int[1];
    int llCount = 0;
    if(linesCount[0] > 0) // get all the multiple lines for this document
      multipleLinesData = salesSalesCreditNote.getMultipleLines(con, stmt, rs, code, multipleLinesData, multipleListLen, multipleLinesCount, dnm, localDefnsDir, defnsDir);

    String pprFile;
    if(printOrFax.equals("P"))
      pprFile = "272.ppr";
    else pprFile = "272f.ppr";

    if((fhPPR = generalUtils.fileOpenD(pprFile, localDefnsDir)) == null)
    {
      if((fhPPR = generalUtils.fileOpenD(pprFile, defnsDir)) == null)
      {
        generalUtils.fileClose(fhO);
        return -1;
      }
    }

    lastOperationPF[0] = false;

    byte[] fldNames = new byte[10000];
    byte[] data     = new byte[6000];
    
    generalUtils.putAlpha(fldNames, 6000, (short)0,  "Credit.CNCode");
    generalUtils.putAlpha(fldNames, 6000, (short)1,  "Credit.Date");
    generalUtils.putAlpha(fldNames, 6000, (short)2,  "Credit.CompanyCode");
    generalUtils.putAlpha(fldNames, 6000, (short)3,  "Credit.CompanyName");
    generalUtils.putAlpha(fldNames, 6000, (short)4,  "Credit.Address1");
    generalUtils.putAlpha(fldNames, 6000, (short)5,  "Credit.Address2");
    generalUtils.putAlpha(fldNames, 6000, (short)6,  "Credit.Address3");
    generalUtils.putAlpha(fldNames, 6000, (short)7,  "Credit.Address4");
    generalUtils.putAlpha(fldNames, 6000, (short)8,  "Credit.Address5");
    generalUtils.putAlpha(fldNames, 6000, (short)9,  "Credit.PostCode");
    generalUtils.putAlpha(fldNames, 6000, (short)10, "Credit.FAO");
    generalUtils.putAlpha(fldNames, 6000, (short)11, "Credit.Misc1");
    generalUtils.putAlpha(fldNames, 6000, (short)12, "Credit.Misc2");
    generalUtils.putAlpha(fldNames, 6000, (short)13, "Credit.Attention");
    generalUtils.putAlpha(fldNames, 6000, (short)14, "CreditL.LineNumber");
    generalUtils.putAlpha(fldNames, 6000, (short)15, "CreditL.ItemCode");
    generalUtils.putAlpha(fldNames, 6000, (short)16, "CreditL.Description");
    generalUtils.putAlpha(fldNames, 6000, (short)17, "CreditL.UnitPrice");
    generalUtils.putAlpha(fldNames, 6000, (short)18, "CreditL.Quantity");
    generalUtils.putAlpha(fldNames, 6000, (short)19, "CreditL.Amount2");
    generalUtils.putAlpha(fldNames, 6000, (short)20, "CreditL.GSTRate");
    generalUtils.putAlpha(fldNames, 6000, (short)21, "CreditL.InvoiceCode");
    generalUtils.putAlpha(fldNames, 6000, (short)22, "Credit.GSTTotal");
    generalUtils.putAlpha(fldNames, 6000, (short)23, "Credit.TotalTotal");
    generalUtils.putAlpha(fldNames, 6000, (short)24, "Credit.TotalWithoutGST");
    generalUtils.putAlpha(fldNames, 6000, (short)25, "Credit.Currency2");
    generalUtils.putAlpha(fldNames, 6000, (short)26, "Credit.Rate2");
    generalUtils.putAlpha(fldNames, 6000, (short)27, "Credit.SignOn");
    generalUtils.putAlpha(fldNames, 6000, (short)28, "Credit.NoteLine1");
    generalUtils.putAlpha(fldNames, 6000, (short)29, "Credit.NoteLines");
    generalUtils.putAlpha(fldNames, 6000, (short)30, "CreditL.UoM");
    generalUtils.putAlpha(fldNames, 6000, (short)31, "Credit.GroupDiscount");
    generalUtils.putAlpha(fldNames, 6000, (short)32, "Credit.GroupDiscountType");
    generalUtils.putAlpha(fldNames, 6000, (short)33, "CreditL.Entry");
    generalUtils.putAlpha(fldNames, 6000, (short)34, "CreditL.Discount");
    generalUtils.putAlpha(fldNames, 6000, (short)35, "CreditL.CustomerItemCode");
    
    double groupDiscount=0.0, gstTotal=0;

    if(salesSalesCreditNote.getRecGivenCode(con, stmt, rs, code, '\000', tmp, dnm, localDefnsDir, defnsDir, bytesOut) != -1L) // just-in-case
    {
      generalUtils.putAlpha(data, 6000, (short)0, code);
      generalUtils.dfs(tmp, (short)1,  b); generalUtils.putAlpha(data, 6000, (short)1, generalUtils.convertFromYYYYMMDD(b)); // date
      generalUtils.dfs(tmp, (short)2,  b); generalUtils.putAlpha(data, 6000, (short)2,  b); // companycode
      generalUtils.dfs(tmp, (short)3,  b); generalUtils.putAlpha(data, 6000, (short)3,  b); // companyname
      generalUtils.dfs(tmp, (short)4,  b); generalUtils.putAlpha(data, 6000, (short)4,  b); // address1
      generalUtils.dfs(tmp, (short)5,  b); generalUtils.putAlpha(data, 6000, (short)5,  b); // address2
      generalUtils.dfs(tmp, (short)6,  b); generalUtils.putAlpha(data, 6000, (short)6,  b); // address3
      generalUtils.dfs(tmp, (short)7,  b); generalUtils.putAlpha(data, 6000, (short)7,  b); // address4
      generalUtils.dfs(tmp, (short)8,  b); generalUtils.putAlpha(data, 6000, (short)8,  b); // address5
      generalUtils.dfs(tmp, (short)9,  b); generalUtils.putAlpha(data, 6000, (short)9,  b); // postcode
      generalUtils.dfs(tmp, (short)10, b); generalUtils.putAlpha(data, 6000, (short)10, b); // FAO
      generalUtils.dfs(tmp, (short)13, b); generalUtils.putAlpha(data, 6000, (short)11, b); // misc1
      generalUtils.dfs(tmp, (short)14, b); generalUtils.putAlpha(data, 6000, (short)12, b); // misc2

      generalUtils.dfs(tmp, (short)12, b); generalUtils.putAlpha(data, 6000, (short)14, b); // attention

      generalUtils.dfs(tmp, (short)15, b); 
      generalUtils.doubleDPs(b, b, '2');
      generalUtils.putAlpha(data, 6000, (short)22, b); // gsttotal

      generalUtils.dfs(tmp, (short)16, b);
      generalUtils.doubleDPs(b, b, '2');
      generalUtils.putAlpha(data, 6000, (short)23, b); // totaltotal
      
      gstTotal = generalUtils.dfsAsDouble(tmp, (short)15);

      generalUtils.dfs(tmp, (short)33, b);
      generalUtils.doubleDPs(b, b, '2');
      generalUtils.putAlpha(data, 6000, (short)31, b); // groupdiscount

      groupDiscount = generalUtils.dfsAsDouble(tmp, (short)33);

      generalUtils.dfs(tmp, (short)34, b);
      b2[0] = ' ';
      generalUtils.bytesToBytes(b2, 1, b, 0);
      generalUtils.putAlpha(data, 6000, (short)32, b); // groupdiscounttype

      double d1 = generalUtils.dfsAsDouble(tmp, (short)15);
      double d2 = generalUtils.dfsAsDouble(tmp, (short)16);
      double totalSansGST = d2 - d1;

      generalUtils.repDoubleGivenSeparator('2', '\000', data, 6000, (short)24, totalSansGST);

      generalUtils.dfs(tmp, (short)23, b); generalUtils.putAlpha(data, 6000, (short)27, b); // signon
      
      generalUtils.dfs(tmp, (short)30, b); generalUtils.putAlpha(data, 6000, (short)25, b); // currency
      
      generalUtils.dfs(tmp, (short)31, b); generalUtils.putAlpha(data, 6000, (short)26, b); // rate
    }

    reportGenDetails.processControl(dnm, unm, localDefnsDir, defnsDir, dp, negStyle, zeroStyle, tm, bm, lm, rm, pageSizeWidth, pageSizeLength, currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName, userFontStyle,
                         userFontItalic, userFontSize, fhPPR, fhO, fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord, across2Coord, oBuf, gstRates);

    reportGenDetails.processSection("RH", null, null, (short)0, dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0], pageSizeWidth[0], pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName,
                         userFontStyle, userFontItalic, userFontSize, fhPPR, fhO, fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord, across2Coord, oBuf, gstRates);

    reportGenDetails.processSection("PH", data, fldNames, (short)33, dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0], pageSizeWidth[0], pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName,
                         userFontStyle, userFontItalic, userFontSize, fhPPR, fhO, fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord, across2Coord, oBuf, gstRates);

    char dpOnQuantities = miscDefinitions.dpOnQuantities(con, stmt, rs, "5");
    byte[] lastItemCode = new byte[21];      lastItemCode[0] = '\000';
    byte[] itemCode     = new byte[21];
    byte[] lineNumB     = new byte[20];
    byte[] entry        = new byte[20];
    byte[] data2        = new byte[1000];
    int lineCount=1, numPages=1;

    int docSizeMax = miscDefinitions.docSizeMax(con, stmt, rs, "credit");

    for(int xx=0;xx<linesCount[0];++xx)
    {
      if(generalUtils.getListEntryByNum(xx, linesData, tmp)) // just-in-case
      {
        generalUtils.replaceTwosWithOnes(tmp);

        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)15, lineNumB);
        generalUtils.repAlpha(data, 6000, (short)14, lineNumB);

        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)2, itemCode);
        generalUtils.repAlpha(data, 6000, (short)15, itemCode); // origin-1 ... origin-0

        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)16, entry);
        generalUtils.repAlpha(data, 6000, (short)33, entry);
        
        //generalUtils.repAlpha(data, 6000, (short)16, itemCode);
        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)3, b);
        generalUtils.repAlpha(data, 6000, (short)16, b); // desc

        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)4, b); // unitprice
        generalUtils.bytesDPsGivenSeparator(true, '\000', '2', b, 20, 0);
        generalUtils.repAlpha(data, 6000, (short)17, b); // unitprice

        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)5, b);
        generalUtils.bytesDPsGivenSeparator(true, '\000', dpOnQuantities, b, 20, 0);
        generalUtils.formatNumeric(b, dpOnQuantities);
        generalUtils.repAlpha(data, 6000, (short)18, b); // qty

        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)11, b); // amt
        generalUtils.bytesDPsGivenSeparator(true, '\000', '2', b, 20, 0);
        generalUtils.repAlpha(data, 6000, (short)19, b); // amt

        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)7, b);
        generalUtils.repAlpha(data, 6000, (short)20, b); // gstrate

        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)13, b); // discount%
        generalUtils.bytesDPsGivenSeparator(true, '\000', '2', b, 20, 0);
        generalUtils.repAlpha(data, 6000, (short)34, b);

        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)14, b);
        generalUtils.repAlpha(data, 6000, (short)35, b); // custItemCode
        
        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)23, b);
        generalUtils.repAlpha(data, 6000, (short)30, b); // uom

        reportGenDetails.processSection("BL1", data, fldNames, (short)35, dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0], pageSizeWidth[0], pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF,
                             userFontName, userFontStyle, userFontItalic, userFontSize, fhPPR, fhO, fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord, across2Coord, oBuf, gstRates);
        ++lineCount;
        if(lineCount >= docSizeMax)
        {
          b[0] = '\000';
          reportGenDetails.outputLine('E', b, data, fldNames, (short)35, ' ', dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0], pageSizeWidth[0], pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF,
                           userFontName, userFontStyle, userFontItalic, userFontSize, fhPPR, fhO, fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord, across2Coord, oBuf, gstRates);
          ++numPages;
          lineCount=1;
        }

        // output any LL lines
        if(   miscDefinitions.showDuplicateDescriptions(con, stmt, rs) || itemCode[0] == '-' 
           || ! generalUtils.matchIgnoreCase(lastItemCode, 0, itemCode, 0))
        {
          b[0] = '\000';
          generalUtils.repAlpha(data, 6000, (short)14, b); // line
          generalUtils.repAlpha(data, 6000, (short)15, b); // itemCode
          generalUtils.repAlpha(data, 6000, (short)16, b); // desc
          generalUtils.repAlpha(data, 6000, (short)17, b); // unitPrice
          generalUtils.repAlpha(data, 6000, (short)18, b); // qty
          generalUtils.repAlpha(data, 6000, (short)19, b); // amt2
          generalUtils.repAlpha(data, 6000, (short)20, b); // gstRate
          generalUtils.repAlpha(data, 6000, (short)21, b); // invoiceCode
          generalUtils.repAlpha(data, 6000, (short)30, b); // uom
          generalUtils.repAlpha(data, 6000, (short)33, b); // entry
          generalUtils.repAlpha(data, 6000, (short)34, b); // discount
          generalUtils.repAlpha(data, 6000, (short)35, b); // custitemcode

          // step through LL lines
          llCount=0;
          llCount = generalUtils.getListEntryByNumAndValue(llCount, multipleLinesCount[0], lineNumB, multipleLinesData, data2);
          while(llCount != -1)
          {
            generalUtils.replaceTwosWithNulls(data2);
            boolean quit1 = false;
            while(! quit1)
            {
              generalUtils.dfs(data2, (short)4,  b);
              generalUtils.repAlpha(data, 6000, (short)16, b); // desc
              reportGenDetails.processSection("BL1", data, fldNames, (short)35, dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0], pageSizeWidth[0],
                         pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName, userFontStyle,
                         userFontItalic, userFontSize, fhPPR, fhO, fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord, across2Coord,
                         oBuf, gstRates);
              ++lineCount;
              if(lineCount >= docSizeMax)
              {
                b[0] = '\000';
                reportGenDetails.outputLine('E', b, data, fldNames, (short)35, ' ', dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0], pageSizeWidth[0],
                         pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName, userFontStyle,
                         userFontItalic, userFontSize, fhPPR, fhO, fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord, across2Coord,
                         oBuf, gstRates);
                ++numPages;
                lineCount=1;
              }

              llCount = generalUtils.getListEntryByNumAndValue(llCount, multipleLinesCount[0], lineNumB, multipleLinesData, data2);
              if(llCount == -1)
                quit1 = true; // on last rec
              else
              {
                if(! generalUtils.match(code, generalUtils.dfsAsStr(data2, (short)1)) || ! generalUtils.match(lineNumB, generalUtils.dfsAsStr(data2, (short)2)) ) // stepped on
                  quit1 = true;
              }
            }
          }
        }

        generalUtils.bytesToBytes(lastItemCode, 0, itemCode, 0);
      }
    }

    salesSalesCreditNote.getRecGivenCode(con, stmt, rs, code, '\000', tmp, dnm, localDefnsDir, defnsDir, bytesOut);

    generalUtils.dfs(tmp, (short)11, b);
    int x=0,  y=0;
    while(b[x] != '\001' && b[x] != '\000')
      b2[y++] = b[x++];
    b2[y] = '\000';
    generalUtils.repAlpha(data, 6000, (short)28, b2);

    reportGenDetails.processSection("BL2", data, fldNames, (short)35, dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0], pageSizeWidth[0],
                         pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName, userFontStyle,
                         userFontItalic, userFontSize, fhPPR, fhO, fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord, across2Coord,
                         oBuf, gstRates);

    // any other note lines
    if(b[x] == '\001')
      ++x; // lf
    while(b[x] != '\000')
    {
      y=0;
      while(b[x] != '\001' && b[x] != '\000')
         b2[y++] = b[x++];
      b2[y] = '\000';
      generalUtils.repAlpha(data, 6000, (short)29, b2);

      reportGenDetails.processSection("BL3", data, fldNames, (short)35, dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0], pageSizeWidth[0],
                         pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName, userFontStyle,
                         userFontItalic, userFontSize, fhPPR, fhO, fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord, across2Coord,
                         oBuf, gstRates);

      if(b[x] == '\001')
       ++x; // lf
    }

    reportGenDetails.processSection("PF", data, fldNames, (short)35, dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0], pageSizeWidth[0], pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName,
                         userFontStyle, userFontItalic, userFontSize, fhPPR, fhO, fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord, across2Coord, oBuf, gstRates);

    if(groupDiscount == 0.0)
    {
      reportGenDetails.processSection("RF2", data, fldNames, (short)35, dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0], pageSizeWidth[0],
                         pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName, userFontStyle,
                         userFontItalic, userFontSize, fhPPR, fhO, fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord, across2Coord,
                         oBuf, gstRates);
    }   
    else // there is a discount
    {   
      reportGenDetails.processSection("RF1", data, fldNames, (short)35, dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0], pageSizeWidth[0], pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF,
                           userFontName, userFontStyle, userFontItalic, userFontSize, fhPPR, fhO, fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord, across2Coord, oBuf, gstRates);
    }
    
    fhO.write(oBuf, 0, oPtr[0]);

    generalUtils.fileClose(fhO);
    generalUtils.fileClose(fhPPR);

    PrintingLayout printingLayout = new PrintingLayout();
    printingLayout.updateNumPages("0.000", reportsDir);

    return numPages;
  }
  
}
