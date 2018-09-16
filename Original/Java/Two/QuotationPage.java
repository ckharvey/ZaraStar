// =======================================================================================================================================================================================================
// System: ZaraStar DocumentEngine: Quote fetch rec page
// Module: QuotationPage.java
// Author: C.K.Harvey
// Copyright (c) 2001-08 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.sql.*;
import java.net.*;

public class QuotationPage extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  Quotation quotation = new Quotation();
  DeliveryOrderToQuotation deliveryOrderToQuotation = new DeliveryOrderToQuotation();
  QuotationToQuotation quotationToQuotation = new QuotationToQuotation();
  MiscDefinitions miscDefinitions = new MiscDefinitions();
  AccountsUtils accountsUtils = new AccountsUtils();
  DashboardUtils dashboardUtils = new DashboardUtils();
  DocumentSortEntry DocumentSortEntry = new DocumentSortEntry();
  
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="", p3="", p4="", p6="", p7="", iw="";

    try
    {
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
      p7  = req.getParameter("p7"); // do entry sort

      iw  = req.getParameter("iw");

      if(iw == null) iw = "";

      if(iw.length() == 0)
      {
        directoryUtils.setContentHeaders(res);
        out = res.getWriter();
      }

      if(p1 == null) p1 = "";
      if(p2 == null) p2 = "A";
      if(p3 == null) p3 = "";
      if(p4 == null) p4 = "";
      if(p6 == null) p6 = "N";
      if(p7 == null) p7 = "N";

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, p3, p4, p6, p7, iw, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "QuotationPage", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 4019, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush();
    }
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="", p3="", p4="", p6="", p7="", iw="";

    try
    {
      int len = req.getContentLength();
      ServletInputStream in  = req.getInputStream();
      byte[] b = new byte[len];
      
      in.readLine(b, 0, len);          
      String name, value;
      int x=0;
      while(x < len)
      {
        if(b[x] == '&' || b[x] == '?') /////////////////////////////
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
        else
        if(name.equals("p7")) // do entry sort
          p7 = value;
        else
        if(name.equals("iw"))
          iw = value;
      }
      
      if(iw == null) iw = "";

      if(iw.length() == 0)
      {
        directoryUtils.setContentHeaders(res);
        out = res.getWriter();
      }

      if(p1 == null) p1 = "";
      if(p2 == null) p2 = "A";
      if(p3 == null) p3 = "";
      if(p4 == null) p4 = "";
      if(p6 == null) p6 = "N";
      if(p7 == null) p7 = "N";

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, p3, p4, p6, p7, iw, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "QuotationPage", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 4019, bytesOut[0], 0, "ERR:" + p1);

      if(iw.length() > 0) pageFrameUtils.postToIW(iw, "ERROR: ERR");

      if(out != null) out.flush();
    }
  }
 
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1, String p2, String p3, String p4, String p6, String p7, String iw,  int[] bytesOut)
                    throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');

    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt  = null;
    Statement stmt2 = null;
    ResultSet rs    = null;
    ResultSet rs2   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 142, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "4019", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 4019, bytesOut[0], 0, "ACC:" + p1);
      if(con != null) con.close();
    if(conp[0] != null) conp[0].close();

      if(iw.length() > 0) pageFrameUtils.postToIW(iw, "ERROR: ACC");

      if(out != null) out.flush();
      return;
    }


    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "4019", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 4019, bytesOut[0], 0, "SID:" + p1);
      if(con != null) con.close();
      if(conp[0] != null) conp[0].close();
      if(out != null) out.flush();

      if(iw.length() > 0) pageFrameUtils.postToIW(iw, "ERROR: SID");

      return;
    }

    char dispOrEdit;
    if(p1 == null || p1.length() == 0)
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
      serviceCode = "4022";
      returnServlet = "QuotationPage";
      sourceDocName = "Quotation";
      basedOn = true;
    }
    else
    if(p1.startsWith("CREATEBASEDONDO:"))
    {
      p1 = p1.substring(16);
      serviceCode = "4021";
      returnServlet = "DeliveryOrderPage";
      sourceDocName = "Delivery Order";
      basedOn = true;
    }
    else
    if(p1.startsWith("CREATEBASEDONSO:"))
    {
      p1 = p1.substring(16);
      serviceCode = "4065";
      returnServlet = "QuotationPage";
      sourceDocName = "Sales Order";
      basedOn = true;
    }
    else
    if(p1.startsWith("CREATEBASEDONINBOX:"))
    {
      p1 = p1.substring(19);
      serviceCode = "3140";
      returnServlet = "_3033";
      sourceDocName = "Inbox Record";
      basedOn = true;
    }
    else
    if(p1.equals("CREATEBASEDONCART"))
      cart = true;
    else generalUtils.strToBytes(codeB, p1);

    String s;

    if(cart)
      s = buildRecfromCart(out, unm, sid, uty, men, den, dnm, bnm, localDefnsDir);
    else
    if(basedOn)
      s = buildRec(out, unm, sid, uty, men, den, dnm, bnm, p1, serviceCode, sourceDocName, returnServlet, localDefnsDir);
    else 
    {
      boolean plain = false;
      if(p6.equals("P"))
        plain = true;

      if(p7.equals("Y"))
      {
        DocumentSortEntry.process(con, stmt, stmt2, rs, p1, "Q");
      }

      s = quotation.getRecToHTML(con, stmt, stmt2, rs, rs2, out, plain, dispOrEdit, codeB, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, p2.charAt(0), p3, dataAlready, req, bytesOut);
    }
    
    if(iw.length() > 0) pageFrameUtils.postToIW(iw, s);

    if(out != null) out.flush();
    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 4019, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con != null) con.close();
    if(conp[0] != null) conp[0].close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private String buildRec(PrintWriter out, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String code, String serviceCode, String sourceDocName, String returnServlet, String localDefnsDir) throws Exception
  {
    String all = "";

    URL url = new URL("http://" + serverUtils.serverToCall("OFSA", localDefnsDir) + "/central/servlet/DocumentBuild");

    HttpURLConnection uc = (HttpURLConnection)url.openConnection();
    uc.setDoInput(true);
    uc.setDoOutput(true);
    uc.setUseCaches(false);
    uc.setDefaultUseCaches(false);
    uc.setRequestProperty("Content-Type", "application/x-www-form.urlencoded");

    String s2 = "&unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + generalUtils.sanitise(code) + "&p2=" + serviceCode + "&p5=" + returnServlet + "&p4=" + generalUtils.sanitise(sourceDocName)
              + "&p3=Quotation";

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
      all += s;
      s = di.readLine();
    }

    di.close();

    return all;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private String buildRecfromCart(PrintWriter out, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir) throws Exception
  {
    String all = "";

    URL url = new URL("http://" + serverUtils.serverToCall("OFSA", localDefnsDir) + "/central/servlet/ProductDocumentCartToDocument");

    HttpURLConnection uc = (HttpURLConnection)url.openConnection();
    uc.setDoInput(true);
    uc.setDoOutput(true);
    uc.setUseCaches(false);
    uc.setDefaultUseCaches(false);
    uc.setRequestProperty("Content-Type", "application/x-www-form.urlencoded");

    String s2 = "&unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=Q&p2=quote";

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
      all += s;
      s = di.readLine();
    }

    di.close();

    return all;
  }

}
