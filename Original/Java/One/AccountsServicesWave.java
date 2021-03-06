// =======================================================================================================================================================================================================
// System: ZaraStar: Accounts: Services access page
// Module: AccountsServicesWave.java
// Author: C.K.Harvey
// Copyright (c) 2002-08 Christopher Harvey. All Rights Reserved.
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

public class AccountsServicesWave extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  AdminUtils adminUtils = new AdminUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();

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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "AccountsServices", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 109, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush(); 
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String year, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');
    String workingDir    = directoryUtils.getUserDir('W', dnm, unm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 109, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreenW(out, req, 13, unm, sid, uty, men, den, dnm, bnm, "109", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 109, bytesOut[0], 0, "ACC:" + year);
      if(con  != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreenW(out, req, 12, unm, sid, uty, men, den, dnm, bnm, "109", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 109, bytesOut[0], 0, "SID:" + year);
      if(con  != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    // updateCurrYearOnFile
    if(! generalUtils.fileExists(workingDir + "accounts.dfn"))
      generalUtils.createDefnFile("accounts.dfn", workingDir);

    generalUtils.repInDefnFile(unm, year, "accounts.dfn", workingDir, "");

    scoutln(out, bytesOut, "109\001\001Accounts Services\001javascript:getHTML('AccountsServicesWave','')\001\001\001\001\003");

    set(con, stmt, rs, out, req, year, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 109, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), year);
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String year, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir,
                   String defnsDir, int[] bytesOut) throws Exception
  {

    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\"></head>");


    scoutln(out, bytesOut, "<form>");

    adminUtils.drawTitleW(out, "Accounts Services", "109", bytesOut);

    scoutln(out, bytesOut, "<table id='page' width=100% border=0>");

    boolean ReceiptServices  = authenticationUtils.verifyAccess(con, stmt, rs, req, 157, true, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean ReceiptVoucherServices  = authenticationUtils.verifyAccess(con, stmt, rs, req, 159, true, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean AccountsReceivableBuilder = authenticationUtils.verifyAccess(con, stmt, rs, req, 4203, true, unm, uty, dnm, localDefnsDir, defnsDir);

    if(AccountsReceivableBuilder || ReceiptServices || ReceiptVoucherServices)
    {
      scoutln(out, bytesOut, "<tr><td nowrap><h1>AR Services</td></tr>");

      if(AccountsReceivableBuilder)
      {
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"javascript:getHTML('AccountsgeceivableServicesw','')\">AR Builder</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(4203) + "</td></tr>");
      }

      if(ReceiptServices)
      {
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"javascript:getHTML('ReceiptServicesw','')\">Receipt Focus</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(157) + "</td></tr>");
      }

      if(ReceiptVoucherServices)
      {
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"javascript:getHTML('ReceiptVoucherServicesw','')\">Receipt Voucher Focus</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(159) + "</td></tr>");
      }
    }

    boolean PaymentVoucherServices  = authenticationUtils.verifyAccess(con, stmt, rs, req, 160, true, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean PaymentServices  = authenticationUtils.verifyAccess(con, stmt, rs, req, 164, true, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean AccountsPayableBuilderPage = authenticationUtils.verifyAccess(con, stmt, rs, req, 5047, true, unm, uty, dnm, localDefnsDir, defnsDir);

    if(AccountsPayableBuilderPage || PaymentVoucherServices || PaymentServices)
    {
      scoutln(out, bytesOut, "<tr><td nowrap><h1>AP Services</td></tr>");

      if(AccountsPayableBuilderPage)
      {
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"javascript:getHTML('AccountsPayableServicesw','')\">AP Builder</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(5047) + "</td></tr>");
      }

      if(PaymentServices)
      {
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"javascript:getHTML('PaymentServicesw','')\">Payment Focus</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(164) + "</td></tr>");
      }

      if(PaymentVoucherServices)
      {
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"javascript:getHTML('PaymentVoucherServicesw','')\">Payment Voucher Focus</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(160) + "</td></tr>");
      }
    }

    boolean BankReconciliationInput = authenticationUtils.verifyAccess(con, stmt, rs, req, 6002, true, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean GSTReconciliation = authenticationUtils.verifyAccess(con, stmt, rs, req, 6003, true, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean AdminGSTRateDefinition = authenticationUtils.verifyAccess(con, stmt, rs, req, 7061, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean AdminBankAccountDefinition = authenticationUtils.verifyAccess(con, stmt, rs, req, 7067, unm, uty, dnm, localDefnsDir, defnsDir);

    if(BankReconciliationInput || GSTReconciliation || AdminGSTRateDefinition || AdminBankAccountDefinition)
    {
      scoutln(out, bytesOut, "<tr><td nowrap><h1>Reconciliation Services</td></tr>");

      if(BankReconciliationInput)
      {
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"javascript:getHTML('BankReconciliationInputw','')\">Bank Reconciliation</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(6002) + "</td></tr>");
      }

      if(AdminBankAccountDefinition)
      {
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"javascript:getHTML('AdminBankAccountDefinitionw','')\">Bank Accounts</a></td><td nowrap width=90% nowrap>"
                              + directoryUtils.buildHelp(7067) + "</td></tr>");
      }

      if(GSTReconciliation)
      {
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"javascript:getHTML('GSTReconciliationw','')\">GST Reconciliation</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(6003) + "</td></tr>");
      }

      if(AdminGSTRateDefinition)
      {
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"javascript:getHTML('AdminGSTRateDefinitionWave','')\">GST Rates</a></td><td nowrap width=90% nowrap>"
                              + directoryUtils.buildHelp(7061) + "</td></tr>");
      }
    }

    boolean AdminCurrencyRateDefinition = authenticationUtils.verifyAccess(con, stmt, rs, req, 7055, true, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean AdminCurrencyDefinition = authenticationUtils.verifyAccess(con, stmt, rs, req, 7054, true, unm, uty, dnm, localDefnsDir, defnsDir);

    if(AdminCurrencyRateDefinition || AdminCurrencyDefinition)
    {
      scoutln(out, bytesOut, "<tr><td nowrap><h1>Currency Services</td></tr>");

      if(AdminCurrencyRateDefinition)
      {
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"javascript:getHTML('AdminCurrencyRateDefinitionw','')\">Currency Rates</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(7055) + "</td></tr>");
      }

      if(AdminCurrencyDefinition)
      {
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"javascript:getHTML('AdminCurrencyDefinitionWave','')\">Currency Definitions</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(7054) + "</td></tr>");
      }
    }

    boolean _6015 = authenticationUtils.verifyAccess(con, stmt, rs, req, 6015, true, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean AccountsCoADefinition = authenticationUtils.verifyAccess(con, stmt, rs, req, 6016, true, unm, uty, dnm, localDefnsDir, defnsDir);

    if(_6015 || AccountsCoADefinition)
    {
      scoutln(out, bytesOut, "<tr><td nowrap><h1>Chart of Accounts Services</td></tr>");

      if(_6015)
      {
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"javascript:getHTML('_6015w','')\">Generate Chart of Accounts Listing</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(6015) + "</td></tr>");
      }

      if(AccountsCoADefinition)
      {
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"javascript:getHTML('AccountsCoADefinitionw','')\">Modify Chart of Accounts</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(6016) + "</td></tr>");
      }
    }

    boolean AccountsBatchDefinitions = authenticationUtils.verifyAccess(con, stmt, rs, req, 6022, true, unm, uty, dnm, localDefnsDir, defnsDir);

    if(AccountsBatchDefinitions)
    {
      scoutln(out, bytesOut, "<tr><td nowrap><h1>Journal Services</td></tr>");

      if(AccountsBatchDefinitions)
      {
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"javascript:getHTML('AccountsBatchDefinitionsw','')\">Modify Journal Batch Adjustments</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(6022) + "</td></tr>");
      }
    }

    boolean AccountsStockLedgerPage = authenticationUtils.verifyAccess(con, stmt, rs, req, 6005, true, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean AccountsViewGL = authenticationUtils.verifyAccess(con, stmt, rs, req, 6028, true, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean AccountsGLDebtorsIndividual = authenticationUtils.verifyAccess(con, stmt, rs, req, 6029, true, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean AccountsGLCreditorsIndividual = authenticationUtils.verifyAccess(con, stmt, rs, req, 6030, true, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean AnalyticsGLListingReport = authenticationUtils.verifyAccess(con, stmt, rs, req, 6031, true, unm, uty, dnm, localDefnsDir, defnsDir);

    if(AccountsViewGL || AccountsGLDebtorsIndividual || AccountsGLCreditorsIndividual || AnalyticsGLListingReport)
    {
      scoutln(out, bytesOut, "<tr><td nowrap><h1>Ledger Services</td></tr>");

      if(AccountsViewGL)
      {
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"javascript:getHTML('AccountsViewGLw','')\">View the General Ledger</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(6028) + "</td></tr>");
      }

      if(AccountsGLDebtorsIndividual)
      {
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"javascript:getHTML('AccountsGLDebtorsIndividualw','')\">View the Debtors Ledger</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(6029) + "</td></tr>");
      }

      if(AccountsGLCreditorsIndividual)
      {
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"javascript:getHTML('AccountsGLCreditorsIndividualw','')\">View the Creditors Ledger</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(6030) + "</td></tr>");
      }

      if(AccountsStockLedgerPage)
      {
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"javascript:getHTML('AccountsStockLedgerPagew','&p1=" + year + "')\">View the Stock Ledger</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(6005) + "</td></tr>");
      }

      if(AnalyticsGLListingReport)
      {
        scoutln(out, bytesOut, "<tr><td>&nbsp;</td><td nowrap><p><a href=\"javascript:getHTML('AnalyticsGLListingReportInput','')\">Print the General Ledger</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(6031) + "</td></tr>");
      }

    }

    boolean AccountsVerification = authenticationUtils.verifyAccess(con, stmt, rs, req, 6032, true, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean StockLevelValuesItem = authenticationUtils.verifyAccess(con, stmt, rs, req, 3069, true, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean AccountsYearEndClosing = authenticationUtils.verifyAccess(con, stmt, rs, req, 6020, true, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean SelfAuditValidationServices = authenticationUtils.verifyAccess(con, stmt, rs, req, 108, true, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean AdminDocumentLocks = authenticationUtils.verifyAccess(con, stmt, rs, req, 7033, true, unm, uty, dnm, localDefnsDir, defnsDir);

    if(AccountsVerification || SelfAuditValidationServices)
    {
      scoutln(out, bytesOut, "<tr><td nowrap><h1>Procedural Services</td></tr>");

      if(AccountsVerification)
      {
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"javascript:getHTML('AccountsVerificationw','&p1=" + year + "')\">Verification Procedures</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(6032) + "</td></tr>");
      }

      if(SelfAuditValidationServices)
      {
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"javascript:getHTML('SelfAuditValidationServicesw','&p1=" + year + "')\">Self-Audit Validation</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(108) + "</td></tr>");
      }

      if(AccountsYearEndClosing)
      {
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"javascript:getHTML('AccountsYearEndClosingw','')\">Year Closing and Starting</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(6020) + "</td></tr>");
      }

      if(AdminDocumentLocks)
      {
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"javascirpt:getHTML('AdminDocumentLocksw','')\">Document Locking</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(7033) + "</td></tr>");
      }

      if(StockLevelValuesItem)
      {
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"javascript:getHTML('StockLevelsAndValuesw','')\">Stock Opening Positions & Valuation</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(3069) + "</td></tr>");
      }
    }

    boolean _6053 = authenticationUtils.verifyAccess(con, stmt, rs, req, 6053, true, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean _6054 = authenticationUtils.verifyAccess(con, stmt, rs, req, 6054, true, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean _6055 = authenticationUtils.verifyAccess(con, stmt, rs, req, 6055, true, unm, uty, dnm, localDefnsDir, defnsDir);

    if(_6053 || _6054 || _6055)
    {
      scoutln(out, bytesOut, "<tr><td nowrap><h1>Statutory Reporting Services</td></tr>");

      if(_6053)
      {
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"javascript:getHTML('AnalyticsTrialBalanceInputw','&p1=G')\">Generate Trial Balance</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(6053) + "</td></tr>");

        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"javascript:getHTML('AnalyticsTrialBalanceInputw','&p1=S')\">Generate Trial Balance (with Debtors and Creditors Detail)</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(6053) + "</td></tr>");
      }

      if(_6054)
      {
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"javascript:getHTML('AnalyticsBSReportInputw','')\">Generate Balance Sheet</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(6054) + "</td></tr>");
      }

      if(_6055)
      {
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"javascript:getHTML('AnalyticsPLCreateReportInputw','')\">Generate Profit and Loss Statement</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(6055) + "</td></tr>");
      }
    }

    boolean InterAccountTransferServices  = authenticationUtils.verifyAccess(con, stmt, rs, req, 161, true, unm, uty, dnm, localDefnsDir, defnsDir);

    if(InterAccountTransferServices)
    {
      scoutln(out, bytesOut, "<tr><td nowrap><h1>Document Services</td></tr>");

      if(InterAccountTransferServices)
      {
        scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"javascript:getHTML('InterAccountTransferServicesw','')\">InterAccount Transfer Focus</a></td><td width=70% nowrap>" + directoryUtils.buildHelp(161) + "</td></tr>");
      }
    }

    scoutln(out, bytesOut, "</table></form>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {
    out.println(str);
    bytesOut[0] += (str.length() + 2);
  }

}
