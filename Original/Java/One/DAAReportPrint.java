// =======================================================================================================================================================================================================
// System: ZaraStar Analytic: Print DAA report  
// Module: DAAReportPrint.java
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

public class DAAReportPrint extends HttpServlet
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

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="";
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
      p1  = req.getParameter("p1");  // fileName

      if(p1  == null) p1  = "";

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "MainPageUtils4c", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1004, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm,
                    String bnm, String p1, int[] bytesOut) throws Exception
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

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 1004, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "1004c", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1004, bytesOut[0], 0, "ACC:" + p1);
      if(con  != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "1004c", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1004, bytesOut[0], 0, "SID:" + p1);
      if(con  != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    RandomAccessFile fhI;
    
    if((fhI = generalUtils.fileOpen(reportsDir + p1)) == null)
      return;

    int numPages = parse(fhI);
    
    generalUtils.copyFileToFile(reportsDir + p1, reportsDir + "0.000");

    print(out, unm, sid, uty, men, den, dnm, bnm, generalUtils.intToStr(numPages), localDefnsDir);
    
    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 1004, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con  != null) con.close();
    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private int parse(RandomAccessFile fhI) throws Exception
  {
    int numPages = 0;

    try
    {
      fhI.seek(0);

      String line = getNextLine(fhI);

      while(line.length() != 0)
      {
        if(line.startsWith("E:")) // next page found
          ++numPages;
        line = getNextLine(fhI);
      }

      ++numPages; // ?

      generalUtils.fileClose(fhI);
    }
    catch(Exception e) { }

    return numPages;
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
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
    String s2 = "?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=&p2=&p3=&p4=" + numPages;

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
