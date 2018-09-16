// =======================================================================================================================================================================================================
// System: ZaraStar Product: Price List Update - Perform operation
// Module: PriceListUpdatePerform.java
// Author: C.K.Harvey
// Copyright (c) 2003-08 Christopher Harvey. All Rights Reserved.
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
import java.util.*;
import java.io.*;
import java.sql.*;

public class PriceListUpdatePerform extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  DocumentUtils documentUtils = new DocumentUtils();
  LibraryUtils libraryUtils = new LibraryUtils();
  Inventory inventory = new Inventory();
  
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", fileNumber="", mfr="", purchaseCurrency="", salesCurrency="", exchangeRate="",
           ourMarkup="", dealerDiscount="", markdown1="", markdown2="", markdown3="", markdown4="";
    boolean addNew=false;

    try
    {
      res.setContentType("text/html");
      out = res.getWriter();

      String name, option="";
      String[] value;
      Enumeration en = req.getParameterNames();
      while(en.hasMoreElements())
      {
        name = (String)en.nextElement();
        value = req.getParameterValues(name);
        if(name.equals("unm"))
          unm = value[0];
        else
        if(name.equals("uty"))
          uty = value[0];
        else
        if(name.equals("sid"))
          sid = value[0];
        else
        if(name.equals("men"))
          men = value[0];
        else
        if(name.equals("den"))
          den = value[0];
        else
        if(name.equals("dnm"))
          dnm = value[0];
        else
        if(name.equals("bnm"))
          bnm = value[0];
        else
        if(name.equals("fileNumber"))
          fileNumber = value[0];
        else
        if(name.equals("mfr"))
          mfr = value[0];
        else
        if(name.equals("purchaseCurrency"))
          purchaseCurrency = value[0];
        else
        if(name.equals("salesCurrency"))
          salesCurrency = value[0];
        else
        if(name.equals("exchangeRate"))
          exchangeRate = value[0];
        else
        if(name.equals("ourMarkup"))
          ourMarkup = value[0];
        else
        if(name.equals("dealerDiscount"))
          dealerDiscount = value[0];
        else
        if(name.equals("markdown1"))
          markdown1 = value[0];
        else
        if(name.equals("markdown2"))
          markdown2 = value[0];
        else
        if(name.equals("markdown3"))
          markdown3 = value[0];
        else
        if(name.equals("markdown4"))
          markdown4 = value[0];
        else
        if(name.length() > 2 && name.substring(1, 3).equals(".x"))
        {
          option = name;
        }
        else
        if(name.length() > 2 && name.substring(1, 3).equals(".y"))
        {
          ;
        }
        else // must be checkbox value
        {
          if(name.equals("addNew"))
            addNew = true;
        }
      }

      boolean rrpInFile;
      if(   (exchangeRate == null || exchangeRate.equalsIgnoreCase("null") || exchangeRate.length() == 0)
         && (ourMarkup == null || ourMarkup.equalsIgnoreCase("null") || ourMarkup.length() == 0)
         && (dealerDiscount == null || dealerDiscount.equalsIgnoreCase("null") || dealerDiscount.length() == 0))
      {
        rrpInFile = true;
      }
      else rrpInFile = false;
      
      doIt(out, req, option, unm, sid, uty, men, den, dnm, bnm, fileNumber, mfr, purchaseCurrency, salesCurrency, exchangeRate, ourMarkup,
           dealerDiscount, markdown1, markdown2, markdown3, markdown4, rrpInFile, addNew, bytesOut);
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
 
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "PriceListUpdatePerform", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3053, bytesOut[0], 0, "ERR:" + mfr);
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String option, String unm, String sid, String uty, String men, String den, String dnm,
                    String bnm, String fileNumber, String mfr, String purchaseCurrency, String salesCurrency, String exchangeRate, String ourMarkup,
                    String dealerDiscount, String markdown1, String markdown2, String markdown3, String markdown4, boolean rrpInFile, boolean addNew,
                    int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String libraryDir       = directoryUtils.getUserDir('L', dnm, unm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
     
   Statement stmt = null;
    ResultSet rs   = null;
    
    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 3053, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "PriceListUpdatePerform", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3053, bytesOut[0], 0, "ACC:" + mfr);
       
      if(con != null) con.close();
     if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "PriceListUpdatePerform", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3053, bytesOut[0], 0, "SID:" + mfr);
       
      if(con != null) con.close();
    if(out != null) out.flush(); 
      return;
    }
    
    String fileName = libraryUtils.getDocumentNameGivenCode(fileNumber, unm, dnm, localDefnsDir, defnsDir);
    int len = libraryDir.length();
    --len; // trailing '/'
    while(len > 0 && libraryDir.charAt(len) != '/') // just-in-case
      --len;    
    String sanitisedFileName = fileName.substring(len);

    switch(option.charAt(0))
    {
      case 'V' : // view
                 viewFile(con, stmt, rs, out, req, unm, sid, uty, men, den, dnm, bnm, fileName, sanitisedFileName, localDefnsDir, defnsDir,
                          bytesOut);
                 break;
      case 'T' : // test update
      case 'U' : // update
                 if(validateFile(con, stmt, rs, out, req, rrpInFile, unm, sid, uty, men, den, dnm, bnm, fileName, sanitisedFileName, localDefnsDir, defnsDir, bytesOut))
                 {
                   scoutln(out, bytesOut, "<html><head><title>Price List Update</title></head>");

                   scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\""
                                        + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\"></head>");

                   int[] hmenuCount = new int[1];
 
                   pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "3053", "", "PriceListUpdate", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
         
   
                   pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Price List Update", "3053", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

                   scoutln(out, bytesOut, "<table id=\"page\" width=100% border=0 cellspacing=2 cellpadding=3 width=100%>");
                
                   scoutln(out, bytesOut, "<tr><td colspan=3><p>The following updates have been made from " + sanitisedFileName + ":</td></tr>");

                   importFile(con, stmt, rs, out, option, rrpInFile, unm, fileName, sanitisedFileName, mfr, purchaseCurrency, salesCurrency,
                              exchangeRate, ourMarkup, dealerDiscount, markdown1, markdown2, markdown3, markdown4, addNew, dnm, localDefnsDir,
                              defnsDir, bytesOut);
                 }
                 
                 scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
                 scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

                 scoutln(out, bytesOut, "</table></form>");     scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
                 break;
    }

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 3053, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), mfr);
      
      if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void viewFile(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty,
                        String men, String den, String dnm, String bnm, String fileName, String sanitisedFileName, String localDefnsDir,
                        String defnsDir, int[] bytesOut) throws Exception
  {
    RandomAccessFile fhi = generalUtils.fileOpen(fileName);
    if(fhi == null) // just-in-case
    {
      scoutln(out, bytesOut, "File Not Found: " + fileName);
      return;
    }

    byte[] tmp = new byte[2000]; // plenty
    
    scoutln(out, bytesOut, "<html><head><title>Price List Update</title></head>");

    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\""
                         + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\"></head>");

    int[] hmenuCount = new int[1];
 
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "3053", "", "PriceListUpdate", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount,
                          bytesOut);
         
    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Price List Update", "3053", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<table id=\"page\" width=100% border=0 cellspacing=2 cellpadding=3 width=100%>");
    
    scoutln(out, bytesOut, "<tr><td colspan=3><p>This is " + sanitisedFileName + ":</td></tr>");

    while(getNextLine(fhi, tmp, 2000))
      scoutln(out, bytesOut, "<tr><td colspan=3><p>" + generalUtils.stringFromBytes(tmp, 0L) + "</td></tr>");

    fhi.close();
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "</table></form>");     scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean validateFile(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, boolean sellPrice1InFile,
                               String unm, String sid, String uty, String men, String den, String dnm, String bnm, String fileName,
                               String sanitisedFileName, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    RandomAccessFile fhi = generalUtils.fileOpen(fileName);
    if(fhi == null) // just-in-case
    {
      scoutln(out, bytesOut, "File Not Found: " + fileName);
      return false;
    }

    byte[] tmp      = new byte[2000]; // plenty
    byte[] fldEntry = new byte[1000]; // plenty
    int y, x;

    boolean first=true, atLeastOneError = false;
    long count = 1;
    int res;
    String msg="";
    
    int maxFields=4;
    if(sellPrice1InFile)
      ++maxFields;  

    while(getNextLine(fhi, tmp, 2000))
    {
      for(x=0;x<maxFields;++x)
      {
        res = getEntry(tmp, x, fldEntry);

        if(res < 0)
          msg = "Line not legal"; // catchall error msg

        if(res == -1) // no entry
        {
          if(x == 2) // desc
            res = 0; // not an error
          else
          if(x == 3) // desc2
            res = 0; // not an error
        }

        if(res == -2) // no closing
          msg = "No closing quotes";

        if(res == 0) // no error
        {
          if(x == 0) // mfr code
          {
            y=0;
            while(fldEntry[y] != '\000')
            {
              if(fldEntry[y++] == ' ')
                res = -999;
            }

            if(res == -999)
              msg = "Embedded spaces not allowed in Code";
          }
          else
          if(x == 1) // purchase price
          {
            if(! generalUtils.isNumeric(fldEntry))
            {
              res = -998;
              msg = "Non-numeric Purchase Price";
            }
          }
          else
          if(x == 2) // desc
          {
            if(generalUtils.lengthBytes(fldEntry, 0) > 80)
            {
              res = -998;
              msg = "Description Longer Than 80 Characters";
            }
          }
          else
          if(x == 3) // desc2
          {
            if(generalUtils.lengthBytes(fldEntry, 0) > 80)
            {
              res = -998;
              msg = "Description2 Longer Than 80 Characters";
            }
          }
          else
          if(x == 4) // sellPrice1 (maybe)
          {
            if(! generalUtils.isNumeric(fldEntry))
            {
              res = -998;
              msg = "Non-numeric Sell Price 1";
            }
          }
        }

        if(res < 0)
        {
          if(first)
          {
            scoutln(out, bytesOut, "<html><head><title>Price List Update</title></head>");

            scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\""
                                 + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\"></head>");

            int[] hmenuCount = new int[1];
 
            pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "3053", "", "PriceListUpdate", unm, sid, uty, men, den, dnm, bnm, localDefnsDir,
                                  defnsDir, hmenuCount, bytesOut);
 
            pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Price List Update", "3053", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

            scoutln(out, bytesOut, "<table id=\"page\" width=100% border=0 cellspacing=2 cellpadding=3 width=100%>");
    
            scoutln(out, bytesOut, "<tr><td colspan=3><span id=\"textRedHighlighting\">The following errors have been found in " + sanitisedFileName
                                 + ":</span></td></tr>");

            first = false;
          }

          scoutln(out, bytesOut, "<tr><td colspan=3><p>" + msg + " [Line " + count + "] " + generalUtils.stringFromBytes(tmp, 0L) + "</td></tr>");

          atLeastOneError = true;
        }
      }

      System.out.print(" " + count);
      ++count;
    }

    fhi.close();

    System.out.println(" Validation Completed");

    if(atLeastOneError)
      return false;

    return true;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void importFile(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String option, boolean rrpInFile, String unm,
                          String fileName, String sanitisedFileName, String mfr, String purchaseCurrency, String salesCurrency, String exchangeRate,
                          String ourMarkup, String dealerDiscount, String markdown1, String markdown2, String markdown3, String markdown4,
                          boolean addNew, String dnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    RandomAccessFile fhi = generalUtils.fileOpen(fileName);
    if(fhi == null) // just-in-case
    {
      scoutln(out, bytesOut, "<tr><td>File Not Found: " + sanitisedFileName + "</td></tr>");
      return;
    }

    byte[] tmp           = new byte[2000]; // plenty
    byte[] mfrCode       = new byte[100];
    byte[] purchasePrice = new byte[30];
    byte[] desc          = new byte[200];
    byte[] desc2         = new byte[200];
    byte[] rrpFromFile   = new byte[200];
    
    int[] updatedCount  = new int[1]; updatedCount[0]  = 0;
    int[] addedCount    = new int[1]; addedCount[0]    = 0;
    int[] unknownCount = new int[1]; unknownCount[0] = 0;
    int[] errorCount    = new int[1]; errorCount[0]    = 0;

    while(getNextLine(fhi, tmp, 2000))
    {
      getEntry(tmp, 0, mfrCode);
      getEntry(tmp, 1, purchasePrice);
      getEntry(tmp, 2, desc);
      getEntry(tmp, 3, desc2);
      if(rrpInFile)
        getEntry(tmp, 4, rrpFromFile);          

      update(con, stmt, rs, out, option, rrpInFile, unm, mfrCode, purchasePrice, desc, desc2, mfr, purchaseCurrency, salesCurrency, exchangeRate,
             ourMarkup, dealerDiscount, markdown1, markdown2, markdown3, markdown4, addNew, localDefnsDir, defnsDir, updatedCount, unknownCount,
             errorCount, addedCount, rrpFromFile, dnm, bytesOut);
    }

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td><p>Records Updated: " + updatedCount[0] + "</td></tr>");

    if(! addNew)
      scoutln(out, bytesOut, "<tr><td><p>Unknown Records: " + unknownCount[0] + "</td></tr>");
    else scoutln(out, bytesOut, "<tr><td><p>Records Added: " + addedCount[0] + "</td></tr>");

    scoutln(out, bytesOut, "<tr><td><p>Errors: " + errorCount[0]    + "</td></tr>");

    if(option.charAt(0) == 'T') // test
    {
      scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
      scoutln(out, bytesOut, "<tr><td><span id=\"textRedHighlighting\"><font size=3><b>No records were actually updated</b></font></span></td></tr>");
    }

    fhi.close();
    System.out.println(" Update Completed");
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void update(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String option, boolean rrpInFile, String unm, byte[] mfrCodeB,
                      byte[] purchasePrice, byte[] desc, byte[] desc2, String mfr, String purchaseCurrency, String salesCurrency, String exchangeRate,
                      String ourMarkup, String dealerDiscount, String markdown1, String markdown2, String markdown3, String markdown4, boolean addNew,
                      String localDefnsDir, String defnsDir, int[] updatedCount, int[] unknownCount, int[] errorCount, int[] addedCount,
                      byte[] rrpFromFile, String dnm, int[] bytesOut) throws Exception
  {
    byte[] data     = new byte[3000];
    byte[] itemCode = new byte[21];

    if(mfrCodeB[0] == '\000') // just-in-case
      return;

    byte[] mfrB = new byte[51];
    generalUtils.strToBytes(mfrB, mfr);

    double d;

    double existingPurchasePrice, rrpD, sellPrice1D, sellPrice2D, sellPrice3D, sellPrice4D, ppToRRPFactorD;

    if(rrpInFile)
    {
      rrpD = generalUtils.doubleFromBytesCharFormat(rrpFromFile, 0); 
        
      sellPrice1D  = rrpD - (rrpD * (generalUtils.doubleFromStr(markdown1) / 100));
      sellPrice2D  = rrpD - (rrpD * (generalUtils.doubleFromStr(markdown2) / 100));
      sellPrice3D  = rrpD - (rrpD * (generalUtils.doubleFromStr(markdown3) / 100));
      sellPrice4D  = rrpD - (rrpD * (generalUtils.doubleFromStr(markdown4) / 100));
     
      d = generalUtils.doubleFromBytesCharFormat(purchasePrice, 0);
      if(d == 0)
        d = 1;
      ppToRRPFactorD = rrpD / d;
      
      exchangeRate = "0.0";
    }
    else
    {
      rrpD  = generalUtils.doubleFromBytesCharFormat(purchasePrice, 0) * generalUtils.doubleFromStr(exchangeRate);
    
      double rrpBefore = rrpD;

      double factor = (100.0 / ((100.0 - generalUtils.doubleFromStr(ourMarkup)) / 100.0)) / 100.0;
      rrpD *= factor;

      factor = (100.0 / ((100.0 - generalUtils.doubleFromStr(dealerDiscount)) / 100.0)) / 100.0;
      rrpD *= factor;
      
      sellPrice1D  = rrpD - (rrpD * (generalUtils.doubleFromStr(markdown1) / 100));
      sellPrice2D  = rrpD - (rrpD * (generalUtils.doubleFromStr(markdown2) / 100));
      sellPrice3D  = rrpD - (rrpD * (generalUtils.doubleFromStr(markdown3) / 100));
      sellPrice4D  = rrpD - (rrpD * (generalUtils.doubleFromStr(markdown4) / 100));

      if(rrpBefore == 0.0)
        ppToRRPFactorD = 0.0;
      else ppToRRPFactorD = rrpD / rrpBefore;
    }

    String s, mfrCode = generalUtils.stringFromBytes(mfrCodeB, 0L);
    
    if(inventory.getStockRecGivenMfrAndMfrCode(con, stmt, rs, mfr, mfrCode, '\000', dnm, data, localDefnsDir, defnsDir) != -1)
    {
      generalUtils.dfs(data, (short)0, itemCode);

      existingPurchasePrice = generalUtils.dfsAsDouble(data, (short)18);

      if(generalUtils.lengthBytes(desc, 0) > 0)
        generalUtils.repAlpha(data, 3000, (short)1, desc);
  
      if(generalUtils.lengthBytes(desc2, 0) > 0)
        generalUtils.repAlpha(data, 3000, (short)2, desc2);

      generalUtils.repAlpha(data, 3000, (short)25, generalUtils.doubleToStr(rrpD));

      generalUtils.repAlpha(data, 3000, (short)20, generalUtils.doubleToStr(sellPrice1D));
      generalUtils.repAlpha(data, 3000, (short)21, generalUtils.doubleToStr(sellPrice2D));
      generalUtils.repAlpha(data, 3000, (short)22, generalUtils.doubleToStr(sellPrice3D));
      generalUtils.repAlpha(data, 3000, (short)23, generalUtils.doubleToStr(sellPrice4D));

      generalUtils.repAlpha(data, 3000, (short)19, purchaseCurrency);
      generalUtils.repAlpha(data, 3000, (short)52, salesCurrency);

      generalUtils.repAlpha(data, 3000, (short)24, exchangeRate);
      generalUtils.repAlpha(data, 3000, (short)18, purchasePrice);

      generalUtils.repAlpha(data, 3000, (short)26, generalUtils.doubleToStr(ppToRRPFactorD));
      generalUtils.repAlpha(data, 3000, (short)38, markdown1);
      generalUtils.repAlpha(data, 3000, (short)39, markdown2);
      generalUtils.repAlpha(data, 3000, (short)40, markdown3);
      generalUtils.repAlpha(data, 3000, (short)41, markdown4);

      generalUtils.repAlpha(data, 3000, (short)34, unm);

      generalUtils.repAlpha(data, 3000, (short)50, generalUtils.todaySQLFormat(localDefnsDir, defnsDir));

      scoutln(out, bytesOut, "<tr><td><p>Updating: " + mfr + " " + generalUtils.stringFromBytes(mfrCodeB, 0L));
      scoutln(out, bytesOut, " (was " + generalUtils.doubleToStr(true, '8', existingPurchasePrice) + ") with "
                             + generalUtils.doubleToStr(true, '8', generalUtils.doubleFromBytesCharFormat(purchasePrice, 0)) + " List: "
                             + generalUtils.doubleToStr(true, '8', rrpD) + " 1: "
                             + generalUtils.doubleToStr(true, '8', sellPrice1D) + " 2: " + generalUtils.doubleToStr(true, '8', sellPrice2D)
                             + " 3: " + generalUtils.doubleToStr(true, '8', sellPrice3D) + " 4: "
                             + generalUtils.doubleToStr(true, '8', sellPrice4D) + "</td></tr>");

      if(option.charAt(0) == 'T') // test
        ;
      else
      {
        if(! inventory.putStockRecGivenCode(con, stmt, rs, '\000', itemCode, 'E', data, dnm, localDefnsDir, defnsDir))
        {
          scoutln(out, bytesOut, "<tr><td><span id=\"textRedHighlighting\">ERROR: FAILED TO UPDATE: " + generalUtils.stringFromBytes(mfrCodeB, 0) + "</span></td></tr>");
          ++errorCount[0];
        }
        else ++updatedCount[0];
      }
    }
    else // not found
    {
      if(addNew)
      {
        if(option.charAt(0) == 'T') // test
          documentUtils.getNextCode(con, stmt, rs, "stock", false, itemCode);
        else documentUtils.getNextCode(con, stmt, rs, "stock", true, itemCode);

        generalUtils.zeroize(data, 3000);
        
        generalUtils.putAlpha(data, 3000, (short)0, itemCode);

        generalUtils.putAlpha(data, 3000, (short)1, desc);
        generalUtils.putAlpha(data, 3000, (short)2, desc2);
        
        generalUtils.putAlpha(data, 3000, (short)3, mfr);
        generalUtils.putAlpha(data, 3000, (short)4, mfrCode);
        
        generalUtils.putAlpha(data, 3000, (short)25, generalUtils.doubleToStr(rrpD));

        generalUtils.putAlpha(data, 3000, (short)20, generalUtils.doubleToStr(sellPrice1D));
        generalUtils.putAlpha(data, 3000, (short)21, generalUtils.doubleToStr(sellPrice2D));
        generalUtils.putAlpha(data, 3000, (short)22, generalUtils.doubleToStr(sellPrice3D));
        generalUtils.putAlpha(data, 3000, (short)23, generalUtils.doubleToStr(sellPrice4D));

        generalUtils.putAlpha(data, 3000, (short)19, purchaseCurrency);
        generalUtils.putAlpha(data, 3000, (short)52, salesCurrency);

        generalUtils.putAlpha(data, 3000, (short)24, exchangeRate);
        generalUtils.putAlpha(data, 3000, (short)18, purchasePrice);

        generalUtils.putAlpha(data, 3000, (short)26, generalUtils.doubleToStr(ppToRRPFactorD));
        generalUtils.putAlpha(data, 3000, (short)38, markdown1);
        generalUtils.putAlpha(data, 3000, (short)39, markdown2);
        generalUtils.putAlpha(data, 3000, (short)40, markdown3);
        generalUtils.putAlpha(data, 3000, (short)41, markdown4);

        generalUtils.putAlpha(data, 3000, (short)34, "");//unm);

        generalUtils.putAlpha(data, 3000, (short)32, "1970-01-01");
        generalUtils.putAlpha(data, 3000, (short)33, "1970-01-01");
        generalUtils.putAlpha(data, 3000, (short)45, "1970-01-01");
        generalUtils.putAlpha(data, 3000, (short)58, "1970-01-01");

        generalUtils.putAlpha(data, 3000, (short)14, "0");
        generalUtils.putAlpha(data, 3000, (short)15, "0");
        generalUtils.putAlpha(data, 3000, (short)16, "0");
        generalUtils.putAlpha(data, 3000, (short)31, "0");
        generalUtils.putAlpha(data, 3000, (short)44, "0");
        generalUtils.putAlpha(data, 3000, (short)46, "0");
        generalUtils.putAlpha(data, 3000, (short)47, "0");
        generalUtils.putAlpha(data, 3000, (short)48, "0");
        generalUtils.putAlpha(data, 3000, (short)55, "0");
        generalUtils.putAlpha(data, 3000, (short)56, "0");
        generalUtils.putAlpha(data, 3000, (short)57, "0"); // ClosingLevel decimal
        generalUtils.putAlpha(data, 3000, (short)61, "0"); // productWaveID

        generalUtils.putAlpha(data, 3000, (short)50, generalUtils.todaySQLFormat(localDefnsDir, defnsDir));

        scoutln(out, bytesOut, "<tr><td><p>Adding: " + mfr + " " + generalUtils.stringFromBytes(mfrCodeB, 0L));
        scoutln(out, bytesOut, "Purchase Price: " + generalUtils.doubleToStr(true, '8', generalUtils.doubleFromBytesCharFormat(purchasePrice, 0)) + " List: "
                             + generalUtils.doubleToStr(true, '8', rrpD) + " 1: " + generalUtils.doubleToStr(true, '8', sellPrice1D) + " 2: "
                             + generalUtils.doubleToStr(true, '8', sellPrice2D) + " 3: " + generalUtils.doubleToStr(true, '8', sellPrice3D) + " 4: "
                             + generalUtils.doubleToStr(true, '8', sellPrice4D) + "</td></tr>");

        if(option.charAt(0) == 'T') // test
          ;
        else
        {
          if(! inventory.putStockRecGivenCode(con, stmt, rs, '\000', itemCode, 'N', data, dnm, localDefnsDir, defnsDir))
          {
             scoutln(out, bytesOut, "<tr><td><span id=\"textRedHighlighting\">ERROR: FAILED TO ADD: " + generalUtils.stringFromBytes(mfrCodeB, 0) + "</span></td></tr>");
             ++errorCount[0];
          }
          else ++addedCount[0];
        }
      }
      else
      {
        scoutln(out, bytesOut, "<tr><td><p>*** Not found: " + mfr + " " + generalUtils.stringFromBytes(mfrCodeB, 0L) + " " + generalUtils.stringFromBytes(desc, 0L)
                             + " " + generalUtils.stringFromBytes(desc2, 0L) + "</td></tr>");
        ++unknownCount[0];
      }
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean getNextLine(RandomAccessFile fh, byte[] buf, int bufSize) throws Exception
  {
    int x=0, red=0;

    long curr = fh.getFilePointer();
    long high = fh.length();

    fh.seek(curr);

    if(curr == high)
      return false;

    fh.read(buf, 0, 1);
    while(curr < high && x < (bufSize - 1))
    {
      if((buf[x] == 10 || buf[x] == 13 || buf[x] == 26))
      {
        while(buf[x] == 10 || buf[x] == 13 || buf[x] == 26)
        {
          red = fh.read(buf, x, 1);
          if(red < 0)
            break;
        }

        if(buf[x] == 26)
          ;
        else
          if(red > 0)
            fh.seek(fh.getFilePointer() -1);

        buf[x] = '\000';

        return true;
      }

      ++x;
      fh.read(buf, x, 1);
      ++curr;
    }

    // remove trailing spaces
    x = bufSize - 1;
    while(buf[x] == 32)
      --x;

    if(x == (bufSize -1))
      buf[x] = '\000';
    else buf[++x] = '\000';

    return true;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // entryReqd is origin-0
  // rtns: -1 no entry
  //       -2 no closing
  private int getEntry(byte[] line, int entryReqd, byte[] entry)
  {
    int x=0, commaCount=0;

    while(commaCount < entryReqd)
    {
      if(line[x] == '"')
      {
        ++x; // "
        while(line[x] != '\000' && line[x] != '"') // scan for the closing quotes
          ++x;

        if(line[x] == '\000') // no closing
          return -2;

        ++x; // "

        if(line[x] != '\000')
          ++x; // ,
      }
      else
      {
        while(line[x] != '\000' && line[x] != ',')
          ++x;

        if(line[x] == '\000') // no entry
        {
          entry[0] = '\000';
          return -1;
        }

        if(line[x] != '\000')
          ++x; // ,
      }

      ++commaCount;
    }

    if(line[x] == '\000') // no entry exists
    {
      entry[0] = '\000';
      return -1;
    }

    int y=0;
    if(line[x] == '"')
    {
      ++x; // "
      while(line[x] != '\000' && line[x] != '"') // scan for the closing quotes
        entry[y++] = line[x++];
      entry[y] = '\000';

      if(line[x] == '\000') // no closing
        return -2;

      return 0;
    }
    // else
    
    while(line[x] != '\000' && line[x] != ',')
      entry[y++] = line[x++];
    entry[y] = '\000';

    if(y == 0) // no entry
      return -1;

    return 0;    
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
