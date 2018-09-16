// =======================================================================================================================================================================================================
// System: ZaraStar Documents: Picking List Queue
// Module: PickingListQueue.java
// Author: C.K.Harvey
// Copyright (c) 2002-09 Christopher Harvey. All Rights Reserved.
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

public class PickingListQueue extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  MiscDefinitions miscDefinitions = new MiscDefinitions();
  DrawingUtils drawingUtils = new DrawingUtils();
  AdminControlUtils adminControlUtils = new AdminControlUtils();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out = null;
    int[] bytesOut = new int[1];
    bytesOut[0] = 0;

    String unm = "", sid = "", uty = "", men = "", den = "", dnm = "", bnm = "";

    try
    {
      directoryUtils.setContentHeaders(res);
      out = res.getWriter();

      unm = req.getParameter("unm");
      sid = req.getParameter("sid");
      uty = req.getParameter("uty");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, bytesOut);
    }
    catch(Exception e)
    {
      StringBuffer url = req.getRequestURL();
      int x = 0;
      if(url.charAt(x) == 'h' || url.charAt(x) == 'H')
        x += 7;
      String urlBit = "";
      while(url.charAt(x) != '\000' && url.charAt(x) != ',' && url.charAt(x) != '/')
        urlBit += url.charAt(x++);

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "_3049", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3049, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush(); 
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir = directoryUtils.getSupportDirs('I');
    String defnsDir = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs = null;

    if(! adminControlUtils.notDisabled(con, stmt, rs, 3049)) //    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 3049, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "_3049", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3049, bytesOut[0], 0, "ACC:");
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    set(con, stmt, rs, out, req, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 3049, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "PL Queue Page (3049)");
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut)
                   throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>PL Queue</title>");

    String cssDirectory = directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir);

    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + cssDirectory + "general.css\">");

    scoutln(out, bytesOut, "<script javascript=\"JavaScript\">");

    scoutln(out, bytesOut, "function view(code){");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/GoodsReceivedPickingList?unm=" + unm + "&uty=" + uty + "&sid=" + sid + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=\"+code+\"&bnm=" + bnm + "\";}");

    String defaultPrinter = miscDefinitions.getPickingListQueueDefaultPrinter(con, stmt, rs);

    scoutln(out, bytesOut, "function defaultPrint(code,count){plCode=code;          var x='var p2=document.forms[0].storeman'+count+'.value;';eval(x);update(code,p2);");
