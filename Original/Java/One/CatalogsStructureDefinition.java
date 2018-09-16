// =======================================================================================================================================================================================================
// System: ZaraStar Catalogs: Structure Definition
// Module: CatalogsStructureDefinition.java
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

public class CatalogsStructureDefinition extends HttpServlet
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
      p1  = req.getParameter("p1");
      
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "CatalogsStructureDefinition", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6403, bytesOut[0], 0, "ERR:");
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

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 6403, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "CatalogsStructureDefinition", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6403, bytesOut[0], 0, "ACC:");
      
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "CatalogsStructureDefinition", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6403, bytesOut[0], 0, "SID:");
      
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }
  
    create(con, stmt, rs, out, req, p1, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 6403, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
      
      if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void create(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String mfr, String unm, String sid,
                      String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut)
                      throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>SC Catalogs - Structure</title>");
    
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
    scoutln(out, bytesOut, "var chapter=sanitise(document.forms[0].chapter.value);");
    scoutln(out, bytesOut, "var section=sanitise(document.forms[0].section.value);");
    scoutln(out, bytesOut, "var page=sanitise(document.forms[0].page.value);");
    scoutln(out, bytesOut, "var posn=sanitise(document.forms[0].posn.value);");
    scoutln(out, bytesOut, "var url = \"http://" + men + "/central/servlet/CatalogsSteelclawsUpdate?unm=\" + escape('" + unm + "') + \"&sid=\" + escape('" + sid
                         + "') + \"&uty=\" + escape('" + uty + "') + \"&men=\" + escape('" + men + "') + \"&den=\" + escape('" + den
                         + "') + \"&bnm=\" + escape('" + bnm + "') + \"&p1=\" + escape('" + mfr + "') + \"&p2=\" + escape(chapter) + \"&p3=\" + "
                         + "escape(section) + \"&p4=\" + page + \"&p5=\" + posn + \"&p6=\" + newOrEdit + \"&dnm=\" + escape('" + dnm + "');");
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

    scoutln(out, bytesOut, "function del(chapter,section,page){");
    scoutln(out, bytesOut, "var url = \"http://" + men + "/central/servlet/CatalogsSteelclawsDelete?unm=\" + escape('" + unm + "') + \"&sid=\" + escape('" + sid
                         + "') + \"&uty=\" + escape('" + uty + "') + \"&men=\" + escape('" + men + "') + \"&den=\" + escape('" + den
                         + "') + \"&bnm=\" + escape('" + bnm + "') + \"&p1=\" + escape('" + mfr + "') + \"&p2=\" + escape(chapter) + \"&p3=\""
                         + " + escape(section) + \"&p4=\" + escape(page) + \"&dnm=\" + escape('" + dnm + "');");
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

    scoutln(out, bytesOut, "function edit(chapter,section,page){");
    scoutln(out, bytesOut, "var url=\"http://" + men + "/central/servlet/CatalogsSteelclawsFetch?unm=\" + escape('" + unm + "') + \"&sid=\" + escape('" + sid
                         + "') + \"&uty=\" + escape('" + uty + "') + \"&men=\" + escape('" + men + "') + \"&den=\" + escape('" + den
                         + "') + \"&bnm=\" + escape('" + bnm + "') + \"&p1=\" + escape('" + mfr
                         + "') + \"&p2=\" + escape(chapter) + \"&p3=\" + escape(section) + \"&p4=\" + escape(page) + \"&dnm=\" + escape('" + dnm
                         + "');");
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
    scoutln(out, bytesOut, "var chapter=req2.responseXML.getElementsByTagName('chapter')[0].childNodes[0].nodeValue;document.forms[0].chapter.value=chapter;");
    scoutln(out, bytesOut, "var section=req2.responseXML.getElementsByTagName('section')[0].childNodes[0].nodeValue;document.forms[0].section.value=section;");
    scoutln(out, bytesOut, "var page=req2.responseXML.getElementsByTagName('page')[0].childNodes[0].nodeValue;document.forms[0].page.value=page;");
    scoutln(out, bytesOut, "var posn=req2.responseXML.getElementsByTagName('posn')[0].childNodes[0].nodeValue;document.forms[0].posn.value=posn;");
    scoutln(out, bytesOut, "var messageElement=document.getElementById('txt');");
    scoutln(out, bytesOut, "messageElement.innerHTML='<p><a href=\"javascript:updateE()\">Update the Chapter/Section/Page Entry</a>';");
    scoutln(out, bytesOut, "messageElement=document.getElementById('cbox');");
    scoutln(out, bytesOut, "messageElement.innerHTML='<p><input type=hidden name=chapter value=\"'+chapter+'\">'+chapter;");
    scoutln(out, bytesOut, "messageElement=document.getElementById('sbox');");
    scoutln(out, bytesOut, "messageElement.innerHTML='<p><input type=hidden name=section value=\"'+section+'\">'+section;");
    scoutln(out, bytesOut, "messageElement=document.getElementById('pbox');");
    scoutln(out, bytesOut, "messageElement.innerHTML='<p><input type=hidden name=page value=\"'+page+'\">'+page;");
    scoutln(out, bytesOut, "}else{ var messageElement=document.getElementById('msg');");
    scoutln(out, bytesOut, "messageElement.innerHTML='<span id=\"textErrorLarge\">'+res+'</span>';");
    scoutln(out, bytesOut, "}}}}}");
      
    // copy
    scoutln(out, bytesOut, "function copy(chapter,section,page,posn){");
    scoutln(out, bytesOut, "document.forms[0].chapter.value=chapter;");
    scoutln(out, bytesOut, "document.forms[0].section.value=section;");
    scoutln(out, bytesOut, "document.forms[0].page.value=page;");
    scoutln(out, bytesOut, "document.forms[0].posn.value=posn;}");

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
    scoutln(out, bytesOut, "this.location.replace(\"/central/servlet/CatalogsStructureDefinition?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den
                         + "&dnm=" + dnm + "&p1=" + mfr + "&bnm=" + bnm + "\");}");
    
    scoutln(out, bytesOut, "</script>");

    int[] hmenuCount = new int[1];

    sCCatalogsUtils.outputPageFrame(con, stmt, rs, out, req, "6403", "CatalogsStructureDefinition", unm, sid, uty, men, den, dnm, bnm, "", "", localDefnsDir, defnsDir, hmenuCount, bytesOut);

    sCCatalogsUtils.drawTitle(con, stmt, rs, req, out, mfr, "Catalog Structure for " + mfr, "6403", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
  
    scoutln(out, bytesOut, "<form><table id='page' border=0 width=100%>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td colspan=3><span id='msg'></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr id='pageColumn'>");
    scoutln(out, bytesOut, "<td nowrap><p>&nbsp; Chapter &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>&nbsp; Section &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>&nbsp; Page &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>&nbsp; Position &nbsp;</td></tr>");
        
    list(con, stmt, rs, out, mfr, bytesOut);
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=4><hr></td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    
    scoutln(out, bytesOut, "<tr><td><span id='cbox'><input type=text name=chapter size=30 maxlength=80></span></td>");
    scoutln(out, bytesOut, "<td><span id='sbox'><input type=text name=section size=30 maxlength=80></span></td>");
    scoutln(out, bytesOut, "<td><span id='pbox'><input type=text name=page size=30 maxlength=80></span></td>");
    scoutln(out, bytesOut, "<td><input type=text name=posn size=6 maxlength=10></td></tr>");
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=3><span id='txt'><p><a href=\"javascript:updateN()\">Add the Chapter/Section/Page Entry</a></span></td></tr>");
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "</table></form>");     scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void list(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String mfr, int[] bytesOut) throws Exception
  {
    try
    {
      stmt = con.createStatement();
  
      rs = stmt.executeQuery("SELECT Chapter, Section, Page, Position FROM catalogc WHERE Manufacturer = '" + generalUtils.sanitiseForSQL(mfr)
                           + "' ORDER BY Position");

      String chapter, section, page, posn, cssFormat;
      boolean line1=true;

      while(rs.next())                  
      {
        chapter = rs.getString(1);
        section = rs.getString(2);
        page    = rs.getString(3);
        posn    = rs.getString(4);
        
        if(line1) { cssFormat = "line1"; line1 = false; } else { cssFormat = "line2"; line1 = true; }
                    
        scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\">");
        scoutln(out, bytesOut, "<td nowrap>" + chapter + "</td>");
        scoutln(out, bytesOut, "<td nowrap>" + section + "</td>");
        scoutln(out, bytesOut, "<td nowrap>" + page + "</td>");
        scoutln(out, bytesOut, "<td nowrap>" + posn + "</td>");

        scoutln(out, bytesOut, "<td nowrap><a href=\"javascript:edit('" + generalUtils.sanitise2(chapter) + "','" + generalUtils.sanitise2(section) + "','"
                             + generalUtils.sanitise2(page) + "')\">Edit</a>&nbsp;&nbsp;<a href=\"javascript:copy('" + generalUtils.sanitise2(chapter) + "','"
                             + generalUtils.sanitise(section) + "','" + generalUtils.sanitise2(page) + "','" + posn + "')\">Copy</a>&nbsp;&nbsp;&nbsp;");
        scoutln(out, bytesOut, "<a href=\"javascript:del('" + generalUtils.sanitise2(chapter) + "','" + generalUtils.sanitise2(section) + "','"
                             + generalUtils.sanitise2(page) + "')\">Delete</a></td></tr>");
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
