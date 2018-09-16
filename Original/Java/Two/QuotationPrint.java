// =======================================================================================================================================================================================================
// System: ZaraStar Document: Quotes: Print
// Module: QuotationPrint.java
// Author: C.K.Harvey
// Copyright (c) 1999-2009 Christopher Harvey. All Rights Reserved.
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

public class QuotationPrint extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  ReportGenDetails reportGenDetails = new ReportGenDetails();
  Quotation quotation = new Quotation();
  MiscDefinitions miscDefinitions = new MiscDefinitions();
  Customer customer = new Customer();
  AdminControlUtils adminControlUtils = new AdminControlUtils();

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
      p1  = req.getParameter("p1"); // code

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
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "QuotationPrint", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 4028, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush();
    }
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');
    String reportsDir    = directoryUtils.getUserDir('R', dnm, unm);
    String workingDir    = directoryUtils.getUserDir('W', dnm, dnm + "/" + unm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;
    
    if(!  (((uty.equals("R") && adminControlUtils.notDisabled(con, stmt, rs, 903) && authenticationUtils.verifyAccessForDocument(con, stmt, rs, req, 4122, p1, unm, sid, uty, dnm, localDefnsDir, defnsDir)))
       ||  (uty.equals("I") && authenticationUtils.verifyAccess(con, stmt, rs, req, 4028, unm, uty, dnm, localDefnsDir, defnsDir))))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "4028", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 4028, bytesOut[0], 0, "ACC:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "4028", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 4028, bytesOut[0], 0, "SID:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    byte[] code = new byte[21];
    generalUtils.strToBytes(code, p1);

    int numPages = generate(con, stmt, rs, code, dnm, unm, workingDir, reportsDir, localDefnsDir, defnsDir, bytesOut);
    switch(numPages)
    {
      case -1 : // Definition File Not Found
                messagePage.msgScreen(false, out, req, 17, unm, sid, uty, men, den, dnm, bnm, "4028", imagesDir, localDefnsDir, defnsDir, bytesOut);
                break;
      case -2 : // cannot create report output file
                messagePage.msgScreen(false, out, req, 18, unm, sid, uty, men, den, dnm, bnm, "4028", imagesDir, localDefnsDir, defnsDir, bytesOut);
                break;
      default : // generated ok
                out.println(generalUtils.intToStr(numPages));
                break;
    }

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 4028, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int generate(Connection con, Statement stmt, ResultSet rs, byte[] code, String dnm, String unm, String workingDir, String reportsDir, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    byte[] b   = new byte[300];
    byte[] b2  = new byte[300];
    byte[] tmp = new byte[3000];

    RandomAccessFile[] fhPPR = new RandomAccessFile[1];
    RandomAccessFile[] fhO   = new RandomAccessFile[1];
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

    if((fhO[0] = reportGenDetails.createNewFile((short)0, workingDir, localDefnsDir, defnsDir, reportsDir)) == null)
      return -2;
    
    // det total number of lines and multiple lines
    int docSizeMax  = miscDefinitions.docSizeMax(con, stmt, rs, "quote");
    int docSizeMaxA = miscDefinitions.docSizeMax(con, stmt, rs, "quote1");
    int docSizeMaxB = miscDefinitions.docSizeMax(con, stmt, rs, "quote2");
    int docSizeMaxC = miscDefinitions.docSizeMax(con, stmt, rs, "quote3");

    // fetch lines data in one go
    byte[] linesData  = new byte[2000];
    int[]  listLen    = new int[1];  listLen[0] = 2000;
    int[]  linesCount = new int[1];
    linesData = quotation.getLines(con, stmt, rs, code, '\001', linesData, listLen, linesCount, dnm, localDefnsDir, defnsDir);
    
    reportGenDetails.getGSTRatesFromDocument(linesData, linesCount[0], 7, gstRates);

    byte[] multipleLinesData  = new byte[2000];
    int[]  multipleListLen    = new int[1];  multipleListLen[0] = 2000;
    int[]  multipleLinesCount = new int[1];
    int llCount=0;
    if(linesCount[0] > 0) // get all the multiple lines for this document
      multipleLinesData = quotation.getMultipleLines(con, stmt, rs, code, multipleLinesData, multipleListLen, multipleLinesCount);
    
    int totalNumLines = linesCount[0] + multipleLinesCount[0];
    int numPages;
    boolean onePager = false, twoPager = false, threePager = false;

    // increase totalNumLines if mfrCode will also be output
    if(miscDefinitions.prependMfrCode(con, stmt, rs)) // mfrCodes wanted
      totalNumLines += linesCount[0]; // one extra line for each line      

    if(totalNumLines == 0)
      numPages = 1;
    else
    {
      if(totalNumLines <= docSizeMax)
      {
        numPages = 1;
        onePager = true;
      }
      else
      if(totalNumLines <= (docSizeMaxA + docSizeMaxC))
      {
        numPages = 2;
        twoPager = true;
      }  
      else
      if(totalNumLines <= (docSizeMaxA + docSizeMaxB + docSizeMaxC))
      {
        numPages = 3;
        threePager = true;
      }  
      else
      {
        numPages = 2;
        int x = totalNumLines - docSizeMaxA;
        while(x > docSizeMaxC)
        {
          x -= docSizeMaxB;
          ++numPages;
        }
      }
    }

    // one page layout  = 227.ppr
    // two page laypout = 227a and 227c
    // three or more    = 227a, 227b, and 227c

    String pprFile;
    if(onePager)
      pprFile = "227.ppr";
    else pprFile = "227a.ppr";

    if((fhPPR[0] = generalUtils.fileOpenD(pprFile, localDefnsDir)) == null)
    {
      if((fhPPR[0] = generalUtils.fileOpenD(pprFile, defnsDir)) == null)
      {
        generalUtils.fileClose(fhO[0]);
        return -1;
      }
    }

    lastOperationPF[0] = false;

    byte[] fldNames = new byte[10000];
    byte[] data     = new byte[4000];

    generalUtils.putAlpha(fldNames, 4000, (short)0,  "Quote.QuoteCode");
    generalUtils.putAlpha(fldNames, 4000, (short)1,  "Quote.Date");
    generalUtils.putAlpha(fldNames, 4000, (short)2,  "Quote.CompanyCode");
    generalUtils.putAlpha(fldNames, 4000, (short)3,  "Quote.CompanyName");
    generalUtils.putAlpha(fldNames, 4000, (short)4,  "Quote.Address1");
    generalUtils.putAlpha(fldNames, 4000, (short)5,  "Quote.Address2");
    generalUtils.putAlpha(fldNames, 4000, (short)6,  "Quote.Address3");
    generalUtils.putAlpha(fldNames, 4000, (short)7,  "Quote.Address4");
    generalUtils.putAlpha(fldNames, 4000, (short)8,  "Quote.Address5");
    generalUtils.putAlpha(fldNames, 4000, (short)9,  "Quote.PostCode");
    generalUtils.putAlpha(fldNames, 4000, (short)10, "Quote.FAO");
    generalUtils.putAlpha(fldNames, 4000, (short)11, "Quote.Misc1");
    generalUtils.putAlpha(fldNames, 4000, (short)12, "Quote.Misc2");
    generalUtils.putAlpha(fldNames, 4000, (short)13, "Quote.DateRange");
    generalUtils.putAlpha(fldNames, 4000, (short)14, "Quote.Attention");
    generalUtils.putAlpha(fldNames, 4000, (short)15, "QuoteL.Entry");
    generalUtils.putAlpha(fldNames, 4000, (short)16, "QuoteL.ItemCode");
    generalUtils.putAlpha(fldNames, 4000, (short)17, "QuoteL.Description");
    generalUtils.putAlpha(fldNames, 4000, (short)18, "QuoteL.UnitPrice");
    generalUtils.putAlpha(fldNames, 4000, (short)19, "QuoteL.Quantity");
    generalUtils.putAlpha(fldNames, 4000, (short)20, "QuoteL.DiscountPercentage");
    generalUtils.putAlpha(fldNames, 4000, (short)21, "QuoteL.Amount");
    generalUtils.putAlpha(fldNames, 4000, (short)22, "QuoteL.GSTRate");
    generalUtils.putAlpha(fldNames, 4000, (short)23, "QuoteL.UoM");
    generalUtils.putAlpha(fldNames, 4000, (short)24, "QuoteL.AltItemCode");
    generalUtils.putAlpha(fldNames, 4000, (short)25, "Quote.GSTTotal");
    generalUtils.putAlpha(fldNames, 4000, (short)26, "Quote.TotalTotal");
    generalUtils.putAlpha(fldNames, 4000, (short)27, "Quote.GroupDiscount");
    generalUtils.putAlpha(fldNames, 4000, (short)28, "Quote.GroupDiscountType");
    generalUtils.putAlpha(fldNames, 4000, (short)29, "Quote.TotalWithoutGST");
    generalUtils.putAlpha(fldNames, 4000, (short)30, "Quote.SalesPerson");
    generalUtils.putAlpha(fldNames, 4000, (short)31, "Quote.EnquiryCode");
    generalUtils.putAlpha(fldNames, 4000, (short)32, "Quote.Currency");
    generalUtils.putAlpha(fldNames, 4000, (short)33, "Quote.NoteLine1");
    generalUtils.putAlpha(fldNames, 4000, (short)34, "Quote.NoteLines");
    generalUtils.putAlpha(fldNames, 4000, (short)35, "QuoteL.CustomerItemCode");
    generalUtils.putAlpha(fldNames, 4000, (short)36, "Quote.Terms");
    generalUtils.putAlpha(fldNames, 4000, (short)37, "Quote.Validity");
    generalUtils.putAlpha(fldNames, 4000, (short)38, "Quote.Delivery");
    generalUtils.putAlpha(fldNames, 4000, (short)39, "Quote.Packaging");
    generalUtils.putAlpha(fldNames, 4000, (short)40, "Quote.Fax");
    generalUtils.putAlpha(fldNames, 4000, (short)41, "Quote.Country");
    generalUtils.putAlpha(fldNames, 4000, (short)42, "Quote.RemarkLine");
    generalUtils.putAlpha(fldNames, 4000, (short)43, "Quote.DeliveryLeadTime");
    generalUtils.putAlpha(fldNames, 4000, (short)44, "Quote.Manufacturer");
    generalUtils.putAlpha(fldNames, 4000, (short)45, "QuoteL.Text1");
    generalUtils.putAlpha(fldNames, 4000, (short)46, "QuoteL.Text2");
    generalUtils.putAlpha(fldNames, 4000, (short)47, "Quote.Phone");
    generalUtils.putAlpha(fldNames, 4000, (short)48, "Quote.SignOn");
    generalUtils.putAlpha(fldNames, 4000, (short)49, "QuoteL.ManufacturerCode");

    double groupDiscount=0.0;
    String remarkType="";
    byte[] customerCode = new byte[21];

    if(quotation.getRecGivenCode(con, stmt, rs, code, '\000', tmp, dnm, localDefnsDir, defnsDir, bytesOut) != -1L) // just-in-case
    {
      generalUtils.putAlpha(data, 4000, (short)0, code);
      generalUtils.dfs(tmp, (short)1,  b); generalUtils.putAlpha(data, 4000, (short)1, generalUtils.convertFromYYYYMMDD(b)); // date
      generalUtils.dfs(tmp, (short)2,  customerCode); generalUtils.putAlpha(data, 4000, (short)2,  customerCode); // companycode
      generalUtils.dfs(tmp, (short)3,  b); generalUtils.putAlpha(data, 4000, (short)3,  b); // companyname
      generalUtils.dfs(tmp, (short)4,  b); generalUtils.putAlpha(data, 4000, (short)4,  b); // address1
      generalUtils.dfs(tmp, (short)5,  b); generalUtils.putAlpha(data, 4000, (short)5,  b); // address2
      generalUtils.dfs(tmp, (short)6,  b); generalUtils.putAlpha(data, 4000, (short)6,  b); // address3
      generalUtils.dfs(tmp, (short)7,  b); generalUtils.putAlpha(data, 4000, (short)7,  b); // address4
      generalUtils.dfs(tmp, (short)8,  b); generalUtils.putAlpha(data, 4000, (short)8,  b); // address5
      generalUtils.dfs(tmp, (short)9,  b); generalUtils.putAlpha(data, 4000, (short)9,  b); // postcode
      generalUtils.dfs(tmp, (short)10, b); generalUtils.putAlpha(data, 4000, (short)10, b); // FAO
      generalUtils.dfs(tmp, (short)15, b); generalUtils.putAlpha(data, 4000, (short)11, b); // misc1
      generalUtils.dfs(tmp, (short)16, b); generalUtils.putAlpha(data, 4000, (short)12, b); // misc2

      generalUtils.dfs(tmp, (short)12, b); generalUtils.putAlpha(data, 4000, (short)14, b); // attention
      
      generalUtils.dfs(tmp, (short)17, b);
      generalUtils.doubleDPs(b, b, '2');
      generalUtils.putAlpha(data, 4000, (short)25, b); // gsttotal
      
      generalUtils.dfs(tmp, (short)18, b);
      generalUtils.doubleDPs(b, b, '2');
      generalUtils.putAlpha(data, 4000, (short)26, b); // totaltotal

      generalUtils.dfs(tmp, (short)33, b);
      generalUtils.doubleDPs(b, b, '2');
      generalUtils.putAlpha(data, 4000, (short)27, b); // groupdiscount

      groupDiscount = generalUtils.dfsAsDouble(tmp, (short)33);

      generalUtils.dfs(tmp, (short)36, b);
      if(b[0] == 'V')
      {
        b[0] = ' ';
      }
      generalUtils.putAlpha(data, 4000, (short)28, b); // groupdiscounttype

      double d1 = generalUtils.dfsAsDouble(tmp, (short)17);
      double d2 = generalUtils.dfsAsDouble(tmp, (short)18);
      double totalSansGST = d2 - d1;
      generalUtils.repDoubleGivenSeparator('2', '\000', data, 4000, (short)29, totalSansGST);

      generalUtils.dfs(tmp, (short)19, b); generalUtils.putAlpha(data, 4000, (short)30, b); // salesperson
      generalUtils.dfs(tmp, (short)39, b); generalUtils.putAlpha(data, 4000, (short)31, b); // enquirycode
      generalUtils.dfs(tmp, (short)42, b); generalUtils.putAlpha(data, 4000, (short)32, b); // currency
      
      generalUtils.dfs(tmp, (short)46, b); generalUtils.putAlpha(data, 4000, (short)36, b); // terms
      generalUtils.dfs(tmp, (short)47, b); generalUtils.putAlpha(data, 4000, (short)37, b); // validity
      generalUtils.dfs(tmp, (short)48, b); generalUtils.putAlpha(data, 4000, (short)38, b); // delivery
      generalUtils.dfs(tmp, (short)49, b); generalUtils.putAlpha(data, 4000, (short)39, b); // packaging
      generalUtils.dfs(tmp, (short)51, b); generalUtils.putAlpha(data, 4000, (short)41, b); // country
      generalUtils.dfs(tmp, (short)54, b); generalUtils.putAlpha(data, 4000, (short)42, b); // remarkLine
      generalUtils.dfs(tmp, (short)55, b); generalUtils.putAlpha(data, 4000, (short)43, b); // deliveryLeadTime
      generalUtils.dfs(tmp, (short)53, b); generalUtils.putAlpha(data, 4000, (short)44, b); // manufacturer
      generalUtils.dfs(tmp, (short)57, b); generalUtils.putAlpha(data, 4000, (short)48, b); // signOn

      remarkType = generalUtils.dfsAsStr(tmp, (short)54);
    }

    String phone = generalUtils.dfsAsStr(tmp, (short)56); // phone
    String fax   = generalUtils.dfsAsStr(tmp, (short)50); // fax

    if(phone.length() == 0)
      phone = customer.getACompanyFieldGivenCode(con, stmt, rs, "Phone1", generalUtils.stringFromBytes(customerCode, 0L));
      
    if(fax.length() == 0)
      fax = customer.getACompanyFieldGivenCode(con, stmt, rs, "Fax", generalUtils.stringFromBytes(customerCode, 0L));

    generalUtils.putAlpha(data, 4000, (short)40, fax);    
    generalUtils.putAlpha(data, 4000, (short)47, phone);

    reportGenDetails.processControl(dnm, unm, localDefnsDir, defnsDir, dp, negStyle, zeroStyle, tm, bm, lm, rm, pageSizeWidth, pageSizeLength, currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName, userFontStyle,
                         userFontItalic, userFontSize, fhPPR[0], fhO[0], fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord, across2Coord, oBuf, gstRates);

    reportGenDetails.processSection("RH", null, null, (short)0, dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0], pageSizeWidth[0], pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName,
                         userFontStyle, userFontItalic, userFontSize, fhPPR[0], fhO[0], fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord, across2Coord, oBuf, gstRates);

    reportGenDetails.processSection("PH", data, fldNames, (short)50, dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0], pageSizeWidth[0], pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName,
                         userFontStyle, userFontItalic, userFontSize, fhPPR[0], fhO[0], fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord, across2Coord, oBuf, gstRates);

    char dpOnQuantities = miscDefinitions.dpOnQuantities(con, stmt, rs, "5");
    byte[] lastEntry = new byte[6];      lastEntry[0] = '\000';
    byte[] itemCode  = new byte[21];
    byte[] lineNumB  = new byte[20];
    byte[] entry     = new byte[20];
    byte[] desc      = new byte[81];
    byte[] unitPrice = new byte[21];
    byte[] quantity  = new byte[20];
    byte[] amount2   = new byte[20];
    byte[] gstRate   = new byte[20];
    byte[] discountPercentage = new byte[20];
    byte[] uom          = new byte[20];
    byte[] custItemCode = new byte[41];
    byte[] mfrCode      = new byte[61];
    byte[] data2        = new byte[1000];
    int[] lineCount = new int[1];  lineCount[0] = 1;
    int[] pageCount = new int[1];  pageCount[0] = 1;

    for(int xx=0;xx<linesCount[0];++xx)
    {
      if(generalUtils.getListEntryByNum(xx, linesData, tmp)) // just-in-case
      {
        generalUtils.replaceTwosWithOnes(tmp);
                
        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)12, lineNumB);
        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)13, entry);
        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)2,  itemCode);
        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)3,  desc);
        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)4,  unitPrice);
        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)5,  quantity);
        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)10, amount2);
        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)7,  gstRate);
        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)8,  discountPercentage);
        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)14, uom);
        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)15, custItemCode);
        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)21, mfrCode);

        if(miscDefinitions.prependMfrCode(con, stmt, rs))// must always o/p else screws-up totalNumLines // && mfrCode[0] != '\000')
        {  
          outputDetailLine(con, stmt, rs, data, fldNames, lineNumB, itemCode, mfrCode, unitPrice, quantity, amount2, gstRate, discountPercentage, uom, custItemCode, entry, lastEntry, lineCount, pageCount, mfrCode, numPages, docSizeMax,
                           docSizeMaxA, docSizeMaxB, onePager, twoPager, threePager, dpOnQuantities, unm, dnm, localDefnsDir, defnsDir, tm, bm, lm, rm, pageSizeWidth, pageSizeLength, currDown, currAcross, currFont, currPage, oPtr, oBufLen,
                           lastOperationPF, userFontName, userFontStyle, userFontItalic, userFontSize, fhPPR, fhO, fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord, across2Coord, oBuf, gstRates);

          entry[0] = '\000'; unitPrice[0] = '\000'; quantity[0] = '\000'; discountPercentage[0] = '\000'; amount2[0] = '\000'; gstRate[0] = '\000'; uom[0] = '\000';
        }

        outputDetailLine(con, stmt, rs, data, fldNames, lineNumB, itemCode, desc, unitPrice, quantity, amount2, gstRate, discountPercentage, uom, custItemCode, entry, lastEntry, lineCount, pageCount, mfrCode, numPages, docSizeMax, docSizeMaxA,
                         docSizeMaxB, onePager, twoPager, threePager, dpOnQuantities, unm, dnm, localDefnsDir, defnsDir, tm, bm, lm, rm, pageSizeWidth, pageSizeLength, currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF,
                         userFontName, userFontStyle, userFontItalic, userFontSize, fhPPR, fhO, fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord, across2Coord, oBuf, gstRates);

        // output any LL lines
        b[0] = '\000';
        generalUtils.repAlpha(data, 4000, (short)15, b); // entry
        generalUtils.repAlpha(data, 4000, (short)16, b); // itemCode
        generalUtils.repAlpha(data, 4000, (short)17, b); // desc
        generalUtils.repAlpha(data, 4000, (short)18, b); // unitprice
        generalUtils.repAlpha(data, 4000, (short)19, b); // qty
        generalUtils.repAlpha(data, 4000, (short)20, b); // discount%
        generalUtils.repAlpha(data, 4000, (short)21, b); // amt
        generalUtils.repAlpha(data, 4000, (short)22, b); // gstrate
        generalUtils.repAlpha(data, 4000, (short)23, b); // uom
        generalUtils.repAlpha(data, 4000, (short)35, b); // custitemcode
        generalUtils.repAlpha(data, 4000, (short)45, b); // linetext1
        generalUtils.repAlpha(data, 4000, (short)46, b); // linetext2

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
            generalUtils.repAlpha(data, 4000, (short)17, b); // desc
            reportGenDetails.processSection("BL1", data, fldNames, (short)48, dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0], pageSizeWidth[0], pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF,
                                 userFontName, userFontStyle, userFontItalic, userFontSize, fhPPR[0], fhO[0], fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord, across2Coord, oBuf, gstRates);
            ++lineCount[0];
            if(chkForPageThrow(lineCount, pageCount, numPages, docSizeMax, docSizeMaxA, docSizeMaxB, onePager, twoPager, threePager, data, fldNames, unm, dnm, localDefnsDir, defnsDir, tm, bm, lm, rm, pageSizeWidth, pageSizeLength, currDown,
                               currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName, userFontStyle, userFontItalic, userFontSize, fhPPR, fhO, fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord, across2Coord,
                               oBuf, gstRates) == -1)
            {
              return -1;
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

        if(custItemCode[0] != '\000' && ! generalUtils.match(custItemCode, "none"))
        {
          generalUtils.repAlpha(data, 4000, (short)35, custItemCode);
          reportGenDetails.processSection("BL2", data, fldNames, (short)50, dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0], pageSizeWidth[0], pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF,
                               userFontName, userFontStyle, userFontItalic, userFontSize, fhPPR[0], fhO[0], fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord, across2Coord, oBuf, gstRates);
          ++lineCount[0];
          if(chkForPageThrow(lineCount, pageCount, numPages, docSizeMax, docSizeMaxA, docSizeMaxB, onePager, twoPager, threePager, data, fldNames, unm, dnm, localDefnsDir, defnsDir, tm, bm, lm, rm, pageSizeWidth, pageSizeLength, currDown,
                             currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName, userFontStyle, userFontItalic, userFontSize, fhPPR, fhO, fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord, across2Coord,
                             oBuf, gstRates) == -1)
          {
            return -1;
          } 
        }
      }
    }

    // if all lines output but totals still not yet done
    // only an issue when the penultimate page fills up to the point where the footer would be on a page C (i.e., the last 2-3 cm).
    if(onePager) ; // no issue
    else
    //if(twoPager) ; // no issue
    //else // if not on page C already
    if(pageCount[0] < numPages)
    {
      pprFile = "227c.ppr";
      generalUtils.fileClose(fhPPR[0]);
      if((fhPPR[0] = generalUtils.fileOpenD(pprFile, localDefnsDir)) == null)
      {
        if((fhPPR[0] = generalUtils.fileOpenD(pprFile, defnsDir)) == null)
        {
          generalUtils.fileClose(fhO[0]);
          return -1;
        }
      }

      b[0] = '\000';
      reportGenDetails.outputLine('F', b, data, fldNames, (short)50, ' ', dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0], pageSizeWidth[0], pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF,
                       userFontName, userFontStyle, userFontItalic, userFontSize, fhPPR[0], fhO[0], fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord, across2Coord, oBuf, gstRates);

      reportGenDetails.processSection("PH", data, fldNames, (short)50, dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0], pageSizeWidth[0], pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF,
                           userFontName, userFontStyle, userFontItalic, userFontSize, fhPPR[0], fhO[0], fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord, across2Coord, oBuf, gstRates);
    }

    quotation.getRecGivenCode(con, stmt, rs, code, '\000', tmp, dnm, localDefnsDir, defnsDir, bytesOut);

    generalUtils.dfs(tmp, (short)11, b);
    int x=0,  y=0;
    while(b[x] != '\001' && b[x] != '\000')
      b2[y++] = b[x++];
    b2[y] = '\000';
    generalUtils.repAlpha(data, 4000, (short)33, b2);

    reportGenDetails.processSection("BL2", data, fldNames, (short)50, dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0], pageSizeWidth[0], pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName,
                         userFontStyle, userFontItalic, userFontSize, fhPPR[0], fhO[0], fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord, across2Coord, oBuf, gstRates);

    // any other note lines
    if(b[x] == '\001')
      ++x; // lf
    while(b[x] != '\000')
    {
      y=0;
      while(b[x] != '\001' && b[x] != '\000')
    	b2[y++] = b[x++];
      b2[y] = '\000';
      
      generalUtils.repAlpha(data, 4000, (short)34, b2);

      reportGenDetails.processSection("BL3", data, fldNames, (short)50, dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0], pageSizeWidth[0], pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF,
                           userFontName, userFontStyle, userFontItalic, userFontSize, fhPPR[0], fhO[0], fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord, across2Coord, oBuf, gstRates);

      if(b[x] == '\001')
	     ++x; // lf
    }

    reportGenDetails.processSection("PF", data, fldNames, (short)50, dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0], pageSizeWidth[0], pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName,
                         userFontStyle, userFontItalic, userFontSize, fhPPR[0], fhO[0], fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord, across2Coord, oBuf, gstRates);

    if(groupDiscount == 0.0)
    {
      reportGenDetails.processSection("RF1", data, fldNames, (short)50, dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0], pageSizeWidth[0], pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF,
                           userFontName, userFontStyle, userFontItalic, userFontSize, fhPPR[0], fhO[0], fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord, across2Coord, oBuf, gstRates);
    }
    else // there is a discount
    {
      reportGenDetails.processSection("RF2", data, fldNames, (short)50, dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0], pageSizeWidth[0], pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF,
                           userFontName, userFontStyle, userFontItalic, userFontSize, fhPPR[0], fhO[0], fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord, across2Coord, oBuf, gstRates);
    }
    
    // posn cursor
    reportGenDetails.processSection("RF7", data, fldNames, (short)50, dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0], pageSizeWidth[0], pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF,
                         userFontName, userFontStyle, userFontItalic, userFontSize, fhPPR[0], fhO[0], fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord, across2Coord, oBuf, gstRates);
    
    outputRemark(remarkType, data, fldNames, dnm, unm, localDefnsDir, defnsDir, tm, bm, lm, rm, pageSizeWidth, pageSizeLength, currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName, userFontStyle, userFontItalic,
                 userFontSize, fhPPR, fhO, fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord, across2Coord, oBuf, gstRates);

    fhO[0].write(oBuf, 0, oPtr[0]);

    generalUtils.fileClose(fhO[0]);
    generalUtils.fileClose(fhPPR[0]);

    PrintingLayout printingLayout = new PrintingLayout();
    printingLayout.updateNumPages("0.000", reportsDir);

    return numPages;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void outputRemark(String remarkType, byte[] data, byte[] fldNames, String dnm, String unm, String localDefnsDir, String defnsDir, double[] tm, double[] bm, double[] lm, double[] rm, double[] pageSizeWidth, double[] pageSizeLength,
                            double[] currDown, double[] currAcross, short[] currFont, short[] currPage, short[] oPtr, short oBufLen, boolean[] lastOperationPF, String[] userFontName, String[] userFontStyle, String[] userFontItalic,
                            String[] userFontSize, RandomAccessFile[] fhPPR, RandomAccessFile[] fhO, char[] fontDefType, char[] fontDefStyle, char[] fontDefItalic, int[] fontDefSize, double[] down2Coord, double[] across2Coord,
                            byte[] oBuf, String[] gstRates) throws Exception
  {
    if(remarkType.length() == 0) return;

    int x=0;
    String s = "";
    while(! s.equals(" "))
    {
      s = generalUtils.getRemark('Q', remarkType, x++, localDefnsDir, defnsDir);
      generalUtils.repAlpha(data, 4000, (short)42, s);

      reportGenDetails.processSection("RF8", data, fldNames, (short)50, dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0], pageSizeWidth[0], pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF,
                           userFontName, userFontStyle, userFontItalic, userFontSize, fhPPR[0], fhO[0], fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord, across2Coord, oBuf, gstRates);
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int chkForPageThrow(int[] lineCount, int[] pageCount, int numPages, int docMaxSize, int docMaxSizeA, int docMaxSizeB, boolean onePager, boolean twoPager, boolean threePager, byte[] data, byte[] fldNames, String unm, String dnm,
                              String localDefnsDir, String defnsDir, double[] tm, double[] bm, double[] lm, double[] rm, double[] pageSizeWidth, double[] pageSizeLength, double[] currDown, double[] currAcross, short[] currFont, short[] currPage,
                              short[] oPtr, short oBufLen, boolean[] lastOperationPF, String[] userFontName, String[] userFontStyle, String[] userFontItalic, String[] userFontSize, RandomAccessFile[] fhPPR, RandomAccessFile[] fhO,
                              char[] fontDefType, char[] fontDefStyle, char[] fontDefItalic, int[] fontDefSize, double[] down2Coord, double[] across2Coord, byte[] oBuf, String[] gstRates) throws Exception
  {
    byte[] b = new byte[1];  b[0] = '\000';
    String pprFile;

    if(onePager)
      ; // already determined that will not need to page throw; nowt to do
    else
    if(twoPager)
    {
      if(pageCount[0] == 2)
        ; // on page 2 already; nowt to check
      else // pageCount must be 1
      {
        if(lineCount[0] > docMaxSizeA)
        {       
          reportGenDetails.outputLine('F', b, data, fldNames, (short)50, ' ', dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0], pageSizeWidth[0], pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF,
                           userFontName, userFontStyle, userFontItalic, userFontSize, fhPPR[0], fhO[0], fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord, across2Coord, oBuf, gstRates);
          lineCount[0] = 1;
          pageCount[0] = 2;

          pprFile = "227c.ppr";           

          generalUtils.fileClose(fhPPR[0]);
          if((fhPPR[0] = generalUtils.fileOpenD(pprFile, localDefnsDir)) == null)
          {
            if((fhPPR[0] = generalUtils.fileOpenD(pprFile, defnsDir)) == null)
            {
              generalUtils.fileClose(fhO[0]);
              return -1;
            }
          }
          
          reportGenDetails.processSection("PH", data, fldNames, (short)50, dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0], pageSizeWidth[0], pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF,
                               userFontName, userFontStyle, userFontItalic, userFontSize, fhPPR[0], fhO[0], fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord, across2Coord, oBuf, gstRates);
        }
      }  
    }
    else
    if(threePager)
    {
      if(pageCount[0] == 3)
        ; // on page 3 already; nowt to check
      else
      if(pageCount[0] == 1)
      {
        if(lineCount[0] > docMaxSizeA)
        {    
          reportGenDetails.outputLine('F', b, data, fldNames, (short)50, ' ', dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0], pageSizeWidth[0], pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF,
                           userFontName, userFontStyle, userFontItalic, userFontSize, fhPPR[0], fhO[0], fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord, across2Coord, oBuf, gstRates);
          lineCount[0] = 1;
          pageCount[0] = 2;

          pprFile = "227b.ppr";           
          generalUtils.fileClose(fhPPR[0]);
          if((fhPPR[0] = generalUtils.fileOpenD(pprFile, localDefnsDir)) == null)
          {
            if((fhPPR[0] = generalUtils.fileOpenD(pprFile, defnsDir)) == null)
            {
              generalUtils.fileClose(fhO[0]);
              return -1;
            }  
          }

          reportGenDetails.processSection("PH", data, fldNames, (short)50, dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0], pageSizeWidth[0], pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF,
                               userFontName, userFontStyle, userFontItalic, userFontSize, fhPPR[0], fhO[0], fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord, across2Coord, oBuf, gstRates);
        }
      }
      else // pageCount must be 2
      {
        if(lineCount[0] > docMaxSizeB)
        {       
          reportGenDetails.outputLine('F', b, data, fldNames, (short)50, ' ', dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0], pageSizeWidth[0], pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF,
                           userFontName, userFontStyle, userFontItalic, userFontSize, fhPPR[0], fhO[0], fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord, across2Coord, oBuf, gstRates);
          lineCount[0] = 1;
          pageCount[0] = 3;

          pprFile = "227c.ppr";
          generalUtils.fileClose(fhPPR[0]);
          if((fhPPR[0] = generalUtils.fileOpenD(pprFile, localDefnsDir)) == null)
          {
            if((fhPPR[0] = generalUtils.fileOpenD(pprFile, defnsDir)) == null)
            {
              generalUtils.fileClose(fhO[0]);
              return -1;
            }
          }

          reportGenDetails.processSection("PH", data, fldNames, (short)50, dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0], pageSizeWidth[0], pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF,
                               userFontName, userFontStyle, userFontItalic, userFontSize, fhPPR[0], fhO[0], fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord, across2Coord, oBuf, gstRates);
        }
      }
    }
    else // more than 3 pages
    {
      if(pageCount[0] == numPages)
        ; // on last page already; nowt to check
      else
      if(pageCount[0] == 1)
      {
        if(lineCount[0] > docMaxSizeA)
        {       
          reportGenDetails.outputLine('F', b, data, fldNames, (short)50, ' ', dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0], pageSizeWidth[0], pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF,
                           userFontName, userFontStyle, userFontItalic, userFontSize, fhPPR[0], fhO[0], fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord, across2Coord, oBuf, gstRates);
          lineCount[0] = 1;
          pageCount[0] = 2;

          pprFile = "227b.ppr";
          generalUtils.fileClose(fhPPR[0]);
          if((fhPPR[0] = generalUtils.fileOpenD(pprFile, localDefnsDir)) == null)
          {
            if((fhPPR[0] = generalUtils.fileOpenD(pprFile, defnsDir)) == null)
            {
              generalUtils.fileClose(fhO[0]);
              return -1;
            }
          }

          reportGenDetails.processSection("PH", data, fldNames, (short)50, dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0], pageSizeWidth[0], pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF,
                               userFontName, userFontStyle, userFontItalic, userFontSize, fhPPR[0], fhO[0], fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord, across2Coord, oBuf, gstRates);
        }
      }
      else
      if(pageCount[0] == (numPages - 1)) // on penultimate page
      {
        if(lineCount[0] > docMaxSizeB)
        {       
          reportGenDetails.outputLine('F', b, data, fldNames, (short)50, ' ', dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0], pageSizeWidth[0], pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF,
                           userFontName, userFontStyle, userFontItalic, userFontSize, fhPPR[0], fhO[0], fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord, across2Coord, oBuf, gstRates);
          lineCount[0] = 1;
          ++pageCount[0];

          pprFile = "227c.ppr";
          generalUtils.fileClose(fhPPR[0]);
          if((fhPPR[0] = generalUtils.fileOpenD(pprFile, localDefnsDir)) == null)
          {
            if((fhPPR[0] = generalUtils.fileOpenD(pprFile, defnsDir)) == null)
            {
              generalUtils.fileClose(fhO[0]);
              return -1;
            }
          }

          reportGenDetails.processSection("PH", data, fldNames, (short)50, dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0], pageSizeWidth[0], pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF,
                               userFontName, userFontStyle, userFontItalic, userFontSize, fhPPR[0], fhO[0], fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord, across2Coord, oBuf, gstRates);
        }
      }
      else // not on first, nor penultimate, nor last
      {
        if(lineCount[0] > docMaxSizeB)
        {
          reportGenDetails.outputLine('E', b, data, fldNames, (short)50, ' ', dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0], pageSizeWidth[0], pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF,
                           userFontName, userFontStyle, userFontItalic, userFontSize, fhPPR[0], fhO[0], fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord, across2Coord, oBuf, gstRates);
          lineCount[0] = 1;
          ++pageCount[0];
        }  
      }
    }   
    
    return 0;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void outputDetailLine(Connection con, Statement stmt, ResultSet rs, byte[] data, byte[] fldNames, byte[] lineNumB, byte[] itemCode, byte[] desc, byte[] unitPrice, byte[] quantity, byte[] amount2, byte[] gstRate,
                                byte[] discountPercentage, byte[] uom, byte[] custItemCode, byte[] entry, byte[] lastEntry, int[] lineCount, int[] pageCount, byte[] mfrCode, int numPages, int docSizeMax, int docSizeMaxA, int docSizeMaxB,
                                boolean onePager, boolean twoPager, boolean threePager, char dpOnQuantities, String unm, String dnm, String localDefnsDir, String defnsDir, double[] tm, double[] bm, double[] lm, double[] rm,
                                double[] pageSizeWidth, double[] pageSizeLength, double[] currDown, double[] currAcross, short[] currFont, short[] currPage, short[] oPtr, short oBufLen, boolean[] lastOperationPF, String[] userFontName,
                                String[] userFontStyle, String[] userFontItalic, String[] userFontSize, RandomAccessFile[] fhPPR, RandomAccessFile[] fhO, char[] fontDefType, char[] fontDefStyle, char[] fontDefItalic, int[] fontDefSize,
                                double[] down2Coord, double[] across2Coord, byte[] oBuf, String[] gstRates) throws Exception
  {
    byte[] b = new byte[100];
    
    generalUtils.repAlpha(data, 4000, (short)15, entry);//lineNumB);
    generalUtils.repAlpha(data, 4000, (short)16, itemCode);
    generalUtils.repAlpha(data, 4000, (short)17, desc);

    if(unitPrice[0] != '\000')
      generalUtils.bytesDPsGivenSeparator(true, '\000', '2', unitPrice, 20, 0);
    generalUtils.repAlpha(data, 4000, (short)18, unitPrice);
    
    if(quantity[0] != '\000')
    {
      generalUtils.bytesDPsGivenSeparator(true, '\000', dpOnQuantities, quantity, 20, 0);
      generalUtils.formatNumeric(b, dpOnQuantities);
    }
    generalUtils.repAlpha(data, 4000, (short)19, quantity);
        
    if(amount2[0] != '\000')
      generalUtils.bytesDPsGivenSeparator(true, '\000', '2', amount2, 20, 0);
    generalUtils.repAlpha(data, 4000, (short)21, amount2);
    
    generalUtils.repAlpha(data, 4000, (short)22, gstRate);
    
    if(discountPercentage[0] != '\000')
      generalUtils.bytesDPsGivenSeparator(true, '\000', '2', discountPercentage, 20, 0);
    generalUtils.repAlpha(data, 4000, (short)20, discountPercentage);
    
    generalUtils.repAlpha(data, 4000, (short)23, uom);
    generalUtils.repAlpha(data, 4000, (short)35, custItemCode);

    generalUtils.repAlpha(data, 4000, (short)45, "Based On:"); // linetext1
    generalUtils.repAlpha(data, 4000, (short)46, "per");       // linetext2

    if(miscDefinitions.hideDuplicateEntries(con, stmt, rs))
    {
      if(generalUtils.matchIgnoreCase(entry, 0, lastEntry, 0))
      {
        b[0] = '\000';
        generalUtils.repAlpha(data, 4000, (short)15, b); // entry
        generalUtils.repAlpha(data, 4000, (short)16, b); // itemCode
        generalUtils.repAlpha(data, 4000, (short)18, b); // unitPrice
        generalUtils.repAlpha(data, 4000, (short)20, b); // discountPercentage
        generalUtils.repAlpha(data, 4000, (short)21, b); // amount
        generalUtils.repAlpha(data, 4000, (short)49, b); // mfrCode
      }

      generalUtils.bytesToBytes(lastEntry, 0, entry, 0);
    }
    else
    {
      generalUtils.repAlpha(data, 4000, (short)15, entry);
      generalUtils.repAlpha(data, 4000, (short)16, itemCode);
      generalUtils.repAlpha(data, 4000, (short)17, desc);
      generalUtils.repAlpha(data, 4000, (short)49, mfrCode);
    }

    reportGenDetails.processSection("BL1", data, fldNames, (short)50, dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0], pageSizeWidth[0], pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName,
                         userFontStyle, userFontItalic, userFontSize, fhPPR[0], fhO[0], fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord, across2Coord, oBuf, gstRates);
    ++lineCount[0];
    if(chkForPageThrow(lineCount, pageCount, numPages, docSizeMax, docSizeMaxA, docSizeMaxB, onePager, twoPager, threePager, data, fldNames, unm, dnm, localDefnsDir, defnsDir, tm, bm, lm, rm, pageSizeWidth, pageSizeLength, currDown, currAcross,
                       currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName, userFontStyle, userFontItalic, userFontSize, fhPPR, fhO, fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord, across2Coord, oBuf, gstRates)
                       == -1)
    {
      ;
    } 
    
  }

}
