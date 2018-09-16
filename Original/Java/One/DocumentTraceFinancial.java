// =======================================================================================================================================================================================================
// System: ZaraStar: Document trace (financial) - do it
// Module: DocumentTraceFinancial.java
// Author: C.K.Harvey
// Copyright (c) 2003-09 Christopher Harvey. All Rights Reserved.
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
import java.sql.*;
import java.util.*;
import java.io.*;

public class DocumentTraceFinancial extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  AccountsUtils accountsUtils = new AccountsUtils();
  Inventory inventory = new Inventory();
  DefinitionTables definitionTables = new DefinitionTables();
  MiscDefinitions miscDefinitions = new MiscDefinitions();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="";

    try
    {
      directoryUtils.setContentHeaders2(res);
      out = res.getWriter();

      unm = req.getParameter("unm");
      sid = req.getParameter("sid");
      uty = req.getParameter("uty");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");
      p1  = req.getParameter("p1"); // soCode
      
      doIt(out, req, p1, unm, sid, uty, men, den, dnm, bnm, bytesOut);
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
 
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "ProductServices6", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1026, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush(); 
    }
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", soCode="";

    try
    {
      directoryUtils.setContentHeaders2(res);
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
          soCode = value[0];
      }
      
      if(soCode == null) soCode = "";
      
      doIt(out, req, soCode, unm, sid, uty, men, den, dnm, bnm, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "DocumentTraceFinancial", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1026, bytesOut[0], 0, "ERR:" + soCode);
      if(out != null) out.flush(); 
    }
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String soCode, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String localDefnsDir  = directoryUtils.getLocalOverrideDir(dnm);
    String defnsDir       = directoryUtils.getSupportDirs('D');
    String imagesDir      = directoryUtils.getSupportDirs('I');
    
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    ResultSet rs = null, rs2 = null, rs3 = null, rs4 = null;
    Statement stmt = null, stmt2 = null, stmt3 = null, stmt4 = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 1026, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "ProductServices6", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1026, bytesOut[0], 0, "ACC:" + soCode);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "ProductServices6", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1026, bytesOut[0], 0, "SID:" + soCode);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    soCode = generalUtils.stripLeadingAndTrailingSpaces(soCode);
    
    process(con, stmt, stmt2, stmt3, stmt4, rs, rs2, rs3, rs4, out, req, soCode, unm, sid, uty, men, den, dnm, bnm, imagesDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 1026, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), soCode);
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void process(Connection con, Statement stmt, Statement stmt2, Statement stmt3, Statement stmt4, ResultSet rs, ResultSet rs2, ResultSet rs3, ResultSet rs4, PrintWriter out, HttpServletRequest req, String soCode,
                       String unm, String sid, String uty, String men, String den, String dnm, String bnm, String imagesDir, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Financial Document Trace</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 139, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewPO(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/PurchaseOrderPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }
    
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 152, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewLP(code){");
      scoutln(out, bytesOut, "var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/LocalPurchasePage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }
    
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 150, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewProforma(code){");
      scoutln(out, bytesOut, "var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/ProformaInvoicePage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }
    
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 149, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewInvoice(code){");
      scoutln(out, bytesOut, "var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/SalesInvoicePage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }
    
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 150, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewPInvoice(code){");
      scoutln(out, bytesOut, "var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/PurchaseInvoicePage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }
    
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 142, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewQuote(code){");
      scoutln(out, bytesOut, "var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/QuotationPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }
    
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 140, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewSO(code){");
      scoutln(out, bytesOut, "var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/SalesOrderPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }
    
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 155, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewCN(code){");
      scoutln(out, bytesOut, "var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/SalesCreditNotePage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }
    
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 148, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewDO(code){");
      scoutln(out, bytesOut, "var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/DeliveryOrderPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 156, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewDN(code){");
      scoutln(out, bytesOut, "var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/SalesDebitNotePage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }
    
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 146, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewOC(code){");
      scoutln(out, bytesOut, "var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/OrderConfirmationPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }
    
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 153, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewGRN(code){");
      scoutln(out, bytesOut, "var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/GoodsReceivedPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }
    
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 147, unm, uty, dnm, localDefnsDir, defnsDir))
    {
       scoutln(out, bytesOut, "function viewPL(code){");
       scoutln(out, bytesOut, "var p1=sanitise(code);");
       scoutln(out, bytesOut, "this.location.href=\"/central/servlet/GoodsReceivedPickingList?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    scoutln(out, bytesOut, "function sanitise(code){");
    scoutln(out, bytesOut, "var code2='';var x;var len=code.length;");
    scoutln(out, bytesOut, "for(x=0;x<len;++x)");
    scoutln(out, bytesOut, "if(code.charAt(x)=='#')code2+='%23';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='\"')code2+='%22';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='&')code2+='%26';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='%')code2+='%25';");
    scoutln(out, bytesOut, "else if(code.charAt(x)==' ')code2+='%20';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='?')code+='%3F';");
    scoutln(out, bytesOut, "else code2+=code.charAt(x);");
    scoutln(out, bytesOut, "return code2};");
    
    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");
  
    scoutln(out, bytesOut, "<script language=\"JavaScript\">"); 

    // text/HTML of the hints
    scoutln(out, bytesOut, "var HINTS_ITEMS = [ 'details' ];");
    scoutln(out, bytesOut, "var myHint;");// = new THints (HINTS_ITEMS, HINTS_CFG);");
    
    // fetch rec for edit
    scoutln(out, bytesOut, "var req2;");
    scoutln(out, bytesOut, "function initRequest2(url)");
    scoutln(out, bytesOut, "{if(window.XMLHttpRequest){req2=new XMLHttpRequest();}else");
    scoutln(out, bytesOut, "if(window.ActiveXObject){req2=new ActiveXObject(\"Microsoft.XMLHTTP\");}}");

    scoutln(out, bytesOut, "function details(docType,docCode){");
    scoutln(out, bytesOut, "var url = \"http://" + men + "/central/servlet/DocumentTraceHints?unm=\" + escape('" + unm + "') + \"&sid=\" + escape('" + sid + "') + \"&uty=\" + escape('" + uty + "') + \"&men=\" + escape('" + men
                         + "') + \"&den=\" + escape('" + den + "') + \"&bnm=\" + escape('" + bnm + "') + \"&p1=\" + docType + \"&p2=\" + docCode + \"&dnm=\" + escape('" + dnm + "');");
    scoutln(out, bytesOut, "initRequest2(url);");
    scoutln(out, bytesOut, "req2.onreadystatechange=processRequest2;");
    scoutln(out, bytesOut, "req2.open(\"GET\",url,true);");
    scoutln(out, bytesOut, "req2.send(null);}");

    scoutln(out, bytesOut, "function processRequest2(){");
    scoutln(out, bytesOut, "if(req2.readyState==4){");
    scoutln(out, bytesOut, "if(req2.status == 200){");
    scoutln(out, bytesOut, "var res=req2.responseXML.getElementsByTagName(\"res\")[0].childNodes[0].nodeValue;");
    scoutln(out, bytesOut, "if(res.length > 0){");
    scoutln(out, bytesOut, "if(res=='.'){");
    scoutln(out, bytesOut, "var hint = req2.responseXML.getElementsByTagName(\"hint\")[0].childNodes[0].nodeValue;");
    scoutln(out, bytesOut, "var messageElement = document.getElementById('hints');");
    scoutln(out, bytesOut, "messageElement.innerHTML=layout(hint);");
    scoutln(out, bytesOut, "document.getElementById('hints').style.height = (15 * numRows(hint));");
    scoutln(out, bytesOut, "document.getElementById('hints').style.width = (7 * numCols(hint));");
    scoutln(out, bytesOut, "}}}}}");

    
    scoutln(out, bytesOut, "var req3;");
    scoutln(out, bytesOut, "function initRequest3(url)");
    scoutln(out, bytesOut, "{if(window.XMLHttpRequest){req3=new XMLHttpRequest();}else");
    scoutln(out, bytesOut, "if(window.ActiveXObject){req3=new ActiveXObject(\"Microsoft.XMLHTTP\");}}");

    scoutln(out, bytesOut, "function hint(code){");
    scoutln(out, bytesOut, "var url = \"http://" + men + "/central/servlet/SupportHelpHints?unm=\" + escape('" + unm + "') + \"&sid=\" + escape('" + sid + "') + \"&uty=\" + escape('" + uty + "') + \"&men=\" + escape('" + men + "') + \"&den=\" + escape('" + den
                         + "') + \"&bnm=\" + escape('" + bnm + "') + \"&p1=\" + code + \"&dnm=\" + escape('" + dnm + "');");
    scoutln(out, bytesOut, "initRequest3(url);");
    scoutln(out, bytesOut, "req3.onreadystatechange=processRequest3;");
    scoutln(out, bytesOut, "req3.open(\"GET\",url,true);");
    scoutln(out, bytesOut, "req3.send(null);}");

    scoutln(out, bytesOut, "function processRequest3(){");
    scoutln(out, bytesOut, "if(req3.readyState==4){");
    scoutln(out, bytesOut, "if(req3.status == 200){");
    scoutln(out, bytesOut, "var res=req3.responseXML.getElementsByTagName(\"res\")[0].childNodes[0].nodeValue;");
    scoutln(out, bytesOut, "if(res.length > 0){");
    scoutln(out, bytesOut, "if(res=='.'){");
    scoutln(out, bytesOut, "var hint = req3.responseXML.getElementsByTagName(\"hint\")[0].childNodes[0].nodeValue;");
    scoutln(out, bytesOut, "var messageElement = document.getElementById('hints');");
    scoutln(out, bytesOut, "messageElement.innerHTML=layout(hint);");
    scoutln(out, bytesOut, "document.getElementById('hints').style.height = (15 * numRows(hint));");
    scoutln(out, bytesOut, "document.getElementById('hints').style.width = (7 * numCols(hint));");
    scoutln(out, bytesOut, "}}}}}");

    scoutln(out, bytesOut, "function layout(s){");
    scoutln(out, bytesOut, "var x=0,len=s.length,t='';");
    scoutln(out, bytesOut, "while(x < len)");
    scoutln(out, bytesOut, "{");
    scoutln(out, bytesOut, "if(s.charAt(x) == '`')");
    scoutln(out, bytesOut, "  t += '<br />';");
    scoutln(out, bytesOut, "else t += s.charAt(x);");
    scoutln(out, bytesOut, "++x; }");
    scoutln(out, bytesOut, "return t;");
    scoutln(out, bytesOut, "}");
    
    scoutln(out, bytesOut, "function numRows(s){");
    scoutln(out, bytesOut, "var x=0,len=s.length,n=1;");
    scoutln(out, bytesOut, "while(x < len)");
    scoutln(out, bytesOut, "{");
    scoutln(out, bytesOut, "if(s.charAt(x) == '`')");
    scoutln(out, bytesOut, "  ++n;");
    scoutln(out, bytesOut, "++x; }");
    scoutln(out, bytesOut, "return n;");
    scoutln(out, bytesOut, "}");
    
    scoutln(out, bytesOut, "function numCols(s){");
    scoutln(out, bytesOut, "var x=0,len=s.length,n=0,y=0;");
    scoutln(out, bytesOut, "while(x < len)");
    scoutln(out, bytesOut, "{");
    scoutln(out, bytesOut, "if(s.charAt(x) == '`')");
    scoutln(out, bytesOut, "{ if(y > n) n = y; y = 0;}else ++y;");
    scoutln(out, bytesOut, "++x; }");
    scoutln(out, bytesOut, "if(y > n)n=y;return n;");
    scoutln(out, bytesOut, "}");

    scoutln(out, bytesOut, "</script>");
    scoutln(out, bytesOut, "<script language=\"JavaScript\" src=\"" + directoryUtils.getSupportDirs('S') + "tigra_hints.js\"></script></head>");
    
    int[] hmenuCount = new int[1];
    
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "1026", "", "ProductServices6", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
    
    scoutln(out, bytesOut, "    <script language=\"JavaScript\">");

    scoutln(out, bytesOut, "var HINTS_CFG = { 'wise'       : true," +
                "	'margin'     : 10," +
                "	'gap'        : 0," +
                "	'align'      : 'tcbc'," +
                "	'css'        : 'hintsClass'," +
                "	'show_delay' : 100," +
                "	'hide_delay' : 100," +
                "   'follow'     : false," +
                "	'z-index'    : 100," +
                "	'IEfix'      : false," +
                "	'IEtrans'    : ['blendTrans(DURATION=.3)', 'blendTrans(DURATION=.3)']," +
                "	'opacity'    : 100 " +
                "};");
    scoutln(out, bytesOut, "</script><script language=\"JavaScript\">myHint = new THints (HINTS_ITEMS, HINTS_CFG);</script>");

    scoutln(out, bytesOut, "<div id=\"hints\" style=\"position:absolute;border:2px dotted red;top:100px;left:100px;z-index:1000;"
            + "visibility:hidden;width:10px;height:10px; font-family: tahoma, verdana, arial; font-size: 12px; background-color: #f0f0f0; color: #000000;"
            + "padding: 5px;\"></div>");

    scoutln(out, bytesOut, "<form>");
  
    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Financial Document Trace", "1026",  unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<p>Documents related to ");

      scout(out, bytesOut, "Sales Order");

    scoutln(out, bytesOut, ": &nbsp; &nbsp; " + soCode);
    
    scoutln(out, bytesOut, "<br><table id=\"page\" width=100%>");

    String[][]  codes      = new String [1][100];
    String[][]  sourceDocs = new String [1][100];
    char[][]    types      = new char[1][100];
    boolean[][] searched   = new boolean[1][100];
    
    int[] codesLen      = new int[1];  codesLen[0]      = 100;
    int[] sourceDocsLen = new int[1];  sourceDocsLen[0] = 100;

    int x, numEntries = 0;
    
    numEntries = searchSOHeadersGivenSOCode(con, stmt, rs, soCode, codes, types, sourceDocs, searched, codesLen, sourceDocsLen, numEntries);
    numEntries = searchPLLinesGivenSOCode(con, stmt, rs, soCode, codes, types, sourceDocs, searched, codesLen, sourceDocsLen, numEntries);
    numEntries = searchPOLinesGivenSOCode(con, stmt, rs, soCode, codes, types, sourceDocs, searched, codesLen, sourceDocsLen, numEntries);
    numEntries = searchLPLinesGivenSOCode(con, stmt, rs, soCode, codes, types, sourceDocs, searched, codesLen, sourceDocsLen, numEntries);
    numEntries = searchDOLinesGivenSOCode(con, stmt, rs, soCode, codes, types, sourceDocs, searched, codesLen, sourceDocsLen, numEntries);
    numEntries = searchInvoiceLinesGivenSOCode(con, stmt, rs, soCode, codes, types, sourceDocs, searched, codesLen, sourceDocsLen, numEntries);
    numEntries = searchOCHeadersGivenSOCode(con, stmt, rs, soCode, codes, types, sourceDocs, searched, codesLen, sourceDocsLen, numEntries);
    numEntries = searchProformaLinesGivenSOCode(con, stmt, rs, soCode, codes, types, sourceDocs, searched, codesLen, sourceDocsLen, numEntries);

    numEntries = addToList(soCode, 'S', soCode, numEntries, codes, types, sourceDocs, searched, codesLen, sourceDocsLen);
    
    boolean atLeastOneRemaining = true;
    while(atLeastOneRemaining)
    {
      for(x=0;x<numEntries;++x)
      {
        if(! searched[0][x])
        {
          numEntries = allSearches(con, stmt, rs, codes[0][x], types[0][x], codes, types, sourceDocs, searched, codesLen, sourceDocsLen, numEntries);
          searched[0][x] = true;
        }
      }

      atLeastOneRemaining = false;
      for(x=0;x<numEntries;++x)
      {
        if(! searched[0][x])
          atLeastOneRemaining = true;
      }
    }

    String baseCurrency = accountsUtils.getBaseCurrency(con, stmt, rs, dnm, localDefnsDir, defnsDir);
    
    displaySummary(con, stmt, stmt2, stmt3, stmt4, rs, rs2, rs3, rs4, out, numEntries, codes[0], types[0], soCode, baseCurrency, dnm, localDefnsDir, defnsDir, bytesOut);
    
    displayResults(con, stmt, stmt2, rs, rs2, out, numEntries, codes, types, baseCurrency, sourceDocs, imagesDir, bytesOut);
    
    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }   
        
  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int allSearches(Connection con, Statement stmt, ResultSet rs, String thisCode, char thisType, String[][] codes, char[][] types, String[][] sourceDocs, boolean[][] searched, int[] codesLen, int[] sourceDocsLen, int numEntries)
                          throws Exception
  {
//    System.out.println(thisType + " " + thisCode);
    
    if(thisType == 'Y')
    {
      numEntries = searchGRNLinesGivenPOCode(con, stmt, rs, thisCode, codes, types, sourceDocs, searched, codesLen, sourceDocsLen, numEntries);
    }
    else
    if(thisType == 'G')
    {
      numEntries = searchPILinesGivenGRCode(con, stmt, rs, thisCode, codes, types, sourceDocs, searched, codesLen, sourceDocsLen, numEntries);
    }

    return numEntries;
  }
    
  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int addToList(String newCode, char newType, String newSourceDoc, int numEntries, String[][] codes, char[][] types, String[][] sourceDocs, boolean[][] searched, int[] codesLen, int[] sourceDocsLen) throws Exception
  {
    if(newCode == null || newCode.length() == 0)
      return numEntries;
    
    if(newCode.indexOf("'") != -1)
    {
      return numEntries;
    }

    for(int x=0;x<numEntries;++x)
    {
      if(codes[0][x].equalsIgnoreCase(newCode))
      {
        if(types[0][x] == newType)
        {
          return numEntries; // already on list
        }     
      }
    }
    
    if(numEntries >= codesLen[0])
    {
      int y, len = codesLen[0];
      
      String[] buf = new String[len];
      for(y=0;y<len;++y)
        buf[y] = codes[0][y];
      codesLen[0] += 100;
      codes[0] = new String[codesLen[0]];
      for(y=0;y<len;++y)
        codes[0][y] = buf[y];

      char[] cbuf = new char[len];
      for(y=0;y<len;++y)
        cbuf[y] = types[0][y];
      types[0] = new char[codesLen[0]];
      for(y=0;y<len;++y)
        types[0][y] = cbuf[y];
      
      String[] buf2 = new String[len];
      for(y=0;y<len;++y)
        buf2[y] = sourceDocs[0][y];
      sourceDocsLen[0] += 100;
      sourceDocs[0] = new String[sourceDocsLen[0]];
      for(y=0;y<len;++y)
        sourceDocs[0][y] = buf2[y];

      boolean[] bbuf = new boolean[len];
      for(y=0;y<len;++y)
        bbuf[y] = searched[0][y];
      searched[0] = new boolean[codesLen[0]];
      for(y=0;y<len;++y)
        searched[0][y] = bbuf[y];
    }
    
    codes[0][numEntries] = newCode;
    types[0][numEntries] = newType;
    sourceDocs[0][numEntries] = newSourceDoc;
    searched[0][numEntries] = false;

    return (numEntries + 1);
  }
    
  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void displayResults(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, int numEntries, String[][] codes, char[][] types, String baseCurrency, String[][] sourceDocs, String imagesDir,
                              int[] bytesOut) throws Exception
  {
    displayDocs(con, stmt, stmt2, rs, rs2, out, 'Q', "viewQuote",    "1079.gif", "1079c.gif", "quote",    "QuoteCode",    "DocumentStatus", numEntries, codes[0], types[0], sourceDocs[0], baseCurrency, imagesDir, bytesOut);
    displayDocs(con, stmt, stmt2, rs, rs2, out, 'S', "viewSO",       "1183.gif", "1183c.gif", "so",       "SOCode",       "Status",         numEntries, codes[0], types[0], sourceDocs[0], baseCurrency, imagesDir, bytesOut);
    displayDocs(con, stmt, stmt2, rs, rs2, out, 'O', "viewOC",       "1296.gif", "1296c.gif", "oc",       "OCCode",       "Status",         numEntries, codes[0], types[0], sourceDocs[0], baseCurrency, imagesDir, bytesOut);
    displayDocs(con, stmt, stmt2, rs, rs2, out, 'R', "viewProforma", "1011.gif", "1011c.gif", "proforma", "ProformaCode", "Status",         numEntries, codes[0], types[0], sourceDocs[0], baseCurrency, imagesDir, bytesOut);
    displayDocs(con, stmt, stmt2, rs, rs2, out, 'P', "viewPL",       "1294.gif", "1294c.gif", "pl",       "PLCode",       "Status",         numEntries, codes[0], types[0], sourceDocs[0], baseCurrency, imagesDir, bytesOut);
    displayDocs(con, stmt, stmt2, rs, rs2, out, 'D', "viewDO",       "1100.gif", "1100c.gif", "do",       "DOCode",       "Status",         numEntries, codes[0], types[0], sourceDocs[0], baseCurrency, imagesDir, bytesOut);
    displayDocs(con, stmt, stmt2, rs, rs2, out, 'I', "viewInvoice",  "1033.gif", "1033c.gif", "invoice",  "InvoiceCode",  "Status",         numEntries, codes[0], types[0], sourceDocs[0], baseCurrency, imagesDir, bytesOut);
    displayDocs(con, stmt, stmt2, rs, rs2, out, 'N', "viewDN",       "1208.gif", "1208c.gif", "debit",    "DNCode",       "Status",         numEntries, codes[0], types[0], sourceDocs[0], baseCurrency, imagesDir, bytesOut);
    displayDocs(con, stmt, stmt2, rs, rs2, out, 'C', "viewCN",       "1042.gif", "1042c.gif", "credit",   "CNCode",       "Status",         numEntries, codes[0], types[0], sourceDocs[0], baseCurrency, imagesDir, bytesOut);
    displayDocs(con, stmt, stmt2, rs, rs2, out, 'Y', "viewPO",       "1102.gif", "1102c.gif", "po",       "POCode",       "Status",         numEntries, codes[0], types[0], sourceDocs[0], baseCurrency, imagesDir, bytesOut);
    displayDocs(con, stmt, stmt2, rs, rs2, out, 'Z', "viewLP",       "1192.gif", "1192c.gif", "lp",       "LPCode",       "Status",         numEntries, codes[0], types[0], sourceDocs[0], baseCurrency, imagesDir, bytesOut);
    displayDocs(con, stmt, stmt2, rs, rs2, out, 'G', "viewGRN",      "1298.gif", "1298c.gif", "gr",       "GRCode",       "Status",         numEntries, codes[0], types[0], sourceDocs[0], baseCurrency, imagesDir, bytesOut);
    displayDocs(con, stmt, stmt2, rs, rs2, out, 'J', "viewPInvoice", "1297.gif", "1297c.gif", "pinvoice", "InvoiceCode",  "Status",         numEntries, codes[0], types[0], sourceDocs[0], baseCurrency, imagesDir, bytesOut);
  }   

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void displayDocs(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, char type, String viewJS, String imageFileLive, String imageFileCancelled, String tableName, String docCodeName,
                           String docStatusName, int numEntries, String[] codes, char[] types, String[] sourceDocs, String baseCurrency, String imagesDir, int[] bytesOut) throws Exception
  {
    String imageFile;
    boolean first = true, liveOrNot;
    String[] currency    = new String[1];
    String[] date        = new String[1];
    String[] returned    = new String[1];
    String[] allSupplied = new String[1];
    double[] amount      = new double[1];
    double[] amount2     = new double[1];

    for(int x=0;x<numEntries;++x)
    {
      if(types[x] == type)
      {
        if(first)
        {
          scout(out, bytesOut, "<tr>");
          first = false;
        }

        liveOrNot = true;
        switch(type)
        {
          case 'S' : liveOrNot = calcDetailsSO(      con, stmt,        rs,      sourceDocs[x],           currency, date, amount, amount2, allSupplied); break;
          case 'P' : liveOrNot = calcDetailsPL(      con, stmt,        rs,      sourceDocs[x], codes[x], currency, date, amount, amount2);              break;
          case 'D' : liveOrNot = calcDetailsDO(      con, stmt,        rs,      sourceDocs[x], codes[x], currency, date, amount, amount2, returned);    break;
          case 'I' : liveOrNot = calcDetailsInvoice( con, stmt, stmt2, rs, rs2, sourceDocs[x], codes[x], currency, date, amount, amount2);              break;
          case 'Y' : liveOrNot = calcDetailsPO(      con, stmt,        rs,      sourceDocs[x], codes[x], currency, date, amount, amount2);              break;
          case 'Z' : liveOrNot = calcDetailsLP(      con, stmt,        rs,      sourceDocs[x], codes[x], currency, date, amount, amount2);              break;
          case 'G' : liveOrNot = calcDetailsGR(      con, stmt,        rs,      sourceDocs[x], codes[x], currency, date, amount, amount2);              break;
          case 'J' : liveOrNot = calcDetailsPInvoice(con, stmt,        rs,      sourceDocs[x], codes[x], currency, date, amount, amount2);              break;
          default  : amount[0] = amount2[0] = 0.0;
                     currency[0] = date[0] = "";
                     break;                     
        }
        
        if(liveOrNot)
          imageFile = imageFileLive;          
        else imageFile = imageFileCancelled;        
     
        scoutln(out, bytesOut, "<td nowrap valign=top><br><a href=\"javascript:" + viewJS + "('" + codes[x] + "')\"><img src=\"" + imagesDir + imageFile + "\" border=0  onMouseOver=\"details('" + tableName + "','" + codes[x]
                             + "');myHint.show('hints', this)\" onMouseOut=\"myHint.hide()\"       ><br>" + codes[x] + "</a><p>" + date[0]);
        
        if(type == 'S' || type == 'I' || type == 'Y' || type == 'Z' || type == 'J')
        {
          scoutln(out, bytesOut, "<br>" + currency[0] + " " + generalUtils.doubleDPs('2', amount[0]));
          if(! currency[0].equals(baseCurrency))
            scoutln(out, bytesOut, "<br>(" + baseCurrency + " " + generalUtils.doubleDPs('2', amount2[0]) + ")");
        }

        if(type == 'S')
        {
          if(allSupplied[0].equals("Y"))
            scoutln(out, bytesOut, "<br>All Supplied");
          else scoutln(out, bytesOut, "<br>Not All Supplied");
        }

        if(type == 'D')
        {
          if(returned[0].equals("Y"))
            scoutln(out, bytesOut, "<br>Returned");
          else scoutln(out, bytesOut, "<br>Being Delivered");
        }

        
        
        scoutln(out, bytesOut, "</td>");        
      }
    }
    
    if(! first)
      scout(out, bytesOut, "</tr>");
  }
  
  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void displaySummary(Connection con, Statement stmt, Statement stmt2, Statement stmt3, Statement stmt4, ResultSet rs, ResultSet rs2, ResultSet rs3, ResultSet rs4, PrintWriter out, int numEntries, String[] codes, char[] types,
                              String soCode, String baseCurrency, String dnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    double cos = 0.0, so = 0.0, invoiced = 0.0;
    
    for(int x=0;x<numEntries;++x)
    {
      switch(types[x])
      {
        case 'S' : so       += chkSalesOrder(con, stmt, rs, codes[x]);
                   cos      += chkInvoiceLines(true, con, stmt, stmt2, rs, rs2, codes[x], dnm, localDefnsDir, defnsDir);
                   invoiced += chkInvoiceLinesForInvoicedAmount(con, stmt, stmt2, rs, rs2, codes[x]);
                   cos      += chkPurchaseInvoiceLines(con, stmt, stmt2, stmt3, stmt4, rs, rs2, rs3, rs4, codes[x]);
                   break;
      }
    }

    scoutln(out, bytesOut, "<tr><td nowrap colspan=4><table>");
    scoutln(out, bytesOut, "<tr><td><p>Sales Order Value: </td><td align=right><p>" + baseCurrency + " " + generalUtils.doubleDPs('2', so) + "</td></tr>");
    scoutln(out, bytesOut, "<tr><td><p>Invoiced Value: </td><td align=right><p>" + baseCurrency + " " + generalUtils.doubleDPs('2', invoiced) + "</td></tr>");
    scoutln(out, bytesOut, "<tr><td><p>Cost of Sale: </td><td align=right><p>" + baseCurrency + " " + generalUtils.doubleDPs('2', cos) + "</td></tr>");
    scoutln(out, bytesOut, "</table><td></tr>");
  }
  
  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean calcDetailsSO(Connection con, Statement stmt, ResultSet rs, String soCode, String[] currency, String[] date, double[] amount, double[] amount2, String[] allSupplied) throws Exception 
  {    
    stmt = con.createStatement();
     
    rs = stmt.executeQuery("SELECT TotalTotal, BaseTotalTotal, Date, Currency2, Status, AllSupplied FROM so WHERE SOCode = '" + soCode + "'");

    String status = "L";
    amount[0] = amount2[0] = 0.0;
     
    if(rs.next()) // just-in-case
    {
      amount[0]      = generalUtils.doubleFromStr(generalUtils.doubleDPs(rs.getString(1), '2'));
      amount2[0]     = generalUtils.doubleFromStr(generalUtils.doubleDPs(rs.getString(2), '2'));
      date[0]        = generalUtils.convertFromYYYYMMDD(rs.getString(3));
      currency[0]    = rs.getString(4);
      status         = rs.getString(5);
      allSupplied[0] = rs.getString(6);
    }
         
    if(stmt != null) stmt.close();
    
    if(status.equals("C"))
      return false;
    
    return true;
  }
  
  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean calcDetailsPL(Connection con, Statement stmt, ResultSet rs, String soCode, String docCode, String[] currency, String[] date, double[] amount, double[] amount2) throws Exception 
  {    
    stmt = con.createStatement();
     
    rs = stmt.executeQuery("SELECT t2.Amount2, t2.Amount, t1.Date, t1.Currency, t1.Status FROM pll AS t2 INNER JOIN pl AS t1 ON t2.PLCode = t1.PLCode WHERE t2.SOCode = '" + soCode + "' AND t2.PLCode = '" + docCode + "'");

    String status = "L";
    amount[0] = amount2[0] = 0.0;
     
    while(rs.next())
    {
      amount[0]  += generalUtils.doubleFromStr(generalUtils.doubleDPs(rs.getString(1), '2'));
      amount2[0] += generalUtils.doubleFromStr(generalUtils.doubleDPs(rs.getString(2), '2'));
      date[0]     = generalUtils.convertFromYYYYMMDD(rs.getString(3));
      currency[0] = rs.getString(4);
      status      = rs.getString(5);
    }
         
    if(stmt != null) stmt.close();
    
    if(status.equals("C"))
      return false;
    
    return true;
  }
  
  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean calcDetailsDO(Connection con, Statement stmt, ResultSet rs, String soCode, String docCode, String[] currency, String[] date, double[] amount, double[] amount2, String[] returned) throws Exception 
  {    
    stmt = con.createStatement();
     
    rs = stmt.executeQuery("SELECT t2.Amount2, t2.Amount, t1.Date, t1.Currency, t1.Status, t1.Returned FROM dol AS t2 INNER JOIN do AS t1 ON t2.DOCode = t1.DOCode WHERE t2.SOCode = '" + soCode + "' AND t2.DOCode = '" + docCode + "'");

    String status = "L";
    amount[0] = amount2[0] = 0.0;
     
    while(rs.next())
    {
      amount[0]  += generalUtils.doubleFromStr(generalUtils.doubleDPs(rs.getString(1), '2'));
      amount2[0] += generalUtils.doubleFromStr(generalUtils.doubleDPs(rs.getString(2), '2'));
      date[0]     = generalUtils.convertFromYYYYMMDD(rs.getString(3));
      currency[0] = rs.getString(4);
      status      = rs.getString(5);
      returned[0] = rs.getString(6);
    }
         
    if(stmt != null) stmt.close();
    
    if(status.equals("C"))
      return false;
    
    return true;
  }
  
  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean calcDetailsInvoice(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String soCode, String docCode, String[] currency, String[] date, double[] amount, double[] amount2) throws Exception 
  {    
    stmt = con.createStatement();
     
    rs = stmt.executeQuery("SELECT t2.Amount2, t2.Amount, t1.Date, t1.Currency, t1.Status, t2.GSTRate FROM invoicel AS t2 INNER JOIN invoice AS t1 ON t2.InvoiceCode = t1.InvoiceCode WHERE t2.SOCode = '" + soCode + "' AND t2.InvoiceCode = '"
                         + docCode + "'");

    String status = "L", gstRateName;
    double gstRate, amt;
    
    amount[0] = amount2[0] = 0.0;
     
    while(rs.next())
    {
      gstRateName = rs.getString(6);
      gstRate     = accountsUtils.getGSTRate(con, stmt2, rs2, gstRateName);
              
      amt = generalUtils.doubleFromStr(generalUtils.doubleDPs(rs.getString(1), '2'));
      amt += (amt * gstRate);
      amount[0]  += amt;
      
      amt = generalUtils.doubleFromStr(generalUtils.doubleDPs(rs.getString(2), '2'));
      amt += (amt * gstRate);
      amount2[0]  += amt;
      
      date[0]     = generalUtils.convertFromYYYYMMDD(rs.getString(3));
      currency[0] = rs.getString(4);
      status      = rs.getString(5);
    }
         
    if(stmt != null) stmt.close();
    
    if(status.equals("C"))
      return false;
    
    return true;
  }
  
  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean calcDetailsPInvoice(Connection con, Statement stmt, ResultSet rs, String grCode, String docCode, String[] currency, String[] date, double[] amount, double[] amount2) throws Exception 
  {    
    stmt = con.createStatement();
     
    rs = stmt.executeQuery("SELECT t2.Amount2, t2.Amount, t1.Date, t1.Currency, t1.Status FROM pinvoicel AS t2 INNER JOIN pinvoice AS t1 ON t2.InvoiceCode = t1.InvoiceCode WHERE t2.GRCode = '" + grCode + "' AND t2.InvoiceCode = '" + docCode + "'");

    String status = "L";
    amount[0] = amount2[0] = 0.0;
     
    while(rs.next())
    {
      amount[0]  += generalUtils.doubleFromStr(generalUtils.doubleDPs(rs.getString(1), '2'));
      amount2[0] += generalUtils.doubleFromStr(generalUtils.doubleDPs(rs.getString(2), '2'));
      date[0]     = generalUtils.convertFromYYYYMMDD(rs.getString(3));
      currency[0] = rs.getString(4);
      status      = rs.getString(5);
    }
         
    if(stmt != null) stmt.close();
    
    if(status.equals("C"))
      return false;
    
    return true;
  }
  
  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean calcDetailsGR(Connection con, Statement stmt, ResultSet rs, String poCode, String docCode, String[] currency, String[] date, double[] amount, double[] amount2) throws Exception 
  {    
    stmt = con.createStatement();
     
    rs = stmt.executeQuery("SELECT t2.Amount, t1.Date, t1.Status FROM grl AS t2 INNER JOIN gr AS t1 ON t2.GRCode = t1.GRCode WHERE t2.POCode = '" + poCode + "' AND t2.GRCode = '" + docCode + "'");

    String status = "L";
    amount[0] = amount2[0] = 0.0;
     
    while(rs.next())
    {
      amount[0]  += generalUtils.doubleFromStr(generalUtils.doubleDPs(rs.getString(1), '2'));
//      amount2[0] += generalUtils.doubleFromStr(generalUtils.doubleDPs(rs.getString(2), '2'));
      date[0]     = generalUtils.convertFromYYYYMMDD(rs.getString(2));
//      currency[0] = rs.getString(3);
      status      = rs.getString(3);
    }
         
    if(stmt != null) stmt.close();
    
    if(status.equals("C"))
      return false;
    
    return true;
  }
  
  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean calcDetailsPO(Connection con, Statement stmt, ResultSet rs, String soCode, String docCode, String[] currency, String[] date, double[] amount, double[] amount2) throws Exception 
  {    
    stmt = con.createStatement();
     
    rs = stmt.executeQuery("SELECT t2.Amount2, t2.Amount, t1.Date, t1.Currency, t1.Status FROM pol AS t2 INNER JOIN po AS t1 ON t2.POCode = t1.POCode WHERE t2.SOCode = '" + soCode + "' AND t2.POCode = '" + docCode + "'");

    String status = "L";
    amount[0] = amount2[0] = 0.0;
     
    while(rs.next())
    {
      amount[0]  += generalUtils.doubleFromStr(generalUtils.doubleDPs(rs.getString(1), '2'));
      amount2[0] += generalUtils.doubleFromStr(generalUtils.doubleDPs(rs.getString(2), '2'));
      date[0]     = generalUtils.convertFromYYYYMMDD(rs.getString(3));
      currency[0] = rs.getString(4);
      status      = rs.getString(5);
    }
         
    if(stmt != null) stmt.close();
    
    if(status.equals("C"))
      return false;
    
    return true;
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean calcDetailsLP(Connection con, Statement stmt, ResultSet rs, String soCode, String docCode, String[] currency, String[] date, double[] amount, double[] amount2) throws Exception 
  {    
    stmt = con.createStatement();
     
    rs = stmt.executeQuery("SELECT t2.Amount2, t2.Amount, t1.Date, t1.Currency, t1.Status FROM LPl AS t2 INNER JOIN LP AS t1 ON t2.LPCode = t1.LPCode WHERE t2.SOCode = '" + soCode + "' AND t2.LPCode = '" + docCode + "'");

    String status = "L";
    amount[0] = amount2[0] = 0.0;
     
    while(rs.next())
    {
      amount[0]  += generalUtils.doubleFromStr(generalUtils.doubleDPs(rs.getString(1), '2'));
      amount2[0] += generalUtils.doubleFromStr(generalUtils.doubleDPs(rs.getString(2), '2'));
      date[0]     = generalUtils.convertFromYYYYMMDD(rs.getString(3));
      currency[0] = rs.getString(4);
      status      = rs.getString(5);
    }
         
    if(stmt != null) stmt.close();
    
    if(status.equals("C"))
      return false;
    
    return true;
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean checkCodeExists(String callerCode, Connection con, Statement stmt, ResultSet rs, String code, char type) throws Exception
  {   
    if(code.length() == 0)
      return false;
    
    boolean res = false;
    
    switch(type)
    {
      case 'Q' : res = existsGivenCode(con, stmt, rs, code, "quote",    "QuoteCode");    break;
      case 'S' : res = existsGivenCode(con, stmt, rs, code, "so",       "SOCode");       break;
      case 'O' : res = existsGivenCode(con, stmt, rs, code, "oc",       "OCCode");       break;
      case 'P' : res = existsGivenCode(con, stmt, rs, code, "pl",       "PLCode");       break;
      case 'D' : res = existsGivenCode(con, stmt, rs, code, "do",       "DOCode");       break;
      case 'I' : res = existsGivenCode(con, stmt, rs, code, "invoice",  "InvoiceCode");  break;
      case 'J' : res = existsGivenCode(con, stmt, rs, code, "pinvoice", "InvoiceCode");  break;
      case 'R' : res = existsGivenCode(con, stmt, rs, code, "proforma", "ProformaCode"); break;
      case 'C' : res = existsGivenCode(con, stmt, rs, code, "credit",   "CNCode");       break;
      case 'Y' : res = existsGivenCode(con, stmt, rs, code, "po",       "POCode");       break;
      case 'Z' : res = existsGivenCode(con, stmt, rs, code, "lp",       "LPCode");       break;
      case 'G' : res = existsGivenCode(con, stmt, rs, code, "gr",       "GRCode");       break;
      case 'N' : res = existsGivenCode(con, stmt, rs, code, "debit",    "DNCode");       break;
    } 
     
    return res;
  }      
      
  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean existsGivenCode(Connection con, Statement stmt, ResultSet rs, String code, String tableName, String codeName) throws Exception
  {
    if(code.length() == 0) // just-in-case
      return false;
    
    if(code.indexOf("'") != -1)
      return false;

    code = code.toUpperCase();

    stmt = con.createStatement();
    
    int numRecs = 0;
    
    rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM " + tableName + " WHERE " + codeName + " = '" + code + "'");
    if(rs.next()) // just-in-case      
      numRecs = rs.getInt("rowcount") ;
    if(rs != null) rs.close();
   
    if(stmt != null) stmt.close();

    if(numRecs == 1)
      return true;
    
    return false;
  }
    
  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scout(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.print(str);
    bytesOut[0] += str.length();    
  }
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int searchSOHeadersGivenSOCode(Connection con, Statement stmt, ResultSet rs, String soCode, String[][] codes, char[][] types, String[][] sourceDocs, boolean[][] searched, int[] codesLen, int[] sourceDocsLen, int numEntries)
                                         throws Exception
  {
    if(! checkCodeExists(soCode, con, stmt, rs, soCode, 'S')) return numEntries;   

    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT QuoteCode FROM so WHERE SOCode = '" + soCode + "'");

    while(rs.next())
    {    
      if(checkCodeExists(soCode, con, stmt, rs, rs.getString(1), 'Q'))
        numEntries = addToList(rs.getString(1), 'Q', soCode, numEntries, codes, types, sourceDocs, searched, codesLen, sourceDocsLen);
    }

    if(stmt != null) stmt.close();
    
    return numEntries;
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int searchOCHeadersGivenSOCode(Connection con, Statement stmt, ResultSet rs, String soCode, String[][] codes, char[][] types, String[][] sourceDocs, boolean[][] searched, int[] codesLen, int[] sourceDocsLen, int numEntries)
                                         throws Exception
  {
    if(! checkCodeExists(soCode, con, stmt, rs, soCode, 'S')) return numEntries;   

    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT OCCode FROM oc WHERE SOCode = '" + soCode  + "'");

    while(rs.next())
    {    
      if(checkCodeExists(soCode, con, stmt, rs, rs.getString(1), 'O'))
        numEntries = addToList(rs.getString(1), 'O', soCode, numEntries, codes, types, sourceDocs, searched, codesLen, sourceDocsLen);
    }

    if(stmt != null) stmt.close();
    
    return numEntries;
  }
  
  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int searchProformaLinesGivenSOCode(Connection con, Statement stmt, ResultSet rs, String soCode, String[][] codes, char[][] types, String[][] sourceDocs, boolean[][] searched, int[] codesLen, int[] sourceDocsLen, int numEntries)
                                             throws Exception
  {
    if(! checkCodeExists(soCode, con, stmt, rs, soCode, 'S')) return numEntries;   

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT ProformaCode FROM proformal WHERE SOCode = '" + soCode + "'");

    while(rs.next())
    {    
      if(checkCodeExists(soCode, con, stmt, rs, rs.getString(1), 'R'))
        numEntries = addToList(rs.getString(1), 'R', soCode, numEntries, codes, types, sourceDocs, searched, codesLen, sourceDocsLen);
    }

    if(stmt != null) stmt.close();
    
    return numEntries;
  }
    
  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int searchPLLinesGivenSOCode(Connection con, Statement stmt, ResultSet rs, String soCode, String[][] codes, char[][] types, String[][] sourceDocs, boolean[][] searched, int[] codesLen, int[] sourceDocsLen, int numEntries) throws Exception
  {
    if(! checkCodeExists(soCode, con, stmt, rs, soCode, 'S')) return numEntries;   

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT PLCode FROM pll WHERE SOCode = '" + soCode + "'");

    while(rs.next())
    {    
      if(checkCodeExists(soCode, con, stmt, rs, rs.getString(1), 'P'))
        numEntries = addToList(rs.getString(1), 'P', soCode, numEntries, codes, types, sourceDocs, searched, codesLen, sourceDocsLen);
    }
    
    if(stmt != null) stmt.close();
    
    return numEntries;
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int searchDOLinesGivenSOCode(Connection con, Statement stmt, ResultSet rs, String soCode, String[][] codes, char[][] types, String[][] sourceDocs, boolean[][] searched, int[] codesLen, int[] sourceDocsLen, int numEntries) throws Exception
  {
    if(! checkCodeExists(soCode, con, stmt, rs, soCode, 'S')) return numEntries;   

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT DOCode FROM dol WHERE SOCode = '" + soCode + "'");

    while(rs.next())
    {    
      if(checkCodeExists(soCode, con, stmt, rs, rs.getString(1), 'D'))
        numEntries = addToList(rs.getString(1), 'D', soCode, numEntries, codes, types, sourceDocs, searched, codesLen, sourceDocsLen);
    }
    
    if(stmt != null) stmt.close();
    
    return numEntries;
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int searchInvoiceLinesGivenSOCode(Connection con, Statement stmt, ResultSet rs, String soCode, String[][] codes, char[][] types, String[][] sourceDocs, boolean[][] searched, int[] codesLen, int[] sourceDocsLen, int numEntries)
                                            throws Exception
  {
    if(! checkCodeExists(soCode, con, stmt, rs, soCode, 'S')) return numEntries;   

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT InvoiceCode FROM invoicel WHERE SOCode = '" + soCode + "'");

    while(rs.next())
    {    
      if(checkCodeExists(soCode, con, stmt, rs, rs.getString(1), 'I'))
        numEntries = addToList(rs.getString(1), 'I', soCode, numEntries, codes, types, sourceDocs, searched, codesLen, sourceDocsLen);
    }
    
    if(stmt != null) stmt.close();
    
    return numEntries;
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int searchPOLinesGivenSOCode(Connection con, Statement stmt, ResultSet rs, String soCode, String[][] codes, char[][] types, String[][] sourceDocs, boolean[][] searched, int[] codesLen, int[] sourceDocsLen, int numEntries) throws Exception
  {
    if(! checkCodeExists(soCode, con, stmt, rs, soCode, 'S')) return numEntries;   
 
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT POCode FROM pol WHERE SOCode = '" + soCode  + "'");

    while(rs.next())
    {    
      if(checkCodeExists(soCode, con, stmt, rs, rs.getString(1), 'Y'))
        numEntries = addToList(rs.getString(1), 'Y', soCode, numEntries, codes, types, sourceDocs, searched, codesLen, sourceDocsLen);
    }

    if(stmt != null) stmt.close();
    
    return numEntries;
  }
  
  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int searchLPLinesGivenSOCode(Connection con, Statement stmt, ResultSet rs, String soCode, String[][] codes, char[][] types, String[][] sourceDocs, boolean[][] searched, int[] codesLen, int[] sourceDocsLen, int numEntries) throws Exception
  {
    if(! checkCodeExists(soCode, con, stmt, rs, soCode, 'S')) return numEntries;   

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT LPCode FROM lpl WHERE SOCode = '" + soCode  + "'");

    while(rs.next())
    {    
      if(checkCodeExists(soCode, con, stmt, rs, rs.getString(1), 'Z'))
        numEntries = addToList(rs.getString(1), 'Z', soCode, numEntries, codes, types, sourceDocs, searched, codesLen, sourceDocsLen);
    }

    if(stmt != null) stmt.close();
    
    return numEntries;
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int searchGRNLinesGivenPOCode(Connection con, Statement stmt, ResultSet rs, String poCode, String[][] codes, char[][] types, String[][] sourceDocs, boolean[][] searched, int[] codesLen, int[] sourceDocsLen, int numEntries) throws Exception
  {
    if(! checkCodeExists(poCode, con, stmt, rs, poCode, 'Y')) return numEntries;   

    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT GRCode FROM grl WHERE POCode = '" + poCode + "'");
    
    while(rs.next())
    {    
      if(checkCodeExists(poCode, con, stmt, rs, rs.getString(1), 'G'))
        numEntries = addToList(rs.getString(1), 'G', poCode, numEntries, codes, types, sourceDocs, searched, codesLen, sourceDocsLen);
    }
    
    if(stmt != null) stmt.close();
    
    return numEntries;
  }
  
  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int searchPILinesGivenGRCode(Connection con, Statement stmt, ResultSet rs, String grCode, String[][] codes, char[][] types, String[][] sourceDocs, boolean[][] searched, int[] codesLen, int[] sourceDocsLen, int numEntries) throws Exception
  {
    if(! checkCodeExists(grCode, con, stmt, rs, grCode, 'G')) return numEntries;

    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT InvoiceCode FROM pinvoicel WHERE GRCode = '" + grCode + "'");
    
    while(rs.next())
    {    
      if(checkCodeExists(grCode, con, stmt, rs, rs.getString(1), 'J'))
        numEntries = addToList(rs.getString(1), 'J', grCode, numEntries, codes, types, sourceDocs, searched, codesLen, sourceDocsLen);
    }
    
    if(stmt != null) stmt.close();
    
    return numEntries;
  }
  
  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private double chkInvoiceLines(boolean useWAC, Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String invoiceCode, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.Amount, t2.ItemCode, t2.Quantity, t1.Date FROM invoicel AS t2 INNER JOIN invoice AS t1 ON t1.InvoiceCode = t2.InvoiceCode WHERE t2.InvoiceCode = '" + invoiceCode + "'");

    double baseTotal = 0.0, baseAmount, quantity;
    String itemCode, date, dateFrom, s;
    String[] financialYearStartMonth = new String[1];
    String[] financialYearEndMonth   = new String[1];
    int startMonth;
    
    while(rs.next())
    {    
      itemCode = rs.getString(2);

      if(itemExists(con, stmt2, rs2, itemCode))
      {      
        if(useWAC)
        {
          quantity = generalUtils.doubleFromStr(rs.getString(3));
          date     = rs.getString(4);
        
          definitionTables.getAppConfigFinancialYearMonths(con, stmt2, rs2, dnm, financialYearStartMonth, financialYearEndMonth);

          startMonth = generalUtils.detMonthNumFromMonthName(financialYearStartMonth[0]);
          if(startMonth < 10)
            s = "-0" + startMonth;
          else s = "-" + startMonth;

          dateFrom = accountsUtils.getAccountingYearForADate(con, stmt2, rs2, generalUtils.convertFromYYYYMMDD(date), dnm, localDefnsDir, defnsDir) + s + "-01";

          baseAmount = inventory.getWAC(con, stmt2, rs2, itemCode, dateFrom, date, dnm);

          baseAmount *= quantity;
        }
        else baseAmount = generalUtils.doubleFromStr(rs.getString(1));

        baseTotal += baseAmount;
      }        
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return baseTotal;
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean itemExists(Connection con, Statement stmt, ResultSet rs, String itemCode) throws Exception
  {
    if(itemCode.length() == 0) // just-in-case
      return false;

    if(itemCode.equals("-")) // quick check
      return false;

    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM stock WHERE ItemCode = '" + itemCode + "'");

    int numRecs = 0;
    
    if(rs.next())
      numRecs = rs.getInt("rowcount") ;

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    if(numRecs == 1)
      return true;
    
    return false;
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private double chkSalesOrder(Connection con, Statement stmt, ResultSet rs, String soCode) throws Exception 
  {    
    stmt = con.createStatement();
     
    rs = stmt.executeQuery("SELECT BaseTotalTotal FROM so WHERE SOCode = '" + soCode + "'");

    double baseAmount = 0.0;
     
    if(rs.next()) // just-in-case
    {
      baseAmount = generalUtils.doubleFromStr(generalUtils.doubleDPs(rs.getString(1), '2'));
    }
         
    if(stmt != null) stmt.close();
    
    return baseAmount;
  }
  
  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private double chkPurchaseInvoiceLines(Connection con, Statement stmt, Statement stmt2, Statement stmt3, Statement stmt4, ResultSet rs, ResultSet rs2, ResultSet rs3, ResultSet rs4, String soCode) throws Exception
  {
    double baseTotal = 0.0;

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.POCode FROM pol AS t2 INNER JOIN po AS t1 ON t1.POCode = t2.POCode WHERE t2.SOCode = '" + soCode + "' AND t1.Status != 'C'");

    String poCode, grCode, grLine, itemCode;
    
    while(rs.next())
    {    
      poCode = rs.getString(1);

      stmt2 = con.createStatement();

      rs2 = stmt2.executeQuery("SELECT t2.GRCode, t2.Line FROM grl AS t2 INNER JOIN gr AS t1 ON t1.GRCode = t2.GRCode WHERE t2.POCode = '" + poCode + "' AND t1.Status != 'C'");
    
      while(rs2.next())
      {    
        grCode = rs2.getString(1);
        grLine = rs2.getString(2);

        stmt3 = con.createStatement();

        rs3 = stmt3.executeQuery("SELECT t2.Amount, t2.ItemCode, t2.Quantity, t1.Date FROM pinvoicel AS t2 INNER JOIN pinvoice AS t1 ON t1.InvoiceCode = t2.InvoiceCode WHERE t2.GRCode = '" + grCode + "' AND t2.GRLine = '" + grLine + "' AND t1.Status != 'C'");

        while(rs3.next())
        {    
          itemCode = rs3.getString(2);

          if(! itemExists(con, stmt4, rs4, itemCode))
          {
            baseTotal += generalUtils.doubleFromStr(rs3.getString(1));
          }
        }

        if(rs3   != null) rs3.close();
        if(stmt3 != null) stmt3.close();
      }

      if(rs2   != null) rs2.close();
      if(stmt2 != null) stmt2.close();
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return baseTotal;
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private double chkInvoiceLinesForInvoicedAmount(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, String soCode) throws Exception
  {
    double baseTotal = 0.0, amt, gstRate;

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT t2.Amount, t2.GSTRate  ,t1.InvoiceCode     FROM invoicel AS t2 INNER JOIN invoice AS t1 ON t1.InvoiceCode = t2.InvoiceCode WHERE t1.Status != 'C' AND t2.SOCode = '" + soCode + "'");

    String gstRateName;
    
    while(rs.next())
    {
      gstRateName = rs.getString(2);
      gstRate     = accountsUtils.getGSTRate(con, stmt2, rs2, gstRateName);
              
      amt = generalUtils.doubleFromStr(generalUtils.doubleDPs(rs.getString(1), '2'));
      amt += generalUtils.doubleDPs((amt * gstRate), '2');
      baseTotal += amt;
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return baseTotal;
  }

}
