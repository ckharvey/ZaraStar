// =======================================================================================================================================================================================================
// System: ZaraStar Product: Generate stock listing report
// Module: ProductStockListingGenerate.java
// Author: C.K.Harvey
// Copyright (c) 2000-07 Christopher Harvey. All Rights Reserved.
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
import java.net.URL;
import java.net.URLConnection;

public class ProductStockListingGenerate extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  ReportGeneration reportGeneration = new ReportGeneration();
  Inventory inventory = new Inventory();
  PrintingLayout printingLayout = new PrintingLayout();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  MiscDefinitions miscDefinitions = new MiscDefinitions();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="", p3="", p4="", p5="";
    
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
      p1  = req.getParameter("p1");  // mfr
      p2  = req.getParameter("p2");  // dateFrom
      p3  = req.getParameter("p3");  // dateTo
      p4  = req.getParameter("p4");  // store
      p5  = req.getParameter("p5");  // includeZero

      if(p1==null) p1="";
      if(p2==null) p2="";
      if(p3==null) p3="";
      if(p4==null) p4="";
      if(p5==null) p5="Y";
      
      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, p3, p4, p5, bytesOut);
    }
    catch(Exception e)
    {
      StringBuffer url = req.getRequestURL();
      int x=0;
      if(url.charAt(x) == 'h' || url.charAt(x) == 'H')
        x += 7;
      String urlbt="";
      while(url.charAt(x) != '\000' && url.charAt(x) != ',' && url.charAt(x) != '/')
        urlbt += url.charAt(x++);

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlbt, men, den, uty, "ProductStockListingGenerate", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3010, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm,
                    String bnm, String p1, String p2, String p3, String p4, String p5, int[] bytesOut) throws Exception
  {
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String reportsDir       = directoryUtils.getUserDir('R', dnm, unm);
    String workingDir       = directoryUtils.getUserDir('W', dnm, unm);
    String imagesDir     = directoryUtils.getSupportDirs('I');
  
    long startTime = new java.util.Date().getTime();

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null, stmt2 = null;
    ResultSet rs   = null, rs2   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 3010, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "ProductStockListingGenerate", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3010, bytesOut[0], 0, "ACC:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "ProductStockListingGenerate", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3010, bytesOut[0], 0, "SID:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    boolean includeZeroLines;
    if(p5.equals("Y"))
      includeZeroLines = true;
    else includeZeroLines = false;
    
    String dateFrom, dateFromText;
    if(p2.length() == 0 || p2.equalsIgnoreCase("NULL"))
    {
      dateFrom = "1970-01-01";
      dateFromText = "Start";
    }  
    else
    {
      dateFromText = p2;
      dateFrom = generalUtils.convertDateToSQLFormat(p2);
    }

    String dateTo, dateToText;
    if(p3.length() == 0 || p3.equalsIgnoreCase("NULL"))
    {
      dateTo = "2099-12-31";
      dateToText = "Finish";
    }
    else
    {
      dateToText = p3;
      dateTo = generalUtils.convertDateToSQLFormat(p3);
    }
    
    short rtn = r022(con, stmt, stmt2, rs, rs2, p1, dateFrom, dateTo, p4, includeZeroLines, unm, sid, uty, men, den, dnm, bnm, reportsDir, workingDir, localDefnsDir, defnsDir);

    int target;
    switch(rtn)
    {
      case -1 : // Definition File Not Found
                target = 17;
                break;
      case -2 : // cannot create report output file
                target = 18;
                break;
      default : // generated ok
                target = 16;
                break;
    }

    messagePage.msgScreen(false, out, req, target, unm, sid, uty, men, den, dnm, bnm, "ProductStockListingGenerate", "", "", "", imagesDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 3010, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con  != null) con.close();
    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private short r022(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String mfr, String dateFrom, String dateTo, String reqdStore, boolean includeZeroLines, String unm, String sid,
                     String uty, String men, String den, String dnm, String bnm, String reportsDir, String workingDir, String localDefnsDir,
                     String defnsDir) throws Exception
  {
    reportGeneration.currFont = 1;
    reportGeneration.currPage = 1;
    reportGeneration.currDown = reportGeneration.currAcross = 0.0;

    reportGeneration.oBufLen = 30000;
    reportGeneration.oBuf = new byte[30000];
    reportGeneration.oPtr = 0;

    String[] newName = new String[1];
    if((reportGeneration.fhO = reportGeneration.createNewFile((short)22, workingDir, localDefnsDir, defnsDir, reportsDir, newName)) == null)
      return -2;

    if((reportGeneration.fhPPR = generalUtils.fileOpenD("022.ppr", localDefnsDir)) == null)
    {
      if((reportGeneration.fhPPR = generalUtils.fileOpenD("022.ppr", defnsDir)) == null)
      {
        generalUtils.fileClose(reportGeneration.fhO);
        return -1;
      }
    }

    reportGeneration.lastOperationPF = false;

    reportGeneration.processControl(dnm, unm, localDefnsDir, defnsDir);
    reportGeneration.processSection("RH", null, null, (short)0, dnm, unm, localDefnsDir, defnsDir);
    reportGeneration.processSection("PH", null, null, (short)0, dnm, unm, localDefnsDir, defnsDir);

    byte[] fldNames = new byte[3000];
    byte[] fldData  = new byte[2000];

    String fieldTypes = inventory.getFieldTypesStock();
    int numFlds = generalUtils.buildFieldNamesInBuf(inventory.getFieldNamesStock() + ",xStore,xLevel,xWAC,xTotalCost", "Stock", fldNames);

    char dpOnQuantities = miscDefinitions.dpOnQuantities(con, stmt, rs, "5");
    
    boolean useWAC = false;
    if(miscDefinitions.inventoryCostingMethod(con, stmt, rs).equals("WAC"))
      useWAC = true;

    stmt = con.createStatement();

    if(mfr.equals("___ALL___"))
      rs = stmt.executeQuery("SELECT * FROM stock ORDER BY Manufacturer, ManufacturerCode");
    else rs = stmt.executeQuery("SELECT * FROM stock WHERE Manufacturer = '" + mfr + "' ORDER BY ManufacturerCode");

    ResultSetMetaData rsmd = rs.getMetaData();

    String traceList, store, level;
    int x, len;
    boolean storeFound;
    double wac, levelD;

    String dateTo2 = generalUtils.convertFromYYYYMMDD(dateTo);
    
    while(rs.next())
    {
      for(x=0;x<(numFlds - 4);++x)
        generalUtils.repAlpha(fldData, 2000, (short)x, inventory.getValue((x + 1), fieldTypes.charAt(x), rs, rsmd));
      
      traceList = getStockLevelsViaTrace(rs.getString(1), dateTo2, unm, uty, sid, men, den, dnm, bnm, localDefnsDir);
      // traceList = "SamLeong\001150000\001SyedAlwi\00150000\001\0"

      len = traceList.length();
      storeFound = false;
      x=0;
      while(! storeFound && x < len)
      {
        store = "";
        while(x < len && traceList.charAt(x) != '\001')
          store += traceList.charAt(x++);
      
        ++x;
        level = "";
        while(x < len && traceList.charAt(x) != '\001')
          level += traceList.charAt(x++);
      
        levelD = generalUtils.doubleFromStr(level);
        
        if(store.equals(reqdStore))
        {
          if(levelD == 0.0 && ! includeZeroLines)
            ;
          else
          {
            generalUtils.repAlpha(fldData, 2000, (short)(numFlds - 4), store);

            generalUtils.repAlpha(fldData, 2000, (short)(numFlds - 3), generalUtils.formatNumeric(level, dpOnQuantities));
      
            if(useWAC)
              wac = inventory.getWAC(con, stmt2, rs2, rs.getString(1), dateFrom, dateTo, dnm);
            else wac = 0.0;
            
            generalUtils.repAlpha(fldData, 2000, (short)(numFlds - 2), generalUtils.formatNumeric(wac, '2'));

            generalUtils.repAlpha(fldData, 2000, (short)(numFlds - 1), generalUtils.formatNumeric((wac * levelD), '2'));
            
            reportGeneration.processSection("BL1", fldData, fldNames, (short)numFlds, dnm, unm, localDefnsDir, defnsDir);
          }
          
          storeFound = true;
        }
        else x++;
      }
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    if(! reportGeneration.lastOperationPF)
      reportGeneration.processSection("PF", null, null, (short)0, dnm, unm, localDefnsDir, defnsDir);
    reportGeneration.processSection("RF", null, null, (short)0, dnm, unm, localDefnsDir, defnsDir);

    reportGeneration.fhO.write(reportGeneration.oBuf, 0, reportGeneration.oPtr);

    reportGeneration.fhO.close();
    reportGeneration.fhPPR.close();

    printingLayout.updateNumPages(0, newName[0], reportsDir);

    return 0;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private String getStockLevelsViaTrace(String itemCode, String dateTo, String unm, String uty, String sid, String men, String den, String dnm, String bnm, String localDefnsDir) throws Exception
  { 
    URL url = new URL("http://" + serverUtils.serverToCall("OFSA", localDefnsDir) + "/central/servlet/StockLevelsGenerate?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=" + itemCode + "&p2=" + dateTo  + "&bnm="
                     + bnm);

    URLConnection uc = url.openConnection();
    uc.setDoInput(true);
    uc.setUseCaches(false);
    uc.setDefaultUseCaches(false);

    uc.setRequestProperty("Content-Type", "application/octet-stream");

    BufferedReader di = new BufferedReader(new InputStreamReader(uc.getInputStream()));

    String t="", s = di.readLine();
    while(s != null)
    {
      t += s;
      s = di.readLine();
    }

    di.close();

    return t;
  }

}
