// =======================================================================================================================================================================================================
// System: ZaraStar Info: Consolidations
// Module: ConsolidationsObsoleteStockExecutejava
// Author: C.K.Harvey
// Copyright (c) 2002-09 Christopher Harvey. All Rights Reserved.
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
import java.sql.*;
import java.io.*;

public class ConsolidationsObsoleteStockExecute extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  AccountsUtils accountsUtils = new AccountsUtils();
  DashboardUtils dashboardUtils = new DashboardUtils();
  Customer customer = new Customer();
  StockLevelsGenerate StockLevelsGenerate = new StockLevelsGenerate();
  
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="";

    try
    {
      req.setCharacterEncoding("UTF-8");
      directoryUtils.setContentHeaders2(res);

      unm = req.getParameter("unm");
      sid = req.getParameter("sid");
      uty = req.getParameter("uty");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");
      p1  = req.getParameter("p1"); // which
      p2  = req.getParameter("p2"); // mfr
      
      if(p1.equals("S"))
        out = res.getWriter();

      doIt(out, req, res, unm, sid, uty, men, den, dnm, bnm, p1, p2, bytesOut);
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
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "ConsolidationsObsoleteStockExecute", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6916, bytesOut[0], 0, "ERR:" + p1+":"+p2);
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, HttpServletResponse res, String unm, String sid, String uty, String men, String den, String dnm,
                    String bnm, String p1, String p2, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String localDefnsDir  = directoryUtils.getLocalOverrideDir(dnm);
    String defnsDir          = directoryUtils.getSupportDirs('D');
    String imagesDir         = directoryUtils.getSupportDirs('I');
    String workingDir       = directoryUtils.getUserDir('W', dnm, "/" + unm);
    
    Statement stmt = null, stmt2 = null, stmt3 = null;
    ResultSet rs   = null, rs2 = null, rs3 = null;    
     
     String uName = directoryUtils.getMySQLUserName();
     String pWord = directoryUtils.getMySQLPassWord();
     Class.forName("com.mysql.jdbc.Driver").newInstance();
 
     Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/xxx_ofsa?user=" + uName + "&password=" + pWord + "&autoReconnect=true");
   
     
    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 6916, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "ConsolidationsObsoleteStock", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6916, bytesOut[0], 0, "ACC:" + p1+":"+p2);
      if(con  != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "ConsolidationsObsoleteStock", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6916, bytesOut[0], 0, "SID:" + p1+":"+p2);
      if(con  != null) con.close();
      if(out != null) out.flush(); 
      return;
    }
    
    boolean isCSV = false;
    
    if(p1.equals("C"))
      isCSV = true;

    RandomAccessFile fh = null;

    if(isCSV)
    {
      String fName;
      if(p2.equals("-")) fName = "6916.csv"; else fName = p2 + "-6916.csv";
      
      fh = generalUtils.create(workingDir + fName);

      this.processCSV(fh, con, stmt, stmt2, stmt3, rs, rs2, out, p2, unm, sid, uty, men, dnm, bnm, bytesOut);
      generalUtils.fileClose(fh);

      download(res, workingDir, fName, bytesOut);
    }
    else processScreen(fh, con, stmt, stmt2, stmt3, rs, rs3, out, req, p1, p2, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 6916, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1+":"+p2);
    
    if(con  != null) con.close();
    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void processScreen(RandomAccessFile fh, Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2,
          PrintWriter out, HttpServletRequest req,
          String p1, String p2,
                             String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Sales by Products</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    {
      scoutln(out, bytesOut, "function viewItem(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/MainPageUtils1a?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+p1;}");
    }

    scoutln(out, bytesOut, "function sanitise(code){");
    scoutln(out, bytesOut, "var code2='';var x;var len=code.length;");
    scoutln(out, bytesOut, "for(x=0;x<len;++x)");
    scoutln(out, bytesOut, "if(code.charAt(x)=='#')code2+='%23';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='\"')code2+='%22';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='&')code2+='%26';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='%')code2+='%25';");
    scoutln(out, bytesOut, "else if(code.charAt(x)==' ')code2+='%20';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='?')code+='%3F';");
    scoutln(out, bytesOut, "else code2+=code.charAt(x);");
    scoutln(out, bytesOut, "return code2};");
    
    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");
   
    outputPageFrame(con, stmt, rs, out, req, false, p1, p2, "", "Sales by Products", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, bytesOut);

    scoutln(out, bytesOut, "<form>");
  
    scout(out, bytesOut, "<p>For: " + p2);
    
//    scout(out, bytesOut, "<p>Consolidated into " + accountsUtils.getBaseCurrency(con, stmt, rs, dnm, localDefnsDir, defnsDir));
    
    scoutln(out, bytesOut, "<br><table id=\"page\" width=100%>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    outputTitleLine(fh, out, false, bytesOut);

     forEachRec(fh, out, false, con, stmt, stmt2, stmt3, rs, rs2, p2, unm, sid, uty, men, dnm, bnm, bytesOut);
   
    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir));
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void processCSV(RandomAccessFile fh, Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, 
          ResultSet rs2, PrintWriter out, String mfr, String unm, String ses, String uty, String men, String dnm,
          String bnm, int[] bytesOut) throws Exception
  {
    outputTitleLine(fh, out, true, bytesOut);

    forEachRec(fh, out, true, con, stmt, stmt2, stmt3, rs, rs2, mfr, unm, ses, uty, men, dnm, bnm, bytesOut);
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void forEachRec(RandomAccessFile fh, PrintWriter out, boolean isCSV, Connection con, Statement stmt, Statement stmt2,
          Statement stmt3, ResultSet rs, ResultSet rs2, String mfr, String unm, String ses, String uty, String men, String dnm,
          String bnm, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    if(! mfr.equals("-"))
    {
      rs = stmt.executeQuery("SELECT ItemCode, Manufacturer, ManufacturerCode, Description, Description2, Status FROM stock WHERE Manufacturer = '" + generalUtils.sanitiseForSQL(mfr) + "'"
                                  + " ORDER BY ManufacturerCode");
    }
    else
    {
      rs = stmt.executeQuery("SELECT ItemCode, Manufacturer, ManufacturerCode, Description, Description2, Status FROM stock "
                                  + " ORDER BY Manufacturer, ManufacturerCode");
    }
    
    String itemCode, mfr2, reason = "", mfrCode, desc, desc2, stockOnHand, status, descA, descB;
    int y, len;
    boolean obs;
    
    while(rs.next())
    {
      itemCode   = rs.getString(1);
      mfr2 = generalUtils.deNull(rs.getString(2));
      mfrCode = generalUtils.deNull(rs.getString(3));
      desc = generalUtils.deNull(rs.getString(4));
      desc2     = generalUtils.deNull(rs.getString(5));
      status     = generalUtils.deNull(rs.getString(6));

      descA = desc.toLowerCase(); 
      descB = desc2.toLowerCase(); 

      obs = false;
      
      if(status.equals("C"))
      {
        reason = "Checked";
        obs = true;
      }
      else
      if(descA.contains("(obs)"))    
      {
        reason = "Description";
        obs = true;
      }
      else
      if(descB.contains("(obs)"))    
      {
        reason = "Description";
        obs = true;
      }
      else
      if(descA.contains("obsolete"))    
      {
        reason = "Description";
        obs = true;
      }
      else
      if(descB.contains("obsolete"))    
      {
        reason = "Description";
        obs = true;
      }
      else
      {
        if(! getPOs(con, stmt2, rs2, itemCode, "2012-07-01"))
        {
          if(! getLPs(con, stmt2, rs2, itemCode, "2012-07-01"))
          {
            obs = true;
            reason = "No Order";
          }
        }
      }  
      
      if(obs)
      {
        stockOnHand = StockLevelsGenerate.fetch(con, stmt2, stmt3, rs2, itemCode, "", "", unm, ses, uty, men, dnm, dnm, bnm);

        y = 0;
        len = stockOnHand.length();
      
        while(y < len && stockOnHand.charAt(y)!= '\001')
          ++y;
        ++y;
      
        stockOnHand = stockOnHand.substring(y); 
                
        outputLine(fh, out, isCSV, reason, itemCode, desc, desc2, mfr2, mfrCode, stockOnHand, bytesOut);
      }
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void outputPageFrame(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, boolean plain, String effectiveDate, String mfr, String bodyStr, String title, String unm, String sid, String uty, String men,
                               String den, String dnm, String bnm, String otherSetup, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    int[] hmenuCount = new int[1];  hmenuCount[0] = 0;

    String subMenuText = "";

    scoutln(out, bytesOut, authenticationUtils.buildScreen(con, stmt, rs, req, true, unm, sid, uty, men, den, dnm, bnm, "ConsolidationsObsoleteStock", subMenuText, hmenuCount[0], bodyStr, otherSetup, localDefnsDir, defnsDir));
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scout(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.print(str);
    bytesOut[0] += str.length();    
  }
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void writeEntry(RandomAccessFile fh, String entry, boolean comma, boolean newLine) throws Exception
  {
    if(entry == null) entry = "";

    fh.writeBytes("\"");
    for(int x=0;x<entry.length();++x)
    {
      if(entry.charAt(x) == '"')
        fh.writeBytes("''");
      else fh.writeBytes("" + entry.charAt(x));
    }

    fh.writeBytes("\"");

    if(comma)
      fh.writeBytes(",");

    if(newLine)
      fh.writeBytes("\n");
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void download(HttpServletResponse res, String dirName, String fileName, int[] bytesOut) throws Exception
  {
    res.setContentType("application/x-download");
    res.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

    OutputStream out = res.getOutputStream();

    InputStream in = null;
    try
    {
      in = new BufferedInputStream(new FileInputStream(dirName + fileName));
      byte[] buf = new byte[4096];
      int bytesRead;
      while((bytesRead = in.read(buf)) != -1)
        out.write(buf, 0, bytesRead);
    }
    catch(Exception e) //finally
    {
      if(in != null)
        in.close();
    }

    File file = new File(dirName + fileName);
    long fileSize = file.length();

    bytesOut[0] += (int)fileSize;
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void outputLine(RandomAccessFile fh, PrintWriter out, boolean isCSV,String reason,  String itemCode, String desc, String desc2,
          String mfr, String mfrCode, String stockOnHand, int[] bytesOut) throws Exception
  {
    if(isCSV)
    {  
      writeEntry(fh, reason, true, false);
      writeEntry(fh, itemCode, true, false);
      writeEntry(fh, desc, true, false);
      writeEntry(fh, desc2, true, false);
      writeEntry(fh, mfr, true, false);
      writeEntry(fh, mfrCode, true, false);
      writeEntry(fh, stockOnHand, false, true);
    }
    else
    {
      scoutln(out, bytesOut, "<tr>");
    
      scoutln(out, bytesOut, "<td valign=top><p>" + reason + "</td>");

      scoutln(out, bytesOut, "<td valign=top><p>");
      scoutln(out, bytesOut, "<a href=\"javascript:viewItem('" + itemCode + "')\">" + itemCode + "</a></td>");

      scoutln(out, bytesOut, "<td><p>" + desc + "<br>" + desc2 + "</td>");

      scoutln(out, bytesOut, "<td valign=top><p>" + mfr + "</td>");

      scoutln(out, bytesOut, "<td valign=top><p>" + mfrCode + "</td>");
      scoutln(out, bytesOut, "<td valign=top align=right><p>" + generalUtils.formatNumeric(stockOnHand, '0') + "</td>");

      scoutln(out, bytesOut, "</tr>");
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void outputTitleLine(RandomAccessFile fh, PrintWriter out, boolean isCSV, int[] bytesOut) throws Exception
  {
    if(isCSV)
    {  
      writeEntry(fh, "Reason", true, false);
      writeEntry(fh, "Item Code", true, false);
      writeEntry(fh, "Description", true, false);
      writeEntry(fh, "Description2", true, false);
      writeEntry(fh, "Manufacturer", true, false);
      writeEntry(fh, "Manufacturer Code", true, false);
      writeEntry(fh, "Stock OnHand", false, true);
    }
    else
    {
      scoutln(out, bytesOut, "<tr id=\"pageColumn\">");
      
      scoutln(out, bytesOut, "<td><p>Reason</td><td><p>ItemCode</td><td><p>Description</td><td><p>Manufacturer</td>"
              + "<td><p>ManufacturerCode</td><td><p>Stock OnHand</td></tr>");
    }
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean getPOs(Connection con, Statement stmt, ResultSet rs, String itemCode, String dateFrom) throws Exception
  {
    boolean res = false;
    
    stmt = con.createStatement();
    
    stmt.setMaxRows(1);
    
    rs = stmt.executeQuery("SELECT t2.Amount FROM po AS t1 INNER JOIN pol AS t2 ON t1.POCode = t2.POCode "
                         + "WHERE t2.ItemCode = '" + generalUtils.sanitiseForSQL(itemCode) + "' AND t1.Date >= {d '" + dateFrom + "'}  AND t1.Status != 'C'");
    
    if(rs.next())
      res = true;
  
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return res;
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean getLPs(Connection con, Statement stmt, ResultSet rs, String itemCode, String dateFrom) throws Exception
  {
    boolean res = false;
    
    stmt = con.createStatement();
    
    stmt.setMaxRows(1);
    
    rs = stmt.executeQuery("SELECT t2.Amount FROM lp AS t1 INNER JOIN lpl AS t2 ON t1.LPCode = t2.LPCode "
                         + "WHERE t2.ItemCode = '" + generalUtils.sanitiseForSQL(itemCode) + "' AND t1.Date >= {d '" + dateFrom + "'}  AND t1.Status != 'C'");
    
    if(rs.next())
      res = true;
  
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return res;
  }

}
