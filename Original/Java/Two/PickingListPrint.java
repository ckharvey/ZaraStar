// =======================================================================================================================================================================================================
// System: ZaraStar Document: PL: Print
// Module: PickingListPrint.java
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

public class PickingListPrint extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  ReportGenDetails reportGenDetails = new ReportGenDetails();
  PickingList pickingList = new PickingList();
  Inventory inventory = new Inventory();
  MiscDefinitions miscDefinitions = new MiscDefinitions();
 
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
      String urlBit="";
      int x=0;
      if(url.charAt(x) == 'h' || url.charAt(x) == 'H')
        x += 7;
      while(url.charAt(x) != '\000' && url.charAt(x) != ',' && url.charAt(x) != '/')
        urlBit += url.charAt(x++);

      System.out.println(e);
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "PickingListPrint", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3048, bytesOut[0], 0, "ERR:" + p1);
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

    String userName = directoryUtils.getMySQLUserName();
    String passWord = directoryUtils.getMySQLPassWord();
    
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + userName + "&password=" + passWord);
    Statement stmt = null, stmt2 = null;
    ResultSet rs = null;
    
    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 3038, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "3048", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3048, bytesOut[0], 0, "ACC:" + p1);
      if(con  != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "3048", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3048, bytesOut[0], 0, "SID:" + p1);
      if(con  != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    byte[] code = new byte[21];
    generalUtils.strToBytes(code, p1);

    int numPages = generate(con, stmt, stmt2, rs, code, unm, sid, uty, men, den, dnm, bnm, workingDir, reportsDir, localDefnsDir, defnsDir, bytesOut);
    switch(numPages)
    {
      case -1 : // Definition File Not Found
                messagePage.msgScreen(false, out, req, 17, unm, sid, uty, men, den, dnm, bnm, "3048", imagesDir, localDefnsDir, defnsDir, bytesOut);
                break;
      case -2 : // cannot create report output file
                messagePage.msgScreen(false, out, req, 18, unm, sid, uty, men, den, dnm, bnm, "3048", imagesDir, localDefnsDir, defnsDir, bytesOut);
                break;
      default : // generated ok
                out.println(generalUtils.intToStr(numPages));
                break;
    }

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 3048, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1 + " print");
    if(con  != null) con.close();
    if(out != null) out.flush(); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int generate(Connection con, Statement stmt, Statement stmt2, ResultSet rs, byte[] code, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String workingDir, String reportsDir, String localDefnsDir,
                       String defnsDir, int[] bytesOut) throws Exception
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

    if((fhPPR[0] = generalUtils.fileOpenD("202.ppr", localDefnsDir)) == null)
    {
      if((fhPPR[0] = generalUtils.fileOpenD("202.ppr", defnsDir)) == null)
      {
        generalUtils.fileClose(fhO[0]);
        return -1;
      }
    }

    String barcodeFileName = workingDir + "barcodePickingListPrint.png";

    lastOperationPF[0] = false;

    byte[] fldNames = new byte[10000];
    byte[] data     = new byte[4000];

    boolean convertToUpperCase;
    String s = generalUtils.getFromDefnFile("PICKINGLISTUPPERCASE", "document.dfn", localDefnsDir, defnsDir);
    if(s.length() > 0 && (s.charAt(0) == 'Y' || s.charAt(0) == 'y'))
      convertToUpperCase = true;
    else convertToUpperCase = false;

    generalUtils.putAlpha(fldNames, 4000, (short)0,  "Picklist.PLCode");
    generalUtils.putAlpha(fldNames, 4000, (short)1,  "Picklist.Date");
    generalUtils.putAlpha(fldNames, 4000, (short)2,  "Picklist.CompanyCode");
    generalUtils.putAlpha(fldNames, 4000, (short)3,  "Picklist.CompanyName");
    generalUtils.putAlpha(fldNames, 4000, (short)4,  "Picklist.Address1");
    generalUtils.putAlpha(fldNames, 4000, (short)5,  "Picklist.Address2");
    generalUtils.putAlpha(fldNames, 4000, (short)6,  "Picklist.Address3");
    generalUtils.putAlpha(fldNames, 4000, (short)7,  "Picklist.Address4");
    generalUtils.putAlpha(fldNames, 4000, (short)8,  "Picklist.Address5");
    generalUtils.putAlpha(fldNames, 4000, (short)9,  "Picklist.PostCode");
    generalUtils.putAlpha(fldNames, 4000, (short)10, "Picklist.FAO");
    generalUtils.putAlpha(fldNames, 4000, (short)11, "Picklist.Misc1");
    generalUtils.putAlpha(fldNames, 4000, (short)12, "Picklist.Misc2");
    generalUtils.putAlpha(fldNames, 4000, (short)13, "Picklist.DateRange");
    generalUtils.putAlpha(fldNames, 4000, (short)14, "Picklist.Attention");
    generalUtils.putAlpha(fldNames, 4000, (short)15, "Picklisl.Entry");
    generalUtils.putAlpha(fldNames, 4000, (short)16, "Picklisl.ItemCode");
    generalUtils.putAlpha(fldNames, 4000, (short)17, "Picklisl.Description");
    generalUtils.putAlpha(fldNames, 4000, (short)18, "Picklisl.UnitPrice");
    generalUtils.putAlpha(fldNames, 4000, (short)19, "Picklisl.Quantity");
    generalUtils.putAlpha(fldNames, 4000, (short)20, "Picklisl.DiscountPercentage");
    generalUtils.putAlpha(fldNames, 4000, (short)21, "Picklisl.Amount");
    generalUtils.putAlpha(fldNames, 4000, (short)22, "Picklisl.GSTRate");
    generalUtils.putAlpha(fldNames, 4000, (short)23, "Picklisl.UoM");
    generalUtils.putAlpha(fldNames, 4000, (short)24, "Picklisl.Amount2");
    generalUtils.putAlpha(fldNames, 4000, (short)25, "Picklisl.PORefNum");
    generalUtils.putAlpha(fldNames, 4000, (short)26, "Picklisl.SerialNumber");
    generalUtils.putAlpha(fldNames, 4000, (short)27, "Picklisl.QuantityRequired");
    generalUtils.putAlpha(fldNames, 4000, (short)28, "Picklisl.SOCode");
    generalUtils.putAlpha(fldNames, 4000, (short)29, "Picklisl.GrossWeight");
    generalUtils.putAlpha(fldNames, 4000, (short)30, "Picklisl.WeightPer");
    generalUtils.putAlpha(fldNames, 4000, (short)31, "Picklist.GSTTotal");
    generalUtils.putAlpha(fldNames, 4000, (short)32, "Picklist.TotalTotal");
    generalUtils.putAlpha(fldNames, 4000, (short)33, "Picklist.GroupDiscount");
    generalUtils.putAlpha(fldNames, 4000, (short)34, "Picklist.GroupDiscountType");
    generalUtils.putAlpha(fldNames, 4000, (short)35, "Picklist.TotalWithoutGST");
    generalUtils.putAlpha(fldNames, 4000, (short)36, "Picklist.SalesPerson");
    generalUtils.putAlpha(fldNames, 4000, (short)37, "Picklist.Returned");
    generalUtils.putAlpha(fldNames, 4000, (short)38, "Picklist.PORefNum");
    generalUtils.putAlpha(fldNames, 4000, (short)39, "Picklist.SignOn");
    generalUtils.putAlpha(fldNames, 4000, (short)40, "Picklist.Type");
    generalUtils.putAlpha(fldNames, 4000, (short)41, "Picklist.PurchasedBy");
    generalUtils.putAlpha(fldNames, 4000, (short)42, "Picklist.CCode");
    generalUtils.putAlpha(fldNames, 4000, (short)43, "Picklist.RevisionOf");
    generalUtils.putAlpha(fldNames, 4000, (short)44, "Picklist.ShipName");
    generalUtils.putAlpha(fldNames, 4000, (short)45, "Picklist.ShipAddress1");
    generalUtils.putAlpha(fldNames, 4000, (short)46, "Picklist.ShipAddress2");
    generalUtils.putAlpha(fldNames, 4000, (short)47, "Picklist.ShipAddress3");
    generalUtils.putAlpha(fldNames, 4000, (short)48, "Picklist.ShipAddress4");
    generalUtils.putAlpha(fldNames, 4000, (short)49, "Picklist.ShipAddress5");
    generalUtils.putAlpha(fldNames, 4000, (short)50, "Picklist.Currency");
    generalUtils.putAlpha(fldNames, 4000, (short)51, "Picklist.NumCartons");
    generalUtils.putAlpha(fldNames, 4000, (short)52, "Picklist.Rate");
    generalUtils.putAlpha(fldNames, 4000, (short)53, "Picklist.AccountCompany");
    generalUtils.putAlpha(fldNames, 4000, (short)54, "Picklist.NoteLine1");
    generalUtils.putAlpha(fldNames, 4000, (short)55, "Picklist.NoteLines");
    generalUtils.putAlpha(fldNames, 4000, (short)56, "Picklist.Time");
    generalUtils.putAlpha(fldNames, 4000, (short)57, "Picklist.ExpectedDate");
    generalUtils.putAlpha(fldNames, 4000, (short)58, "Picklisl.QuantityPacked");
    generalUtils.putAlpha(fldNames, 4000, (short)59, "Picklisl.Instruction");
    generalUtils.putAlpha(fldNames, 4000, (short)60, "Picklisl.CustomerItemCode");
    generalUtils.putAlpha(fldNames, 4000, (short)61, "Picklisl.LotNum");
    generalUtils.putAlpha(fldNames, 4000, (short)62, "Picklist.Terms");
    generalUtils.putAlpha(fldNames, 4000, (short)63, "Picklist.TotalNetWeight");
    generalUtils.putAlpha(fldNames, 4000, (short)64, "Picklist.Dimension");
    generalUtils.putAlpha(fldNames, 4000, (short)65, "Picklist.ExportStatement");
    generalUtils.putAlpha(fldNames, 4000, (short)66, "Picklist.InvoiceCode");
    generalUtils.putAlpha(fldNames, 4000, (short)67, "Picklist.OCCode");
    generalUtils.putAlpha(fldNames, 4000, (short)68, "Picklisl.InvoiceCode");
    generalUtils.putAlpha(fldNames, 4000, (short)69, "Picklisl.Store1");
    generalUtils.putAlpha(fldNames, 4000, (short)70, "Picklisl.Store2");
    generalUtils.putAlpha(fldNames, 4000, (short)71, "Picklisl.NetWeight");
    generalUtils.putAlpha(fldNames, 4000, (short)72, "Picklist.TotalGrossWeight");
    generalUtils.putAlpha(fldNames, 4000, (short)73, "Picklisl.Manufacturer");
    generalUtils.putAlpha(fldNames, 4000, (short)74, "Picklisl.ManufacturerCode");
    generalUtils.putAlpha(fldNames, 4000, (short)75, "Picklist.AssignedTo");
    generalUtils.putAlpha(fldNames, 4000, (short)76, "Picklist.DateTimeAssigned");
    generalUtils.putAlpha(fldNames, 4000, (short)77, "Picklisl.Location");
    
    if(pickingList.getRecGivenCode(con, stmt, rs, code, '\000', tmp, dnm, localDefnsDir, defnsDir, bytesOut) != -1L) // just-in-case
    {
      generalUtils.putAlpha(data, 4000, (short)0, code);
      generalUtils.dfs(tmp, (short)1, b);
      if(convertToUpperCase) generalUtils.toUpper(b, 0);
      generalUtils.putAlpha(data, 4000, (short)1, generalUtils.convertFromYYYYMMDD(b)); // date

      generalUtils.dfs(tmp, (short)2, b);
      if(convertToUpperCase) generalUtils.toUpper(b, 0);
      generalUtils.putAlpha(data, 4000, (short)2, b); // companycode

      generalUtils.dfs(tmp, (short)3, b);
      if(convertToUpperCase) generalUtils.toUpper(b, 0);
      generalUtils.putAlpha(data, 4000, (short)3, b); // companyname

      generalUtils.dfs(tmp, (short)4, b);
      if(convertToUpperCase) generalUtils.toUpper(b, 0);
      generalUtils.putAlpha(data, 4000, (short)4, b); // address1

      generalUtils.dfs(tmp, (short)5, b);
      if(convertToUpperCase) generalUtils.toUpper(b, 0);
      generalUtils.putAlpha(data, 4000, (short)5, b); // address2

      generalUtils.dfs(tmp, (short)6, b);
      if(convertToUpperCase) generalUtils.toUpper(b, 0);
      generalUtils.putAlpha(data, 4000, (short)6, b); // address3

      generalUtils.dfs(tmp, (short)7, b);
      if(convertToUpperCase) generalUtils.toUpper(b, 0);
      generalUtils.putAlpha(data, 4000, (short)7, b); // address4

      generalUtils.dfs(tmp, (short)8, b);
      if(convertToUpperCase) generalUtils.toUpper(b, 0);
      generalUtils.putAlpha(data, 4000, (short)8, b); // address5

      generalUtils.dfs(tmp, (short)9, b);
      if(convertToUpperCase) generalUtils.toUpper(b, 0);
      generalUtils.putAlpha(data, 4000, (short)9, b); // postcode

      generalUtils.dfs(tmp, (short)10, b);
      if(convertToUpperCase) generalUtils.toUpper(b, 0);
      generalUtils.putAlpha(data, 4000, (short)10, b); // FAO

      generalUtils.dfs(tmp, (short)15, b);
      if(convertToUpperCase) generalUtils.toUpper(b, 0);
      generalUtils.putAlpha(data, 4000, (short)11, b); // misc1

      generalUtils.dfs(tmp, (short)16, b);
      if(convertToUpperCase) generalUtils.toUpper(b, 0);
      generalUtils.putAlpha(data, 4000, (short)12, b); // misc2

      generalUtils.dfs(tmp, (short)12, b);
      if(convertToUpperCase) generalUtils.toUpper(b, 0);
      generalUtils.putAlpha(data, 4000, (short)14, b); // attention

      generalUtils.dfs(tmp, (short)17, b);
      generalUtils.doubleDPs(b, b, '2');
      generalUtils.putAlpha(data, 4000, (short)31, b); // gsttotal

      generalUtils.dfs(tmp, (short)18, b);
      generalUtils.doubleDPs(b, b, '2');
      generalUtils.putAlpha(data, 4000, (short)32, b); // totaltotal

      generalUtils.dfs(tmp, (short)21, b);
      generalUtils.doubleDPs(b, b, '2');
      generalUtils.putAlpha(data, 4000, (short)33, b); // groupdiscount

      generalUtils.dfs(tmp, (short)24, b);
      b2[0] = ' ';
      generalUtils.bytesToBytes(b2, 1, b, 0);
      generalUtils.putAlpha(data, 4000, (short)34, b); // groupdiscounttype

      double d1 = generalUtils.dfsAsDouble(tmp, (short)17);
      double d2 = generalUtils.dfsAsDouble(tmp, (short)18);
      double totalSansGST = d2 - d1;
      generalUtils.repDoubleGivenSeparator('2', '\000', data, 4000, (short)35, totalSansGST);

      generalUtils.dfs(tmp, (short)29, b); generalUtils.toUpper(b, 0); generalUtils.putAlpha(data, 4000, (short)36, b); // salesperson
      generalUtils.dfs(tmp, (short)19, b); generalUtils.toUpper(b, 0); generalUtils.putAlpha(data, 4000, (short)37, b); // returned
      generalUtils.dfs(tmp, (short)28, b); generalUtils.toUpper(b, 0); generalUtils.putAlpha(data, 4000, (short)38, b); // porefnum
      generalUtils.dfs(tmp, (short)20, b); generalUtils.toUpper(b, 0); generalUtils.putAlpha(data, 4000, (short)39, b); // signon
      generalUtils.dfs(tmp, (short)25, b); generalUtils.toUpper(b, 0); generalUtils.putAlpha(data, 4000, (short)40, b); // type
      generalUtils.dfs(tmp, (short)27, b); generalUtils.toUpper(b, 0); generalUtils.putAlpha(data, 4000, (short)41, b); // purchasedby
      generalUtils.dfs(tmp, (short)30, b); generalUtils.toUpper(b, 0); generalUtils.putAlpha(data, 4000, (short)42, b); // ccode
      generalUtils.dfs(tmp, (short)31, b); generalUtils.toUpper(b, 0); generalUtils.putAlpha(data, 4000, (short)43, b); // revisionof
      generalUtils.dfs(tmp, (short)32, b); generalUtils.toUpper(b, 0); generalUtils.putAlpha(data, 4000, (short)44, b); // shipname
      generalUtils.dfs(tmp, (short)33, b); generalUtils.toUpper(b, 0); generalUtils.putAlpha(data, 4000, (short)45, b); // shipaddr1
      generalUtils.dfs(tmp, (short)34, b); generalUtils.toUpper(b, 0); generalUtils.putAlpha(data, 4000, (short)46, b); // shipaddr2
      generalUtils.dfs(tmp, (short)35, b); generalUtils.toUpper(b, 0); generalUtils.putAlpha(data, 4000, (short)47, b); // shipaddr3
      generalUtils.dfs(tmp, (short)36, b); generalUtils.toUpper(b, 0); generalUtils.putAlpha(data, 4000, (short)48, b); // shipaddr4
      generalUtils.dfs(tmp, (short)37, b); generalUtils.toUpper(b, 0); generalUtils.putAlpha(data, 4000, (short)49, b); // shipaddr5
      generalUtils.dfs(tmp, (short)38, b); generalUtils.toUpper(b, 0); generalUtils.putAlpha(data, 4000, (short)50, b); // curr
      generalUtils.dfs(tmp, (short)39, b); generalUtils.toUpper(b, 0); generalUtils.putAlpha(data, 4000, (short)51, b); // numcartons
      generalUtils.dfs(tmp, (short)40, b); generalUtils.toUpper(b, 0); generalUtils.putAlpha(data, 4000, (short)52, b); // rate

      //generalUtils.dfs(tmp, (short)26, accountCompany); generalUtils.toUpper(b, 0); generalUtils.putAlpha(data, 4000, (short)53, accountCompany);

      generalUtils.dfs(tmp, (short)42, b); generalUtils.toUpper(b, 0); generalUtils.putAlpha(data, 4000, (short)56, b); // time
      generalUtils.dfs(tmp, (short)43, b); generalUtils.toUpper(b, 0); generalUtils.putAlpha(data, 4000, (short)57, generalUtils.convertFromYYYYMMDD(b)); // expecteddate
      generalUtils.dfs(tmp, (short)44, b); generalUtils.toUpper(b, 0); generalUtils.putAlpha(data, 4000, (short)62, b); // terms
      generalUtils.dfs(tmp, (short)54, b); generalUtils.toUpper(b, 0); generalUtils.putAlpha(data, 4000, (short)63, b); // totalnetweight
      generalUtils.dfs(tmp, (short)50, b); generalUtils.toUpper(b, 0); generalUtils.putAlpha(data, 4000, (short)64, b); // dimension
      generalUtils.dfs(tmp, (short)51, b); generalUtils.toUpper(b, 0); generalUtils.putAlpha(data, 4000, (short)65, b); // exportstatement
      generalUtils.dfs(tmp, (short)52, b); generalUtils.toUpper(b, 0); generalUtils.putAlpha(data, 4000, (short)66, b); // invoicecode
      generalUtils.dfs(tmp, (short)53, b); generalUtils.toUpper(b, 0); generalUtils.putAlpha(data, 4000, (short)67, b); // occode
      generalUtils.dfs(tmp, (short)49, b); generalUtils.toUpper(b, 0); generalUtils.putAlpha(data, 4000, (short)72, b); // totalgrossweight

      generalUtils.dfs(tmp, (short)56, b); generalUtils.toUpper(b, 0); generalUtils.putAlpha(data, 4000, (short)75, b); // assignedTo
      generalUtils.dfs(tmp, (short)57, b); generalUtils.toUpper(b, 0); generalUtils.putAlpha(data, 4000, (short)76, b); // dateTimeAssigned
    }

    reportGenDetails.processControl(dnm, unm, localDefnsDir, defnsDir, dp, negStyle, zeroStyle, tm, bm, lm, rm, pageSizeWidth, pageSizeLength, currDown,
                         currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName, userFontStyle, userFontItalic, userFontSize,
                         fhPPR[0], fhO[0], fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord, across2Coord, oBuf, gstRates);
    reportGenDetails.processSection("RH", "PH", "PF", null, null, (short)0, dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0], pageSizeWidth[0],
                         pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName, userFontStyle,
                         userFontItalic, userFontSize, fhPPR[0], fhO[0], fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord, across2Coord,
                         oBuf, barcodeFileName, gstRates);
    reportGenDetails.processSection("PH", "PH", "PF", data, fldNames, (short)78, dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0], pageSizeWidth[0],
                         pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName, userFontStyle,
                         userFontItalic, userFontSize, fhPPR[0], fhO[0], fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord, across2Coord,
                         oBuf, barcodeFileName, gstRates);

    char dpOnQuantities = miscDefinitions.dpOnQuantities(con, stmt, rs, "5");
    int numPages=1;

    int docSizeMax = miscDefinitions.docSizeMax(con, stmt, rs, "pl");

    // fetch lines data in one go
    byte[] linesData  = new byte[2000];
    int[]  listLen    = new int[1];  listLen[0] = 2000;
    int[]  linesCount = new int[1];
    linesData = pickingList.getLines(con, stmt, rs, code, '\001', linesData, listLen, linesCount, dnm, localDefnsDir, defnsDir);

    reportGenDetails.getGSTRatesFromDocument(linesData, linesCount[0], 7, gstRates);

    byte[] multipleLinesData  = new byte[2000];
    int[]  multipleListLen    = new int[1];  multipleListLen[0] = 2000;
    int[]  multipleLinesCount = new int[1];
    int llCount=0;
    if(linesCount[0] > 0) // get all the multiple lines for this document
      multipleLinesData = pickingList.getMultipleLines(con, stmt, rs, code, multipleLinesData, multipleListLen, multipleLinesCount, dnm, localDefnsDir, defnsDir);
    
    byte[] data2              = new byte[1000];
    byte[] lineNumB           = new byte[20];
    byte[] entry              = new byte[20];
    byte[] desc               = new byte[81];
    byte[] unitPrice          = new byte[21];
    byte[] quantity           = new byte[20];
    byte[] amount             = new byte[20];
    byte[] amount2            = new byte[20];
    byte[] gstRate            = new byte[20];
    byte[] discountPercentage = new byte[20];
    byte[] uom                = new byte[20];
    byte[] storeB             = new byte[50];
    byte[] mfr                = new byte[31];
    byte[] mfrCode            = new byte[61];
    byte[] itemCode           = new byte[21];
    byte[] serialNumber       = new byte[21];
    byte[] custItemCode       = new byte[41];
    byte[] qtyReqd            = new byte[20];
    byte[] soCode             = new byte[21];
    byte[] weightPer          = new byte[20];
    byte[] grossWeight        = new byte[20];
    byte[] qtyPacked          = new byte[20];
    byte[] instruction        = new byte[81];
    byte[] lotNum             = new byte[20];
    byte[] invoiceCode        = new byte[21];
    byte[] netWeight          = new byte[20];
    
    int[] lineCount = new int[1];  lineCount[0] = 1;
    int[] pageCount = new int[1];  pageCount[0] = 1;

    boolean prepending = miscDefinitions.plPrependMfrCode(con, stmt, rs);
    
    for(int xx=0;xx<linesCount[0];++xx)
    {
      if(generalUtils.getListEntryByNum(xx, linesData, tmp)) // just-in-case
      {
        generalUtils.replaceTwosWithOnes(tmp);

        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)19, lineNumB);
        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)20, entry); 
        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)2,  itemCode);
        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)3,  desc);
        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)4,  unitPrice);
        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)14, serialNumber);
        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)17, qtyReqd);
        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)21, soCode);
        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)23, weightPer);
        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)22, grossWeight);
        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)24, qtyPacked);
        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)25, instruction);
        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)26, lotNum);
        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)27, invoiceCode);
        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)28, netWeight);
        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)33, mfr);
        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)34, mfrCode);
        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)30, storeB);
        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)18, custItemCode);
        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)13, uom);
        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)6,  amount);
        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)11, amount2);
        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)7,  gstRate);
        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)8,  discountPercentage);
        generalUtils.dfsGivenSeparator(true, '\001', tmp, (short)5,  quantity);

        if(prepending && mfrCode[0] != '\000')
        {  
          numPages = outputDetailLine(con, stmt, stmt2, rs, true, true, data, fldNames, lineNumB, entry, itemCode, mfrCode, unitPrice, serialNumber, qtyReqd, soCode,
                                      weightPer, grossWeight, qtyPacked, instruction, lotNum, invoiceCode, netWeight, mfr, storeB, custItemCode, uom,
                                      amount, amount2, gstRate, discountPercentage, quantity, lineCount, dpOnQuantities, convertToUpperCase, numPages,
                                      docSizeMax, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, tm, bm, lm, rm, pageSizeWidth,
                                      pageSizeLength, currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName,
                                      userFontStyle, userFontItalic, userFontSize, fhPPR, fhO, fontDefType, fontDefStyle, fontDefItalic, fontDefSize,
                                      down2Coord, across2Coord, oBuf, barcodeFileName, gstRates);

          entry[0] = '\000';  unitPrice[0] = '\000';    quantity[0] = '\000';    discountPercentage[0] = '\000'; amount2[0]   = '\000';    gstRate[0] = '\000';
          uom[0] = '\000';    custItemCode[0] = '\000'; storeB[0] = '\000';      amount[0] = '\000';             serialNumber[0] = '\000'; qtyReqd[0] = '\000';
          soCode[0] = '\000'; weightPer[0] = '\000';    grossWeight[0] = '\000'; qtyPacked[0] = '\000';          instruction[0] = '\000';  lotNum[0] = '\000';
          invoiceCode[0] = '\000'; netWeight[0] = '\000'; quantity[0] = '\000';  itemCode[0] = '\000';
        }

        numPages = outputDetailLine(con, stmt, stmt2, rs, ! prepending, false, data, fldNames, lineNumB, entry, itemCode, desc, unitPrice, serialNumber, qtyReqd, soCode,
                                    weightPer, grossWeight, qtyPacked, instruction, lotNum, invoiceCode, netWeight, mfr, storeB, custItemCode, uom,
                                    amount, amount2, gstRate, discountPercentage, quantity, lineCount, dpOnQuantities, convertToUpperCase, numPages,
                                    docSizeMax, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, tm, bm, lm, rm, pageSizeWidth,
                                    pageSizeLength, currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName,
                                    userFontStyle, userFontItalic, userFontSize, fhPPR, fhO, fontDefType, fontDefStyle, fontDefItalic, fontDefSize,
                                    down2Coord, across2Coord, oBuf, barcodeFileName, gstRates);

        // output any LL lines
        b[0] = '\000';
        generalUtils.repAlpha(data, 4000, (short)15, b); // linenum
        generalUtils.repAlpha(data, 4000, (short)16, b); // itemCode
        generalUtils.repAlpha(data, 4000, (short)17, b); // desc
        generalUtils.repAlpha(data, 4000, (short)18, b); // unitprice
        generalUtils.repAlpha(data, 4000, (short)19, b); // qty
        generalUtils.repAlpha(data, 4000, (short)20, b); // discount%
        generalUtils.repAlpha(data, 4000, (short)21, b); // amt
        generalUtils.repAlpha(data, 4000, (short)22, b); // gstrate
        generalUtils.repAlpha(data, 4000, (short)23, b); // uom
        generalUtils.repAlpha(data, 4000, (short)24, b); // amt2
        generalUtils.repAlpha(data, 4000, (short)26, b); // serialnum
        generalUtils.repAlpha(data, 4000, (short)27, b); // qtyreqd
        generalUtils.repAlpha(data, 4000, (short)28, b); // socode
        generalUtils.repAlpha(data, 4000, (short)29, b); // grossweight
        generalUtils.repAlpha(data, 4000, (short)30, b); // weightper
        generalUtils.repAlpha(data, 4000, (short)58, b); // quantitypacked
        generalUtils.repAlpha(data, 4000, (short)59, b); // instruction
        generalUtils.repAlpha(data, 4000, (short)60, b); // custitemcode
        generalUtils.repAlpha(data, 4000, (short)61, b); // lotnum
        generalUtils.repAlpha(data, 4000, (short)69, b); // store1
        generalUtils.repAlpha(data, 4000, (short)70, b); // store2
        generalUtils.repAlpha(data, 4000, (short)71, b); // netweight
        generalUtils.repAlpha(data, 4000, (short)73, b); // mfr
        generalUtils.repAlpha(data, 4000, (short)74, b); // mfrCode

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
            reportGenDetails.processSection("BL1", "PH", "PF", data, fldNames, (short)78, dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0],
                                 pageSizeWidth[0], pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF,
                                 userFontName, userFontStyle, userFontItalic, userFontSize, fhPPR[0], fhO[0], fontDefType, fontDefStyle,
                                 fontDefItalic, fontDefSize, down2Coord, across2Coord, oBuf, barcodeFileName, gstRates);
            ++ lineCount[0];

            llCount = generalUtils.getListEntryByNumAndValue(llCount, multipleLinesCount[0], lineNumB, multipleLinesData, data2);
            if(llCount == -1)
            {
              quit1 = true; // on last rec
            }
            else
            {
              if(   ! generalUtils.match(code, generalUtils.dfsAsStr(data2, (short)1))
                 || ! generalUtils.match(lineNumB, generalUtils.dfsAsStr(data2, (short)2)) ) // stepped on
              {
                quit1 = true;
              }

              if(lineCount[0] >= docSizeMax)
              {
                b[0] = '\000';
                reportGenDetails.outputLine('E', b, data, fldNames, (short)78, ' ', dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0], pageSizeWidth[0],
                         pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName, userFontStyle,
                         userFontItalic, userFontSize, fhPPR[0], fhO[0], fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord, across2Coord,
                         oBuf, gstRates);
                ++numPages;
                lineCount[0] = 1;
              }
            }
          }
        }

        if(miscDefinitions.showCustItemCodeAfterDescription(con, stmt, rs))
        {
          // output the custitemcode (if it exists)
          if(custItemCode[0] != '\000')
          {
            prependPretext("custitemcode", custItemCode, localDefnsDir, defnsDir);
            generalUtils.repAlpha(data, 4000, (short)17, custItemCode); // desc
            b[0] = '\000';
            generalUtils.repAlpha(data, 4000, (short)15, b); // linenum
            generalUtils.repAlpha(data, 4000, (short)16, b); // itemCode
            generalUtils.repAlpha(data, 4000, (short)18, b); // unitprice
            generalUtils.repAlpha(data, 4000, (short)19, b); // qty
            generalUtils.repAlpha(data, 4000, (short)20, b); // discount%
            generalUtils.repAlpha(data, 4000, (short)21, b); // amt
            generalUtils.repAlpha(data, 4000, (short)22, b); // gstrate
            generalUtils.repAlpha(data, 4000, (short)23, b); // uom
            generalUtils.repAlpha(data, 4000, (short)24, b); // amt2
            generalUtils.repAlpha(data, 4000, (short)26, b); // serialnum
            generalUtils.repAlpha(data, 4000, (short)27, b); // qtyreqd
            generalUtils.repAlpha(data, 4000, (short)28, b); // socode
            generalUtils.repAlpha(data, 4000, (short)29, b); // grossweight
            generalUtils.repAlpha(data, 4000, (short)30, b); // weightper
            generalUtils.repAlpha(data, 4000, (short)58, b); // quantitypacked
            generalUtils.repAlpha(data, 4000, (short)59, b); // instruction
            generalUtils.repAlpha(data, 4000, (short)60, b); // custitemcode
            generalUtils.repAlpha(data, 4000, (short)61, b); // lotnum
            generalUtils.repAlpha(data, 4000, (short)69, b); // store1
            generalUtils.repAlpha(data, 4000, (short)70, b); // store2
            generalUtils.repAlpha(data, 4000, (short)71, b); // netweight
            generalUtils.repAlpha(data, 4000, (short)73, b); // mfr
            generalUtils.repAlpha(data, 4000, (short)74, b); // mfrCode

            reportGenDetails.processSection("BL1", "PH", "PF", data, fldNames, (short)78, dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0], pageSizeWidth[0],
                         pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName, userFontStyle,
                         userFontItalic, userFontSize, fhPPR[0], fhO[0], fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord, across2Coord,
                         oBuf, barcodeFileName, gstRates);
            ++lineCount[0];
            if(lineCount[0] >= docSizeMax)
            {
              b[0] = '\000';
              reportGenDetails.outputLine('E', b, data, fldNames, (short)78, ' ', dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0], pageSizeWidth[0],
                         pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName, userFontStyle,
                         userFontItalic, userFontSize, fhPPR[0], fhO[0], fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord, across2Coord,
                         oBuf, gstRates);
              ++numPages;
              lineCount[0] = 1;
            }
          }
        }

        if(miscDefinitions.drawHorizontalLineOnPickingList(con, stmt, rs))
        {
              reportGenDetails.processSection("BL4", "PH", "PF", data, fldNames, (short)78, dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0],
                                   pageSizeWidth[0], pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF,
                                   userFontName, userFontStyle, userFontItalic, userFontSize, fhPPR[0], fhO[0], fontDefType, fontDefStyle,
                                   fontDefItalic, fontDefSize, down2Coord, across2Coord, oBuf, barcodeFileName, gstRates);
        }
      }
    }

    pickingList.getRecGivenCode(con, stmt, rs, code, '\000', tmp, dnm, localDefnsDir, defnsDir, bytesOut);

    generalUtils.dfs(tmp, (short)11, b);
    int x=0,  y=0;
    while(b[x] != '\001' && b[x] != '\000')
      b2[y++] = b[x++];
    b2[y] = '\000';
    generalUtils.repAlpha(data, 4000, (short)54, b2);

    reportGenDetails.processSection("BL2", "PH", "PF", data, fldNames, (short)78, dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0], pageSizeWidth[0],
                         pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName, userFontStyle,
                         userFontItalic, userFontSize, fhPPR[0], fhO[0], fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord, across2Coord,
                         oBuf, barcodeFileName, gstRates);

    // any other note lines
    if(b[x] == '\001')
      ++x; // lf
    while(b[x] != '\000')
    {
      y=0;
      while(b[x] != '\001' && b[x] != '\000')
	     b2[y++] = b[x++];
      b2[y] = '\000';
      generalUtils.repAlpha(data, 4000, (short)55, b2);

      reportGenDetails.processSection("BL3", "PH", "PF", data, fldNames, (short)78, dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0], pageSizeWidth[0],
                         pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName, userFontStyle,
                         userFontItalic, userFontSize, fhPPR[0], fhO[0], fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord, across2Coord,
                         oBuf, barcodeFileName, gstRates);

      if(b[x] == '\001')
	      ++x; // lf
    }

    //?  if(! Other->bLastOperationPF)
    reportGenDetails.processSection("PF", "PH", "PF", data, fldNames, (short)78, dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0], pageSizeWidth[0],
                         pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF, userFontName, userFontStyle,
                         userFontItalic, userFontSize, fhPPR[0], fhO[0], fontDefType, fontDefStyle, fontDefItalic, fontDefSize, down2Coord, across2Coord,
                         oBuf, barcodeFileName, gstRates);

    fhO[0].write(oBuf, 0, oPtr[0]);

    generalUtils.fileClose(fhO[0]);
    generalUtils.fileClose(fhPPR[0]);

    PrintingLayout printingLayout = new PrintingLayout();
    printingLayout.updateNumPages("0.000", reportsDir);

    return numPages;
  }

  // ------------------------------------------------------------------------------------------------------------------------------------------------------
  private void prependPretext(String which, byte[] b, String localDefnsDir, String defnsDir) throws Exception
  {
    String s = generalUtils.getFromDefnFile("picklist-" + which, "pretext.dfn", localDefnsDir, defnsDir);
    int len = s.length();
    if(len == 0)
      return;

    byte[] b2 = new byte[100];
    int x=0;

    while(x < len)
    {
      b2[x] = (byte)s.charAt(x);
      ++x;
    }

    b2[x++] = ' ';
 
    int y=0;
    while(b[y] != '\000')
      b2[x++] = b[y++];
    b2[x] = '\000';

    x=y=0;
    while(b2[y] != '\000')
      b[x++] = b2[y++];
    b[x] = '\000';
  }
    
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private int outputDetailLine(Connection con, Statement stmt, Statement stmt2, ResultSet rs, boolean prependingFirstCall, boolean notPrepending, byte[] data,
                               byte[] fldNames, byte[] lineNumB, byte[] entry,
                               byte[] itemCode, byte[] mfrCodeOrDesc, byte[] unitPrice, byte[] serialNumber, byte[] qtyReqd, byte[] soCode,
                               byte[] weightPer, byte[] grossWeight, byte[] qtyPacked, byte[] instruction, byte[] lotNum, byte[] invoiceCode,
                               byte[] netWeight, byte[] mfr, byte[] store, byte[] custItemCode, byte[] uom, byte[] amount, byte[] amount2,
                               byte[] gstRate, byte[] discountPercentage, byte[] qty, int[] lineCount, char dpOnQuantities,
                               boolean convertToUpperCase, int numPages, int docSizeMax, String unm, String sid, String uty, String men,
                               String den, String dnm, String bnm, String localDefnsDir, String defnsDir, double[] tm, double[] bm, double[] lm,
                               double[] rm, double[] pageSizeWidth, double[] pageSizeLength, double[] currDown, double[] currAcross, short[] currFont,
                               short[] currPage, short[] oPtr, short oBufLen, boolean[] lastOperationPF, String[] userFontName,
                               String[] userFontStyle, String[] userFontItalic, String[] userFontSize, RandomAccessFile[] fhPPR,
                               RandomAccessFile[] fhO, char[] fontDefType, char[] fontDefStyle, char[] fontDefItalic, int[] fontDefSize,
                               double[] down2Coord, double[] across2Coord, byte[] oBuf, String barcodeFileName, String[] gstRates) throws Exception
  {
    byte[] b = new byte[100];
    double[] levels = new double[2];
    
    String location = inventory.getLocationGivenItemCodeAndStore(con, stmt, rs, generalUtils.stringFromBytes(itemCode, 0L), "TanjongPenjuru");//generalUtils.stringFromBytes(store, 0L));
    generalUtils.repAlpha(data, 4000, (short)77, location);

    if(prependingFirstCall || notPrepending)
    {
      if(miscDefinitions.useStoreAndQtyNotTwoStores(con, stmt, rs))
      {
        generalUtils.repAlpha(data, 4000, (short)69, store); // store1 fld (use for storename)

        // get store quantities for the item for this store
        levels[0] = inventory.stockLevelForAStore(con, stmt, stmt2, rs, "TanjongPenjuru", generalUtils.stringFromBytes(itemCode, 0L), "", unm, sid, uty, men, den, dnm, bnm);

        generalUtils.doubleToBytesCharFormat(levels[0], b, 0);
        generalUtils.bytesDPsGivenSeparator(true, '\000', dpOnQuantities, b, 20, 0);
        generalUtils.formatNumeric(b, dpOnQuantities);
        generalUtils.repAlpha(data, 4000, (short)70, b); // store2 fld (use for stock level for the store)
      }
      else
      {
        // get store quantities for the item
        generalUtils.doubleToBytesCharFormat(levels[0], b, 0);
        generalUtils.bytesDPsGivenSeparator(true, '\000', dpOnQuantities, b, 20, 0);
        generalUtils.formatNumeric(b, dpOnQuantities);
        generalUtils.repAlpha(data, 4000, (short)69, b); // store1

        generalUtils.doubleToBytesCharFormat(levels[1], b, 0);
        generalUtils.bytesDPsGivenSeparator(true, '\000', dpOnQuantities, b, 20, 0);
        generalUtils.formatNumeric(b, dpOnQuantities);
        generalUtils.repAlpha(data, 4000, (short)70, b); // store2
      }      
    }
    else
    {
      generalUtils.repAlpha(data, 4000, (short)69, ""); // store1
      generalUtils.repAlpha(data, 4000, (short)70, ""); // store2      
    }

   generalUtils.repAlpha(data, 4000, (short)15, entry);

   if(convertToUpperCase) generalUtils.toUpper(itemCode, 0);
   generalUtils.repAlpha(data, 4000, (short)16, itemCode);

   if(convertToUpperCase) generalUtils.toUpper(mfrCodeOrDesc, 0);
   generalUtils.repAlpha(data, 4000, (short)17, mfrCodeOrDesc); // desc

   if(convertToUpperCase) generalUtils.toUpper(unitPrice, 0);
   generalUtils.repAlpha(data, 4000, (short)18, unitPrice); // unitprice

   if(qty[0] != '\000')
   {
     generalUtils.bytesToBytes(b, 0, qty, 0);
     generalUtils.bytesDPsGivenSeparator(true, '\000', dpOnQuantities, b, 20, 0);
     generalUtils.formatNumeric(b, dpOnQuantities);
     generalUtils.repAlpha(data, 4000, (short)19, b); // qty
   }
   else generalUtils.repAlpha(data, 4000, (short)19, ""); // qty

   if(convertToUpperCase) generalUtils.toUpper(discountPercentage, 0);
   generalUtils.repAlpha(data, 4000, (short)20, discountPercentage); // discount%

   if(convertToUpperCase) generalUtils.toUpper(amount, 0);
   generalUtils.repAlpha(data, 4000, (short)21, amount); // amt

   if(convertToUpperCase) generalUtils.toUpper(gstRate, 0);
   generalUtils.toUpper(b, 0); generalUtils.repAlpha(data, 4000, (short)22, gstRate); // gstrate

   if(convertToUpperCase) generalUtils.toUpper(uom, 0);
   generalUtils.repAlpha(data, 4000, (short)23, uom); // uom

   if(convertToUpperCase) generalUtils.toUpper(amount2, 0);
   generalUtils.repAlpha(data, 4000, (short)24, amount2); // amount2

   if(convertToUpperCase) generalUtils.toUpper(serialNumber, 0);
   generalUtils.repAlpha(data, 4000, (short)26, serialNumber); // serialnum

   if(qtyReqd[0] != '\000')
   {
     generalUtils.bytesToBytes(b, 0, qtyReqd, 0);
     generalUtils.bytesDPsGivenSeparator(true, '\000', dpOnQuantities, b, 20, 0);
     generalUtils.formatNumeric(b, dpOnQuantities);
     generalUtils.repAlpha(data, 4000, (short)27, b); // qtyreqd
   }
   else generalUtils.repAlpha(data, 4000, (short)27, ""); // qtyreqd
        
   if(convertToUpperCase) generalUtils.toUpper(soCode, 0);
   generalUtils.repAlpha(data, 4000, (short)28, soCode); // socode

   if(convertToUpperCase) generalUtils.toUpper(grossWeight, 0);
   generalUtils.repAlpha(data, 4000, (short)29, grossWeight); // grossweight

   if(convertToUpperCase) generalUtils.toUpper(weightPer, 0);
   generalUtils.repAlpha(data, 4000, (short)30, weightPer); // weightper

   generalUtils.repAlpha(data, 4000, (short)58, qtyPacked); // quantitypacked

   if(convertToUpperCase) generalUtils.toUpper(instruction, 0);
   generalUtils.repAlpha(data, 4000, (short)59, instruction); // instruction

   generalUtils.toUpper(custItemCode, 0);
   if(generalUtils.match(custItemCode, "NONE"))
     custItemCode[0] = '\000';
   generalUtils.repAlpha(data, 4000, (short)60, custItemCode);

   if(convertToUpperCase) generalUtils.toUpper(lotNum, 0);
   generalUtils.repAlpha(data, 4000, (short)61, lotNum); // lotnum

   if(convertToUpperCase) generalUtils.toUpper(invoiceCode, 0);
   generalUtils.putAlpha(data, 4000, (short)68, invoiceCode); // invoicecode

   if(convertToUpperCase) generalUtils.toUpper(netWeight, 0);
   generalUtils.putAlpha(data, 4000, (short)71, netWeight); // netweight

   if(convertToUpperCase) generalUtils.toUpper(mfr, 0);
   generalUtils.putAlpha(data, 4000, (short)73, mfr); // mfr

   if(convertToUpperCase) generalUtils.toUpper(mfrCodeOrDesc, 0);
   generalUtils.putAlpha(data, 4000, (short)74, mfrCodeOrDesc); // mfrCode

   reportGenDetails.processSection("BL1", "PH", "PF", data, fldNames, (short)78, dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0],
                                   pageSizeWidth[0], pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF,
                                   userFontName, userFontStyle, userFontItalic, userFontSize, fhPPR[0], fhO[0], fontDefType, fontDefStyle,
                                   fontDefItalic, fontDefSize, down2Coord, across2Coord, oBuf, barcodeFileName, gstRates);
   ++lineCount[0];
   
            if(lineCount[0] >= docSizeMax)
            {
              b[0] = '\000';
              reportGenDetails.outputLine('E', b, data, fldNames, (short)78, ' ', dnm, unm, localDefnsDir, defnsDir, tm[0], bm[0], lm[0], rm[0],
                               pageSizeWidth[0], pageSizeLength[0], currDown, currAcross, currFont, currPage, oPtr, oBufLen, lastOperationPF,
                               userFontName, userFontStyle, userFontItalic, userFontSize, fhPPR[0], fhO[0], fontDefType, fontDefStyle, fontDefItalic,
                               fontDefSize, down2Coord, across2Coord, oBuf, gstRates);
              ++numPages;
              lineCount[0] = 1;
            }
    return numPages;
  }

}
