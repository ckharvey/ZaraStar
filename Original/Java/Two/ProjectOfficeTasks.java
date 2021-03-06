// =======================================================================================================================================================================================================
// System: ZaraStar Project: Office Tasks
// Module: ProjectOfficeTasks.java
// Author: C.K.Harvey
// Copyright (c) 2003-07 Christopher Harvey. All Rights Reserved.
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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class ProjectOfficeTasks extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  ProjectUtils projectUtils = new ProjectUtils();

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
      p1  = req.getParameter("p1"); // projectCode
      
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "ProjectOfficeTasks", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6803, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String p1, String unm, String sid, String uty, String men,
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

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 6800, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "6803", imagesDir, localDefnsDir,
                         defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6803, bytesOut[0], 0, "ACC:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "6803", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6803, bytesOut[0], 0, "SID:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }
  
    create(con, stmt, rs, out, req, p1, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 6803, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void create(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String p1, String unm, String sid, String uty,
                      String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut)
                      throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Project: Office Tasks</title>");
    
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
    scoutln(out, bytesOut, "function update(newOrEdit)");
    scoutln(out, bytesOut, "{");
    scoutln(out, bytesOut, "var code=sanitise(document.forms[0].code.value);");
    scoutln(out, bytesOut, "var taskNumber=sanitise(document.forms[0].taskNumber.value);");
    scoutln(out, bytesOut, "var task=sanitise(document.forms[0].task.value);");
    scoutln(out, bytesOut, "var department=sanitise(document.forms[0].department.value);");
    scoutln(out, bytesOut, "var startDate=sanitise(document.forms[0].startDate.value);");
    scoutln(out, bytesOut, "var expectedFinishDate=sanitise(document.forms[0].expectedFinishDate.value);");
    scoutln(out, bytesOut, "var actualFinishDate=sanitise(document.forms[0].actualFinishDate.value);");
    scoutln(out, bytesOut, "var remark=sanitise(document.forms[0].remark.value);");
    scoutln(out, bytesOut, "var status;if(document.forms[0].status.checked)status='C';else status='O';");      
    scoutln(out, bytesOut, "var url = \"http://" + men + "/central/servlet/ProjectOfficeTasksUpdate?unm=\" + escape('" + unm
                         + "') + \"&sid=\" + escape('" + sid + "') + \"&uty=\" + escape('" + uty
                         + "') + \"&men=\" + escape('" + men + "') + \"&den=\" + escape('" + den + "') + \"&bnm=\" + escape('" + bnm
                         + "') + \"&p1=\" + code + \"&p2=\" + taskNumber + \"&p3=\" + task + \"&p4=\" + department + \"&p5=\" + status+ \"&p6=\" + startDate + \"&p7=\" + expectedFinishDate + \"&p8=\" + actualFinishDate + \"&p9=\" + remark + \"&p10=\" + newOrEdit + \"&dnm=\" + escape('"
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

    scoutln(out, bytesOut, "function del(code,taskNumber)");
    scoutln(out, bytesOut, "{var url = \"http://" + men + "/central/servlet/ProjectOfficeTasksDelete?unm=\" + escape('" + unm
                         + "') + \"&sid=\" + escape('" + sid + "') + \"&uty=\" + escape('" + uty
                         + "') + \"&men=\" + escape('" + men + "') + \"&den=\" + escape('" + den + "') + \"&bnm=\" + escape('" + bnm
                         + "') + \"&p1=\" + escape(code) + \"&p2=\" + escape(taskNumber) + \"&dnm=\" + escape('" + dnm + "');");
    scoutln(out, bytesOut, "initRequest3(url);");
    scoutln(out, bytesOut, "req3.onreadystatechange=processRequest3;");
    scoutln(out, bytesOut, "req3.open(\"GET\", url, true);");
    scoutln(out, bytesOut, "req3.send(null);}");

    scoutln(out, bytesOut, "function processRequest3()");
    scoutln(out, bytesOut, "{if(req3.readyState==4)");
    scoutln(out, bytesOut, "{if(req3.status==200)");
    scoutln(out, bytesOut, "{var res=req3.responseXML.getElementsByTagName(\"res\")[0].childNodes[0].nodeValue;");
    scoutln(out, bytesOut, "if(res.length>0)");
    scoutln(out, bytesOut, "{if(res=='.')");
    scoutln(out, bytesOut, "refetchPage();else{ var messageElement = document.getElementById('msg');");
    scoutln(out, bytesOut, "messageElement.innerHTML = '<span id=\"textErrorLarge\">'+res+'</span>';");
    scoutln(out, bytesOut, "}}}}}");
    
    // fetch rec for edit
    scoutln(out, bytesOut, "var req2;");    
    scoutln(out, bytesOut, "function initRequest2(url)");
    scoutln(out, bytesOut, "{if(window.XMLHttpRequest){req2=new XMLHttpRequest();}else");
    scoutln(out, bytesOut, "if(window.ActiveXObject){req2=new ActiveXObject(\"Microsoft.XMLHTTP\");}}");

    scoutln(out, bytesOut, "function edit(code,taskNumber)");
    scoutln(out, bytesOut, "{var url = \"http://" + men + "/central/servlet/ProjectOfficeTasksEdit?unm=\" + escape('" + unm
                         + "') + \"&sid=\" + escape('" + sid + "') + \"&uty=\" + escape('" + uty
                         + "') + \"&men=\" + escape('" + men + "') + \"&den=\" + escape('" + den + "') + \"&bnm=\" + escape('" + bnm
                           + "') + \"&p1=\" + escape(code) + \"&p2=\" + escape(taskNumber) + \"&dnm=\" + escape('" + dnm + "');");
    scoutln(out, bytesOut, "initRequest2(url);");
    scoutln(out, bytesOut, "req2.onreadystatechange=processRequest2;");
    scoutln(out, bytesOut, "req2.open(\"GET\",url,true);");
    scoutln(out, bytesOut, "req2.send(null);}");

    scoutln(out, bytesOut, "function processRequest2()");
    scoutln(out, bytesOut, "{if(req2.readyState==4)");
    scoutln(out, bytesOut, "{if(req2.status==200)");
    scoutln(out, bytesOut, "{var res=req2.responseXML.getElementsByTagName(\"res\")[0].childNodes[0].nodeValue;");
    scoutln(out, bytesOut, "if(res.length>0)");
    scoutln(out, bytesOut, "{if(res=='.'){");
    scoutln(out, bytesOut, "var code=req2.responseXML.getElementsByTagName(\"code\")[0].childNodes[0].nodeValue;document.forms[0].code.value=code;");
    scoutln(out, bytesOut, "var taskNumber=req2.responseXML.getElementsByTagName(\"taskNumber\")[0].childNodes[0].nodeValue;document.forms[0].taskNumber.value=taskNumber;");
    scoutln(out, bytesOut, "var task=req2.responseXML.getElementsByTagName(\"task\")[0].childNodes[0].nodeValue;document.forms[0].task.value=task;");
    scoutln(out, bytesOut, "var department=req2.responseXML.getElementsByTagName(\"department\")[0].childNodes[0].nodeValue;document.forms[0].department.value=department;");
    scoutln(out, bytesOut, "var startDate=req2.responseXML.getElementsByTagName(\"startDate\")[0].childNodes[0].nodeValue;document.forms[0].startDate.value=startDate;");
    scoutln(out, bytesOut, "var expectedFinishDate=req2.responseXML.getElementsByTagName(\"expectedFinishDate\")[0].childNodes[0].nodeValue;document.forms[0].expectedFinishDate.value=expectedFinishDate;");
    scoutln(out, bytesOut, "var actualFinishDate=req2.responseXML.getElementsByTagName(\"actualFinishDate\")[0].childNodes[0].nodeValue;document.forms[0].actualFinishDate.value=actualFinishDate;");
    scoutln(out, bytesOut, "var remark=req2.responseXML.getElementsByTagName(\"remark\")[0].childNodes[0].nodeValue;document.forms[0].remark.value=remark;");
    scoutln(out, bytesOut, "var status=req2.responseXML.getElementsByTagName(\"status\")[0].childNodes[0].nodeValue;if(status=='C')document.forms[0].status.checked=true;else document.forms[0].status.checked=false;");
    scoutln(out, bytesOut, "var messageElement = document.getElementById('option');");
    scoutln(out, bytesOut, "messageElement.innerHTML='<p><a href=\"javascript:updateE()\">Update the Task</a>';");
    scoutln(out, bytesOut, "messageElement=document.getElementById('codebox');");
    scoutln(out, bytesOut, "messageElement.innerHTML='<p><input type=hidden name=code value=\"'+code+'\">'+code;");
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
    scoutln(out, bytesOut, "this.location.replace(\"/central/servlet/ProjectOfficeTasks?unm=" + unm + "&sid=" + sid + "&uty="
                           + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\");}");
    
    scoutln(out, bytesOut, "</script>");

    int[] hmenuCount = new int[1];
    projectUtils.outputPageFrame(con, stmt, rs, out, req, "6803", "ProjectOfficeTasks", "", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount, bytesOut);

    projectUtils.drawTitle(con, stmt, rs, req, out, "ProjectOfficeTasks", "Project Office Tasks for: " + p1, "6803", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
  
    scoutln(out, bytesOut, "<form><table id=\"page\" border=0 width=100%>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td colspan=3><span id=\"msg\"></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr id=\"pageColumn\">");
    scoutln(out, bytesOut, "<td nowrap><p>&nbsp; Task # &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>&nbsp; Task &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>&nbsp; Department &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>&nbsp; Status &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>&nbsp; Start Date &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>&nbsp; Expected Finish Date &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>&nbsp; Actual Finish Date &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>&nbsp; Remark &nbsp;</td><td></td></tr>");
        
    list(out, p1, dnm, localDefnsDir, defnsDir, bytesOut);
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=8><hr></td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    
    scoutln(out, bytesOut, "<tr><td><span id=\"codebox\"><input type=text name=code size=5 maxlength=20></span></td>");
    scoutln(out, bytesOut, "<td><input type=text name=task size=10 maxlength=40></td>");
    scoutln(out, bytesOut, "<td><input type=text name=department size=10 maxlength=40></td>");
    scoutln(out, bytesOut, "<td nowrap><p><input type=checkbox name=status>&nbsp;&nbsp;Complete</td>");
    scoutln(out, bytesOut, "<td><input type=text name=startDate size=10 maxlength=20></td>");
    scoutln(out, bytesOut, "<td><input type=text name=expectedFinishDate size=10 maxlength=20></td>");
    scoutln(out, bytesOut, "<td><input type=text name=actualFinishDate size=10 maxlength=20></td>");
    scoutln(out, bytesOut, "<td><input type=text name=remark size=20 maxlength=100></td></tr>");
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=3><span id=\"option\"><p><a href=\"javascript:updateN()\">Add the Task</a></span></td></tr>");
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "</table></form>");     scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void list(PrintWriter out, String code, String dnm, String localDefnsDir, String defnsDir, int[] bytesOut)
                    throws Exception
  {
    Connection con = null;
    Statement stmt = null;
    ResultSet rs = null;
    
    try
    {
      String uName    = directoryUtils.getMySQLUserName();
      String passWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();
    
      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_project?user=" + uName + "&password=" + passWord);

      stmt = con.createStatement();
  
      rs = stmt.executeQuery("SELECT * FROM officetasks");

      String taskNumber, task, department, status, startDate, expectedFinishDate, actualFinishDate, remark, cssFormat;
      boolean line1=true;

      while(rs.next())                  
      {
        taskNumber         = rs.getString(1);
        task               = rs.getString(2);
        department         = rs.getString(3);
        status             = rs.getString(4);
        startDate          = rs.getString(5);
        expectedFinishDate = rs.getString(6);
        actualFinishDate   = rs.getString(7);
        remark             = rs.getString(8);
        
        if(line1)
        {
          cssFormat = "line1";
          line1 = false;
        }
        else
        {
          cssFormat = "line2";
          line1 = true;              
        }
                    
        scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\">");
        scoutln(out, bytesOut, "<td align=center>" + taskNumber + "</td>");
        scoutln(out, bytesOut, "<td>" + task + "</td>");
        scoutln(out, bytesOut, "<td>" + department + "</td>");

        if(status.equals("I"))
          status = "InProgress";
        else status = "Complete";            
        scoutln(out, bytesOut, "<td>" + status + "</td>");

        scoutln(out, bytesOut, "<td>" + startDate + "</td>");
        scoutln(out, bytesOut, "<td>" + expectedFinishDate + "</td>");
        scoutln(out, bytesOut, "<td>" + actualFinishDate + "</td>");
        scoutln(out, bytesOut, "<td>" + remark + "</td>");
        
        scoutln(out, bytesOut, "<td nowrap><a href=\"javascript:edit('" + code + "','" + taskNumber + "')\">Edit</a>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
        scoutln(out, bytesOut, "<a href=\"javascript:del('" + code + "','" + taskNumber + "')\">Delete</a></td></tr>");
      }
                 
      if(rs   != null) rs.close();                                 
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }    
  }  

  // -------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
