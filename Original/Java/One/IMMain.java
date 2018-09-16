// =======================================================================================================================================================================================================
// System: ZaraStar: IM: main page
// Module: IMMain.java
// Author: C.K.Harvey
// Copyright (c) 2000-09 Christopher Harvey. All Rights Reserved.
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

public class IMMain extends HttpServlet
{
  GeneralUtils  generalUtils = new GeneralUtils();
  MessagePage  messagePage = new MessagePage();
  ServerUtils  serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils  directoryUtils = new DirectoryUtils();
  ConnectionsUtils  connectionsUtils = new ConnectionsUtils();
  
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
      sid = req.getParameter("sid");
      uty = req.getParameter("uty");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");
      p1  = req.getParameter("p1"); // newChannel
      p2  = req.getParameter("p2"); // newChannelType
      
      if(p1 == null) p1 = "";
      if(p2 == null) p2 = "";
      
      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, bytesOut);
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

      System.out.println("12700: " + e);
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "IMMain", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 12700, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush(); 
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1, String p2, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;
    
    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "IMMain", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 12700, bytesOut[0], 0, "SID:" + p1+":"+p2);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    set(con, stmt, rs, out, req, p1, p2, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 12700, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1+":"+p2);
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String newChannel, String newChannelType, String unm, String sid, String uty, String men, String den, String dnm,
                   String bnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Open Sessions</title>");
    
    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    scoutln(out, bytesOut, "<script javascript=\"JavaScript\">");

    scoutln(out, bytesOut, "var olTimerID=null;");
    scoutln(out, bytesOut, "var olSecs;");
    scoutln(out, bytesOut, "var olTimerRunning=false;");
    scoutln(out, bytesOut, "var olDelay=5000;");
    
    scoutln(out, bytesOut, "function olInitializeTimer(secs){");
    scoutln(out, bytesOut, "olStopClock();");
    scoutln(out, bytesOut, "try{poll();}catch(err){}");
    scoutln(out, bytesOut, "olStartTimer();}");

    scoutln(out, bytesOut, "function olStopClock(){");
    scoutln(out, bytesOut, "if(olTimerRunning)clearTimeout(olTimerID);");
    scoutln(out, bytesOut, "olTimerRunning=false;}");

    scoutln(out, bytesOut, "function olStartTimer(){");
    scoutln(out, bytesOut, "olTimerRunning=true;");
    scoutln(out, bytesOut, "olTimerID=self.setTimeout(\"olInitializeTimer()\",olDelay);}");

    // get open
    
    scoutln(out, bytesOut, "var ireq,openChannels;");
    scoutln(out, bytesOut, "function iinitRequest(url){");
    scoutln(out, bytesOut, "if(window.XMLHttpRequest){ireq=new XMLHttpRequest();}else");
    scoutln(out, bytesOut, "if(window.ActiveXObject){ireq=new ActiveXObject(\"Microsoft.XMLHTTP\");}}");

    scoutln(out, bytesOut, "function open(){");
    scoutln(out, bytesOut, "var url=\"http://" + men + "/central/servlet/ChannelOpenChats?unm=\" + escape('" + unm + "') + \"&sid=\" + escape('" + sid + "') + \"&uty=\" + escape('" + uty + "') + \"&men=\" + escape('" + men + "') + \"&den=\" + escape('"
                         + den + "') + \"&bnm=\" + escape('" + bnm + "') + \"&p1=\" + escape('" + newChannel + "') + \"&p2=" + newChannelType + "&dnm=\" + escape('" + dnm + "');");
    scoutln(out, bytesOut, "iinitRequest(url);");
    scoutln(out, bytesOut, "ireq.onreadystatechange=iprocessRequest;");
    scoutln(out, bytesOut, "ireq.open(\"GET\",url,true);");
    scoutln(out, bytesOut, "ireq.send(null);}");

    scoutln(out, bytesOut, "function iprocessRequest(){");
    scoutln(out, bytesOut, "if(ireq.readyState==4){");
    scoutln(out, bytesOut, "if(ireq.status==200){");
    scoutln(out, bytesOut, "var res=ireq.responseXML.getElementsByTagName(\"res\")[0].childNodes[0].nodeValue;");
    scoutln(out, bytesOut, "if(res.length>0){");
    scoutln(out, bytesOut, "if(res=='.'){");
    scoutln(out, bytesOut, "var stuff=ireq.responseXML.getElementsByTagName(\"stuff\")[0].childNodes[0].nodeValue;");
    scoutln(out, bytesOut, "openChannels=ireq.responseXML.getElementsByTagName(\"open\")[0].childNodes[0].nodeValue;");
    scoutln(out, bytesOut, "if(stuff.length>0){");
    scoutln(out, bytesOut, "var messageElement=document.getElementById('all');");
    scoutln(out, bytesOut, "messageElement.innerHTML+=stuff;}");

    scoutln(out, bytesOut, "var oc=openChannels,j,chan;");
    scoutln(out, bytesOut, "while(oc.length>0){");
    scoutln(out, bytesOut, "j=oc.indexOf('&#1;');chan=oc.substring(0,j);j+=4;oc=oc.substring(j);");
    scoutln(out, bytesOut, "if(chan.length>0){");
    scoutln(out, bytesOut, "messageElement=document.getElementById('prev'+chan);}}");
    
    scoutln(out, bytesOut, "}}}olInitializeTimer(100);}}");

    // poll
    
    scoutln(out, bytesOut, "var olreq;");    
    scoutln(out, bytesOut, "function olinitRequest(url){");
    scoutln(out, bytesOut, "if(window.XMLHttpRequest){olreq=new XMLHttpRequest();}else");
    scoutln(out, bytesOut, "if(window.ActiveXObject){olreq=new ActiveXObject(\"Microsoft.XMLHTTP\");}}");

    scoutln(out, bytesOut, "function poll(){");
    scoutln(out, bytesOut, "var url=\"http://" + men + "/central/servlet/ChannelPolling?unm=\" + escape('" + unm + "') + \"&sid=\" + escape('" + sid + "') + \"&uty=\" + escape('" + uty + "') + \"&men=\" + escape('" + men + "') + \"&den=\" + escape('"
                         + den + "') + \"&bnm=\" + escape('" + bnm + "') + \"&p1=\" + escape(openChannels) + \"&dnm=\" + escape('" + dnm + "');");
    scoutln(out, bytesOut, "olinitRequest(url);");
    scoutln(out, bytesOut, "olreq.onreadystatechange=olprocessRequest;");
    scoutln(out, bytesOut, "olreq.open(\"GET\",url,true);");
    scoutln(out, bytesOut, "olreq.send(null);}");

    scoutln(out, bytesOut, "function olprocessRequest(){");
    scoutln(out, bytesOut, "if(olreq.readyState==4){");
    scoutln(out, bytesOut, "if(olreq.status==200){");
    scoutln(out, bytesOut, "var res=olreq.responseXML.getElementsByTagName(\"res\")[0].childNodes[0].nodeValue;");
    scoutln(out, bytesOut, "if(res.length>0){");
    scoutln(out, bytesOut, "if(res=='.'){");
    scoutln(out, bytesOut, "var toUsers=olreq.responseXML.getElementsByTagName(\"toUsers\")[0].childNodes[0].nodeValue;");
    scoutln(out, bytesOut, "var stuff=olreq.responseXML.getElementsByTagName(\"stuff\")[0].childNodes[0].nodeValue;");
    scoutln(out, bytesOut, "var newStuff=olreq.responseXML.getElementsByTagName(\"newStuff\")[0].childNodes[0].nodeValue;");
    scoutln(out, bytesOut, "openChannels=olreq.responseXML.getElementsByTagName(\"open\")[0].childNodes[0].nodeValue;");
   
    scoutln(out, bytesOut, "var messageElement;");
    scoutln(out, bytesOut, "if(newStuff.length>0){");
    scoutln(out, bytesOut, "messageElement=document.getElementById('newBit');");
    scoutln(out, bytesOut, "messageElement.innerHTML+=newStuff;}");
    
    scoutln(out, bytesOut, "if(toUsers!='.'){var x=0,j,toUser,stuffUser,len=toUsers.length;while(x<len){toUser='';");
    scoutln(out, bytesOut, "while(x<len&&toUsers.charAt(x)!=':')toUser+=toUsers.charAt(x++);++x;");
    scoutln(out, bytesOut, "j=stuff.indexOf('&#1;');stuffUser=stuff.substring(0,j);j+=4;stuff=stuff.substring(j);");
    scoutln(out, bytesOut, "if(stuffUser.length>0){");
    scoutln(out, bytesOut, "messageElement=document.getElementById('prev'+toUser);");
    scoutln(out, bytesOut, "messageElement.innerHTML+=stuffUser;");
    scoutln(out, bytesOut, "}  }}}}}}}");

    // close

    scoutln(out, bytesOut, "var creq;");
    scoutln(out, bytesOut, "function cinitRequest(url){");
    scoutln(out, bytesOut, "if(window.XMLHttpRequest){creq=new XMLHttpRequest();}else");
    scoutln(out, bytesOut, "if(window.ActiveXObject){creq=new ActiveXObject(\"Microsoft.XMLHTTP\");}}");

    scoutln(out, bytesOut, "function close(u,allOrNewBit,id,type){");
    scoutln(out, bytesOut, "closeW(u,allOrNewBit,id);olStopClock();");
    scoutln(out, bytesOut, "var url=\"http://" + men + "/central/servlet/ChannelClose?unm=\" + escape('" + unm + "') + \"&sid=\" + escape('" + sid + "') + \"&uty=\" + escape('" + uty + "') + \"&men=\" + escape('" + men + "') + \"&den=\" + escape('"
                         + den + "') + \"&bnm=\" + escape('" + bnm + "') + \"&p1=\" + escape(u) + \"&p2=\" + type + \"&dnm=\" + escape('" + dnm + "');");
    scoutln(out, bytesOut, "cinitRequest(url);");
    scoutln(out, bytesOut, "creq.onreadystatechange=cprocessRequest;");
    scoutln(out, bytesOut, "creq.open(\"GET\",url,true);");
    scoutln(out, bytesOut, "creq.send(null);}");

    scoutln(out, bytesOut, "function cprocessRequest(){");
    scoutln(out, bytesOut, "if(creq.readyState==4){");
    scoutln(out, bytesOut, "if(creq.status==200){");
    scoutln(out, bytesOut, "var res=creq.responseXML.getElementsByTagName(\"res\")[0].childNodes[0].nodeValue;");
    scoutln(out, bytesOut, "if(res.length>0){");
    scoutln(out, bytesOut, "if(res=='.'){;");
    scoutln(out, bytesOut, "}}olStartTimer();}}}");

    scoutln(out, bytesOut, "function closeW(u,allOrNewBit,id){");
    scoutln(out, bytesOut, "var d=document.getElementById(allOrNewBit);");
    scoutln(out, bytesOut, "var e=document.getElementById(id);");
    scoutln(out, bytesOut, "d.removeChild(e);");
    scoutln(out, bytesOut, "var oc2='',n,i=openChannels.indexOf(\"&#1;\");");
    scoutln(out, bytesOut, "while(i!=-1){");
    scoutln(out, bytesOut, "n=openChannels.substring(0,i);");
    scoutln(out, bytesOut, "if(n!=u)oc2+=(n+'&#1;');");
    scoutln(out, bytesOut, "openChannels=openChannels.substring(i+4);");
    scoutln(out, bytesOut, "i=openChannels.indexOf(\"&#1;\");}");
    scoutln(out, bytesOut, "openChannels=oc2;}");

    // send
    
    scoutln(out, bytesOut, "var sreq;");
    scoutln(out, bytesOut, "function sinitRequest(url){");
    scoutln(out, bytesOut, "if(window.XMLHttpRequest){sreq=new XMLHttpRequest();}else");
    scoutln(out, bytesOut, "if(window.ActiveXObject){sreq=new ActiveXObject(\"Microsoft.XMLHTTP\");}}");

    scoutln(out, bytesOut, "function send(toUser,channelType){olStopClock();");
    scoutln(out, bytesOut, "var s='document.forms[0].in'+toUser+'.value';");
    scoutln(out, bytesOut, "var msg=sanitise(eval(s));");
    scoutln(out, bytesOut, "var url=\"http://" + men + "/central/servlet/ChannelSendMessage?unm=\"+escape('" + unm + "')+\"&sid=\" + escape('" + sid + "')+\"&uty=\" + escape('" + uty + "')+\"&men=\"+escape('" + men + "')+\"&den=\" + escape('" + den
                         + "')+\"&bnm=\" + escape('" + bnm + "')+\"&p1=\"+msg+\"&p2=\"+channelType+\"&p3=\"+toUser+\"&dnm=\"+escape('" + dnm + "');");
    scoutln(out, bytesOut, "sinitRequest(url);");
    scoutln(out, bytesOut, "sreq.onreadystatechange=sprocessRequest;");
    scoutln(out, bytesOut, "sreq.open(\"GET\",url,true);");
    scoutln(out, bytesOut, "sreq.send(null);}");

    scoutln(out, bytesOut, "function sprocessRequest(){");
    scoutln(out, bytesOut, "if(sreq.readyState==4){");
    scoutln(out, bytesOut, "if(sreq.status==200){");
    scoutln(out, bytesOut, "var res=sreq.responseXML.getElementsByTagName(\"res\")[0].childNodes[0].nodeValue;");
    scoutln(out, bytesOut, "if(res.length>0){");
    scoutln(out, bytesOut, "if(res=='.'){");
    scoutln(out, bytesOut, "var stuff=sreq.responseXML.getElementsByTagName(\"stuff\")[0].childNodes[0].nodeValue;");
    scoutln(out, bytesOut, "var toUser=sreq.responseXML.getElementsByTagName(\"toUser\")[0].childNodes[0].nodeValue;");

    scoutln(out, bytesOut, "if(stuff.length>0){");
    scoutln(out, bytesOut, "var messageElement=document.getElementById('prev'+toUser);");
    scoutln(out, bytesOut, "messageElement.innerHTML+=stuff;");
    scoutln(out, bytesOut, "messageElement=document.getElementById('in'+toUser);");
    scoutln(out, bytesOut, "messageElement.value='';");
    scoutln(out, bytesOut, "messageElement.focus();");
    scoutln(out, bytesOut, "}}}olStartTimer();}}}");

    // prune

    scoutln(out, bytesOut, "var preq;");
    scoutln(out, bytesOut, "function pinitRequest(url){");
    scoutln(out, bytesOut, "if(window.XMLHttpRequest){preq=new XMLHttpRequest();}else");
    scoutln(out, bytesOut, "if(window.ActiveXObject){preq=new ActiveXObject(\"Microsoft.XMLHTTP\");}}");

    scoutln(out, bytesOut, "function prune(u,type){");
    scoutln(out, bytesOut, "olStopClock();");
    scoutln(out, bytesOut, "var url=\"http://" + men + "/central/servlet/ChannelPrune?unm=\" + escape('" + unm + "') + \"&sid=\" + escape('" + sid + "') + \"&uty=\" + escape('" + uty + "') + \"&men=\" + escape('" + men + "') + \"&den=\" + escape('"
                         + den + "') + \"&bnm=\" + escape('" + bnm + "') + \"&p1=\" + escape(u) + \"&p2=\" + type + \"&dnm=\" + escape('" + dnm + "');");
    scoutln(out, bytesOut, "pinitRequest(url);");
    scoutln(out, bytesOut, "preq.onreadystatechange=pprocessRequest;");
    scoutln(out, bytesOut, "preq.open(\"GET\",url,true);");
    scoutln(out, bytesOut, "preq.send(null);}");

    scoutln(out, bytesOut, "function pprocessRequest(){");
    scoutln(out, bytesOut, "if(preq.readyState==4){");
    scoutln(out, bytesOut, "if(preq.status==200){");
    scoutln(out, bytesOut, "var res=preq.responseXML.getElementsByTagName(\"res\")[0].childNodes[0].nodeValue;");
    scoutln(out, bytesOut, "if(res.length>0){");
    scoutln(out, bytesOut, "if(res=='.'){");
    scoutln(out, bytesOut, "var toUser=preq.responseXML.getElementsByTagName(\"toUser\")[0].childNodes[0].nodeValue;");
    scoutln(out, bytesOut, "var stuff=preq.responseXML.getElementsByTagName(\"stuff\")[0].childNodes[0].nodeValue;");
    scoutln(out, bytesOut, "if(stuff.length>0){");
    scoutln(out, bytesOut, "messageElement=document.getElementById('prev'+toUser);");
    scoutln(out, bytesOut, "messageElement.innerHTML=stuff;}");    
    scoutln(out, bytesOut, "}}olStartTimer();}}}");

    //

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

    scoutln(out, bytesOut, "function history(name,type){");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/ChannelHistorical?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=\" + type + \"&p1=\"+name;}");

    scoutln(out, bytesOut, "</script>");

    int[] hmenuCount = new int[1];
    connectionsUtils.outputPageFrame(con, stmt, rs, out, req, "", "IMMain", "12700", unm, sid, uty, men, den, dnm, bnm, " openChannels=''; open(); ", localDefnsDir, defnsDir, hmenuCount, bytesOut);

    connectionsUtils.drawTitle(con, stmt, rs, req, out, "Online Sessions Currently Open", "12700", unm, sid, uty, men, den, dnm, bnm, hmenuCount, localDefnsDir, defnsDir, bytesOut);

    scoutln(out, bytesOut, "<form><br><span id='all'></span><span id='newBit'>");

    scoutln(out, bytesOut, "</span>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir));
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
