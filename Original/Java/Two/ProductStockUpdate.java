// =======================================================================================================================================================================================================
// System: ZaraStar ProductEngine: Update stock file
// Module: ProductStockUpdate.java
// Author: C.K.Harvey
// Copyright (c) 1998-2007 Christopher Harvey. All Rights Reserved.
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
import java.net.*;
import java.io.*;
import java.sql.*;

public class ProductStockUpdate extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ErrorValidation errorValidation = new ErrorValidation();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  Inventory inventory = new Inventory();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", code="", cad="", saveStr="", thisOp="";
    boolean statusFound = false;

    try
    {
      out = res.getWriter();
      directoryUtils.setContentHeaders(res);

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
          cad = value[0];
        else
        if(name.equals("p2"))
          code = value[0];
        else
        if(name.equals("p4"))
          thisOp = value[0];
        else
        if(name.length() > 2 && name.substring(1, 3).equals(".x"))
          thisOp = "" + name.charAt(0);
        else
        if(name.length() > 2 && name.substring(1, 3).equals(".y"))
          ;
        else
        if(name.equalsIgnoreCase("Status"))
        {
          saveStr += ("stock.status=C\001");
          statusFound = true;
        }
        else saveStr += ("stock." + name + "=" + value[0] + "\001");
      }      

      if(! statusFound)
        saveStr += ("stock.status=L\001");

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, code, cad.charAt(0), saveStr, thisOp, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "ProductStockUpdate", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3005, bytesOut[0], 0, "ERR:" + code);
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men,
                    String den, String dnm, String bnm, String codeStr, char cad, String p3, String thisOp, int[] bytesOut)
                    throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;
    
    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 3004, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "ProductStockUpdate", imagesDir, localDefnsDir,
                         defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3005, bytesOut[0], 0, "ACC:" + codeStr);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }
    
    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "ProductStockUpdate", imagesDir, localDefnsDir,
                         defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3005, bytesOut[0], 0, "SID:" + codeStr);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    byte[] code = new byte[21];
    generalUtils.stringToBytes(codeStr, 0, code);

    byte[] newCode = new byte[21];

    if(thisOp.length() > 0 && thisOp.charAt(0) == 'X') // cancel
      getRec(out, codeStr, unm, sid, uty, men, den, dnm, bnm, localDefnsDir);
    else
    {
      byte[] recData = new byte[2000];
      int y = generalUtils.strToBytes(recData, p3);
      String[] retStr = new String[1];
      if(errorValidation.validatePageReturningStr(con, stmt, rs, "", retStr, inventory.getFieldNamesStock(), inventory.getFieldTypesStock(), inventory.getFieldStylesStock(),
                                         "stock", "Update Stock Record", dnm, unm, recData, y, imagesDir, localDefnsDir,
                                         defnsDir)) // validated ok
      {
        switch(inventory.stockPutRec(con, stmt, rs, req, code, unm, uty, dnm, localDefnsDir, defnsDir, cad, recData, y, newCode))
        {
          case ' ' : // updated successfully
                      getRec(out, generalUtils.sanitise(generalUtils.stringFromBytes(newCode, 0L)), unm, sid, uty, men, den, dnm, bnm,
                             localDefnsDir);
                      break;
          case 'X' : // already exists
                      errorValidation.formatErrMsg("Already Exists", retStr, imagesDir);
                      reGetRec(out, codeStr, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, retStr[0], recData,
                               cad);
                      break;
          case 'I' : // illegal code
                      errorValidation.formatErrMsg("Illegal Stock Code", retStr, imagesDir);
                      reGetRec(out, codeStr, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, retStr[0], recData,
                               cad);
                      break;
          case 'N' : // no code specified
                      errorValidation.formatErrMsg("No Stock Code Specified", retStr, imagesDir);
                      reGetRec(out, codeStr, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, retStr[0], recData,
                               cad);
                      break;
          case 'F' : // not updated
                      errorValidation.formatErrMsg("Not Updated - Contact Support", retStr, imagesDir);
                      reGetRec(out, codeStr, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, retStr[0], recData,
                               cad);
                      break;
        }
      }
      else reGetRec(out, codeStr, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, retStr[0], recData, cad);
    }

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 3005, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), codeStr);
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void getRec(PrintWriter out, String code, String unm, String sid, String uty, String men, String den,
                      String dnm, String bnm, String localDefnsDir) throws Exception
  {
    URL url = new URL("http://" + serverUtils.serverToCall("OFSA", localDefnsDir) + "/central/servlet/ProductStockRecord?unm=" + unm  + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=" + code
                      + "&p2=A&bnm=" + bnm);

    URLConnection uc = url.openConnection();
    uc.setDoInput(true);
    uc.setUseCaches(false);
    uc.setDefaultUseCaches(false);

    uc.setRequestProperty("Content-Type", "application/octet-stream");

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
  private void reGetRec(PrintWriter out, String code, String unm, String sid, String uty, String men, String den, 
                        String dnm, String bnm, String localDefnsDir, String errStr, byte[] dataAlready, char cad) throws Exception
  {
    String code2        = generalUtils.sanitise(code);
    String errStr2      = generalUtils.sanitise(true, errStr);
    String dataAlready2 = generalUtils.sanitise(dataAlready);
    
    URL url = new URL("http://" + serverUtils.serverToCall("OFSA", localDefnsDir) + "/central/servlet/ProductStockEdit?unm=" + unm  + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=" + code2 + "&p2="
                      + errStr2 + "&p3=" + dataAlready2 + "&p4=" + cad + "&bnm=" + bnm);

    URLConnection uc = url.openConnection();
    uc.setDoInput(true);
    uc.setUseCaches(false);
    uc.setDefaultUseCaches(false);

    uc.setRequestProperty("Content-Type", "application/octet-stream");

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
