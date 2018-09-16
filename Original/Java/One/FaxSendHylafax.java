// =======================================================================================================================================================================================================
// System: ZaraStar: Fax: Send document to Hylafax
// Module: FaxSendHylafax.java
// Author: C.K.Harvey
// Copyright (c) 1999-2009 Christopher Harvey. All Rights Reserved.
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

public class FaxSendHylafax extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", faxCode="", docCode="", number="", person="", company="", subject="", coverSheet="", comments="", senderPhone="", senderFax="", senderName="", senderCompany="",
           text="";

    try
    {
      directoryUtils.setContentHeaders(res);
      out = res.getWriter();
      
      int len = req.getContentLength();
      ServletInputStream in  = req.getInputStream();
      byte[] b = new byte[len];
      
      in.readLine(b, 0, len);          
          
      String name, value;
      int x=0;
      while(x < len)
      {
        ++x; // &
        name="";
        while(x < len && b[x] != '=')
          name += (char)b[x++];
        
        ++x; // =
        value="";

        while(x < len && b[x] != '&')
          value += (char)b[x++];
        value = generalUtils.deSanitise(value);

        if(name.equals("unm"))
          unm = value;
        else
        if(name.equals("sid"))
          sid = value;
        else
        if(name.equals("uty"))
          uty = value;
        else
        if(name.equals("men"))
          men = value;
        else
        if(name.equals("den"))
          den = value;
        else
        if(name.equals("dnm"))
          dnm = value;
        else
        if(name.equals("bnm"))
          bnm = value;
        else
        if(name.equals("number"))
          number = value;
        else
        if(name.equals("faxCode"))
          faxCode = value;
        else
        if(name.equals("person"))
          person = value;
        else
        if(name.equals("company"))
          company = value;
        else
        if(name.equals("subject"))
          subject = value;
        else
        if(name.equals("coverSheet"))
          coverSheet = value;
        else
        if(name.equals("comments"))
          comments = value;
        else
        if(name.equals("senderPhone"))
          senderPhone = value;
        else
        if(name.equals("senderFax"))
          senderFax = value;
        else
        if(name.equals("senderName"))
          senderName = value;
        else
        if(name.equals("senderCompany"))
          senderCompany = value;
        else
        if(name.equals("text"))
          text = value;
      }  

      number        = generalUtils.stripLeadingAndTrailingSpaces(number);
      person        = generalUtils.stripLeadingAndTrailingSpaces(person);
      company       = generalUtils.stripLeadingAndTrailingSpaces(company);
      subject       = generalUtils.stripLeadingAndTrailingSpaces(subject);
      comments      = generalUtils.stripLeadingAndTrailingSpaces(comments);
      senderPhone   = generalUtils.stripLeadingAndTrailingSpaces(senderPhone);
      senderFax     = generalUtils.stripLeadingAndTrailingSpaces(senderFax);
      senderName    = generalUtils.stripLeadingAndTrailingSpaces(senderName);
      senderCompany = generalUtils.stripLeadingAndTrailingSpaces(senderCompany);
  
      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, faxCode, docCode, number, person, company, subject, coverSheet, comments,  senderPhone, senderFax, senderName, senderCompany, text, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "FaxSendHylafax", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 11002, bytesOut[0], 0, "ERR:" + docCode);
      if(out != null) out.flush(); 
    }
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String faxCode, String docCode, String number, String person, String company, String subject,
                    String coverSheet, String comments, String senderPhone, String senderFax, String senderName, String senderCompany, String text, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String faxTmpDir     = directoryUtils.getFaxTmpDir();
    
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;
    
    // write text into tmp file and call sendfax.sh with that fileName
    long l = new java.util.Date().getTime();
    int ii = new Random(l).nextInt();
    if(ii < 0)
      ii *= -1;

    String fileName = faxTmpDir + ii;
    RandomAccessFile fh = generalUtils.create(fileName);
    if(fh != null) // just-in-case
    {
      fh.seek(0);
      fh.writeBytes(generalUtils.deSanitiseReplaceNewLines(generalUtils.deSanitise(text)));

      generalUtils.fileClose(fh);
    }  
    
    callSendfax(number, person, company, subject, coverSheet, generalUtils.deSanitiseReplaceNewLines(comments), senderPhone, senderFax, senderName, senderCompany, fileName, faxTmpDir);

    String s, id="";
    fh = generalUtils.fileOpen(fileName + ".id");
    if(fh != null) // just-in-case
    {
      try
      {
        s = fh.readLine();
        if(s.startsWith("request id is "))
        {
          int x=14;
          while(x < s.length() && s.charAt(x) != ' ') // just-in-case
            id += s.charAt(x++);
          
          generalUtils.fileClose(fh);
        }
      }
      catch(Exception ioErr)
      {
        generalUtils.fileClose(fh);
      }
    }

    // update DB rec with hylafaxID
    updateToDBServer(unm, sid, uty, men, den, dnm, bnm, faxCode, id, localDefnsDir);
    
    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 11002, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), docCode);
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void callSendfax(String number, String person, String company, String subject, String coverSheet, String comments, String senderPhone, String senderFax, String senderName, String senderCompany, String fileName, String faxDir)
                           throws Exception
  {
    Process p;
    BufferedReader reader;
    String currentLine;

    Runtime r = Runtime.getRuntime();

    if(coverSheet.equals("on"))
      coverSheet = "";
    else coverSheet = " -n";
  
    String commandArray = "/Zara/Support/Scripts/Fax/sendfax.sh -x \"" + company + "\" " + coverSheet + " -c \"" + comments + "\" -f \"" + senderName + "\" -r \"" + subject + "\" -U \"" + senderPhone + "\" -W \"" + senderFax + "\" -C \"" + faxDir
                        + "faxcover.ps\" -y \"" + senderCompany + "\" -d \"" + person + "@" + number + "\" " + fileName;

    p = r.exec(commandArray);

    p.waitFor();
    System.out.println("Process exit code is: " + p.exitValue());

    InputStreamReader isr = new InputStreamReader(p.getInputStream());
    reader = new BufferedReader(isr);

    String res="";
    while((currentLine = reader.readLine()) != null)
      res += currentLine;

    reader.close();
    isr.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void updateToDBServer(String unm, String sid, String uty, String men, String den, String dnm, String bnm, String faxCode,  String hylafaxID, String localDefnsDir) throws Exception
  {
    URL url = new URL("http://" + serverUtils.serverToCall("OFSA", localDefnsDir) + "/central/servlet/FaxSendUpdate");

    HttpURLConnection uc = (HttpURLConnection)url.openConnection();
    uc.setDoInput(true);
    uc.setDoOutput(true);
    uc.setUseCaches(false);
    uc.setDefaultUseCaches(false);
    uc.setRequestProperty("Content-Type", "application/x-www-form.urlencoded");

    String s2 = "&unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&FaxCode=" + faxCode + "&HylafaxID=" + hylafaxID + "&bnm=" + bnm;

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
      s = di.readLine();

    di.close();
  }

}
