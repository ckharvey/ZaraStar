// ===================================================================================================================================================
// System: ZaraStar Admin: Channel Users Definition
// Module: ChannelUsersDefinition.java
// Author: C.K.Harvey
// Copyright (c) 2003-08 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
//
// ===================================================================================================================================================

package org.zarastar.zarastar;


import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.sql.*;

public class ChannelUsersDefinition extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  ConnectionsUtils connectionsUtils = new ConnectionsUtils();

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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "ChannelUsersDefinition", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 12707, bytesOut[0], 0, "ERR:");
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

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 12707, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "ChannelUsersDefinition", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 12707, bytesOut[0], 0, "ACC:");
      
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "ChannelUsersDefinition", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 12707, bytesOut[0], 0, "SID:");
      
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }
  
    create(con, stmt, rs, out, req, unm, sid, uty, men, den, dnm, bnm, imagesDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 12707, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
      
      if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void create(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty,
                      String men, String den, String dnm, String bnm, String imagesDir, String localDefnsDir, String defnsDir, int[] bytesOut)
                      throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Admin - Channel User Definition</title>");
    
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
    scoutln(out, bytesOut, "var name=sanitise(document.forms[0].name.value);");
    scoutln(out, bytesOut, "var user=sanitise(document.forms[0].user.value);");
    scoutln(out, bytesOut, "var domain=sanitise(document.forms[0].domain.value);");
    scoutln(out, bytesOut, "var password=sanitise(document.forms[0].password.value);");
    scoutln(out, bytesOut, "var status;if(document.forms[0].status.checked)status='A';else status='D';");      
    scoutln(out, bytesOut, "var url = \"http://" + men + "/central/servlet/ChannelUsersDefinitionUpdate?unm=\" + escape('" + unm + "') + \"&sid=\" + escape('" + sid
                         + "') + \"&uty=\" + escape('" + uty + "') + \"&men=\" + escape('" + men + "') + \"&den=\" + escape('" + den
                         + "') + \"&bnm=\" + escape('" + bnm
                         + "') + \"&p1=\" + name + \"&p2=\" + user + \"&p3=\" + domain + \"&p4=\" + password + \"&p5=\" + status + \"&p6=\" + newOrEdit + \"&dnm=\" + escape('"
                         + dnm + "');");
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

    scoutln(out, bytesOut, "function edit(name,user,domain){");
    scoutln(out, bytesOut, "var url = \"http://" + men + "/central/servlet/ChannelUsersDefinitionEdit?unm=\" + escape('" + unm + "') + \"&sid=\" + escape('" + sid
                         + "') + \"&uty=\" + escape('" + uty + "') + \"&men=\" + escape('" + men + "') + \"&den=\" + escape('" + den
                         + "') + \"&bnm=\" + escape('" + bnm + "') + \"&p1=\" + name + \"&p2=\" + user + \"&p3=\" + domain + \"&dnm=\" + escape('"
                         + dnm + "');");
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
    scoutln(out, bytesOut, "var user=req2.responseXML.getElementsByTagName(\"user\")[0].childNodes[0].nodeValue;document.forms[0].user.value=user;");
    scoutln(out, bytesOut, "var domain=req2.responseXML.getElementsByTagName(\"domain\")[0].childNodes[0].nodeValue;if(domain=='Y')document.forms[0].domain.checked=true;else document.forms[0].domain.checked=false;");
    scoutln(out, bytesOut, "var password=req2.responseXML.getElementsByTagName(\"password\")[0].childNodes[0].nodeValue;document.forms[0].password.value=password;");
    scoutln(out, bytesOut, "var status=req2.responseXML.getElementsByTagName(\"status\")[0].childNodes[0].nodeValue;if(status=='A')document.forms[0].status.checked=true;else document.forms[0].status.checked=true;");
    scoutln(out, bytesOut, "var messageElement = document.getElementById('option');");
    scoutln(out, bytesOut, "messageElement.innerHTML='<p><a href=\"javascript:updateE()\">Update the Channel User</a>';");
    scoutln(out, bytesOut, "messageElement=document.getElementById('namebox');");
    scoutln(out, bytesOut, "messageElement.innerHTML='<p><input type=hidden name=name value=\"'+name+'\">'+name;");
    scoutln(out, bytesOut, "messageElement=document.getElementById('userbox');");
    scoutln(out, bytesOut, "messageElement.innerHTML='<p><input type=hidden name=user value=\"'+user+'\">'+user;");
    scoutln(out, bytesOut, "messageElement=document.getElementById('domainbox');");
    scoutln(out, bytesOut, "messageElement.innerHTML='<p><input type=hidden name=domain value=\"'+domain+'\">'+domain;");
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
    scoutln(out, bytesOut, "this.location.replace(\"/central/servlet/ChannelUsersDefinition?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den="
                         + den + "&dnm=" + dnm + "&bnm=" + bnm + "\");}");
    
    scoutln(out, bytesOut, "</script>");

    int[] hmenuCount = new int[1];

    connectionsUtils.outputPageFrame(con, stmt, rs, out, req, "ChannelUsersDefinition", "", "12707", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount, bytesOut);

    connectionsUtils.drawTitle(con, stmt, rs, req, out, "Administration - Channel Users Definition", "12707", unm, sid, uty, men, den, dnm, bnm, hmenuCount, localDefnsDir, defnsDir, bytesOut);
  
    scoutln(out, bytesOut, "<form><table id='page' border=0 width=100%>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td colspan=3><span id=\"msg\"></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr id=\"pageColumn\">");
    scoutln(out, bytesOut, "<td nowrap><p>&nbsp; Channel Name &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>&nbsp; User &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>&nbsp; Domain &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>&nbsp; PassWord &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>&nbsp; Status &nbsp;</td><td></td></tr>");
        
    list(out, dnm, localDefnsDir, defnsDir, bytesOut);
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=6><hr></td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    
    scoutln(out, bytesOut, "<tr><td><p><span id=\"namebox\"><input type=text name=name size=30 maxlength=60></span></td>");
    scoutln(out, bytesOut, "<td><p><span id=\"userbox\"><input type=text name=user size=20 maxlength=20></span></td>");
    scoutln(out, bytesOut, "<td><p><span id=\"domainbox\"><input type=text name=domain size=20 maxlength=40></span></td>");
    scoutln(out, bytesOut, "<td><p><input type=text name=password size=20 maxlength=20></td>");
    scoutln(out, bytesOut, "<td nowrap><p><input type=checkbox name=status>&nbsp;&nbsp;Allowed</td></tr>");
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=5><span id=\"option\"><p><a href=\"javascript:updateN()\">Add the Channel User</a></span></td></tr>");
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void list(PrintWriter out, String dnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
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
  
      rs = stmt.executeQuery("SELECT * FROM channelusers ORDER BY Name, Domain, User");

      String name, user, domain, password, status, cssFormat;
      boolean line1=true;

      while(rs.next())                  
      {
        name     = rs.getString(1);
        user     = rs.getString(2);
        domain   = rs.getString(3);
        password = rs.getString(4);
        status   = rs.getString(5);
        
        if(line1) { cssFormat = "line1"; line1 = false; } else { cssFormat = "line2"; line1 = true; }
                    
        scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\">");
        scoutln(out, bytesOut, "<td>" + name + "</td>");
        scoutln(out, bytesOut, "<td>" + user + "</td>");
        scoutln(out, bytesOut, "<td>" + domain + "</td>");
        scoutln(out, bytesOut, "<td>" + password + "</td>");

        if(status.equals("A"))
          status = "Allowed";
        else status = "Disallowed";            
        scoutln(out, bytesOut, "<td>" + status + "</td>");

        scoutln(out, bytesOut, "<td nowrap><a href=\"javascript:edit('" + generalUtils.sanitise(name) + "','" + generalUtils.sanitise(user) + "','"
                               + generalUtils.sanitise(domain) + "')\">Edit</a></td></tr>");
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

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
