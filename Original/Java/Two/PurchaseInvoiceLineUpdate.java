// =======================================================================================================================================================================================================
// System: ZaraStar DocumentEngine: Purchase Invoices update line rec
// Module: PurchaseInvoiceLineUpdate.java
// Author: C.K.Harvey
// Copyright (c) 2001-07 Christopher Harvey. All Rights Reserved.
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
import java.net.HttpURLConnection;
import java.net.URL;

public class PurchaseInvoiceLineUpdate extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ErrorValidation errorValidation = new ErrorValidation();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  PurchaseInvoice purchaseInvoice = new PurchaseInvoice();

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
      p1  = req.getParameter("p1"); // cad
      p2  = req.getParameter("p2"); // code
      p3  = req.getParameter("p3"); // line
      p4  = req.getParameter("p4"); // thisOp
      p5  = req.getParameter("p5"); // saveStr

      doIt(out, req, unm, sid, uty, dnm, men, den, bnm, p2, p3, p1.charAt(0), p4.charAt(0), p5, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "PurchaseInvoiceLineUpdate", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 5085, bytesOut[0], 0, "ERR:" + p2);
      if(out != null) out.flush();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String dnm,
                      String men, String den, String bnm, String code, String line, char cad, char thisOp, String saveStr,
                      int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;
    
    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 5084, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "5085", imagesDir, localDefnsDir,
                         defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 5085, bytesOut[0], 0, "ACC:" + code);
    if(con  != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "5085", imagesDir, localDefnsDir,
                         defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 5085, bytesOut[0], 0, "SID:" + code);
    if(con  != null) con.close();
      if(out != null) out.flush();
      return;
    }

    byte[] codeB = new byte[21];
    generalUtils.stringToBytes(code, 0, codeB);

    byte[] lineB = new byte[20];
    generalUtils.stringToBytes(line, 0, lineB);

    byte[] newCode = new byte[21];

    switch(thisOp)
    {
      case 'D' :  if(purchaseInvoice.deleteLine(con, stmt, rs, codeB, lineB, dnm, localDefnsDir, defnsDir, bytesOut))
                     getRec(out, unm, sid, uty, men, den, dnm, bnm, code, localDefnsDir);
                  else messagePage.msgScreen(false, out, req, 5, unm, sid, uty, men, den, dnm, bnm, "5085", imagesDir, localDefnsDir, defnsDir, bytesOut);
                  break;
      case 'X' :  // cancel
                  getRec(out, unm, sid, uty, men, den, dnm, bnm, code, localDefnsDir);
                  break;
      default  : // amend or create
                  byte[] recData = new byte[2000];
                  int y = generalUtils.strToBytes(recData, saveStr);
                  String[] retStr = new String[1];
                  if(errorValidation.validatePageReturningStr(con, stmt, rs, "", retStr, purchaseInvoice.getFieldNamesPurchaseInvoiceL(),
                                                    purchaseInvoice.getFieldTypesPurchaseInvoiceL(), purchaseInvoice.getFieldStylesPurchaseInvoiceL(),
                                                    "pinvoicel", "Update Purchase Invoice Record", dnm, unm, recData, y, imagesDir,
                                                    localDefnsDir, defnsDir))
                  {
                    switch(purchaseInvoice.putLine(con, stmt, rs, lineB, codeB, unm, dnm, localDefnsDir, defnsDir, cad, recData, y, newCode, bytesOut))
                    {
                      case ' ' : // updated successfully
                                 if(thisOp == 'S') // save & new
                                   newLine(out, code, unm, sid, uty, men, den, dnm, bnm, recData, localDefnsDir);
                                 else getRec(out, unm, sid, uty, men, den, dnm, bnm, code, localDefnsDir);
                                 break;
                      case 'X' : // already exists
                                 errorValidation.formatErrMsg("Already Exists", retStr, imagesDir);
                                 reGetRec(out, unm, sid, uty, men, den, dnm, bnm, code, line, retStr[0], recData, cad, localDefnsDir);
                                 break;
                      case 'I' : // illegal code
                                 errorValidation.formatErrMsg("Illegal Purchase Invoice Line", retStr, imagesDir);
                                 reGetRec(out, unm, sid, uty, men, den, dnm, bnm, code, line, retStr[0], recData, cad, localDefnsDir);
                                 break;
                      case 'N' : // no code specified
                                 errorValidation.formatErrMsg("No Purchase Invoice Line Specified", retStr, imagesDir);
                                 reGetRec(out, unm, sid, uty, men, den, dnm, bnm, code, line, retStr[0], recData, cad, localDefnsDir);
                                 break;
                      case 'F' : // not updated
                                 errorValidation.formatErrMsg("Not Updated - Contact Support", retStr, imagesDir);
                                 reGetRec(out, unm, sid, uty, men, den, dnm, bnm, code, line, retStr[0], recData, cad, localDefnsDir);
                                 break;
                   }
                 }
                 else reGetRec(out, unm, sid, uty, men, den, dnm, bnm, code, line, retStr[0], recData, cad, localDefnsDir);
    }

    int service;
    String operation = "";
    switch(thisOp)
    {
      case 'D' : service = 5086; operation = " Line: " + line;  break;
      case 'C' : service = 5085; operation = " Line: " + line;  break;
      case 'X' : service = 5085; operation = " Cancel";         break;
      default  : service = 5085; 
                 if(cad == 'A')
                   operation = " Amend";
                 else operation = " Create";
                 operation += (" Line: " + generalUtils.stringFromBytes(newCode, 0L));
                 break;
    }

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, service, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), code + operation);
    if(con  != null) con.close();
    if(out != null) out.flush();
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void getRec(PrintWriter out, String unm, String sid, String uty, String men, String den, String dnm,
                        String bnm, String code, String localDefnsDir) throws Exception
  {
    URL url = new URL("http://" + serverUtils.serverToCall("OFSA", localDefnsDir) + "/central/servlet/PurchaseInvoicePage");

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

  // -------------------------------------------------------------------------------------------------------------------------------
  private void reGetRec(PrintWriter out, String unm, String sid, String uty, String men, String den, String dnm,
                          String bnm, String code, String line, String errStr, byte[] dataAlready, char cad, String localDefnsDir)
                          throws Exception
  {
    URL url = new URL("http://" + serverUtils.serverToCall("OFSA", localDefnsDir) + "/central/servlet/PurchaseInvoiceLine");

    HttpURLConnection uc = (HttpURLConnection)url.openConnection();
    uc.setDoInput(true);
    uc.setDoOutput(true);
    uc.setUseCaches(false);
    uc.setDefaultUseCaches(false);
    uc.setRequestProperty("Content-Type", "application/x-www-form.urlencoded");

    String s2 = "&unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm
              + "&bnm=" + bnm + "&p1="  + generalUtils.sanitise(code) + "&p2="  + generalUtils.sanitise(line) + "&p3="  + generalUtils.sanitise(true, errStr)
              + "&p4="  + generalUtils.sanitise(dataAlready);

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
  
  // -------------------------------------------------------------------------------------------------------------------------------
  private void newLine(PrintWriter out, String code, String unm, String sid, String uty, String men, String den,
                         String dnm, String bnm, byte[] dataAlready, String localDefnsDir) throws Exception
  {
    URL url = new URL("http://" + serverUtils.serverToCall("OFSA", localDefnsDir) + "/central/servlet/PurchaseInvoiceLine");

    HttpURLConnection uc = (HttpURLConnection)url.openConnection();
    uc.setDoInput(true);
    uc.setDoOutput(true);
    uc.setUseCaches(false);
    uc.setDefaultUseCaches(false);
    uc.setRequestProperty("Content-Type", "application/x-www-form.urlencoded");

    String s2 = "&unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm
              + "&bnm=" + bnm + "&p1="  + generalUtils.sanitise(code) + "&p2=&p3=&p4=" + generalUtils.sanitise(dataAlready);

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

}
