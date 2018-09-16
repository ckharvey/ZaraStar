// =======================================================================================================================================================================================================
// System: ZaraStar Analytic: Print statements report  
// Module: StatementsReport.java
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
import java.sql.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class StatementsReport extends HttpServlet
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

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="", p3="", p4="", p6="", p7="";
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
      p1  = req.getParameter("p1");  // codeFrom (or, nameFrom)
      p2  = req.getParameter("p2");  // codeTo (or, nameTo)
      p3  = req.getParameter("p3");  // fileName
      p4  = req.getParameter("p4");  // type
      p6  = req.getParameter("p6");  // ignore no transactions: Y or N
      p7  = req.getParameter("p7");  // ignore zero balance: Y or N

      if(p1  == null) p1  = "";
      if(p2  == null) p2  = "";
      if(p3  == null) p3  = "";
      if(p4  == null) p4  = "CODE";

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, p3, p4, p6, p7, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "StatementsReport", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1012, bytesOut[0], 0, "ERR:" + p3);
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den,
                    String dnm, String bnm, String p1, String p2, String p3, String p4, String p6, String p7,
                    int[] bytesOut) throws Exception
   {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String reportsDir       = directoryUtils.getUserDir('R', dnm, unm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 1012, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "1012d", imagesDir, localDefnsDir,
                         defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1012, bytesOut[0], 0, "ACC:" + p3);
      if(con  != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "1012d", imagesDir, localDefnsDir,
                         defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1012, bytesOut[0], 0, "SID:" + p3);
      if(con  != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    boolean ignoreZeroBalance = false;
    boolean ignoreNoTransactions = false;
    
    if(p6.equals("Y"))
      ignoreNoTransactions = true;

    if(p7.equals("Y"))
      ignoreZeroBalance = true;
      
    if(p1.length() == 0) // codeOrNameFrom
    {
      if(p4.equals("CODE"))
        p1 = "000000000000000";
      else p1 = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
    }
    
    if(p2.length() == 0) // codeOrNameTo
    {
      if(p4.equals("CODE"))
        p2 = "999999999999999";
      else p2 = "zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz";
    }
        
    RandomAccessFile fhO, fhI;
    
    if((fhO = generalUtils.create(reportsDir + "0.000")) == null)
      return;

    if((fhI = generalUtils.fileOpen(reportsDir + p3)) == null)
      return;

    int numPages = parse(fhI, fhO, p4, ignoreNoTransactions, ignoreZeroBalance, p1, p2, unm, dnm, bnm, localDefnsDir, defnsDir);

    print(out, unm, sid, uty, men, den, dnm, bnm, generalUtils.intToStr(numPages), localDefnsDir);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 1012, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p3);
    if(con  != null) con.close();
    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private int parse(RandomAccessFile fhI, RandomAccessFile fhO, String type, boolean ignoreNoTransactions, boolean ignoreZeroBalance,
                    String codeOrNameFrom, String codeOrNameTo, String unm, String dnm, String bnm, String localDefnsDir, String defnsDir)
  {
    String thisPage = "", custCode="", currCustCode="", currCustCodeSave="";
    boolean wanted = true, steppedOn, notFound;
    int numPages = 0;
    long saveCurrPosn;

    try
    {
      fhI.seek(0);

      String line = getNextLine(fhI);

      while(line.length() != 0)
      {
        if(line.startsWith("M:0BALANCE"))
        {
          if(ignoreZeroBalance)
            wanted = false;
        }
        else
        if(line.startsWith("M:NOTRANS"))
        {
          if(ignoreNoTransactions)
            wanted = false;
        }
        else
        if(line.startsWith("M:"))
        {
          currCustCode = line.substring(2);
        }
        else
        if(line.startsWith("E:")) // next page found
        {
          // Need to know if next page is second (or subsequent) page of this SoA, or the next customer's SoA; only output to new file iff is new SoA.
          // Look for the 'M:' entry (it may not be on the next line)
          steppedOn = false;
          saveCurrPosn = fhI.getFilePointer();
          notFound = true;
          while(notFound)
          {
            line = getNextLine(fhI);
            if(line.length() == 0) // eof
            {
              steppedOn = true;
              notFound = false;
              fhI.seek(saveCurrPosn);
            }
            else
            {
              if(line.startsWith("M:"))
              {
                custCode = line.substring(2);
                if(! currCustCode.equals(custCode))
                {
                  steppedOn = true;
                  currCustCodeSave = currCustCode;
                  currCustCode = custCode;
                  notFound = false;
                  fhI.seek(saveCurrPosn);
                }
                else
                {
                  notFound = false;
                  fhI.seek(saveCurrPosn);
               }
              }
            }
          }  
          
          if(steppedOn)
          {
            if(wanted)
            {
              // check whether is range (code or name) has been stated
              if(codeOrNameFrom.equalsIgnoreCase(codeOrNameTo))
              {
                if((currCustCodeSave.toUpperCase()).startsWith(codeOrNameFrom.toUpperCase()))
                {
                  fhO.writeBytes(thisPage);
                  fhO.writeBytes("E:\n");
                  ++numPages;
                }
              }  
              else
              if(currCustCodeSave.compareToIgnoreCase(codeOrNameFrom) >= 0 && currCustCodeSave.compareToIgnoreCase(codeOrNameTo) <=  0)
              {
                fhO.writeBytes(thisPage);
                fhO.writeBytes("E:\n");
                ++numPages;
              }
            }
            else wanted = true; // for next time

            thisPage = "M:" + currCustCode + "\n";
          }
          else
          {
            thisPage += "E:\n";          
            thisPage += "M:" + currCustCode + "\n";
          }
        }
        else
        if(line.startsWith("S:"))
        {
          fhO.writeBytes(line + "\n");
        }
        else
        {
          thisPage += (line + "\n");
        }

        line = getNextLine(fhI);
      }

      if(wanted)
      {
        fhO.writeBytes(thisPage);
        fhO.writeBytes(line + "\n");
        thisPage = "";
      }
      
      ++numPages;

      generalUtils.fileClose(fhI);
      generalUtils.fileClose(fhO);
    }
    catch(Exception e) { }

    return numPages;
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------
  private String getNextLine(RandomAccessFile fh)
  {
    byte b;
    String str="";

    try
    {
      long curr = fh.getFilePointer();
      long high = fh.length();

      if(curr == high)
        return "";

      fh.seek(curr);

      b = fh.readByte();

      while(b == (byte)';') // comment line
      {
        while(curr < high && b != (byte)10 && b != (byte)13 && b != (byte)26)
        {
          b = fh.readByte();
          ++curr;
        }

        while(b == (byte)10 || b == (byte)13 || b == (byte)26)
        {
          try
          {
            b = fh.readByte();
          }
          catch(Exception exEof)
          {
            return str;
          }
          ++curr;
        }
        if(b == (byte)26)
          return str;
      }

      while(curr < high)
      {
        if(b == (byte)10 || b == (byte)13 || b == (byte)26)
        {
          while(b == (byte)10 || b == (byte)13 || b == (byte)26)
          {
            try
            {
              b = fh.readByte();
            }
            catch(Exception exEof2)
            {
              return str;
            }
          }

          if(b == (byte)26)
           ; // --x;
          else fh.seek(fh.getFilePointer() - 1);

          return str;
        }

        str += (char)b;
        ++curr;

        try
        {
          b = fh.readByte();
        }
        catch(Exception exEof2)
        {
          return str;
        }
      }
    }

    catch(Exception e)
    {
      return str;
    }

    return str;
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void print(PrintWriter out, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String numPages,
                     String localDefnsDir) throws Exception
  {
    String s2 = "?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
              + "&p1=&p2=&p3=&p4=" + numPages;

    URL url = new URL("http://" + serverUtils.serverToCall("OFSA", localDefnsDir) + "/central/servlet/AdminPrintControl" + s2);

    HttpURLConnection uc = (HttpURLConnection)url.openConnection();
    uc.setDoInput(true);
    uc.setDoOutput(true);
    uc.setUseCaches(false);
    uc.setDefaultUseCaches(false);
    uc.setRequestProperty("Content-Type", "application/x-www-form.urlencoded");

    uc.setRequestMethod("POST");

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
