// =======================================================================================================================================================================================================
// System: ZaraStar Utils: Online users
// Module: OnlineUsersListWave.java
// Author: C.K.Harvey
// Copyright (c) 2002-08 Christopher Harvey. All Rights Reserved.
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

public class OnlineUsersListWave extends HttpServlet
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "OnlineUsers", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 12601, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                    int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);

    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 12601, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "OnlineUsers", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 12601, bytesOut[0], 0, "ACC:");

      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "OnlineUsers", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 12601, bytesOut[0], 0, "SID:");

      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    set(con, stmt, rs, out, req, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 12601, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");

      if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty,
                   String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    scoutln(out, bytesOut, "<script javascript=\"JavaScript\">");

    scoutln(out, bytesOut, "var olTimerID = null;");
    scoutln(out, bytesOut, "var olSecs;");
    scoutln(out, bytesOut, "var olTimerRunning = false;");
    scoutln(out, bytesOut, "var olDelay = 5000;");
    scoutln(out, bytesOut, "var olReq;");

      scoutln(out, bytesOut, "function olInitializeTimer(secs)");
      scoutln(out, bytesOut, "{");
        scoutln(out, bytesOut, "olStopClock();");
        scoutln(out, bytesOut, " try{ ol(); } catch(err) { } ");
        scoutln(out, bytesOut, "olStartTimer();");
      scoutln(out, bytesOut, "}");

      scoutln(out, bytesOut, "function olStopClock()");
      scoutln(out, bytesOut, "{");
        scoutln(out, bytesOut, "if(olTimerRunning) clearTimeout(olTimerID);");
        scoutln(out, bytesOut, "olTimerRunning = false;");
      scoutln(out, bytesOut, "}");

      scoutln(out, bytesOut, "function olStartTimer()");
      scoutln(out, bytesOut, "{");
          scoutln(out, bytesOut, "olTimerRunning = true;");
          scoutln(out, bytesOut, "olTimerID = self.setTimeout(\"olInitializeTimer()\", olDelay);");
      scoutln(out, bytesOut, "}   ");

    // fetch info
    scoutln(out, bytesOut, "var olreq;");
    scoutln(out, bytesOut, "function olinitRequest(url)");
    scoutln(out, bytesOut, "{if(window.XMLHttpRequest){olreq=new XMLHttpRequest();}else");
    scoutln(out, bytesOut, "if(window.ActiveXObject){olreq=new ActiveXObject(\"Microsoft.XMLHTTP\");}}");

    scoutln(out, bytesOut, "function ol(){");
    scoutln(out, bytesOut, "var url = \"http://" + men + "/central/servlet/OnlineUsersa?unm=\" + escape('" + unm + "') + \"&sid=\" + escape('" + sid
                         + "') + \"&uty=\" + escape('" + uty + "') + \"&men=\" + escape('" + men + "') + \"&den=\" + escape('" + den
                         + "') + \"&bnm=\" + escape('" + bnm + "') + \"&dnm=\" + escape('" + dnm + "');");
    scoutln(out, bytesOut, "olinitRequest(url);");
    scoutln(out, bytesOut, "olreq.onreadystatechange=olprocessRequest;");
    scoutln(out, bytesOut, "olreq.open(\"GET\",url,true);");
    scoutln(out, bytesOut, "olreq.send(null);");
    scoutln(out, bytesOut, "}");

    scoutln(out, bytesOut, "function olprocessRequest(){");
    scoutln(out, bytesOut, "if(olreq.readyState==4){");
    scoutln(out, bytesOut, "if(olreq.status==200){");
    scoutln(out, bytesOut, "var res=olreq.responseXML.getElementsByTagName(\"res\")[0].childNodes[0].nodeValue;");
    scoutln(out, bytesOut, "if(res.length>0){");
    scoutln(out, bytesOut, "if(res=='.'){");
    scoutln(out, bytesOut, "var stuff=olreq.responseXML.getElementsByTagName(\"stuff\")[0].childNodes[0].nodeValue;");

    scoutln(out, bytesOut, "var bs='<table id=\"page\" cellspacing=2 cellpadding=0 width=100% border=0>';");
    scoutln(out, bytesOut, "bs+='<tr><td>&nbsp;</td></tr>';");
    scoutln(out, bytesOut, "bs+='<tr id=\"pageColumn\">';");
    scoutln(out, bytesOut, "bs+='<td><p><b>User</td>';");
    scoutln(out, bytesOut, "bs+='<td><p><b>Type</td>';");
    scoutln(out, bytesOut, "bs+='<td><p><b>Status</td>';");
    scoutln(out, bytesOut, "bs+='<td><p><b>Host</td>';");
    scoutln(out, bytesOut, "bs+='<td><p><b>Last Operation</td>';");
    scoutln(out, bytesOut, "bs+='<td><p><b>Detail</td></tr>';");
    scoutln(out, bytesOut, "bs+=stuff;");
    scoutln(out, bytesOut, "bs+='<tr><td>&nbsp;</td></tr></table>';");

    scoutln(out, bytesOut, "var messageElement=document.getElementById('bodyStuff');");
    scoutln(out, bytesOut, "messageElement.innerHTML=bs;");
    scoutln(out, bytesOut, "}}}}}");

    scoutln(out, bytesOut, "</script>");

    scoutln(out, bytesOut, "<span id=\"bodyStuff\"></span>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {
    out.println(str);
    bytesOut[0] += str.length() + 2;
  }

}
