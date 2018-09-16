// =======================================================================================================================================================================================================
// System: ZaraStar Text: Save text
// Module: TextSaveText.java
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
import java.io.*;
import java.sql.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;

public class TextSaveText extends HttpServlet
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

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", textFile="", text="", directory="";

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
          text = value[0];
        else
        if(name.equals("textFile"))
          textFile = value[0];
        else
        if(name.equals("directory"))
          directory = value[0];
      }
 
      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, directory, textFile, text, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "TextSaveText", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6103, bytesOut[0], 0, "ERR:" + textFile);
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String directory, String textFile, String text, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String textsDir      = directoryUtils.getTextsDir(dnm);
      
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 6103, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "TextSaveText", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6103, bytesOut[0], 0, "ACC:" + textFile);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "TextSaveText", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6103, bytesOut[0], 0, "SID:" + textFile);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    set(out, req, unm, sid, uty, men, den, dnm, bnm, directory, textsDir + directory + "/" + textFile, text, localDefnsDir, defnsDir,
        bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 6103, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), textFile);
    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String directory, String textFile, String text, String localDefnsDir, String defnsDir, int[] bytesOut)
                   throws Exception
  {
    String t1 = generalUtils.replaceNewlinesWithSpaces(text);

    RandomAccessFile fh;

    if((fh = generalUtils.create(textFile)) != null)
    {
      String s;
      try
      {
        fh.writeBytes(t1);
      }
      catch(Exception ioErr)
      {
        generalUtils.fileClose(fh);
        return;
      }
    }
    else
    {
      return;
    }
    
    generalUtils.fileClose(fh);
    
    fetch(out, directory, unm, sid, uty, men, den, dnm, bnm, localDefnsDir);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void fetch(PrintWriter out, String directory, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir) throws Exception
  {
    URL url = new URL("http://" + serverUtils.serverToCall("TEXT", localDefnsDir) + "/central/servlet/TextListDirectory?unm=" + unm  + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=" + generalUtils.sanitise(directory) + "&bnm="
                    + bnm);

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
