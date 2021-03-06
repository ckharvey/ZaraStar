// =======================================================================================================================================================================================================
// System: ZaraStar Admin: Document Locks definition
// Module: AdminDocumentLocks.java
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
import java.sql.Statement;

public class AdminDocumentLocks extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();

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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "AdminDocumentLocks", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7033, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                    int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
     
   Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 7033, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "AdminDocumentLocks", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7033, bytesOut[0], 0, "ACC:");
      
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "AdminDocumentLocks", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7033, bytesOut[0], 0, "SID:");
      
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }
  
    create(con, stmt, rs, out, req, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 7033, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
      
      if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void create(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men,
                      String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Admin - Document Locks Definition</title>");
    
    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\""
                           + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">"); 
    
    // update record to DB
    scoutln(out, bytesOut, "var req4;");    
    scoutln(out, bytesOut, "function initRequest4(url)");
    scoutln(out, bytesOut, "{if(window.XMLHttpRequest){req4=new XMLHttpRequest();}else");
    scoutln(out, bytesOut, "if(window.ActiveXObject){req4=new ActiveXObject(\"Microsoft.XMLHTTP\");}}");

    scoutln(out, bytesOut, "function updateE(){");
    scoutln(out, bytesOut, "var docAbbrev=sanitise(document.forms[0].docAbbrev.value);");
    scoutln(out, bytesOut, ";var lockedUpto=sanitise(document.forms[0].lockedUpto.value);");
    scoutln(out, bytesOut, "var openTo=sanitise(document.forms[0].openTo.value);");
    scoutln(out, bytesOut, "var url=\"http://" + men + "/central/servlet/AdminDocumentLocksUpdate?unm=\" + escape('" + unm + "') + \"&sid=\" + escape('" + sid
                         + "') + \"&uty=\" + escape('" + uty + "') + \"&men=\" + escape('" + men + "') + \"&den=\" + escape('" + den
                         + "') + \"&bnm=\" + escape('" + bnm
                         + "') + \"&p1=\" + docAbbrev + \"&p2=\" + lockedUpto + \"&p3=\" + escape(openTo) + \"&dnm=\" + escape('" + dnm + "');");
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
    
    // fetch rec for edit
    scoutln(out, bytesOut, "var req2;");    
    scoutln(out, bytesOut, "function initRequest2(url)");
    scoutln(out, bytesOut, "{if(window.XMLHttpRequest){req2=new XMLHttpRequest();}else");
    scoutln(out, bytesOut, "if(window.ActiveXObject){req2=new ActiveXObject(\"Microsoft.XMLHTTP\");}}");

    scoutln(out, bytesOut, "function edit(name){");
    scoutln(out, bytesOut, "var url = \"http://" + men + "/central/servlet/AdminDocumentLocksDefinitionEdit?unm=\" + escape('" + unm + "') + \"&sid=\" + escape('" + sid
                         + "') + \"&uty=\" + escape('" + uty + "') + \"&men=\" + escape('" + men + "') + \"&den=\" + escape('" + den
                         + "') + \"&bnm=\" + escape('" + bnm + "') + \"&p1=\" + name + \"&dnm=\" + escape('" + dnm + "');");
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
    scoutln(out, bytesOut, "var lockedUpto=req2.responseXML.getElementsByTagName(\"lockedUpto\")[0].childNodes[0].nodeValue;if(lockedUpto=='.')lockedUpto='';document.forms[0].lockedUpto.value=lockedUpto;");
    scoutln(out, bytesOut, "var docAbbrev=req2.responseXML.getElementsByTagName(\"docAbbrev\")[0].childNodes[0].nodeValue;");
    scoutln(out, bytesOut, "var docName=req2.responseXML.getElementsByTagName(\"docName\")[0].childNodes[0].nodeValue;//document.forms[0].docName.value=docName;");
    scoutln(out, bytesOut, "var openTo=req2.responseXML.getElementsByTagName(\"openTo\")[0].childNodes[0].nodeValue;if(openTo=='.')openTo='';document.forms[0].openTo.value=openTo;");
    if(bnm.charAt(0) == 'M')
    {
      scoutln(out, bytesOut, "document.getElementById('lockedUpto').style.visibility='visible';");
      scoutln(out, bytesOut, "document.getElementById('openTo').style.visibility='visible';");
    }   
    else
    {
      scoutln(out, bytesOut, "document.forms[0].lockedUpto.style.visibility='visible';");
      scoutln(out, bytesOut, "document.forms[0].openTo.style.visibility='visible';");
    }
    scoutln(out, bytesOut, "var messageElement=document.getElementById('option');");
    scoutln(out, bytesOut, "messageElement.innerHTML='<p><a href=\"javascript:updateE()\">Update the Document</a>';");
    scoutln(out, bytesOut, "messageElement=document.getElementById('namebox');");
    scoutln(out, bytesOut, "messageElement.innerHTML='<p><input type=hidden name=docAbbrev value=\"'+docAbbrev+'\">'+docName;");
    scoutln(out, bytesOut, "}else{ var messageElement=document.getElementById('msg');");
    scoutln(out, bytesOut, "messageElement.innerHTML='<span id=\"textErrorLarge\">'+res+'</span>';");
    scoutln(out, bytesOut, "}}}}}");
      
    scoutln(out, bytesOut, "function sanitise(code){");
    scoutln(out, bytesOut, "var code2='';var len=code.length;");
    scoutln(out, bytesOut, "for(var x=0;x<len;++x)");
    scoutln(out, bytesOut, "if(code.charAt(x)=='#')code2+='%23';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='\"')code2+='%22';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='&')code2+='%26';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='%')code2+='%25';");
    scoutln(out, bytesOut, "else if(code.charAt(x)==' ')code2+='%20';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='+')code2+='%2b';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='?')code2+='%3F';");
    scoutln(out, bytesOut, "else code2+=code.charAt(x);");
    scoutln(out, bytesOut, "return code2};");

    scoutln(out, bytesOut, "function init(){");
    if(bnm.charAt(0) == 'M')
    {
      scoutln(out, bytesOut, "document.getElementById('lockedUpto').style.visibility='hidden';");
      scoutln(out, bytesOut, "document.getElementById('openTo').style.visibility='hidden';}");
    }   
    else
    {
      scoutln(out, bytesOut, "document.forms[0].lockedUpto.style.visibility='hidden';");
      scoutln(out, bytesOut, "document.forms[0].openTo.style.visibility='hidden';}");
    }
    scoutln(out, bytesOut, "function refetchPage(){");
    scoutln(out, bytesOut, "this.location.replace(\"/central/servlet/AdminDocumentLocks?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den
                        + "&dnm=" + dnm + "&bnm=" + bnm + "\");}");

    scoutln(out, bytesOut, "</script>");
    
    int[] hmenuCount = new int[1];
 
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "7033", "", false, false, "", "", "", "", "", "AdminDocumentLocks", unm, sid, uty, men, den, dnm, bnm, "init();",
                          localDefnsDir, defnsDir, hmenuCount, bytesOut);

    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Administration - Document Locks Definition", "7033", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);
  
    scoutln(out, bytesOut, "<form><table id='page' border=0 width=100%>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td colspan=3><span id=\"msg\"></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr id=\"pageColumn\">");
    scoutln(out, bytesOut, "<td nowrap><p>Document</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Date Locked Upto</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Users With Access</td><td></td></tr>");
        
    list(out, dnm, bytesOut);
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=4><hr></td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    
    scoutln(out, bytesOut, "<tr><td><span id=\"namebox\"></span></td>");
    scoutln(out, bytesOut, "<td><input type=text name=lockedUpto size=10 maxlength=10></td>");
    scoutln(out, bytesOut, "<td nowrap width=90%><p><input type=text name=openTo size=60 maxlength=250></td></tr>");
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=3><span id=\"option\"></span></td></tr>");
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir));
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void list(PrintWriter out, String dnm, int[] bytesOut) throws Exception
  {
    Connection con = null;
    Statement stmt = null;
    ResultSet rs = null;
    
    try
    {
      String uName    = directoryUtils.getMySQLUserName();
      String passWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();
    
      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + uName + "&password=" + passWord);

      stmt = con.createStatement();
  
      rs = stmt.executeQuery("SELECT DocumentName, DocumentAbbrev, LockedUpto, OpenTo FROM locks ORDER BY DocumentName");

      String docName, docAbbrev, lockedUpto, openTo, cssFormat;
      boolean line1=true;

      while(rs.next())                  
      {
        docName    = rs.getString(1);
        docAbbrev  = rs.getString(2);
        lockedUpto = rs.getString(3);
        openTo     = rs.getString(4);
        
        if(line1) { cssFormat = "line1"; line1 = false; } else { cssFormat = "line2"; line1 = true; }
                    
        scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\">");
        scoutln(out, bytesOut, "<td nowrap>" + docName + "</td>");
        scoutln(out, bytesOut, "<td>" + generalUtils.convertFromYYYYMMDD(lockedUpto) + "</td>");
        scoutln(out, bytesOut, "<td>" + openTo + "</td>");

        scoutln(out, bytesOut, "<td nowrap><a href=\"javascript:edit('" + docAbbrev + "')\">Edit</a></td></tr>");
      }
                 
      if(rs   != null) rs.close();                                 
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }    
  }  

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
