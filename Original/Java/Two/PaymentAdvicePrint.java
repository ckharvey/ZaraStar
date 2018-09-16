// =======================================================================================================================================================================================================
// System: ZaraStar: Document: Payment (Advice) Print
// Module: PaymentAdvicePrint.java
// Author: C.K.Harvey
// Copyright (c) 1999-2007 Christopher Harvey. All Rights Reserved.
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

public class PaymentAdvicePrint extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  ReportGeneration reportGeneration = new ReportGeneration();
  PaymentVoucher paymentVoucher = new PaymentVoucher();
  MiscDefinitions miscDefinitions = new MiscDefinitions();
  AccountsUtils accountsUtils = new AccountsUtils();
  
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "PaymentAdvicePrint", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6073, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush();
    }
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String reportsDir    = directoryUtils.getUserDir('R', dnm, unm);
    String workingDir    = directoryUtils.getUserDir('W', dnm, dnm + "/" + unm);
    String imagesDir     = directoryUtils.getSupportDirs('I');

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;
    
    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 6073, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "6073", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6073, bytesOut[0], 0, "ACC:" + p1);
      if(con  != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "6073", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6073, bytesOut[0], 0, "SID:" + p1);
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
                messagePage.msgScreen(false, out, req, 17, unm, sid, uty, men, den, dnm, bnm, "6073", imagesDir, localDefnsDir, defnsDir, bytesOut);
                break;
      case -2 : // cannot create report output file
                messagePage.msgScreen(false, out, req, 18, unm, sid, uty, men, den, dnm, bnm, "6073", imagesDir, localDefnsDir, defnsDir, bytesOut);
                break;
      default : // generated ok
                out.println(generalUtils.intToStr(numPages));
                break;
    }

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 6073, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
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

    // fetch lines data in one go
    byte[] linesData  = new byte[2000];
    int[]  listLen    = new int[1];  listLen[0] = 2000;
    int[]  linesCount = new int[1];
    linesData = paymentVoucher.getLines(con, stmt, rs, code, '\001', linesData, listLen, linesCount, dnm, localDefnsDir, defnsDir);
    
    byte[] multipleLinesData  = new byte[2000];
    int[]  multipleListLen    = new int[1];  multipleListLen[0] = 2000;
    int[]  multipleLinesCount = new int[1];

    if(linesCount[0] > 0) // get all the multiple lines for this document
    {
      multipleLinesData = paymentVoucher.getMultipleLines(con, stmt, rs, code, multipleLinesData, multipleListLen, multipleLinesCount, dnm, localDefnsDir, defnsDir);
    }

    if((reportGeneration.fhPPR = generalUtils.fileOpenD("117.ppr", localDefnsDir)) == null)
    {
      if((reportGeneration.fhPPR = generalUtils.fileOpenD("117.ppr", defnsDir)) == null)
      {
        generalUtils.fileClose(reportGeneration.fhO);
        return -1;
      }
    }

    reportGeneration.lastOperationPF = false;

    byte[] fldNames = new byte[6500];
    byte[] data     = new byte[6500];
    
    generalUtils.repAlpha(fldNames, 6500, (short)0,  "Voucher.VoucherCode");
    generalUtils.repAlpha(fldNames, 6500, (short)1,  "Voucher.Date");
    generalUtils.repAlpha(fldNames, 6500, (short)2,  "Voucher.CompanyCode");
    generalUtils.repAlpha(fldNames, 6500, (short)3,  "Voucher.CompanyName");
    generalUtils.repAlpha(fldNames, 6500, (short)4,  "Voucher.Address1");
    generalUtils.repAlpha(fldNames, 6500, (short)5,  "Voucher.Address2");
    generalUtils.repAlpha(fldNames, 6500, (short)6,  "Voucher.Address3");
    generalUtils.repAlpha(fldNames, 6500, (short)7,  "Voucher.Address4");
    generalUtils.repAlpha(fldNames, 6500, (short)8,  "Voucher.Address5");
    generalUtils.repAlpha(fldNames, 6500, (short)9,  "Voucher.PostCode");
    generalUtils.repAlpha(fldNames, 6500, (short)10, "Voucher.FAO");
    generalUtils.repAlpha(fldNames, 6500, (short)11, "Voucher.Attention");
    generalUtils.repAlpha(fldNames, 6500, (short)12, "Voucher.Misc1");
    generalUtils.repAlpha(fldNames, 6500, (short)13, "Voucher.Misc2");
    generalUtils.repAlpha(fldNames, 6500, (short)14, "Voucher.GSTTotal");
    generalUtils.repAlpha(fldNames, 6500, (short)15, "Voucher.TotalTotal");
    generalUtils.repAlpha(fldNames, 6500, (short)16, "Voucher.AccountCr");
    generalUtils.repAlpha(fldNames, 6500, (short)17, "Voucher.ChequeNumber");
    generalUtils.repAlpha(fldNames, 6500, (short)18, "Voucher.Reference");
    generalUtils.repAlpha(fldNames, 6500, (short)19, "Voucher.ProjectCode");
    generalUtils.repAlpha(fldNames, 6500, (short)20, "Voucher.Processed");
    generalUtils.repAlpha(fldNames, 6500, (short)21, "Voucher.DateProcessed");
    generalUtils.repAlpha(fldNames, 6500, (short)22, "Voucher.SignOnProcessed");
    generalUtils.repAlpha(fldNames, 6500, (short)23, "Voucher.Rate");
    generalUtils.repAlpha(fldNames, 6500, (short)24, "Voucher.Currency");
    generalUtils.repAlpha(fldNames, 6500, (short)25, "Voucher.BaseTotalTotal");
    generalUtils.repAlpha(fldNames, 6500, (short)26, "Voucher.BaseGSTTotal");
    generalUtils.repAlpha(fldNames, 6500, (short)27, "Voucher.SignOn");
    generalUtils.repAlpha(fldNames, 6500, (short)28, "Voucher.Type");
    generalUtils.repAlpha(fldNames, 6500, (short)29, "Voucher.Status");
    generalUtils.repAlpha(fldNames, 6500, (short)30, "Voucher.BankAccount");
    generalUtils.repAlpha(fldNames, 6500, (short)31, "Voucher.CashOrNot");
    generalUtils.repAlpha(fldNames, 6500, (short)32, "Voucher.Reconciled");
    generalUtils.repAlpha(fldNames, 6500, (short)33, "VoucherL.ItemCode");
    generalUtils.repAlpha(fldNames, 6500, (short)34, "VoucherL.Description");
    generalUtils.repAlpha(fldNames, 6500, (short)35, "VoucherL.Amount");
    generalUtils.repAlpha(fldNames, 6500, (short)36, "VoucherL.GSTRate");
    generalUtils.repAlpha(fldNames, 6500, (short)37, "VoucherL.AccountDr");
    generalUtils.repAlpha(fldNames, 6500, (short)38, "VoucherL.Amount2");
    generalUtils.repAlpha(fldNames, 6500, (short)39, "VoucherL.Line");
    generalUtils.repAlpha(fldNames, 6500, (short)40, "VoucherL.Entry");
    generalUtils.repAlpha(fldNames, 6500, (short)41, "VoucherL.SignOn");
    generalUtils.repAlpha(fldNames, 6500, (short)42, "Voucher.NoteLine1");
    generalUtils.repAlpha(fldNames, 6500, (short)43, "Voucher.NoteLines");
    
    String currency="";
    byte[] companyCode = new byte[21];
    byte[] data2 = new byte[5000];
    byte[] lineNumB     = new byte[20];

    if(paymentVoucher.getRecGivenCode(con, stmt, rs, code, '\000', tmp, dnm, localDefnsDir, defnsDir, bytesOut) != -1L) // just-in-case
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
      generalUtils.dfs(tmp, (short)13, b); generalUtils.repAlpha(data, 6500, (short)12, b); // misc1
      generalUtils.dfs(tmp, (short)14, b); generalUtils.repAlpha(data, 6500, (short)13, b); // misc2

      generalUtils.dfs(tmp, (short)15, b); 
      generalUtils.doubleDPs(b, b, '2');
      generalUtils.repAlpha(data, 6500, (short)14, b); // gstTotal
      
      generalUtils.dfs(tmp, (short)16, b); 
      generalUtils.doubleDPs(b, b, '2');
      generalUtils.repAlpha(data, 6500, (short)15, b); // totalTotal

      generalUtils.dfs(tmp, (short)17, b); generalUtils.repAlpha(data, 6500, (short)16, b); // AccountCr
      generalUtils.dfs(tmp, (short)18, b); generalUtils.repAlpha(data, 6500, (short)17, b); // chequeNumber
      generalUtils.dfs(tmp, (short)21, b); generalUtils.repAlpha(data, 6500, (short)18, b); // reference
      generalUtils.dfs(tmp, (short)22, b); generalUtils.repAlpha(data, 6500, (short)19, b); // projectCode
      generalUtils.dfs(tmp, (short)23, b); generalUtils.repAlpha(data, 6500, (short)20, b); // processed
      generalUtils.dfs(tmp, (short)24, b); generalUtils.repAlpha(data, 6500, (short)21, generalUtils.convertFromYYYYMMDD(b)); // dateProcessed
      generalUtils.dfs(tmp, (short)25, b); generalUtils.repAlpha(data, 6500, (short)22, b); // signOnProcessed

      generalUtils.dfs(tmp, (short)27, b); generalUtils.repAlpha(data, 6500, (short)24, b); // currency
      currency = generalUtils.dfsAsStr(tmp, (short)27);
      
      generalUtils.dfs(tmp, (short)26, b); generalUtils.repAlpha(data, 6500, (short)23, b); // rate
      // create the exchange rate msg line (if this curr != base curr)
      if(! currency.equalsIgnoreCase(accountsUtils.getBaseCurrency(con, stmt, rs, dnm, localDefnsDir, defnsDir)))
      {
        generalUtils.formatNumeric(b, '2');
        generalUtils.repAlpha(data, 6500, (short)23, b);
      }
      
      generalUtils.dfs(tmp, (short)28, b); 
      generalUtils.doubleDPs(b, b, '2');
      generalUtils.repAlpha(data, 6500, (short)25, b); // basetotaltotal
      
      generalUtils.dfs(tmp, (short)29, b); 
      generalUtils.doubleDPs(b, b, '2');
      generalUtils.repAlpha(data, 6500, (short)26, b); // basegsttotal

      generalUtils.dfs(tmp, (short)31, b); generalUtils.repAlpha(data, 6500, (short)27, b); // signOn
      generalUtils.dfs(tmp, (short)32, b); generalUtils.repAlpha(data, 6500, (short)28, b); // type
      generalUtils.dfs(tmp, (short)33, b); generalUtils.repAlpha(data, 6500, (short)29, b); // status
      generalUtils.dfs(tmp, (short)34, b); generalUtils.repAlpha(data, 6500, (short)30, b); // bankAccount
      generalUtils.dfs(tmp, (short)35, b); generalUtils.repAlpha(data, 6500, (short)31, b); // cashOrNot
      generalUtils.dfs(tmp, (short)36, b); generalUtils.repAlpha(data, 6500, (short)32, b); // reconciled
    }

    reportGeneration.processControl(dnm, unm, localDefnsDir, defnsDir);
    reportGeneration.processSection("RH", null, null, (short)0, dnm, unm, localDefnsDir, defnsDir);
    reportGeneration.processSection("PH", data, fldNames, (short)44, dnm, unm, localDefnsDir, defnsDir);

    int lineCount=1, numPages=1, llCount=0;

    int docSizeMax = miscDefinitions.docSizeMax(con, stmt, rs, "voucher");

    for(int xx=0;xx<linesCount[0];++xx)
    {
      if(generalUtils.getListEntryByNum(xx, linesData, tmp)) // just-in-case
      {
        generalUtils.replaceTwosWithOnes(tmp);

        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)2, b); // itemCode
        generalUtils.repAlpha(data, 6500, (short)33, b);
        
        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)3, b); // desc
        generalUtils.repAlpha(data, 6500, (short)34, b);

        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)4, b); // amount
        generalUtils.bytesDPsGivenSeparator(true, '\000', '2', b, 20, 0);
        generalUtils.repAlpha(data, 6500, (short)35, b);
        
        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)5, b); // gstRate
        generalUtils.repAlpha(data, 6500, (short)36, b);
        
        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)6, b); // accountDr
        generalUtils.repAlpha(data, 6500, (short)37, b);
        
        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)8, b); // amount2
        generalUtils.bytesDPsGivenSeparator(true, '\000', '2', b, 20, 0);
        generalUtils.repAlpha(data, 6500, (short)38, b);

        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)9, lineNumB);
        generalUtils.repAlpha(data, 6500, (short)39, lineNumB);

        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)10, b); // entry
        generalUtils.repAlpha(data, 6500, (short)40, b);

        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)12, b); // signOn
        generalUtils.repAlpha(data, 6500, (short)41, b); 

        reportGeneration.processSection("BL1", data, fldNames, (short)44, dnm, unm, localDefnsDir, defnsDir);
        ++lineCount;
        if(lineCount >= docSizeMax)
        {
          b[0] = '\000';
          reportGeneration.outputLine('E', b, data, fldNames, (short)44, ' ', dnm, unm, localDefnsDir, defnsDir);
          ++numPages;
          lineCount=1;
        }
        
        // output any LL lines
        b[0] = '\000';
        generalUtils.repAlpha(data, 6500, (short)33, b); // itemCode
        generalUtils.repAlpha(data, 6500, (short)34, b); // desc
        generalUtils.repAlpha(data, 6500, (short)35, b); // amount
        generalUtils.repAlpha(data, 6500, (short)36, b); // gstRate
        generalUtils.repAlpha(data, 6500, (short)37, b); // accountDr
        generalUtils.repAlpha(data, 6500, (short)38, b); // amount2
        generalUtils.repAlpha(data, 6500, (short)39, b); // line
        generalUtils.repAlpha(data, 6500, (short)40, b); // entry
        generalUtils.repAlpha(data, 6500, (short)41, b); // signOn

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
            generalUtils.repAlpha(data, 6500, (short)34, b); // desc
            reportGeneration.processSection("BL1", data, fldNames, (short)44, dnm, unm, localDefnsDir, defnsDir);
            ++lineCount;
            if(lineCount >= docSizeMax)
            {
              b[0] = '\000';
              reportGeneration.outputLine('E', b, data, fldNames, (short)44, ' ', dnm, unm, localDefnsDir, defnsDir);
              ++numPages;
              lineCount = 1;
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
      }
    }

    paymentVoucher.getRecGivenCode(con, stmt, rs, code, '\000', tmp, dnm, localDefnsDir, defnsDir, bytesOut);
      
    generalUtils.dfs(tmp, (short)11, b);
    int x=0,  y=0;
    while(b[x] != '\001' && b[x] != '\000')
      b2[y++] = b[x++];
    b2[y] = '\000';
    generalUtils.repAlpha(data, 6500, (short)42, b2);

    reportGeneration.processSection("BL2", data, fldNames, (short)44, dnm, unm, localDefnsDir, defnsDir);

    // any other note lines
    if(b[x] == '\001')
      ++x; // lf
    while(b[x] != '\000')
    {
      y=0;
      while(b[x] != '\001' && b[x] != '\000')
         b2[y++] = b[x++];
      b2[y] = '\000';
      generalUtils.repAlpha(data, 6500, (short)43, b2);

      reportGeneration.processSection("BL3", data, fldNames, (short)44, dnm, unm, localDefnsDir, defnsDir);

      if(b[x] == '\001')
       ++x; // lf
    }

    reportGeneration.processSection("PF", data, fldNames, (short)44, dnm, unm, localDefnsDir, defnsDir);

    reportGeneration.fhO.write(reportGeneration.oBuf, 0, reportGeneration.oPtr);

    generalUtils.fileClose(reportGeneration.fhO);
    generalUtils.fileClose(reportGeneration.fhPPR);

    PrintingLayout printingLayout = new PrintingLayout();
    printingLayout.updateNumPages("0.000", reportsDir);

    return numPages;
  }

}
