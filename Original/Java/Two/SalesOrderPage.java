// =======================================================================================================================================================================================================
// System: ZaraStar: DocumentEngine: SO Fetch rec page
// Module: SalesOrderPage.java
// Author: C.K.Harvey
// Copyright (c) 2001-07 Christopher Harvey. All Rights Reserved.
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

public class SalesOrderPage extends HttpServlet
{
  GeneralUtils  generalUtils = new GeneralUtils();
  MessagePage  messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils  directoryUtils = new DirectoryUtils();
  SalesOrder salesOrder = new SalesOrder();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="", p3="", p4="", p6="";

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
      p2  = req.getParameter("p2"); // cad
      p3  = req.getParameter("p3"); // errStr
      p4  = req.getParameter("p4"); // dataAlready
      p6  = req.getParameter("p6"); // plain or not

      if(p1 == null) p1 = "";
      if(p2 == null) p2 = "A";
      if(p3 == null) p3 = "";
      if(p4 == null) p4 = "";
      if(p6 == null) p6 = "N";

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, p3, p4, p6, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "SalesOrderPage", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 4031, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush();
    }
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="", p3="", p4="", p6="";
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
        if(b[x] == '&' || b[x] == '?')
          ++x; // & or ?
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
        if(name.equals("p1")) // code
          p1 = value;
        else
        if(name.equals("p2")) // cad
          p2 = value;
        else
        if(name.equals("p3")) // errStr
          p3 = value;
        else
        if(name.equals("p4")) // dataAlready
          p4 = value;
        else
        if(name.equals("p6")) // plain or not
          p6 = value;
      }
      
      if(p1 == null) p1 = "";
      if(p2 == null) p2 = "A";
      if(p3 == null) p3 = "";
      if(p4 == null) p4 = "";
      if(p6 == null) p6 = "N";

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, p3, p4, p6, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "SalesOrderPage", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 4031, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush();
    }
  }
 
  // -------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men,
                      String den, String dnm, String bnm, String p1, String p2, String p3, String p4, String p6, int[] bytesOut)
                      throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');

    Connection con = null;
    Statement stmt = null, stmt2 = null;
    ResultSet rs   = null, rs2   = null;

    try
    {
      String uName = directoryUtils.getMySQLUserName();
      String pWord = directoryUtils.getMySQLPassWord();
      Class.forName("com.mysql.jdbc.Driver").newInstance();
      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + uName + "&password=" + pWord);

      if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 4031, unm, uty, dnm, localDefnsDir, defnsDir))
      {
        messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "4031", imagesDir, localDefnsDir, defnsDir, bytesOut);
        serverUtils.etotalBytes(req, unm, dnm, 4031, bytesOut[0], 0, "ACC:" + p1);
        if(con  != null) con.close();    
        if(out != null) out.flush();
        return;
      }

      if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
      {
        messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "4031", imagesDir, localDefnsDir, defnsDir, bytesOut);
        serverUtils.etotalBytes(req, unm, dnm, 4031, bytesOut[0], 0, "SID:" + p1);
        if(con  != null) con.close();    
        if(out != null) out.flush();
        return;
      }

      char dispOrEdit;
      if(p1.length() == 0)
        dispOrEdit = 'E';
      else dispOrEdit = 'D';

      byte[] dataAlready = new byte[5000];
      generalUtils.stringToBytes(p4, 0, dataAlready);

      byte[] codeB = new byte[21];

      p1 = generalUtils.stripLeadingAndTrailingSpaces(p1);
      p1 = p1.toUpperCase();
    
      // see if we have to create the record first
      boolean basedOn = false, cart = false;
      String serviceCode="", sourceDocName="", returnServlet=""; 

      if(p1.startsWith("CREATEBASEDONQUOTE:"))
      {
        p1 = p1.substring(19);
        serviceCode = "4033";
        returnServlet = "QuotationPage";
        sourceDocName = "Quotation";
        basedOn = true;
      } 
      else
      if(p1.startsWith("CREATEBASEDONSO:"))
      {
        p1 = p1.substring(16);
        serviceCode = "4034";
        returnServlet = "SalesOrderPage";
        sourceDocName = "Sales Order";
        basedOn = true;
      }
      else
      if(p1.startsWith("CREATEBASEDONINBOX:"))
      {
        p1 = p1.substring(19);
        serviceCode = "3141";
        returnServlet = "_3033";
        sourceDocName = "Inbox Record";
        basedOn = true;
      }
      else
      if(p1.equals("CREATEBASEDONCART"))
        cart = true;
      else generalUtils.strToBytes(codeB, p1);
        
      if(cart)
        buildRecfromCart(out, unm, sid, uty, men, den, dnm, bnm, localDefnsDir);
      else
      if(basedOn)
        buildRec(out, unm, sid, uty, men, den, dnm, bnm, p1, serviceCode, sourceDocName, returnServlet, localDefnsDir);
      else 
      {
       boolean plain = false;
       if(p6.equals("P"))
        plain = true;
      
        salesOrder.getRecToHTML(con, stmt, stmt2, rs, rs2, out, plain, dispOrEdit, codeB, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, p2.charAt(0), p3, dataAlready, req, bytesOut);
      }    
    }
    catch(Exception e) { } 

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 4031, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con  != null) con.close();    
    if(out != null) out.flush();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void buildRec(PrintWriter out, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String code,
                        String serviceCode, String sourceDocName, String returnServlet, String localDefnsDir) throws Exception
  {
    URL url = new URL("http://" + serverUtils.serverToCall("OFSA", localDefnsDir) + "/central/servlet/DocumentBuild");

    HttpURLConnection uc = (HttpURLConnection)url.openConnection();
    uc.setDoInput(true);
    uc.setDoOutput(true);
    uc.setUseCaches(false);
    uc.setDefaultUseCaches(false);
    uc.setRequestProperty("Content-Type", "application/x-www-form.urlencoded");

    String s2 = "&unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm
              + "&bnm=" + bnm + "&p1=" + generalUtils.sanitise(code) + "&p2=" + serviceCode + "&p5=" + returnServlet + "&p4="
              + generalUtils.sanitise(sourceDocName) + "&p3=Sales%20Order";

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
  private void buildRecfromCart(PrintWriter out, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir)
                                throws Exception
  {
    URL url = new URL("http://" + serverUtils.serverToCall("OFSA", localDefnsDir) + "/central/servlet/ProductDocumentCartToDocument");

    HttpURLConnection uc = (HttpURLConnection)url.openConnection();
    uc.setDoInput(true);
    uc.setDoOutput(true);
    uc.setUseCaches(false);
    uc.setDefaultUseCaches(false);
    uc.setRequestProperty("Content-Type", "application/x-www-form.urlencoded");

    String s2 = "&unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=S&p2=so";

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

