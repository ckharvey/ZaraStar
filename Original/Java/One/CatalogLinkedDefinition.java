// =======================================================================================================================================================================================================
// System: ZaraStar Admin: Linked Catalogs Definition
// Module: CatalogLinkedDefinition.java
// Author: C.K.Harvey
// Copyright (c) 2003-08 Christopher Harvey. All Rights Reserved.
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

public class CatalogLinkedDefinition extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  AdminUtils adminUtils = new AdminUtils();
  AccountsUtils accountsUtils = new AccountsUtils();

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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "CatalogLinkedDefinition", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2011, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm,
                    String bnm, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    
    Statement stmt = null;
    ResultSet rs   = null;
    
    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 2011, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "CatalogLinkedDefinition", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2011, bytesOut[0], 0, "ACC:");
      
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "CatalogLinkedDefinition", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 2011, bytesOut[0], 0, "SID:");
      
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }
  
    create(con, stmt, rs, out, req, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 2011, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void create(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty,
                      String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Admin - Linked Catalogs Definition</title>");
    
    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\""
                         + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">"); 

    // update record to DB
    scoutln(out, bytesOut, "var req4;");    
    scoutln(out, bytesOut, "function initRequest4(url)");
    scoutln(out, bytesOut, "{if(window.XMLHttpRequest){req4=new XMLHttpRequest();}else");
    scoutln(out, bytesOut, "if(window.ActiveXObject){req4=new ActiveXObject(\"Microsoft.XMLHTTP\");}}");

    scoutln(out, bytesOut, "function updateN(){update('N');}");
    scoutln(out, bytesOut, "function updateE(){update('E');}");
    scoutln(out, bytesOut, "function update(newOrEdit){");
    scoutln(out, bytesOut, "var mfr=sanitise(document.forms[0].mfr.value);");
    scoutln(out, bytesOut, "var desc=sanitise(document.forms[0].desc.value);");
    scoutln(out, bytesOut, "var pricingUpline=sanitise(document.forms[0].pricingUpline.value);");
    scoutln(out, bytesOut, "var catalogURL=sanitise(document.forms[0].catalogURL.value);");
    scoutln(out, bytesOut, "var pricingURL=sanitise(document.forms[0].pricingURL.value);");
    scoutln(out, bytesOut, "var catalogUpline=sanitise(document.forms[0].catalogUpline.value);");
    scoutln(out, bytesOut, "var userName=sanitise(document.forms[0].userName.value);");
    scoutln(out, bytesOut, "var passWord=sanitise(document.forms[0].passWord.value);");
    scoutln(out, bytesOut, "var markup=sanitise(document.forms[0].markup.value);");
    scoutln(out, bytesOut, "var disc1=sanitise(document.forms[0].disc1.value);");
    scoutln(out, bytesOut, "var disc2=sanitise(document.forms[0].disc2.value);");
    scoutln(out, bytesOut, "var disc3=sanitise(document.forms[0].disc3.value);");
    scoutln(out, bytesOut, "var disc4=sanitise(document.forms[0].disc4.value);");
    scoutln(out, bytesOut, "var showPrices='N';if(document.forms[0].showPrices.checked)showPrices='Y';");
    scoutln(out, bytesOut, "var showAvailability='N';if(document.forms[0].showAvailability.checked)showAvailability='Y';");
    scoutln(out, bytesOut, "var catalogType;if(document.forms[0].catalogType[0].checked)catalogType='C';");
    scoutln(out, bytesOut, "else if(document.forms[0].catalogType[1].checked)catalogType='L';else catalogType='B';");
    scoutln(out, bytesOut, "var currency=sanitise(document.forms[0].currency.value);");
    scoutln(out, bytesOut, "var priceBasis;if(document.forms[0].priceBasis[0].checked)priceBasis='L';else priceBasis='D';");
    scoutln(out, bytesOut, "var url = \"http://" + men + "/central/servlet/CatalogLinkedDefinitionUpdate?unm=\" + escape('" + unm + "') + \"&sid=\" + escape('" + sid
                         + "') + \"&uty=\" + escape('" + uty + "') + \"&men=\" + escape('" + men + "') + \"&den=\" + escape('" + den
                         + "') + \"&bnm=\" + escape('" + bnm + "') + \"&p1=\" + mfr + \"&p2=\" + desc + \"&p3=\" + pricingUpline + \"&p4=\""
                         + " + pricingURL + \"&p5=\" + userName + \"&p6=\" + passWord + \"&p7=\" + markup + \"&p8=\" + showPrices + \"&p9=\""
                         + " + showAvailability + \"&p10=\" + catalogURL + \"&p11=\" + catalogUpline + \"&p12=\" + disc1 + \"&p13=\""
                         + " + disc2 + \"&p14=\" + disc3 + \"&p15=\" + disc4 + \"&p16=\" + catalogType + \"&p17=\" + currency + \"&p18=\""
                         + " + priceBasis + \"&p19=\" + newOrEdit + \"&dnm=" + dnm + "\";");
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

    scoutln(out, bytesOut, "function del(mfr){");
    scoutln(out, bytesOut, "var url = \"http://" + men + "/central/servlet/CatalogLinkeDefinitionDelete?unm=\" + escape('" + unm + "') + \"&sid=\" + escape('" + sid
                         + "') + \"&uty=\" + escape('" + uty + "') + \"&men=\" + escape('" + men + "') + \"&den=\" + escape('" + den
                         + "') + \"&bnm=\" + escape('" + bnm + "') + \"&p1=\" + escape(mfr) + \"&dnm=\" + escape('" + dnm + "');");
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
    
    // fetch rec for edit
    scoutln(out, bytesOut, "var req2;");    
    scoutln(out, bytesOut, "function initRequest2(url)");
    scoutln(out, bytesOut, "{if(window.XMLHttpRequest){req2=new XMLHttpRequest();}else");
    scoutln(out, bytesOut, "if(window.ActiveXObject){req2=new ActiveXObject(\"Microsoft.XMLHTTP\");}}");

    scoutln(out, bytesOut, "function edit(mfr){");
    scoutln(out, bytesOut, "var url = \"http://" + men + "/central/servlet/CatalogLinkedDefinitionEdit?unm=\" + escape('" + unm + "') + \"&sid=\" + escape('" + sid
                         + "') + \"&uty=\" + escape('" + uty + "') + \"&men=\" + escape('" + men + "') + \"&den=\" + escape('" + den
                         + "') + \"&bnm=\" + escape('" + bnm + "') + \"&p1=\" + mfr + \"&dnm=\" + escape('" + dnm + "');");
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
    scoutln(out, bytesOut, "var mfr=req2.responseXML.getElementsByTagName(\"mfr\")[0].childNodes[0].nodeValue;document.forms[0].mfr.value=mfr;");
    scoutln(out, bytesOut, "var desc=req2.responseXML.getElementsByTagName(\"desc\")[0].childNodes[0].nodeValue;document.forms[0].desc.value=desc;");
    scoutln(out, bytesOut, "var pricingUpline=req2.responseXML.getElementsByTagName(\"pricingUpline\")[0].childNodes[0].nodeValue;document.forms[0].pricingUpline.value=pricingUpline;");
    scoutln(out, bytesOut, "var catalogUpline=req2.responseXML.getElementsByTagName(\"catalogUpline\")[0].childNodes[0].nodeValue;document.forms[0].catalogUpline.value=catalogUpline;");
    scoutln(out, bytesOut, "var catalogURL=req2.responseXML.getElementsByTagName(\"catalogURL\")[0].childNodes[0].nodeValue;document.forms[0].catalogURL.value=catalogURL;");
    scoutln(out, bytesOut, "var pricingURL=req2.responseXML.getElementsByTagName(\"pricingURL\")[0].childNodes[0].nodeValue;document.forms[0].pricingURL.value=pricingURL;");
    scoutln(out, bytesOut, "var userName=req2.responseXML.getElementsByTagName(\"userName\")[0].childNodes[0].nodeValue;document.forms[0].userName.value=userName;");
    scoutln(out, bytesOut, "var passWord=req2.responseXML.getElementsByTagName(\"passWord\")[0].childNodes[0].nodeValue;document.forms[0].passWord.value=passWord;");
    scoutln(out, bytesOut, "var markup=req2.responseXML.getElementsByTagName(\"markup\")[0].childNodes[0].nodeValue;document.forms[0].markup.value=markup;");
    scoutln(out, bytesOut, "var disc1=req2.responseXML.getElementsByTagName(\"disc1\")[0].childNodes[0].nodeValue;document.forms[0].disc1.value=disc1;");
    scoutln(out, bytesOut, "var disc2=req2.responseXML.getElementsByTagName(\"disc2\")[0].childNodes[0].nodeValue;document.forms[0].disc2.value=disc2;");
    scoutln(out, bytesOut, "var disc3=req2.responseXML.getElementsByTagName(\"disc3\")[0].childNodes[0].nodeValue;document.forms[0].disc3.value=disc3;");
    scoutln(out, bytesOut, "var disc4=req2.responseXML.getElementsByTagName(\"disc4\")[0].childNodes[0].nodeValue;document.forms[0].disc4.value=disc4;");
    scoutln(out, bytesOut, "var showPrices=req2.responseXML.getElementsByTagName(\"showPrices\")[0].childNodes[0].nodeValue;if(showPrices=='Y')document.forms[0].showPrices.checked=true;");
    scoutln(out, bytesOut, "var showAvailability=req2.responseXML.getElementsByTagName(\"showAvailability\")[0].childNodes[0].nodeValue;if(showAvailability=='Y')document.forms[0].showAvailability.checked=true;");
    scoutln(out, bytesOut, "var catalogType=req2.responseXML.getElementsByTagName(\"catalogType\")[0].childNodes[0].nodeValue;if(catalogType=='C')document.forms[0].catalogType[0].checked=true;else if(catalogType=='L')document.forms[0].catalogType[1].checked=true;else document.forms[0].catalogType[2].checked=true;");
    scoutln(out, bytesOut, "var priceBasis=req2.responseXML.getElementsByTagName(\"priceBasis\")[0].childNodes[0].nodeValue;if(priceBasis=='L')document.forms[0].priceBasis[0].checked=true;else document.forms[0].priceBasis[1].checked=true;");
    scoutln(out, bytesOut, "var currency=req2.responseXML.getElementsByTagName(\"currency\")[0].childNodes[0].nodeValue;document.forms[0].currency.value=currency;");
    scoutln(out, bytesOut, "var messageElement=document.getElementById('option');");
    scoutln(out, bytesOut, "messageElement.innerHTML='<p><a href=\"javascript:updateE()\">Update the Link</a>';");
    scoutln(out, bytesOut, "messageElement=document.getElementById('mfrbox');");
    scoutln(out, bytesOut, "messageElement.innerHTML='<p><input type=hidden name=mfr value=\"'+mfr+'\">'+mfr;");
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
    scoutln(out, bytesOut, "this.location.replace(\"/central/servlet/CatalogLinkedDefinition?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den
                         + "&dnm=" + dnm + "&bnm=" + bnm + "\");}");
    
    scoutln(out, bytesOut, "</script>");

    int[] hmenuCount = new int[1];

    adminUtils.outputPageFrame(con, stmt, rs, out, req, "CatalogLinkedDefinition", "", "2011", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount, bytesOut);

    adminUtils.drawTitle(con, stmt, rs, req, out, "Administration - Linked Catalogs Definition", "2011", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
  
    scoutln(out, bytesOut, "<form><table id=\"page\" border=0 width=100% cellpadding=0 cellspacing=0>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td colspan=3><span id=\"msg\"></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr id=\"pageColumn\">");
    scoutln(out, bytesOut, "<td nowrap><p>Manufacturer &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Description &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Pricing Upline &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Pricing URL &nbsp;</td></tr>");
    
    scoutln(out, bytesOut, "<tr id=\"pageColumn\">");
    scoutln(out, bytesOut, "<td nowrap><p>UserName &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>PassWord &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Catalog Upline &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Catalog URL &nbsp;</td></tr>");
    
    scoutln(out, bytesOut, "<tr id=\"pageColumn\">");
    scoutln(out, bytesOut, "<td nowrap><p>Markup % &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Discount1 % &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Discount2 % &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Discount3 % &nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr id=\"pageColumn\">");
    scoutln(out, bytesOut, "<td nowrap><p>Discount4 % &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Show Prices &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Show Availability &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>CatalogType &nbsp;</td></tr>");

    list(con, stmt, rs, out, bytesOut);
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=4><hr></td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    
    scoutln(out, bytesOut, "<tr><td><p><span id=\"mfrbox\"><input type=text name=mfr size=15 maxlength=30></span></td>");
    scoutln(out, bytesOut, "<td><p><input type=text name=desc size=25 maxlength=80></td>");
    scoutln(out, bytesOut, "<td><p><input type=text name=pricingUpline size=10 maxlength=40></td>");
    scoutln(out, bytesOut, "<td><p><input type=text name=pricingURL size=25 maxlength=80></td></tr>");
    
    scoutln(out, bytesOut, "<tr><td><p><input type=text name=userName size=10 maxlength=20></td>");
    scoutln(out, bytesOut, "<td><p><input type=text name=passWord size=10 maxlength=20></td>");
    scoutln(out, bytesOut, "<td><p><input type=text name=catalogUpline size=10 maxlength=40></td>");
    scoutln(out, bytesOut, "<td><p><input type=text name=catalogURL size=25 maxlength=80></td></tr>");
    
    scoutln(out, bytesOut, "<tr><td><p><input type=text name=markup size=10 maxlength=10></td>");
    scoutln(out, bytesOut, "<td><p><input type=text name=disc1 size=10 maxlength=10></td>");
    scoutln(out, bytesOut, "<td><p><input type=text name=disc2 size=10 maxlength=10></td>");
    scoutln(out, bytesOut, "<td><p><input type=text name=disc3 size=10 maxlength=10></td></tr>");
    
    scoutln(out, bytesOut, "<tr><td><p><input type=text name=disc4 size=10 maxlength=10></td>");
    scoutln(out, bytesOut, "<td><p>" + accountsUtils.getCurrencyNamesDDL(con, stmt, rs, "currency", dnm, localDefnsDir, defnsDir) + "</td>");
    scoutln(out, bytesOut, "<td valign=top><p><input type=radio name=priceBasis>List");
    scoutln(out, bytesOut, "<br><input type=radio name=priceBasis>Discounted</td>");

    scoutln(out, bytesOut, "<td valign=top><p><input type=radio name=catalogType>Catalog");
    scoutln(out, bytesOut, "<br><input type=radio name=catalogType>Listing");
    scoutln(out, bytesOut, "<br><input type=radio name=catalogType>Both</td></tr>");
    
    scoutln(out, bytesOut, "<tr><td><p><input type=checkbox name=showPrices>Show Prices</td>");
    scoutln(out, bytesOut, "<td><p><input type=checkbox name=showAvailability>Show Availability</td></tr>");
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=3><span id=\"option\"><p><a href=\"javascript:updateN()\">Add the Link</a></span></td></tr>");
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "</table></form>");     scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void list(Connection con, Statement stmt, ResultSet rs, PrintWriter out, int[] bytesOut) throws Exception
  {
    try
    {
      stmt = con.createStatement();
  
      rs = stmt.executeQuery("SELECT Manufacturer, PricingUpline, PricingURL, UserName, PassWord, MarkupPercentage, ShowPrices, ShowAvailability, "
                           + "Description, CatalogUpline, CatalogURL, DiscountPercentage1, DiscountPercentage2, DiscountPercentage3, "
                           + "DiscountPercentage4, CatalogType, Currency, PriceBasis FROM linkedcat ORDER BY Manufacturer");

      String mfr, pricingUpline, pricingURL, catalogURL, catalogUpline, userName, passWord, markupPercentage, showPrices, showAvailability, desc,
             disc1, disc2, disc3, disc4, catalogType, currency, priceBasis, cssFormat = "line1";

      while(rs.next())                  
      {
        mfr              = rs.getString(1);
        pricingUpline    = rs.getString(2);
        pricingURL       = rs.getString(3);
        userName         = rs.getString(4);
        passWord         = rs.getString(5);
        markupPercentage = rs.getString(6);
        showPrices       = rs.getString(7);
        showAvailability = rs.getString(8);
        desc             = rs.getString(9);
        catalogUpline    = rs.getString(10);
        catalogURL       = rs.getString(11);
        disc1            = rs.getString(12);
        disc2            = rs.getString(13);
        disc3            = rs.getString(14);
        disc4            = rs.getString(15);
        catalogType      = rs.getString(16);
        currency         = rs.getString(17);
        priceBasis       = rs.getString(18);
        
        if(cssFormat.equals("line2")) cssFormat = "line1"; else cssFormat = "line2";
        
        if(showPrices.equals("Y"))
          showPrices = "Show Prices";
        else showPrices = "";
                    
        if(showAvailability.equals("Y"))
          showAvailability = "Show Availability";
        else showAvailability = "";
                    
        if(catalogType.equals("C"))
          catalogType = "Catalog";
        else
        if(catalogType.equals("L"))
          catalogType = "Listing";
        else catalogType = "Both";
                    
        if(priceBasis.equals("L"))
          priceBasis = "List";
        else priceBasis = "Discounted";
                    
        scoutln(out, bytesOut, "<tr id=\""   + cssFormat     + "\">");
        scoutln(out, bytesOut, "<td nowrap>" + mfr           + "</td>");
        scoutln(out, bytesOut, "<td nowrap>" + desc          + "</td>");
        scoutln(out, bytesOut, "<td nowrap>" + pricingUpline + "</td>");
        scoutln(out, bytesOut, "<td nowrap>" + pricingURL    + "</td>");
        scoutln(out, bytesOut, "<td nowrap><a href=\"javascript:edit('" + mfr + "')\">Edit</a>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
        scoutln(out, bytesOut, "<a href=\"javascript:del('" + mfr + "')\">Delete</a></td></tr>");
        
        scoutln(out, bytesOut, "<tr id=\""   + cssFormat     + "\">");
        scoutln(out, bytesOut, "<td nowrap>" + userName      + "</td>");
        scoutln(out, bytesOut, "<td nowrap>" + passWord      + "</td>");
        scoutln(out, bytesOut, "<td nowrap>" + catalogUpline + "</td>");
        scoutln(out, bytesOut, "<td nowrap>" + catalogURL    + "</td></tr>");
        
        scoutln(out, bytesOut, "<tr id=\""   + cssFormat        + "\">");
        scoutln(out, bytesOut, "<td nowrap>" + generalUtils.formatNumeric(markupPercentage, '2') + "</td>");
        scoutln(out, bytesOut, "<td nowrap>" + generalUtils.formatNumeric(disc1, '2') + "</td>");
        scoutln(out, bytesOut, "<td nowrap>" + generalUtils.formatNumeric(disc2, '2') + "</td>");
        scoutln(out, bytesOut, "<td nowrap>" + generalUtils.formatNumeric(disc3, '2') + "</td></tr>");
        
        scoutln(out, bytesOut, "<tr id=\""   + cssFormat        + "\">");
        scoutln(out, bytesOut, "<td nowrap>" + generalUtils.formatNumeric(disc4, '2') + "</td>");
        scoutln(out, bytesOut, "<td nowrap>" + currency         + "</td>");
        scoutln(out, bytesOut, "<td nowrap>" + priceBasis       + "</td>");
        scoutln(out, bytesOut, "<td nowrap>" + catalogType      + "</td></tr>");

        scoutln(out, bytesOut, "<tr id=\""   + cssFormat        + "\">");
        scoutln(out, bytesOut, "<td nowrap>" + showPrices       + "</td>");
        scoutln(out, bytesOut, "<td nowrap>" + showAvailability + "</td></tr>");
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

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
