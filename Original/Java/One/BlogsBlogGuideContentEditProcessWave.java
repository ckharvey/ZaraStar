// =======================================================================================================================================================================================================
// System: ZaraStar Info: Edit BlogGuide
// Module: BlogsBlogGuideContentEditProcess.java
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

public class BlogsBlogGuideContentEditProcessWave extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  BlogGuideUtils blogGuideUtils = new BlogGuideUtils();

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
      p1  = req.getParameter("p1"); // name

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, bytesOut);
    }
    catch(Exception e)
    {
      messagePage.msgScreenW(out, req, 3, unm, sid, uty, men, den, dnm, bnm, "8112a", "", "", e.toString(), "", "", directoryUtils.getSupportDirs('D'), bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8112, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if((! serverUtils.isSysAdmin(con, stmt, rs, unm, dnm, sid, uty, localDefnsDir, defnsDir)) && (! serverUtils.isDBAdmin(con, stmt, rs, unm)))
    {
      messagePage.msgScreenW(out, req, 13, unm, sid, uty, men, den, dnm, bnm, "BlogsBlogGuideContentEditProcess", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8112, bytesOut[0], 0, "ACC:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreenW(out, req, 12, unm, sid, uty, men, den, dnm, bnm, "BlogsBlogGuideContentEditProcess", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8112, bytesOut[0], 0, "SID:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    scoutln(out, bytesOut, "8112\001Blogs\001BlogGuide Edit\001javascript:getHTML('BlogsBlogGuideContentEditProcessWave','')\001\001Y\001\001\003");

    create(con, stmt, rs, out, req, p1, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 8112, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void create(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String name, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir,
                      int[] bytesOut) throws Exception
  {
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
    scoutln(out, bytesOut, "var section=sanitise(document.forms[0].section.value);");
    scoutln(out, bytesOut, "var title=sanitise(document.forms[0].title.value);");
    scoutln(out, bytesOut, "var services=sanitise(document.forms[0].services.value);");
    scoutln(out, bytesOut, "var remark=sanitise(document.forms[0].remark.value);");
    scoutln(out, bytesOut, "var abridged;if(document.forms[0].abridged.checked)abridged='Y';else abridged='N';");
    scoutln(out, bytesOut, "var url=\"http://" + men + "/central/servlet/BlogsBlogGuideContentUpdateWave?unm=\" + escape('" + unm + "') + \"&sid=\" + escape('" + sid + "') + \"&uty=\" + escape('" + uty + "') + \"&men=\" + escape('" + men + "') + \"&den=\" + escape('"
                         + den + "') + \"&bnm=\" + escape('" + bnm + "') + \"&p1=" + generalUtils.sanitise(name) + "&p2=\" + escape(title) + \"&p3=\" + escape(section) + \"&p4=\" + escape(services) + \"&p5=\" + escape(remark) + \"&p6=\" + abridged + \"&p7=\" + newOrEdit + \"&dnm=\" + escape('" + dnm + "');");
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

    scoutln(out, bytesOut, "function del(section){");
    scoutln(out, bytesOut, "var url=\"http://" + men + "/central/servlet/BlogsBlogGuideContentDeleteWave?unm=\" + escape('" + unm + "') + \"&sid=\" + escape('" + sid + "') + \"&uty=\" + escape('" + uty + "') + \"&men=\" + escape('" + men + "') + \"&den=\" + escape('"
                         + den + "') + \"&bnm=\" + escape('" + bnm + "') + \"&p1=" + generalUtils.sanitise(name) + "&p2=\" + escape(section) + \"&dnm=\" + escape('" + dnm + "');");
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

    scoutln(out, bytesOut, "function edit(section){");
    scoutln(out, bytesOut, "var url=\"http://" + men + "/central/servlet/BlogsBlogGuideContentEditFetchWave?unm=\" + escape('" + unm + "') + \"&sid=\" + escape('" + sid + "') + \"&uty=\" + escape('" + uty + "') + \"&men=\" + escape('" + men + "') + \"&den=\" + escape('"
                         + den + "') + \"&bnm=\" + escape('" + bnm + "') + \"&p1=" + generalUtils.sanitise(name) + "&p2=\" + escape(section) + \"&dnm=\" + escape('" + dnm + "');");
    scoutln(out, bytesOut, "initRequest2(url);");
    scoutln(out, bytesOut, "req2.onreadystatechange=processRequest2;");
    scoutln(out, bytesOut, "req2.open('GET',url,true);");
    scoutln(out, bytesOut, "req2.send(null);}");

    scoutln(out, bytesOut, "function processRequest2(){");
    scoutln(out, bytesOut, "if(req2.readyState==4){");
    scoutln(out, bytesOut, "if(req2.status==200){");
    scoutln(out, bytesOut, "var res=req2.responseXML.getElementsByTagName(\"res\")[0].childNodes[0].nodeValue;");
    scoutln(out, bytesOut, "if(res.length>0){");
    scoutln(out, bytesOut, "if(res=='.'){");
    scoutln(out, bytesOut, "var section=req2.responseXML.getElementsByTagName(\"section\")[0].childNodes[0].nodeValue;document.forms[0].section.value=section;");
    scoutln(out, bytesOut, "var title=req2.responseXML.getElementsByTagName('title')[0].childNodes[0].nodeValue;if(title=='.')title='';document.forms[0].title.value=title;");
    scoutln(out, bytesOut, "var services=req2.responseXML.getElementsByTagName('services')[0].childNodes[0].nodeValue;if(services=='.')services='';document.forms[0].services.value=services;");
    scoutln(out, bytesOut, "var remark=req2.responseXML.getElementsByTagName('remark')[0].childNodes[0].nodeValue;if(remark=='.')remark='';document.forms[0].remark.value=remark;");
    scoutln(out, bytesOut, "var abridged=req2.responseXML.getElementsByTagName(\"abridged\")[0].childNodes[0].nodeValue;if(abridged=='Y')document.forms[0].abridged.checked=true;else document.forms[0].abridged.checked=false;");
    scoutln(out, bytesOut, "var messageElement=document.getElementById('option');");
    scoutln(out, bytesOut, "messageElement.innerHTML='<p><a href=\"javascript:updateE()\">Update the Guide</a>';");
    scoutln(out, bytesOut, "messageElement=document.getElementById('sectionBox');");
    scoutln(out, bytesOut, "messageElement.innerHTML='<p><input type=hidden name=section value=\"'+section+'\">'+section;");

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

    scoutln(out, bytesOut, "function refetchPage(){getHTML('BlogsBlogGuideContentEditProcessWave','&p1=" + generalUtils.sanitise(name) + "');}");

    scoutln(out, bytesOut, "</script>");

    int[] hmenuCount = new int[1];
    hmenuCount[0] = 0;

    blogGuideUtils.drawTitleW(con, stmt, rs, req, out, "Blogs - Edit BlogGuide Contents for " + name, "8112", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<form><table id=\"page\" border=0 width=100%>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td colspan=3><span id=\"msg\"></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr id=\"pageColumn\">");
    scoutln(out, bytesOut, "<td><p>Section</td>");
    scoutln(out, bytesOut, "<td><p>Title</td>");
    scoutln(out, bytesOut, "<td><p>Services</td>");
    scoutln(out, bytesOut, "<td><p>Remark</td>");
    scoutln(out, bytesOut, "<td><p>Abridged</td></tr>");

    list(stmt, rs, out, name, dnm, bytesOut);

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=5><hr></td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td valign=top><span id=\"sectionBox\"><input type=text name=section size=10 maxlength=10></span></td>");

    scoutln(out, bytesOut, "<td valign=top><input type=text name=title size=40 maxlength=60></td>");
    scoutln(out, bytesOut, "<td valign=top><input type=text name=services size=20 maxlength=40></td>");
    scoutln(out, bytesOut, "<td valign=top><input type=text name=remark size=40 maxlength=100></td>");
    scoutln(out, bytesOut, "<td nowrap><p><input type=checkbox name=abridged>&nbsp;Abridged</td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=3><span id=\"option\"><p><a href=\"javascript:updateN()\">Add the Section</a></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "</table></form>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void list(Statement stmt, ResultSet rs, PrintWriter out, String name, String dnm, int[] bytesOut) throws Exception
  {
    Connection conInfo = null;

    try
    {
      String uName = directoryUtils.getMySQLUserName();
      String pWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();

      conInfo = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_info?user=" + uName + "&password=" + pWord);

      stmt = conInfo.createStatement();

      rs = stmt.executeQuery("SELECT Section FROM blogguide WHERE Name = '" + generalUtils.sanitiseForSQL(name) + "'");

      String section, orderList, entriesList = "";
      int numEntries = 0;

      while(rs.next())
      {
        entriesList += (rs.getString(1) + "\001");
        ++numEntries;
      }

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();

      orderList = generalUtils.sortDecimalDot(entriesList, numEntries);

      String title, services, remark, cssFormat = "line1", indent, abridged;
      int y;

      for(int x=0;x<numEntries;++x)
      {
        stmt = conInfo.createStatement();

        section = getSectionByPosition(orderList, x, entriesList);

        rs = stmt.executeQuery("SELECT Title, Services, Remark, Abridged FROM blogguide WHERE Name = '" + generalUtils.sanitiseForSQL(name) + "' AND Section = '" + section + "'");

        if(rs.next()) // just-in-case
        {
          title    = rs.getString(1);
          services = rs.getString(2);
          remark   = rs.getString(3);
          abridged = rs.getString(4);

          if(title    == null) title = "";
          if(services == null) services = "";
          if(remark   == null) remark = "";
          if(abridged == null) abridged = "N";

          if(abridged.equals("Y")) abridged = "Abridged"; else abridged = "";

          if(cssFormat.equals("line2")) cssFormat = "line1"; else cssFormat = "line2";

          scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\">");

          indent = "";
          for(y=0;y<section.length();++y)
            if(section.charAt(y) == '.')
              indent += "&nbsp;&nbsp;&nbsp;&nbsp;";

          scoutln(out, bytesOut, "<td><p>" + indent + section + "</td>");
          scoutln(out, bytesOut, "<td nowrap><p>" + indent + title + "</td>");
          scoutln(out, bytesOut, "<td><p>" + services + "</td>");
          scoutln(out, bytesOut, "<td><p>" + remark + "</td>");
          scoutln(out, bytesOut, "<td><p>" + abridged + "</td>");

          scoutln(out, bytesOut, "<td nowrap><a href=\"javascript:edit('" + section + "')\">Edit</a>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href=\"javascript:del('" + section + "')\">Delete</a></td></tr>");
        }

        if(rs   != null) rs.close();
        if(stmt != null) stmt.close();
      }

      if(conInfo  != null) conInfo.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
      if(conInfo  != null) conInfo.close();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String getSectionByPosition(String orderList, int entryWithinOrderList, String entriesList) throws Exception
  {
    int x, y = 0;

    for(x=0;x<entryWithinOrderList;++x)
    {
      while(orderList.charAt(y) != '\001')
        ++y;
      ++y;
    }

    String entryWithinEntriesList = "";
    while(orderList.charAt(y) != '\001')
      entryWithinEntriesList += orderList.charAt(y++);

    int offset = generalUtils.strToInt(entryWithinEntriesList);

    y = 0;
    for(x=0;x<offset;++x)
    {
      while(entriesList.charAt(y) != '\001')
        ++y;
      ++y;
    }

    String thisEntry = "";
    while(entriesList.charAt(y) != '\001')
      thisEntry += entriesList.charAt(y++);

    return thisEntry;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {
    out.println(str);
    bytesOut[0] += (str.length() + 2);
  }

}
