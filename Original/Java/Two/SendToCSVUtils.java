// =======================================================================================================================================================================================================
// System: ZaraStar Utils: Send to CSV
// Module: SendToCSVUtils.java
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

public class SendToCSVUtils extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
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

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="", p3="";

    try
    {
      unm = req.getParameter("unm");
      sid = req.getParameter("sid");
      uty = req.getParameter("uty");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");
      p1  = req.getParameter("p1"); // code
      p2  = req.getParameter("p2"); // fileNameL
      p3  = req.getParameter("p3"); // docName

      if(p1 == null) p1 = "";
      if(p2 == null) p2 = "";
      if(p3 == null) p3 = "";
  
      doIt(out, req, res, unm, sid, uty, men, den, dnm, bnm, p1, p2, p3, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "SendToCSVUtils", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 11300, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush(); 
    }
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, HttpServletResponse res, String unm, String sid, String uty, String men, String den,
                    String dnm, String bnm, String p1, String p2, String p3, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String workingDir       = directoryUtils.getUserDir('W', dnm, "/" + unm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      serverUtils.etotalBytes(req, unm, dnm, 11300, bytesOut[0], 0, "SID:" + p1);
      if(con  != null) con.close();
      return;
    }

    String docName = createDocName(p3, p1);

    RandomAccessFile fh = generalUtils.create(workingDir + docName);
    
    if(p2.equals("quotel"))
    {
      generate(fh, p1, p2, "QuoteCode", "ItemCode", "Description", "UnitPrice", "Quantity", "Amount2", "GSTRate", "Discount", "DeliveryDate", "Line", "Entry", "UoM", "CustomerItemCode", "Manufacturer", "ManufacturerCode", dnm, localDefnsDir,
               defnsDir);
    }
    
    generalUtils.fileClose(fh);

    download(res, unm, workingDir, docName, bytesOut);
    
    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 11300, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con  != null) con.close();
  }
   
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private String createDocName(String docName, String docCode) throws Exception
  {
    String s = docName + "_";

    int len = docCode.length();
    for(int x=0;x<len;++x)
    {
      if(docCode.charAt(x) >= '0' && docCode.charAt(x) <= '9')
        s += docCode.charAt(x);
    }
    
    return s + ".csv";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void generate(RandomAccessFile fh, String code, String tableName, String codeFld, String itemCodeFld, String descFld, String unitPriceFld, String qtyFld, String amount2Fld, String gstRateFld, String discountFld, String deliveryDateFld,
                        String lineFld, String entryFld, String uomFld, String custItemCodeFld, String mfrFld, String mfrCodeFld, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    String userName = directoryUtils.getMySQLUserName();
    String passWord = directoryUtils.getMySQLPassWord();
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + userName + "&password=" + passWord);
    
    Statement stmt = con.createStatement(), stmt2 = null;
    ResultSet rs2 = null;

    ResultSet rs = stmt.executeQuery("SELECT " + lineFld + "," + entryFld + "," + itemCodeFld + "," + mfrFld + "," + mfrCodeFld + "," + custItemCodeFld + "," + descFld + "," + qtyFld + "," + uomFld + "," + unitPriceFld + "," + discountFld + ","
                                   + gstRateFld  + "," + amount2Fld + "," + deliveryDateFld + " FROM " + tableName + " WHERE " + codeFld + " = '" + code + "' ORDER BY " + lineFld);

    char dpOnQuantities = miscDefinitions.dpOnQuantities(con, stmt, rs, "5");

    writeEntry(fh, lineFld,         ' ', ' ', true, false);
    writeEntry(fh, entryFld,        ' ', ' ', true, false);
    writeEntry(fh, itemCodeFld,     ' ', ' ', true, false);
    writeEntry(fh, mfrFld,          ' ', ' ', true, false);
    writeEntry(fh, mfrCodeFld,      ' ', ' ', true, false);
    writeEntry(fh, custItemCodeFld, ' ', ' ', true, false);
    writeEntry(fh, descFld,         ' ', ' ', true, false);
    writeEntry(fh, qtyFld,          ' ', ' ', true, false);
    writeEntry(fh, uomFld,          ' ', ' ', true, false);
    writeEntry(fh, unitPriceFld,    ' ', ' ', true, false);
    writeEntry(fh, discountFld,     ' ', ' ', true, false);
    writeEntry(fh, gstRateFld,      ' ', ' ', true, false);
    writeEntry(fh, amount2Fld,      ' ', ' ', true, false);
    writeEntry(fh, deliveryDateFld, ' ', ' ', false, true);

    String text;
    
    while(rs.next())
    {    
      writeEntry(fh, rs.getString(1),  ' ', dpOnQuantities, true, false);
      writeEntry(fh, rs.getString(2),  ' ', dpOnQuantities, true, false);
      writeEntry(fh, rs.getString(3),  ' ', dpOnQuantities, true, false);
      writeEntry(fh, rs.getString(4),  ' ', dpOnQuantities, true, false);
      writeEntry(fh, rs.getString(5),  ' ', dpOnQuantities, true, false);
      writeEntry(fh, rs.getString(6),  ' ', dpOnQuantities, true, false);
      
      text = rs.getString(7) + generateMultipleLines(con, stmt2, rs2, code, rs.getString(1), tableName, codeFld);

      writeEntry(fh, text, ' ', dpOnQuantities, true, false);
      
      writeEntry(fh, rs.getString(8),  'Q', dpOnQuantities, true, false);
      writeEntry(fh, rs.getString(9),  ' ', dpOnQuantities, true, false);
      writeEntry(fh, rs.getString(10), 'V', dpOnQuantities, true, false);
      writeEntry(fh, rs.getString(11), 'V', dpOnQuantities, true, false);
      writeEntry(fh, rs.getString(11), ' ', dpOnQuantities, true, false);
      writeEntry(fh, rs.getString(12), ' ', dpOnQuantities, true, false);
      writeEntry(fh, rs.getString(13), 'V', dpOnQuantities, true, false);
      writeEntry(fh, rs.getString(14), 'D', dpOnQuantities, false, true);
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    if(con  != null) con.close();  
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private String generateMultipleLines(Connection con, Statement stmt, ResultSet rs, String code, String entry, String tableName, String codeFld)
                                       throws Exception
  {
    String text = "";

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT Text FROM " + tableName + "l WHERE " + codeFld + " = '" + code + "' AND Entry = '" + entry + "' ORDER BY Line");

    while(rs.next())
    {
      text += (" " + rs.getString(1));
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    return text;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void writeEntry(RandomAccessFile fh, String entry, char type, char dpOnQuantities, boolean comma, boolean newLine) throws Exception
  {
    if(entry == null) entry = "";
    
    switch(type)
    {
      case 'D' : entry = generalUtils.convertFromYYYYMMDD(entry);       break;
      case 'Q' : entry = generalUtils.doubleDPs(entry, dpOnQuantities); break;
      case 'V' : entry = generalUtils.doubleDPs(entry, '2');            break;
    }
    
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
  private void download(HttpServletResponse res, String unm, String dirName, String fileName, int[] bytesOut) throws Exception
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

}
