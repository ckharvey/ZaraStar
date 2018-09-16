// =======================================================================================================================================================================================================
// System: ZaraStar Catalogs: SC category edit
// Module: CatalogsSteelclawsCategoryEdit.java
// Author: C.K.Harvey
// Copyright (c) 2003-08 Christopher Harvey. All Rights Reserved.
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

public class CatalogsSteelclawsCategoryEdit extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  SCCatalogsUtils sCCatalogsUtils = new SCCatalogsUtils();
  
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="";

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
      p2  = req.getParameter("p2"); // category
      
      doIt(out, req, p1, p2, unm, sid, uty, men, den, dnm, bnm, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "CatalogsSteelclawsCategoryEdit", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6402, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String p1, String p2, String unm, String sid, String uty, String men, String den,
                    String dnm, String bnm, int[] bytesOut) throws Exception
  {
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');

    long startTime = new java.util.Date().getTime();

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 6402, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "CatalogsSteelclawsCategoryEdit", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6402, bytesOut[0], 0, "ACC:");
      
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "CatalogsSteelclawsCategoryEdit", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6402, bytesOut[0], 0, "SID:");
      
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }
  
    create(con, stmt, rs, out, req, p1, p2, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 6402, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
      
      if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void create(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String mfr, String category, String unm, String sid, String uty, String men,
                      String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Catalogs - Category Edit</title>");
    
    scoutln(out, bytesOut, "<script language=\"JavaScript\" type=\"text/javascript\" src=\"" + directoryUtils.getEditorDirectory() + "editor.js\"></script>"); 

    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\""
                         + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">"); 

    // update record to DB
    scoutln(out, bytesOut, "var req4;");    
    scoutln(out, bytesOut, "function initRequest4(url)");
    scoutln(out, bytesOut, "{if(window.XMLHttpRequest){req4=new XMLHttpRequest();}else");
    scoutln(out, bytesOut, "if(window.ActiveXObject){req4=new ActiveXObject(\"Microsoft.XMLHTTP\");}}");

    scoutln(out, bytesOut, "function update(){");
    scoutln(out, bytesOut, "var url=\"http://" + men + "/central/servlet/CatalogsSteelclawsCategoryEditUpdate\";");

    scoutln(out, bytesOut, "initRequest4(url);");
    scoutln(out, bytesOut, "req4.onreadystatechange=processRequest4;");
    scoutln(out, bytesOut, "req4.open(\"POST\",url,true);");
    scoutln(out, bytesOut, "var query=createQuery(document.forms['form6402']);");
    scoutln(out, bytesOut, "req4.setRequestHeader(\"Content-type\",\"application/x-www-form-urlencoded\")");
    scoutln(out, bytesOut, "req4.setRequestHeader(\"Content-length\",query.length);");
    scoutln(out, bytesOut, "req4.setRequestHeader(\"Connection\",\"close\")");
    scoutln(out, bytesOut, "req4.send(query);}");
    
    scoutln(out, bytesOut, "function createQuery(form){");
    scoutln(out, bytesOut, "var elements=form.elements;");
    scoutln(out, bytesOut, "var pairs=new Array();");
    scoutln(out, bytesOut, "for(var i=0;i<elements.length;++i){");
    scoutln(out, bytesOut, "if((name=elements[i].name)&&(value=elements[i].value))");

    scoutln(out, bytesOut, "{if(name=='newProduct')if(form.newProduct.checked)value='on';else value='off';");    
    
    scoutln(out, bytesOut, "pairs.push(name+\"=\"+encodeURIComponent(value));}}");
    scoutln(out, bytesOut, "return pairs.join(\"&\");}");
    
    scoutln(out, bytesOut, "function processRequest4(){");
    scoutln(out, bytesOut, "if(req4.readyState==4){");
    scoutln(out, bytesOut, "if(req4.status==200){");
    scoutln(out, bytesOut, "var res=req4.responseXML.getElementsByTagName(\"res\")[0].childNodes[0].nodeValue;");
    scoutln(out, bytesOut, "if(res.length>0){");
    scoutln(out, bytesOut, "if(res=='.')");
    scoutln(out, bytesOut, "refetchPage();else{var messageElement=document.getElementById('msg');");
    scoutln(out, bytesOut, "messageElement.innerHTML='<span id=\"textErrorLarge\">'+res+'</span>';");
    scoutln(out, bytesOut, "}}}}}");
      
    // set table
    scoutln(out, bytesOut, "var req3;");    
    scoutln(out, bytesOut, "function initRequest3(url)");
    scoutln(out, bytesOut, "{if(window.XMLHttpRequest){req3=new XMLHttpRequest();}else");
    scoutln(out, bytesOut, "if(window.ActiveXObject){req3=new ActiveXObject(\"Microsoft.XMLHTTP\");}}");

    scoutln(out, bytesOut, "function initialSet(){");
    scoutln(out, bytesOut, "var mfr=document.forms[0].mfr.value;");
    scoutln(out, bytesOut, "var category=document.forms[0].category.value;");
    scoutln(out, bytesOut, "var rows=document.forms[0].rows.value;");
    scoutln(out, bytesOut, "var cols=document.forms[0].cols.value;");
    scoutln(out, bytesOut, "var url=\"http://" + men + "/central/servlet/CatalogsSteelclawsCategoryEditFetch?unm=\" + escape('" + unm + "') + \"&sid=\" + escape('" + sid
                         + "') + \"&uty=\" + escape('" + uty + "') + \"&men=\" + escape('" + men + "') + \"&den=\" + escape('" + den
                         + "') + \"&bnm=\" + escape('" + bnm + "') + \"&p1=" + generalUtils.sanitise(mfr) + "&p2=" + generalUtils.sanitise(category)
                         + "&p3=0&p4=0&dnm=\" + escape('" + dnm + "');");
    scoutln(out, bytesOut, "initRequest3(url);");
    scoutln(out, bytesOut, "req3.onreadystatechange=processRequest3;");
    scoutln(out, bytesOut, "req3.open(\"GET\", url, true);");
    scoutln(out, bytesOut, "req3.send(null);}");

    scoutln(out, bytesOut, "function set(){");
    scoutln(out, bytesOut, "var mfr=document.forms[0].mfr.value;");
    scoutln(out, bytesOut, "var category=document.forms[0].category.value;");
    scoutln(out, bytesOut, "var rows=document.forms[0].rows.value;");
    scoutln(out, bytesOut, "var cols=document.forms[0].cols.value;");
    scoutln(out, bytesOut, "var url=\"http://" + men + "/central/servlet/CatalogsSteelclawsCategoryEditFetch?unm=\" + escape('" + unm + "') + \"&sid=\" + escape('" + sid
                         + "') + \"&uty=\" + escape('" + uty + "') + \"&men=\" + escape('" + men + "') + \"&den=\" + escape('" + den
                         + "') + \"&bnm=\" + escape('" + bnm + "') + \"&p1=\" + escape(mfr) + \"&p2=\" + escape(category) + \"&p3=\" + escape(rows)"
                         + " + \"&p4=\" + escape(cols) + \"&dnm=\" + escape('" + dnm + "');");
    scoutln(out, bytesOut, "initRequest3(url);");
    scoutln(out, bytesOut, "req3.onreadystatechange=processRequest3;");
    scoutln(out, bytesOut, "req3.open(\"GET\", url, true);");
    scoutln(out, bytesOut, "req3.send(null);}");

    scoutln(out, bytesOut, "function processRequest3(){");
    scoutln(out, bytesOut, "if(req3.readyState==4){");
    scoutln(out, bytesOut, "if(req3.status==200){");
    scoutln(out, bytesOut, "var res=req3.responseXML.getElementsByTagName(\"res\")[0].childNodes[0].nodeValue;");
    scoutln(out, bytesOut, "if(res.length>0){");
    scoutln(out, bytesOut, "if(res!='.')");
    scoutln(out, bytesOut, "{var messageElement = document.getElementById('msg');");
    scoutln(out, bytesOut, "messageElement.innerHTML='<span id=\"textErrorLarge\">'+res+'</span>';}else{");
    scoutln(out, bytesOut, "var stuff=req3.responseXML.getElementsByTagName(\"stuff\")[0].childNodes[0].nodeValue;");
    scoutln(out, bytesOut, "var messageElement=document.getElementById('bodyStuff');");
    scoutln(out, bytesOut, "messageElement.innerHTML=stuff;");   
    scoutln(out, bytesOut, "}}}}}");

    // adjust table
    scoutln(out, bytesOut, "var req2;");    
    scoutln(out, bytesOut, "function initRequest2(url)");
    scoutln(out, bytesOut, "{if(window.XMLHttpRequest){req2=new XMLHttpRequest();}else");
    scoutln(out, bytesOut, "if(window.ActiveXObject){req2=new ActiveXObject(\"Microsoft.XMLHTTP\");}}");

    scoutln(out, bytesOut, "function adjust(){");
    scoutln(out, bytesOut, "var mfr=document.forms[0].mfr.value;");
    scoutln(out, bytesOut, "var category=document.forms[0].category.value;");
    scoutln(out, bytesOut, "var rows=document.forms[0].rows.value;");
    scoutln(out, bytesOut, "var cols=document.forms[0].cols.value;");
    scoutln(out, bytesOut, "var url=\"http://" + men + "/central/servlet/CatalogsSteelclawsCategoryEditAdjust?unm=\" + escape('" + unm + "') + \"&sid=\" + escape('" + sid
                         + "') + \"&uty=\" + escape('" + uty + "') + \"&men=\" + escape('" + men + "') + \"&den=\" + escape('" + den
                         + "') + \"&bnm=\" + escape('" + bnm + "') + \"&p1=\" + escape(mfr) + \"&p2=\" + escape(category) + \"&p3=\" + escape(rows)"
                         + " + \"&p4=\" + escape(cols) + \"&dnm=\" + escape('" + dnm + "');");
    scoutln(out, bytesOut, "initRequest2(url);");
    scoutln(out, bytesOut, "req2.onreadystatechange=processRequest2;");
    scoutln(out, bytesOut, "req2.open(\"GET\", url, true);");
    scoutln(out, bytesOut, "req2.send(null);}");

    scoutln(out, bytesOut, "function processRequest2(){");
    scoutln(out, bytesOut, "if(req2.readyState==4){");
    scoutln(out, bytesOut, "if(req2.status==200){");
    scoutln(out, bytesOut, "var res=req2.responseXML.getElementsByTagName(\"res\")[0].childNodes[0].nodeValue;");
    scoutln(out, bytesOut, "if(res.length>0){");
    scoutln(out, bytesOut, "if(res!='.')");
    scoutln(out, bytesOut, "{ var messageElement = document.getElementById('msg');");
    scoutln(out, bytesOut, "messageElement.innerHTML = '<span id=\"textErrorLarge\">'+res+'</span>';}else{");
    scoutln(out, bytesOut, "var stuff=req2.responseXML.getElementsByTagName(\"stuff\")[0].childNodes[0].nodeValue;");
    scoutln(out, bytesOut, "var messageElement=document.getElementById('bodyStuff');");
    scoutln(out, bytesOut, "messageElement.innerHTML=stuff;");   
    scoutln(out, bytesOut, "}}}}}");

    // fetch sections
    scoutln(out, bytesOut, "var req5;");    
    scoutln(out, bytesOut, "function initRequest5(url)");
    scoutln(out, bytesOut, "{if(window.XMLHttpRequest){req5=new XMLHttpRequest();}else");
    scoutln(out, bytesOut, "if(window.ActiveXObject){req5=new ActiveXObject(\"Microsoft.XMLHTTP\");}}");

    scoutln(out, bytesOut, "function sfetch(thisObj){");
    scoutln(out, bytesOut, "var mfr=document.forms[0].mfr.value;");
    scoutln(out, bytesOut, "var chapter=thisObj.options[thisObj.selectedIndex].value;");
    scoutln(out, bytesOut, "var url=\"http://" + men + "/central/servlet/CatalogsSectionsChapter?unm=\" + escape('" + unm + "') + \"&sid=\" + escape('" + sid
                         + "') + \"&uty=\" + escape('" + uty + "') + \"&p1=\" + escape(mfr) + \"&p2=\" + escape(chapter) + \"&dnm=\" + escape('"
                         + dnm + "');");
    scoutln(out, bytesOut, "initRequest5(url);");
    scoutln(out, bytesOut, "req5.onreadystatechange=processRequest5;");
    scoutln(out, bytesOut, "req5.open(\"GET\", url, true);");
    scoutln(out, bytesOut, "req5.send(null);}");

    scoutln(out, bytesOut, "function processRequest5(){");
    scoutln(out, bytesOut, "if(req5.readyState==4){");
    scoutln(out, bytesOut, "if(req5.status==200){");
    scoutln(out, bytesOut, "var res=req5.responseXML.getElementsByTagName(\"res\")[0].childNodes[0].nodeValue;");
    scoutln(out, bytesOut, "if(res.length>0){");
    scoutln(out, bytesOut, "if(res!='.'){");
    scoutln(out, bytesOut, "var messageElement=document.getElementById('msg');");
    scoutln(out, bytesOut, "messageElement.innerHTML='<span id=\"textErrorLarge\">'+res+'</span>';}else{");
    scoutln(out, bytesOut, "var sections=req5.responseXML.getElementsByTagName(\"sections\")[0].childNodes[0].nodeValue;");
    scoutln(out, bytesOut, "var messageElement=document.getElementById('sections');");
    scoutln(out, bytesOut, "messageElement.innerHTML=sections;");   
    scoutln(out, bytesOut, "pfetch(document.forms[0].section);");
    scoutln(out, bytesOut, "}}}}}");

    // fetch pages
    scoutln(out, bytesOut, "var req6;");    
    scoutln(out, bytesOut, "function initRequest6(url)");
    scoutln(out, bytesOut, "{if(window.XMLHttpRequest){req6=new XMLHttpRequest();}else");
    scoutln(out, bytesOut, "if(window.ActiveXObject){req6=new ActiveXObject(\"Microsoft.XMLHTTP\");}}");

    scoutln(out, bytesOut, "function pfetch(thisObj){");
    scoutln(out, bytesOut, "var mfr=document.forms[0].mfr.value;");
    scoutln(out, bytesOut, "var i=document.forms[0].chapter.selectedIndex;");
    scoutln(out, bytesOut, "var chapter=document.forms[0].chapter[i].value;");
    scoutln(out, bytesOut, "var section=thisObj.options[thisObj.selectedIndex].value;");
    scoutln(out, bytesOut, "var url=\"http://" + men + "/central/servlet/CatalogsPagesSection?unm=\" + escape('" + unm + "') + \"&sid=\" + escape('" + sid
                         + "') + \"&uty=\" + escape('" + uty + "') + \"&p1=\" + escape(mfr) + \"&p2=\" + escape(chapter) + \"&p3=\" + escape(section)"
                         + " + \"&dnm=\" + escape('" + dnm + "');");
    scoutln(out, bytesOut, "initRequest6(url);");
    scoutln(out, bytesOut, "req6.onreadystatechange=processRequest6;");
    scoutln(out, bytesOut, "req6.open(\"GET\", url, true);");
    scoutln(out, bytesOut, "req6.send(null);}");

    scoutln(out, bytesOut, "function processRequest6(){");
    scoutln(out, bytesOut, "if(req6.readyState==4){");
    scoutln(out, bytesOut, "if(req6.status==200){");
    scoutln(out, bytesOut, "var res=req6.responseXML.getElementsByTagName(\"res\")[0].childNodes[0].nodeValue;");
    scoutln(out, bytesOut, "if(res.length>0){");
    scoutln(out, bytesOut, "if(res!='.'){");
    scoutln(out, bytesOut, "var messageElement=document.getElementById('msg');");
    scoutln(out, bytesOut, "messageElement.innerHTML='<span id=\"textErrorLarge\">'+res+'</span>';}else{");
    scoutln(out, bytesOut, "var pages=req6.responseXML.getElementsByTagName(\"pages\")[0].childNodes[0].nodeValue;");
    scoutln(out, bytesOut, "var messageElement=document.getElementById('pages');");
    scoutln(out, bytesOut, "messageElement.innerHTML=pages;");   
    scoutln(out, bytesOut, "}}}}}");

    scoutln(out, bytesOut, "</script>");

    int[] hmenuCount = new int[1];

    sCCatalogsUtils.outputPageFrame(con, stmt, rs, out, req, "6402", "CatalogsSteelclawsCategoryEdit", unm, sid, uty, men, den, dnm, bnm, "", " initialSet(); ", localDefnsDir, defnsDir, hmenuCount, bytesOut);

    sCCatalogsUtils.drawTitle(con, stmt, rs, req, out, mfr, mfr + ": " + category, "6402", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<table id=\"page\" border=0 cellpadding=2 cellspacing=0>");

    scoutln(out, bytesOut, "<tr><td colspan=3><span id=\"msg\"></span></td></tr>");

    String[] chapter      = new String[1];
    String[] section      = new String[1];
    String[] pageName     = new String[1];
    String[] title        = new String[1];
    String[] imageSmall   = new String[1];
    String[] imageSmall2  = new String[1];
    String[] imageLarge   = new String[1];
    String[] download     = new String[1];
    String[] externalURL  = new String[1];
    String[] categoryLink = new String[1];
    String[] text         = new String[1];
    String[] text2        = new String[1];
    String[] newProduct   = new String[1];
    String[] originalPage = new String[1];

    getHeaderData(con, stmt, rs, mfr, category, chapter, section, pageName, title, imageSmall, imageSmall2, imageLarge, download, externalURL,
                  categoryLink, text, text2, newProduct, originalPage);

    scoutln(out, bytesOut, "<input type=hidden name=mfr value='" + mfr + "'>");
    
    scoutln(out, bytesOut, "<tr><td><p>Category</td><td><p><span id=\"categorybox\"><input type=text name=category size=10 maxlength=10 value='"
                         + category + "'></span></td>");
    
    scoutln(out, bytesOut, "<td><p>Chapter</td><td><p>");
    getChapters(con, stmt, rs, out, mfr, chapter[0], bytesOut);
    scoutln(out, bytesOut, "&nbsp;</td></tr>");
    
    scoutln(out, bytesOut, "<tr><td><p>Section</td><td><p><span id='sections'>");
    getSections(con, stmt, rs, out, mfr, chapter[0], section[0], bytesOut);
    scoutln(out, bytesOut, "</span>&nbsp;</td>");

    scoutln(out, bytesOut, "<td><p>Page</td><td><p><span id='pages'>");
    getPages(con, stmt, rs, out, mfr, chapter[0], section[0], pageName[0], bytesOut);
    scoutln(out, bytesOut, "</span>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td><p>Title</td><td><p><input type=text name=title size=40 maxlength=80 value=\"" + title[0] + "\"></td>");
    scoutln(out, bytesOut, "<td><p>Original Page</td><td><p><input type=text name=originalPage size=4 value=\"" + originalPage[0] + "\"></td></tr>");
    
    scoutln(out, bytesOut, "<tr><td nowrap><p>Image Small &nbsp; </td><td><p><input type=text name=imageSmall size=30 maxlength=30 value='"
                         + imageSmall[0] + "'></td>");
    scoutln(out, bytesOut, "<td nowrap><p>Image Small 2 &nbsp; </td><td><p><input type=text name=imageSmall2 size=30 maxlength=30 value='"
                         + imageSmall2[0] + "'></td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap><p>Image Large</td><td><p><input type=text name=imageLarge size=30 maxlength=30 value='" + imageLarge[0]
                         + "'></td>");
    scout(out, bytesOut, "<td nowrap><p>New Product</td><td><p><input type=checkbox name=newProduct");

    if(newProduct[0].equals("Y"))
      scout(out, bytesOut, " checked");
    scoutln(out, bytesOut, "></td></tr>");

    scoutln(out, bytesOut, "<tr><td colspan=4><textarea id='text1' name='text1' cols='100' rows='6'>" + text[0] + "</textarea>");
    scoutln(out, bytesOut, "<script language='JavaScript'>drawEditor('text1', 'text1bit', '100%', '200', '#F0F0F0', '#F0F0F0', '#C0C0C0','" + unm
                           + "','" + sid + "','" + uty + "','" + men + "','" + den + "','" + dnm + "','" + bnm + "');</script></td></tr>");

    scoutln(out, bytesOut, "<tr><td colspan=4><textarea id='text2' name='text2' cols='100' rows='2'>" + text2[0] + "</textarea></td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr></table>");

    scoutln(out, bytesOut, "<span id=\"bodyStuff\"></span>");

    scoutln(out, bytesOut, "<table id=\"page\" border=0 cellpadding=0 cellspacing=0>");
    scoutln(out, bytesOut, "<tr><td><p>Rows &nbsp;&nbsp;</td><td><p><input type=text name=rows size=4 maxlength=4></td>");
    scoutln(out, bytesOut, "<td><p>&nbsp;&nbsp;Columns&nbsp;&nbsp;</td><td><p><input type=text name=cols size=4 maxlength=4>&nbsp;&nbsp;&nbsp;"
                        + "<a href=\"javascript:adjust()\">Adjust</a>&nbsp;&nbsp;&nbsp;<a href=\"javascript:set()\">Set</a></td></tr></table>");
    
    scoutln(out, bytesOut, "</form></div></body></html>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void getHeaderData(Connection con, Statement stmt, ResultSet rs, String mfr, String category, String[] chapter, String section[],
                             String[] pageName, String[] title, String[] imageSmall, String[] imageSmall2, String[] imageLarge, String[] download,
                             String[] externalURL, String[] categoryLink, String[] text, String[] text2, String[] newProduct, String[] originalPage)
                             throws Exception
  {
    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT * FROM catalog WHERE Manufacturer = '" + generalUtils.sanitiseForSQL(mfr) + "' AND Category = '" + category + "'");

      if(rs.next())
      {
        chapter[0]      = rs.getString(3);
        pageName[0]     = rs.getString(4);
        title[0]        = rs.getString(5);
        imageSmall[0]   = rs.getString(6);
        imageSmall2[0]  = rs.getString(7);
        imageLarge[0]   = rs.getString(8);
        download[0]     = rs.getString(9);
        externalURL[0]  = rs.getString(10);
        newProduct[0]   = rs.getString(21);
        categoryLink[0] = rs.getString(22);
        text[0]         = rs.getString(23);
        text2[0]        = rs.getString(24);
        section[0]      = rs.getString(25);
        originalPage[0] = rs.getString(26);
        
        if(chapter[0] == null) chapter[0] = "";
        if(pageName[0] == null) pageName[0] = "";
        if(title[0] == null) title[0] = "";
        if(imageSmall[0] == null) imageSmall[0] = "";
        if(imageSmall2[0] == null) imageSmall2[0] = "";
        if(imageLarge[0] == null) imageLarge[0] = "";
        if(download[0] == null) download[0] = "0";
        if(externalURL[0] == null) externalURL[0] = "";
        if(newProduct[0] == null) newProduct[0] = "N";
        if(categoryLink[0] == null) categoryLink[0] = "0";
        if(text[0] == null) text[0] = "";
        if(text2[0] == null) text2[0] = "";
        if(section[0] == null) section[0] = "";
        if(originalPage[0] == null) originalPage[0] = "0";
      }
      else
      {
        chapter[0]      = "";
        pageName[0]     = "";
        title[0]        = "";
        imageSmall[0]   = "";
        imageSmall2[0]  = "";
        imageLarge[0]   = "";
        download[0]     = "0";
        externalURL[0]  = "";
        categoryLink[0] = "0";
        text[0]         = "";
        text2[0]        = "";
        newProduct[0]   = "";
        section[0]      = "";
        originalPage[0] = "";
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
  private void getChapters(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String mfr, String current, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<select name='chapter' onchange='javascript:sfetch(this)'>");
    
    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT DISTINCT Chapter FROM catalogc WHERE Manufacturer = '" + generalUtils.sanitiseForSQL(mfr) + "' ORDER BY Position");

      String chapter;
      
      while(rs.next())
      {
        chapter = rs.getString(1);
        scout(out, bytesOut, "<option value='" +  generalUtils.sanitise(chapter) + "'");
        if(current.equals(chapter))
          scout(out, bytesOut, " selected");
        scoutln(out, bytesOut, ">" + chapter);
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

    scoutln(out, bytesOut, "</select>");
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void getSections(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String mfr, String chapter, String current, int[] bytesOut)
                           throws Exception
  {
    scoutln(out, bytesOut, "<select name='section' onchange='javascript:pfetch(this)'>");
    
    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT DISTINCT Section FROM catalogc WHERE Manufacturer = '" + generalUtils.sanitiseForSQL(mfr) + "' AND Chapter = '"
                             + generalUtils.sanitiseForSQL(chapter) + "' ORDER BY Position");

      String section;
      
      while(rs.next())
      {
        section = rs.getString(1);
        scout(out, bytesOut, "<option value='" +  generalUtils.sanitise(section) + "'");
        if(current.equals(section))
          scout(out, bytesOut, " selected");
        scoutln(out, bytesOut, ">" + section);
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

    scoutln(out, bytesOut, "</select>");
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void getPages(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String mfr, String chapter, String section, String current,
                        int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<select name='pageName'>");
    
    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT Page FROM catalogc WHERE Manufacturer = '" + generalUtils.sanitiseForSQL(mfr) + "' AND Chapter = '"
                             + generalUtils.sanitiseForSQL(chapter) + "' AND Section = '" + generalUtils.sanitiseForSQL(section) + "' ORDER BY Position");

      String page;
      
      while(rs.next())
      {
        page = rs.getString(1);
        scout(out, bytesOut, "<option value='" + generalUtils.sanitise(page) + "'");
        if(current.equals(page))
          scout(out, bytesOut, " selected");
        scoutln(out, bytesOut, ">" + page);
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

    scoutln(out, bytesOut, "</select>");
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
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

}
