// =======================================================================================================================================================================================================
// System: ZaraStar Admin: Servers Definition
// Module: AdminServers.java
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

public class AdminServers extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  DashboardUtils dashboardUtils = new DashboardUtils();
  SignOnAdministrator  signOnAdministrator  = new SignOnAdministrator();

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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "AdminServers", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7003, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men,
                      String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');

    long startTime = new java.util.Date().getTime();

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! serverUtils.isSysAdmin(con, stmt, rs, unm, dnm, sid, uty, localDefnsDir, defnsDir) && ! serverUtils.isDBAdmin(con, stmt, rs, unm))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "7003", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7003, bytesOut[0], 0, "ACC:");
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "7003", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7003, bytesOut[0], 0, "SID:");
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    create(con, stmt, rs, out, unm, sid, uty, men, den, dnm, bnm, imagesDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 7003, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void create(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String unm, String sid, String uty, String men,
                        String den, String dnm, String bnm, String imagesDir, String defnsDir, int[] bytesOut)
                        throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Servers Definition</title>");

    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    // update record to DB
    scoutln(out, bytesOut, "var req4;");
    scoutln(out, bytesOut, "function initRequest4(url)");
    scoutln(out, bytesOut, "{if(window.XMLHttpRequest){req4=new XMLHttpRequest();}else");
    scoutln(out, bytesOut, "if(window.ActiveXObject){req4=new ActiveXObject(\"Microsoft.XMLHTTP\");}}");

    scoutln(out, bytesOut, "function updateN(){update('N');}");
    scoutln(out, bytesOut, "function updateE(){update('E');}");
    scoutln(out, bytesOut, "function update(newOrEdit)");
    scoutln(out, bytesOut, "{");
    scoutln(out, bytesOut, "var name=sanitise(document.forms[0].name.value);");
    scoutln(out, bytesOut, "var type=sanitise(document.forms[0].type.value);");
    scoutln(out, bytesOut, "var intIP=sanitise(document.forms[0].intIP.value);");
    scoutln(out, bytesOut, "var extIP=sanitise(document.forms[0].extIP.value);");
    scoutln(out, bytesOut, "var url = \"http://" + men + "/central/servlet/AdminServersUpdate?unm=\" + escape('" + unm
                         + "') + \"&sid=\" + escape('" + sid + "') + \"&uty=\" + escape('" + uty
                         + "') + \"&men=\" + escape('" + men + "') + \"&den=\" + escape('" + den + "') + \"&bnm=\" + escape('" + bnm
                         + "') + \"&p1=\" + name + \"&p2=\" + type + \"&p3=\" + intIP + \"&p4=\" + extIP + \"&p5=\" + newOrEdit + \"&dnm=\" + escape('"
                         + dnm + "');");
    scoutln(out, bytesOut, "initRequest4(url);");
    scoutln(out, bytesOut, "req4.onreadystatechange = processRequest4;");
    scoutln(out, bytesOut, "req4.open(\"GET\",url,true);");
    scoutln(out, bytesOut, "req4.send(null);}");

    scoutln(out, bytesOut, "function processRequest4()");
    scoutln(out, bytesOut, "{");
    scoutln(out, bytesOut, "if(req4.readyState==4)");
    scoutln(out, bytesOut, "{");
    scoutln(out, bytesOut, "if(req4.status==200)");
    scoutln(out, bytesOut, "{");
    scoutln(out, bytesOut, "var res=req4.responseXML.getElementsByTagName(\"res\")[0].childNodes[0].nodeValue;");
    scoutln(out, bytesOut, "if(res.length>0)");
    scoutln(out, bytesOut, "{");
    scoutln(out, bytesOut, "  if(res=='.')");
    scoutln(out, bytesOut, "    refetchPage();else{var messageElement=document.getElementById('msg');");
    scoutln(out, bytesOut, "    messageElement.innerHTML='<span id=\"textErrorLarge\">'+res+'</span>';");
    scoutln(out, bytesOut, "}}}}}");

    // delete record from DB
    scoutln(out, bytesOut, "var req3;");
    scoutln(out, bytesOut, "function initRequest3(url)");
    scoutln(out, bytesOut, "{if(window.XMLHttpRequest){req3=new XMLHttpRequest();}else");
    scoutln(out, bytesOut, "if(window.ActiveXObject){req3=new ActiveXObject(\"Microsoft.XMLHTTP\");}}");

    scoutln(out, bytesOut, "function del(name)");
    scoutln(out, bytesOut, "{");
    scoutln(out, bytesOut, "var url = \"http://" + men + "/central/servlet/AdminServersDelete?unm=\" + escape('" + unm
                         + "') + \"&sid=\" + escape('" + sid + "') + \"&uty=\" + escape('" + uty
                         + "') + \"&men=\" + escape('" + men + "') + \"&den=\" + escape('" + den + "') + \"&bnm=\" + escape('" + bnm
                         + "') + \"&p1=\" + escape(name) + \"&dnm=\" + escape('" + dnm + "');");
    scoutln(out, bytesOut, "initRequest3(url);");
    scoutln(out, bytesOut, "req3.onreadystatechange=processRequest3;");
    scoutln(out, bytesOut, "req3.open(\"GET\", url, true);");
    scoutln(out, bytesOut, "req3.send(null);}");

    scoutln(out, bytesOut, "function processRequest3()");
    scoutln(out, bytesOut, "{");
    scoutln(out, bytesOut, "if(req3.readyState==4)");
    scoutln(out, bytesOut, "{");
    scoutln(out, bytesOut, "if(req3.status==200)");
    scoutln(out, bytesOut, "{");
    scoutln(out, bytesOut, "var res=req3.responseXML.getElementsByTagName(\"res\")[0].childNodes[0].nodeValue;");
    scoutln(out, bytesOut, "if(res.length>0)");
    scoutln(out, bytesOut, "{");
    scoutln(out, bytesOut, "if(res=='.')");
    scoutln(out, bytesOut, "refetchPage();else{ var messageElement = document.getElementById('msg');");
    scoutln(out, bytesOut, "messageElement.innerHTML = '<span id=\"textErrorLarge\">'+res+'</span>';");
    scoutln(out, bytesOut, "}}}}}");

    // fetch rec for edit
    scoutln(out, bytesOut, "var req2;");
    scoutln(out, bytesOut, "function initRequest2(url)");
    scoutln(out, bytesOut, "{if(window.XMLHttpRequest){req2=new XMLHttpRequest();}else");
    scoutln(out, bytesOut, "if(window.ActiveXObject){req2=new ActiveXObject(\"Microsoft.XMLHTTP\");}}");

    scoutln(out, bytesOut, "function edit(name){");
    scoutln(out, bytesOut, "var url = \"http://" + men + "/central/servlet/AdminServersEdit?unm=\" + escape('" + unm
                         + "') + \"&sid=\" + escape('" + sid + "') + \"&uty=\" + escape('" + uty
                         + "') + \"&men=\" + escape('" + men + "') + \"&den=\" + escape('" + den + "') + \"&bnm=\" + escape('" + bnm
                         + "') + \"&p1=\" + name + \"&dnm=\" + escape('" + dnm + "');");
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
    scoutln(out, bytesOut, "var name=req2.responseXML.getElementsByTagName(\"name\")[0].childNodes[0].nodeValue;document.forms[0].name.value=name;");
    scoutln(out, bytesOut, "var type=req2.responseXML.getElementsByTagName(\"type\")[0].childNodes[0].nodeValue;document.forms[0].type.value=type;");
    scoutln(out, bytesOut, "var intIP=req2.responseXML.getElementsByTagName(\"intIP\")[0].childNodes[0].nodeValue;document.forms[0].rate.value=intIP;");
    scoutln(out, bytesOut, "var extIP=req2.responseXML.getElementsByTagName(\"extIP\")[0].childNodes[0].nodeValue;if(extIP=='.')extIP='';document.forms[0].extIP.value=extIP;");
    scoutln(out, bytesOut, "var messageElement = document.getElementById('option');");
    scoutln(out, bytesOut, "messageElement.innerHTML='<p><a href=\"javascript:updateE()\">Update the Server</a>';");
    scoutln(out, bytesOut, "messageElement=document.getElementById('namebox');");
    scoutln(out, bytesOut, "messageElement.innerHTML='<p><input type=hidden name=name value=\"'+name+'\">'+name;");
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
    scoutln(out, bytesOut, "this.location.replace(\"/central/servlet/AdminServers?unm=" + unm + "&sid=" + sid + "&uty="
                           + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\");}");

    scoutln(out, bytesOut, "</script>");

    signOnAdministrator.heading(out, true, unm, sid, uty, bnm, dnm, men, den, imagesDir, bytesOut);

    dashboardUtils.drawTitle(out, "Servers Definition", "7003", unm, sid, uty, men, den, dnm, bnm, bytesOut);

    scoutln(out, bytesOut, "<form><table id=\"page\" border=0 width=100%>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td colspan=3><span id=\"msg\"></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr id=\"pageColumn\">");
    scoutln(out, bytesOut, "<td nowrap><p>&nbsp; Name &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>&nbsp; Type &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>&nbsp; Internal IP &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>&nbsp; External IP &nbsp;</td><td></td></tr>");

    list(con, stmt, rs, out, bytesOut);

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=4><hr></td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td><span id=\"namebox\"><input type=text name=name size=20 maxlength=40></span></td>");

    scoutln(out, bytesOut, "<td>");
    getTypeDDL(out, bytesOut);
    scoutln(out, bytesOut, "</td>");

    scoutln(out, bytesOut, "<td><input type=text name=intIP size=40 maxlength=60></td>");
    scoutln(out, bytesOut, "<td><input type=text name=extIP size=40 maxlength=60></td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=3><span id=\"option\"><p><a href=\"javascript:updateN()\">Add the Server</a></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "</table></form></body></html>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void list(Connection con, Statement stmt, ResultSet rs, PrintWriter out, int[] bytesOut) throws Exception
  {
    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT * FROM servers ORDER BY Name");

      String name, type, intIP, extIP, cssFormat="";

      while(rs.next())
      {
        name  = rs.getString(1);
        type  = rs.getString(2);
        intIP = rs.getString(3);
        extIP = rs.getString(4);

        if(cssFormat.equals("line1")) cssFormat = "line2"; else cssFormat = "line1";

        scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\">");
        scoutln(out, bytesOut, "<td align=center><p>" + name + "</td>");

        switch(type.charAt(0))
        {
          case 'D' : type = "DataBase"; break;
          case 'S' : type = "Sun Ray";  break;
          case 'M' : type = "Mail";     break;
          case 'N' : type = "NX";       break;
          case 'U' : type = "Utility";  break;
        }
        scoutln(out, bytesOut, "<td><p>" + type + "</td>");

        scoutln(out, bytesOut, "<td><p>" + intIP + "</td>");
        scoutln(out, bytesOut, "<td><p>" + extIP + "</td>");

        scoutln(out, bytesOut, "<td nowrap><a href=\"javascript:edit('" + name + "')\">Edit</a>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
        scoutln(out, bytesOut, "<a href=\"javascript:del('" + name + "')\">Delete</a></td></tr>");
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
  private void getTypeDDL(PrintWriter out, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<select name='type'>");

    scoutln(out, bytesOut, "<option value='D'>DataBase");

    scoutln(out, bytesOut, "<option value='M'>Mail");

    scoutln(out, bytesOut, "<option value='S'>Sun Ray");

    scoutln(out, bytesOut, "<option value='N'>NX");

    scoutln(out, bytesOut, "<option value='U'>Utility");

    scoutln(out, bytesOut, "</select>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {
    out.println(str);
    bytesOut[0] += (str.length() + 2);
  }

}
