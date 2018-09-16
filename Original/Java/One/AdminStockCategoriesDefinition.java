// =======================================================================================================================================================================================================
// System: ZaraStar Admin: Stock Categories Definition
// Module: AdminStockCategoriesDefinition.java
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

public class AdminStockCategoriesDefinition extends HttpServlet
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

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="";

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
      p1  = req.getParameter("p1"); // mfr
      
      doIt(out, req, p1, unm, sid, uty, men, den, dnm, bnm, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "AdminStockCategoriesDefinition", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7071, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String p1, String unm, String sid, String uty, String men, String den, String dnm,
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

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 7071, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "7071", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7071, bytesOut[0], 0, "ACC:" + p1);
      
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "7071", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7071, bytesOut[0], 0, "SID:" + p1);
      
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }
  
    create(con, stmt, rs, out, req, p1, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 7071, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
      
      if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void create(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String mfr, String unm, String sid,
                      String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut)
                      throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Admin - Stock Category Definition</title>");
    
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
    scoutln(out, bytesOut, "var code=sanitise(document.forms[0].code.value);");
    scoutln(out, bytesOut, "var desc=sanitise(document.forms[0].desc.value);");
    scoutln(out, bytesOut, "var image=sanitise(document.forms[0].image.value);");
    scoutln(out, bytesOut, "var mfr=sanitise(document.forms[0].mfr.value);");
    scoutln(out, bytesOut, "var page=sanitise(document.forms[0].page.value);");
    scoutln(out, bytesOut, "var download=sanitise(document.forms[0].download.value);");
    scoutln(out, bytesOut, "var text=sanitise(document.forms[0].text.value);");
    scoutln(out, bytesOut, "var text2=sanitise(document.forms[0].text2.value);");
    scoutln(out, bytesOut, "var categoryLink=sanitise(document.forms[0].categoryLink.value);");
    scoutln(out, bytesOut, "var noPrices='N';if(document.forms[0].noPrices.checked)noPrices='Y';");
    scoutln(out, bytesOut, "var noAvailability='N';if(document.forms[0].noAvailability.checked)noAvailability='Y';");
    scoutln(out, bytesOut, "var style=sanitise(document.forms[0].style.value);");
    scoutln(out, bytesOut, "var url=sanitise(document.forms[0].url.value);");
    scoutln(out, bytesOut, "var wikiPage=sanitise(document.forms[0].wikiPage.value);");
    scoutln(out, bytesOut, "var order;if(document.forms[0].order[0].checked)order='C';");
    scoutln(out, bytesOut, "else if(document.forms[0].order[1].checked)order='D';else order='S';");
    scoutln(out, bytesOut, "var url = \"http://" + men + "/central/servlet/AdminStockCategoryDefinitionUpdate?unm=\" + escape('" + unm + "') + \"&sid=\" + escape('" + sid
                         + "') + \"&uty=\" + escape('" + uty + "') + \"&men=\" + escape('" + men + "') + \"&den=\" + escape('" + den
                         + "') + \"&bnm=\" + escape('" + bnm
                         + "') + \"&p1=\" + code + \"&p2=\" + desc + \"&p3=\" + image + \"&p4=\" + mfr + \"&p5=\" + page + \"&p6=\" + download"
                         + " + \"&p7=\" + text + \"&p8=\" + categoryLink + \"&p9=\" + text2 + \"&p10=\" + noPrices + \"&p11=\" + noAvailability"
                         + " + \"&p12=\" + style + \"&p13=\" + url + \"&p14=\" + wikiPage + \"&p15=\" + order + \"&p16=\" + newOrEdit + \"&dnm="
                         + dnm + "\";");
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

    scoutln(out, bytesOut, "function del(code){");
    scoutln(out, bytesOut, "var url = \"http://" + men + "/central/servlet/AdminStockCategoryDefinitionDelete?unm=\" + escape('" + unm + "') + \"&sid=\" + escape('" + sid
                         + "') + \"&uty=\" + escape('" + uty + "') + \"&men=\" + escape('" + men + "') + \"&den=\" + escape('" + den
                         + "') + \"&bnm=\" + escape('" + bnm + "') + \"&p1=\" + escape(code) + \"&dnm=\" + escape('" + dnm + "');");
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

    scoutln(out, bytesOut, "function edit(code){");
    scoutln(out, bytesOut, "var url = \"http://" + men + "/central/servlet/AdminStockCategoryDefinitionEdit?unm=\" + escape('" + unm + "') + \"&sid=\" + escape('" + sid
                         + "') + \"&uty=\" + escape('" + uty + "') + \"&men=\" + escape('" + men + "') + \"&den=\" + escape('" + den
                         + "') + \"&bnm=\" + escape('" + bnm + "') + \"&p1=\" + sanitise(code) + \"&dnm=\" + escape('" + dnm + "');");
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
    scoutln(out, bytesOut, "var code=req2.responseXML.getElementsByTagName(\"code\")[0].childNodes[0].nodeValue;document.forms[0].code.value=code;");
    scoutln(out, bytesOut, "var desc=req2.responseXML.getElementsByTagName(\"desc\")[0].childNodes[0].nodeValue;document.forms[0].desc.value=desc;");
    scoutln(out, bytesOut, "var image=req2.responseXML.getElementsByTagName(\"image\")[0].childNodes[0].nodeValue;");
    scoutln(out, bytesOut, "var mfr=req2.responseXML.getElementsByTagName(\"mfr\")[0].childNodes[0].nodeValue;document.forms[0].mfr.value=mfr;");
    scoutln(out, bytesOut, "var page=req2.responseXML.getElementsByTagName(\"page\")[0].childNodes[0].nodeValue;document.forms[0].page.value=page;");
    scoutln(out, bytesOut, "var download=req2.responseXML.getElementsByTagName(\"download\")[0].childNodes[0].nodeValue;");
    scoutln(out, bytesOut, "var text=req2.responseXML.getElementsByTagName(\"text\")[0].childNodes[0].nodeValue;");
    scoutln(out, bytesOut, "var categoryLink=req2.responseXML.getElementsByTagName(\"categoryLink\")[0].childNodes[0].nodeValue;");
    scoutln(out, bytesOut, "var text2=req2.responseXML.getElementsByTagName(\"text2\")[0].childNodes[0].nodeValue;");
    scoutln(out, bytesOut, "var noPrices=req2.responseXML.getElementsByTagName(\"noPrices\")[0].childNodes[0].nodeValue;");
    scoutln(out, bytesOut, "if(noPrices=='Y')document.forms[0].noPrices.checked=true;else document.forms[0].noPrices.checked=false;");
    scoutln(out, bytesOut, "var noAvailability=req2.responseXML.getElementsByTagName(\"noAvailability\")[0].childNodes[0].nodeValue;");
    scoutln(out, bytesOut, "if(noAvailability=='Y')document.forms[0].noAvailability.checked=true;else document.forms[0].noAvailability.checked=false;");
    scoutln(out, bytesOut, "var style=req2.responseXML.getElementsByTagName(\"style\")[0].childNodes[0].nodeValue;document.forms[0].style.value=style;");
    scoutln(out, bytesOut, "var url=req2.responseXML.getElementsByTagName(\"url\")[0].childNodes[0].nodeValue;");
    scoutln(out, bytesOut, "var wikiPage=req2.responseXML.getElementsByTagName(\"wikiPage\")[0].childNodes[0].nodeValue;");
    scoutln(out, bytesOut, "var order=req2.responseXML.getElementsByTagName(\"order\")[0].childNodes[0].nodeValue;");
    scoutln(out, bytesOut, "if(order=='C')document.forms[0].order[0].checked=true;else if(order=='D')document.forms[0].order[1].checked=true;else document.forms[0].order[2].checked=true;");
    scoutln(out, bytesOut, "var messageElement = document.getElementById('option');");
    scoutln(out, bytesOut, "messageElement.innerHTML='<p><a href=\"javascript:updateE()\">Update the Stock Category</a>';");
    scoutln(out, bytesOut, "messageElement=document.getElementById('codebox');");
    scoutln(out, bytesOut, "messageElement.innerHTML='<p><input type=hidden name=code value=\"'+code+'\">'+code;");

    scoutln(out, bytesOut, "if(image=='.')image='';document.forms[0].image.value=image");
    scoutln(out, bytesOut, "if(download=='.')download='';document.forms[0].download.value=download");
    scoutln(out, bytesOut, "if(text=='.')text='';document.forms[0].text.value=text");
    scoutln(out, bytesOut, "if(categoryLink=='.')categoryLink='';document.forms[0].categoryLink.value=categoryLink");
    scoutln(out, bytesOut, "if(text2=='.')text2='';document.forms[0].text2.value=text2");
    scoutln(out, bytesOut, "if(url=='.')url='';document.forms[0].url.value=url");
    scoutln(out, bytesOut, "if(wikiPage=='.')wikiPage='';document.forms[0].wikiPage.value=wikiPage");

    scoutln(out, bytesOut, "}else{ var messageElement=document.getElementById('msg');");
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
    scoutln(out, bytesOut, "this.location.replace(\"/central/servlet/AdminStockCategoriesDefinition?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den
                         + "&dnm=" + dnm + "&p1=" + generalUtils.sanitise(mfr) + "&bnm=" + bnm + "\");}");
    
    scoutln(out, bytesOut, "</script>");

    int[] hmenuCount = new int[1];

    adminUtils.outputPageFrame(con, stmt, rs, out, req, "AdminStockCategoriesDefinition", "", "7071", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount, bytesOut);

    adminUtils.drawTitle(con, stmt, rs, req, out, "Administration - Stock Category Definition", "7071", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
  
    scoutln(out, bytesOut, "<form><table id=\"page\" border=0 width=100% cellpadding=0 cellspacing=0>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td colspan=3><span id=\"msg\"></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr id=\"pageColumn\">");
    scoutln(out, bytesOut, "<td nowrap><p>Category Code &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Description &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Page &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Wiki Page &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Style &nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr id=\"pageColumn\">");
    scoutln(out, bytesOut, "<td nowrap><p>Manufacturer &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Image &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Download &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>No Prices &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Text 2 &nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr id=\"pageColumn\">");
    scoutln(out, bytesOut, "<td nowrap><p>Text &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>External URL &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Category Link &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>No Availability &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Ordering &nbsp;</td></tr>");
        
    list(con, stmt, rs, out, mfr, bytesOut);
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=4><hr></td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    
    scoutln(out, bytesOut, "<tr><td><span id=\"codebox\"><p><input type=text name=code size=8 maxlength=10></span></td>");
    scoutln(out, bytesOut, "<td><p><input type=text name=desc size=25 maxlength=80></td>");
    scoutln(out, bytesOut, "<td><p><input type=text name=page size=4 maxlength=10></td>");
    scoutln(out, bytesOut, "<td><p><input type=text name=wikiPage size=4 maxlength=10></td>");
    scoutln(out, bytesOut, "<td><p>");
    getStyleDDL(out, dnm, localDefnsDir, defnsDir, bytesOut);
    scoutln(out, bytesOut, "</td></tr>");
    
    scoutln(out, bytesOut, "<tr><td><p>");
    getMfrsDDL(out, dnm, localDefnsDir, defnsDir, bytesOut);
    scoutln(out, bytesOut, "</td>");
    scoutln(out, bytesOut, "<td><p><input type=text name=image size=25 maxlength=100></td>");
    scoutln(out, bytesOut, "<td><p><input type=text name=download size=4 maxlength=10></td>");
    scoutln(out, bytesOut, "<td><p><input type=checkbox name=noPrices>No Prices</td>");
    scoutln(out, bytesOut, "<td><p><input type=text name=text2 size=25 maxlength=100></td></tr>");
    
    scoutln(out, bytesOut, "<tr><td><p><input type=text name=text size=25 maxlength=100></td>");
    scoutln(out, bytesOut, "<td><p><input type=text name=url size=25 maxlength=100></td>");
    scoutln(out, bytesOut, "<td><p><input type=text name=categoryLink size=4 maxlength=10></td>");
    scoutln(out, bytesOut, "<td><p><input type=checkbox name=noAvailability>No Availability</td>");
    scoutln(out, bytesOut, "<td><p><input type=radio name=order>by Mfr Code");
    scoutln(out, bytesOut, "<br><input type=radio name=order>by Description");
    scoutln(out, bytesOut, "<br><input type=radio name=order>by Size</td></tr>");
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=3><span id=\"option\"><p><a href=\"javascript:updateN()\">Add the Stock Category</a></span></td></tr>");
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir));
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void list(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String mfr, int[] bytesOut) throws Exception
  {
    try
    {
      stmt = con.createStatement();
  
      rs = stmt.executeQuery("SELECT CategoryCode, Description, Image, Page, Download, Text, WikiPage, URL, NoPrices, NoAvailability, "
                           + "CategoryLink, OrderByDescription, Style, Text2 FROM stockcat WHERE Manufacturer = '" + generalUtils.sanitiseForSQL(mfr)
                           + "' ORDER BY CategoryCode");

      String code, desc, image, page, download, text, wikiPage, url, noPrices, noAvailability, categoryLink, order, style, text2, cssFormat;
      boolean line1=true;

      while(rs.next())                  
      {
        code           = rs.getString(1);
        desc           = rs.getString(2);
        image          = rs.getString(3);
        page           = rs.getString(4);
        download       = rs.getString(5);
        text           = rs.getString(6);
        wikiPage       = rs.getString(7);
        url            = rs.getString(8);
        noPrices       = rs.getString(9);
        noAvailability = rs.getString(10);
        categoryLink   = rs.getString(11);
        order          = rs.getString(12);
        style          = rs.getString(13);
        text2          = rs.getString(14);
        
        if(line1) { cssFormat = "line1"; line1 = false; } else { cssFormat = "line2"; line1 = true; }
        
        if(download.equals("0"))
          download = "";
                    
        if(wikiPage.equals("0"))
          wikiPage = "";
                    
        if(noPrices.equals("Y"))
          noPrices = "No Prices";
        else noPrices = "";
                    
        if(noAvailability.equals("Y"))
          noAvailability = "No Availability";
        else noAvailability = "";
                    
        if(categoryLink.equals("0"))
          categoryLink = "";
                    
        if(order.equals("C"))
          order = "By Mfr Code";
        else
        if(order.equals("D"))
          order = "Description";
        else order = "Size";
                    
        scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\">");
        scoutln(out, bytesOut, "<td nowrap>" + code     + "</td>");
        scoutln(out, bytesOut, "<td nowrap>" + desc     + "</td>");
        scoutln(out, bytesOut, "<td nowrap>" + page     + "</td>");
        scoutln(out, bytesOut, "<td nowrap>" + wikiPage + "</td>");
        scoutln(out, bytesOut, "<td nowrap>" + style    + "</td>");
        scoutln(out, bytesOut, "<td nowrap><a href=\"javascript:edit('" + code + "')\">Edit</a>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
        scoutln(out, bytesOut, "<a href=\"javascript:del('" + code + "')\">Delete</a></td></tr>");
        
        scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\">");
        scoutln(out, bytesOut, "<td nowrap>" + mfr      + "</td>");
        scoutln(out, bytesOut, "<td nowrap>" + image    + "</td>");
        scoutln(out, bytesOut, "<td nowrap>" + download + "</td>");
        scoutln(out, bytesOut, "<td nowrap>" + noPrices + "</td>");
        scoutln(out, bytesOut, "<td nowrap>" + text2    + "</td></tr>");
        
        scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\">");
        scoutln(out, bytesOut, "<td nowrap>" + text           + "</td>");
        scoutln(out, bytesOut, "<td nowrap>" + url            + "</td>");
        scoutln(out, bytesOut, "<td nowrap>" + categoryLink   + "</td>");
        scoutln(out, bytesOut, "<td nowrap>" + noAvailability + "</td>");
        scoutln(out, bytesOut, "<td nowrap>" + order          + "</td></tr>");
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
  private void getMfrsDDL(PrintWriter out, String dnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    String userName = directoryUtils.getMySQLUserName();
    String passWord = directoryUtils.getMySQLPassWord();
    
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + userName + "&password=" + passWord);
    Statement stmt = con.createStatement();

    ResultSet rs = stmt.executeQuery("SELECT DISTINCT Manufacturer FROM stock ORDER BY Manufacturer");

    scoutln(out, bytesOut, "<select name=\"mfr\">");
    
    String mfr;
    
    while(rs.next())
    {
      mfr = rs.getString(1);
      if(mfr.length() > 0)
        scoutln(out, bytesOut, "<option value=\"" + mfr + "\">" + mfr);
    }

    scoutln(out, bytesOut, "</select>");
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    if(con  != null) con.close();
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void getStyleDDL(PrintWriter out, String dnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<select name=\"style\">");
    
    scoutln(out, bytesOut, "<option value=\"Default\">Default");

    scoutln(out, bytesOut, "</select>");
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
