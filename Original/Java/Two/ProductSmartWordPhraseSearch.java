// =======================================================================================================================================================================================================
// System: ZaraStar: Products: Smart Word Phrase Search
// Module: ProductSmartWordPhraseSearch.java
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
import java.io.*;
import java.net.*;
import java.sql.*;

public class ProductSmartWordPhraseSearch extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  Profile profile = new Profile();
  
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p3="", p4="", p5="";

    try
    {
      directoryUtils.setContentHeaders2(res);
      out = res.getWriter();

      unm = req.getParameter("unm");
      sid = req.getParameter("sid");      
      uty = req.getParameter("uty");
      dnm = req.getParameter("dnm");
      men = req.getParameter("men");
      den = req.getParameter("den");
      bnm = req.getParameter("bnm");
      p1  = req.getParameter("p1"); // phrase
      p3  = req.getParameter("p3"); // searchInHouseStock
      p4  = req.getParameter("p4"); // searchBuyers
      p5  = req.getParameter("p5"); // searchCatalogs
      
      if(p1 == null) p1 = ""; 
      if(p3 == null) p3 = "Y";
      if(p4 == null) p4 = "N";
 if(p5==null) p5="N";

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p3, p4, p5, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "ProductSmartWordPhraseSearch", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2013, bytesOut[0], 0, "ERR:" + p1);
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                    String p1, String p3, String p4, String p5, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String workingDir       = directoryUtils.getUserDir('W', dnm, unm);
    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 2013, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "ProductSmartWordPhraseSearch", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2013, bytesOut[0], 0, "ACC:" + p1);
      if(con  != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "ProductSmartWordPhraseSearch", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2013, bytesOut[0], 0, "SID:" + p1);
      if(con  != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    // create tmp searchresults file here on web server... necessary if we are to search 'local' and SC databases
    RandomAccessFile fh = generalUtils.create(workingDir + "searchresults.txt");
    if((fh = generalUtils.fileOpenD("searchresults.txt", workingDir)) == null) // just-in-case
      return;

    RandomAccessFile fhi = generalUtils.create(workingDir + "resultsoffsets.inx");
    if((fhi = generalUtils.fileOpenD("resultsoffsets.inx", workingDir)) == null) // just-in-case
      return;

    int[]    count = new int[1];    count[0] = 0;
    double[] time  = new double[1]; time[0]  = 0.0;
    
    int numPages=0;

    if(p3.charAt(0) == 'Y') // searchInHouseStock
      numPages = searchStock(fh, fhi, p1, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, count, time);

    if(p4.charAt(0) == 'Y') // searchBuyers
    {
      String[] customerCode = new String[1];
      int i = unm.indexOf("_");
      
      profile.getExternalAccessNameCustomerCode(con, stmt, rs, unm.substring(0, i), dnm, localDefnsDir, defnsDir, customerCode);
      
      numPages += searchBuyers(fh, fhi, customerCode[0], p1, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, count, time);
    }

    generalUtils.fileClose(fh);
    generalUtils.fileClose(fhi);

    displayFirstPage(out, count[0], time[0], numPages, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, p1);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 2013, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con  != null) con.close();
    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private int searchStock(RandomAccessFile fh, RandomAccessFile fhi, String phrase, String unm, String sid, String uty, String men, String den,
                          String dnm, String bnm, String localDefnsDir, int[] count, double[] time) throws Exception
  {
    URL url = new URL("http://" + serverUtils.serverToCall("OFSA", localDefnsDir) + "/central/servlet/ProductSmartSearchStock?unm=" + unm + "&sid=" + sid + "&uty=" + uty
                    + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=" + generalUtils.sanitise(phrase) + "&p2=Stock%20G%20" //+ "&p3=" + searchType
                    + "&bnm=" + bnm);
                      
    URLConnection uc = url.openConnection();
    uc.setDoInput(true);
    uc.setUseCaches(false);
    uc.setDefaultUseCaches(false);

    uc.setRequestProperty("Content-Type", "application/octet-stream");

    BufferedReader di = new BufferedReader(new InputStreamReader(uc.getInputStream()));

    int c=count[0], offset, numPages=0;
    String s = di.readLine();
    while(s != null)
    {
      if(s.startsWith("COUNTANDTIME"))
      {
        int x=13;
        int len = s.length();
        String t="";
        while(x < len && s.charAt(x) != ' ')
          t += s.charAt(x++);
        count[0] += generalUtils.strToInt(t);

        t="";
        ++x;
        while(x < len)
          t += s.charAt(x++);
        time[0] += generalUtils.doubleFromStr(t);
      }
      else
      {
        if(c == ((c / 20) * 20))
        {
          offset = (int)fh.getFilePointer();
          fh.writeBytes(s + "\n");
          fhi.writeInt(offset);
          ++numPages;
        }
        else fh.writeBytes(s + "\n");

        ++c;
      }

      s = di.readLine();
    }

    di.close();

    return numPages;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private int searchBuyers(RandomAccessFile fh, RandomAccessFile fhi, String customerCode, String phrase, String unm, String sid, String uty,
                           String men, String den, String dnm, String bnm, String localDefnsDir, int[] count, double[] time) throws Exception
  {
    URL url = new URL("http://" + serverUtils.serverToCall("OFSA", localDefnsDir) + "/central/servlet/ProductSmartSearchBuyers?unm=" + unm + "&sid=" + sid + "&uty=" + uty
                    + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=" + generalUtils.sanitise(phrase) + "&p2=" + customerCode + "&bnm=" + bnm);

    URLConnection uc = url.openConnection();
    uc.setDoInput(true);
    uc.setUseCaches(false);
    uc.setDefaultUseCaches(false);

    uc.setRequestProperty("Content-Type", "application/octet-stream");

    BufferedReader di = new BufferedReader(new InputStreamReader(uc.getInputStream()));

    int c=count[0], offset, numPages=0;
    String s = di.readLine();
    while(s != null)
    {
      if(s.startsWith("COUNTANDTIME"))
      {
        int x=13;
        int len = s.length();
        String t="";
        while(x < len && s.charAt(x) != ' ')
          t += s.charAt(x++);
        count[0] += generalUtils.strToInt(t);

        t="";
        ++x;
        while(x < len)
          t += s.charAt(x++);
        time[0] += generalUtils.doubleFromStr(t);
      }
      else
      {
        if(c == ((c / 20) * 20))
        {
          offset = (int)fh.getFilePointer();
          fh.writeBytes(s + "\n");
          fhi.writeInt(offset);
          ++numPages;
        }
        else fh.writeBytes(s + "\n");

        ++c;
      }

      s = di.readLine();
    }

    di.close();

    return numPages;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void displayFirstPage(PrintWriter out, int count, double time, int numPages, String unm, String sid, String uty, String men, String den,
                                String dnm, String bnm, String localDefnsDir, String p1) throws Exception
  {
    URL url = new URL("http://" + serverUtils.serverToCall("OFSA", localDefnsDir) + "/central/servlet/ProductSmarSearchResults?unm=" + unm + "&sid=" + sid + "&uty=" + uty
                    + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=F&p3=" + generalUtils.intToStr(numPages) + "&p4=" + generalUtils.intToStr(count)
                    + "&p6=" + generalUtils.doubleToStr(time) + "&p7=" + generalUtils.sanitise(p1) + "&bnm=" + bnm);

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
