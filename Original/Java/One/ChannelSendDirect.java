// =======================================================================================================================================================================================================
// System: ZaraStar Utils: Send direct
// Module: ChannelSendDirect.java
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
import java.sql.*;
import java.net.*;

public class ChannelSendDirect extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  AdminUtils adminUtils = new AdminUtils();
  
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="", p3="";

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
      p1  = req.getParameter("p1"); // code
      p2  = req.getParameter("p2"); // desc
      p3  = req.getParameter("p3"); // msg

      if(p1 == null) p1 = "";
      if(p2 == null) p2 = "";
      if(p3 == null) p3 = "";
  
      doIt(out, req, res, unm, sid, uty, men, den, dnm, bnm, p1, p2, p3, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "ChannelSendDirect", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 12708, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush(); 
    }
  }
  
  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, HttpServletResponse res, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1, String p2, String p3, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 12708, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "ChannelSendDirect", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 12708, bytesOut[0], 0, "ACC:");
      
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "ChannelSendDirect", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 12708, bytesOut[0], 0, "SID:");
      
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    char docType = 'Q';
    
    String companyCode = "";
    
    switch(docType)
    {
      case 'Q' : Quotation quotation = new Quotation();
                 companyCode = quotation.getAQuoteFieldGivenCode(con, stmt, rs, "CompanyCode", p1);
                 break;
    }

    Customer customer = new Customer();    
    String zcn = customer.getACompanyFieldGivenCode(con, stmt, rs, "ZCN", companyCode);
    
    String link = " <a href=\"javascript:connect('" + dnm + "','" + unm + "','" + p1 + "','" + docType + "')\">Connect</a>";
 
    send(con, stmt, rs, out, req, p1, p2, p3 + link, zcn, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);
    
    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 12708, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void send(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String code, String desc, String msg, String zcn, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                    String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Channels - Send Direct</title>");
    
    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">"); 

    scoutln(out, bytesOut, "function send(user,zcn){");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/ChannelSendDirectSend?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p3=\"+user+\"&p4=\"+zcn+\"&p2=P&p1=" + generalUtils.sanitise(msg) + "&bnm=" + bnm + "\";}");
         
    scoutln(out, bytesOut, "function channel(user){");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/ChannelOpenChats?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=\"+user+\"&p2=P&bnm=" + bnm + "\";}");
    
    scoutln(out, bytesOut, "function sanitise(code){");
    scoutln(out, bytesOut, "var code2='';var x;var len=code.length;");
    scoutln(out, bytesOut, "for(x=0;x<len;++x)");
    scoutln(out, bytesOut, "if(code.charAt(x)=='#')code2+='%23';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='\"')code2+='%22';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='&')code2+='%26';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='%')code2+='%25';");
    scoutln(out, bytesOut, "else if(code.charAt(x)==' ')code2+='%20';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='+')code2+='%2b';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='?')code2+='%3F';");
    scoutln(out, bytesOut, "else code2+=code.charAt(x);");
    scoutln(out, bytesOut, "return code2};");

    scoutln(out, bytesOut, "</script>");

    int[] hmenuCount = new int[1];

    adminUtils.outputPageFrame(con, stmt, rs, out, req, "ChannelSendDirect", "", "12708", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount, bytesOut);

    adminUtils.drawTitle(con, stmt, rs, req, out, "Channels - Send Direct", "12708", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
  
    getZCNInfoFromCloud(out, zcn, unm, dnm, localDefnsDir);
    
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void getZCNInfoFromCloud(PrintWriter out, String zcn, String unm, String dnm, String localDefnsDir)
  {
    try
    {
      URL url = new URL("http://" + serverUtils.serverToCall("ZC", localDefnsDir) + "/central/servlet/ChannelSendDirectd?unm=" + unm + "&dnm=" + dnm + "&zcn=" + zcn);
                      
      URLConnection uc = url.openConnection();
      uc.setDoInput(true);
      uc.setUseCaches(false);
      uc.setDefaultUseCaches(false);

      uc.setRequestProperty("Content-Type", "application/octet-stream");

      BufferedReader di = new BufferedReader(new InputStreamReader(uc.getInputStream()));

      String s = di.readLine(), zpn, host;
      int x = 0, len = s.length();
      
      while(x < len)
      {
        zpn = "";
        while(x < len && s.charAt(x) != '\001') // just-in-case
          zpn += s.charAt(x++);
        ++x;

        host = "";
        while(x < len && s.charAt(x) != '\001') // just-in-case
          host += s.charAt(x++);
        ++x;
               
        getProfileInfoFromSite(out, host, zpn, unm, zcn);
      }
      
      di.close();
    }
    catch(Exception e) { }
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void getProfileInfoFromSite(PrintWriter out, String host, String zpn, String unm, String zcn)
  {
    try
    {
      if(! host.startsWith("http://"))
        host = "http://" + host;
      
      URL url = new URL(host + "/central/servlet/ChannelProfileInfo?unm=" + unm + "&men=" + host + "&dnm=" + zcn + "&p1=" + zpn);
                      
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
    catch(Exception e) { }
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
