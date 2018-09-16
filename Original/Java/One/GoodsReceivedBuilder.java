// =======================================================================================================================================================================================================
// System: ZaraStar Document: GR: create initial GR builder page
// Module: GoodsReceivedBuilder.java
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

public class GoodsReceivedBuilder extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  DrawingUtils drawingUtils = new DrawingUtils();
  Supplier supplier = new Supplier();
  MiscDefinitions miscDefinitions = new MiscDefinitions();
  DocumentUtils documentUtils = new DocumentUtils();
  GoodsReceivedNote goodsReceivedNote = new GoodsReceivedNote();

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
      directoryUtils.setContentHeaders(res);
      out = res.getWriter();

      unm = req.getParameter("unm");
      sid = req.getParameter("sid");
      uty = req.getParameter("uty");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");
      p1  = req.getParameter("p1"); // suppCode
      p2  = req.getParameter("p2"); // poCode (maybe)

      if(p1 == null) p1 = "";
      if(p2 == null) p2 = "";
      
      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2.toUpperCase(), bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "_3033", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3033, bytesOut[0], 0, "ERR:" + p1 + ":" + p2);
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1, String p2, int[] bytesOut) throws Exception
   {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    
    Statement stmt = null;
    Statement stmt2 = null;
    ResultSet rs    = null;
    ResultSet rs2   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 3033, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "3033", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3033, bytesOut[0], 0, "ACC:" + p1 + ":" + p2);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "3033", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3033, bytesOut[0], 0, "SID:" + p1 + ":" + p2);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    p1 = p1.toUpperCase();
    byte[] codeB = new byte[21];
    generalUtils.strToBytes(codeB, p1);

    set(con, stmt, stmt2, rs, rs2, out, req, p1, codeB, p2, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 3033, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "GR Builder: " + p1 + ":" + p2);
      
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, HttpServletRequest req, String code, byte[] codeB, String poCode, String unm, String sid, String uty, String men, String den,
                   String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Goods Received Builder</title>");
    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    scoutln(out, bytesOut, "function view(code){");
    scoutln(out, bytesOut, "var p1=sanitise(code);");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/PurchaseOrderPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    
    scoutln(out, bytesOut, "function create(){document.lines.submit();}");
    scoutln(out, bytesOut, "function createOne(suppCode){document.forms[0].supplierCode.value=suppCode;document.lines.submit();}");
        
    scoutln(out, bytesOut, "function sanitise(code){");
    scoutln(out, bytesOut, "var code2='';var x;var len=code.length;");
    scoutln(out, bytesOut, "for(x=0;x<len;++x)");
    scoutln(out, bytesOut, "if(code.charAt(x)=='#')code2+='%23';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='\"')code2+='%22';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='&')code2+='%26';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='%')code2+='%25';");
    scoutln(out, bytesOut, "else if(code.charAt(x)==' ')code2+='%20';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='?')code2+='%3F';");
    scoutln(out, bytesOut, "else code2+=code.charAt(x);");
    scoutln(out, bytesOut, "return code2};");

    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    int[] hmenuCount = new int[1];

    drawingUtils.outputPageFrame(con, stmt, rs, out, req, "3033", "", "_3033", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount, bytesOut);

    drawingUtils.drawTitle(out, false, false, "_3033", "", "Goods Received Builder", "3033", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<form action=\"GoodsRgreivedBuilderCreate\" name=lines enctype=\"application/x-www-form-urlencoded\" method=POST>");
    scoutln(out, bytesOut, "<input type=hidden name=unm value=" + unm + ">");
    scoutln(out, bytesOut, "<input type=hidden name=sid value=" + sid + ">");
    scoutln(out, bytesOut, "<input type=hidden name=uty value=" + uty + ">");
    scoutln(out, bytesOut, "<input type=hidden name=men value=" + men + ">");
    scoutln(out, bytesOut, "<input type=hidden name=den value=" + den + ">");
    scoutln(out, bytesOut, "<input type=hidden name=dnm value=" + dnm + ">");
    scoutln(out, bytesOut, "<input type=hidden name=bnm value=" + bnm + ">");
    scoutln(out, bytesOut, "<input type=hidden name=supplierCode value=" + code + ">");
    
    scoutln(out, bytesOut, "<table id=\"page\" border=0 cellspacing=2 cellpadding=0 width=100%>");

    // get supplier name
    byte[] supplierName = new byte[81];
    supplier.getASupplierFieldGivenCode(con, stmt, rs, "Name", codeB, supplierName);
    String name = generalUtils.stringFromBytes(supplierName, 0L);

    scoutln(out, bytesOut, "<tr><td><p>Outstanding PO Lines for: " + code + " (" + name + ")</td></tr></table>");

    scoutln(out, bytesOut, "<table id=\"page\" border=0 cellspacing=2 cellpadding=0>");

    scoutln(out, bytesOut, "<tr><td nowrap><p>Note &nbsp; <input type=text name=note size=70 maxlength=200></td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr></table>");

    scoutln(out, bytesOut, "<table id='page' border=0 cellspacing=2 cellpadding=3><tr id=\"pageColumn\">");
    scoutln(out, bytesOut, "<td nowrap><p>Date &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>PO Code &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap align=center><p>&nbsp; Line &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap align=center><p>&nbsp; Entry &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap align=center><p> &nbsp; Quantity &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap align=center><p>&nbsp; Quantity &nbsp;<br>&nbsp; Already &nbsp;<br>&nbsp; Received  &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>&nbsp; This &nbsp;<br>&nbsp; Quantity &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Item Code &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Manufacturer<br>Code</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Supplier<br>Item Code</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Supplier<br>DO Code</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Supplier<br>Invoice Code</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Unit Price &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Store &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Description &nbsp;</td></tr>");

    String[] poCodesArg = new String[1];  poCodesArg[0] = "";
    String[] poLinesArg = new String[1];  poLinesArg[0] = "";

    String suppCode = "";
    if(poCode.length() > 0)
      suppCode = doOnePO(con, stmt, stmt2, rs, rs2, out, poCode, poCodesArg, poLinesArg, bytesOut);
    else detPOLines(con, stmt, stmt2, rs, rs2, out, code, poCodesArg, poLinesArg, bytesOut);
    
    scoutln(out, bytesOut, "<input type=hidden name=poCodes value=" + poCodesArg[0] + ">");
    scoutln(out, bytesOut, "<input type=hidden name=poLines value=" + poLinesArg[0] + ">");
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    if(poCode.length() > 0)
      scoutln(out, bytesOut, "<tr><td nowrap colspan=4><p><a href=\"javascript:createOne('" + suppCode + "')\">Create</a> Goods Received Note</td></tr>");
    else scoutln(out, bytesOut, "<tr><td nowrap colspan=4><p><a href=\"javascript:create()\">Create</a> Goods Received Note</td></tr>");

    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private String doOnePO(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, String poCode, String[] poCodesArg, String[] poLinesArg, int[] bytesOut) throws Exception
  {
    String suppCode = "";
      
    try
    {
      String poCodes = "", poDates = "", allReceived;
      
      stmt = con.createStatement();
      rs = stmt.executeQuery("SELECT Date, AllReceived, CompanyCode FROM po WHERE POCode = '" + poCode + "'"); 
      
      if(rs.next())
      {
        suppCode    = rs.getString(3);
        allReceived = rs.getString(2);
        if(! allReceived.equals("R") && ! allReceived.equals("Y")) // not marked as received
        {
          poCodes += (poCode + "\001");
          poDates += (rs.getString(1) + "\001");
        }  
      }  
      
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();

      processPOCodes(con, stmt, stmt2, rs, rs2, out, poCodes, poDates, poCodesArg, poLinesArg, bytesOut);
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(rs   != null) rs.close();     
      if(stmt != null) stmt.close();
    }
    
    return suppCode;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void detPOLines(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, String code, String[] poCodesArg, String[] poLinesArg, int[] bytesOut) throws Exception
  {
    try
    {
      String poCodes = "", poDates = "", allReceived;
      
      stmt = con.createStatement();
      rs = stmt.executeQuery("SELECT POCode, Date, AllReceived FROM po WHERE CompanyCode = '" + code + "'"); 
      
      while(rs.next())
      {
        allReceived = rs.getString(3);
        if(! allReceived.equals("R") && ! allReceived.equals("Y")) // not marked as received
        {
          poCodes += (rs.getString(1) + "\001");
          poDates += (rs.getString(2) + "\001");
        }  
      }  
      
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();

      processPOCodes(con, stmt, stmt2, rs, rs2, out, poCodes, poDates, poCodesArg, poLinesArg, bytesOut);
    }
    catch(Exception e)
    {
    System.out.println(e);
      if(rs   != null) rs.close();     
      if(stmt != null) stmt.close();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void processPOCodes(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, String poCodes, String poDates, String[] poCodesArg, String[] poLinesArg, int[] bytesOut)
  {
    try
    {
      char dpOnQuantities = miscDefinitions.dpOnQuantities(con, stmt, rs, "5");

      String thisCode, thisDate, poLine, qty, unitPrice, itemCode, store, mfrCode, desc, actualQtyStr, supplierItemCode, entry;
      double actualQty, qtyD;
      int x=0, y=0, poCodesLen = poCodes.length(), poDatesLen = poDates.length(), count=0;
      byte[] b = new byte[20];
      
      while(x < poCodesLen)
      {
        thisCode="";
        while(x < poCodesLen && poCodes.charAt(x) != '\001')
          thisCode += poCodes.charAt(x++);
        ++x;
      
        thisDate="";
        while(y < poDatesLen && poDates.charAt(y) != '\001')
          thisDate += poDates.charAt(y++);
        ++y;
      
        stmt = con.createStatement();
        rs = stmt.executeQuery("SELECT ItemCode, Description, UnitPrice, Quantity, Line, StoreCode, ManufacturerCode, SupplierItemCode, Entry FROM pol WHERE POCode = '" + thisCode + "'"); 
        
        while(rs.next())
        {
          itemCode  = rs.getString(1);
          desc      = rs.getString(2);
          unitPrice = rs.getString(3);
          qty       = rs.getString(4);
          poLine    = rs.getString(5);
          store     = rs.getString(6);
          mfrCode   = rs.getString(7);
          supplierItemCode = rs.getString(8);
          entry     = rs.getString(9);
          
          qtyD = generalUtils.doubleFromStr(qty);
          actualQty = goodsReceivedNote.getTotalReceivedForAPOLine(con, stmt2, rs2, thisCode, poLine);

          generalUtils.doubleToBytesCharFormat(actualQty, b, 0);
          generalUtils.formatNumeric(b, dpOnQuantities);
          actualQtyStr = generalUtils.stringFromBytes(b, 0L);

          if(actualQty < qtyD) // incomplete order
          {
            scoutln(out, bytesOut, "<tr><td><p>" + thisDate + "</td>");
            scoutln(out, bytesOut, "<td><p><a href=\"javascript:view('" + thisCode + "')\">" + thisCode + "</a></td>");
            scoutln(out, bytesOut, "<td align=center><p>"+ poLine + "</td>");
            scoutln(out, bytesOut, "<td align=center><p>"+ entry + "</td>");
            scoutln(out, bytesOut, "<td align=center><p>" + generalUtils.formatNumeric(qty, '0') + "</td>");
            scoutln(out, bytesOut, "<td align=center><p>" + actualQtyStr + "</td>");
            scoutln(out, bytesOut, "<td nowrap><p><input type=text name=a" + count + " value=\"0\" size=5 maxlength=10></td>");
            scoutln(out, bytesOut, "<td nowrap><p>" + itemCode + "</td>");
            scoutln(out, bytesOut, "<td nowrap><p>" + mfrCode + "</td>");
            scoutln(out, bytesOut, "<td nowrap><p>" + supplierItemCode + "</td>");
            scoutln(out, bytesOut, "<td nowrap><p><input type=text name=b" + count + " value=unknown size=14 maxlength=40></td>");
            scoutln(out, bytesOut, "<td nowrap><p> &nbsp; <input type=text name=c" + count + " value=unknown size=14 maxlength=40></td>");
            scoutln(out, bytesOut, "<td align=right nowrap><p>" + generalUtils.formatNumeric(unitPrice, '2') + "</td>");
            scoutln(out, bytesOut, "<td nowrap><p>" + store + "</td>");
            scoutln(out, bytesOut, "<td nowrap><p>" + desc + "</td></tr>");

            poCodesArg[0] += (thisCode + "\001");
            poLinesArg[0] += (poLine + "\001");

            ++count;
          }
        }
      }   
    
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
    System.out.println(e);
      try
      {
        if(rs   != null) rs.close();     
        if(stmt != null) stmt.close();
      }
      catch(Exception e2) { }
    }
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
