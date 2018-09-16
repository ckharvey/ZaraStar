// =======================================================================================================================================================================================================
// System: ZaraStar: Fax: Send document (to fax server)
// Module: FaxSend.java
// Author: C.K.Harvey
// Copyright (c) 1999-2007 Christopher Harvey. All Rights Reserved.
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
import java.net.*;
import java.sql.*;
import java.util.*;

public class FaxSend extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  AdminPrintControlNetwork adminPrintControlNetwork = new AdminPrintControlNetwork();
  FaxUtils faxUtils = new FaxUtils();
  
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", docCode="", docType="", servlet="", option="", number="", person="",
           company="", subject="", coverSheet="", comments="", senderPhone="", senderFax="", senderName="", senderCompany="", text="", companyCode="",
           companyType="";

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
        if(name.equals("p1")) 
          docCode = value[0];
        else
        if(name.equals("p2"))
          docType = value[0];
        else
        if(name.equals("p3")) 
          servlet = value[0];
        else
        if(name.equals("p4"))
          option = value[0];
        else
        if(name.equals("number"))
          number = value[0];
        else
        if(name.equals("person"))
          person = value[0];
        else
        if(name.equals("company"))
          company = value[0];
        else
        if(name.equals("companyCode"))
          companyCode = value[0];
        else
        if(name.equals("companyType"))
          companyType = value[0];
        else
        if(name.equals("subject"))
          subject = value[0];
        else
        if(name.equals("coverSheet"))
          coverSheet = value[0];
        else
        if(name.equals("comments"))
          comments = value[0];
        else
        if(name.equals("senderPhone"))
          senderPhone = value[0];
        else
        if(name.equals("senderFax"))
          senderFax = value[0];
        else
        if(name.equals("senderName"))
          senderName = value[0];
        else
        if(name.equals("senderCompany"))
          senderCompany = value[0];
        else
        if(name.equals("text"))
          text = value[0];
      }  

      number        = generalUtils.stripAllNonNumeric(number);
      person        = generalUtils.stripLeadingAndTrailingSpaces(person);
      company       = generalUtils.stripLeadingAndTrailingSpaces(company);
      companyCode   = generalUtils.stripLeadingAndTrailingSpaces(companyCode);
      subject       = generalUtils.stripLeadingAndTrailingSpaces(subject);
      comments      = generalUtils.stripLeadingAndTrailingSpaces(comments);
      senderPhone   = generalUtils.stripLeadingAndTrailingSpaces(senderPhone);
      senderFax     = generalUtils.stripLeadingAndTrailingSpaces(senderFax);
      senderName    = generalUtils.stripLeadingAndTrailingSpaces(senderName);
      senderCompany = generalUtils.stripLeadingAndTrailingSpaces(senderCompany);
  
      doIt(out, req, res, unm, sid, uty, men, den, dnm, bnm, servlet, option, docCode, docType, number, person, company, companyCode, companyType, subject, coverSheet, comments, senderPhone, senderFax, senderName, senderCompany, text, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "FaxSend", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 11002, bytesOut[0], 0, "ERR:" + docCode);
      if(out != null) out.flush(); 
    }
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, HttpServletResponse res, String unm, String sid, String uty, String men,
                    String den, String dnm, String bnm, String servlet, String option, String docCode, String docType, String number, String person,
                    String company, String companyCode, String companyType, String subject, String coverSheet, String comments, String senderPhone,
                    String senderFax, String senderName, String senderCompany, String text, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;
    
    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "11002a", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 11002, bytesOut[0], 0, "SID:" + docCode);
      if(con  != null) con.close();
      if(out != null) out.flush(); 
      return;
    }
    
    String textForFaxedTable;
    
    // if fax is for a document
    if(docCode.length() > 0)
    {
      try
      {
        generate(out, unm, sid, uty, men, den, dnm, bnm, docType, servlet, docCode, option, bytesOut);
      }
      catch(Exception e) { System.out.println("11002a: generate: " + e);} 
      try
      {
        adminPrintControlNetwork.pprToPS(unm, dnm, "0.000", "", "");
      }
      catch(Exception e) { System.out.println("11002a: pprToPS: " + e);} 
    
      String s;
      RandomAccessFile fh = generalUtils.fileOpen("/Zara/" + dnm + "/Users/" + unm + "/" + dnm + "/Reports/0.000.ps");
      if(fh != null) // just-in-case
      {
        try
        {
          s = fh.readLine();
          while(s != null && s.length() > 0)
          {
            text += (s + "\n");
            s = fh.readLine();
          }
        }
        catch(Exception ioErr)
        {
          generalUtils.fileClose(fh);
        }
      }
      
      textForFaxedTable = "";
    }
    else textForFaxedTable = text;
    
    String[] faxCode = new String[1];
    
    String cs;
    if(coverSheet.equals("on"))
      cs = "Y";
    else cs = "N";

    if(faxUtils.addToFaxed(docCode, docType, number, person, company, companyCode, companyType, subject, cs, comments, textForFaxedTable, senderPhone, senderFax, senderName, senderCompany, unm, dnm, localDefnsDir, defnsDir, faxCode)) // just-in-case
    {
      sendToFaxServer(unm, sid, uty, men, den, dnm, bnm, faxCode[0], docCode, docType, number, person, company, subject, coverSheet, comments, senderPhone, senderFax, senderName, senderCompany, text, localDefnsDir);
    }
    
    display(out, unm, sid, uty, men, den, dnm, bnm, bytesOut);
    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 11002, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), docCode);
    if(con  != null) con.close();
    if(out != null) out.flush(); 
  }
   
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void display(PrintWriter out, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    URL url = new URL("http://" + men + "/central/servlet/FaxServices?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&dnm=" + dnm + "&bnm=" + bnm);

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
      bytesOut[0] += s.length();
      s = di.readLine();
    }

    di.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void sendToFaxServer(String unm, String sid, String uty, String men, String den, String dnm, String bnm, String faxCode, 
                               String docCode, String docType, String number, String person, String company, String subject, String coverSheet,
                               String comments, String senderPhone, String senderFax, String senderName, String senderCompany, String text,
                               String localDefnsDir) throws Exception
  {
    URL url = new URL("http://" + serverUtils.serverToCall("FAX", localDefnsDir) + "/central/servlet/FaxSendHylafax");

    HttpURLConnection uc = (HttpURLConnection)url.openConnection();
    uc.setDoInput(true);
    uc.setDoOutput(true);
    uc.setUseCaches(false);
    uc.setDefaultUseCaches(false);
    uc.setRequestProperty("Content-Type", "application/x-www-form.urlencoded");

    String s2 = "&unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&docCode=" + sanitise(docCode) + "&docType=" + docType + "&number=" + sanitise(number) + "&person=" + sanitise(person) + "&company="
              + sanitise(company)+ "&subject=" + sanitise(subject) + "&coverSheet=" + coverSheet + "&comments=" + sanitise(comments) + "&senderPhone=" + sanitise(senderPhone) + "&senderFax=" + sanitise(senderFax) + "&senderName=" + senderName
              + "&faxCode=" + faxCode + "&senderCompany=" + sanitise(senderCompany) + "&text=" + sanitise2(text) + "&bnm=" + bnm;

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
      s = di.readLine();
    }

    di.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String sanitise(String str)
  {
    String str2="";
    int x, len=str.length();
    for(x=0;x<len;++x)
    {
           if(str.charAt(x) == '#')  str2 += "%23";
      else if(str.charAt(x) == '\"') str2 += ""; // remove quotes
      else if(str.charAt(x) == '\'') str2 += "";  // remove quotes
      else if(str.charAt(x) == '&')  str2 += "%26";
      else if(str.charAt(x) == '%')  str2 += "%25";
      else if(str.charAt(x) == ' ')  str2 += "%20";
      else if(str.charAt(x) == '?')  str2 += "%3f";
      else if(str.charAt(x) == '+')  str2 += "%2b";
      else if(str.charAt(x) == '\n') str2 += '\003';
      else if(str.charAt(x) == '\r') ;
      else str2 += str.charAt(x);
    }
    return str2;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String sanitise2(String str)
  {
    String str2="";
    int x, len=str.length();
    for(x=0;x<len;++x)
    {
           if(str.charAt(x) == '#')  str2 += "%23";
      else if(str.charAt(x) == '\"') str2 += "``"; // remove quotes ... replaced by backtick
      else if(str.charAt(x) == '\'') str2 += "`";  // remove quotes ... replaced by backtick
      else if(str.charAt(x) == '&')  str2 += "%26";
      else if(str.charAt(x) == '%')  str2 += "%25";
      else if(str.charAt(x) == ' ')  str2 += "%20";
      else if(str.charAt(x) == '?')  str2 += "%3f";
      else if(str.charAt(x) == '+')  str2 += "%2b";
      else if(str.charAt(x) == '\n') str2 += '\003';
      else if(str.charAt(x) == '\r') ;
      else str2 += str.charAt(x);
    }
    return str2;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private String generate(PrintWriter out, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String docType, String servlet, String code, String option, int[] bytesOut) throws Exception
  {
    URL url;
    
    if(docType.equals("I") || docType.equals("C"))
    {
      url = new URL("http://" + men + "/central/servlet/" + servlet + "?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&uty="
                    + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=" + code + "&p2=" + option + "&p3=F&dnm=" + dnm + "&bnm=" + bnm);
    }
    else
    {
      url = new URL("http://" + men + "/central/servlet/" + servlet + "?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&uty="
                    + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=" + code + "&p2=" + option + "&dnm=" + dnm + "&bnm=" + bnm);
    }

    URLConnection uc = url.openConnection();
    uc.setDoInput(true);
    uc.setUseCaches(false);
    uc.setDefaultUseCaches(false);

    uc.setRequestProperty("Content-Type", "application/octet-stream");

    BufferedReader di = new BufferedReader(new InputStreamReader(uc.getInputStream()));

    String s = di.readLine();

    String numPages = s;
    while(s != null)
    {
      s = di.readLine();
    }

    di.close();
    
    return numPages;
  }

}
