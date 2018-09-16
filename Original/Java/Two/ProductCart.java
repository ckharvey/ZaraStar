// =======================================================================================================================================================================================================
// System: ZaraStar Product: cart 
// Module: ProductCart.java
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
import java.io.*;
import java.sql.*;

public class ProductCart extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  DashboardUtils dashboardUtils = new DashboardUtils();
  MiscDefinitions miscDefinitions = new MiscDefinitions();
  AdminControlUtils adminControlUtils = new AdminControlUtils();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="";

    try
    {
      directoryUtils.setContentHeaders(res);
      out = res.getWriter();

      unm = req.getParameter("unm");
      uty = req.getParameter("uty");
      sid = req.getParameter("sid");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");
      
      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "ProductCart", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 121, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush(); 
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut)
                    throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;
    
    if((uty.equals("R") && ! adminControlUtils.notDisabled(con, stmt, rs, 908)) || (uty.equals("A") && ! adminControlUtils.notDisabled(con, stmt, rs, 808)) || (uty.equals("I") && ! authenticationUtils.verifyAccess(con, stmt, rs, req, 121, unm, uty, dnm, localDefnsDir, defnsDir)))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "ProductCart", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 121, bytesOut[0], 0, "ACC:");
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      if(uty.equals("A"))
      {
        String sessionsDir = directoryUtils.getSessionsDir(dnm);
        sid = serverUtils.newSessionID(unm, "A", dnm, sessionsDir, localDefnsDir, defnsDir);
        den = dnm;
        unm = "_" + sid;
        StringBuffer url = req.getRequestURL();
        int x=0;
        if(url.charAt(x) == 'h' || url.charAt(x) == 'H')
          x += 7;
        men="";
        while(url.charAt(x) != '\000' && url.charAt(x) != ',' && url.charAt(x) != '/')
          men += url.charAt(x++);
      }
      else
      {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "ProductCart", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 121, bytesOut[0], 0, "SID:");
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }
    }
    
    String cartTable = createCartTable(con, stmt, unm);

    create(con, stmt, rs, out, req, cartTable, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 121, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void create(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String cartTable, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir,
                      String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Product Cart</title>");
    
    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">"); 
    
    // update record to DB
    scoutln(out, bytesOut, "var req4;");    
    scoutln(out, bytesOut, "function initRequest4(url)");
    scoutln(out, bytesOut, "{if(window.XMLHttpRequest){req4=new XMLHttpRequest();}else");
    scoutln(out, bytesOut, "if(window.ActiveXObject){req4=new ActiveXObject(\"Microsoft.XMLHTTP\");}}");

    scoutln(out, bytesOut, "function updateN(){update('N');}");
    scoutln(out, bytesOut, "function updateE(){update('E');}");
    scoutln(out, bytesOut, "function update(newOrEdit){");
    scoutln(out, bytesOut, "var line=sanitise(document.forms[0].line.value);");
    scoutln(out, bytesOut, "var entry=sanitise(document.forms[0].entry.value);");
    scoutln(out, bytesOut, "var itemCode=sanitise(document.forms[0].itemCode.value);");
    scoutln(out, bytesOut, "var mfr=sanitise(document.forms[0].mfr.value);");
    scoutln(out, bytesOut, "var mfrCode=sanitise(document.forms[0].mfrCode.value);");
    scoutln(out, bytesOut, "var desc=sanitise(document.forms[0].desc.value);");
    scoutln(out, bytesOut, "var qty=sanitise(document.forms[0].qty.value);");
    scoutln(out, bytesOut, "var uom=sanitise(document.forms[0].uom.value);");
    scoutln(out, bytesOut, "var price=sanitise(document.forms[0].price.value);");
    scoutln(out, bytesOut, "var currency=sanitise(document.forms[0].currency.value);");
    scoutln(out, bytesOut, "var url=\"http://" + men + "/central/servlet/ProductCartUpdate?unm=\" + escape('" + unm + "') + \"&sid=\" + escape('" + sid + "') + \"&uty=\" + escape('" + uty + "') + \"&men=\" + escape('" + men + "') + \"&den=\" + escape('"
                         + den + "') + \"&bnm=\" + escape('" + bnm + "') + \"&p1=\" + line + \"&p2=\" + entry + \"&p3=\" + itemCode + \"&p4=\" + mfr "
                         + "+ \"&p5=\" + mfrCode + \"&p6=\" + desc + \"&p7=\" + qty + \"&p8=\" + uom + \"&p9=\" + price + \"&p10=\" + currency + \"&p11=" + cartTable + "&p12=\" + newOrEdit + \"&dnm=\" + escape('" + dnm + "');");
    scoutln(out, bytesOut, "initRequest4(url);");
    scoutln(out, bytesOut, "req4.onreadystatechange = processRequest4;");
    scoutln(out, bytesOut, "req4.open(\"GET\",url,true);");
    scoutln(out, bytesOut, "req4.send(null);}");

    scoutln(out, bytesOut, "function processRequest4(){");
    scoutln(out, bytesOut, "if(req4.readyState==4){");
    scoutln(out, bytesOut, "if(req4.status==200){");
    scoutln(out, bytesOut, "var res=req4.responseXML.getElementsByTagName(\"res\")[0].childNodes[0].nodeValue;");
    scoutln(out, bytesOut, "if(res.length>0){");
    scoutln(out, bytesOut, "if(res=='.')");
    scoutln(out, bytesOut, "refetchPage();else{var messageElement=document.getElementById('msg');");
    scoutln(out, bytesOut, "messageElement.innerHTML='<span id=\"textErrorLarge\">'+res+'</span>';");
    scoutln(out, bytesOut, "}}}}}");
      
    // delete record from DB
    scoutln(out, bytesOut, "var req3;");    
    scoutln(out, bytesOut, "function initRequest3(url)");
    scoutln(out, bytesOut, "{if(window.XMLHttpRequest){req3=new XMLHttpRequest();}else");
    scoutln(out, bytesOut, "if(window.ActiveXObject){req3=new ActiveXObject(\"Microsoft.XMLHTTP\");}}");

    scoutln(out, bytesOut, "function del(line){");
    scoutln(out, bytesOut, "var url = \"http://" + men + "/central/servlet/ProductCartDeleteItem?unm=\" + escape('" + unm + "') + \"&sid=\" + escape('" + sid + "') + \"&uty=\" + escape('" + uty + "') + \"&men=\" + escape('" + men + "') + \"&den=\" + escape('"
                         + den + "') + \"&bnm=\" + escape('" + bnm + "') + \"&p1=\" + escape(line) + \"&p2=" + cartTable + "&dnm=\" + escape('" + dnm + "');");
    scoutln(out, bytesOut, "initRequest3(url);");
    scoutln(out, bytesOut, "req3.onreadystatechange=processRequest3;");
    scoutln(out, bytesOut, "req3.open(\"GET\", url, true);");
    scoutln(out, bytesOut, "req3.send(null);}");

    scoutln(out, bytesOut, "function processRequest3(){");
    scoutln(out, bytesOut, "if(req3.readyState==4){");
    scoutln(out, bytesOut, "if(req3.status==200){");
    scoutln(out, bytesOut, "var res=req3.responseXML.getElementsByTagName(\"res\")[0].childNodes[0].nodeValue;");
    scoutln(out, bytesOut, "if(res.length>0){");
    scoutln(out, bytesOut, "if(res=='.')");
    scoutln(out, bytesOut, "refetchPage();else{ var messageElement = document.getElementById('msg');");
    scoutln(out, bytesOut, "messageElement.innerHTML = '<span id=\"textErrorLarge\">'+res+'</span>';");
    scoutln(out, bytesOut, "}}}}}");
    
    // delete cart table
    scoutln(out, bytesOut, "var req5;");    
    scoutln(out, bytesOut, "function initRequest5(url)");
    scoutln(out, bytesOut, "{if(window.XMLHttpRequest){req5=new XMLHttpRequest();}else");
    scoutln(out, bytesOut, "if(window.ActiveXObject){req5=new ActiveXObject(\"Microsoft.XMLHTTP\");}}");

    scoutln(out, bytesOut, "function delCart(){");
    scoutln(out, bytesOut, "var url = \"http://" + men + "/central/servlet/ProductCartDeleteCart?unm=\" + escape('" + unm + "') + \"&sid=\" + escape('" + sid + "') + \"&uty=\" + escape('" + uty + "') + \"&men=\" + escape('" + men + "') + \"&den=\" + escape('"
                         + den + "') + \"&bnm=\" + escape('" + bnm + "') + \"&p1=" + cartTable + "&dnm=\" + escape('" + dnm + "');");
    scoutln(out, bytesOut, "initRequest5(url);");
    scoutln(out, bytesOut, "req5.onreadystatechange=processRequest5;");
    scoutln(out, bytesOut, "req5.open(\"GET\", url, true);");
    scoutln(out, bytesOut, "req5.send(null);}");

    scoutln(out, bytesOut, "function processRequest5(){");
    scoutln(out, bytesOut, "if(req5.readyState==4){");
    scoutln(out, bytesOut, "if(req5.status==200){");
    scoutln(out, bytesOut, "var res=req5.responseXML.getElementsByTagName(\"res\")[0].childNodes[0].nodeValue;");
    scoutln(out, bytesOut, "if(res.length>0){");
    scoutln(out, bytesOut, "if(res=='.')");
    scoutln(out, bytesOut, "refetchPage();else{ var messageElement = document.getElementById('msg');");
    scoutln(out, bytesOut, "messageElement.innerHTML = '<span id=\"textErrorLarge\">'+res+'</span>';");
    scoutln(out, bytesOut, "}}}}}");
    
    // fetch rec for edit
    scoutln(out, bytesOut, "var req2;");    
    scoutln(out, bytesOut, "function initRequest2(url)");
    scoutln(out, bytesOut, "{if(window.XMLHttpRequest){req2=new XMLHttpRequest();}else");
    scoutln(out, bytesOut, "if(window.ActiveXObject){req2=new ActiveXObject(\"Microsoft.XMLHTTP\");}}");

    scoutln(out, bytesOut, "function edit(line){");
    scoutln(out, bytesOut, "var url = \"http://" + men + "/central/servlet/ProductCartFetchLineEdit?unm=\" + escape('" + unm + "') + \"&sid=\" + escape('" + sid + "') + \"&uty=\" + escape('" + uty + "') + \"&men=\" + escape('" + men + "') + \"&den=\" + escape('"
                         + den + "') + \"&bnm=\" + escape('" + bnm + "') + \"&p1=\" + line + \"&p2=" + cartTable + "&dnm=\" + escape('" + dnm + "');");
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
    scoutln(out, bytesOut, "var line=req2.responseXML.getElementsByTagName(\"line\")[0].childNodes[0].nodeValue;document.forms[0].line.value=line;");
    scoutln(out, bytesOut, "var entry=req2.responseXML.getElementsByTagName(\"entry\")[0].childNodes[0].nodeValue;document.forms[0].entry.value=entry;");
    scoutln(out, bytesOut, "var itemCode=req2.responseXML.getElementsByTagName(\"itemCode\")[0].childNodes[0].nodeValue;document.forms[0].itemCode.value=itemCode;");
    scoutln(out, bytesOut, "var mfr=req2.responseXML.getElementsByTagName(\"mfr\")[0].childNodes[0].nodeValue;document.forms[0].mfr.value=mfr;");
    scoutln(out, bytesOut, "var mfrCode=req2.responseXML.getElementsByTagName(\"mfrCode\")[0].childNodes[0].nodeValue;document.forms[0].mfrCode.value=mfrCode;");
    scoutln(out, bytesOut, "var desc=req2.responseXML.getElementsByTagName(\"desc\")[0].childNodes[0].nodeValue;document.forms[0].desc.value=desc;");
    scoutln(out, bytesOut, "var qty=req2.responseXML.getElementsByTagName(\"qty\")[0].childNodes[0].nodeValue;document.forms[0].qty.value=qty;");
    scoutln(out, bytesOut, "var uom=req2.responseXML.getElementsByTagName(\"uom\")[0].childNodes[0].nodeValue;document.forms[0].uom.value=uom;");
    scoutln(out, bytesOut, "var price=req2.responseXML.getElementsByTagName(\"price\")[0].childNodes[0].nodeValue;document.forms[0].price.value=price;");
    scoutln(out, bytesOut, "var currency=req2.responseXML.getElementsByTagName(\"currency\")[0].childNodes[0].nodeValue;document.forms[0].currency.value=currency;");
    scoutln(out, bytesOut, "var messageElement = document.getElementById('option');");
    scoutln(out, bytesOut, "messageElement.innerHTML='<p><a href=\"javascript:updateE()\">Update the Line</a>';");

    scoutln(out, bytesOut, "messageElement=document.getElementById('linebox');");
    scoutln(out, bytesOut, "messageElement.innerHTML='<input type=hidden name=code value=\"'+line+'\">'+line;");
    
    if(uty.equals("I"))
    {
      scoutln(out, bytesOut, "document.forms[0].uom.value=uom;");
      scoutln(out, bytesOut, "document.forms[0].price.value=price;");
      scoutln(out, bytesOut, "document.forms[0].currency.value=currency;");
    }
    else
    {
      scoutln(out, bytesOut, "messageElement=document.getElementById('uombox');");
      scoutln(out, bytesOut, "messageElement.innerHTML='<input type=hidden name=uom value=\"'+uom+'\">'+uom;");
    
      scoutln(out, bytesOut, "messageElement=document.getElementById('pricebox');");
      scoutln(out, bytesOut, "messageElement.innerHTML='<input type=hidden name=price value=\"'+price+'\">'+price;");
    
      scoutln(out, bytesOut, "messageElement=document.getElementById('currencybox');");
      scoutln(out, bytesOut, "messageElement.innerHTML='<input type=hidden name=currency value=\"'+currency+'\">'+currency;");
    } 
    
    scoutln(out, bytesOut, "messageElement=document.getElementById('msg');messageElement.innerHTML='';");
    scoutln(out, bytesOut, "}else{var messageElement=document.getElementById('msg');");
    scoutln(out, bytesOut, "messageElement.innerHTML='<span id=\"textErrorLarge\">'+res+'</span>';");
    scoutln(out, bytesOut, "}}}}}");
      
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

    scoutln(out, bytesOut, "function refetchPage(){");
    scoutln(out, bytesOut, "this.location.replace(\"/central/servlet/ProductCart?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\");}");
    
    if(! uty.equals("I"))
    {
      scoutln(out, bytesOut, "function checkout(){");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/ExternalUserCartCheckout?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\";}");
    }
    else
    {
      scoutln(out, bytesOut, "function toDoc(servletToCall){");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/_\"+servletToCall+\"?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=CREATEBASEDONCART&bnm=" + bnm + "\";}");
    }
    
    scoutln(out, bytesOut, "</script>");

    outputPageFrame(con, stmt, rs, out, req, "", "Product Cart", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, bytesOut);
 
    scoutln(out, bytesOut, "<form><table id=\"page\" border=0 cellpadding=2 cellspacing=0 width=100%>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td colspan=7><span id='msg'></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr id=\"pageColumn\"><td id=\"pageColumn\">&nbsp;</td>");
    scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap align=center><p>Line &nbsp;</td>");

    if(uty.equals("I"))
      scoutln(out, bytesOut, "<td id=\"pageColumn\" nowrap><p>Entry &nbsp;</td>");
    
    scoutln(out, bytesOut, "<td id=\"pageColumn\"><p>Our Code</td>");
    scoutln(out, bytesOut, "<td id=\"pageColumn\"><p>Manufacturer/Code</td>");
    scoutln(out, bytesOut, "<td id=\"pageColumn\"><p>Description</td>");
    scoutln(out, bytesOut, "<td id=\"pageColumn\"><p align=center>Quantity</td>");
    scoutln(out, bytesOut, "<td id=\"pageColumn\"><p align=center>UoM</td>");
    scoutln(out, bytesOut, "<td id=\"pageColumn\"><p align=center>Currency</td>");
    scoutln(out, bytesOut, "<td id=\"pageColumn\"><p align=right>Price</td></tr>");
        
    list(con, stmt, rs, out, cartTable, uty, miscDefinitions.dpOnQuantities(con, stmt, rs, "5"), bytesOut);
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=10><hr></td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    
    scoutln(out, bytesOut, "<tr><td></td>");
    scoutln(out, bytesOut, "<td><span id=\"linebox\"><input type=text name=line size=3 maxlength=4></span></td>");

    if(uty.equals("I"))
      scoutln(out, bytesOut, "<td><input type=text name=entry size=5 maxlength=6></td>");
    else scoutln(out, bytesOut, "<input type=hidden name=entry value=''>");
    
    scoutln(out, bytesOut, "<td><input type=text name=itemCode size=10 maxlength=20></td>");
    
    scoutln(out, bytesOut, "<td>");
    getMfrsDDL(con, stmt, rs, out, uty, bytesOut);
    scoutln(out, bytesOut, "</td>");

    scoutln(out, bytesOut, "<td><input type=text name=desc size=40 maxlength=80></td>");
    scoutln(out, bytesOut, "<td align=center><input type=text name=qty size=4 maxlength=10></td>");

    if(uty.equals("I"))
    {
      scoutln(out, bytesOut, "<td align=center><input type=text name=uom size=10 maxlength=20></td>");
      scoutln(out, bytesOut, "<td align=center>");
      getCurrenciesDDL(con, stmt, rs, out, bytesOut);
      scoutln(out, bytesOut, "</td>");

      scoutln(out, bytesOut, "<td align=center><input type=text name=price size=10 maxlength=11></td></tr>");
    }
    else
    {
      scoutln(out, bytesOut, "<td align=center><p><span id=\"uombox\"><input type=hidden name=uom></span></td>");
      scoutln(out, bytesOut, "<td align=center><p><span id=\"pricebox\"><input type=hidden name=price></span></td>");
      scoutln(out, bytesOut, "<td align=center><p><span id=\"currencybox\"><input type=hidden name=currency></span></td>");
    }
      
    if(uty.equals("I"))
      scoutln(out, bytesOut, "<tr><td colspan=4></td>");
    else scoutln(out, bytesOut, "<tr><td colspan=3></td>");
    scoutln(out, bytesOut, "<td><input type=text name=mfrCode size=20 maxlength=60></td></tr>");
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=3><span id=\"option\"><p><a href=\"javascript:updateN()\">Add the Line</a></span></td></tr>");

    if(! uty.equals("I"))
      scoutln(out, bytesOut, "<tr><td colspan=3><span id=\"option\"><p><a href=\"javascript:checkout()\">Checkout</a></span></td></tr>");
    
    scoutln(out, bytesOut, "<tr><td colspan=3><span id=\"option\"><p><a href=\"javascript:delCart()\">Remove All</a></span></td></tr>");
    
    scoutln(out, bytesOut, "<tr><td colspan=9><p>Notes:</td></tr>");

    scoutln(out, bytesOut, "<tr><td colspan=9><p>Enter <i>either</i> <b>Our Code</b>, <i>or</i> a <b>Manufacturer</b> <i>and</i> <b>Manufacturer Code</b> combination.");
    scoutln(out, bytesOut, "Alternatively, leave both codes blank and simply enter your own description.</td></tr>");

    scoutln(out, bytesOut, "<tr><td colspan=9><p>Use the <b>Add the Line</b> option to manually enter a line.</td></tr>");
    
    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir));
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void list(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String cartTable, String uty, char dpsOnQuantities, int[] bytesOut) throws Exception
  {
    try
    {
      stmt = con.createStatement();
  
      rs = stmt.executeQuery("SELECT Line, Entry, ItemCode, Mfr, MfrCode, Description, Quantity, UoM, Price, Currency FROM " + cartTable + " ORDER BY Line");

      String line, entry, itemCode, mfr, mfrCode, desc, qty, uom, price, currency, cssFormat = "";

      while(rs.next())                  
      {
        line     = rs.getString(1);
        entry    = rs.getString(2);
        itemCode = rs.getString(3);
        mfr      = rs.getString(4);
        mfrCode  = rs.getString(5);
        desc     = rs.getString(6);
        qty      = rs.getString(7);
        uom      = rs.getString(8);
        price    = rs.getString(9);
        currency = rs.getString(10);
        
        if(uom == null) uom = "";
        if(currency == null) currency = "";
        if(price == null || price.length() == 0) price = "poa";
        
        if(cssFormat.equals("line2")) cssFormat = "line1"; else cssFormat = "line2";
        
        scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\"><td></td>");
        scoutln(out, bytesOut, "<td align=center>" + line + "</td>");

        if(uty.equals("I"))
          scoutln(out, bytesOut, "<td align=center>" + entry + "</td>");
        
        scoutln(out, bytesOut, "<td>" + itemCode + "</td>");
        scoutln(out, bytesOut, "<td>" + mfr + "</td>");
        scoutln(out, bytesOut, "<td>" + desc + "</td>");
        scoutln(out, bytesOut, "<td align=center>" + generalUtils.doubleDPs(qty, dpsOnQuantities) + "</td>");

        scoutln(out, bytesOut, "<td align=center>" + uom + "</td>");
        scoutln(out, bytesOut, "<td align=center>" + currency + "</td>");
        
        if(price.equals("poa"))
          scoutln(out, bytesOut, "<td align=right>poa</td>");
        else scoutln(out, bytesOut, "<td align=right>" + generalUtils.doubleDPs(price, '2') + "</td>");

        scoutln(out, bytesOut, "<td><p><a href=\"javascript:edit('" + line + "')\">Edit</a>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
        scoutln(out, bytesOut, "<a href=\"javascript:del('" + line + "')\">Delete</a></td></tr>");

        if(uty.equals("I"))
          scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\"><td colspan=4></td>");
        else scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\"><td colspan=3></td>");

        scoutln(out, bytesOut, "<td>" + mfrCode + "</td>");
        scoutln(out, bytesOut, "<td colspan=7></td></tr>");
      }
                 
      if(rs   != null) rs.close();                                 
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(rs   != null) rs.close();                                 
      if(stmt != null) stmt.close();
    }    
  }  

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // checks that mfrs on the linkedcat table are also in the list
  private void getMfrsDDL(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String uty, int[] bytesOut) throws Exception
  {
    int numMfrs = 0;
    String mfr;
    byte[] mfrB = new byte[100];
    byte[] list = new byte[1000];
    int[] listLen = new int[1]; listLen[0] = 1000;
      
    try
    {
      stmt = con.createStatement();

      String where = "";
      if(! uty.equals("I"))
        where = "WHERE ShowToWeb = 'Y'";
      
      rs = stmt.executeQuery("SELECT DISTINCT Manufacturer FROM stock " + where + " ORDER BY Manufacturer");

      while(rs.next())
      {
        mfr = rs.getString(1);
    
        if(mfr.length() > 0)
        {
          generalUtils.strToBytes(mfrB, (mfr + "\001"));
          list = generalUtils.addToList(mfrB, list, listLen);
          ++numMfrs;
        }
      }
            
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(rs   != null) rs.close();                                 
      if(stmt != null) stmt.close();
    }

    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT Manufacturer FROM linkedcat");

      while(rs.next())
      {
        mfr = rs.getString(1);
    
        if(mfr.length() > 0)
        {
          generalUtils.strToBytes(mfrB, (mfr + "\001"));
          list = generalUtils.addToList(false, mfrB, list, listLen);
          ++numMfrs;
        }
      }
            
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
      
      scoutln(out, bytesOut, "<select name=\"mfr\">");

      for(int x=0;x<numMfrs;++x)
      {
        generalUtils.getListEntryByNum(x, list, mfrB);
        mfr = generalUtils.stringFromBytes(mfrB, 0L);
        scoutln(out, bytesOut, "<option value=\"" + mfr + "\">" + mfr);
      }

      scoutln(out, bytesOut, "</select>");
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(rs   != null) rs.close();                                 
      if(stmt != null) stmt.close();
    }
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void getCurrenciesDDL(Connection con, Statement stmt, ResultSet rs, PrintWriter out, int[] bytesOut) throws Exception
  {
    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT CurrencyCode, CurrencyDesc FROM currency ORDER BY CurrencyCode"); 

      scoutln(out, bytesOut, "<select name=\"currency\">");
    
      String currency;
    
      while(rs.next())
      {
        currency = rs.getString(1);
        scoutln(out, bytesOut, "<option value=\"" + currency + "\">" + currency);
      }

      scoutln(out, bytesOut, "</select>");
     
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(rs   != null) rs.close();                                 
      if(stmt != null) stmt.close();
    }    
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String createCartTable(Connection con, Statement stmt, String unm) throws Exception
  {
    String tmpTable = "";

    try
    {
      stmt = con.createStatement();

      if(unm.startsWith("_")) // anon user
        tmpTable = unm.substring(1) + "_cart_tmp";
      else
      {
        int i = unm.indexOf("_");
        if(i != -1) // registered user
          tmpTable = unm.substring(0, i) + "_cart_tmp";
        else tmpTable = unm + "_cart_tmp";
      }

      directoryUtils.createTmpTable(false, con, stmt, "Line integer, Entry char(6), ItemCode char(20), Mfr char(40), MfrCode char(60), Description char(80), Quantity decimal(17,8), UoM char(20), Price decimal(19,8), Currency char(3)", "", tmpTable);

      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(stmt != null) stmt.close();
    }    

    return tmpTable;
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void outputPageFrame(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String bodyStr, String title, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                               String otherSetup, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    int[] hmenuCount = new int[1];

    String subMenuText = dashboardUtils.buildSubMenuText(con, stmt, rs, req, "121", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount);

    subMenuText += "<table id='title' width=100%><tr><td colspan=2 nowrap><p>" + title + directoryUtils.buildHelp(121) + "</td></tr></table>";

    if(uty.equals("I"))
      subMenuText += buildSubMenuText(con, stmt, rs, req, unm, uty, dnm, localDefnsDir, defnsDir, hmenuCount);

    scoutln(out, bytesOut, authenticationUtils.buildScreen(con, stmt, rs, req, true, unm, sid, uty, men, den, dnm, bnm, "ProductCart", subMenuText, hmenuCount[0], bodyStr, otherSetup, localDefnsDir, defnsDir));

    scoutln(out, bytesOut, "<table border=0><tr><td valign=top><div id='second' style=\"{margin-top:0px;}\"></div></td>");
    scoutln(out, bytesOut, "<td valign=top><div id='third' style=\"{margin-top:0px;margin-left:0px;}\"></div></td></tr></table>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String buildSubMenuText(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, String unm, String uty, String dnm, String localDefnsDir, String defnsDir, int[] hmenuCount) throws Exception
  {
    String s = "<div id='submenu'>";

    ++hmenuCount[0];

    boolean QuotationPage = authenticationUtils.verifyAccess(con, stmt, rs, req, 4019, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean SalesOrderPage = authenticationUtils.verifyAccess(con, stmt, rs, req, 4031, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean GoodsReceivedPickingList = authenticationUtils.verifyAccess(con, stmt, rs, req, 3038, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean DeliveryOrderPage = authenticationUtils.verifyAccess(con, stmt, rs, req, 4054, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean ProformaInvoicePage = authenticationUtils.verifyAccess(con, stmt, rs, req, 4080, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean SalesInvoicePage = authenticationUtils.verifyAccess(con, stmt, rs, req, 4067, unm, uty, dnm, localDefnsDir, defnsDir);

    s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0] + "');\">Convert</dt><dd id='hmenu" + hmenuCount[0]++ + "'><ul>";
    if(QuotationPage || SalesOrderPage || GoodsReceivedPickingList || DeliveryOrderPage || ProformaInvoicePage || SalesInvoicePage) // || PurchaseOrderPage || LocalPurchasePage)
    {
      if(QuotationPage)
        s += "<li><a href=\"javascript:toDoc('4019')\">Quotation</a></li>\n";
      if(SalesOrderPage)
        s += "<li><a href=\"javascript:toDoc('4031')\">Sales Order</a></li>\n";
      if(GoodsReceivedPickingList)
        s += "<li><a href=\"javascript:toDoc('3038')\">Picking List</a></li>";
      if(DeliveryOrderPage)
        s += "<li><a href=\"javascript:toDoc('4054')\">Delivery Order</a></li>\n";
      if(ProformaInvoicePage)
        s += "<li><a href=\"javascript:toDoc('4080')\">Proforma Invoice</a></li>";
      if(SalesInvoicePage)
        s += "<li><a href=\"javascript:toDoc('4067')\">Sales Invoice</a></li>";
    }
    s += "</ul></dd></dl>\n";

    s += "</div>";
    
    --hmenuCount[0];
    
    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
