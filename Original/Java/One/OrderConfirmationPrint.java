// =======================================================================================================================================================================================================
// System: ZaraStar: Document: OC Print
// Module: OrderConfirmationPrint.java
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

public class OrderConfirmationPrint extends HttpServlet
{
  GeneralUtils  generalUtils = new GeneralUtils();
  MessagePage  messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils  directoryUtils = new DirectoryUtils();
  ReportGenDetails  reportGenDetails = new ReportGenDetails();
  OrderConfirmation  orderConfirmation = new OrderConfirmation();
  SalesOrder salesOrder = new SalesOrder();
  MiscDefinitions  miscDefinitions = new MiscDefinitions();
  AccountsUtils  accountsUtils = new AccountsUtils();
  Customer customer = new Customer();

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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "OrderConfirmationPrint", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 4051, bytesOut[0], 0, "ERR:" + p1);
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
    String reportsDir       = directoryUtils.getUserDir('R', dnm, unm);
    String workingDir       = directoryUtils.getUserDir('W', dnm, dnm + "/" + unm);
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

      if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 4051, unm, uty, dnm, localDefnsDir, defnsDir))
      {
        messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "4051", imagesDir, localDefnsDir, defnsDir, bytesOut);
        serverUtils.etotalBytes(req, unm, dnm, 4051, bytesOut[0], 0, "ACC:" + p1);
        if(con  != null) con.close();
        if(out != null) out.flush();
        return;
      }

      if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
      {
        messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "4051", imagesDir, localDefnsDir, defnsDir, bytesOut);
        serverUtils.etotalBytes(req, unm, dnm, 4051, bytesOut[0], 0, "SID:" + p1);
        if(con  != null) con.close();
        if(out != null) out.flush();
        return;
      }

      byte[] code = new byte[21];
      generalUtils.strToBytes(code, p1);

      int numPages = generate(con, stmt, rs, code, dnm, unm, workingDir, reportsDir, localDefnsDir, defnsDir, bytesOut);
      switch(numPages)
      {
        case -1 : // Definition File Not Found
                  messagePage.msgScreen(false, out, req, 17, unm, sid, uty, men, den, dnm, bnm, "4051", imagesDir, localDefnsDir, defnsDir, bytesOut);
                  break;
        case -2 : // cannot create report output file
                  messagePage.msgScreen(false, out, req, 18, unm, sid, uty, men, den, dnm, bnm, "4051", imagesDir, localDefnsDir, defnsDir, bytesOut);
                  break;
        default : // generated ok
                  out.println(generalUtils.intToStr(numPages));
                  break;
      }
    }
    catch(Exception e) { }    

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 4051, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con  != null) con.close();
    if(out != null) out.flush();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private int generate(Connection con, Statement stmt, ResultSet rs, byte[] code, String dnm, String unm, String workingDir, String reportsDir,
                       String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
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

    // fetch lines data in one go
    byte[] linesData  = new byte[2000];
    int[]  listLen    = new int[1];  listLen[0] = 2000;
    int[]  linesCount = new int[1];
    linesData = orderConfirmation.getLines(con, stmt, rs, code, '\001', linesData, listLen, linesCount, dnm, localDefnsDir, defnsDir);
    
    reportGenDetails.getGSTRatesFromDocument(linesData, linesCount[0], 7, gstRates);

    byte[] multipleLinesData  = new byte[2000];
    int[]  multipleListLen    = new int[1];  multipleListLen[0] = 2000;
    int[]  multipleLinesCount = new int[1];
    int llCount=0;
    if(linesCount[0] > 0) // get all the multiple lines for this document
    {
      multipleLinesData = orderConfirmation.getMultipleLines(con, stmt, rs, code, multipleLinesData, multipleListLen, multipleLinesCount, dnm, localDefnsDir,
                                                 defnsDir);
    }

    if((fhPPR[0] = generalUtils.fileOpenD("116.ppr", localDefnsDir)) == null)
    {
      if((fhPPR[0] = generalUtils.fileOpenD("116.ppr", defnsDir)) == null)
      {
        generalUtils.fileClose(fhO[0]);
        return -1;
      }
    }

    lastOperationPF[0] = false;

    byte[] fldNames = new byte[10000];
    byte[] data     = new byte[6500];

    generalUtils.putAlpha(fldNames, 6500, (short)0, "OC.OCCode");
    generalUtils.putAlpha(fldNames, 6500, (short)1, "OC.Date");
    generalUtils.putAlpha(fldNames, 6500, (short)2, "OC.CompanyCode");
    generalUtils.putAlpha(fldNames, 6500, (short)3, "OC.CompanyName");
    generalUtils.putAlpha(fldNames, 6500, (short)4, "OC.Address1");
    generalUtils.putAlpha(fldNames, 6500, (short)5, "OC.Address2");
    generalUtils.putAlpha(fldNames, 6500, (short)6, "OC.Address3");
    generalUtils.putAlpha(fldNames, 6500, (short)7, "OC.Address4");
    generalUtils.putAlpha(fldNames, 6500, (short)8, "OC.Address5");
    generalUtils.putAlpha(fldNames, 6500, (short)9, "OC.PostCode");
    generalUtils.putAlpha(fldNames, 6500, (short)10, "OC.FAO");
    generalUtils.putAlpha(fldNames, 6500, (short)11, "OCL.Entry");
    generalUtils.putAlpha(fldNames, 6500, (short)12, "OC.Misc1");
    generalUtils.putAlpha(fldNames, 6500, (short)13, "OC.Misc2");
    generalUtils.putAlpha(fldNames, 6500, (short)14, "OC.GSTTotal");
    generalUtils.putAlpha(fldNames, 6500, (short)15, "OC.TotalTotal");
    generalUtils.putAlpha(fldNames, 6500, (short)16, "OC.GroupDiscount");
    generalUtils.putAlpha(fldNames, 6500, (short)17, "OC.GroupDiscountType");
    generalUtils.putAlpha(fldNames, 6500, (short)18, "OC.TotalWithoutGST");
    generalUtils.putAlpha(fldNames, 6500, (short)19, "OC.SOCode");
    generalUtils.putAlpha(fldNames, 6500, (short)20, "OC.SODate");
    generalUtils.putAlpha(fldNames, 6500, (short)21, "OC.SignOn");
    generalUtils.putAlpha(fldNames, 6500, (short)22, "OC.Terms");
    generalUtils.putAlpha(fldNames, 6500, (short)23, "OC.Comment");
    generalUtils.putAlpha(fldNames, 6500, (short)24, "OC.Rate");
    generalUtils.putAlpha(fldNames, 6500, (short)25, "OC.Currency");
    generalUtils.putAlpha(fldNames, 6500, (short)26, "OC.ShipName");
    generalUtils.putAlpha(fldNames, 6500, (short)27, "OC.ShipAddress1");
    generalUtils.putAlpha(fldNames, 6500, (short)28, "OC.ShipAddress2");
    generalUtils.putAlpha(fldNames, 6500, (short)29, "OC.ShipAddress3");
    generalUtils.putAlpha(fldNames, 6500, (short)30, "OC.ShipAddress4");
    generalUtils.putAlpha(fldNames, 6500, (short)31, "OC.ShipAddress5");
    generalUtils.putAlpha(fldNames, 6500, (short)32, "OC.SalesPerson");
    generalUtils.putAlpha(fldNames, 6500, (short)33, "OC.CustomerPOCode");
    generalUtils.putAlpha(fldNames, 6500, (short)34, "OCL.LineNumber");
    generalUtils.putAlpha(fldNames, 6500, (short)35, "OCL.ItemCode");
    generalUtils.putAlpha(fldNames, 6500, (short)36, "OCL.Description");
    generalUtils.putAlpha(fldNames, 6500, (short)37, "OCL.UnitPrice");
    generalUtils.putAlpha(fldNames, 6500, (short)38, "OCL.Quantity");
    generalUtils.putAlpha(fldNames, 6500, (short)39, "OCL.Amount");
    generalUtils.putAlpha(fldNames, 6500, (short)40, "OCL.GSTRate");
    generalUtils.putAlpha(fldNames, 6500, (short)41, "OCL.UoM");
    generalUtils.putAlpha(fldNames, 6500, (short)42, "OCL.DeliveryDate");
    generalUtils.putAlpha(fldNames, 6500, (short)43, "OCL.Remark");
    generalUtils.putAlpha(fldNames, 6500, (short)44, "OCL.CustomerItemCode");
    generalUtils.putAlpha(fldNames, 6500, (short)45, "OCL.ActualQuantity");
    generalUtils.putAlpha(fldNames, 6500, (short)46, "OCL.Discount");
    generalUtils.putAlpha(fldNames, 6500, (short)47, "OC.NoteLine1");
    generalUtils.putAlpha(fldNames, 6500, (short)48, "OC.NoteLines");
    generalUtils.putAlpha(fldNames, 6500, (short)49, "Customer.Phone1");
    generalUtils.putAlpha(fldNames, 6500, (short)50, "Customer.Fax");
    generalUtils.putAlpha(fldNames, 6500, (short)51, "OCL.Manufacturer");
    generalUtils.putAlpha(fldNames, 6500, (short)52, "OCL.ManufacturerCode");
        
    double groupDiscount=0.0, gstTotal=0, total=0;
    String currency="";
    byte[] customerCode = new byte[21];

    if(orderConfirmation.getRecGivenCode(con, stmt, rs, code, '\000', tmp, dnm, localDefnsDir, defnsDir, bytesOut) != -1L) // just-in-case
    {
      generalUtils.putAlpha(data, 6500, (short)0, code);
      generalUtils.dfs(tmp, (short)1,  b); generalUtils.putAlpha(data, 6500, (short)1,  generalUtils.convertFromYYYYMMDD(b)); // date
      generalUtils.dfs(tmp, (short)2,  customerCode); generalUtils.putAlpha(data, 6500, (short)2,  customerCode); // companycode
      generalUtils.dfs(tmp, (short)3,  b); generalUtils.putAlpha(data, 6500, (short)3,  b); // companyname
      generalUtils.dfs(tmp, (short)4,  b); generalUtils.putAlpha(data, 6500, (short)4,  b); // address1
      generalUtils.dfs(tmp, (short)5,  b); generalUtils.putAlpha(data, 6500, (short)5,  b); // address2
      generalUtils.dfs(tmp, (short)6,  b); generalUtils.putAlpha(data, 6500, (short)6,  b); // address3
      generalUtils.dfs(tmp, (short)7,  b); generalUtils.putAlpha(data, 6500, (short)7,  b); // address4
      generalUtils.dfs(tmp, (short)8,  b); generalUtils.putAlpha(data, 6500, (short)8,  b); // address5
      generalUtils.dfs(tmp, (short)9,  b); generalUtils.putAlpha(data, 6500, (short)9,  b); // postcode
      generalUtils.dfs(tmp, (short)10, b); generalUtils.putAlpha(data, 6500, (short)10, b); // FAO
      generalUtils.dfs(tmp, (short)14, b); generalUtils.putAlpha(data, 6500, (short)12, b); // misc1
      generalUtils.dfs(tmp, (short)15, b); generalUtils.putAlpha(data, 6500, (short)13, b); // misc2

      generalUtils.dfs(tmp, (short)16, b); 
      generalUtils.doubleDPs(b, b, '2');
      generalUtils.putAlpha(data, 6500, (short)14, b); // gsttotal
      
      generalUtils.dfs(tmp, (short)17, b); 
      generalUtils.doubleDPs(b, b, '2');
      generalUtils.putAlpha(data, 6500, (short)15, b); // totaltotal

      gstTotal = generalUtils.dfsAsDouble(tmp, (short)16);
      total    = generalUtils.dfsAsDouble(tmp, (short)17);

      generalUtils.dfs(tmp, (short)43, b); 
      generalUtils.doubleDPs(b, b, '2');
      generalUtils.putAlpha(data, 6500, (short)16, b); // groupdiscount

      groupDiscount = generalUtils.dfsAsDouble(tmp, (short)43);

      generalUtils.dfs(tmp, (short)44, b);
      b2[0] = ' ';
      generalUtils.bytesToBytes(b2, 1, b, 0);
      generalUtils.putAlpha(data, 6500, (short)17, b); // groupdiscounttype

      double totalSansGST = generalUtils.doubleDPs(total, '2') - generalUtils.doubleDPs(gstTotal, '2');

      generalUtils.repDoubleGivenSeparator('2', '\000', data, 6500, (short)18, totalSansGST);

      generalUtils.dfs(tmp, (short)38, b); generalUtils.putAlpha(data, 6500, (short)32, b); // salesperson
      
      generalUtils.dfs(tmp, (short)42, b); generalUtils.putAlpha(data, 6500, (short)19, b); // socode
      String soDate = salesOrder.getASOFieldGivenCode(con, stmt, rs, "Date", generalUtils.stringFromBytes(b, 0L));
      generalUtils.putAlpha(data, 6500, (short)20, soDate);

      generalUtils.dfs(tmp, (short)39, b); generalUtils.putAlpha(data, 6500, (short)33, b); // custPOCode
      generalUtils.dfs(tmp, (short)22, b); generalUtils.putAlpha(data, 6500, (short)21, b); // signon

      generalUtils.dfs(tmp, (short)28, b); generalUtils.putAlpha(data, 6500, (short)22, b); // terms
      
      generalUtils.dfs(tmp, (short)31, b); generalUtils.putAlpha(data, 6500, (short)25, b); // currency
      generalUtils.putAlpha(data, 6500, (short)51, b); currency = generalUtils.dfsAsStr(tmp, (short)31);
      
      generalUtils.dfs(tmp, (short)30, b); generalUtils.putAlpha(data, 6500, (short)24, b); // rate
      // create the exchange rate msg line (if this curr != base curr)
      if(! currency.equalsIgnoreCase(accountsUtils.getBaseCurrency(con, stmt, rs, dnm, localDefnsDir, defnsDir)))
      {
        generalUtils.formatNumeric(b, '2');
        generalUtils.putAlpha(data, 6500, (short)23, "Exchange Rate: " + generalUtils.stringFromBytes(b, 0L));
      }
      
      generalUtils.dfs(tmp, (short)32, b); generalUtils.putAlpha(data, 6500, (short)26, b); // shipname
      generalUtils.dfs(tmp, (short)33, b); generalUtils.putAlpha(data, 6500, (short)27, b); // shipaddr1
      generalUtils.dfs(tmp, (short)34, b); generalUtils.putAlpha(data, 6500, (short)28, b); // shipaddr2
      generalUtils.dfs(tmp, (short)35, b); generalUtils.putAlpha(data, 6500, (short)29, b); // shipaddr3
      generalUtils.dfs(tmp, (short)36, b); generalUtils.putAlpha(data, 6500, (short)30, b); // shipaddr4
      generalUtils.dfs(tmp, (short)37, b); generalUtils.putAlpha(data, 6500, (short)31, b); // shipaddr5

      String phone1 = customer.getACompanyFieldGivenCode(con, stmt, rs, "Phone1", generalUtils.stringFromBytes(customerCode, 0L));
      String fax    = customer.getACompanyFieldGivenCode(con, stmt, rs, "Fax",    generalUtils.stringFromBytes(customerCode, 0L));
      
      generalUtils.putAlpha(data, 4000, (short)49, phone1);
      generalUtils.putAlpha(data, 4000, (short)50, fax);    
    }

    reportGenDetails.processControl(dnm, unm, localDefnsDir, defnsDir, dp, negStyle, zeroStyle, tm, bm, lm, rm, pageSizeWidth, pageSizeLength, currDown,
                         currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName, userFontStyle, userFontItalic, userFontSize,
                         fhPPR[0], fhO[0], fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord, across2Coord, oBuf, gstRates);
    
    reportGenDetails.processSection("RH", null, null, (short)0, dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0], pageSizeWidth[0],
                         pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName, userFontStyle,
                         userFontItalic, userFontSize, fhPPR[0], fhO[0], fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord,
                         across2Coord, oBuf, gstRates);
    reportGenDetails.processSection("PH", data, fldNames, (short)53, dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0], pageSizeWidth[0],
                         pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName, userFontStyle,
                         userFontItalic, userFontSize, fhPPR[0], fhO[0], fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord,
                         across2Coord, oBuf, gstRates);

    char dpOnQuantities = miscDefinitions.dpOnQuantities(con, stmt, rs, "5");
    byte[] lastItemCode = new byte[21];  lastItemCode[0] = '\000';
    byte[] itemCode     = new byte[21];
    byte[] lineNumB     = new byte[20];
    byte[] entry        = new byte[20];
    byte[] desc      = new byte[81];
    byte[] unitPrice = new byte[21];
    byte[] quantity  = new byte[20];
    byte[] actualQuantity = new byte[20];
    byte[] amount             = new byte[20];
    byte[] gstRate            = new byte[20];
    byte[] discountPercentage = new byte[20];
    byte[] uom      = new byte[20];
    byte[] custItemCode = new byte[41];
    byte[] mfr          = new byte[31];
    byte[] mfrCode      = new byte[61];
    byte[] data2        = new byte[1000];
    int[] lineCount = new int[1];  lineCount[0] = 1;
    int[] numPages  = new int[1];  numPages[0] = 1;
    byte[] lastEntry = new byte[6];      lastEntry[0] = '\000';
    byte[] deliveryDate   = new byte[20];

    lineCount[0] = 1;
    int docSizeMax = miscDefinitions.docSizeMax(con, stmt, rs, "oc");

    for(int xx=0;xx<linesCount[0];++xx)
    {
      if(generalUtils.getListEntryByNum(xx, linesData, tmp)) // just-in-case
      {
        generalUtils.replaceTwosWithOnes(tmp);

        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)22, lineNumB);
        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)2,  itemCode);
        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)23, entry);
        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)3,  desc);
        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)4,  unitPrice);
        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)5,  quantity);
        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)20, amount);
        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)7,  gstRate);
        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)9,  actualQuantity);
        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)28, discountPercentage);
        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)27, custItemCode);
        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)21, uom);
        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)29, mfr);
        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)30, mfrCode);
        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)25, deliveryDate);

        if(miscDefinitions.prependMfrCode(con, stmt, rs) && mfrCode[0] != '\000')
        {  
          outputDetailLine(con, stmt, rs, data, fldNames, lineNumB, itemCode, mfrCode, unitPrice, quantity, actualQuantity, amount, gstRate, discountPercentage, uom,
                           custItemCode, entry, lastEntry, lineCount, mfr, mfrCode, numPages, docSizeMax, dpOnQuantities, unm, dnm, localDefnsDir, defnsDir, tm,
                           bm, lm, rm, pageSizeWidth, pageSizeLength, currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF,
                           userFontName, userFontStyle, userFontItalic, userFontSize, fhPPR, fhO, fontDefType, fontDefStyle, fontDefItalic,
                           fontDefSize, down2Coord, across2Coord, oBuf, gstRates);
          entry[0] = '\000'; unitPrice[0] = '\000'; quantity[0] = '\000'; actualQuantity[0] = '\000'; discountPercentage[0] = '\000'; amount[0] = '\000';
          gstRate[0] = '\000'; uom[0] = '\000';
        }

        outputDetailLine(con, stmt, rs, data, fldNames, lineNumB, itemCode, desc, unitPrice, quantity, actualQuantity, amount, gstRate, discountPercentage, uom,
                         custItemCode, entry, lastEntry, lineCount, mfr, mfrCode, numPages, docSizeMax, dpOnQuantities, unm, dnm, localDefnsDir, defnsDir, tm,
                         bm, lm, rm, pageSizeWidth, pageSizeLength, currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF,
                         userFontName, userFontStyle, userFontItalic, userFontSize, fhPPR, fhO, fontDefType, fontDefStyle, fontDefItalic, fontDefSize,
                         down2Coord, across2Coord, oBuf, gstRates);
        

        // output any LL lines
        b[0] = '\000';
        generalUtils.repAlpha(data, 6500, (short)11, b); // entry
        generalUtils.repAlpha(data, 6500, (short)34, b); // line
        generalUtils.repAlpha(data, 6500, (short)35, b); // itemCode
        generalUtils.repAlpha(data, 6500, (short)36, b); // desc
        generalUtils.repAlpha(data, 6500, (short)37, b); // unitprice
        generalUtils.repAlpha(data, 6500, (short)38, b); // qty
        generalUtils.repAlpha(data, 6500, (short)46, b); // discount%
        generalUtils.repAlpha(data, 6500, (short)39, b); // amt
        generalUtils.repAlpha(data, 6500, (short)40, b); // gstrate
        generalUtils.repAlpha(data, 6500, (short)41, b); // uom
        generalUtils.repAlpha(data, 6500, (short)44, b); // custitemcode
        generalUtils.repAlpha(data, 6500, (short)45, b); // actualqty

        generalUtils.repAlpha(data, 6500, (short)47, b); // linetext1
        generalUtils.repAlpha(data, 6500, (short)48, b); // linetext2
        generalUtils.repAlpha(data, 6500, (short)51, b); // mfr
        generalUtils.repAlpha(data, 6500, (short)52, b); // mfrCode

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
            generalUtils.repAlpha(data, 6500, (short)36, b); // desc
            reportGenDetails.processSection("BL1", data, fldNames, (short)53, dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0], pageSizeWidth[0],
                         pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName, userFontStyle,
                         userFontItalic, userFontSize, fhPPR[0], fhO[0], fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord,
                         across2Coord, oBuf, gstRates);
            ++lineCount[0];
            if(lineCount[0] >= docSizeMax)
            {
              b[0] = '\000';
              reportGenDetails.outputLine('E', b, data, fldNames, (short)53, ' ', dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0],
                               pageSizeWidth[0], pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF,
                               userFontName, userFontStyle, userFontItalic, userFontSize, fhPPR[0], fhO[0], fontDefType, fontDefStyle, fontDefItalic,
                               fontDefSize, down2Coord, across2Coord, oBuf, gstRates);
              ++ numPages[0];
              lineCount[0] = 1;
            }

            llCount = generalUtils.getListEntryByNumAndValue(llCount, multipleLinesCount[0], lineNumB, multipleLinesData, data2);
            if(llCount == -1)
              quit1 = true; // on last rec
            else
            {
              if(   ! generalUtils.match(code, generalUtils.dfsAsStr(data2, (short)1))
                 || ! generalUtils.match(lineNumB, generalUtils.dfsAsStr(data2, (short)2)) ) // stepped on
              {
                quit1 = true;
              }  
            }
          }
        }

        // append the delivery date (if it exists)
        if(deliveryDate[0] != '\000' && ! generalUtils.match(deliveryDate, "1970-01-01"))
        {
          if(miscDefinitions.appendDeliveryDate(con, stmt, rs))
          {
            generalUtils.repAlpha(data, 6500, (short)36, "Delivery Date: " + generalUtils.convertFromYYYYMMDD2(deliveryDate));
            b[0] = '\000';
            generalUtils.repAlpha(data, 6500, (short)11, b); // entry
            generalUtils.repAlpha(data, 6500, (short)34, b); // line
            generalUtils.repAlpha(data, 6500, (short)35, b); // itemCode
            generalUtils.repAlpha(data, 6500, (short)37, b); // unitprice
            generalUtils.repAlpha(data, 6500, (short)38, b); // qty
            generalUtils.repAlpha(data, 6500, (short)46, b); // discount%
            generalUtils.repAlpha(data, 6500, (short)39, b); // amt
            generalUtils.repAlpha(data, 6500, (short)40, b); // gstrate
            generalUtils.repAlpha(data, 6500, (short)41, b); // uom
            generalUtils.repAlpha(data, 6500, (short)44, b); // custitemcode
            generalUtils.repAlpha(data, 6500, (short)45, b); // actualqty

            generalUtils.repAlpha(data, 6500, (short)47, b); // linetext1
            generalUtils.repAlpha(data, 6500, (short)48, b); // linetext2
            generalUtils.repAlpha(data, 6500, (short)51, b); // mfr
            generalUtils.repAlpha(data, 6500, (short)52, b); // mfrCode

            reportGenDetails.processSection("BL1", data, fldNames, (short)53, dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0], pageSizeWidth[0],
                         pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName, userFontStyle,
                         userFontItalic, userFontSize, fhPPR[0], fhO[0], fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord,
                         across2Coord, oBuf, gstRates);
            ++lineCount[0];
        
            if(lineCount[0] >= docSizeMax)
            {
              b[0] = '\000';
              reportGenDetails.outputLine('E', b, data, fldNames, (short)53, ' ', dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0],
                               pageSizeWidth[0], pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF,
                               userFontName, userFontStyle, userFontItalic, userFontSize, fhPPR[0], fhO[0], fontDefType, fontDefStyle, fontDefItalic,
                               fontDefSize, down2Coord, across2Coord, oBuf, gstRates);
              ++numPages[0];
              lineCount[0] = 1;
            }
          }
        }
      }
    }

    orderConfirmation.getRecGivenCode(con, stmt, rs, code, '\000', tmp, dnm, localDefnsDir, defnsDir, bytesOut);
      
    generalUtils.dfs(tmp, (short)11, b);
    int x=0,  y=0;
    while(b[x] != '\001' && b[x] != '\000')
      b2[y++] = b[x++];
    b2[y] = '\000';
    generalUtils.repAlpha(data, 6500, (short)35, b2);

    reportGenDetails.processSection("BL2", data, fldNames, (short)53, dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0], pageSizeWidth[0],
                         pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName, userFontStyle,
                         userFontItalic, userFontSize, fhPPR[0], fhO[0], fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord,
                         across2Coord, oBuf, gstRates);

    // any other note lines
    if(b[x] == '\001')
      ++x; // lf
    while(b[x] != '\000')
    {
      y=0;
      while(b[x] != '\001' && b[x] != '\000')
         b2[y++] = b[x++];
      b2[y] = '\000';
      generalUtils.repAlpha(data, 6500, (short)36, b2);

      reportGenDetails.processSection("BL3", data, fldNames, (short)53, dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0], pageSizeWidth[0],
                           pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName, userFontStyle,
                           userFontItalic, userFontSize, fhPPR[0], fhO[0], fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord,
                           across2Coord, oBuf, gstRates);

      if(b[x] == '\001')
       ++x; // lf
    }

    reportGenDetails.processSection("PF", data, fldNames, (short)53, dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0], pageSizeWidth[0],
                         pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName, userFontStyle,
                         userFontItalic, userFontSize, fhPPR[0], fhO[0], fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord,
                         across2Coord, oBuf, gstRates);

    if(groupDiscount == 0.0)
    {
      reportGenDetails.processSection("RF1", data, fldNames, (short)53, dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0], pageSizeWidth[0],
                           pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName, userFontStyle,
                           userFontItalic, userFontSize, fhPPR[0], fhO[0], fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord,
                           across2Coord, oBuf, gstRates);
    }   
    else // there is a discount
    {   
      reportGenDetails.processSection("RF2", data, fldNames, (short)53, dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0], pageSizeWidth[0],
                           pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName, userFontStyle,
                           userFontItalic, userFontSize, fhPPR[0], fhO[0], fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord,
                           across2Coord, oBuf, gstRates);
    }
    
    fhO[0].write(oBuf, 0, oPtr[0]);

    generalUtils.fileClose(fhO[0]);
    generalUtils.fileClose(fhPPR[0]);

    PrintingLayout printingLayout = new PrintingLayout();
    printingLayout.updateNumPages("0.000", reportsDir);

    return numPages[0];
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void outputDetailLine(Connection con, Statement stmt, ResultSet rs, byte[] data, byte[] fldNames, byte[] lineNumB, byte[] itemCode, byte[] desc, byte[] unitPrice, byte[] quantity,
                                byte[] actualQuantity, byte[] amount2, byte[] gstRate, byte[] discountPercentage, byte[] uom, byte[] custItemCode,
                                byte[] entry, byte[] lastEntry, int[] lineCount, byte[] mfr, byte[] mfrCode, int[] numPages, int docSizeMax, char dpOnQuantities, String unm,
                                String dnm, String localDefnsDir, String defnsDir, double[] tm, double[] bm, double[] lm, double[] rm,
                                double[] pageSizeWidth, double[] pageSizeLength, double[] currDown, double[] currAcross, short[] currFont,
                                short[] currPage, short[] oPtr, short oBufLen, boolean[] lastOperationPF, String[] userFontName,
                                String[] userFontStyle, String[] userFontItalic, String[] userFontSize, RandomAccessFile[] fhPPR,
                                RandomAccessFile[] fhO, char[] fontDefType, char[] fontDefStyle, char[] fontDefItalic, int[] fontDefSize,
                                double[] down2Coord, double[] across2Coord, byte[] oBuf, String[] gstRates) throws Exception
  {
    byte[] b = new byte[100];
    
    generalUtils.repAlpha(data, 4000, (short)11, entry);//lineNumB);
    generalUtils.repAlpha(data, 4000, (short)35, itemCode);
    generalUtils.repAlpha(data, 4000, (short)36, desc);

    if(unitPrice[0] != '\000')
      generalUtils.bytesDPsGivenSeparator(true, '\000', '2', unitPrice, 20, 0);
    generalUtils.repAlpha(data, 4000, (short)37, unitPrice);
    
    if(quantity[0] != '\000')
    {
      generalUtils.bytesDPsGivenSeparator(true, '\000', dpOnQuantities, quantity, 20, 0);
      generalUtils.formatNumeric(b, dpOnQuantities);
    }
    generalUtils.repAlpha(data, 4000, (short)38, quantity);
        
    if(actualQuantity[0] != '\000')
    {
      generalUtils.bytesDPsGivenSeparator(true, '\000', dpOnQuantities, actualQuantity, 20, 0);
      generalUtils.formatNumeric(b, dpOnQuantities);
    }
    generalUtils.repAlpha(data, 4000, (short)45, actualQuantity);
        
    if(amount2[0] != '\000')
      generalUtils.bytesDPsGivenSeparator(true, '\000', '2', amount2, 20, 0);
    generalUtils.repAlpha(data, 4000, (short)39, amount2);
    
    generalUtils.repAlpha(data, 4000, (short)40, gstRate);
    
    if(discountPercentage[0] != '\000')
      generalUtils.bytesDPsGivenSeparator(true, '\000', '2', discountPercentage, 20, 0);
    generalUtils.repAlpha(data, 4000, (short)46, discountPercentage);
    
    generalUtils.repAlpha(data, 4000, (short)41, uom);
    generalUtils.repAlpha(data, 4000, (short)44, custItemCode);

    if(miscDefinitions.hideDuplicateEntries(con, stmt, rs))
    {
      if(generalUtils.matchIgnoreCase(entry, 0, lastEntry, 0))
      {
        b[0] = '\000';
        generalUtils.repAlpha(data, 4000, (short)11, b); // entry
        generalUtils.repAlpha(data, 4000, (short)35, b); // itemCode
        generalUtils.repAlpha(data, 4000, (short)37, b); // unitPrice
        generalUtils.repAlpha(data, 4000, (short)46, b); // discountPercentage
        generalUtils.repAlpha(data, 4000, (short)39, b); // amount
        generalUtils.repAlpha(data, 4000, (short)51, b); // mfr
        generalUtils.repAlpha(data, 4000, (short)52, b); // mfrCode
      }

      generalUtils.bytesToBytes(lastEntry, 0, entry, 0);
    }
    else
    {
      generalUtils.repAlpha(data, 4000, (short)11, entry);
      generalUtils.repAlpha(data, 4000, (short)35, itemCode);
      generalUtils.repAlpha(data, 4000, (short)36, desc);
      generalUtils.repAlpha(data, 4000, (short)51, mfr);
      generalUtils.repAlpha(data, 4000, (short)52, mfrCode);
    }

    reportGenDetails.processSection("BL1", data, fldNames, (short)53, dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0], pageSizeWidth[0],
                         pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName, userFontStyle,
                         userFontItalic, userFontSize, fhPPR[0], fhO[0], fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord,
                         across2Coord, oBuf, gstRates);
    ++lineCount[0];
    if(lineCount[0] >= docSizeMax)
    {
      b[0] = '\000';
      reportGenDetails.outputLine('E', b, data, fldNames, (short)53, ' ', dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0], pageSizeWidth[0],
                         pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName, userFontStyle,
                         userFontItalic, userFontSize, fhPPR[0], fhO[0], fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord,
                         across2Coord, oBuf, gstRates);
      ++ numPages[0];
      lineCount[0] = 1;
    }
  }
  
}