//    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/AdminPrintControl?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p5=_3049" + generalUtils.sanitise("?") + "&p6="
//                         + generalUtils.sanitiseReplacingNewlines("Picking List Queue") + "&p7=" + generalUtils.sanitiseReplacingNewlines(defaultPrinter) + "&p1=PickingListPrint&p2=\"+code;");
    scoutln(out, bytesOut, "}");

    // remove
    scoutln(out, bytesOut, "var req5;");
    scoutln(out, bytesOut, "function initRequest5(url)");
    scoutln(out, bytesOut, "{if(window.XMLHttpRequest){req5=new XMLHttpRequest();}else");
    scoutln(out, bytesOut, "if(window.ActiveXObject){req5=new ActiveXObject(\"Microsoft.XMLHTTP\");}}");

    scoutln(out, bytesOut, "function del(code){");
    scoutln(out, bytesOut, "var url=\"http://" + men + "/central/servlet/PickingListQueueRemove?unm=\" + escape('" + unm + "') + \"&sid=\" + escape('" + sid + "') + \"&uty=\" + escape('" + uty + "') + \"&men=\" + escape('" + men + "') + \"&den=\" + escape('"
                         + den + "') + \"&bnm=\" + escape('" + bnm + "') + \"&p1=\" + code + \"&dnm=\" + escape('" + dnm + "');");
    scoutln(out, bytesOut, "initRequest5(url);");
    scoutln(out, bytesOut, "req5.onreadystatechange=processRequest5;");
    scoutln(out, bytesOut, "req5.open(\"GET\",url,true);");
    scoutln(out, bytesOut, "req5.send(null);}");

    scoutln(out, bytesOut, "function processRequest5(){");
    scoutln(out, bytesOut, "if(req5.readyState==4){");
    scoutln(out, bytesOut, "if(req5.status==200){");
    scoutln(out, bytesOut, "var res=req5.responseXML.getElementsByTagName(\"res\")[0].childNodes[0].nodeValue;");
    scoutln(out, bytesOut, "if(res.length>0){chk();");
    scoutln(out, bytesOut, "}}}}");

    // update
    scoutln(out, bytesOut, "var req3,plCode;");
    scoutln(out, bytesOut, "function initRequest3(url)");
    scoutln(out, bytesOut, "{if(window.XMLHttpRequest){req3=new XMLHttpRequest();}else");
    scoutln(out, bytesOut, "if(window.ActiveXObject){req3=new ActiveXObject(\"Microsoft.XMLHTTP\");}}");

    scoutln(out, bytesOut, "function update(code,storeman){");
    scoutln(out, bytesOut, "var url=\"http://" + men + "/central/servlet/_3049c?unm=\" + escape('" + unm + "') + \"&sid=\" + escape('" + sid + "') + \"&uty=\" + escape('" + uty + "') + \"&men=\" + escape('" + men + "') + \"&den=\" + escape('"
                         + den + "') + \"&bnm=\" + escape('" + bnm + "') + \"&p1=\" + code + \"&p2=\" + storeman + \"&dnm=\" + escape('" + dnm + "');");
    scoutln(out, bytesOut, "initRequest3(url);");
    scoutln(out, bytesOut, "req3.onreadystatechange=processRequest3;");
    scoutln(out, bytesOut, "req3.open(\"GET\",url,true);");
    scoutln(out, bytesOut, "req3.send(null);}");

    scoutln(out, bytesOut, "function processRequest3(){");
    scoutln(out, bytesOut, "if(req3.readyState==4){");
    scoutln(out, bytesOut, "if(req3.status==200){");
    scoutln(out, bytesOut, "var res=req3.responseXML.getElementsByTagName(\"res\")[0].childNodes[0].nodeValue;");
    scoutln(out, bytesOut, "if(res.length>0){");

    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/AdminPrintControl?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p5=_3049" + generalUtils.sanitise("?") + "&p6="
                         + generalUtils.sanitiseReplacingNewlines("Picking List Queue") + "&p7=" + generalUtils.sanitiseReplacingNewlines(defaultPrinter) + "&p1=PickingListPrint&p2=\"+plCode;");
    scoutln(out, bytesOut, "}}}}");

    // ------------------

    scoutln(out, bytesOut, "var chkTimerID=null;");
    scoutln(out, bytesOut, "var chkSecs;");
    scoutln(out, bytesOut, "var chkTimerRunning=false;");
    scoutln(out, bytesOut, "var chkDelay=1000;");
    scoutln(out, bytesOut, "var chkFirst=0;");

    scoutln(out, bytesOut, "function chkInitializeTimer(secs){");
    scoutln(out, bytesOut, "chkSecs=secs;");
    scoutln(out, bytesOut, "chkStopClock();");
    scoutln(out, bytesOut, "chkStartTimer();}");

    scoutln(out, bytesOut, "function chkStopClock(){");
    scoutln(out, bytesOut, "if(chkTimerRunning)clearTimeout(chkTimerID);");
    scoutln(out, bytesOut, "chkTimerRunning=false;}");

    scoutln(out, bytesOut, "function chkStartTimer(){");
    scoutln(out, bytesOut, "if(chkSecs<=0){chkStopClock();chk();chkInitializeTimer(18000);}");
    scoutln(out, bytesOut, "else{");
    scoutln(out, bytesOut, "var s=(chkSecs/60);      ");
    scoutln(out, bytesOut, "if(parseInt(s)==parseFloat(s)){      ");
    scoutln(out, bytesOut, "chkStopClock();chk();chkInitializeTimer(chkSecs-1);}");
    scoutln(out, bytesOut, "else{");
    scoutln(out, bytesOut, "chkSecs=chkSecs-1;");
    scoutln(out, bytesOut, "chkTimerRunning=true;");
    scoutln(out, bytesOut, "chkTimerID=self.setTimeout(\"chkStartTimer()\",chkDelay);}}}");

    // fetch PLs
    scoutln(out, bytesOut, "var req2;");
    scoutln(out, bytesOut, "function initRequest2(url)");
    scoutln(out, bytesOut, "{if(window.XMLHttpRequest){req2=new XMLHttpRequest();}else");
    scoutln(out, bytesOut, "if(window.ActiveXObject){req2=new ActiveXObject(\"Microsoft.XMLHTTP\");}}");

    scoutln(out, bytesOut, "function chk(){++chkFirst;if(chkFirst==1)return;");
    scoutln(out, bytesOut, "var url=\"http://" + men + "/central/servlet/PickingListQueueGet?unm=\" + escape('" + unm + "') + \"&sid=\" + escape('" + sid + "') + \"&uty=\" + escape('" + uty + "') + \"&men=\" + escape('" + men + "') + \"&den=\" + escape('"
                         + den + "') + \"&bnm=\" + escape('" + bnm + "') + \"&dnm=\" + escape('" + dnm + "');");
    scoutln(out, bytesOut, "initRequest2(url);");
    scoutln(out, bytesOut, "req2.onreadystatechange=processRequest2;");
    scoutln(out, bytesOut, "req2.open(\"GET\",url,true);");
    scoutln(out, bytesOut, "req2.send(null);}");

    scoutln(out, bytesOut, "function processRequest2(){");
    scoutln(out, bytesOut, "if(req2.readyState==4){");
    scoutln(out, bytesOut, "if(req2.status==200){");
    scoutln(out, bytesOut, "var res=req2.responseXML.getElementsByTagName(\"res\")[0].childNodes[0].nodeValue;");
    scoutln(out, bytesOut, "if(res.length>0){");
    scoutln(out, bytesOut, "if(res=='.'){");
    scoutln(out, bytesOut, "var stuff=req2.responseXML.getElementsByTagName(\"stuff\")[0].childNodes[0].nodeValue;");

    scoutln(out, bytesOut, "var bs='<form><table id=\"page\" cellspacing=2 cellpadding=0 width=100% border=0>'");
    scoutln(out, bytesOut, "bs+='<tr><td>&nbsp;</td></tr>'");
    scoutln(out, bytesOut, "bs+='<tr id=\"pageColumn\">'");
    scoutln(out, bytesOut, "bs+='<td><p><b>PL Code</td>'");
    scoutln(out, bytesOut, "bs+='<td><p><b>Date Queued</td>'");
    scoutln(out, bytesOut, "bs+='<td><p><b>SalesPerson</td>'");
    scoutln(out, bytesOut, "bs+='<td><p><b>Storeman</td></tr>'");
    scoutln(out, bytesOut, "bs+=stuff");
    scoutln(out, bytesOut, "bs+='<tr><td>&nbsp;</td></tr></table></form>'");

    scoutln(out, bytesOut, "var messageElement=document.getElementById('bodyStuff');");
    scoutln(out, bytesOut, "messageElement.innerHTML=bs;");
    scoutln(out, bytesOut, "}}}}}");

    scoutln(out, bytesOut, "</script>");

    int[] hmenuCount = new int[1];

    drawingUtils.outputPageFrame(con, stmt, rs, out, req, "3049", "", "_3049", unm, sid, uty, men, den, dnm, bnm, " chkInitializeTimer(3600); ", localDefnsDir, defnsDir, hmenuCount, bytesOut);

    drawingUtils.drawTitle(out, false, false, "_3049", "", "Picking Lists", "3049", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<span id='bodyStuff'></span>");

    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir));
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {
    out.println(str);
    bytesOut[0] += str.length() + 2;
  }
}

