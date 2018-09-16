// =======================================================================================================================================================================================================
// System: ZaraStar Document: LPs: Print
// Module: LocalPurchasePrint.java
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

public class LocalPurchasePrint extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  ReportGenDetails reportGenDetails = new ReportGenDetails();
  LocalPurchase localPurchase = new LocalPurchase();
  MiscDefinitions miscDefinitions = new MiscDefinitions();
  Supplier supplier = new Supplier();
  Profile profile = new Profile();

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
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "LocalPurchasePrint", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 5023, bytesOut[0], 0, "ERR:" + p1);
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
    String imagesDir     = directoryUtils.getSupportDirs('I');
    String reportsDir       = directoryUtils.getUserDir('R', dnm, unm);
    String workingDir       = directoryUtils.getUserDir('W', dnm, dnm + "/" + unm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;
    
    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 5023, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "5023", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 5023, bytesOut[0], 0, "ACC:" + p1);
    if(con  != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "5023", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 5023, bytesOut[0], 0, "SID:" + p1);
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
                messagePage.msgScreen(false, out, req, 17, unm, sid, uty, men, den, dnm, bnm, "5023", imagesDir, localDefnsDir, defnsDir,
                                   bytesOut);
                break;
      case -2 : // cannot create report output file
                messagePage.msgScreen(false, out, req, 18, unm, sid, uty, men, den, dnm, bnm, "5023", imagesDir, localDefnsDir, defnsDir,
                                   bytesOut);
                break;
      default : // generated ok
                out.println(generalUtils.intToStr(numPages));
                break;
    }

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 5023, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
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
    int docSizeMax  = miscDefinitions.docSizeMax(con, stmt, rs, "lp");
    int numPages=1;

    // fetch lines data in one go
    byte[] linesData  = new byte[2000];
    int[]  listLen    = new int[1];  listLen[0] = 2000;
    int[]  linesCount = new int[1];
    linesData = localPurchase.getLines(con, stmt, rs, code, '\001', linesData, listLen, linesCount, dnm, localDefnsDir, defnsDir);

    byte[] multipleLinesData  = new byte[2000];
    int[]  multipleListLen    = new int[1];  multipleListLen[0] = 2000;
    int[]  multipleLinesCount = new int[1];
    int llCount=0;
    if(linesCount[0] > 0) // get all the multiple lines for this document
      multipleLinesData = localPurchase.getMultipleLines(con, stmt, rs, code, multipleLinesData, multipleListLen, multipleLinesCount, dnm, localDefnsDir, defnsDir);
    
    String pprFile = "226.ppr";

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

    generalUtils.putAlpha(fldNames, 4000, (short)0,  "LP.LPCode");
    generalUtils.putAlpha(fldNames, 4000, (short)1,  "LP.Date");
    generalUtils.putAlpha(fldNames, 4000, (short)2,  "LP.SupplierCode");
    generalUtils.putAlpha(fldNames, 4000, (short)3,  "LP.SupplierName");
    generalUtils.putAlpha(fldNames, 4000, (short)4,  "LP.Address1");
    generalUtils.putAlpha(fldNames, 4000, (short)5,  "LP.Address2");
    generalUtils.putAlpha(fldNames, 4000, (short)6,  "LP.Address3");
    generalUtils.putAlpha(fldNames, 4000, (short)7,  "LP.Address4");
    generalUtils.putAlpha(fldNames, 4000, (short)8,  "LP.Address5");
    generalUtils.putAlpha(fldNames, 4000, (short)9,  "LP.PostCode");
    generalUtils.putAlpha(fldNames, 4000, (short)10, "LP.FAO");
    generalUtils.putAlpha(fldNames, 4000, (short)11, "LP.Misc1");
    generalUtils.putAlpha(fldNames, 4000, (short)12, "LP.Misc2");
    generalUtils.putAlpha(fldNames, 4000, (short)13, "Supplier.OfficePhone");
    generalUtils.putAlpha(fldNames, 4000, (short)14, "LP.Attention");
    generalUtils.putAlpha(fldNames, 4000, (short)15, "LP.StoreCode");
    generalUtils.putAlpha(fldNames, 4000, (short)16, "LP.ProjectCode");
    generalUtils.putAlpha(fldNames, 4000, (short)17, "Supplier.Fax");
    generalUtils.putAlpha(fldNames, 4000, (short)18, "LP.Rate");
    generalUtils.putAlpha(fldNames, 4000, (short)19, "LP.Currency");
    generalUtils.putAlpha(fldNames, 4000, (short)20, "LP.ShipName");
    generalUtils.putAlpha(fldNames, 4000, (short)21, "LP.ShipAddress1");
    generalUtils.putAlpha(fldNames, 4000, (short)22, "LP.ShipAddress2");
    generalUtils.putAlpha(fldNames, 4000, (short)23, "LP.ShipAddress3");
    generalUtils.putAlpha(fldNames, 4000, (short)24, "LP.ShipAddress4");
    generalUtils.putAlpha(fldNames, 4000, (short)25, "LP.ShipAddress5");
    generalUtils.putAlpha(fldNames, 4000, (short)26, "LP.RevisionOf");
    generalUtils.putAlpha(fldNames, 4000, (short)27, "LP.GSTTotal");
    generalUtils.putAlpha(fldNames, 4000, (short)28, "LP.TotalTotal");
    generalUtils.putAlpha(fldNames, 4000, (short)29, "LP.TotalWithoutGST");
    generalUtils.putAlpha(fldNames, 4000, (short)30, "LP.SignOn");
    generalUtils.putAlpha(fldNames, 4000, (short)31, "LPL.Entry");
    generalUtils.putAlpha(fldNames, 4000, (short)32, "LPL.ItemCode");
    generalUtils.putAlpha(fldNames, 4000, (short)33, "LPL.Description");
    generalUtils.putAlpha(fldNames, 4000, (short)34, "LPL.UnitPrice");
    generalUtils.putAlpha(fldNames, 4000, (short)35, "LPL.Quantity");
    generalUtils.putAlpha(fldNames, 4000, (short)36, "LPL.Amount");
    generalUtils.putAlpha(fldNames, 4000, (short)37, "LPL.GSTRate");
    generalUtils.putAlpha(fldNames, 4000, (short)38, "LPL.UoM");
    generalUtils.putAlpha(fldNames, 4000, (short)39, "LPL.StoreCode");
    generalUtils.putAlpha(fldNames, 4000, (short)40, "LPL.RequiredBy");
    generalUtils.putAlpha(fldNames, 4000, (short)41, "LPL.DeliveryMethod");
    generalUtils.putAlpha(fldNames, 4000, (short)42, "LPL.SOCode");
    generalUtils.putAlpha(fldNames, 4000, (short)43, "LP.NoteLine1");
    generalUtils.putAlpha(fldNames, 4000, (short)44, "LP.NoteLines");
    generalUtils.putAlpha(fldNames, 4000, (short)45, "LPL.SupplierItemCode");
    generalUtils.putAlpha(fldNames, 4000, (short)46, "LP.RemarkLine");
    generalUtils.putAlpha(fldNames, 4000, (short)47, "LP.Revision");
    generalUtils.putAlpha(fldNames, 4000, (short)48, "LP.DeliveryMethod");
    generalUtils.putAlpha(fldNames, 4000, (short)49, "LP.ShipTo");
    generalUtils.putAlpha(fldNames, 4000, (short)50, "LP.InvoiceTo");
    generalUtils.putAlpha(fldNames, 4000, (short)51, "LP.Customer");
    generalUtils.putAlpha(fldNames, 4000, (short)52, "LP.Misc3");
    generalUtils.putAlpha(fldNames, 4000, (short)53, "LP.ShippingTerms");
    generalUtils.putAlpha(fldNames, 4000, (short)54, "LP.GroupDiscountAmount");
    generalUtils.putAlpha(fldNames, 4000, (short)55, "LP.GroupDiscountValue");
    generalUtils.putAlpha(fldNames, 4000, (short)56, "LP.RequiredBy");
    generalUtils.putAlpha(fldNames, 4000, (short)57, "LP.SalesPerson");
    generalUtils.putAlpha(fldNames, 4000, (short)58, "LPL.Discount");
    generalUtils.putAlpha(fldNames, 4000, (short)59, "LPL.Manufacturer");
    generalUtils.putAlpha(fldNames, 4000, (short)60, "LPL.ManufacturerCode");

    double groupDiscount=0.0;
    String remarkType="";

    if(localPurchase.getRecGivenCode(con, stmt, rs, code, '\000', tmp, dnm, localDefnsDir, defnsDir, bytesOut) != -1L) // just-in-case
    {
      byte[] supplierCode = new byte[21];
      
      generalUtils.putAlpha(data, 4000, (short)0, code);
      generalUtils.dfs(tmp, (short)1,  b); generalUtils.putAlpha(data, 4000, (short)1, generalUtils.convertFromYYYYMMDD(b)); // date
      generalUtils.dfs(tmp, (short)2,  supplierCode); generalUtils.putAlpha(data, 4000, (short)2, supplierCode); // companycode
      generalUtils.dfs(tmp, (short)3,  b); generalUtils.putAlpha(data, 4000, (short)3,  b); // companyname
      generalUtils.dfs(tmp, (short)4,  b); generalUtils.putAlpha(data, 4000, (short)4,  b); // address1
      generalUtils.dfs(tmp, (short)5,  b); generalUtils.putAlpha(data, 4000, (short)5,  b); // address2
      generalUtils.dfs(tmp, (short)6,  b); generalUtils.putAlpha(data, 4000, (short)6,  b); // address3
      generalUtils.dfs(tmp, (short)7,  b); generalUtils.putAlpha(data, 4000, (short)7,  b); // address4
      generalUtils.dfs(tmp, (short)8,  b); generalUtils.putAlpha(data, 4000, (short)8,  b); // address5
      generalUtils.dfs(tmp, (short)9,  b); generalUtils.putAlpha(data, 4000, (short)9,  b); // postcode
      generalUtils.dfs(tmp, (short)10, b); generalUtils.putAlpha(data, 4000, (short)10, b); // FAO
      generalUtils.dfs(tmp, (short)14, b); generalUtils.putAlpha(data, 4000, (short)11, b); // misc1
      generalUtils.dfs(tmp, (short)15, b); generalUtils.putAlpha(data, 4000, (short)12, b); // misc2

      String officePhone = supplier.getASupplierFieldGivenCode(con, stmt, rs, "OfficePhone", generalUtils.stringFromBytes(supplierCode, 0L));
      String fax         = supplier.getASupplierFieldGivenCode(con, stmt, rs, "Fax",         generalUtils.stringFromBytes(supplierCode, 0L));
      
      generalUtils.putAlpha(data, 4000, (short)13, officePhone);
      generalUtils.putAlpha(data, 4000, (short)17, fax);
      
      generalUtils.dfs(tmp, (short)12, b); generalUtils.putAlpha(data, 4000, (short)14, b); // attention
      generalUtils.dfs(tmp, (short)29, b); generalUtils.putAlpha(data, 4000, (short)16, b); // projectCode
      
      generalUtils.dfs(tmp, (short)16, b);
      generalUtils.doubleDPs(b, b, '2');
      generalUtils.putAlpha(data, 4000, (short)27, b); // gsttotal
      generalUtils.dfs(tmp, (short)17, b);
      generalUtils.doubleDPs(b, b, '2');
      generalUtils.putAlpha(data, 4000, (short)28, b); // totaltotal

      generalUtils.dfs(tmp, (short)28, b); generalUtils.putAlpha(data, 4000, (short)15, b); // storecode
      generalUtils.dfs(tmp, (short)30, b); generalUtils.putAlpha(data, 4000, (short)18, b); // rate

      byte[] currency = new byte[4];
      generalUtils.dfs(tmp, (short)31, currency);
      generalUtils.putAlpha(data, 4000, (short)19, currency); // currency2

      generalUtils.dfs(tmp, (short)32, b); generalUtils.putAlpha(data, 4000, (short)20, b); // shipname
      generalUtils.dfs(tmp, (short)33, b); generalUtils.putAlpha(data, 4000, (short)21, b); // shipaddr1
      generalUtils.dfs(tmp, (short)34, b); generalUtils.putAlpha(data, 4000, (short)22, b); // shipaddr2
      generalUtils.dfs(tmp, (short)35, b); generalUtils.putAlpha(data, 4000, (short)23, b); // shipaddr3
      generalUtils.dfs(tmp, (short)36, b); generalUtils.putAlpha(data, 4000, (short)24, b); // shipaddr4
      generalUtils.dfs(tmp, (short)37, b); generalUtils.putAlpha(data, 4000, (short)25, b); // shipaddr5
      generalUtils.dfs(tmp, (short)38, b); generalUtils.putAlpha(data, 4000, (short)26, b); // revisionof
      generalUtils.dfs(tmp, (short)22, b); generalUtils.putAlpha(data, 4000, (short)30, b); // signon
      generalUtils.dfs(tmp, (short)43, b); generalUtils.putAlpha(data, 4000, (short)47, b); // revision
      generalUtils.dfs(tmp, (short)44, b); generalUtils.putAlpha(data, 4000, (short)48, b); // deliverymethod
      generalUtils.dfs(tmp, (short)45, b); generalUtils.putAlpha(data, 4000, (short)49, b); // shipto
      generalUtils.dfs(tmp, (short)46, b); generalUtils.putAlpha(data, 4000, (short)50, b); // invoiceto
      generalUtils.dfs(tmp, (short)47, b); generalUtils.putAlpha(data, 4000, (short)51, b); // customer
      generalUtils.dfs(tmp, (short)48, b); generalUtils.putAlpha(data, 4000, (short)53, b); // shippingterms
      generalUtils.dfs(tmp, (short)52, b); generalUtils.putAlpha(data, 4000, (short)52, b); // misc3
      generalUtils.dfs(tmp, (short)53, b); generalUtils.putAlpha(data, 4000, (short)56, b); // requiredby

      generalUtils.dfs(tmp, (short)41, b); 
      String salesPerson = profile.getNameFromProfile(con, stmt, rs, generalUtils.stringFromBytes(b, 0));
      generalUtils.putAlpha(data, 4000, (short)57, salesPerson);

      generalUtils.dfs(tmp, (short)50, b); 
      generalUtils.doubleDPs(b, b, '2');
      generalUtils.putAlpha(data, 4000, (short)54, b); // groupdiscount

      groupDiscount = generalUtils.dfsAsDouble(tmp, (short)50);

      // modify the groupdiscount text for display
      generalUtils.dfs(tmp, (short)50, b); 
      generalUtils.doubleDPs(b, b, '2');
      generalUtils.putAlpha(data, 4000, (short)54, b); // groupdiscountamt
      generalUtils.dfs(tmp, (short)49, b); // groupdiscounttype

      byte[] a = new byte[20];
      if(b[0] == '%')
      {
        generalUtils.dfs(tmp, (short)50, a); // groupdiscountamount
        generalUtils.catAsBytes(" %", 0, a, false);
      }
      else // != '%'
      {
        generalUtils.dfs(tmp, (short)50, a); // groupdiscountamount
        generalUtils.doubleDPs(a, a, '2');
      }
      generalUtils.putAlpha(data, 4000, (short)55, a); // groupdiscountvalue
      
      double d1 = generalUtils.dfsAsDouble(tmp, (short)16);
      double d2 = generalUtils.dfsAsDouble(tmp, (short)17);
      double totalSansGST = d2 - d1;
      generalUtils.repDoubleGivenSeparator('2', '\000', data, 4000, (short)29, totalSansGST);

      remarkType = generalUtils.dfsAsStr(tmp, (short)42);
    }

    reportGenDetails.processControl(dnm, unm, localDefnsDir, defnsDir, dp, negStyle, zeroStyle, tm, bm, lm, rm, pageSizeWidth, pageSizeLength, currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName, userFontStyle,
                         userFontItalic, userFontSize, fhPPR[0], fhO[0], fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord, across2Coord, oBuf, gstRates);
    reportGenDetails.processSection("RH", null, null, (short)0, dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0], pageSizeWidth[0], pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName,
                         userFontStyle, userFontItalic, userFontSize, fhPPR[0], fhO[0], fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord, across2Coord, oBuf, gstRates);
    reportGenDetails.processSection("PH", data, fldNames, (short)61, dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0], pageSizeWidth[0], pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName,
                         userFontStyle, userFontItalic, userFontSize, fhPPR[0], fhO[0], fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord, across2Coord, oBuf, gstRates);

    char dpOnQuantities = miscDefinitions.dpOnQuantities(con, stmt, rs, "5");
    byte[] lastEntry = new byte[6];      lastEntry[0] = '\000';
    byte[] lastItemCode = new byte[21];  lastItemCode[0] = '\000';
    byte[] itemCode           = new byte[21];
    byte[] lineNumB           = new byte[20];
    byte[] entry              = new byte[20];
    byte[] desc               = new byte[81];
    byte[] unitPrice          = new byte[21];
    byte[] quantity           = new byte[20];
    byte[] amount2            = new byte[20];
    byte[] gstRate            = new byte[20];
    byte[] discountPercentage = new byte[20];
    byte[] uom                = new byte[20];
    byte[] storeCode          = new byte[50];
    byte[] suppItemCode       = new byte[41];
    byte[] reqdBy             = new byte[20];
    byte[] deliveryMethod     = new byte[21];
    byte[] mfr                = new byte[31];
    byte[] mfrCode            = new byte[61];
    byte[] data2              = new byte[1000];
    int[] lineCount = new int[1];  lineCount[0] = 1;
    int[] pageCount = new int[1];  pageCount[0] = 1;
    
    for(int xx=0;xx<linesCount[0];++xx)
    {
      if(generalUtils.getListEntryByNum(xx, linesData, tmp)) // just-in-case
      {
        generalUtils.replaceTwosWithOnes(tmp);
 
        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)22, lineNumB);
        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)2,  itemCode);
        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)23, entry); 
        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)31, mfr);
        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)3,  desc);
        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)4,  unitPrice);
        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)5,  quantity);
        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)20, amount2);
        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)7,  gstRate);
        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)24, discountPercentage);
        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)21, uom);
        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)25, storeCode);
        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)26, reqdBy);
        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)15, deliveryMethod);
        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)28, suppItemCode);
        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)32, mfrCode);

        if(miscDefinitions.prependMfrCode(con, stmt, rs) && mfrCode[0] != '\000')
        {  
          outputDetailLine(con, stmt, rs, data, fldNames, lineNumB, itemCode, mfrCode, unitPrice, quantity, amount2, gstRate, discountPercentage, uom, storeCode, reqdBy, deliveryMethod, suppItemCode, mfr, mfrCode, entry, lastEntry, lineCount,
                           pageCount, numPages, docSizeMax, dpOnQuantities, unm, dnm, localDefnsDir, defnsDir, tm, bm, lm, rm, pageSizeWidth, pageSizeLength, currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName,
                           userFontStyle, userFontItalic, userFontSize, fhPPR, fhO, fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord, across2Coord, oBuf, gstRates);

          entry[0] = '\000'; unitPrice[0] = '\000'; quantity[0] = '\000'; discountPercentage[0] = '\000'; amount2[0] = '\000'; gstRate[0] = '\000';
          uom[0] = '\000'; suppItemCode[0] = '\000'; storeCode[0] = '\000'; deliveryMethod[0] = '\000'; //reqdBy[0] = '\000';
          if(lineCount[0] >= docSizeMax)
          {
            b[0] = '\000';
            reportGenDetails.outputLine('E', b, data, fldNames, (short)61, ' ', dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0], pageSizeWidth[0], pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF,
                             userFontName, userFontStyle, userFontItalic, userFontSize, fhPPR[0], fhO[0], fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord, across2Coord, oBuf, gstRates);
            ++numPages;
            lineCount[0] = 1;
          }
        }

        outputDetailLine(con, stmt, rs, data, fldNames, lineNumB, itemCode, desc, unitPrice, quantity, amount2, gstRate, discountPercentage, uom, storeCode, reqdBy, deliveryMethod, suppItemCode, mfr, mfrCode, entry, lastEntry, lineCount,
                         pageCount, numPages, docSizeMax, dpOnQuantities, unm, dnm, localDefnsDir, defnsDir, tm, bm, lm, rm, pageSizeWidth, pageSizeLength, currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName,
                         userFontStyle, userFontItalic, userFontSize, fhPPR, fhO, fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord, across2Coord, oBuf, gstRates);

        if(lineCount[0] >= docSizeMax)
        {
          b[0] = '\000';
          reportGenDetails.outputLine('E', b, data, fldNames, (short)61, ' ', dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0], pageSizeWidth[0], pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF,
                           userFontName, userFontStyle, userFontItalic, userFontSize, fhPPR[0], fhO[0], fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord, across2Coord, oBuf, gstRates);
          ++numPages;
          lineCount[0] = 1;
        }

        // output any LL lines
        b[0] = '\000';
        generalUtils.repAlpha(data, 4000, (short)31, b); 
        generalUtils.repAlpha(data, 4000, (short)32, b); 
        generalUtils.repAlpha(data, 4000, (short)33, b); 
        generalUtils.repAlpha(data, 4000, (short)34, b);
        generalUtils.repAlpha(data, 4000, (short)35, b);
        generalUtils.repAlpha(data, 4000, (short)36, b);
        generalUtils.repAlpha(data, 4000, (short)37, b);
        generalUtils.repAlpha(data, 4000, (short)38, b);
        generalUtils.repAlpha(data, 4000, (short)39, b); 
        generalUtils.repAlpha(data, 4000, (short)40, b); 
        generalUtils.repAlpha(data, 4000, (short)41, b); 
        generalUtils.repAlpha(data, 4000, (short)42, b); 
        generalUtils.repAlpha(data, 4000, (short)45, b); 
        generalUtils.repAlpha(data, 4000, (short)58, b); 
        generalUtils.repAlpha(data, 4000, (short)59, b);
        generalUtils.repAlpha(data, 4000, (short)60, b);

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
            generalUtils.repAlpha(data, 4000, (short)33, b); // desc
            reportGenDetails.processSection("BL1", data, fldNames, (short)61, dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0], pageSizeWidth[0], pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF,
                                   userFontName, userFontStyle, userFontItalic, userFontSize, fhPPR[0], fhO[0], fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord, across2Coord, oBuf, gstRates);
            ++lineCount[0];

            if(lineCount[0] >= docSizeMax)
            {
              b[0] = '\000';
              reportGenDetails.outputLine('E', b, data, fldNames, (short)61, ' ', dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0], pageSizeWidth[0], pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF,
                               userFontName, userFontStyle, userFontItalic, userFontSize, fhPPR[0], fhO[0], fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord, across2Coord, oBuf, gstRates);
              ++numPages;
              lineCount[0] = 1;
            }

            llCount = generalUtils.getListEntryByNumAndValue(llCount, multipleLinesCount[0], lineNumB, multipleLinesData, data2);
            if(llCount == -1)
            {
              quit1 = true; // on last rec
            }
            else
            {
              if(! generalUtils.match(code, generalUtils.dfsAsStr(data2, (short)1)) || ! generalUtils.match(lineNumB, generalUtils.dfsAsStr(data2, (short)2)) ) // stepped on
                quit1 = true;
            }
          }
        }

        // append the delivery date (if it exists)
        if(reqdBy[0] != '\000' && ! generalUtils.match(reqdBy, "1970-01-01"))
        {
          if(miscDefinitions.appendDeliveryDate(con, stmt, rs))
          {
            generalUtils.repAlpha(data, 4000, (short)33, "Delivery Date: " + generalUtils.convertFromYYYYMMDD2(reqdBy));
            b[0] = '\000';
            generalUtils.repAlpha(data, 4000, (short)31, b);
            generalUtils.repAlpha(data, 4000, (short)32, b);
            generalUtils.repAlpha(data, 4000, (short)34, b);
            generalUtils.repAlpha(data, 4000, (short)35, b);
            generalUtils.repAlpha(data, 4000, (short)36, b);
            generalUtils.repAlpha(data, 4000, (short)37, b);
            generalUtils.repAlpha(data, 4000, (short)38, b);
            generalUtils.repAlpha(data, 4000, (short)39, b);
            generalUtils.repAlpha(data, 4000, (short)40, b);
            generalUtils.repAlpha(data, 4000, (short)41, b);
            generalUtils.repAlpha(data, 4000, (short)42, b);
            generalUtils.repAlpha(data, 4000, (short)45, b);
            generalUtils.repAlpha(data, 4000, (short)58, b);
            generalUtils.repAlpha(data, 4000, (short)59, b);
            generalUtils.repAlpha(data, 4000, (short)60, b);

            reportGenDetails.processSection("BL1", data, fldNames, (short)61, dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0], pageSizeWidth[0], pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF,
                                 userFontName, userFontStyle, userFontItalic, userFontSize, fhPPR[0], fhO[0], fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord, across2Coord, oBuf, gstRates);
            ++ lineCount[0];
            if(lineCount[0] >= docSizeMax)
            {
              b[0] = '\000';
              reportGenDetails.outputLine('E', b, data, fldNames, (short)61, ' ', dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0], pageSizeWidth[0], pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF,
                               userFontName, userFontStyle, userFontItalic, userFontSize, fhPPR[0], fhO[0], fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord, across2Coord, oBuf, gstRates);
              ++numPages;
              lineCount[0] = 1;
            }
          }
        }

        generalUtils.bytesToBytes(lastItemCode, 0, itemCode, 0);
      }
    }

    localPurchase.getRecGivenCode(con, stmt, rs, code, '\000', tmp, dnm, localDefnsDir, defnsDir, bytesOut);

    generalUtils.dfs(tmp, (short)11, b);
    int x=0,  y=0;
    while(b[x] != '\001' && b[x] != '\000')
      b2[y++] = b[x++];
    b2[y] = '\000';
    generalUtils.repAlpha(data, 4000, (short)43, b2);

    reportGenDetails.processSection("BL2", data, fldNames, (short)61, dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0], pageSizeWidth[0], pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName,
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
      generalUtils.repAlpha(data, 4000, (short)44, b2);

      reportGenDetails.processSection("BL3", data, fldNames, (short)61, dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0], pageSizeWidth[0], pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF,
                           userFontName, userFontStyle, userFontItalic, userFontSize, fhPPR[0], fhO[0], fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord, across2Coord, oBuf, gstRates);

      if(b[x] == '\001')
       ++x; // lf
    }

    reportGenDetails.processSection("PF", data, fldNames, (short)61, dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0], pageSizeWidth[0], pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName,
                         userFontStyle, userFontItalic, userFontSize, fhPPR[0], fhO[0], fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord, across2Coord, oBuf, gstRates);

    if(groupDiscount == 0.0)
    {
      reportGenDetails.processSection("RF1", data, fldNames, (short)61, dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0], pageSizeWidth[0], pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF,
                           userFontName, userFontStyle, userFontItalic, userFontSize, fhPPR[0], fhO[0], fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord, across2Coord, oBuf, gstRates);
    }
    else // there is a discount
    {
      reportGenDetails.processSection("RF2", data, fldNames, (short)61, dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0], pageSizeWidth[0], pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF,
                           userFontName, userFontStyle, userFontItalic, userFontSize, fhPPR[0], fhO[0], fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord, across2Coord, oBuf, gstRates);
    }

    // posn cursor
    reportGenDetails.processSection("RF5", data, fldNames, (short)61, dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0], pageSizeWidth[0], pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName,
                         userFontStyle, userFontItalic, userFontSize, fhPPR[0], fhO[0], fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord, across2Coord, oBuf, gstRates);

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
                            String[] userFontSize, RandomAccessFile[] fhPPR, RandomAccessFile[] fhO, char[] fontDefType, char[] fontDefStyle, char[] fontDefItalic, int[] fontDefSize, double[] down2Coord, double[] across2Coord, byte[] oBuf,
                            String[] gstRates) throws Exception
  {
    if(remarkType.length() == 0) return;

    int x=0;
    String s = "";
    while(! s.equals(" "))
    {
      s = generalUtils.getRemark('Q', remarkType, x++, localDefnsDir, defnsDir);
      generalUtils.repAlpha(data, 4000, (short)42, s);

      reportGenDetails.processSection("RF6", data, fldNames, (short)61, dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0], pageSizeWidth[0], pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF,
                           userFontName, userFontStyle, userFontItalic, userFontSize, fhPPR[0], fhO[0], fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord, across2Coord, oBuf, gstRates);
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void outputDetailLine(Connection con, Statement stmt, ResultSet rs, byte[] data, byte[] fldNames, byte[] lineNumB, byte[] itemCode, byte[] desc, byte[] unitPrice, byte[] quantity, byte[] amount2, byte[] gstRate,
                                byte[] discountPercentage, byte[] uom, byte[] storeCode, byte[] reqdBy, byte[] deliveryMethod, byte[] suppItemCode, byte[] mfr, byte[] mfrCode, byte[] entry, byte[] lastEntry, int[] lineCount,
                                int[] pageCount, int numPages, int docSizeMax, char dpOnQuantities, String unm, String dnm, String localDefnsDir, String defnsDir, double[] tm, double[] bm, double[] lm, double[] rm, double[] pageSizeWidth,
                                double[] pageSizeLength, double[] currDown, double[] currAcross, short[] currFont, short[] currPage, short[] oPtr, short oBufLen, boolean[] lastOperationPF, String[] userFontName, String[] userFontStyle,
                                String[] userFontItalic, String[] userFontSize, RandomAccessFile[] fhPPR, RandomAccessFile[] fhO, char[] fontDefType, char[] fontDefStyle, char[] fontDefItalic, int[] fontDefSize, double[] down2Coord,
                                double[] across2Coord, byte[] oBuf, String[] gstRates) throws Exception
  {
    byte[] b = new byte[100];

    generalUtils.repAlpha(data, 4000, (short)31, entry);
    generalUtils.repAlpha(data, 4000, (short)32, itemCode);
    generalUtils.repAlpha(data, 4000, (short)33, desc);

    if(unitPrice[0] != '\000')
      generalUtils.bytesDPsGivenSeparator(true, '\000', '2', unitPrice, 20, 0);
    generalUtils.repAlpha(data, 4000, (short)34, unitPrice);

    if(quantity[0] != '\000')
    {
      generalUtils.bytesDPsGivenSeparator(true, '\000', dpOnQuantities, quantity, 20, 0);
      generalUtils.formatNumeric(b, dpOnQuantities);
    }
    generalUtils.repAlpha(data, 4000, (short)35, quantity);

    if(amount2[0] != '\000')
      generalUtils.bytesDPsGivenSeparator(true, '\000', '2', amount2, 20, 0);
    generalUtils.repAlpha(data, 4000, (short)36, amount2);

    generalUtils.repAlpha(data, 4000, (short)37, gstRate);

    if(discountPercentage[0] != '\000')
      generalUtils.bytesDPsGivenSeparator(true, '\000', '2', discountPercentage, 20, 0);
    generalUtils.repAlpha(data, 4000, (short)58, discountPercentage);

    generalUtils.repAlpha(data, 4000, (short)38, uom);
    generalUtils.repAlpha(data, 4000, (short)39, storeCode);
    generalUtils.repAlpha(data, 4000, (short)40, generalUtils.convertFromYYYYMMDD2(reqdBy));
    generalUtils.repAlpha(data, 4000, (short)41, deliveryMethod);
    generalUtils.repAlpha(data, 4000, (short)45, suppItemCode);
    generalUtils.repAlpha(data, 4000, (short)59, mfr);

    if(generalUtils.lengthBytes(suppItemCode, 0) > 0)
      generalUtils.repAlpha(data, 4000, (short)60, suppItemCode);
    else generalUtils.repAlpha(data, 4000, (short)60, mfrCode);

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
      }

      generalUtils.bytesToBytes(lastEntry, 0, entry, 0);
    }
    else
    {
      generalUtils.repAlpha(data, 4000, (short)15, entry);
      generalUtils.repAlpha(data, 4000, (short)16, itemCode);
      generalUtils.repAlpha(data, 4000, (short)17, desc);
      generalUtils.repAlpha(data, 4000, (short)59, mfr);

      if(generalUtils.lengthBytes(suppItemCode, 0) > 0)
        generalUtils.repAlpha(data, 4000, (short)60, suppItemCode);
      else generalUtils.repAlpha(data, 4000, (short)60, mfrCode);
    }

    reportGenDetails.processSection("BL1", data, fldNames, (short)61, dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0], pageSizeWidth[0], pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName,
                         userFontStyle, userFontItalic, userFontSize, fhPPR[0], fhO[0], fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord, across2Coord, oBuf, gstRates);
    ++lineCount[0];
  }

}
