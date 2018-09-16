// =======================================================================================================================================================================================================
// System: ZaraStar Document: LR update header rec
// Module: LocalRequisitionHeaderUpdate.java
// Author: C.K.Harvey
// Copyright (c) 2001-08 Christopher Harvey. All Rights Reserved.
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
import java.net.*;
import java.io.*;
import java.sql.*;

public class LocalRequisitionHeaderUpdate extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ErrorValidation errorValidation = new ErrorValidation();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  LocalRequisition localRequisition = new LocalRequisition();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="", p3="", p4="";
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
      p3  = req.getParameter("p3"); // recData
      p4  = req.getParameter("p4"); // thisOp

      if(p3 == null) p3 = "";
      if(p4 == null) p4 = "";

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p2, p1.charAt(0), p3, p4, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "LocalRequisitionHeaderUpdate", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3078, bytesOut[0], 0, "ERR:" + p2);
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                    String codeStr, char cad, String p3, String thisOp, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 3077, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "3078", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3078, bytesOut[0], 0, "ACC:" + codeStr);
      if(con  != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "3078", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3078, bytesOut[0], 0, "SID:" + codeStr);
      if(con  != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    byte[] code = new byte[21];
    generalUtils.stringToBytes(codeStr, 0, code);

    byte[] newCode = new byte[21];
    byte[] recData = new byte[2000];

    if(thisOp.length() > 0 && thisOp.charAt(0) == 'X') // cancel
    {
      recData[0] = '\000';
      getRec(out, unm, sid, uty, men, den, dnm, bnm, codeStr, "", recData, cad, localDefnsDir);
    }
    else
    {
      int y = generalUtils.strToBytes(recData, p3);
      String[] retStr = new String[1];
      if(errorValidation.validatePageReturningStr(con, stmt, rs, "Date", retStr, localRequisition.getFieldNamesLR(), localRequisition.getFieldTypesLR(), localRequisition.getFieldStylesLR(), "lr",
                                        "Update Loan/Return Note Header", dnm, unm, recData, y, imagesDir, localDefnsDir, defnsDir)) // validated ok
      {
        switch(localRequisition.putHead(con, stmt, rs, code, unm, dnm, localDefnsDir, defnsDir, cad, recData, y, newCode))
        {
          case ' ' : // updated successfully
                     recData[0] = '\000';
                     getRec(out, unm, sid, uty, men, den, dnm, bnm, generalUtils.sanitise(generalUtils.stringFromBytes(newCode, 0L)), "", recData, 'A', localDefnsDir);
                     break;
          case 'X' : // already exists
                     errorValidation.formatErrMsg("Already Exists", retStr, imagesDir);
                     reGetRec(out, unm, sid, uty, men, den, dnm, bnm, codeStr, retStr[0], p3, cad, localDefnsDir);
                     break;
          case 'I' : // illegal code
                     errorValidation.formatErrMsg("Invalid Loan/Return Note Code", retStr, imagesDir);
                     reGetRec(out, unm, sid, uty, men, den, dnm, bnm, codeStr, retStr[0], p3, cad, localDefnsDir);
                     break;
          case 'N' : // no code specified
                     errorValidation.formatErrMsg("No Loan/Return Note Code Specified", retStr, imagesDir);
                     reGetRec(out, unm, sid, uty, men, den, dnm, bnm, codeStr, retStr[0], p3, cad, localDefnsDir);
                     break;
          case 'F' : // not updated
                     retStr[0]="";
                     reGetRec(out, unm, sid, uty, men, den, dnm, bnm, codeStr, retStr[0], p3, cad, localDefnsDir);
                     break;
        }
      }
      else reGetRec(out, unm, sid, uty, men, den, dnm, bnm, codeStr, retStr[0], p3,/*recData*/ cad, localDefnsDir);
    }

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 3078, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), codeStr);
    if(con  != null) con.close();
    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void getRec(PrintWriter out, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String code, String errStr,
                      byte[] dataAlready, char cad, String localDefnsDir) throws Exception
  {
    URL url = new URL("http://" + serverUtils.serverToCall("OFSA", localDefnsDir) + "/central/servlet/LocalRequisitionPage");

    HttpURLConnection uc = (HttpURLConnection)url.openConnection();
    uc.setDoInput(true);
    uc.setDoOutput(true);
    uc.setUseCaches(false);
    uc.setDefaultUseCaches(false);
    uc.setRequestProperty("Content-Type", "application/x-www-form.urlencoded");

    String s2 = "&unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1="
              + generalUtils.sanitise(code) + "&p2="  + cad + "&p3="  + generalUtils.sanitise(true, errStr) + "&p4="  + generalUtils.sanitise(dataAlready);

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
  private void reGetRec(PrintWriter out, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String code,
                        String errStr, String dataAlready, char cad, String localDefnsDir) throws Exception
  {
    URL url = new URL("http://" + serverUtils.serverToCall("OFSA", localDefnsDir) + "/central/servlet/LocalRequisitionEdit");

    HttpURLConnection uc = (HttpURLConnection)url.openConnection();
    uc.setDoInput(true);
    uc.setDoOutput(true);
    uc.setUseCaches(false);
    uc.setDefaultUseCaches(false);
    uc.setRequestProperty("Content-Type", "application/x-www-form.urlencoded");

    String s2 = "&unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1="
              + generalUtils.sanitise(code) + "&p2="  + cad + "&p3="  + generalUtils.sanitise(true, errStr) + "&p4="  + generalUtils.sanitise(dataAlready);

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
