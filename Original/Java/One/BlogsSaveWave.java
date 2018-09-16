// =======================================================================================================================================================================================================
// System: ZaraStar Blogs: Save
// Module: BlogsSave.java
// Author: C.K.Harvey
// Copyright (c) 2006-09 Christopher Harvey. All Rights Reserved.
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
import java.net.*;
import java.util.Enumeration;

public class BlogsSaveWave extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  BlogsUtils blogsUtils = new BlogsUtils();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", code="", date="", text="", title="", type="", topic="", image="", callingServlet="", stay="", published="", isAMenuItem="", isTheHomePage="", isASpecial="", shortTitle="",
           displayTheTitle="", serviceCode="", isSharable="", owner="";

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
        if(name.equals("code"))
          code = value[0];
        else
        if(name.equals("date"))
          date = value[0];
        else
        if(name.equals("p1"))
          text = value[0];
        else
        if(name.equals("title"))
          title = value[0];
        else
        if(name.equals("type"))
          type = value[0];
        else
        if(name.equals("topic"))
          topic = value[0];
        else
        if(name.equals("image"))
          image = value[0];
        else
        if(name.equals("published"))
          published = value[0];
        else
        if(name.equals("isAMenuItem"))
          isAMenuItem = value[0];
        else
        if(name.equals("isTheHomePage"))
          isTheHomePage = value[0];
        else
        if(name.equals("isASpecial"))
          isASpecial = value[0];
        else
        if(name.equals("shortTitle"))
          shortTitle = value[0];
        else
        if(name.equals("displayTheTitle"))
          displayTheTitle = value[0];
        else
        if(name.equals("serviceCode"))
          serviceCode = value[0];
        else
        if(name.equals("isSharable"))
          isSharable = value[0];
        else
        if(name.equals("owner"))
          owner = value[0];
        else
        if(name.equals("callingServlet"))
          callingServlet = value[0];
        else
        if(name.equals("stay"))
          stay = value[0];
      }

      if(code  == null) code  = "";
      if(text  == null) text  = "";
      if(title == null) title = "";
      if(topic == null) topic = "";
      if(image == null) image = "";
      if(type  == null || type.length() == 0) type  = "0"; // raw HTML

      doIt(out, req, stay, callingServlet, unm, sid, uty, men, den, dnm, bnm, code, date, title, type, topic, text, image, published, isAMenuItem, isTheHomePage, isASpecial, shortTitle, displayTheTitle, serviceCode, isSharable, owner, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "BlogsSave", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8103, bytesOut[0], 0, "ERR:" + code);
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String stay, String callingServlet, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String code, String date, String title, String type,
                    String topic, String text, String image, String published, String isAMenuItem, String isTheHomePage, String isASpecial, String shortTitle, String displayTheTitle, String serviceCode, String isSharable, String owner,
                    int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 8102, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "BlogsSave", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8103, bytesOut[0], 0, "ACC:" + code);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "BlogsSave", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8103, bytesOut[0], 0, "SID:" + code);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    set(out, stay, callingServlet, owner, unm, sid, uty, men, den, dnm, bnm, code, date, title, type, topic, text, image, published, isAMenuItem, isTheHomePage, isASpecial, shortTitle, displayTheTitle, serviceCode, isSharable, localDefnsDir);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 8103, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), code);
    if(con  != null) con.close();
    if(out != null) out.flush();
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(PrintWriter out, String stay, String callingServlet, String owner, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String code, String date, String title, String type, String topic,
                   String text, String image, String published, String isAMenuItem, String isTheHomePage, String isASpecial, String shortTitle, String displayTheTitle, String serviceCode, String isSharable, String localDefnsDir) throws Exception
  {
    String[] newCode = new String[1];  newCode[0] = code;

    String t1;
    if(type.equals("0")) // raw html
      t1 = text;
    else t1 = generalUtils.replaceNewlinesWithSpaces(text);

    if(published.equals("on"))       published = "Y";       else published       = "N";
    if(isAMenuItem.equals("on"))     isAMenuItem = "Y";     else isAMenuItem     = "N";
    if(isTheHomePage.equals("on"))   isTheHomePage = "Y";   else isTheHomePage   = "N";
    if(isASpecial.equals("on"))      isASpecial = "Y";      else isASpecial      = "N";
    if(displayTheTitle.equals("on")) displayTheTitle = "Y"; else displayTheTitle = "N";
    if(isSharable.equals("on"))      isSharable = "Y";      else isSharable      = "N";

    if(owner == null || owner.length() == 0)
      owner = unm;

    blogsUtils.add(dnm, code, owner, date, title, type, image, topic, t1, published, isAMenuItem, isTheHomePage, isASpecial, shortTitle, displayTheTitle, serviceCode, isSharable, newCode);

    if(stay.equals("Y"))
    {
      refetch(out, callingServlet, unm, sid, uty, men, den, dnm, bnm, newCode[0], localDefnsDir);
    }
    else
    {
      display(out, unm, sid, uty, men, den, dnm, bnm, date, topic, localDefnsDir);
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void refetch(PrintWriter out, String servlet, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String code, String localDefnsDir) throws Exception
  {
    URL url = new URL("http://" + serverUtils.serverToCall("INFO", localDefnsDir) + "/central/servlet/" + servlet + "?unm=" + unm  + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=" + code + "&bnm=" + bnm);

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

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void display(PrintWriter out, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String date, String topic, String localDefnsDir) throws Exception
  {
    URL url = new URL("http://" + serverUtils.serverToCall("INFO", localDefnsDir) + "/central/servlet/BlogsDisplayWave?unm=" + unm  + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=" + unm + "&topic=" + generalUtils.sanitise(topic)
                   + "&date=" + generalUtils.sanitise(date) + "&bnm=" + bnm);

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
