// =======================================================================================================================================================================================================
// System: ZaraStar: Document: Receipt print
// Module: ReceiptPrint.java
// Author: C.K.Harvey
// Copyright (c) 1999-2008 Christopher Harvey. All Rights Reserved.
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

public class ReceiptPrint extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  ReportGenDetails reportGenDetails = new ReportGenDetails();
  Receipt receipt = new Receipt();
  MiscDefinitions miscDefinitions = new MiscDefinitions();
  AccountsUtils accountsUtils = new AccountsUtils();
  Customer customer = new Customer();
  SalesInvoice salesInvoice = new SalesInvoice();
  
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "ReceiptPrint", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 4212, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush();
    }
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm,
                    String bnm, String p1, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String reportsDir       = directoryUtils.getUserDir('R', dnm, unm);
    String workingDir       = directoryUtils.getUserDir('W', dnm, dnm + "/" + unm);
    String imagesDir     = directoryUtils.getSupportDirs('I');

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;
    
    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 4212, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "4212", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 4212, bytesOut[0], 0, "ACC:" + p1);
    if(con  != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "4212", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 4212, bytesOut[0], 0, "SID:" + p1);
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
                messagePage.msgScreen(false, out, req, 17, unm, sid, uty, men, den, dnm, bnm, "4212", imagesDir, localDefnsDir, defnsDir,
                                   bytesOut);
                break;
      case -2 : // cannot create report output file
                messagePage.msgScreen(false, out, req, 18, unm, sid, uty, men, den, dnm, bnm, "4212", imagesDir, localDefnsDir, defnsDir,
                                   bytesOut);
                break;
      default : // generated ok
                out.println(generalUtils.intToStr(numPages));
                break;
    }

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 4212, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con  != null) con.close();
    if(out != null) out.flush();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private int generate(Connection con, Statement stmt, ResultSet rs, byte[] code, String dnm, String unm, String workingDir, String reportsDir, String localDefnsDir, String defnsDir,
                       int[] bytesOut) throws Exception
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
    linesData = receipt.getLines(con, stmt, rs, code, '\001', linesData, listLen, linesCount, dnm, localDefnsDir, defnsDir);
    
    if((fhPPR[0] = generalUtils.fileOpenD("066.ppr", localDefnsDir)) == null)
    {
      if((fhPPR[0] = generalUtils.fileOpenD("066.ppr", defnsDir)) == null)
      {
        generalUtils.fileClose(fhO[0]);
        return -1;
      }
    }

    lastOperationPF[0] = false;

    byte[] fldNames = new byte[6500];
    byte[] data     = new byte[6500];
 
    generalUtils.repAlpha(fldNames, 6500, (short)0,  "Receipt.ReceiptCode");
    generalUtils.repAlpha(fldNames, 6500, (short)1,  "Receipt.Date");
    generalUtils.repAlpha(fldNames, 6500, (short)2,  "Receipt.CompanyCode");
    generalUtils.repAlpha(fldNames, 6500, (short)3,  "Receipt.CompanyName");
    generalUtils.repAlpha(fldNames, 6500, (short)4,  "Receipt.Address1");
    generalUtils.repAlpha(fldNames, 6500, (short)5,  "Receipt.Address2");
    generalUtils.repAlpha(fldNames, 6500, (short)6,  "Receipt.Address3");
    generalUtils.repAlpha(fldNames, 6500, (short)7,  "Receipt.Address4");
    generalUtils.repAlpha(fldNames, 6500, (short)8,  "Receipt.Address5");
    generalUtils.repAlpha(fldNames, 6500, (short)9,  "Receipt.PostCode");
    generalUtils.repAlpha(fldNames, 6500, (short)10, "Receipt.FAO");
    generalUtils.repAlpha(fldNames, 6500, (short)11, "Receipt.Attention");
    generalUtils.repAlpha(fldNames, 6500, (short)12, "Receipt.Misc1");
    generalUtils.repAlpha(fldNames, 6500, (short)13, "Receipt.Misc2");
    generalUtils.repAlpha(fldNames, 6500, (short)14, "Receipt.TotalAmount");
    generalUtils.repAlpha(fldNames, 6500, (short)15, "Receipt.DateReceived");
    generalUtils.repAlpha(fldNames, 6500, (short)16, "Receipt.AccCredited");
    generalUtils.repAlpha(fldNames, 6500, (short)17, "Receipt.AccDebited");
    generalUtils.repAlpha(fldNames, 6500, (short)18, "Receipt.SignOn");
    generalUtils.repAlpha(fldNames, 6500, (short)19, "Receipt.ProjectCode");
    generalUtils.repAlpha(fldNames, 6500, (short)20, "Receipt.GSTComponent");
    generalUtils.repAlpha(fldNames, 6500, (short)21, "Receipt.ChequeNumber");
    generalUtils.repAlpha(fldNames, 6500, (short)22, "Receipt.PrintCount");
    generalUtils.repAlpha(fldNames, 6500, (short)23, "Receipt.ARCode");
    generalUtils.repAlpha(fldNames, 6500, (short)24, "Receipt.Currency");
    generalUtils.repAlpha(fldNames, 6500, (short)25, "Receipt.Rate");
    generalUtils.repAlpha(fldNames, 6500, (short)26, "Receipt.BaseTotalAmount");
    generalUtils.repAlpha(fldNames, 6500, (short)27, "Receipt.BaseGSTComponent");
    generalUtils.repAlpha(fldNames, 6500, (short)28, "Receipt.CashOrNot");
    generalUtils.repAlpha(fldNames, 6500, (short)29, "Receipt.ReceiptReference");
    generalUtils.repAlpha(fldNames, 6500, (short)30, "Receipt.BankAccount");
    generalUtils.repAlpha(fldNames, 6500, (short)31, "Receipt.Charges");
    generalUtils.repAlpha(fldNames, 6500, (short)32, "Receipt.DiscountAllowed");
    generalUtils.repAlpha(fldNames, 6500, (short)33, "Receipt.BankAmount");
    generalUtils.repAlpha(fldNames, 6500, (short)34, "Receipt.RateBankToBase");
    generalUtils.repAlpha(fldNames, 6500, (short)35, "Receipt.ExchangeAdjustment");
    generalUtils.repAlpha(fldNames, 6500, (short)36, "ReceiptL.InvoiceCode");
    generalUtils.repAlpha(fldNames, 6500, (short)37, "ReceiptL.InvoiceDate");
    generalUtils.repAlpha(fldNames, 6500, (short)38, "ReceiptL.InvoiceAmount");
    generalUtils.repAlpha(fldNames, 6500, (short)39, "ReceiptL.Description");
    generalUtils.repAlpha(fldNames, 6500, (short)40, "ReceiptL.AmountReceived");
    generalUtils.repAlpha(fldNames, 6500, (short)41, "ReceiptL.SignOn");
    generalUtils.repAlpha(fldNames, 6500, (short)42, "ReceiptL.OriginalRate");
    generalUtils.repAlpha(fldNames, 6500, (short)43, "ReceiptL.GSTComponent");
    generalUtils.repAlpha(fldNames, 6500, (short)44, "ReceiptL.AccountDr");
    generalUtils.repAlpha(fldNames, 6500, (short)45, "ReceiptL.InvoiceLine");
    generalUtils.repAlpha(fldNames, 6500, (short)46, "ReceiptL.Line");
    generalUtils.repAlpha(fldNames, 6500, (short)47, "ReceiptL.Entry");
    generalUtils.repAlpha(fldNames, 6500, (short)48, "ReceiptL.BaseAmountPaid");
    generalUtils.repAlpha(fldNames, 6500, (short)49, "ReceiptL.BaseGSTComponent");
    generalUtils.repAlpha(fldNames, 6500, (short)50, "Receipt.NoteLine1");
    generalUtils.repAlpha(fldNames, 6500, (short)51, "Receipt.NoteLines");
    generalUtils.repAlpha(fldNames, 6500, (short)52, "Customer.CustomerName");
    generalUtils.repAlpha(fldNames, 6500, (short)53, "Customer.Address1");
    generalUtils.repAlpha(fldNames, 6500, (short)54, "Customer.Address2");
    generalUtils.repAlpha(fldNames, 6500, (short)55, "Customer.Address3");
    generalUtils.repAlpha(fldNames, 6500, (short)56, "Customer.Address4");
    generalUtils.repAlpha(fldNames, 6500, (short)57, "Customer.Address5");
    generalUtils.repAlpha(fldNames, 6500, (short)58, "Customer.BankName");
    generalUtils.repAlpha(fldNames, 6500, (short)59, "Customer.BankCode");
    generalUtils.repAlpha(fldNames, 6500, (short)60, "Customer.BankAccount");
    generalUtils.repAlpha(fldNames, 6500, (short)61, "Customer.BankBranchCode");
    generalUtils.repAlpha(fldNames, 6500, (short)62, "Document.Date");
    generalUtils.repAlpha(fldNames, 6500, (short)63, "Document.Code");
    
    String currency="";
    byte[] companyCode = new byte[21];
    byte[] data2 = new byte[5000];

    if(receipt.getRecGivenCode(con, stmt, rs, code, '\000', tmp, dnm, localDefnsDir, defnsDir, bytesOut) != -1L) // just-in-case
    {
      generalUtils.repAlpha(data, 6500, (short)0, code);
      generalUtils.dfs(tmp, (short)1,  b); generalUtils.repAlpha(data, 6500, (short)1,  generalUtils.convertFromYYYYMMDD(b)); // date
      generalUtils.dfs(tmp, (short)2,  companyCode); generalUtils.repAlpha(data, 6500, (short)2, companyCode);
      generalUtils.dfs(tmp, (short)3,  b); generalUtils.repAlpha(data, 6500, (short)3,  b); // companyname
      generalUtils.dfs(tmp, (short)4,  b); generalUtils.repAlpha(data, 6500, (short)4,  b); // address1
      generalUtils.dfs(tmp, (short)5,  b); generalUtils.repAlpha(data, 6500, (short)5,  b); // address2
      generalUtils.dfs(tmp, (short)6,  b); generalUtils.repAlpha(data, 6500, (short)6,  b); // address3
      generalUtils.dfs(tmp, (short)7,  b); generalUtils.repAlpha(data, 6500, (short)7,  b); // address4
      generalUtils.dfs(tmp, (short)8,  b); generalUtils.repAlpha(data, 6500, (short)8,  b); // address5
      generalUtils.dfs(tmp, (short)9,  b); generalUtils.repAlpha(data, 6500, (short)9,  b); // postcode
      generalUtils.dfs(tmp, (short)10, b); generalUtils.repAlpha(data, 6500, (short)10, b); // FAO
      generalUtils.dfs(tmp, (short)14, b); generalUtils.repAlpha(data, 6500, (short)12, b); // misc1
      generalUtils.dfs(tmp, (short)15, b); generalUtils.repAlpha(data, 6500, (short)13, b); // misc2

      generalUtils.dfs(tmp, (short)24, b); 
      generalUtils.doubleDPs(b, b, '2');
      generalUtils.repAlpha(data, 6500, (short)20, b); // gstcomponent
      
      generalUtils.dfs(tmp, (short)15, b); 
      generalUtils.doubleDPs(b, b, '2');
      generalUtils.repAlpha(data, 6500, (short)14, b); // totalAmount

      generalUtils.dfs(tmp, (short)16, b); generalUtils.repAlpha(data, 6500, (short)15, generalUtils.convertFromYYYYMMDD(b)); // dateReceived
      generalUtils.dfs(tmp, (short)17, b); generalUtils.repAlpha(data, 6500, (short)16, b); // accCredited
      generalUtils.dfs(tmp, (short)18, b); generalUtils.repAlpha(data, 6500, (short)17, b); // accDebited
      generalUtils.dfs(tmp, (short)21, b); generalUtils.repAlpha(data, 6500, (short)18, b); // signon
      generalUtils.dfs(tmp, (short)23, b); generalUtils.repAlpha(data, 6500, (short)19, b); // projectCode
      generalUtils.dfs(tmp, (short)25, b); generalUtils.repAlpha(data, 6500, (short)21, b); // chequeNumber
      generalUtils.dfs(tmp, (short)26, b); generalUtils.repAlpha(data, 6500, (short)22, b); // printCount
      generalUtils.dfs(tmp, (short)27, b); generalUtils.repAlpha(data, 6500, (short)23, b); // ARCode

      generalUtils.dfs(tmp, (short)28, b); generalUtils.repAlpha(data, 6500, (short)24, b); // currency
      currency = generalUtils.dfsAsStr(tmp, (short)28);
      
      generalUtils.dfs(tmp, (short)29, b); generalUtils.repAlpha(data, 6500, (short)25, b); // rate
      // create the exchange rate msg line (if this curr != base curr)
      if(! currency.equalsIgnoreCase(accountsUtils.getBaseCurrency(con, stmt, rs, dnm, localDefnsDir, defnsDir)))
      {
        generalUtils.formatNumeric(b, '2');
        generalUtils.repAlpha(data, 6500, (short)23, b);
      }
      
      generalUtils.dfs(tmp, (short)32, b); 
      generalUtils.doubleDPs(b, b, '2');
      generalUtils.repAlpha(data, 6500, (short)27, b); // basegstcomponent
      
      generalUtils.dfs(tmp, (short)30, b); 
      generalUtils.doubleDPs(b, b, '2');
      generalUtils.repAlpha(data, 6500, (short)26, b); // basetotalAmount

      generalUtils.dfs(tmp, (short)33, b); generalUtils.repAlpha(data, 6500, (short)28, b); // cashOrNot
      generalUtils.dfs(tmp, (short)34, b); generalUtils.repAlpha(data, 6500, (short)29, b); // Reference
      generalUtils.dfs(tmp, (short)36, b); generalUtils.repAlpha(data, 6500, (short)30, b); // bankAccount
      generalUtils.dfs(tmp, (short)37, b); generalUtils.repAlpha(data, 6500, (short)31, b); // charges
      generalUtils.dfs(tmp, (short)38, b); generalUtils.repAlpha(data, 6500, (short)32, b); // discountAllowed
      generalUtils.dfs(tmp, (short)41, b); generalUtils.repAlpha(data, 6500, (short)33, b); // bankAmount
      generalUtils.dfs(tmp, (short)42, b); generalUtils.repAlpha(data, 6500, (short)34, b); // rateBankToBase
      generalUtils.dfs(tmp, (short)43, b); generalUtils.repAlpha(data, 6500, (short)35, b); // exchangeAdjustment

      // get stuff from Customer table
      if(customer.getCompanyRecGivenCode(con, stmt, rs, companyCode, '\000', dnm, data2, localDefnsDir, defnsDir) != -1) // just-in-case
      {
        generalUtils.dfs(data2, (short)1,  b); generalUtils.repAlpha(data, 6500, (short)52, b); // name
        generalUtils.dfs(data2, (short)2,  b); generalUtils.repAlpha(data, 6500, (short)53, b); // addr1
        generalUtils.dfs(data2, (short)3,  b); generalUtils.repAlpha(data, 6500, (short)54, b); // addr2
        generalUtils.dfs(data2, (short)4,  b); generalUtils.repAlpha(data, 6500, (short)55, b); // addr3
        generalUtils.dfs(data2, (short)5,  b); generalUtils.repAlpha(data, 6500, (short)56, b); // addr4
        generalUtils.dfs(data2, (short)6,  b); generalUtils.repAlpha(data, 6500, (short)57, b); // addr5
      }
    }

    reportGenDetails.processControl(dnm, unm, localDefnsDir, defnsDir, dp, negStyle, zeroStyle, tm, bm, lm, rm, pageSizeWidth, pageSizeLength, currDown,
                         currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName, userFontStyle, userFontItalic, userFontSize,
                         fhPPR[0], fhO[0], fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord, across2Coord, oBuf, null);
    reportGenDetails.processSection("RH", null, null, (short)0, dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0], pageSizeWidth[0],
                         pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName, userFontStyle,
                         userFontItalic, userFontSize, fhPPR[0], fhO[0], fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord, across2Coord,
                         oBuf, null);
    reportGenDetails.processSection("PH", data, fldNames, (short)64, dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0], pageSizeWidth[0],
                         pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName, userFontStyle,
                         userFontItalic, userFontSize, fhPPR[0], fhO[0], fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord, across2Coord,
                         oBuf, null);

    int lineCount, numPages=1;

    lineCount=1;
    int docSizeMax = miscDefinitions.docSizeMax(con, stmt, rs, "receipt");
    byte[] invoiceCode = new byte[21];

    for(int xx=0;xx<linesCount[0];++xx)
    {
      if(generalUtils.getListEntryByNum(xx, linesData, tmp)) // just-in-case
      {
        generalUtils.replaceTwosWithOnes(tmp);

        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)2, invoiceCode); // invoiceCode
        generalUtils.repAlpha(data, 6500, (short)36, invoiceCode);
        
        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)3, b); // invoiceDate
        generalUtils.repAlpha(data, 6500, (short)37, generalUtils.convertFromYYYYMMDD(b));

        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)4, b); // invoiceAmount
        generalUtils.bytesDPsGivenSeparator(true, '\000', '2', b, 20, 0);
        generalUtils.repAlpha(data, 6500, (short)38, b);
        
        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)5, b); // desc
        generalUtils.repAlpha(data, 6500, (short)39, b);
        
        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)6, b); // amountReceived
        generalUtils.bytesDPsGivenSeparator(true, '\000', '2', b, 20, 0);
        generalUtils.repAlpha(data, 6500, (short)40, b);
        
        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)8, b); // signOn
        generalUtils.repAlpha(data, 6500, (short)41, b);

        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)11, b); // originalRate
        generalUtils.bytesDPsGivenSeparator(true, '\000', '2', b, 20, 0);
        generalUtils.repAlpha(data, 6500, (short)42, b);

        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)13, b); // gstComponent
        generalUtils.bytesDPsGivenSeparator(true, '\000', '2', b, 20, 0);
        generalUtils.repAlpha(data, 6500, (short)43, b);

        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)14, b); // accountCr
        generalUtils.repAlpha(data, 6500, (short)44, b); 

        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)15, b); // invoiceLine
        generalUtils.repAlpha(data, 6500, (short)45, b);

        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)16, b); // line
        generalUtils.repAlpha(data, 6500, (short)46, b);

        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)17, b); // entry
        generalUtils.repAlpha(data, 6500, (short)47, b);

        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)18, b); // baseAmountPaid
        generalUtils.bytesDPsGivenSeparator(true, '\000', '2', b, 20, 0);
        generalUtils.repAlpha(data, 6500, (short)48, b);
        
        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)19, b); // baseGSTComponent
        generalUtils.bytesDPsGivenSeparator(true, '\000', '2', b, 20, 0);
        generalUtils.repAlpha(data, 6500, (short)49, b);

        // get stuff from invoice table
        if(salesInvoice.getRecGivenCode(con, stmt, rs, invoiceCode, '\000', data2, dnm, localDefnsDir, defnsDir, bytesOut) != -1) // just-in-case
        {
          generalUtils.dfs(data2, (short)1,  b); generalUtils.repAlpha(data, 6500, (short)62, generalUtils.convertFromYYYYMMDD(b)); // date
          generalUtils.dfs(data2, (short)46, b); generalUtils.repAlpha(data, 6500, (short)63, b); // CustomerInvoiceCode
        }
        
        // need to check DNs ???

        reportGenDetails.processSection("BL1", data, fldNames, (short)64, dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0],
                             pageSizeWidth[0], pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF,
                             userFontName, userFontStyle, userFontItalic, userFontSize, fhPPR[0], fhO[0], fontDefType, fontDefStyle,
                             fontDefItalic, fontDefSize, down2Coord, across2Coord, oBuf, null);
        ++lineCount;
        if(lineCount >= docSizeMax)
        {
          b[0] = '\000';
          reportGenDetails.outputLine('E', b, data, fldNames, (short)64, ' ', dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0],
                           pageSizeWidth[0], pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF,
                           userFontName, userFontStyle, userFontItalic, userFontSize, fhPPR[0], fhO[0], fontDefType, fontDefStyle, fontDefItalic,
                           fontDefSize, down2Coord, across2Coord, oBuf, null);
          ++numPages;
          lineCount=1;
        }
      }
    }

    receipt.getRecGivenCode(con, stmt, rs, code, '\000', tmp, dnm, localDefnsDir, defnsDir, bytesOut);
      
    generalUtils.dfs(tmp, (short)11, b);
    int x=0,  y=0;
    while(b[x] != '\001' && b[x] != '\000')
      b2[y++] = b[x++];
    b2[y] = '\000';
    generalUtils.repAlpha(data, 6500, (short)50, b2);

    reportGenDetails.processSection("BL2", data, fldNames, (short)64, dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0],
                         pageSizeWidth[0], pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF,
                         userFontName, userFontStyle, userFontItalic, userFontSize, fhPPR[0], fhO[0], fontDefType, fontDefStyle,
                         fontDefItalic, fontDefSize, down2Coord, across2Coord, oBuf, null);

    // any other note lines
    if(b[x] == '\001')
      ++x; // lf
    while(b[x] != '\000')
    {
      y=0;
      while(b[x] != '\001' && b[x] != '\000')
         b2[y++] = b[x++];
      b2[y] = '\000';
      generalUtils.repAlpha(data, 6500, (short)51, b2);

      reportGenDetails.processSection("BL3", data, fldNames, (short)64, dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0],
                           pageSizeWidth[0], pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF,
                           userFontName, userFontStyle, userFontItalic, userFontSize, fhPPR[0], fhO[0], fontDefType, fontDefStyle,
                           fontDefItalic, fontDefSize, down2Coord, across2Coord, oBuf, null);

      if(b[x] == '\001')
       ++x; // lf
    }

    reportGenDetails.processSection("PF", data, fldNames, (short)64, dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0], pageSizeWidth[0],
                         pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName, userFontStyle,
                         userFontItalic, userFontSize, fhPPR[0], fhO[0], fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord, across2Coord,
                         oBuf, null);

    reportGenDetails.processSection("RF", data, fldNames, (short)64, dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0], pageSizeWidth[0],
                         pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName, userFontStyle,
                         userFontItalic, userFontSize, fhPPR[0], fhO[0], fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord, across2Coord,
                         oBuf, null);

    fhO[0].write(oBuf, 0, oPtr[0]);

    generalUtils.fileClose(fhO[0]);
    generalUtils.fileClose(fhPPR[0]);

    PrintingLayout printingLayout = new PrintingLayout();
    printingLayout.updateNumPages("0.000", reportsDir);

    return numPages;
  }

}
