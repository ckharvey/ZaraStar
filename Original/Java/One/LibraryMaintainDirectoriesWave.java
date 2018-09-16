// =======================================================================================================================================================================================================
// System: ZaraStar Library: Maintain Directories
// Module: LibraryMaintainDirectoriesWave.java
// Author: C.K.Harvey
// Copyright (c) 2006-09 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.io.*;
import java.sql.*;

public class LibraryMaintainDirectoriesWave extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  LibraryUtils libraryUtils = new LibraryUtils();

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

      byte[] userNames    = new byte[1000]; userNames[0]    = '\000';
      int[]  userNamesLen = new int[1];     userNamesLen[0] = 1000;

      int thisEntryLen, inc;
      Enumeration en = req.getParameterNames();
      while(en.hasMoreElements())
      {
        String name = (String)en.nextElement();
        String[] value = req.getParameterValues(name);
        if(name.equals("unm"))
          unm = value[0];
        else
        if(name.equals("sid"))
          sid = value[0];
        else
        if(name.equals("uty"))
          uty = value[0];
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
        if(name.equals("p1"))
          p1 = value[0];
        else
        if(name.length() > 2 && name.substring(1, 3).equals(".x")) ;
        else
        if(name.length() > 2 && name.substring(1, 3).equals(".y")) ;
        else // must be checkbox value
        {
          thisEntryLen = name.length() + 2;
          if((generalUtils.lengthBytes(userNames, 0) + thisEntryLen) >= userNamesLen[0])
          {
            byte[] tmp = new byte[userNamesLen[0]];
            System.arraycopy(userNames, 0, tmp, 0, userNamesLen[0]);
            if(thisEntryLen > 1000)
              inc = thisEntryLen;
            else inc = 1000;
            userNamesLen[0] += inc;
            userNames = new byte[userNamesLen[0]];
            System.arraycopy(tmp, 0, userNames, 0, userNamesLen[0] - inc);
          }

          generalUtils.catAsBytes(name + "\001", 0, userNames, false);
        }
      }

      doIt(out, req, p1, userNames, unm, sid, uty, men, den, dnm, bnm, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "LibraryMaintainDirectories", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 12010, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush(); 
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String p1, byte[] userNames, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(p1.equals("A") && (! authenticationUtils.verifyAccess(con, stmt, rs, req, 12010, unm, uty, dnm, localDefnsDir, defnsDir)))
    {
      messagePage.msgScreenW(out, req, 13, unm, sid, uty, men, den, dnm, bnm, "LibraryMaintainDirectories", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 12010, bytesOut[0], 0, "ACC:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreenW(out, req, 12, unm, sid, uty, men, den, dnm, bnm, "LibraryMaintainDirectories", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 12010, bytesOut[0], 0, "SID:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    boolean canInsert;
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 12008, unm, uty, dnm, localDefnsDir, defnsDir))
      canInsert = true;
    else canInsert = false;

    boolean canDelete;
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 12009, unm, uty, dnm, localDefnsDir, defnsDir))
      canDelete = true;
    else canDelete = false;

    boolean canView;
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 12000, unm, uty, dnm, localDefnsDir, defnsDir))
      canView = true;
    else canView = false;

    boolean canShowProperties;
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 12015, unm, uty, dnm, localDefnsDir, defnsDir))
      canShowProperties = true;
    else canShowProperties = false;

    scoutln(out, bytesOut, "12010\001Documents\001Document Directories\001javascript:getHTML('LibraryMaintainDirectoriesw','')\001\001Y\001\001\003");

    process(con, stmt, rs, out, req, userNames, canView, canInsert, canDelete, canShowProperties, p1, unm, sid, uty, men, den, dnm, bnm, imagesDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 12010, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void process(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, byte[] userNames, boolean canView, boolean canInsert, boolean canDelete, boolean canShowProperties, String option, String unm,
                       String sid, String uty, String men, String den, String dnm, String bnm, String imagesDir, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    scoutln(out, bytesOut, "<script javascript=\"JavaScript\">");

    if(canInsert)
    {
      scoutln(out, bytesOut, "function insertDir(dir){var p1=sanitise(dir);getHTML('LibraryInsertDirectoryw','&p2=" + option + "&p1='+p1);}");
    }

    if(canDelete)
    {
      scoutln(out, bytesOut, "function deleteDir(dir){var p1=sanitise(dir);getHTML('LibraryDeleteDirectoryw','&p2=" + option + "&p1='+p1);}");
    }

    if(canShowProperties)
    {
      scoutln(out, bytesOut, "function showProperties(dir){var p1=sanitise(dir);getHTML('LibraryPropertiesw','&p2=" + option + "&p1='+p1);}");
    }

    if(canView)
    {
      scoutln(out, bytesOut, "function list(dir,userName){var p1=sanitise(dir);getHTML('LibraryListDirectoryWave','&p2='+userName+'&p1='+p1);}");
    }

    scoutln(out, bytesOut, "function listquote(customerCode){var code=sanitise(customerCode);getHTML('QuotationListingw','&p1=3&p2=F&p3='+code+'&p4=&p5=&p6=0&p7=0&p8=D&p9=&p10=&p11='+code);}");

    scoutln(out, bytesOut, "function listso(customerCode){var code=sanitise(customerCode);getHTML('SalesOrderListingw','&p1=3&p2=F&p3='+code+'&p4=&p5=&p6=0&p7=0&p8=D&p9=&p10=&p11='+code);}");

    scoutln(out, bytesOut, "function listpl(customerCode){var code=sanitise(customerCode);getHTML('PickingListListingw','&p1=3&p2=F&p3='+code+'&p4=&p5=&p6=0&p7=0&p8=D&p9=&p10=&p11='+code);}");

    scoutln(out, bytesOut, "function listinvoice(customerCode){var code=sanitise(customerCode);getHTML('SalesInvoiceListingw','&p1=3&p2=F&p3='+code+'&p4=&p5=&p6=0&p7=0&p8=D&p9=&p10=&p11='+code);}");

    scoutln(out, bytesOut, "function listdo(customerCode){var code=sanitise(customerCode);getHTML('DeliveryOrderListingw','&p1=3&p2=F&p3='+code+'&p4=&p5=&p6=0&p7=0&p8=D&p9=&p10=&p11='+code);}");

    scoutln(out, bytesOut, "function listproforma(customerCode){var code=sanitise(customerCode);getHTML('ProformaInvoiceListingw','&p1=3&p2=F&p3='+code+'&p4=&p5=&p6=0&p7=0&p8=D&p9=&p10=&p11='+code);}");

    scoutln(out, bytesOut, "function listoc(customerCode){var code=sanitise(customerCode);getHTML('OrderConfirmationListingw','&p1=3&p2=F&p3='+code+'&p4=&p5=&p6=0&p7=0&p8=D&p9=&p10=&p11='+code);}");

    scoutln(out, bytesOut, "function listpo(customerCode){var code=sanitise(customerCode);getHTML('PurchaseOrderListingw','&p1=3&p2=F&p3='+code+'&p4=&p5=&p6=0&p7=0&p8=D&p9=&p10=&p11='+code);}");

    scoutln(out, bytesOut, "function listlp(customerCode){var code=sanitise(customerCode);getHTML('LocalPurchaseListingw','&p1=3&p2=F&p3='+code+'&p4=&p5=&p6=0&p7=0&p8=D&p9=&p10=&p11='+code);}");

    scoutln(out, bytesOut, "function listgr(customerCode){var code=sanitise(customerCode);getHTML('GoodsReceivedListingw','&p1=3&p2=F&p3='+code+'&p4=&p5=&p6=0&p7=0&p8=D&p9=&p10=&p11='+code);}");

    scoutln(out, bytesOut, "function listpinvoice(customerCode){var code=sanitise(customerCode);getHTML('PurchaseInvoiceListingw','&p1=3&p2=F&p3='+code+'&p4=&p5=&p6=0&p7=0&p8=D&p9=&p10=&p11='+code);}");

    scoutln(out, bytesOut, "function listMail(companyCode,companyType){var code=sanitise(companyCode);getHTML('MailZaraViewSearchLibraryw','&p1=&p2=&p3=&p4=&p5=&p6=&p7='+code+'&p8='+companyType+'&p9=&p10=&p11=&p12=&p13=A');}");

    scoutln(out, bytesOut, "function listFaxes(companyCode,companyType){var code=sanitise(companyCode);getHTML('FaxSearchw','&p1=&p2=&p3=&p4=&p5=&p6=&p7=&p8=&p9='+code+'&p10='+companyType+'&p11=A');}");

    scoutln(out, bytesOut, "</script>");

    if(option.charAt(0) == 'M')
    {
      libraryUtils.drawTitleW(con, stmt, rs, req, out, "Directory List", "12010", unm, uty, dnm, localDefnsDir, defnsDir, bytesOut);
    }
    else
    {
      libraryUtils.drawTitleW(con, stmt, rs, req, out, "Directory List", "12010", unm, uty, dnm, localDefnsDir, defnsDir, bytesOut);
    }

    scoutln(out, bytesOut, "<form>");

    if(option.charAt(0) == 'M') // only this user's directories
    {
      String dir;
      int directoryPrefixBitLen;
      dir = directoryUtils.getUserDir('L', dnm, unm);
      if(! generalUtils.fileExists(dir))
        generalUtils.createDir(dir);
      directoryPrefixBitLen = dir.length() - 1;

      scoutln(out, bytesOut, "<table border=0 id=\"directory\" width=100%><tr>");

      scoutln(out, bytesOut, "<td id=\"directoryCell\"><table width=100%><tr><td width=90%><p> &nbsp; " + unm + " &nbsp; </td>");
      if(canInsert)
        scoutln(out, bytesOut, "<td align=right><a href=\"javascript:insertDir('" + dir + "')\"><img src=\"" + imagesDir + "insertdirectory.gif\" border=0></td>");

      scoutln(out, bytesOut, "</tr></table></td>");

      drawTable(con, stmt, rs, out, canView, canInsert, canDelete, canShowProperties, unm, dir, 1, imagesDir, unm, dnm, localDefnsDir, defnsDir, directoryPrefixBitLen, bytesOut);
      scoutln(out, bytesOut, "</tr></table>");
    }
    else
    if(option.charAt(0) == 'R') // reference directory
    {
      String dir = directoryUtils.getReferenceLibraryDir(dnm);
      int directoryPrefixBitLen;
      if(! generalUtils.fileExists(dir))
        generalUtils.createDir(dir);
      directoryPrefixBitLen = dir.length() - 1;

      scoutln(out, bytesOut, "<table border=0 id=\"directory\" width=100%><tr>");

      scoutln(out, bytesOut, "<td id=\"directoryCell\"><table width=100%><tr><td width=90%><p> &nbsp; Reference &nbsp; </td>");
      if(canInsert)
        scoutln(out, bytesOut, "<td align=right><a href=\"javascript:insertDir('" + dir + "')\"><img src=\"" + imagesDir + "insertdirectory.gif\" border=0></td>");

      scoutln(out, bytesOut, "</tr></table></td>");

      drawTable(con, stmt, rs, out, canView, canInsert, canDelete, canShowProperties, "___REFERENCE___", dir, 1, imagesDir, unm, dnm, localDefnsDir, defnsDir, directoryPrefixBitLen, bytesOut);
      scoutln(out, bytesOut, "</tr></table>");
    }
    else
    {
      int x=0;
      String userName;

      int len = generalUtils.lengthBytes(userNames, 0);
      while(x < len)
      {
        userName="";
        while(userNames[x] != '\001' && userNames[x] != '\000')
          userName += (char)userNames[x++];

        displayAUser(con, stmt, rs, out, userName, canView, canInsert, canDelete, canShowProperties, unm, dnm, imagesDir, localDefnsDir, defnsDir, bytesOut);

        ++x;
      }
    }

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "</form>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void displayAUser(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String userName, boolean canView, boolean canInsert, boolean canDelete, boolean canShowProperties, String unm, String dnm, String imagesDir,
                            String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    String dir = directoryUtils.getUserDir('L', dnm, userName);
    if(! generalUtils.fileExists(dir))
      generalUtils.createDir(dir);
    int directoryPrefixBitLen = dir.length() - 1;

    scoutln(out, bytesOut, "<table border=0 id=\"directory\" width=100%><tr>");

    scoutln(out, bytesOut, "<td id=\"directoryCell\"><table width=100%><tr><td width=90%><p> &nbsp; " + userName + " &nbsp; </td>");
    if(canInsert)
      scoutln(out, bytesOut, "<td align=right><a href=\"javascript:insertDir('" + dir + userName + "')\"><img src=\"" + imagesDir + "insertdirectory.gif\" border=0></td>");

    scoutln(out, bytesOut, "</tr></table></td>");

    drawTable(con, stmt, rs, out, canView, canInsert, canDelete, canShowProperties, userName, dir, 1, imagesDir, unm, dnm, localDefnsDir, defnsDir, directoryPrefixBitLen, bytesOut);

    scoutln(out, bytesOut, "</tr></table>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int drawTable(Connection con, Statement stmt, ResultSet rs, PrintWriter out, boolean canView, boolean canInsert, boolean canDelete, boolean canShowProperties, String userName, String libraryDir, int depthCount, String imagesDir,
                        String unm, String dnm, String localDefnsDir, String defnsDir, int directoryPrefixBitLen, int[] bytesOut) throws Exception
  {
    String[] companyCode   = new String[1];
    String[] companyType   = new String[1];
    String[] documentsType = new String[1];

    File path = new File(libraryDir);
    String fs[] = new String[0];
    fs = path.list();

    int len = fs.length;

    generalUtils.insertionSort(fs, len);

    int dirCount=0;
    for(int x=0;x<len;++x)
    {
      if(generalUtils.isDirectory(libraryDir + fs[x]))
      {
        ++dirCount;
        if(x > 0 && dirCount > 1)
        {
          for(int y=0;y<depthCount;++y)
          {
            if(y == (depthCount - 1))
            {
              if(x == (fs.length - 1))
                scoutln(out, bytesOut, "<td valign=center><img src=\"" + imagesDir + "libraryl.gif\" border=0></td>");
              else scoutln(out, bytesOut, "<td valign=center><img src=\"" + imagesDir + "librarybarright.gif\" border=0></td>");
            }
            else
            {
              scoutln(out, bytesOut, "<td valign=center><img src=\"" + imagesDir + "librarybar.gif\" border=0></td>");
            }
          }
        }

        scoutln(out, bytesOut, "<td id=\"directoryCell\"><table width=100%><tr><td width=90%><p>" + fs[x]);

        libraryUtils.getProperties(libraryDir + fs[x], unm, dnm, localDefnsDir, defnsDir, companyCode, companyType, documentsType);

        if(documentsType[0].length() > 0)
        {
          scout(out, bytesOut, "<br>" + documentsType[0]);
          if(companyType[0].equals("C") && companyCode[0].length() > 0)
            scout(out, bytesOut, " (Customer: " + companyCode[0] + ") ");
          if(companyType[0].equals("S") && companyCode[0].length() > 0)
            scout(out, bytesOut, " (Supplier: " + companyCode[0] + ")");
          if(companyType[0].equals("O") && companyCode[0].length() > 0)
            scout(out, bytesOut, " (Organization: " + companyCode[0] + ")");
        }

        scoutln(out, bytesOut, "</td><td align=right>");

        if(canDelete)
          scoutln(out, bytesOut, "<a href=\"javascript:deleteDir('" + libraryDir + fs[x] + "')\"><img src=\"" + imagesDir + "deletedirectory.gif\" border=0>");

        if(canInsert)
          scoutln(out, bytesOut, "<a href=\"javascript:insertDir('" + libraryDir + fs[x] + "')\"><img src=\"" + imagesDir + "insertdirectory.gif\" border=0>");

        if(canShowProperties)
          scoutln(out, bytesOut, "<a href=\"javascript:showProperties('" + libraryDir + fs[x] + "')\"><img src=\"" + imagesDir + "showproperties.gif\" border=0>");

        scoutln(out, bytesOut, "</td></tr>");

        if(documentsType[0].length() == 0 || documentsType[0].equals("Documents") || documentsType[0].equals("Any"))
          listFiles(out, libraryDir, fs[x], directoryPrefixBitLen, userName, canView, bytesOut);

        if(documentsType[0].equals("Faxes") || documentsType[0].equals("Any"))
          listFaxes(out, companyCode[0], companyType[0], documentsType[0], dnm, bytesOut);

        if(documentsType[0].equals("Mail") || documentsType[0].equals("Any"))
          listMail(out, companyCode[0], companyType[0], documentsType[0], dnm, bytesOut);

        if(documentsType[0].equals("Quotations") || documentsType[0].equals("Any"))
          listDocs(con, stmt, rs, out, companyCode[0], "quotation", "quote", documentsType[0], bytesOut);

        if(documentsType[0].equals("SalesOrders") || documentsType[0].equals("Any"))
          listDocs(con, stmt, rs, out, companyCode[0], "sales order", "so", documentsType[0], bytesOut);

        if(documentsType[0].equals("PickingLists") || documentsType[0].equals("Any"))
          listDocs(con, stmt, rs, out, companyCode[0], "picking list", "pl", documentsType[0], bytesOut);

        if(documentsType[0].equals("SalesInvoices") || documentsType[0].equals("Any"))
          listDocs(con, stmt, rs, out, companyCode[0], "invoice", "invoice", documentsType[0], bytesOut);

        if(documentsType[0].equals("DeliveryOrders") || documentsType[0].equals("Any"))
          listDocs(con, stmt, rs, out, companyCode[0], "delivery order", "do", documentsType[0], bytesOut);

        if(documentsType[0].equals("ProformaInvoices") || documentsType[0].equals("Any"))
          listDocs(con, stmt, rs, out, companyCode[0], "proforma invoices", "proforma", documentsType[0], bytesOut);

        if(documentsType[0].equals("OrderConfirmations") || documentsType[0].equals("Any"))
          listDocs(con, stmt, rs, out, companyCode[0], "order confirmation", "oc", documentsType[0], bytesOut);

        if(documentsType[0].equals("PurchaseOrders") || documentsType[0].equals("Any"))
          listDocs(con, stmt, rs, out, companyCode[0], "purchase order", "po", documentsType[0], bytesOut);

        if(documentsType[0].equals("LocalRequisitions") || documentsType[0].equals("Any"))
          listDocs(con, stmt, rs, out, companyCode[0], "local requisition", "lp", documentsType[0], bytesOut);

        if(documentsType[0].equals("GoodsReceived") || documentsType[0].equals("Any"))
          listDocs(con, stmt, rs, out, companyCode[0], "goods received note", "gr", documentsType[0], bytesOut);

        if(documentsType[0].equals("PurchaseInvoices") || documentsType[0].equals("Any"))
          listDocs(con, stmt, rs, out, companyCode[0], "purchase invoice", "pinvoice", documentsType[0], bytesOut);

        scoutln(out, bytesOut, "</table>");

        if(drawTable(con, stmt, rs, out, canView, canInsert, canDelete, canShowProperties, userName, (libraryDir + fs[x] + "/"), (depthCount + 1), imagesDir, unm, dnm, localDefnsDir, defnsDir, directoryPrefixBitLen, bytesOut) == 0)
          scoutln(out, bytesOut, "</tr><tr>");
      }
    }

    return countDirectories(libraryDir);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int countFiles(String libraryDir) throws Exception
  {
    File path = new File(libraryDir);
    String fs[] = new String[0];
    fs = path.list();

    int fileCount=0;
    for(int x=0;x<fs.length;++x)
    {
      if(! generalUtils.isDirectory(libraryDir + fs[x]))
        ++fileCount;
    }

    return fileCount;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int countDirectories(String libraryDir) throws Exception
  {
    File path = new File(libraryDir);
    String fs[] = new String[0];
    fs = path.list();

    int dirCount=0;
    for(int x=0;x<fs.length;++x)
    {
      if(generalUtils.isDirectory(libraryDir + fs[x]))
        ++dirCount;
    }

    return dirCount;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void listFiles(PrintWriter out, String libraryDir, String fs, int directoryPrefixBitLen, String userName, boolean canView, int[] bytesOut) throws Exception
  {
    int fileCount;

    if((fileCount = countFiles((libraryDir + fs + "/"))) > 0)
    {
      scoutln(out, bytesOut, "<tr><td><p>");
      if(canView)
        scoutln(out, bytesOut, "<a href=\"javascript:list('" + generalUtils.sanitise2((libraryDir + fs).substring(directoryPrefixBitLen)) + "','" + userName + "')\">");

      scout(out, bytesOut, "" + fileCount);
      if(fileCount == 1)
       scout(out, bytesOut, " file");
      else scout(out, bytesOut, " files");

      if(canView)
        scout(out, bytesOut, "</a>");
      scout(out, bytesOut, "</td></tr>");
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void listDocs(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String companyCode, String docName, String tableName, String documentsType, int[] bytesOut) throws Exception
  {
    if(companyCode.length() == 0) // just-in-case
      return;

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM " + tableName + " WHERE CompanyCode= '" + companyCode + "'");

    int numRecs;
    if(! rs.next())
      numRecs = 0;
    else numRecs = rs.getInt("rowcount");

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    if(numRecs == 0 && documentsType.equals("Any"))
      ;
    else
    {
      scoutln(out, bytesOut, "<tr><td><p>");
      scoutln(out, bytesOut, "<a href=\"javascript:list" + tableName + "('" + companyCode + "')\">");

      scout(out, bytesOut, numRecs + " " + docName);
      if(numRecs != 1)
        scout(out, bytesOut, "s");

      scout(out, bytesOut, "</a>");
      scoutln(out, bytesOut, "</td></tr>");
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void listMail(PrintWriter out, String companyCode, String companyType, String documentsType, String dnm, int[] bytesOut) throws Exception
  {
    if(companyCode.length() == 0) // just-in-case
      return;

    String userName = directoryUtils.getMySQLUserName();
    String passWord = directoryUtils.getMySQLPassWord();

    Class.forName("com.mysql.jdbc.Driver").newInstance();

    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_mail?user=" + userName + "&password=" + passWord);
    Statement stmt = con.createStatement();

    ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM mail WHERE CompanyCode = '" + companyCode + "' AND CompanyType = '" + companyType + "'");

    int numRecs;
    if(! rs.next())
      numRecs = 0;
    else numRecs = rs.getInt("rowcount");

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    if(con  != null) con.close();

    if(numRecs == 0 && documentsType.equals("Any"))
      ;
    else
    {
      scoutln(out, bytesOut, "<tr><td><p>");
      scoutln(out, bytesOut, "<a href=\"javascript:listMail('" + companyCode + "','" + companyType + "')\">");

      scout(out, bytesOut, numRecs + " mail");
      if(numRecs > 1)
        scout(out, bytesOut, "s");

      scoutln(out, bytesOut, "</a></td></tr>");
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void listFaxes(PrintWriter out, String companyCode, String companyType, String documentsType, String dnm, int[] bytesOut) throws Exception
  {
    if(companyCode.length() == 0) // just-in-case
      return;

    String userName = directoryUtils.getMySQLUserName();
    String passWord = directoryUtils.getMySQLPassWord();

    Class.forName("com.mysql.jdbc.Driver").newInstance();

    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_fax?user=" + userName + "&password=" + passWord);
    Statement stmt = con.createStatement();

    ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM faxed WHERE CompanyCode = '" + companyCode + "' AND CompanyType = '" + companyType + "'");

    int numRecs;
    if(! rs.next())
      numRecs = 0;
    else numRecs = rs.getInt("rowcount");

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    if(con  != null) con.close();

    if(numRecs == 0 && documentsType.equals("Any"))
      ;
    else
    {
      scoutln(out, bytesOut, "<tr><td><p>");
      scoutln(out, bytesOut, "<a href=\"javascript:listFaxes('" + companyCode + "','" + companyType + "')\">");

      scout(out, bytesOut, numRecs + " fax");
      if(numRecs > 1)
        scout(out, bytesOut, "es");

      scoutln(out, bytesOut, "</a></td></tr>");
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {
    out.println(str);
    bytesOut[0] += (str.length() + 2);
  }
  private void scout(PrintWriter out, int bytesOut[], String str) throws Exception
  {
    out.print(str);
    bytesOut[0] += str.length();
  }

}
