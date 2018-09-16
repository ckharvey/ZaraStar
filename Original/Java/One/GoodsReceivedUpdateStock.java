// =======================================================================================================================================================================================================
// System: ZaraStar Document: GR update stock position
// Module: GoodsReceivedUpdateStock.java
// Author: C.K.Harvey
// Copyright (c) 2006-07 Christopher Harvey. All Rights Reserved.
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
import java.net.HttpURLConnection;
import java.net.URL;

public class GoodsReceivedUpdateStock extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  Supplier supplier = new Supplier();
  GoodsReceivedNote goodsReceivedNote = new GoodsReceivedNote();
  PurchaseOrder purchaseOrder = new PurchaseOrder();
  
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", code="", option="";
    
    try
    {
      directoryUtils.setContentHeaders(res);
      out = res.getWriter();
      
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
        if(name.length() > 2 && name.substring(1, 3).equals(".x")) ;
        else
        if(name.length() > 2 && name.substring(1, 3).equals(".y")) ;
        else
        if(name.equals("p1"))
          code = value[0];
        else
        if(name.equals("p2")) // S or T (stock or transit)
          option = value[0];
      }  

      doIt(out, req, code, option, unm, sid, uty, men, den, bnm, dnm, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "GoodsReceivedUpdateStock", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3037, bytesOut[0], 0, "ERR:" + code);
      if(out != null) out.flush(); 
    }
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String code, String option, String unm, String sid, String uty, String men, String den,
                    String bnm, String dnm, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String localDefnsDir  = directoryUtils.getLocalOverrideDir(dnm);
    String defnsDir          = directoryUtils.getSupportDirs('D');
    String imagesDir         = directoryUtils.getSupportDirs('I');

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null, stmt2 = null;
    ResultSet rs   = null, rs2   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 3037, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "GoodsReceivedUpdateStock", imagesDir, localDefnsDir,
                         defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3037, bytesOut[0], 0, "ACC:" + code);
    if(con  != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "GoodsReceivedUpdateStock", imagesDir, localDefnsDir,
                         defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3037, bytesOut[0], 0, "SID:" + code);
    if(con  != null) con.close();
      if(out != null) out.flush(); 
      return;
    }
        
    if(process(con, stmt, stmt2, rs, rs2, code, option, dnm, localDefnsDir, defnsDir))
      reGetRec(out, unm, sid, uty, men, den, dnm, bnm, code, localDefnsDir);    
    else messagePage.msgScreen(false, out, req, 23, unm, sid, uty, men, den, dnm, bnm, "GoodsReceivedUpdateStock", imagesDir, localDefnsDir, defnsDir, bytesOut);
    
    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 3037, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), code);
    if(con  != null) con.close();
    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean process(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String code, String option, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    byte[] codeB = new byte[21];
    generalUtils.strToBytes(codeB, code);
    
    String value;    
    if(option.equals("S")) // in-stock
      value = "Y";
    else value = "N";
    
    boolean res = goodsReceivedNote.updateAGRField(con, stmt, rs, codeB, "StockProcessed", value, dnm, localDefnsDir, defnsDir);
    
    if(res)
    {
      if(option.equals("S")) // in-stock
      {
        res = goodsReceivedNote.updateAGRField(con, stmt, rs, codeB, "DateStockProcessed", generalUtils.todaySQLFormat(localDefnsDir, defnsDir), dnm, localDefnsDir, defnsDir);

        byte[] list = new byte[1000];  list[0] = '\000';
        int[] listLen = new int[1];  listLen[0]  = 1000;

        int y, len;
        String soCode, salesPerson, companyName;

        eachGRNLine(con, stmt, stmt2, rs, rs2, code, list, listLen);

        int numEntries = generalUtils.countListEntries(list);
        byte[] entry = new byte[200];

        for(int x=0;x<numEntries;++x)
        {
          if(generalUtils.getListEntryByNum(x, list, entry)) // just-in-case
          {
            y = 0;
            len = generalUtils.lengthBytes(entry, 0);

            soCode = "";
            while(y < len && entry[y] != ':') // just-in-case
              soCode += (char)entry[y++];
            ++y;
            salesPerson = "";
            while(y < len && entry[y] != ':') // just-in-case
              salesPerson += (char)entry[y++];
            ++y;
            companyName = "";
            while(y < len)
              companyName += (char)entry[y++];

            sendNotifications(con, stmt, rs, salesPerson, code, soCode, companyName);
          }
        }
      }
      else
      {
        res = goodsReceivedNote.updateAGRField(con, stmt, rs, codeB, "DateStockProcessed", "1970-01-01", dnm, localDefnsDir, defnsDir);
      }  
    }
    
    return res;
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------
  private void reGetRec(PrintWriter out, String unm, String sid, String uty, String men, String den, String dnm,
                      String bnm, String code, String localDefnsDir) throws Exception
  {
    URL url = new URL("http://" + serverUtils.serverToCall("OFSA", localDefnsDir) + "/central/servlet/GoodsReceivedPage");

    HttpURLConnection uc = (HttpURLConnection)url.openConnection();
    uc.setDoInput(true);
    uc.setDoOutput(true);
    uc.setUseCaches(false);
    uc.setDefaultUseCaches(false);
    uc.setRequestProperty("Content-Type", "application/x-www-form.urlencoded");

    String s2 = "&unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm
              + "&bnm=" + bnm + "&p1="  + generalUtils.sanitise(code) + "&p2=A&p3=&p4=";

    Integer len = new Integer(s2.length());
    uc.setRequestProperty("Content-Length", len.toString());
    
    uc.setRequestMethod("POST");

    PrintWriter p = new PrintWriter(uc.getOutputStream());
    p.print(s2);    
    p.flush();
    p.close();

    BufferedReader di = new BufferedReader(new InputStreamReader(uc.getInputStream()));
    String s = di.readLine();
    while(s != null)
    {
      out.println(s);
      s = di.readLine();
    }

    di.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void eachGRNLine(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String grnCode, byte[] list, int[] listLen) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT POCode, POLine FROM grl WHERE GRCode = '" + grnCode + "'");

    String poCode, poLine, soCode;

    while(rs.next())
    {
      poCode = rs.getString(1);
      poLine = rs.getString(2);

      soCode = getSOCodeFromPOLine(con, stmt2, rs2, poCode, poLine);

      if(soCode.length() == 0)
        soCode = getSOCodeFromLPLine(con, stmt2, rs2, poCode, poLine);

      list = onSO(con, stmt2, rs2, soCode, list, listLen);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private byte[] onSO(Connection con, Statement stmt, ResultSet rs, String soCode, byte[] list, int[] listLen) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT SalesPerson, CompanyName FROM so WHERE SOCOde = '" + soCode + "' AND Status != 'C' AND AllSupplied != 'Y'");

    String salesPerson, companyName;
    byte[] b = new byte[200];

    if(rs.next())
    {
      salesPerson = rs.getString(1);
      companyName = rs.getString(2);

      generalUtils.strToBytes(b, soCode + ":" + salesPerson + ":" + companyName + "\001");
      list = generalUtils.addToList(false, b, list, listLen);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    return list;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private String getSOCodeFromPOLine(Connection con, Statement stmt, ResultSet rs, String poCode, String poLine) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT SOCode FROM pol WHERE POCode = '" + poCode + "' AND Line = '" + poLine + "'");

    String soCode = "";

    if(rs.next())
      soCode = rs.getString(1);

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    return soCode;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private String getSOCodeFromLPLine(Connection con, Statement stmt, ResultSet rs, String poCode, String poLine) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT SOCode FROM lpl WHERE LPCode = '" + poCode + "' AND Line = '" + poLine + "'");

    String soCode = "";

    if(rs.next())
      soCode = rs.getString(1);

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    return soCode;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void sendNotifications(Connection con, Statement stmt, ResultSet rs, String salesPerson, String grCode, String soCode, String companyName) throws Exception
  {
      if(1==1)return;
    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT UserCode FROM managepeople WHERE SalesPerson = '" + generalUtils.sanitiseForSQL(salesPerson) + "'");

      String userCode;

      while(rs.next())
      {
        userCode = rs.getString(1);

        // create Notification
      }
    }
    catch(Exception e)
    {
      System.out.println(e);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

}
