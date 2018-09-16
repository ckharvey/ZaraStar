// =======================================================================================================================================================================================================
// System: ZaraStar Fax: Main page
// Module: FaxServices.java
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

public class FaxServices extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  FaxUtils faxUtils = new FaxUtils();

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
      int x=0;
      if(url.charAt(x) == 'h' || url.charAt(x) == 'H')
        x += 7;
      String urlBit="";
      while(url.charAt(x) != '\000' && url.charAt(x) != ',' && url.charAt(x) != '/')
        urlBit += url.charAt(x++);
 
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "FaxServices", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 11000, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush(); 
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;
    
    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 11000, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "FaxServices", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 11000, bytesOut[0], 0, "ACC:");
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "FaxServices", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 11000, bytesOut[0], 0, "SID:");
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }
        
    set(con, stmt, rs, out, req, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);
    
    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 11000, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut)
                   throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Faxes</title>");
   
    String cssDirectory = directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir);
            
    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + cssDirectory + "general.css\">");
    
    scoutln(out, bytesOut, "<script javascript=\"JavaScript\">");

    scoutln(out, bytesOut, "function view(type,code){if(type=='Q')s=4019;else if(type=='O')s=4043;else if(type=='P')s=3038;else if(type=='R')s=4080;");
    scoutln(out, bytesOut, "else if(type=='Y')s=5006;else if(type=='Z')s=5016;else if(type=='I')s=4067;else if(type=='A')s=5049;else if(type=='E')s=4205;else if(type=='C')s=4101;");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/_\"+s+\"?unm=" + unm + "&uty=" + uty + "&sid=" + sid + "&men=" + men + "&den="
                           + den + "&dnm=" + dnm + "&p1=\"+code+\"&bnm=" + bnm + "\";}");    
    
    scoutln(out, bytesOut, "function abort(hylafaxCode){");
    scoutln(out, bytesOut, "this.location.replace(\"/central/servlet/FaxAbort?unm=" + unm + "&uty=" + uty + "&sid=" + sid + "&men=" + men + "&den="
                           + den + "&dnm=" + dnm + "&p1=\"+hylafaxCode+\"&bnm=" + bnm + "\");}");

    scoutln(out, bytesOut, "var chkTimerID=null;");
    scoutln(out, bytesOut, "var chkSecs;");
    scoutln(out, bytesOut, "var chkTimerRunning=false;");
    scoutln(out, bytesOut, "var chkDelay=1000;");
    scoutln(out, bytesOut, "var chkReq,chkCount=1;");

    scoutln(out, bytesOut, "function chkInitializeTimer(secs){");
    scoutln(out, bytesOut, "chkSecs=secs;");
    scoutln(out, bytesOut, "chkStopClock();");
    scoutln(out, bytesOut, "chkStartTimer();}");

    scoutln(out, bytesOut, "function chkStopClock(){");
    scoutln(out, bytesOut, "if(chkTimerRunning)clearTimeout(chkTimerID);");
    scoutln(out, bytesOut, "chkTimerRunning=false;}");

    scoutln(out, bytesOut, "function chkStartTimer(){");
    scoutln(out, bytesOut, "if(chkSecs<=0){");
    scoutln(out, bytesOut, "chkStopClock();chk();chkInitializeTimer(100);}");
    scoutln(out, bytesOut, "else{");
    scoutln(out, bytesOut, "var s=(chkSecs/10);");
    scoutln(out, bytesOut, "if(parseInt(s)==parseFloat(s)){");
    scoutln(out, bytesOut, "chkStopClock();chk();chkInitializeTimer(chkSecs-1);}");
    scoutln(out, bytesOut, "else{");
    scoutln(out, bytesOut, "chkSecs=chkSecs-1;");
    scoutln(out, bytesOut, "chkTimerRunning=true;  chkDelay = (chkDelay * (chkCount++));  ");
    scoutln(out, bytesOut, "chkTimerID=self.setTimeout(\"chkStartTimer()\",chkDelay);}}}");
      
    // fetch hylafax info from fax server
    scoutln(out, bytesOut, "var req2;");    
    scoutln(out, bytesOut, "function initRequest2(url)");
    scoutln(out, bytesOut, "{if(window.XMLHttpRequest){req2=new XMLHttpRequest();}else");
    scoutln(out, bytesOut, "if(window.ActiveXObject){req2=new ActiveXObject(\"Microsoft.XMLHTTP\");}}");

    scoutln(out, bytesOut, "function chk(){");
    scoutln(out, bytesOut, "var url = \"http://" + men + "/central/servlet/FaxGetStatusData?unm=\" + escape('" + unm + "') + \"&sid=\" + escape('" + sid
                         + "') + \"&uty=\" + escape('" + uty + "') + \"&men=\" + escape('" + men + "') + \"&den=\" + escape('" + den
                         + "') + \"&bnm=\" + escape('" + bnm + "') + \"&dnm=\" + escape('" + dnm + "');");
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

    scoutln(out, bytesOut, "var bs='<table id=\"page\" cellspacing=2 cellpadding=0 width=100% border=0>'");
    scoutln(out, bytesOut, "bs+='<tr><td>&nbsp;</td></tr>'");
    scoutln(out, bytesOut, "bs+='<tr id=\"pageColumn\">'");
    scoutln(out, bytesOut, "bs+='<td><p><b>Fax Code</td>'");
    scoutln(out, bytesOut, "bs+='<td><p><b>Sender</td>'");
    scoutln(out, bytesOut, "bs+='<td><p><b>Code</td>'");
    scoutln(out, bytesOut, "bs+='<td><p><b>To</td>'");
    scoutln(out, bytesOut, "bs+='<td><p><b>Fax #</td>'");
    scoutln(out, bytesOut, "bs+='<td><p><b>Type</td>'");
    scoutln(out, bytesOut, "bs+='<td><p><b>Code</td>'");
    scoutln(out, bytesOut, "bs+='<td><p><b>Time</td>'");
    scoutln(out, bytesOut, "bs+='<td><p><b>Status</td>'");
    scoutln(out, bytesOut, "bs+='<td><p><b>Action</td></tr>'");
    scoutln(out, bytesOut, "bs+=stuff");
    scoutln(out, bytesOut, "bs+='<tr><td>&nbsp;</td></tr></table>'");
    
    scoutln(out, bytesOut, "var messageElement=document.getElementById('bodyStuff');");
    scoutln(out, bytesOut, "messageElement.innerHTML=bs;");
    scoutln(out, bytesOut, "}}}}}");

    scoutln(out, bytesOut, "</script>");

    int[] hmenuCount = new int[1];
    
    faxUtils.outputPageFrame(con, stmt, rs, out, req, "", "FaxServices", "11000", unm, sid, uty, men, den, dnm, bnm, " chkInitializeTimer(100); ", localDefnsDir, defnsDir, hmenuCount, bytesOut);

    faxUtils.drawTitle(con, stmt, rs, req, out, "Faxes", "11000", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

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

